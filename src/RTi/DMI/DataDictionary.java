// DataDictionary - class to create a data dictionary for a database

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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.HTMLWriter;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

// TODO smalers 2018-09-12 with advances in the JDBC API, this code could be rewritten to be more readable.
// For example, call the column metadata resultset getString("COLUMN_NAME") instead of dealing with resultset
// integer positions.  There are some limitations, such as not being able to cleanly detect array columns
// and dimensions of the array.
/**
This class creates a data dictionary.
*/
public class DataDictionary
{

	// TODO smalers 2018-09-21 why is this using integer positions rather than column name lookup,
	// which would be more readable?
/**
Field numbers used in determining field values when generating data dictionaries.
Equivalent is databaseMetaData.getColumns() and then getString("COLUMN_NAME"), etc.
*/
private final int 
	__POS_NUM = 11,
	__POS_COLUMN_NAME = 0,
	__POS_IS_PRIMARY_KEY = 1,
	__POS_COLUMN_TYPE = 2,
	__POS_COLUMN_SIZE = 3,
	__POS_NUM_DIGITS = 4,
	__POS_NULLABLE = 5,
	__POS_REMARKS = 6,
	__POS_EXPORTED = 7,
	__POS_FOREIGN = 8,
	__POS_PRIMARY_TABLE = 9,
	__POS_PRIMARY_FIELD = 10;

/**
Constructor.
*/
public DataDictionary ()
{
	
}

/**
Creates an HTML data dictionary.
The data dictionary consists of three main sections:<ol>
<li>The initial table list.  This shows a list of all the tables in the
database and any accompanying remarks.  Each table name is a link to the 
table detail in section 2, below:</li>
<li>A detailed list of the columns and column types for every table.</li>
<li>A list of all the reference tables passed in to this method and a dump of all their data.</li>
@param dmi DMI instance for an opened database connection.
@param filename Complete name of the data dictionary HTML file to write.  If
the filename does not end with ".html", that will be added to the end of the filename.
@param newline string to replace newlines with (e.g., "<br">).
@param surroundWithPre if true, output comments/remarks surrounded by <pre></pre>, for example
to keep newlines as is.
@param encodeHtmlChars if true, encode HTML characters that have meaning, such as < so as to pass through to HTML.
@param referenceTables If not null, the contents of these tables will be listed
in a section of the data dictionary to illustrate possible values for lookup fields.  
@param excludeTables list of tables that should be
excluded from the data dictionary.  The names of the tables in this list
must match the actual table names exactly (cases and spaces).  May be null.  May contain wildcard *.
*/
public void createHTMLDataDictionary ( DMI dmi, String filename, String newline,
	boolean surroundWithPre, boolean encodeHtmlChars,
	List<String> referenceTables, List<String> excludeTables)
{
	String routine = getClass().getSimpleName() + ".createHTMLDataDictionary";

	// Get the name of the data.  If the name is null, it's most likely
	// because the connection is going through ODBC, in which case the 
	// name of the ODBC source will be used.
	String dbName = dmi.getDatabaseName();
	if (dbName == null) {
		dbName = dmi.getODBCName();
	}

	// do the following so no worries about making null checks
	if (referenceTables == null) {
		referenceTables = new ArrayList<String>();
	}
	
	if (!StringUtil.endsWithIgnoreCase(filename, ".html")) {
		filename = filename + ".html";
	}

	Message.printStatus(2, routine, "Creating HTMLWriter");
	HTMLWriter html = null;
	// try to open an HTMLWriter object.
	try {
		html = new HTMLWriter(filename, dbName + " Data Dictionary");
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error opening HTMLWriter file - aborting data dictionary creation (" + e + ").");
		return;
	}

	// Write out the header information.  
	// This info tells when the Data Dictionary was 
	// created and if the database connection is through JDBC:
	// - the name of the database engine
	// - the name of the database server
	// - the name of the database
	// - the port on which the database is found
	//
	// If the database connection is through ODBC, the name of the ODBC
	// source is printed.
	Message.printStatus(2, routine, "Writing Data Dictionary header information");
	try {
		html.heading(1, dbName + " Data Dictionary");
		DateTime now = new DateTime(DateTime.DATE_CURRENT);
		html.addText("Generated at: " + now);
		html.paragraph();

		if (dmi.getJDBCODBC()) {
			html.addText("Database engine: " + dmi.getDatabaseEngine());
			html.breakLine();
			html.addText("Database server: " + dmi.getDatabaseServer());
			html.breakLine();
			html.addText("Database name: " + dmi.getDatabaseName());
		}
		else {
			html.addText("ODBC DSN: " + dmi.getODBCName());
		}
		html.paragraph();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error writing dictionary header (" + e + ").");
		Message.printWarning(2, routine, e);
	}

	Message.printStatus(2, routine, "Getting list of tables and views");
	ResultSet rs = null;
    String [] tableTypes = { "TABLE", "VIEW" };
    if ( dmi.getDatabaseEngineType() == DMI.DBENGINE_SQLSERVER ) {
        // SQL Server does not seem to recognize the type array so get all and then filter below
        tableTypes = null;
    }
	DatabaseMetaData metadata = null;
	try {	
		metadata = dmi.getConnection().getMetaData();
		rs = metadata.getTables(null, null, null, tableTypes);
		if (rs == null) {
			Message.printWarning(2, routine, "Error getting list of tables.  Aborting");
			return;
		} 
	} 
	catch (Exception e) {
		Message.printWarning(2, routine, "Error getting list of tables - aborting (" + e + ").");
		Message.printWarning(2, routine, e);
		return;
	} 

	// Loop through the result set and pull out the list of all the table names and the table remarks.  
	Message.printStatus(2, routine, "Building table name and remark list");	
	String temp;
	String temp2;
	List<String> tableNames = new ArrayList<String>(); // Used for sorting the list of tables
	List<JdbcTableMetadata> tableList0 = new ArrayList<JdbcTableMetadata>();
	boolean doFullTableMetadata = true;
	boolean idColumnSupported = true;
	boolean idColumnSetterSupported = true;
	while (true) {
		try {
			if ( !rs.next() ) {
				break;
			}
			JdbcTableMetadata table = new JdbcTableMetadata();
			// Table name...
			temp = rs.getString(3);
			if (!rs.wasNull()) {
				tableNames.add(temp.trim());
				table.setName(temp.trim());
				// Remarks...
				temp = rs.getString(5);
				if (!rs.wasNull()) {
					table.setRemarks(temp.trim());
				}
				else {	
					// Add a multi-character blank string so when it's placed in the HTML table,
					// it will be turned into &nbsp; and will keep the table cell full.
					table.setRemarks("  ");
				}
				if ( doFullTableMetadata ) {
					temp = rs.getString(1);
					if (!rs.wasNull()) {
						table.setCatalog(temp.trim());
					}
					temp = rs.getString(2);
					if (!rs.wasNull()) {
						table.setSchema(temp.trim());
					}
					temp = rs.getString(4);
					if (!rs.wasNull()) {
						table.setType(temp.trim());
					}
					try {
						temp = rs.getString(9);
						if (!rs.wasNull()) {
							table.setSelfRefColumn(temp.trim());
						}
					}
					catch ( Exception e ) {
						// Not all databases support
						idColumnSupported = false;
					}
					try {
						temp = rs.getString(10);
						if (!rs.wasNull()) {
							table.setSelfRefColumnHowCreated(temp.trim());
						}
					}
					catch ( Exception e ) {
						// Not all databases support
						idColumnSetterSupported = false;
					}
				}
			}
			tableList0.add(table);
		}
		catch (Exception e) {
			// continue getting the list of table names, but report the error.
		    Message.printWarning(3, routine, "Error getting list of table names (" + e + ").");
			Message.printWarning(3, routine, e);
		}
	} 
	try {	
		DMI.closeResultSet(rs);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	// Sort the list of table names in ascending order and also the remarks, ignoring case.
	int [] sortOrder = new int[tableNames.size()];
	tableNames = StringUtil.sortStringList(tableNames, StringUtil.SORT_ASCENDING, sortOrder, true, true);
	List<JdbcTableMetadata> tableList = new ArrayList<JdbcTableMetadata>(sortOrder.length);
	for ( int i = 0; i < sortOrder.length; i++ ) {
		tableList.add(tableList0.get(sortOrder[i]));
	}
	
	Message.printStatus(2, routine, "Read " + tableNames.size() + " tables from database.");

	// Remove the list of system tables for each kind of database (all database types have certain system tables)
	boolean isSQLServer = false;
	//String databaseEngine = dmi.getDatabaseEngine();
	int databaseEngineType = dmi.getDatabaseEngineType();
	Message.printStatus(2, routine, "Removing tables that should be skipped");
	String [] systemTablePatternsToRemove = DMIUtil.getSystemTablePatternsToRemove (databaseEngineType);
    for ( int i = 0; i < systemTablePatternsToRemove.length; i++ ) {
        StringUtil.removeMatching(tableNames,systemTablePatternsToRemove[i],true);
    }
	
	// Remove all the tables that were in the excludeTables parameter passed in to this method.
	if (excludeTables != null) {
		for ( String excludeTable : excludeTables ) {
			// Handle glob-style wildcards and protect other than *
			// Escape special characters that may occur in table names 
			String excludeTable2 = excludeTable.replace(".", "\\.").replace("*",".*").replace("$", "\\$");
			for ( int i = tableList.size() - 1; i >= 0; i-- ) {
				String table = tableList.get(i).getName();
				// Remove table name at end so loop works
				if ( table.matches(excludeTable2) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug(1,routine,"Removing table \"" + table + "\" from dictionary.");
					}
					tableList.remove(i);
				}
			}
		}
	}

	Message.printStatus(2, routine, "Printing table names and remarks");
	// Print out a table containing the names of all the tables 
	// that will be reported on as well as any table remarks for 
	// those tables.  Each table name will be a link to its detailed
	// column information later in the data dictionary.
	try {
		html.paragraph();
		html.heading(2, dbName + " Tables");

		//html.blockquoteStart();
		html.tableStart("border=2 cellspacing=0");
		html.tableRowStart("valign=top");
		html.tableRowStart("valign=top bgcolor=#CCCCCC");	
		html.tableHeader("Table Name");
		html.tableHeader("Remarks");
		if ( doFullTableMetadata ) {
			html.tableHeader("Catalog");
			html.tableHeader("Schema");
			html.tableHeader("Type");
			if ( idColumnSupported ) {
				html.tableHeader("ID Column");
			}
			if ( idColumnSetterSupported ) {
				html.tableHeader("ID Column Setter");
			}
		}
		html.tableRowEnd();
	
		for ( JdbcTableMetadata table : tableList ) {
			String name = table.getName();
			html.tableRowStart("valign=top");
			html.tableCellStart();
			html.linkStart("#Table:" + name);
			html.addLinkText(name);
			html.linkEnd();
			html.tableCellEnd();
			temp = table.getRemarks();
			if (temp.trim().equals("")) {
				temp = "    ";
			}
			html.tableCell(temp);
			if ( doFullTableMetadata ) {
				html.tableCell(table.getCatalog());
				html.tableCell(table.getSchema());
				html.tableCell(table.getType());
				if ( idColumnSupported ) {
					html.tableCell(table.getSelfRefColumn());
				}
				if ( idColumnSetterSupported ) {
					html.tableCell(table.getSelfRefColumnHowCreated());
				}
			}
			html.tableRowEnd();
		}

		html.tableEnd();
		//html.blockquoteEnd();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error writing list of tables (" + e + ").");
		Message.printWarning(2, routine, e);
	}

	// Format the key for table formats
	try {
		html.paragraph();
		html.heading(2, "Table Color Legend");
		html.paragraph();
		//html.blockquoteStart();

		html.tableStart("border=2 cellspacing=0");
		
		html.tableRowStart("valign=top bgcolor=#CCCCCC");
		html.tableHeader("Table Section");
		html.tableHeader("Formatting Style");
		html.tableRowEnd();
		
		html.tableRowStart("valign=top");
		html.tableCell("Column Names");
		html.tableCellStart("valign=top bgcolor=#CCCCCC");
		html.boldStart();
		html.addText("Bold text, gray background");
		html.boldEnd();
		html.tableCellEnd();
		html.tableRowEnd();

		html.tableRowStart("valign=top");
		html.tableCell("Primary Key Fields");
		html.tableCellStart("valign=top bgcolor=yellow");
		html.boldStart();
		html.addText("Bold text, yellow background");
		html.boldEnd();
		html.tableCellEnd();
		html.tableRowEnd();
		
		html.tableRowStart("valign=top");
		html.tableCell("Foreign Key Fields");
		html.tableCellStart("valign=top bgcolor=orange");
		html.addText("Orange background with Foreign Key Link field");
		html.tableCellEnd();
		html.tableRowEnd();

		html.tableRowStart("valign=top");
		html.tableCell("Other Fields");
		html.tableCell("Normal text, white background");
		html.tableRowEnd();

		html.tableEnd();

		//html.blockquoteEnd();
	}
	catch (Exception e) {
	    Message.printWarning(3, routine, "Error creating key for tables (" + e + ").");
		Message.printWarning(3, routine, e);
	}

	// Start the table detail section of the data dictionary.
	try {
		html.paragraph();
		html.heading(2, "Table Detail");
		html.paragraph();
		//html.blockquoteStart();
	}
	catch (Exception e) {
	    Message.printWarning(3,routine,"Error creating table detail heading (" + e + ").");
		Message.printWarning(3, routine, e);
	}
	
	Message.printStatus(2, routine, "Writing table details for " + tableList.size() + " tables");

	String primaryKeyField = null;
	String primaryKeyTable = null;
	String tableName = null;
	int tableCount = 0;
	for ( JdbcTableMetadata table : tableList ) {
		++tableCount;
		try {
			tableName = table.getName();
			Message.printStatus(1, routine, "Processing table \"" + tableName + "\" (" + tableCount + " of " + tableList.size() + ")" );
			html.anchor("Table:" + tableName);
			html.headingStart(3);
			html.addText(table.getName() + " - " + table.getRemarks() );

			for (int j = 0; j < referenceTables.size(); j++) {
				if (tableName.equalsIgnoreCase(referenceTables.get(j))) {
				   	html.addText("  ");
					html.link("#ReferenceTable:" + tableName, "(View Contents)");
					j = referenceTables.size() + 1;
				}
			}
			
			html.headingEnd(3);
			//html.blockquoteStart();

			// Get a list of all the table columns that are in the Primary key.
			ResultSet primaryKeysRS = null;
			List<String> primaryKeysV = null;
			int primaryKeysSize = 0;
			try {
				primaryKeysRS = metadata.getPrimaryKeys(null, null, tableName);
				primaryKeysV = new ArrayList<String>();
				while (primaryKeysRS.next()) {
					primaryKeysV.add(primaryKeysRS.getString(4));	
				}
				primaryKeysSize = primaryKeysV.size();
				DMI.closeResultSet(primaryKeysRS);
			}
			catch (Exception e) {
				// If an exception is thrown here, it is probably because the JDBC driver does not
				// support the "getPrimaryKeys" method.  
				// No problem, it will be treated as if there were no primary keys.
			    Message.printWarning(2,routine,"Error getting primary keys for table \"" + tableName +
			         "\" - not formatting primary keys (" + e + ").");
			}

			// Get a list of all the table columns that have foreign key references to other tables
			ResultSet foreignKeysRS = null;
			List<String> foreignKeyPriTablesV = null;
			List<String> foreignKeyPriFieldsV = null;
			List<String> foreignKeyFieldsV = null;
			int foreignKeysSize = 0;
			try {
				foreignKeysRS = metadata.getImportedKeys(null, null, tableName);
				foreignKeyPriFieldsV = new ArrayList<String>();
				foreignKeyPriTablesV = new ArrayList<String>();
				foreignKeyFieldsV = new ArrayList<String>();
				while (foreignKeysRS.next()) {
					foreignKeyPriTablesV.add(foreignKeysRS.getString(3));
					foreignKeyPriFieldsV.add(foreignKeysRS.getString(4));
					foreignKeyFieldsV.add(foreignKeysRS.getString(8));
				}
				foreignKeysSize = foreignKeyFieldsV.size();
				DMI.closeResultSet(foreignKeysRS);
			}
			catch (Exception e) {
			    Message.printWarning(2,routine,"Error getting primary keys for table \"" + tableName +
	                 "\" - not formatting foreign keys (" + e + ").");
			}

			// Get a list of all the fields that are exported so that foreign keys can link to them
			ResultSet exportedKeysRS = null;
			List<String> exportedKeysV = null;
			int exportedKeysSize = 0;
			try {
				exportedKeysRS = metadata.getExportedKeys(null, null, tableName);
				exportedKeysV = new ArrayList<String>();
				while (exportedKeysRS.next()) {
					exportedKeysV.add(exportedKeysRS.getString(4));
				}
				exportedKeysSize = exportedKeysV.size();
				DMI.closeResultSet(exportedKeysRS);
			}
			catch (Exception e) {
			    Message.printWarning(3, routine, "Error getting exported keys for table \"" + tableName + "\" (" + e + ").");
			    Message.printWarning(3, routine, e);
			}

			boolean exportedKey = false;
			boolean foreignKey = false;
			boolean primaryKey = false;
			int foreignKeyPos = -1;
			List<List<String>> tableColumnsMetadataList = new ArrayList<List<String>>();
			List<String> columnNames = new ArrayList<String>();

			// Next, get the actual column data for the current table.
			rs = metadata.getColumns(null, null, tableName, null);
			if (rs == null) {
				Message.printWarning(3, routine, "Error getting columns for \"" + tableName+"\" table.");
				continue;
			} 

			// Loop through each column and move all its important data into a list of lists.  This data will
			// be run through at least twice, and to do that
			// with a ResultSet would require several expensive opens and closes.

			String columnName = null;
			while (rs.next()) {
				exportedKey = false;
				foreignKey = false;
				primaryKey = false;
				foreignKeyPos = -1;
				List<String> columnMetadataList = new ArrayList<String>();
				for ( int ic = 0; ic < __POS_NUM; ic++ ) {
					columnMetadataList.add("");
				}
				if ( Message.isDebugOn ) {
				    // TODO SAM 2014-03-09 The following seems to mess up subsequent requests for data
				    // Subsequent requests give "no data" errors almost as if the first rs.getString() call
				    // advances the record
				    printColumnMetadata ( rs );
				}
				// Get the 'column name' and store it in list position __POS_COLUMN_NAME
				columnName = rs.getString(4);
				if (columnName == null) {
					columnName = " ";
				}
				else {
					columnName= columnName.trim();
				}
				columnMetadataList.set(__POS_COLUMN_NAME, columnName);
				columnNames.add(columnName);

				// Get whether this is a primary key or not and store either "TRUE" (for it being a 
				// primary key) or "FALSE" in list position __POS_IS_PRIMARY_KEY
				for (int j = 0; j < primaryKeysSize; j++) {
					if (columnName.equals(primaryKeysV.get(j).trim())) {
						primaryKey = true;		
						j = primaryKeysSize + 1;
					}
				}				
				if (primaryKey) {
					columnMetadataList.set(__POS_IS_PRIMARY_KEY,"TRUE");
				}
				else {
					columnMetadataList.set(__POS_IS_PRIMARY_KEY,"FALSE");
				}

				// Get the 'column type' and store it in list position __POS_COLUMN_TYPE
				temp = rs.getString(6);
				if (temp == null) {
					temp = "Unknown";
				} 
				else {
					temp = temp.trim();
				}
				columnMetadataList.set(__POS_COLUMN_TYPE,temp);

				// Get the 'column size' and store it in list position __POS_COLUMN_SIZE
				temp = rs.getString(7);
				columnMetadataList.set(__POS_COLUMN_SIZE,temp );
				
				// Get the 'column num digits' and store it in list position __POS_NUM_DIGITS
				temp = rs.getString(9);
				if (temp == null) {
					columnMetadataList.set(__POS_NUM_DIGITS,"0");
				}
				else {
					columnMetadataList.set(__POS_NUM_DIGITS,temp);
				}

				// Get whether the column is nullable and store it in list position __POS_NULLABLE
				temp = rs.getString(18);
				if (temp == null) {
					temp = "Unknown";
				}
				else {
					temp = temp.trim();
				}
				columnMetadataList.set(__POS_NULLABLE, temp );
				
				// Get the column remarks and store them in list position __POS_REMARKS
				if (isSQLServer) {
	                temp = rs.getString(12);
					//columnData.set(__POS_REMARKS,getSQLServerColumnComment(dmi, tableName, columnName));
				} 
				else {
					temp = rs.getString(12);
				}
				if (temp == null) {
					temp = "   ";
				} 
				else {
					temp = temp.trim();
				}
				columnMetadataList.set(__POS_REMARKS,temp);
				
				// Get whether the column is exported for foreign keys to connect to and store it
				// in Vector position __POS_EXPORTED as either "TRUE" or "FALSE"
				for (int j = 0; j < exportedKeysSize; j++) {
					if (columnName.equals(exportedKeysV.get(j).trim())) {
						exportedKey = true;		
						j = exportedKeysSize + 1;
					}
				}				
				if (exportedKey) {
					columnMetadataList.set(__POS_EXPORTED,"TRUE");
				}
				else {
					columnMetadataList.set(__POS_EXPORTED,"FALSE");
				}

				// Get whether the column is a foreign key field and store it in Vector position 
				// __POS_FOREIGN as either "TRUE" or "FALSE"

				// Additionally, set the table of the primary key to which the foreign key connects as
				// Vector position __POS_PRIMARY_TABLE.  
				// If not a foreign key, that position will be null

				// Set the field of the primary key to which the foreign key connects as Vector position
				// __POS_PRIMARY_FIELD.  If not a foreign key, that position will be null.

				for (int j = 0; j < foreignKeysSize; j++) {
					if (columnName.equals( foreignKeyFieldsV.get(j).trim())) {
						foreignKey = true;		
						foreignKeyPos = j; 
						j = foreignKeysSize + 1;
					}
				}				
				if (foreignKey) {
					columnMetadataList.set(__POS_FOREIGN,"TRUE");
					columnMetadataList.set(__POS_PRIMARY_TABLE,foreignKeyPriTablesV.get(foreignKeyPos));
					columnMetadataList.set(__POS_PRIMARY_FIELD,foreignKeyPriFieldsV.get(foreignKeyPos));
				}
				else {
					columnMetadataList.set(__POS_FOREIGN,"FALSE");
					columnMetadataList.set(__POS_PRIMARY_TABLE,null);
					columnMetadataList.set(__POS_PRIMARY_FIELD,null);
				}
				
				tableColumnsMetadataList.add(columnMetadataList);		
			}

			try {	
				DMI.closeResultSet(rs);
			}
			catch (Exception e) {
				Message.printWarning(2, routine, e);
			}
		
			// Create the table and the table header for displaying the table column information.
			html.tableStart("border=2 cellspacing=0");
			html.tableRowStart("valign=top bgcolor=#CCCCCC");
			html.tableHeader("Column Name");
			html.tableHeader("Remarks");
			html.tableHeader("Column Type");
			html.tableHeader("Allow Null");
			if (foreignKeysSize > 0) {
				html.tableHeader("Foreign Key Link");
			}
			html.tableRowEnd();			

			// Next, an alphabetized list of the column names in the table will be compiled.  This will be used
			// to display columns in the right sorting order.
			int numColumns = columnNames.size();
			int[] order = new int[numColumns];
			// FIXME SAM 2014-03-08 set the order to the original for now
			// Some code most have been lost here
			for ( int j = 0; j < numColumns; j++ ) {
			    order[j] = j;
			}
			List[] sortedVectors = new List[numColumns];
			for (int j = 0; j < numColumns; j++) {
				sortedVectors[j] = (List<String>)tableColumnsMetadataList.get(order[j]);
			}
			
			// Now that the sorted order of the column names (and the lists of data) is known,
			// loop through the data lists looking for columns which are in 
			// the Primary key.  They will be displayed in bold face font with a yellow background.
			for (int j = 0; j < numColumns; j++) {
				List<String> columnData = sortedVectors[j];
				temp = null;

				temp = columnData.get(__POS_IS_PRIMARY_KEY);

				if (temp.equals("TRUE")) {
					html.tableRowStart("valign=top bgcolor=yellow");
					
					// display the column name
					temp = columnData.get(__POS_COLUMN_NAME);
					html.tableCellStart();
					html.boldStart();

					temp2 = columnData.get(__POS_EXPORTED);
					if (temp2.equals("TRUE")) {
						html.anchor("Table:" + tableName + "." + temp);
					}
										
					html.addText(temp);
					html.boldEnd();
					html.tableCellEnd();

					// display the remarks
					temp = columnData.get(__POS_REMARKS);
					html.tableCellStart();
					html.boldStart();
					writeRemarks(html,temp,newline,surroundWithPre,encodeHtmlChars);
					html.boldEnd();
					html.tableCellEnd();

					// display the column type
					temp = columnData.get(__POS_COLUMN_TYPE);
					Message.printStatus(2, routine, "Table \"" + tableName + "\" column \"" + columnName + "\" type is " + temp );
					if (temp.equalsIgnoreCase("real")) {
						temp = temp + "(" + columnData.get(__POS_COLUMN_SIZE) + ", " + columnData.get(__POS_NUM_DIGITS) + ")";
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
						temp = temp + "(" + columnData.get(__POS_COLUMN_SIZE) + ")";
					}
					html.tableCellStart();
					html.boldStart();
					html.addText(temp);
					html.boldEnd();
					html.tableCellEnd();

					// display whether it's nullable
					temp = columnData.get(__POS_NULLABLE);
					html.tableCellStart();
					html.boldStart();
					html.addText(temp);
					html.boldEnd();
					html.tableCellEnd();

					temp = columnData.get(__POS_FOREIGN);
					if (temp.equals("TRUE")) {
						html.tableCellStart();
						primaryKeyTable = columnData.get(__POS_PRIMARY_TABLE);
						primaryKeyField = columnData.get(__POS_PRIMARY_FIELD);

						html.link("#Table:" + primaryKeyTable,	primaryKeyTable);
						html.addLinkText(".");
						html.link("#Table:" + primaryKeyTable + "." + primaryKeyField, primaryKeyField);
						html.tableCellEnd();
					}
					else if (foreignKeysSize > 0) {
						html.tableCell("  ");
					}

					html.tableRowEnd();
				}
			}

			// Now do the same thing for the other fields, the non-primary key fields.  
			for (int j = 0; j < numColumns; j++) {
				List<String> column = sortedVectors[j];
				String isPrimaryKey = column.get(__POS_IS_PRIMARY_KEY);

				if (isPrimaryKey.equals("FALSE")) {
					String isForeignKey = column.get(__POS_FOREIGN);
					if (isForeignKey.equals("TRUE")) {
						html.tableRowStart( "valign=top bgcolor=orange");
					}
					else {
						html.tableRowStart( "valign=top");
					}
					
					// display the column name
					String columnName2 = column.get(__POS_COLUMN_NAME);
					html.tableCellStart();
					html.addText(columnName2);
					html.tableCellEnd();

					// display the remarks
					String remarks = column.get(__POS_REMARKS);
					html.tableCellStart();
					writeRemarks(html,remarks,newline,surroundWithPre,encodeHtmlChars);
					html.tableCellEnd();

					// display the column type
					String columnType = column.get(__POS_COLUMN_TYPE);
					Message.printStatus(2, routine, "Table \"" + tableName + "\" column \"" + columnName + "\" type is " + columnType );
					if (columnType.equalsIgnoreCase("real")) {
						columnType = columnType + "(" + column.get(__POS_COLUMN_SIZE)
						+ ", " + column.get(__POS_NUM_DIGITS) + ")";
					}
					else if (columnType.equalsIgnoreCase("float")||
						(columnType.equalsIgnoreCase("double"))||
						(columnType.equalsIgnoreCase("smallint"))||
						(columnType.equalsIgnoreCase("int"))||
						(columnType.equalsIgnoreCase("integer"))||
						(columnType.equalsIgnoreCase("counter"))||
						(columnType.equalsIgnoreCase("datetime"))) {
						// TODO smalers 2018-09-21 not sure why not just print the column size always as below
					}
					else {
						columnType = columnType + "(" + column.get(__POS_COLUMN_SIZE) + ")";
					}
					if ( columnType.startsWith("_") ) {
						// Used for arrays in PostgreSQL and maybe others
						columnType = columnType + "[...]"; // TODO smalers 2018-09-21 need to get actual dimension
					}
					html.tableCellStart();
					html.addText(columnType);
					html.tableCellEnd();

					// display whether it's nullable
					String isNullable = column.get(__POS_NULLABLE);
					html.tableCellStart();
					html.addText(isNullable);
					html.tableCellEnd();

					if (isForeignKey.equals("TRUE")) {
						html.tableCellStart();
						primaryKeyTable = column.get(__POS_PRIMARY_TABLE);
						primaryKeyField = column.get(__POS_PRIMARY_FIELD);

						html.link("#Table:" + primaryKeyTable, primaryKeyTable);
						html.addLinkText(".");
						html.link("#Table:" + primaryKeyTable + "." + primaryKeyField, primaryKeyField);
						html.tableCellEnd();
					}
					else if (foreignKeysSize > 0) {
						html.tableCell("  ");
					}					
					
					html.tableRowEnd();
				}
			}			

			// Close the table, insert a paragraph break, and get ready to do it again for the next table.
			html.tableEnd();
			//html.blockquoteEnd();
			html.paragraph();
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error printing column information for table \"" + tableName + "\" (" + e + ").");
			Message.printWarning(2, routine, e);
		}
	}

	Message.printStatus(2, routine, "Listing stored procedures (not implemented yet)");
	// List stored procedures...
	// Not yet done
	// TODO (JTS - 2003-04-22) does this need to be done?

	// Now list the contents of the reference tables.  These tables are dumped out in their entirety. 

	if (referenceTables.size() > 0) {
		Message.printStatus(2, routine, "Printing contents of reference tables");
		try {
			html.paragraph();
			html.heading(2, "Reference Table Contents");
			html.paragraph();
			//html.blockquoteStart();
		}
		catch (Exception e) {
		    Message.printWarning(3, routine, "Error printing header for reference table contents (" + e + ").");
			Message.printWarning(3, routine, e);
		}
	}

	String ldelim = dmi.getFieldLeftEscape();
	String rdelim = dmi.getFieldRightEscape();

	// Loop through each of the tables that was passed in to the method
	// in the referenceTables array and get a list of its column names
	// and then print out all of its data in one table.
	String refTableName;
	for (int i = 0; i < referenceTables.size(); i++) {
		refTableName = referenceTables.get(i);
	
		try {	
			rs = metadata.getColumns(null, null, refTableName, null);
			if (rs == null) {
				Message.printWarning(2, routine, "Error getting columns for \"" + refTableName + "\" table.");
				continue;
			} 
			html.anchor("ReferenceTable:" + refTableName);
			html.headingStart(3);
			html.addText(refTableName + "  ");
			html.link("#Table:" + refTableName, "(View Definition)");
			html.headingEnd(3);
			//html.blockquoteStart();

			List<String> columnNames = new ArrayList<String>();
			while (rs.next()) {
			    columnNames.add(rs.getString(4).trim());
			}
			DMI.closeResultSet(rs);

			// create a SQL String that will query the appropriate table for all data in the found fields.
			// This is used because perhaps in the future it might be
			// desire to limit the fields from which data is displayed.
			String sql = "SELECT ";
			int j = 0;
			for (j = 0; j < columnNames.size(); j++) {
				if (j > 0) {
					sql += ", ";
				}
				sql += ldelim + columnNames.get(j) + rdelim;
			}
			sql += " FROM " + ldelim + refTableName + rdelim + " ORDER BY ";

			for (j = 0; j < columnNames.size(); j++) {
				if (j > 0) {
					sql += ", ";
				}
				sql += ldelim + columnNames.get(j) + rdelim;
			}

			// j will be greater than 0 if there were any columns in the list of columnNames for the table.
			// It will equal 0 if the table name could not be found or was null.
			if (j > 0) {
				rs = dmi.dmiSelect(sql);

				// Create the header for the reference table
				html.tableStart("border=2 cellspacing=0");
				html.tableRowStart( "valign=top bgcolor=#CCCCCC");
				
				for (j = 0; j < columnNames.size(); j++) {
					html.tableHeader(columnNames.get(j));
				}
				html.tableRowEnd();

				// Start dumping out all the data in the reference table.  The data is retrieved as
				// Strings, which seems to work fine.
				String tableCellValue;
				while (rs.next()) {
					html.tableRowStart("valign=top");
					for (j = 0; j < columnNames.size();j++){
						tableCellValue = rs.getString(j+1);
						if (tableCellValue == null) {
							tableCellValue = "NULL";
						}
						html.tableCell(tableCellValue);
					}
					html.tableRowEnd();
				}
				html.tableEnd();
				DMI.closeResultSet(rs);
			}
			//html.blockquoteEnd();
			html.paragraph();		
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "Error dumping reference table data (" + e + ").");
			Message.printWarning(2, routine, e);
		}
	}

	Message.printStatus(2, routine, "Writing HTML file");
	// Finally, try to close and write out the HTML file.
	try {
		html.closeFile();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error closing the HTML file (" + e + ").");
		Message.printWarning(2, routine, e);
	}		
	Message.printStatus(2, routine, "Done creating data dictionary");
}

/**
Print the column metadata from a result set.
*/
private static void printColumnMetadata ( ResultSet rs )
{
    try {
        Message.printStatus(2,"","TABLE_CAT=" + rs.getString(1) );
        Message.printStatus(2,"","TABLE_SCHEM=" + rs.getString(2) );
        Message.printStatus(2,"","TABLE_NAME=" + rs.getString(3) );
        Message.printStatus(2,"","COLUMN_NAME=" + rs.getString(4) );
        Message.printStatus(2,"","DATA_TYPE=" + rs.getInt(5) );
        Message.printStatus(2,"","TYPE_NAME=" + rs.getString(6) );
        Message.printStatus(2,"","COLUMN_SIZE=" + rs.getInt(7) );
        Message.printStatus(2,"","BUFFER_LENGTH=" + rs.getInt(8) );
        Message.printStatus(2,"","DECIMAL_DIGITS=" + rs.getInt(9) );
        Message.printStatus(2,"","NUM_PREC_RADIX=" + rs.getInt(10) );
        Message.printStatus(2,"","NULLABLE=" + rs.getInt(11) );
        Message.printStatus(2,"","REMARKS=" + rs.getString(12) );
        Message.printStatus(2,"","COLUMN_DEF=" + rs.getString(13) );
        Message.printStatus(2,"","SQL_DATA_TYPE=" + rs.getInt(14) );
        Message.printStatus(2,"","SQL_DATETIME_SUB=" + rs.getInt(15) );
        Message.printStatus(2,"","CHAR_OCTET_LENGTH=" + rs.getInt(16) );
        Message.printStatus(2,"","ORDINAL_POSITION=" + rs.getInt(17) );
        Message.printStatus(2,"","IS_NULLABLE=" + rs.getString(18) );
        Message.printStatus(2,"","SCOPE_CATLOG=" + rs.getString(19) );
        Message.printStatus(2,"","SCOPE_SCHEMA=" + rs.getString(20) );
        Message.printStatus(2,"","SCOPE_TABLE=" + rs.getString(21) );
        Message.printStatus(2,"","SOURCE_DATA_TYPE=" + rs.getString(22) );
        Message.printStatus(2,"","IS_AUTOINCREMENT=" + rs.getString(23) );
    }
    catch ( Exception e ) {
        // Ignore, most likely something like Microsoft Access not supporting indices correctly
    }
}

private void writeRemarks ( HTMLWriter html, String temp, String newline, boolean surroundWithPre, boolean encodeHtmlChars )
throws Exception
{
	String tempUpper = temp.toUpperCase();
	if ( tempUpper.startsWith("<HTML>") && tempUpper.endsWith("</HTML>") ) {
		// Pass the text through without additional formatting, but first remove the HTML tags.
		html.write(temp.substring(6,temp.length()-12));
	}
	else if ( surroundWithPre ) {
		html.write("<pre>");
		html.write(temp);
		html.write("</pre>");
	}
	else if ( encodeHtmlChars ) {
		html.addText(temp);
	}
	else {
		// Just write the text
		if ( (newline != null) && !newline.isEmpty() ) {
			temp = temp.replace("\n",newline);							
		}
		html.write(temp);
	}
}

}
