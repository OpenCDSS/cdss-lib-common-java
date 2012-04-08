package RTi.DMI;

import java.sql.SQLException;

/**
The DMIDeleteStatement class stores basic information about SQL delete 
statements.  Currently all functionality is included in the base class but at
some point data and behavior may be moved to derived classes.
*/
public class DMIDeleteStatement extends DMIStatement
{

/**
Construct a delete statement.
*/
public DMIDeleteStatement ( DMI dmi ) {
	super ( dmi );
}

/**
Executes this statement's stored procedure.  If this statement was not set
up as a Stored Procedure, an exception will be thrown.
*/
public void executeStoredProcedure()
throws SQLException {
	if (!isStoredProcedure()) {
		throw new SQLException("Cannot use executeStoredProcedure() to "
			+ "execute a DMIDeleteStatement that is not a stored procedure.");
	}
	__storedProcedureCallableStatement.executeUpdate();
}

/**
Format the DELETE statement.
@return the DELETE statement as a string.
*/
public String toString() {
	StringBuffer statement = new StringBuffer("DELETE ");

	int size = _table_Vector.size();
	if ( size > 0 ) {
		statement.append ( " FROM " );
		statement.append ( _table_Vector.get(0) );
	}

	size = _where_Vector.size();
	if ( size > 0 ) {
		statement.append ( " WHERE " );
		statement.append ( _where_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( " AND " + _where_Vector.get(i) );
		}
	}
	return statement.toString();
}

}