// ----------------------------------------------------------------------------
// DataTestFunction_IsNotMissing - a function which checks to see if the value 
//	in a time series at a date/time is a missing value.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-05-02	J. Thomas Sapienza, RTi	Initial version.
// 2006-05-17	JTS, RTi		Added hasData() check to evaluate() and
//					toString().
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

/**
This class is a function which checks to see if the value in a time series 
at a date/time is a missing value.
*/
public class DataTestFunction_IsNotMissing
extends DataTestFunction {

/**
Whether the MISSING value is defined in internal properties as being stored in
the time series.
*/
private boolean __missingFromTS = false;

/**
Whether checkProperties() has been called or not.
*/
private boolean __propertiesChecked = false;

/**
The value of the missing data.
*/
private double __missing = DMIUtil.MISSING_DOUBLE;

/**
Empty constructor.  
*/
public DataTestFunction_IsNotMissing() 
throws Exception {
	super(1);
	setNumTS(1);
	int[] positions = { 0 };
	setTSIDPositions(positions);	
}

/**
Copy constructor.  
@param f the DataTestFunction_IsNotMissing to copy.
*/
public DataTestFunction_IsNotMissing(DataTestFunction f) {
	super(f);

	DataTestFunction_IsNotMissing i = (DataTestFunction_IsNotMissing)f;

	__missingFromTS = i.__missingFromTS;
	__missing = i.__missing;
	__propertiesChecked = i.__propertiesChecked;
}

/**
Constructor.
@param dataModel the model to use for setting up this function's data.
@throws Exception if any necessary data are missing from the data model.
*/
public DataTestFunction_IsNotMissing(DataTestFunctionDataModel dataModel) 
throws Exception {
	super(dataModel, 1);
	setNumTS(1);
	int[] positions = { 0 };
	setTSIDPositions(positions);
}

/**
Evaluates the function, returning the value stored in the time series at
the specified date/time.
@param dateTime the date/time at which to return a value from the TS.
@return the time series value at the specified date/time.
*/
public double evaluate(DateTime dateTime) {
	if (!getTS(0).hasData()) {
		return DataTest.FALSE;
	}

	double d = getTS(0).getDataValue(dateTime);
	setLastResult(d);

	if (d != __missing) {
		return DataTest.TRUE;
	}
	else {
		return DataTest.FALSE;
	}
}

/**
Returns the default positive result message
(see DataTestFunction.buildPositiveResultMessage()).
@return the default positive result message
(see DataTestFunction.buildPositiveResultMessage()).
*/
public String getDefaultPositiveMessage() {
	return "IsNotMissing($TSID[0][$DATETIME])=$VALUE";
}

/**
Sets up the test data by reading the time series stored in the input IDs.
@param startDate the first date for which to set test data.  Can be null.
@param endDate the last date for which to set test data.  Can be null.
@return true if the data were set up properly.  False if not.
*/
public boolean setTestData(DateTime startDate, DateTime endDate) {
	setLastResult(DMIUtil.MISSING_DOUBLE);
	String id = (String)(getInputDataIDs()[0]);
	try {
		setTS(getTSSupplier().readTimeSeries(id,
			startDate, endDate, null, true), 0);
	}
	catch (Exception e) {
		Message.printWarning(2, "setTestData",
			"Could not read time series for TSID: '" + id + "'");
		Message.printWarning(2, "setTestdata", e);
		return false;
	}

	if (!__propertiesChecked) {
		if (getProperties() == null) {
			// do nothing -- defaults are set
		}
		else {
			String propValue = getProperties().getValue("Missing");
			if (propValue != null) {
				if (propValue.equalsIgnoreCase("TS")) {
					__missingFromTS = true;
				}
				else {
					try {
						__missing = (new Double(
							propValue))
							.doubleValue();
					}
					catch (Exception e) {
						Message.printWarning(2, 
							"setTestData",
							"\"Missing\" value in "
							+ "properties is not a "
							+ "number: \"" 
							+ propValue + "\".");
						Message.printWarning(2,
							"setTestData", e);
						// REVISIT (JTS - 2005-05-02)
						// Possibly want to return false
						// here.
					}
				}
			}
		}
		__propertiesChecked = true;
	}

	if (__missingFromTS) {
		__missing = getTS(0).getMissing();
	}
	return true;			
}

/**
Returns a representation of this time series' value at the specified date/time.
@return a representation of this time series' value at the specified date/time.
*/
public String toString(DateTime dateTime) {
	String tsid = "Null TS";
	String date = "Null Date";
	String value = "??";

	if (dateTime != null) {
		date = "" + dateTime;
	}

	if (getTS(0) != null) {
		tsid = "" + getTS(0).getIdentifier();
		if (getTS(0).hasData() && date != null) {
			value = "" + getTS(0).getDataValue(dateTime);
		}
	}

	return "IsNotMissing([" + tsid + "])[" + date + "]: " + value;
}

}
