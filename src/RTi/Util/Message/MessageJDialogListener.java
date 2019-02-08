// MessageJDialogListener - listener to communicate between MessageJDialog and other components

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
// MessageJDialogListener - listener to communicate between MessageJDialog and
//			other components
// ----------------------------------------------------------------------------
// History:
//
// 2002-05-26	Steven A. Malers, RTi	Implemented code.
// ----------------------------------------------------------------------------
// 2003-08-22	J. Thomas Sapienza, RTi	Initial Swing version.
// ----------------------------------------------------------------------------

package RTi.Util.Message;

/**
This interface should be used to capture events from a MessageJDialog object.
Currently the listeners in the MessageJDialog (added using addMessageListener)
are static.
*/
public abstract interface MessageJDialogListener
{
/**
MessageJDialog will call this method if the Cancel button is enabled and is
pressed to close the dialog.  Use, for example, to cancel out of a long loop
of actions when a Cancel is acknowledged.
@param command Action command that has occurred (currently only "Cancel" is
notified).
*/
public abstract void messageJDialogAction ( String command );

} // End MessageJDialogListener
