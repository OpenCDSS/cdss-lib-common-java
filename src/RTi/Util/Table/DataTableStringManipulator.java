package RTi.Util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Perform simple column-based string manipulation on a table.
*/
public class DataTableStringManipulator
{
    
/**
Data table on which to perform math.
*/
private DataTable __table = null;

/**
Filter to include rows.
*/
private StringDictionary __columnIncludeFilters = null;

/**
Filter to exclude rows.
*/
private StringDictionary __columnExcludeFilters = null;

/**
Construct an instance.
@param table table that is being manipulated
@param columnIncludeFilters a list of filters that will be checked to include rows
@param columnIncludeFilters a list of filters that will be checked to exclude rows
*/
public DataTableStringManipulator ( DataTable table, StringDictionary columnIncludeFilters, StringDictionary columnExcludeFilters )
{
    __table = table;
    __columnIncludeFilters = columnIncludeFilters;
    __columnExcludeFilters = columnExcludeFilters;
}

/**
Get the list of operators that can be used.
*/
public static List<DataTableStringOperatorType> getOperatorChoices()
{
    List<DataTableStringOperatorType> choices = new Vector<DataTableStringOperatorType>();
    choices.add ( DataTableStringOperatorType.APPEND );
    choices.add ( DataTableStringOperatorType.PREPEND );
    choices.add ( DataTableStringOperatorType.REPLACE );
    choices.add ( DataTableStringOperatorType.REMOVE );
    choices.add ( DataTableStringOperatorType.SPLIT );
    choices.add ( DataTableStringOperatorType.SUBSTRING );
    // TODO SAM 2015-04-29 Need to enable boolean
    //choices.add ( DataTableStringOperatorType.TO_BOOLEAN );
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
    List<String> stringChoices = new ArrayList<String>();
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
@param inputValue3 additional constant input to use as input, or null if not used
@param outputColumn the name of the output column
@param problems a list of strings indicating problems during processing
*/
public void manipulate ( String inputColumn1, DataTableStringOperatorType operator,
    String inputColumn2, String inputValue2, String inputValue3, String outputColumn, List<String> problems )
{   String routine = getClass().getSimpleName() + ".manipulate" ;
	// Construct the filter
	DataTableFilter filter = null;
	try {
		filter = new DataTableFilter ( __table, __columnIncludeFilters, __columnExcludeFilters );
	}
	catch ( Exception e ) {
		// If any problems are detected then processing will be stopped below
		problems.add(e.getMessage());
	}
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
        // TODO SAM 2015-04-29 Need to enable Boolean
        //if ( operator == DataTableStringOperatorType.TO_BOOLEAN ) {
        //    __table.addField(new TableField(TableField.DATA_TYPE_BOOLEAN,outputColumn,-1,-1), null );
        //}
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
    
    // Check for special cases on input, for example ^ and $ are used with replace
    // In these cases, remove the special characters
    boolean replaceStart = false;
    boolean replaceEnd = false;
	if ( (operator == DataTableStringOperatorType.REPLACE) || (operator == DataTableStringOperatorType.REMOVE) ) {
    	if ( inputValue2 != null ) {
    		if ( inputValue2.startsWith("^") ) {
	    		replaceStart = true;
	    		inputValue2 = inputValue2.substring(1,inputValue2.length());
    		}
    		else if ( inputValue2.endsWith("$") ) {
	    		replaceEnd = true;
	    		inputValue2 = inputValue2.substring(0,inputValue2.length()-1);
    		}
    		// Also replace "\s" with single space
    		inputValue2 = inputValue2.replace("\\s"," ");
    	}
    	if ( operator == DataTableStringOperatorType.REMOVE ) {
    		// Same as substring but second string is a space
    		inputValue3 = "";
    	}
    	else {
	    	if ( inputValue3 != null ) {
	    		// Also replace "\ " with single space, anywhere in the output
	    		inputValue3 = inputValue3.replace("\\s"," ");
	    	}
    	}
	}

    // Loop through the records
    int nrec = __table.getNumberOfRecords();
    Object val = null;
    String input1Val = null;
    String input2Val = null;
    String input3Val = null;
    Object outputVal = null;
    int maxChars = -1; // Maximum string length of output
    for ( int irec = 0; irec < nrec; irec++ ) {
    	// Check whether row should be included/excluded - "true" below indicates to throw exceptions
    	try {
	    	if ( !filter.includeRow(irec,true) ) {
	    		continue;
	    	}
    	}
    	catch ( Exception e ) {
    		problems.add(e.getMessage());
    	}
        // Initialize the values
        input1Val = null;
        input2Val = null;
        input3Val = null;
        outputVal = null;
        // Get the input values
        try {
            val = __table.getFieldValue(irec, input1ColumnNum);
            if ( val == null ) {
            	input1Val = null;
            }
            else {
            	input1Val = "" + val; // Do this way so that even non-strings can be manipulated
            }
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input column 1 (" + e + ")." );
            continue;
        }
        try {
            if ( inputValue2 != null ) {
                input2Val = inputValue2;
            }
            else if ( input2ColumnNum >= 0 ) {
            	// Constant value was not given so get from column
                val = __table.getFieldValue(irec, input2ColumnNum);
                if ( val == null ) {
                	input2Val = null;
                }
                else {
                	input2Val = "" + val;
                }
            }
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input column 2 (" + e + ")." );
            continue;
        }
        if ( inputValue3 != null ) {
        	// Only constant value is allowed (not from column)
            input3Val = inputValue3;
        }
        // Check for missing values and compute the output
        if ( input1Val == null ) {
        	// Output is null regardless of the operator
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
        else if ( (operator == DataTableStringOperatorType.REPLACE) ||
        	(operator == DataTableStringOperatorType.REMOVE)) {
        	if ( operator == DataTableStringOperatorType.REMOVE ) {
        		input3Val = ""; // Replace found string with empty string
        	}
        	// This is tricky because don't want to change unless there is a match.
        	// Problems can occur if one call messes with data that another call previously changed.
        	// Therefore need to handle with care depending on whether output column is the same as input column.
        	if ( input1ColumnNum == outputColumnNum ) {
        		// Default is output will be the same as input unless changed below
        		outputVal = input1Val;
        	}
        	else {
        		// Get the value of the output column before manipulation
        		try {
        			Object o = __table.getFieldValue(irec, outputColumnNum);
        			if ( o == null ) {
        				outputVal = null;
        			}
        			else {
        				outputVal = "" + o;
        			}
        		}
        		catch ( Exception e ) {
            		outputVal = null;
        		}
        		if ( outputVal == null ) {
        			// Probably first pass manipulating so set to input
        			outputVal = input1Val;
        		}
        	}
            if ( (input2Val != null) && (input3Val != null) ) {
            	// Handle strings at beginning and end specifically
            	if ( replaceStart ) {
            		if ( input1Val.startsWith(input2Val) ) {
            			if ( input1Val.length() > input2Val.length() ) {
            				// Have longer string so have to replace part
            				outputVal = input3Val + input1Val.substring(input2Val.length());
            			}
	            		else {
	            			// Replace whole string
	            			outputVal = input3Val;
	            		}
            		}
            		// Else defaults to default output as determined above
            	}
            	else if ( replaceEnd ) {
            		input2Val = input2Val.substring(0,input2Val.length());
            		if ( input1Val.endsWith(input2Val) ) {
	            		if ( input1Val.length() > input2Val.length() ) {
	            			outputVal = input1Val.substring(0,input1Val.length() - input2Val.length()) + input3Val;
	            		}
	            		else {
	            			outputVal = input3Val;
	            		}
            		}
            		// Else defaults to default output as determined above
            	}
            	else {
            		// Simple replace - may not do anything if not matched
            		String outputValTmp = input1Val.replace(input2Val, input3Val);
            		if ( !outputValTmp.equals(outputVal) ) {
            			// Output was changed so update the value, otherwise leave previous output determined above
            			outputVal = outputValTmp;
            		}
            	}
            }
        }
        else if ( operator == DataTableStringOperatorType.SPLIT ) {
        	// That parameter character positions are 1+ but internal positions are 0+
        	// Split out a token where input2Value is the delimiter and input3Value is the integer position (1++)
        	// TODO SAM 2016-06-16 Figure out how to error-handle better
        	int input3ValInt = -1;
        	try {
        		input3ValInt = Integer.parseInt(input3Val);
        	}
        	catch ( Exception e ) {
        		input3ValInt = -1;
        	}
        	if ( (input1Val == null) || input1Val.isEmpty() ) {
        		outputVal = "";
        	}
        	else {
	        	if ( input3ValInt >= 0 ) {
		        	// First break the string list
		        	List<String> tokens = StringUtil.breakStringList(input1Val,input2Val,0);
		        	int iCol = input3ValInt - 1;
		        	if ( iCol < tokens.size() ) {
		        		outputVal = tokens.get(iCol);
		        	}
		        	else {
		        		outputVal = "";
		        	}
	        	}
        	}
        }
        else if ( operator == DataTableStringOperatorType.SUBSTRING ) {
        	// Note that parameter character positions are 1+ but internal positions are 0+
        	// Extract a substring from the string based on character positions
        	int input2ValInt = -1;
        	int input3ValInt = -1;
        	try {
        		input2ValInt = Integer.parseInt(input2Val);
        	}
        	catch ( Exception e ) {
        		input2ValInt = -1;
        	}
        	try {
        		input3ValInt = Integer.parseInt(input3Val);
        	}
        	catch ( Exception e ) {
        		input3ValInt = -1;
        	}
            if ( (input2ValInt >= 0) && (input3ValInt < 0) ) {
            	// Substring to end of string
            	if ( input2ValInt > input1Val.length() ) {
            		outputVal = "";
            	}
            	else {
            		outputVal = input1Val.substring(input2ValInt - 1);
            	}
            }
            else if ( (input2ValInt >= 0) && (input3ValInt >= 0) ) {
            	int input1ValLength = input1Val.length();
            	outputVal = "";
            	if ( (input2ValInt <= input1ValLength) && (input3ValInt <= input1ValLength) ) {
            		outputVal = input1Val.substring((input2ValInt - 1), input3ValInt);
            	}
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
        // Check the length of the string because may need to reset output column width
        if ( input1ColumnNum == outputColumnNum ) {
	        if ( (outputVal != null) && outputVal instanceof String ) {
	        	String s = (String)outputVal;
	        	if ( s.length() > maxChars ) {
	        		maxChars = s.length();
	        	}
	        }
        }
        // Set the value...
        try {
            __table.setFieldValue(irec, outputColumnNum, outputVal );
        }
        catch ( Exception e ) {
            problems.add ( "Error setting value (" + e + ")." );
        }
        // Set the column width
        if ( input1ColumnNum == outputColumnNum ) {
        	int width = __table.getFieldWidth(outputColumnNum);
        	if ( maxChars > width ) {
        		try {
        			__table.setFieldWidth(outputColumnNum, maxChars);
        		}
        		catch ( Exception e ) {
        		}
        	}
        }
    }
}

}