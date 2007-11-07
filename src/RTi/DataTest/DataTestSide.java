// ----------------------------------------------------------------------------
// DataTestSide - Abstract base class for both expressions and functions.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-14	JTS, RTi		evaluatePositiveResult() now takes a
//					DataTest parameter.
// 2006-05-03	JTS, RTi		* Removed AlertIOInterface and related
//					  set/get methods.
//					* write() now returns the ID number of
//					  the object that was written, rather 
//					  than a boolean.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.Time.DateTime;

/**
This class is the abstract base class for all expression and function classes.
*/
public abstract class DataTestSide {

/**
Used to differentiate internally between expressions and functions.
*/
public static final int 
	UNKNOWN = -1,
	EXPRESSION = 1,
	FUNCTION = 2;

/**
The result of the last evaluate() call made.
*/
private double __lastResult = DMIUtil.MISSING_DOUBLE;

/**
A short message explaining the side in case of positive evaluation.
*/
private String __positiveMessage = DMIUtil.MISSING_STRING;

/**
Empty constructor.
*/
public DataTestSide() {}

/**
Copy constructor.
@param s the DataTestSide to be copied.
*/
public DataTestSide(DataTestSide s) {
	__lastResult = s.__lastResult;
	__positiveMessage = s.__positiveMessage;
}

/**
Builds a message for a positive result at the given date/time.
@param dateTime the date/time for which to build a positive result message.
@return a message representing this data side at the given date/time.
*/
public abstract String buildPositiveResultMessage(DateTime dateTime);

/**
Copies a DataTestSide.  
@param side the DataTestSide to copy.
@param type EXPRESSION or FUNCTION.
@return a copy of the DataTestSide.
*/
public static DataTestSide copy(DataTestSide side, int type) {
	if (type == EXPRESSION) {
		return copyExpression((DataTestExpression)side);
	}
	else {
		return copyFunction((DataTestFunction)side);
	}
}

/**
Copies a DataTestExpression.
@param e the DataTestExpression to copy.
@return a copy of the DataTestExpression or null, if the type of the expression
is not known (or the expression to copy is null).
*/
public static DataTestExpression copyExpression(DataTestExpression e) {
	if (e == null) {
		return null;
	}

	int type = e.getExpressionType();
	if (type == DataTestExpression.TYPE_ADD) {
		return new DataTestExpression_Add(e);
	}
	else if (type == DataTestExpression.TYPE_AND) {
		return new DataTestExpression_And(e);
	}
	else if (type == DataTestExpression.TYPE_DIVIDE) {
		return new DataTestExpression_Divide(e);
	}
	else if (type == DataTestExpression.TYPE_EQUALS) {
		return new DataTestExpression_Equals(e);
	}
	else if (type == DataTestExpression.TYPE_GREATERTHAN) {
		return new DataTestExpression_GreaterThan(e);
	}
	else if (type == DataTestExpression.TYPE_GREATERTHANOREQUALTO) {
		return new DataTestExpression_GreaterThanOrEqualTo(e);
	}
	else if (type == DataTestExpression.TYPE_LESSTHAN) {
		return new DataTestExpression_LessThan(e);
	}
	else if (type == DataTestExpression.TYPE_LESSTHANOREQUALTO) {
		return new DataTestExpression_LessThanOrEqualTo(e);
	}
	else if (type == DataTestExpression.TYPE_MULTIPLY) {
		return new DataTestExpression_Multiply(e);
	}
	else if (type == DataTestExpression.TYPE_NOTEQUAL) {
		return new DataTestExpression_NotEqual(e);
	}
	else if (type == DataTestExpression.TYPE_OR) {
		return new DataTestExpression_Or(e);
	}
	else if (type == DataTestExpression.TYPE_SUBTRACT) {
		return new DataTestExpression_Subtract(e);
	}
	else {
		return null;
	}
}

/**
Copies a DataTestFunction.
@param f the DataTestFunction to copy.
@return a copy of the DataTestFunction, or null if the type of the function is
not known (or if the function to copy is null).
*/
public static DataTestFunction copyFunction(DataTestFunction f) {
	if (f == null) {
		return null;
	}

	int type = f.getFunctionType();
	if (type == DataTestFunction.TYPE_CONSTANT) {
		return new DataTestFunction_Constant(f);
	}
	else if (type == DataTestFunction.TYPE_GETTSVALUE) {
		return new DataTestFunction_GetTSValue(f);
	}
	else if (type == DataTestFunction.TYPE_ISMISSING) {
		return new DataTestFunction_IsMissing(f);
	}
	else if (type == DataTestFunction.TYPE_ISNOTMISSING) {
		return new DataTestFunction_IsNotMissing(f);
	}	
	else if (type == DataTestFunction.TYPE_TSDELTA) {
		return new DataTestFunction_TSDelta(f);
	}
	else {
		return null;
	}
}

/**
The evaluation function that must be defined in all extending classes.
@param dateTime the date/time at which the expression or function will be
evaluated.
@return the evaluation result.
*/
public abstract double evaluate(DateTime dateTime);

/**
Evaluates for a positive result, creating DataTestResults.  This is called once
evalute() returns a TRUE value.
@param test the DataTest in which the expression was evaluated.
@param result the Vector into which DataTestResult objects are accumulated.
@param testTime the date/time at which this side was evaluated.
@param level the level of the DataTestResult, which determines its location
within the overall expression tree.
*/
public abstract void evaluatePositiveResult(DataTest test, Vector results, 
DateTime testTime, int level);

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	__positiveMessage = null;
	super.finalize();
}


/**
Returns the data test side name given the data test side type (e.g., FUNCTION),
or null if the type is not known.
@return the data test side name given the data test side type (e.g., FUNCTION), 
or null if the type is not known.
*/
public static String getSideName(int type) {
	if (type == EXPRESSION) {
		return "expression";
	}
	else if (type == FUNCTION) {
		return "function";
	}
	else {
		return null;
	}
}

/**
Returns the data test side type given the data test side name (e.g., "function")
or null if the name is null or unrecognized.
@return the data test side type given the data test side name (e.g., "function")
or null if the name is null or unrecognized.
*/
public static int getSideType(String name) {
	if (name == null) {
		return UNKNOWN;
	}
	else if (name.equalsIgnoreCase("expression")) {
		return EXPRESSION;
	}
	else if (name.equalsIgnoreCase("function")) {
		return FUNCTION;
	}
	else {
		return UNKNOWN;
	}
}

/**
Returns the last evaluation result.
@return the last evaluation result.
*/
public double getLastResult() {
	return __lastResult;
}

/**
Returns the function message.
@return the function message.
*/
public String getPositiveMessage() {
	return __positiveMessage;
}

/**
Returns whether the implementing class is an expression or not.
@return whether the implementing class in an expression or not.
*/
public abstract boolean isExpression();

/**
Returns whether the implementing class is a function or not.
@return whether the implementing class is a function or not.
*/
public abstract boolean isFunction();

/**
Sets the last evaluation result.
@param lastResult the last evaluation result.
*/
public void setLastResult(double lastResult) {
	__lastResult = lastResult;
}

/**
Sets the message for a positive expression.
@param message the function message to set.
*/
public void setPositiveMessage(String message) {
	__positiveMessage = message;
}

/**
Returns a string representation of the function or expression at the given
date/time.
@return a string representation of the function or expression at the given
date/time.
*/
public abstract String toString(DateTime dt);

/**
Writes this object to the IO Interface.
@param io the AlertIOInterface to use for writing the object.
@return the ID number of the side that was written, or -1 if the side was
not successfully written.
*/
public abstract int write(AlertIOInterface io)
throws Exception;

}
