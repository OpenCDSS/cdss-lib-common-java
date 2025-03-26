// DMIStoredProcedureData - class for storing stored procedure

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.DMI;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.util.List;

/**
A class that stores information on how in particular a stored procedure will
execute.  The following is an example of how to create and use this object, from HydroBaseDMI:<p>
<code>
	// Look up the definition of the stored procedure (stored in a
	// DMIStoredProcedureData object) in the hashtable.  This allows
	// repeated calls to the same stored procedure to re-used stored
	// procedure meta data without requerying the database.

	DMIStoredProcedureData data = (DMIStoredProcedureData)__storedProcedureHashtable.get(name);
	
	if (data != null) {
		// If a data object was found, set up the data in the statement below and return true
	}
	else if (data == null && DMIUtil.databaseHasStoredProcedure(this, name)) {
		// If no data object was found, but the stored procedure is
		// defined in the database then build the data object for the
		// stored procedure and then store it in the hashtable.
		data = new DMIStoredProcedureData(this, name);
		__storedProcedureHashtable.put(name, data);
	}
	else {
		// If no data object was found and the stored procedure is not
		// defined in the database, then use the original DMI code.
		// Return false to buildSQL() so it knows to continue executing.
		if (IOUtil.testing()) {
			Message.printStatus(2, "HydroBaseDMI.canSetUpStoredProcedure",
				"No stored procedure defined in database for SQL#: " + sqlNumber);
		}

		if (Message.isDebugOn) {
			Message.printDebug(30, "HydroBaseDMI.canSetUpStoredProcedure",
				"No stored procedure defined in database for SQL#: " + sqlNumber);
		}		
		return false;
	}

	if (Message.isDebugOn) {
		Message.printDebug(30, "HydroBaseDMI.canSetUpStoredProcedure",
			"Stored procedure '" + name + "' found and will be used.");
	}	

	if (IOUtil.testing() && debug) {
		DMIUtil.dumpProcedureInfo(this, name);
	}

	// Set the data object in the statement.  Doing so will set up the
	// statement as a stored procedure statement.

	statement.setStoredProcedureData(data);
</code>
*/
public class DMIStoredProcedureData {

/**
Whether the parameter can be null or not.
*/
private boolean[] __parameterNullable = null;

/**
Whether there is a return type, used with a procedure.
*/
private boolean __hasReturnValue = false;

/**
The object that will actually execute the stored procedure in the low level java code.
*/
private CallableStatement __callableStatement = null;

/**
The dmi used that this stored procedure will connect to the DB with.
*/
private DMI __dmi = null;

/**
The types of all the parameters in int type, corresponding to java.sql.Types.
*/
private int[] __parameterTypes = null;

/**
The number of parameters.
*/
private int __numParameters = 0;

/**
The type of the return value, from java.sql.Types.
*/
private int __returnType = -1;

/**
The names of all the parameters.
*/
private String[] __parameterNames = null;

/**
The types of all the parameters in readable String format.
*/
private String[] __parameterTypeStrings = null;

/**
The name of the stored procedure.
*/
private String __procedureName = null;

/**
The name of the return parameter.
*/
private String __returnName = null;

/**
The type of the return value, from java.sql.Types.
*/
private String __returnTypeString = null;

/**
Constructor.
@param dmi the dmi to use for getting stored procedure information.
@param procedureName the name of the procedure for which to query the 
database for information.
*/
public DMIStoredProcedureData(DMI dmi, String procedureName) 
throws Exception {
	__procedureName = procedureName;
	ResultSet rs = dmi.getConnection().getMetaData().getProcedureColumns(
		dmi.getDatabaseName(), null, procedureName, null);
	__dmi = dmi;
	List<List<Object>> v = DMIUtil.processResultSet(rs);
	//DMIUtil.printResults(v);
	rs.close();

	fillProcedureData(v);

	buildCallableStatement();
}

/**
Creates the callable statement that will be executed whenever this stored procedure is run.
*/
private void buildCallableStatement() 
throws Exception {
	__callableStatement = __dmi.getConnection().prepareCall(createStoredProcedureCallString());
		
	if (hasReturnValue()) {
		// Register the output value so that it can be retrieved to check the status
		// - note that the return type can vary so calling code needs to understand how to retrieve
		__callableStatement.registerOutParameter(1, getReturnType());
	}	
}

/**
Creates the string that will be passed to the DMI's connection in order to build the stored procedure.
The syntax will be {[?=]call procedureName[([parameter],[,[parameter]]...)]},
depending on whether a return value is used and number of parameters.
@return the "{call .... }" string.
*/
private String createStoredProcedureCallString() {
	String callString = "{";
	
	if (hasReturnValue()) {
		callString += "? = ";
	}
	
	callString += "call " + getProcedureName() + " (";

	int numParameters = getNumParameters();

	for (int i = 0; i < numParameters; i++) {
		if (i > 0) {
			callString += ", ";
		}
		callString+= "?";
	}

	callString += ") }";

	return callString;
}

/**
Pulls out information from the list of procedure data and populates the member variables.
*/
private void fillProcedureData(List<List<Object>> v) {
	__hasReturnValue = hasReturnValue(v);

	int size = v.size();
	__numParameters = size;
	if (__hasReturnValue) {
		__numParameters--;
	}

	__parameterNullable = new boolean[__numParameters];
	__parameterTypes = new int[__numParameters];
	__parameterNames = new String[__numParameters];
	__parameterTypeStrings = new String[__numParameters];

	Integer I = null;
	int count = 0;
	List<Object> row = null;
	for (int i = 0; i < size; i++) {
		row = v.get(i);

		I = (Integer)row.get(4);
		if (I.intValue() == DatabaseMetaData.procedureColumnReturn) {
			// this is a return value row
			__returnName = (String)row.get(3);
			__returnType = ((Integer)row.get(5)).intValue();
			__returnTypeString = (String)row.get(6);
		}
		else {
			__parameterNames[count] = (String)row.get(3);
			__parameterTypes[count] = ((Integer)row.get(5)).intValue();
			__parameterTypeStrings[count]= (String)row.get(6);
			if (((Integer)row.get(11)).intValue() == DatabaseMetaData.procedureNullable) {
				__parameterNullable[count] = true;
			}
			else {
				__parameterNullable[count] = false;
			}
			count++;
		}
	}
}

/**
Returns the callable statement that was created to run this stored procedure.
@return the callable statement that was created to run this stored procedure.
*/
public CallableStatement getCallableStatement() {
	return __callableStatement;
}

/**
Returns the number of parameters in the procedure.
@return the number of parameters in the procedure.
*/
public int getNumParameters() {
	return __numParameters;
}

/**
Returns the name of the parameter.
@return the name of the parameter.
*/
public String getParameterName(int parameterNum) {
	return __parameterNames[parameterNum];
}

/**
Returns the integer type (comparable to java.sql.Types) of the parameter.
@return the integer type of the parameter.
*/
public int getParameterType(int parameterNum) {
	return __parameterTypes[parameterNum];
}

/**
Returns the type string of the parameter.
@return the type string of the parameter.
*/
public String getParameterTypeString(int parameterNum) {
	return __parameterTypeStrings[parameterNum];
}

/**
Returns the procedure name.
@return the procedure name.
*/
public String getProcedureName() {
	return __procedureName;
}

/**
Returns the name of the return value, or null if there is no return value.
@return the name of the return value, or null if there is no return value.
*/
public String getReturnName() {
	return __returnName;
}

/**
Returns the type of the return value, or -1 if there is no return value.
@return the type of the return value, or -1 if there is no return value.
*/
public int getReturnType() {
	return __returnType;
}

/**
Returns the type string of the return value or null if there is no return value.
@return the type string of the return value or null if there is no return value.
*/
public String getReturnTypeString() {
	return __returnTypeString;
}

/**
Returns whether the parameter has a return value.
@return whether the parameter has a return value.
*/
public boolean hasReturnValue() {
	return __hasReturnValue;
}

/**
Checks procedure data to see if there are any return values.
@return true if any return values are present, false otherwise.
*/
private boolean hasReturnValue(List<List<Object>> v) {
	int size = v.size();

	Integer I = null;
	List<Object> row = null;
	for (int i = 0; i < size; i++) {
		row = v.get(i);

		I = (Integer)row.get(4);
		if (I.intValue() == DatabaseMetaData.procedureColumnReturn) {
			return true;
		}
	}
	return false;
}

/**
Returns whether a parameter can be null or not.
@return whether a parameter can be null or not.
*/
public boolean isParameterNullable(int parameterNum) {
	return __parameterNullable[parameterNum];
}

/**
Returns a String description of the stored procedure.
@return a String description of the stored procedure.
*/
public String toString() {
	String desc = "Stored Procedure:   '" + __procedureName + "'\n";
	desc += "  Has return value?  " + __hasReturnValue + "\n";
	if (__hasReturnValue) {
		desc += "  Return parameter: '" + __returnName + "'\n";
		desc += "  Return type:      '" + __returnTypeString + "'\n";
	}
	desc += "  Num parameters:    " + __numParameters + "\n";
	for (int i = 0; i < __numParameters; i++) {
		desc += "  [" + i + "] Name:         '" + __parameterNames[i] + "'\n";
		desc += "      Type:          " + __parameterTypeStrings[i] + " (" + __parameterTypes[i] + ")\n";
		desc += "      Nullable?      " + __parameterNullable[i] + "\n";
	}
	return desc;
}

}