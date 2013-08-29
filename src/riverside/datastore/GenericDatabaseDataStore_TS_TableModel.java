package riverside.datastore;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for time series metadata information.
By default the sheet will contain row and column numbers.
*/
public class GenericDatabaseDataStore_TS_TableModel extends JWorksheet_AbstractRowTableModel
{

/**
Number of columns in the table model (with the alias).
*/
private int __COLUMNS = 11;

/**
Absolute column indices, for column lookups.
*/
public final int COL_LOC_TYPE = 0;
public final int COL_ID = 1;
//public final int COL_ALIAS = 1;
public final int COL_DESC = 2;
public final int COL_DATA_SOURCE= 3;
public final int COL_DATA_TYPE = 4;
public final int COL_TIME_STEP = 5;
public final int COL_SCENARIO = 6;
//public final int COL_SEQUENCE = 7;
public final int COL_UNITS = 7;
public final int COL_START = 8;
public final int COL_END = 9;
public final int COL_INPUT_TYPE	= 10;
//public final int COL_INPUT_NAME	= 12;

/**
Datastore that is providing the data.
*/
private GenericDatabaseDataStore __dataStore = null;

/**
Constructor.  This builds the model for displaying the given time series data.
@param data the list of TimeSeriesMeta that will be displayed in the table (null is allowed).
location column.  The JWorksheet.removeColumn ( COL_ALIAS ) method should be called.
@throws Exception if an invalid results passed in.
*/
public GenericDatabaseDataStore_TS_TableModel ( List data, GenericDatabaseDataStore dataStore )
throws Exception
{	if ( data == null ) {
		_rows = 0;
	}
	else {
	    _rows = data.size();
	}
	_data = data;
	__dataStore = dataStore;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given
column.  All values are treated as strings.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
	    case COL_LOC_TYPE: return String.class;
		case COL_ID: return String.class;
		case COL_DESC: return String.class;
		case COL_DATA_SOURCE: return String.class;
		case COL_DATA_TYPE: return String.class;
		case COL_TIME_STEP: return String.class;
		case COL_SCENARIO: return String.class;
		case COL_UNITS: return String.class;
		case COL_START: return String.class;
		case COL_END: return String.class;
		case COL_INPUT_TYPE: return String.class;
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
	    case COL_LOC_TYPE: return "Location\nType";
		case COL_ID: return "\nID";
		case COL_DESC: return "Name/\nDescription";
		case COL_DATA_SOURCE: return "Data\nSource";
		case COL_DATA_TYPE: return "Data\nType";
		case COL_TIME_STEP: return "Time\nStep";
		case COL_SCENARIO: return "\nScenario";
		case COL_UNITS: return "\nUnits";
		case COL_START: return "\nStart";
		case COL_END: return "\nEnd";
		case COL_INPUT_TYPE: return "Input\nType";
		default: return "";
	}
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
{	// make sure the row numbers are never sorted ...

	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	TimeSeriesMeta meta = (TimeSeriesMeta)_data.get(row);
	if ( meta == null ) {
		return "";
	}
	switch (col) {
	    case COL_LOC_TYPE: return meta.getLocationType();
		case COL_ID: return meta.getLocationID();
		case COL_DESC: return meta.getDescription();
		case COL_DATA_SOURCE: return meta.getDataSource();
		case COL_DATA_TYPE: return meta.getDataType();
		case COL_TIME_STEP: return meta.getInterval();
		case COL_SCENARIO: return meta.getScenario();
		case COL_UNITS:
		    return meta.getUnits();
		case COL_START:
		    return "";
		case COL_END:
		    return "";
	    case COL_INPUT_TYPE:
	            return __dataStore.getName();
		default:
		    return "";
	}
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	widths[COL_LOC_TYPE] = 12;
	widths[COL_ID] = 12;
	widths[COL_DESC] = 20;
	widths[COL_DATA_SOURCE] = 10;
	widths[COL_DATA_TYPE] = 8;
	widths[COL_TIME_STEP] = 8;
	widths[COL_SCENARIO] = 8;
	widths[COL_UNITS] = 8;
	widths[COL_START] = 10;
	widths[COL_END] = 10;
	widths[COL_INPUT_TYPE] = 12;
	return widths;
}

}