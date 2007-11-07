//-----------------------------------------------------------------------------
// DMIDeleteStatement - class for an SQL delete statement
//-----------------------------------------------------------------------------
// History:
//
// 2002-07-11	J. Thomas Sapienza, RTi	Initial version
// 2002-08-12	JTS			toString was finally fleshed out to
//					work.
// 2004-06-16	JTS, RTi		Statement can now execute as a stored
//					procedure.
//-----------------------------------------------------------------------------

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
			+ "execute a DMIDeleteStatement that is not a "
			+ "stored procedure.");
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
		statement.append ( (String)_table_Vector.elementAt(0) );
	}

	size = _where_Vector.size();
	if ( size > 0 ) {
		statement.append ( " WHERE " );
		statement.append ( (String)_where_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( " AND " +
				(String)_where_Vector.elementAt(i) );
		}
	}
	return statement.toString();
}

} // end DMIDeleteStatement
