// DataTableComparer - compare two tables for differences and create a new table that contains the comparison

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;

/**
Compare two tables for differences and create a new table that contains the comparison.
This table can be output to a simple HTML format to provide a visual way to find specific differences.
The comparison is currently not very complicated.
Tables are assumed to have consistent column definitions and numbers of rows,
although the comparison is done on strings so column types don't necessarily have to be the same.
*/
public class DataTableComparer
{

// For now treat the following similar to static values since private.

/**
 * Comparison result = error.
 */
private final int CELL_ERROR = -1;

/**
 * Comparison result = unknown (initial value).
 */
private final int CELL_UNKNOWN = -99;

/**
 * Comparison result = same:
 * - use zero to simplify checks
 * - all values > 0 indicate a difference of some type
 */
private final int CELL_SAME = 0;

/**
 * Comparison result = different.
 */
private final int CELL_DIFFERENT = 1;

/**
 * Comparison result = no row in table 1, for advanced and simple analysis.
 */
private final int CELL_NO_ROW_TABLE1 = 2;

/**
 * Comparison result = no row in table 2, for advanced and simple analysis.
 */
private final int CELL_NO_ROW_TABLE2 = 3;

/**
 * Comparison result = empty row inserted in table 1, for advanced analysis.
 */
private final int CELL_INSERT_EMPTY_ROW_TABLE1 = 4;

/**
 * Comparison result = empty row inserted in table 2, for advanced analysis.
 */
private final int CELL_INSERT_EMPTY_ROW_TABLE2 = 5;

/**
 * Comparison result = only have data in table 1, for advanced and simple analysis.
 */
private final int CELL_ROW_ONLY_IN_TABLE1 = 6;

/**
 * Comparison result = only have data in table 2, for advanced and simple analysis.
 */
private final int CELL_ROW_ONLY_IN_TABLE2 = 7;

/**
The first table to be compared.
*/
private DataTable __table1;

/**
The second table to be compared.
*/
private DataTable __table2;

/**
The list of column names to compare from the first table.
*/
private List<String> __compareColumns1;

/**
The table positions for the columns being compared from the first table.
*/
//private int [] __columnNumbers1;

/**
The list of column names to compare from the second table.
*/
private List<String> __compareColumns2;

/**
The list of column names to match from the first table, used with advanced analysis.
*/
private List<String> __matchColumns1;

/**
The list of column names to match from the second table, used with advanced analysis.
*/
private List<String> __matchColumns2;

/**
The table positions for the columns being compared from the second table.
*/
//private int [] __columnNumbers2;

/**
The name of the first new comparison table to be created.
*/
private String __newTableID = "";

/**
The name of the second new comparison table to be created.
*/
private String __newTable2ID = "";

/**
The name of the final merged comparison (difference) table to be created.
*/
private String diffTableID = "";

/**
 * New column to add containing the original table row number.
 */
private String rowNumberColumn = null;

/**
 * Which rows to output ("All", "Different", or "Same").
 */
private String outputRows = "All";

/**
Whether to match columns by name (true) or order (false).
*/
private boolean __matchColumnsByName = true;

/**
 * Analysis type.
 */
private DataTableComparerAnalysisType analysisType = null;

/**
The precision to use when comparing floating point numbers.
*/
private Integer __precision = null;

/**
The tolerance to use when comparing floating point numbers.
*/
private Double __tolerance = null;

/**
The first difference table that is created.  Will be null if the analysis has not been run.
*/
private DataTable diffTable1 = null;

/**
The second comparison table that is created.  Will be null if the analysis has been run.
*/
private DataTable diffTable2 = null;

/**
 * The final difference table, created from 'diffTable1' and 'diffTable2'.
 * For simple analysis, the only result is a merged table (diffTable1).
 * For advanced analysis two comparison tables are initially always created.
 * If a merged comparison table is requested, then those two tables are merged into one.
 */
private DataTable diffTable = null;

/**
2D array with values for each table cell that indicates differences in the cells,
used for simple analysis where a single comparison table is used.
An array is used because it can be sized up front and will not be resized.
This is used for formatting output.
It is an integer and not boolean because in the future more care may be implemented
to allow tolerances in differences and consequently the table could be visualized with
different colors depending on the level of difference.
For each cell a value can be:
  -1 indicates an error
  0 indicates no difference and no error
  1 indicates different
  2 indicates no row in the original table 1
  3 indicates no row in the original table 2
*/
//private int [][] __differenceArray = null;

/**
Used with the first difference table.
2D list of lists with values for each cell that indicates differences in the cells, used for advanced analysis.
An array of list for each column is used because lists can be resized dynamically if rows are inserted.
This is used for formatting output.
It is an integer and not boolean because in the future more care may be implemented
to allow tolerances in differences and consequently the table could be visualized with
different colors depending on the level of difference.
For each cell a value can be:
  -1 indicates an error
  0 indicates no difference and no error
  1 indicates different
  4 indicates an inserted row in table 1 to align
  5 indicates an inserted row in table 2 to align
*/
private List<Integer>[] __differenceList1 = null;

/**
 * Used with the second difference table, similar to __differenceList1.
 */
private List<Integer>[] __differenceList2 = null;

/**
 * Used with the final difference table, similar to __differenceList1.
 */
private List<Integer>[] __differenceList = null;

/**
Create the data table comparer instance and check for initialization problems.
@param table1 first table for comparison
@param compareColumns1 list of column names from the first table to compare
@param excludeColumns1 list of column names from the first table to exclude
@param matchColumns1 list of column names from the first table to match rows, for advanced analysis
(removed from compareColumns1 if necessary)
@param table2 second table for comparison
@param compareColumns2 list of column names from the second table to compare
@param matchColumns2 list of column names from the second table to match rows, for advanced analysis
@param matchColumnsByName if true, then the column names are used to match columns for comparison,
using the columns from the first table as the main list; if false, then columns are matched by column position
@param analysisType the analysis type
@param precision the number of digits (1+) after the decimal point to compare numbers in floating point columns
(specify as null to ignore precision comparison)
@param tolerance the absolute value to check differences between floating point numbers (if not specified then
values must be exact when checked to the precision)
@param newTableID name of new table to create with comparison results
@param newTable2ID name of new table to create with comparison results, for advanced analysis
@param mergedTableID the name of an output table to merge the comparison tables, used with advanced analysis
@param rowNumberColumn name of table column to add to contain the original row number, useful for comparison
@param outputRows indicate which rows to output ("All" (default), "Different", or "Same")
*/
public DataTableComparer (
	DataTable table1,
	List<String>compareColumns1, List<String> excludeColumns1, List<String> matchColumns1,
    DataTable table2,
    List<String> compareColumns2, List<String> matchColumns2,
    boolean matchColumnsByName,
    DataTableComparerAnalysisType analysisType,
    Integer precision, Double tolerance,
    String newTableID, String newTable2ID,
    String diffTableID,
    String rowNumberColumn,
    String outputRows ) {
    // The tables being compared must not be null.
    if ( table1 == null ) {
        throw new InvalidParameterException( "The first table to compare is null." );
    }
    else {
        setTable1 ( table1 );
    }
    if ( table2 == null ) {
        throw new InvalidParameterException( "The second table to compare is null." );
    }
    else {
        setTable2 ( table2 );
    }
    // Get the column names to compare, which will either be those passed in by calling code,
    // or if not specified will be the full list.
    if ( (compareColumns1 == null) || (compareColumns1.size() == 0) ) {
        // Get all the columns from the first table.
        compareColumns1 = new ArrayList<>(Arrays.<String>asList(table1.getFieldNames()));
        // Remove the columns to be ignored.
        StringUtil.removeMatching(compareColumns1, excludeColumns1, true);
    }
    else {
        // Confirm that the requested columns exist.
        StringBuilder warning = new StringBuilder();
        for ( String column: compareColumns1 ) {
            try {
                table1.getFieldIndex(column);
            }
            catch ( Exception e ) {
                warning.append ( "; column1 to compare \"" + column + "\" does not exist in the first table" );
            }
        }
        if ( warning.length() > 0 ) {
            throw new InvalidParameterException( "Some columns to compare in the first table do not exist:  " +
                warning + "." );
        }
    }
    setCompareColumns1 ( compareColumns1 );
    if ( (compareColumns2 == null) || (compareColumns2.size() == 0) ) {
        // Get all the columns from the second table.
        compareColumns2 = new ArrayList<>(Arrays.<String>asList(table2.getFieldNames()));
    }
    else {
        // Confirm that the requested columns exist.
        StringBuilder warning = new StringBuilder();
        for ( String column: compareColumns2 ) {
            try {
                table2.getFieldIndex(column);
            }
            catch ( Exception e ) {
                warning.append ( "; column2 to compare \"" + column + "\" does not exist in the second table" );
            }
        }
        if ( warning.length() > 0 ) {
            throw new InvalidParameterException( "Some columns to compare in the second table do not exist:  " +
                warning + "." );
        }
    }
    setCompareColumns2 ( compareColumns2 );
    setMatchColumnsByName ( matchColumnsByName );

    // Check the match columns.
    int matchColumns1Size = 0;
    int matchColumns2Size = 0;
    if ( (matchColumns1 == null) || matchColumns1.isEmpty() ) {
    	// Match columns were not specified so use compare columns.
    	matchColumns1 = compareColumns1;
    }
    this.__matchColumns1 = matchColumns1;
    // Get the table column numbers for the match column names.
    StringBuilder warning = new StringBuilder();
    matchColumns1Size = matchColumns1.size();
    for ( String column: matchColumns1 ) {
        try {
            table1.getFieldIndex(column);
        }
        catch ( Exception e ) {
            warning.append ( "; column to match \"" + column + "\" does not exist in the first table" );
        }
    }
    if ( warning.length() > 0 ) {
        throw new InvalidParameterException( "Some columns to match in the first table do not exist:  " +
            warning + "." );
    }
    if ( (matchColumns2 == null) || matchColumns2.isEmpty() ) {
    	// Default to matchColumns1.
    	matchColumns2 = matchColumns1;
    }
    this.__matchColumns2 = matchColumns2;
    warning = new StringBuilder();
    matchColumns2Size = matchColumns2.size();
    if ( matchColumnsByName ) {
    	for ( String column: matchColumns2 ) {
        	try {
            	table2.getFieldIndex(column);
        	}
        	catch ( Exception e ) {
            	warning.append ( "; column to match \"" + column + "\" does not exist in the second table" );
        	}
    	}
    }
    if ( warning.length() > 0 ) {
        throw new InvalidParameterException( "Some columns to match in the second table do not exist:  " +
            warning + "." );
    }
    if ( matchColumns1Size != matchColumns2Size ) {
        throw new InvalidParameterException( "The number of table1 match columns (" + matchColumns1Size +
        	") is different than the number of table2 match columns (" + matchColumns2Size + ")." );
    }

    // The precision must be 0+.
    if ( (precision != null) && (precision < 0) ) {
        throw new InvalidParameterException( "The precision (" + precision + ") if specified must be >= 0).");
    }
    if ( analysisType == null ) {
    	analysisType = DataTableComparerAnalysisType.SIMPLE; // Default.
    }
    this.analysisType = analysisType;
    setPrecision ( precision );
    // The tolerance must be 0+.
    if ( (tolerance != null) && (tolerance < 0.0) ) {
        throw new InvalidParameterException( "The tolerance (" + tolerance + ") if specified must be >= 0).");
    }
    setTolerance ( tolerance );
    // The first new table ID must be specified because the table use is controlled by the calling code and
    // an identifier conflict because of an assumed name should not be introduced here.
    if ( (newTableID == null) || newTableID.equals("") ) {
        throw new InvalidParameterException( "The first new table ID is null or blank." );
    }
    else {
        setNewTableID ( newTableID );
    }
    // For advanced analysis,
    // the second new table ID must be specified because the table use is controlled by the calling code and
    // an assumed identifier should not be introduced here.
    if ( analysisType == DataTableComparerAnalysisType.ADVANCED ) {
    	if ( (newTable2ID == null) || newTable2ID.isEmpty() ) {
        	throw new InvalidParameterException( "The second new table ID is null or blank." );
    	}
    	else {
        	setNewTable2ID ( newTable2ID );
    	}
    }
    // Set the final difference table ID.
    this.diffTableID = diffTableID;
    // The row number column is optional.
    this.rowNumberColumn = rowNumberColumn;
    
    // Which rows should be output.
    this.outputRows = outputRows;
}

	/**
 	* Adjust the difference table and indicator list rows for requested output rows (all, same, or different).
 	* @param analysisType the analysis type
 	* @param doOutputDifferent whether to output different rows
 	* @param doOutputSame whether to output same rows
 	* @param rowNumberColumnOffset column offset if output includes a row number column
 	*/
    private void adjustDiffTableRows ( DataTableComparerAnalysisType analysisType,
    	boolean doOutputDifferent, boolean doOutputSame, int rowNumberColumnOffset ) {
    	String routine = getClass().getSimpleName() + ".adjustDiffTableRows";

    	if ( doOutputDifferent && doOutputSame ) {
    		// Requested output is different and same so don't need to adjust.
    		return;
    	}
    	
    	// If here then either same or different rows have been requested.

    	boolean ifSame = false;
    	boolean ifDifferent = false;
    	boolean needToRemove = false;
    	
    	DataTable diffTable = null;
    	List<Integer>[] differenceList = null;

    	// Loop through and adjust all the output tables.
    	for ( int iTable = 0; iTable < 3; iTable++ ) {
    		diffTable = null;
    		differenceList = null;
    		if ( iTable == 0 ) {
    			diffTable = this.diffTable1;
    			differenceList = this.__differenceList1;
    		}
    		else if ( iTable == 1 ) {
    			diffTable = this.diffTable2;
    			differenceList = this.__differenceList2;
    		}
    		else if ( iTable == 2 ) {
    			diffTable = this.diffTable;
    			differenceList = this.__differenceList;
    		}
    		if ( diffTable != null ) {
    			for ( int iRow = (diffTable.getNumberOfRecords() - 1); iRow >= 0; --iRow ) {
    				// Loop backwards so that rows can be removed without impacting the indices.
   					ifSame = ifRowSame ( diffTable, differenceList, iRow, rowNumberColumnOffset );
   					ifDifferent = ifRowDifferent ( diffTable, differenceList, iRow, rowNumberColumnOffset );
   					needToRemove = false;
    				if ( doOutputSame && !ifSame) {
    					// Only want same rows output.
    					needToRemove = true;
    				}
    				else if ( doOutputDifferent && !ifDifferent) {
    					// Only want different rows output.
    					needToRemove = true;
    				}
    				if ( needToRemove ) {
    					// Remove the data table row.
    					try {
    						diffTable.deleteRecord(iRow);
    					}
    					catch ( Exception e ) {
    						// Should not happen.
    						Message.printWarning(3, routine, "Error removing data table row.");
    					}
    					// Remove the indicator list rows for all columns.
    					for ( int iCol = 0; iCol < diffTable.getNumberOfFields(); iCol++ ) {
    						differenceList[iCol].remove(iRow);
    					}
    				}
    			}
    		}
    	}
    }

/**
Perform the comparison, creating the output table(s).
*/
public void compare ()
throws Exception {
    String routine = getClass().getSimpleName() + ".compare";
    // At this point the inputs should be OK so create a new table that has columns that
    // include both of the original column names but are of type string.
    DataTable table1 = getTable1();
    DataTable table2 = getTable2();
    DataTable diffTable1 = new DataTable ();
    diffTable1.setTableID ( getNewTableID() );
    DataTable diffTable2 = null;
    if ( this.analysisType == DataTableComparerAnalysisType.ADVANCED ) {
    	// Advanced comparison uses a second table.
    	diffTable2 = new DataTable ();
    	diffTable2.setTableID ( getNewTable2ID() );
    }

    // Set the comparison tables in the class so that they can be retrieved, even with partial results.
    setDiffTable1 ( diffTable1 );
    setDiffTable2 ( diffTable2 );

    boolean doRowNumberColumn = false; // Default.
	int rowNumberColumnOffset = 0;
    if ( (this.rowNumberColumn != null) && !this.rowNumberColumn.isEmpty() ) {
    	// Add a row number column to the comparison table(s).
    	doRowNumberColumn = true;
    	rowNumberColumnOffset = 1;
    }
    
    // What rows to output:
    // - default is to output all
    boolean doOutputSame = true;
    boolean doOutputDifferent = true;
    if ( this.outputRows.equalsIgnoreCase("Different") ) {
    	doOutputDifferent = true;
    	doOutputSame = false;
    }
    else if ( this.outputRows.equalsIgnoreCase("Same") ) {
    	doOutputDifferent = false;
    	doOutputSame = true;
    }

    // Local variables for compare columns.
    List<String> compareColumns1 = getCompareColumns1();
    List<String> compareColumns2 = getCompareColumns2();

    Message.printStatus(2, routine, "Table comparsion analysis type = " + analysisType );

    // Table 1 is the primary and consequently its indices will control the comparisons.
    int[] compareColumnNumbers1 = table1.getFieldIndices(StringUtil.toArray(compareColumns1));
    // Table 2 column numbers are first determined from the table.
    int[] compareColumnNumbers2 = table2.getFieldIndices(StringUtil.toArray(compareColumns2));
    Message.printStatus(2, routine, "Have " + compareColumns1.size() + " table 1 compare columns and "
    	+ compareColumns2.size() + " table 2 compare columns." );
    if ( getMatchColumnsByName() ) {
        // Order in column2 may not be the same as was originally specified.
        compareColumnNumbers2 = new int[compareColumnNumbers1.length];
        // Loop through the first tables columns and find the matching column in the second table.
        for ( int i = 0; i < compareColumns1.size(); i++ ) {
            try {
                compareColumnNumbers2[i] = table2.getFieldIndex(compareColumns1.get(i));
                Message.printStatus(2,routine,"Compare column [" + i + "] \"" + compareColumns1.get(i) +
                    "\" in table 1 matches column [" + compareColumnNumbers2[i] + "] \"" + compareColumns2.get(i) +
                    "\" in table 2.");
            }
            catch ( Exception e ) {
                compareColumnNumbers2[i] = -1; // Column not matched.
                Message.printStatus(2,routine,"Compare column [" + i + "] \"" + compareColumns1.get(i) +
                    "\" in table 1 does not match column \"" + compareColumns2.get(i) + "\" in table 2.");
            }
        }
    }
    else {
        // Make sure that the second table column number array has at least as many elements as
        // the first table array and use -1 for the array positions.
        if ( compareColumnNumbers2.length < compareColumnNumbers1.length ) {
            int [] columnNumbersTemp = new int[compareColumnNumbers1.length];
            // Initialize a longer array.
            for ( int i = 0; i < compareColumnNumbers1.length; i++ ) {
                columnNumbersTemp[i] = -1; // Default.
            }
            // Copy original shorter array into first part of the temporary array.
            System.arraycopy(compareColumnNumbers2, 0, columnNumbersTemp, 0, compareColumnNumbers2.length);
            compareColumnNumbers2 = columnNumbersTemp;
        }
    }

    // Get the column numbers for the match columns (for advanced analysis).
    int[] matchColumnNumbers1 = null;
    int[] matchColumnNumbers2 = null;
    int n1 = 0;
    int n2 = 0;
    if ( this.__matchColumns1 != null ) {
    	n1 = this.__matchColumns1.size();
    }
    if ( this.__matchColumns2 != null ) {
    	n2 = this.__matchColumns2.size();
    }
    Message.printStatus(2, routine, "Have " + n1 + " table 1 match columns and " + n2 + " table 2 match columns." );
    if ( (this.__matchColumns1 != null) && !this.__matchColumns1.isEmpty() ) {
        matchColumnNumbers1 = new int[this.__matchColumns1.size()];
        // Loop through the first table's match columns and find the matching column in the second table.
        for ( int i = 0; i < this.__matchColumns1.size(); i++ ) {
            try {
                matchColumnNumbers1[i] = table1.getFieldIndex(this.__matchColumns1.get(i));
                Message.printStatus(2,routine,"Match column [" + i + "] \"" + this.__matchColumns1.get(i) +
                    "\" in table 1 matches column [" + matchColumnNumbers1[i] + "].");
            }
            catch ( Exception e ) {
                matchColumnNumbers1[i] = -1; // Column not matched.
                Message.printStatus(2,routine,"Match column [" + i + "] \"" + this.__matchColumns1.get(i) +
                    "\" in table 1 does not match any column.");
            }
        }
    }
    if ( getMatchColumnsByName() ) {
    	// Only do the check if matching columns by name.
    	if ( (this.__matchColumns2 != null) && !this.__matchColumns2.isEmpty() ) {
        	matchColumnNumbers2 = new int[this.__matchColumns2.size()];
        	// Loop through the second table's match columns and find the matching column in the second table.
        	for ( int i = 0; i < this.__matchColumns2.size(); i++ ) {
            	try {
                	matchColumnNumbers2[i] = table2.getFieldIndex(this.__matchColumns2.get(i));
                	Message.printStatus(2,routine,"Match column [" + i + "] \"" + this.__matchColumns2.get(i) +
                    	"\" in table 2 matches column [" + matchColumnNumbers2[i] + "].");
            	}
            	catch ( Exception e ) {
                	matchColumnNumbers2[i] = -1; // Column not matched.
                	Message.printStatus(2,routine,"Match column [" + i + "] \"" + this.__matchColumns2.get(i) +
                    	"\" in table 2 does not match any column.");
            	}
        	}
    	}
    }
    
    Message.printStatus(2, routine,
    	"CompareColumns1.size=" + this.__compareColumns1.size() +
    	" CompareColumnNumbers1.length=" + compareColumnNumbers1.length +
    	" CompareColumns2.size=" + this.__compareColumns2.size() +
    	" CompareColumnNumbers2.length=" + compareColumnNumbers2.length
    );

    // C-style formats to convert compare column values to strings for comparison:
    // - these are in the position of the match columns in the original table
    String[] compareColumnFieldFormats1 = new String[this.__compareColumns1.size()];
    // Use the column numbers because the array is sized correctly above.
    String[] compareColumnFieldFormats2 = new String[compareColumnNumbers2.length];
    Integer precision = getPrecision();
    Double tolerance = getTolerance();
    for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
		// Get the format from the table properties (works for everything except for floating point).
    	compareColumnFieldFormats1[icol] = table1.getFieldFormat(compareColumnNumbers1[icol]);
  		// Set the precision for floating point columns.
    	if ( (precision != null) && (precision >= 0) ) {
        	// Update the field formats to use the requested precision, if a floating point field.
        	String fieldFormat = "%." + precision + "f";
           	if ( matchColumnNumbers1[icol] >= 0 ) {
           		if ( (table1.getFieldDataType(compareColumnNumbers1[icol]) == TableField.DATA_TYPE_DOUBLE) ||
                	(table1.getFieldDataType(compareColumnNumbers1[icol]) == TableField.DATA_TYPE_FLOAT) ) {
                	compareColumnFieldFormats1[compareColumnNumbers1[icol]] = fieldFormat;
            	}
           	}
        }
    }
    // Second column may default to names of the first column, which may not be correct when using column order,
    // so handle both cases.
    // TODO smalers 2024-02-26 need to figure out why the following have different sizes.
    //for ( int icol = 0; icol < compareColumnNumbers2.length; icol++ ) {
    for ( int icol = 0; icol < this.__compareColumns2.size(); icol++ ) {
		// Get the format from the table properties (works for everything except for floating point).
    	if ( getMatchColumnsByName() ) {
    		// Get the precision based on the column name.
    		compareColumnFieldFormats2[icol] = table2.getFieldFormat(compareColumnNumbers2[icol]);
    	}
    	else {
    		// Get the precision based on the column order.
    		compareColumnFieldFormats2[icol] = table2.getFieldFormat(icol);
    	}
    	if ( (precision != null) && (precision >= 0) ) {
    		// Set the precision for floating point columns.
        	String fieldFormat = "%." + precision + "f";
        	if ( getMatchColumnsByName() ) {
        		if ( compareColumnNumbers2[icol] >= 0 ) {
        			// Get the precision based on the column name.
        			if ( (table2.getFieldDataType(compareColumnNumbers2[icol]) == TableField.DATA_TYPE_DOUBLE) ||
        				(table2.getFieldDataType(compareColumnNumbers2[icol]) == TableField.DATA_TYPE_FLOAT) ) {
        				compareColumnFieldFormats2[compareColumnNumbers2[icol]] = fieldFormat;
        			}
        		}
        	}
        	else {
        		// Get the precision based on the column order.
        		if ( (table2.getFieldDataType(icol) == TableField.DATA_TYPE_DOUBLE) ||
        			(table2.getFieldDataType(icol) == TableField.DATA_TYPE_FLOAT) ) {
        			compareColumnFieldFormats2[icol] = fieldFormat;
        		}
            }
        }
    }

    // C-style formats to convert match column values to strings for comparison:
    // - these are in the position of the match columns in the original table
    String[] matchColumnFieldFormats1 = new String[this.__matchColumns1.size()];
    String[] matchColumnFieldFormats2 = new String[this.__matchColumns2.size()];
    for ( int icol = 0; icol < matchColumnFieldFormats1.length; icol++ ) {
		// Get the format from the table properties (works for everything except for floating point).
    	matchColumnFieldFormats1[icol] = table1.getFieldFormat(matchColumnNumbers1[icol]);
    	if ( (precision != null) && (precision >= 0) ) {
        	// Update the field formats to use the requested precision, if a floating point field.
           	if ( matchColumnNumbers1[icol] >= 0 ) {
           		String fieldFormat = "%." + precision + "f";
           		if ( (table1.getFieldDataType(matchColumnNumbers1[icol]) == TableField.DATA_TYPE_DOUBLE) ||
             		(table1.getFieldDataType(matchColumnNumbers1[icol]) == TableField.DATA_TYPE_FLOAT) ) {
             		matchColumnFieldFormats1[matchColumnNumbers1[icol]] = fieldFormat;
           		}
           	}
       	}
    }
    if ( matchColumnNumbers2 != null ) {
    	for ( int icol = 0; icol < matchColumnNumbers2.length; icol++ ) {
			// Get the format from the table properties (works for everything except for floating point).
    		matchColumnFieldFormats2[icol] = table2.getFieldFormat(matchColumnNumbers2[icol]);
    		if ( (precision != null) && (precision >= 0) ) {
        		// Update the field formats to use the requested precision, if a floating point field.
        		String fieldFormat = "%." + precision + "f";
           		if ( matchColumnNumbers2[icol] >= 0 ) {
               		if ( (table2.getFieldDataType(matchColumnNumbers2[icol]) == TableField.DATA_TYPE_DOUBLE) ||
                   		(table2.getFieldDataType(matchColumnNumbers2[icol]) == TableField.DATA_TYPE_FLOAT) ) {
                   		matchColumnFieldFormats2[matchColumnNumbers2[icol]] = fieldFormat;
               		}
        		}
    		}
    	}
    }

    // Create tracking array/list with values for each cell to track whether the cells are different:
    // - see CELL_* for valid values
    // - initial value is CELL_SAME
    // - this is used as a style mask when formatting the HTML
    // - create as the maximum number of rows in case the tables have different number of rows
    /*
    if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
    	// Simple analysis:
    	// - can use fixed size array
    	int maxRows = Math.max(table1.getNumberOfRecords(), table2.getNumberOfRecords() );
    	int numCols = compareColumns1.size();
    	if ( doRowNumberColumn ) {
    		++numCols;
    	}
    	this.__differenceArray = new int[maxRows][numCols];
    	// Initialize to same:
    	// - will set to other values as the comparison is done
    	for ( int i = 0; i < maxRows; i++ ) {
    		for ( int j = 0; j < numCols; j++ ) {
               	setCellDifferenceIndicator1 ( i, j, this.CELL_UNKNOWN );
    		}
    	}
    }
    else {
    */
    	// Advanced analysis:
    	// - analysis may insert blank rows to align data
    	// - initialize to the first table's size, one list per comparison column
    	int numCols = compareColumns1.size() + rowNumberColumnOffset;

    	// Allocate the first difference list.
   		this.__differenceList1 = new ArrayList[numCols];
   		for ( int icol = 0; icol < numCols; icol++ ) {
   			// Create a List<Integer> for every column:
   			// - this results in list arrays than if lists are added for each row
   			this.__differenceList1[icol] = new ArrayList<Integer>(table1.getNumberOfRecords());
   			for ( int irow = 0; irow < table1.getNumberOfRecords(); irow++ ) {
   				// Initialize to unknown:
    			// - will set to other values as the comparison is done
   				setCellDifferenceIndicator1(irow, icol, this.CELL_UNKNOWN);
   			}
   		}

    	// Allocate the second difference list.
   		this.__differenceList2 = new ArrayList[numCols];
   		for ( int icol = 0; icol < numCols; icol++ ) {
   			// Create a List<Integer> for every column:
   			// - this results in list arrays than if lists are added for each row
   			this.__differenceList2[icol] = new ArrayList<Integer>(table2.getNumberOfRecords());
   			for ( int irow = 0; irow < table2.getNumberOfRecords(); irow++ ) {
   				// Initialize to unknown:
    			// - will set to other values as the comparison is done
   				setCellDifferenceIndicator2(irow, icol, this.CELL_UNKNOWN);
   			}
   		}
    //}

    // If requested, add a new column containing the original row number:
    // - all column positions will need to be offset by 'rowNumberColumnOffset'
    if ( doRowNumberColumn ) {
    	if ( diffTable1 != null ) {
      		int newField = diffTable1.addField(new TableField(TableField.DATA_TYPE_INT, rowNumberColumn, -1), "");
       		diffTable1.getTableField(newField).setDescription("Original table 1 row number.");
    	}
    	if ( diffTable2 != null ) {
      		int newField = diffTable2.addField(new TableField(TableField.DATA_TYPE_INT, rowNumberColumn, -1), "");
       		diffTable2.getTableField(newField).setDescription("Original table 2 row number.");
    	}
    }

    // For the comparison tables, loop through the compare column lists,
    // which should be the same size for tables 1 and 2, and define columns.
    for ( int icol = 0; icol < compareColumns1.size(); icol++ ) {
        // Define columns of type string (no width specified),
    	// where the column name will be a simple concatenation of both column names,
    	// or one name if the column names for table1 and table2 match.
        String colName1 = table1.getFieldName(compareColumnNumbers1[icol]);
        String colName2 = ""; // Default for unmatched column - / will indicate difference in table names.
        if ( compareColumnNumbers2[icol] >= 0 ) {
            colName2 = table2.getFieldName(compareColumnNumbers2[icol]);
        }
        if ( !colName1.equalsIgnoreCase(colName2)) {
            // Show the column names from both tables.
            colName1 += " / " + colName2;
        }
        int newField = diffTable1.addField(new TableField(TableField.DATA_TYPE_STRING, colName1,-1), "");
        // Also set the column descriptions so the final results are easier to interpret.
        String desc1 = table1.getTableField(compareColumnNumbers1[icol]).getDescription();
        String desc2 = "";
        if ( compareColumnNumbers2[icol] >= 0 ) {
            desc2 = table2.getTableField(compareColumnNumbers2[icol]).getDescription();
        }
        if ( !desc1.equalsIgnoreCase(desc2) ) {
            desc1 += " / " + desc2;
        }
        diffTable1.getTableField(newField).setDescription(desc1);

        if ( diffTable2 != null ) {
        	// Set the second comparison table to have the same column names.
        	newField = diffTable2.addField(new TableField(TableField.DATA_TYPE_STRING, colName1,-1), "");
        	diffTable2.getTableField(newField).setDescription(desc1);
        }
    }

    // The values to compare are formatted as strings to handle floating point numbers:
    // - format each value in an array so that the entire row can be handled after detecting a difference in the row
    String [] formattedValue1 = new String[compareColumnNumbers1.length];
    String [] formattedValue2 = new String[compareColumnNumbers2.length];
    String formattedValue = null; // The comparison output.
    // Difference count in the row (for the columns being compared).
    int rowDiffCount = 0;
    // Difference count in the row (for match columns).
    int matchColDiffCount = 0;
    // Row position in first input table.
    int inRow1 = 0;
    // Row position in second input table:
    // - same as inRow1 for simple analysis
    // - can diverge for advanced analysis
    int inRow2 = 0;
    // Row position in output comparison tables.
    int outRow1 = 0;
    int outRow2 = 0;
    // Used to control how comparison table output is handled.
    boolean doAddEmptyRowForComparisonTable1 = false;
    // Used to control how comparison table output is handled.
    boolean doAddEmptyRowForComparisonTable2 = false;

    // Loop through the records in table 1 and compare
    for ( inRow1 = 0; inRow1 < table1.getNumberOfRecords(); ) {
    	// Reset the number of differences in the row to 0.
    	rowDiffCount = 0;
    	matchColDiffCount = 0;

   		// Used below if found a match later for table2:
      	// - add table2 row and add empty row for table1
		doAddEmptyRowForComparisonTable1 = false;

  		// Used below if no match is found in table2:
       	// - add table1 row and add empty row for table2
		doAddEmptyRowForComparisonTable2 = false;

    	// Loop through the columns in the row:
    	// - the actual column numbers in the table must be looked up using 'icol' as the index
        for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
        	try {
            	// Get the value from the first table and format as a string for comparisons.
               	formattedValue1[icol] =
               		formatInputTableValueString ( table1, inRow1, icol,
               			compareColumnFieldFormats1[compareColumnNumbers1[icol]] );

            	// Get the value from the second table and format as a string for comparisons:
            	// - the rows in the second table must be in the same order
            	// - the table rows should have been sorted before calling this code
               	formattedValue2[icol] =
               		formatInputTableValueString ( table2, inRow2, icol,
               			compareColumnFieldFormats2[compareColumnNumbers2[icol]] );

        		if ( Message.isDebugOn ) {
        			Message.printStatus ( 2, routine, "Comparing table1/table2 input row [" +
        				inRow1 + "]/[" + inRow2 + "] columns [" +
                    	compareColumnNumbers1[icol] + "]/[" + compareColumnNumbers2[icol] + "] icol " + icol + " values " +
        				formattedValue1[icol] + "/" + formattedValue2[icol]);
        		}

            	// Do the comparison of the column values:
            	// - the default behavior is to compare the formatted strings
            	if ( !formattedValue1[icol].equals(formattedValue2[icol]) ) {
            		// The values formatted as strings are different:
            		// - don't set anything in the difference output until all columns in the row are checked
            		// - this allows the advanced comparison to search for a matching row if necessary
            		++rowDiffCount;

                   	if ( this.analysisType == DataTableComparerAnalysisType.ADVANCED ) {
                   		// Check whether the difference is in a match column for advanced analysis:
            		    // - if so, then a row will need to be inserted below
                   		// - if differences are not in any of the match columns, then the row can be added as a difference
                   		for ( int iMatchCol : matchColumnNumbers1 ) {
                   			if ( iMatchCol == compareColumnNumbers1[icol] ) {
                   				// The column is one of the match columns:
                   				// - this means that the rows do not match key values
                   				++matchColDiffCount;
                   			}
                   		}
                   	}
            	}
        	}
        	catch ( Exception e ) {
        		// Typically occurs if there is a casting problem:
        		// - offset 0-index to 1-index for messages since user-facing
        		Message.printWarning(3, routine, "Error comparing cell values at table 1 row " + (inRow1 + 1) + " column "
        			+ (compareColumnNumbers1[icol] + 1) + ".");
        		Message.printWarning(3,routine,e);
               	formattedValue = "ERROR";
            	// Use -1 as to indicate error.
               	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
               		// Simple analysis.
               		diffTable1.setFieldValue(inRow1, (icol + rowNumberColumnOffset), formattedValue, true);
               		setCellDifferenceIndicator1 ( outRow1, (icol + rowNumberColumnOffset), this.CELL_ERROR );
               	}
               	else {
               		// Advanced analysis.
               		diffTable1.setFieldValue(outRow1, (icol + rowNumberColumnOffset), formattedValue, true);
               		setCellDifferenceIndicator1 ( outRow1, (icol + rowNumberColumnOffset), this.CELL_ERROR );
               		diffTable2.setFieldValue(outRow2, (icol + rowNumberColumnOffset), formattedValue, true);
               		setCellDifferenceIndicator2 ( outRow2, (icol + rowNumberColumnOffset), this.CELL_ERROR );
               	}
        	}
        }

        if ( Message.isDebugOn ) {
            Message.printStatus(2, routine, "Row analysis results: inRow1=" + inRow1 + " inRow2=" + inRow2
            	+ " rowDiffCount=" + rowDiffCount + " matchColDiffCount=" + matchColDiffCount );
        }

       	if ( (this.analysisType == DataTableComparerAnalysisType.ADVANCED) && (matchColDiffCount > 0) ) {
       		// Advanced analysis and there were differences in the match columns:
       		// - need to search for matching row using the match columns
       		// - virtual empty rows could be inserted in table1 or table2 by advancing the row index
       		// - the comparison table(s) will include empty rows depending on the search results

           	if ( Message.isDebugOn ) {
           		Message.printStatus(2, routine, "Searching forward in table2 for row matching table1 match columns inRow1=" + inRow1);
           	}

       		// Search forward in table 2 for a row that matches:
       		// - only the match column values are compared (typically these columns are for unique identifiers)
       		// - start with the current table 2 row because may match the "match" columns even if other differences
       		String table1Value = null;
       		String table2Value = null;
       		boolean foundMatch = false;
       		for ( int iSearchRow2 = inRow2; iSearchRow2 < table2.getNumberOfRecords(); iSearchRow2++ ) {
       			// The 'matchCount' is reset for each compared row.
      			int matchCount = 0;
                for ( int iMatchCol = 0; iMatchCol < matchColumnNumbers1.length; iMatchCol++ ) {
                	table1Value = formatInputTableValueString (
               			table1, inRow1, matchColumnNumbers1[iMatchCol], matchColumnFieldFormats1[matchColumnNumbers1[iMatchCol]] );
                	table2Value = formatInputTableValueString (
                		table2, iSearchRow2, matchColumnNumbers2[iMatchCol], matchColumnFieldFormats2[matchColumnNumbers2[iMatchCol]] );
                	if ( Message.isDebugOn ) {
           		    	Message.printStatus(2, routine, "  Comparing \"" + table1Value + "\" and \"" + table2Value + "\"." );
                	}
                   	if ( table1Value.equals(table2Value) ) {
                   		++matchCount;
                   	}
                }
                if ( Message.isDebugOn ) {
           		    Message.printStatus(2, routine, "  For table1 inRow1=" + inRow1
           		    	+ " and table2 inRow2=" + inRow2 + " matchCount=" + matchCount);
                }
                if ( matchCount == matchColumnNumbers1.length ) {
                	// Found a matching row in table 2.
                	if ( Message.isDebugOn ) {
                		Message.printStatus(2, routine, "Found a matching table2 row [" + iSearchRow2 + "].");
                	}
                	if ( iSearchRow2 == inRow2 ) {
                		// Row had differences but the match columns have no differences:
                		// - just continue to below to add to output as a different line
                		// - comparison table1 and comparison table2 will have data
                	}
                	else {
                		// Match was found in table2 later in the table:
                		// - insert table2 rows prior to the matching row
                		// - insert empty table 1 rows prior to the matching row
                		for ( int iAddRow = inRow2; iAddRow < iSearchRow2; iAddRow++ ) {
                			// Table 1:
                			// - add a blank row to comparison table 1
                			diffTable1.insertRecord(outRow1, diffTable1.emptyRecord(), false );
                			if ( doRowNumberColumn ) {
                				setCellDifferenceIndicator1(outRow1, 0, CELL_INSERT_EMPTY_ROW_TABLE1);
                			}
                			for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
                				setCellDifferenceIndicator1(outRow1, (icol + rowNumberColumnOffset), CELL_INSERT_EMPTY_ROW_TABLE1);
                			}
                			++outRow1;
                			// Table 2:
                			// - add the data from table 2
                			diffTable2.insertRecord(outRow1, diffTable2.emptyRecord(), false );
                			if ( doRowNumberColumn ) {
           	        			diffTable2.setFieldValue(outRow2, 0, "" + (inRow2 + 1), true);
                				setCellDifferenceIndicator2(outRow2, 0, CELL_ROW_ONLY_IN_TABLE2);
                			}
                			for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
        	        			try {
        	        				// Use the same value so that the result will not show a difference:
        	        				// - don't use the 'formattedValue1' and 'formattedValue2' arrays
        	        				formattedValue =
        	        					formatInputTableValueString ( table2, inRow2, icol,
        	        						compareColumnFieldFormats2[compareColumnNumbers2[icol]] );
               	        			formattedValue = formatComparisonTableValuesString (
               		        			outRow2, (icol + rowNumberColumnOffset),
               		        			formattedValue, formattedValue,
               		        			table2.getFieldDataType(compareColumnNumbers2[icol]),
               		        			tolerance,
               		        			2);

               	        			// Set the field value in comparison table 2, creating the row if necessary.
               	        			diffTable2.setFieldValue(outRow2, (icol + rowNumberColumnOffset), formattedValue, true);
               	        			setCellDifferenceIndicator2(outRow2, (icol + rowNumberColumnOffset), CELL_ROW_ONLY_IN_TABLE2);
        	        			}
        	        			catch ( Exception e ) {
        		        			// Should not happen.
        	        			}
                			}
                			++inRow2;
                			++outRow2;
                			// Set the 'formattedValue1' and 'formattedValue2' arrays to set in the aligned row.
                			for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
                				// Get the value from the first table and format as a string for comparisons.
               	   				formattedValue1[icol] =
               		   				formatInputTableValueString ( table1, inRow1, icol,
               			   				compareColumnFieldFormats1[compareColumnNumbers1[icol]] );

            	   				// Get the value from the second table and format as a string for comparisons:
            	   				// - the rows in the second table must be in the same order
            	   				// - the table rows should have been sorted before calling this code
               	   				formattedValue2[icol] =
               		   				formatInputTableValueString ( table2, inRow2, icol,
               			   				compareColumnFieldFormats2[compareColumnNumbers2[icol]] );
                			}
                		}
                	}
                	// Break out of the row search.
               		foundMatch = true;
                	break;
                }
                else {
                	// Did not find a matching row in table2:
                	// - search the next row in table2
                }
       		}

       		if ( !foundMatch ) {
       			// Did not find a matching row in table 2:
       			// - output table 1 showing difference (nothing in table 2)
       			// - add a blank row in table 2
       			//++outRow1;
       			if ( Message.isDebugOn ) {
       				Message.printStatus ( 2, routine, "Did not find a match in table2.  "
       					+ "Will add table1 row to comparison table1 and add an empty row to comparison table2." );
       			}
       			// Trick the code below by using the same values for both tables so just table 1 values (no diff) are in output.
       			for ( int icol = 0; icol < compareColumnNumbers1.length; icol++ ) {
       				formattedValue2[icol] = formattedValue1[icol];
       			}
      			diffTable2.insertRecord(outRow2, diffTable2.emptyRecord(), false );
       			// Indicate to add an empty row below.
       			doAddEmptyRowForComparisonTable2 = true;
       		}
       	}

       	if ( Message.isDebugOn ) {
       		Message.printStatus ( 2, routine, "Before adding to comparison tables, inRow1=" + inRow1 + " inRow2=" + inRow2
       			+ " outRow1=" + outRow1 + " outRow2=" + outRow2 );
       	}

        // Set the values in the comparison tables for the current matched rows (may or may not contain difference).
       	if ( doRowNumberColumn ) {
       		// Table 1.
       		// Add the row number:
       		// - the position is the output row
       		// - the value is the input row, to allow comparison with the original input
			if ( doAddEmptyRowForComparisonTable1 ) {
   				// Adding an empty row so don't add the row number.
   				setCellDifferenceIndicator1(outRow1, 0, CELL_INSERT_EMPTY_ROW_TABLE1);
			}
			else {
 				// Adding a normal comparison table1 row:
 				// - use CELL_SAME for the row number since independent of difference count.
   				if ( doAddEmptyRowForComparisonTable2 ) {
   					setCellDifferenceIndicator1(outRow1, 0, CELL_ROW_ONLY_IN_TABLE1);
   				}
   				else {
   					setCellDifferenceIndicator1(outRow1, 0, CELL_SAME);
   				}
				diffTable1.setFieldValue(outRow1, 0, "" + (inRow1 + 1), true);
			}
			// Table 2.
   			if ( diffTable2 != null ) {
   				if ( doAddEmptyRowForComparisonTable2 ) {
   					// Adding an empty row so don't add the row number.
   					setCellDifferenceIndicator2(outRow1, 0, CELL_ROW_ONLY_IN_TABLE1);
   					setCellDifferenceIndicator2(outRow2, 0, CELL_INSERT_EMPTY_ROW_TABLE2);
   				}
   				else {
   					// Adding a normal comparison table2 row:
   					// - use CELL_SAME for the row number since independent of difference count.
       				setCellDifferenceIndicator2(outRow2, 0, CELL_SAME);
       				diffTable2.setFieldValue(outRow2, 0, "" + (inRow2 + 1), true);
       			}
           	}
       	}

       	// Loop through the comparison table columns and output results:
       	// - the column for output must be offset by the row number column
       	int iColEnd = compareColumnNumbers1.length - 1;
        for ( int icol = 0; icol <= iColEnd; icol++ ) {
        	try {
            	if ( analysisType == DataTableComparerAnalysisType.SIMPLE ) {
            		// Comparison table 1.
           			if ( icol == 0 ) {
           				if ( Message.isDebugOn ) {
           					Message.printStatus(2, routine, "  Simple analysis: adding table1 comparison row at outRow1=" + outRow1);
           				}
           			}
            		formattedValue = formatComparisonTableValuesString (
            			outRow1, (icol + rowNumberColumnOffset),
            			formattedValue1[icol], formattedValue2[icol],
        		        table1.getFieldDataType(compareColumnNumbers1[icol]),
            			this.__tolerance, 1 );

            		// Set the field value in the first (and only) comparison table, creating the row if necessary.
            		diffTable1.setFieldValue(outRow1, (icol + rowNumberColumnOffset), formattedValue, true);
            		//Message.printStatus(2, "", "formattedValue1=\"" + formattedValue1 + "\" (format=" + format1 +
            		//    ") formattedValue2=\"" + formattedValue2 + "\" (format=" + format2 +
            		//    ") mask=" + differenceArray[inRow1][icol] );

    	       		// Increment the input row for the second table (inRow1 is incremented in the loop counter).
           			if ( icol == iColEnd ) {
           				++inRow1;
           				++outRow1;
           				++inRow2;
           				++outRow2;
           			}
            	}
            	else if ( analysisType == DataTableComparerAnalysisType.ADVANCED ) {
            		// Set the field value in both comparison tables, creating the row if necessary.
            		// Table 1.
            		if ( doAddEmptyRowForComparisonTable1 ) {
            			// Add an empty table 1 row.
            			if ( icol == 0 ) {
           					if ( Message.isDebugOn ) {
           						Message.printStatus(2, routine, "  Advanced analysis: adding empty table1 comparison row at outRow1=" + outRow1);
           					}
           				}
            			formattedValue = "";
            			diffTable1.setFieldValue(outRow1, (icol + rowNumberColumnOffset), formattedValue, true);
            			setCellDifferenceIndicator1(outRow1, (icol + rowNumberColumnOffset), CELL_INSERT_EMPTY_ROW_TABLE1);
            			if ( icol == iColEnd ) {
            				// Increment the row if processed the last column in the row.
            				++outRow1;
            				// Input row is not incremented since an empty row was inserted.
            			}
            		}
            		else {
            			// Add a normal table 1 row (may have differences).
            			if ( icol == 0 ) {
           					if ( Message.isDebugOn ) {
           						Message.printStatus(2, routine, "  Advanced analysis: adding table1 comparison row with data at outRow1=" + outRow1);
           					}
           				}
            			formattedValue = formatComparisonTableValuesString (
            				outRow1, (icol + rowNumberColumnOffset),
            				formattedValue1[icol], formattedValue2[icol],
            				table1.getFieldDataType(compareColumnNumbers1[icol]),
            				this.__tolerance, 1 );
            			diffTable1.setFieldValue(outRow1, (icol + rowNumberColumnOffset), formattedValue, true);
            			if ( doAddEmptyRowForComparisonTable2 ) {
            				// Since table 2 is empty row, set the flag on table 1 accordingly.
            				setCellDifferenceIndicator1(outRow1, (icol + rowNumberColumnOffset), CELL_ROW_ONLY_IN_TABLE1);
            			}
            			if ( icol == iColEnd ) {
            				// Increment the row if processed the last column in the row.
            				++inRow1;
            				++outRow1;
            			}
            		}
            		// Table 2.
            		if ( doAddEmptyRowForComparisonTable2 ) {
            			// Add an empty table2 row.
            			if ( icol == 0 ) {
           					if ( Message.isDebugOn ) {
           						Message.printStatus(2, routine, "  Advanced analysis: adding empty table2 comparison row at outRow2=" + outRow2);
           					}
           				}
            			formattedValue = "";
            			diffTable2.setFieldValue(outRow2, (icol + rowNumberColumnOffset), formattedValue, true);
            			setCellDifferenceIndicator2(outRow2, (icol + rowNumberColumnOffset), CELL_INSERT_EMPTY_ROW_TABLE2);
            			if ( icol == iColEnd ) {
            				// Increment the row if processed the last column in the row.
            				++outRow2;
            				// Input row is not incremented since an empty row was inserted.
            			}
            		}
            		else {
            			// Add a normal data row, but may include differences.
            			if ( icol == 0 ) {
           					if ( Message.isDebugOn ) {
           						Message.printStatus(2, routine, "  Advanced analysis: adding table2 comparison row with data at outRow2=" + outRow2);
           					}
           				}
            			formattedValue = formatComparisonTableValuesString (
            				outRow2, (icol + rowNumberColumnOffset),
            				formattedValue1[icol], formattedValue2[icol],
            				table1.getFieldDataType(compareColumnNumbers1[icol]),
            				this.__tolerance, 2 );
            			diffTable2.setFieldValue(outRow2, (icol + rowNumberColumnOffset), formattedValue, true);
            			if ( doAddEmptyRowForComparisonTable1 ) {
            				// Since table1 is empty, set table 2 to indicate only it has data.
            				setCellDifferenceIndicator2(outRow2, (icol + rowNumberColumnOffset), CELL_ROW_ONLY_IN_TABLE2);
            			}
            			if ( icol == iColEnd ) {
            				// Increment the input row for the second table (inRow1 is incremented in the loop counter).
            				++inRow2;
            				// Increment the row if processed the last column in the row.
            				++outRow2;
            			}
            		}
            	}
        	}
        	catch ( Exception e ) {
        		// Should not happen.
        		Message.printWarning(3, routine, "Exception processing table comparison row (should not happen).");
        		Message.printWarning(3, routine,e);
        	}
        }

       	if ( Message.isDebugOn ) {
       		Message.printStatus ( 2, routine, "After adding to comparison tables, inRow1=" + inRow1 + " inRow2=" + inRow2
       			+ " outRow1=" + outRow1 + " outRow2=" + outRow2 );
       	}
    }

    if ( Message.isDebugOn ) {
    	Message.printStatus ( 2, routine, "After processing table1 rows, inRow1=" + inRow1 + " inRow2=" + inRow2
    		+ " outRow1=" + outRow1 + " outRow2=" + outRow2 + " diffTable1.size=" + diffTable1.getNumberOfRecords());
		if ( diffTable2 != null ) {
  			Message.printStatus ( 2, routine, "  comaparisonTable2.size=" + diffTable2.getNumberOfRecords());
		}
    }

    // If rows remain in table 2:
    // - add empty rows to comparison table 1
    // - and 2 rows to the second comparison table
    // - this should work for simple and advanced analysis
    for ( ; inRow2 < table2.getNumberOfRecords(); ++inRow2 ) {
    	// Add empty row to table 1:
   		// - do not set the row number in table 1 since no original data in table 1
    	if ( Message.isDebugOn ) {
    		Message.printStatus ( 2, routine, "Adding comparison table 1 empty row [" + outRow1 +
    			"] at inRow1=" + inRow1 + " outRow1=" + outRow1 + " diffTable1.size=" +
    			diffTable1.getNumberOfRecords());
    	}
   		diffTable1.insertRecord(outRow1, diffTable1.emptyRecord(), false );
   		for ( int icol = 0; icol < (compareColumnNumbers1.length + rowNumberColumnOffset); icol++ ) {
   			// This will cause the row to be automatically added.
   			diffTable1.setFieldValue(outRow1, icol, " ", true);
   			setCellDifferenceIndicator1(outRow1, icol, this.CELL_NO_ROW_TABLE1);
   		}
		++outRow1;
   		if ( diffTable2 != null ) {
   			if ( Message.isDebugOn ) {
   				Message.printStatus ( 2, routine, "Before adding ending comparison table 2 row [" + outRow2 +
    				"] at inRow2=" + inRow2 + " outRow2=" + outRow2 + " comaparisonTable2.size=" +
    				diffTable2.getNumberOfRecords());
   			}
   			// Add contents of table 2:
   			// - set the row number in table 2
   			diffTable2.insertRecord(outRow2, diffTable2.emptyRecord(), false );
   			if ( doRowNumberColumn ) {
   				// Row number is 1+ whereas internal is 0+
      			diffTable2.setFieldValue(outRow2, 0, "" + (inRow2 + 1), true);
   				setCellDifferenceIndicator2(outRow2, 0, this.CELL_ROW_ONLY_IN_TABLE2);
   			}
   			for ( int icol = 0; icol < compareColumnNumbers2.length; icol++ ) {
   				// Set the data value from the original table.
            	formattedValue2[icol] = formatInputTableValueString (
            		table2, inRow2, compareColumnNumbers2[icol], compareColumnFieldFormats2[compareColumnNumbers2[icol]] );
      			formattedValue = formatComparisonTableValuesString (
      				outRow2, (icol + rowNumberColumnOffset),
      				// Use the same value for both so that it will show as the same.
      				formattedValue2[icol], formattedValue2[icol],
      				table2.getFieldDataType(compareColumnNumbers2[icol]),
      				this.__tolerance, 2 );
      			diffTable2.setFieldValue(outRow2, (icol + rowNumberColumnOffset), formattedValue, true);
   				// TODO smalers 2024-02-19 may need a way to say only data on right.
   				setCellDifferenceIndicator2(outRow2, (icol + rowNumberColumnOffset), this.CELL_ROW_ONLY_IN_TABLE2);
   			}
   			++outRow2;
   			if ( Message.isDebugOn ) {
   				Message.printStatus ( 2, routine, "After adding ending comparison table 2 row [" + outRow2 +
    				"] at inRow2=" + inRow2 + " outRow2=" + outRow2 + " comaparisonTable2.size=" +
    				diffTable2.getNumberOfRecords());
   			}
   		}
    }

    // If a final difference table is requested, create it.

    if ( (analysisType == DataTableComparerAnalysisType.ADVANCED) &&
    	(this.diffTableID != null) && !this.diffTableID.isEmpty() ) {

    	// If here the data tables and difference tracking lists have consistent sizes.

    	createDiffTable ( doRowNumberColumn );
    }
    
    // Adjust the tables to include only rows of interest.
    
    adjustDiffTableRows ( analysisType, doOutputDifferent, doOutputSame, rowNumberColumnOffset );
}

    /**
     * Create the final difference table and set as class data.
     * @param doRowNumberColumn whether the difference tables should include a row number column
     */
    private void createDiffTable ( boolean doRowNumberColumn ) throws Exception {
    	String routine = getClass().getSimpleName() + ".createFinalDiffTable";
    	String message = null;

    	int rowNumberColumnOffset = 0;
    	// Offset considering two row number columns for the final difference table.
    	if ( doRowNumberColumn ) {
    		rowNumberColumnOffset = 1;
    	}

    	// If the analysis was successful,
    	// the first and second difference tables should have the same number of columns and rows.

    	if ( this.diffTable1.getNumberOfFields() != this.diffTable2.getNumberOfFields() ) {
    		message = "The first difference table number of columns (" + diffTable1.getNumberOfFields() +
    		") is different than the second difference table number of columns (" + diffTable2.getNumberOfFields() + ").";
    		Message.printWarning(3, routine, message);
    		throw new Exception ( message );
    	}
    	if ( diffTable1.getNumberOfRecords() != diffTable2.getNumberOfRecords() ) {
    		message = "The first difference table number of rows (" + diffTable1.getNumberOfRecords() +
    		") is different from the second difference table number of rows (" + diffTable2.getNumberOfRecords() + ").";
    		Message.printWarning(3, routine, message);
    		throw new Exception ( message );
    	}

    	// Make sure that the difference tracking data are also the same size.

    	if ( this.__differenceList1.length != this.__differenceList2.length ) {
    		message = "The first difference tracker table number of columns (" + this.__differenceList1.length +
    		") is different than the second difference tracker table number of columns (" + this.__differenceList2.length + ").";
    		Message.printWarning(3, routine, message);
    		throw new Exception ( message );
    	}
   		else {
    		for ( int icol = 0; icol < this.__differenceList1.length; icol++ ) {
    			// Make sure the rows are the same length.
    			if ( this.__differenceList1[icol].size() != this.__differenceList2[icol].size() ) {
    				message = "The first difference tracker table column [" + icol +
    					"] number of rows (" + this.__differenceList1[icol].size() +
    					") is different than the second difference tracker table number of rows (" +
    					this.__differenceList2[icol].size() + ").";
    				Message.printWarning(3, routine, message);
    				throw new Exception ( message );
    			}
    		}
   		}
    	// Copy the difference table1 to a new final difference table.

   		String [] reqIncludeColumns = null;
   		String [] distinctColumns = null;
   		Hashtable<String,String> columnMap = null;
   		Hashtable<String,String> columnFilters = null;
   		StringDictionary columnExcludeFilters = null;
   		DataTable diffTable = diffTable1.createCopy ( this.diffTable1,
   			this.diffTableID, reqIncludeColumns, distinctColumns, columnMap, columnFilters, columnExcludeFilters);
   		setDiffTable ( diffTable );

   		// Copy the first table difference indicator list:
   		// - increment the size by the 'rowNumberColumnOffset' for the second table (first table is already in the size)
   		this.__differenceList = new ArrayList[this.__differenceList1.length + rowNumberColumnOffset];
   		if ( doRowNumberColumn ) {
   			// Copy the first column:
   			// - do here so that a column for the second table can be inserted below
   			int iCol = 0; // Index of the final indicator list array.
   			int iCol1 = 0; // Row number column index.
   			this.__differenceList[iCol] = new ArrayList<Integer>();
   			List<Integer> list1 = this.__differenceList1[iCol1];
   			List<Integer> list = this.__differenceList[iCol];
   			for ( int irow = 0; irow < list1.size(); irow++ ) {
   				// OK to add the same Integer since they are immutable.
   				list.add(list1.get(irow));
   			}
   			// Add a second list for the table2 row number column.
   			iCol = 1; // Index of the final indicator list array.
   			int iCol2 = 0; // Row number column index.
   			this.__differenceList[iCol] = new ArrayList<Integer>();
   			List<Integer> list2 = this.__differenceList2[iCol2];
   			list = this.__differenceList[1];
   			for ( int irow = 0; irow < list1.size(); irow++ ) {
   				// OK to add the same Integer since they are immutable.
   				list.add(list2.get(irow));
   			}
   		}
   		// Copy the rest of the table1 list columns.
   		for ( int iCol1 = rowNumberColumnOffset; iCol1 < this.__differenceList1.length; iCol1++ ) {
   			// Position in the final difference indicator lists.
   			int iCol = iCol1 + rowNumberColumnOffset;
   			this.__differenceList[iCol] = new ArrayList<Integer>();
   			List<Integer> list1 = this.__differenceList1[iCol1];
   			List<Integer> list = this.__differenceList[iCol];
   			for ( int irow = 0; irow < list1.size(); irow++ ) {
   				// OK to add the same Integer since they are immutable.
   				list.add(list1.get(irow));
   			}
   		}

   		// Insert the second table indicator row number.

   		// Change the names of the table columns if they are not the same in both tables.

   		for ( int icol = 0; icol < diffTable.getNumberOfFields(); icol++ ) {
			TableField tableField1 = diffTable.getTableField(icol);
			TableField tableField2 = diffTable2.getTableField(icol);
   			if ( doRowNumberColumn && (icol == 0) ) {
   				// Change the name of the row number column.
   				// The position is assured because 'diffTable' is a copy of 'diffTable1'.
   				tableField1.setName(tableField1.getName() + "-Table1");
   			}
   			else {
   				// All other columns.
   				if ( !tableField1.getName().equals(tableField2.getName()) ) {
   					// Column names are different:
  					// - set the field name to both separated by a slash
   					tableField1.setName(tableField1.getName() + "/" + tableField2.getName());
   				}
   			}
   		}

   		if ( doRowNumberColumn ) {
   			// Add a second row number column for the second table:
   			// - the input row numbers for the second table can be different from the second table
   			// - the table1 row number column was added above so add in position 1
   			diffTable.addField(1, new TableField(TableField.DATA_TYPE_INT, rowNumberColumn + "-Table2", -1), null);
   		}

   		// Loop through the difference tables and update the contents of the final difference table:
   		// - set the cell flag (CELL_*)
   		// - if necessary, copy data from the second difference table
   		// - precedence is given to setting rows that are only present in the second table
   		// - the column numbers will include the table1 row number column and table2 row number column inserted above
   		DataTable diffTable2 = this.diffTable2;
   		// Column position in diffTable1, considering row number columns.
   		int iCol1 = 0;
   		// Column position in diffTable2, considering row number columns.
   		int iCol2 = 0;
   		for ( int iRow = 0; iRow < diffTable.getNumberOfRecords(); iRow++ ) {
   			for ( int iCol = 0; iCol < diffTable.getNumberOfFields(); iCol++ ) {
				iCol1 = iCol - rowNumberColumnOffset;
				iCol2 = iCol - rowNumberColumnOffset;
   				if ( doRowNumberColumn && (iCol == 0)) {
   					// The row number column for table1:
   					// - check the table2 indicator and set the table1 indicator so the column will match for the row
   					Integer indicator2 = this.__differenceList2[iCol2 + 1].get(iRow);
   					if ( indicator2 == CELL_ROW_ONLY_IN_TABLE2 ) {
   						setCellDifferenceIndicator(iRow, iCol, indicator2);
   					}
   				}
   				else if ( doRowNumberColumn && (iCol == 1)) {
   					// The row number column for table2;
   					// - always copy the table2 row number from the second table, OK to copy nulls
   					diffTable.setFieldValue(iRow, iCol, diffTable2.getFieldValue(iRow, 0));
   					Integer indicator1 = this.__differenceList1[iCol1].get(iRow);
   					Integer indicator2 = this.__differenceList2[iCol2].get(iRow);
   					if ( indicator1 == CELL_ROW_ONLY_IN_TABLE1 ) {
   						setCellDifferenceIndicator(iRow, iCol, indicator1);
   					}
   					else if ( indicator2 == CELL_ROW_ONLY_IN_TABLE2 ) {
   						setCellDifferenceIndicator(iRow, iCol, indicator2);
   					}
   				}
   				else {
   					// All other columns:
   					// - no need to offset the index position for row number columns
   					// - the second difference list does need to be offset because it may contain one row number column
   					Integer indicator2 = this.__differenceList2[iCol2].get(iRow);
   					if ( indicator2 == CELL_ROW_ONLY_IN_TABLE2 ) {
   						// Row is only present in table2:
   						// - copy to the final difference table and set the indicator
   						diffTable.setFieldValue(iRow, iCol, diffTable2.getFieldValue(iRow, iCol2));
   						setCellDifferenceIndicator(iRow, iCol, indicator2);
   					}
   				}
   			}
   		}

   		// Set the final difference table in the class data to allow retrieval by calling code.
   		setDiffTable(diffTable);
    }

/**
 * Format a single input table value as a string to allow comparison.
 * @param table table containing the data
 * @param irow table row (0+)
 * @param icol table column (0+)
 * @param format the format to use for the value
 */
private String formatInputTableValueString ( DataTable table, int irow, int icol, String format ) {
	String formattedValue = null;
	if ( icol < 0 ) {
		// Column name was not found in the table.
		formattedValue = "";
	}
	else {
		// Get the value from the table.
		Object value = null;
		try {
			value = table.getFieldValue(irow, icol);
		}
		catch ( Exception e ) {
			value = null;
		}

		// Format the value.
		if ( value == null ) {
			formattedValue = "";
		}
		else {
           	// Check for integer to format without trailing 0's.
           	// First check for number, then for integer.
       		// FIXME smalers 2022-04-29 the following does not handle Integer because of casting.
           	if (((table.getFieldDataType(icol) == TableField.DATA_TYPE_DOUBLE) ||
               	(table.getFieldDataType(icol) == TableField.DATA_TYPE_FLOAT)) &&
               	(value.getClass().getName() == "Integer" || ((Double) value == Double.POSITIVE_INFINITY) ||
               	((Double) value - Math.round((Double) value)) == 0)) {
               	formattedValue = StringUtil.formatString(value,"%.0f").trim();
           	}
           	else {
               	formattedValue = StringUtil.formatString(value,format).trim();
           	}
		}
    }

   	return formattedValue;
}

/**
 * Format the final comparison table output string based on formatted table1 and table2 values.
 * This will also set the difference data information.
 * @param outRow the comparison table output column position (0+) for the 'comparisonTableNum' table,
 * used to set the comparison table cell flag
 * @param icol the input column position (0+) from table 1 being compared, should reflect include row number column
 * @param formattedValue1 formatted value from table 1
 * @param formattedValue2 formatted value from table 2
 * @param columnDataType the table column data type, used to detect floating point numbers
 * @param tolerance the tolerance for comparisons, or null if not used
 * @param comaparisonTableNum the comparison table number, used when setting output in the difference array/list
 * @return the formatted string suitable for output in the comparison table
 */
private String formatComparisonTableValuesString (
	int outRow, int icol,
	String formattedValue1, String formattedValue2,
	int columnDataType,
	Double tolerance,
	int comparisonTableNum ) {
   	// Format the value for the comparison table.
	String formattedValue = "";
    if ( formattedValue1.equals(formattedValue2) ) {
       	// Formatted values are the same so the output table value is just the formatted value.
       	formattedValue = formattedValue1;
       	if ( comparisonTableNum == 1 ) {
       		setCellDifferenceIndicator1 ( outRow, icol, this.CELL_SAME );
       	}
       	else {
       		setCellDifferenceIndicator2 ( outRow, icol, this.CELL_SAME );
       	}
   	}
    else {
    	// The values formatted as strings are different:
       	// - show both values as "value1 / value2" and set the boolean indicating a difference
       	if ( ((columnDataType == TableField.DATA_TYPE_DOUBLE) ||
          	(columnDataType == TableField.DATA_TYPE_FLOAT)) &&
          	(tolerance != null) &&
           	StringUtil.isDouble(formattedValue1) && StringUtil.isDouble(formattedValue2)) {
           	// Convert the formatted strings to doubles and compare the difference against the tolerance.
          	double dvalue1 = Double.parseDouble(formattedValue1);
           	double dvalue2 = Double.parseDouble(formattedValue2);
           	if ( Math.abs(dvalue1 - dvalue2) >= tolerance ) {
           		// Floating point numbers have difference > the the tolerance.
               	formattedValue = formattedValue1 + " / " + formattedValue2;
               	if ( comparisonTableNum == 1 ) {
               		setCellDifferenceIndicator1 ( outRow, icol, this.CELL_DIFFERENT );
               	}
               	else {
               		setCellDifferenceIndicator2 ( outRow, icol, this.CELL_DIFFERENT );
               	}
           	}
           	else {
           		// Floating point numbers are the same within the tolerance:
               	// - still show both values but don't set the difference flag since tolerance is met
               	// - indicate that values compare within tolerance using ~
               	formattedValue = formattedValue1 + " ~/~ " + formattedValue2;
               	if ( comparisonTableNum == 1 ) {
               		setCellDifferenceIndicator1 ( outRow, icol, this.CELL_SAME );
               	}
               	else {
               		setCellDifferenceIndicator2 ( outRow, icol, this.CELL_SAME );
               	}
           	}
        }
        else {
          	// Not floating point or floating point and no tolerance is specified so no need to do
           	// additional comparison.
           	formattedValue = formattedValue1 + " / " + formattedValue2;
           	if ( comparisonTableNum == 1 ) {
           		setCellDifferenceIndicator1 ( outRow, icol, this.CELL_DIFFERENT );
           	}
           	else {
           		setCellDifferenceIndicator2 ( outRow, icol, this.CELL_DIFFERENT );
           	}
        }
    }
    return formattedValue;
}

/**
Get the list of columns to be compared from the first table.
*/
private List<String> getCompareColumns1 () {
    return this.__compareColumns1;
}

/**
Get the list of columns to be compared from the second table.
*/
private List<String> getCompareColumns2 () {
    return this.__compareColumns2;
}

/**
Return the first comparison (difference) table.
This calls getDiffTable1().
@return the first comparison table.
@deprecated use getDiffTable1.
*/
public DataTable getComparisonTable () {
    return getDiffTable1();
}

/**
Return the final difference table.
@return the final difference table.
*/
public DataTable getDiffTable () {
    return this.diffTable;
}

/**
Return the first difference table.
@return the first difference table.
*/
public DataTable getDiffTable1 () {
    return this.diffTable1;
}

/**
Return the second difference table.
@return the second difference table.
*/
public DataTable getDiffTable2 () {
    return this.diffTable2;
}

/**
Return the difference array.
@return the difference array.
*/
/*
private int [][] getDifferenceArray () {
    return this.__differenceArray;
}
*/

/**
Return the first difference list.
@return the first difference list.
*/
private List<Integer> [] getDifferenceList1 () {
    return this.__differenceList1;
}

/**
Return the second difference list.
@return the second difference list.
*/
private List<Integer> [] getDifferenceList2 () {
    return this.__differenceList2;
}

/**
Return the count of the differences, based on the first comparison table cell flags.
Do not count the row column.
@return the count of the differences.
*/
public int getDifferenceCount () {
	// Determine if a row number column is used.
	int rowNumberColumnOffset = 0;
    if ( (this.rowNumberColumn != null) && !this.rowNumberColumn.isEmpty() ) {
    	// Add a row number column to the comparison table(s).
    	rowNumberColumnOffset = 1;
    }
    /*
   	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
   		// Simple analysis:
   		// - differences are in the 2D array
		int [][] differenceArray = getDifferenceArray();
		if ( differenceArray == null ) {
			return 0;
		}
		else {
			int differenceCount = 0;
			for ( int irow = 0; irow < differenceArray.length; irow++ ) {
				for ( int icol = rowNumberColumnOffset; icol < differenceArray[irow].length; icol++ ) {
					if ( differenceArray[irow][icol] > this.CELL_SAME ) {
						++differenceCount;
					}
				}
			}
			return differenceCount;
		}
	}
	else {
	*/
		// Advanced analysis:
		// - differences are in an array of lists
		List<Integer> [] differenceList = getDifferenceList1();
		if ( differenceList == null ) {
			return 0;
		}
		else {
			int differenceCount = 0;
			for ( int icol = rowNumberColumnOffset; icol < differenceList.length; icol++ ) {
				for ( int irow = 0; irow < differenceList[icol].size(); irow++ ) {
					if ( differenceList[icol].get(irow) > this.CELL_SAME ) {
						++differenceCount;
					}
				}
			}
			return differenceCount;
		}
	//}
}

/**
 * Get the number of cell differences, same as getDifferenceCount().
 * @return the number of cell differences
 */
public int getDifferentCellCount () {
	return getDifferenceCount();
}

/**
Return the count of the row differences, based on the first table's comparison table.
@return the count of the row differences.
*/
public int getDifferentRowCount () {
	// Determine if a row number column is used.
	int rowNumberColumnOffset = 0;
    if ( (this.rowNumberColumn != null) && !this.rowNumberColumn.isEmpty() ) {
    	// Add a row number column to the comparison table(s).
    	rowNumberColumnOffset = 1;
    }
    /*
   	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
   		// Simple analysis:
   		// - differences are in the 2D array
		int [][] differenceArray = getDifferenceArray();
		if ( differenceArray == null ) {
			return 0;
		}
		else {
			int differenceCount = 0;
			for ( int irow = 0; irow < differenceArray.length; irow++ ) {
				for ( int icol = rowNumberColumnOffset; icol < differenceArray[irow].length; icol++ ) {
					if ( differenceArray[irow][icol] > this.CELL_SAME ) {
						// A column in the row is different.
						++differenceCount;
						// Go to the next row since counting rows.
						break;
					}
				}
			}
			return differenceCount;
		}
	}
	else {
	*/
		// Advanced analysis:
		// - differences are in an array of lists
		List<Integer> [] differenceList = getDifferenceList1();
		if ( differenceList == null ) {
			return 0;
		}
		else {
			int differenceCount = 0;
			for ( int irow = 0; irow < differenceList[0].size(); irow++ ) {
				for ( int icol = rowNumberColumnOffset; icol < differenceList.length; icol++ ) {
					if ( differenceList[icol].get(irow) > this.CELL_SAME ) {
						// A column in the row is different.
						++differenceCount;
						// Go to the next row since counting rows.
						break;
					}
				}
			}
			return differenceCount;
		}
	//}
}

/**
Return the count of the errors.
@return the count of the errors.
*/
public int getErrorCount () {
	/*
   	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
		int [][] differenceArray = getDifferenceArray();
		if ( differenceArray == null ) {
			return 0;
		}
		else {
			int errorCount = 0;
			for ( int irow = 0; irow < differenceArray.length; irow++ ) {
				for ( int icol = 0; icol < differenceArray[irow].length; icol++ ) {
					if ( differenceArray[irow][icol] == this.CELL_ERROR ) {
						++errorCount;
					}
				}
			}
			return errorCount;
		}
	}
	else {
	*/
		List<Integer> [] differenceList = getDifferenceList1();
		if ( differenceList == null ) {
			return 0;
		}
		else {
			int errorCount = 0;
			for ( int icol = 0; icol < differenceList.length; icol++ ) {
				for ( int irow = 0; irow < differenceList[icol].size(); irow++ ) {
					if ( differenceList[icol].get(irow) == this.CELL_ERROR ) {
						++errorCount;
					}
				}
			}
			return errorCount;
		}
	//}
}

/**
Return whether to match the columns by name.
@return true to match columns by name, false to match by order.
*/
private boolean getMatchColumnsByName () {
    return this.__matchColumnsByName;
}

/**
Return the identifier to be used for the first new comparison table.
@return the identifier to be used for the first new comparison table.
*/
private String getNewTableID () {
    return this.__newTableID;
}

/**
Return the identifier to be used for the second new comparison table.
@return the identifier to be used for the second new comparison table.
*/
private String getNewTable2ID () {
    return this.__newTable2ID;
}

/**
Return the precision to use for floating point comparisons.
@return the precision to use for floating point comparisons.
*/
private Integer getPrecision () {
    return this.__precision;
}

/**
Return the count of the same cells, based on the first comparison table cell indicators.
Do not count the row number column.
@return the count of the same cells.
*/
public int getSameCellCount () {
	// Determine if a row number column is used.
	int rowNumberColumnOffset = 0;
    if ( (this.rowNumberColumn != null) && !this.rowNumberColumn.isEmpty() ) {
    	// Add a row number column to the comparison table(s).
    	rowNumberColumnOffset = 1;
    }

	List<Integer> [] differenceList = getDifferenceList1();
	if ( differenceList == null ) {
		return 0;
	}
	else {
		int sameCount = 0;
		for ( int icol = rowNumberColumnOffset; icol < differenceList.length; icol++ ) {
			for ( int irow = 0; irow < differenceList[icol].size(); irow++ ) {
				if ( differenceList[icol].get(irow) == this.CELL_SAME ) {
					++sameCount;
				}
			}
		}
		return sameCount;
	}
}

/**
Return the count of the same rows, based on the first table's comparison table.
@return the count of the same rows.
*/
public int getSameRowCount () {
	// Determine if a row number column is used.
	int rowNumberColumnOffset = 0;
    if ( (this.rowNumberColumn != null) && !this.rowNumberColumn.isEmpty() ) {
    	// Add a row number column to the comparison table(s).
    	rowNumberColumnOffset = 1;
    }

	List<Integer> [] differenceList = getDifferenceList1();
	if ( differenceList == null ) {
		return 0;
	}
	else {
		int rowSameCount = 0;
		int colSameCount = 0;
		int nCols = differenceList.length - rowNumberColumnOffset;
		for ( int irow = 0; irow < differenceList[0].size(); irow++ ) {
			colSameCount = 0;
			for ( int icol = rowNumberColumnOffset; icol < differenceList.length; icol++ ) {
				if ( differenceList[icol].get(irow) == this.CELL_SAME ) {
					// A column in the row is same.
					++colSameCount;
				}
			}
			if ( colSameCount == nCols ) {
				++rowSameCount;
			}
		}
		return rowSameCount;
	}
}
/**
Return the first table being compared.
@return the first table being compared
*/
public DataTable getTable1 () {
    return this.__table1;
}

/**
Return the second table being compared.
@return the second table being compared
*/
public DataTable getTable2 () {
    return this.__table2;
}

/**
Return the tolerance to use for floating point comparisons.
@return the tolerance to use for floating point comparisons.
*/
private Double getTolerance () {
    return this.__tolerance;
}

/**
 * Determine if any cells in the row have indicator > CELL_SAME (so different).
 * @param diffTable a difference table to check
 * @param differenceList difference list to track cell state
 * @param iRow table row number (0+)
 * @param rowNumberColumnOffset offset in the table columns for the row number column
 */
private boolean ifRowDifferent ( DataTable diffTable, List<Integer>[] differenceList, int iRow, int rowNumberColumnOffset ) {
	// All cells are assumed to be the same until a difference is detected.
	boolean ifRowDifferent = false;
	
	int nCols = diffTable.getNumberOfFields();
	Integer indicator = null;
	for ( int iCol = rowNumberColumnOffset; iCol < nCols; iCol++ ) {
		indicator = differenceList[iCol].get(iRow);
		if ( indicator > this.CELL_SAME ) {
			// Cell is different.
			ifRowDifferent = true;
			break;
		}
	}

	return ifRowDifferent;
}
/**
 * Determine if all the cells in the row have indicator CELL_SAME.
 * @param diffTable a difference table to check
 * @param differenceList difference list to track cell state
 * @param iRow table row number (0+)
 * @param rowNumberColumnOffset offset in the table columns for the row number column
 */
private boolean ifRowSame ( DataTable diffTable, List<Integer>[] differenceList, int iRow, int rowNumberColumnOffset ) {
	// All cells are assumed to be the same until a difference is detected.
	boolean ifRowSame = true;
	
	int nCols = diffTable.getNumberOfFields();
	Integer indicator = null;
	for ( int iCol = rowNumberColumnOffset; iCol < nCols; iCol++ ) {
		indicator = differenceList[iCol].get(iRow);
		if ( indicator > this.CELL_SAME ) {
			// Cell is different.
			ifRowSame = false;
			break;
		}
	}

	return ifRowSame;
}

/**
 * Set the indicator of whether the cell is different, etc., used with the final difference table.
 * @param irow the row index position (0+)
 * @param icol the column index position (0+), must include the row number column if added
 * @param indicator indicator value, one of CELL_*.
 */
private void setCellDifferenceIndicator ( int irow, int icol, Integer indicator ) {
	// Advanced analysis.
	List<Integer> list = (List<Integer>)this.__differenceList[icol];
	// Unlike the other methods, there should be no reason to insert intervening rows
	// because the final difference lists are allocated up front.
	// Set the indicator for the specific existing row.
	if ( indicator == null ) {
		list.set(irow, indicator);
	}
	else {
		list.set(irow, Integer.valueOf(indicator));
	}
}

/**
 * Set the indicator of whether the cell is different, etc., used with the first difference table.
 * @param irow the row index position (0+)
 * @param icol the column index position (0+), must include the row number column if added
 * @param indicator indicator value, one of CELL_*.
 */
private void setCellDifferenceIndicator1 ( int irow, int icol, int indicator ) {
	// If necessary, initialize rows that have not been assigned a value
	/*
	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
		this.__differenceArray[irow][icol] = indicator;
	}
	else {
	*/
		// Advanced analysis.
		List<Integer> list = (List<Integer>)this.__differenceList1[icol];
		if ( irow >= list.size() ) {
			// Add intervening indicators initialized with UNKNOWN:
			// - do not add at 'irow' in the loop since that is added after the loop
			for ( int irow2 = list.size(); irow2 < irow; irow2++ ) {
				list.add(irow2, Integer.valueOf(this.CELL_UNKNOWN));
			}
			// Add the indicator that was specified.
			list.add(Integer.valueOf(indicator));
		}
		else {
			// Set the indicator for the specific existing row.
			list.set(irow, Integer.valueOf(indicator));
		}
	//}
}

/**
 * Set the indicator of whether the cell is different, etc., used with the second difference table.
 * @param irow the row index position (0+)
 * @param icol the column index position (0+), must include the row number column if added
 * @param indicator indicator value, one of CELL_*.
 */
private void setCellDifferenceIndicator2 ( int irow, int icol, int indicator ) {
	// Advanced analysis.
	List<Integer> list = (List<Integer>)this.__differenceList2[icol];
	if ( irow >= list.size() ) {
		// Add intervening indicators initialized with UNKNOWN:
		// - do not add at 'irow' in the loop since that is added after the loop
		for ( int irow2 = list.size(); irow2 < irow; irow2++ ) {
			list.add(irow2, Integer.valueOf(this.CELL_UNKNOWN));
		}
		// Add the indicator that was specified.
		list.add(Integer.valueOf(indicator));
	}
	else {
		// Set the indicator for the specific existing row.
		list.set(irow, Integer.valueOf(indicator));
	}
}

/**
Set the list of columns being compared from the first table.
@param compareColumns1 list of columns being compared from the first table.
*/
private void setCompareColumns1 ( List<String> compareColumns1 ) {
    this.__compareColumns1 = compareColumns1;
}

/**
Set the list of columns being compared from the second table.
@param compareColumns2 list of columns being compared from the second table.
*/
private void setCompareColumns2 ( List<String> compareColumns2 ) {
    this.__compareColumns2 = compareColumns2;
}

/**
Set the final difference table created by this class, corresponding to overlapping 'table1' and 'table2' analysis.
@param diffTable second new comparison table.
*/
private void setDiffTable ( DataTable diffTable ) {
    this.diffTable = diffTable;
}

/**
Set the first difference table created by this class, corresponding to 'table1' analysis.
@param diffTable1 first new difference table.
*/
private void setDiffTable1 ( DataTable diffTable1 ) {
    this.diffTable1 = diffTable1;
}

/**
Set the second difference table created by this class, corresponding to the 'table2' analysis.
@param diffTable2 second new comparison table.
*/
private void setDiffTable2 ( DataTable diffTable2 ) {
    this.diffTable2 = diffTable2;
}

/**
Set whether to match columns by name.
@param matchColumnsByName true to match by name, false to match by order.
*/
private void setMatchColumnsByName ( boolean matchColumnsByName ) {
    this.__matchColumnsByName = matchColumnsByName;
}

/**
Set the name of the first new comparison table being created.
@param newTableID name of the first new comparison table being compared.
*/
private void setNewTableID ( String newTableID ) {
    this.__newTableID = newTableID;
}

/**
Set the name of the second new comparison table being created.
@param newTableID name of the second new comparison table being compared.
*/
private void setNewTable2ID ( String newTable2ID ) {
    this.__newTable2ID = newTable2ID;
}

/**
Set the precision for floating point comparisons.
@param precision the precision for floating point comparisons.
*/
private void setPrecision ( Integer precision ) {
    this.__precision = precision;
}

/**
Set the first table being compared.
@param table1 first table being compared.
*/
private void setTable1 ( DataTable table1 ) {
    this.__table1 = table1;
}

/**
Set the second table being compared.
@param table1 second table being compared.
*/
private void setTable2 ( DataTable table2 ) {
    this.__table2 = table2;
}

/**
Set the tolerance for floating point comparisons.
@param tolerance the tolerance for floating point comparisons.
*/
private void setTolerance ( Double tolerance ) {
    this.__tolerance = tolerance;
}

/**
Write an HTML representation of the final difference table in which different cells are highlighted.
This uses the generic DataTableHtmlWriter with a style mask for the different cells.
@param htmlFile the path to the output file to write
*/
public void writeHtmlDiffFile ( String htmlFile )
throws Exception, IOException {
	// For advanced, must convert the difference lists into a 2D array.
	int nRows = this.__differenceList[0].size();
	int nCols = this.__differenceList.length;
	int [][] differenceArray = new int[nRows][nCols];
	for ( int iCol = 0; iCol < nCols; iCol++ ) {
		List<Integer> list = this.__differenceList[iCol];
		for ( int iRow = 0; iRow < nRows; iRow++ ) {
			differenceArray[iRow][iCol] = list.get(iRow);
		}
	}
	writeHtmlFileInternal ( getDiffTable(), "Data Table - " + getDiffTable().getTableID(),
		differenceArray, htmlFile );
}

/**
Write an HTML representation of the first difference table in which different cells are highlighted.
This uses the generic DataTableHtmlWriter with a style mask for the different cells.
@param htmlFile the path to the output file to write
*/
public void writeHtmlDiffFile1 ( String htmlFile )
throws Exception, IOException {
	/*
	if ( this.analysisType == DataTableComparerAnalysisType.SIMPLE ) {
		// Can use the array as is.
		writeHtmlFileInternal ( getComparisonTable(), "Data Table - " + getComparisonTable().getTableID(),
			getDifferenceArray(), htmlFile );
	}
	else {
	*/
		// Advanced, must convert the difference lists into a 2D array.
		int nRows = this.__differenceList1[0].size();
		int nCols = this.__differenceList1.length;
		int [][] differenceArray = new int[nRows][nCols];
		for ( int iCol = 0; iCol < nCols; iCol++ ) {
			List<Integer> list = this.__differenceList1[iCol];
			for ( int iRow = 0; iRow < nRows; iRow++ ) {
				differenceArray[iRow][iCol] = list.get(iRow);
			}
		}
		writeHtmlFileInternal ( getDiffTable1(), "Data Table - " + getDiffTable1().getTableID(),
			differenceArray, htmlFile );
	//}
}

/**
Write an HTML representation of the second difference table in which different cells are highlighted.
This uses the generic DataTableHtmlWriter with a style mask for the different cells.
@param htmlFile the path to the output file to write
*/
public void writeHtmlDiffFile2 ( String htmlFile )
throws Exception, IOException {
	// For advanced, must convert the difference lists into a 2D array.
	int nRows = this.__differenceList2[0].size();
	int nCols = this.__differenceList2.length;
	int [][] differenceArray = new int[nRows][nCols];
	for ( int iCol = 0; iCol < nCols; iCol++ ) {
		List<Integer> list = this.__differenceList2[iCol];
		for ( int iRow = 0; iRow < nRows; iRow++ ) {
			differenceArray[iRow][iCol] = list.get(iRow);
		}
	}
	writeHtmlFileInternal ( getDiffTable2(), "Data Table - " + getDiffTable2().getTableID(),
		differenceArray, htmlFile );
}

/**
Write an HTML representation of the comparison table in which different cells are highlighted.
This uses the generic DataTableHtmlWriter with a style mask for the different cells.
@param table the table to write
@param differenceArray the difference array to use for styling
@param htmlFile the path to the output file to write
*/
private void writeHtmlFileInternal ( DataTable table, String title, int [][] differenceArray, String htmlFile )
throws Exception, IOException {
    DataTableHtmlWriter tableWriter = new DataTableHtmlWriter(table);
    HashMap<Integer,String> stylesMap = new LinkedHashMap<>();
    // Create styles to format the output cells:
    // - the numbers must match the CELL_*
    // - the 'customStyleText' below must include classes for the map values shown below
    // - OK to use name since style will be applied in each comparison table
    stylesMap.put(Integer.valueOf(CELL_ERROR), "error");
    stylesMap.put(Integer.valueOf(CELL_UNKNOWN), "unknown");
    stylesMap.put(Integer.valueOf(CELL_SAME), "");
    stylesMap.put(Integer.valueOf(CELL_DIFFERENT), "diff");
    stylesMap.put(Integer.valueOf(CELL_INSERT_EMPTY_ROW_TABLE1), "emptyrow");
    stylesMap.put(Integer.valueOf(CELL_INSERT_EMPTY_ROW_TABLE2), "emptyrow");
    stylesMap.put(Integer.valueOf(CELL_NO_ROW_TABLE1), "norow");
    stylesMap.put(Integer.valueOf(CELL_NO_ROW_TABLE2), "norow");
    stylesMap.put(Integer.valueOf(CELL_ROW_ONLY_IN_TABLE1), "rowin1");
    stylesMap.put(Integer.valueOf(CELL_ROW_ONLY_IN_TABLE2), "rowin2");

    String customStyleText =
    	".diff { /* Cell value is different in the tables. */\n" +
    	"  background-color: yellow;\n" +
    	"}\n" +
    	".emptyrow { /* Inserted empty row to align tables (light gray). */\n" +
    	"  background-color: #cccccc;\n" +
    	"  padding: 8px; /* Use to ensure that empty rows have some height. */\n" +
    	"}\n" +
    	".error { /* Error analyzing the cell. */\n" +
    	"  background-color: red;\n" +
    	"}\n" +
    	".norow { /* No row in the table, used with different length input. */\n" +
    	"  background-color: black;\n" +
    	"  padding: 8px; /* Use to ensure that empty rows have some height. */\n" +
    	"}\n" +
    	".rowin1 { /* Row is in only in table1 (light blue). */\n" +
    	"  background-color: #b3d9ff;\n" +
    	"}\n" +
    	".rowin2 { /* Row is in only in table2 (light green). */\n" +
    	"  background-color: #80ff80;\n" +
    	"}\n" +
    	".unknown { /* Unknown cell status (not analyzed, dark grey). */\n" +
    	"  background-color: #999999;\n" +
    	"}\n";
    tableWriter.writeHtmlFile (
    	htmlFile,
    	title,
        true,
        null, // No comments.
        differenceArray,
        stylesMap,
        customStyleText );
}

}