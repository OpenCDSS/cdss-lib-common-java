// DragAndDropSimpleJComboBox - a SimpleJComboBox that supports drag and drop capability

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

import java.awt.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.util.List;

import javax.accessibility.Accessible;

import javax.swing.JList;

import javax.swing.plaf.basic.ComboPopup;

import javax.swing.text.JTextComponent;

/**
This class implements a SimpleJComboBox that supports dragging and dropping
text.  Currently only supports drags and drops of DragAndDropTransferPrimitive data.  
*/
@SuppressWarnings("serial")
public class DragAndDropSimpleJComboBox extends SimpleJComboBox
implements DragGestureListener, DragSourceListener, DropTargetListener, 
DragAndDrop {

/**
The DragAndDropControl object that holds information about how this object works with DragAndDrop.
*/
private DragAndDropControl __data = null;

/**
The item from the combo box that was dragged.
*/
private int __selectedIndex = -1;

/**
The text of the item that was last-selected from the list that appears when the
combo box is clicked on.  This is so that items from the middle of the list can be dragged.
*/
private String __lastSelectedItem = null;

/**
Creates a SimpleJComboBox that supports drag and drop.
@param editable whether the text in this object is editable.  SimpleJComboBoxes
with editable text cannot support drag operations.
@param dragAction the action to take when dragging data.  
See DragAndDropUtil.ACTION_*.  Drags can only be performed if the combo box is NON editable.
@param dropAction the action to take when dropping data.  
See DragAndDropUtil.ACTION_*.  Drops can only be performed if the combo box is editable.
*/
public DragAndDropSimpleJComboBox(boolean editable, int dragAction, int dropAction) {
	super(editable);
	initialize(dragAction, dropAction);
}

/**
Creates a SimpleJComboBox that supports drag and drop.
@param v a List of values to initialize the combo box with.
@param dragAction the action to take when dragging data.  
See DragAndDropUtil.ACTION_*.  Drags can only be performed if the combo box is NON editable.
@param dropAction the action to take when dropping data.  
See DragAndDropUtil.ACTION_*.  Drops can only be performed if the combo box is editable.
*/
public DragAndDropSimpleJComboBox(List v, int dragAction, int dropAction) {
	super(v);
	initialize(dragAction, dropAction);
}

/**
Creates a SimpleJComboBox that supports drag and drop.
@param v a Vector of values to initialize the combo box with.
@param editable whether the text in this object is editable.  SimpleJComboBoxes
with editable text cannot support drag operations.
@param dragAction the action to take when dragging data.  
See DragAndDropUtil.ACTION_*.  Drags can only be performed if the combo box is NON editable.
@param dropAction the action to take when dropping data.  
See DragAndDropUtil.ACTION_*.  Drops can only be performed if the combo box is editable.
*/
public DragAndDropSimpleJComboBox(List v, boolean editable, int dragAction, int dropAction) {
	super(v, editable);
	initialize(dragAction, dropAction);
}

/**
Creates a SimpleJComboBox that supports drag and drop.
@param size the default width of the drop down combo box area.
@param editable whether the text in this object is editable.  SimpleJComboBoxes
with editable text cannot support drag operations.
@param dragAction the action to take when dragging data.  
See DragAndDropUtil.ACTION_*.  Drags can only be performed if the combo box is NON editable.
@param dropAction the action to take when dropping data.  
See DragAndDropUtil.ACTION_*.  Drops can only be performed if the combo box is editable.
*/
public DragAndDropSimpleJComboBox(int size, boolean editable, int dragAction, 
int dropAction) {
	super(size, editable);
	initialize(dragAction, dropAction);
}

/**
Creates a SimpleJComboBox that supports drag and drop.
@param v a Vector of values to initialize the combo box with.
@param size the default width of the drop down combo box area.
@param editable whether the text in this object is editable.  SimpleJComboBoxes
with editable text cannot support drag operations.
@param dragAction the action to take when dragging data.  
See DragAndDropUtil.ACTION_*.  Drags can only be performed if the combo box is NON editable.
@param dropAction the action to take when dropping data.  
See DragAndDropUtil.ACTION_*.  Drops can only be performed if the combo box is editable.
*/
public DragAndDropSimpleJComboBox(List v, int size, boolean editable, int dragAction, int dropAction) {
	super(v, size, editable);
	initialize(dragAction, dropAction);
}

/** 
Returns the data flavors in which the combo box can transfer data.
@return the data flavors in which the combo box can transfer data.
*/
public DataFlavor[] getDataFlavors() {
	return DragAndDropTransferPrimitive.getTransferDataFlavors(
		DragAndDropTransferPrimitive.TYPE_STRING);
}

/**
Returns the data structure with info about the combo box's DragAndDrop rules.
@return the data structure with info about the combo box's DragAndDrop rules.
*/
public DragAndDropControl getDragAndDropControl() {
	return __data;
}

/**
Returns the transferable object that can be dragged from this combo box.
@return the transferable object that can be dragged from this combo box.
*/
public Transferable getTransferable() {
	if (__data.getAlternateTransferable() != null) {
		return __data.getAlternateTransferable();
	}
	else {
		return new DragAndDropTransferPrimitive(__lastSelectedItem);
	}
}

/**
Handles data that has been dropped on this combo box.
@param o the data that has been dropped.
*/
public boolean handleDropData(Object o, Point p) {
	if (o == null) {
		return false;
	}
	if (o instanceof DragAndDropTransferPrimitive) {
		setText("" + ((String)((DragAndDropTransferPrimitive)o).getData()));
	}
	else {
		// this class is set up (see getDataFlavors()) to recognize
		// STRING type transfer data flavors as defined in the 
		// Primitive.  All Primitives support transferring their data
		// in its default format (Boolean, Integer, String, etc.) but
		// also support a final default 'text' flavor, which supports
		// transferring most other kinds of data from other
		// applications.  Nearly everything can at least transfer data
		// as text.  This catches that.
		setText(o.toString());
	}
	return true;
}

/**
Initializes the drag and drop aspects of this class.
@param dragAction the action to take on dragging.
@param dropAction the action to take on dropping.
*/
private void initialize(int dragAction, int dropAction) {
	// Create the data object (for use by DragAndDropUtil).
	__data = new DragAndDropControl(dragAction, dropAction);

	Accessible a = getUI().getAccessibleChild(this, 0);
	JList list = null;
	if (a != null && a instanceof ComboPopup) {
		// Get the popup list.
		list = ((ComboPopup)a).getList();
	}	
		
	setDragAction(dragAction);

	// only allow dragging if the combo box is not editable
	if (__data.allowsDrag() && !isEditable()
		&& list != null) {
		__data.setDragSource( DragAndDropUtil.createDragSource( list, dragAction, this));
	}
	// Only allow drops if the combobox is editable.
	if (__data.allowsDrop() && isEditable()) {
		// note:
		// it was essential that the drop target be the editor for the combo box and not the combo box itself.
		// Otherwise, there was some weird behavior.
		if (isEditable()) {
			__data.setDropTarget( DragAndDropUtil.createDropTarget(
				(JTextComponent)getEditor()
				.getEditorComponent(), 
				dropAction, this));			
		}
	}
	else {
		// turn off the default drag and drop support in the text component.
		__data.setDropAction(DragAndDropUtil.ACTION_NONE);
		((JTextComponent)getEditor().getEditorComponent()).setTransferHandler(null);
	}

	DragAndDropDefaultListCellRenderer lcr = new DragAndDropDefaultListCellRenderer(this);
	setRenderer(lcr);
}

/**
Sets whether dragging is enabled, and makes sure it is not enabled if the combo box is editable.
@param action the action to take on dragging
*/
public void setDragAction(int action) {
	if (isEditable()) {
		__data.setDragAction(DragAndDropUtil.ACTION_NONE);
	}
	else {
		__data.setDragAction(action);
	}
}

/**
Sets the item that was last-selected from the list that appears when the 
combo box is clicked on.  This is so that items in the middle of the list can be dragged.
@param lastSelectedItem the text of the last-selected item.
*/
protected void setLastSelectedItem(String lastSelectedItem) {
	__lastSelectedItem = lastSelectedItem;
}

///////////////////////////////////////////////////////////////////////
// DragAndDrop interface methods
///////////////////////////////////////////////////////////////////////

/**
Does nothing (DragAndDrop interface method).
*/
public void dragStarted() {}

/**
Called when a drag has been performed successfully.  Checks to see if the 
action performed was a move, and if so, removes the item that was dragged
from the combo box.
@param action the action performed by the drop component when the drop occurred.
*/
public void dragSuccessful(int action) {
	int result = DragAndDropUtil.determineAction(
		__data.getDragAction(), action);
	if (result == DragAndDropUtil.ACTION_MOVE && __selectedIndex > -1) {
		removeAt(__selectedIndex);
	}
	__selectedIndex = -1;
}	

/**
Does nothing (DragAndDrop interface method).
*/
public void dragUnsuccessful(int action) {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dropExited() {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dropAllowed() {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dropNotAllowed() {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dropSuccessful() {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dropUnsuccessful() {}

/**
Sets the alternate transferable, in case data other than the label in the
combo box should be transferred upon a drag.
@param t the alternate Transferable to use.
*/
public void setAlternateTransferable(Transferable t) {
	__data.setAlternateTransferable(t);
}


////////////////////////////////////////////////////////////////
// Drag Gesture events
/**
Calls DragAndDropUtil.dragStart (DragGesture event) and also keeps track of
the item that was selected in case it has to be removed from the combo box
upon a successful drag with a move action.
*/
public void dragGestureRecognized(DragGestureEvent dge) {
	DragAndDropUtil.dragStart(this, this, dge);
	__selectedIndex = getSelectedIndex();
}

///////////////////////////////////////////////////////////////////////
// Drag methods
///////////////////////////////////////////////////////////////////////

/**
Calls DragAndDropUtil.dragDropEnd (Drag event).
*/
public void dragDropEnd(DragSourceDropEvent dsde) {
	DragAndDropUtil.dragDropEnd(this, dsde);
}

/**
Calls DragAndDropUtil.dragEnter (Drag event).
*/
public void dragEnter(DragSourceDragEvent dsde) {
	DragAndDropUtil.dragEnter(this, dsde);
}

/**
Calls DragAndDropUtil.dragExit(Drag event).
*/
public void dragExit(DragSourceEvent dse) {
	DragAndDropUtil.dragExit(this, dse);
}

/**
Calls DragAndDropUtil.dragOver (Drag event).
*/
public void dragOver(DragSourceDragEvent dsde) {
	DragAndDropUtil.dragOver(this, dsde);
}

/**
Calls DragAndDropUtil.dropActionChanged (Drag event).
*/
public void dropActionChanged(DragSourceDragEvent dsde) {
	DragAndDropUtil.dropActionChanged(this, dsde);
}

/**
Calls DragAndDropUtil.dropActionChanged (Drop event).
*/
public void dropActionChanged(DropTargetDragEvent dtde) {
	DragAndDropUtil.dropActionChanged(this, dtde);
}

///////////////////////////////////////////////////////////////////////
// Drop methods
///////////////////////////////////////////////////////////////////////

/**
Calls DragAndDropUtil.dragEnter (Drop event).
*/
public void dragEnter(DropTargetDragEvent dtde) {
	DragAndDropUtil.dragEnter(this, dtde);
}

/**
Calls DragAndDropUtil.dragExit (Drop event).
*/
public void dragExit(DropTargetEvent dte) {
	DragAndDropUtil.dragExit(this, dte);
}

/**
Calls DragAndDropUtil.dragOver (Drop event).
*/
public void dragOver(DropTargetDragEvent dtde) {
	DragAndDropUtil.dragOver(this, dtde);
}

/**
Calls DragAndDropUtil.drop (Drop event).
*/
public void drop(DropTargetDropEvent dtde) {
	DragAndDropUtil.drop(this, dtde);
}

}
