//-----------------------------------------------------------------------------
// DMIStatement - general class for an SQL statement
//-----------------------------------------------------------------------------
// History:
//
// 2002-06-25	Steven A. Malers, RTi	Initial version.
// 2002-07-10	J. Thomas Sapienza, RTi	Added _values_Vector (for use with
//					INSERTs and UPDATEs).  Also added 	
//					_autonumber_Vector (because they're a
//					special case in INSERTing and 
//					UPDATEing).
// 2002-07-23	JTS, RTi		Added date formatting ability (through
//					SimpleDateFormat), and also added the
//					testAddValue() methods.  Added 
//					the RTi...Message class use.
// 2002-12-09	JTS, RTi		Removed "addAutonumberField" method.
//					Unused in code and the reason for adding
//					it in July was a development path RTi	
//					went down.
// 2003-01-03	SAM, RTi		* The database engine in DMI is now
//					  available as a protected integer - use
//					  it to improve performance.
//					* The missing values are now defined in
//					  DMIUtil (not DMI).  Also, call the
//					  DMIUtil.isMissing() method for data
//					  types that may require more extensive
//					  checks than a simple comparison.
// 2003-05-13	JTS, RTi		Added code so that if a String value
//					with a single quote is added to a 
//					statement for SQL Server, the quote
//					is replaced by double single quotes.
// 2003-05-21	JTS, RTi		Added addValue and testAddValue methods
//					for long datatypes
// 2003-06-12	JTS, RTi		Corrected error in addValue(String)
//					where: 
//					* If the string was checked for "'" and
//					  one was found, the string was not then
//					  being added with quotes around it.
//					* SQLServer2000 was not being checked
//					  as a server type.
// 2003-06-20	JTS, RTi		Added removeField()
// 2003-11-12	JTS, RTi		Updated the addValue(Date) methods to
//					use DMIUtil.formatDateTime().
// 2004-06-15	JTS, RTi		* Added support for joins.
//					* Expanded to allow support for 
//					  stored procedures using all the
//					  currently-existing methods.
// 2005-02-03	JTS, RTi		setValue() parameters were made 
//					public.
// 2005-03-08	JTS, RTi		Added createStoredProcedureString().
// 2005-04-05	JTS, RTi		addOrderByClause() now checks to see
//					whether the order by clause is already
//					in the order by Vector, because having
//					two of the same column names in an 
//					ORDER BY clause will cause an SQL error.
// 2005-06-02	JTS, RTi		Moved the code that creates the 
//					stored procedure callable statement
//					out to DMIStoredProcedureData so that
//					they can be reused.
// 2006-05-03	JTS, RTi		* Deprecated all testAddValue() methods.
//					  They should be removed ASAP and 
//					  instead addValueOrNull() should be 
//       				  used instead.
//					* Added addValueOrNull().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.DMI;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Vector;

import RTi.Util.Time.DateTime;

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
The object that will actually execute the stored procedure in the low level
java code.
*/
protected CallableStatement __storedProcedureCallableStatement = null;

/**
DMI that will execute the query.  It is used to determine the database type
for formatting statements.
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
Vector for fields that are autonumbers
*/
protected Vector _autonumber_Vector;

/**
Vector for fields used in the statement (e.g., SELECT XXXX, XXXX).
*/
protected Vector _field_Vector;

/**
Vector to specify the tables for joins.
*/
protected Vector _join_Vector;

/**
Vector to specify the join types for joins.
*/
protected Vector _join_type_Vector;

/**
Vector for specifying the ON clauses for joins.
*/
protected Vector _on_Vector;

/**
Vector for ORDER BY clauses used in the statement
(e.g., ORDER BY XXXX, XXXX).
*/
protected Vector _order_by_Vector;

/**
Vector for tables used in the statement (e.g., FROM XXXX, XXXX).
*/
protected Vector _table_Vector;

/**
Vector for values to be inserted or updated with the statement
*/
protected Vector _values_Vector;

/**
Vector for where clauses used in the statement (e.g., WHERE XXXX, XXXX).
*/
protected Vector _where_Vector;

/**
Construct an SQL statement.  Typically a derived class instance (e.g.,
DMISelectStatement) is declared.
@param dmi DMI instance to use (this is checked to properly format 
the statement for the database engine).
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
	_field_Vector.addElement ( field );
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
		_values_Vector.addElement(null);
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
		s = (String)_order_by_Vector.elementAt(i);
		if (s.equalsIgnoreCase(order_by_clause)) {
			// already present in the order by Vector, do not
			// add again.
			return;
		}
	}
	_order_by_Vector.addElement ( order_by_clause );
}

/**
Adds a series of order by clauses to the statement at once.
@param order_by_clauses Vector of String order by clauses to add.
*/
public void addOrderByClauses(Vector order_by_clauses) {
	for (int i = 0; i < order_by_clauses.size(); i++) {
		addOrderByClause((String)order_by_clauses.elementAt(i));
	}
}

/**
Add a table the statement.
@param table Table to add to the statement.
*/
public void addTable ( String table ) {
	_table_Vector.addElement ( table );
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
		_values_Vector.addElement(new Boolean(value));
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
Adds a date value to the statement, with a default date format of
yyyy-MM-dd
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
Add a Date value to the statement
@param value Date value to add to the statement
@param format a string format representing how the date should be formatted.
For more information on formats, see the Javadocs for SimpleDateFormatter
@deprecated Don't use!  Dates with hours in the afternoon will not be entered
properly into SQL Server database.  Use addValue(Date, int) instead.
*/
public void addValue (Date value, String format) 
throws Exception {
	SimpleDateFormat sdf = new SimpleDateFormat(format);

	if (value == null) {
		_values_Vector.addElement(null);
	} else {
	/*
		_values_Vector.addElement(DMIUtil.formatDateTime(_dmi, 
			new DateTime(value)));
	*/
		if (_dmi._database_engine == _dmi._DBENGINE_ACCESS ) {
			_values_Vector.addElement
				("#" + sdf.format(value) + "#");
		}
		else if	( (_dmi._database_engine == _dmi._DBENGINE_SQLSERVER7)||
			(_dmi._database_engine==_dmi._DBENGINE_SQLSERVER2000)){
			_values_Vector.addElement 
				("'" + sdf.format(value) + "'");
		}
		else {
			_values_Vector.addElement 
				("'" + sdf.format(value) + "'");
		}
	}
}

/**
Adds a Date value to the statement.
@param value Date value to add to the statement.
@param precision the DateTime PRECISION_* flag that determines how much of
the Date will be formatted by DMIUtil.formatDateTime().
@throws Exception if there is an error formatting the date time (shouldn't 
happen).
*/
public void addValue(Date value, int precision) 
throws Exception {
	if (_isSP) {
		addValue(value);
		return;
	}
	_values_Vector.addElement(DMIUtil.formatDateTime(_dmi, 
		new DateTime(value, precision)));
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
		_values_Vector.addElement(value);
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
		_values_Vector.addElement (new Double(value));
	}
}

/**
Adds a Double value to the statement.
@param value the Double value to add to the statement.
*/
public void addValue(Double value) 
throws Exception {
	addValue(value.doubleValue());
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
		_values_Vector.addElement (new Float(value));
	}
}

/**
Adds a Float value to the statement.
@param value the Float value to add to the statement.
*/
public void addValue(Float value) 
throws Exception {
	addValue(value.floatValue());
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
		_values_Vector.addElement(new Integer(value));
	}
}

/**
Adds an Integer value to the statement.
@param value Integer value to add to the statement.
*/
public void addValue(Integer value) 
throws Exception {
	addValue(value.intValue());
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
		_values_Vector.addElement(new Long(value));
	}
}

/**
Add a long value to the statement
@param value long value to add to the statement
*/
public void addValue(Long value) 
throws Exception {
	addValue(value.longValue());
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
		if (_dmi.getDatabaseEngine().equalsIgnoreCase("SQL_Server") 
		 || _dmi.getDatabaseEngine().equalsIgnoreCase("SQLServer7") 
		 || _dmi.getDatabaseEngine().equalsIgnoreCase("SQLServer2000")){
			if (value.indexOf('\'') > -1) {
				_values_Vector.addElement("'"
					+ StringUtil.replaceString(value, "'", 
					"''") + "'");
			}	
			else {
				_values_Vector.addElement("'" + value + "'");
			}
		}
		else {
			_values_Vector.addElement("'" + value + "'");
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
		_where_Vector.addElement ( where_clause );
	}
}

/**
Adds a series of where clauses to the statement at once.
@param where_clauses Vector ofString where clauses to add.
*/
public void addWhereClauses(Vector where_clauses) 
throws Exception {
	String s;
	for (int i = 0; i < where_clauses.size(); i++) {
		s = (String)where_clauses.elementAt(i);
		addWhereClause(s);
	}
}

/**
Creates the string that can be pasted into a Query Analyzer to run the
stored procedure exactly as it is executed internally by the DMI.  The result
from this call is different from the createStoredProcedureCallString() call.
@return the "exec ...." String.
*/
public String createStoredProcedureString() {
	String callString = "";
	
	callString += "exec " + _spData.getProcedureName() + " ";

	int numParameters = _spData.getNumParameters();

	for (int i = 0; i < numParameters; i++) {
		if (i > 0) {
			callString += ", ";
		}
		callString += __spParameters[i];
	}


	return callString;
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
Returns the DMI's stored procedure data (that specifies how the stored 
procedure is set up and run.
@return the DMI's stored procedure data (that specifies how the stored 
procedure is set up and run.
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
	_autonumber_Vector = new Vector();
	_field_Vector = new Vector();
	_join_Vector = new Vector();
	_join_type_Vector = new Vector();
	_on_Vector = new Vector();
	_order_by_Vector = new Vector();
	_table_Vector = new Vector();
	_values_Vector = new Vector();
	_where_Vector = new Vector();
}

/**
Returns whether this statement is using a stored procedure or not.
@return true if the statement is going to execute via stored procedure, false
if not.
*/
public boolean isStoredProcedure() {
	return _isSP;
}

/**
Removes a field from the statement.
@param field the field to remove from the statement.
*/
public void removeField (String field) {
	_field_Vector.removeElement(field);
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
		__storedProcedureCallableStatement 
			= _spData.getCallableStatement();
	
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
@param parameterNum the number of the parameter position to set.
@throws Exception if the specified parameter is not a String type.
*/
private void setNullValue(int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setNull(parameterNum, 
		_spData.getParameterType(parameterNum));
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
@param parameterNum the number of the parameter position to set.
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
@param parameterNum the number of the parameter position to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(Date param, int parameterNum) 
throws Exception {
	// Note:
	// the Date object is cast because the Date passed into this 
	// method is a java.util.Date, whereas the callable statement 
	// expects a javal.sql.Date, which is a subclass of java.util.Date.
	__storedProcedureCallableStatement.setDate(parameterNum, 
		(new java.sql.Date(param.getTime())));
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
@param parameterNum the number of the parameter position to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(DateTime param, int parameterNum) 
throws Exception {
	// Note:
	// the Date object is cast because the Date returned from getDate()
	// is a java.util.Date, whereas the callable statement expects a 
	// javal.sql.Date, which is a subclass of java.util.Date.
	__storedProcedureCallableStatement.setDate(parameterNum, 
		(new java.sql.Date(param.getDate().getTime())));
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
@param parameterNum the number of the parameter position to set.
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
@param parameterNum the number of the parameter position to set.
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
@param parameterNum the number of the parameter position to set.
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
@param parameterNum the number of the parameter position to set.
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
Sets a value in the specified parameter position.
@param param the parameter to pass in.
@param parameterNum the number of the parameter position to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(String param, int parameterNum) 
throws Exception {
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
@param param the parameter to pass in.
@param parameterNum the number of the parameter position to set.
@throws Exception if the specified parameter is not a String type.
*/
public void setValue(Timestamp param, int parameterNum) 
throws Exception {
	__storedProcedureCallableStatement.setTimestamp(parameterNum, 
		(new Timestamp(param.getTime())));
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
	
	// find the position of the first equals sign. 
	
	int space = where.indexOf("=");
	int spaceLen = 1;
	if (space == -1) {
		// if none can be found, then check to find the first instance
		// of " LIKE "
		space = upper.indexOf(" LIKE ");
		if (space == -1) {	
			// if none can be found, check to see if the where
			// clause is comparing against NULL.
			space = upper.indexOf(" IS NULL");
			if (space == -1) {
				// if none of the above worked, then the 
				// column = value combination could not
				// be determined. 
				throw new Exception ("Cannot determine "
					+ "columns or value from where "
					+ "clause: '" + where + "'");
			}
			// " IS NULL" was found, so denote in the boolean
			// that a NULL value will need to be set
			isNull = true;
		}
		// the length of the token separating column name and value 
		// is 1 character if it's just an equal sign, but because 
		// " LIKE " was found, the parsing will need to take into
		// acount the larger width of the separator.
		spaceLen = 5;
	}
	
	String origParam = where.substring(0, space).trim();
	// see if the column was specified in the where clause as a 
	// table_name.column_name combination.  Remove the table name, if
	// present.
	int index = origParam.indexOf(".");
	if (index > -1) {
		origParam = origParam.substring(index + 1);
	}
	
	// For SQL Server 2000, at least, all the parameter names in 
	// the stored procedure are preceded by an '@' sign.
	String param = "@" + origParam;

	// iterate through the parameter names stored in the stored procedure
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
		// no match was found in the stored procedure parameter names
		// for the column
		throw new Exception ("Couldn't find parameter '" 
			+ origParam + "', specified in where clause: '"
			+ where + "'.\nKnown parameters are: '"
			+ paramList + "'");
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
			// REVISIT (JTS - 2004-06-15)
			// Bits might be stored in the database as 0 or 1,
			// not sure right now as we're not dealing with 
			// boolean paremeters now. 
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
			setValue(new java.sql.Date(DT.getDate().getTime()),
			      	pos);
			break;		
		case java.sql.Types.TIMESTAMP:
			value = value.trim();
			if (value.startsWith("'")) {
				value = value.substring(1, len - 1);
			}
			DateTime DT2 = DateTime.parse(value);
			setValue(new Timestamp(DT2.getDate().getTime()),
			      	pos);
			break;
		default:
			throw new Exception ("Unsupported type: " 
				+ type);
	}

}

////////////////////////////////////////////////////////////////////
// REVISIT (JTS - 2004-06-15)
// revisit these later.  Are they used anywhere?  If so, according to the
// (incorrect) javadocs?  Are they needed? 
// REVISIT (JTS - 2006-05-03)
// REMOVE ASAP!

/**
Test to see if the date is missing, and if so, delete the field from
the _field_vector and do not add the value to the _values_vector
@param value Date value to add to the statement
@deprecated use addValueOrNull
*/
public void testAddValue (Date value, String format) 
throws Exception {	
	if ( DMIUtil.isMissing(value) ) {		
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(Date)",
					"Warning: " + 
					"Field: '" + field + "' not found in " +
					"_field_Vector."
				);
			}
		}
		*/
		addNullValue();
	} else {
		addValue(value, format);
	}
}

/**
Test to see if the date is missing, and if so, delete the field from the
_field_vector and do not add the value to the _values_vector.
@param value Date value to add to the statement
@param precision the precision in which to format the date.
@throws Exception if there is an error formatting the date time.
@deprecated use addValueOrNull
*/
public void testAddValue(Date value, int precision)
throws Exception {
	if (DMIUtil.isMissing(value)) {
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(Date, int)",
					"Warning: " + 
					"FIeld: '" + field + "' not found int "+
					"_field_Vector."
				);
			}
		}
		*/
		addNullValue();
	}
	else {
		addValue(value, precision);
	}
}

/**
Test to see if the number is missing, and if so, delete the field from
the _field_vector and do not add the value to the _values_vector
@param value double value to add to the statement
@deprecated use addValueOrNull
*/
public void testAddValue (double value) 
throws Exception {
	if ( DMIUtil.isMissing(value) ) {
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(double)",
					"Warning: " + 
					"Field: '" + field + "' not found in " +
					"_field_Vector."
				);
			}
		}
		*/
		addNullValue();
	} else {
		addValue(value);
	}
}

/**
Test to see if the int is missing, and if so, delete the field from
the _field_vector and do not add the value to the _values_vector
@param value int value to add to the statement
@deprecated use addValueOrNull
*/
public void testAddValue (int value) 
throws Exception {
	if (value == DMIUtil.MISSING_INT) {
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(int)",
					"Warning: " + 
					"Field: '" + field + "' not found in " +
					"_field_Vector."
				);
			}
		}
		*/
		addNullValue();
	} else {
		addValue(value);
	}
}

/**
Test to see if the long is missing, and if so, delete the field from
the _field_vector and do not add the value to the _values_vector
@param value long value to add to the statement
@deprecated use addValueOrNull
*/
public void testAddValue (long value) 
throws Exception {
	if (value == DMIUtil.MISSING_LONG) {
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(int)",
					"Warning: " + 
					"Field: '" + field + "' not found in " +
					"_field_Vector."
				);
			}
		}
		*/
		addNullValue();
	} else {
		addValue(value);
	}
}

/**
Test to see if the string is missing, and if so, delete the field from
the _field_vector and do not add the value to the _values_vector
@param value String value to add to the statement
@deprecated use addValueOrNull
*/
public void testAddValue (String value) 
throws Exception {
	if (DMIUtil.isMissing(value)) {
		/*
		if (_field_Vector.removeElement(field) == false) {
			if (Message.isDebugOn) {
				Message.printWarning(25,
					"DMIStatement.testAddValue(String)",
					"Warning: " + 
					"Field: '" + field + "' not found in " +
					"_field_Vector."
				);
			}
		}
		*/
//		addValue("");		
		addNullValue();
	} else {
		addValue(value);
	}
}

}
