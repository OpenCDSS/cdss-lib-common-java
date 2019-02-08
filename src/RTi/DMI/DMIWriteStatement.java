// DMIWriteStatement - write statement

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

import RTi.Util.Message.Message;

/**
The DMIWriteStatement class stores basic information about SQL write statements, allowing write statements
to be constructed for execution.
See HydroBaseDMI for many good examples of using this class.
*/
public class DMIWriteStatement 
extends DMIStatement {

/**
Construct a select statement.
*/
public DMIWriteStatement ( DMI dmi ) {	
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
			+ "execute a DMIWriteStatement that is not a stored procedure.");
	}
	__storedProcedureCallableStatement.executeUpdate();
}

/**
Removes the name of the table from fields stored in the SQL, if the fields have the table name.  
@param fieldName the field name to check for a table name and remove, if present.
@return the field name without the table name.
*/
private String removeTableName(String fieldName) {
	int loc = fieldName.indexOf(".");
	return(fieldName.substring(loc + 1));
}

/**
Format the INSERT statement.
@return the INSERT statement as a string.
*/
public String toInsertString() {
	StringBuffer statement = new StringBuffer("INSERT INTO ");

	int size = _table_Vector.size();
	if ( size > 0 ) {
		statement.append ( _table_Vector.get(0) );
		statement.append (" (");
	} else {
		Message.printWarning(2, "DMIWriteStatement.toInsertString", 	
			"No table specified to use in creation of SQL");
		return "";
	}

	size = _field_Vector.size();
	if (size > 0) {
		statement.append ( DMIUtil.escapeField(_dmi,removeTableName(_field_Vector.get(0))));

		for (int i = 1; i < size; i++) {
			statement.append(", " + DMIUtil.escapeField(_dmi,removeTableName(_field_Vector.get(i))));
		}
	} else {
		Message.printWarning(2, "DMIWriteStatement.toInsertString", 	
			"No fields specified to use in creation of SQL");
		return "";
	}
	
	statement.append (") VALUES (");
	size = _values_Vector.size();
	if (size > 0) {
		statement.append(_values_Vector.get(0));

		for (int i = 1; i < size; i++) {
			statement.append (", " + _values_Vector.get(i));
		}
	} else {
		Message.printWarning(2, "DMIWriteStatement.toInsertString", 	
			"No values specified to use in creation of SQL");
		return "";
	}

	statement.append(")");
	
	return statement.toString();
}

/**
Format the WRITE statement.
@return the WRITE statement as a string.
*/
public String toUpdateString() {
	return toUpdateString(false);
}

/**
Format the WRITE statement.
@param tryBuildWhere if set to true and the write statement was never set a 
where clause, this will try to build a where clause from the fields and values
in the rest of the write statement.  In other words, it will create a write 
statement that sets a row in the database to the row's same values.  This is
used when doing UPDATE_INSERTs in the DMI.
@return the WRITE statement as a string.
*/
public String toUpdateString(boolean tryBuildWhere) {
	StringBuffer statement = new StringBuffer("UPDATE ");

	int size = _table_Vector.size();
	if ( size > 0 ) {
		statement.append ( _table_Vector.get(0) );
		statement.append (" ");
	} else {
		Message.printWarning(2, "DMIWriteStatement.toUpdateString", 	
			"No table specified to use in creation of SQL");
		return "";
	}

	statement.append("SET ");

	size = _field_Vector.size();
	int size2 = _values_Vector.size();

	if (size != size2) {
		Message.printWarning(2, "DMIWriteStatement.toUpdateString", 	
			"Can't build SQL with " + size + " column names " +
			"and " + size2 + " values to put in those columns.");
		return "";
	}
	else if ( size > 0 ) {
		statement.append (DMIUtil.escapeField(_dmi,_field_Vector.get(0)));
		statement.append (" = ");
		statement.append (_values_Vector.get(0));

		for (int i = 1; i < size; i++) {
			statement.append (", ");
			statement.append (DMIUtil.escapeField(_dmi,_field_Vector.get(i)));
			statement.append (" = ");
			statement.append (_values_Vector.get(i));
		}
	}

	statement.append(" WHERE ");

	size = _where_Vector.size();
	if (size > 0) {
		statement.append (_where_Vector.get(0));
		
		for (int i = 1; i < size; i++) {
			statement.append(" AND ");
			statement.append(_where_Vector.get(i));
		}
	}
	else if ( (_field_Vector.size() > 0) && tryBuildWhere) {
		statement.append(DMIUtil.escapeField(_dmi,_field_Vector.get(0)));
		statement.append(" = ");
		statement.append(_values_Vector.get(0));

		size = _field_Vector.size();
		for (int i = 1; i < size; i++) {
			statement.append (" AND ");
			statement.append (DMIUtil.escapeField(_dmi, _field_Vector.get(i)));
			statement.append (" = ");
			statement.append (_values_Vector.get(i));
		}
	}
	else {
		Message.printWarning(2, "DMIWriteStatement.toUpdateString",
			"No where clause specified for update SQL");
		return "";
	}
	
	return statement.toString();
}

public String toString() {
	return "Insert string version: \n" + toInsertString() + "\nUpdate string version: \n" + toUpdateString(true);
/*		
	String s= "Insert string version: \n" + toInsertString() + "\n\nUpdate "
		+ "string version: \n" + toUpdateString(true) + "\n";
	for (int i = 0; i < _table_Vector.size(); i++) {
		s += "Table (" +i+ "): '" + _table_Vector.elementAt(i) + "'\n";
	}
	for (int i = 0; i < _where_Vector.size(); i++) {
		s += "where (" +i+ "): '" + _where_Vector.elementAt(i) + "'\n";
	}
	for (int i = 0; i < _values_Vector.size(); i++) {
		s += "values (" +i+ "): '"+_values_Vector.elementAt(i) + "'\n";
	}
	for (int i = 0; i < _field_Vector.size(); i++) {
		s += "field (" +i+ "): '" + _field_Vector.elementAt(i) + "'\n";
	}
	return s;
*/
}

}
