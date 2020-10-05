// DMISelectStatement - select statement

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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
The DMISelectStatement class stores basic information about SQL select statements, allowing select statements to
be formed and executed.
See HydroBaseDMI for many good examples of using DMISelectStatement;
TODO (JTS - 2006-05-23) Add some examples.
*/
public class DMISelectStatement 
extends DMIStatement {

/**
Flag indicating whether a distinct select is executed.
*/
protected boolean _distinct;

/**
Flag indicating whether the ORDER BY clause should be a GROUP BY clause, instead.
*/
protected boolean _groupBy = false;

/**
Indicating whether a "top" clause should be used by indicating the number of rows to return.
-1 means no "top" clause.
@TODO SAM 2013-04-10 This capability appears to vary quite a bit between database vendors
and is mainly being implemented to support SQL Server, although some other engines are checked
for in toString().
*/
protected int _top = -1;

/**
Construct a select statement.
*/
public DMISelectStatement ( DMI dmi ) {	
	super ( dmi );
	_distinct = false;
}

/**
Executes this statement's stored procedure to query data.  If this statement was not set
up as a Stored Procedure, an exception will be thrown.
@return the ResultSet that was returned from the query.
*/
public ResultSet executeStoredProcedureQuery()
throws SQLException {
	if (!isStoredProcedure()) {
		throw new SQLException("Cannot use executeStoredProcedure() to "
			+ "execute a DMISelectStatement that is not a stored procedure.");
	}
	// Put together the query string for troubleshooting
	
	// executeQuery returns a result set by fetching some data from the database.
	// It executes only select statements.
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
Sets whether a TOP clause should be used (the syntax will vary by database engine).
@param top number of rows to return
*/
public void setTop(int top) {
    _top = top;
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
		statement.append ( _field_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " + _field_Vector.get(i) );
		}
	}
	
	size = _table_Vector.size();
	if (size > 0 && _join_Vector.size() == 0) {
		statement.append ( " FROM " );
		statement.append ( _table_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " + _table_Vector.get(i) );
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
		
		statement.append(_table_Vector.get(0));

		for (int i = 0; i < size; i++) {
			type = ((Integer)_join_type_Vector.get(i)).intValue();
			if (type == _JOIN_INNER) {
				statement.append(" INNER JOIN ");
			}
			else if (type == _JOIN_LEFT) {
				statement.append(" LEFT JOIN ");
			}
			else if (type == _JOIN_RIGHT) {
				statement.append(" RIGHT JOIN ");
			}
			s = _join_Vector.get(i);
			statement.append(s + " ON ");
			s = _on_Vector.get(i);
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
		statement.append ( _where_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( " AND (" + _where_Vector.get(i) + ")");
		}
	}
	
	size = _order_by_Vector.size();
	if (size > 0) {
		if (_groupBy) {
			statement.append(" GROUP BY ");
		} else {
			statement.append ( " ORDER BY " );
		}
		statement.append ( _order_by_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " + _order_by_Vector.get(i));
		}
	}
	return statement.toString();
}

/**
Format the SELECT statement for SQL.
If a stored procedure, the parent class toString() is used.
@return the SELECT statement as a string.
*/
public String toString() {	
	if ( this.isStoredProcedure() ) {
		return super.toString();
	}

	if (_dmi.getDatabaseEngine().equalsIgnoreCase("Access")) {
		return toAccessString();
	}

	StringBuffer statement = new StringBuffer("SELECT ");

    if ( _top > 0 ) {
        if ( _dmi.getDatabaseEngineType() == DMI.DBENGINE_SQLSERVER ) {
            statement.append ( "TOP " + _top + " " );
        }
    }
	   
	if ( _distinct ) {
		statement.append ( "DISTINCT " );
	}
	
	int size = _field_Vector.size();
	if (size > 0) {
		statement.append ( _field_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
	        statement.append ( ", " + DMIUtil.escapeField(_dmi,_field_Vector.get(i)) );
		}
	}
	
	size = _table_Vector.size();
	if (size > 0) {
		statement.append ( " FROM " );
		statement.append ( _table_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " + _table_Vector.get(i) );
		}
	}
	
	size = _join_Vector.size();
	if (size > 0) {
		int type = -1;
		String s = null;
		for (int i = 0; i < size; i++) {
			type = (_join_type_Vector.get(i)).intValue();
			if (type == _JOIN_INNER) {
				statement.append(" INNER JOIN ");
			}
			else if (type == _JOIN_LEFT) {
				statement.append(" LEFT JOIN ");
			}
			else if (type == _JOIN_RIGHT) {
				statement.append(" RIGHT JOIN ");
			}
			s = _join_Vector.get(i);
			statement.append(s + " ON ");
			s = _on_Vector.get(i);
			statement.append(s);
		}
	}
	
	int whereSize = _where_Vector.size();
	if (whereSize > 0) {
		statement.append ( " WHERE " );
		statement.append ( _where_Vector.get(0) );
		for ( int i = 1; i < whereSize; i++ ) {
			statement.append ( " AND (" + _where_Vector.get(i) + ")");
		}
	}
    if ( _top > 0 ) {
        if ( _dmi.getDatabaseEngineType() == DMI.DBENGINE_ORACLE ) {
            if ( whereSize == 0 ) {
                statement.append ( " WHERE " );
            }
            else {
                statement.append ( " AND " );
            }
            statement.append ( " (ROWNUM <= " + _top + ")" );
        }
    }
	
	size = _order_by_Vector.size();
	if (size > 0) {
		if (_groupBy) {
			statement.append(" GROUP BY ");
		} else {
			statement.append ( " ORDER BY " );
		}
		statement.append ( _order_by_Vector.get(0) );
		for ( int i = 1; i < size; i++ ) {
			statement.append ( ", " + _order_by_Vector.get(i));
		}
	}
	
    if ( _top > 0 ) {
        if ( _dmi.getDatabaseEngineType() == DMI.DBENGINE_MYSQL ) {
            statement.append ( " LIMIT " + _top );
        }
    }

	// Add the trailing character to the statement, may be empty string.
	String end = _dmi.getStatementEnd();
	if ( !end.isEmpty() && !statement.toString().endsWith(end) ) {
		statement.append(end);
	}
	
	return statement.toString();
}

}