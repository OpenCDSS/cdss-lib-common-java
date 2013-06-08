package RTi.Util.Table;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Format table columns to create an output column string.
*/
public class DataTableStringFormatter
{
    
/**
Data table on which to perform math.
*/
private DataTable __table = null;

/**
Construct an instance using the table to operate on.
*/
public DataTableStringFormatter ( DataTable table )
{
    __table = table;
}

/**
Format a string
@param inputColumns the name of the first column to use as input
@param format the operator to execute for processing data
@param inputColumn2 the name of the second column to use as input (if input2 is not specified), or null if not used
@param inputValue2 the constant input to use as input (if inputColumn2 is not specified), or null if not used
@param outputColumn the name of the output column
@param problems a list of strings indicating problems during processing
*/
public void format ( String [] inputColumns, String format, String outputColumn, List<String> problems )
{   String routine = getClass().getName() + ".format" ;
    // Look up the columns for input and output
    int [] inputColumnNum = new int[inputColumns.length];
    for ( int i = 0; i < inputColumns.length; i++ ) {
        inputColumnNum[i] = -1;
        try {
            inputColumnNum[i] = __table.getFieldIndex(inputColumns[i]);
        }
        catch ( Exception e ) {
            problems.add ( "Input column (1) \"" + inputColumns + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    int outputColumnNum = -1;
    try {
        outputColumnNum = __table.getFieldIndex(outputColumn);
    }
    catch ( Exception e ) {
        Message.printStatus(2, routine, "Output column \"" + outputColumn + "\" not found in table \"" +
            __table.getTableID() + "\" - automatically adding." );
            __table.addField(new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
        try {
            outputColumnNum = __table.getFieldIndex(outputColumn);
        }
        catch ( Exception e2 ) {
            // Should not happen.
            problems.add ( "Output column \"" + outputColumn + "\" not found in table \"" + __table.getTableID() + "\".  Error addung column." );
        }
    }
    
    if ( problems.size() > 0 ) {
        // Return if any problems were detected
        return;
    }

    // Loop through the records, get the input column objects, and format for output
    int nrec = __table.getNumberOfRecords();
    String outputVal = null;
    List<Object> values = new Vector<Object>();
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Get the input values
        values.clear();
        for ( int iCol = 0; iCol < inputColumnNum.length; iCol++ ) {
            if ( inputColumnNum[iCol] < 0 ) {
                // Set the result to null
                values.clear();
                break;
            }
            else {
                try {
                    values.add(__table.getFieldValue(irec, inputColumnNum[iCol]));
                }
                catch ( Exception e ) {
                    problems.add ( "Error getting value for input column (" + e + ")." );
                    values.clear();
                    break;
                }
            }
        }
        if ( values.size() == 0 ) {
            outputVal = null;
        }
        else {
            //Message.printStatus(2, routine, "format=\"" + format + "\"" );
            //for ( int i = 0; i < values.size(); i++ ) {
            //    Message.printStatus(2, routine, "value=\"" + values.get(i) + "\"" );
            //}
            outputVal = StringUtil.formatString(values,format);
        }
        // Set the value...
        try {
            __table.setFieldValue(irec, outputColumnNum, outputVal );
        }
        catch ( Exception e ) {
            problems.add ( "Error setting value (" + e + ")." );
        }
    }   
}

}