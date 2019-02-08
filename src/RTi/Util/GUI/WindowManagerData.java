// WindowManagerData - class to hold instance data that the WindowManager classes will manage

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

//------------------------------------------------------------------------------
// WindowManagerData - class to hold instance data that the WindowManager
//	classes will manage.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 2004-02-16	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
//------------------------------------------------------------------------------

package RTi.Util.GUI;

import javax.swing.JFrame;

/**
The WindowManagerData class holds instance data for window instances 
managed by the WindowManager.  This allows multiple instances of the same
window type to be opened with different data contents.
*/
public class WindowManagerData {

/**
The status of the instance.
*/
private int __status = WindowManager.STATUS_UNMANAGED;

/**
The window for the instance.
*/
private JFrame __window = null;

/**
The unique identifier for the instance.
*/
private Object __id = null;

/**
Constructor.  
Creates an instance and initializes with a null window reference,
null Object identifier, and window status of WindowManager.STATUS_UNMANAGED.
*/
public WindowManagerData() {}

/**
Constructor.
@param window the window for the instance.
@param id the id for the instance.
@param status the status for the instance.
*/
public WindowManagerData(JFrame window, Object id, int status) {
	__window = window;
	__id = id;
	__status = status;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__window = null;
	__id = null;
	super.finalize();
}

/**
Returns the window instance's unique identifier.
@return the window instance's unique identifier.
*/
public Object getID() {
	return __id;
}

/**
Returns the window instance's status.
@return the window instance's status.
*/
public int getStatus() {
	return __status;
}

/**
Returns the window instance's window.
@return the window instance's window.
*/
public JFrame getWindow() {
	return __window;
}

/**
Set the window instance's ID.
@param id value to put in the ID.
*/
public void setID(Object id) {
	__id = id;
}

/**
Sets the window instance's status.
@param status value to put in the status.
*/
public void setStatus(int status) {
	__status = status;
}

/**
Sets the window instance's window.
@param window window to set the instance window to.
*/
public void setWindow(JFrame window) {
	__window = window;
}

/**
Returns a String representation of the Object.
@return a String representation of the Object.
*/
public String toString() {
	return "ID: '" + __id + "'  Status: " + __status;
}

}
