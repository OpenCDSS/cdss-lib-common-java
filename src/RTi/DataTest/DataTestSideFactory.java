// ----------------------------------------------------------------------------
// DataTestFactory - A factory class for constructing DataTestSides.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-22	JTS, RTi		Added support for Less Than.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.TS.TSSupplier;

/**
This class is a factory for putting together DataTestSide objects based on 
data read from an IO Interface and stored in data models.
*/
public class DataTestSideFactory {

/**
The IO Interface to use for reading data.
*/
private static AlertIOInterface __ioInterface = null;

/**
The supplier that will provide time series.
*/
private static TSSupplier __tsSupplier = null;

/**
Generates a DataTestSide object.  If building a function, setTestData() is not
called on the side.  Once a DataTest containing the function has been built,
call its setTestData() method.
@param type the type of DataTestSide to build (DataTestSide.EXPRESSION or
DataTestSide.FUNCTION).
@param num the ID number of the DataTestSide to build.
@throws Exception if an error occurs.
*/
public static DataTestSide getDataTestSide(int type, int num) 
throws Exception {
	if (type == DataTestSide.EXPRESSION) {
		DataTestExpressionDataModel model 
			= __ioInterface.readDataTestExpressionDataModel(num);

		int exprType = DataTestExpression.getExpressionType(
			model.getOperator());
		DataTestExpression expr = null;

		if (exprType == DataTestExpression.TYPE_ADD) {
			expr = new DataTestExpression_Add(model);
		}
		else if (exprType == DataTestExpression.TYPE_AND) {
		    	expr = new DataTestExpression_And(model);
		}		
		else if (exprType == DataTestExpression.TYPE_DIVIDE) {
			expr = new DataTestExpression_Divide(model);
		}
		else if (exprType == DataTestExpression.TYPE_EQUALS) {
			expr = new DataTestExpression_Equals(model);
		}
		else if (exprType == DataTestExpression.TYPE_GREATERTHAN){
			expr = new DataTestExpression_GreaterThan(model);
		}
		else if (exprType 
		    == DataTestExpression.TYPE_GREATERTHANOREQUALTO) {
			expr = new DataTestExpression_GreaterThanOrEqualTo(
				model);
		}
		else if (exprType == DataTestExpression.TYPE_LESSTHAN) {
			expr = new DataTestExpression_LessThan(model);
		}
		else if (exprType 
		    == DataTestExpression.TYPE_LESSTHANOREQUALTO) {
			expr = new DataTestExpression_LessThanOrEqualTo(model);
		}
		else if (exprType == DataTestExpression.TYPE_MULTIPLY) {
			expr = new DataTestExpression_Multiply(model);
		}
		else if (exprType == DataTestExpression.TYPE_NOTEQUAL) {
			expr = new DataTestExpression_NotEqual(model);
		}
		else if (exprType == DataTestExpression.TYPE_OR) {
		    	expr = new DataTestExpression_Or(model);
		}
		else if (exprType == DataTestExpression.TYPE_SUBTRACT) {
			expr = new DataTestExpression_Subtract(model);
		}
		else {
			throw new Exception("Unknown expression type: "
				+ exprType + " " + model.toString());
		}
		expr.setExpressionType(exprType);
		expr.setLeftSide(DataTestSideFactory.getDataTestSide(
			expr.getLeftSideType(), 
			expr.getLeftSideIDNum()));
		expr.setRightSide(DataTestSideFactory.getDataTestSide(
			expr.getRightSideType(),
			expr.getRightSideIDNum()));
		return expr;
	}
	else if (type == DataTestSide.FUNCTION) {
		DataTestFunctionDataModel model 
			= __ioInterface.readDataTestFunctionDataModel(num);
		int funcType = model.getFunctionType();
		DataTestFunction func = null;
		if (funcType == DataTestFunction.TYPE_CONSTANT) {
			func = new DataTestFunction_Constant(model);
		}
		else if (funcType == DataTestFunction.TYPE_GETTSVALUE) {
		    	func = new DataTestFunction_GetTSValue(model);
		}
		else if (funcType == DataTestFunction.TYPE_ISMISSING) {
			func = new DataTestFunction_IsMissing(model);
		}
		else if (funcType == DataTestFunction.TYPE_ISNOTMISSING) {
			func = new DataTestFunction_IsNotMissing(model);
		}		
		else if (funcType == DataTestFunction.TYPE_TSDELTA) {
			func = new DataTestFunction_TSDelta(model);
		}
		else {
			throw new Exception("Unknown function type: "
				+ funcType);
		}
		func.setFunctionType(funcType);
		func.setTSSupplier(__tsSupplier);
		return func;
	}
	else {
		throw new Exception("Unknown side type: " + type);
	}
}

/**
Sets the IO Interface to use for reading data into data models.
@param ioInterface the IO Interface to use.
*/
public static void setIOInterface(AlertIOInterface ioInterface) {
	__ioInterface = ioInterface;
}

/**
Sets the TSSupplier that will provide time series.
@param supplier the supplier to use.
*/
public static void setTSSupplier(TSSupplier supplier) {
	__tsSupplier = supplier;
}

}
