// ----------------------------------------------------------------------------
// JWorksheet_AbstractTableModel - Class from which all the 
//	Table Models that will be used in a JWorksheet should be built.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-20	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-13	JTS, RTi		Added addRow() and deleteRow()
// 2003-06-17	JTS, RTi		_results changed to _data
// 2003-07-07	JTS, RTi		Added code for override cell editable
//					status.
// 2003-07-24	JTS, RTi		Added code for doing consecutive row
//					reads.
// 2003-08-25	JTS, RTi		Added getData()
// 2003-10-10	JTS, RTi		Added getColumnToolTips().
// 2003-10-15	JTS, RTi		Added insertRowAt().
// 2003-10-20	JTS, RTi		Added setRowData().
// 2003-10-22	JTS, RTi		Set _data to initially be an empty 
//					Vector, rather than setting it to null.
// 2003-10-27	JTS, RTi		* Added addTableModelListener().
//					* Added removeTableModelListener().
// 2003-11-18	JTS, RTi		Added finalize().
// 2003-12-03	JTS, RTi		Added getFormat().
// 2004-01-27	JTS, RTi		Added valueChanged().
// 2005-10-20	JTS, RTi		Added shouldDoGetConsecutiveValueAt()
//					and shouldResetGetConsecutiveValueAt().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
This is the class from which all the classes that will be used as 
TableModels in a JWorksheet should be used.  It implements a few core 
data members that all those classes should have, including some sorting support. 
<p>
TODO (JTS - 2006-05-25) If I could do this over, I would combine this table model with 
AbstractRowTableModel, in order to simplify things.  I don't see a very 
good reason to require both of these, honestly.
*/
public abstract class JWorksheet_AbstractTableModel extends AbstractTableModel {

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
can be set by derived classes.
TODO (JTS - 2004-11-30) remove?  subclasses may be using this ...
TODO (JTS - 2005-03-30) no, leave in, and have it called automatically by the worksheet whenever a model
is set in it.  add a setWorksheet() method.
*/
protected JWorksheet _worksheet;

/**
Vector of integer arrays denoting the cells whose default editability has
been overridden.  The int[] arrays consist of:<br><pre>
0 - the row of the cell
1 - the column of the cell
2 - a 1 if the cell is editable, 0 if it is not
</pre>
*/
protected Vector _cellEditOverride = new Vector();

/**
The data that will be shown in the table.
*/
protected List _data = new Vector();

/**
The Vector of table model listeners.
*/
protected Vector _listeners = new Vector();

/**
Adds the object to the table model.
@param o the object to add to the end of the results vector.
*/
public void addRow(Object o) {
	if (_data == null) {
		_data = new Vector();
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

	Vector removes = new Vector();
	if (size > 0) {
		int[] temp;
		for (int i = 0; i < size; i++) {
			temp = (int[])_cellEditOverride.elementAt(i);
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
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	_sortOrder = null;
	_cellEditOverride = null;
	_data = null;
	_listeners = null;
	_worksheet = null;
	super.finalize();
}

/**
Dummy version of the method to get column tool tips for a worksheet.  This one
just returns null, meaning that no tool tips are to be set up.
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
public List getData() {
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
public void insertRowAt(Object o, int pos) {
	if (_data == null) {
		_data = new Vector();
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
			temp = (int[])_cellEditOverride.elementAt(i);
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
	_listeners.removeElement(listener);
}

/**
Sets new data into the table model (used if many rows change at once or all-new data is to be shown.
@param data the list of data objects to be displayed in rows of the table.
*/
public void setNewData(List data) {
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
public void setRowData(Object o, int pos) {
	if (_data == null) {
		_data = new Vector();
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
public void startNewConsecutiveRead() {}

/**
Can be called upon changing values in a table model.  Notifies all listeners
of the change.  Will not be called automatically by the table model.
@param row the row of the changed value
@param col the col of the changed value
@param value the new value.
*/
protected void valueChanged(int row, int col, Object value) {
	for (int i = 0; i < _listeners.size(); i++) {
		((JWorksheet_TableModelListener)_listeners.elementAt(i))
			.tableModelValueChanged(row, col, value);
	}
}

}
