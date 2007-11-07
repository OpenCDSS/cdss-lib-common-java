// ----------------------------------------------------------------------------
// DataTestFunction_TSDelta - a function which calculates the delta between
//	two time series steps.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-22	JTS, RTi		toString(DateTime) now accepts null
//					DateTime values.
// 2006-05-17	JTS, RTi		Added hasData() check to evaluate() and
//					toString().
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.DMI.DMIUtil;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

/**
This class is a function which calculates the delta between two time series
steps.  Does not work with Irregular time series.
*/
public class DataTestFunction_TSDelta
extends DataTestFunction {

/**
The time step base (e.g., day, hour, etc).
*/
private int __base = DMIUtil.MISSING_INT;

/**
The multiplier that determines by how much the delta is computed.
*/
private int __mult = DMIUtil.MISSING_INT;

/**
Empty constructor.
*/
public DataTestFunction_TSDelta() 
throws Exception {
	super(1);
	setNumTS(1);
	int[] positions = { 0 };
	setTSIDPositions(positions);	
}

/**
Copy contructor.
@param f the DataTestFunction_TSDelta to be copied.
*/
public DataTestFunction_TSDelta(DataTestFunction f) {
	super(f);
}

/**
Constructor.
@param dataModel the model to use for setting up this function's data.
@throws Exception if any necessary data are missing from the data model.
*/
public DataTestFunction_TSDelta(DataTestFunctionDataModel dataModel) 
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
		return getTS(0).getMissing();
	}
	DateTime clone = new DateTime(dateTime);
	clone.addInterval(__base, __mult);
	double d1 = getTS(0).getDataValue(clone);
	double d2 = getTS(0).getDataValue(dateTime);
	if (DMIUtil.isMissing(d1) || DMIUtil.isMissing(d2)) {
		setLastResult(DMIUtil.MISSING_DOUBLE);
		return DMIUtil.MISSING_DOUBLE;
	}
	else {
		setLastResult(d2 - d1);
		return d2 - d1;
	}
}

/**
Returns the default positive result message
(see DataTestFunction.buildPositiveResultMessage()).
@return the default positive result message
(see DataTestFunction.buildPositiveResultMessage()).
*/
public String getDefaultPositiveMessage() {
	return "TSDelta($TSID[0][$DATETIME])=$VALUE";
}

/**
Sets up the test data by reading the time series stored in the input IDs.
@param startDate the first date for which to set test data.  Can be null.
@param endDate the last date for which to set test data.  Can be null.
@return true if the data were set up properly, false if not.
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
			"Could not read time series.");
		Message.printWarning(2, "setTestdata", e);
		return false;
	}

	__base = getTS(0).getDataIntervalBase();
	__mult = DMIUtil.MISSING_INT;
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
			DateTime clone = new DateTime(dateTime);
			clone.addInterval(__base, __mult);
			double d1 = getTS(0).getDataValue(clone);
			double d2 = getTS(0).getDataValue(dateTime);
			value = "" + (d1 - d2);
		}
	}

	return "TSDelta([" + tsid + "])[" + date + "]: " + value;
}

}
