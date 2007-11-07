// ----------------------------------------------------------------------------
// ResponseJDialog - provides a pop-up dialog
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 16 Dec 1997	Darrell L. Gillmeister, Created initial class description.
//		RTi
// 23 Jan 1998	Steven A. Malers, RTi	Change so that the flag indicating the
//					buttons to use is a mask so that it is
//					easier to add to.  Also use the same
//					flags for response values (don't need
//					to treat as a mask.  The input mask
//					should work with legacy code but the
//					return will not.
//					Similar to the MessageDialog class,
//					accept newlines in the text.
// 13 Feb 1998	SAM, RTi		Overload to take a title.
// 09 Apr 1998  DLG, RTi		Overload to default to OK Button.
// 07 May 1998  DLG, RTi		Added javadoc comments.
// 07 Jan 2001	SAM, RTi		Change GUI to GUIUtil.  Change import *
//					to specific imports.
// 2001-11-01	SAM, RTi		Track down problem where title is not
//					handled properly with HelpAboutDialog.
//					The issue is that HBGUIApp gets used
//					in TSTool and the HBGUIApp constructor
//					calls the setFrameTitle() method to
//					use CWRAT.  For now just make sure the
//					HelpAboutDialog() uses a constructor
//					that sets the title.  Add finalize(),
//					cleanup memory management and update
//					javadoc.
// 2001-11-21	SAM, RTi		Overload constructor to take alignment
//					flag.
// 2002-01-16	SAM, RTi		Remove _parent Frame data member since
//					it is never used.  If the Frame that is
//					passed in is null, create a temporary
//					one.
// ============================================================================
// 2002-10-24	SAM, RTi		Copy ResponseDialog to this class and
//					update to use Swing.
// 2003-09-03	J. Thomas Sapienza, RTi	Corrected a misspelling that was 
//					resulting in "Cancel" buttons returning
//					a response of 0, which doesn't match
//					Yes, No, Cancel or OK.
// 2003-09-17	JTS, RTi		* Changed String references like 
//					  "Yes", "No", etc to be coded in a
//					  single place up front.
//					* Added the key listeners so that 
//					  the dialog behaves more like
//					  Microsoft-standard dialogs.
// 2003-10-15	SAM, RTi		* Overload constructors to take JDialog.
//
// 2004-01-14	SAM, AML, RTi		* Display the content as a scroll pane
//					  if the number of lines exceeds a
//					  limit of 20.
// 2005-11-16	JTS, RTi		Changed GUIUtil to JGUIUtil.
// 2006-05-18	JTS, RTi		Added some modeless dialogs for use
//					in ESRI Java code for AN.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.util.Vector;

import RTi.Util.String.StringUtil;

/**
ResponseJDialog provides an area for a message body and expects a user reponse
given a choice of buttons.  Flags can be passed to the constructor that
determine what sort of dialog will be visible.  The flags supported are:
YES_NO_CANCEL, YES_NO, OK.
A ResponseJDialog should be instantiated as follows:
<pre>
int x = new ResponseJDialog( JFrame parent, 
		String label, int flag ).response()
</pre>
where processing is halted until a reponse occures (the dialog is modal).
The user response is returned through the response() method.
*/
public class ResponseJDialog 
extends JDialog 
implements ActionListener, KeyListener, WindowListener {

private String
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_NO = "No",
	__BUTTON_OK = "OK",
	__BUTTON_YES = "Yes";

private SimpleJButton	__yes_JButton,	// Yes Button				
                       	__no_JButton,  	// No Button
			__cancel_JButton,// Cancel Button
			__ok_JButton;	// Ok Button
private int		__mode,		// mode in which class was
					// instantiated
			__response;	// user selected reponse as
					// identified by the return status
					// public final statics below

/**
ResponseJDialog modes, which can be ORed together.
*/
public final static int		YES	= 0x1,
				NO	= 0x2,
				OK	= 0x4,
				CANCEL	= 0x8;

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
*/
public ResponseJDialog ( JFrame parent, String label )
{	// Call the full version with no title and ok Button
	super(parent, true);
	initialize ( null, label, OK, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JDialog class instantiating this class.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
*/
public ResponseJDialog ( JDialog parent, String label )
{	// Call the full version with no title and ok Button
	super(parent, true);
	initialize ( null, label, OK, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param modal whether the dialog is modal or not.
*/
public ResponseJDialog ( JFrame parent, String label, boolean modal )
{	// Call the full version with no title and ok Button
	super(parent, modal);
	initialize ( null, label, OK, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor for use by the HelpAboutJDialog.
@param parent JDialog class instantiating this class.
@param modal whether it will be a modal dialog or not.
*/
protected ResponseJDialog(JFrame parent, boolean modal) {
	super(parent, modal);
}

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param label Label to display in the dialog.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
*/
public ResponseJDialog ( JFrame parent, String label, int mode )
{	// Call the full version with no title...
	super(parent, true);
	initialize ( null, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param label Label to display in the dialog.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param modal whether the dialog is modal or not.
*/
public ResponseJDialog ( JFrame parent, String label, int mode, boolean modal )
{	// Call the full version with no title...
	super(parent, modal);
	initialize ( null, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JDialog class instantiating this class.
@param label Label to display in the dialog.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
*/
public ResponseJDialog ( JDialog parent, String label, int mode )
{	// Call the full version with no title...
	super(parent, true);
	initialize ( null, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
*/
public ResponseJDialog ( JFrame parent, String title, String label, int mode )
{	super(parent, true);
	initialize ( title, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JFrame class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param modal whether the dialog is modal or not.
*/
public ResponseJDialog ( JFrame parent, String title, String label, int mode,
boolean modal)
{	super(parent, modal);
	initialize ( title, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent JDialog class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
*/
public ResponseJDialog ( JDialog parent, String title, String label, int mode )
{	super(parent, true);
	initialize ( title, label, mode, GridBagConstraints.WEST );
}

/**
ResponseJDialog constructor.
@param parent Frame class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param alignment Specify GridBagConstraints.CENTER to center the text lines.
*/
public ResponseJDialog ( JFrame parent, String title, String label, int mode,
			int alignment )
{	super(parent, true);
	initialize ( title, label, mode, alignment );
}

/**
ResponseJDialog constructor.
@param parent Frame class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param alignment Specify GridBagConstraints.CENTER to center the text lines.
@param modal whether the dialog is modal or not.
*/
public ResponseJDialog ( JFrame parent, String title, String label, int mode,
			int alignment, boolean modal )
{	super(parent, modal);
	initialize ( title, label, mode, alignment );
}

/**
ResponseJDialog constructor.
@param parent JDialog class instantiating this class.
@param title Dialog title.
@param label Label to display in the GUI.  Newlines result in line breaks in
the dialog.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param alignment Specify GridBagConstraints.CENTER to center the text lines.
*/
public ResponseJDialog ( JDialog parent, String title, String label, int mode,
			int alignment )
{	super(parent, true);
	initialize ( title, label, mode, alignment );
}



/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	String s = event.getActionCommand();
	if ( s.equals(__BUTTON_YES) ) {
		__response = YES;
	}	
	else if ( s.equals(__BUTTON_NO) ) {
		__response = NO;
	}
	else if ( s.equals(__BUTTON_CANCEL) ) {
		__response = CANCEL;
	}
	else if ( s.equals(__BUTTON_OK) ) {
		__response = OK;
	}
	s = null;
	response();
}

/**
Clean up for garbage collection.
@exception Throwable if an error.
*/
protected void finalize()
throws Throwable
{	__yes_JButton = null;
	__no_JButton = null;
	__cancel_JButton = null;
	__ok_JButton = null;
	super.finalize();
}

/**
Instantiate the dialog components
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param mode mode in which this gui is to be used (i.e., YES|NO|CANCEL)
process different types of yes reponses from the calling form.
@param alignment Specify GridBagConstraints.CENTER to center the text lines.
*/
private void initialize ( String title, String label, int mode, int alignment )
{	__mode = mode;
	addWindowListener( this );

	// Split the text based on the new-line delimiter (we use \n, not the
	// platform's separator!
	Vector vec = StringUtil.breakStringList ( label, "\n", 0 );
	int size = vec.size();
        // North Panel
	JPanel north_JPanel = new JPanel();
	if ( vec != null ) {
		if ( alignment == GridBagConstraints.CENTER ) {
			Insets insets = new Insets ( 1, 5, 1, 5 );
			// New approach where alignment can be CENTER (because
			// used by the HelpAboutDialog)...
        		north_JPanel.setLayout(new GridBagLayout () );
			if ( size > 20 ) {
				//add message String to a JList
				//that is within a JScrollPane
				JList list = new JList( vec );
				list.setBackground(Color.LIGHT_GRAY);
				JScrollPane pane = new JScrollPane( list );
				Dimension d = new Dimension ( 400, 200 );
				pane.setPreferredSize( d );
				pane.setMinimumSize( d );
				pane.setMaximumSize( d );

				//add JScrollPane to JPanel
        			JGUIUtil.addComponent(north_JPanel,
				pane, 0,0,1,1,0,0,insets,
				GridBagConstraints.NONE, alignment );
			}
			else {
				// Add each string as a JLabel...
				for ( int i = 0; i < size; i++ ) {
        				JGUIUtil.addComponent(north_JPanel,
					new JLabel( (String)vec.elementAt(i)),
					0,i,1,1,0,0,insets,
					GridBagConstraints.NONE, alignment );
				}
			}
			insets = null;
		}
		else {	// This is the layout that was used previously.  If the
			// above works out OK with spacing, etc., might use
			// GridBagLayout always.
        		//north_JPanel.setLayout(new GridLayout ( vec.size(), 1));
			if (size > 20 ) {
        			north_JPanel.setLayout(new GridLayout( 1, 1));
				//add message String to a JList
				//that is within a JScrollPane
				JList list = new JList( vec );
				list.setBackground(Color.LIGHT_GRAY);
				JScrollPane pane = new JScrollPane( list );
				Dimension d = new Dimension ( 600, 200 );
				pane.setPreferredSize( d );
				pane.setMinimumSize( d );
				pane.setMaximumSize( d );

				//add JScrollPane to JPanel
        			north_JPanel.add( pane );
			}
			else {
        			north_JPanel.setLayout( 
				new GridLayout ( vec.size(), 1));
				// Add each string...
				for ( int i = 0; i < size; i++ ) {
        				north_JPanel.add( new JLabel( "    " +
					vec.elementAt(i) + "     " ) );
				}
			}
		}
	}

        getContentPane().add("North", north_JPanel);

	// Now add the buttons...

        // South Panel
	JPanel south_JPanel = new JPanel();
        south_JPanel.setLayout( new BorderLayout() );
        getContentPane().add( "South", south_JPanel );
        
        // South Panel: North
        JPanel southNorth_JPanel = new JPanel();
        southNorth_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        south_JPanel.add("North", southNorth_JPanel);

	if ( (__mode & YES) != 0 ) {
		// Add a Yes button...
        	__yes_JButton = new SimpleJButton(__BUTTON_YES, this);
		__yes_JButton.addKeyListener(this);
	        southNorth_JPanel.add(__yes_JButton);
	}

	if ( (__mode & NO) != 0 ) {
        	__no_JButton = new SimpleJButton(__BUTTON_NO, this);
		__no_JButton.addKeyListener(this);
        	southNorth_JPanel.add(__no_JButton);   
	}

	if ( (__mode & OK) != 0 ) {
        	__ok_JButton = new SimpleJButton(__BUTTON_OK, this);
		__ok_JButton.addKeyListener(this);
	        southNorth_JPanel.add(__ok_JButton);
	}

	if ( (__mode & CANCEL) != 0 ) {
		__cancel_JButton = new SimpleJButton(__BUTTON_CANCEL, this);
		__cancel_JButton.addKeyListener(this);
	        southNorth_JPanel.add(__cancel_JButton);
	}

	if ( title != null ) {
		setTitle ( title );
	}
	// Dialogs do no need to be resizable...
	setResizable ( false );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
	// Clean up...
        north_JPanel = null;
        south_JPanel = null;
        southNorth_JPanel = null;
	vec = null;
	addKeyListener(this);
}

/**
Responds to key pressed events.  If the dialog has been initialized to have a
'No' button, pressing 'N' will activate that button.  If the dialog has been
initialized to have a 'Yes' button, pressing 'Y' will activate that button.
If the dialog has a 'Cancel' button, pressing 'Escape' will activate that 
button.  If the dialog has an 'OK' button, pressing 'Enter' will activate that
button.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {
	int code = e.getKeyCode();

	if (code == KeyEvent.VK_N) {
		if ((__mode & NO) == NO) {
			actionPerformed(new ActionEvent(this, 0, __BUTTON_NO));
		}
	}
	if (code == KeyEvent.VK_Y) {
		if ((__mode & YES) == YES) {
			actionPerformed(new ActionEvent(this, 0, __BUTTON_YES));
		}
	}
	if (code == KeyEvent.VK_ESCAPE) {
		if ((__mode & CANCEL) == CANCEL) {
			actionPerformed(new ActionEvent(this, 0, 
				__BUTTON_CANCEL));
		}
	}
	if (code == KeyEvent.VK_ENTER) {
		if ((__mode & OK) == OK) {
			actionPerformed(new ActionEvent(this, 0, __BUTTON_OK));
		}
	}
}

/**
Responds to key released events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {}

/**
Responds to key typed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyTyped(KeyEvent e) {}

/**
Return the user response and dispose the dialog.
@return the Dialog response (e.g., OK, CANCEL, YES, or NO)
*/
public int response()
{	setVisible( false );
	dispose();
	return __response;
}

/**
Respond to WindowEvents.
@param event WindowEvent object.
*/
public void windowClosing( WindowEvent event )
{	__response = CANCEL;
	response();
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

} // end ResponseJDialog
