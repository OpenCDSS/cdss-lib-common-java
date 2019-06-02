// TSViewSummaryJFrame - Summary (text) view of one or more time series

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
// TSViewSummaryJFrame - Summary (text) view of one or more time series
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1)	This class displays the time series using the property
//			list.  Mixed time steps are allowed.
//------------------------------------------------------------------------------
// History:
// 
// 05 Dec 1998	Steven A. Malers,	Initial version.  Copy OpTableDislayGUI
//		Riverside Technology,	and modify as necessary.
//		inc.
// 12 Oct 2000	SAM, RTi		Enable Print and Save As buttons.
// 19 Feb 2001	SAM, RTi		Change GUI to GUIUtil.
// 04 May 2001	SAM, RTi		Add TSViewTitleString property to set
//					the title of the view window.
//					Enable the search button.
// 17 May 2001	SAM, RTi		Change Save As button to Save As choice
//					to save .txt and DateValue file.  Add
//					finalize().
// 07 Sep 2001	SAM, RTi		Set TextArea buffer to null after set
//					to help garbage collection.
// 2001-11-05	SAM, RTi		Update javadoc.  Make sure variables are
//					set to null when done.
// 2001-12-11	SAM, RTi		Change help key to "TSView.Summary".
// 2002-01-17	SAM, RTi		Change name from TSViewSummaryGUI to
//					TSViewSummaryFrame to allow support for
//					Swing.
// ==================================
// 2002-11-11	SAM, RTi		Copy AWT version and update to Swing.
// 2003-06-04	SAM, RTi		* Final update to Swing based on GR and
//					  TS changes.
//					* Add a JScrollPane around the
//					  JTextArea.
//					* Change the Save As choice to a button.
// 2003-08-21	SAM, RTi		* Change DateValueTS.writeTimeSeries()
//					  to DateValueTS.writeTimeSeriesList().
// 2003-09-30	SAM, RTi		* Use icon/title from the main
//					  application if available.
// 2004-01-04	SAM, RTi		* Fix bug where saving the file was not
//					  using the full path.
//					* Comment the Help button for now -
//					  is not typically enabled.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.GRTS;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import RTi.TS.DateValueTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SearchJDialog;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintJGUI;
import RTi.Util.IO.PropList;
import RTi.Util.Help.URLHelp;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The TSViewSummaryJFrame provides a text report summary for a list of time
series.  The report is dependent on the time series interval and data type.  The
TSViewSummaryJFrame is managed by the TSViewJFrame parent.
*/
@SuppressWarnings("serial")
public class TSViewSummaryJFrame extends JFrame
implements ActionListener, WindowListener
{

// Private data...

/**
TSViewJFrame parent, which manages all the views collectively.
*/
private TSViewJFrame __tsview_JFrame;
/**
List of time series to graph.
*/
private List<TS> __tslist;
/**
Property list.
*/
private PropList __props;

private SimpleJButton __graph_JButton = null;
private SimpleJButton __table_JButton = null;
private SimpleJButton __save_JButton = null;
private SimpleJButton __close_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __print_JButton = null;
private SimpleJButton __search_JButton = null;

private JTextArea __summary_JTextArea = null;

private String __summary_font_name = "Courier";// Default for now
private int __summary_font_style = Font.PLAIN;
private int __summary_font_size = 11;

/**
Construct a TSViewSummaryJFrame.
@param tsview_gui Parent TSViewJFrame.
@param tslist List of time series to display.
@param props Properties for display (currently same list passed in to TSViewFrame).
@exception if there is an error displaying the time series.
*/
public TSViewSummaryJFrame(	TSViewJFrame tsview_gui, List<TS> tslist, PropList props )
throws Exception
{	super ( "Time Series - Summary View" );
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	initialize ( tsview_gui, tslist, props );
}

/**
Handle action events (button press, etc.)
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e )
{	Object o = e.getSource();
	if ( o == __close_JButton ) {
		// Close the GUI via the parent...
		__tsview_JFrame.closeGUI(TSViewType.SUMMARY);
	}
	else if ( o == __help_JButton ) {
		// Show help...
		URLHelp.showHelpForKey ("TSView.Summary");
	}
	else if ( o == __graph_JButton ) {
		// Display a graph...
		__tsview_JFrame.openGUI ( TSViewType.GRAPH );
	}
	else if ( o == __print_JButton ) {
		// Print the summary...
		PrintJGUI.printJTextAreaObject(this, null, __summary_JTextArea);
	}
	else if ( o == __save_JButton ) {
		// Save the summary report or the data in the report...
		save ();
	}
	else if ( o == __search_JButton ) {
		// Search the text area...
		new SearchJDialog ( this, __summary_JTextArea, "Search " + getTitle() );
	}
	else if ( o == __table_JButton ) {
		// Display a table...
		__tsview_JFrame.openGUI ( TSViewType.TABLE );
	}
}

/**
Clean up for garbage collection.
*/
protected void finalize()
throws Throwable
{	__tsview_JFrame = null;
	__tslist = null;
	__props = null;

	__graph_JButton = null;
	__table_JButton = null;
	__save_JButton = null;
	__close_JButton = null;
	__help_JButton = null;
	__print_JButton = null;
	__search_JButton = null;
	__summary_JTextArea = null;
	__summary_font_name = null;

	super.finalize();
}

/**
Initialize the data and GUI.
@param tsview_gui Parent TSViewJFrame.
@param tslist List of time series to display.
@param props Properties for display (currently same list passed in to TSViewJFrame).
*/
private void initialize ( TSViewJFrame tsview_gui, List<TS> tslist, PropList props )
{	__tsview_JFrame = tsview_gui;
	__tslist = tslist;
	__props = props;
	String prop_value = __props.getValue ( "TSViewTitleString" );
	if ( prop_value == null ) {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Time Series - Summary" );
		}
		else {
			setTitle( JGUIUtil.getAppNameForWindows() + " - Time Series - Summary" );
		}
	}
	else {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( prop_value + " - Summary" );
		}
		else {
			setTitle( JGUIUtil.getAppNameForWindows() + " - " + prop_value + " - Summary" );
		}
	}
	openGUI ( true );
}

/**
Open the GUI and display the time series summary.
@param mode Indicates whether the GUI should be visible at creation.
*/
private void openGUI ( boolean mode )
{	String routine = getClass().getSimpleName() + ".openGUI";
	// Start a big try block to set up the GUI...
	try {

	// Add a listener to catch window manager events...

	addWindowListener ( this );

	// Lay out the main window component by component.  We will start with
	// the menubar default components.  Then add each requested component
	// to the menu bar and the interface...

	GridBagLayout gbl = new GridBagLayout();

	Insets insetsTLBR = new Insets ( 7, 7, 7, 7 );	// space around text area
	
	// Add a panel to hold the text area...

	JPanel display_JPanel = new JPanel ();
	display_JPanel.setLayout ( gbl );
	getContentPane().add ( display_JPanel );

	// Get the formatted string for the time series...

	StringBuffer buffer = new StringBuffer();
	String nl = System.getProperty ( "line.separator" );
	try {
		List<String> summary_strings = TSUtil.formatOutput (__tslist,__props);
		if ( summary_strings != null ) {
			int size = summary_strings.size();
			for ( int i = 0; i < size; i++ ) {
				buffer.append ( summary_strings.get(i) + nl );
			}
		}
	}
	catch ( Exception e ) {
		buffer.append ( "Error creating time series summary." );
		Message.printWarning(3, routine, e);
	}
	nl = null;

	__summary_JTextArea = new JTextArea ();
	__summary_JTextArea.setBackground ( Color.white );
	__summary_JTextArea.setEditable ( false );
	__summary_JTextArea.setFont ( new Font ( __summary_font_name, __summary_font_style, __summary_font_size ) );
	JScrollPane summary_JScrollPane = new JScrollPane (__summary_JTextArea);
	JGUIUtil.addComponent ( display_JPanel, summary_JScrollPane,
			0, 0, 1, 1, 1, 1,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );

	// Put the buttons on the bottom of the window...

	JPanel button_JPanel = new JPanel ();
	button_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

	__graph_JButton =new SimpleJButton("Graph", "TSViewSummaryJFrame.Graph", this );
	String prop_value=__props.getValue("EnableGraph");
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
		__graph_JButton.setEnabled(false);
	}
	button_JPanel.add ( __graph_JButton );

	__table_JButton =new SimpleJButton("Table", "TSViewSummaryJFrame.Table", this);
	prop_value=__props.getValue("EnableTable");
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
		__table_JButton.setEnabled(false);
	}
	button_JPanel.add ( __table_JButton );

	__search_JButton = new SimpleJButton("Search", "TSViewSummaryJFrame.Search", this );
	button_JPanel.add ( __search_JButton );

	__help_JButton = new SimpleJButton("Help", "TSViewSummaryJFrame.Help",this);
	// REVISIT - enable later when better on-line help system is enabled.
	//button_JPanel.add ( __help_JButton );

	__print_JButton =new SimpleJButton("Print", "TSViewSummaryJFrame.Print", this );
	button_JPanel.add ( __print_JButton );

	__save_JButton = new SimpleJButton("Save", "TSViewSummaryJFrame.Save", this );
	button_JPanel.add ( __save_JButton );

	__close_JButton=new SimpleJButton("Close", "TSViewSummaryJFrame.Close",this);
	button_JPanel.add ( __close_JButton );

	getContentPane().add ( "South", button_JPanel );

	// Get properties specific to the view
	prop_value = __props.getValue ( "Summary.TotalWidth" );
	if ( prop_value == null ) {
		prop_value = __props.getValue ( "TotalWidth" );
	}
	pack ();	// Before setting size
	int total_width = 0, total_height = 0;
	if ( prop_value != null ) {
		total_width = StringUtil.atoi(prop_value);
	}
	prop_value = __props.getValue ( "Summary.TotalHeight" );
	if ( prop_value == null ) {
		prop_value = __props.getValue ( "TotalHeight" );
	}
	if ( prop_value != null ) {
		total_height = StringUtil.atoi(prop_value);
	}
	if ( (total_width <= 0) || (total_height <= 0) ) {
		// No property so make a guess...
		setSize ( 800, 600 );
	}
	else {
		setSize ( total_width, total_height );
	}
	// Get the UI component to determine screen to display on - needed for multiple monitors
	Object uiComponentO = __props.getContents( "TSViewParentUIComponent" );
	Component parentUIComponent = null;
	if ( (uiComponentO != null) && (uiComponentO instanceof Component) ) {
		parentUIComponent = (Component)uiComponentO;
	}
	// Center on the UI component rather than the graph, because the graph screen seems to get tied screen 0?
	JGUIUtil.center ( this, parentUIComponent );
	// Seems to work best here to get the window size right.
	__summary_JTextArea.setText ( buffer.toString() );
	// Set the cursor position to the top
	__summary_JTextArea.setCaretPosition(0);
	setVisible ( mode );
	} // end of try
	catch ( Exception e ) {
		Message.printWarning(3, routine, e);
	}
}

/**
Save the graph in standard formats.
*/
private void save ()
{	String routine = "TSViewSummaryFrame.save";
	String last_directory = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc =JFileChooserFactory.createJFileChooser(last_directory);
	fc.setDialogTitle ( "Save Summary" );
	fc.setAcceptAllFileFilterUsed ( false );
	SimpleFileFilter dv_sff = new SimpleFileFilter("dv", "DateValue Time Series File" );
	fc.addChoosableFileFilter ( dv_sff );
	SimpleFileFilter dvtxt_sff = new SimpleFileFilter ( "txt", "DateValue Time Series File" );
	fc.addChoosableFileFilter ( dvtxt_sff );
	SimpleFileFilter txt_sff = new SimpleFileFilter("txt", "Text Report" );
	fc.addChoosableFileFilter ( txt_sff );
	if ( fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION ) {
		// Canceled...
		return;
	}
	// Else figure out the file format and location and then do the save...
	last_directory = fc.getSelectedFile().getParent();
	String path = fc.getSelectedFile().getPath();
	JGUIUtil.setLastFileDialogDirectory(last_directory);
	if ( (fc.getFileFilter() == dv_sff) || (fc.getFileFilter() == dvtxt_sff) ) {
		if ( fc.getFileFilter() == dv_sff ) {
			path = IOUtil.enforceFileExtension ( path, "dv" );
		}
		else {
			path = IOUtil.enforceFileExtension ( path, "txt" );
		}
		if ( !TSUtil.intervalsMatch(__tslist) ) {
			Message.printWarning ( 1, routine, "Unable to write " +
			"DateValue time series of different intervals." );
			return;
		}
		try {
			__tsview_JFrame.setWaitCursor ( true );
			DateValueTS.writeTimeSeriesList ( __tslist, path );
			__tsview_JFrame.setWaitCursor ( false );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving DateValue file \"" + path + "\"");
			Message.printWarning ( 3, routine, e );
			__tsview_JFrame.setWaitCursor ( false );
		}
	}
	else if ( fc.getFileFilter() == txt_sff ) {
		path = IOUtil.enforceFileExtension ( path, "txt" );
		try {
			__tsview_JFrame.setWaitCursor ( true );
			JGUIUtil.writeFile ( __summary_JTextArea, path );
			__tsview_JFrame.setWaitCursor ( false );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving report file \"" + path + "\"");
			Message.printWarning ( 3, routine, e );
			__tsview_JFrame.setWaitCursor ( false );
		}
	}
}

/**
Respond to window closing event.
@param event WindowEvent object.
*/
public void windowClosing( WindowEvent event ){
	__tsview_JFrame.closeGUI(TSViewType.SUMMARY);
	return;
}

public void processWindowEvent ( WindowEvent e) 
{	if (e.getID() == WindowEvent.WINDOW_CLOSING ) {
		super.processWindowEvent(e);
		__tsview_JFrame.closeGUI(TSViewType.SUMMARY);
	}
	else {
		super.processWindowEvent(e);
	}
}

// WindowListener functions...

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}

}
