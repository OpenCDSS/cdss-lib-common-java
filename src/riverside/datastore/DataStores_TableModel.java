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
private int __COLUMNS = 6;

/**
Absolute column indices, for column lookups.
*/
public final int COL_TYPE = 0;
public final int COL_NAME = 1;
public final int COL_DESCRIPTION = 2;
// Database data store...
public final int COL_DATABASE_SERVER = 3;
public final int COL_DATABASE_NAME = 4;
// Web service data store...
public final int COL_SERVICE_ROOT_URI = 5;

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
        case COL_TYPE: return "\nType";
        case COL_NAME: return "\nName";
        case COL_DESCRIPTION: return "\nDescription";
        case COL_DATABASE_SERVER: return "Database\nServer";
        case COL_DATABASE_NAME: return "Database\nName";
        case COL_SERVICE_ROOT_URI: return "Web Service\nRoot URI";
        default: return "";
    }
}

/**
Return tool tips for the columns.
*/
public String[] getColumnToolTips() {
    String [] tooltips = new String[__COLUMNS];
    tooltips[COL_TYPE] = "Data store type.";
    tooltips[COL_NAME] = "Data store name.";
    tooltips[COL_DESCRIPTION] = "Data store description.";
    tooltips[COL_DATABASE_SERVER] = "Database server for database data store.";
    tooltips[COL_DATABASE_NAME] = "Database name for database data store.";
    tooltips[COL_SERVICE_ROOT_URI] = "Root URI for web service data store.";
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
        case COL_NAME: return dataStore.getName();
        case COL_DESCRIPTION: return dataStore.getDescription();
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
        case COL_SERVICE_ROOT_URI:
            if ( webServiceDataStore != null ) {
                return webServiceDataStore.getServiceRootURI();
            }
            else {
                return "";
            }
        default: return "";
    }
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
    int[] widths = new int[__COLUMNS];
    widths[COL_TYPE] = 15;
    widths[COL_NAME] = 20;
    widths[COL_DESCRIPTION] = 45;
    widths[COL_DATABASE_SERVER] = 12;
    widths[COL_DATABASE_NAME] = 12;
    widths[COL_SERVICE_ROOT_URI] = 45;
    return widths;
}

}