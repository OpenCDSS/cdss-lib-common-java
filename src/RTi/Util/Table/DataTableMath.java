// DataTableMath - perform simple column-based math operations on a table

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

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Math.MathUtil;
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
private DataTable table = null;

/**
Construct an instance using the table to operate on.
*/
public DataTableMath ( DataTable table ) {
    this.table = table;
}

/**
Get the list of operators that can be used.
*/
public static List<DataTableMathOperatorType> getOperatorChoices() {
    List<DataTableMathOperatorType> choices = new ArrayList<>();
    // Put symbol operators at the front, grouped logically.
    choices.add ( DataTableMathOperatorType.ASSIGN );
    choices.add ( DataTableMathOperatorType.ADD );
    choices.add ( DataTableMathOperatorType.SUBTRACT );
    choices.add ( DataTableMathOperatorType.MULTIPLY );
    choices.add ( DataTableMathOperatorType.DIVIDE );
    //choices.add ( DataTableMathOperatorType.TO_DOUBLE ); // TODO SAM 2013-08-26 Need to enable below, with Integer as input for all.
    // Put word operators at the end.
    choices.add ( DataTableMathOperatorType.CUMULATE );
    choices.add ( DataTableMathOperatorType.DELTA );
    choices.add ( DataTableMathOperatorType.MAX );
    choices.add ( DataTableMathOperatorType.MIN );
    choices.add ( DataTableMathOperatorType.TO_INTEGER );
    return choices;
}

/**
Get the list of operators that can be performed.
@return the operator display names as strings.
*/
public static List<String> getOperatorChoicesAsStrings() {
    List<DataTableMathOperatorType> choices = getOperatorChoices();
    List<String> stringChoices = new ArrayList<>();
    for ( DataTableMathOperatorType choice : choices ) {
        stringChoices.add ( "" + choice );
    }
    return stringChoices;
}

/**
 * Determine whether the input is all integers or longs, * used to help with casting.
 * @return true if all the input is integers (short, integer, long), otherwise return false
 */
private boolean inputIsIntegers (
	int input1Field, int input1FieldType, Double input1ConstantDouble, Integer input1ConstantInteger, Long input1ConstantLong,
	int input2Field, int input2FieldType, Double input2ConstantDouble, Integer input2ConstantInteger, Long input2ConstantLong ) {
	String routine = getClass().getSimpleName() + ".inputIsIntegers";
	boolean inputIsIntegers = true;
	if ( input1Field >= 0 ) {
		// Have an input1 field (column) to check.
		if ( (input1FieldType != TableField.DATA_TYPE_INT) && (input1FieldType != TableField.DATA_TYPE_SHORT) && (input1FieldType != TableField.DATA_TYPE_LONG) ) {
			// Input field is not an integer type.
			Message.printStatus(2,routine,"Input field 1 is not an integer type.");
			inputIsIntegers = false;
		}
	}
	if ( inputIsIntegers ) {
		if ( input2Field >= 0 ) {
			// Have an input2 field (column) to check.
			if ( (input2FieldType != TableField.DATA_TYPE_INT) && (input2FieldType != TableField.DATA_TYPE_SHORT) && (input2FieldType != TableField.DATA_TYPE_LONG) ) {
				// Input field is not an integer type.
				Message.printStatus(2,routine,"Input field 2 is not an integer type.");
				inputIsIntegers = false;
			}
		}
		else {
			// Check the second constant.
			if ( input2ConstantLong == null ) {
				// The second constant is an integer or long (long is set if an integer and holds larger values).
				Message.printStatus(2,routine,"Constant field 2 is not an integer/long type.");
				inputIsIntegers = false;
			}
		}
	}
	Message.printStatus(2,routine,"inputIsIntegers output=" + inputIsIntegers);
	return inputIsIntegers;
}

/**
Perform a math calculation.
If the output is for Double or Float, double precision numbers are used for calculations
and the appropriate output type is set at the end.
Integer and Long output are handled specifically.
@param input1 the name of the first column to use as input
@param operator the operator to execute for processing data
@param input2 the name of the second column to use as input, or a constant
@param output the name of the output column
@param outputType the output column type (TableField.DATA_*), or -1 to automatically determine
@param nonValue value to assign when floating point numbers cannot be computed (null or Double.NaN)
@param problems a list of strings indicating problems during processing
*/
public void math (
	String input1, DataTableMathOperatorType operator, String input2,
	String output, int outputType,
    Double nonValue,
	TableRowConditionEvaluator evaluator,
    List<String> problems ) {
    String routine = getClass().getSimpleName() + ".math" ;
    // Look up the columns for input and output.
    int input1Field = -1;
    int input1FieldType = -1;
    int input2FieldType = -1;
    // Only use constant for Input1 for assignment.
    Double input1ConstantDouble = null;
    Integer input1ConstantInteger = null;
    Long input1ConstantLong = null;

    // First try to get the input field assuming that the first input is a column name.
    try {
        input1Field = this.table.getFieldIndex(input1);
        input1FieldType = this.table.getFieldDataType(input1Field);
    }
    catch ( Exception e ) {
    	// Was not able to find a table column name for Input1:
    	// - check whether a constant
    	if ( operator == DataTableMathOperatorType.ASSIGN ) {
    		// Check integer first since more restrictive (no decimal point).
    		// Then check double (allows decimal point).
    		if ( StringUtil.isInteger(input1) ) {
    			// First input supplied as an integer:
    			// - set as Integer and Long to allow output to output column type
    			input1ConstantInteger = Integer.valueOf(input1);
    			input1ConstantLong = Long.valueOf(input1);
    			input1FieldType = TableField.DATA_TYPE_INT;
    			if ( Message.isDebugOn ) {
    				Message.printStatus(2, routine, "First input provided as constant integer: " + input1);
    			}
    		}
    		else if ( StringUtil.isDouble(input1) ) {
    			// First input supplied as a double (math uses Double rather than Float and conversion is done at the end).
            	input1ConstantDouble = Double.parseDouble(input1);
            	input1FieldType = TableField.DATA_TYPE_DOUBLE;
            	if ( Message.isDebugOn ) {
            		Message.printStatus(2, routine, "First input provided as constant double: " + input1);
            	}
    		}
    		else {
    			problems.add ( "Input1 \"" + input1 + " is not a table column name, integer constant, or double constant." );
    		}
    	}
    	else {
    		problems.add ( "Input column (1) \"" + input1 + "\" not found in table \"" + this.table.getTableID() + "\"" );
    	}
    }
    
    // Check the second input, which might be a table column, or a constant.
    int input2Field = -1;
    Double input2ConstantDouble = null;
    Integer input2ConstantInteger = null;
    Long input2ConstantLong = null;
    boolean requireInput2 = requiresInput2(operator);
    if ( requireInput2 ) {
        // Need to get the second input to do the math.
        if ( StringUtil.isDouble(input2) ) {
            // Second input supplied as a double, which is more restrictive because it has a decimal.
            input2ConstantDouble = Double.parseDouble(input2);
            input2FieldType = TableField.DATA_TYPE_DOUBLE;
            if ( Message.isDebugOn ) {
            	Message.printStatus(2, routine, "Second input provided as constant double: " + input2);
            }
        }
        if ( StringUtil.isInteger(input2) ) {
            // Second input supplied as an integer:
        	// - use Integer in addition to double (handle below if 1st and 2nd arguments are different type)
        	// - also set Long in case output is Long and can't fit in an Integer
            input2ConstantInteger = Integer.valueOf(input2);
            input2ConstantLong = Long.valueOf(input2);
            input2ConstantDouble = Double.valueOf(input2);
            input2FieldType = TableField.DATA_TYPE_INT;
            if ( Message.isDebugOn ) {
            	Message.printStatus(2, routine, "Second input provided as constant integer: " + input2);
            }
        }
        else if ( StringUtil.isLong(input2) ) {
            // Second input supplied as a long:
        	// - use Long in addition to double (handle below if 1st and 2nd arguments are different type)
        	// - cannot set Integer because the value is too large
            input2ConstantLong = Long.valueOf(input2);
            input2ConstantDouble = Double.valueOf(input2);
            input2FieldType = TableField.DATA_TYPE_LONG;
            if ( Message.isDebugOn ) {
            	Message.printStatus(2, routine, "Second input provided as constant long integer (too big for normal integer): " + input2);
            }
        }
        if ( (input2ConstantDouble == null) && (input2ConstantInteger == null) && (input2ConstantLong == null) ) {
            // Second input supplied as a column name rather than constant number.
            try {
                input2Field = this.table.getFieldIndex(input2);
                input2FieldType = this.table.getFieldDataType(input2Field);
            }
            catch ( Exception e ) {
                problems.add ( "Input 2 is not integer, long, or double and column 2 \"" + input2 +
                	"\" is not found in table \"" + this.table.getTableID() + "\"" );
            }
        }
    }
    // The output field type is controlled by existing output column,
    // the operator (for new column), or input column types.
    // Once the output column type is set, it controls casting of input data below.
    int outputField = -1;
    int outputFieldType = -1;
    try {
        outputField = this.table.getFieldIndex(output);
        // If FLOAT, it is handled similar to DOUBLE in calculations and set to Float at the end.
        outputFieldType = this.table.getFieldDataType(outputField);

        // Check that the existing output column data type is compatible with the operator:
        // - most operators allow integer and double
        if ( operator == DataTableMathOperatorType.TO_INTEGER ) {
        	if ( outputFieldType != TableField.DATA_TYPE_INT ) {
        		String message = "Existing table column \"" + outputField
        			+ "\" type \"" + outputFieldType + "\" is not integer as requested by operator " + operator;
        		Message.printWarning(3, routine, message);
        		problems.add(message);
        	}
        }
    }
    catch ( Exception e ) {
    	// Existing column does not exist.
        // Automatically add to the table, initialize with null (not nonValue).
    	if ( outputType >= 0 ) {
    		// Output type is explicitly specified, so use the provided type.
            outputFieldType = outputType;
    		Message.printStatus(2, routine, "Output type is explicitly set to \"" + TableField.getDataTypeAsString(outputType) + "\"");
    	}
    	else if ( operator == DataTableMathOperatorType.TO_INTEGER ) {
        	// Output field type is integer by definition.
            outputFieldType = TableField.DATA_TYPE_INT;
        }
        else if (
        	(operator == DataTableMathOperatorType.ASSIGN) ||
        	(operator == DataTableMathOperatorType.CUMULATE) ||
        	(operator == DataTableMathOperatorType.DELTA) ) {
        	// Output field type is the same as input.
            outputFieldType = input1FieldType;
        }
        else if ( (input1FieldType == TableField.DATA_TYPE_INT) &&
        	(input2FieldType == TableField.DATA_TYPE_INT) ) {
        	// Both input fields are integer.
            outputFieldType = TableField.DATA_TYPE_INT;
        }
        else if ( (input1FieldType == TableField.DATA_TYPE_LONG) &&
        	(input2FieldType == TableField.DATA_TYPE_LONG) ) {
        	// Both input fields are long.
            outputFieldType = TableField.DATA_TYPE_LONG;
        }
        else {
        	// One or both output fields are floating point so default output to double.
        	// This is consistent with most common programming languages and handles mixed case
        	// Also use this for division so that fractions are not truncated.
        	// TODO smalers 2026-02-13 does this need to handle FLOAT?
            outputFieldType = TableField.DATA_TYPE_DOUBLE;
        }
        // Create the table output field of the correct type.
        if ( outputFieldType == TableField.DATA_TYPE_INT ) {
        	Message.printWarning(3, routine, "Output column \"" + output + "\" not found in table \"" +
            	this.table.getTableID() + "\" - automatically adding integer column." );
            outputField = this.table.addField(new TableField(outputFieldType,output,-1,-1), null );
        }
        else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
        	Message.printWarning(3, routine, "Output column \"" + output + "\" not found in table \"" +
            	this.table.getTableID() + "\" - automatically adding long column." );
            outputField = this.table.addField(new TableField(outputFieldType,output,-1,-1), null );
        }
        else if ( outputFieldType == TableField.DATA_TYPE_DOUBLE ) {
        	// This is also used if input is FLOAT.
        	Message.printWarning(3, routine, "Output column \"" + output + "\" not found in table \"" +
            	this.table.getTableID() + "\" - automatically adding double column." );
        	// Use the maximum width and precision of the input columns.
        	int precision = 4;
        	int width = 10;
        	if ( inputIsIntegers(input1Field, input1FieldType, input1ConstantDouble, input1ConstantInteger, input1ConstantLong,
        			input2Field, input2FieldType, input2ConstantDouble, input2ConstantInteger, input2ConstantLong) ) {
        		// Input is all integers or longs and output is Double:
        		// - can't examine input Double to determine precision and width
        		// - use default width and precision
        		precision = 4;
        		width = -1;
        	}
        	else {
        		// Some input is Double:
        		// - set the width and precision based on the largest double
        		if ( input1Field >= 0 ) {
            		precision = this.table.getFieldPrecision(input1Field);
            		width = this.table.getFieldWidth(input1Field);
            		if ( input2Field >= 0 ) {
            			precision = MathUtil.max(precision,this.table.getFieldPrecision(input2Field));
            			width = MathUtil.max(width,this.table.getFieldWidth(input2Field));
            		}
        		}
        		else if ( input2Field >= 0 ) {
        			// Second input is a table column so use the column properties to set the output precision.
            		precision = this.table.getFieldPrecision(input2Field);
            		width = this.table.getFieldWidth(input2Field);
        		}
        		if ( input2ConstantDouble != null ) {
        			// Set the output precision based on the original double (number of digits after the period).
           			width = input2.length();
           			precision = MathUtil.max(precision,StringUtil.numberOfDigits(input2, 1));
        		}
        	}
            outputField = this.table.addField(new TableField(outputFieldType,output,width,precision),null);
        }
    }

    if ( (input1FieldType != TableField.DATA_TYPE_INT) &&
    	(input1FieldType != TableField.DATA_TYPE_LONG) &&
    	(input1FieldType != TableField.DATA_TYPE_DOUBLE) &&
    	(input1FieldType != TableField.DATA_TYPE_FLOAT) ) {
       	// Incompatible column types.
    	problems.add("Input1 column (" + input1 + ") type (" + TableField.getDataTypeAsString(input1FieldType)
    		+ ") is not integer, long, double, or float - cannot do math.");
    }
    if ( (input2Field >= 0) &&
    	(input2FieldType != TableField.DATA_TYPE_INT) &&
    	(input2FieldType != TableField.DATA_TYPE_LONG) &&
    	(input2FieldType != TableField.DATA_TYPE_DOUBLE) &&
    	(input2FieldType != TableField.DATA_TYPE_FLOAT) ) {
       	// Incompatible column types.
    	problems.add("Input2 column (" + input2 + ") type (" + TableField.getDataTypeAsString(input2FieldType)
    		+ ") is not integer, long, double, or float - cannot do math.");
    }

    if ( problems.size() > 0 ) {
        // Return if any problems were detected.
        return;
    }

    // Loop through the table records and do the math operation.
    int nrec = this.table.getNumberOfRecords();
    Object val = null;
    Double input1ValDouble = null;
    Integer input1ValInteger = null;
    Long input1ValLong = null;
    Double input2ValDouble = null;
    Integer input2ValInteger = null;
    Long input2ValLong = null;
    Double outputValDouble = Double.NaN;
    Integer outputValInteger = null;
    Long outputValLong = null;
    Double cumulativeValDouble = Double.valueOf(0.0);
    Integer cumulativeValInteger = Integer.valueOf(0);
    Long cumulativeValLong = Long.valueOf(0);
    for ( int irec = 0; irec < nrec; irec++ ) {
    	// Check whether the row should be evaluated.
    	if ( evaluator != null ) {
    		if ( ! evaluator.evaluate(this.table, irec) ) {
    			// Condition was not met so don't process the row.
    			if ( Message.isDebugOn ) {
    				Message.printStatus(2, routine, "evaluate() returned false for irec=" + irec);
    			}
    			continue;
    		}
    		else {
    			if ( Message.isDebugOn ) {
    				Message.printStatus(2, routine, "evaluate() returned true for irec=" + irec);
    			}
    		}
    	}
        // Initialize the values to null or missing:
    	// - if mixed-case math is done below, cast to use non-missing values in the computation section
        input1ValDouble = Double.NaN;
        input1ValInteger = null;
        input1ValLong = null;
        input2ValDouble = Double.NaN;
        input2ValInteger = null;
        input2ValLong = null;
        outputValDouble = nonValue;
        outputValInteger = null;
        outputValLong = null;
        // Get the first input value.
        try {
        	if ( input1Field >= 0 ) {
        		// Using an input field, will be the case for most operations.
        		val = this.table.getFieldValue(irec, input1Field);
        		if ( input1FieldType == TableField.DATA_TYPE_DOUBLE ) {
        			input1ValDouble = (Double)val;
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_FLOAT ) {
        			Float input1ValFloat = (Float)val;
        			if ( input1ValFloat == null ) {
        				input1ValDouble = null;
        			}
        			else if ( input1ValFloat.isNaN() ) {
        				input1ValDouble = Double.NaN;
        			}
        			else {
        				input1ValDouble = input1ValFloat.doubleValue();
        			}
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        			input1ValInteger = (Integer)val;
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        			input1ValLong = (Long)val;
        		}
        		else {
        			// Leave as null.  Warnings will result.
        			problems.add("Don't understand how to get input value 1 from column (not double, integer, or long).");
        		}
        	}
        	else {
        		// Use the constant value.
        		if ( input1FieldType == TableField.DATA_TYPE_DOUBLE ) {
        			input1ValDouble = input1ConstantDouble;
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_FLOAT ) {
        			// Value was previously converted to a double when parsed:
        			// - this may not be needed but include for explanation
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        			input1ValInteger = input1ConstantInteger;
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        			input1ValLong = input1ConstantLong;
        		}
        		else {
        			// Leave as null.  Warnings will result.
        			problems.add("Don't understand how to get input value 1 from constant (not double, integer, or long).");
        		}
        	}
        }
        catch ( Exception e ) {
        	// Usually class cast exception.
            problems.add ( "Error getting value for row " + (irec + 1) +", input field 1 (" + val + ") (" + e + ")." );
            Message.printWarning(3, routine, e);
            continue;
        }

        // Get the second input value, which depends on the operator.

        if ( operator == DataTableMathOperatorType.CUMULATE ) {
        	// Second value is the previous row value for the input column.
        	if ( irec == 0 ) {
      			// First record - assume that the initial value was zero.
        		if ( (input1FieldType == TableField.DATA_TYPE_DOUBLE) ||
        			(input1FieldType == TableField.DATA_TYPE_FLOAT) ) {
        			input2ValDouble = Double.valueOf(0.0);
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        			input2ValInteger = Integer.valueOf(0);
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        			input2ValLong = Long.valueOf(0);
        		}
        		else {
        			// Leave as null.  Warnings will result.
        			problems.add("Don't understand how to get row " + (irec + 1) + " initial input value 2 for "
        				+ operator + " (not a double, integer, or long).");
        		}
        	}
        	else {
        		// Get the previous row's value in the input column.
        		try {
            		val = this.table.getFieldValue((irec - 1), input1Field);
            		if ( input1FieldType == TableField.DATA_TYPE_DOUBLE ) {
        		   		input2ValDouble = (Double)val;
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_FLOAT ) {
        		   		Float input2ValFloat = (Float)val;
        		   		if ( input2ValFloat == null ) {
       				  		input2ValDouble = null;
       			  		}
       			  		else if ( input2ValFloat.isNaN() ) {
       				  		input2ValDouble = Double.NaN;
       			  		}
       			  		else {
       				  		input2ValDouble = input2ValFloat.doubleValue();
       			  		}
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        		   		input2ValInteger = (Integer)val;
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        		   		input2ValLong = (Long)val;
        	   		}
        	   		else {
        	   			// Leave as null.  Warnings will result.
        	   			problems.add("Don't understand how to get row " + (irec + 1) + " input value 2 from previous row value for "
        	   				+ operator + " (not a double, integer, or long).");
        	   		}
           		}
           		catch ( Exception e ) {
               		problems.add ( "Error getting value for row " + (irec + 1) + " input field 2, previous row used for " + operator + " (" + e + ")." );
               		continue;
           		}
        	}
        }
        else if ( operator == DataTableMathOperatorType.DELTA ) {
        	// Second value is the previous row value for the output column.
        	if ( irec == 0 ) {
      			// First record - assume initial value was zero.
        		if ( (input1FieldType == TableField.DATA_TYPE_DOUBLE) ||
        			(input1FieldType == TableField.DATA_TYPE_FLOAT) ) {
        			input2ValDouble = Double.valueOf(0.0);
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        			input2ValInteger = Integer.valueOf(0);
        		}
        		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        			input2ValLong = Long.valueOf(0);
        		}
        		else {
        			// Leave as null.  Warnings will result.
        			problems.add("Don't understand how to get row " + (irec + 1) + " initial input value 2 for "
        				+ operator + " (not a double, integer, or long).");
        		}
        	}
        	else {
        		// Get the previous row's value in the input column.
        		try {
            		val = this.table.getFieldValue((irec - 1), input1Field);
            		if ( input1FieldType == TableField.DATA_TYPE_INT ) {
        		   		input2ValInteger = (Integer)val;
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_DOUBLE ) {
        		   		input2ValDouble = (Double)val;
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_FLOAT ) {
        		   		Float input2ValFloat = (Float)val;
        		   		if ( input2ValFloat == null ) {
       				  		input2ValDouble = null;
       			  		}
       			  		else if ( input2ValFloat.isNaN() ) {
       				  		input2ValDouble = Double.NaN;
       			  		}
       			  		else {
       				  		input2ValDouble = input2ValFloat.doubleValue();
       			  		}
        	   		}
            		else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
        		   		input2ValLong = (Long)val;
        	   		}
        	   		else {
        	   			// Leave as null.  Warnings will result.
        	   			problems.add("Don't understand how to get row " + (irec + 1) + " input value 2 from previous row value for "
        	   				+ operator + " (not a double, integer, or long).");
        	   		}
           		}
           		catch ( Exception e ) {
               		problems.add ( "Error getting value for row " + (irec + 1) + " input field 2, previous row used for " + operator + " (" + e + ")." );
               		continue;
           		}
        	}
        }
        else if ( input2Field >= 0 ) {
        	// All other operators.
            // Second value is determined from table column.
            try {
                val = this.table.getFieldValue(irec, input2Field);
                if ( input2FieldType == TableField.DATA_TYPE_INT ) {
                	input2ValInteger = (Integer)val;
                }
                else if ( input2FieldType == TableField.DATA_TYPE_DOUBLE ) {
                	input2ValDouble = (Double)val;
                }
            	else if ( input1FieldType == TableField.DATA_TYPE_FLOAT ) {
        	   		Float input2ValFloat = (Float)val;
        	   		if ( input2ValFloat == null ) {
       			  		input2ValDouble = null;
       		  		}
       		  		else if ( input2ValFloat.isNaN() ) {
       			  		input2ValDouble = Double.NaN;
       		  		}
       		  		else {
       			  		input2ValDouble = input2ValFloat.doubleValue();
       		  		}
        		}
                else if ( input2FieldType == TableField.DATA_TYPE_LONG ) {
                	input2ValLong = (Long)val;
                }
                else {
        	   		// Leave as null.  Warnings will result.
        			problems.add("Don't understand how to get initial row + " + (irec + 1) + " input value 2 if column is not integer, long, double, or float.");
                }
            }
            catch ( Exception e ) {
                problems.add ( "Error getting value for row " + (irec + 1) + " input field 2 (" + e + ")." );
                continue;
            }
        }
        else {
            // Second value is a constant (not from a table column).
        	if ( input2ConstantDouble != null ) {
	            input2ValDouble = input2ConstantDouble;
	        }
	        if ( input2ConstantInteger != null ) {
	            input2ValInteger = input2ConstantInteger;
	        }
	        if ( input2ConstantLong != null ) {
	            input2ValLong = input2ConstantLong;
	        }
        }
        // Compute the output value based on the operator:
        // - check for missing values and determine the output type
        if ( operator == DataTableMathOperatorType.CUMULATE ) {
        	// Handle calculation explicitly since different than other operators:
            // - use cumulative rather than any row value
	    	if ( (outputFieldType == TableField.DATA_TYPE_DOUBLE) ||
	    		(outputFieldType == TableField.DATA_TYPE_FLOAT) ) {
	    		// If input is still missing, set the output to missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
                	// Unable to compute so set to the non-value.
                    outputValDouble = nonValue;
                }
                else {
                	// Output is the previous cumulative value plus the input.
                	outputValDouble = cumulativeValDouble + input1ValDouble;
                	cumulativeValDouble = outputValDouble;
                }
            }
	    	else if ( outputFieldType == TableField.DATA_TYPE_INT ) {
                if ( input1ValInteger == null ) {
                	// Unable to compute so set to the non-value.
                    outputValInteger = null;
                }
                else {
               		outputValInteger = cumulativeValInteger + input1ValInteger;
                	cumulativeValInteger = outputValInteger;
                }
	    	}
	    	else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
                if ( input1ValLong == null ) {
                	// Unable to compute so set to the non-value.
                    outputValLong = null;
                }
                else {
               		outputValLong = cumulativeValLong + input1ValLong;
                	cumulativeValLong = outputValLong;
                }
	    	}
	    	else {
        		problems.add("Don't understand how to calculate cumulate output for row " + (irec + 1) + ".");
	    	}
        }
        else if ( operator == DataTableMathOperatorType.DELTA ) {
        	// Handle calculation explicitly since different than other operators:
            // - second input is the previous row value
	    	if ( (outputFieldType == TableField.DATA_TYPE_DOUBLE) ||
	    		(outputFieldType == TableField.DATA_TYPE_FLOAT) ) {
	    		// If input is still missing, set the output to missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() || (input2ValDouble == null) || (input2ValDouble.isNaN())) {
                	// Unable to compute so set to the non-value.
                    outputValDouble = nonValue;
                }
                else {
                	// Output is the current row value minus the previous row value.
                	outputValDouble = input1ValDouble - input2ValDouble;
                }
            }
	    	else if ( outputFieldType == TableField.DATA_TYPE_INT ) {
                if ( (input1ValInteger == null) || (input2ValInteger == null) ) {
                	// Unable to compute so set to the non-value.
                    outputValInteger = null;
                }
                else {
               		outputValInteger = input1ValInteger - input2ValInteger;
                }
	    	}
	    	else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
                if ( (input1ValLong == null) || (input2ValLong == null) ) {
                	// Unable to compute so set to the non-value.
                    outputValLong = null;
                }
                else {
               		outputValLong = input1ValLong - input2ValLong;
                }
	    	}
	    	else {
        		problems.add("Don't understand how to calculate delta output for row " + (irec + 1) + ".");
	    	}
        }
        else if ( operator == DataTableMathOperatorType.TO_INTEGER ) {
        	// Output should be set as an integer.
    		// TODO smalers 2021-09-20 output field was determined above.
            //outputFieldType = TableField.DATA_TYPE_INT;
			// Only need the first input:
			// - set integer and double in case output table column is not configured properly as integer
        	if ( (input1FieldType == TableField.DATA_TYPE_DOUBLE) ||
        		(input1FieldType == TableField.DATA_TYPE_FLOAT) ) {
	            if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
	                outputValInteger = null;
	            }
	            else {
	                outputValInteger = input1ValDouble.intValue();
	            }
        	}
        	else if ( input1FieldType == TableField.DATA_TYPE_INT ) {
                if ( input1ValInteger == null ) {
                    outputValInteger = null;
                }
                else {
                	outputValInteger = input1ValInteger;
                }
            }
        	else if ( input1FieldType == TableField.DATA_TYPE_LONG ) {
                if ( input1ValLong == null ) {
                    outputValLong = null;
                }
                else {
                	outputValLong = input1ValLong;
                }
            }
        	else {
        		problems.add("Don't understand how to calculate output for row " + (irec + 1) + " input 1 column type.");
        	}
		}
        else if ( !requireInput2 ) {
        	// Only Input1 is required, such as for ASSIGN.
	    	if ( (outputFieldType == TableField.DATA_TYPE_DOUBLE) ||
	    		(outputFieldType == TableField.DATA_TYPE_FLOAT) ) {
	    		// Do math as doubles.
	    		// If mixed input types, use integer values if double is missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
                	if ( input1ValInteger != null ) {
                		// Mixed case so get input from integer.
                		input1ValDouble = Double.valueOf(input1ValInteger);
                	}
                	else if ( input1ValLong != null ) {
                		// Mixed case so get input from long.
                		input1ValDouble = Double.valueOf(input1ValLong);
                	}
                }
	    		// If input is still missing, set the output to missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
                	// Unable to compute so set to the non-value.
                    outputValDouble = nonValue;
                }
                else if ( operator == DataTableMathOperatorType.ASSIGN ) {
                	outputValDouble = input1ValDouble;
                }
                else {
        		    problems.add("Don't understand how to calculate row " + (irec + 1) + " double output.");
        	    }
	    	}
	    	else if ( outputFieldType == TableField.DATA_TYPE_INT ) {
	    		// Do math as integer.
                if ( input1ValInteger == null ) {
                	if ( input1ValLong != null ) {
                		// Mixed case so get input from long.
                		input1ValInteger = Integer.valueOf(input1ValLong.intValue());
                	}
                	else if ( (input1ValDouble != null) && !input1ValDouble.isNaN() ) {
                		// Mixed case so get input from double.
                		input1ValInteger = Integer.valueOf(input1ValDouble.intValue());
                	}
                }
                // If input is still missing, set to missing.
                if ( input1ValInteger == null ) {
                	if ( Message.isDebugOn ) {
                		Message.printStatus(2, routine, "Setting integer row " + (irec + 1) + " output to null since have null input.");
                	}
                    outputValInteger = null;
                }
                if ( operator == DataTableMathOperatorType.ASSIGN ) {
                	outputValInteger = input1ValInteger;
                }
                else {
        		    problems.add("Don't understand how to calculate row " + (irec + 1) + " integer output for operator: " + operator);
        	    }
	    	}
	    	else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
	    		// Do math as long.
                if ( input1ValLong == null ) {
                	if ( input1ValInteger != null ) {
                		// Mixed case so get input from integer.
                		input1ValLong = Long.valueOf(input1ValInteger);
                	}
                	else if ( (input1ValDouble != null) && !input1ValDouble.isNaN() ) {
                		// Mixed case so get input from double.
                		input1ValLong = Long.valueOf(input1ValDouble.intValue());
                	}
                }
                // If input is still missing, set to missing.
                if ( input1ValLong == null ) {
                	if ( Message.isDebugOn ) {
                		Message.printStatus(2, routine, "Setting long row " + (irec + 1) + " output to null since have null input.");
                	}
                    outputValLong = null;
                }
                if ( operator == DataTableMathOperatorType.ASSIGN ) {
                	outputValLong = input1ValLong;
                }
                else {
        		    problems.add("Don't understand how to calculate row " + (irec + 1) + " long output for operator: " + operator);
        	    }
	    	}
        }
        else {
        	// All other operators.  Handle the operations depending on data type.
        	// The following operators need two input values to compute.
	    	//if ( (input1FieldType == TableField.DATA_TYPE_DOUBLE) || (input2FieldType == TableField.DATA_TYPE_DOUBLE) ||
	    	//	(input1FieldType != input2FieldType) ) { // }
	    	if ( (outputFieldType == TableField.DATA_TYPE_DOUBLE) ||
	    		(outputFieldType == TableField.DATA_TYPE_FLOAT) ) {
	    		// Do math as doubles.
	    		// Double input and double output (or mixed in which case double values were set above).
	    		// TODO smalers 2021-09-20 output field was determined above.
	    		//outputFieldType = TableField.DATA_TYPE_DOUBLE;
	    		// If mixed input types, use integer or long values if double is missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() ) {
                	if ( input1ValInteger != null ) {
                		// Mixed case so get input from integer.
                		input1ValDouble = Double.valueOf(input1ValInteger);
                	}
                	else if ( input1ValLong != null ) {
                		// Mixed case so get input from long.
                		input1ValDouble = Double.valueOf(input1ValLong);
                	}
                }
                if ( (input2ValDouble == null) || input2ValDouble.isNaN() ) {
                	if ( input2ValInteger != null ) {
                		// Mixed case so get input from integer.
                		input2ValDouble = Double.valueOf(input2ValInteger);
                	}
                	else if ( input2ValLong != null ) {
                		// Mixed case so get input from long.
                		input2ValDouble = Double.valueOf(input2ValLong);
                	}
                }
	    		// If input is still missing, set the output to missing.
                if ( (input1ValDouble == null) || input1ValDouble.isNaN() || (input2ValDouble == null) || input2ValDouble.isNaN() ) {
                	// Unable to compute so set to the non-value.
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
                else if ( operator == DataTableMathOperatorType.MAX ) {
                	outputValDouble = Math.max(input1ValDouble, input2ValDouble);
                }
                else if ( operator == DataTableMathOperatorType.MIN ) {
                	outputValDouble = Math.min(input1ValDouble, input2ValDouble);
                }
                else {
                	if ( outputFieldType == TableField.DATA_TYPE_DOUBLE ) {
                		problems.add("Don't understand how to calculate row " + (irec + 1) + " double output.");
                	}
                	else if ( outputFieldType == TableField.DATA_TYPE_FLOAT ) {
                		problems.add("Don't understand how to calculate row " + (irec + 1) + " float output.");
                	}
        	    }
            }
	    	//else if ( (input1FieldType == TableField.DATA_TYPE_INT) && (input2FieldType == TableField.DATA_TYPE_INT) ) { // }
	    	else if ( outputFieldType == TableField.DATA_TYPE_INT ) {
	    		// Do math as integer.
	    		// Integer input and integer output:
	    		// - both inputs must be consistent
	    		// TODO smalers 2021-09-20 output field was determined above.
	    		//outputFieldType = TableField.DATA_TYPE_INT;
                if ( input1ValInteger == null ) {
					if ( input1ValLong != null ) {
						// Mixed case so get input from long.
						input1ValInteger = Integer.valueOf(input1ValLong.intValue());
					}
					else if ( (input1ValDouble != null) && !input1ValDouble.isNaN() ) {
						// Mixed case so get input from double.
						input1ValInteger = Integer.valueOf(input1ValDouble.intValue());
					}
                }
                if ( input2ValInteger == null ) {
                	if ( input2ValLong != null ) {
                		// Mixed case so get input from long.
                		input2ValInteger = Integer.valueOf(input2ValLong.intValue());
                	}
                	else if ( (input2ValDouble != null) && !input2ValDouble.isNaN() ) {
                		// Mixed case so get input from double.
                		input2ValInteger = Integer.valueOf(input2ValDouble.intValue());
                	}
                }
                // If input is still missing, set to missing.
                if ( (input1ValInteger == null) || (input2ValInteger == null) ) {
                	if ( Message.isDebugOn ) {
                		Message.printStatus(2, routine, "Setting integer row " + (irec + 1) + " output to null since have null input.");
                	}
                    outputValInteger = null;
                }
                else if ( operator == DataTableMathOperatorType.ADD ) {
                	outputValInteger = input1ValInteger + input2ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.ASSIGN ) {
                	outputValInteger = input1ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.DIVIDE ) {
                    if ( input2ValInteger == 0 ) {
                        outputValInteger = null;
                    }
                    else {
                        outputValInteger = input1ValInteger / input2ValInteger;
                    }
                }
                else if ( operator == DataTableMathOperatorType.MAX ) {
                	outputValInteger = Math.max(input1ValInteger, input2ValInteger);
                }
                else if ( operator == DataTableMathOperatorType.MIN ) {
                	outputValInteger = Math.min(input1ValInteger, input2ValInteger);
                }
                else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
                	outputValInteger = input1ValInteger * input2ValInteger;
                }
                else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
                	outputValInteger = input1ValInteger - input2ValInteger;
                }
                else {
        		    problems.add("Don't understand how to calculate row " + (irec + 1) + " integer output for operator: " + operator);
        	    }
            }
	    	else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
	    		// Do math as long.
	    		// Long input and long output:
	    		// - both inputs must be consistent
	    		// TODO smalers 2021-09-20 output field was determined above.
	    		//outputFieldType = TableField.DATA_TYPE_LONG;
                if ( input1ValLong == null ) {
					if ( input1ValInteger != null ) {
						// Mixed case so get input from integer.
						input1ValLong = Long.valueOf(input1ValInteger);
					}
					else if ( (input1ValDouble != null) && !input1ValDouble.isNaN() ) {
						// Mixed case so get input from double.
						input1ValLong = Long.valueOf(input1ValDouble.intValue());
					}
                }
                if ( input2ValLong == null ) {
                	if ( input2ValInteger != null ) {
                		// Mixed case so get input from integer.
                		input2ValLong = Long.valueOf(input2ValInteger);
                	}
                	else if ( (input2ValDouble != null) && !input2ValDouble.isNaN() ) {
                		// Mixed case so get input from double.
                		input2ValLong = Long.valueOf(input2ValDouble.intValue());
                	}
                }
                // If input is still missing, set to missing.
                if ( (input1ValLong == null) || (input2ValLong == null) ) {
                	if ( Message.isDebugOn ) {
                		Message.printStatus(2, routine, "Setting long row " + (irec + 1) + " output to null since have null input.");
                	}
                    outputValLong = null;
                }
                else if ( operator == DataTableMathOperatorType.ADD ) {
                	outputValLong = input1ValLong + input2ValLong;
                }
                else if ( operator == DataTableMathOperatorType.ASSIGN ) {
                	outputValLong = input1ValLong;
                }
                else if ( operator == DataTableMathOperatorType.DIVIDE ) {
                    if ( input2ValLong == 0 ) {
                        outputValLong = null;
                    }
                    else {
                        outputValLong = input1ValLong / input2ValLong;
                    }
                }
                else if ( operator == DataTableMathOperatorType.MAX ) {
                	outputValLong = Math.max(input1ValLong, input2ValLong);
                }
                else if ( operator == DataTableMathOperatorType.MIN ) {
                	outputValLong = Math.min(input1ValLong, input2ValLong);
                }
                else if ( operator == DataTableMathOperatorType.MULTIPLY ) {
                	outputValLong = input1ValLong * input2ValLong;
                }
                else if ( operator == DataTableMathOperatorType.SUBTRACT ) {
                	outputValLong = input1ValLong - input2ValLong;
                }
                else {
        		    problems.add("Don't understand how to calculate row " + (irec + 1) + " long output for operator: " + operator);
        	    }
            }
	    	else {
	    		// Unhandled combination:
	    		// - TODO smalers 2021-08-19 Need to fix the code?
                problems.add ( "Unhandled case setting the results for row " + (irec + 1) + " - check software design." );
	    	}
        }
        // Set the output value:
        // - do additional casting if necessary
        try {
            if ( outputFieldType == TableField.DATA_TYPE_INT ) {
            	if ( Message.isDebugOn ) {
            		Message.printStatus(2, routine, "Setting integer output for row " + (irec + 1) + " to: " + outputValInteger);
            	}
                this.table.setFieldValue(irec, outputField, outputValInteger );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
                this.table.setFieldValue(irec, outputField, outputValLong );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_DOUBLE ) {
                this.table.setFieldValue(irec, outputField, outputValDouble );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_FLOAT ) {
            	Float outputValFloat = null;
            	if ( outputValDouble.isNaN() ) {
            		outputValFloat = Float.NaN;
            	}
            	else if ( outputValDouble != null ) {
            		outputValFloat = outputValDouble.floatValue();
            	}
                this.table.setFieldValue(irec, outputField, outputValFloat );
            }
            else {
            	// TODO SAM 2016-08-02 may need to support other output columns like strings.
                problems.add ( "Error setting value in row " + (irec + 1) + " - don't know how to handle table column type " +
                	TableField.getDataTypeAsString(outputFieldType) );
            }
        }
        catch ( Exception e ) {
            if ( outputFieldType == TableField.DATA_TYPE_INT ) {
                problems.add ( "Error setting value in row " + (irec + 1) + " to " + outputValInteger + " (" + e + ")." );
            }
            else if ( outputFieldType == TableField.DATA_TYPE_LONG ) {
                problems.add ( "Error setting value in row " + (irec + 1) + " to " + outputValLong + " (" + e + ")." );
            }
            else if ( (outputFieldType == TableField.DATA_TYPE_DOUBLE) || (outputFieldType == TableField.DATA_TYPE_FLOAT) ) {
            	// Double and Float use double for calculations.
                problems.add ( "Error setting value in row " + (irec + 1) + " to " + outputValDouble + " (" + e + ")." );
            }
            else {
            	problems.add ( "Error setting value in row " + (irec + 1) + " (" + e + ")." );
            }
        }
    }
}

/**
 * Indicate whether the operator requires a second input value.
 * @param operator operator that is being used
 * @return true if the second input value is needed, false if not
 */
public static boolean requiresInput2 ( DataTableMathOperatorType operator ) {
    if ( (operator == DataTableMathOperatorType.ASSIGN) ||
    	(operator == DataTableMathOperatorType.CUMULATE) ||
        (operator == DataTableMathOperatorType.DELTA) ||
    	(operator == DataTableMathOperatorType.TO_INTEGER) ) {
    	// Only one input value is required.
    	return false;
    }
    else {
    	// Two input values are required.
    	return true;
    }
}

}