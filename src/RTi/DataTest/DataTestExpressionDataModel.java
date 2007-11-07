// ----------------------------------------------------------------------------
// DataTestExpressionDataModel - the data model for transferring data from 
//	the IO interface to an expression object.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.PropList;

/**
This class is a wrapper class for storing data between the data source and the
final expression object.
<p>
<b>Note on DataModel classes:</b><p>
In a data model class, there are set methods that take two types of 
parameters (e.g., in Action, setActionInterval(), which takes either a String
or a TimeInterval), and there are get methods that return either a 
core Java data type or a more complicated object (e.g., 
in Action, getActionBaseTime()/getActionBaseTimeString(), which return a 
DateTime or a String, respectively).  <p>
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
public class DataTestExpressionDataModel {

/**
The type of expression.
*/
private int __expressionType = DMIUtil.MISSING_INT;

/**
The ID number of this expression.
*/
private int __expressionNum = DMIUtil.MISSING_INT;

/**
The ID and type of the left hand side.
*/
private int 
	__leftSideIDNum = DMIUtil.MISSING_INT,
	__leftSideType = DMIUtil.MISSING_INT;

/**
The ID and type of the right hand side.
*/
private int 
	__rightSideType = DMIUtil.MISSING_INT,
	__rightSideIDNum = DMIUtil.MISSING_INT;

/**
The operator.
*/
private String __operator = DMIUtil.MISSING_STRING;

/**
Any additional properties for the expression.
*/
private String __properties = DMIUtil.MISSING_STRING;

/**
Checks to make sure that all the required data have been set.  If false is 
returned, more information can be gathered via getMissingValues().
@return true if all the necessary data have been set.  False if not.
*/
public boolean checkAllSet() {
	if (!DMIUtil.isMissing(__expressionType)
	    && !DMIUtil.isMissing(__leftSideIDNum) 
	    && !DMIUtil.isMissing(__leftSideType)
	    && !DMIUtil.isMissing(__rightSideIDNum) 
	    && !DMIUtil.isMissing(__rightSideType)
	    && !DMIUtil.isMissing(__operator) 
	    && !DMIUtil.isMissing(__expressionNum)) {
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
	__operator = null;
	__properties = null;
	super.finalize();
}

/**
Returns the expression type.
@return the expression type.
*/
public int getExpressionType() {
	return __expressionType;
}

/**
Returns the ID number.
@return the ID number.
*/
public int getExpressionNum() {
	return __expressionNum;
}

/**
Returns the ID of the left hand side.
@return the ID of the left hand side.
*/
public int getLeftSideIDNum() {
	return __leftSideIDNum;
}

/**
Returns the type of the left hand side.
@return the type of the left hand side.
*/
public int getLeftSideType() {
	return __leftSideType;
}

/**
In conjunction with checkAllSet(), returns the list of the data which have not
been set.
@return a String with the names of the data that have not been set, or "" 
if no data are missing.
*/
public String getMissingValues() {
	Vector v = new Vector();

	if (DMIUtil.isMissing(__expressionType)) {
		v.add("expression type");	
	}
	if (DMIUtil.isMissing(__expressionNum)) {
		v.add("ID number");
	}
	if (DMIUtil.isMissing(__leftSideIDNum)) {
		v.add("left side num");
	}
	if (DMIUtil.isMissing(__leftSideType)) {
		v.add("left side type");
	}	
	if (DMIUtil.isMissing(__rightSideIDNum)) {
		v.add("right side num");
	}
	if (DMIUtil.isMissing(__rightSideType)) {
		v.add("right side type");
	}		
	if (DMIUtil.isMissing(__operator)) {
		v.add("operator");
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
Returns the ID of the right hand side.
@return the ID of the right hand side.
*/
public int getRightSideIDNum() {
	return __rightSideIDNum;
}

/**
Returns the type of the right hand side.
@return the type of the right hand side.
*/
public int getRightSideType() {
	return __rightSideType;
}

/**
Returns the operator.
@return the operator.
*/
public String getOperator() {
	return __operator;
}

/**
Returns additional properties.
@return additional properties.
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
Returns additional properties as a String.
@return additional properties as a String.
*/
public String getPropertiesString() {
	return __properties;
}

/**
Sets the type of the expression.
@param expressionType the type of the expression.
*/
public void setExpressionType(int expressionType) {
	__expressionType = expressionType;
}

/**
Sets the ID number.
@param expressionNum the ID number to set.
*/
public void setExpressionNum(int expressionNum) {
	__expressionNum = expressionNum;
}

/**
Sets the ID of the left hand side.
@param leftSideIDNum the ID to set.
*/
public void setLeftSideIDNum(int leftSideIDNum) {
	__leftSideIDNum = leftSideIDNum;
}

/**
Sets the type of the left hand side.
@param leftSideType the type to set.
*/
public void setLeftSideType(int leftSideType) {
	__leftSideType = leftSideType;
}

/**
Sets the ID of the right hand side.
@param rightSideIDNum the ID to set.
*/
public void setRightSideIDNum(int rightSideIDNum) {
	__rightSideIDNum = rightSideIDNum;
}

/**
Sets the type of the right hand side.
@param rightSideType the type to set.
*/
public void setRightSideType(int rightSideType) {
	__rightSideType = rightSideType;
}

/**
Sets the operator.
@param op the operator to set.
*/
public void setOperator(String op) {
	__operator = op;
}

/**
Sets additional properties.
@param properties the properties to set.
*/
public void setProperties(String properties) {
	__properties = properties;
}

/**
Sets additional properties.
@param properties the properties to set.
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
	return "DataTestExpressionDataModel {\n"
		+ "Expression Type:     " + __expressionType + "\n"
		+ "ID Num:              " + __expressionNum + "\n"
		+ "Left Side Num:   " + __leftSideIDNum + "\n"
		+ "Left Side Type:  " + __leftSideType + "\n"
		+ "Right Side Num:  " + __rightSideIDNum + "\n"
		+ "Right Side Type: " + __rightSideType + "\n"
		+ "Operator:           '" + __operator + "'\n"
		+ "Properties:         '" + __properties + "'\n}\n";
}

}
