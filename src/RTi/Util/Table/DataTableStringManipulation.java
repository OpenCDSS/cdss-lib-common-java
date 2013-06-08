package RTi.Util.Table;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

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
    List<DataTableStringOperatorType> choices = new Vector<DataTableStringOperatorType>();
    choices.add ( DataTableStringOperatorType.APPEND );
    choices.add ( DataTableStringOperatorType.PREPEND );
    choices.add ( DataTableStringOperatorType.TO_DATE );
    choices.add ( DataTableStringOperatorType.TO_DATE_TIME );
    choices.add ( DataTableStringOperatorType.TO_DOUBLE );
    choices.add ( DataTableStringOperatorType.TO_INTEGER );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings()
{
    List<DataTableStringOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new Vector<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Perform a string manipulation.
@param inputColumn1 the name of the first column to use as input
@param operator the operator to execute for processing data
@param inputColumn2 the name of the second column to use as input (if input2 is not specified), or null if not used
@param inputValue2 the constant input to use as input (if inputColumn2 is not specified), or null if not used
@param outputColumn the name of the output column
@param problems a list of strings indicating problems during processing
*/
public void manipulate ( String inputColumn1, DataTableStringOperatorType operator,
    String inputColumn2, String inputValue2, String outputColumn, List<String> problems )
{   String routine = getClass().getName() + ".manipulate" ;
    // Look up the columns for input and output
    int input1ColumnNum = -1;
    try {
        input1ColumnNum = __table.getFieldIndex(inputColumn1);
    }
    catch ( Exception e ) {
        problems.add ( "Input column (1) \"" + inputColumn1 + "\" not found in table \"" + __table.getTableID() + "\"" );
    }
    int input2ColumnNum = -1;
    if ( inputColumn2 != null ) {
        try {
            input2ColumnNum = __table.getFieldIndex(inputColumn2);
        }
        catch ( Exception e ) {
            problems.add ( "Input column (2) \"" + inputColumn2 + "\" not found in table \"" + __table.getTableID() + "\"" );
        }
    }
    int outputColumnNum = -1;
    try {
        outputColumnNum = __table.getFieldIndex(outputColumn);
    }
    catch ( Exception e ) {
        Message.printStatus(2, routine, "Output column \"" + outputColumn + "\" not found in table \"" +
            __table.getTableID() + "\" - automatically adding." );
        // Automatically add to the table, initialize with null
        if ( operator == DataTableStringOperatorType.TO_INTEGER ) {
            __table.addField(new TableField(TableField.DATA_TYPE_INT,outputColumn,-1,-1), null );
        }
        else if ( (operator == DataTableStringOperatorType.TO_DATE) ||
            (operator == DataTableStringOperatorType.TO_DATE_TIME) ) {
            // Precision is handled by precision on individual date/time objects
            __table.addField(new TableField(TableField.DATA_TYPE_DATETIME,outputColumn,-1,-1), null );
        }
        else if ( operator == DataTableStringOperatorType.TO_DOUBLE ) {
            __table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,outputColumn,-1,6), null );
        }
        else {
            __table.addField(new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
        }
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

    // Loop through the records
    int nrec = __table.getNumberOfRecords();
    Object val;
    String input1Val;
    String input2Val;
    Object outputVal = null;
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Initialize the values
        input1Val = null;
        input2Val = null;
        outputVal = null;
        // Get the input values
        try {
            val = __table.getFieldValue(irec, input1ColumnNum);
            input1Val = (String)val;
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input column 1 (" + e + ")." );
            continue;
        }
        try {
            if ( inputValue2 != null ) {
                input2Val = inputValue2;
            }
            else if ( input2ColumnNum >= 0 ){
                val = __table.getFieldValue(irec, input2ColumnNum);
                input2Val = (String)val;
            }
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input column 2 (" + e + ")." );
            continue;
        }
        // Check for missing values and compute the output
        if ( (input1Val == null)  ) {
            outputVal = null;
        }
        else if ( operator == DataTableStringOperatorType.APPEND ) {
            if ( input2Val == null ) {
                outputVal = null;
            }
            else {
                outputVal = input1Val + input2Val;
            }
        }
        else if ( operator == DataTableStringOperatorType.PREPEND ) {
            if ( input2Val == null ) {
                outputVal = null;
            }
            else {
                outputVal = input2Val + input1Val;
            }
        }
        else if ( (operator == DataTableStringOperatorType.TO_DATE) ||
            (operator == DataTableStringOperatorType.TO_DATE_TIME)) {
            try {
                outputVal = DateTime.parse(input1Val);
                // TODO SAM 2013-05-13 Evaluate whether this is needed since string should parse
                //if ( operator == DataTableStringOperatorType.TO_DATE ) {
                //    ((DateTime)outputVal).setPrecision(DateTime.PRECISION_DAY);
                //}
            }
            catch ( Exception e ) {
                outputVal = null;
            }
        }
        else if ( operator == DataTableStringOperatorType.TO_DOUBLE ) {
            try {
                outputVal = Double.parseDouble(input1Val);
            }
            catch ( NumberFormatException e ) {
                outputVal = null;
            }
        }
        else if ( operator == DataTableStringOperatorType.TO_INTEGER ) {
            try {
                outputVal = Integer.parseInt(input1Val);
            }
            catch ( NumberFormatException e ) {
                outputVal = null;
            }
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