package riverside.datastore;

import RTi.TS.TSIdent;
import riverside.datastore.GenericDatabaseDataStore;

/**
Simple object to hold time series metadata for GenericDatabaseDataStore.
*/
public class TimeSeriesMeta
{

/**
Internal identifier (primary key).
*/
private long id;

/**
Location type.
*/
private String locationType;

/**
Loction ID.
*/
private String locationID;

/**
Data source.
*/
private String dataSource;

/**
Data type.
*/
private String dataType;

/**
Interval.
*/
private String interval;

/**
Scenario.
*/
private String scenario;

/**
Data units.
*/
private String units;

/**
Construct a metadata object.
*/
public TimeSeriesMeta ( String locationType, String locationID, String dataSource, String dataType, String interval,
    String scenario, String units, long id )
{
    this.locationType = locationType;
    this.locationID = locationID;
    this.dataSource = dataSource;
    this.dataType = dataType;
    this.interval = interval;
    this.scenario = scenario;
    this.units = units;
    this.id = id;
}

/**
Return the data source.
*/
public String getDataSource ()
{
    return this.dataSource;
}

/**
Return the data type.
*/
public String getDataType ()
{
    return this.dataType;
}

/**
Return the identifier.
*/
public long getId ()
{
    return this.id;
}

/**
Return the interval.
*/
public String getInterval ()
{
    return this.interval;
}

/**
Return the location ID.
*/
public String getLocationID ()
{
    return this.locationID;
}

/**
Return the location ID.
*/
public String getLocationType ()
{
    return this.locationType;
}

/**
Return the scenario.
*/
public String getScenario ()
{
    return this.scenario;
}

/**
Return the time series identifier corresponding to the metadata.
@param dataStore the data store that is being used to process data, or null to ignore (if null, the current
API version is assumed).
@return the TSID string of the form ID.ACIS.MajorVariableNumber.Day[~DataStoreName]
*/
public String getTSID ( GenericDatabaseDataStore dataStore )
{
    String dataStoreName = dataStore.getName();
    StringBuffer tsid = new StringBuffer();
    if ( !getLocationType().equals("") ) {
        tsid.append ( getLocationType() );
        tsid.append ( TSIdent.LOC_TYPE_SEPARATOR );
    }
    tsid.append ( getLocationID() );
    tsid.append ( TSIdent.SEPARATOR );
    tsid.append ( getDataSource() );
    tsid.append ( TSIdent.SEPARATOR );
    tsid.append ( getDataType() );
    tsid.append ( TSIdent.SEPARATOR );
    tsid.append ( getInterval() );
    if ( !getScenario().equals("") ) {
        tsid.append ( TSIdent.SEPARATOR );
        tsid.append ( getScenario() );
    }
    if ( (dataStoreName != null) && !dataStoreName.equals("") ) {
        tsid.append ( "~" + dataStoreName );
    }
    return tsid.toString();
}

/**
Return the data type.
*/
public String getUnits ()
{
    return this.units;
}

}