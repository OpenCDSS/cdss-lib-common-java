package RTi.Util.Table;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;

/**
Perform simple column-based string operations on a table.
*/
public class DataTableStringManipulation
{
    
/**
Data table on which to perform math.
*/
private DataTable __table = null;

/**
Construct an instance using the table to operate on.
*/
public DataTableStringManipulation ( DataTable table )
{
    __table = table;
}

/**
Get the list of operators that can be used.
*/
public static List<DataTableStringOperatorType> getOperatorChoices()
{
    List<DataTableStringOperatorType> choices = new Vector();
    choices.add ( DataTableStringOperatorType.APPEND );
    choices.add ( DataTableStringOperatorType.PREPEND );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings()
{
    List<DataTableStringOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Perform a string manipulation.
@param inputColumn1 the name of the first column to use as input
@param operator the operator to execute for processing data
@param inputColumn2 the name of the second column to use as input (if input2 is not specified)
@param input2 the constant input to use as input (if inputColumn2 is not specified)
@param outputColumn the name of the output column
@param problems a list of strings indicating problems during processing
*/
public void manipulate ( String inputColumn1, DataTableStringOperatorType operator,
    String inputColumn2, String input2, String outputColumn, List<String> problems )
{   String routine = getClass().getName() + ".manipulate" ;
    // Look up the columns for input and output
    int input1Field = -1;
    try {
        input1Field = __table.getFieldIndex(inputColumn1);
    }
    catch ( Exception e ) {
        problems.add ( "Input field (1) \"" + inputColumn1 + "\" not found in table \"" + __table.getTableID() + "\"" );
    }
    int input2Field = -1;
    if ( inputColumn2 != null ) {
        try {
            input2Field = __table.getFieldIndex(inputColumn2);
        }
        catch ( Exception e ) {
            problems.add ( "Input field (2) \"" + inputColumn2 + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    int outputField = -1;
    try {
        outputField = __table.getFieldIndex(outputColumn);
    }
    catch ( Exception e ) {
        Message.printStatus(2, routine, "Output field \"" + outputColumn + "\" not found in table \"" +
            __table.getTableID() + "\" - automatically adding." );
        // Automatically add to the table, initialize with null
        __table.addField(new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
        try {
            outputField = __table.getFieldIndex(outputColumn);
        }
        catch ( Exception e2 ) {
            // Should not happen.
            problems.add ( "Output field \"" + outputColumn + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    
    if ( problems.size() > 0 ) {
        // Return if any problems were detected
        return;
    }

    // Loop through the records
    int nrec = __table.getNumberOfRecords();
    Object val;
    String input1Val;
    String input2Val;
    String outputVal = null;
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Initialize the values
        input1Val = null;
        input2Val = null;
        outputVal = null;
        // Get the input values
        try {
            val = __table.getFieldValue(irec, input1Field);
            input1Val = (String)val;
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input field 1 (" + e + ")." );
            continue;
        }
        try {
            if ( input2 != null ) {
                input2Val = input2;
            }
            else {
                val = __table.getFieldValue(irec, input2Field);
                input2Val = (String)val;
            }
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input field 2 (" + e + ")." );
            continue;
        }
        // Check for missing values and compute the output
        if ( (input1Val == null) || (input2Val == null) ) {
            outputVal = null;
        }
        else if ( operator == DataTableStringOperatorType.APPEND ) {
            outputVal = input1Val + input2Val;
        }
        else if ( operator == DataTableStringOperatorType.PREPEND ) {
            outputVal = input2Val + input1Val;
        }
        // Set the value...
        try {
            __table.setFieldValue(irec, outputField, outputVal );
        }
        catch ( Exception e ) {
            problems.add ( "Error setting value (" + e + ")." );
        }
    }   
}

}