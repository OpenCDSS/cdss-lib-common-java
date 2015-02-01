package riverside.datastore;

import java.util.List;

import RTi.DMI.DatabaseDataStore;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
Table model for displaying data store data in a JWorksheet.
This one table model is used for database and web service data stores because currently
it is easier to show all than split out and make the user pick different categories for viewing.
*/
public class DataStores_TableModel extends JWorksheet_AbstractRowTableModel
{

/**
The table displayed in the worksheet.
*/
private List<DataStore> __dataStoreList = null;

/**
Number of columns in the table model (with the alias).
*/
private int __COLUMNS = 10;

/**
Absolute column indices, for column lookups.
*/
public final int COL_TYPE = 0;
public final int COL_NAME = 1;
public final int COL_DESCRIPTION = 2;
public final int COL_ENABLED = 3;
public final int COL_STATUS = 4;
// Database data store...
public final int COL_DATABASE_SERVER = 5;
public final int COL_DATABASE_NAME = 6;
// Straight ODBC connection...
public final int COL_ODBC_NAME = 7;
// Web service data store...
public final int COL_SERVICE_ROOT_URI = 8;
// General error string
public final int COL_STATUS_MESSAGE = 9;

/**
Constructor.
@param dataStoreList the list of data stores to show in a worksheet.
@throws NullPointerException if the dataTable is null.
*/
public DataStores_TableModel(List<DataStore> dataStoreList) 
throws Exception {
    if ( dataStoreList == null ) {
        _rows = 0;
    }
    else {
        _rows = dataStoreList.size();
    }
    __dataStoreList = dataStoreList;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given
column.  All values are treated as strings.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
    switch (columnIndex) {
        // All handled as strings
        default: return String.class;
    }
}

/**
From AbstractTableMode.  Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
    return __COLUMNS;
}

/**
From AbstractTableMode.  Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
    switch (columnIndex) {
        case COL_TYPE: return "Type";
        case COL_NAME: return "Name";
        case COL_DESCRIPTION: return "Description";
        case COL_ENABLED: return "Enabled";
        case COL_STATUS: return "Status";
        case COL_DATABASE_SERVER: return "Database Server";
        case COL_DATABASE_NAME: return "Database Name";
        case COL_ODBC_NAME: return "ODBC Name";
        case COL_SERVICE_ROOT_URI: return "Web Service Root URI";
        case COL_STATUS_MESSAGE: return "Status Message";
        default: return "";
    }
}

/**
Return tool tips for the columns.
*/
public String[] getColumnToolTips() {
    String [] tooltips = new String[__COLUMNS];
    tooltips[COL_TYPE] = "Datastore type.";
    tooltips[COL_NAME] = "Datastore name.";
    tooltips[COL_DESCRIPTION] = "Datastore description.";
    tooltips[COL_ENABLED] = "Is datastore enabled?  Currently always true if displayed because disabled datastores won't be initialized.";
    tooltips[COL_STATUS] = "Status (Ok/Error) - see Status Message";
    tooltips[COL_DATABASE_SERVER] = "Database server for database datastore.";
    tooltips[COL_DATABASE_NAME] = "Database name for database datastore.";
    tooltips[COL_ODBC_NAME] = "ODBC name when used with generic database datastore.";
    tooltips[COL_SERVICE_ROOT_URI] = "Root URI for web service datastore.";
    tooltips[COL_STATUS_MESSAGE] = "Error message (e.g., when initialization failed).";
    return tooltips;
}

/**
Returns the format to display the specified column.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString()).
*/
public String getFormat ( int column ) {
    switch (column) {
        default: return "%s";
    }
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
    return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the absolute column for which to return data.
@return the data that should be placed in the JTable at the given row and column.
*/
public Object getValueAt(int row, int col)
{   // make sure the row numbers are never sorted ...

    if (_sortOrder != null) {
        row = _sortOrder[row];
    }

    DataStore dataStore = __dataStoreList.get(row);
    if ( dataStore == null ) {
        return "";
    }
    DatabaseDataStore databaseDataStore = null;
    if ( dataStore instanceof DatabaseDataStore ) {
        databaseDataStore = (DatabaseDataStore)dataStore;
    }
    WebServiceDataStore webServiceDataStore = null;
    if ( dataStore instanceof WebServiceDataStore ) {
        webServiceDataStore = (WebServiceDataStore)dataStore;
    }
    switch (col) {
        case COL_TYPE:
            // Use the class name but don't include the package
            String clazz = dataStore.getClass().getName();
            String [] parts = clazz.split("\\.");
            return parts[parts.length - 1];
        case COL_NAME:
        	return dataStore.getName();
        case COL_DESCRIPTION:
        	return dataStore.getDescription();
        case COL_ENABLED:
        	Object o = dataStore.getProperty("Enabled");
        	if ( o == null ) {
        		return "True";
        	}
        	else {
        		return "" + o;
        	}
        case COL_STATUS:
        	int status = dataStore.getStatus();
        	if ( status == 0 ) {
        		return "Ok";
        	}
        	else {
        		return "Error (" + status + ")";
        	}
        case COL_DATABASE_SERVER:
            if ( databaseDataStore != null ) {
                return databaseDataStore.getDMI().getDatabaseServer();
            }
            else {
                return "";
            }
        case COL_DATABASE_NAME:
            if ( databaseDataStore != null ) {
                return databaseDataStore.getDMI().getDatabaseName();
            }
            else {
                return "";
            }
        case COL_ODBC_NAME:
            if ( databaseDataStore != null ) {
                return databaseDataStore.getDMI().getODBCName();
            }
            else {
                return "";
            }
        case COL_SERVICE_ROOT_URI:
            if ( webServiceDataStore != null ) {
                return webServiceDataStore.getServiceRootURI();
            }
            else {
                return "";
            }
        case COL_STATUS_MESSAGE:
        	return dataStore.getStatusMessage();
        default: return "";
    }
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
    int[] widths = new int[__COLUMNS];
    widths[COL_TYPE] = 20;
    widths[COL_NAME] = 20;
    widths[COL_DESCRIPTION] = 45;
    widths[COL_ENABLED] = 10;
    widths[COL_STATUS] = 10;
    widths[COL_DATABASE_SERVER] = 15;
    widths[COL_DATABASE_NAME] = 15;
    widths[COL_ODBC_NAME] = 16;
    widths[COL_SERVICE_ROOT_URI] = 50;
    widths[COL_STATUS_MESSAGE] = 30;
    return widths;
}

}