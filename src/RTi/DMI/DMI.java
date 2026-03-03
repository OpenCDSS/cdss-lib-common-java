// DMI - base class for all database DMI operations

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
The DMI class serves as a base class to allow interaction with ODBC/JDBC compliant database servers.
Derived classes should define specific code to implement a connection with the database.
In particular, a derived class should:

<ol>
<li>	Call the appropriate constructor in this base class when constructing an instance.</li>
<li>	Implement a determineDatabaseVersion() method,
		which is called from the open() method in this base DMI class.
		The determineDatabaseVersion() method should call the setDatabaseVersion() method of this DMI base class.</li>
<li>	Implement a readGlobalData() method to read global data that will be kept in memory in the derived DMI class
		(e.g., for commonly used data like units).
<li>	Implement a getDatabaseProperties() method that can be used by an application
		(e.g., to show database properties to a user.</li>
<li>	Use the DMI*Statement objects to create SQL statements.  Execute the statements using dmi*() methods.</li>
<li>	Implement constructors that follow the guidelines described below.
</ol>

To create a connection to a database, a DMI instance should be created using one of the two constructors.
The constructors are provided for the two main ways to connect to a database:

<ol>
<li>	Using an ODBC DSN that is defined on the machine.
		In this case only the ODBC name is required from the derived class.</li>
<li>	Using a database server name and database name on the server.
		<b>This method is preferred with Java because it allows individual
		JDBC drivers to be used.</b>  In this case the constructor
		in the derived class should pass the system login and password used to make the connection,
		as well as other information that may be required for the specific database.</li>
</ol>

The open() method in this base class will make a database connection using only one of the above methods,
depending on whether the ODBC name or the database name and server have been defined.

A new DMI should typically be constructed only after an existing DMI is destroyed.
Application code should include code similar to the following:

<pre>
someDMI dmi = null;
...
// Doing a database login/connection.
if ( dmi != null ) {
	try {
		dmi.close();
		dmi = null;
	}
	catch ( Exception e ) {
		// Usually can ignore.
	}
}

try {
	dmi = new someDMI ( ... );
	// If necessary.
	dmi.setXXX();
	dmi.open();
}
catch ( Exception e ) {
	// Error message/dialog.
}
</pre>
*/
public abstract class DMI {

public final static String CLASS = "DMI";

///////////////////////////////////////////////////////////
//  Commit / Rollback constants
///////////////////////////////////////////////////////////

/**
Constant for referring to ROLLBACK operations.
*/
public static final int ROLLBACK = 0;

/**
Constant for referring to COMMIT operations.
*/
public static final int COMMIT = 1;

///////////////////////////////////////////////////////////
//  SQL Type constants
///////////////////////////////////////////////////////////
/**
Constant used to tell that the last query type was NONE.
*/
public static final int NONE = 0;

/**
Constant used to tell that the last query type was COUNT.
*/
public static final int COUNT = 1;

/**
Constant used to tell that the last query type was DELETE.
*/
public static final int DELETE = 2;

/**
Constant used to tell that the last query type was SELECT.
*/
public static final int SELECT = 3;

/**
Constant used to tell that the last query type was a WRITE.
*/
public static final int WRITE = 4;

/**
Constant used in dmiWrite() to specify to first do an INSERT, and if that fails, try to do an UPDATE.
*/
public static final int INSERT_UPDATE = 6;

/**
Constant used in dmiWrite() to specify to first do an UPDATE, and if that fails, try to do an INSERT.
*/
public static final int UPDATE_INSERT = 7;

/**
Constant used in dmiWrite() to specify to first delete the record before inserting a new one.
*/
public static final int DELETE_INSERT = 8;

/**
Constant used in dmiWrite() to specify to do an UPDATE.
*/
public static final int UPDATE = 9;

/**
Constant used in dmiWrite() to specify to do an INSERT.
*/
public static final int INSERT = 10;

///////////////////////////////////////////////////////////
//  Member variables
///////////////////////////////////////////////////////////

/**
Indicate whether database changes are automatically committed once they are done.
*/
private boolean __autoCommit;

/**
Indicate whether the SQL should be capitalized before being sent to the database.
*/
private boolean __capitalize;

/**
If true, then when an SQL statement is executed and an error occurs,
the text of the SQL statement will be printed to the log file at Status level 2.
This should only be used for debugging and testing, and should not be enabled in production code.
*/
private boolean __dumpSQLOnError = false;

/**
If true, then when an SQL statement is executed,
the text of the SQL statement will be printed to the log file at Status level 2.
This should only be used for debugging and testing, and should not be enabled in production code.
*/
private boolean __dumpSQLOnExecution = false;

/**
Indicate whether an JDBC connection is automatically set up (true) or
whether the ODBC is defined on the machine (false).
*/
private boolean isJdbc;

/**
Whether the program is currently connected to the database or not.
*/
private boolean __connected;

/**
Whether operations are currently being done in a transaction.
*/
private boolean __inTransaction = false;

/**
Whether for the open() methods to print information to debug as normal (false)
or to force the output to go to status level 1 (true).
This is used with troubleshooting.  Usually this should be false.
*/
private boolean __printStatus = false;

/**
Connection that is used for the database interaction.
*/
private Connection __connection;

/**
 * Additional connection properties provided in the constructor:
 * - this are granular properties for the connection string that the engine must handle
 * - the older 'additionalConnectionProperties' string is now deprecated
 */
private Map<String,Object> connectionPropertiesMap = new HashMap<>();

/**
Name of the database in the server if __jdbc_odbc is true.
*/
private String __database_name;

/**
ODBC Data Source Name if __jdbc_odbc is false.
*/
private String __odbc_name;

/**
Additional connection properties, which will be added at the end of the connection URL.
If a leading delimiter is needed such as ?, semi-colon or &, include in the string,
but it should be adjusted.
These are typically passed in for datastores that require special properties.
Set with setAdditionalConnectionProperties() before calling open().
Login and password are handled separately from the connection URL.

Alternatively, use the connection properties map in the constructor, for example with ConnectionUrlAppend.
@deprecated
*/
private String __additionalConnectionProperties = "";

/**
Database engine to connect to, as a string, useful for debugging.
<p>
Valid types are:<br>
<ul>
<li>Access</ul>
<li>Excel</ul>
<li>Informix</ul>
<li>MySQL</ul>
<li>Oracle</ul>
<li>PostgreSQL</ul>
<li>SQLite</ul>
<li>SQLServer</ul>
</ul>
*/
private String __database_engine_String;

/**
The left-side escape string to wrap fields so that they are not mistaken for reserved words.
This leads to more verbose SQL but is conservative.  For example, for SQL Server, "[" is used.
*/
private String __fieldLeftEscape = "";

/**
The right-side escape string to wrap fields so that they are not mistaken for reserved words.
This leads to more verbose SQL but is conservative.  For example, for SQL Server, "]" is used.
*/
private String __fieldRightEscape = "";

/**
The delimiter for strings, such as single quotes.
*/
private String __stringDelim = "";

/**
The character for statement end, such as semicolon for SQLite.
This is used in auto-generated internal SQL.
*/
private String __statementEnd = "";

/**
Database engine as enumeration.
*/
protected DMIDatabaseType _database_engine;

/**
Database version.  This should be set by calling determineDatabaseVersion(),
which is an abstract method to be defined in derived DMI classes.
The standard for the version number is XXXXXXDDDDDDDD where XXXXXXX is typically a database version
(e.g., RiverTrak 020601) and DDDDDDDD is usually the build date (e.g., 20020625).
*/
private long __database_version;

/**
True if any writes have been done to the database (uncommitted).
*/
private boolean __dirty = false;

/**
Whether the DMI should treat the database as being in read-only mode
(false, all calls to DMI.delete() or DMI.write() are ignored) or not.
*/
private boolean __editable;

/**
Host name or IP address of the database server (or "Local" if the local host is used).
*/
private String __database_server;

/**
The last SELECT query SQL string that was executed.
*/
private DMISelectStatement __lastQuery;

/**
The last SQL statement that was executed.
*/
private DMIStatement __lastSQL;

/**
The type of the last SQL executed.
*/
private int __lastSQLType;

/**
The login timeout to use when establishing the database connection.
*/
private int __loginTimeout = -1;

/**
Port number of the database to connect, used by client-server databases.
*/
private int __port;

/**
Sets whether in toString and getDatabaseProperties to print out information that may be secure
(such as login name and password), or not.
*/
private boolean __secure = false;

/**
ID string to identify the DMI connection.
*/
private String __id;

/**
Name string to identify the DMI connection.
*/
private String __inputName = "";

/**
Login name to connect to the database (often used in database connection URL).
*/
private String __system_login;

/**
Password to connect to the database (often used in the database connection URL).
*/
private String __system_password;

/**
User login name to restrict access to the database.
This is typically more restrictive than the system login but may be the same.
*/
private String __user_login;

/**
User password to restrict access to the database.
This is typically more restrictive than the system password but may be the same.
*/
private String __user_password;

/**
List for holding all the statements that were created during a transaction,
so they can be closed when the transaction is committed or rolled back.
*/
private List<Statement> __statementsVector;

/**
An empty constructor.
If this constructor is used, initialize() must be called with the proper values to initialize the DMI settings.
*/
public DMI() {
}

/**
Constructor for this DMI base class for cases when a connection to an ODBC DSN defined on the machine.
The generic JDBC/ODBC driver is used.  To use a manufacturer's driver,
use the overloaded constructor that takes as a parameter the database name.
@param databaseEngine Database engine type (see setDatabaseEngine()).
@param odbcName ODBC DSN to use.  The ODBC DSN must be defined on the machine.
@param systemLogin System login to use for the database connection.
Specify as null to not use.
Generally the login will be defined in a derived class and is specific to a database.
@param systemPassword System login to use for the database connection.
Specify as null to not use.  Generally the login will be defined in a derived class and is specific to a database.
@throws Exception thrown if an unknown databaseEngine is specified.
*/
public DMI ( String databaseEngine, String odbcName, String systemLogin, String systemPassword )
throws Exception {
	Map<String,Object> connectionPropertiesMap = new HashMap<>();
	initialize (
		databaseEngine,
		null, // Database server, not used with ODBC name.
		null, // Database name, not used with ODBC name.
		0, // Port, not used with ODBC name.
		systemLogin,
		systemPassword,
		odbcName,
		false, // True if using JDBC (false here since using ODBC).
		connectionPropertiesMap );
}

/**
Constructor for this DMI base class for cases when a connection to a server database will be defined automatically,
not using a predefined ODBC DSN.
The specified JDBC/ODBC driver for the database engine is used.
To use the generic JDBC/ODBC driver with an ODBC DSN, use the overloaded constructor that takes as a parameter the ODBC name.
@param databaseEngine Database engine type (see setDatabaseEngine()).
@param databaseServer Database server name (IP address or DNS-resolvable name).
@param databaseName Database name on the server.
@param port Port to use for the database communications.
Specify as a negative number to use the default.
Generally the default value determined from the database engine is correct.
@param systemLogin System login to use for the database connection.
Specify as null to not use.  Generally the login will be defined in a derived class and is specific to a database.
@param systemPassword System login to use for the database connection.
Specify as null to not use.  Generally the login will be defined in a derived class and is specific to a database.
@throws Exception thrown if an unknown databaseEngine is specified.
*/
public DMI (
	String databaseEngine,
	String databaseServer,
	String databaseName,
	int port,
	String systemLogin,
	String systemPassword )
	throws Exception {
	// No additional properties.
	Map<String,Object> connectionPropertiesMap = new HashMap<>();
	initialize (
		databaseEngine,
		databaseServer,
		databaseName,
		port,
		systemLogin,
		systemPassword,
		null, // No ODBC name is used.
		true, // Using JDBC.
		connectionPropertiesMap );
}

/**
Constructor for this DMI base class for cases when a connection to a server database will be defined automatically,
not using a predefined ODBC DSN.
The specified JDBC/ODBC driver for the database engine is used.
To use the generic JDBC/ODBC driver with an ODBC DSN, use the overloaded constructor that takes as a parameter the ODBC name.
@param databaseEngine Database engine type (see setDatabaseEngine()).
@param databaseServer Database server name (IP address or DNS-resolvable name).
@param databaseName Database name on the server.
@param port Port to use for the database communications.
Specify as a negative number to use the default.
Generally the default value determined from the database engine is correct.
@param systemLogin System login to use for the database connection.
Specify as null to not use.  Generally the login will be defined in a derived class and is specific to a database.
@param systemPassword System login to use for the database connection.
Specify as null to not use.  Generally the login will be defined in a derived class and is specific to a database.
@param connectionPropertiesMap map of properties specific to a database engine.  The following are supported:
@throws Exception thrown if an unknown databaseEngine is specified.
*/
public DMI (
	String databaseEngine,
	String databaseServer,
	String databaseName,
	int port,
	String systemLogin,
	String systemPassword,
	Map<String,Object> connectionPropertiesMap )
	throws Exception {
	// No additional properties.
	initialize (
		databaseEngine,
		databaseServer,
		databaseName,
		port,
		systemLogin,
		systemPassword,
		null, // No ODBC name is used.
		true, // Using JDBC.
		connectionPropertiesMap );
}

/**
Initialization routine that sets up the internal DMI settings.
@param databaseEngine the type of database engine to which the DMI is connecting.
@param databaseServer the name or IP of the server to which the DMI will connect.
@param databaseName the name of the database on the server that the DMI will connect to.
@param port the port on the database_server where the database listens for connections.
@param systemLogin the login value to use to connect to the database server or ODBC connection.
@param ssytemPassword the login password to use to connect to the database server or ODBC connection.
@param odbcName the name of the ODBC data source to connect to.
@param isJdbc if true then the connection is a JDBC IP or server-name connection,
and if false, it is an ODBC name connection
@param connectionPropertiesMap map of properties specific to a database engine.  The following are supported:
<pre>
Engine     Property Name              Type        Description
=========  =========================  ==========  ================================================================
All        ConnectionStringAppend     String      Text to append to the connection URL.
Oracle     OracleWallet               String      Full path to the Oracle Wallet folder
                                                  (will set as TNS_ADMIN=OracleWallet in the connection string).
</pre>
@throws Exception if the databaseEngine that is passed in is not recognized.
*/
public void initialize (
	String databaseEngine,
	String databaseServer,
	String databaseName,
	int port,
	String systemLogin,
	String systemPassword,
	String odbcName,
	boolean isJdbc,
	Map<String,Object> connectionPropertiesMap )
	throws Exception {
	String routine = getClass().getSimpleName() + ".initialize";
	int dl = 25;

	if ( Message.isDebugOn ) {
		Message.printDebug(dl, routine, "DMI created for database engine: " + databaseEngine);
	}
	Message.printStatus(2, routine, "Initializing DMI for database engine: " + databaseEngine);

	// Initialize member variables.
	this.__database_engine_String = databaseEngine;

	this.isJdbc = isJdbc;
	this.__database_name = databaseName;
	this.__odbc_name = odbcName;
	this.__database_server = databaseServer;
	this.__system_login = systemLogin;
	this.__system_password = systemPassword;
	this.__user_login = null;
	this.__user_password = null;
	this.__lastSQL = null;
	this.__lastQuery = null;
	this.__connection = null;
	if ( connectionPropertiesMap == null ) {
		this.connectionPropertiesMap = new HashMap();
	}
	else {
		this.connectionPropertiesMap = connectionPropertiesMap;
	}

	this.__database_version = 0;
	this.__port = port;

	this.__dirty = false;
	this.__editable = false;
	this.__secure = false;
	this.__autoCommit = true;
	this.__connected = false;

	this.__lastSQLType = NONE;

	// Check the database engine type and set appropriate defaults.
	if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("Access")) {
		this.__fieldLeftEscape = "[";
		this.__fieldRightEscape = "]";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this.__database_server = "Local";
		this._database_engine = DMIDatabaseType.ACCESS;
	}
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("Derby")) {
        this.__fieldLeftEscape = "";
        this.__fieldRightEscape = "";
        this.__stringDelim = "'";
		this.__statementEnd = "";
        if ( this.__database_server.equalsIgnoreCase("memory") ) {
            this.__database_server = "localhost";
        }
        this._database_engine = DMIDatabaseType.DERBY;
    }
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("Excel")) {
	    // TODO SAM 2012-11-09 Need to confirm how Excel queries behave, for now use Access settings.
        this.__fieldLeftEscape = "[";
        this.__fieldRightEscape = "]";
        this.__stringDelim = "'";
		this.__statementEnd = "";
        this.__database_server = "Local";
        this._database_engine = DMIDatabaseType.EXCEL;
    }
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("Informix")) {
		this.__fieldLeftEscape = "\"";
		this.__fieldRightEscape = "\"";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.INFORMIX;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
	}
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("MySQL")) {
		this.__fieldLeftEscape = "";
		this.__fieldRightEscape = "";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.MYSQL;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
	}
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("Oracle")) {
		this.__fieldLeftEscape = "\"";
		this.__fieldRightEscape = "\"";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.ORACLE;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
	}
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("PostgreSQL")) {
		// Escape for keywords.
		// See:  https://www.postgresql.org/docs/9.6/sql-keywords-appendix.html
		// The following caused issues:
		// - TODO smalers 2020-10-12 why is this an issue?
		//__fieldLeftEscape = "\"";
		//__fieldRightEscape = "\"";
		this.__fieldLeftEscape = "";
		this.__fieldRightEscape = "";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.POSTGRESQL;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
	}
	else if ( (this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("SQLite")) {
		// Escape the fields to make sure that reserved keywords, etc., are not an issue.
		// See:  https://sqlite.org/lang_keywords.html
		this.__fieldLeftEscape = "\"";
		this.__fieldRightEscape = "\"";
		this.__stringDelim = "'";
		this.__statementEnd = ";";
		this._database_engine = DMIDatabaseType.SQLITE;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
	}
	else if ((this.__database_engine_String != null) &&
		(StringUtil.startsWithIgnoreCase(this.__database_engine_String,"SQL_Server") || // Older.
		StringUtil.startsWithIgnoreCase(this.__database_engine_String,"SQLServer")) ) { // Current configuration file.
		this.__fieldLeftEscape = "[";
		this.__fieldRightEscape = "]";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.SQLSERVER;
		if ( port <= 0 ) {
			setDefaultPort ();
		}
		else {
            // Warn if the configuration has provided a port AND a named instance (e.g. localhost\SQLEXPRESS).
            if (this.__database_server.indexOf('\\') >= 0) {
                Message.printWarning(3, "initialize",
                	"SQLServer connection should either provide a named instance OR a port, but not both.");
            }
        }
	}
    else if ((this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("H2")) {
        this.__fieldLeftEscape = "";
		this.__fieldRightEscape = "";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.H2;
    }
    else if ((this.__database_engine_String != null) && this.__database_engine_String.equalsIgnoreCase("HSQLDB")) {
        this.__fieldLeftEscape = "";
		this.__fieldRightEscape = "";
		this.__stringDelim = "'";
		this.__statementEnd = "";
		this._database_engine = DMIDatabaseType.HSQLDB;
    }
	else {
	    if ( (this.__odbc_name != null) && !this.__odbc_name.equals("") ) {
	        // Using a generic ODBC DSN connection so assume some basic defaults.
	        this.__fieldLeftEscape = "";
	        this.__fieldRightEscape = "";
	        this.__stringDelim = "'";
	        this.__statementEnd = "";
	        this._database_engine = DMIDatabaseType.ODBC;
	    }
	    else {
	        // Using a specific driver but don't know which one, which is problematic for internals.
	        throw new Exception("Trying to use unknown database engine: " + this.__database_engine_String + " in DMI()");
	    }
	}

	this.__statementsVector = new Vector<>();
}

/**
 * Append a connection URL parameter.
 * If the parameter starts with a delimiter, the delimiter is removed and then re-added based on the 'paramCount' value
 * @param connUrlBuilder the connection string to modify
 * @param delim1 the delimiter for the first parameter (e.g., "?")
 * @param delim2 the delimiter for the second and later parameters (e.g., "&")
 * @param paramCount the count of parameters that have previously been appended
 * @param param the parameter to add (if null, don't append)
 * @return the count of parameters that have been appended (paramCount + 1)
 */
private int appendConnectionUrlParameter ( StringBuilder connUrlBuilder, String delim1, String delim2, int paramCount, String param ) {
	if ( param == null ) {
		// Don't append.
		return paramCount;
	}
	if ( paramCount == 0 ) {
		// Append the first delimiter.
		connUrlBuilder.append ( delim1 );
	}
	else {
		// Append the second delimiter.
		connUrlBuilder.append ( delim2 );
	}
	// Append the parameter:
	// - if it starts with either of the delimiters, remove because it is redundant and may not be correct
	if ( param.startsWith(delim1) ) {
		if ( param.length() == 1 ) {
			param = "";
		}
		else {
			param = param.substring(1);
		}
	}
	if ( param.startsWith(delim2) ) {
		if ( param.length() == 1 ) {
			param = "";
		}
		else {
			param = param.substring(1);
		}
	}
	// Append the parameter:
	// - if the length is zero, it is OK
	connUrlBuilder.append ( param );
	return (paramCount + 1);
}

/**
Close the database connection.  If the database is not connected yet, don't do anything.
@throws SQLException thrown if the java.sql code has
any problems doing a Connection.close()
*/
public void close() throws SQLException {
	String routine = getClass().getSimpleName() + ".close";
	// Let the JDBC handle the close.
	if (this.__connected) {
		if ( Message.isDebugOn ) {
			Message.printDebug(1, routine, "DMI database " + getDatabaseName() + " is connected.  Closing the connection.");
		}
		this.__connection.close();
		this.__connected = false;
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printDebug(1, routine, "DMI database " + getDatabaseName() + " is not connected.  No need to close the connection.");
		}
	}
}

/**
Closes a result set and frees the resources associated with it.
@param rs the ResultSet to close.
*/
public static void closeResultSet(ResultSet rs) {
    try {
        if (rs != null) {
        	// Get the statement so that it can be closed after closing the ResultSet.
    		Statement s = rs.getStatement();
    		// Close the ResultSet.
    		rs.close();
    		rs = null;
    		// Close the Statement.
    		if (s != null) {
    			s.close();
    			s = null;
    		}
    	}
    }
    catch ( SQLException e ) {
        // Swallow the exception since this is a utility method that is called to clean-up.
    }
}

/**
Closes a result set from a stored procedure and frees the resources associated with it.
@param rs the ResultSet to close.
@param select the select statement that was set up to execute the stored procedure.
*/
public static void closeResultSet(ResultSet rs, DMIStatement select)
throws SQLException {
	if (rs != null) {
       	// Get the statement so that it can be closed after closing the ResultSet.
		Statement s = rs.getStatement();
   		// Close the ResultSet.
		rs.close();
		rs = null;
   		// Close the Statement.
		if (s != null) {
			s.close();
			s = null;
		}
	}

	// Also call the associated callable statement.
	if (select.getCallableStatement() != null) {
		select.getCallableStatement().close();
	}
}

/**
Closes an statements that were opened during a transaction.
Called automatically by commit() and rollback().
*/
private void closeStatements() {
	String routine = "DMI.closeStatements()";
	int size = this.__statementsVector.size();
	Statement s = null;
	for (int i = 0; i < size; i++) {
		s = this.__statementsVector.get(i);
		try {
			s.close();
		}
		catch (Exception e) {
			Message.printWarning(3, routine, "Error closing statement:");
			Message.printWarning(3, routine, e);
		}
	}
}

/**
Commits any database operations that have been made since the beginning of the current transaction.
@throws SQLException thrown if the java.sql code has any problems
doing a Connection.commit() or in setAutoCommit(),
or if the database was not connected when the call was made
*/
public void commit() throws SQLException {
	// TODO (JTS - 2006-05-22)This code has not been tested or used in nearly 4 years.
	// Do not rely on this method without testing it first.
	if (!this.__connected) {
		throw new SQLException ("Database not connected, cannot call DMI.commit().");
	}

	this.__inTransaction = false;

	// Connection.commit() should only be used when autoCommit is turned off, so check that.
	if (this.__autoCommit == false) {
		this.__connection.commit();
	}

	// Since commit() marks the end of a transaction,
	// turn autoCommit back on and mark the database as not dirty (clean).
	setAutoCommit(true);
	this.__dirty = false;

	closeStatements();
}

/**
Indicate whether the database is connected.
@return true if the database connection is made, false otherwise.
*/
public boolean connected() {
	return this.__connected;
}

/**
Determine the version of the database.
This method should be defined in a derived class and be called when a database connection is made
(e.g., in the derived class open() method).
The database version should be set to 0 (zero) if it cannot be determined.
*/
public abstract void determineDatabaseVersion ();

/**
Execute a count query to find the number of records specified by the query.
This may be used in conjunction with a select statement to find out how many records will be pulled back
(for doing a progress bar or the like),
so the input SQL string can be a properly-formatted <b><code>SELECT </b></code> statement,
or a normal <b><code>SELECT COUNT</b></code> statement.<p>
If a normal SELECT statement is passed in,
this method will chop off any <b><codE>ORDER BY</b></code> clauses and
will also remove the fields that are being selected and replace them with <b><code>COUNT(*)</b></code>.<p>
For instance, the SQL:<br>
<code>
SELECT field1, field2 FROM tableName where field3 = 123 ORDER BY field4;<p>
</code> will converted to:<br>
<code>
SELECT COUNT(*) FROM tableName where field3 = 123;<p>
</codE> prior to being run.
<p>
<b>Known issues</b><br>
A problem has been encountered with an ODBC connection to a Microsoft Access
97 database in which malformed COUNT() statements have returned incorrect results.
<p>
An SQL statement of SELECT COUNT(*) ASDKJASD (where ASDKJASD could actually
be pretty much any word or combination of letters) would return a result that
said that 1 record was returned.  It is unclear why this SQL statement was
not returning an error from the database that the SQL was malformed.
@param sql a Select statement for which the number of records that will
be affected is the result wanted
@return an integer telling how many records were counted
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery(), or if the database was not connected
*/
public int dmiCount(String sql) throws SQLException{
	// TODO (JTS - 2006-05-22) This code has not been tested or used in nearly 4 years.
	// Do not rely on this method without testing it first.
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiCount().");
	}

	String routine = "DMI.dmiCount";
	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL to count (pre): '" + sql + "'");
	}

	sql = sql.trim();
	sql = sql.toUpperCase();

	// If the sql statement is already formatted as a SELECT COUNT statement, do nothing with it.
	if (!sql.startsWith("SELECT COUNT")) {
		// Otherwise, the sql is formatted as a SELECT statement so the 'SELECT [fields]' part is removed.
		if (sql.indexOf("FROM") >= 0) {
			sql = sql.substring(sql.indexOf("FROM"));
			// And then the ORDER BY clauses are removed as well, leaving behind only:
			//   "FROM [table] WHERE (wheres)"
			if (sql.indexOf("ORDER BY") >= 0) {
				sql = sql.substring(0, sql.indexOf("ORDER BY"));
			}
		}
		// Graft on the count statement to the beginning and it is functional SQL again, or should be.
		sql = "SELECT COUNT(*) " + sql;
	}

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL to count (post): '" + sql + "'");
	}

	Statement s = this.__connection.createStatement();
	ResultSet rs = s.executeQuery(sql);
	rs.next();
	int count = rs.getInt(1);

	closeResultSet(rs);
	if (this.__inTransaction) {
		this.__statementsVector.add(s);
	}
	else {
		s.close();
	}

	return count;
}

/**
Executes a count query from a DMISelectStatement object.
This creates the SQL string from the DMISelectStatement object and then passes the resulting string to the dmiCount(String) function.
@param s a DMISelectStatement object to run a count by
@return an integer telling how many records were counted
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery()
*/
public int dmiCount(DMISelectStatement s)
throws SQLException {
	// TODO (JTS - 2006-05-22) This code has not been tested or used in nearly 4 years.
	// Do not rely on this method without testing it first.
	if (!this.__connected) {
		throw new SQLException ("Database not connected, cannot call DMI.dmiCount().");
	}

	if (s.isStoredProcedure()) {
		ResultSet rs = s.executeStoredProcedureQuery();
		rs.next();
		return rs.getInt(1);
	}

	// Saves the DMISelectStatement as the last statement executed.
	setLastStatement(s);

	return dmiCount(s.toString());
}

/**
Executes a database delete from a DMIDeleteStatement object.
This method creates the SQL string from the DMIDeleteStatement object and then passes
the resulting string to the dmiDelete(String) function.<p>
The code in dmiDelete is surrounded by a check of the private boolean member
variable __editable (see isEditable() and setEditable(boolean)).
If __editable is set to true, none of the code is executed and an exception is thrown.
@param s a DMIStatement object containing a delete statement (should be a DMIDeleteStatement object)
@return the number of rows deleted
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery().  Also thrown if
the database is in read-only mode
*/
public int dmiDelete(DMIDeleteStatement s) throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected, cannot call DMI.dmiDelete().");
	}

	if (!this.__editable) {
		throw new SQLException("Database in read-only mode, cannot execute a dmiDelete.");
	}

	if (s.isStoredProcedure()) {
		s.executeStoredProcedure();
		return s.getIntReturnValue();
	}
	else {
		// Save the DMIDeleteStatement as the last statement executed.
		setLastStatement(s);
		return dmiDelete(s.toString());
	}
}

/**
Executes a database delete.  This method runs the SQL DELETE statement in its String parameter.<p>
The code in dmiDelete is surrounded by a check of the private boolean member
variable __editable (see isEditable() and setEditable(boolean)).
If __editable is set to true, none of the code is executed and an exception is thrown.
@param sql the SQL statement that contains the <b><code>DELETE</b></code> command
@return the number of rows deleted
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery().
Also thrown if the database is in read-only mode, or if it is not connected
*/
public int dmiDelete(String sql) throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected. Cannot make call to DMI.dmiDelete().");
	}

	if (this.__dumpSQLOnExecution) {
		Message.printStatus(2, "DMI.dmiDelete", sql);
	}

	String routine = "DMI.dmiDelete";
	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}

	if (!this.__editable) {
		throw new SQLException("Database in read-only mode, cannot execute a dmiDelete.");
	}

	Statement s = this.__connection.createStatement();
	if (this.__capitalize) {
		sql = sql.toUpperCase();
	}

	int result = 0;
	try {
		result = s.executeUpdate(sql);
	}
	catch (SQLException ex) {
		if (this.__dumpSQLOnError) {
			Message.printStatus(2, "DMI.dmiDelete", sql);
		}
		throw ex;
	}

	if (this.__inTransaction) {
		this.__statementsVector.add(s);
	}
	else {
		s.close();
	}

	// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
	// Since a delete statement causes a database change (and if the code has
	// gotten this far the delete was successful and didn't throw an exception),
	// the database can now be considered changed and the __dirty flag should be set.
	testAndSetDirty();
	return result;
}

/**
Executes a String of SQL code.  Used for doing create and drop table commands.
@param sql the command to execute
@return the number of lines affected
@throws Exception if an error occurs
*/
public int dmiExecute(String sql) throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiExecute.");
	}

	if (!this.__editable) {
		throw new SQLException ("Database is in read-only mode, cannot make call to DMI.dmiExecute.");
	}

	String routine = "DMI.dmiExecute";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}

	Statement s = this.__connection.createStatement();
	if (this.__capitalize) {
		sql = sql.toUpperCase();
	}

	int result = s.executeUpdate(sql);

	if (this.__inTransaction) {
		this.__statementsVector.add(s);
	}
	else {
		s.close();
	}

	return result;
}

/**
Re-executes the previous SQL statement executed by the database.
Every time one of the dmiCount, dmiDelete, dmiWrite or dmiSelect methods that take a DMIDeleteStatement, DMIWriteStatement or
DMISelectStatement as their parameter is called is called, the DMI stores the DMI*Statement.
This method re-runs the DMI*Statement executed just previously by the database.
<p>
If the database is accessed via one of the dmiCount, dmiDelete,
or dmiWrite statements that takes a String argument then no previous-sql information is saved.<p>
If the database is accessed via a dmiSelect statement that takes a String argument,
the SELECT statement is saved, but cannot be used by this method.
It saved for use in the dmiRunLastSelectForTableModel function.
<b>Note:</b><p>
If using stored procedures, this method WILL NOT WORK.
It only works with DMI connections that pass String-based SQL statements (either as Strings or DMIStatement objects).
@return an object, the type of which depends on which kind of query was being executed.
The problem here is that SELECT queries return
Vector objects while all other SQL statements return integers.
Any method which calls this function should first call <b><code>
getLastSQLType() </b></code> so that it knows what sort of object will be returned,
and then cast the return type appropriately.<p>
<code>
int type = rdmi.getLastSQLType();<br>
if (type == DMI.SELECT) {<br>
&nbsp;&nbsp;&nbsp;Vector results = (Vector) dmiRunSQL();<br>
} else { // for all other types<br>
&nbsp;&nbsp;&nbsp;Integer i = (Integer) dmiRunSQL();<br>
}<br>
@throws SQLException thrown if there are problems with a
Connection.createStatement, Statement.executeQuery(),
Statement.executeDelete() or Statement.executeUpdate()
@throws Exception thrown by dmiWrite(DMIWriteStatement) when a DELETE_INSERT is attempted
*/
public Object dmiRunLastSQL() throws SQLException, Exception {
	switch(this.__lastSQLType) {
		case NONE:
			return null;
		case SELECT:
			return dmiSelect((DMISelectStatement)this.__lastSQL);
		case WRITE:
			return Integer.valueOf(dmiWrite((DMIWriteStatement)this.__lastSQL, UPDATE_INSERT));
		case DELETE:
			return Integer.valueOf(dmiDelete((DMIDeleteStatement)this.__lastSQL));
		case COUNT:
			return Integer.valueOf(dmiCount((DMISelectStatement)this.__lastSQL));
		default:
			return null;
	}
}

/**
Re-executes the last SELECT statement executed.
Used for re-querying a table after a DELETE or INSERT statement is executed.
<code>dmiRunSQL</code> can't be used at that point as the last SQL executed will have been the DELETE or INSERT statement
@return a ResultSet of the values returned from the query
@throws SQLException thrown if there are problems with a Connection.createStatement or Statement.executeQuery()
*/
public ResultSet dmiRunSelect() throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiRunSelect().");
	}
	return dmiSelect((DMISelectStatement)this.__lastQuery);
}

// TODO SAM 2012-09-07 Need to set the last query string.
/**
Runs an SQL string that contains a <b><code>SELECT</b></code> statement and returns a resultSet of the records returned.
@param sql an SQL statement that contains the <b><code>SELECT</b></code> statement to be executed
@return the resultset pulled back from the operation
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery(), or if the database is not connected.
*/
public ResultSet dmiSelect(String sql) throws SQLException {
	String routine = getClass().getSimpleName() + ".dmiSelect";
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiSelect().");
	}

	if (this.__dumpSQLOnExecution) {
		Message.printStatus(2, routine, sql);
	}

	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}

	Statement s = this.__connection.createStatement();
	ResultSet rs = null;
	if (this.__capitalize) {
		sql = sql.toUpperCase();
	}

	// FIXME SAM 2008-04-15 Evaluate if needed.
	//__lastQueryString = sql;

	try {
		rs = s.executeQuery(sql);
	}
	catch (SQLException ex) {
		if (this.__dumpSQLOnError) {
			Message.printStatus(2, routine, sql);
		}
		throw ex;
	}
	// The statement will automatically be closed so don't do here.

	return rs;
}

/**
Execute an SQL select statement from a DMISelectStatement object.
The SQL statement is built from the DMISelectStatement object and the resulting string passed to dmiSelect(String).
The ResultSet must be closed by the calling code.
@param select an DMISelectStatement instance specifying the query.
@return the ResultSet from the select.
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery()
*/
public ResultSet dmiSelect ( DMISelectStatement select )
throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiSelect().");
	}

	if (select.isStoredProcedure()) {
		return select.executeStoredProcedureQuery();
	}
	else {
		// Set the DMISelectStatement as the last statement executed.
		setLastStatement(select);
		return dmiSelect(select.toString());
	}
}

/**
Runs an SQL string that contains a <b><code>SELECT</b></code> statement and returns a single value.
The SELECT statement must return a single value.
@param sql an SQL statement that contains the <b><code>SELECT</b></code> statement to be executed
@return the value from a single row single column
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery(), or if the database is not connected.
*/
public Object dmiSelectOneObject ( String sql ) throws SQLException {
	ResultSet rs = dmiSelect ( sql );
	rs.next();
	Object o = rs.getObject(1);
	rs.close();
	return o;
}

/**
Runs an SQL statement that contains a <b><code>INSERT</b></code> or <b><code> UPDATE</b></code>.<p>
The code in dmiWrite is surrounded by a check of the private boolean member
variable __editable (set isEditable() and setEditable(boolean).
If __editable is set to true, none of the code is executed and an exception is thrown.
@param sql an SQL command that contains the insert or update command to be run
@return an integer of the row count from the insert or update
@throws SQLException thrown if there are problems with a Connection.createStatement or Statement.executeUpdate().
Also thrown if the database is in read-only mode, or if the database is not connected.
*/
public int dmiWrite(String sql) throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiWrite.");
	}

	if (!this.__editable) {
		throw new SQLException ("Database is in read-only mode, cannot make call to DMI.dmiWrite.");
	}

	if (this.__dumpSQLOnExecution) {
		Message.printStatus(2, "DMI.dmiWrite", sql);
	}

	String routine = "DMI.dmiWrite";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}

	Statement s = this.__connection.createStatement();
	if (this.__capitalize) {
		sql = sql.toUpperCase();
	}

	int result = 0;
	try {
		result = s.executeUpdate(sql);
	}
	catch (SQLException ex) {
		if (this.__dumpSQLOnError) {
			Message.printStatus(2, "DMI.dmiWrite", sql);
		}
		throw ex;
	}

	if (this.__inTransaction) {
		this.__statementsVector.add(s);
	}
	else {
		s.close();
	}

	return result;
}

/**
Executes an insert or update statement from a DMIWriteStatement object.
The SQL string stored in the DMIWriteStatement object is generated and the
resulting string passed into a dmiWrite(String) function that does the work.<p>
The code in dmiWrite is surrounded by a check of the private boolean member
variable __editable (set isEditable() and setEditable(boolean).
If __editable is set to true, none of the code is executed and an exception is thrown.
@param s a DMIWriteStatement object to be executed
@param writeFlag not used with stored procedures; for SQL can be INSERT_UPDATE, UPDATE_INSERT, UPDATE,
INSERT, DELETE_INSERT to indicate order of operations (can impact performance).
@return an integer of the row count from the insert or update
@throws SQLException thrown if there are problems with a
Connection.createStatement or Statement.executeQuery().
Also thrown if the database is in read-only mode, or if the database is not connected
@throws Exception thrown if a DELETE_INSERT statement is run, as this statement type is not supported yet
*/
public int dmiWrite(DMIWriteStatement s, int writeFlag)
throws SQLException, Exception {
	// Enable the following to troubleshoot but normally should be false.
	//__dumpSQLOnExecution = true;
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.dmiWrite().");
	}
	if (!this.__editable) {
		throw new SQLException("Database is in read-only mode.");
	}

	if (s.isStoredProcedure()) {
		s.executeStoredProcedure();
		return s.getIntReturnValue();
	}

	// Set the DMIWriteStatement as the last statement executed.
	setLastStatement(s);

	Statement stmt = this.__connection.createStatement();

	int rowCount = -1; // Number of rows inserted or updated.
	switch (writeFlag) {
		case INSERT_UPDATE:
		// first try to insert the statement in the table.

		////////////////////////////////////////////////////////////////
		// NOTE FOR DEVELOPERS:
		////////////////////////////////////////////////////////////////
		// This database write method works by first trying to insert a record,
		// and if that record already existed in the database, an update is performed.
		//
		// The tricky part is that each database sends different error
		// codes to tell when a duplicate record is found on an insert.
		// To add a new database type to this section, a duplicate record
		// insert must attempted on the new database type and the SQLState
		// and ErrorCode from the resulting SQLException must be recorded
		// and then used below (where, for instance, ODBC has S1000 and 0)
		///////////////////////////////////////////////////////////////
		try {
			if (this.__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite", "Trying to execute INSERT: " + s.toInsertString());
			}
			rowCount = stmt.executeUpdate(s.toInsertString());
			if (this.__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite", "Inserted " + rowCount + " rows.");
			}
			// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
			// Since a delete statement causes a database change (and if the code has gotten this far
			// the delete was successful and didn't throw an exception), the database can now be
			// considered changed and the __dirty flag should be set.
			testAndSetDirty();
		}
		catch (SQLException e) {
			if (this._database_engine == DMIDatabaseType.ACCESS ) {
				if (e.getSQLState() == "S1000" && e.getErrorCode() == 0) {
				    // The Insert failed because a record with the existing key data already exists.
					// That record will be updated, instead.
					try {
						if (this.__dumpSQLOnExecution) {
							Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
						}
						rowCount = stmt.executeUpdate(s.toUpdateString());
						if (this.__dumpSQLOnExecution) {
							Message.printStatus(2, "DMI.dmiWrite", "Inserted " + rowCount + " rows.");
						}
					}
					catch (Exception ex) {
						if (this.__dumpSQLOnError) {
							Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
						}
						throw ex;
					}

					// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
					// Since a delete statement causes a database change
					// (and if the code has gotten this far the delete was successful and didn't throw an exception),
					// the database can now be considered changed and the __dirty flag should be set.
					testAndSetDirty();
				}
				else {
					stmt.close();
					throw e;
				}
			}
			// TODO SAM 2009-05-14 Evaluate whether this code is needed/fragile/etc.
			else if ( (this._database_engine == DMIDatabaseType.SQLSERVER) ){
				if (e.getSQLState() == "23000" && e.getErrorCode() == 2627) {
				    // The Insert failed because a record with the existing key data already exists.
					// That record will be updated, instead.
					try {
						if (this.__dumpSQLOnExecution) {
							Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
						}
						rowCount = stmt.executeUpdate(s.toUpdateString());
						if (this.__dumpSQLOnExecution) {
							Message.printStatus(2, "DMI.dmiWrite", "Inserted " + rowCount + " rows.");
						}
					}
					catch (Exception ex) {
						if (this.__dumpSQLOnError) {
							Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
						}
						throw ex;
					}

					// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
					// Since a delete statement causes a database change (and if
					// the code has gotten this far the delete was successful and didn't throw an exception),
					// the database can now be considered changed and the __dirty flag should be set.
					testAndSetDirty();
				}
				else {
					stmt.close();
					throw(e);
				}
			}
			else {
				throw new Exception ("INSERT_UPDATE may have encountered an "
				+ "existing record, but since there is no information "
				+ "on the INSERT error codes for the database type "
				+ "with which you are working (" + this.__database_engine_String + ") "
			 	+ "there is no certainty about this.  The DMI class needs to be enhanced to handle."
				+ " SQLState=" + e.getSQLState() + " SQLError=" + e.getErrorCode()
				+ " SQLErrorMessage=" + e.getMessage());
			}
		}
		break;
		case UPDATE_INSERT:
			try {
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString(true));
				}
				rowCount = stmt.executeUpdate(s.toUpdateString(true));
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", "Updated " + rowCount + " rows.");
				}
			}
			catch (Exception e) {
				if (this.__dumpSQLOnError) {
					Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
				}
				throw e;
			}

			if (rowCount == 0) {
				// The update failed, so try an insert.
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", s.toInsertString());
				}
				try {
					rowCount = stmt.executeUpdate(s.toInsertString());
				}
				catch (Exception e) {
					if (this.__dumpSQLOnError) {
						Message.printStatus(2, "DMI.dmiWrite", s.toInsertString());
					}
					throw e;
				}
			}
			// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
			// Since a delete statement causes a database change (and if the code has gotten this far
			// the delete was successful and didn't throw an exception),
			// the database can now be considered changed and the __dirty flag should be set.
			testAndSetDirty();
			break;
		case UPDATE:
			try {
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
				}
				rowCount = stmt.executeUpdate(s.toUpdateString());
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", "Updated " + rowCount + " rows.");
				}
			}
			catch (Exception e) {
				if (this.__dumpSQLOnError) {
					Message.printStatus(2, "DMI.dmiWrite", s.toUpdateString());
				}
				throw e;
			}

			// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
			// Since a delete statement causes a database change (and if the code has gotten this far
			// the delete was successful and didn't throw an exception),
			// the database can now be considered changed and the __dirty flag should be set.
			testAndSetDirty();
			break;
		case INSERT:
			try {
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", s.toInsertString());
				}
				rowCount = stmt.executeUpdate(s.toInsertString());
				if (this.__dumpSQLOnExecution) {
					Message.printStatus(2, "DMI.dmiWrite", "Inserted " + rowCount + " rows.");
				}
			}
			catch (Exception e) {
				if (this.__dumpSQLOnError) {
					Message.printStatus(2, "DMI.dmiWrite", s.toInsertString());
				}
				throw e;
			}
			// Used for knowing when to do a startTransaction(ROLLBACK) versus a startTransaction(COMMIT).
			// Since a delete statement causes a database change (and if the code has gotten this far
			// the delete was successful and didn't throw an exception),
			// the database can now be considered changed and the __dirty flag should be set.
			testAndSetDirty();
			break;
		case DELETE_INSERT:
			Message.printWarning(25, "DMI.dmiWrite", "DELETE_INSERT not implemented yet");
			throw new Exception ("DELETE_INSERT not implemented");
		default:
			throw new Exception ("Unspecified WRITE type in DMI.dmiWrite:" + writeFlag);
	}

	if (this.__inTransaction) {
		this.__statementsVector.add(stmt);
	}
	else {
		stmt.close();
	}

	// Return the statement return status, which is typically the number of rows processed.
	return rowCount;
}

/**
Applies escape sequences to a string based on the kind of database being used, as follows:
<li>
<ol> SQL Server engine - translate all instances of ' (apostrophe) to '' (two apostrophes).</li>
</li>
This method is intended to apply to simple strings (e.g., query parameters) and does not deal with
escaping full SQL strings (e.g., to add [] around parameter names that may be reserved words).
@param str the string to check for characters that need escaped.
@return a string with the appropriate escape sequences inserted.
*/
public String escape(String str) {
	String workStr = new String(str);
	if ( this._database_engine == DMIDatabaseType.SQLSERVER ) {
		if (workStr.indexOf('\'') > -1) {
			workStr = StringUtil.replaceString(str, "'", "''");
		}
	}

	// Any other escaping can be done here, too.

	return workStr;
}

// TODO smalers 2021-09-15 might make sense to move this elsewhere, but put here for now.
// TODO smalers 2021-09-15 need to make more generic but not sure what parts are modular yet.
// TODO smalers 2021-09-15 currently does a full replacement of the property but need to add substring replace.
/**
 * Expand a property such as ${pgpass:password} to its expanded value.
 * The properties that are supported are documented in datastore documentation.
 * Any property that is unrecognized is expanded
 * Currently this is only enabled for PostgreSQL password.
 * @param props datastore properties, used to retrieve the "DatabaseEngine" and other properties
 * @param propName name of property that is being expanded
 * @param propValue value of property that is being expanded, can include with ${Property}.
 * This should be passed in as the current value corresponding to 'propName',
 * such as from the configuration file or expanded by another method.
 * If no expansion occurs, the value is returned without modification.
 * @return the expanded property, or the original property value if expansion is not required (return null if propValue is null)
 */
public static String expandDatastoreConfigurationProperty ( PropList props, String propName, String propValue ) {
	String routine = DMI.class.getSimpleName() + ".expandDatastoreConfigurationProperty";
	if ( propValue == null ) {
		return null;
	}
	// Get properties that are used below:
	// - databaseType is an enumeration based on the 'DatabaseEngine' and cannot be assumed,
	//   so the specific database must be specified
	String databaseEngine = props.getValue("DatabaseEngine");
	DMIDatabaseType databaseType = DMIDatabaseType.valueOfIgnoreCase(databaseEngine);
	if ( databaseType == null ) {
		Message.printWarning ( 3, routine, "Unable to determine database type from 'DatabaseEngine' \"" + databaseEngine + "\".");
		return propValue;
	}
	String databaseServer = props.getValue("DatabaseServer");
	String databaseName = props.getValue("DatabaseName");
	String databasePort = props.getValue("DatabasePort");
	if ( (databasePort == null) || databasePort.isEmpty() ) {
		// Database port was not specified in the configuration file so get the default to use below.
		databasePort = "" + DMI.getDefaultPort(databaseType);
	}
	String systemLogin = props.getValue("SystemLogin");
	if ( databaseType == DMIDatabaseType.POSTGRESQL ) {
		Message.printStatus(2, routine, "PostgreSQL detected." );
		if ( propName.equalsIgnoreCase("SystemPassword") ) {
			//Message.printStatus(2, routine, "Getting SystemPassword." );
			// Supported syntax:
			//   ${pgpass:password}
			int pos = StringUtil.indexOfIgnoreCase(propValue,"${pgpass:",0);
			if ( (pos >= 0) && (propValue.length() > (pos + 9)) ) {
				//Message.printStatus(2, routine, "Detected request for pgpass password." );
				if ( propValue.substring(pos + 9).trim().toUpperCase().startsWith("PASSWORD") ) {
					// Matched ${pgpass:password so requesting password:
					// - read the password from user's '.pgpass' file
					// - see: https://www.postgresql.org/docs/14/libpq-pgpass.html
					// - on Windows, the file is %APPDATA%\postgresql\pgpass.conf
					// - on Linux, the file is ~/.pgpass
					//Message.printStatus(2, routine, "Getting pgpass password." );
					String pgpassPath = null;
					if ( IOUtil.isUNIXMachine() ) {
							pgpassPath = System.getProperty("user.home") + "/.pgpass";
					}
					else {
						// Windows.
						String appData = System.getenv("APPDATA");
						if ( appData != null ) {
							pgpassPath = appData + "\\postgresql\\pgpass.conf";
						}
						else {
							Message.printWarning(2, routine, "Windows APPDATA environment variable is not set.  Cannot determine 'pgpass' file location." );
						}
					}
					//Message.printStatus(2, routine, "Path to pgpass file: " + pgpassPath );
					if ( pgpassPath != null ) {
						// Read the .pgpass file into a list of strings.
						String [] parts;
						try {
							File f = new File(pgpassPath);
							if ( !f.exists() ) {
								Message.printWarning(2, routine, "PostgreSQL pgpass password file does not exist: " + pgpassPath );
							}
							else {
								Message.printStatus(2, routine, "Reading PostgreSQL pgpass password file: " + pgpassPath );
								List<String> pgpassLines = IOUtil.fileToStringList(pgpassPath);
								// Loop through the .pgpass lines:
								// - the format is:  hostname:port:database:username:password
								for ( String pgpassLine : pgpassLines ) {
									pgpassLine = pgpassLine.trim();
									// DO NOT print the line in production systems because password is shown.
									//Message.printStatus(2, routine, "Processing line: " + pgpassLine );
									if ( pgpassLine.length() == 0 ) {
										continue;
									}
									else if ( pgpassLine.charAt(0) == '#' ) {
										// Comment.
										continue;
									}
									else {
										parts = pgpassLine.split(":");
										// DO NOT print the line in production systems because password is shown.
										Message.printStatus(2, routine, "Comparing server " + parts[0] + " with " + databaseServer );
										Message.printStatus(2, routine, "Comparing port " + parts[1] + " with " + databasePort );
										Message.printStatus(2, routine, "Comparing databaseName " + parts[2] + " with " + databaseName );
										Message.printStatus(2, routine, "Comparing systemLogin " + parts[3] + " with " + systemLogin );
										if ( parts.length == 5 ) {
											// Match the parts in order to find the password.
											if ( parts[0].equals(databaseServer) && parts[1].equals(databasePort)
												&& parts[2].equals(databaseName) && parts[3].equals(systemLogin) ) {
												// The database and account parts match.  Return the password.
												// DO NOT print the line in production systems because password is shown.
												return parts[4];
											}
										}
										Message.printWarning(2, routine, "No pgpass entry matches requested information.  Did not find the password.");
									}
								}
							}
						}
						catch ( IOException e ) {
							Message.printWarning(3, routine, "Exception reading .pgpass file: " + pgpassPath );
						}
					}
					else {
						Message.printWarning(3, routine, "Could not detemine path to pgpass file.");
					}
				}
			}
		}
    }
	// No additional expansion was found.  Return the original value.
	return propValue;
}

/**
Return the additional database connection properties.
These are used to customize a connection based on the JDBC driver connection string options.
@return the additional database connection properties.
@deprecated use the connection properties in the constructor, as of TSTool 15.2.0
*/
public String getAdditionalConnectionProperties() {
	return this.__additionalConnectionProperties;
}

/**
Returns all the kinds of databases a DMI can connect to.
@return a list of all the kinds of databases a DMI can connect to.
*/
protected static List<String> getAllDatabaseTypes() {
	List<String> types = new ArrayList<>(10);
	types.add("Access");
	types.add("Derby");
	types.add("Excel");
	types.add("H2");
	types.add("HSQLDB");
	types.add("Informix");
	types.add("MySQL");
	types.add("Oracle");
	types.add("PostgreSQL");
	types.add("SQLServer");
	return types;
}

/**
Returns the current value of the autoCommit setting.
@return the value of the autoCommit setting
*/
public boolean getAutoCommit() {
	return this.__autoCommit;
}

/**
Returns whether the SQL is being capitalized or not.
@return whether the SQL is being capitalized or not.
*/
public boolean getCapitalize() {
	return this.__capitalize;
}

/**
Returns the connection being used to interact with the database.
@return the connection being used to interact with the database
*/
public Connection getConnection() {
	return this.__connection;
}

/**
 * Get a connection property from this.connectionPropertiesMap as a string.
 * @param propertyName the name of the property
 * @return the property value corresponding to the property name by casting to a string, or null if not defined
 */
private String getConnectionPropertyAsString ( String propertyName ) {
	Object o = this.connectionPropertiesMap.get ( propertyName );
	if ( o == null ) {
		return null;
	}
	else {
		return "" + o;
	}
}

/**
Returns the meta data associated with the currently-opened connection,
or null if there is no open connection.
@return the meta data associated with the currently-opened connection,
or null if there is no open connection.
*/
public DatabaseMetaData getDatabaseMetaData() {
	if (this.__connected) {
		try {
			return this.__connection.getMetaData();
		}
		catch (SQLException e) {
			return null;
		}
	}
	return null;
}

/**
Return the name of the database.  This is used when the database name on the
server has been set with setDatabaseName() for cases when the connection URL is
completed automatically and a predefined ODBC DSN is not used.
@return the name of the database.
*/
public String getDatabaseName() {
	return this.__database_name;
}

/**
Return a list of Strings containing properties from the DMI object.
DMI classes that extend this class should define this method for use in general database properties displays.
Currently there is no standard envisioned for the database information,
only that it provide the user with useful information about the database connection.
The contents returned here depend on whether the connection is secure
(if not, login and password information are not printed).
@return a list containing database properties as Strings.
@param level A numerical value that can be used to control the amount of output, to be defined by specific DMI instances.
A general guideline is to use 3 for full output, including database version, history of changes,
server information (e.g., for use in a properties dialog);
2 for a concise output including server name (e.g., for use in the header of an output file);
1 for very concise output (e.g., the database name and version, for use in a product footer).
This arguments defaults to 3 if this base class method is called.
*/
public List<String> getDatabaseProperties ( int level ) {
	List<String> properties = new ArrayList<>();
	if ( this.isJdbc ) {
		// Need the server name, database name, etc.
		properties.add( "Database engine: " + this.__database_engine_String );
		properties.add( "Database server: " + this.__database_server);
		properties.add( "Database name: " + this.__database_name);
		properties.add( "Database port: " + this.__port);
	}
	else {
		properties.add( "ODBC DSN: " + this.__odbc_name );
	}
	// Always have this.
	properties.add( "Database version: " + this.__database_version );

	// Secure information.

	if ( __secure ) {
		properties.add( "System login: " + this.__system_login);
		properties.add( "System password: " + this.__system_password);
		properties.add( "User login: " + this.__user_login);
		properties.add( "User password: " + this.__user_password);
	}

	return properties;
}

/**
Return the database engine type.  One of:<br>
<ul>
<li>Access</li>
<li>H2</li>
<li>HSQLDB</li>
<li>Informix</li>
<li>MySQL</li>
<li>Oracle</li>
<li>PostgreSQL</li>
<li>SQLServer</li>
</ul>
@return the database engine type as a string.
*/
public String getDatabaseEngine() {
	return this.__database_engine_String;
}

/**
Return the database engine type.
@return the database engine type.
*/
public DMIDatabaseType getDatabaseEngineType() {
	return this._database_engine;
}

/**
Return the database server name (IP address or DNS-resolvable machine name).
The server name is only required if setDataseName() is also called.
Otherwise, the connection is assumed to use a predefined ODBC DSN,
which can be retrieved using getODBCName().
@return the database server name (IP address or DNS-resolvable machine name).
*/
public String getDatabaseServer() {
	return this.__database_server;
}

/**
Return the database version number.
@return the database version number.
*/
public long getDatabaseVersion () {
	return this.__database_version;
}

/**
 * Get the default port to use for a database engine.
 * @param databaseType the database type of interest
 * @return the default database port or -1 if unknown
 */
public static int getDefaultPort( DMIDatabaseType databaseType ) {
	if ( databaseType == null ) {
		return -1;
	}
	switch ( databaseType ) {
		case ACCESS:
			// Port not used since a file database.
			return -1;
		case H2:
			// Port not used.
			return -1;
		case HSQLDB:
			// Port not used.
			return -1;
		case INFORMIX:
			return 1526;
		case MYSQL:
			return 3306;
		case ORACLE:
			return 1521;
		case POSTGRESQL:
			return 5432;
		case SQLITE:
			// Not used since a file database.
			return -1;
		case SQLSERVER:
			return 1433;
		default:
			return -1;
	}
}

/**
Return the status of the dirty flag.
@return the status of the dirty flag
*/
public boolean getDirty() {
	return this.__dirty;
}

/**
Returns the ID string that identifies the connection.
@return the ID string that identifies the connection.
*/
public String getID() {
	return this.__id;
}

/**
Returns the name of the connection.
@return the name of the connection.
*/
public String getInputName() {
	return this.__inputName;
}

/**
Indicate whether this is a JDBC connection or not (ODBC).
@return true if the database connection uses JDBC
(meaning that the connection is created with a database server and database name.
Return false if the connection is made by specifying an ODBC DSN.
*/
public boolean getIsJdbc () {
	return this.isJdbc;
}

/**
Returns the last query string that was executed.
@return the last query string that was executed
*/
public String getLastQueryString() {
	if ( this.__lastQuery == null ) {
		return "";
	}
	else {
		return this.__lastQuery.toString();
	}
}

/**
Returns the last SQL string that was executed.
Returns "" if the DMI connection is using stored procedures.
@return the last SQL string that was executed.
*/
public String getLastSQLString() {
	switch(this.__lastSQLType) {
		case NONE:
			return "";
		case WRITE:
			DMIWriteStatement w = (DMIWriteStatement)this.__lastSQL;
			return w.toUpdateString() + " / " + w.toInsertString();
		case SELECT:
		case COUNT:
			DMISelectStatement s = (DMISelectStatement)this.__lastSQL;
			return s.toString();
		case DELETE:
			DMIDeleteStatement d = (DMIDeleteStatement)this.__lastSQL;
			return d.toString();
		default:
			return "";
	}
}

/**
Returns the type of the last SQL string that was executed.
@return the type of the last SQL string that was executed
*/
public int getLastSQLType() {
	return this.__lastSQLType;
}

/**
Returns the field left escape string.
@return the field left escape string
*/
public String getFieldLeftEscape() {
	return this.__fieldLeftEscape;
}

/**
Returns the field right escape string.
@return the field right escape string
*/
public String getFieldRightEscape() {
    return this.__fieldRightEscape;
}

/**
Return the ODBC Data Source Name (DSN).
This is used with a predefined ODBC DSN on the machine.
@return the ODBC DSN for the database connection.
*/
public String getODBCName() {
	return this.__odbc_name;
}

/**
Returns the port of the database connection.
@return the port of the database connection.
*/
public int getPort() {
	return this.__port;
}

/**
Returns the setting of the secure variable.
@return the setting of the secure variable
*/
public boolean getSecure() {
	return this.__secure;
}

/**
Returns the database types a DMI can connect to that are done via direct server connection
(no predefined ODBC connection is needed).
@return a list of the database types a DMI can connect to that are done via direct server connection.
*/
protected static List<String> getServerDatabaseTypes() {
	List<String> types = new ArrayList<>();
	// Do not include Access since this requires that an ODBC connection be defined.
	types.add("H2");
	types.add("HSQLDB");
	types.add("Informix");
	types.add("MySQL");
	types.add("Oracle");
	types.add("PostgreSQL");
	types.add("SQLServer");
	return types;
}

/**
Returns the statement end string, for example semicolon needed by some databases.
@return the statement end string.
*/
public String getStatementEnd() {
    return this.__statementEnd;
}

/**
Returns the string delimiter.
@return the string delimiter
*/
public String getStringIdDelim() {
	return this.__stringDelim;
}

/**
Return the system login name for the database connection.
@return the login name for the database connection.
*/
public String getSystemLogin() {
	return this.__system_login;
}

/**
Return the password for the database connection.
@return the password for the database connection.
*/
public String getSystemPassword() {
	return this.__system_password;
}

/**
Return the user login name.
@return the user login name.
*/
public String getUserLogin() {
	return __user_login;
}

/**
Return the user password.
@return the user password.
*/
public String getUserPassword() {
	return this.__user_password;
}

/**
Indicate whether the database version is at least the indicated version.
@return true if the database version is at least that indicated.
*/
public boolean isDatabaseVersionAtLeast ( long version ) {
	// The database versions are just numbers so can check arithmetically.
	if ( this.__database_version >= version ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Indicate whether or not the database is set to read-only mode.
If isEditable() returns true, then no calls to dmiWrite or dmiDelete will be executed.
@return true if the database is in read-only mode, false if not
*/
public boolean isEditable() {
	return this.__editable;
}

/**
Indicate whether the DMI is connected to a database.
@return true if the database connection has been established.
*/
public boolean isOpen() {
	return this.__connected;
}

/**
Opens the connection to the DMI with information that was previously set,
and sets printStatus to the given value while open() (the other version of the method,
not this one) is being called, in order to print more debugging information.
Once this method is done, __printStatus will be set to false.<p>
__printStatus is primarily used in printStatusOrDebug(),
used internally for debugging DMI connection open()s.
@param printStatus whether to print information to status while in open().
@throws SQLException if the DMI is already connected.
@throws Exception if some other error happens while connecting to the database.
*/
public void open(boolean printStatus)
throws Exception, SQLException {
	this.__printStatus = printStatus;
	try {
		open();
	}
	catch (SQLException se) {
		this.__printStatus = false;
		throw se;
	}
	catch (Exception e) {
		this.__printStatus = false;
		throw e;
	}
	this.__printStatus = false;
}

/**
Open a connection to the database with information that was previously specified.
@throws SQLException if the DMI is already connected.
@throws Exception if some other error happens while connecting to the database.
*/
public void open ()
throws Exception, SQLException {
	String routine = CLASS + ".open";

	printStatusOrDebug(10, routine, "DMI.open() -----------------------");
	printStatusOrDebug(10, routine, "Checking for existing connection ...");

	if (this.__connected) {
		printStatusOrDebug(10, routine, "Already connected!  Throwing exception.");
		throw new SQLException ("Must close the first DMI connection before opening a new one.");
	}

	printStatusOrDebug(10, routine, "         ... no connection found.");

	open (this.__system_login, this.__system_password );

	// Determine the database version (the derived class method will be called if defined).
	printStatusOrDebug(10, routine, "Determining database version ... (abstract method, defined in class derived from DMI)");
	determineDatabaseVersion();

	printStatusOrDebug(10, routine, "Reading global data ... (abstract method, defined in class derived from DMI)");
	readGlobalData();
	this.__lastQuery = null;
	this.__lastSQL = null;
	this.__lastSQLType = NONE;
	printStatusOrDebug(10, routine, "----------------------- DMI.open()");
}

/**
Open a connection to the database with the specified system login and password.
All other connection parameters must have been set previously.
@param systemLogin System login.
@param systemPassword System password.
@throws SQLException thrown by DriverManger.getConnection() or Connection.setAutoCommit()
@throws Exception thrown when attempting to connecting to a database for which no JDBC information is known.
*/
public void open ( String systemLogin, String systemPassword )
throws SQLException, Exception {
	String routine = CLASS + ".open(String, String)";
	printStatusOrDebug(10, routine, "Checking for existing connection ...");
	if (this.__connected) {
		printStatusOrDebug(10, routine, "Already connected!  Throwing exception.");
		throw new SQLException ("Must close the first DMI connection before opening a new one.");
	}

	printStatusOrDebug(10, routine, "         ... no connection found.");

	String connUrl = "";
	int dl = 10;

	if ( (this.__secure) && (Message.isDebugOn || this.__printStatus)) {
		printStatusOrDebug(dl, routine, "SystemLogin: " + systemLogin + "," + "SystemPassword: " + systemPassword + ")");
	}

	// Set up the database-specific connection strings.

	// The propertyMap is required for the StringUtil.expandForProperties utility method:
	// - currently it provides minimal environment data
	// - the expansion below is in addition to any expansion that occurred when reading the datastore configuration file
	HashMap<String,Object> propertyMap = new HashMap<>();
	propertyMap.put("ProcessId", "" + IOUtil.getProcessId());

	// Whether the connection should be opened by passing the login and password:
	// - this is usually the case but some things like Google Wallet may use authentication in the wallet
	boolean useLoginAndPassword = true;
	
	// Connection properties that are not set in the connection URL:
	// - these are Java system properties, not to be confused with this.connectionPropertiesMap
	// - for example, this is used with Google wallet to set the wallet folder location
	// - the list can be empty
	Properties connectionProps = new Properties();
	
	if ( this.isJdbc ) {
		printStatusOrDebug(dl, routine, "Using JDBC connection.");
		// The URL is formed using several pieces of information.
		if ( this._database_engine == DMIDatabaseType.ACCESS ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.ACCESS'");
			// Always require an ODBC DSN (although this may be
			// a problem with some configuration files and software where
			// an MDB file is allowed for the database name).
			// This case is therefore the same as if __jdbc_odbc = false.
			if ( IOUtil.getJavaMajorVersion() <= 7 ) {
				// Use the built-in Microsoft Access driver.
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				connUrl = "jdbc:odbc:" +  this.__database_name;
		    	if ( ( this.__additionalConnectionProperties != null) && ! this.__additionalConnectionProperties.isEmpty() ) {
			    	connUrl = connUrl + StringUtil.expandForProperties( this.__additionalConnectionProperties, propertyMap);
		    	}
				Message.printStatus(2, routine,
					"Java version is <= 7. Opening database connection for Microsoft Access built-in JDBC/ODBC driver and \"" + connUrl + "\"");
			}
			else {
				// Must use a third party Access driver:
				// - use open source UCanAccess: http://ucanaccess.sourceforge.net/site.html#home
				// - example:  jdbc:ucanaccess://C:/db/main.mdb;remap=c:\db\linkee1.mdb|C:\pluto\linkee1.mdb&c:\db\linkee2.mdb|C:\pluto\linkee2.mdb
				Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
				//String dbPath = this.__database_name;
				String dbPath = IOUtil.toPortablePath(this.__database_name);
				if ( !dbPath.startsWith("//") ) {
					// UCanAccess wants // in front of the path.
					dbPath = "//" + dbPath;
				}
				connUrl = "jdbc:ucanaccess:" + dbPath;
		    	if ( ( this.__additionalConnectionProperties != null) && ! this.__additionalConnectionProperties.isEmpty() ) {
			    	connUrl = connUrl + StringUtil.expandForProperties( this.__additionalConnectionProperties, propertyMap);
		    	}
				Message.printStatus(2, routine,
					"Java version is not <= 7.  Opening database connection for Microsoft Access using UCanAccess JDBC driver and \"" + connUrl + "\"");
			}
		}
		else if ( this._database_engine == DMIDatabaseType.DERBY ) {
            printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.DERBY'");
            // If the server name is "memory" then the in-memory URL is used.
            // TODO SAM 2014-04-22 Figure out how to handle better.
            // TODO SAM 2014-04-22 Figure out how to open versus create.
            System.setProperty("derby.system.home", "C:\\derby");
            // Load the database driver class into memory.
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            if (  this.__database_server.equalsIgnoreCase("memory") ) {
                connUrl = "jdbc:derby:memory:db;create=true";
		        if ( ( this.__additionalConnectionProperties != null) && ! this.__additionalConnectionProperties.isEmpty() ) {
			        connUrl = connUrl + StringUtil.expandForProperties( this.__additionalConnectionProperties, propertyMap);
		        }
            }
            else {
                throw new SQLException ( "For Derby currenty only support in-memory databases." );
            }
            Message.printStatus(2, routine, "Opening ODBC connection for Derby JDBC/ODBC and \"" + connUrl + "\"");
        }
		else if (this._database_engine == DMIDatabaseType.INFORMIX ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.INFORMIX'");
			// Use the free driver that comes from Informix.
			// If Informix is ever enabled, also need to add a
			// property to the configuration which is the "online server",
			// like "ol_hydrobase".  For now just dummy in.
			String server = "ol_hydrobase";
            // Load the database driver class into memory.
			Class.forName("com.informix.jdbc.IfxDriver");
			connUrl = "jdbc:informix-sqli://"
				+ this.__database_server + ":"
				+ this.__port + "/" + this.__database_name
				+ ":INFORMIXSERVER=" + server;
		    if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
			// Login and password specified below.
			// +";user=" + login + ";password=" + password;
			Message.printStatus(2, routine, "Opening ODBC connection for Informix using (login not shown): " + connUrl);
		}
		else if ( this._database_engine == DMIDatabaseType.MYSQL ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.MYSQL'");
			// Use the public domain driver that comes with MySQL.
			connUrl = "jdbc:mysql://"
				+ this.__database_server + ":"
				+ this.__port + "/" + this.__database_name;
		    if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
			Message.printStatus(2, routine, "Opening ODBC connection for MySQL using \"" + connUrl + "\"" );
		}
		else if ( this._database_engine == DMIDatabaseType.POSTGRESQL ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.POSTGRESQL'");
			connUrl = "jdbc:postgresql://"
				+ this.__database_server + ":"
				+ this.__port + "/" + __database_name;
		    if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
			Message.printStatus ( 2, routine, "Opening ODBC connection for PostgreSQL using (login not shown): " + connUrl );
		}
		else if ( this._database_engine == DMIDatabaseType.SQLITE ) {
			// Database is a file, not via port connection:
			// -see:  https://www.sqlitetutorial.net/sqlite-java/sqlite-jdbc-driver/
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.SQLITE'");
			if ( this.__database_server.equalsIgnoreCase("memory") ) {
				// Open an in-memory database.
				connUrl = "jdbc:sqlite::memory:";
		    	if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    	connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    	}
			}
			else {
				// Path on windows needs to be forward slashes, therefore convert:
				//   C:\a\b\c   to   C:/a/b/c
				connUrl = "jdbc:sqlite:" + __database_server.replace('\\', '/');
		    	if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    	connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    	}
			}
			Message.printStatus ( 2, routine, "Opening ODBC connection for SQLite using (login not shown): " + connUrl );
		}
		// All the SQL Server connections are now concentrated into one code block since using the SQL Server
		// 2008 JDBC (jdbc4) driver.
		else if ( this._database_engine == DMIDatabaseType.SQLSERVER ) {
            // http://msdn.microsoft.com/en-us/library/ms378428%28SQL.90%29.aspx
            connUrl = "jdbc:sqlserver://" + __database_server;
		    if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
            // If connecting to a named instance, DO NOT USE PORT.
            // It is generally recommended to use the port for speed.
            if (this.__database_server.indexOf('\\') < 0) {
                // Database instance is NOT specified, for example the server name would be something like:
                //     "localhost\CDSS" (and database name would be "HydroBase_CO_YYYYMMDD")
                // Consequently, use the port number.
                connUrl += ":" + this.__port;
            }
            connUrl += ";databaseName=" + this.__database_name;
			Message.printStatus ( 2, routine, "Opening ODBC connection for SQLServer using (login not shown): " + connUrl );
		}
        else if ( this._database_engine == DMIDatabaseType.H2 ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.H2'");
            // Load the database driver class into memory.
			Class.forName( "org.h2.Driver");
            java.io.File f = new java.io.File(__database_server);
			connUrl = "jdbc:h2:file:" + f.getAbsolutePath() + ";IFEXISTS=TRUE";
		    if ( ( this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
			Message.printStatus(2, routine, "Opening JDBC connection for H2 using (login not shown): " + connUrl );
		}
        else if ( this._database_engine == DMIDatabaseType.HSQLDB ) {
			printStatusOrDebug(dl, routine, "Database engine is type 'DMIDatabaseType.HSQLDB'");
            // Load the database driver class into memory.
			Class.forName( "org.h2.Driver");
            java.io.File f = new java.io.File(this.__database_server);
			connUrl = "jdbc:hsqldb:file:" + f.getAbsolutePath();
		    if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
			    connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		    }
			Message.printStatus(2, routine, "Opening JDBC connection for HSQLDB using (login not shown): " + connUrl );
		}
        else if ( this._database_engine == DMIDatabaseType.ORACLE ) {
        	// Oracle database:
        	// - use mixed case for specific configuration properties

			printStatusOrDebug(dl, routine, "Database engine is type 'ORACLE'");
            // Load the database driver class into memory.
			Class.forName( "oracle.jdbc.driver.OracleDriver");

			// Create a builder string:
			// - convert to the 'connUrl' string at the end before opening the connection
			StringBuilder connUrlBuilder = new StringBuilder ();

            // Delimiters for connection string, similar to a URL, used with thin client.
            String delim1 = "?";
            String delim2 = "&";
            int paramCount = 0;
			
			// Determine whether the thin driver is being used.
			boolean doThinDriver = true;
			String OracleDriverType = getConnectionPropertyAsString("OracleDriverType");
			if ( (OracleDriverType != null) && !OracleDriverType.equalsIgnoreCase("Thin") ) {
				// Request is for something other than thin driver.
				Message.printWarning(3, routine, "Currntly, only the Oracle thin driver is supported.  Will try to connect using the thin driver." );
			}

			// Determine whether the connection URL has been fully specified.
			boolean doBuildUrl = true;
			String ConnectionUrl = getConnectionPropertyAsString("ConnectionUrl");
			if ( (ConnectionUrl != null) && !ConnectionUrl.isEmpty() ) {
				// The full connection URL is specified so just use it without forming from the parts.
				doBuildUrl = false; 
				connUrlBuilder.append ( ConnectionUrl );
			}
			
			// Do a basic check to see if Google Wallet is being used:
			// - if either OracleNetworkFolder or OracleWalletFolder is specified, then wallet is being used
			boolean doOracleWallet = false;
			String OracleNetworkFolder = getConnectionPropertyAsString("OracleNetworkFolder");
			String OracleWalletFolder = getConnectionPropertyAsString("OracleWalletFolder");
			if ( ((OracleNetworkFolder != null) && !OracleNetworkFolder.isEmpty())
				|| ((OracleWalletFolder!= null) && !OracleWalletFolder.isEmpty()) ) {
				// Oracle wallet is being used.
				doOracleWallet = true;
			}
			
			// Set the initial part of the connection string.
			
			if ( doBuildUrl && doThinDriver ) {
				connUrlBuilder.append("jdbc:oracle:thin:");
			}
			if ( doBuildUrl ) {
				if ( doOracleWallet ) {
					// Using Google Wallet:
					// - the database name is an alias that should be found in the 'tnsnames.ora' file
					//connUrlBuilder.append ( "@" + this.__database_name );
					connUrlBuilder.append ( "@" + this.__database_name );
				}
				else {
					// Old and simpler configuration.
					connUrlBuilder.append ( "@" + this.__database_server + ":" + this.__port + ":" + this.__database_name );
				}
			}

            if ( doOracleWallet ) {
            	// Using an Oracle Wallet.
            	//
            	// Example files for for HDB:
            	//   TLS = Transport Layer Security
            	//   Files in the TNS_NAMES folder:
            	//     sqlnet.ora - text, standard name, contains DIRECORY that must match the actual wallet folder
            	//     tnsnames.ora - text, standard name, maps NTA name in the connection string to the actual database, and Wallet folder
            	//   Files in the Oracle wallet folder:
            	//     cwallet.sso - binary, standard name, auto-login, does not contain a password, contains TLS certificate
            	//     cwallet.sso.lck - standard name, empty file, lock file, why is it here?
            	//     testdb.cer - standard name, CA certificate
            	
            	// First specify the TNS_ADMIN in the connection URL:
            	// - this can only be done with a datastore 'OracleNetworkFolder' property
       			// - some experimentation shows that forward slashes may work better than backslashes
            	if ( doBuildUrl && (OracleNetworkFolder != null) && !OracleNetworkFolder.isEmpty() ) {
            		paramCount = appendConnectionUrlParameter ( connUrlBuilder, delim1, delim2, paramCount, "TNS_ADMIN="
            			+ OracleNetworkFolder.replace("\\", "/") );
            		// Also check whether the folder and files exist for troubleshooting.
            		File f = new File(OracleNetworkFolder);
            		if ( !f.exists() ) {
            			Message.printWarning(3,routine,"Oracle network folder does not exist: \"" + OracleNetworkFolder + "\"");
            		}
            		f = new File(OracleNetworkFolder + File.separator + "sqlnet.ora");
            		if ( f.exists() ) {
            			Message.printWarning(3,routine,"Oracle network file sqlnet.ora exists: \"" + f.getAbsolutePath() + "\"");
            		}
            		else {
            			Message.printWarning(3,routine,"Oracle network file sqlnet.ora does not exist: \"" + f.getAbsolutePath() + "\"");
            		}
            		f = new File(OracleNetworkFolder + File.separator + "tnsnames.ora");
            		if ( f.exists() ) {
            			Message.printWarning(3,routine,"Oracle network file tnsnames.ora file exists: \"" + f.getAbsolutePath() + "\"");
            		}
            		else {
            			Message.printWarning(3,routine,"Oracle network file tnsnames.ora does not exist: \"" + f.getAbsolutePath() + "\"");
            		}
            	}
            	
            	// Set the Oracle wallet folder:
            	// - this uses the 'OracleWalletFolder' datastore property
            	// - this is not added to the URL so do regardless of the value of 'doBuildUrl'
            	if ( (OracleWalletFolder != null) && !OracleWalletFolder.isEmpty() ) {
            		// Set the property needed to specify the wallet:
            		// - 'walletFolder' needs to use double backslash if on Windows
            		String walletFolder = OracleWalletFolder;
            		boolean doWalletProperty = false;
            		if ( doWalletProperty ) {
            			// Set the Google Wallet folder using a system property.
            			walletFolder.replace("\\", "\\\\");
            			connectionProps.setProperty("oracle.net.wallet_location",
           			    	"(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY=" + walletFolder + ")))");
            		}
            		else {
            			// This should work for driver version 21+, which is what ojdbc11.jar uses:
            			// - some experimentation shows that forward slashes may work better than backslashes
            			paramCount = appendConnectionUrlParameter ( connUrlBuilder, delim1, delim2, paramCount, "oracle.net.wallet_location="
            				+ OracleWalletFolder.replace("\\", "/") );
            		}

            		// Check for the existence of other files.
            		File f = new File(OracleWalletFolder + File.separator + "cwallet.sso");
            		if ( f.exists() ) {
            			Message.printWarning(3,routine,"Oracle Wallet file cwallet.sso exists: \"" + f.getAbsolutePath() + "\"");
            		}
            		else {
            			Message.printWarning(3,routine,"Oracle Wallet file cwallet.sso does not exist: \"" + f.getAbsolutePath() + "\"");
            		}
            		// There may also be a *.cer file, but the name matches the "CN" in the "tsnames.ora" file for a specific alias:
            		// - for now don't try to parse
            	}
            }

            // Add additional connection properties using the current approach:
            // - the delimiter at the start of the string must have been included
            if ( doBuildUrl ) {
            	String connectionUrlAppend = getConnectionPropertyAsString("ConnectionUrlAppend");
       			paramCount = appendConnectionUrlParameter ( connUrlBuilder, delim1, delim2, paramCount, connectionUrlAppend );

       			// Add additional connection properties using the legacy approach:
       			// - the delimiter at the start of the string must hvae been included

       			if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
		    	 	String additions = this.__additionalConnectionProperties;
		    	 	paramCount = appendConnectionUrlParameter ( connUrlBuilder, delim1, delim2, paramCount,
		    		StringUtil.expandForProperties(additions, propertyMap) );
		     	}
            }
		    
		    // Convert the StringBuilder to a String.
		    connUrl = connUrlBuilder.toString();
			Message.printStatus ( 2, routine, "Opening ODBC connection for Oracle using (login not shown): " + connUrl );
        }
		else {
			// The database engine is not understood.
			printStatusOrDebug(dl, routine, "Unknown database engine, throwing exception.");
			throw new Exception("Don't know what JDBC driver to use for database type " + this.__database_engine_String);
		}
	}
	else {
		// The URL uses a standard JDBC ODBC (with ODBC name) connections,
		// regardless of the database engine (this should not normally be used for Java applications,
		// or need to figure out how to use the ODBC protocol URL knowing only the ODBC DSN for each engine).
		printStatusOrDebug(dl, routine, "Using default Java connection method.");
		if ( IOUtil.getJavaMajorVersion() <= 7 ) {
			// Use the built-in Microsoft Access driver.
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			connUrl = "jdbc:odbc:" + this.__odbc_name;
			if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
				connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
			}
			Message.printStatus(2, routine,
				"Java version is <= 7. Opening ODBC connection for Microsoft Access built-in JDBC/ODBC driver and \"" + connUrl + "\"");
		}
		else {
			// Must use a third party Access driver:
			// - use open source UCanAccess: http://ucanaccess.sourceforge.net/site.html#home
			// - example:  jdbc:ucanaccess://C:/db/main.mdb;remap=c:\db\linkee1.mdb|C:\pluto\linkee1.mdb&c:\db\linkee2.mdb|C:\pluto\linkee2.mdb
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
			connUrl = "jdbc:ucanaccess:" + __database_name;
		   	if ( (this.__additionalConnectionProperties != null) && !this.__additionalConnectionProperties.isEmpty() ) {
		    	connUrl = connUrl + StringUtil.expandForProperties(this.__additionalConnectionProperties, propertyMap);
		   	}
			Message.printStatus(2, routine,
				"Java version is not <= 7.  Opening ODBC connection for Microsoft Access using UCanAccess driver and \"" + connUrl + "\"");
		}
	}
    if ( this.__secure ) {
		printStatusOrDebug(dl, routine, "Calling getConnection("
		+ connUrl + ", " + systemLogin + ", " + systemPassword + ") via the Java DriverManager.");
	}
	else {
	    printStatusOrDebug(dl, routine, "Calling getConnection("
		+ connUrl + ", " + systemLogin + ", " + "(password-not-shown) via the Java DriverManager.");
	}
    // Get the login timeout and reset to requested if specified.
    int loginTimeout = DriverManager.getLoginTimeout();
    if ( this.__loginTimeout >= 0 ) {
        DriverManager.setLoginTimeout(this.__loginTimeout);
    }
    if ( useLoginAndPassword ) {
    	// Login and password are used:
    	// - set in the connection properties
    	connectionProps.setProperty("user", systemLogin );
    	connectionProps.setProperty("password", systemPassword );
    }
    
    // Open the connection:
    // - properties may contain the user and password from above
    // - properties may also contain other properties that cannot be passed in the connection URL
   	this.__connection = DriverManager.getConnection ( connUrl, connectionProps );

    if ( this.__loginTimeout >= 0 ) {
        // Now set back to the original timeout.
        DriverManager.setLoginTimeout(loginTimeout);
    }

	/* TODO SAM 2013-10-07 This seems to be old so commenting out.
    if (_database_engine == DMIDatabaseType.ORACLE && __database_name != null ) {
        __connection.createStatement().execute("alter session set current_schema = " + __database_name );
    }
    */
    printStatusOrDebug(dl, routine, "Setting autoCommit to: " + this.__autoCommit);
	this.__connection.setAutoCommit(this.__autoCommit);

	printStatusOrDebug(dl, routine, "Connected!");
	this.__connected = true;
}

/**
A helper method called by the open() methods that determines whether output needs to go to debug as normal,
or should be forced to status level 2.
See open(boolean), which sets __printStatus. If __printStatus is true, debug will go to status level 2.
@param dl the level at which the message should be printed (for debug messages).
@param routine the routine that is printing the message.
@param message the message to be printed.
*/
private void printStatusOrDebug(int dl, String routine, String message) {
	if (this.__printStatus) {
		Message.printStatus(2, routine, message);
	}
	else {
		Message.printDebug(dl, routine, message);
	}
}

/**
Read global data from the database, that can be used throughout the DMI session.
This is called from the open() method.
*/
public abstract void readGlobalData () throws SQLException, Exception;

/**
Attempts to perform a rollback to cancel database changes since the start of the last transaction.
@throws SQLException thrown by Connection.rollback() or Connection.setAutoCommit,
or when the database is not connected
*/
public void rollback() throws SQLException {
	// TODO (JTS - 2006-05-22) This code has not been tested or used in nearly 4 years.
	// Do not rely on this method without testing it first.
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.rollback()");
	}

	String routine = "DMI.rollback";
	int dl = 25;

	this.__inTransaction = false;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "[method called]");
	}

	__connection.rollback();

	// Since a rollback signifies the end of a transaction, set the autoCommit setting back to being on.
	setAutoCommit(true);
	this.__dirty = false;
	closeStatements();
}

/**
Set additional connection URL properties to be appended to the connection string.
@param additionalConnectionProperties a string of form ";prop1=value1;prop2=value2",
where the semicolon represents a delimiter.
Use the appropriate delimiter character for the database technology being used.
@deprecated set the connection properties in the constructor, as of TSTool 15.2.0.
*/
public void setAdditionalConnectionProperties ( String additionalConnectionProperties ) {
	this.__additionalConnectionProperties = additionalConnectionProperties;
}

/**
Sets the autoCommit setting for the database.
@param autoCommitSetting the autoCommit setting.
@throws SQLException thrown by Connection.setAutoCommit(), or if the database is not connected
*/
public void setAutoCommit(boolean autoCommitSetting) throws SQLException {
	// TODO (JTS - 2006-05-22) This code has not been tested or used in nearly 4 years.
	// Do not rely on this method without testing it first.
	// There's the likelihood that the setting of autoCommit will have to be handled differently for certain database types.
	// This isn't the case right now.
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.setAutoCommit()");
	}

	String routine = "DMI.setAutoCommit";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "autoCommit: " + autoCommitSetting);
	}

	this.__autoCommit = autoCommitSetting;

	if (this.__database_engine_String.equalsIgnoreCase("SOMETHING") ) {
		// TODO handle this differently -- i.e., does MS Access blow up when trying to setAutoCommit?
	}
	else {
		this.__connection.setAutoCommit(autoCommitSetting);
	}
}

/**
Sets whether SQL should be capitalized before being passed to the database.
@param capitalize whether SQL should be capitalized.
*/
public void setCapitalize(boolean capitalize) {
	this.__capitalize = capitalize;
}

/**
Sets the connection used to interact with the database.
In this way, the connection established with one DMI can be passed to a different one so
that the second doesn't have to go through the connection process again.
@param c the connection to use for communicating with the database
*/
public void setConnection(Connection c) {
	this.__connection = c;
	if (c != null) {
		this.__connected = true;
	}
}

/**
Sets the DMI to use the default port for the database engine.
Only works currently for true client-server type databases.
*/
private void setDefaultPort() {
	if ( this._database_engine == DMIDatabaseType.ACCESS ) {
	}
	else if ( this._database_engine == DMIDatabaseType.H2 ) {
	}
	else if ( this._database_engine == DMIDatabaseType.HSQLDB ) {
		// TODO smalers 2022-07-07 should port configuration be supported?
	}
	else if ( this._database_engine == DMIDatabaseType.INFORMIX ) {
		this.__port = 1526;
	}
	else if ( this._database_engine == DMIDatabaseType.MYSQL ) {
		this.__port = 3306;
	}
	else if ( this._database_engine == DMIDatabaseType.ORACLE ) {
		this.__port = 1521;
	}
	else if ( this._database_engine == DMIDatabaseType.POSTGRESQL ) {
		this.__port = 5432;
	}
	else if ( this._database_engine == DMIDatabaseType.SQLITE ) {
		// Not used since a file database.
		this.__port = -1;
	}
	else if ( this._database_engine == DMIDatabaseType.SQLSERVER ) {
		this.__port = 1433;
	}
	else {
		//
	}
}

/**
Set the type of the database engine to which the DMI will connect.  Valid types are:<br>
<ul>
<li> Access</ul>
<li> H2</ul>
<li> HSQLDB</ul>
<li> Informix</ul>
<li> MySQL</ul>
<li> Oracle</ul>
<li> PostgreSQL</ul>
<li> SQLServer</ul>
</ul>
@param database_engine the type of database engine.
@exception Exception if the database engine is not recognized.
*/
public void setDatabaseEngine (String database_engine)
throws Exception {
	String routine = getClass().getSimpleName() + ".setDatabaseEngine";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "database_engine: " + database_engine);
	}
	this.__database_engine_String = database_engine;
	if (this.__database_engine_String.equalsIgnoreCase("Access")) {
		this.__database_server = "Local";
		this._database_engine = DMIDatabaseType.ACCESS;
	}
	else if (this.__database_engine_String.equalsIgnoreCase("Derby")) {
        this.__database_server = "myhost";
        this._database_engine = DMIDatabaseType.DERBY;
    }
	else if (this.__database_engine_String.equalsIgnoreCase("Informix")) {
		this._database_engine = DMIDatabaseType.INFORMIX;
	}
	else if (this.__database_engine_String.equalsIgnoreCase("MySQL")) {
		this._database_engine = DMIDatabaseType.MYSQL;
	}
	else if (this.__database_engine_String.equalsIgnoreCase("Oracle")) {
		this._database_engine = DMIDatabaseType.ORACLE;
	}
	else if (this.__database_engine_String.equalsIgnoreCase("PostgreSQL")) {
		this._database_engine = DMIDatabaseType.POSTGRESQL;
	}
	else if (this.__database_engine_String.equalsIgnoreCase("SQLite")) {
		this._database_engine = DMIDatabaseType.SQLITE;
	}
	else if (StringUtil.startsWithIgnoreCase(this.__database_engine_String,"SQL_Server") ||
		StringUtil.startsWithIgnoreCase(this.__database_engine_String,"SQLServer")) {
		this._database_engine = DMIDatabaseType.SQLSERVER;
	}
    else if (this.__database_engine_String.equalsIgnoreCase("H2")) {
		this._database_engine = DMIDatabaseType.H2;
	}
    else if (this.__database_engine_String.equalsIgnoreCase("HSQLDB")) {
		this._database_engine = DMIDatabaseType.HSQLDB;
	}
	else {
		throw new Exception("Trying to use unknown database engine: " + this.__database_engine_String + " in DMI()");
	}
}

/**
Set the name of the database,
which is used when a connection URL is automatically defined and a predefined ODBC DSN is not used.
If an ODBC DSN is used, then the database connection uses the ODBC Data Source Name (DSN) set with setODBCName().
@param databaseName the name of the database
*/
public void setDatabaseName ( String databaseName ) {
	if ( Message.isDebugOn ) {
		Message.printDebug ( 25, "DMI.setDatabaseName", "database_name: " + databaseName );
	}
	this.__database_name = databaseName;
	this.isJdbc = true;
}

/**
Set the database version.
This should be called from a derived DMI class determineDatabaseVersion() method.
@param version Database version.
The standard for the version number is XXXXXXDDDDDDDD where XXXXXXX is typically a database version
(e.g., RiverTrak 020601) and DDDDDDDD is usually the build date (e.g., 20020625).
*/
public void setDatabaseVersion ( long version ) {
	this.__database_version = version;
}

/**
Sets the value of __editable.
If __editable is true, no calls to dmiWrite or dmiDelete will be executed.
@param editable value to set __editable to
*/
public void setEditable(boolean editable) {
	this.__editable = editable;
}

/**
Set the IP address or DNS-resolvable machine name of the database server.
Note that this can be set to "Local" if a local database is used (e.g., for Access).
@param database_server the IP address or machine name of the database host.
*/
public void setDatabaseServer ( String database_server ) {
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, "DMI.setDatabaseServer", "Database server: \"" + database_server + "\"");
	}
	this.__database_server = database_server;
}

/**
Sets whether to print out the SQL string that caused an exception to be thrown after a database command.
If true, the string will be printed at Message.printStatus(2).
Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL after there is an error.
*/
public void setDumpSQLOnError(boolean dumpSQL) {
	this.__dumpSQLOnError = dumpSQL;
}

/**
Sets whether to print out the SQL string that caused an exception to be thrown after a database command.
If true, the string will be printed at Message.printStatus(2).
Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL after an error occurs.
*/
public void dumpSQLOnError(boolean dumpSQL) {
	setDumpSQLOnError(dumpSQL);
}

/**
Sets whether to print out the SQL string that will be executed prior to any UPDATEs, INSERTS, SELECTs or DELETEs.
If true, the string will be printed at Message.printStatus(2).  Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL prior to running it.
*/
public void setDumpSQLOnExecution(boolean dumpSQL) {
	this.__dumpSQLOnExecution = dumpSQL;
}

/**
Sets whether to print out the SQL string that will be executed prior to any
UPDATEs, INSERTS, SELECTs or DELETEs.  If true, the string will be printed at
Message.printStatus(2).  Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL prior to running it.
*/
public void dumpSQLOnExecution(boolean dumpSQL) {
	setDumpSQLOnExecution(dumpSQL);
}

/**
Sets the ID string that identifies the connection.
@param id the id string that identifies the connection.
*/
public void setID(String id) {
	this.__id = id;
}

/**
Sets the name of the connection.
@param inputName the name of the connection.
*/
public void setInputName(String inputName) {
	this.__inputName = inputName;
}

/**
Saves the last delete statement executed.
@param s a DMIDeleteStatement to be saved
*/
private void setLastStatement(DMIDeleteStatement s) {
	this.__lastSQL = (DMIStatement) s;
	this.__lastSQLType = DELETE;
}

/**
Saves the last select statement executed.
@param s a DMISelectStatement to be saved
*/
private void setLastStatement(DMISelectStatement s) {
	this.__lastSQL = (DMIStatement) s;
	this.__lastSQLType = SELECT;
	this.__lastQuery = s;
}

/**
Saves the last write statement executed.
@param s a DMIWriteStatement to be saved
*/
private void setLastStatement(DMIWriteStatement s) {
	this.__lastSQL = (DMIStatement) s;
	this.__lastSQLType = WRITE;
}

/**
Set the database connection login timeout, which should be set prior to calling open().
A call to DriverManager.setLoginTimeout() will occur prior to getting the connection
and then the timeout will be set back to the previous value.
This ensures that the value is not interpreted globally.
@param loginTimeout connection login timeout in seconds.
*/
public void setLoginTimeout ( int loginTimeout ) {
    this.__loginTimeout = loginTimeout;
}

/**
Set the ODBC DSN for the database, which is used when a predefined ODBC DSN is used to make the connection.
If an ODBC DSN is not used, then the database connection uses the database server
set with setDatabaseServer() and database name set with setDatabaseName().
@param odbc_name the ODBC DSN for the database (must be defined on the machine).
*/
public void setODBCName ( String odbcName ) {
	if ( Message.isDebugOn ) {
		Message.printDebug ( 25, "DMI.setODBCName", "odbc_name: " + odbcName );
	}
	this.__odbc_name = odbcName;
	this.isJdbc = false;
}

/**
Sets the port of the database to connect to.
@param port the value to set the port to
*/
public void setPort(int port) {
	String routine = "DMI.setPort";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Port: " + port);
	}
	this.__port = port;
}

/**
Sets the secure flag.
@param secure the value to set the secure flag to
*/
public void setSecure(boolean secure) {
	String routine = "DMI.setSecure";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Secure: " + secure);
	}
	this.__secure = secure;
}

/**
Set the system login name for the database connection.
@param login the system login name.
*/
public void setSystemLogin(String login) {
	String routine = "DMI.setSystemLogin";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Login: '" + login + "'");
	}
	this.__system_login = login;
}

/**
Set the system password for the database connection.
@param pw the system password for the database connection.
*/
public void setSystemPassword(String pw) {
	String routine = "DMI.setSystemPassword";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "Password: '" + pw + "'");
	}
	this.__system_password = pw;
}

/**
Set the user login name for database use.  This information can be used for record-based permissions.
@param login the user login name.
*/
public void setUserLogin ( String login) {
	this.__user_login = login;
}

/**
Set the user password for database use.  This information can be used for record-based permissions.
@param password the user password.
*/
public void setUserPassword ( String password ) {
	this.__user_password = password;
}

/**
Starts a transaction, first rolling back any changes that may have been
made to the database since the start of the last transaction.
@throws SQLException thrown in startTransaction()
*/
public void startTransaction() throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.startTransaction().");
	}

	this.__dirty = false;
	startTransaction(ROLLBACK);

	// TODO (JTS - 2004-07-20) nothing in here to actually START a transaction.
	// Other transactions are either committed or rolled back, but where is one started?

	this.__inTransaction = true;
}

/**
Starts a transaction, and either rolls back the current database changes or commits them,
depending on the argument to this method.
@param action specifies what to do when the transaction is started.
values that can be passed in are <code>ROLLBACK</code> or <code>COMMIT</code>,
and they tell what to do before the transaction is started.
@throws SQLException thrown in setAutoCommit(), commit() or rollback(),
or when the database is not connected
*/
public void startTransaction(int action) throws SQLException {
	if (!this.__connected) {
		throw new SQLException ("Database not connected.  Cannot make call to DMI.startTransaction().");
	}

	String routine = "DMI.startTransaction";
	int dl = 25;

	// AutoCommit and transaction do not work together.
	// Autocommit must be turned off for transactions to function, and so do that.
	setAutoCommit(false);

	if (action == COMMIT) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "COMMIT");
		}
		commit();
	}
	else if (action == ROLLBACK) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, routine, "ROLLBACK");
		}
		rollback();
	}
}

/**
Sets the dirty flag if autocommit is set to off.  otherwise, does nothing.
*/
private void testAndSetDirty() {
	if (this.__autoCommit == false) {
		this.__dirty = true;
	}
}

/**
Returns a string of useful information about the DMI.
If the connection is secure login information will not be printed.
@return a string of useful information about the DMI
*/
public String toString() {
	StringBuilder dmiDesc = new StringBuilder();

	if (!this.__secure) {
		dmiDesc.append(
			"DMI Information:"
			+ "\n   System Login: " + this.__system_login
			+ "\n   System Password: " + this.__system_password
			+ "\n   User Login: " + this.__user_login
			+ "\n   User Password: " + this.__user_password
			+ "\n   Connecting to database '" + this.__database_name + "' of type '" + this.__database_engine_String + "'"
			+ "\n   at '" + this.__database_server + ":" + this.__port + "'");
	}
	else {
		dmiDesc.append(
			"DMI Information:"
			+ "\n   Connected to database '" + this.__database_name
			+ "' of type '" + this.__database_engine_String + "'" );
	}
	dmiDesc.append( "\n   Current status is: ");
	if (this.__connected == true) {
		dmiDesc.append( "CONNECTED" );
	}
	else {
		dmiDesc.append( "NOT CONNECTED" );
	}
	if ( this.__connection == null ) {
		dmiDesc.append( "\n   JDBC connection is null" );
	}
	else {
		dmiDesc.append( "\n   JDBC connection is not null" );
		try {
			if ( this.__connection.isClosed() ) {
				dmiDesc.append( "\n   JDBC connection is closed." );
			}
			else {
				dmiDesc.append( "\n   JDBC connection is open." );
			}
		}
		catch ( SQLException e ) {
			dmiDesc.append( "\n   Error checking JDBC connection." );
		}
	}
	dmiDesc.append( "\n   autoCommit is currently: ");
	if (this.__autoCommit == true) {
		dmiDesc.append( "ON" );
	}
	else {
		dmiDesc.append( "OFF" );
	}

	if (!this.__secure) {
		dmiDesc.append( "\n   The previously executed SQL statement was:\n   [" + getLastSQLString() + "]" );
	}

	return dmiDesc.toString();
}

}