package RTi.Util.Table;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import RTi.Util.String.StringUtil;

/**
Compare two tables for differences and create a new table that contains the comparison.
This table can be output to a simple HTML format to provide a visual way to find specific differences.
The comparison is currently not very complicated.  Tables are assumed to have consistent column definitions
and numbers of rows, although the comparison is done on strings so column types don't necessarily have to be
the same.
*/
public class DataTableComparer
{
    
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
private List<String>__compareColumns1;

/**
The list of column names to compare from the second table.
*/
private List<String>__compareColumns2;

/**
The name of the new table to be created.
*/
private String __newTableID = "";

/**
The precision to use when comparing floating point numbers.
*/
private Integer __precision = null;

/**
The tolerance to use when comparing floating point numbers.
*/
private Double __tolerance = null;
    
/**
The comparison table that is created.  Can be null if not yet compared.
*/
private DataTable __comparisonTable;

/**
Array that indicates differences in the cells.  This is used for formatting output.
It is an integer and not boolean because in the future more care may be implemented
to allow tolerances in differences and consequently the table could be visualized with
different colors depending on the level of difference.
For each cell a value of 0 indicates no difference and 1 indicates different.
*/
private int [][] __differenceArray;
    
/**
Create the data table comparer instance and check for initialization problems.
@param table1 first table for comparison
@param table2 second table for comparison
@param compareColumns1 list of column names from the first table to compare
@param compareColumns2 list of column names from the second table to compare
@param precision the number of digits (1+) after the decimal point to compare numbers in floating point columns
(specify as null to ignore precision comparison)
@param tolerance the absolute value to check differences between floating point numbers (if not specified then
values must be exact when checked to the precision)
@param newTableID name of new table to create with comparison results
*/
public DataTableComparer ( DataTable table1, DataTable table2, List<String>compareColumns1,
    List<String> compareColumns2, Integer precision, Double tolerance, String newTableID )
{
    // The tables being compared must not be null
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
    // Get the column names to compare, which will either be those passed in by calling code, or if not specified
    // will be the full list.
    if ( table1 != null ) {
        if ( compareColumns1 == null ) {
            // Get the columns from the first table
            if ( table1 != null ) {
                compareColumns1 = Arrays.asList(table1.getFieldNames());
            }
        }
        else {
            // Confirm that the columns exist
            StringBuffer warning = new StringBuffer();
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
    }
    setCompareColumns1 ( compareColumns1 );
    if ( table2 != null ) {
        if ( compareColumns2 == null ) {
            // Get the columns from the second table
            if ( table2 != null ) {
                compareColumns2 = Arrays.asList(table2.getFieldNames());
            }
        }
        else {
            // Confirm that the columns exist
            StringBuffer warning = new StringBuffer();
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
    }
    setCompareColumns2 ( compareColumns2 );
    // The list of columns must be the same length
    if ( compareColumns1.size() != compareColumns2.size() ) {
        throw new InvalidParameterException( "The number of columns to compare from the first table (" +
            compareColumns1.size() + ") is not the same as the number of columns to compare from the second table (" +
            compareColumns2.size() + ").");
    }
    // The precision must be 0+
    if ( (precision != null) && (precision < 0) ) {
        throw new InvalidParameterException( "The precision (" + precision + ") if specified must be >= 0).");
    }
    setPrecision ( precision );
    // The tolerance must be 0+
    if ( (tolerance != null) && (tolerance < 0.0) ) {
        throw new InvalidParameterException( "The tolerance (" + tolerance + ") if specified must be >= 0).");
    }
    setTolerance ( tolerance );
    // The new table ID must be specified because the table use is controlled by the calling code and
    // an identifier conflict because of an assumed name should not be introduced here
    if ( (newTableID == null) || newTableID.equals("") ) {
        throw new InvalidParameterException( "The new table ID is null or blank." );
    }
    else {
        setNewTableID ( newTableID );
    }
}

/**
Perform the comparison, creating the output table.
*/
public void compare ()
throws Exception
{
    // At this point the inputs should be OK so create a new table that has columns that
    // include both of the original column names but are of type string
    DataTable table1 = getTable1();
    DataTable table2 = getTable2();
    DataTable comparisonTable = new DataTable ();
    comparisonTable.setTableID ( getNewTableID() );
    List<String> compareColumns1 = getCompareColumns1();
    List<String> compareColumns2 = getCompareColumns2();
    int[] columnNumbers1 = table1.getFieldIndices((String [])compareColumns1.toArray());
    int[] columnNumbers2 = table2.getFieldIndices((String [])compareColumns2.toArray());
    String[] fieldFormats1 = table1.getFieldFormats(); // C-style formats to convert data to strings for comparison
    String[] fieldFormats2 = table2.getFieldFormats();
    Integer precision = getPrecision();
    Double tolerance = getTolerance();
    if ( (precision != null) && (precision >= 0) ) {
        // Update the field formats to use the requested precision, if a floating point field
        String fieldFormat = "%." + precision + "f";
        for ( int i = 0; i < columnNumbers1.length; i++ ) {
            if ( (table1.getFieldDataType(columnNumbers1[i]) == TableField.DATA_TYPE_DOUBLE) ||
                (table1.getFieldDataType(columnNumbers1[i]) == TableField.DATA_TYPE_FLOAT) ) {
                fieldFormats1[i] = fieldFormat;
            }
        }
        for ( int i = 0; i < columnNumbers2.length; i++ ) {
            if ( (table2.getFieldDataType(columnNumbers2[i]) == TableField.DATA_TYPE_DOUBLE) ||
                (table2.getFieldDataType(columnNumbers2[i]) == TableField.DATA_TYPE_FLOAT) ) {
                fieldFormats2[i] = fieldFormat;
            }
        }
    }
    // Create an int array to track whether the cells are different (initial value is 0)
    // This is used as a style mask when formatting the HTML
    int [][] differenceArray = new int[table1.getNumberOfRecords()][compareColumns1.size()];
    // Loop through the column lists, which should be the same size and define columns
    for ( int icol = 0; icol < compareColumns1.size(); icol++ ) {
        // Define columns of type string (no width specified), where the column name will be a simple
        // concatenation of both column names, or one name if the column names for table1 and table2 match
        String colName = compareColumns1.get(icol);
        if ( !colName.equals(compareColumns2.get(icol))) {
            colName += "/" + compareColumns2.get(icol);
        }
        comparisonTable.addField(new TableField(TableField.DATA_TYPE_STRING, colName,-1), "");
    }
    // Now loop through the records in table 1 and compare
    String formattedValue1;
    String formattedValue2;
    String formattedValue = null; // The comparison output
    Object value1;
    Object value2;
    for ( int irow = 0; irow < table1.getNumberOfRecords(); ++irow ) {
        for ( int icol = 0; icol < columnNumbers1.length; icol++ ) {
            // Get the value from the first table and format as a string for comparisons...
            try {
                value1 = table1.getFieldValue(irow, icol);
            }
            catch ( Exception e ) {
                value1 = "";
            }
            if ( value1 == null ) {
                formattedValue1 = "";
            }
            else {
                // TODO SAM 2010-12-18 Evaluate why trim is needed
                formattedValue1 = StringUtil.formatString(value1,fieldFormats1[columnNumbers1[icol]]).trim();
            }
            // Get the value from the second table and format as a string for comparisons...
            try {
                value2 = table2.getFieldValue(irow, icol);
            }
            catch ( Exception e ) {
                value2 = "";
            }
            if ( value2 == null ) {
                formattedValue2 = "";
            }
            else {
                formattedValue2 = StringUtil.formatString(value2,fieldFormats2[columnNumbers2[icol]]).trim();
            }
            // Default behavior is to compare strings so do this check first.
            if ( formattedValue1.equals(formattedValue2) ) {
                // Formatted values are the same so the output table value is just the formatted value
                formattedValue = formattedValue1;
            }
            else {
                // Show both values as "value1 / value2" and set the boolean indicating a difference
                if ( ((table1.getFieldDataType(columnNumbers1[icol]) == TableField.DATA_TYPE_DOUBLE) ||
                    (table1.getFieldDataType(columnNumbers1[icol]) == TableField.DATA_TYPE_FLOAT)) &&
                    (tolerance != null) &&
                    StringUtil.isDouble(formattedValue1) && StringUtil.isDouble(formattedValue2)) {
                    // Convert the formatted strings to doubles and compare the difference against the tolerance
                    double dvalue1 = Double.parseDouble(formattedValue1);
                    double dvalue2 = Double.parseDouble(formattedValue2);
                    if ( Math.abs(dvalue1 - dvalue2) >= tolerance ) {
                        formattedValue = formattedValue1 + " / " + formattedValue2;
                        differenceArray[irow][icol] = 1;
                    }
                    else {
                        // Still show both values but don't set the difference flag since tolerance is met
                        // Indicate the tolerance in output so review is not confused
                        formattedValue = formattedValue1 + " / " + formattedValue2 + " (= for tol=" + tolerance + ")";
                    }
                }
                else {
                    // Not floating point or floating point and no tolerance is specified so no need to do
                    // additional comparison
                    formattedValue = formattedValue1 + " / " + formattedValue2;
                    differenceArray[irow][icol] = 1;
                }
            }
            // Set the field value, creating the row if necessary
            comparisonTable.setFieldValue(irow, icol, formattedValue, true);
            //Message.printStatus(2, "", "formattedValue1=\"" + formattedValue1 +
            //    "\" formattedValue2=\"" + formattedValue2 + "\" mask=" + differenceArray[irow][icol]  );
        }
    }
    setComparisonTable ( comparisonTable );
    setDifferenceArray ( differenceArray );
}

/**
Get the list of columns to be compared from the first table.
*/
private List<String> getCompareColumns1 ()
{
    return __compareColumns1;
}

/**
Get the list of columns to be compared from the second table.
*/
private List<String> getCompareColumns2 ()
{
    return __compareColumns2;
}

/**
Return the comparison table.
@return the comparison table.
*/
public DataTable getComparisonTable ()
{
    return __comparisonTable;
}

/**
Return the difference array.
@return the difference array.
*/
private int [][] getDifferenceArray ()
{
    return __differenceArray;
}

/**
Return the count of the differences.
@return the count of the differences.
*/
public int getDifferenceCount ()
{
    int [][] differenceArray = getDifferenceArray();
    if ( differenceArray == null ) {
        return 0;
    }
    else {
        int differenceCount = 0;
        for ( int irow = 0; irow < differenceArray.length; irow++ ) {
            for ( int icol = 0; icol < differenceArray[irow].length; icol++ ) {
                if ( differenceArray[irow][icol] > 0 ) {
                    ++differenceCount;
                }
            }
        }
        return differenceCount;
    }
}

/**
Return the identifier to be used for the new comparison table.
@return the identifier to be used for the new comparison table.
*/
private String getNewTableID ()
{
    return __newTableID;
}

/**
Return the precision to use for floating point comparisons.
@return the precision to use for floating point comparisons.
*/
private Integer getPrecision ()
{
    return __precision;
}

/**
Return the first table being compared.
*/
public DataTable getTable1 ()
{
    return __table1;
}

/**
Return the second table being compared.
*/
public DataTable getTable2 ()
{
    return __table2;
}

/**
Return the tolerance to use for floating point comparisons.
@return the tolerance to use for floating point comparisons.
*/
private Double getTolerance ()
{
    return __tolerance;
}

/**
Set the list of columns being compared from the first table.
@param compareColumns1 list of columns being compared from the first table.
*/
private void setCompareColumns1 ( List<String> compareColumns1 )
{
    __compareColumns1 = compareColumns1;
}

/**
Set the list of columns being compared from the second table.
@param compareColumns2 list of columns being compared from the second table.
*/
private void setCompareColumns2 ( List<String> compareColumns2 )
{
    __compareColumns2 = compareColumns2;
}

/**
Set the comparison table created by this class.
@param comparisonTable new comparison table.
*/
private void setComparisonTable ( DataTable comparisonTable )
{
    __comparisonTable = comparisonTable;
}

/**
Set the difference array.
@param differenceArray the difference array.
*/
private void setDifferenceArray ( int [][] differenceArray )
{
    __differenceArray = differenceArray;
}

/**
Set the name of the new comparison table being created.
@param newTableID name of the new comparison table being compared.
*/
private void setNewTableID ( String newTableID )
{
    __newTableID = newTableID;
}

/**
Set the precision for floating point comparisons.
@param precision the precision for floating point comparisons.
*/
private void setPrecision ( Integer precision )
{
    __precision = precision;
}

/**
Set the first table being compared.
@param table1 first table being compared.
*/
private void setTable1 ( DataTable table1 )
{
    __table1 = table1;
}

/**
Set the second table being compared.
@param table1 second table being compared.
*/
private void setTable2 ( DataTable table2 )
{
    __table2 = table2;
}

/**
Set the tolerance for floating point comparisons.
@param tolerance the tolerance for floating point comparisons.
*/
private void setTolerance ( Double tolerance )
{
    __tolerance = tolerance;
}

// TODO SAM 2011-12-23 Enable colors that indicate amount of difference
// Could be two colors (positive difference, negative difference) or shades based on degree of difference
// (based on tolerance?).
/**
Write an HTML representation of the comparison table in which different cells are highlighted.
This uses the generic DataTableHtmlWriter with a style mask for the different cells.
*/
public void writeHtmlFile ( String htmlFile )
throws Exception, IOException
{
    DataTableHtmlWriter tableWriter = new DataTableHtmlWriter(getComparisonTable());
    String [] styles = { "", "diff" };
    String customStyleText = ".diff { background-color:red; }\n";
    tableWriter.writeHtmlFile(htmlFile,
        true,
        null, // No comments
        getDifferenceArray(),
        styles,
        customStyleText );
}

}