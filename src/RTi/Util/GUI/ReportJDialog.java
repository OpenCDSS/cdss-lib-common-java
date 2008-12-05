//-----------------------------------------------------------------------------
// ReportJDialog - component to display a text report in a JTextArea object
//-----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 
// 2005-11-16	J. Thomas Sapienza, RTi	Initial version from ReportJFrame.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.util.List;

import RTi.Util.Help.URLHelp;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SearchJDialog;

import RTi.Util.IO.ExportJGUI;
import RTi.Util.IO.PrintJGUI;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
Display a report in a JTextArea.  See the constructor for more information.
*/
public class ReportJDialog 
extends JDialog
implements ActionListener, HyperlinkListener, WindowListener {

private JTextField	_status_JTextField;	// status TextField
private JTextArea	_info_JTextArea;	// Report TextArea
private JEditorPane	_info_JEditorPane;	// Report TextArea
private List _info_Vector;		// Contains String elements to display in the _info_TextArea object
                                                
private PropList	_prop;			// PropList object
private String		_help_key;              // Help Keyword

private SimpleJButton	_close_JButton,		// close
			_help_JButton,		// help
			_print_JButton,		// print
			_save_JButton,		// save
			_search_JButton;	// Search
                        
private int	_page_length,		// lines to a page
			_print_size;		// print point size
private String		_title = null;		// Title for frame

/**
Determines the kind of text component that will be used for displaying results.
*/
private String __textComponent = "JTextArea";

/**
ReportJDialog constructor.
@param info Contains String elements to Display
@param prop PropList object as described in the following table
<table width=80% cellpadding=2 cellspacing=0 border=2>
<tr>
<td>Property</td>        <td>Description</td>     <td>Default</td>
</tr>

<tr>
<td>DisplayFont</td>
<td>Font used within text area of ReportGUI.</td>
<td>Courier</td>
</tr>

<tr>
<td>DisplaySize</td>
<td>Font size used within text area of ReportGUI.</td>
<td>11</td>
</tr>

<tr>
<td>DisplayStyle</td>
<td>Font style used within text area of ReportGUI.</td>
<td>Font.PLAIN</td>
</tr>

<tr>
<td>HelpKey</td>
<td>Search key for help.</td>
<td>Help button is disabled.</td>
</tr>

<tr>
<td>PageLength</td>
<td>No longer used, paging not necessary in Windows NT, NT200), etc or with
Java1.2.  If a Windows 95/98/ME machine is detected, the page length is set
to 100 regardless of what the property is.</td>
<td>-</td>
</tr>

<tr>
<td>PrintSize</td>
<td>Font size used for printing information.</td>
<td>10</td>
</tr>

<tr>
<td>Search</td>
<td>Indicates whether to enable the search button.</td>
<td>True.</td>
</tr>

<tr>
<td>Title</td>
<td>Title placed at the top of the ReportGUI frame.</td>
<td>No title (blank).</td>
</tr>

<tr>
<td>TotalHeight</td>
<td>Height of ReportGUI, in pixels.</td>
<td>550</td>
</tr>

<tr>
<td>TotalWidth</td>
<td>Width of ReportGUI, in pixels.</td>
<td>600</td>
</tr>

<tr>
<td>URL</td>
<td>If specified, display the page using the URL, rather than the Vector of
String.</td>
<td>Use Vector of String.</td>
</tr>

<tr>
<td>DisplayTextComponent</td>
<td>If specified, determines the kind of Text Component to use for displaying
the report data.  Possible options are "JTextArea" and "JEditorPane".  The
difference is:<br>
<ul>
<li><b>JTextArea</b> - This text component turns off line wrapping, but cannot
display HTML</li>
<li><b>JEditorPane</b> - This text component cannot turn off line wrapping, 
but it can display HTML</li>
</ul>
<td>JTextArea</td>
</tr>

</table>
*/
public ReportJDialog (JFrame parent, List info, PropList prop, boolean modal){
	super(parent, modal);
	_info_Vector = info;
	_prop = prop;

	setGUI();
}

public ReportJDialog(JDialog parent, List info, PropList prop, boolean modal){
	super(parent, modal);
	_info_Vector = info;
	_prop = prop;

	setGUI();
}

/**
Responds to components that generate action events.
@param evt ActionEvent object
*/
public void actionPerformed( ActionEvent evt )
{	Object o = evt.getSource();
	if ( o == _close_JButton) {
		o = null;
		close_clicked();
	}
	else if ( o == _help_JButton ) {
		o = null;
		URLHelp.showHelpForKey( _help_key );
	}
	else if ( o == _print_JButton ) {
		o = null;
		PrintJGUI.print ( this, _info_Vector, null, _print_size );
	}
	else if ( o == _save_JButton ) {
		o = null;
		ExportJGUI.export ( this, _info_Vector );
	}
	else if ( o == _search_JButton ) {
		o = null;
		if ( _title == null ) {
			if (__textComponent.equals("JTextArea")) {
				new SearchJDialog(this, _info_JTextArea, null);
			} 
			else if (__textComponent.equals("JEditorPane")) {
				new SearchJDialog (this,_info_JEditorPane,null);
			}
		}	
		else {	
			if (__textComponent.equals("JTextArea")) {
				new SearchJDialog(this, _info_JTextArea, 
					"Search " + _title);
			} 
			else if (__textComponent.equals("JEditorPane")) {
				new SearchJDialog ( this, _info_JEditorPane,
					"Search " + _title );
			}
		}
	}
}

/**
Responsible for closing the component.
*/
private void close_clicked()
{	setVisible( false );
	// If the soft close property is true, then just set hidden
	String prop_val = _prop.getValue("Close");
	if (	(prop_val == null) ||
		((prop_val != null) && !prop_val.equalsIgnoreCase("soft")) ) {
        	dispose();
	}
}

/**
Add the contents of the formatted Vector to the JTextArea object starting from
the specified line number and ending with the specified line number.
*/
private void displayContents()
{	_status_JTextField.setText( "Displaying Report..." );
	setGUICursor( Cursor.WAIT_CURSOR );

	String prop_value = _prop.getValue ( "URL" );
	if ( prop_value != null ) {
		if (__textComponent.equals("JEditorPane")) {
			// Try to set text using the URL.
			try {	_info_JEditorPane.setPage ( prop_value );
				// Force the position to be at the top...
				_info_JEditorPane.setCaretPosition ( 0 );
				_info_JEditorPane.addHyperlinkListener ( this );
				_status_JTextField.setText( "Ready" );
			}
			catch ( Exception e ) {
				_status_JTextField.setText( "Unable to display "
					+ "\"" + prop_value + "\"" );
			}
		}
		else {
			_status_JTextField.setText( "Unable to display "
				+ "\"" + prop_value + "\"" );
		}			
	}
	else if ( _info_Vector != null ) {
		StringBuffer contents = new StringBuffer();
		String newLine = System.getProperty ( "line.separator" );
		int from = 0; 
		int to = _info_Vector.size();
		int size = _info_Vector.size();
                
		// Set the JTextArea
		if ( Message.isDebugOn ) {
			String routine = "ReportJDialog.displayContents";
			Message.printDebug ( 1, routine,
			"Text report is " + size + " lines." );
		}
		for ( int i=from; i<to; i++ ) {
			contents.append ( (String)_info_Vector.get( i ) + newLine );
		}
		if (__textComponent.equals("JTextArea")) {
			_info_JTextArea.setText(contents.toString());
			_info_JTextArea.setCaretPosition(0);
		}
		else if (__textComponent.equals("JEditorPane")) {
			_info_JEditorPane.setContentType( "text/html" );
			_info_JEditorPane.setText( contents.toString() );
			// Force the position to be at the top...
			_info_JEditorPane.setCaretPosition ( 0 );
		}
		_status_JTextField.setText( "Ready" );
	}
	else {	_status_JTextField.setText( "No text to display" );
	}

	setGUICursor( Cursor.DEFAULT_CURSOR );
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_status_JTextField = null;
	_info_JEditorPane = null;
	_info_JTextArea = null;
	_info_Vector = null;
                                                
	_prop = null;
	_help_key = null;
	_title = null;

	_save_JButton = null;
	_search_JButton = null;
	_help_JButton = null;
	_print_JButton = null;
	_close_JButton = null;

	super.finalize();
}

/**
Handle hyperlink events, if a URL is being displayed.
*/
public void hyperlinkUpdate ( HyperlinkEvent e )
{	if ( e.getEventType() != EventType.ACTIVATED ) {
		return;
	}
	if (!__textComponent.equals("JEditorPane")) {
		return;
	}
	try {	_info_JEditorPane.setPage ( e.getURL() );
		// Force the position to be at the top...
		_info_JEditorPane.setCaretPosition ( 0 );
		_status_JTextField.setText( "Ready" );
	}
	catch ( Exception e2 ) {
		_status_JTextField.setText( "Unable to display \"" +
		e.getURL() + "\"" );
	}
}

/**
Instantiates and arranges the GUI components.
*/
private void setGUI()
{	int		height,
			width,
			displayStyle,
			displaySize;
	String		displayFont,
			propValue;

	/**
	This anonymous inner class extends WindowAdapter and overrides
	the no-ops window closing event.
	*/
	addWindowListener(this);
	// Objects used throughout the GUI layout..

	Insets insetsTLBR = new Insets(7,7,7,7);
	GridBagLayout gbl = new GridBagLayout();

	// If the property list is null, allocate one here so we
	// don't have to constantly check for null...
	if ( _prop == null ) {
		_prop = new PropList("Default");
	}

	// No check needed on these as null is the default value
	_help_key = _prop.getValue( "HelpKey" );
	_title = _prop.getValue( "Title");

	// Check the non-null values so a default is applied if the
	// property 'key' does not exist

	// Determine the width
	propValue = _prop.getValue( "TotalWidth" );
	if ( propValue == null ) {
		width = 600;
	}
	else {	width = StringUtil.atoi( propValue );
	}

	// Determine the height
	propValue =  _prop.getValue( "TotalHeight" );
	if ( propValue == null ) {
		height = 550;
	}
	else {	height = StringUtil.atoi( propValue );
	}

	// Determine the Font type
	propValue =  _prop.getValue( "DisplayFont" );
	if ( propValue == null ) {
		displayFont = "Courier";
	}
	else {	displayFont = propValue;
	}

	// Determine the Font style
	propValue =  _prop.getValue( "DisplayStyle" );
	if ( propValue == null ) {
		displayStyle = Font.PLAIN;
	}
	else {	displayStyle = StringUtil.atoi( propValue );
	}

	// Determine the Font size
	propValue =  _prop.getValue( "DisplaySize" );
	if ( propValue == null ) {
		displaySize = 11;
	}
	else {	displaySize = StringUtil.atoi( propValue );
	}

	// Determine the print size in number of lines
	propValue =  _prop.getValue( "PrintSize" );
	if ( propValue == null ) {
		_print_size = 10;
	}
	else {	_print_size = StringUtil.atoi( propValue );
	}

	propValue = _prop.getValue("DisplayTextComponent");
	if (propValue != null) {
		if (propValue.equalsIgnoreCase("JTextArea")) {
			__textComponent = "JTextArea";
		}
		else if (propValue.equalsIgnoreCase("JEditorPane")) {
			__textComponent = "JEditorPane";
		}
		else {
			// default to JTextArea if any other value is provided
			__textComponent = "JTextArea";
		}
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, "ReportJDialog.setGUI", 
			"Setting text display area to be of type:\"" + 
			__textComponent + "\"" );
		}
	}

	// Determine the page length in number of lines. NOTE: This was
	// implemented to manage limitations of displayable memory in the
	// the java.awt.TextArea in Windows 95.  If a Windows 95 variant, then
	// if the value page length is not set, set it to 100.  If not a
	// Windows 95 variant (e.g., NT), then set the value to a large number.
	propValue =  _prop.getValue( "PageLength" );
	String os_name = System.getProperty ( "os.name" );
	if ( propValue == null ) {
		//_page_length = 100;
		// SAM (2001-06-08) - make this large so that paging is off
		// by default.  However, if a Windows 95/98/ME machine, set to
		// 100 because these machines cannot handle large reports...
		if ( os_name.equalsIgnoreCase("Windows 95") ) {
			_page_length = 100;
		}
		else {	_page_length = 1000000000;
		}
        }
	else {	_page_length = StringUtil.atoi( propValue );
		if ( !os_name.equalsIgnoreCase("Windows 95") ) {
			// Set to a large number to disable the page length
			// for NT machines...
			_page_length = 1000000000;
		}
		else {	// Limit to reasonable value...
			if ( _page_length > 200 ) {
				_page_length = 100;
			}
		}
        }
	os_name = null;

	// Center Panel
	JPanel center_JPanel = new JPanel();
	center_JPanel.setLayout( gbl );
	getContentPane().add( "Center", center_JPanel );

	if (__textComponent.equals("JTextArea")) {
		_info_JTextArea = new JTextArea();
		_info_JTextArea.setEditable( false );
		_info_JTextArea.setLineWrap(false);
		_info_JTextArea.setFont( new Font( displayFont, displayStyle,
				displaySize ) );
		JScrollPane info_JScrollPane = new JScrollPane(_info_JTextArea);
		JGUIUtil.addComponent(center_JPanel, info_JScrollPane,
			0, 0, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	} 
	else if (__textComponent.equals("JEditorPane")) {
		_info_JEditorPane = new JEditorPane();
		_info_JEditorPane.setFont( new Font( displayFont, displayStyle,
				displaySize ) );
		_info_JEditorPane.setEditable ( false );
		JScrollPane info_JScrollPane=new JScrollPane(_info_JEditorPane);
		JGUIUtil.addComponent(center_JPanel, info_JScrollPane,
			0, 0, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	}
       
	// Bottom Panel
	JPanel bottom_JPanel = new JPanel();
	bottom_JPanel.setLayout( new BorderLayout() );
	getContentPane().add( "South", bottom_JPanel );

	// Bottom: Center Panel
	JPanel bottomC_JPanel = new JPanel();
	bottomC_JPanel.setLayout( new FlowLayout(FlowLayout.CENTER) );
	bottom_JPanel.add( "Center", bottomC_JPanel );
        
	_print_JButton = new SimpleJButton( "Print", this );        
	bottomC_JPanel.add( _print_JButton );
	_save_JButton = new SimpleJButton( "Save", this );        
	bottomC_JPanel.add( _save_JButton );
	_close_JButton = new SimpleJButton( "Close", this );
	bottomC_JPanel.add( _close_JButton );

	if ( _help_key != null ) {
		_help_JButton = new SimpleJButton( "Help", this );
	}

	propValue = _prop.getValue( "Search" );
	if ( (propValue == null) || !propValue.equalsIgnoreCase("true") ) {
		_search_JButton = new SimpleJButton( "Search", this );
		bottomC_JPanel.add ( _search_JButton );
	}

	// Bottom: South Panel
	JPanel bottomS_JPanel = new JPanel();
	bottomS_JPanel.setLayout( gbl );
	bottom_JPanel.add("South", bottomS_JPanel);

	_status_JTextField = new JTextField();
	_status_JTextField.setEditable( false );
	JGUIUtil.addComponent(bottomS_JPanel, _status_JTextField,
		0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Frame settings
	if ( _title != null ) {
		if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle( _title );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() + " - " +
			_title );
		}
	}
	pack();
	setSize( width, height );
	JGUIUtil.center( this );

	displayContents();
        
	setVisible( true );
}

/**
Sets the Cursor for all the GUI components
@param flag Cursor type (e.g, Cursor.WAIT_CURSOR etc..)
*/
private void setGUICursor( int flag )
{	setCursor( new Cursor(flag) );
	if (__textComponent.equals("JTextArea")) {
		_info_JTextArea.setCursor(new Cursor(flag));
	} 
	else if (__textComponent.equals("JEditorPane")) {
		_info_JEditorPane.setCursor( new Cursor(flag) );
	}
	_status_JTextField.setCursor( new Cursor(flag) );
}

public void windowActivated(WindowEvent e) {}
public void windowClosed(WindowEvent e) {}
public void windowClosing(WindowEvent e) {
	close_clicked();
}
public void windowDeactivated(WindowEvent e) {}
public void windowDeiconified(WindowEvent e) {}
public void windowIconified(WindowEvent e) {}
public void windowOpened(WindowEvent e) {}

} // end ReportJDialog
