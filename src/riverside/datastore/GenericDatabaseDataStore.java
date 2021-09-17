// GenericDatabaseDataStore - generic datastore for a database connection

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

package riverside.datastore;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import RTi.DMI.AbstractDatabaseDataStore;
import RTi.DMI.DMI;
import RTi.DMI.DMISelectStatement;
import RTi.DMI.DMIUtil;
import RTi.DMI.GenericDMI;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;

/**
Data store for Generic database, to allow table/view queries.
This class maintains the database connection information in a general way.
@author sam
*/
public class GenericDatabaseDataStore extends AbstractDatabaseDataStore
{

/**
Datastore configuration properties that map the database time series metadata table/view to the datastore.
*/
public static final String TS_META_TABLE_PROP = "TimeSeriesMetadataTable";
public static final String TS_META_TABLE_LOCTYPE_COLUMN_PROP = "TimeSeriesMetadataTable_LocationTypeColumn";
public static final String TS_META_TABLE_LOCATIONID_COLUMN_PROP = "TimeSeriesMetadataTable_LocationIdColumn";
public static final String TS_META_TABLE_DATASOURCE_COLUMN_PROP = "TimeSeriesMetadataTable_DataSourceColumn";
public static final String TS_META_TABLE_DATATYPE_COLUMN_PROP = "TimeSeriesMetadataTable_DataTypeColumn";
public static final String TS_META_TABLE_DATAINTERVAL_COLUMN_PROP = "TimeSeriesMetadataTable_DataIntervalColumn";
public static final String TS_META_TABLE_SCENARIO_COLUMN_PROP = "TimeSeriesMetadataTable_ScenarioColumn";
public static final String TS_META_TABLE_DESCRIPTION_COLUMN_PROP = "TimeSeriesMetadataTable_DescriptionColumn";
public static final String TS_META_TABLE_UNITS_COLUMN_PROP = "TimeSeriesMetadataTable_DataUnitsColumn";
public static final String TS_META_TABLE_ID_COLUMN_PROP = "TimeSeriesMetadataTable_MetadataIdColumn";

/*
Property that indicates filter, will be followed by ".1", ".2", etc.
*/
public static final String TS_META_TABLE_FILTER_PROP = "TimeSeriesMetadataTable_MetadataFilter";

public static final String TS_DATA_TABLE_PROP = "TimeSeriesDataTable";
public static final String TS_DATA_TABLE_METAID_COLUMN_PROP = "TimeSeriesDataTable_MetadataIdColumn";
public static final String TS_DATA_TABLE_DATETIME_COLUMN_PROP = "TimeSeriesDataTable_DateTimeColumn";
public static final String TS_DATA_TABLE_VALUE_COLUMN_PROP = "TimeSeriesDataTable_ValueColumn";
public static final String TS_DATA_TABLE_FLAG_COLUMN_PROP = "TimeSeriesDataTable_FlagColumn";

/**
Hashtable that stores list of data types for different time series metadata inputs.
The key is a string consisting of locType, locID, dataSource, interval, scenario as passed to getTimeSeriesMetaDataTypeList.
*/
private Hashtable<String,List<String>> timeSeriesMetaHash = new Hashtable<String,List<String>>();

/**
Database metadata, stored here to speed up database interactions.
*/
private DatabaseMetaData databaseMetadata = null;
    
/**
Construct a data store given a DMI instance, which is assumed to be open.
@param name identifier for the data store
@param description name for the data store
@param dmi DMI instance to use for the data store.
*/
public GenericDatabaseDataStore ( String name, String description, DMI dmi )
{
    setName ( name );
    setDescription ( description );
    setDMI ( dmi );
    // Rely on other authentication to prevent writing.
    // TODO SAM 2013-02-26 Perhaps use a database configuration file property to control
    if ( dmi != null ) {
    	dmi.setEditable ( true );
    }
}
    
/**
Factory method to construct a data store connection from a properties file.
@param filename name of file containing property strings
*/
public static GenericDatabaseDataStore createFromFile ( String filename )
throws IOException, Exception
{
    // Read the properties from the file.
    PropList props = new PropList ("");
    props.setPersistentName ( filename );
    props.readPersistent ( false );
    // Set a property for the configuration filename because it is used later.
    props.set("DataStoreConfigFile="+filename);
    String name = IOUtil.expandPropertyForEnvironment("Name",props.getValue("Name"));
    String connectionProperties = IOUtil.expandPropertyForEnvironment("ConnectionProperties",props.getValue("ConnectionProperties"));
    String description = IOUtil.expandPropertyForEnvironment("Description",props.getValue("Description"));
    String databaseEngine = IOUtil.expandPropertyForEnvironment("DatabaseEngine",props.getValue("DatabaseEngine"));
    String databaseServer = IOUtil.expandPropertyForEnvironment("DatabaseServer",props.getValue("DatabaseServer"));
    String databaseName = IOUtil.expandPropertyForEnvironment("DatabaseName",props.getValue("DatabaseName"));
    String databasePort = IOUtil.expandPropertyForEnvironment("DatabasePort",props.getValue("DatabasePort"));
    int port = -1;
    if ( (databasePort != null) && !databasePort.equals("") ) {
        try {
            port = Integer.parseInt(databasePort);
        }
        catch ( NumberFormatException e ) {
            port = -1;
        }
    }
    String odbcName = props.getValue ( "OdbcName" );
    String systemLogin = IOUtil.expandPropertyForEnvironment("SystemLogin",props.getValue("SystemLogin"));
    String systemPassword = IOUtil.expandPropertyForEnvironment("SystemPassword",props.getValue("SystemPassword"));

    // Additionally, expand the password property since it might be specified in a file:
    // - for example, ${pgpass:password} is used with PostgreSQL
    systemPassword = DMI.expandDatastoreConfigurationProperty(props, "SystemPassword", systemPassword);
    
    // Get the properties and create an instance.
    GenericDMI dmi = null;
    if ( (odbcName != null) && !odbcName.isEmpty() ) {
        // An ODBC connection is configured so use it.
        dmi = new GenericDMI (
            databaseEngine, // Needed for internal SQL handling.
            odbcName, // Must be configured on the machine.
            systemLogin, // OK if null - use read-only guest.
            systemPassword ); // OK if null - use read-only guest.
    }
    else {
        // Use the parts and create the connection string on the fly.
        dmi = new GenericDMI(
        	databaseEngine,
        	databaseServer,
        	databaseName,
        	port,
        	systemLogin,
        	systemPassword );
    }
    // Set additional connection properties if specified, for example the login timeout:
    // - the properties will depend on the database but usually is ?prop1=value1&prop2=value2
    dmi.setAdditionalConnectionProperties(connectionProperties);
    dmi.open();
    GenericDatabaseDataStore ds = new GenericDatabaseDataStore( name, description, dmi );
    // Save all the properties generically for use later.  This defines tables for time series meta and data mapping.
    ds.setProperties(props);
    return ds;
}

/**
Return the SQL column type.
*/
private int getColumnType ( DatabaseMetaData metadata, String tableName, String columnName )
{   String routine = "GenericDatabaseDataStore.getColumnType", message;
    ResultSet rs;
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
        throw new RuntimeException ( message );
    }
    // Now check for the column by looping through result set...

    String s;
    int i;
    int colType = -1;
    try {
        while ( rs.next() ) {
            // The column name is field 4, data type field 5...
            s = rs.getString(4);
            if ( !rs.wasNull() ) {
                s = s.trim();
                if ( s.equalsIgnoreCase(columnName) ) {
                    i = rs.getInt(5);
                    if ( !rs.wasNull() ) {
                        colType = i;
                    }
                    break;
                }
            }
        }
    }
    catch ( SQLException e ) {      
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return colType;
}

/**
Return the database metadata associated with the database.  If metadata have not been retrieved, retrieve and save.
*/
private DatabaseMetaData getDatabaseMetaData ()
throws SQLException
{
    if ( this.databaseMetadata == null ) {
        // Metadata have not previously been retrieved so get now
        this.databaseMetadata = getDMI().getConnection().getMetaData();
    }
    return this.databaseMetadata;
}

/**
Get a datastore property for a specific table.  This requires that property be coded as:
<pre>
PropName = "Table1:PropValue,Table2:PropValue"
</pre>
*/
private String getPropertyForTable ( String propName, String table )
{
    // Get the full property
    String propVal = getProperty ( propName );
    if ( propVal == null || (propVal.indexOf(":") < 0) ) {
        // Return as is
        return propVal;
    }
    // Parse, multiple items in list separated by ,
    String [] parts = propVal.split(",");
    String [] parts2;
    for ( int i = 0; i < parts.length; i++ ) {
        parts2 = parts[i].split(":");
        if ( parts2[0].trim().equalsIgnoreCase(table) ) {
            return parts2[1].trim();
        }
    }
    return null;
}

/**
Get data type strings for the datastore, if time series support is configured.
This method checks for a cached result and if found, returns it.  Otherwise, it calls the read method of similar name and caches
the result.
@param includeNotes if true, include notes in the data type strings, like "DataType - Note"
(currently does nothing)
@param locType location type to use as filter (ignored if blank or null)
@param locID location ID to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of data type strings by making a unique query of the 
*/
public List<String> getTimeSeriesMetaDataTypeList ( boolean includeNotes,
    String locType, String locID, String dataSource, String interval, String scenario )
{
    // Only cache certain combinations as there is a trade-off between memory and performance
    // TODO SAM 2013-08-28 Evaluate what gets cached and what not.
    // For now only cache all nulls since that is the main choice
    // TODO SAM 2013-08-28 Remove logging messages if code works OK
    //Message.printStatus(2, "", "Getting data types for " + locType + "." + locID + "." + dataSource + "." + interval + "." + scenario);
    List<String> dataTypeList = null;
    if ( (locType == null) && (locID == null) && (dataSource == null) && (interval == null) && (scenario == null) ) {
        //Message.printStatus(2, "", "Looking up data from hashtable");
        // Use the hashtable
        String key = locType + "." + locID + "." + dataSource + "." + interval + "." + scenario;
        Object o = this.timeSeriesMetaHash.get(key);
        if ( o == null ) {
            // Not in the hashtable so read the data and set in the hastable
            //Message.printStatus(2, "", "Nothing in hashtable...reading");
            dataTypeList = readTimeSeriesMetaDataTypeList ( includeNotes, locType, locID, dataSource, interval, scenario );
            //Message.printStatus(2, "", "Read " + dataTypeList.size() + " data types...saving to hashtable");
            this.timeSeriesMetaHash.put(key,dataTypeList);
        }
        else {
        	@SuppressWarnings("unchecked")
			List<String> dataTypeList0 = (List<String>)o;
            dataTypeList = dataTypeList0;
            //Message.printStatus(2, "", "Got " + dataTypeList.size() + " data types from hashtable");
        }
        return dataTypeList;
    }
    else {
        dataTypeList = readTimeSeriesMetaDataTypeList ( includeNotes, locType, locID, dataSource, interval, scenario );
        //Message.printStatus(2, "", "Got " + dataTypeList.size() + " data types from read (but not saving to hashtable)");
        return dataTypeList;
    }
}

/**
Create a list of where clauses give an InputFilter_JPanel.  The InputFilter
instances that are managed by the InputFilter_JPanel must have been defined with
the database table and field names in the internal (non-label) data.
@return a list of where clauses, each of which can be added to a DMI statement.
@param dmi The DMI instance being used, which may be checked for specific formatting.
@param panel The InputFilter_JPanel instance to be converted.  If null, an empty list will be returned.
*/
private List<String> getWhereClausesFromInputFilter ( DMI dmi, InputFilter_JPanel panel ) 
{
    // Loop through each filter group.  There will be one where clause per filter group.

    if (panel == null) {
        return new ArrayList<String>();
    }

    int nfg = panel.getNumFilterGroups ();
    InputFilter filter;
    List<String> whereClauses = new ArrayList<String>();
    String whereClause = ""; // A where clause that is being formed.
    for ( int ifg = 0; ifg < nfg; ifg++ ) {
        filter = panel.getInputFilter ( ifg );  
        whereClause  = DMIUtil.getWhereClauseFromInputFilter(dmi, filter,panel.getOperator(ifg), true);
        if (whereClause != null) {
            whereClauses.add(whereClause );
        }
    }
    return whereClauses;
}

/**
Create a where string given an InputFilter_JPanel.  The InputFilter
instances that are managed by the InputFilter_JPanel must have been defined with
the database table and field names in the internal (non-label) data.
@return a list of where clauses as a string, each of which can be added to a DMI statement.
@param dmi The DMI instance being used, which may be checked for specific formatting.
@param panel The InputFilter_JPanel instance to be converted.  If null, an empty list will be returned.
@param tableAndColumnName the name of the table for which to get where clauses in format TableName.ColumnName.
@param useAnd if true, then "and" is used instead of "where" in the where strings.  The former can be used
with "join on" SQL syntax.
@param addNewline if true, add a newline if the string is non-blank - this simply helps with formatting of
the big SQL, so that logging has reasonable line breaks
*/
private String getWhereClauseStringFromInputFilter ( DMI dmi, InputFilter_JPanel panel, String tableAndColumnName,
   boolean addNewline )
{
    List<String> whereClauses = getWhereClausesFromInputFilter ( dmi, panel );
    StringBuffer whereString = new StringBuffer();
    for ( String whereClause : whereClauses ) {
        //Message.printStatus(2,"","Comparing where clause \"" + whereClause + "\" to \"" + tableAndColumnName + "\"");
        if ( whereClause.toUpperCase().indexOf(tableAndColumnName.toUpperCase()) < 0 ) {
            // Not for the requested table so don't include the where clause
            //Message.printStatus(2, "", "Did not match");
            continue;
        }
        //Message.printStatus(2, "", "Matched");
        if ( (whereString.length() > 0)  ) {
            // Need to concatenate
            whereString.append ( " and ");
        }
        whereString.append ( "(" + whereClause + ")");
    }
    if ( addNewline && (whereString.length() > 0) ) {
        whereString.append("\n");
    }
    return whereString.toString();
}

/**
Indicate whether properties have been defined to allow querying time series from the datastore.
Only the minimal properties are checked.
@param checkDatabase check to see whether tables and columns mentioned in the configuration actually
exist in the database
@return true if the datastore has properties defined to support reading time series
*/
public boolean hasTimeSeriesInterface ( boolean checkDatabase )
{   String routine = "GenericDatabaseDataStore.hasTimeSeriesInterface";
    DatabaseMetaData meta = null;
    if ( checkDatabase ) {
        try {
            meta = getDatabaseMetaData();
        }
        catch ( Exception e ) {
            return false;
        }
    }
    String table, column;
    try {
        // Must have metadata table
        table = getProperty(TS_META_TABLE_PROP);
        if ( table == null ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" does not have configuration property \"" + TS_META_TABLE_PROP + "\"." );
            return false;
        }
        else if ( checkDatabase && !DMIUtil.databaseHasTable(meta, table) ) {
             Message.printStatus(2,routine,"Datastore \"" + getName() +
                 "\" does not have table/view \"" + table + "\"." );
             return false;
        }
        // Must have location, data type, and interval columns
        column = getProperty(TS_META_TABLE_LOCATIONID_COLUMN_PROP);
        if ( column == null ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" does not have configuration property \"" + TS_META_TABLE_LOCATIONID_COLUMN_PROP + "\"." );
            return false;
        }
        else if ( checkDatabase && !DMIUtil.databaseTableHasColumn(meta, table, column) ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" table/view \"" + table + "\" does not have column \"" + column + "\"." );
            return false;
        }
        column = getProperty(TS_META_TABLE_DATASOURCE_COLUMN_PROP);
        if ( column == null ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" does not have configuration property \"" + TS_META_TABLE_DATASOURCE_COLUMN_PROP + "\"." );
            return false;
        }
        else if ( checkDatabase && !DMIUtil.databaseTableHasColumn(meta, table, column) ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" table/view \"" + table + "\" does not have column \"" + column + "\"." );
            return false;
        }
        column = getProperty(TS_META_TABLE_DATAINTERVAL_COLUMN_PROP);
        if ( column == null ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" does not have configuration property \"" + TS_META_TABLE_DATAINTERVAL_COLUMN_PROP + "\"." );
            return false;
        }
        else if ( checkDatabase && !DMIUtil.databaseTableHasColumn(meta, table, column) ) {
            Message.printStatus(2,routine,"Datastore \"" + getName() +
                "\" table/view \"" + table + "\" does not have column \"" + column + "\"." );
            return false;
        }
    }
    catch ( Exception e ) {
        // Could not get database information
        return false;
    }
    return true;
}

/**
Read a time series from the datastore.
*/
public TS readTimeSeries ( String tsidentString, DateTime inputStart, DateTime inputEnd, boolean readData )
{   String routine = "GenericDatabaseDataStore.readTimeSeries", message;
    TS ts = null;
    TSIdent tsident = null;
    try {
        tsident = TSIdent.parseIdentifier(tsidentString);
    }
    catch ( Exception e ) {
        message = "Time series identifier \"" + tsidentString + "\" is invalid (" + e + ")";
        Message.printWarning(3,routine,message);
        throw new RuntimeException ( message );
    }
    // Get the time series metadata record
    StopWatch metaTimer = new StopWatch();
    metaTimer.start();
    TimeSeriesMeta tsMeta = readTimeSeriesMeta ( tsident.getLocationType(), tsident.getLocation(),
        tsident.getSource(), tsident.getType(), tsident.getInterval(), tsident.getScenario() );
    metaTimer.stop();
    if ( tsMeta == null ) {
        return null;
    }
    // Create the time series
    double missing = Double.NaN;
    try {
        ts = TSUtil.newTimeSeries(tsident + "~" + getName(), true);
        ts.setIdentifier(tsident);
        ts.setDataUnits ( tsMeta.getUnits() );
        ts.setDataUnitsOriginal ( tsMeta.getUnits() );
        ts.setDescription( tsMeta.getDescription() );
        ts.setMissing(missing);
    }
    catch ( Exception e ) {
        Message.printWarning(3,routine,"Error creating time series (" + e + ")." );
        return null;
    }
    if ( !readData ) {
        return ts;
    }
    // Read the time series data
    DMI dmi = getDMI();
    StopWatch dataTimer = new StopWatch();
    dataTimer.start();
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String dataTable = getProperty ( GenericDatabaseDataStore.TS_DATA_TABLE_PROP );
    //Message.printStatus(2, routine, "Data table = \"" + dataTable + "\"");
    if ( dataTable != null ) {
        // Table name may contain formatting like %I, etc.
        dataTable = ts.formatLegend(dataTable);
    }
    String dtColumn = getPropertyForTable ( GenericDatabaseDataStore.TS_DATA_TABLE_DATETIME_COLUMN_PROP, dataTable );
    boolean dateTimeInt = false; // true=integer year, false=timestamp
    int dtColumnType = -1;
    try {
        dtColumnType = getColumnType(getDatabaseMetaData(), dataTable, dtColumn);
        if ( (dtColumnType == Types.TIMESTAMP) || (dtColumnType == Types.DATE)) {
            dateTimeInt = false;
        }
        else if ( (dtColumnType == Types.BIGINT) || (dtColumnType == Types.INTEGER) || (dtColumnType == Types.SMALLINT) ) {
            dateTimeInt = true;
        }
        else {
            message = "SQL column type " + dtColumnType + " for \"" + dtColumn +
                "\" is not supported - don't understand date/time.";
            Message.printWarning(3, routine, message);
            throw new RuntimeException ( message );
        }
    }
    catch ( SQLException e ) {
        message = "Cannot determine column type for \"" + dtColumn + "\" don't understand date/time (" + e + ").";
        Message.printWarning(3, routine, message);
        throw new RuntimeException ( message );
    }
    String valColumn = getProperty ( GenericDatabaseDataStore.TS_DATA_TABLE_VALUE_COLUMN_PROP );
    String flagColumn = getProperty ( GenericDatabaseDataStore.TS_DATA_TABLE_FLAG_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_DATA_TABLE_METAID_COLUMN_PROP );
    ss.addTable(dataTable);
    ss.addField(dtColumn);
    ss.addField(valColumn);
    if ( flagColumn != null ) {
        ss.addField(flagColumn);
    }
    ss.addOrderByClause(dtColumn);
    try {
        ss.addWhereClause(idColumn + " = " + tsMeta.getId() );
    }
    catch ( Exception e ) {
        Message.printWarning(3, routine, "Error setting TimeSeriesMeta ID for query (" + e + ")." );
    }
    if ( inputStart != null ) {
        try {
            ss.addWhereClause(dtColumn + " >= " + DMIUtil.formatDateTime(dmi, inputStart) );
        }
        catch ( Exception e ) {
            Message.printWarning(3, routine, "Error setting input start for query (" + e + ")." );
        }
    }
    if ( inputEnd != null ) {
        try {
            ss.addWhereClause(dtColumn + " <= " + DMIUtil.formatDateTime(dmi, inputEnd) );
        }
        catch ( Exception e ) {
            Message.printWarning(3, routine, "Error setting input end for query (" + e + ")." );
        }
    }
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Select statement = " + sqlString );
    ResultSet rs = null;
    double d, value;
    Date dt;
    String s, flag = "";
    DateTime dateTime;
    int i;
    int index;
    List<TimeSeriesData> tsdataList = new Vector<TimeSeriesData>();
    StopWatch selectTimer = new StopWatch();
    try {
        selectTimer.start();
        rs = dmi.dmiSelect(ss);
        selectTimer.stop();
        while (rs.next()) {
            index = 1;
            if ( dateTimeInt ) {
                i = rs.getInt(index++);
                if (rs.wasNull()) {
                    continue;
                }
                else {
                    dateTime = new DateTime(DateTime.PRECISION_YEAR);
                    dateTime.setYear(i);
                }
            }
            else {
                dt = rs.getTimestamp(index++);
                if (rs.wasNull()) {
                    continue;
                }
                else {
                    dateTime = new DateTime(dt);
                }
            }
            d = rs.getDouble(index++);
            if (rs.wasNull()) {
                value = missing;
            }
            else {
                value = d;
            }
            if ( flagColumn != null ) {
                s = rs.getString(index);
                if (rs.wasNull()) {
                    flag = s;
                }
                else {
                    flag = "";
                }
            }
            tsdataList.add(new TimeSeriesData(dateTime,value,flag));
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series data from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    dataTimer.stop();
    // Process the data records into the time series
    StopWatch setTimer = new StopWatch();
    setTimer.start();
    if ( inputStart != null ) {
        ts.setDate1(inputStart);
        ts.setDate1Original(inputStart);
    }
    if ( inputEnd != null ) {
        ts.setDate2(inputEnd);
        ts.setDate1Original(inputEnd);
    }
    if ( tsdataList.size() > 0 ) {
        ts.setDate1(tsdataList.get(0).getDateTime());
        ts.setDate1Original(tsdataList.get(0).getDateTime());
        ts.setDate2(tsdataList.get(tsdataList.size() - 1).getDateTime());
        ts.setDate2Original(tsdataList.get(tsdataList.size() - 1).getDateTime());
        ts.allocateDataSpace();
        for ( TimeSeriesData tsdata : tsdataList ) {
            if ( flagColumn == null ) {
                ts.setDataValue(tsdata.getDateTime(), tsdata.getValue() );
            }
            else {
                ts.setDataValue(tsdata.getDateTime(), tsdata.getValue(), tsdata.getFlag(), -1 );
            }
        }
    }
    setTimer.stop();
    Message.printStatus(2,routine,"Read " + tsdataList.size() + " values for \"" + ts.getIdentifierString() +
        "\" metaID=" + tsMeta.getId() +
    	" metatime=" + metaTimer.getMilliseconds() +
        "ms, selecttime=" + selectTimer.getMilliseconds() +
        "ms, datatime=" + dataTimer.getMilliseconds() + "ms, settime=" + setTimer.getMilliseconds() + "ms");
    return ts;
}

/**
Read time series metadata for one time series.
@return the time series metadata object, or null if not exactly 1 metadata records match.
*/
public TimeSeriesMeta readTimeSeriesMeta ( String locType, String locID,
    String dataSource, String dataType, String interval, String scenario )
{   String routine = "GenericDatabaseDataStore.readTimeSeriesMeta";
    DMI dmi = getDMI();
    // Create a statement to read the specific metadata record
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_ID_COLUMN_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String locIdColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    String descColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DESCRIPTION_COLUMN_PROP );
    String unitsColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_UNITS_COLUMN_PROP );
    ss.addTable(metaTable);
    if ( idColumn != null ) {
        ss.addField(idColumn);
    }
    if ( ltColumn != null ) {
        ss.addField(ltColumn);
    }
    if ( locIdColumn != null ) {
        ss.addField(locIdColumn);
    }
    if ( sourceColumn != null ) {
        ss.addField(sourceColumn);
    }
    if ( dtColumn != null ) {
        ss.addField(dtColumn);
    }
    if ( intervalColumn != null ) {
        ss.addField(intervalColumn);
    }
    if ( scenarioColumn != null ) {
        ss.addField(scenarioColumn);
    }
    if ( descColumn != null ) {
        ss.addField(descColumn);
    }
    if ( unitsColumn != null ) {
        ss.addField(unitsColumn);
    }
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    readTimeSeriesMetaAddWhere(ss,metaTable,locIdColumn,locID);
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    String sqlString = ss.toString();
    //Message.printStatus(2,routine,"Select statement = " + sqlString );
    ResultSet rs = null;
    long l, id = -1;
    String s, desc = "", units = "";
    int count = 0, i;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            // Since the calling arguments include everything of interest, really only need the ID, units, and description from the query,
            // but jump through the arguments as of above
            // TODO SAM 2013-08-29 should request by column name
            ++count;
            i = 0;
            if ( idColumn != null ) {
                l = rs.getLong(++i);
                if (!rs.wasNull()) {
                    id = l;
                }
            }
            if ( ltColumn != null ) {
                ++i;
            }
            if ( locIdColumn != null ) {
                ++i;
            }
            if ( sourceColumn != null ) {
                ++i;
            }
            if ( dtColumn != null ) {
                ++i;
            }
            if ( intervalColumn != null ) {
                ++i;
            }
            if ( scenarioColumn != null ) {
                ++i;
            }
            if ( descColumn != null ) {
                s = rs.getString(++i);
                if (!rs.wasNull()) {
                    desc = s;
                }
            }
            if ( unitsColumn != null ) {
                s = rs.getString(++i);
                if (!rs.wasNull()) {
                    units = s;
                }
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    if ( count != 1 ) {
        Message.printWarning(3, routine, "Expecting 1 time series meta object for \"" + sqlString + "\" but have " + count );
        return null;
    }
    if ( id < 0 ) {
        return null;
    }
    else {
        return new TimeSeriesMeta(locType, locID, dataSource, dataType, interval, scenario, desc, units, id);
    }
}

/**
Read location type strings for the data store, if time series support is configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param locID location ID to use as filter (ignored if blank or null)
@param locType location type to use as filter (ignored if blank or null)
@param dataType data type to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of interval strings by making a unique query of the 
*/
public List<String> readTimeSeriesMetaDataSourceList ( String locType, String locID,
    String dataType, String interval, String scenario )
{   String routine = "GenericDatabaseDataStore.readDataSourceStrings";
    DMI dmi = getDMI();
    List<String> dataSources = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(sourceColumn);
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    readTimeSeriesMetaAddWhere(ss,metaTable,idColumn,locID);
    // LocationType is what is being read so don't filter
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    ss.selectDistinct(true);
    ss.addOrderByClause(sourceColumn);
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Running:" + sqlString );
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                dataSources.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return dataSources;
}

/**
Read data type strings for the data store, if time series support is configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param includeNotes if true, include notes in the data type strings, like "DataType - Note"
(currently does nothing)
@param locType location type to use as filter (ignored if blank or null)
@param locID location ID to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of data type strings by making a unique query of the 
*/
public List<String> readTimeSeriesMetaDataTypeList ( boolean includeNotes,
    String locType, String locID, String dataSource, String interval, String scenario )
{   String routine = "GenericDatabaseDataStore.readTimeSeriesMetaDataTypeList";
    DMI dmi = getDMI();
    List<String> dataTypes = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(dtColumn);
    ss.selectDistinct(true);
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    readTimeSeriesMetaAddWhere(ss,metaTable,idColumn,locID);
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    // Data type is what is being read so don't filter
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    ss.addOrderByClause(dtColumn);
    String sqlString = ss.toString();
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                dataTypes.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return dataTypes;
}

/**
Read interval strings for the data store, if time series support is configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param locType location type to use as filter (ignored if blank or null)
@param locID location ID to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param dataType data type to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of distinct location ID strings from time series metadata
*/
public List<String> readTimeSeriesMetaIntervalList ( String locType, String locID, String dataSource,
    String dataType, String scenario )
{   String routine = "GenericDatabaseDataStore.readIntervalStrings";
    DMI dmi = getDMI();
    List<String> intervals = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(intervalColumn);
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    readTimeSeriesMetaAddWhere(ss,metaTable,idColumn,locID);
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    // Interval is what is being read so don't filter
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    ss.selectDistinct(true);
    ss.addOrderByClause(intervalColumn);
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Running:" + sqlString );
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                intervals.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return intervals;
}

/**
Read a list of TimeSeriesMeta for the specified criteria.
@param dataType data type to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param filterPanel panel that contains input filters where filter criteria for query are specified
@return list of TimeSeriesMeta matching the query criteria
*/
public List<TimeSeriesMeta> readTimeSeriesMetaList ( String dataType, String interval,
    GenericDatabaseDataStore_TimeSeries_InputFilter_JPanel filterPanel )
{
    List<TimeSeriesMeta> metaList = new Vector<TimeSeriesMeta>();
    String routine = "GenericDatabaseDataStore.readTimeSeriesMetaList";
    DMI dmi = getDMI();
    // Create a statement to read the specific metadata record
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_ID_COLUMN_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String locIdColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    String descColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DESCRIPTION_COLUMN_PROP );
    String unitsColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_UNITS_COLUMN_PROP );
    ss.addTable(metaTable);
    if ( idColumn != null ) {
        ss.addField(idColumn);
    }
    if ( ltColumn != null ) {
        ss.addField(ltColumn);
    }
    if ( locIdColumn != null ) {
        ss.addField(locIdColumn);
    }
    if ( sourceColumn != null ) {
        ss.addField(sourceColumn);
    }
    if ( dtColumn != null ) {
        ss.addField(dtColumn);
    }
    if ( intervalColumn != null ) {
        ss.addField(intervalColumn);
    }
    if ( scenarioColumn != null ) {
        ss.addField(scenarioColumn);
    }
    if ( descColumn != null ) {
        ss.addField(descColumn);
    }
    if ( unitsColumn != null ) {
        ss.addField(unitsColumn);
    }
    String locType = null;
    String locID = null;
    String dataSource = null;
    String scenario = null;
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    List<String> whereClauses = new ArrayList<String>();
    if ( ltColumn != null ) {
        whereClauses.add ( getWhereClauseStringFromInputFilter ( dmi, filterPanel, metaTable + "." + ltColumn, true ) );
    }
    if ( locIdColumn != null ) {
        whereClauses.add ( getWhereClauseStringFromInputFilter ( dmi, filterPanel, metaTable + "." + locIdColumn, true ) );
    }
    if ( sourceColumn != null ) {
        whereClauses.add ( getWhereClauseStringFromInputFilter ( dmi, filterPanel, metaTable + "." + sourceColumn, true ) );
    }
    if ( scenarioColumn != null ) {
        whereClauses.add ( getWhereClauseStringFromInputFilter ( dmi, filterPanel, metaTable + "." + scenarioColumn, true ) );
    }
    // Add user-specified filters, must be in the provided standard columns
    int i = 0;
    while ( true ) {
    	++i;
    	String propName = TS_META_TABLE_FILTER_PROP + "." + i;
    	String propVal = getProperty ( propName );
    	if ( propVal == null ) {
    		// Done with filter properties
    		break;
    	}
    	String [] parts = propVal.split(",");
    	if ( parts.length == 5 ) {
    		String column = parts[1].trim();
    		if ( column != null ) {
    	        whereClauses.add ( getWhereClauseStringFromInputFilter ( dmi, filterPanel, metaTable + "." + column, true ) );
    	    }
    	}
    }
    try {
        ss.addWhereClauses(whereClauses);
    }
    catch ( Exception e ) {
        Message.printWarning(3, routine, "Error adding where clauses (" + e + ")." );
    }
    String sqlString = ss.toString();
    Message.printStatus(2, routine, "Running:  " + sqlString );
    ResultSet rs = null;
    long l, id = -1;
    String s, desc = "", units = "";
    int index;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            index = 1;
            if ( idColumn != null ) {
                l = rs.getLong(index++);
                if (!rs.wasNull()) {
                    id = l;
                }
            }
            if ( ltColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    locType = "";
                }
                else {
                    locType = s;
                }
            }
            if ( locIdColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    locID = "";
                }
                else {
                    locID = s;
                }
            }
            if ( sourceColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    dataSource = "";
                }
                else {
                    dataSource = s;
                }
            }
            if ( dtColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    dataType = "";
                }
                else {
                    dataType = s;
                }
            }
            if ( intervalColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    interval = "";
                }
                else {
                    interval = s;
                }
            }
            if ( scenarioColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    scenario = "";
                }
                else {
                    scenario = s;
                }
            }
            if ( descColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    desc = "";
                }
                else {
                    desc = s;
                }
            }
            if ( unitsColumn != null ) {
                s = rs.getString(index++);
                if (rs.wasNull()) {
                    units = "";
                }
                else {
                    units = s;
                }
            }
            metaList.add(new TimeSeriesMeta(locType, locID, dataSource, dataType, interval, scenario, desc, units, id));
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return metaList;
}

/**
Read location ID strings for the data store, if time series features are configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param locType location type to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param dataType data type to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of distinct location ID strings from time series metadata
*/
public List<String> readTimeSeriesMetaLocationIDList ( String locType, String dataSource,
    String dataType, String interval, String scenario )
{   String routine = "GenericDatabaseDataStore.readLocationIDStrings";
    DMI dmi = getDMI();
    List<String> locIDs = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(idColumn);
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    // LocationID is what is being read so don't filter
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    ss.selectDistinct(true);
    ss.addOrderByClause(idColumn);
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Running:" + sqlString );
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                locIDs.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return locIDs;
}

/**
Read location type strings for the data store, if time series features are configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param locID location ID to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param dataType data type to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@param scenario scenario to use as filter (ignored if blank or null)
@return the list of interval strings by making a unique query of the 
*/
public List<String> readTimeSeriesMetaLocationTypeList ( String locID, String dataSource,
    String dataType, String interval, String scenario )
{   String routine = "GenericDatabaseDataStore.readLocationTypeStrings";
    DMI dmi = getDMI();
    List<String> locTypes = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(ltColumn);
    // LocationType is what is being read so don't filter
    readTimeSeriesMetaAddWhere(ss,metaTable,idColumn,locID);
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    readTimeSeriesMetaAddWhere(ss,metaTable,scenarioColumn,scenario);
    ss.selectDistinct(true);
    ss.addOrderByClause(ltColumn);
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Running:" + sqlString );
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                locTypes.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return locTypes;
}

/**
Utility method to add a where clause to the metadata select statement.
@param ss select statement to execute
@param table table to query
@param column for where clause
@param value value to use in where clause
*/
private void readTimeSeriesMetaAddWhere ( DMISelectStatement ss, String table, String column, String value )
{
    if ( (value != null) && !value.equals("") && !value.equals("*") ) {
        try {
            ss.addWhereClause(table + "." + column + " = '" + value + "'" );
        }
        catch ( Exception e ) {
            // Should not happen
        }
    }
}

/**
Read location type strings for the data store, if time series features are configured.
Not a lot of error checking is done because the data store should have been checked out by this point
@param locType location type to use as filter (ignored if blank or null)
@param locID location ID to use as filter (ignored if blank or null)
@param dataSource data source to use as filter (ignored if blank or null)
@param dataType data type to use as filter (ignored if blank or null)
@param interval interval to use as filter (ignored if blank or null)
@return the list of interval strings by making a unique query of the 
*/
public List<String> readTimeSeriesMetaScenarioList ( String locType, String locID,
    String dataSource, String dataType, String interval )
{   String routine = "GenericDatabaseDataStore.readDataSourceStrings";
    DMI dmi = getDMI();
    List<String> scenarios = new Vector<String>();
    // Create a statement to read distinct data types from the time series metadata table
    DMISelectStatement ss = new DMISelectStatement(dmi);
    String metaTable = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String ltColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String idColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String sourceColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String dtColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATATYPE_COLUMN_PROP );
    String intervalColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATAINTERVAL_COLUMN_PROP );
    String scenarioColumn = getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );
    ss.addTable(metaTable);
    ss.addField(scenarioColumn);
    readTimeSeriesMetaAddWhere(ss,metaTable,ltColumn,locType);
    readTimeSeriesMetaAddWhere(ss,metaTable,idColumn,locID);
    readTimeSeriesMetaAddWhere(ss,metaTable,sourceColumn,dataSource);
    readTimeSeriesMetaAddWhere(ss,metaTable,dtColumn,dataType);
    readTimeSeriesMetaAddWhere(ss,metaTable,intervalColumn,interval);
    // Scenario is what is being read so don't filter
    ss.selectDistinct(true);
    ss.addOrderByClause(scenarioColumn);
    String sqlString = ss.toString();
    Message.printStatus(2,routine,"Running:" + sqlString );
    ResultSet rs = null;
    String s;
    try {
        rs = dmi.dmiSelect(ss);
        while (rs.next()) {
            s = rs.getString(1);
            if (!rs.wasNull()) {
                scenarios.add(s);
            }
        }
    }
    catch ( Exception e ) {
        Message.printWarning ( 3, routine, "Error reading time series metadata from database with statement \"" + sqlString + "\" (" + e + ")."); 
    }
    finally {
        DMI.closeResultSet(rs);
    }
    return scenarios;
}

}
