//-----------------------------------------------------------------------------
// ReportJFrame - component to display a text report in a JTextArea object
//-----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 01 Apr 1998  DLG, RTi                Created initial class decription.
// 24 Feb 1999  CEN, RTi                No longer need paging using Java 1.2.
// 11 Jun 1999  Steven A. Malers, RTi	Clean up code, add finalize, and check
//					for problem displaying large report.
//					Rechecked in previous revisions to get
//					permissions back in place.
// 28 Nov 2000	SAM, RTi		Change "Export" button to "Save", which
//					is more consistent with standard GUI
//					notation.
// 07 Jan 2001	SAM, RTi		Change GUI to GUIUtil.
// 29 Mar 2001	SAM, RTi		Add optional Search button and related
//					features.
// 16 May 2001	SAM, RTi		Change search feature to be on by
//					default.
// 16 Jul 2001	SAM, RTi		If using Windows 95/98/ME, turn paging
//					on by default because the systems cannot
//					handle reports > 64KB.  Otherwise, turn
//					paging off always.  If Windows 95/98/ME,
//					change label on Search to "Search Page".
// 2002-04-05	SAM, RTi		Minor revision to use toVector() for
//					TextArea conversion.
// 2002-09-04	J. Thomas Sapienza, RTi	Dropped a 0 off of both assignments:
//					_page_length = 10000000000;
//					because the compiler was breaking.
// ============================================================================
// 2002-10-24	SAM, RTi		Copy the AWT JReportGUI to this class
//					and implement a Swing version.
//					Change to support general JTextComponent
//					and add ability to display a URL.
//					Remove paging code because Swing does
//					not require and it complicates the
//					logic.
// 2003-03-28	JTS, RTi		Added code for using a JTextArea as 
//					opposed to a JEditorPane if the proper
//					prop is passed in.  JTextAreas can 
//					very easily turn off word wrapping.
// 2003-05-27	JTS, RTi		Made class a window listener to get rid
//					of anonymous inner window adapter class.
// 2003-09-30	SAM, RTi		Enable the title bar and icon as set
//					by the main application.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Component;
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
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintJGUI;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
Display a report in a JTextArea.  See the constructor for more information.
*/
public class ReportJFrame extends JFrame implements ActionListener, HyperlinkListener, WindowListener
{

private JTextField _status_JTextField; // status TextField
private JTextArea _info_JTextArea; // Report TextArea
private JEditorPane _info_JEditorPane; // Report TextArea
private List<String> _info_Vector; // Contains list of String to display in the _info_TextArea object
                                                
private PropList _prop; // Controls display
private String _help_key; // Help Keyword

private SimpleJButton _close_JButton,
			_help_JButton,
			_print_JButton,
			_save_JButton,
			_search_JButton;
                        
private int _page_length; // lines to a page
private int _print_size; // print point size
private String _title = null; // Title for frame

/**
Determines the kind of text component that will be used for displaying results, either
"JTextArea" (for simple black on white text) or "JEditorPane" (for marked-up navigable HTML).
*/
private String __textComponent = "JTextArea";

/**
ReportJFrame constructor.
@param info Contains list of String to display.
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
<td>ParentUIComponent</td>
<td>Component to use for parent, used to determine screen for centering the dialog.</td>
<td>Screen 0</td>
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
<td>If specified, display the page using the URL, rather than the Vector of String.</td>
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
public ReportJFrame ( List<String> info, PropList prop )
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
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
				new SearchJDialog(this, _info_JTextArea, "Search " + _title);
			} 
			else if (__textComponent.equals("JEditorPane")) {
				new SearchJDialog ( this, _info_JEditorPane, "Search " + _title );
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
	if ( (prop_val == null) || ((prop_val != null) && !prop_val.equalsIgnoreCase("soft")) ) {
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
	if ( __textComponent.equals("JEditorPane") ) {
       if ( prop_value != null ) {
			// Try to set text in the HTML viewer using the URL.
			try {
                _info_JEditorPane.setPage ( prop_value );
				// Force the position to be at the top...
				_info_JEditorPane.setCaretPosition ( 0 );
				_info_JEditorPane.addHyperlinkListener ( this );
				_status_JTextField.setText( "Ready" );
			}
			catch ( Exception e ) {
				_status_JTextField.setText( "Unable to display \"" + prop_value + "\"" );
			}
		}
		else {
			_status_JTextField.setText( "Unable to display - no URL provided" );
		}			
	}
	else {
	    // Trying to view using the text area...
        boolean status_set = false; // Indicate whether the status message has been set - to get most appropriate message
        if ( (_info_Vector == null) && (prop_value != null) ) {
            // Read the text into a Vector...
            if ( !IOUtil.fileExists( prop_value) ) {
                _status_JTextField.setText( "Unable to display - file does not exist:  " + prop_value );
                status_set = true;
            }
            else {
                try {
                    _info_Vector = IOUtil.fileToStringList ( prop_value );
                }
                catch ( IOException e ) {
                    _info_Vector = null;
                    _status_JTextField.setText( "Unable to display - no URL provided" );
                    status_set = true;
                }
            }
        }
        
        if ( _info_Vector != null ) {

    		StringBuffer contents = new StringBuffer();
    		String newLine = System.getProperty ( "line.separator" );
    		int from = 0; 
    		int to = _info_Vector.size();
    		int size = _info_Vector.size();
                    
    		// Set the JTextArea
    		if ( Message.isDebugOn ) {
    			String routine = "ReportJFrame.displayContents";
    			Message.printDebug ( 1, routine, "Text report is " + size + " lines." );
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

    	else if ( !status_set ) {
            _status_JTextField.setText( "No text to display" );
    	}
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
	try {
        _info_JEditorPane.setPage ( e.getURL() );
		// Force the position to be at the top...
		_info_JEditorPane.setCaretPosition ( 0 );
		_status_JTextField.setText( "Ready" );
	}
	catch ( Exception e2 ) {
		_status_JTextField.setText( "Unable to display \"" + e.getURL() + "\"" );
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

	// Check the non-null values so a default is applied if the property 'key' does not exist

	// Determine the width
	propValue = _prop.getValue( "TotalWidth" );
	if ( propValue == null ) {
		width = 600;
	}
	else {
        width = StringUtil.atoi( propValue );
	}

	// Determine the height
	propValue =  _prop.getValue( "TotalHeight" );
	if ( propValue == null ) {
		height = 550;
	}
	else {
        height = StringUtil.atoi( propValue );
	}

	// Determine the Font type
	propValue =  _prop.getValue( "DisplayFont" );
	if ( propValue == null ) {
		displayFont = "Courier";
	}
	else {
        displayFont = propValue;
	}

	// Determine the Font style
	propValue =  _prop.getValue( "DisplayStyle" );
	if ( propValue == null ) {
		displayStyle = Font.PLAIN;
	}
	else {
        displayStyle = StringUtil.atoi( propValue );
	}

	// Determine the Font size
	propValue =  _prop.getValue( "DisplaySize" );
	if ( propValue == null ) {
		displaySize = 11;
	}
	else {
        displaySize = StringUtil.atoi( propValue );
	}

	// Determine the print size in number of lines
	propValue =  _prop.getValue( "PrintSize" );
	if ( propValue == null ) {
		_print_size = 10;
	}
	else {
        _print_size = StringUtil.atoi( propValue );
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
			Message.printDebug( 2, "ReportJFrame.setGUI", "Setting text display area to be of type:\"" + __textComponent + "\"" );
		}
	}

	// Determine the page length in number of lines. NOTE: This was
	// implemented to manage limitations of displayable memory in the
	// the java.awt.TextArea in Windows 95.  If a Windows 95 variant, then
	// if the value page length is not set, set it to 100.  If not a
	// Windows 95 variant (e.g., NT), then set the value to a large number.
	propValue = _prop.getValue( "PageLength" );
	String os_name = System.getProperty ( "os.name" );
	if ( propValue == null ) {
		//_page_length = 100;
		// TODO SAM (2001-06-08) - Evaluate need.
        // Make this large so that paging is off
		// by default.  However, if a Windows 95/98/ME machine, set to
		// 100 because these machines cannot handle large reports...
		if ( os_name.equalsIgnoreCase("Windows 95") ) {
			_page_length = 100;
		}
		else {
            _page_length = 1000000000;
		}
    }
	else {
        _page_length = StringUtil.atoi( propValue );
		if ( !os_name.equalsIgnoreCase("Windows 95") ) {
			// Set to a large number to disable the page length for NT machines...
			_page_length = 1000000000;
		}
		else {
            // Limit to reasonable value...
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
		_info_JTextArea.setFont( new Font( displayFont, displayStyle, displaySize ) );
		JScrollPane info_JScrollPane = new JScrollPane(_info_JTextArea);
		JGUIUtil.addComponent(center_JPanel, info_JScrollPane,
			0, 0, 1, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST);
	} 
	else if (__textComponent.equals("JEditorPane")) {
		_info_JEditorPane = new JEditorPane();
		_info_JEditorPane.setFont( new Font( displayFont, displayStyle,	displaySize ) );
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
    
    propValue = _prop.getValue( "Search" );
    if ( (propValue == null) || !propValue.equalsIgnoreCase("true") ) {
        _search_JButton = new SimpleJButton( "Search", this );
        bottomC_JPanel.add ( _search_JButton );
    }
	_print_JButton = new SimpleJButton( "Print", this );        
	bottomC_JPanel.add( _print_JButton );
	_save_JButton = new SimpleJButton( "Save", this );        
	bottomC_JPanel.add( _save_JButton );
	_close_JButton = new SimpleJButton( "Close", this );
	bottomC_JPanel.add( _close_JButton );

	if ( _help_key != null ) {
		_help_JButton = new SimpleJButton( "Help", this );
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
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle( _title );
		}
		else {
            setTitle( JGUIUtil.getAppNameForWindows() + " - " + _title );
		}
	}
	pack();
	setSize( width, height );
	// Get the UI component to determine screen to display on - needed for multiple monitors
	Object uiComponentO = _prop.getContents( "ParentUIComponent" );
	Component parentUIComponent = null;
	if ( (uiComponentO != null) && (uiComponentO instanceof Component) ) {
		parentUIComponent = (Component)uiComponentO;
	}
	JGUIUtil.center( this, parentUIComponent );

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

}