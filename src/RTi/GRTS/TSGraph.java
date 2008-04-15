// ----------------------------------------------------------------------------
// TSGraph - class to manage drawing areas for one time series graph
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2002-01-18	Steven A. Malers, RTi	Modify the original TSGraph (which is
//					now TSGraphFrame).  This class now only
//					manages a group of drawing areas and
//					draws the graph.  See the TSGraphFrame
//					class for history for prior development.
// 2002-02-04	SAM, RTi		Change _regression_data to a Vector to
//					allow more than one relationship to be
//					shown on the same plot (one X, multiple
//					Y).  Rework the date labels on the X
//					axis to be much more flexible than
//					before.
// 2002-03-25	SAM, RTi		Update to work with new TSRegression.
// 2002-04-28	SAM, RTi		Update legend variables to use left,
//					right, etc.  Still have only bottom
//					functionality.  Change so if
//					LegendFormat for graph is not Auto and
//					for time series it is, use the graph
//					format.
// 2002-04-29	SAM, RTi		Finish support for left and right side
//					legend.
//					Change so the legend symbol width is
//					always 25 pixels (not 5% of legend
//					drawing area) - the % did not work with
//					left and right legends.
// 2002-05-19	SAM, RTi		Change so that for a regression plot the
//					first time series is by default the Y
//					axis (dependent) time series and all
//					others are X axis (independent) time
//					series.  This is more appropriate when
//					filling time series.  Use the XYScatter*
//					properties when doing the regression.
//					Add support for the DataLabel*
//					properties.  Add support for the
//					Confidence line in XY plots.
// ==================================
// 2002-11-11	SAM, RTi		Copy AWT version and update to Swing.
// 2003-06-03	SAM, RTi		* Update based on recent changes to GR
//					  (rework of class names, cleanup, etc.)
//					  and TS (use DateTime, etc.)
//					* Use all Swing components for dialogs,
//					  etc.
//					* Synchronize with recent NoSwing
//					  changes as listed below:
//					2003-03-21	SAM, RTi		
//					* For legend "Auto" display the alias if
//					  it is set.
//					2003-05-14	SAM, RTi		
//					* Add support for XYScatterIntercept.
//					* Add support for
//					  XYScatterAnalyzeForFilling.
//					* Add support for
//					  XYScatterFillPeriodStart and
//					  XYScatterFillPeriodEnd.
//					* Add support for
//					  XYScatterDependentAnalysisPeriodStart
//					  and
//					  XYScatterDependentAnalysisPeriodEnd.
//					* Add support for
//					 XYScatterIndependentAnalysisPeriodStart
//					  and
//					  XYScatterIndependentAnalysisPeriodEnd.
//					* Fix bug in drawXAxis() - was not
//					  parsing the correct string for the
//					  grid color.
//					* If there is an error getting the date
//					  limits for the plot, call
//					  needToClose(true).
// 2003-09-25	SAM, RTi		* Limits of XYScatter were not getting
//					  set when only one point was being
//					  plotted (regression fails).  Remove
//					  the check for a successful regression
//					  when determining the limits.
// 2003-12-16	SAM, RTi		* Enable drawing data flag for
//					  labelling now that all main TS classes
//					  support getDataPoint() - previously
//					  only a blank string was drawn.
// 2004-02-24	J. Thomas Sapienza	Added new methods, including:
//					* getMaxEndDate()
//					* getMaxStartDate()
//					* setMaxEndDate()
//					* setMaxStartDate()
// 2004-04-07	SAM, RTi		Fix bug where label position was always
//					getting set to "Right".
// 2004-04-20	JTS, RTi		* Added drawAnnotations() to draw 
//					  annotations over and under the graph.
//					* Added getEndDate(), getStartDate(),
//					  setEndDate(), setStartDate(), 
//					  setComputeWithSetDates() in order to
//					  be able to set the date limits at 
//					  which the graph will be drawn.
// 2004-04-23	SAM, RTi		Change TSViewPropertiesJFrame to
//					TSProductJFrame.
// 2004-05-17	SAM, RTi		When labelling the Y axis, there is
//					sometimes a "shadow" upper label, due
//					to the last label being offset by 1/2
//					of the label height and the code not
//					knowing when it is done labelling.
//					There have been enough comments about
//					the offset that perhaps it is better to
//					not have it.  Try using an upper label
//					that is in line with the last tick and
//					see if any problems occur with the
//					Y-axis label overwriting.  The change
//					involves removing the GRText.SHIFT_ENDS
//					flag from GRAxis.drawLabels().
// 2004-05-18	JTS, RTi		Corrected an error in computeLabels()
//					where an improper delta value was
//					resulting in infinite loops.
// 2004-05-26	JTS, RTi		Renamed computeMaxDataLimits() to 
//					computeDataLimits() so that it can
//					compute the limits with the set dates
//					instead of only the max dates.
// 2005-04-20	JTS, RTi		* Added support for setting max and 
//					  min Y values less than and greater
//					  than the global max and min values,
//					  respectively.  This required adding
//					  support for clipping the drawing area
//					  to only the graph (search for 
//					  setClip() and getClip()).
//					* Added support for chaning the max and
//					  min Y values of a graph as the graph
//					  is zoomed, so that the height of
//					  the graph corresponds to the highest
//					  and lowest points of the visible data.
//					  Not currently enabled, this option 
//					  will require changing 
//					  _zoom_keep_y_limits to "false" in both
//					  this class and TSGraphJComponent.
// 2005-04-29	JTS, RTi		* Added support for Point graphs.
//					* Added support for anti-aliasing 
//					  symbol drawing.  This is because
//					  Java's drawing package draws very 
//					  awful circles when not anti-aliased.
// 2005-05-05	JTS, RTi		* Added support for predicted value
//					  graphs.
//					* Added support for predicted value
//					  residual graphs.
//					* Time series can now be enabled or
//					  disabled.
// 2005-06-09	JTS, RTi		Added new legend drawing capabilties:
//					* Legends can be turned off altogether
//					* Legends can be drawn inside of a graph
//					  in one of the 4 corners.
// 2005-08-08	JTS, RTi		Corrected a bug caused by a missing 
//					"else" that was resulting in the wrong
//					X data limits being generated for
//					XY Scatter plots.
// 2005-10-05	JTS, RTi		Added code to drawAnnotations() for 
//					drawing Symbol annotations.
// 2005-10-10	JTS, RTi		Added graph clipping to the annotation
//					drawing code.
// 2005-10-25	JTS, RTi		Added support in drawAnnotations() for
//					"XFormat" and "YFormat"
//					properties
// 2006-02-08	JTS, RTi		Added support in drawAnnotations() for
//					"OutlineColor" property.
// 2006-04-26	JTS, RTi		* All data flags are trimmed prior to
//					  being drawn on a graph.
//					* Graph DataLabelPosition properties are
//					  being used at the correct times now,
//					  rather than the time series property
//					  being used incorrectly.
//
// 2006-09-06	KAT, RTi		* Fixed a bug where data would be offset
// 					  by X number of hours (or any other
//					  interval) compared to other time
//					  series when the data were not on an
//					  even interval (e.g., 24-hour data
//					  recorded at 12 noon).  This was fixed
//					  by rounding the period at which the
//					  graph starts at and then adding the
//					  correct offset at which the data was
//					  collected.  For example, 24 hour data
//					  that was collected at noon instead of
//					  12:00 am.  The code counted on the
//					  data being collected at 0 = 12:00 am.
//					  The graph would show data coming in at
//					  12:00 am every day, which was
//					  incorrect.  It should have been 12:00
//					  pm.  The fixed code rounds to the next
//					  lowest, even interval and then add
//					  that offset at which the data was
//					  collected.  In this case it is adding
//					  an offset of 12 hours.
// 2006-09-07	KAT			* Fixed the code where LeftYAxisMax and
//					  LeftYAxisMin (set in the properties
//					  file for the product) were not being
//					  re-calculated when the graph's data
//					  would go beyond these limits (behavior
//					  did not match the documentation).  I
//					  fixed two lines of code that check for
//					  LeftYAxisMax and LeftYAxisMin and only
//					  override the current values with the
// 					  property values if they are larger
//					  (max) or smaller (min).
// 2006-09-28	SAM			* Review KAT's code.
// 2006-10-03 	KAT			* Added method getNearestDateTimeLessThanOrEqualTo
// 					  which returns a DateTime nearest 
// 					  to an even interval. 
// 2006-11-27	KAT	 		* Fixed a glitch in the getNearestDateTimeLess....
//					  method to not go back an extra day if the returnDate
//					  has a date that is greater than the candidateDate.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.GRTS;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.JPopupMenu;

import RTi.GR.GRAspect;
import RTi.GR.GRAxis;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.TS.DayTS;
import RTi.TS.HourTS;
import RTi.TS.IrregularTS;
import RTi.TS.MinuteTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSDurationAnalysis;
import RTi.TS.TSLimits;
import RTi.TS.TSRegression;
import RTi.TS.TSUtil;

import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJMenuItem;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;

import RTi.Util.Math.FDistribution;
import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
The TSGraph class manages the drawing areas for displaying one or more time
series in a single graph.   The drawing areas are set up by specifying a
GRJComponentDevice and information about how much of the device should be used
for this graph.  Drawing properties are retrieved from a TSProduct, where this
graph is identified as a subproduct of the entire product.
This class also implements TSViewListener, which is
typically used to allow a reference other TSGraph so that zooming can
occur similarly for all graphs.
The layout of the graph is as follows (see TSGraphJComponent for other layout
features like the main title).  Because the AWT Canvas and Graphics do not allow
for vertical text, the Y axis label is currently added at the top of the axis.
This may change in the future.
Currently only one legend can be present and the position can be either the
bottom (default, left, right, or top).  The following figure shows the placement
of all the legends, although only one will be used.
<pre>
-----------------------------------------------------------------------------
|                    Full graph (_da_page)                                  |
| ------------------------------------------------------------------------- |
| |                  Main title (_da_maintitle)                           | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                    Sub title (_da_subtitle)                           | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                       Legend (_da_top_legend)                         | |
| ------------------------------------------------------------------------- |
| ---                 -------------------------------                   --- |
| | |                 |Top x axis title(not enabled)|                   | | |
| | |                 | (_da_topx_title)            |                   | | |
| |_|                 -------------------------------                   |_| |
| |d|                 -------------------------------                   |d| |
| |a|                 | Top x labels (not enabled)  |                   |a| |
| |_|                 | (_da_topx_label)            |                   |_| |
| |l|                 -------------------------------                   |r| |
| |e|      ---------------------           ----------------------       |i| |
| |f|      | Left y axis title |           | Right y axis title |       |g| |
| |t|      | (_da_lefty_title) |           | (_da_righty_title) |       |h| |
| |_|      ---------------------           ----------------------       |t| |
| |l| -------------------- ---------------------- --------------------- |_| |
| |e| |                  | |                    | |                   | |l| |
| |g| |Left y axis labels| |       Graph        | |Right y axis labels| |e| |
| |e| |(_da_lefty_label) | |       (_da_graph)  | |(_da_righty_title) | |g| |
| |n| |                  | |                    | |                   | |e| |
| |d| -------------------- ---------------------- --------------------- |n| |
| | |                      ----------------------                       |d| |
| | |                      |Bottom x axis labels|                       | | |
| | |                      | (_da_bottomx_label)|                       | | |
| | |                      ----------------------                       | | |
| | |                      ----------------------                       | | |
| | |                      | Bottom x axis title|                       | | |
| | |                      | (_da_bottomx_title)|                       | | |
| ---                      ----------------------                       --- |
| ------------------------------------------------------------------------- |
| |                      Legend (_da_bottom_legend)                       | |
| ------------------------------------------------------------------------- |
-----------------------------------------------------------------------------
</pre>
*/
public class TSGraph	// extends GRGraph //Future development??
implements ActionListener
{

/**
Popup menu options.
*/
private String	
	__MENU_ANALYSIS_DETAILS =	"Analysis Details",
	__MENU_PROPERTIES = 		"Properties",
	__MENU_REFRESH = 		"Refresh",
	__MENU_Y_MAXIMUM_AUTO = 	"Set Y Maximum to Auto",
	__MENU_Y_MINIMUM_AUTO = 	"Set Y Minimum to Auto",
	__MENU_Y_MAXIMUM_VISIBLE = 	"Set Y Maximum to Visible Maximum",
	__MENU_Y_MINIMUM_VISIBLE = 	"Set Y Minimum to Visible Minimum";

/**
If the graph type is Duration, this holds the results of the duration data
for each time series.
*/
private Vector _duration_data = null;

/**
The following reference is used internally so that the _graphics does not need
to be passed between methods in this class.  The Graphics is volatile and should
be reset in each call to paint().
*/
private Graphics _graphics = null;

/**
Indicates whether units should be ignored for normal graphs.  This allows line
graphs to plot different units and units are put in the legend and removed from
the Y-axis label.  If the units are not the same, then one of the following
will occur:
<ol>
<li>	If the "LeftYAxisIgnoreUnits" property is set, then it will be used to
	indicate how the data should be treated.</li>
<li>	If the property is not set, the user will be propted as to what they
	want to do.  They can choose to not continue.</li>
</ol>
The _ignore_units flag is then set.  If necessary, the
TSGraphJComponent.needToClose() method will be called and the graph won't be
displayed.
*/
private boolean _ignore_units = false;

/**
Whether labels should be drawn or not.  Labels are only NOT drawn if the graph
has no data or time series.
*/
private boolean __drawLabels = true;

/**
Graph type.  Reset with properties.
*/
private int _graph_type = TSProduct.GRAPH_TYPE_LINE;

/**
The graph type for the last redraw.
This will force the analysis to be done for analytical graph types when the
graph type changes.
*/
private int _last_graph_type = -1;	

/**
Vector of all time series to plot (this is used for the legend).
*/
private Vector __tslist = null;

/**
Vector of time series to plot using left axis (currently the default).
*/
private Vector __left_tslist = null;	

/**
Precision for left y-axis labels.
*/
private int _lefty_precision = 2;

/**
TSProduct containing information about the graph product to be displayed.
*/
private TSProduct _tsproduct = null;

/**
Display properties used to indicate display characteristics outside the
TSProduct.
*/
private PropList _display_props = null;

/**
TSProduct sub-product number (starting with zero, which is 1 off the value in
the product file).
*/
private int _subproduct = 0;

/**
Background color.
*/
private GRColor _background = GRColor.white;

/**
Popup menu for the graph.
*/
private JPopupMenu _graph_JPopupMenu = null;

/**
Data limits kept separate from the _grda.  These are the data being drawn and
reflect zoom etc.  This is true whether a reference or main graph and reflects
the computation of labels.
*/
private GRLimits _data_limits = null;

/**
Limits for time series data for full period, with no adjustments for nice
labels.
*/
private TSLimits _max_tslimits = null;

/**
Limits for time series data for current zoom.
*/
private TSLimits _tslimits = null;

/**
Maximum data limits based on full period data limits.  Initialie to unit limits
because this is what the default data limits are.  Then, if no data are
available, something reasonable cann still be drawn (e.g., "No Data Available").
*/
private GRLimits _max_data_limits = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );

/**
End date for current zoom level.  Done this way because generic drawing uses
only coordinates - not dates.
*/
private DateTime _end_date;

/**
Start date for current zoom level.  Done this way because generic drawing uses
only coordinates - not dates.
*/
private DateTime _start_date;

/**
End date for maximum data extents.  Done this way because generic drawing uses
only coordinates - not dates.
*/
private DateTime _max_end_date = null;

/**
Start date for maximum data extents.  Done this way because generic drawing uses
only coordinates - not dates.
*/
private DateTime _max_start_date = null;

private TSGraphJComponent _dev = null;

// Drawing areas from top to bottom (and left to right).

private GRJComponentDrawingArea _da_page = null;// Drawing area for full "page"
private GRJComponentDrawingArea _da_maintitle =null;
						// Drawing area for main title
private GRJComponentDrawingArea _da_subtitle = null;
						// Drawing area for sub title
private GRJComponentDrawingArea _da_topx_title = null;
						// Drawing area for top X title
private GRJComponentDrawingArea _da_topx_label = null;
						// Drawing area for top X labels
private GRJComponentDrawingArea _da_lefty_title = null;
						// Drawing area for left Y title
private GRJComponentDrawingArea _da_righty_title = null;
						// Drawing area for right Y
						// title
private GRJComponentDrawingArea _da_lefty_label = null;
						// Drawing area for left Y
						// labels
private GRJComponentDrawingArea _da_graph = null;
						// Drawing area for graph
						// area inside the axes.
private GRJComponentDrawingArea _da_righty_label = null;
						// Drawing area for right Y
						// labels
private GRJComponentDrawingArea _da_bottomx_label = null;
						// Drawing area for bottom X
						// labels
private GRJComponentDrawingArea _da_bottomx_title = null;
						// Drawing area for bottom
						// X title
private GRJComponentDrawingArea _da_bottom_legend = null;
						// Drawing area for bottom
						// legend
private GRJComponentDrawingArea _da_left_legend = null;
						// Drawing area for left legend
private GRJComponentDrawingArea _da_right_legend = null;
						// Drawing area for right legend
private GRJComponentDrawingArea _da_inside_legend = null;
						// Drawing area for inside 
						// legend

// Data and drawing limits from top to bottom (and left to right).

private GRLimits _datalim_page = null;		// Limits for full "page"
private GRLimits _drawlim_page = null;		// Limits for full "page"
private GRLimits _datalim_maintitle =null;	// Limits for main title
private GRLimits _drawlim_maintitle =null;	// Limits for main title
private GRLimits _datalim_subtitle = null;	// Limits for sub title
private GRLimits _drawlim_subtitle = null;	// Limits for sub title
private GRLimits _datalim_topx_title = null;	// Limits for top X title
private GRLimits _drawlim_topx_title = null;	// Limits for top X title
private GRLimits _datalim_topx_label = null;	// Limits for top X labels
private GRLimits _drawlim_topx_label = null;	// Limits for top X labels
private GRLimits _datalim_lefty_title = null;	// Limits for left Y title
private GRLimits _drawlim_lefty_title = null;	// Limits for left Y title
private GRLimits _datalim_righty_title = null;	// Limits for right Y title
private GRLimits _drawlim_righty_title = null;	// Limits for right Y title
private GRLimits _datalim_lefty_label = null;	// Limits for left Y labels
private GRLimits _drawlim_lefty_label = null;	// Limits for left Y labels
// Don't carry this around because it gets confusing
//private GRLimits _datalim_graph = null;		// Limits for graph
// _data_limits are the current viewable limits.  If a reference graph this is
// the highlighted area but not the limits corresponding to _da_graph.
private GRLimits _drawlim_graph = null;		// Limits for graph
private GRLimits _datalim_righty_label = null;	// Limits for right Y labels
private GRLimits _drawlim_righty_label = null;	// Limits for right Y labels
private GRLimits _datalim_bottomx_label = null;// Limits for bottom X labels
private GRLimits _drawlim_bottomx_label = null;// Limits for bottom X labels
private GRLimits _datalim_bottomx_title = null;	// Limits for bottom X title
private GRLimits _drawlim_bottomx_title = null;	// Limits for bottom X title
private GRLimits _datalim_bottom_legend = null;	// Limits for bottom legend
private GRLimits _drawlim_bottom_legend = null;	// Limits for bottom legend
private GRLimits _datalim_left_legend = null;	// Limits for left legend
private GRLimits _drawlim_left_legend = null;	// Limits for left legend
private GRLimits _datalim_right_legend = null;	// Limits for right legend
private GRLimits _drawlim_right_legend = null;	// Limits for right legend
private GRLimits _datalim_inside_legend = null; // Limits for inside legend
private GRLimits _drawlim_inside_legend = null; // Limits for inside legend

// Dimensions for drawing areas...

/**
Is the graph a reference graph?  This is set at construction by checking the
display properties.
*/
private boolean _is_reference_graph = false;

/**
If a reference graph, the index for the time series is selected in the
TSViewGraphFrame class and passed in (so that the reference time series can be
consistent between the main and reference graph canvases.
*/
private int _reference_ts_index = -1;

private String _gtype = "Main:";		// Used for messages so we can
						// tell whether in a main or
						// reference graph.
private boolean _zoom_keep_y_limits = false;	// When zooming, keep the
						// Y axis limits the same
						// (false indicates to
						// recompute from data in the
						// zoom window).
						// false also indicates that
						// users can change the
						// min and max values in the
						// properties JFrame.

private Vector _regression_data = null;		// Regression data used by
						// scatter plot.

//private TSDoubleMass _double_mass_data = null;// Used by double mass plot.

/**
Maximum time interval for time series being plotted.
*/
private int _interval_max = TimeInterval.SECOND;

/**
Minimum time interval for time series being plotted.
*/
private int _interval_min = TimeInterval.YEAR;

/**
Precision for x-axis date data.  This is not private because TSViewGraphGUI
uses the precision for the mouse tracker.
*/
protected int _xaxis_date_precision;

/**
DateTime format to use for bottom x-axis date data.
*/
private int _bottomx_date_format = -1;

private int _xaxis_precision = 2;		// For numeric data.
private double [] _xlabels = null;
private double [] _ylabels = null;

/**
If true, then the dates that were set for start_date and end_date will be
used for computing the date limits.  Otherwise, start_date and end_date will
be recomputed.
*/
private boolean __useSetDates = false;

/**
Construct a TSGraph and display the time series.
@param dev TSGraphJComponent that is managing this graph.
@param drawlim_page Initial device limits that should be used for this graph,
determined in the managing TSGraphJComponent (the limits will change as the
component is resized.
@param tsproduct TSProduct containing information to control display of time
series.  Most of these properties are documented in TSViewFrame, with the
following additions:
@param display_props Additional properties used for displays.
ReferenceGraph can be set to "true" or "false" to indicate whether the graph is
a reference graph.  ReferenceTSIndex can be set to a Vector index to indicate
the reference time series for the reference graph (the default is the time
series with the longest overall period).  This value must be set for the local
tslist Vector that is passed in.
@param subproduct The subproduct from the main product.  This is used to look
up properties specific to this graph product.  The first product is 1.
@param tslist Vector of time series to graph.  Only the time series for this
graph are expected but time series for other graphs can be shared (access them
in the TSProduct).
@param reference_ts_index Index in the "tslist" for the reference time series.
This may be different from the "ReferenceTSIndex" property in display_props,
which was for the original time series list (not the subset used just for this graph).
*/
public TSGraph ( TSGraphJComponent dev, GRLimits drawlim_page, TSProduct tsproduct, PropList display_props,
			int subproduct, Vector tslist, int reference_ts_index )
{
	String routine = "TSGraph";

	// Keep a local reference...

	_dev = dev;
	_tsproduct = tsproduct;
	_display_props = display_props;
	if ( _display_props == null ) {
		_display_props = new PropList ( "TSGraph" );
	}
	_subproduct = subproduct;
	if ( tslist == null ) {
		// Create an empty vector so checks for null don't need to be added everywhere...
        Message.printStatus(2, routine, "Null list of time series for graph.  Using empty list for graph." );
		__tslist = new Vector(1);
	}
	else {
        __tslist = tslist;
	}
	__left_tslist = tslist;

	// Check a few properties to increase performance.  The only properties
	// that are checked and set locally here are those that are not going
	// to change during the life of the graph, even if its properties are
	// changed.  All other properties should be checked before being used
	// (e.g., axis properties should be checked in the drawAxesBack()
	// method).
	// It is a little slower to look up the properties sometimes but the
	// code is simpler to maintain.

	if ( _dev.isReferenceGraph() ) {
		_is_reference_graph = true;
		_gtype = "Ref:";
	}

	//if (Message.isDebugOn) {
		// Might need to use this when we try to process all null time series...
		int ssize = 0;
		if ( __tslist != null ) {
		    ssize = __tslist.size();
		}
        Message.printStatus(2, routine, "Have " + ssize + " time series for graph." );
		TS sts;
		for (int ii = 0; ii < ssize; ii++) {
			sts = (TS)__tslist.elementAt(ii);
			if (sts == null) {
				Message.printStatus(3, routine, _gtype + "TS[" + ii + "] is null");
			}
			else {	
				Message.printStatus(3, routine, _gtype + "TS[" + ii + "] is " + sts.getIdentifierString() +
				        "period " + sts.getDate1() + " to " + sts.getDate2() );
			}
		}
	//}

	// A reference TS index can be used in a main or reference graph...

	_reference_ts_index = reference_ts_index;

	_graph_type = TSProduct.lookupGraphTypeNumber ( tsproduct.getLayeredPropValue ( "GraphType", subproduct, -1, false ) );
	if ( _graph_type < 0 ) {
		// Should never happen...
		_graph_type = TSProduct.GRAPH_TYPE_LINE;
	}

	if (_is_reference_graph) {
		_graph_type = TSProduct.GRAPH_TYPE_LINE;
	}

	_drawlim_page = new GRLimits ( drawlim_page );

	openDrawingAreas ();

	// Perform the data analysis once to get data limits...
	// This is the place where the reference graph has its data set.
	// This is also checked in the paint() method in case any analysis settings change...

	doAnalysis();
	_last_graph_type = _graph_type;

	// Initialize the data limits...

	if ( _is_reference_graph ) {
		_da_graph.setDataLimits ( _max_data_limits );
	}
	else {
        _da_graph.setDataLimits ( _data_limits );
	}

	// Get the units to use for the left y-axis...

	int size = 0;
	if ( __left_tslist != null ) {
		size = __left_tslist.size();
	}
	if ( __left_tslist != null ) {
		TS ts = null;
		for ( int i = 0; i < size; i++ ) {
			ts = (TS)__left_tslist.elementAt(i);
			if ( (ts == null) || !ts.getEnabled() ) {
				continue;
			}
			// Check the interval so that we can make decisions during plotting...
			try {
                _interval_max = MathUtil.max ( _interval_max, ts.getDataIntervalBase() );
				_interval_min = MathUtil.min ( _interval_min, ts.getDataIntervalBase() );
			}
			catch ( Exception e ) {
				// Probably never will occur.
                Message.printWarning (3, routine, e);
			}
		}
		ts = null;
	}
	computeXAxisDatePrecision ();
}

/**
Handle action events generated by the popup menu for this graph
*/
public void actionPerformed(ActionEvent event)
{
	String command = event.getActionCommand ();
	if ( command.equals(__MENU_PROPERTIES) ) {
		// Only one properties window is shown per graph so let the
		// TSViewFrame handle showing the properties
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewJFrame.PROPERTIES);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		// Immediately remove from the component container.
		// This works except that when escape is pressed on the popup
		// menu we never get an action event to remove the menu (is
		// there a better way to handle?  Could always add the popups
		// up front but does this take more resources than having a
		// few extra popups because of escapes?).
		_dev.remove(_graph_JPopupMenu);
		frame = null;
	}
	else if (command.equals(__MENU_ANALYSIS_DETAILS)) {
		// Display the results of the regression(s)
		int size = 0;
		if (_regression_data != null) {
			size = _regression_data.size();
		}
		TSRegression r = null;
		Vector v = new Vector();	
		for (int i = 0; i < size; i++) {
			r = (TSRegression)_regression_data.elementAt(i);
			if (r == null) {
				continue;
			}
			v.addElement(r.toString ());
			v.addElement("");
			v.addElement("");
		}
		
		if (v.size() == 0) {
			Message.printWarning(1, "TSGraph.actionPerformed", "No regression results available.");
		}
		
		PropList p = new PropList("Regression");
		p.set("Title", "Analysis Details");
		p.set("TotalHeight", "400");
		new ReportJFrame(v, p);
		v = null;
		p = null;
		r = null;
		// Immediately remove from the component container.  See note above.
		_dev.remove(_graph_JPopupMenu);
	}
	else if (command.equals(__MENU_REFRESH)) {
		// Refresh the image.  This is probably only used on UNIX
		// because the refresh does not occur automatically.
		_dev.refresh ();
	}
	else if (command.equals(__MENU_Y_MAXIMUM_VISIBLE)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewJFrame.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		TSLimits limits = null;

		Vector tslist = null;
		if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
	    	int nreg = 0;
			if (__tslist != null) {
				nreg = __tslist.size() - 1;
			}
			Vector v = new Vector();
			TSRegression regressionData = null;
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1)) {
					continue;
				}
				regressionData = (TSRegression)_regression_data.elementAt(i);
				v.add(regressionData.getResidualTS());
			}
			tslist = v;
		}
		else if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
	    	int nreg = 0;
			if (__tslist != null) {
				nreg = __tslist.size() - 1;
			}
			Vector v = new Vector();
			TSRegression regressionData = null;
			if (isTSEnabled(0)) {
				v.add(__tslist.elementAt(0));
			}
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1)) {
					continue;
				}
				regressionData = (TSRegression)_regression_data.elementAt(i);
				if (isTSEnabled(i + 1)) {
					v.add(regressionData.getDependentTS());
					v.add(regressionData.getPredictedTS());
				}
			}
			tslist = v;
		}		
		else {
			tslist = getEnabledTSList();
		}
		
		try {
			limits = TSUtil.getDataLimits(tslist, _start_date, _end_date, "", false, _ignore_units);
		}
		catch (Exception e) {
			String routine = "TSGraph.actionPerformed";
			Message.printWarning(2, routine, "There was an error getting the limits for "
				+ "the period.  The zoom will not be changed.");
			Message.printWarning(2, routine, e);
			return;
		}

		if (IOUtil.testing()) {
			Message.printStatus(2, "", "Start: " + _start_date);
			Message.printStatus(2, "", "  End: " + _end_date);
			Message.printStatus(2, "", "  Max: " + limits.getMaxValue());
			Message.printStatus(2, "", "  Min: " + limits.getMinValue());
		}

		pframe.setMaximumYValue("" + limits.getMaxValue(), true);
	}
	else if (command.equals(__MENU_Y_MINIMUM_VISIBLE)) {
		// currently only supported for residual graphs
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewJFrame.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		TSLimits limits = null;

    	int nreg = 0;
		if (__tslist != null) {
			nreg = __tslist.size() - 1;
		}
		Vector v = new Vector();
		TSRegression regressionData = null;
		for (int i = 0; i < nreg; i++) {
			if (!isTSEnabled(i + 1)) {
				continue;
			}
			regressionData = (TSRegression)_regression_data.elementAt(i);
			v.add(regressionData.getResidualTS());
		}
		
		try {
			limits = TSUtil.getDataLimits(v,_start_date, _end_date, "", false,_ignore_units);
		}
		catch (Exception e) {
			String routine = "TSGraph.actionPerformed";
			Message.printWarning(2, routine, "There was an error getting the limits for "
				+ "the period.  The zoom will not be changed.");
			Message.printWarning(2, routine, e);
			return;
		}

		if (IOUtil.testing()) {
			Message.printStatus(2, "", "Start: " + _start_date);
			Message.printStatus(2, "", "  End: " + _end_date);
			Message.printStatus(2, "", "  Max: " + limits.getMaxValue());
			Message.printStatus(2, "", "  Min: " + limits.getMinValue());
		}

		pframe.setMinimumYValue("" + limits.getMinValue(),true);
	}	
	else if (command.equals(__MENU_Y_MAXIMUM_AUTO)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewJFrame.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		pframe.setMaximumYValue("Auto");
	}
	else if (command.equals(__MENU_Y_MINIMUM_AUTO)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewJFrame.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		pframe.setMinimumYValue("Auto", true);
	}	
}

/**
Adjust a confidence curve for the XY Scatter plot to make sure the points lie
within the graph.  Most likely some points will be off the graph.  In these
cases, interpolate coordinates to the edge of the graph.  If the next/previous
point is also off the graph, then set the point to the last endpoint.  This
makes sure we have cleanly handled the end points without having to adjust the
array lengths.
@param x X-coordinates for confidence curve.
@param y Y-coordinates for confidence curve.
@param npts Number of points to process (may be less than array size).
*/
private void adjustConfidenceCurve ( double [] x, double [] y, int npts )
{	String routine = "TSGraph.adjustConfidenceCurve";
    // First figure out if the slope of the line is up to the right or down to the right...
	int i = 0;
	double x_edge = 0.0;
	double min_datay = _data_limits.getMinY();
	double max_datay = _data_limits.getMaxY();
	if ( y[0] < y[npts - 1] ) {
		// Slope is up and to the right.  Adjust the points on the left...
		for ( i = 0; i < npts; i++ ) {
			if ( y[i] >= min_datay ) {
				// Found the first Y that is on the graph.  Interpolate the previous point...
				if ( i == 0 ) {
					// No need to do more...
					break;
				}
				x_edge = MathUtil.interpolate ( min_datay, y[i - 1], y[i], x[i - 1], x[i] );
				x[i - 1] = x_edge;
				y[i - 1] = min_datay;
				// Now set all previous points to the interpolated edge value.
				for (	int i2 = (i - 2); i2 >= 0; i2-- ) {
					y[i2] = min_datay;
					x[i2] = x_edge;
				}
				// Done adjusting first points...
				break;
			}
		}
		// Adjust the points on the right...
		for ( i = (npts - 1); i >= 0; i-- ) {
			if ( y[i] <= max_datay ) {
				// Found the first Y that is on the graph.  Interpolate the previous point...
				if ( i == (npts - 1) ) {
					// No need to do more...
					break;
				}
				x_edge = MathUtil.interpolate ( max_datay, y[i + 1], y[i], x[i + 1], x[i] );
				x[i + 1] = x_edge;
				y[i + 1] = max_datay;
				// Now set all previous points to the interpolated edge value.
				for (	int i2 = (i + 2); i2 < npts; i2++ ) {
					y[i2] = max_datay;
					x[i2] = x_edge;
				}
				// Done adjusting last points...
				break;
			}
		}
	}
	else {
        // Slope is down to the right...
		// FIXME ... do later... inverse correlation not likely with what we are doing.
        Message.printWarning(3, routine, "Negative slope is not supported for confidence curve.");
	}
}

/**
Indicate whether the graph can zoom.  Currently this is tied to the graph type.
Later it may be tied to a property.
@return true if the graph can zoom, false otherwise.
*/
public boolean canZoom() {
	if ((_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) 
 	    || (_graph_type == TSProduct.GRAPH_TYPE_DURATION)) {
		return false;
	}
	else {	
		return true;
	}
}

/**
Check properties that may change dynamically and, if necessary, reset internal
variables that correspond to properties.  Internal data are used to increase
performance, especially for data that are used often (e.g., the axis precision
and units, which are used for the mouse tracker).  This method should be called
immediately before drawing.
*/
private void checkInternalProperties ()
{	// "BottomXAxisLabelFormat" = _bottomx_date_format;

	String prop_val = _tsproduct.getLayeredPropValue (
		"BottomXAxisLabelFormat", _subproduct, -1, false );
	if ( prop_val != null ) {
		// Currently only handle special cases...
		if ( prop_val.equalsIgnoreCase("MM-DD") ) {
			_bottomx_date_format = DateTime.FORMAT_MM_DD;
		}
	}

	// "LeftYAxisLabelPrecision" = _lefty_precision;

	_lefty_precision = 2;
	prop_val = _tsproduct.getLayeredPropValue (
		"LeftYAxisLabelPrecision", _subproduct, -1, false );
	if ( prop_val != null ) {
		_lefty_precision = StringUtil.atoi ( prop_val );
	}
}

/**
Compute the maximum data limits based on the time series.  This is normally
only called from doAnalysis(), which is called at construction.  The maximum
values and the current data limits are set to the limits, which serve as the
initial data limits until zooming occurs.
@param max whether to compute data limits from the max dates or not.  Really
only applies currently to empty graphs.  For other graphs, see setComputeWithSetDates().
*/
protected void computeDataLimits (boolean max)
{	String routine = "TSGraph.computeDataLimits";
	// Exceptions are thrown when trying to draw empty graph (no data)
	if (getEnabledTSList().size() == 0) {
		__useSetDates = false;
		if (max) {
			if (_max_start_date == null) {
				_data_limits = new GRLimits(0, 0, 0, 0);
				_max_data_limits = new GRLimits(_data_limits);
			}
			else {
				_data_limits = new GRLimits( _max_start_date.toDouble(), 0, _max_end_date.toDouble(), 1);
				_max_data_limits = new GRLimits(_data_limits);
			}
		}
		else {
			if (_start_date == null) {
				_data_limits = new GRLimits(0, 0, 0, 0);
				_max_data_limits = new GRLimits(_data_limits);
			}
			else {
				_data_limits = new GRLimits( _start_date.toDouble(), 0,	_end_date.toDouble(), 1);
				_max_data_limits = new GRLimits(_data_limits);
			}
		}
		return;
	}

	try {
        // First get the date limits from the full set of time series...
		TSLimits limits = null;
		if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
	    	int nreg = 0;
			if (__tslist != null) {
				nreg = __tslist.size() - 1;
			}
			Vector v = new Vector();
			TSRegression regressionData = null;
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1)) {
					continue;
				}
				regressionData = (TSRegression)_regression_data.elementAt(i);
				v.add(regressionData.getResidualTS());
			}

			limits = TSUtil.getPeriodFromTS(v, TSUtil.MAX_POR);
		}
		else {
			limits = TSUtil.getPeriodFromTS(getEnabledTSList(), TSUtil.MAX_POR);
		}

		if (__useSetDates) {
		}
		else {
			_start_date = new DateTime ( limits.getDate1() );
			_end_date = new DateTime ( limits.getDate2() );
			_max_start_date = new DateTime ( _start_date );
			_max_end_date = new DateTime ( _end_date );

		}
		__useSetDates = false;
		limits = null;	// Clean up
		// Now get the data limits.  To do the check correctly, the data units must be considered.
		_ignore_units = false;
		// First set defaults...
		if ( (_graph_type == TSProduct.GRAPH_TYPE_DOUBLE_MASS) || (_graph_type == TSProduct.GRAPH_TYPE_PERIOD) ||
			(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ) {
			_ignore_units = true;
		}
		// Now check the property (keep a separate copy so we can avoid the prompt below if appropriate)...
		String ignore_units_prop = _tsproduct.getLayeredPropValue (	"LeftYAxisIgnoreUnits", _subproduct, -1, false);
		boolean ignore = false;
		if ( (ignore_units_prop != null) && ignore_units_prop.equalsIgnoreCase("true") ) {
			_ignore_units = true;
			if (TSUtil.areUnitsCompatible(getEnabledTSList(), true)) {
				_ignore_units = false;
				_tsproduct.setPropValue("LeftYAxisIgnoreUnits", "false", _subproduct, -1);
				ignore = true;
			}
		}
		try {	
			if (_ignore_units) {
				// Can ignore units...
				_max_tslimits = TSUtil.getDataLimits( getEnabledTSList(), _start_date, _end_date, "", false, _ignore_units);
			}
			else {
                // Need to have consistent units.  For now require them to be the same because we don't
				// want to do units conversions on the fly or alter the original data...
				//
				// TODO - need to add on the fly conversion of units (slower but changing original data is
				// a worse alternative).
				if (!TSUtil.areUnitsCompatible(
				    getEnabledTSList(), true)) {
					if (_is_reference_graph) {
						// Rely on the main graph to set the _ignore_units flag and determine whether the graph
						// view needs to be closed.  Assume that the _ignore_units flag can be set to true since
						// the reference graph only displays one graph.
						_ignore_units = true;
						_max_tslimits = TSUtil.getDataLimits( __tslist, _start_date,
							_end_date, "", false, _ignore_units);
					}
					else if (ignore) {
						_max_tslimits = TSUtil.getDataLimits( getEnabledTSList(), 
							_start_date, _end_date, "", false, _ignore_units);
					}
					else {	
						// Let the user interactively indicate whether to continue.
						// If running in batch mode, there may not be a parent.
						int x = ResponseJDialog.YES;
						if ( _dev.getJFrame() != null ){
							x = new ResponseJDialog( _dev.getJFrame(), "Continue Graph?",
							"The data units are incompatible\n" + "Continue graphing anyway?",
							ResponseJDialog.YES|ResponseJDialog.NO ).response();
						}
						else {
                            // No frame so default to ignore units...
							x = ResponseJDialog.YES;
						}
						if ( x == ResponseJDialog.NO ) {
							// Set this so that code that uses this component can check to see if the
							// component needs to close itself.
							_dev.needToClose( true);
						}
						else {	
							_ignore_units = true;
							_max_tslimits = TSUtil.getDataLimits( getEnabledTSList(), _start_date,
							        _end_date, "", false, _ignore_units);
						}
					}
				}
				else {	
                	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
                		int nreg = 0;
                		if (__tslist != null) {
                			nreg = __tslist.size() - 1;
                		}
                	
                		Vector v = new Vector();
                		TSRegression regressionData = null;
                		TSLimits tempLimits = null;
                		double maxValue = 0;
                		double minValue = 0;
                		for (int i = 0; i < nreg; i++) {
                			if (!isTSEnabled(i + 1)) {
                				continue;
                			}
                			regressionData = (TSRegression)_regression_data.elementAt(i);
                			v.add(regressionData.getResidualTS());
                			tempLimits = TSUtil.getDataLimits(v, _start_date, _end_date, "", false, _ignore_units );
                			if (tempLimits.getMaxValue() > maxValue) {
                				maxValue = tempLimits.getMaxValue();
                			}
                			if (tempLimits.getMinValue() < minValue) {
                				minValue = tempLimits.getMinValue();
                			}
                		}
                		_max_tslimits = TSUtil.getDataLimits(getEnabledTSList(), 
                			_start_date, _end_date, "", false, _ignore_units);
                		_max_tslimits.setMaxValue(maxValue);
                		_max_tslimits.setMinValue(minValue);
                	}
                	else {
                		_max_tslimits = TSUtil.getDataLimits(getEnabledTSList(), 
                			_start_date, _end_date, "", false, _ignore_units);
                	}
				}
			}
			// If a period graph, the limits should be a count of
			// the time series, 0 to 1 more than the time series
			// count.  Reverse the axis so the number is correct...
			if ( _graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
				_max_tslimits.setMaxValue(0.0);
				_max_tslimits.setMinValue(getEnabledTSList().size() + 1);
			}
		}
		catch ( Exception e ) {
			// This typically throws an exception if the data are not of consistent units.
			if ( !_is_reference_graph ) {
				Message.printWarning ( 1, routine, "Data are not compatible (different units?).  Cannot graph." );
				Message.printWarning ( 2, routine, e );
			}
		}
		if ( _max_tslimits == null ) {
			// Typically due to a cancel of the graph due to incompatible units.  In this case we get to here but
			// just need to gracefully handle nulls until the graph can be closed in parent container code...
			return;
		}
		if (_is_reference_graph && (_reference_ts_index >= 0)) {
			// Reset the coordinates based only on the reference time series but use the full period for dates...
			Vector ref_tslist = new Vector(1,1);
			ref_tslist.addElement((TS)__tslist.elementAt(_reference_ts_index));
			TSLimits reflimits = TSUtil.getDataLimits (	ref_tslist, _start_date, _end_date, "", false,_ignore_units );
			_max_tslimits.setMinValue ( reflimits.getMinValue() );
			_max_tslimits.setMaxValue ( reflimits.getMaxValue() );
			reflimits = null;	// clean up
			ref_tslist = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, _gtype + "Reference graph max data limits are " + _max_tslimits );
			}
		}
		else {
            if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, _gtype + "Main graph max data limits are " + _max_tslimits );
			}
			// If the properties are given, set the limits to thegiven properties,
            // but only if they are outside the range of the data that was determined...
			//
			// TODO SAM 2006-09-28
			// Still need to evaluate how switching between Auto and hard limits can be handled better.
			String prop_value = _tsproduct.getLayeredPropValue ( "LeftYAxisMax", _subproduct, -1, false);
			if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
				double ymax = StringUtil.atod(prop_value);
				if (!_zoom_keep_y_limits && ymax > _max_tslimits.getMaxValue()) {
					_max_tslimits.setMaxValue(ymax);
				}
			}
			else if (prop_value != null && prop_value.equalsIgnoreCase("Auto")) {
            	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
            		int nreg = 0;
            		if (__tslist != null) {
            			nreg = __tslist.size() - 1;
            		}
            	
            		Vector v = new Vector();
            		TSRegression regressionData = null;
            		double minValue = 0;
            		double maxValue = 0;
            		TSLimits tempLimits = null;
            		for (int i = 0; i < nreg; i++) {
            			if (!isTSEnabled(i + 1)) {
            				continue;
            			}
            			regressionData = (TSRegression)_regression_data.elementAt(i);
            			v.add(regressionData.getResidualTS());
            			tempLimits = TSUtil.getDataLimits(v, _max_start_date,_max_end_date, "", false, _ignore_units );
            			if (tempLimits.getMaxValue() > maxValue) {
            				maxValue = tempLimits.getMaxValue();
            			}
            			if (tempLimits.getMinValue() < minValue) {
            				minValue = tempLimits.getMinValue();
            			}
            		}
            
            		_max_tslimits.setMaxValue(maxValue);
            		_max_tslimits.setMinValue(minValue);
            	}
            	else {
            		TSLimits tempLimits = TSUtil.getDataLimits(getEnabledTSList(), 
            			_max_start_date, _max_end_date, "", false, _ignore_units);
            		_max_tslimits.setMaxValue(tempLimits.getMaxValue());
            	}
			// TODO SAM 2006-09-28
			// Still need to evaluate how switching between Auto and hard limits can be handled better.
			}	
			prop_value = _tsproduct.getLayeredPropValue ("LeftYAxisMin", _subproduct, -1, false);
			if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
				double ymin = StringUtil.atod(prop_value);
				if (!_zoom_keep_y_limits && ymin < _max_tslimits.getMinValue()) {
					_max_tslimits.setMinValue(ymin);
				}
			}
		}
		_tslimits = new TSLimits ( _max_tslimits );
		// Initialize this here because this is what the reference
		// graph uses throughout (it does not need nice labels).
		if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
			boolean xlimits_found = false;
			boolean ylimits_found = false;
			TSRegression regressionData = null;
			int nregression = 0;
			if ( _regression_data != null ) {
				nregression = _regression_data.size();
			}
			double xmin = 0.0, ymin = 0.0, xmax = 1.0, ymax = 1.0;
			for ( int ir = 0; ir < nregression; ir++ ) {
				regressionData = (TSRegression)_regression_data.elementAt(ir);
				if ( (regressionData != null) && !regressionData.isMonthlyAnalysis() ) {
					// One equation...
					if ( regressionData.isAnalyzed() ) {
						// Analysis was successful so full data are available...
						//
						// Get the limits from the regression data since some data may have been ignored...
						// Y is the 2nd+ time series picked.
						if ( xlimits_found ) {
							xmin = MathUtil.min(xmin,regressionData.getMinX1());
							xmax = MathUtil.max(xmax, regressionData.getMaxX1());
						}
						else {
                            xmin=regressionData.getMinX1();
							xmax=regressionData.getMaxX1();
							xlimits_found = true;
						}
						if ( ylimits_found ) {
							ymin = MathUtil.min(ymin,regressionData.getMinY1());
							ymax = MathUtil.max(ymax,regressionData.getMaxY1());
						}
						else {
                            ymin=regressionData.getMinY1();
							ymax=regressionData.getMaxY1();
							ylimits_found = true;
						}
					}
					else if ( regressionData.getN1() == 1){
						// Regression was not successful but if no line was requested, still plot the
						// points...  special case found in snow GUI.
						/* TODO - for now do regardless of whether the line	was requested.
						Message.printStatus(1, "SAMX", "No line but number of"+
						String prop_val = _tsproduct.getLayeredPropValue ("RegressionLineEnabled",_subproduct, ir,false);
						if ( (prop_val != null) && !prop_val.equalsIgnoreCase("true") ) {
							Message.printStatus(1,"SAMX", "No line but number of points is regressionData.getN1() );
						}
						*/
						// What we really want are the common limits, but that may
						// not be available - need to do some work in TSRegression.
						TS rts= regressionData.getIndependentTS();
						TSLimits lim = rts.getDataLimits();
						if ( (lim != null) && (!rts.isDataMissing(lim.getMinValue()) )) {
    						if ( xlimits_found ) {
    							xmin = MathUtil.min(xmin,lim.getMinValue());
    							xmax = MathUtil.max(xmax,lim.getMaxValue());
    						}
    						else {
                                xmin=lim.getMinValue();
    							xmax=lim.getMaxValue();
    							xlimits_found = true;
    						}
						}
						rts= regressionData.getDependentTS();
						lim = rts.getDataLimits();
						if ( (lim != null) && (!rts.isDataMissing(lim.getMinValue()) )) {
    						if ( ylimits_found ) {
    							ymin = MathUtil.min(ymin,lim.getMinValue());
    							ymax = MathUtil.max( ymax,lim.getMaxValue());
    						}
    						else {
                                ymin=lim.getMinValue();
    							ymax=lim.getMaxValue();
    							ylimits_found = true;
						}
						}
					}
				}
				else if ((regressionData != null) && regressionData.isMonthlyAnalysis() ) {
					boolean [] analyze_month = regressionData.getAnalyzeMonth();
					// Monthly analysis...
					//
					// Get the limits from the regression data since some data may have been ignored...
					// Y is the 2nd+ time series picked.
					for ( int il = 1; il <= 12; il++ ) {
					if ( !regressionData.isAnalyzed(il) ) {
						continue;
					}
					if ( !analyze_month[il - 1] ) {
						continue;
					}
					if ( xlimits_found ) {
						xmin = MathUtil.min(xmin,regressionData.getMinX1(il));
						xmax = MathUtil.max(xmax,regressionData.getMaxX1(il));
					}
					else {
                        xmin=regressionData.getMinX1(il);
						xmax=regressionData.getMaxX1(il);
						xlimits_found = true;
					}
					if ( ylimits_found ) {
						ymin = MathUtil.min(ymin,regressionData.getMinY1(il));
						ymax = MathUtil.max(ymax,regressionData.getMaxY1(il));
					}
					else {
                        ymin=regressionData.getMinY1(il);
						ymax=regressionData.getMaxY1(il);
						ylimits_found = true;
					}
					}
				}
			}

			if (!xlimits_found) {
				// Use the full limits...
				TS ts0 = (TS)__tslist.elementAt(0);
				TSLimits limits0 = ts0.getDataLimits();
				xmin = limits0.getMinValue();
				xmax = limits0.getMaxValue();
				ts0 = null;
				limits0 = null;
			}

			if (!ylimits_found) {
				// Loop through the Y axis time series (hopefully this code is never executed).
				TS ts = null;
				TSLimits ylimits = null;
				for ( int its = 1; its <= nregression; its++ ) {
					ts = (TS)__tslist.elementAt(its);
					if (ts == null || !isTSEnabled(its)) {
						continue;
					}
					ylimits = ts.getDataLimits();
					if ( ylimits_found ) {
						ymin = MathUtil.min ( ymin, ylimits.getMinValue() );
						ymax = MathUtil.min ( ymax, ylimits.getMaxValue() );
					}
					else {
                        ymin = ylimits.getMinValue();
						ymax = ylimits.getMaxValue();
						ylimits_found = true;
					}
				}
				ts = null;
				ylimits = null;
			}
			// Set the limits regardless.  Worst case they will be zero to one...
			_data_limits = new GRLimits ( xmin, ymin, xmax, ymax );
		}
		else if ( _graph_type == TSProduct.GRAPH_TYPE_DURATION ) {
			// X limits are 0 to 100.  Y limits are based on the time series...
			_data_limits = new GRLimits ( 0.0, 0.0,	100.0, _tslimits.getMaxValue() );
		}
		else if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL
		    || _graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
		    	boolean residual = true;
			if (_graph_type==TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
				residual = false;
			}
			TSRegression regressionData = null;
			int nregression = 0;
			if (_regression_data != null) {
				nregression = _regression_data.size();
			}
			TSLimits tsLimits = null;
			double maxValue = 0;
			double minValue = 0;
			Vector tempV = null;
			for (int ir = 0; ir < nregression; ir++) {
				regressionData = (TSRegression)_regression_data.elementAt(ir);
				tempV = new Vector();
				tsLimits = null;
				if (regressionData != null) {
				    if (residual) { 
						tempV.add(regressionData.getResidualTS());
					}
					else {
					   	tempV.add(regressionData.getIndependentTS());
						tempV.add(regressionData.getDependentTS());
						tempV.add(regressionData.getPredictedTS());
					}
					tsLimits = TSUtil.getDataLimits( tempV, _max_start_date, _max_end_date, "", false, _ignore_units);
				}
				else {
					// ignore -- null regression data
				}

				if (tsLimits != null) {
					if (tsLimits.getMaxValue() > maxValue) {
						maxValue = tsLimits.getMaxValue();
					}
					if (tsLimits.getMinValue() < minValue) {
						minValue = tsLimits.getMinValue();
					}
				}
			}
			_data_limits = new GRLimits ( _start_date.toDouble(), minValue, _end_date.toDouble(), maxValue);
		}
		else {	
			_data_limits = new GRLimits (_start_date.toDouble(), _tslimits.getMinValue(),
				_end_date.toDouble(),_tslimits.getMaxValue() );
		}

		if (_data_limits != null) {
			if (max) {
				_max_data_limits = new GRLimits(_data_limits);
			}
			else {
				if (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) {
				    	_max_data_limits = new GRLimits(_data_limits);
				}
				else {
    				GRLimits tempLimits = new GRLimits( _max_start_date.toDouble(), _tslimits.getMinValue(),
    				        _max_end_date.toDouble(),_tslimits.getMaxValue() );
    				_max_data_limits = new GRLimits(tempLimits);
				}
			}

			if (Message.isDebugOn) {
				Message.printDebug ( 1, routine, _gtype	+ "Initial computed _max_data_limits "
					+ "are " + _max_data_limits.toString());
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(3, routine, _gtype + "Error getting dates for plot.");
		Message.printWarning(3, _gtype + "TSGraph", e);
		_dev.needToClose(true);
	}
}

/**
Compute the labels given the current zoomed data.  Call this after the data
limits have initially been set.  The label values are computed based on the
drawing area size and the axis font to make sure that labels do not overlap.
This sets _datalim_graph.
@param limits For data that is being used (generally the max or current
limits - whatever the graph is supposed to display).  <b>This is time series
data so for scatter plots, etc., it does not contain all that is needed.
*/
private void computeLabels ( TSLimits limits )
{   String routine = "TSGraph.computeLabels";
	if ( (_da_graph == null) || (limits == null) ) {
		// Have not initialized the drawing areas yet or bad graph data...
		// JTS
		// otherwise exceptions thrown when drawing an empty graph
		_ylabels = new double[1];
		_ylabels[0] = 0;
		__drawLabels = false;
		return;
	}
	else {
		__drawLabels = true;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Computing labels using TS limits: " +	limits.toString() );
	}

	boolean log_y = false;
	boolean log_xy_scatter = false;
	String prop_value = _tsproduct.getLayeredPropValue(	"LeftYAxisType", _subproduct, -1, false);
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = true;
	}

	prop_value = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
		log_xy_scatter = true;
	}	

	// Now get recompute the limits to be nice.  First do the Y axis...
	// The maximum number of labels is based on the font height and the
	// drawing area height.  However, in most cases, we want at least a
	// spacing of 3 times the font height, unless this results in less than
	// 3 labels.
	double height, width;
	// Format a label based on the font for the Y axis...

	String fontname = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", _subproduct, -1, false );
	String fontsize = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", _subproduct, -1, false );
	String fontstyle = _tsproduct.getLayeredPropValue (	"LeftYAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_lefty_label, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRLimits label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, "astring", GRUnits.DEVICE );
	height = label_extents.getHeight();
	width = label_extents.getWidth();
	int minlabels = (int)(_drawlim_graph.getHeight()/(height*6.0));
	if ( minlabels < 3 ) {
		minlabels = 3;
	}
	int maxlabels = (int)(_drawlim_graph.getHeight()/(height*2.0));
	if ( maxlabels < minlabels ) {
		maxlabels = minlabels*2;
	}

	// TODO (JTS - 2004-03-03)
	// logic for determining max and min number of labels is screwy when
	// creating new graphs sometimes.  Puts fewer labels in than look like should be in there.
	
	if (log_y) {
		if ( (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) && (_regression_data != null) ) {
			// Old used data from the regression...
			//_ylabels = GRAxis.findLogLabels (_regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data...
			_ylabels = GRAxis.findLogLabels (_data_limits.getMinY(),_data_limits.getMaxY() );
		}
		else {
		    _ylabels = GRAxis.findLogLabels ( limits.getMinValue(),	limits.getMaxValue() );
		}
	}
	else if (log_xy_scatter) {
		if ( (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) && (_regression_data != null) ) {
			// Old used data from the regression...
			//_ylabels = GRAxis.findLogLabels (_regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data...
			_ylabels = GRAxis.findLogLabels ( _data_limits.getMinY(), _data_limits.getMaxY() );
		}
		else {
		    _ylabels = GRAxis.findLogLabels ( limits.getMinValue(),	limits.getMaxValue() );
		}
	}
	else if ( _graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
		// Y-labels are whole numbers...
		_ylabels = new double[getEnabledTSList().size()];
		for ( int i = 0; i < getEnabledTSList().size(); i++ ) {
			_ylabels[i] = i + 1;
		}
	}
	else {
	    // Linear.  Minimum and maximum number of labels as computed above...
		if ( (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) && (_regression_data != null) ) {
			while ( minlabels >= 3 ) {
				// Make sure the max values properly account for the other axis...
				// Old...
				//_ylabels = GRAxis.findNLabels ( _regression_data.getMin2(), _regression_data.getMax2(),
					//false, minlabels, maxlabels );
				_ylabels = GRAxis.findNLabels (	_data_limits.getMinY(), _data_limits.getMaxY(),
					false, minlabels, maxlabels );
				if ( _ylabels != null ) {
					break;
				}
				--minlabels;
			}
		}
		else {	
		    while ( minlabels >= 3 ) {
				_ylabels = GRAxis.findNLabels (	limits.getMinValue(),limits.getMaxValue(),
					false, minlabels, maxlabels );
				if ( _ylabels != null ) {
					break;
				}
				--minlabels;
			}
		}
	}

	if ( _ylabels == null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Unable to find labels using " +
			minlabels + " to " + maxlabels + " labels.  Using end-point data values." );
		}
		_ylabels = new double [2];
		if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
			_ylabels[0] = _data_limits.getMinY();
			_ylabels[1] = _data_limits.getMaxY();
			_data_limits = new GRLimits ( _max_data_limits.getMinX(), _ylabels[0],
				_max_data_limits.getMaxX(), _ylabels[1] );
		}
		else {	_ylabels[0] = limits.getMinValue();
			_ylabels[1] = limits.getMaxValue();
			_data_limits = new GRLimits ( _start_date.toDouble(), _ylabels[0],
					_end_date.toDouble(), _ylabels[1] );
		}
	}
	else {	
		if (_graph_type == TSProduct.GRAPH_TYPE_PERIOD) {
			_data_limits = new GRLimits ( _start_date.toDouble(), (getEnabledTSList().size() + 1),
				_end_date.toDouble(), 0.0);
		}
		else if (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
			_data_limits = new GRLimits ( _max_data_limits.getMinX(), _ylabels[0],
				_max_data_limits.getMaxX(),	_ylabels[_ylabels.length - 1]);
		}
		else {	
			_data_limits = new GRLimits ( _start_date.toDouble(), _ylabels[0],
				_end_date.toDouble(), _ylabels[_ylabels.length - 1]);
		}
		
		if (Message.isDebugOn) {
			Message.printDebug(1, routine, _gtype + "Found " + _ylabels.length 
				+ " labels requesting " + minlabels + " to " + maxlabels);
		}
	}
	if ( !_is_reference_graph ) {
		_da_graph.setDataLimits ( _data_limits );
	}
	int size = _ylabels.length;
	int i = 0;
	if ( Message.isDebugOn ) {
		for ( i = 0; i < size; i++ ) {
			Message.printDebug ( 1, routine, _gtype + "_ylabel[" + i + "]=" + _ylabels[i] );
		}
	}

	// X axis labels:
	//
	// If normal plot, based on the dates for the current zoom.
	// If a scatter plot, based on data limits.
	// If a duration plot, based on 0 - 100 percent.

	fontname = _tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, fontname, fontstyle,	StringUtil.atod(fontsize) );

	if ( _graph_type == TSProduct.GRAPH_TYPE_DURATION ) {
		// Limits are 0 to 100.0..
		String maxstring = StringUtil.formatString(	(double)100.0, "%.0f");
		label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(_drawlim_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(_drawlim_graph.getHeight()/(width*1.5));
		if ( maxlabels < minlabels ) {
			maxlabels = minlabels*2;
		}
		while ( minlabels >= 3 ) {
			_xlabels = GRAxis.findNLabels ( 0.0, 100.0, false, minlabels, maxlabels );
			if ( _xlabels != null ) {
				break;
			}
			--minlabels;
		}
		if ( _xlabels == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, _gtype + "Unable to find X labels using " +
				minlabels + " to " + maxlabels + " labels.  Using data values." );
			}
			_xlabels = new double [2];
			_xlabels[0] = _data_limits.getMinX();
			_xlabels[1] = _data_limits.getMaxX();
		}
		_data_limits = new GRLimits ( _xlabels[0], _ylabels[0],
					_xlabels[_xlabels.length - 1], _ylabels[_ylabels.length - 1] );
		if ( !_is_reference_graph ) {
			_da_graph.setDataLimits ( _data_limits );
		}
		maxstring = null;
		return;
	}
	else if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
		// Labels are based on the _data_limits...
		// Need to check precision for units but assume .1 for now...
		String maxstring = StringUtil.formatString(	_data_limits.getMaxX(), "%." + _xaxis_precision + "f");
		label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(_drawlim_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(_drawlim_graph.getHeight()/(width*1.5));

		prop_value = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
		boolean asLog = false;
		if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")){
			asLog = true;
		}

		if ( maxlabels < minlabels ) {
			maxlabels = minlabels*2;
		}
		while ( minlabels >= 3 ) {
			if ( _regression_data != null ) {
				// Old...
				//_xlabels = GRAxis.findNLabels ( _regression_data.getMin1(),
					//_regression_data.getMax1(), false, minlabels, maxlabels );
				if (asLog) {
				    _xlabels = GRAxis.findNLabels (
//					    Math.pow(10, _data_limits.getMinX()), Math.pow(10, _data_limits.getMaxX()),
				        _data_limits.getMinX(),	_data_limits.getMaxX(),	false, minlabels, maxlabels );
				}
				else {
				    _xlabels = GRAxis.findNLabels (	_data_limits.getMinX(),
					_data_limits.getMaxX(),	false, minlabels, maxlabels );
				}
			}
			else {
			    // Use the limits of the time series data...
				_xlabels = GRAxis.findNLabels (	limits.getMinValue(),limits.getMaxValue(),
					false, minlabels, maxlabels );
			}
			if ( _xlabels != null ) {
				break;
			}
			--minlabels;
		}
		if ( _xlabels == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, _gtype + "Unable to find X labels using " +
				minlabels + " to " + maxlabels + " labels.  Using data values." );
			}
			_xlabels = new double [2];
			_xlabels[0] = limits.getMinValue();
			_xlabels[1] = limits.getMaxValue();
		}
		_data_limits = new GRLimits ( _xlabels[0], _ylabels[0],
					_xlabels[_xlabels.length - 1], _ylabels[_ylabels.length - 1] );
		if ( !_is_reference_graph ) {
			_da_graph.setDataLimits ( _data_limits );
		}
		maxstring = null;
		return;
	}

	// Remainder is x-axis with dates...

	// First get the extents of a typical label, based on graphics and precision of label...

	DateTime date = new DateTime ( 2000.5, true );
	date.setPrecision ( _xaxis_date_precision );
	// Font for _grda was set above.  Get limits - we are interested in horizontal
	// positioning based on dates...
	label_extents = GRDrawingAreaUtil.getTextExtents( _da_bottomx_label, date.toString(), GRUnits.DEVICE );
	width = label_extents.getWidth();
	// Maintain spacing of at least one label width...
	int nlabels = (int)(_drawlim_graph.getWidth()/(width*2.0));
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Number of X labels is " + nlabels );
	}

	// Number of months in data...

	int nmonths = _end_date.getAbsoluteMonth() - _start_date.getAbsoluteMonth() + 1;
	Vector x_axis_labels_temp = new Vector (10,10);

	int delta = 0;
	if ( _xaxis_date_precision == DateTime.PRECISION_YEAR ) {
		// Yearly data...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Determining Year labels" );
		}
		if ( nlabels != 0 ) {
			delta = (_end_date.getYear() - _start_date.getYear() + 1)/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( _start_date );
		for ( i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.addElement (	new Double(date.toDouble() ) );
			date.addYear ( delta );
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_MONTH ) {
		// Monthly data...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Determining month labels" );
		}
		if ( nlabels != 0 ) {
			delta = nmonths/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( _start_date );
		for ( i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, _gtype + "Label is for " + date.toString() );
			}
			x_axis_labels_temp.addElement (	new Double(date.toDouble() ) );
			date.addMonth ( delta );
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_DAY ) {
		// Daily data...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1,	routine, _gtype + "Determining day labels" );
		}
		int ndays = _end_date.getAbsoluteDay() - _start_date.getAbsoluteDay() + 1;
		if ( nlabels != 0 ) {
			delta = ndays/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( _start_date );
		for ( i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.addElement (	new Double(date.toDouble() ) );
			date.addDay ( delta );
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_HOUR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Determining hour labels" );
		}
		
		// Could be irregular data...
		int nhours = 0;

		TS ts = getFirstEnabledTS();
		// Try to find first non-null time series
		int nts = 0;
		if ( __tslist != null ) {
		    nts = __tslist.size();
		}
		Object o = null;
		for ( int its = 0; its < nts; ++its ) {
		        o = __tslist.elementAt(its);
		        if ( o != null ) {
		               ts = (TS)o;
		               break;
		        }
		}
		if (ts == null) {
		    // FIXME SAM 2008-01-13 What do do in this situation?
		    // Unable to get time series to evaluate
		    // Hopefully a check is occurring prior to this to detect all null data.
		    if ( Message.isDebugOn ) {
		        Message.printDebug ( 10, routine, "No time series are non-null.");
		    }
		    return;
		}
		int dataIntervalBase = ts.getDataIntervalBase();
		
		if (dataIntervalBase == TimeInterval.IRREGULAR) {
			nhours = (int)(
				TimeUtil.absoluteMinute(_end_date.getYear(),_end_date.getMonth(),
					_end_date.getDay(),	_end_date.getHour(),_end_date.getMinute() ) 
				- TimeUtil.absoluteMinute(_start_date.getYear(),_start_date.getMonth(),
					_start_date.getDay(),_start_date.getHour(),	_start_date.getMinute())) / 60;
		}
		else if (dataIntervalBase == TimeInterval.HOUR) {
			// Not likely but could happen
			nhours = HourTS.calculateDataSize( _start_date, _end_date, 1);
		}
		else if (dataIntervalBase == TimeInterval.DAY) {
			// Not likely but could happen
			nhours = DayTS.calculateDataSize(_start_date, _end_date, 1) * 24;
		}
		else { 
			nhours = HourTS.calculateDataSize( _start_date, _end_date, 1);
		}

		if (nlabels != 0) {
			delta = nhours / nlabels;
		}

		if (delta == 0) {
			delta = 1;
		}

		date = new DateTime(_start_date);

		for (i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.addElement( new Double(date.toDouble()));
			date.addHour(delta);
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_MINUTE ) {
		// Could be irregular data...
		int nminutes = 0;

		TS ts = getFirstEnabledTS();
		if (ts == null) {
			ts = (TS)__tslist.elementAt(0);
		}
		int dataIntervalBase = ts.getDataIntervalBase();
		
		if (dataIntervalBase == TimeInterval.IRREGULAR) {
			nminutes = (int)(
				TimeUtil.absoluteMinute(_end_date.getYear(),_end_date.getMonth(),
					_end_date.getDay(),	_end_date.getHour(), _end_date.getMinute())
				- TimeUtil.absoluteMinute( _start_date.getYear(), _start_date.getMonth(),
					_start_date.getDay(), _start_date.getHour(), _start_date.getMinute()));
		}
		else if (dataIntervalBase == TimeInterval.HOUR) {
			// Not likely but could happen
			nminutes = HourTS.calculateDataSize(_start_date, _end_date, 1) * 60;
		}
		else if (dataIntervalBase == TimeInterval.DAY) {
			// Not likely but could happen
			nminutes = DayTS.calculateDataSize(	_start_date, _end_date, 1) * 1440;
		}
		else if (dataIntervalBase == TimeInterval.MONTH) {
			// Not likely but could happen
			nminutes = MonthTS.calculateDataSize( _start_date, _end_date, 1) * 1440 * 31;
		}
		else {	
			nminutes = MinuteTS.calculateDataSize( _start_date, _end_date, 1);
		}

		if (nlabels != 0) {
			delta = nminutes / nlabels;
		}

		if (delta == 0) {
			delta = 60;
		}

		// Round to even 60 minutes...
		delta = (delta / 60) * 60;

		// delta was being rounded to 0 in some cases above and that
		// was causing infinite loops below.
		if (delta == 0) {
			delta = 60;
		}
		
		date = new DateTime(_start_date);
		for (i = 0; date.lessThanOrEqualTo(_end_date); i++) {
			x_axis_labels_temp.addElement( new Double(date.toDouble()));
			date.addMinute(delta);
		}
	}

	// Now convert Vector to array of labels...

	size = x_axis_labels_temp.size();
	_xlabels = new double[size];
	for ( i = 0; i < size; i++ ) {
		_xlabels[i] = ((Double)x_axis_labels_temp.elementAt(i)).doubleValue();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,_gtype + "_xlabel[" + i + "]=" + _xlabels[i] );
		}
	}

	// Help garbage collection...
	date = null;
	x_axis_labels_temp = null;
}

/**
Determine and set the precision for the X axis.  The precision is set to the
most detailed time series data interval.  Call this in the constructor so the
precision can be used in setDrawingLimits().
This information is not used for scatter plots or other plots that don't use
date axes.
*/
private void computeXAxisDatePrecision ()
{	// Initialize to largest value...
	_xaxis_date_precision = TimeInterval.YEAR;
	if ( __tslist == null ) {
		return;
	}

	// Loop through and find the smallest time unit from the time series
	// intervals...

	int size = __tslist.size();
	TS ts = null;
	int interval = 0;
	DateTime date = null;
	for (int i = 0; i < size; i++) {
		ts = (TS)__tslist.elementAt(i);
		if ((ts == null) || !ts.getEnabled() || !isTSEnabled(i)) {
			continue;
		}

		try {	// Set the axis precision to the smallest time
			// interval of any data time series...
			interval = ts.getDataIntervalBase();
			if (	interval == TimeInterval.IRREGULAR ) {
				// Use the precision from the first date in
				// the data...
				date = ts.getDate1();
				if ( date == null ) {
					continue;
				}
				if (	date.getPrecision() ==
					DateTime.PRECISION_MINUTE ) {
					interval = TimeInterval.MINUTE;
				}
				else if ( date.getPrecision() ==
					DateTime.PRECISION_HOUR ) {
					interval = TimeInterval.HOUR;
				}
				else if ( date.getPrecision() ==
					DateTime.PRECISION_DAY ) {
					interval = TimeInterval.DAY;
				}
				else if ( date.getPrecision() ==
					DateTime.PRECISION_MONTH ) {
					interval = TimeInterval.MONTH;
				}
				else if ( date.getPrecision() ==
					DateTime.PRECISION_YEAR ) {
					interval = TimeInterval.YEAR;
				}
				else {	interval = TimeInterval.MINUTE;
				}
			}
			if ( interval < _xaxis_date_precision ) {
				_xaxis_date_precision = interval;
			}
		}
		catch ( Exception e ) {
			// Do nothing for now...
			;
		}
	}
	ts = null;
	date = null;
	// Now convert the precision to a real DateTime precision...
	if ( _xaxis_date_precision == TimeInterval.YEAR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is year." );
		}
		_xaxis_date_precision = DateTime.PRECISION_YEAR;
	}
	else if ( _xaxis_date_precision == TimeInterval.MONTH ) {
		_xaxis_date_precision = DateTime.PRECISION_MONTH;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is month." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.DAY ) {
		_xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is day." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.HOUR ) {
		_xaxis_date_precision = DateTime.PRECISION_HOUR;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is hour." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.MINUTE ) {
		_xaxis_date_precision = DateTime.PRECISION_MINUTE;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is minute." );
		}
	}
	else {	// Default to day...
		_xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision",
			_gtype + "X axis date precision is day." );
		}
	}
}

/**
Perform additional analysis on the data if other than a basic graph is
indicated.  The graph type needs to be specified before this method is called.
After the analysis, the data limits are recomputed (this is done for simple
data also).
*/
private void doAnalysis ()
{	String routine = "TSGraph.doAnalysis";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"Analyzing time series for " + _graph_type + " graph.");
	}

	if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
		// Do a linear regression analysis.  The first time series is
		// considered the independent (X) and the remaining time series
		// the Y.  If a regression fails, set it to null in the vector
		// so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = 0;
		if ( __tslist != null ) {
			nreg = __tslist.size() - 1;
		}
		if ( nreg > 0 ) {
			_regression_data = new Vector ( nreg );
		}
		PropList rprops = new PropList("regress");
		// Set the regression properties.  These may be changed by the
		// properties interface.
		String prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterAnalyzeForFilling", _subproduct, -1, false );
			rprops.set ( "AnalyzeForFilling=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterDependentAnalysisPeriodStart",
			_subproduct, -1, false );
		rprops.set ( "DependentAnalysisPeriodStart=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterDependentAnalysisPeriodEnd",
			_subproduct, -1, false );
		rprops.set ( "DependentAnalysisPeriodEnd=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterFillPeriodStart", _subproduct, -1, false );
		rprops.set ( "FillPeriodStart=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterFillPeriodEnd", _subproduct, -1, false );
		rprops.set ( "FillPeriodEnd=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterIndependentAnalysisPeriodStart",
			_subproduct, -1, false );
		rprops.set ( "IndependentAnalysisPeriodStart=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterIndependentAnalysisPeriodEnd",
			_subproduct, -1, false );
		rprops.set ( "IndependentAnalysisPeriodEnd=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterIntercept", _subproduct, -1, false );
			rprops.set ( "Intercept=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterMethod", _subproduct, -1, false );
		rprops.set ( "AnalysisMethod=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterMonth", _subproduct, -1, false );
		rprops.set ( "AnalysisMonth", prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterNumberOfEquations", _subproduct, -1, false );
		rprops.set ( "NumberOfEquations=" + prop_value );
		prop_value = _tsproduct.getLayeredPropValue (
			"XYScatterTransformation", _subproduct, -1, false );
		rprops.set ( "Transformation=" + prop_value );

		TSRegression regressionData = null;
		for ( int i = 1; i <= nreg; i++ ) {
			// The first time series [0] is always the dependent
			// time series and time series [1+] are the independent
			// for each relationship...
			ts0 = (TS)__tslist.elementAt(0);
			ts = (TS)__tslist.elementAt(i);
			try {	regressionData = new TSRegression (
					ts, ts0, rprops );
			}
			catch ( Exception e ) {
				Message.printWarning ( 2, routine,
				"Error performing regression for TS [" +
				(i - 1) + "]" );
				Message.printWarning ( 2, routine, e );
				regressionData = null;
			}
			// Always add something...
			_regression_data.addElement ( regressionData );
		}
		ts0 = null;
		ts = null;
	}
	else if ( _graph_type == TSProduct.GRAPH_TYPE_DOUBLE_MASS ) {
		// Do a double mass analysis so the information is available.
		// TODO SAM 2007-05-09 Need to enable?
		//TS ts0 = null;
		//TS ts1 = null;
		//if ( (__tslist != null) && (__tslist.size() == 2) ) {
		//	ts0 = (TS)__tslist.elementAt(0);
		//	ts1 = (TS)__tslist.elementAt(1);
		//}
/*
		if ( (ts0 != null) && (ts1 != null) ) {
			PropList rprops = new PropList("doublemass");
			try {	_double_mass_data = new TSDoubleMass (
						ts0, ts1, rprops );
			}
			catch ( Exception e ) {
				_title = "Double Mass - Unable to analyze";
			}
		}
*/
		//ts0 = null;
		//ts1 = null;
	}
	else if (	(_graph_type == TSProduct.GRAPH_TYPE_DURATION) &&
			(__tslist != null) && (__tslist.size() != 0) ) {
		// Generate TSDurationAnalysis for each time series...
		int size = __tslist.size();
		_duration_data = new Vector ( size );
		TSDurationAnalysis da = null;
		for ( int i = 0; i < size; i++ ) {
			try {	da = new TSDurationAnalysis (
					(TS)__tslist.elementAt(i) );
				_duration_data.addElement ( da );
			}
			catch ( Exception e ) {
				_duration_data.addElement ( null );
			}
		}
		da = null;
	}
	else if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE
	    || _graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
		// Do a linear regression analysis.  The first time series is
		// considered the independent (X) and the remaining time series
		// the Y.  If a regression fails, set it to null in the vector
		// so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = 0;
		if (__tslist != null) {
			nreg = __tslist.size() - 1;
		}
		if (nreg > 0 ) {
			_regression_data = new Vector(nreg);
		}
		PropList rprops = new PropList("regress");
		// Set the regression properties.  These may be changed by the
		// properties interface.
		rprops.set("AnalysisMethod", "MOVE2");
		rprops.set("Transformation", "None");
		rprops.set("NumberOfEquations", "OneEquation");
		rprops.set("MinimumDataCount", "1");
		rprops.set("MinimumR", "0");
		rprops.set("BestFit", "SEP");
		rprops.set("OutputFile", "c:\\temp\\output.regress");

		TSRegression regressionData = null;
		for (int i = 1; i <= nreg; i++) {
			// The first time series [0] is always the independent
			// time series and time series [1+] are the dependent
			// for each relationship...
			ts0 = (TS)__tslist.elementAt(0);
			ts = (TS)__tslist.elementAt(i);
			try {	
				regressionData = new TSRegression(
					ts0, ts, rprops);
				regressionData.createPredictedTS();
			}
			catch (Exception e) {
				Message.printWarning(2, routine,
					"Error performing regression for TS [" 
					+ (i - 1) + "]");
				Message.printWarning(2, routine, e);
				regressionData = null;
			}

			// Always add something...
			_regression_data.addElement(regressionData);
		}
		ts0 = null;
		ts = null;
	}
	// Compute the maximum data limits using the analysis objects that have
	// been created...
	computeDataLimits (true);
}

/**
Draws any annotations on the graph.  This method should be called twice, 
once with false before drawing data and then with true after data have been
drawn.
@param overGraph if true, then the annotations that should be drawn over the
data will be drawn.  If false, then the annotations that should be drawn under
the data will be drawn.  
*/
private void drawAnnotations(boolean overGraph) {
	if (_is_reference_graph) {
		return;
	}
	String routine = "TSGraph.drawAnnotations(" + overGraph + ")";
	int na = _tsproduct.getNumAnnotations(_subproduct);
	PropList annotation = null;
	String s = null;
	String type = null;
	String points = null;
	Vector pointsV = null;
	String point = null;
	Vector pointV = null;
	boolean valid = false;
	boolean niceSymbols = true;
	boolean isSymbol = false;

	String prop_value = _tsproduct.getLayeredPropValue("SymbolAntiAlias", 
		-1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	Shape clip = GRDrawingAreaUtil.getClip(_da_graph);	
	GRDrawingAreaUtil.setClip(_da_graph, _da_graph.getDataLimits());

	for (int iatt = 0; iatt < na; iatt++) {
		annotation = new PropList("Annotation " + iatt);
		valid = true;
		type = _tsproduct.getLayeredPropValue("ShapeType", _subproduct,
				iatt, false, true);
		if (type == null) {
			Message.printWarning(2, routine, "Null shapetype");
			valid = false;
		}
		else if (type.equalsIgnoreCase("Text")) {
			point = _tsproduct.getLayeredPropValue("Point", 
				_subproduct, iatt, false, true);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point");
			}
			else {
				pointV = StringUtil.breakStringList(point,
					",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, 
						"Invalid point declaration");
				}
			}
		}
		else if (type.equalsIgnoreCase("Line")) {
			points = _tsproduct.getLayeredPropValue("Points", 
				_subproduct, iatt, false, true);
			if (points == null) {
				valid = false;
				Message.printWarning(2, routine, "Null points");
			}
			else {
				pointsV = StringUtil.breakStringList(points,
					",", 0);
				if (pointsV == null || pointsV.size() != 4) {
					valid = false;
					Message.printWarning(2, routine, 
						"Invalid points declaration");
				}
			}			
		}
		else if (type.equalsIgnoreCase("Symbol")) {
			point = _tsproduct.getLayeredPropValue("Point", 
				_subproduct, iatt, false, true);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point");
			}
			else {
				pointV = StringUtil.breakStringList(point,
					",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, 
						"Invalid point declaration");
				}
			}				
			isSymbol = true;
		}
		else {
			valid = false;
		}

		if (!valid) {
			// some error encountered in checkProperties for this
			// so skip
			Message.printWarning(2, routine, "Invalid annotation: " 
				+ (_subproduct + 1) + "." + (iatt + 1));
			continue;
		}

		s = _tsproduct.getLayeredPropValue("Order", _subproduct, iatt,
			false, true);

		if (s == null) {
			// default to on top
			s = "OnTopOfData";
		}
		
		if (overGraph && s.equalsIgnoreCase("BehindData")) {
			continue;
		}
		if (!overGraph && s.equalsIgnoreCase("OnTopOfData")) {
			continue;
		}

		annotation.set("Color", 
			_tsproduct.getLayeredPropValue("Color",
			_subproduct, iatt, false, true));
		annotation.set("OutlineColor", 
			_tsproduct.getLayeredPropValue("OutlineColor",
			_subproduct, iatt, false, true));			
		annotation.set("XFormat", 
			_tsproduct.getLayeredPropValue("XFormat",
			_subproduct, iatt, false, true));
		annotation.set("YFormat", 
			_tsproduct.getLayeredPropValue("YFormat",
			_subproduct, iatt, false, true));
		annotation.set("Order", 
			_tsproduct.getLayeredPropValue("Order",
			_subproduct, iatt, false, true));
		annotation.set("ShapeType", 
			_tsproduct.getLayeredPropValue("ShapeType",
			_subproduct, iatt, false, true));
		annotation.set("XAxisSystem", 
			_tsproduct.getLayeredPropValue("XAxisSystem",
			_subproduct, iatt, false, true));
		annotation.set("YAxisSystem", 
			_tsproduct.getLayeredPropValue("YAxisSystem",
			_subproduct, iatt, false, true));
		annotation.set("LineStyle", 
			_tsproduct.getLayeredPropValue("LineStyle",
			_subproduct, iatt, false, true));
		annotation.set("LineWidth", 
			_tsproduct.getLayeredPropValue("LineWidth",
			_subproduct, iatt, false, true));
		annotation.set("Points", 
			_tsproduct.getLayeredPropValue("Points",
			_subproduct, iatt, false, true));
		annotation.set("FontSize", 
			_tsproduct.getLayeredPropValue("FontSize",
			_subproduct, iatt, false, true));
		annotation.set("FontStyle", 
			_tsproduct.getLayeredPropValue("FontStyle",
			_subproduct, iatt, false, true));
		annotation.set("FontName", 
			_tsproduct.getLayeredPropValue("FontName",
			_subproduct, iatt, false, true));
		annotation.set("Point", 
			_tsproduct.getLayeredPropValue("Point",
			_subproduct, iatt, false, true));
		annotation.set("Text", 
			_tsproduct.getLayeredPropValue("Text",
			_subproduct, iatt, false, true));
		annotation.set("TextPosition", 
			_tsproduct.getLayeredPropValue("TextPosition",
			_subproduct, iatt, false, true));
		annotation.set("SymbolSize",
			_tsproduct.getLayeredPropValue("SymbolSize",
			_subproduct, iatt, false, true));
		annotation.set("SymbolStyle",
			_tsproduct.getLayeredPropValue("SymbolStyle",
			_subproduct, iatt, false, true));
		annotation.set("SymbolPosition",
			_tsproduct.getLayeredPropValue("SymbolPosition",
			_subproduct, iatt, false, true));

		if (isSymbol && niceSymbols) {
			GRDrawingAreaUtil.setDeviceAntiAlias(_da_graph, true);
		}

		GRDrawingAreaUtil.drawAnnotation(_da_graph, annotation);
		
		if (isSymbol && niceSymbols) {
			GRDrawingAreaUtil.setDeviceAntiAlias(_da_graph, false);
		}		
	}

	// remove the clip around the graph.  This allows other things to be
	// drawn outside the graph bounds
	GRDrawingAreaUtil.setClip(_da_graph, (Shape)null);
	GRDrawingAreaUtil.setClip(_da_graph, clip);	
}

/**
Draw the axes features that should be behind the plotted data, including the
surrounding box, and the grid lines.
Call drawAxesFront() to draw features taht are to be on top of the graph
(tic marks and labels).
*/
private void drawAxesBack ()
{	// Previous code used the main _da_graph to draw the axis labels.  Now
	// that label areas are separate drawing areas, draw the labels in those
	// drawing areas.  To make sure the drawing limits are OK, set to the
	// _data_limits values here...

	_datalim_lefty_label = new GRLimits ( _da_lefty_label.getDataLimits() );
	_datalim_lefty_label.setBottomY ( _data_limits.getBottomY() );
	_datalim_lefty_label.setTopY ( _data_limits.getTopY() );
	_da_lefty_label.setDataLimits ( _datalim_lefty_label );

	_datalim_righty_label = new GRLimits(_da_righty_label.getDataLimits());
	_datalim_righty_label.setBottomY ( _data_limits.getBottomY() );
	_datalim_righty_label.setTopY ( _data_limits.getTopY() );
	_da_righty_label.setDataLimits ( _datalim_righty_label );

	_datalim_bottomx_label =new GRLimits(_da_bottomx_label.getDataLimits());
	_datalim_bottomx_label.setLeftX ( _data_limits.getLeftX() );
	_datalim_bottomx_label.setRightX ( _data_limits.getRightX() );
	_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );

	if ( !_is_reference_graph ) {
		drawXAxisGrid();
		drawYAxisGrid();
	}
	drawOutlineBox();
}

/**
Draw axes features that show in front of the plotted data.  Currently this
includes the axes tic marks, titles, and labels.  The tic marks are currently
always drawn in black.
*/
private void drawAxesFront ()
{	if ( _is_reference_graph ) {
		return;
	}

	// Used throughout...

	String prop_value = null;
	String title;
	String fontname;
	String fontsize;
	String fontstyle;

	boolean log_y = false;
	prop_value = _tsproduct.getLayeredPropValue (
			"LeftYAxisType", _subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y = true;
	}

	prop_value = _tsproduct.getLayeredPropValue (
		"XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
	}	

	// Left Y Axis labels, and tics...

	double [] xlabels = new double[2];
	xlabels[0] = _data_limits.getLeftX();
	xlabels[1] = _data_limits.getRightX();

	xlabels = null;
	fontname = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_lefty_label, fontname, fontstyle,
			StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.setColor ( _da_lefty_label, GRColor.black );
	if ( log_y ) {
		// Only draw major labels...
		double [] ylabels_log = new double[
				(_ylabels.length)/9 + 1];
		int j = 0;
		for ( int k = 0; k < _ylabels.length; k++ ) {
			if ( ((k%9) == 0) || (k == 0)) {
				ylabels_log[j++] =
					_ylabels[k];
			}
		}
		GRAxis.drawLabels ( _da_lefty_label, ylabels_log.length,
		ylabels_log, _datalim_lefty_label.getRightX(), GRAxis.Y,
		"%.1f", GRText.RIGHT|GRText.CENTER_Y );
		ylabels_log = null;
	}
	else {	if (__drawLabels) {
			if ( _graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
				// Only want to label with whole numbers that
				// are > 0 and <= __tslist.size()...
				GRAxis.drawLabels ( _da_lefty_label,
				_ylabels.length,
				_ylabels, _datalim_lefty_label.getRightX(),
				GRAxis.Y, "%." + _lefty_precision + "f",
				GRText.RIGHT|GRText.CENTER_Y);
			}
			else {	GRAxis.drawLabels ( _da_lefty_label,
				_ylabels.length,
				_ylabels, _datalim_lefty_label.getRightX(),
				GRAxis.Y, "%." + _lefty_precision + "f",
				GRText.RIGHT|GRText.CENTER_Y);
			}
		}
	}

	// Left Y-Axis title...
	title = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleString", _subproduct, -1, false );
	fontname = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_lefty_title, fontname, fontstyle,
			StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( _da_lefty_title, title,
		_datalim_lefty_title.getCenterX(),
		_datalim_lefty_title.getCenterY(),
		0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Bottom X-Axis title...

	title = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleString", _subproduct, -1, false );
	fontname = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_title, fontname, fontstyle,
			StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( _da_bottomx_title, title,
		_datalim_bottomx_title.getCenterX(),
		_datalim_bottomx_title.getCenterY(), 0.0,
		GRText.CENTER_X|GRText.CENTER_Y );

	// Bottom X Axis labels, title, and tics

	fontname = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, fontname, fontstyle,
			StringUtil.atod(fontsize) );

	// Label axis after drawing so tics are on top of data...

	if (	(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ||
		(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ) {
		// Label the X axis with formatted numbers...
		GRAxis.drawLabels ( _da_bottomx_label, _xlabels.length,
		_xlabels, _datalim_bottomx_label.getTopY(), GRAxis.X,
		"%.1f", GRText.TOP|GRText.CENTER_X );
		double[] xt = new double[2];
		double[] yt = new double[2];
		double[] yt2 = new double[2];
		double tic_height = 0.0;	// Height of major tic marks
		yt[0] = _ylabels[0];
		yt2[0] = _ylabels[0];
		// Figure out the y-positions and tic height (same regardless of
		// intervals being used for labels)...
		if ( log_y ) {
			// Need to make sure the line is nice length!
			tic_height = yt[0]*.05;
			yt[1] = yt[0] + tic_height;
			yt2[1] = yt2[0] + tic_height/2.0;
		}
		else {	tic_height = _data_limits.getHeight()*.02;
			yt[1] = yt[0] + tic_height;
			yt2[1] = yt2[0] + tic_height/2.0;
		}
		for ( int i = 0; i < _xlabels.length; i++ ) {
			xt[0] = xt[1] = _xlabels[i];
			GRDrawingAreaUtil.drawLine ( _da_graph, xt, yt );
		}
	}
	else {	// Draw the X-axis date/time labels...
		drawXAxisDateLabels ( false );
	}
}

/**
Draw the "current" time line, if properties are present to do so.  This checks
to see if the CurrentDateTime property is set (it will be set in the override
properties in the TSProduct).  If set and within the limits of the current
graph, the current line will be drawn.
*/
private void drawCurrentDateTime ()
{	if (	(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ||
		(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ) {
		return;
	}
	// Allow layered properties because the current time could be specified once for all graphs...
	String prop_value = _tsproduct.getLayeredPropValue(	"CurrentDateTime", _subproduct, -1, true );
	if ( (prop_value == null) || (prop_value.trim().length() == 0) || prop_value.equalsIgnoreCase("None")) {
		return;
	}
	try {	DateTime current_time = null;
		if ( prop_value.equalsIgnoreCase("Auto") ) {
			// Use the current time from the system.
			current_time = new DateTime ( DateTime.DATE_CURRENT );
		}
		else {
		    // Parse the date/time from the property...
			current_time = DateTime.parse ( prop_value );
		}
		prop_value = _tsproduct.getLayeredPropValue( "CurrentDateTimeColor", _subproduct, -1, true );
		// FIXME SAM 2008-01-10 Remove when done.
		//Message.printStatus(2, "", "Color for CurrentDateTimeColor is " + prop_value );
		try {
		    GRDrawingAreaUtil.setColor(_da_graph, GRColor.parseColor( prop_value) );
		}
		catch ( Exception e2 ) {
			GRDrawingAreaUtil.setColor ( _da_graph, GRColor.green );
		}
		double xp[] = new double[2];
		double yp[] = new double[2];
		xp[0] = current_time.toDouble();
		xp[1] = xp[0];
		// Get the drawing area limits.  This allows the check for
		// reference and main graph windows...
		GRLimits data_limits = _da_graph.getDataLimits();
		yp[0] = data_limits.getMinY();
		yp[1] = data_limits.getMaxY();
		if ( (xp[0] >= data_limits.getMinX()) && (xp[0] <= data_limits.getMaxX()) ) { 
			GRDrawingAreaUtil.drawLine ( _da_graph, xp, yp );
		}
		current_time = null;
		xp = null;
		yp = null;
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "TSGraph.drawCurrentDateTime", "Unable to draw current date/time." );
		Message.printWarning ( 3, "TSGraph.drawCurrentDateTime", e );
	}
}

/**
Draw the drawing areas, for troubleshooting.  Drawing areas boundaries are drawn
in magenta.
*/
public void drawDrawingAreas ()
{	//boolean do_names = false;
	boolean do_names = true;
	_da_page.setColor ( GRColor.magenta );	// actually sets for all
	GRDrawingAreaUtil.setFont ( _da_page, "Helvetica", "Plain", 8 );
	// Reference and main...
	GRDrawingAreaUtil.drawRectangle (	_da_graph,
				_data_limits.getLeftX(),
				_data_limits.getBottomY(),
				_data_limits.getWidth(),
				_data_limits.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_graph, _da_graph.getName(),
				_data_limits.getCenterX(),
				_data_limits.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
	if ( _is_reference_graph ) {
		// Don't need to draw anything else...
		return;
	}

	// Everything else draw top to bottom and left to right...

	GRDrawingAreaUtil.drawRectangle (	_da_page,
				_datalim_page.getLeftX(),
				_datalim_page.getBottomY(),
				_datalim_page.getWidth(),
				_datalim_page.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_page, _da_page.getName(),
				_datalim_page.getCenterX(),
				_datalim_page.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_maintitle,
				_datalim_maintitle.getLeftX(),
				_datalim_maintitle.getBottomY(),
				_datalim_maintitle.getWidth(),
				_datalim_maintitle.getHeight());
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_maintitle,
				_da_maintitle.getName(),
				_datalim_maintitle.getCenterX(),
				_datalim_maintitle.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_subtitle,
				_datalim_subtitle.getLeftX(),
				_datalim_subtitle.getBottomY(),
				_datalim_subtitle.getWidth(),
				_datalim_subtitle.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_subtitle,
				_da_subtitle.getName(),
				_datalim_subtitle.getCenterX(),
				_datalim_subtitle.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

/*  not enabled until we can get Yaxis labels out of the way
	GRDrawingAreaUtil.drawRectangle (	_da_topx_title,
				_datalim_topx_title.getLeftX(),
				_datalim_topx_title.getBottomY(),
				_datalim_topx_title.getWidth(),
				_datalim_topx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_topx_title,
				_da_topx_title.getName(),
				_datalim_topx_title.getCenterX(),
				_datalim_topx_title.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_topx_label,
				_datalim_topx_label.getLeftX(),
				_datalim_topx_label.getBottomY(),
				_datalim_topx_label.getWidth(),
				_datalim_topx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_topx_label,
				_da_topx_label.getName(),
				_datalim_topx_label.getCenterX(),
				_datalim_topx_label.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
*/

	GRDrawingAreaUtil.drawRectangle (	_da_lefty_title,
				_datalim_lefty_title.getLeftX(),
				_datalim_lefty_title.getBottomY(),
				_datalim_lefty_title.getWidth(),
				_datalim_lefty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_lefty_title,
				_da_lefty_title.getName(),
				_datalim_lefty_title.getCenterX(),
				_datalim_lefty_title.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_lefty_label,
				_datalim_lefty_label.getLeftX(),
				_datalim_lefty_label.getBottomY(),
				_datalim_lefty_label.getWidth(),
				_datalim_lefty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_lefty_label,
				_da_lefty_label.getName(),
				_datalim_lefty_label.getCenterX(),
				_datalim_lefty_label.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_righty_title,
				_datalim_righty_title.getLeftX(),
				_datalim_righty_title.getBottomY(),
				_datalim_righty_title.getWidth(),
				_datalim_righty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_righty_title,
				_da_righty_title.getName(),
				_datalim_righty_title.getCenterX(),
				_datalim_righty_title.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_righty_label,
				_datalim_righty_label.getLeftX(),
				_datalim_righty_label.getBottomY(),
				_datalim_righty_label.getWidth(),
				_datalim_righty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_righty_label,
				_da_righty_label.getName(),
				_datalim_righty_label.getCenterX(),
				_datalim_righty_label.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_bottomx_label,
				_datalim_bottomx_label.getLeftX(),
				_datalim_bottomx_label.getBottomY(),
				_datalim_bottomx_label.getWidth(),
				_datalim_bottomx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_bottomx_label,
				_da_bottomx_label.getName(),
				_datalim_bottomx_label.getCenterX(),
				_datalim_bottomx_label.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_bottomx_title,
				_datalim_bottomx_title.getLeftX(),
				_datalim_bottomx_title.getBottomY(),
				_datalim_bottomx_title.getWidth(),
				_datalim_bottomx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_bottomx_title,
				_da_bottomx_title.getName(),
				_datalim_bottomx_title.getCenterX(),
				_datalim_bottomx_title.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_bottom_legend,
				_datalim_bottom_legend.getLeftX(),
				_datalim_bottom_legend.getBottomY(),
				_datalim_bottom_legend.getWidth(),
				_datalim_bottom_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_bottom_legend,
				_da_bottom_legend.getName(),
				_datalim_bottom_legend.getCenterX(),
				_datalim_bottom_legend.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_left_legend,
				_datalim_left_legend.getLeftX(),
				_datalim_left_legend.getBottomY(),
				_datalim_left_legend.getWidth(),
				_datalim_left_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_left_legend,
				_da_left_legend.getName(),
				_datalim_left_legend.getCenterX(),
				_datalim_left_legend.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle (	_da_right_legend,
				_datalim_right_legend.getLeftX(),
				_datalim_right_legend.getBottomY(),
				_datalim_right_legend.getWidth(),
				_datalim_right_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (	_da_right_legend,
				_da_right_legend.getName(),
				_datalim_right_legend.getCenterX(),
				_datalim_right_legend.getCenterY(),
				0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
} 

/**
Draw a duration plot.
*/
private void drawDurationPlot ()
{	String routine = "TSGraph.drawDurationPlot";
	if ( __tslist == null ) {
		return;
	}	
	int size = __tslist.size();
	TS ts = null;
	TSDurationAnalysis da = null;
	double [] values, percents;
	int symbol = 0;
	double symbol_size = 0.0;
	String prop_value;
	boolean niceSymbols = true;
	prop_value = _tsproduct.getLayeredPropValue(
		"SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	for ( int i = 0; i < size; i++ ) {
		ts = (TS)__tslist.elementAt(i);
		if ((ts == null) || !ts.getEnabled() || !isTSEnabled(i)) {
			Message.printWarning ( 2, routine,
			"Null time series to graph [" + i + "]" );
			return;
		}
		
		da = (TSDurationAnalysis)_duration_data.elementAt(i);

		if (da == null) {
			Message.printWarning(2, routine,
				"Null TSDurationAnalysis to graph [" 
				+ i + "]");
			return;
		}
		values = da.getValues();
		percents = da.getPercents();
		if ( (values == null) || (percents == null) ) {
			Message.printWarning ( 2, routine,
			"Null TSDurationAnalysis data graph [" + i + "]" );
			return;
		}
		try {	_da_graph.setColor ( GRColor.parseColor(
				_tsproduct.getLayeredPropValue (
				"Color", _subproduct, i, false ) ) );
		}
		catch ( Exception e ) {
			_da_graph.setColor ( GRColor.black );
		}
		GRDrawingAreaUtil.drawPolyline ( _da_graph, values.length,
			percents, values );
		prop_value = _tsproduct.getLayeredPropValue (
			"SymbolStyle", _subproduct, i, false );
		try {	symbol = GRSymbol.toInteger(prop_value);
		}
		catch ( Exception e ) {
			symbol = GRSymbol.SYM_NONE;
		}
		symbol_size = StringUtil.atod(
				_tsproduct.getLayeredPropValue (
				"SymbolSize", _subproduct, i, false ) );
		if (	!_is_reference_graph &&
			(symbol != GRSymbol.SYM_NONE) &&
			(symbol_size > 0) ) {
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(
					_da_graph, true);
			}
			GRDrawingAreaUtil.drawSymbols (_da_graph, symbol,
					values.length, percents, values,
					symbol_size,
					GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X|
					GRSymbol.SYM_CENTER_Y );
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(
					_da_graph, false);
			}
		}
	}
	// Clean up...
	values = null;
	percents = null;
	ts = null;
	da = null;
	routine = null;
}

/**
Draw the time series graph.  This is the highest-level draw method and calls the
other time series drawing methods.
*/
private void drawGraph () {
	String routine = "TSGraph.drawGraph";
	int size = 0;
	if (__tslist != null) {
		size = __tslist.size();
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype +
		"Drawing graph:  " + size + " time series" );
	}
	if ( size == 0 ) {
		return;
	}

	if ( _data_limits == null ) {
		return;
	}

	// Print the limits for debugging...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Drawing limits: " +
		_da_graph.getDrawingLimits() );
		Message.printDebug ( 1, routine, _gtype + "Data limits: " +
		_da_graph.getDataLimits() );
		Message.printDebug ( 1, routine, _gtype + "_data_limits: " +
		_data_limits );
		Message.printDebug ( 1, routine, _gtype + "Plotting limits: " +
		_da_graph.getPlotLimits(GRDrawingArea.COORD_PLOT) );
	}


	/* If need to use for development...
	Message.printStatus ( 1, routine, _gtype + "Drawing: [" +
	_subproduct + "]: " + _da_graph.getName() );
	Message.printStatus ( 1, routine, _gtype + "Drawing limits: " +
	_da_graph.getDrawingLimits() );
	Message.printStatus ( 1, routine, _gtype + "Data limits: " +
	_da_graph.getDataLimits() );
	Message.printStatus ( 1, routine, _gtype + "_data_limits: " +
	_data_limits );
	Message.printStatus ( 1, routine, _gtype + "Plotting limits: " +
	_da_graph.getPlotLimits(GRDrawingArea.COORD_PLOT) );
	*/
	

	// Graph the time series.  If a reference map, only draw one time
	// series, as specified in the properties...

	TS ts = null;
	if ( _graph_type == TSProduct.GRAPH_TYPE_DURATION ) {
		drawDurationPlot ();
	}
	else if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
		drawXYScatterPlot ();
	}
	else if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE
	    || _graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
	    	boolean residual = true;
		if (_graph_type==TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
			residual = false;
		}
		TSRegression regressionData = null;
		int nregression = 0;
		if (_regression_data != null) {
			nregression = _regression_data.size();
		}
		TS predicted = null;

		for (int ir = 0; ir < nregression; ir++) {
			regressionData = (TSRegression)
				_regression_data.elementAt(ir);
			if (regressionData != null) {
			    	if (residual) {
					if (isTSEnabled(ir + 1)) {
						drawTS(ir + 1, 
					       		regressionData
							.getResidualTS());
					}
				}
				else {
					PropList props = new PropList("");
						
					if (isTSEnabled(0)) {
						drawTS(0,
					    	    	regressionData
						    	.getIndependentTS(),
					    		props);
					}
	
					if (isTSEnabled(ir + 1)) {
						drawTS(ir + 1,
					      		regressionData
							.getDependentTS(),
					      		props);
					
						props.set("LineStyle=Dashed");
						props.set("SymbolStyle=None");
						predicted = regressionData
							.getPredictedTS();
						drawTS(ir + 1, predicted, 
							props);
					}
				}
			}
			else {
				// ignore -- null regression data
			}
		}
	}
	else {	
		// "Normal" graph that can be handled in general code...
		for (int i = 0; i < size; i++) {
			ts = (TS)__tslist.elementAt(i);
			if ((ts == null) 
			    || (!_is_reference_graph && !ts.getEnabled())
			    || (!_is_reference_graph && !isTSEnabled(i))) {
				continue;
			}

			if (_is_reference_graph && (i != _reference_ts_index)) {
				// A reference graph but we have not found the
				// reference time series yet.  We want the
				// reference time series drawn in the same color
				// as it occurs in the main graph.
				if (Message.isDebugOn) {
					Message.printDebug(1, routine,
						_gtype + "Skipping time series "
						+ i);
				}
				continue;
			}

			// Draw each time series...
			drawTS(i, ts);
		}
	}

	// Clean up...
	routine = null;
	ts = null;
}

/**
Draw the legend.  The drawing is the same regardless of the legend position (the
legend items are draft from top to bottom).  This should work well for left,
right, and bottom legends.  Additional logic may need to be implemented when the
top legend is supported.
*/
private void drawLegend () {
	if (_is_reference_graph) {
		return;
	}

	if ((__tslist == null) || (__tslist.size() == 0)
	    || getEnabledTSList().size() == 0) {
		return;
	}

	// Figure out which legend drawing area we are using...

	String prop_val = _tsproduct.getLayeredPropValue(
				"LegendPosition", _subproduct, -1, false);
	GRDrawingArea da_legend = _da_bottom_legend;		// Default.
	GRLimits datalim_legend = _datalim_bottom_legend;	// Default.
	if ( prop_val.equalsIgnoreCase("Left") ) {
		da_legend = _da_left_legend;
		datalim_legend = _datalim_left_legend;
	}
	else if ( prop_val.equalsIgnoreCase("Right") ) {
		da_legend = _da_right_legend;
		datalim_legend = _datalim_right_legend;
	}
	else if (StringUtil.startsWithIgnoreCase(prop_val, "Inside")) {
		da_legend = _da_inside_legend;
		datalim_legend = _datalim_inside_legend;

		GRDrawingAreaUtil.setColor(da_legend, GRColor.white);
		GRDrawingAreaUtil.fillRectangle(da_legend, -4, -4,
			datalim_legend.getWidth() + 8, 
			datalim_legend.getHeight() + 8);
		GRDrawingAreaUtil.setColor(da_legend, GRColor.black);
		GRDrawingAreaUtil.drawRectangle(da_legend, -4, -4,
			datalim_legend.getWidth() + 8, 
			datalim_legend.getHeight() + 8);
	}

	// Get the properties for the legend...
	String legend_font = _tsproduct.getLayeredPropValue (
			"LegendFontName", _subproduct, -1, false );
	String legend_fontsize = _tsproduct.getLayeredPropValue (
			"LegendFontSize", _subproduct, -1, false );
	String legend_fontstyle = _tsproduct.getLayeredPropValue (
			"LegendFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( da_legend, legend_font, legend_fontstyle,
			StringUtil.atod(legend_fontsize) );
	GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
			da_legend, "TEST STRING", GRUnits.DEVICE );
	double ydelta = text_limits.getHeight();
	text_limits = null;

	// Draw legend from top down in case we run out of room.  Can center
	// vertically on the following (line will be one font height down and
	// font will be 1/2 height down to top).
	double ylegend = datalim_legend.getTopY() - ydelta;
	// Put first time series in list at the top...
	double symbol_size = 0;
	int symbol = 0;
	String prop_value = null;
	String legend = null;
	String line_style = null;
	double x[] = new double[2];
	double y[] = new double[2];
	int size = 0;
	boolean niceSymbols = true;
	prop_value = _tsproduct.getLayeredPropValue(
		"SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	if (__tslist != null) {
		size = __tslist.size();
	}
	
	TS ts;

	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
		size = 1 + ((size - 1) * 2);
	}
	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
		size = size - 1;
	}

	double[] lineDash = new double[2];
	lineDash[0] = 3;
	lineDash[1] = 5;

	boolean predicted = false;
	int tsNum = 0;
	TSRegression regressionData = null;

	for (int i = 0; i < size; i++) {
		predicted = false;

		// Make sure that the legend is not drawing using 
		// negative data units.  If it is, then it will likely 
		// go into another graph (since there can be more than 
		// one graph in a window/page
		if (ylegend < 0.0) {
			 continue;
		}
		
		if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
			// determine the correspondence of the TS to be 
			// drawn versus the actual time series that there is
			// access to.  
			// ts 0 corresponds to 0
			// ts 1 corresponds to 1 and 2 (for the dependent
			//   and predicted TS)
			// ts 2 corresponds to 3 and 4 ...
			// etc.

			if (i == 0) {
				tsNum = 0;
			}
			else {
				if (i % 2 == 0) {
					// the ts is half of the even 
					// numbers
					tsNum = i / 2;
					predicted = true;
				}
				else {
					// for odd numbers, the ts is half
					// (the value plus one)
					tsNum = (i + 1) / 2;
				}
			}
			
			if (!isTSEnabled(tsNum)) {
				continue;
			}

			if (predicted) {
				// predicted ones have to be retrieved 
				// from the regression data.
				regressionData = (TSRegression)
					_regression_data.elementAt(tsNum - 1);
				ts = regressionData.getPredictedTS();

			}
			else {
				ts = (TS)__tslist.elementAt(tsNum);
			}

			legend = getLegendString(ts, tsNum);
			if (legend == null) {
				continue;
			}
			
			if (predicted) {
				legend = legend + " (Predicted)";
			}

			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue(
				"Color", _subproduct, tsNum, false);
			try {	
				da_legend.setColor(GRColor.parseColor(
					prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
			
			line_style = _tsproduct.getLayeredPropValue(
				"LineStyle", _subproduct, tsNum, false);
	
			if (line_style == null) {
				line_style = "None";
			}

			if (predicted) {
				line_style = "Dashed";
			}
		}
		else if (_graph_type 
		    == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
			if (!isTSEnabled(i + 1)) {
				continue;
			}

			regressionData = (TSRegression)
				_regression_data.elementAt(i);
			ts = regressionData.getResidualTS();

			legend = getLegendString(ts, i + 1) 
				+ " (Residual)";
			if (legend == null) {
				continue;
			}
			
			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue(
				"Color", _subproduct, i + 1, false);

			try {	
				da_legend.setColor(GRColor.parseColor(
					prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
		}
		else {
			if (!isTSEnabled(i)) {
				continue;
			}
			ts = (TS)__tslist.elementAt(i);
			legend = getLegendString(ts, i);
			if (legend == null) {
				continue;
			}
			
			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue(
				"Color", _subproduct, i, false);
			try {	
				da_legend.setColor(GRColor.parseColor(
					prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
			
			line_style = _tsproduct.getLayeredPropValue(
				"LineStyle", _subproduct, i, false);
	
			if (line_style == null) {
				line_style = "None";
			}
		}
		
		x[0] = datalim_legend.getLeftX();
		// Legend drawing limits are in device units so just use
		// pixels...
		x[1] = x[0] + 25;
		y[0] = ylegend + ydelta/2.0;
		y[1] = y[0];
		if (	(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) &&
			(i == 0) ) {
			;// Do nothing.  Don't want the symbol (but do want the
			// string label below
		}
		else if (_graph_type == TSProduct.GRAPH_TYPE_BAR
		    || _graph_type 
		    == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
			GRDrawingAreaUtil.fillRectangle (
				da_legend, x[0], ylegend,(x[1] - x[0]), ydelta);
		}
		else {	
			prop_value = getLayeredPropValue("LineWidth", 
				_subproduct, i, false, null);
			if (prop_value != null) {
				if (StringUtil.isInteger(prop_value)) {
					GRDrawingAreaUtil.setLineWidth(
						da_legend,
						StringUtil.atoi(prop_value));
				}
			}

			if (_graph_type != TSProduct.GRAPH_TYPE_POINT 
			   && line_style.equalsIgnoreCase("Solid") ) {
				GRDrawingAreaUtil.drawLine(da_legend, x, y);
			}
			else if (_graph_type != TSProduct.GRAPH_TYPE_POINT 
			   && line_style.equalsIgnoreCase("Dashed")) {
				GRDrawingAreaUtil.setLineDash(
					da_legend, lineDash, 0);
				GRDrawingAreaUtil.drawLine(da_legend, x, y);
				GRDrawingAreaUtil.setLineDash(
					da_legend, null, 0);
			}

			GRDrawingAreaUtil.setLineWidth(da_legend, 1);

			// Draw the symbol if any is specified...
			prop_value = _tsproduct.getLayeredPropValue (
				"SymbolStyle", _subproduct, i, false );
			try {	
				symbol = GRSymbol.toInteger(prop_value);
			}
			catch (Exception e) {
				symbol = GRSymbol.SYM_NONE;
			}
			symbol_size = StringUtil.atod(
					_tsproduct.getLayeredPropValue (
					"SymbolSize", _subproduct, i, false ) );
			if ((symbol != GRSymbol.SYM_NONE) && (symbol_size > 0)){
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias(
						da_legend, true);
				}
				GRDrawingAreaUtil.drawSymbol (da_legend, symbol,
					(x[0] + x[1])/2.0, y[0], symbol_size,
					GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X|
					GRSymbol.SYM_CENTER_Y );
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias(
						da_legend, false);
				}
			}
		}
		da_legend.setColor ( GRColor.black );
		GRDrawingAreaUtil.drawText ( da_legend, legend,
				x[1], ylegend, 0.0, GRText.LEFT|GRText.BOTTOM );
		ylegend -= ydelta;
	}
	x = null;
	y = null;
}

/**
Draw a box around the graph.  This is normally done after drawing grid lines
so the box looks solid, and before drawing data.
*/
private void drawOutlineBox ()
{	GRDrawingAreaUtil.setColor ( _da_graph, GRColor.black );
	if ( _is_reference_graph ) {
		// Just draw a box around the graph area to make it more
		// visible...
		// Using GR seems to not always get the line (roundoff)?
		Rectangle bounds = _dev.getBounds();

		_graphics.drawRect ( 0, 0, (bounds.width - 1),
			(bounds.height - 1) );
		return;
	}
	else {	// Normal drawing area...
		GRDrawingAreaUtil.drawRectangle ( _da_graph,
			_data_limits.getMinX(), _data_limits.getMinY(),
			_data_limits.getWidth(), _data_limits.getHeight() );
	}
}

/**
Draw the main and sub titles for the graph.  The properties are retrieved again
in case they have been reset by a properties GUI.
*/
private void drawTitles ()
{	if ( _is_reference_graph ) {
		return;
	}

	// Main title.

	_da_maintitle.setColor ( GRColor.black );
	String maintitle_font = _tsproduct.getLayeredPropValue (
				"MainTitleFontName", _subproduct, -1, false );
	String maintitle_fontstyle = _tsproduct.getLayeredPropValue (
				"MainTitleFontStyle", _subproduct, -1, false );
	String maintitle_fontsize = _tsproduct.getLayeredPropValue (
				"MainTitleFontSize", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font,
			maintitle_fontstyle,
			StringUtil.atod(maintitle_fontsize) );
	String maintitle_string = _tsproduct.getLayeredPropValue (
				"MainTitleString", _subproduct, -1, false );
	GRDrawingAreaUtil.drawText (	_da_maintitle, maintitle_string,
			_datalim_maintitle.getCenterX(),
			_datalim_maintitle.getCenterY(), 0.0,
			GRText.CENTER_X|GRText.CENTER_Y );

	// Sub title....

	_da_subtitle.setColor ( GRColor.black );
	String subtitle_font = _tsproduct.getLayeredPropValue (
				"SubTitleFontName", _subproduct, -1, false );
	String subtitle_fontstyle = _tsproduct.getLayeredPropValue (
				"SubTitleFontStyle", _subproduct, -1, false );
	String subtitle_fontsize = _tsproduct.getLayeredPropValue (
				"SubTitleFontSize", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font,
		subtitle_fontstyle, StringUtil.atod(subtitle_fontsize) );
	String subtitle_string = _tsproduct.getLayeredPropValue (
				"SubTitleString", _subproduct, -1, false );
	GRDrawingAreaUtil.drawText (	_da_subtitle, subtitle_string,
			_datalim_subtitle.getCenterX(),
			_datalim_subtitle.getCenterY(), 0.0,
			GRText.CENTER_X|GRText.CENTER_Y );
}

private void drawTS(int its, TS ts) {
	drawTS(its, ts, null);
}

/**
Draw a single time series.  This method is called for graph types
TSProduct.GRAPH_TYPE_BAR, TSProduct.GRAPH_TYPE_LINE,
TSProduct.GRAPH_TYPE_PERIOD.  Other graph types should
use the individual drawing methods for those graph types.
@param its Counter for time series (starting at 0).
@param ts Single time series to draw.
*/
private void drawTS(int its, TS ts, PropList overrideProps) {
	String routine = "TSGraph.drawTS";

	if ((ts == null) || (!_is_reference_graph && !ts.getEnabled())) {
		return;
	}

	if ((ts.getDataIntervalBase() == TimeInterval.IRREGULAR)  
	    && (_graph_type == TSProduct.GRAPH_TYPE_PERIOD)) {
		// Can't draw irregular time series in period of record graph.
		return;
	}

	GRColor tscolor = null;
	try {	
		tscolor = GRColor.parseColor(
			getLayeredPropValue("Color", _subproduct, its, false,
				overrideProps));
	}
	catch (Exception e) {
		tscolor = GRColor.black;
	}
	_da_graph.setColor(tscolor);

/*
	// The following was envisioned to optimize the processing.  For now,
	// handle below to skip data that do not need to be drawn...
	try {	valid_dates =
		TSUtil.getValidPeriod ( ts, _start_date, _end_date );
	}
	catch ( Exception e ) {
		return;
	}
	DateTime start	= valid_dates.getDate1();
	DateTime end	= valid_dates.getDate2();
*/

	// REVISIT SAM 2006-10-01 The following is what we want to do.  For now,
	// test the method at the bottom of this class, but need to move it
	// elsewhere
	//start = getNearestDateTimeLessThanOrEqualTo ( _start_date, ts );
	// When zoomed in really far, sometimes lines don't draw completely
	// across the edges.  Maybe should decrement the returned DateTime by
	// one time series data interval to make sure it starts outside the
	// page (and will get cropped correctly upon drawing) - need to evaluate
	// this more.

	// To draw the single time series, use the start and end dates for the
	// graph, using the correct precision for the time series.  The start
	// and end dates for the graph are determined using all time series and
	// may have an offset that is not suitable for the data.
	// For example, 24-hour data may be stored at hour 12 rather than hour
	// zero.  Therefore, reset the start date/time for this time series to
	// match the specific time series.
	
	// Get the start date/time for the graph (for all data) and round it
	// down to nearest even interval based on the current time series...
	// only do this if the ts has an hour interval type
	DateTime start = new DateTime(_start_date);   
	if(ts.getDataIntervalBase() == TimeInterval.HOUR)
	{
		DateTime start_withOffset = new DateTime();
		start_withOffset = TSGraph.getNearestDateTimeLessThanOrEqualTo(start, ts);
		start = start_withOffset;
	}

	// REVISIT SAM 2006-09-28
/* SAM - not sure if Kurt's code is bulletproof for a list of monthy, daily,
irregular time series mixed - have him look at some more combinations and
possibly consider something like the following...
	// Need to evaluate the following code to see if it works in all
	// situations.
	// Make sure that the time series actually has a date-time that aligns
	// with the graph start.  This may involve, for example
	//start = new DateTime ( findNearestDateTime ( start, ts, -1 ) );

	// Here is a start on some ideas that work more with integer math...
	// Get the biggest offset that could occur (the interval of the data)...
	int offset_interval_mult = ts.getDataIntervalMult();
	// Now check whether the time series is recorded at an offset...
	if ( (ts.getDate1().getIntervalValue(ts.getIntervalBase())%
		ts.getIntervalMult() != 0 ) {
		// Time series is offset...
		so adjust the start by the offset
	}
*/
	
	// End REVISIT SAM 2006-09-28

	// Make sure that the iterator for this time series is using a precision
	// that matches the time series data...
	start.setPrecision(ts.getDataIntervalBase());
	DateTime end = new DateTime(_end_date);	 
	end.setPrecision(ts.getDataIntervalBase());

	/* Can uncomment for debug purposes on start and end dates 
	Message.printStatus(1, "",
	"----------------------------------------------------------");
	String tsStatus = "TS:" + (ts.getIdentifier()).getIdentifier() +
	"    GRAPH_TYPE:" + _gtype;
	String tsInfo   = "DIFF:" + diff.toString() + "  Interval:" +
	ts.getDataIntervalBase() + "   Mult:" + ts.getDataIntervalMult();
	Message.printStatus(1, "", tsInfo);
	Message.printStatus(1, "", tsStatus);
	String timeStatus = "START   " + _start_date.toString() + "   " +
	start.toString() + "    END:" + end.toString();
	Message.printStatus(1, routine, timeStatus);
	Message.printStatus(1, "",
	"----------------------------------------------------------");
	*/

	// If the time series data start date is greater than the global start
	// date set to local (increases performance).  Similar for the end date.

	if (ts.getDate1().greaterThan(start)) {
		start = new DateTime(ts.getDate1());
	}
	if (ts.getDate2().lessThan(end)) {
		end = new DateTime(ts.getDate2());
	}
	
	if (Message.isDebugOn) {
		Message.printDebug(1, routine,
			_gtype + "Drawing time series from " + _start_date 
			+ " to " + _end_date);
	}

	// Only draw the time series if the units are being ignored or can be
	// converted.  The left axis units are determined at construction.

	if (!_ignore_units) {
		if (_graph_type != TSProduct.GRAPH_TYPE_DURATION 
		    && _graph_type != TSProduct.GRAPH_TYPE_XY_SCATTER) {
		    	String lefty_units = getLayeredPropValue(
				"LeftYAxisUnits", _subproduct, -1, false,
				overrideProps);

			if (!DataUnits.areUnitsStringsCompatible(
			    ts.getDataUnits(),lefty_units,true)) {
				if (lefty_units.equals("")) {
					// new graph -- set units to whatever
					// ts 1's units are
					int how_set_prev = 
						_tsproduct.getPropList()
						.getHowSet();
					_tsproduct.getPropList().setHowSet(
						Prop.SET_AS_RUNTIME_DEFAULT);
					_tsproduct.setPropValue(
						"LeftYAxisUnits", 
						ts.getDataUnits(), 
						_subproduct, -1);
					_tsproduct.getPropList().setHowSet(
						how_set_prev);
				}
				else {
					// no units, so can't draw the graph
					return;
				}
			}
		}
	}

	double lasty = ts.getMissing();
	double symbol_size = 0.0;
	double x;
	double y;
	int drawcount = 0;
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();	
	int symbol = GRSymbol.SYM_NONE;
	String prop_value;

	if (_is_reference_graph) {
		symbol = GRSymbol.SYM_NONE;
	}
	else {	
		prop_value = getLayeredPropValue("SymbolStyle", _subproduct,
			its, false, overrideProps);
		try {	
			symbol = GRSymbol.toInteger(prop_value);
		}
		catch (Exception e) {
			symbol = GRSymbol.SYM_NONE;
		}
	}

	symbol_size = StringUtil.atod(getLayeredPropValue(
		"SymbolSize", _subproduct, its, false, overrideProps));

	// Data label.

	boolean label_symbol = false;
	int label_position = 0;
	String label_format = "";
	String label_position_string;
	String label_units = "";
	String label_value_format = "";

	boolean graphLabelFormatSet = false;

	// First try to get the label format from the time series
	// properties.
	label_format = getLayeredPropValue("DataLabelFormat", _subproduct,
		its, false, overrideProps);
	if (label_format == null || label_format.equals("")) {
		// Try to get from the graph properties.
		label_format = getLayeredPropValue("DataLabelFormat",
			_subproduct, -1, false, overrideProps);
		if (!label_format.equals("")) {
			// Label the format
			label_symbol = true;
			graphLabelFormatSet = true;
		}
	}
	else {	
		label_symbol = true;
	}

	// REVISIT (JTS - 2006-04-26)
	// What happens here is that if the label format has been set in the
	// time series properties, the time series label position will be used,
	// too.  If the label format has been set in the graph properties, the
	// graph's label position should be used.  
	//
	// What is not covered explicitly, though, is what happens if the 
	// label format has been set in both the time series and the graph.
	// Currently, the time series properties will override the graph
	// level properties.  As discussed with SAM, this may not be the
	// desired behavior, but for now it will be the graph's behavior.  

	if (label_symbol) {
		// Are drawing point labels so get the position, set
		// the font, and get the format.
		label_position_string = getLayeredPropValue("DataLabelPosition",
			_subproduct, its, false, overrideProps);
		// Determine the label position automatically, if necessary.
		if (graphLabelFormatSet 
		    || label_position_string.equals("") 
		    || label_position_string.equalsIgnoreCase("Auto")) {
			// Try to get from the graph properties.
			label_position_string = getLayeredPropValue(
				"DataLabelPosition", _subproduct, -1, false,
				overrideProps);
			if (label_position_string.equals("") 
			    || label_position_string.equalsIgnoreCase("Auto")) {
				// Default position
				label_position_string = "Right";
			}
		}

		label_position = GRText.CENTER_Y | GRText.LEFT;

		try {	
			label_position = GRText.parseTextPosition(
				label_position_string);
		}
		catch (Exception e) {
			label_position = GRText.CENTER_Y | GRText.LEFT;
		}

		// The font is only defined at the graph level.
		// Set for point labels.
		String fontname = getLayeredPropValue("DataLabelFontName",
			_subproduct, -1, false, overrideProps);
		String fontsize = getLayeredPropValue("DataLabelFontSize", 
			_subproduct, -1, false, overrideProps);
		String fontstyle = getLayeredPropValue("DataLabelFontStyle", 
			_subproduct, -1, false, overrideProps);
		GRDrawingAreaUtil.setFont(_da_graph, fontname, fontstyle,
			StringUtil.atod(fontsize));

		// Determine the format for the data value in case it
		// is needed to format the label.
		label_units = ts.getDataUnits();
		label_value_format = DataUnits.getOutputFormatString(
			label_units, 0, 4);
	}

	// Bar graph parameters
	double bar_width = 0.0;		// Width actually drawn
	double bar_width_d2 = 0.0;	// bar_width/2
	double full_bar_width = 0.0;	// Width used for positioning
	double full_bar_width_d2 = 0.0;	// full_bar_width/2
	double maxy = 0.0;
	double miny = 0.0;

	int bar_position = 0;		// position 0 means centered on the date
	prop_value = getLayeredPropValue("BarPosition", _subproduct, -1, false,
		overrideProps);
	if (prop_value != null) {
		if (prop_value.equalsIgnoreCase("LeftOfDate")) {
			bar_position = -1;
		}
		else if (prop_value.equalsIgnoreCase("RightOfDate")) {
			bar_position = 1;
		}
	}

	// generate the clipping area that will be set so that no data are 
	// drawn outside of the graph
	Shape clip = GRDrawingAreaUtil.getClip(_da_graph);
	GRDrawingAreaUtil.setClip(_da_graph, _da_graph.getDataLimits());

	// If a bar graph, the bar width is the data interval/nts.  Rather than
	// compute a bar width that may vary some with the plot zoom, always
	// draw filled in and draw a border.  The position of the bar is
	// determined by the "BarPosition" property.

	int nts = getEnabledTSList().size();

	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL
	    && __tslist != null) {
		int numReg = __tslist.size() - 1;
		nts = 0;
		for (int i = 0; i < numReg; i++) {
			if (isTSEnabled(i + 1)) {
				nts++;
			}
		}
	}
	
	boolean draw_bounding_rectangle = true;
	boolean draw_line = true;

	boolean niceSymbols = true;
	int lineWidth = 1;
	
	prop_value = getLayeredPropValue("LineStyle", _subproduct, its, false,
		overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("None")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("GraphType", _subproduct, -1, false,
		overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Point")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("SymbolAntiAlias", -1, -1, false,
		overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	if (!_is_reference_graph) {
		prop_value = getLayeredPropValue("LineWidth", _subproduct, its,
			false, overrideProps);
		if (prop_value != null) {
			if (StringUtil.isInteger(prop_value)) {
				lineWidth = StringUtil.atoi(prop_value);
				if (lineWidth < 0) {
					lineWidth = 1;
				}
			}
		}
	}
	else {
		lineWidth = 1;
		draw_line = true;
	}

	// line dashes are currently only supported
	boolean dashedLine = false;
	double[] lineDash = null;
	prop_value = getLayeredPropValue("LineStyle", _subproduct, its, 
		false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Dashed")) {
		dashedLine = true;
		lineDash = new double[2];
		lineDash[0] = 3;
		lineDash[1] = 5;
	}

	if (_graph_type == TSProduct.GRAPH_TYPE_BAR
	    || _graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
		DateTime temp_date = new DateTime(_tslimits.getDate1());
		// Convert date to a double
		full_bar_width = temp_date.toDouble();
		// Subtract from the date
		if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
			// Use largest number of days in month to prevent
			// overlap.  Need to use day precision to make this
			// work.
			temp_date.setPrecision(DateTime.PRECISION_DAY );
			temp_date.addDay(-30);
		}
		else if (ts.getDataIntervalBase() == TimeInterval.IRREGULAR) {
			// Make width of bar one hour.
			temp_date.addHour(-1);
		}
		else {	
			temp_date.addInterval(interval_base, -interval_mult);
		}

		// Subtract the new value to get the bar width in plotting
		// units.
		full_bar_width -= temp_date.toDouble();
		temp_date = null;
		miny = _data_limits.getMinY();
		maxy = _data_limits.getMaxY();

		// Account for the number of time series...
		full_bar_width /= nts;

		// If bar width is <= 5 pixels in device units, do not
		// draw bounding rectangle because it will hide the data...
		if ((_da_graph.scaleXData(full_bar_width) 
		    - _da_graph.scaleXData(0.0)) <= 5.0) {
			draw_bounding_rectangle = false;
		}
	}

	// Use the same plotting width as the position width for all but
	// monthly since months have different numbers of days.
	if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
		// No need for separator since rectangle is smaller.
		draw_bounding_rectangle = false;
		bar_width = full_bar_width*.85;
	}
	else {	
		bar_width = full_bar_width;
	}

	full_bar_width_d2 = full_bar_width/2.0;
	bar_width_d2 = bar_width/2.0;

	// If set to true, always draw at least a line for the bar.  This makes
	// sure that all the data are shown.  It is not that computationally
	// intensive to do.  Make a property later?  REVISIT

	boolean always_draw_bar = true;
	double leftx = 0.0;	// Left edge of a bar.
	double centerx = 0.0;	// Center of a bar.

	if (interval_base == TimeInterval.IRREGULAR) {
		// Get the data and loop through the vector.  Currently do not
		// use TSIterator because head-to-head performance tests have
		// not been performed.  Need to do so before deciding which
		// approach is faster.
		IrregularTS irrts = (IrregularTS)ts;
		Vector alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;

		for ( int i = 0; i < nalltsdata; i++ ) {
/////////////////////////////////////
// Two indents have been removed from the following to make it more legible
// at the right margin of the page.
		
	tsdata = (TSData)alltsdata.elementAt(i);
	date = tsdata.getDate();
	if (date.greaterThan(end)) {
		// Past the end of where want to go so
		// quit.
		break;
	}

// REVISIT (JTS - 2006-04-26)
// All data flags (returned from getDataFlag()) are being trimmed below.
// In the future, if the spacing of data flags becomes critical, this may need
// revisited.
	if (date.greaterThanOrEqualTo(start)) {
		y = tsdata.getData();
		if (ts.isDataMissing(y)) {
			lasty = y;
			continue;
		}

		// Else, see if need to moveto or lineto
		// the point.
		x = date.toDouble();
		if (((drawcount == 0) || ts.isDataMissing(lasty)) 
		    && (_graph_type != TSProduct.GRAPH_TYPE_BAR
    	&& _graph_type != TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL)) {
			// Always draw the symbol
//if (tsdata != null) 
//Message.printStatus(1, "", "JTS0" + date + ": '" + tsdata.getDataFlag() 
//	+ "'  '" + label_position + "'  '" + y + "'");
			if (_is_reference_graph) {
				// Don't draw symbols
			}
			else if (label_symbol 
			    && ((symbol == GRSymbol.SYM_NONE)
			    || (symbol_size <= 0))) {
				// Text only
				GRDrawingAreaUtil.drawText(_da_graph,
					TSData.toString(label_format,
					label_value_format, date, y, 0.0,
					tsdata.getDataFlag().trim(),
					label_units), 
					x, y, 0.0, label_position);
			}
			else if (label_symbol) {
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias(
					     _da_graph, true);
				}

				// Text and symbol
				GRDrawingAreaUtil.drawSymbolText(_da_graph, 
					symbol, x, y, symbol_size,
					TSData.toString(label_format,
					label_value_format, date, y, 0.0,
					tsdata.getDataFlag().trim(), 
					label_units), 
					0.0, label_position, GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X 
					| GRSymbol.SYM_CENTER_Y);
					
				if (niceSymbols) {
					// turn off antialiasing so that
					// it only applies for symbols
					GRDrawingAreaUtil.setDeviceAntiAlias(
					     _da_graph, false);
				}
			}
			else {	
				// Symbol only
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias(
					     _da_graph, true);
				}

				GRDrawingAreaUtil.drawSymbol(_da_graph, symbol,
					x, y, symbol_size, GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X
					| GRSymbol.SYM_CENTER_Y);

				if (niceSymbols) {
					// turn off antialiasing so that
					// it only applies for symbols
					GRDrawingAreaUtil.setDeviceAntiAlias(
					     _da_graph, false);
				}
			}

			// First point or skipping data. Put second so 
			// symbol coordinates do not set the last point.
			GRDrawingAreaUtil.moveTo(_da_graph, x, y );
		}
		else {	
			// Draw the line segment or bar
			if (_graph_type != TSProduct.GRAPH_TYPE_BAR
	&& _graph_type != TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
				if (draw_line) {
					GRDrawingAreaUtil.setLineWidth(
						_da_graph, lineWidth);
						
					if (dashedLine) {
						GRDrawingAreaUtil.setLineDash(
							_da_graph, lineDash, 0);
					}
					
					GRDrawingAreaUtil.lineTo(_da_graph, x, 
						y);

					// reset the line width to the normal
					// setting for all other drawing
					GRDrawingAreaUtil.setLineWidth(
						_da_graph, 1);

					if (dashedLine) {
						// reset the line dash so that
						// only this line is 
						// drawn dashed
						GRDrawingAreaUtil.setLineDash(
							_da_graph, null, 0);
					}
				}
				else {	
					// no line to draw, so simply move 
					// the position of the stylus
					GRDrawingAreaUtil.moveTo(_da_graph,
						x, y);
				}
				if (_is_reference_graph) {
					// No symbol or label to draw
				}
				else if (label_symbol 
				    && ((symbol == GRSymbol.SYM_NONE) 
				        || (symbol_size <= 0))) {
					// Just text
					GRDrawingAreaUtil.drawText(_da_graph,
						TSData.toString(label_format,
						label_value_format, date, y, 
						0.0, 
						tsdata.getDataFlag().trim(),
						label_units), x, y, 0.0, 
						label_position);
				}
				else if (label_symbol) {
					if (niceSymbols) {
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
					     		_da_graph, true);
					}
					
					// Symbol and label...
					GRDrawingAreaUtil.drawSymbolText(
						_da_graph, symbol, x, y,
						symbol_size, TSData.toString(
						label_format,label_value_format,
						date, y, 0.0, 
						tsdata.getDataFlag().trim(), 
						label_units), 0.0, 
						label_position, GRUnits.DEVICE,
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);
					if (niceSymbols) {
						// turn off antialiasing
						// so it doesn't affect anything
						// else
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
					     		_da_graph, false);
					}
				}
				else {	
					// Just symbol
					if (niceSymbols) {
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
						     	_da_graph, true);
					}
					GRDrawingAreaUtil.drawSymbol(_da_graph,
						symbol, x, y, symbol_size,
						GRUnits.DEVICE, 
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);
					if (niceSymbols) {
						// turn off antialiasing so
						// it doesn't affect anything
						// else
						GRDrawingAreaUtil.
					     		setDeviceAntiAlias(
					     		_da_graph, false);
					}
				}

				// Need because symbol coordinates have set 
				// the last point.
				GRDrawingAreaUtil.moveTo(_da_graph, x, y);
				lasty = y;
				++drawcount;
				continue;
			}

			// If get to here need to draw the line or bar.
			// Shift the bars according to the BarPosition property.
			if (bar_position == -1) {
				// All bars left of date
				centerx = x - full_bar_width_d2
					- (nts - 1) * full_bar_width 
					+ its * full_bar_width;
			}
			else if (bar_position == 1) {
				// Bar right of date.
				centerx = x + full_bar_width_d2
					+ its * full_bar_width;
			}
			else {	
				// Center on date.
				centerx = x - (nts - 1)
					* full_bar_width_d2 
					+ its * full_bar_width;
			}
			
			leftx = centerx - bar_width_d2;
			
			if ((leftx >=_data_limits.getMinX())
			    && ((leftx + bar_width) <= _data_limits.getMaxX())){
				_da_graph.setColor(tscolor);
				if (y >= 0.0) {
					// Positive bars...
					if (miny >= 0.0) {
						// From miny up
						GRDrawingAreaUtil.fillRectangle(
							_da_graph, leftx,
							miny, bar_width,
							(y - miny));
						if (always_draw_bar) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, miny,
								leftx, y);
						}
					}
					else {	
						// From zero up
						GRDrawingAreaUtil.fillRectangle(
							_da_graph, leftx,
							0.0, bar_width, y);
						if (always_draw_bar) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, 0.0,
								leftx, y);
						}
					}
				}
				else {	
					// Negative bars.
					if (maxy >= 0.0) {
						// Up to zero.
						GRDrawingAreaUtil.fillRectangle(
							_da_graph, leftx,
							y, bar_width, -y);
						if (always_draw_bar) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, y,
								leftx, 0.0);
						}
					}
					else {	
						// Up to top negative value
						GRDrawingAreaUtil.fillRectangle(
							_da_graph, leftx,
							y, bar_width,
							(maxy - y));
						if (always_draw_bar) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, y,
								leftx, maxy);
						}
					}
				}

				GRDrawingAreaUtil.setColor(_da_graph, 
					_background);

				if (draw_bounding_rectangle) {
					if (y >= 0.0) {
						if (miny >= 0.0) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph, 
								leftx, miny, 
								leftx, y);
						}
						else {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, 0.0,
								leftx, y);
						}
					}
					else {	
						if (maxy >= 0.0) {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, 0.0,
								leftx, y);
						}
						else {
							GRDrawingAreaUtil
								.drawLine(
								_da_graph,
								leftx, maxy,
								leftx, y);
						}
					}
				}
			}
		}
		lasty = y;
		++drawcount;
	}

// Two indents have been removed from the preceding to make it more legible
// at the right margin of the page.
/////////////////////////////////////
		}
		
		irrts = null;
		alltsdata = null;
		tsdata = null;
		date = null;
	}
	else {	
		// Loop using addInterval
		DateTime date = new DateTime(start);
		// Make sure the time zone is not set
		date.setTimeZone("");

		TSData tsdata = null;
		for (;
		    date.lessThanOrEqualTo(end);
		    date.addInterval(interval_base, interval_mult)) {
			// Use the actual data value
			if (label_symbol) {
				// REVISIT - need to optimize
				tsdata = ts.getDataPoint(date);
				y = tsdata.getData();
			}
			else {	
				y = ts.getDataValue(date);
			}
			
			if (ts.isDataMissing(y)) {
				lasty = y;
				continue;
			}
			
			if (_graph_type == TSProduct.GRAPH_TYPE_PERIOD) {
				// Reset to use se the plotting position of the
				// time series, which will result in a
				// horizontal line.  Want the y position to
				// result in the same order as in the legend,
				// where the first time series is at the top of
				// the legend.  This is accomplished by
				// reversing the Y axis for plotting
				y = its + 1;
			}

			// Else, see if we need to moveto or lineto the point
			x = date.toDouble();

			// Uncomment this for hard-core debugging
			//Message.printStatus(1, routine,
			//	"its=" + its + " date = " 
			//	+ date.toString(DateTime.FORMAT_Y2K_LONG) 
			//	+ " x = " + x + " y=" + y);

			if (((drawcount == 0) || ts.isDataMissing(lasty))
			    && (_graph_type != TSProduct.GRAPH_TYPE_BAR
	&& _graph_type != TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL)) {
				// Previous point was missing so all need to
				// do is draw the symbol (if not a reference
				// graph)				
				if (_is_reference_graph) {
					// Don't label or draw symbol.
				}
				else if (label_symbol 
				    && ((symbol == GRSymbol.SYM_NONE) 
				    || (symbol_size <= 0))) {
					// Just text
					GRDrawingAreaUtil.drawText(_da_graph,
						TSData.toString(label_format,
						label_value_format, date, y, 
						0.0, 
						tsdata.getDataFlag().trim(), 
						label_units), x, y, 0.0, 
						label_position);
				}
				else if (label_symbol) {
					// Symbol and label
					if (niceSymbols) {
						GRDrawingAreaUtil.
							setDeviceAntiAlias(
							_da_graph, true);
					}
					GRDrawingAreaUtil.drawSymbolText(
						_da_graph, symbol, x, y,
						symbol_size,
						TSData.toString ( label_format,
						label_value_format, date, y,
						0.0, 
						tsdata.getDataFlag().trim(),
						label_units ), 0.0,
						label_position,
						GRUnits.DEVICE,
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);
					if (niceSymbols) {
						// turn off antialiasing
						// so nothing is antialiased
						// that shouldn't be
						GRDrawingAreaUtil.
							setDeviceAntiAlias(
							_da_graph, false);
					}
				}
				else {	
					// Just symbol
					if (niceSymbols) {
						GRDrawingAreaUtil.
							setDeviceAntiAlias(
							_da_graph, true);
					}
					GRDrawingAreaUtil.drawSymbol(_da_graph,
						symbol, x, y, symbol_size,
						GRUnits.DEVICE,
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);
					if (niceSymbols) {
						// turn off antialiasing so
						// nothing is antialiased that
						// shouldn't be
						GRDrawingAreaUtil.
							setDeviceAntiAlias(
							_da_graph, false);
					}
				}

				// Do after symbol
				GRDrawingAreaUtil.moveTo(_da_graph, x, y);
				lasty = y;
				++drawcount;
				continue;
			}

			// If here, need to draw the line segment or bar...
			if (_graph_type != TSProduct.GRAPH_TYPE_BAR
	&& _graph_type != TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
				if (draw_line) {
					if (dashedLine) {
						GRDrawingAreaUtil.setLineDash(
							_da_graph, lineDash, 0);
					}
					
					GRDrawingAreaUtil.setLineWidth(
						_da_graph, lineWidth);
					GRDrawingAreaUtil.lineTo (
						_da_graph, x, y );
					GRDrawingAreaUtil.setLineWidth(
						_da_graph, 1);

					if (dashedLine) {
						// turn off the line dashes
						GRDrawingAreaUtil.setLineDash(
							_da_graph, null, 0);
					}
				}
				else {	
					GRDrawingAreaUtil.moveTo(_da_graph, x, 
						y);
				}

				if (_is_reference_graph) {}
				else if (label_symbol 
				    && ((symbol == GRSymbol.SYM_NONE) 
				    || (symbol_size <= 0))) {
					// Text only
					GRDrawingAreaUtil.drawText(_da_graph,
						TSData.toString(label_format,
						label_value_format, date, y, 
						0.0, 
						tsdata.getDataFlag().trim(), 
						label_units), x, y, 0.0, 
						label_position);
				}
				else if (label_symbol) {
					// Symbol and label
					if (niceSymbols) {
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
							_da_graph, true);
					}

					GRDrawingAreaUtil.drawSymbolText(
						_da_graph, symbol, x, y, 
						symbol_size, TSData.toString( 
						label_format, 
						label_value_format, date, y, 
						0.0, 
						tsdata.getDataFlag().trim(), 
						label_units ), 0.0, 
						label_position, GRUnits.DEVICE, 
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);
						
					if (niceSymbols) {
						// turn off antialiasing
						// so nothing is antialiased
						// that shouldn't be
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
							_da_graph, false);
					}
				}
				else {	
					// Symbol only
					if (niceSymbols) {
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
							_da_graph, true);
					}
					
					GRDrawingAreaUtil.drawSymbol(
						_da_graph, symbol, x, y, 
						symbol_size, GRUnits.DEVICE, 
						GRSymbol.SYM_CENTER_X
						| GRSymbol.SYM_CENTER_Y);

					if (niceSymbols) {
						// turn off antialiasing
						// so nothing is antialiased
						// that shouldn't be
						GRDrawingAreaUtil
							.setDeviceAntiAlias(
							_da_graph, false);
					}
				}

				// Need to override last position from symbol
				GRDrawingAreaUtil.moveTo(_da_graph, x, y);
			}
			else {	
				// Drawing bars
				if (bar_position == -1) {
					// All bars left of date
					centerx = x - full_bar_width_d2 
						- (nts - 1) * full_bar_width 
						+ its * full_bar_width;
				}
				else if (bar_position == 1) {
					// Bar right of date
					centerx = x + full_bar_width_d2 
						+ its * full_bar_width;
				}
				else {	
					// Center on date
					centerx = x - (nts - 1) 
						* full_bar_width_d2 
						+ its * full_bar_width;
				}

				leftx = centerx - bar_width_d2;
				if ((leftx >= _data_limits.getMinX())
				    && ((leftx + bar_width) 
				    <= _data_limits.getMaxX())) {
					_da_graph.setColor(tscolor);

/////////////////////////////////////////////
// The following have had two indent levels removed for legibility at the
// right margin
					
			if (y >= 0.0) {
				// Positive bars
				if (miny >=0.0) {
					// From miny up
					GRDrawingAreaUtil.fillRectangle(
						_da_graph, leftx, miny, 
						bar_width, (y - miny));

					if (always_draw_bar) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, miny,
							leftx, y);
					}
				}
				else {	
					// From zero up
					GRDrawingAreaUtil.fillRectangle(
						_da_graph, leftx, 0.0, 
						bar_width, y);

					if (always_draw_bar) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, 0.0, 
							leftx, y);
					}
				}
			}
			else {	
				// Negative bars
				if (maxy >= 0.0) {
					// Up to zero
					GRDrawingAreaUtil.fillRectangle(
						_da_graph, leftx, y, 
						bar_width, -y);

					if (always_draw_bar) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, y, 
							leftx, 0.0);
					}
				}
				else {	
					// Up to top
					GRDrawingAreaUtil.fillRectangle(
						_da_graph, leftx, y, bar_width, 
						(maxy - y));

					if (always_draw_bar) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, y,
							leftx, maxy);
					}
				}
			}
			GRDrawingAreaUtil.setColor(_da_graph, _background);
			if (draw_bounding_rectangle) {
				if (y >= 0.0) {
					if (miny >= 0.0) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, miny, 
							leftx, y);
					}
					else {	
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, 0.0, 
							leftx, y);
					}
				}
				else {	
					if (maxy >= 0.0) {
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx, 0.0, 
							leftx, y);
					}
					else {	
						GRDrawingAreaUtil.drawLine(
							_da_graph, leftx,
							maxy, leftx, y);
					}
				}
			}
// The preceding have had two indent levels removed for legibility at the
// right margin					
/////////////////////////////////////////////
				}
			}
			lasty = y;
			++drawcount;
		}
		date = null;
	}

	// remove the clip around the graph.  This allows other things to be
	// drawn outside the graph bounds
	GRDrawingAreaUtil.setClip(_da_graph, (Shape)null);
	GRDrawingAreaUtil.setClip(_da_graph, clip);
	
	// clean up...
	routine = null;
	end = null;
	start = null;
	tscolor = null;
}

/**
Draw the X-axis grid.  This calls the drawXAxisDateLabels() if necessary.
*/
private void drawXAxisGrid ()
{	if (	(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ||
		(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ) {
		// Do the grid here because it uses simple numbers and not
		// dates...
	
		String color_prop = _tsproduct.getLayeredPropValue (
			"BottomXAxisMajorGridColor", _subproduct, -1, false );
		if (	(color_prop == null) ||
			color_prop.equalsIgnoreCase("None") ) {
			return;
		}

		GRColor color;
		try {	color = GRColor.parseColor(color_prop);
		}
		catch ( Exception e ) {
			color = GRColor.black;
		}
		_da_graph.setColor ( color );
		double [] y = new double[2];
		y[0] = _data_limits.getBottomY();
		y[1] = _data_limits.getTopY();
		// Draw a vertical grid.
		GRAxis.drawGrid ( _da_graph, _xlabels.length, _xlabels,
			2, y, GRAxis.GRID_SOLID );
	}
	else {	// Draw the grid in the same code that does the X-axis
		// date/time labels so they are consistent...
		drawXAxisDateLabels ( true );
	}
}

/**
Draw the X-axis date/time labels.  This method can be called with "draw_grid"
set as true to draw the background grid, or "draw_grid" set to false to draw
the labels.
@param grid_only If true, only draw the x-axis grid lines.  If false, only
draw labels and tic marks.
*/
private void drawXAxisDateLabels ( boolean draw_grid ) {
	if (!__drawLabels) {
		return;
	}

	boolean log_y = false;
	boolean log_xy_scatter = false;
	String prop_value = _tsproduct.getLayeredPropValue (
			"LeftYAxisType", _subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y = true;
	}
	prop_value = _tsproduct.getLayeredPropValue (
		"XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
		log_xy_scatter = true;
	}	

	if ( draw_grid ) {
		prop_value = _tsproduct.getLayeredPropValue (
			"BottomXAxisMajorGridColor", _subproduct, -1, false );
		if (	(prop_value == null) ||
			prop_value.equalsIgnoreCase("None") ) {
			return;
		}
		GRColor color = null;
		try {	color = GRColor.parseColor(prop_value);
		}
		catch ( Exception e ) {
			color = GRColor.lightGray;
		}
		GRDrawingAreaUtil.setColor ( _da_graph, color );
	}
	else {	GRDrawingAreaUtil.setColor ( _da_bottomx_label, GRColor.black );
	}

	// Now draw all the labels...

	String fontname = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontName", _subproduct, -1, false );
	String fontsize = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontSize", _subproduct, -1, false );
	String fontstyle = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, fontname, fontstyle,
			StringUtil.atod(fontsize) );

	// This logic for date labels ignores the _xlabels array that was used
	// elsewhere.  Instead, special care is given to check the precision of
	// the dates, the period that is visible, and the font size.  One or two
	// layers of date labels is shown with major (and possibly minor) tic
	// marks.

	DateTime start = null, label_date = null;
	int buffer = 6;			// 2*Pixels between labels (for
					// readability)
	int label_width = 0;		// Width of a sample label.
	int label0_devx, label1_devx;	// X device coordinates for
					// adjacent test labels.
	double[] xt = new double[2];	// Major ticks
	double[] xt2 = new double[2];	// Minor ticks
	double[] yt = new double[2];	// Major ticks
	double[] yt2 = new double[2];	// Minor ticks
	int label_spacing = 0;		// Spacing of labels, center to center.
	double tic_height = 0.0;	// Height of major tic marks
	yt[0] = _ylabels[0];
	yt2[0] = _ylabels[0];
	// Figure out the y-positions and tic height (same regardless of
	// intervals being used for labels)...
	if (log_y) {
		// Need to make sure the line is nice length!
		tic_height = yt[0]*.05;
		yt[1] = yt[0] + tic_height;
		yt2[1] = yt2[0] + tic_height/2.0;
	}
	else if (log_xy_scatter) {
		tic_height = yt[0]*.05;
		yt[1] = yt[0] + tic_height;
		yt2[1] = yt2[0] + tic_height/2.0;
	}
	else {	tic_height = _data_limits.getHeight()*.02;
		yt[1] = yt[0] + tic_height;
		yt2[1] = yt2[0] + tic_height/2.0;
	}
	if ( _graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
		// Reversed axes...
		yt[0] = getEnabledTSList().size() + 1;
		yt2[0] = getEnabledTSList().size() + 1;
		tic_height = _data_limits.getHeight()*.02;
		yt[1] = yt[0] - tic_height;
		yt2[1] = yt2[0] - tic_height/2.0;
	}
	if ( draw_grid ) {
		// Reset with the maximum values...
		yt[0] = _data_limits.getMinY();
		yt[1] = _data_limits.getMaxY();
	}

	if (	(_xaxis_date_precision == DateTime.PRECISION_YEAR) ||
		((_end_date.getAbsoluteMonth() -
		_start_date.getAbsoluteMonth()) > 36) ) {
		// Long periods where showing the year and possibly
		// month are good enough.
		//
		// The top axis label is the year and the bottom label
		// is not used.  Additional criteria are:
		//
		// *	If the period allows all years to be labelled, do it
		// *	If not, try to plot even years.
		// *	Then try every 5 years.
		// *	Then try every 10 years.
		// *	Then try every 20 years.
		// *	Then try every 25 years.
		// *	Then try every 50 years.
		//
		// Apparently "9999" is not the widest string for fonts and
		// picking other numbers or letters does not always give nice
		// spacing so to be sure try different numbers to get the max
		// likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( 
			_da_bottomx_label, "" + ic + ic + ic + ic,
			GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years
		// can be shown.  Determine by seeing if the first two
		// overlap...
		int[] year_increments ={ 1, 2, 5, 10, 20, 25, 50, 100 };
		int year_increment = 1;
		boolean found = false;
		for (	int itry = 0;
			itry < year_increments.length; itry++ ) {
			start = new DateTime ( _start_date );
			start.setPrecision(DateTime.PRECISION_YEAR );
			
			year_increment = year_increments[itry];
			start.setYear((start.getYear()/year_increment)*
				year_increment );
			label_date = new DateTime ( start );
			label_date.addYear ( year_increment );
			label0_devx=(int)_da_bottomx_label.scaleXData(
				start.toDouble());
			label1_devx=(int)_da_bottomx_label.scaleXData(
				label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label...
			year_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot
		// position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		for ( ; ; date.addYear ( year_increment ) ) {
			// Draw minor tick marks first because they may
			// cover an area on the edge of the graph. 
			if ( !draw_grid ) {	// Don't draw minor tics when
						// drawing grid
			if ( year_increment == 1 ) {
				if ( label_spacing > 70 ) {
					// Have enough room for a minor
					// tic mark every month...
					label_date = new DateTime ( date);
					label_date.setPrecision (
					DateTime.PRECISION_MONTH );
					label_date.setMonth ( 1 );
					// Work backwards...
					for ( int it = 0; it < 11; it++ ) {
						label_date.addMonth(-1);
						x=label_date.toDouble();
						if (x < _data_limits.getMinX()){
							continue;
						}
						else if ( x >
							_data_limits.getMaxX()){
							continue;
						}
						xt2[0] = x;
						xt2[1] = x;
						GRDrawingAreaUtil.drawLine (
							_da_graph, xt2, yt2 );
					}
				}
				else {	// Have enough room for a minor tic mark
					// at 6 month interval...
					label_date = new DateTime ( date);
					label_date.setPrecision (
					DateTime.PRECISION_MONTH );
					label_date.setMonth ( 1 );
					label_date.addMonth ( -6 );
					x=label_date.toDouble();
					if((x>=_data_limits.getMinX())&&
						(x <= _data_limits.getMaxX())){
						xt2[0] = x;
						xt2[1] = x;
						GRDrawingAreaUtil.drawLine (
							_da_graph, xt2, yt2 );
					}
				}
			}
			else if ((year_increment == 5) &&
				(label_spacing > 50) ) {
				// Have enough room for a minor
				// tic mark every year...
				label_date = new DateTime ( date);
				// Work backwards...
				for (	int it = 0; it < 4; it++ ) {
					label_date.addYear(-1);
					x=label_date.toDouble();
					if ( x <_data_limits.getMinX()){
						continue;
					}
					else if ( x > _data_limits.getMaxX()){
						continue;
					}
					xt2[0] = x;
					xt2[1] = x;
					GRDrawingAreaUtil.drawLine (
						_da_graph, xt2, yt2 );
				}
			}
			else if ( (year_increment == 2) ||
				(year_increment == 10) ||
				(year_increment == 20) ) {
				// Have enough room for a minor tic in the
				// middle...
				label_date = new DateTime ( date);
				label_date.addYear ( -year_increment/2);
				x=label_date.toDouble();
				if (	(x >= _data_limits.getMinX()) &&
					(x <= _data_limits.getMaxX())){
					xt2[0] = x;
					xt2[1] = x;
					GRDrawingAreaUtil.drawLine (
						_da_graph, xt2, yt2 );
				}
			}
			} // end !draw_grid
			// Don't worry about others...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_limits.getMaxX() ) {
				break;
			}
			if ( draw_grid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
			else {	// Draw the labels and tics...
				GRDrawingAreaUtil.drawText (
				_da_bottomx_label, date.toString(), x,
				_datalim_bottomx_label.getTopY(), 0.0,
				GRText.CENTER_X|GRText.TOP );
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
		}
	}
	else if ((_xaxis_date_precision==DateTime.PRECISION_MONTH)||
		((_end_date.getAbsoluteDay() - _start_date.getAbsoluteDay() > 90)) ){
		// Months less than 36 months or higher precision data
		// more than 90 days...
		//
		// The top axis label is the month and the bottom label
		// is the year.  Additional criteria are:
		//
		// *	If the period allows all months to be labelled, do it
		// *	If not, try to plot even months.
		// *	Then try every 3 months.
		// *	Then try every 4 months.
		// *	Then try every 6 months.
		// Apparently "99" is not the widest string for fonts and
		// picking other numbers or letters does not always give nice
		// spacing so to be sure try different numbers to get the max
		// likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years
		// can be shown.  Determine by seeing if the first two overlap..
		int[] month_increments = { 1, 2, 3, 4, 6 };
		int month_increment = 1;
		boolean found = false;
		for (	int itry = 0;
			itry < month_increments.length; itry++ ) {
			start = new DateTime ( _start_date, DateTime.DATE_FAST );			
			start.setPrecision(DateTime.PRECISION_MONTH);
			
			month_increment = month_increments[itry];
			start.setMonth((start.getMonth()/month_increment)*month_increment );
			if ( start.getMonth() == 0 ) {
				start.setMonth(1);
			}
			label_date = new DateTime ( start );
			label_date.addMonth ( month_increment );
			label0_devx=(int)_da_bottomx_label.scaleXData( start.toDouble());
			label1_devx=(int)_da_bottomx_label.scaleXData( label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label...
			month_increment = 10000;
		}
		// When here, do the labeling.  Just label until the
		// plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addMonth ( month_increment ) ) {
			// Draw minor tick marks first because they may
			// cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_limits.getMaxX() ) {
				break;
			}
			if ( draw_grid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( _da_bottomx_label,
				"" + date.getMonth(),x, _datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getMonth() <= month_increment ) {
					// Label the year...
					GRDrawingAreaUtil.drawText (
					_da_bottomx_label, "" + date.getYear(),
					x, _datalim_bottomx_label.getBottomY(),
					0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
		}
		if ( !draw_grid && (nlabel2 == 0) ) {
			// Need to draw at least one year label...
			date = new DateTime(start);
			for ( ; ; date.addMonth ( month_increment ) ) {
				x = date.toDouble();
				if ( x < _data_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label, "" + date.getYear(), x,
					_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					break;
			}
		}
	}
	else if ((_xaxis_date_precision == DateTime.PRECISION_DAY)||
		(TSUtil.calculateDataSize(_start_date,_end_date, TimeInterval.HOUR, 1) > 168) ) {
		// Days less than 60 days or higher
		// precision data more than 7 days (168 hours)...
		//
		// The top axis label is the day and the bottom label
		// is YYYY-MM.  Additional criteria are:
		//
		// *	If the period allows all days to be labelled, do it
		// *	If not, try to plot even days.
		// *	Then try every 7 days.
		// Apparently "99" is not the widest string for fonts
		// and picking other numbers or letters does not always
		// give nice spacing so to be sure try different numbers
		// to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( 
			_da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years
		// can be shown.  Determine by seeing if the first two overlap..
		int[] day_increments = { 1, 2, 7 };
		int day_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < day_increments.length; itry++ ) {
            // The following may set the day to zero so use DATE_FAST
			start = new DateTime ( _start_date, DateTime.DATE_FAST );		
			start.setPrecision(DateTime.PRECISION_DAY);
					
			day_increment = day_increments[itry];
			start.setDay((start.getDay()/day_increment)*day_increment );
			if ( start.getDay() == 0 ) {
				start.setDay(1);
			}
			label_date = new DateTime ( start );
			label_date.addDay ( day_increment );
			label0_devx=(int)_da_bottomx_label.scaleXData(start.toDouble());
			label1_devx=(int)_da_bottomx_label.scaleXData(label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label...
			day_increment = 10000;
		}
		// When here, do the labeling.  Just label until the
		// plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addDay ( day_increment ) ) {
			// Draw minor tick marks first because they may
			// cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_limits.getMaxX() ) {
				break;
			}
			if ( draw_grid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( _da_bottomx_label,	"" + date.getDay(), x,
					_datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getDay() <= day_increment ) {
					// Label the year and month...
					GRDrawingAreaUtil.drawText ( _da_bottomx_label,
					date.toString(DateTime.FORMAT_YYYY_MM),
					x, _datalim_bottomx_label.getBottomY(),
					0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
		}
		if ( !draw_grid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show the year...
			date = new DateTime ( start );
			for ( ; ; date.addDay ( day_increment ) ) {
				x = date.toDouble();
				if ( x < _data_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label,	date.toString(DateTime.FORMAT_YYYY_MM), x,
				_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else if ((_xaxis_date_precision == DateTime.PRECISION_HOUR)||
		(TSUtil.calculateDataSize(_start_date,_end_date,
		TimeInterval.MINUTE,1) > 1440) ) {
		// Hours less than 7 days or minute data more than 1 day...
		//
		// The top axis label is the hour and the bottom label
		// is YYYY-MM-DD.  Additional criteria are:
		//
		// *	If the period allows all hours to be labelled, do it
		// *	If not, try to plot even hours.
		// *	If not, try to plot every 3 hours.
		// *	If not, try to plot every 4 hours.
		// *	If not, try to plot every 6 hours.
		// *	If not, try to plot every 12 hours.
		//
		// Apparently "99" is not the widest string for fonts
		// and picking other numbers or letters does not always
		// give nice spacing so to be sure try different numbers
		// to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( 
			_da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		int[] hour_increments = { 1, 2, 3, 4, 6, 12 };
		int hour_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < hour_increments.length; itry++ ) {
			start = new DateTime ( _start_date, DateTime.DATE_FAST );
			start.setPrecision(DateTime.PRECISION_HOUR);
			
			hour_increment = hour_increments[itry];
			start.setHour((start.getHour()/
				hour_increment)*hour_increment );
			label_date = new DateTime ( start );
			label_date.addHour ( hour_increment );
			label0_devx=(int)_da_bottomx_label.scaleXData( start.toDouble());
			label1_devx=(int)_da_bottomx_label.scaleXData( label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label...
			hour_increment = 10000;
		}
		// When here, do the labeling.  Just label until the
		// plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addHour ( hour_increment ) ) {
			// Draw minor tick marks first because they may
			// cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_limits.getMaxX() ) {
				break;
			}
			if ( draw_grid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
			else {	GRDrawingAreaUtil.drawText ( _da_bottomx_label,
				"" + date.getHour(), x,
				_datalim_bottomx_label.getTopY(), 0.0,
				GRText.CENTER_X|GRText.TOP );
				if ( date.getHour() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label YYYY-MM-DD...
						GRDrawingAreaUtil.drawText (
						_da_bottomx_label,
						date.toString(
						DateTime.FORMAT_YYYY_MM_DD), x,
						_datalim_bottomx_label.getBottomY(),
						0.0,
						GRText.CENTER_X|GRText.BOTTOM );
					}
					else {	// Label MM-DD...
						GRDrawingAreaUtil.drawText (
						_da_bottomx_label,
						date.toString(
						DateTime.FORMAT_MM_DD), x,
						_datalim_bottomx_label.getBottomY(),
						0.0,
						GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
		}
		if ( !draw_grid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			for ( ; ; date.addHour ( hour_increment ) ) {
				x = date.toDouble();
				if ( x < _data_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label,
				date.toString(DateTime.FORMAT_YYYY_MM_DD),
				x, _datalim_bottomx_label.getBottomY(),
				0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else {	// All that is left is minute data less than 1 day...
		//
		// The top axis label is the minute and the bottom label
		// is YYYY-MM-DD HH.  Additional criteria are:
		//
		// *	If the period allows all minutes to be labelled, do it
		// *	If not, try to plot even minutes.
		// *	If not, try to plot every 5 minutes.
		// *	If not, try to plot every 10 minutes.
		// *	If not, try to plot every 15 minutes.
		// *	If not, try to plot every 20 minutes.
		// *	If not, try to plot every 30 minutes.
		//
		// Apparently "99" is not the widest string for fonts and
		// picking other numbers or letters does not always give nice
		// spacing so to be sure try different numbers to get the max
		// likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( 
			_da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		int[] minute_increments = { 1, 2, 5, 10, 15, 20, 30 };
		int minute_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < minute_increments.length; itry++ ) {
			start = new DateTime ( _start_date );
			start.setPrecision( DateTime.PRECISION_MINUTE);
			
			minute_increment = minute_increments[itry];
			start.setMinute((start.getMinute()/
				minute_increment)*minute_increment );
			label_date = new DateTime ( start );
			label_date.addMinute ( minute_increment );
			label0_devx=(int)_da_bottomx_label.scaleXData(
				start.toDouble());
			label1_devx=(int)_da_bottomx_label.scaleXData(
				label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label...
			minute_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot
		// position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addMinute ( minute_increment ) ) {
			// Draw minor tick marks first because they may
			// cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_limits.getMaxX() ) {
				break;
			}
			if ( draw_grid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
			else {	GRDrawingAreaUtil.drawText (
					_da_bottomx_label, "" +
					date.getMinute(), x,
					_datalim_bottomx_label.getTopY(), 0.0,
					GRText.CENTER_X|GRText.TOP );
				if ( date.getMinute() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label the YYYY-MM-DD:HH...
						GRDrawingAreaUtil.drawText (
						_da_bottomx_label,
						date.toString(
						DateTime.FORMAT_YYYY_MM_DD)+
						":" +
						StringUtil.formatString(
						date.getHour(),"%02d"), x,
						_datalim_bottomx_label.
						getBottomY(), 0.0,
						GRText.CENTER_X|GRText.BOTTOM );
					}
					else {	// Label the HH...
						GRDrawingAreaUtil.drawText (
						_da_bottomx_label,
						"" +
						StringUtil.formatString(
						date.getHour(),"%02d"), x,
						_datalim_bottomx_label.
						getBottomY(), 0.0,
						GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_graph, xt, yt );
			}
		}
		if ( !draw_grid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			//for ( ; ; date.addMinute ( minute_increment )) {}
			for ( ; ; date.addHour ( 1 )) {
				x = date.toDouble();
				if ( x < _data_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label,
				date.toString(DateTime.FORMAT_YYYY_MM_DD)+
				":" + StringUtil.formatString(date.getHour(),
				"%02d"),
				x, _datalim_bottomx_label.getBottomY(),
				0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
}

/**
Draw a scatter plot.  One X-axis time series is drawn against multiple Y-axis
time series.
*/
private void drawXYScatterPlot ()
{	String routine = "TSGraph.drawXYScatterPlot";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Scatter data limits are " +
		_data_limits.toString() );
	}
	if ( __tslist == null ) {
		return;
	}
	TS ts0 = (TS)__tslist.elementAt(0);
	if ( ts0 == null) {
		return;
	}
	DateTime start = new DateTime ( ts0.getDate1() );
	DateTime end = new DateTime ( ts0.getDate2() );
	int interval_base = ts0.getDataIntervalBase();
	int interval_mult = ts0.getDataIntervalMult();

	// Assume regression data, even though some may be null...

	int nreg = __tslist.size() - 1;

	// Loop through the x-axis (independent) time series and draw, using
	// the same y-axis (dependent) time series for each...

	DateTime date = null;
	TSRegression regressionData = null;
	GRColor plot_color = null;
	TS ts = null;
	String prop_val = null;
	boolean draw_line = true;
	boolean label_symbol = false;	// Default...
	String label_position_string;
	String label_units = "";
	String label_format = "";
	String label_value_format = "";
	String label;
	int label_position = 0;
	boolean analyze_monthly = false;
	double [] xp = null;
	double [] yp = null;
	double [] yp2 = null;
	double [] yp_sorted = null;
	double [] yp2_sorted = null;
	double [] xp2_sorted = null;
	int [] sort_order = null;
	double A = 0.0;
	double B = 0.0;
	double F = 0.0, xbar = 0.0, ybar = 0.0, left, right, xsum = 0.0,
			yhatsum = 0.0;
	double x, y;
	int iyci;
	int n1;
	double dn1;
	String prop_value;
	double min_datay = _data_limits.getMinY();
	double max_datay = _data_limits.getMaxY();

	boolean niceSymbols = true;
	prop_value = _tsproduct.getLayeredPropValue(
		"SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	
	for (int i = 0, its = 1; i < nreg; i++, its++) {
		ts = (TS)__tslist.elementAt(its);
		if (ts == null || !isTSEnabled(i)) {
			continue;
		}
		// Draw a the line of best fit (if can't do this can still
		// draw the data below)...
	
		draw_line = true;
		regressionData = (TSRegression)_regression_data.elementAt(i);
		if ( regressionData == null ) {
			Message.printWarning ( 2, routine,
			"Regression data for TS [" + i + "] is null." );
			draw_line = false;
		}

		prop_val = _tsproduct.getLayeredPropValue (
			"RegressionLineEnabled", _subproduct, its, false );
		if ( (prop_val == null) || prop_val.equalsIgnoreCase("false") ){
			draw_line = false;
		}
		// For now use the font for the bottom x axis tic label for the
		// curve fit line...
		String fontname = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontName", _subproduct, -1, false );
		String fontsize = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontSize", _subproduct, -1, false );
		String fontstyle = "BOLD";//_tsproduct.getLayeredPropValue (
			//"BottomXAxisLabelFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_graph, fontname, fontstyle,
			StringUtil.atod(fontsize) );
		boolean [] analyze_month = null;
		// This applies whether monthly or one equation...
		if ( (_regression_data != null) && draw_line ) {
			analyze_month = regressionData.getAnalyzeMonth();
			// Draw single and monthly lines, if available.
			// For now always draw everything in black...
			// SAMX later need CurveFitLineColor with multiple
			// colors.
			analyze_monthly = regressionData.isMonthlyAnalysis();
			GRDrawingAreaUtil.setColor ( _da_graph, GRColor.black );
			int nlines = 1;
			if ( analyze_monthly ) {
				nlines = 12;
			}
			xp = new double[2];
			yp = new double[2];
			A = 0.0;
			B = 0.0;
			double xlabel, ylabel;
			for ( int il = 1; il <= nlines; il++ ) {
				if (	analyze_monthly &&
					!regressionData.isAnalyzed(il) ) {
					continue;
				}
				else if ( !analyze_monthly &&
					!regressionData.isAnalyzed() ) {
					continue;
				}
				if ( analyze_monthly ) {
					try {	A = regressionData.getA(il);
						B = regressionData.getB(il);
					}
					catch ( Exception le ) {
						continue;
					}
				}
				else {	A = regressionData.getA();
					B = regressionData.getB();
				}
				// Should always know this point...
				xp[0] = _data_limits.getMinX();
				yp[0] = A + xp[0]*B;
				// Make sure Y does not go off the page...
				xp[1] = _data_limits.getMaxX();
				yp[1] = A + xp[1]*B;
				if ( yp[1] > max_datay ) {
					yp[1] = max_datay;
					xp[1] = (yp[1] - A)/B;
				}
				else if ( yp[1] < min_datay ) {
					yp[1] = min_datay;
					xp[1] = (yp[1] - A)/B;
				}
				if ( yp[0] > max_datay ) {
					yp[0] = max_datay;
					xp[0] = (yp[0] - A)/B;
				}
				else if ( yp[0] < min_datay ) {
					yp[0] = min_datay;
					xp[0] = (yp[0] - A)/B;
				}
				GRDrawingAreaUtil.drawLine (_da_graph, xp, yp );
				if ( analyze_monthly ) {
					// Number the month at the top of the
					// graph.  Don't optimize the position
					// yet...
					xlabel = xp[1];
					ylabel = yp[1];
					GRDrawingAreaUtil.drawText (_da_graph,
					"" + il, xlabel, ylabel,
					0.0, GRText.CENTER_X|GRText.CENTER_Y );
				}
				else {	label =
					regressionData.getPropList().getValue(
					"AnalysisMonth" );
					if (	(label != null) &&
						!label.equals("") ) {
						xlabel = xp[1];
						ylabel = yp[1];
						GRDrawingAreaUtil.drawText (
						_da_graph, label,
						xlabel, ylabel, 0.0,
						GRText.CENTER_X|
						GRText.CENTER_Y );
					}
				}
				prop_value = _tsproduct.getLayeredPropValue (
					"XYScatterConfidenceInterval",
					_subproduct, its, false );
				try {	if ( analyze_monthly ) {
						n1 = regressionData.getN1(il);
					}
					else {	n1 = regressionData.getN1();
					}
				}
				catch ( Exception e ) {
					n1 = 0;
				}
				if (	(prop_value != null) &&
					(prop_value.equals("95") ||
					prop_value.equals("99")) && (n1 > 0) ){
					// SAMX - need to figure out if this
					// is done in the TSRegression
					// constructor or here - don't want
					// to carry around a lot of points but
					// don't want to hurt performance.  For
					// now do in the draw code since this
					// really only makes sense when only a
					// few points are analyzed.
					// Calculate the information that is
					// necessary to draw the confidence
					// interval - only need to do this when
					// regression data is calculated.
					yp = new double[n1];
					xp = new double[n1];
					yp2 = new double[n1];
					dn1 = (double)n1;
					try {
					F =
					FDistribution.getCumulativeFDistribution
						( 2, (n1 - 2),
						(100 -
						StringUtil.atoi(prop_value)) );
					if ( analyze_monthly ) {
						ybar =
						regressionData.getMeanY1(il);
						xbar =
						regressionData.getMeanX1(il);
					}
					else {	ybar =
						regressionData.getMeanY1();
						xbar =
						regressionData.getMeanX1();
					}
					}
					catch ( Exception e ) {
						// Should never happen because
						// of the check for N1 above.
					}
					xsum = 0.0;
					yhatsum = 0.0;
					date = new DateTime ( start );
					for (	;
						date.lessThanOrEqualTo( end );
						date.addInterval(interval_base,
						interval_mult) ) {
						if (	!analyze_month[
							date.getMonth() - 1] ) {
							continue;
						}
						if (	analyze_monthly &&
						!regressionData.isAnalyzed(
							date.getMonth()) ) {
							continue;
						}
						else if ( !analyze_monthly &&
						!regressionData.isAnalyzed() ){
							continue;
						}
						y = ts0.getDataValue(date);
						if ( ts0.isDataMissing(y) ) {
							continue;
						}
						x = ts.getDataValue(date);
						if ( ts.isDataMissing(x) ) {
							continue;
						}
						// Calculate the totals that are
						// needed...
						yhatsum += (((A + B*x) - y)*
							((A + B*x) - y));
						xsum += ((x - xbar)*(x - xbar));
					}
					date = new DateTime ( start );
					iyci = 0;
					for (	;
						date.lessThanOrEqualTo( end );
						date.addInterval(interval_base,
						interval_mult) ) {
						if (	!analyze_month[
							date.getMonth() - 1] ) {
							continue;
						}
						if (	analyze_monthly &&
						!regressionData.isAnalyzed(
							date.getMonth()) ) {
							continue;
						}
						else if ( !analyze_monthly &&
						!regressionData.isAnalyzed() ){
							continue;
						}
						y = ts0.getDataValue(date);
						if ( ts0.isDataMissing(y) ) {
							continue;
						}
						x = ts.getDataValue(date);
						if ( ts.isDataMissing(x) ) {
							continue;
						}
						left = ybar + B*(x - xbar);
						right = Math.sqrt(2.0*F)*
							Math.sqrt(
							(1.0/(dn1 - 2.0))*
							yhatsum) *
							Math.sqrt(1/dn1 +
							((x - xbar)*(x - xbar))/
							xsum);
						xp[iyci] = x;
						yp[iyci] = left + right;
						yp2[iyci++] = left - right;
					}
					// Sort the X coordinates so that the
					// line is drawn without zig-zagging
					// back on itself...
					sort_order = new int[n1];
					MathUtil.sort ( xp, MathUtil.SORT_QUICK,
						MathUtil.SORT_ASCENDING,
						sort_order, true );
					yp_sorted = new double[n1];
					yp2_sorted = new double[n1];
					xp2_sorted = new double[n1];
					for ( int i2 = 0; i2 < iyci; i2++ ) {
						yp_sorted[i2] =
							yp[sort_order[i2]];
						xp2_sorted[i2]=xp[i2];
						yp2_sorted[i2]=
							yp2[sort_order[i2]];
					}
					// Adjust to make sure points are on the
					// graph...
					adjustConfidenceCurve ( xp, yp_sorted,
							iyci );
					adjustConfidenceCurve ( xp2_sorted,
						yp2_sorted, iyci );
					// Now draw lines...
					GRDrawingAreaUtil.drawPolyline (
						_da_graph, iyci,
						xp, yp_sorted );
					GRDrawingAreaUtil.drawPolyline (
						_da_graph, iyci,
						xp2_sorted, yp2_sorted );
				}
			}
			xp = null;
			yp = null;
			yp2 = null;
			yp_sorted = null;
			yp2_sorted = null;
			sort_order = null;
		}
	
		// Now draw the data only for months that have been analyzed...
	
		try {	plot_color = GRColor.parseColor(
				_tsproduct.getLayeredPropValue (
				"Color", _subproduct, its, false ) );
		}
		catch ( Exception e ) {
			plot_color = GRColor.black;
		}
		GRDrawingAreaUtil.setColor ( _da_graph, plot_color );
		prop_value = _tsproduct.getLayeredPropValue (
			"SymbolStyle", _subproduct, its, false );
		int symbol = GRSymbol.SYM_NONE;
		try {	symbol = GRSymbol.toInteger(prop_value);
		}
		catch ( Exception e ) {
			symbol = GRSymbol.SYM_NONE;
		}
		double symbol_size = StringUtil.atod(
				_tsproduct.getLayeredPropValue (
				"SymbolSize", _subproduct, its, false ) );
		// First try to get the label format from the time series
		// properties...
		label_format = _tsproduct.getLayeredPropValue (
				"DataLabelFormat", _subproduct, its, false );
		if ( label_format.equals("") ) {
			// Try to get from the graph properties....
			label_format = _tsproduct.getLayeredPropValue (
				"DataLabelFormat", _subproduct, -1, false );
			if ( !label_format.equals("") ) {
				// Label the format...
				label_symbol = true;
			}
		}
		else {	label_symbol = true;
		}
		if ( label_symbol ) {
			// Are drawing point labels so get the position, set
			// the font, and get the format...
			label_position_string = _tsproduct.getLayeredPropValue (
				"DataLabelPosition", _subproduct, its, false );
			if (	label_position_string.equals("") ||
				label_position_string.equalsIgnoreCase("Auto")){
				// Try to get from the graph properties....
				label_position_string =
					_tsproduct.getLayeredPropValue (
					"DataLabelPosition", _subproduct, -1,
					false );
				if (	label_position_string.equals("") ||
					label_position_string.equalsIgnoreCase(
					"Auto") ) {
					// Default position...
					label_position_string = "Right";
				}
			}
			label_position = GRText.CENTER_Y | GRText.LEFT;
			try {	label_position = GRText.parseTextPosition (
					label_position_string );
			}
			catch ( Exception e ) {
				label_position = GRText.CENTER_Y | GRText.LEFT;
			}
			// The font is only defined at the graph level...
			// Set for point labels...
			fontname = _tsproduct.getLayeredPropValue (
				"DataLabelFontName", _subproduct, -1, false );
			fontsize = _tsproduct.getLayeredPropValue (
				"DataLabelFontSize", _subproduct, -1, false );
			fontstyle = _tsproduct.getLayeredPropValue (
				"DataLabelFontStyle", _subproduct, -1, false );
			GRDrawingAreaUtil.setFont ( _da_graph, fontname,
				fontstyle, StringUtil.atod(fontsize) );
			// Determine the format for the data value in case it
			// is needed to format the label...
			label_units = ts.getDataUnits();
			label_value_format = DataUnits.getOutputFormatString(
				label_units, 0, 4 );
		}
		date = new DateTime ( start );
		for (	;
			date.lessThanOrEqualTo( end );
			date.addInterval(interval_base, interval_mult) ) {
			// If drawing a line, only draw points that are
			// appropriate for the line.  If not drawing the line
			// of best fit, draw all available data...
			if (	draw_line &&
				!analyze_month[date.getMonth() - 1] ) {
				continue;
			}
			if (	draw_line && analyze_monthly &&
				!regressionData.isAnalyzed(date.getMonth()) ) {
				continue;
			}
			else if ( draw_line && !analyze_monthly &&
				(!regressionData.isAnalyzed() &&
				(regressionData.getN1() != 1)) ) {
				continue;
			}
			// Dependent is always the first one...
			y = ts0.getDataValue(date);
			if ( ts0.isDataMissing(y) ) {
				continue;
			}
			x = ts.getDataValue(date);
			if ( ts.isDataMissing(x) ) {
				continue;
			}

			//Message.printStatus ( 1, routine,
			//"SAMX Drawing " + x + "," + y );
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(
					_da_graph, true);
			}
			if ( label_symbol ) {
				GRDrawingAreaUtil.drawSymbolText ( _da_graph,
					symbol, x, y, symbol_size,
					TSData.toString ( label_format,
					label_value_format, date, x, y,
					"", label_units ), 0.0,
					label_position,
					GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X|
					GRSymbol.SYM_CENTER_Y );
			}
			else {	GRDrawingAreaUtil.drawSymbol (
					_da_graph, symbol, x, y, symbol_size,
					GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X|
					GRSymbol.SYM_CENTER_Y );
			}
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(
					_da_graph, false);
			}
		}
	}
	start = null;
	end = null;
	date = null;
	ts0 = null;
	ts = null;
	plot_color = null;
}

/**
Draw the Y-axis grid lines.  Currently only the major grid lines are drawn.
*/
private void drawYAxisGrid()
{	String prop_value = _tsproduct.getLayeredPropValue (
			"LeftYAxisMajorGridColor", _subproduct, -1, false );
	if ( (prop_value == null) || prop_value.equalsIgnoreCase("None") ) {
		return;
	}

	GRColor color;
	try {	color = GRColor.parseColor(prop_value);
	}
	catch ( Exception e ) {
		color = GRColor.black;
	}
	_da_graph.setColor ( color );
	double [] x = new double[2];
	x[0] = _data_limits.getLeftX();
	x[1] = _data_limits.getRightX();
	// Draw a horizontal grid.
	GRAxis.drawGrid ( _da_graph, 2, x,
		_ylabels.length, _ylabels, GRAxis.GRID_SOLID );
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable {
	_duration_data = null;
	_graphics = null;
	__tslist = null;
	__left_tslist = null;	
	_tsproduct = null;
	_display_props = null;
	_background = null;
	_graph_JPopupMenu = null;
	_data_limits = null;
	_max_tslimits = null;
	_tslimits = null;
	_max_data_limits = null;
	_end_date = null;
	_start_date = null;
	_max_end_date = null;
	_max_start_date = null;
	_dev = null;
	_da_page = null;
	_da_maintitle =null;
	_da_subtitle = null;
	_da_topx_title = null;
	_da_topx_label = null;
	_da_lefty_title = null;
	_da_righty_title = null;
	_da_lefty_label = null;
	_da_graph = null;
	_da_righty_label = null;
	_da_bottomx_label = null;
	_da_bottomx_title = null;
	_da_bottom_legend = null;
	_da_left_legend = null;
	_da_right_legend = null;
	_datalim_page = null;
	_drawlim_page = null;
	_datalim_maintitle =null;
	_drawlim_maintitle =null;
	_datalim_subtitle = null;
	_drawlim_subtitle = null;
	_datalim_topx_title = null;
	_drawlim_topx_title = null;
	_datalim_topx_label = null;
	_drawlim_topx_label = null;
	_datalim_lefty_title = null;
	_drawlim_lefty_title = null;
	_datalim_righty_title = null;
	_drawlim_righty_title = null;
	_datalim_lefty_label = null;
	_drawlim_lefty_label = null;
	_drawlim_graph = null;
	_datalim_righty_label = null;
	_drawlim_righty_label = null;
	_datalim_bottomx_label = null;
	_drawlim_bottomx_label = null;
	_datalim_bottomx_title = null;
	_drawlim_bottomx_title = null;
	_datalim_bottom_legend = null;
	_drawlim_bottom_legend = null;
	_datalim_left_legend = null;
	_drawlim_left_legend = null;
	_datalim_right_legend = null;
	_drawlim_right_legend = null;
	_gtype = null;
	_regression_data = null;
	_xlabels = null;
	_ylabels = null;
	super.finalize();
}

/**
Format a data point for a tracker "X: xxxxx,  Y: yyyyy".
@param datapt Data point to format.
*/
public String formatMouseTrackerDataPoint ( GRPoint datapt )
{	if ( datapt == null ) {
		return "";
	}
	else if ((_graph_type == TSProduct.GRAPH_TYPE_DOUBLE_MASS) ||
		(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ||
		(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ){
		return "X:  " + StringUtil.formatString(datapt.x,"%.2f") +
			",  Y:  " + StringUtil.formatString(
			datapt.y,"%." + _lefty_precision + "f");
	}
	else {	DateTime mouse_date = new DateTime(datapt.x, true);
		mouse_date.setPrecision ( _xaxis_date_precision );
		if ( _bottomx_date_format > 0 ) {
			return "X:  " + mouse_date.toString(
			_bottomx_date_format) + ",  Y:  " +
			StringUtil.formatString(datapt.y,"%." +
			_lefty_precision + "f");
		}
		else {	return "X:  " + mouse_date.toString() + ",  Y:  " +
			StringUtil.formatString(datapt.y,"%." +
			_lefty_precision + "f");
		}
	}
}

/**
Return the data limits for the graph.  For a reference graph, this is the
zoomed data limits but not the overall data limits.  For a normal graph, the
limits from this method are the same as for the drawing area.
@return The current data limits.
*/
public GRLimits getDataLimits()
{	return _data_limits;
}

/**
Returns a Vector of all the time series that are enabled.  This will never 
return null.  If no time series are enabled, a new Vector will be returned.
@return a Vector of all the time series that are enabled.
*/
public Vector getEnabledTSList() {
	if (__tslist == null || __tslist.size() == 0) {
		return new Vector();
	}

	int size = __tslist.size();
	String propValue = null;
	Vector v = new Vector();
	for (int i = 0; i < size; i++) {
		propValue = _tsproduct.getLayeredPropValue("Enabled", _subproduct, i, false);
		if (propValue != null && propValue.equalsIgnoreCase("False")) {
			// skip it
		}
		else {
			v.add(__tslist.elementAt(i));
		}
	}
	return v;
}

/**
Returns the end date.
@return the end date.
*/
protected DateTime getEndDate() {
	return _end_date;
}

/**
Returns the first time series in the time series list that is enabled.
@return the first time series in the time series list that is enabled.
*/
public TS getFirstEnabledTS() {
	Vector v = getEnabledTSList();
	if (v.size() == 0) {
		return null;
	}
	return (TS)v.elementAt(0);
}

/**
Return the drawing area used for the graph.  The value may be null.
@return The drawing area used for the graph.
*/
public GRJComponentDrawingArea getGraphDrawingArea()
{	return _da_graph;
}

/**
Return the graph type (see TSProduct.GRAPH_TYPE_*).
@return the graph type.
*/
public int getGraphType ()
{	return _graph_type;
}

/**
Returns a prop value from the product, taking into account any override 
properties that may be set in the graph.  This method is used when drawing
time series.
@param key the key of the property to return.
@param subproduct the subproduct of the property to return
@param its the number of the time series of the property to return
@param annotation if true, then the property is an annotation
@param overrideProps if not null, this is the proplist that will be checked
for the property.
@return the prop value, or null if the property does not exist
*/
private String getLayeredPropValue(String key, int subproduct, int its,
boolean annotation, PropList overrideProps) {
	if (overrideProps == null) {
		return _tsproduct.getLayeredPropValue(
			key, subproduct, its, annotation);
	}
	else {
		String propValue = overrideProps.getValue(key);
		if (propValue == null) {
			return _tsproduct.getLayeredPropValue(
				key, subproduct, its, annotation);
		}
		else {
			return propValue;
		}
	}
}

/**
Return the legend string for a time series.
@return a legend string.  If null is returned, the legend should not be drawn.
@param ts Time series to get legend.
@param i Loop counter for time series (0-index).
*/
private String getLegendString ( TS ts, int i )
{	String legend = "";
	if ( (ts == null) || !ts.getEnabled() ) {
		// Null and disabled time series are not shown in the legend.
		// This is consistent with how the legend drawing area was set
		// up.
		return null;
	}
	// If there is a reference time series, indicate so in the
	// legend, but only if not printing...
	String reference_string = " (REF TS)";
	if (	(_graph_type == TSProduct.GRAPH_TYPE_DOUBLE_MASS) ||
		(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ||
		(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ||
		_dev.isPrinting() ||
		(getEnabledTSList().size() == 1) ) {
		// No reason to indicate reference time series in legend...
		reference_string = "";
	}

	// REVISIT (JTS - 2005-07-18)
	// in the above there should also be a check to see if the graph
	// is being saved to a file.  If so, the reference string should also
	// be ""
	
	// Subproduct legend format, which will provide a default if the time
	// series legend format is "Auto"...
	String subproduct_legend_format = _tsproduct.getLayeredPropValue(
					"LegendFormat", _subproduct, -1, false);
	// Determine the legend format for the specific time series.  If the
	// label is "Auto", define using the default (however, if Auto and the
	// subproduct is not auto, use the subproduct format).  If blank, don't
	// draw the legend...
	String legend_format = _tsproduct.getLayeredPropValue("LegendFormat",
		_subproduct, i, false );
	if ( legend_format == null ) {
		// Try the legend format for the subproduct...
		legend_format = subproduct_legend_format;
	}
	if (	(legend_format == null) ||
		(legend_format.length() == 0) ) {
		// Do not draw a legend.  Later might add a LegendEnabled, which
		// would totally turn off the legend (LegendFormat is really
		// more for the string label).
		return null;
	}
	else if ( !legend_format.equalsIgnoreCase("Auto") ) {
		// A specific legend has been specified...
		legend = ts.formatLegend ( legend_format );
	}
	// Below here "Auto" is in effect...
	else if ( (ts.getLegend() != null) && (ts.getLegend().length() != 0) ) {
		// The time series data itself has legend information so use
		// it...
		// SAMX
		// Should this even be allowed any more now that properties are
		// being used? - probably for applications that want more
		// control.
		legend = ts.formatLegend ( ts.getLegend() );
	}
	else if ( !subproduct_legend_format.equalsIgnoreCase("Auto") ) {
		// Use the subproduct legend...
		legend = ts.formatLegend ( subproduct_legend_format );
	}
	else {	// "Auto", format the legend manually...
		if ( _ignore_units && !ts.getDataUnits().equals("") ) {
			// Add units to legend because they won't be on the axis
			// label...
			if ( ts.getAlias().equals("") ) {
				legend = ts.getDescription() + ", " +
					ts.getIdentifierString() + ", " +
					ts.getDataUnits() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
			else {	legend = ts.getAlias() + " - " +
					ts.getDescription() + ", " +
					ts.getIdentifierString() + ", " +
					ts.getDataUnits() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
		}
		else {	// Don't put units in legend...
			if ( ts.getAlias().equals("") ) {
				legend = ts.getDescription() + ", " +
					ts.getIdentifierString() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
			else {	legend = ts.getAlias() + " - " +
					ts.getDescription() + ", " +
					ts.getIdentifierString() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
		}
	}
	if ( i == _reference_ts_index ) {
		legend += reference_string;
	}
	if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
		if ( i == 0 ) {
			legend = "Y (dependent): " + legend;
		}
	}
	else if ( _graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
		legend = (i + 1) + ") " + legend;
	}
	return legend;
}

/**
Return the maximum data limits.
*/
public GRLimits getMaxDataLimits ()
{	return _max_data_limits;
}

/**
Returns the max end date.
@return the max end date.
*/
protected DateTime getMaxEndDate() {
	return _max_end_date;
}

/**
Returns the max start date.
@return the max start date.
*/
protected DateTime getMaxStartDate() {
	return _max_start_date;
}

/**
Return the number of time series.
@return the number of time series (useful for automated selection of colors,
symbols, etc.)
*/
public int getNumTS ()
{	return __tslist.size();
}

/**
Return the drawing area used for the full page.  The value may be null.
@return The drawing area used for the full page.
*/
public GRJComponentDrawingArea getPageDrawingArea()
{	return _da_page;
}

/**
Return an instance of a PopupMenu that applies for the graph.  Because multiple
graphs can be drawn in a component, the PopupMenu needs to be specific to the
graph.  The PopupMenu can then be added to the and shown.
@return a PopupMenu instance appropriate for the graph, or null if no PopupMenu
choices are available (also return null if a reference graph).
*/
public JPopupMenu getJPopupMenu() {
	if (_is_reference_graph) {
		// currently no popups are available for reference graphs
		return null;
	}
	
	if (_graph_JPopupMenu != null) {
		// the menu was already created in a previous call and
		// can be returned 
		return _graph_JPopupMenu;
	}
	
	// The popups are not created by default because the graph may be
	// being created in batch mode, or the user made not use popups.
	_graph_JPopupMenu = new JPopupMenu("Graph");
	
	// Add properties specific to each graph type
	if (_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) {
		_graph_JPopupMenu.add(new SimpleJMenuItem(
			__MENU_ANALYSIS_DETAILS,__MENU_ANALYSIS_DETAILS,this));
		_graph_JPopupMenu.addSeparator();
	}
	
	// All graphs have properties
	_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_PROPERTIES,
		__MENU_PROPERTIES, this));

	// Add ability to set Y Maximum values
	_graph_JPopupMenu.addSeparator();

	if (_graph_type == TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
		_graph_JPopupMenu.add(new SimpleJMenuItem(
			__MENU_Y_MAXIMUM_VISIBLE, __MENU_Y_MAXIMUM_VISIBLE, 
			this));
		_graph_JPopupMenu.add(new SimpleJMenuItem(
			__MENU_Y_MINIMUM_VISIBLE, __MENU_Y_MINIMUM_VISIBLE, 
			this));
		_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_Y_MAXIMUM_AUTO,
			__MENU_Y_MAXIMUM_AUTO, this));
		_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_Y_MINIMUM_AUTO,
			__MENU_Y_MINIMUM_AUTO, this));
	}
	else {
		_graph_JPopupMenu.add(new SimpleJMenuItem(
			__MENU_Y_MAXIMUM_VISIBLE, __MENU_Y_MAXIMUM_VISIBLE, 
			this));
		_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_Y_MAXIMUM_AUTO,
			__MENU_Y_MAXIMUM_AUTO, this));
	}
	
	// If UNIX, add a refresh option to help with X server differences
	if (IOUtil.isUNIXMachine()) {
		_graph_JPopupMenu.addSeparator();
		_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_REFRESH, 
			__MENU_REFRESH, this));
	}
	return _graph_JPopupMenu;
}

/**
Return the Vector of TSRegression data that applies to the graph, if available.
This can be displayed in a details window for the graph.
@return TSRegression data for graph (use with scatter plot) or null if no
analysis has been performed.
*/
public Vector getRegressionData ()
{	return _regression_data;
}

/**
Returns the start date.
@return the start date.
*/
protected DateTime getStartDate() {
	return _start_date;
}

/**
Return the subproduct number for the graph.  This is used for example, by
TSGraphJComponent to get the ZoomGroup for a TSGraph when a zoom occurs
in a TSGraph.  Other TSGraphcan then be zoomed similarly.
*/
public int getSubProductNumber ()
{	return _subproduct;
}

/**
Return the time series list used for graphing.  Currently only the full list
is returned.
*/
public Vector getTSList()
{	return __tslist;
}

/**
Indicate whether the graph drawing area for this TSGraph contains the
device point that is specified.  This is used to determine whether a component
event should impact this TSGraph.
@param devpt a point of interest, in raw device units (not GR plotting units).
@return true if devpt is in the graph drawing area.
*/
public boolean graphContains ( GRPoint devpt )
{	// The check MUST be done in device units because more than one graph
	// may share the same device units...
	// be able to optimize this when there is time.
	return _da_graph.getPlotLimits(
		GRDrawingArea.COORD_DEVICE).contains ( devpt);
}

/**
Indicate whether units are being ignored on the axis.
*/
public boolean ignoreLeftYAxisUnits ()
{	return _ignore_units;
}

/**
Indicate whether the graph is for a reference graph.
*/
public boolean isReferenceGraph ()
{	return _is_reference_graph;
}

/**
Returns whether the time series of the graph's subproduct is enabled.
@param its the time series to check.
@return true if the time series is enabled, false if not.
*/
public boolean isTSEnabled(int its) {
	String propValue = _tsproduct.getLayeredPropValue("Enabled", 
		_subproduct, its, false);
	if (propValue != null && propValue.equalsIgnoreCase("False")) {
		return false;
	}
	return true;
}

/**
Open the drawing areas and set the drawing limits.  Data limits are initialized
to non-null values (and will be reset in setDataLimits()).
*/
private void openDrawingAreas ()
{	String prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisType", _subproduct, -1, false );
	boolean log_y = false;
	boolean log_xy_scatter = false;
	if ( prop_val.equalsIgnoreCase("Log") ) {
		log_y = true;
	}
	String prop_value = _tsproduct.getLayeredPropValue (
		"XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
		log_xy_scatter = true;
	}	
	// Full page...

	_da_page = new GRJComponentDrawingArea ( _dev, "TSGraph.Page",
			GRAspect.FILL, null, GRUnits.DEVICE,
			GRLimits.DEVICE, null );
	_datalim_page = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_page.setDataLimits ( _datalim_page );

	// Drawing area for main title...

	_da_maintitle = new GRJComponentDrawingArea ( _dev,
			"TSGraph.MainTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_maintitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_maintitle.setDataLimits ( _datalim_maintitle );

	// Drawing area for sub title...

	_da_subtitle = new GRJComponentDrawingArea ( _dev,
			"TSGraph.SubTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_subtitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_subtitle.setDataLimits ( _datalim_subtitle );

	// Top X axis...

	_da_topx_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.TopXTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_topx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_topx_title.setDataLimits ( _datalim_topx_title );

	_da_topx_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.TopXLabels", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_topx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_topx_label.setDataLimits ( _datalim_topx_label );
	if (log_xy_scatter) {
		// both axes are log
//		GRDrawingAreaUtil.setAxes(_da_topx_label, GRAxis.LOG,
//			GRAxis.LINEAR);
	}

	// Y axis titles...

	_da_lefty_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftYTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_lefty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_lefty_title.setDataLimits ( _datalim_lefty_title );

	_da_righty_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.RightYTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_righty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_righty_title.setDataLimits ( _datalim_righty_title );

	// Left Y axis labels...

	_da_lefty_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftYLabel", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_lefty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_lefty_label.setDataLimits ( _datalim_lefty_label );
	if (log_y) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_lefty_label, GRAxis.LINEAR,
			GRAxis.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(_da_lefty_label, GRAxis.LINEAR,
			GRAxis.LOG);
	}

	// Drawing area for graphing...

	_da_graph = new GRJComponentDrawingArea ( _dev,
			"TSGraph.Graphs",
			GRAspect.FILL, null, GRUnits.DEVICE,
			GRLimits.DEVICE, null );
	// Initial values that will be reset pretty quickly...
	GRLimits datalim_graph = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_graph.setDataLimits ( datalim_graph );

	if (log_y) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_graph, GRAxis.LINEAR,
			GRAxis.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(_da_graph, GRAxis.LINEAR,
			GRAxis.LINEAR);
	}

	// Right Y axis labels...

	_da_righty_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.RightYLabel", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_righty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_righty_label.setDataLimits ( _datalim_righty_label );
	if (log_y) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_righty_label, GRAxis.LINEAR,
			GRAxis.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(_da_righty_label, GRAxis.LINEAR,
			GRAxis.LOG);
	}

	// Drawing area for bottom X axis...

	_da_bottomx_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomXLabel",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_bottomx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );
	if (log_xy_scatter) {
		// both axes are log
//		GRDrawingAreaUtil.setAxes(_da_bottomx_label, GRAxis.LOG,
//			GRAxis.LINEAR);
	}

	_da_bottomx_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomXTitle",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_bottomx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottomx_title.setDataLimits ( _datalim_bottomx_title );

	// Legend (open drawing areas for each legend area, although currently
	// only one will be used)...

	_da_bottom_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomLegend",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_bottom_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottom_legend.setDataLimits ( _datalim_bottom_legend );

	_da_left_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftLegend",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_left_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_left_legend.setDataLimits ( _datalim_left_legend );

	_da_right_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.RightLegend",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_right_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_right_legend.setDataLimits ( _datalim_right_legend );

	_da_inside_legend = new GRJComponentDrawingArea(_dev,
		"TSGraph.InsideLegend", GRAspect.FILL, null,
		GRUnits.DEVICE, GRLimits.DEVICE, null);
	_datalim_inside_legend = new GRLimits(0.0, 0.0, 1.0, 1.0);
	_da_inside_legend.setDataLimits(_datalim_inside_legend);
}

/**
Update the TSGraph visible image.  For performance reasons, it is assumed
that the state of the GRDevice passed into the constructor will be consistent
with the Graphics that is being used (e.g., if the main device is printing then
the graphics will be a print graphics).  It is assumed that the GRDevice will
also control double buffering and the handling of the image and therefore if
this method is called, drawing must need to occur (because of a zoom or
component resize).  Therefore, the drawing limits are checked for each call.
For a reference graph component, this method should only be called for the
TSGraph that corresponds to the reference time series.
@param g Graphics instance to use for drawing.
*/
public void paint ( Graphics g )
{	String routine = "TSGraph.paint";

	if (g == null) {
		Message.printStatus(1, routine, "Null Graphics in paint()");
		return;
	}
	
	// Print some messages so we know what the paint is doing...

	if ( Message.isDebugOn ) {
		if ( _dev.isPrinting() ) {
			Message.printDebug ( 1, routine,
			_gtype + "Painting TSGraph for printing..." );
		}
		else if ( _is_reference_graph ) {
			Message.printDebug ( 1, routine, _gtype +
			"Painting reference graph..." );
		}
		else {	Message.printDebug ( 1,
			routine, _gtype + "Painting main graph..." );
		}
	}

	try { // Main try...

	// The following will be executed at initialization.  The code is here
	// because a valid Graphics is needed to check font sizes, etc.

	_graphics = g;

	// Now set the drawing limits based on the current size of the device.

	setDrawingLimits ( _drawlim_page );


	// If the graph type has changed, redo the data analysis.
	// Currently the Properties interface does not allow the graph type to
	// be changed.  However, XY Scatter parameters can be changed.

	boolean need_to_analyze = false;
	if ( _graph_type != _last_graph_type ) {
		need_to_analyze = true;
	}
	if ( _graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
		// Check the properties in the old regression results and see
		// if they differ from the current TSProduct properties.  If
		// they do, then reanalyze the data.  Assume for now that the
		// properties for the first regression apply to all regression
		// data...
// XJTSX		
		if ((_regression_data != null) && (_regression_data.size()>0)) {
			PropList old_props = ((TSRegression)
				_regression_data.elementAt(0)).getPropList();
			if (!_tsproduct.getLayeredPropValue(
				"XYScatterMethod", _subproduct, -1,
				false).equalsIgnoreCase(
				old_props.getValue("AnalysisMethod")) 
			  || !_tsproduct.getLayeredPropValue (
				"XYScatterMonth", _subproduct, -1,
				false).equalsIgnoreCase(
				old_props.getValue("AnalysisMonth")) 
		  	  || !_tsproduct.getLayeredPropValue (
				"XYScatterNumberOfEquations", _subproduct, -1,
				false ).equalsIgnoreCase(
				old_props.getValue("NumberOfEquations")) 
			  || !_tsproduct.getLayeredPropValue (
				"XYScatterTransformation", _subproduct, -1,
				false ).equalsIgnoreCase(
				old_props.getValue("Transformation")) ) {

				need_to_analyze = true;
			}
		}
	}
	if ( need_to_analyze ) {
		// Redo the analysis...
		doAnalysis();
		// This is the place where the reference graph has its data set.
	}
	_last_graph_type = _graph_type;

	// Compute the labels for the data, which will set the _datalim_graph,
	// which is used to set other data labels...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Computing labels..." );
	}
	if ( !_is_reference_graph ) {
		computeLabels ( _tslimits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Set initial data limits to " + _data_limits );
		}
	}

	if ( _is_reference_graph ) {
		// Fill in the background to gray...
		_da_graph.setColor ( GRColor.gray );
		GRDrawingAreaUtil.fillRectangle ( _da_graph,
			_max_data_limits.getLeftX(),
			_max_data_limits.getBottomY(),
			_max_data_limits.getWidth(),
			_max_data_limits.getHeight() );
		// Highlight current data (zoom) limits in white...
		_da_graph.setColor ( GRColor.white );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Data limits for reference box are " + _data_limits.toString() );
		}
		// Get the Y-dimension from the maximum values...
		GRDrawingAreaUtil.fillRectangle ( _da_graph,
			_data_limits.getLeftX(),
			_data_limits.getBottomY(),
			_data_limits.getWidth(),
			_max_data_limits.getHeight() );
		// Also draw a line in case we are zoomed in so far that the
		// rectangle does not draw...
		GRDrawingAreaUtil.drawLine ( _da_graph, _data_limits.getLeftX(),
			_data_limits.getBottomY(), _data_limits.getLeftX(),
			_data_limits.getTopY() );
		// The time series will graph over the background in the
		// following code.
	}

	// Now draw the graph(s)...

	// There are checks in each method to see whether
	// a reference graph should do anything.  This allows
	// some experimentation with reference graphs that
	// display some information.
	checkInternalProperties ();
	drawTitles ();
	drawAxesBack ();
	drawAnnotations(false);
	drawGraph ();
	drawAnnotations(true);
	drawAxesFront ();
	drawCurrentDateTime ();
	drawLegend ();
	// Uncomment this for development to help track down drawing problems...
	//if ( IOUtil.testing() ) {
	//	drawDrawingAreas ();
	//}
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine,
		_gtype + "Error drawing graph." );
		Message.printWarning ( 2, routine, e );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype +
		"...done painting TSGraph." );
	}
	routine = null;
}

/**
Set whether the start_date and end_date were set via the setEndDate and 
setStartDate methods and should be used for calculating the date limits.
*/
public void setComputeWithSetDates(boolean b) {
	__useSetDates = b;
}

/**
Reset the data limits and force a redraw.  For example call when a zoom event
occurs and tsViewZoom() is called.  If a reference graph, the overall limits
will remain the same but the box for the zoom location will move to the
specified limits.  For typical time series plots, the x-axis limits are the
floating point year and the y-axis are data values.  For scatter plots, the
y-axis limits are the data limits from the first time series and the x-axis
limits are the data limits from the second time series.
@param datalim_graph Data limits for the graph.  The data limits are either the
initial values or the values from a zoom.
*/
public void setDataLimits ( GRLimits datalim_graph )
{	if ( datalim_graph == null ) {
		return;
	}
	// JTS
	// exceptions thrown when trying to zoom
	if (_end_date == null && _start_date == null) {
		return;
	}
	// JTS
	if ( Message.isDebugOn ) {
		Message.printDebug(1, "setDataLimits",
			_gtype + "Setting [" +_subproduct + "] _data_limits to " + datalim_graph.toString());
	}

	if ( _is_reference_graph ) {
		// Save the new data limits for drawing but do not reset the
		// actual GRDrawingArea.  Also make sure the Y limits are the maximum...
		_data_limits = new GRLimits ( datalim_graph );
		_data_limits.setTopY ( _max_data_limits.getTopY() );
		_data_limits.setBottomY ( _max_data_limits.getBottomY() );
	}
	else {
	    // Do the full recalculation and zoom...
		// Need to recompute new start and end dates...
		// Make sure to keep the same date precision.

		_start_date = new DateTime ( datalim_graph.getLeftX(), true );
		_start_date.setPrecision ( _end_date.getPrecision() );
		_end_date = new DateTime ( datalim_graph.getRightX(), true );
		_end_date.setPrecision ( _start_date.getPrecision() );


		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setDataLimits",
			_gtype + "Set _start_date to " + _start_date + " _end_date to " + _end_date );
		}
		try {
		    // Recompute the limits, based on the period and data values...
			if (getEnabledTSList().size() == 0) {
				_tslimits = null;
				return;
			}
			else {
				_tslimits = TSUtil.getDataLimits( getEnabledTSList(), _start_date, _end_date, "", false, _ignore_units);

				if (_graph_type == TSProduct.GRAPH_TYPE_PERIOD){
					// Set the minimum value to 0 and 
					// the maximum value to one more than 
					// the number of time series.  Reverse 
					// the limits to number the same as 
					// the legend...
					_tslimits.setMaxValue(0.0);
					_tslimits.setMinValue( getEnabledTSList().size() + 1);
				}
				if (!_zoom_keep_y_limits) {
					// Keep the y limits to the maximum...
					_tslimits.setMinValue (	_max_tslimits.getMinValue() );
					_tslimits.setMaxValue (	_max_tslimits.getMaxValue() );
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, _gtype + "TSGraph", "Error getting dates for plot." );
			Message.printWarning ( 2, _gtype + "TSGraph", e );
			return;
		}
		// This will set _datalim_graph.  The Y limits are computed from
		// the max data limits.  The X limits are computed from _start_date and _end_date...
		if (getEnabledTSList().size() > 0) {
			computeLabels ( _tslimits );
			_da_graph.setDataLimits ( _data_limits );
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug(1, "setDataLimits", _gtype + "After reset, [" +_subproduct 
			+ "] _data_limits are " + datalim_graph );
	}
}

/**
Set the drawing limits for all drawing areas based on properties and window
size.  The drawing limits are all set to within the limits of the device limits
that are passed in (initially the limits from the GRJComponentDevice when the
TSGraph was constructed and later the limits from the GRJComponentDevice as it
resizes).  Axes are set to log if the properties indicate to do so.
@param drawlim_page Drawing limits for the full extent of the graph within a
GRJComponentDevice.
*/
public void setDrawingLimits ( GRLimits drawlim_page )
{	double buffer = 2.0;	// Buffer around drawing areas (helps separate
				// things and also makes it easier to see
				// drawing areas when in debug mode
	String routine = "TSGraph.setDrawingLimits";

	boolean log_y = false;
	boolean log_xy_scatter = false;
	String prop_value = _tsproduct.getLayeredPropValue (
			"LeftYAxisType", _subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y = true;
	}
	prop_value = _tsproduct.getLayeredPropValue (
		"XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
// JTS - 2005-09-07 -- this isn't working right now when the axis types below
// are set
//		log_xy_scatter = true;
	}	

	// Figure out dimensions up front...

	// Drawing areas will have zero size if nothing is drawn in them...

	double maintitle_height = 0.0;
	String maintitle_string = _tsproduct.getLayeredPropValue (
				"MainTitleString", _subproduct, -1, false );
	if ( (maintitle_string != null) && !maintitle_string.equals("") ) {
		// Get the text extents and set the height based on that...
		String maintitle_font = _tsproduct.getLayeredPropValue (
				"MainTitleFontName", _subproduct, -1, false );
		String maintitle_fontsize = _tsproduct.getLayeredPropValue (
				"MainTitleFontSize", _subproduct, -1, false );
		String maintitle_fontstyle = _tsproduct.getLayeredPropValue (
				"MainTitleFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font,
				maintitle_fontstyle,
				StringUtil.atod(maintitle_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_maintitle, maintitle_string,
				GRUnits.DEVICE );
		maintitle_height = text_limits.getHeight();
		text_limits = null;
	}

	double subtitle_height = 0.0;
	String subtitle_string = _tsproduct.getLayeredPropValue (
				"SubTitleString", _subproduct, -1, false );
	if ( (subtitle_string != null) && !subtitle_string.equals("") ) {
		// Get the text extents and set the height based on that...
		String subtitle_font = _tsproduct.getLayeredPropValue (
				"SubTitleFontName", _subproduct, -1, false );
		String subtitle_fontsize = _tsproduct.getLayeredPropValue (
				"SubTitleFontSize", _subproduct, -1, false );
		String subtitle_fontstyle = _tsproduct.getLayeredPropValue (
				"SubTitleFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font,
				subtitle_fontstyle,
				StringUtil.atod(subtitle_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_subtitle, subtitle_string,
				GRUnits.DEVICE );
		subtitle_height = text_limits.getHeight();
		text_limits = null;
	}

	// For now, hard-code the y-axis widths.  This has been done for some
	// time so it should be OK.  Later need to do more intelligently by
	// checking labels for the maximum values (SAMX - get max *10 and
	// compute label width so we don't have to rely on full label
	// determination?).

	double lefty_label_width = 80.0;	// Height set to graph height
	double righty_label_width = 30.0;	// Height set to graph height

	double lefty_title_height = 0.0;
	double lefty_title_width = 0.0;
	String lefty_title_string = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleString", _subproduct, -1, false );
	if ( (lefty_title_string != null) && !lefty_title_string.equals("") ){
		// Get the text extents and set the height based on that...
		String lefty_title_font = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontName", _subproduct, -1, false );
		String lefty_title_fontsize = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontSize", _subproduct, -1, false );
		String lefty_title_fontstyle = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_lefty_title, lefty_title_font,
			lefty_title_fontstyle,
			StringUtil.atod(lefty_title_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_lefty_title, lefty_title_string,
				GRUnits.DEVICE );
		lefty_title_height = text_limits.getHeight();
		lefty_title_width = text_limits.getWidth();
		text_limits = null;
	}

	double righty_title_width = 0.0;
	double righty_title_height = 0.0;

	// Determinination X axis titles.

	double topx_title_height = 0.0;
	String topx_title_string = _tsproduct.getLayeredPropValue (
			"TopXAxisTitleString", _subproduct, -1, false );
	if ( (topx_title_string != null) && !topx_title_string.equals("") ){
		// Get the text extents and set the height based on that...
		String topx_title_font = _tsproduct.getLayeredPropValue (
			"TopXAxisTitleFontName", _subproduct, -1, false );
		String topx_title_fontsize = _tsproduct.getLayeredPropValue (
			"TopXAxisTitleFontSize", _subproduct, -1, false );
		String topx_title_fontstyle = _tsproduct.getLayeredPropValue (
			"TopXAxisTitleFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_topx_title, topx_title_font,
			topx_title_fontstyle,
			StringUtil.atod(topx_title_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_topx_title, topx_title_string,
				GRUnits.DEVICE );
		topx_title_height = text_limits.getHeight();
		text_limits = null;
	}

	double bottomx_title_height = 0.0;
	String bottomx_title_string = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleString", _subproduct, -1, false );
	if (	(bottomx_title_string != null) &&
		!bottomx_title_string.equals("") ){
		// Get the text extents and set the height based on that...
		String bottomx_title_font = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontName", _subproduct, -1, false );
		String bottomx_title_fontsize = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontSize", _subproduct, -1, false );
		String bottomx_title_fontstyle =_tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_bottomx_title,
			bottomx_title_font, bottomx_title_fontstyle,
			StringUtil.atod(bottomx_title_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_bottomx_title, bottomx_title_string,
				GRUnits.DEVICE );
		bottomx_title_height = text_limits.getHeight();
		text_limits = null;
	}

	double bottomx_label_height = 0.0;
	String bottomx_label_font = _tsproduct.getLayeredPropValue (
		"BottomXAxisLabelFontName", _subproduct, -1, false );
	String bottomx_label_fontsize = _tsproduct.getLayeredPropValue (
		"BottomXAxisLabelFontSize", _subproduct, -1, false );
	String bottomx_label_fontstyle = _tsproduct.getLayeredPropValue (
		"BottomXAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, bottomx_label_font,
		bottomx_label_fontstyle,
		StringUtil.atod(bottomx_label_fontsize) );
	GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
			_da_bottomx_label, "A string",
			GRUnits.DEVICE );
	if (	(_graph_type == TSProduct.GRAPH_TYPE_DURATION) ||
		(_graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ) {
		bottomx_label_height = text_limits.getHeight();
	}
	else {	// For X labels - leave room for two rows of labels for dates.
		bottomx_label_height = 2*text_limits.getHeight();
	}
	text_limits = null;

	// Make an initial determination of the legend height and width, based
	// on the font height and string width.  The dynamic nature of the plot
	// size really should only impact how many legend items are shown.  Try
	// to limit to a reasonable number.  This logic works for any legend
	// position.

	String legend_position = _tsproduct.getLayeredPropValue (
				"LegendPosition", _subproduct, -1, false );

	double legend_height = 0.0;
	double legend_width = 0.0;
	if ((__tslist == null) || (__tslist.size() == 0) 
	    || getEnabledTSList().size() == 0 
	    || legend_position.equalsIgnoreCase("None")) {
		// Default to no legend...
		legend_height = 0.0;
		legend_width = 0.0;
	}
	else {	
		// The legend height is based on the legend font size.
		// = size*(nts + 1), with the buffer, where nts is the
		// number of enabled, non-null time series.
		// The legend properties are for the subproduct.
		String legend_font = _tsproduct.getLayeredPropValue (
				"LegendFontName", _subproduct, -1, false );
		String legend_fontsize = _tsproduct.getLayeredPropValue (
				"LegendFontSize", _subproduct, -1, false );
		String legend_fontstyle = _tsproduct.getLayeredPropValue (
				"LegendFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_bottom_legend, legend_font,
				legend_fontstyle,
				StringUtil.atod(legend_fontsize) );
		int nts = 0;
		int size = __tslist.size();
		TS ts = null;
		String legend = null;

//////////////////////////////////////////////////////
// two indents removed for legibility
for ( int i = 0; i < size; i++ ) {
	ts = (TS)__tslist.elementAt(i);
	if (ts == null || !isTSEnabled(i)) {
		continue;
	}
	if ( ts.getEnabled() ) {
		if (_graph_type 
    			== TSProduct.GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL) {
			if (i == 0) {
				// ignore the zeroth
				continue;
			}
			// The time series will be plotted and will be
			// shown in the legend...
			legend = getLegendString(ts, i)
				+ " (Residual)";
			if (legend != null) {
				text_limits =
					GRDrawingAreaUtil.getTextExtents(
					_da_bottom_legend, legend,
					GRUnits.DEVICE);
				legend_width = MathUtil.max(
					legend_width, text_limits.getWidth());
			}
			++nts;			
		}
		else if (_graph_type 
    			== TSProduct.GRAPH_TYPE_PREDICTED_VALUE) {
			// The time series will be plotted and will be
			// shown in the legend...
			legend = getLegendString(ts, i);
			if (legend != null) {
				text_limits =
					GRDrawingAreaUtil.getTextExtents(
					_da_bottom_legend, legend,
					GRUnits.DEVICE);
				legend_width = MathUtil.max(
					legend_width, text_limits.getWidth());
			}
			++nts;
			if (i > 0) {
				// add the predicted time series, too
				legend = getLegendString(ts, i)
					+ " (Predicted)";
				if (legend != null) {
					text_limits =
						GRDrawingAreaUtil
							.getTextExtents(
							_da_bottom_legend, 
							legend,
							GRUnits.DEVICE);
					legend_width = MathUtil.max(
						legend_width, 
						text_limits.getWidth());
				}
				++nts;				
			}
		}
		else {
			// The time series will be plotted and will be
			// shown in the legend...
			legend = getLegendString(ts, i);
			if (legend != null) {
				text_limits =
					GRDrawingAreaUtil.getTextExtents(
					_da_bottom_legend, legend,
					GRUnits.DEVICE);
				legend_width = MathUtil.max(
					legend_width, text_limits.getWidth());
			}
			++nts;
		}
	}
}
// two indents removed from the above for legibility
////////////////////////////////////////////////////
		ts = null;
		// Estimate the overall height...
		text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_bottom_legend, "TEST STRING",
				GRUnits.DEVICE );
		legend_height = nts*text_limits.getHeight();
		// The legend width is increased by the width of the symbol
		// (currently always 25 pixels)...
		legend_width += 25;
		text_limits = null;
	}

	// Drawing limits for the page are set first...

	_drawlim_page = new GRLimits ( drawlim_page );
	
	// Do a check on the graph height and adjust some of the other heights
	// if necessary (enhance this over time)...

	// For legend position "Top" and "Bottom", limit the legend to 1/2 the
	// page (this can be an issue when traces or many time series are drawn
	// - in this case the legend position should probably be set to "Left"
	// or "Right" and labeled with the sequnce number, scenario, or some
	// other short string.  Use the page DA for checks because SAM's too
	// lazy to do all the other checks here to find out what half the graph
	// height would be, with buffers.  Currently only one legend is
	// supported but to simplify the logic below transfer the generic
	// "legend_height" and "legend_width" variables to specific variables.

	if (	legend_position.equalsIgnoreCase("Bottom") ||
		legend_position.equalsIgnoreCase("Top") ) {
		if (	legend_height > _drawlim_page.getHeight()*.5 ) {
			legend_height = _drawlim_page.getHeight()*.5;
		}
	}

	// Compute specific legend height and width values for each legend
	// position...

	double bottom_legend_height = 0.0;
	double left_legend_width = 0.0;
	double left_legend_buffer = 0.0;
	double right_legend_width = 0.0;
	double right_legend_buffer = 0.0;
	double inside_legend_height = 0.0;
	double inside_legend_width = 0.0;
	double inside_legend_buffer = 0.0;
	
	if ( legend_position.equalsIgnoreCase("Bottom") ) {
		bottom_legend_height = legend_height;
	}
	else if ( legend_position.equalsIgnoreCase("Left") ) {
		left_legend_width = legend_width;
		left_legend_buffer = buffer;
	}
	else if ( legend_position.equalsIgnoreCase("Right") ) {
		right_legend_width = legend_width;
		right_legend_buffer = buffer;
	}
	else if ( legend_position.equalsIgnoreCase("Top") ) {
	}
	else if (StringUtil.startsWithIgnoreCase(legend_position, "Inside")) {
		inside_legend_height = legend_height;
		inside_legend_width = legend_width;
		inside_legend_buffer = buffer * 4;
	}
	
	// Set the drawing limits based on what was determined above...

	// Graph main title is only impacted by overall (page) limits...

	if ( maintitle_height == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_maintitle = new GRLimits (
			(drawlim_page.getLeftX() + buffer),
			drawlim_page.getTopY(),
			(drawlim_page.getRightX() - buffer),
			drawlim_page.getTopY() );
	}
	else {	_drawlim_maintitle = new GRLimits (
			(drawlim_page.getLeftX() + buffer),
			(drawlim_page.getTopY() - buffer - maintitle_height),
			(drawlim_page.getRightX() - buffer),
			(drawlim_page.getTopY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Main title drawing limits are: " +
		_drawlim_maintitle.toString() );
	}

	// Graph subtitle is only impacted by page limits and main title...

	if ( subtitle_height == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_subtitle = new GRLimits (
			(drawlim_page.getLeftX() + buffer),
			_drawlim_maintitle.getBottomY(),
			(drawlim_page.getRightX() - buffer),
			_drawlim_maintitle.getBottomY() );
	}
	else {	_drawlim_subtitle = new GRLimits (
			(drawlim_page.getLeftX() + buffer),
			(_drawlim_maintitle.getBottomY() - buffer -
			subtitle_height),
			(drawlim_page.getRightX() - buffer),
			(_drawlim_maintitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1,  routine,
		_gtype + "Sub title drawing limits are: " +
		_drawlim_subtitle.toString() );
	}

	// Top legend is impacted by page limits and position of subtitle...

	// Currently top legend is not enabled...

	// Top X axis title is impacted by the left and right legends and the
	// position of the subtitle.

	if ( topx_title_height == 0.0 ) {
		// Zero height drawing area place holder at same height as
		// the bottom of the subtitle...
		_drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer),
			_drawlim_subtitle.getBottomY(),
			(drawlim_page.getRightX() - buffer -
			right_legend_width - right_legend_buffer -
			righty_label_width - buffer),
			_drawlim_subtitle.getBottomY() );
	}
	else {	_drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer),
			_drawlim_subtitle.getBottomY() - buffer -
			topx_title_height,
			(drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer -
			righty_label_width - buffer),
			_drawlim_subtitle.getBottomY() - buffer );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Top X title drawing limits are: " +
		_drawlim_topx_title.toString() );
	}

	// Top x labels are impacted by left and right legends and the position
	// of the top x title.  Top x labels are not currently processed (until
	// we can move the Y axis titles out of the way)...

	if ( lefty_title_height == 0.0 ) {
		// Zero height drawing area place holder at same height as
		// bottom of top x title...
		_drawlim_lefty_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer - lefty_title_width/2.0),
			_drawlim_topx_title.getBottomY(),
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer + lefty_title_width/2.0),
			_drawlim_topx_title.getBottomY() );
	}
	else {	_drawlim_lefty_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer - lefty_title_width/2.0),
			(_drawlim_topx_title.getBottomY() - buffer -
			lefty_title_height),
			(drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer +
			lefty_label_width + buffer + lefty_title_width/2.0),
			_drawlim_topx_title.getBottomY() - buffer );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Left y title drawing limits are: " +
		_drawlim_lefty_title.toString() );
	}

	// Left y labels are impacted by left and right legends and position
	// of the y axis titles.
	// Left y labels are always present.  Even if zero width, use buffer
	// because other code does below...

	double y = drawlim_page.getBottomY();
	if ( bottom_legend_height > 0.0 ) {
		y += (buffer + bottom_legend_height);
	}
	if ( bottomx_title_height > 0.0 ) {
		y += (buffer + bottomx_title_height);
	}
	else {	y += (2.0 * buffer);	// Bottomx title is always at least
					// buffer in actuality.
	}
	if ( bottomx_label_height > 0.0 ) {
		y += (buffer + bottomx_label_height);
	}
	y += buffer;
	_drawlim_lefty_label = new GRLimits (
			(_drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer),
			y,
			(_drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width + buffer + lefty_label_width),
			(_drawlim_lefty_title.getBottomY() - buffer) );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Left Y label drawing limits are: " +
		_drawlim_lefty_label.toString() );
	}

	// Right y axis labels...

	_drawlim_righty_label = new GRLimits (
			(_drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer - righty_label_width),
			y,
			(_drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer),
			(_drawlim_lefty_title.getBottomY() - buffer) );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Right Y label drawing limits are: " +
		_drawlim_righty_label.toString() );
	}

	// The right y title is assumed to be similar to the left y title (no
	// checks are made yet to make sure).  Use the left y axis for further
	// vertical positioning below.

	if ( righty_title_height == 0.0 ) {
		// Zero height drawing area place holder at same height as
		// bottom of top x title...
		_drawlim_righty_title = new GRLimits (
			(drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer -
			righty_label_width - buffer - righty_title_width/2.0),
			_drawlim_topx_title.getBottomY(),
			(drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer -
			righty_label_width - buffer + righty_title_width/2.0),
			_drawlim_topx_title.getBottomY() );
	}
	else {	_drawlim_righty_title = new GRLimits (
			(drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer -
			righty_label_width - buffer - righty_title_width/2.0),
			(_drawlim_topx_title.getBottomY() - buffer -
			righty_title_height),
			(drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width - buffer -
			righty_label_width - buffer + righty_title_width/2.0),
			(_drawlim_topx_title.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Top X title drawing limits are: " +
		_drawlim_righty_title.toString() );
	}

	// ...Skip the graph area for now because it will be the remainder...
	// ...see below for its definition...

	// Now work up from the bottom...
	// Drawing limits for the bottom legend (always independent of other
	// legends)...

	if ( bottom_legend_height == 0.0 ) {
		// Make zero-height same Y as the bottom of the page area...
		_drawlim_bottom_legend = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			_drawlim_page.getBottomY(),
			(_drawlim_page.getRightX() - buffer),
			_drawlim_page.getBottomY());
	}
	else {	_drawlim_bottom_legend = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			(_drawlim_page.getBottomY() + buffer),
			(_drawlim_page.getRightX() - buffer),
			(_drawlim_page.getBottomY() + buffer +
			bottom_legend_height) );
	}
	// Set the data limits for the legend to use device units...
	_datalim_bottom_legend = new GRLimits ( 0.0, 0.0,
			_drawlim_bottom_legend.getWidth(),
			_drawlim_bottom_legend.getHeight() );

	// The position of the bottom X axis title and labels is impacted by
	// left and right legends and the position of the bottom legend.
	// Bottom X axis title and labels - work up from the bottom legend...
	// For the bottomx title, add a little space around so it looks
	// better, even if no title is given.

	if ( bottomx_title_height == 0.0 ) {
		// Make zero-height same Y as the top of the legend area...
		_drawlim_bottomx_title = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer),
			(_drawlim_bottom_legend.getTopY() + buffer),
			(_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_bottom_legend.getTopY() + buffer + buffer) );
	}
	else {	_drawlim_bottomx_title = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer),
			(_drawlim_bottom_legend.getTopY() + buffer),
			(_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_bottom_legend.getTopY() + buffer +
			bottomx_title_height + buffer));
	}

	if ( bottomx_label_height == 0.0 ) {
		// Make zero-height same Y as the top of the X title...
		_drawlim_bottomx_label = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer),
			_drawlim_bottomx_title.getTopY(),
			(_drawlim_righty_label.getLeftX() - buffer),
			_drawlim_bottomx_title.getTopY());
	}
	else {	_drawlim_bottomx_label = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer),
			(_drawlim_bottomx_title.getTopY() + buffer),
			(_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_bottomx_title.getTopY() + buffer +
			bottomx_label_height));
	}

	// Graph drawing area (always what is left)...

	if ( _is_reference_graph ) {
		_drawlim_graph = new GRLimits (
			drawlim_page.getLeftX(),
			drawlim_page.getBottomY(),
			drawlim_page.getRightX(),
			drawlim_page.getTopY() );
	}
	else {	
		_drawlim_graph = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer),
			(_drawlim_bottomx_label.getTopY() + buffer),
			(_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_lefty_title.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		_gtype + "Graph drawing limits are: " +
		_drawlim_graph.toString() );
	}

	// If the legend is on the left or right, define the drawing area now
	// because it is impacted by the graph drawing area (typically the
	// left and right legends will draw down the side until the bottom of
	// the graph (this could be a problem if the graph ends - fix by not
	// drawing when negative y-coordinates are found for data).

	if ( legend_position.equalsIgnoreCase("Left") ) {
		_drawlim_left_legend = new GRLimits (
			(_drawlim_page.getLeftX() + left_legend_buffer),
			_drawlim_graph.getBottomY(),
			(_drawlim_page.getLeftX() + left_legend_buffer +
			left_legend_width),
			_drawlim_graph.getTopY() );
	}
	else {	// Set to zero width...
		_drawlim_left_legend = new GRLimits (
			_drawlim_page.getLeftX(),
			_drawlim_graph.getBottomY(),
			_drawlim_page.getLeftX(),
			_drawlim_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units...
	_datalim_left_legend = new GRLimits ( 0.0, 0.0,
		_drawlim_left_legend.getWidth(),
		_drawlim_left_legend.getHeight() );
	if ( legend_position.equalsIgnoreCase("Right") ) {
		_drawlim_right_legend = new GRLimits (
			(_drawlim_page.getRightX() - right_legend_buffer -
			right_legend_width),
			_drawlim_graph.getBottomY(),
			(_drawlim_page.getRightX() - right_legend_buffer),
			_drawlim_graph.getTopY() );
	}
	else {	// Set to zero width...
		_drawlim_right_legend = new GRLimits (
			_drawlim_page.getRightX(),
			_drawlim_graph.getBottomY(),
			_drawlim_page.getRightX(),
			_drawlim_graph.getTopY() );
	}

	if (legend_position.equalsIgnoreCase("InsideUpperLeft")) {
		_drawlim_inside_legend = new GRLimits(
			_drawlim_graph.getLeftX() + inside_legend_buffer,
			_drawlim_graph.getTopY() - inside_legend_buffer
				- inside_legend_height,
			_drawlim_graph.getLeftX() + inside_legend_buffer
				+ inside_legend_width,
			_drawlim_graph.getTopY() - inside_legend_buffer);
	}
	else if (legend_position.equalsIgnoreCase("InsideUpperRight")) {
		_drawlim_inside_legend = new GRLimits(
			_drawlim_graph.getRightX() - inside_legend_buffer
				- inside_legend_width,
			_drawlim_graph.getTopY() - inside_legend_buffer
				- inside_legend_height,
			_drawlim_graph.getRightX() - inside_legend_buffer,
			_drawlim_graph.getTopY() - inside_legend_buffer);
	}
	else if (legend_position.equalsIgnoreCase("InsideLowerLeft")) {
		_drawlim_inside_legend = new GRLimits(
			_drawlim_graph.getLeftX() + inside_legend_buffer,
			_drawlim_graph.getBottomY() + inside_legend_buffer,
			_drawlim_graph.getLeftX() + inside_legend_buffer
				+ inside_legend_width,
			_drawlim_graph.getBottomY() + inside_legend_buffer
				+ inside_legend_height);
	}
	else if (legend_position.equalsIgnoreCase("InsideLowerRight")) {
		_drawlim_inside_legend = new GRLimits(
			_drawlim_graph.getRightX() - inside_legend_buffer
				- inside_legend_width,
			_drawlim_graph.getBottomY() + inside_legend_buffer,
			_drawlim_graph.getRightX() - inside_legend_buffer,
			_drawlim_graph.getBottomY() + inside_legend_buffer
				+ inside_legend_height);
	}
	
	else {
		_drawlim_inside_legend = new GRLimits(
			_drawlim_page.getLeftX(),
			_drawlim_page.getBottomY(),
			_drawlim_page.getRightX(),
			_drawlim_page.getTopY());
	}
	_datalim_inside_legend = new GRLimits(0.0, 0.0,
		_drawlim_inside_legend.getWidth(),
		_drawlim_inside_legend.getHeight());
	
	// Set the data limits for the legend to use device units...
	_datalim_right_legend = new GRLimits ( 0.0, 0.0,
		_drawlim_right_legend.getWidth(),
		_drawlim_right_legend.getHeight() );

	// Now set in the drawing areas...

	if ( (_da_page != null) && (_drawlim_page != null) ) {
		// _drawlim_page is set in the constructor - we just need to
		// use it as is...
		_da_page.setDrawingLimits ( _drawlim_page,
			GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_maintitle != null) && (_drawlim_maintitle != null) ) {
		_da_maintitle.setDrawingLimits ( _drawlim_maintitle,
			GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_subtitle != null) && (_drawlim_subtitle != null) ) {
		_da_subtitle.setDrawingLimits ( _drawlim_subtitle,
			GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_topx_title != null) && (_drawlim_topx_title != null) ) {
		_da_topx_title.setDrawingLimits (
			_drawlim_topx_title, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_topx_title.setDataLimits ( _datalim_topx_title );
	}
	if ( (_da_topx_label != null) && (_drawlim_topx_label != null) ) {
		_da_topx_label.setDrawingLimits (
			_drawlim_topx_label, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_topx_label.setDataLimits ( _datalim_topx_label );
		if (log_xy_scatter) {
			GRDrawingAreaUtil.setAxes(_da_topx_label,
				GRAxis.LOG, GRAxis.LINEAR);
		}				
	}
	if ( (_da_lefty_title != null) && (_drawlim_lefty_title != null) ) {
		_da_lefty_title.setDrawingLimits (
			_drawlim_lefty_title, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_lefty_title.setDataLimits ( _datalim_lefty_title );
	}
	if ( (_da_lefty_label != null) && (_drawlim_lefty_label != null) ) {
		_da_lefty_label.setDrawingLimits (
			_drawlim_lefty_label, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_lefty_label.setDataLimits ( _datalim_lefty_label );
		if (log_y || log_xy_scatter) {
			GRDrawingAreaUtil.setAxes(_da_lefty_label,
				GRAxis.LINEAR, GRAxis.LOG);
		}
	}
	if ( (_da_righty_title != null) && (_drawlim_righty_title != null) ) {
		_da_righty_title.setDrawingLimits (
			_drawlim_righty_title, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_righty_title.setDataLimits ( _datalim_righty_title );
	}
	if ( (_da_righty_label != null) && (_drawlim_righty_label != null) ) {
		_da_righty_label.setDrawingLimits (
			_drawlim_righty_label, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_righty_label.setDataLimits ( _datalim_righty_label );
		if (log_y || log_xy_scatter) {
			GRDrawingAreaUtil.setAxes(_da_righty_label,
				GRAxis.LINEAR, GRAxis.LOG);
		}
	}
	if ( (_da_graph != null) && (_drawlim_graph != null) ) {
		_da_graph.setDrawingLimits ( _drawlim_graph, GRUnits.DEVICE,
			GRLimits.DEVICE );
		if (log_y) {
			GRDrawingAreaUtil.setAxes(_da_graph, GRAxis.LINEAR,
				GRAxis.LOG);
		}
		else if (log_xy_scatter) {
			GRDrawingAreaUtil.setAxes(_da_graph, GRAxis.LOG,
				GRAxis.LOG);
		}
	}
	if ( (_da_bottomx_label != null) && (_drawlim_bottomx_label != null) ) {
		_da_bottomx_label.setDrawingLimits (
			_drawlim_bottomx_label, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );
		if (log_xy_scatter) {
			GRDrawingAreaUtil.setAxes(_da_bottomx_label,
				GRAxis.LOG, GRAxis.LINEAR);
		}		
	}
	if ( (_da_bottomx_title != null) && (_drawlim_bottomx_title != null) ) {
		_da_bottomx_title.setDrawingLimits (
			_drawlim_bottomx_title, GRUnits.DEVICE,
			GRLimits.DEVICE );
		_da_bottomx_title.setDataLimits ( _datalim_bottomx_title );
	}
	if ( (_da_bottom_legend != null) && (_drawlim_bottom_legend != null) ) {
		_da_bottom_legend.setDrawingLimits ( _drawlim_bottom_legend,
			GRUnits.DEVICE, GRLimits.DEVICE );
		_da_bottom_legend.setDataLimits ( _datalim_bottom_legend );
	}
	if ( (_da_left_legend != null) && (_drawlim_left_legend != null) ) {
		_da_left_legend.setDrawingLimits ( _drawlim_left_legend,
			GRUnits.DEVICE, GRLimits.DEVICE );
		_da_left_legend.setDataLimits ( _datalim_left_legend );
	}
	if ( (_da_right_legend != null) && (_drawlim_right_legend != null) ) {
		_da_right_legend.setDrawingLimits ( _drawlim_right_legend,
			GRUnits.DEVICE, GRLimits.DEVICE );
		_da_right_legend.setDataLimits ( _datalim_right_legend );
	}
	if ((_da_inside_legend != null) && (_drawlim_inside_legend != null)) {
		_da_inside_legend.setDrawingLimits(_drawlim_inside_legend,
			GRUnits.DEVICE, GRLimits.DEVICE);
		_da_inside_legend.setDataLimits(_datalim_inside_legend);
	}
}

/**
Sets the end date.
@param endDate value to set the end date to.
*/
protected void setEndDate(DateTime endDate) {
	_end_date = endDate;
}

/**
Sets the maximum end date.
@param d the end date.
*/
protected void setMaxEndDate(DateTime d) {	
	_max_end_date = new DateTime(d);
}

/**
Sets the maximum start date.
@param d the start date.
*/
protected void setMaxStartDate(DateTime d) {
	_max_start_date = new DateTime(d);
}

/**
Sets the start date.
@param startDate value to set the start date to.
*/
protected void setStartDate(DateTime startDate) {
	_start_date = startDate;
}

// REVISIT SAM 2006-10-01 Move this to TSUtil or similar for general use.
/**
Deterine the nearest DateTime equal to or before the candidate DateTime,
typically in order to determine an iterator start DateTime that is compatible
with the specific time series.  For example, when graphing time series with
data having different intervals, the graphing software will determine the
visible window using a DateTime range with a precision that matches the smallest
data interval.  Time series that use dates that are less precise may be able to
simply reset the more precise DateTime to a lower precision.  However, it may
also be necessary for calling code to decrement the returned DateTime by an
interval to make sure that a complete overlapping period is considered during
iteration.

Time series that use higher precision DateTimes also need to check the candidate
DateTime for offsets.  For example, the candidate DateTime may have a precision
of DateTime.PRECISION_HOUR and an hour of 6.  The time series being checked may
be a 6-hour time series but observatins are recorded at 0300, 0900, 1500, 2100.
In this case, the first DateTime in the time series before the candidate must be
chosen, regardless of whether the candidate's hour aligns with the time series
data.
@param candidate_DateTime The candidate DateTime, which may have a precision
greater than or less than those used by the indicated time series.
@param ts Time series to examine, which may be a regular or irregular time
series.
@return DateTime matching the precision of DateTimes used in the specified time
series that is equal to or less than the candidate DateTime.  The returned
DateTime will align with the time series data and may NOT align evenly with the
candidate DateTime.
*/
public static DateTime getNearestDateTimeLessThanOrEqualTo (
					DateTime candidate_DateTime,
					TS ts )
{
	
	DateTime returnDate = new DateTime(candidate_DateTime);
	returnDate.round(-1, ts.getDataIntervalBase(), ts.getDataIntervalMult());
	
	// Compute the offset from exact interval breaks for this time series...
	DateTime start_rounded = new DateTime(ts.getDate1());
	start_rounded.round(-1, ts.getDataIntervalBase(),
		ts.getDataIntervalMult());
	DateTime start_not_rounded = new DateTime(ts.getDate1());
	DateTime diff = null;
	try {
		diff = new DateTime(TimeUtil.diff(start_not_rounded,
			start_rounded));
	}
	catch ( Exception e ) {
		// REVISIT SAM 2006-09-28
		// Need to handle?
	}

	// add the offset to the rounded base time if there is one
	if(!(start_not_rounded.equals(start_rounded)))
		returnDate.add(diff);	

	// set precision for the time series
	returnDate.setPrecision(ts.getDate1().getPrecision());	
	
	return returnDate;
}

}
