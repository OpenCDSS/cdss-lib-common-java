// ----------------------------------------------------------------------------
// DataTestFunctionDataModel - the data model for transferring data from 
//	the IO interface to an function object.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.PropList;

import RTi.Util.Time.TimeInterval;

/**
This class is a wrapper class for storing data between the data source and the
final function object.
<p>
<b>Note on DataModel classes:</b><p>
In a data model class, there are set methods that take two types of 
parameters (e.g., setEvaluationInterval(), which takes either a String
or a TimeInterval), and there are get methods that return either a 
core Java data type or a more complicated object (e.g., 
getEvaluationWindow()/getEvaluationWindowString(), which return a DateTime
or a String, respectively).  <p>
The reason for this is that DataModels are the link between the full 
data object (e.g., DataTestFunction) and the data as it is represented in the
persistent data source.  Because the persistent data source could be a
database, an XML file, a web data source, a text file, or anything else, 
the data have to be able to be stored in a generic way, as base Java types
(String, int, double, etc). <p>
DataModels, then, are the bridging classes that translate data from the 
generic storage data to the more complicated data (e.g, DateTime) needed 
by the full data objects, and also do the reverse, taking the complicated 
data objects and translating them back into base Java data types.
*/
public class DataTestFunctionDataModel {

/**
The type of this function.
*/
private int __functionType = DMIUtil.MISSING_INT;

/**
The ID number of this function.
*/
private int __functionNum = DMIUtil.MISSING_INT;

/**
The array of input data IDs.
*/
private String[] __inputDataIDs = null;

/**
The evaluation interval.
*/
private String __evaluationInterval = DMIUtil.MISSING_STRING;

/**
The evaluation window.
*/
private String __evaluationWindow = DMIUtil.MISSING_STRING;

/**
The name of the function.
*/
private String __functionName = DMIUtil.MISSING_STRING;

/**
The function message.
*/
private String __message = DMIUtil.MISSING_STRING;

/**
Additional properties for the class.
*/
private String __properties = DMIUtil.MISSING_STRING; 

/**
Checks to make sure that all the required data have been set.  If false is 
returned, more information can be gathered via getMissingValues().
@return true if all the necessary data have been set.  False if not.
*/
public boolean checkAllSet() {
	if (!DMIUtil.isMissing(__functionType) 
	    && __inputDataIDs != null && __inputDataIDs.length > 0 
	    && !DMIUtil.isMissing(__functionName) 
	    && !DMIUtil.isMissing(__functionNum)) {
	    	return true;
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
	__evaluationInterval = null;
	__evaluationWindow = null;
	// Do not use IOUtil.nullArray() on the __inputDataIDs array, as it
	// is passed directly into the associated DataTestFunction and used 
	// there.  Using IOUtil.nullArray() will result in hard-to-find 
	// null pointer exceptions.
	__inputDataIDs = null;
	__functionName = null;
	__message = null;
	__properties = null;
	super.finalize();
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
Returns the function type.
@return the function type.
*/
public int getFunctionType() {
	return __functionType;
}

/**
Returns the function name.
@return the function name.
*/
public String getFunctionName() {
	return __functionName;
}

/**
Returns the ID number.
@return the ID number.
*/
public int getFunctionNum() {
	return __functionNum;
}

/**
Returns the array of Input data IDs.
@return the array of Input data IDs.
*/
public String[] getInputDataIDs() {
	return __inputDataIDs;
}

/**
Returns the message.
@return the message.
*/
public String getMessage() {
	return __message;
}

/**
In conjunction with checkAllSet(), returns the list of the data which have not
been set.
@return a String with the names of the data that have not been set, or "" if 
no data are missing.
*/
public String getMissingValues() {
	Vector v = new Vector();

	if (DMIUtil.isMissing(__functionType)) {
		v.add("function");
	}
	if (DMIUtil.isMissing(__functionNum)) {
		v.add("ID number");
	}	
	if (__inputDataIDs == null || __inputDataIDs.length == 0) {
		v.add("input data IDs");
	}
	if (DMIUtil.isMissing(__functionName)) {
		v.add("function name");
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
Returns the additional properties for the class.
@return the additional properties for the class.
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
Sets the evaluation window.
@param evaluationWindow the evaluation window to set.
*/
public void setEvaluationWindow(String evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets the evaluation window.
@param evaluationWindow the evaluation window to set.
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
Sets the function type.
@param function the type of function to set.
*/
public void setFunctionType(int function) {
	__functionType = function;
}

/**
Sets the function name.
@param functionName the name of the function to set.
*/
public void setFunctionName(String functionName) {
	__functionName = functionName;
}

/**
Sets the ID number.
@param functionNum the ID number to set.
*/
public void setFunctionNum(int functionNum) {
	__functionNum = functionNum;
}

/**
Sets the input data ID array.
@param inputDataIDs the array of IDs to set.
*/
public void setInputDataIDs(String[] inputDataIDs) {
	__inputDataIDs = inputDataIDs;
}

/**
Sets the message.
@param message the message to set.
*/
public void setMessage(String message) {
	__message = message;
}

/**
Sets the additional properties.
@param properties the additional properties to set.
*/
public void setProperties(String properties) {
	__properties = properties;
}

/**
Sets the additional properties.
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
Returns a String representation of this object.
@return a String representation of this object.
*/
public String toString() {
	String s = "DataTestFunctionDataModel {\n"
		+ "Function:              " + __functionType + "\n"
		+ "ID Number:             " + __functionNum + "\n"
		+ "Evaluation Interval:  '" + __evaluationInterval + "'\n"
		+ "Evaluation Window:    '" + __evaluationWindow + "'\n"
		+ "Function Name:        '" + __functionName + "'\n"
		+ "Message:              '" + __message + "'\n"
		+ "Properties:           '" + __properties + "'\n"
		+ "Input Data ID Numbers: \n";
	s += "Input Data IDs:        \n";
	if (__inputDataIDs != null) {
		for (int i = 0; i < __inputDataIDs.length; i++) {
			s += "                       " 
				+ __inputDataIDs[i];
		}
	}
	return s;
}

}
