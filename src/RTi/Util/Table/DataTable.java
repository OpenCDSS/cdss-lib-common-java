// DataTable - class to hold tabular data from a database

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

import RTi.DMI.DMIUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

// TODO SAM 2010-12-16 Evaluate using a different package for in-memory tables,
// such as from SQLite, H2 or other embedded database.
/**
This class contains records of data as a table, using a list of TableRecord instances.
The format of the table is defined using the TableField class.
Tables can be used to store record-based data, for common data types.
Handling additional data types can be added in the future.
An example of a DataTable instantiation is:
<p>

<pre>
try {
	/// First, create define the table by assembling a list of TableField objects.
	List<TableField> myTableFields = new ArrayList<>(3);
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_STRING, "id_label_6", 12 ) );
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_INT, "Basin", 12 ) );
	myTableFields.add ( new TableField ( TableField.DATA_TYPE_STRING, "aka", 12 ) );

	// Now define table with one simple call.
	DataTable myTable = new DataTable ( myTableFields );

	// Now define a record to be included in the table.
	TableRecord contents = new TableRecord (3);
	contents.addFieldValue ( "123456" );
	contents.addFieldValue ( Integer.valueOf (6));
	contents.addFieldValue ( "Station ID" );

	myTable.addRecord ( contents );

	// Get the 2nd field from the first record (fields and records are zero-index based).
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
List of comments for the table.
For example, an analysis that creates a table of results may need explanatory comments corresponding to column headings.
The comments can be output when the table is written to a file.
*/
private List<String> __comments = new ArrayList<>();

/**
Number of records in the table (kept for case where records are not in memory).
*/
protected int _num_records = 0;

/**
Indicates if data records have been read into memory.
This can be reset by derived classes that instead keep open a binary database file (e.g., dBase)
and override the read/write methods.
*/
protected boolean _haveDataInMemory = true;

/**
Indicates whether string data should be trimmed on retrieval.
In general, this should be true because older databases like Dbase pad data with spaces but seldom
are spaces actually actual data values.
*/
protected boolean _trim_strings = true;

/**
Indicates whether addRecord() has been called.
If so, assume that the data records are in memory for calls to getNumberOfRecords().
Otherwise, just return the _num_records value.
*/
protected boolean _add_record_called = false;

/**
Construct a new table.  Use setTableFields() at a later time to define the table.
*/
public DataTable () {
	// Estimate that 100 is a good increment for the data list.
	initialize ( new ArrayList<>(), 10, 100 );
}

/**
Construct a new table.  The list of TableRecord will increment in size by 100.
@param tableFieldsList a list of TableField objects defining table contents.
*/
public DataTable ( List<TableField> tableFieldsList ) {
	// Estimate that 100 is a good increment for the data list.
	initialize ( tableFieldsList, 10, 100 );
}

/**
Construct a new table.
@param tableFieldsList a list of TableField objects defining table contents.
@param listSize Initial list size for the list holding records.
This can be used to optimize performance.
@param listIncrement Increment for the list holding records.
This can be used to optimize performance.
*/
public DataTable ( List<TableField> tableFieldsList, int listSize, int listIncrement ) {
	initialize ( tableFieldsList, listSize, listIncrement );
}

/**
Add a String to the comments associated with the time series (e.g., station remarks).
@param comment Comment string to add.
*/
public void addToComments( String comment ) {
    if ( comment != null ) {
        __comments.add ( comment );
    }
}

/**
Add a list of String to the comments associated with the time series (e.g., station remarks).
@param comments Comments strings to add.
*/
public void addToComments( List<String> comments ) {
    if ( comments == null ) {
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
@param record record to add
@return the new record (allows command chaining)
@exception Exception when the number of fields in new_record is not equal to the
number of fields in the current TableField declaration.
*/
public TableRecord addRecord ( TableRecord record )
throws Exception {
	int num_table_fields = _table_fields.size();
	int num_new_record_fields = record.getNumberOfFields();
	_add_record_called = true;
	if ( num_new_record_fields == num_table_fields ) {
		_table_records.add ( record );
		return record;
	}
	else {
        throw new Exception ( "Number of fields in the new record (" +
		num_new_record_fields + ") does not match current description of the table fields (" + num_table_fields + ")." );
	}
}

/**
Add a field to the right-most end of table and each entry in the existing TableRecords.
The added fields are initialized with blank strings or NaN, as appropriate.
@param tableField information about field to add.
@param initValue the initial value to set for all the existing rows in the table (can be null).
@return the field index (0+).
*/
public int addField ( TableField tableField, Object initValue ) {
    return addField ( -1, tableField, initValue );
}

/**
Add a field to the table and each entry in TableRecord.
The field is added at the specified insert position.
The added fields are initialized with blank strings or NaN, as appropriate.
@param insertPos the column (0+) at which to add the column (-1 or >= the number of existing columns to insert at the end).
@param tableField information about field to add.
@param initValue the initial value to set for all the existing rows in the table (can be null).
@return the field index (0+).
*/
public int addField ( int insertPos, TableField tableField, Object initValue ) {
	return addField ( insertPos, tableField, initValue, null );
}

/**
Add a field to the table and each entry in TableRecord.  The field is added at the specified insert position.
The added fields are initialized with blank strings or NaN, as appropriate.
@param insertPos the column (0+) at which to add the column (-1 or >= the number of existing columns to insert at the end).
@param tableField information about field to add.
@param initValue the initial value to set for all the existing rows in the table (can be null).
@param initFunction the initial function used to set initial values for all the existing rows in the table (can be null).
@return the field index (0+).
*/
public int addField ( int insertPos, TableField tableField, Object initValue, DataTableFunctionType initFunction ) {
	String routine = getClass().getSimpleName() + ".addField";
	boolean addAtEnd = false;
    if ( (insertPos < 0) || (insertPos >= _table_fields.size()) ) {
        // Add at the end.
    	if ( Message.isDebugOn ) {
    		Message.printDebug(1, routine, "Adding table field \"" + tableField.getName() + "\" at end index=" + getNumberOfFields());
    	}
        _table_fields.add ( tableField );
        addAtEnd = true;
    }
    else {
        // Insert at the specified column location.
    	if ( Message.isDebugOn ) {
    		Message.printDebug(1, routine, "Adding table field \"" + tableField.getName() + "\" at index=" + insertPos);
    	}
        _table_fields.add(insertPos,tableField);
    }
    // Add value to each record in the table to be consistent with the field data.
	int num = _table_records.size();
	TableRecord tableRecord;
	for ( int i=0; i<num; i++ ) {
		tableRecord = _table_records.get(i);
		// Calculate the initial value if a function.
		// Add element and set to specified initial value.
		// These are ordered in the most likely types to optimize.
		// TODO SAM 2014-05-04 Why are these broken out separately?
		int dataType = tableField.getDataType();
		if ( dataType == TableField.DATA_TYPE_STRING ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = "" + (i + 1);
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = "" + i;
				}
			}
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
		    else {
		        tableRecord.addFieldValue( insertPos, initValue );
		    }
		}
		else if ( dataType == TableField.DATA_TYPE_INT ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = Integer.valueOf(i + 1);
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = Integer.valueOf(i);
				}
			}
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_DOUBLE ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = Double.valueOf(i + 1);
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = Double.valueOf(i);
				}
			}
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_SHORT ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = Short.valueOf((short)(i + 1));
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = Short.valueOf((short)(i));
				}
			}
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
		else if ( dataType == TableField.DATA_TYPE_FLOAT ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = Float.valueOf(i + 1);
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = Float.valueOf(i);
				}
			}
		    if ( addAtEnd ) {
		        tableRecord.addFieldValue( initValue );
		    }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
		}
        else if ( dataType == TableField.DATA_TYPE_LONG ) {
			if ( initFunction != null ) {
				if ( initFunction == DataTableFunctionType.ROW ) {
					initValue = Long.valueOf(i + 1);
				}
				else if ( initFunction == DataTableFunctionType.ROW0 ) {
					initValue = Long.valueOf(i);
				}
			}
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
        else if ( dataType == TableField.DATA_TYPE_DATE ) {
        	// Function not relevant.
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
        else if ( dataType == TableField.DATA_TYPE_DATETIME ) {
        	// Function not relevant.
            if ( addAtEnd ) {
                tableRecord.addFieldValue( initValue );
            }
            else {
                tableRecord.addFieldValue( insertPos, initValue );
            }
        }
	}
	if ( addAtEnd ) {
	    return getNumberOfFields() - 1; // Zero offset index (0+).
	}
	else {
	    return insertPos;
	}
}

/**
Append one table to another.
@param table original table
@param appendTable table that is being appended to the original table
@param reqIncludeColumns requested columns in append table to include or null to include all
@param columnMap map to rename original columns in the append table to new name (to match original table that is receiving data)
@param columnData map to set additional data for appended records,
the strings are converted to a type for the data column
@param columnFilters map for columns that will apply a filter in the append table, to limit rows being appended
@param appendRowNumbers an array of row numbers (1+) to append,
which is considered in addition to the other filters, negative row positions are ignored
@return the number of rows appended
*/
public int appendTable ( DataTable table, DataTable appendTable, String [] reqIncludeColumns,
    Hashtable<String,String> columnMap, Hashtable<String,String> columnData,
    Hashtable<String,String> columnFilters,
    int [] appendRowNumbers ) {
    String routine = getClass().getSimpleName() + ".appendTable";
    // List of columns that will be appended.
    String [] columnNamesToAppend = null;
    String [] firstTableColumnNames = table.getFieldNames();
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Append only the requested names from the append table.
        columnNamesToAppend = reqIncludeColumns;
    }
    else {
        // Append all columns from the append table.
        columnNamesToAppend = appendTable.getFieldNames();
    }
    // Column numbers in the append table to align with the original table.  Any values set to -1 will result in null.
    int [] firstTableColumnNumbersInAppendTable = new int[table.getNumberOfFields()];
    String [] appendTableColumnNamesOriginal = appendTable.getFieldNames();  // Columns from append table.
    String [] appendTableColumnNamesAfterMapping = appendTable.getFieldNames(); // Columns from append table after mapping, set below.
    // Replace the append table names using the column map.
    String columnMapped;
    for ( int icol = 0; icol < appendTableColumnNamesAfterMapping.length; icol++ ) {
    	// If column map is provided, use it.
        if ( columnMap != null ) {
        	// Append column table output name is looked up from append table column original name.
            columnMapped = columnMap.get(appendTableColumnNamesAfterMapping[icol]);
            if ( columnMapped != null ) {
                // Reset the append column name with the new name, which should match a column name in the first table.
                appendTableColumnNamesAfterMapping[icol] = columnMapped;
            }
        }
    }
    // Loop through the columns in the original table and match the column numbers in the append table.
    boolean appendColumnFound = false;
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    for ( int icol = 0; icol < firstTableColumnNumbersInAppendTable.length; icol++ ) {
        firstTableColumnNumbersInAppendTable[icol] = -1; // No match between first and append table, will result in null in original table.
        // Check each of the column names in the original table to match whether appending from the append table:
        // - appendTableColumnNamesAfterMapping are names in the original table, achieved through mapping
        // The append table column names will have been mapped to the first table above.
        for ( int i = 0; i < appendTableColumnNamesAfterMapping.length; i++ ) {
            // First check to see if the column name should be appended by whether append table and original table columns map to same column name.
            appendColumnFound = false;
            for ( int j = 0; j < columnNamesToAppend.length; j++ ) {
            	// columnNamesToAppend
                if ( columnNamesToAppend[j].equalsIgnoreCase(appendTableColumnNamesOriginal[i]) ) {
                	// Found a requested column to append in the append table (using original column names).
                    appendColumnFound = true;
                    break;
                }
            }
            if ( !appendColumnFound ) {
            	//Message.printStatus(2, routine, "Column \"" + appendTableColumnNamesOriginal[i] +
            		//"\" was not requested to append/copy to first table.");
                continue;
            }
            if ( firstTableColumnNames[icol].equalsIgnoreCase(appendTableColumnNamesAfterMapping[i]) ) {
            	// Have a matching column in first (original) table and the append table:
            	// - this is after append table column names have been mapped (renamed)
            	Message.printStatus(2, routine, "Original table column [" + icol + "] \"" +
            		firstTableColumnNames[icol] + "\" maps to append table column [" + i + "] \"" +
            			appendTableColumnNamesAfterMapping[i] + "\" (before mapping: \"" +
            		    appendTableColumnNamesOriginal[i] + "\")");
                firstTableColumnNumbersInAppendTable[icol] = i;
                break;
            }
        }
    }
    int [] tableColumnTypes = table.getFieldDataTypes(); // Original table column types.
    int [] appendTableColumnTypes = appendTable.getFieldDataTypes(); // Append column types, lined up with original table.
    // Get filter columns and glob-style regular expressions.
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobRegex = new String[columnFilters.size()];
    Enumeration<String> keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = keys.nextElement();
            columnNumbersToFilter[ikey] = appendTable.getFieldIndex(key);
            columnFilterGlobRegex[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation.
            columnFilterGlobRegex[ikey] = columnFilterGlobRegex[ikey].replace("*", ".*").toUpperCase();
            Message.printStatus(2, routine, "Filtering column [" + columnNumbersToFilter[ikey] + "] \"" +
                key + "\" to match \"" + columnFilterGlobRegex[ikey] + "\"");
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append("\n");
            }
            errorMessage.append ( "Filter column \"" + key + "\" not found in table \"" + appendTable.getTableID() + "\".");
        }
    }
    // If extra column data were provided, parse here so don't have to parse each time added:
    // - these are values for the original table columns that are not provided by append table
    Object [] columnDataParsedValues = new Object[columnData.size()];
    int iColumnData = -1;
	for ( Map.Entry<String,String> entry : columnData.entrySet() ) {
		++iColumnData;
		String column = entry.getKey();
		String value = entry.getValue();
		// Get the column.
		try {
			int iColumn = getFieldIndex(column);
			int colType = getFieldDataType(iColumn);
        	if ( colType == TableField.DATA_TYPE_STRING ) {
        		columnDataParsedValues[iColumnData] = value;
        	}
        	else if ( colType == TableField.DATA_TYPE_DOUBLE ) {
        		Double value_double = Double.valueOf(value);
        		columnDataParsedValues[iColumnData] = value_double;
        	}
        	else if ( colType == TableField.DATA_TYPE_INT ) {
        		Integer value_int = Integer.valueOf(value);
        		columnDataParsedValues[iColumnData] = value_int;
        	}
        	else if ( colType == TableField.DATA_TYPE_LONG ) {
        		Long value_long = Long.valueOf(value);
        		columnDataParsedValues[iColumnData] = value_long;
        	}
       	}
       	catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append("\n");
            }
            errorMessage.append ( "Error parsing column data ( " + value + ") - will use null" );
      		columnDataParsedValues[iColumnData] = null;
       	}
    }
    // Loop through all the data records and append records to the table.
    int icol;
    int irowAppended = 0;
    boolean somethingAppended = false;
    boolean filterMatches;
    String s;
    TableRecord rec;
    // Row count 1+, used when checking row numbers.
    int irow1 = 0;
    if ( (appendRowNumbers != null) && (appendRowNumbers.length > 0) ) {
    	for ( int appendRowNumber : appendRowNumbers ) {
   			Message.printStatus(2, routine, "Will append row number: " + appendRowNumber);
    	}
   	}
    for ( int irow = 0; irow < appendTable.getNumberOfRecords(); irow++ ) {
        somethingAppended = false; // Nothing appended so don't process record below.
        filterMatches = true; // Meaning, include the record, true by default if no filters.
        // Check the row numbers to include first since an easy check.
        if ( (appendRowNumbers != null) && (appendRowNumbers.length > 0) ) {
        	// Append only matching rows.
        	irow1 = irow + 1;
        	boolean doAppendRow = false;
        	for ( int appendRowNumber : appendRowNumbers ) {
        		if ( appendRowNumber == irow1 ) {
        			doAppendRow = true;
        			break;
        		}
        	}
        	if ( ! doAppendRow ) {
        		// The current row does not match a specific row number to append.
        		continue;
        	}
        }
        // Check filters.
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing append.
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                	// Column for filter was not found so cannot apply the filter.
                    filterMatches = false;
                    Message.printStatus(2, routine, "Column name to filter could not be determined." );
                    break;
                }
                try {
                    Object o = appendTable.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        filterMatches = false;
                        // Message.printStatus(2, routine, "Object is null, cannot check against filter \"" + columnFilterGlobRegex[icol] + "\"");
                        break; // Don't include nulls when checking values.
                    }
                    s = ("" + o).toUpperCase();
                    // Message.printStatus(2, routine, "Checking \"" + s + "\" against filter \"" + columnFilterGlobRegex[icol] + "\"");
                    if ( !s.matches(columnFilterGlobRegex[icol]) ) {
                        // A filter did not match so don't copy the record.
                        filterMatches = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                	if ( errorMessage.length() > 0 ) {
                    	errorMessage.append("\n");
                	}
                    errorMessage.append("Error getting append table data [" + irow + "][" +
                        columnNumbersToFilter[icol] + "] for filter.");
                    Message.printWarning(3, routine, "Error getting append table data for [" + irow + "][" +
                        columnNumbersToFilter[icol] + "] for filter (" + e + ")." );
                    ++errorCount;
                }
            } // End checking column filters.
            if ( !filterMatches ) {
            	// One or more filters were provided and did not match the record so skip the record:
            	// - the following generates a lot of output and can fill up the disk - disable for production code
            	//Message.printStatus(2, routine, "Row " + (irow + 1) + " did not match filter...not appending row.");
                continue;
            }
        }
        // Loop through columns in the original table and set values from the append table:
        // - the number of columns will match the first table
        // Create a record and add values for each column extracted from the append table.
        rec = new TableRecord();
        for ( icol = 0; icol < firstTableColumnNumbersInAppendTable.length; icol++ ) {
            try {
                if ( firstTableColumnNumbersInAppendTable[icol] < 0 ) {
                    // Column in first table was not matched in the append table so set to null.
                    rec.addFieldValue(null);
                }
                else {
                    // Set the value in the original table, if the type matches.
                    if ( tableColumnTypes[icol] == appendTableColumnTypes[firstTableColumnNumbersInAppendTable[icol]] ) {
                        rec.addFieldValue(appendTable.getFieldValue(irow, firstTableColumnNumbersInAppendTable[icol]));
                    }
                    else {
                    	// Types did not match so set to null:
                    	// - TODO smalers 2019-11-05 add intelligent casting
                    	Message.printWarning(3, routine, "Column types do not match for column \"" +
                    		firstTableColumnNames[icol] + "\" - using null value." );
                    	++errorCount;
                        rec.addFieldValue(null);
                    }
                }
                somethingAppended = true; // Checked below to ensure that empty TableRecord is not added.
            }
            catch ( Exception e ) {
                // Should not happen.
               	if ( errorMessage.length() > 0 ) {
                   	errorMessage.append("\n");
               	}
                errorMessage.append("Error appending [" + irow + "][" + firstTableColumnNumbersInAppendTable[icol] + "].");
                Message.printWarning(3, routine, "Error setting/appending [" + irow + "][" +
                    firstTableColumnNumbersInAppendTable[icol] + "] (" + e + ")." );
                ++errorCount;
            }
        }
        if ( somethingAppended ) {
        	// A record was determined from the append table that contained some data to append.
            // Set the record in the original table.
        	// First see if additional data should be set, as provided by column data.
        	if ( columnData != null ) {
        		// Iterate through the hash.
        		iColumnData = -1;
        		for ( Map.Entry<String,String> entry : columnData.entrySet() ) {
        			++iColumnData;
        			String column = entry.getKey();
        			// Get the column.
        			try {
        				int iColumn = getFieldIndex(column);
        				rec.setFieldValue(iColumn, columnDataParsedValues[iColumnData]);
        			}
        			catch ( Exception e ) {
        				// Should not happen but print warning if it does.
                        ++errorCount;
                        if ( errorMessage.length() > 0 ) {
                            errorMessage.append("\n");
                        }
                        errorMessage.append ( "Error setting column \"" + column + "\" data value (" +
                        	columnDataParsedValues[iColumnData] + ") - previous value will remain." );
        			}
        		}
        	}
        	// If here add the record to the original table.
            try {
                table.addRecord(rec);
                ++irowAppended;
            }
            catch ( Exception e ) {
               	if ( errorMessage.length() > 0 ) {
                   	errorMessage.append("\n");
               	}
                errorMessage.append("Error appending row [" + irow + "].");
                ++errorCount;
            }
        }
    }
    if ( errorCount > 0 ) {
    	Message.printWarning(3, routine, errorMessage.toString());
        throw new RuntimeException ( "There were " + errorCount + " errors appending data from \"" + appendTable.getTableID() +
        	"\" to the table \"" + table.getTableID() + "\"" );
    }
    return irowAppended;
}

/**
Change the table column data type to the requested type.
This is typically done because an original type cannot be generically handled,
for example, DBF file floating point column that is actually storing an integer.
@param reqDataType requested data type, one of TableField.DATA_TYPE_*.
@param newWidth new column width, can be -1 to not use, -2 to keep previous value.
@param newPrecision new column precision, can be -1 to not use, -2 to keep previous value.
@exception Exception if an error occurs converting values
*/
public void changeFieldDataType ( int fieldNum, int newDataType, int newWidth, int newPrecision )
throws Exception {
	int oldDataType = -1;
	String format = null; // Used when converting from Float/Double to String.
	try {
		oldDataType = this.getFieldDataType(fieldNum);
	}
	catch ( Exception e ) {
        throw new RuntimeException ( "Cannot determine data type for column [" + fieldNum + "]" );
	}
	if ( oldDataType == newDataType ) {
		// Nothing to do.
		return;
	}
	// Field will be changed below, also need width and precision to format some conversions.
	TableField field = this.getTableField(fieldNum);
	if ( (newDataType != TableField.DATA_TYPE_DOUBLE) &&
	    (newDataType != TableField.DATA_TYPE_FLOAT) &&
	    (newDataType != TableField.DATA_TYPE_INT) &&
	    (newDataType != TableField.DATA_TYPE_STRING) ) {
        throw new RuntimeException ( "New column data type \"" + TableField.getDataTypeAsString(newDataType) + "\" is not implemented." );
	}
	else {
	    if ( (newDataType == TableField.DATA_TYPE_STRING) &&
            (oldDataType != TableField.DATA_TYPE_DOUBLE) &&
	        (oldDataType != TableField.DATA_TYPE_FLOAT) ) {
	    	// Define output format, necessary to prevent default exponential format from toString().
	    	format = "%" + field.getWidth() + "." + field.getPrecision() + "f";
	    }
	}
	if ( (oldDataType != TableField.DATA_TYPE_DOUBLE) &&
	    (oldDataType != TableField.DATA_TYPE_FLOAT) &&
	    (oldDataType != TableField.DATA_TYPE_INT) &&
	    (oldDataType != TableField.DATA_TYPE_STRING) ) {
        throw new RuntimeException ( "Converstion of column data type \"" +
            TableField.getDataTypeAsString(oldDataType) + "\" to \"" +
            TableField.getDataTypeAsString(newDataType) + "\" is not implemented." );
	}
	// Change the table field information.
	field.setDataType(newDataType);
	if ( newWidth > -2 ) {
	    field.setWidth(newWidth);
	}
	if ( newPrecision > -2 ) {
	    field.setPrecision(newPrecision);
	}
	// Change the data in the table.
	int numRec = getNumberOfRecords();
	Object o;
	Float f;
	Double d;
	String s;
	Integer i;
	for ( int irec = 0; irec < numRec; irec++ ) {
	    o = this.getFieldValue(irec, fieldNum);
	    if ( o == null ) {
	    	// Nothing to be done because old and new will both be null.
	    }
	    if ( newDataType == TableField.DATA_TYPE_DOUBLE ) {
	    	if ( o instanceof Float ) {
	    	    f = (Float)o;
	    		if ( f.isNaN() ) {
	        	    this.setFieldValue(irec, fieldNum, Double.NaN );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, Double.valueOf(f.doubleValue()));
	    		}
	        }
	    	else if ( o instanceof Integer ) {
	    	    i = (Integer)o;
	            this.setFieldValue(irec, fieldNum, Double.valueOf(i.doubleValue()));
	        }
	    	else if ( o instanceof String ) {
	    	    s = (String)o;
	        	this.setFieldValue(irec, fieldNum, Double.valueOf(s));
	        }
	    }
	    else if ( newDataType == TableField.DATA_TYPE_FLOAT ) {
	    	if ( o instanceof Double ) {
	    	    d = (Double)o;
	    		if ( d.isNaN() ) {
	        	    this.setFieldValue(irec, fieldNum, Float.NaN );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, Float.valueOf((float)d.doubleValue()));
	    		}
	        }
	    	else if ( o instanceof Integer ) {
	    	    i = (Integer)o;
	            this.setFieldValue(irec, fieldNum, Float.valueOf(i.floatValue()));
	        }
	    	else if ( o instanceof String ) {
	    	    s = (String)o;
	        	this.setFieldValue(irec, fieldNum, Float.valueOf(s));
	        }
	    }
	    else if ( newDataType == TableField.DATA_TYPE_INT ) {
	    	if ( o instanceof Double ) {
	    	    d = (Double)o;
	    		if ( d.isNaN() ) {
	    		    // Integer does not have equivalent to NaN so use null.
	        	    this.setFieldValue(irec, fieldNum, null );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, Integer.valueOf(d.intValue()));
	    		}
	        }
	    	else if ( o instanceof Float ) {
	    	    f = (Float)o;
	    		if ( f.isNaN() ) {
	    		    // Integer does not have equivalent to NaN so use null.
	        	    this.setFieldValue(irec, fieldNum, null );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, Integer.valueOf(f.intValue()));
	    		}
	        }
	    	else if ( o instanceof String ) {
	    	    s = (String)o;
	        	this.setFieldValue(irec, fieldNum, Integer.valueOf(s));
	        }
	    }
	    else if ( newDataType == TableField.DATA_TYPE_STRING ) {
	    	if ( o instanceof Double ) {
	    	    d = (Double)o;
	    		if ( d.isNaN() ) {
	    		    // String does not have equivalent to NaN so use null.
	        	    this.setFieldValue(irec, fieldNum, null );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, String.format(format, d));
	    		}
	        }
	    	else if ( o instanceof Float ) {
	    	    f = (Float)o;
	    		if ( f.isNaN() ) {
	    		    // String does not have equivalent to NaN so use null.
	        	    this.setFieldValue(irec, fieldNum, null );
	    		}
	    		else {
	        	    this.setFieldValue(irec, fieldNum, String.format(format, f));
	    		}
	        }
	    	else if ( o instanceof Integer ) {
	    	    i = (Integer)o;
	        	this.setFieldValue(irec, fieldNum, "" + i);
	        }
	    }
	}
}

/**
Create a copy of the table.
@param table original table
@param newTableID identifier for new table
@param reqIncludeColumns requested columns to include or null to include all,
must specify the distinct column if only the distinct column is to be copied
(this is a change from behavior prior to TSTool 10.26.00 where distinctColumns would
override the reqIncludeColumns and default of all columns)
@param distinctColumns requested columns to check for distinct combinations, multiple columns are allowed,
specify null to not check for distinct values
@param columnMap map to rename original columns to new name
@param columnFilters map for columns that will apply a filter to match column values to include
@param columnExcludeFilters dictionary for columns that will apply a filter to match column values to exclude
@return copy of original table
*/
public DataTable createCopy ( DataTable table, String newTableID, String [] reqIncludeColumns,
    String [] distinctColumns, Hashtable<String,String> columnMap,
    Hashtable<String,String> columnFilters, StringDictionary columnExcludeFilters ) {
    String routine = getClass().getSimpleName() + ".createCopy";
    // List of columns that will be copied.
    String [] columnNamesToCopy = null;
    // TODO SAM 2013-11-25 Remove code if the functionality works.
    //if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
    //    // Distinct overrides requested column names
    //    reqIncludeColumns = distinctColumns;
    //}
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Copy only the requested names.
        columnNamesToCopy = reqIncludeColumns;
    }
    else {
        // Copy all.
        columnNamesToCopy = table.getFieldNames();
    }

    /* TODO SAM 2013-11-26 Remove this once tested - distinct columns are NOT required to be in output.
    if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
        // Add the distinct columns to the requested columns if not already included.
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
        if ( foundCount != distinctColumns.length ) { // At least one of the distinct columns was not found.
            String [] tmp = new String[reqIncludeColumns.length + (distinctColumns.length - foundCount)];
            System.arraycopy(reqIncludeColumns, 0, tmp, 0, reqIncludeColumns.length);
            int addCount = 0;
            for ( int id = 0; id < distinctColumns.length; id++ ) {
                if ( !found[id] ) {
                    tmp[tmp.length + addCount] = distinctColumns[id];
                    ++addCount; // Do after assignment above.
                }
            }
            reqIncludeColumns = tmp;
        }
    }
    */

    // Figure out which columns numbers should be copied.
    // Initialize an array with -1 and then set to actual table columns if matching.

    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    int [] columnNumbersToCopy = new int[columnNamesToCopy.length];
    for ( int icol = 0; icol < columnNamesToCopy.length; icol++ ) {
        try {
            columnNumbersToCopy[icol] = table.getFieldIndex(columnNamesToCopy[icol]);
        }
        catch ( Exception e ) {
            columnNumbersToCopy[icol] = -1; // Requested column not matched.
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "Requested column \"" + columnNamesToCopy[icol] + "\" not found in the existing table.");
        }
    }

    // Get (include) filter columns and glob-style regular expressions.

    if ( columnFilters == null ) {
        columnFilters = new Hashtable<>();
    }
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnNamesToFilter = new String[columnFilters.size()];
    String [] columnFilterGlobRegex = new String[columnFilters.size()];
    Enumeration<String> keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = keys.nextElement();
            columnNamesToFilter[ikey] = key;
            columnNumbersToFilter[ikey] = table.getFieldIndex(key);
            columnFilterGlobRegex[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation.
            columnFilterGlobRegex[ikey] = columnFilterGlobRegex[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "ColumnFilters \"" + key + "\" not found in the existing table.");
        }
    }

    // Get exclude filter columns and glob-style regular expressions.

    int [] columnExcludeFiltersNumbers = new int[0];
    String [] columnExcludeFiltersNames = new String[0];
    String [] columnExcludeFiltersGlobRegex = null;
    if ( columnExcludeFilters != null ) {
        LinkedHashMap<String, String> map = columnExcludeFilters.getLinkedHashMap();
        columnExcludeFiltersNames = new String[map.size()];
        columnExcludeFiltersNumbers = new int[map.size()];
        columnExcludeFiltersGlobRegex = new String[map.size()];
        ikey = -1;
        for ( Map.Entry<String,String> entry : map.entrySet() ) {
            ++ikey;
            columnExcludeFiltersNumbers[ikey] = -1;
            try {
                key = entry.getKey();
                columnExcludeFiltersNames[ikey] = key;
                columnExcludeFiltersNumbers[ikey] = table.getFieldIndex(key);
                columnExcludeFiltersGlobRegex[ikey] = map.get(key);
                // Turn default globbing notation into internal Java regex notation.
                columnExcludeFiltersGlobRegex[ikey] = columnExcludeFiltersGlobRegex[ikey].replace("*", ".*").toUpperCase();
            }
            catch ( Exception e ) {
                ++errorCount;
                if ( errorMessage.length() > 0 ) {
                    errorMessage.append(" ");
                }
                errorMessage.append ( "ColumnExcludeFilters column \"" + key + "\" not found in the existing table.");
            }
        }
    }
    
    // Get the distinct column numbers.
    
    int [] distinctColumnNumbers = null;
    if ( (distinctColumns != null) && (distinctColumns.length > 0) ) {
        distinctColumnNumbers = new int[distinctColumns.length];
        for ( int id = 0; id < distinctColumns.length; id++ ) {
            distinctColumnNumbers[id] = -1;
            try {
                distinctColumnNumbers[id] = table.getFieldIndex(distinctColumns[id]);
            }
            catch ( Exception e ) {
                distinctColumnNumbers[id] = -1; // Distinct column not matched.
                ++errorCount;
                if ( errorMessage.length() > 0 ) {
                    errorMessage.append(" ");
                }
                errorMessage.append ( "Distinct column \"" + distinctColumns[id] + "\" not found in the existing table.");
            }
        }
    }

    // Create a new data table with the requested column names.

    DataTable newTable = new DataTable();
    newTable.setTableID ( newTableID );
    // Get the column information from the original table.
    Object newColumnNameO = null; // Used to map column names.
    TableField newTableField; // New table field.
    List<Object []> distinctList = new ArrayList<Object []>(); // Unique combinations of requested distinct column values.
    // Create requested columns in the output table.
    for ( int icol = 0; icol < columnNumbersToCopy.length; icol++ ) {
        if ( columnNumbersToCopy[icol] == -1 ) {
            // Did not find the column in the table so add a String column for null values.
            newTableField = new TableField(TableField.DATA_TYPE_STRING, columnNamesToCopy[icol], -1, -1);
        }
        else {
            // Copy the data from the original table.
            // First make a copy of the existing table field.
            newTableField = new TableField(table.getTableField(columnNumbersToCopy[icol]));
        }
        if ( columnMap != null ) {
            newColumnNameO = columnMap.get(newTableField.getName());
            if ( newColumnNameO != null ) {
                // Reset the column name with the new name.
                newTableField.setName((String)newColumnNameO);
            }
        }
        newTable.addField(newTableField, null );
    }

    // Loop through all the data records and copy to the output table:
    // - check each row against the include and exclude filters if specified

   	if ( Message.isDebugOn && (columnNumbersToFilter.length > 0) ) {
    	Message.printStatus(2, routine, "Checking table rows for include filters:");
        for ( ikey = 0; ikey < columnNumbersToFilter.length; ikey++ ) {
        	Message.printStatus(2, routine, "  Include column name = \"" + columnNamesToFilter[ikey]
        		+ "\" column number = " + columnNumbersToFilter[ikey] + ", regex = " + columnFilterGlobRegex[ikey] );
        }
    }
   	if ( Message.isDebugOn && (columnExcludeFiltersNumbers.length > 0) ) {
    	Message.printStatus(2, routine, "Checking table rows for exclude filters:");
        for ( ikey = 0; ikey < columnExcludeFiltersNumbers.length; ikey++ ) {
        	Message.printStatus(2, routine, "  Exclude column name = \"" + columnExcludeFiltersNames[ikey]
        		+ "\" number = " + columnExcludeFiltersNumbers[ikey] + ", regex = " + columnExcludeFiltersGlobRegex[ikey] );
        }
    }

    int irowCopied = 0;
    boolean somethingCopied = false;
    boolean filterMatches, distinctMatches;
    Object o = null;
    Object [] oDistinctCheck = null;
    if ( (distinctColumnNumbers != null) && (distinctColumnNumbers.length > 0) ) {
        oDistinctCheck = new Object[distinctColumnNumbers.length];
    }
    String s;
    // The number of distinct column value that match the current row.
    int distinctMatchesCount = 0;
    for ( int irow = 0; irow < table.getNumberOfRecords(); irow++ ) {
    	// Whether something was copied:
    	// - if try will increment the row count of the copy at the end
        somethingCopied = false;
        // Whether the filters match:
        // - default to true and set to false if filters don't match
        // - default of true works if no filters are used
        filterMatches = true;
        
        // Check the include filters.

        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing copy.
            for ( int icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                	// Was not able to determine the filter columns
                    filterMatches = false;
                    break;
                }
                try {
                    o = table.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                    	// Null value so can't check the value.
                        filterMatches = false;
                        // Don't include nulls when checking values.
                        break;
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobRegex[icol]) ) {
                        // A filter did not match so don't copy the record.
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

        // If here need to check the exclude filters on the row.

        if ( columnExcludeFiltersNumbers.length > 0 ) {
            int matchesCount = 0;
            // Filters can be done on any columns so loop through to see if row matches before doing copy.
            for ( int icol = 0; icol < columnExcludeFiltersNumbers.length; icol++ ) {
                if ( columnExcludeFiltersNumbers[icol] < 0 ) {
                    // Can't do filter so don't try.
                    break;
                }
                try {
                    o = table.getFieldValue(irow, columnExcludeFiltersNumbers[icol]);
                    if ( o == null ) {
                    	if ( columnExcludeFiltersGlobRegex[icol].isEmpty() ) {
                    		// Trying to match blank cells.
                    		++matchesCount;
                    	}
                    	else {
                    		// Don't include nulls when checking values.
                    		break;
                    	}
                    }
                    s = ("" + o).toUpperCase();
                    if ( s.matches(columnExcludeFiltersGlobRegex[icol]) ) {
                        // A filter matched so don't copy the record.
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
                // Skip the record since all filters were matched.
                continue;
            }
        }

        // If here then the row is OK to include.

        if ( (distinctColumnNumbers != null) && (distinctColumnNumbers.length > 0) ) {
            // Distinct columns can be done on any columns so loop through to see if row matches before doing copy.
            // First retrieve the objects and store in an array because a distinct combinations of 1+ values is checked.
            distinctMatches = false;
            for ( int icol = 0; icol < distinctColumnNumbers.length; icol++ ) {
                if ( distinctColumnNumbers[icol] < 0 ) {
                    break;
                }
                try {
                    // This array is reused but will be copied below if needed to save.
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
            // Now actually check the values.
            for ( Object [] odArray : distinctList ) {
                distinctMatchesCount = 0;
                for ( int icol = 0; icol < distinctColumnNumbers.length; icol++ ) {
                    if ( (oDistinctCheck[icol] == null) ||
                        ((oDistinctCheck[icol] instanceof String) && ((String)oDistinctCheck[icol]).trim().length() == 0) ) {
                        // TODO SAM 2013-11-25 Don't include nulls and blank strings in distinct values.
                        // Might need to change this in the future if those values have relevance.
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
                distinctList.add(oDistinctCheckCopy); // Have another combination of distinct values to check for other table rows.
                // The row will be added below.
            }
        }

        // If here then the row can be added.

        for ( int icol = 0; icol < columnNumbersToCopy.length; icol++ ) {
            try {
                if ( columnNumbersToCopy[icol] < 0 ) {
                    // Value in new table is null.
                    newTable.setFieldValue(irowCopied, icol, null, true );
                }
                else {
                    // Value in new table is copied from original.
                    // TODO SAM 2013-08-06 Need to evaluate - following is OK for immutable objects but what about DateTime, etc?
                    newTable.setFieldValue(irowCopied, icol, table.getFieldValue(irow, columnNumbersToCopy[icol]), true );
                }
                somethingCopied = true;
            }
            catch ( Exception e ) {
                // Should not happen.
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
        throw new RuntimeException ( "There were " + errorCount + " errors transferring data to new table: " + errorMessage );
    }
    return newTable;
}

/**
Deletes a field and all the field's data from the table.
@param fieldNum the number of the field to delete.
*/
public void deleteField(int fieldNum)
throws Exception {
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
Delete all records from the table, useful when a temporary table is being reused.
@return the number of records deleted.
*/
public int deleteAllRecords()
throws Exception {
	int nrec = _table_records.size();
	_table_records.clear();
	return nrec;
}

/**
Deletes a record from the table.
@param recordNum the number of the record to delete (0+).
*/
public void deleteRecord(int recordNum)
throws Exception {
	if ( (recordNum < 0) || (recordNum > (_table_records.size() - 1)) ) {
		throw new Exception ("Record number " + recordNum + " to delete is out of bounds (0 to " + (_table_records.size() - 1) + " are allowed).");
	}

	_table_records.remove(recordNum);
}

/**
 * Determine the table columns to include in a processing task.
 * The list of columns to include is determined first.
 * Then the list of columns to exclude is removed.
 * @param includeColumns names of columns to include in a processing task,
 * can include Java expression, if empty or null include all
 * @param excludeColumns names of columns to exclude in a processing task,
 * can include Java expression, if empty or null don't exclude any
 * @return an array indicating whether the columns in the table should be included in processing (true), or not (false)
 */
public boolean[] determineColumnsToInclude(String[] includeColumns, String[] excludeColumns) {
	int cols = getNumberOfFields();
	boolean [] columnOk = new boolean[cols];
    for ( int icol = 0; icol < cols; icol++) {
    	if ( (includeColumns == null) || (includeColumns.length == 0) ) {
    		// Initialize to indicate that all columns will be processed.
    		columnOk[icol] = true;
    	}
    	else {
    		// Initialize all to false and only include columns that are requested, checked below.
    		columnOk[icol] = false;
    	}
    }
    // Loop through the table columns and check whether any are specifically included or excluded.
   	if ( ((includeColumns != null) && (includeColumns.length != 0)) || ((excludeColumns != null) && (excludeColumns.length != 0)) ) {
   		for ( int icol = 0; icol < cols; icol++) {
   			// First check included names.
   			if ( (includeColumns != null) && (includeColumns.length != 0) ) {
   				for ( String includeColumn : includeColumns ) {
   					if ( getFieldName(icol).matches(includeColumn) ) {
   						columnOk[icol] = true;
   						break;
   					}
   				}
   			}
   			if ( (excludeColumns != null) && (excludeColumns.length != 0) ) {
   				for ( String excludeColumn : excludeColumns ) {
   					if ( getFieldName(icol).matches(excludeColumn) ) {
   						columnOk[icol] = false;
   						break;
   					}
   				}
   			}
   		}
   	}
   	return columnOk;
}

/**
Dumps a table to Status level 1.
@param delimiter the delimiter to use.
@throws Exception if an error occurs.
*/
public void dumpTable(String delimiter)
throws Exception {
	String routine = getClass().getSimpleName() + ".dumpTable";
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
@param cloneData if true, the data in the table will be cloned.
If false, both tables will have pointers to the same data.
@return the new copy of the table.
*/
public static DataTable duplicateDataTable(DataTable originalTable, boolean cloneData) {
	String routine = DataTable.class.getSimpleName() + ".duplicateDataTable";

	DataTable newTable = null;
	int numFields = originalTable.getNumberOfFields();

	TableField field = null;
	TableField newField = null;
	List<TableField> tableFields = new ArrayList<>();
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
    	        	newRecord.addFieldValue(Integer.valueOf(((Integer)originalTable.getFieldValue(i, j)).intValue()));
    			}
    			else if (type == TableField.DATA_TYPE_SHORT) {
    	        	newRecord.addFieldValue(Short.valueOf(((Short)originalTable.getFieldValue(i, j)).shortValue()));
    			}
    			else if (type == TableField.DATA_TYPE_DOUBLE) {
    	        	newRecord.addFieldValue(Double.valueOf(((Double)originalTable.getFieldValue(i, j)).doubleValue()));
    			}
    			else if (type == TableField.DATA_TYPE_FLOAT) {
    	        	newRecord.addFieldValue(Float.valueOf(((Float)originalTable.getFieldValue(i, j)).floatValue()));
    			}
    			else if (type == TableField.DATA_TYPE_STRING) {
    	        	newRecord.addFieldValue((String)originalTable.getFieldValue(i, j));
    			}
    			else if (type == TableField.DATA_TYPE_DATE) {
    	        	newRecord.addFieldValue( ((Date)originalTable.getFieldValue(i, j)).clone());
    			}
                else if (type == TableField.DATA_TYPE_DATETIME) {
                    newRecord.addFieldValue( ((DateTime)originalTable.getFieldValue(i, j)).clone());
                }
    			else if (type == TableField.DATA_TYPE_LONG) {
                    newRecord.addFieldValue(Long.valueOf(((Long)originalTable.getFieldValue(i, j)).longValue()));
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
public TableRecord emptyRecord () {
    TableRecord newRecord = new TableRecord();
    int nCol = getNumberOfFields();
    for ( int i = 0; i < nCol; i++ ) {
        newRecord.addFieldValue( null );
    }
    return newRecord;
}

/**
Used internally when parsing a delimited file to determine whether a field name is already present in a table's fields,
so as to avoid duplication.
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
Format the contents of an array column into a string,
should only call if the table cell is true for isColumnArray().
@param row table row 0+
@param col table column 0+
@param return table cell formatted as a string, for example to display in UI table.
Return null if the value is null.
@exception Exception if an error occurs processing the data
*/
public String formatArrayColumn ( int row, int col ) throws Exception {
	// Get the internal data type
	int columnType = getFieldDataTypes()[col];
	// The data within the array is determined from an offset of the TableField.DATA_TYPE_ARRAY type.
	int dataType = columnType - TableField.DATA_TYPE_ARRAY_BASE;
	Object oa = getFieldValue(row,col);
	if ( oa == null ) {
		return null;
	}
	// For the purposes of rendering in the table, treat array as formatted string [ val1, val2, ... ]
	// Where the formatting of the values is for the raw value.
	StringBuilder b = new StringBuilder("[");
	switch ( dataType ) {
		case TableField.DATA_TYPE_DATETIME:
			DateTime [] dta = (DateTime [])oa;
			for ( int i = 0; i < dta.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( dta[i] != null ) {
					b.append("\"" + dta[i] + "\"");
				}
			}
			break;
		case TableField.DATA_TYPE_DOUBLE:
			double [] da = new double[0];
			if ( oa instanceof double[] ) {
				da = (double [])oa;
			}
			else if ( oa instanceof Double[] ) {
				Double [] Da = (Double [])oa;
				da = new double[Da.length];
				for ( int i = 0; i < da.length; i++ ) {
					if ( DMIUtil.isMissing(Da[i])) {
						da[i] = DMIUtil.MISSING_DOUBLE;
					}
					else {
						da[i] = Da[i];
					}
				}
			}
			else {
				throw new RuntimeException ( "Don't know how to handle double array - is not double[] or Double[]" );
			}
			for ( int i = 0; i < da.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(da[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(StringUtil.formatString(da[i],"%.6f"));
				}
			}
			break;
		case TableField.DATA_TYPE_FLOAT:
			float [] fa = new float[0];
			if ( oa instanceof float[] ) {
				fa = (float [])oa;
			}
			else if ( oa instanceof Float[] ) {
				Float [] Fa = (Float [])oa;
				fa = new float[Fa.length];
				for ( int i = 0; i < fa.length; i++ ) {
					if ( DMIUtil.isMissing(Fa[i])) {
						fa[i] = DMIUtil.MISSING_FLOAT;
					}
					else {
						fa[i] = Fa[i];
					}
				}
			}
			else {
				throw new RuntimeException ( "Don't know how to handle float array - is not float[] or Float[]" );
			}
			for ( int i = 0; i < fa.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(fa[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(StringUtil.formatString(fa[i],"%.6f"));
				}
			}
			break;
		case TableField.DATA_TYPE_INT:
			int [] ia = new int[0];
			if ( oa instanceof int[] ) {
				ia = (int [])oa;
			}
			else if ( oa instanceof Integer[] ) {
				Integer [] Ia = (Integer [])oa;
				ia = new int[Ia.length];
				for ( int i = 0; i < ia.length; i++ ) {
					if ( DMIUtil.isMissing(Ia[i])) {
						ia[i] = DMIUtil.MISSING_INT;
					}
					else {
						ia[i] = Ia[i];
					}
				}
			}
			/* TODO smalers 2021-08-02 lists have too much overhead and harder to check.  Stick to array for now.
			else if ( (oa instanceof List<?>) ) {
				// Handle List<Integer>:
				// - TODO smalers 2021-08-02 make sure the list contains Integer and handle nulls
				List<?> Ia = (List)oa;
				ia = new int[Ia.size()];
				Integer I;
				for ( int i = 0; i < ia.length; i++ ) {
					I = (Integer)Ia.get(i);
					if ( (I == null) && DMIUtil.isMissing(I) ) {
						ia[i] = DMIUtil.MISSING_INT;
					}
					else {
						ia[i] = (Integer)Ia.get(i);
					}
				}
			}
			*/
			else {
				throw new RuntimeException ( "Don't know how to handle integer array - is not int[] or Integer[]" );
			}
			for ( int i = 0; i < ia.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(ia[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(ia[i]);
				}
			}
			break;
		case TableField.DATA_TYPE_LONG:
			long [] la = new long[0];
			if ( oa instanceof long[] ) {
				la = (long [])oa;
			}
			else if ( oa instanceof Long[] ) {
				Long [] La = (Long [])oa;
				la = new long[La.length];
				for ( int i = 0; i < la.length; i++ ) {
					if ( DMIUtil.isMissing(La[i])) {
						la[i] = DMIUtil.MISSING_LONG;
					}
					else {
						la[i] = La[i];
					}
				}
			}
			else {
				throw new RuntimeException ( "Don't know how to handle long array - is not long[] or Long[]" );
			}
			for ( int i = 0; i < la.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(la[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(la[i]);
				}
			}
			break;
		case TableField.DATA_TYPE_SHORT:
			short [] sa = new short[0];
			if ( oa instanceof short[] ) {
				sa = (short [])oa;
			}
			else if ( oa instanceof Short[] ) {
				Short [] Sa = (Short [])oa;
				sa = new short[Sa.length];
				for ( int i = 0; i < sa.length; i++ ) {
					if ( DMIUtil.isMissing(sa[i])) {
						sa[i] = DMIUtil.MISSING_SHORT;
					}
					else {
						sa[i] = sa[i];
					}
				}
			}
			else {
				throw new RuntimeException ( "Don't know how to handle short array - is not short[] or Short[]" );
			}
			for ( int i = 0; i < sa.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(sa[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(sa[i]);
				}
			}
			break;
		case TableField.DATA_TYPE_BOOLEAN:
			Boolean [] Ba = new Boolean[0]; // Use Boolean object array because boolean can't indicate null value.
			if ( oa instanceof Boolean[] ) {
				Ba = (Boolean [])oa;
			}
			else if ( oa instanceof boolean[] ) {
				boolean [] ba= (boolean [])oa;
				Ba = new Boolean[ba.length];
				for ( int i = 0; i < ba.length; i++ ) {
					// No need to check for missing.
					Ba[i] = ba[i];
				}
			}
			else {
				throw new RuntimeException ( "Don't know how to handle boolean array - is not boolean[] or Boolean[]" );
			}
			for ( int i = 0; i < Ba.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( !DMIUtil.isMissing(Ba[i]) ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					b.append(Ba[i]);
				}
			}
			break;
		case TableField.DATA_TYPE_STRING:
			String [] stra = (String [])oa;
			for ( int i = 0; i < stra.length; i++ ) {
				if ( i > 0 ) {
					b.append(",");
				}
				if ( stra[i] != null ) {
					// Need to get the TableField format because the overall column will be string.
					// TODO SAM 2015-09-06
					//b.append(StringUtil.formatString(da[i],__fieldFormats[col]));
					// Quote strings to make sure separation of strings is clear
					b.append("\"" + stra[i] + "\"");
				}
			}
			break;
		default:
			// Don't know the type so don't know how to format the array. Just leave blank.
			break;
	}
	b.append("]");
	return b.toString();
}

/**
Return the time series comments.
@return The comments list.
*/
public List<String> getComments () {
    return __comments;
}

/**
Return the field data type, given an index.
@return Data type for specified zero-based index.
@param index field index (0+).
*/
public int getFieldDataType ( int index ) {
	if ( _table_fields.size() <= index ) {
        throw new ArrayIndexOutOfBoundsException( "Table field index " + index + " is not valid." );
    }
    return (_table_fields.get ( index )).getDataType();
}

/**
Return the field data types for all of the fields.
This is useful because code that processes all the fields can request the information once and then re-use.
@return Data types for all fields, in an integer array or null if no fields.
*/
public int[] getFieldDataTypes () {
	int size = getNumberOfFields();
	if ( size == 0 ) {
		return null;
	}
	int types[] = new int[size];
	for ( int i = 0; i < size; i++ ) {
		types[i] = getFieldDataType(i);
	}
	return types;
}

// TODO smalers 2022-04-29 how does this work with boolean, DateTime, etc?
/**
Get C-style format specifier that can be used to format field values for output.
This method handles string, float, double, and integer column types.
This format can be used with StringUtil.formatString().
All fields formats are set to the full width and precision defined for the field.
Strings are left-justified and numbers are right justified.
@return a String format specifier.
@param index Field index (zero-based).
*/
public String getFieldFormat ( int index ) {
	int fieldType = getFieldDataType(index);
    int fieldWidth = getFieldWidth(index);
	if ( fieldType == TableField.DATA_TYPE_STRING ) {
		// Output left-justified and padded.
	    if ( fieldWidth < 0 ) {
	        // Variable width strings.
	        return "%-s";
	    }
	    else {
	        return "%-" + fieldWidth + "." + getFieldWidth(index) + "s";
	    }
	}
	else {
		// Numbers are right justified so they line up to indicate magnitude.
        if ( (fieldType == TableField.DATA_TYPE_FLOAT) || (fieldType == TableField.DATA_TYPE_DOUBLE) ) {
            int precision = getFieldPrecision(index);
            if ( fieldWidth < 0 ) {
                if ( precision < 0 ) {
                    // No width precision specified - rely on data object representation.
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
Get C-style format specifiers that can be used to format field values for output,
handles string, float, double, and integer column types.
These formats can be used with StringUtil.formatString().
The format depends on the column type.
It is possible that objects other than the expected type will be stored in a column,
in which case there could be a formatting problem.
@return a new String array with the format specifiers.
*/
public String[] getFieldFormats() {
	int nfields = getNumberOfFields();
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
public int getFieldIndex ( String fieldName ) throws Exception {
	// Default historical behavior is to throw an exception when the field index is not found.
	return getFieldIndex ( fieldName, true );
}

/**
Return the field index associated with the given field name.
@return Index of table entry associated with the given field name.
@param field_name Field name to look up.
@param throwException if true, throw an exception if the field is not found, if false, return -1
@exception Exception if the field name is not found.
*/
public int getFieldIndex ( String fieldName, boolean throwException )
throws Exception {
	//String routine = getClass().getSimpleName() + ".getFieldndex";
	int num = _table_fields.size();
	for ( int i = 0; i < num; i++ ) {
		//Message.printStatus(2, routine, "Checking field name \"" + _table_fields.get(i).getName() + "\" to find \"" + fieldName + "\".");
		if ((_table_fields.get(i)).getName().equalsIgnoreCase(fieldName)) {
			return i;
        }
	}

	// If this line is reached, the given field was never found.
	if ( throwException ) {
		throw new Exception( "Unable to find table field with name \"" + fieldName + "\" in table \"" + getTableID() + "\"" );
	}
	else {
		return -1;
	}
}

/**
Return the field indices associated with the given field names.
This method simply calls getFieldIndex() for each requested name.
@return array of indices associated with the given field names.
@param fieldNames Field names to look up.
@exception Exception if any field name is not found.
*/
public int [] getFieldIndices ( String [] fieldNames )
throws Exception {
    int [] fieldIndices = new int[fieldNames.length];
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
public String getFieldName ( int index ) {
	return (_table_fields.get ( index )).getName();
}

/**
Return the field names for all fields.
@return a String array with the field names.
*/
public String[] getFieldNames () {
	int nfields = getNumberOfFields();
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
public int getFieldPrecision ( int index ) {
	return (_table_fields.get ( index )).getPrecision();
}

/**
Return the field value for the requested record and field name.
The overloaded method that takes integers should be called for optimal performance (so the field name lookup is avoided).
@param record_index zero-based index of record
@param field_name Field name of field to read.
@return field value for the specified field name of the specified record index
The returned object must be properly cast.
*/
public Object getFieldValue ( long record_index, String field_name )
throws Exception {
	return getFieldValue ( record_index, getFieldIndex(field_name) );
}

/**
Return the field value for the requested record and field index.
<b>Note that this method can be overruled to implement on-the-fly data reads.
For example, the DbaseDataTable class overrules this method to allow data to be read from the binary Dbase file,
as needed, at run-time, rather than reading from memory.
In this case, the haveData() method can be used to indicate if data should be taken from memory
(using this method) or read from file (using a derived class method).</b>
@param record_index zero-based index of record
@param field_index zero_based index of desired field
@return field value for the specified index of the specified record index
The returned object must be properly cast.
*/
public Object getFieldValue ( long record_index, int field_index )
throws Exception {
	int num_recs = _table_records.size();
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
	return o;
}

/**
Return the field values for all rows in the table for the requested field/column.
@return the field values for all rows in the table for the requested field/column
@param fieldName name of field for which to return values for all rows
*/
public List<Object> getFieldValues ( String fieldName )
throws Exception {
    List<Object> values = new ArrayList<>();
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
public int getFieldWidth ( int index ) {
	return (_table_fields.get ( index )).getWidth();
}

/**
Return the number of fields in the table.
@return number of fields in the table.
*/
public int getNumberOfFields () {
	return _table_fields.size();
}

// TODO SAM 2010-09-22 Evaluate whether the records list size should be returned if records in memory?
/**
Return the number of records in the table.
<b>This value should be set by code that manipulates the data table.
If the table records list has been manipulated with a call to addRecord(), the size of the list will be returned.
Otherwise, the setNumberOfRecords() methods should be called appropriately and its the value that is set will be returned.
This latter case will be in effect if tables are being read on-the-fly.</b>
@return number of records in the table.
*/
public int getNumberOfRecords () {
	if ( _add_record_called ) {
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
throws Exception {
	if ( !_haveDataInMemory ) {
		// Most likely a derived class is not handling on the fly reading of data and needs more development.
		// Return null because the limitation is likely handled elsewhere.
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
Return the TableRecord for the given column and column value.
If multiple records are matched the first record is returned.
@param columnNum column number, 0+
@param columnValue column value to match in the records.  The first matching record is returned.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value or null if no record is matched.
*/
public TableRecord getRecord ( int columnNum, Object columnValue )
throws Exception {
    int [] columnNums = new int[1];
    columnNums[0] = columnNum;
    List<Object> columnValues = new ArrayList<>();
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
Return the TableRecord for the given column and column value.
If multiple records are matched the first record is returned.
@param columnName name of column (field), case-insensitive.
@param columnValue column value to match in the records.  The first matching record is returned.
The type of the object will be checked before doing the comparison.
@return TableRecord matching the specified column value or null if no record is matched.
*/
public TableRecord getRecord ( String columnName, Object columnValue )
throws Exception {
    List<String> columnNames = new ArrayList<>();
    columnNames.add(columnName);
    List<Object> columnValues = new ArrayList<>();
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
@return List of TableRecord matching the specified column value, guaranteed to be non-null but may be empty list.
*/
public List<TableRecord> getRecords ( List<String> columnNames, List<? extends Object> columnValues )
throws Exception {
    // Figure out the column numbers that will be checked.
    int iColumn = -1;
    int [] columnNumbers = new int[columnNames.size()];
    List<TableRecord> recList = new ArrayList<>();
    for ( String columnName: columnNames ) {
        ++iColumn;
        // If -1 is returned then a column name does not exist and no matches are possible.
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
@return List of TableRecord matching the specified column values, guaranteed to be non-null but may be an empty list.
*/
public List<TableRecord> getRecords ( int [] columnNumbers, List<? extends Object> columnValues )
throws Exception {
   if ( !_haveDataInMemory ) {
        // Most likely a derived class is not handling on the fly reading of data and needs more development.
		// Return null because the limitation is likely handled elsewhere.
        // TODO SAM 2013-07-02 Why not return an empty list here?
        return null;
    }
    List<TableRecord> recList = new ArrayList<>();
    // Make sure column numbers are valid.
    for ( int iColumn = 0; iColumn < columnNumbers.length; iColumn++ ) {
        if ( columnNumbers[iColumn] < 0 ) {
            return recList;
        }
    }
    // Now search the the records and then the columns in the record.
    Object columnContents;
    int iColumn = -1;
    for ( TableRecord rec : _table_records ) { // Loop through all table records.
        int matchCount = 0; // How many column values match.
        iColumn = -1;
        for ( Object columnValue: columnValues ) {
            ++iColumn;
            columnContents = rec.getFieldValue(columnNumbers[iColumn]);
            if ( columnContents == null ) {
                // Only match if both are match.
                if ( columnValue == null ) {
                    ++matchCount;
                }
            }
            else if ( getFieldDataType(columnNumbers[iColumn]) == TableField.DATA_TYPE_STRING ) {
                // Do case insensitive comparison.
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
            // Have matched the requested number of column values so add record to the match list.
            recList.add(rec);
        }
    }
    return recList;
}

/**
Return the table identifier.
@return the table identifier.
*/
public String getTableID () {
    return __table_id;
}

/**
Return the list of TableRecords.
@return list of TableRecord.
*/
public List<TableRecord> getTableRecords ( ) {
	return _table_records;
}

/**
Return the TableField object for the requested column.
@param index Table field index (zero-based).
@return TableField object for the specified zero-based index.
*/
public TableField getTableField ( int index ) {
	return (_table_fields.get( index ));
}

/**
Get the data type for the field.
@return the data type for the field (see TableField.DATA_TYPE_*).
@param index index of field (zero-based).
@exception If the index is out of range.
@deprecated use getFieldDataType
*/
public int getTableFieldType ( int index ) {
	if ( _table_fields.size() <= index ) {
		throw new ArrayIndexOutOfBoundsException( "Index " + index + " is not valid." );
	}
	return _table_fields.get(index).getDataType ();
}

/**
Return the unique field values for the requested field index.
This is used, for example, when displaying unique values on a map display.
The calling code will need to cast the returned objects appropriately.
The performance of this operation will degrade if a large number of unique values are present.
This should not normally be the case if the end-user is intelligent about their
choice of the field that is being analyzed.
@param field_index zero_based index of desired field
@return Simple array (e.g., double[]) of unique data values from the field.
Depending on the field data type, a double[], int[], short[], or String[] will be returned.
@exception if the field index is not in the allowed range.
*/
/* TODO SAM Implement this later.
public Object getUniqueFieldValues ( int field_index )
throws Exception {
	int num_recs = _table_records.size();
	int num_fields = _table_fields.size();

	if ( num_fields <= field_index ) {
		throw new Exception ( "Requested field index " + field_index +
		" is not available (only " + num_fields + " are available)." );
	}

	// Use a temporary list to get the unique values.
	Vector u = new Vector ( 100, 100 );

	// Determine the field type.
	int field_type = getTableFieldType ( field_index );
	//String rtn = "getFieldValue";
	//Message.printStatus ( 10, rtn, "Getting table record " +
	//	record_index + " from " + num_recs + " available records." );
	TableRecord tableRecord = null;
	Object o = null;
	for ( int i = 0; i < num_recs; i++ ) {
		tableRecord = (TableRecord)_table_records.elementAt(i);
		o = tableRecord.getFieldValue(field_index);
		// Now search through the list of known unique values.
		usize = u.size();
		for ( j = 0; j < usize; j++ ) {
		}
	}
	// Now return the values in an array of the appropriate type.
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
Indicate whether the table has data in memory.
This will be true if any table records have been added during a read or write operation.
This method is meant to be called by derived classes
that allow records to be accessed on the fly rather than from memory (e.g., dBase tables).
*/
public boolean haveDataInMemory () {
	return _haveDataInMemory;
}

/**
Initialize the data.
@param tableFieldsList list of TableField used to define the DataTable.
@param listSize Initial list size for the list holding records.
@param sizeIncrement Increment for the list holding records.
*/
private void initialize ( List<TableField> tableFieldsList, int listSize, int sizeIncrement ) {
	_table_fields = tableFieldsList;
	_table_records = new ArrayList<> ( 10 );
}

/**
Insert a table record into the table.
If inserting at the start or middle, the provided table record will be inserted and all other records will be shifted.
If inserting after the existing records, empty records will be added up to the requested insert position.
@param row row position (0+) to insert the record
@param record table record to insert
@param doCheck indicate whether the record should be checked against the table for consistency;
false inserts with no check (currently this parameter is not enabled).
Use emptyRecord() to create a record that matches the table design.
@exception Exception if there is an error inserting the record
*/
public void insertRecord ( int row, TableRecord record, boolean doCheck )
throws Exception {
    // TODO SAM 2014-02-01 enable doCheck.
    int nRows = getNumberOfRecords();
    if ( row < nRows ) {
        // Inserting in the existing table.
        _table_records.add ( row, record );
    }
    else {
        // Appending - add blank rows up until the last one.
        for ( int i = nRows; i < row; i++ ) {
            addRecord(emptyRecord());
        }
        // Now add the final record.
        addRecord ( record );
    }
}

/**
Determine whether the column data type is an array.
@return true if the column data type is an array (data type is DATA_TYPE_ARRAY_BASE plus primitive type).
*/
public boolean isColumnArray(int columnType) {
	if ( ((columnType/100)*100) == TableField.DATA_TYPE_ARRAY_BASE ) {
		// Data type is 10nn so it is an array.
		return true;
	}
	else {
		return false;
	}
}

/**
Indicate whether a table's column is empty (all null or blank strings).
This is useful when setting column widths narrow for unused data, or deleting unused columns.
@param columnNum column number 0+ to check
@return true if the column is empty, false if contains at least one record with a value.
*/
public boolean isColumnEmpty ( int columnNum ) {
	TableRecord rec = null;
	int recCount = getNumberOfRecords();
	int emptyCount = 0;
	String s;
	Object o = null;
	int columnType = getFieldDataType(columnNum);
	for (int i = 0; i < recCount; i++) {
		rec = _table_records.get(i);
		try {
			o = rec.getFieldValue(columnNum);
		}
		catch ( Exception e ) {
			// Count as empty.
			++emptyCount;
			continue;
		}
		if ( o == null ) {
			++emptyCount;
		}
		else if ( columnType == TableField.DATA_TYPE_STRING ) {
			s = (String)o;
			if ( s.trim().isEmpty() ) {
				++emptyCount;
			}
		}
	}
	if ( emptyCount == recCount ) {
		return true;
	}
	else {
		return false;
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
@param joinColumnsMap map indicating which columns need to be matched in the tables,
for the join (this must be populated, even if the join column name is the same in both tables)
@param reqIncludeColumns requested columns to include from the second table or null to include all
(the join tables will be automatically included because they exist in the first table)
@param columnMap map to rename original columns to new name
@param columnFilters map for columns that will apply a filter to limit rows that are processed
@param joinMethod the method used to join the tables
@param handleMultipleMatchesHow indicate how multiple join matches should be handled
(currently only NUMBER_COLUMNS and USE_LAST_MATCH [default] are supported)
@param problems list of problems that will be filled during processing
@return the number of rows joined
*/
public int joinTable ( DataTable table, DataTable tableToJoin, Hashtable<String,String> joinColumnsMap, String [] reqIncludeColumns,
    Hashtable<String,String> columnMap, Hashtable<String,String> columnFilters, DataTableJoinMethodType joinMethod,
    HandleMultipleJoinMatchesHowType handleMultipleMatchesHow, List<String> problems ) {
    String routine = getClass().getSimpleName() + ".joinTable", message;

    // List of columns that will be copied to the first table.
    String [] columnNamesToCopy = null;
    if ( (reqIncludeColumns != null) && (reqIncludeColumns.length > 0) ) {
        // Append only the requested names.
        columnNamesToCopy = reqIncludeColumns;
        for ( int icol = 0; icol < reqIncludeColumns.length; icol++ ) {
            Message.printStatus(2,routine,"Will copy table2 column \"" + reqIncludeColumns[icol] + "\"" );
        }
    }
    else {
        // Append all.
        Message.printStatus(2,routine,"Copy all columns in table2 to table1.");
        columnNamesToCopy = tableToJoin.getFieldNames();
    }
    // Make sure that the columns to copy do not include the join columns, which should already by in the tables.
    // Just set to blank so they can be ignored in following logic.
    for ( int icol = 0; icol < columnNamesToCopy.length; icol++ ) {
        Enumeration<String> keys = joinColumnsMap.keys();
        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            if ( columnNamesToCopy[icol].equalsIgnoreCase(key) ) {
                Message.printStatus(2,routine,"Table 2 column to copy \"" + columnNamesToCopy[icol] +
                    "\" is same as join column.  Will not copy from table2.");
                columnNamesToCopy[icol] = "";
            }
        }
    }
    // Column numbers in the copy table to match the original table.  Any values set to -1 will result in null in output.
    // numberDuplicates will add columns on the fly as they are needed, at end of table.
    String [] table1CopyColumnNames = new String[columnNamesToCopy.length];
    int [] table1CopyColumnNumbers = new int[columnNamesToCopy.length];
    int [] table1CopyColumnTypes = new int[columnNamesToCopy.length];
    String [] table2CopyColumnNames = new String[columnNamesToCopy.length];
    int [] table2CopyColumnNumbers = new int[columnNamesToCopy.length];
    int [] table2CopyColumnTypes = new int[columnNamesToCopy.length];
    List<Integer> matchCountList = new ArrayList<>();
    if ( handleMultipleMatchesHow == HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS ) {
    	// Create a list to count how many matches have occurred so that duplicates can add new numbered columns.
    	// Will need to be careful if "InsertBeforeColumn" functionality is added.
    	matchCountList = new ArrayList<>(table.getNumberOfRecords());
    }
    // Replace the copy table names using the column map.
    Object o;
    for ( int icol = 0; icol < columnNamesToCopy.length; icol++ ) {
        table1CopyColumnNames[icol] = columnNamesToCopy[icol]; // Default to same as requested.
        table2CopyColumnNames[icol] = columnNamesToCopy[icol]; // Default.
        if ( table2CopyColumnNames[icol].equals("") ) {
        	// Column was removed from copy above (typically because it is the join column).
        	table2CopyColumnNumbers[icol] = -1;
        }
        else {
	        try {
	            table2CopyColumnNumbers[icol] = tableToJoin.getFieldIndex(table2CopyColumnNames[icol]);
	            table2CopyColumnTypes[icol] = tableToJoin.getFieldDataType(table2CopyColumnNumbers[icol]);
	        }
	        catch ( Exception e ) {
	            message = "Cannot determine table2 copy column number for \"" + table2CopyColumnNames[icol] + "\".";
	            problems.add ( message );
	            Message.printWarning(3,routine,message);
	        }
        }
        if ( columnMap != null ) {
            // Initialize the table2 column to join from the requested columns, with matching name in both tables.
            // Rename in output (table1).
            o = columnMap.get(table2CopyColumnNames[icol]);
            if ( o != null ) {
                // Reset the copy column name with the new name, which will match a column name in the first table
                // (or will be created in the new table if necessary).
                // This column may not yet exist in the joined table so get column number and type below after column is added.
                table1CopyColumnNames[icol] = (String)o;
            }
        }
        Message.printStatus(2,routine,"Will copy table2 column \"" + table2CopyColumnNames[icol] + "\" to table1 column \"" +
            table1CopyColumnNames[icol] + "\"" );
    }

    // Create columns in the output table for the "include columns" (including new column names from the column map).
    // Use column types that match the copy table's column types.
    // Figure out the column numbers in both tables for the include.
    for ( int icol = 0; icol < table1CopyColumnNames.length; icol++ ) {
        table1CopyColumnNumbers[icol] = -1;
        if ( table1CopyColumnNames[icol].length() == 0 ) {
            // Name was removed above because it duplicates the join column, so don't add.
            continue;
        }
        try {
            table1CopyColumnNumbers[icol] = table.getFieldIndex(table1CopyColumnNames[icol]);
        }
        catch ( Exception e ) {
             // OK - handle non-existent column below.
        }
        if ( table1CopyColumnNumbers[icol] >= 0 ) {
            // Already exists so skip because don't want table2 values to overwrite table1 values.
            message = "Include column \"" + table1CopyColumnNames[icol] +
                "\" already exists in original table.  Not adding new column.";
            Message.printStatus(2,routine,message);
            // TODO SAM 2014-04-15 Actually, do want join to overwrite - allows subset of table to be processed.
            //table1CopyColumnNumbers[icol] = -1;
            table1CopyColumnTypes[icol] = table.getFieldDataType(table1CopyColumnNumbers[icol]);
        }
        else {
            // Does not exist in first table so create column with the same properties as the original.
            // Use the original column name to find the property.
            try {
                Message.printStatus(2,routine,"Creating table1 column \"" + table1CopyColumnNames[icol] +
                    "\" type=" + TableColumnType.valueOf(tableToJoin.getFieldDataType(table2CopyColumnNumbers[icol])) +
                    " width=" + tableToJoin.getFieldWidth(table2CopyColumnNumbers[icol]) +
                    " precision=" + tableToJoin.getFieldPrecision(table2CopyColumnNumbers[icol]));
                table1CopyColumnNumbers[icol] = table.addField(
                    new TableField(tableToJoin.getFieldDataType(table2CopyColumnNumbers[icol]),
                    table1CopyColumnNames[icol],tableToJoin.getFieldWidth(table2CopyColumnNumbers[icol]),
                    tableToJoin.getFieldPrecision(table2CopyColumnNumbers[icol])), null);
                table1CopyColumnTypes[icol] = table.getFieldDataType(table1CopyColumnNumbers[icol]);
            }
            catch ( Exception e ) {
                message = "Error adding new column \"" + table1CopyColumnNames[icol] + "\" to joined table (" + e + ").";
                problems.add ( message );
                Message.printWarning(3,routine,message);
            }
        }
    }

    // Determine the column numbers in the first and second tables for the join columns.
    // Do this AFTER the above checks on output columns because columns may be inserted and change the column order.
    /* TODO SAM 2015-02-03 Does not seem to be needed.
    if ( reqIncludeColumns == null ) {
        reqIncludeColumns = new String[0];
    }
    */
    String [] table1JoinColumnNames = new String[joinColumnsMap.size()];
    int [] table1JoinColumnNumbers = new int[joinColumnsMap.size()];
    int [] table1JoinColumnTypes = new int[joinColumnsMap.size()];
    String [] table2JoinColumnNames = new String[joinColumnsMap.size()];
    int [] table2JoinColumnNumbers = new int[joinColumnsMap.size()];
    int [] table2JoinColumnTypes = new int[joinColumnsMap.size()];
    Enumeration<String> keys = joinColumnsMap.keys();
    String key;
    int ikey = -1;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        table1JoinColumnNames[ikey] = "";
        table1JoinColumnNumbers[ikey] = -1;
        table2JoinColumnNames[ikey] = "";
        table2JoinColumnNumbers[ikey] = -1;
        key = keys.nextElement();
        Message.printStatus(2, routine, "Determining join columns for table1 join column \"" + key + "\"");
        try {
            table1JoinColumnNames[ikey] = key;
            table1JoinColumnNumbers[ikey] = table.getFieldIndex(table1JoinColumnNames[ikey]);
            table1JoinColumnTypes[ikey] = table.getFieldDataType(table1JoinColumnNumbers[ikey]);
            Message.printStatus(2,routine,"Table1 join column \"" + table1JoinColumnNames[ikey] + "\" has table1 column number=" +
                table1JoinColumnNumbers[ikey]);
            try {
            	// Look up the column to use in table2 by using a key from table1.
                table2JoinColumnNames[ikey] = joinColumnsMap.get(table1JoinColumnNames[ikey]);
                table2JoinColumnNumbers[ikey] = tableToJoin.getFieldIndex(table2JoinColumnNames[ikey]);
                table2JoinColumnTypes[ikey] = tableToJoin.getFieldDataType(table2JoinColumnNumbers[ikey]);
                Message.printStatus(2,routine,"Table2 join column \"" + table2JoinColumnNames[ikey] + "\" has table2 column number=" +
                    table2JoinColumnNumbers[ikey]);
            }
            catch ( Exception e ) {
                message = "Table2 join column \"" + table2JoinColumnNames[ikey] + "\" not found in table2 \"" +
                    tableToJoin.getTableID() + "\".";
                problems.add ( message );
                Message.printWarning(3,routine,message);
            }
        }
        catch ( Exception e ) {
            message = "Join column \"" + table1JoinColumnNames[ikey] + "\" not found in table1 \"" + table.getTableID() + "\".";
            problems.add (message);
            Message.printWarning(3,routine,message);
        }
    }

    // Get filter columns and glob-style regular expressions.
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobRegex = new String[columnFilters.size()];
    keys = columnFilters.keys();
    ikey = -1;
    key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = (String)keys.nextElement();
            columnNumbersToFilter[ikey] = tableToJoin.getFieldIndex(key);
            columnFilterGlobRegex[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation.
            columnFilterGlobRegex[ikey] = columnFilterGlobRegex[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            message = "Filter column \"" + key + "\" not found in table \"" + tableToJoin.getTableID() + "\".";
            problems.add ( message );
            Message.printWarning(3,routine,message);
        }
    }
    // Loop through all of the records in the table being joined and check the filters.
    // Do this up front because the records are checked multiple times during the join.
    boolean [] joinTableRecordMatchesFilter = new boolean[tableToJoin.getNumberOfRecords()];
    int icol;
    // Number of table 1 rows joined with table2 data.
    int nrowsJoined = 0;
    String s;
    for ( int irow = 0; irow < tableToJoin.getNumberOfRecords(); irow++ ) {
        joinTableRecordMatchesFilter[irow] = true;
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing copy.
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    joinTableRecordMatchesFilter[irow] = false;
                    break;
                }
                try {
                    o = tableToJoin.getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        joinTableRecordMatchesFilter[irow] = false;
                        break; // Don't include nulls when checking values.
                    }
                    // Do filter on strings only using uppercase.
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobRegex[icol]) ) {
                        // A filter did not match so don't copy the record.
                        joinTableRecordMatchesFilter[irow] = false;
                        break;
                    }
                }
                catch ( Exception e ) {
                    message = "Error getting copy table data for [" + irow + "][" + columnNumbersToFilter[icol] + "] (" + e + ").";
                    problems.add(message);
                    Message.printWarning(3, routine, message );
                }
            }
        }
    }
    // Loop through all the data records in the original table (the original records,
    // NOT any that have been appended due to the join), loop through records in the join table,
    // and join records to the table original as appropriate
    // (this may result in a modification of the same records, or appending new records at the bottom of the table).
    // Keep track of which rows do not match and add at the end.  Otherwise, duplicate rows are added.
    boolean [] joinTableRecordMatchesTable1 = new boolean[tableToJoin.getNumberOfRecords()];
    int tableNumRows = table.getNumberOfRecords();
    boolean joinColumnsMatch = false; // Indicates whether two tables' join column values match.
    Object table1Value, table2Value;
    String stringTable1Value, stringTable2Value;
    TableRecord recToModify = null;
    // Loop through all rows in the first table.
    for ( int irow = 0; irow < tableNumRows; irow++ ) {
    	if ( handleMultipleMatchesHow == HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS ) {
    		// Initialize the number of matches for this row.
    		matchCountList.add(Integer.valueOf(0));
    	}
        // Loop through all rows in the second table
        for ( int irowJoin = 0; irowJoin < tableToJoin.getNumberOfRecords(); irowJoin++ ) {
            if ( !joinTableRecordMatchesFilter[irowJoin] ) {
                // Join row did not match filter so no need to process it.
                continue;
            }
            else {
                // Join table record matched filter so evaluate if the join column values match in the two tables.
                // If there is a match, the join will be done in-line with an existing record.
                // If not, the join will only occur if the join method is JOIN_ALWAYS and in this case the join column values.
                // and all append values will be added to the main table in a new row.
                joinColumnsMatch = true; // Set to false in checks below.
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
                    // For now if either is null do not add the record.
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
                    // All other data types use equals.
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
                        recToModify = table.getRecord(irow); // Modify existing row in table.
                    }
                    catch ( Exception e ) {
                        message = "Error getting existing joined record to modify (" + e + ").";
                        problems.add ( message );
                        Message.printWarning(3, routine, message );
                    }
                    // Loop through the columns to copy and set the values from
                    // the second table into the first table (which previously had columns added).
                    for ( icol = 0; icol < table2CopyColumnNumbers.length; icol++ ) {
                        try {
                            if ( table1CopyColumnNumbers[icol] < 0 ) {
                                // There was an issue with the column to add so skip.
                                //Message.printStatus(2,routine,"Don't have column number for table1 column \"" +
                                //     table1CopyColumnNames[icol] + "\"");
                                continue;
                            }
                            else if ( table2CopyColumnNumbers[icol] < 0 ) {
                                // There was an issue with the column to add so skip.
                                //Message.printStatus(2,routine,"Don't have column number for table2 column \"" +
                                //     table2CopyColumnNames[icol] + "\"");
                                continue;
                            }
                            else {
                                // Set the value in the original table, if the type matches.
                                // TODO SAM 2013-08-19 Check that the column types match.
                                if ( table1CopyColumnTypes[icol] == table2CopyColumnTypes[icol] ) {
                                	if ( handleMultipleMatchesHow == HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS ) {
                                        // Increment the match counter.
                                		if ( icol == 0 ) {
                                			matchCountList.set(irow,Integer.valueOf(matchCountList.get(irow) + 1));
                                			Message.printStatus(2, routine, "Incremented match counter [" + irow +
                                				"] to " + matchCountList.get(irow) + " for column \"" + table2CopyColumnNames[icol] + "\"");
                                		}
                                		if ( matchCountList.get(irow) == 1 ) {
                                			// This is the first match so do simple set on requested output columns.
                                			// Set the column values in the joined table.
    	                                    recToModify.setFieldValue(table1CopyColumnNumbers[icol],
    	                                        tableToJoin.getFieldValue(irowJoin, table2CopyColumnNumbers[icol]));
                                		}
                                		else {
                                			// Else, need to add output columns that have number appended.
                                			// For now look up the column.
                                			// TODO SAM 2015-03-04 add column to the existing column number array to increase performance.
                                			int icol1 = -1;
                                			String duplicateColumn = table1CopyColumnNames[icol] + "_" + matchCountList.get(irow);
                                			Message.printStatus(2,routine,"Duplicate match.  Will output to column \"" + duplicateColumn + "\"");
                                			try {
                                				Message.printStatus(2,routine,"See if column \"" + duplicateColumn + "\" exists.");
                                				icol1 = table.getFieldIndex(duplicateColumn);
                                				Message.printStatus(2,routine,"It does, will write to table 1 column [" + icol1 + "].");
                                			}
                                			catch ( Exception e ) {
                                				// Add the column if it has not been added by a previous duplicate.
                                				// First get the column used for the first match, which will not have a trailing number.
                                				Message.printStatus(2,routine,"It does not, need to add new column to table1.");
                                				Message.printStatus(2,routine,"Getting table2 column to copy properties [" + table2CopyColumnNumbers[icol] + "]");
                                				TableField tf = tableToJoin.getTableField(table2CopyColumnNumbers[icol]);
                                				// Keep everything the same except change the column name.
                                				Message.printStatus(2,routine,"Adding table1 column \"" + duplicateColumn + "\" with properties from \"" + tf.getName() + "\"");
                                				icol1 = table.addField(
                                					new TableField(tf.getDataType(), duplicateColumn, tf.getWidth(), tf.getPrecision()), null);
                                				Message.printStatus(2,routine,"Added table1 column \"" + duplicateColumn + "\" [" + icol1 + "]");
                                			}
                                			// Set in new column number, using table2 column number to copy.
                                			Message.printStatus(2,routine,"Setting table1 col \"" + duplicateColumn + "\" [" + icol1 + "] from table1 [" +irowJoin +
                                				"][" + icol + "] value " + tableToJoin.getFieldValue(irowJoin, table2CopyColumnNumbers[icol]));
                                			recToModify.setFieldValue(icol1,tableToJoin.getFieldValue(irowJoin, table2CopyColumnNumbers[icol]));
                                		}
                                	}
                                	else {
                                		// Set the column values in the joined table.
	                                    recToModify.setFieldValue(table1CopyColumnNumbers[icol],
	                                        tableToJoin.getFieldValue(irowJoin, table2CopyColumnNumbers[icol]));
                                	}
                                    ++nrowsJoined;
                                }
                                else {
                                    Message.printStatus(2,routine,"Column types are different, cannot set value from table2 to table1.");
                                }
                            }
                        }
                        catch ( Exception e ) {
                            // Should not happen.
                            message = "Error setting [" + irow + "][" + table1CopyColumnNumbers[icol] + "] (" + e + ").";
                            problems.add(message);
                            Message.printWarning(3, routine, message );
                        }
                    }
                }
            }
        }
    }
    // Now add any rows that were not matched:
    // - add at the end so as to not upset the original sequence and lists used above
    // - TODO SAM 2015-03-05 Need to enable for NUMBER_COLUMNS.
    if ( joinMethod == DataTableJoinMethodType.JOIN_ALWAYS ) {
    	if ( handleMultipleMatchesHow == HandleMultipleJoinMatchesHowType.NUMBER_COLUMNS ) {
    		problems.add("Requested NumberColumns for multiple join matches but not suported "
    			+ "with JoinMethod=JoinAlways - multiple matches will be in extra rows.");
    	}
        for ( int irowJoin = 0; irowJoin < tableToJoin.getNumberOfRecords(); irowJoin++ ) {
            if ( joinTableRecordMatchesTable1[irowJoin] ) {
                // Row was matched above so no need to add again.
                continue;
            }
            // Add a row to the table, containing only the join column values from the second table
            // and nulls for all the other values.
            try {
                recToModify = table.addRecord(table.emptyRecord());
            }
            catch ( Exception e ) {
                message = "Error adding new record to modify (" + e + ").";
                problems.add ( message );
                Message.printWarning(3, routine, message );
            }
            // A new record was added.  Also include the join column values using the table1 names.
            // TODO SAM 2013-08-19 Evaluate whether table2 names should be used (or option to use).
            for ( icol = 0; icol < table2JoinColumnNumbers.length; icol++ ) {
                try {
                    if ( table2JoinColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip.
                        continue;
                    }
                    else {
                        // Set the value in the original table, if the type matches.
                        // TODO SAM 2013-08-19 Check that the column types match.
                        if ( table1JoinColumnTypes[icol] == table2JoinColumnTypes[icol] ) {
                            recToModify.setFieldValue(table1JoinColumnNumbers[icol],
                                tableToJoin.getFieldValue(irowJoin, table2JoinColumnNumbers[icol]));
                            ++nrowsJoined;
                        }
                    }
                }
                catch ( Exception e ) {
                    // Should not happen.
                    message = "Error setting row value for column [" + table1JoinColumnNumbers[icol] + "] (" + e + ").";
                    problems.add(message);
                    Message.printWarning(3, routine, message );
                    Message.printWarning(3, routine, e );
                }
            }
            // Loop through the columns to include and set the values from
            // the second table into the first table (which previously had columns added).
            for ( icol = 0; icol < table2CopyColumnNumbers.length; icol++ ) {
                try {
                    if ( table1CopyColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip.
                        Message.printStatus(2,routine,"Don't have column number for table1 column \"" +
                             table1CopyColumnNames[icol]);
                        continue;
                    }
                    else if ( table2CopyColumnNumbers[icol] < 0 ) {
                        // There was an issue with the column to add so skip.
                        Message.printStatus(2,routine,"Don't have column number for table2 column \"" +
                             table2CopyColumnNames[icol] + "\"");
                        continue;
                    }
                    else {
                        // Set the value in the original table, if the type matches.
                        // TODO SAM 2013-08-19 Check that the column types match.
                        if ( table1CopyColumnTypes[icol] == table2CopyColumnTypes[icol] ) {
                            recToModify.setFieldValue(table1CopyColumnNumbers[icol],
                                tableToJoin.getFieldValue(irowJoin, table2CopyColumnNumbers[icol]));
                            ++nrowsJoined;
                        }
                        else {
                            Message.printStatus(2,routine,"Column types are different, cannot set value from table2 to table1.");
                        }
                    }
                }
                catch ( Exception e ) {
                    // Should not happen.
                    message = "Error adding new row, column [" + table1CopyColumnNumbers[icol] + "] (" + e + ").";
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
    // TODO smalers 2020-10-19 ignore the nrowsJoined from above and compute from the tracking array:
    // - need to further clean this up
    int nrowsJoined2 = 0;
    for ( int i = 0; i < joinTableRecordMatchesTable1.length; i++ ) {
    	if ( joinTableRecordMatchesTable1[i] ) {
    		++nrowsJoined2;
    	}
    }
    //return nrowsJoined;
    return nrowsJoined2;
}

/**
Given a definition of what data to expect, read a simple delimited file and store the data in a table.
Comment lines start with # and are not considered part of the header.
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
Given a definition of what data to expect, read a simple delimited file and store the data in a table.
Comment lines start with # and are not considered part of the header.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields list of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).
The header lines are read and ignored.
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
Given a definition of what data to expect, read a simple delimited file and store the data in a table.
Comment lines start with # and are not considered part of the header.
This method may not be maintained in the future.
The parseFile() method is more flexible.
@return new DataTable containing data.
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file (typically a comma).
@param tableFields list of TableField objects defining data expectations.
@param num_lines_header number of lines in header (typically 1).  The header lines are read and ignored.
@param trim_spaces if true, then when a column value is read between delimiters,
it will be .trim()'d before being parsed into a number or String.
@param maxLines the maximum number of lines to read from the file.
If less than or equal to 0, all lines will be read.
@exception Exception if there is an error parsing the file.
*/
public static DataTable parseDelimitedFile ( String filename, String delimiter, List<TableField> tableFields,
	int num_lines_header, boolean trim_spaces, int maxLines)
throws Exception {
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

	// Create an array to use for determining the maximum size of all the fields that are Strings.
	// This will be used to set the width of the data values for those fields so that the width of the field is
	// equal to the width of the longest string.
	// This is mostly important for when the table is to be placed within a DataTable_TableModel,
	// so that the String field data are not truncated.
	int numFields = tableFields.size();
	int[] stringLengths = new int[numFields];
	for (int i = 0; i < numFields; i++) {
		stringLengths[i] = 0;
	}
	int length = 0;

	while (( iline = in.readLine ()) != null ) {
		// Check if read comment or empty line.
		if ( iline.startsWith("#") || iline.trim().length()==0) {
			continue;
		}

		// TODO SAM if a column contains only quoted strings, but each string is a number,
		// then there is no way to treat the column as strings.  This may be problematic if the string is zero-padded.
		columns = StringUtil.breakStringList ( iline, delimiter, StringUtil.DELIM_ALLOW_STRINGS);

		// Line is part of header.
		if ( !processed_header ) {
			num_fields = columns.size();
			if ( num_fields < tableFields.size() ) {
				in.close();
				throw new IOException ( "Table fields specifications do not match data found in file." );
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
    					contents.addFieldValue(	Double.valueOf(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_INT ) {
    					contents.addFieldValue(	Integer.valueOf(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_SHORT ) {
    					contents.addFieldValue(	Short.valueOf(col));
    				}
    				else if ( field_types[i] ==	TableField.DATA_TYPE_FLOAT ) {
    					contents.addFieldValue(	Float.valueOf(col));
    				}
                    else if ( field_types[i] == TableField.DATA_TYPE_LONG ) {
                        contents.addFieldValue( Long.valueOf(col));
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

			// Set the widths of the string fields to the length of the longest strings within those fields.
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
throws Exception {
	return parseDelimitedFileHeader ( filename, "," );
}

/**
Reads the header of a delimited file and return list of TableField objects.
The field names will be correctly returned.
The data type, however, will be set to TableField.DATA_TYPE_STRING.  This should be changed if not appropriate.
@return list of TableField objects (field names will be correctly set but data type will be string).
@param filename name of file containing delimited data.
@param delimiter string representing delimiter in data file.
@exception Exception if there is an error reading the file.
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

    		// Check whether read a comment or empty line.
    		if ( iline.startsWith("#") || iline.trim().length()==0) {
    			continue;
    		}

    		columns = StringUtil.breakStringList ( iline, delimiter, 0);
    //			StringUtil.DELIM_SKIP_BLANKS );

    		num_fields = columns.size();
    		tableFields = new ArrayList<> ( num_fields );
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
// For now assume no embedded quotes in quoted strings.
/**
Parses a file and returns the DataTable for the file.
Currently only does delimited files, and the data type for a column must be consistent.
The lines in delimited files do not need to all have the same number of columns:
the number of columns in the returned DataTable will be
the same as the line in the file with the most delimited columns,
all others will be padded with empty value columns on the right of the table.
@param filename the name of the file from which to read the table data,
will be ignored if the BufferedReader property is provided.
@param props a PropList with settings for how the file should be read and handled.<p>
Properties and their effects:<br>
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>    <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>BufferedReader</b></td>
<td>A BufferedReader that has been opened for the filename to read,
for example if the file is being read from a Jar file resource.
The BufferedReader must be set as the property contents (not the string value).</td>
<td>The specified file will be read.</td>
</tr>

<tr>
<td><b>ColumnDataTypes</b></td>
<td>The data types for the column, either "Auto" (determine from column contents) or "AllStrings"
(all are strings, fastest processing and the default from historical behavior).
SEE ALSO DateTimeColumns.</td>
<td>AllStrings.</td>
</tr>

<tr>
<td><b>ColumnNames</b></td>
<td>The column names, separated by commas.
Specify when the file does not contain column names.
If specified, the HeaderLines parameter will be ignored.</td>
<td></td>
</tr>

<tr>
<td><b>CommentLineIndicator</b></td>
<td>The characters with which comment lines begin.
Lines starting with this character are skipped (TrimInput is applied after checking for comments).</td>
<td>No default.</td>
</tr>

<tr>
<td><b>DateTimeColumns</b></td>
<td>Specify comma-separated column names that should be treated as DateTime columns.
The column names must agree with those determined from the table headings.</td>
<td>Determine column types from data - date/times are not determined.</td>
</tr>

<tr>
<td><b>Delimiter</b></td>
<td>The character (s) that should be used to delimit fields in the file.
Fields are broken using the following StringUtil.breakStringList() call
(the flag can be modified by MergeDelimiters):<br>
<blockquote>
    v = StringUtil.breakStringList(line, delimiters, 0);
</blockquote><br></td>
<td>Comma (,).</td>
</tr>

<tr>
<td><b>DoubleColumns</b></td>
<td>Specify comma-separated column names that should be treated as double precision columns.
The column names must agree with those determined from the table headings.</td>
<td>Determine column types from data - date/times are not determined.</td>
</tr>

<tr>
<td><b>FixedFormat</b></td>
<td>"True" or "False".  Currently ignored.</td>
<td></td>
</tr>

<tr>
<td><b>HeaderLines (previously HeaderRows)</b></td>
<td>The lines containing the header information, specified as single number or a range (e.g., 2-3).
Multiple lines will be separated with a newline when displayed,
or Auto to automatically treat the first non-comment row as a header if the value is double-quoted.
This will be ignored if ColumnNames is specified.</td>
<td>Auto</td>
</tr>

<tr>
<td><b>IntegerColumns</b></td>
<td>Specify comma-separated column names that should be treated as integer columns.
The column names must agree with those determined from the table headings.</td>
<td>Determine column types from data - date/times are not determined.</td>
</tr>

<tr>
<td><b>MergeDelimiters</b></td>
<td>"True" or "False".  If true, then adjoining delimiter characters are treated as one by using
StringUtil.breakStringList(line,delimiters,StringUtil.DELIM_SKIP_BLANKS.</td>
<td>False (do not merge blank columns).</td>
</tr>

<tr>
<td><b>SkipLines (previously SkipRows)</b></td>
<td>Lines from the original file to skip (each value 0+), as list of comma-separated individual row or ranges like 3-6.
Skipped lines are generally information that cannot be parsed.
The lines are skipped after the initial read and are not available for further processing.</td>
<td>Don't skip any lines.</td>
</tr>

<tr>
<td><b>TextColumns</b></td>
<td>Specify comma-separated column names that should be treated as text columns.
The column names must agree with those determined from the table headings.</td>
<td>Determine column types from data - date/times are not determined.</td>
</tr>

<tr>
<td><b>Top</b></td>
<td>Specify an integer that is the top N rows to be processed.</td>
<td>Determine column types from data - date/times are not determined.</td>
</tr>

<tr>
<td><b>TrimInput</b></td>
<td>"True" or "False".  Indicates input strings should be trimmed before parsing.</td>
<td>False</td>
</tr>

<tr>
<td><b>TrimStrings</b></td>
<td>"True" or "False".
Indicates whether strings should be trimmed before being placed in the data table (after parsing).</td>
<td>False</td>
</tr>

</table>
@return the DataTable that was created.
@throws Exception if an error occurs
*/
public static DataTable parseFile(String filename, PropList props)
throws Exception {
    String routine = DataTable.class.getSimpleName() + ".parseFile";
	if ( props == null ) {
		props = new PropList(""); // To simplify code below.
	}
	// TODO SAM 2005-11-16 why is FixedFormat included?  Future feature?
	/*String propVal = props.getValue("FixedFormat");
	if (propVal != null) {
		if (propVal.equalsIgnoreCase("false")) {
			fixed = false;
		}
	}
	*/

	// Default is to treat column types as all strings, which is fastest:
	// - setting ColumnDataTypes=Auto will determine column types by data
    boolean ColumnDataTypes_Auto_boolean = false;
    // TODO SAM 2008-04-15 Evaluate whether the following should be used.
    //String ColumnDataTypes = "AllStrings";  // Default for historical reasons.
    String propVal = props.getValue("ColumnDataTypes");
    if ( propVal != null ) {
    	if ( propVal.equalsIgnoreCase("Auto") ) {
    		ColumnDataTypes_Auto_boolean = true;
    	}
    	else if ( propVal.equalsIgnoreCase("AllStrings") ) {
    		//ColumnDataTypes = "Auto";
    		ColumnDataTypes_Auto_boolean = false;
    	}
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
        // Use older form.
        propVal = props.getValue("HeaderRows");
        if ( propVal != null ) {
            Message.printWarning(3, routine, "Need to convert HeaderRows parameter to HeaderLines in software." );
        }
    }
    List<Integer> HeaderLineList = new ArrayList<>();
    int HeaderLinesList_maxval = -1; // Used to optimize code below.
    boolean HeaderLines_Auto_boolean = false; // Are header rows to be determined automatically?
    if ( (propVal == null) || (propVal.length() == 0) ) {
        // Default is determine header lines automatically.
        HeaderLines_Auto_boolean = true;
    }
    else {
        // Interpret the HeaderLines property.
        Message.printStatus ( 2, routine, "HeaderLines=\"" + propVal + "\"" );
        if ( propVal.equalsIgnoreCase("Auto")) {
            HeaderLines_Auto_boolean = true;
        }
        else {
            // Determine the list of rows for the header.
            List<String> headerRowList = StringUtil.breakStringList ( propVal, ", ", StringUtil.DELIM_SKIP_BLANKS );
            if ( headerRowList.size() > 1 ) {
            	Message.printWarning(3, routine,
            		"Currently only know how to handle a single header line (headers must be on one line in file).");
            	// Remove the remaining header line numbers.
            	for ( int i = (headerRowList.size() - 1); i > 0; --i ) {
            		headerRowList.remove(i);
            	}
            }
            // Code below can handle multiple header lines but will only parse one line for now.
            for ( String vi : headerRowList) {
                if ( StringUtil.isInteger(vi)) {
                	// Single integer.
                    int row = Integer.parseInt(vi);
                    Message.printStatus ( 2, routine, "Header row is [" + row + "]");
                    HeaderLineList.add(Integer.valueOf(row));
                    HeaderLinesList_maxval = Math.max(HeaderLinesList_maxval, row);
                }
                else {
                	// Check whether a range of integers such as 0-1.
                    int pos = vi.indexOf("-");
                    if ( pos >= 0 ) {
                        // Specifying a range of values.
                        int first_header = -1;
                        int last_header = -1;
                        if ( pos == 0 ) {
                            // First index is 0.
                            first_header = 0;
                        }
                        else {
                            // Get first header row, zero index.
                            first_header = Integer.parseInt(vi.substring(0,pos).trim());
                        }
                        // Get the last header row, zero index.
                        last_header = Integer.parseInt(vi.substring(pos+1).trim());
                        // Add a list of integers corresponding to the header rows, zero index.
                        for ( int is = first_header; is <= last_header; is++ ) {
                            HeaderLineList.add(Integer.valueOf(is));
                            HeaderLinesList_maxval = Math.max(HeaderLinesList_maxval, is);
                        }
                    }
                }
            }
        }
    }
    // Use to speed up code below when checking for header rows.
    int HeaderLinesList_size = HeaderLineList.size();

	String [] columnNames = new String[0];
    propVal = props.getValue("ColumnNames");
    if ( (propVal != null) && !propVal.isEmpty() ) {
    	// Use the column names that are specified (otherwise headers may be found below).
		columnNames = propVal.split(",");
		for ( int i = 0; i < columnNames.length; i++ ) {
			columnNames[i] = columnNames[i].trim();
		}
    }

    String [] dateTimeColumns = null;
    propVal = props.getValue("DateTimeColumns");
    if ( (propVal != null) && !propVal.isEmpty() ) {
        dateTimeColumns = propVal.split(",");
        for ( int i = 0; i < dateTimeColumns.length; i++ ) {
            dateTimeColumns[i] = dateTimeColumns[i].trim();
        }
    }

    String [] doubleColumns = null;
    propVal = props.getValue("DoubleColumns");
    if ( (propVal != null) && !propVal.isEmpty() ) {
    	doubleColumns = propVal.split(",");
        for ( int i = 0; i < doubleColumns.length; i++ ) {
        	doubleColumns[i] = doubleColumns[i].trim();
        }
    }

    String [] integerColumns = null;
    propVal = props.getValue("IntegerColumns");
    if ( (propVal != null) && !propVal.isEmpty() ) {
    	integerColumns = propVal.split(",");
        for ( int i = 0; i < integerColumns.length; i++ ) {
        	integerColumns[i] = integerColumns[i].trim();
        }
    }

    String [] textColumns = null;
    propVal = props.getValue("TextColumns");
    if ( (propVal != null) && !propVal.isEmpty() ) {
        textColumns = propVal.split(",");
        for ( int i = 0; i < textColumns.length; i++ ) {
            textColumns[i] = textColumns[i].trim();
        }
    }

    int top = -1;
    int topm1 = -1; // Used for 0-index comparison.
    propVal = props.getValue("Top");
    if ( (propVal != null) && !propVal.isEmpty() ) {
    	try {
    		top = Integer.parseInt(propVal);
    	    topm1 = top - 1;
    	}
    	catch ( NumberFormatException e ) {
    		// Just process all.
    	}
    }

	int parseFlagHeader = StringUtil.DELIM_ALLOW_STRINGS;
	// Retain the quotes in data records makes sure that quoted numbers come across as intended as literal strings.
    // This is important when numbers are zero padded, such as for station identifiers.
	// The problem is that it will result in embedded escaped quotes "" in the output.
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
        // Try the older form.
        propVal = props.getValue("SkipRows");
        if ( propVal != null ) {
            Message.printWarning(3, routine, "Need to convert SkipRows parameter to SkipLines in software." );
        }
    }
    List<Integer> skipLinesList = new ArrayList<>();
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
                skipLinesList.add(Integer.valueOf(row));
                skipLinesList_maxval = Math.max(skipLinesList_maxval, row);
            }
            else {
                int pos = vi.indexOf("-");
                if ( pos >= 0 ) {
                    // Specifying a range of values.
                    int first_to_skip = -1;
                    int last_to_skip = -1;
                    if ( pos == 0 ) {
                        // First index is 0.
                        first_to_skip = 0;
                    }
                    else {
                        // Get first to skip.
                        first_to_skip = Integer.parseInt(vi.substring(0,pos).trim());
                    }
                    last_to_skip = Integer.parseInt(vi.substring(pos+1).trim());
                    for ( int is = first_to_skip; is <= last_to_skip; is++ ) {
                        skipLinesList.add(Integer.valueOf(is));
                        skipLinesList_maxval = Math.max(skipLinesList_maxval, is);
                    }
                }
            }
        }
    }
    // Use to speed up code below.
    int skipLinesList_size = skipLinesList.size();

	propVal = props.getValue("TrimInput");
	boolean TrimInput_Boolean = false; // Default.
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		TrimInput_Boolean = true;
	}

    boolean TrimStrings_boolean = false;
	propVal = props.getValue("TrimStrings");
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		TrimStrings_boolean = true;
	}

	// 'data_record_tokens' is all strings from the initial read:
	// - TODO smalers 2022-05-11 evaluate using string arrays to use less memory, especially for tokens in a row
	List<List<String>> data_record_tokens = new ArrayList<>();
	List<String> tokens = null;
	int maxColumns = 0;
	int numColumnsParsed = 0;

	Object object = props.getContents("BufferedReader");
	BufferedReader in = null;
	if ( object != null ) {
		// Use the provided BufferedReader.
		in = (BufferedReader)object;
	}
	else {
		// Open the file.
		in = new BufferedReader(new FileReader(filename));
	}
	String line;

	// TODO JTS 2006-06-05
	// Found a bug in DataTable.
	// If attempt to call parseFile() on a file of size 0 (no lines, no characters) it will throw an exception.
	// This should be checked out in the future.

	// If the column names were specified, set them up front.
	List<TableField> tableFields = null; // Table fields as controlled by header or examination of data records.
	boolean headers_found = false; // Indicates whether the headers have been found.
	int numFields = -1; // Number of table fields.
	if ( columnNames.length > 0 ) {
        tableFields = parseFile_SetColumnNames ( columnNames );
        numFields = tableFields.size();
        headers_found = true;
	}

	// Read until the end of the file.

	int linecount = 0; // linecount = 1 for first line in file, for user perspective.
	int dataLineCount = 0; // Count of data lines (comments and header lines are not included).
	int linecount0; // linecount0 = linecount - 1 (zero index), for code perspective.
	int headerLineCount = 0; // Count of header lines.
	int noncommentLineCount0 = -1; // Count of noncomment lines, could be header or data.
	TableField tableField = null; // Table field added below.
	while ( true ) {
		line = in.readLine();
		if ( line == null ) {
		    // End of file.
		    break;
		}
		++linecount;
		linecount0 = linecount - 1; // Zero index.

		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine, "Line [" + linecount0 + "]: " + line );
		}

		// Skip any comments anywhere in the file.
		if ( (CommentLineIndicator != null) && line.startsWith(CommentLineIndicator) ) {
		    continue;
		}
		++noncommentLineCount0;

		// Also skip the requested lines to skip linecount is 1+ while lines to skip are 0+.

		if ( linecount0 <= skipLinesList_maxval ) {
		    // Need to check it.
		    if ( parseFile_LineMatchesLineFromList(linecount0,skipLinesList, skipLinesList_size)) {
		        // Skip the line as requested.
                continue;
		    }
		}

		// "line" now contains the latest non-comment line:
		// - evaluate whether the line contains the column names (header lines)
		// - header lines MUST come before data lines
		// - currently only handle one header line

        //Message.printStatus(2, routine, "headers_found=" + headers_found + ", dataLineCount=" + dataLineCount +
        //	", HeaderLines_Auto_boolean=" + HeaderLines_Auto_boolean + ", HeaderLineList=" + HeaderLineList );
		if ( !headers_found && (dataLineCount == 0) &&
			(HeaderLines_Auto_boolean || ((HeaderLineList != null) && (dataLineCount == 0)) ) ) { //&& (headerLineCount <= HeaderLinesList_maxval)) ) ) {}
		    if ( HeaderLines_Auto_boolean ) {
		        // If a quote is detected, then this line is assumed to contain the name of the columns.
        	    if (line.startsWith("\"")) {
        	    	// HeaderLineCount
		            Message.printStatus(2, routine, "Header line to parse:" + line);
        	        tableFields = parseFile_ParseHeaderLine ( line, linecount0, TrimInput_Boolean, Delimiter, parseFlagHeader );
        	        numFields = tableFields.size();
        	        // Go to the top of the loop to read another line.
        	        ++headerLineCount;
        	        headers_found = true;
        	        continue;
        	    }
		    }
		    else if ( HeaderLineList != null ) {
		        // Calling code has specified the header rows.  Check to see if this is a header row:
		    	// - 'linecount0' is the line position, zero index
		    	// - the HeaderLines line index is also zero index
		        if ( parseFile_LineMatchesLineFromList(noncommentLineCount0, HeaderLineList, HeaderLinesList_size)) {
		            // This row has been specified as a header row so process it.
		            Message.printStatus(2, routine, "Header line to parse:" + line);
		            tableFields = parseFile_ParseHeaderLine ( line, linecount0, TrimInput_Boolean, Delimiter, parseFlagHeader );
		            numFields = tableFields.size();

                    //FIXME SAM 2008-01-27 Figure out how to deal with multi-row headings.
                    // What is the column name?
		            // If the maximum header row has been processed, indicate that headers have been found.
		            //if ( linecount0 == HeaderLines_Vector_maxval ) {
        	        ++headerLineCount;
		                headers_found = true;
		            //}
        	        // Go to the top of the loop to read another line.
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

		++dataLineCount;
		// If "Top" was specified as a parameter, skip lines after top.
		if ( (top >= 0) && (dataLineCount > top) ) {
			break;
		}

        if ( TrimInput_Boolean ) {
			tokens = StringUtil.breakStringList(line.trim(), Delimiter, parseFlag );
		}
		else {
            tokens = StringUtil.breakStringList(line, Delimiter, parseFlag );
		}
		numColumnsParsed = tokens.size();
		if (numColumnsParsed > maxColumns) {
			maxColumns = numColumnsParsed;
		}
		// Save the tokens from the data rows - this will NOT include comments, headers, or lines to be excluded.
		data_record_tokens.add(tokens);
	}
	// Close the file.
	in.close();

	// Make sure that the table fields are in place for the maximum number of columns.

	if (tableFields == null) {
		tableFields = new ArrayList<>();
		for (int i = 0; i < maxColumns; i++) {
			// Default field definition builds String fields.
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
	// Do this in any case because the length of the string columns and precision for floating point columns need to be determined.

	numFields = tableFields.size();
	int numRecords = data_record_tokens.size(); // Number of data records.
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
    // Loop through all rows of data that were read.
    int numTokens;
    String cell;
    String cellTrimmed; // Must have when checking for types.
    String cellTrimmed2; // Used if string is quoted.
    int periodPos; // Position of period in floating point numbers.
    boolean isTypeFound = false;
	for ( int irow = 0; irow < numRecords; irow++ ) {
		// If "Top" was specified as a parameter, skip lines after top.
		if ( (top >= 0) && (irow > topm1) ) {
			break;
		}
	    tokens = data_record_tokens.get(irow);
	    numTokens = tokens.size();
	    // Loop through all columns in the row.
	    for ( int icol = 0; icol < numTokens; icol++ ) {
	        cell = tokens.get(icol);
	        cellTrimmed = cell.trim();
    		// Some data has quotes so remove for the purpose of checking precision, etc.:
	        // - quoted strings are treated as string column data unless overruled
	    	cellTrimmed2 = cellTrimmed.replace("\"", "");
	        isTypeFound = false;
	        if ( Message.isDebugOn ) {
	        	//Message.printStatus(2, routine, "Checking irow=" + irow + " icol=" + icol + " cellTrimmed=" + cellTrimmed);
	        }
	        if ( cellTrimmed.length() == 0 ) {
	        	// Blank cell - can be any type and should not impact result.
	        	++count_blank[icol];
	        	isTypeFound = true;
	        }
	        if ( StringUtil.isInteger(cellTrimmed2)) {
	        	if ( StringUtil.isInteger(cellTrimmed)) {
	        		// Only count as an integer if not quoted.
	        		++count_int[icol];
	        		isTypeFound = true;
	        	}
	            // Length needed in case handled as string data.
	            lenmax_string[icol] = Math.max(lenmax_string[icol], cellTrimmed2.length());
	        }
	        // TODO SAM 2012-05-31 Evaluate whether this needs a more robust solution.
	        // Sometimes long integers won't parse in the above but do get parsed as doubles below.
	        // This can lead to treatment as a floating point number.
	        // Instead, the column likely should be treated as strings.
	        // An example is very long identifiers like "394359105411900".
	        // For now the work-around is to add quotes in the original data to make sure the column is treated like a string.
	        // Could add a long but this cascades through a lot of code since the long type is not yet supported in DataTable.
            if ( StringUtil.isDouble(cellTrimmed2)) {
            	if ( StringUtil.isDouble(cellTrimmed) ) {
	        		// Only count as a double if not quoted.
            		++count_double[icol];
            		isTypeFound = true;
            	}
                // Length needed in case handled as string data and also to format the double.
                lenmax_string[icol] = Math.max(lenmax_string[icol], cellTrimmed2.length());
                // Precision to help with visualization, such as table views.
                periodPos = cellTrimmed2.indexOf(".");
                if ( periodPos >= 0 ) {
                	// String cell has a period so process the precision:
                	// - precision is the number of digits after the decimal
                	// - TODO smalers 2022-05-11 what if in scientific notation?
                    precision[icol] = Math.max(precision[icol], (cellTrimmed2.length() - periodPos - 1) );
                }
            }
            // TODO SAM 2008-01-27 Need to handle date/time?
            if ( !isTypeFound ) {
                // Assume string, but strip off the quotes if necessary.
                ++count_string[icol];
                if ( TrimStrings_boolean ) {
                    lenmax_string[icol] = Math.max(lenmax_string[icol], cellTrimmed.length());
                }
                else {
                    lenmax_string[icol] = Math.max(lenmax_string[icol], cell.length());
                }
            }
	    }
	}

	// TODO SAM 2016-08-25 Could optimize so that if all column types are specified, don't need to scan data for type.

	// Loop through the table fields and based on the examination of data above,
	// set the table field type and if a string, max width.

	int [] tableFieldType = new int[tableFields.size()];
	boolean isString = false;
	boolean isDateTime = false;
	boolean isInteger = false;
	boolean isDouble = false;
	if ( ColumnDataTypes_Auto_boolean ) {
    	for ( int icol = 0; icol < maxColumns; icol++ ) {
    		isDateTime = false;
    	    tableField = (TableField)tableFields.get(icol);
    		if ( dateTimeColumns != null ) {
    			for ( int i = 0; i < dateTimeColumns.length; i++ ) {
    				if ( dateTimeColumns[i].equalsIgnoreCase(tableField.getName()) ) {
    					isDateTime = true;
    				}
    			}
    		}
    		isDouble = false;
    		if ( doubleColumns != null ) {
    			for ( int i = 0; i < doubleColumns.length; i++ ) {
    				if ( doubleColumns[i].equalsIgnoreCase(tableField.getName()) ) {
    					isDouble = true;
    				}
    			}
    		}
    		isInteger = false;
    		if ( integerColumns != null ) {
    			for ( int i = 0; i < integerColumns.length; i++ ) {
    				if ( integerColumns[i].equalsIgnoreCase(tableField.getName()) ) {
    					isInteger = true;
    				}
    			}
    		}
    		isString = false;
    		if ( textColumns != null ) {
    			for ( int i = 0; i < textColumns.length; i++ ) {
    				if ( textColumns[i].equalsIgnoreCase(tableField.getName()) ) {
    					isString = true;
    				}
    			}
    		}
    		// Set column type based on calling code specified type and then discovery from data.
    	    if ( isDateTime ) {
    	    	tableField.setDataType(TableField.DATA_TYPE_DATETIME);
    	        tableFieldType[icol] = TableField.DATA_TYPE_DATETIME;
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is date/time as determined from specified column type (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
    	    }
    	    else if ( isDouble ) {
    	    	tableField.setDataType(TableField.DATA_TYPE_DOUBLE);
    	        tableFieldType[icol] = TableField.DATA_TYPE_DOUBLE;
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is double as determined from specified column type (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
                tableField.setWidth (lenmax_string[icol] );
                tableField.setPrecision ( precision[icol] );
    	        // Default the following.
                //tableField.setWidth (-1);
                //tableField.setPrecision ( 6 );
    	    }
    	    else if ( isInteger ) {
    	    	tableField.setDataType(TableField.DATA_TYPE_INT);
    	        tableFieldType[icol] = TableField.DATA_TYPE_INT;
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is integer as determined from specified column type (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
    	    }
    	    else if ( isString ) {
    	    	tableField.setDataType(TableField.DATA_TYPE_STRING);
    	        tableFieldType[icol] = TableField.DATA_TYPE_STRING;
    	        if ( lenmax_string[icol] <= 0 ) {
    	            // Likely that the entire column of numbers is empty so set the width to the field name width if available).
    	            tableField.setWidth (tableFields.get(icol).getName().length() );
    	        }
    	        else {
    	            tableField.setWidth (lenmax_string[icol] );
    	        }
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is string as determined from specified column type (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
    	    }
    	    else if ( (count_int[icol] > 0) && (count_string[icol] == 0) &&
    	        ((count_double[icol] == 0) || (count_int[icol] == count_double[icol])) ) {
    	        // All data are integers so assume column type is integer.
    	        // Note that integers also meet the criteria of double, hence the extra check above.
    	        // TODO SAM 2013-02-17 Need to handle DATA_TYPE_LONG.
    	        tableField.setDataType(TableField.DATA_TYPE_INT);
    	        tableFieldType[icol] = TableField.DATA_TYPE_INT;
    	        tableField.setWidth (lenmax_string[icol] );
    	        Message.printStatus ( 2, routine, "Column [" + icol +
    	            "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is integer as determined from examining data (" + count_int[icol] +
    	            " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks).");
    	    }
    	    else if ( (count_double[icol] > 0) && (count_string[icol] == 0) ) {
    	        // All data are double (integers will also count as double) so assume column type is double.
                tableField.setDataType(TableField.DATA_TYPE_DOUBLE);
                tableFieldType[icol] = TableField.DATA_TYPE_DOUBLE;
                tableField.setWidth (lenmax_string[icol] );
                tableField.setPrecision ( precision[icol] );
                Message.printStatus ( 2, routine, "Column [" + icol +
                    "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is double as determined from examining data (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings, " +
                    count_blank[icol] + " blanks, width=" + lenmax_string[icol] + ", precision=" + precision[icol] + ".");
            }
    	    else {
    	        // Based on what is known, can only treat column as containing strings.
    	        tableField.setDataType(TableField.DATA_TYPE_STRING);
    	        tableFieldType[icol] = TableField.DATA_TYPE_STRING;
    	        if ( lenmax_string[icol] <= 0 ) {
    	            // Likely that the entire column of numbers is empty so set the width to the field name width if available).
    	            tableField.setWidth (tableFields.get(icol).getName().length() );
    	        }
    	        else {
    	            tableField.setWidth (lenmax_string[icol] );
    	        }
    	        Message.printStatus ( 2, routine, "Column [" + icol +
                    "] \"" + tableField.getName() + "\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	            "\" is string as determined from examining data (" + count_int[icol] +
                    " integers, " + count_double[icol] + " doubles, " + count_string[icol] + " strings), " +
                    count_blank[icol] + " blanks.");
    	       // Message.printStatus ( 2, routine, "length max=" + lenmax_string[icol] );
    	    }
    	}
	}
	else {
	    // All are strings (from above but reset just in case).
	    for ( int icol = 0; icol < maxColumns; icol++ ) {
	        tableField = (TableField)tableFields.get(icol);
	        tableField.setDataType(TableField.DATA_TYPE_STRING);
	        tableFieldType[icol] = TableField.DATA_TYPE_STRING;
	        tableField.setWidth (lenmax_string[icol] );
	        Message.printStatus ( 2, routine,"Column [" + icol + "] \"" + tableField.getName() +
	        	"\" type \"" + TableField.getDataTypeAsString(tableField.getDataType()) +
    	        "\" is " + tableField.getDataType() + " all strings assumed, width=" + tableField.getWidth() );
	    }
	}
	// The data fields may have less columns than the headers and if so set the field type of the unknown columns to string.
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
	for (int irow = 0; irow < numRecords; irow++) {
		// If "Top" was specified as a parameter, skip lines after top.
		if ( (top >= 0) && (irow > topm1) ) {
			break;
		}
		tokens = data_record_tokens.get(irow);

		tablerec = new TableRecord(maxColumns);
		cols = tokens.size();
		for (int icol = 0; icol < cols; icol++) {
			if (TrimStrings_boolean) {
			    cell = tokens.get(icol).trim();
			}
			else {
				cell = tokens.get(icol);
			}
			if ( ColumnDataTypes_Auto_boolean ) {
			    // Set the data as an object of the column type.
			    if ( tableFieldType[icol] == TableField.DATA_TYPE_INT ) {
		    		// Some data has quotes around numbers so remove.
			    	cell = cell.trim().replace("\"","");
			    	if ( parseFile_CellContentsNull(cell) ) {
			    		tablerec.addFieldValue ( null );
			    	}
			    	else {
			    		tablerec.addFieldValue( Integer.valueOf(cell) );
			    	}
			    }
			    else if ( tableFieldType[icol] == TableField.DATA_TYPE_DATETIME ) {
			    	cell = cell.trim();
			    	if ( parseFile_CellContentsNull(cell) ) {
			    		tablerec.addFieldValue ( null );
			    	}
			    	else {
			    		try {
			    			// Some data has quotes around date/times so remove.
			    			tablerec.addFieldValue( DateTime.parse(cell.replace("\"", "")) );
			    		}
			    		catch ( Exception e ) {
			    			tablerec.addFieldValue ( null );
			    		}
			    	}
	            }
			    else if ( tableFieldType[icol] == TableField.DATA_TYPE_DOUBLE ) {
		    		// Some data has quotes around numbers so remove.
			    	cell = cell.trim().replace("\"", "");
			    	if ( parseFile_CellContentsNull(cell) ) {
			    		tablerec.addFieldValue ( null );
			    	}
			    	else {
			    		tablerec.addFieldValue( Double.valueOf(cell) );
			    	}
	            }
			    else if ( tableFieldType[icol] == TableField.DATA_TYPE_STRING ) {
			    	// Know that it is a string.
			    	// Could contain embedded "" that need to be replaced with single ".
			    	tablerec.addFieldValue( parseFile_ProcessString(cell) );
			    }
			    else {
			        // Add as string.
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
		// Sometimes the header row has more columns than data rows,
		// in particular because breakStringList() will drop an empty field at the end.

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
	    // There were errors processing the data.
	    String message = "There were " + errorCount + " errors processing the data.";
	    Message.printWarning ( 3, routine, message );
	    throw new Exception ( message );
	}

	return table;
}

/**
 * Determine whether a cell's string contents are null.
 * This will be the case if the cell is empty, "null" (upper or lower case).
 * Call this when processing non-text cells that need to store a value (double, integer, boolean, date/time, etc.).
 */
private static boolean parseFile_CellContentsNull ( String cell ) {
	if ( (cell == null) || cell.isEmpty() || cell.toUpperCase().equals("NULL") ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Determine whether a line from the file matches the list of rows that are of interest.
@param linecount0 line count, zero index, may be full count for (SkipLines) or header count (HeaderLines)
@param rows_List list of Integer objects that are row numbers (0+) of interest.
@param rows_List_size Size of rows_List - used to speed up performance.
@return true if the line matches an item in the list.
*/
private static boolean parseFile_LineMatchesLineFromList( int linecount0, List<Integer> rows_List, int rows_List_size ) {
    Integer int_object;
    if ( rows_List != null ) {
        rows_List_size = rows_List.size();
    }
    for ( int is = 0; is < rows_List_size; is++ ) {
        int_object = rows_List.get(is);
        if ( linecount0 == int_object.intValue() ) {
            // Skip the line as requested.
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
    String line, int linecount0, boolean TrimInput_Boolean, String Delimiter, int parse_flag ) {
    String routine = DataTable.class.getSimpleName() + ".parseFile_ParseHeaderLine";
    Message.printStatus ( 2, routine, "Adding column headers from line [" + linecount0 + "]: " + line );
    List<String> columns = null;
    if ( TrimInput_Boolean ) {
        columns = StringUtil.breakStringList( line.trim(), Delimiter, parse_flag );
    }
    else {
        columns = StringUtil.breakStringList(line, Delimiter, parse_flag );
    }

    int numFields = columns.size();
    List<TableField> tableFields = new ArrayList<>();
    TableField tableField = null;
    String temp = null;
    for (int i = 0; i < numFields; i++) {
        temp = columns.get(i).trim();
        while (findPreviousFieldNameOccurances(tableFields, temp)) {
            temp = temp + "_2";
        }
        tableField = new TableField();
        tableField.setName(temp);
        // All table fields by default are treated as strings:
        // - the column type will be set after reading the data lines
        tableField.setDataType(TableField.DATA_TYPE_STRING);
        tableFields.add(tableField);
    }
    return tableFields;
}

/**
Process a string table field value before setting as data in the table.
Remote surrounding quotes.
@param cell table cell value as string
*/
private static String parseFile_ProcessString ( String cell ) {
    if ( (cell == null) || (cell.length() == 0) ) {
        return cell;
    }
    char c1 = cell.charAt(0);
    int len = cell.length();
    char c2 = cell.charAt(len - 1);
    if ( (c1 == '"') || (c1 == '\'') ) {
        // Have a quoted string.  Remove the quotes from each end (but not the middle).
        // Embedded quotes will typically be represented as double quote "" or '' so replace.
        if ( (c2 == c1) && (len > 1) ) {
            return cell.substring(1,len - 1).replace("\"\"", "\"");
            // Add single quotes later if necessary...seem to mainly deal with double quotes.
        }
        else {
            // Truncated field or error in input?  Unlikely case.
            return cell.substring(1);
        }
    }
    else {
        return cell;
    }
}

/**
Initialize the table fields to specified column names.
All fields are set to type String, although this will be reset when data records are processed.
@param columnNames column names to use for table fields.
@return A list of TableField describing the table columns.
*/
private static List<TableField> parseFile_SetColumnNames ( String [] columnNames ) {
    List<TableField> tableFields = new ArrayList<>();
    TableField tableField = null;
    String temp = null;
    for (int i = 0; i < columnNames.length; i++) {
        temp = columnNames[i].trim();
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
Rename field(s) (column(s)) in a table.
@param table table to modify
@param columnMap map to rename original columns to new name
@param problems list of problems that will be filled during processing
*/
public void renameFields ( DataTable table, Hashtable<String,String> columnMap, List<String> problems ) {
    //String routine = getClass().getSimpleName() + ".renameFields";

	// Iterate through the columnMap and rename columns
	for ( Map.Entry<String,String> entry: columnMap.entrySet() ) {
		String name = entry.getKey();
		String newName = entry.getValue();
		// Get the existing column by searching.
		int colNum = -1;
		try {
			colNum = getFieldIndex(name);
		}
		catch ( Exception e ) {
			problems.add("Requested column name \"" + name + "\" is not found in table \"" + table.getTableID() + "\"");
			continue;
		}
		TableField field = _table_fields.get(colNum);
		// Rename the column.
		field.setName(newName);
	}
}

/**
Reorder field(s) (column(s)) in a table.
Columns that are not found will be ignored.
Columns that are not specified will remain on the right side of the table.
@param table table to modify
@param columnNames array of column names in desired order
@param problems list of problems that will be filled during processing
@exception Exception if a serious error occurs, in which case the table might be left in a state of corruption
*/
public void reorderFields ( String [] columnNames, List<String> problems ) throws Exception {
    // Loop through the column names.
    int columnNumberOld;
    int columnNumberNew = -1;
    for ( String columnName : columnNames ) {
    	columnNumberOld = -1;
    	try {
    		columnNumberOld = getFieldIndex(columnName);
    	}
    	catch ( Exception e ) {
    		columnNumberOld = -1;
    		problems.add ( "Column \"" + columnName + "\" is not found in table \"" + getTableID() + "\" - ignoring column for reorder." );
    		continue;
    	}

    	// The new column number is sequential from the start of the column name list.
    	++columnNumberNew;

    	// Swap the table fields (only changes the fields, not the data rows).

    	TableField tableFieldOld = this._table_fields.get(columnNumberOld);
    	this._table_fields.add(columnNumberNew, tableFieldOld);
    	this._table_fields.remove(columnNumberOld + 1);

    	// Swap the data row fields:
    	// - have to process each record because the table is row-based

    	Object dataOld = null;
    	for ( TableRecord rec : this._table_records ) {
    		// These methods will throw an Exception if something bad happens:
    		// - the table might be left in a state of corruption due to records being misaligned
    		dataOld = rec.getFieldValue(columnNumberOld);
    		rec.addFieldValue(columnNumberNew, dataOld);
    		rec.deleteField(columnNumberOld + 1);
    	}
    }

    // All original columns that were not requested will remain at the end of the table.
}

/**
Set the comments string list.
@param comments Comments to set.
*/
public void setComments ( List<String> comments ) {
    if ( comments != null ) {
        __comments = comments;
    }
}

/**
Sets the precision of the field.
@param col the column (0+) for which to set the precision.
@param precision the precision to set.
*/
public void setFieldPrecision(int col, int precision)
throws Exception {
	TableField field = _table_fields.get(col);
	field.setPrecision(precision);
}

/**
Sets the value of a specific field.
@param row the row (0+) in which to set the value.
@param col the column (0+) in which to set the value.
@param value the value to set.
@exception Exception if the field value cannot be set, including if the row does not exist.
*/
public void setFieldValue(int row, int col, Object value)
throws Exception {
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
throws Exception {
    int nRows = getNumberOfRecords();
    if ( (row > (nRows - 1)) && createIfNecessary ) {
        // Create empty rows.
        for ( int i = nRows; i <= row; i++ ) {
            addRecord(emptyRecord());
        }
    }
    // Now set the value (will throw ArrayIndexOutOfBoundsException if row is out of range).
    TableRecord record = _table_records.get(row);
    record.setFieldValue(col, value);
}

/**
Sets the width of the field.
@param col the column (0+) for which to set the width.
@param width the width to set.
*/
public void setFieldWidth(int col, int width)
throws Exception {
	TableField field = _table_fields.get(col);
	field.setWidth(width);
}

/**
Set the number of records in the table.
This method should typically only be called when data are read on-the-fly
(and are not stored in memory in the table records).
@param num_records Number of records in the table.
*/
public void setNumberOfRecords ( int num_records ) {
	_num_records = num_records;
}

/**
Set field data type and header for the specified zero-based index.
@param index index of field to set
@param data_type data type; use TableField.DATA_TYPE_*
@param name name of the field.
*/
public void setTableField ( int index, int data_type, String name )
throws Exception {
	if ( _table_fields.size() <= index ) {
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
public void setTableFields ( List<TableField> tableFieldsList ) {
	_table_fields = tableFieldsList;
}

/**
Set the table field name.
@param index index of field to set (zero-based).
@param name Field name.
@exception If the index is out of range.
*/
public void setTableFieldName ( int index, String name )
throws Exception {
	if ( _table_fields.size() <= index ) {
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
throws Exception {
	if ( _table_fields.size() <= index ) {
		throw new Exception ( "Index " + index + " is not valid." );
	}
	TableField tableField = _table_fields.get(index);
	tableField.setDataType ( data_type );
}

/**
Set the table identifier.
@param table_id Identifier for the table
*/
public void setTableID ( String table_id ) {
    __table_id = table_id;
}

/**
Set values in the list of table records and setting values for specific columns in each record.
The records might exist in a table or may not yet have been added to the table.
The table records are modified directly, rather than trying to find the row in the table to modify.
This command is used, for example, when inserting table rows and it is desired to avoid
changing the rest of the table rows.
@param tableRecords list of TableRecord to set values in
@param columnValues map for columns values that will be set, where rows to be modified will be the result of the filters;
values are strings and need to be converged before setting, based on column type
@param getter a DataTableValueStringProvider implementation, which is called prior to setting values if not null,
used to provide ability to dynamically format the values being set in the table
@param createColumns indicates whether new columns should be created if necessary
(currently ignored due to need to synchronize the table records and full table)
*/
public void setTableRecordValues ( List<TableRecord> tableRecords, HashMap<String,String> columnValues,
	DataTableValueStringProvider getter, boolean createColumns ) {
	String routine = getClass().getSimpleName() + ".setTableRecordValues";
	if ( tableRecords == null ) {
		return;
	}
    // List of columns that will be set, taken from keys in the column values.
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    // Get the column numbers and values to to set.
    String [] columnNamesToSet = new String[columnValues.size()];
    String [] columnValuesToSet = new String[columnValues.size()];
    int [] columnNumbersToSet = new int[columnValues.size()];
    int [] columnTypesToSet = new int[columnValues.size()];
    int ikey = -1;
    for ( Map.Entry<String,String> pairs: columnValues.entrySet() ) {
        columnNumbersToSet[++ikey] = -1;
        try {
            columnNamesToSet[ikey] = pairs.getKey();
            columnValuesToSet[ikey] = pairs.getValue();
            columnNumbersToSet[ikey] = getFieldIndex(columnNamesToSet[ikey]);
            columnTypesToSet[ikey] = getFieldDataType(columnNumbersToSet[ikey]);
            //Message.printStatus(2,routine,"Setting column \"" + columnNamesToSet[ikey] + " " + columnNumbersToSet[ikey] + "\"");
        }
        catch ( Exception e ) {
            // OK, will add the column below.
        }
    }
    // If necessary, add columns to the table and records.  For now, always treat as strings.
    // TODO SAM 2013-08-06 Evaluate how to handle other data types in set.
    //TableField newTableField;
    // Create requested columns in the output table.
    for ( int icol = 0; icol < columnNumbersToSet.length; icol++ ) {
        if ( (columnNumbersToSet[icol] < 0) && createColumns ) {
        	errorMessage.append("  createColumns=true is not yet supported.");
        	/*
            // Did not find the column in the table so add a String column for null values.
            newTableField = new TableField(TableField.DATA_TYPE_STRING, columnNamesToSet[icol], -1, -1);
            // Add to the full table.
            columnNumbersToSet[icol] = addField(newTableField, null );
            columnTypesToSet[icol] = getFieldDataType(columnNumbersToSet[icol]);
            */
        }
    }
    // Now loop through all the provided data records and set values.
    int icol;
    for ( TableRecord rec : tableRecords ) {
        String columnValueToSet = null; // A single value to set, may contain formatting such as ${Property} when used with TSTool.
        for ( icol = 0; icol < columnNumbersToSet.length; icol++ ) {
            try {
                // OK if setting to null value, but hopefully should not happen.
                // TODO SAM 2013-08-06 Handle all column types.
                //Message.printStatus(2,routine,"Setting ColNum=" + columnNumbersToSet[icol] + " RowNum=" + irow + " value=" +
                //    columnValues.get(columnNamesToSet[icol]));
                if ( columnNumbersToSet[icol] >= 0 ) {
                	columnValueToSet = columnValuesToSet[icol];
                	if ( getter != null ) {
                		// columnValueToSet will initially have formatting information like ${Property}.
                		columnValueToSet = getter.getTableCellValueAsString(columnValueToSet);
                	}
                    if ( columnTypesToSet[icol] == TableField.DATA_TYPE_INT ) {
                        // TODO SAM 2013-08-26 Should parse the values once rather than each time set to improve error handling and performance.
                        rec.setFieldValue(columnNumbersToSet[icol], Integer.parseInt(columnValueToSet) );
                    }
                    else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_DATETIME ) {
                        // TODO SAM 2013-08-26 Should parse the values once rather than each time set to improve error handling and performance.
                        rec.setFieldValue(columnNumbersToSet[icol], DateTime.parse(columnValueToSet) );
                    }
                    else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_DOUBLE ) {
                        // TODO SAM 2013-08-26 Should parse the values once rather than each time set to improve error handling and performance.
                        rec.setFieldValue(columnNumbersToSet[icol], Double.parseDouble(columnValueToSet) );
                    }
                    else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_STRING ) {
                        rec.setFieldValue(columnNumbersToSet[icol], columnValueToSet);
                    }
                    else {
                        errorMessage.append("Do not know how to set column data for type (" + TableColumnType.valueOf(columnTypesToSet[icol]) +
                            ") for column \"" + columnNamesToSet[icol] + "].");
                        ++errorCount;
                    }
                }
            }
            catch ( Exception e ) {
                // Should not happen.
                errorMessage.append("Error setting table record [" + columnNumbersToSet[icol] + "] (" + e + ").");
                Message.printWarning(3, routine, "Error setting new table data for [" + columnNumbersToSet[icol] + "] (" + e + ")." );
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
Set values in the table by first matching rows using column filters
(default is match all) and then setting values for specific columns.
This method is called by overloaded versions that specify either column filters or lists of records.
@param columnFilters map to filter rows to set values in
@param columnValues map for columns values that will be set, where rows to be modified will be the result of the filters;
values are strings and need to be converged before setting, based on column type
@param getter a DataTableValueStringProvider implementation, which is called prior to setting values if not null,
used to provide ability to dynamically format the values being set in the table
@param createColumns indicates whether new columns should be created if necessary
*/
public void setTableValues ( Hashtable<String,String> columnFilters, HashMap<String,String> columnValues,
	DataTableValueStringProvider getter, boolean createColumns ) {
    String routine = getClass().getSimpleName() + ".setTableValues";
    // List of columns that will be set, taken from keys in the column values.
    int errorCount = 0;
    StringBuffer errorMessage = new StringBuffer();
    // Get filter columns and glob-style regular expressions.
    int [] columnNumbersToFilter = new int[columnFilters.size()];
    String [] columnFilterGlobRegex = new String[columnFilters.size()];
    Enumeration<String> keys = columnFilters.keys();
    int ikey = -1;
    String key = null;
    while ( keys.hasMoreElements() ) {
        ++ikey;
        columnNumbersToFilter[ikey] = -1;
        try {
            key = keys.nextElement();
            columnNumbersToFilter[ikey] = getFieldIndex(key);
            columnFilterGlobRegex[ikey] = columnFilters.get(key);
            // Turn default globbing notation into internal Java regex notation.
            columnFilterGlobRegex[ikey] = columnFilterGlobRegex[ikey].replace("*", ".*").toUpperCase();
        }
        catch ( Exception e ) {
            ++errorCount;
            if ( errorMessage.length() > 0 ) {
                errorMessage.append(" ");
            }
            errorMessage.append ( "Filter column \"" + key + "\" not found in table.");
        }
    }
    // Get the column numbers and values to to set.
    String [] columnNamesToSet = new String[columnValues.size()];
    String [] columnValuesToSet = new String[columnValues.size()]; // Values as strings, to be parsed.
    Object [] columnObjectsToSet = new Object[columnValues.size()]; // Values as objects, after parsing, set on first row with set.
    int [] columnNumbersToSet = new int[columnValues.size()];
    int [] columnTypesToSet = new int[columnValues.size()]; // Can be array type with specific type extracted below.
    ikey = -1;
    for ( Map.Entry<String,String> pairs: columnValues.entrySet() ) {
        columnNumbersToSet[++ikey] = -1;
        try {
            columnNamesToSet[ikey] = pairs.getKey();
            columnValuesToSet[ikey] = pairs.getValue();
            columnNumbersToSet[ikey] = getFieldIndex(columnNamesToSet[ikey]);
            columnTypesToSet[ikey] = getFieldDataType(columnNumbersToSet[ikey]);
            //Message.printStatus(2,routine,"Setting column \"" + columnNamesToSet[ikey] + " " + columnNumbersToSet[ikey] + "\"");
        }
        catch ( Exception e ) {
            // OK, will add the column below.
        }
    }
    // If necessary, add columns to the table.  For now, always treat as strings.
    // TODO SAM 2013-08-06 Evaluate how to handle other data types in set.
    TableField newTableField;
    // Create requested columns in the output table.
    for ( int icol = 0; icol < columnNumbersToSet.length; icol++ ) {
        if ( (columnNumbersToSet[icol] < 0) && createColumns ) {
            // Did not find the column in the table so add a String column for null values.
            newTableField = new TableField(TableField.DATA_TYPE_STRING, columnNamesToSet[icol], -1, -1);
            columnNumbersToSet[icol] = addField(newTableField, null );
            columnTypesToSet[icol] = getFieldDataType(columnNumbersToSet[icol]);
        }
    }
    // Now loop through all the data records and set values if rows are matched.
    int icol;
    boolean filterMatches;
    Object o = null;
    String s;
    // Number of rows that have been set, useful for troubleshooting and used to control parsing:
    // - do not "continue" or "break" in logic below in a way that interferes with this counter
    // - process each row completely or not all all to increment counter
    int rowSetCount = 0;
    for ( int irow = 0; irow < getNumberOfRecords(); irow++ ) {
        filterMatches = true; // Default to matching the column and set to false with checks below.
        if ( columnNumbersToFilter.length > 0 ) {
            // Filters can be done on any columns so loop through to see if row matches before doing set>
            for ( icol = 0; icol < columnNumbersToFilter.length; icol++ ) {
                if ( columnNumbersToFilter[icol] < 0 ) {
                    filterMatches = false;
                    break;
                }
                try {
                    o = getFieldValue(irow, columnNumbersToFilter[icol]);
                    if ( o == null ) {
                        filterMatches = false;
                        break; // Don't include nulls when checking values.
                    }
                    s = ("" + o).toUpperCase();
                    if ( !s.matches(columnFilterGlobRegex[icol]) ) {
                        // A filter did not match so don't process the record.
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
        String columnValueToSet = null; // A single value to set, may contain formatting such as ${Property} when used with TSTool.
        for ( icol = 0; icol < columnNumbersToSet.length; icol++ ) {
            try {
                // OK if setting to null value, but hopefully should not happen.
                //Message.printStatus(2,routine,"Setting ColNum=" + columnNumbersToSet[icol] + " RowNum=" + irow + " value=" +
                //    columnValues.get(columnNamesToSet[icol]));
                if ( columnNumbersToSet[icol] >= 0 ) {
                	// The column number was determined above so can set.
                	columnValueToSet = columnValuesToSet[icol];
                	if ( getter != null ) {
                		// columnValueToSet may initially have formatting information like ${Property}:
                		// - using local variable will cause reevaluation if necessary
                		// - TODO smalers 2019-10-02 need to evaluate how this impacts (re)parsing logic below
                		columnValueToSet = getter.getTableCellValueAsString(columnValueToSet);
                	}
               		if ( rowSetCount == 0 ) {
               			// First row has not been processed so need to determine the objects to set.
                        if ( isColumnArray(columnTypesToSet[icol]) ) {
                    	    // Array string will have format "[value1,value2,...]"
                    	    // Since an array type the actual data type is determined by subtracting the array type.
                    	    int dataTypeForArray = columnTypesToSet[icol] - TableField.DATA_TYPE_ARRAY;
                    	    // For now only handle simple formats and assume no special characters
                    	    // (e.g., commas are delimiters but not included in data values).
                    	    // Strip the array brackets.
                    	    String arrayValue = columnValueToSet.replace("[", "").replace("]", "");
                    	    String[] arrayValueParts = arrayValue.split(",");
                    	    Object [] arrayValueO = null; // Array that is parsed from input strings, used to avoid re-parsing.
                    	    // Allocate the correct array type below because instanceOf may be used elsewhere to check type:
                    	    // - in other words don't allocate array of Object
                    	    for ( int ia = 0; ia < arrayValueParts.length; ia++ ) {
                    	    	if ( dataTypeForArray == TableField.DATA_TYPE_BOOLEAN ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Boolean[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                                		arrayValueO[ia] = Boolean.parseBoolean(arrayValueParts[ia]);
                                	}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_DATETIME ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new DateTime[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                                        arrayValueO[ia] = DateTime.parse(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_DOUBLE ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Double[arrayValueParts.length];
                                	}
                                	if ( arrayValueParts[ia] == null ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                                	else if ( arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                                	}
                                	else if ( arrayValueParts[ia].equalsIgnoreCase("NaN") ) {
                                		 arrayValueO[ia] = Double.NaN;
                                	}
                    	    		else {
                    	    			arrayValueO[ia] = Double.parseDouble(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_FLOAT ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Float[arrayValueParts.length];
                                	}
                                	if ( arrayValueParts[ia] == null ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                                	else if ( arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                                	}
                                	else if ( arrayValueParts[ia].equalsIgnoreCase("NaN") ) {
                                		 arrayValueO[ia] = Float.NaN;
                                	}
                    	    		else {
                    	    			arrayValueO[ia] = Float.parseFloat(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_INT ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Integer[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                    	    			arrayValueO[ia] = Integer.parseInt(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_LONG ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Long[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                    	    			arrayValueO[ia] = Long.parseLong(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_SHORT ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new Short[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].isEmpty() || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		 arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                    	    			arrayValueO[ia] = Short.parseShort(arrayValueParts[ia]);
                    	    		}
                                }
                                else if ( dataTypeForArray == TableField.DATA_TYPE_STRING ) {
                                	if ( ia == 0 ) {
                                		// Allocate array of the correct type.
                                		arrayValueO = new String[arrayValueParts.length];
                                	}
                                	if ( (arrayValueParts[ia] == null) || arrayValueParts[ia].equalsIgnoreCase("null") ) {
                                		// Empty strings are OK.
                                		arrayValueO[ia] = null;
                    	    		}
                    	    		else {
                    	    			arrayValueO[ia] = arrayValueParts[ia];
                    	    		}
                                }
                                else {
                                    errorMessage.append("Do not know how to set column for array data type (" + TableColumnType.valueOf(dataTypeForArray) +
                                        ") for column \"" + columnNamesToSet[icol] + "].");
                                    ++errorCount;
                                }
                    	    }
                    	    // The field value is the array of objects:
                    	    // - to ensure that data are separate, create a new instance of the array each time
                    	    // - TODO smalers 2019-10-02 if the column getter/formatter will change each time due to property evaluations,
                    	    //   need to use a dynamic array, maybe check for a non-null dynamic array in this case?
                            columnObjectsToSet[icol] = arrayValueO;
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_BOOLEAN ) {
                            columnObjectsToSet[icol] = Boolean.parseBoolean(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_DATETIME ) {
                            columnObjectsToSet[icol] = DateTime.parse(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_DOUBLE ) {
                            columnObjectsToSet[icol] = Double.parseDouble(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_FLOAT ) {
                            columnObjectsToSet[icol] = Float.parseFloat(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_INT ) {
                            columnObjectsToSet[icol] = Integer.parseInt(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_LONG ) {
                            columnObjectsToSet[icol] = Long.parseLong(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_SHORT ) {
                            columnObjectsToSet[icol] = Short.parseShort(columnValueToSet);
                        }
                        else if ( columnTypesToSet[icol] == TableField.DATA_TYPE_STRING ) {
                            columnObjectsToSet[icol] = columnValueToSet;
                        }
                        else {
                            errorMessage.append("Do not know how to set column for data type (" + TableColumnType.valueOf(columnTypesToSet[icol]) +
                                ") for column \"" + columnNamesToSet[icol] + "].");
                            ++errorCount;
                        }
               		}
           			// The value to set will be defined from above so set.
                    if ( isColumnArray(columnTypesToSet[icol]) ) {
                    	// TODO smalers 2019-10-02 Arrays are not immutable, so need to clone the arrays.
                        setFieldValue(irow, columnNumbersToSet[icol], columnObjectsToSet[icol], true );
                    }
                    else {
                    	// Objects are immutable so don't need to clone them.
                        setFieldValue(irow, columnNumbersToSet[icol], columnObjectsToSet[icol], true );
                    }
                }
            }
            catch ( Exception e ) {
                // Should not happen.
                errorMessage.append("Error setting table data [" + irow + "][" + columnNumbersToSet[icol] + "] (" + e + ").");
                Message.printWarning(3, routine, "Error setting new table data for [" + irow + "][" +
                    columnNumbersToSet[icol] + "] (" + e + ")." );
                Message.printWarning(3, routine, e);
                ++errorCount;
            }
        }
        // If here the row was processed for setting.
        ++rowSetCount;
    }
    Message.printStatus(2, routine, "Updated " + rowSetCount + " rows with new values.");
    if ( errorCount > 0 ) {
        throw new RuntimeException ( "There were " + errorCount + " errors setting table values: " + errorMessage );
    }
}

/**
Sort the table rows by sorting a column's values.
@param sortColumns the name of the columns to be sorted, allowed to be integer, double, string, or DateTime type.
@param sortOrder order to sort (specify as 0+ to sort ascending and < 0 to sort descending)
@return the sort order array indicating the position in the original data
(useful if a parallel sort of data needs to occur), default is ascending
*/
public int [] sortTable ( String [] sortColumns, int [] sortOrder ) {
	String routine = getClass().getSimpleName() + ".sortTable";
    int [] sortColumnsNum = new int[sortColumns.length];
    if ( sortOrder == null ) {
    	// Default to ascending.
    	sortOrder = new int[sortColumns.length];
    	for ( int i = 0; i < sortOrder.length; i++ ) {
    		sortOrder[i] = 0;
    	}
    }
    // List of sort column names that are not found in the table.
    List<String> errors = new ArrayList<>();
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
    	StringBuilder b = new StringBuilder("The following column(s) to sort were not found in table \"" + getTableID() + "\": ");
    	for ( int i = 0; i < errors.size(); i++ ) {
    		if ( i > 0 ) {
    			b.append (",");
    		}
    		b.append("\"" + errors.get(i) + "\"");
    	}
    	throw new RuntimeException ( b.toString() );
    }
    int nrecords = getNumberOfRecords();
    int sortFlag = StringUtil.SORT_ASCENDING;
    if ( sortOrder[0] < 0 ) {
        sortFlag = StringUtil.SORT_DESCENDING;
    }
    int [] sortedOrderArray = new int[nrecords]; // Overall sort order different from original.
    // First sort by the first column.
    int iSort = 0;
    if ( getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_STRING ) {
        String value;
        List<String> values = new ArrayList<>(nrecords);
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
                // Should not happen but if it does it is probably bad.
                throw new RuntimeException ( e );
            }
        }
        StringUtil.sortStringList(values, sortFlag, sortedOrderArray, true, true);
    }
    else if ( getFieldDataType(sortColumnsNum[iSort]) == TableField.DATA_TYPE_DATE ) {
    	// Legacy Java Date.
        Object value;
        long [] values = new long[nrecords];
        int irec = -1;
        for ( TableRecord rec : getTableRecords() ) {
            ++irec;
            try {
                value = rec.getFieldValue(sortColumnsNum[iSort]);
                if ( value == null ) {
                    value = -Long.MAX_VALUE;
                }
                values[irec] = ((Date)value).getTime();
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad.
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortedOrderArray, true);
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
                // Should not happen but if it does it is probably bad.
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
                // Should not happen but if it does it is probably bad.
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
                // Should not happen but if it does it is probably bad.
                throw new RuntimeException ( e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortedOrderArray, true);
    }
    else {
        throw new RuntimeException ( "Sorting table only implemented for string, integer, double, float, Date and DateTime columns." );
    }
    // Shuffle the table's row list according to sortOrder.
    // Because other objects may have references to the tables record list, can't create a new list.
    // Therefore, copy the old list to a backup and then use that to sort into an updated original list.
    List<TableRecord> backup = new ArrayList<>(nrecords);
    List<TableRecord> records = this.getTableRecords();
    for ( TableRecord rec : records ) {
        backup.add ( rec );
    }
    // Now set from the backup to the original list.
    for ( int irec = 0; irec < nrecords; irec++ ) {
        records.set(irec, backup.get(sortedOrderArray[irec]) );
    }
    // Now sort by columns [1]+ (zero index).  Only sort the last column being iterated.
    // The previous columns are used to find blocks of rows to sort.
    // In other words, if 3 columns are sorted then columns [0-1] must match and then that block of rows is sorted based on column [2].
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
            // Check the current row's sort columns against the previous row.
            if ( sortValuesPrev == null ) {
            	// Initialize this row with values to be compared with the next row.
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
            	// Compare this row's values with the previous block of similar values.
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
	            		// The current row did not match so save the current row as the previous and break to indicate that the block needs sorted.
	            		//Message.printStatus(2,routine,"Record " + irec + " compare values did not match previous row." );
	            		break;
	            	}
	            }
	            // If all the values matched, can process another row before sorting, but check to see if at end of table below.
	            if ( sortColumnMatchCount == iSort ) {
	            	//Message.printStatus(2,routine,"Record " + irec + " sort columns match previous." );
	            	needToSort = false;
	            	blockEndRow = irec; // Advance the end of the block
	            }
	            else {
	            	// Current row's sort column values did not match so need to sort the block.
	            	//Message.printStatus(2,routine,"Record " + irec + " sort columns do not match previous.  Resetting \"previous\" values to this record." );
	            	needToSort = true;
	            	// Save the current row to compare with the next row.
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
	            	// Need to sort if in the last row unless the block was only one row.
	            	needToSort = true;
	            	//Message.printStatus(2, routine, "Need to sort end of table from " + blockStartRow + " to " + blockEndRow );
	            }
	            if ( needToSort ) {
	            	// Need to sort the block of rows using the "rightmost" sort column.
	            	//Message.printStatus(2, routine, "Need to sort block of rows from " + blockStartRow + " to " + blockEndRow );
	            	try {
	            		//Message.printStatus(2, routine, "Sorting rows from " + blockStartRow + " to " + blockEndRow + " based on column " + sortColumnNum[iSort] );
	            		sortTableSubset(blockStartRow,blockEndRow,sortColumnsNum[iSort],sortOrder[iSort],sortedOrderArray);
	            	}
	            	catch ( Exception e ) {
	            		Message.printWarning(3, routine, e);
	            		throw new RuntimeException(e);
	            	}
	            	// Now that the block has been started, reset for the next block.
	            	// blockStartRow should = irec since rec was different and triggered the sort of the previous block.
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
throws Exception {
	if ( blockStartRow == blockEndRow ) {
		// Only one row to sort.
		return;
	}
    int nrecords = blockEndRow - blockStartRow + 1; // Number of records in the block to sort.
    int [] sortOrderArray2 = new int[nrecords]; // Overall sort order different from original.
    // First sort by the first column.
    int sortFlag = StringUtil.SORT_ASCENDING;
    if ( sortOrder < 0 ) {
        sortFlag = StringUtil.SORT_DESCENDING;
    }
    if ( getFieldDataType(iCol) == TableField.DATA_TYPE_STRING ) {
        String value = null;
        List<String> values = new ArrayList<>(nrecords);
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
                // Should not happen but if it does it is probably bad.
                String message = "Error sorting table row [" + irec + "] string value=" + value;
                throw new RuntimeException ( message, e );
            }
        }
        StringUtil.sortStringList(values, sortFlag, sortOrderArray2, true, true);
    }
    else if ( getFieldDataType(iCol) == TableField.DATA_TYPE_DATE ) {
        Object value = null;
        double [] values = new double[nrecords];
        TableRecord rec;
        DateTime dt = null;
        for ( int irec = blockStartRow, pos = 0; irec <= blockEndRow; irec++, pos++ ) {
        	rec = getRecord(irec);
            try {
                value = rec.getFieldValue(iCol);
                if ( value == null ) {
                    value = -Double.MAX_VALUE;
                }
                if ( value instanceof Date ) {
                	dt = new DateTime((Date)value);
                	values[pos] = dt.toDouble();
                }
                else if ( value instanceof DateTime ) {
                	values[pos] = ((DateTime)value).toDouble();
                }
            }
            catch ( Exception e ) {
                // Should not happen but if it does it is probably bad.
                String message = "Error sorting table row [" + irec + "] date value=" + value;
                throw new RuntimeException ( message, e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else if ( getFieldDataType(iCol) == TableField.DATA_TYPE_DATETIME ) {
        Object value = null;
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
                // Should not happen but if it does it is probably bad.
                String message = "Error sorting table row [" + irec + "] date/time value=" + value;
                throw new RuntimeException ( message, e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else if ( (getFieldDataType(iCol) == TableField.DATA_TYPE_DOUBLE) ||
    	(getFieldDataType(iCol) == TableField.DATA_TYPE_FLOAT) ) {
    	Object o = null;
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
                // Should not happen but if it does it is probably bad.
            	if ( getFieldDataType(iCol) == TableField.DATA_TYPE_DOUBLE ) {
            		String message = "Error sorting table row [" + irec + "] double value=" + o;
            		throw new RuntimeException ( message, e );
            	}
            	else {
            		String message = "Error sorting table row [" + irec + "] float value=" + o;
            		throw new RuntimeException ( message, e );
            	}
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else if ( getFieldDataType(iCol) == TableField.DATA_TYPE_INT) {
        Integer value = null;
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
                // Should not happen but if it does it is probably bad.
                String message = "Error sorting table row [" + irec + "] integer value=" + value;
                throw new RuntimeException ( message, e );
            }
        }
        MathUtil.sort(values, MathUtil.SORT_QUICK, sortFlag, sortOrderArray2, true);
    }
    else {
        throw new RuntimeException ( "Sorting table only implemented for string, integer, double, float and DateTime columns." );
    }
    // Shuffle the table's row list according to sortOrder.
    // Because other objects may have references to the tables record list, can't create a new list.
    // Therefore, copy the old list to a backup and then use that to sort into an updated original list.
    List<TableRecord> backup = new ArrayList<>(nrecords);
    List<TableRecord> records = this.getTableRecords();
    TableRecord rec;
    for ( int irec = blockStartRow; irec <= blockEndRow; irec++ ) {
    	rec = getRecord(irec);
        backup.add ( rec );
    }
    // Now set from the backup to the original list.
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
public boolean trimStrings ( boolean trim_strings ) {
	_trim_strings = trim_strings;
	return _trim_strings;
}

/**
Indicate whether strings should be trimmed at read.
@return Boolean value indicating whether strings should be trimmed.
*/
public boolean trimStrings ( ) {
	return _trim_strings;
}

/**
Writes a table to a delimited file.  If the data items contain the delimiter,
they will be written surrounded by double quotes.
The overloaded version of this method is called.
@param filename the file to write
@param delimiter the delimiter between columns
@param writeColumnNames If true, the field names will be read from the fields
and written as a one-line header of field names.  The headers are double-quoted.
If all headers are missing, then the header line will not be written.
@param comments a list of Strings to put at the top of the file as comments,
@param commentLinePrefix prefix string for comment lines specify if incoming comment strings have not already been prefixed.
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
	HashMap<String,Object> writeProps = new HashMap<>();
	if ( alwaysQuoteStrings ) {
		writeProps.put("AlwaysQuoteStrings", "True");
	}
	else {
		writeProps.put("AlwaysQuoteStrings", "False");
	}
	if ( newlineReplacement != null ) {
		writeProps.put("NewlineReplacement", newlineReplacement);
	}
	if ( NaNValue != null ) {
		writeProps.put("NaNValue", NaNValue);
	}
	// Call the generic version that takes list of properties.
	writeDelimitedFile(filename, delimiter, writeColumnNames, comments, commentLinePrefix, writeProps );
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
@param commentLinePrefix prefix string for comment lines specify if incoming comment strings have not already been prefixed.
@param writeProps additional properties to control writing:
<ul>
<li>AlwaysQuoteDateTimes - if true, then always surround date/times with double quotes;
if false date/times will only be quoted when they include the delimiter</li>
<li>AlwaysQuoteStrings - if true, then always surround strings with double quotes;
if false strings will only be quoted when they include the delimiter</li>
<li>Append - if "true" (string) append to the file without writing the header, if "false" (default) create a new file</li>
<li>IncludeColumns - array of String containing column names to include</li>
<li>ExcludeColumns - array of String containing column names to exclude</li>
<li>NaNValue - value to replace NaN in output (no property or null will result in NaN being written).</li>
<li>NewlineReplacement - if not null, replace newlines in string table values with this replacement string
(which can be an empty string).  This is needed to ensure that the delimited file does not include unexpected
newlines in mid-row.  Checks are done for \r\n, then \n, then \r to catch all combinations.
This can be a performance hit and mask data issues so the default is to NOT replace newlines.</li>
</ul>
*/
public void writeDelimitedFile(String filename, String delimiter, boolean writeColumnNames, List<String> comments,
    String commentLinePrefix, HashMap<String,Object> writeProps )
throws Exception {
	String routine = getClass().getSimpleName() + ".writeDelimitedFile";

	if (filename == null) {
		Message.printWarning(1, routine, "Cannot write to file '" + filename + "'");
		throw new Exception("Cannot write to file '" + filename + "'");
	}
	if ( comments == null ) {
	    comments = new ArrayList<>(); // To simplify logic below.
	}
	String commentLinePrefix2 = commentLinePrefix;
	if ( !commentLinePrefix.equals("") ) {
	    commentLinePrefix2 = commentLinePrefix + " "; // Add space for readability.
	}

	// Check whether should append.
	boolean append = false;
	Object propO = writeProps.get("Append");
	if ( (propO != null) && ((String)propO).equalsIgnoreCase("true") ) {
		append = true;
	}

	// Output string to use for NaN values.
	propO = writeProps.get("NaNValue");
	String NaNValue = "NaN"; // Default.
	if ( propO != null ) {
	    NaNValue = (String)propO;
	}

	// Indicate whether strings should always be quoted:
	// - default is to not quote date/times
	boolean alwaysQuoteDateTimes = false;
	propO = writeProps.get("AlwaysQuoteDateTimes");
	String AlwaysQuoteDateTimes = null;
	if ( propO != null ) {
		AlwaysQuoteDateTimes = (String)propO;
	}
	if ( (AlwaysQuoteDateTimes != null) && AlwaysQuoteDateTimes.equalsIgnoreCase("true") ) {
		alwaysQuoteDateTimes = true;
	}

	// Indicate whether strings should always be quoted:
	// - default is to only quote if string includes delimiter
	boolean alwaysQuoteStrings = false;
	String AlwaysQuoteStrings = null;
	propO = writeProps.get("AlwaysQuoteStrings");
	if ( propO != null ) {
		AlwaysQuoteStrings = (String)propO;
	}
	if ( (AlwaysQuoteStrings != null) && AlwaysQuoteStrings.equalsIgnoreCase("true") ) {
		alwaysQuoteStrings = true;
	}

	// String to use for newlines, can be "", by default don't replace:
	// - default is to not replace newlines
	propO = writeProps.get("NewlineReplacement");
	String NewlineReplacement = null;
	if ( propO != null ) {
		NewlineReplacement = (String)propO;
	}
	String newlineReplacement = null;
	if ( NewlineReplacement != null ) {
		newlineReplacement = NewlineReplacement;
	}

	// Check whether include and exclude columns are indicated.
	String [] includeColumns = new String[0];
	String [] excludeColumns = new String[0];
	propO = writeProps.get("IncludeColumns");
	if ( propO != null ) {
		includeColumns = (String [])propO;
	}
	propO = writeProps.get("ExcludeColumns");
	if ( propO != null ) {
		excludeColumns = (String [])propO;
	}

	PrintWriter out = null;
	if ( append ) {
		// Open in append mode.
		out = new PrintWriter( new BufferedWriter(new FileWriter(filename,true)));
	}
	else {
		// Open in write (not append) mode.
		out = new PrintWriter( new BufferedWriter(new FileWriter(filename)));
	}
	int irow = 0, icol = 0;
	try {
    	// If any comments have been passed in, write them at the top of the file.
		// Do not write if in append mode.
    	if ( !append && (comments != null) && (comments.size() > 0) ) {
    		for ( String comment : comments ) {
    			out.println(commentLinePrefix2 + comment );
    		}
    	}

    	int cols = getNumberOfFields();
    	if (cols == 0) {
    		Message.printWarning(3, routine, "Table has 0 columns!  Nothing will be written.");
    		return;
    	}

    	// Determine which columns should be written:
    	// - default is to write all
       	boolean [] columnOkToWrite = determineColumnsToInclude(includeColumns, excludeColumns);

    	StringBuffer line = new StringBuffer();

        int nonBlank = 0; // Number of non-blank table headings.
    	if ( writeColumnNames && !append ) {
    	    // First determine if any headers are non blank:
    		// - only write headers if requested and have at least one non-blank header
    		// - TODO smalers 2021-10-24 is this left over?  All columns should typically have names for lookups.
            for ( icol = 0; icol < cols; icol++) {
            	if ( columnOkToWrite[icol] ) {
            		if ( getFieldName(icol).length() > 0 ) {
            			++nonBlank;
            		}
            	}
            }
            if ( nonBlank > 0 ) {
            	// Write the column headings.
        		line.setLength(0);
        		int iColOut = 0; // Count of columns written.
        		for ( icol = 0; icol < cols; icol++) {
        			if ( columnOkToWrite[icol] ) {
        				// Count of output columns, so know when to print the delimiter.
        				++iColOut;
        				if ( iColOut > 1 ) {
        					// Add a delimiter after the first column.
        					line.append(delimiter);
        				}
        				line.append( "\"" + getFieldName(icol) + "\"");
        			}
        		}
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
    	boolean doQuoteCell = false; // Whether a single cell should have surrounding quotes.
    	int icolOut = 0; // Count of columns actually written.
    	for ( irow = 0; irow < rows; irow++) {
    		line.setLength(0);
    		icolOut = 0;
    		for ( icol = 0; icol < cols; icol++) {
       			if ( !columnOkToWrite[icol] ) {
       				continue;
       			}
       			++icolOut;
    		    if ( icolOut > 1 ) {
    		        line.append ( delimiter );
    		    }
    		    tableFieldType = getFieldDataType(icol);
    		    precision = getFieldPrecision(icol);
    		    fieldValue = getFieldValue(irow,icol);
    		    if ( fieldValue == null ) {
    		        cell = "";
    		    }
    		    else if ( isColumnArray(tableFieldType) ) {
                	// The following formats the array for display in UI table.
                	cell = formatArrayColumn(irow,icol);
                }
    		    else if ( tableFieldType == TableField.DATA_TYPE_FLOAT ) {
    		    	// Handle specifically in order to format precision and handle NaN value.
                    fieldValueFloat = (Float)fieldValue;
                    if ( fieldValueFloat.isNaN() ) {
                        cell = NaNValue;
                    }
                    else if ( precision >= 0 ) {
                        // Format according to the precision if floating point.
                        cell = StringUtil.formatString(fieldValue,"%." + precision + "f");
                    }
                    else {
                        // Use default formatting.
                        cell = "" + fieldValue;
                    }
    		    }
    		    else if ( tableFieldType == TableField.DATA_TYPE_DOUBLE ) {
    		    	// Handle specifically in order to format precision and handle NaN value.
    		        fieldValueDouble = (Double)fieldValue;
    		        if ( fieldValueDouble.isNaN() ) {
    		            cell = NaNValue;
    		        }
    		        else if ( precision >= 0 ) {
                        // Format according to the precision if floating point.
                        cell = StringUtil.formatString(fieldValue,"%." + precision + "f");
    		        }
    		        else {
    		            // Use default formatting.
                        cell = "" + fieldValue;
    		        }
                }
                else {
                    // Use default formatting from object toString().
                    cell = "" + fieldValue;
                }
    		    // Figure out if the initial cell needs to be quoted.
    			// Surround the values with double quotes if:
    		    // 1) the field contains the delimiter
    		    // 2) alwaysQuoteStrings=true
    		    // 3) the field contains a double quote (additionally replace " with "")
    		    doQuoteCell = false;
    		    if ( tableFieldType == TableField.DATA_TYPE_STRING ) {
    		    	if ( cell.indexOf("\"") > -1 ) {
    		    		// Cell includes a double quote so quote the whole thing.
    		    		doQuoteCell = true;
    		    	}
    		    	else if ( alwaysQuoteStrings ) {
    		    		// Calling code requests quoting strings always.
    		    		doQuoteCell = true;
    		    	}
    		    }
    		    else if ( tableFieldType == TableField.DATA_TYPE_DATETIME ) {
    		    	if ( cell.indexOf("\"") > -1 ) {
    		    		// Cell includes a double quote so quote the whole thing.
    		    		doQuoteCell = true;
    		    	}
    		    	else if ( alwaysQuoteDateTimes ) {
    		    		// Calling code requests quoting date/times always.
    		    		doQuoteCell = true;
    		    	}
    		    }
    			if ( (cell.indexOf(delimiter) > -1) ) {
    				// Always have to protect delimiter character in the cell string.
    				doQuoteCell = true;
    			}
    			if ( doQuoteCell ) {
    				// First replace all single \" instances with double.
    				cell = cell.replace("\"", "\"\"");
    				// Then add quotes around the whole thing.
    				cell = "\"" + cell + "\"";
    			}
    			if ( (tableFieldType == TableField.DATA_TYPE_STRING) && (newlineReplacement != null) ) {
    			    // Replace newline strings with the specified string.
    			    cell = cell.replace("\r\n", newlineReplacement); // Windows/Mac use 2-characters.
    			    cell = cell.replace("\n", newlineReplacement); // *NIX
    			    cell = cell.replace("\r", newlineReplacement); // To be sure.
    			}
    			line.append ( cell );
    		}
    		out.println(line);
    	}
	}
	catch ( Exception e ) {
	    // Log and rethrow the exception.
	    Message.printWarning(3, routine, "Unexpected error writing delimited file row [" + irow + "][" + icol + "] (" + e + ")." );
	    Message.printWarning(3, routine, e);
	    throw ( e );
	}
	finally {
    	out.flush();
    	out.close();
	}
}

}