// ----------------------------------------------------------------------------
// DummyAction - 
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-04-10	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.List;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

/**
A dummy action that is being used during testing.
*/
public class DummyAction 
extends Action {

/**
Constructor.
@param dataModel data model containing this object's values.
*/
public DummyAction(ActionDataModel dataModel) 
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
@param test the test for which to run the action.
@return true.
*/
public boolean runAction(DateTime runDateTime, DataTest test) {
	removeOldResults(runDateTime);
	List results = getPositiveResults(test);
	int size = results.size();
	List v = null;
	for (int i = 0; i < size; i++) {
		print("----------------------------------------");
		v = (List)results.get(i);
		DataTestResult res = (DataTestResult)v.get(0);
		print("" + runDateTime);
		print("" + res.getTestTime());
		print("   " + res.getMessage());
		print("----------------------------------------");
	}
	return true;
}

/**
Just for testing.  Will be removed in the future.
*/
public void print(String s) {
	Message.printStatus(1, "", s);
	System.out.println(s);
}

}
