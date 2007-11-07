// ----------------------------------------------------------------------------
// DataTestExpression_Add - class that stores information for an addition
//	expression.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-15	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.Util.Time.DateTime;

/**
This class stores information for an addition expression.
*/
public class DataTestExpression_Add
extends DataTestExpression {

/**
Empty constructor.
*/
public DataTestExpression_Add() 
throws Exception {}

/**
Constructor.
@param model a data model containing data to use for this expression.
@throws Exception if not all the data values in the data model have been filled 
out.
*/
public DataTestExpression_Add(DataTestExpressionDataModel model) 
throws Exception {
	super(model);
}

/**
Copy constructor.
@param e the DataTestExpression to copy.
*/
public DataTestExpression_Add(DataTestExpression e) {
	super(e);
}

/**
Evaluates the expression for the given date/time.
@param dateTime the date/time at which evaluation should occur.
@return the sum of the expressions.
*/
public double evaluate(DateTime dateTime) {
	double d = getLeftSide().evaluate(dateTime) 
		+ getRightSide().evaluate(dateTime);
	setLastResult(d);
	return d;
}

/**
Returns a string representing the expression being evaluated at the given 
date/time.
@param dt the Date/Time to evaluate the expression at in order to return it
in string form.  Can be null, though less information will be returned about
the expression.
@return a string representing the expression being evaluated at the given 
date/time.
*/
public String toString(DateTime dt) {
	return "(" + getLeftSide().toString(dt) + " + " 
		+ getRightSide().toString(dt) + ")";
}

}
