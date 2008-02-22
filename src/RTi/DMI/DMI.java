// ----------------------------------------------------------------------------
// DMI.java - base class for all database DMI operations
// ----------------------------------------------------------------------------
// To do:
//
// * Can the last* data and methods be boiled down to just "statement" (any
//   need for the query data/method)?
// * Need to figure out cases where a check for "Access_ODBC" is done.
// * should the standard be to use a dmiExecute() method that detects the type
//   of DMIStatement to process?
// * In constructors and setDatabaseEngine() - can some duplicate code be 
//   combined without confusing the logic?  Should setDatabaseEngine() also set
//   the port number?
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// Notes:
// (1) This class is abstract and cannot be directly instantiated
// (2) Derived classes must override the 
//     public abstract String buildSQL(int sqlNumber, Vector values);
//     function
// ----------------------------------------------------------------------------
// History:
// 2002-05-10	J. Thomas Sapienza, RTi	Initial version around this time.
// 2002-05-21	JTS, RTi		Changed private members to begin with 
//					two underscores, representing the new 
//					naming convention in use.  Began 
//					changelog.  Added the MISSING_* 
//					variables and the isMissing* methods.
// 2002-05-22	JTS, RTi		executeQuery and dmiRunQuery return
//                         		ResultSets now, not Vectors.  Changed 
//					STRING_NA to MISSING_STRING and set 
//					it to "".
// 2002-05-23	JTS, RTi		Superficial coding changes (spaces 
//					removed, several constants renumbered, 
//					parentheses removed from returns, 
//					methods renamed)
// 2002-05-28	JTS, RTi		Added formatDate() method.
// 2002-05-29	JTS, RTi		Removed everything related to the 
//					__debug member variable.  Replaced all 
//					System.out.println's with calls to
//					the Message class.
// 2002-05-20	JTS, RTi		'UserName' in variables and methods has 
//					become 'Login'
// 2002-06-25	Steven A. Malers, RTi	Update to handle DMIStatement to
//					store SQL statement information.
//					Add databaseHasTable(),
//					databaseTableHasColumn(),
//					determineDatabaseVersion(), and
//					isDatabaseVersionAtLeast() to support
//					versioning.
//					Remove @author and @version Javadoc
//					tags.
//					Add readGlobalData().
//					Change isConnected() to isOpen() to be
//					consistent with previous DMI work.
// 2002-07-11	J. Thomas Sapienza, RTi	Change things to use the DMIStatement
//					classes, as opposed to operating solely
//					based on SQL strings
// 2002-07-12	JTS, RTi		Changed all the DMI functions that
//					were named executeXXXX to the form
//					"dmiXXXX" to avoid confusion with 
//					the executeXXXX functions contained in
//					java.sql.Statement
//					dmiWrite changed to use an integer flag
//					to determine what its operation should
//					be
//					Removed several functions rendered 
//					unnecessary by the change to dmiWrite,
//					including dmiUpdate, dmiInsert, and
//					a couple overloaded dmiWrite functions
// 2002-07-16	JTS, RTi		Changed all the Message.printDebugs
//					that were still printing the old names
//					of various functions
// 2002-07-17	JTS, RTi		Corrected several bugs that hadn't
//					yet been uncovered in dmiCount, 
//					including one that formed count queries
//					incorrectly when an ORDER BY clause was
//					present in the original SQL, and 
//					another that caused windows memory	
//					reference faults.  Removed 
//					MISSING_INTEGER since the class already
//					has MISSING_INT
//					Added support for SQL Server 2000
//					jdbc connections.  Removed the old
//					(and by now, commented-out) buildSQL
//					code.
//					Massive rework of commenting that had
//					grown badly out-of-date
// 2002-07-18	JTS, RTi		Added code to get a list of tables in
//					the current database.  Database can 
//					now be set in read-only mode (disables
//					any calls to dmiWrite or dmiDelete.
//					Database can
//					generate information about the fields
//					in a table (or tables) and populate
//					a DMITableModel to show the data in 
//					a JTable.  "SQL_Server_2000" changed to
//					"SQLServer2000".  Sql Server 2000 no
//					longer has the port number hard-coded.
// 2002-07-22	JTS, RTi		Change in dmiGetFieldInfo to avoid
//					returning null.  Instead, an empty
//					DMITableModel is returned.  Cleaned 
//					up the printDebug statements and added
//					the use of the dl and routine variables.
// 2002-07-23	JTS, RTi		Method calls that require the database
//					to be connected are first checked to
//					see if the connection is open.  If it
//					isn't, a SQLException is thrown.
// 2002-07-24	JTS, RTi		Added some methods that will need to 
//					be removed later, but which are useful
//					for development for Morgan and me right
//					now (soft, loud, createWeirdFiles)
// 2002-08-06	JTS, RTi		Added the databaseHasStoredProcedure
//					procedures
// 2002-08-13	JTS, RTi		Changed database type "Access" to
//					"Access_ODBC" (to more accurately 
//					represent what it is)
// 2002-08-14	JTS, RTi		Made a few constants static that
//					weren't.  Alphabetized, organized
//					member variables and methods.
//					Recurrent bug when executing a count
//					statement of the general form 
//					SELECT COUNT (*);
//					finally eliminated with lots of string
//					work.  BTW, that's a regular expression.
//					More checks in various methods for
//					__connected. Code cleaned up.
//					Removed some ancient methods that have
//					not been used for the last three months
//					and had been set to deprecated for 
//					almost that long.
// 2002-08-15	JTS, RTi		More Housekeeping.  Removed the "field
//					type constants.  
// 2002-08-21	JTS, RTi		Better handling of Statement and 
//					ResultSet objects now; they get 
//					close()d.  Introduced a finalize()
//					method.
// 2002-08-22	JTS, RTi		Statements aren't closed anymore -- 
//					found out that they lead to erratic
//					and unpredictable exceptions if they
//					finish closing before they're done 
//					populating the result set
// 2002-10-07	JTS, RTi		Added support for SQL Server 7, and 
//					cleaned up some bad string comparisons
//					on the way out.
// 2002-10-14	SAM, RTi		* Change the login and password data and
//					  members to use the notation "System"
//					  for low-level system connections and
//					  "User" for user connections.  The
//					  system information is used for the URL
//					  to make the connection and the user
//					  information can be used for table and
//					  record level restrictions.
//					* Default __secure to false - need to
//					  change to true if the user login is
//					  somehow verified as secure.
//					* Change getDatabaseProperties to return
//					  a Vector of String - not other object
//					  types.
// 2002-12-24	SAM, RTi		* Clarify how code handles different
//					  database connections.  Now the
//					  _auto_odbc boolean is used to
//					  indicate whether the ODBC URL is
//					  formed internally (using
//					  __database_name) or whether the ODBC
//					  name is specified (using __odbc_name).
//					* Replace the
//					  Access_ODBC type with Access database
//					  type coupled with __auto_odbc = false
//					  and __odbc_name.
//					* Change __databaseType to
//					  _database_engine to more closely agree
//					  with C++ and other conventions.
//					  Change the related methods
//					  appropriately.
//					* Change __ip to __database_server to
//					  more closely agree with other
//					  conventions.  Change the related
//					  methods appropriately.
//					* Remove the __blab data member and
//					  related code since it is redundant
//					  with Message debugging and the DMI
//					  classs has tested out in production.
//					* Remove references to TSDate since it
//					  is not used - in the future use
//					  DateTime if necessary.
//					* Move general support code to DMIUtil
//					  to streamline this DMI code.
//					* Use equalsIgnoreCase() for database
//					  engine, etc. - no reason to require
//					  exact match because it leads to errors
//					  in configuration.
//					* Remove checkStringForNull() - the
//					  wasNull() method should be used when
//					  processing result sets, giving better
//					  overall performance.
//					* Remove createWeirdFiles() - apparently
//					  used in early development.
//					* Update the constructors to work either
//					  with straight ODBC or an internally
//					  created JDBC/ODBC URL.
//					* Define protected final int values for
//					  database types to increase performance
//					  (string comparisons for the database
//					  type is slower).  This also allows
//					  more checks for valid database
//					  engines.
// 2003-01-06	SAM, RTi		* JTS moved DMITableModel code out to
//					  other file(s).  Will work on later to
//					  make a general component.
//					* condenseSpaces(), condenseString(),
//					  removeSpaces(), split(),
//					  vectorToStringArray()
//					  have been removed since they supported
//					  an error trap that will be very
//					  infrequent.  Need to include in
//					  StringUtil as time allows.
//					  Remove dmiCountCheck() whose sole
//					  purpose was to check for SELECT
//					  COUNT (*); - we need to guard against
//					  this crash in other ways.
//					* Update getDatabaseProperties() to be
//					  more verbose and stand-alone.
//					* Move sqlError() to
//					  DMIUtil.printSQLException().
//					* Change the connection method from
//					  __auto_odbc to __jdbc_odbc.  If true
//					  then a specific driver is used.  If
//					  false the the general ODBC driver is
//					  used.  The other option for C++
//					  applications is "File".
// 2003-03-05	JTS, RTi		Added the left and right delims
//					that are set for each database type.
// 2003-03-07	JTS, RTI		Added setCapitalized() method so that
//					capitalization of all SQL strings can
//					be handled at the most basic level.
// 2003-03-08	SAM, RTi		Fix bug where when using an ODBC name
//					constructor (not JDBC-ODBC), the ODBC
//					name was not actually getting set in the
//					constructor.
// 2003-04-07	JTS, RTi		Added getDatabaseMetaData().
// 2003-04-22	JTS, RTi		Added getJDBCODBC().
// 2003-06-16	JTS, RTi		Added escape().
// 2003-08-26	JTS, RTi		Added __id and __name fields.
// 2004-04-26	JTS, RTi		Added new open() methods that take a
//					boolean parameter for printing debug
//					information to status level 1 instead.
//					This is useful for applications like
//					RTAssistant where the dmi connection
//					happens when the GUI starts up and 
//					where the user doesn't have a chance
//					to change the Debug level settings
//					prior to the call to open().
// 2004-07-13	JTS, RTi		Added closeResultSet() methods.
// 2004-07-20	JTS, RTi		* Added __statementsVector to hold
//					  the statements created during a 
//					  transaction.
//					* Statements created during delete
//					  and write operations are closed now.
// 2004-11-16	Scott Townsend, RTi	Added PostgreSQL support.
// 2005-03-07	JTS, RTi		Added a new closeResultSet() method
//					that takes a Statement as a parameter.
//					This is used with connections that use
//					stored procedures.
// 2005-04-07	JTS, RTi		Added setInputName() and getInputName().
// 2005-10-12	JTS, RTi		Shared constructor code moved to a 
//					single initialize() method in order to
//					accomodate changes in HydroBase.
// 2005-12-01	JTS, RTi		Previous fix broke Access ODBC 
//					connections.  Fixed.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.DMI;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.Vector;

/**
The DMI class serves as a base class to allow interaction with ODBC/JDBC
compliant database servers.
Derived classes should define specific code to implement a connection with
the database.
In particular, a derived class should:
<ol>
<li>	Call the appropriate constructor in this base class when constructing an
	instance.</li>
<li>	Implement a determineDatabaseVersion() method, which is called from
	the open() method in this base DMI class.  The
	determineDatabaseVersion() method should call the setDatabaseVersion()
	method of this DMI base class.</li>
<li>	Implement a readGlobalData() method to read global data that will be
	kept in memory in the derived DMI class (e.g., for commonly used data
	like units).
<li>	Implement a getDatabaseProperties() method that can be used by an
	application (e.g., to show database properties to a user.</li>
<li>	Use the DMI*Statement objects to create SQL statements.  Execute the
	statements using dmi*() methods.</li>
<li>	Implement constructors that follow the guidelines described below.
</ol>
To create a connection to a database, a DMI instance should be created using one
of the two constructors.  The constructors are provided for the two main ways
to connect to a database:
<ol>
<li>	Using an ODBC DSN that is defined on the machine.  In this case only
	the ODBC name is required from the derived class.</li>
<li>	Using a database server name and database name on the server.
	<b>This method is preferred with Java because it allows individual
	manufacture JDBC drivers to be used.</b>  In this case the constructor
	in the derived class should pass the system login and password used to
	make the connection, as well as other information that may be required
	for the specific database.</li>
</ol>
The open() method in this base class will make a database connection using only
one of the above methods, depending on whether the ODBC name or the database
name and server have been defined.

A new DMI should typically be constructed only after an existing DMI is
destroyed.  Application code should include code similar to the following:

<pre>
someDMI dmi = null;
...
// Doing a database login/connection...
if ( dmi != null ) {
	try {	dmi.close();
		dmi = null;
	}
	catch ( Exception e ) {
		// Usually can ignore.
	}
}

try {	dmi = new someDMI ( ... );
	// If necessary...
	dmi.setXXX();
	dmi.open();
}
catch ( Exception e ) {
	// Error message/dialog...
}
</pre>
*/
public abstract class DMI{

public final static String CLASS = "DMI";

// Integer constants for database engine types, to formalize the engines that
// are supported and to streamline internal processing.

/**
Database engine corresponding to "Access" database engine (Jet).
*/
protected final int _DBENGINE_ACCESS = 10;

/**
Database engine corresponding to "Informix" database engine (no distinction
about version?).
*/
protected final int _DBENGINE_INFORMIX = 20;

/**
Database engine corresponding to "MySQL" database engine (no distinction
about version?).
*/
protected final int _DBENGINE_MYSQL = 30;

/**
Database engine corresponding to "Oracle" database engine (no distinction
about version?).
*/
protected final int _DBENGINE_ORACLE = 40;

/**
Database engine corresponding to "SQL Server 7" database engine.
*/
protected final int _DBENGINE_SQLSERVER7 = 50;

/**
Database engine corresponding to "SQL Server 2000" database engine.
*/
protected final int _DBENGINE_SQLSERVER2000 = 60;

/**
Database engine corresponding to "SQL Server 2005" database engine
*/
protected final int _DBENGINE_SQLSERVER_2005 = 61;

/**
Database engine corresponding to "PostgreSQL" database engine (no distinction
about version?).
*/
protected final int _DBENGINE_POSTGRESQL = 70;

/**
Database engine corresponding to "H2" database engine
 */
protected final int _DBENGINE_H2 = 80;

///////////////////////////////////////////////////////////
//  Commit / Rollback constants
///////////////////////////////////////////////////////////

/**
constant for referring to ROLLBACK operations
*/
public static final int ROLLBACK = 0;

/**
constant for referring to COMMIT operations
*/
public static final int COMMIT = 1;

///////////////////////////////////////////////////////////
//  SQL Type constants
///////////////////////////////////////////////////////////
/**
constant used to tell that the last query type was NONE
*/
public static final int NONE = 0;

/**
constant used to tell that the last query type was COUNT
*/
public static final int COUNT = 1;

/**
constant used to tell that the last query type was DELETE
*/
public static final int DELETE = 2;

/**
constant used to tell that the last query type was SELECT
*/
public static final int SELECT = 3;

/**
constant used to tell that the last query type was a WRITE
*/
public static final int WRITE = 4;

/** 
constant used in dmiWrite() to specify to first do an INSERT, and if that
fails, try to do an UPDATE
*/
public static final int INSERT_UPDATE	= 6;

/** 
constant used in dmiWrite() to specify to first do an UPDATE, and if that
fails, try to do an INSERT
*/
public static final int UPDATE_INSERT	= 7;

/**
constant used in dmiWrite() to specify to first delete the record before
inserting a new one
*/
public static final int DELETE_INSERT	= 8;

/**
constant used in dmiWrite() to specify to do an UPDATE
*/
public static final int UPDATE		= 9;

/**
constant used in dmiWrite() to specify to do an INSERT
*/
public static final int INSERT		= 10;

///////////////////////////////////////////////////////////
//  Member variables
///////////////////////////////////////////////////////////	

/**
Indicate whether database changes are automatically committed once they are
done.
*/
private boolean __autoCommit;

/**
Indicate whether the SQL should be capitalized before being sent to the 
database.
*/
private boolean __capitalize;

/**
If true, then when an SQL statement is executed and an error occurs, the
text of the SQL statement will be printed to the log file at Status level 2.
This should only be used for debugging and testing, and should not be enabled
in production code.
*/
private boolean __dumpSQLOnError = false;

/**
If true, then when an SQL statement is executed, the text of the SQL statement 
will be printed to the log file at Status level 2. This should only be used 
for debugging and testing, and should not be enabled in production code.
*/
private boolean __dumpSQLOnExecution = false;

/**
Indicate whether an JDBC ODBC connection is automatically set up (true) or
whether the ODBC is defined on the machine (false).
*/
private boolean __jdbc_odbc;

/**
whether the program is currently connected to the database or not
*/
private boolean __connected;

/**
Whether operations are currently being done in a transaction.
*/
private boolean __inTransaction = false;

/**
Whether for the open() methods to print information to debug as normal (false)
or to force the output to go to status level 1 (true).
*/
private boolean __printStatus = false;

/**
connection that is used for the database interaction
*/
private Connection __connection;

/**
Name of the database in the server if __jdbc_odbc is true.
*/
private String __database_name;

/**
ODBC Data Source Name if __jdbc_odbc is false.
*/
private String __odbc_name;

/**
Database engine to connect to, as a string, useful for debugging.
<p>
Valid types are:<br>
<ul>
<li>Access</ul>
<li>Informix</ul>
<li>MySQL</ul>
<li>Oracle</ul>
<li>PostgreSQL</ul>
<li>SQL_Server (interpreted as SQLServer7) - should use SQLServer7</ul>
<li>SQLServer2000</ul>
<li>SQLServer7</ul>
</ul>
*/
private String __database_engine_String;

/**
The left-side delim for reserved words.
*/
protected String _left_id_delim = "";
/**
The right-side delim for reserved words.
*/
protected String _right_id_delim = "";
/**
The delim for strings.
*/
protected String _string_delim = "";

/**
Database engine as integer, to improve performance.  The protected value is
used directly in DMIStatement and other classes in this package to improve
performance.
*/
protected int _database_engine;

/**
Database version.  This should be set by calling determineDatabaseVersion(),
which is an abstract method to be defined in derived DMI classes.  The 
standard for the version number is XXXXXXDDDDDDDD where XXXXXXX is 
typically a database version (e.g., RiverTrak 020601) and DDDDDDDD is 
usually the build date (e.g., 20020625).
*/
private long __database_version;

/**
true if any writes have been done to the database (uncommitted)
*/
private boolean __dirty = false;

/**
whether the dmi should treat the database as being in read-only mode (false,
all calls to DMI.delete() or DMI.write() are ignored) or not
*/
private boolean __editable;

/**
Host name or IP address of the database server (or "Local" if the local host
is used).
*/
private String __database_server;

/**
the last SELECT query sql string that was executed.
*/
private DMISelectStatement __lastQuery;

// TODO SAM 2007-05-08 Need to evaluate if used in troubleshooting, etc.
/**
the last SELECT query (in string form) that was executed.
JTS 16/04/02 -- This is in here so that panels that use the DMI without
the use of statement objects (ie, the SQL Analyzer-type program) can 
requery after a write or delete.
*/
private String __lastQueryString;

/**
the last SQL statement that was executed
*/
private DMIStatement __lastSQL;

/**
the type of the last SQL executed
*/
private int __lastSQLType;

/**
Port number of the database to connect, used by client-server databases.
*/
private int __port;

/**
sets whether in toString and getDatabaseProperties to print out 
information that may be secure (such as login name and password), or not.
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
Name string to identify the DMI connection.
REVISIT (JTS - 2005-04-07)
probably rendered obsolete by the new InputName stuff.  Deprecated -- anything
uses and I won't remove.  If nothing has popped up in 4 months, eliminate.
*/
private String __name;

/**
Login name to connect to the database (often used in database connection URL).
*/
private String __system_login;

/**
Password to connect to the database (often used in the database connection URL).
*/
private String __system_password;

/**
User login name to restrict access to the database.  This is typically more
restrictive than the system login but may be the same.
*/
private String __user_login;

/**
User password to restrict access to the database.  This is typically more
restrictive than the system password but may be the same.
*/
private String __user_password;

/**
Vector for holding all the statements that were created during a transaction,
so they can be closed when the transaction is committed or rolled back.
*/
private Vector __statementsVector;

/**
An empty constructor.  If this constructor is used, initialize() must be called
with the proper values to initialize the DMI settings.
*/
public DMI() {}

/**
Constructor for this DMI base class for cases when a connection to an ODBC DSN
defined on the machine.  The generic JDBC/ODBC driver is used.  To use a
manufacturer's driver, use the overloaded constructor that takes as a parameter
the database name.
@param database_engine Database engine type (see setDatabaseEngine()).
@param ODBC_name ODBC DSN to use.  The ODBC DSN must be defined on the machine.
@param system_login System login to use for the database connection.  Specify
as null to not use.  Generally the login will be defined in a derived class and
is specific to a database.
@param system_password System login to use for the database connection.  Specify
as null to not use.  Generally the login will be defined in a derived class and
is specific to a database.
@throws Exception thrown if an unknown databaseEngine is specified.
*/
public DMI(String database_engine, String ODBC_name, String system_login, 
String system_password) 
throws Exception {
	initialize(database_engine, null, null, 0, system_login, 
		system_password, ODBC_name, false);
}

/**
Constructor for this DMI base class for cases when a connection to a server
database will be defined automatically, not using a predefined ODBC DSN.
The specifid JDBC/ODBC driver for the database engine is used.  To use
the generic JDBC/ODBC driver with an ODBC DSN, use the overloaded constructor
that takes as a parameter the ODBC name.
@param database_engine Database engine type (see setDatabaseEngine()).
@param database_server Database server name (IP address or DNS-resolvable name).
@param database_name Database name on the server.
@param port Port to use for the database communications.  Specify as a negative
number to use the default.  Generally the default value determined from the
database engine is correct.
@param system_login System login to use for the database connection.  Specify
as null to not use.  Generally the login will be defined in a derived class and
is specific to a database.
@param system_password System login to use for the database connection.  Specify
as null to not use.  Generally the login will be defined in a derived class and
is specific to a database.
@throws Exception thrown if an unknown databaseEngine is specified.
*/
public DMI(String database_engine, String database_server, String database_name,
int port, String system_login, String system_password)
throws Exception {
	initialize(database_engine, database_server, database_name, port,
		system_login, system_password, null, true);
}

/**
Initialization routine that sets up the internal DMI settings.
@param database_engine the type of database engine to which the DMI is 
connecting.  See the __database_engine_string private javadocs.
@param database_server the name or IP of the server to which the DMI will 
connect.
@param database_name the name of the database on the server that the DMI will
connect to.
@param port the port on the database_server where the database listens for
connections.
@param system_login the login value to use to connect to the database server
or ODBC connection.
@param ssytem_password the login password to use to connect to the database
server or ODBC connection.
@param ODBC_name the name of the ODBC data source to connect to.
@param jdbc_odbc if true then the connection is a IP or server-name -based
connection.  If false, it is an ODBC connection.
REVISIT (JTS - 2006-05-22)
This boolean seems backwards!!!
@throws Exception if the database_engine that is passed in is not recognized.
*/
public void initialize(String database_engine, String database_server, 
String database_name, int port, String system_login, String system_password,
String ODBC_name, boolean jdbc_odbc)
throws Exception {
	String routine = "DMI.DMI";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"DMI created for database engine: " + database_engine);
	} 

	// initialize member variables
	__database_engine_String= database_engine;

	__jdbc_odbc		= jdbc_odbc;
	__database_name		= database_name;
	__odbc_name		= ODBC_name;
	__database_server	= database_server;
	__system_login		= system_login;
	__system_password	= system_password;
	__user_login		= null;
	__user_password		= null;
	__lastSQL		= null;
	__lastQuery		= null;
	__lastQueryString 	= null;
	__connection 		= null;

	__database_version	= 0;
	__port			= port;
	
	__dirty			= false;
	__editable		= false;
	__secure		= false;
	__autoCommit 		= true;
	__connected 		= false;

	__lastSQLType		= NONE;

	// Check the database engine type and set appropriate defaults...
	if (__database_engine_String.equalsIgnoreCase("Access")) {
		_left_id_delim = "[";
		_right_id_delim = "]";	
		_string_delim = "'";
		__database_server = "Local";
		_database_engine = _DBENGINE_ACCESS;
	}
	else if (__database_engine_String.equalsIgnoreCase("Informix")) {
		_left_id_delim = "\"";
		_right_id_delim = "\"";	
		_string_delim = "'";
		// The port number for Informix is typically not changed...
		if ( port <= 0 ) {
			__port = 1526;
		}
		_database_engine = _DBENGINE_INFORMIX;
	}
	else if (__database_engine_String.equalsIgnoreCase("MySQL")) {
		_left_id_delim = "\"";
		_right_id_delim = "\"";	
		_string_delim = "'";
		_database_engine = _DBENGINE_MYSQL;
	}
	else if (__database_engine_String.equalsIgnoreCase("Oracle")) {
		_left_id_delim = "\"";
		_right_id_delim = "\"";	
		_string_delim = "'";
		_database_engine = _DBENGINE_ORACLE;
	}
	else if (__database_engine_String.equalsIgnoreCase("PostgreSQL")) {
		_left_id_delim = "\"";
		_right_id_delim = "\"";	
		_string_delim = "'";
		// Default port number for postgresql is 5432
		// and is not typically changed
		if ( port <= 0 ) {
			__port = 5432;
		}
		_database_engine = _DBENGINE_POSTGRESQL;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQL_Server") ||
		__database_engine_String.equalsIgnoreCase("SQLServer7")) {
		_left_id_delim = "[";
		_right_id_delim = "]";		
		_string_delim = "'";
		// SQL_Server is being phased out in favor of SQLServer7.
		// The port number for SQL server is typically not changed...
		if ( port <= 0 ) {
			__port = 1433;
		}
		_database_engine = _DBENGINE_SQLSERVER7;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQLServer2000")) {
		_left_id_delim = "[";
		_right_id_delim = "]";	
		_string_delim = "'";
		// The port number for SQL server is typically not changed...
		if ( port <= 0 ) {
			__port = 1433;
		}
		_database_engine = _DBENGINE_SQLSERVER2000;
	}
    else if (__database_engine_String.equalsIgnoreCase("SQLServer2005")) {
        _left_id_delim = "[";
		_right_id_delim = "]";	
		_string_delim = "'";
		// The port number for SQL server is typically not changed...
		if ( port <= 0 ) {
			__port = 1433;
		}
		_database_engine = _DBENGINE_SQLSERVER_2005;
    }
    else if (__database_engine_String.equalsIgnoreCase("H2")) {
        _left_id_delim = "";
		_right_id_delim = "";	
		_string_delim = "'";
		_database_engine = _DBENGINE_H2;
    }
	else {	throw new Exception("Trying to use unknown database engine: "
			+ __database_engine_String + " in DMI()");
	}

	__statementsVector = new Vector();
}

/**
Close the database connection.  If the database is not connected yet, an 
exception will be thrown.
@throws SQLException thrown if the java.sql code has 
any problems doing a Connection.close()
*/
public void close() throws SQLException{
	// let the JDBC handle the close
	if (!__connected) {
		throw new SQLException ("Database not connected, cannot call "
			+ "DMI.close()");	
	}	
	
	__connection.close();
	__connected = false;
}

/**
Closes a result set and frees the resources associated with it.
@param rs the ResultSet to close.
@throws SQLException if there is any error closing the result set.
*/
public static void closeResultSet(ResultSet rs) 
throws SQLException {
	if (rs != null) {
		Statement s = rs.getStatement();
		rs.close();
		if (s != null) {
			s.close();
			s = null;
		}
		rs = null;
	}
}


/**
Closes a result set from a stored procedure and frees the resources associated 
with it.
@param rs the ResultSet to close.
@param select the select statement that was set up to execute the stored
procedure.
*/
public static void closeResultSet(ResultSet rs, DMIStatement select) 
throws SQLException {
	if (rs != null) {
		Statement s = rs.getStatement();
		rs.close();
		if (s != null) {
			s.close();
			s = null;
		}
		rs = null;
	}

	if (select.getCallableStatement() != null) {
		select.getCallableStatement().close();
	}
}

/**
Closes an statements that were opened during a transaction.  Called 
automatically by commit() and rollback().
*/
private void closeStatements() {
	String routine = "DMI.closeStatements()";
	int size = __statementsVector.size();
	Statement s = null;
	for (int i = 0; i < size; i++) {
		s = (Statement)__statementsVector.elementAt(i);
		try {
			s.close();
		}
		catch (Exception e) {
			Message.printWarning(2, routine, 
				"Error closing statement:");
			Message.printWarning(2, routine, e);
		}
	}
}

/**
Commits any database operations that have been made since the beginning
of the current transaction.
@throws SQLException thrown if the java.sql code has any problems 
doing a Connection.commit() or in setAutoCommit(), or if the database
was not connected when the call was made
*/
public void commit() throws SQLException {
// REVISIT (JTS - 2006-05-22)
// This code has not been tested or used in nearly 4 years.  Do not rely
// on this method without testing it first!
	if (!__connected) {
		throw new SQLException ("Database not connected, cannot call "
			+ "DMI.commit()");
	}
	
	__inTransaction = false;

	// Connection.commit() should only be used when autoCommit
	// is turned off, so check that
	if (__autoCommit == false) {
		__connection.commit();
	}

	// since commit() marks the end of a transaction, turn 
	// autoCommit back on and mark the database as not dirty (clean)
	setAutoCommit(true);
	__dirty = false;

	closeStatements();
}

/**
Indicate whether the database is connected.
@return true if the database connection is made, false otherwise.
*/
public boolean connected() {
	return __connected;
}

/**
Determine the version of the database.  This method should be defined in a
derived class and be called when a database connection is made (e.g., in the
derived class open() method).  The database version should be set to 0 (zero) if
it cannot be determined.
*/
public abstract void determineDatabaseVersion ();

/** 
Execute a count query to find the number of records specified by the query.
This may be used in conjunction with a select statement to find
out how many records will be pulled back (for doing a progress bar or the
like), so the input sql string can be a properly-formatted <b><code>
SELECT </b></code>
statement, or a normal <b><code>SELECT COUNT</b></code> statement.<p>
If a normal SELECT statement is passed in, 
this method will chop off any <b><codE>ORDER BY</b></code> clauses and
will also remove the fields that are being selected and replace them
with <b><code>COUNT(*)</b></code>.<p>
For instance, the sql:<br>
<code>
SELECT field1, field2 FROM tableName where field3 = 123 ORDER BY field4;<p>
</code> will converted to:<br>
<code> 
SELECT COUNT(*) FROM tableName where field3 = 123;<p>
</codE> prior to being run.
<p>
<b>Known issues</b><br>
A problem has been encountered with an ODBC connection to a Microsoft Access
97 database in which malformed COUNT() statements have returned incorrect
results.  <p>
An SQL statement of SELECT COUNT(*) ASDKJASD (where ASDKJASD could actually
be pretty much any word or combination of letters) would return a result that
said that 1 record was returned.  It is unclear why this SQL statement was
not returning an error from the database that the SQL was malformed.
@param sql a Select statement for which the number of records that will
be affected is the result wanted
@return an integer telling how many records were counted
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery(), or if the database
was not connected
*/
public int dmiCount(String sql) throws SQLException{
// REVISIT (JTS - 2006-05-22)
// This code has not been tested or used in nearly 4 years.  Do not rely
// on this method without testing it first!
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot make"
			+ " call to DMI.dmiCount()");
	}
	
	String routine = "DMI.dmiCount";
	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
		"SQL to count  (pre): '" + sql + "'");
	}
	
	sql = sql.trim();
	sql = sql.toUpperCase();
	
	// if the sql statement is already formatted as a SELECT COUNT 
	// statement, do nothing with it.
	if (!sql.startsWith("SELECT COUNT")) {
	// otherwise, the sql is formatted as a SELECT statement
	// so the 'SELECT [fields]' part is removed ...
	if (sql.indexOf("FROM") >= 0) {
		sql = sql.substring(sql.indexOf("FROM"));
		// and then the ORDER BY clauses are removed
		// as well, leaving behind only
		// "FROM [table] WHERE (wheres)"
		if (sql.indexOf("ORDER BY") >= 0) {
			sql = sql.substring(0, 
				sql.indexOf("ORDER BY"));
		}
	}
	
	// graft on the count statement to the beginning 
	// and it is functional sql again, or should be
		sql = "SELECT COUNT(*) " + sql;
	}
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"SQL to count (post): '" + sql + "'");
	}	
	
	Statement s = __connection.createStatement();
	ResultSet rs = s.executeQuery(sql);
	rs.next();
	int count = rs.getInt(1);

	closeResultSet(rs);
	if (__inTransaction) {
		__statementsVector.add(s);
	}
	else {
		s.close();
	}
		
	return count;
}

/**
Executes a count query from a DMISelectStatement object.  This creates the 
SQL string from the DMISelectStatement object and then passes the resulting
string to the dmiCount(String) function.
@param s a DMISelectStatement object to run a count by 
@return an integer telling how many records were counted
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery()
*/
public int dmiCount(DMISelectStatement s) 
throws SQLException {
// REVISIT (JTS - 2006-05-22)
// This code has not been tested or used in nearly 4 years.  Do not rely
// on this method without testing it first!
	if (!__connected) {
		throw new SQLException ("Database not connected, cannot call "
			+ "DMI.dmiCount()");
	}	
	
	if (s.isStoredProcedure()) {
		ResultSet rs = s.executeStoredProcedure();
		rs.next();
		return rs.getInt(1);
	}
	
	// saves the DMISelectStatement as the last statement executed
	setLastStatement(s);

	return dmiCount(s.toString());
}

/**
Executes a database delete from a DMIDeleteStatement object.  This method
creates the SQL string from the DMIDeleteStatement object and then passes
the resulting string to the dmiDelete(String) function.<p>
The code in dmiDelete is surrounded by a check of the private boolean member 
variable __editable (see isEditable() and setEditable(boolean)).  If
__editable is set to true, none of the code is executed and an exception
is thrown.
@param s a DMIStatement object containing a delete statement (should be a
DMIDeleteStatement object)
@return the number of rows deleted
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery().  Also thrown if
the database is in read-only mode
*/
public int dmiDelete(DMIDeleteStatement s) throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected, cannot call "
			+ "DMI.dmiDelete()");
	}	
	
	if (!__editable) {
		throw new SQLException("Database in read-only mode, cannot "
			+ "execute a dmiDelete");
	}
	
	if (s.isStoredProcedure()) {
		s.executeStoredProcedure();
		return s.getIntReturnValue();
	}
	else {	
		// saves the DMIDeleteStatement as the last statement executed
		setLastStatement(s);
		return dmiDelete(s.toString());
	}
}

/** 
Executes a database delete.  This method runs the SQL DELETE statement
in its String parameter.<p>
The code in dmiDelete is surrounded by a check of the private boolean member 
variable __editable (see isEditable() and setEditable(boolean)).  If
__editable is set to true, none of the code is executed and an exception
is thrown. 
@param sql the sql statement that contains the <b><code>DELETE</b></code>
command
@return the number of rows deleted
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery().  Also thrown if
the database is in read-only mode, or if it is not connected
*/
public int dmiDelete(String sql) throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected. Cannot make "
			+ "call to DMI.dmiDelete()");
	}

	if (__dumpSQLOnExecution) {
		Message.printStatus(2, "DMI.dmiDelete", sql);
	}
	
	String routine = "DMI.dmiDelete";
	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}
	
	if (!__editable) {
		throw new SQLException("Database in read-only mode, cannot "
			+ "execute a dmiDelete");
	}

	Statement s = __connection.createStatement();
	if (__capitalize) {
		sql = sql.toUpperCase();
	}
	
	int result = 0;
	try {	
		result = s.executeUpdate(sql);
	}
	catch (SQLException ex) {
		if (__dumpSQLOnError) {
			Message.printStatus(2, "DMI.dmiDelete", sql);
		}
		throw ex;
	}
	
	if (__inTransaction) {
		__statementsVector.add(s);
	}
	else {
		s.close();
	}	

	// used for knowing when to do a 
	// startTransaction(ROLLBACK) versus a 
	// startTransaction(COMMIT).  since a delete statement
	// causes a database change (and if the code has 
	// gotten this far the delete was successful and 
	// didn't throw an exception), the database can 
	// now be considered changed and the __dirty flag 
	// should be set
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
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiWrite");
	}

	if (!__editable) {
		throw new SQLException ("Database is in read-only mode, cannot"
			+ " make call to DMI.dmiWrite");
	}	
	
	String routine = "DMI.dmiExecute";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "SQL: '" + sql + "'");
	}

	Statement s = __connection.createStatement();
	if (__capitalize) {
		sql = sql.toUpperCase();
	}	

	int result = s.executeUpdate(sql);

	if (__inTransaction) {
		__statementsVector.add(s);
	}
	else {
		s.close();
	}	

	return result;
}

/**
Re-executes the previous sql statement executed by the database.  
Everytime one of the dmiCount, dmiDelete, dmiWrite or dmiSelect 
methods that take a DMIDeleteStatement, DMIWriteStatement or 
DMISelectStatement as their parameter is called is called, 
the DMI stores the DMI*Statement.  This method re-runs the DMI*Statement
executed just previously by the database.
<p>
If the database is accessed via one of the dmiCount, dmiDelete, or
dmiWrite statements that takes a String argument then no previous-sql
information is saved.  <p>
If the database is accessed via a dmiSelect statement that takes a String
argument, the SELECT statement is saved, but cannot be used by this method.
It saved for use in the dmiRunLastSelectForTableModel function.
<b>Note:</b><p>
If using stored procedures, this method WILL NOT WORK.  It only works with
DMI connections that pass String-based SQL statements (either as Strings or
DMIStatement objects).
@return an object, the type of which depends on which kind of query was
being executed.  The problem here is that SELECT queries return 
Vector objects while all other sql statements return integers.
Any method which calls this function should first call <b><code>
getLastSQLType() </b></code> so that it knows what sort of object will
be returned, and then cast the return type appropriately.<p>
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
@throws Exception thrown by dmiWrite(DMIWriteStatement) when a DELETE_INSERT
is attempted
*/
public Object dmiRunLastSQL() throws SQLException, Exception {
	switch(__lastSQLType) {
		case NONE:
			return null;
		case SELECT:
			return dmiSelect((DMISelectStatement)__lastSQL);
		case WRITE:
			return new Integer(
			dmiWrite((DMIWriteStatement)__lastSQL, UPDATE_INSERT));
		case DELETE:
			return new Integer(
			dmiDelete((DMIDeleteStatement)__lastSQL));
		case COUNT:
			return new Integer(
			dmiCount((DMISelectStatement)__lastSQL));
	}
	return null;
}

/** 
Re-executes the last SELECT statement executed.  Used for requerying
a table after a DELETE or INSERT statement is executed.  
<code>dmiRunSQL</code> can't be used at that point as the last
SQL executed will have been the DELETE or INSERT statement
@return a ResultSet of the values returned from the query
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery()
*/
public ResultSet dmiRunSelect() throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiRunSelect()");
	}
	return dmiSelect((DMISelectStatement)__lastQuery);
}

/**
Runs an sql string that contains a <b><code>SELECT</b></code> statement 
and returns a resultSet of the records returned.
@param sql an sql statement that contains the <b><code>SELECT</b></code>
statement to be executed
@return the resultset pulled back from the operation
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery(), or if the database
is not connected
*/
public ResultSet dmiSelect(String sql) throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiSelect()");
	}
	
	if (__dumpSQLOnExecution) {
		Message.printStatus(2, "DMI.dmiSelect", sql);
	}
	
	String routine = "DMI.dmiSelect";
	int dl = 25;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"SQL: '" + sql + "'");
	}

	Statement s = __connection.createStatement();
	ResultSet rs = null;
	if (__capitalize) {
		sql = sql.toUpperCase();
	}	
	
	__lastQueryString = sql;

	try {
		rs = s.executeQuery(sql);
	}
	catch (SQLException ex) {
		if (__dumpSQLOnError) {
			Message.printStatus(2, "DMI.dmiSelect", sql);
		}
		throw ex;
	}
		
	return rs;
}

/**
Execute an SQL select statement from a DMISelectStatement object.  The sql
statement is built from the DMISelectStatement object and the resulting 
string passed to dmiSelect(String).
@param select an DMISelectStatement instance specifying the query.
@return the ResultSet from the select.
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery()
*/
public ResultSet dmiSelect ( DMISelectStatement select )
throws SQLException {	
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiSelect()");
	}

	if (select.isStoredProcedure()) {
		return select.executeStoredProcedure();
	}
	else {	
		// sets the DMISelectStatement as the last statement executed
		setLastStatement(select);
		return dmiSelect(select.toString());
	}
}

/**
Runs an sql statement that contains a <b><code>INSERT</b></code> or <b><code>
UPDATE</b></code>.<p>
The code in dmiWrite is surrounded by a check of the private boolean member 
variable __editable (set isEditable() and setEditable(boolean).  If
__editable is set to true, none of the code is executed and an exception
is thrown.  
@param sql an sql command that contains the insert or update command to be 
run
@return an integer of the the rowcount from the insert or update
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeUpdate().  Also thrown if
the database is in read-only mode, or if the database is not connected
*/
public int dmiWrite(String sql) throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiWrite");
	}

	if (!__editable) {
		throw new SQLException ("Database is in read-only mode, cannot"
			+ " make call to DMI.dmiWrite");
	}	

	if (__dumpSQLOnExecution) {
		Message.printStatus(2, "DMI.dmiWrite", sql);
	}
	
	String routine = "DMI.dmiWrite";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"SQL: '" + sql + "'");
	}

	Statement s = __connection.createStatement();
	if (__capitalize) {
		sql = sql.toUpperCase();
	}	
	
	int result = 0;
	try {
		result = s.executeUpdate(sql);
	}
	catch (SQLException ex) {
		if (__dumpSQLOnError) {
			Message.printStatus(2, "DMI.dmiWrite", sql);
		}
		throw ex;
	}
	
	if (__inTransaction) {
		__statementsVector.add(s);
	}
	else {
		s.close();
	}	

	return result;
}

/**
Executes an insert or update statement from a DMIWriteStatement object.
The SQL string stored in the DMIWriteStatement object is generated and the
resulting string passed into a dmiWrite(String) function that does the 
work.<p>
The code in dmiWrite is surrounded by a check of the private boolean member 
variable __editable (set isEditable() and setEditable(boolean).  If
__editable is set to true, none of the code is executed and an exception
is thrown.  
@param s a DMIWriteStatement object to be executed
@return an integer of the rowcount from the insert or update
@throws SQLException thrown if there are problems with a 
Connection.createStatement or Statement.executeQuery().  Also thrown if the
database is in read-only mode, or if the database is not connected
@throws Exception thrown if a DELETE_INSERT statement is run, as this
statement type is not supported yet. 
*/
public int dmiWrite(DMIWriteStatement s, int writeFlag) 
throws SQLException, Exception {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.dmiWrite()");
	}
	if (!__editable) {
		throw new SQLException("Database is in read-only mode");
	}

	if (s.isStoredProcedure()) {
		s.executeStoredProcedure();
		return s.getIntReturnValue();
	}

	// sets the DMIWriteStatement as the last statement executed
	setLastStatement(s);
	
	Statement stmt = __connection.createStatement();

	/////////////////////////////////////////////////////////////
	// TEXT SPACING NOTE:
	/////////////////////////////////////////////////////////////
	// Some of the tabbing has been deliberately shifted to the left
	// simply because the extensive indenting was leading to lines only
	// about 20 characters long, and pretty much unreadable
	//                                     - JTS
	/////////////////////////////////////////////////////////////

	switch (writeFlag) {
	case INSERT_UPDATE:
	// first try to insert the statement in the table.  

	////////////////////////////////////////////////////////////////
	// NOTE FOR DEVELOPERS:
	////////////////////////////////////////////////////////////////
	// This database write method works by first trying to insert a 
	// a record, and if that record already existed in the database,
	// an update is performed.  
	//
	// The tricky part is that each database sends different error
	// codes to tell when a duplicate record is found on an insert.
	// To add a new database type to this section, a duplicate record
	// insert must attempted on the new database type and the SQLState
	// and ErrorCode from the resulting SQLException must be recorded
	// and then used below (where, for instance, ODBC has S1000 and 0)
	///////////////////////////////////////////////////////////////	
	try {
		if (__dumpSQLOnExecution) {
			Message.printStatus(2, "DMI.dmiWrite", "Trying to "
				+ "execute INSERT: " + s.toInsertString());
		}
		stmt.executeUpdate(s.toInsertString());
		// used for knowing when to do a startTransaction(ROLLBACK) 
		// versus a startTransaction(COMMIT).  since a delete 
		// statement causes a database change (and if the code has 
		// gotten this far the delete was successful and didn't throw 
		// an exception), the database can now be considered changed 
		// and the __dirty flag should be set		
		testAndSetDirty();				
	} 
	catch (SQLException e) {
		if (_database_engine == _DBENGINE_ACCESS ) {
			if (e.getSQLState() == "S1000" && 
			    e.getErrorCode() == 0) {
			    	// The Insert failed because a record with
				// the existing key data already exists.
				// That record will be updated, instead.
				try {
					if (__dumpSQLOnExecution) {
						Message.printStatus(2, 
							"DMI.dmiWrite",
							s.toUpdateString());
					}
					stmt.executeUpdate(s.toUpdateString());
				}
				catch (Exception ex) {
					if (__dumpSQLOnError) {
						Message.printStatus(2, 
							"DMI.dmiWrite", 
							s.toUpdateString());
					}
					throw ex;
				}
						
				// used for knowing when to do a 
				// startTransaction(ROLLBACK) versus a 
				// startTransaction(COMMIT).  since a delete 
				// statement causes a database change (and if 
				// the code has gotten this far the delete was 
				// successful and didn't throw an exception), 
				// the database can now be considered changed 
				// and the __dirty flag should be set
				testAndSetDirty();
			} 
			else {
				stmt.close();
				throw e;
			}
		}
		else if ( (_database_engine == _DBENGINE_SQLSERVER2000) ||
			(_database_engine == _DBENGINE_SQLSERVER7) ){
			if (e.getSQLState() == "23000" && 
			    e.getErrorCode() == 2627) {
			    	// The Insert failed because a record with
				// the existing key data already exists.
				// That record will be updated, instead.
				try {
					if (__dumpSQLOnExecution) {
						Message.printStatus(2, 
							"DMI.dmiWrite",
							s.toUpdateString());
					}
					stmt.executeUpdate(s.toUpdateString());
				}
				catch (Exception ex) {
					if (__dumpSQLOnError) {
						Message.printStatus(2, 
							"DMI.dmiWrite", 
							s.toUpdateString());
					}
					throw ex;
				}
									
				// used for knowing when to do a 
				// startTransaction(ROLLBACK) versus a 
				// startTransaction(COMMIT).  since a delete 
				// statement causes a database change (and if 
				// the code has gotten this far the delete was 
				// successful and didn't throw an exception), 
				// the database can now be considered changed 
				// and the __dirty flag should be set
				testAndSetDirty();
			} 
			else {
				stmt.close();
				throw(e);
			}
		}
		else {
			throw new Exception ("INSERT_UPDATE may have "
			+ "encountered an " 
			+ "existing record, but since there is no information "
			+ "on the INSERT error codes for the database type " 
			+ "with which you are working ("
			+ __database_engine_String + ") "
		 	+ "there is no certainty about this.  DMI needs to be "
			+ "worked on in this regard.");
		}
	}
	break;
	case UPDATE_INSERT:
		int result = 0;
		try {
			if (__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite",
					s.toUpdateString(true));
			}
			result = stmt.executeUpdate(s.toUpdateString(true));
		}
		catch (Exception e) {
			if (__dumpSQLOnError) {
				Message.printStatus(2, "DMI.dmiWrite", 
					s.toUpdateString());
			}
			throw e;
		}

		if (result == 0) {
			// The update failed, so try an insert.
			if (__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite",
					s.toInsertString());
			}
			try {
				result = stmt.executeUpdate(s.toInsertString());
			}
			catch (Exception e) {
				if (__dumpSQLOnError) {
					Message.printStatus(2, "DMI.dmiWrite", 
						s.toInsertString());
				}
				throw e;
			}		
		}
		// used for knowing when to do a startTransaction(ROLLBACK) 
		// versus a startTransaction(COMMIT).  since a delete 
		// statement causes a database change (and if the code has 
		// gotten this far the delete was successful and didn't throw 
		// an exception), the database can now be considered changed 
		// and the __dirty flag should be set				
		testAndSetDirty();			
		break;
	case UPDATE:
		try {
			if (__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite",
					s.toUpdateString());
			}
			stmt.executeUpdate(s.toUpdateString());	
		}
		catch (Exception e) {
			if (__dumpSQLOnError) {
				Message.printStatus(2, "DMI.dmiWrite", 
					s.toUpdateString());
			}
			throw e;
		}
		
		// used for knowing when to do a startTransaction(ROLLBACK) 
		// versus a startTransaction(COMMIT).  since a delete 
		// statement causes a database change (and if the code has 
		// gotten this far the delete was successful and didn't throw 
		// an exception), the database can now be considered changed 
		// and the __dirty flag should be set				
		testAndSetDirty();			
		break;
	case INSERT:		
		try {
			if (__dumpSQLOnExecution) {
				Message.printStatus(2, "DMI.dmiWrite",
					s.toInsertString());
			}
			stmt.executeUpdate(s.toInsertString());	
		}
		catch (Exception e) {
			if (__dumpSQLOnError) {
				Message.printStatus(2, "DMI.dmiWrite", 
					s.toInsertString());
			}
			throw e;
		}
		// used for knowing when to do a startTransaction(ROLLBACK) 
		// versus a startTransaction(COMMIT).  since a delete 
		// statement causes a database change (and if the code has 
		// gotten this far the delete was successful and didn't throw 
		// an exception), the database can now be considered changed 
		// and the __dirty flag should be set		
		testAndSetDirty();			
		break;
	case DELETE_INSERT:
		Message.printWarning(25, "DMI.dmiWrite",
		"DELETE_INSERT not implemented yet");
		throw new Exception ("DELETE_INSERT not implemented");
	default:
		throw new Exception ("Unspecified WRITE type in DMI.dmiWrite:"
		+ writeFlag);
		}

	if (__inTransaction) {
		__statementsVector.add(stmt);
	}
	else {
		stmt.close();
	}			

	// REVISIT 
	// Remove the integer return values, or not?
	// The argument can be made for keeping and removing the return values.
	//
	// Against:
	// They are unnecessary.  When working with write statements, no return
	// is anticipated and therefore is unlikely to be used.
	//
	// For:
	// It's just a little more debug information.  The return value from a
	// update or insert is how many records were updated or inserted.  
	// While that number should be known for insert statements, perhaps
	// there could be a time when the number of updated records would be
	// useful information to have.
	return 0;

}

/**
Applies escape sequences to a string based on the kind of database being
used.  Currently only works with "SQL_Server", "SQLServer7" and "SQLServer2000"
databases.  It will translate all instances of ' (apostrophe) to '' (two 
apostrophes). 
@param str the string to check for characters that need escaped.
@return a string with the right escape sequences inserted.
*/
public String escape(String str) {
	String workStr = new String(str);
	if (getDatabaseEngine().equalsIgnoreCase("SQL_Server") ||
	    getDatabaseEngine().equalsIgnoreCase("SQLServer7") ||
	    getDatabaseEngine().equalsIgnoreCase("SQLServer2000")) {	
		if (workStr.indexOf('\'') > -1) {
			workStr = StringUtil.replaceString(str, "'", "''");
		}	
	}

	// any other escaping can be done here, too ...

	return workStr;
}

/**
Clean up memory for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize() 
throws Throwable {
	__database_engine_String= null;
	__database_name		= null;
	__odbc_name		= null;
	__database_server	= null;
	__system_login		= null;
	__system_password	= null;
	__user_login		= null;
	__user_password		= null;
	__lastSQL		= null;
	__lastQuery		= null;
	__lastQueryString 	= null;
	__connection 		= null;

	super.finalize();
}

/**
Returns all the kinds of databases a DMI can connect to.
@return a Vector of all the kinds of databases a DMI can connect to.
*/
protected static Vector getAllDatabaseTypes() {
	Vector v = new Vector();
	v.add("Access");
	v.add("Informix");
	v.add("MySQL");
	v.add("Oracle");
	v.add("PostgreSQL");
	v.add("SQL_Server");
	v.add("SQLServer2000");
	v.add("SQLServer7");
	return v;
}

/**
Returns the current value of the autoCommit setting
@return the value of the autoCommit setting
*/
public boolean getAutoCommit() {
	return __autoCommit;
}

/**
Returns whether the SQL is being capitalized or not.
@return whether the SQL is being capitalized or not.
*/
public boolean getCapitalize() {
	return __capitalize;
}

/**
Returns the connection being used to interact with the database
@return the connection being used to interact with the database
*/
public Connection getConnection() {
	return __connection;
}

/**
Returns the meta data associated with the currently-opened connection, or null
if there is no open connection.
@return the meta data associated with the currently-opened connection, or null
if there is no open connection.
*/
public DatabaseMetaData getDatabaseMetaData() {
	if (__connected) {
		try {
			return __connection.getMetaData();
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
completed automatically and a pre-defined ODBC DSN is not used.
@return the name of the database.
*/
public String getDatabaseName() {
	return __database_name;
}

/**
Return a Vector of Strings containing properties from the DMI object.
DMI classes that extend this class should define this method for use in general
database properties displays.  Currently there is no standard envisioned for the
database information, only that it provide the user with useful information
about the database connection.  The contents returned here depend on whether the
connection is secure (if not, login and password information are not printed).
@return a Vector containing database properties as Strings.
@param level A numerical value that can be used to control the amount
of output, to be defined by specific DMI instances.  A general guideline is to
use 3 for full output, including database version, history of changes, server
information (e.g., for use in a properties dialog); 2 for a concise output
including server name (e.g., for use in the header of an output file; 1 for
very concise output (e.g., the database name and version, for use in a product
footer).  This arguments defaults to 3 if this base class method is called.
*/
public Vector getDatabaseProperties(int level) {
	Vector v = new Vector();
	if ( __jdbc_odbc )
	{ 	// Need the server name, database name, etc...
		v.addElement( "Database engine: " + __database_engine_String );
		v.addElement( "Database server: " + __database_server);
		v.addElement( "Database name: " + __database_name);
		v.addElement( "Database port: " + __port);
	}
	else
	{	v.addElement( "ODBC DSN: " + __odbc_name );
	}
	// Always have this...
	v.addElement( "Database version: " + __database_version );

	// Secure information...

	if ( __secure )
	{	v.addElement( "System login: " + __system_login);
		v.addElement( "System password: " + __system_password);
		v.addElement( "User login: " + __user_login);
		v.addElement( "User password: " + __user_password);
	}
	
	return v;
}

/**
Return the database engine type.  One of:<br>
<ul>
<li>Access</li>
<li>Informix</li>
<li>MySQL</li>
<li>Oracle</li>
<li>PostgreSQL</li>
<li>SQL_Server (interpreted as SQLServer7) - should use SQLServer7</li>
<li>SQLServer2000</li>
<li>SQLServer7</li>
</ul>
@return the database engine type.
*/
public String getDatabaseEngine() {
	return __database_engine_String;
}

/**
Return the database server name (IP address or DNS-resolvable machine name).
The server name is only required if setDataseName() is also called.
Otherwise, the connection is assumed to use a predefined ODBC DSN,
which can be retrieved using getODBCName().
@return the database server name (IP address or DNS-resolvable machine name).
*/
public String getDatabaseServer() {
	return __database_server;
}

/**
Return the database version number.
@return the database version number.
*/
public long getDatabaseVersion () {
	return __database_version;
}

/**
Return the status of the dirty flag
@return the status of the dirty flag
*/
public boolean getDirty() {
	return __dirty;
}

/**
Returns the ID string that identifies the connection.
@return the ID string that identifies the connection.
*/
public String getID() {
	return __id;
}

/**
Returns the name of the connection.
@return the name of the connection.
*/
public String getInputName() {
	return __inputName;
}

/**
Indicate whether this is a jdbc_odbc connection or not.
@return true if the database connection uses JDBC/ODBC (meaning that the
connection is created with a database server and database name.  Return false if
the connection is made by specifying an ODBC DSN.
*/
public boolean getJDBCODBC() {
	return __jdbc_odbc;
}

/**
Returns the last query string that was executed
@return the last query string that was executed
*/
public String getLastQueryString() {
	if (__lastQuery == null) {
		return "";
	} else {
		return __lastQuery.toString();
	}
}

/** 
Returns the last sql string that was executed.  Returns "" if the DMI 
connection is using stored procedures.
@return the last sql string that was executed.
*/
public String getLastSQLString() {
	switch(__lastSQLType) {
		case NONE:
			return "";
		case WRITE:
			DMIWriteStatement w = (DMIWriteStatement)__lastSQL;
			return w.toUpdateString() + " / " + w.toInsertString();
		case SELECT:
		case COUNT:
			DMISelectStatement s = (DMISelectStatement)__lastSQL;
			return s.toString();			
		case DELETE:
			DMIDeleteStatement d = (DMIDeleteStatement)__lastSQL;
			return d.toString();
	}
	return "";
}

/**
Returns the type of the last sql string that was executed
@return the type of the last sql string that was executed
*/
public int getLastSQLType() {
	return __lastSQLType;
}

/**
Returns the left id delimiter
@return the left id delimiter
*/
public String getLeftIdDelim() {
	return _left_id_delim;
}

/**
Returns the name of the connection.
@return the name of the connection.
@deprecated (2005-04-07)
*/
public String getName() {
	return __name;
}

/**
Return the ODBC Data Source Name (DSN).
This is used with a pre-defined ODBC DSN on the machine.
@return the ODBC DSN for the database connection.
*/
public String getODBCName() {
	return __odbc_name;
}

/**
Returns the port of the database to connect to
@return the port of the database to connect to
*/
public int getPort() {
	return __port;
}

/**
Returns the default port number for a certain database.
@param type the kind of database to return a port number for.
@return the default port number for a database, or -1 if unknown.
*/
protected static int getPortForDatabaseType(String type) {
	if (type.equals("Informix")) {
		return 1526;
	}
	else if (type.equals("SQL_Server")
		|| type.equals("SQLServer2000")
		|| type.equals("SQLServer7")) {
		return 1433;
	}
	else {
		return -1;
	}

}

/**
Returns the right id delimiter
@return the right id delimiter
*/
public String getRightIdDelim() {
	return _right_id_delim;
}

/**
Returns the setting of the secure variable
@return the setting of the secure variable
*/
public boolean getSecure() {
	return __secure;
}

/**
Returns the database types a DMI can connect to that are done via direct
server connection.
@return a Vector of the database types a DMI can connect to that are done via
direct server connection.
*/
protected static Vector getServerDatabaseTypes() {
	Vector v = new Vector();
	v.add("Informix");
	v.add("MySQL");
	v.add("Oracle");
	v.add("PostgreSQL");
	v.add("SQL_Server");
	v.add("SQLServer2000");
	v.add("SQLServer7");
	return v;
}

/**
Returns the string delimiter
@return the string delimiter
*/
public String getStringIdDelim() {
	return _string_delim;
}

/**
Return the system login name for the database connection.
@return the login name for the database connection.
*/
public String getSystemLogin() {
	return __system_login;
}

/**
Return the password for the database connection.
@return the password for the database connection.
*/
public String getSystemPassword() {
	return __system_password;
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
	return __user_password;
}

/**
Indicate whether the database version is at least the indicated version.
@return true if the database version is at least that indicated.
*/
public boolean isDatabaseVersionAtLeast ( long version ) {
	// The database versions are just numbers so can check arithmetically...
	if ( __database_version >= version ) {
		return true;
	}
	else {	return false;
	}
}

/**
whether or not the database is set into read-only mode.  If isEditable()
returns true, then no calls to dmiWrite or dmiDelete will be executed.
@return true if the database is in read-only mode, false if not
*/
public boolean isEditable() {
	return __editable;
}

/**
Indicate whether the DMI is connected to a database.
@return true if the database connection has been established.
*/
public boolean isOpen() {
	return __connected;
}

/**
Opens the connection to the DMI with information that was previously set, 
and sets printStatus to the given value while open() (the other version of the
method, not this one) is being called, in order to print more debugging
information.  Once this method is done, __printStatus will be set to false.<p>
__printStatus is primarily used in printStatusOrDebug(), used internally for
debugging DMI connection open()s.
@param printStatus whether to print information to status while in open().
@throws SQLException if the dmi is already connected.
@throws Exception if some other error happens while connecting to the database.
*/
public void open(boolean printStatus) 
throws Exception, SQLException {
	__printStatus = printStatus;
	try {
		open();
	}
	catch (SQLException se) {
		__printStatus = false;
		throw se;
	}
	catch (Exception e) {
		__printStatus = false;
		throw e;
	}
	__printStatus = false;
}

/**
Open a connection to the database with information that was previously
specified.
@throws SQLException if the dmi is already connected.
@throws Exception if some other error happens while connecting to the database.
*/
public void open () 
throws Exception, SQLException {
	String routine = CLASS + ".open";

	printStatusOrDebug(10, routine, "DMI.open() -----------------------");
	printStatusOrDebug(10, routine, "Checking for existing connection ...");

	if (__connected) {
		printStatusOrDebug(10, routine, "Already connected!  Throwing "
			+ "exception.");
		throw new SQLException ("Must close the first DMI connection "
			+ "before opening a new one.");	
	}

	printStatusOrDebug(10, routine, "         ... no connection found.");

	open (__system_login, __system_password );
	
	// Determine the database version (the derived class method will be
	// called if defined)...
	printStatusOrDebug(10, routine, "Determining database version ... "
		+ " (abstract method, outside of DMI class)");
	determineDatabaseVersion();

	printStatusOrDebug(10, routine, "Reading global data ..."
		+ " (abstract method, outside of DMI class)");
	readGlobalData();
	__lastQuery = null;
	__lastSQL = null;
	__lastSQLType = NONE;
	printStatusOrDebug(10, routine, "----------------------- DMI.open()");
}		  

/**
Opens the connection to the DMI with information that was previously set, 
and sets printStatus to the given value while open() (the other version of the
method, not this one) is being called, in order to print more debugging
information.  Once this method is done, __printStatus will be set to false.<p>
__printStatus is primarily used in printStatusOrDebug(), used internally for
debugging DMI connection open()s.
@param system_login the login to use for connecting to the database.
@param system_password the password to use for connecting to the database.
@param printStatus whether to print information to status in open().
@throws SQLException if the dmi is already connected.
@throws Exception if some other error happens while connecting to the database.
@deprecated (JTS - 2006-05-22) given the existence of the other open(boolean)
method, I don't think this one is necessary anymore.
*/
public void open(String system_login, String system_password, 
boolean printStatus) 
throws Exception {
	__printStatus = printStatus;
	try {
		open(system_login, system_password);
	}
	catch (SQLException se) {
		__printStatus = false;
		throw se;
	}
	catch (Exception e) {
		__printStatus = false;
		throw e;
	}
	__printStatus = false;
}

/**
Open a connection to the database with the specified system login and password.
All other connection parameters must have been set previously.
@param system_login System login.
@param system_password System password.
@throws SQLException thrown by DriverManger.getConnection() or 
Connection.setAutoCommit()
@throws Exception thrown when attempting to connectng to a database
for which no JDBC information is known.
*/
public void open ( String system_login, String system_password ) 
throws SQLException, Exception {
	String routine = CLASS + ".open(String, String)";
	printStatusOrDebug(10, routine, "Checking for existing connection ...");
	if (__connected) {
		printStatusOrDebug(10, routine, "Already connected!  Throwing "
			+ "exception.");
		throw new SQLException ("Must close the first DMI connection "
			+ "before opening a new one.");	
	}
	
	printStatusOrDebug(10, routine, "         ... no connection found.");

	String connUrl = "";
	int dl = 10;

	if ( __secure && (Message.isDebugOn || __printStatus)) {
		printStatusOrDebug(dl, routine, "SystemLogin: "
			+ system_login + ","
			+ "SystemPassword: " + system_password + ")");
	}

	// Set up the database-specific connection strings...

	if ( __jdbc_odbc ) {
		printStatusOrDebug(dl, routine, "Using JDBC ODBC connection.");
		// The URL is formed using several pieces of information...
		if (_database_engine == _DBENGINE_ACCESS ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_ACCESS'");
			// Always require an ODBC DSN (although this may be
			// a problem with some config files and software where
			// an MDB file is allowed for the database name).  This
			// case is therefore the same as if __jdbc_odbc = false.
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			connUrl = "jdbc:odbc:" + __database_name;
			Message.printStatus(2, routine,
				"Opening ODBC connection for Microsoft Access"
				+ " JDBC/ODBC and \"" + connUrl + "\"");
		} 
		else if (_database_engine == _DBENGINE_INFORMIX ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_INFORMIX'");
			// Use the free driver that comes from Informix...
			// If Informix is ever enabled, also need to add a
			// property to the configuration which is the "online
			// server", like "ol_hydrobase".  For now just dummy
			// in...
			String server = "ol_hydrobase";
			Class.forName("com.informix.jdbc.IfxDriver");
			connUrl = "jdbc:informix-sqli://" 
				+ __database_server + ":" 
				+ __port + "/" + __database_name
				+ ":INFORMIXSERVER=" + server;
			// Login and password specified below.
			// +";user=" + login +
			//";password=" + password;
			Message.printStatus(2, routine,
				"Opening ODBC connection for Informix using \""
				+ connUrl + "\"");
		}
		else if (_database_engine == _DBENGINE_MYSQL ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_MYSQL'");
			// Use the public domain driver that comes with MySQL...
			Class.forName("org.gjt.mm.mysql.Driver");
			connUrl = "jdbc:MySQL://" 
				+ __database_server + ":" 
				+ __port + "/" + __database_name;
			Message.printStatus(2, routine,
				"Opening ODBC connection for MySQL using \""
				+ connUrl + "\"" );
		}
		else if (_database_engine == _DBENGINE_POSTGRESQL ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_POSTGRESQL'");
			// Use the public domain driver that comes with 
			// PostgreSQL...
			Class.forName("org.postgresql.Driver");
			connUrl = "jdbc:postgresql://" 
				+ __database_server + ":" 
				+ __port + "/" + __database_name;
			Message.printStatus(2, routine,
				"Opening ODBC connection for PostgreSQL "
				+ "using \"" + connUrl + "\"" );
		}
		else if (_database_engine == _DBENGINE_SQLSERVER2000 ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_SQLSERVER2000'");
			// Use the driver distributed by Microsoft...
			Class.forName(
				"com.microsoft.jdbc.sqlserver.SQLServerDriver");
			connUrl = "jdbc:microsoft:sqlserver://"
				+ __database_server + ":"
				+ __port + ";DatabaseName=" + __database_name;
			Message.printStatus(2, routine,
				"Opening ODBC connection for SQLServer2000 "
				+ "using \"" + connUrl + "\"");
		} 
        else if (_database_engine == _DBENGINE_SQLSERVER_2005 ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_SQLSERVER 2005'");
			// Use the driver distributed by Microsoft...
            // note the slight differences in class name...
            // other than that, they behave the same?
			Class.forName(
				"com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connUrl = "jdbc:sqlserver://"
				+ __database_server + ":"
				+ __port + ";DatabaseName=" + __database_name;
			Message.printStatus(2, routine,
				"Opening ODBC connection for SQLServer "
				+ "using \"" + connUrl + "\"");
		} 
		else if (_database_engine == _DBENGINE_SQLSERVER7 ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_SQLSERVER7'");
			// This is the older UNA2000 driver...

			// NOTE:
			// Una2000 is the driver purchased by the state for
			// connecting to SQL Server 7 databases.  This driver
			// is not something RTi is free to distribute to 
			// customers.  Other customers may need a different
			// driver coded in here or -- better yet -- they should
			// switch to MS SQL Server 2000, which is freely 
			// available as MSDE.  

			Class.forName( "com.inet.tds.TdsDriver");
			connUrl = "jdbc:inetdae7:" 
				+ __database_server + ":"
				+ __port + "?database=" + __database_name;
			// This will turn off support for < SQL Server 7...
			// "?sql7=true";
			Message.printStatus(2, routine,
				"Opening ODBC connection for SQLServer7 "
				+ "using \"" + connUrl + "\"" );
		}
        else if (_database_engine == _DBENGINE_H2 ) {
			printStatusOrDebug(dl, routine, "Database engine is "
				+ "type 'DBENGINE_H2'");
			Class.forName( "org.h2.Driver");
			connUrl = "jdbc:h2:file:/" 
				+ __database_server;
			Message.printStatus(2, routine,
				"Opening JDBC connection for H2 "
				+ "using \"" + connUrl + "\"" );
		}
		else {	
			printStatusOrDebug(dl, routine, "Unknown database "
				+ "engine, throwing exception.");
			throw new Exception("Don't know what JDBC driver to "
				+ "use for database type " 
				+ __database_engine_String);
		}
	}
	else {	
		// The URL uses a standard JDBC ODBC connections, regardless of
		// the database engine (this should not normally be used for
		// Java appplications - or need to figure out how to use the
		// ODBC protocol URL knowing only the ODBC DSN for each engine).
		printStatusOrDebug(dl, routine, "Using default Java connection "
			+ "method.");
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		connUrl = "jdbc:odbc:" + __odbc_name;
		Message.printStatus (2, routine,
			"Opening ODBC connection using JDBC/ODBC and \""
			+ connUrl + "\"" );
	}
		
	if ( __secure ) {
		printStatusOrDebug(dl, routine, "Calling getConnection(" 
		+ connUrl + ", " + system_login + ", " + system_password
		+ ") via the Java DriverManager.");
	}
	else {	printStatusOrDebug(dl, routine, "Calling getConnection(" 
		+ connUrl + ", " + system_login + ", " + "password-not-shown" 
		+ ") via the Java DriverManager.");
	}
	__connection = DriverManager.getConnection(connUrl, system_login, 
		system_password );

	printStatusOrDebug(dl, routine, "Setting autoCommit to: " 
		+ __autoCommit);
	__connection.setAutoCommit(__autoCommit);
	
	printStatusOrDebug(dl, routine, "Connected!");
	__connected = true;
}

/**
A helper method called by the open() methods that determines whether output
needs to go to debug as normal, or should be forced to status level 2.
See open(boolean), which sets __printStatus. If __printStatus is true, 
debug will go to status level 2.
@param dl the level at which the message should be printed (for debug messages).
@param routine the routine that is printing the message.
@param message the message to be printed.
*/
private void printStatusOrDebug(int dl, String routine, String message) {
	if (__printStatus) {
		Message.printStatus(2, routine, message);
	}
	else {
		Message.printDebug(dl, routine, message);
	}
}

/**
Read global data from the database, that can be used throughout the DMI 
session.  This is called from the open() method.
SAMX - This is being evaluated.
REVISIT
*/
public abstract void readGlobalData () throws SQLException, Exception;

/**
Attempts to perform a rollback to cancel database changes since the start
of the last transaction.
@throws SQLException thrown by Connection.rollback() or 
Connection.setAutoCommit, or when the database is not connected
*/
public void rollback() throws SQLException {
// REVISIT (JTS - 2006-05-22)
// This code has not been tested or used in nearly 4 years.  Do not rely
// on this method without testing it first!
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.rollback()");
	}
	
	String routine = "DMI.rollback";
	int dl = 25;
	
	__inTransaction = false;
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "[method called]");
	}

	__connection.rollback();
	
	// since a rollback signifies the end of a transaction, set the
	// autoCommit setting back to being on
	setAutoCommit(true);
	__dirty = false;
	closeStatements();
}

/**
Sets the autoCommit setting for the database.  
@param autoCommitSetting a boolean telling what to set the autoCommit
setting to
@throws SQLException thrown by Connection.setAutoCommit(), or if the 
database is not connected
*/
public void setAutoCommit(boolean autoCommitSetting) throws SQLException {
// REVISIT (JTS - 2006-05-22)
// This code has not been tested or used in nearly 4 years.  Do not rely
// on this method without testing it first!
	// there's the likelihood that the setting of autoCommit will have
	// to be handled differently for certain database types.  This isn't
	// the case right now.
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.setAutoCommit()");
	}
	
	String routine = "DMI.setAutoCommit";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"autoCommit: " + autoCommitSetting);
	}

	__autoCommit = autoCommitSetting;

	if (__database_engine_String.equalsIgnoreCase("SOMETHING") ) {
		// REVISIT
		// handle this differently -- ie, does MS Access blow up
		// when trying to setAutoCommit?
	} 
	else {	
		__connection.setAutoCommit(autoCommitSetting);	
	}
}

/**
Sets whether SQL should be capitalized before being passed to the database.
@param capitalize whether SQL should be capitalized.
*/
public void setCapitalize(boolean capitalize) {
	__capitalize = capitalize;
}

/**
sets the connection used to interact with the database.  in this way, the
connection established with one dmi can be passed to a different one so 
that the second doesn't have to go through the connection process again.
@param c the connection to use for communicating with the database
*/
public void setConnection(Connection c) {
	__connection = c;
	if (c != null) {
		__connected = true;
	}
}

/**
Set the type of the database engine to which the DMI will connect.
Valid types are:<br>
<ul>
<li> Access</ul>
<li> Informix</ul>
<li> MySQL</ul>
<li> Oracle</ul>
<li> PostgreSQL</ul>
<li> SQL_Server (interpreted as SQLServer7) - should use SQLServer7</ul>
<li> SQLServer7 -- <b>SQLServer7 uses a the driver purchased by the state for
connecting to SQL Server 7 databases.  This driver is not something RTi is 
free to distribute to other customers, so they may need a different
driver or they should switch to MS SQL Server 2000, which is freely 
available as MSDE.</b>
</ul>
<li> SQLServer2000</ul>
</ul>
@param database_engine the type of database engine.
@exception Exception if the database engine is not recognized.
*/
public void setDatabaseEngine (String database_engine)
throws Exception {
	String routine = "DMI.setDatabaseEngine";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"database_engine: " + database_engine);
	}
	__database_engine_String = database_engine;
	if (__database_engine_String.equalsIgnoreCase("Access")) {
		__database_server = "Local";
		_database_engine = _DBENGINE_ACCESS;
	}
	else if (__database_engine_String.equalsIgnoreCase("Informix")) {
		// The port number for Informix is typically not changed...
		//__port = 1526;
		_database_engine = _DBENGINE_INFORMIX;
	}
	else if (__database_engine_String.equalsIgnoreCase("MySQL")) {
		_database_engine = _DBENGINE_MYSQL;
	}
	else if (__database_engine_String.equalsIgnoreCase("Oracle")) {
		_database_engine = _DBENGINE_ORACLE;
	}
	else if (__database_engine_String.equalsIgnoreCase("PostgreSQL")) {
		_database_engine = _DBENGINE_POSTGRESQL;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQL_Server") ||
		__database_engine_String.equalsIgnoreCase("SQLServer7")) {
		// SQL_Server is being phased out in favor of SQLServer7.
		// The port number for SQL server is typically not changed...
		//__port = 1433;
		_database_engine = _DBENGINE_SQLSERVER7;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQLServer2000")) {
		// The port number for SQL server is typically not changed...
		//__port = 1433;
		_database_engine = _DBENGINE_SQLSERVER2000;
	}
    else if (__database_engine_String.equalsIgnoreCase("H2")) {
		_database_engine = _DBENGINE_H2;
	}
	else {	throw new Exception("Trying to use unknown database engine: "
			+ __database_engine_String + " in DMI()");
	}
}

/**
Set the name of the database, which is used when a connection URL is
automatically defined and a pre-defined ODBC DSN is not used.
If an ODBC DSN is used, then the database connection uses the ODBC Data Source
Name (DSN) set with setODBCName().
@param database_name the name of the database
*/
public void setDatabaseName(String database_name) {
	if (Message.isDebugOn) {
		Message.printDebug(25, "DMI.setDatabaseName",
			"database_name: " + database_name);
	}
	__database_name = database_name;
	__jdbc_odbc = true;
}

/**
Set the database version.  This should be called from a derived DMI class
determineDatabaseVersion() method.
@param version Database version.  The standard
for the version number is XXXXXXDDDDDDDD where XXXXXXX is typically a 
database version (e.g., RiverTrak 020601) and DDDDDDDD is usually the 
build date (e.g., 20020625).
*/
public void setDatabaseVersion ( long version ) {
	__database_version = version;
}

/**
sets the value of __editable.  If __editable is true, no calls to dmiWrite
or dmiDelete will be executed.
@param editable value to set __editable to
*/
public void setEditable(boolean editable) {
	__editable = editable;
}

/**
Set the IP address or DNS-resolvable machine name of the database server.
Note that this can be set to "Local" if a local database is used (e.g., for
Access).
@param database_server the IP address or machine name of the database host.
*/
public void setDatabaseServer ( String database_server ) {
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, "DMI.setDatabaseServer",
			"Database server: \"" + database_server + "\"");
	}
	__database_server = database_server;
}

/**
Sets whether to print out the SQL string that caused an exception to be
thrown after a database command.  If true, the string will be printed at
Message.printStatus(2).  Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL after there is an error.
*/
public void setDumpSQLOnError(boolean dumpSQL) {
	__dumpSQLOnError = dumpSQL;
}

/**
Sets whether to print out the SQL string that caused an exception to be
thrown after a database command.  If true, the string will be printed
at Message.printStatus(2).  Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL after an error occurs.
*/
public void dumpSQLOnError(boolean dumpSQL) {
	setDumpSQLOnError(dumpSQL);
}

/**
Sets whether to print out the SQL string that will be executed prior to any
UPDATEs, INSERTS, SELECTs or DELETEs.  If true, the string will be printed at
Message.printStatus(2).  Defaults to false.  For use in debugging.
@param dumpSQL whether to dump the SQL prior to running it.
*/
public void setDumpSQLOnExecution(boolean dumpSQL) {
	__dumpSQLOnExecution = dumpSQL;
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
	__id = id;
}

/**
Sets the name of the connection.
@param inputName the name of the connection.
*/
public void setInputName(String inputName) {
	__inputName = inputName;
}

/**
Saves the last delete statement executed.
@param s a DMIDeleteStatement to be saved
*/
private void setLastStatement(DMIDeleteStatement s) {
	__lastSQL = (DMIStatement) s;
	__lastSQLType = DELETE;
}

/**
Saves the last select statement executed.  
@param s a DMISelectStatement to be saved
*/
private void setLastStatement(DMISelectStatement s) {
	__lastSQL = (DMIStatement) s;
	__lastSQLType = SELECT;
	__lastQuery = s;
}

/**
Saves the last write statement executed
@param s a DMIWriteStatement to be saved
*/
private void setLastStatement(DMIWriteStatement s) {
	__lastSQL = (DMIStatement) s;
	__lastSQLType = WRITE;
}

/**
Sets the name of the connection.
@param name the name of the connection.
@deprecated (2005-04-07)
*/
public void setName(String name) {
	__name = name;
}

/**
Set the ODBC DSN for the database, which is used when a predefined ODBC DSN is
used to make the connection.  If an ODBC DSN is not used, then the database
connection uses the database server set with setDatabaseServer() and database
name set with setDatabaseName().
@param odbc_name the ODBC DSN for the database (must be defined on the machine).
*/
public void setODBCName(String odbc_name) {
	if (Message.isDebugOn) {
		Message.printDebug(25, "DMI.setODBCName",
			"odbc_name: " + odbc_name);
	}
	__odbc_name = odbc_name;
	__jdbc_odbc = false;
}

/**
Sets the port of the database to connect to
@param port the value to set the port to
*/
public void setPort(int port) {
	String routine = "DMI.setPort";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine,
			"Port: " + port);
	}
	__port = port;
}

/**
Sets the secure flag
@param secure the value to set the secure flag to
*/
public void setSecure(boolean secure) {
	String routine = "DMI.setSecure";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"Secure: " + secure);
	}
	__secure = secure;
}

/** 
Set the system login name for the database connection.
@param login the system login name.
*/
public void setSystemLogin(String login) {
	String routine = "DMI.setSystemLogin";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"Login: '" + login + "'");
	}
	__system_login = login;
}

/**
Set the system password for the database connection.
@param pw the system password for the database connection.
*/
public void setSystemPassword(String pw) {
	String routine = "DMI.setSystemPassword";
	int dl = 25;
	
	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, 
			"Password: '" + pw + "'");
	}
	__system_password = pw;
}

/** 
Set the user login name for database use.  This information can be used for
record-based permissions.
@param login the user login name.
*/
public void setUserLogin ( String login) {
	__user_login = login;
}

/**
Set the user password for database use.  This information can be used for
record-based permissions.
@param password the user password.
*/
public void setUserPassword ( String password ) {
	__user_password = password;
}

/** 
Starts a transaction, first rolling back any changes that may have been 
made to the database since the start of the last transaction.
@throws SQLException thrown in startTransaction()
*/
public void startTransaction() throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.startTransaction()");
	}
	
	__dirty = false;
	startTransaction(ROLLBACK);

	// REVISIT (JTS - 2004-07-20)
	// nothing in here to actually START a transaction!  Other transactions
	// are either committed or rolled back, but where is one started?

	__inTransaction = true;
}

/** 
Starts a transaction, and either rolls back the current database changes
or commits them, depending on the argument to this method.
@param action specifies what to do when the transaction is started.  
values that can be passed in are <code>ROLLBACK</code> or <code>COMMIT
</code>, and they tell what to do before the transaction is started.
@throws SQLException thrown in setAutoCommit(), commit() or rollback(),
or when the database is not connected
*/
public void startTransaction(int action) throws SQLException {
	if (!__connected) {
		throw new SQLException ("Database not connected.  Cannot "
			+ "make call to DMI.startTransaction()");
	}
	
	String routine = "DMI.startTransaction";
	int dl = 25;

	// autoCommit and transaction do not work together.  autocommit
	// must be turned off for transactions to function, and so do that
	setAutoCommit(false);
	
	if (action == COMMIT) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, 
				routine,
				"COMMIT");
		}
		commit();
	}
	else if (action == ROLLBACK) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, 
				routine, 
				"ROLLBACK");
		}
		rollback();
	}
}

/**
Sets the dirty flag if autocommit is set to off.  otherwise, does nothing
*/
private void testAndSetDirty() {
	if (__autoCommit == false) {
		__dirty = true;
	}
}
	
/** 
Returns a string of useful information about the DMI
@return a string of useful information about the DMI
*/
public String toString() {
	String dmiDesc = "";

	if (!__secure) {
		dmiDesc = 
			"DMI Information:"
			+ "\n   System Login: " + __system_login 
			+ "\n   System Password: " + __system_password
			+ "\n   User Login: " + __user_login 
			+ "\n   User Password: " + __user_password
			+ "\n   Connecting to database '" + __database_name 
			+ "' of type '" + __database_engine_String + "'"
			+ "\n   at '" + __database_server + ":" + __port + "'"
			+ "\n   Current status is: ";
			
			if (__connected == true) {
				dmiDesc += "CONNECTED";
			} else {
				dmiDesc += "NOT CONNECTED";
			}
		
		dmiDesc +=
			"\n   autoCommit is currently: ";
	
			if (__autoCommit == true) {
				dmiDesc += "ON";
			} else {
				dmiDesc += "OFF";
			}
		
		dmiDesc += 
			"\n   The previously executed SQL statement was:"
			+ "\n   [" + getLastSQLString() + "]";
	} else {
		dmiDesc = 
			"DMI Information:"
			+ "\n   Connecting to database '" + __database_name 
			+ "' of type '" + __database_engine_String + "'"
			+ "\n   Current status is: ";
			
			if (__connected == true) {
				dmiDesc += "CONNECTED";
			} else {
				dmiDesc += "NOT CONNECTED";
			}
	}

	return dmiDesc;
}


/**
Sets the DMI to use the default port for the database engine that the DMI
was set up to connect to.  Only works currently for Informix, PostgreSQL, and
SQLServer variants.
*/
public void setDefaultPort() {
	if (__database_engine_String.equalsIgnoreCase("Access")) {
	}
	else if (__database_engine_String.equalsIgnoreCase("Informix")) {
		__port = 1526;
	}
	else if (__database_engine_String.equalsIgnoreCase("MySQL")) {
	}
	else if (__database_engine_String.equalsIgnoreCase("Oracle")) {
	}
	else if (__database_engine_String.equalsIgnoreCase("PostgreSQL")) {
		__port = 5432;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQL_Server") ||
		__database_engine_String.equalsIgnoreCase("SQLServer7")) {
		__port = 1433;
	}
	else if (__database_engine_String.equalsIgnoreCase("SQLServer2000")) {
		__port = 1433;
	}
	else {	
		//
	}
}

}
