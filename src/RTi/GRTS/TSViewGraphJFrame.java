// TSViewGraphJFrame - view to display graph of one or more time series

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
// TSViewGraphJFrame - view to display graph of one or more time series
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
// 28 Jun 1999	SAM, RTi		Update to use new GR functionality.
//					Start enhancing to be fully functional.
// 26 Jul 1999	SAM, RTi		Add more functionality now that the
//					GIS.GeoView features seem to be working
//					well.
// 23 May 2000	SAM, RTi		Enable the image save.  Add a second
//					row for buttons to allow toggling select
//					and zoom modes.  Add intelligence for
//					selecting the reference graph time
//					series.
// 13 Oct 2000	SAM, RTi		Change so default is zoom mode.  Enable
//					properties window.  Add "Visible" label
//					next to reference graph so people know
//					better what is going on.  Add refresh()
//					method to recompute data limits and
//					redraw.
// 26 Oct 2000	SAM, RTi		Add scatter plot (which disables the
//					reference graph).
// 30 Oct 2000	SAM, RTi		Add duration graph, with similar
//					behavior as scatter.
// 06 Nov 2000	SAM, RTi		Add more intelligence to disable/enable
//					features based on graph type so calling
//					code mainly only needs to set the graph
//					type.
// 30 Nov 2000	SAM, RTi		Add XAxis.Format property to allow some
//					control over mouse formatting.
// 19 Feb 2001	SAM, RTi		Change GUI to GUIUtil.
// 13 Apr 2001	SAM, RTi		Disable "Change to Select Mode" until
//					it does something.  Add ability to
//					gracefully handle incompatible units
//					when creating TSGraph.  Add finalize().
// 04 May 2001	SAM, RTi		Add TSViewTitleString property to set
//					the title of the window.
// 18 May 2001	SAM, RTi		Change the Save As button to a choice
//					so that DateValue can be added as an
//					output.  For cursor Y precision,
//					recognize the YAxisPrecision property.
//					Change so if Table or Summary are not
//					enabled, don't even show the buttons.
//					Change so selection of reference graph
//					is based on non-missing data limits,
//					not overall limits.
// 14 Aug 2001	SAM, RTi		Add buttons to scroll graph
//					incrementally or go to either end.
//					Comment out until there is time to do
//					it completely.
// 2001-11-05	SAM, RTi		Update javadoc and verify that variables
//					are set to null when no longer used.
// 2001-12-11	SAM, RTi		Change help key to "TSView.Graph".
// 2002-01-17	SAM, RTi		Change TSViewGraphGUI to
//					TSViewGraphFrame to allow support for
//					Swing.  Allow construction from a
//					TSProduct, which is being phased in as
//					the preferred method.
//					Add "TSProduct" to save as choice.
// 2002-02-09	SAM, RTi		Remove the Properties and Details
//					buttons.  They are now available as a
//					popup for each graph.  Finish
//					implementing the scroll buttons.
// 2002-02-20	SAM, RTi		Change default product extension to
//					tspd.
// 2003-06-03	SAM, RTi		* Update to be consistent with current
//					  GR and TS packages.
//					* Change default product extension back
//					  to "tsp".
//					* Change the Save As to a Save button
//					  and use the filter to determine what
//					  file format is being saved.
//					* Remove batchGraph() method since it
//					  was not being called - TSProcessor
//					  can be used instead.
//					* Change actionPerformed() to check
//					  objects, not command names.
//					* Use Swing setPreferredSize() for the
//					  reference graph.
// 2003-08-21	SAM, RTi		* Change DateValueTS.writeTimeSeries()
//					  to writeTimeSeriesList().
// 2003-09-19	SAM, RTi		* Fix bug where save was not using the
//					  full path.
// 2003-09-30	SAM, RTi		* Set the icon and title using
//					  information from the main application.
// 2004-01-04	SAM, RTi		* Comment out Help button - enable later
//					  if a better help system is
//					  implemented.
// 2004-02-24	J. Thomas Sapienza, RTi	Added getReferenceGraph().
// 2004-05-03	JTS, RTi		* Added shouldClose() to check whether
//					  the GUI can be closed.
//					* Renamed save() to saveGraph().
//					* Save now checks for any registered
//					  TSProductDMIs and allows tsproducts
//					  to be written through them.
// 2004-08-06	JTS, RTi		Enabled saving to PNGs.
// 2005-07-06	JTS, RTi		Changed the save dialog text for when 
//					saving through a TSProductDMI.
// 2005-07-13	JTS, RTi		TSProductDMIs are now stored in the
//					parent TSViewJFrame rather than within
//					this class.
// 2005-07-14	JTS, RTi		When saving as a TSP, the extension is
//					now enforced.  Same for DV and TXT
//					files.
// 2005-08-04	SAM, RTi		Clean up the wording on the save warning
//					and use more standard dialog buttons.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.GRTS;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import RTi.DMI.DMI;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRShape;
import RTi.TS.DateValueTS;
import RTi.TS.TS;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.SimpleJToggleButton;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Help.URLHelp;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
The TSViewGraphJFrame displays a graph of one or more time series, and is
managed by the parent TSViewJFrame.  See the constructor documentation for more information.
*/
@SuppressWarnings("serial")
public class TSViewGraphJFrame extends JFrame
implements ActionListener, ItemListener, TSViewListener, WindowListener
{

/**
TSViewJFrame parent.
TODO SAM 2008-01-11 Describe whether new JFrame() can be used as parent for batch processing.
*/
private TSViewJFrame __tsview_JFrame;

/**
List of time series to graph.
*/
private List<TS> __tslist;

// TODO SAM 2008-01-11 Need to describe the properties.
/**
Property list (old-style).  See also the TSViewJFrame constructor properties.
*/
private PropList __props;

/**
TSProduct defining the graph(s).
*/
private TSProduct __tsproduct = null;

/**
GUI for detail information.
*/
private ReportJFrame __detail_JFrame = null;

protected TSGraphJComponent _ts_graph = null;
protected TSGraphJComponent _ref_graph = null;

private TSGraphJComponentGlassPane tsgraphGlassPane = null;

private SimpleJButton __close_JButton = null;
private SimpleJButton __help_JButton = null;
private SimpleJButton __save_JButton = null;
private SimpleJButton __save_edits_JButton = null;
private SimpleJButton __summary_JButton = null;
private SimpleJButton __print_JButton = null;
private SimpleJButton __table_JButton = null;
private SimpleJButton __zoom_out_JButton = null;
private SimpleJButton zoomXPrevious_JButton = null;
private SimpleJButton zoomXNext_JButton = null;
private SimpleJButton __mode_JButton = null;

private SimpleJComboBox __trackerModeJComboBox = null;

private SimpleJButton __left_tostart_JButton = null;
private SimpleJButton __left_page_JButton = null;
private SimpleJButton __left_halfpage_JButton = null;
private SimpleJButton __right_toend_JButton = null;
private SimpleJButton __right_page_JButton = null;
private SimpleJButton __right_halfpage_JButton = null;

/**
Display for status messages.
*/
private JTextField __message_JTextField = null;

/**
Display mouse position.
*/
private JTextField __tracker_JTextField = null;

private JToolBar _toolbar;
/** Controls whether in Edit mode */
private SimpleJToggleButton _edit_JToogleButton;
/** Controls whether in Zoom mode (default)*/
private SimpleJToggleButton _zoom_JToogleButton;
/** Use interpolation to adjust data points */
private SimpleJButton _fillInterpolation_JButton = null;
/** Encapsulates (most) editing functionality */
private TSGraphEditor _tsGraphEditor;
/** Controls whether auto-connect is used during editing*/
private JCheckBox _autoConnect_JCheckBox;

/**
Construct a TSViewGraphJFrame.
@param tsview_gui Parent TSViewJFrame.
@param tslist List of time series to view.
@param props Properties to customize the data display.  The properties are
defined in the TSViewJFrame constructor documentation.
@exception Exception if there is an error displaying the view.
*/
public TSViewGraphJFrame ( TSViewJFrame tsview_gui, List<TS> tslist, PropList props )
throws Exception
{	super ( "Time Series - Graph View" );
	initialize ( tsview_gui, tslist, props );
}

/**
Construct a TSViewGraphJFrame.
@param tsview_gui Parent TSViewJFrame.
@param tsproduct TSProduct defining the graph(s).
@exception Exception if there is an error displaying the view.
*/
public TSViewGraphJFrame ( TSViewJFrame tsview_gui, TSProduct tsproduct )
throws Exception
{	super ( "Time Series - Graph View" );
	// Set before calling initialize()...
	__tsproduct = tsproduct;
	// Set a property to make sure the centering works
	PropList props = new PropList("TSViewJFrame");
	// TODO SAM 2016-04-01 This needs to use the TSTool frame, not the hidden frame?
	if ( tsview_gui != null ) {
		props.setUsingObject("TSViewParentUIComponent",tsview_gui);
	}
	if ( (tsproduct == null) || (tsproduct.getTSList() == null) ) {
		initialize ( tsview_gui, new ArrayList<TS>(), props );
	}
	else {
		initialize ( tsview_gui, tsproduct.getTSList(), props );
	}
}

/**
Handle action events.
@param event ActionEvent.
*/
public void actionPerformed ( ActionEvent event )
{	Object o = event.getSource();
    if (o == _edit_JToogleButton) {
      _ts_graph.setDisplayCursor(true);
      _ts_graph.setInteractionMode(TSGraphJComponent.INTERACTION_EDIT);
      _fillInterpolation_JButton.setEnabled(true);
      _autoConnect_JCheckBox.setEnabled(true);
      _autoConnect_JCheckBox.setSelected(true);
      _tsGraphEditor.setAutoConnect(true);
    }
    else if (o == _zoom_JToogleButton) {
      _ts_graph.setDisplayCursor(false);
      _ts_graph.setInteractionMode(TSGraphJComponent.INTERACTION_ZOOM);
      _fillInterpolation_JButton.setEnabled(false);
      _autoConnect_JCheckBox.setEnabled(false);
    }
    else if (o == _autoConnect_JCheckBox) {
      _tsGraphEditor.setAutoConnect(((JCheckBox)o).isSelected());
    }
    else if (o == _fillInterpolation_JButton) {
      _tsGraphEditor.doFillWithInterpolation();
      _ts_graph.refresh(false);
    }
    else if ( o == __left_tostart_JButton ) {
		_ts_graph.scrollToStart ( true );
	}
	else if ( o == __left_page_JButton ) {
		_ts_graph.scroll ( -1.0, true );
	}
	else if ( o == __left_halfpage_JButton ) {
		_ts_graph.scroll ( -.5, true );
	}
	else if ( o == __right_toend_JButton ) {
		_ts_graph.scrollToEnd ( true );
	}
	else if ( o == __right_page_JButton ) {
		_ts_graph.scroll ( 1.0, true );
	}
	else if ( o == __right_halfpage_JButton ) {
		_ts_graph.scroll ( .5, true );
	}
	else if ( o == __close_JButton ) {
		// Close the detail...
		closeDetail ();
		// Close the GUI via the parent...
		__tsview_JFrame.closeGUI(TSViewType.GRAPH);
	}
	else if ( o == __help_JButton ) {
		// Show help...
		URLHelp.showHelpForKey ("TSView.Graph");
	}
	else if ( o == __help_JButton ) {
		// Set the interaction mode to select or zoom...
		if ( __mode_JButton.getText().equals("Change to Select Mode")) {
			// In zoom mode so toggle to select...
			__mode_JButton.setText( "Change to Zoom Mode" );
			setInteractionMode ( TSGraphJComponent.INTERACTION_SELECT );
			__message_JTextField.setText ( "Select Mode");
		}
		else {	// In select mode so toggle to zoom...
			__mode_JButton.setText( "Change to Select Mode" );
			setInteractionMode (TSGraphJComponent.INTERACTION_ZOOM);
/* FIXME SAM 2008-02-21
			if (	(_ts_graph.getGraphType() ==
				TSGraphJComponent.GRAPH_TYPE_DOUBLE_MASS) ||
				(_ts_graph.getGraphType() ==
 				TSGraphJComponent.GRAPH_TYPE_DURATION) ||
				(_ts_graph.getGraphType() ==
				TSGraphJComponent.GRAPH_TYPE_XY_SCATTER) ) {
				__message_JTextField.setText (
					"Zoom Mode Disabled" );
			}
			else {	__message_JTextField.setText ( "Zoom Mode");
			}
*/
			__message_JTextField.setText ( "Zoom Mode");
		}
	}
	else if ( o == __print_JButton ) {
		// Print the graph...
		if ( _ts_graph != null ) {
			try {
			    _ts_graph.printGraph ();
			}
			catch ( Exception e ) {
				Message.printWarning ( 1, "TSViewGraphJFrame.actionPerformed", "Error printing graph." );
				Message.printWarning ( 2, "TSViewGraphJFrame.actionPerformed", e );
			}
		}
	}
	else if ( o == __save_JButton ) {
		saveGraph();
	}
	else if ( o == __save_edits_JButton ) {
	    saveEdits();
	}
	else if ( o == __summary_JButton ) {
		// Display a summary...
		__tsview_JFrame.openGUI ( TSViewType.SUMMARY );
	}
	else if ( o == __table_JButton ) {
		// Display a table...
		__tsview_JFrame.openGUI ( TSViewType.TABLE );
		TSViewTableJFrame tsViewTableJFrame = __tsview_JFrame.getTSViewTableJFrame();
		_tsGraphEditor.addPropertyChangeListener(tsViewTableJFrame);
	}
	else if ( o == __zoom_out_JButton ) {
		// Zoom out to original extents...
		_ts_graph.zoomOut ();
		if ( _ref_graph != null ) {
			_ref_graph.zoomOut ();
		}
		// Also save an entry in the zoom history
		List<TSGraph> tsgraphs = _ref_graph.getTSGraphs();
		// Reference graph should be first and only in list
		if ( tsgraphs.size() >= 1 ) {
			TSGraph graph = tsgraphs.get(0);
			_ref_graph.getReferenceGraphZoomHistory().add(graph.getDataLimits());
		}
	}
	else if ( o == zoomXNext_JButton ) {
		// Zoom to next X zoom
		GRLimits newZoom = _ref_graph.getReferenceGraphZoomHistory().next();
		if ( newZoom != null ) {
			_ref_graph.zoom(newZoom);
		}
		checkGUIState();
	}
	else if ( o == zoomXPrevious_JButton ) {
		// Zoom to previous X zoom
		GRLimits newZoom = _ref_graph.getReferenceGraphZoomHistory().previous();
		if ( newZoom != null ) {
			_ref_graph.zoom(newZoom);
		}
		checkGUIState();
	}
}

/**
 * Add a component to the LayeredPane that is used to manage the TSGraphJComponet and
 * transparent glass pane for the mouse tracker.
 * @param container
 * @param component
 * @param gridx
 * @param gridy
 * @param gridwidth
 * @param gridheight
 * @param weightx
 * @param weighty
 * @param insets
 * @param fill
 * @param anchor
 */
private void addLayeredPaneComponent ( JLayeredPane container, Component component, Integer z ) {
	// The following was copied from GUIUtil.addComponent(), but added Z-level here.
	//LayoutManager lm = container.getLayout();
	int gridx = 0;
	int gridy = 0;
	int gridwidth = 1;
	int gridheight = 1;
	double weightx = 1.0;
	double weighty = 1.0;
	//Insets insets;
	int fill = GridBagConstraints.BOTH;
	int anchor = GridBagConstraints.CENTER;
	//
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gridx;
    gbc.gridy = gridy;
    gbc.gridwidth = gridwidth;
    gbc.gridheight = gridheight;
    gbc.weightx = weightx;
    gbc.weighty = weighty;
    gbc.insets = new Insets(0,0,0,0);
    gbc.fill = fill;
    gbc.anchor = anchor;
    //((GridBagLayout)lm).setConstraints(component, gbc);
    //container.add (component, z);
    container.add (component, gbc, z);
}

/**
 * Check the GUI state, to enable disable buttons and other features.
 */
private void checkGUIState () {
	// Main buttons on interface such as zooming
	if ( this._ref_graph != null ) {
		// If there is anything in the zoom history, enable buttons accordingly
		if ( this._ref_graph.getReferenceGraphZoomHistory().getNextZoom() == null ) {
			this.zoomXNext_JButton.setEnabled(false);
		}
		else {
			this.zoomXNext_JButton.setEnabled(true);
		}
		if ( this._ref_graph.getReferenceGraphZoomHistory().getPreviousZoom() == null ) {
			this.zoomXPrevious_JButton.setEnabled(false);
		}
		else {
			this.zoomXPrevious_JButton.setEnabled(true);
		}
	}
}

/**
Close the detail GUI.  This should be called before calling
TSViewJFrame.closeGUI() to close this graph GUI.
*/
private void closeDetail ()
{	// If the detail GUI is not null, close it.  It is possible that the
	// GUI will have been closed within the ReportGUI, but the try/catch should handle if so.
	if ( __detail_JFrame == null ) {
		return;
	}
	try {
	    __detail_JFrame.setVisible ( false );
		__detail_JFrame.dispose();
		__detail_JFrame = null;
	}
	catch ( Exception e ) {
	}
}

/**
Return the main TSGraphJComponent.
@return the main TSGraphJComponent.
*/
public TSGraphJComponent getMainJComponent ()
{	return _ts_graph;
}

/**
Get a controlling property value for the display.
This method handles whether properties come from the TSProduct or other property list.
@param propname property name (e.g., "EnableReferenceGraph").
@return String value of the property.
*/
private String getPropValue ( String propname )
{
    if ( __tsproduct == null ) {
        return __props.getValue(propname);
    }
    else {
        return __tsproduct.getPropValue(propname);
    }
}

/**
Returns the reference graph.
@return the reference graph.
*/
protected TSGraphJComponent getReferenceGraph() {
	return _ref_graph;
}

/**
Return the TSViewJFrame instance.
*/
public final TSViewJFrame getTSViewJFrame()
{	return __tsview_JFrame;
}

/**
Initialize data and open the GUI.
@param tsview_gui Parent TSViewJFrame.
@param tslist List of time series to view.
@param props Properties to customize the data display.  The properties are
defined in the TSViewJFrame constructor documentation.
*/
private void initialize (TSViewJFrame tsview_gui,List<TS> tslist, PropList props )
{	__tsview_JFrame = tsview_gui;
	__tslist = tslist;
	__props = props;
	// Used to set the menu bar...

	//if ( includeFullCode ) {
	String prop_value = null;
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	
	// TODO SAM 2011-11-29 Need to figure out how to let TSProduct file properties set the window title
	if (__tsproduct == null) {
		prop_value = __props.getValue("TSViewTitleString");
        if (__tsproduct == null) {
            prop_value = __props.getValue("Product.TSViewTitleString");
        }
	}
	else {
		prop_value = __tsproduct.getPropValue("TSViewTitleString");
		if (__tsproduct == null) {
		    prop_value = __tsproduct.getPropValue("Product.TSViewTitleString");
		}
	}

	if ( prop_value == null ) {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Time Series - Graph" );
		}
		else {
		    setTitle( JGUIUtil.getAppNameForWindows() +	" - Time Series - Graph" );
		}
	}
	else {
	    if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( prop_value + " - Graph" );
		}
		else {
		    setTitle( JGUIUtil.getAppNameForWindows() + " - " +	prop_value + " - Graph" );
		}
	}
	//} // includeFullCode

	openGUI ( true );
}

/**
 * Handle ItemEvents, for example from comboboxes.
 */
public void itemStateChanged ( ItemEvent evt ) {
	Object o = evt.getItemSelectable();
	
	if ( (o == __trackerModeJComboBox) && (evt.getStateChange() == ItemEvent.SELECTED) ) {
		// Mouse tracker was changed so change in the drawing component
		this.tsgraphGlassPane.setMouseTrackerType (
			TSGraphMouseTrackerType.valueOfIgnoreCase(__trackerModeJComboBox.getSelected()));
	}
}

/**
Indicate whether the graph needs to be closed due to start-up problems.
This will be the case, for example, if time series are incompatible for plotting
and the user indicates not to continue.  This should be called by the parent
code after a TSViewGraphFrame is constructed.
*/
public boolean needToClose () {
	if ( _ts_graph == null ) {
		// Should not normally happen but need for some testing configurations
		return false;
	}
	else {
		return _ts_graph.needToClose();
	}
}

/**
Open the GUI and display the time series.
@param mode Indicates whether the GUI should be visible at creation, typically true
unless processing a graph in batch mode, in which case an image file is usually the output.
*/
private void openGUI ( boolean mode )
{	String routine = "TSViewGraphJFrame.openGUI";
	int	y = 0;

	// Start a big try block to set up the GUI...
	try {

	// Add a listener to catch window manager events (close window, etc.)...

	addWindowListener ( this );

	// Lay out the main window component by component.
	// Start with the menubar default components.
	// Then add each requested component to the menu bar and the interface.

	GridBagLayout gbl = new GridBagLayout();

	//Insets insetsTLBR = new Insets ( 3, 3, 3, 3 );// space around components
	Insets insetsTLBR = new Insets ( 1, 3, 1, 3 ); // space around component
	Insets insetsNone = new Insets ( 0, 0, 0, 0 ); // no space around component, for surrounding the drawing JComponent
	
	// The drawing component will occupy the CENTER part of the BorderLayout.
	// Controls at the bottom will occupy the SOUTH part of the BoarderLayout.

	Container contentPane = getContentPane();
	contentPane.setLayout(new BorderLayout() );

	//if ( includeFullCode ) {
	_toolbar = new JToolBar();
	//main_JPanel.add ( "North", _toolbar);
	
	_edit_JToogleButton = new SimpleJToggleButton("Edit Mode", this, false);
	_edit_JToogleButton.setToolTipText("Edit graph mode");
	_toolbar.add(_edit_JToogleButton);
	_zoom_JToogleButton = new SimpleJToggleButton("Zoom Mode", this, true);
	_zoom_JToogleButton.setToolTipText("Zoom graph mode");
	_toolbar.add(_zoom_JToogleButton);
	
	ButtonGroup buttonGroup= new ButtonGroup();
	buttonGroup.add(_edit_JToogleButton);
	buttonGroup.add(_zoom_JToogleButton);
	
	_fillInterpolation_JButton = new SimpleJButton("FillInterpolate","FillInterpolate",this);
	//_toolbar.add(__fillInterpolation);
	
	_autoConnect_JCheckBox= new JCheckBox("Auto-Connect");
	_autoConnect_JCheckBox.setSelected(true);
	_autoConnect_JCheckBox.setEnabled(false);
	_autoConnect_JCheckBox.addActionListener(this);
	_autoConnect_JCheckBox.setToolTipText("Automatically connect points");
	_toolbar.add(_autoConnect_JCheckBox);

	// Display toolbar only when there are editable time series
	_toolbar.setVisible(TSUtil.areAnyTimeSeriesEditable(__tslist));
	TS tmp = TSUtil.getFirstEditableTS(__tslist);
	//TODO: dre tmp.setLegend("Editing:" + tmp.getLegend());
	//tmp.setLegend("EditMe");

	_tsGraphEditor = new TSGraphEditor(tmp);

	// For the reference graph, pick the time series with the longest
	// period of record for non-missing data.  Indicate the time series to
	// the main graph so a note can be made on the display (but not printed)...
	int size = __tslist.size();
	// Initialize to first time series...
	int max_period_index = 0;
	int max_period_months = 0;
	TS ts_i = null;
	int nmonths = 0;
	TSLimits limits = null;
	for ( int i = 0; i < size; i++ ) {
		ts_i = __tslist.get(i);
		if ( ts_i == null ) {
			continue;
		}
		limits = ts_i.getDataLimits ();
		if ( limits.areLimitsFound() ) {
			nmonths = limits.getNonMissingDataDate2().getAbsoluteMonth() -
			    limits.getNonMissingDataDate1().getAbsoluteMonth();
		}
		else {
		    // Limits are not found so try to use dates if they are available
		    if ( (ts_i.getDate1() == null) || (ts_i.getDate2() == null) ) {
		        // Cannot process the time series.
		        continue;
		    }
			nmonths = ts_i.getDate2().getAbsoluteMonth() - ts_i.getDate1().getAbsoluteMonth();
		}
		if ( nmonths > max_period_months ) {
			max_period_months = nmonths;
			max_period_index = i;
		}
	}

	// Main graph...

	if ( __tsproduct == null ) {
		// Old-style...
		__props.set ( "ReferenceTSIndex=" + max_period_index );
		_ts_graph = new TSGraphJComponent ( this, __tslist, __props );
	}
	else {
	    // New-style...
		PropList additional_props = new PropList ( "TSViewGraphJFrame");
		additional_props.set ( "ReferenceTSIndex=" + max_period_index );
		__tsproduct.setTSList ( __tslist );
		_ts_graph = new TSGraphJComponent ( this, __tsproduct, additional_props );
	}
    _ts_graph.setEditor(_tsGraphEditor);
	//} // includeFullCode
    //int layoutType = 0; // This works, but is a pain to forward mouse events to underlying components
    //int layoutType = 2;
    int layoutType = 3; // Now this works
    // TODO sam 2017-02-22 Figure out why only layoutType=0 is only one that works
    boolean doFrameGlassPane = true; // Default is to use glass pane on JFrame, but layout 2 uses JLayeredPane instead
    JLayeredPane layeredPane = null;
    JPanel panelForTSGraphJComponent = null;
    JPanel panelForJLayeredPane = null;
    if ( layoutType == 0 ) {
    	// Legacy layout - this works, and will add the glass pane over the frame
    	contentPane.add ( _ts_graph, BorderLayout.CENTER );
    	//TestCanvas _testCanvas = new TestCanvas(500,300);
    	//contentPane.add ( _testCanvas, BorderLayout.CENTER );
    	doFrameGlassPane = true; // Glass pane is added below
    }
    else if ( layoutType == 1 ) {
    	// Try the approach of:
    	// - adding the graph to a JPanel/GridBagLaout
    	// - add the above to JLayeredPane
    	// - adding the JLayeredPane to a panel that uses GridBagLayout to fill the space
    	// - adding the above to content pane BorderLayout CENTER
    	// This does not work - panel is zero size at initialization and does not force graph to resize
    	if ( Message.isDebugOn ) {
	        Message.printStatus(2,routine,"TSGraphJComponent size before laying out in window, width="
	            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
    	}
    	layeredPane = new JLayeredPane ();
    	// Set the preferred size of the JLayeredPane to be the same as the TSGraphJComponent (since it sets its own preferred size)
    	layeredPane.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
    	//_tsGraphGlassPane = new TSGraphJComponentGlassPane(_ts_graph, _ts_graph);
    	//layeredPane.add(_tsGraphGlassPane,2);
        panelForTSGraphJComponent = new JPanel();
        panelForTSGraphJComponent.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
        panelForTSGraphJComponent.setLayout(gbl);
    	JGUIUtil.addComponent ( panelForTSGraphJComponent, _ts_graph,
        	0, 0, 1, 1, 1.0, 1.0, insetsNone, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    	if ( Message.isDebugOn ) {
	    	Message.printStatus(2,routine,"TSGraphJComponent size after adding to panel, width="
	            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
    	}
    	layeredPane.add(panelForTSGraphJComponent,1); // TSGraphJComponent calls setPreferredSize()
        // Fill the whole panel and allow resize in both directions
    	panelForJLayeredPane = new JPanel();
    	panelForJLayeredPane.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
    	panelForJLayeredPane.setLayout(gbl);
    	JGUIUtil.addComponent ( panelForJLayeredPane, layeredPane,
    		0, 0, 1, 1, 1.0, 1.0, insetsNone, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    	if ( Message.isDebugOn ) {
	    	Message.printStatus(2,routine,"TSGraphJComponent size after JLayeredPane added to panel, width="
	            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
    	}
    	// This should automatically resize to fill
    	contentPane.add ( panelForJLayeredPane, BorderLayout.CENTER );
    	if ( Message.isDebugOn ) {
	    	Message.printStatus(2,routine,"TSGraphJComponent size after JLayeredPane added to contentPane, width="
	            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
    	}
    	doFrameGlassPane = false;
    }
    else if ( layoutType == 2 ) {
    	// Try the approach of adding the JLayeredPane to the center panel of the content pane
    	// and then the graph goes in a JPanel that uses GridBagLayout to fill the space.
    	// TSGraphJComponent (_ts_graph) calls setPreferredSize().
    	// This does not work - panel is zero size at initialization and does not force graph to resize
        panelForTSGraphJComponent = new JPanel();
        panelForTSGraphJComponent.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
        panelForTSGraphJComponent.setLayout(gbl);
        // Fill the whole panel and allow resize in both directions
    	JGUIUtil.addComponent ( panelForTSGraphJComponent, _ts_graph,
    		0, 0, 1, 1, 1.0, 1.0, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
    	layeredPane = new JLayeredPane ();
    	layeredPane.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
    	layeredPane.add(panelForTSGraphJComponent,new Integer(JLayeredPane.DEFAULT_LAYER));
    	//_tsGraphGlassPane = new TSGraphJComponentGlassPane(_ts_graph, _ts_graph);
    	//layeredPane.add(_tsGraphGlassPane,2);
    	// This should automatically resize to fill
    	contentPane.add ( layeredPane, BorderLayout.CENTER );
    	doFrameGlassPane = false;
    }
    else if ( layoutType == 3 ) {
    	// Try the approach of adding the JLayeredPane to the center panel of the content pane
    	// and then the graph goes in a JPanel that uses GridBagLayout to fill the space.
    	// TSGraphJComponent (_ts_graph) calls setPreferredSize().
    	// This does not work - panel is zero size at initialization and does not force graph to resize
    	layeredPane = new JLayeredPane ();
    	layeredPane.setLayout(gbl);
    	//_ts_graph.setOpaque(false); // Doesn't do anything?
    	//_ts_graph.setBackground(new Color(0,0,0,0));
    	addLayeredPaneComponent ( layeredPane, _ts_graph, new Integer(JLayeredPane.DEFAULT_LAYER) );
    	// The above sets the horizontal layout but the following ensures vertical ordering in layer (1=bottom)
    	layeredPane.setPosition(_ts_graph, 1);
    	//JPanel testJPanel = new JPanel();
    	//testJPanel.setOpaque(false);
    	//addLayeredPaneComponent ( layeredPane, testJPanel, new Integer(JLayeredPane.DEFAULT_LAYER + 1) );
    	//layeredPane.setPreferredSize(new Dimension(_ts_graph.getWidth(),_ts_graph.getHeight()));
    	//layeredPane.add(panelForTSGraphJComponent,1);
    	//_tsGraphGlassPane = new TSGraphJComponentGlassPane(_ts_graph, layeredPane.getRootPane());
    	this.tsgraphGlassPane = new TSGraphJComponentGlassPane(_ts_graph, null);
    	addLayeredPaneComponent ( layeredPane, this.tsgraphGlassPane, new Integer(JLayeredPane.DEFAULT_LAYER) );
    	// The above sets the horizontal layout but the following ensures vertical ordering in layer (0=top)
    	//layeredPane.setLayer(_tsGraphGlassPane, JLayeredPane.DEFAULT_LAYER, 0);
    	layeredPane.setPosition(this.tsgraphGlassPane, 0);
    	// This should automatically resize to fill
    	contentPane.add ( layeredPane, BorderLayout.CENTER );
    	// Don't do a glass pane on the frame because only doing on the LayeredPane
    	doFrameGlassPane = false;
    }
    
    // If the following is true, a glass frame component is used over the entire JFrame,
    // for use in mouse tracking, etc.
    if ( doFrameGlassPane ) {
		// Add a glass pane over the entire parent JFrame
    	this.tsgraphGlassPane = new TSGraphJComponentGlassPane(_ts_graph,contentPane);
    	//JRootPane root = SwingUtilities.getRootPane(this);
    	//root.setGlassPane(_tsGraphGlassPane);
    	setGlassPane(this.tsgraphGlassPane); // This must be done before setting visible
    	//glassPane.setOpaque(true);
    	//glassPane.setOpaque(false);
    	this.tsgraphGlassPane.setVisible(true);
    }
	
	// Panel to hold the reference graph and buttons (to maintain spatial ordering)...
    //if ( includeFullCode ) {
	JPanel bottom_JPanel = new JPanel ();
	bottom_JPanel.setLayout ( gbl );

	// Reference graph...
	String prop_value=null;
	if ( __tsproduct == null ) {
		prop_value = __props.getValue("EnableReferenceGraph");
	}
	else {
	    prop_value = __tsproduct.getPropValue("EnableReferenceGraph");
	}

	// See if any of the graphs can use a reference graph.  If not, turn
	// off (regardless of what property says)...
	if ( !_ts_graph.canUseReferenceGraph() ) {
		prop_value = "false";
	}
	if ( !needToClose() && ((prop_value == null)|| !prop_value.equalsIgnoreCase("false"))){
		// Want a reference graph...
		JPanel reference_JPanel = new JPanel ();
		reference_JPanel.setLayout ( gbl );
		PropList ref_props = new PropList ( "reference" );
		// New set of properties for reference graph...
		ref_props.set ( "GraphType", "Line" );
		ref_props.set ( "MaximizeGraphSpace", "true" );
		ref_props.set ( "ReferenceGraph", "true" );
		ref_props.set ( "ReferenceTSIndex=" + max_period_index );
		if ( __tsproduct == null ) {
			// Old style...
			_ref_graph = new TSGraphJComponent ( this, __tslist, ref_props );
		}
		else {
		    // New style...
			__tsproduct.setTSList ( __tslist );
			_ref_graph = new TSGraphJComponent ( this, __tsproduct, ref_props );
		}
		// Width is the same as the main graph, height is hard-coded...
		Dimension graph_size = _ts_graph.getSize();
		_ref_graph.setSize ( graph_size.width, 15 );
		// Swing seems to need this...
		_ref_graph.setPreferredSize(new Dimension(graph_size.width,15));
		// Seems to be needed to prevent the graph from shrinking to
		// zero height, since the reference graph is not sized in the
		// TSGraphJComponent itself...
		_ref_graph.setMinimumSize(new Dimension(10,15));
		// Only fill horizontally...
		y++;
		JGUIUtil.addComponent (reference_JPanel,
				new JLabel("Visible Period (white):"),
				0, 0, 1, 1, 0.0, 0.0,
				insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
		JGUIUtil.addComponent ( reference_JPanel, _ref_graph,
				1, 0, 1, 1, 1.0, 0.0,
				insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );
		JGUIUtil.addComponent ( bottom_JPanel, reference_JPanel,
				0, 1, 1, 1, 1.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER );
		// Let this JFrame listen to the reference map (for mouse coordinate label)...
		_ref_graph.addTSViewListener ( this );
		// Let the main graph listen to the reference graph (for zooming, etc.)...
		_ref_graph.addTSViewListener ( _ts_graph );
		// Let the reference graph listen to itself so it can redraw its reference box...
		// TODO SAM 2010-09-07 not needed anymore?
		//_ref_graph.addTSViewListener ( _ref_graph );
		// Add the initial graph data limits to the zoom history
		List<TSGraph> tsgraphs = _ref_graph.getTSGraphs();
		// Reference graph should be first and only in list
		if ( tsgraphs.size() >= 1 ) {
			TSGraph graph = tsgraphs.get(0);
			_ref_graph.getReferenceGraphZoomHistory().add(graph.getDataLimits());
		}
	}

	// Listeners...

	// Let this Frame listen to the main graph (for mouse coordinate label)...
	_ts_graph.addTSViewListener ( this );
	// Let the reference graph listen to the main graph (for zooming, etc.)...
	_ts_graph.addTSViewListener ( _ref_graph );

	// Put the buttons on the bottom of the window...

	JPanel button_top_JPanel = new JPanel ();
	button_top_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

	JPanel button_bottom_JPanel = new JPanel ();
	button_bottom_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

	if ( __tsproduct == null ) {
		prop_value=__props.getValue("EnableSummary");
	}
	else {
	    prop_value=__tsproduct.getPropValue("EnableSummary");
	}
	if ( (prop_value == null) || ((prop_value != null) &&!prop_value.equalsIgnoreCase("false"))){
		// Add the button...
		__summary_JButton = new SimpleJButton("Summary", "TSViewGraphJFrame.Summary", this );
		button_bottom_JPanel.add ( __summary_JButton );
	}

	if ( __tsproduct == null ) {
		prop_value=__props.getValue("EnableTable");
	}
	else {
	    prop_value=__tsproduct.getPropValue("EnableTable");
	}
	if ( (prop_value == null) || ((prop_value != null) &&!prop_value.equalsIgnoreCase("false"))){
		__table_JButton = new SimpleJButton("Table", "TSViewGraphJFrame.Table", this);
		button_bottom_JPanel.add ( __table_JButton );
	}

	// Buttons to allow paging left and right...

	__left_tostart_JButton = new SimpleJButton("|<", "TSViewGraphJFrame.Left3", this);
	__left_tostart_JButton.setToolTipText("Scroll to start of period");
	button_top_JPanel.add ( __left_tostart_JButton );

	__left_page_JButton = new SimpleJButton("<<", "TSViewGraphJFrame.Left2", this);
	__left_page_JButton.setToolTipText("Scroll left one \"page\"");
	button_top_JPanel.add ( __left_page_JButton );

	__left_halfpage_JButton = new SimpleJButton("<", "TSViewGraphJFrame.Left1", this);
	__left_halfpage_JButton.setToolTipText("Scroll left one-half \"page\"");
	button_top_JPanel.add ( __left_halfpage_JButton );

	__right_halfpage_JButton = new SimpleJButton(">", "TSViewGraphJFrame.Right1", this);
	__right_halfpage_JButton.setToolTipText("Scroll right one-half \"page\"");
	button_top_JPanel.add ( __right_halfpage_JButton );

	__right_page_JButton = new SimpleJButton(">>", "TSViewGraphJFrame.Right2", this);
	__right_page_JButton.setToolTipText("Scroll right one \"page\"");
	button_top_JPanel.add ( __right_page_JButton );

	__right_toend_JButton = new SimpleJButton(">|", "TSViewGraphJFrame.Right3", this);
	__right_toend_JButton.setToolTipText("Scroll to end of period");
	button_top_JPanel.add ( __right_toend_JButton );

	// Zoom out button to restore original zoom...

	__zoom_out_JButton = new SimpleJButton("Zoom Out", "TSViewGraphJFrame.ZoomOut", this);
	__zoom_out_JButton.setToolTipText("Zoom to full period");
	button_top_JPanel.add ( __zoom_out_JButton );
	__zoom_out_JButton.setEnabled ( true );
	
	zoomXPrevious_JButton = new SimpleJButton("<x", "TSViewGraphJFrame.ZoomXPrevious", this);
	zoomXPrevious_JButton.setToolTipText("Zoom to previous X-axis extent");
	button_top_JPanel.add ( zoomXPrevious_JButton );
	zoomXPrevious_JButton.setEnabled ( false );
	// TODO SAM 2017-04-24 need to fully enable
	zoomXPrevious_JButton.setVisible ( false );
	
	zoomXNext_JButton = new SimpleJButton("x>", "TSViewGraphJFrame.ZoomXNext", this);
	zoomXNext_JButton.setToolTipText("Zoom to next X-axis extent");
	button_top_JPanel.add ( zoomXNext_JButton );
	zoomXNext_JButton.setEnabled ( false );
	// TODO SAM 2017-04-24 need to fully enable
	zoomXNext_JButton.setVisible ( false );
	
	// Add choices for tracker behavior
	
	__trackerModeJComboBox = new SimpleJComboBox(false);
	__trackerModeJComboBox.setToolTipText("Indicate how the mouse tracker should behave, for graphs that support the tracker");
	__trackerModeJComboBox.add("" + TSGraphMouseTrackerType.NEAREST);
	__trackerModeJComboBox.add("" + TSGraphMouseTrackerType.NEAREST_SELECTED);
	__trackerModeJComboBox.add("" + TSGraphMouseTrackerType.NEAREST_TIME);
	__trackerModeJComboBox.add("" + TSGraphMouseTrackerType.NEAREST_TIME_SELECTED);
	__trackerModeJComboBox.add("" + TSGraphMouseTrackerType.NONE);
	__trackerModeJComboBox.select("" + TSGraphMouseTrackerType.NONE);
	__trackerModeJComboBox.addItemListener(this);
	button_top_JPanel.add ( new JLabel("Tracker:") );
	button_top_JPanel.add ( __trackerModeJComboBox );

	// Disable zooming if the component has no graphs that can zoom...

	if ( !_ts_graph.canUseZoom() ) {
		__left_tostart_JButton.setEnabled(false);
		__left_page_JButton.setEnabled(false);
		__left_halfpage_JButton.setEnabled(false);
		__right_halfpage_JButton.setEnabled(false);
		__right_page_JButton.setEnabled(false);
		__right_toend_JButton.setEnabled(false);
		__zoom_out_JButton.setEnabled(false);
		zoomXPrevious_JButton.setEnabled(false);
		zoomXNext_JButton.setEnabled(false);
	}

	// Default the mode to select initially (so user can change to zoom mode if they want)...

/* Comment out for now since we need the space...
	__mode_JButton = new SimpleJButton("Change to Select Mode",
		"TSViewGraphJFrame.Mode", this);
	button_top_JPanel.add ( __mode_JButton );
	// Don't enable until it actually does something...
	//__mode_JButton.setEnabled ( true );
	__mode_JButton.setEnabled ( false );

*/
	setInteractionMode ( TSGraphJComponent.INTERACTION_ZOOM );
/*
	if (	(_ts_graph.getGraphType() ==
		TSGraphJComponent.GRAPH_TYPE_DOUBLE_MASS) ||
		(_ts_graph.getGraphType() ==
		TSGraphJComponent.GRAPH_TYPE_DURATION) ||
		(_ts_graph.getGraphType() ==
		TSGraphJComponent.GRAPH_TYPE_XY_SCATTER) ){
		_mode_Button.setEnabled(false);
	}
*/

/* TODO - add later when actually in use
	__annotate_JButton = new SimpleJButton("Annotate",
		"TSViewGraphJFrame.Annotate", this);
	button_top_JPanel.add ( __annotate_JButton );
	__annotate_JButton.setEnabled ( false );
*/

	__help_JButton =new SimpleJButton("Help","TSViewGraphJFrame.Help",this);
	// TODO - add later if better help system is enabled.
	//button_bottom_JPanel.add ( __help_JButton );

	__print_JButton = new SimpleJButton( "Print","TSViewGraphJFrame.Print",this);
	button_bottom_JPanel.add ( __print_JButton );

	__save_JButton = new SimpleJButton( "Save","TSViewGraphJFrame.Save",this);
	button_bottom_JPanel.add ( __save_JButton );
	
	if ( TSUtil.areAnyTimeSeriesEditable(__tslist) ) {
	    __save_edits_JButton = new SimpleJButton( "Save Edits","TSViewGraphJFrame.SaveEdits",this);
	    button_bottom_JPanel.add ( __save_edits_JButton );
	    String DefaultSaveFile = getPropValue ( "DefaultSaveFile");
	    if ( DefaultSaveFile != null ) {
	        __save_edits_JButton.setToolTipText ( "Save editable time series to: \"" + DefaultSaveFile + "\"");
	    }
	}

	__close_JButton = new SimpleJButton( "Close","TSViewGraphJFrame.Close",this);
	button_bottom_JPanel.add ( __close_JButton );

	//add ( "South", button_JPanel );
	++y;
	JGUIUtil.addComponent ( bottom_JPanel, button_top_JPanel,
		0, 2, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER );

	++y;
	JGUIUtil.addComponent ( bottom_JPanel, button_bottom_JPanel,
		0, 3, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.SOUTH );
	
	// Add panel for status and tracker window...

	++y;
	JPanel status_JPanel = new JPanel ();
	status_JPanel.setLayout ( gbl );
	__message_JTextField = new JTextField ();
	__message_JTextField.setEditable ( false );
	if ( _ts_graph.canUseZoom() ) {
		__message_JTextField.setText ( "Zoom Mode");
	}
	else {
	    __message_JTextField.setText ( "Zoom Mode Disabled");
	}
	JGUIUtil.addComponent ( status_JPanel, __message_JTextField,
			0, 0, 1, 1, .3, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	__tracker_JTextField = new JTextField (30);
	__tracker_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( status_JPanel, __tracker_JTextField,
			1, 0, 1, 1, .7, 1.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	++y;
	JGUIUtil.addComponent ( bottom_JPanel, status_JPanel,
		0, 4, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH );
	
	contentPane.add ( bottom_JPanel, BorderLayout.SOUTH );
    //} // includeFullCode

	if ( panelForTSGraphJComponent != null ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2,routine,"Graph panel size before calling pack() is width=" + panelForTSGraphJComponent.getWidth() + ", height=" + panelForTSGraphJComponent.getHeight() );
		}
	}
	if ( Message.isDebugOn ) {
		Message.printStatus(2,routine,"TSGraphJComponent size before calling pack(), width="
            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
	}
	pack ();
	if ( Message.isDebugOn ) {
		Message.printStatus(2,routine,"TSGraphJComponent size after calling pack(), width="
            + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
	}
	//if ( includeFullCode ) {
	// TODO SAM 2016-04-01 Need to set property in calling code so center works properly
	// Get the UI component to determine screen to display on - needed for multiple monitors
	if ( __props != null ) {
		// Get the UI component to determine screen to display on - needed for multiple monitors
		Object uiComponentO = __props.getContents( "TSViewParentUIComponent" );
		Component parentUIComponent = null;
		if ( (uiComponentO != null) && (uiComponentO instanceof Component) ) {
			parentUIComponent = (Component)uiComponentO;
		}
		// Center on the UI component rather than the graph, because the graph screen seems to get tied screen 0?
		JGUIUtil.center(this,parentUIComponent);
	}
	else {
		JGUIUtil.center ( this );
	}
	//} // includeFullCode

	//if ( includeFullCode) {
	if ( !needToClose() ) {
		setVisible ( mode );
	}
	if (mode) {
		toFront();
	}
	//} // includeFullCode
	//if ( !includeFullCode) {
		setVisible(true);
	//} // !includeFullCode
	
	// Clean up...
		if ( Message.isDebugOn ) {
			if ( this.tsgraphGlassPane != null ) {
				Message.printStatus(2,routine,"Glass pane width=" + this.tsgraphGlassPane.getWidth() + " height=" + this.tsgraphGlassPane.getHeight());
				if ( this.tsgraphGlassPane == getGlassPane() ) {
					Message.printStatus(2,routine,"Glass pane is set for JFrame");
				}
				Message.printStatus(2,routine,"Glass pane visible=" + this.tsgraphGlassPane.isVisible());
				Message.printStatus(2,routine,"Glass pane enabled=" + this.tsgraphGlassPane.isEnabled());
				//_tsGraphGlassPane.setPoint(new Point(_tsGraphGlassPane.getWidth()/2,_tsGraphGlassPane.getHeight()/2));
				//_tsGraphGlassPane.repaint();
				if ( panelForJLayeredPane != null ) {
					Message.printStatus(2,routine,"Layered pane panel size is width=" + panelForJLayeredPane.getWidth() + ", height=" + panelForJLayeredPane.getHeight() );
				}
				if ( layeredPane != null ) {
					Message.printStatus(2,routine,"Layered pane size is width=" + layeredPane.getWidth() + ", height=" + layeredPane.getHeight() );
				}
				if ( panelForTSGraphJComponent != null ) {
					Message.printStatus(2,routine,"Graph panel size is width=" + panelForTSGraphJComponent.getWidth() + ", height=" + panelForTSGraphJComponent.getHeight() );
				}
				if ( _ts_graph != null ) {
					Message.printStatus(2,routine,"TSGraphJComponent width=" + _ts_graph.getWidth() + ", height=" + _ts_graph.getHeight() );
				}
			}
		}

		if ( __tsproduct == null ) {
			// Should now have a TSProduct generated in TSGraphJComponent...
			if ( _ts_graph != null ) {
				__tsproduct = _ts_graph.getTSProduct();
			}
		}
	} // end of try
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
	}
}

/**
Refresh the graphs.  This should be called if time series properties have
changed (e.g., enable/disable time series in TSViewPropertiesFrame).
*/
public void refresh ()
{	_ts_graph.refresh();
	if ( _ref_graph != null ) {
		_ref_graph.refresh();
	}
}

/**
Save the editable time series.  This is generally configured programmatically
to allow users access to time series that can actually be edited (default is nothing
editable).  The button will only be available if time series are editable.
*/
private void saveEdits ()
{   String routine = "TSViewGraph.saveEdits";
    // FIXME SAM 2008-01-11 Need to enable a file chooser if the default is not specified
    // FIXME SAM 2008-01-11 Need to check the file extension to determine what format to write
    String DefaultSaveFile = getPropValue ( "DefaultSaveFile");
    if ( DefaultSaveFile == null ) {
        Message.printWarning ( 1, routine, "DefaultSaveFile property is not specified.  File chooser is not enabled.");
        return;
    }
    List<TS> editable_tslist = new Vector<TS>();
    String DefaultSaveFile_full = DefaultSaveFile;
    try {
        int size = 0;
        if ( __tslist != null ) {
            size = __tslist.size();
        }
        TS ts = null;
        for ( int i = 0; i < size; i++ ) {
            ts = __tslist.get(i);
            if ( ts == null ) {
                // Might have null time series in list.
                continue;
            }
            if ( ts.isEditable()) {
                editable_tslist.add ( ts );
            }
        }
        DefaultSaveFile_full = IOUtil.getPathUsingWorkingDir ( DefaultSaveFile );
        Message.printStatus ( 2, routine, "Saving " + editable_tslist.size() +
                " editable time series to DateValue file \"" + DefaultSaveFile_full );
        DateValueTS.writeTimeSeriesList(editable_tslist, DefaultSaveFile_full );
    }
    catch ( Exception e ) {
        Message.printWarning ( 1, routine, "Error saving " + editable_tslist.size() +
                " editable time series to \"" + DefaultSaveFile_full + "\".");
        Message.printWarning( 2, routine, e );
    }
}

/**
Save the graph in standard formats.  First prompt for the format and then
save.  The save can be canceled.
*/
private void saveGraph() {
	TSProductJFrame product = __tsview_JFrame.getTSProductJFrame();
	if (product != null) {
		product.applyClicked();
	}
	String routine = "TSViewGraphFrame.saveGraph";
	String last_directory = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc =JFileChooserFactory.createJFileChooser(last_directory);
	fc.setDialogTitle ( "Save Graph" );
	fc.setAcceptAllFileFilterUsed ( false );
	SimpleFileFilter dv_sff = new SimpleFileFilter("dv", "DateValue Time Series File" );
	fc.addChoosableFileFilter ( dv_sff );
	SimpleFileFilter txt_sff = new SimpleFileFilter ( "txt", "DateValue Time Series File" );
	fc.addChoosableFileFilter ( txt_sff );
	SimpleFileFilter jpg_sff = new SimpleFileFilter("jpg", "JPEG Image File" );
	fc.addChoosableFileFilter ( jpg_sff );
	SimpleFileFilter png_sff = new SimpleFileFilter("png", "PNG Image File" );
	fc.addChoosableFileFilter ( png_sff );
    SimpleFileFilter svg_sff = new SimpleFileFilter("svg", "Scalable Vector Graphics File" );
    // only enabled if batik jars on classpath
    if (TSGraphJComponent.svgEnabled) {
        fc.addChoosableFileFilter ( svg_sff );  
    }
	SimpleFileFilter tsp_sff = new SimpleFileFilter("tsp", "Time Series Product File" );
	fc.addChoosableFileFilter ( tsp_sff );

	List<TSProductDMI> tsProductDMIs = __tsview_JFrame.getTSProductDMIs();
	int size = tsProductDMIs.size();
	SimpleFileFilter[] dmiff = null;
	String s = null;
	if (size > 0) {
		dmiff = new SimpleFileFilter[size];
	 	TSProductDMI dmi = null;
		for (int i = 0; i < size; i++) {
			dmi = tsProductDMIs.get(i);
			s = "Time Series Product saved to " + dmi.getDMIName();
			if (dmi instanceof DMI) {
				if (!((DMI)dmi).getInputName().equals("")) {
					s += " (" + ((DMI)dmi).getInputName() + ")";
				}
			}
			dmiff[i] = new SimpleFileFilter(SimpleFileFilter.NA, s);
			fc.addChoosableFileFilter(dmiff[i]);
		}

		File file = new File(__tsproduct.getLayeredPropValue( "ProductID", -1, -1, false));
		fc.setSelectedFile(file);
	}
	// Always default the selection to a TSP file since that is the most common use
	fc.setFileFilter(tsp_sff);
		
	//-------------------------------
	/*
	SimpleFileFilter db_sff = new SimpleFileFilter("db", "Save to DataTable File");
	fc.addChoosableFileFilter ( db_sff );
	*/
	//-------------------------------
	if ( fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION ) {
		// Canceled...
		return;
	}
	// Else figure out the file format and location and then do the save...
	last_directory = fc.getSelectedFile().getParent();
	String path = fc.getSelectedFile().getPath();
	JGUIUtil.setLastFileDialogDirectory(last_directory);
	if ( (fc.getFileFilter() == dv_sff) || (fc.getFileFilter() == txt_sff) ) {
		if ( !TSUtil.intervalsMatch(__tslist) ) {
			Message.printWarning ( 1, routine, "Unable to write DateValue time series of different intervals." );
			return;
		}
		try {	
			if (fc.getFileFilter() == dv_sff) {
			    path = IOUtil.enforceFileExtension ( path, "dv" );
			}
			else if (fc.getFileFilter() == txt_sff) {
			    path = IOUtil.enforceFileExtension ( path, "txt" );
			}
			__tsview_JFrame.setWaitCursor ( true );
			DateValueTS.writeTimeSeriesList ( __tslist, path );
			__tsview_JFrame.setWaitCursor ( false );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving DateValue file \"" + path + "\"");
			Message.printWarning ( 2, routine, e );
			__tsview_JFrame.setWaitCursor ( false );
		}
	}
	else if (fc.getFileFilter() == jpg_sff || fc.getFileFilter() == png_sff) {
	    String type = "PNG";   // For message below
		try {	
			__tsview_JFrame.setWaitCursor(true);
			if (fc.getFileFilter() == png_sff) {
			    path = IOUtil.enforceFileExtension ( path, "png" );
			}
			// Leave *.jpeg alone but by default enforce *.jpg
			else if ( (fc.getFileFilter() == jpg_sff) && !StringUtil.endsWithIgnoreCase(path,"jpeg")) {
	            path = IOUtil.enforceFileExtension ( path, "jpg" );
	            type = "JPG";
	        }
			// The following will examine the extension and save as PNG or JPG accordingly
     		_ts_graph.saveAsFile(path);
			__tsproduct.setDirty(false);
			__tsview_JFrame.setWaitCursor(false);
		}
		catch (Exception e) {
			Message.printWarning(1, routine, "Error saving " + type + " image file \"" + path + "\"");
			Message.printWarning(2, routine, e);
			__tsview_JFrame.setWaitCursor(false);
		}
	}
    else if ( fc.getFileFilter() == svg_sff ) {
        try {   
            __tsview_JFrame.setWaitCursor(true);
            path = IOUtil.enforceFileExtension ( path, "svg" );
            try {
                _ts_graph.saveAsSVG(path);
            }
            catch ( Exception e ) {
                Message.printWarning ( 1, "TSViewGraphJFrame.actionPerformed", "Error printing graph." );
                Message.printWarning ( 2, "TSViewGraphJFrame.actionPerformed", e );
            }
            
            __tsview_JFrame.setWaitCursor(false);
        }
        catch (Exception e) {
            Message.printWarning(1, routine, "Error saving SVG file \"" + path + "\"");
            Message.printWarning(2, routine, e);
            __tsview_JFrame.setWaitCursor(false);
        }
    }
	else if ( fc.getFileFilter() == tsp_sff ) {
		try {
		    path = IOUtil.enforceFileExtension ( path, "tsp" );		
			__tsproduct.writeFile ( path, false );
			__tsproduct.setDirty(false);
			__tsproduct.propsSaved();
//			__tsproduct.setPropsHowSet(Prop.SET_FROM_PERSISTENT);
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving time series product file:\n" + "\"" + path + "\"");
			Message.printWarning ( 2, routine, e );
		}
	}
	else {
	    // Save to a DMI (database, etc.) via the interface.
		if (dmiff != null) {
			for (int i = 0; i < size; i++) {
				if (fc.getFileFilter() == dmiff[i]) {
					TSProductDMI dmi = tsProductDMIs.get(i);
					dmi.writeTSProduct(__tsproduct);
				}
			}
		}
		__tsproduct.setDirty(false);
		__tsproduct.propsSaved();
//		__tsproduct.setPropsHowSet(Prop.SET_FROM_PERSISTENT);
	}
}

/**
Set the interaction mode for the main and reference graphs.  Currently this
just calls the TSGraphJComponent version.
@param mode Interaction mode (see TSGraphJComponent).
*/
public void setInteractionMode ( int mode )
{	if ( _ts_graph != null ) {
		_ts_graph.setInteractionMode( mode );
	}
	// Reference graph is always in zoom mode...
	if ( _ref_graph != null ) {
		_ref_graph.setInteractionMode( TSGraphJComponent.INTERACTION_ZOOM );
	}
}

/**
Checks to see whether the GUI should be allowed to close.  If the TSProduct is
not dirty, it is allowed to close.  Otherwise, a dialog opens checking whether
the user wants to quit without saving.
@return whether the GUI is allowed to close (true) or not.
*/
protected boolean shouldClose() {
	if (!__tsproduct.isDirty()) {
		return true;
	}

	int x = new ResponseJDialog(this, "Time Series Product has Changed",
		"Changes have been made.  Do you want to save changes?",
		ResponseJDialog.YES | ResponseJDialog.NO |
		ResponseJDialog.CANCEL ).response();

	if ( x == ResponseJDialog.CANCEL ) {
		// Close has been canceled...
		return false;
	}
	else if ( x == ResponseJDialog.NO ) {
		// Close without saving
		return true;
	}
	else {
	    // Save the changes as if the Save button had been pressed and
		// indicate that close is OK...
		saveGraph();
		return true;
	}
}

/**
Handle mouse motion events and display the mouse coordinates in the status TextField.
@param devpt Mouse point in GR device coordinates.
@param datapt Mouse point in data coordinates.
*/
public void tsViewMouseMotion ( TSGraph g, GRPoint devpt, GRPoint datapt )
{	__tracker_JTextField.setText ( g.formatMouseTrackerDataPoint(devpt,datapt) );
}

public void tsViewSelect ( TSGraph g, GRShape devlim, GRShape datalim, List<Object> selected )
{
	checkGUIState();
}

public void tsViewZoom ( TSGraph g, GRShape devlim, GRShape datalim )
{
	Message.printStatus(2, "tsViewZoom", "zooming to provided limits" + datalim );
	if ( datalim instanceof GRLimits ) {
		GRLimits newLimits = new GRLimits((GRLimits)datalim);
		Message.printStatus(2, "tsViewZoom", "Adding new limits to reference graph" );
		_ref_graph.getReferenceGraphZoomHistory().add(newLimits);
	}
	checkGUIState();
}

// WindowListener functions...

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}

/**
Responds to the window Closing window event.
@param event WindowEvent object.
*/
public void windowClosing(WindowEvent event) {
	setDefaultCloseOperation(HIDE_ON_CLOSE);
	// Close the detail window...
	closeDetail ();
	// Now let the main manager close GUIs as appropriate...
	__tsview_JFrame.closeGUI(TSViewType.GRAPH);
}

public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}

}
