// ----------------------------------------------------------------------------
// NotificationAction_SystemCall - class that perofrms a system call as the
// response to at least one positive data test result
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-07-19	Michael Thiemann, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.Vector;

import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
This class is an action that will make a system call when a positive action
occurs.
*/
public class NotificationAction_SystemCall extends NotificationAction
{

/**
Constructor.
@param dataModel data model containing this object's values.
*/
public NotificationAction_SystemCall(ActionDataModel dataModel) 
throws Exception
{
	super(dataModel);
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable
{
	super.finalize();
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
@return false if there was an error adding nodes to the message board tree.
True otherwise.
*/
public boolean runAction(DateTime runDateTime, DataTest test) {
	String mthd = "NotificationAction_SystemCall.runAction";
	removeOldResults(runDateTime);
	Vector results = getPositiveResults(test);
	int resSize = results.size();
	
	Message.printStatus(1, "", "Positive Results: " + resSize);
	
	if( resSize > 0 ) {
		// REVISIT SAM 2006-07-27
		// properties should be protected in base class, not private.
		PropList properties = getProperties();
		String commandPrompt = properties.getValue("COMMAND");
		Vector ArgsList = StringUtil.breakStringList (
			commandPrompt, " ", StringUtil.DELIM_SKIP_BLANKS);
		int size = ArgsList.size();
		
		if( size == 0 ) {
			Message.printWarning(2, mthd,
			"Empty command prompt, will not do a system call");
			return false;
		}
		
		String[] args = new String[size];	
		for ( int i = 0; i < size; i++ ) {
			args[i] = (String)ArgsList.elementAt(i);
		}
	
		String commandLine = "";
		for (int i = 0; i < size; i++) {
			commandLine += args[i] + " ";
		}
		Message.printStatus(1, "", "Command line: " + commandLine);
		ProcessManager pm = new ProcessManager(args);
		pm.run();
	}
	
	return true;
}

}
