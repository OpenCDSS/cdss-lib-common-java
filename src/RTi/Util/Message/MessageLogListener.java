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
