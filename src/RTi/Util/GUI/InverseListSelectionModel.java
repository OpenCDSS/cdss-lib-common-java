// InverseListSelectionModel - class to allow inverse list selections (all selected first)

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//---------------------------------------------------------------------------
// InverseListSelectionModel - class to allow inverse list selections, where
// 	all values are selected by default and the user selects the items to
// 	deselect.
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
// 2003-03-26	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
//---------------------------------------------------------------------------

package RTi.Util.GUI;

import javax.swing.DefaultListSelectionModel;

/**
This class provides a list selection model in which the default
mode is for all the rows to be selected and cells are deselected when they
are clicked on.  This is the inverse of the way the list usually works.  <p>
Developers do not need to interact directly with this class, instead, they 
should use a SimpleJList and call setInverseListSelection(true) on that, like
the following:<p>
<code>
        SimpleJList divisionJList = new SimpleJList(names);
	divisionJList.setInverseListSelection(true);
</code>
*/
@SuppressWarnings("serial")
public class InverseListSelectionModel 
extends DefaultListSelectionModel {

/**
An array which remembers which rows should (true) and shouldn't be (false)
selected.
*/
private boolean[] __selected;

/**
Constructor.
@param size the number of elements in the JList that should be monitored 
for selection or not.
*/
public InverseListSelectionModel(int size) {
	initialize(size);
}

/**
Used when a new item is added to the list to preserve the old selections.
@param index the location of the new item.
*/
public void add(int index) {
	int length = __selected.length;
	boolean[] temp = new boolean[length];
	for (int i = 0; i < length; i++) {
		temp[i] = __selected[i];
	}
	__selected = new boolean[length + 1];
	if (index == (length + 1)) {
		__selected[index - 1] = false;
		return;
	}
	int i = 0;
	for (i = 0; i < index; i++) {
		__selected[i] = temp[i];
	}
	__selected[i] = false;
	for (i = (index + 1); i < length; i++) {
		__selected[i] = temp[i];
	}
}		

/**
Adds an interval of values to the selected list. 
Overrides method in DefaultListSelectionModel. 
@param row0 the first row to be selected.
@param row1 the last row to be selected.
*/
public void addSelectionInterval(int row0, int row1) {
	__selected[row0] = true;
	super.addSelectionInterval(row0, row0);
}

/**
Clears all selected rows.  Overrides method in DefaultListSelectionModel.
*/
public void clearSelection() {
	for (int i = 0; i < __selected.length; i++) {
		__selected[i] = false;
	}
	super.removeSelectionInterval(0, (__selected.length - 1));
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__selected = null;
	super.finalize();
}

/**
Gets the highest-selected row.  Overrides method in DefaultListSelectionModel.
@return the highest selected row, or -1 if no rows are selected.
*/
public int getMaxSelectionIndex() {
	for (int i = (__selected.length - 1); i >= 0; i--) {
		if (__selected[i]) {
			return i;
		}
	}
	return -1;
}

/**
Gets the lowest-selected row.  Overrides method in DefaultListSelectionModel.
@return the lowest-selected row, or -1 if no rows are selected.
*/
public int getMinSelectionIndex() {
	for (int i = 0; i < __selected.length; i++) {
		if (__selected[i]) {
			return i;
		}
	}
	return -1;
}


/**
Initializes the __selected array and also initializes the entire list to be
selected.
@param size the size of the JList to be monitored for selection.
*/
private void initialize(int size) {
	__selected = new boolean[size];
	for (int i = 0; i < size; i++) {
		__selected[i] = true;
	}
	super.setSelectionInterval(0, size - 1);
}

/**
Returns true if the given row is selected.  Overrides method in 
DefaultListSelectionModel.
@param row the row to check if it is selected.
@return true if the row is selected, false otherwise.
*/
public boolean isSelected(int row) {
	if (__selected[row] == true) {
		return true;
	}
	return false;
}

/**
Returns true if the list has no rows selected.  
Overrides method in DefaultListSelectionModel.
@return true if the list has no rows selected, false otherwise.
*/
public boolean isSelectionEmpty() {
	for (int i = 0; i < __selected.length; i++) {
		if (__selected[i]) {
			return false;
		}
	}
	return true;
}

/**
Removes the row from the list of selected rows and shifts all the higher rows'
selection values down in the array.
@param index the location of the new item.
*/
public void remove(int index) {
	int length = __selected.length;
	boolean[] temp = new boolean[length];
	for (int i = 0; i < length; i++) {
		temp[i] = __selected[i];
	}
	__selected = new boolean[length - 1];
	for (int i = 0; i < index; i++) {
		__selected[i] = temp[i];
	}
	for (int i = (index + 1); i < length; i++) {
		__selected[i] = temp[i];
	}
}

/**
Removes an interval of values from the selected list.
Overrides method in DefaultListSelectionModel; 
@param row0 the first row to be selected.
@param row1 the last row to be selected.
*/
public void removeSelectionInterval(int row0, int row1) {
	if (__selected[row0] == true) {
		__selected[row0] = false;
		super.removeSelectionInterval(row0, row0);
	}
	else {
		__selected[row0] = true;
		super.addSelectionInterval(row0, row0);
	}
}

/**
Selects all the rows in the list.
*/
public void selectAll() {
	int length = __selected.length;
	for (int i = 0; i < length; i++) {
		__selected[i] = true;
		super.addSelectionInterval(i, i);
	}	
}

/**
Sets a selection of rows as selected.
Overrides method in DefaultListSelectionModel; 
@param row0 the first row to be selected.
@param row1 the last row to be selected.
*/
public void setSelectionInterval(int row0, int row1) {
	if (__selected[row0] == true) {
		__selected[row0] = false;
		super.removeSelectionInterval(row0, row0);
	}
	else {
		__selected[row0] = true;
		super.addSelectionInterval(row0, row0);
	}
}

/**
Updates the list selection model to monitor a JList of a different size.
@param size the size of the JList to be monitored for selection.
*/
public void update(int size) {
	initialize(size);
}

}
