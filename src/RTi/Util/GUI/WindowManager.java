//------------------------------------------------------------------------------
// WindowManager - abstract class to allow applications to easily manage
//	their windows.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 2004-02-05	J. Thomas Sapienza, RTi	Initial version based off of StateMod's
//					WindowManager.
// 2004-02-26	JTS, RTi		Changed docs and code after review 
//					by SAM.
// 2004-12-21	JTS, RTi		Added closeAllWindows().
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

/**
The WindowManager class allows applications to more easily manage their windows.  This
class handles opening 2 kinds of windows:<br><ul>
<li>Windows where only instance can be opened at a time.  These are referred
to simply as Windows.</li>
<li>Windows where multiple instances can be opened at a time.  These are
referred to as Window Instances.</li>
</ul><p>

This base class should be extended to create a Window Manager for each
application.  The most basic extended Window Manager should at least 
implement the displayWindow() method and call the super class constructor with 
a parameter that is the number of windows to be managed.  The manager should
also declare integer values for referring to each window, for instance:
<blockquote><pre>
	public final int 
		WINDOW_MAIN =		0,
		WINDOW_OPTIONS = 	1,
		WINDOW_NEW_DATA = 	2;
</pre></blockquote><p>

<b>General Overview</b><p>

In the first case, 
a window of a particular type can only have one visible instance.
If the user opens a window and tries to open it again, the
WindowManager will move the already-open copy of the window to the front of
the screen and will not create a new copy of the window.  Until the user 
closes the
window, the WindowManager will continue to do this every time an attempt is made
to re-open the window.  This case will use any method named 
<b>XXXWindow()</b> (where XXX is the action performed by the method, such as
set or get).<p>

The second case works almost identically to the first, except that there can
be multiple instances of each window open.  Each instance of the window is
identified by a unique identifier (see below).  
If the application attempts to 
open another copy of the window with the same unique 
identifier, the already-open copy of the window with that identifier is 
displayed.  This case will use the methods named <b>XXXWindowInstance()</b>.
In addition, any variables that have the word 'instance' in their name are
only used with window instances.<p>

<b>Unique Identifier</b><p>
The unique identifier that is used to manage multiple instances of the same
window type is application-dependent and can be any Object, though the Object
must implement the <tt>boolean equals(Object)</tt>
method, which is used to compare identifiers.<p>

Note that the unique identifiers are used to identify different Window 
Instances, not Window types.  Window types are identified with integer values
that typically are named WINDOW_*, as defined in the derived window manager
class.  These integers are used as array indices and should therefore be
numbered sequentially, starting with 0 (see below).  
For example, to open a certain Window in
an application code like this might be used:<p>
<blockquote><pre>
	JFrame window = __windowManager.displayWindow(WINDOW_OPTIONS, false);
</pre></blockquote><p>
To open a certain instance of a particular window, the following code might
be used:<p>
<blockquote><pre>
	JFrame window = __windowManager.displayWindow(WINDOW_NEW_DATA, false,
		"ID: " + data.getID());
</blockquote></pre><p>

In the first example only a single instance of the window represented by 
WINDOW_OPTIONS can be open.  In the second example, multiple instances of the
window represented by WINDOW_NEW_DATA can be open, and in this case an instance
with the unique identifier '"ID: " + data.getID()' is being opened or displayed.
<p>

Unique identifiers are not required to be Strings.  They can be
any Object that implements the equals() method.  However, using simple objects
may improve performance and be more robust.<p>

<b>Initialize</b><p>
Apart from the method displayWindow() that must be extended when 
derived classes are built, 
derived classes must also call the initialize() method of the
super class.  The parameter passed to initialize is the number of window types
that will be managed by the WindowManager.  Here is some example Constructor 
code:<p>
<blockquote><pre>
protected final static int _NUM_WINDOWS = 12;
	public GenericWindowManager() {		
		super(_NUM_WINDOWS);
	}
</pre></blockquote><p>

<b>Window Instances</b><p>

By default, no window allows multple instances.  This must be turned on with
a call to setAllowMultipleWindowInstances(). Here some sample code:<p>
<blockquote><pre>
	__windowManager.setAllowMultipleWindowInstances(WINDOW_OPTIONS, true);
</blockquote></pre><p>
This turns on multiple instances for the Options window.  Otherwise, only a 
single options window could ever be open.<p>

<b>Internal Arrays</b><p>
This class manages several internal arrays (such as _windowStatus and 
_windows) that are sized by the initialize() method to the number of windows
passed in to that method (see above).  The index of each element in these
arrays corresponds to a particular window type.  These unique indices should
be specified in the derived class.  As an example:<p>
<blockquote><pre>
	public final int 
		WINDOW_MAIN =		0,
		WINDOW_OPTIONS = 	1,
		WINDOW_NEW_DATA = 	2;
</pre></blockquote><p>
In the above example, the window manager's constructor should call
super.initialize() with a parameter of 3, so that the window manager is set up
to manage 3 windows.  At this point, each of the above window types has a 
space set aside for it in the internal arrays.  Information in the arrays about
the main window is in array position 0, information about the options window
is in array position 1, and information about the new data window is in array
position 2.<p>

These window types are used to represent window regardless of whether the
window supports only a single instance being open or multiple instances being
open.  In fact, because of the way this class implements the window management
system, all the windows can be represented as both windows that allow a single
instance and windows that allow multiple instances, at the same time.<p>

An example of how this could be useful uses an imaginary application in 
which a window allows both entry of new data and editing of existing data.  It
is decided that only a single new entry window should be open at a time.  More
than one edit window for existing data can be open at a time.  When the window
for creating new data is created, the setWindow() methods are used.  When
the windows for editing existing data are created, the setWindowInstance() 
methods are used.  In this way, the same window identifier is used to refer
to both types of windows.  Since only a single Window can be open at a time,
using the window methods for new data reults in only a single new data window
ever able to be open.  Using the window instance methods allows multiple data
edit windows to be open.
<p>

The WINDOW_* are used in methods to refer to the window type upon which an 
operation should occur instead of referring to each window type by an integer
value.<p>

<b>displayWindow()</b><p>
The displayWindow() method must be overridden in order for derived
WindowManager classes to properly open windows.  Because this method will
vary greatly depending on the application in which the WindowManager is 
being used, the following is a step-by-step description of what should 
happen in this method:<p>

<b>1.</b> If the window does not allow multiple instances to be open, then 
do the following:<p>
<blockquote>
	<b>1.1.</b>If no copy of the window is already open
	create a new copy of the window and set it to be open.<p>
	<pre>
		if (getWindowStatus(winIndex) != STATUS_OPEN) {
			JFrame window = new WindowToBeCreated(...);
			setWindowOpen(windowIndex, window);
	</pre><p>
	<b>1.2.</b> Otherwise the window must already be open.
	If so, make sure it is not minimized and then pop the window to
	the front of all the windows.  Code similar to this can be used:<p>
	<pre>
		if (getWindowStatus(windowType) == STATUS_OPEN) {	
			win = getWindow(windowType);
			win.setState(win.NORMAL);
			win.toFront();
			return win;
		}
	</pre><p>
</blockquote>
<b>2.</b> Check to see if the window to be displayed allows multiple instances
to be displayed.<p>
<blockquote>
	<b>2.1.</b> If so, then get the unique identifier for the window 
	instance to display.  For example, 
	have the application generate it and then pass it 
	in to an overloaded version of the displayWindow method.<p>
	<b>2.2.</b> If no instance of the window with the identifier is
	already open then create a new one and call setInstanceOpen to
	set that instance of the window to be marked as open.  The following
	pseudo-code demonstrates this:<p>
	<pre>
		// id == unique instance identifier passed in to the
		// displayWindow call
		if (getWindowInstanceStatus(winIndex, id) != STATUS_OPEN) {
			JFrame window = new WindowToBeCreated(...);
			setWindowInstanceOpen(windowIndex, window, id);
	<pre><p>	
	<b>2.3.</b> Call getWindowInstanceStatus for the unique identifier
	and see if there is already a window instance with that identifier
	that is open.  If the window
	exists, make sure the window is not minimized and
	also pop it to the front.  Code similar to this can be used:<p>
	<pre>
		if (getWindowInstanceStatus(windowType, id) == STATUS_OPEN) {
			win = getWindowInstanceWindow(windowType, id);
			win.setState(win.NORMAL);
			win.toFront();
			return win;
		}
	</pre><p>
</blockquote><p>
Working with Windows and Window Instances should not be confusing, as the code
is practically the same.  The major differences are that method names differ
slightly (doSomethingToWindow() versus doSomethingToWindowInstance()) and
that Window Instance methods will require passing in the unique identifier in
order to locate the instance on which to operate.  <p>

<b>Closing Windows</b><p>
All the code for closing a window inside application JFrames must go
through the WindowManager, if the WindowManager is to manage those windows.
Do not dispose() of JFrames that are managed by the WindowManager -- let the
WindowManager close them instead.
This code will be fairly-JFrame specific, though it must finally call either
closeWindow(...) or closeWindowInstance(...), depending on the type of 
window it was opened as.  These methods will close the window.<p>

As an example, in a JFrame, the calls for closing the frame 
(from a Close button, an error in the frame, or pressing the X button) 
should result in a call being made to __windowManager.closeWindow(WINDOW_X); or
__windowManager.closeWindowInstance(WINDOW_X, id);.
Of course, the name of the Window Manager instance and the window type variable
will be different in actual code, but this is the general pattern.  
*/
public abstract class WindowManager {

/**
Class name.  
*/
private final String __CLASS = "WindowManager";

/**
Window status settings.<ul>
<li>STATUS_UNMANAGED - refers to window instances that have not yet been opened
and managed by the window manager.  There is no reference to them in any of
the Window Manager's internal data arrays.  In other words, the window instance
currently is not open, and it isn't an instance that was open before but has
now been set to closed.  It is just not in the Window Manager's memory -- it's
a brand new instance, and currently unmanaged.  Only used in reference to 
window instances.</li>
<li>STATUS_CLOSED - refers to windows that are closed and to window instances
that are managed but not open yet.</li>
<li>STATUS_OPEN - refers to windows and window instances that are currently open
and being managed.</li>
*/
protected final static int 
	STATUS_UNMANAGED =	-1,
	STATUS_CLOSED = 	0,
	STATUS_OPEN = 		1;
	// INVISIBLE - might be needed if we decide to not fully destroy
	// windows - that is why an integer is tracked (not a boolean)

/**
Array that marks which window types are allowed to have multiple instance 
windows open.  This will be sized to the number of windows passed in the
WindowManager constructor, and each array position will correspond to one
window and one of the WINDOW_* identifiers.
*/
protected boolean[] _allowMultipleWindowInstances;

/**
Array to keep track of window status (STATUS_OPEN or STATUS_CLOSED).
This will be sized to the number of windows passed in the
WindowManager constructor, and each array position will correspond to one
window and one of the WINDOW_* identifiers.
*/
protected int[] _windowStatus;

/**
Array of all the windows that are open.
This will be sized to the number of windows passed in the
WindowManager constructor, and each array position will correspond to one
window and one of the WINDOW_* identifiers.
*/
protected JFrame[] _windows;

/**
Array of Vectors, each of which contains WindowManagerData Objects representing
the statuses of the windows at given array index positions.  Since each window
type could possibly be made to handle window instances, and since it isn't 
possible to determine how many window instances could open at one time, Vectors
are used.  
TODO - maybe something to set in future to limit the number of instance 
windows that can be open at one time?
*/
protected List[] _windowInstanceInformation;

/**
Constructor.
@param numWindows the number of windows to set up the arrays to manage.
*/
public WindowManager(int numWindows) {
	initialize(numWindows);
}

/**
Indicates whether the window at the specified index allows multiple instance
windows.
@param windowType the index of the window to check.
@return whether the window allows multiple instances.
*/
public boolean allowsMultipleWindowInstances(int windowType) {
	return _allowMultipleWindowInstances[windowType];
}

/**
Checks to see if any windows are currently open.
@return true if any windows or window instances are open, false if not.
*/
public boolean areWindowsOpen() {
	for (int i = 0; i < _windowStatus.length; i++) {
		if (_windowStatus[i] == STATUS_OPEN) {
			return true;
		}
	}

	List v;
	WindowManagerData data;
	int size = 0;
	for (int i = 0; i < _windowInstanceInformation.length; i++) {
		if (_windowInstanceInformation[i] != null) {
			v = _windowInstanceInformation[i];
			size = v.size();
			for (int j = 0; j < size; j++) {
				data = (WindowManagerData)v.get(j);
				if (data.getStatus() == STATUS_OPEN) {
					return true;
				}
			}
		}
	}
	
	return false;
}

/**
Closes all windows that are currently open.
*/
public void closeAllWindows() {
	for (int i = 0; i < _windowStatus.length; i++) {
		if (_windowStatus[i] == STATUS_OPEN) {
			closeWindow(i);
		}
	}

	List v;
	WindowManagerData data;
	int size = 0;
	for (int i = 0; i < _windowInstanceInformation.length; i++) {
		if (_windowInstanceInformation[i] != null) {
			v = _windowInstanceInformation[i];
			size = v.size();
			for (int j = 0; j < size; j++) {
				data = (WindowManagerData)v.get(j);
				if (data.getStatus() == STATUS_OPEN) {
					closeWindowInstance(i, data.getID());
				}
			}
		}
	}
}

/**
Closes a certain instance of a window.
@param windowType the index of the window type.
@param id the unique identifier of the windows instance to close.
*/
public void closeWindowInstance(int windowType, Object id) {
	String routine = __CLASS + ".closeWindowInstance";
	int status = getWindowInstanceStatus(windowType, id);
	Message.printStatus(2, routine, 
		"Closing window : " + windowType + " with instance identifier "
		+ "'" + id + "' and current status " + status);
	if (status == STATUS_CLOSED || status == STATUS_UNMANAGED) {
		return;
	}

	JFrame window = getWindowInstanceWindow(windowType, id);
	// Now close the window...
	window.setVisible(false);
	window.dispose();

	removeWindowInstance(windowType, id);
}

/**
Close the window and set its reference to null.  If the window was never opened,
then no action is taken.  Use setWindowOpen() when opening a window to allow
the management of windows to occur.  
@param windowType the number of the window.
*/
public void closeWindow(int windowType) {
	if (getWindowStatus(windowType) == STATUS_CLOSED) {
		Message.printStatus(2, "closeWindow", "Window already closed, "
			+ "not closing again.");
		// No need to do anything...
		return;
	}

	// Get the window...
	JFrame window = getWindow(windowType);
	// Now close the window...
	window.setVisible(false);
	window.dispose();
	// Set the "soft" data...
	setWindowStatus(windowType, STATUS_CLOSED);
	setWindow(windowType, null);
	Message.printStatus(2, "closeWindow", "Window closed: " + windowType);
}

/**
Display the window of the indicated window type.  
If that window type is already displayed, bring it to the front.  
Otherwise create the window.  For more information, see the class description
docs above.
@param window_type a window of the specified type, as defined in the 
derived class.
@param editable Indicates if the data in the window should be editable.
@return the window that is displayed.
*/
public abstract JFrame displayWindow(int window_type, boolean editable);

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	_allowMultipleWindowInstances = null;
	_windowStatus = null;
	IOUtil.nullArray(_windows);
	IOUtil.nullArray(_windowInstanceInformation);
	super.finalize();
}

/**
Locates the position of an index within the internal data Vectors.
@param windowType the type of window the instance of the window is.
@param id the unique window instance identifier.
@return the int location of the window instance within the window instance 
Vector, or -1 if the window instance could not be found.
*/
private int findWindowInstancePosition(int windowType, Object id) {
	WindowManagerData data;
	int size = _windowInstanceInformation[windowType].size();
	for (int i = 0; i < size; i++){
		data = (WindowManagerData)_windowInstanceInformation[windowType].get(i);
		if (data.getID().equals(id)) {
			return i;
		}
	}
	return -1;
}

/**
Returns the status (STATUS_OPEN, STATUS_CLOSED, STATUS_UNMANAGED) of the 
specified window instance.
@param windowType the type of window the window instance is.
@param id the unique identifier of the window instance.
@return the status of the window instance (STATUS_OPEN or STATUS_CLOSED) or 
STATUS_UNMANAGED if no such window instance could be found.
*/
public int getWindowInstanceStatus(int windowType, Object id) { 
	if (!allowsMultipleWindowInstances(windowType)) {
		return getWindowStatus(windowType);
	}

	WindowManagerData data;
	int size = _windowInstanceInformation[windowType].size();
	for (int i = 0; i < size; i++){
		data = (WindowManagerData)_windowInstanceInformation[windowType].get(i);
		if (data.getID().equals(id)) {
			return data.getStatus();
		}
	}

	return STATUS_UNMANAGED;
}

/**
Returns the JFrame for a window instance.
@param windowType the type of window.
@param id the unique window instance identifier.
@return the JFrame for the window instance, or null if no matching 
window instance could be found.
*/
public JFrame getWindowInstanceWindow(int windowType, Object id) {
	WindowManagerData data;
	int size = _windowInstanceInformation[windowType].size();
	for (int i = 0; i < size; i++){
		data = (WindowManagerData)_windowInstanceInformation[windowType].get(i);
		if (data.getID().equals(id)) {
			return data.getWindow();
		}
	}
	return null;
}

/**
Returns the window at the specified position.
@param windowType the position of the window (should be one of the public fields
above).
@return the window at the specified position.
*/
public JFrame getWindow(int windowType) {
	return _windows[windowType];
}

/**
Returns the status of the window at the specified position.
@param windowType the position of the window (should be one of the public fields
above).
@return the status of the window at the specified position.
*/
public int getWindowStatus(int windowType) {
	return _windowStatus[windowType];
}

/**
Initializes arrays.
@param numWindows the number of windows the window manager will manage.
*/
private void initialize(int numWindows) {
	_windowStatus = new int[numWindows];
	_windows = new JFrame[numWindows];
	_allowMultipleWindowInstances = new boolean[numWindows];
	_windowInstanceInformation = new Vector[numWindows];

	for (int i = 0; i < numWindows; i++) {
		_windowStatus[i] = STATUS_CLOSED;
		_windows[i] = null;
		_allowMultipleWindowInstances[i] = false;
		_windowInstanceInformation[i] = null;
	}
}

/**
Returns whether a window is currently open.
@return true if the window is open, false if not.
*/
public boolean isWindowOpen(int windowType) {
	if (_windowStatus[windowType] == STATUS_OPEN) {
		return true;
	}
	return false;
}

/**
Removes a window instance from the internal data collection.
@param windowType the index of the window from which to remove the window 
instance.
@param id the identifier of the window instance to remove.
*/
private void removeWindowInstance(int windowType, Object id) {
	String routine = __CLASS + ".removeWindowInstance";
	Message.printStatus(2, routine, 
		"Remove instance of window " + windowType + " with identifier '"
		+ id + "'");
	WindowManagerData data;
	for (int i = 0; i < _windowInstanceInformation[windowType].size(); i++){
		data = (WindowManagerData)
			_windowInstanceInformation[windowType].get(i);
		if (data.getID().equals(id)) {
			_windowInstanceInformation[windowType].remove(i);
			// because the Vector is being resized within a loop,
			// decrement the counter here when a removeElemenAt
			// is done, otherwise the element that now takes
			// the place of the removed element will be skipped 
			// over.  This is also why the .size() is done within
			// the for() -- because the Vector size changes within
			// the loop.
			i--;
		}
	}

}

/**
Sets whether the specified window should allow multiple window instances 
to be open. The default is for windows to allow only a single window instance.
@param windowType the index of the window to allow multiple window instances 
for.
@param allow whether multiple window instances should be allowed (true) or not 
(false).
*/
public void setAllowMultipleWindowInstances(int windowType, boolean allow) {
	_allowMultipleWindowInstances[windowType] = allow;
	if (allow = true) {
		_windowInstanceInformation[windowType] = new Vector();
	}
	else {
		_windowInstanceInformation[windowType] = null;
	}
}

/**
Sets the window at the specified position.
@param windowType the position of the window (should be one of the public fields
above).
@param window the window to set.
*/
public void setWindow(int windowType, JFrame window) {
	_windows[windowType] = window;
}

/**
Sets up a window instance that has been opened.  This is just a convenience call
that calls setWindowInstanceWindow() and setWindowInstanceStatus(STATUS_OPEN).
@param windowType the index of the window.
@param window the JFrame of the window instance.
@param id the unique window instance identifier.
*/
public void setWindowInstanceOpen(int windowType, JFrame window, Object id) {
	setWindowInstanceWindow(windowType, window, id);
	setWindowInstanceStatus(windowType, id, STATUS_OPEN);
}

/**
Sets the status of one of a window's instances.
@param windowType the index of the window for which to set a window instance 
status.
@param id the id of the window instance to set a status for.
@param status the status to set the window instance to.
*/
public void setWindowInstanceStatus(int windowType, Object id, int status) {
	String routine = __CLASS + ".setWindowInstanceStatus";
	Message.printStatus(2, routine, 
		"Set instance status for window: " + windowType 
		+ ", instance: '" + id + "' to " + status);
	WindowManagerData data = new WindowManagerData(null, id, status);
	if (getWindowInstanceStatus(windowType, id) == STATUS_UNMANAGED) {
		Message.printStatus(2, routine, 
			"  No instance with that ID, adding a new one.");
		_windowInstanceInformation[windowType].add(data);
	}
	else {
		int i = findWindowInstancePosition(windowType, id);
		if (i == -1) {
			Message.printStatus(2, routine, 
				"  Instance not found!  Adding as a new one.");
			_windowInstanceInformation[windowType].add(data);
		}
		else {
			WindowManagerData updateData = (WindowManagerData)_windowInstanceInformation[windowType].get(i);
			Message.printStatus(2, routine, "  Instance found, updating status.");
			updateData.setStatus(status);
			_windowInstanceInformation[windowType].set(i,updateData);
		}
	}
}

/**
Sets the JFrame represented by a window instance.
@param windowType the index of the window to set the window instance JFrame for.
@param window the JFrame to set the window instance to.
@param id the unique identifier of the window instance.
*/
public void setWindowInstanceWindow(int windowType, JFrame window, Object id) {
	String routine = __CLASS + ".setWindowInstanceWindow";
	Message.printStatus(2, routine, 
		"Set instance window for: " + windowType 
		+ " with ID of '" + id + "'");
	WindowManagerData data = 
		new WindowManagerData(window, id, STATUS_CLOSED);
	if (getWindowInstanceStatus(windowType, id) == STATUS_UNMANAGED) {
		Message.printStatus(2, routine, 
			"  No instance with that ID, adding a new one.");
		_windowInstanceInformation[windowType].add(data);
	}
	else {
		int i = findWindowInstancePosition(windowType, id);
		if (i == -1) {
			Message.printStatus(2, routine, "  Instance not found!  Adding as a new one.");
			_windowInstanceInformation[windowType].add(data);
		}
		else {
			WindowManagerData updateData = (WindowManagerData)_windowInstanceInformation[windowType].get(i);
			Message.printStatus(2, routine, "  Instance found, updating window.");
			updateData.setWindow(window);
			_windowInstanceInformation[windowType].set(i,updateData);
		}
	}
}

/**
Indicate that a window is opened, and provide the JFrame corresponding to the
window.  This method should be called to allow the StateMod GUI to track
windows (so that only one copy of a data set group window is open at a time).
@param windowType Window type (see WINDOW_*).
@param window The JFrame associated with the window.
*/
public void setWindowOpen(int windowType, JFrame window) {
	setWindow(windowType, window);
	setWindowStatus(windowType, STATUS_OPEN);
	Message.printStatus(2, "setWindowOpen", "Window set open: " + windowType);
}

/**
Sets the window at the specified position to be either STATUS_OPEN or 
STATUS_CLOSED.  
@param windowType the position of the window (should be one of the public fields
above).
@param status the status of the window (STATUS_OPEN or STATUS_CLOSED)
*/
private void setWindowStatus(int windowType, int status) {
	_windowStatus[windowType] = status;
}

}
