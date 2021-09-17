// DMIDatabaseType - enumeration for database types (referred to as database engine in some code)

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

/**
This enumeration stores values for a database types, which indicate the database server software.
*/
public enum DMIDatabaseType
{

	/**
	Database engine corresponding to "Access" database engine (Jet).
	*/
	ACCESS("Access"),

	/**
	Database engine corresponding to "Derby" database engine (Oracle implementation of Apache Derby).
	*/
	DERBY("Derby"),

	/**
	Database engine corresponding to "Excel" database engine (no distinction about version?).
	*/
	EXCEL("Excel"),

	/**
	Database engine corresponding to "H2" database engine
 	*/
	H2("H2"),

	/**
	Database engine corresponding to "Informix" database engine (no distinction about version?).
	*/
	INFORMIX("Informix"),

	/**
	Database engine corresponding to "MySQL" database engine (no distinction about version?).
	*/
	MYSQL("MySQL"),

	/**
	Database engine corresponding to ODBC DSN database connection but engine type is not
	specifically known to this code.  Useful for generic connections.
	*/
	ODBC("ODBC"),

	/**
	Database engine corresponding to "Oracle" database engine (no distinction about version?).
	*/
	ORACLE("Oracle"),

	/**
	Database engine corresponding to "PostgreSQL" database engine (no distinction about version?).
	*/
	POSTGRESQL("PostgreSQL"),

	/**
	Database engine corresponding to SQLite database.
	*/
	SQLITE("SQLite"),

	/**
	Database engine corresponding to "SQL Server" database engine (2000, 2005, or 2008).
	Previously had definitions for the following but the current 2008 JDBC driver is advertised to be
	backward compatible so only use the one value now:
	<pre>
	protected final int _DBENGINE_SQLSERVER7 = 50;
	protected final int _DBENGINE_SQLSERVER2000 = 60;
	protected final int _DBENGINE_SQLSERVER_2005 = 61;
	</pre>
	*/
	SQLSERVER("SQLServer" );

	/**
 	* The name that should be displayed when the best fit type is used in UIs and reports.
 	*/
	private final String displayName;

	/**
	Construct an enumeration value.
	@param displayName name that should be displayed in choices, etc.
	*/
	private DMIDatabaseType(String displayName) {
    	this.displayName = displayName;
	}

	/**
	Return the display name for the enumeration string.  This is usually the same as the
	value but using appropriate mixed case.
	@return the display name.
	*/
	@Override
	public String toString() {
    	return displayName;
	}

	/**
	Return the enumeration value given a string name (case-independent).
	@return the enumeration value given a string name (case-independent), or null if not matched.
	*/
	public static DMIDatabaseType valueOfIgnoreCase(String name) {
	   if ( name == null ) {
        	return null;
    	}
    	DMIDatabaseType [] values = values();
    	// Currently supported values
    	for ( DMIDatabaseType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	} 
    	// Special cases for backward compatibility.
       	if ( name.equalsIgnoreCase("SQL_Server") ) {
       		return SQLSERVER;
       	}
    	return null;
	}

}