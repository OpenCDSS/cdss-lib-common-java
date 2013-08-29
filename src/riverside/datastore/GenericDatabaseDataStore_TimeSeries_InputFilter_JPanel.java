package riverside.datastore;

import java.util.List;
import java.util.Vector;

import riverside.datastore.GenericDatabaseDataStore;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;

import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying GenericDatabaseDataStore database.
*/
public class GenericDatabaseDataStore_TimeSeries_InputFilter_JPanel extends InputFilter_JPanel //implements ItemListener, KeyListener
{
    
/**
Associated data store.
*/
private GenericDatabaseDataStore __dataStore = null;

/**
Constructor.
@param dataStore the datastore for queries.  Cannot be null.
@param numFilterGroups the number of filter groups to display
*/
public GenericDatabaseDataStore_TimeSeries_InputFilter_JPanel( GenericDatabaseDataStore dataStore, int numFilterGroups )
{   super();
    __dataStore = dataStore;
    setFilters ( numFilterGroups );
}

/**
Set the filter data.  This method is called at setup.
Always use the most current parameter name from the API (translate when filter is initialized from input).
@param numFilterGroups the number of filter groups to display
*/
public void setFilters ( int numFilterGroups )
{   //String routine = getClass().getName() + ".setFilters";
    List<InputFilter> filters = new Vector();

    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank
    GenericDatabaseDataStore ds = getDataStore();
    String metaTable = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String locTypeColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String locIdColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String dataSourceColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String scenarioColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );

    // Get lists for choices.  The data type and interval are selected outside the filter and are not included
    // Don't cascade the filters.  Just list unique values for all
    List<String> locTypes = ds.readTimeSeriesMetaLocationTypeList(null, null, null, null, null);
    List<String> locIds = ds.readTimeSeriesMetaLocationIDList(null, null, null, null, null);
    List<String> dataSources = ds.readTimeSeriesMetaDataSourceList(null, null, null, null, null);
    List<String> scenarios = ds.readTimeSeriesMetaScenarioList(null, null, null, null, null);
    
    filters.add(new InputFilter("Location Type",
        metaTable + "." + locTypeColumn, "",
        StringUtil.TYPE_STRING, locTypes, locTypes,
        true,
        "Location type"));
    
    filters.add(new InputFilter("Location ID",
        metaTable + "." + locIdColumn, "",
        StringUtil.TYPE_STRING, locIds, locIds, true,
        "Location identifiers for the location type"));
    
    filters.add(new InputFilter("Data Source",
        metaTable + "." + dataSourceColumn, "",
        StringUtil.TYPE_STRING, dataSources, dataSources, true,
        "Data source (data provider)"));
    
    filters.add(new InputFilter("Scenario",
        metaTable + "." + scenarioColumn, "",
        StringUtil.TYPE_STRING, scenarios, scenarios, true,
        "Scenario helps uniquely identify time series"));
    
    setToolTipText("Database queries can be filtered based on location and time series metadata");
    setInputFilters(filters, numFilterGroups, 25);
}

/**
Return the data store corresponding to this input filter panel.
@return the data store corresponding to this input filter panel.
*/
public GenericDatabaseDataStore getDataStore ( )
{
    return __dataStore;
}

}