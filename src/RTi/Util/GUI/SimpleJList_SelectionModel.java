// SimpleJList_SelectionModel - selection model for selecting items in a mutable JList

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
Selection model for selecting items in a SimpleJList.  Offers a lot more 
customization than is availble using only the normal selection model, including
the ability to programmatically deselect cells easily.  It also contains support
for detecting drag actions, for use with DragAndDropMutableJLists.<p>
When using this with a class that extends JList, the class that extends JList
must implement the ListSelectionListener interface and must have the 
following code in its initialization section:<p>
<pre>
	setSelectionModel(new SimpleJList_SelectionModel(getItemCount()));
	getSelectionModel().addListSelectionListener(this);
	addMouseListener((SimpleJList_SelectionModel)getSelectionModel());
</pre>
<p>
If the extending list also needs to be drag-and-droppable, the following lines
should also be with the above 3:<p>
<pre>
	((SimpleJList_SelectionModel)getSelectionModel())
		.setSupportsDragAndDrop(true);
</pre>
<p>
The class's valueChanged() method, from the ListSelectionListener, only needs
to repaint the class in response to list changes.  The following code will
work:<p>
<pre>
public void valueChanged(ListSelectionEvent e) {
	repaint();
}
</pre>
*/
@SuppressWarnings("serial")
public class SimpleJList_SelectionModel
extends DefaultListSelectionModel 
implements MouseListener {

/**
An array that keeps track of the cells that are selected.
*/
private boolean[] __cellsSelected = null;

/**
Whether a potential drag was started on the row selection model.  
This happens when a cell is clicked on, and then clicked on again while it
was already selected.
*/
private boolean __possibleDrag = false;

/**
Whether this selection model supports drag-and-drop functionality.  If set
to false for jlists that aren't drag-and-droppable, may slightly improve
performance.
*/
private boolean __supportDnD = false;

/**
A list of the rows that were selected during a previous mouse press.  Used 
in determining whether a drag has started.
*/
private int[] __wasSelected = null;

/**
The anchro index.
*/
private int __anchor = 0;

/**
The number of the row selected in the mouse action after rows were initally
selected.  Used in determining when a drag has started.
*/
private int __lastRow = -1;

/**
The lead index.
*/
private int __lead = 0;

/**
The previous lead index.
*/
private int __oldLead = -1;

/**
Constructor.
@param rows the number of rows in the list.
*/
public SimpleJList_SelectionModel(int rows) {
	__cellsSelected = new boolean[rows];
	zeroArray(__cellsSelected);
}

/**
Used when a new item is added to the list to preserve the old selections.
@param index the location of the new item.
*/
public void add(int index) {
	int length = __cellsSelected.length;
	boolean[] temp = new boolean[length];
	for (int i = 0; i < length; i++) {
		temp[i] = __cellsSelected[i];
	}
	__cellsSelected = new boolean[length + 1];
	if (index == (length + 1)) {
		__cellsSelected[index - 1] = false;
		return;
	}
	int i = 0;
	for (i = 0; i < index; i++) {
		__cellsSelected[i] = temp[i];
	}
	__cellsSelected[i] = false;
	for (i = (index + 1); i < length; i++) {
		__cellsSelected[i] = temp[i];
	}
	notifyAllListeners(0, __cellsSelected.length);
}		

/**
Adds a selection interval to the list of selected intervals.
Overrides method in DefaultListSelectionModel.  
@param row0 the first row of the selection interval
@param row1 the last row of the selection interval.
*/
public void addSelectionInterval(int row0, int row1) {
	__anchor = row0;
	__lead = row1;
	int low = (row0 < row1 ? row0 : row1);
	int high = (row0 > row1 ? row0 : row1);

	if (getSelectionMode() != MULTIPLE_INTERVAL_SELECTION) {
		setSelectionInterval(row0, row1);
		return;
	}
	
	for (int i = low; i <= high; i++) {
		__cellsSelected[i] = true;
	}
	notifyAllListeners(low, high);
}

/**
Clears all selected rows.
Overrides method in DefaultListSelectionModel.
*/
public void clearSelection() {
	zeroArray(__cellsSelected);
	notifyAllListeners(0, __cellsSelected.length);
}

/**
Deselects a row.
@param row the row to deselect.
*/
public void deselectRow(int row) {	
	__cellsSelected[row] = false;
	notifyAllListeners(row, row);
}

/**
Returns whether a drag (for drag and drop) was started on a cell in the 
selection model.
@return whether a drag was started.
*/
public boolean dragWasTriggered() {
	return __possibleDrag;
}

/**
Returns the anchor selection index.
Overrides method in DefaultListSelectionModel.
@return the anchor.
*/
public int getAnchorSelectionIndex() {
	return __anchor;
}

/**
Returns the lead selection index.
Overrides method in DefaultListSelectionModel.
@return the lead.
*/
public int getLeadSelectionIndex() {
	return __lead;
}

/**
Returns the max selected index.
@return the max selected index.
Overrides method in DefaultListSelectionModel.
*/
public int getMaxSelectionIndex() {
	for (int i = (__cellsSelected.length - 1); i >= 0; i--) {
		if (__cellsSelected[i] == true) {
			return i;
		}
	}
	return -1;
}

/**
Returns the min selected index.
@return the min selected index.
Overrides method in DefaultListSelectionModel.
*/
public int getMinSelectionIndex() {
	for (int i = 0; i < __cellsSelected.length; i++) {
		if (__cellsSelected[i] == true) {
			return i;
		}
	}
	return -1;
}

/**
Initializes the __cellsSelected array.
@param size the size of the JList to be monitored for selection.
*/
private void initialize(int size) {
	__cellsSelected = new boolean[size];
	for (int i = 0; i < size; i++) {
		__cellsSelected[i] = false;
	}
	notifyAllListeners(0, __cellsSelected.length);
}

/**
Insert 'length' indices beginning before/after index. If the value at index is 
itself selected and the selection mode is not SINGLE_SELECTION, set all of
the newly inserted items as selected. Otherwise leave them unselected. This
method is typically called to sync the selection model with a corresponding
change in the data model.
Overrides method in DefaultListSelectionModel.
@param index the index at which to insert new indices.
@param length the number of indices to insert.
@param before if true, insert the indices before the index.  If false, insert
them after.
*/
public void insertIndexInterval(int index, int length, boolean before) {
//Message.printStatus(1, "", "insertIndexInterval(" + index + ", " + length
//	+ ", " + before + ")");
	boolean[] temp = new boolean[__cellsSelected.length + length];

	boolean selected = false;
	
	// Get the selection value of the index at which the new indices 
	// will be added.  The new indices will have the same selection value
	// (e.g., selected or not selected).
	if (index >= 0 && index < __cellsSelected.length) {
		selected = __cellsSelected[index];
	}

	int count = 0;

	if (before) {
		for (int i = 0; i < index; i++) {	
			temp[i] = __cellsSelected[i];
			count++;
		}
		for (int i = 0; i < length; i++) {	
			temp[count] = selected;
			count++;
		}
		for (int i = index; i < __cellsSelected.length; i++) {
			temp[count] = __cellsSelected[i];
			count++;
		}
	}
	else {
		for (int i = 0; i <= index; i++) {
			temp[i] = __cellsSelected[i];
			count++;
		}
		for (int i = 0; i < length; i++) {	
			temp[count] = selected;
			count++;
		}		
		for (int i = index + 1; i < __cellsSelected.length; i++) {
			temp[count] = __cellsSelected[i];
			count++;
		}
	}

	// Set the selection array to the one created in this method.
	__cellsSelected = temp;
	notifyAllListeners(0, __cellsSelected.length);
}

/**
Overrides method in DefaultListSelectionModel.  Returns whether the given 
row is selected or not.  Always returns true.
@return true.
*/
public boolean isSelectedIndex(int row) {
	if (row < 0 || row >= __cellsSelected.length) {
		return false;
	}
	return __cellsSelected[row];
}

/**
Responds to mouse clicks; does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Responds to mouse enters, does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Responds to mouse exits; does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Responds to mouse presses; does nothing.
*/
public void mousePressed(MouseEvent event) {}

/**
Responds to mouse button releases.  Generates the __wasSelected array that
contains all the rows selected at mouse release time.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (!__supportDnD) {
		return;
	}

	int count = 0;
	for (int i = 0; i < __cellsSelected.length; i++) {
		if (__cellsSelected[i]) {
			count++;
		}
	}
	__wasSelected = new int[count];
	count = 0;
	for (int i = 0; i < __cellsSelected.length; i++) {
		if (__cellsSelected[i]) {
			__wasSelected[count] = i;
			count++;
		}
	}
	__lastRow = -1;
}

/**
Notifies all listeners that something has changed in the selection model.
@param startRow the first row at which a change has occurred.
@param endRow the last row at which a change has occured.
*/
private void notifyAllListeners(int startRow, int endRow) {
	ListSelectionListener[] listeners = getListSelectionListeners();
	ListSelectionEvent e = null;
	for (int i = 0; i < listeners.length; i++) {
		if (e == null) {
			e = new ListSelectionEvent(this, 0, 10000, true);
		}
		((ListSelectionListener)listeners[i]).valueChanged(e);
	}	
}

/**
Removes the row from the list of selected rows and shifts all the higher rows'
selection values down in the array.
@param index the location of the new item.
*/
public void remove(int index) {
	int length = __cellsSelected.length;
	boolean[] temp = new boolean[length];
	for (int i = 0; i < length; i++) {
		temp[i] = __cellsSelected[i];
	}
	__cellsSelected = new boolean[length - 1];
	for (int i = 0; i < index; i++) {
		__cellsSelected[i] = temp[i];
	}
	for (int i = (index + 1); i < length; i++) {
		__cellsSelected[i - 1] = temp[i];
	}
	notifyAllListeners(0, __cellsSelected.length);
}

/**
Overrides method in DefaultListSelectionModel.  Removes an row interval from 
those already selected.  Does nothing.
@param row0 the first row.
@param row1 the last row.
*/
public void removeIndexInterval(int row0, int row1) {
//Message.printStatus(1, "", "removeIndexInterval(" + row0 + ", " + row1 + ")");
	int low = (row0 < row1 ? row0 : row1);
	int high = (row0 > row1 ? row0 : row1);

	int length = high - low;
	
	boolean[] temp = new boolean[__cellsSelected.length - length];

	int count = 0;
	for (int i = 0; i < row0; i++) {	
		temp[count] = __cellsSelected[i];
		count++;
	}

	for (int i = (row1 + 1); i < __cellsSelected.length; i++) {
		temp[count] = __cellsSelected[i];
		count++;
	}


	__cellsSelected = temp;
	
	notifyAllListeners(0, __cellsSelected.length);
}

/**
Overrides method in DefaultListSelectionModel.  Removes a selection 
interval from those already selected.  
@param row0 the first row to remove
@param row1 the last row to remove.
*/
public void removeSelectionInterval(int row0, int row1) {
//Message.printStatus(1, "", "removeSelectionInterval(" + row0+", "+row1 + ")");
	if (row0 == -1 || row1 == -1) {
		return;
	}

	__anchor = row0;
	__lead = row1;
	int low = (row0 < row1 ? row0 : row1);
	int high = (row0 > row1 ? row0 : row1);

	if (getSelectionMode() != MULTIPLE_INTERVAL_SELECTION
		&& low > getMinSelectionIndex() 
		&& high < getMaxSelectionIndex()) {
		high = __cellsSelected.length;
	}

	for (int i = low; i <= high; i++) {
		if (__cellsSelected[i] == false) {
			__cellsSelected[i] = true;
		}
		else {
			__cellsSelected[i] = false;
		}
	}
	notifyAllListeners(low, high);
}	

/**
Selects all the rows in the list.
*/
public void selectAll() {
	int length = __cellsSelected.length;
	for (int i = 0; i < length; i++) {
		__cellsSelected[i] = true;
		super.addSelectionInterval(i, i);
	}	
}

/**
Overrides method in DefaultListSelectionModel.  Sets the anchor's selection row.
@param anchorIndex the row of the anchor position.
*/
public void setAnchorSelectionIndex(int anchorIndex) {
//Message.printStatus(1, "", "setAnchorSelectionIndex(" + anchorIndex + ")");
	__anchor = anchorIndex;
//	notifyAllListeners(0, __cellsSelected.length);
}

/**
Sets whether the selection model is being used with a drag-and-drop list.  
If not, setting this to false may slightly improve performance.
@param dnd whether the selection model is being used with a drag-and-drop list.
*/
public void setSupportsDragAndDrop(boolean dnd) {
	__supportDnD = dnd;
}

/**
Overrides method in DefaultListSelectionModel.  Sets the lead selection row.  
@param leadIndex the lead row.
*/
public void setLeadSelectionIndex(int leadIndex) {
//Message.printStatus(1, "", "setLeadSelectionIndex(" + leadIndex + ")");
	if (__anchor == -1 || leadIndex == -1) {
		return;
	}

	boolean selected = __cellsSelected[__anchor];

	__lead = leadIndex;
	int low = (__oldLead < __anchor ? __oldLead : __anchor);
	int high = (__oldLead > __anchor ? __oldLead : __anchor);	

	if (__oldLead > -1) {
		for (int i = low; i < high; i++) {
			if (selected) {
				__cellsSelected[i] = false;
			}
			else {
				__cellsSelected[i] = true;
			}
		}
	}

	int low2 = (__lead < __anchor ? __lead : __anchor);
	int high2 = (__lead > __anchor ? __lead : __anchor);		

	for (int i = low2; i < high2; i++) {
		if (selected) {
			__cellsSelected[i] = true;
		}
		else {
			__cellsSelected[i] = false;
		}
	}

	int minLow = (low < low2 ? low : low2);
	int maxHigh = (high > high2 ? high : high2);

	__oldLead = __lead;
	notifyAllListeners(minLow, maxHigh);
}

/**
Overrides method in DefaultListSelectionModel.  Sets the selection interval.
@param row0 the first selection interval.
@param row1 the last selection interval.
*/
public void setSelectionInterval(int row0, int row1) {
//Message.printStatus(1, "", "setSelectionInterval(" + row0 + ", "+ row1 + ")");
	if (row0 == -1 || row1 == -1) {
		return;
	}

	if (getSelectionMode() == SINGLE_SELECTION) {
//		Message.printStatus(1, "", "   (SS)");
		row0 = row1;
	}

	__anchor = row0;
	__lead = row1;

	int low = 0;
	int high = 0;
	low = (row0 < row1 ? row0 : row1);
	high = (row0 > row1 ? row0 : row1);
	zeroArray(__cellsSelected);

	// the next section determines when a drag occurs.
	// The array __wasSelected, which is consulted by wasSelected(), is
	// generated when a mouse button is released.  When this occurs, 
	// rows have been selected or deselected.

	// The order of calls that happen when a row is selected and processed
	// for drag is this:
	// 1) the user selects rows with the mouse button, and while that 
	//	happens, setSelectionInterval is called to add each row.
	// 2) the user releases the mouse button after selecting a row and
	//	the __wasSelected array is generated.  It contains the numbers
	//	of all the rows that are selected.
	// 3) the user clicks a row again and setSelectionInterval is called.
	// 	__lastRow is currently -1.  If the row that was clicked on is
	//	in the __wasSelected array, the row number is stored in 
	//	__lastRow.
	// 4) the -- continuing to hold the mouse button down on the same row 
	//	-- drags the mouse.  Another call is made to 
	//	setSelectionInterval.  This time, __lastRow is NOT -1.  If the
	//	user is dragging in the same row, __possibleDrag is set to
	//	true and the list will begin dragging.

	// this convoluted method is necessary because List events are 
	// dispatched before MouseMotionEvents, so it can't simply wait
	// for a CLICK/CLICK-DRAG event pattern to detect when a drag
	// occurs.

	if (__supportDnD) {
		if (__lastRow == -1) {
			if (wasSelected(low)) {
				__lastRow = low;
				__possibleDrag = false;
			}
			else {
				__lastRow = -1;
				__possibleDrag = false;
			}
		}
		else {
			if (__lastRow == low) {
				__possibleDrag = true;
			}
			else {
				__possibleDrag = false;
			}
		}
	
		__wasSelected = null;
	}

	for (int i = low; i <= high; i++) {
		__cellsSelected[i] = true;
	}
	
	notifyAllListeners(low, high);
}

/**
Updates the list selection model to monitor a JList of a different size.
@param size the size of the JList to be monitored for selection.
*/
public void update(int size) {
	initialize(size);
}

/**
Tells whether a row had been selected during a previous mouse action.
@param row the row to check.
@return true if it had been selected, false if it had not.
*/
private boolean wasSelected(int row) {
	if (__wasSelected == null) {
		return false;
	}
	for (int i = 0; i < __wasSelected.length; i++) {
		if (__wasSelected[i] == row) {
			return true;
		}
	}
	return false;
}

/**
Sets a boolean array to all false.
@param array the array to "zero" out.
*/
private void zeroArray(boolean[] array) {
	for (int i = 0; i < array.length; i++) {
		array[i] = false;
	}
}

}
