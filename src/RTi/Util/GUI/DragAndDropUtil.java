// DragAndDropUtil - utility methods for use by classes that implement drag and drop capability

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

//-----------------------------------------------------------------------------
// DragAndDropUtil - Utility methods for use by classes that implement 
//	drag and drop capability.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
// 2004-02-24	J. Thomas Sapienza, RTi	Initial version.
// 2004-03-04	JTS, RTi		Updated Javadocs in response to 
//					numerous changes.
// 2004-04-27	JTS, RTi		Revised after SAM's review.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

/**
Utility methods for use by classes that implement drag and drop capability.
For information how to implement drag and drop in a Swing JComponent, see the
documentation for the DragAndDrop interface.<p>
<b>Note:</b><br>
Because this class handles utility functions for both drag and drop listeners,
there can be some confusion in the method names.  Java's API uses the same
method names for both drag and drop listeners; only the parameters are named
differently.  For this reason, in the methods that might be confusing the 
Javadocs for each method are prefaced with the command that is occuring.  
For instance:<br>
<blockquote>
Drop: called when ....
</blockquote><p>
refers to a method that is used by drop listeners.
*/
public class DragAndDropUtil {

/**
Class name.
*/
private static final String __CLASS = "DragAndDropUtil";

////////////////////////////////////////
// These actions are chained from the actions in DnDConstants
// because DnDConstants provides some other actions (in particular, for 
// moving, linking and copying files) that JTS felt were unnecessary at
// the current time for RTi developers.  Putting these links to the other
// commands limits RTi developers to only using the following commands.

/**
Used for DragAndDrop components that don't respond to any DragAndDrop 
actions.  For instance, if a component allows dragging but not dropping, 
the drop action would be ACTION_NONE.  
<p>Numeric value: 0.
*/
public static final int ACTION_NONE = DnDConstants.ACTION_NONE;

/**
Used for DragAndDrop components that allow a copy of information.  
Information that is copied should remain in the drag component and be 
duplicated into the drop component.  
<p>Numeric value: 1.
*/
public static final int ACTION_COPY = DnDConstants.ACTION_COPY;

/**
Used for DragAndDrop components that move information from one to another.  
Information that is moved will be copied into the drop component and 
deleted from the drag component. 
<p>Numeric value: 2.
*/
public static final int ACTION_MOVE = DnDConstants.ACTION_MOVE;

/**
Used for DragAndDrop components that respond to either copy or move 
actions.  This is mostly for use when working with outside drag sources, 
such as from a Windows application, because it can't be certain whether 
the information was sent with
a COPY or a MOVE action.  Even then, they might not be properly used.<p>

For example, if text is dragged from Wordpad into a java application, the
Wordpad drag sends a MOVE action.  The text that was dragged from Wordpad
remainds in Wordpad, however, so it is actually a COPY action.<p>

ACTION_COPY_OR_MOVE is a logical OR of the ACTION_COPY and ACTION_COPY 
actions, provided for convenience.
<p>Numerical value: 3.
*/
public static final int ACTION_COPY_OR_MOVE = DnDConstants.ACTION_COPY_OR_MOVE;

/**
Registers a DragAndDropListener for an DragAndDrop object.
@param r the DragAndDrop object on which to register the listener. This can
be either a component from which can are dragged or a component on which
data are dropped.
@param d the listener to register. This is the component that will be 
informed of events occuring throughout dragging and dropping.
*/
public static void addDragAndDropListener(DragAndDrop r, 
DragAndDropListener d) {
	r.getDragAndDropControl().addDragAndDropListener(d);
}

/**
Creates a drag source for a component that can be dragged.  The drag source
handles recognizing and start drag events on a drop component.  The 
DragSource object will be stored in the component's DragAndDropData object.  
Most of the time, the parameters 'c' and 'dgl' will be the same object.
@param c the Component from which data can be dragged.  It must implement 
DragAndDrop.
@param action the action performed by the drag.  See DragAndDropUtil.ACTION_*.
@param dgl the DragGestureListener that will recognize when a drag begins.  
Must implement DragAndDrop.
@return a DragSource object for storage in a DragAndDropData object.
*/
public static DragSource createDragSource(Component c, int action,
DragGestureListener dgl) {
	DragSource dragSource = DragSource.getDefaultDragSource();
	dragSource.createDefaultDragGestureRecognizer(c,action, dgl);
	return dragSource;
}

/**
Creates a drop target for a component onto which data can be dragged.  The
drop target handles recognizing when a drop has occured on a drop compoenent, 
and is stored in the component's DragAndDropData object.
Most of the time, the parameters 'c' and 'dtl' will be the same object.
@param c the Component onto which data can be dragged.  It must implement 
DragAndDrop.
@param action the action that the drop recognizes.  See 
DragAndDropUtil.ACTION_*.
@param dtl the DropTargetListener that recognizes when a drop occurs.  
Must implement DragAndDrop.
@return a DropTarget object for storage in a DragAndDropData object.
*/
public static DropTarget createDropTarget(Component c, int action, 
DropTargetListener dtl) {
	DropTarget dropTarget = new DropTarget(c, action, dtl, true);
	return dropTarget;
}

/**
Determines the action that should be taken given a combination of actions
from a drag and a drop component.  When setting up a draggable or a droppable
component, an action is specified that tells Java's drag and drop how to 
behave.  This method takes an action from the component from which data was
dragged and the action from the component on which data is to be dropped and
sees if the actions can work together and perform a task. <p>
If either action is ACTION_NONE, then ACTION_NONE will be returned and no
drag and drop action can be performed.<p>
If one action is ACTION_COPY, then ACTION_COPY is returned if the other
action is ACTION_COPY or ACTION_COPY_OR_MOVE.  Otherwise, ACTION_NONE will
be returned.<p>
If one action is ACTION_MOVE, then ACTION_MOVE is returned if the other
action is ACTION_MOVE or ACTION_COPY_OR_MOVE.  Otherwise, ACTION_NONE will
be returned.<p>
If one action is ACTION_COPY_OR_MOVE, then ACTION_COPY will be returned if
the other action is ACTION_COPY.  ACTION_MOVE will be returned if the other
action is ACTION_MOVE. ACTION_COPY will be returned if the other action is
ACTION_COPY_OR_MOVE.
@param dragAction the action performed by the drag component.
@param dropAction the action recognized by the drop component.
@return the action that should be performed, given the actions of both 
components. See DragAndDrop's documentation for "Responding to a 
Successful Drag" for an explanation.
*/
public static int determineAction(int dragAction, int dropAction) {
	if (dragAction == ACTION_NONE || dropAction == ACTION_NONE) {
		return ACTION_NONE;
	}

	if (dragAction == ACTION_COPY) {
		if (dropAction == ACTION_COPY) {
			return ACTION_COPY;
		}
		else if (dropAction == ACTION_MOVE) {
			return ACTION_NONE;
		}
		else if (dropAction == ACTION_COPY_OR_MOVE) {
			return ACTION_COPY;
		}
	}
	else if (dragAction == ACTION_MOVE) {
		if (dropAction == ACTION_COPY) {
			return ACTION_NONE;
		}
		else if (dropAction == ACTION_MOVE) {
			return ACTION_MOVE;
		}
		else if (dropAction == ACTION_COPY_OR_MOVE) {
			return ACTION_MOVE;
		}	
	}
	else if (dragAction == ACTION_COPY_OR_MOVE) {
		if (dropAction == ACTION_COPY) {
			return ACTION_COPY;
		}
		else if (dropAction == ACTION_MOVE) {
			return ACTION_MOVE;
		}
		else if (dropAction == ACTION_COPY_OR_MOVE) {
			return ACTION_COPY;
		}	
	}
	return ACTION_NONE;
}

/**
Drag: called when a drag has been terminated because the mouse button was 
released.  It determines whether or not the drag was successful and calls
dragUnsuccessful() or dragSuccessful() on the DragAndDrop parameter as 
necessary.  In addition, all the DragAndDropListeners for the drag object 
are notified via dragSuccessful() or dragUnsuccessful().
@param d the DragAndDrop object that instantiated the drag.
@param dsde the DragSourceDropEvent created when the drag ended.
*/
public static void dragDropEnd(DragAndDrop d, DragSourceDropEvent dsde) {
	if ((!d.getDragAndDropControl().allowsDrag()) 
	    || (dsde.getDropSuccess() == false)) {
		d.dragUnsuccessful(dsde.getDropAction());
		d.getDragAndDropControl().notifyListenersDragUnsuccessful(
			dsde.getDropAction());
	}
	else {
		d.dragSuccessful(dsde.getDropAction());
		d.getDragAndDropControl().notifyListenersDragSuccessful(
			dsde.getDropAction());
	}
}

/**
Drag: called when a drag goes into a component.
Sets the mouse cursor over the component to represent whether anything can
be dropped in that component.
@param d the DragAndDrop object from which data was dragged.
@param dsde the DragSourceDragEvent created when the drag entered the 
droppable component.
*/
public static void dragEnter(DragAndDrop d, DragSourceDragEvent dsde) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		DragSourceContext context = dsde.getDragSourceContext();
		context.setCursor(DragSource.DefaultCopyNoDrop);
	}
	else {
		setDragOverFeedback(d, dsde);
	}
}

/**
Drop: called when a drag goes into a component that can be dropped on.
Checks to see if the component allows drags and drops and calls dropNotAllowed()
or dropAllowed() on the 'd' parameter as necessary.  In addition, the
drop component's DragAndDropListeners are notified via dropAllowed()
or dropNotAllowed().
@param d the DragAndDrop object that data is being dragged onto.
@param dtde the DropTargetDragEvent created when the mouse entered the 
component.
*/
public static void dragEnter(DragAndDrop d, DropTargetDragEvent dtde) {
	if ((!d.getDragAndDropControl().allowsDrop()) 
	    || (!DragAndDropUtil.isDragOK(d, dtde))) {
		d.dropNotAllowed();
		d.getDragAndDropControl().notifyListenersDropNotAllowed();
		dtde.rejectDrag();
	}
	else {
		d.dropAllowed();
		d.getDragAndDropControl().notifyListenersDropAllowed();
		dtde.acceptDrag(dtde.getDropAction());
	}
}

/**
Drag: called when a drag exits a component that can be dropped on.  Sets 
the cursor to indicate that no drop is allowed.
@param d the DragAndDrop object from which data was dragged.
@param dse the DragSourceEvent created when the component was exited.
*/
public static void dragExit(DragAndDrop d, DragSourceEvent dse) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		DragSourceContext context = dse.getDragSourceContext();
		context.setCursor(DragSource.DefaultCopyNoDrop);
		return;
	}

	DragSourceContext context = dse.getDragSourceContext();
	int dragAction = d.getDragAndDropControl().getDragAction();

	if (dragAction == ACTION_COPY 
	    || dragAction == ACTION_COPY_OR_MOVE) {
		context.setCursor(DragSource.DefaultCopyNoDrop);
	}
	else if (dragAction == ACTION_MOVE) {
		context.setCursor(DragSource.DefaultMoveNoDrop);
	}
	else {
		context.setCursor(DragSource.DefaultCopyNoDrop);
	}		
	context.setCursor(null);
}

/**
Drop: called when a drag exits a component that can be dropped on.  Calls
dropExited() on the 'd' parameter, if the 'd' object allows drops.  In 
addition, the drop component's DragAndDropListeners are notified via
dropExited().
@param d the DragAndDrop object that was exited.
@param dtde the DropTargetEvent created when the component was exited.
*/
public static void dragExit(DragAndDrop d, DropTargetEvent dtde) {
	if (!d.getDragAndDropControl().allowsDrop()) {
		return;
	}
	d.dropExited();
	d.getDragAndDropControl().notifyListenersDropExited();
}

/**
Drag: called when a drag is over a component that can be dragged on.
@param d the DragAndDrop object from which data was dragged.
@param dsde the DragSourceDragEvent created when the component was dragged over.
*/
public static void dragOver(DragAndDrop d, DragSourceDragEvent dsde) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		return;
	}
//	This next line isn't probably necessary, so it is commented out.
//	It is left in here as an example of what can be done in here.
//	setDragOverFeedback(d, dsde);
// 	REVISIT (JTS - 2004-02-26)
// 	should this method provide any feedback to the 'd' parameter?
}

/**
Drop: called when a drag is over a component that can be dropped on.  Calls
d.dropNotAllowed() if the component doesn't allow drops or if the drop is 
invalid.  Calls d.dropAllowed() if the drop is permitted.  In addition, 
the drop component's DragAndDropListeners are notified via the appropriate
dropAllowed() or dropNotAllowed() call.
@param d the DragAndDrop object that is being dragged over.
@param dtde the DropTargetDragEvent created when the component is dragged over.
*/
public static void dragOver(DragAndDrop d, DropTargetDragEvent dtde) {
	if ((!d.getDragAndDropControl().allowsDrop()) 
	    || (!DragAndDropUtil.isDragOK(d, dtde))) {
		d.dropNotAllowed();
		d.getDragAndDropControl().notifyListenersDropNotAllowed();
		dtde.rejectDrag();
	}
	else {
		d.dropAllowed();
		d.getDragAndDropControl().notifyListenersDropAllowed();
		dtde.acceptDrag(dtde.getDropAction());
	}
}

/**
DragGestureEvent: called when a drag should be started.  Gets the data to be
dragged and starts the drag operation.  The drag component's 
DragAndDropListeners are notified via dragAboutToStart() prior to the 
Transferable data being copied into the drag event.  Once the data have been
copied into the drag and are under the mouse cursor, d.dragStart() is called
and the drag component's DragAndDropListeners are notified via dragStart().
@param d the DragAndDrop from which the drag is occuring.
@param dsl the DragSourceListener that recognized the drag.
@param dge the DragGestureEvent that was recognized.
*/
public static void dragStart(DragAndDrop d, DragSourceListener dsl, 
DragGestureEvent dge) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		return;
	}

	if ((dge.getDragAction() & d.getDragAndDropControl().getDragAction())
		== 0) {
		return;
	}

	// give the listeners a chance to veto the drag ...
	if (!d.getDragAndDropControl().notifyListenersDragAboutToStart()) {
		return;
	}

	Transferable transferable = d.getTransferable();

	try {
		// REVISIT (JTS - 2004-02-24)
		// if you want to drag images, this code won't work
		dge.startDrag(DragSource.DefaultCopyNoDrop, transferable, dsl);
		d.dragStarted();
		d.getDragAndDropControl().notifyListenersDragStarted();
	}
	catch (InvalidDnDOperationException idoe) {
		Message.printWarning(2, __CLASS + ".dragStart", 
			"Invalid DragAndDrop operation.");
		Message.printWarning(2, __CLASS + ".dragStart", idoe);
		idoe.printStackTrace();
	}
}

/**
Drop: called when data is dropped onto an DragAndDrop component.  If for
any reason the drop is invalid, d.dropUnsuccessful() is called and 
the drop component's DragAndDropListeners are notified via dropUnsuccessful().  
If the drop is valid, d.handleDropData(...) is called.  
If the data were handled properly, d.dropSuccessful() is called and the drop 
component's DragAndDropListeners are notified via dropSuccessful(). 
Otherwise d.dropUnsuccessful() is called.
@param d the DragAndDrop onto which the drop is occurring.
@param dtde the DropTargetDropEvent created when the drop occurred.
*/
public static void drop(DragAndDrop d, DropTargetDropEvent dtde) {
	String routine = __CLASS + ".getDropObject";
	if (!d.getDragAndDropControl().allowsDrop()) {
		return;
	}
	
	DataFlavor[] flavors = d.getDataFlavors();
	int pos = findRequestedDragFlavor(d, dtde);
	if (pos == -1) {
		Message.printWarning(2, routine, "No matching data flavor "
			+ "found.");
		dtde.rejectDrop();
		d.dropUnsuccessful();
		d.getDragAndDropControl().notifyListenersDropUnsuccessful();
		return;
	}
	DataFlavor chosen = flavors[pos];
	if (IOUtil.testing()) {
		Message.printStatus(2, routine, "Using data flavor '" 
			+ chosen.getMimeType());
	}

	int sourceActions = dtde.getSourceActions();
	if ((sourceActions & d.getDragAndDropControl().getDropAction()) == 0) {
		Message.printWarning(2, routine, "No action match found for "
			+ sourceActions);
		dtde.rejectDrop();
		d.dropUnsuccessful();
		d.getDragAndDropControl().notifyListenersDropUnsuccessful();
		return;
	}

	Object data = null;
	try {
		dtde.acceptDrop(d.getDragAndDropControl().getDropAction());
		data = dtde.getTransferable().getTransferData(chosen);
	}
	catch (Throwable t) {
		Message.printWarning(2, routine, "Couldn't get transfer data: "
			+ t.getMessage());
	//	t.printStackTrace();
		Message.printWarning(2, routine, t);
		dtde.dropComplete(false);
		d.dropUnsuccessful();
		d.getDragAndDropControl().notifyListenersDropUnsuccessful();
		return;
	}

	boolean result = false;
	try {
		result = d.handleDropData(data, dtde.getLocation());
	}
	catch (Exception e) {	
		e.printStackTrace();
		result = false;
	}

	if (!result) {
		dtde.dropComplete(false);
		d.dropUnsuccessful();
		d.getDragAndDropControl().notifyListenersDropUnsuccessful();
	}
	else {
		dtde.dropComplete(true);
		d.dropSuccessful();
		d.getDragAndDropControl().notifyListenersDropSuccessful();
	}
}

/**
Drag: called when the action for a drop has been changed by the user pressing
control, shift, or control-shift.  Sets the cursor appropriately to represent
the new action.
@param d the DragAndDrop object from which data was dragged.
@param dsde the DragSourceDragEvent created when the drop action changed.
*/
public static void dropActionChanged(DragAndDrop d, 
DragSourceDragEvent dsde) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		return;
	}
	setDragOverFeedback(d, dsde);
}

/**
Drop: called when the action for a drop has been changed by the user pressing
control, shift, or control-shift.
@param d the DragAndDrop object on which data is being dragged.
@param dtde the DropTargetDragEvent created when the drop action changed.
*/
public static void dropActionChanged(DragAndDrop d, 
DropTargetDragEvent dtde) {
	if ((!d.getDragAndDropControl().allowsDrop()) 
	    || (!DragAndDropUtil.isDragOK(d, dtde))) {
		dtde.rejectDrag();
	}
	else {
		dtde.acceptDrag(dtde.getDropAction());
	}
}

/**
Finds the numeric position of the first of the DragAndDrop's DragFlavors 
that the DropTargetDropEvent supports, in the DragAndDrop's DataFlavor 
array.  In the low-level java code, checks to see if a List contains() an 
instance of one of the drag flavors.
@param d the DragAndDrop into which data will be dropped.
@param dtde the DropTargetDropEvent of the data that is trying to be dropped.
@return the numeric position of the first supported DataFlavor, or -1 if 
none of the DataFlavors match or are supported.
*/
private static int findRequestedDragFlavor(DragAndDrop d, 
DropTargetDropEvent dtde){
	DataFlavor[] flavors = d.getDataFlavors();	
	for (int i = 0; i < flavors.length; i++) {
		if (dtde.isDataFlavorSupported(flavors[i])) {
			return i;
		}
	}
	return -1;
}

/**
Checks to see if the drag event supports the data flavors in the 
DragAndDrop object.
@param d the DragAndDrop object into which data is being dragged.
@param dtde the DropTargetDragEvent with information about the drag.
@return true if the drag events supports the DragAndDrop's data flavors.  
False if not.
*/
public static boolean isDragFlavorSupported(DragAndDrop d, 
DropTargetDragEvent dtde) {
	DataFlavor[] flavors = d.getDataFlavors();	
	for (int i = 0; i < flavors.length; i++) {
		if (dtde.isDataFlavorSupported(flavors[i])) {
			return true;
		}
	}
	return false;
}

/**
Checks to see if a drag action is OK or not.  The drag action is OK if
the DataFlavor of the drag is supported and the drop action of the 
drag is supported, too.
@param d the DragAndDrop object into which something is being dragged.
@param dtde the DropTargetDragEvent of the attempted drag.
@return true if the drag is OK, false if not.
*/
private static boolean isDragOK(DragAndDrop d, DropTargetDragEvent dtde) {
	String routine = __CLASS + ".isDragOK";
	
	if (!isDragFlavorSupported(d, dtde)) {
		if (IOUtil.testing()) {
			Message.printWarning(2, routine, "Drag Flavor is not "
				+ "supported.");
		}
		return false;
	}

	int da = dtde.getDropAction();
	if ((da & d.getDragAndDropControl().getDropAction()) == 0) {
		if (IOUtil.testing()) {
			Message.printWarning(2, routine, 
				"No acceptable matching drag "
				+ "action compared to: " + da);
			Message.printWarning(2, routine, "   drop action is: "
				+ d.getDragAndDropControl().getDropAction());
		}
		return false;
	}
	return true;
}

/**
Unregisters a DragAndDropListener for an DragAndDrop object.
@param r the DragAndDrop object on which to unregister the listener.  This can
be either a component from which can are dragged or a component on which
data are dropped.
@param d the listener to unregister.
*/
public static void removeDragAndDropListener(DragAndDrop r, 
DragAndDropListener d) {
	r.getDragAndDropControl().removeDragAndDropListener(d);
}

/**
Sets the cursor feedback for when data is being dragged over a component.
@param d the DragAndDrop object from which data is being dragged.
@param dsde the DragSourceDragEvent created when data is over a component.
*/
public static void setDragOverFeedback(DragAndDrop d, 
DragSourceDragEvent dsde) {
	if (!d.getDragAndDropControl().allowsDrag()) {
		return;
	}

	DragSourceContext context = dsde.getDragSourceContext();
	int dropAction = dsde.getDropAction();
	int dragAction = d.getDragAndDropControl().getDragAction();
	
	// if the drop action is valid, then set the cursor to represent that
	if ((dropAction & dragAction) != 0) {
		if (dropAction == ACTION_COPY 
		    || dropAction == ACTION_COPY_OR_MOVE) {
			context.setCursor(DragSource.DefaultCopyDrop);
		}
		else if (dropAction == ACTION_MOVE) {
			context.setCursor(DragSource.DefaultMoveDrop);
		}
		else {
			context.setCursor(DragSource.DefaultCopyDrop);
		}
	}
	// otherwise, put a "can't do that" cursor
	else {
		if (dragAction == ACTION_COPY 
		    || dragAction == ACTION_COPY_OR_MOVE) {
			context.setCursor(DragSource.DefaultCopyNoDrop);
		}
		else if (dragAction == ACTION_MOVE) {
			context.setCursor(DragSource.DefaultMoveNoDrop);
		}
		else {
			context.setCursor(DragSource.DefaultCopyNoDrop);
		}	
	}
}

}
