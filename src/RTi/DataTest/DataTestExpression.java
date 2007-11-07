// ----------------------------------------------------------------------------
// DataTestExpression - the abstract base class for all expressions.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-14	JTS, RTi		The DataTest is now passed into the
//					DataTestResult that is created.
// 2006-05-03	JTS, RTi		* The AlertIOInterface is now passed 
//					  into write(), rather than being 
//					  stored in the class.
//					* write() now returns the ID number of
//					  the object that was written, rather 
//					  than a boolean.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.PropList;

import RTi.Util.Time.DateTime;

/**
This class is the abstract base class from which all other Expression classes
are built.
*/
public abstract class DataTestExpression 
extends DataTestSide {

/**
The types of expressions supported.
*/
public static final int 
	TYPE_UNKNOWN = -1,
	TYPE_ADD = 			1,
	TYPE_AND = 			2,
	TYPE_DIVIDE = 			3,
	TYPE_EQUALS = 			4,
	TYPE_GREATERTHAN = 		5,
	TYPE_GREATERTHANOREQUALTO = 	6,
	TYPE_LESSTHAN = 		7,
	TYPE_LESSTHANOREQUALTO = 	8,
	TYPE_MULTIPLY = 		9,
	TYPE_NOTEQUAL = 		10,
	TYPE_OR = 			11,
	TYPE_SUBTRACT = 		12;


/**
References to the left and right hand sides of the expression.
*/
private DataTestSide
	__leftSide = null,
	__rightSide = null;

/**
The type of expression this expression is (e.g., TYPE_ADD).
*/
private int __expressionType = DMIUtil.MISSING_INT;

/**
The ID number of this expression.
*/
private int __expressionNum = DMIUtil.MISSING_INT;

/**
The ID number of the left hand data side, and its type.
*/
private int 
	__leftSideIDNum = DMIUtil.MISSING_INT,
	__leftSideType = DMIUtil.MISSING_INT;

/**
The ID number of the right hand data side, and its type.
*/
private int 
	__rightSideType = DMIUtil.MISSING_INT,
	__rightSideIDNum = DMIUtil.MISSING_INT;

/**
Any additional properties necessary for this expression.
*/
private PropList __properties = null;

/**
The operator used in this expression (e.g., "+").
*/
private String __operator = DMIUtil.MISSING_STRING;

/**
Empty constructor.  This constructor does not set up any data members.  Any code
that calls this constructor must set up all the data members for the class.
*/
public DataTestExpression() 
throws Exception {
	this((DataTestExpressionDataModel) null);
}

/**
Constructor.
@param model the DataTestExpressionDataModel to use to fill data members.
@throws Exception if not all the required values in the data model have been
filled.
*/
public DataTestExpression(DataTestExpressionDataModel model) 
throws Exception {
	if (model == null) {
		return;
	}
	transferValuesFromDataModel(model);
}

/**
Copy Constructor.
@param e the DataTestExpression to copy.
*/
public DataTestExpression(DataTestExpression e) {
	super(e);
	__leftSide = DataTestSide.copy(e.__leftSide, e.__leftSideType);
	__rightSide = DataTestSide.copy(e.__rightSide, e.__rightSideType);

	__expressionType = e.__expressionType;
	__expressionNum = e.__expressionNum;

	__leftSideIDNum = e.__leftSideIDNum;
	__leftSideType = e.__leftSideType;
	__rightSideIDNum = e.__rightSideIDNum;
	__rightSideType = e.__rightSideType;

	if (e.__properties != null) {
		__properties = new PropList(e.__properties);
	}

	__operator = e.__operator;
}

/**
Creates a DataTestResult for this expression with the most basic of info
filled out.
@param test the DataTest in which the result was generated.
@param testTime the time at which the test was run.
@param level the level at which the result will be built.
@return a DataTestResult for this expression.
*/
private DataTestResult buildDataTestResult(DataTest test, DateTime testTime, 
int level) {
	DataTestResult result = new DataTestResult();
	result.setDataTest(test);
	result.setDataTestNum(test.getDataTestNum());
	result.setDataTestExpressionNum(__expressionNum);
	result.setLeftSideValue(__leftSide.getLastResult());
	result.setRightSideValue(__rightSide.getLastResult());
	result.setLevel(level);
	result.setTestTime(new DateTime(testTime));
	return result;
}

/**
Builds the message that will be displayed for this expression in a 
positive test result.
@param dateTime the date/time for which to build the positive message.
*/
public String buildPositiveResultMessage(DateTime dateTime) {
	return getLeftSide().buildPositiveResultMessage(dateTime) 
		+ " " + getOperator() + " " 
		+ getRightSide().buildPositiveResultMessage(dateTime);
}

/**
Evalutes the expression after it has been determined (via evaluate()) that 
a positive result occurs.  See DataTest.run().
@param test the DataTest in which this expression is occuring.
@param results the Vector into which DataTestResults will be accumulated.
@param testTime the date/time of the test.
@param level the level of the results that will be generated.  The top level
of results are at level 0.
*/
public void evaluatePositiveResult(DataTest test, Vector results, 
DateTime testTime, int level) {
	DataTestResult result = buildDataTestResult(test, testTime, level);
	
	boolean hasMessage = false;
	int increment = 1;
	if (level == 0 && !DMIUtil.isMissing(test.getMessage())) {
		hasMessage = true;
		increment = 2;
	}

	DataTestSide leftSide = getLeftSide();
	if (leftSide.isExpression() 
	    && leftSide.getLastResult() != DataTest.FALSE) {
		// By comparing to != FALSE, it is checking for a non-zero 
		// value.  Anything == 0 is considered FALSE, anything 
		// non-zero is TRUE.
		leftSide.evaluatePositiveResult(test, results, testTime, 
			(level + increment));
	}

	DataTestSide rightSide = getRightSide();
	if (rightSide.isExpression() 
    	    && rightSide.getLastResult() != DataTest.FALSE) {
		// By comparing to != FALSE, it is checking for a non-zero 
		// value.  Anything == 0 is considered FALSE, anything 
		// non-zero is TRUE.	    
		rightSide.evaluatePositiveResult(test, results, testTime, 
			(level + increment));
	}
	
	//## mt 2006-10-06:
	//String operatorString = getOperator();
	//if( rightSide.isFunction()
	//    && rightSide.getPositiveMessage.equals("NOMESSAGE") ) {
	//	operatorString = "";
	//}

	if (hasMessage) {
		DataTestResult copy = new DataTestResult(result);
		copy.setMessage(leftSide.buildPositiveResultMessage(testTime) 
			+ " " + getOperator() + " "   //##mt: use operatorString
			+ rightSide.buildPositiveResultMessage(testTime));
		copy.setLevel(1);
		results.add(copy);
		result.setMessage(test.getMessage());
	}
	else {
		result.setMessage(leftSide.buildPositiveResultMessage(testTime) 
			+ " " + getOperator() + " "   //##mt: use operatorString
			+ rightSide.buildPositiveResultMessage(testTime));
	}

	results.add(result);
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__leftSide = null;
	__rightSide = null;
	__operator = null;
	__properties = null;
	super.finalize();
}

/**
Returns the expression operator given the expression type (e.g., TYPE_ADD). 
Null will be returned if an unknown type is passed in.
@return the expression operator given the expression type (e.g., TYPE_ADD). 
Null will be returned if an unknown type is passed in.
*/
public static String getExpressionOperator(int type) {
	if (type == TYPE_ADD) {
		return "+";
	}
	else if (type == TYPE_AND) {
		return "&&";
	}
	else if (type == TYPE_DIVIDE) {
		return "/";
	}
	else if (type == TYPE_EQUALS) {
		return "==";
	}
	else if (type == TYPE_GREATERTHAN) {
		return ">";
	}
	else if (type == TYPE_GREATERTHANOREQUALTO) {
		return ">=";
	}
	else if (type == TYPE_LESSTHAN) {
		return "<";
	}
	else if (type == TYPE_LESSTHANOREQUALTO) {
		return "<=";
	}
	else if (type == TYPE_MULTIPLY) {
		return "*";
	}
	else if (type == TYPE_NOTEQUAL) {
		return "!=";
	}
	else if (type == TYPE_OR) {
		return "||";
	}
	else if (type == TYPE_SUBTRACT) {
		return "-";
	}
	else {
		return null;
	}
}

/**
Returns the expression type given the expression operator (e.g., "+").  
TYPE_UNKNOWN will be returned if an unknown operator is passed in.
@return the expression type given the expression operator (e.g., "+").  
TYPE_UNKNOWN will be returned if an unknown operator is passed in.
*/
public static int getExpressionType(String op) {
	if (op == null) {
		return TYPE_UNKNOWN;
	}
	else if (op.equals("+")) {
		return TYPE_ADD;
	}
	else if (op.equals("&&")) {
		return TYPE_AND;
	}
	else if (op.equals("/")) {
		return TYPE_DIVIDE;
	}
	else if (op.equals("==")) {
		return TYPE_EQUALS;
	}
	else if (op.equals(">")) {
		return TYPE_GREATERTHAN;
	}
	else if (op.equals(">=")) {
		return TYPE_GREATERTHANOREQUALTO;
	}
	else if (op.equals("<")) {
		return TYPE_LESSTHAN;
	}
	else if (op.equals("<=")) {
		return TYPE_LESSTHANOREQUALTO;
	}
	else if (op.equals("*")) {
		return TYPE_MULTIPLY;
	}
	else if (op.equals("!=")) {
		return TYPE_NOTEQUAL;
	}
	else if (op.equals("||")) {
		return TYPE_OR;
	}
	else if (op.equals("-")) {
		return TYPE_SUBTRACT;
	}
	else {
		return TYPE_UNKNOWN;
	}
}

/**
Returns the type of this expression.
@return the type of this expression.
*/
public int getExpressionType() {
	return __expressionType;
}

/**
Returns the ID number of this expression.
@return the ID number of this expression.
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
Returns the left side.
@return the left side.
*/
public DataTestSide getLeftSide() {
	return __leftSide;
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
Returns the right side.
@return the right side.
*/
public DataTestSide getRightSide() {
	return __rightSide;
}

/**
Returns the operator for this expression.
@return the operator for this expression.
*/
public String getOperator() {
	return __operator;
}

/**
Returns the additional properties for this expression.
@return the additional properties for this expression.
*/
public PropList getProperties() {
	return __properties;
}

/**
Returns true.
@return true.
*/
public boolean isExpression() {
	return true;
}

/**
Returns false.
@return false.
*/
public boolean isFunction() {
	return false;
}

/**
Sets the type of this expression.
@param expressionType the type to set.
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
@param leftSideNum the ID to set.
*/
public void setLeftSideIDNum(int leftSideNum) {
	__leftSideIDNum = leftSideNum;
}

/**
Sets the type of the left hand side (DataTestSide.FUNCTION or 
DataTestSide.EXPRESSION).
@leftSideType the type to set.
*/
public void setLeftSideType(int leftSideType) {
	__leftSideType = leftSideType;
}

/**
Sets the left side.
@param leftSide the reference to the left side.
*/
public void setLeftSide(DataTestSide leftSide) {
	__leftSide = leftSide;
}

/**
Sets the operator.
@param op the operator to set.
*/
public void setOperator(String op) {
	__operator = op;
}

/**
Sets the properties.
@param properties the properties to set.
*/
public void setProperties(PropList properties) {
	__properties = properties;
}

/**
Sets the ID of the right hand side.
@param rightSideNum the ID to set.
*/
public void setRightSideIDNum(int rightSideNum) {
	__rightSideIDNum = rightSideNum;
}

/**
Sets the type of the right hand side.
@param rightSideType the type to set.
*/
public void setRightSideType(int rightSideType) {
	__rightSideType = rightSideType;
}

/**
Sets the right side.
@param rightSide the reference to the right side.
*/
public void setRightSide(DataTestSide rightSide) {
	__rightSide = rightSide;
}

/**
Converts the values in this expression to a data model.
@return a DataTestExpressionDataModel filled with this expression's data.
*/
public DataTestExpressionDataModel toDataModel() {
	DataTestExpressionDataModel model 
		= new DataTestExpressionDataModel();
	model.setExpressionType(getExpressionType());
	model.setExpressionNum(getExpressionNum());
	model.setLeftSideIDNum(getLeftSideIDNum());
	model.setLeftSideType(getLeftSideType());
	model.setRightSideIDNum(getRightSideIDNum());
	model.setRightSideType(getRightSideType());
	model.setOperator(getOperator());
	model.setProperties(getProperties());
	return model;
}

/**
Returns a string representation of this object, suitable for debugging.
@return a string representation of this object.
*/
public String toString() {
	return "DataTestExpression {\n" + 
		  "Operator: " + __operator + "'\n"
		+ "Type: " + __expressionType + "\n"
		+ "LHNum: " + __leftSideIDNum + "\n"
		+ "LHType: " + __leftSideType + "\n"
		+ "RHNum: " + __rightSideIDNum + "\n"
		+ "RHType: " + __rightSideType + "\n}\n";
}

/**
Transfers data from the data model into this object.
@param model the model from which to transfer data.
@throws Exception if not all the necessary data values were set in the 
model.
*/
private void transferValuesFromDataModel(DataTestExpressionDataModel model)
throws Exception {
	if (model == null) {
		// allow the creation of a blank Expression object, although
		// the field values must be set in other code.
		return;
	}

	if (!model.checkAllSet()) {
		throw new Exception("Not all data values were read in and "
			+ "set in the data model: "
			+ model.getMissingValues());
	}
	setExpressionType(model.getExpressionType());
	setExpressionNum(model.getExpressionNum());
	setLeftSideIDNum(model.getLeftSideIDNum());
	setLeftSideType(model.getLeftSideType());
	setRightSideIDNum(model.getRightSideIDNum());
	setRightSideType(model.getRightSideType());
	setOperator(model.getOperator());
	setProperties(model.getProperties());
}

/**
Writes this object to the IO interface.
@param io the AlertIOInterface to use for writing the data.
@return the ID number of the expression that was written, or -1 if the 
expression was not successfully written.
*/
public int write(AlertIOInterface io) 
throws Exception {
	return io.writeDataTestExpressionDataModel(toDataModel());
}

/*
Adding a new Expression:
- create class
- add to build.xml
- add constant (TYPE_XXX) to DataTestExpression
- add code to DataTestSide.copyExpression()
- add code to DataTestSideFactory.getDataTestSide()
- add code to DataTestExpression.getExpressionName()
- add code to DataTestExpression.getExpressionType()
*/
}
