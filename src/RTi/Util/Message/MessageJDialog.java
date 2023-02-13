// MessageJDialog - modal dialog that is displayed with warning level one

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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
This class provides a modal dialog for messages.
It is normally only used from within the Message class to display warning messages for level 1 warnings
(currently the warning prefix is hard-coded here).
The dialog looks similar to the following:
<p align="center">
<img src="MessageJDialog.gif">
</p>
See also the Message.setPropValue() method to control the handling of warning messages.
@see Message
*/
@SuppressWarnings("serial")
public class MessageJDialog extends JDialog implements ActionListener, KeyListener, WindowListener
{

/**
 * JLabel for button indicating whether more warnings should be allowed.
 */
private String _ok_no_more_button_label = null;

/**
 *  Listeners that want to know the MessageJDialog buttons that are pressed.
 */
private static MessageJDialogListener [] _listeners = null;

/**
The parent JFrame on which the dialog was opened.
*/
private JFrame __parent = null;

/**
Construct the dialog with the specified message.
@param parent JFrame from which the dialog is created.
@param message Message to display.
*/
public MessageJDialog( JFrame parent, String message ) {
	super( parent, "Warning!", true );
	__parent = parent;
	if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( "Warning!" );
	}
	else {
		setTitle( JGUIUtil.getAppNameForWindows() + " - Warning!" );
	}

	addWindowListener(this);
	
	setResizable( false );

	message = StringUtil.wrap(message, 100);

	List<String> vec = StringUtil.breakStringList( message, "\n", StringUtil.DELIM_SKIP_BLANKS );
	JPanel pan = new JPanel();
	int size = vec.size();

	String prop_value = Message.getPropValue ( "WarningDialogScrollCutoff" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) || ( size > 20 ) ) {
		pan.setLayout( new GridLayout( 1, 1 ) );
		//use a JList within a JScrollPane to display text instead of just making JLabels.
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JList<List<String>> list = new JList( new Vector<String>(vec) );
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
			pan.add( new JLabel( "     " + vec.get(i) + "     " ) );
		}
	}
	getContentPane().add( "Center", pan );

	// Add buttons.

	JPanel bp = new JPanel();
	bp.setLayout( new FlowLayout() );

	SimpleJButton ok_Button = new SimpleJButton( "OK", this );
	bp.add ( ok_Button );

	// Custom buttons based on Message properties.

	prop_value = null;
	prop_value = Message.getPropValue ( "WarningDialogOKNoMoreButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		_ok_no_more_button_label = Message.getPropValue ( "WarningDialogOKNoMoreButtonLabel" );
		SimpleJButton okno_Button = new SimpleJButton(
			_ok_no_more_button_label, this );
		bp.add( okno_Button );
	}
	prop_value = Message.getPropValue ( "WarningDialogViewLogButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		// Only add the button if there is a log file name specified.
		if ( Message.getLogFile().length() > 0 ) {
			SimpleJButton view_log_Button = new SimpleJButton( "View Log", this );
			bp.add( view_log_Button );
		}
	}
	prop_value = Message.getPropValue ( "WarningDialogCancelButton" );
	if ( (prop_value != null) && (prop_value.equalsIgnoreCase("true")) ) {
		SimpleJButton cancel_Button = new SimpleJButton( "Cancel", this );
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
public void actionPerformed ( ActionEvent e ) {
	// Check the names of the events.  These are tied to button names.
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
Multiple listeners can be registered.
MessageJDialog button actions will result in registered listeners being called.
@param listener MessageJDialogListener to add.
*/
public static void addMessageJDialogListener ( MessageJDialogListener listener ) {
	// Use arrays to make a little simpler than Vectors to use later.
	if ( listener != null ) {
		// Resize the listener array.
		if ( _listeners == null ) {
			_listeners = new MessageJDialogListener[1];
			_listeners[0] = listener;
		}
		else {
			// Need to resize and transfer the list.
			int size = _listeners.length;
			MessageJDialogListener [] newlisteners = new MessageJDialogListener[size + 1];
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
private void cancelClicked () {
	setVisible ( false );
	// If any MessageJDialogListeners are registered, call the messageJDialogAction() method.
	if ( _listeners != null ) {
		for ( int ilist = 0; ilist< _listeners.length; ilist++ ) {
			_listeners[ilist].messageJDialogAction ( "Cancel" );
		}
	}
	dispose();
}

/**
Handle key events.
@param e KeyEvent to handle.
*/
public void keyPressed ( KeyEvent e ) {
	// Handle a return as if OK is pressed.
	if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
		Message.printStatus ( 1, "", "return over OK" );
		okClicked();
	}
}

public void keyReleased ( KeyEvent e ) {
}

public void keyTyped ( KeyEvent e ) {
	// Just worry about what is pressed.
}

/**
Handle event to close the dialog.
*/
private void okClicked () {
	setVisible ( false );
	dispose();
}

public void windowActivated(WindowEvent e) {
}

public void windowClosed(WindowEvent e) {
}

public void windowClosing(WindowEvent e) {
}

public void windowDeactivated(WindowEvent e) {
}

public void windowDeiconified(WindowEvent e) {
}

public void windowIconified(WindowEvent e) {
}

public void windowOpened(WindowEvent e) {
}

/**
Display the log file.
*/
private void viewLogClicked () {
	String logfile = Message.getLogFile();
	if (logfile.equals("")) {
		return;
	}

	try {
		PropList p = new PropList("");
		p.set("NumberSummaryListLines=10");
		p.set("NumberLogLines=25");
		p.set("ShowSummaryList=true");
		MessageLogJDialog logDialog = new MessageLogJDialog(__parent, true, p);
		logDialog.processLogFile(Message.getLogFile());
		logDialog.setVisible(true);
	}
	catch (Exception ex) {
		String routine = "MessageJDialog.viewLogClicked";
		Message.printWarning(1, routine, "Unable to view log file \"" + Message.getLogFile() + "\"");
		Message.printWarning(2, routine, ex);
	}
}

}