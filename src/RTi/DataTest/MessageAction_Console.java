// ----------------------------------------------------------------------------
// MessageAction_Console - class that reports on positive test results
//	by printing to the console.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-03-22	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.Util.Time.DateTime;

/**
This class is an action that will print information about positive tests
to the console.
*/
public class MessageAction_Console 
extends MessageAction {

/**
Constructor.
@param dataModel data model containing this object's values.
*/
public MessageAction_Console(ActionDataModel dataModel) 
throws Exception {
	super(dataModel);
}

/**
Does nothing.
@return true.
*/
public boolean logAction(DataTest test) {
	return true;
}

/**
Prints the top-level data result information to the console.
@param test the DataTest for which to run actions.
@return true.
*/
public boolean runAction(DateTime runDateTime, DataTest test) {
	removeOldResults(runDateTime);
	Vector results = getPositiveResults(test);
	int size = results.size();
	Vector v = null;
	for (int i = 0; i < size; i++) {
		System.out.println("----------------------------------------");
		System.out.println("                  SUCCESS");
		v = (Vector)results.elementAt(i);
		for (int j = 0; j < v.size(); j++) {
			System.out.println("" + v.elementAt(j));
		}
		
		System.out.println("----------------------------------------");
	}
	return true;
}

}
