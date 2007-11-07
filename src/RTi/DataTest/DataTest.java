// ----------------------------------------------------------------------------
// DataTest - class for holding data for a DataTest.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-20	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-14	JTS, RTi		Added done() so that DataTests can 
//					know when to do certain cleanup.
// 2006-04-20	JTS, RTi		Added lastTestDate, nextTestDate, and
//					wasLastTestPositive
// 2006-04-24	JTS, RTi		Added checkRealtimeData(), 
//					runInterval(), computeNextRunDate(),
//					and many methods from DataTest_Util.
// 2006-05-01	JTS, RTi		Results are no longer removed from the
//					associated actions in preTestRun().
// 2006-05-04	JTS, RTi		* Removed __dataTestStatusNum.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.TS.DayTS;
import RTi.TS.HourTS;
import RTi.TS.IrregularTS;
import RTi.TS.MinuteTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.YearTS;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormat;
import RTi.Util.Time.TimeInterval;

/**
This class represents a DataTest to be evaluated by the alarm system.
*/
public class DataTest {

/**
The delimiter used in parsing/putting together PropList strings.
*/
public static final String PROP_DELIM = ";";

/**
Used to represent true and false results in expressions.
*/
public static final int
	TRUE = 1,
	FALSE = 0;

/**
The Actions that will be notified in the case of a positive result for this 
data test.
*/
private Action[] __actions = null;

/**
Whether the test is active or not.  Active tests will not be evaluated.
*/
private boolean __isActive = false;

/**
Whether this test is a master test or not.  Master tests are typically test
that were created dynamically in TSTool but which contain time series 
identifiers with wildcards.  For every time series containing a wildcard that
is currently in memory a new data test is created.  These new data tests
are identical to the master data test, except that instead of wildcard TSIDs
they contain an actual TSID.  These new data tests are stored under the 
__dataTests Vector, but are never directly accessed by the sytem.  Instead,
the master data test is treated as a single test, although it contains an 
aggregate of many datatests with different time series.
*/
private boolean __isMaster = false;

/**
Whether the test was positive last time it was run.
*/
private boolean __wasLastTestPositive = false;

/**
The expression that is evaluated when this test is run.
*/
private DataTestExpression __expression = null;

/**
Dates defining the season during which the test will be evaluated.  If the 
test date/time is equal to or greater than active season start and less than
or equal to active season end, the test will be executed.  
*/
private DateTime 
	__activeSeasonEnd = null,
	__activeSeasonStart = null;

/**
Sets of dates that define an evaluation window.  Used to constrain the time
series data read from data sources.
*/
private DateTime
	__evaluationEnd = null,
	__evaluationStart = null;

/**
Status information for running a data test in a real-time system, like 
RiverTrak.
*/
private DateTime
	__lastTestDateTime = null,
	__nextTestDateTime = null;

/**
The base time from which to start tests.
*/
private DateTime __testBaseTime = null;

/**
Internal DateTimeFormats made from the date format strings.
*/
private DateTimeFormat
	__activeSeasonFormat = null,
	__testBaseTimeFormat = null;

/**
ID numbers for all the actions associated with this test.
*/
private int[] __actionNums = null;

/**
The ID number of the main test expression.
*/
private int __dataTestExpressionNum = DMIUtil.MISSING_INT;

/**
The ID number of the group to which this DataTest belongs.
*/
private int __dataTestGroupNum = DMIUtil.MISSING_INT;

/**
The ID number of this DataTest, which uniquely identifies it in the data test
system.
*/
private int __testNum = DMIUtil.MISSING_INT;

/**
The number of positive hits that must occur before the test is considered to
have a positive result.  See postTestRun().
*/
private int __positiveCount = DMIUtil.MISSING_INT;

/**
Additional properties.
*/
private PropList __properties = null;

/**
A description of the test.
*/
private String __description = DMIUtil.MISSING_STRING;

/**
An ID String used internally.
REVISIT (JTS - 2006-05-08)
This may not be necessary anymore.
*/
private String __id = DMIUtil.MISSING_STRING;

/**
A message that will be logged when the data test is positive.
*/
private String __message = DMIUtil.MISSING_STRING;

/**
Time intervals that define specific test settings.
*/
private TimeInterval
	__evaluationInterval = null,
	__evaluationWindow = null,
	__positiveTestInterval = null,
	__testInterval = null;

/**
If this test is a Master test (e.g., created dynamically in TSTool and 
containing wildcard time series), this Vector contains the data tests under
the main test.
*/
private Vector __dataTests = null;

/**
A Vector of the DataTestResults that were generated during a test run.  
*/
private Vector __results = new Vector();

/**
Constructor.  This constructor doesn't set up any internal data members, so
any code that calls this should be prepared to fill in all the data.
*/
public DataTest() 
throws Exception {
	this((DataTestDataModel) null);
}

/**
Constructor.
@param model the DataTestDataModel to use for populating the DataTest's data
members.
@throws Exception if not all the necessary data members were set in the 
DataTestDataModel.
*/
public DataTest(DataTestDataModel model) 
throws Exception {
	if (model == null) {
		return;
	}
	transferValuesFromDataModel(model);
	__expression = (DataTestExpression)DataTestSideFactory.getDataTestSide(
		DataTestSide.EXPRESSION, 
		getDataTestExpressionNum());	
}

/**
Copy constructor.
@param dt the DataTest to copy.
*/
public DataTest(DataTest dt) {
	if (dt.__actions != null) {
		__actions = new Action[dt.__actions.length];
		// It's okay for the array members to point to the same
		// instance data, instead of copies, here.
		for (int i = 0; i < dt.__actions.length; i++) {
			__actions[i] = dt.__actions[i];
		}
	}
	__isActive = dt.__isActive;

	__expression = DataTestSide.copyExpression(dt.__expression);
	
	if (dt.__activeSeasonEnd != null) {
		__activeSeasonEnd = new DateTime(dt.__activeSeasonEnd);
	}

	if (dt.__activeSeasonStart != null) {
		__activeSeasonStart = new DateTime(dt.__activeSeasonStart);
	}

	if (dt.__evaluationEnd != null) {
		__evaluationEnd = new DateTime(dt.__evaluationEnd);
	}

	if (dt.__evaluationStart != null) {
		__evaluationStart = new DateTime(dt.__evaluationStart);
	}

	if (dt.__testBaseTime != null) {
		__testBaseTime = new DateTime(dt.__testBaseTime);
	}

	if (dt.__activeSeasonFormat != null) {
		__activeSeasonFormat = new DateTimeFormat(
		dt.__activeSeasonFormat);
	}

	if (dt.__testBaseTimeFormat != null) {
		__testBaseTimeFormat = new DateTimeFormat(
		dt.__testBaseTimeFormat);
	}

	if (dt.__actionNums != null) {
		__actionNums = new int[dt.__actionNums.length];
		for (int i = 0; i < dt.__actionNums.length; i++) {
			__actionNums[i] = dt.__actionNums[i];
		}
	}

	__dataTestExpressionNum = dt.__dataTestExpressionNum;
	__dataTestGroupNum = dt.__dataTestGroupNum;
	__testNum = dt.__testNum;
	__positiveCount = dt.__positiveCount;
	__description = dt.__description;
	__id = dt.__id;
	__message = dt.__message;

	if (dt.__properties != null) {
		__properties = new PropList(dt.__properties);
	}

	if (dt.__evaluationInterval != null) {
		__evaluationInterval = new TimeInterval(
			dt.__evaluationInterval);
	}
	if (dt.__evaluationWindow != null) {
		__evaluationWindow = new TimeInterval(dt.__evaluationWindow);
	}
	if (dt.__positiveTestInterval != null) {
		__positiveTestInterval = new TimeInterval(
			dt.__positiveTestInterval);
	}
	if (dt.__testInterval != null) {
		__testInterval = new TimeInterval(dt.__testInterval);
	}
}

/**
Checks to see whether all the data values necessary for running a data test
in a realtime system have been set.  If any are missing, an exception is thrown.
*/
public void checkRealtimeData() 
throws Exception {
	String s = "";
	int count = 0;

	if (getTestBaseTime() == null) 	{
		s += "TestBaseTime";
		count++;
	}

	if (getTestBaseTimeFormat() == null) {
		if (count > 0) {
			s += ", ";
		}	
		s += "TestBaseTimeFormat";
		count++;
	}

	if (getEvaluationWindow() == null) {
		if (count > 0) {
			s += ", ";
		}
		s += "EvaluationWindow";
		count++;
	}

	if (count > 0) {
		throw new Exception("The following data members must be set "
			+ "if this test is to run in a real-time system: "
			+ s);
	}
}

/**
Checks the time series in a DataTest to make sure that they all have the 
same interval base and multiplier.  This method should be called after 
setTestData() has been called on the DataTest, because otherwise the time series
will not be available to check.
@return false if the time series do not have the same interval base and 
multiplier, true if they do.
*/
public boolean checkTimeSeriesForSameIntervals() {
	Vector v = expressionTreeToVector();

	DataTestFunction func = null;
	DataTestSide side = null;
	int base = -1;
	int mult = -1;
	int numTS = -1;
	int size = v.size();
	TimeInterval tempInterval = null;
	TimeInterval ti = null;
	TS ts = null;

	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}
		
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
	
		// At this point, it is known that the function has at least
		// one time series.  Use the interval base and multiplier of 
		// the function's first time series to compare against the
		// base and multiplier of the other times series in the
		// function.
		ts = func.getTS(0);
		base = ts.getDataIntervalBase();
		mult = ts.getDataIntervalMult();
		tempInterval = new TimeInterval(base, mult);

		// Iterate through any additional time series and make sure that
		// they all have the same base and multiplier (if the function
		// only has a single time series, this loop will not be 
		// entered).
		for (int j = 1; j < numTS; j++) {
			ts = func.getTS(j);
			if (base != ts.getDataIntervalBase()
			    || mult != ts.getDataIntervalMult()) {
				return false;
			}
		}

		if (ti == null) {
			// ti is null if this is the first function with 
			// time series that has been encountered.
			ti = new TimeInterval(tempInterval);
		}
		else {
			// compare against the base/mult from other functions'
			// time series.
			if (ti.getBase() != tempInterval.getBase()
			    || ti.getMultiplier() 
		               != tempInterval.getMultiplier()) {
			       	return false;
			}
		}
	}

	return true;
}

/**
Computes the value of __nextTestDateTime.  This method should be called after
runInterval() is called, with the same date/time passed to runInterval().
@param date the date at which runInterval() was called.
@return true if the next test time was computed successfully, false if not.
*/
public boolean computeNextRunDateTime(DateTime date) {
	// Calculate the next time the test should be run.  
	// Copy the passed-in date/time so that changes to it aren't seen
	// outside this method.
	DateTime next = new DateTime(date);

	// If any positive results were generated and a positive test interval
	// has been specified, use the positive test interval to determine
	// when next to run the test.

	int positiveCount = getPositiveResultsCount();
	if (positiveCount > 0 && getPositiveTestInterval() != null) {
		// There was at least one positive result, and a positive
		// test interval was set, so the positive test interval will
		// be used to determine the next time the test should be run.
		next.addInterval(getPositiveTestInterval().getBase(),
			getPositiveTestInterval().getMultiplier());
	}
	else {
		// Use the normal test interval to compute the next time when
		// the test should be run.
		next.addInterval(getTestInterval().getBase(),
			getTestInterval().getMultiplier());
	}

	DateTime testBaseTime = new DateTime(getTestBaseTime());
	DateTimeFormat format = getTestBaseTimeFormat();
	format.fillRelativeDateTime(testBaseTime, next);

	// If the next test time goes past the test's base test time, "trim"
	// the date to the test base time. 

	if (next.greaterThan(testBaseTime) 
	    && testBaseTime.greaterThan(date)) {
		setNextTestDateTime(testBaseTime);
	}
	else {
		setNextTestDateTime(next);
	}
	return true;
}

/**
Given a Vector of all the actions in the system and the internal array of IDs 
of all the actions that this data test uses, this method stores a 
reference to each of the actions used in this test.
@param actions the list of all actions in the system.
*/
public void connectActions(Vector actions) {
	if (__actionNums == null) {
		return;
	}

	Action action = null;
	int size = actions.size();
	Vector v = new Vector();

	for (int i = 0; i < size; i++) {
		action = (Action) actions.elementAt(i);
		for (int j = 0; j < __actionNums.length; j++) {
			if (action.getActionNum() == __actionNums[j]) {
				v.add(action);
			}
		}
	}

	size = v.size();
	__actions = new Action[size];
	
	for (int i = 0; i < size; i++) {
		__actions[i] = (Action) v.elementAt(i);
	}

	v.clear();
}

/**
Converts the expression tree into a Vector of DataTestSide objects, in the 
order that the expression tree nodes are traversed.  This is used for the 
numerous methods in DataTest that need to iterate through the tree, in order
to consolidate the iteration code to a single place (should changes be 
needed in the future) and also to simplify the iteration of the expression
nodes (by simply returning them in traversal order in a Vector).
@return a Vector of DataTesSide objects representing the nodes in the 
expression tree in the order they were traversed.
*/
private Vector expressionTreeToVector() {
	DataTestExpression expr = (DataTestExpression)getDataTestExpression();
	Vector v = new Vector();
	v.add(expr);
	expressionTreeToVectorHelper(v, expr);
	return v;
}

/**
Recursive function that works with expressionTreeToVector() to convert the 
expression tree into a Vector of DataTestSide objects, in the 
order that the expression tree nodes are traversed.
@param v the Vector into which nodes are placed as they are traversed.
@param a DataTestExpression from which to traverse the tree.  Its left side
nodes are traversed before its right side nodes.
*/
private void expressionTreeToVectorHelper(Vector v, DataTestExpression expr) {
	v.add(expr.getLeftSide());
	if (expr.getLeftSide().isExpression()) {
		expressionTreeToVectorHelper(v, 
			(DataTestExpression)expr.getLeftSide());
	}
	v.add(expr.getRightSide());
	if (expr.getRightSide().isExpression()) {
		expressionTreeToVectorHelper(v, 
			(DataTestExpression)expr.getRightSide());
	}
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	IOUtil.nullArray(__actions);
	__expression = null;
	__activeSeasonEnd = null;
	__activeSeasonStart = null;
	__evaluationEnd = null;
	__evaluationStart = null;
	__lastTestDateTime = null;
	__nextTestDateTime = null;
	__testBaseTime = null;
	__activeSeasonFormat = null;
	__testBaseTimeFormat = null;
	__actionNums = null;
	__description = null;
	__id = null;
	__message = null;
	__properties = null;
	__evaluationInterval = null;
	__evaluationWindow = null;
	__positiveTestInterval = null;
	__testInterval = null;
	__dataTests = null;
	__results = null;
	super.finalize();
}

/**
Returns the actions that will be called in the event of a positive result in the
test.
@return the actions that will be called in the event of a positive result in the
test.
*/
public Action[] getActions() {
	return __actions;
}

/**
Returns the IDs of the actions that will be called by this test.
This method will only return a value once setActionNums() and connectActions() 
have been called.
@return the IDs of the actions that will be called by this test.
*/
public int[] getActionNums() {
	if (__actions == null) { 
		return new int[0];
	}
	
	int size = __actions.length;
	int[] arr = new int[size];

	for (int i = 0; i < size; i++) {
		arr[i] = __actions[i].getActionNum();
	}

	return arr;
}

/**
Returns the end of the active season during which this test will be performed.
@return the end of the active season during which this test will be performed.
*/
public DateTime getActiveSeasonEnd() {
	return __activeSeasonEnd;
}

/**
Returns the format of the active season end date/time.
@return the format of the active season end date/time.
*/
public DateTimeFormat getActiveSeasonFormat() {
	return __activeSeasonFormat;
}

/**
Returns the start of the active season during which this test will be performed.
@return the start of the active season during which this test will be performed.
*/
public DateTime getActiveSeasonStart() {
	return __activeSeasonStart;
}

/**
Returns the data tests that are under this DataTest, if this test is a master.
@return the data tests that are under this DataTest, if this test is a master.
*/
public Vector getDataTests() {
	return __dataTests;
}

/**
Returns the data test expression run by this test.
@return the data test expression run by this test.
*/
public DataTestExpression getDataTestExpression() {
	return __expression;
}

/**
Returns the ID of the test's data test expression.
@return the ID of the test's data test expression.
*/
public int getDataTestExpressionNum() {
	return __dataTestExpressionNum;
}

/**
Returns the data test group number.
@return the data test group number.
*/
public int getDataTestGroupNum() {
	return __dataTestGroupNum;
}

/**
Returns the test description.
@return the test description.
*/
public String getDescription() {
	return __description;
}

/**
Returns the latest end date of all the time series in the DataTest, or null if
the DataTest contains no time series.  This should be called after test data
are set in the DataTest's functions.
@return the latest end date of all the time series in the DataTest, or null if 
the DataTest contains no time series.  This date/time is a copy of the latest
date, so it can be modified without affecting anything else.
*/
public DateTime getEndDate() {
	Vector v = expressionTreeToVector();
	
	DataTestFunction func = null;
	DataTestSide side = null;
	DateTime latestDate = null;
	int numTS = -1;
	int size = v.size();
	TS ts = null;

	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction)side;
		}
	
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
		
		for (int j = 0; j < numTS; j++) {
			ts = func.getTS(j);
			if (latestDate == null) {
				latestDate = ts.getDate2();
			}
			else if (ts.getDate2().greaterThan(latestDate)) {
				latestDate = ts.getDate2();
			}
		}
	}

	// Return a copy of the DateTime
	return new DateTime(latestDate);
}

/**
Returns the evaluation end.
@return the evaluation end.
*/
public DateTime getEvaluationEnd() {
	return __evaluationEnd;
}

/**
Returns the evaluation interval.
@return the evaluation interval.
*/
public TimeInterval getEvaluationInterval() {
	return __evaluationInterval;
}

/**
Returns the evaluation start date.
@return the evaluation start date.
*/
public DateTime getEvaluationStart() {
	return __evaluationStart;
}

/**
Returns the evaluation window.
@return the evaluation window.
*/
public TimeInterval getEvaluationWindow() {
	return __evaluationWindow;
}

/**
Returns the String ID of the test.
@return the String ID of the test.
*/
public String getID() {
	return __id;
}

/**
Returns the last time the test was run in a real-time system.
@return the last time the test was run in a real-time system.
*/
public DateTime getLastTestDateTime() {
	return __lastTestDateTime;
}

/**
Returns the next time the test will be run in a real-time system.
@return the next time the test will be run in a real-time system.
*/
public DateTime getNextTestDateTime() {
	return __nextTestDateTime;
}

/**
Returns the earliest start date of all the time series in the DataTest, or 
null if the DataTest contains no time series.  This should be called after test 
data are set in the DataTest's functions.
@return the earliest start date of all the time series in the DataTest, or null 
if the DataTest contains no time series.  This date/time is a copy of the 
earliest date, so it can be modified without affecting anything else.
*/
public DateTime getStartDate() {
	Vector v = expressionTreeToVector();
	
	DataTestFunction func = null;
	DataTestSide side = null;
	DateTime earliestDate = null;
	int numTS = -1;
	int size = v.size();
	TS ts = null;
	
	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions that have time series.
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}
	
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
		
		for (int j = 0; j < numTS; j++) {
			ts = func.getTS(j);
			if (earliestDate == null) {
				earliestDate = ts.getDate1();
			}
			else if (ts.getDate1().lessThan(earliestDate)) {
				earliestDate = ts.getDate1();
			}
		}
	}

	// Return a copy of the DateTime
	return new DateTime(earliestDate);
}

/**
Returns the ID of the test.
@return the ID of the test.
@deprecated use getDataTestNum().
*/
public int getTestNum() {
	return __testNum;
}

public int getDataTestNum() {
	return __testNum;
}

/**
Returns the associated test message.
@return the associated test message.
*/
public String getMessage() {
	return __message;
}

/**
Returns the positive count.
@return the positive count.
*/
public int getPositiveCount() {	
	return __positiveCount;
}

/**
Returns a count of the number of positive results from the last test run.
See preRun() and postRun().
@return a count of the number of positive results from the last test run.
*/
public int getPositiveResultsCount() {
	// __results will never be null.
	return __results.size();
}

/**
Returns the positive test interval.
@return the positive test interval.
*/
public TimeInterval getPositiveTestInterval() {
	return __positiveTestInterval;
}

/**
Returns the additional properties.
@return the additional properties.
*/
public PropList getProperties() {
	return __properties;
}

/**
Returns the test base time.
@return the test base time.
*/
public DateTime getTestBaseTime() {
	return __testBaseTime;
}

/**
Returns the test base time format.
@return the test base time format.
*/
public DateTimeFormat getTestBaseTimeFormat() {
	return __testBaseTimeFormat;
}

/**
Returns the test interval.
@return the test interval.
*/
public TimeInterval getTestInterval() {
	return __testInterval;
}

/**
Returns the interval base and multiplier of the time series in the DataTest.
This should be called after checkDataTestTimeSeriesForSameIntervals() has been
used to check that the base/mult are all the same, as this method will do 
<b>NO</b> validation to make sure that all the time series in the DataTest have
the same base or multiplier.
@return a TimeInterval with the DataTest interval data, or null if the 
DataTest has no time series in its equation.
*/
public TimeInterval getTimeSeriesIntervalData() {
	Vector v = expressionTreeToVector();
	
	DataTestFunction func = null;
	DataTestSide side = null;
	int base = -1;
	int mult = -1;
	int numTS = -1;
	int size = v.size();
	TS ts = null;
	
	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions that have time series.
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}
		
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
	
		// At this point, it is known that the function has at least
		// one time series.

		// Because the method has already been checked with 
		// checkDataTestTimeSeriesForSameIntervals(), the first 
		// TimeInterval that is found is returned (all the others will
		// be the same).  If the 
		// checkDataTestTimeSeriesForSameIntervals() has not been
		// called, this may not be the case and invalid data might be
		// returned.

		ts = func.getTS(0);
		base = ts.getDataIntervalBase();
		mult = ts.getDataIntervalMult();
		return new TimeInterval(base, mult);
	}

	// no functions with time series found, so return null.
	return null;
}

/**
Returns all the wildcarded TSIDs in the DataTest's functions.  This should be
used in conjunction with hasWildcards().
@return a Vector of all the wildcarded TSIDs in the DataTest.  This Vector will
never be null, even if no wildcarded TSIDs were found.
*/
public Vector getWildcardTSIDs() {
	Vector v = expressionTreeToVector();

	DataTestFunction func = null;
	DataTestSide side = null;
	int[] tsidPositions = null;
	int numTS = -1;
	int size = v.size();
	Vector tsids = new Vector();
	
	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}
		
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
	
		tsidPositions = func.getTSIDPositions();
	
		for (int j = 0; j < tsidPositions.length; j++) {
			if (func.getInputDataID(tsidPositions[j]).indexOf("*") 
			    > -1) {
			    	// Any * in a TSID means that it is a wildcard
				// TSID.
				tsids.add(
					func.getInputDataID(tsidPositions[j]));
			}
		}
	}
	
	return tsids;
}

/**
Checks a DataTest to see if any of the time series in its functions have 
wildcards.  This should be called before any test data are set in the test.
@param test the test to check for wildcards.
@return true if any wildcards were found, false if not.
*/
public boolean hasWildcards() {
	Vector v = expressionTreeToVector();
	DataTestSide side = null;
	DataTestFunction func = null;
	int[] tsidPositions = null;
	int numTS = -1;
	int size = v.size();
	for (int i = 0; i < size; i++) {
		side = (DataTestSide)v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction)side;
		}
		
		numTS = ((DataTestFunction)side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
	
		tsidPositions = func.getTSIDPositions();
	
		for (int j = 0; j < tsidPositions.length; j++) {
			if (func.getInputDataID(tsidPositions[j]).indexOf("*") 
			    > -1) {
			    	// Any * in the TSID means it is a wildcard
				// TSID.
			    	return true;
			}
		}
	}
	
	return false;
}

/**
Returns whether the test is active or not.
@return whether the test is active or not.
*/
public boolean isActive() {
	return __isActive;
}

/**
Checks whether the given date is in the active season.
@return whether the given date is in the active season or not.
*/
public boolean isInActiveSeason(DateTime dt) {
	// if no active season is defined, the date is always considered to
	// be in the active season.  
	if (__activeSeasonEnd == null || __activeSeasonStart == null) {
		return true;
	}

	// Otherwise, fill the parts of the active season dates (which are 
	// typically specified as relative dates, such as "mm-dd") that are
	// not specified in the format with the appropriate data from the
	// passed-in date time.

	// For example, if the __activeSeasonFormat was "mm-dd" (month and day)
	// __actionSeasonEnd was "03-15", and dt was "2006-07-01 13:45", 
	// once fillRelativeDateTime() is called __activeSeasonEnd would 
	// contain "2006-03-15 13:45".  For more information, see
	// DateTimeFormat.fillRelativeDateTime().
	
	__activeSeasonFormat.fillRelativeDateTime(__activeSeasonEnd, dt);
	__activeSeasonFormat.fillRelativeDateTime(__activeSeasonStart, dt);
	
	// __activeSeasonEnd may be less than __activeSeasonStart if the 
	// season wraps around a year (for instance, start could be 10-01 and
	// end could be 04-01).  iterateRelativeDateTime() will advance 
	// the end date to the next year in the above case.  See
	// DateTimeFormat.iterateRelativeDateTime() for more information.
	
	if (__activeSeasonEnd.lessThan(__activeSeasonStart)) {
		__activeSeasonFormat.iterateRelativeDateTime(__activeSeasonEnd);
	}

	if (dt.lessThanOrEqualTo(__activeSeasonEnd) 
	    && dt.greaterThanOrEqualTo(__activeSeasonStart)) {
	    	return true;
	}

	return false;
}

/**
Returns whether this is a master DataTest (see the __isMaster documentation).
@return whether this is a master DataTest.
*/
public boolean isMaster() {
	return __isMaster;
}

/**
Checks to see if the current should be run, given the specified date/time.
@param date the date/time for which to check to see if the test should be run.
@return true if the test should be run, false if not.
*/
public boolean isTimeToRun(DateTime date) {
	DateTime nextDate = getNextTestDateTime();

	// Check to see if the test is ready to be run or not.  Tests are
	// ready to run if:
	// - a NextTestDateTime has been set in them, and the current date/time
	//   is >= to the NextTestDateTime.
	// - the current date/time is >= the test's TestBaseTime.
	
	if (nextDate != null && date.greaterThanOrEqualTo(nextDate)) {
		return true;
	}
	else if (nextDate == null) {
		DateTime testBaseTime = new DateTime(getTestBaseTime());
		DateTimeFormat format = getTestBaseTimeFormat();
		format.fillRelativeDateTime(testBaseTime, date);
		if (date.greaterThanOrEqualTo(testBaseTime)) {
			return true;
		}
		else {
			return false;
		}
	}
	else {	// date < nextDate
		return false;
	}
}

/**
Performs various internal tasks that should be done every time after a test is 
run, such as:
- copying test results to actions
- setting whether the test was positive
- trimming results if the threshold number of results was not reached
*/
public void postTestRun() {
	if (__positiveCount > 1 && __results.size() < __positiveCount) {
		__results.removeAllElements();
	}
	
	if (__results != null) {
		int size = __results.size();	
		if (__actions != null) {
			// REVISIT MT 2006-10-05
			// The following code results in messages for each
			// positive test.  However, the output would be more
			// usable if only a summary result is output where
			// __positiveCount (or size) > 1!
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < __actions.length; j++) {
					__actions[j].addResults(
						(Vector) 
						__results.elementAt(i));
				}
			}
		}

		if (size > 0) {
			setWasLastTestPositive(true);
		}		
	}
}

/**
Initialization that should take place every time prior to a test being run, 
such as:
- remove all result from the interval __results Vector
- setting __wasLastTestPositive to false
*/
public void preTestRun() {
	__results.removeAllElements();
	__wasLastTestPositive = false;
}

/**
Runs the test for a single point in time.  This is called by the TSTool code
that executes a DataTest.
@param dateTime the date/time when the test is run.
@return true if the test returned a positive result, false if not.
*/
public boolean run(DateTime dt) {
	Vector results = new Vector();
	if (__expression.evaluate(dt) != FALSE) {
		// By comparing to != FALSE, it is checking for a non-zero 
		// value.  Anything == 0 is considered FALSE, anything 
		// non-zero is TRUE.
		__expression.evaluatePositiveResult(this, results, dt, 0);
		__results.add(results);
		return true;
	}
	else {
		return false;
	}
}

/**
Runs the test over an interval of time (determined by __evaluationWindow).  
Calls preRun() at the start of the method and postRun() at the end.
@param date the date/time at which to run the test.
@return true if the test ran successfully, false if not.
*/
public boolean runInterval(DateTime date) {
	preTestRun();

	// Calculate the date/time from which to run the test relative to the
	// current date/time.  The date/time to start from is the current 
	// date/time minus the evaluation window.  This is the period of data
	// that the test will check.
	DateTime runDate = new DateTime(date);
	TimeInterval interval = getEvaluationWindow();
	runDate.subtractInterval(interval.getBase(), interval.getMultiplier());

	// Re-query the test data (such as time series). 
	setTestData(runDate, date);

	// Get the interval base and multiplier from the time series being 
	// tested.
	// REVISIT SAM 2006-10-12
	// Currently all must be the same interval, but this will need to be
	// revisited.
	interval = getTimeSeriesIntervalData();

	// Iterate through the evaluation window, running the test at each
	// time step.
	runDate = trimToActiveSeasonStart(runDate);
	DateTime endDate = new DateTime(date);
	endDate = trimToActiveSeasonEnd(endDate);
	
	// Set the output precision to that of the time series so that output
	// results are to the proper precision.
	// REVISIT SAM 2006-10-12
	// This will need to be revisited if tests are allowed on time series
	// with different intervals.
	int precision = interval.getBase();
	runDate.setPrecision( precision );
	endDate.setPrecision( precision );

	while (runDate.lessThanOrEqualTo(endDate)) {
		run(runDate);
		runDate.addInterval(interval.getBase(), 
			interval.getMultiplier());
	}

	// Run post-test cleanup.  In particular, this does the processing
	// to make sure that if a certain number of positive hits are
	postTestRun();

	// REVISIT (JTS - 2006-05-09)
	// Never returns false!  What's the point of having a return? 
	// Possibly in the future it will return false?  Maybe check the
	// return value from run() (in the while{} loop above) and figure
	// something to return from that?
	return true;
}

/**
Sets the IDs numbers of the actions associated with this test.
@param actionIDs an int array of the IDs associated with this test.
*/
public void setActionNums(int[] actionIDs) {
	__actionNums = actionIDs;
}

/**
Sets the active season end.
@param activeSeasonEnd the date to set.
*/
public void setActiveSeasonEnd(DateTime activeSeasonEnd) {
	__activeSeasonEnd = activeSeasonEnd;
}

/**
Sets the active season  format.
@param activeSeasonFormat the date format to set.
*/
public void setActiveSeasonFormat(DateTimeFormat activeSeasonFormat) {
	__activeSeasonFormat = activeSeasonFormat;
}

/**
Sets the active season start.
@param activeSeasonStart the date to set.
*/
public void setActiveSeasonStart(DateTime activeSeasonStart) {
	__activeSeasonStart = activeSeasonStart;
}

/**
Sets the data tests that are under this DataTest, if this data test is a master.
@param dataTests the tests to set.
*/
public void setDataTests(Vector dataTests) {
	__dataTests = dataTests;
}

/**
Sets the main DataTestExpression.
@param expression the expression to set.
*/
public void setDataTestExpression(DataTestExpression expression) {
	__expression = expression;
}

/**
Sets the ID number of the data test expression.
@param dataTestExpressionNum the ID number to set.
*/
public void setDataTestExpressionNum(int dataTestExpressionNum) {
	__dataTestExpressionNum = dataTestExpressionNum;
}

/**
Sets the data test group num.
@param dataTestGroupNum the group num to set.
*/
public void setDataTestGroupNum(int dataTestGroupNum) {
	__dataTestGroupNum = dataTestGroupNum;
}

/**
Sets the description.
@param description the description to set.
*/
public void setDescription(String description) {
	__description = description;
}

/**
Sets the evaluation end date.
@param evaluationEnd the evalution end date.
*/
public void setEvaluationEnd(DateTime evaluationEnd) {
	__evaluationEnd = evaluationEnd;
}

/**
Sets the evaluation interval.
@param evaluationInterval the interval to set.
*/
public void setEvaluationInterval(TimeInterval evaluationInterval) {
	__evaluationInterval = evaluationInterval;
}

/**
Sets the evaluation start date.
@param evaluationStart the evaluation start date.
*/
public void setEvaluationStart(DateTime evaluationStart) {
	__evaluationStart = evaluationStart;
}

/**
Sets the evaluation window.
@param evaluationWindow the interval to set.
*/
public void setEvaluationWindow(TimeInterval evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets the String ID of the test.
@param id the ID of the test to set.
*/
public void setID(String id) {
	__id = id;
}

/**
Sets whether this is the master DataTest (see the __isMaster variable 
documentation).
@param isMaster whether this is the master DataTest.
*/
public void setIsMaster(boolean isMaster) {
	__isMaster = isMaster;
}

/**
Sets the ID number of the data test.
@param testNum the ID number to set.
@deprecated use setDataTestNum()
*/
public void setTestNum(int testNum) {
	__testNum = testNum;
}

public void setDataTestNum(int testNum) {
	__testNum = testNum;
}

/**
Sets whether the test is active.
@param isActive whether the test is active.
*/
public void setIsActive(boolean isActive) {
	__isActive = isActive;
}

/**
Sets the last time the test was run in a real-time system.
@param lastTestDateTime the last time the test was run in a real-time system.
*/
public void setLastTestDateTime(DateTime lastTestDateTime) {
	__lastTestDateTime = lastTestDateTime;
}

/**
Sets the next time the test will be run in a real-time system.
@param nextTestDateTime the next time the test will be run in a real-time 
system.
*/
public void setNextTestDateTime(DateTime nextTestDateTime) {
	__nextTestDateTime = nextTestDateTime;
}

/**
Sets the associated message.
@param message the associated message.
*/
public void setMessage(String message) {
	__message = message;
}

/**
Sets the positive count.
@param positiveCount the count to set.
*/
public void setPositiveCount(int positiveCount) {	
	__positiveCount = positiveCount;
}

/**
Sets the positive test interval.
@param positiveTestInterval the interval to set.
*/
public void setPositiveTestInterval(TimeInterval positiveTestInterval) {
	__positiveTestInterval = positiveTestInterval;
}

/**
Sets additional properties.
@param properties the additional properties to set.
*/
public void setProperties(PropList properties) {
	__properties = properties;
}

/**
Sets the test base time.
@param testBaseTime the base time to set.
*/
public void setTestBaseTime(DateTime testBaseTime) {
	__testBaseTime = testBaseTime;
}

/**
Sets the test base time format.
@param testBaseTime the base time to set.
*/
public void setTestBaseTimeFormat(DateTimeFormat testBaseTimeFormat) {
	__testBaseTimeFormat = testBaseTimeFormat;
}

/**
Calls setTestData on every function in the DataTest.  This method is necessary
when DataTests are created on the fly.  
*/
public void setTestData() {
	setTestData(null, null);
}

/**
Calls setTestData on every function in the DataTest.  This method is necessary
when DataTests are created on the fly.  
*/
public void setTestData(DateTime startDate, DateTime endDate) {
	Vector v = expressionTreeToVector();

	DataTestFunction func = null;
	DataTestSide side = null;
	int size = v.size();

	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}

		func.setTestData(startDate, endDate);
	}
}

/**
Sets the test interval.
@param testInterval the interval to set.
*/
public void setTestInterval(TimeInterval testInterval) {
	__testInterval = testInterval;
}

/**
Sets whether the test was positive last time it was run in a real-time
system.
@param wasLastTestPositive whether the test was positive last time it was run
in a real-time system.
*/
public void setWasLastTestPositive(boolean wasLastTestPositive) {
	__wasLastTestPositive = wasLastTestPositive;
}

/**
Sets all the wildcards TSIDs in the DataTest to the specified tsid.  This should
be called before test data are set in the DataTest.  <p>
<b>Note:</b><p>
An assumption of the system is that if a DataTest's expression has wildcard
TSIDs in it, they are all the same.  There is no support for an expression like:
<p>
   GetTSValue(A*) &gt; 100 || GetTSValue(B*) &lt; 30<p>
@param tsid the tsid to change the wildcards to.  
*/
public void setWildcards(String tsid) {
	Vector v = expressionTreeToVector();

	DataTestFunction func = null;
	DataTestSide side = null;
	int[] tsidPositions = null;
	int numTS = -1;
	int size = v.size();
	
	for (int i = 0; i < size; i++) {
		side = (DataTestSide) v.elementAt(i);
		if (side.isExpression()) {
			// only have to check Functions
			continue;
		}
		else {
			func = (DataTestFunction) side;
		}
		
		numTS = ((DataTestFunction) side).getNumTS();
		if (numTS <= 0) {
			// only have to check Functions that have time series.
			continue;
		}
	
		tsidPositions = func.getTSIDPositions();
	
		for (int j = 0; j < tsidPositions.length; j++) {
			if (func.getInputDataID(tsidPositions[j]).indexOf("*") 
			    > -1) {
			    	// Any * means the TSID is a wildcard TSID
				func.setInputDataID(tsid, tsidPositions[j]);
			}
		}
	}
}

/**
Returns a DataTestDataModel filled with the values in this object.
@return a DataTestDataModel filled with the values in this object.
*/
public DataTestDataModel toDataModel() {
	DataTestDataModel model = new DataTestDataModel();
	if (getActiveSeasonFormat() != null) {
		model.setActiveSeasonEnd(
			getActiveSeasonFormat().format(getActiveSeasonEnd()));
		model.setActiveSeasonStart(
			getActiveSeasonFormat().format(getActiveSeasonStart()));
	}
	model.setActiveSeasonFormat(getActiveSeasonFormat());
	model.setActionNums(getActionNums());
	model.setDataTestExpressionNum(getDataTestExpressionNum());
	model.setDataTestGroupNum(getDataTestGroupNum());
	model.setDescription(getDescription());
	model.setEvaluationEnd(getEvaluationEnd());
	model.setEvaluationStart(getEvaluationStart());
	model.setEvaluationInterval(getEvaluationInterval());
	model.setEvaluationWindow(getEvaluationWindow());
	model.setDataTestNum(getDataTestNum());
	model.setIsActive(isActive());
	model.setMessage(getMessage());
	model.setPositiveCount(getPositiveCount());
	model.setPositiveTestInterval(getPositiveTestInterval());
	model.setProperties(getProperties());
	if (getTestBaseTimeFormat() != null) {
		model.setTestBaseTime(getTestBaseTimeFormat().format(
			getTestBaseTime()));
	}
	model.setTestBaseTimeFormat(getTestBaseTimeFormat());
	model.setTestInterval(getTestInterval());
	model.setLastTestDateTime(getLastTestDateTime());
	model.setNextTestDateTime(getNextTestDateTime());
	model.setWasLastTestPositive(wasLastTestPositive());
	return model;
}

/**
Returns a string representation of the function data.
@return a string representation of the function data.
*/
public String toString() {
	String s = "Test Num: " + __testNum + "\n"
		+ "Expression Num: " + __dataTestExpressionNum + "\n"
		+ "Group Num: " + __dataTestGroupNum + "\n"
		+ "Active Season Start: '" + __activeSeasonStart + "'\n"
		+ "Active Season End: '" + __activeSeasonEnd + "'\n"
		+ "Description: '" + __description + "'\n"
		+ "IsActive: " + __isActive + "\n"
		+ "Message: '" + __message + "'\n";
	if (__properties != null) {
		s += "Properties: '" + __properties.toString(PROP_DELIM) + "\n";
	}
	else {
		s += "Properties: '[null]'\n";
	}
	if (__actionNums != null) {
		s += "Actions: \n";
		for (int i = 0; i < __actionNums.length; i++) {
			s += "  " + __actionNums[i] + "\n";
		}
	}
	else {
		s += "Actions: [NULL]\n";
	}
	s += "Positive Count: " + __positiveCount + "\n"
		+ "Evaluation Interval: " + __evaluationInterval + "\n"
		+ "Evaluation Window: " + __evaluationWindow + "\n"
		+ "Positive Test Interval: " + __positiveTestInterval + "\n"
		+ "Test Base Time: " + __testBaseTime + "\n"
		+ "Test Interval: " + __testInterval + "\n";
	return s;
}

/**
Transfers data from the data model into the object.
@param model the model from which to transfer data.
@throws Exception if some of the necessary data values were not set in the
data model.
*/
public void transferValuesFromDataModel(DataTestDataModel model)
throws Exception {
	if (!model.checkAllSet()) {
		throw new Exception("Not all data values were read in and "
			+ "set in the data model: "
			+ model.getMissingValues());
	}
	setActionNums(model.getActionNums());
	setActiveSeasonFormat(model.getActiveSeasonFormat());
	if (getActiveSeasonFormat() != null) {
		setActiveSeasonEnd(getActiveSeasonFormat().parse(
			model.getActiveSeasonEndString()));
		setActiveSeasonStart(getActiveSeasonFormat().parse(
			model.getActiveSeasonStartString()));
	}
	setDataTestExpressionNum(model.getDataTestExpressionNum());
	setDataTestGroupNum(model.getDataTestGroupNum());
	setDescription(model.getDescription());
	setEvaluationEnd(model.getEvaluationEnd());
	setEvaluationStart(model.getEvaluationStart());
	setEvaluationInterval(model.getEvaluationInterval());
	setEvaluationWindow(model.getEvaluationWindow());
	setDataTestNum(model.getDataTestNum());
	setIsActive(model.isActive());
	setMessage(model.getMessage());
	setPositiveCount(model.getPositiveCount());
	setPositiveTestInterval(model.getPositiveTestInterval());
	setProperties(model.getProperties());
	setTestBaseTimeFormat(model.getTestBaseTimeFormat());
	if (getTestBaseTimeFormat() != null) {
		setTestBaseTime(getTestBaseTimeFormat().parse(
			model.getTestBaseTimeString()));
	}
	setTestInterval(model.getTestInterval());
}

/**
Given a date, it compares that date to the activeSeasonEnd date, and if the
given date is less than or equal to the activeSeasonEnd, the passed-in date is
returned.  Otherwise, the activeSeasonEnd date is returned.
@return the activeSeasonEnd date or the passed-in date.
*/
public DateTime trimToActiveSeasonEnd(DateTime date) {
	if (__activeSeasonEnd == null) {
		return date;
	}
	__activeSeasonFormat.fillRelativeDateTime(__activeSeasonEnd, date);
	if (__activeSeasonEnd.greaterThanOrEqualTo(date)) {
		return date;
	}
	else {
		return new DateTime(__activeSeasonEnd);
	}
}

/**
Given a date, it compares that date to the activeSeasonStart date, and if the
given date is greater than or equal to the activeSeasonStart, 
the passed-in date is returned.  Otherwise, the activeSeasonStart date 
is returned.
@return the activeSeasonStart date or the passed-in date.
*/
public DateTime trimToActiveSeasonStart(DateTime date) {
	if (__activeSeasonStart == null) {
		return date;
	}
	__activeSeasonFormat.fillRelativeDateTime(__activeSeasonStart, date);
	if (__activeSeasonStart.lessThanOrEqualTo(date)) {
		return date;
	}
	else {
		return new DateTime(__activeSeasonStart);
	}
}

/**
Returns whether the test was positive last time it was run in a real-time
system.
@return whether the test was positive last time it was run in a real-time
system.
*/
public boolean wasLastTestPositive() {
	return __wasLastTestPositive;
}

/**
Writes this object to the IO interface.
@param io the AlertIOInterface to use for writing.
@return the ID number of the test that was written, or -1 if the test 
could not be written successfully.
*/
public int write(AlertIOInterface io) 
throws Exception {
	int exprNum = writeExpressionTree(io, getDataTestExpression());
	DataTestDataModel model = toDataModel();
	if (DMIUtil.isMissing(getDataTestExpressionNum())) {
		model.setDataTestExpressionNum(exprNum);
	}
	int idNum = io.writeDataTestDataModel(model);
	return idNum;
}


/**
Used in conjunction with write(), this writes the expression tree starting from
__expression on down.  Every function and expression under __expression is
written to the persistent data source.  This is a recursive function.
@param io the AlertIOInterface to use for writing the data.
@param side the DataTestSide (either an expression or a function) to be written.
@throws Exception if an error occurs writing the DataTestSide.
@return the ID number of the DataTestSide that was written.
*/
private int writeExpressionTree(AlertIOInterface io, DataTestSide side) 
throws Exception {
	int idNum = -1;
	if (side.isExpression()) {
		DataTestExpression expr = (DataTestExpression)side;
		int idLeft = writeExpressionTree(io, expr.getLeftSide());
		int idRight = writeExpressionTree(io, expr.getRightSide());
		expr.setLeftSideIDNum(idLeft);
		expr.setRightSideIDNum(idRight);
		idNum = io.writeDataTestExpressionDataModel(expr.toDataModel());
	}
	else {
		DataTestFunction func = (DataTestFunction)side;
		idNum = io.writeDataTestFunctionDataModel(func.toDataModel());
	}

	return idNum;
}

////////////////////////////////////////////////////////////////////////////////
// Code below this point is in development.                                   //
////////////////////////////////////////////////////////////////////////////////

/**
Test code -- does not work properly at this time.
REVISIT (JTS - 2006-05-04)
Get to work properly in the future!  Might possibly be of some use, but not 
in the immediate future.
*/
public TS resultsToTimeSeries() 
throws Exception {
	if (__results == null || __results.size() == 0) {
		return null;
	}

	int size = __results.size();

	TimeInterval interval = getTimeSeriesIntervalData();
	int base = interval.getBase();
	TS ts = null;
	if (base == TimeInterval.MINUTE) {
		ts = new MinuteTS();
	}
	else if (base == TimeInterval.HOUR) {
		ts = new HourTS();
	}
	else if (base == TimeInterval.DAY) {
		ts = new DayTS();
	}
	else if (base == TimeInterval.MONTH) {
		ts = new MonthTS();
	}
	else if (base == TimeInterval.YEAR) {
		ts = new YearTS();
	}
	else if (base == TimeInterval.IRREGULAR) {
		ts = new IrregularTS();
	}
	else {
		throw new Exception("Unsupported base: " + base);
	}	
	ts.setDataInterval(base, interval.getMultiplier());

	Vector v1 = (Vector) __results.elementAt(0);
	Vector v2 = (Vector) __results.elementAt(size - 1);

	DataTestResult r1 = (DataTestResult) v1.elementAt(0);
	DateTime dt1 = r1.getTestTime();
	DataTestResult r2 = (DataTestResult) v2.elementAt(0);
	DateTime dt2 = r2.getTestTime();
	
	ts.setDate1(dt1);
	ts.setDate2(dt2);
	ts.allocateDataSpace();

	Vector v = null;
	DataTestResult r = null;
	for (int i = 0; i < size; i++) {
		v = (Vector) __results.elementAt(i);
		r = (DataTestResult) v.elementAt(0);
		ts.setDataValue(r.getTestTime(), 1);
	}
	
	return ts;
}

public void computeStartingDateTime(DateTime date) {
	DateTime testBaseTime = new DateTime(getTestBaseTime());
	DateTimeFormat format = getTestBaseTimeFormat();
	format.fillRelativeDateTime(testBaseTime, date);

	if (date.lessThan(testBaseTime)) {
		// the date passed in to this function is during the period
		// of the day when the test does not run.  The test will 
		// start at the test base time.
		setNextTestDateTime(testBaseTime);
	}
	else {
		while (testBaseTime.lessThan(date)) {
			testBaseTime.addInterval(
				__testInterval.getBase(),
				__testInterval.getMultiplier());
		}

		setNextTestDateTime(testBaseTime);
	}
}

}
