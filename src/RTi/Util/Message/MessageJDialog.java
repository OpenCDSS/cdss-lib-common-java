// MessageJDialog - modal dialog that is displayed with warning level one

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
// MessageJDialog - modal dialog that is displayed with warning level one
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 02 Sep 1997	Matthew J. Rutherford,	Created function.
//		RTi
// 13 Oct 1997	MJR, RTi		Put in code to handle messages that
//					have new-line characters in the middle.
// 12 Dec 1997	Steven A. Malers, RTi	Change to 1.1 event handling.  Hopefully
//					this fixes some CRDSS problems.  Also
//					use the RTi GUI utility class.
// 16 Mar 1998	SAM, RTi		Add javadoc.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 07 Feb 2001	SAM, RTi		Try to allow enter on dialog button to
//					serve as OK - does not seem to work!
//					Previously the handler was
//					being set for the dialog itself, which
//					does not generate key events.  Fix to
//					tie to the button itself.  Other minor
//					cleanup.  Change GUI to GUIUtil.  Need
//					to change so that if the dialog string
//					is > 100 characters, break at 80 for
//					displays.  Add okClicked().
// 15 Mar 2001	SAM, RTi		In conjunction with new Message class
//					data, check for whether to use
//					"OK - Don't Show Warning JDialog" and
//					"Cancel" buttons.
// 2002-05-24	SAM, RTi		Add ability to display a Cancel button
//					and add MessageJDialogListener feature
//					so the "Cancel" button can be detected
//					in high-level code.  Remove AWTEvent
//					code since listeners are being used.
// 2002-10-11	SAM, RTi		Change ProcessManager to
//					ProcessManager1.
// 2002-10-17	AML, RTi		Change ProcessManager1 to
//					ProcessManager.
//------------------------------------------------------------------------------
// 2003-08-22	J. Thomas Sapienza, RTi	Initial Swing version.
// 2003-08-25	JTS, RTi		Corrected infinite loop happening on
//					dialog close.
// 2003-09-30	SAM, RTi		Use the title from main app if set.
// 2003-12-10	SAM, RTi		* Change the properties from
//					  "..JDialog.." back to "..Dialog.."
//					  since the intent is more general than
//					  AWT or Swing.
//					* Remove a few lines of commented code -
//					  no need to retain old font selection
//					  code.
// 2004-01-14	SAM, AML, RTi		* Display the content as a scroll pane
//					  if the number of lines exceeds a
//					  limit.
// 2004-02-04	JTS, RTi		Corrected bug in displaying multi-line
//					warnings caused by incorrect set-up of
//					GridLayout.
// 2004-03-03	JTS, RTi		Added code to wrap lines at 100 
//					characters when they are put into the
//					MessageJDialogs.
// 2005-04-05	JTS, RTi		"View Log File" now uses the new 
//					log file viewer, instead of opening
//					the log file with notepad.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package	RTi.Util.Message;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;

/**
This class provides a modal dialog for messages.  It is normally only used
from within the Message class to display warning messages for level 1 warnings
(currently the warning prefix is hard-coded here).
The dialog looks similar to the following:
<p align="center">
<img src="MessageJDialog.gif">
</p>
See also the Message.setPropValue() method to control the handling of warning
messages.
@see Message
*/
@SuppressWarnings("serial")
public class MessageJDialog extends JDialog implements ActionListener, 
KeyListener, WindowListener
{

private String _ok_no_more_button_label = null;	// JLabel for button indicating
						// whether more warnings should
						// be allowed.

private static MessageJDialogListener [] _listeners = null;
						// Listeners that want to know
						// the MessageJDialog buttons
						// that are pressed.

/**
The parent JFrame on which the dialog was opened.
*/
private JFrame __parent = null;

/**
Construct the dialog with the specified message.
@param parent JFrame from which the dialog is created.
@param message Message to display.
*/
public MessageJDialog( JFrame parent, String message )
{	super( parent, "Warning!", true );
	__parent = parent;
	if (	(JGUIUtil.getAppNameForWindows() == null) ||
		JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( "Warning!" );
	}
	else {	setTitle( JGUIUtil.getAppNameForWindows() +
		" - Warning!" );
	}

	addWindowListener(this);
	
	setResizable( false );

	message = StringUtil.wrap(message, 100);

	List<String> vec = StringUtil.breakStringList( message, "\n", StringUtil.DELIM_SKIP_BLANKS );
	JPanel pan = new JPanel();
	int size = vec.size();

	String prop_value = Message.getPropValue ( "WarningDialogScrollCutoff" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ||
	( size > 20 ) ) { 
		pan.setLayout( new GridLayout( 1, 1 ) );
		//use a JList within a JScrollPane to display text
		//instead of just making JLabels 
		JList list = new JList( new Vector(vec) );
		list.setBackground( Color.LIGHT_GRAY );
		JScrollPane pane = new JScrollPane( list );
		Dimension d = new Dimension ( 600, 200 );
		pane.setPreferredSize( d );
		pane.setMinimumSize( d );
		pane.setMaximumSize( d );
		pan.add ( pane );
	}
	else {
		pan.setLayout( new GridLayout( size, 1 ) );
		for( int i=0; i < size; i++ ){
			pan.add( new JLabel( "     " + 
			vec.get(i) + "     " ) );
		}
	}
	getContentPane().add( "Center", pan );

	// Add buttons...

	JPanel bp = new JPanel();
	bp.setLayout( new FlowLayout() );

	SimpleJButton ok_Button = new SimpleJButton( "OK", this );
	bp.add ( ok_Button );

	// Custom buttons based on Message properties...

	prop_value = null;
	prop_value = Message.getPropValue ( "WarningDialogOKNoMoreButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		_ok_no_more_button_label =
			Message.getPropValue (
			"WarningDialogOKNoMoreButtonLabel" );
		SimpleJButton okno_Button = new SimpleJButton(
			_ok_no_more_button_label, this );
		bp.add( okno_Button );
	}
	prop_value = Message.getPropValue ( "WarningDialogViewLogButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		// Only add the button if there is a log file name specified...
		if ( Message.getLogFile().length() > 0 ) {
			SimpleJButton view_log_Button =
				new SimpleJButton( "View Log", this );
			bp.add( view_log_Button );
		}
	}
	prop_value = Message.getPropValue ( "WarningDialogCancelButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		SimpleJButton cancel_Button = new SimpleJButton(
			"Cancel", this );
		bp.add( cancel_Button );
	}

	bp.addKeyListener ( this );
	getContentPane().add( "South", bp );
	pack();
        JGUIUtil.center( this );
	setVisible ( true );
}

/**
Handle action events.
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e )
{	// Check the names of the events.  These are tied to button names.
	String command = e.getActionCommand();
	if ( command.equals("Cancel") ) {
		cancelClicked();
	}
	else if ( command.equals("OK") ) {
		okClicked();
	}
	else if ( command.equals(_ok_no_more_button_label) ) {
		Message.setPropValue ( "ShowWarningDialog=false" );
		okClicked();
	}
	else if ( command.equals("View Log") ) {
		viewLogClicked();
	}
}

/**
Add a MissageJDialogListener to receive MessageJDialog events.  
Multiple listeners
can be registered.  MessageJDialog button actions will result in registered
listeners being called.
@param listener MessageJDialogListener to add.
*/
public static void addMessageJDialogListener ( MessageJDialogListener listener )
{	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener != null ) {
		// Resize the listener array...
		if ( _listeners == null ) {
			_listeners = new MessageJDialogListener[1];
			_listeners[0] = listener;
		}
		else {	// Need to resize and transfer the list...
			int size = _listeners.length;
			MessageJDialogListener [] newlisteners =
				new MessageJDialogListener[size + 1];
			for ( int i = 0; i < size; i++ ) {
				newlisteners[i] = _listeners[i];
			}
			_listeners = newlisteners;
			_listeners[size] = listener;
			newlisteners = null;
		}
	}
}

/**
Handle event to close the dialog.
*/
private void cancelClicked ()
{	setVisible ( false );
	// If any MessageJDialogListeners are registered, call the
	// messageJDialogAction() method.
	if ( _listeners != null ) {
		for ( int ilist = 0; ilist< _listeners.length; ilist++ ) {
			_listeners[ilist].messageJDialogAction ( "Cancel" );
		}
	}
	dispose();
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	_ok_no_more_button_label = null;
	super.finalize();
}

/**
Handle key events.
@param e KeyEvent to handle.
*/
public void keyPressed ( KeyEvent e )
{	// Handle a return as if OK is pressed...
	if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
	Message.printStatus ( 1, "", "return over OK" );
		okClicked();
	}
}

public void keyReleased ( KeyEvent e )
{
}

public void keyTyped ( KeyEvent e )
{
	// Just worry about what is pressed.
}

/**
Handle event to close the dialog.
*/
private void okClicked () {
	setVisible ( false );
	dispose();
}

public void windowActivated(WindowEvent e) {}
public void windowClosed(WindowEvent e) {}
public void windowClosing(WindowEvent e) {}
public void windowDeactivated(WindowEvent e) {}
public void windowDeiconified(WindowEvent e) {}
public void windowIconified(WindowEvent e) {}
public void windowOpened(WindowEvent e) {}

/**
Display the log file.
*/
private void viewLogClicked ()
{	String logfile = Message.getLogFile();
	if (logfile.equals("")) {
		return;
	}

	try {
		PropList p = new PropList("");
		p.set("NumberSummaryListLines=10");
		p.set("NumberLogLines=25");
		p.set("ShowSummaryList=true");
		MessageLogJDialog logDialog 
			= new MessageLogJDialog(__parent, true, p);
		logDialog.processLogFile(Message.getLogFile());
		logDialog.setVisible(true);
	}
	catch (Exception ex) {
		String routine = "MessageJDialog.viewLogClicked";
		Message.printWarning(1, routine, "Unable to view log file \"" 
			+ Message.getLogFile() + "\"");
		Message.printWarning(2, routine, ex);
	}
}

} // end MessageJDialog
