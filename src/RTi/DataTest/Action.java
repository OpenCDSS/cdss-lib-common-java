// ----------------------------------------------------------------------------
// Action - the abstract base class for all Actions.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-21	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-14	JTS, RTi		Added versions of logAction() and 
//					runAction() that take a DataTest
//					parameter.
// 2006-05-03	JTS, RTi		* Removed AlertIOInterface and related
//					  set/get methods.
// 					* The AlertIOInterface is now passed 
//					  into write(), rather than being 
//       				  stored in the class.
//					* write() now returns the ID number of
//					  the object that was written, rather 
//					  than a boolean.
// 2006-07-19	Michael Thiemann, RTi	* Added public static final int
//                                        TYPE_SYSTEMCALL
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.List;
import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.PropList;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormat;
import RTi.Util.Time.TimeInterval;

import RTi.Util.Message.Message;

/**
This class is the abstract base class for all actions.  An action is a step
taken by the system is response to a positive data test.  Actions can range
from notification of a group of people, to messaging a user, to logging, etc.
*/
public abstract class Action {

/**
The types of actions supported by the DataTest system.
*/
public static final int
	TYPE_UNKNOWN = -1,
	TYPE_DUMMY = 1,
	TYPE_MESSAGEBOARD = 2,
	TYPE_SYSTEMCALL = 3;

/**
Whether the action is active or not.  Inactive actions will not be performed,
even if a positive data test tries to run it.
*/
private boolean __isActive = false;

/**
The base time at which the action is performed.  This value is used with 
the evaluation interval to determine the times at which the action is performed.
Typically, this is a value such as "00:30", meaning that no actions will be run
between 00:00 and 00:29.
*/
private DateTime __actionBaseTime = null;

/**
The date/time when the Action will be run next.
*/
private DateTime __nextActionDateTime = null;

/**
The format of the action base time (e.g., "hh:MM")
*/
private DateTimeFormat __actionBaseTimeFormat = null;

/**
The ID number of the action, used internally to uniquely identify the action.
*/
private int __actionNum = DMIUtil.MISSING_INT;

/**
The type of action this action is (e.g., TYPE_DUMMY).
*/
private int __actionType = DMIUtil.MISSING_INT;

/**
The level of results that the action will use for generating messages.  When
DataTestResults are put into this Action (via addResults()), results with 
levels greater than this value will not be added to the Action's internal
result array.  See DataTestResult for more information on result levels.
*/
private int __resultLevel = 0;

/**
The severity of this action.
REVISIT (JTS - 2006-03-23)
currently unused.  Come back once severity levels are fleshed out more.  
*/
private int __severityType = DMIUtil.MISSING_INT;

/**
Additional properties for the Action.
*/
private PropList __properties = null;

private Severity __severity = null;

/**
A description of this action.
*/
private String __description = DMIUtil.MISSING_STRING;

/**
The name of this action (e.g., "Dummy").
*/
private String __actionName = DMIUtil.MISSING_STRING;

/**
The interval between when the action is performed and when it can be performed
again.
*/
private TimeInterval __actionInterval = null;

/**
The range of time prior to the action call for which the action will process
data results.  That is, if there are results for 2 weeks' worth of data positive
data tests, but this interval is set to 1 week, this action will only be called
on the last week of data.
REVISIT (JTS - 2006-05-03)
Now that results are put into the action AFTER the test is done running, 
instead of as a test is run, the evaluation Window needs re-evaluated.  How
to make sure that older results are maintained, or is it even necessary?
*/
private TimeInterval __evaluationWindow = null;

/**
A Vector of the data test results from positive tests.  This Vector will never
be null.  This is a Vector of Vectors of DataTestResults.
*/
private List __positiveResults = new Vector();

/**
Empty constructor.  Data members must be set before this action can be used.
*/
public Action() 
throws Exception {
	this(null);
}

/**
Constructor.  
@param model the model that contains the data values.
*/
public Action(ActionDataModel model) 
throws Exception {
	if (model == null) {
		// allow null models, but the data members must be placed
		// in the Action elsewhere.
		return;
	}
	transferValuesFromDataModel(model);
}

/**
Adds positive test results to the internal Vector of positive test results.
@param resultVector the result Vector to add to the internal Vector.  Only
test results in this Vector that have a result level equal to or less than
the internal __resultLevel member variable will be added.
*/
public void addResults(List results) {
	boolean missing = DMIUtil.isMissing(__resultLevel);
	DataTestResult result = null;
	int size = results.size();
	List v = new Vector();
	
	for (int i = 0; i < size; i++) {
		result = (DataTestResult) results.get(i);
		if (missing || result.getLevel() <= __resultLevel) {
			v.add(result);
		}
	}

	if (v.size() > 0) {
		__positiveResults.add(v);
	}
}

/**
Checks to see if there are any positive test results set in this action.  If
true, the action should be executed (via runAction()).
@return true if there are any positive test results sets in this action.
*/
public boolean checkForPositiveDataTests() {
	if (__positiveResults.size() > 0) {
		return true;
	}
	else {
		return false;
	}
}

/**
Clears the internal Vector of positive test results.  
*/
public void clearPositiveResults() {
	__positiveResults.clear();
}

/**
Computes the next date/time at which the action will be run, and stores it 
internally in __nextActionDateTime.
@param dateTime the date/time at which the action is currently being run.
@return true.
*/
public boolean computeNextRunDateTime(DateTime dateTime) {
	// Calculate the next time the action should be run.  
	// Create a copy of the passed-in date/time so that changes to its
	// values are not felt outside the method.
	DateTime next = new DateTime(dateTime);

	// If any positive results were generated and a positive action interval
	// has been specified, use the positive action interval to determine
	// when next to run the action.

	next.addInterval(getActionInterval().getBase(),
		getActionInterval().getMultiplier());

	DateTime actionBaseTime = new DateTime(getActionBaseTime());
	DateTimeFormat format = getActionBaseTimeFormat();
	format.fillRelativeDateTime(actionBaseTime, next);

	// If the next action time goes past the action's base action time, 
	// "trim" the dateTime to the action base time. 

	if (next.greaterThan(actionBaseTime) 
	    && actionBaseTime.greaterThan(dateTime)) {
		setNextActionDateTime(actionBaseTime);
	}
	else {
		setNextActionDateTime(next);
	}
	return true;
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__actionBaseTime = null;
	__actionBaseTimeFormat = null;
	__description = null;
	__actionName = null;
	__properties = null;
	__actionInterval = null;
	__evaluationWindow = null;
	__positiveResults = null;
	__severity = null;

	super.finalize();
}

/**
Returns the action name given the action type (e.g., TYPE_DUMMY).  
Null will be returned if an invalid type was passed in.
@return the action name given the action type.
*/
public static String getActionName(int type) {
	if (type == TYPE_DUMMY) {
		return "dummy";
	}
	else if (type == TYPE_MESSAGEBOARD) {
		return "messageBoard";
	}
	else if (type == TYPE_SYSTEMCALL) {
		return "SystemCall";
	}
	else {
		return null;
	}
}

/**
Returns the action type given the action name (e.g., "dummy").  
TYPE_UNKNOWN will be returned if an unknown name was passed in.
@return the action type given the action name.
*/
public static int getActionType(String name) {
	
	int type;

	if (name.equalsIgnoreCase("dummy")) {
		type =  TYPE_DUMMY;
	}
	else if (name.equalsIgnoreCase("messageBoard")) {
		type =   TYPE_MESSAGEBOARD;
	}
	else if (name.equalsIgnoreCase("SystemCall")) {
		type =   TYPE_SYSTEMCALL;
	}
	else {
		type =   TYPE_UNKNOWN;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, "Action.getActionType", "Action type is /" + name + "/; number is " + type);
	}
		
	return type;
}

/**
Returns the base time at which the action is set to run.  
@return the base time at which the action is set to run.
*/
public DateTime getActionBaseTime() {
	return __actionBaseTime;
}

/**
Returns the format of the action base time (e.g., "hh:MM")
@return the format of the action base time (e.g., "hh:MM")
*/
public DateTimeFormat getActionBaseTimeFormat() {
	return __actionBaseTimeFormat;
}

/**
Returns the interval between when the action ran last and when it can be
run again.
@return the interval between when the action ran last and when it can be
run again.
*/
public TimeInterval getActionInterval() {
	return __actionInterval;
}

/**
Returns the action name.
@return the action name.
*/
public String getActionName() {
	return __actionName;
}

/**
Returns the action id number.
@return the action id number.
*/
public int getActionNum() {
	return __actionNum;
}

/**
Returns the action type.
@return the action type.
*/
public int getActionType() {
	return __actionType;
}

/**
Returns the description of the action.
@return the description of the action.
*/
public String getDescription() {
	return __description;
}

/**
Returns the interval of time for which the action will analyze positive test 
results.
@return the interval of time for which the action will analyze positive test
results.
*/
public TimeInterval getEvaluationWindow() {
	return __evaluationWindow;
}

/**
Returns the next date/time at which the action will be run.
@return the next date/time at which the action will be run.
*/
public DateTime getNextActionDateTime() {
	return __nextActionDateTime;
}

/**
Returns the positive test results.
@return the positive test results.
*/
public List getPositiveResults() {
	return getPositiveResults(null);
}

/**
Returns the positive test results for the given DataTest.  This is typically
used in the runAction() method of classes that extend Action.
@param test the DataTest for which to return results.  If null, the results
for all DataTests will be returned.
@return the positive test results.  This Vector will never be null.
*/
public List getPositiveResults(DataTest test) {
	if (test == null) {
		return __positiveResults;
	}

	DataTestResult result = null;
	int size = getPositiveResultsCount();
	int size2 = 0;
	List ret = new Vector();
	List v = null;

	for (int i = 0; i < size; i++) {
		v = (List) __positiveResults.get(i);
		size2 = v.size();
		for (int j = 0; j < size2; j++) {
			result = (DataTestResult) v.get(j);
			if (result.getDataTest() == test) {
				ret.add(v);
			}
			break;
		}
	}

	return ret;
}

/**
Returns the count of positive test results.
@return the count of positive test results.
*/
public int getPositiveResultsCount() {
	// __positiveResults will never null.
	return __positiveResults.size();
}

/**
Returns the additional properties.
@return the additional properties.
*/
public PropList getProperties() {
	return __properties;
}

/**
Returns the level of results for which the action will process results.
@return the level of results for which the action will process results.
*/
public int getResultLevel() {
	return __resultLevel;
}

/**
Returns the action severity.
@return the action severity.
*/
public int getSeverityType() {
	return __severityType;
}

/**
Returns whether this action is active or not.
@return whether this action is active or not.
*/
public boolean isActive() {
	return __isActive;
}

/**
Returns whether it is time to run this action or not.
@param dateTime the time to check against.
@return true if it is time to run this action or false if not.
*/
public boolean isTimeToRun(DateTime date) {
	DateTime nextDate = getNextActionDateTime();

	// Check to see if the action is ready to be run or not.  Actions are
	// ready to run if:
	// - a NextActionDateTime has been set in them, and the current 
	//   date/time is >= to the NextActionDateTime.
	// - the current date/time is >= the action's ActionBaseTime.
	
	if (nextDate != null && date.greaterThanOrEqualTo(nextDate)) {
		// - A NextActionDateTime has been set
		// - The current date at which the action is being checked
		//   is greater than or equal to the next run date.
		return true;
	}
	else if (nextDate == null) {
		// No next date has been set, so need to compare to the
		// action base time.
		DateTime actionBaseTime = new DateTime(getActionBaseTime());
		DateTimeFormat format = getActionBaseTimeFormat();
		format.fillRelativeDateTime(actionBaseTime, date);
		if (date.greaterThanOrEqualTo(actionBaseTime)) {
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
Logs this action.
REVISIT (JTS - 2006-03-22)
once logging is in the system, flesh out this description!
@return true if the action was logged successfully, false if not.
*/
public boolean logAction() {
	return logAction(null);
}

/**
Logs this action.
REVISIT (JTS - 2006-03-22)
once logging is in the system, flesh out this description!
@param test the DataTest for which to log the results.
@return true if the action was logged successfully, false if not.
*/
public abstract boolean logAction(DataTest test);

/** 
Removes old results that are past the action's evaluation window, and so which
do not need to be processed by the Action anymore.
@param dateTime the date/time at which the action is to be run.  All results 
stored in this action which are older than (dateTime - evaluationWindow) will be
removed.
*/
public void removeOldResults(DateTime dateTime) {
	if (dateTime == null || __evaluationWindow == null) {
		return;
	}

	// create a copy of the passed-in date/time so that changes to it are
	// not seen outside this method.
	DateTime dt = new DateTime(dateTime);

	dt.subtractInterval(__evaluationWindow.getBase(), 
		__evaluationWindow.getMultiplier());
	
	DataTestResult result = null;
	int size1 = __positiveResults.size();
	List v = null;

	for (int i = (size1 - 1); i >= 0; i--) {
		v = (List) __positiveResults.get(i);
		for (int j = 0; j < 1; j++) {
			// check just the first element in the Vector.
			result = (DataTestResult) v.get(j);
			if (dt.greaterThanOrEqualTo(result.getTestTime())) {
				__positiveResults.remove(i);
			}
		}
	}
}

/**
Runs the action.
@return true if the action was successful, false if not.
*/
public boolean runAction() {
	return runAction(null, null);
}

/**
Runs the action.
@param runDateTime the time at which the action is being run.  Used to remove
old results (see removeOldResults()) from the results Vector.  If null, no
old results will be removed.
@param test the DataTest for which to run the results.  If null, the action
will run for all DataTest.
@return true if the action was successful, false if not.
*/
public abstract boolean runAction(DateTime runDateTime, DataTest test);

/**
Sets the base time at which the action will be run.
@param actionBaseTime the action base time to set.
*/
public void setActionBaseTime(DateTime actionBaseTime) {
	__actionBaseTime = actionBaseTime;
}

/**
Sets the format of the action base time (e.g., "hh:MM").
@param actionBaseTimeFormat the action base time format to set.
*/
public void setActionBaseTimeFormat(DateTimeFormat actionBaseTimeFormat) {
	__actionBaseTimeFormat = actionBaseTimeFormat;
}

/**
Sets the interval between action runs.
@param actionInterval the action interval to set.
*/
public void setActionInterval(TimeInterval actionInterval) {
	__actionInterval = actionInterval;
}

/**
Sets the action name.
@param name the name of the action.
*/
public void setActionName(String name) {
	__actionName = name;
}

/**
Sets the action ID number.
@param actionNum the action ID number to set.
*/
public void setActionNum(int actionNum) {
	__actionNum = actionNum;
}

/**
Sets the action type.
@param actionType the action type to set.
*/
public void setActionType(int actionType) {
	__actionType = actionType;
}

/**
Sets the description.
@param description the description to set.
*/
public void setDescription(String description) {
	__description = description;
}

/**
Sets the window of dates of results that the action will analyze.
@param evaluationWindow the evaluation window to set.
*/
public void setEvaluationWindow(TimeInterval evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets whether the action is active or not.
@param isActive whether the action is active or not.
*/
public void setIsActive(boolean isActive) {
	__isActive = isActive;
}

/**
Sets the next date/time at which the action will be run.
@param dateTime the next date/time at which the action will be run.
*/
public void setNextActionDateTime(DateTime dateTime) {
	__nextActionDateTime = dateTime;
}

/**
Sets the additional properties.
@param properties the additional properties to set.
*/
public void setProperties(PropList properties) {
	__properties = properties;
}

/**
Sets the result level.  Any results above the given level will be ignored when
the Vector of DataTestResults is passed to this action following a positive
data test.  See DataTestResult for more information about result levels.
@param resultLevel the result level to set.
*/
public void setResultLevel(int resultLevel) {
	__resultLevel = resultLevel;
}

/**
Sets the action severity.
@param severity the action severity to set.
*/
public void setSeverityType(int severity) {
	__severityType = severity;
}

/**
Returns a DataTestDataModel filled with the values in this object.
@return a DataTestDataModel filled with the values in this object.
*/
public ActionDataModel toDataModel() {
	ActionDataModel model = new ActionDataModel();
	model.setActionBaseTimeFormat(getActionBaseTimeFormat());
	if (getActionBaseTimeFormat() != null) {
		model.setActionBaseTime(getActionBaseTimeFormat().format(
			getActionBaseTime()));
	}
	model.setActionInterval(getActionInterval());
	model.setActionNum(getActionNum());
	model.setActionType(getActionType());
	model.setDescription(getDescription());
	model.setEvaluationWindow(getEvaluationWindow());
	model.setActionName(getActionName());
	model.setResultLevel(getResultLevel());
	model.setSeverityType(getSeverityType());
	model.setIsActive(isActive());
	model.setProperties(getProperties());
	return model;
}

/**
Returns a string representation of the function data.
@return a string representation of the function data.
*/
public String toString() {
	String s = "Action Num: " + __actionNum + "\n"
		+ "Result Level: " + __resultLevel + "\n"
		+ "Action Base Time: " + __actionBaseTime + "\n"
		+ "Action Type: " + __actionType + "\n"
		+ "Description: '" + __description + "'\n"
		+ "Name: '" + __actionName + "'\n"
		+ "Severity : " + __severity + "\n"
		+ "Action Interval: " + __actionInterval + "\n"
		+ "Evaluation Window: " + __evaluationWindow + "\n"
		+ "Properties: '" + __properties + "'\n"
		+ "Subcribed Data Tests: ";
	return s;
}

/**
Transfers data from the data model into the object.
@param model the model from which to transfer data.
@throws Exception if some of the necessary data values were not set in the
data model.
*/
public void transferValuesFromDataModel(ActionDataModel model)
throws Exception {
	if (!model.checkAllSet()) {
		throw new Exception("Not all data values were read in and "
			+ "set in the data model: "
			+ model.getMissingValues());
	}
	setActionBaseTimeFormat(model.getActionBaseTimeFormat());
	if (getActionBaseTimeFormat() != null) {
		setActionBaseTime(getActionBaseTimeFormat().parse(
			model.getActionBaseTimeString()));
	}
	setActionInterval(model.getActionInterval());
	setActionNum(model.getActionNum());
	setActionType(model.getActionType());
	setDescription(model.getDescription());
	setEvaluationWindow(model.getEvaluationWindow());
	setActionName(model.getActionName());
	setResultLevel(model.getResultLevel());
	setSeverityType(model.getSeverityType());
	setIsActive(model.isActive());
	setProperties(model.getProperties());
}

/**
Writes the action to the persistent data source.
@param io the AlertIOInterface to use for writing.
@return the ID number of the action that was written, or -1 if the Action was
not successfully written.
*/
public int write(AlertIOInterface io) 
throws Exception {
	return io.writeActionDataModel(toDataModel());
}

public Severity getSeverity() {
	return __severity;
}

public void setSeverity(Severity severity) {
	__severity = severity;
}

public void connectSeverity(List severityVector) {
	if (severityVector == null) {
		return;
	}

	int size = severityVector.size();
	Severity s = null;

	for (int i = 0; i < size; i++) {
		s = (Severity)severityVector.get(i);
		if (s.getSeverityType() == __severityType) {
			__severity = s;
			return;
		}
	}
}

public void computeStartingDateTime(DateTime date) {
	DateTime actionBaseTime = new DateTime(getActionBaseTime());
	DateTimeFormat format = getActionBaseTimeFormat();
	format.fillRelativeDateTime(actionBaseTime, date);

	if (date.lessThan(actionBaseTime)) {
		// the date passed in to this function is during the period
		// of the day when the test does not run.  The test will 
		// start at the test base time.
		
		setNextActionDateTime(actionBaseTime);
	}
	else {
		while (actionBaseTime.lessThan(date)) {
			actionBaseTime.addInterval(
				__actionInterval.getBase(),
				__actionInterval.getMultiplier());
		}

		setNextActionDateTime(actionBaseTime);
	}
}

}
