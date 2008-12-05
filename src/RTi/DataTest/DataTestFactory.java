// ----------------------------------------------------------------------------
// DataTestFactory - static class that creates DataTests from equation strings.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-04-06	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.io.ByteArrayInputStream;

import java.util.List;
import java.util.Vector;

import RTi.DataTest.ExpressionParser.ASTDouble;
import RTi.DataTest.ExpressionParser.ASTFunction;
import RTi.DataTest.ExpressionParser.ASTInteger;
import RTi.DataTest.ExpressionParser.ASTStart;
import RTi.DataTest.ExpressionParser.DataTestExpressionParser;
import RTi.DataTest.ExpressionParser.SimpleNode;

import RTi.DMI.DMIUtil;

import RTi.TS.TSSupplier;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTimeFormat;
import RTi.Util.Time.TimeInterval;

/**
This class is a static factory that aids in the creation of DataTests.  
DataTests can either be made by specifying an equation string that will be
evaluated by the DataTest, or they can be read from a persistent data source.
*/
public class DataTestFactory {

/**
The AlertIOInterface to use for reading data tests.  This does not need to be
set if DataTests will be returned from buildDataTest(), but if getDataTest()
will be called this needs to be set.
*/
private static AlertIOInterface __ioInterface = null;

/**
Whether the DataTestExpressionParser has already been initialized or not.
*/
private static boolean __parserInitialized = false;

/**
Used to refer to the left- or right-side node in a tree as the tree is being
walked.
*/
private static int 
	__LEFT = 0, 
	__RIGHT = 1;

/**
Constructs a DataTest given a DataTest equation string.
@param equation the equation string (e.g., "GetTSValue(*) &gt; 50") from which
to build the DataTest.
@param supplier the TSSupplier to use for getting time series data.
@return the DataTest that was constructed.
@throws Exception if any error occurs in the creation of the DataTest. 
*/
public static DataTest buildDataTest(String equation, TSSupplier supplier) 
throws Exception {
	String routine = "DataTestFactory.buildDataTest";

	// The parser requires an input stream, not a String, so the equation
	// String must be placed into an input stream.
	ByteArrayInputStream stream = new ByteArrayInputStream(
		equation.getBytes());

	// The parse action has its own try{} block so that errors can be 
	// reported with information as to exactly where the error occurred.  
	// The default exception information returned from the parse attempt 
	// is very verbose and not suited to information for users.
	
	ASTStart startNode = null;
	
	try {
		// Create the parser and attempt to parse the equation.  Any 
		// error in parsing will result in an Exception.  
		if (!__parserInitialized) {
			__parserInitialized = true;
		}
		else {
			DataTestExpressionParser.ReInit(stream);
		}
	}
	catch (Exception e) {
		// log the full parse error at level 3, but throw a separate
		// exception more suited to user information.
		Message.printWarning(3, routine, e);
		throw new Exception("An error occurred while attempting to "
			+ "parse the equation '" + equation + "'.");
	}

	// Parse the string and return a pointer to the head of the parse tree,
	// which is a Start node.
	startNode = DataTestExpressionParser.Start();	

	// Build the DataTestExpression object that is represented by the
	// parse tree.
	DataTestExpression expr = buildDataTestExpression(
		(SimpleNode)startNode.jjtGetChild(0), supplier);
	DataTest test = new DataTest();
	
	// The default action is a DummyAction with the ID # of MISSING_INT
	int[] actionNums = new int[1];
	actionNums[0] = DMIUtil.MISSING_INT;
	test.setActionNums(actionNums);

	DummyAction dummyAction = new DummyAction(null);
	dummyAction.setActionNum(DMIUtil.MISSING_INT);

	// Connect the data test to the dummy action.
	List actions = new Vector();
	actions.add(dummyAction);
	test.connectActions(actions);
	
	// Activate the test so that it can be run.
	test.setDataTestExpression(expr);

	// Set a default test base time and format
	DateTimeFormat format = new DateTimeFormat("hh:MM");
	test.setTestBaseTimeFormat(format);
	test.setTestBaseTime(format.parse("00:00"));

	// Set a default test interval
	test.setTestInterval(TimeInterval.parseInterval("1hour"));
	test.setEvaluationWindow(TimeInterval.parseInterval("1hour"));
	test.setEvaluationInterval(TimeInterval.parseInterval("1hour"));

	test.setIsActive(true);

	return test;
}


/**
The first step of building the DataTest from the parse tree generated by the 
parser.  This builds the DataTest's main DataTestExpression.
@param node the node underneath the Start node in the parse tree.
@param supplier the TSSupplier to set in all the DataTestFunction objects.
@return the DataTestExpression that is created.
@throws Exception if an error occurs, or if the equation was not an expression 
(e.g., it was just a constant value with no operators).
*/
private static DataTestExpression buildDataTestExpression(SimpleNode node, 
TSSupplier supplier) 
throws Exception {
	int id = node.getID();
	String op = node.getOp();
	DataTestExpression expr = null;

	// based on the node ID, build the expression for the expression tree.
	switch (id) {
		case DataTestExpressionParser.JJTMULT:
			if (op.equals("*")) {
				expr = new DataTestExpression_Multiply();	
				expr.setExpressionType(
					DataTestExpression.TYPE_MULTIPLY);
			}
			else if (op.equals("/")) {
				expr = new DataTestExpression_Divide();	
				expr.setExpressionType(
					DataTestExpression.TYPE_DIVIDE);
			}
			break;
		case DataTestExpressionParser.JJTADD:
			if (op.equals("-")) {
				expr = new DataTestExpression_Subtract();	
				expr.setExpressionType(
					DataTestExpression.TYPE_SUBTRACT);
			}
			else if (op.equals("+")) {
				expr = new DataTestExpression_Add();	
				expr.setExpressionType(
					DataTestExpression.TYPE_ADD);
			}
			break;		
		case DataTestExpressionParser.JJTLOGICAL:
			if (op.equals("||")) {
				expr = new DataTestExpression_Or();	
				expr.setExpressionType(
					DataTestExpression.TYPE_OR);
			}
			else if (op.equals("&&")) {
				expr = new DataTestExpression_And();	
				expr.setExpressionType(
					DataTestExpression.TYPE_AND);
			}
			else if (op.equals("==")) {
				expr = new DataTestExpression_Equals();
				expr.setExpressionType(
					DataTestExpression.TYPE_EQUALS);
			}
			else if (op.equals("!=")) {
				expr = new DataTestExpression_NotEqual();
				expr.setExpressionType(
					DataTestExpression.TYPE_NOTEQUAL);
			}
			break;		
		case DataTestExpressionParser.JJTCOMP:
			if (op.equals(">")) {
				expr = new DataTestExpression_GreaterThan();
				expr.setExpressionType(
					DataTestExpression.TYPE_GREATERTHAN);
			}
			else if (op.equals(">=")) {
				expr = new 
				    DataTestExpression_GreaterThanOrEqualTo(
				    	);	
				expr.setExpressionType(
					DataTestExpression
					.TYPE_GREATERTHANOREQUALTO);
			}			
			else if (op.equals("<")) {
				expr = new DataTestExpression_LessThan();	
				expr.setExpressionType(
					DataTestExpression.TYPE_LESSTHAN);
			}
			else if (op.equals("<=")) {
				expr = new 
				    DataTestExpression_LessThanOrEqualTo();	
				expr.setExpressionType(
					DataTestExpression
					.TYPE_LESSTHANOREQUALTO);
			}			
			break;
		default:
			throw new Exception("The main equation was not an "
				+ "expression.");
	}

	expr.setOperator(op);

	// recurse through the tree building the DataTestFunction or
	// DataTestExpression objects for each node.
	SimpleNode lhs = (SimpleNode)node.jjtGetChild(0);
	buildDataTestSideTree(expr, lhs, __LEFT, supplier);
	SimpleNode rhs = (SimpleNode)node.jjtGetChild(1);
	buildDataTestSideTree(expr, rhs, __RIGHT, supplier);
	return expr;
}

/**
Builds the tree of DataTestSide objects (DataTestFunctions and 
DataTestExpressions) that make up the DataTest's expression tree.
@param parentExpr the parent expression in which to place the DataTestSide
objects created.
@param node the node for which to build the DataTestSide objects.
@param side the side (__LEFT or __RIGHT) of the parent expression into which
to place the objects that are created.
@param supplier the TSSupplier to use for getting data for functions.
@throws Exception if an error occurs.
*/
private static void buildDataTestSideTree(DataTestExpression parentExpr, 
SimpleNode node, int side, TSSupplier supplier)
throws Exception {
	boolean neg = false;
	DataTestExpression expr = null;
	DataTestFunction func = null;
	int id = node.getID();
	int type = -1;
	String op = node.getOp();

	// negative numbers are expressed in the parser as a NEGATIVE node with
	// a single child node (a DOUBLE or INTEGER).  This is a result of how
	// values are parsed, and must be handled as follows:
	// - immediately get the child node (which will be a DOUBLE or an 
	//	INTEGER)
	// - set the boolean 'neg' to true to denote that the value of the
	// 	constant must be multiplied by -1
	// - handle it as a normal parse of a DOUBLE or INTEGER node.
	if (id == DataTestExpressionParser.JJTNEGATIVE) {
		node = (SimpleNode)node.jjtGetChild(0);
		id = node.getID();
		neg = true;
	}

	String[] ids = null;
	List params = null;

	switch (id) {
		case DataTestExpressionParser.JJTINTEGER:
			func = new DataTestFunction_Constant();
			func.setFunctionName(
				DataTestFunction.getFunctionName(
				DataTestFunction.TYPE_CONSTANT));
			int ival = ((ASTInteger)node).getValue();
			if (neg) {
				ival *= -1;
			}
			ids = new String[1];
			ids[0] = "" + ival;
			func.setInputDataIDs(ids);
			func.setFunctionType(DataTestFunction.TYPE_CONSTANT);
			type = DataTestSide.FUNCTION;
			break;
		case DataTestExpressionParser.JJTDOUBLE:
			func = new DataTestFunction_Constant();
			func.setFunctionName(
				DataTestFunction.getFunctionName(
				DataTestFunction.TYPE_CONSTANT));
			double dval = ((ASTDouble)node).getValue();
			if (neg) {
				dval *= -1;
			}
			ids = new String[1];
			ids[0] = "" + dval;
			func.setInputDataIDs(ids);
			func.setFunctionType(DataTestFunction.TYPE_CONSTANT);
			type = DataTestSide.FUNCTION;
			break;
		case DataTestExpressionParser.JJTFUNCTION:
			String name = ((ASTFunction)node).getName();
			int funcType = DataTestFunction.getFunctionType(name);
			if (funcType == DataTestFunction.TYPE_GETTSVALUE) {
				func = new DataTestFunction_GetTSValue();
			}
			else if (funcType == DataTestFunction.TYPE_ISMISSING) {
				func = new DataTestFunction_IsMissing();
			}
			else if (funcType == DataTestFunction.TYPE_TSDELTA) {
				func = new DataTestFunction_TSDelta();
			}
			func.setFunctionType(funcType);
			func.setFunctionName(name);
			params = ((ASTFunction)node).getParams();
			if (params != null) {
				ids = new String[params.size()];
				for (int k = 0; k < ids.length; k++) {
					ids[k] = (String)params.get(k);
				}
			}
			func.setInputDataIDs(ids);
			type = DataTestSide.FUNCTION;
			break;
		case DataTestExpressionParser.JJTMULT:
			if (op.equals("*")) {
				expr = new DataTestExpression_Multiply();
				expr.setExpressionType(
					DataTestExpression.TYPE_MULTIPLY);
			}
			else if (op.equals("/")) {
				expr = new DataTestExpression_Divide();
				expr.setExpressionType(
					DataTestExpression.TYPE_DIVIDE);
			}
			type = DataTestSide.EXPRESSION;
			break;
		case DataTestExpressionParser.JJTADD:
			if (op.equals("-")) {
				expr = new DataTestExpression_Subtract();	
				expr.setExpressionType(
					DataTestExpression.TYPE_SUBTRACT);
			}
			else if (op.equals("+")) {
				expr = new DataTestExpression_Add();
				expr.setExpressionType(
					DataTestExpression.TYPE_ADD);
			}
			type = DataTestSide.EXPRESSION;
			break;		
		case DataTestExpressionParser.JJTLOGICAL:
			if (op.equals("||")) {
				expr = new DataTestExpression_Or();
				expr.setExpressionType(
					DataTestExpression.TYPE_OR);
			}
			else if (op.equals("&&")) {
				expr = new DataTestExpression_And();
				expr.setExpressionType(
					DataTestExpression.TYPE_AND);
			}
			else if (op.equals("==")) {
				expr = new DataTestExpression_Equals();
				expr.setExpressionType(
					DataTestExpression.TYPE_EQUALS);
			}
			else if (op.equals("!=")) {
				expr = new DataTestExpression_NotEqual();
				expr.setExpressionType(
					DataTestExpression.TYPE_NOTEQUAL);
			}
			type = DataTestSide.EXPRESSION;
			break;		
		case DataTestExpressionParser.JJTCOMP:
			if (op.equals(">")) {
				expr 
				    = new DataTestExpression_GreaterThan();	
				expr.setExpressionType(
					DataTestExpression.TYPE_GREATERTHAN);
			}
			else if (op.equals(">=")) {
				expr = new 
				    DataTestExpression_GreaterThanOrEqualTo();
				expr.setExpressionType(
					DataTestExpression
					.TYPE_GREATERTHANOREQUALTO);
			}			
			else if (op.equals("<")) {
				expr = new DataTestExpression_LessThan();	
				expr.setExpressionType(
					DataTestExpression.TYPE_LESSTHAN);
			}
			else if (op.equals("<=")) {
				expr = new 
				    DataTestExpression_LessThanOrEqualTo();	
				expr.setExpressionType(
					DataTestExpression
					.TYPE_LESSTHANOREQUALTO);
			}			
			type = DataTestSide.EXPRESSION;
			break;
		default:		
			throw new Exception("Unknown node type: '" + id + "'");
	}

	if (type == DataTestSide.EXPRESSION) {
		expr.setOperator(op);
		SimpleNode lhs = (SimpleNode)node.jjtGetChild(0);
		SimpleNode rhs = (SimpleNode)node.jjtGetChild(1);
		buildDataTestSideTree(expr, lhs, __LEFT, supplier);
		buildDataTestSideTree(expr, rhs, __RIGHT, supplier);
	}
	else {
		func.setTSSupplier(supplier);
	}

	if (side == __LEFT) {
		parentExpr.setLeftSideType(type);
		if (type == DataTestSide.FUNCTION) {
			parentExpr.setLeftSide(func);
		}
		else {
			parentExpr.setLeftSide(expr);
		}
	}
	else {
		parentExpr.setRightSideType(type);
		if (type == DataTestSide.FUNCTION) {
			parentExpr.setRightSide(func);
		}
		else {
			parentExpr.setRightSide(expr);
		}
	}
}

/**
Returns all the data tests in the system.
@return all the data tests in the system.
*/
public static List getAllDataTests() 
throws Exception {
	DataTestDataModel model = null;
	List models = __ioInterface.readAllDataTestDataModels();
	int size = models.size();

	DataTest test = null;
	DataTestSide mainExpr = null;

	List tests = new Vector();
	
	for (int i = 0; i < size; i++) {
		model = (DataTestDataModel) models.get(i);
		if (model == null) {
			continue;
		}
		
		test = new DataTest(model);
	
		mainExpr = DataTestSideFactory.getDataTestSide(
			DataTestSide.EXPRESSION, 
			test.getDataTestExpressionNum());

		test.setDataTestExpression((DataTestExpression) mainExpr);

		tests.add(test);
	}

	return tests;
}

/**
Gets the data test with the given unique identifier from the AlertIOInterface
set in this factory.  setTestData() will need to be called on the data test 
once it has been generated.
@param dataTestNum the unique identifier of the data test to read.
@return the DataTest with the given identifier.
*/
public static DataTest getDataTest(int dataTestNum) 
throws Exception {
	DataTestDataModel model = __ioInterface.readDataTestDataModel(
		dataTestNum);
	if (model == null) {
		return null;
	}
	DataTest test = new DataTest(model);
	
	DataTestSide mainExpr = DataTestSideFactory.getDataTestSide(
		DataTestSide.EXPRESSION, test.getDataTestExpressionNum());

	test.setDataTestExpression((DataTestExpression)mainExpr);

	return test;
}

/**
Sets the IO interface to use for reading data tests from a persistent source
in getDataTest().
@param ioInterface the IO Interface to set.
*/
public static void setIOInterface(AlertIOInterface ioInterface) {
	__ioInterface = ioInterface;
}

}
