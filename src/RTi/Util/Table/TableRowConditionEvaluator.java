// TableRowConditionEvaluator - evaluate whether a condition is true for a table row

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.Table;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;

/**
 * Create an evaluator for table row condition.
 * The condition can have syntax:
 * <pre>
 *     ColumnName Operator Value
 *     
 *     where:
 *     
 *     ColumnName is a column name as string
 *     Operator is <, <=, >, >=, ==, or !=
 *     Value is consistent with the column type
 * </pre>
 * This allows the evaluator to be initialized once and reused for multiple table rows.
 * Spaces may or may not be present around operator.
 *
 */
public class TableRowConditionEvaluator {
	
	/**
	 * String condition, if null, was not specified.
	 */
	private String condition = null;
	
	/**
	 * Table column number for left side of condition.
	 */
	private int conditionField = -1;
	
	/**
	 * Table column type for left side of condition.
	 */
	private int conditionFieldType = -1;
	
	/**
	 * Operator as string.
	 */
	private String operator = "??";
	
	/**
	 * Value to check against to evaluate criteria.
	 */
	private Double valueDouble = null;
	private Integer valueInt = null;
	private String valueString = null;
	private String valueStringUpper = null;
	
	/**
	 * List of rows to process.
	 * Currently only the following patterns are handled.
	 * <ul>
	 * <li> "first" - first row in the table
	 * <li> "last" - last row in the table
	 * </ul>
	 */
	private String [] processRows = new String[0];

	/**
	 * Constructor.
	 * TODO smalers, consolidate code with If() command and others (much of this is copied from If command).
	 * @param table data table to process with the evaluator
	 * @param condition condition string to evaluate
	 */
	public TableRowConditionEvaluator ( DataTable table, String condition )
	throws Exception {
		this ( table, condition, new String[0]);
	}

	/**
	 * Constructor.
	 * TODO smalers, consolidate code with If() command and others (much of this is copied from If command).
	 * @param table data table to process with the evaluator
	 * @param condition condition string to evaluate
	 * @param processRows an array of string patterns indicating row numbers to process.
	 * <ul>
	 * <li> first</li>
	 * <li> last</li>
	 * </ul>
	 */
	public TableRowConditionEvaluator ( DataTable table, String condition, String[] processRows )
	throws Exception {
		String routine = getClass().getSimpleName();
	    if ( ((condition == null) || condition.isEmpty()) && ((processRows == null) || (processRows.length == 0)) ) {
	    	throw new RuntimeException("Condition and rows to process are both null or empty.");
	    }
	    // OK if null.
	    this.condition = condition;
	    if ( processRows == null ) {
	    	// Set to empty list to streamline processing.
	    	processRows = new String[0];
	    }
	    this.processRows = processRows;
	    
	    if ( (condition != null) && !condition.isEmpty() ) {
    	// TODO SAM 2013-12-07 Figure out if there is a more elegant way to do this
    	// Currently only Value1 Operator Value2 is allowed.  Brute force split by finding the operator
    	int pos, pos1 = -1, pos2 = -1;
    	String value1 = "", value2 = "";
    	String op = "??";
    	String conditionUpper = condition.toUpperCase();
    	String message;
    	boolean compareAsStrings = false; // TODO enable later
    	if ( condition.indexOf("<=") > 0 ) {
    	    pos = condition.indexOf("<=");
    	    op = "<=";
    	    pos1 = pos;
    	    pos2 = pos + 2;
    	}
    	else if ( condition.indexOf("<") > 0 ) {
    	    pos = condition.indexOf("<");
    	    op = "<";
            pos1 = pos;
            pos2 = pos + 1;
    	}
        else if ( condition.indexOf(">=") > 0 ) {
            pos = condition.indexOf(">=");
            op = ">=";
            pos1 = pos;
            pos2 = pos + 2;
        }
        else if ( condition.indexOf(">") > 0 ) {
            pos = condition.indexOf(">");
            op = ">";
            pos1 = pos;
            pos2 = pos + 1;
        }
        else if ( condition.indexOf("==") > 0 ) {
            pos = condition.indexOf("==");
            op = "==";
            pos1 = pos;
            pos2 = pos + 2;
        }
        else if ( condition.indexOf("!=") > 0 ) {
            pos = condition.indexOf("!=");
            op = "!=";
            pos1 = pos;
            pos2 = pos + 2;
        }
        else if ( conditionUpper.indexOf("!CONTAINS") > 0 ) {
        	// Put this before the next "CONTAINS" operator
            pos = conditionUpper.indexOf("!CONTAINS");
            op = "!CONTAINS";
            pos1 = pos;
            pos2 = pos + 9;
            compareAsStrings = true; // "!contains" is only used on strings
        }
        else if ( conditionUpper.indexOf("CONTAINS") > 0 ) {
            pos = conditionUpper.indexOf("CONTAINS");
            op = "CONTAINS";
            pos1 = pos;
            pos2 = pos + 8;
            compareAsStrings = true; // "contains" is only used on strings
        }
        else if ( conditionUpper.indexOf("!ISEMPTY") > 0 ) {
        	// Put this before the next "ISEMPTY" operator
            pos = conditionUpper.indexOf("!ISEMPTY");
            op = "!ISEMPTY";
            pos1 = pos;
            pos2 = pos + 8;
            compareAsStrings = true; // "!isempty" is only used on strings
        }
        else if ( conditionUpper.indexOf("ISEMPTY") > 0 ) {
            pos = conditionUpper.indexOf("ISEMPTY");
            op = "ISEMPTY";
            pos1 = pos;
            pos2 = pos + 7;
            compareAsStrings = true; // "isempty" is only used on strings
        }
        else if ( condition.indexOf("=") > 0 ) {
            message = "Bad use of = in condition.  Use == to check for equality";
            throw new RuntimeException(message);
            /*
            Message.printWarning(3,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Use == to check for equality." ) );
                    */
        }
        else {
            message = "Unknown condition operator for \"" + condition + "\"";
            throw new RuntimeException(message);
            /*
            Message.printWarning(3,
                MessageUtil.formatMessageTag( command_tag, ++warning_count),
                routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Make sure condition operator is supported - refer to command editor and documentation." ) );
                    */
        }
    	String arg1 = condition.substring(0,pos1).trim();
    	this.operator = op;
    	if ( Message.isDebugOn ) {
    		Message.printStatus(2, routine, "Left side: " + arg1 );
    	}
    	if ( arg1.indexOf("${") >= 0 ) { // } to match bracket to help with editor
    		// TODO smalers - need to enable
    		//value1 = TSCommandProcessorUtil.expandParameterValue(this.getCommandProcessor(),this,arg1 );
    		//if ( Message.isDebugOn ) {
    			//Message.printStatus(2, routine, "Left side after expansion: " + value1 );
    		//}
    		value1 = arg1;
    	}
    	else {
    		value1 = arg1;
    	}
    	String arg2 = condition.substring(pos2).trim();
    	if ( Message.isDebugOn ) {
    		Message.printStatus(2, routine, "Right side:" + arg2 );
    	}
    	if ( arg2.indexOf("${") >= 0 ) {
    		// TODO smalers - need to enable
    		//value2 = TSCommandProcessorUtil.expandParameterValue(this.getCommandProcessor(),this,arg2 );
    		//if ( Message.isDebugOn ) {
    		//	Message.printStatus(2, routine, "Right side after expansion: " + value2 );
    		//}
    		value2 = arg2;
    	}
    	else {
    		value2 = arg2;
    	}
    	this.valueString = value2;
    	this.valueStringUpper = value2.toUpperCase();
    	if ( StringUtil.isInteger(value2) ) {
    		this.valueInt = new Integer(value2);
    	}
    	if ( StringUtil.isDouble(value2) ) {
    		this.valueDouble = new Double(value2);
    	}

	    // Make sure that the left side of the condition matches a table column
    	// - this will throw an exception
	    this.conditionField = table.getFieldIndex(value1);
	    this.conditionFieldType = table.getFieldDataType(this.conditionField);
	    }
	}
	
	/**
	 * Evaluate the table row using the condition.
	 * @param table table to evaluate
	 * @param row row to evaluate for the condition
	 */
	public boolean evaluate ( DataTable table, int row ) {

		if ( (this.processRows.length == 0) && ((condition == null) || condition.isEmpty()) ) {
			// No conditions to match.  Return true.
			// This should not happen because constructor forces at least one to be specified.
			return true;
		}

		// If rows to process have been specified, check them:
		// - default is false for rows to process and condition
		boolean result = false;
		if ( this.processRows.length > 0 ) {
			if ( evaluateRowNumber(table, row) ) {
				// Row is to be processed.
				result = true;
			}
   			if ( Message.isDebugOn ) {
   				Message.printStatus(2, "", "After evaluating row number " + row + ", result=" + result);
   			}
		}

		if ( (this.condition == null) || condition.isEmpty() ) {
			// Nothing else to evaluate.
			return result;
		}

		// Have a condition to further evaluate.
		// Get the value from the row.
		Object o = null;
		try {
			o = table.getFieldValue(row, this.conditionField);
			Double o_double = null;
			Integer o_int = null;
			String o_string = null;
			if ( o != null ) {
				o_string = "" + o;
				if ( o instanceof Integer ) {
					o_int = (Integer)o;
					o_double = o_int.doubleValue();
				}
				else if ( o instanceof Double ) {
					o_double = (Double)o;
				}
				else if ( o instanceof String ) {
					o_string = (String)o;
				}
				else {
					throw new Exception ("Evaluating condition not implemented for type: " + o.getClass() );
				}
				// Evaluate the condition.
				if ( this.operator.equals("<") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( o_int < this.valueInt ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( o_double < this.valueDouble ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( o_string.compareTo(this.valueString) < 0 ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("< operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("<=") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( o_int <= this.valueInt ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( o_double <= this.valueDouble ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("<= operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals(">") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( o_int > this.valueInt ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( o_double > this.valueDouble ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( o_string.compareTo(this.valueString) > 0 ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("> operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals(">=") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( o_int >= this.valueInt ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( o_double >= this.valueDouble ) {
						    return true;
						}
					}
				    else {
					    throw new Exception (">= operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("==") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( o_int.equals(this.valueInt) ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( o_double.equals(this.valueDouble) ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( o_string.equals(this.valueString) ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("== operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("!=") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_INT ) {
						if ( !o_int.equals(this.valueInt) ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_DOUBLE ) {
						if ( !o_double.equals(this.valueDouble) ) {
						    return true;
						}
					}
					else if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( !o_string.equals(this.valueString) ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("!= operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("CONTAINS") ) {
					//String o_upper = o_string.toUpperCase();
					//if ( (this.conditionFieldType == TableField.DATA_TYPE_STRING) && (o_upper.indexOf(this.valueStringUpper) >= 0) ) { // }
					if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( o_string.indexOf(this.valueString) >= 0 ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("CONTAINS operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("!CONTAINS") ) {
					//String o_upper = o_string.toUpperCase();
					//if ( (this.conditionFieldType == TableField.DATA_TYPE_STRING) && (o_upper.indexOf(this.valueStringUpper) < 0) ) { // }
					if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( o_string.indexOf(this.valueString) < 0 ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("!CONTAINS operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else if ( this.operator.equals("ISEMPTY") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( (o_string.isEmpty()) || (o_string == null) ) {
						    return true;
						}
					}
				    else {
				    	// For data types other than String not null is by definition not empty.
					    //throw new Exception ("ISEMPTY operator is not implemented for value " + o + ", data type: " + o.getClass() );
				    	return false;
				    }
				}
				else if ( this.operator.equals("!ISEMPTY") ) {
					if ( this.conditionFieldType == TableField.DATA_TYPE_STRING ) {
						if ( !o_string.isEmpty() && (o_string != null) ) {
						    return true;
						}
					}
				    else {
					    throw new Exception ("!ISEMPTY operator is not implemented for data type: " + o.getClass() );
				    }
				}
				else {
				    throw new Exception ("Operator " + operator + " is not recognized." );
				}
			}
			else {
				// Object is null:
				// - match isempty condition
				if ( this.operator.equals("ISEMPTY") ) {
					return true;
				}
			}
		}
		catch ( Exception e ) {
			// Error so rethrow.
			throw new RuntimeException(e);
		}
		// Did not match the criteria so return false.
		return false;
	}

	/**
	 * Determine whether to process the row, used when row numbers to process are specified.
	 * @param row row number 0+
	 * @return true if the row should be processed, false if not
	 */
	private boolean evaluateRowNumber ( DataTable table, int row ) {
		if ( this.processRows.length == 0 ) {
			// No filter for row numbers so process all.
			return true;
		}
		else if ( table.getNumberOfRecords() <= 0 ) {
			// No data in table so no need to process.
			return false;
		}
		else {
			// Check row numbers to process.
			for ( String processRow : this.processRows ) {
				if ( processRow.equals("*") ) {
					// Process all.
					return true;
				}
				else if ( processRow.equalsIgnoreCase("first") ) {
					if ( row == 0 ) {
						// Requesting the first row and first row is found.
						return true;
					}
				}
				else if ( processRow.equalsIgnoreCase("last") ) {
					if ( row == (table.getNumberOfRecords() - 1) ) {
						return true;
					}
				}
			}
			// Nothing found so return false.
			return false;
		}
	}
}