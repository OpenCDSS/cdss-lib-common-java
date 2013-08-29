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
private long id = -1;

/**
Location type.
*/
private String locationType;

/**
Location ID.
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
Descrition for time series (often the location name).
*/
private String description;

/**
Data units.
*/
private String units;

/**
Construct a metadata object.
@param locationType the location type, as per the conventions of the datastore.
@param locationID the location identifier (e.g., station identifier).
@param dataSource the data source abbreviation (e.g., agency).
@param dataType the data type abbreviation (e.g., "Streamflow").
@param interval the data interval, as per TimeInterval strings.
@param scenario the scenario for the time series.
@param description a short description of the time series, often the location name.
@param units the time series data units
@param id internal identifier, typically the primary key in a database.
*/
public TimeSeriesMeta ( String locationType, String locationID, String dataSource, String dataType, String interval,
    String scenario, String description, String units, long id )
{
    this.locationType = (locationType == null ? "" : locationType);
    this.locationID = (locationID == null ? "" : locationID);
    this.dataSource = (dataSource == null ? "" : dataSource);
    this.dataType = (dataType == null ? "" : dataType);
    this.interval = (interval == null ? "" : interval);
    this.scenario = (scenario == null ? "" : scenario);
    this.description = (description == null ? "" : description);
    this.units = (units == null ? "" : units);
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
Return the description.
*/
public String getDescription ()
{
    return this.description;
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