// MessageLogListener - an interface for responding to user actions from the MessageLogJFrame.

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
// MessageLogListener - an interface for responding to user actions from the
//	MessageLogJFrame.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 2005-03-10	J. Thomas Sapienza, RTi	Initial version.
//------------------------------------------------------------------------------

package RTi.Util.Message;

/**
This interface is implemented by classes that will respond to actions in the 
MessageLogJFrame.
*/
public interface MessageLogListener {

/**
Used to tell the listener to perform an action related to the specified tag.
This method is called by MessageLogJFrame when the user selects a tagged 
message and "Go To Original Item."  The listener can then respond to the
request (e.g., selecting a command or component in the main application).
@param tag the tag associated with a message, as used in messages.
*/
public void goToMessageTag(String tag);

}
