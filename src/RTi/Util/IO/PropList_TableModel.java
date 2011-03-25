// ----------------------------------------------------------------------------
// PropList_TableModel - table model for displaying prop list data.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-10-27	J. Thomas Sapienza, RTi	Initial version.
// 2004-11-29	JTS, RTi		* Added insertRowAt().
//					* Added addRow().
//					* Made the column reference numbers
//					  public for access in apps.
//					* Added deleteRow().
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.GUI.JWorksheet_TableModelListener;

import RTi.Util.Message.Message;

/**
This table model displays proplist data.  Currently it only handles proplists
that have String key/value pairs.  <p>
TODO (JTS - 2003-10-27) Add support for Object-storing props, or simply exclude them from being displayed.
*/
public class PropList_TableModel
extends JWorksheet_AbstractRowTableModel {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 3;

/**
Reference to the column numbers.
*/
public final int
	COL_KEY = 1,
	COL_VAL = 2;

/**
Whether the table data is editable or not.
*/
private boolean 
	__keyEditable = true,
	__valEditable = true;

/**
The PropList for which data is displayed in the worksheet.
*/
private PropList __props;

/**
The column names.  They can be overridden by calling setKeyColumnName() and
setValueColumnName(), but this must be done before the worksheet displaying the prop list is shown.
*/
private String 
	__keyColName = "KEY",
	__valColName = "VALUE";

/**
Constructor.  
@param props the proplist that will be displayed in the table.  This proplist 
will be duplicated for display so that changes can be accepted or rejected 
by the user before being committed to the proplist read in from a file.
@param keyEditable whether the prop keys can be edited
@param valEditable whether the prop values can be edited
@throws Exception if invalid data were passed in.
*/
public PropList_TableModel(PropList props, boolean keyEditable, boolean valEditable)
throws Exception {
	if (props == null) {
		throw new Exception ("Invalid proplist data passed to PropList_TableModel constructor.");
	}
	__props = new PropList(props);
	_rows = __props.size();

	__keyEditable = keyEditable;
	__valEditable = valEditable;
}

/**
Constructor.  
@param props the proplist that will be displayed in the table.  This proplist 
will be duplicated for display so that changes can be accepted or rejected by 
the user before being committed to the proplist read in from a file.
@param ignores a Vector of Strings representing keys that should not be 
displayed in the table model.  Cannot be null.
@param keyEditable whether the prop keys can be edited
@param valEditable whether the prop values can be edited
@throws Exception if invalid data were passed in.
*/
public PropList_TableModel(PropList props, List<String> ignores, boolean keyEditable, boolean valEditable)
throws Exception {
	if (props == null) {
		throw new Exception ("Invalid proplist data passed to PropList_TableModel constructor.");
	}
	__props = new PropList(props);

	int size = ignores.size();
	for (int i = 0; i < size; i++) {
		__props.unSet(ignores.get(i));
	}
	
	_rows = __props.size();

	__keyEditable = keyEditable;
	__valEditable = valEditable;
}

/**
Adds a row to the table; called by the worksheet when a call is made to 
JWorksheet.addRow() or JWorksheet.insertRowAt().
@param o the object (in this case, should only be a Prop) to insert.
@param row the row to insert the object at.
*/
public void addRow(Object o) {
	if (!(o instanceof Prop)) {
		Message.printWarning(2, "PropList_TableModel.addRow()",	
			"Only RTi.Util.IO.Prop objects can be added to a PropList table model.");
		return;
	}
	_rows++;
	__props.getList().add((Prop)o);
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
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__props = null;
	__keyColName = null;
	__valColName = null;
	super.finalize();
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_KEY:	return String.class;
		case COL_VAL:	return String.class;
	}
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
		case COL_KEY:	return __keyColName;
		case COL_VAL:	return __valColName;
	}
	return " ";
}

/**
Returns the text to be assigned to worksheet tooltips.
@return a String array of tool tips.
*/
public String[] getColumnToolTips() {
	String[] tips = new String[__COLUMNS];

	tips[COL_KEY] = "This is the value of the key in the proplist.";
	tips[COL_VAL] = "This is the value associated with the key in the proplist.";
	return tips;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case 1: return "%-256s";
		case 2: return "%-256s";
	}
	return "%8s";
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

	Prop p = (Prop)__props.elementAt(row);
	switch (col) {
		case COL_KEY: return p.getKey();
		case COL_VAL: return p.getValue();
	}
	return " ";
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	widths[COL_KEY] = 20;
	widths[COL_VAL] = 20;

	return widths;
}

/**
Inserts a new row in the table; called by the worksheet when a call is made to JWorksheet.insertRowAt().
@param o the object (in this case, should only be a Prop) to insert.
@param row the row to insert the object at.
*/
public void insertRowAt(Object o, int row) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	if (!(o instanceof Prop)) {
		Message.printWarning(2, "PropList_TableModel.insertRowAt()",	
			"Only RTi.Util.IO.Prop objects can be inserted to a PropList table model.");
		return;
	}
	_rows++;
	__props.getList().add(row, (Prop)o);
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
	if (columnIndex == COL_VAL) {
		if (!__valEditable) {
			return false;
		}
		return true;
	}
	return true;
}

/**
Overrides the default name of the key column ("KEY") -- THIS MUST BE DONE BEFORE
THE WORKSHEET IS SHOWN IN THE GUI OR IT WILL NOT WORK.
@param name the name to give to the column.
*/
public void setKeyColumnName(String name) {
	__keyColName = name;
}

/**
Overrides the default name of the value column ("VALUE") -- THIS MUST BE DONE 
BEFORE THE WORKSHEET IS SHOWN IN THE GUI OR IT WILL NOT WORK.
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
		tml = (JWorksheet_TableModelListener)_listeners.elementAt(i);
		tml.tableModelValueChanged(row, col, null);
	}
}

}