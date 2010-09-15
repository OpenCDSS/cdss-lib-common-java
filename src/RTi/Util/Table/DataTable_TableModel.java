// ----------------------------------------------------------------------------
// DataTable_TableModel - table model for displaying DataTable values in 
//	a JWorksheet.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-21	J. Thomas Sapienza, RTi	Initial version.
// 2004-01-22	JTS, RTi		Removed 0th column to account for new
//					style of doing row headers.
// 2004-05-24	JTS, RTi		Corrected bug in determineClasses()
//					in which Doubles were being cast as
//					Integers.
// 2004-08-06	JTS, RTi		getColumnName() now checks to see
//					if a column prefix has been set.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.Table;

import java.util.Date;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.IO.IOUtil;

/**
Table model for displaying data table data in a JWorksheet.
*/
public class DataTable_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
The classes of the fields, stored in an array for quicker access.
*/
private Class[] __fieldClasses;

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
	__fieldClasses = determineClasses(__dataTable.getFieldDataTypes());
}

/**
Determines the kind of classes stored in each table field.
@param dataTypes the data types array from the data table.
@return an array of the Class of each field.
*/
private Class[] determineClasses(int[] dataTypes) {
	Class[] classes = new Class[dataTypes.length];

	for (int i = 0; i < dataTypes.length; i++) {
		switch (dataTypes[i]) {
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
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	return __fieldFormats[column];
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
		return __dataTable.getFieldValue(row, col);
	}
	catch (Exception e) {
		e.printStackTrace();
		return "";
	}
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