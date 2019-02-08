// TSViewJFrame - main TSView controller class

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
// TSViewJFrame - main TSView controller class
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1)	This class handles the TSView GUIs using a JFrame that
//			is never made visible.
//			At instantiation, only the event handlers are set up.
//------------------------------------------------------------------------------
// History:
// 
// 05 Dec 1998	Steven A. Malers,	Initial version.  Copy OpTableGUI and
//		Riverside Technology,	modify.
//		inc.
// 15 Oct 2000	SAM, RTi		Enable properties window handling.
//					Add refresh() method to update GUIs
//					based on properties changes.
// 13 Apr 2001	SAM, RTi		Add getPropValue(), setPropValue(),
//					isDirty(), finalize().
// 04 May 2001	SAM, RTi		Add more properties to documentation:
//					YAxisPrecision
//					TSViewTitleString
//					Add setWaitCursor().
// 17 May 2001	SAM, RTi		Add saveAsText() and saveAsDateValue()
//					to work as utility methods from the
//					views.  Remove unused code that attaches
//					menus.
// 17 Jul 2001	SAM, RTi		Change so if using Windows 95 variant
//					that a ReportGUI is used for the summary
//					so that the paging works.  This does
//					not allow for as much interaction
//					between the window but at least gets
//					things working on Windows 95.  Add
//					a WindowListener to monitor the
//					ReportGUI.  Add a warning to the table
//					that displaying daily data may be slow
//					and add a needToClose() check here.
// 05 Sep 2001	SAM, RTi		Add _identifier to allow graphs to be
//					managed externally (e.g., by TSTool).
//					Add isVisible(boolean) to allow view
//					to be hidden/visible during preparation.
// 2001-11-05	SAM, RTi		Update javadoc.  Verify that variables
//					are set to null when no longer used.
// 2002-01-17	SAM, RTi		Rename TSViewGUI to TSViewFrame so that
//					support can be added for Swing
//					(TSViewJFrame).
// 2002-07-12	SAM, RTi		Fix so warning is printed if trying to
//					write DateValue time series of different
//					intervals.
// =========================
// 2002-11-11	SAM, RTi		Copy TSViewFrame and update to use
//					Swing.
// 2003-06-03	SAM, RTi		* Update based on current GR and TS
//					  packages.
//					* Use JGUIUtil instead of GUIUtil.
// 2003-08-21	SAM, RTi		* Change DateValueTS.writeTimeSeries()
//					  to writeTimeSeriesList().
// 2004-02-24	J. Thomas Sapienza, RTi	Added getViewGraphJFrame().
// 2004-04-23	SAM, RTi		Renamed TSViewPropertiesJFrame to
//					TSProductJFrame and adjusted string
//					properties appropriately.
// 2004-04-30	JTS, RTi		* Added closePropertiesGUI().
//					* Added openPropertiesGUI().
// 2004-10-11	JTS, RTi		Added closeGraphGUI().
// 2005-04-22	JTS, RTi		Added PROPERTIES_HIDDEN so that the
//					properties GUI can be opened non-visible
//					in order to use it to change properties
//					on graphs.
// 2005-07-13	JTS, RTi		TSProductDMIs are now stored in this
//					class instead of in the 
//					TSViewGraphJFrame.
// 2005-10-14	JTS, RTi		Added a combo box for selecting an 
//					annotation provider.
// 2005-10-18	JTS, RTi		TSProductAnnotationProviders are now
//					stored in this class, like 
//					TSProductDMIs
// 2005-10-28	SAM, RTi		Add needToCloseGraph() to chain the
//					call to the similar methods in other
//					views.  If true is returned it means
//					that the view could not be started up,
//					usually because of a lack of data.  This
//					can then be used by a TSTool check in
//					batch mode to exit with a non-zero
//					status.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.GRTS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import RTi.TS.DateValueTS;
import RTi.TS.TS;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
The TSViewJFrame class provides a simple and uniform way to provide views of
time series data.  Three main views are available:
<ol>
<li>	graph, implemented with TSViewGraphJFrame,</li>
<li>	summary text report, implemented with TSViewSummaryJFrame,</li>
<li>	table, implemented with TSViewTableJFrame.</li>
</ol>
The above views may allow additional windows to be displayed (e.g., the
TSViewGraphJFrame allows the TSProductJFrame to be displayed, showing
properties for the graph).  The TSViewJFrame is a hidden
frame and manages all of the views for a set of time series, using listeners.
This approach allows sharing of resources between views and minimizes redraw
times when windows are minimized/maximized.  When the last view is closed, all
of the graphical resources will be freed and this frame will be disposed().  The
different views can be enabled or disabled, as appropriate, for the data that
are being displayed.  See the documentation for the constructor for a list of
properties that can be used to control output.
*/
@SuppressWarnings("serial")
public class TSViewJFrame extends JFrame
implements ActionListener
{
	
// Private data related to TSView...

/**
List of time series to display.
*/
private List<TS> _tslist = null;
/**
Property list used for displays.
*/
private PropList _props = null;
/**
TSProduct (used instead of old-style) PropList.
*/
private TSProduct _tsproduct = null;
/**
Currently this is only used by the graph and indicates when a property has been set in the
TSProductJFrame (so that title, etc. can be updated in the graph).
*/
private boolean _is_dirty = true;
/**
Static window manager shared among all instances.
*/
private static TSViewWindowManager __tsViewWM = new TSViewWindowManager();

// Private data related to Frame...

//protected Frame _parent = null;		// Parent frame.
/**
Frame to display the time series in a graph.
*/
protected TSViewGraphJFrame _graph_gui = null;
/**
Frame to display the time series in a summary report.
*/
protected TSViewSummaryJFrame _summary_gui = null;
/**
Frame to display the time series in a tabular display.
*/
protected TSViewTableJFrame _table_gui = null;
/**
Frame to display the time series product properties
*/
protected TSProductJFrame _tsproduct_gui = null;

/**
List of TSProductAnnotationProvider objects available for use with any of 
the sub guis.  Used in TSProductJFrame properties display.
*/
private List<TSProductAnnotationProvider> __tsProductAnnotationProviders = null;

/**
List of TSProductDMI objects available for use with any of the sub guis.
*/
private List<TSProductDMI> __tsProductDMIs = null;

/**
Construct a stand-alone frame that manages a time series graph, summary and table.
@param tslist list of TS to display.
@param proplist Properties to control the display.  Properties
can have the values shown in the following table:
<p>
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td align=center colspan=3>General Properties</td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>InitialView</b></td>
<td>Indicates whether the initial view should be a "Graph", "Summary", or "Table".</td>
<td>"Summary"</td>
</tr>

<tr>
<td><b>EnableGraph</b></td>
<td>Indicates whether the graph view should be enabled.</td>
<td>"true"</td>
</tr>

<tr>
<td><b>EnableSummary</b></td>
<td>Indicates whether the summary view should be enabled.</td>
<td>"true"</td>
</tr>

<tr>
<td><b>EnableTable</b></td>
<td>Indicates whether the table view should be enabled.</td>
<td>"true"</td>
</tr>

<tr>
<td><b>TSViewTitleString</b></td>
<td>If specified the views will use this string for the frame title, which will
then be visible when windows are minimized.  The string is used with the
following strings appended as appropriate to create an initial title:
" - Graph", " - Table", " - Summary".  The title may further be modified in
the individual views if more information is available (e.g., the graph type).
It should only be necessary to set the title string once to achieve reasonably specific titles.
</td>
<td>Defaults to "Time Series - Graph View", "Time Series - Table View",
"Time Series - Summary View".</td>
</tr>

<tr>
<td align=center colspan=3>
Graph View Properties</td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>DoubleBuffer</b></td>
<td>Indicates whether double-buffering should be used.  Doing so increases
performance for refreshes but uses more memory.
</td>
<td>true</td>
</tr>

<tr>
<td><b>Graph.EnableTracker</b></td>
<td>Indicates whether the mouse tracker should be enabled.  If true, then
for screen output the mouse position is shown in a TextField below the graph.
</td>
<td>"true"</td>
</tr>

<tr>
<td><b>GraphHeight</b></td>
<td>Height of graph canvas in pixels.
</td>
<td>400</td>
</tr>

<tr>
<td><b>GraphType</b></td>
<td>Type of graph ("Bar", "Double-Mass", "Duration", "Line", "PeriodOfRecord",
"XY-Scatter").
</td>
<td>"Line"</td>
</tr>

<tr>
<td><b>GraphWidth</b></td>
<td>Width of graph canvas in pixels.
</td>
<td>400</td>
</tr>

<tr>
<td><b>MaximizeGraphSpace</b></td>
<td>Indicates whether the canvas should be completely filled with the graph.
This property was used during development and should not be specified in
production components.
</td>
<td>"true"
</tr>

<tr>
<td><b>EnableReferenceGraph</b></td>
<td>Indicates whether the graph should display a reference graph.
</td>
<td>"true"
</tr>

<tr>
<td><b>TitleString</b></td>
<td>Literal string to use for main title (above plot).
</td>
<td>Determined from graph type.
</tr>

<tr>
<td><b>XAxisFormat</b></td>
<td>Format for X-axis label.  Currently only "MM-DD" is recognized other
than the default format determined from data.
</td>
<td>Determined from graph type and data for axis.
</tr>

<tr>
<td><b>XAxisLabelString</b></td>
<td>Literal string to use for X-axis label (labeled under X axis).
</td>
<td>Date for simple plots or determined based on graph type (e.g., for
Duration plot the label is the percentage of time exceeded.</td>
</tr>

<tr>
<td><b>YAxisLabelString</b></td>
<td>Literal string to use for Y-axis label (labeled at top of Y-axis).
</td>
<td>Units.</td>
</tr>

<tr>
<td><b>YAxisPrecision</b></td>
<td>Precision for Y-axis labels.  Use when it is likely that the precision based
on units will not be satisfactory (e.g., for dimensionless numbers, which are
harder to set a default precision for).
</td>
<td>Use precision based on units or default of 2.</td>
</tr>

<tr>
<td><b>YAxisType</b></td>
<td>Indicates type of Y-axis ("Log" or "Linear").
</td>
<td>"Linear"</td>
</tr>

<tr>
<td align=center colspan=3>
Summary View Properties</td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>OutputPrecision</b></td>
<td>Precision of output.
</td>
<td>Determined from units or often 2.</td>
</tr>

<tr>
<td><b>TotalHeight</b></td>
<td>Height of summary view frame in pixels.
</td>
<td>400</td>
</tr>

<tr>
<td><b>TotalWidth</b></td>
<td>Width of summary view frame pixels.
</td>
<td>600</td>
</tr>

<tr>
<td align=center colspan=3>
Table View Properties</td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>OutputPrecision</b></td>
<td>Precision of output.
</td>
<td>Determined from units or often 2.</td>
</tr>

<tr>
<td><b>Table.UseExtendedLegend</b></td>
<td>Indicates whether the time series extended legend should be used for
column headings.  Specify as "true" or "false".
</td>
<td>false</td>
</tr>

</table>
@exception Exception if there is an error opening the view.
*/
public TSViewJFrame ( List<TS> tslist, PropList proplist )
throws Exception
{	super ( "Time Series View" );
	initialize ( tslist, proplist );
}

/**
Construct using a TSProduct, instead of the old-style PropList.
*/
public TSViewJFrame ( TSProduct tsproduct )
throws Exception
{	super ( "Time Series View" );
	// Must set this before calling initialize()...
	_tsproduct = tsproduct;
	// Later phase out _tslist
	// Actually, set a property to make sure the centering works
	PropList props = new PropList("TSViewJFrame");
	props.setUsingObject("TSViewParentUIComponent",this);
	initialize ( tsproduct.getTSList(), props );
}

/**
Handle action events.
@param event ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent event )
{	
}

/**
@deprecated use the other method that takes a proplist
*/
public void addTSProductAnnotationProvider(TSProductAnnotationProvider provider) {	
	__tsProductAnnotationProviders.add(provider);
	if (_tsproduct != null) {
		try {
			_tsproduct.addTSProductAnnotationProvider(provider, null);
		}
		catch (Exception e) {
			Message.printWarning(2, 
				"addTSProductAnnotationProvider", "Error adding annotations to provider.");
			Message.printWarning(2, "addTSProductAnnotationProvider", e);
		}
	}
}

/**
Adds a TSProductAnnotationProvider to the TSProduct viewed by this view JFrame.
If the TSProduct has time series, the annotation provider will put annotations
on the product immediately.  Otherwise, the provider will be cached by the
TSProduct until time series are set in the TSProduct.
@param provider the TSProductAnnotationProvider to add.
@param controlProps control properties for the annotation provider.
*/
public void addTSProductAnnotationProvider(TSProductAnnotationProvider provider, PropList controlProps) {	
	__tsProductAnnotationProviders.add(provider);
	if (_tsproduct != null) {
		try {
			_tsproduct.addTSProductAnnotationProvider(provider, controlProps);
		}
		catch (Exception e) {
			Message.printWarning(2, "addTSProductAnnotationProvider", "Error adding annotations to provider.");
			Message.printWarning(2, "addTSProductAnnotationProvider", e);
		}
	}
}

/**
Adds a TSProductDMI to the list of TSProductDMIs stored in this class.
@param productDMI the TSProductDMI to add.
*/
public void addTSProductDMI(TSProductDMI productDMI) {	
	__tsProductDMIs.add(productDMI);
}

/**
Close a subordinate Frame.  This is done here to coordinate open/closing the
Frames.  If all subordinate Frames are closed, this Frame is disposed.
@param type Type of TSView Frame go close (e.g., GRAPH), as defined in this class.
@return 0 if the window was not closed, 1 if the window was closed, and -1 if the controlling
TSViewJFrame was closed.
*/
protected int closeGUI ( TSViewType type )
{	int closeCount = 0; // Will be set to 1 if the window actually closed
    if ( type == TSViewType.GRAPH ) {
		if ( _graph_gui != null ) {
			if (_graph_gui.shouldClose()) {
				_graph_gui.setVisible ( false );
				_graph_gui.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				_graph_gui.dispose();
				_graph_gui = null;
				// Also close the properties window (currently this is only associated with the graph Frame)...
				closeGUI ( TSViewType.PROPERTIES );
				++closeCount;
			}
			else {
				_graph_gui.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			}
		}
	}
	else if (type == TSViewType.PROPERTIES || type == TSViewType.PROPERTIES_HIDDEN) {
		if ( _tsproduct_gui != null ) {
			if (_tsproduct_gui.checkUserInput()) {
				_tsproduct_gui.setVisible ( false );
				_tsproduct_gui.setDefaultCloseOperation (DISPOSE_ON_CLOSE);
				_tsproduct_gui.dispose();
				_tsproduct_gui = null;
				++closeCount;
			}
			else {
				_tsproduct_gui.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			}
		}
	}
	else if ( type == TSViewType.SUMMARY ) {
		if ( _summary_gui != null ) {
			_summary_gui.setVisible ( false );
			_summary_gui.setDefaultCloseOperation (DISPOSE_ON_CLOSE);
			_summary_gui.dispose();
			_summary_gui = null;
			++closeCount;
		}
	}
	else if ( type == TSViewType.TABLE ) {
		if ( _table_gui != null ) {
			_table_gui.setVisible ( false );
			_table_gui.setDefaultCloseOperation ( DISPOSE_ON_CLOSE);
			_table_gui.dispose();
			_table_gui = null;
			++closeCount;
		}
	}
	// Close this Frame if there are no other Frames open (otherwise there
	// is no way to garbage collect)...
	if ( (_graph_gui == null) && (_tsproduct_gui == null) && (_summary_gui == null) &&
		(_table_gui == null) ) {
	    // Remove from the manager before disposing...
	    getTSViewWindowManager().remove ( this );
		setDefaultCloseOperation ( DISPOSE_ON_CLOSE);
		dispose ();
		return -1;
	}
	else {
	    return closeCount;
	}
}

/**
Closes an open Graph GUI.
*/
public void closeGraphGUI() {
	closeGUI(TSViewType.GRAPH);
}

/**
Closes an open properties GUI.
*/
public void closePropertiesGUI() {
	closeGUI(TSViewType.PROPERTIES);
}

/**
Clean up memory for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_tslist = null;	
	_props = null;
	_graph_gui = null;
	_tsproduct = null;
	_summary_gui = null;
	_table_gui = null;
	_tsproduct_gui = null;
	super.finalize();
}

/**
Get a TSView property, which is available to all the view components.
@return value of property corresponding to the key.
@param key Property key.
*/
public String getPropValue ( String key )
{	return _props.getValue ( key );
}

/**
Returns the list of TSProductAnnotationProviders that were added to this class
@return the list of TSProductAnnotationProviders that were added to this class
*/
public List<TSProductAnnotationProvider> getTSProductAnnotationProviders() {
	return __tsProductAnnotationProviders;
}

/**
Returns the list of TSProductDMIs that were added to this class.
@return the list of TSProductDMIs that were added to this class.
*/
public List<TSProductDMI> getTSProductDMIs() {
	return __tsProductDMIs;
}

/**
Return a reference to the TSProductJFrame.  This is used from the
TSGraph code to control the properties window.
*/
public TSProductJFrame getTSProductJFrame()
{	return _tsproduct_gui;
}

/**
Returns the view graph JFrame.
@return the view graph JFrame.
*/
public TSViewGraphJFrame getViewGraphJFrame() {
	return _graph_gui;
}

/**
 * Returns the table JFrame.
 * @return table JFrame
 */
public TSViewTableJFrame getTSViewTableJFrame()
{
  return _table_gui;
}

/**
Return the shared window manager for all window instances.
@return the shared window manager for all window instances.
*/
public static TSViewWindowManager getTSViewWindowManager()
{
  return __tsViewWM;
}

/**
Initialize the TSViewJFrame data.
@param tslist list of TS to display.
@param proplist Properties to control the display.
@exception Exception if an error occurs.
*/
private void initialize ( List<TS> tslist, PropList proplist )
throws Exception
{	String message, routine = "TSViewJFrame.initialize";

	try {
    	//_parent = parent;
    	/* TODO SAM 2009-04-16 - allow no time series to be more flexible
    	if ( tslist == null ) {
    		// Cannot continue, we need a TS list...
    		throw new Exception ( "No time series to view." );
    	}
    	if ( tslist.size() == 0 ) {
    		// Cannot continue, we need a TS list...
    		throw new Exception ( "No time series to view." );
    	}
    	*/
    	_tslist = tslist;
    	if ( proplist == null ) {
    		// That is ok, create a new one...
    		_props = new PropList ( "TSView.Defaults" );
    	}
    	else {
    	    // Use what was supplied...
    		_props = proplist;
    	}
    
    	// Check the proplist for the initial view...
    
    	String prop_value = null;
    	if ( _tsproduct == null ) {
    		prop_value = _props.getValue ( "InitialView" );
    		//Message.printStatus ( 1, "","InitialView from PropList is \""+ prop_value + "\"" );
    	}
    	else {
    	    prop_value = _tsproduct.getPropValue ( "InitialView" );
    		//Message.printStatus ( 1,"","InitialView from TSProduct is \""+prop_value + "\"" );
    	}
    	if ( prop_value == null ) {
    		// Default to summary...
    		openGUI ( TSViewType.SUMMARY );
    	}
    	else if ( prop_value.equalsIgnoreCase("Graph") ) {
    		openGUI ( TSViewType.GRAPH );
    	}
    	else if ( prop_value.equalsIgnoreCase("Table") ) {
    		openGUI ( TSViewType.TABLE );
    	}
    	else {
    		// Default to summary...
    		openGUI ( TSViewType.SUMMARY );
    	}
	}
	catch ( Exception e ) {
		message = "Unable to open time series view.";
		Message.printWarning ( 3, routine, e );
		Message.printWarning ( 1, routine, message );
		throw new Exception ( message );
	}
	
	if (__tsProductDMIs == null) {
		__tsProductDMIs = new Vector<TSProductDMI>();
	}
	if (__tsProductAnnotationProviders == null) {
		__tsProductAnnotationProviders = new Vector<TSProductAnnotationProvider>();
	}
}

/**
Indicate whether the time series have been modified.  Ideally a change in one
view will result in other views being changed.  Currently time series cannot
be edited and only the graph properties can be dynamically set so the dirty
flag is not used by the different views.
*/
protected boolean isDirty ()
{	return _is_dirty;
}

/**
Set whether the time series have been modified.
@param is_dirty Indicates whether the time series views need to be refreshed in
response to a time series data change.
*/
protected void isDirty ( boolean is_dirty )
{	_is_dirty = is_dirty;
}

/**
Set whether the views are visible.  Currently this does nothing.
@param is_visible Indicates whether all views should be visible.
*/
protected void isVisible ( boolean is_visible )
{	// This frame is always invisible but need to make sure the components are set to invisible...
}

/**
Indicate whether the graph needs to be closed due to start-up problems.
This will be the case, for example, if time series are incompatible for plotting
and the user indicates not to continue.  This should be called by the parent
code after a TSViewGraphFrame is constructed.  If the graph is null (was not
created), then true is returned.
@return true if the graph should be automatically closed due to data problems,
false if the graph should be (or is currently) displayed.
*/
public boolean needToCloseGraph ()
{	if ( _graph_gui != null ) {
		return _graph_gui.needToClose();
	}
	else {
		return true;
	}
}

/**
Open a subordinate GUI.  This is done here to coordinate open/closing the
JFrames within the package classes.
@param type Type of GUI to open (e.g., GRAPH).
*/
protected void openGUI ( TSViewType type )
{	String routine = "TSViewJFrame.openGUI";
    TSViewWindowManager wm = getTSViewWindowManager();
	try {
    	if ( type == TSViewType.GRAPH ) {
    		if ( _graph_gui == null ) {
    			// OK to open new GUI...
    			setWaitCursor ( true );
    
    			if ( _tsproduct == null ) {
    				// Old-style...
    				_graph_gui = new TSViewGraphJFrame ( this, _tslist, _props );
    			}
    			else {
    				// New-style...
    				_tsproduct.setTSList ( _tslist );
    				_graph_gui = new TSViewGraphJFrame ( this, _tsproduct );
    			}
   				wm.add ( this, _graph_gui );
    			setWaitCursor ( false );
    			// The following gracefully handles shut-down of a graph.  An attempt to close the graph
    			// GUI from itself will fail because _graph_gui will still be null.
    			if ( _graph_gui.needToClose() ) {
    				Message.printStatus ( 2, routine,
    				"Automatically closing the graph because of initialization problems." );
    				closeGUI ( TSViewType.GRAPH );
    			}
    		}
    		else {
    			// The GUI is already open, pop to the front (any way to do this?)...
    			_graph_gui.setVisible ( true );
    			_graph_gui.toFront();
    		}
    	}
    	else if ( type == TSViewType.PROPERTIES ) {
    		if ( _tsproduct_gui == null ) {
    			// Need to pass in the main canvas, which is
    			// currently where the TSProduct properties are fully checked.
    			if ( _graph_gui != null ) {
    				_tsproduct_gui = new TSProductJFrame ( this, _graph_gui.getMainJComponent() );
    				wm.add ( this, _tsproduct_gui );
    			}
    		}
    		else {
    			// The GUI is already open, pop to the front (any way to do this?)...
    			_tsproduct_gui.setVisible ( true );
    		}
    	}
    	else if (type == TSViewType.PROPERTIES_HIDDEN) {
    		if (_tsproduct_gui == null) {
    			// Need to pass in the main canvas, which is currently where the TSProduct properties
    			// are fully checked.
    			if (_graph_gui != null) {
    				_tsproduct_gui = new TSProductJFrame(this, _graph_gui.getMainJComponent(),false);
    				wm.add ( this, _tsproduct_gui );
    			}
    		}
    		else {	
    			// The GUI is already open, in a visible or invisible
    			// mode.  Either way, it's fine how it is.  
    		}
    	}	
    	else if ( type == TSViewType.SUMMARY ) {
    		if ( _summary_gui == null ) {
    			// OK to open new GUI...
    			setWaitCursor ( true );
    			_summary_gui = new TSViewSummaryJFrame ( this, _tslist, _props );
    			wm.add ( this, _summary_gui );
    		}
    		else {
    			// The GUI is already open, pop to the front (any way to do this?)...
    			_summary_gui.setVisible ( true );
    		}
    	}
    	else if ( type == TSViewType.TABLE ) {
    		if ( _table_gui == null ) {
    			// OK to open new GUI...
    			setWaitCursor ( true );
    			_table_gui = new TSViewTableJFrame ( this, _tslist,	_props );
    			wm.add ( this, _table_gui );
    			// The following gracefully handles shut-down of a
    			// graph.  An attempt to close the graph GUI from 
    			// itself will fail because _graph_gui will still be null.
    			/*
    			TODO (JTS - 2003-07-21) this method was removed from TSViewTableJFrame.
    			if ( _table_gui.needToClose() ) {
    				closeGUI ( TABLE );
    			}
    			*/
    		}
    		else {
    			// The GUI is already open, pop to the front (any way to do this?)...
    			_table_gui.setVisible ( true );
    		}
    	}
	}
	catch ( OutOfMemoryError e ) {
		Message.printWarning ( 1, routine, "Unable to display view (out of memory).  Try displaying less data." );
		System.gc();
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine, "Unable to open view.  This may be due to data being unavailable.");
		Message.printWarning ( 2, routine, e );
	}
	finally {
	    setWaitCursor ( false );
	}
}

/**
Opens the Graph properties display.
*/
public void openPropertiesGUI() {
	openGUI(TSViewType.PROPERTIES);
}

/**
Refresh the displays based on property changes.  Currently only the graph GUI is refreshed.
*/
protected void refresh ()
{	// For now only refresh the plot.
	if ( _graph_gui != null ) {
		_graph_gui.refresh();
	}
}

/**
Removes a TSProductAnnotationProvider from those that were added to the class.
@param provider the TSProductAnnotationProvider to remove.
*/
public void removeTSProductAnnotationProvider(
TSProductAnnotationProvider provider) {
	int size = __tsProductAnnotationProviders.size();

	TSProductAnnotationProvider tsProductAnnotationProvider = null;
	for (int i = (size - 1); i <= 0; i--) {
		tsProductAnnotationProvider = (TSProductAnnotationProvider)__tsProductAnnotationProviders.get(i);
		if (tsProductAnnotationProvider == provider) {
			__tsProductAnnotationProviders.remove(i);
		}
	}
}

/**
Removes a TSProductDMI from those that were added to the class.
@param productDMI the TSProductDMI to remove.
*/
public void removeTSProductDMI(TSProductDMI productDMI) {
	int size = __tsProductDMIs.size();

	TSProductDMI tsProductDMI = null;
	for (int i = (size - 1); i <= 0; i--) {
		tsProductDMI = __tsProductDMIs.get(i);
		if (tsProductDMI == productDMI) {
			__tsProductDMIs.remove(i);
		}
	}
}


/**
Save the time series in the list to a DateValue file.  The user is prompted to select an output file.
*/
public void saveAsDateValue ()
{	String routine = "TSViewJFrame.saveAsDateValue";
	Message.printStatus ( 2, routine, "Saving DateValue file." );
	if ( !TSUtil.intervalsMatch(_tslist) ) {
		Message.printWarning ( 1, routine, "Unable to write DateValue time series of different intervals." );
		return;
	}
	JFileChooser fc = new JFileChooser();
	fc.setDialogTitle ( "Save DateValue File" );
	File default_file = new File ( "export.dv" );
	fc.setSelectedFile ( default_file );
	SimpleFileFilter sff = new SimpleFileFilter ( "dv", "DateValue Time Series File" );
	fc.addChoosableFileFilter ( sff );
	fc.addChoosableFileFilter ( new SimpleFileFilter ( "txt", "DateValue Time Series File" ) );
	fc.setFileFilter ( sff );
	String last_directory = JGUIUtil.getLastFileDialogDirectory();
	if ( last_directory != null ) {
		fc.setCurrentDirectory ( new File(last_directory) );
	}

	if ( fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ) {
		String directory = fc.getSelectedFile().getParent();
		if ( directory != null ) {
			JGUIUtil.setLastFileDialogDirectory(directory);
		}
		String filename = directory + File.separator + fc.getName(fc.getSelectedFile());
		try {
			setWaitCursor ( true );
			DateValueTS.writeTimeSeriesList ( _tslist, filename );
			setWaitCursor ( false );
		}
		catch ( Exception e ) {
			Message.printWarning ( 1, routine, "Error saving DateValue file \"" + filename + "\"");
			Message.printWarning ( 3, routine, e );
			setWaitCursor ( false );
		}
	}
}

/**
Set the interaction mode.  Currently this just calls the TSViewGraphFrame version.
Need to figure out how listeners will communicate within this package.
@param mode TSViewGraphFrame interaction mode.
*/
public void setInteractionMode ( int mode )
{	if ( _graph_gui != null ) {
		_graph_gui.setInteractionMode( mode );
	}
}

/**
Set a property, which will be available to all the view components.
@param key Property key.
@param value Property value string.
@deprecated Set the property in the TSProduct.
*/
public void setPropValue ( String key, String value )
{	_props.set ( key, value );
}

/**
Set the wait cursor on/off.  This can be used, for example, to set the cursor
for the views to the hourglass while a view is opening.
@param status true to set to wait cursor, false to set to normal cursor.
*/
public void setWaitCursor ( boolean status )
{	if ( _graph_gui != null ) {
		// Don't use the glass pane to intercept events because that is handled by the component
		JGUIUtil.setWaitCursor ( _graph_gui, status, false );
	}
	if ( _table_gui != null ) {
		JGUIUtil.setWaitCursor ( _table_gui, status );
	}
	if ( _summary_gui != null ) {
		JGUIUtil.setWaitCursor ( _summary_gui, status );
	}
}

}
