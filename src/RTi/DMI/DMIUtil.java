// ----------------------------------------------------------------------------
// DMIUtil.java - static methods for use with the DMI package
//
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2002-10-25	Steven A. Malers, RTi	Initial version - don't want to keep
//					adding to DMI when static utility
//					methods may not always be used and would
//					add to the size of DMI.  Start with the
//					getAvailableOdbcDsn() method.
// 2002-12-24	SAM, RTi		* Move general code from the DMI class
//					  to streamline the DMI class.
//					* Fill out the formatDateTime() class
//					  to actually do something, using CDSS
//					  HBData.formatSQLDate() as a model.
//					* Make the specific isMissingXXX()
//					  methods private to encourage only the
//					  isMissing() methods to be used.
// 2003-03-05	J. Thomas Sapienza, RTi	Moved formatting code for where strings
//					and order clauses in here from 
//					the old HydroBaseDMI.
// 2003-03-08	SAM, RTi		Add createHTMLDataDictionary() method to
//					automatically create a data dictionary
//					from a database connection.
// 2003-03-31	JTS, RTi		Fixed bug in formatDateTime that was
//					resulting in the wrong output for the
//					given precision.
// 2003-04-21	JTS, RTi		Added test HTML-generating Data 
//					dictionary code.
// 2003-04-22	JTS, RTi		Added the initial duplicateTable() code.
// 2003-04-23	JTS, RTi		* Added removeTable()
//					* Cleaned up duplicateTable().
//					* Added code to the Data Dictionary 
//					  generator to read SQL Server table
//					  and column comments.
// 2003-07-31	JTS, RTi		Data dictionary code now only limits
//					its initial query to finding type
//					TABLE database objects.
// 2003-09-02	JTS, RTi		* Cleaned out some old debugging code.
//					* Updated javadocs for recently-added
//					  methods.
// 2003-10-23	JTS, RTi		Added isMissing() methods for primitive
//					containers (Double, Integer, etc).
// 2003-11-12	JTS, RTi		* Corrected error in formatDateTime that
//					  was (for Access databases) putting in
//					  'day' instead of the actual day and
//					  'year' instead of the actual year.
//					* Corrected error in formatDateTime 
//					  that was screwing up Informix dates.
// 2004-01-02	SAM, RTi		* Add getWhereClausesFromInputFilter().
// 2004-01-21	JTS, RTi		* Corrected bug in formatWhere clause
//					  caused by passing in a *
// 2004-06-22	JTS, RTi		* Added getExtremeRecord().
//					* Added getMaxRecord().
//					* Added getMinRecord().
// 2004-10-25	SAM, RTi		Change getWhereClausesFromInputFilter()
//					to check the operator ignoring case.
//					Some tools now use the operator string
//					in a persistent way that may not match
//					the case.
// 2004-11-18	JTS, RTi		* Corrected error in how table anchor
//					  links were generated as they were not
//					  working with IE.
// 					* Foreign links are now pulled out of
//					  the table and added to the HTML
//					  data dictionary.
// 					* Converted so that ResultSets are now
//					  closed with DMI.closeResultSet().
//					* Port number no longer appears on
//					  data dictionary.
//					* Data dictionary now has a legend for
//					  table colors.
//					* Data dictionary now has links from
//					  the reference table definitions to
//					  the contents, and vice versa.
// 2004-11-19	JTS, RTi		DateTimes can now be formatted for
//					PostgreSQL databases.
// 2005-01-11	JTS, RTi		* Where fields are now trimmed when 
//					  pulled out of filter panels.
//					* Method to create where clauses from
//					  input filters now does so via 
//					  a separate call which operates on 
//					  a single filter and its operator.
// 2005-04-25	JTS, RTi		Added formatDateTime() that allows 
//					leaving off the escape characters from
//					the date string.  This was done
//					primarily for use with stored 
//					procedure dates.
// 2005-11-16	JTS, RTi		Added resultSetHasColumn().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

// REVISIT (JTS - 2003-04-24)
// TO-DO:
// 1) Add an error section at the bottom of the data dictionary listing 
//    all the errors encountered (i.e., if in Access and unable to get the list
//    of foreign keys)
// 2) Get the stored procedures out of the database and list those
// 3) For reference tables, add something to split certain reference tables
//    into multiple lines (because they scroll off the page)
// 4) Should display table views

package RTi.DMI;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

import java.security.InvalidParameterException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
The DMIUtil class provides static methods to facilitate database interaction.
*/
public abstract class DMIUtil
{

///////////////////////////////////////////////////////////
//  Missing data value fields
//  - Use special values that are unlikely to occur for real data values
///////////////////////////////////////////////////////////

/**
Constant that represents a missing date (DateTime).
*/
public static final Date MISSING_DATE = null;

/** 
Constant that represents a missing double value.
*/
public static final double MISSING_DOUBLE = Double.NaN;

/**
Constant that represents the low end of a missing double value, when performing
comparisons where roundoff may have occurred.
*/
// TODO SAM 2015-08-06 Evaluate whether this can be permanently removed
//public static final double MISSING_DOUBLE_FLOOR = -999.1;

/**
Constant that represents the high end of a missing double value, when performing
comparisons where roundoff may have occurred.
*/
//TODO SAM 2015-08-06 Evaluate whether this can be permanently removed
//public static final double MISSING_DOUBLE_CEILING = -998.9;

/**
Constant that represents a missing float value.
*/
public static final float MISSING_FLOAT = Float.NaN;

/**
Constant that represents a missing int value.
*/
public static final int MISSING_INT = Integer.MIN_VALUE;

/**
Constant that represents a missing long value.
*/
public static final long MISSING_LONG = Long.MIN_VALUE;

/**
Constant that represents a missing string.
*/
public static final String MISSING_STRING = "";

/**
Checks a string for the presence of single quotes (').
@param s String to check
@return true if a single quote is detected, false otherwise.
*/
public static boolean checkSingleQuotes(String s) {
	int index = s.indexOf("'");
	if (index > -1) {
		return true;
	}
	return false;
}

/**
Creates a data dictionary.
@param dmi DMI instance for an opened database connection.
@param filename Complete name of the data dictionary HTML file (a file extension
is not added) to write.
@param referenceTables If not null, the contents of these tables will be listed
in a section of the data dictionary to illustrate possible values for lookup
fields.  Need to work on this to know what field to list first.
TODO (JTS - 2004-11-19) this method is VERY out of date, compared to the HTML data dictionary.  The
two methods should either be reconciled, or this one should be removed.
*/
public static void createDataDictionary ( DMI dmi, String filename, String [] referenceTables )
{	String routine = "DMIUtil.createDataDictionary";
	// Convert the messages to HTML code after getting the logic to work.
	// First get a list of tables and print out their information...

	Message.printStatus (2, routine, "Tables" );
	ResultSet rs = null;
	DatabaseMetaData metadata = null;
	try {
		metadata = dmi.getConnection().getMetaData();
		rs = metadata.getTables ( null, null, null, null );
		if ( rs == null ) {
			Message.printWarning ( 2, routine, "Error getting list of tables." );
		} 
	} 
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "Error getting list of tables." );
		rs = null;
	} 

	// Now get all the table names by looping through result set...

	boolean more = false;
	try {
		more = rs.next();
	}
	catch ( Exception e ) {
		more = false;
	}

	String	s;
	List table_names = new Vector();
	List table_remarks = new Vector();
	while ( more ) {

		try {
			// Table name...
			s = rs.getString(3);
			if ( !rs.wasNull() ) {
				table_names.add ( s.trim() );
				// Remarks...
				s = rs.getString(5);
				if ( !rs.wasNull() ) {
					table_remarks.add ( s.trim() );
					// Remarks...
				}
				else {	
					table_remarks.add ( "" );
				}
			}
			// Get the next item in the list...
			more = rs.next ();
		}
		catch ( Exception e ) {
			// Ignore for now...
			Message.printWarning ( 2, routine, e );
		}
	} 
	try {	
		DMI.closeResultSet(rs);
	}
	catch ( Exception e ) {
	}

	// Sort (need to add)...

	// Output tables...

	int size = table_names.size();
	for ( int i = 0; i < size; i++ ) {
		Message.printStatus ( 2, routine, table_names.get(i) + "," + table_remarks.get(i) );
	}

	// Next list the table contents...

	String table_name;
	String column_name;
	String column_type;
	int column_size;
	int column_num_digits;
	String column_nullable;
	for ( int i = 0; i < size; i++ ) {
		Message.printStatus ( 2, routine, "Table details" );

		table_name = (String)table_names.get(i);
		try {
			rs = metadata.getColumns (null, null, table_name, null);
			if ( rs == null ) {
				Message.printWarning ( 2, routine, "Error getting columns for \"" + table_name+"\" table." );
				DMI.closeResultSet(rs);
				continue;
			} 

			// Print the column information...

			more = rs.next();

			while ( more ) {
				// The column name is field 4...
				column_name = rs.getString(4);
				if ( !rs.wasNull() ) {
					column_name = column_name.trim();
				}
				else {
					column_name = "";
				}
				column_type = rs.getString(6);
				if ( !rs.wasNull() ) {
					column_type = column_type.trim();
				}
				else {
					column_type = "Unknown";
				}
				column_size = rs.getInt(7);
				column_num_digits = rs.getInt(9);
				column_nullable = rs.getString(18);
				if ( !rs.wasNull() ) {
					column_nullable =column_nullable.trim();
				}
				else {
					column_nullable = "Unknown";
				}
				Message.printStatus ( 2, routine, "\"" + table_name + "\": \"" +
				column_name + "\" " +
				column_type + " " +
				column_size + " " +
				column_num_digits + " " +
				column_nullable );
	
				more = rs.next ();
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Error getting columns for \"" + table_name+"\" table." );
		}
		try {	
			DMI.closeResultSet(rs);
		}
		catch ( Exception e ) {
		}
	}

	// List stored procedures...
	Message.printStatus ( 2, routine, "Stored procedures" );

	// Next list the contents of reference tables...
	Message.printStatus ( 2, routine, "Reference tables" );
}

/**
Creates a list of ERDiagram_Relationship objects to be used in an ER Diagram.
@param dmi an open and connected dmi object.  Must not be null.
@param notIncluded a list of the names of the tables for which to not make
relationships.  May be null.
@return a list of ERDiagram_Relationship objects for use in an ER Diagram.  
null is returned if there was an error creating the objects or reading from the database.
*/
public static List<ERDiagram_Relationship> createERDiagramRelationships(DMI dmi, List<String> notIncluded) {
	List<String> tableNames = getDatabaseTableNames(dmi, null, null, true, notIncluded);

	if (tableNames == null) {
		return null;
	}

	try {
		DatabaseMetaData metadata = dmi.getConnection().getMetaData();
		ResultSet rs = null;

		String startTable = null;
		String endTable = null;
		String startField = null;
		String endField = null;
	
		int size = tableNames.size();
		List rels = new Vector();
	
		for (int i = 0; i < size; i++) {
			rs = metadata.getExportedKeys(null, null, (String)tableNames.get(i));
	
			while (rs.next()) {		
				startTable = rs.getString(3);
				startField = rs.getString(4);
				endTable = rs.getString(7);
				endField = rs.getString(8);
				
				ERDiagram_Relationship rel = 
					new ERDiagram_Relationship( startTable, startField, endTable, endField);
				rels.add(rel);
			}
			DMI.closeResultSet(rs);
		}
		return rels;
	}
	catch (Exception e) {
		e.printStackTrace();
		return null;
	}	
}

/**
Creates a list of ERDiagram_Tables for use in an ERDiagram.
@param dmi an open and connected DMI object.  Must not be null.
@param tablesTableName the name of the table in the database that contains the
list of table names and ER Diagram information.  If null the coordinates for the ER Diagram will not be set.
@param tableField the name of the column in the tables table that contains the names of the tables.
@param erdXField the name of the column in the tables table that contains the X positions of the ERDiagram Tables.
@param erdYField the name of the column in the tables table that contains the Y positions of the ERDIagram Tables.
@param notIncluded a list of the names of the tables to not include in the ERDiagram.  May be null.
@return a list of ERDiagram_Table objects that can be used to build an 
ER Diagram.  null is returned if there was an error creating the tables or reading from the database.
*/
public static List<ERDiagram_Table> createERDiagramTables(DMI dmi,
	String tablesTableName, String tableField, String erdXField, String erdYField,
	List notIncluded)
{
	String routine = "DMIUtil.createERDiagramTables";
	String temp;
	DatabaseMetaData metadata = null;
	ResultSet rs = null;

	List<String> tableNames = getDatabaseTableNames(dmi, null, null, true, notIncluded);

	if (tableNames == null) {
		return null;
	}

	int size = tableNames.size();
	String tableName = null;
	Message.printStatus(2, routine, "Determining table details for ER Diagram");
	
	List<ERDiagram_Table> tables = new Vector();
	ERDiagram_Table table = null;

	try {
		metadata = dmi.getConnection().getMetaData();
	}
	catch (Exception e) {
		Message.printWarning(3,routine,e);
		return null;
	}
	
	for (int i = 0; i < size; i++) {
		tableName = tableNames.get(i);
		table = new ERDiagram_Table(tableName);
	
		try {	
			// First get a list of all the table columns that are in the Primary key.
			ResultSet primaryKeyRS = null;
			List<String> primaryKeyList = null;
			int primaryKeyListSize = 0;
			try {
				primaryKeyRS = metadata.getPrimaryKeys( null, null, tableName);
				primaryKeyList = new ArrayList();
				while (primaryKeyRS.next()) {
					primaryKeyList.add(primaryKeyRS.getString(4));	
				}
				primaryKeyListSize = primaryKeyList.size();
				DMI.closeResultSet(primaryKeyRS);
			}
			catch (Exception e) {
				// If an exception is thrown here, it is probably because the JDBC driver does not
				// support the "getPrimaryKeys" method.  
				// No problem, it will be treated as if there were no primary keys.
			}
			Message.printStatus(2,routine,"Table \"" + tableName + "\" has " + primaryKeyListSize + " primary keys");
			
			boolean key = false;
			List columns = new ArrayList();
			List<String> columnNames = new ArrayList<String>();

			// Next, get the actual column data for the current table.
			rs = metadata.getColumns(null, null, tableName, null);
			if (rs == null) {
				Message.printWarning(2, routine, "Error getting columns for \"" + tableName + "\" table.");
				DMI.closeResultSet(rs);
				continue;
			} 

			// Loop through each column and move all its important data into a list of list.  This data will
			// be run through at least twice, and to do that with a ResultSet would require several expensive opens and closes.
			String columnName = null;
			while (rs.next()) {
				key = false;
				List columnNameList = new ArrayList<String>();
			
				// Get the 'column name' and store it in list position 0
				columnName = rs.getString(4);
				if (columnName == null) {
					columnName = " ";
				}
				else {
					columnName= columnName.trim();
				}
				columnNameList.add(columnName);
				columnNames.add(columnName);

				// Get whether this is a primary key or not and store either "TRUE" (for it being a 
				// primary key) or "FALSE" in list position 1
				for (int j = 0; j < primaryKeyListSize; j++) {
					if (columnName.equals(primaryKeyList.get(j).trim())) {
						key = true;		
					}
				}				

				if (key) {
					columnNameList.add("TRUE");
				}
				else {
					columnNameList.add("FALSE");
				}

				// Get the 'column type' and store it in list position 2
				temp = rs.getString(6);
				if (temp == null) {
					temp = "Unknown";
				} 
				else {
					temp = temp.trim();
				}
				columnNameList.add(temp);

				// Get the 'column size' and store it in list position 3
				temp = rs.getString(7);
				columnNameList.add(temp);
				
				// Get the 'column num digits' and store it in list position 4
				temp = rs.getString(9);
				columnNameList.add(temp);

				// Get whether the column is nullable and store it in list position 5
				temp = rs.getString(18);
				if (temp == null) {
					temp = "Unknown";
				}
				else {
					temp = temp.trim();
				}
				columnNameList.add(temp);
				
				columns.add(columnNameList);			
			}

			// Next, an alphabetized list of the column names in the table will be compiled.
			// This will be used to display columns in the right sorting order.
			int numColumns = columnNames.size();
			int[] order = new int[numColumns];
			List[] sortedLists = new List[numColumns];
			for (int j = 0; j < numColumns; j++) {
				sortedLists[j] = (List)columns.get(order[j]);
			}
		
			String[] keyFields = new String[primaryKeyListSize];
			// Now that the sorted order of the column names (and the lists of data) is known, loop through
			// the data lists looking for columns that are in the Primary key.  They will be displayed in bold
			// face font with a yellow background.
			String field;
			String[] nonKeyFields = new String[(numColumns - primaryKeyListSize)];
			int count = 0;
			for (int j = 0; j < numColumns; j++) {
				List column = sortedLists[j];
				temp = null;

				temp = (String)column.get(1);

				if (temp.equals("TRUE")) {
					// display the column name
					temp = (String)column.get(0);
					field = temp + ": ";

					// display the column type
					temp = (String)column.get(2);
					if (temp.equalsIgnoreCase("real")) {
						temp = temp + "(" + (String)column.get(3) + ", " + (String)column.get(4);
					}
					else if (temp.equalsIgnoreCase("float")||
						(temp.equalsIgnoreCase("double"))||
						(temp.equalsIgnoreCase("smallint"))||
						(temp.equalsIgnoreCase("int"))||
						(temp.equalsIgnoreCase("integer"))||
						(temp.equalsIgnoreCase("counter"))||
						(temp.equalsIgnoreCase("datetime"))) {
					}
					else {
						temp = temp + "(" + (String)column.get(3) + ")";
					}					
					field += temp;
					keyFields[count++] = field;
				}
			}

			// Now do the same thing for the other fields, the non-primary key fields.  
			count = 0;
			for (int j = 0; j < numColumns; j++) {
				List column = sortedLists[j];
				temp = null;

				temp = (String)column.get(1);

				if (temp.equals("FALSE")) {
					// display the column name
					temp = (String)column.get(0);
					field = temp + ": ";

					// display the column type
					temp = (String)column.get(2);
					if (temp.equalsIgnoreCase("real")) {
						temp = temp + "(" + (String)column.get(3) + ", " + (String)column.get(4);
					}
					else if (temp.equalsIgnoreCase("float")||
						(temp.equalsIgnoreCase("double"))||
						(temp.equalsIgnoreCase("smallint"))||
						(temp.equalsIgnoreCase("int"))||
						(temp.equalsIgnoreCase("integer"))||
						(temp.equalsIgnoreCase("counter"))||
						(temp.equalsIgnoreCase("datetime"))) {
					}
					else {
						temp = temp + "(" + (String)column.get(3) + ")";
					}					
					field += temp;
					nonKeyFields[count++] = field;
				}
			}			
			table.setKeyFields(keyFields);
			table.setNonKeyFields(nonKeyFields);
			table.setVisible(true);
			if ( (tablesTableName != null) && !tablesTableName.isEmpty() ) {
				setTableXY(dmi, table, tablesTableName, tableField, erdXField, erdYField);
			}
			tables.add(table);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error determining column information for table: " + tableName);
			Message.printWarning(2, routine, e);
		}

		try {	
			DMI.closeResultSet(rs);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, e);
		}
	}
	return tables;	
}

/**
Given a result set, prints the type of each column to Status level 2.
@param rs the ResultSet to dump information for.
@throws SQLException if there is an error dumping information.
*/
public static void dumpResultSetTypes(ResultSet rs) 
throws SQLException {

	// set up the types vector 
	ResultSetMetaData rsmd = rs.getMetaData();	
	int columnCount = rsmd.getColumnCount();
	int colType = 0;
	String type = null;
	for (int i = 0; i < columnCount; i++) {
		colType = rsmd.getColumnType(i + 1);
		switch (colType) {
			case java.sql.Types.BIGINT:
				type = "bigint";
				break;
			case java.sql.Types.BIT:
				type = "bit";
				break;
			case java.sql.Types.CHAR:
				type = "char";
				break;
			case java.sql.Types.DATE:
				type = "date";
				break;
			case java.sql.Types.DECIMAL:
				type = "decimal";
				break;
			case java.sql.Types.DOUBLE:			
				type = "double";
				break;
			case java.sql.Types.FLOAT:
				type = "float";
				break;
			case java.sql.Types.INTEGER:
				type = "integer";
				break;
			case java.sql.Types.LONGVARBINARY:
				type = "longvarbinary";
				break;
			case java.sql.Types.LONGVARCHAR:
				type = "longvarchar";
				break;
			case java.sql.Types.NULL:
				type = "NULL";
				break;
			case java.sql.Types.NUMERIC:
				type = "numeric";
				break;
			case java.sql.Types.OTHER:
				type = "other";
				break;
			case java.sql.Types.REAL:
				type = "real";
				break;
			case java.sql.Types.SMALLINT:
				type = "smallint";
				break;
			case java.sql.Types.TIME:
				type = "time";
				break;
			case java.sql.Types.TIMESTAMP:
				type = "timestamp";
				break;
			case java.sql.Types.TINYINT:
				type = "tinyint";
				break;
			case java.sql.Types.VARBINARY:
				type = "varbinary";
				break;
			case java.sql.Types.VARCHAR:
				type = "varchar";
				break;	
		}		

		Message.printStatus(2, "", "Column " + (i + 1) + ": " + type);
	}
}

/**
Duplicates a table, its columns and primary key, and possibly the data (see the copyData parameter).
@param dmi an open DMI connection.
@param origTableName the name of the table to duplicate
@param newTableName the name of the table to create
@param copyData if set to true, the data from the original table will also
be copied into the new table.  <br>
<b> Note:</b> If copyData is set to true, a 
"SELECT * INTO newTable FROM origTable" query is run.  If set to false, the
column information is queried from the original table and a CREATE TABLE
SQL command is built.
@throws Exception if an error occurs
*/
public static void duplicateTable(DMI dmi, String origTableName, String newTableName, boolean copyData) 
throws Exception {
	String routine = "DMIUtil.duplicateTable";
	StringBuffer SQL = new StringBuffer();

	// Make sure not trying to create a table name that already 
	// exists in the database.  This check is done because this 
	// might not necessarily throw an Exception from the database.
	//
	// Example with SQL Server 2000:
	//
	// If the database owner has created a table called "Scenario",
	// it will be in the database catalog under the full name of
	// "dbo.Scenario".  If the user "guest" creates tries to duplicate
	// "Scenario", the new table will be placed in the database catalog as "guest.Scenario".
	//
	// Queries that don't reference the full table name, like:
	// "SELECT * FROM SCENARIO"
	// can still be executed, but it's unclear which table will actually be read.
	//
	// So it's better just to prevent duplicate table names completely.
	if (databaseHasTable(dmi, newTableName)) {
		throw new Exception ("Table '" + newTableName + "' already " + "exists in the database.");
	}

	if (copyData) {
		SQL.append("SELECT * INTO " + newTableName + " FROM " + origTableName);
		// Turn off capitalization before executing the query so that
		// table names are made case sensitive to how the name was passed in to the method.
		boolean caps = dmi.getCapitalize();
		if (caps) {
			dmi.setCapitalize(false);
		}
		dmi.dmiExecute(SQL.toString());
		if (caps) {
			dmi.setCapitalize(true);
		}		
		return;
	}

	ResultSet rs = null;
	DatabaseMetaData metadata = null;
	metadata = dmi.getConnection().getMetaData();

	// get the column data for the original table
	rs = metadata.getColumns(null, null, origTableName, null);
	if (rs == null) {
		throw new Exception ("Error getting columns for \"" + origTableName + "\" table.");
	} 

	boolean more = rs.next();
	if (more == false) {
		throw new Exception ("Unable to retrieve column information for table '" + origTableName + "'");
	}

	// get a list of all the table columns that are in the Primary key.
	ResultSet primaryKeysRS = null;
	List primaryKeysV = null;
	int primaryKeysSize = 0;
	primaryKeysRS = metadata.getPrimaryKeys(null, null, origTableName);
	primaryKeysV = new Vector();
	while (primaryKeysRS.next()) {
		primaryKeysV.add(primaryKeysRS.getString(4));	
	}
	primaryKeysSize = primaryKeysV.size();
	DMI.closeResultSet(primaryKeysRS);

	boolean key = false;
	String temp = null;
	List columns = new Vector();
	// Loop through each column and move all its important data into a Vector of Vectors.  This data will
	// be run through at least twice, and to do that with a ResultSet would require several expensive
	// opens and closes.
	while (more) {
		key = false;
		List column = new Vector();
		
		// Get the 'column name' and store it in Vector position 0
		temp = rs.getString(4);
		if (temp == null) {
			temp = " ";
		}
		else {
			temp = temp.trim();
		}
		column.add(temp);

		// Get whether this is a primary key or not and store either "TRUE" (for it being a 
		// primary key) or "FALSE" in list position 1
		for (int j = 0; j < primaryKeysSize; j++) {
			if (temp.trim().equals( ((String)primaryKeysV.get(j)).trim())) {
				key = true;		
			}
		}				
		if (key) {
			column.add("TRUE");
		}
		else {
			column.add("FALSE");
		}

		// Get the 'column type' and store it in list position 2
		temp = rs.getString(6);
		if (temp == null) {
		temp = "Unknown";
		} 
		else {
			temp = temp.trim();
		}
		column.add(temp);

		// Get the 'column size' and store it in list position 3
		temp = rs.getString(7);
		column.add(temp);
		
		// Get the 'column num digits' and store it in list position 4
		temp = rs.getString(9);
		column.add(temp);

		// Get whether the column is nullable and store it in list position 5
		temp = rs.getString(18);
		if (temp == null) {
			temp = "Unknown";
		}
		else {
			temp = temp.trim();
		}
		column.add(temp);
		
		// Get the column remarks and store them in list position 6
		temp = rs.getString(12);
		if (temp == null) {
			temp = "   ";
		} 
		else {
			temp = temp.trim();
		}
		column.add(temp);
			
		columns.add(column);
		more = rs.next();			
	}

	DMI.closeResultSet(rs);

	// Start forming the sql string.
	SQL.append("CREATE TABLE " + newTableName + "(\n");

	int numFields = columns.size();
	String comma = null; 
	for (int i = 0; i < numFields; i++) {
		comma = ",\n";
		if (i == (numFields - 1) && primaryKeysSize == 0) {
			comma = "\n)";
		}
		List column = (List)columns.get(i);
		String name = (String)column.get(0);
		String type = (String)column.get(2);
		if (type.equalsIgnoreCase("VARCHAR")) {
			type = type + " (" + (String)column.get(3) + ")";
		}
		String isNull = (String)column.get(5);
		if (isNull.equalsIgnoreCase("Unknown") || isNull.equalsIgnoreCase("No")) {
		    	isNull = "NOT NULL";
		}
		else {
			isNull = "";
		}

		SQL.append("   " + name + "\t" + type + "\t" + isNull + comma);
	}
	if (primaryKeysSize > 0) {
		SQL.append("PRIMARY KEY (");
		for (int i = 0; i < primaryKeysSize; i++) {
			if (i > 0) {
				SQL.append(", ");
			}
			SQL.append(primaryKeysV.get(i));
		}
		SQL.append("))");
	
	} 
	Message.printDebug(25, routine, "SQL: '" + SQL.toString() + "'");

	// Turn off capitalization before executing the query so that
	// table names are made case sensitive to how the name was passed in to the method.
	boolean caps = dmi.getCapitalize();
	if (caps) {
		dmi.setCapitalize(false);
	}
	dmi.dmiExecute(SQL.toString());
	if (caps) {
		dmi.setCapitalize(true);
	}
}

/**
Determine whether a database has a given stored procedure.  This can be used
to determine a database version or as a basic check for a stored procedure before executing it.
@return true if the procedure is in the database, false if not.
@param dmi DMI instance for an opened database connection.
@param procedureName the name of the procedure to test for.
@exception Exception if an error occurs
*/
public static boolean databaseHasStoredProcedure ( DMI dmi, String procedureName) 
throws Exception, SQLException {
	if (!dmi.connected() ) {
		throw new SQLException ("Database not connected, cannot call DMIUtil.databaseHasStoredProcedure()");
	}
	
	return databaseHasStoredProcedure ( dmi, dmi.getConnection().getMetaData(), procedureName);
}

/**
Determine whether a database has a given stored procedure.  This can be used
to determine a database version or as a basic check for a stored procedure before executing it.
@return true if the procedure is in the database, false if not
@param dmi DMI instance for a database.
@param metaData meta data to search for the procedure name
@param procedureName the name of the procedure
@exception Exception if an error occurs.
*/
public static boolean databaseHasStoredProcedure ( DMI dmi, DatabaseMetaData metaData, String procedureName)
throws Exception, SQLException {
	if (!dmi.connected()) {
		throw new SQLException ("Database not connected, cannot call DMIUtil.databaseHasStoredProcedure()");
	}	

	String message;
	String routine = "DMIUtil.databaseHasStoredProcedure";
	int dl = 25;
	
	String dbName = dmi.getDatabaseName();
	if ( Message.isDebugOn ) {
		Message.printDebug(dl, routine, "Checking if database \"" + dbName + "\" has stored procedure \"" +
		procedureName + "\"" );
	}
	ResultSet rs = null;
	try {
		rs = metaData.getProcedures( dbName, null, null);
	}
	catch ( Exception e ) {
		message = "Exception getting list of stored procedures from database \"" + dbName + "\" (" + e + ")";
        Message.printWarning(3, routine, message);
        Message.printWarning(3, routine, e);
        throw new Exception (message);	
	}
	if (rs == null) {
		message = "Null result set getting procedure names";
        Message.printWarning(3, routine, message);
        throw new Exception (message);
	}
	
	while (rs.next()) {
		String proc = rs.getString("PROCEDURE_NAME");
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "Procedure name to check = \"" + proc + "\"" );
		}
		// The SQL Server driver may return the procedure name with ";" and a number at the end.  If so
		// strip it off for further processing.
		int pos = proc.indexOf(";");
		if ( pos > 0 ) {
			proc = proc.substring(0,pos);
		}
		if (proc.equalsIgnoreCase(procedureName)) {
			// Using the following will close the related statement, which causes a problem on the 2nd call
			// to this method.
			//DMI.closeResultSet(rs);
			rs.close();
			if ( Message.isDebugOn ) {
				Message.printDebug(dl, routine, "Database \"" + dbName + "\" DOES have stored procedure \"" +
				procedureName + "\"" );
			}
			return true;
		}
	}
	// Using the following will close the related statement, which causes a problem on the 2nd call
	// to this method.
	//DMI.closeResultSet(rs);
	rs.close();
	if ( Message.isDebugOn ) {
		Message.printDebug(dl, routine, "Database \"" + dbName + "\" DOES NOT have stored procedure \"" +
		procedureName + "\"" );
	}
	return false;
}
	
/**
Determine whether a database has a table.
@return true if the specified table is in the database, false if not.
@param dmi DMI instance for a database connection.
@param tableName Name of table.
@exception Exception if there is an error getting database information.
*/
public static boolean databaseHasTable ( DMI dmi, String tableName )
throws Exception, SQLException	{	
	if (!dmi.connected()) {
		throw new SQLException ("Database not connected, cannot call DMIUtil.databaseHasTable()");
	}	
	
	return databaseHasTable(dmi.getConnection().getMetaData(), tableName );
}

/**
Determine whether a database has a table.
@return true if the specified table is in the database, false if not.
@param metadata DatabaseMetaData for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static boolean databaseHasTable ( DatabaseMetaData metadata, String tableName )
throws Exception {	
	String message, routine = "DMI.databaseHasTable";
	ResultSet rs = null;
	int dl = 5;

	// The following can be used to get a full list of columns...
	try {
	    rs = metadata.getTables ( null, null, null, null );
		if ( rs == null ) {
			message = "Error getting list of tables to find table \"" + tableName + "\".";
			Message.printWarning ( 2, routine, message );
			throw new Exception ( message );
		} 
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Database returned non-null table list." );
		} 
	} 
	catch ( Exception e ) {
		message = "Error getting list of tables to find table \"" + tableName + "\".";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	} 

	// Now check for the table by looping through result set...

	boolean more = rs.next();

	String s;
	int count = 0;
	while ( more ) {
		++count;

		// The table name is field 3...

		//if ( Message.isDebugOn ) {
		//	Message.printDebug ( dl, routine, "Checking table " + count );
		//}
		s = rs.getString(3);
		if ( !rs.wasNull() ) {
			s = s.trim();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Database has table \"" + s + "\"" );
			} 
			if ( s.equalsIgnoreCase(tableName) ) {
				rs.close();
				return true;
			} 
		}  
		else {
		    if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Database has null table." );
			}  
		} 

		// Get the next item in the list...

		more = rs.next ();
	} 
	DMI.closeResultSet(rs);
	return false;
}

/**
Determine whether a table in the database has a column.
@return true if the specified table includes the specified column, false if the column is not in the table.
@param dmi DMI instance for a database connection.
@param tableName Name of table.
@param columnName Name of column to check.
@exception if there is an error getting database information.
*/
public static boolean databaseTableHasColumn ( DMI dmi, String tableName, String columnName )
throws SQLException, Exception {	
	if (!dmi.connected()) {
		throw new SQLException ("Database not connected, cannot call DMIUtil.databaseTableHasColumn()");
	}	
    boolean columnExists = databaseTableHasColumn ( dmi.getConnection().getMetaData(), tableName, columnName);	
    if (!columnExists) {
        columnExists = databaseTableHasColumn( dmi.getConnection().getMetaData(),
                tableName.toUpperCase(), columnName.toUpperCase());
    }
    return columnExists;
}

/**
Determine whether a table in the database has a column.
@return true if the specified table includes the specified column, false if the column is not in the table.
@param metadata DatabaseMetaData for connection.
@param tableName Name of table.
@param columnName Name of column to check.
@exception if there is an error getting database information.
*/
public static boolean databaseTableHasColumn ( DatabaseMetaData metadata, String tableName, String columnName )
throws Exception, SQLException {	
	String message, routine = "DMI.databaseTableHasColumn";
	ResultSet rs = null;
	int dl = 5;

	// The following can be used to get a full list of columns...
	//try {	rs = metadata.getColumns ( null, null, table_name, null );}
	try {
	    rs = metadata.getColumns ( null, null, tableName, columnName);
		if ( rs == null ) {
			message = "Error getting columns for \"" + tableName+"\" table.";
			Message.printWarning ( 2, routine, message );
			throw new Exception ( message );
		} 
	} 
	catch ( Exception e ) {
		message = "Error getting database information for table \"" + tableName + "\".";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	} 

	// Now check for the columns by looping through result set...

	boolean more = rs.next();

	String	s;
	//Vector	column_names = new Vector ( 5, 5 );
	while ( more ) {
		// The column name is field 4...

		s = rs.getString(4);
		if ( !rs.wasNull() ) {
			s = s.trim();
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Table \"" + tableName + "\" has column \"" + s + "\"" );
			}
			if ( s.equalsIgnoreCase(columnName) ) {
				rs.close();
				return true;
			}
			//column_names.add ( s );
		} 
		else {
		    if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Table \"" + tableName + "\" has null column" );
			}
		}

		// Get the next item in the list...

		more = rs.next ();
	}

	DMI.closeResultSet(rs);
	return false;
}

// TODO SAM 2012-05-06 Need to evaluate whether functions should be processed more but complex SQL should
// probably just be constructed without the helper methods
/**
Escape an SQL field (column, table, or table with database and/or schema).
This is necessary to ensure that fields that are reserved names do not get interpreted as such.
For example, in SQL Server, [brackets] can be used around fields.
If the input string contains a "(", then it is assumed that the field is specified using a function
and no action is taken.
If the input string contains a ".", then it is assumed that the database, schema, and/or table name precedes the
field name and the string is split so that only the field name is escaped.
@param dmi the DMI being used
@param field the field being escaped, can be "Column", "Table.Column", "Schema.Table.Column" or "Database.Schema.Table.Column"
(or omit the column).
@return the escaped field
*/
public static String escapeField ( DMI dmi, String field )
{
    if ( (field.indexOf('(') >= 0) ||
        (!"".equals(dmi.getFieldLeftEscape())) && (field.indexOf(dmi.getFieldLeftEscape()) >= 0) ) {
        // Function or already escaped so probably too complicated to figure out here
        return field;
    }
    else {
        // Escape the fields
        String [] parts = field.split("\\."); // This is a regex; have to escape the '.'
        StringBuilder b = new StringBuilder();
        String le, re;
        for ( String part : parts ) {
            if ( part.startsWith(dmi.getFieldLeftEscape()) ) {
                // Already escaped so no need to do so again
                le = "";
                re = "";
            }
            else {
                le = dmi.getFieldLeftEscape();
                re = dmi.getFieldRightEscape();
            }
            if ( b.length() > 0 ) {
                b.append(".");
            }
            b.append ( le );
            b.append ( part );
            b.append ( re );
        }
        return b.toString();
    }
}

/**
Format a date/time string based on the database engine so that it can be used in an SQL statement.
@param dmi DMI instance form which to format date.
@param datetime a DateTime object containing a date.  The precision of this
DateTime object controls the formatting of the string.
@return a String representation of the DateTime, in the proper
form for use with the specified database engine.
*/
public static String formatDateTime ( DMI dmi, DateTime datetime )
throws Exception {
	return formatDateTime(dmi, datetime, true, -1);
}

/**
Format a date/time string based on the database engine so that it can be used in an SQL statement.
@param dmi DMI instance form which to format date.
@param datetime a DateTime object containing a date.  The precision of this
DateTime object controls the formatting of the string.
@param escapeChar if true, the date will be wrapped with an escape character
appropriate for the database engine.  
@return a String representation of the DateTime, in the proper
form for use with the specified database engine.
*/
public static String formatDateTime ( DMI dmi, DateTime datetime, boolean escapeChar )
throws Exception
{
    return formatDateTime ( dmi, datetime, escapeChar, -1 );
}

/**
Format a date/time string based on the database engine so that it can be used in an SQL statement.
@param dmi DMI instance form which to format date.
@param datetime a DateTime object containing a date.  The precision of this
DateTime object controls the formatting of the string.
@param escapeChar if true, the date will be wrapped with an escape character
appropriate for the database engine
@param DateTime.PRECISION_* indicating the format of the output - if specified as -1, use the precision
from the date/time
@return a String representation of the DateTime, in the proper
form for use with the specified database engine.
*/
public static String formatDateTime ( DMI dmi, DateTime datetime, boolean escapeChar, int precision )
throws Exception {
	String month = StringUtil.formatString(datetime.getMonth(),"%02d");
	String day = StringUtil.formatString(datetime.getDay(),"%02d");
	String year = StringUtil.formatString(datetime.getYear(),"%04d");
	String hour = null;
	String minute = null;
	String second = null;
	String databaseEngine = dmi.getDatabaseEngine();
	int databaseEngineType = dmi.getDatabaseEngineType();
	StringBuffer formatted = new StringBuffer();

	if ( precision == -1 ) {
	    // Default is to use precision from date/time
	    precision = datetime.getPrecision();
	}
	if ( precision <= DateTime.PRECISION_HOUR ) {
		hour = StringUtil.formatString( datetime.getHour(),"%02d");
	}
	if ( precision <= DateTime.PRECISION_MINUTE ) {
		minute = StringUtil.formatString( datetime.getMinute(),"%02d");
	}
	if ( precision <= DateTime.PRECISION_SECOND ) {
		second = StringUtil.formatString( datetime.getSecond(),"%02d");
	}

	// There are just enough differences between database engines to make
	// reusing code difficult.  Just handle separately for each engine.

	if ( databaseEngineType == DMI.DBENGINE_ACCESS ) {
		// TODO How to handle month or year precision?
		if (escapeChar) {
			formatted.append ( "#" );
		}
		formatted.append( "" + month );
		formatted.append ( "-" );
		formatted.append ( "" + day);
		formatted.append ( "-" );
		formatted.append ( "" + year);
		if ( precision <= DateTime.PRECISION_HOUR ) {
			formatted.append ( " " + hour );
		}
		if ( precision <= DateTime.PRECISION_MINUTE ) {
			formatted.append ( ":" + minute );
		}
		if ( precision <= DateTime.PRECISION_SECOND ) {
			formatted.append ( ":" + second );
		}
		if (escapeChar) {
			formatted.append ( "#" );
		}
		return formatted.toString();
	}
	else if ( databaseEngineType == DMI.DBENGINE_INFORMIX ) {
		// TODO Need to check the INFORMIX documentation for all the variations on this...
		if (escapeChar) {
			formatted.append ( "DATETIME (");
		}
		formatted.append ( "" + year );
		if ( precision <= DateTime.PRECISION_MONTH ) {
			formatted.append ( "-" + month );
		}
		if ( precision <= DateTime.PRECISION_DAY ) {
			formatted.append ( "-" + day );
		}
		if ( precision <= DateTime.PRECISION_HOUR ) {
			formatted.append ( " " + hour );
		}
		if ( precision <= DateTime.PRECISION_MINUTE ) {
			formatted.append ( ":" + minute );
		}
		if ( precision <= DateTime.PRECISION_SECOND ) {
			formatted.append ( ":" + second );
		}
		if (escapeChar) {
			formatted.append ( ")" );
		}
		return formatted.toString();
	}
	//else if ( databaseEngineType == DMI.DBENGINE_ORACLE ) {
	//}
	else if ( databaseEngineType == DMI.DBENGINE_SQLSERVER ) {
		if (escapeChar) {
			formatted.append ( "'" );
		}
		formatted.append ( "" + year );
		if ( precision <= DateTime.PRECISION_MONTH ) {
			formatted.append ( "-" + month );
		}
		if ( precision <= DateTime.PRECISION_DAY ) {
			formatted.append ( "-" + day );
		}
		if ( precision <= DateTime.PRECISION_HOUR ) {
			formatted.append ( " " + hour );
		}
		if ( precision <= DateTime.PRECISION_MINUTE ) {
			formatted.append ( ":" + minute );
		}
		if ( precision <= DateTime.PRECISION_SECOND ) {
			formatted.append ( ":" + second );
		}
		if (escapeChar) {
			formatted.append ( "'" );
		}
		return formatted.toString();
	}
	else if (databaseEngineType == DMI.DBENGINE_POSTGRESQL) {
		// PostgreSQL datetimes have to have at least year-month-day
		if (escapeChar) {
			formatted.append("'");
		}
		formatted.append("" + year);
		formatted.append("-" + month);
		formatted.append("-" + day);
		if (precision <= DateTime.PRECISION_HOUR) {
			formatted.append(" " + hour);
		}
		if (precision <= DateTime.PRECISION_MINUTE) {
			formatted.append(":" + minute);
		}
		if (precision <= DateTime.PRECISION_SECOND) {
			formatted.append(":" + second);
		}
		if (escapeChar) {
			formatted.append("'");
		}
		return formatted.toString();
	}
	else {
		throw new Exception("Unsupported database type (" + databaseEngine + ") in formatDateTime()");
	}
}

/**
Formats a where clause given the field side (in the whereString) and the 
is side (in the isString), and the kind of value that should be stored in the isString(type).
@param whereString field name (checks are not performed on this)
@param isString user-specified search criteria (checks are performed on this)
@param type the expected isString type (e.g., STRING, INT, ... etc)
@return a formatted where String if checks are satisfied, or null if an
exception is thrown or 'NONE' if the format is not to be added as a where clause.
@throws Exception if an error occurs farther down the stack
*/
public static String formatWhere(String whereString, String isString, int type) 
throws Exception {
	// initialize variables
	String formatString = null;

	// trim where and is Strings
	whereString = whereString.trim();
	isString = isString.trim();
	
	// check first for "is null" and "is not null" as the isString
	if ( isString.equalsIgnoreCase("is null") || isString.equalsIgnoreCase("is not null")) {
		return whereString + " " + isString;
	}

	// check the isString and format an Integer type where clause
	if ( type == StringUtil.TYPE_INTEGER || type == StringUtil.TYPE_DOUBLE ||
		type == StringUtil.TYPE_FLOAT) {
		formatString = formatWhereNumber(whereString, isString, type);
	} 
	else if (type == StringUtil.TYPE_STRING) {
		formatString = formatWhereString(whereString, isString);
	} 

	return formatString;
}

/**
Determines if the numString is the expected type.
@param type the type that numString should be
@param numString the string to check 
@return formatte where string 
*/
private static String formatWhereCheckNumber(int type, String numString) {
	String formatString = null;
	
	// Determine if the numString is of the expected type. IF not, throw an Exception.
	if (type == StringUtil.TYPE_INTEGER) {
		Integer isInteger = new Integer(numString);
		formatString = isInteger.toString();		
	}
	else if (type == StringUtil.TYPE_DOUBLE || type == StringUtil.TYPE_FLOAT) {
		Double isDouble = new Double(numString);
		formatString = isDouble.toString();		
	}
	return formatString;
}

/**
Formats the integer, double and float type where clauses.
@param whereString field name (checks are not performed on this)
@param isString user-specified search criteria (checks are performed on this)
@param type one of INT, DOUBLE or FLOAT (static values defined in this class)
@return a formatted where string if the checks are satisfied, null if an 
exception is thrown, or 'NONE' if the format is not to be added as a where clause.
TODO (JTS 2003-03-04) Maybe rework so instead of returning null, returns an exception?
*/
private static String formatWhereNumber(String whereString, String isString, int type)
throws Exception {
	if (isString == null) {
		return "NONE";
	}

	// first check if this field is being queried for everything ('*' is
	// treated, for RTi's purposes, as a valid numeric query).  If so,
	// just return "NONE" so that this isn't added as a where clause.
	if (isString.trim().equals("*")) {
		return "NONE";
	}

	String function = "DMIUtil.formatWhereNumber";
	String formatString = null;
	String firstOperator = null;		
	String secondOperator = null;	
	String isNumber = null;
	String numberString = "";
	String message = "";
	String betweenForNumber = "";
	String exampleMessage = "";

	if (type == StringUtil.TYPE_INTEGER) {
		message = " is not a valid search criteria. Examples are:";
		exampleMessage = "\nBETWEEN 500 AND 550"
				+ "\n= 550"
				+ "\n<= 550"
				+ "\nis null"
				+ "\nis not null"		
				+ "\n* (returns all records)";
		message = message + exampleMessage;
		betweenForNumber = "BETWEEN 12 AND 56";
	}
	else if (type == StringUtil.TYPE_DOUBLE ||
		 type == StringUtil.TYPE_FLOAT) {
		message = " is not a valid search criteria. Examples are";
		exampleMessage = "\nBETWEEN 12.34 AND 56.78"
				+ "\n= 1234.56"
				+ "\n<= 1345.56"
				+ "\nis null"
				+ "\nis not null"
				+ "\n* (returns all records)";
		message = message + exampleMessage;
		betweenForNumber = "BETWEEN 12.34 AND 56.78";
	}

	// isString cannot be empty. The user must supply a search criteria.
	if (isString.length() == 0) {
		Message.printWarning(2, function, "You must select an \"Is\""
			+ " query criteria. Examples are:" + exampleMessage);
		return null;
	}
	
	// check for BETWEEN searches
	if (isString.startsWith("BETWEEN")) {
		List v = StringUtil.breakStringList(isString, " ", 0);

		if (((String)v.get(0)).equalsIgnoreCase("BETWEEN")) {
			// If the query is using BETWEEN searches, then it MUST
			// adhere to the following format:
			// elementAt(0) = "BETWEEN"
			// elementAt(1) = Integer or Double
			// elementAt(2) = "AND"
			// elementAt(3) = Integer or Double
			// any other BETWEEN format is NOT accepted
			// try will catch Vector out of bounds
			try {	
				// try will catch number format for the first value
				try {
					formatWhereCheckNumber(type,(String)v.get(1));
				}
				catch(Exception e5) {
					Message.printWarning(2, function, (String)v.get(1)+ message);
					return null;
				}

				// determine if AND is present
				try {
					String andString = (String)v.get(2);
					if (!andString.equalsIgnoreCase("AND")) {
						Message.printWarning(2,function,
							"Missing 'AND' in the search criteria."
							+ "\nBETWEEN searches must be specified as follows:\n" + betweenForNumber);
						return null;
					}
				}
				catch(IndexOutOfBoundsException e) {
					Message.printWarning(2, function, 
						"Missing 'AND' in the search criteria.\nBETWEEN searches "
						+ "must be specified as follows:\n" + betweenForNumber);
					return null;
				}

				// try will catch number format for the second value
				try {
					formatWhereCheckNumber(type, (String)v.get(3));
				}
				catch(Exception e6) {
					Message.printWarning(2, function, (String)v.get(3)+message);
					return null;
				}		
				
				// Make sure that no more than 4 elements are present in the list
				if (v.size() > 4) {
					Message.printWarning(2, function, 
						"Too many terms specified in the search criteria."
						+ "\nBETWEEN searches must be specified as follows:"
						+ "\n" + betweenForNumber);	
				}

				formatString = " " + (String)v.get(0)
						+ " " + (String)v.get(1)
						+ " " + (String)v.get(2)
						+ " " + (String)v.get(3);

				return whereString + formatString;
			}
			catch(IndexOutOfBoundsException e4) {
				Message.printWarning(2, function, 
					"BETWEEN searches must be specified as follows:" 
					+ "\n" + betweenForNumber);
				return null;
			}
		}
	} 

	// if the isString can successfully be decoded into a number
	// then no operators where provided. Default to placing an '=' 
	// in front of the number.
	try  {
		isNumber = formatWhereCheckNumber(type, isString);
		formatString = " = " + isNumber.toString();		
	}

	// could not successfully decode the isString into the 
	// expected number type. check to see if an operator(s)or 
	// other characters prevented this.
	catch(Exception e1) {
		// this loop will build a numberString without the 
		// operators so that we can decode the remaining 
		// characters to ensure that a valid number type exist. 
		// The operator(s)are concatenated with the numberString 
		// if all the checks are passed.
		int isLength = isString.length();
		for (int currIndex = 0; currIndex < isLength; currIndex++) {
			String currChar = String.valueOf(isString.charAt(currIndex)).trim();

			// Check the first character which MUST be an operator. If not, throw an exception.
			if (currIndex == 0) {
				// if the first character is not '=', 
				// '<', or '>' then throw an exception
				if ( !(currChar.equals("=")) && !(currChar.equals("<")) && !(currChar.equals(">"))) {
					try  {
						formatWhereCheckNumber(type, isString);
					}
					catch(Exception e2) {
						Message.printWarning(2, function, isString + message);
						return null;
					}
				}
				else {
					firstOperator = currChar;
				}
			}
			// Check the second character which CAN be an operator if the first operator was '<' or '>'
			else if (currIndex == 1) {
				// determine if a second operator exist
				if ( firstOperator.equals("<") || firstOperator.equals(">")) {
					if (currChar.equals("=")) {
						secondOperator = currChar;
					}					
					// build the number String
					else {
						numberString = currChar;
					}
				} 
				// build the number String
				else {
					numberString = currChar;
				}
			}
			// build the number String
			else {
				numberString += currChar;
			}
		} 

		// decode the numberString to determine if it is the expected number type
		try  {
			isNumber = formatWhereCheckNumber(type, numberString);
			// numberString was successfully decoded.  Build the concatenated formatString
			if (secondOperator != null) {			
				formatString = " " + firstOperator + secondOperator + " " + isNumber.toString();		
			}
			else {
				formatString = " " + firstOperator + " " + isNumber.toString();		
			}
		}
		// Could not successfully decode the numberString.
		// issue a warning.
		catch(Exception e3) {
			Message.printWarning(2, function, isString + message);
			return null;
		}
	}

	return whereString + formatString;
}

/**
Formats the String type where clauses.
@param whereString field name (checks are not done on this)
@param isString user-specified search criteria (checks are performed on this)
@return formatted where String if checks are satisfied, null otherwise.
TODO (JTS 2003-03-04)  Maybe throw an exception if the checks aren't satisfied?
*/
private static String formatWhereString(String whereString, String isString) {
	// initialize variables
	String function = "DMIUtil.formatWhereString()";
	String formatString = null;
	String likeString = null;
	boolean foundLike = false;
	String remainingString = null;

	String exampleMessage = "\nLike COLORADO"
		+ "\n= COLORADO"
		+ "\nis null"
		+ "\nis not null"		
		+ "\n*(returns all records)"
		+ "\n\nNote:  Strings are converted to uppercase "
		+ "for queries.";

	// IsString cannot be empty. The user must supply a search criteria.
	if (isString.length() == 0) {
		Message.printWarning(2, function, "You must select an \"Is\""
			+ " query criteria. Examples are:" + exampleMessage);
		return null;
	}
		
	// Replace wild cards using * with the database's wildcard character.
	String replacedString = isString;
	try {		
		// char c = DMI._wildcard.charAt(0);
		replacedString = replacedString.replace('*', '%');
	}
	catch (Exception e) {
		// do nothing, _wildcard's length was < 1
		e.printStackTrace();
	}

	// check for single quotes. if present issue warning and return null.
	int isLength = isString.length();
	if (checkSingleQuotes(isString)) {
		Message.printWarning(2, function, "Single quotes are not permitted in queries. ");
		return null;
	}

	// If the isString is a '*' or '%' then return a NONE so that this will
	// not be added as a where clause. Issue a warning that ALL records will
	// be returned.
        //if (isString.equals("*") || isString.equals(DMI._wildcard)) {
//        if (isString.equals("*") || isString.equals("%")) {
//		return "NONE";
//	}
	
	// Determine if the replacedString begins with an '=' or 'Like' if not,
	// we will supply the 'Like' syntax. recall that the replacedString
	// has been trimmed so it should begin with one of the following.
	isLength = isString.length();
	if (replacedString.substring(0, 1).equalsIgnoreCase("=")) {
		likeString = "=";
		remainingString = replacedString.substring(1).trim();
		foundLike = true;
	}
	else if (isLength > 3) {
		if (replacedString.substring(0, 4).equalsIgnoreCase("like")) {
			likeString = "LIKE";
			remainingString = replacedString.substring(4).trim();
			foundLike = true;
		}
	}
	// user did not supply 'Like' or '=' therefore default to 'Like'
	if (!(foundLike)) {
		likeString = "LIKE";
		remainingString = replacedString.substring(0).trim();
	}
	
	// Check to ensure that the remaining String begins and ends with '
	// add the missing ' at the beginning of remainingString
	if (!(remainingString.startsWith("'"))) {
		remainingString = "'" + remainingString;
	}
	// add the missing ' at the end of remainingString
	if (!(remainingString.endsWith("'"))) {
		remainingString = remainingString + "'";
	}

	// Loop over the remainingString to provide internal checks within the
	// quotes
	isLength = remainingString.length();

	formatString = " " + likeString + " " + remainingString;
	return whereString + formatString;
}

/**
Constructs a concatenated String with AND inserted at the appropriate locations.
@param and Vector of Strings in which to construct an AND clause
@return returns a String with AND inserted at the appropriate locations, null if "and" is null
*/
public static String getAndClause(List and) {
        if (and == null) {
                return null;
        }

        int size = and.size();
        String andString = "";

        for (int i = 0; i < size; i++) {
            if ((i != (size - 1)) && (size != 1)) {
                andString += and.get(i)+ " AND " ;
            }
            else {
                andString += and.get(i);
            }
        }
        return "(" + andString + ")";         
}

/**
Returns all the kinds of databases a DMI can connect to.
@return a list of all the kinds of databases a DMI can connect to.
*/
public static List getAllDatabaseTypes() {
	return DMI.getAllDatabaseTypes();
}

/**
Return the list of columns for a table.
@return the list of columns for a table.
@param dmi DMI for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static List<String> getTableColumns ( DMI dmi, String tableName )
throws Exception, SQLException
{
    return getTableColumns ( dmi.getDatabaseMetaData(), tableName );
}

/**
Return the list of column names for a table.
@return the list of columns for a table.
@param metadata DatabaseMetaData for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static List<String> getTableColumns ( DatabaseMetaData metadata, String tableName )
throws Exception, SQLException {    
    String message, routine = "DMI.getTableColumns";
    ResultSet rs = null;
    int dl = 5;

    // The following can be used to get a full list of columns...
    try {
        rs = metadata.getColumns ( null, null, tableName, null );
        if ( rs == null ) {
            message = "Error getting columns for \"" + tableName+"\" table.";
            Message.printWarning ( 2, routine, message );
            throw new Exception ( message );
        } 
    } 
    catch ( Exception e ) {
        message = "Error getting database information for table \"" + tableName + "\".";
        Message.printWarning ( 2, routine, message );
        throw new Exception ( message );
    }
    
    String columnName;
    List<String> columnNames = new Vector();
    while ( rs.next() ) {
        try {   
            // Column name...
            columnName = rs.getString(4);
            if (!rs.wasNull()) {
                columnNames.add(columnName.trim());
            }
        }
        catch (Exception e) {
            // continue getting the list of table names, but report the error.
            Message.printWarning(3, routine, e);
        }
    } 
    try {   
        DMI.closeResultSet(rs);
    }
    catch (Exception e) {
        Message.printWarning(2, routine, e);
    }
    return columnNames;
}

/**
Return the foreign table and column name corresponding to table and column that have a foreign key defined.
@return the foreign table and column name corresponding to table and column that have a foreign key defined.
@param dmi DMI for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static String [] getTableForeignKeyTableAndColumn ( DMI dmi, String tableName, String columnName )
throws Exception, SQLException
{
    return getTableForeignKeyTableAndColumn ( dmi.getDatabaseMetaData(), tableName, columnName );
}

/**
Return the list of columns that are a primary key for a table.
@return the list of columns that are a primary key for a table.
@param metadata DatabaseMetaData for connection
@param tableName name of table that has a foreign key to another table
@param columnName name of column in tableName that is the foreign key to another table
@exception if there is an error getting database information.
*/
public static String [] getTableForeignKeyTableAndColumn ( DatabaseMetaData metadata,
    String tableName, String columnName )
throws Exception, SQLException {    
    String message, routine = "DMIUtil.getTableForeignKeyTableAndColumn";
    ResultSet rs = null;

    try {
        rs = metadata.getImportedKeys( null, null, tableName);
        //rs = metadata.getExportedKeys( null, null, tableName);
        if ( rs == null ) {
            message = "Error getting foreign keys for \"" + tableName + "\" table.";
            Message.printWarning ( 2, routine, message );
            throw new Exception ( message );
        } 
    } 
    catch ( Exception e ) {
        message = "Error getting foreign keys for table \"" + tableName + "\".";
        Message.printWarning ( 2, routine, message );
        throw new Exception ( message );
    }
    
    String mainColumn = null;
    //String mainTable = null;
    String foreignTable = null;
    String foreignColumn = null;
    String [] data = null;
    while ( rs.next() ) {
        try {
            // Foreign table...
            foreignTable = rs.getString(3);
            foreignColumn = rs.getString(4);
            // The table initiating the request (the ones that has keys pointing to the foreign table)...
            //mainTable = rs.getString(7);
            mainColumn = rs.getString(8);
            if ( columnName.equalsIgnoreCase(mainColumn) ) {
                // Found the matching requested column
                data = new String[2];
                data[0] = foreignTable;
                data[1] = foreignColumn;
                break;
            }
            //Message.printStatus(2, routine, "For requested table \"" + mainTable + "\" requested column \"" + mainColumn +
            //    "\", foreignTable=\"" + foreignTable + "\" foreignColumn=\"" + foreignColumn + "\"");
        }
        catch (Exception e) {
            // continue getting the list of table names, but report the error.
            Message.printWarning(3, routine, e);
        }
    } 
    try {   
        DMI.closeResultSet(rs);
    }
    catch (Exception e) {
        Message.printWarning(2, routine, e);
    }
    return data;
}

/**
Return the table column names that are for primary keys.
@return the table column names that are for primary keys.
@param dmi DMI for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static List<String> getTablePrimaryKeyColumns ( DMI dmi, String tableName )
throws Exception, SQLException
{
    return getTablePrimaryKeyColumns ( dmi.getDatabaseMetaData(), tableName );
}

/**
Return the list of columns that are a primary key for a table.
@return the list of columns that are a primary key for a table.
@param metadata DatabaseMetaData for connection.
@param tableName Name of table.
@exception if there is an error getting database information.
*/
public static List<String> getTablePrimaryKeyColumns ( DatabaseMetaData metadata, String tableName )
throws Exception, SQLException {    
    String message, routine = "DMI.getTablePrimaryKeyColumns";
    ResultSet rs = null;

    try {
        rs = metadata.getPrimaryKeys( null, null, tableName);
        if ( rs == null ) {
            message = "Error getting primary keys for \"" + tableName + "\" table.";
            Message.printWarning ( 2, routine, message );
            throw new Exception ( message );
        } 
    } 
    catch ( Exception e ) {
        message = "Error getting primary keys for table \"" + tableName + "\".";
        Message.printWarning ( 2, routine, message );
        throw new Exception ( message );
    }
    
    String columnName;
    List<String> columnNames = new Vector();
    while ( rs.next() ) {
        try {   
            // Column name...
            columnName = rs.getString(4);
            if (!rs.wasNull()) {
                columnNames.add(columnName.trim());
            }
        }
        catch (Exception e) {
            // continue getting the list of table names, but report the error.
            Message.printWarning(3, routine, e);
        }
    } 
    try {   
        DMI.closeResultSet(rs);
    }
    catch (Exception e) {
        Message.printWarning(2, routine, e);
    }
    return columnNames;
}

/**
Returns the catalog of database names in the connection, excluding known system databases.
For example, if the JDBC connection is with a SQL Server "master" database, this will return the databases
under that instance.
@param dmi an open, connected and not-null DMI connection to a database.
@param removeSystemDatabases if true, remove the known system databases (this may take some work to keep up to date).
@param notIncluded a list of all the database names that should not be included
in the final list of database names.
@return the list of databases names in the dmi's database instance.  null
is returned if there was an error reading from the database.
*/
public static List<String> getDatabaseCatalogNames(DMI dmi, boolean removeSystemDatabases, List<String> notIncluded)
{
    String routine = "getDatabaseCatalogNames";
    // Get the name of the data.  If the name is null, it's most likely
    // because the connection is going through ODBC, in which case the 
    // name of the ODBC source will be used.
    String dbName = dmi.getDatabaseName();
    if (dbName == null) {
        dbName = dmi.getODBCName();
    }

    Message.printStatus(2, routine, "Getting catalog of databases");
    ResultSet rs = null;
    DatabaseMetaData metadata = null;
    try {   
        metadata = dmi.getConnection().getMetaData();
        rs = metadata.getCatalogs();
        if (rs == null) {
            Message.printWarning(2, routine, "Error getting catalog of databases.");
            return null;
        } 
    } 
    catch (Exception e) {
        Message.printWarning(2, routine, "Error getting catalog of databases.");
        Message.printWarning(2, routine, e);
        return null;
    } 

    String catName;
    List<String> dbNames = new Vector<String>();
    try {
        while ( rs.next() ) {
            try {   
                // Table name...
                catName = rs.getString(1);
                if (rs.wasNull()) {
                    catName = null;
                }
                if ( catName != null ) {
                    dbNames.add(catName.trim());
                }
            }
            catch (Exception e) {
                // continue getting the list of table names, but report the error.
                Message.printWarning(2, routine, e);
            }
        }
    }
    catch ( Exception e ) {
        // TODO SAM 2012-01-31 probably should not catch this but has been done historically
    }
    try {   
        DMI.closeResultSet(rs);
    }
    catch (Exception e) {
        Message.printWarning(2, routine, e);
    }

    // Sort the list of database names in ascending order, ignoring case.
    dbNames = StringUtil.sortStringList(dbNames, StringUtil.SORT_ASCENDING, null, false, true);
   
    // Additionally remove all the databases that were in the notIncluded parameter passed in to this method.
    if (notIncluded != null) {
        Message.printStatus(2, routine, "Removing requested databases from database list");
        for ( String s : notIncluded ) {
            StringUtil.removeMatching(dbNames,s,true);
        }
    }
    return dbNames;
}

/**
Returns the list of procedure names in the database, excluding known system procedures.
@param dmi an open, connected and not-null DMI connection to a database.
@param removeSystemProcedures if true, remove the known system procedures
(this may take some work to keep up to date).
@param notIncluded a list of all the procedure names that should not be included
in the final list of procedure names.
@return the list of procedure names in the dmi's database.  null
is returned if there was an error reading from the database.
*/
public static List<String> getDatabaseProcedureNames(DMI dmi, boolean removeSystemProcedures,
    List<String> notIncluded)
throws SQLException
{   String routine = "getDatabaseProcedureNames", message;
    int dl = 25;
    // Get the name of the data.  If the name is null, it's most likely
    // because the connection is going through ODBC, in which case the 
    // name of the ODBC source will be used.
    String dbName = dmi.getDatabaseName();
    if (dbName == null) {
        dbName = dmi.getODBCName();
    }

    Message.printStatus(2, routine, "Getting list of procedures");
    ResultSet rs = null;
    DatabaseMetaData metadata = null;
    try {   
        metadata = dmi.getConnection().getMetaData();
        rs = metadata.getProcedures( dbName, null, null);
        if (rs == null) {
            Message.printWarning(2, routine, "Error getting list of procedures.  Aborting");
            return null;
        } 
    } 
    catch (Exception e) {
        Message.printWarning(2, routine, "Error getting list of procedures.  Aborting.");
        Message.printWarning(2, routine, e);
        return null;
    }
    if (rs == null) {
        message = "Null result set getting procedure names";
        Message.printWarning(3, routine, message);
        throw new RuntimeException (message);
    }
    
    List<String> procNames = new Vector<String>();
    while (rs.next()) {
        String proc = rs.getString("PROCEDURE_NAME");
        if ( Message.isDebugOn ) {
            Message.printDebug(dl, routine, "Procedure name to check = \"" + proc + "\"" );
        }
        // The SQL Server driver may return the procedure name with ";" and a number at the end.  If so
        // strip it off for further processing.
        int pos = proc.indexOf(";");
        if ( pos > 0 ) {
            proc = proc.substring(0,pos);
        }
        procNames.add ( proc );
    }
    // Strip out system stored procedures that a user should not run.
    // TODO SAM 2013-04-10 Need to figure out a more elegant way to do this.
    int databaseEngineType = dmi.getDatabaseEngineType();
    String [] systemProcPatternsToRemove = new String[0];
    if ( databaseEngineType == DMI.DBENGINE_SQLSERVER ) {
        String [] systemProcPatternsToRemove0 = {
            "dm.*",
            "dt.*",
            "fn.*",
            "sp.*",
            "xp.*"
        };
        systemProcPatternsToRemove = systemProcPatternsToRemove0;
    }
    if ( removeSystemProcedures ) {
        Message.printStatus(2, routine, "Removing system procedures from procedure list");
        for ( int i = 0; i < systemProcPatternsToRemove.length; i++ ) {
            StringUtil.removeMatching(procNames,systemProcPatternsToRemove[i],true);
        }
    }
    // Additionally remove all the tables that were in the notIncluded parameter passed in to this method.
    if (notIncluded != null) {
        Message.printStatus(2, routine, "Removing requested procedures from procedure list");
        for ( String s : notIncluded ) {
            StringUtil.removeMatching(procNames,s,true);
        }
    }
    // Using the following will close the related statement, which causes a problem on the 2nd call
    // to this method.
    //DMI.closeResultSet(rs);
    rs.close();
    return procNames;
}

/**
Returns the catalog of schema names for the connection, excluding known system schemas.
For example, if the JDBC connection is with a SQL Server "master" database, this will return the databases
under that instance.
@param dmi an open, connected and not-null DMI connection to a database.
@param catalog the catalog
@param removeSystemSchemas if true, remove the known system databases (this may take some work to keep up to date).
@param notIncluded a list of all the database names that should not be included
in the final list of database names.
@return the list of databases names in the dmi's database instance.  null
is returned if there was an error reading from the database.
*/
public static List<String> getDatabaseSchemaNames(DMI dmi, String catalog, boolean removeSystemSchemas, List<String> notIncluded)
{
    String routine = "getDatabaseSchemaNames";
    // Get the name of the data.  If the name is null, it's most likely
    // because the connection is going through ODBC, in which case the 
    // name of the ODBC source will be used.
    String dbName = dmi.getDatabaseName();
    if (dbName == null) {
        dbName = dmi.getODBCName();
    }

    Message.printStatus(2, routine, "Getting schemas");
    ResultSet rs = null;
    DatabaseMetaData metadata = null;
    try {
        metadata = dmi.getConnection().getMetaData();
        // TODO SAM 2013-07-22 SQL Server does not implement completely so just brute force below
        //if ( dmi.getDatabaseEngineType() == DMI.DBENGINE_SQLSERVER ) {
        //    // Have to make the call without the catalog...
            rs = metadata.getSchemas();
        //}
        //else {
        //    rs = metadata.getSchemas(catalog,null);
        //}
        if (rs == null) {
            Message.printWarning(3, routine, "Error getting schemas.");
            return null;
        } 
    } 
    catch (Exception e) {
        Message.printWarning(3, routine, "Error getting schemas.");
        Message.printWarning(3, routine, e);
        return null;
    } 

    String schema, cat;
    List<String> schemas = new Vector<String>();
    try {
        while ( rs.next() ) {
            try {   
                // Schema name...
                schema = rs.getString(1);
                if ( !rs.wasNull()) {
                    if ( (catalog == null) || (dmi.getDatabaseEngineType() == DMI.DBENGINE_SQLSERVER) ) {
                        schemas.add(schema.trim());
                    }
                    else {
                        // Check the catalog before adding
                        // TODO SAM 2013-07-22 SQL Server does not seem to return catalog
                        cat = rs.getString(2);
                        Message.printStatus(2,routine,"schema=" + schema + " cat=" + cat );
                        if ( !rs.wasNull()) {
                            // Have catalog
                            if ( catalog.equalsIgnoreCase(cat) ) {
                                schemas.add(schema.trim());
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                // continue getting the list of schemas, but report the error.
                Message.printWarning(3, routine, "Error getting schema information (" + e + ").");
                Message.printWarning(3, routine, e);
            }
        }
    }
    catch ( Exception e ) {
        // TODO SAM 2012-01-31 probably should not catch this but has been done historically
    }
    try {   
        DMI.closeResultSet(rs);
    }
    catch (Exception e) {
        Message.printWarning(2, routine, e);
    }

    // Sort the list of schemas in ascending order, ignoring case.
    schemas = StringUtil.sortStringList(schemas, StringUtil.SORT_ASCENDING, null, false, true);
   
    // Additionally remove all the schemas that were in the notIncluded parameter passed in to this method.
    if (notIncluded != null) {
        Message.printStatus(2, routine, "Removing requested schemas from schema list");
        for ( String s : notIncluded ) {
            StringUtil.removeMatching(schemas,s,true);
        }
    }
    return schemas;
}

/**
Returns the list of table names in the database, excluding known system tables.
@param dmi an open, connected and not-null DMI connection to a database.
@param catalog if not null, a database name under a main database (such as database under SQL Server master
database that corresponds to the connection).
@param schema if not null, the schema for a database (such as used by Oracle)
@param removeSystemTables if true, remove the known system tables (this may take some work to keep up to date).
@param notIncluded a list of all the table names that should not be included
in the final list of table names.
@return the list of table names in the dmi's database.  null
is returned if there was an error reading from the database.
*/
public static List<String> getDatabaseTableNames(DMI dmi, String catalog, String schema,
    boolean removeSystemTables, List<String> notIncluded)
{
	String routine = "getDatabaseTableNames";
	// Get the name of the data.  If the name is null, it's most likely
	// because the connection is going through ODBC, in which case the 
	// name of the ODBC source will be used.
	String dbName = dmi.getDatabaseName();
	if (dbName == null) {
		dbName = dmi.getODBCName();
	}

	Message.printStatus(2, routine, "Getting list of tables");
	ResultSet rs = null;
	DatabaseMetaData metadata = null;
	int databaseEngineType = dmi.getDatabaseEngineType();
	try {	
		metadata = dmi.getConnection().getMetaData();
		String [] typeArray = { "TABLE", "VIEW" };
		if ( databaseEngineType == DMI.DBENGINE_SQLSERVER ) {
		    // SQL Server does not seem to recognize the type array so get all and then filter below
		    typeArray = null;
		}
		rs = metadata.getTables(catalog, schema, null, typeArray);
		if (rs == null) {
			Message.printWarning(2, routine, "Error getting list of tables.  Aborting");
			return null;
		} 
	} 
	catch (Exception e) {
		Message.printWarning(2, routine, "Error getting list of tables.  Aborting.");
		Message.printWarning(2, routine, e);
		return null;
	} 

	// Loop through the result set and pull out the list of
	// all the table names and the table remarks.  
	Message.printStatus(2, routine, "Building table name list");	
	String tableName;
	String tableType;
	List<String> tableNames = new ArrayList();
	try {
    	while ( rs.next() ) {
    		try {	
    			// Table name...
    		    tableName = rs.getString(3);
    			if (rs.wasNull()) {
    				tableName = null;
    			}
    			else {
    			    if ( databaseEngineType == DMI.DBENGINE_ORACLE ) {
    			        if ( tableName.startsWith("/") ) {
    			            // See large number of tables with names like "/f1892dbb_LogicalBasicNetwork", type is "SYNONYM"
    			            continue;
    			        }
    			    }
    			}
    	        // Table type...
                tableType = rs.getString(4);
                if (rs.wasNull()) {
                    tableType = null;
                }
                if ( Message.isDebugOn ) {
                    Message.printDebug(10, routine, "Table \"" + tableName + "\" type is \"" + tableType + "\"" );
                }
                if ( removeSystemTables ) {
                    // TODO SAM 2012-01-31 Should perhaps be able to check for system tables here but SQL
                    // Server seems to call everything "TABLE" or "VIEW"
                    if ( !tableType.equalsIgnoreCase("TABLE") && !tableType.equalsIgnoreCase("VIEW") ) {
                        tableName = null;
                    }
                }
                if ( tableName != null ) {
                    tableNames.add(tableName.trim());
                }
    		}
    		catch (Exception e) {
    			// continue getting the list of table names, but report the error.
    			Message.printWarning(2, routine, e);
    		}
    	}
	}
	catch ( Exception e ) {
	    // TODO SAM 2012-01-31 probably should not catch this but has been done historically
	}
	try {	
		DMI.closeResultSet(rs);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	// Sort the list of table names in ascending order, ignoring case.
	tableNames = StringUtil.sortStringList(tableNames, StringUtil.SORT_ASCENDING, null, false, true);

	// Remove the list of system tables for each kind of database 
	// (all database types have certain system tables)
	// TODO SAM 2012-01-31 Should be able to do from metadata but SQL Server does not indicate system tables.
	String [] systemTablePatternsToRemove = getSystemTablePatternsToRemove (dmi.getDatabaseEngineType() );
	
	if ( removeSystemTables ) {
	    Message.printStatus(2, routine, "Removing system tables from table list");
        for ( int i = 0; i < systemTablePatternsToRemove.length; i++ ) {
            StringUtil.removeMatching(tableNames,systemTablePatternsToRemove[i],true);
        }
	}
	
	// Additionally remove all the tables that were in the notIncluded parameter passed in to this method.
	if (notIncluded != null) {
	    Message.printStatus(2, routine, "Removing requested tables from table list");
		for ( String s : notIncluded ) {
			StringUtil.removeMatching(tableNames,s,true);
		}
	}
	return tableNames;
}

/**
Return a list of String containing defined ODBC Data Source Names.
This method is only applicable on Windows operating systems.  The windows
registry for "HKEY_CURRENT_USER: Software\ODBC\ODBC.INI\ODBC Data Sources" is
read using the external shellcon.exe program.  This program must therefore be in the path.
@return a Vector of String containing defined ODBC Data Source Names.  The
Vector may be empty.
@param strip_general If true, strip general ODBC DSNs from the list (e.g., "Excel Files").
*/
public static List getDefinedOdbcDsn ( boolean strip_general )
{	List output = null;
	if (!IOUtil.isUNIXMachine()) {
		try {
			String [] command_array = new String[2];
			command_array[0] = "shellcon";
			command_array[1] = "-dsn";
			ProcessManager pm = new ProcessManager(command_array);
			pm.saveOutput(true);
			pm.run ();
			output = pm.getOutputList();
			//Message.printStatus ( 2, routine,
			//"Exit status from shellcon for ODBC is: " +
			//pm.getExitStatus() );
			// Finish the process...
			pm = null;
		}
		catch (Exception e) {
			// Won't work if running as an Applet!
			Message.printWarning (2, "DMIUtil.getDefinedOdbcDsn",e);
			output = null;
		}
	}

	List available_OdbcDsn = new Vector();
	if ((output != null) && (output.size() > 0)) {
		output = StringUtil.sortStringList (output, StringUtil.SORT_ASCENDING, null, false, true);
		int size = output.size();
		String odbc = "";
		for (int i = 0; i < size; i++) {
			odbc = ((String)output.get(i)).trim();
			if ( strip_general &&
				(odbc.regionMatches(true,0,"dBASE Files",0,11) ||
				odbc.regionMatches(true,0,"Excel Files",0,11) ||
				odbc.regionMatches(true,0,"FoxPro Files",0,12) ||
				odbc.regionMatches(true,0,"MS Access Database",0,18) ||
				odbc.regionMatches(true,0,"MQIS",0,4) ||
				odbc.regionMatches(true,0,"Visual FoxPro",0,13) ) ) {
				continue;
			}
			available_OdbcDsn.add ( odbc );
		}
	}
	return available_OdbcDsn;
}

/**
This function determines the extreme integer value of the specified field 
from the requested table via using max(field) or min(field).
@param dmi the DMI to use.
@param table table name.
@param field table field to determine the max value of.
@param flag "MAX" or "MIN" depending upon which extreme is desired.
@return returns a int of the extreme record or DMIUtil.MISSING_INT if an error occurred.
*/
private static int getExtremeRecord(DMI dmi, String table, String field, 
String flag) {
	try {
		String query = "select " + flag + "(" + field.trim() + ") from " + table.trim();
		ResultSet rs = dmi.dmiSelect(query);
	
		int extreme = DMIUtil.MISSING_INT;
		if (rs.next()) {
			extreme = rs.getInt(1);
			if (rs.wasNull()) {
				extreme = DMIUtil.MISSING_INT;
			}
		}
		DMI.closeResultSet(rs);
        return extreme;
	}
	catch (Exception e) {
		String routine = "DMIUtil.getExtremeRecord";
		Message.printWarning(2, routine, "Error finding extreme.");
		Message.printWarning(2, routine, e);
		return DMIUtil.MISSING_INT;
	}
}

/**
This function determines the max value of the specified integer field from the 
requested table via using max(field) and the the specified DMI connection.
@param dmi the DMI to use.
@param table table name
@param field table field to determine the max value of
@return returns max record or DMIUtil.MISSING_INT if an error occurred.
*/
public static int getMaxRecord(DMI dmi, String table, String field) {
	return DMIUtil.getExtremeRecord(dmi, table, field, "MAX");
}

/**
This function determines the minimum value of the specified integer field from the 
requested table via using min(field) and the the specified DMI connection.
@param dmi the DMI to use.
@param table table name
@param field table field to determine the min value of
@return returns min record or DMIUtil.MISSING_INT if an error occurred.
*/
public static int getMinRecord(DMI dmi, String table, String field) {
	return DMIUtil.getExtremeRecord(dmi, table, field, "MIN");
}

/**
Constructs a concatenated String with OR inserted at the appropriate locations.
@param or list of Strings in which to construct an OR clause
@return returns a String with OR inserted at the appropriate locations, null if "or" is null
*/
public static String getOrClause(List<String> or) {
        if (or == null) {
            return null;
        }

        int size = or.size();
        String orString = "";

        for (int i = 0; i < size; i++) {
            if ((i != (size - 1)) && (size != 1)) {
                orString += or.get(i)+ " OR " ;
            }
            else {
                orString += or.get(i);
            }
        }
        return "(" + orString + ")";         
}

/**
Returns the database types a DMI can connect to that are done via direct server connection.
@return a list of the database types a DMI can connect to that are done via direct server connection.
*/
public static List getServerDatabaseTypes() {
	return DMI.getServerDatabaseTypes();
}

/**
Returns the comment for a table in a SQL Server database.
@param dmi a dmi object that is open, not-null, and connected to a SQL Server database.
@param tableName the name of the table for which to return the comment.  Must not be null.
@return the comment for a table in the SQL Server database.
*/
public static String getSQLServerTableComment(DMI dmi, String tableName)
throws Exception {
	String SQL = "SELECT * FROM ::fn_listextendedproperty('MS_Description'"
		+ ",'user','dbo', 'table','" + tableName + "',null,null)";
	ResultSet rs = dmi.dmiSelect(SQL);

	boolean more = rs.next();

	String comment = "   ";
	if (more) {
		comment = rs.getString(4);
	}
	DMI.closeResultSet(rs);
	return comment;	
}

/**
Returns the comment for a column in a SQL Server database.
@param dmi a dmi object that is open, not-null, and connected to a SQL Server database.
@param tableName the name of the table containing the column.  Must not be null.
@param columnName the name of the column for which to return the comment.  Must not be null.
@return the comment for a column in a SQL Server database.
*/
public static String getSQLServerColumnComment(DMI dmi, String tableName, String columnName) 
throws Exception {
    boolean doMeta = false;
    String comment = "   ";
    if ( doMeta ) {
        
    }
    else {
    	String SQL = "SELECT * FROM ::fn_listextendedproperty('MS_Description'"
    		+ ",'user','dbo', 'table','" + tableName + "','column'"	+ ",'" + columnName + "')";
    	ResultSet rs = dmi.dmiSelect(SQL);
    
    	boolean more = rs.next();
    
    	if (more) {
    		comment = rs.getString(4);
    	}
    	DMI.closeResultSet(rs);
    }
	return comment;	
}

/**
Return a list of system table name patterns to remove.  These are internal tables that should not be visible to users.
*/
public static String [] getSystemTablePatternsToRemove ( int databaseEngineType )
{
    String [] systemTablePatternsToRemove = new String[0];
    if ( databaseEngineType == DMI.DBENGINE_ACCESS ) {
        String [] systemTablePatternsToRemove0 = {
            "MSysAccessObjects",
            "MSysACEs",
            "MSysObjects",
            "MSysQueries",
            "MSysRelationships",
            "Paste Errors"
        };
        systemTablePatternsToRemove = systemTablePatternsToRemove0;
    }
    else if ( databaseEngineType == DMI.DBENGINE_SQLSERVER ) {
        String [] systemTablePatternsToRemove0 = {
        // Older SQL Server (pre 2005 ?)...
        // Use .* for regex when glob * is needed - the following deals with ignoring case
        "syscolumns",
        "syscomments",
        "sysdepends",
        "sysfilegroups",
        "sysfiles",
        "sysfiles1",
        "sysforeignkeys",
        "sysfulltextcatalogs",
        "sysfulltextnotify",
        "sysindexes",
        "sysindexkeys",
        "sysmembers",
        "sysobjects",
        "syspermissions",
        "sysproperties",
        "sysprotects",
        "sysreferences",
        "systypes",
        "sysusers",
        "sysconstraints",
        "syssegments",
        "dtproperties",
        // Newer SQL Server (2005+ ?)...
        // Use .* for regex when glob * is needed - the following deals with ignoring case
        "all_columns",
        "all_objects",
        "all_parameters",
        "all_sql_modules",
        "all_views",
        "allocation_keys",
        "allocation_units",
        "assemblies",
        "assembly_.*",
        "asymmetric_keys",
        "availability_.*",
        "backup_devices",
        "certificates",
        "check_constraints",
        "column_.*",
        "columns",
        "computed_columns",
        "configurations",
        "constraint_.*",
        "conversation_.*",
        "credentials",
        "crypt_properties",
        "cryptographic_.*",
        "databases",
        "database_.*",
        "data_spaces",
        "default_constraints",
        "destination_data.*",
        "dm_.*",
        "domains",
        "domain_constraints",
        "change_tracking.*",
        "endpoints",
        "endpoint_webmethods",
        "event_notification.*",
        "events",
        "extended_.*",
        "filegroups",
        "filetable.*",
        "foreign_key.*",
        "fulltext_.*",
        "function_.*",
        "http_endpoints",
        "identity_columns",
        "index_columns",
        "indexes",
        "internal_tables",
        "key_.*",
        "linked_logins",
        "login_token",
        "master_.*",
        "messages",
        "message_.*",
        "module_.*",
        "numbered_.*",
        "objects",
        "openkeys",
        "parameter_.*",
        "parameters",
        "partition_.*",
        "partitions",
        "plan_guides",
        "procedures.*",
        "referential_.*",
        "remote_.*",
        "registered_search.*",
        "resource_.*",
        "routes",
        "routine_columns",
        "routines",
        "sequences",
        "schemas",
        "schemata",
        "securable_classes",
        "selective_xml.*",
        "server_.*",
        "servers",
        "service_.*",
        "services",
        "soap_.*",
        "spatial_.*",
        "spt_.*",
        "sql_.*",
        "stats",
        "stats_columns",
        "symmetric_keys",
        "synonyms",
        "system_.*",
        "sys.*",
        "tables",
        "table_constraints",
        "table_privileges",
        "table_types",
        "tcp_endpoints",
        "trace_.*",
        "traces",
        "transmission_queue",
        "triggers",
        "trigger_.*",
        "types",
        "type_assembly_usages",
        "user_token",
        "version",
        "via_endpoints",
        "views",
        "view_column_usage",
        "view_table_usage",
        "xml_indexes",
        "xml_schema.*",
        };
        systemTablePatternsToRemove = systemTablePatternsToRemove0;
    }
    else {  
        // TODO SAM 2012-01-31 Unsure what tables are specific to other database types, this needs to be checked.
    }
    return systemTablePatternsToRemove;
}

/**
Create a list of where clauses give an InputFilter_JPanel.  The InputFilter
instances that are managed by the InputFilter_JPanel must have been defined with
the database table and field names in the internal (non-label) data.
@return a list of where clauses, each of which can be added to a DMI statement.
@param dmi The DMI instance being used, which may be checked for specific formatting.
@param panel The InputFilter_JPanel instance to be converted.
*/
public static List<String> getWhereClausesFromInputFilter (	DMI dmi, InputFilter_JPanel panel )
{	// Loop through each filter group.  There will be one where clause per filter group.
	int nfg = panel.getNumFilterGroups ();
	InputFilter filter;
	List<String> where_clauses = new Vector();
	String where_clause="";	// A where clause that is being formed.
	for ( int ifg = 0; ifg < nfg; ifg++ ) {
		filter = panel.getInputFilter ( ifg );	
		where_clause = getWhereClauseFromInputFilter(dmi, filter, panel.getOperator(ifg));
		if (where_clause != null) {
			where_clauses.add(where_clause);
		}
	}
	return where_clauses;
}

/**
Create a single where clause given an InputFilter.  The InputFilter
instances that are managed by the InputFilter_JPanel must have been defined with
the database table and field names in the internal (non-label) data.
@return a list of where clauses, each of which can be added to a DMI statement.
@param dmi The DMI instance being used, which may be checked for specific formatting.
@param operator the operator to use in creating the where clause
@param panel The InputFilter_JPanel instance to be converted.
*/
public static String getWhereClauseFromInputFilter(DMI dmi, InputFilter filter, String operator) {
    return getWhereClauseFromInputFilter( dmi, filter, operator, false);
}

/**
Create a single where clause given an InputFilter.  The InputFilter
instances that are managed by the InputFilter_JPanel must have been defined with
the database table and field names in the internal (non-label) data.
@return a list of where clauses, each of which can be added to a DMI statement.
@param dmi The DMI instance being used, which may be checked for specific formatting
@param panel The InputFilter_JPanel instance to be converted
@param operator the operator to use in creating the where clause
@param upperCase if true, then the where clause for strings will be converted to upper case using
the SQL upper() function - this is necessary for databases where a global case-insensitive
option is not available
*/
public static String getWhereClauseFromInputFilter(DMI dmi, InputFilter filter, String operator,
    boolean upperCase ) {
	String routine = "getWhereClauseFromInputFilter";
	// Get the selected filter for the filter group...
	if ( filter.getWhereLabel().trim().equals("") ) {
		// Blank indicates that the filter should be ignored...
		return null;
	}
	// Get the input type...
	int input_type = filter.getInputType();
	// Get the internal where...
	String whereSubject = filter.getWhereInternal();
	if ( (whereSubject == null) || whereSubject.equals("") ) {
	    return null;
	}
	// Get the user input...
	String input = filter.getInputInternal().trim();
    if ( upperCase ) {
        input = input.toUpperCase();
    }
	Message.printStatus(2,routine,"Internal input is \"" + input + "\"");
	// Now format the where clause...
	
	String where_clause = null;
	
	if ( operator.equalsIgnoreCase(InputFilter.INPUT_BETWEEN) ) {
		// TODO - need to enable in InputFilter_JPanel.
	}
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_CONTAINS) ) {
		// Only applies to strings...
	    if ( upperCase ) {
	        where_clause = "upper(" + whereSubject + ") like '%" + input + "%'";
	    }
	    else {
	        where_clause = whereSubject + " like '%" + input + "%'";
	    }
	}
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_ENDS_WITH) ) {
		// Only applies to strings...
        if ( upperCase ) {
            where_clause = "upper(" + whereSubject + ") like '%" + input + "'";
        }
        else {
            where_clause = whereSubject + " like '%" + input + "'";
        }
	}
	else if ( operator.equalsIgnoreCase(InputFilter.INPUT_EQUALS) ){
		if ( input_type == StringUtil.TYPE_STRING ) {
	        if ( upperCase ) {
	            where_clause = "upper(" + whereSubject + ")='" + input + "'";
	        }
	        else {
	            where_clause = whereSubject + "='" + input + "'";
	        }
		}
		else {
			// Number...
			where_clause = whereSubject + "=" + input;
		}
	}
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_GREATER_THAN) ) {
		// Only applies to numbers (?)...
		where_clause = whereSubject + ">" + input;
	}
	else if ( operator.equalsIgnoreCase(InputFilter.INPUT_GREATER_THAN_OR_EQUAL_TO) ) {
		// Only applies to numbers (?)...
		where_clause = whereSubject + ">=" + input;
	}
    else if ( operator.equalsIgnoreCase(InputFilter.INPUT_IS_EMPTY)){
        where_clause = whereSubject + "='' or where is null";
    }
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_LESS_THAN) ) {
		// Only applies to numbers (?)...
		where_clause = whereSubject + "<" + input;
	}
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_LESS_THAN_OR_EQUAL_TO) ) {
		// Only applies to numbers (?)...
		where_clause = whereSubject + "<=" + input;
	}
	else if ( operator.equalsIgnoreCase(InputFilter.INPUT_MATCHES)){
	    // Only applies to strings
	    if ( upperCase ) {
	        where_clause = "upper(" + whereSubject + ")='" + input + "'";
	    }
	    else {
	        where_clause = whereSubject + "='" + input + "'";
	    }
	}
	else if ( operator.equalsIgnoreCase(InputFilter.INPUT_ONE_OF) ){
		// TODO - need to enable in InputFilter_JPanel
	}
	else if ( operator.equalsIgnoreCase( InputFilter.INPUT_STARTS_WITH) ) {
		// Only applies to strings...
	    if ( upperCase ) {
	        where_clause = "upper(" + whereSubject + ") like '" + input + "%'";
	    }
	    else {
	        where_clause = whereSubject + " like '" + input + "%'";
	    }
	}
	else {
		// Unrecognized where...
	    String message = "Unrecognized operator \"" + operator + "\"";
		Message.printWarning ( 2, routine, message );
		throw new InvalidParameterException(message);
	}
	// TODO - need to handle is null, negative (not), when enabled in InputFilter_JPanel.
	// TODO - need a clean way to enforce upper case input but
	// also perhaps allow a property in the filter to override
	// because a database may have mixed case in only a few tables...
	//if ( dmi.uppercaseStringsPreferred() ) {
		//where_clause = where_clause.toUpperCase();
	//}
	return where_clause;
}

/**
Determines whether a date value is missing.
@param value the date to be checked 
@return true if the date is missing, false if not
*/
public static boolean isMissing(Date value) {
	return isMissingDate(value);
}

/**
Determines whether a double value is missing.
@param value the double to be checked
@return true if the double is missing, false if not
*/
public static boolean isMissing(double value) {
	return isMissingDouble(value);
}

/**
Determines whether a Double value is missing.
@param value the Double to be checked.
@return true if the Double is missing, false if not.
*/
public static boolean isMissing(Double value) {
	return isMissingDouble(value.doubleValue());
}

/**
Determines whether a float value is missing.
@param value the float to be checked
@return true if the float is missing, false if not
*/
public static boolean isMissing(float value) {
	return isMissingDouble(value);
}

/**
Determines whether a Float value is missing.
@param value the Float to be checked.
@return true if the Float is missing, false if not.
*/
public static boolean isMissing(Float value) {
	return isMissingDouble(value.floatValue());
}

/**
Determines whether an int value is missing.
@param value the int to be checked
@return true if the int is missing, false if not
*/
public static boolean isMissing(int value) {
	return isMissingInt(value);
}

/**
Determines whether an Integer value is missing.
@param value the Integer to be checked.
@return true if the Integer is missing, false if not.
*/
public static boolean isMissing(Integer value) {
	return isMissingInt(value.intValue());
}

/**
Determines whether a long value is missing.
@param value the long to be checked
@return true if the long is missing, false if not
*/
public static boolean isMissing(long value) {
	return isMissingLong(value);
}

/**
Determines whether a Long value is missing.
@param value the Long to be checked.
@return true if the Long is missing, false if not.
*/
public static boolean isMissing(Long value) {
	return isMissingLong(value.longValue());
}

/**
Determines whether a String value is missing.
@param value the String to be checked
@return true if the String is missing, false if not
*/
public static boolean isMissing(String value) {
	if ((value == null) || (value.length() == 0)) {
		return true;
	}
	return false;
}

/**
Determines whether a Date is missing.
@param value the Date to be checked
@return true if the Date is missing, false if not
*/
private static boolean isMissingDate(Date value) {
	if (value == MISSING_DATE) {
		return true;
	}
	return false;
}

/**
Determines whether a double is missing.
@param value the double to be checked
@return true if the double is missing, false if not
*/
private static boolean isMissingDouble(double value) {
	if ( Double.isNaN(value) ) {
		return true;
	}
	// TODO SAM 2015-08-06 Remove the following if NaN works for missing
	//if ((value > MISSING_DOUBLE_FLOOR) && (value < MISSING_DOUBLE_CEILING)) {
	//	return true;
	//}
	return false;
}

/**
Determines whether an int is missing.
@param value the int to be checked
@return true if the int is missing, false if not
*/
private static boolean isMissingInt(int value) {
	if (value == MISSING_INT) {
		return true;
	} 
	return false;
}

/**
Determines whether a long is missing.
@param value the long to be checked
@return true if the long is missing, false if not
*/
private static boolean isMissingLong(long value) {
	if (value == MISSING_LONG) {
		return true;
	}
	return false;
}

/**
Uses Message.printDebug(1, ...) to print out the results stored in a 
list of lists (which has been returned from a call to processResultSet)
*/
public static void printResults(List v) {
	printResults(v, "  ");
}

/**
Uses Message.printDebug(1, ...) to print out the results stored in a 
list of lists (which has been returned from a call to processResultSet)
*/
public static void printResults(List v, String delim) {
	int size = v.size();
	for (int i = 0; i < size; i++) {
		List vv = (List)v.get(i);
		int vsize = vv.size();
		Message.printDebug(1, "", "  -> ");
		for (int j = 0; j < vsize; j++) {
			Message.printDebug(1, "", "" + vv.get(j) + delim);
		}
		Message.printDebug(1, "", "\n");
	}
}

/**
Print the detailed information for an SQLException, using Message.printDebug().
The SQLException message, state, and error code are printed for each level of the stack trace.
@param dl Debug level to print at.
@param routine Name of the routine that should be included in the message (can be null).
@param e the exception that was thrown
*/
public static void printSQLException( int dl, String routine, SQLException e)
{	if (Message.isDebugOn) {
		Message.printDebug( dl, routine, "SQL Exception:");
		while (e != null) {
			Message.printDebug(1, "", "  Message:   " + e.getMessage ());
			Message.printDebug(1, "", "  SQLState:  " + e.getSQLState ());
			Message.printDebug(1, "", "  ErrorCode: " + e.getErrorCode ());
			e.printStackTrace();
			e = e.getNextException();
			Message.printDebug(dl, "", "");
		}
	}
}

/** 
Takes a result set returned from a <b><code>SELECT</b></code> statement
and transforms it into a list so that it can be returned and operated on more easily.<p>
strings are entered into the resulting list as strings, but numeric 
values are entered in as the Java Wrapper objects that correspond to the kind of primitive they are. <p>
e.g., <code>int</code>s will be entered into the vector as <code>Integer</code>
s.  <code>float</code>s will be entered into the vector as <code>Float
</code>s, and so on.<p>
<b>Note:</b> Not all of the SQL data types are accounted for yet in this
method.  For instance, <code>java.sql.Types.DISTINCT</code> has no code
in place to transform it into something we can work with in a vector. 
This may cause some odd errors, but there's little that can be done right 
now.  Fortunately, such occurrences should be very rare.
@param rs the resultSet whose values will be entered into vector format
@return a vector containing all the values from the resultset
@throws SQLException thrown by ResultSet.getMetaData(), 
ResultSetMetaData.getColumnCount(), or any of the ResultSet.get[DataType]() methods
*/
public static List processResultSet(ResultSet rs) throws SQLException {
	String routine = "DMI.processResultSet";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "[method called]");
	}

	// Used for storing the type of each column in the resultSet
	List types = new Vector(0);

	// The list which will be built containing the rows from the resultSet
	List results = new Vector(0);

	// Set up the types list 
	ResultSetMetaData rsmd = rs.getMetaData();	
	int columnCount = rsmd.getColumnCount();
	for (int i = 0; i < columnCount; i++) {
		types.add(new Integer(rsmd.getColumnType(i + 1)));
	}

	while(rs.next()) {
		List row = new Vector(0);
		for (int i = 0; i < columnCount; i++) {
			Integer I = (Integer)types.get(i);
			int val = I.intValue();
	
			switch(val) {
				case java.sql.Types.BIGINT:
					row.add(new Integer(rs.getInt(i + 1)));
					break;
				case java.sql.Types.BIT:
					row.add("java.sql.Types.BIT");
					break;
				case java.sql.Types.CHAR:
					row.add(rs.getString(i + 1)); 
					break;
				case java.sql.Types.DATE:
					row.add(rs.getDate(i + 1));  
					break;
				case java.sql.Types.DECIMAL:
					row.add(new Double(rs.getDouble(i + 1)));
					break;
				case java.sql.Types.DOUBLE:			
					row.add(new Double(rs.getDouble(i + 1)));
					break;
				case java.sql.Types.FLOAT:
					row.add(new Float(rs.getFloat(i + 1)));  
					break;
				case java.sql.Types.INTEGER:
					row.add(new Integer(rs.getInt(i + 1)));  
					break;
				case java.sql.Types.LONGVARBINARY:
					row.add(rs.getBinaryStream(i + 1));
					break;
				case java.sql.Types.LONGVARCHAR:
					row.add(rs.getString(i + 1)); 
					break;
				case java.sql.Types.NULL:
					row.add("java.sql.Types.NULL");
					break;
				case java.sql.Types.NUMERIC:
					row.add("java.sql.Types.NUMERIC");
					break;
				case java.sql.Types.NVARCHAR:
					row.add(rs.getString(i + 1));  
					break;
				case java.sql.Types.OTHER:
					row.add(rs.getObject(i + 1)); 
					break;
				case java.sql.Types.REAL:
					row.add(new Float(rs.getFloat(i + 1))); 
					break;
				case java.sql.Types.SMALLINT:
					row.add(new Integer(rs.getInt(i + 1)));  
					break;
				case java.sql.Types.TIME:
					row.add(rs.getTime(i + 1));  
					break;
				case java.sql.Types.TIMESTAMP:
					row.add(rs.getTimestamp(i + 1));  
					break;
				case java.sql.Types.TINYINT:
					row.add(new Integer(rs.getInt(i + 1)));  
					break;
				case java.sql.Types.VARBINARY:
					row.add(rs.getBinaryStream(i + 1));
					break;
				case java.sql.Types.VARCHAR:
					row.add(rs.getString(i + 1));  
					break;
				default:
					Message.printWarning(3, routine, "Unhandled database data type " + val );
					break;
			} // end switch
		} // end for on columns
		results.add(row);
	} // end while on records
	return results;
}

/**
Queries a resultset's meta data for the names of the columns returned into the result set.  
@param rs the ResultSet for which the column names are desired
@return a vector containing the names of the columns in order from 1 to X
@throws SQLException thrown by ResultSet.getMetaData, 
ResultSetMetaData.getColumnCount or ResultSetMetaData.getColumnName
*/
public static List processResultSetColumnNames(ResultSet rs) 
throws SQLException {
	String routine = "DMI.processResultSetColumnNames";
	int dl = 25;

	if (Message.isDebugOn) {
		Message.printDebug(dl, routine, "[method called]");
	}
	List names = new Vector(0);
	
	ResultSetMetaData rsmd = rs.getMetaData();
	int count = rsmd.getColumnCount();
	
	///////////////////////////////////////////////////////////////////
	// Important Developer Note
	///////////////////////////////////////////////////////////////////	
	// when querying the names of columns from ResultSetMetaData, columns 
	// are numbered from 1 to X, not from 0 to X. 
	// 
	// Furthermore, calling rsmd.getColumnName(0) did not throw an 
	// exception, nor did it crash the program.  The code just hung.  
	// Something to watch out for.

	for (int i = 1; i <= count; i++) {
		names.add(rsmd.getColumnName(i));
	}

	return names;
}

/**
Remove comments from an SQL statement.  This is needed because some databases such as Microsoft Access
do not allow comments.  Currently only C-style slash-dot and dot-slash comments are removed.
@param sql SQL statement to process.
*/
public static String removeCommentsFromSql ( String sql )
{
    StringBuilder s = new StringBuilder(sql);
    // Search for /* */ comments until none are left
    int pos1 = 0;
    int pos2 = 0;
    int lenRemoved;
    while ( true ) {
        pos1 = s.indexOf("/*",pos2);
        pos2 = s.indexOf("*/",pos2);
        if ( (pos1 < 0) || (pos2 < 0) ) {
            break;
        }
        s.delete(pos1,(pos2 + 2));
        lenRemoved = pos2 - pos1 + 2;
        pos2 = pos2 - lenRemoved;
        if ( pos2 > (s.length() - 1) ) {
            break;
        }
    }
    // Search for -- to newline until none are left
    pos1 = 0;
    while ( true ) {
        pos1 = s.indexOf("--",pos1);
        pos2 = s.indexOf("\n",pos2);
        if ( pos2 < 0 ) {
            // Go to the end of the string (-1 because incremented below)
            pos2 = s.length() - 1;
        }
        if ( (pos1 < 0) || (pos2 < 0) ) {
            break;
        }
        // The following should work even if \r\n is used for new lines
        s.delete(pos1,(pos2 + 1));
        lenRemoved = pos2 - pos1 + 1;
        pos2 = pos2 - lenRemoved;
        if ( pos2 > (s.length() - 1) ) {
            break;
        }
    }
    return s.toString();
}

/**
Removes a table from a database if the user has the permission to.
@param dmi an open DMI object connected to the database in which to work.
@param tableName the name of the table to remove.
@throws Exception if an error occurs
*/
public static void removeTable(DMI dmi, String tableName) 
throws Exception {
	String SQL = null;

	String databaseEngine = dmi.getDatabaseEngine();
	int databaseEngineType = dmi.getDatabaseEngineType();

	if ( databaseEngineType == DMI.DBENGINE_SQLSERVER ) {
		SQL = "DROP TABLE " + tableName;
	}
	else {
		throw new Exception ("Unsupported database type: " + databaseEngine + " in 'removeTable'");
	}
	
	dmi.dmiExecute(SQL);
}

/**
Checks to see whether a result set has a column with the given name.  The check is case-sensitive.
@param resultSet the result set to check.
@param columnName the name of the column to search for in the result set.  
The column name is checked with case sensitivity.
@return true if the result set has a column with the given name, false if not.
@throws Exception if an error occurs checking for the name.
*/
public static boolean resultSetHasColumn(ResultSet resultSet, String columnName) 
throws Exception {
	ResultSetMetaData rsmd = resultSet.getMetaData();
	int num = rsmd.getColumnCount();
	for (int i = 1; i <= num; i++) {
		if (rsmd.getColumnName(i).equals(columnName)) {
			return true;
		}
	}
	return false;
}

/**
Sets the X and Y position of an ERDiagram_Table object from data in the dmi's database.
@param dmi an open, connected and not-null dmi object.
@param table the table object to fill with data from the database.  Must not be null.
@param tablesTableName the name of the table in the database that contains the
list of table names and ER Diagram information.  Must not be null.
@param tableField the name of the column in the tables table that contains the
names of the tables.  Must not be null.
@param erdXField the name of the column in the tables table that contains the
X positions of the ERDiagram Tables.  Must not be null.
@param erdYField the name of the column in the tables table that contains the
Y positions of the ERDIagram Tables.  Must not be null.
*/
private static void setTableXY(DMI dmi, ERDiagram_Table table, 
String tablesTableName, String tableField, String erdXField, String erdYField) {
	String sql = "SELECT " + erdXField + ", " + erdYField + " FROM " 
		+ tablesTableName + " WHERE " + tableField + " = '" + table.getName() + "'";
	try {
		ResultSet rs = dmi.dmiSelect(sql);

		boolean more = rs.next();
	
		if (more) {
			double x = rs.getDouble(1);
			if (!rs.wasNull()) {
				table.setX(x);
			}
			double y = rs.getDouble(2);
			if (!rs.wasNull()) {
				table.setY(y);
			}				
		}	
		DMI.closeResultSet(rs);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

// TODO (JTS - 2003-04-22) Proof of concept of getting user privileges out of a table
public static void testPrivileges(DMI dmi, String tableName) {
	try {
		DatabaseMetaData metadata = null;
		metadata = dmi.getConnection().getMetaData();

		ResultSet rs = metadata.getTablePrivileges(null, null, tableName);

		boolean more = rs.next();
		/*
		System.out.println("Privileges for table: " + tableName);
		System.out.println("Grantor		Grantee		" + "Privilege");
		*/
		while (more) {
			/*
			System.out.println(rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6));
			*/
			more = rs.next();
		}
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

// TODO (JTS - 2003-04-22) Proof of concept of getting key information out of a table
public static void testKeys(DatabaseMetaData metadata, String tableName) {
	Message.printStatus(2, "", "" + tableName + " foreign key info:");
	ResultSet keysRS = null;
	List keysV = null;
	int keysSize = 0;
	try {
		keysRS = metadata.getImportedKeys(null, null, tableName);
		keysV = new Vector();
		while (keysRS.next()) {
			keysV.add(testKeysString(keysRS));
		}
		keysSize = keysV.size();
	}
	catch (Exception e) {
	}
	for (int i = 0; i < keysSize; i++) {
		Message.printStatus(2, "", "  [" + i + "] (imp): " + keysV.get(i));
	}

	try {
		keysRS = metadata.getExportedKeys(null, null, tableName);
		keysV = new Vector();
		while (keysRS.next()) {
			keysV.add(testKeysString(keysRS));
		}
		keysSize = keysV.size();
	}
	catch (Exception e) {
	}
	for (int i = 0; i < keysSize; i++) {
		Message.printStatus(2, "", "  [" + i + "] (exp): " + keysV.get(i));
	}
}

// TODO (JTS - 2003-04-22) Proof of concept of getting key information out of a table
public static String testKeysString(ResultSet rs) {
	String s = null;
	try {
		s = "[" + rs.getString(3) + "." + rs.getString(4) + "] -> ["
			+ rs.getString(7) + "." + rs.getString(8) + "]";
	}
	catch (Exception e) {
		e.printStackTrace();
		s = "[null]";
	}
	return s;
}

/**
Convert a ResultSet to a list of strings.  The ResultSet is expected to only contain a string data element.
This is useful for converting a distinct string query to the list of strings.
@param rs ResultSet from a table query.
@return a list of strings for the first and only column in the result set, guaranteed to be non-null,
but may be empty.
@throws SQLException if an error occurs
*/
public static List<String> toStringList ( ResultSet rs ) 
throws SQLException {
    List<String> v = new Vector();
    int index = 1;
    String s;
    while ( rs.next() ) {
        index = 1;
        s = rs.getString(index++);
        if ( !rs.wasNull() ) {
            v.add(s.trim());
        }
    }
    return v;
}

public static void dumpProcedureInfo(DMI dmi, String procedure) {
Message.printStatus(2, "", "Dumping procedure info for: " + procedure);
Message.printStatus(2, "", "----------------------------------------------");
	try {
		DatabaseMetaData metadata = null;
		metadata = dmi.getConnection().getMetaData();
		ResultSet rs = metadata.getProcedureColumns( dmi.getDatabaseName(), null, procedure, null);
		printResults(processResultSet(rs));	
		rs.close();
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

}