// JWorksheet_AbstractTableModel - base class for JWorksheet table models

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

package RTi.Util.GUI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

// TODO sam 2017-03-15 Need to make the data private and set the data via a constructor,
// and allow a constructor with no data since that may be managed in a child class.

/**
This is the class from which all the classes that will be used as 
TableModels in a JWorksheet should be used.  It implements a few core 
data members that all those classes should have, including some sorting support. 
<p>
TODO (JTS - 2006-05-25) If I could do this over, I would combine this table model with 
AbstractRowTableModel, in order to simplify things.  I don't see a very 
good reason to require both of these, honestly.
*/
@SuppressWarnings("serial")
public abstract class JWorksheet_AbstractTableModel<T> extends AbstractTableModel {

/**
Holds the sorted order of the records to be displayed.
*/
protected int[] _sortOrder = null;

/**
The number of rows in the results.
*/
protected int _rows = 0;

/**
The type of format in which the columns should be displayed.
*/
protected int _type = -1;

/**
The worksheet that this table model works with.  This is here so that it 
can be set by derived classes.  This class provides a built-in List<T> data array
that allows basic table model interaction, such as interacting with rows.
If a List<T> is not appropriate for the data model, then manage the data
in the child class and provide appropriate methods.
TODO (JTS - 2004-11-30) remove?  subclasses may be using this ...
TODO (JTS - 2005-03-30) no, leave in, and have it called automatically by the worksheet whenever a model
is set in it.  add a setWorksheet() method.
*/
protected JWorksheet _worksheet;

/**
List of integer arrays denoting the cells whose default editability has
been overridden.  The int[] arrays consist of:<br><pre>
0 - the row of the cell
1 - the column of the cell
2 - a 1 if the cell is editable, 0 if it is not
</pre>
*/
protected List<int[]> _cellEditOverride = new ArrayList<int[]>();

/**
The data that will be shown in the table.
*/
protected List<T> _data = new ArrayList<>();

/**
The list of table model listeners.
*/
protected List<JWorksheet_TableModelListener> _listeners = new ArrayList<>();

/**
Adds the object to the table model.
@param o the object to add to the end of the results list.
*/
public void addRow(T o) {
	if (_data == null) {
		_data = new ArrayList<>();
		_data.add(o);
	}
	else {
		_data.add(o);
	}
	_rows++;
}

/**
Adds a table model listener to the list of table model listeners.
@param listener the listener to add
*/
public void addTableModelListener(JWorksheet_TableModelListener listener) {
	_listeners.add(listener);
}

/**
Clears the data from the table model and empties the table.
*/
public void clear() {
	_data = null;
	_rows = 0;
}

/**
Removes a row's editability override and lets the normal cell editing rules take effect.
@param row the row for which to remove the cell editability.
*/
public void clearOverrideCellEdit(int row) {
	int size = _cellEditOverride.size();

	List<Integer> removes = new ArrayList<>();
	if (size > 0) {
		int[] temp;
		for (int i = 0; i < size; i++) {
			temp = (int[])_cellEditOverride.get(i);
			if (temp[0] == row) {
				removes.add(new Integer(i));
			}
		}
	}
	
	size = removes.size();
	for (int i = (size - 1); i >= 0; i--) { 
		_cellEditOverride.remove(row);
	}
}

/**
Deletes the specified row from the table model.
@param row the row to delete.
*/
public void deleteRow(int row) {
	if (_data == null) {
		return;
	}
	if (row < 0 || row > _data.size()) {
		return;
	}
	_rows--;
	_data.remove(row);

	clearOverrideCellEdit(row);
}

/**
Dummy version of the method to get column tool tips for a worksheet.
This one just returns null, meaning that no tool tips are to be set up.
*/
public String[] getColumnToolTips() {
	return null;
}

/**
Used for reading consecutive values from table models in which the values of 
one row depend on the values of the rows before them (e.g., Time Series dates
or running averages).  When a consecutive read is made, the table model is 
guaranteed that the previous row that was operated on was either the current
row (the column could be different) or the previous row.<p>
The default implementation of this method simply pipes through the call to getValueAt(row, column).<p>
<b>Note:</b> if a class overrides this method, it should also override startNewConsecutiveRead().
@param row the row from which to read data
@param column the column from which to read data
@return the data read from the given row and column.
*/
public Object getConsecutiveValueAt(int row, int column) {
	return getValueAt(row, column);
}

/**
Returns the data stored in the table model.
@return the data stored in the table model.
*/
public List<T> getData() {
	return _data;
}

/**
Returns the format in which the given column should be formatted.
@param absoluteColumn the absolute column for which to return the format.
@return the format in which the given column should be formatted.
*/
public abstract String getFormat(int absoluteColumn);

/**
Inserts a row at the specified position.
@param o the object that stores a row of data.
@param pos the position at which to insert the row.
*/
public void insertRowAt(T o, int pos) {
	if (_data == null) {
		_data = new ArrayList<>();
		_data.add(o);
	}
	else {
		_data.add(pos, o);
	}
	_rows++;
}

/**
Return the sort order array.  If the data in the table have been sorted, this array
is needed to access to original data in the proper order.
*/
public int [] getSortOrder ()
{
    return _sortOrder;
}

/**
Overrides the default cell editability of the specified cell and sets the 
cell to be editable or not depending on the value of state.
@param row row of the cell
@param column column of the cell
@param state whether the cell should be editable (true) or not (false)
*/
public void overrideCellEdit(int row, int column, boolean state) {
	int size = _cellEditOverride.size();

	if (size > 0) {
		int[] temp;
		for (int i = 0; i < size; i++) {
			temp = (int[])_cellEditOverride.get(i);
			if (temp[0] == row && temp[1] == column) {
				if (state) {
					temp[2] = 1;
				}
				else {
					temp[2] = 0;
				}
				return;
			}
		}
	}

	int[] cell = new int[3];
	cell[0] = row;
	cell[1] = column;
	if (state) {
		cell[2] = 1;
	}
	else {
		cell[2] = 0;
	}
	_cellEditOverride.add(cell);
}

/**
Removes a table model listener from the list of table model listeners.
@param listener the table model listener to remove.
*/
public void removeTableModelListener(JWorksheet_TableModelListener listener) {
	_listeners.remove(listener);
}

/**
Sets new data into the table model (used if many rows change at once or all-new data is to be shown.
@param data the list of data objects to be displayed in rows of the table.
*/
public void setNewData(List<T> data) {
	_data = data;
	if (data == null) {
		_rows = 0;
	}
	else {
		_rows = _data.size();
	}
}

/**
Changes a row of data by replacing the old row with the new data.
@param o the object that stores a row of data.
@param pos the position at which to set the data row.
*/
public void setRowData(T o, int pos) {
	if (_data == null) {
		_data = new ArrayList<>();
		_data.add(o);
	}
	else {
		_data.set(pos,o);
	}
}

/**
Sets the sorted order for the records to be displayed in.  This method is in
this base class for all the table models used in a JWorksheet so that the 
base JWorksheet can be sure that all of its models will have this method.
@param sortOrder the sorted order in which records should be displayed.  
The record that would normally go at position X will now be placed at position 'sortOrder[x]'
*/
public void setSortedOrder(int[] sortOrder) {
	_sortOrder = sortOrder;
}

boolean __shouldDoGetConsecutiveValueAt = false;

public boolean shouldDoGetConsecutiveValueAt() {
	return __shouldDoGetConsecutiveValueAt;
}

public void shouldDoGetConsecutiveValueAt(boolean should) {
	__shouldDoGetConsecutiveValueAt = should;
}

/**
TODO SAM 2014-03-30 What does this do?
*/
public boolean __shouldResetGetConsecutiveValueAt = false;

public boolean shouldResetGetConsecutiveValueAt() {
	return __shouldResetGetConsecutiveValueAt;
}

public void shouldResetGetConsecutiveValueAt(boolean should) {
	__shouldResetGetConsecutiveValueAt = should;
}

/**
Used to notify the table model of a new consecutive row read.  Consecutive reads
might be used in places where the values of a row depend on the values of the
previous rows (e.g., Time Series dates, running averages).  In a consecutive
read, the table model is guaranteed that the row in the call to
getValueAt() is either the same row that was read from last, or one greater
than the row that was read from last time.
*/
public void startNewConsecutiveRead() {
}

/**
Can be called upon changing values in a table model.  Notifies all
JWorksheet_TableModelListener listeners
of the change.  Will not be called automatically by the table model.
@param row the row (0+) of the changed value
@param col the col (0+) of the changed value
@param value the new value for the cell indicated by row and column.
*/
protected void valueChanged(int row, int col, Object value) {
	for (int i = 0; i < _listeners.size(); i++) {
		_listeners.get(i).tableModelValueChanged(row, col, value);
	}
}

}