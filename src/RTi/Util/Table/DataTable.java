// ----------------------------------------------------------------------------
// DataTable - class to hold tabular data from a database
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 23 Jun 1999	Catherine E.
//		Nutting-Lane, RTi	Initial version
// 2001-09-17	Steven A. Malers, RTi	Change the name of the class from Table
//					to DataTable to avoid conflict with the
//					existing C++ class.  Review code.
//					Remove unneeded messages to increase
//					performance.  Add get methods for fields
//					for use when writing table.  Make data
//					protected to allow extension to derived
//					classes (e.g., DbaseDataTable).
// 2001-10-04	SAM, RTi		Add getFormatFormat(int) to allow
//					operation on a single field.
// 2001-10-10	SAM, RTi		Add getFieldNames().
// 2001-10-12	SAM, RTi		By default, trim character fields.  Add
//					the trimStrings() method to allow an
//					override.  This data member should be
//					checked by the specific read code in
//					derived classes.  Also change the
//					format for strings to %- etc. because
//					strings are normally left justified.
// 2001-12-06	SAM, RTi		Change so that getNumberOfRecords()
//					returns the value of _num_records and
//					not _table_records.size().  The latter
//					produces errors when records are read
//					on the fly.  Classes that allow on-the-
//					fly reads will need to set the number of
//					records.
// 2002-07-27	SAM, RTi		Trim the column names when reading the
//					header.
// 2003-12-16	J. Thomas Sapienza, RTi	* Added code for writing a table out to
//					  a delimited file.
//					* Added code for dumping a table to 
//					  Status level 1 (for debugging).
//					* Added code to trim spaces from values
//					  read in from a table.
// 2003-12-18	JTS, RTi		Added deleteRecord().
// 2004-02-25	JTS, RTi		Added parseFile().
// 2004-03-11	JTS, RTi		Added isDirty().
// 2004-03-15	JTS, RTi		Commented out the DELIM_SKIP_BLANKS 
//					from the delimited file read, so to 
//					allow fields with no data.
// 2004-04-04	SAM, RTi		Fix bug where the first non-comment line
//					was being ignored.
// 2004-08-03	JTS, RTi		Added setFieldValue().
// 2004-08-05	JTS, RTi		Added version of parseDelimitedFile()
//					that takes a parameter specifying the 
//					max number of lines to read from the 
//					file.
// 2004-10-21	JTS, RTi		Added hasField().
// 2004-10-26	JTS, RTi		Added deleteField().
// 2005-01-03	JTS, RTi		* Added setFieldWidth()
//					* When a table is read in with
//					  parseDelimitedFile(), String columns
//					  are now checked for the longest string
//					  and the width of that column is set
//					  so that the entire string will
//					  be displayed.
// 2005-01-27	JTS, RTi		Corrected null pointer bug in 
//					parseDelimitedFile().
// 2005-11-16	SAM, RTi		Add MergeDelimiters and TrimInput
//					properties to parseDelimitedFile().
// 2006-03-02	SAM, RTi		Change so that when on the fly reading
//					is occurring, getTableRecord() returns
//					null.
// 2006-03-13	JTS, RTi		Correct bug so that parsed data tables
//					have _have_data set to true.
// 2006-06-21	SAM, RTi		Change so that when writing a delimited
//					file the contents are quoted if the data
//					contain the delimiter.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.Table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.Vector;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class contains records of data as a table, using a Vector of TableRecord
instances.  The format of the table is defined using the TableField class.
Tables can be used to store record-based data.  This class was originally
implemented to store Dbase files associated with ESRI shapefile GIS data.
Consequently, although a data table theoretically can store a variety of
data types (see TableField), in practice only String and double types are used
for some applications.
Full handling of other data types will be added in the future.
An example of a DataTable instantiation is:
<p>

<pre>
try {	// First, create define the table by assembling a vector of TableField
	// objects...
	Vector myTableFields = new Vector(3);
	myTableFields.addElement ( new TableField ( 
		TableField.DATA_TYPE_STRING, "id_label_6", 12 ) );
	myTableFields.addElement ( new TableField ( 
		TableField.DATA_TYPE_INT, "Basin", 12 ) );
	myTableFields.addElement ( new TableField ( 
		TableField.DATA_TYPE_STRING, "aka", 12 ) );

	// Now define table with one simple call...
	DataTable myTable = new DataTable ( myTableFields );

	// Now define a record to be included in the table...
	TableRecord contents = new TableRecord (3);
	contents.addFieldValue ( "123456" );
	contents.addFieldValue ( new Integer (6));
	contents.addFieldValue ( "RTi station" );

	myTable.addRecord ( contents );

	// Get the 2nd field from the first record (fields and records are
	// zero-index based)...
	system.out.println ( myTable.getFieldValue ( 0, 1 ));

} catch (Exception e ) {
	// process exception
}
</pre>

@see RTi.Util.Table.TableField
@see RTi.Util.Table.TableRecord
*/
public class DataTable {

protected Vector 	_table_fields;		// Vector of TableField
protected Vector	_table_records;		// Vector of TableRecord 
protected int		_num_records = 0;	// Number of records in the
						// table.

protected boolean	_have_data = false;	// Indicates if data records
						// have been read.  This should
						// be reset by derived classes.

protected boolean	_trim_strings = true;	// Indicates whether string data
						// should be trimmed on
						// retrieval.  In general, this
						// should be true because older
						// databases like Dbase pad
						// data with spaces but seldom
						// are spaces actually actual
						// data values.

protected boolean	_add_record_called = false;
						// Indicates whether addRecord()
						// has been called.  If so,
						// assume that the data records
						// are in memory for calls to
						// getNumberOfRecords().
						// Otherwise, just return the
						// _num_records value.

/**
Construct a new table.  Use setTableFields() at a later time to define the
table.
*/
public DataTable ()
{	// Estimate that 100 is a good increment for the data vector...
	initialize ( null, 10, 100 );
}

/**
Construct a new table.  The Vector of TableRecord will increment in size by 100.
@param tableFieldsVector a vector of TableField objects defining table contents.
*/
public DataTable ( Vector tableFieldsVector)
{	// Guess that 100 is a good increment for the data vector...
	initialize ( tableFieldsVector, 10, 100 );
}

/**
Construct a new table.
@param tableFieldsVector a vector of TableField objects defining table contents.
@param Vector_size Initial Vector size for the Vector holding records.  This
can be used to optimize performance.
@param Vector_increment Increment for the Vector holding records.  This
can be used to optimize performance.
*/
public DataTable (	Vector tableFieldsVector, int Vector_size,
			int Vector_increment )
{	initialize ( tableFieldsVector, Vector_size, Vector_increment );
}

/**
Adds a record to the vector of TableRecords maintained in the DataTable.
@param new_record new record to be added.
@exception Exception when the number of fields in new_record is not equal to th
number of fields in the current TableField declaration.
*/
public void addRecord ( TableRecord new_record )
throws Exception
{	int num_table_fields = _table_fields.size();
	int num_new_record_fields = new_record.getNumberOfFields();
	_add_record_called = true;
	if ( num_new_record_fields == num_table_fields ) {
		_table_records.addElement ( new_record );
	}
	else {	throw new Exception ( "Number of fields in the new record (" +
		num_new_record_fields + ") does not match current " +
		"description of the table fields (" +
		num_table_fields + ")." );
	}
}

/**
Add a field to the table and each entry in TableRecord.  The field is added
at the end of the other fields.  The added fields are initialized with blank
strings or zeros, as appropriate.
@param tableField information about field to add.
*/
public void addField ( TableField tableField )
{	_table_fields.addElement ( tableField );

	int num = _table_records.size();
	TableRecord tableRecord;
	for ( int i=0; i<num; i++ ) {
		tableRecord = (TableRecord)_table_records.elementAt(i);

		// add element and set default to 0 or ""
		// these are ordered in the most likely types to optimize
		int data_type = tableField.getDataType();
		if ( data_type == TableField.DATA_TYPE_STRING ) {
			tableRecord.addFieldValue( "");
		}
		else if ( data_type == TableField.DATA_TYPE_INT ) {
			tableRecord.addFieldValue( new Integer ( 0 ));
		}
		else if ( data_type == TableField.DATA_TYPE_DOUBLE ) {
			tableRecord.addFieldValue( new Double ( 0 ));
		}
		else if ( data_type == TableField.DATA_TYPE_SHORT ) {
			tableRecord.addFieldValue( new Short ( "0" ));
		}
		else if ( data_type == TableField.DATA_TYPE_FLOAT ) {
			tableRecord.addFieldValue( new Float ( 0 ));
		}
	}
	tableRecord = null;
}

/**
Deletes a field and all the field's data from the table.
@param fieldNum the number of the field to delete.
*/
public void deleteField(int fieldNum) 
throws Exception {
	if (fieldNum < 0 || fieldNum > (_table_fields.size() - 1)) {
		throw new Exception ("Field number " + fieldNum + " out of "
			+ "bounds.");
	}
	_table_fields.removeElementAt(fieldNum);

	int size = _table_records.size();
	TableRecord record = null;
	for (int i = 0; i < size; i++) {
		record = (TableRecord)_table_records.elementAt(i);
		record.deleteField(fieldNum);
	}
}

/**
Deletes a record from the table.
@param recordNum the number of the record to delete.
*/
public void deleteRecord(int recordNum) 
throws Exception {
	if (recordNum < 0 || recordNum > (_table_records.size() - 1)) {
		throw new Exception ("Record number " + recordNum + " out of "
			+ "bounds.");
	}
	
	_table_records.removeElementAt(recordNum);
}

/**
Dumps a table to Status level 1.
@param delimiter the delimiter to use.
@throws Exception if an error occurs.
*/
public void dumpTable(String delimiter) 
throws Exception {
	String routine = "DataTable.dumpTable";
	int cols = getNumberOfFields();
	int rows = getNumberOfRecords();
	String rowPlural = "s";
	if (rows == 1) {
		rowPlural = "";
	}
	String colPlural = "s";
	if (cols == 1) {
		colPlural = "";
	}
	Message.printStatus(1, "", "Table has " + rows + " row" + rowPlural
		+ " and " + cols + " column" + colPlural + ".");
		
	if (cols == 0) {
		Message.printWarning(1, routine, "Table has 0 columns!  "
			+ "Nothing will be written.");
		return;
	}

	String line = null;

	line = "";
	for (int col = 0; col < (cols - 1); col++) {
		line += getFieldName(col) + delimiter;
	}
	line += getFieldName((cols - 1));
	Message.printStatus(1, "", line);
	
	for (int row = 0; row < rows; row++) {
		line = "";
		for (int col = 0; col < (cols - 1); col++) {
			line += "" + getFieldValue(row, col) + delimiter;
		}
		line += getFieldValue(row, (cols - 1));

		Message.printStatus(1, "", line);
	}
}

/**
Copies a DataTable.
@param originalTable the table to be copied.
@param cloneData if true, the data in the table will be cloned.  If false, both
tables will have pointers to the same data.
@return the new copy of the table.
*/
public static DataTable duplicateDataTable(DataTable originalTable, 
boolean cloneData) {
	String routine = "DataTable.duplicateDataTable";
	
	DataTable newTable = null;
	int numFields = originalTable.getNumberOfFields();

	TableField field = null;
	TableField newField = null;
	Vector tableFields = new Vector();
	for (int i = 0; i < numFields; i++) {
		field = (TableField)originalTable.getTableField(i);
		newField = new TableField(field.getDataType(), 
			new String(field.getName()),
			field.getWidth(), field.getPrecision());
		tableFields.add(newField);
	}
	newTable = new DataTable(tableFields);
	if (!cloneData) {
		return newTable;
	}
	newTable._have_data = true;

	int numRecords = originalTable.getNumberOfRecords();
	int type = -1;
	TableRecord newRecord = null;
	for (int i = 0; i < numRecords; i++) {
		try {
		newRecord = new TableRecord(numFields);
		for (int j = 0; j < numFields; j++) {
			type = newTable.getFieldDataType(j);
			if (type == TableField.DATA_TYPE_INT) {
		        	newRecord.addFieldValue(new Integer(
				    ((Integer)originalTable.getFieldValue(i, j))
					.intValue()));
			}
			else if (type == TableField.DATA_TYPE_SHORT) {
		        	newRecord.addFieldValue(new Short(
			  	      ((Short)originalTable.getFieldValue(i, j))
					.shortValue()));
			}
			else if (type == TableField.DATA_TYPE_DOUBLE) {
		        	newRecord.addFieldValue(new Double(
				     ((Double)originalTable.getFieldValue(i, j))
					.doubleValue()));
			}
			else if (type == TableField.DATA_TYPE_FLOAT) {
		        	newRecord.addFieldValue(new Float(
				      ((Float)originalTable.getFieldValue(i, j))
					.floatValue()));
			}
			else if (type == TableField.DATA_TYPE_STRING) {
		        	newRecord.addFieldValue(new String(
				    (String)originalTable.getFieldValue(i, j)));
			}
			else if (type == TableField.DATA_TYPE_DATE) {
		        	newRecord.addFieldValue(
				       ((Date)originalTable.getFieldValue(i, j))
				       .clone());
			}
		}
		newTable.addRecord(newRecord);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, 
				"Error adding record " + i + " to table.");
			Message.printWarning(2, routine, e);
		}
	}
	return newTable;
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable
{	_table_fields = null;
	_table_records = null;
	super.finalize();
}

/**
Used internally to determine whether a field name is already present in a 
table's fields, so as to avoid duplication.
@param tableFields a Vector of the tableFields created so far for a table.
@param name the name of the field to check.
@return true if the field name already is present in the table fields, false
if not.
*/
private static boolean findPreviousFieldNameOccurances(Vector tableFields,
String name) {
	int size = tableFields.size();
	TableField field = null;
	String fieldName = null;
	for (int i = 0; i < size; i++) {
		field = (TableField)tableFields.elementAt(i);
		fieldName = field.getName();
		if (name.equals(fieldName)) {
			return true;
		}
	}
	return false;
}

/**
Return the field data type, given an index.
@return Data type for specified zero-based index.
@param index field index.
*/
public int getFieldDataType ( int index )
{	return ((TableField)_table_fields.elementAt ( index )).getDataType();
}

/**
Return the field data types for all of the fields.  This is useful because
code that processes all the fields can request the informtion once and then
re-use.
@return Data types for all fields, in an integer array or null if no fields.
*/
public int[] getFieldDataTypes ()
{	int size = getNumberOfFields();
	if ( size == 0 ) {
		return null;
	}
	int types[] = new int[size];
	for ( int i = 0; i < size; i++ ) {
		types[i] = getFieldDataType(i);
	}
	return types;
}

/**
Get C-style format specifier that can be used to format field values for
output.  This format can be used with StringUtil.formatString().  All fields
formats are set to the full width and precision defined for the field.  Strings
are left-justified and numbers are right justified.
@return a String format specifier.
@param index Field index (zero-based).
*/
public String getFieldFormat ( int index )
{	int field_type = getFieldDataType(index);
	if ( field_type == TableField.DATA_TYPE_STRING ) {
		// Output left-justified and padded...
		return "%-" + getFieldWidth(index) + "." +
			getFieldWidth(index) + "s";
	}
	else {	if (	(field_type == TableField.DATA_TYPE_FLOAT) ||
			(field_type == TableField.DATA_TYPE_DOUBLE) ) {
			return "%" + getFieldWidth(index) + "." +
				getFieldPrecision(index) + "f";
		}
		else {	return "%" + getFieldWidth(index) + "d";
		}
	}
}

/**
Get C-style format specifiers that can be used to format field values for
output.  These formats can be used with StringUtil.formatString().
@return a String array with the format specifiers.
*/
public String[] getFieldFormats()
{	int nfields = getNumberOfFields();
	String [] format_spec = new String[nfields];
	for ( int i = 0; i < nfields; i++ ) {
		format_spec[i] = getFieldFormat ( i );
	}
	return format_spec;
}

/**
Return the field index associated with the given field name.
@return Index of table entry associated with the given field name.
@param field_name Field name to look up.
@exception Exception if the field name is not found.
*/
public int getFieldIndex ( String field_name )
throws Exception
{	int num = _table_fields.size();
	for ( int i=0; i<num; i++ ) {
		if (((TableField)_table_fields.elementAt(i)).
			getName().equalsIgnoreCase(field_name))
			return i;
	}

	// if this line is reached, the given field was never found
	throw new Exception( "Unable to find field with name \"" + 
		field_name + "\"" );
}

/**
Return the field name, given an index.
@return Field name for specified zero-based index.
@param index field index.
*/
public String getFieldName ( int index )
{	return ((TableField)_table_fields.elementAt ( index )).getName();
}

/**
Return the field names for all fields.
@return a String array with the field names.
*/
public String[] getFieldNames ()
{	int nfields = getNumberOfFields();
	String [] field_names = new String[nfields];
	for ( int i = 0; i < nfields; i++ ) {
		field_names[i] = getFieldName ( i );
	}
	return field_names;
}

/**
Return the field precision, given an index.
@return Field precision for specified zero-based index.
@param index field index.
*/
public int getFieldPrecision ( int index )
{	return ((TableField)_table_fields.elementAt ( index )).getPrecision();
}

/**
Return the field value for the requested record and field name.
The overloaded method that takes integers should be called for optimal
performance (so the field name lookup is avoided).
@param record_index zero-based index of record
@param field_name Field name of field to read.
@return field value for the specified field name of the specified record index
The returned object must be properly cast.
*/
public Object getFieldValue ( long record_index, String field_name )
throws Exception
{	return getFieldValue ( record_index, getFieldIndex(field_name) );
}

/**
Return the field value for the requested record and field index.  <b>Note that
this method can be overruled to implement on-the-fly data reads.  For example,
the DbaseDataTable class overrules this method to allow data to be read from the
binary Dbase file, as needed, at run-time, rather than reading from memory.  In
this case, the haveData() method can be used to indicate if data should be
taken from memory (using this method) or read from file (using a derived class
method).</b>
@param record_index zero-based index of record
@param field_index zero_based index of desired field
@return field value for the specified index of the specified record index
The returned object must be properly cast.
*/
public Object getFieldValue ( long record_index, int field_index )
throws Exception
{	int num_recs = _table_records.size();
	int num_fields = _table_fields.size();

	if ( num_recs <= record_index ) {
		throw new Exception ( "Requested record index " + record_index +
		" is not available (only " + num_recs + 
		" are available)." );
	}

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index +
		" is not available (only " + num_fields +
		" have been established." );
	}

	TableRecord tableRecord = (TableRecord)_table_records.
		elementAt((int)record_index);
	Object o = tableRecord.getFieldValue(field_index);
	tableRecord = null;
	return o;
}

/**
Return the field width, given an index.
@return Field width for specified zero-based index.
@param index field index.
*/
public int getFieldWidth ( int index )
{	return ((TableField)_table_fields.elementAt ( index )).getWidth();
}

/**
Return the heading for a field index.
@return heading title for specified zero-based index.
@param index field index.
@deprecated Use getFieldName(int).
*/
public String getHeadingForIndex ( int index )
{	return getFieldName ( index );
}

/**
Return the number of fields in the table.
@return number of fields in the table.
*/
public int getNumberOfFields ()
{	return _table_fields.size();
}

/**
Return the number of records in the table.  <b>This value should be set by
code that manipulates the data table.  If the table records Vector has been
manipulated with a call to addRecord(), the size of the Vector will be returned.
Otherwise, the setNumberOfRecords() methods should be called appropriately and
its the value that is set will be returned.  This latter case
will be in effect if tables are being read on-the-fly.</b>
@return number of records in the table.
*/
public int getNumberOfRecords ()
{	if ( _add_record_called ) {
		return _table_records.size();
	}
	else {	return _num_records;
	}
}

/**
Return the TableRecord at a record index.
@param record_index Record index (zero-based).
@return TableRecord at specified record_index
*/
public TableRecord getRecord ( int record_index )
throws Exception
{	if ( !_have_data ) {
		// Most likely a derived class is not handling on the fly
		// reading of data and needs more development.  Return null
		// because the limitation is likely handled elsewhere.
		return null;
	}
	if ( _table_records.size() <= record_index ) {
		throw new Exception ( 
		"Unable to return TableRecord at index [" + record_index +
		"].  Max value allowed is " + (_table_records.size() - 1) +".");
	}
	return ((TableRecord)_table_records.elementAt(record_index));
}

/**
Return the Vector of TableRecords.
@return vector of TableRecord.
*/
public Vector getTableRecords ( )
{	return _table_records;
}

/**
Return the TableField object for the requested column.
@param index Table field index (zero-based).
@return TableField object for the specified zero-based index.
*/
public TableField getTableField ( int index )
{	return ((TableField)_table_fields.elementAt( index ));
}

/**
Get the data type for the field.
@return the data type for the field (see TableField.DATA_TYPE_*).
@param index index of field (zero-based).
@exception If the index is out of range.
*/
public int getTableFieldType ( int index )
throws Exception
{	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.elementAt(index);
	int type = tableField.getDataType ();
	tableField = null;
	return type;
}

/**
Return the unique field values for the requested field index.  This is used,
for example, when displaying unique values on a map display.  The calling code
will need to cast the returned objects appropriately.  The performance of this
operation will degrade if a large number of unique values are present.  This
should not normally be the case if the end-user is intelligent about their
choice of the field that is being analyzed.
@param field_index zero_based index of desired field
@return Simple array (e.g., double[]) of unique data values from the field.
Depending on the field data type, a double[], int[], short[], or String[] will
be returned.
@exception if the field index is not in the allowed range.
*/
/* SAM- Implement this later.
public Object getUniqueFieldValues ( int field_index )
throws Exception
{	int num_recs = _table_records.size();
	int num_fields = _table_fields.size();

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index +
		" is not available (only " + num_fields +
		" are available)." );
	}

	// Use a temporary vector to get the unique values...
	Vector u = new Vector ( 100, 100 );

	// Determine the field type...
	int field_type = getTableFieldType ( field_index );
	//String rtn = "getFieldValue";
	//Message.printStatus ( 10, rtn, "Getting table record " +
	//	record_index + " from " + num_recs + " available records." );
	TableRecord tableRecord = null;
	Object o = null;
	for ( int i = 0; i < num_recs; i++ ) {
		tableRecord = (TableRecord)_table_records.elementAt(i);
		o = tableRecord.getFieldValue(field_index);
		// Now search through the list of known unique values...
		usize = u.size();
		for ( j = 0; j < usize; j++ ) {
		}
	}
	// Now return the values in an array of the appropriate type...
}
*/

/**
Checks to see if the table has a field with the given name.
@param fieldName the name of the field to check for (case-sensitive).
@return true if the table has the field, false otherwise.
*/
public boolean hasField(String fieldName) {
	String[] fieldNames = getFieldNames();
	for (int i = 0; i < fieldNames.length; i++) {
		if (fieldNames[i].equals(fieldName)) {
			return true;
		}
	}
	return false;
}


/**
Indicate whether the table has data.  This will be true if any table records
have been added during a read or write operation.  This method is meant to be
called by derived classes.
*/
public boolean haveData ()
{	return _have_data;
}

/**
Initialize the data.
@param tableFieldsVector Vector of TableField used to define the DataTable.
@param Vector_size Initial Vector size for the Vector holding records.
@param Vector_increment Increment for the Vector holding records.
*/
private void initialize (	Vector tableFieldsVector, int Vector_size,
				int Vector_increment )
{	_table_fields = tableFieldsVector;
	_table_records = new Vector ( 10, 100 );
}

/**
Returns whether any of the table records are dirty or not.
@return whether any of the table records are dirty or not.
*/
public boolean isDirty() {
	TableRecord record = null;
	int recordCount = getNumberOfRecords();

	for (int i = 0; i < recordCount; i++) {
		record = ((TableRecord)_table_records.elementAt(i));
		if (record.isDirty()) {
			return true;
		}
	}
	return false;
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered
part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields vector of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile (	String filename,
						String delimiter,
						Vector tableFields,
						int num_lines_header )
throws Exception {
	return parseDelimitedFile(filename, delimiter, tableFields,
		num_lines_header, false);
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered
part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields vector of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@param trim_spaces if true, then when a column value is read between delimiters,
it will be .trim()'d before being parsed into a number or String. 
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile (	String filename,
						String delimiter,
						Vector tableFields,
						int num_lines_header, 
						boolean trim_spaces)
throws Exception {
	return parseDelimitedFile(filename, delimiter, tableFields,
		num_lines_header, trim_spaces, -1);
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered
part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields vector of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@param trim_spaces if true, then when a column value is read between delimiters,
it will be .trim()'d before being parsed into a number or String. 
@param maxLines the maximum number of lines to read from the file.  If less than
or equal to 0, all lines will be read.
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile (	String filename,
						String delimiter,
						Vector tableFields,
						int num_lines_header, 
						boolean trim_spaces,
						int maxLines)
throws Exception {

	String iline;
	boolean processed_header = false;
	Vector columns;
	int num_fields=0, num_lines_header_read=0;
	int lineCount = 0;
	DataTable table;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	table = new DataTable( tableFields );
	table._have_data = true;
	int field_types[] = table.getFieldDataTypes();
	if ( num_lines_header == 0 ) {
		processed_header = true;
		num_fields = field_types.length;
	}

	String col = null;

	// create an array to use for determining the maximum size of all the
	// fields that are Strings.  This will be used to set the width of
	// the data values for those fields so that the width of the field is
	// equal to the width of the longest string.  This is mostly important
	// for when the table is to be placed within a DataTable_TableModel, 
	// so that the String field data are not truncated.
	int numFields = tableFields.size();
	int[] stringLengths = new int[numFields];
	for (int i = 0; i < numFields; i++) {	
		stringLengths[i] = 0;
	}
	int length = 0;

	while (( iline = in.readLine ()) != null ) {
		// check if read comment or empty line
		if ( iline.startsWith("#") || iline.trim().length()==0) {
			continue;
		}

		columns = StringUtil.breakStringList ( iline,
			delimiter, 
//			StringUtil.DELIM_SKIP_BLANKS |
			StringUtil.DELIM_ALLOW_STRINGS);

		// line is part of header ... 
		if ( !processed_header ) {
			num_fields = columns.size();
			if ( num_fields < tableFields.size() ) {
				throw new IOException ( 
				"Table fields specifications do not " +
				"match data found in file." );
			}
			
			num_lines_header_read++;
			if ( num_lines_header_read == num_lines_header ) {
				processed_header = true;
			}
		}
		else {	// line contains data - store in table as record
			TableRecord contents = new TableRecord(num_fields);
			try {						
			for ( int i=0; i<num_fields; i++ ) {
				col = (String)columns.elementAt(i);
				if (trim_spaces) {
					col = col.trim();
				}
				if (	field_types[i] ==
					TableField.DATA_TYPE_STRING ) {
					contents.addFieldValue(col);
					length = col.length();
					if (length > stringLengths[i]) {
						stringLengths[i] = length;
					}
				}
				else if ( field_types[i] ==
					TableField.DATA_TYPE_DOUBLE ){
					contents.addFieldValue(
						new Double(col));
				}
				else if ( field_types[i] ==
					TableField.DATA_TYPE_INT ) {
					contents.addFieldValue(
						new Integer(col));
				}
				else if ( field_types[i] ==
					TableField.DATA_TYPE_SHORT ) {
					contents.addFieldValue(
						new Short(col));
				}
				else if ( field_types[i] ==
					TableField.DATA_TYPE_FLOAT ) {
					contents.addFieldValue(
						new Float(col));
				}
			}
			table.addRecord ( contents );
			contents = null;
			} catch ( Exception e ) {
				if (IOUtil.testing()) {
					e.printStackTrace();
				}
				Message.printWarning ( 2,
				"DataTable.parseDelimitedFile", e );
			}
		}
		lineCount++;
		if (maxLines > 0 && lineCount >= maxLines) {
			in.close();

			// set the widths of the string fields to the length
			// of the longest strings within those fields
			for (int i = 0; i < num_fields; i++) {
				col = (String)columns.elementAt(i);
				if (field_types[i] 
					== TableField.DATA_TYPE_STRING) {
					table.setFieldWidth(i, 
						stringLengths[i]);
				}
			}
							
			return table;
		}
	}
	in.close();
	
	return table;
}

/**
Reads the header of a comma-delimited file and return Vector of TableField
objects.
@return vector of TableField objects (only field names will be set).
@param filename name of file containing delimited data.
*/
public static Vector parseDelimitedFileHeader ( String filename )
throws Exception
{	return parseDelimitedFileHeader ( filename, "," );
}

/**
Reads the header of a delimited file and return vector of TableField objects.
The field names will be correctly returned.  The data type, however, will be set
to TableField.DATA_TYPE_STRING.  This should be changed if not appropriate.
@return vector of TableField objects (field names will be correctly set but
data type will be string).
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file.
@exception Exception if there is an error reading the file.
*/
public static Vector parseDelimitedFileHeader ( String filename, 
						String delimiter )
throws Exception
{	String iline;
	Vector columns, tableFields = null;
	int num_fields=0;
	TableField newTableField = null;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	while (( iline = in.readLine ()) != null ) {

		// check if read comment or empty line
		if ( iline.startsWith("#") || iline.trim().length()==0) {
			continue;
		}

		columns = StringUtil.breakStringList ( iline,
			delimiter, 0);
//			StringUtil.DELIM_SKIP_BLANKS );

		num_fields = columns.size();
		tableFields = new Vector ( num_fields, 1 );
		for ( int i=0; i<num_fields; i++ ) {
			newTableField = new TableField ( );
			newTableField.setName (
				((String)columns.elementAt(i)).trim());
			newTableField.setDataType(TableField.DATA_TYPE_STRING);
			tableFields.addElement ( newTableField );
		}
		break;
	}
	in.close();
	in = null;
	iline = null;
	columns = null;
	newTableField = null;
	return tableFields;
}

/**
Parses a file and returns the DataTable for the file.  Currently only does
delimited files.  The lines in delimited files do not need to all have the same
number of columns: the number of columns in the returned DataTable will be 
the same as the line in the file with the most delimited columns, all others
will be padded with empty value columns on the right of the table.
@param filename the name of the file from which to read the table data.
@param props a PropList with settings for how the file should be read and
handled.<p>
Properties and their effects:<br>
<ul>
<li><b>FixedFormat</b> - "True" or "False".  Currently ignored.</li>
<li><b>Delimiter</b> - The character (s) that should be used to delimit fields
in the file.  Fields are broken using the following
StringUtil.breakStringList() call (the flag can be modified by
MergeDelimiters):<br>
<blockquote>
	v = StringUtil.breakStringList(line, delimiters, 0);
</blockquote><br>
Defaults to ",".</li>
<li><b>MergeDelimiters</b> - "True" or "False".  If true, then adjoining
delimiter characters are treated as one by using
StringUtil.breakStringList(line,delimiters,StringUtil.DELIM_SKIP_BLANKS.</li>
<li><b>CommentLineIndicator</b> - The characters with which comment lines begin.
Lines starting with this character are skipped (TrimInput is applied after
checking for comments).</li>
<li><b>TrimInput</b> - "True" or "False".  Indicates input strings should
be trimmed before parsing.  Defaults to false.</li>
<li><b>TrimStrings</b> - "True" or "False".  Indicates whether strings should
be trimmed before being placed in the data table (after parsing).  Defaults to
false.</li>
</ul>
@return the DataTable that was created.
@throws Exception if an error occurs
*/
public static DataTable parseFile(String filename, PropList props) 
throws Exception {
	String delim = "";
	String comment = null;
	boolean trimString = false;
	// TODO SAM 2005-11-16 why is FixedFormat included?  Future feature?
	/*String propVal = props.getValue("FixedFormat");
	if (propVal != null) {
		if (propVal.equalsIgnoreCase("false")) {
			fixed = false;
		}
	}
	*/
	
	String propVal = props.getValue("Delimiter");
	if (propVal != null) {		
		delim = propVal;
	}
	else {
		delim = ",";
	}

	propVal = props.getValue("MergeDelimiters");
	int parse_flag = StringUtil.DELIM_ALLOW_STRINGS;
	if (propVal != null) {		
		parse_flag |= StringUtil.DELIM_SKIP_BLANKS;
	}

	propVal = props.getValue("CommentLineIndicator");
	if (propVal != null) {
		comment = propVal;
	}
	else {
		comment = null;
	}

	propVal = props.getValue("TrimInput");
	boolean trimInput = false;	// Default
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		trimInput = true;
	}

	propVal = props.getValue("TrimStrings");
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		trimString = true;
	}

	Vector file = new Vector();
	Vector v = null;
	int maxColumns = 0;
	int size = 0;

	BufferedReader in = new BufferedReader(new FileReader(filename));
	String line = in.readLine();

	// REVISIT (JTS - 2006-06-05)
	// Found a bug in DataTable.  If you attempt to call
	// parseFile() on a file of size 0 (no lines, no characters)
	// it will throw an exception.  This should be checked out in
	// the future.
	
	// skip any comments at the top of the file (most likely place for
	// them to appear)
	while (comment != null && line.startsWith(comment)) {
		line = in.readLine();
	}

	// "line" now contains the latest non-comment line so evaluate whether
	// the line contains the column names.
		
	// If a quote is detected, then this line is assumed to contain
	// the name of the fields.  
	
	int numFields = -1;
	Vector tableFields = null;
	TableField tableField = null;
	if (line.startsWith("\"")) {
		Vector columns = null;
		if ( trimInput ) {
			columns = StringUtil.breakStringList(
				line.trim(), delim, parse_flag );
		}
		else {	columns = StringUtil.breakStringList(line, delim, 
					parse_flag );
		}

		numFields = columns.size();
		tableFields = new Vector();
		String temp = null;
		for (int i = 0; i < numFields; i++) {
			temp = ((String)columns.elementAt(i)).trim();
			while (findPreviousFieldNameOccurances(tableFields,
			    temp)) {
				temp = temp + "_2";
			}
			tableField = new TableField();
			tableField.setName(temp);
			tableField.setDataType(TableField.DATA_TYPE_STRING);
			tableFields.addElement(tableField);
		}
		// Read another line, as expected below...
		line = in.readLine();
	}

	// Now evaluate the data lines...
	
	while (line != null) {
		if (comment != null && line.startsWith(comment)) {
			// skip
		}
		else {	if ( trimInput ) {
				v = StringUtil.breakStringList(line.trim(),
					delim, parse_flag );
			}
			else {	v = StringUtil.breakStringList(line, delim, 
					parse_flag );
			}
			size = v.size();
			if (size > maxColumns) {
				maxColumns = size;
			}
			file.add(v);
		}
		line = in.readLine();
	}
	in.close();

	if (tableFields == null) {
		tableFields = new Vector();
		for (int i = 0; i < maxColumns; i++) {
			// default field definition builds String fields
			tableFields.add(new TableField());
		}
	}
	else {
		// add enough fields to account for the maximum number 
		// of columns in the table.  
		String temp = null;
		for (int i = numFields; i < maxColumns; i++) {
			tableField = new TableField();
			temp = "Field_" + (i + 1);
			while (findPreviousFieldNameOccurances(tableFields,
			    temp)) {
				temp = temp + "_2";
			}
			tableField.setName(temp);
			tableField.setDataType(TableField.DATA_TYPE_STRING);
			tableFields.addElement(tableField);
		}
	}

	DataTable table = new DataTable(tableFields);
	table._have_data = true;
	TableRecord contents = null;

	size = file.size();
	int cols = 0;
	int j = 0;
	for (int i = 0; i < size; i++) {
		v = (Vector)file.elementAt(i);

		contents = new TableRecord(maxColumns);
		cols = v.size();
		for (j = 0; j < cols; j++) {
			if (trimString) {
				contents.addFieldValue(
					((String)v.elementAt(j)).trim());
			}
			else {
				contents.addFieldValue((String)v.elementAt(j));
			}
		}
		
		if (j < maxColumns) {
			for (; j < maxColumns; j++) {
				contents.addFieldValue("");
			}
		}
		
		table.addRecord(contents);
	}

	return table;		
}

/**
Sets the value of a specific field. 
@param row the row in which to set the value.
@param col the column in which to set the value.
@param value the value to set.
*/
public void setFieldValue(int row, int col, Object value) 
throws Exception {
	TableRecord record = (TableRecord)_table_records.elementAt(row);
	record.setFieldValue(col, value);
}

/**
Sets the width of the field.
@param col the column for which to set the width.
@param width the width to set.
*/
public void setFieldWidth(int col, int width) 
throws Exception {
	TableField field = (TableField)_table_fields.elementAt(col);
	field.setWidth(width);
}

/**
Set the number of records in the table.  This method should typically only be
called when data are read on-the-fly (and are not stored in memory in the
table records).
@param num_records Number of records in the table.
*/
public void setNumberOfRecords ( int num_records )
{	_num_records = num_records;
}

/**
Set field data type and header for the specified zero-based index.
@param index index of field to set
@param data_type data type; use TableField.DATA_TYPE_*
@param name name of the field.
*/
public void setTableField ( int index, int data_type, String name )
throws Exception
{	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.elementAt(index);
	tableField.setDataType ( data_type );
	tableField.setName ( name );
	tableField = null;
}

/**
Set the table fields to define the table.
@param tableFieldsVector a vector of TableField objects defining table contents.
*/
public void setTableFields ( Vector tableFieldsVector )
{	_table_fields = tableFieldsVector;
}

/**
Set table header for the specified zero-based index.
@param index index of field to set.
@param header header of the field.
@exception If the index is out of range.
@deprecated Use setTableFieldName().
*/
public void setTableFieldHeader ( int index, String header )
throws Exception 
{	setTableFieldName ( index, header );	
}

/**
Set table field name.
@param index index of field to set (zero-based).
@param name Field name.
@exception If the index is out of range.
*/
public void setTableFieldName ( int index, String name )
throws Exception 
{	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.elementAt(index);
	tableField.setName ( name );
	tableField = null;
}

/**
Set field data type for the specified zero-based index.
@param index index of field to set
@param data_type data type; use TableField.DATA_TYPE_*
@exception If the index is out of range.
*/
public void setTableFieldType ( int index, int data_type )
throws Exception
{	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = (TableField)_table_fields.elementAt(index);
	tableField.setDataType ( data_type );
	tableField = null;
}

/**
Set whether strings should be trimmed at read.
@param trim_strings If true, strings will be trimmed at read.
@return Boolean value indicating whether strings should be trimmed, after reset.
*/
public boolean trimStrings ( boolean trim_strings )
{	_trim_strings = trim_strings;
	return _trim_strings;
}

/**
Indicate whether strings should be trimmed at read.
@return Boolean value indicating whether strings should be trimmed.
*/
public boolean trimStrings ( )
{	return _trim_strings;
}

// REVISIT SAM 2006-06-21
// Need to check for delimiter in header and make this code consistent with
// the RTi.Util.GUI.JWorksheet file saving code, or refactor to use the same
// code.
/**
Writes a table to a delimited file.  If the data items contain the delimiter,
they will be written surrounded by double quotes.
@param filename the file to write to.
@param delimiter the delimiter to use.
@param writeHeader If true, the field names will be read from the fields 
and written as a one-line header of field names.  Currently the headers are
not checked for delimiter and are not double-quoted.
@param comments a Vector of Strings to put at the top of the file as comments.
@throws Exception if an error occurs.
*/
public void writeDelimitedFile(String filename, String delimiter,
boolean writeHeader, Vector comments) 
throws Exception {
	String routine = "DataTable.writeDelimitedFile";
	
	if (filename == null) {
		Message.printWarning(1, routine, "Cannot write to file '"
			+ filename + "'");
		throw new Exception("Cannot write to file '" + filename + "'");
	}
		
	PrintWriter out = new PrintWriter(
		new BufferedWriter(new FileWriter(filename)));

	// if any comments have been passed in, print them at the top of
	// the file
	if (comments != null && comments.size() > 0) {
		int size = comments.size();
		for (int i = 0; i < size; i++) {
			out.println("# " + (comments.elementAt(i)).toString());
		}
	}

	int cols = getNumberOfFields();
	if (cols == 0) {
		Message.printWarning(1, routine, "Table has 0 columns!  "
			+ "Nothing will be written.");
		return;
	}

	String line = null;

	if (writeHeader) {
		line = "";
		for (int col = 0; col < (cols - 1); col++) {
			line += getFieldName(col) + delimiter;
		}
		line += getFieldName((cols - 1));
		out.println(line);
	}
	
	int rows = getNumberOfRecords();
	String cell;
	for (int row = 0; row < rows; row++) {
		line = "";
		for (int col = 0; col < (cols - 1); col++) {
			cell = "" + getFieldValue(row,col);
			// If the field contains the delimiter, surround with
			// double quotes...
			if ( cell.indexOf(delimiter) > -1 ) {
				cell = "\"" + cell + "\"";
			}
			line += "" + cell + delimiter;
		}
		line += getFieldValue(row, (cols - 1));

		out.println(line);
	}

	out.flush();
	out.close();
}

} // End of DataTable class
