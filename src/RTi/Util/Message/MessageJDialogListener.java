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
