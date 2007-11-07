// ----------------------------------------------------------------------------
// DataTestExpression_LessThan - class that stores information for a 
//	LESS THAN expression.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.Util.Time.DateTime;

/**
This class stores information for a LESS THAN expression.
*/
public class DataTestExpression_LessThan
extends DataTestExpression {

/**
Empty constructor.
*/
public DataTestExpression_LessThan() 
throws Exception {}

/**
Constructor.
@param model a data model containing data to use for this expression.
@throws Exception if not all the data values in the data model have been filled 
out.
*/
public DataTestExpression_LessThan(DataTestExpressionDataModel model) 
throws Exception {
	super(model);
}

/**
Copy constructor.
@param e the DataTestExpression to copy.
*/
public DataTestExpression_LessThan(DataTestExpression e) {
	super(e);
}

/**
Evaluates the expression for the given date/time.
@param dateTime the date/time at which evaluation should occur.
@return DataTest.TRUE or DataTest.FALSE.
*/
public double evaluate(DateTime dateTime) {
	if (getLeftSide().evaluate(dateTime) 
	    < getRightSide().evaluate(dateTime)) {
	    	setLastResult(DataTest.TRUE);
		return DataTest.TRUE;
	}
	else {
	    	setLastResult(DataTest.FALSE);
		return DataTest.FALSE;
	}
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
	return "(" + getLeftSide().toString(dt) + " < " 
		+ getRightSide().toString(dt) + ")";
}

}
