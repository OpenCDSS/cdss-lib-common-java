// ----------------------------------------------------------------------------
// DbaseDataTable - Dbase database table representation
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2001-09-26	Steven A. Malers, RTi	Implement class to allow generic reading
//					and writing of Dbase files.  For field
//					size, use readUnsignedByte() rather than
//					readByte().  The latter returned -2 for
//					a value of 254.  This class is now used
//					when reading/writing ESRI shapefiles.
// 2001-10-12	SAM, RTi		Check the base class _trim_strings
//					method when reading strings.  By default
//					strings will be trimmed.  This makes a
//					lot more sense when joining with GIS
//					files, labelling, etc.
// 2001-12-06	SAM, RTi		Overload write to take the write_record
//					boolean array.  This can be used in
//					conjunction with GIS data to limit
//					writes only to certain records.
// 2003-07-24	SAM, RTi		Change TSDate to DateTime.
// 2003-08-21	J. Thomas Sapienza, RTi	Added close().
// 2007-02-17	SAM, RTi		Incorporate performance enhancements from Ian.
//					Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.Table;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class contains records of data as a DataTable, specifically for Dbase
file data (version IV, although many flags are ignored).  Methods are supplied
to read Dbase header and data records.  The simplest way to use the class is to
call the constructor.  To allow for optimal performance, a table can be read
into memory or can remain open for on-the-fly data reads.  Dbase does not seem
to make a distinction between integer and floating point data (both are numeric)
types, other than the precision for an integer can be set to zero.  This class
currently only returns double precision values for numeric data.  This may
impact code where integer values are cross-referenced to other data (e.g., for
identifiers).  If problems arise, an option may be added to treat numeric
zero-precision values as integer data types.
See:  http://www.dbase.com/Knowledgebase/INT/db7_file_fmt.htm
<b>Note - the getTableRecord() and getTableRecords() methods have not been
implemented for on-the-fly reads.</b>
@see RTi.Util.Table.DataTable
*/
public class DbaseDataTable extends DataTable {

/**
Used for reading and writing.
*/
private EndianRandomAccessFile _raf = null;
/**
Used when reading fields - the buffer includes a char[]
for each field, sized according to the header.
 */
private char _field_buffer[][] = null;
/**
Indicates Dbase field types.
*/
private char _field_type[] = null;
/**
Widths of fields.
*/
private int _field_size[] = null;
/**
Byte position of field within record.
*/
private int _field_byte[] = null;
/**
Precision after period.  Used for numeric types.
*/
private int _field_precision[] = null;
/**
Dbase main header is 32.
*/
private int _header_bytes = 32;
/**
Size of a record, bytes.
*/
private int _record_bytes = 0;

/**
Construct a new data table from the Dbase file.  This version is meant to be
used when reading from an existing Dbase file (currently it is not envisioned
that the file would be updated, although this enhancement may be added in the future).
@param filename Name of Dbase file to read.
@param read_data Indicates whether the data should be read.  The header is
always read.  If true, all the data will be read and stored in memory.  If
false, the header only will be read.  Use subsequent calls to getFieldValue() to read data.
@param remain_open Indicates whether the file should remain open.  If true,
then data values can be read from the file using getFieldValue().
@exception IOException if there is an error reading the file.
*/
public DbaseDataTable ( String filename, boolean read_data, boolean remain_open)
throws IOException
{	// Open the file...
	_raf = new EndianRandomAccessFile ( filename, "r" );
	// Read the header and optionally the data...
	readData ( read_data );
	_haveDataInMemory = read_data;
	// Close the file...
	if ( !remain_open ) {
		// Close the random access file...
		_raf.close();
		_raf = null;
	}
}

/**
Closes the random access file, if it is not null.
*/
public void close() {
	if (_raf != null) {
		try {
			_raf.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/**
Return the field value for the requested record and field index.
@param record_index zero-based index of record.
@param field_index zero_based index of desired field.
@return field value for the specified field of the specified record.
The returned object must be properly cast.
@exception Exception if an index is out of range.
*/
public Object getFieldValue ( long record_index, int field_index )
throws Exception
{	if ( _haveDataInMemory ) {
		// The data for the table are in memory so grab the value...
		int num_recs = _table_records.size();
		int num_fields = _table_fields.size();

		if ( num_recs <= record_index ) {
			throw new Exception ( "Requested record index " +
			record_index + " is not available (only " + num_recs + " are in data." );
		}

		if ( num_fields <= field_index ) {
			throw new Exception ( "Requested field index " + field_index +
			" is not available (only " + num_fields + " are in data." );
		}

		TableRecord tableRecord = (TableRecord)_table_records.get((int)record_index);
		Object o = tableRecord.getFieldValue(field_index);
		tableRecord = null;
		return o;
	}
	else {
	    // Try reading the value from the file...
		return readFieldValue ( record_index, field_index );
	}
}

/**
Read the header and optionally the data from a Dbase file.
@param read_data Indicates whether data should be read (true) or only the header (false).
@exception IOException if there is an error reading the input.
*/
private void readData ( boolean read_data )
throws IOException
{	String routine = "DbaseDataTable.readData";
	int dl = 50;

	// Read the DBF file Header info.

	// Ignore first few data...
	byte []buffer4 = new byte[4];
	_raf.read ( buffer4 );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "First 4 bytes:" +buffer4[0] +
		" " + buffer4[1] + " " + buffer4[2] + " " + buffer4[3] );
	}

	// Get the number of records..

	int nrecords = _raf.readLittleEndianInt ();

	// Set in the base class...
	setNumberOfRecords ( nrecords );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "There are " + nrecords + " records in the DBF file." );
	}

	// Get the header size in bytes (this includes the main header, the
	// field headers, and the single trailing header terminator)...
	_header_bytes = _raf.readLittleEndianShort ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Header size is " + _header_bytes + " bytes." );
	}

	// Get the record size in bytes...
	_record_bytes = _raf.readLittleEndianShort ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Record size is " + _record_bytes + " bytes." );
	}

	// Calculate the number of fields (32 bytes in main header,
	// _header_bytes includes the main header and the field descriptors, 32
	// bytes to describe each field)...

	int nfields = (_header_bytes - 32)/32;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Number of fields is " + nfields );
	}

	// Go to after the header...

	byte [] buffer20 = new byte[20];
	_raf.read ( buffer20 );

	// Now read the field headers..

	String field_name[] = new String[nfields];	// Field names as String
	char field_chars[] = new char[10]; // Field names as char[] 10 chars+\0 in header

	_field_size = new int[nfields]; // Width of field
	_field_byte = new int[nfields]; // Byte position of field within record
	_field_precision = new int[nfields]; // Precision after .  (used for numeric types).
	_field_buffer = new char[nfields][];
	_field_type = new char[nfields];
	int tmp_field_type=0;
	byte [] buffer14 = new byte[14];
	List tableFields = new Vector(nfields);

	// Read the field name...
    int rowSize = 0; // IWS
	for ( int i = 0; i < nfields; i++ ) {
		field_chars[0] = _raf.readLittleEndianChar1();
		field_chars[1] = _raf.readLittleEndianChar1();
		field_chars[2] = _raf.readLittleEndianChar1();
		field_chars[3] = _raf.readLittleEndianChar1();
		field_chars[4] = _raf.readLittleEndianChar1();
		field_chars[5] = _raf.readLittleEndianChar1();
		field_chars[6] = _raf.readLittleEndianChar1();
		field_chars[7] = _raf.readLittleEndianChar1();
		field_chars[8] = _raf.readLittleEndianChar1();
		field_chars[9] = _raf.readLittleEndianChar1();
		// Use trimmed fields just to make output easier to deal with...
		field_name[i] = (new String(field_chars)).trim();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Field [" + i + "] name is \"" + field_name[i] + "\"" );
		}

		// Skip the trailing null...

		_raf.readLittleEndianChar1();

		// Read field type...

		_field_type[i] = _raf.readLittleEndianChar1();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Field type is \"" + _field_type[i] + "\"" );
		}

		// Skip the field address..

		_raf.read ( buffer4 );

		// Get the field size...

        _field_size[i] = _raf.readUnsignedByte();
        rowSize += _field_size[i]; // IWS
		//_field_size[i] = (int)_raf.readByte();
		if ( i == 0 ) {
			_field_byte[i] = 0;
		}
		else {
		    _field_byte[i] = _field_byte[i - 1] + _field_size[i - 1];
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Field size is \"" + _field_size[i] + "\"" );
		}

		// Get the field precision...

		_field_precision[i] = _raf.readUnsignedByte();
		if ( _field_type[i] == 'N' || _field_type[i] == 'F' ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Field precision is \"" + _field_precision[i] + "\"");
			}
		}
		else {
		    // Not a number...
			_field_precision[i] = 0;
		}
		// Allocate a working buffer for reads.
		_field_buffer[i] = new char[_field_size[i]];
		// Skip rest for now...
		_raf.read ( buffer14 );
		if ( _field_type[i] == 'N' ) {
			tmp_field_type = TableField.DATA_TYPE_DOUBLE;
		}
		else if ( _field_type[i] == 'F' ) {
			tmp_field_type = TableField.DATA_TYPE_FLOAT;
		}
		else if ( _field_type[i] == 'I' ) {
			tmp_field_type = TableField.DATA_TYPE_INT;
		}
		else {
		    // treat is if _field_type[i] == 'C'
			tmp_field_type = TableField.DATA_TYPE_STRING;
			// ...but do a check...
			if ( _field_type[i] != 'C' ) {
				Message.printWarning ( 2, routine, "Treating field [" + i + "] " + _field_type[i] + " as C" );
				_field_type[i] = 'C';
			}
		}
		tableFields.add ( new TableField ( tmp_field_type, field_name[i], _field_size[i], _field_precision[i]) );
	}

	field_chars = null;
	field_name = null;
	buffer4 = null;
	buffer14 = null;
	buffer20 = null;

	// Create the attribute table for the fields...

	setTableFields ( tableFields );

	// Header record terminator...

	_raf.readLittleEndianChar1();

	// Now read the records...  For now do not allocate memory.

	if ( !read_data ) {
		return;
	}

	String data_string;
	TableRecord contents = null;

	int i = 0;
    byte[] rowBuffer = new byte[rowSize]; // IWS
	for ( int i_dbf = 0; i_dbf < nrecords; ++i_dbf ) {
		try {
		    // Read deleted flag...
			_raf.readLittleEndianChar1();
			contents = new TableRecord(nfields);
            _raf.readFully(rowBuffer); // IWS
            int rowOffset = 0; // IWS
			for ( i = 0; i < nfields; i++ ) {
                            
                data_string = new String( rowBuffer, rowOffset, _field_size[i]);
                rowOffset += _field_size[i];
				if ( _field_type[i] == 'C' ) {
					// Read string...
                                        
					// IWS
//					for ( j = 0; j < _field_size[i]; j++ ) {
//						_field_buffer[i][j] =
//						_raf.readLittleEndianChar1();
//					}
//					data_string = new String (
//							_field_buffer[i] );
					if ( _trim_strings ) {
						contents.addFieldValue ( data_string.trim() );
					}
					else {
					    contents.addFieldValue ( data_string );
					}
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						    routine, "Field [" + i + "] Data string is \"" + data_string + "\"" );
					}
				}
				else if ( _field_type[i] == 'N' ) {
                    // IWS
//					// Read string and convert to double...
//					for ( j = 0; j < _field_size[i]; j++ ){
//						_field_buffer[i][j] =
//						_raf.readLittleEndianChar1();
//					}
//					data_string = new String (_field_buffer[i] );
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						    routine, "Field [" + i + "] Data string for number is \"" + data_string + "\"" );
					}
					// Sometimes overflow values have
					// "******" so set to zero if there is a problem.
					try {
					    contents.addFieldValue (new Double(data_string.trim()));
					}
					catch (Exception e3) {
						contents.addFieldValue (new Double ( 0.0 ));
						Message.printWarning ( 2, routine, "Field [" + i + "] Record [" +
						i_dbf + "] Invalid data string for number: \"" + data_string + "\"" );
					}
				}
				else if ( _field_type[i] == 'F' ) {
					// Read string and convert to float...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						    routine, "Field [" + i + "] Data string for float is \"" + data_string + "\"" );
					}
					// Sometimes overflow values have
					// "******" so set to zero if there is a problem.
					try {
					    contents.addFieldValue (new Float(data_string.trim()));
					}
					catch (Exception e3) {
						contents.addFieldValue (new Float ( 0.0 ));
						Message.printWarning ( 2, routine, "Field [" + i + "] Record [" +
						i_dbf + "] Invalid data string for float: \"" + data_string + "\"" );
					}
				}
				/*
				else if ( _field_type[i] == 'I' ) {
					// Read string and convert to int...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						    routine, "Field [" + i + "] Data string for int is \"" + data_string + "\"" );
					}
					// Sometimes overflow values have
					// "******" so set to zero if there is a problem.
					try {
					    contents.addFieldValue (new Integer(data_string.trim()));
					}
					catch (Exception e3) {
						contents.addFieldValue (new Integer(0));
						Message.printWarning ( 2, routine, "Field [" + i + "] Record [" +
						i_dbf + "] Invalid data string for integer: \"" + data_string + "\"" );
					}
				}*/
				else {
				    // Not yet implemented.  Problem!
					throw new IOException ( "Field type \"" + _field_type[i] + "\" is not yet implemented." );
				}
			}
			try {
			    addRecord ( contents );
			} catch ( Exception exc ) {
				Message.printWarning ( 2, routine,
				"Unable to add record to table - record [" + i_dbf + "] field [" + i + "]" );
				Message.printWarning ( 2, "", exc );
			}
		}
		catch ( IOException e ) {
			Message.printWarning ( 2, routine, "Exception reading DBF data record [" + i_dbf + "]" );
			throw e;
		}
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Number of fields: " + getNumberOfFields());
		Message.printDebug ( dl, routine, "Number of records read: " + getNumberOfRecords());
	}
}

/**
Return the field value for the requested record and field index by reading
from the file.  This method will overall be a little slower than reading the
file sequentially.  However, for cases where the amount of data is large and/or
zooming in occurs, reading only the needed data will often result in fast
performance.  This method reads the open database binary file.
@param record_index zero-based index of record
@param field_index zero_based index of desired field
@return field value for the specified index of the specified record index
Returned object must be properly cast.
*/
public Object readFieldValue ( long record_index, int field_index )
throws Exception
{	// If _raf is null or other errors occur, just let an exception be thrown.  Assume that all is ok.
	long pos = _header_bytes +
		+ record_index*_record_bytes
		+ _field_byte[field_index] + 1;	// +1 is to position after all the other data
	// Now seek to the position...
	//Message.printStatus ( 1, "", "Reading record " + record_index +
	//" field " + field_index + " from byte " + pos );
	_raf.seek ( pos );
	// Read the value.  In-line this code from above...
	Object o = null;
	int j = 0;
	if ( _field_type[field_index] == 'C' ) {
		// Read string...
		for ( j = 0; j < _field_size[field_index]; j++ ) {
			_field_buffer[field_index][j] = _raf.readLittleEndianChar1();
		}
		if ( _trim_strings ) {
			o = (new String(_field_buffer[field_index])).trim();
		}
		else {
		    o = new String ( _field_buffer[field_index] );
		}
	}
	else if ( _field_type[field_index] == 'N' ) {
		// Read string and convert to number...
		for ( j = 0; j < _field_size[field_index]; j++ ){
			_field_buffer[field_index][j] = _raf.readLittleEndianChar1();
		}
		try {
		    o = new Double ( new String (_field_buffer[field_index] ) );
		}
		catch ( Exception e ) {
			o = new Double ( 0.0 );
			Message.printWarning (2,"DbaseDataTable.readFieldValue",
			"Field [" + field_index + "] Record [" + record_index +
			"] Invalid data string for number: \"" + new String(_field_buffer[field_index]) + "\"" );
		}
	}
	else {
	    // Not yet implemented.  Problem!
		throw new IOException ( "Field type " + _field_type[field_index] + " is not yet implemented." );
	}
	return o;
}

/**
Write a Dbase file given a DataTable.  All records are written.
@param dbf_file Name of dbase file, with or without extension.
@param table Data table to write.
@exception IOException if problem writing to file.
*/
public static void write ( String dbf_file, DataTable table )
throws IOException
{	write ( dbf_file, table, null );
}

/**
Write a Dbase file given a DataTable.
@param dbf_file Name of dbase file, with or without extension.
@param table Data table to write.
@param write_record An array with a length matching the number of records in the
table.  Each boolean in the array indicates whether the record should be
written.  If null, all records are written.
@exception IOException if problem writing to file.
*/
public static void write ( String dbf_file, DataTable table, boolean[] write_record )
throws IOException
{	// Make RandomAccessFile to write out data...

	EndianRandomAccessFile raf_DBF_stream = null;
	if ( (dbf_file.length() > 4) && dbf_file.regionMatches(true,(dbf_file.length() - 4),".dbf",0,4) ) {
		raf_DBF_stream = new EndianRandomAccessFile(dbf_file, "rw");
	}
	else {
	    raf_DBF_stream = new EndianRandomAccessFile(dbf_file + ".dbf", "rw");
	}

	// Write the DBF header 
	// 0 byte is version number 
	// 0x03 is plain .dbf file with no memo

	raf_DBF_stream.writeByte(0x03);

	// 1-3 is date  YYMMDD

	int yr = 101;
	int mo = 1;
	int day = 1;
	DateTime now = new DateTime(DateTime.DATE_CURRENT);
	day = now.getDay();
	mo = now.getMonth();
	yr = now.getYear() - 1900;
	raf_DBF_stream.writeByte(yr);
	raf_DBF_stream.writeByte(mo);
	raf_DBF_stream.writeByte(day);
	now = null;

	// 4-7 # records in file
	int nrecords = table.getNumberOfRecords();
	Message.printStatus ( 2, "", "Total of " + nrecords + " dbf rec" );
	int nrecords_write = 0;
	if ( write_record != null ) {
		// Need to recalculate the records...
		for ( int i = 0; i < nrecords; i++ ) {
			if ( write_record[i] ) {
				++nrecords_write;
			}
		}
	}
	raf_DBF_stream.writeLittleEndianInt(nrecords_write);
	Message.printStatus ( 2, "", "Writing " + nrecords_write + " dbf rec" );

	// 8-9 #bytes in header
	// There are 32 bytes in first part of header and then an additional 32
	// bytes per FIELD descriptor array.  Finally, the record header is
	// closed with an additional terminating byte.
	// So if have only 1 field, the #bytes in header is:
	// 32+32+1 = 65
	int nfields = table.getNumberOfFields();
	raf_DBF_stream.writeLittleEndianShort ( (short)(32 + nfields*32 + 1) );

	// 10-11	#bytes in record
	// The record size is the width of each field plus one byte for the
	// delete field at the front.

	int size = 0;
	for ( int i = 0; i < nfields; i++ ) {
		size += table.getFieldWidth(i);
	}
	size += 1;	// leading delete flag
	raf_DBF_stream.writeLittleEndianShort((short)size);

	// 12-13 - reserved. set to 0
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);

	// 14 flag indicating incomplete transaction
	// 00H transaction ended 01H transaction started
	raf_DBF_stream.writeByte(0x01);

	// 15 encryption flag.  
	// 00H not encrypted  01H encrypted
	raf_DBF_stream.writeByte(0x00);

	// 16-27 reserved
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);

	// 28 Production index file flag
	// 00h is no flag, 01H if flag
	raf_DBF_stream.writeByte(0x00);

	// 29 language driver ID: 0x00 ignored
	// 0x01 437 DOS USA, 0x02 850 DOS Multi-line, 
	// 0X03 1251 Windows ANSI, 0xC8 1250 Windows EE
	raf_DBF_stream.writeByte(0x01);

	// 30-31-reserved . fill with 0
	raf_DBF_stream.writeByte(0x00);
	raf_DBF_stream.writeByte(0x00);

	// 32+ is each file descriptor array

	//start FIELD DESCPRIPTOR ARRAY

	StringBuffer b = new StringBuffer();
	// Put together the format specifier for each field as the fields are processed...
	for ( int ifield = 0; ifield < nfields; ifield++ ) {
		//32-42 field descriptor name (11 bytes, null-terminated)
		b.setLength(0);
		b.append ( table.getFieldName(ifield) );
		// Pad to 11 characters, null, terminated
		if ( b.length() >= 10 ) {
			b.setLength(10);
			b.append('\0');
		}
		else {
		    for ( int i = b.length(); i < 11; i++ ) {
				b.append ( '\0' );
			}
		}
		raf_DBF_stream.writeLittleEndianChar1 ( b.toString() );
	
		// 43 field type.  All numeric types are saved as type 'N'
		int field_type = table.getFieldDataType(ifield);
		if ( field_type == TableField.DATA_TYPE_STRING ) {
			raf_DBF_stream.writeLittleEndianChar1 ('C');
		}
		else if ( field_type == TableField.DATA_TYPE_DOUBLE ) {
		    raf_DBF_stream.writeLittleEndianChar1 ('N');
		}
		else {
			raf_DBF_stream.close();
			throw new IOException ( "Writing TableField type " + field_type + " is not supported." );
		}

		// 44-47 reserved
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);

		// 48 field length
		//raf_DBF_stream.writeUnsignedByte(table.getFieldWidth(ifield));
		raf_DBF_stream.writeByte( table.getFieldWidth(ifield));

		// 49 field decimal count in binary 
		//raf_DBF_stream.writeUnsignedByte(table.getFieldPrecision(ifield));
		raf_DBF_stream.writeByte( table.getFieldPrecision(ifield));

		// 50-51 reserved
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);

		// 52 work area ID  
		raf_DBF_stream.writeByte(0);

		// 53-62 reserved
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);
		raf_DBF_stream.writeByte(0);

		// 63 production MDX field flag- 01H if has index tag in MDX file, 00H if not.
		raf_DBF_stream.writeLittleEndianChar1 ( '\0' );
	}
	b = null;

	// Last byte before data records is the header Record terminator, which is ODH == 13 in decimal.
	raf_DBF_stream.writeLittleEndianChar1 ('\013');

	// Now start with the data records.
	// Each record preceded by 1 space==20h=32 decimal.

	// Get the floats out of the vector that is storing them...
	String outstring = null;
	String [] format_spec = table.getFieldFormats();
	int ifield = 0;
	for ( int i = 0; i < nrecords; i++ ) {
		if ( (write_record != null) && !write_record[i] ) {
			continue;
		}
		// Write delete flag that precedes each record (0x20=space is not deleted, 0x2A=* if deleted)
		raf_DBF_stream.writeLittleEndianChar1(' ');
		for ( ifield = 0; ifield < nfields; ifield++ ) {
			// Write the field data value using the format specification assigned above.  The formatString()
			// method cannot just take an Object so need to cast.
			try {
			    outstring = StringUtil.formatString(table.getFieldValue(i,ifield),format_spec[ifield]);
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, "", e );
				raf_DBF_stream.close();
				raf_DBF_stream = null;
				outstring = null;
				throw new IOException (
				"Error writing record " + i + " field " + ifield + " using " + format_spec[ifield]);
			}
			// The string will be the exact length so can just write the whole thing...
			raf_DBF_stream.writeLittleEndianChar1(outstring);
		}
	}

	// End of file marker 1AH == 26 decimal  or 0x1A
	raf_DBF_stream.writeLittleEndianChar1('\026');

	// Close stream
	raf_DBF_stream.close();
}

}