// ----------------------------------------------------------------------------
// DataTestFunction_Constant - a function which represents a constant value.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-18	JTS, RTi		Added support for an input data ID
//					of "MISSING".
// 2006-05-02	JTS, RTi		Removed "MISSING" in favor of using
//					the new IsMissing() function.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

import RTi.Util.Time.DateTime;

/**
This class represents a constant value.
*/
public class DataTestFunction_Constant
extends DataTestFunction {

/**
The constant value.
*/
private double __value = 0;

/**
Empty constructor.
*/
public DataTestFunction_Constant() 
throws Exception {
	super(1);
}

/**
Copy constructor.
@param f the DataTestFunction_Constant to copy.
*/
public DataTestFunction_Constant(DataTestFunction f) {
	super(f);
	__value = ((DataTestFunction_Constant)f).__value;
}

/**
Constructor.
@param dataModel the model containing the data to set up the function.
@throws Exception if any necessary data are missing from the data model.
*/
public DataTestFunction_Constant(DataTestFunctionDataModel dataModel) 
throws Exception {
	super(dataModel, 1);
}

/**
Returns the constant value.
@return the constant value.
*/
public double evaluate(DateTime dateTime) {
	setLastResult(__value);
	return __value;
}

/**
Returns the default positive message ("$VALUE") 
(see DataTestFunction.buildPositiveResultMessage()).
@return the default positive message ("$VALUE")
(see DataTestFunction.buildPositiveResultMessage()).
*/
public String getDefaultPositiveMessage() {
	return "$VALUE";
}

/**
Sets up the test data by treating the input ID as a number.
@param startDate unused.
@param endDate unused.
@return true.
*/
public boolean setTestData(DateTime startDate, DateTime endDate) {
	String id = (String)(getInputDataIDs()[0]);
	__value = (new Double(id)).doubleValue();
	setLastResult(DMIUtil.MISSING_DOUBLE);
	return true;
}

/**
Returns the constant value in string format.
@return the constant value in string format.
*/
public String toString(DateTime dt) {
	return "" + __value;
}

}
