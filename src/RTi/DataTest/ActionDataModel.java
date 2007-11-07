// ----------------------------------------------------------------------------
// ActionDataModel - the data model for transferring action data between the 
//	system and the data source.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-21	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.PropList;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeFormat;
import RTi.Util.Time.TimeInterval;

/**
This class is a data model for transferring Action data between the alarm 
system and the persistent data source.  For detailed information on all the 
data members, see the Action class.
<p>
<b>Note on DataModel classes:</b><p>
In a data model class, there are set methods that take two types of 
parameters (e.g., setActionInterval(), which takes either a String
or a TimeInterval), and there are get methods that return either a 
core Java data type or a more complicated object (e.g., 
getActionBaseTime()/getActionBaseTimeString(), which return a DateTime
or a String, respectively).  <p>
The reason for this is that DataModels are the link between the full 
data object (e.g., Action) and the data as it is represented in the
persistent data source.  Because the persistent data source could be a
database, an XML file, a web data source, a text file, or anything else, 
the data have to be able to be stored in a generic way, as base Java types
(String, int, double, etc). <p>
DataModels, then, are the bridging classes that translate data from the 
generic storage data to the more complicated data (e.g, DateTime) needed 
by the full data objects, and also do the reverse, taking the complicated 
data objects and translating them back into base Java data types.
*/
public class ActionDataModel {

/**
Whether the acion is active or not.
*/
private boolean __isActive = false;

/**
The ID number of the action.
*/
private int __actionNum = DMIUtil.MISSING_INT;

/**
The type of action.
*/
private int __actionType = DMIUtil.MISSING_INT;

/**
Qualifier that specifies the level above which DataTestResults will be 
ignored by this Action.  See DataTestResult for more informatio about
result levels.
*/
private int __resultLevel = 0;

/**
The severity of the action.
*/
private int __severityType = DMIUtil.MISSING_INT;

/**
The int array of contact IDs for the action.
*/
private int[] __contactIDs = null;

/**
The action base time.
*/
private String __actionBaseTime = DMIUtil.MISSING_STRING;

/**
The action base time date/time format.
*/
private String __actionBaseTimeFormat = DMIUtil.MISSING_STRING;

/**
The action interval.
*/
private String __actionInterval = DMIUtil.MISSING_STRING;

/**
The description of the action.
*/
private String __description = DMIUtil.MISSING_STRING;

/**
The evaluation window for the action.
*/
private String __evaluationWindow = DMIUtil.MISSING_STRING;

/**
The name of the action.
*/
private String __actionName = DMIUtil.MISSING_STRING;

/**
Additional properties.
*/
private String __properties = DMIUtil.MISSING_STRING;

/**
Checks to make sure that all the required data have been set.  If false is 
returned, more information can be gathered via getMissingValues().
@return true if all the necessary data have been set.  False if not.
*/
public boolean checkAllSet() {
	if (!DMIUtil.isMissing(__actionNum)
	    && !DMIUtil.isMissing(__actionType)
	    && !DMIUtil.isMissing(__severityType)
	    && !DMIUtil.isMissing(__actionName)
	    && !DMIUtil.isMissing(__actionInterval)
	    && !DMIUtil.isMissing(__description)) {
		boolean missingDate = DMIUtil.isMissing(__actionBaseTime);
		boolean missingFormat = DMIUtil.isMissing(
			__actionBaseTimeFormat);
		
		if ((missingDate && missingFormat) 
		    || (!missingDate && !missingFormat)) {
			return true;
		}
		else {
			return false;
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
	__actionBaseTime = null;
	__actionBaseTimeFormat = null;
	__actionInterval = null;
	__description = null;
	__evaluationWindow = null;
	__actionName = null;
	__properties = null;
	__contactIDs = null;
	super.finalize();
}

/**
Returns the action base time.
@return the action base time.
*/
public DateTime getActionBaseTime() 
throws Exception { 
	if (DMIUtil.isMissing(__actionBaseTime)) {
		return null;
	}
	else {
		DateTimeFormat format = new DateTimeFormat(
			__actionBaseTimeFormat);
		return format.parse(__actionBaseTime);
	}
}

/**
Returns the action base time as a String.
@return the action base time as a String.
*/
public String getActionBaseTimeString() 
throws Exception {
	return __actionBaseTime;
}	

/**
Returns the action base time format.
@return the action base time format.
*/
public DateTimeFormat getActionBaseTimeFormat() 
throws Exception {
	if (DMIUtil.isMissing(__actionBaseTimeFormat)) {
		return null;
	}
	else {
		return new DateTimeFormat(__actionBaseTimeFormat);
	}
}

/**
Returns the action base time format as a String.
@return the action base time format as a String.
*/
public String getActionBaseTimeFormatString() {
	return __actionBaseTimeFormat;
}

/**
Returns the action interval.
@return the action interval.
*/
public TimeInterval getActionInterval() 
throws Exception {
	if (DMIUtil.isMissing(__actionInterval)) {
		return null;
	}
	else {
		return TimeInterval.parseInterval(__actionInterval);
	}
}

/**
Returns the action interval as a String.
@return the action interval as a String.
*/
public String getActionIntervalString() {
	return __actionInterval;
}

/**
Returns the ID of the action.
@return the ID of the action.
*/
public int getActionNum() {
	return __actionNum;
}

/**
Returns the type of the action.
@return the type of the action.
*/
public int getActionType() {
	return __actionType;
}

/**
Returns the contact ID array.
@return the contact ID array.
*/
public int[] getContactIDs() {
	return __contactIDs;
}

/**
Returns the action's description.
@return the action's description.
*/
public String getDescription() {
	return __description;
}

/**
Returns the action's evaluation window.
@return the action's evaluation window.
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
Returns the action's evaluation window as a String.
@return the action's evaluation window as a String.
*/
public String getEvaluationWindowString() 
throws Exception {
	return __evaluationWindow;
}

/**
In conjunction with checkAllSet(), returns the list of the data which have not
been set but must be.
@return a String with the names of the data that have not been set, or "" if
no data are missing.
*/
public String getMissingValues() {
	Vector v = new Vector();
	
	if (DMIUtil.isMissing(__actionType)) {
		v.add("action type");
	}
	if (DMIUtil.isMissing(__actionNum)) {
		v.add("action number");
	}
	if (DMIUtil.isMissing(__actionBaseTime)) {
		v.add("action base time");
	}
	if (DMIUtil.isMissing(__actionInterval)) {
		v.add("action interval");
	}
	if (DMIUtil.isMissing(__severityType)) {
		v.add("severity");
	}
	if (DMIUtil.isMissing(__actionName)) {
		v.add("name");
	}	
	if (DMIUtil.isMissing(__description)) {
		v.add("description");
	}

	boolean mDate = DMIUtil.isMissing(__actionBaseTime);
	boolean missingFormat = DMIUtil.isMissing(__actionBaseTimeFormat);

	if (!mDate && missingFormat) {
		v.add("action base time");
	}
	if (mDate && !missingFormat) {
		v.add("action base time format");
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
			s += v.elementAt(i);
		}
		return s;
	}
}

/**
Returns the action's name.
@return the action's name.
*/
public String getActionName() {
	return __actionName;
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
		return PropList.parse(__properties, "Action Properties", 
			DataTest.PROP_DELIM);
	}
}

/**
Returns the additional properties as a String.
@return the additional properties as a String.
*/
public String getPropertiesString() {
	return __properties;
}

/**
Returns the action's cut-off result level.
@return the action's cut-off result level.
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
Returns whether the action is active or not.
@return whether the action is active or not.
*/
public boolean isActive() {
	return __isActive;
}

/**
Sets the base time of the action.
@param actionBaseTime the base time to set.
*/
public void setActionBaseTime(String actionBaseTime) {
	__actionBaseTime = actionBaseTime;
}

/**
Sets the base time of the action.
@param actionBaseTime the base time to set.
*/
public void setActionBaseTime(DateTime actionBaseTime) {
	if (actionBaseTime == null) {
		__actionBaseTime = DMIUtil.MISSING_STRING;
	}
	else {
		__actionBaseTime = actionBaseTime.toString();
	}
}

/**
Sets the base time of the action format.
@param actionBaseTimeFormat the base time format to set.
*/
public void setActionBaseTimeFormat(String actionBaseTimeFormat) {
	__actionBaseTimeFormat = actionBaseTimeFormat;
}

/**
Sets the base time of the action format.
@param actionBaseTimeFormat the base time format to set.
*/
public void setActionBaseTimeFormat(DateTimeFormat actionBaseTimeFormat) {
	if (actionBaseTimeFormat == null) {
		__actionBaseTimeFormat = DMIUtil.MISSING_STRING;
	}
	else {
		__actionBaseTimeFormat = actionBaseTimeFormat.getFormat();
	}
}

/**
Sets the interval of the action.
@param actionInterval the interval to set.
*/
public void setActionInterval(String actionInterval) {
	__actionInterval = actionInterval;
}

/**
Sets the interval of the action.
@param actionInterval the interval to set.
*/
public void setActionInterval(TimeInterval actionInterval) {
	if (actionInterval == null) {
		__actionInterval = DMIUtil.MISSING_STRING;
	}
	else {
		__actionInterval = actionInterval.toString();
	}
}

/**
Sets the ID number of the action.
@param actionNum the ID number to set.
*/
public void setActionNum(int actionNum) {
	__actionNum = actionNum;
}

/**
Sets the type of the action.
@param actionType the type of the action.
*/
public void setActionType(int actionType) {
	__actionType = actionType;
}

/**
Sets the contact IDs.
@param contactIDs the contact IDs.
*/
public void setContactIDs(int[] contactIDs) {
	__contactIDs = contactIDs;
}

/**
Sets the description of the action.
@param description the description of the action.
*/
public void setDescription(String description) {
	__description = description;
}

/**
Sets the window of evaluation of the action.
@param evaluationWindow the window of evaluation of the action.
*/
public void setEvaluationWindow(String evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets the window of evaluation of the action.
@param evaluationWindow the window of evaluation of the action.
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
Sets whether the action is active or not.
@param isActive whether the action is active or not.
*/
public void setIsActive(boolean isActive) {
	__isActive = isActive;
}

/**
Sets the name of the action.
@param name the name of the action.
*/
public void setActionName(String name) {
	__actionName = name;
}

/**
Sets any additional properties.
@param properties the additional properties to set.
*/
public void setProperties(String properties) {
	__properties = properties;
}

/**
Sets any additional properties.
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
Sets the cut-off result level of the action.
@param resultLevel the cut-off result level of the action.
*/
public void setResultLevel(int resultLevel) {
	__resultLevel = resultLevel;
}

/**
Sets the severity of the action.
@param severity the severity of the action.
*/
public void setSeverityType(int severity) {
	__severityType = severity;
}

/**
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	return "ActionDataModel {\n"
		+ "Is Active:                 " + __isActive + "\n"
		+ "Action Num:                " + __actionNum + "\n"
		+ "Action Type:               " + __actionType + "\n"
		+ "Result Level:              " + __resultLevel + "\n"
		+ "Severity:                  " + __severityType + "\n"
		+ "Action Base Time:         '" + __actionBaseTime + "'\n"
		+ "Action Base Time Format:  '" + __actionBaseTimeFormat + "'\n"
		+ "Action Interval:          '" + __actionInterval + "'\n"
		+ "Description:              '" + __description + "'\n"
		+ "Evaluation Window:        '" + __evaluationWindow + "'\n"
		+ "Name:                     '" + __actionName + "'\n"
		+ "Properties:               '" + __properties + "'\n}\n";
}

}
