// TextResponseJDialog - provides a pop-up dialog allowing user to enter a text response

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

// ----------------------------------------------------------------------------
// TextResponseJDialog - provides a pop-up dialog allowing user to enter
//	a text response.
// ----------------------------------------------------------------------------
//  Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
//  Notes:
//	(1)This GUI provides a Dialog which expects
//	a user reponse.
//	(2)flags may be passed through the constructor which
//	determine what sort of dialog will be visible. The
//	flags supported are: OK, OK_CANCEL
//	(3)This object should be instantiated as follows:
//	String x = new TextResponseJDialog( Frame parent, 
//		String label, int flag ).response()
//	where processing is halted until a reponse occures.
//	(4)The user response is returned through the response()
//	function.
//	(5)While the user is able to specify the mode in which the GUI
//	is initially created, the only way to know if the user clicked on
//	"Cancel" is if the response in (4) is null.  Otherwise, the response
//	will contain the text contained in the text field.
// ----------------------------------------------------------------------------
// History: 
//
// 2003-05-11	SAM, RTi		Copy TextResponseDialog and update for
//					Swing.
// 2004-05-03	J. Thomas Sapienza, RTi	Added constructor that allows specifying
//					the text to initially appear in the
//					JTextField.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.util.List;

import RTi.Util.String.StringUtil;

@SuppressWarnings("serial")
public class TextResponseJDialog extends JDialog
implements ActionListener, KeyListener, WindowListener {

private JButton		_cancel_Button,	// Cancel Button
			_ok_Button;	// Ok Button
private JTextField	_textResponse;	// text response from user
private static String	_frame_title;	// Frame Title String
private int		_mode,		// mode in which class was
					// instantiated
			_response;	// button press

/**
TextResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param label Label to display in the GUI.
*/
public TextResponseJDialog ( JFrame parent, String label )
{	// Call the full version with no title and ok Button
	super ( parent, true );
	initialize ( parent, null, label, ResponseJDialog.OK, "");
}

/**
TextResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param label Label to display in the GUI.
@param mode mode in which this gui is to be used (i.e., OK, OK_CANCEL)
process different types of yes reponses from the calling form.
*/
public TextResponseJDialog ( JFrame parent, String label, int mode )
{	// Call the full version with no title...
	super ( parent, true );
	initialize ( parent, null, label, mode, "");
}

/**
TextResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param mode mode in which this gui is to be used (i.e., OK, OK_CANCEL)
process different types of yes reponses from the calling form.
*/
public TextResponseJDialog (	JFrame parent, String title, String label,
				int mode )
{	super ( parent, true );
	initialize ( parent, title, label, mode, "" );
}

/**
TextResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param text the text to initially put in the text field in the dialog.
@param mode mode in which this gui is to be used (i.e., OK, OK_CANCEL)
process different types of yes reponses from the calling form.
*/
public TextResponseJDialog(JFrame parent, String title, String label,
String text, int mode) {
	super(parent, true);
	initialize(parent, title, label, mode, text);
}

/**
Responds to ActionEvents
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	String s = event.getActionCommand();
	if ( s.equals("Cancel") ) {
		_response = ResponseJDialog.CANCEL;
	}
	else if ( s.equals("OK") ) {
		_response = ResponseJDialog.OK;
	}
	response();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	_cancel_Button = null;
	_ok_Button = null;
	_textResponse = null;
	super.finalize();
}

/**
Instantiates the GUI components
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param mode mode in which this gui is to be used (i.e., OK, OK_CANCEL)
process different types of yes reponses from the calling form.
@param text the text to seed the textfield with.
*/
private void initialize ( JFrame parent, String title, String label, int mode,
String text ) {
	_mode = mode;

	addWindowListener( this );

        // North Panel
        JPanel north_Panel = new JPanel();
  
	// Split the text based on the new-line delimiter (we use \n, not the
	// platform's separator!

	List<String> vec = StringUtil.breakStringList ( label, "\n", 0 );

	if ( vec != null ) {
		// Add each string...
		for ( int i = 0; i < vec.size(); i++ ) {
        		north_Panel.add( new JLabel( "    " + vec.get(i) +
			"     " ) );
		}
	}
	_textResponse = new JTextField(20);
	_textResponse.addKeyListener ( this );
        north_Panel.add( _textResponse);
	_textResponse.setText(text);

        north_Panel.setLayout(new GridLayout ( vec.size()+1, 1));
        getContentPane().add("North", north_Panel);

	// Now add the buttons...

        // South Panel
        JPanel south_Panel = new JPanel();
        south_Panel.setLayout( new BorderLayout() );
        getContentPane().add( "South", south_Panel );
        
        // South Panel: North
        JPanel southNorth_Panel = new JPanel();
        southNorth_Panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        south_Panel.add("North", southNorth_Panel);

	if ( (_mode & ResponseJDialog.CANCEL) != 0 ) {
		_cancel_Button = new JButton("Cancel");
		_cancel_Button.addActionListener( this );
	}

	if ( (_mode & ResponseJDialog.OK) != 0 ) {
        	_ok_Button = new JButton("OK");
		_ok_Button.addActionListener( this );
	}

	// show the appropriate buttons depending upon
	// the selected mode.
	if ( (_mode & ResponseJDialog.OK) != 0 ) {
	        southNorth_Panel.add(_ok_Button);
	}
	if ( (_mode & ResponseJDialog.CANCEL) != 0 ) {
	        southNorth_Panel.add(_cancel_Button);
	}

        // frame settings
	if ( title != null ) {
		setTitle ( title );
	}
	else if ( _frame_title != null ) {
		setTitle( _frame_title );
	}
	// Dialogs do no need to be resizable...
	setResizable ( false );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		// Enter key has same effect as "OK" button...
		_response = ResponseJDialog.OK;
		response();
	}
}

public void keyReleased ( KeyEvent event ) {;}
public void keyTyped ( KeyEvent event ) {;}

/**
Return the user response.
@return the dialog response string.
*/
public String response()
{	setVisible( false );
	dispose();
	if ( _response == ResponseJDialog.CANCEL ) {
		return null;
	}
	else {	return _textResponse.getText();
	}
}

/**
This function sets the JFrame Title variable that is used
for all instances of this class.
@param title Frame title
*/
public static void setFrameTitle ( String title )
{	if ( title != null )  {
		_frame_title = title;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	_response = ResponseJDialog.CANCEL;
	response();
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
