// DMIStatement - base class for statements

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeZoneDefaultType;
import RTi.Util.String.StringUtil;

/**
The DMIStatement class stores basic information about SQL statements.  It serves
as the base class for more specific statements.
*/
public class DMIStatement {

/**
Flags to note what kind of JOIN (if any) the statement is.
*/
protected final int
	_JOIN_INNER = 0,
	_JOIN_LEFT = 1,
	_JOIN_RIGHT = 2;

/**
Whether the statement is executing via a stored procedure.
*/
protected boolean _isSP = false;

/**
The object that will actually execute the stored procedure in the low level java code.
*/
protected CallableStatement __storedProcedureCallableStatement = null;

/**
DMI that will execute the query.  It is used to determine the database type for formatting statements.
*/
protected DMI _dmi;

/**
The object that holds information about the stored procedure, if one is
being used.  If this query will not use a stored procedure, this is null.
*/
protected DMIStoredProcedureData _spData = null;

/**
A counter for stored procedures that tracks the parameter being set via
a setValue() call.  This is necessary in order to be able to use the existing
write.*() methods in DMIs and have them work with both stored procedures and 
the old style dmi statements.  As addValue() statements are called, the
counter is incremented so the stored procedure knows the position into which
the value placed by the next addValue() statement should go.
*/
private int __paramNum;

/**
Array of the parameters set in a stored procedure, for use in printing the
stored procedure out in executable format when a query is run.
*/
private String[] __spParameters = null;

/**
List for fields that are auto-numbers
*/
protected List<String> _autonumber_Vector;

/**
List for fields used in the statement (e.g., SELECT XXXX, XXXX).
*/
protected List<String> _field_Vector;

/**
List to specify the tables for joins.
*/
protected List<String> _join_Vector;

/**
List to specify the join types for joins.
*/
protected List<Integer> _join_type_Vector;

/**
List for specifying the ON clauses for joins.
*/
protected List<String> _on_Vector;

/**
List for ORDER BY clauses used in the statement (e.g., ORDER BY XXXX, XXXX).
*/
protected List<String> _order_by_Vector;

/**
List for table names used in the statement (e.g., FROM XXXX, XXXX).
*/
protected List<String> _table_Vector;

/**
List for values to be inserted or updated with the statement.  Values may be Java objects
(String, Integer, etc.) or a DMISelectStatement, which will do a select on the value, for
example to select foreign key from the human-readable data value.
*/
protected List<Object> _values_Vector;

/**
List for where clauses used in the statement (e.g., WHERE XXXX, XXXX).
*/
protected List<String> _where_Vector;

/**
Construct an SQL statement.  Typically a derived class instance (e.g., DMISelectStatement) is declared.
@param dmi DMI instance to use (this is checked to properly format the statement for the database engine).
*/
public DMIStatement ( DMI dmi ) {
	_dmi = dmi;
	_isSP = false;
	initialize();
}

/**
Construct an SQL statement that will execute via a Stored Procedure.
@param dmi DMI instance to use.
@param data DMIStoredProcedureData to use for controlling the procedure.
*/
public DMIStatement(DMI dmi, DMIStoredProcedureData data) 
throws Exception {
	_dmi = dmi;
	_spData = data;
	_isSP = true;
	initialize();
	// parameter numbering starts at 1 for JDBC parameters, not 0.
	__paramNum = 1;
	__storedProcedureCallableStatement = _spData.getCallableStatement();

	if (_spData.hasReturnValue()) {
		__paramNum = 2;
	}	

	__spParameters = new String[_spData.getNumParameters()];
	for (int i = 0; i < __spParameters.length; i++) {
		__spParameters[i] = "?";
	}
}

/**
Add a field the statement.
@param field Field to add to the statement.
*/
public void addField ( String field ) {	
	_field_Vector.add ( field );
}

/**
Adds the table to be INNER joined to a select query.  An ON clause will need
to be set via the addJoinOn() method.
*/
public void addInnerJoin(String tableName, String on) {
	_join_Vector.add(tableName);
	_join_type_Vector.add(new Integer(_JOIN_INNER));
	_on_Vector.add(on);
}	

/**
Adds the table to be LEFT joined to a select query.  An ON clause will need
to be set via the addJoinOn() method.
*/
public void addLeftJoin(String tableName, String on) {
	_join_Vector.add(tableName);
	_join_type_Vector.add(new Integer(_JOIN_LEFT));
	_on_Vector.add(on);
}	

/**
Adds the table to be RIGHT joined to a select query.  An ON clause will need
to be set via the addJoinOn() method.
*/
public void addRightJoin(String tableName, String on) {
	_join_Vector.add(tableName);
	_join_type_Vector.add(new Integer(_JOIN_RIGHT));
	_on_Vector.add(on);
}	

/**
Adds a null value to the values vector.
*/
public void addNullValue() 
throws Exception {
	if (_isSP) {
		setNullValue(__paramNum++);
	}
	else {
		// this is for insert/update statements
		_values_Vector.add(null);
	}
}

/**
Add an ORDER BY clause to the statement.
@param order_by_clause ORDER BY clause to add to the statement.
*/
public void addOrderByClause(String order_by_clause) {
	int size = _order_by_Vector.size();
	String s = null;
	for (int i = 0; i < size; i++) {
		s = _order_by_Vector.get(i);
		if (s.equalsIgnoreCase(order_by_clause)) {
			// already present in the order by list, do not add again.
			return;
		}
	}
	_order_by_Vector.add ( order_by_clause );
}

/**
Adds a series of order by clauses to the statement at once.
@param order_by_clauses list of String order by clauses to add.
*/
public void addOrderByClauses(List<String> order_by_clauses) {
	for (int i = 0; i < order_by_clauses.size(); i++) {
		addOrderByClause(order_by_clauses.get(i));
	}
}

/**
Add a table the statement.
@param table Table to add to the statement.
*/
public void addTable ( String table ) {
	_table_Vector.add ( table );
}

/**
Adds a boolean value to the statement.
@param value boolean value to add to the statement.
*/
public void addValue(boolean value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add(new Boolean(value));
	}
}

/**
Adds a Boolean value to the statement.
@param value Boolean value to add to the statement.
*/
public void addValue(Boolean value) 
throws Exception {
	addValue(value.booleanValue());
}

/** 
Adds a date value to the statement, with a default date format of yyyy-MM-dd
@param value date value to add to the statement
*/
public void addValue (Date value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		addValue(value, DateTime.PRECISION_DAY);
	}
}

/**
Adds a Date value to the statement.
@param value Date value to add to the statement.
@param precision the DateTime PRECISION_* flag that determines how much of
the Date will be formatted by DMIUtil.formatDateTime().
@throws Exception if there is an error formatting the date time (shouldn't happen).
*/
public void addValue(Date value, int precision) 
throws Exception {
	if (_isSP) {
		addValue(value);
		return;
	}
	_values_Vector.add(DMIUtil.formatDateTime(_dmi, new DateTime(value, precision)));
}

/**
Adds a DateTime value to the statement.
@param value DateTime value to add to the statement.
*/
public void addValue(DateTime value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add(value);
	}
}

/**
Add a double value to the statement
@param value double value to add to the statement
*/
public void addValue (double value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add (new Double(value));
	}
}

/**
Adds a Double value to the statement.
@param value the Double value to add to the statement.
*/
public void addValue(Double value) 
throws Exception {
    if ( value == null ) {
        addNullValue();
    }
    else {
        addValueOrNull(value.doubleValue());
    }
}

/**
Add a float value to the statement
@param value float value to add to the statement
*/
public void addValue (float value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add (new Float(value));
	}
}

/**
Adds a Float value to the statement.
@param value the Float value to add to the statement.
*/
public void addValue(Float value) 
throws Exception {
	addValueOrNull(value.floatValue());
}

/**
Add an integer value to the statement
@param value int value to add to the statement
*/
public void addValue (int value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add(new Integer(value));
	}
}

/**
Adds an Integer value to the statement.
@param value Integer value to add to the statement.
*/
public void addValue(Integer value) 
throws Exception {
    if ( value == null ) {
        addNullValue();
    }
    else {
        addValueOrNull(value.intValue());
    }
}

/**
Add a long value to the statement
@param value long value to add to the statement
*/
public void addValue (long value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
		_values_Vector.add(new Long(value));
	}
}

/**
Add a long value to the statement
@param value long value to add to the statement
*/
public void addValue(Long value) 
throws Exception {
	addValueOrNull(value.longValue());
}

/**
Add a String value to the statement
@param value String value to add to the statement
*/
public void addValue (String value) 
throws Exception {
	if (_isSP) {
		setValue(value, __paramNum++);
	}
	else {
	    if (value.indexOf('\'') > -1) {
    		if ( (_dmi.getDatabaseEngineType() == DMIDatabaseType.SQLSERVER) ||
    		    (_dmi.getDatabaseEngineType() == DMIDatabaseType.ACCESS) ) {
    		    // Handle specifically because the following '' is documented
				_values_Vector.add("'" + StringUtil.replaceString(value, "'", "''") + "'");
			}	
			else {
				_values_Vector.add("'" + StringUtil.replaceString(value, "'", "\\'") + "'");
			}
		}
        else {
            _values_Vector.add("'" + value + "'");
        }
	}
}

/**
Add a DMISelectStatement value to the statement
@param value DMISelectStatement value to add to the statement - basically a nested select string that
goes into the statement
*/
public void addValue (DMISelectStatement value) 
throws Exception {
    if (_isSP) {
        throw new RuntimeException ( "Stored procedures do not support nested select in write statement." );
        //setValue(value, __paramNum++);
    }
    else {
        if ( _dmi.getDatabaseEngineType() == DMIDatabaseType.SQLSERVER ) {
            // TODO SAM 2013-02-03 Need to evaluate whether any specific cleanup needs to be done internally
            // as in the following code so that the embedded select statement is properly formatted
            //if (value.indexOf('\'') > -1) {
            //    _values_Vector.add("'" + StringUtil.replaceString(value, "'", "''") + "'");
            //}   
            //else {
            //    _values_Vector.add("'" + value + "'");
            //}
            _values_Vector.add("(" + value + ")");
        }
        else {
            _values_Vector.add("(" + value + ")");
        }
    }
}

/**
Checks the value with DMIUtil.isMissing() to see if it is missing and if so,
calls addNullValue(); otherwise calls addValue().  This is done to insert
values into fields which can take NULL values.
@param value Date value to add to the statement
@param precision the precision in which to format the date.
@throws Exception if there is an error formatting the date time.
*/
public void addValueOrNull(Date value, int precision)
throws Exception {
	if (DMIUtil.isMissing(value)) {
		addNullValue();
	}
	else {
		addValue(value, precision);
	}
}

/**
Checks the value with DMIUtil.isMissing() to see if it is missing and if so,
calls addNullValue(); otherwise calls addValue().  This is done to insert
values into fields which can take NULL values.
@param value double value to add to the statement
*/
public void addValueOrNull(double value) 
throws Exception {
	if (DMIUtil.isMissing(value)) {
		addNullValue();
	} 
	else {
		addValue(value);
	}
}

/**
Checks the value with DMIUtil.isMissing() to see if it is missing and if so,
calls addNullValue(); otherwise calls addValue().  This is done to insert
values into fields which can take NULL values.
@param value int value to add to the statement
*/
public void addValueOrNull(int value) 
throws Exception {
	if (DMIUtil.isMissing(value)) {
		addNullValue();
	} 
	else {
		addValue(value);
	}
}

/**
Checks the value with DMIUtil.isMissing() to see if it is missing and if so,
calls addNullValue(); otherwise calls addValue().  This is done to insert
values into fields which can take NULL values.
@param value long value to add to the statement
*/
public void addValueOrNull(long value) 
throws Exception {
	if (DMIUtil.isMissing(value)) {
		addNullValue();
	} 
	else {
		addValue(value);
	}
}

/**
Checks the value with DMIUtil.isMissing() to see if it is missing and if so,
calls addNullValue(); otherwise calls addValue().  This is done to insert
values into fields which can take NULL values.
@param value String value to add to the statement
*/
public void addValueOrNull(String value) 
throws Exception {
	if (DMIUtil.isMissing(value)) {
		addNullValue();
	} 
	else {
		addValue(value);
	}
}

/**
Add a WHERE clause to the statement.
@param where_clause WHERE clause to add to the statement.
*/
public void addWhereClause ( String where_clause ) 
throws Exception {
	if (_isSP) {
		setValueFromWhereClause(where_clause);	
	}
	else {
		_where_Vector.add ( where_clause );
	}
}

/**
Adds a list of WHERE clauses to the statement.
@param whereClauses list of String WHERE clauses to add.
*/
public void addWhereClauses( List<String> whereClauses) 
throws Exception
{
	for ( String whereClause : whereClauses ) {
	    if ( !whereClause.equals("") ) {
	        addWhereClause(whereClause);
	    }
	}
}

/**
Creates the string that can be pasted into a Query Analyzer to run the
stored procedure exactly as it is executed internally by the DMI.  The result
from this call is different from the createStoredProcedureCallString() call.
This is used for logging but is NOT passed to the database for processing.
@return the "exec ...." String.
*/
public String createStoredProcedureString() {
	String callString = "";
	
	if ( _spData != null ) {
	    // There may be a case where stored procedures are being used for the database but the current
	    // statement is doing a direct query (such as on a published view).
    	callString += "exec " + _spData.getProcedureName() + " ";
    
    	int numParameters = _spData.getNumParameters();
    
    	for (int i = 0; i < numParameters; i++) {
    		if (i > 0) {
    			callString += ", ";
    		}
    		callString += __spParameters[i];
    	}
	}
	else {
	    // TODO SAM 2012-09-07 Evaluate how to better handle case when stored procedure is not used
	}

	return callString;
}

/**
Executes this statement's stored procedure.  If this statement was not set
up as a Stored Procedure, an exception will be thrown.
@return the ResultSet that was returned from the query.
*/
public boolean executeStoredProcedure()
throws SQLException {
	if (!isStoredProcedure()) {
		throw new SQLException("Cannot use executeStoredProcedure() to "
			+ "execute a DMIStatement that is not a stored procedure.");
	}
	// Put together the query string for troubleshooting
	
	// execute can be used with any type of SQL statement and returns a boolean.
	// A true indicates that the method returned a result set object that can be retrieved using getResultSet().
	// A false indicates that the query returned an int value or void.
	// It executes select and insert/update statements.
	return __storedProcedureCallableStatement.execute();
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable {
	_dmi = null;
	_spData = null;
	_autonumber_Vector = null;
	_field_Vector = null;
	_join_Vector = null;
	_join_type_Vector = null;
	_on_Vector = null;
	_order_by_Vector = null;
	_table_Vector = null;
	_values_Vector = null;
	_where_Vector = null;
	super.finalize();
}

/**
Returns the callable statement executed by the stored procedure.  This is done
usually in order to close the statement in the two-parameter version of 
DMI.closeResultSet().
@return the callable statement executed by the stored procedure.
*/
public java.sql.Statement getCallableStatement() {
	return __storedProcedureCallableStatement;
}

/**
Returns the DMI's stored procedure data (that specifies how the stored procedure is set up and run.
@return the DMI's stored procedure data (that specifies how the stored procedure is set up and run.
*/
public DMIStoredProcedureData getDMIStoredProcedureData() {
	return _spData;
}

/**
Gets the return value from a stored procedure that returns a value as an int.
@return the return value.
*/
public int getIntReturnValue() 
throws SQLException {
	return __storedProcedureCallableStatement.getInt(1);  
}

/**
Gets the return value from a stored procedure as an Object.
This is useful when the return value type does not need to be specifically handled.
SQL return types are mapped to typical Java object types Boolean, Float, Double, Integer, Long, String.
See:  https://www.tutorialspoint.com/jdbc/jdbc-data-types.htm
@return the return value.
*/
public Object getReturnValue() 
throws SQLException {
	int returnType = getDMIStoredProcedureData().getReturnType();
	if ( returnType == java.sql.Types.BIGINT ) {
		return __storedProcedureCallableStatement.getLong(1);  
	}
	else if ( (returnType == java.sql.Types.BIT) ||
		(returnType == java.sql.Types.BOOLEAN) ) {
		return __storedProcedureCallableStatement.getBoolean(1);  
	}
	else if ( (returnType == java.sql.Types.DECIMAL) ||
		(returnType == java.sql.Types.FLOAT) ||
		(returnType == java.sql.Types.REAL) ) {
		return __storedProcedureCallableStatement.getFloat(1);  
	}
	else if ( returnType == java.sql.Types.DOUBLE ) {
		return __storedProcedureCallableStatement.getDouble(1);  
	}
	else if ( returnType == java.sql.Types.INTEGER ) {
		return __storedProcedureCallableStatement.getInt(1);  
	}
	else if ( (returnType == java.sql.Types.LONGVARCHAR) ||
	    (returnType == java.sql.Types.VARCHAR) ) {
		return __storedProcedureCallableStatement.getString(1);  
	}
	// TODO smalers 2019-08-29 need to handle timestamp and date
	else {
		throw new SQLException ( "SQL procedure return type " + returnType + " is not handled.");
	}
}

/**
Gets the return value from a stored procedure that returns a value as a String.
@return the return value.
*/
public String getStringReturnValue() 
throws SQLException {
	return __storedProcedureCallableStatement.getString(1);	
}

/**
Initializes data members.
*/
private void initialize() {
	_autonumber_Vector = new Vector<String>();
	_field_Vector = new Vector<String>();
	_join_Vector = new Vector<String>();
	_join_type_Vector = new Vector<Integer>();
	_on_Vector = new Vector<String>();
	_order_by_Vector = new Vector<String>();
	_table_Vector = new Vector<String>();
	_values_Vector = new Vector<Object>();
	_where_Vector = new Vector<String>();
}

/**
Returns whether this statement is using a stored procedure or not.
@return true if the statement is going to execute via stored procedure, false if not.
*/
public boolean isStoredProcedure() {
	return _isSP;
}

/**
Removes a field from the statement.
@param field the field to remove from the statement.
*/
public void removeField (String field) {
	_field_Vector.remove(field);
}

/**
Sets the stored procedure data to use.  If this data is set to a non-null value
then the statement will execute with the stored procedure.  Otherwise, the
statement will try to create a SQL string.
@param data the stored procedure data to use.
*/
public void setStoredProcedureData(DMIStoredProcedureData data) 
throws Exception {
	_spData = data;
	if (data == null) {
		_isSP = false;
		__storedProcedureCallableStatement = null;
	}
	else {
		_isSP = true;
		__storedProcedureCallableStatement = _spData.getCallableStatement();
	
		// Note re: __paramNum:
		// Parameter numbering in JDBC/Database interface classes
		// begins at 1.  When a stored procedure should expect a 
		// return statement, the return value is automatically placed
		// in position 1, and the passed-in parameters begin at 2.
	
		if (_spData.hasReturnValue()) {
			__paramNum = 2;
		}			
		else {
			__paramNum = 1;
		}

		__spParameters = new String[_spData.getNumParameters()];
		for (int i = 0; i < __spParameters.length; i++) {
			__spParameters[i] = "?";
		}		
	}
}

/**
Sets a value in the specified parameter position.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
private void setNullValue(int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setNull(parameterNum, _spData.getParameterType(parameterNum));
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "NULL";
	}
	else {
		__spParameters[parameterNum - 1] = "NULL";
	}
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(boolean param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setBoolean(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "" + param;
	}
	else {
		__spParameters[parameterNum - 1] = "" + param;
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(Date param, int parameterNum) 
throws Exception {
    if ( param == null ) {
        setNullValue(parameterNum);
    }
	// Note:
	// the Date object is cast because the Date passed into this 
	// method is a java.util.Date, whereas the callable statement 
	// expects a javal.sql.Date, which is a subclass of java.util.Date.
	__storedProcedureCallableStatement.setDate(parameterNum, (new java.sql.Date(param.getTime())));
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "'" + param + "'";
	}
	else {
		__spParameters[parameterNum - 1] = "'" + param + "'";
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(DateTime param, int parameterNum) 
throws Exception {
    if ( param == null ) {
        setNullValue(parameterNum);
    }
	// Note:
	// the Date object is cast because the Date returned from getDate()
	// is a java.util.Date, whereas the callable statement expects a 
	// javal.sql.Date, which is a subclass of java.util.Date.
    // TODO SAM 2016-03-11 the following matches legacy behavior but handling of time zone may need additional evaluation
	__storedProcedureCallableStatement.setDate(parameterNum,(new java.sql.Date(param.getDate(TimeZoneDefaultType.LOCAL).getTime())));
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "'" + param + "'";
	}
	else {
		__spParameters[parameterNum - 1] = "'" + param + "'";
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(double param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setDouble(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "" + param;
	}
	else {
		__spParameters[parameterNum - 1] = "" + param;
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position to (1+) set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(float param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setFloat(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "" + param;
	}
	else {
		__spParameters[parameterNum - 1] = "" + param;
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(int param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setInt(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "" + param;
	}
	else {
		__spParameters[parameterNum - 1] = "" + param;
	}	
}

/**
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(long param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setLong(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "" + param;
	}
	else {
		__spParameters[parameterNum - 1] = "" + param;
	}
}

/**
Sets a value in the specified parameter position for stored procedure.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(String param, int parameterNum) 
throws Exception {
    if ( param == null ) {
        setNullValue(parameterNum);
    }
	__storedProcedureCallableStatement.setString(parameterNum, param);
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "'" + param + "'";
	}
	else {
		__spParameters[parameterNum - 1] = "'" + param + "'";
	}	
}

/**
Sets a value in the specified parameter position.
@param param the procedure parameter to pass in, as a timestamp.
@param parameterNum the number of the parameter position (1+) to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(Timestamp param, int parameterNum) 
throws Exception {
    if ( param == null ) {
        setNullValue(parameterNum);
    }
    // Pass a new instance to protect against any changes
    // - time zone will have been applied in calling code
	__storedProcedureCallableStatement.setTimestamp(parameterNum, (new Timestamp(param.getTime())));
	// The following is for logging later
	if (_spData.hasReturnValue()) {
		__spParameters[parameterNum - 2] = "'" + param + "'";
	}
	else {
		__spParameters[parameterNum - 1] = "'" + param + "'";
	}	
}

/**
Sets a value in a stored procedure based on the column name in the 
where clause.  The column name is split out from the where clause and
then the DMIStoredProcedureData object is consulted to determine which
parameter number the where clause maps to.  The value is also split out
from the where clause and put in the appropriate stored procedure parameter
position.  Where clauses are broken into column name and value by either an 
equal sign ('='), " LIKE " or " IS NULL".  
@param where the where clause.
*/
private void setValueFromWhereClause(String where)
throws Exception {
	boolean isNull = false;
	String upper = where.toUpperCase().trim();
	where = where.trim();
	
	// Find the position of the first equals sign. 
	
	int space = where.indexOf("=");
	int spaceLen = 1;
	if (space == -1) {
		// If none can be found, then check to find the first instance of " LIKE "
		space = upper.indexOf(" LIKE ");
		if (space == -1) {	
			// If none can be found, check to see if the where clause is comparing against NULL.
			space = upper.indexOf(" IS NULL");
			if (space == -1) {
				// If none of the above worked, then the column = value combination could not be determined. 
				throw new Exception ("Cannot determine columns or value from where clause: '" + where + "'");
			}
			// " IS NULL" was found, so denote in the boolean that a NULL value will need to be set
			isNull = true;
		}
		// The length of the token separating column name and value 
		// is 1 character if it's just an equal sign, but because 
		// " LIKE " was found, the parsing will need to take into
		// account the larger width of the separator.
		spaceLen = 5;
	}
	
	String origParam = where.substring(0, space).trim();
	// See if the column was specified in the where clause as a 
	// table_name.column_name combination.  Remove the table name, if present.
	int index = origParam.indexOf(".");
	if (index > -1) {
		origParam = origParam.substring(index + 1);
	}
	
	// For SQL Server 2000, at least, all the parameter names in 
	// the stored procedure are preceded by an '@' sign.
	String param = "@" + origParam;

	// Iterate through the parameter names stored in the stored procedure
	// data and see if a match can be found for the column name.
	int num = _spData.getNumParameters();
	int pos = -1;
	String paramName = null;
	String paramList = "";
	for (int i = 0; i < num; i++) {
		if (i > 0) {
			paramList += ", ";
		}
		paramName = _spData.getParameterName(i);
		paramList += paramName;
		if (param.equalsIgnoreCase(paramName)) {
			pos = i;
			break;
		}
	}

	if (pos == -1) {
		// No match was found in the stored procedure parameter names for the column
		throw new Exception ("Couldn't find parameter '" + origParam + "', specified in where clause: '"
			+ where + "'.\nKnown parameters are: '" + paramList + "'");
	}

	// Note that right now, pos is base-0, which is how the stored 
	// procedure data stored parameter information.

	// get out the type for the located parameter.
	int type = _spData.getParameterType(pos);
	String value = "";

	if (!isNull) {
		// determine the value that the column is being compared to
		where = where.substring(space + spaceLen).trim();
		value = where.trim();
	}

	int len = value.length();

	// pos is currently base-0, but for use in specifying the parameter
	// in the JDBC code, it needs to be incremented to be base-1.
	pos++;
	if (_spData.hasReturnValue()) {
		// furthermore, if there is a return value for the stored 
		// procedure, the passed-in parameters are actually base-2.
		pos++;
	}

	if (isNull) {
		setNullValue(pos);
		return;
	}

	switch (type) {
		case java.sql.Types.VARCHAR:
			value = value.substring(1, len - 1);
			setValue(value, pos);
			break;
		case java.sql.Types.BIT:
			// Boolean
			// TODO (JTS - 2004-06-15) Bits might be stored in the database as 0 or 1,
			// not sure right now as we're not dealing with boolean parameters now. 
			break;
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
			Integer I = new Integer(value);
			setValue(I.intValue(), pos);
			break;
		case java.sql.Types.BIGINT:
			Long L = new Long(value);
			setValue(L.longValue(), pos);
			break;
		case java.sql.Types.REAL:
			Float F = new Float(value);
			setValue(F.floatValue(), pos);
			break;
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
			Double D = new Double(value);
			setValue(D.doubleValue(), pos);
			break;
		case java.sql.Types.DATE:
			value = value.trim();
			if (value.startsWith("'")) {
				value = value.substring(1, len - 1);
			}
			DateTime DT = DateTime.parse(value);
		    // TODO SAM 2016-03-11 the following matches legacy behavior but handling of time zone may need additional evaluation
			setValue(new java.sql.Date(DT.getDate(TimeZoneDefaultType.LOCAL).getTime()), pos);
			break;
		case java.sql.Types.TIMESTAMP:
			value = value.trim();
			if (value.startsWith("'")) {
				value = value.substring(1, len - 1);
			}
			DateTime DT2 = DateTime.parse(value);
			setValue(new Timestamp(DT2.getDate(TimeZoneDefaultType.LOCAL).getTime()), pos);
			break;
		default:
			throw new Exception ("Unsupported type: " + type);
	}
}

/**
 * Return a string representation of the statement.
 * Typically this is handled in overloaded methods but for stored procedure is handled here.
 */
public String toString() {
	if ( isStoredProcedure() ) {
		StringBuilder desc = new StringBuilder();
		if ( _spData.hasReturnValue() ) {
			desc.append(_spData.getReturnTypeString() + " ");
		}
		desc.append(_spData.getProcedureName() + "(");
		for (int i = 0; i < _spData.getNumParameters(); i++) {
			if ( i != 0 ) {
				desc.append(", ");
			}
			desc.append( __spParameters[i] );
		}
		desc.append(")");
		return desc.toString();
	}
	else {
		// Pass through
		return super.toString();
	}
}

}