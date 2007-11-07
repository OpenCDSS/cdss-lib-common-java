// ----------------------------------------------------------------------------
// NotificationAction - abstract base class for an action that notifies a user
//	somehow.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import RTi.Util.IO.IOUtil;

/**
This is an abstract base class for all actions that notify contacts of 
result information.
*/
public abstract class NotificationAction 
extends Action {

/**
For Actions that contact users, the array of Contacts that will be contacted.
*/
private Contact[] __contacts = null;

/**
Constructor.
@param dataModel the data model that holds this object's values.
*/
public NotificationAction(ActionDataModel dataModel) 
throws Exception {
	super(dataModel);
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__contacts);
	super.finalize();
}

/**
Returns the array of Contacts to be contacted by this Action.
@return the array of Contacts to be contacted by this Action.
*/
public Contact[] getContacts() {
	return __contacts;
}

/**
Sets the array of Contacts.
@param contacts the array of Contacts to set.
*/
public void setContacts(Contact[] contacts) {
	__contacts = contacts;
}

}
