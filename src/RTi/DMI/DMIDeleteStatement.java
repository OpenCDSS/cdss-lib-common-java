// DMIDeleteStatement - delete statement

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
