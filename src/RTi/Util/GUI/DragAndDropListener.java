// RTiDragAndDrop - interface for components that support drag and drop

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
// RTiDragAndDrop - Interface for components that support drag and drop.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
// 2004-02-24	J. Thomas Sapienza, RTi	Initial version.
// 2004-03-04	JTS, RTi		Updated Javadocs in response to 
//					numerous changes.
// 2004-04-27	JTS, RTi		Docs revised after SAM's review.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This interface defines the methods used by a class that wants to be informed
of drag and drop events.  The following methods must be defined in the listener 
class:<p>
<pre>
public boolean dragAboutToStart() {}
public void dragStarted() {}
public interface DragAndDropListener {}
public void dragUnsuccessful(int action) {}
public void dragSuccessful(int action) {}
public void dropAllowed() {}
public void dropExited() {}
public void dropNotAllowed() {}
public void dropSuccessful() {}
public void dropUnsuccessful() {}
</pre>

<b>Order of Listener Calls</b>

A large number of the class methods are concerned with notifying listeners
when certain events occur during the drag and drop process.  The following 
details the order when events are called:<p>

<ol>
<li>DragAndDropControl object is created in a component that handles dragging
or dropping.</li>
<li>Listeners are registered to the compoennt.</li>
<li><b>DragAndDropUtil.dragStart()</b> is called, which calls 
	<b>DragAndDropControl.notifyListenersDragAboutToStart()</b>, which calls
	<b>DragAndDropListener.dragAboutToStart()</b></li>
	
<li>Later in <b>DragAndDropUtil.dragStart()</b>, 
	<b>DragAndDrop.dragStarted()</b> is called.  Immediately afterward, 
	<b>DragAndDropControl.notifyListenersDragStarted()</b> is called,
	which calls <b>DragAndDropListener.dragStarted()</b></li>	
	
<li>When the user drags data over a component that can be dropped on, 	
	<b>DragAndDropUtil.dragEnter()</b> is called.  It checks to see 
	whether the component will allow the kind of data that are in the 
	drag to be dropped on it.  If not, 
	<b>DragAndDrop.dropNotAllowed()</b> is called.  Immediately afterward,
	<b>DragAndDropControl.notifyListenersDropNotAllowed()</b> is called,
	which calls <b>DragAndDropListener.dropNotAllowed()</b><p>

	If the kind of data in the drag are allowed to be dropped in the 
	component, <b>DragAndDrop.dropAllowed()</b> is called.  
	Immediately afterward,
	<b>DragAndDropControl.notifyListenersDropAllowed()</b> is called,
	which calls <b>DragAndDropListener.dropAllowed()</b></li>

<li>When the user drags data over a component that can be dropped on, and
	then continues dragging the data out of the component, 
	<b>DragAndDropUtil.dragExit()</b> is called.  It calls
	<b>DragAndDrop.dropExited()</b>, and immediately afterward calls
	<b>DragAndDropControl.notifyListenersDropExited()</b>, which calls
	<b>DragAndDropListener.dropExited()</b>.</li>
	
<li>When the mouse button is released and data have been dropped on a drop 
	target, the component from which data were dragged checks to see 
	if the data were dragged successfully to the component
	in <b>DragAndDropUtil.dragDropEnd()</b>.  If the data were not
	dragged successfully for any reason, 
	<b>DragAndDrop.dropUnsuccessful()</b> is called.  Immediately afterward,
	<b>DragAndDropControl.notifyListenersDragUnsuccessful()</b> is called,
	which calls <b>DragAndDropListener.dragUnsuccessful()</b><p>
	
	If the data were dragged successfully, 
	<b>DragAndDrop.dropSuccessful()</b> is called.  Immediately afterward,
	<b>DragAndDropControl.notifyListenersDragSuccessful()</b> is called,
	which calls <b>DragAndDropListener.dragSuccessful()</b><p></li>
	
<li>When the mouse button is released and data have been dropped on a drop
	target, the component on which data are being dropped checks to
	see if the data were dropped successffuly in 
	<b>DragAndDropUtil.drop()</b>.  If the data were not dropped
	successfully for any reason, 
	<b>DragAndDrop.dropUnsuccessful()</b> is called.  Immediately afterward,
	<b>DragAndDropControl.notifyListenersDropUnsuccessful()</b> is called,
	which calls <b>DragAndDropListener.dropSuccessful()</b><p>

	If the data were dropped successfully,
	<b>DragAndDrop.dropSuccessful()</b> is called.  Immediately afterward,
	<b>DragAndDropControl.nofityListenersDropSuccessful()</b> is called,
	which calls <b>DragAndDropListener.dropSuccessful()</b><p></li>
</ol>
*/
public interface DragAndDropListener {

/**
This method is called for the component from which data are being dragged
to indicate that a drag is about to start.  It gives listeners a chance 
to set an alternate Transferable object (see 
DragAndDropData.setAlternateTransferable() for more information) or to
veto the drag if there are conditions that indicate a drag should
not be allowed.<p>
The value returned indicates whether the current listener will allow the 
drag to start.  If any listener registered on a component returns false 
on dragAboutToStart(), the drag will not be started.<p>
dragAboutToStart() will be called for every listener registered on a drag
component, even if one returns false.
@return whether the current listener will allow the drag to start.  
*/
public boolean dragAboutToStart();

/**
This method is called for the component from which data are being dragged
to indicate that a drag has been started and data are currently in transit
under the mouse cursor.
*/
public void dragStarted();

/**
This method is called for the component from which data are being dragged to
indicate that although a drag ended, it was not successful for one reason 
or another.  No information is available about why the drag was not 
successful, only that it failed.
@param action the action that was attempted (DragAndDropUtil.ACTION_MOVE, 
DragAndDropUtil.ACTION_COPY, etc) by the component on which data was 
being dropped.  
*/
public void dragUnsuccessful(int action);

/**
This method is called for the component from which data are being dragged
to indicate that a drag ended and the data was successfully dragged.  This
method can be used by a component, for instance, to remove the local copy of 
data that was dragged with an action of DragAndDropUtil.ACTION_MOVE.  <p>

Since components can have action that are not exactly the same 
(DragAndDropUtil.ACTION_MOVE and DragAndDropUtil.ACTION_COPY_OR_MOVE, for 
instance) to determine the action to take when a drag was successful, call 
DragAndDropUtil.determineAction() like this:<p>
<blockquote><pre>
	int result = DragAndDropUtil.determineAction(
		__data.getDragAction(), action);
</blockquote></pre><p>
This call uses the drag action set in the local data member as well as the
drag action that the drop component responded to and returns the action 
that actually occurred. 
@param action the action that happened (DragAndDropUtil.ACTION_MOVE, 
DragAndDropUtil.ACTION_COPY, etc) by the component on which data was 
being dropped.  
*/
public void dragSuccessful(int action);

/**
This method is called for a component on which data can be dropped 
when data has been dragged over it and when the component can accept the data.  
It can be used to change how the drop component looks in order to visually
show the user that they can drag data there successfully, for instance, by 
changing the border color to green.
*/
public void dropAllowed();

/**
This method is called for a a component on which data can be dropped 
when a drag has left its area.
If the dropAllowed() or dropNotAllowed() methods were used to change how the 
component looks in, this method can be used to set it back to looking normal.
*/
public void dropExited();

/**
This method is called for a component on which data can be dropped 
when data has been dragged over it and when the component cannot 
accept the data.  
It can be used to change how the drop component looks in order to visually
show the user that they cannot drag data there successfully, for instance, by 
changing the border color to red.
*/
public void dropNotAllowed();

/**
Called on the component on which data are being dropped when a drag was 
successful and data was dropped onto that component.
*/
public void dropSuccessful();

/**
Called on the component on which data are being dropped when a drag was not
successful, and no data was transferred into that component.
*/
public void dropUnsuccessful();

}
