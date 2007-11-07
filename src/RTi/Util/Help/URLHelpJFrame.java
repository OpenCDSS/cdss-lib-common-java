// ----------------------------------------------------------------------------
// URLHelpJFrame - GUI for the URLHelp class
// ----------------------------------------------------------------------------
// Copyright: see the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 24 Jan 1998	Steven A. Malers, RTi	First version.
// 14 Apr 1999	SAM, RTi		Update to include browse buttons to
//					pick the web browser, etc.  Change so
//					the initial refresh reads the index
//					file since the read is now triggered
//					by viewing in URLHelp.
// 07 Jun 1999	SAM, RTi		Fix problem where if running
//					stand-alone, we don't want the help to
//					read the index file until it is
//					actually needed.  For the GUI, update
//					so the index list is not shown until
//					the GUI is made visible.
// 2001-11-14	SAM, RTi		Update javadoc.  Change GUI to JGUIUtil.
//					Add finalize().  Verify that variables
//					are set to null when no longer used.
//					Move the browser and index selection to
//					the bottom since they should be set
//					correctly at run-time now.  Change so
//					that index is re-read only if the URL
//					has changed.  Allow the title to be set.
//					Change so that when resizing vertically,
//					the list can grow but other components
//					cannot.  Remove menu items (no need for
//					this Dialog to have a menu).  Add
//					JPopupMenu to list to allow display and
//					search of help.
// ----------------------------------------------------------------------------
// 2003-05-19	J. Thomas Sapienza, RTi	Initial Swing version from AWT code.
// 2002-11-29	SAM, RTi		Set the title and icon consistent with
//					other components.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package	RTi.Util.Help;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.FindInJListJDialog;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJMenuItem;

/**
This class implements a graphical user interface (GUI) for the URLHelp
class.  The GUI should be instantiated with code similar to the following:
<p>

<pre>
        private URLHelpJFrame _help__index_gui = null;

        // Add the Help Index GUI using the standard dialog...

        _help__index_gui = new URLHelpJFrame ();
        _help__index_gui.attachMainJMenu ( _help_menu );
</pre>
<p>

The class code will attach its own menus to the specified menu and will
set up its own event handlers.  The GUI interface appears as follows:
<p>

<center>
<img src="URLHelpJFrame.gif"><p>
</center>

The browser path is that specified by URLHelp.getBrowser() and the index is
that specified by URLHelp.getIndexURL() (RTi in the past used
the <tt>-browser Browser</tt> and <tt>-helpindex URL</tt> command-line
arguments or applet parameters which are interpreted in the main program,
resulting in calls to URLHelp.setBrowser() and URLHelp.setIndexURL().  Newer
code uses the URLHelp.initialize() method to set up the help system.)
The help topics are those read from
the index file by the URLHelp.readIndex() function.
A help topic can be selected and when
"Get Help for Selected Topic" is pressed, the URL will be displayed in a
stand-alone browser (if running as a stand-alone application) or into a blank
browser page if running as an applet.
<p>

It is envisioned that the selection of the browser and index will be enhanced
and perhaps a "Details" button will display the key and URL as well as the
topic to help documentation writers.

@see URLHelp
*/
public class URLHelpJFrame extends JFrame
implements ActionListener, KeyListener, MouseListener, ListSelectionListener {

private final String __NO_TOPICS = "No topics selected";

private final String __SELECT_BROWSER = "URLHelp.SelectBrowser";
private final String __CLOSE = "URLHelp.Close";
private final String __URLHELP = "URLHelp";
private final String __HELP = "URLHelp.GetHelp";
private final String __SELECT_INDEX = "URLHelp.SelectIndex";
private final String __SEARCH = "URLHelp.Search for...";

private JTextField	__browserJTextField;
private JTextField	__indexJTextField;

private SimpleJButton	__getHelpButton;
private SimpleJButton	__browser_selectButton;
private SimpleJButton	__index_selectButton;

private JList		__topicJList;

private boolean		__dataRefreshedOnce = false;

private JPopupMenu	__helpJPopupMenu;

/**
Construct with the specified mode.
@param mode 1 if the GUI should be visible at construction, 0 if hidden.
@param title Title of window (default is "Help Index").
*/
public URLHelpJFrame ( int mode, String title )
{	super( "Help Index" );
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	if ( title == null ) {
		if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Help Index" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() +
			" - Help Index" );
		}
	}
	else {	if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( title + " - Help Index" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() + " - " +
			title + " - Help Index" );
		}
	}
	openGUI ( mode );
}

/**
Construct with the default mode (do not make visible at construction).
*/
public URLHelpJFrame ()
{	this ( 0, null );
}

/**
Construct with the specified mode.
@param mode 1 if the GUI should be visible at construction, 0 if hidden.
*/
public URLHelpJFrame ( int mode )
{	this ( mode, null );
}

/**
Handle action events.
@param event Action event.
*/
public void actionPerformed ( ActionEvent event )
{	// Check the names of the events.  These are tied to menu names.
	String command = event.getActionCommand();
	if ( command.equals(__URLHELP) ) {
		// The main menu choice... Make the GUI visible...
		if ( !__dataRefreshedOnce ) {
			refresh ( true );
		}
		setVisible(true);
	}
	else if ( command.equals(__CLOSE) ) {
		// Make the GUI hidden...
		setVisible(false);
	}
	else if ( command.equals(__HELP) ) {
		// Get help for the selected topic...
		int index = __topicJList.getSelectedIndex ();
		if ( index >= 0 ) {
			URLHelp.showHelpForIndex ( index );
		}
	}
	else if ( command.equals(__SELECT_BROWSER) ) {
		String lastDirectory =
			JGUIUtil.getLastFileDialogDirectory();		
		JFileChooser fc = JFileChooserFactory.createJFileChooser(
			lastDirectory);
		fc.setDialogTitle("Select Web Browser");
		SimpleFileFilter jff = new SimpleFileFilter("exe", 
			"Executable Files");
		fc.addChoosableFileFilter(jff);
		fc.setFileFilter(jff);
		fc.showOpenDialog(this);
		
		File file = fc.getSelectedFile();
		if (file == null ||
		    file.getName() == null || file.getName().equals("")) {
			return;
		}
	
		String fileName = file.getParent() + "\\" + file.getName();

		URLHelp.setBrowser ( fileName );
		__browserJTextField.setText ( fileName );
	}
	else if ( command.equals(__SELECT_INDEX) ) {
		// Select and set the index...
		String lastDirectory =
			JGUIUtil.getLastFileDialogDirectory();		
		JFileChooser fc = JFileChooserFactory.createJFileChooser(
				lastDirectory);
		fc.setDialogTitle("Select Help Index File");
		SimpleFileFilter jff = new SimpleFileFilter("html", 
			"HTML Files");
		fc.addChoosableFileFilter(jff);
		fc.setFileFilter(jff);
		fc.showOpenDialog(this);
		
		File file = fc.getSelectedFile();
		if (file == null ||
		    file.getName() == null || file.getName().equals("")) {
			return;
		}
	
		String fileName = file.getParent() + "\\" + file.getName();
		JGUIUtil.setLastFileDialogDirectory(file.getParent());
		
		__indexJTextField.setText ( fileName );
	}
	else if ( command.equals(__SEARCH) ) {
		new FindInJListJDialog (this, __topicJList, "Find Help Topic" );
	}
	command = null;
}

/**
Attach the GUI menus to the specified menu.
@param menu The menu to attach to.
*/
public void attachMainJMenu ( JMenu menu )
{	// The command used will be what triggers the GUI to become visible
	// when the menu is selected in the main app!
	menu.add ( new SimpleJMenuItem("Help Index...",__URLHELP,this) );
}

/**
Close the GUI.  At this time it just hides the GUI because the GUI handles
events for itself.
@param status Unused at this time.  Specify zero.
*/
public void closeGUI ( int status )
{	setVisible ( false );
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	__browserJTextField = null;
	__indexJTextField = null;

	__getHelpButton = null;
	__browser_selectButton = null;
	__index_selectButton = null;

	__topicJList = null;
	__helpJPopupMenu = null;
	super.finalize();
}

/**
Handle key press events.
@param event The key press event.
*/
public void keyPressed( KeyEvent event )
{
}

/**
Handle key release events.
@param event The key release event.
*/
public void keyReleased ( KeyEvent event )
{
}

/**
Handle key type events.
@param event The key type event.
*/
public void keyTyped ( KeyEvent event )
{
}

/**
Handle mouse clicked event.
*/
public void mouseClicked ( MouseEvent event )
{
}

/**
Handle mouse entered event.
*/
public void mouseEntered ( MouseEvent event )
{
}

/**
Handle mouse exited event.
*/
public void mouseExited ( MouseEvent event )
{
}

/**
Handle mouse pressed event.
*/
public void mousePressed ( MouseEvent event )
{	int mods = event.getModifiers();
	Component c = event.getComponent();
	if (	c.equals(__topicJList) &&
		(__topicJList.getModel().getSize() > 0) &&
		((mods & MouseEvent.BUTTON3_MASK) != 0) ) {//&&
		//event.isPopupTrigger() ) {
		__helpJPopupMenu.show (
		event.getComponent(), event.getX(), event.getY() );
	}
	c = null;
}

/**
Handle mouse released event.
*/
public void mouseReleased ( MouseEvent event )
{
}
/**
Open the GUI.
@param mode if 1, make the GUI visible; if 0, hide the GUI.
*/
private void openGUI ( int mode )
{	// objects to be used in the GUI Layout
	int b = 3;
	Insets NLBR = new Insets( 0,b,b,b );
	Insets TLNR = new Insets( b,b,0,b );
	Insets TNNR = new Insets( b,0,0,b );
	Insets TLNN = new Insets( b,b,0,0 );

	// Make sure that we have a valid URLHelp to hold the data...
	// Use a main panel with grid bag layout so the list can expand...

	GridBagLayout gbl = new GridBagLayout();

	// Add a list of the available index information at the top of the
	// dialog...

	JPanel topics_JPanel = new JPanel();
	topics_JPanel.setLayout( gbl );

	int y = 0;
	JGUIUtil.addComponent(	topics_JPanel,
				new JLabel("Help Topics:"),
				0, y, 8, 1, 1, 0, TLNR,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	
	Vector data = URLHelp.getData();
	// We create the list no matter what here so there is a list to work
	// with later.  The "refresh" is used to reset the list...
	if ( (data == null) || (mode != JGUIUtil.GUI_VISIBLE) ) {
		// There is no help index available.  Add a list with one
		// item that has "No topics available"...
		Vector v = new Vector();
		v.add(__NO_TOPICS);
		__topicJList = new JList (v);
		JGUIUtil.addComponent(	topics_JPanel, 
			new JScrollPane(__topicJList),
					0, ++y, 8, 1, 1, 1, NLBR,
					GridBagConstraints.BOTH, GridBagConstraints.WEST );
	}
	else {	// Add a list that has all the help topics shown...
		// Wait until refresh to fill!
		Vector v = new Vector();
		v.add(__NO_TOPICS);		
		__topicJList = new JList ();
		JGUIUtil.addComponent(	topics_JPanel, 
			new JScrollPane(__topicJList), 0, ++y, 8, 1, 1, 1, 
			NLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	}
	__topicJList.setSize(300, 200);
	__topicJList.addListSelectionListener ( this );
	__topicJList.addMouseListener ( this );

	__helpJPopupMenu = new JPopupMenu ( "" );
	__helpJPopupMenu.add ( new SimpleJMenuItem ("Search for...",
				__SEARCH, this ) );
	__helpJPopupMenu.add ( new SimpleJMenuItem ("Show Help",
				__HELP, this ) );
	getContentPane().add ( __helpJPopupMenu );

	// Add a panel that displays index file and browser and allows user to
	// select...

	JGUIUtil.addComponent(	topics_JPanel,
				new JLabel("Browser:"),
				0, ++y, 1, 1, 0, 0, TLNN,
				GridBagConstraints.NONE, GridBagConstraints.WEST );
	__browserJTextField = new JTextField ( URLHelp.getBrowser() );
	// User must browse...
	__browserJTextField.setEnabled(false);
	JGUIUtil.addComponent(	topics_JPanel, __browserJTextField,
				1, y, 6, 1, 1, 0, TNNR,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	__browser_selectButton = new SimpleJButton("Browse...",
		__SELECT_BROWSER,this);
	__browser_selectButton.setToolTipText (
		"Select the web browser program to run." );
	JGUIUtil.addComponent(	topics_JPanel, __browser_selectButton,
				7, y, 1, 1, 0, 0, TNNR,
				GridBagConstraints.NONE, GridBagConstraints.EAST );

	JGUIUtil.addComponent(	topics_JPanel,
				new JLabel("Help Index:"),
				0, ++y, 1, 1, 0, 0, TLNN,
				GridBagConstraints.NONE, GridBagConstraints.WEST );
	__indexJTextField = new JTextField ( URLHelp.getIndexURL() );
	// User must browse...
	__indexJTextField.setEnabled(false);
	//__indexJTextField.addKeyListener ( this );
	JGUIUtil.addComponent(	topics_JPanel, __indexJTextField,
				1, y, 6, 1, 1, 0, TNNR,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	__index_selectButton = new SimpleJButton("Browse...",
		__SELECT_INDEX,this);
	__index_selectButton.setToolTipText (
		"Select the help index file for documentation." );
	JGUIUtil.addComponent(	topics_JPanel, __index_selectButton,
				7, y, 1, 1, 0, 0, TNNR,
				GridBagConstraints.NONE, GridBagConstraints.EAST );

	// Now add the buttons at the bottom...

	// Only center panels can resize!!!!
	getContentPane().add ( "Center", topics_JPanel );

	JPanel button_JPanel = new JPanel();
	getContentPane().add ( "South", button_JPanel );
	button_JPanel.setLayout( new FlowLayout(FlowLayout.CENTER) );

	__getHelpButton = new SimpleJButton("Show Help",
		__HELP,this);
	__getHelpButton.setToolTipText (
		"Display the selected help topic using the web browser." );
	__getHelpButton.setEnabled(false);
	button_JPanel.add ( __getHelpButton );

	button_JPanel.add (new SimpleJButton("Close",__CLOSE,this));

	// Now clean up...

	if ( (mode & JGUIUtil.GUI_VISIBLE) != 0 ) {
		// We want to see the GUI at creation...
		setVisible(true);
	}
	else {	// We don't want to see the GUI at creation...
		setVisible(false);
	}

	if ( mode == JGUIUtil.GUI_VISIBLE ) {
		// Refresh the list now...
		refresh ( true );
	}
	setSize(300,300);
	pack ();
	JGUIUtil.center( this );

	// Clean up...

	NLBR = null;
	TLNR = null;
	TNNR = null;
	TLNN = null;
	topics_JPanel = null;
	button_JPanel = null;
	gbl = null;
	data = null;
}

/**
Handle window events.
@param event The window event.
*/
public void processWindowEvent( WindowEvent event )
{	if( event.getID() == WindowEvent.WINDOW_CLOSING ){
		setVisible( false );
	}
}

/**
This function takes the data fields and refreshes the results by running
the security checks again.
@param flag true if the index should be re-read.
*/
private void refresh ( boolean flag )
{	// Indicate that the data have been refreshed at least once...

	__dataRefreshedOnce = true;

	// Re-read the index file and reset the list.  ALL OF THIS NEEDS BETTER
	// ERROR HANDLING!!!...

	String index_file = __indexJTextField.getText();
	int len = 0;
	if ( index_file != null ) {
		len = index_file.length();
	}
	if ( len > 0 ) {
		// First set the index and reread the data...
		if ( flag ) {
			// Only reset if something has changed...
			if (	!URLHelp.getIndexURL().equalsIgnoreCase(
				index_file )) {
				URLHelp.setIndexURL ( index_file );
				URLHelp.readIndex ();
			}
		}
		// Now reset the list.  By this point, there will be something
		// in the list so we just clear all and add again...
		Vector data = URLHelp.getData();
		if ( data != null ) {
			__topicJList.removeAll();
			__getHelpButton.setEnabled ( false );
			if ( data.size() < 1 ) {
				Vector v = new Vector();
				v.add(__NO_TOPICS);
				__topicJList.setListData(v);
			}
			else {	URLHelpData idata = null;
				Vector v = new Vector();
				for (	int i = 0; i < data.size(); i++ ) {
					idata = (URLHelpData)data.elementAt(i);
					// At some point we may want to allow
					// display of things other than the
					// topic, sort the topics, etc.
					//__topicJList.add ( idata.getTopic() );
					v.add(idata.getTopic());
				}
				__topicJList.setListData(v);
				idata = null;
			}
		}
		data = null;
	}
	// Reset the browser...
	URLHelp.setBrowser ( __browserJTextField.getText().trim() );
	index_file = null;
}

public void valueChanged(ListSelectionEvent e) {
	// All we care is that the help item is selected and we enable the
	// help button...

	String string = (String)__topicJList.getSelectedValue();
	if ( string == null ) {
		// IE 4 was catching a null here?
		__getHelpButton.setEnabled ( false );
	}
	String browser = URLHelp.getBrowser();
	if ( browser == null ) {
		__getHelpButton.setEnabled ( false );
	}
	if ( browser.length() <= 0 ) {
		__getHelpButton.setEnabled ( false );
	}
	if ( !string.equals(__NO_TOPICS) ) {
		__getHelpButton.setEnabled ( true );
	}
}

}
