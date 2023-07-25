// PropList_TableModel - table model for displaying prop list data.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.GUI.JWorksheet_TableModelListener;

/**
This table model displays PropList data.
Currently it only handles PropLists that have String key/value pairs.
The data model interactions are fully-handled in this class because PropList is not a simple list of data.
*/
@SuppressWarnings("serial")
public class PropList_TableModel extends JWorksheet_AbstractRowTableModel<PropList> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 3;

/**
Reference to the column numbers.
*/
public final int
	COL_KEY = 0,
	COL_TYPE = 1,
	COL_VAL = 2;

/**
Whether the table data are editable or not.
*/
private boolean
	__keyEditable = true,
	__valEditable = true;

/**
The PropList for which data is displayed in the worksheet.
*/
private PropList __props;

/**
The column names.  They can be overridden by calling setKeyColumnName() and setValueColumnName(),
but this must be done before the worksheet displaying the prop list is shown.
*/
private String
	__keyColName = "KEY",
	__typeColName = "TYPE",
	__valColName = "VALUE";

/**
Constructor.
@param props the proplist that will be displayed in the table.
This proplist will be duplicated for display so that changes can be accepted or rejected
by the user before being committed to the proplist read in from a file.
@param keyEditable whether the prop keys can be edited
@param valEditable whether the prop values can be edited
@throws Exception if invalid data were passed in.
*/
public PropList_TableModel(PropList props, boolean keyEditable, boolean valEditable)
// TODO smalers 2021-07-10 need to implement sort, boolean sort)
throws Exception {
	if (props == null) {
		throw new Exception ("Invalid proplist data passed to PropList_TableModel constructor.");
	}
	// Attempt to clone the original property contents to protect from modifying.
	//__props = new PropList(props);
	__props = new PropList(props, true);
	/* TODO smalers 2021-07-10 need to implement case-independent sort.
	if ( sort ) {
		// Sort the properties by name.
		__props.sortList();
	}
	*/
	_rows = __props.size();

	__keyEditable = keyEditable;
	__valEditable = valEditable;
}

/**
Constructor.
@param props the proplist that will be displayed in the table.
This proplist will be duplicated for display so that changes can be accepted or rejected by
the user before being committed to the proplist read in from a file.
@param ignores a list of Strings representing keys that should not be displayed in the table model.  Cannot be null.
@param keyEditable whether the prop keys can be edited
@param valEditable whether the prop values can be edited
@throws Exception if invalid data were passed in.
*/
public PropList_TableModel(PropList props, List<String> ignores, boolean keyEditable, boolean valEditable)
throws Exception {
	if (props == null) {
		throw new Exception ("Invalid proplist data passed to PropList_TableModel constructor.");
	}
	// Attempt to clone the original property contents to protect from modifying.
	//__props = new PropList(props);
	__props = new PropList(props, true);

	int size = ignores.size();
	for (int i = 0; i < size; i++) {
		__props.unSet(ignores.get(i));
	}

	_rows = __props.size();

	__keyEditable = keyEditable;
	__valEditable = valEditable;
}

/**
Adds a row to the table,
called by the worksheet when a call is made to JWorksheet.addRow() or JWorksheet.insertRowAt().
@param prop the object (in this case, should only be a Prop) to insert because
the data object is a full PropList.
@param row the row to insert the object at.
*/
public void addRow(Prop prop) {
	/*
	if (!(o instanceof Prop)) {
		Message.printWarning(2, "PropList_TableModel.addRow()",
			"Only RTi.Util.IO.Prop objects can be added to a PropList table model.");
		return;
	}
	*/
	_rows++;
	__props.getList().add(prop);
}

/**
Deletes a row from the table; called by the worksheet when a call is made to JWorksheet.deleteRow().
@param row the number of the row to delete.
*/
public void deleteRow(int row) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	_rows--;
	__props.getList().remove(row);
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_KEY:
			return String.class;
		case COL_TYPE:
			return String.class;
		case COL_VAL:
			return String.class;
	}
	// Default.
	return String.class;
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_KEY:
			return __keyColName;
		case COL_TYPE:
			return __typeColName;
		case COL_VAL:
			return __valColName;
	}
	// Default.
	return " ";
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_KEY] = "Property name.";
	tips[COL_KEY] = "Property type.";
	tips[COL_VAL] = "Property value.";
	return tips;
}

/**
Returns the format that the specified column should be displayed in when the table is being displayed in the given table format.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_KEY:
			return "%-s";
		case COL_TYPE:
			return "%-s";
		case COL_VAL:
			return "%-s";
	}
	// Default.
	return "%-s";
}

/**
Returns the prop list of properties that are displayed in the worksheet.
@return the prop list of properties that are displayed in the worksheet.
*/
public PropList getPropList() {
	return __props;
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

	Prop p = __props.elementAt(row);
	switch (col) {
		case COL_KEY:
			return p.getKey();
		case COL_TYPE:
			// Return the type that is commonly used such as TSTool SetProperty command.
			Object value = p.getContents();
			// Use for troubleshooting but don't wan't run-time, possibly sensitive data in log file.
			//if ( Message.isDebugOn ) {
			//	if ( value != null ) {
			//		Message.printStatus(2, "", "Property " + p.getKey() + " = " + value);
			//		Message.printStatus(2, "", "Property " + p.getKey() + " = " + p.getContents().getClass());
			//	}
			//}
			if ( value == null ) {
				return "";
			}
			else {
				// Return the Java class even though may be long.
				return value.getClass().getSimpleName();
			}
		case COL_VAL:
			return p.getValue();
	}
	return " ";
}

/**
Returns an array containing the widths (in number of characters) that the fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	// TODO sam 2017-03-15 need to make the widths more intelligent.
	widths[COL_KEY] = 20;
	widths[COL_TYPE] = 10;
	widths[COL_VAL] = 80; // Make wide to handle long strings.

	return widths;
}

/**
Inserts a new row in the table; called by the worksheet when a call is made to JWorksheet.insertRowAt().
@param prop the object (in this case, should only be a Prop) to insert.
@param row the row to insert the object at.
*/
public void insertRowAt(Prop prop, int row) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	/*
	if (!(prop instanceof Prop)) {
		Message.printWarning(2, "PropList_TableModel.insertRowAt()",
			"Only RTi.Util.IO.Prop objects can be inserted to a PropList table model.");
		return;
	}
	*/
	_rows++;
	__props.getList().add(row, (Prop)prop);
}

/**
Returns whether the cell at the given position is editable or not.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (columnIndex == COL_KEY) {
		if (!__keyEditable) {
			return false;
		}
		return true;
	}
	if (columnIndex == COL_TYPE) {
		return false;
	}
	if (columnIndex == COL_VAL) {
		if (!__valEditable) {
			return false;
		}
		return true;
	}
	return true;
}

/**
Overrides the default name of the key column ("KEY").
THIS MUST BE DONE BEFORE THE WORKSHEET IS SHOWN IN THE GUI OR IT WILL NOT WORK.
@param name the name to give to the column.
*/
public void setKeyColumnName(String name) {
	__keyColName = name;
}

/**
Overrides the default name of the key column ("TYPE").
THIS MUST BE DONE BEFORE THE WORKSHEET IS SHOWN IN THE GUI OR IT WILL NOT WORK.
@param name the name to give to the column.
*/
public void setTypeColumnName(String name) {
	__typeColName = name;
}

/**
Overrides the default name of the value column ("VALUE").
THIS MUST BE DONE BEFORE THE WORKSHEET IS SHOWN IN THE GUI OR IT WILL NOT WORK.
@param name the name to give to the column.
*/
public void setValueColumnName(String name) {
	__valColName = name;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	Prop p = (Prop)__props.elementAt(row);

	switch (col) {
		case COL_KEY:
			if (!(p.getKey().equals((String)value))) {
				p.setKey((String)value);
				valueChanged(row, col);
			}
			break;
		case COL_TYPE:
			// Returned based on value but nothing done here.
			break;
		case COL_VAL:
			if (!(p.getValue().equals((String)value))) {
				p.setValue((String)value);
				valueChanged(row, col);
			}
			break;
	}

	super.setValueAt(value, row, col);
}

/**
Called when one of the properties is edited.
@param row the row of the property that was edited.
@param col the column of the property that was edited.
*/
private void valueChanged(int row, int col) {
	int size = _listeners.size();
	JWorksheet_TableModelListener tml = null;
	for (int i = 0; i < size; i++) {
		tml = _listeners.get(i);
		tml.tableModelValueChanged(row, col, null);
	}
}

}