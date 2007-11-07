// ----------------------------------------------------------------------------
// DataTestResult - stores the values from a positive data test result.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-22	JTS, RTi		toString(DateTime) now accepts null
//					DateTime values.
// 2006-04-14	JTS, RTi		Added the __test variable.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormat;

/**
This class stores data for a positive test result for one level of an 
expression tree.  Results are only built for expressions -- function values
are stored within a single expression's DataTestResult.  The level of an 
expression depends on its location within the overall expression tree.  The
top-most (main) expression is at level 0, any expression it directly references
are at level 1, etc.
*/
public class DataTestResult { 

/**
Static data initialization block.
*/
static {
	__testTimeFormat = new DateTimeFormat("yyyy-mm-dd hh:MM");
}

/**
The DataTest in which this result occurred.
*/
private DataTest __test = null;

/**
The time at which the positive test was performed.
*/
private DateTime __testTime = null;

/**
The time at which the positive test was performed.
*/
private static DateTimeFormat __testTimeFormat = null;

/**
The value from the left side of the expression.
*/
private double __leftSideValue = DMIUtil.MISSING_DOUBLE;

/**
The value from the right side of the expression.
*/
private double __rightSideValue = DMIUtil.MISSING_DOUBLE;

/**
The ID number of the expression that this result is tied to.
*/
private int __dataTestExpressionNum = DMIUtil.MISSING_INT;

/**
The ID number of the data test this result is tied to.
*/
private int __dataTestNum = DMIUtil.MISSING_INT;

/**
The level of this result.  Results are assigned a level based on the
depth in the tree of the expression which the result represents.  A top-level
expression is at level 0, while an expression under the top-level expression is
at 1, and so on.
*/
private int __level = 0;

/**
The message of this result.
*/
private String __message = DMIUtil.MISSING_STRING;

/**
Constructor.
*/
public DataTestResult() {}

/**
Copy constructor.
*/
public DataTestResult(DataTestResult r) {
	__test = r.__test;
	__testTime = new DateTime(r.__testTime);
	__leftSideValue = r.__leftSideValue;
	__rightSideValue = r.__rightSideValue;
	__dataTestExpressionNum = r.__dataTestExpressionNum;
	__dataTestNum = r.__dataTestNum;
	__level = r.__level;
	__message = r.__message;
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize()
throws Throwable {
	__test = null;
	__testTime = null;
	__message = null;
	super.finalize();
}

/**
Returns the DataTest in which this result occurred.
@return the DataTest in which this result occurred.
*/
public DataTest getDataTest() {
	return __test;
}

/**
Returns the DataTest ID of the test to which this result is tied.
@return the DataTest ID of the test to which this result is tied.
*/
public int getDataTestNum() {
	return __dataTestNum;
}

/**
Returns the ID of the data test expression this result is tied to.
@return the ID of the data test expression this result is tied to.
*/
public int getDataTestExpressionNum() {
	return __dataTestExpressionNum;
}

/**
Returns the left side value for this result's expression.
@return the left side value for this result's expression.
*/
public double getLeftSideValue() {
	return __leftSideValue;
}

/**
Returns the level of this result.
@return the level of this result.
*/
public int getLevel() {
	return __level;
}

/**
Returns this result's message.
@return this result's message.
*/
public String getMessage() {
	return __message;
}

/**
Returns the right side value of this result's expression.
@return the right side value of this result's expression.
*/
public double getRightSideValue() {
	return __rightSideValue;
}

/**
Returns the date/time of this result.
@return the date/time of this result.
*/
public DateTime getTestTime() {
	return __testTime;
}

/**
Returns the date/time format.
@return the date/time format.
*/
public DateTimeFormat getTestTimeFormat() {
	return __testTimeFormat;
}

/**
Sets the DataTest in which this result occurred.
@param test the DataTest in which this result occurred.
*/
public void setDataTest(DataTest test) {
	__test = test;
}

/**
Sets the DataTest in which this result occurred.
@param test the DataTest in which this result occurred.
*/
public void setDataTestNum(int dataTestNum) {
	__dataTestNum = dataTestNum;
}

/**
Sets the ID of the expression that this result is tied to.
@param num the ID of the expression this result is tied to.
*/
public void setDataTestExpressionNum(int num) {
	__dataTestExpressionNum = num;
}

/**
Sets the left value of this result's expression.
@param value the expression's left value.
*/
public void setLeftSideValue(double value) {
	__leftSideValue = value;
}

/**
Sets the level of this result.
@param level the level of this result.
*/
public void setLevel(int level) {
	__level = level;
}

/**
Sets this result's message.
@param message this result's message.
*/
public void setMessage(String message) {
	__message = message;
}

/**
Sets the result's expression's right side value.
@param value the value of the expression's right side.
*/
public void setRightSideValue(double value) {
	__rightSideValue = value;
}

/**
Sets the date/time at which this result was generated.
@param testTime the time at which this result was generated.
*/
public void setTestTime(DateTime testTime) {
	__testTime = testTime;
}

/**
Returns a String representation of this result for debugging.
@return a String representation of this result for debugging.
*/
public String toString() {
	return "DataTestResult[" + __level + "]: \n"
		+ "   Time:  " + __testTime + "\n"
		+ "   Ex#:   " + __dataTestExpressionNum + "\n"
		+ "   LH:    " + __leftSideValue + "\n"
		+ "   RH:    " + __rightSideValue + "\n"
		+ "   Mess: '" + __message + "'\n";
}

}
