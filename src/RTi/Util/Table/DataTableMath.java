package RTi.Util.Table;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Perform simple column-based math operations on a table.
*/
public class DataTableMath
{
    
/**
Data table on which to perform math.
*/
private DataTable __table = null;

/**
Construct an instance using the table to operate on.
*/
public DataTableMath ( DataTable table )
{
    __table = table;
}

/**
Get the list of operators that can be used.
*/
public static List<DataTableMathOperatorType> getOperatorChoices()
{
    List<DataTableMathOperatorType> choices = new Vector();
    choices.add ( DataTableMathOperatorType.ADD );
    choices.add ( DataTableMathOperatorType.SUBTRACT );
    choices.add ( DataTableMathOperatorType.MULTIPLY );
    choices.add ( DataTableMathOperatorType.DIVIDE );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings()
{
    List<DataTableMathOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Perform a math calculation.
@param input1 the name of the first column to use as input
@param operator the operator to execute for processing data
@param input2 the name of the second column to use as input, or a constant
@param output the name of the output column
@param nonValue value to assign when floaging point numbers cannot be computed (null or Double.NaN)
@param problems a list of strings indicating problems during processing
*/
public void math ( String input1, DataTableMathOperatorType operator, String input2, String output,
    Double nonValue, List<String> problems )
{   String routine = getClass().getName() + ".math" ;
    // Look up the columns for input and output
    int input1Field = -1;
    try {
        input1Field = __table.getFieldIndex(input1);
    }
    catch ( Exception e ) {
        problems.add ( "Input field (1) \"" + input1 + "\" not found in table \"" + __table.getTableID() + "\"" );
    }
    int input2Field = -1;
    Double input2Double = null;
    if ( StringUtil.isDouble(input2) ) {
        input2Double = Double.parseDouble(input2);
    }
    else {
        try {
            input2Field = __table.getFieldIndex(input2);
        }
        catch ( Exception e ) {
            problems.add ( "Input field (2) \"" + input2 + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    int outputField = -1;
    try {
        outputField = __table.getFieldIndex(output);
    }
    catch ( Exception e ) {
        Message.printStatus(2, routine, "Output field \"" + output + "\" not found in table \"" +
            __table.getTableID() + "\" - automatically adding." );
        // Automatically add to the table, initialize with null (not nonValue)
        __table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,output,10,4), null );
        try {
            outputField = __table.getFieldIndex(output);
        }
        catch ( Exception e2 ) {
            // Should not happen.
            problems.add ( "Output field \"" + output + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    
    if ( problems.size() > 0 ) {
        // Return if any problems were detected
        return;
    }

    // Loop through the records
    int nrec = __table.getNumberOfRecords();
    Object val;
    Double input1Val;
    Double input2Val;
    Double outputVal = Double.NaN;
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Initialize the values
        input1Val = Double.NaN;
        input2Val = Double.NaN;
        outputVal = nonValue;
        // Get the input values
        try {
            val = __table.getFieldValue(irec, input1Field);
            input1Val = (Double)val;
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input field 1 (" + e + ")." );
            continue;
        }
        try {
            if ( input2Double != null ) {
                input2Val = input2Double;
            }
            else {
                val = __table.getFieldValue(irec, input2Field);
                input2Val = (Double)val;
            }
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input field 2 (" + e + ")." );
            continue;
        }
        // Check for missing values and compute the output
        if ( (input1Val == null) || input1Val.isNaN() || (input2Val == null) || input2Val.isNaN() ) {
            outputVal = nonValue;
        }
        else if ( operator == DataTableMathOperatorType.ADD ) {
            outputVal = input1Val + input2Val;
        }
        else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
            outputVal = input1Val - input2Val;
        }
        else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
            outputVal = input1Val * input2Val;
        }
        else if ( operator == DataTableMathOperatorType.DIVIDE ) {
            if ( input2Val == 0.0 ) {
                outputVal = nonValue;
            }
            else {
                outputVal = input1Val / input2Val;
            }
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