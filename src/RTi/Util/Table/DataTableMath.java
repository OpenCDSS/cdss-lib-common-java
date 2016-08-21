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
    List<DataTableMathOperatorType> choices = new Vector<DataTableMathOperatorType>();
    choices.add ( DataTableMathOperatorType.ADD );
    choices.add ( DataTableMathOperatorType.SUBTRACT );
    choices.add ( DataTableMathOperatorType.MULTIPLY );
    choices.add ( DataTableMathOperatorType.DIVIDE );
    //choices.add ( DataTableMathOperatorType.TO_DOUBLE ); // TODO SAM 2013-08-26 Need to enable below, with Integer as input for all
    choices.add ( DataTableMathOperatorType.TO_INTEGER );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings()
{
    List<DataTableMathOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new Vector<String>();
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
@param nonValue value to assign when floating point numbers cannot be computed (null or Double.NaN)
@param problems a list of strings indicating problems during processing
*/
public void math ( String input1, DataTableMathOperatorType operator, String input2, String output,
    Double nonValue, List<String> problems )
{   String routine = getClass().getSimpleName() + ".math" ;
    // Look up the columns for input and output
    int input1Field = -1;
    int input1FieldType = -1;
    int input2FieldType = -1;
    int outputFieldType = -1;
    try {
        input1Field = __table.getFieldIndex(input1);
        input1FieldType = __table.getFieldDataType(input1Field);
    }
    catch ( Exception e ) {
        problems.add ( "Input column (1) \"" + input1 + "\" not found in table \"" + __table.getTableID() + "\"" );
    }
    int input2Field = -1;
    Double input2ConstantDouble = null;
    Integer input2ConstantInteger = null;
    if ( operator != DataTableMathOperatorType.TO_INTEGER ) {
        // Need to get the second input to do the math
        if ( StringUtil.isDouble(input2) ) {
            // Second input supplied as a double
            input2ConstantDouble = Double.parseDouble(input2);
            input2FieldType = TableField.DATA_TYPE_DOUBLE;
        }
        if ( StringUtil.isInteger(input2) ) {
            // Second input supplied as an integer - use instead of double (handle below if 1st and 2nd arguments are different)
            input2ConstantInteger = Integer.parseInt(input2);
            input2FieldType = TableField.DATA_TYPE_INT;
        }
        if ( (input2ConstantDouble == null) && (input2ConstantInteger == null) ) {
            // Second input supplied as a column name rather than constant number
            try {
                input2Field = __table.getFieldIndex(input2);
                input2FieldType = __table.getFieldDataType(input2Field);
            }
            catch ( Exception e ) {
                problems.add ( "Input column (2) \"" + input2 + "\" not found in table \"" + __table.getTableID() + "\"" );
            }
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
        if ( (operator == DataTableMathOperatorType.TO_INTEGER) ||
        	((input1FieldType == TableField.DATA_TYPE_INT) &&
        	(input2FieldType == TableField.DATA_TYPE_INT)) ) {
            outputField = __table.addField(new TableField(TableField.DATA_TYPE_INT,output,-1,-1), null );
        }
        else {
        	// One or both output fields are floating point so default output to double
            outputField = __table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,output,10,4), null );
        }
    }
    if ( (input1FieldType != TableField.DATA_TYPE_INT) && (input1FieldType != TableField.DATA_TYPE_DOUBLE) ) {
    	problems.add("Input1 column type is not integer or double - cannot do math.");
    }
    if ( (input2Field >= 0) && (input2FieldType != TableField.DATA_TYPE_INT) && (input2FieldType != TableField.DATA_TYPE_DOUBLE) ) {
    	problems.add("Input2 column type is not integer or double - cannot do math.");
    }
    
    if ( problems.size() > 0 ) {
        // Return if any problems were detected
        return;
    }

    // Loop through the records
    int nrec = __table.getNumberOfRecords();
    Object val;
    Double input1ValDouble = null;
    Double input2ValDouble = null;
    Integer input1ValInteger = null;
    Integer input2ValInteger = null;
    Double outputValDouble = Double.NaN;
    Integer outputValInteger = null;
    for ( int irec = 0; irec < nrec; irec++ ) {
        // Initialize the values
        input1ValDouble = Double.NaN;
        input2ValDouble = Double.NaN;
        outputValDouble = nonValue;
        input1ValInteger = null;
        input2ValInteger = null;
        outputValInteger = null;
        // Get the input values
        try {
            val = __table.getFieldValue(irec, input1Field);
        	if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        		input1ValInteger = (Integer)val;
        	}
        	else {
        		input1ValDouble = (Double)val;
        	}
        }
        catch ( Exception e ) {
            problems.add ( "Error getting value for input field 1 (" + e + ")." );
            continue;
        }
        if ( input2Field >= 0 ) {
            try {
                // Second value is determined from table
                val = __table.getFieldValue(irec, input2Field);
                if ( input2FieldType == TableField.DATA_TYPE_INT ) {
                	input2ValInteger = (Integer)val;
                }
                else {
                	input2ValDouble = (Double)val;
                }
            }
            catch ( Exception e ) {
                problems.add ( "Error getting value for input field 2 (" + e + ")." );
                continue;
            }
        }
        else {
        	if ( input2ConstantDouble != null ) {
	            // Second value was a constant
	            input2ValDouble = input2ConstantDouble;
	        }
	        if ( input2ConstantInteger != null ) {
	            // Second value was a constant
	            input2ValInteger = input2ConstantInteger;
	        }
        }
        // TODO SAM 2015-08-14 If at least one of the inputs is a double then the output is a double
        if ( input1FieldType != input2FieldType ) {
        	// Make sure the input is cast properly
        	if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        		if ( input1ValInteger == null ) {
        			input1ValDouble = null;
        		}
        		else {
        			input1ValDouble = 0.0 + input1ValInteger;
        		}
        	}
        	if ( input2FieldType == TableField.DATA_TYPE_INT ) {
        		if ( input2ValInteger == null ) {
        			input2ValDouble = null;
        		}
        		else {
        			input2ValDouble = 0.0 + input2ValInteger;
        		}
        	}
        } 
        // Check for missing values and compute the output
        if ( operator == DataTableMathOperatorType.TO_INTEGER ) {
			// Only need the first input
			// Set integer and double in case output table column is not configured properly
        	if ( input1FieldType == TableField.DATA_TYPE_DOUBLE ) {
	            if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
	                outputValInteger = null;
	                outputValDouble = nonValue;
	            }
	            else {
	                outputValInteger = input1ValDouble.intValue();
	                outputValDouble = (double)input1ValDouble.intValue();
	            }
        	}
        	else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
                if ( input1ValInteger == null ) {
                    outputValInteger = null;
                    outputValDouble = nonValue;
                }
                else {
                	outputValInteger = input1ValInteger;
                	outputValDouble = (double)input1ValInteger;
                }
            }
		}
        else {
        	// The following operators need two input values to compute
	    	if ( (input1FieldType == TableField.DATA_TYPE_DOUBLE) || (input2FieldType == TableField.DATA_TYPE_DOUBLE) ||
	    		(input1FieldType != input2FieldType) ) {
	    		// Double input and double output (or mixed in which case double values were set above)
	    		outputFieldType = TableField.DATA_TYPE_DOUBLE;
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() || (input2ValDouble == null) || input2ValDouble.isNaN() ) {
                    outputValDouble = nonValue;
                }
                else if ( operator == DataTableMathOperatorType.ADD ) {
                	outputValDouble = input1ValDouble + input2ValDouble;
                }
                else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
                	outputValDouble = input1ValDouble - input2ValDouble;
                }
                else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
                	outputValDouble = input1ValDouble * input2ValDouble;
                }
                else if ( operator == DataTableMathOperatorType.DIVIDE ) {
                    if ( input2ValDouble == 0.0 ) {
                        outputValDouble = nonValue;
                    }
                    else {
                        outputValDouble = input1ValDouble / input2ValDouble;
                    }
                }
            }
	    	else if ( (input1FieldType == TableField.DATA_TYPE_INT) && (input2FieldType == TableField.DATA_TYPE_INT) ) {
	    		// Integer input and integer output
	    		outputFieldType = TableField.DATA_TYPE_INT;
                if ( (input1ValInteger == null) || (input2ValInteger == null) ) {
                    outputValInteger = null;
                }
                else if ( operator == DataTableMathOperatorType.ADD ) {
                	outputValInteger = input1ValInteger + input2ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
                	outputValInteger = input1ValInteger - input2ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
                	outputValInteger = input1ValInteger * input2ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.DIVIDE ) {
                    if ( input2ValInteger == 0.0 ) {
                        outputValInteger = null;
                    }
                    else {
                        outputValInteger = input1ValInteger / input2ValInteger;
                    }
                }
            }
        }
        // Set the value...
        try {
            if ( outputFieldType == TableField.DATA_TYPE_INT ) {
                __table.setFieldValue(irec, outputField, outputValInteger );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_DOUBLE ) {
                __table.setFieldValue(irec, outputField, outputValDouble );
            }
            else {
            	// TODO SAM 2016-08-02 may need to support other output columns like strings
            }
        }
        catch ( Exception e ) {
            if ( outputFieldType == TableField.DATA_TYPE_INT ) {
                problems.add ( "Error setting value in row [" + irec + "] to " + outputValInteger + " (" + e + ")." );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_DOUBLE ) {
                problems.add ( "Error setting value in row [" + irec + "] to " + outputValDouble + " (" + e + ")." );
            }
            else {
            	problems.add ( "Error setting value in row [" + irec + "] (" + e + ")." );
            }
        }
    }   
}

}