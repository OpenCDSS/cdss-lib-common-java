// Table - coordinating class for the Table utility

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.Table;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;


/**
This class manages the table group functionality.
Tables can be used to store record-based data.
An example of its is as follows:
<p>

<pre>
try {
	// first, create table definition by assembling vector of
	// TableField objects
	Vector myTableFields = new Vector(3);
	TableField firstTableField = new TableField ( TableField.DATA_TYPE_STRING, "id_label_6" );
	TableField secondTableField = new TableField ( TableField.DATA_TYPE_INT, "labelLength" );
	TableField thirdTableField = new TableField ( TableField.DATA_TYPE_STRING, "aka" );
	myTableFields.addElement ( firstTableField );
	myTableFields.addElement ( secondTableField );
	myTableFields.addElement ( thirdTableField );

	// now define table with one simple call
	Table myTable = new Table ( myTableFields );

	TableRecord contents = new TableRecord (3);
	contents.addFieldValue ( "123456" );
	contents.addFieldValue ( Integer.valueOf (6));
	contents.addFieldValue ( "RTi station" );

	myTable.addRecord ( contents );

	system.out.println ( myTable.getFieldValue ( 0, 2 ));

} catch (Exception e ) {
	// process exception
}
</pre>

@see RTi.Util.Table.TableField
@see RTi.Util.Table.TableRecord
@deprecated Use DataTable.
*/
@Deprecated
public class Table {

private List<TableField> _table_fields;	// List of data types - DATA_TYPE_*.
private List<TableRecord> _table_records;	// List of records.

/**
Construct a new table.
@param tableFieldsVector a list of TableField objects defining table contents
*/
public Table ( List<TableField> tableFieldsVector) {
	initialize ( tableFieldsVector );
}

private void initialize ( List<TableField> tableFieldsVector ) {
	_table_fields = tableFieldsVector;
	_table_records = new Vector<TableRecord>( 10, 10 );
}

/**
Adds a record to the vector of TableRecords.
@param new_record new record to be added
@exception when the number of entries in new_record is not equal to the number of entries in the current TableField declaration
*/
public void addRecord ( TableRecord new_record )
throws Exception {
	int num_table_fields = _table_fields.size();
	int num_new_record_fields = new_record.getNumberOfFields();
	if ( num_new_record_fields == num_table_fields ) {
		_table_records.add ( new_record );
	}
	else {
		throw new Exception ( "Number of records in the new record (" + num_new_record_fields + ") does not match current description of the table fields." );
	}
}

/**
Add a field to the TableField and each entry in TableRecord
@param tableField type of data the new field will contain
*/
public void addField ( TableField tableField ) {
	_table_fields.add ( tableField );

	// Add field to each record.
	int num = _table_records.size();
	TableRecord tableRecord;
	for ( int i=0; i<num; i++ ) {
		tableRecord = (TableRecord)_table_records.get(i);

		// Add element and set default to 0 or "" these are ordered in the most likely types to optimize.
		int data_type = tableField.getDataType();
		if ( data_type == TableField.DATA_TYPE_STRING ) {
			tableRecord.addFieldValue( new String ( "" ));
		}
		else if ( data_type == TableField.DATA_TYPE_INT ) {
			tableRecord.addFieldValue( Integer.valueOf ( 0 ));
		}
		else if ( data_type == TableField.DATA_TYPE_DOUBLE ) {
			tableRecord.addFieldValue( Double.valueOf ( 0 ));
		}
		else if ( data_type == TableField.DATA_TYPE_SHORT ) {
			tableRecord.addFieldValue( Short.valueOf ( "0" ));
		}
		else if ( data_type == TableField.DATA_TYPE_FLOAT ) {
			tableRecord.addFieldValue( Float.valueOf ( 0 ));
		}
	}
}

/**
Returns a field index.
@return Index of table entry associated with the given heading.
*/
public int getFieldIndex ( String heading )
throws Exception {
	int num = _table_fields.size();
	for ( int i=0; i<num; i++ ) {
		if (((TableField)_table_fields.get(i)).
			getName().equalsIgnoreCase(heading))
			return i;
	}

	// If this line is reached, the given heading was never found.
	throw new Exception( "Unable to find field with heading \"" + heading + "\"" );
}

/**
Returns the number of fields in the table.
@return number of fields this table is current representing
*/
public int getNumberOfFields () {
	return _table_fields.size();
}

/**
Returns the number of records within this table.
@return number of records within this table
*/
public int getNumberOfRecords () {
	return _table_records.size();
}

/**
Returns the TableFeld object for the specified zero-based index.
@return TableField object for the specified zero-based index.
*/
public TableField getTableField ( int index ) {
	return ((TableField)_table_fields.get( index ));
}

/**
Returns a field value.
@param record_index zero-based index of record
@param heading title of desired heading
@return field value for the specified heading of the specified record index
Returned object must be properly cast.
*/
public Object getFieldValue ( int record_index, String heading )
throws Exception {
	return getFieldValue ( record_index, getFieldIndex(heading) );
}

/**
Returns a field value.
@param record_index zero-based index of record
@param field_index zero_based index of desired field
@return field value for the specified index of the specified record index
Returned object must be properly cast.
*/
public Object getFieldValue ( int record_index, int field_index )
throws Exception {
	String rtn = "getFieldValue";
	int num_recs = _table_records.size();
	int num_fields = _table_fields.size();

	if ( num_recs <= record_index ) {
		throw new Exception ( "Requested record index " + record_index + " is not available (only " + num_recs + " have been established." );
	}

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index + " is not available (only " + num_fields + " have been established." );
	}

	/* break this up ...
	return (((TableRecord)_table_records.elementAt(record_index)).getFieldValue(field_index));
	*/
	Message.printStatus ( 10, rtn, "Getting table record " + record_index + " from " + num_recs + " available records." );
	TableRecord tableRecord = (TableRecord)_table_records.get(record_index);
	Message.printStatus ( 10, rtn, "Getting table record field." );
	return tableRecord.getFieldValue(field_index);
}

/**
Returns the header title for an index.
@return heading title for specified zero-based index.
*/
public String getHeadingForIndex ( int index ) {
	return ((TableField)_table_fields.get ( index )).getName();
}

/**
Returns the table record at a specified index.
@return TableRecord at specified record_index
*/
public TableRecord getRecord ( int record_index )
throws Exception {
	 if ( _table_records.size() <= record_index ) {
	 	throw new Exception ( "Unable to return TableRecord at index " + record_index );
	 }
	 return ((TableRecord)_table_records.get(record_index));
}

/**
Returns all the table records.
@return vector of TableRecord
*/
public List<TableRecord> getTableRecords ( ) {
	return _table_records;
}

/**
Set table header for the specified zero-based index.
@param index index of field to set
@param header header of the field
*/
public void setTableFieldHeader ( int index, String header )
throws Exception {
	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.get(index);
	tableField.setName ( header );
}

/**
Set field data type for the specified zero-based index.
@param index index of field to set
@param data_type data type; use FieldType.DATA_TYPE_*
*/
public void setTableFieldType ( int index, int data_type )
throws Exception {
	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.get(index);
	tableField.setDataType ( data_type );
}

/**
Set field data type and header for the specified zero-based index.
@param index index of field to set
@param data_type data type; use FieldType.DATA_TYPE_*
@param header header of the field
*/
public void setTableField( int index, int data_type, String header )
throws Exception {
	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.get(index);
	tableField.setDataType ( data_type );
	tableField.setName ( header );
}

/**
Given a clear definition of what data to expect, reads and stores data in table.
@return new table containing data
@param filename name of file containing delimited data
@param delimiter string representing delimiter in data file
@param tableFields vector of TableField objects defining data expectations
@param num_lines_header number of lines in header (typically 1)
*/
public static Table parseDelimitedFile ( String filename, String delimiter,
	List<TableField> tableFields, int num_lines_header )
throws Exception {
	String rtn = "Table.parseDelimitedFile";
	String iline;
	boolean processed_header = false;
	List<String> columns;
	int num_fields=0, type, num_lines_header_read=0;
	Table table;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	try {
	table = new Table( tableFields );
	if ( num_lines_header == 0 )
		processed_header = true;

	while (( iline = in.readLine ()) != null ) {

		// Check if read comment or empty line.
		if ( iline.startsWith("#") || iline.trim().length()==0) {
			continue;
		}

		columns = StringUtil.breakStringList ( iline, delimiter, StringUtil.DELIM_SKIP_BLANKS );

		// Line is part of header.
		if ( !processed_header ) {
			num_fields = columns.size();
			if ( num_fields < tableFields.size() ) {
				throw new IOException ( "table fields specifications do not match data found in file." );
			}

			num_lines_header_read++;
			if ( num_lines_header_read == num_lines_header ) {
				processed_header = true;
			}
		}
		else {
			// Line contains data - store in table as record.
			TableRecord contents = new TableRecord(num_fields);
			try {
			for ( int i=0; i<num_fields; i++ ) {
				type = tableFields.get(i).getDataType();
				if ( type == TableField.DATA_TYPE_STRING ) {
					contents.addFieldValue ( columns.get(i) );
					/*
					currentString = (String)columns.elementAt(i);
					// Strip any double quotes.
					modifiedString = currentString.replace ('\"', ' ' );
					contents.addField ( modifiedString );
					*/
				}
				else if ( type == TableField.DATA_TYPE_DOUBLE ) {
					contents.addFieldValue ( Double.valueOf ( columns.get(i)));
				}
				else if ( type == TableField.DATA_TYPE_INT ) {
					contents.addFieldValue( Integer.valueOf ( columns.get(i)));
				}
				else if ( type == TableField.DATA_TYPE_SHORT ) {
					contents.addFieldValue( Short.valueOf ( columns.get(i)));
				}
				else if ( type == TableField.DATA_TYPE_FLOAT ) {
					contents.addFieldValue( Float.valueOf ( columns.get(i)));
				}
			}
			table.addRecord ( contents );
			} catch ( Exception e ) {
				Message.printWarning ( 2, rtn, e );
			}
		}
	}

	}
	finally {
		if ( in != null ) {
			in.close();
		}
	}
	return table;
}

/**
Reads header of delimited file and return vector of TableField objects
@return list of TableField objects (only header titles will be set)
@param filename name of file containing delimited data
*/
public static List<TableField> parseDelimitedFileHeader ( String filename )
throws Exception {
	return parseDelimitedFileHeader ( filename, "," );
}

/**
Reads header of delimited file and return vector of TableField objects.
The heading titles will be correctly returned.  The data type, however, will be set to TableField.DATA_TYPE_STRING.
This should be changed if not appropriate.
@return vector of TableField objects (heading titles will be correctly set but data type will be string)
@param filename name of file containing delimited data
@param delimiter string representing delimiter in data file
*/
public static List<TableField> parseDelimitedFileHeader ( String filename, String delimiter )
throws Exception {
	String iline;
	List<String> columns;
	List<TableField> tableFields = null;
	int num_fields=0;
	TableField newTableField = null;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	try {
	while (( iline = in.readLine ()) != null ) {

		// Check if read comment or empty line.
		if ( iline.startsWith("#") || iline.trim().length()==0) {
			continue;
		}

		columns = StringUtil.breakStringList ( iline, delimiter, StringUtil.DELIM_SKIP_BLANKS );

		num_fields = columns.size();
		tableFields = new Vector<>( num_fields, 1 );
		for ( int i=0; i<num_fields; i++ ) {
			newTableField = new TableField ( );
			newTableField.setName((String)columns.get(i));
			newTableField.setDataType(TableField.DATA_TYPE_STRING);
			tableFields.add ( newTableField );
		}
		return tableFields;
	}
	}
	finally {
		if ( in != null ) {
			in.close();
		}
	}
	return tableFields;
}

}