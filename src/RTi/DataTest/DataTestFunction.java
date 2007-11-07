// ----------------------------------------------------------------------------
// DataTestFunction - the abstract base class for all functions.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-13	J. Thomas Sapienza, RTi	Initial version.
// 2006-04-14	JTS, RTi		evaluatePositiveResult() now takes
//					a DataTest parameter.
// 2006-05-03	JTS, RTi		* The AlertIOInterface is now passed 
//					  into write(), rather than being 
//   					  stored in the class.
//					* write() now returns the ID number of
//					  the object that was written, rather 
//					  than a boolean.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.DMI.DMIUtil;

import RTi.TS.TS;
import RTi.TS.TSSupplier;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
This class is the abstract base class from which all other Function classes
are built.
*/
public abstract class DataTestFunction 
extends DataTestSide {

/**
The types of functions supported.
*/
public static final int
	TYPE_UNKNOWN = -1,
	TYPE_CONSTANT = 1,
	TYPE_GETTSVALUE = 2,
	TYPE_TSDELTA = 3,
	TYPE_ISMISSING = 4,
	TYPE_ISNOTMISSING = 5;

/**
The positions in the InputDataID list at which TSIDs should be found.
*/
private int[] __tsidPositions = null;

/**
The ID Number of the function.
*/
private int __functionNum = DMIUtil.MISSING_INT;

/**
The type of the function (e.g., TYPE_CONSTANT).
*/
private int __functionType = DMIUtil.MISSING_INT;

/**
The number of parameters that the function should have.  This is used to ensure
that all the input parameters have been set.
*/
private int __numParams = 0;

/**
Additional properties for the function.
*/
private PropList __properties = null;

/**
An array of input data IDs -- most likely TSIDs.
*/
private String[] __inputDataIDs = null;

/**
The name of the function.
*/
private String __functionName = DMIUtil.MISSING_STRING;

/**
The interval of evaluation for the function.
*/
private TimeInterval __evaluationInterval = null;

/**
The window of evaluation for the function.
*/
private TimeInterval __evaluationWindow = null;

/**
The time series from which to read values.
*/
private TS[] __ts = null;

/**
The time series supplier.
*/
private TSSupplier __tsSupplier = null;

/**
Empty constructor for a function created in the DataTestFactory.  Only the 
number of parameters the function takes is specified.
@param numParams the number of parameters the function requires.
*/
public DataTestFunction(int numParams) 
throws Exception {
	this(null, numParams);
}

/**
Constructor.
@param model the DataTestFunctionDataModel from which the data in this function
will be taken.
@param numParams the number of parameters the function takes.
@throws Exception if an error occurs.
*/
public DataTestFunction(DataTestFunctionDataModel model, int numParams) 
throws Exception {
	if (model != null) {
		transferValuesFromDataModel(model);
	}
	setNumParams(numParams);
}

/**
Copy constructor.
@param f the DataTestFunction to copy.
*/
public DataTestFunction(DataTestFunction f) {
	super(f);

	if (f.__tsidPositions != null) {
		__tsidPositions = new int[f.__tsidPositions.length];
		for (int i = 0; i < f.__tsidPositions.length; i++) {
			__tsidPositions[i] = f.__tsidPositions[i];
		}
	}

	__functionNum = f.__functionNum;

	__numParams = f.__numParams;

	if (f.__properties != null) {
		__properties = new PropList(f.__properties);
	}

	if (f.__inputDataIDs != null) {
		__inputDataIDs = new String[f.__inputDataIDs.length];
		for (int i = 0; i < f.__inputDataIDs.length; i++) {
			__inputDataIDs[i] = f.__inputDataIDs[i];
		}
	}

	__functionName = f.__functionName;

	if (f.__evaluationInterval != null) {
		__evaluationInterval = new TimeInterval(
			f.__evaluationInterval);
	}

	if (f.__evaluationWindow != null) {
		__evaluationWindow = new TimeInterval(
			f.__evaluationInterval);
	}

	if (f.__ts != null) {
		__ts = new TS[f.__ts.length];
		for (int i = 0; i < f.__ts.length; i++) {
			__ts[i] = f.__ts[i];
		}
	}

	__tsSupplier = f.__tsSupplier;
}

/**
Builds a positive message for this function for a DataTestResult after it has
been determined (via evaluate()) that the function is part of an expression
that returns a positive result.<p>
Several variables are recognized in the function's message value that will be 
replaced with values:<p>
<b>$DATETIME</b> -- replaced with the dateTime value passed to this method.<p>
<b>$TSID[X]</b> -- replaced with the TSID of the time series that the 
function operates on, supporting up to 10 different (0-9) TSIDs.  In the bold
text, X is a number from 0-9.
<b>$VALUE</b> -- replaced with the evaluated value of the function.<p>
These variables are case-sensitive, so do not type (for example) "$datetime".
@param dateTime the dateTime for which to build the positive message.
@return the message string generated for the function call at the given time.
*/
public String buildPositiveResultMessage(DateTime dateTime) {
	String message = getPositiveMessage();
	if (DMIUtil.isMissing(message)) {
		message = getDefaultPositiveMessage();
	}

	for (int i = 0; i < 10; i++) {
		if (message.indexOf("$TSID[" + i + "]") > -1) {
			//##mt 2006-10-06
			//TS tsi = getTS(i);
			//if( tsi ) {
			message = StringUtil.replaceString(message, 
				"$TSID[" + i + "]",
				getTS(i).getIdentifier().toString()); //use tsi instead of getTS(i)
			//}
		}
	}
	
	if (message.indexOf("$VALUE") > -1) {
		message = StringUtil.replaceString(message, "$VALUE",
			"" + getLastResult());
	}	
	
	if (message.indexOf("$DATETIME") > -1) {
		
		//##mt 2006-10-06
		//TS tszero = getTS(0);
		//if( tszero ) {
		//	dateTime.setPrecision( getTS(0).getDataIntervalBase() );
		//}
		
		message = StringUtil.replaceString(message, "$DATETIME",
			dateTime.toString());
	}		
	
	return message;
}

/**
Checks to make sure the function has the proper number of parameters.
@return a String describing the problem with the function if it has the
wrong number of parameters, or null if the function is OK.  The string will
be in the format:<p>
   Function 'FunctionName' takes X parameters, not Y (Param1, Param2, ...).
*/
public String checkFunctionParameters() {
	int size = 0;
	if (__inputDataIDs != null) {
		size = __inputDataIDs.length;
	}

	String s = null;

	if (size != __numParams) {
		String plural = "s";
		if (__numParams == 1) {
			plural = "";
		}
		
		s = "Function '" + __functionName + "' takes " + __numParams
			+ " parameter" + plural + ", not " + size;
		if (size > 0) {
			s += " (";
			for (int i = 0; i < size; i++) {
				s += __inputDataIDs[i];
				if (i < size - 1) {
					s += ", ";
				}
			}
			s += ").";
		}
		else {
			s += ".";
		}
	}

	return s;
}

/**
Overridden as it is declared abstract in DataTestSide, though it's only 
applicable to DataTestExpression objects.  Does nothing, as nothing needs to 
be done for a function.  Declared in DataTestSide to be more generic when 
calling it on an expression tree.
*/
public void evaluatePositiveResult(DataTest test, Vector results, 
DateTime testTime, int level) {
	// defined in DataTestSide so no casting need be done on any side,
	// but will only ever be called on Expression objects, so should not
	// do anything for functions.
	return;
}

/**
Cleans up member variables.
@throws Throwable if an error occurs.
*/
public void finalize() 
throws Throwable {
	__tsidPositions = null;
	__properties = null;
	IOUtil.nullArray(__inputDataIDs);
	__functionName = null;
	__evaluationInterval = null;
	__evaluationWindow = null;
	IOUtil.nullArray(__ts);
	__tsSupplier = null;
 	super.finalize();
}


/**
Returns the function name given the function type (e.g., TYPE_CONSTANT).  If
an invalid type is passed in, null will be returned.
@return the function name given the function type (e.g., TYPE_CONSTANT).  If
an invalid type is passed in, null will be returned.
*/
public static String getFunctionName(int type) {
	if (type == TYPE_CONSTANT) {
		return "constant";
	}
	else if (type == TYPE_GETTSVALUE) {
		return "getTSValue";
	}
	else if (type == TYPE_ISMISSING) {
		return "isMissing";
	}
	else if (type == TYPE_ISNOTMISSING) {
		return "isNotMissing";
	}	
	else if (type == TYPE_TSDELTA) {
		return "tsDelta";
	}
	else {
		return null;
	}
}

/**
Returns the function type given the function name (e.g., "constant").  If an
invalid or null name is passed in, TYPE_UNKNOWN will be returned.
@return the function type given the function name (e.g., "constant").  If an
invalid or null name is passed in, TYPE_UNKNOWN will be returned.
*/
public static int getFunctionType(String name) {
	if (name == null) {
		return TYPE_UNKNOWN;
	}
	else if (name.equalsIgnoreCase("constant")) {
		return TYPE_CONSTANT;
	}
	else if (name.equalsIgnoreCase("getTSValue")) {
		return TYPE_GETTSVALUE;
	}
	else if (name.equalsIgnoreCase("isMissing")) {
		return TYPE_ISMISSING;
	}
	else if (name.equalsIgnoreCase("isNotMissing")) {
		return TYPE_ISNOTMISSING;
	}	
	else if (name.equalsIgnoreCase("tsDelta")) {
		return TYPE_TSDELTA;
	}
	else {
		return TYPE_UNKNOWN;
	}
}

/**
Returns the default positive result message.
@return the default positive result message.
*/
public abstract String getDefaultPositiveMessage();

/**
Returns the evaluation interval.
@return the evaluation interval.
*/
public TimeInterval getEvaluationInterval() {
	return __evaluationInterval;
}

/** 
Returns the evaluation window.
@return the evaluation window.
*/
public TimeInterval getEvaluationWindow() {
	return __evaluationWindow;
}

/**
Returns the function name.
@return the function name.
*/
public String getFunctionName() {
	return __functionName;
}

/**
Returns the ID Number.
@return the ID Number.
*/
public int getFunctionNum() {
	return __functionNum;
}

/**
Returns the type of function.
@return the type of function.
*/
public int getFunctionType() {	
	return __functionType;
}

/**
Returns the Input Data ID at the specified position.
@return the Input Data ID at the specified position.
*/
public String getInputDataID(int pos) {
	return __inputDataIDs[pos];
}

/**
Returns the Input Data IDs.
@return the Input Data IDs.
*/
public String[] getInputDataIDs() {
	return __inputDataIDs;
}

/**
Returns the number of Input Data IDs.
@return the number of Input Data IDs.
*/
public int getNumInputDataIDs() {
	if (__inputDataIDs == null) {
		return 0;
	}
	else {
		return __inputDataIDs.length;
	}
}

/**
Returns the number of parameters the function should have.
@return the number of parameters the function should have.
*/
public int getNumParams() {
	return __numParams;
}

/**
Returns the additional properties.
@return the additional properties.
*/
public PropList getProperties() {
	return __properties;
}

/**
Returns the function's time series.
@param pos the position from which to return the time series (base-0).
@return the function's time series.
*/
public TS getTS(int pos) {
	if (__ts == null) {
		return null;
	}
	return __ts[pos];
}

/**
Returns the number of time series in the function.
@return the number of time series in the function.
*/
public int getNumTS() {
	if (__ts == null) {
		return 0;
	}
	else {
		return __ts.length;
	}
}

/**
Returns the positions in the InputDataID array at which TSIDs are found.
@return the positions in the InputDataID array at which TSIDs are found.
*/
public int[] getTSIDPositions() {
	return __tsidPositions;
}

/**
Returns the TSSupplier.
@return the TSSupplier.
*/
public TSSupplier getTSSupplier() {
	return __tsSupplier;
}

/**
Returns false.
@return false.
*/
public boolean isExpression() {
	return false;
}

/**
Returns true.
@return true.
*/
public boolean isFunction() {
	return true;
}

/**
Sets the evaluation interval.
@param evaluationInterval the evaluation interval to set.
*/
public void setEvaluationInterval(TimeInterval evaluationInterval) {
	__evaluationInterval = evaluationInterval;
}

/**
Sets the evaluation window.
@param evaluationWindow the evaluation window to set.
*/
public void setEvaluationWindow(TimeInterval evaluationWindow) {
	__evaluationWindow = evaluationWindow;
}

/**
Sets the function name.
@param functionName the function name to set.
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
Sets the type of function.
@param functionType the type of function.
*/
public void setFunctionType(int functionType) {
	__functionType = functionType;
}

/**
Sets the additional properties.
@param properties the additonal properties to set.
*/
public void setProperties(PropList properties) {
	__properties = properties;
}

/**
Sets the Input Data ID at the specified position.
@param id the Input Data ID at the specified position.
@param pos the position in which to set the ID.
*/
public void setInputDataID(String id, int pos) {
	__inputDataIDs[pos] = id;
}

/**
Sets the input data IDs.
@param dataIDs the input data IDs to set.
*/
public void setInputDataIDs(String[] dataIDs) {
	__inputDataIDs = dataIDs;
}

/**
Sets the number of parameters the function should have.  Should be called
by the derived class constructor.
@param numParams the number of parameters the function should have.
*/
public void setNumParams(int numParams) {
	__numParams = numParams;
}

/**
Sets the number of time series that the function will need to handle, and 
initializes the __ts array to hold that number of time series.
@param num the number of time series that the function needs to be able to
handle.
*/
public void setNumTS(int num) {
	__ts = new TS[num];
}

/**
Implemented in derived classes, this sets up the data based on the data IDs
passed in.
@param startDate the first date for which to set test data.  Can be null.
@param endDate the last date for which to set test data.  Can be null.
@return true if the data were set up properly, false if not.
*/
public abstract boolean setTestData(DateTime startDate, DateTime endDate);

/**
Sets the time series.
@param pos the position in the ts array at which to set the TS.
@param ts the time series to set.
*/
public void setTS(TS ts, int pos) {
	__ts[pos] = ts;
}

/**
Sets the positions in the InputDataID array at which TSIDs are found.
@param tsidPositions the positions in the InputDataID array at which TSIDs 
are found.
*/
public void setTSIDPositions(int[] tsidPositions) {
	__tsidPositions = tsidPositions;
}

/**
Sets the TS Supplier to use for getting time series.
@param tsSupplier the supplier to use.
*/
public void setTSSupplier(TSSupplier tsSupplier) {
	__tsSupplier = tsSupplier;
}

/**
Returns a DataTestFunctionDataModel for the data in this object.
@return a DataTestFunctionDataModel for the data in this object.
*/
public DataTestFunctionDataModel toDataModel() {
	DataTestFunctionDataModel model 
		= new DataTestFunctionDataModel();
	model.setFunctionNum(getFunctionNum());
	model.setFunctionType(getFunctionType());
	model.setEvaluationInterval(getEvaluationInterval());
	model.setEvaluationWindow(getEvaluationWindow());
	model.setFunctionName(getFunctionName());
	model.setProperties(getProperties());
	model.setInputDataIDs(getInputDataIDs());
	model.setMessage(getPositiveMessage());
	return model;
}

/**
Returns a string representation of the function data.
@return a string representation of the function data.
*/
public String toString() {
	String s = "Function: '" + __functionName + "'\n"
		+ "Interval: '" + __evaluationInterval + "'\n"
		+ "Window:   '" + __evaluationWindow + "'\n";
	if (__inputDataIDs != null) {
		for (int i = 0; i < __inputDataIDs.length; i++) {
			s += "in[" + i + "]: '" + __inputDataIDs[i]
				+ "'\n";
		}
	}

	return s;
}

/**
Transfers data from the data model into the function object.
@param model the model from which to transfer data.
@throws Exception if some of the necessary data values were not set in the
data model.
*/
public void transferValuesFromDataModel(DataTestFunctionDataModel model)
throws Exception {
	if (model == null) {
		// allow the creation of a blank Function object, although
		// the field values must be set in other code.
		return;
	}

	if (!model.checkAllSet()) {
		throw new Exception("Not all data values were read in and "
			+ "set in the data model: "
			+ model.getMissingValues());
	}
	setEvaluationInterval(model.getEvaluationInterval());
	setEvaluationWindow(model.getEvaluationWindow());
	setFunctionName(model.getFunctionName());
	setProperties(model.getProperties());
	setInputDataIDs(model.getInputDataIDs());
	setPositiveMessage(model.getMessage());
	setFunctionType(model.getFunctionType());
}

/**
Writes the function to the IO Interface.
@param io the AlertIOInterface to use for writing.
@return the ID number of the function that was written, or -1 if the function 
was not successfully written.
*/
public int write(AlertIOInterface io) 
throws Exception {
	return io.writeDataTestFunctionDataModel(toDataModel());
}

}

/*
Adding a new Function:
- create class
- add to build.xml
- add constant (TYPE_XXX) to DataTestFunction
- add code to DataTestSide.copyFunction()
- add code to DataTestSideFactory.getDataTestSide()
- add code to DataTestFunction.getFunctionName()
- add code to DataTestFunction.getFunctionType()
*/
