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

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;

import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

// TODO SAM 2010-12-16 Evaluate using a different package for in-memory tables, such as
// from H2 or other embedded database.
/**
This class contains records of data as a table, using a list of TableRecord
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
try {
	/// First, create define the table by assembling a list of TableField objects...
	List<TableField> myTableFields = new ArrayList<TableField>(3);
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_STRING, "id_label_6", 12 ) );
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_INT, "Basin", 12 ) );
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_STRING, "aka", 12 ) );

	// Now define table with one simple call...
	DataTable myTable = new DataTable ( myTableFields );

	// Now define a record to be included in the table...
	TableRecord contents = new TableRecord (3);
	contents.addFieldValue ( "123456" );
	contents.addFieldValue ( new Integer (6));
	contents.addFieldValue ( "Station ID" );

	myTable.addRecord ( contents );

	// Get the 2nd field from the first record (fields and records are zero-index based)...
	system.out.println ( myTable.getFieldValue ( 0, 1 ));

} catch (Exception e ) {
	// process exception
}
</pre>

@see RTi.Util.Table.TableField
@see RTi.Util.Table.TableRecord
*/
public class DataTable
{
/**
The identifier for the table.
*/
private String __table_id = "";

/**
List of TableField that define the table columns.
*/
protected List<TableField> _table_fields;

/**
List of TableRecord, that contains the table data.
*/
protected List<TableRecord> _table_records;

/**
List of comments for the table.  For example, an analysis that creates a table of results may
need explanatory comments corresponding to column headings.  The comments can be output when the
table is written to a file.
*/
private List<String> __comments = new ArrayList<String>();

/**
Number of records in the table (kept for case where records are not in memory).
*/
protected int _num_records = 0;

/**
Indicates if data records have been read into memory.  This can be reset by derived classes that
instead keep open a binary database file (e.g., dBase) and override the read/write methods.
*/
protected boolean _haveDataInMemory = true;

/**
Indicates whether string data should be trimmed on retrieval.  In general, this
should be true because older databases like Dbase pad data with spaces but seldom
are spaces actually actual data values.
*/
protected boolean _trim_strings = true;

/**
Indicates whether addRecord() has been called.  If so, assume that the data records
are in memory for calls to getNumberOfRecords(). Otherwise, just return the _num_records value.
*/
protected boolean _add_record_called = false;

/**
Construct a new table.  Use setTableFields() at a later time to define the table.
*/
public DataTable ()
{	// Estimate that 100 is a good increment for the data list...
	initialize ( new ArrayList<TableField>(), 10, 100 );
}

/**
Construct a new table.  The list of TableRecord will increment in size by 100.
@param tableFieldsList a list of TableField objects defining table contents.
*/
public DataTable ( List<TableField> tableFieldsList )
{	// Guess that 100 is a good increment for the data list...
	initialize ( tableFieldsList, 10, 100 );
}

/**
Construct a new table.
@param tableFieldsList a list of TableField objects defining table contents.
@param listSize Initial list size for the list holding records.  This
can be used to optimize performance.
@param listIncrement Increment for the list holding records.  This
can be used to optimize performance.
*/
public DataTable ( List<TableField> tableFieldsList, int listSize, int listIncrement )
{	initialize ( tableFieldsList, listSize, listIncrement );
}

/**
Add a String to the comments associated with the time series (e.g., station remarks).
@param comment Comment string to add.
*/
public void addToComments( String comment )
{   if ( comment != null ) {
        __comments.add ( comment );
    }
}

/**
Add a list of String to the comments associated with the time series (e.g., station remarks).
@param comments Comments strings to add.
*/
public void addToComments( List<String> comments )
{   if ( comments == null ) {
        return;
    }
    for ( String comment : comments ) {
        if ( comment != null ) {
            __comments.add ( comment );
        }
    }
}

/**
Adds a record to end of the list of TableRecords maintained in the DataTable.
Use insertRecord() to insert within the existing records.
@param newRecord new record to be added.
@exception Exception when the number of fields in new_record is not equal to the
number of fields in the current TableField declaration.
@return the new record (allows command chaining)
*/
public TableRecord addRecord ( TableRecord newRecord )
throws Exception
{	int num_table_fields = _table_fields.size();
	int num_new_record_fields = newRecord.getNumberOfFields();
	_add_record_called = true;
	if ( num_new_record_fields == num_table_fields ) {
		_table_records.add ( newRecord );
		return newRecord;
	}
	else {
        throw new Exception ( "Number of fields in the new record (" +
		num_new_record_fields + ") does not match current description of the table fields (" +
		num_table_fields + ")." );
	}
}

/**
Add a field to the right-most end of table and each entry in the existing TableRecords.
The added fields are initialized with blank strings or NaN, as appropriate.
@param tableField information about field to add.
@param initValue the initial value to set for all the existing rows in the table (can be null).
@return the field index (0+).
*/
public int addField ( TableField tableField, Object initValue )
{
    return addField ( -1, tableField, initValue );
}

/**
Add a field to the table and each entry in TableRecord.  The field is added at the specified insert position.
The added fields are initialized with blank strings or NaN, as appropriate.
@param insertPos the column (0+) at which to add the column (-1 or >= the number of existing columns to insert at the end).
@param tableField information about field to add.
@param initValue the initial value to set for all the existing rows in the table (can be null).
@return the field index (0+).
*/
public int addField ( int insertPos, TableField tableField, Object initValue )
{	boolean addAtEnd = false;
    if ( (insertPos < 0) || (insertPos >= _table_fields.size()) ) {
        // Add at the end
        _table_fields.add ( tableField );
        addAtEnd = true;
    }
    else {
        // Insert at the specified column location
        _table_fields.add(insertPos,tableField);
    }
    // Add value to each record in the table to be consistent with the field data
	int num = _table_records.size();
	TableRecord tableRecord;
	for ( int i=0; i<num; i++ ) {
		tableRecord = _table_records.get(i);

		// Add element and set default to 0 or ""
		// These are ordered in the most likely types to optimize
		// TODO SAM 2014-05-04 Why are these broken out separately?
		int dataType = tableField.getDataType();
		if ( dataType == TableField.DATA_TYPE_STRING ) {
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
		    else {
		        tableRecord.addFieldValue( insertPos, initValue );
		    }
		}
		else if ( dataType == TableField.DATA_TYPE_INT ) {
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_DOUBLE ) {
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_SHORT ) {
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_FLOAT ) {
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
        else if ( dataType == TableField.DATA_TYPE_LONG ) {
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
        else if ( dataType == TableField.DATA_TYPE_DATE ) {
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
        else if ( dataType == TableField.DATA_TYPE_DATETIME ) {
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
	}
	if ( addAtEnd ) {
	    return getNumberOfFields() - 1; // Zero offset
	}
	else {
	    return insertPos;
	}
}

/**
Append one table to another.
@param table original table
@param newTableID identifier for new table
@param reqIncludeColumns requested columns to include or null to include all
@param distinctColumns requested columns to check for distinct combinations (currently only one column
is allowed), will override reqIncludeColumns, specify null to not check for distinct values
@param columnMap map to rename original columns to new name
@param columnFilters map for columns that will apply a filter
@return the number of rows appended
*/
public int appendTable ( DataTable table, DataTable appendTable, String [] reqIncludeColumns,
    Hashtable<String,String> columnMap, Hashtable<String,String> columnFilters )
{   String routine = getClass().getName() + ".appendTable";
    // List of columns that will be appended
    String [] columnNamesToAppend = null;
    String [] columnNames = table.getFieldNames();
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Append only the requested names
        columnNamesToAppend = reqIncludeColumns;
    }
    else {
        // Append all
        columnNamesToAppend = table.getFieldNames();
    }
    // Column numbers in the append table to match the original table.  Any values set to -1 will result in null.
    int [] columnNumbersInAppendTable = new int[table.getNumberOfFields()];
    String [] appendTableColumnNamesOriginal = appendTable.getFieldNames();
    String [] appendTableColumnNames = appendTable.getFieldNames();
    // Replace the append table names using the column map
    Object o;
    for ( int icol = 0; icol < appendTableColumnNames.length; icol++ ) {
        if ( columnMap != null ) {
            o = columnMap.get(appendTableColumnNames[icol]);
            if ( o != null ) {
                // Reset the append column name with the new name, which should match a column name in the first table
                appendTableColumnNames[icol] = (String)o;
            }
        }
    }
    // Loop through the columns in the original table and match the column numbers in the append table
    boolean appendColumnFound = false;
    for ( int icol = 0; icol < columnNumbersInAppendTable.length; icol++ ) {
        columnNumbersInAppendTable[icol] = -1; // No match between first and append table
        // Check each of the column names in the original table to match whether appending from the append table
        // The append table column names will have been mapped to the first table above
        for ( int i = 0; i < appendTableColumnNames.length; i++ ) {
            // First check to see if the column name should be appended
            appendColumnFound = false;
            for ( int j = 0; j < columnNamesToAppend.length; j++ ) {
                if ( columnNamesToAppend[j].equalsIgnoreCase(appendTableColumnNamesOriginal[i]) ) {
                    appendColumnFound = true;
                    break;
                }
            }
            if ( !appendColumnFound ) {
                // Skip the table column - don't append
                continue;
            }
            if ( columnNames[icol].equalsIgnoreCase(appendTableColumnNames[i]) ) {
                columnNumbersInAppendTable[icol] = i;
                break;
            }
        }
    }
    int [] tableColumnTypes = table.getFieldDataTypes(); // Original table column types
    int [] appendTableColumnTypes = appendTable.getFieldDataTypes(); // Append column types, lined up with original table
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    // Get filter columns and glob-style regular expressions
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobs = new String[columnFilters.size()];
    Enumeration keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = (String)keys.nextElement();
            columnNumbersToFilter[ikey] = appendTable.getFieldIndex(key);
            columnFilterGlobs[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation
            columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "Filter column \"" + key + "\" not found in table \"" + appendTable.getTableID() + "\".");
        }
    }
    // Loop through all the data records and append records to the table
    int icol;
    int irowAppended = 0;
    boolean somethingAppended = false;
    boolean filterMatches;
    String s;
    TableRecord rec;
    for ( int irow = 0; irow < appendTable.getNumberOfRecords(); irow++ ) {
        somethingAppended = false;
        filterMatches = true;
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing append
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    filterMatches = false;
                    break;
                }
                try {
                    o = appendTable.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        filterMatches = false;
                        break; // Don't include nulls when checking values
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobs[icol]) ) {
                        // A filter did not match so don't copy the record
                        filterMatches = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                    errorMessage.append("Error getting append table data [" + irow + "][" +
                        columnNumbersToFilter[icol] + "].");
                    Message.printWarning(3, routine, "Error getting append table data for [" + irow + "][" +
                        columnNumbersToFilter[icol] + "] (" + e + ")." );
                }
            }
            if ( !filterMatches ) {
                // Skip the record.
                continue;
            }
        }
        // Loop through columns in the original table and set values from the append table
        // Create a record and add...
        rec = new TableRecord();
        for ( icol = 0; icol < columnNumbersInAppendTable.length; icol++ ) {
            try {
                if ( columnNumbersInAppendTable[icol] < 0 ) {
                    // Column in first table was not matched in the append table so set to null
                    rec.addFieldValue(null);
                }
                else {
                    // Set the value in the original table, if the type matches
                    if ( tableColumnTypes[icol] == appendTableColumnTypes[columnNumbersInAppendTable[icol]] ) {
                        rec.addFieldValue(appendTable.getFieldValue(irow, columnNumbersInAppendTable[icol]));
                    }
                    else {
                        rec.addFieldValue(null);
                    }
                }
                somethingAppended = true;
            }
            catch ( Exception e ) {
                // Should not happen
                errorMessage.append("Error appending [" + irow + "][" + columnNumbersInAppendTable[icol] + "].");
                Message.printWarning(3, routine, "Error setting appending [" + irow + "][" +
                    columnNumbersInAppendTable[icol] + "] (" + e + ")." );
                ++errorCount;
            }
        }
        if ( somethingAppended ) {
            // Set the record in the original table
            try {
                table.addRecord(rec);
                ++irowAppended;
            }
            catch ( Exception e ) {
                errorMessage.append("Error appending row [" + irow + "].");
            }
        }
    }
    if ( errorCount > 0 ) {
        throw new RuntimeException ( "There were + " + errorCount + " errors appending data to the table: " +
            appendTable.getTableID() );
    }
    return irowAppended;
}

/**
Create a copy of the table.
@param table original table
@param newTableID identifier for new table
@param reqIncludeColumns requested columns to include or null to include all, must specify the distinct column if only
the distinct column is to be copied (this is a change from behavior prior to TSTool 10.26.00 where distinctColumns would
override the reqIncludeColumns and default of all columns)
@param distinctColumns requested columns to check for distinct combinations, multiple columns are allowed,
specify null to not check for distinct values
@param columnMap map to rename original columns to new name
@param columnFilters map for columns that will apply a filter to match column values to include
@param columnExcludeFilters dictionary for columns tht will apply a filter to match column values to exclude
@return copy of original table
*/
public DataTable createCopy ( DataTable table, String newTableID, String [] reqIncludeColumns,
    String [] distinctColumns, Hashtable columnMap, Hashtable columnFilters, StringDictionary columnExcludeFilters )
{   String routine = getClass().getName() + ".createCopy";
    // List of columns that will be copied
    String [] columnNamesToCopy = null;
    // TODO SAM 2013-11-25 Remove code if the functionality works
    //if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
    //    // Distinct overrides requested column names
    //    reqIncludeColumns = distinctColumns;
    //}
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Copy only the requested names
        columnNamesToCopy = reqIncludeColumns;
    }
    else {
        // Copy all
        columnNamesToCopy = table.getFieldNames();
    }
    /* TODO SAM 2013-11-26 Remove this once tested - distinct columns are NOT required to be in output
    if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
        // Add the distinct columns to the requested columns if not already included
        boolean [] found = new boolean[distinctColumns.length];
        int foundCount = 0;
        for ( int id = 0; id < distinctColumns.length; id++ ) {
            found[id] = false;
            for ( int ir = 0; ir < reqIncludeColumns.length; ir++ ) {
                if ( reqIncludeColumns[ir].equalsIgnoreCase(distinctColumns[id]) ) {
                    ++foundCount;
                    found[id] = true;
                    break;
                }
            }
        }
        if ( foundCount != distinctColumns.length ) { // At least one of the distinct columns was not found
            String [] tmp = new String[reqIncludeColumns.length + (distinctColumns.length - foundCount)];
            System.arraycopy(reqIncludeColumns, 0, tmp, 0, reqIncludeColumns.length);
            int addCount = 0;
            for ( int id = 0; id < distinctColumns.length; id++ ) {
                if ( !found[id] ) {
                    tmp[tmp.length + addCount] = distinctColumns[id];
                    ++addCount; // Do after assignment above
                }
            }
            reqIncludeColumns = tmp;
        }
    }
    */
    // Figure out which columns numbers should be copied.  Initialize an array with -1 and then set to
    // actual table columns if matching
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    int [] columnNumbersToCopy = new int[columnNamesToCopy.length];
    for ( int icol = 0; icol < columnNamesToCopy.length; icol++ ) {
        try {
            columnNumbersToCopy[icol] = table.getFieldIndex(columnNamesToCopy[icol]);
        }
        catch ( Exception e ) {
            columnNumbersToCopy[icol] = -1; // Requested column not matched
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "Requested column \"" + columnNamesToCopy[icol] + "\" not found in existing table.");
        }
    }
    // Get (include) filter columns and glob-style regular expressions
    if ( columnFilters == null ) {
        columnFilters = new Hashtable<String,String>();
    }
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobs = new String[columnFilters.size()];
    Enumeration keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = (String)keys.nextElement();
            columnNumbersToFilter[ikey] = table.getFieldIndex(key);
            columnFilterGlobs[ikey] = (String)columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation
            columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "ColumnFilters \"" + key + "\" not found in existing table.");
        }
    }
    // Get exclude filter columns and glob-style regular expressions
    int [] columnExcludeFiltersNumbers = new int[0];
    String [] columnExcludeFiltersGlobs = null;
    if ( columnExcludeFilters != null ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        columnExcludeFiltersNumbers = new int[map.size()];
        columnExcludeFiltersGlobs = new String[map.size()];
        ikey = -1;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            ++ikey;
            columnExcludeFiltersNumbers[ikey] = -1;
            try {
                key = entry.getKey();
                columnExcludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                columnExcludeFiltersGlobs[ikey] = map.get(key);
                // Turn default globbing notation into internal Java regex notation
                columnExcludeFiltersGlobs[ikey] = columnExcludeFiltersGlobs[ikey].replace("*", ".*").toUpperCase();
            }
            catch ( Exception e ) {
                ++errorCount;
                if ( errorMessage.length() > 0 ) {
                    errorMessage.append(" ");
                }
                errorMessage.append ( "ColumnExcludeFilters column \"" + key + "\" not found in existing table.");
            }
        }
    }
    int [] distinctColumnNumbers = null;
    if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
        distinctColumnNumbers = new int[distinctColumns.length];
        for ( int id = 0; id < distinctColumns.length; id++ ) {
            distinctColumnNumbers[id] = -1;
            try {
                distinctColumnNumbers[id] = table.getFieldIndex(distinctColumns[id]);
            }
            catch ( Exception e ) {
                distinctColumnNumbers[id] = -1; // Distinct column not matched
                ++errorCount;
                if ( errorMessage.length() > 0 ) {
                    errorMessage.append(" ");
                }
                errorMessage.append ( "Distinct column \"" + distinctColumns[id] + "\" not found in existing table.");
            }
        }
    }
    // Create a new data table with the requested column names
    DataTable newTable = new DataTable();
    newTable.setTableID ( newTableID );
    // Get the column information from the original table
    Object newColumnNameO = null; // Used to map column names
    TableField newTableField; // New table field
    List<Object []> distinctList = new ArrayList<Object []>(); // Unique combinations of requested distinct column values
    // Create requested columns in the output table
    for ( int icol = 0; icol < columnNumbersToCopy.length; icol++ ) {
        if ( columnNumbersToCopy[icol] == -1 ) {
            // Did not find the column in the table so add a String column for null values
            newTableField = new TableField(TableField.DATA_TYPE_STRING, columnNamesToCopy[icol], -1, -1);
        }
        else {
            // Copy the data from the original table
            // First make a copy of the existing table field
            newTableField = new TableField(table.getTableField(columnNumbersToCopy[icol]));
        }
        if ( columnMap != null ) {
            newColumnNameO = columnMap.get(newTableField.getName());
            if ( newColumnNameO != null ) {
                // Reset the column name with the new name
                newTableField.setName((String)newColumnNameO);
            }
        }
        newTable.addField(newTableField, null );
    }
    // Now loop through all the data records and copy to the output table
    int icol;
    int irowCopied = 0;
    boolean somethingCopied = false;
    boolean filterMatches, distinctMatches;
    Object o = null;
    Object [] oDistinctCheck = null;
    if ( (distinctColumnNumbers != null) && (distinctColumnNumbers.length > 0) ) {
        oDistinctCheck = new Object[distinctColumnNumbers.length];
    }
    String s;
    int distinctMatchesCount = 0; // The number of distinct column value that match the current row
    for ( int irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
        somethingCopied = false;
        filterMatches = true;
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing copy
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    filterMatches = false;
                    break;
                }
                try {
                    o = table.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        filterMatches = false;
                        break; // Don't include nulls when checking values
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobs[icol]) ) {
                        // A filter did not match so don't copy the record
                        filterMatches = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                    errorMessage.append("Error getting table data for filter check [" + irow + "][" +
                        columnNumbersToFilter[icol] + "].");
                    Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                        columnNumbersToFilter[icol] + "] (" + e + ")." );
                    ++errorCount;
                }
            }
            if ( !filterMatches ) {
                // Skip the record.
                continue;
            }
        }
        if ( columnExcludeFiltersNumbers.length > 0 ) {
            int matchesCount = 0;
            // Filters can be done on any columns so loop through to see if row matches before doing copy
            for ( icol = 0; icol < columnExcludeFiltersNumbers.length; icol++ ) {
                if ( columnExcludeFiltersNumbers[icol] < 0 ) {
                    // Can't do filter so don't try
                    break;
                }
                try {
                    o = table.getFieldValue(irow, columnExcludeFiltersNumbers[icol]);
                    if ( o == null ) {
                        break; // Don't include nulls when checking values
                    }
                    s = ("" + o).toUpperCase();
                    if ( s.matches(columnExcludeFiltersGlobs[icol]) ) {
                        // A filter matched so don't copy the record
                        ++matchesCount;
                    }
                }
                catch ( Exception e ) {
                    errorMessage.append("Error getting table data for filter check [" + irow + "][" +
                        columnExcludeFiltersNumbers[icol] + "].");
                    Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                        columnExcludeFiltersNumbers[icol] + "] (" + e + ")." );
                    ++errorCount;
                }
            }
            if ( matchesCount == columnExcludeFiltersNumbers.length ) {
                // Skip the record since all filters were matched
                continue;
            }
        }
        if ( (distinctColumnNumbers != null) && (distinctColumnNumbers.length > 0) ) {
            // Distinct columns can be done on any columns so loop through to see if row matches before doing copy
            // First retrieve the objects and store in an array because a distinct combinations of 1+ values is checked
            distinctMatches = false;
            for ( icol = 0; icol < distinctColumnNumbers.length; icol++ ) {
                if ( distinctColumnNumbers[icol] < 0 ) {
                    break;
                }
                try {
                    // This array is reused but will be copied below if needed to save
                    oDistinctCheck[icol] = table.getFieldValue(irow, distinctColumnNumbers[icol]);
                }
                catch ( Exception e ) {
                    errorMessage.append("Error getting table data checking distinct for [" + irow + "][" +
                        distinctColumnNumbers[icol] + "].");
                    Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                        distinctColumnNumbers[icol] + "] (" + e + ")." );
                    ++errorCount;
                }
            }
            // Now actually check the values
            for ( Object [] odArray : distinctList ) {
                distinctMatchesCount = 0;
                for ( icol = 0; icol < distinctColumnNumbers.length; icol++ ) {
                    if ( (oDistinctCheck[icol] == null) ||
                        ((oDistinctCheck[icol] instanceof String) && ((String)oDistinctCheck[icol]).trim().length() == 0) ) {
                        // TODO SAM 2013-11-25 Don't include nulls and blank strings in distinct values
                        // Might need to change this in the future if those values have relevance
                        continue;
                    }
                    if ( odArray[icol].equals(oDistinctCheck[icol]) ) {
                        ++distinctMatchesCount;
                    }
                }
                if ( distinctMatchesCount == distinctColumnNumbers.length ) {
                    // The columns of interest matched a distinct combination so skip adding the record.
                    distinctMatches = true;
                    break;
                }
            }
            if ( distinctMatches ) {
                // The columns of interest matched a distinct combination so skip adding the record.
                continue;
            }
            else {
                // Create a copy of the temporary object to save and use below.
                Object [] oDistinctCheckCopy = new Object[distinctColumnNumbers.length];
                System.arraycopy(oDistinctCheck, 0, oDistinctCheckCopy, 0, distinctColumnNumbers.length);
                distinctList.add(oDistinctCheckCopy); // Have another combination of distinct values to check for other table rows
                // The row will be added below
            }
        }
        // If here then the row can be added.
        for ( icol = 0; icol < columnNumbersToCopy.length; icol++ ) {
            try {
                if ( columnNumbersToCopy[icol] < 0 ) {
                    // Value in new table is null
                    newTable.setFieldValue(irowCopied, icol, null, true );
                }
                else {
                    // Value in new table is copied from original
                    // TODO SAM 2013-08-06 Need to evaluate - following is OK for immutable objects but what about DateTime, etc?
                    newTable.setFieldValue(irowCopied, icol, table.getFieldValue(irow, columnNumbersToCopy[icol]), true );
                }
                somethingCopied = true;
            }
            catch ( Exception e ) {
                // Should not happen
                errorMessage.append("Error setting new table data copying [" + irow + "][" +
                    columnNumbersToCopy[icol] + "].");
                Message.printWarning(3, routine, "Error setting new table data for [" + irow + "][" +
                    columnNumbersToCopy[icol] + "] (" + e + ")." );
                ++errorCount;
            }
        }
        if ( somethingCopied ) {
            ++irowCopied;
        }
    }
    if ( errorCount > 0 ) {
        throw new RuntimeException ( "There were + " + errorCount + " errors transferring data to new table: " +
            errorMessage );
    }
    return newTable;
}

/**
Deletes a field and all the field's data from the table.
@param fieldNum the number of the field to delete.
*/
public void deleteField(int fieldNum) 
throws Exception
{
	if (fieldNum < 0 || fieldNum > (_table_fields.size() - 1)) {
		throw new Exception ("Field number " + fieldNum + " out of bounds.");
	}
	_table_fields.remove(fieldNum);

	int size = _table_records.size();
	TableRecord record = null;
	for (int i = 0; i < size; i++) {
		record = _table_records.get(i);
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
		throw new Exception ("Record number " + recordNum + " out of bounds.");
	}
	
	_table_records.remove(recordNum);
}

/**
Dumps a table to Status level 1.
@param delimiter the delimiter to use.
@throws Exception if an error occurs.
*/
public void dumpTable(String delimiter) 
throws Exception
{
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
	Message.printStatus(1, "", "Table has " + rows + " row" + rowPlural + " and " + cols + " column" + colPlural + ".");
		
	if (cols == 0) {
		Message.printWarning(2, routine, "Table has 0 columns!  Nothing will be written.");
		return;
	}

	String line = "";
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

		Message.printStatus(2, "", line);
	}
}

/**
Copies a DataTable.
@param originalTable the table to be copied.
@param cloneData if true, the data in the table will be cloned.  If false, both
tables will have pointers to the same data.
@return the new copy of the table.
*/
public static DataTable duplicateDataTable(DataTable originalTable, boolean cloneData)
{
	String routine = "DataTable.duplicateDataTable";
	
	DataTable newTable = null;
	int numFields = originalTable.getNumberOfFields();

	TableField field = null;
	TableField newField = null;
	List<TableField> tableFields = new ArrayList<TableField>();
	for (int i = 0; i < numFields; i++) {
		field = originalTable.getTableField(i);
		newField = new TableField(field.getDataType(), 
			new String(field.getName()), field.getWidth(), field.getPrecision());
		tableFields.add(newField);
	}
	newTable = new DataTable(tableFields);
	if (!cloneData) {
		return newTable;
	}
	newTable._haveDataInMemory = true;

	int numRecords = originalTable.getNumberOfRecords();
	int type = -1;
	TableRecord newRecord = null;
	for (int i = 0; i < numRecords; i++) {
		try {
    		newRecord = new TableRecord(numFields);
    		for (int j = 0; j < numFields; j++) {
    			type = newTable.getFieldDataType(j);
    			if (type == TableField.DATA_TYPE_INT) {
    	        	newRecord.addFieldValue(new Integer(((Integer)originalTable.getFieldValue(i, j)).intValue()));
    			}
    			else if (type == TableField.DATA_TYPE_SHORT) {
    	        	newRecord.addFieldValue(new Short(((Short)originalTable.getFieldValue(i, j)).shortValue()));
    			}
    			else if (type == TableField.DATA_TYPE_DOUBLE) {
    	        	newRecord.addFieldValue(new Double(((Double)originalTable.getFieldValue(i, j)).doubleValue()));
    			}
    			else if (type == TableField.DATA_TYPE_FLOAT) {
    	        	newRecord.addFieldValue(new Float(((Float)originalTable.getFieldValue(i, j)).floatValue()));
    			}
    			else if (type == TableField.DATA_TYPE_STRING) {
    	        	newRecord.addFieldValue(new String((String)originalTable.getFieldValue(i, j)));
    			}
    			else if (type == TableField.DATA_TYPE_DATE) {
    	        	newRecord.addFieldValue( ((Date)originalTable.getFieldValue(i, j)).clone());
    			}
                else if (type == TableField.DATA_TYPE_DATETIME) {
                    newRecord.addFieldValue( ((DateTime)originalTable.getFieldValue(i, j)).clone());
                }
    			else if (type == TableField.DATA_TYPE_LONG) {
                    newRecord.addFieldValue(new Long(((Long)originalTable.getFieldValue(i, j)).longValue()));
                }
    		}
    		newTable.addRecord(newRecord);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error adding record " + i + " to table.");
			Message.printWarning(2, routine, e);
		}
	}
	return newTable;
}

/**
Return a new TableRecord that is compatible with this table, where all values are null.  This is useful
for inserting new table records where only specific column value is known, in which case the record can be
modified with TableRecord.setFieldValue().
@return a new record with null objects in each value.
*/
public TableRecord emptyRecord ()
{
    TableRecord newRecord = new TableRecord();
    int nCol = getNumberOfFields();
    for ( int i = 0; i < nCol; i++ ) {
        newRecord.addFieldValue( null );
    }
    return newRecord;
}

/**
Used internally when parsing a delimited file to determine whether a field name is already present in a 
table's fields, so as to avoid duplication.
@param tableFields a list of the tableFields created so far for a table.
@param name the name of the field to check.
@return true if the field name already is present in the table fields, false if not.
*/
private static boolean findPreviousFieldNameOccurances(List<TableField> tableFields, String name) {
	int size = tableFields.size();
	TableField field = null;
	String fieldName = null;
	for (int i = 0; i < size; i++) {
		field = tableFields.get(i);
		fieldName = field.getName();
		if (name.equals(fieldName)) {
			return true;
		}
	}
	return false;
}

/**
Return the time series comments.
@return The comments list.
*/
public List<String> getComments ()
{   return __comments;
}

/**
Return the field data type, given an index.
@return Data type for specified zero-based index.
@param index field index (0+).
*/
public int getFieldDataType ( int index )
{	if ( _table_fields.size() <= index ) {
        throw new ArrayIndexOutOfBoundsException( "Table field index " + index + " is not valid." );
    }
    return (_table_fields.get ( index )).getDataType();
}

/**
Return the field data types for all of the fields.  This is useful because
code that processes all the fields can request the information once and then re-use.
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
{	int fieldType = getFieldDataType(index);
    int fieldWidth = getFieldWidth(index);
	if ( fieldType == TableField.DATA_TYPE_STRING ) {
		// Output left-justified and padded...
	    if ( fieldWidth < 0 ) {
	        // Variable width strings
	        return "%-s";
	    }
	    else {
	        return "%-" + fieldWidth + "." + getFieldWidth(index) + "s";
	    }
	}
	else {
        if ( (fieldType == TableField.DATA_TYPE_FLOAT) || (fieldType == TableField.DATA_TYPE_DOUBLE) ) {
            int precision = getFieldPrecision(index);
            if ( fieldWidth < 0 ) {
                if ( precision < 0 ) {
                    // No width precision specified - rely on data object representation
                    return "%f";
                }
                else {
                    return "%." + precision + "f";
                }
            }
            else {
                return "%" + fieldWidth + "." + precision + "f";
            }
		}
		else {
		    return "%" + fieldWidth + "d";
		}
	}
}

/**
Get C-style format specifiers that can be used to format field values for
output.  These formats can be used with StringUtil.formatString().
@return a new String array with the format specifiers.
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
		if ((_table_fields.get(i)).getName().equalsIgnoreCase(field_name)) {
			return i;
        }
	}

	// if this line is reached, the given field was never found
	throw new Exception( "Unable to find table field with name \"" + field_name + "\" in table \"" + getTableID() + "\"" );
}

/**
Return the field indices associated with the given field names.  This method simply
calls getFieldIndex() for each requested name.
@return array of indices associated with the given field names.
@param fieldNames Field names to look up.
@exception Exception if any field name is not found.
*/
public int [] getFieldIndices ( String [] fieldNames )
throws Exception
{   int [] fieldIndices = new int[fieldNames.length];
    for ( int i=0; i<fieldNames.length; i++ ) {
        fieldIndices[i] = getFieldIndex ( fieldNames[i] );
    }
    return fieldIndices;
}

/**
Return the field name, given an index.
@return Field name for specified zero-based index.
@param index field index.
*/
public String getFieldName ( int index )
{	return (_table_fields.get ( index )).getName();
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
{	return (_table_fields.get ( index )).getPrecision();
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
taken from memory (using this method) or read from file (using a derived class method).</b>
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
		" is not available (only " + num_recs + " are available)." );
	}

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index +
		" is not available (only " + num_fields + " have been established." );
	}

	TableRecord tableRecord = _table_records.get((int)record_index);
	Object o = tableRecord.getFieldValue(field_index);
	tableRecord = null;
	return o;
}

/**
Return the field values for all rows in the table for the requested field/column.
@return the field values for all rows in the table for the requested field/column
@param fieldName name of field for which to return values for all rows
*/
public List<Object> getFieldValues ( String fieldName )
throws Exception
{
    List<Object> values = new ArrayList<Object>();
    int columnNum = getFieldIndex(fieldName);
    int size = getNumberOfRecords();
    for ( int i = 0; i < size; i++ ) {
        values.add ( getFieldValue(i,columnNum) );
    }
    return values;
}

/**
Return the field width, given an index.
@return Field width for specified zero-based index.
@param index field index.
*/
public int getFieldWidth ( int index )
{	return (_table_fields.get ( index )).getWidth();
}

/**
Return the number of fields in the table.
@return number of fields in the table.
*/
public int getNumberOfFields ()
{	return _table_fields.size();
}

// TODO SAM 2010-09-22 Evaluate whether the records list size should be returned if records in memory?
/**
Return the number of records in the table.  <b>This value should be set by
code that manipulates the data table.  If the table records list has been
manipulated with a call to addRecord(), the size of the list will be returned.
Otherwise, the setNumberOfRecords() methods should be called appropriately and
its the value that is set will be returned.  This latter case
will be in effect if tables are being read on-the-fly.</b>
@return number of records in the table.
*/
public int getNumberOfRecords ()
{	if ( _add_record_called ) {
		return _table_records.size();
	}
	else {
	    return _num_records;
	}
}

/**
Return the TableRecord at a record index.
@param record_index Record index (zero-based).
@return TableRecord at specified record_index
*/
public TableRecord getRecord ( int record_index )
throws Exception
{	if ( !_haveDataInMemory ) {
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
	return (_table_records.get(record_index));
}

/**
Return the TableRecord for the given column and column value.  If multiple records are matched the first record is returned.
@param columnNum column number, 0+
@param columnValue column value to match in the records.  The first matching record is returned.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value or null if no record is matched.
*/
public TableRecord getRecord ( int columnNum, Object columnValue )
throws Exception
{
    int [] columnNums = new int[1];
    columnNums[0] = columnNum;
    List<Object> columnValues = new ArrayList<Object>();
    columnValues.add(columnValue);
    List<TableRecord> records = getRecords ( columnNums, columnValues );
    if ( records.size() == 0 ) {
        return null;
    }
    else {
        return records.get(0);
    }
}

/**
Return the TableRecord for the given column and column value.  If multiple records are matched
the first record is returned.
@param columnName name of column (field), case-insensitive.
@param columnValue column value to match in the records.  The first matching record is returned.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value or null if no record is matched.
*/
public TableRecord getRecord ( String columnName, Object columnValue )
throws Exception
{
    List<String> columnNames = new ArrayList<String>();
    columnNames.add(columnName);
    List<Object> columnValues = new ArrayList<Object>();
    columnValues.add(columnValue);
    List<TableRecord> records = getRecords ( columnNames, columnValues );
    if ( records.size() == 0 ) {
        return null;
    }
    else {
        return records.get(0);
    }
}

/**
Return a list of TableRecord matching the given columns and column values.
@param columnNames list of column (field) names, case-insensitive.
@param columnValue list of column values to match in the records.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value, guaranteed to be non-null but may be zero length.
*/
public List<TableRecord> getRecords ( List<String> columnNames, List<? extends Object> columnValues )
throws Exception
{
    // Figure out the column numbers that will be checked
    int iColumn = -1;
    int [] columnNumbers = new int[columnNames.size()];
    List<TableRecord> recList = new ArrayList<TableRecord>();
    for ( String columnName: columnNames ) {
        ++iColumn;
        // If -1 is returned then a column name does not exist and no matches are possible
        columnNumbers[iColumn] = getFieldIndex ( columnName );
        if ( columnNumbers[iColumn] < 0 ) {
            return recList;
        }
    }
    return getRecords ( columnNumbers, columnValues );
}

/**
Return a list of TableRecord matching the given columns and column values.
@param columnNumbers list of column (field) numbers, 0+.  Any values < 0 will result in an empty list being returned.
@param columnValue list of column values to match in the records.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value, guaranteed to be non-null but may be zero length.
*/
public List<TableRecord> getRecords ( int [] columnNumbers, List<? extends Object> columnValues )
throws Exception
{   if ( !_haveDataInMemory ) {
        // Most likely a derived class is not handling on the fly
        // reading of data and needs more development.  Return null
        // because the limitation is likely handled elsewhere.
        // TODO SAM 2013-07-02 Why not return an empty list here?
        return null;
    }
    List<TableRecord> recList = new ArrayList<TableRecord>();
    // Make sure column numbers are valid.
    for ( int iColumn = 0; iColumn < columnNumbers.length; iColumn++ ) {
        if ( columnNumbers[iColumn] < 0 ) {
            return recList;
        }
    }
    // Now search the the records and then the columns in the record
    Object columnContents;
    int iColumn = -1;
    for ( TableRecord rec : _table_records ) { // Loop through all table records
        int matchCount = 0; // How many column values match
        iColumn = -1;
        for ( Object columnValue: columnValues ) {
            ++iColumn;
            columnContents = rec.getFieldValue(columnNumbers[iColumn]);
            if ( columnContents == null ) {
                // Only match if both are match
                if ( columnValue == null ) {
                    ++matchCount;
                }
            }
            else if ( getFieldDataType(columnNumbers[iColumn]) == TableField.DATA_TYPE_STRING ) {
                // Do case insensitive comparison
                if ( ((String)columnValue).equalsIgnoreCase("" + columnContents)) {
                    ++matchCount;
                }
            }
            else {
                // Not a string so just use the equals() method to compare.
                if ( columnValue.equals(columnContents)) {
                    ++matchCount;
                }
            }
        }
        if ( matchCount == columnValues.size() ) {
            // Have matched the requested number of column values so add record to the match list
            recList.add(rec);
        }
    }
    return recList;
}

/**
Return the table identifier.
@return the table identifier.
*/
public String getTableID ()
{
    return __table_id;
}

/**
Return the list of TableRecords.
@return list of TableRecord.
*/
public List<TableRecord> getTableRecords ( )
{	return _table_records;
}

/**
Return the TableField object for the requested column.
@param index Table field index (zero-based).
@return TableField object for the specified zero-based index.
*/
public TableField getTableField ( int index )
{	return (_table_fields.get( index ));
}

/**
Get the data type for the field.
@return the data type for the field (see TableField.DATA_TYPE_*).
@param index index of field (zero-based).
@exception If the index is out of range.
@deprecated use getFieldDataType
*/
public int getTableFieldType ( int index )
{	if ( _table_fields.size() <= index ) {
		throw new ArrayIndexOutOfBoundsException( "Index " + index + " is not valid." );
	}
	return _table_fields.get(index).getDataType ();
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
Depending on the field data type, a double[], int[], short[], or String[] will be returned.
@exception if the field index is not in the allowed range.
*/
/* TODO SAM Implement this later.
public Object getUniqueFieldValues ( int field_index )
throws Exception
{	int num_recs = _table_records.size();
	int num_fields = _table_fields.size();

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index +
		" is not available (only " + num_fields +
		" are available)." );
	}

	// Use a temporary list to get the unique values...
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
Indicate whether the table has data in memory.  This will be true if any table records
have been added during a read or write operation.  This method is meant to be called by derived classes
that allow records to be accessed on the fly rather than from memory (e.g., dBase tables).
*/
public boolean haveDataInMemory ()
{	return _haveDataInMemory;
}

/**
Initialize the data.
@param tableFieldsList list of TableField used to define the DataTable.
@param listSize Initial list size for the list holding records.
@param sizeIncrement Increment for the list holding records.
*/
private void initialize ( List<TableField> tableFieldsList, int listSize, int sizeIncrement )
{	_table_fields = tableFieldsList;
	_table_records = new ArrayList<TableRecord> ( 10 );
}

/**
Insert a table record into the table.  If inserting at the start or middle, the provided table record will be inserted and
all other records will be shifted.  If inserting after the existing records, empty records will be added up to the requested
insert position.
@param row row position (0+) to insert the record
@param record table record to insert
@param doCheck indicate whether the record should be checked against the table for consistency; false inserts with no check
(currently this parameter is not enabled).  Use emptyRecord() to create a record that matches the table design.
@exception Exception if there is an error inserting the record
*/
public void insertRecord ( int row, TableRecord record, boolean doCheck )
throws Exception
{
    // TODO SAM 2014-02-01 enable doCheck
    int nRows = getNumberOfRecords();
    if ( row < nRows ) {
        // Inserting in the existing table
        _table_records.add ( row, record );
    }
    else {
        // Appending - add blank rows up until the last one
        for ( int i = nRows; i < row; i++ ) {
            addRecord(emptyRecord());
        }
        // Now add the final record
        addRecord ( record );
    }
}

/**
Returns whether any of the table records are dirty or not.
@return whether any of the table records are dirty or not.
*/
public boolean isDirty() {
	TableRecord record = null;
	int recordCount = getNumberOfRecords();

	for (int i = 0; i < recordCount; i++) {
		record = _table_records.get(i);
		if (record.isDirty()) {
			return true;
		}
	}
	return false;
}

/**
Join one table to another by matching column column values.
@param table original table
@param tableToJoin table being joined
@param joinColumnsMap map indicating which tables need to be matched in the tables, for the join
@param reqIncludeColumns requested columns to include from the second table or null to include all
(the join tables will be automatically included because they exist in the first table)
@param columnMap map to rename original columns to new name
@param columnFilters map for columns that will apply a filter to limit rows that are processed
@param joinMethod the method used to join the tables
@param problems list of problems that will be filled during processing
@return the number of rows appended
*/
public int joinTable ( DataTable table, DataTable tableToJoin, Hashtable<String,String> joinColumnsMap, String [] reqIncludeColumns,
    Hashtable<String,String> columnMap, Hashtable<String,String> columnFilters, DataTableJoinMethodType joinMethod,
    List<String> problems )
{   String routine = getClass().getName() + ".joinTable", message;

    // List of columns that will be appended to the first table
    String [] columnNamesToAppend = null;
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Append only the requested names
        columnNamesToAppend = reqIncludeColumns;
        for ( int icol = 0; icol < reqIncludeColumns.length; icol++ ) {
            Message.printStatus(2,routine,"Will include table2 column \"" + reqIncludeColumns[icol] + "\"" );
        }
    }
    else {
        // Append all
        Message.printStatus(2,routine,"Joining all columns in table2 to table1.");
        columnNamesToAppend = tableToJoin.getFieldNames();
    }
    // Make sure that the columns to append do not include the join columns, which should already by in the tables.
    // Just set to blank so they can be ignored in following logic
    for ( int icol = 0; icol < columnNamesToAppend.length; icol++ ) {
        Enumeration keys = joinColumnsMap.keys();
        while ( keys.hasMoreElements() ) {
            String key = (String)keys.nextElement();
            if ( columnNamesToAppend[icol].equalsIgnoreCase(key) ) {
                Message.printStatus(2,routine,"Table 2 column to join is same as join column.  Ignoring.");
                columnNamesToAppend[icol] = "";
            }
        }
    }
    // TODO 2013-08-19 if the column name to append matches the original table, need to automatically rename with a number
    // at the end, but do this after the column mapping below.
    // Column numbers in the append table to match the original table.  Any values set to -1 will result in null in output.
    String [] table1AppendColumnNames = new String[reqIncludeColumns.length];
    int [] table1AppendColumnNumbers = new int[reqIncludeColumns.length];
    int [] table1AppendColumnTypes = new int[reqIncludeColumns.length];
    String [] table2AppendColumnNames = new String[reqIncludeColumns.length];
    int [] table2AppendColumnNumbers = new int[reqIncludeColumns.length];
    int [] table2AppendColumnTypes = new int[reqIncludeColumns.length];
    // Replace the append table names using the column map
    Object o;
    for ( int icol = 0; icol < reqIncludeColumns.length; icol++ ) {
        table1AppendColumnNames[icol] = reqIncludeColumns[icol]; // Default to same as requested
        table2AppendColumnNames[icol] = reqIncludeColumns[icol]; // Default
        try {
            table2AppendColumnNumbers[icol] = tableToJoin.getFieldIndex(table2AppendColumnNames[icol]);
        }
        catch ( Exception e ) {
            message = "Cannot determine table2 append column number for \"" + table2AppendColumnNames[icol] + "\".";
            problems.add ( message );
            Message.printWarning(3,routine,message);
        }
        table2AppendColumnTypes[icol] = tableToJoin.getFieldDataType(table2AppendColumnNumbers[icol]);
        if ( columnMap != null ) {
            // Initialize the table2 column to join from the requested columns, with matching name in both tables.
            // Rename in output (table1)
            o = columnMap.get(table2AppendColumnNames[icol]);
            if ( o != null ) {
                // Reset the append column name with the new name, which will match a column name in the first table
                // (or will be created in the new table if necessary)
                // This column may not yet exist in the joined table so get column number and type below after column is added
                table1AppendColumnNames[icol] = (String)o;
            }
        }
        Message.printStatus(2,routine,"Will copy table2 column \"" + table2AppendColumnNames[icol] + "\" to table1 column \"" +
            table1AppendColumnNames[icol] + "\"" );
    }
    
    // Create columns in the output table for the "include columns" (including new column names from the column map)
    // Use column types that match the append table's column types
    // Figure out the column numbers in both tables for the include
    for ( int icol = 0; icol < table1AppendColumnNames.length; icol++ ) {
        table1AppendColumnNumbers[icol] = -1;
        if ( table1AppendColumnNames[icol].length() == 0 ) {
            // Name was removed above because it duplicates the join column, so don't add
            continue;
        }
        try {
            table1AppendColumnNumbers[icol] = table.getFieldIndex(table1AppendColumnNames[icol]);
        }
        catch ( Exception e ) {
             // OK - handle non-existent column below.
        }
        if ( table1AppendColumnNumbers[icol] >= 0 ) {
            // Already exists so skip because don't want table2 values to overwrite table1 values
            message = "Include column \"" + table1AppendColumnNames[icol] +
                "\" already exists in original table.  Not adding new column.";
            Message.printStatus(2,routine,message);
            // TODO SAM 2014-04-15 Actually, do want join to overwrite - allows subset of table to be processed
            //table1AppendColumnNumbers[icol] = -1;
            table1AppendColumnTypes[icol] = table.getFieldDataType(table1AppendColumnNumbers[icol]);
        }
        else {
            // Does not exist in first table so create column with the same properties as the original
            // Use the original column name to find the property
            try {
                Message.printStatus(2,routine,"Creating table1 column \"" + table1AppendColumnNames[icol] +
                    "\" type=" + TableColumnType.valueOf(tableToJoin.getFieldDataType(table2AppendColumnNumbers[icol])) +
                    " width=" + tableToJoin.getFieldWidth(table2AppendColumnNumbers[icol]) +
                    " precision=" + tableToJoin.getFieldPrecision(table2AppendColumnNumbers[icol]));
                table1AppendColumnNumbers[icol] = table.addField(
                    new TableField(tableToJoin.getFieldDataType(table2AppendColumnNumbers[icol]),
                    table1AppendColumnNames[icol],tableToJoin.getFieldWidth(table2AppendColumnNumbers[icol]),
                    tableToJoin.getFieldPrecision(table2AppendColumnNumbers[icol])), null);
                table1AppendColumnTypes[icol] = table.getFieldDataType(table1AppendColumnNumbers[icol]);
            }
            catch ( Exception e ) {
                message = "Error adding new column \"" + table1AppendColumnNames[icol] + "\" to joined table (" + e + ").";
                problems.add ( message );
                Message.printWarning(3,routine,message);
            }
        }
    }

    // Determine the column numbers in the first and second tables for the join columns
    // Do this AFTER the above checks on output columns because columns may be inserted and change the column order
    if ( reqIncludeColumns == null ) {
        reqIncludeColumns = new String[0];
    }
    String [] table1JoinColumnNames = new String[joinColumnsMap.size()];
    int [] table1JoinColumnNumbers = new int[joinColumnsMap.size()];
    int [] table1JoinColumnTypes = new int[joinColumnsMap.size()];
    String [] table2JoinColumnNames = new String[joinColumnsMap.size()];
    int [] table2JoinColumnNumbers = new int[joinColumnsMap.size()];
    int [] table2JoinColumnTypes = new int[joinColumnsMap.size()];
    Enumeration keys = joinColumnsMap.keys();
    String key;
    int ikey = -1;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        table1JoinColumnNames[ikey] = "";
        table1JoinColumnNumbers[ikey] = -1;
        table2JoinColumnNames[ikey] = "";
        table2JoinColumnNumbers[ikey] = -1;
        try {
            table1JoinColumnNames[ikey] = (String)keys.nextElement();
            table1JoinColumnNumbers[ikey] = table.getFieldIndex(table1JoinColumnNames[ikey]);
            table1JoinColumnTypes[ikey] = table.getFieldDataType(table1JoinColumnNumbers[ikey]);
            Message.printStatus(2,routine,"Table1 join column \"" + table1JoinColumnNames[ikey] + "\" has table1 column number=" +
                table1JoinColumnNumbers[ikey]);
            try {
                table2JoinColumnNames[ikey] = (String)joinColumnsMap.get(table1JoinColumnNames[ikey]);
                table2JoinColumnNumbers[ikey] = tableToJoin.getFieldIndex(table2JoinColumnNames[ikey]);
                table2JoinColumnTypes[ikey] = tableToJoin.getFieldDataType(table2JoinColumnNumbers[ikey]);
                Message.printStatus(2,routine,"Table2 join column \"" + table2JoinColumnNames[ikey] + "\" has table2 column number=" +
                    table2JoinColumnNumbers[ikey]);
            }
            catch ( Exception e ) {
                message = "Table2 join column \"" + table2JoinColumnNames[ikey] + "\" not found in table \"" +
                    tableToJoin.getTableID() + "\".";
                problems.add ( message );
                Message.printWarning(3,routine,message);
            }
        }
        catch ( Exception e ) {
            message = "Join column \"" + table1JoinColumnNames[ikey] + "\" not found in first table \"" + table.getTableID() + "\".";
            problems.add (message);
            Message.printWarning(3,routine,message);
        }
    }
    
    // Get filter columns and glob-style regular expressions
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobs = new String[columnFilters.size()];
    keys = columnFilters.keys();
    ikey = -1;
    key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = (String)keys.nextElement();
            columnNumbersToFilter[ikey] = tableToJoin.getFieldIndex(key);
            columnFilterGlobs[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation
            columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            message = "Filter column \"" + key + "\" not found in table \"" + tableToJoin.getTableID() + "\".";
            problems.add ( message );
            Message.printWarning(3,routine,message);
        }
    }
    // Loop through all of the records in the table being joined and check the filters.
    // Do this up front because the records are checked multiple times during the join
    boolean [] joinTableRecordMatchesFilter = new boolean[tableToJoin.getNumberOfRecords()];
    int icol;
    int nrowsJoined = 0;
    String s;
    for ( int irow = 0; irow < tableToJoin.getNumberOfRecords(); irow++ ) {
        joinTableRecordMatchesFilter[irow] = true;
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing append
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    joinTableRecordMatchesFilter[irow] = false;
                    break;
                }
                try {
                    o = tableToJoin.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        joinTableRecordMatchesFilter[irow] = false;
                        break; // Don't include nulls when checking values
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobs[icol]) ) {
                        // A filter did not match so don't copy the record
                        joinTableRecordMatchesFilter[irow] = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                    message = "Error getting append table data for [" + irow + "][" + columnNumbersToFilter[icol] + "] (" + e + ").";
                    problems.add(message);
                    Message.printWarning(3, routine, message );
                }
            }
        }
    }
    // Loop through all the data records in the original table (the original records, NOT any that have been appended due
    // to the join), loop through records in the join table, and join records to the table original as appropriate (this may
    // result in a modification of the same records, or appending new records at the bottom of the table).
    // Keep track of which rows do not match and add at the end.  Otherwise, duplicate rows are added.
    boolean [] joinTableRecordMatchesTable1 = new boolean[tableToJoin.getNumberOfRecords()];
    int tableNumRows = table.getNumberOfRecords();
    boolean joinColumnsMatch = false; // Indicates whether two tables' join column values match
    Object table1Value, table2Value;
    String stringTable1Value, stringTable2Value;
    TableRecord recToModify = null;
    // Loop through all rows in the first table
    for ( int irow = 0; irow < tableNumRows; irow++ ) {
        // Loop through all rows in the second table
        for ( int irowJoin = 0; irowJoin < tableToJoin.getNumberOfRecords(); irowJoin++ ) {
            if ( !joinTableRecordMatchesFilter[irowJoin] ) {
                // Join row did not match filter so no need to process it
                continue;
            }
            else {
                // Join table record matched filter so evaluate if the join column values match in the two tables.
                // If there is a match, the join will be done in-line with an existing record.
                // If not, the join will only occur if the join method is JOIN_ALWAYS and in this case the join column values
                // and all append values will be added to the main table in a new row.
                joinColumnsMatch = true; // Set to false in checks below
                table1Value = null;
                table2Value = null;
                for ( icol = 0; icol < table1JoinColumnNumbers.length; icol++ ) {
                    if ( (table1JoinColumnNumbers[icol] < 0) || (table2JoinColumnNumbers[icol] < 0) ) {
                        // Something did not check out above so ignore to avoid more errors.
                        continue;
                    }
                    try {
                        table1Value = table.getFieldValue(irow, table1JoinColumnNumbers[icol]);
                    }
                    catch ( Exception e ) {
                        message = "Error getting table1 value to check join (" + e + ").";
                        problems.add ( message );
                        Message.printWarning(3, routine, message );
                    }
                    try {
                        table2Value = tableToJoin.getFieldValue(irowJoin, table2JoinColumnNumbers[icol]);
                    }
                    catch ( Exception e ) {
                        message = "Error getting table2 value to check join (" + e + ").";
                        problems.add ( message );
                        Message.printWarning(3, routine, message );
                    }
                    // For now if either is null do not add the record
                    if ( (table1Value == null) || (table2Value == null) ) {
                        joinColumnsMatch = false;
                        break;
                    }
                    else if ( table1JoinColumnTypes[icol] == TableField.DATA_TYPE_STRING) {
                        stringTable1Value = (String)table1Value;
                        stringTable2Value = (String)table2Value;
                        if ( !stringTable1Value.equalsIgnoreCase(stringTable2Value) ) {
                            joinColumnsMatch = false;
                            break;
                        }
                    }
                    else if ( !table1Value.equals(table2Value) ) {
                        joinColumnsMatch = false;
                        break;
                    }
                }
                //Message.printStatus(2,routine,"Join value1=\"" + table1Value + "\" value2=\""+ table2Value + "\" match=" + joinColumnsMatch);
                if ( joinColumnsMatch ) {
                    //Message.printStatus(2,routine,"Setting in existing row.");
                    joinTableRecordMatchesTable1[irowJoin] = true;
                    try {
                        recToModify = table.getRecord(irow); // Modify existing row in table
                    }
                    catch ( Exception e ) {
                        message = "Error getting existing joined record to modify (" + e + ").";
                        problems.add ( message );
                        Message.printWarning(3, routine, message );
                    }
                    // Loop through the columns to include and set the values from
                    // the second table into the first table (which previously had columns added)
                    for ( icol = 0; icol < table2AppendColumnNumbers.length; icol++ ) {
                        try {
                            if ( table1AppendColumnNumbers[icol] < 0 ) {
                                // There was an issue with the column to add so skip
                                Message.printStatus(2,routine,"Don't have column number for table1 column \"" +
                                     table1AppendColumnNames[icol] + "\"");
                                continue;
                            }
                            else if ( table2AppendColumnNumbers[icol] < 0 ) {
                                // There was an issue with the column to add so skip
                                Message.printStatus(2,routine,"Don't have column number for table2 column \"" +
                                     table2AppendColumnNames[icol] + "\"");
                                continue;
                            }
                            else {
                                // Set the value in the original table, if the type matches
                                // TODO SAM 2013-08-19 Check that the column types match
                                if ( table1AppendColumnTypes[icol] == table2AppendColumnTypes[icol] ) {
                                    recToModify.setFieldValue(table1AppendColumnNumbers[icol],
                                        tableToJoin.getFieldValue(irowJoin, table2AppendColumnNumbers[icol]));
                                    ++nrowsJoined;
                                }
                                else {
                                    Message.printStatus(2,routine,"Column types are different, cannot set value from table2 to table1.");
                                }
                            }
                        }
                        catch ( Exception e ) {
                            // Should not happen
                            message = "Error setting [" + irow + "][" + table1AppendColumnNumbers[icol] + "] (" + e + ").";
                            problems.add(message);
                            Message.printWarning(3, routine, message );
                        }
                    }
                }
            }
        }
    }
    // Now add any rows that were not matched
    if ( joinMethod == DataTableJoinMethodType.JOIN_ALWAYS ) {
        for ( int irowJoin = 0; irowJoin < tableToJoin.getNumberOfRecords(); irowJoin++ ) {
            if ( joinTableRecordMatchesTable1[irowJoin] ) {
                // Row was matched above so no need to add again.
                continue;
            }
            // Add a row to the table, containing only the join column values from the second table
            // and nulls for all the other values
            try {
                recToModify = table.addRecord(table.emptyRecord());
            }
            catch ( Exception e ) {
                message = "Error adding new record to modify (" + e + ").";
                problems.add ( message );
                Message.printWarning(3, routine, message );
            }
            // A new record was added.  Also include the join column values using the table1 names
            // TODO SAM 2013-08-19 Evaluate whether table2 names should be used (or option to use)
            for ( icol = 0; icol < table2JoinColumnNumbers.length; icol++ ) {
                try {
                    if ( table2JoinColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip
                        continue;
                    }
                    else {
                        // Set the value in the original table, if the type matches
                        // TODO SAM 2013-08-19 Check that the column types match
                        if ( table1JoinColumnTypes[icol] == table2JoinColumnTypes[icol] ) {
                            recToModify.setFieldValue(table1JoinColumnNumbers[icol],
                                tableToJoin.getFieldValue(irowJoin, table2JoinColumnNumbers[icol]));
                            ++nrowsJoined;
                        }
                    }
                }
                catch ( Exception e ) {
                    // Should not happen
                    message = "Error setting row value for column [" + table1JoinColumnNumbers[icol] + "] (" + e + ").";
                    problems.add(message);
                    Message.printWarning(3, routine, message );
                    Message.printWarning(3, routine, e );
                }
            }
            // Loop through the columns to include and set the values from
            // the second table into the first table (which previously had columns added)
            for ( icol = 0; icol < table2AppendColumnNumbers.length; icol++ ) {
                try {
                    if ( table1AppendColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip
                        Message.printStatus(2,routine,"Don't have column number for table1 column \"" +
                             table1AppendColumnNames[icol]);
                        continue;
                    }
                    else if ( table2AppendColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip
                        Message.printStatus(2,routine,"Don't have column number for table2 column \"" +
                             table2AppendColumnNames[icol] + "\"");
                        continue;
                    }
                    else {
                        // Set the value in the original table, if the type matches
                        // TODO SAM 2013-08-19 Check that the column types match
                        if ( table1AppendColumnTypes[icol] == table2AppendColumnTypes[icol] ) {
                            recToModify.setFieldValue(table1AppendColumnNumbers[icol],
                                tableToJoin.getFieldValue(irowJoin, table2AppendColumnNumbers[icol]));
                            ++nrowsJoined;
                        }
                        else {
                            Message.printStatus(2,routine,"Column types are different, cannot set value from table2 to table1.");
                        }
                    }
                }
                catch ( Exception e ) {
                    // Should not happen
                    message = "Error adding new row, column [" + table1AppendColumnNumbers[icol] + "] (" + e + ").";
                    problems.add(message);
                    Message.printWarning(3, routine, message );
                }
            }
        }
    }
    if ( problems.size() > 0 ) {
        throw new RuntimeException ( "There were " + problems.size() + " errors joining table \"" + tableToJoin.getTableID() + "\" to \"" +
            table.getTableID() + "\"" );
    }
    return nrowsJoined;
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields list of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile ( String filename, String delimiter,
    List<TableField> tableFields, int num_lines_header )
throws Exception {
	return parseDelimitedFile(filename, delimiter, tableFields,	num_lines_header, false);
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields list of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@param trim_spaces if true, then when a column value is read between delimiters,
it will be .trim()'d before being parsed into a number or String. 
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile ( String filename, String delimiter, List<TableField> tableFields,
	int num_lines_header, boolean trim_spaces)
throws Exception {
	return parseDelimitedFile(filename, delimiter, tableFields, num_lines_header, trim_spaces, -1);
}

/**
Given a definition of what data to expect, read a simple delimited file and
store the data in a table.  Comment lines start with # and are not considered part of the header.
This method may not be maintained in the future.
The parseFile() method is more flexible.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields list of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header
lines are read and ignored.
@param trim_spaces if true, then when a column value is read between delimiters,
it will be .trim()'d before being parsed into a number or String. 
@param maxLines the maximum number of lines to read from the file.  If less than
or equal to 0, all lines will be read.
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile ( String filename, String delimiter, List<TableField> tableFields,
	int num_lines_header, boolean trim_spaces, int maxLines)
throws Exception
{
	String iline;
	boolean processed_header = false;
	List<String> columns;
	int num_fields=0, num_lines_header_read=0;
	int lineCount = 0;
	DataTable table;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	table = new DataTable( tableFields );
	table._haveDataInMemory = true;
	int field_types[] = table.getFieldDataTypes();
	if ( num_lines_header == 0 ) {
		processed_header = true;
		num_fields = field_types.length;
	}

	String col = null;

	// Create an array to use for determining the maximum size of all the
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

		// TODO SAM if a column contains only quoted strings, but each string is a number, then there is no
		// way to treat the column as strings.  This may be problematic if the string is zero-padded.
		columns = StringUtil.breakStringList ( iline, delimiter, StringUtil.DELIM_ALLOW_STRINGS);

		// line is part of header ... 
		if ( !processed_header ) {
			num_fields = columns.size();
			if ( num_fields < tableFields.size() ) {
				throw new IOException ( "Table fields specifications do not match data found in file." );
			}
			
			num_lines_header_read++;
			if ( num_lines_header_read == num_lines_header ) {
				processed_header = true;
			}
		}
		else {
		    // line contains data - store in table as record
			TableRecord contents = new TableRecord(num_fields);
			try {						
    			for ( int i=0; i<num_fields; i++ ) {
    				col = columns.get(i);
    				if (trim_spaces) {
    					col = col.trim();
    				}
    				if ( field_types[i] == TableField.DATA_TYPE_STRING ) {
    					contents.addFieldValue(col);
    					length = col.length();
    					if (length > stringLengths[i]) {
    						stringLengths[i] = length;
    					}
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_DOUBLE ){
    					contents.addFieldValue(	new Double(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_INT ) {
    					contents.addFieldValue(	new Integer(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_SHORT ) {
    					contents.addFieldValue(	new Short(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_FLOAT ) {
    					contents.addFieldValue(	new Float(col));
    				}
                    else if ( field_types[i] == TableField.DATA_TYPE_LONG ) {
                        contents.addFieldValue( new Long(col));
                    }
    			}
    			table.addRecord ( contents );
    			contents = null;
			} catch ( Exception e ) {
				if (IOUtil.testing()) {
					e.printStackTrace();
				}
				Message.printWarning ( 2, "DataTable.parseDelimitedFile", e );
			}
		}
		lineCount++;
		if (maxLines > 0 && lineCount >= maxLines) {
			in.close();

			// Set the widths of the string fields to the length
			// of the longest strings within those fields
			for (int i = 0; i < num_fields; i++) {
				col = columns.get(i);
				if (field_types[i] == TableField.DATA_TYPE_STRING) {
					table.setFieldWidth(i, stringLengths[i]);
				}
			}
							
			return table;
		}
	}
	in.close();
	return table;
}

/**
Reads the header of a comma-delimited file and return list of TableField objects.
@return list of TableField objects (only field names will be set).
@param filename name of file containing delimited data.
*/
public static List<TableField> parseDelimitedFileHeader ( String filename )
throws Exception
{	return parseDelimitedFileHeader ( filename, "," );
}

/**
Reads the header of a delimited file and return list of TableField objects.
The field names will be correctly returned.  The data type, however, will be set
to TableField.DATA_TYPE_STRING.  This should be changed if not appropriate.
@return list of TableField objects (field names will be correctly set but data type will be string).
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file.
@exception Exception if there is an error reading the file.
*/
public static List<TableField> parseDelimitedFileHeader ( String filename, String delimiter )
throws Exception
{	String iline;
	List<String> columns;
	List<TableField> tableFields = null;
	int num_fields=0;
	TableField newTableField = null;

	BufferedReader in = new BufferedReader ( new FileReader ( filename ));

	try {
    	while (( iline = in.readLine ()) != null ) {
    
    		// check if read comment or empty line
    		if ( iline.startsWith("#") || iline.trim().length()==0) {
    			continue;
    		}
    
    		columns = StringUtil.breakStringList ( iline, delimiter, 0);
    //			StringUtil.DELIM_SKIP_BLANKS );
    
    		num_fields = columns.size();
    		tableFields = new ArrayList<TableField> ( num_fields );
    		for ( int i=0; i<num_fields; i++ ) {
    			newTableField = new TableField ( );
    			newTableField.setName (	columns.get(i).trim());
    			newTableField.setDataType(TableField.DATA_TYPE_STRING);
    			tableFields.add ( newTableField );
    		}
    		break;
    	}
	}
	finally {
	    if ( in != null ) {
	        in.close();
	    }
	}
	return tableFields;
}

// TODO SAM 2012-01-09 Need to handle doubled and tripled quotes as per:
// http://en.wikipedia.org/wiki/Comma-separated_values
// For now assume no embedded quotes in quoted strings
/**
Parses a file and returns the DataTable for the file.  Currently only does
delimited files, and the data type for a column must be consistent.
The lines in delimited files do not need to all have the same
number of columns: the number of columns in the returned DataTable will be 
the same as the line in the file with the most delimited columns, all others
will be padded with empty value columns on the right of the table.
@param filename the name of the file from which to read the table data.
@param props a PropList with settings for how the file should be read and handled.<p>
Properties and their effects:<br>
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>    <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>ColumnDataTypes</b></td>
<td>The data types for the column, either "Auto" (determine from column contents),
"AllStrings" (all are strings, fastest processing and the default from historical behavior),
or a list of data types (to be implemented in the future).</td>
<td>AllStrings.</td>
</tr>

<tr>
<td><b>CommentLineIndicator</b></td>
<td>The characters with which comment lines begin.
Lines starting with this character are skipped (TrimInput is applied after checking for comments).</td>
<td>No default.</td>
</tr>

<tr>
<td><b>Delimiter</b></td>
<td>The character (s) that should be used to delimit fields in the file.  Fields are broken
using the following StringUtil.breakStringList() call (the flag can be modified by MergeDelimiters):<br>
<blockquote>
    v = StringUtil.breakStringList(line, delimiters, 0);
</blockquote><br></td>
<td>Comma (,).</td>
</tr>

<tr>
<td><b>FixedFormat</b></td>
<td>"True" or "False".  Currently ignored.</td>
<td></td>
</tr>

<tr>
<td><b>HeaderLines (previously HeaderRows)</b></td>
<td>The lines containing the header information, specified as single number or a range (e.g., 2-3).
Multiple lines will be separated with a newline when displayed, or Auto to automatically treat the
first non-comment row as a header if the value is double-quoted.</td>
<td>Auto</td>
</tr>

<tr>
<td><b>MergeDelimiters</b></td>
<td>"True" or "False".  If true, then adjoining delimiter characters are treated as one by using
StringUtil.breakStringList(line,delimiters,StringUtil.DELIM_SKIP_BLANKS.</td>
<td>False (do not merge blank columns).</td>
</tr>

<tr>
<td><b>SkipLines (previously SkipRows)</b></td>
<td>Lines from the original file to skip (each value 0+), as list of comma-separated individual row or
ranges like 3-6.  Skipped lines are generally information that cannot be parsed.  The lines are skipped after
the initial read and are not available for further processing.</td>
<td>Don't skip any lines.</td>
</tr>

<tr>
<td><b>TrimInput</b></td>
<td>"True" or "False".  Indicates input strings should be trimmed before parsing.</td>
<td>False</td>
</tr>

<tr>
<td><b>TrimStrings</b></td>
<td>"True" or "False".  Indicates whether strings should
be trimmed before being placed in the data table (after parsing).</td>
<td>False</td>
</tr>

</table>
@return the DataTable that was created.
@throws Exception if an error occurs
*/
public static DataTable parseFile(String filename, PropList props) 
throws Exception
{   String routine = "DataTable.parseFile";
	if ( props == null ) {
		props = new PropList(""); // To simplify code below
	}
	// TODO SAM 2005-11-16 why is FixedFormat included?  Future feature?
	/*String propVal = props.getValue("FixedFormat");
	if (propVal != null) {
		if (propVal.equalsIgnoreCase("false")) {
			fixed = false;
		}
	}
	*/
   
    // FIXME SAM 2008-01-27 Using other than the default of strings does not seem to work
    // The JWorksheet does not display correctly.
    boolean ColumnDataTypes_Auto_boolean = false;   // To improve performance below
    // TODO SAM 2008-04-15 Evaluate whether the following should be used
    //String ColumnDataTypes = "AllStrings";  // Default for historical reasons
    String propVal = props.getValue("ColumnDataTypes");
    if ( (propVal != null) && (propVal.equalsIgnoreCase("Auto"))) {      
        //ColumnDataTypes = "Auto";
        ColumnDataTypes_Auto_boolean = true;
    }

    String Delimiter = "";
	propVal = props.getValue("Delimiter");
	if (propVal != null) {		
        Delimiter = propVal;
	}
	else {
        Delimiter = ",";
	}
	
    propVal = props.getValue("HeaderLines");
    if ( propVal == null ) {
        // Use older form...
        propVal = props.getValue("HeaderRows");
        if ( propVal != null ) {
            Message.printWarning(3, routine, "Need to convert HeaderRows parameter to HeaderLines in software." );
        }
    }
    List<Integer> HeaderLineList = new ArrayList<Integer>();
    int HeaderLinesList_maxval = -1;  // Used to optimize code below
    boolean HeaderLines_Auto_boolean = false;    // Are header rows to be determined automatically?
    if ( (propVal == null) || (propVal.length() == 0) ) {
        // Default...
        HeaderLines_Auto_boolean = true;
    }
    else {
        // Interpret the property.
        Message.printStatus ( 2, routine, "HeaderLines=\"" + propVal + "\"" );
        if ( propVal.equalsIgnoreCase("Auto")) {
            HeaderLines_Auto_boolean = true;
        }
        else {
            // Determine the list of rows to skip.
            List<String> v = StringUtil.breakStringList ( propVal, ", ", StringUtil.DELIM_SKIP_BLANKS );
            int vsize = 0;
            if ( v != null ) {
                vsize = v.size();
            }
            // FIXME SAM 2008-01-27 Figure out how to deal with multi-row headings.  For now only handle first
            if ( vsize > 1 ) {
                Message.printWarning ( 3, routine,
                   "Only know how to handle single-row headings.  Ignoring other heading rows." );
                vsize = 1;
            }
            for ( int i = 0; i < vsize; i++ ) {
                String vi = v.get(i);
                if ( StringUtil.isInteger(vi)) {
                    int row = Integer.parseInt(vi);
                    Message.printStatus ( 2, routine, "Header row is [" + row + "]");
                    HeaderLineList.add(new Integer(row));
                    HeaderLinesList_maxval = Math.max(HeaderLinesList_maxval, row);
                }
                else {
                    int pos = vi.indexOf("-");
                    if ( pos >= 0 ) {
                        // Specifying a range of values...
                        int first_to_skip = -1;
                        int last_to_skip = -1;
                        if ( pos == 0 ) {
                            // First index is 0...
                            first_to_skip = 0;
                        }
                        else {
                            // Get first to skip...
                            first_to_skip = Integer.parseInt(vi.substring(0,pos).trim());
                        }
                        last_to_skip = Integer.parseInt(vi.substring(pos+1).trim());
                        for ( int is = first_to_skip; is <= last_to_skip; is++ ) {
                            HeaderLineList.add(new Integer(is));
                            HeaderLinesList_maxval = Math.max(HeaderLinesList_maxval, is);
                        }
                    }
                }
            }
        }
    }
    // Use to speed up code below.
    int HeaderLinesList_size = HeaderLineList.size();

	int parseFlagHeader = StringUtil.DELIM_ALLOW_STRINGS;
	// Retain the quotes in data records makes sure that quoted numbers come across as intended as literal strings. 
    // This is important when numbers are zero padded, such as for station identifiers.
	int parseFlag = StringUtil.DELIM_ALLOW_STRINGS | StringUtil.DELIM_ALLOW_STRINGS_RETAIN_QUOTES;
	propVal = props.getValue("MergeDelimiters");
	if (propVal != null) {		
		parseFlag |= StringUtil.DELIM_SKIP_BLANKS;
		parseFlagHeader |= StringUtil.DELIM_SKIP_BLANKS;
	}

    String CommentLineIndicator = null;
	propVal = props.getValue("CommentLineIndicator");
	if (propVal != null) {
        CommentLineIndicator = propVal;
	}

    propVal = props.getValue("SkipLines");
    if ( propVal == null ) {
        // Try the older form...
        propVal = props.getValue("SkipRows");
        if ( propVal != null ) {
            Message.printWarning(3, routine, "Need to convert SkipRows parameter to SkipLines in software." );
        }
    }
    List<Integer> skipLinesList = new ArrayList<Integer>();
    int skipLinesList_maxval = - 1;
    if ( (propVal != null) && (propVal.length() > 0) ) {
        // Determine the list of rows to skip.
        List<String> v = StringUtil.breakStringList ( propVal, ", ", StringUtil.DELIM_SKIP_BLANKS );
        int vsize = 0;
        if ( v != null ) {
            vsize = v.size();
        }
        for ( int i = 0; i < vsize; i++ ) {
            String vi = v.get(i);
            if ( StringUtil.isInteger(vi)) {
                int row = Integer.parseInt(vi);
                skipLinesList.add(new Integer(row));
                skipLinesList_maxval = Math.max(skipLinesList_maxval, row);
            }
            else {
                int pos = vi.indexOf("-");
                if ( pos >= 0 ) {
                    // Specifying a range of values...
                    int first_to_skip = -1;
                    int last_to_skip = -1;
                    if ( pos == 0 ) {
                        // First index is 0...
                        first_to_skip = 0;
                    }
                    else {
                        // Get first to skip...
                        first_to_skip = Integer.parseInt(vi.substring(0,pos).trim());
                    }
                    last_to_skip = Integer.parseInt(vi.substring(pos+1).trim());
                    for ( int is = first_to_skip; is <= last_to_skip; is++ ) {
                        skipLinesList.add(new Integer(is));
                        skipLinesList_maxval = Math.max(skipLinesList_maxval, is);
                    }
                }
            }
        }
    }
    // Use to speed up code below.
    int skipLinesList_size = skipLinesList.size();
	
	propVal = props.getValue("TrimInput");
	boolean TrimInput_Boolean = false;	// Default
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		TrimInput_Boolean = true;
	}

    boolean TrimStrings_boolean = false;
	propVal = props.getValue("TrimStrings");
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		TrimStrings_boolean = true;
	}

	List<List<String>> data_record_tokens = new ArrayList<List<String>>();
	List<String> v = null;
	int maxColumns = 0;
	int size = 0;

	BufferedReader in = new BufferedReader(new FileReader(filename));
	String line;

	// TODO JTS 2006-06-05
	// Found a bug in DataTable.  If you attempt to call
	// parseFile() on a file of size 0 (no lines, no characters)
	// it will throw an exception.  This should be checked out in the future.
	
	// Read until the end of the file...
	
	int linecount = 0; // linecount = 1 for first line in file, for user perspective.
	int linecount0; // linecount0 = linecount - 1 (zero index), for code perspective.
	boolean headers_found = false; // Indicates whether the headers have been found
	List<TableField> tableFields = null; // Table fields as controlled by header or examination of data records
	int numFields = -1; // Number of table fields.
	TableField tableField = null; // Table field added below
	while ( true ) {
		line = in.readLine();
		if ( line == null ) {
		    // End of file...
		    break;
		}
		++linecount;
		linecount0 = linecount - 1;
		
		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine, "Line [" + linecount0 + "]: " + line );
		}
		
		// Skip any comments anywhere in the file.
		if ( (CommentLineIndicator != null) && line.startsWith(CommentLineIndicator) ) {
		    continue;
		}
		
		// Also skip the requested lines to skip linecount is 1+ while lines to skip are 0+
		
		if ( linecount0 <= skipLinesList_maxval ) {
		    // Need to check it...
		    if ( parseFile_LineMatchesLineFromList(linecount0,skipLinesList, skipLinesList_size)) {
		        // Skip the line as requested
                continue;
		    }
		}
		
		// "line" now contains the latest non-comment line so evaluate whether
	    // the line contains the column names.
	    
		if ( !headers_found && (HeaderLines_Auto_boolean ||
		    ((HeaderLineList != null) && linecount0 <= HeaderLinesList_maxval)) ) {
		    if ( HeaderLines_Auto_boolean ) {
		        // If a quote is detected, then this line is assumed to contain the name of the fields.
        	    if (line.startsWith("\"")) {
        	        tableFields = parseFile_ParseHeaderLine ( line, linecount0, TrimInput_Boolean, Delimiter, parseFlagHeader );
        	        numFields = tableFields.size();
        	        // Read another line of data to be used below
        	        headers_found = true;
        	        continue;
        	    }
		    }
		    else if ( HeaderLineList != null ) {
		        // Calling code has specified the header rows.  Check to see if this is a row.
		        if ( parseFile_LineMatchesLineFromList(linecount0,HeaderLineList, HeaderLinesList_size)) {
		            // This row has been specified as a header row so process it.
		            tableFields = parseFile_ParseHeaderLine ( line, linecount0, TrimInput_Boolean, Delimiter, parseFlagHeader );
		            numFields = tableFields.size();
		                
                    //FIXME SAM 2008-01-27 Figure out how to deal with multi-row headings
                    // What is the column name?
		            // If the maximum header row has been processed, indicate that headers have been found.
		            //if ( linecount0 == HeaderLines_Vector_maxval ) {
		                headers_found = true;
		            //}
		            // Now read another line of data to be used below.
		            continue;
		        }
		    }
		}
		
		if ( linecount0 <= HeaderLinesList_maxval ) {
		    // Currently only allow one header row so need to ignore other rows that are found
		    // (don't want them considered as data).
		    if ( parseFile_LineMatchesLineFromList(linecount0,HeaderLineList, HeaderLinesList_size)) {
		        continue;
		    }
		}

    	// Now evaluate the data lines.  Parse into tokens to allow evaluation of the number of columns below.
    	
        if ( TrimInput_Boolean ) {
			v = StringUtil.breakStringList(line.trim(), Delimiter, parseFlag );
		}
		else {
            v = StringUtil.breakStringList(line, Delimiter, parseFlag );
		}
		size = v.size();
		if (size > maxColumns) {
			maxColumns = size;
		}
		// Save the tokens from the data rows - this will NOT include comments, headers, or lines to be excluded.
		data_record_tokens.add(v);
	}
	// Close the file...
	in.close();
	
	// Make sure that the table fields are in place for the maximum number of columns.

	if (tableFields == null) {
		tableFields = new ArrayList<TableField>();
		for (int i = 0; i < maxColumns; i++) {
			// Default field definition builds String fields
			tableFields.add(new TableField());
		}
	}
	else {
		// Add enough fields to account for the maximum number of columns in the table.  
		String temp = null;
		for (int i = numFields; i < maxColumns; i++) {
			tableField = new TableField();
			temp = "Field_" + (i + 1);
			while (findPreviousFieldNameOccurances(tableFields,temp)) {
				temp = temp + "_2";
			}
			tableField.setName(temp);
			tableField.setDataType(TableField.DATA_TYPE_STRING);
			tableFields.add(tableField);
		}
	}
	
	// Loop through the data and determine what type of data are in each column.
	// Do this in any case because the length of the string columns and precision for floating point
	// columns need to be determined.
	
	numFields = tableFields.size();
	size = data_record_tokens.size();
	int [] count_int = new int[maxColumns];
    int [] count_double = new int[maxColumns];
    int [] count_string = new int[maxColumns];
    int [] count_blank = new int[maxColumns];
    int [] lenmax_string = new int[maxColumns];
    int [] precision = new int[maxColumns];
    for ( int icol = 0; icol < maxColumns; icol++ ) {
        count_int[icol] = 0;
        count_double[icol] = 0;
        count_string[icol] = 0;
        count_blank[icol] = 0;
        lenmax_string[icol] = 0;
        precision[icol] = 0;
    }
    // Loop through all rows of data that were read
    int vsize;
    String cell;
    String cell_trimmed; // Must have when checking for types.
    int periodPos; // Position of period in floating point numbers
    boolean isTypeFound = false;
	for ( int irow = 0; irow < size; irow++ ) {
	    v = data_record_tokens.get(irow);
	    vsize = v.size();
	    // Loop through all columns in the row.
	    for ( int icol = 0; icol < vsize; icol++ ) {
	        cell = v.get(icol);
	        cell_trimmed = cell.trim();
	        isTypeFound = false;
	        if ( cell_trimmed.length() == 0 ) {
	        	// Blank cell - can be any type and should not impact result
	        	++count_blank[icol];
	        	isTypeFound = true;
	        }
	        if ( StringUtil.isInteger(cell_trimmed)) {
	            ++count_int[icol];
	            // Length needed in case handled as string data
	            lenmax_string[icol] = Math.max(lenmax_string[icol], cell_trimmed.length());
	            isTypeFound = true;
	        }
	        // TODO SAM 2012-05-31 Evaluate whether this needs a more robust solution
	        // Sometimes long integers won't parse in the above but do get parsed as doubles below.  This can
	        // lead to treatment as a floating point number.  Instead, the column likely should be treated as
	        // strings.  An example is very long identifiers like "394359105411900".  For now the work-around
	        // is to add quotes in the original data to make sure the column is treated like a string.
	        // Could add a long but this cascades through a lot of code since the long type is not yet supported
	        // in DataTable
            if ( StringUtil.isDouble(cell_trimmed)) {
                ++count_double[icol];
                isTypeFound = true;
                // Length needed in case handled as string data
                lenmax_string[icol] = Math.max(lenmax_string[icol], cell_trimmed.length());
                // Precision to help with visualization, such as table views
                periodPos = cell_trimmed.indexOf(".");
                if ( periodPos >= 0 ) {
                    precision[icol] = Math.max(precision[icol], (cell_trimmed.length() - periodPos - 1) );
                }
            }
            // TODO SAM 2008-01-27 Need to handle date/time?
            if ( !isTypeFound ) {
                // Assume string, but strip off the quotes if necessary
                ++count_string[icol];
                if ( TrimStrings_boolean ) {
                    lenmax_string[icol] = Math.max(lenmax_string[icol], cell_trimmed.length());
                }
                else {
                    lenmax_string[icol] = Math.max(lenmax_string[icol], cell.length());
                }
            }
	    }
	}
	
	// Loop through the table fields and based on the examination of data above,
	// set the table field type and if a string, max width.
	
	int [] tableFieldType = new int[tableFields.size()];
	if ( ColumnDataTypes_Auto_boolean ) {
    	for ( int icol = 0; icol < maxColumns; icol++ ) {
    	    tableField = (TableField)tableFields.get(icol);
    	    if ( (count_int[icol] > 0) && (count_string[icol] == 0) &&
    	        ((count_double[icol] == 0) || (count_int[icol] == count_double[icol])) ) {
    	        // All data are integers so assume column type is integer
    	        // Note that integers also meet the criteria of double, hence the extra check above
    	        // TODO SAM 2013-02-17 Need to handle DATA_TYPE_LONG
    	        tableField.setDataType(TableField.DATA_TYPE_INT);
    	        tableFieldType[icol] = TableField.DATA_TYPE_INT;
    	        tableField.setWidth (lenmax_string[icol] );
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] type is integer as determined from examining data (" + count_int[icol] +
    	            " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks).");
    	    }
    	    else if ( (count_double[icol] > 0) && (count_string[icol] == 0) ) {
    	        // All data are double (integers will also count as double) so assume column type is double
                tableField.setDataType(TableField.DATA_TYPE_DOUBLE);
                tableFieldType[icol] = TableField.DATA_TYPE_DOUBLE;
                tableField.setWidth (lenmax_string[icol] );
                tableField.setPrecision ( precision[icol] );
                Message.printStatus ( 2, routine, "Column [" + icol +
                    "] type is double as determined from examining data (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
            }
    	    else {
    	        // Based on what is known, can only treat column as containing strings.
    	        tableField.setDataType(TableField.DATA_TYPE_STRING);
    	        tableFieldType[icol] = TableField.DATA_TYPE_STRING;
    	        if ( lenmax_string[icol] <= 0 ) {
    	            // Likely that the entire column of numbers is empty so set the width to the field name
    	            // width if available)
    	            tableField.setWidth (tableFields.get(icol).getName().length() );
    	        }
    	        else {
    	            tableField.setWidth (lenmax_string[icol] );
    	        }
    	        Message.printStatus ( 2, routine, "Column [" + icol +
                    "] type is string as determined from examining data (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings), " +
                    count_blank[icol] + " blanks.");
    	       // Message.printStatus ( 2, routine, "length max=" + lenmax_string[icol] );
    	    }
    	}
	}
	else {
	    // All are strings (from above but reset just in case)...
	    for ( int icol = 0; icol < maxColumns; icol++ ) {
	        tableField = (TableField)tableFields.get(icol);
	        tableField.setDataType(TableField.DATA_TYPE_STRING);
	        tableFieldType[icol] = TableField.DATA_TYPE_STRING;
	        tableField.setWidth (lenmax_string[icol] );
	        Message.printStatus ( 2, routine,"Column [" + icol + "] type is " +
	            tableField.getDataType() + " all strings assumed, width =" + tableField.getWidth() );
	    }
	}
	// The data fields may have less columns than the headers and if so set the field type of the
	// unknown columns to string
	for ( int icol = maxColumns; icol < tableFields.size(); icol++) {
	    tableFieldType[icol] = TableField.DATA_TYPE_STRING;
	}
	
	// Create the table from the field information.

	DataTable table = new DataTable(tableFields);
	table._haveDataInMemory = true;
	TableRecord tablerec = null;
	
	// Now transfer the data records to table records.
	
	int cols = 0;
	int errorCount = 0;
	for (int irow = 0; irow < size; irow++) {
		v = data_record_tokens.get(irow);

		tablerec = new TableRecord(maxColumns);
		cols = v.size();
		for (int icol = 0; icol < cols; icol++) {
			if (TrimStrings_boolean) {
			    cell = v.get(icol).trim();
			}
			else {
				cell = v.get(icol);
			}
			if ( ColumnDataTypes_Auto_boolean ) {
			    // Set the data as an object of the column type.
			    if ( tableFieldType[icol] == TableField.DATA_TYPE_INT ) {
			    	cell = cell.trim();
			    	if ( cell.length() != 0 ) {
			    		tablerec.addFieldValue( Integer.valueOf(cell.trim()) );
			    	}
			    	else {
			    		tablerec.addFieldValue ( null );
			    	}
			    }
			    else if ( tableFieldType[icol] == TableField.DATA_TYPE_DOUBLE ) {
			    	cell = cell.trim();
			    	if ( cell.length() != 0 ) {
			    		tablerec.addFieldValue( Double.valueOf(cell.trim()) );
			    	}
			    	else {
			    		tablerec.addFieldValue ( null );
			    	}
	            }
			    else {
			        // Add as string
	                tablerec.addFieldValue( parseFile_ProcessString(cell) );
	            }
			}
			else {
			    // Set as the string value.
			    tablerec.addFieldValue( parseFile_ProcessString(cell) );
	        }
		}
		
		// If the specific record does not have enough columns, pad the columns at the end with blanks,
		// using blank strings or NaN for number fields.  This depends on whether headings were read.
		// Sometimes the header row has more columns than data rows, in particular because breakStringList()
		// will drop an empty field at the end.
		
		for ( int icol = cols; icol < table.getNumberOfFields(); icol++) {
		    if ( ColumnDataTypes_Auto_boolean ) {
		        // Add values based on the column type.
		        if ( tableFieldType[icol] == TableField.DATA_TYPE_STRING ) {
		            tablerec.addFieldValue( "" );
		        }
		        else {
                    tablerec.addFieldValue( null );
                }
		    }
		    else {
		        // Add a blank string.
		        tablerec.addFieldValue("");
		    }
		}
		
		try {
		    table.addRecord(tablerec);
		}
		catch ( Exception e ) {
		    Message.printWarning ( 3, routine, "Error adding row to table at included data row [" + irow +
		        "] (" + e + ")." );
		    ++errorCount;
		}
	}
	if ( errorCount > 0 ) {
	    // There were errors processing the data
	    String message = "There were " + errorCount + " errors processing the data.";
	    Message.printWarning ( 3, routine, message );
	    throw new Exception ( message );
	}

	return table;		
}

/**
Determine whether a line from the file matches the list of rows that are of interest.
@param linecount0
@param rows_List list of Integer objects that are row numbers (0+) of interest.
@param rows_List_size Size of rows_List - used to speed up performance.
@return true if the line matches an item in the list.
*/
private static boolean parseFile_LineMatchesLineFromList( int linecount0, List<Integer> rows_List, int rows_List_size )
{
    Integer int_object;
    if ( rows_List != null ) {
        rows_List_size = rows_List.size();
    }
    for ( int is = 0; is < rows_List_size; is++ ) {
        int_object = rows_List.get(is);
        if ( linecount0 == int_object.intValue() ) {
            // Skip the line as requested
            return true;
        }
    }
    return false;
}

/**
Parse a line that is known to be a header line to initialize the table fields.
All fields are set to type String, although this will be reset when data records are processed.
@param line Line to parse.
@param linecount0 Line number (0+).
@param TrimInput_Boolean Indicates whether input rows should be trimmed before parsing.
@param Delimiter The delimiter characters for parsing the line into tokens.
@param parse_flag the flag to be passed to StringUtil.breakStringList() when parsing the line.
@return A list of TableField describing the table columns.
*/
private static List<TableField> parseFile_ParseHeaderLine (
    String line, int linecount0, boolean TrimInput_Boolean, String Delimiter, int parse_flag )
{   String routine = "DataTable.parseFile_ParseHeaderLine";
    Message.printStatus ( 2, routine, "Adding column headers from line [" + linecount0 + "]: " + line );
    List<String> columns = null;
    if ( TrimInput_Boolean ) {
        columns = StringUtil.breakStringList( line.trim(), Delimiter, parse_flag );
    }
    else {
        columns = StringUtil.breakStringList(line, Delimiter, parse_flag );
    }
    
    int numFields = columns.size();
    List<TableField> tableFields = new ArrayList<TableField>();
    TableField tableField = null;
    String temp = null;
    for (int i = 0; i < numFields; i++) {
        temp = columns.get(i).trim();
        while (findPreviousFieldNameOccurances(tableFields, temp)) {
            temp = temp + "_2";
        }
        tableField = new TableField();
        tableField.setName(temp);
        // All table fields by default are treated as strings.
        tableField.setDataType(TableField.DATA_TYPE_STRING);
        tableFields.add(tableField);
    }
    return tableFields;
}

/**
Process a string table field value before setting as data in the table.
*/
private static String parseFile_ProcessString ( String cell )
{
    if ( (cell == null) || (cell.length() == 0) ) {
        return cell;
    }
    char c1 = cell.charAt(0);
    int len = cell.length();
    char c2 = cell.charAt(len - 1);
    if ( (c1 == '"') || (c1 == '\'') ) {
        // Have a quoted string.  Remove the quotes from each end (but not the middle)
        if ( (c2 == c1) && (len > 1) ) {
            return cell.substring(1,len - 1);
        }
        else {
            // Truncated field or error in input?  Unlikely case
            return cell.substring(1);
        }
        // TODO SAM 2012-05-01 How to deal with embedded quotes?
    }
    else {
        return cell;
    }
}

/**
Set the comments string list.
@param comments Comments to set.
*/
public void setComments ( List<String> comments )
{   if ( comments != null ) {
        __comments = comments;
    }
}

/**
Sets the value of a specific field. 
@param row the row (0+) in which to set the value.
@param col the column (0+) in which to set the value.
@param value the value to set.
@exception Exception if the field value cannot be set, including if the row does not exist.
*/
public void setFieldValue(int row, int col, Object value) 
throws Exception
{
    setFieldValue ( row, col, value, false );
}

/**
Sets the value of a specific field. 
@param row the row (0+) in which to set the value .
@param col the column (0+) in which to set the value.
@param value the value to set.
@param createIfNecessary if true and the requested row is not in the existing rows, create
intervening rows, initialize to missing (null objects), and then set the data.
*/
public void setFieldValue(int row, int col, Object value, boolean createIfNecessary ) 
throws Exception
{
    int nRows = getNumberOfRecords();
    if ( (row > (nRows - 1)) && createIfNecessary ) {
        // Create empty rows
        for ( int i = nRows; i <= row; i++ ) {
            addRecord(emptyRecord());
        }
    }
    // Now set the value (will throw ArrayIndexOutOfBoundsException if row is out of range)...
    TableRecord record = _table_records.get(row);
    record.setFieldValue(col, value);
}

/**
Sets the width of the field.
@param col the column for which to set the width.
@param width the width to set.
*/
public void setFieldWidth(int col, int width) 
throws Exception {
	TableField field = _table_fields.get(col);
	field.setWidth(width);
}

/**
Set the table identifier.
@param table_id Identifier for the table
*/
public void setTableID ( String table_id )
{
    __table_id = table_id;
}

/**
Set the number of records in the table.  This method should typically only be
called when data are read on-the-fly (and are not stored in memory in the table records).
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
	TableField tableField = _table_fields.get(index);
	tableField.setDataType ( data_type );
	tableField.setName ( name );
}

/**
Set the table fields to define the table.
@param tableFieldsList a list of TableField objects defining table contents.
*/
public void setTableFields ( List<TableField> tableFieldsList )
{	_table_fields = tableFieldsList;
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
	TableField tableField = _table_fields.get(index);
	tableField.setName ( name );
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
	TableField tableField = _table_fields.get(index);
	tableField.setDataType ( data_type );
}

/**
Set values in the table by first matching rows using column filters (default is match all) and
then setting values for specific columns.
@param table table to modify
@param columnFilters map to filter rows to set values in
@param columnValues map for columns values that will be set, where rows to be modified will be the result of the filters;
values are strings and need to be converged before setting, based on column type
@param createColumns indicates whether new columns should be created if necessary
*/
public void setTableValues ( Hashtable<String,String> columnFilters, HashMap<String,String> columnValues, boolean createColumns )
{   String routine = getClass().getName() + ".setTableValues";
    // List of columns that will be set, taken from keys in the column values
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    // Get filter columns and glob-style regular expressions
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobs = new String[columnFilters.size()];
    Enumeration keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = (String)keys.nextElement();
            columnNumbersToFilter[ikey] = getFieldIndex(key);
            columnFilterGlobs[ikey] = (String)columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation
            columnFilterGlobs[ikey] = columnFilterGlobs[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "Filter column \"" + key + "\" not found in table.");
        }
    }
    // Get the column numbers and values to to set
    String [] columnNamesToSet = new String[columnValues.size()];
    String [] columnValuesToSet = new String[columnValues.size()];
    int [] columnNumbersToSet = new int[columnValues.size()];
    int [] columnTypesToSet = new int[columnValues.size()];
    ikey = -1;
    for ( Map.Entry<String,String> pairs: columnValues.entrySet() ) {
        columnNumbersToSet[++ikey] = -1;
        try {
            columnNamesToSet[ikey] = (String)pairs.getKey();
            columnValuesToSet[ikey] = pairs.getValue();
            columnNumbersToSet[ikey] = getFieldIndex(columnNamesToSet[ikey]);
            columnTypesToSet[ikey] = getFieldDataType(columnNumbersToSet[ikey]);
            //Message.printStatus(2,routine,"Setting column \"" + columnNamesToSet[ikey] + " " + columnNumbersToSet[ikey] + "\"");
        }
        catch ( Exception e ) {
            // OK, will add the column below
        }
    }
    // If necessary, add columns to the table.  For now, always treat as strings
    // TODO SAM 2013-08-06 Evaluate how to handle other data types in set
    TableField newTableField;
    // Create requested columns in the output table
    for ( int icol = 0; icol < columnNumbersToSet.length; icol++ ) {
        if ( (columnNumbersToSet[icol] < 0) && createColumns ) {
            // Did not find the column in the table so add a String column for null values
            newTableField = new TableField(TableField.DATA_TYPE_STRING, columnNamesToSet[icol], -1, -1);
            columnNumbersToSet[icol] = addField(newTableField, null );
            columnTypesToSet[icol] = getFieldDataType(columnNumbersToSet[icol]);
        }
    }
    // Now loop through all the data records and set values if rows are matched
    int icol;
    boolean filterMatches;
    Object o = null;
    String s;
    for ( int irow = 0; irow < getNumberOfRecords(); irow++ ) {
        filterMatches = true;
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing set
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    filterMatches = false;
                    break;
                }
                try {
                    o = getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        filterMatches = false;
                        break; // Don't include nulls when checking values
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobs[icol]) ) {
                        // A filter did not match so don't process the record
                        filterMatches = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                    errorMessage.append("Error getting table data for [" + irow + "][" +
                        columnNumbersToFilter[icol] + "].");
                    Message.printWarning(3, routine, "Error getting table data for [" + irow + "][" +
                        columnNumbersToFilter[icol] + "] (" + e + ")." );
                    ++errorCount;
                }
            }
            //Message.printStatus(2,routine,"" + irow + " matches=" + filterMatches );
            if ( !filterMatches ) {
                // Skip the record.
                continue;
            }
        }
        for ( icol = 0; icol < columnNumbersToSet.length; icol++ ) {
            try {
                // OK if setting to null value, but hopefully should not happen
                // TODO SAM 2013-08-06 Handle all column types
                //Message.printStatus(2,routine,"Setting ColNum=" + columnNumbersToSet[icol] + " RowNum=" + irow + " value=" +
                //    columnValues.get(columnNamesToSet[icol]));
                if ( columnNumbersToSet[icol] >= 0 ) {
                    if ( columnTypesToSet[icol] == TableField.DATA_TYPE_INT ) {
                        // TODO SAM 2013-08-26 Should parse the values once rather than each time set to improve error handling and performance
                        setFieldValue(irow, columnNumbersToSet[icol], Integer.parseInt(columnValuesToSet[icol]), true );
                    }
                    else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_DOUBLE ) {
                        // TODO SAM 2013-08-26 Should parse the values once rather than each time set to improve error handling and performance
                        setFieldValue(irow, columnNumbersToSet[icol], Double.parseDouble(columnValuesToSet[icol]), true );
                    }
                    else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_STRING ) {
                        setFieldValue(irow, columnNumbersToSet[icol], columnValuesToSet[icol], true );
                    }
                    else {
                        errorMessage.append("Do not know how to set data type (" + TableColumnType.valueOf(columnTypesToSet[icol]) +
                            ") for column \"" + columnNamesToSet[icol] + "].");
                        ++errorCount;
                    }
                }
            }
            catch ( Exception e ) {
                // Should not happen
                errorMessage.append("Error setting table data [" + irow + "][" + columnNumbersToSet[icol] + "] (" + e + ").");
                Message.printWarning(3, routine, "Error setting new table data for [" + irow + "][" +
                    columnNumbersToSet[icol] + "] (" + e + ")." );
                Message.printWarning(3, routine, e);
                ++errorCount;
            }
        }
    }
    if ( errorCount > 0 ) {
        throw new RuntimeException ( "There were + " + errorCount + " errors setting table values: " + errorMessage );
    }
}

/**
Sort the table rows by sorting a column's values.
@param sortColumns the name of the columns to be sorted, allowed to be integer, double, string, or DateTime type.
@param sortOrder order to sort (specify as 0+ to sort ascending and < 0 to sort descending)
@return the sort order array indicating the position in the original data
(useful if a parallel sort of data needs to occur)
*/
public int [] sortTable ( String [] sortColumns, int [] sortOrder )
{	//String routine = getClass().getSimpleName() + ".sortTable";
    int [] sortColumnsNum = new int[sortColumns.length];
    List<String> errors = new ArrayList<String>();
    for ( int i = 0; i < sortColumns.length; i++ ) {
    	sortColumnsNum[i] = -1;
	    try {
	        sortColumnsNum[i] = getFieldIndex(sortColumns[i]);
	    }
	    catch ( Exception e ) {
	        errors.add(sortColumns[i]);
	    }
    }
    if ( errors.size() > 0 ) {
    	StringBuilder b = new StringBuilder("The following column(s) to sort were not found in table \"" + getTableID() + "\":");
    	for ( int i = 0; i < errors.size(); i++ ) {
    		if ( i > 0 ) {
    			b.append (",");
    		}
    		b.append(sortColumns[i]);
    	}
    	throw new RuntimeException ( b.toString() );
    }
    int nrecords = getNumberOfRecords();
    int sortFlag = StringUtil.SORT_ASCENDING;
    if ( sortOrder[0] < 0 ) {
        sortFlag = StringUtil.SORT_DESCENDING;
    }
    int [] sortedOrderArray = new int[nrecords]; // Overall sort order different from original
    // First sort by the first column.
    int iSort = 0;
    if ( getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_STRING ) {
        String value;
        List<String> values = new ArrayList<String>(nrecords);
        for ( TableRecord rec : getTableRecords() ) {
            try {
                value = rec.getFieldValueString(sortColumnsNum[iSort]);
                if ( value == null ) {
                    value = "";
                }
                else {
                    values.add(value);
                }
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        StringUtil.sortStringList(values, sortFlag, sortedOrderArray, true, true);
    }
    else if ( getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_DATETIME ) {
        Object value;
        double [] values = new double[nrecords];
        int irec = -1;
        for ( TableRecord rec : getTableRecords() ) {
            ++irec;
            try {
                value = rec.getFieldValue(sortColumnsNum[iSort]);
                if ( value == null ) {
                    value = -Double.MAX_VALUE;
                }
                values[irec] = ((DateTime)value).toDouble();
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortedOrderArray, true);
    }
    else if ( (getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_DOUBLE) ||
    	(getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_FLOAT) ) {
    	Object o;
        double value;
        double [] values = new double[nrecords];
        int irec = -1;
        for ( TableRecord rec : getTableRecords() ) {
            ++irec;
            try {
                o = rec.getFieldValue(sortColumnsNum[iSort]);
                if ( o == null ) {
                    value = -Double.MAX_VALUE;
                }
                else {
                	if ( o instanceof Double ) {
                		value = (Double)o;
                	}
                	else {
                		value = (Float)o;
                	}
                }
                values[irec] = value;
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortedOrderArray, true);
    }
    else if ( getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_INT) {
        Integer value;
        int [] values = new int[nrecords];
        int irec = -1;
        for ( TableRecord rec : getTableRecords() ) {
            ++irec;
            try {
                value = (Integer)rec.getFieldValue(sortColumnsNum[iSort]);
                if ( value == null ) {
                    value = -Integer.MAX_VALUE;
                }
                values[irec] = value;
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortedOrderArray, true);
    }
    else {
        throw new RuntimeException ( "Sorting table only implemented for string, integer, double, float and DateTime columns." );
    }
    // Shuffle the table's row list according to sortOrder.  Because other objects may have references to
    // the tables record list, can't create a new list.  Therefore, copy the old list to a backup and then use
    // that to sort into an updated original list.
    List<TableRecord> backup = new ArrayList<TableRecord>(nrecords);
    List<TableRecord> records = this.getTableRecords();
    for ( TableRecord rec : records ) {
        backup.add ( rec );
    }
    // Now set from the backup to the original list
    for ( int irec = 0; irec < nrecords; irec++ ) {
        records.set(irec, backup.get(sortedOrderArray[irec]) );
    }
    // Now sort by columns [1]+ (zero index).  Only sort the last column being iterated.
    // The previous columns are used to find blocks of rows to sort.  In other words, if 3 columns are sorted
    // then columns [0-1] must match and then that block of rows is sorted based on column [2].
    int iSort2;
    int lastRec = getNumberOfRecords() - 1;
    for ( iSort = 1; iSort < sortColumnsNum.length; iSort++ ) {
    	Object [] sortValuesPrev = null;
    	int irec = -1;
    	boolean needToSort = false;
    	Object o2;
    	int blockStartRow = 0, blockEndRow = 0, sortColumnMatchCount = 0;
    	// Iterate through the table.
        for ( TableRecord rec : getTableRecords() ) {
            ++irec;
            //Message.printStatus(2,routine,"Processing record " + irec );
            // Check the current row's sort columns against the previous row
            if ( sortValuesPrev == null ) {
            	// Initialize this row with values to be compared with the next row
            	sortValuesPrev = new Object[iSort];
            	for ( iSort2 = 0; iSort2 < iSort; iSort2++ ) {
            		try {
            			sortValuesPrev[iSort2] = rec.getFieldValue(sortColumnsNum[iSort2]);
            		}
            		catch ( Exception e ) {
            			throw new RuntimeException ( e );
            		}
            	}
            	blockStartRow = irec;
            	blockEndRow = irec;
            	//Message.printStatus(2,routine,"Initializing " + irec + " for first comparison." );
            	continue;
            }
            else {
            	// Compare this row's values with the previous block of similar values
            	sortColumnMatchCount = 0;
	            for ( iSort2 = 0; iSort2 < iSort; iSort2++ ) {
	            	try {
	            		o2 = rec.getFieldValue(sortColumnsNum[iSort2]);
	            	}
	            	catch ( Exception e ) {
	            		throw new RuntimeException ( e );
	            	}
	            	if ( ((o2 == null) && (sortValuesPrev[iSort2] == null)) || (o2 != null) && o2.equals(sortValuesPrev[iSort2]) ) {
	            		//Message.printStatus(2, routine, "Previous value["+iSort2+"]: " + sortValuesPrev[iSort2] + " current value=" + o2 );
	            		++sortColumnMatchCount;
	            	}
	            	else {
	            		// The current row did not match so save the current row as the previous and break to indicate that the block needs sorted
	            		//Message.printStatus(2,routine,"Record " + irec + " compare values did not match previous row." );
	            		break;
	            	}
	            }
	            // If all the values matched, can process another row before sorting, but check to see if at end of table below
	            if ( sortColumnMatchCount == iSort ) {
	            	//Message.printStatus(2,routine,"Record " + irec + " sort columns match previous." );
	            	needToSort = false;
	            	blockEndRow = irec; // Advance the end of the block
	            }
	            else {
	            	// Current row's sort column values did not match so need to sort the block
	            	//Message.printStatus(2,routine,"Record " + irec + " sort columns do not match previous.  Resetting \"previous\" values to this record." );
	            	needToSort = true;
	            	// Save the current row to compare with the next row
            		for ( int iSort3 = 0; iSort3 < iSort; iSort3++ ) {
                		try {
                			sortValuesPrev[iSort3] = rec.getFieldValue(sortColumnsNum[iSort3]);
                		}
                		catch ( Exception e ) {
                			throw new RuntimeException ( e );
                		}
                	}
	            }
	            if ( (irec == lastRec) && (blockStartRow != blockEndRow) ) {
	            	// Need to sort if in the last row unless the block was only one row
	            	needToSort = true;
	            	//Message.printStatus(2, routine, "Need to sort end of table from " + blockStartRow + " to " + blockEndRow );
	            }
	            if ( needToSort ) {
	            	// Need to sort the block of rows using the "rightmost" sort column
	            	//Message.printStatus(2, routine, "Need to sort block of rows from " + blockStartRow + " to " + blockEndRow );
	            	try {
	            		//Message.printStatus(2, routine, "Sorting rows from " + blockStartRow + " to " + blockEndRow + " based on column " + sortColumnNum[iSort] );
	            		sortTableSubset(blockStartRow,blockEndRow,sortColumnsNum[iSort],sortOrder[iSort],sortedOrderArray);
	            	}
	            	catch ( Exception e ) {
	            		throw new RuntimeException(e);
	            	}
	            	// Now that the block has been started, reset for the next block
	            	// blockStartRow should = irec since rec was different and triggered the sort of the previous block
	            	blockStartRow = blockEndRow + 1;
	            	blockEndRow = blockStartRow;
	            }
	            //Message.printStatus(2, routine, "At end of loop irec=" + irec + " blockStartRow=" + blockStartRow + " blockEndRow=" + blockEndRow );
            }
        }
    }
    return sortedOrderArray;
}

/**
Sort a subset of a table.  This is called internally by other methods.
@param blockStartRow starting row (0 index) to sort
@param blockEndRow ending row (0 index) to sort
@param iCol column number to sort
@param sortOrder sort order
@param sortedOrderArray the sort order array indicating the position in the original data
(useful if a parallel sort of data needs to occur)
*/
private void sortTableSubset ( int blockStartRow, int blockEndRow, int iCol, int sortOrder, int [] sortedOrderArray )
throws Exception
{
	if ( blockStartRow == blockEndRow ) {
		// Only one row to sort
		return;
	}
    int nrecords = blockEndRow - blockStartRow + 1; // Number of records in the block to sort
    int [] sortOrderArray2 = new int[nrecords]; // Overall sort order different from original
    // First sort by the first column.
    int sortFlag = StringUtil.SORT_ASCENDING;
    if ( sortOrder < 0 ) {
        sortFlag = StringUtil.SORT_DESCENDING;
    }
    if ( getFieldDataType(iCol) == TableField.DATA_TYPE_STRING ) {
        String value;
        List<String> values = new ArrayList<String>(nrecords);
        TableRecord rec;
        for ( int irec = blockStartRow; irec <= blockEndRow; irec++ ) {
        	rec = getRecord(irec);
            try {
                value = rec.getFieldValueString(iCol);
                if ( value == null ) {
                    value = "";
                }
                else {
                    values.add(value);
                }
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        StringUtil.sortStringList(values, sortFlag, sortOrderArray2, true, true);
    }
    else if ( getFieldDataType(iCol) == TableField.DATA_TYPE_DATETIME ) {
        Object value;
        double [] values = new double[nrecords];
        TableRecord rec;
        for ( int irec = blockStartRow, pos = 0; irec <= blockEndRow; irec++, pos++ ) {
        	rec = getRecord(irec);
            try {
                value = rec.getFieldValue(iCol);
                if ( value == null ) {
                    value = -Double.MAX_VALUE;
                }
                values[pos] = ((DateTime)value).toDouble();
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else if ( (getFieldDataType(iCol) == TableField.DATA_TYPE_DOUBLE) ||
    	(getFieldDataType(iCol) == TableField.DATA_TYPE_FLOAT) ) {
    	Object o;
        double value;
        double [] values = new double[nrecords];
        TableRecord rec;
        for ( int irec = blockStartRow, pos = 0; irec <= blockEndRow; irec++, pos++ ) {
        	rec = getRecord(irec);
            try {
                o = rec.getFieldValue(iCol);
                if ( o == null ) {
                    value = -Double.MAX_VALUE;
                }
                else {
                	if ( o instanceof Double ) {
                		value = (Double)o;
                	}
                	else {
                		value = (Float)o;
                	}
                }
                values[pos] = value;
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else if ( getFieldDataType(iCol) == TableField.DATA_TYPE_INT) {
        Integer value;
        int [] values = new int[nrecords];
        TableRecord rec;
        for ( int irec = blockStartRow, pos = 0; irec <= blockEndRow; irec++, pos++ ) {
        	rec = getRecord(irec);
            try {
                value = (Integer)rec.getFieldValue(iCol);
                if ( value == null ) {
                    value = -Integer.MAX_VALUE;
                }
                values[pos] = value;
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else {
        throw new RuntimeException ( "Sorting table only implemented for string, integer, double, float and DateTime columns." );
    }
    // Shuffle the table's row list according to sortOrder.  Because other objects may have references to
    // the tables record list, can't create a new list.  Therefore, copy the old list to a backup and then use
    // that to sort into an updated original list.
    List<TableRecord> backup = new ArrayList<TableRecord>(nrecords);
    List<TableRecord> records = this.getTableRecords();
    TableRecord rec;
    for ( int irec = blockStartRow; irec <= blockEndRow; irec++ ) {
    	rec = getRecord(irec);
        backup.add ( rec );
    }
    // Now set from the backup to the original list
    for ( int irec = blockStartRow; irec <= blockEndRow; irec++ ) {
        records.set(irec, backup.get(sortOrderArray2[irec-blockStartRow]) );
        sortedOrderArray[irec] = sortOrderArray2[irec-blockStartRow];
    }
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

// TODO SAM 2006-06-21
// Need to check for delimiter in header and make this code consistent with
// the RTi.Util.GUI.JWorksheet file saving code, or refactor to use the same code.
/**
Writes a table to a delimited file.  If the data items contain the delimiter,
they will be written surrounded by double quotes.
@param filename the file to write
@param delimiter the delimiter between columns
@param writeColumnNames If true, the field names will be read from the fields 
and written as a one-line header of field names.  The headers are double-quoted.
If all headers are missing, then the header line will not be written.
@param comments a list of Strings to put at the top of the file as comments, 
@param commentLinePrefix prefix string for comment lines specify if incoming comment strings have not already been
prefixed.
@param alwaysQuoteStrings if true, then always surround strings with double quotes; if false strings will only
be quoted when they include the delimiter
@param newlineReplacement if not null, replace newlines in string table values with the replacement string
(which can be an empty string).  This is needed to ensure that the delimited file does not include unexpected
newlines in mid-row.  Checks are done for \r\n, then \n, then \r to catch all combinations.  This can be a
performance hit and mask data issues so the default is to NOT replace newlines.
@param NaNValue value to replace NaN in output (a value of null will result in NaN being written).
*/
public void writeDelimitedFile(String filename, String delimiter, boolean writeColumnNames, List<String> comments,
    String commentLinePrefix, boolean alwaysQuoteStrings, String newlineReplacement, String NaNValue ) 
throws Exception {
	String routine = "DataTable.writeDelimitedFile";
	
	if (filename == null) {
		Message.printWarning(1, routine, "Cannot write to file '" + filename + "'");
		throw new Exception("Cannot write to file '" + filename + "'");
	}
	if ( comments == null ) {
	    comments = new ArrayList<String>(); // To simplify logic below
	}
	String commentLinePrefix2 = commentLinePrefix;
	if ( !commentLinePrefix.equals("") ) {
	    commentLinePrefix2 = commentLinePrefix + " "; // Add space for readability
	}
	if ( NaNValue == null ) {
	    NaNValue = "NaN";
	}

	PrintWriter out = new PrintWriter( new BufferedWriter(new FileWriter(filename)));
	int row = 0, col = 0;
	try {
    	// If any comments have been passed in, print them at the top of the file
    	if (comments != null && comments.size() > 0) {
    		int size = comments.size();
    		for (int i = 0; i < size; i++) {
    			out.println(commentLinePrefix2 + comments.get(i) );
    		}
    	}
    
    	int cols = getNumberOfFields();
    	if (cols == 0) {
    		Message.printWarning(3, routine, "Table has 0 columns!  Nothing will be written.");
    		return;
    	}
    
    	StringBuffer line = new StringBuffer();
    
        int nonBlank = 0; // Number of non-blank table headings
    	if (writeColumnNames) {
    	    // First determine if any headers are non blank
            for ( col = 0; col < cols; col++) {
                if ( getFieldName(col).length() > 0 ) {
                    ++nonBlank;
                }
            }
            if ( nonBlank > 0 ) {
        		line.setLength(0);
        		for ( col = 0; col < (cols - 1); col++) {
        			line.append( "\"" + getFieldName(col) + "\"" + delimiter);
        		}
        		line.append( "\"" + getFieldName((cols - 1)) + "\"");
        		out.println(line);
            }
    	}
    	
    	int rows = getNumberOfRecords();
    	String cell;
    	int tableFieldType;
    	int precision;
    	Object fieldValue;
    	Double fieldValueDouble;
    	Float fieldValueFloat;
    	for ( row = 0; row < rows; row++) {
    		line.setLength(0);
    		for ( col = 0; col < cols; col++) {
    		    if ( col > 0 ) {
    		        line.append ( delimiter );
    		    }
    		    tableFieldType = getFieldDataType(col);
    		    precision = getFieldPrecision(col);
    		    fieldValue = getFieldValue(row,col);
    		    if ( fieldValue == null ) {
    		        cell = "";
    		    }
    		    else if ( tableFieldType == TableField.DATA_TYPE_FLOAT ) {
                    fieldValueFloat = (Float)fieldValue;
                    if ( fieldValueFloat.isNaN() ) {
                        cell = NaNValue;
                    }
                    else if ( precision > 0 ) {
                        // Format according to the precision if floating point
                        cell = StringUtil.formatString(fieldValue,"%." + precision + "f");
                    }
                    else {
                        // Use default formatting.
                        cell = "" + fieldValue;
                    }
    		    }
    		    else if ( tableFieldType == TableField.DATA_TYPE_DOUBLE ) {
    		        fieldValueDouble = (Double)fieldValue;
    		        if ( fieldValueDouble.isNaN() ) {
    		            cell = NaNValue;
    		        }
    		        else if ( precision > 0 ) {
                        // Format according to the precision if floating point
                        cell = StringUtil.formatString(fieldValue,"%." + precision + "f");
    		        }
    		        else {
    		            // Use default formatting.
                        cell = "" + fieldValue;
    		        }
                }
                else {
                    // Use default formatting.
                    cell = "" + fieldValue;
                }
    			// Surround the values with double quotes if:
    		    // 1) the field contains the delimiter
    		    // 2) alwaysQuoteStrings=true
    			if ( (cell.indexOf(delimiter) > -1) ||
    			    ((tableFieldType == TableField.DATA_TYPE_STRING) && alwaysQuoteStrings) ) {
    				cell = "\"" + cell + "\"";
    			}
    			if ( (tableFieldType == TableField.DATA_TYPE_STRING) && (newlineReplacement != null) ) {
    			    // Replace newline strings with the specified string
    			    cell = cell.replace("\r\n", newlineReplacement); // Windows/Mac use 2-characters
    			    cell = cell.replace("\n", newlineReplacement); // *NIX
    			    cell = cell.replace("\r", newlineReplacement); // to be sure
    			}
    			line.append ( cell );
    		}
    		out.println(line);
    	}
	}
	catch ( Exception e ) {
	    // Log and rethrow
	    Message.printWarning(3, routine, "Unexpected error writing delimited file row [" + row + "][" + col +
	        "] (" + e + ")." );
	    Message.printWarning(3, routine, e);
	    throw ( e );
	}
	finally {
    	out.flush();
    	out.close();
	}
}
}