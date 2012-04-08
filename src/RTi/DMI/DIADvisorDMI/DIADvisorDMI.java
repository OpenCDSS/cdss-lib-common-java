// ----------------------------------------------------------------------------
// DIADvisorDMI.java - DMI to interact with DIADvisor database
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// Notes:
// (1) This class must override determineDatabaseVersion() and readGlobalData()
// in DMI.java
// ----------------------------------------------------------------------------
// History:
//
// 2003-02-04	Steven A. Malers, RTi	Initial version, based on
//					RiversideDB_DMI.
// 2003-03-30	SAM, RTi		* Add SysConfig to data objects.
//					* Add __operational_DMI and
//					  __archive_DMI to allow queries to the
//					  related database, when needed.
// 2003-06-16	SAM, RTi		* Update to use the new TS package
//					  (DateTime instead of TSDate, etc.).
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
//EndHeader

package RTi.DMI.DIADvisorDMI;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import RTi.DMI.DMI;
import RTi.DMI.DMISelectStatement;
import RTi.DMI.DMIStatement;
import RTi.DMI.DMIUtil;

import RTi.TS.DayTS;
import RTi.TS.HourTS;
import RTi.TS.IrregularTS;
import RTi.TS.MinuteTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The DIADvisorDMI provides an interface to the DIADvisor database.

<b>SQL Method Naming Conventions</b><p>

The first word in the method name is one of the following:<br>
<ol>
<li>read</li>
<li>write</li>
<li>delete</li>
<li>count</li>
</ol>

The second part of the method name is the data object being operated on.
If a list is returned, then "List" is included in the method name.
Finally, if a select based on a where clause is used, the method includes the
field for the Where.  Examples are:

<ol>
<li>	readSensorDefList</li>
<li>	readSensorDefForSensorID_num</li>
</ol>

<p>
<b>Notes on versioning:</b><br>
Version changes require changes throughout the code base.  The following
example tells all the changes that need to be made when a new field is
added to an existing table:<p>
<ul>
<li>in buildSQL(), add the new field to all the select and write statement
sections for the appropriate table.  Do not forget to wrap this new code
with tests for the proper version (DMI.isDatabaseVersionAtLeast())</li>
<li>if, for the table XXXX, a method exists like:<br>
<code>private Vector toXXXXList</code><br>
then add the field to the Vector-filling code in this method</li>
<li>go to the RiversideDB_XXXX.java object that represents the table for
which this field was added.  Add the data member for the field, 
get/set statements, and then add the field (with brief information on the
version in which it was added) to the toString()</li>
<li>add the field, and the appropriate version-checking code, to the 
writeXXXX() method</li>
<li>update determineDatabaseVersion()</li>
</ul>
*/
public class DIADvisorDMI extends DMI
{

/**
DIADvisor version for 2.61 as of ????-??-??, including the
following design elements:  NEED SOMETHING FROM DIAD
*/
public final static long VERSION_026100_00000000 = 26120030330L;
public final static long VERSION_LATEST = VERSION_026100_00000000;

// Alphabetize the following by table.  Make sure values to not overlap.

/**
Select DataChron, returning a Vector of DIADvisor_DataChron
*/
protected final int _S_DATACHRON = 50;

/**
Select GroupDef, returning a Vector of DIADvisor_GroupDef
*/
protected final int _S_GROUPDEF = 60;

/**
Select SensorDef, returning a Vector of DIADvisor_SensorDef
*/
protected final int _S_SENSOR_DEF = 100;

/**
Select SiteDef, returning a Vector of DIADvisor_SiteDef
*/
protected final int _S_SITE_DEF = 200;

/**
Select SysConfig, returning a single DIADvisor_SysConfig
*/
protected final int _S_SYS_CONFIG = 300;

/**
References to the operational and archive databases.  This is needed because
DIADvisor has two databases, each with an ODBC connection.  All database
interaction is assumed to occur with the operational datbase, with the archive
DMI used only when reading time series from the archive database.
*/
DIADvisorDMI __dmi_operational = null;
DIADvisorDMI __dmi_archive = null;

/** 
Constructor for a predefined ODBC DSN.
@param database_engine The database engine to use (see the DMI constructor).
@param odbc_name The ODBC DSN that has been defined on the machine.
@param system_login If not null, this is used as the system login to make the
connection.  If null, the default system login is used.
@param system_password If not null, this is used as the system password to make
the connection.  If null, the default system password is used.
*/
public DIADvisorDMI (	String database_engine, String odbc_name,
			String system_login, String system_password )
throws Exception
{	// Use the default system login and password
	super ( database_engine, odbc_name, system_login, system_password );
	if ( system_login == null ) {
		// Use the default...
		setSystemLogin("diad");
	}
	if ( system_password == null ) {
		// Use the default...
		setSystemPassword("lena");
	}
	setEditable(true);
	setSecure(false);
}

/** 
Constructor for a database server and database name, to use an automatically
created URL.
@param database_engine The database engine to use (see the DMI constructor).
@param database_server The IP address or DSN-resolvable database server
machine name.
@param database_name The database name on the server.  If null, default to
"RiversideDB".
@param port Port number used by the database.  If negative, default to that for
the database engine.
@param system_login If not null, this is used as the system login to make the
connection.  If null, the default system login is used.
@param system_password If not null, this is used as the system password to make
the connection.  If null, the default system password is used.
*/
public DIADvisorDMI (	String database_engine, String database_server,
			String database_name, int port,
			String system_login, String system_password )
throws Exception
{	// Use the default system login and password
	super ( database_engine, database_server, database_name, port,
		system_login, system_password );
	if ( system_login == null ) {
		// Use the default...
		setSystemLogin("diad");
	}
	if ( system_password == null ) {
		// Use the default...
		setSystemPassword("lena");
	}
	setEditable(true);
	setSecure(false);
}

/** 
Build an SQL string based on a requested SQL statement code.  This defines 
the basic statement and allows overloaded methods to avoid redundant code.
This method is used to eliminate redundant code where methods use the same
basic statement but with different where clauses.
@param statement Statement to set values in.
@param sqlNumber the number of the SQL statement to build.  Usually defined
as a private constant as a mnemonic aid.
@return a string containing the full SQL.
*/
private void buildSQL ( DMIStatement statement, int sqlNumber )
throws Exception
{	DMISelectStatement select;
	//DMIWriteStatement write;	// Probably don't need
	//DMIDeleteStatement del;
    String leftIdDelim = getFieldLeftEscape();
    String rightIdDelim = getFieldRightEscape();
	switch ( sqlNumber ) {
		case _S_DATACHRON:
			select = (DMISelectStatement)statement;
			select.addField ( "DataChron." + leftIdDelim +
				"Date/Time" + rightIdDelim );
			select.addField ( "DataChron." + leftIdDelim +
				"Sensor ID" + rightIdDelim );
			select.addField ( "DataChron.Count" );
			select.addField ( "DataChron." + leftIdDelim +
				"Data Type" + rightIdDelim );
			select.addField ( "DataChron.Source" );
			select.addField ( "DataChron." + leftIdDelim +
				"Data Value" + rightIdDelim );
			select.addField ( "DataChron." + leftIdDelim +
				"Data Value 2" + rightIdDelim );
			select.addField ( "DataChron.SeqNum" );
			select.addField ( "DataChron.Comment" );
			select.addTable ( "DataChron" );
			break;
		case _S_GROUPDEF:
			select = (DMISelectStatement)statement;
			select.addField ( "GroupDef.Group" );
			select.addField ( "GroupDef.Operation" );
			select.addField ( "GroupDef.Units1" );
			select.addField ( "GroupDef.Units2" );
			select.addField ( "GroupDef.Display" );
			select.addTable ( "GroupDef" );
			break;
		case _S_SENSOR_DEF:
			select = (DMISelectStatement)statement;
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Sensor ID" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Site ID" +
					rightIdDelim );
			select.addField ( "SensorDef.Type" );
			select.addField ( "SensorDef.Group" );
			select.addField ( "SensorDef.Description" );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Max Count" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Min Count" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Pos Delta" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Neg Delta" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Rating Type" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Rating Interpolation" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Rating Shift" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Calibration Offset" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Calibration Date" +
					rightIdDelim );
			select.addField ( "SensorDef.Slope" );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Reference Level" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Display Units" +
					rightIdDelim );
			select.addField ( "SensorDef.Decimal" );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Display Units 2" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Decimal 2" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"In Service" +
					rightIdDelim );
			select.addField ( "SensorDef.Suspect" );
			select.addField ( "SensorDef.Alarms" );
			select.addField ( "SensorDef.Notify" );
			select.addField ( "SensorDef.Timeout" );
			select.addField ( "SensorDef.Children" );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Most Recent Time" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Most Recent Data" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Last Valid Time" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Last Valid Data" +
					rightIdDelim );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Last Count" +
					rightIdDelim);
			select.addField ( "SensorDef.Equation" );
			select.addField ( "SensorDef." +
					leftIdDelim +
					"Equation 2" +
					rightIdDelim );
			select.addTable ( "SensorDef" );
			break;
		case _S_SITE_DEF:
			select = (DMISelectStatement)statement;
			select.addField ( "SiteDef.SiteName" );
			select.addField ( "SiteDef." + leftIdDelim +
				"Site ID" + rightIdDelim );
			select.addField ( "SiteDef.Latitude" );
			select.addField ( "SiteDef.Longitude" );
			select.addField ( "SiteDef.XCoord" );
			select.addField ( "SiteDef.YCoord" );
			select.addField ( "SiteDef.PKey" );
			select.addField ( "SiteDef." + leftIdDelim +
				"Repeater Group" + rightIdDelim );
			select.addField ( "SiteDef.Elevation" );
			select.addField ( "SiteDef." + leftIdDelim +
				"Site Picture" + rightIdDelim );
			select.addField ( "SiteDef.Zone" );
			select.addField ( "SiteDef.FIPS" );
			select.addField ( "SiteDef.LastUpdate" );
			select.addTable ( "SiteDef" );
			break;
		case _S_SYS_CONFIG:
			select = (DMISelectStatement)statement;
			select.addField ( "SysConfig.Interval" );
			select.addTable ( "SysConfig" );
			break;
		default:
			Message.printWarning ( 2, "DIADvisorDMI.buildSQL",
			"Unknown statement code: " + sqlNumber );
			break;
	}
}

/**
Determine the database version by examining the table structure for the
database.  The following versions are known for DIADvisor:
<ul>
<li>	02610000000000 - Current version used for testing new features.</li>
</ul>
*/
public void determineDatabaseVersion()
{	// Assume this...
	setDatabaseVersion ( VERSION_026100_00000000 );
	Message.printStatus ( 1, "DIADvisorDMI.determineDatabaseVersion",
	"DIADvisor version determined to be at least " + getDatabaseVersion() );
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize() throws Throwable
{	super.finalize();
}

/**
Read records from the DataChron table.
This method is called from readTimeSeries().
@return a vector of objects of type DIADvisor_DataChron.
@param sensorid Sensor ID for data records.
@param req_date1 Requested first date/time (can be null).
@param req_date2 Requested last date/time (can be null).
*/
public List readDataChronListForSensorIDAndPeriod (	String sensorid,
							DateTime req_date1,
							DateTime req_date2 )
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	// Build the SQL here because it depends on the table...
	q = new DMISelectStatement ( this );
	buildSQL ( q, _S_DATACHRON );
	// Primary key is the sensor ID...
	q.addWhereClause ( "DataChron." + getFieldLeftEscape() + "Sensor ID" + getFieldRightEscape() + "=" + sensorid);
	// If req_date1 is specified, use it...
	if ( req_date1 != null ) {
		q.addWhereClause (
			"DataChron.StartTime >= " +DMIUtil.formatDateTime( this, req_date1));
	}
	// If req_date2 is specified, use it...
	if ( req_date2 != null ) {
		q.addWhereClause (
			"DataChron.StartTime <= " + DMIUtil.formatDateTime( this, req_date2));
	}
	// Also add wheres for the start and end dates if they are specified...
	ResultSet rs = dmiSelect(q);
	List v = toDataChronList (rs);
	rs.close();
	return v;
}

/**
Read global data that should be kept in memory to increase performance.
This is called from the DMI.open() base class method.
throws SQLException if there is an error reading the data.
throws Exception thrown in readTablesList()
*/
public void readGlobalData () throws SQLException, Exception {
	// Read the Tables table.
	// Nothing here yet.
}

/**
Read the GroupDef records.
@return a vector of objects of type DIADvisor_GroupDef.
*/
public List readGroupDefList ()
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	buildSQL ( q, _S_GROUPDEF );
	ResultSet rs = dmiSelect(q);
	List v = toGroupDefList (rs);
	rs.close();
	return v;
}

/**
Read records from the regular interval time series tables:  hour, day, interval.
This method is called from readTimeSeries().
@return a vector of objects of type DIADvisor_RegularTSRecord.
@param ts_table Table from which to read the time series records.
@param sensorid Sensor ID for data records.
@param req_date1 Requested first date/time (can be null).
@param req_date2 Requested last date/time (can be null).
*/
private List readRegularTSRecordList (	String ts_table,
						String sensorid,
						DateTime req_date1,
						DateTime req_date2 )
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	// Build the SQL here because it depends on the table...
	q = new DMISelectStatement ( this );
	q.addTable ( ts_table );
	q.addField ( ts_table + ".StartTime" );
	q.addField ( ts_table + "." + getFieldLeftEscape() + "Sensor ID" + getFieldRightEscape() );
	q.addField ( ts_table + ".Value" );
	q.addField ( ts_table + ".Count" );
	// Primary key is the sensor ID...
	q.addWhereClause ( ts_table + "." + getFieldLeftEscape() + "Sensor ID" + getFieldRightEscape() + "=" + sensorid);
	// If req_date1 is specified, use it...
	if ( req_date1 != null ) {
		q.addWhereClause (
			ts_table +
			".StartTime >= " + DMIUtil.formatDateTime( this,
			req_date1));
	}
	// If req_date2 is specified, use it...
	if ( req_date2 != null ) {
		q.addWhereClause (
			ts_table + ".StartTime <= " +
			DMIUtil.formatDateTime( this, req_date2));
	}
	// Also add wheres for the start and end dates if they are specified...
	ResultSet rs = dmiSelect(q);
	List v = toRegularTSRecordList (rs);
	rs.close();
	return v;
}

/**
Read SensorDef records for distinct group.
@return a vector of objects of type DIADvisor_SensorDef.
*/
public List readSensorDefListForDistinctGroup ()
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	// Select from SensorDef
	q.addField ( "SensorDef.Group" );
	q.addTable ( "SensorDef" );
	q.selectDistinct(true);
	q.addOrderByClause("SensorDef.Group");
	ResultSet rs = dmiSelect ( q );
	// Transfer here instead of the toSensorDefList() method...
	List v = new Vector();
	int index = 1;
	String s;
	DIADvisor_SensorDef data = null;
	while ( rs.next() ) {
		data = new DIADvisor_SensorDef();
		s = rs.getString ( index );
		if ( !rs.wasNull() ) {
			data.setGroup ( s.trim() );
		}
		v.add ( data );
	}
	rs.close();
	return v;
}

/**
Read a SensorDef record for the given sensor id.
@param SensorId Sensor id for sensor that is to be queried.
@return a single DIADvisor_SensorDef.
*/
public DIADvisor_SensorDef readSensorDefForSensorID ( int SensorId )
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	buildSQL ( q, _S_SENSOR_DEF );
	q.addWhereClause ( "SensorDef." + getFieldLeftEscape() + "Sensor ID" + getFieldRightEscape() + "=" + SensorId ); 
	ResultSet rs = dmiSelect(q);
	List v = toSensorDefList (rs);
	rs.close();
	if ( (v == null) || (v.size() < 1) ) {
		return null;
	}
	else {
	    return (DIADvisor_SensorDef)v.get(0);
	}
}

/**
Read the SensorDef records.
@return a vector of objects of type DIADvisor_SensorDef.
*/
public List readSensorDefList ()
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	buildSQL ( q, _S_SENSOR_DEF );
	ResultSet rs = dmiSelect(q);
	List v = toSensorDefList (rs);
	rs.close();
	return v;
}

/**
Read the SiteDef records.
@return a vector of objects of type DIADvisor_SiteDef.
*/
public List readSiteDefList ()
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	buildSQL ( q, _S_SITE_DEF );
	//q.addWhereClause ( "Area.MeasLoc_num=" + MeasLoc_num ); 
	ResultSet rs = dmiSelect(q);
	List v = toSiteDefList (rs);
	rs.close();
	return v;
}

/**
Read the SysConfig record.
@return the DIADvisor_SysConfig corresponding to the single record in the
SysConfig table.
*/
public DIADvisor_SysConfig readSysConfig ()
throws Exception
{	DMISelectStatement q = new DMISelectStatement ( this );
	buildSQL ( q, _S_SYS_CONFIG );
	ResultSet rs = dmiSelect(q);
	List v = toSysConfigList (rs);
	rs.close();
	if ( (v == null) || (v.size() == 0) ) {
		return null;
	}
	else {	return (DIADvisor_SysConfig)v.get(0);
	}
}

/**
Read a time series matching a time series identifier.
@return a time series or null.
@param tsident_string TSIdent string indentifying the time series.
@param req_date1 Optional date to specify the start of the query (specify 
null to read the entire time series).
@param req_date2 Optional date to specify the end of the query (specify 
null to read the entire time series).
@param req_units requested data units (specify null or blank string to 
return units from the database).
@param read_data Indicates whether data should be read (specify false to 
only read header information).
@exception if there is an error reading the time series.
*/
public TS readTimeSeries (String tsident_string, DateTime req_date1,
			  DateTime req_date2, String req_units,
			  boolean read_data ) throws Exception
{	String routine = "DIADvisorDMI.readTimeSeries";
	// Create a TSIdent to get at the individual parts...
	TSIdent tsident = new TSIdent ( tsident_string );
	String interval = tsident.getInterval();
	TS ts = null;	// Time series being read
	String table;	// Table from which to read data
	boolean is_regular = true;	// Indicates whether a regular or
					// irregular time series interval.
	boolean is_datavalue1 = true;	// Indicates whether DataValue1 or
					// DataValue2 is used - to increase
					// performance.
	try {
	if (	StringUtil.indexOfIgnoreCase(tsident.getInterval(),
		"DataValue2",0) >= 0 ) {
		is_datavalue1 = false;
	}
	if ( tsident.getInterval().regionMatches(true,0,"irreg",0,5) ) {
		// Read from Datachron...
		ts = new IrregularTS ();
		table = "DataChron";
		is_regular = false;
	}
	else {	// Read from Day, Hour, or Interval...
		if ( interval.equalsIgnoreCase("Hour") ) {
			ts = new HourTS ();
			ts.setDataInterval ( TimeInterval.HOUR, 1 );
			table = "Hour";
		}
		else if ( interval.equalsIgnoreCase("Day") ) {
			ts = new DayTS ();
			ts.setDataInterval ( TimeInterval.DAY, 1 );
			table = "Day";
		}
		else {	// Assume minutes...
			ts = new MinuteTS ();
			ts.setDataInterval ( TimeInterval.MINUTE,
				tsident.getIntervalMult() );
			table = "Interval";
		}
	}
	ts.setIdentifier ( tsident );

	// Read the sensor for more information...

	DIADvisor_SensorDef sensordef =
			__dmi_operational.readSensorDefForSensorID (
			StringUtil.atoi(tsident.getLocation()) );
	if ( sensordef == null ) {
		Message.printWarning ( 2, routine, "Sensor " +
		tsident.getLocation() + " not found in DIADvisor for TS \"" +
		tsident_string + "\"" );
	}

	ts.setDescription ( sensordef.getDescription() );
	if (	StringUtil.indexOfIgnoreCase(tsident.getType(),"DataValue2",0)
		>= 0 ) {
		ts.setDataUnits ( sensordef.getDisplayUnits2() );
		ts.setDataUnitsOriginal ( sensordef.getDisplayUnits2() );
	}
	else {	ts.setDataUnits ( sensordef.getDisplayUnits() );
		ts.setDataUnitsOriginal ( sensordef.getDisplayUnits() );
	}
	if ( req_date1 != null ) {
		ts.setDate1 ( req_date1 );
	}
	if ( req_date2 != null ) {
		ts.setDate2 ( req_date2 );
	}
	// SAMX - problem here - in order to read the header and get the dates,
	// we really need to get the dates from somewhere.
	if ( !read_data ) {
		return ts;
	}
	// Read the data...
	List data_records = null;
	if ( is_regular ) {
		if ( tsident.getScenario().equals("") ) {
			data_records =__dmi_operational.readRegularTSRecordList(
					table,
					tsident.getLocation(), req_date1,
					req_date2 );
		}
		else {	data_records = __dmi_archive.readRegularTSRecordList(
					table,
					tsident.getLocation(), req_date1,
					req_date2 );
		}
	}
	else {	if ( tsident.getScenario().equals("") ) {
			data_records =
			__dmi_operational.readDataChronListForSensorIDAndPeriod(
			tsident.getLocation(), req_date1, req_date2 );
		}
		else {	data_records =
			__dmi_archive.readDataChronListForSensorIDAndPeriod (
			tsident.getLocation(), req_date1, req_date2 );
		}
	}
	DIADvisor_RegularTSRecord reg_data = null;
	DIADvisor_DataChron irreg_data = null;
	int size = 0;
	if ( data_records != null ) {
		size = data_records.size();
	}
	if ( size == 0 ) {
		// Return null because there are no data to set dates.
		// This prevents problems in graphing and other code
		// where dates are required.
		return null;
	}

	if ( (req_date1 != null) && (req_date2 != null) ) {
		// Allocate the memory regardless of whether there was
		// data.  If no data have been found then missing data
		// will be initialized...
		ts.setDate1(req_date1);
		ts.setDate1Original(req_date1);
		ts.setDate2(req_date2);
		ts.setDate2Original(req_date2);
		ts.allocateDataSpace();
	}
	else if ( size > 0 ) {
		// Set the date from the records...
		if ( is_regular ) {
			reg_data = (DIADvisor_RegularTSRecord)
				data_records.get(0);
			ts.setDate1(new DateTime(reg_data._StartTime));
			ts.setDate1Original(new DateTime(reg_data._StartTime));

			reg_data = (DIADvisor_RegularTSRecord)
				data_records.get(size - 1);
			ts.setDate2(new DateTime(reg_data._StartTime));
			ts.setDate2Original(new DateTime(reg_data._StartTime));
			ts.allocateDataSpace();
		}
		else {	irreg_data = (DIADvisor_DataChron)
				data_records.get(0);
			ts.setDate1(new DateTime(irreg_data._DateTime));
			ts.setDate1Original(new DateTime(irreg_data._DateTime));

			irreg_data = (DIADvisor_DataChron)
				data_records.get(size - 1);
			ts.setDate2(new DateTime(irreg_data._DateTime));
			ts.setDate2Original(new DateTime(irreg_data._DateTime));
			ts.allocateDataSpace();
		}
	}

	// The date needs to be the correct precision so assign from the TS
	// start date (the precision is adjusted when dates are set)...

	DateTime date = new DateTime ( ts.getDate1() );
	
	double val = 0.0;
	String flag = "";
	if ( tsident.getInterval().regionMatches(true,0,"irreg",0,5) ) {
		// Loop through and assign the data...
		for ( int i = 0; i < size; i++ ) {
			// Set the date rather than declaring a new instance
			// to increase performance...
			if ( is_regular ) {
				reg_data = (DIADvisor_RegularTSRecord)
					data_records.get(i);
				date.setDate ( reg_data._StartTime );
				// Adjust so the time is the interval-ending
				// time (DIADvisor uses the interval start,
				// which is not consistent with other RTi
				// tools...
				// Also take the value from the one possible
				// value (for DataValue1)...
				val = reg_data._Value;
			}
			else {	// Since DataChron is irregular, the time is
				// used as is.   Get the value depending on
				// whether DataValue1 or DataValue2 are used...
				irreg_data = (DIADvisor_DataChron)
					data_records.get(i);
				date.setDate ( irreg_data._DateTime );
				flag = irreg_data._DataType;
				if ( is_datavalue1 ) {
					val = irreg_data._DataValue;
				}
				else {	val = irreg_data._DataValue2;
				}
			}
			if ( is_regular ) {
				ts.setDataValue ( date, val );
			}
			else {	// Also set the data flag...
				ts.setDataValue ( date, val, flag, 0 );
			}
		}
	}
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
		Message.printWarning ( 2, routine, "Last op SQL: " +
		__dmi_operational.getLastSQLString() );
		Message.printWarning ( 2, routine, "Last archive SQL: " +
		__dmi_operational.getLastSQLString() );
	}
	return ts;
}

/**
Set the DIADvisorDMI corresponding to the archive database.
@param dmi DIADvisorDMI corresponding to the DIADvisor archive database.
*/
public void setArchiveDMI ( DIADvisorDMI dmi )
{	__dmi_archive = dmi;
}

/**
Set the DIADvisorDMI corresponding to the operational database.
@param dmi DIADvisorDMI corresponding to the DIADvisor operational database.
*/
public void setOperationalDMI ( DIADvisorDMI dmi )
{	__dmi_operational = dmi;
}

/**
Convert a ResultSet to a Vector of DIADvisor_DataChron.
@param rs ResultSet from a DataChron table query.
query.
*/
private List toDataChronList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	double d;
	int i;
	String s;
	Date date;
	DIADvisor_DataChron data = null;
	while ( rs.next() ) {
		data = new DIADvisor_DataChron();
		index = 1;
		date = rs.getTimestamp(index++);
		if ( rs.wasNull() ) {
			// Absolutely need a date!
			continue;
		}
		data.setDateTime(date);
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setSensorID(i);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setCount(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setDataType(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setSource(s.trim());
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setDataValue(d);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setDataValue2(d);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setSeqNum(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setComment(s.trim());
		}
		v.add(data);
	}
	return v;
}

/**
Convert a ResultSet to a Vector of DIADvisor_GroupDef.
@param rs ResultSet from a GroupDef table query.
*/
private List toGroupDefList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	String s;
	int i;
	DIADvisor_GroupDef data = null;
	while ( rs.next() ) {
		data = new DIADvisor_GroupDef();
		index = 1;
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setGroup(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setOperation(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setUnits1(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setUnits2(s.trim());
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setDisplay(i);
		}
		v.add(data);
	}
	return v;
}

/**
Convert a ResultSet to a Vector of DIADvisor_RegularTSRecord.
@param rs ResultSet from a regular time series table (Hour, Day, Interval)
query.
*/
private List toRegularTSRecordList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	double d;
	int i;
	Date date;
	DIADvisor_RegularTSRecord data = null;
	while ( rs.next() ) {
		data = new DIADvisor_RegularTSRecord();
		index = 1;
		date = rs.getTimestamp(index++);
		if ( rs.wasNull() ) {
			// Absolutely need a date!
			continue;
		}
		data.setStartTime(date);
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setSensorID(i);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setValue(d);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setCount(i);
		}
		v.add(data);
	}
	return v;
}

/**
Convert a ResultSet to a Vector of DIADvisor_SensorDef.
@param rs ResultSet from a SensorDef table query.
*/
private List toSensorDefList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	String s;
	int i;
	boolean b;
	double d;
	Date date;
	DIADvisor_SensorDef data = null;
	while ( rs.next() ) {
		data = new DIADvisor_SensorDef();
		index = 1;
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setSensorID(i);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setSiteID(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setType(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setGroup(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setDescription(s.trim());
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setMaxCount(i);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setMinCount(i);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setPosDelta(i);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setNegDelta(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setRatingType(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setRatingInterpolation(s.trim());
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setRatingShift(d);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setCalibrationOffset(i);
		}
		date = rs.getTimestamp(index++);
		if ( !rs.wasNull() ) {
			data.setCalibrationDate(date);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setSlope(d);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setReferenceLevel(d);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setDisplayUnits(s.trim());
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setDecimal(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setDisplayUnits2(s.trim());
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setDecimal2(i);
		}
		b = rs.getBoolean(index++);
		if ( !rs.wasNull() ) {
			data.setInService(b);
		}
		b = rs.getBoolean(index++);
		if ( !rs.wasNull() ) {
			data.setSuspect(b);
		}
		b = rs.getBoolean(index++);
		if ( !rs.wasNull() ) {
			data.setAlarms(b);
		}
		b = rs.getBoolean(index++);
		if ( !rs.wasNull() ) {
			data.setNotify(b);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setTimeout(i);
		}
		b = rs.getBoolean(index++);
		if ( !rs.wasNull() ) {
			data.setChildren(b);
		}
		date = rs.getTimestamp(index++);
		if ( !rs.wasNull() ) {
			data.setMostRecentTime(date);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setMostRecentData(d);
		}
		date = rs.getTimestamp(index++);
		if ( !rs.wasNull() ) {
			data.setLastValidTime(date);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setLastValidData(d);
		}
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setLastCount(i);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setEquation(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setEquation2(s.trim());
		}
		v.add(data);
	}
	return v;
}

/**
Convert a ResultSet to a Vector of DIADvisor_SiteDef.
@param rs ResultSet from a SiteDef table query.
*/
private List toSiteDefList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	String s;
	long l;
	double d;
	Date date;
	DIADvisor_SiteDef data = null;
	while ( rs.next() ) {
		data = new DIADvisor_SiteDef();
		index = 1;
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setSiteName(s.trim());
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setLatitude(d);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setLongitude(d);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setXCoord(d);
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setYCoord(d);
		}
		l = rs.getLong(index++);
		if ( !rs.wasNull() ) {
			data.setPKey(l);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setRepeaterGroup(s.trim());
		}
		d = rs.getDouble(index++);
		if ( !rs.wasNull() ) {
			data.setElevation(d);
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setSitePicture(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setZone(s.trim());
		}
		s = rs.getString(index++);
		if ( !rs.wasNull() ) {
			data.setFIPS(s.trim());
		}
		date = rs.getTimestamp(index++);
		if ( !rs.wasNull() ) {
			data.setLastUpdate(date);
		}
		v.add(data);
	}
	return v;
}

/**
Convert a ResultSet to a Vector of DIADvisor_SysConfig.
@param rs ResultSet from a SysConfig table query.
*/
private List toSysConfigList ( ResultSet rs ) throws Exception
{	List v = new Vector();
	int index = 1;
	int i;
	DIADvisor_SysConfig data = null;
	while ( rs.next() ) {
		data = new DIADvisor_SysConfig();
		index = 1;
		i = rs.getInt(index++);
		if ( !rs.wasNull() ) {
			data.setInterval(i);
		}
		v.add(data);
	}
	return v;
}

} // End DIADvisorDMI
