package RTi.Util.Table;

import java.util.Date;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Table model for displaying data table data in a JWorksheet.
*/
@SuppressWarnings("serial")
public class DataTable_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
The classes of the fields, stored in an array for quicker access.
*/
private Class[] __fieldClasses;

/**
The field types as per the table field types.
*/
private int [] __fieldTypes;

/**
The table displayed in the worksheet.
*/
private DataTable __dataTable;

/**
The number of columns in the table model.
*/
private int __columns = 0;

/**
The formats of the table fields, stored in an array for quicker access.
*/
private String[] __fieldFormats;

/**
The names of the table fields, stored in the array for quicker access.
*/
private String[] __fieldNames;

/**
Constructor.
@param dataTable the table to show in a worksheet.
@throws NullPointerException if the dataTable is null.
*/
public DataTable_TableModel(DataTable dataTable) 
throws Exception {
	if (dataTable == null) {
		throw new NullPointerException();
	}

	__dataTable = dataTable;
	_rows = __dataTable.getNumberOfRecords();
	__columns = __dataTable.getNumberOfFields();

	__fieldNames = __dataTable.getFieldNames();
	__fieldFormats = __dataTable.getFieldFormats();
	__fieldTypes = __dataTable.getFieldDataTypes();
	__fieldClasses = determineClasses(__fieldTypes);
}

/**
Determines the kind of classes stored in each table field.
@param dataTypes the data types array from the data table.
@return an array of the Class of each field.
*/
private Class[] determineClasses(int[] dataTypes) {
	Class[] classes = new Class[dataTypes.length];

	for (int i = 0; i < dataTypes.length; i++) {
		if ( __dataTable.isColumnArray(dataTypes[i]) ) {
			classes[i] = String.class;
		}
		else {
			switch (dataTypes[i]) {
				case TableField.DATA_TYPE_ARRAY:
					// For the purposes of rendering in the table, treat array as formatted string "[ , , , ]"
					classes[i] = String.class;
					break;
				case TableField.DATA_TYPE_BOOLEAN:
					classes[i] = Boolean.class;
					break;
				case TableField.DATA_TYPE_INT:
					classes[i] = Integer.class;
					break;
				case TableField.DATA_TYPE_SHORT:
					classes[i] = Short.class;
					break;
				case TableField.DATA_TYPE_DOUBLE:
					classes[i] = Double.class;
					break;
				case TableField.DATA_TYPE_FLOAT:
					classes[i] = Float.class;
					break;
				case TableField.DATA_TYPE_STRING:
					classes[i] = String.class;
					break;
				case TableField.DATA_TYPE_DATE:
					classes[i] = Date.class;
					break;
	            case TableField.DATA_TYPE_DATETIME:
	                classes[i] = DateTime.class;
	                break;
	            case TableField.DATA_TYPE_LONG:
	                classes[i] = Long.class;
	                break;
	            default:
	            	throw new RuntimeException ( "TableField data type " + dataTypes[i] + " is not supported in DataTable table model." ); 
			}
		}
	}
	return classes;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__fieldClasses);
	__dataTable = null;
	IOUtil.nullArray(__fieldFormats);
	IOUtil.nullArray(__fieldNames);
	super.finalize();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	return __fieldClasses[columnIndex];
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __columns;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	String prefix = "";
	if (_worksheet != null) {
		prefix = _worksheet.getColumnPrefix(columnIndex);
	}
	return prefix + __fieldNames[columnIndex];
}

/**
Returns an array containing the column tool tips.
@return a String array containing the tool tips for each field (the field descriptions are used).
*/
public String[] getColumnToolTips() {
    String[] tips = new String[__columns];
    for (int i = 0; i < __columns; i++) {
        tips[i] = __dataTable.getTableField(i).getDescription();
    }
    return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
    int[] widths = new int[__columns];
    for (int i = 0; i < __columns; i++) {
        widths[i] = __dataTable.getFieldWidth(i);
        if ( widths[i] < 0 ) {
            widths[i] = 15; // Default
        }
    }
    return widths;
}

/**
Returns the format to be applied to data values in the column, for display in the table.
If the column contains an array, the format applies to the individual values in the array.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (__fieldTypes[column]) {
		case TableField.DATA_TYPE_ARRAY:
			// For the purposes of rendering in the table, treat array as formatted string
			return "%s";
		default:
			return __fieldFormats[column];
	}
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	try {
		if ( __dataTable.isColumnArray(__fieldTypes[col]) ) {
			// Column is an array of primitive types
			return __dataTable.formatArrayColumn(row,col);
		}
		else {
			return __dataTable.getFieldValue(row, col);
		}
	}
	catch (Exception e) {
		Message.printWarning(3, "getValueAt", "Error processing column \"" + getColumnName(col) + "\"");
		Message.printWarning(3, "getValueAt", e);
		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns above #2 are editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	super.setValueAt(value, row, col);
}

}