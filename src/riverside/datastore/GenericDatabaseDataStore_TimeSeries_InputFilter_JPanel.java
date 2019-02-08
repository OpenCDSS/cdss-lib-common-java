// GenericDatabaseDataStore_TimeSeries_InputFilter_JPanel - input filter panel to use with a GenericDatabaseDataStore

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

import java.util.ArrayList;
import java.util.List;

import riverside.datastore.GenericDatabaseDataStore;
import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying GenericDatabaseDataStore database.
*/
@SuppressWarnings("serial")
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
{   String routine = getClass().getSimpleName() + ".setFilters";
    List<InputFilter> filters = new ArrayList<InputFilter>();

    GenericDatabaseDataStore ds = getDataStore();
    String metaTable = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_PROP );
    String locTypeColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCTYPE_COLUMN_PROP );
    String locIdColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_LOCATIONID_COLUMN_PROP );
    String dataSourceColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_DATASOURCE_COLUMN_PROP );
    String scenarioColumn = ds.getProperty ( GenericDatabaseDataStore.TS_META_TABLE_SCENARIO_COLUMN_PROP );

    // Get lists for choices.  The data type and interval are selected outside the filter and are not included
    // Don't cascade the filters.  Just list unique values for all
    List<String> locTypes = new ArrayList<String>();
    if ( (locTypeColumn != null) && !locTypeColumn.equals("") ) {
    	locTypes = ds.readTimeSeriesMetaLocationTypeList(null, null, null, null, null);
    }
    List<String> locIds = new ArrayList<String>();
    if ( (locIds != null) && !locIds.equals("") ) {
    	locIds = ds.readTimeSeriesMetaLocationIDList(null, null, null, null, null);
    }
    List<String> dataSources = new ArrayList<String>();
    if ( (dataSourceColumn != null) && !dataSourceColumn.equals("") ) {
    	dataSources = ds.readTimeSeriesMetaDataSourceList(null, null, null, null, null);
	}
    List<String> scenarios = new ArrayList<String>();
    if ( (scenarioColumn != null) && !scenarioColumn.equals("") ) {
    	scenarios = ds.readTimeSeriesMetaScenarioList(null, null, null, null, null);
    }
    
    // Because GenericDatabaseDataStore is generic, it is difficult to know what will
    // be returned from the above and the filters allow user-entered values.
    // Therefore even if above does not return anything, add the filters below to allow user input.
    
    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank
    
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
    
    // Add additional filters with property name TimeSeriesMetadata_MetadataFilter.1, etc. (number increasing)
    // The property value is a string with comma-separated values indicating:
    // -  Label, shown in the filter drop-down
    // - Database table/view column name (without table/view), used when querying for values
    // - list/field, indicates whether unique list should be provided or text field
    // - editable/noneditable, indicates whether list/field should be editable
    // - description, used for pop-up help

    int i = 0;
    while ( true ) {
    	++i;
    	String propName = GenericDatabaseDataStore.TS_META_TABLE_FILTER_PROP + "." + i;
    	String propVal = ds.getProperty ( propName );
    	if ( propVal == null ) {
    		// Done with filter properties
    		break;
    	}
    	String [] parts = propVal.split(",");
    	if ( parts.length == 5 ) {
    		String label = parts[0].trim();
    		String column = parts[1].trim();
    		String componentType = parts[2].trim();
    		boolean editable = false;
    		if ( parts[3].trim().equalsIgnoreCase("true")) {
    			editable = true;
    		}
    		String description = parts[4].trim();
    		// TODO SAM 2015-02-06 For now always add as field
    		editable = true;
    		componentType = "field";
    		if ( componentType.equalsIgnoreCase("list") ) {
    			// Query the choices from the database and add as a list
    		}
    		else {
    			// Add as a field.
        	    filters.add(new InputFilter(label,
    	            metaTable + "." + column, "",
    	            StringUtil.TYPE_STRING, null, null, editable,
    	            description));
    		}
    	}
    	else {
    		Message.printWarning(3,routine,"Datastore configuration property \"" + propName + "\" does not provide exactly 5 configuration values.");
    	}
    }
    
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
