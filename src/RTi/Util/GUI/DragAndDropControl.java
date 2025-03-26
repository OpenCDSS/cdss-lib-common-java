// DragAndDropControl - class for holding data necessary for components that implement drag and drop capabilities.

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

import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;

/**
This class holds data necessary for components that implement DragAndDrop.
The data are stored here mainly because Java interfaces are not 
allowed to hold member variables.<p>

All classes that implement DragAndDrop need to have a private member 
variable of this type, and they must all implement a method to return that 
member variable.  It is through the data stored in this variable that the 
DragAndDropUtil code knows how drags and drops should work on components.  
Developers building drag-and-drop capable components should not need to do 
much with the DragAndDropControl objects once they are instantiated.  
DragAndDropUtil will take care of working with this data.<p>

See the DragAndDropListener documentation for information on the order in
which listener calls happen.
*/
public class DragAndDropControl {

/**
The name of the class.
*/
private final String __CLASS = "DragAndDropControl";

/**
The action that is performed on a drag (see DragAndDropUtil.ACTION_*).  For 
example, if this is ACTION_COPY, then when data are dragged from a component
the system knows that the data should be copied from that component and not
removed once the drag is complete.
*/
private int __dragAction = DragAndDropUtil.ACTION_NONE;

/**
The action that is performed on a drop (see DragAndDropUtil.ACTION_*).  For
example, if this is ACTION_MOVE, then when data are dropped on a component
the system knows that the data are to be removed from the origin component
and placed into the destination component.
*/
private int __dropAction = DragAndDropUtil.ACTION_NONE;

/**
The drag source.  DragSource objects are Java objects that contain information
about where drags can and cannot occur and relay that to the JVM running the GUI.
*/
private DragSource __dragSource = null;

/**
The drop target.  DropTarget objects are Java objects that contain information
about where drops can and cannot occur and relay that to the JVM running the GUI.
*/
private DropTarget __dropTarget = null;

/**
The alternate transferable object that should be used instead of the default
component transferable (if not null).   An example of when this could be used:
<p>An application may set an alternate transferable in a 
DragAndDropSimpleJComboBox, so that when something is dragged from the combo 
box, it is not the text displayed in the combo box that is dragged, but an
object to which the text refers.  The combo box may have lists of time series
identifiers, but when the drag occurs, the listener (notified by 
dragAboutToStart from the DragAndDropListener class) can put the time series
to be transferred in the DragAndDrop as the alternate transferable and then
the time series, not the time series identifier, will be what is dragged and dropped.

*/
private Transferable __alternateTransferable;

/**
The list of DragAndDropListeners registered for this drag and drop 
component.  This is used so that other classes can be informed of events 
occurring during the drag and drop process.
*/
private List<DragAndDropListener> __listeners = null;

/**
Constructor.  Sets up whether drags and drops are possible as well as the 
appropriate drag and drop actions.
@param dragAction the action that drags perform (see DragAndDropUtil.ACTION_*) 
when data is dragged from the component.
@param dropAction the action that drops perform (see DragAndDropUtil.ACTION_*) 
when data is dropped on the component.
*/
public DragAndDropControl(int dragAction, int dropAction) {
	__listeners = new Vector<DragAndDropListener>();
	setDragAction(dragAction);
	setDropAction(dropAction);
}

/**
Registers a DragAndDropListener on the component to which this data belongs.
If the listener is already registered, it will not be registered again.
@param d the DragAndDropListener to register.
*/
public void addDragAndDropListener(DragAndDropListener d) {
	DragAndDropListener listener;
	for (int i = 0; i < __listeners.size(); i++) {
		listener = (DragAndDropListener)__listeners.get(i);
		if (listener == d) {
			return;
		}
	}
	__listeners.add(d);
}

/**
Returns whether drags are supported.  Checks to make sure that the drag action
is not ACTION_NONE.
@return whether drags are supported.
*/
public boolean allowsDrag() {
	// any other drag action (ACTION_MOVE, ACTION_COPY, ACTION_COPY_OR_MOVE)
	// means that data can be dragged from the component.
	if (__dragAction != DragAndDropUtil.ACTION_NONE) {
		return true;
	}
	return false;
}

/**
Returns whether drops are supported.  Checks to make sure that the drop action
is not ACTION_NONE.
@return whether drops are supported.
*/
public boolean allowsDrop() {
	// any other drag action (ACTION_MOVE, ACTION_COPY, ACTION_COPY_OR_MOVE)
	// means that data can dropped on the component.
	if (__dropAction != DragAndDropUtil.ACTION_NONE) {
		return true;
	}
	return false;
}

/**
Returns the alternate transferable of the component to which this data 
belongs.  For more information on alternate transferables, see the javadocs
for setAlternateTransferable().
@return the alternate transferable of the component to which thsi data 
belongs.
*/
public Transferable getAlternateTransferable() {
	return __alternateTransferable;
}

/**
Returns the drag action.
@return the drag action.
*/
public int getDragAction() {
	return __dragAction;
}

/**
Returns the drop action.
@return the drop action.
*/
public int getDropAction() {
	return __dropAction;
}

/**
Returns the drag source.
@return the drag source.
*/
public DragSource getDragSource() {
	return __dragSource;
}

/**
Returns the drop target.
@return the drop target.
*/
public DropTarget getDropTarget() {
	return __dropTarget;
}

/**
Notifies DragAndDropListeners registered on the drag component to which 
this data belongs that data is about to be copied into the drag for 
transfer to another component.  At this point, registered listeners can 
veto the drag or can make some last-minute changes to the data before 
it is put into the drag.
@return true if all the listeners will allow the drag to begin, false if any
veto the drag.
*/
public boolean notifyListenersDragAboutToStart() {
	boolean successful = true;
	for (int i = 0; i < __listeners.size(); i++) {	
		if (!((DragAndDropListener)__listeners.get(i)).dragAboutToStart()) {
			successful = false;
		}
	}
	return successful;
}

/**
Notifies DragAndDropListeners registered on the drag component to which
this data belongs that the drag has officially started and data is 
currently under the mouse cursor and being dragged to another component.
*/
public void notifyListenersDragStarted() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dragStarted();
	}
}

/**
Notifies DragAndDropListeners registered on the drag component to which this
data belongs that data dragged out of this component was successfully dropped elsewhere.
@param action the action performed by the component that received the dropped data.
*/
public void notifyListenersDragSuccessful(int action) {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dragSuccessful(action);
	}
}

/**
Notifies DragAndDropListeners registered on the drag component to which this
data belongs that data dragged out of this component was unsuccessfully dropped elsewhere.  
@param action the action attempted by the component that tried to receive the dropped data.
*/
public void notifyListenersDragUnsuccessful(int action) {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i))
			.dragUnsuccessful(action);
	}
}

/**
Notifies DragAndDropListeners registered on the drop component to which this
data belongs that a drop is allowed for the area that the mouse cursor is
over.  Components can use this notification to change GUI attributes such
as Colors or information in status bars.
*/
public void notifyListenersDropAllowed() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dropAllowed();
	}
}

/**
Notifies DragAndDropListeners registered on the drop component to which 
this data belongs that a drop area has been exited.  Components can use this
notification to change back GUI attributes that may have been set when the
mouse cursor moved into a drop area.
*/
public void notifyListenersDropExited() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dropExited();
	}
}

/**
Notifies DragAndDropListeners registered on the drop component to which this
data belongs that a drop is not allowed for the area that the mouse cursor
is over.  Components can use this notification to change GUI attributes such
as Colors, or to put information in status bars.
*/
public void notifyListenersDropNotAllowed() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dropNotAllowed();
	}
}

/**
Notifies DragAndDropListeners registered on the drop component to which this
data belongs that a data drop was performed successfully.
*/
public void notifyListenersDropSuccessful() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dropSuccessful();
	}
}

/**
Notifies DragAndDropListeners registered on the drop component to which this
data belongs that a data drop was performed but it was not successful.
*/
public void notifyListenersDropUnsuccessful() {
	for (int i = 0; i < __listeners.size(); i++) {	
		((DragAndDropListener)__listeners.get(i)).dropUnsuccessful();
	}
}

/**
Unregisters a DragAndDropListener on the component to which this data belongs.
@param d the DragAndDropListener to unregister.
*/
public void removeDragAndDropListener(DragAndDropListener d) {
	DragAndDropListener listener;
	for (int i = 0; i < __listeners.size(); i++) {
		listener = (DragAndDropListener)__listeners.get(i);
		if (listener == d) {
			__listeners.remove(listener);
			return;
		}
	}
}

/**
Sets an alternate transferable object that a DragAndDrop-enabled GUI object
can put into a drag.  Transferable objects are the data that are 
transferred between components in a drag (see 
RTi.Util.GUI.DragAndDropTransferPrimitive).  <p>

Alternate transferables are useful for situations such as the following:<p>

A DragAndDropSimpleJComboBox holds the names of time series.  A time series
can be dragged from the combo box to another component, and when that happens
the actual time series data, not the time series data name, should be moved
to the other component.  <p>

Since DragAndDropSimpleJComboBoxes store strings and transfer their data
using DragAndDropPrimitives, a drag and drop from this class would transfer
a String by default.  But by listening to the 'dragAboutToStart()' event
(in the DragAndDrop interface) and using this method, the GUI can set up
the TS to be the Transferable object when the drag starts.  Once the drag 
is complete -- either successful or not -- the alternate transferable will
be set to null automatically.<p>

Here is the setAlternateTransferable() pseudo-code for the above example:<p>

<blockquote><pre>
	public boolean dragAboutToStart() {
		// get the time series id stored in the combo box
		String tsid = __tsidComboBox.getSelected();
		// try to get the time series data
		TS ts = getTimeSeriesForID(tsid);
		// if no data was returned, the drag cannot happen ...
		if (ts == null) {
			// ... so veto the drag by returning false.
			return false;
		}
		else {
			// ... otherwise, set the time series as the 
			// alternate transferable (otherwise the combo box
			// is set up to automatically transfer whatever
			// string is currently selected -- in this case
			// the tsid)
			__tsidComboBox.setAlternateTransferable(ts);
			// ... and accept the transfer
			return true;
		}
	}
</pre></blockquote>
*/
public void setAlternateTransferable(Transferable t) {
	__alternateTransferable = t;
}

/**
Sets the action to be performed when data is dragged from this object.  See
DragAndDropUtil.ACTION_*.
@param dragAction drag action to set.
*/
public void setDragAction(int dragAction) {
	String routine = __CLASS + ".setAction";
	if (dragAction != DragAndDropUtil.ACTION_COPY 
	    && dragAction != DragAndDropUtil.ACTION_MOVE
	    && dragAction != DragAndDropUtil.ACTION_COPY_OR_MOVE
	    && dragAction != DragAndDropUtil.ACTION_NONE) {
	    	Message.printWarning(2, routine, "Invalid dragAction: " 
			+ dragAction + ".  Defaulting to "
			+ "DragAndDropUtil.ACTION_NONE.");
		__dragAction = DragAndDropUtil.ACTION_NONE;
	}
	else {
		__dragAction = dragAction;
	}
}

/**
Sets the action to be performed when data is dropped on this object.  See 
DragAndDropUtil.ACTION_*.
@param dropAction drop action to set.
*/
public void setDropAction(int dropAction) {
	String routine = __CLASS + ".setAction";
	if (dropAction != DragAndDropUtil.ACTION_COPY 
	    && dropAction != DragAndDropUtil.ACTION_MOVE
	    && dropAction != DragAndDropUtil.ACTION_COPY_OR_MOVE
	    && dropAction != DragAndDropUtil.ACTION_NONE) {
	    	Message.printWarning(2, routine, "Invalid dropAction: " 
			+ dropAction + ".  Defaulting to "
			+ "DragAndDropUtil.ACTION_NONE.");
		__dropAction = DragAndDropUtil.ACTION_NONE;
	}
	else {
		__dropAction = dropAction;
	}
}

/**
Sets the drag source.
@param ds drag source to set.
*/
public void setDragSource(DragSource ds) {
	__dragSource = ds;
}

/**
Sets the drop target.
@param dt drop target to set.
*/
public void setDropTarget(DropTarget dt) {
	__dropTarget = dt;
}

/**
Returns a String representation of this object, suitable for debugging.
@return a String representation of this object, suitable for debugging.
*/
public String toString() {
	String atc = null;
	if (__alternateTransferable == null) {
		atc = "null";
	}
	else {
		atc = "" + __alternateTransferable.getClass();
	}
	return	"AllowsDrag: " + allowsDrag() + "\n" +
		"AllowsDrop: " + allowsDrop() + "\n" + 
		"DragAction: " + __dragAction + "\n" + 
		"DropAction: " + __dropAction + "\n" + 
		"DragSource: " + __dragSource + "\n" +
		"DropTarget: " + __dropTarget + "\n" +
		"AlternateTransferable Class: " + atc;
}

}
