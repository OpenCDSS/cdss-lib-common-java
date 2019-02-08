// GenericDMI extends DMI - DMI object for generic database connection

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

// ----------------------------------------------------------------------------
// GenericDMIJava - base class for a Generic DMI
// This is basically just a very bare-bones DMI for doing database operations.
// The DMI class is abstract and so cannot be instantiated by itself.  If
// there is a need to simply connect to a database and pass in SQL strings to
// run queries, the GenericDMI can be instantiated and used to do that.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2002-07-16	J. Thomas Sapienza, RTi	Initial Version
// 2003-01-03	Steven A. Malers, RTi	Make changes consistent with the updated
//					DMI base class.  Looks like this class
//					needs a lot of work anyhow?
// 2003-05-07	JTS, RTi		Finall did some javadoc'ing.
// ----------------------------------------------------------------------------

package RTi.DMI;

/**
Bare-bones class for doing simple and quick DMI work.  Mostly it just implements
the abstract methods in the base DMI class, but it has no database-specific
code to get in the way of doing simple JDBC work.<p>
This class is useful for DMI debugging purposes.  It isn't
tied to any particular database and so can be used for checking connections,
executing queries (with dmiSelect) and writes (with dmiWrite).  
Here is an example of use:
<pre>
	GenericDMI dmi = null;
	dmi = new GenericDMI("SQLServer", "localhost", "RiversideDB", 1433, "sa", "sa");
	dmi.open();
	ResultSet rs = dmi.dmiSelect("select * from geoloc");
	if (rs.next()) {
		System.out.println("'''" + rs.getString(8) + "'''");
	}
</pre>
*/
public class GenericDMI extends DMI {

/**
Builds a DMI connection to the named ODBC connection.
@param database_engine the kind of database that is running
@param ODBC_name the name of the ODBC data source to connect to.
@param system_login the login to use to log into the ODBC data source
@param system_password the password to use to log into the ODBC data source
@throws Exception if an error occurs
*/
public GenericDMI (String database_engine, String ODBC_name,
String system_login, String system_password )
throws Exception {
	super ( database_engine, ODBC_name, system_login, system_password );
}

/**
Builds a DMI connection to the named database.
@param database_engine the kind of database that is running
@param database_server the machine on which the database is running
@param database_name the name of the database to which to connect
@param port the port on which the database is running
@param system_login the login to use to log into the ODBC data source
@param system_password the password to use to log into the ODBC data source
@throws Exception if an error occurs
*/
public GenericDMI (String database_engine, String database_server,
String database_name, int port,	String system_login, String system_password )
throws Exception {
	super ( database_engine, database_server, database_name, port, system_login, system_password );
}

/**
Does nothing.
*/
public void determineDatabaseVersion() {
	setDatabaseVersion(0);
}

/**
Does nothing.
*/
public void readGlobalData() {
}

}
