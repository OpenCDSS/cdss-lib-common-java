// ----------------------------------------------------------------------------
// ActionFactory - A factory class for constructing Actions.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// 2006-03-30	JTS, RTi		* Added __ioInterface.
//					* Added getAllActions().
//					* Added getAction().
//					* Removed getActions().
// 2006-07-19	Michael Thiemann, RTi	* Added NotificationAction_SystemCall
//                                        to buildAction(int).
// 2006-07-27	Steven A. Malers, RTi	* Continue above addition.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

/**
This class is a factory for putting together Action objects based on 
data models read from an IO Interface.
*/
public class ActionFactory {

/**
The io interface used to read data from persistent data sources.
*/
public static AlertIOInterface __ioInterface = null;

public static Action buildAction(int type) 
throws Exception {
	Action a = null;
	
	switch (type) {
		case Action.TYPE_DUMMY:
			a = new DummyAction(null);
			break;
		case Action.TYPE_MESSAGEBOARD:
			a = new MessageAction_MessageBoard(null);
			break;
		case Action.TYPE_SYSTEMCALL:
			a = new NotificationAction_SystemCall(null);
			break;
		default:
			return null;
	}

	a.setActionType(type);
	return a;
}

/**
Builds all the Actions in the system.
@return a Vector of Actions.
*/
public static Vector getAllActions() 
throws Exception {
	ActionDataModel model = null;
	Vector models = __ioInterface.readAllActionDataModels();
	int size = models.size();
	Vector actions = new Vector();

	for (int i = 0; i < size; i++) {
		model = (ActionDataModel)models.elementAt(i);
		actions.add(getAction(model));
	}
	return actions;
}

/**
Generates an Action object for the specified Action data model. 
@param model the ActionDataModel to use for building the Action.
@return the action that was built.
@throws Exception if an error occurs.
*/
public static Action getAction(ActionDataModel model) 
throws Exception {
	if (__severityVector == null) {
		__severityVector = __ioInterface.readSeverityTypes();
	}

	int actionType = model.getActionType();
	Action action = null;
	if (actionType == Action.TYPE_DUMMY) {
		action = new DummyAction(model);
	}
	else if (actionType == Action.TYPE_MESSAGEBOARD) {
		action = new MessageAction_MessageBoard(model);
	}
	// REVISIT SAM 2006-07-27
	// Is this the right place for this?  Added to support Haiti but need to
	// review the design.
	else if (actionType == Action.TYPE_SYSTEMCALL) {
		action = new NotificationAction_SystemCall(model);
	}
	else {
		throw new Exception("Unknown action type: " + actionType);
	}

	if (actionType == -1) {
		// REVISIT (JTS - 2006-05-05)
		// later, replace the -1 with "NotificationAction" types
		// once a NotificationAction has been built!
		int[] contactIDs = model.getContactIDs();
		if (contactIDs != null && contactIDs.length > 0) {
			Contact[] contacts = new Contact[contactIDs.length];
			for (int i = 0; i < contactIDs.length; i++) {
				contacts[i] = __ioInterface.readContact(
					contactIDs[i]);
			}
//			action.setContacts(contacts);
		}
	}

	action.connectSeverity(__severityVector);
	
	return action;
}

/**
Sets the IO Interface to use for reading data from persistent data sources.
@param ioInterface the IO Interface to use.
*/
public static void setIOInterface(AlertIOInterface ioInterface) {
	__ioInterface = ioInterface;
}

private static Vector __severityVector = null;

}
