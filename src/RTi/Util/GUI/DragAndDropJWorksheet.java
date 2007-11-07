// ----------------------------------------------------------------------------
// DragAndDropJWorksheet - Class that implements a JWorksheet from which data 
//	can be dragged and onto which data can be dropped.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-03-01	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
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

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class implements a JWorksheet that supports dragging data out of and 
dropping data into.
*/
public class DragAndDropJWorksheet
extends JWorksheet
implements DragGestureListener, DragSourceListener, DropTargetListener, 
DragAndDrop {

/**
The DragAndDropControl object that holds information about how this object 
works with DragAndDrop.
*/
private DragAndDropControl __data = null;

/**
Constructor.  Creates a JWorksheet with the specified cell renderer and table 
model.  For information about the support properties that can be passed in 
in the proplist, see the JWorksheet javadocs.
@param cellRenderer the cell renderer to use.
@param tableModel the table model to use.
@param props the properties that define JWorksheet behavior.
*/
public DragAndDropJWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props) {
	super(cellRenderer, tableModel, props);;
	initialize(DragAndDropUtil.ACTION_COPY, DragAndDropUtil.ACTION_NONE);
}

/**
Constructor.  Creates a JWorksheet with the specified number of rows and 
columns. For information about the support properties that can be passed in 
in the proplist, see the JWorksheet javadocs.
@param rows the number of rows in the empty worksheet.
@param cols the number of columns in the empty worksheet.
@param props the properties that define JWorksheet behavior.
*/
public DragAndDropJWorksheet(int rows, int cols, PropList props) {
	super(rows, cols, props);
	initialize(DragAndDropUtil.ACTION_COPY, DragAndDropUtil.ACTION_NONE);
}

/** 
Returns the data flavors in which the worksheet can transfer data.
@return the data flavors in which the worksheet can transfer data.
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
Returns the data structure with info about the worksheet's DragAndDrop rules.
@return the data structure with info about the worksheet's DragAndDrop rules.
*/
public DragAndDropControl getDragAndDropControl() {
	return __data;
}

/**
Returns the transferable object that can be dragged from this worksheet.
@return the transferable object that can be dragged from this worksheet.
*/
public Transferable getTransferable() {
	if (IOUtil.testing()) {
		Message.printStatus(1, "", "getTransferable: "
			+ __data.getAlternateTransferable());
	}
	if (__data.getAlternateTransferable() != null) {
		return __data.getAlternateTransferable();
	}
	else {
		return new DragAndDropTransferPrimitive(
			"No alternate transferable set");
	}
}

/**
Handles data that has been dropped on this worksheet.
@param o the data that has been dropped.
*/
public boolean handleDropData(Object o, Point p) {
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

	// only allow dragging if the worksheet is not editable
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
	int col = columnAtPoint(p);
	int row = rowAtPoint(p);

	if (isCellSelected(row, col)) {
		if (((JWorksheet_RowSelectionModel)getSelectionModel())
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

}
