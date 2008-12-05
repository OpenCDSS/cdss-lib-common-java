// ----------------------------------------------------------------------------
// DataTestDataModel - class that holds data for a when the data is read
//	from a data source.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-20	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-20	JTS, RTi		Added lastTestDate, nextTestDate, and
//					wasLastTestPositive
// 2006-05-04	JTS, RTi		* Removed __dataTestStatusNum.
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

/**
This class represents a DataTest to be evaluated by the alarm system.
<p>
<b>Note on DataModel classes:</b><p>
In a data model class, there are set methods that take two types of 
parameters (e.g., setEvaluationInterval(), which takes either a String
or a TimeInterval), and there are get methods that return either a 
core Java data type or a more complicated object (e.g., 
getTestBaseTime()/getTestBaseTimeString(), which return a DateTime
or a String, respectively).  <p>
The reason for this is that DataModels are the link between the full 
data object (e.g., DataTest) and the data as it is represented in the
persistent data source.  Because the persistent data source could be a
database, an XML file, a web data source, a text file, or anything else, 
the data have to be able to be stored in a generic way, as base Java types
(String, int, double, etc). <p>
DataModels, then, are the bridging classes that translate data from the 
generic storage data to the more complicated data (e.g, DateTime) needed 
by the full data objects, and also do the reverse, taking the complicated 
data objects and translating them back into base Java data types.
*/
public class DataTestDataModel {

/**
Whether the test is active or not.
*/
private boolean __isActive = false;

/**
Whether the test was positive last time it was run (see __lastTestDateTime).
*/
private boolean __wasLastTestPositive = false;

/**
The ID number of the main test expression.
*/
private int __dataTestExpressionNum = DMIUtil.MISSING_INT;

/**
The ID number of the group to which the data test belongs.
*/
private int __dataTestGroupNum = DMIUtil.MISSING_INT;

/**
The ID number of this DataTest.
*/
private int __testNum = DMIUtil.MISSING_INT;

/**
The number of positive hits that must occur before actions are taken.
*/
private int __positiveCount = DMIUtil.MISSING_INT;

/**
The actions that are tied to this data test.
*/
private int[] __actionNums = null;

/**
String representations of the active season begin and end.
*/
private String
	__activeSeasonEnd = DMIUtil.MISSING_STRING,
	__activeSeasonStart = DMIUtil.MISSING_STRING,
	__activeSeasonFormat = DMIUtil.MISSING_STRING;

/**
A description of the test.
*/
private String __description = DMIUtil.MISSING_STRING;

/**
The evaluation period start and end date.
*/
private String 
	__evaluationEnd = DMIUtil.MISSING_STRING,
	__evaluationStart = DMIUtil.MISSING_STRING;

/**
Status information for running a data test in a real-time system, such as
RiverTrak.
*/
private String
	__lastTestDateTime = DMIUtil.MISSING_STRING,
	__nextTestDateTime = DMIUtil.MISSING_STRING;

/**
A message that will be logged when the data test is positive.
*/
private String __message = DMIUtil.MISSING_STRING;

/**
Additional properties.
*/
private String __properties = DMIUtil.MISSING_STRING;

/**
Time intervals that define specific test settings.
*/
private String  
	__evaluationInterval = DMIUtil.MISSING_STRING,
	__evaluationWindow = DMIUtil.MISSING_STRING,
	__positiveTestInterval = DMIUtil.MISSING_STRING,
	__testInterval = DMIUtil.MISSING_STRING;

/**
Date for the test base time.
*/
private String
	__testBaseTime = DMIUtil.MISSING_STRING,
	__testBaseTimeFormat = DMIUtil.MISSING_STRING;

/**
Checks to make sure that all the required data have been set.  If false is 
returned, more information can be gathered via getMissingValues().
@return true if all the necessary data have been set.  False if not.
*/
public boolean checkAllSet() {
	if (!DMIUtil.isMissing(__dataTestExpressionNum)
//	    && !DMIUtil.isMissing(__dataTestGroupNum)
	    && !DMIUtil.isMissing(__testNum)
//	    && !DMIUtil.isMissing(__description)
//	    && !DMIUtil.isMissing(__message)
//	    && __actionNums != null && __actionNums.length > 0) {
// REVISIT (JTS - 2006-05-09)
// Once the database design has settled down a little, come back and evaluate
// the member variables that MUST be set.
	) {
	    	boolean s = DMIUtil.isMissing(__activeSeasonStart);
		boolean e = DMIUtil.isMissing(__activeSeasonEnd);
		boolean f = DMIUtil.isMissing(__activeSeasonFormat);
		if (s && e && f) {
			// if all are missing, OK
		}
		else if (!s && !e && !f) { 
			// if none are missing, OK
		}
		else {
			// if not all are present and not all are missing, not
			// OK
			return false;
		}

		boolean missingBaseTime = DMIUtil.isMissing(__testBaseTime);
		boolean missingFormat = DMIUtil.isMissing(__testBaseTimeFormat);
		if (missingBaseTime || missingFormat) {
			return false;
		}
		else {
			return true;
		}
	}
	else {
		return false;
	}
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__actionNums = null;
	__activeSeasonEnd = null;
	__activeSeasonStart = null;
	__activeSeasonFormat = null;
	__evaluationEnd = null;
	__evaluationStart = null;
	__lastTestDateTime = null;
	__nextTestDateTime = null;	
	__message = null;
	__description = null;
	__properties = null;
	__evaluationInterval = null;
	__evaluationWindow = null;
	__positiveTestInterval = null;
	__testInterval = null;
	__testBaseTime = null;
	__testBaseTimeFormat = null;
	super.finalize();
}

/**
In conjunction with checkAllSet(), returns the list of the data which have not
been set.
@return a String with the names of the data that have not been set, or ""
if no data are missing.
*/
public String getMissingValues() {
	List v = new Vector();

	if (DMIUtil.isMissing(__testNum)) {
		v.add("ID number");
	}
	if (DMIUtil.isMissing(__dataTestExpressionNum)) {
		v.add("ID number");
	}	
	/*
	if (DMIUtil.isMissing(__dataTestGroupNum)) {
		v.add("data test group number");	
	}
	*/
	/*
	if (DMIUtil.isMissing(__description)) {
		v.add("description");
	}
	*/
	/*
	if (__actionNums == null || __actionNums.length == 0) {
		v.add("action IDs");
	}
	*/
	/*
	if (DMIUtil.isMissing(__message)) {
		v.add("message");
	}
	*/

    	boolean ms = DMIUtil.isMissing(__activeSeasonStart);
	boolean me = DMIUtil.isMissing(__activeSeasonEnd);
	boolean mf = DMIUtil.isMissing(__activeSeasonFormat);

	if ((ms || me) && !mf) {
		// format set but one of the dates was not set	
		if (ms && me) {
			v.add("active season start and end");
		}
		else if (ms) {
			v.add("active season start");
		}
		else if (me) {
			v.add("active season end");
		}
	}
	if ((!ms && !me) && mf) {
		// format not set but dates set	
		v.add("active season format");
	}

	if (DMIUtil.isMissing(__testBaseTimeFormat)) {
		v.add("test base time format");
	}
	else if (DMIUtil.isMissing(__testBaseTime)) {
		v.add("test base time");
	}

	int size = v.size();
	if (size == 0) {
		return "";
	}
	else {
		String s = "";
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				s += ", ";
			}
			s += v.get(i);
		}
		return s;
	}
}

/**
Returns the IDs of the actions tied to this data test.
@return the actions tied to this data test.
*/
public int[] getActionNums() {
	return __actionNums;
}

/**
Returns the active season end.
@return the active season end.
*/
public DateTime getActiveSeasonEnd() 
throws Exception {
	if (DMIUtil.isMissing(__activeSeasonEnd)
	    || DMIUtil.isMissing(__activeSeasonFormat)) {
	    	return null;
	}
	else {
		DateTimeFormat format = new DateTimeFormat(
			__activeSeasonFormat);
		return format.parse(__activeSeasonEnd);
	}
}

/**
Returns the active season end as a String.
@return the active season end as a String.
*/
public String getActiveSeasonEndString() 
throws Exception {
	return __activeSeasonEnd;
}

/**
Returns the active season end format.
@return the active season end format.
*/
public DateTimeFormat getActiveSeasonFormat() 
throws Exception {
	if (DMIUtil.isMissing(__activeSeasonFormat)) {
		return null;
	}
	else {
		return new DateTimeFormat(__activeSeasonFormat);
	}
}

/**
Returns the active season end format as a String.
@return the active season end format as a String.
*/
public String getActiveSeasonFormatString() {
	return __activeSeasonFormat;
}

/**
Returns the active season start.
@return the active season start.
*/
public DateTime getActiveSeasonStart() 
throws Exception {
	if (DMIUtil.isMissing(__activeSeasonStart)
	    || DMIUtil.isMissing(__activeSeasonFormat)) {
	    	return null;
	}
	else {
		DateTimeFormat format = new DateTimeFormat(
			__activeSeasonFormat);
		return format.parse(__activeSeasonStart);
	}
}

/**
Returns the active season start as a String.
@return the active season start as a String.
*/
public String getActiveSeasonStartString() 
throws Exception {
	return __activeSeasonStart;
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
Returns the evaluation end date.
@return the evaluation end date.
*/
public DateTime getEvaluationEnd() 
throws Exception {
	if (DMIUtil.isMissing(__evaluationEnd)) {
		return null;
	}
	else {
		return DateTime.parse(__evaluationEnd);
	}
}

/**
Returns the evaluation end date as a string.
@return the evaluation end date as a string.
*/
public String getEvaluationEndString() {
	return __evaluationEnd;
}

/**
Returns the evaluation interval.
@return the evaluation interval.
*/
public TimeInterval getEvaluationInterval() 
throws Exception {
	if (DMIUtil.isMissing(__evaluationInterval)) {
		return null;
	}
	else {
		return TimeInterval.parseInterval(__evaluationInterval);
	}
}

/**
Returns the evaluation interval as a String.
@return the evaluation interval as a String.
*/
public String getEvaluationIntervalString() {
	return __evaluationInterval;
}

/**
Returns the evaluation start date.
@return the evaluation start date.
*/
public DateTime getEvaluationStart() 
throws Exception {
	if (DMIUtil.isMissing(__evaluationStart)) {
		return null;
	}
	else {
		return DateTime.parse(__evaluationStart);
	}
}

/**
Returns the evaluation start date as a string.
@return the evaluation start date as a string.
*/
public String getEvaluationStartString() {
	return __evaluationStart;
}

/**
Returns the evaluation window.
@return the evaluation window.
*/
public TimeInterval getEvaluationWindow() 
throws Exception {
	if (DMIUtil.isMissing(__evaluationWindow)) {
		return null;
	}
	else {
		return TimeInterval.parseInterval(__evaluationWindow);
	}
}

/**
Returns the evaluation window as a String.
@return the evaluation window as a String.
*/
public String getEvaluationWindowString() {
	return __evaluationWindow;
}

/**
Returns the ID of the test.
@return the ID of the test.
@deprecated use getDataTestNum()
*/
public int getTestNum() {
	return __testNum;
}

public int getDataTestNum() {
	return __testNum;
}

/**
Returns the last time the test was run in a real-time system.
@return the last time the test was run in a real-time system.
*/
public DateTime getLastTestDateTime() 
throws Exception {
	return DateTime.parse(__lastTestDateTime);
}

/**
Returns the last time the test was run in a real-time system.
@return the last time the test was run in a real-time system.
*/
public String getLastTestDateTimeString() {
	return __lastTestDateTime;
}

/**
Returns the next time the test will be run in a real-time system.
@return the next time the test will be run in a real-time system.
*/
public DateTime getNextTestDateTime() 
throws Exception {
	return DateTime.parse(__nextTestDateTime);
}

/**
Returns the next time the test will be run in a real-time system.
@return the next time the test will be run in a real-time system.
*/
public String getNextTestDateTimeString() {
	return __nextTestDateTime;
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
Returns the positive test interval.
@return the positive test interval.
*/
public TimeInterval getPositiveTestInterval() 
throws Exception {
	if (DMIUtil.isMissing(__positiveTestInterval)) {
		return null;
	}
	else {
		return TimeInterval.parseInterval(__positiveTestInterval);
	}
}

/**
Returns the positive test interval as a String.
@return the positive test interval as a String.
*/
public String getPositiveTestIntervalString() {
	return __positiveTestInterval;
}

/**
Returns the additional properties.
@return the additional properties.
*/
public PropList getProperties() {
	if (DMIUtil.isMissing(__properties)) {
		return null;
	}
	else {
		return PropList.parse(__properties, DataTest.PROP_DELIM, 
			"Properties");
	}
}

/**
Returns the properties String.
@return the properties String.
*/
public String getPropertiesString() {
	return __properties;
}

/**
Returns the test base time.
@return the test base time.
*/
public DateTime getTestBaseTime() 
throws Exception {
	if (DMIUtil.isMissing(__testBaseTime) 
	    || DMIUtil.isMissing(__testBaseTimeFormat)) {
	    	return null;
	}
	else {
		DateTimeFormat format = new DateTimeFormat(
			__testBaseTimeFormat);
		return format.parse(__testBaseTime);
	}
}

/**
Returns the test base time as a String.
@return the test base time as a String.
*/
public String getTestBaseTimeString() 
throws Exception {
	return __testBaseTime;
}

/**
Returns the test base time format.
@return the test base time format.
*/
public DateTimeFormat getTestBaseTimeFormat() 
throws Exception {
	if (DMIUtil.isMissing(__testBaseTimeFormat)) {
		return null;
	}
	else {
		return new DateTimeFormat(__testBaseTimeFormat);
	}
}

/**
Returns the test base time format as String.
@return the test base time format as String.
*/
public String getTestBaseTimeFormatString() {
	return __testBaseTimeFormat;
}

/**
Returns the test interval.
@return the test interval.
*/
public TimeInterval getTestInterval() 
throws Exception {
	if (DMIUtil.isMissing(__testInterval)) {
		return null;
	}
	else {
		return TimeInterval.parseInterval(__testInterval);
	}
}

/**
Returns the test interval as a String.
@return the test interval as a String.
*/
public String getTestIntervalString() {
	return __testInterval;
}

/**
Returns whether the test is active or not.
@return whether the test is active or not.
*/
public boolean isActive() {
	return __isActive;
}

/**
Sets the actions associated with this test (in an int array of ID numbers).
@param actions the actions associated with this test.
*/
public void setActionNums(int[] actionNums) {
	__actionNums = actionNums;
}

/**
Sets the active season end.
@param activeSeasonEnd the date to set.
*/
public void setActiveSeasonEnd(String activeSeasonEnd) {
	__activeSeasonEnd = activeSeasonEnd;
}

/**
Sets the active season end.
@param activeSeasonEnd the date to set.
*/
public void setActiveSeasonEnd(DateTime activeSeasonEnd) {
	if (activeSeasonEnd == null) {
		__activeSeasonEnd = DMIUtil.MISSING_STRING;
	}
	else {
		__activeSeasonEnd = activeSeasonEnd.toString();
	}
}

/**
Sets the active season format.
@param activeSeasonFormat the date format to set.
*/
public void setActiveSeasonFormat(String activeSeasonFormat) {
	__activeSeasonFormat = activeSeasonFormat;
}

/**
Sets the active season format.
@param activeSeason the format to set.
*/
public void setActiveSeasonFormat(DateTimeFormat activeSeasonFormat) {
	if (activeSeasonFormat == null) {
		__activeSeasonFormat = DMIUtil.MISSING_STRING;
	}
	else {
		__activeSeasonFormat = activeSeasonFormat.getFormat();
	}
}

/**
Sets the active season start.
@param activeSeasonStart the date to set.
*/
public void setActiveSeasonStart(String activeSeasonStart) {
	__activeSeasonStart = activeSeasonStart;
}

/**
Sets the active season start.
@param activeSeasonStart the date to set.
*/
public void setActiveSeasonStart(DateTime activeSeasonStart) {
	if (activeSeasonStart == null) {
		__activeSeasonStart = DMIUtil.MISSING_STRING;
	}
	else {
		__activeSeasonStart = activeSeasonStart.toString();
	}
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
@param evaluationEnd the evaluation end date.
*/
public void setEvaluationEnd(DateTime evaluationEnd) {
	if (evaluationEnd == null) {
		__evaluationEnd = DMIUtil.MISSING_STRING;
	}
	else {
		__evaluationEnd = evaluationEnd.toString();
	}
}

/**
Sets the evaluation end date.
@param evaluationEnd the evaluation end date.
*/
public void setEvaluationEnd(String evaluationEnd) {
	__evaluationEnd = evaluationEnd;
}

/**
Sets the evaluation interval.
@param evaluationInterval the interval to set.
*/
public void setEvaluationInterval(String evaluationInterval) {
	__evaluationInterval = evaluationInterval;
}

/**
Sets the evaluation interval.
@param evaluationInterval the interval to set.
*/
public void setEvaluationInterval(TimeInterval evaluationInterval) {
	if (evaluationInterval == null) {
		__evaluationInterval = DMIUtil.MISSING_STRING;
	}
	else {
		__evaluationInterval = evaluationInterval.toString();
	}
}

/**
Sets the evaluation start date.
@param evaluationStart the evaluation start date.
*/
public void setEvaluationStart(DateTime evaluationStart) {
	if (evaluationStart == null) {
		__evaluationStart = DMIUtil.MISSING_STRING;
	}
	else {
		__evaluationStart = evaluationStart.toString();
	}
}

/**
Sets the evaluation start date.
@param evaluationStart the evaluation start date.
*/
public void setEvaluationStart(String evaluationStart) {
	__evaluationStart = evaluationStart;
}

/**
Sets the evaluation window.
@param evaluationWindow the interval to set.
*/
public void setEvaluationWindow(String evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets the evaluation window.
@param evaluationWindow the interval to set.
*/
public void setEvaluationWindow(TimeInterval evaluationWindow) {
	if (evaluationWindow == null) {
		__evaluationWindow = DMIUtil.MISSING_STRING;
	}
	else {
		__evaluationWindow = evaluationWindow.toString();
	}
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
	if (lastTestDateTime == null) {
		__lastTestDateTime = DMIUtil.MISSING_STRING;
	}
	else {
		__lastTestDateTime = lastTestDateTime.toString();
	}
}

/**
Sets the last time the test was run in a real-time system.
@param lastTestDateTime the last time the test was run in a real-time system.
*/
public void setLastTestDateTime(String lastTestDateTime) {
	__lastTestDateTime = lastTestDateTime;
}

/**
Sets the next time the test will be run in a real-time system.
@param nextTestDateTime the next time the test will be run in a real-time 
system.
*/
public void setNextTestDateTime(DateTime nextTestDateTime) {
	if (nextTestDateTime == null) {
		__nextTestDateTime = DMIUtil.MISSING_STRING;
	}
	else {
		__nextTestDateTime = nextTestDateTime.toString();
	}
}

/**
Sets the next time the test will be run in a real-time system.
@param nextTestDateTime the next time the test will be run in a real-time 
system.
*/
public void setNextTestDateTime(String nextTestDateTime) {
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
public void setPositiveTestInterval(String positiveTestInterval) {
	__positiveTestInterval = positiveTestInterval;
}

/**
Sets the positive test interval.
@param positiveTestInterval the interval to set.
*/
public void setPositiveTestInterval(TimeInterval positiveTestInterval) {
	if (positiveTestInterval == null) {
		__positiveTestInterval = DMIUtil.MISSING_STRING;
	}
	else {
		__positiveTestInterval = positiveTestInterval.toString();
	}
}

/**
Sets additional properties.
@param properties the additional properties to set.
*/
public void setProperties(String properties) {
	__properties = properties;
}

/**
Sets additional properties.
@param properties the additional properties to set.
*/
public void setProperties(PropList properties) {
	if (properties == null) {
		__properties = DMIUtil.MISSING_STRING;
	}
	else {
		__properties = properties.toString(DataTest.PROP_DELIM);
	}
}

/**
Sets the test base time.
@param testBaseTime the base time to set.
*/
public void setTestBaseTime(String testBaseTime) {
	__testBaseTime = testBaseTime;
}

/**
Sets the test base time.
@param testBaseTime the base time to set.
*/
public void setTestBaseTime(DateTime testBaseTime) {
	if (testBaseTime == null) {
		__testBaseTime = DMIUtil.MISSING_STRING;
	}
	else {
		__testBaseTime = testBaseTime.toString();
	}
}

/**
Sets the test base time format.
@param testBaseTime the format to set.
*/
public void setTestBaseTimeFormat(String testBaseTimeFormat) {
	__testBaseTimeFormat = testBaseTimeFormat;
}

/**
Sets the test base time format.
@param testBaseTime the base time to set.
*/
public void setTestBaseTimeFormat(DateTimeFormat testBaseTimeFormat) {
	if (testBaseTimeFormat == null) {
		__testBaseTimeFormat = DMIUtil.MISSING_STRING;
	}
	else {
		__testBaseTimeFormat = testBaseTimeFormat.getFormat();
	}
}

/**
Sets the test interval.
@param testInterval the interval to set.
*/
public void setTestInterval(String testInterval) {
	__testInterval = testInterval;
}

/**
Sets the test interval.
@param testInterval the interval to set.
*/
public void setTestInterval(TimeInterval testInterval) {
	if (testInterval == null) {
		__testInterval = DMIUtil.MISSING_STRING;
	}
	else {
		__testInterval = testInterval.toString();
	}
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
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return "DataTestDataModel {\n"
		+ "Data Test Expression Num: " + __dataTestExpressionNum + "\n"
		+ "Data Test Group Num:      " + __dataTestGroupNum + "\n"
		+ "Test Num:                   " + __testNum + "\n"
		+ "Positive Count:           " + __positiveCount + "\n"
		+ "Action Nums:             '" + __actionNums + "'\n"
		+ "Active Season End:       '" + __activeSeasonEnd + "'\n"
		+ "Action Season Start:     '" + __activeSeasonStart + "'\n"
		+ "Active Season Format:    '" + __activeSeasonFormat + "'\n"
		+ "Description:             '" + __description + "'\n"
		+ "Is Active:                " + __isActive + "\n"
		+ "Message:                 '" + __message + "'\n"
		+ "Properties:              '" + __properties + "'\n"
		+ "Evaluation Interval:     '" + __evaluationInterval + "'\n"
		+ "Evaluation Window:       '" + __evaluationWindow + "'\n"
		+ "Positive Test Interval:  '" + __positiveTestInterval + "'\n"
		+ "Test Interval:           '" + __testInterval + "'\n"
		+ "Test Base Time:          '" + __testBaseTime + "'\n"
		+ "Test Base Time Format:   '" + __testBaseTimeFormat  + "'\n"
		+ "Last Test Date Time:     '" + __lastTestDateTime + "'\n"
		+ "Next Test Date Time:     '" + __nextTestDateTime + "'\n"
		+ "Was Last Test Positive:   " + __wasLastTestPositive
		+ "\n}\n";
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

}
