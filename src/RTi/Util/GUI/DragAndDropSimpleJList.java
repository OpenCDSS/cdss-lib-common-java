// DragAndDropSimpleJList - class that supports drag and drop operations on a JList

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
// DragAndDropSimpleJList - class that supports drag and drop operations 
// 	on a JList.
//---------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//---------------------------------------------------------------------------
// History:
//
// 2003-05-06	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-08	JTS, RTi		Renamed from DragAndDropMutableJList.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//---------------------------------------------------------------------------

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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class DragAndDropSimpleJList
extends SimpleJList<String>
implements DragGestureListener, DragSourceListener, DropTargetListener, 
DragAndDrop, ListSelectionListener {

/**
The DragAndDropControl object that holds information about how this object 
works with DragAndDrop.
*/
private DragAndDropControl __data = null;

/**
Constructor.
@param dragAction the action to be taken when something is dragged from the list
@param dropAction the action to be taken when something is dropped on the list
*/
public DragAndDropSimpleJList(int dragAction, int dropAction) {
	super();
	initialize(dragAction, dropAction);
}	

/**
Constructor.
@param array an array of strings that will be used to populate the list.
@param dragAction the action to be taken when something is dragged from the list
@param dropAction the action to be taken when something is dropped on the list
*/
public DragAndDropSimpleJList(String array[], int dragAction, int dropAction) {
	super(array);
	initialize(dragAction, dropAction);
}

/**
Constructor.
@param list a list of strings that will be used to populate the list.
@param dragAction the action to be taken when something is dragged from the list
@param dropAction the action to be taken when something is dropped on the list
*/
public DragAndDropSimpleJList(List<String> list, int dragAction, int dropAction) {
	super(list);
	initialize(dragAction, dropAction);
}

/** 
Returns the data flavors in which the combo box can transfer data.
@return the data flavors in which the combo box can transfer data.
*/
public DataFlavor[] getDataFlavors() {
	if (__data.getAlternateTransferable() != null) {
		return __data.getAlternateTransferable()
			.getTransferDataFlavors();
	}
	else {
		return DragAndDropTransferPrimitive.getTransferDataFlavors(
			DragAndDropTransferPrimitive.TYPE_STRING);
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__data = null;
	super.finalize();
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
		return new DragAndDropTransferPrimitive(
			(String)getSelectedItem());
	}
}

/**
Handles data that has been dropped on this combo box.
@param o the data that has been dropped.
*/
public boolean handleDropData(Object o, Point p) {
	// REVISIT (JTS - 2004-05-06)
	// come back and actually add support for dropping things on the list,
	// once we start doing that.  No time right now.
	if (o == null) {
		return false;
	}
	if (o instanceof DragAndDropTransferPrimitive) {
		
	}
	else {
		// should be String
	}
	return true;
}

/**
Initializes the drag and drop aspects of this class.
@param dragAction the action to take on dragging.
@param dropAction the action to take on dropping.
*/
private void initialize(int dragAction, int dropAction) {
	// create the data object (for use by DragAndDropUtil)
	__data = new DragAndDropControl(dragAction, dropAction);

	// only allow dragging if the combo box is not editable
	if (__data.allowsDrag()) {
		__data.setDragSource(
			DragAndDropUtil.createDragSource(
			this, dragAction, this));
	}
	if (__data.allowsDrop()) {
		__data.setDropTarget(
			DragAndDropUtil.createDropTarget(
			this, dropAction, this));			
	}
	setSelectionModel(new SimpleJList_SelectionModel(getItemCount()));
	((SimpleJList_SelectionModel)getSelectionModel())
		.setSupportsDragAndDrop(true);
	getSelectionModel().addListSelectionListener(this);
	addMouseListener((SimpleJList_SelectionModel)getSelectionModel());
}

///////////////////////////////////////////////////////////////////////
// DragAndDrop interface methods
///////////////////////////////////////////////////////////////////////

/**
Does nothing (DragAndDrop interface method).
*/
public void dragStarted() {}

/**
Does nothing (DragAndDrop interface method).
*/
public void dragSuccessful(int action) {
	int result = DragAndDropUtil.determineAction(
		__data.getDragAction(), action);
	if (result == DragAndDropUtil.ACTION_MOVE) {
		remove(getSelectedIndex());
	}
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
Sets the alternate transferable to use in the event of a drag.
@param t the alternate transferable to use.
*/
public void setAlternateTransferable(Transferable t) {
	__data.setAlternateTransferable(t);
}

/**
Recognizes a drag only if a click is made on a cell that is already selected
and a drag is started.  Otherwise, allow normal worksheet cell-selection via
dragging.
*/
public void dragGestureRecognized(DragGestureEvent dge) {
	Point p = dge.getDragOrigin();
	int row = locationToIndex(p);

	if (isSelectedIndex(row)) {
		if (((SimpleJList_SelectionModel)getSelectionModel())
			.dragWasTriggered()) {
			DragAndDropUtil.dragStart(this, this, dge);
		}
	}	
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

/**
Repaints the list in response to list changes. From ListSelectionListener.
@param event the ListSelectionEvent that happened.
*/
public void valueChanged(ListSelectionEvent event) {
	repaint();
}

}
