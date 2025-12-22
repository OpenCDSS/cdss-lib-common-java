// DataTableStringManipulator - perform simple column-based string manipulation on a table

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

import java.util.ArrayList;
import java.util.List;

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
 * 
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
public DataTableStringManipulator ( DataTable table, StringDictionary columnIncludeFilters, StringDictionary columnExcludeFilters ) {
    __table = table;
    __columnIncludeFilters = columnIncludeFilters;
    __columnExcludeFilters = columnExcludeFilters;
}

/**
Get the list of operators that can be used.
@return the list of DataTableStringOperatorType that can be used
*/
public static List<DataTableStringOperatorType> getOperatorChoices() {
    List<DataTableStringOperatorType> choices = new ArrayList<>();
    choices.add ( DataTableStringOperatorType.APPEND );
    choices.add ( DataTableStringOperatorType.COPY );
    // TODO smalers 2022-11-30 would be nice to extract using a wildcard rather than substring with positions.
    // choices.add ( DataTableStringOperatorType.EXTRACT );
    choices.add ( DataTableStringOperatorType.PREPEND );
    choices.add ( DataTableStringOperatorType.REMOVE );
    choices.add ( DataTableStringOperatorType.REPLACE );
    choices.add ( DataTableStringOperatorType.SPLIT );
    choices.add ( DataTableStringOperatorType.SUBSTRING );
    // TODO SAM 2015-04-29 Need to enable boolean
    choices.add ( DataTableStringOperatorType.TO_BOOLEAN );
    choices.add ( DataTableStringOperatorType.TO_DATE );
    choices.add ( DataTableStringOperatorType.TO_DATE_TIME );
    choices.add ( DataTableStringOperatorType.TO_DOUBLE );
    choices.add ( DataTableStringOperatorType.TO_INTEGER );
    choices.add ( DataTableStringOperatorType.TO_LONG );
    choices.add ( DataTableStringOperatorType.TO_LOWERCASE );
    choices.add ( DataTableStringOperatorType.TO_MIXEDCASE );
    choices.add ( DataTableStringOperatorType.TO_UPPERCASE );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings() {
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
@param useEmptyStringForNullInput whether to use an empty string if the table input is null
@param outputColumn the name of the output column
@param problems a list of strings indicating problems during processing
*/
public void manipulate (
	String inputColumn1,
	DataTableStringOperatorType operator,
    String inputColumn2, String inputValue2, String inputValue3,
    boolean useEmptyStringForNullInput, String outputColumn,
    List<String> problems ) {
    String routine = getClass().getSimpleName() + ".manipulate" ;
	// Construct the filter.
	DataTableFilter filter = null;
	try {
		filter = new DataTableFilter ( __table, __columnIncludeFilters, __columnExcludeFilters );
	}
	catch ( Exception e ) {
		// If any problems are detected then processing will be stopped below.
		problems.add(e.getMessage());
	}
    // Look up the columns for input and output.
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
        // Automatically add to the table, initialize with null.
        if ( operator == DataTableStringOperatorType.TO_BOOLEAN ) {
            __table.addField(new TableField(TableField.DATA_TYPE_BOOLEAN,outputColumn,-1,-1), null );
        }
        else if ( operator == DataTableStringOperatorType.TO_INTEGER ) {
            __table.addField(new TableField(TableField.DATA_TYPE_INT,outputColumn,-1,-1), null );
        }
        else if ( operator == DataTableStringOperatorType.TO_LONG ) {
            __table.addField(new TableField(TableField.DATA_TYPE_LONG,outputColumn,-1,-1), null );
        }
        else if ( (operator == DataTableStringOperatorType.TO_DATE) ||
            (operator == DataTableStringOperatorType.TO_DATE_TIME) ) {
            // Precision is handled by precision on individual date/time objects.
            __table.addField(new TableField(TableField.DATA_TYPE_DATETIME,outputColumn,-1,-1), null );
        }
        else if ( operator == DataTableStringOperatorType.TO_DOUBLE ) {
            __table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,outputColumn,-1,6), null );
        }
        else {
        	// All other types use a string column type.
            __table.addField(new TableField(TableField.DATA_TYPE_STRING,outputColumn,-1,-1), null );
        }
        try {
            outputColumnNum = __table.getFieldIndex(outputColumn);
            Message.printStatus ( 2, routine, "Output column \"" + outputColumn + "\" added in position [" + outputColumnNum + "]." );
        }
        catch ( Exception e2 ) {
            // Should not happen.
            problems.add ( "Output column \"" + outputColumn + "\" not found in table \"" + __table.getTableID() + "\".  Error addung column." );
        }
    }

    if ( problems.size() > 0 ) {
        // Return if any problems were detected.
        return;
    }

    // Check for special cases on input, for example ^ and $ are used with replace.
    // In these cases, remove the special characters.
    boolean replaceStart = false;
    boolean replaceEnd = false;
    boolean removeCharSet = false;
	if ( (operator == DataTableStringOperatorType.REPLACE) || (operator == DataTableStringOperatorType.REMOVE) ) {
    	if ( inputValue2 != null ) {
    		if ( inputValue2.startsWith("^") ) {
	    		replaceStart = true;
	    		inputValue2 = inputValue2.substring(1,inputValue2.length());
    		}
    		else if ( inputValue2.endsWith("$") && !inputValue2.endsWith("\\$") ) {
    			// Input value indicates line ending.
	    		replaceEnd = true;
	    		// Remove the dollar sign.
	    		inputValue2 = inputValue2.substring(0,inputValue2.length()-1);
    		}
    		else if ( inputValue2.startsWith("[") && inputValue2.endsWith("]") ) {
    			// Replacing a character set in [123...].
    			removeCharSet = true;
	    		// Remove the braces.
	    		inputValue2 = inputValue2.substring(1,inputValue2.length()-1);
    		}
    		// Replace "\s" with single space.
    		inputValue2 = inputValue2.replace("\\s"," ");
    		// Replace "\$" with dollar sign since not a line ending.
    		inputValue2 = inputValue2.replace("\\$","$");
    	}
    	if ( operator == DataTableStringOperatorType.REMOVE ) {
    		// Same as substring but second string is blank.
    		inputValue3 = "";
    	}
    	else {
	    	if ( inputValue3 != null ) {
	    		// Replace "\ " with single space, anywhere in the output.
	    		inputValue3 = inputValue3.replace("\\s"," ");
	    	}
    	}
	}

    // Loop through the records.
    int nrec = __table.getNumberOfRecords();
    Object val = null;
    String input1Val = null;
    String input2Val = null;
    String input3Val = null;
    Object outputVal = null;
    int maxChars = -1; // Maximum string length of output.
    for ( int irec = 0; irec < nrec; irec++ ) {
    	// Check whether row should be included/excluded - "true" below indicates to throw exceptions.
    	try {
	    	if ( !filter.includeRow(irec,true) ) {
	    		continue;
	    	}
    	}
    	catch ( Exception e ) {
    		problems.add(e.getMessage());
    	}
        // Initialize the values.
        input1Val = null;
        input2Val = null;
        input3Val = null;
        outputVal = null;
        // Get the input values.
        try {
            val = __table.getFieldValue(irec, input1ColumnNum);
            if ( (val == null) && useEmptyStringForNullInput ) {
            	val = "";
            }
            if ( val == null ) {
            	input1Val = null;
            }
            else {
            	input1Val = "" + val; // Do this way so that even non-strings can be manipulated.
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
            	// Constant value was not given so get from column.
                val = __table.getFieldValue(irec, input2ColumnNum);
                if ( (val == null) && useEmptyStringForNullInput ) {
                	val = "";
                }
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
        	// Only constant value is allowed (not from column).
            input3Val = inputValue3;
        }
        // Check for missing values and compute the output.
        if ( input1Val == null ) {
        	// Output is null regardless of the operator.
            outputVal = null;
        }
        else if ( operator == DataTableStringOperatorType.APPEND ) {
            if ( input2Val == null ) {
                outputVal = null;
            }
            else {
                outputVal = input1Val + input2Val;
                Message.printStatus(2, routine, "Output value is \"" + outputVal + "\".");
            }
        }
        else if ( operator == DataTableStringOperatorType.COPY ) {
        	// Output is the same as input.
            outputVal = input1Val;
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
        		input3Val = ""; // Replace found string with empty string.
        	}
        	// This is tricky because don't want to change unless there is a match.
        	// Problems can occur if one call messes with data that another call previously changed.
        	// Therefore need to handle with care depending on whether output column is the same as input column.
        	if ( input1ColumnNum == outputColumnNum ) {
        		// Default is output will be the same as input unless changed below.
        		outputVal = input1Val;
        	}
        	else {
        		// Get the value of the output column before manipulation.
        		try {
        			Object o = __table.getFieldValue(irec, outputColumnNum);
        			if ( (o == null) && useEmptyStringForNullInput ) {
        				o = "";
        			}
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
        			// Probably first pass manipulating so set to input.
        			outputVal = input1Val;
        		}
        	}
            if ( (input2Val != null) && (input3Val != null) ) {
            	// Handle strings at beginning and end specifically.
            	if ( replaceStart ) {
            		if ( input1Val.startsWith(input2Val) ) {
            			if ( input1Val.length() > input2Val.length() ) {
            				// Have longer string so have to replace part.
            				outputVal = input3Val + input1Val.substring(input2Val.length());
            			}
	            		else {
	            			// Replace whole string.
	            			outputVal = input3Val;
	            		}
            		}
            		// Else defaults to default output as determined above.
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
            		// Else defaults to default output as determined above.
            	}
            	else if ( removeCharSet ) {
            		// 'input2Val' contains a list of characters to be removed:
            		// - the original parameter value is something like [123]
            		// - the brackets were stripped off above
            		// - rely on built-in String.replace() method
            		String c;
            		for ( int i = 0; i < input2Val.length(); i++ ) {
            			c = String.valueOf(input2Val.charAt(i));
            			// Check first so that less memory is allocated for 'outputVal'.
            			if ( input1Val.contains(c) ) {
            				input1Val = input1Val.replace(c,"");
            			}
            		}
            		// Set the output to the input1Val after it has been processed.
            		outputVal = input1Val;
            	}
            	else {
            		// Simple replace - may not do anything if not matched.
            		String outputValTmp = input1Val.replace(input2Val, input3Val);
            		if ( !outputValTmp.equals(outputVal) ) {
            			// Output was changed so update the value, otherwise leave previous output determined above.
            			outputVal = outputValTmp;
            		}
            	}
            }
        }
        else if ( operator == DataTableStringOperatorType.SPLIT ) {
        	// That parameter character positions are 1+ but internal positions are 0+.
        	// Split out a token where input2Value is the delimiter and input3Value is the integer position (1++).
        	// TODO SAM 2016-06-16 Figure out how to error-handle better.
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
		        	// First break the string list.
	        		inputValue2 = inputValue2.replace("\\s"," ");
	        		inputValue2 = inputValue2.replace("\\n","\n");
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
        	// Note that parameter character positions are 1+ but internal positions are 0+.
        	// Extract a substring from the string based on character positions.
        	// Use MAX_VALUE for missing.
        	int input2ValInt = Integer.MAX_VALUE;
        	int input3ValInt = Integer.MAX_VALUE;
        	try {
        		input2ValInt = Integer.parseInt(input2Val);
        		if ( input2ValInt < 0 ) {
        			// Index is from the end of the string:
        			// - reset to an equivalent positive index from the front of the string
        			input2ValInt = input1Val.length() + input2ValInt + 1;
        		}
        	}
        	catch ( Exception e ) {
        		input2ValInt = Integer.MAX_VALUE;
        	}
        	try {
        		input3ValInt = Integer.parseInt(input3Val);
        		if ( input3ValInt < 0 ) {
        			// Index is from the end of the string:
        			// - reset to an equivalent positive index from the front of the string
        			input3ValInt = input1Val.length() + input3ValInt + 1;
        		}
        	}
        	catch ( Exception e ) {
        		input3ValInt = Integer.MAX_VALUE;
        	}
            if ( (input2ValInt >= 0) && (input2ValInt != Integer.MAX_VALUE) && (input3ValInt == Integer.MAX_VALUE) ) {
            	// Substring to end of string.
            	if ( input2ValInt > input1Val.length() ) {
            		outputVal = "";
            	}
            	else {
            		outputVal = input1Val.substring(input2ValInt - 1);
            	}
            }
            else if ( (input2ValInt >= 0) && (input2ValInt != Integer.MAX_VALUE) &&
            	(input3ValInt >= 0) && (input3ValInt != Integer.MAX_VALUE) ) {
            	// Substring between index values.
            	int input1ValLength = input1Val.length();
            	outputVal = "";
            	if ( input2ValInt > input1Val.length() ) {
            		outputVal = "";
            	}
            	else if ( input2ValInt <= input1ValLength ) {
            		if  (input3ValInt <= input1ValLength ) {
            			// Get the requested substring.
            			outputVal = input1Val.substring((input2ValInt - 1), input3ValInt);
            		}
            		else {
            			// Get the partial string that is available.
            			outputVal = input1Val.substring(input2ValInt - 1);
            		}
            	}
            }
        }
        else if ( operator == DataTableStringOperatorType.TO_BOOLEAN ) {
        	// First try to convert the string to a Boolean:
        	// - don't use built-in Boolean.parseBoolean() because it does not handle 0 or 1
           	if ( input1Val == null ) {
           		// Why is this dead code?
           		outputVal = null;
           	}
           	else if ( input1Val.equalsIgnoreCase("true") || input1Val.equals("1") ) {
           		outputVal = Boolean.TRUE;
           	}
           	else if ( input1Val.equalsIgnoreCase("false") || input1Val.equals("0") ) {
           		outputVal = Boolean.FALSE;
           	}
           	else {
           		// Not a recognized boolean.
           		outputVal = null;
            }
        }
        else if ( (operator == DataTableStringOperatorType.TO_DATE) ||
            (operator == DataTableStringOperatorType.TO_DATE_TIME)) {
            try {
                outputVal = DateTime.parse(input1Val);
                // TODO SAM 2013-05-13 Evaluate whether this is needed since string should parse.
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
        	// First try to convert the string to an Integer.
            try {
                outputVal = Integer.parseInt(input1Val);
            }
            catch ( NumberFormatException e ) {
            	// If the above fails, try converting to a Double and then rounding to an Integer.
            	try {
            		Double outputDouble = Double.parseDouble(input1Val);
            		// intValue may truncate but want to round in the normal way.
            		outputVal = Integer.valueOf((int)Math.round(outputDouble));
            	}
            	catch ( NumberFormatException e2 ) {
            		outputVal = null;
            	}
            }
        }
        else if ( operator == DataTableStringOperatorType.TO_LONG ) {
        	// First try to convert the string to a Long.
            try {
                outputVal = Long.parseLong(input1Val);
            }
            catch ( NumberFormatException e ) {
            	// If the above fails, try converting to a Double and then rounding to a Long.
            	try {
            		Double outputDouble = Double.parseDouble(input1Val);
            		// intValue may truncate but want to round in the normal way.
            		outputVal = Long.valueOf((int)Math.round(outputDouble));
            	}
            	catch ( NumberFormatException e2 ) {
            		outputVal = null;
            	}
            }
        }
        else if ( operator == DataTableStringOperatorType.TO_LOWERCASE ) {
            outputVal = input1Val.toLowerCase();
        }
        else if ( operator == DataTableStringOperatorType.TO_MIXEDCASE ) {
        	// Convert the first letter of each word to mixed case and others to lower case.
        	StringBuilder b = new StringBuilder(input1Val.toLowerCase());
        	for ( int i = 0; i < input1Val.length(); i++ ) {
        		if ( (i == 0) || ((i > 0) && Character.isWhitespace(b.charAt(i - 1))) ) {
        			// First character in a word.
        			b.setCharAt(i, Character.toUpperCase(b.charAt(i)) );
        		}
        	}
            outputVal = b.toString();
        }
        else if ( operator == DataTableStringOperatorType.TO_UPPERCASE ) {
            outputVal = input1Val.toUpperCase();
        }
        // Check the length of the string because may need to reset output column width.
        if ( input1ColumnNum == outputColumnNum ) {
	        if ( (outputVal != null) && outputVal instanceof String ) {
	        	String s = (String)outputVal;
	        	if ( s.length() > maxChars ) {
	        		maxChars = s.length();
	        	}
	        }
        }
        // Set the value.
        try {
            __table.setFieldValue(irec, outputColumnNum, outputVal );
        }
        catch ( Exception e ) {
            String message = "Error setting value in row [" + irec + "], column [" + outputColumnNum + "] (" + e + ").";
            problems.add ( message );
            if ( problems.size() <= 10 ) {
            	Message.printWarning(3, routine, message);
            	Message.printWarning(3, routine, e);
            }
            if ( problems.size() == 10 ) {
            	Message.printWarning(3, routine, "Only printing 10 exceptions.");
            }
        }
        // Set the column width.
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
