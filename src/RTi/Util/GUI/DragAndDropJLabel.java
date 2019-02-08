// DragAndDropJLabel - JLabel from which data can be dragged and onto which data can be dropped.

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

// ----------------------------------------------------------------------------
// DragAndDropJLabel - Class that implements a JLabel from which data can be
// 	dragged and onto which data can be dropped.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-03-01	J. Thomas Sapienza, RTi	Initial version.
// 2004-04-27	JTS, RTi		Revised after SAM's review.
// ----------------------------------------------------------------------------

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

import javax.swing.JLabel;

/**
This class implements a JLabel that supports dragging String data from and 
dropping String data onto.
*/
@SuppressWarnings("serial")
public class DragAndDropJLabel
extends JLabel
implements DragGestureListener, DragSourceListener, DropTargetListener, 
DragAndDrop {

/**
Reference to the DragAndDropControl that controls how drag and drop is performed
on this object.
*/
private DragAndDropControl __data = null;

/**
Constructor.  Creates a JLabel with the specified dragging and dropping actions.
@param text the text to appear on the JLabel.
@param dragAction the action to take upon dragging.  If 
DragAndDropUtil.ACTION_NONE, dragging is not supported.
@param dropAction the action to take upon dropping.  If
DragAndDropUtil.ACTION_NONE, dropping is not supported.
*/
public DragAndDropJLabel(String text, int dragAction, int dropAction) {
	super(text);
	initialize(dragAction, dropAction);
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
Initializes the DragAndDropControl according to the specified actions.
@param dragAction the action to take upon dragging.  If 
DragAndDropUtil.ACTION_NONE, dragging is not supported.
@param dropAction the action to take upon dropping.  If
DragAndDropUtil.ACTION_NONE, dropping is not supported.
*/
private void initialize(int dragAction, int dropAction) {
	__data = new DragAndDropControl(dragAction, dropAction);
	if (__data.allowsDrag()) {
		__data.setDragSource(
			DragAndDropUtil.createDragSource(this, dragAction, 
			this));
	}
	if (__data.allowsDrop()) {
		__data.setDropTarget(
			DragAndDropUtil.createDropTarget(this, dropAction, 
			this));
	}
}

/**
Returns the data flavors that this object can use to transfer data.
@return the data flavors that this object can use to transfer data.
*/
public DataFlavor[] getDataFlavors() {
	return DragAndDropTransferPrimitive.getTransferDataFlavors(
		DragAndDropTransferPrimitive.TYPE_STRING);
}

/**
Returns the data that controls how drag and drop are performed.
@return the data that controls how drag and drop are performed.
*/
public DragAndDropControl getDragAndDropControl() {
	return __data;
}

/**
Handles data that were dropped on this object.  If the data were of a 
supported  type, the text of the label is changed to the dropped text.
@param o the data dropped on the label.
@param p the Point at which data was dropped.
*/
public boolean handleDropData(Object o, Point p) {
	if (o == null) {
		return false;
	}
	if (o instanceof DragAndDropTransferPrimitive) {
		setText("" + ((String)((DragAndDropTransferPrimitive)o)
			.getData()));
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
Returns the transferable object that can be moved around in a drag operation.
@return the transferable object that can be moved around in a drag operation.
*/
public Transferable getTransferable() {
	return new DragAndDropTransferPrimitive(getText());
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
public void dragSuccessful(int action) {}

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
Does nothing (DragAndDrop interface method).
*/
public void setAlternateTransferable(Transferable t) {}

///////////////////////////////////////////////////////////////////////
// DragGesture method
///////////////////////////////////////////////////////////////////////

/**
Calls DragAndDropUtil.dragStart (DragGesture event).
*/
public void dragGestureRecognized(DragGestureEvent dge) {
	DragAndDropUtil.dragStart(this, this, dge);
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
