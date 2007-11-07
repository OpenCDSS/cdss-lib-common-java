//------------------------------------------------------------------------------
// MessageLogJDialog - a GUI for displaying message log text 
// 	in order to quickly summarize and move through the log file.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 2005-03-10	J. Thomas Sapienza, RTi	Initial version.
// 2005-03-17	JTS, RTi		Incorporated a new way of handling
//					tags.
// 2005-03-22	JTS, RTi		Modified to use a worksheet as an 
//					alternate to the JTextArea.
// 2005-03-23	JTS, RTi		* Added the number of lines to the
//					  information printed by the summary 
//					  list panel.
// 					* Summary information and the log file
//					  can now be printed.
//					* The summary is turned off if tags
//					  are turned off in Message.
//					* Worksheet now uses Courier font.
// 2005-03-25	JTS, RTi		* The popup menu to go to a command is
//					  now only enabled if a command with a
//					  tag is selected.
//					* Warnings at level 1 are now included
//					  in the summary list.
// 2005-04-05	JTS, RTi		Converted from a JFrame to a JDialog.
// 2005-05-03	JTS, RTi		* Changed MutableJList to SimpleJList.
//					* The messages at the top were not
//					  being properly filtered according to 
//					  message level when a file was opened.
//					  This has been corrected.
// 2005-05-26	JTS, RTi		Removed all message log code and
//					moved it to MessageLogJPanel.  This
//					class is now just a shell class for
//					displaying that panel.
//------------------------------------------------------------------------------

package RTi.Util.Message;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;

import RTi.Util.IO.PropList;

/**
The MessageLogJDialog class provides a graphical user interface for 
viewing, summarizing, and navigating a log file.  For complete functionality, 
a call to the following should be made in the application code:</p>
<pre>
	Message.setPropValue("ShowMessageLevel=true");
	Message.setPropValue("ShowMessageTag=true");
</pre>
<p>The first call above results in message levels being shown in log file 
messages, using square brackets.  The second call results in message tags 
being shown in log file messages, using angle brackets.  The following 
example illustrates the syntax of messages:</p>
<pre>
	Warning[level]<tag>(routine):  Body of message...
</pre>
<p>The message levels are interpreted by MessageLogJDialog, resulting in 
summary messages being listed in an area at the top of the viewer.  By 
default, warning level 1 and 2 messages are listed in the summary, to 
facilitate troubleshooting.  The summary messages are associated with the 
messages in the full log display, providing a popup menu and simplifying 
navigation of the full message log.</p>
<p> The tags are also interpreted by MessageLogJDialog to allow 
navigation in an application.  Messages with tags are printed using the 
Message.print*(..., String Tag,...) methods.  If no tag is provided, then 
messages will not include the <tag> information.  If the tag is provided, then 
the MessageLogJDialog is able to notify MessageLogListener instances 
when a tagged message is selected and the "Go to..." is chosen.</p>
<p>
An example of using the expanded Message class capability in an 
application that performs clearly defined processing steps is as follows:
</p>
<ol>
<li> Within each process, print important warning messages at level 2, 
where the tag is the command/process count + "," + the warning count (e.g., 
"1,1", "1,2").  Throw an exception if one or more important warnings are 
generated while processing.</li>
<li> For the controller, if an exception is caught, print a warning at level 1 
indicating that a process/command resulted in a warning.  Use a tag that 
indicates the commaand/process count (e.g., "ProcessCommands,1").</li>
<li> Use the <code>Message.addMessageLogListener()</code> method 
from the application code when initializing the application interface.  When a 
user selects a message in the log file summary and then selects the popup 
"Go to..." menu, the application method will receive the selected tag.
The command/process count can then be processed and an appropriate 
action taken (e.g., the command can be highlighted).</li> 
</ol>
*/
public class MessageLogJDialog
extends JDialog
implements WindowListener {

/**
The panel that does all the work of displaying the log file.
*/
private MessageLogJPanel __messageLogJPanel = null;

/**
Constructor.
@param parent the parent JDialog on which this dialog should be opened.
Cannot be null.
@param modal if true, this dialog will be opened as a modal dialog.
@param props the PropList that controls the dialog's properties.  See the
first constructor for valid properties.
*/
public MessageLogJDialog(JDialog parent, boolean modal, PropList props) 
throws Exception {
	super(parent, modal);
	setupGUI(props);
}

/**
Constructor.
@param parent the parent JFrame on which this dialog should be opened.
Cannot be null.
@param modal if true, this dialog will be opened as a modal dialog.
@param props the PropList that controls the dialog's properties.  See the
first constructor for valid properties.
*/
public MessageLogJDialog(JFrame parent, boolean modal, PropList props) 
throws Exception {
	super(parent, modal);
	setupGUI(props);
}

/**
Used by the MessageLogJPanel to add its summary panel to the dialog.
@param summaryJPanel the summary panel to add.
*/
protected void add(JPanel summaryJPanel) {
	getContentPane().add("North", summaryJPanel);
	invalidate();
	validate();
	repaint();	
	summaryJPanel.repaint();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__messageLogJPanel = null;
	super.finalize();
}

/**
Processes a log file in order to display its text and summary
information in the GUI.  This method is called internally and can be called
by external code to reprocess a log file.  This method will re-read the 
MessageLogListeners that were set in Message.addMessageLogListener().
@param filename the name of the file to read and process.
@throws Exception if the file cannot be found or there is an error reading
from the file.
*/
public void processLogFile(String filename) 
throws Exception {
	__messageLogJPanel.processLogFile(filename);
}

/**
Used by the MessageLogJDialog to remove its summary panel from the dialog.
@param summaryJPanel the summary panel to be removed.
*/
protected void remove(JPanel summaryJPanel) {
	getContentPane().remove(summaryJPanel);
}

/**
Sets up the GUI.
*/
private void setupGUI(PropList props) 
throws Exception {
	addWindowListener(this);

	__messageLogJPanel = new MessageLogJPanel(this, props);
	getContentPane().add("Center", __messageLogJPanel);
	getContentPane().add("South", __messageLogJPanel.getButtonJPanel());

	if (JGUIUtil.getAppNameForWindows() == null) {
		setTitle("Message Log");
	}
	else {
		setTitle(JGUIUtil.getAppNameForWindows() + " - Message Log");
	}

//	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	
	pack();
	setSize(getWidth() + 200, getHeight());
		// pack() packs in everything pretty well, but the overall
		// size is just a little small for the worksheet to display
		// nicely.
	JGUIUtil.center(this);
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

/**
Does nothing.
*/
public void windowClosing(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

}
