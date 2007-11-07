//-----------------------------------------------------------------------------
// DMISelectStatement - class for an SQL select statement
//-----------------------------------------------------------------------------
// History:
//
// 2002-06-25	Steven A. Malers, RTi	Initial version.
// 2003-02-20	J. Thomas Sapienza, RTi	Added support for GROUP BY clauses.
// 2003-03-05	JTS, RTi		WHERE clauses are now automatically
//					surrounded by parentheses.
// 2004-06-15	JTS, RTi		Joins are now supported.
// 2004-06-16	JTS, RTi		Statement can now execute as a stored
//					procedure.
// 2005-05-31	JTS, RTi		Added toAccessString() for properly
//					formatting JOIN information in Access
//					queries.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.DMI;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
The DMISelectStatement class stores basic information about SQL select
statements.  Currently all functionality is included in the base class but at
some point data and behavior may be moved to derived classes.<p>
See HydroBaseDMI for many good examples of using DMISelectStatement;
REVISIT (JTS - 2006-05-23)
Add some examples.
*/
public class DMISelectStatement 
extends DMIStatement {

/**
Flag indicating whether a distinct select is executed.
*/
protected boolean _distinct;

/**
Flag indicating whether the ORDER BY clause should be a GROUP BY clause, 
instead.
*/
protected boolean _groupBy = false;

/**
Construct a select statement.
*/
public DMISelectStatement ( DMI dmi ) {	
	super ( dmi );
	_distinct = false;
}

/**
Executes this statement's stored procedure.  If this statement was not set
up as a Stored Procedure, an exception will be thrown.
@return the ResultSet that was returned from the query.
*/
public ResultSet executeStoredProcedure()
throws SQLException {
	if (!isStoredProcedure()) {
		throw new SQLException("Cannot use executeStoredProcedure() to "
			+ "execute a DMISelectStatement that is not a "
			+ "stored procedure.");
	}
	return __storedProcedureCallableStatement.executeQuery();
}

/**
Returns whether the ORDER BY clause should be a GROUP BY clause, instead.
@return whether the ORDER BY clause should be a GROUP BY clause, instead.
*/
public boolean getGroupBy() {
	return _groupBy;
}

/**
Indicate whether a distinct select statement is to be executed.
@return true if a DISTINCT select will be executed.
*/
public boolean selectDistinct () {	
	return _distinct;
}

/**
Set whether a distinct select statement is to be executed.
@param distinct If true, a DISTINCT select will be executed.
@return the value of the flag, after setting.
*/
public boolean selectDistinct ( boolean distinct ) {	
	_distinct = distinct;
	return _distinct;
}

/**
Sets whether the ORDER BY clause should be a GROUP BY clause, instead.
@param groupBy true or false
*/
public void setGroupBy(boolean groupBy) {
	_groupBy = groupBy;
}

/**
Formats the SELECT statement for Access databases.
@return the SELECT statement as a string.
*/
private String toAccessString() {
	StringBuffer statement = new StringBuffer("SELECT ");
	
	if ( _distinct ) {
		statement.append ( "DISTINCT " );
	}
	
	int size = _field_Vector.size();
	if (size > 0) {
		statement.append ( (String)_field_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_field_Vector.elementAt(i) );
		}
	}
	
	size = _table_Vector.size();
	if (size > 0 && _join_Vector.size() == 0) {
		statement.append ( " FROM " );
		statement.append ( (String)_table_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_table_Vector.elementAt(i) );
		}
	}
	
	size = _join_Vector.size();
	if (size > 0) {
		int type = -1;
		String s = null;

		statement.append(" FROM ");
		for (int i = 0; i < (size - 1); i++) {
			statement.append("(");
		}
		
		statement.append((String)_table_Vector.elementAt(0));

		for (int i = 0; i < size; i++) {
			type = ((Integer)_join_type_Vector.elementAt(i))
				.intValue();
			if (type == _JOIN_INNER) {
				statement.append(" INNER JOIN ");
			}
			else if (type == _JOIN_LEFT) {
				statement.append(" LEFT JOIN ");
			}
			else if (type == _JOIN_RIGHT) {
				statement.append(" RIGHT JOIN ");
			}
			s = (String)_join_Vector.elementAt(i);
			statement.append(s + " ON ");
			s = (String)_on_Vector.elementAt(i);
			statement.append(s);
			
			if (size > 1 && i < (size - 1)) {
				// only do this for joins where joining more
				// than one thing, but do not do for the last
				// join in such an occasion
				statement.append(")");
			}
		}
	}
	
	size = _where_Vector.size();
	if (size > 0) {
		statement.append ( " WHERE " );
		statement.append ( (String)_where_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( " AND (" +
				(String)_where_Vector.elementAt(i) + ")");
		}
	}
	
	size = _order_by_Vector.size();
	if (size > 0) {
		if (_groupBy) {
			statement.append(" GROUP BY ");
		} else {
			statement.append ( " ORDER BY " );
		}
		statement.append ( (String)_order_by_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_order_by_Vector.elementAt(i));
		}
	}
	return statement.toString();
}

/**
Format the SELECT statement.
@return the SELECT statement as a string.
*/
public String toString() {	
	if (_dmi.getDatabaseEngine().equalsIgnoreCase("Access")) {
		return toAccessString();
	}

	StringBuffer statement = new StringBuffer("SELECT ");
	
	if ( _distinct ) {
		statement.append ( "DISTINCT " );
	}
	
	int size = _field_Vector.size();
	if (size > 0) {
		statement.append ( (String)_field_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_field_Vector.elementAt(i) );
		}
	}
	
	size = _table_Vector.size();
	if (size > 0) {
		statement.append ( " FROM " );
		statement.append ( (String)_table_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_table_Vector.elementAt(i) );
		}
	}
	
	size = _join_Vector.size();
	if (size > 0) {
		int type = -1;
		String s = null;
		for (int i = 0; i < size; i++) {
			type = ((Integer)_join_type_Vector.elementAt(i))
				.intValue();
			if (type == _JOIN_INNER) {
				statement.append(" INNER JOIN ");
			}
			else if (type == _JOIN_LEFT) {
				statement.append(" LEFT JOIN ");
			}
			else if (type == _JOIN_RIGHT) {
				statement.append(" RIGHT JOIN ");
			}
			s = (String)_join_Vector.elementAt(i);
			statement.append(s + " ON ");
			s = (String)_on_Vector.elementAt(i);
			statement.append(s);
		}
	}
	
	size = _where_Vector.size();
	if (size > 0) {
		statement.append ( " WHERE " );
		statement.append ( (String)_where_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( " AND (" +
				(String)_where_Vector.elementAt(i) + ")");
		}
	}
	
	size = _order_by_Vector.size();
	if (size > 0) {
		if (_groupBy) {
			statement.append(" GROUP BY ");
		} else {
			statement.append ( " ORDER BY " );
		}
		statement.append ( (String)_order_by_Vector.elementAt(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " +
				(String)_order_by_Vector.elementAt(i));
		}
	}
	return statement.toString();
}

} // end DMISelectStatement
