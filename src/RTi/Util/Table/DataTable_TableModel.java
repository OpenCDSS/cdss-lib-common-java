// DataTable_TableModel - table model for displaying data table data in a JWorksheet

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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

package RTi.Util.Table;

import java.util.Date;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Table model for displaying data table data in a JWorksheet.
*/
@SuppressWarnings("serial")
public class DataTable_TableModel
extends JWorksheet_AbstractRowTableModel<DataTable> {

/**
The classes of the fields, stored in an array for quicker access.
*/
private Class<?>[] fieldClasses;

/**
The field types as per the table field types.
*/
private int [] fieldTypes;

/**
The table displayed in the worksheet.
*/
private DataTable dataTable;

/**
The number of columns in the table model.
*/
private int columns = 0;

/**
The formats of the table fields, stored in an array for quicker access.
*/
private String[] fieldFormats;

/**
The names of the table fields, stored in the array for quicker access.
*/
private String[] fieldNames;

/**
Constructor.
@param dataTable the table to show in a worksheet.
@throws NullPointerException if the dataTable is null.
*/
public DataTable_TableModel ( DataTable dataTable )
throws Exception {
	if ( dataTable == null ) {
		throw new NullPointerException();
	}

	this.dataTable = dataTable;
	super._rows = this.dataTable.getNumberOfRecords();
	this.columns = this.dataTable.getNumberOfFields();

	this.fieldNames = this.dataTable.getFieldNames();
	this.fieldFormats = this.dataTable.getFieldFormats();
	this.fieldTypes = this.dataTable.getFieldDataTypes();
	this.fieldClasses = determineClasses(this.fieldTypes);
}

/**
Determines the kind of classes stored in each table field.
@param dataTypes the data types array from the data table.
@return an array of the Class of each field.
*/
private Class<?>[] determineClasses ( int[] dataTypes ) {
	Class<?>[] classes = new Class[dataTypes.length];

	for (int i = 0; i < dataTypes.length; i++) {
		if ( this.dataTable.isColumnArray(dataTypes[i]) ) {
			classes[i] = String.class;
		}
		else {
			switch (dataTypes[i]) {
				case TableField.DATA_TYPE_ARRAY:
					// For the purposes of rendering in the table, treat array as formatted string "[ , , , ]".
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
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class (0+).
*/
public Class<?> getColumnClass ( int columnIndex ) {
	return this.fieldClasses[columnIndex];
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return this.columns;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name (0+).
@return the name of the column at the given position.
*/
public String getColumnName ( int columnIndex ) {
	String prefix = "";
	if ( super._worksheet != null ) {
		prefix = super._worksheet.getColumnPrefix(columnIndex);
	}
	return prefix + this.fieldNames[columnIndex];
}

/**
Returns an array containing the column tool tips.
@return a String array containing the tool tips for each field (the field descriptions are used).
*/
public String[] getColumnToolTips() {
    String[] tips = new String[this.columns];
    for (int i = 0; i < this.columns; i++) {
        tips[i] = this.dataTable.getTableField(i).getDescription();
    }
    return tips;
}

/**
Returns an array containing the widths that the fields in the table should be sized to.
This is not equivalent to the maximum characters in the column but are correlated.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	String routine = getClass().getSimpleName() + ".getColumnWidths";
	boolean debug = false;
	// The number of columns is set in the constructor.
    int[] widths = new int[this.columns];
    for ( int i = 0; i < this.columns; i++ ) {
    	// Default the column width to the data table field width.
        widths[i] = this.dataTable.getFieldWidth(i);
        if ( debug ) {
        	Message.printStatus(2, routine, "Table '" + this.dataTable.getTableID() + "' model width[" + i + "] from data table is " + widths[i]);
        }
        if ( widths[i] < 0 ) {
        	// Table column does not have the width set so use a default value.
            widths[i] = 15; // Default.
            if ( debug ) {
            	Message.printStatus(2, routine, "Table '" + this.dataTable.getTableID()+ "' model width[" + i + "] default is " + widths[i]);
            }
        }
    }
    return widths;
}

/**
Returns the format to be applied to data values in the column, for display in the table.
If the column contains an array, the format applies to the individual values in the array.
@param column column for which to return the format (0+).
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (this.fieldTypes[column]) {
		case TableField.DATA_TYPE_ARRAY:
			// For the purposes of rendering in the table, treat array as formatted string.
			return "%s";
		default:
			return this.fieldFormats[column];
	}
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return super._rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data (0+).
@param col the column for which to return data (0+).
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if ( super._sortOrder != null ) {
		row = super._sortOrder[row];
	}

	try {
		if ( this.dataTable.isColumnArray(this.fieldTypes[col]) ) {
			// Column is an array of primitive types.
			return this.dataTable.formatArrayColumn(row,col);
		}
		else {
			return this.dataTable.getFieldValue(row, col);
		}
	}
	catch (Exception e) {
		Message.printWarning(3, "getValueAt", "Error processing column \"" + getColumnName(col) + "\"");
		Message.printWarning(3, "getValueAt", e);
		return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable (0+).
@return whether the cell at the given position is editable (always return false).
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value (0+).
@param col the col of the cell for which to set the value (0+).
*/
public void setValueAt(Object value, int row, int col) {
	super.setValueAt(value, row, col);
}

}