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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JPopupMenu;

import RTi.GR.GRAspect;
import RTi.GR.GRAxis;
import RTi.GR.GRAxisDirectionType;
import RTi.GR.GRColor;
import RTi.GR.GRColorTable;
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
import RTi.TS.TSIterator;
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
import RTi.Util.Math.DataTransformationType;
import RTi.Util.Math.FDistribution;
import RTi.Util.Math.MathUtil;
import RTi.Util.Math.NumberOfEquationsType;
import RTi.Util.Math.RegressionType;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTableList_JFrame;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
The TSGraph class manages the drawing areas for displaying one or more time
series in a single graph.   The drawing areas are set up by specifying a
GRJComponentDevice and information about how much of the device should be used
for this graph.  Drawing properties are retrieved from a TSProduct, where this
graph is identified as a sub-product of the entire product.
This class also implements TSViewListener, which is
typically used to allow a reference other TSGraph so that zooming can occur similarly for all graphs.
The layout of the graph is as follows (see TSGraphJComponent for other layout
features like the main title).  Because the AWT Canvas and Graphics do not allow
for vertical text, the Y axis label is currently added at the top of the axis.
This may change in the future.
Currently only one legend can be present and the position can be either the
bottom (default, left, right, or top).  The following figure shows the placement
of all the legends, although only one will be used.
<pre>
------------------------------------------------------------------------------------------------------------------------
|                                               Full graph (_da_page)                                                  |
| -------------------------------------------------------------------------------------------------------------------- |
| |                                           Main title (_da_maintitle)                                             | |
| -------------------------------------------------------------------------------------------------------------------- |
| -------------------------------------------------------------------------------------------------------------------- |
| |                                            Sub title (_da_subtitle)                                              | |
| -------------------------------------------------------------------------------------------------------------------- |
| -------------------------------------------------------------------------------------------------------------------- |
| |                                                Legend (_da_top_legend)                                           | |
| -------------------------------------------------------------------------------------------------------------------- |
| ---                                     -------------------------------                                          --- |
| | |                                     |Top x axis title(not enabled)|                                          | | |
| | |                                     | (_da_topx_title)            |                                          | | |
| |_|                                     -------------------------------                                          |_| |
| |d|                                     -------------------------------                                          |d| |
| |a|                                     | Top x labels (not enabled)  |                                          |a| |
| |_|                                     | (_da_topx_label)            |                                          |_| |
| |l|                                     -------------------------------                                          |r| |
| |e|                                ---------------------   ----------------------                                |i| |
| |f|                                | Left y axis title |   | Right y axis title |                                |g| |
| |t|                                | (_da_lefty_title) |   | (_da_righty_title) |                                |h| |
| | |                                | *if not on left*  |   | *if not on right*  |                                | | |
| |_|                                ---------------------   ----------------------                                |t| |
| |l| -------------------- ---------------------- --------------------- --------------------- -------------------- |_| |
| |e| |                  | |                    | |      Graph        | |                   | |                  | |l| |
| |g| |Left y axis title | | Left y axis labels | | (_da_lefty_graph) | |Right y axis labels| |Right y axis title| |e| |
| |e| |(_da_lefty_title) | |  (_da_lefty_label) | | (_da_righty_graph)| |(_da_righty_label) | |(_da_righty_title)| |g| |
| |n| |*if not above axis| |                    | | they overlap      | |                   | |*if not above axis| |e| |
| |d| -------------------- ---------------------- --------------------- --------------------- -------------------- |n| |
| | |  * currently _da_lefty_title                ----------------------                                           |d| |
| | |    overlaps _da_lefty_label                 |Bottom x axis labels|                                           | | |
| | |                                             | (_da_bottomx_label)|                                           | | |
| | |                                             ----------------------                                           | | |
| | |                                             ----------------------                                           | | |
| | |                                             | Bottom x axis title|                                           | | |
| | |                                             | (_da_bottomx_title)|                                           | | |
| ---                                             ----------------------                                           --- |
| -------------------------------------------------------------------------------------------------------------------- |
| |                                             Legend (_da_bottom_legend)                                           | |
| -------------------------------------------------------------------------------------------------------------------- |
------------------------------------------------------------------------------------------------------------------------
</pre>
*/
public class TSGraph	// extends GRGraph //Future development??
implements ActionListener
{

/**
Popup menu options.
*/
private final String	
	__MENU_ANALYSIS_DETAILS = "Analysis Details",
	__MENU_PROPERTIES = "Properties",
	__MENU_REFRESH = "Refresh",
	__MENU_Y_MAXIMUM_AUTO = "Set Y Maximum to Auto",
	__MENU_Y_MINIMUM_AUTO = "Set Y Minimum to Auto",
	__MENU_Y_MAXIMUM_VISIBLE = "Set Y Maximum to Visible Maximum",
	__MENU_Y_MINIMUM_VISIBLE = "Set Y Minimum to Visible Minimum";

/**
If the graph type is Duration, this holds the results of the duration data for each time series.
*/
private List<TSDurationAnalysis> _duration_data = null;

/**
The following reference is used internally so that the _graphics does not need
to be passed between methods in this class.  The Graphics is volatile and should
be reset in each call to paint().
*/
private Graphics _graphics = null;

/**
Indicates whether units should be ignored for normal graphs, for left y-axis.
This allows line graphs to plot different units and units are put in the legend and removed from
the y-axis label.  If the units are not the same, then one of the following will occur:
<ol>
<li>	If the "LeftYAxisIgnoreUnits" property is set, then it will be used to
	indicate how the data should be treated.</li>
<li>	If the property is not set, the user will be prompted as to what they
	want to do.  They can choose to not continue.</li>
</ol>
The _ignore_units flag is then set.  If necessary, the
TSGraphJComponent.needToClose() method will be called and the graph won't be displayed.
*/
private boolean _ignoreLeftAxisUnits = false;

/**
 * Indicates whether units should be ignored for normal graphs, for right y-axis.
 * See the discussion of left axis.
 */
private boolean _ignoreRightAxisUnits = false;

/**
Whether left y-axis labels should be drawn or not.
Labels are only NOT drawn if the graph has no data or time series.
*/
private boolean __drawLeftyLabels = true;

/**
Whether right y-axis labels should be drawn or not.
Labels are only NOT drawn if the graph has no data or time series.
*/
private boolean __drawRightyLabels = true;

/**
Graph type for left y-axis.  Reset with properties.
The type is set based on the property to streamline code logic.
*/
private TSGraphType __leftYAxisGraphType = TSGraphType.LINE;

/**
Graph type for right y-axis.  Reset with properties.
The type is set based on the property to streamline code logic.
*/
private TSGraphType __rightYAxisGraphType = TSGraphType.NONE;

/**
The graph type for the last redraw.
This will force the analysis to be done for analytical graph types when the graph type changes.
*/
private TSGraphType __lastLeftYAxisGraphType = TSGraphType.UNKNOWN;	

/**
List of all the original time series to plot (this is used for the legend).
The time series are in the order of the data referenced for the graph to ensure alignment with properties.
*/
private List<TS> __tslist = null;

/**
List of all the derived time series to plot.  For example, the stacked bar graph requires that total
time series are used for plotting positions.  The contents of the list are determined by the graph type.
This list is guaranteed to be non-null but may be empty.
*/
private List<TS> __derivedTSList = new ArrayList<TS>();

/**
List of time series to plot using left y-axis.
This list is the same length as the full list but time series that are not associated with left axis will be null.
This is needed to keep the product property index lined up.
*/
private List<TS> __left_tslist = null;	

/**
Precision for left y-axis labels.
*/
private int _lefty_precision = 2;

/**
List of time series to plot using right y-axis.
This list is the same length as the full list but time series that are not associated with left axis will be null.
This is needed to keep the product property index lined up.
*/
private List<TS> __right_tslist = null;	

/**
Precision for right y-axis labels.
*/
private int _righty_precision = 2;

/**
Left y-axis direction.
*/
private GRAxisDirectionType __leftyDirection = GRAxisDirectionType.NORMAL;

/**
Right y-axis direction.
*/
private GRAxisDirectionType __rightyDirection = GRAxisDirectionType.NORMAL;

/**
TSProduct containing information about the graph product to be displayed.
*/
private TSProduct _tsproduct = null;

/**
Display properties used to indicate display characteristics outside the TSProduct.
*/
private PropList _display_props = null;

/**
TSProduct sub-product number (starting with zero, which is 1 off the value in the product file).
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
Data limits for left y-axis graph drawing area.
These are the data being drawn and reflect zoom etc.
This is true whether a reference or main graph and reflects the computation of labels.
These limits always are Y-axis increasing.
*/
private GRLimits _data_lefty_limits = null;

/**
Data limits for right y-axis graph drawing area.
These are the data being drawn and reflect zoom etc.
This is true whether a reference or main graph and reflects the computation of labels.
These limits always are Y-axis increasing.
*/
private GRLimits _data_righty_limits = null;

/**
Limits for time series data for full period, left y-axis, with no adjustments for nice labels.
*/
private TSLimits _max_tslimits_lefty = null;

/**
Limits for time series data for full period, right y-axis, with no adjustments for nice labels.
*/
private TSLimits _max_tslimits_righty = null;

/**
Limits for time series data for left y-axis current zoom.
*/
private TSLimits _tslimits_lefty = null;

/**
Limits for time series data for right y-axis current zoom.
*/
private TSLimits _tslimits_righty = null;

/**
Maximum data limits for left y-axis time series, based on full period data limits.
Initialize to unit limits because this is what the default data limits are.
Then, if no data are available, something reasonable can still be drawn (e.g., "No Data Available").
*/
private GRLimits _max_lefty_data_limits = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );

/**
Maximum data limits for right y-axis time series, based on full period data limits.
Initialize to unit limits because this is what the default data limits are.
Then, if no data are available, something reasonable can still be drawn (e.g., "No Data Available").
*/
private GRLimits _max_righty_data_limits = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );

/**
End date for current zoom level.  Done this way because generic drawing uses only coordinates - not dates.
*/
private DateTime _end_date;

/**
Start date for current zoom level.  Done this way because generic drawing uses only coordinates - not dates.
*/
private DateTime _start_date;

/**
End date for maximum data extents, applies to left and right y-axis time series.
Done this way because generic drawing uses only coordinates - not dates.
*/
private DateTime _max_end_date = null;

/**
Start date for maximum data extents, applies to left and right y-axis time series.
Done this way because generic drawing uses only coordinates - not dates.
*/
private DateTime _max_start_date = null;

private TSGraphJComponent _dev = null;

// Drawing areas from top to bottom (and left to right).

/**
Drawing area for full "page".
*/
private GRJComponentDrawingArea _da_page = null;
/**
Drawing area for main title.
*/
private GRJComponentDrawingArea _da_maintitle = null;
/**
Drawing area for sub title.
*/
private GRJComponentDrawingArea _da_subtitle = null;
/**
Drawing area for top X title.
*/
private GRJComponentDrawingArea _da_topx_title = null;
/**
Drawing area for top X labels.
*/
private GRJComponentDrawingArea _da_topx_label = null;
/**
Drawing area for left Y title.
*/
private GRJComponentDrawingArea _da_lefty_title = null;
/**
Drawing area for right Y title.
*/
private GRJComponentDrawingArea _da_righty_title = null;
/**
Drawing area for left Y labels.
*/
private GRJComponentDrawingArea _da_lefty_label = null;
/**
Drawing area for graph area inside the axes, left y-axis.
*/
private GRJComponentDrawingArea _da_lefty_graph = null;
/**
Drawing area for graph area inside the axes, right y-axis.
*/
private GRJComponentDrawingArea _da_righty_graph = null;
/**
Drawing area for right Y labels.
*/
private GRJComponentDrawingArea _da_righty_label = null;
/**
Drawing area for bottom X labels.
*/
private GRJComponentDrawingArea _da_bottomx_label = null;
/**
Drawing area for bottom X title.
*/
private GRJComponentDrawingArea _da_bottomx_title = null;
/**
Drawing area for bottom legend.
*/
private GRJComponentDrawingArea _da_bottom_legend = null;
/**
Drawing area for left legend.
*/
private GRJComponentDrawingArea _da_left_legend = null;
/**
Drawing area for right legend.
*/
private GRJComponentDrawingArea _da_right_legend = null;
/**
Drawing area for inside legend.
*/
private GRJComponentDrawingArea _da_inside_legend = null;

// Data and drawing limits listed from top to bottom (and left to right).

/**
Data limits for full "page".
*/
private GRLimits _datalim_page = null;
/**
Drawing limits for full "page".
*/
private GRLimits _drawlim_page = null;
/**
Data limits for the main title.
*/
private GRLimits _datalim_maintitle =null;
/**
Drawing limits for the main title.
*/
private GRLimits _drawlim_maintitle =null;
/**
Data limits for the main title.
*/
private GRLimits _datalim_subtitle = null;
/**
Drawing limits for sub title.
*/
private GRLimits _drawlim_subtitle = null;
/**
Data limits for top X title.
*/
private GRLimits _datalim_topx_title = null;
/**
Drawing limits for top X title.
*/
private GRLimits _drawlim_topx_title = null;
/**
Data limits for top X labels.
*/
private GRLimits _datalim_topx_label = null;
/**
Drawing limits for top X labels.
*/
private GRLimits _drawlim_topx_label = null;
/**
Data limits for left Y title.
*/
private GRLimits _datalim_lefty_title = null;
/**
Drawing limits for left Y title.
*/
private GRLimits _drawlim_lefty_title = null;
/**
Limits for right Y title.
*/
private GRLimits _datalim_righty_title = null;
/**
Limits for right Y title.
*/
private GRLimits _drawlim_righty_title = null;
/**
Limits for left Y labels.
*/
private GRLimits _datalim_lefty_label = null;
/**
Limits for left Y labels.
*/
private GRLimits _drawlim_lefty_label = null;
// Don't carry this around because it gets confusing
//private GRLimits _datalim_graph = null;		// Limits for graph
// _data_limits are the current viewable limits.  If a reference graph this is
// the highlighted area but not the limits corresponding to _da_graph.
/**
Drawing limits for graph, left y-axis.
*/
private GRLimits _drawlim_lefty_graph = null;
/**
Drawing limits for graph, right y-axis.
*/
private GRLimits _drawlim_righty_graph = null;
/**
Data limits for right Y labels.
*/
private GRLimits _datalim_righty_label = null;
/**
Drawing limits for right Y labels.
*/
private GRLimits _drawlim_righty_label = null;
/**
Data limits for bottom X labels.
*/
private GRLimits _datalim_bottomx_label = null;
/**
Drawing imits for bottom X labels.
*/
private GRLimits _drawlim_bottomx_label = null;
/**
Data limits for bottom X title.
*/
private GRLimits _datalim_bottomx_title = null;
/**
Drawing limits for bottom X title.
*/
private GRLimits _drawlim_bottomx_title = null;
/**
Data limits for bottom legend.
*/
private GRLimits _datalim_bottom_legend = null;
/**
Drawing limits for bottom legend.
*/
private GRLimits _drawlim_bottom_legend = null;
/**
Data limits for left legend.
*/
private GRLimits _datalim_left_legend = null;
/**
Drawing limits for left legend.
*/
private GRLimits _drawlim_left_legend = null;
/**
Data limits for right legend.
*/
private GRLimits _datalim_right_legend = null;
/**
Drawing limits for right legend.
*/
private GRLimits _drawlim_right_legend = null;
/**
Data limits for inside legend.
*/
private GRLimits _datalim_inside_legend = null;
/**
Drawing limits for inside legend.
*/
private GRLimits _drawlim_inside_legend = null;

// Dimensions for drawing areas...

/**
Is the graph a reference graph?  This is set at construction by checking the display properties.
*/
private boolean _is_reference_graph = false;

/**
If a reference graph, the index for the time series is selected in the
TSViewGraphFrame class and passed in (so that the reference time series can be
consistent between the main and reference graph canvases.
*/
private int _reference_ts_index = -1;

/**
Used for messages so can tell whether in a main or reference (overview) graph.
*/
private String _gtype = "Main:";
/**
When zooming, keep the Y axis limits the same (false indicates to recompute from data in the zoom window).
false also indicates that users can change the min and max values in the properties JFrame.
*/
private boolean _zoom_keep_y_limits = false;
/**
Regression data used by scatter plot.
*/
private List<TSRegression> _regression_data = null;

// TODO SAM Evaluate adding double mass plot
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
 * Indicate whether drawing area outlines should be shown.
 */
private boolean _showDrawingAreaOutline = false;

/**
Precision for x-axis date data.  This is not private because TSViewGraphGUI
uses the precision for the mouse tracker.
*/
protected int _xaxis_date_precision;

/**
DateTime format to use for bottom x-axis date data.
*/
private int _bottomx_date_format = -1;
/**
X-axis precision for numerical data.
*/
private int _xaxis_precision = 2;
/**
X-axis labels for numerical data.
*/
private double [] _xlabels = null;
/**
Y-axis labels for numerical data for left y-axis.
*/
private double [] _ylabels_lefty = null;
/**
Y-axis labels for numerical data for right y-axis.
*/
private double [] _ylabels_righty = null;
/**
Array for drawing shapes, in particular to avoid moveto/lineto.
*/
private double [] xCacheArray = null;
/**
Array for drawing shapes, in particular to avoid moveto/lineto.
*/
private double [] yCacheArray = null;
/**
Indicate whether to use cache for drawing arrays.
TODO SAM 2015-09-04 Need to enable.  Even if set to true only impacts line drawing for irregular time series.
*/
private boolean useXYCache = false;
/**
Fraction of points to add to cache arrays when more array positions are needed.
For example, if the array size is 100 and more slots are needed, use .5 to add 50%.
*/
private double xyCacheDelta = .5;

/**
If true, then the dates that were set for start_date and end_date will be
used for computing the date limits.  Otherwise, start_date and end_date will be recomputed.
*/
private boolean __useSetDates = false;

/**
Construct a TSGraph and display the time series.
@param dev TSGraphJComponent that is managing this graph.
@param drawlim_page Initial device limits that should be used for this graph,
determined in the managing TSGraphJComponent (the limits will change as the component is resized.
@param tsproduct TSProduct containing information to control display of time
series.  Most of these properties are documented in TSViewFrame, with the following additions:
@param display_props Additional properties used for displays.
ReferenceGraph can be set to "true" or "false" to indicate whether the graph is
a reference graph.  ReferenceTSIndex can be set to a list index to indicate
the reference time series for the reference graph (the default is the time
series with the longest overall period).  This value must be set for the local
tslist list that is passed in.
@param subproduct The sub-product from the main product.  This is used to look
up properties specific to this graph product.  The first product is 1.
@param tslist Vector of time series to graph.  Only the time series for this
graph are expected but time series for other graphs can be shared (access them in the TSProduct).
@param reference_ts_index Index in the "tslist" for the reference time series.
This may be different from the "ReferenceTSIndex" property in display_props,
which was for the original time series list (not the subset used just for this graph).
*/
public TSGraph ( TSGraphJComponent dev, GRLimits drawlim_page, TSProduct tsproduct, PropList display_props,
	int subproduct, List<TS> tslist, int reference_ts_index )
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
    __leftYAxisGraphType = TSGraphType.valueOfIgnoreCase ( tsproduct.getLayeredPropValue ( "GraphType", subproduct, -1, false ) );
    if ( __leftYAxisGraphType == null ) {
        // Should never happen...
        __leftYAxisGraphType = TSGraphType.LINE;
    }
    __rightYAxisGraphType = TSGraphType.valueOfIgnoreCase ( tsproduct.getLayeredPropValue ( "RightYAxisGraphType", subproduct, -1, false ) );
    if ( __rightYAxisGraphType == null ) {
        // Should never happen...
    	__rightYAxisGraphType = TSGraphType.NONE;
    }
	if ( tslist == null ) {
		// Create an empty vector so checks for null don't need to be added everywhere...
        Message.printStatus(2, routine, "Null list of time series for graph.  Using empty list for graph." );
		__tslist = new ArrayList<TS>();
	}
	else if ( __leftYAxisGraphType != TSGraphType.RASTER ) {
	    // OK to display all time series
        __tslist = tslist;
	}
	else if ( __leftYAxisGraphType == TSGraphType.RASTER ) {
	    // Only graph the first time series
	    // TODO SAM 2013-09-11 Evaluate whether 2+ time series can be displayed somehow (tiles? on top?)
	    __tslist = new ArrayList<TS>();
	    if ( tslist.size() > 0 ) {
	        __tslist.add(tslist.get(0));
	    }
	}
	// Get time series lists for each axis
	__left_tslist = new ArrayList<TS>();
	__right_tslist = new ArrayList<TS>();
	String propVal = null;
	int lefttsCount = 0;
	int righttsCount = 0;
	for ( int its = 0; its < __tslist.size(); its++ ) {
		propVal = _tsproduct.getLayeredPropValue ( "YAxis", _subproduct, its, false );
		// Include time series associated with the left y-axis
		if ( (propVal == null) || propVal.isEmpty() || propVal.equalsIgnoreCase("Left") ) {
			__left_tslist.add(__tslist.get(its));
			++lefttsCount;		}
		else {
			// Add a null so product properties will align properly with the full time series list.
			__left_tslist.add(null);
		}
		// Include time series associated with the right y-axis
		if ( (propVal != null) && propVal.equalsIgnoreCase("Right") ) {
			__right_tslist.add(__tslist.get(its));
			++righttsCount;
		}
		else {
			// Add a null so product properties will align properly with the full time series list.
			__right_tslist.add(null);
		}
	}

	// Check a few properties to increase performance.  The only properties
	// that are checked and set locally here are those that are not going
	// to change during the life of the graph, even if its properties are
	// changed.  All other properties should be checked before being used
	// (e.g., axis properties should be checked in the drawAxesBack() method).
	// It is a little slower to look up the properties sometimes but the code is simpler to maintain.

	if ( _dev.isReferenceGraph() ) {
		_is_reference_graph = true;
		_gtype = "Ref:";
	}

	// Might need to use this when we try to process all null time series...
	int ssize = __tslist.size();
    Message.printStatus(2, routine, _gtype + "Have " + ssize + " time series for all graphs." );
    Message.printStatus(2, routine, _gtype + "Have " + lefttsCount + " time series for left y-axis graphs." );
    Message.printStatus(2, routine, _gtype + "Have " + righttsCount + " time series for right y-axis graphs." );
	TS sts;
	for (int ii = 0; ii < ssize; ii++) {
		sts = __tslist.get(ii);
		if (sts == null) {
			Message.printStatus(3, routine, _gtype + "TS[" + ii + "] is null");
		}
		else {	
			Message.printStatus(3, routine, _gtype + "TS[" + ii + "] is " + sts.getIdentifierString() +
			        "period " + sts.getDate1() + " to " + sts.getDate2() );
		}
	}

	// A reference TS index can be used in a main or reference graph...

	_reference_ts_index = reference_ts_index;

	// TODO SAM 2013-02-06 maybe should put some of the other code in the following call?
	checkInternalProperties ();

	if (_is_reference_graph) {
		__leftYAxisGraphType = TSGraphType.LINE;
	}

	_drawlim_page = new GRLimits ( drawlim_page );

	openDrawingAreas ();

	// Perform the data analysis once to get data limits...
	// This is the place where the reference graph has its data set.
	// This is also checked in the paint() method in case any analysis settings change...

	doAnalysis(__leftYAxisGraphType);
	__lastLeftYAxisGraphType = __leftYAxisGraphType;

	// Initialize the data limits...

	if ( _is_reference_graph ) {
	    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
	        GRLimits limits = new GRLimits(_max_lefty_data_limits);
	        _da_lefty_graph.setDataLimits ( limits.reverseY() );
	    }
	    else {
	        _da_lefty_graph.setDataLimits ( _max_lefty_data_limits );
	    }
	}
	else {
		// Left y-axis
	    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
            GRLimits limits = new GRLimits(_data_lefty_limits);
            _da_lefty_graph.setDataLimits ( limits.reverseY() );
        }
        else {
            _da_lefty_graph.setDataLimits ( _data_lefty_limits );
        }
		// Right y-axis
	    if ( __rightyDirection == GRAxisDirectionType.REVERSE ) {
            GRLimits limits = new GRLimits(_data_righty_limits);
            _da_righty_graph.setDataLimits ( limits.reverseY() );
        }
        else {
            _da_righty_graph.setDataLimits ( _data_righty_limits );
        }
	}

	// Get the units to use for the left y-axis...

	int size = 0;
	if ( __left_tslist != null ) {
		size = __left_tslist.size();
	}
	if ( __left_tslist != null ) {
		TS ts = null;
		for ( int i = 0; i < size; i++ ) {
			ts = __left_tslist.get(i);
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
	}
	computeXAxisDatePrecision ();
	
	// Get the units to use for the left y-axis...

	size = 0;
	if ( __right_tslist != null ) {
		size = __right_tslist.size();
	}
	if ( __right_tslist != null ) {
		TS ts = null;
		for ( int i = 0; i < size; i++ ) {
			ts = __right_tslist.get(i);
			if ( (ts == null) || !ts.getEnabled() ) {
				continue;
			}
			// Check the interval so that we can make decisions during plotting...
			/** TODO SAM 2016-10-23 Evaluate how to do on right y-axis
			try {
                _interval_max = MathUtil.max ( _interval_max, ts.getDataIntervalBase() );
				_interval_min = MathUtil.min ( _interval_min, ts.getDataIntervalBase() );
			}
			catch ( Exception e ) {
				// Probably never will occur.
                Message.printWarning (3, routine, e);
			}
			*/
		}
	}
	// TODO SAM 2016-10-23 Evaluate whether the following is needed
	//computeYAxisDatePrecision ();
}

/**
Handle action events generated by the popup menu for this graph
*/
public void actionPerformed(ActionEvent event)
{
	String command = event.getActionCommand ();
	if ( command.equals(__MENU_PROPERTIES) ) {
		// Only one properties window is shown per graph so let the TSViewFrame handle showing the properties
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES);
		
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
		// Display the results of the regression(s), duration plot, etc.
		if ( (_duration_data != null) && (_duration_data.size() > 0) ) {
			// Create a DataTable and then use generic table view component
			DataTable table = new DataTable();
			List<TSDurationAnalysis> durationList = _duration_data;
			List<DataTable> tableList = new ArrayList<DataTable>(durationList.size());
			String [] viewLabels = new String[durationList.size()];
			int its = -1;
			for ( TSDurationAnalysis a : durationList ) {
				// Each analysis has a time series and arrays of values and percents
				// Create simple tables for each and pass to the display
				++its;
				table = new DataTable();
				tableList.add(table);
				TS ts = a.getTS();
				// This is copied from TSViewTable_TableModel.getColumnName()
				boolean useExtendedLegend = false; // For now disable this to see how following works
				String valueLabel = ts.getLocation();
				String percentLabel = "percent of values >= value";
				viewLabels[its] = ts.getLocation(); // Default
				if (useExtendedLegend && (ts.getExtendedLegend().length() != 0)) {
					valueLabel = ts.formatLegend(ts.getExtendedLegend());
				}
				else if (ts.getLegend().length() > 0) {
					valueLabel = ts.formatLegend(ts.getLegend());
				}
				else {	
					String unitsString = "";
					String datatypeString = "";
					String sequenceString = "";
					if (ts.getDataUnits().length() > 0) {
						unitsString = " (" + ts.getDataUnits() +")";
					}
					if (ts.getDataType().length() == 0) {
						datatypeString = ", " + ts.getIdentifier().getType();
					}
					else {
						datatypeString = ", " + ts.getDataType();
					}
					if (ts.getSequenceID().length() > 0) {
						sequenceString = " [" + ts.getSequenceID() + "]";
					}
					if (ts.getAlias().equals("")) {
						valueLabel = ts.getLocation() + sequenceString + datatypeString + unitsString;
						viewLabels[its] = ts.getLocation() + sequenceString;
					}
					else {
						valueLabel = ts.getAlias() + sequenceString + datatypeString + unitsString;
						viewLabels[its] = ts.getAlias() + sequenceString;
					}
				}
				// --end copy
				table.addField(new TableField(TableField.DATA_TYPE_DOUBLE, valueLabel, -1, 2), null);
				table.addField(new TableField(TableField.DATA_TYPE_DOUBLE, percentLabel, -1, 2), null);
				double [] values = a.getValues();
				double [] percents = a.getPercents();
				for ( int i = 0; i < values.length; i++ ) {
					try {
						table.setFieldValue(i, 0, values[i], true);
						table.setFieldValue(i, 1, percents[i], true);
					}
					catch ( Exception e ) {
						// Swallow for now
						Message.printWarning(3,"","Error adding data to duration curve table (" + e + ").");
					}
				}
			}
			try {
				new DataTableList_JFrame("Duration Curve Analysis Data", viewLabels, tableList);
			}
			catch ( Exception e ) {
				Message.printWarning(1, "TSGraph.actionPerformed", "Unable to display duration curve analysis data (" + e + ").");
				Message.printWarning(3, "TSGraph.actionPerformed", e );
			}
		}
		if (_regression_data != null) {
			int size = 0;
			if (_regression_data != null) {
				size = _regression_data.size();
			}
			TSRegression r = null;
			List<String> v = new Vector();	
			for (int i = 0; i < size; i++) {
				r = _regression_data.get(i);
				if (r == null) {
					continue;
				}
				// Split by newlines so that report has separate lines of information...
				List<String> lines = StringUtil.breakStringList ( r.toString(), "\n", 0 );
				int size2 = 0;
				if ( lines != null ) {
				    size2 = lines.size();
				    for ( int i2 = 0; i2 < size2; i2++ ) {
				        v.add ( lines.get(i2) );
				    }
				}
				v.add(r.toString ());
				v.add("");
				v.add("");
			}
			
			if (v.size() == 0) {
				Message.printWarning(1, "TSGraph.actionPerformed", "No regression results available.");
			}
			
			PropList p = new PropList("Regression");
			p.set("Title", "Analysis Details");
			p.set("TotalHeight", "400");
			new ReportJFrame(v, p);
		}
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
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		TSLimits limits = null;

		List<TS> tslist = null;
		if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	    	int nreg = 0;
			if (__tslist != null) {
				nreg = __tslist.size() - 1;
			}
			List<TS> v = new ArrayList<TS>();
			TSRegression regressionData = null;
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1)) {
					continue;
				}
				regressionData = _regression_data.get(i);
				v.add(regressionData.getResidualTS());
			}
			tslist = v;
		}
		else if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
	    	int nreg = 0;
			if (__tslist != null) {
				nreg = __tslist.size() - 1;
			}
			List<TS> v = new ArrayList<TS>();
			TSRegression regressionData = null;
			if (isTSEnabled(0)) {
				v.add(__tslist.get(0));
			}
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1)) {
					continue;
				}
				regressionData = _regression_data.get(i);
				if (isTSEnabled(i + 1)) {
					v.add(regressionData.getDependentTS());
					v.add(regressionData.getPredictedTS());
				}
			}
			tslist = v;
		}		
		else {
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			tslist = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
		}
		
		try {
			limits = TSUtil.getDataLimits(tslist, _start_date, _end_date, "", false, _ignoreLeftAxisUnits);
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
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		TSLimits limits = null;

    	int nreg = 0;
		if (__tslist != null) {
			nreg = __tslist.size() - 1;
		}
		List<TS> v = new Vector();
		TSRegression regressionData = null;
		for (int i = 0; i < nreg; i++) {
			if (!isTSEnabled(i + 1)) {
				continue;
			}
			regressionData = _regression_data.get(i);
			v.add(regressionData.getResidualTS());
		}
		
		try {
			limits = TSUtil.getDataLimits(v,_start_date, _end_date, "", false,_ignoreLeftAxisUnits);
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
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);
		
		// Display the properties for the graph of choice
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(_subproduct);
		pframe.setMaximumYValue("Auto");
	}
	else if (command.equals(__MENU_Y_MINIMUM_AUTO)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);
		
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
makes sure we have cleanly handled the end points without having to adjust the array lengths.
@param x X-coordinates for confidence curve.
@param y Y-coordinates for confidence curve.
@param npts Number of points to process (may be less than array size).
*/
private void adjustConfidenceCurve ( double [] x, double [] y, int npts )
{	String routine = "TSGraph.adjustConfidenceCurve";
    // First figure out if the slope of the line is up to the right or down to the right...
	int i = 0;
	double x_edge = 0.0;
	double min_datay = _data_lefty_limits.getMinY();
	double max_datay = _data_lefty_limits.getMaxY();
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
				for ( int i2 = (i - 2); i2 >= 0; i2-- ) {
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
				for ( int i2 = (i + 2); i2 < npts; i2++ ) {
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
	if ((__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (__leftYAxisGraphType == TSGraphType.DURATION) ||
	    (__leftYAxisGraphType == TSGraphType.RASTER)) {
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
and units, which are used for the mouse tracker).  This method should be called immediately before drawing.
*/
private void checkInternalProperties ()
{	// "BottomXAxisLabelFormat" = _bottomx_date_format;

	String prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFormat", _subproduct, -1, false );
	if ( prop_val != null ) {
		// Currently only handle special cases...
		if ( prop_val.equalsIgnoreCase("MM-DD") ) {
			_bottomx_date_format = DateTime.FORMAT_MM_DD;
		}
	}
	
	// "LeftYAxisDirection"

    __leftyDirection = GRAxisDirectionType.NORMAL;
    prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisDirection", _subproduct, -1, false );
    if ( prop_val != null ) {
        __leftyDirection = GRAxisDirectionType.valueOfIgnoreCase(prop_val);
        if ( __leftyDirection == null ) {
            __leftyDirection = GRAxisDirectionType.NORMAL;
        }
    }
    //String routine = getClass().getName() + ".checkInternalProperties";
    //Message.printStatus(2, routine, "LeftYAxisDirection=" + __leftyDirection );

	// "LeftYAxisLabelPrecision" = _lefty_precision;

	_lefty_precision = 2;
	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelPrecision", _subproduct, -1, false );
	if ( prop_val != null ) {
		_lefty_precision = StringUtil.atoi ( prop_val );
	}
	
	// "RightYAxisDirection"

    __rightyDirection = GRAxisDirectionType.NORMAL;
    prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisDirection", _subproduct, -1, false );
    if ( prop_val != null ) {
        __rightyDirection = GRAxisDirectionType.valueOfIgnoreCase(prop_val);
        if ( __rightyDirection == null ) {
            __rightyDirection = GRAxisDirectionType.NORMAL;
        }
    }

	// "RightYAxisLabelPrecision" = _righty_precision;

	_righty_precision = 2;
	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelPrecision", _subproduct, -1, false );
	if ( prop_val != null ) {
		_righty_precision = StringUtil.atoi ( prop_val );
	}
}

/**
TODO SAM 2010-11-22 This only computes the max/min date/time range?  Evaluate SetDataLimits() use.
Compute the maximum data limits based on the time series.  This is normally
only called from doAnalysis(), which is called at construction.  The maximum
values and the current data limits are set to the limits, which serve as the
initial data limits until zooming occurs.
@param graphType the type of graph being produced.
@param max whether to compute data limits from the max dates or not.  Currently, this really
only applies to empty graphs.  For other graphs, see setComputeWithSetDates().
*/
protected void computeDataLimits ( boolean max )
{	String routine = "TSGraph.computeDataLimits";
    TSGraphType graphType = getLeftYAxisGraphType();
	// Exceptions are thrown when trying to draw empty graph (no data)
    // Left y-axis is the most complex so process first
    boolean includeLeftYAxis = true;
    boolean includeRightYAxis = false;
    List<TS> enabledLeftYAxisTSList = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
	if (enabledLeftYAxisTSList.size() == 0) {
		// All this is for left y-axis
		__useSetDates = false;
		if (max) {
			if (_max_start_date == null) {
				_data_lefty_limits = new GRLimits(0, 0, 0, 0);
				_max_lefty_data_limits = new GRLimits(_data_lefty_limits);
			}
			else {
				_data_lefty_limits = new GRLimits( _max_start_date.toDouble(), 0, _max_end_date.toDouble(), 1);
				_max_lefty_data_limits = new GRLimits(_data_lefty_limits);
			}
		}
		else {
			if (_start_date == null) {
				_data_lefty_limits = new GRLimits(0, 0, 0, 0);
				_max_lefty_data_limits = new GRLimits(_data_lefty_limits);
			}
			else {
				_data_lefty_limits = new GRLimits( _start_date.toDouble(), 0, _end_date.toDouble(), 1);
				_max_lefty_data_limits = new GRLimits(_data_lefty_limits);
			}
		}
	}
	
	// Right y-axis
	
    includeLeftYAxis = false;
    includeRightYAxis = true;
    List<TS> enabledRightYAxisTSList = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
	if (enabledRightYAxisTSList.size() == 0) {
		// All this is for right y-axis
		if (max) {
			if (_max_start_date == null) {
				_data_righty_limits = new GRLimits(0, 0, 0, 0);
				_max_righty_data_limits = new GRLimits(_data_righty_limits);
			}
			else {
				_data_righty_limits = new GRLimits( _max_start_date.toDouble(), 0, _max_end_date.toDouble(), 1);
				_max_righty_data_limits = new GRLimits(_data_righty_limits);
			}
		}
		else {
			if (_start_date == null) {
				_data_righty_limits = new GRLimits(0, 0, 0, 0);
				_max_righty_data_limits = new GRLimits(_data_righty_limits);
			}
			else {
				_data_righty_limits = new GRLimits( _start_date.toDouble(), 0, _end_date.toDouble(), 1);
				_max_righty_data_limits = new GRLimits(_data_righty_limits);
			}
		}
	}
	
	// Left y-axis

	includeLeftYAxis = true;
    includeRightYAxis = false;

	try {
        // First get the date limits from the full set of time series...
		TSLimits limits = null;
		if ( enabledLeftYAxisTSList.size() > 0 ) {
			if ( graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL ) {
				// Use only the left y-axis and assume all time series,
				// until there is time to evaluate whether right y-axis can be enabled
		    	int nreg = __tslist.size() - 1;
				List<TS> v = new ArrayList<TS>();
				TSRegression regressionData = null;
				for (int i = 0; i < nreg; i++) {
					if (!isTSEnabled(i + 1)) {
						continue;
					}
					regressionData = _regression_data.get(i);
					v.add(regressionData.getResidualTS());
				}
				limits = TSUtil.getPeriodFromTS(v, TSUtil.MAX_POR);
			}
			else if ( graphType == TSGraphType.AREA_STACKED ) {
			    limits = TSUtil.getPeriodFromTS(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), TSUtil.MAX_POR);
			}
			else {
			    // Get the limits from the enabled time series
				limits = TSUtil.getPeriodFromTS(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), TSUtil.MAX_POR);
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
			_ignoreLeftAxisUnits = false;
			_ignoreRightAxisUnits = false;
			// First set defaults...
			if ( (__leftYAxisGraphType == TSGraphType.DOUBLE_MASS) || (__leftYAxisGraphType == TSGraphType.PERIOD) ||
				(__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
				_ignoreLeftAxisUnits = true;
				_ignoreRightAxisUnits = true;
			}
			// Now check the property (keep a separate copy so we can avoid the prompt below if appropriate)...
			String ignoreLeftAxisUnitsProp = _tsproduct.getLayeredPropValue ( "LeftYAxisIgnoreUnits", _subproduct, -1, false);
			boolean ignoreLeftAxisUnits = false;
			if ( (ignoreLeftAxisUnitsProp != null) && ignoreLeftAxisUnitsProp.equalsIgnoreCase("true") ) {
				_ignoreLeftAxisUnits = true;
				if (TSUtil.areUnitsCompatible(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), true)) {
					_ignoreLeftAxisUnits = false;
					_tsproduct.setPropValue("LeftYAxisIgnoreUnits", "false", _subproduct, -1);
					ignoreLeftAxisUnits = true;
				}
			}
			String ignoreRightAxisUnitsProp = _tsproduct.getLayeredPropValue ( "RightYAxisIgnoreUnits", _subproduct, -1, false);
			boolean ignoreRightAxisUnits = false;
			if ( (ignoreRightAxisUnitsProp != null) && ignoreRightAxisUnitsProp.equalsIgnoreCase("true") ) {
				_ignoreRightAxisUnits = true;
				if (TSUtil.areUnitsCompatible(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), true)) {
					_ignoreRightAxisUnits = false;
					_tsproduct.setPropValue("RightYAxisIgnoreUnits", "false", _subproduct, -1);
					ignoreRightAxisUnits = true;
				}
			}
			// TODO SAM 2016-10-17 Need to evaluate how to handle right y-axis - for now set to false
			_ignoreRightAxisUnits = false;
			try {
				if (_ignoreLeftAxisUnits) {
					// Can ignore units...
					_max_tslimits_lefty = TSUtil.getDataLimits( getTSListToRender(true,includeLeftYAxis,includeRightYAxis), _start_date, _end_date, "", false, _ignoreLeftAxisUnits);
				}
				else {
	                // Need to have consistent units.  For now require them to be the same because we don't
					// want to do units conversions on the fly or alter the original data...
					//
					// TODO - need to add on the fly conversion of units (slower but changing original data is
					// a worse alternative).
					if (!TSUtil.areUnitsCompatible(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), true)) {
						if (_is_reference_graph) {
							// Rely on the main graph to set the _ignore_units flag and determine whether the graph
							// view needs to be closed.  Assume that the _ignore_units flag can be set to true since
							// the reference graph only displays one graph.
							_ignoreLeftAxisUnits = true;
							_max_tslimits_lefty = TSUtil.getDataLimits( getTSListToRender(true,includeLeftYAxis,includeRightYAxis), _start_date, _end_date, "", false, _ignoreLeftAxisUnits);
						}
						else if (ignoreLeftAxisUnits) {
							_max_tslimits_lefty = TSUtil.getDataLimits( getTSListToRender(true,includeLeftYAxis,includeRightYAxis), 
								_start_date, _end_date, "", false, _ignoreLeftAxisUnits);
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
								_ignoreLeftAxisUnits = true;
								_max_tslimits_lefty = TSUtil.getDataLimits( getTSListToRender(true,includeLeftYAxis,includeRightYAxis), _start_date,
								        _end_date, "", false, _ignoreLeftAxisUnits);
							}
						}
					}
					else {	
	                	if (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	                		int nreg = 0;
	                		if (__tslist != null) {
	                			nreg = __tslist.size() - 1;
	                		}
	                	
	                		List<TS> v = new ArrayList<TS>();
	                		TSRegression regressionData = null;
	                		TSLimits tempLimits = null;
	                		double maxValue = 0;
	                		double minValue = 0;
	                		for (int i = 0; i < nreg; i++) {
	                			if (!isTSEnabled(i + 1)) {
	                				continue;
	                			}
	                			regressionData = _regression_data.get(i);
	                			v.add(regressionData.getResidualTS());
	                			tempLimits = TSUtil.getDataLimits(v, _start_date, _end_date, "", false, _ignoreLeftAxisUnits );
	                			if (tempLimits.getMaxValue() > maxValue) {
	                				maxValue = tempLimits.getMaxValue();
	                			}
	                			if (tempLimits.getMinValue() < minValue) {
	                				minValue = tempLimits.getMinValue();
	                			}
	                		}
	                		_max_tslimits_lefty = TSUtil.getDataLimits(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), 
	                			_start_date, _end_date, "", false, _ignoreLeftAxisUnits);
	                		_max_tslimits_lefty.setMaxValue(maxValue);
	                		_max_tslimits_lefty.setMinValue(minValue);
	                	}
	                	else {
	                		_max_tslimits_lefty = TSUtil.getDataLimits(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), 
	                			_start_date, _end_date, "", false, _ignoreLeftAxisUnits);
	                	}
					}
				}
				// If a period graph, the limits should be a count of
				// the time series, 0 to 1 more than the time series
				// count.  Reverse the axis so the number is correct...
				if ( graphType == TSGraphType.PERIOD ) {
					_max_tslimits_lefty.setMaxValue(0.0);
					_max_tslimits_lefty.setMinValue(getEnabledTSList(includeLeftYAxis,includeRightYAxis).size() + 1);
				}
			}
			catch ( Exception e ) {
				// This typically throws an exception if the data are not of consistent units.
				if ( !_is_reference_graph ) {
					Message.printWarning ( 1, routine, "Data are not compatible (different units?).  Cannot graph." );
					Message.printWarning ( 2, routine, e );
				}
			}
			if ( _max_tslimits_lefty == null ) {
				// Typically due to a cancel of the graph due to incompatible units.  In this case we get to here but
				// just need to gracefully handle nulls until the graph can be closed in parent container code...
				return;
			}
			if (_is_reference_graph && (_reference_ts_index >= 0)) {
				// Reset the coordinates based only on the reference time series but use the full period for dates...
				List<TS> ref_tslist = new Vector(1,1);
				ref_tslist.add(__tslist.get(_reference_ts_index));
				TSLimits reflimits = TSUtil.getDataLimits (	ref_tslist, _start_date, _end_date, "", false,_ignoreLeftAxisUnits );
				_max_tslimits_lefty.setMinValue ( reflimits.getMinValue() );
				_max_tslimits_lefty.setMaxValue ( reflimits.getMaxValue() );
				reflimits = null;	// clean up
				ref_tslist = null;
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Reference graph max data limits are " + _max_tslimits_lefty );
				}
			}
			else {
	            if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Main graph max data limits are " + _max_tslimits_lefty );
				}
				// If the properties are given, set the limits to the given properties,
	            // but only if they are outside the range of the data that was determined...
				//
				// TODO SAM 2006-09-28
				// Still need to evaluate how switching between Auto and hard limits can be handled better.
				String prop_value = _tsproduct.getLayeredPropValue ( "LeftYAxisMax", _subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymax = StringUtil.atod(prop_value);
					if (!_zoom_keep_y_limits && ymax > _max_tslimits_lefty.getMaxValue()) {
						_max_tslimits_lefty.setMaxValue(ymax);
					}
				}
				else if (prop_value != null && prop_value.equalsIgnoreCase("Auto")) {
	            	if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	            		int nreg = 0;
	            		if (__tslist != null) {
	            			nreg = __tslist.size() - 1;
	            		}
	            	
	            		List<TS> v = new ArrayList<TS>();
	            		TSRegression regressionData = null;
	            		double minValue = 0;
	            		double maxValue = 0;
	            		TSLimits tempLimits = null;
	            		for (int i = 0; i < nreg; i++) {
	            			if (!isTSEnabled(i + 1)) {
	            				continue;
	            			}
	            			regressionData = _regression_data.get(i);
	            			v.add(regressionData.getResidualTS());
	            			tempLimits = TSUtil.getDataLimits(v, _max_start_date,_max_end_date, "", false, _ignoreLeftAxisUnits );
	            			if (tempLimits.getMaxValue() > maxValue) {
	            				maxValue = tempLimits.getMaxValue();
	            			}
	            			if (tempLimits.getMinValue() < minValue) {
	            				minValue = tempLimits.getMinValue();
	            			}
	            		}
	            
	            		_max_tslimits_lefty.setMaxValue(maxValue);
	            		_max_tslimits_lefty.setMinValue(minValue);
	            	}
	            	else {
	            		TSLimits tempLimits = TSUtil.getDataLimits(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), 
	            			_max_start_date, _max_end_date, "", false, _ignoreLeftAxisUnits);
	            		_max_tslimits_lefty.setMaxValue(tempLimits.getMaxValue());
	            	}
				// TODO SAM 2006-09-28
				// Still need to evaluate how switching between Auto and hard limits can be handled better.
				}	
				prop_value = _tsproduct.getLayeredPropValue ("LeftYAxisMin", _subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymin = StringUtil.atod(prop_value);
					if (!_zoom_keep_y_limits && ymin < _max_tslimits_lefty.getMinValue()) {
						_max_tslimits_lefty.setMinValue(ymin);
					}
				}
			}
			_tslimits_lefty = new TSLimits ( _max_tslimits_lefty );
			// Initialize this here because this is what the reference
			// graph uses throughout (it does not need nice labels).
			if ( __leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
				boolean xlimits_found = false;
				boolean ylimits_found = false;
				TSRegression regressionData = null;
				int nregression = 0;
				if ( _regression_data != null ) {
					nregression = _regression_data.size();
				}
				double xmin = 0.0, ymin = 0.0, xmax = 1.0, ymax = 1.0;
				for ( int ir = 0; ir < nregression; ir++ ) {
					regressionData = _regression_data.get(ir);
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
							TS rts = regressionData.getIndependentTS();
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
							rts = regressionData.getDependentTS();
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
					TS ts0 = __tslist.get(0);
					TSLimits limits0 = ts0.getDataLimits();
					xmin = limits0.getMinValue();
					xmax = limits0.getMaxValue();
				}
	
				if (!ylimits_found) {
					// Loop through the Y axis time series (hopefully this code is never executed).
					TS ts = null;
					TSLimits ylimits = null;
					for ( int its = 1; its <= nregression; its++ ) {
						ts = __tslist.get(its);
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
				}
				// Set the limits regardless.  Worst case they will be zero to one...
				_data_lefty_limits = new GRLimits ( xmin, ymin, xmax, ymax );
			}
			else if ( __leftYAxisGraphType == TSGraphType.DURATION ) {
				// X limits are 0 to 100.  Y limits are based on the time series...
				_data_lefty_limits = new GRLimits ( 0.0, 0.0,	100.0, _tslimits_lefty.getMaxValue() );
			}
			else if ( (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL)
			    || (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) ) {
			    boolean residual = true;
				if (__leftYAxisGraphType==TSGraphType.PREDICTED_VALUE) {
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
				List<TS> tempV = null;
				for (int ir = 0; ir < nregression; ir++) {
					regressionData = _regression_data.get(ir);
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
						tsLimits = TSUtil.getDataLimits( tempV, _max_start_date, _max_end_date, "", false, _ignoreLeftAxisUnits);
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
				_data_lefty_limits = new GRLimits ( _start_date.toDouble(), minValue, _end_date.toDouble(), maxValue);
			}
	        else if ( __leftYAxisGraphType == TSGraphType.RASTER ) {
	            // X limits are 0 to 367 if daily, 1 to 13 if monthly (right side is at edge of next interval.
	            // Y limits are based on the year of the period of the time series.
	            int intervalBase = TimeInterval.UNKNOWN;
	            for ( TS ts : __tslist ) {
	                if ( ts != null ) {
	                    intervalBase = ts.getDataIntervalBase();
	                    break;
	                }
	            }
	            if ( intervalBase == TimeInterval.DAY ) {
	                // TODO SAM 2013-07-20 Need to figure out how to handle leap year, for now always include
	                _data_lefty_limits = new GRLimits ( 1.0, _tslimits_lefty.getDate1().getYear(), 367.0, _tslimits_lefty.getDate2().getYear() + 1 );
	            }
	            else if ( intervalBase == TimeInterval.MONTH ) {
	                _data_lefty_limits = new GRLimits ( 1.0, _tslimits_lefty.getDate1().getYear(), 13.0, _tslimits_lefty.getDate2().getYear() + 1 );
	            }
	            Message.printStatus(2,routine,"Data limits for raster graph: " + _data_lefty_limits);
	        }
			else {	
				_data_lefty_limits = new GRLimits (_start_date.toDouble(), _tslimits_lefty.getMinValue(),
					_end_date.toDouble(),_tslimits_lefty.getMaxValue() );
			}
	
			if ( _data_lefty_limits != null ) {
				if (max) {
					_max_lefty_data_limits = new GRLimits(_data_lefty_limits);
				}
				else {
					if (__leftYAxisGraphType == TSGraphType.XY_SCATTER) {
					    _max_lefty_data_limits = new GRLimits(_data_lefty_limits);
					}
					else {
	    				GRLimits tempLimits = new GRLimits( _max_start_date.toDouble(), _tslimits_lefty.getMinValue(),
	    				    _max_end_date.toDouble(),_tslimits_lefty.getMaxValue() );
	    				_max_lefty_data_limits = new GRLimits(tempLimits);
					}
				}
	
				if (Message.isDebugOn) {
					Message.printDebug ( 1, routine, _gtype	+
					    "Initial computed _max_lefty_data_limits (including limit properties) are " + _max_lefty_data_limits );
				}
			}
		}
		
		// Right y-axis

		if ( enabledRightYAxisTSList.size() > 0 ) {
			// TODO SAM 2016-10-24 Need to do this only for allowed simple graph types
			//if ( graph type allowed ) {
			if ( _tslimits_righty != null ) {
				_data_righty_limits = new GRLimits (_start_date.toDouble(), _tslimits_righty.getMinValue(),
					_end_date.toDouble(),_tslimits_righty.getMaxValue() );
			}
	
			if ( _data_righty_limits != null ) {
				if (max) {
					_max_righty_data_limits = new GRLimits(_data_righty_limits);
				}
				else {
					if (__rightYAxisGraphType == TSGraphType.XY_SCATTER) {
					    _max_righty_data_limits = new GRLimits(_data_righty_limits);
					}
					else {
						if ( _tslimits_righty != null ) {
		    				GRLimits tempLimits = new GRLimits( _max_start_date.toDouble(), _tslimits_righty.getMinValue(),
		    				    _max_end_date.toDouble(),_tslimits_righty.getMaxValue() );
		    				_max_righty_data_limits = new GRLimits(tempLimits);
						}
					}
				}
	
				if (Message.isDebugOn) {
					Message.printDebug ( 1, routine, _gtype	+
					    "Initial computed _max_righty_data_limits (including limit properties) are " + _max_righty_data_limits );
				}
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
Compute the x-axis and y-axis labels given the current zoomed data.
Call this after the data limits have initially been set.
The label values are computed based on the
drawing area size and the axis font to make sure that labels do not overlap.
This resets _datalim_lefty_graph and _datalim_righty_graph to be nicer bounding limits.
@param limitsLeftYAxis For data that is being used (generally the max or current
limits - whatever the graph is supposed to display).  <b>This is time series
data so for scatter plots, etc., it does not contain all that is needed.</b>
*/
private void computeLabels ( TSLimits limitsLeftYAxis, TSLimits limitsRightYAxis )
{   String routine = "TSGraph.computeLabels";
	String propValue = null;
	
	// Left y-axis

	if ( (_da_lefty_graph == null) || (limitsLeftYAxis == null) ) {
		// Have not initialized the drawing areas yet or bad graph data...
		// TODO JTS otherwise exceptions thrown when drawing an empty graph
		_ylabels_lefty = new double[1];
		_ylabels_lefty[0] = 0;
		__drawLeftyLabels = false;
		return;
	}
	else {
		__drawLeftyLabels = true;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Computing left y-axis labels using TS limits: " + limitsLeftYAxis );
	}
	
	// Right y-axis
	
	if ( (_da_righty_graph == null) || (limitsLeftYAxis == null) ) {
		// Have not initialized the drawing areas yet or bad graph data...
		// TODO JTS otherwise exceptions thrown when drawing an empty graph
		_ylabels_righty = new double[1];
		_ylabels_righty[0] = 0;
		__drawRightyLabels = false;
		return;
	}
	else {
		propValue = _tsproduct.getLayeredPropValue(	"RightYAxisGraphType", _subproduct, -1, false);
		if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
			__drawRightyLabels = true;
		}
		else {
			__drawRightyLabels = false;
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Computing right y-axis labels using TS limits: " + limitsLeftYAxis );
	}

	boolean log_y_lefty = false;
	boolean log_xy_scatter = false;
	propValue = _tsproduct.getLayeredPropValue(	"LeftYAxisType", _subproduct, -1, false);
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_lefty = true;
	}

	propValue = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_lefty = false;
		log_xy_scatter = true;
	}	

	// Now get recompute the limits to be nice.  First do the Y axis...
	// The maximum number of labels is based on the font height and the
	// drawing area height.  However, in most cases, we want at least a
	// spacing of 3 times the font height, unless this results in less than 3 labels.
	double height, width;
	// Format a label based on the font for the Y axis...

	String fontname = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", _subproduct, -1, false );
	String fontsize = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", _subproduct, -1, false );
	String fontstyle = _tsproduct.getLayeredPropValue (	"LeftYAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_lefty_label, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRLimits label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, "astring", GRUnits.DEVICE );
	height = label_extents.getHeight();
	width = label_extents.getWidth();
	int minlabels = (int)(_drawlim_lefty_graph.getHeight()/(height*6.0));
	if ( minlabels < 3 ) {
		minlabels = 3;
	}
	int maxlabels = (int)(_drawlim_lefty_graph.getHeight()/(height*2.0));
	if ( maxlabels < minlabels ) {
		maxlabels = minlabels*2;
	}

	// TODO (JTS - 2004-03-03)
	// logic for determining max and min number of labels is screwy when
	// creating new graphs sometimes.  Puts fewer labels in than look like should be in there.
	
	if (log_y_lefty) {
		if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (_regression_data != null) ) {
			// Old used data from the regression...
			//_ylabels = GRAxis.findLogLabels (_regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data...
			_ylabels_lefty = GRAxis.findLogLabels (_data_lefty_limits.getMinY(),_data_lefty_limits.getMaxY() );
		}
		else {
		    _ylabels_lefty = GRAxis.findLogLabels ( limitsLeftYAxis.getMinValue(),	limitsLeftYAxis.getMaxValue() );
		}
	}
	else if (log_xy_scatter) {
		if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (_regression_data != null) ) {
			// Old used data from the regression...
			//_ylabels = GRAxis.findLogLabels (_regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data...
			_ylabels_lefty = GRAxis.findLogLabels ( _data_lefty_limits.getMinY(), _data_lefty_limits.getMaxY() );
		}
		else {
		    _ylabels_lefty = GRAxis.findLogLabels ( limitsLeftYAxis.getMinValue(),	limitsLeftYAxis.getMaxValue() );
		}
	}
	else if ( __leftYAxisGraphType == TSGraphType.PERIOD ) {
		// Y-labels are whole numbers...
		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = false;
		List<TS> enabledTSList = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
		_ylabels_lefty = new double[enabledTSList.size()];
		for ( int i = 0; i < enabledTSList.size(); i++ ) {
			_ylabels_lefty[i] = i + 1;
		}
	}
    else if ( __leftYAxisGraphType == TSGraphType.RASTER ) {
        // Y-labels are whole numbers integer years from data period
        while ( minlabels >= 3 ) {
            _ylabels_lefty = GRAxis.findNLabels ( _data_lefty_limits.getMinY(), _data_lefty_limits.getMaxY(), true, minlabels, maxlabels );
            if ( _ylabels_lefty != null ) {
                break;
            }
            --minlabels;
        }
    }
	else {
	    // Linear.  Minimum and maximum number of labels as computed above...
		if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (_regression_data != null) ) {
			while ( minlabels >= 3 ) {
				// Make sure the max values properly account for the other axis...
				// Old...
				//_ylabels = GRAxis.findNLabels ( _regression_data.getMin2(), _regression_data.getMax2(),
					//false, minlabels, maxlabels );
				_ylabels_lefty = GRAxis.findNLabels ( _data_lefty_limits.getMinY(), _data_lefty_limits.getMaxY(),
					false, minlabels, maxlabels );
				if ( _ylabels_lefty != null ) {
					break;
				}
				--minlabels;
			}
		}
		else {	
		    while ( minlabels >= 3 ) {
				_ylabels_lefty = GRAxis.findNLabels ( limitsLeftYAxis.getMinValue(),limitsLeftYAxis.getMaxValue(), false, minlabels, maxlabels );
				if ( _ylabels_lefty != null ) {
					break;
				}
				--minlabels;
			}
		}
	}

	if ( (_ylabels_lefty == null) || (_ylabels_lefty.length == 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Unable to find left y-axis labels using " +
			minlabels + " to " + maxlabels + " labels.  Using end-point data values." );
		}
		_ylabels_lefty = new double [2];
		if ( __leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			_ylabels_lefty[0] = _data_lefty_limits.getMinY();
			_ylabels_lefty[1] = _data_lefty_limits.getMaxY();
			_data_lefty_limits = new GRLimits ( _max_lefty_data_limits.getMinX(), _ylabels_lefty[0],
				_max_lefty_data_limits.getMaxX(), _ylabels_lefty[1] );
		}
		else {
		    if ( log_y_lefty ) {
		        // No data points so put in .1 to 100
		        _ylabels_lefty = new double [4];
		        _ylabels_lefty[0] = .1;
                _ylabels_lefty[1] = 1.0;
                _ylabels_lefty[2] = 10.0;
                _ylabels_lefty[3] = 100.0;
		    }
		    else {
    		    _ylabels_lefty[0] = limitsLeftYAxis.getMinValue();
    			_ylabels_lefty[1] = limitsLeftYAxis.getMaxValue();
	         }
    		_data_lefty_limits = new GRLimits ( _start_date.toDouble(), _ylabels_lefty[0], _end_date.toDouble(), _ylabels_lefty[1] );
		}
	}
	else {	
		if (__leftYAxisGraphType == TSGraphType.PERIOD) {
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			_data_lefty_limits = new GRLimits ( _start_date.toDouble(), (getEnabledTSList(includeLeftYAxis,includeRightYAxis).size() + 1),
				_end_date.toDouble(), 0.0);
		}
		else if (__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			_data_lefty_limits = new GRLimits ( _max_lefty_data_limits.getMinX(), _ylabels_lefty[0],
				_max_lefty_data_limits.getMaxX(),	_ylabels_lefty[_ylabels_lefty.length - 1]);
		}
		else {	
			_data_lefty_limits = new GRLimits ( _start_date.toDouble(), _ylabels_lefty[0],
				_end_date.toDouble(), _ylabels_lefty[_ylabels_lefty.length - 1]);
		}
		
		if (Message.isDebugOn) {
			Message.printDebug(1, routine, _gtype + "Found " + _ylabels_lefty.length 
				+ " labels requesting " + minlabels + " to " + maxlabels);
		}
	}
	if ( !_is_reference_graph ) {
	    GRLimits dataLimits = new GRLimits(_data_lefty_limits);
	    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
	        dataLimits.reverseY();
	    }
	    if ( Message.isDebugOn ) {
	        Message.printDebug ( 1, routine, _gtype + "Y-axis labels (LeftYAxisDirection=" + __leftyDirection +
	            ") resulted in data limits " + dataLimits );
	    }
	    _da_lefty_graph.setDataLimits ( dataLimits );
	}
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < _ylabels_lefty.length; i++ ) {
			Message.printDebug ( 1, routine, _gtype + "_ylabel_lefty[" + i + "]=" + _ylabels_lefty[i] );
		}
	}
	
	// Right y-axis labels, only if right y-axis is enabled
	
	if ( _drawlim_righty_graph != null ) {
	
	boolean log_y_righty = false;
	propValue = _tsproduct.getLayeredPropValue(	"RightYAxisType", _subproduct, -1, false);
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_righty = true;
	}

	// Now get recompute the limits to be nice.  First do the Y axis...
	// The maximum number of labels is based on the font height and the
	// drawing area height.  However, in most cases, we want at least a
	// spacing of 3 times the font height, unless this results in less than 3 labels.
	// Format a label based on the font for the Y axis...

	fontname = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_righty_label, fontname, fontstyle, StringUtil.atod(fontsize) );
	label_extents = GRDrawingAreaUtil.getTextExtents( _da_righty_label, "astring", GRUnits.DEVICE );
	height = label_extents.getHeight();
	width = label_extents.getWidth();
	minlabels = (int)(_drawlim_righty_graph.getHeight()/(height*6.0));
	if ( minlabels < 3 ) {
		minlabels = 3;
	}
	maxlabels = (int)(_drawlim_righty_graph.getHeight()/(height*2.0));
	if ( maxlabels < minlabels ) {
		maxlabels = minlabels*2;
	}

	// TODO (JTS - 2004-03-03)
	// logic for determining max and min number of labels is screwy when
	// creating new graphs sometimes.  Puts fewer labels in than look like should be in there.
	
	if (log_y_righty) {
		_ylabels_righty = GRAxis.findLogLabels ( limitsRightYAxis.getMinValue(), limitsRightYAxis.getMaxValue() );
	}
	else if ( __rightYAxisGraphType == TSGraphType.PERIOD ) {
		// Y-labels are whole numbers...
		boolean includeLeftYAxis = false;
		boolean includeRightYAxis = true;
		List<TS> enabledTSList = getEnabledTSList(includeRightYAxis,includeRightYAxis);
		_ylabels_righty = new double[enabledTSList.size()];
		for ( int i = 0; i < enabledTSList.size(); i++ ) {
			_ylabels_righty[i] = i + 1;
		}
	}
	else {
	    // Linear.  Minimum and maximum number of labels as computed above...
		if ( limitsRightYAxis != null ) {
		    while ( minlabels >= 3 ) {
				_ylabels_righty = GRAxis.findNLabels ( limitsRightYAxis.getMinValue(),limitsRightYAxis.getMaxValue(), false, minlabels, maxlabels );
				if ( _ylabels_righty != null ) {
					break;
				}
				--minlabels;
			}
		}
	}

	if ( (_ylabels_righty == null) || (_ylabels_righty.length == 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Unable to find right y-axis labels using " +
			minlabels + " to " + maxlabels + " labels.  Using end-point data values." );
		}
		_ylabels_righty = new double [2];
	    if ( log_y_righty ) {
	        // No data points so put in .1 to 100
	        _ylabels_righty = new double [4];
	        _ylabels_righty[0] = .1;
            _ylabels_righty[1] = 1.0;
            _ylabels_righty[2] = 10.0;
            _ylabels_righty[3] = 100.0;
	    }
	    else {
	    	if ( limitsRightYAxis == null ) {
	    		_ylabels_righty[0] = 0.0;
	    		_ylabels_righty[1] = 1.0;
	    	}
	    	else {
			    _ylabels_righty[0] = limitsRightYAxis.getMinValue();
				_ylabels_righty[1] = limitsRightYAxis.getMaxValue();
	    	}
         }
		_data_righty_limits = new GRLimits ( _start_date.toDouble(), _ylabels_righty[0], _end_date.toDouble(), _ylabels_righty[1] );
	}
	else {	
		if (__rightYAxisGraphType == TSGraphType.PERIOD) {
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			_data_righty_limits = new GRLimits ( _start_date.toDouble(), (getEnabledTSList(includeRightYAxis,includeRightYAxis).size() + 1),
				_end_date.toDouble(), 0.0);
		}
		else {	
			_data_righty_limits = new GRLimits ( _start_date.toDouble(), _ylabels_righty[0],
				_end_date.toDouble(), _ylabels_righty[_ylabels_righty.length - 1]);
		}
		
		if (Message.isDebugOn) {
			Message.printDebug(1, routine, _gtype + "Found " + _ylabels_righty.length 
				+ " labels requesting " + minlabels + " to " + maxlabels);
		}
	}
	if ( !_is_reference_graph ) {
	    GRLimits dataLimits = new GRLimits(_data_righty_limits);
	    if ( __rightyDirection == GRAxisDirectionType.REVERSE ) {
	        dataLimits.reverseY();
	    }
	    if ( Message.isDebugOn ) {
	        Message.printDebug ( 1, routine, _gtype + "Y-axis labels (RightYAxisDirection=" + __rightyDirection +
	            ") resulted in data limits " + dataLimits );
	    }
	    _da_righty_graph.setDataLimits ( dataLimits );
	}
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < _ylabels_righty.length; i++ ) {
			Message.printDebug ( 1, routine, _gtype + "_ylabel_righty[" + i + "]=" + _ylabels_righty[i] );
		}
	}
	}

	// X axis labels, shared between left and right y-axis
	//
	// If normal plot, based on the dates for the current zoom.
	// If a scatter plot, based on data limits.
	// If a duration plot, based on 0 - 100 percent.
	// If a raster plot, based on days or months in year

	fontname = _tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontName", _subproduct, -1, false );
	fontsize = _tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontSize", _subproduct, -1, false );
	fontstyle = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, fontname, fontstyle,	StringUtil.atod(fontsize) );
	
	// More complex plots only can display using left y-axis graph
	if ( __leftYAxisGraphType == TSGraphType.DURATION ) {
		// Limits are 0 to 100.0..
		String maxstring = StringUtil.formatString(	(double)100.0, "%.0f");
		label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(_drawlim_lefty_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(_drawlim_lefty_graph.getHeight()/(width*1.5));
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
			_xlabels[0] = _data_lefty_limits.getMinX();
			_xlabels[1] = _data_lefty_limits.getMaxX();
		}
		_data_lefty_limits = new GRLimits ( _xlabels[0], _ylabels_lefty[0],
			_xlabels[_xlabels.length - 1], _ylabels_lefty[_ylabels_lefty.length - 1] );
		if ( !_is_reference_graph ) {
		    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
		        GRLimits dataLimits = new GRLimits(_data_lefty_limits);
		        _da_lefty_graph.setDataLimits ( dataLimits.reverseY() );
		    }
		    else {
		        _da_lefty_graph.setDataLimits ( _data_lefty_limits );
		    }
		}
		return;
	}
	else if ( __leftYAxisGraphType == TSGraphType.RASTER ) {
        // Limits are always the month boundaries
        _xlabels = new double[13];
        boolean includeLeftYAxis = true;
        boolean includeRightYAxis = false;
        List<TS> tslist = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
        if ( tslist.size() == 0 ) {
            return;
        }
        TS ts = tslist.get(0);
        int intervalBase = ts.getDataIntervalBase();
        if ( intervalBase == TimeInterval.DAY ) {
            DateTime d = new DateTime();
            d.setYear(2000); // A leap year
            d.setDay(1);
            for ( int ix = 1; ix <= 12; ix++ ) {
                d.setMonth(ix);
                _xlabels[ix - 1] = TimeUtil.dayOfYear(d);
            }
            // Add end value for last day in year
            d.setDay(TimeUtil.numDaysInMonth(d));
            _xlabels[_xlabels.length - 1] = TimeUtil.dayOfYear(d);
        }
        else if ( intervalBase == TimeInterval.MONTH ) {
            for ( int ix = 1; ix <= 13; ix++ ) {
                _xlabels[ix - 1] = ix;
            }
        }
        /* TODO SAM 2013-07-21 Decide if this is needed given special handling of labels
        String maxstring = "MMM"; // 3-letter month abbreviation
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
                Message.printDebug ( 1, routine, _gtype + "Unable to find X labels using " +
                minlabels + " to " + maxlabels + " labels.  Using data values." );
            }
            _xlabels = new double [2];
            _xlabels[0] = _data_limits.getMinX();
            _xlabels[1] = _data_limits.getMaxX();
        }
        */
        _data_lefty_limits = new GRLimits ( _xlabels[0], _ylabels_lefty[0],
            _xlabels[_xlabels.length - 1], _ylabels_lefty[_ylabels_lefty.length - 1] );
        _da_lefty_graph.setDataLimits ( _data_lefty_limits );
        return;
    }
	else if ( __leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
		// Labels are based on the _data_limits...
		// Need to check precision for units but assume .1 for now...
		String maxstring = StringUtil.formatString(	_data_lefty_limits.getMaxX(), "%." + _xaxis_precision + "f");
		label_extents = GRDrawingAreaUtil.getTextExtents( _da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(_drawlim_lefty_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(_drawlim_lefty_graph.getHeight()/(width*1.5));

		propValue = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
		boolean asLog = false;
		if ((propValue != null) && propValue.equalsIgnoreCase("Log")){
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
				        _data_lefty_limits.getMinX(),	_data_lefty_limits.getMaxX(),	false, minlabels, maxlabels );
				}
				else {
				    _xlabels = GRAxis.findNLabels (	_data_lefty_limits.getMinX(),
					_data_lefty_limits.getMaxX(),	false, minlabels, maxlabels );
				}
			}
			else {
			    // Use the limits of the time series data...
				_xlabels = GRAxis.findNLabels (	limitsLeftYAxis.getMinValue(),limitsLeftYAxis.getMaxValue(),
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
			_xlabels[0] = limitsLeftYAxis.getMinValue();
			_xlabels[1] = limitsLeftYAxis.getMaxValue();
		}
		_data_lefty_limits = new GRLimits ( _xlabels[0], _ylabels_lefty[0],
					_xlabels[_xlabels.length - 1], _ylabels_lefty[_ylabels_lefty.length - 1] );
		if ( !_is_reference_graph ) {
		    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
                GRLimits dataLimits = new GRLimits(_data_lefty_limits);
                _da_lefty_graph.setDataLimits ( dataLimits.reverseY() );
            }
            else {
                _da_lefty_graph.setDataLimits ( _data_lefty_limits );
            }
		}
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
	int nlabels = (int)(_drawlim_lefty_graph.getWidth()/(width*2.0));
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Number of X labels is " + nlabels );
	}

	// Number of months in data...

	int nmonths = _end_date.getAbsoluteMonth() - _start_date.getAbsoluteMonth() + 1;
	List<Double> x_axis_labels_temp = new ArrayList<Double>(10);

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
		for ( int i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.add ( new Double(date.toDouble() ) );
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
		for ( int i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, _gtype + "Label is for " + date.toString() );
			}
			x_axis_labels_temp.add (	new Double(date.toDouble() ) );
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
		for ( int i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.add ( new Double(date.toDouble() ) );
			date.addDay ( delta );
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_HOUR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Determining hour labels" );
		}
		
		// Could be irregular data...
		int nhours = 0;

		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = true;
		TS ts = getFirstEnabledTS(includeLeftYAxis,includeRightYAxis);
		// Try to find first non-null time series
		int nts = __tslist.size();
		for ( int its = 0; its < nts; ++its ) {
	        ts = __tslist.get(its);
	        if ( ts != null ) {
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

		for (int i = 0; date.lessThanOrEqualTo(_end_date); i++ ) {
			x_axis_labels_temp.add( new Double(date.toDouble()));
			date.addHour(delta);
		}
	}
	else if ( _xaxis_date_precision == DateTime.PRECISION_MINUTE ) {
		// Could be irregular data...
		int nminutes = 0;

		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = true;
		TS ts = getFirstEnabledTS(includeLeftYAxis,includeRightYAxis);
		if (ts == null) {
			ts = __tslist.get(0);
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

		// delta was being rounded to 0 in some cases above and that was causing infinite loops below.
		if (delta == 0) {
			delta = 60;
		}
		
		date = new DateTime(_start_date);
		for ( int i = 0; date.lessThanOrEqualTo(_end_date); i++) {
			x_axis_labels_temp.add( new Double(date.toDouble()));
			date.addMinute(delta);
		}
	}

	// Now convert list to array of labels...

	int size = x_axis_labels_temp.size();
	_xlabels = new double[size];
	for ( int i = 0; i < size; i++ ) {
		_xlabels[i] = x_axis_labels_temp.get(i).doubleValue();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,_gtype + "_xlabel[" + i + "]=" + _xlabels[i] );
		}
	}
}

/**
Determine and set the precision for the X axis.  The precision is set to the
most detailed time series data interval.  Call this in the constructor so the
precision can be used in setDrawingLimits().
This information is not used for scatter plots or other plots that don't use date axes.
*/
private void computeXAxisDatePrecision ()
{	// Initialize to largest value...
	_xaxis_date_precision = TimeInterval.YEAR;
	if ( __tslist == null ) {
		return;
	}

	// Loop through and find the smallest time unit from the time series intervals...

	int size = __tslist.size();
	TS ts = null;
	int interval = 0;
	DateTime date = null;
	for (int i = 0; i < size; i++) {
		ts = __tslist.get(i);
		if ((ts == null) || !ts.getEnabled() || !isTSEnabled(i)) {
			continue;
		}

		try {
		    // Set the axis precision to the smallest time interval of any data time series...
			interval = ts.getDataIntervalBase();
			if ( interval == TimeInterval.IRREGULAR ) {
				// Use the precision from the first date in the data...
				date = ts.getDate1();
				if ( date == null ) {
					continue;
				}
				if ( date.getPrecision() == DateTime.PRECISION_MINUTE ) {
					interval = TimeInterval.MINUTE;
				}
				else if ( date.getPrecision() == DateTime.PRECISION_HOUR ) {
					interval = TimeInterval.HOUR;
				}
				else if ( date.getPrecision() == DateTime.PRECISION_DAY ) {
					interval = TimeInterval.DAY;
				}
				else if ( date.getPrecision() == DateTime.PRECISION_MONTH ) {
					interval = TimeInterval.MONTH;
				}
				else if ( date.getPrecision() == DateTime.PRECISION_YEAR ) {
					interval = TimeInterval.YEAR;
				}
				else {
				    interval = TimeInterval.MINUTE;
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
	// Now convert the precision to a real DateTime precision...
	if ( _xaxis_date_precision == TimeInterval.YEAR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is year." );
		}
		_xaxis_date_precision = DateTime.PRECISION_YEAR;
	}
	else if ( _xaxis_date_precision == TimeInterval.MONTH ) {
		_xaxis_date_precision = DateTime.PRECISION_MONTH;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is month." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.DAY ) {
		_xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is day." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.HOUR ) {
		_xaxis_date_precision = DateTime.PRECISION_HOUR;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is hour." );
		}
	}
	else if ( _xaxis_date_precision == TimeInterval.MINUTE ) {
		_xaxis_date_precision = DateTime.PRECISION_MINUTE;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is minute." );
		}
	}
	else {
	    // Default to day...
		_xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", _gtype + "X axis date precision is day." );
		}
	}
}

/**
Perform additional analysis on the data if other than a basic graph is indicated.
After the analysis, the data limits are recomputed (this is done for simple data also).
@param graphType the graph type
*/
private void doAnalysis ( TSGraphType graphType )
{	String routine = "TSGraph.doAnalysis";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Analyzing time series for " + __leftYAxisGraphType +
		    " graph - will produced derived data if necessary for output.");
	}
	List<TS> tslist = getTSList();
	if ( graphType == TSGraphType.XY_SCATTER ) {
		// Do a linear regression analysis.  The first time series is
		// considered the independent (X) and the remaining time series
		// the Y.  If a regression fails, set it to null in the vector
		// so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = tslist.size() - 1;
		if ( nreg > 0 ) {
			_regression_data = new Vector ( nreg );
		}
		// Set the regression properties.  These may be changed by the properties interface.
		boolean analyzeForFilling = false;
		if (_tsproduct.getLayeredPropValue ( "XYScatterAnalyzeForFilling", _subproduct, -1, false ).
		    equalsIgnoreCase("True") ) {
		    analyzeForFilling = true;
		}
		DateTime dependentAnalysisStart = null;
		String propValue = _tsproduct.getLayeredPropValue ( "XYScatterDependentAnalysisPeriodStart", _subproduct, -1, false );
		if ( TimeUtil.isDateTime(propValue) ) {
		    try {
		        dependentAnalysisStart = DateTime.parse(propValue);
		    }
		    catch ( Exception e ) {
		        // Should not happen
		    }
		}
		DateTime dependentAnalysisEnd = null;
		propValue = _tsproduct.getLayeredPropValue ( "XYScatterDependentAnalysisPeriodEnd", _subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                dependentAnalysisEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
        DateTime independentAnalysisStart = null;
		propValue = _tsproduct.getLayeredPropValue ( "XYScatterIndependentAnalysisPeriodStart", _subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                independentAnalysisStart = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
        DateTime independentAnalysisEnd = null;
		propValue = _tsproduct.getLayeredPropValue ( "XYScatterIndependentAnalysisPeriodEnd", _subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                independentAnalysisEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
        DateTime fillStart = null;
        propValue = _tsproduct.getLayeredPropValue ( "XYScatterFillPeriodStart", _subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                fillStart = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
        DateTime fillEnd = null;
        propValue = _tsproduct.getLayeredPropValue ( "XYScatterFillPeriodEnd", _subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                fillEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen
            }
        }
		Double intercept = null;
		propValue = _tsproduct.getLayeredPropValue ( "XYScatterIntercept", _subproduct, -1, false );
		if ( StringUtil.isDouble(propValue)) {
		    intercept = Double.parseDouble(propValue);
		}
		RegressionType analysisMethod = RegressionType.valueOfIgnoreCase(
		    _tsproduct.getLayeredPropValue ( "XYScatterMethod", _subproduct, -1, false ) );
		int [] analysisMonths = StringUtil.parseIntegerSequenceArray(
		    _tsproduct.getLayeredPropValue ( "XYScatterMonth", _subproduct, -1, false ), ", ", StringUtil.DELIM_SKIP_BLANKS );
		NumberOfEquationsType numberOfEquations = NumberOfEquationsType.valueOfIgnoreCase(
		    _tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", _subproduct, -1, false ) );
		DataTransformationType transformation = DataTransformationType.valueOfIgnoreCase(
		    _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false ) );

		TSRegression regressionData = null;
		for ( int i = 1; i <= nreg; i++ ) {
			// The first time series [0] is always the dependent
			// time series and time series [1+] are the independent for each relationship...
			ts0 = tslist.get(0);
			ts = tslist.get(i);
			try {
			    regressionData = new TSRegression ( ts, ts0,
			            analyzeForFilling,
			            analysisMethod,
			            intercept, numberOfEquations, analysisMonths,
			            transformation,
			            null, // Default value for <= 0 data value when log transform
	                    null, // Don't specify the confidence interval
			            dependentAnalysisStart, dependentAnalysisEnd,
			            independentAnalysisStart, independentAnalysisEnd,
			            fillStart, fillEnd );
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, "Error performing regression for TS [" + (i - 1) + "]" );
				Message.printWarning ( 3, routine, e );
				regressionData = null;
			}
			// Always add something...
			_regression_data.add ( regressionData );
		}
	}
	else if ( graphType == TSGraphType.DOUBLE_MASS ) {
		// Do a double mass analysis so the information is available.
		// TODO SAM 2007-05-09 Need to enable?
		//TS ts0 = null;
		//TS ts1 = null;
		//if ( tslist.size() == 2 ) {
		//	ts0 = __tslist.elementAt(0);
		//	ts1 = __tslist.elementAt(1);
		//}
/*
		if ( (ts0 != null) && (ts1 != null) ) {
			PropList rprops = new PropList("doublemass");
			try {
			    _double_mass_data = new TSDoubleMass ( ts0, ts1, rprops );
			}
			catch ( Exception e ) {
				_title = "Double Mass - Unable to analyze";
			}
		}
*/
	}
	else if ( (graphType == TSGraphType.DURATION) && (tslist.size() != 0) ) {
		// Generate TSDurationAnalysis for each time series...
		int size = tslist.size();
		_duration_data = new Vector ( size );
		TSDurationAnalysis da = null;
		for ( int i = 0; i < size; i++ ) {
			try {
			    da = new TSDurationAnalysis ( tslist.get(i) );
				_duration_data.add ( da );
			}
			catch ( Exception e ) {
				_duration_data.add ( null );
			}
		}
	}
	else if (graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		// Do a linear regression analysis.  The first time series is
		// considered the independent (X) and the remaining time series
		// the Y.  If a regression fails, set it to null in the vector
		// so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = tslist.size() - 1;
		if (nreg > 0 ) {
			_regression_data = new Vector(nreg);
		}

		TSRegression regressionData = null;
		for (int i = 1; i <= nreg; i++) {
			// The first time series [0] is always the independent
			// time series and time series [1+] are the dependent for each relationship...
			ts0 = tslist.get(0);
			ts = tslist.get(i);
			try {
			    // Pick reasonable defaults for the regression analysis - they can be changed in the
			    // properties interface
				regressionData = new TSRegression( ts0, ts,
				    false, // Analyze for filling
				    RegressionType.MOVE2,
                    null, // intercept
                    NumberOfEquationsType.ONE_EQUATION,
                    null, //analysisMonths,
                    DataTransformationType.NONE,
                    null, // Default value for <= 0 data value when log transform
                    null, // Don't specify the confidence interval
                    null, //dependentAnalysisStart,
                    null, //dependentAnalysisEnd,
                    null, //independentAnalysisStart,
                    null, //independentAnalysisEnd,
                    null, // FillStart
                    null ); // FillEnd
				// FIXME SAM 2009-08-30 These were being set previously for TSRegression but seem to have no
				// effect:
		        //rprops.set("MinimumDataCount", "1");
		        //rprops.set("MinimumR", "0");
		        //rprops.set("BestFit", "SEP");
		        //rprops.set("OutputFile", "c:\\temp\\output.regress");
				regressionData.createPredictedTS();
			}
			catch (Exception e) {
				Message.printWarning(3, routine, "Error performing regression for TS [" + (i - 1) + "]");
				Message.printWarning(3, routine, e);
				regressionData = null;
			}

			// Always add something...
			_regression_data.add(regressionData);
		}
	}
    else if ( graphType == TSGraphType.AREA_STACKED ) {
        doAnalysisAreaStacked();
    }
	// Compute the maximum data limits using the analysis objects that have been created...
	computeDataLimits ( true );
}

/**
Analyze the time series data for the stacked area graph.
This consists of creating new time series for each original time series, where the new time series
are the sum of the previous time series.  The resulting derived time series will be drawn as area
graphs back to front.  Any original time series that do not have a graph type of stacked area, or null
time series, are set as null in the derived data to maintain the order in the time series list.
*/
private void doAnalysisAreaStacked ()
{   String routine = getClass().getSimpleName() + ".doAnalysisAreaStacked";
	// Analysis is only enabled for left y-axis
	boolean includeLeftYAxis = true;
	boolean includeRightYAxis = false;
    List<TS> tslist = getEnabledTSList(includeLeftYAxis,includeRightYAxis);
    if ( tslist.size() == 0 ) {
        // None are explicitly enabled so process all
        // TODO SAM 2010-11-22 Need to evaluate enabled 
        tslist = getTSList();
    }
    if ( tslist.size() == 0 ) {
        // No time series to graph
        return;
    }
    // The original time series are considered to be incremental time series and are
    // used to create corresponding total time series, which will be graphed with simple area graphs
    // The first derived time series is a clone of the first actual time series
    List<TS> derivedTSList = new Vector();
    // Get the overall period for the time series - all stacked time series will have the same period
    TSLimits limits = null;
    try {
        limits = TSUtil.getPeriodFromTS(tslist.get(0), tslist, TSUtil.MAX_POR);
    }
    catch ( Exception e ) {
        
    }
    int its = -1; // Counter for time series being processed
    TS newtsPrev = null;
    for ( TS ts : tslist ) {
        ++its;
        // Time series may be null if a data problem...
        if ( ts == null ) {
            Message.printStatus(2,routine,"Time series [" + its + "] is null...set to null in derived list." );
            derivedTSList.add(null);
            continue;
        }
        // If the time series graph type is not stacked area, then skip it as a derived time
        // series - it will be drawn separately
        TSGraphType tsGraphType = getTimeSeriesGraphType(TSGraphType.AREA_STACKED, its);
        if ( tsGraphType != TSGraphType.AREA_STACKED ) {
            Message.printStatus(2,routine,"Time series [" + its + "] graph type (" + tsGraphType +
                ") is not stacked area type...set to null in derived list." );
            derivedTSList.add(null);
            continue;
        }
        // If the first time series clone.  Otherwise copy the previous and add to the value
        TS newts = null;
        if ( newtsPrev == null ) {
            newts = (TS)ts.clone();
            try {
                newts.changePeriodOfRecord(limits.getDate1(), limits.getDate2());
            }
            catch ( Exception e ) {
                Message.printWarning(3,routine,
                    "Error changing period of record for stacked area time series [" + its + "]..." +
                    "setting to null in derived list (" + e + ")." );
                derivedTSList.add(null);
                continue;
            }
        }
        else {
            newts = (TS)ts.clone();
            try {
                newts.changePeriodOfRecord(limits.getDate1(), limits.getDate2());
            }
            catch ( Exception e ) {
                Message.printWarning(3,routine,
                    "Error changing period of record for stacked area time series [" + its + "]..." +
                    "setting to null in derived list (" + e + ")." );
                derivedTSList.add(null);
                continue;
            }
            try {
                TSUtil.add(newts, newtsPrev);
            }
            catch ( Exception e ) {
                Message.printWarning(3, routine, "Error adding time series for stacked area graph [" + its + "]..." +
                		"setting to null in derived list (" + e + ").");
                derivedTSList.add(null);
                continue;
            }
        }
        // Now add to the derived time series list and set the description back to the original
        // (don't want add, etc. in description)
        newts.setDescription ( ts.getDescription() );
        derivedTSList.add ( newts );
        Message.printStatus(2,routine,"Time series [" + its + "] added to derived list." );
        // Keep track of the previous non-null time series, since each in stacked graph is an
        // increment of the previous
        newtsPrev = newts;
    }
    Message.printStatus(2, routine, "Created " + derivedTSList.size() + " time series in derived list.");
    setDerivedTSList ( derivedTSList );
}

/**
Draws any annotations on the graph.  This method can be called multiple times, 
once with false before drawing data and then with true after data have been drawn.
@param drawingStepType indicates the step during drawing that should be matched.
If the annotation "Order" property does not match the drawing step then the annotation will not be drawn.  
*/
private void drawAnnotations( TSProduct tsproduct, int subproduct,
	GRDrawingArea daLeftYAxisGraph, GRDrawingArea daRightYAxisGraph, TSGraphDrawingStepType drawingStepType) {
	if (_is_reference_graph) {
		return;
	}
	String routine = getClass().getSimpleName() + ".drawAnnotations(" + drawingStepType + ")";
	int na = tsproduct.getNumAnnotations(subproduct);
	PropList annotation = null;
	String s = null;
	String type = null;
	String points = null;
	List<String> pointsV = null;
	String point = null;
	List<String> pointV = null;
	boolean valid = false;
	boolean niceSymbols = true;
	boolean isSymbol = false;

	String prop_value = tsproduct.getLayeredPropValue("SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	// Set clipping for both Y axes
	// TODO SAM 2016-10-23 Evaluate whether annotations should be allowed to extend outside graph
	// - this may be desirable for symbols, in particular becaue they get cut off
	// Left y-axis
	Shape clip = GRDrawingAreaUtil.getClip(daLeftYAxisGraph);	
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, daLeftYAxisGraph.getDataLimits());
	// Right y-axis
	// TODO SAM 2016-10-23 Figure out how to clip in the proper sequence
	//clip = GRDrawingAreaUtil.getClip(daRightYAxisGraph);	
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, daRightYAxisGraph.getDataLimits());

	boolean drawLeftYAxis = true; // true=left axis, false=right axis 
	for (int iatt = 0; iatt < na; iatt++) {
		annotation = new PropList("Annotation " + iatt);
		valid = true;
		drawLeftYAxis = true; // Default is left y-axis
		type = tsproduct.getLayeredPropValue("ShapeType", subproduct, iatt, false, true);
		if (type == null) {
			Message.printWarning(2, routine, "Null shapetype");
			valid = false;
		}
		else if (type.equalsIgnoreCase("Line")) {
			points = tsproduct.getLayeredPropValue("Points", subproduct, iatt, false, true);
			if (points == null) {
				valid = false;
				Message.printWarning(2, routine, "Null points");
			}
			else {
				pointsV = StringUtil.breakStringList(points, ",", 0);
				if (pointsV == null || pointsV.size() != 4) {
					valid = false;
					Message.printWarning(2, routine, "Invalid points declaration");
				}
			}			
		}
		else if (type.equalsIgnoreCase("Rectangle")) {
			points = tsproduct.getLayeredPropValue("Points", subproduct, iatt, false, true);
			if (points == null) {
				valid = false;
				Message.printWarning(2, routine, "Null points");
			}
			else {
				pointsV = StringUtil.breakStringList(points, ",", 0);
				if (pointsV == null || pointsV.size() != 4) {
					valid = false;
					Message.printWarning(2, routine, "Invalid points declaration");
				}
			}			
		}
		else if (type.equalsIgnoreCase("Symbol")) {
			point = tsproduct.getLayeredPropValue("Point", subproduct, iatt, false, true);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point");
			}
			else {
				pointV = StringUtil.breakStringList(point, ",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, "Invalid point declaration");
				}
			}				
			isSymbol = true;
		}
		else if (type.equalsIgnoreCase("Text")) {
			point = tsproduct.getLayeredPropValue("Point", subproduct, iatt, false, true);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point");
			}
			else {
				pointV = StringUtil.breakStringList(point, ",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, "Invalid point declaration");
				}
			}
		}
		else {
			valid = false;
		}

		if (!valid) {
			// some error encountered in checkProperties for this so skip
			Message.printWarning(2, routine, "Invalid annotation: " + (subproduct + 1) + "." + (iatt + 1));
			continue;
		}

		s = tsproduct.getLayeredPropValue("Order", subproduct, iatt, false, true);

		if (s == null) {
			// default to on top
			s = "OnTopOfData";
		}
		
		if ((drawingStepType == TSGraphDrawingStepType.BEFORE_DATA) && !s.equalsIgnoreCase("BehindData")) {
			// Current drawing step is before data but that does not match the annotation
			continue;
		}
		if ((drawingStepType == TSGraphDrawingStepType.AFTER_DATA) && !s.equalsIgnoreCase("OnTopOfData")) {
			// Current drawing step is after data but that does not match the annotation
			continue;
		}
		if ((drawingStepType == TSGraphDrawingStepType.BEFORE_BACK_AXES) && !s.equalsIgnoreCase("BehindAxes")) {
			// Current drawing step is before drawing back axes but that does not match the annotation
			continue;
		}
		
		// If the annotation uses an annotation table, have to loop through the table
		
		String annotationTableID = tsproduct.getLayeredPropValue("AnnotationTableID", subproduct, iatt, false, true);
		
		if ( (annotationTableID == null) || annotationTableID.isEmpty() ) {
			// Simple annotations - one shape per annotation
			// Properties for all annotations
			String yAxis = null;
			annotation.set("Color", tsproduct.getLayeredPropValue("Color", subproduct, iatt, false, true));
			annotation.set("Order", tsproduct.getLayeredPropValue("Order", subproduct, iatt, false, true));
			annotation.set("ShapeType", tsproduct.getLayeredPropValue("ShapeType", subproduct, iatt, false, true));
			annotation.set("XAxisSystem", tsproduct.getLayeredPropValue("XAxisSystem", subproduct, iatt, false, true));
			annotation.set("YAxis", tsproduct.getLayeredPropValue("YAxis", subproduct, iatt, false, true));
			// Now check to see which axis annotations should be drawn on
			yAxis = tsproduct.getLayeredPropValue("YAxisSystem", subproduct, iatt, false, true);
			if ( (yAxis != null) && !yAxis.isEmpty() && yAxis.equalsIgnoreCase("Right") ) {
				drawLeftYAxis = false;
			}
			annotation.set("XFormat", tsproduct.getLayeredPropValue("XFormat", subproduct, iatt, false, true)); // Whether axis is number, DateTime, always defaults?
			annotation.set("YFormat", tsproduct.getLayeredPropValue("YFormat", subproduct, iatt, false, true));
			// Properties for shape type
			if (type.equalsIgnoreCase("Line")) {
				// Properties for Line shape type
				annotation.set("LineStyle", tsproduct.getLayeredPropValue("LineStyle", subproduct, iatt, false, true));
				annotation.set("LineWidth", tsproduct.getLayeredPropValue("LineWidth", subproduct, iatt, false, true));
				annotation.set("Points", tsproduct.getLayeredPropValue("Points", subproduct, iatt, false, true));
			}
			else if (type.equalsIgnoreCase("Rectangle")) {
				// Properties for Rectangle shape type
				//annotation.set("OutlineColor", tsproduct.getLayeredPropValue("OutlineColor", subproduct, iatt, false, true)); // Future enhancement
				annotation.set("Points", tsproduct.getLayeredPropValue("Points", subproduct, iatt, false, true));
			}
			else if (type.equalsIgnoreCase("Symbol")) {
				// Properties for Symbol shape type
				annotation.set("OutlineColor", tsproduct.getLayeredPropValue("OutlineColor", subproduct, iatt, false, true));
				annotation.set("Point", tsproduct.getLayeredPropValue("Point", subproduct, iatt, false, true));
				annotation.set("SymbolSize", tsproduct.getLayeredPropValue("SymbolSize", subproduct, iatt, false, true));
				annotation.set("SymbolStyle", tsproduct.getLayeredPropValue("SymbolStyle", subproduct, iatt, false, true));
				annotation.set("SymbolPosition", tsproduct.getLayeredPropValue("SymbolPosition", subproduct, iatt, false, true));
			}
			else if (type.equalsIgnoreCase("Text")) {
				// Properties for Text shape type
				annotation.set("FontName", tsproduct.getLayeredPropValue("FontName", subproduct, iatt, false, true));
				annotation.set("FontSize", tsproduct.getLayeredPropValue("FontSize", subproduct, iatt, false, true));
				annotation.set("FontStyle", tsproduct.getLayeredPropValue("FontStyle", subproduct, iatt, false, true));
				annotation.set("Point", tsproduct.getLayeredPropValue("Point", subproduct, iatt, false, true));
				annotation.set("Text", tsproduct.getLayeredPropValue("Text", subproduct, iatt, false, true));
				annotation.set("TextPosition", tsproduct.getLayeredPropValue("TextPosition", subproduct, iatt, false, true));
			}
			
			if (isSymbol && niceSymbols) {
				if ( drawLeftYAxis ) {
					GRDrawingAreaUtil.setDeviceAntiAlias(daLeftYAxisGraph, true);
				}
				else {
					GRDrawingAreaUtil.setDeviceAntiAlias(daRightYAxisGraph, true);
				}
			}
	
			if ( drawLeftYAxis ) {
				GRDrawingAreaUtil.drawAnnotation(daLeftYAxisGraph, annotation);
			}
			else {
				GRDrawingAreaUtil.drawAnnotation(daRightYAxisGraph, annotation);
			}
		}
		else {
			// Annotations specified using an annotation table.
			// TODO SAM 2016-10-23 need to enable
			// Lookup annotation table
			// Loop through records.
			// Draw each annotation
			// Optimize to not draw if outside visible graph
		}
		
		if (isSymbol && niceSymbols) {
			if ( drawLeftYAxis ) {
				GRDrawingAreaUtil.setDeviceAntiAlias(daLeftYAxisGraph, false);
			}
			else {
				GRDrawingAreaUtil.setDeviceAntiAlias(daRightYAxisGraph, false);
			}
		}		
	}

	// Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds
	// Left y-axis
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, (Shape)null);
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, clip);	
	// Right y-axis
	// TODO SAM 2016-10-23 Figure out how to clip in the proper sequence
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, (Shape)null);
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, clip);
}

/**
Draw the axes features that should be behind the plotted data, including the surrounding box, and the grid lines.
Call drawAxesFront() to draw features that are to be on top of the graph (tic marks and labels).
*/
private void drawAxesBack ( )
{	// Previous code used the main _da_graph to draw the axis labels.  Now
	// that label areas are separate drawing areas, draw the labels in those
	// drawing areas.  To make sure the drawing limits are OK, set to the _data_limits values here...

	_datalim_lefty_label = new GRLimits ( _da_lefty_label.getDataLimits() );
	// TODO SAM 2013-01-22 Remove the following if code tests out
	//_datalim_lefty_label.setBottomY ( _data_limits.getBottomY() );
	//_datalim_lefty_label.setTopY ( _data_limits.getTopY() );
	// This handles if the axis is reversed
	_datalim_lefty_label.setBottomY ( _da_lefty_graph.getDataLimits().getBottomY() );
    _datalim_lefty_label.setTopY ( _da_lefty_graph.getDataLimits().getTopY() );
	_da_lefty_label.setDataLimits ( _datalim_lefty_label );

	_datalim_righty_label = new GRLimits(_da_righty_label.getDataLimits());
	_datalim_righty_label.setBottomY ( _data_lefty_limits.getBottomY() );
	_datalim_righty_label.setTopY ( _data_lefty_limits.getTopY() );
	_da_righty_label.setDataLimits ( _datalim_righty_label );

	_datalim_bottomx_label = new GRLimits(_da_bottomx_label.getDataLimits());
	_datalim_bottomx_label.setLeftX ( _data_lefty_limits.getLeftX() );
	_datalim_bottomx_label.setRightX ( _data_lefty_limits.getRightX() );
	_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );

	if ( !_is_reference_graph ) {
		drawXAxisGrid();
		drawYAxisGrid();
	}
	drawOutlineBox();
}

// TODO SAM 2016-10-23 Need to break up the following method into components
// - for now work on refactoring to remove globals
/**
Draw axes features that show in front of the plotted data.  Currently this
includes the axes tic marks, titles, and labels.  The tic marks are currently always drawn in black.
@param tsproduct time series product containing properties
@param subproduct subproduct (graph) number (0+)
*/
private void drawAxesFront ( TSProduct tsproduct, int subproduct,
	GRJComponentDrawingArea daMainTitle,
	boolean drawLeftYAxisLabels, TSGraphType leftYAxisGraphType,
	GRJComponentDrawingArea daLeftYAxisGraph, GRJComponentDrawingArea daLeftYAxisTitle, GRJComponentDrawingArea daLeftYAxisLabel,
	GRLimits datalimLeftYAxisGraph, GRLimits datalimLeftYAxisTitle, GRLimits datalimLeftYAxisLabel,
	double ylabelsLeftYAxis[], int leftYAxisPrecision,
	boolean drawRightYAxisLabels, TSGraphType rightYAxisGraphType,
	GRJComponentDrawingArea daRightYAxisGraph, GRJComponentDrawingArea daRightYAxisTitle, GRJComponentDrawingArea daRightYAxisLabel,
	GRLimits datalimRightYAxisGraph, GRLimits datalimRightYAxisTitle, GRLimits datalimRightYAxisLabel,
	double ylabelsRightYAxis[], int rightYAxisPrecision,
	// boolean drawBottomXAxisLabels, // TODO SAM 2016-10-23 Enble in the future
	GRJComponentDrawingArea daBottomXAxisTitle, GRJComponentDrawingArea daBottomXAxisLabel,
	GRLimits datalimBottomXAxisTitle, GRLimits datalimBottomXAxisLabel,
	double xlabelsBottomXAxis[])
{	if ( _is_reference_graph ) {
		return;
	}

	// Used throughout...

	String prop_value = null;
	String title;
	String rotation;
	String fontname;
	String fontsize;
	String fontstyle;
	
	// Draw text nice using anti-aliasing
	GRDrawingAreaUtil.setDeviceAntiAlias( daMainTitle, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daLeftYAxisLabel, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daRightYAxisLabel, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daBottomXAxisLabel, true);

	boolean leftYAxisLogY = false;
	prop_value = tsproduct.getLayeredPropValue ( "LeftYAxisType", subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		leftYAxisLogY = true;
	}

	prop_value = tsproduct.getLayeredPropValue ( "XYScatterTransformation", subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		leftYAxisLogY = false;
	}	

	// Left Y Axis labels, and tics...

	double [] xlabels = new double[2];
	xlabels[0] = datalimLeftYAxisLabel.getLeftX();
	xlabels[1] = datalimLeftYAxisLabel.getRightX();

	xlabels = null;
	fontname = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontStyle", subproduct, -1, false );
	String yaxisDir = tsproduct.getLayeredPropValue ( "LeftYAxisDirection", subproduct, -1, false );
    boolean yaxisDirReverse = false;
    if ( (yaxisDir != null) && yaxisDir.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
        yaxisDirReverse = true;
    }
	GRDrawingAreaUtil.setFont ( daLeftYAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.setColor ( daLeftYAxisLabel, GRColor.black );
	if ( leftYAxisLogY ) {
		// Only draw major labels...
		double [] ylabels_log = new double[(ylabelsLeftYAxis.length)/9 + 1];
		int j = 0;
		for ( int k = 0; k < ylabelsLeftYAxis.length; k++ ) {
			if ( ((k%9) == 0) || (k == 0)) {
				ylabels_log[j++] = ylabelsLeftYAxis[k];
			}
		}
		GRAxis.drawLabels ( daLeftYAxisLabel, ylabels_log.length,
		ylabels_log, datalimLeftYAxisLabel.getRightX(), GRAxis.Y, "%.1f", GRText.RIGHT|GRText.CENTER_Y );
		ylabels_log = null;
	}
	else {
	    if (drawLeftYAxisLabels) {
			if ( leftYAxisGraphType == TSGraphType.PERIOD ) {
				// Only want to label with whole numbers that are > 0 and <= __tslist.size()...
			    // FIXME SAM 2013-07-21 This is no different than below.  Precision is being set elsewhere for PERIOD graph
				GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis.length,
					ylabelsLeftYAxis, datalimLeftYAxisLabel.getRightX(),
					GRAxis.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
			else {
			    GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis.length,
			    	ylabelsLeftYAxis, datalimLeftYAxisLabel.getRightX(),
			    	GRAxis.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
		}
	}

	// Left Y-Axis title...
	title = tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", subproduct, -1, false );
	rotation = tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", subproduct, -1, false );
	double rotationDeg = 0.0;
	try {
		rotationDeg = Double.parseDouble(rotation);
	}
	catch ( NumberFormatException e) {
		// Ignore - should be empty or a valid number
	}
	fontname = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daLeftYAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( daLeftYAxisTitle, title,
		datalimLeftYAxisTitle.getCenterX(), datalimLeftYAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y, rotationDeg );
	
	// Right Y Axis labels, and tics...
	Message.printStatus(2, "drawAxesFront", "Right y-axis da limits:" + _da_righty_label.getDrawingLimits());
	Message.printStatus(2, "drawAxesFront", "Right y-axis data limits:" + _da_righty_label.getDataLimits());
	Message.printStatus(2, "drawAxesFront", "Right y-axis _datalim_righty_label:" + _datalim_righty_label);
	
	if ( drawRightYAxisLabels ) {
		boolean rightYAxisLogY = false;
		prop_value = tsproduct.getLayeredPropValue ( "RightYAxisType", subproduct, -1, false );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
			rightYAxisLogY = true;
		}
	
		double [] xlabelsRight = new double[2];
		xlabelsRight[0] = datalimRightYAxisLabel.getRightX();
		xlabelsRight[1] = datalimRightYAxisLabel.getRightX();
	
		xlabelsRight = null;
		fontname = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontName", subproduct, -1, false );
		fontsize = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", subproduct, -1, false );
		fontstyle = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", subproduct, -1, false );
		String yaxisDirRight = tsproduct.getLayeredPropValue ( "RightYAxisDirection", subproduct, -1, false );
	    boolean yaxisDirRightReverse = false;
	    if ( (yaxisDirRight != null) && yaxisDirRight.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
	        yaxisDirRightReverse = true;
	    }
		GRDrawingAreaUtil.setFont ( daRightYAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );
		GRDrawingAreaUtil.setColor ( daRightYAxisLabel, GRColor.black );
		if ( rightYAxisLogY ) {
			// Only draw major labels...
			double [] ylabels_log = new double[(ylabelsRightYAxis.length)/9 + 1];
			int j = 0;
			for ( int k = 0; k < ylabelsRightYAxis.length; k++ ) {
				if ( ((k%9) == 0) || (k == 0)) {
					ylabels_log[j++] = ylabelsRightYAxis[k];
				}
			}
			GRAxis.drawLabels ( daRightYAxisLabel, ylabels_log.length,
			ylabels_log, datalimRightYAxisLabel.getRightX(), GRAxis.Y, "%.1f", GRText.RIGHT|GRText.CENTER_Y );
			ylabels_log = null;
		}
		else if ( (daRightYAxisLabel != null) && (ylabelsRightYAxis != null) && (datalimRightYAxisLabel != null) ) { // Can be null if no right y-axis
			if ( rightYAxisGraphType == TSGraphType.PERIOD ) {
				// Only want to label with whole numbers that are > 0 and <= __tslist.size()...
			    // FIXME SAM 2013-07-21 This is no different than below.  Precision is being set elsewhere for PERIOD graph
				GRAxis.drawLabels ( daRightYAxisLabel, ylabelsRightYAxis.length,
					ylabelsRightYAxis, datalimRightYAxisLabel.getLeftX(),
					GRAxis.Y, "%." + rightYAxisPrecision + "f", GRText.LEFT|GRText.CENTER_Y);
			}
			else {
			    GRAxis.drawLabels ( daRightYAxisLabel, ylabelsRightYAxis.length,
			    	ylabelsRightYAxis, datalimRightYAxisLabel.getLeftX(),
			    	GRAxis.Y, "%." + rightYAxisPrecision + "f", GRText.LEFT|GRText.CENTER_Y);
			}
		}
		
		// Right Y-Axis title...
		String position = tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", subproduct, -1, false );
		if ( (position != null) && !position.equalsIgnoreCase("None") ) { 
			title = tsproduct.getLayeredPropValue ( "RightYAxisTitleString", subproduct, -1, false );
			rotation = tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", subproduct, -1, false );
			rotationDeg = 0.0;
			try {
				rotationDeg = Double.parseDouble(rotation);
			}
			catch ( NumberFormatException e) {
				// Ignore - should be empty or a valid number
			}
			fontname = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", subproduct, -1, false );
			fontsize = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", subproduct, -1, false );
			fontstyle = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", subproduct, -1, false );
			GRDrawingAreaUtil.setFont ( daRightYAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
			GRDrawingAreaUtil.drawText ( daRightYAxisTitle, title,
				datalimRightYAxisTitle.getCenterX(), datalimRightYAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y, rotationDeg );
		}
	}

	// Bottom X-Axis title...

	title = tsproduct.getLayeredPropValue ( "BottomXAxisTitleString", subproduct, -1, false );
	fontname = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontSize", subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daBottomXAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( daBottomXAxisTitle, title,
		datalimBottomXAxisTitle.getCenterX(), datalimBottomXAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Bottom X Axis labels, title, and tics

	fontname = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daBottomXAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );

	// Label axis after drawing so tics are on top of data...

	if ( (leftYAxisGraphType == TSGraphType.XY_SCATTER) || (leftYAxisGraphType == TSGraphType.DURATION) ) {
		// Label the X axis with formatted numbers...
		GRAxis.drawLabels ( daBottomXAxisTitle, xlabelsBottomXAxis.length,
			xlabelsBottomXAxis, datalimBottomXAxisLabel.getTopY(), GRAxis.X, "%.1f", GRText.TOP|GRText.CENTER_X );
		double[] xt = new double[2];
		double[] yt = new double[2];
		double[] yt2 = new double[2];
		double tic_height = 0.0; // Height of major tic marks
		yt[0] = ylabelsLeftYAxis[0];
		yt2[0] = ylabelsLeftYAxis[0];
	    if ( yaxisDirReverse ) {
	        yt[0] = ylabelsLeftYAxis[ylabelsLeftYAxis.length - 1];
	        yt2[0] = yt[0];
	    }
		// Figure out the y-positions and tic height (same regardless of intervals being used for labels)...
		if ( leftYAxisLogY ) {
			// Need to make sure the line is nice length!
			tic_height = yt[0]*.05;
			yt[1] = yt[0] + tic_height;
			yt2[1] = yt2[0] + tic_height/2.0;
		}
		else {
		    tic_height = datalimLeftYAxisGraph.getHeight()*.02;
		    if ( yaxisDirReverse ) {
		        // Reverse Y axis orientation
                yt[1] = yt[0] - tic_height;
                yt2[1] = yt2[0] - tic_height/2.0;
		    }
		    else {
		        // Normal Y axis orientation
    			yt[1] = yt[0] + tic_height;
    			yt2[1] = yt2[0] + tic_height/2.0;
		    }
		}
		for ( int i = 0; i < xlabelsBottomXAxis.length; i++ ) {
			xt[0] = xt[1] = xlabelsBottomXAxis[i];
			GRDrawingAreaUtil.drawLine ( daLeftYAxisGraph, xt, yt );
		}
	}
	else {
	    // Draw the X-axis date/time labels...
		if ( drawLeftYAxisLabels || drawRightYAxisLabels ) {
			// Only draw the X-axis if there is something on the y-axis also
			drawXAxisDateLabels ( leftYAxisGraphType, false );
		}
	}
	
	// Turn off anti-aliasing since a performance hit for data
	GRDrawingAreaUtil.setDeviceAntiAlias( daMainTitle, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daLeftYAxisLabel, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daRightYAxisLabel, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daBottomXAxisLabel, false);
}

/**
Draw the "current" time line, if properties are present to do so.  This checks
to see if the CurrentDateTime property is set (it will be set in the override
properties in the TSProduct).  If set and within the limits of the current graph, the current line will be drawn.
*/
private void drawCurrentDateTime ()
{	if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (__leftYAxisGraphType == TSGraphType.DURATION) ) {
		return;
	}
	// Allow layered properties because the current time could be specified once for all graphs...
	String prop_value = _tsproduct.getLayeredPropValue(	"CurrentDateTime", _subproduct, -1, true );
	if ( (prop_value == null) || (prop_value.trim().length() == 0) || prop_value.equalsIgnoreCase("None")) {
		return;
	}
	try {
	    DateTime current_time = null;
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
		    GRDrawingAreaUtil.setColor(_da_lefty_graph, GRColor.parseColor( prop_value) );
		}
		catch ( Exception e2 ) {
			GRDrawingAreaUtil.setColor ( _da_lefty_graph, GRColor.green );
		}
		double xp[] = new double[2];
		double yp[] = new double[2];
		xp[0] = current_time.toDouble();
		xp[1] = xp[0];
		// Get the drawing area limits.  This allows the check for reference and main graph windows...
		GRLimits data_limits = _da_lefty_graph.getDataLimits();
		yp[0] = data_limits.getMinY();
		yp[1] = data_limits.getMaxY();
		if ( (xp[0] >= data_limits.getMinX()) && (xp[0] <= data_limits.getMaxX()) ) { 
			GRDrawingAreaUtil.drawLine ( _da_lefty_graph, xp, yp );
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "TSGraph.drawCurrentDateTime", "Unable to draw current date/time." );
		Message.printWarning ( 3, "TSGraph.drawCurrentDateTime", e );
	}
}

/**
Draw the drawing areas, for troubleshooting.  Drawing areas boundaries are drawn in magenta.
*/
public void drawDrawingAreas ()
{	// This method is used by developers so OK to use global object data extensively.
	//boolean do_names = false;
	boolean do_names = true; // Display drawing area names
	_da_page.setColor ( GRColor.magenta );	// actually sets for all
	GRDrawingAreaUtil.setFont ( _da_page, "Helvetica", "Plain", 8 );
	// Reference and main...
	GRDrawingAreaUtil.drawRectangle ( _da_lefty_graph, _data_lefty_limits.getLeftX(), _data_lefty_limits.getBottomY(),
		_data_lefty_limits.getWidth(), _data_lefty_limits.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_lefty_graph, _da_lefty_graph.getName(), _data_lefty_limits.getCenterX(),
			_data_lefty_limits.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
	if ( (_da_righty_graph != null) && (_data_righty_limits != null) ) {
		GRDrawingAreaUtil.drawRectangle ( _da_righty_graph, _data_righty_limits.getLeftX(), _data_righty_limits.getBottomY(),
			_data_righty_limits.getWidth(), _data_righty_limits.getHeight() );
		if ( do_names ) {
			// Add a little bit to the height so does not overlap the left y-axis text
			GRDrawingAreaUtil.drawText ( _da_righty_graph, _da_righty_graph.getName(), _data_righty_limits.getCenterX(),
				(_data_righty_limits.getCenterY() + _data_righty_limits.getHeight()*.05), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
		}
	}
	if ( _is_reference_graph ) {
		// Don't need to draw anything else...
		return;
	}

	// Everything else draw top to bottom and left to right...

	GRDrawingAreaUtil.drawRectangle ( _da_page, _datalim_page.getLeftX(), _datalim_page.getBottomY(),
		_datalim_page.getWidth(), _datalim_page.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_page, _da_page.getName(), _datalim_page.getCenterX(),
			_datalim_page.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_maintitle, _datalim_maintitle.getLeftX(), _datalim_maintitle.getBottomY(),
		_datalim_maintitle.getWidth(), _datalim_maintitle.getHeight());
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_maintitle, _da_maintitle.getName(), _datalim_maintitle.getCenterX(),
			_datalim_maintitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_subtitle, _datalim_subtitle.getLeftX(), _datalim_subtitle.getBottomY(),
		_datalim_subtitle.getWidth(), _datalim_subtitle.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_subtitle, _da_subtitle.getName(), _datalim_subtitle.getCenterX(),
			_datalim_subtitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

/*  not enabled until we can get Yaxis labels out of the way
	GRDrawingAreaUtil.drawRectangle ( _da_topx_title, _datalim_topx_title.getLeftX(), _datalim_topx_title.getBottomY(),
		_datalim_topx_title.getWidth(), _datalim_topx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_topx_title, _da_topx_title.getName(), _datalim_topx_title.getCenterX(),
			_datalim_topx_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_topx_label, _datalim_topx_label.getLeftX(), _datalim_topx_label.getBottomY(),
		_datalim_topx_label.getWidth(), _datalim_topx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_topx_label, _da_topx_label.getName(), _datalim_topx_label.getCenterX(),
			_datalim_topx_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
*/

	GRDrawingAreaUtil.drawRectangle ( _da_lefty_title, _datalim_lefty_title.getLeftX(),
	    _datalim_lefty_title.getBottomY(), _datalim_lefty_title.getWidth(), _datalim_lefty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_lefty_title, _da_lefty_title.getName(), _datalim_lefty_title.getCenterX(),
			_datalim_lefty_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_lefty_label, _datalim_lefty_label.getLeftX(),
	    _datalim_lefty_label.getBottomY(), _datalim_lefty_label.getWidth(), _datalim_lefty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_lefty_label, _da_lefty_label.getName(), _datalim_lefty_label.getCenterX(),
			_datalim_lefty_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_righty_title, _datalim_righty_title.getLeftX(),
	    _datalim_righty_title.getBottomY(),	_datalim_righty_title.getWidth(), _datalim_righty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_righty_title, _da_righty_title.getName(), _datalim_righty_title.getCenterX(),
			_datalim_righty_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_righty_label, _datalim_righty_label.getLeftX(),
	    _datalim_righty_label.getBottomY(),	_datalim_righty_label.getWidth(), _datalim_righty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_righty_label, _da_righty_label.getName(), _datalim_righty_label.getCenterX(),
		    _datalim_righty_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_bottomx_label, _datalim_bottomx_label.getLeftX(),
	    _datalim_bottomx_label.getBottomY(), _datalim_bottomx_label.getWidth(), _datalim_bottomx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_bottomx_label, _da_bottomx_label.getName(), _datalim_bottomx_label.getCenterX(),
		    _datalim_bottomx_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_bottomx_title, _datalim_bottomx_title.getLeftX(),
	    _datalim_bottomx_title.getBottomY(), _datalim_bottomx_title.getWidth(), _datalim_bottomx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_bottomx_title, _da_bottomx_title.getName(), _datalim_bottomx_title.getCenterX(),
			_datalim_bottomx_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_bottom_legend, _datalim_bottom_legend.getLeftX(),
		_datalim_bottom_legend.getBottomY(), _datalim_bottom_legend.getWidth(), _datalim_bottom_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (_da_bottom_legend, _da_bottom_legend.getName(), _datalim_bottom_legend.getCenterX(),
			_datalim_bottom_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_left_legend, _datalim_left_legend.getLeftX(),
		_datalim_left_legend.getBottomY(), _datalim_left_legend.getWidth(), _datalim_left_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_left_legend, _da_left_legend.getName(), _datalim_left_legend.getCenterX(),
			_datalim_left_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( _da_right_legend, _datalim_right_legend.getLeftX(),
		_datalim_right_legend.getBottomY(), _datalim_right_legend.getWidth(), _datalim_right_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( _da_right_legend, _da_right_legend.getName(), _datalim_right_legend.getCenterX(),
			_datalim_right_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
} 

/**
Draw a duration plot.
@param tslist the list of time series to render
*/
private void drawDurationPlot ( GRDrawingArea daGraph, TSProduct tsproduct, int subproduct,
	List<TS> tslist, List<TSDurationAnalysis> durationAnalysisList,
	boolean isReferenceGraph )
{	String routine = "TSGraph.drawDurationPlot";
	if ( (tslist == null) || (tslist.size() == 0) ) {
		return;
	}	
	int size = tslist.size();
	TS ts = null;
	TSDurationAnalysis da = null;
	double [] values, percents;
	int symbol = 0;
	double symbol_size = 0.0;
	String prop_value;
	boolean niceSymbols = true;
	prop_value = tsproduct.getLayeredPropValue( "SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ((ts == null) || !ts.getEnabled() || !isTSEnabled(i)) {
			Message.printWarning ( 2, routine, "Null time series to graph [" + i + "]" );
			return;
		}
		
		da = durationAnalysisList.get(i);

		if (da == null) {
			Message.printWarning(2, routine, "Null TSDurationAnalysis to graph [" + i + "]");
			return;
		}
		values = da.getValues();
		percents = da.getPercents();
		if ( (values == null) || (percents == null) ) {
			Message.printWarning ( 2, routine, "Null TSDurationAnalysis data graph [" + i + "]" );
			return;
		}
		// Set line color and width...
		try {
		    daGraph.setColor ( GRColor.parseColor( tsproduct.getLayeredPropValue ( "Color", subproduct, i, false ) ) );
		}
		catch ( Exception e ) {
			daGraph.setColor ( GRColor.black );
		}
        prop_value = getLayeredPropValue("LineWidth", subproduct, i, false, null);
        if (prop_value != null) {
            if (StringUtil.isInteger(prop_value)) {
                GRDrawingAreaUtil.setLineWidth( daGraph, StringUtil.atoi(prop_value));
            }
        }
		GRDrawingAreaUtil.drawPolyline ( daGraph, values.length, percents, values );
		prop_value = tsproduct.getLayeredPropValue ( "SymbolStyle", subproduct, i, false );
		try {
		    symbol = GRSymbol.toInteger(prop_value);
		}
		catch ( Exception e ) {
			symbol = GRSymbol.SYM_NONE;
		}
		symbol_size = StringUtil.atod( tsproduct.getLayeredPropValue ( "SymbolSize", subproduct, i, false ) );
		if ( !isReferenceGraph && (symbol != GRSymbol.SYM_NONE) && (symbol_size > 0) ) {
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
			}
			GRDrawingAreaUtil.drawSymbols (daGraph, symbol, values.length, percents, values,
					symbol_size, GRUnits.DEVICE, GRSymbol.SYM_CENTER_X|GRSymbol.SYM_CENTER_Y );
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
			}
		}
	}
	// Clean up...
	GRDrawingAreaUtil.setLineWidth( daGraph, 1.0 );
}

/**
Draw the time series graph for one axis.
This is the highest-level draw method for drawing the data part of the graph and calls the
other time series drawing methods.
@param graphType graph type to draw, for the axis.
@param daGraph drawing area for the graph, could be aligned with left y-axis or right y-axis.
@param tslist list of time series to render, might be all time series or only one axis, depending on graph type.
@param dataLimits data limits for drawing area, consistent with nice labels, and zoom.
*/
private void drawGraph ( TSGraphType graphType, GRDrawingArea daGraph, TSProduct tsproduct, int subproduct,
	List<TS> tslist, GRLimits dataLimits ) {
	String routine = "TSGraph.drawGraph";
	int size = tslist.size();

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Drawing graph type " + graphType + ", " + size +
		    " time series total (some may be other graph types)." );
	}
	if ( size == 0 ) {
		return;
	}

	if ( dataLimits == null ) {
		// Not properly initialized, missing data, etc.
		return;
	}

	// Print the limits for debugging...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Drawing limits: " + daGraph.getDrawingLimits() );
		Message.printDebug ( 1, routine, _gtype + "Data limits: " + daGraph.getDataLimits() );
		Message.printDebug ( 1, routine, _gtype + "dataLimits: " + dataLimits );
		Message.printDebug ( 1, routine, _gtype + "Plotting limits: " + daGraph.getPlotLimits(GRDrawingArea.COORD_PLOT) );
	}

	/* If need to use for development...
	Message.printStatus ( 1, routine, _gtype + "Drawing: [" + _subproduct + "]: " + daGraph.getName() );
	Message.printStatus ( 1, routine, _gtype + "Drawing limits: " + daGraph.getDrawingLimits() );
	Message.printStatus ( 1, routine, _gtype + "Data limits: " + daGraph.getDataLimits() );
	Message.printStatus ( 1, routine, _gtype + "dataLimits: " + dataLimits );
	Message.printStatus ( 1, routine, _gtype + "Plotting limits: " + daGraph.getPlotLimits(GRDrawingArea.COORD_PLOT) );
	*/
	
	// Graph the time series.  If a reference map, only draw one time series, as specified in the properties...
	TS ts = null;
	if ( graphType == TSGraphType.DURATION ) {
		// All the time series are graphed (no left or right list)
		drawDurationPlot (daGraph, tsproduct, subproduct,
				tslist, _duration_data, _is_reference_graph);
	}
    if ( graphType == TSGraphType.RASTER ) {
        drawGraphRaster (tsproduct, subproduct, tslist);
    }
	else if ( graphType == TSGraphType.XY_SCATTER ) {
		drawXYScatterPlot (daGraph, tsproduct, subproduct, dataLimits, tslist, _regression_data);
	}
	else if ( graphType == TSGraphType.PREDICTED_VALUE || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	    boolean residual = true;
		if ( graphType == TSGraphType.PREDICTED_VALUE ) {
			residual = false;
		}
		TSRegression regressionData = null;
		int nregression = 0;
		if (_regression_data != null) {
			nregression = _regression_data.size();
		}
		TS predicted = null;

		for (int ir = 0; ir < nregression; ir++) {
			regressionData = _regression_data.get(ir);
			if (regressionData != null) {
			   	if (residual) {
					if (isTSEnabled(ir + 1)) {
						drawTS(tsproduct, subproduct, ir + 1, regressionData.getResidualTS(), graphType );
					}
				}
				else {
					PropList props = new PropList("");
						
					if (isTSEnabled(0)) {
						drawTS(tsproduct, subproduct, 0,regressionData.getIndependentTS(),graphType, props);
					}
	
					if (isTSEnabled(ir + 1)) {
						drawTS(tsproduct, subproduct, ir + 1,regressionData.getDependentTS(), graphType, props);
						props.set("LineStyle=Dashed");
						props.set("SymbolStyle=None");
						predicted = regressionData.getPredictedTS();
						drawTS(tsproduct, subproduct, ir + 1, predicted, graphType, props);
					}
				}
			}
			else {
				// ignore -- null regression data
			}
		}
	}
	else if ( graphType == TSGraphType.AREA_STACKED ) {
	    drawGraphAreaStacked (tsproduct, subproduct, tslist, getDerivedTSList() );
	}
	else {	
		// "Normal" graph that can be handled in general code...
		for (int its = 0; its < size; its++) {
			ts = tslist.get(its);
			if ((ts == null) || (!_is_reference_graph && !ts.getEnabled())
			    || (!_is_reference_graph && !isTSEnabled(its))) {
				continue;
			}

			if (_is_reference_graph && (its != _reference_ts_index)) {
				// A reference graph but we have not found the reference time series yet.  We want the
				// reference time series drawn in the same color as it occurs in the main graph.
				if (Message.isDebugOn) {
					Message.printDebug(1, routine, _gtype + "Skipping time series " + its);
				}
				continue;
			}

			// Draw each time series using the requested graph type.
		    // Allow the individual time series graph type to be different from the main graph type
			// TODO SAM 2010-11-22 Need to evaluate defaults/checks consistent with other code.
		    TSGraphType tsGraphType = getTimeSeriesGraphType(graphType, its);
			drawTS ( tsproduct, subproduct, its, ts, tsGraphType );
		}
	}
}

/**
Draw the time series graph for an "AreaStacked" graph.
@param tslist list of time series to draw, all time series for the axis.
@param derivedTSList list of derived time series needed for stacked area graph, time series cumulative values.
*/
private void drawGraphAreaStacked ( TSProduct tsproduct, int subproduct, List<TS> tslist, List<TS> derivedTSList )
{
    // Loop through the derived time series that have been previously produced.  Draw
    // in the reverse order because the last time series should have the biggest values and be
    // drawn at the back.
    TSGraphType graphType = getLeftYAxisGraphType();
    // Note that that the derived list will contain time series that add to each other
    // as well as individual time series that are not stacked.  Check the time series graph type
    // to know what to do.
    //List<TS> derivedTSlist = getDerivedTSList();
    // Also need the full list of time series, some of which may be drawn in addition to stacked area
    // This list is actually iterated because the time series positions match those in the derived
    // time series, for property purposes
    //List<TS> tslist = getTSList();
    if ( derivedTSList == null ) {
        return;
    }
    int size = tslist.size();
    TS ts = null;
    for ( int its = size - 1; its >= 0; its-- ) {
        ts = derivedTSList.get(its);
        if ( ts == null ) {
            // No data to draw or this is not a stacked area time series
            continue;
        }
        TSGraphType tsGraphType = getTimeSeriesGraphType ( graphType, its );
        if ( tsGraphType == TSGraphType.AREA_STACKED ) {
            drawTS(tsproduct, subproduct, its, ts, TSGraphType.AREA_STACKED );
        }
    }
    // TODO SAM 2010-11-22 Need to have property to draw on top or bottom - assume top
    // Now also draw the other time series that may not be the same type
    // Allow the individual time series graph type to be different from the main graph type
    for ( int its = 0; its < size; its++ ) {
        TSGraphType tsGraphType = getTimeSeriesGraphType ( graphType, its );
        if ( tsGraphType != TSGraphType.AREA_STACKED ) {
            drawTS ( tsproduct, subproduct, its, tslist.get(its), tsGraphType );
        }
    }
}

/**
Draw the time series graph for a "Raster" graph.
@param run-time overrideProps override properties for the graph
*/
private void drawGraphRaster ( TSProduct tsproduct, int subproduct, List<TS> tslist )
{
    // Raster graph can only draw one time series so get the first non-null time series
    TS ts = null;
    int its;
    for ( its = 0; its < tslist.size(); its++ ) {
        ts = tslist.get(its);
        if ( ts != null ) {
            break;
        }
    }
    if ( ts == null ) {
        return;
    }
    drawTS ( tsproduct, subproduct, its, ts, TSGraphType.RASTER );
}

/**
Draw the legend.  The drawing is the same regardless of the legend position (the
legend items are draft from top to bottom and first time series to last, except for special cases like
stacked area plot where the order is reversed).  This should work well for left,
right, and bottom legends.  Additional logic may need to be implemented when the top legend is supported.
*/
private void drawLegend ()
{   TSGraphType graphType = getLeftYAxisGraphType();
	if (_is_reference_graph) {
		return;
	}

	boolean includeLeftYAxis = true;
	boolean includeRightYAxis = true;
	if ((__tslist == null) || (__tslist.size() == 0) || getEnabledTSList(includeLeftYAxis,includeRightYAxis).size() == 0) {
		return;
	}

	// Figure out which legend drawing area we are using...

	String prop_val = _tsproduct.getLayeredPropValue("LegendPosition", _subproduct, -1, false);
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

	    GRDrawingAreaUtil.setLineWidth( da_legend, 1 );
		GRDrawingAreaUtil.setColor(da_legend, GRColor.white);
		GRDrawingAreaUtil.fillRectangle(da_legend, -4, -4,
			datalim_legend.getWidth() + 8, datalim_legend.getHeight() + 8);
		GRDrawingAreaUtil.setColor(da_legend, GRColor.black);
		GRDrawingAreaUtil.drawRectangle(da_legend, -4, -4,
			datalim_legend.getWidth() + 8, datalim_legend.getHeight() + 8);
	}

	// Get the properties for the legend...
	String legend_font = _tsproduct.getLayeredPropValue ( "LegendFontName", _subproduct, -1, false );
	String legend_fontsize = _tsproduct.getLayeredPropValue ( "LegendFontSize", _subproduct, -1, false );
	String legend_fontstyle = _tsproduct.getLayeredPropValue ( "LegendFontStyle", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( da_legend, legend_font, legend_fontstyle, StringUtil.atod(legend_fontsize) );
	GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( da_legend, "TEST STRING", GRUnits.DEVICE );
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
	prop_value = _tsproduct.getLayeredPropValue( "SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	if (__tslist != null) {
		size = __tslist.size();
	}
	
	TS ts;

	if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
		size = 1 + ((size - 1) * 2);
	}
	if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		size = size - 1;
	}

	double[] lineDash = new double[2];
	lineDash[0] = 3;
	lineDash[1] = 5;

	boolean predicted = false;
	int tsNum = 0;
	TSRegression regressionData = null;

	int iStart = 0;
	int iEnd = size; // One more than last index will break loop
	int iIncrement = 1;
	// Determine if any time series are being drawn as stacked area
	boolean reverseLegendOrder = false;
	for ( int i = 0; i < size; i++ ) {
	    TSGraphType tsGraphType = getTimeSeriesGraphType(graphType, i);
    	if ( tsGraphType == TSGraphType.AREA_STACKED ) {
    	    reverseLegendOrder = true;
    	    break;
    	}
	}
	if ( reverseLegendOrder ) {
	    // Reverse the order of drawing the time series so the legend order matches the area
	    iStart = size - 1;
	    iEnd = -1; // One less than first index will break loop
	    iIncrement = -1;
	}
	// The following loop works when plotting the time series list forward or backward
	for ( int i = iStart; i != iEnd; i = i + iIncrement ) {
	    TSGraphType tsGraphType = getTimeSeriesGraphType(graphType, i);
		predicted = false;

		// Make sure that the legend is not drawing using negative data units.  If it is, then it will likely 
		// go into another graph (since there can be more than one graph in a window/page
		if (ylegend < 0.0) {
			 continue;
		}
		
		if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
			// Determine the correspondence of the TS to be drawn versus the actual time series that there is
			// access to.  
			// ts 0 corresponds to 0
			// ts 1 corresponds to 1 and 2 (for the dependent and predicted TS)
			// ts 2 corresponds to 3 and 4 ...
			// etc.

			if (i == 0) {
				tsNum = 0;
			}
			else {
				if (i % 2 == 0) {
					// the ts is half of the even numbers
					tsNum = i / 2;
					predicted = true;
				}
				else {
					// for odd numbers, the ts is half (the value plus one)
					tsNum = (i + 1) / 2;
				}
			}
			
			if (!isTSEnabled(tsNum)) {
				continue;
			}

			if (predicted) {
				// predicted ones have to be retrieved from the regression data.
				regressionData = _regression_data.get(tsNum - 1);
				ts = regressionData.getPredictedTS();

			}
			else {
				ts = __tslist.get(tsNum);
			}

			legend = getLegendString(ts, tsNum);
			if (legend == null) {
				continue;
			}
			
			if (predicted) {
				legend = legend + " (Predicted)";
			}

			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue( "Color", _subproduct, tsNum, false);
			try {	
				da_legend.setColor(GRColor.parseColor( prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
			
			line_style = _tsproduct.getLayeredPropValue( "LineStyle", _subproduct, tsNum, false);
	
			if (line_style == null) {
				line_style = "None";
			}

			if (predicted) {
				line_style = "Dashed";
			}
		}
		else if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			if (!isTSEnabled(i + 1)) {
				continue;
			}

			regressionData = _regression_data.get(i);
			ts = regressionData.getResidualTS();

			legend = getLegendString(ts, i + 1) + " (Residual)";
			if (legend == null) {
				continue;
			}
			
			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue( "Color", _subproduct, i + 1, false);

			try {	
				da_legend.setColor(GRColor.parseColor(prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
		}
		else {
			if (!isTSEnabled(i)) {
				continue;
			}
			ts = __tslist.get(i);
			legend = getLegendString(ts, i);
			if (legend == null) {
				continue;
			}
			
			// Draw the legend line
			prop_value = _tsproduct.getLayeredPropValue("Color", _subproduct, i, false);
			try {	
				da_legend.setColor(GRColor.parseColor(prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
			
			line_style = _tsproduct.getLayeredPropValue( "LineStyle", _subproduct, i, false);
	
			if (line_style == null) {
				line_style = "None";
			}
		}
		
		x[0] = datalim_legend.getLeftX();
		// Legend drawing limits are in device units so just use pixels...
		x[1] = x[0] + 25;
		y[0] = ylegend + ydelta/2.0;
		y[1] = y[0];
		if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (i == 0) ) {
			;// Do nothing.  Don't want the symbol (but do want the string label below
		}
		else if ( (tsGraphType == TSGraphType.AREA) || (tsGraphType == TSGraphType.AREA_STACKED) ||
		    (tsGraphType == TSGraphType.BAR) || (tsGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
			GRDrawingAreaUtil.fillRectangle ( da_legend, x[0], ylegend,(x[1] - x[0]), ydelta);
		}
		else {	
			prop_value = getLayeredPropValue("LineWidth", _subproduct, i, false, null);
			if (prop_value != null) {
				if (StringUtil.isInteger(prop_value)) {
					GRDrawingAreaUtil.setLineWidth( da_legend, StringUtil.atoi(prop_value));
				}
			}

			if (__leftYAxisGraphType != TSGraphType.POINT && line_style.equalsIgnoreCase("Solid") ) {
				GRDrawingAreaUtil.drawLine(da_legend, x, y);
			}
			else if (__leftYAxisGraphType != TSGraphType.POINT && line_style.equalsIgnoreCase("Dashed")) {
				GRDrawingAreaUtil.setLineDash( da_legend, lineDash, 0);
				GRDrawingAreaUtil.drawLine(da_legend, x, y);
				GRDrawingAreaUtil.setLineDash( da_legend, null, 0);
			}

			GRDrawingAreaUtil.setLineWidth(da_legend, 1);

			// Draw the symbol if any is specified...
			prop_value = _tsproduct.getLayeredPropValue ( "SymbolStyle", _subproduct, i, false );
			try {	
				symbol = GRSymbol.toInteger(prop_value);
			}
			catch (Exception e) {
				symbol = GRSymbol.SYM_NONE;
			}
			symbol_size = StringUtil.atod( _tsproduct.getLayeredPropValue ( "SymbolSize", _subproduct, i, false ) );
			if ((symbol != GRSymbol.SYM_NONE) && (symbol_size > 0)){
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias( da_legend, true);
				}
				GRDrawingAreaUtil.drawSymbol (da_legend, symbol,
					(x[0] + x[1])/2.0, y[0], symbol_size, GRUnits.DEVICE,
					GRSymbol.SYM_CENTER_X|GRSymbol.SYM_CENTER_Y );
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias( da_legend, false);
				}
			}
		}
		da_legend.setColor ( GRColor.black );
		// Put some space so text does not draw right up against symbol
		GRDrawingAreaUtil.drawText ( da_legend, " " + legend, x[1], ylegend, 0.0, GRText.LEFT|GRText.BOTTOM );
		ylegend -= ydelta;
	}
}

/**
Draw a box around the graph.  This is normally done after drawing grid lines
so the box looks solid, and before drawing data.
*/
private void drawOutlineBox ()
{	GRDrawingAreaUtil.setColor ( _da_lefty_graph, GRColor.black );
	if ( _is_reference_graph ) {
		// Just draw a box around the graph area to make it more visible...
		// Using GR seems to not always get the line (roundoff)?
		Rectangle bounds = _dev.getBounds();

		_graphics.drawRect ( 0, 0, (bounds.width - 1), (bounds.height - 1) );
		return;
	}
	else {	// Normal drawing area...
		GRDrawingAreaUtil.drawRectangle ( _da_lefty_graph, _data_lefty_limits.getMinX(), _data_lefty_limits.getMinY(),
			_data_lefty_limits.getWidth(), _data_lefty_limits.getHeight() );
	}
}

/**
Draw time series annotations, which are related to specific time series.
Currently this method does nothing.
*/
private void drawTimeSeriesAnnotations ()
{
    
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
	String maintitle_font = _tsproduct.getLayeredPropValue ("MainTitleFontName", _subproduct, -1, false );
	String maintitle_fontstyle = _tsproduct.getLayeredPropValue ("MainTitleFontStyle", _subproduct, -1, false );
	String maintitle_fontsize = _tsproduct.getLayeredPropValue ("MainTitleFontSize", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font,
		maintitle_fontstyle, StringUtil.atod(maintitle_fontsize) );
	String maintitle_string = _tsproduct.expandPropertyValue(
	    _tsproduct.getLayeredPropValue ( "MainTitleString", _subproduct, -1, false));
	GRDrawingAreaUtil.drawText ( _da_maintitle, maintitle_string, _datalim_maintitle.getCenterX(),
		_datalim_maintitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Sub title....

	_da_subtitle.setColor ( GRColor.black );
	String subtitle_font = _tsproduct.getLayeredPropValue ( "SubTitleFontName", _subproduct, -1, false );
	String subtitle_fontstyle = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", _subproduct, -1, false );
	String subtitle_fontsize = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", _subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font, subtitle_fontstyle, StringUtil.atod(subtitle_fontsize) );
	String subtitle_string = _tsproduct.expandPropertyValue(
	    _tsproduct.getLayeredPropValue ( "SubTitleString", _subproduct, -1, false));
	GRDrawingAreaUtil.drawText ( _da_subtitle, subtitle_string, _datalim_subtitle.getCenterX(),
		_datalim_subtitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
}

/**
Draw (render) a single time series on the graph.
@param its the time series list position (0+, for retrieving properties and messaging)
@param ts the time series to render
@param graphType the graph type to use for the time series
*/
private void drawTS ( TSProduct tsproduct, int subproduct, int its, TS ts, TSGraphType graphType ) {
	drawTS(tsproduct, subproduct, its, ts, graphType, null);
}

/**
Draw a single time series.
@param its the time series list position (0+, for retrieving properties and messaging)
@param ts Single time series to draw.
@param graphType the graph type to use for the time series
@param overrideProps run-time override properties to consider when getting graph properties
*/
private void drawTS(TSProduct tsproduct, int subproduct, int its, TS ts, TSGraphType graphType, PropList overrideProps )
{   String routine = "TSGraph.drawTS";

	if ((ts == null) || !ts.hasData() || (!_is_reference_graph && !ts.getEnabled())) {
	    // No need or unable to draw
		return;
	}
	// First check for graph types that have their own rendering method
    // Take a new approach for the area graph by having a separate method.  This will duplicate
    // some code, but the code below is getting too complex with multiple graph types handled
    // in the same code.  The separate renderers also can be refactored into separate classes if appropriate.
	if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ) {
	    drawTSRenderAreaGraph ( its, ts, graphType, overrideProps );
	    return;
	}
	else if ( graphType == TSGraphType.RASTER ) {
        drawTSRenderRasterGraph ( ts, graphType, overrideProps );
        return;
    }

	if ((ts.getDataIntervalBase() == TimeInterval.IRREGULAR) && (__leftYAxisGraphType == TSGraphType.PERIOD)) {
		// Can't draw irregular time series in period of record graph.
		return;
	}

	GRColor tscolor = drawTSHelperGetTimeSeriesColor ( its, overrideProps );
    _da_lefty_graph.setColor(tscolor);
	
	DateTime start = drawTSHelperGetStartDateTime ( ts );
	DateTime end = drawTSHelperGetEndDateTime ( ts );

	/* Can uncomment for debug purposes on start and end dates 
	Message.printStatus(2, "",
	"----------------------------------------------------------");
	String tsStatus = "TS:" + (ts.getIdentifier()).getIdentifier() + " GRAPH_TYPE:" + _gtype;
	String tsInfo   = "DIFF:" + diff.toString() + "  Interval:" +
	ts.getDataIntervalBase() + "   Mult:" + ts.getDataIntervalMult();
	Message.printStatus(2, "", tsInfo);
	Message.printStatus(2, "", tsStatus);
	String timeStatus = "START   " + _start_date.toString() + "   " +
	start.toString() + "    END:" + end.toString();
	Message.printStatus(2, routine, timeStatus);
	Message.printStatus(2, "",
	"----------------------------------------------------------");
	*/

	if (Message.isDebugOn) {
		Message.printDebug(1, routine, _gtype + "Drawing time series " + start + " to " + end +
		    " global period is: " + _start_date + " to " + _end_date);
	}

	// Only draw the time series if the units are being ignored or can be
	// converted.  The left axis units are determined at construction.

	if (!_ignoreLeftAxisUnits) {
		if ( (__leftYAxisGraphType != TSGraphType.DURATION) && (__leftYAxisGraphType != TSGraphType.XY_SCATTER) ) {
		   	String lefty_units = getLayeredPropValue( "LeftYAxisUnits", subproduct, -1, false, overrideProps);

			if (!DataUnits.areUnitsStringsCompatible(ts.getDataUnits(),lefty_units,true)) {
				if (lefty_units.equals("")) {
					// new graph -- set units to whatever ts 1's units are
					int how_set_prev = tsproduct.getPropList().getHowSet();
					tsproduct.getPropList().setHowSet( Prop.SET_AS_RUNTIME_DEFAULT);
					tsproduct.setPropValue("LeftYAxisUnits", ts.getDataUnits(), subproduct, -1);
					tsproduct.getPropList().setHowSet(how_set_prev);
				}
				else {
					// no units, so can't draw the graph
					return;
				}
			}
		}
	}

	double lasty = ts.getMissing();
	double x;
	double y;
	int drawcount = 0; // Overall count of points in time series
	int pointsInSegment = 0; // Points in current line segment being drawn
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();	

	String prop_value;

	int symbolNoFlag = drawTSHelperGetSymbolStyle ( its, overrideProps, false );
	int symbolWithFlag = drawTSHelperGetSymbolStyle ( its, overrideProps, true );
	int symbol = symbolNoFlag; // Default symbol to use for a specific data point, checked below
	//Message.printStatus ( 2, routine, _gtype + "symbolNoFlag=" + symbolNoFlag + " symbolWithFlag=" + symbolWithFlag );
	double symbol_size = drawTSHelperGetSymbolSize ( its, overrideProps );

	// Data text label.

	boolean labelPointWithFlag = false;
	int label_position = 0;
	String label_format = "";
	String label_position_string;
	String label_units = "";
	String label_value_format = "";

	boolean graphLabelFormatSet = false;

	// First try to get the label format from the time series properties.
	label_format = getLayeredPropValue("DataLabelFormat", subproduct, its, false, overrideProps);
	if (label_format == null || label_format.equals("")) {
		// Try to get from the graph properties.
		label_format = getLayeredPropValue("DataLabelFormat", subproduct, -1, false, overrideProps);
		if (!label_format.equals("")) {
			// Label the format
			labelPointWithFlag = true;
			graphLabelFormatSet = true;
		}
	}
	else {	
		labelPointWithFlag = true;
	}

	// TODO (JTS - 2006-04-26) Evaluate label format
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

	if (labelPointWithFlag) {
		// Are drawing point labels so get the position, set the font, and get the format.
		label_position_string = getLayeredPropValue("DataLabelPosition", subproduct, its, false, overrideProps);
		// Determine the label position automatically, if necessary.
		if (graphLabelFormatSet || label_position_string.equals("") || label_position_string.equalsIgnoreCase("Auto")) {
			// Try to get from the graph properties.
			label_position_string = getLayeredPropValue( "DataLabelPosition", subproduct, -1, false, overrideProps);
			if (label_position_string.equals("") || label_position_string.equalsIgnoreCase("Auto")) {
				// Default position
				label_position_string = "Right";
			}
		}

		label_position = GRText.CENTER_Y | GRText.LEFT;

		try {	
			label_position = GRText.parseTextPosition( label_position_string);
		}
		catch (Exception e) {
			label_position = GRText.CENTER_Y | GRText.LEFT;
		}

		// The font is only defined at the graph level.
		// Set for point labels.
		String fontname = getLayeredPropValue("DataLabelFontName", subproduct, -1, false, overrideProps);
		String fontsize = getLayeredPropValue("DataLabelFontSize", subproduct, -1, false, overrideProps);
		String fontstyle = getLayeredPropValue("DataLabelFontStyle", subproduct, -1, false, overrideProps);
		GRDrawingAreaUtil.setFont(_da_lefty_graph, fontname, fontstyle, StringUtil.atod(fontsize));

		// Determine the format for the data value in case it is needed to format the label.
		label_units = ts.getDataUnits();
		label_value_format = DataUnits.getOutputFormatString(label_units, 0, 4);
	}

	// Bar graph parameters
	double bar_width = 0.0; // Width actually drawn (single bar)
	double bar_width_d2 = 0.0; // bar_width/2
	double full_bar_width = 0.0; // Width used for positioning (may be multiple bars if no overlap)
	double full_bar_width_d2 = 0.0; // full_bar_width/2
	double maxy = 0.0;
	double miny = 0.0;

	int bar_position = 0; // position 0 means centered on the date, -1 to left, 1 to right
	prop_value = getLayeredPropValue("BarPosition", subproduct, -1, false, overrideProps);
	if (prop_value != null) {
		if (prop_value.equalsIgnoreCase("LeftOfDate")) {
			bar_position = -1;
		}
		else if (prop_value.equalsIgnoreCase("RightOfDate")) {
			bar_position = 1;
		}
	}
	
    boolean barOverlap = false; // whether bars are overlapped
    prop_value = getLayeredPropValue("BarOverlap", subproduct, -1, false, overrideProps);
    if ( (prop_value != null) && prop_value.equalsIgnoreCase("True")) {
        barOverlap = true;
    }

	// Generate the clipping area that will be set so that no data are drawn outside of the graph
	Shape clip = GRDrawingAreaUtil.getClip(_da_lefty_graph);
	GRDrawingAreaUtil.setClip(_da_lefty_graph, _da_lefty_graph.getDataLimits());

	// If a bar graph, the bar width is the data interval/nts (if barOverlap=false) or
	// interval (if barOverlap=true).  Rather than compute a bar width that may vary some
	// with the plot zoom, always draw filled in and draw a border.  The position of the bar is
	// determined by the "BarPosition" property.

	// TODO SAM 2016-10-24 Need to enable right y-axis
	boolean includeLeftYAxis = true;
	boolean includeRightYAxis = false;
	int nts = getEnabledTSList(includeLeftYAxis,includeRightYAxis).size();

	if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL && __tslist != null) {
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
	
	prop_value = getLayeredPropValue("LineStyle", subproduct, its, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("None")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("GraphType", subproduct, -1, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Point")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("SymbolAntiAlias", -1, -1, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	if (!_is_reference_graph) {
		prop_value = getLayeredPropValue("LineWidth", subproduct, its, false, overrideProps);
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
	prop_value = getLayeredPropValue("LineStyle", subproduct, its, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Dashed")) {
		dashedLine = true;
		lineDash = new double[2];
		lineDash[0] = 3;
		lineDash[1] = 5;
	}

	if (__leftYAxisGraphType == TSGraphType.BAR || __leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		DateTime temp_date = new DateTime(_tslimits_lefty.getDate1());
		// Convert date to a double
		full_bar_width = temp_date.toDouble();
		// Subtract from the date
		if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
			// Use largest number of days in month to prevent
			// overlap.  Need to use day precision to make this work.
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

		// Subtract the new value to get the bar width in plotting units.
		full_bar_width -= temp_date.toDouble();
		temp_date = null;
		miny = _data_lefty_limits.getMinY();
		maxy = _data_lefty_limits.getMaxY();

		// Account for the number of time series...
		if ( barOverlap == false ) {
		    full_bar_width /= nts;
		}

		// If bar width is <= 5 pixels in device units, do not
		// draw bounding rectangle because it will hide the data...
		if ((_da_lefty_graph.scaleXData(full_bar_width) - _da_lefty_graph.scaleXData(0.0)) <= 5.0) {
			draw_bounding_rectangle = false;
		}
	}

	// Use the same plotting width as the position width for all but
	// monthly since months have different numbers of days.
	if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
		// No need for separator since rectangle is smaller.
		draw_bounding_rectangle = false;
		// This gives a little space between the bars
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
    String dataFlag = null;

	if (interval_base == TimeInterval.IRREGULAR) {
		// Get the data and loop through the vector.  Currently do not
		// use TSIterator because head-to-head performance tests have
		// not been performed.  Need to do so before deciding which approach is faster.
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		boolean yIsMissing = false;

		//Message.printStatus(2,routine,"Starting to draw time series.");
		for ( int i = 0; i < nalltsdata; i++ ) {
        	tsdata = alltsdata.get(i);
        	date = tsdata.getDate();
        	if (date.greaterThan(end)) {
        		// Past the end of where want to go so quit.
        		break;
        	}

            // TODO (JTS - 2006-04-26) All data flags (returned from getDataFlag()) are being trimmed below.
            // In the future, if the spacing of data flags becomes critical, this may need revisited.
        	if (date.greaterThanOrEqualTo(start)) {
        		y = tsdata.getDataValue();
        		yIsMissing = ts.isDataMissing(y);
        		//Message.printStatus(2, routine,"Date=" + date + " yIsMissing=" + yIsMissing);
        		if ( yIsMissing ) {
	        		if ( this.useXYCache ) {
	        			// Missing point is found. If pointsInSegment > 0, draw the line segment
	        			if ( pointsInSegment > 0 ) {
	        				//Message.printStatus(2, routine, "Found missing value - drawing segment with " + pointsInSegment + " points.");
	        				GRDrawingAreaUtil.drawPolyline ( _da_lefty_graph, pointsInSegment, this.xCacheArray, this.yCacheArray );
		        			pointsInSegment = 0;
	        			}
	        		}
	        		else {
	        			// No need to draw anything
	        			lasty = y;
	        		}
        			continue;
        		}
                dataFlag = tsdata.getDataFlag();
                if ( (dataFlag == null) || dataFlag.length() == 0 ) {
                    symbol = symbolNoFlag;
                }
                else {
                    symbol = symbolWithFlag;
                }
        
        		// Else, see if need to moveto or lineto the point.
        		x = date.toDouble();
        		if ( this.useXYCache ) {
        			if ( this.xCacheArray == null ) {
        				// Allocate initial cache arrays
        				this.xCacheArray = new double[100];
        				this.yCacheArray = new double[100];
        			}
        		}
        		if (((drawcount == 0) || ts.isDataMissing(lasty)) && (__leftYAxisGraphType != TSGraphType.BAR &&
        		    __leftYAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) {
        			// First point in the entire time series or first non-missing point after a missing point.
        			// Always draw the symbol
                    //if (tsdata != null) 
                    //Message.printStatus(1, "", "JTS0" + date + ": '" + tsdata.getDataFlag() 
                    //	+ "'  '" + label_position + "'  '" + y + "'");
        			if (_is_reference_graph) {
        				// Don't draw symbols
        			}
        			else if (labelPointWithFlag && ((symbol == GRSymbol.SYM_NONE) || (symbol_size <= 0))) {
        				// Text only
        				GRDrawingAreaUtil.drawText(_da_lefty_graph,
        				    TSData.toString(label_format,label_value_format, date, y, 0.0,
        					tsdata.getDataFlag().trim(),label_units), 
        					x, y, 0.0, label_position);
        			}
        			else if (labelPointWithFlag) {
        				if (niceSymbols) {
        					GRDrawingAreaUtil.setDeviceAntiAlias(_da_lefty_graph, true);
        				}
        
        				// Text and symbol
        				GRDrawingAreaUtil.drawSymbolText(_da_lefty_graph, symbol, x, y, symbol_size,
        					TSData.toString(label_format,label_value_format, date, y, 0.0,
        					tsdata.getDataFlag().trim(), label_units), 
        					0.0, label_position, GRUnits.DEVICE,
        					GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
        					
        				if (niceSymbols) {
        					// Turn off anti-aliasing so that it only applies for symbols
        					GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
        				}
        			}
        			else {	
        				// Symbol only
        				if (niceSymbols) {
        					GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, true);
        				}
        
        				GRDrawingAreaUtil.drawSymbol(_da_lefty_graph, symbol, x, y, symbol_size,
        				    GRUnits.DEVICE, GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
        
        				if (niceSymbols) {
        					// Turn off anti-aliasing so that it only applies for symbols
        					GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
        				}
        			}
        
        			// First point or skipping data. Put second so symbol coordinates do not set the last point.
        			if ( this.useXYCache ) {
        				//Message.printStatus(2, routine, "Initializing first point in segment to " + x + "," + y);
	        			xCacheArray[0] = x;
	        			yCacheArray[0] = y;
	        			pointsInSegment = 1;
        			}
        			else {
        				// Move/draw each point individually
        				GRDrawingAreaUtil.moveTo(_da_lefty_graph, x, y );
        			}
        		}
        		else {	
        			// Draw the line segment or bar
        			if (__leftYAxisGraphType != TSGraphType.BAR	&& __leftYAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        				if (draw_line) {
        					GRDrawingAreaUtil.setLineWidth( _da_lefty_graph, lineWidth);
        						
        					if (dashedLine) {
        						GRDrawingAreaUtil.setLineDash( _da_lefty_graph, lineDash, 0);
        					}
        					
        					if ( this.useXYCache ) {
        						// Save the point but do not draw yet
        						if ( pointsInSegment == this.xCacheArray.length ) {
        							// Increase the size of the cache arrays
        							int newLength = (int)(this.xCacheArray.length*(1.0+this.xyCacheDelta));
        							double [] xTemp = new double[newLength];
        							System.arraycopy(this.xCacheArray, 0, xTemp, 0, this.xCacheArray.length);
        							this.xCacheArray = xTemp;
        							double [] yTemp = new double[newLength];
        							System.arraycopy(this.yCacheArray, 0, yTemp, 0, this.yCacheArray.length);
        							this.yCacheArray = yTemp;
        							//Message.printStatus(2, routine, "Increase the cache array length to " + newLength );
        						}
        						this.xCacheArray[pointsInSegment] = x;
        						this.yCacheArray[pointsInSegment] = y;
        						//Message.printStatus(2, routine, "Point["+pointsInSegment+"]=" + x + "," + y);
        						++pointsInSegment;
        					}
        					else {
        						// Drawing each point with LineTo
        						GRDrawingAreaUtil.lineTo(_da_lefty_graph, x, y);
        					}
        
        					// Reset the line width to the normal setting for all other drawing
        					GRDrawingAreaUtil.setLineWidth(	_da_lefty_graph, 1);
        
        					if (dashedLine) {
        						// Reset the line dash so that only this line is drawn dashed
        						GRDrawingAreaUtil.setLineDash( _da_lefty_graph, null, 0);
        					}
        				}
        				else {	
        					// No line to draw, so simply move the position of the stylus
        					GRDrawingAreaUtil.moveTo(_da_lefty_graph,	x, y);
        				}
        				if (_is_reference_graph) {
        					// No symbol or label to draw
        				}
        				else if (labelPointWithFlag && ((symbol == GRSymbol.SYM_NONE) || (symbol_size <= 0))) {
        					// Just text
        					GRDrawingAreaUtil.drawText(_da_lefty_graph,
        						TSData.toString(label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
        						label_units), x, y, 0.0, label_position);
        				}
        				else if (labelPointWithFlag) {
        					if (niceSymbols) {
        						GRDrawingAreaUtil.setDeviceAntiAlias(_da_lefty_graph, true);
        					}
        					
        					// Symbol and label...
        					GRDrawingAreaUtil.drawSymbolText( _da_lefty_graph, symbol, x, y, symbol_size,
        					    TSData.toString(label_format,label_value_format,date, y, 0.0, tsdata.getDataFlag().trim(), 
        						label_units), 0.0, label_position, GRUnits.DEVICE, GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
        					if (niceSymbols) {
        						// Turn off anti-aliasing so it doesn't affect anything else
        						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
        					}
        				}
        				else {	
        					// Just symbol
        					if (niceSymbols) {
        						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, true);
        					}
        					GRDrawingAreaUtil.drawSymbol(_da_lefty_graph, symbol, x, y, symbol_size,
        						GRUnits.DEVICE, GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
        					if (niceSymbols) {
        						// Turn off anti-aliasing so it doesn't affect anything else
        						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
        					}
        				}
        
        				// Need because symbol coordinates have set the last point.
        				if ( !this.useXYCache ) {
        					GRDrawingAreaUtil.setLineWidth( _da_lefty_graph, lineWidth);
    						if (dashedLine) {
        						GRDrawingAreaUtil.setLineDash( _da_lefty_graph, lineDash, 0);
        					}
        					GRDrawingAreaUtil.moveTo(_da_lefty_graph, x, y);
        				}
        				lasty = y;
        				++drawcount;
        				continue;
        			}
        
        			// If get to here need to draw the line or bar.
        			// Shift the bars according to the BarPosition and BarOverlap properties.
        			if (bar_position == -1) {
        				// All bars left of date
        			    if ( barOverlap ) {
        			        centerx = x - full_bar_width_d2;
        			    }
        			    else {
        			        centerx = x - full_bar_width_d2 - (nts - 1) * full_bar_width + its * full_bar_width;
        			    }
        			}
        			else if (bar_position == 1) {
        				// Bar right of date.
                        if ( barOverlap ) {
                            centerx = x + full_bar_width_d2;
                        }
                        else {
                            centerx = x + full_bar_width_d2 + its * full_bar_width;
                        }
        			}
        			else {	
        				// Center on date.
                        if ( barOverlap ) {
                            centerx = x;
                        }
                        else {
                            centerx = x - (nts - 1)* full_bar_width_d2 + its * full_bar_width;
                        }
        			}
        			
        			leftx = centerx - bar_width_d2;
        			
        			if ((leftx >=_data_lefty_limits.getMinX()) && ((leftx + bar_width) <= _data_lefty_limits.getMaxX())){
        				_da_lefty_graph.setColor(tscolor);
        				if (y >= 0.0) {
        					// Positive bars...
        					if (miny >= 0.0) {
        						// From miny up
        						GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, miny, bar_width, (y - miny));
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, miny, leftx, y);
        						}
        					}
        					else {	
        						// From zero up
        						GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, 0.0, bar_width, y);
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, 0.0, leftx, y);
        						}
        					}
        				}
        				else {	
        					// Negative bars.
        					if (maxy >= 0.0) {
        						// Up to zero.
        						GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, y, bar_width, -y);
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, y, leftx, 0.0);
        						}
        					}
        					else {	
        						// Up to top negative value
        						GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, y, bar_width, (maxy - y));
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, y, leftx, maxy);
        						}
        					}
        				}
        
        				GRDrawingAreaUtil.setColor(_da_lefty_graph, _background);
        
        				if (draw_bounding_rectangle) {
        					if (y >= 0.0) {
        						if (miny >= 0.0) {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, miny, leftx, y);
        						}
        						else {
        							GRDrawingAreaUtil.drawLine(	_da_lefty_graph, leftx, 0.0, leftx, y);
        						}
        					}
        					else {	
        						if (maxy >= 0.0) {
        							GRDrawingAreaUtil.drawLine(	_da_lefty_graph, leftx, 0.0, leftx, y);
        						}
        						else {
        							GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, maxy,	leftx, y);
        						}
        					}
        				}
        			}
        		}
        		lasty = y;
        		++drawcount;
        	}
		}
    	// If here and the last line segment was not drawn, draw it
    	//Message.printStatus(2,routine,"pointsInSegment="+pointsInSegment + " yIsMissing="+yIsMissing);
		if ( this.useXYCache && ((__leftYAxisGraphType != TSGraphType.BAR) && (__leftYAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) &&
			(pointsInSegment > 0) && !yIsMissing) {
			// This point is the last in the data so need to draw the line
			// Missing point is found. If the number of points is > 0, draw the line
			GRDrawingAreaUtil.drawPolyline ( _da_lefty_graph, pointsInSegment, this.xCacheArray, this.yCacheArray );
		}
	}
	else {	
		// Loop using addInterval
		DateTime date = new DateTime(start);
		// Make sure the time zone is not set
		date.setTimeZone("");

		TSData tsdata = new TSData();
		// Define a boolean to increase performance below
		boolean doDataPoint = false; // Symbol will be determined from "symbolNoFlag" value
		if ( labelPointWithFlag || (symbolNoFlag != symbolWithFlag) ) {
		    // Symbol will be determined by checking the flag, which is in the data point
		    doDataPoint = true;
		}
		for (; date.lessThanOrEqualTo(end); date.addInterval(interval_base, interval_mult)) {
			// Use the actual data value
			if ( doDataPoint ) {
				tsdata = ts.getDataPoint(date, tsdata);
				y = tsdata.getDataValue();
				dataFlag = tsdata.getDataFlag();
				if ( (dataFlag == null) || (dataFlag.length() == 0) ) {
				    symbol = symbolNoFlag;
				}
				else {
				    symbol = symbolWithFlag;
				}
			}
			else {
			    // No text and no need to check flags for symbol
				y = ts.getDataValue(date);
				dataFlag = null;
			}
			
			if (ts.isDataMissing(y)) {
				lasty = y;
				continue;
			}
			
			if (__leftYAxisGraphType == TSGraphType.PERIOD) {
				// Reset to use the plotting position of the time series, which will result in a
				// horizontal line.  Want the y position to result in the same order as in the legend,
				// where the first time series is at the top of the legend.  This is accomplished by
				// reversing the Y axis for plotting
				y = its + 1;
			}

			// Else, see if we need to moveto or lineto the point
			x = date.toDouble();

			// Uncomment this for debugging
			//Message.printStatus(1, routine, "its=" + its + " date = " + date + " x = " + x + " y=" + y);

			if (((drawcount == 0) || ts.isDataMissing(lasty)) && (__leftYAxisGraphType != TSGraphType.BAR
			    && __leftYAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) {
				// Previous point was missing so all need to do is draw the symbol (if not a reference graph)				
				if (_is_reference_graph) {
					// Don't label or draw symbol.
				}
				else if ( labelPointWithFlag ) {
				    if ( (symbol == GRSymbol.SYM_NONE) || (symbol_size <= 0) ) {
    					// Just text
    					GRDrawingAreaUtil.drawText(_da_lefty_graph,
    						TSData.toString(label_format,
    						label_value_format, date, y, 0.0, 
    						tsdata.getDataFlag().trim(), label_units), x, y, 0.0, label_position);
    				}
    				else {
    					// Symbol and label
    					if (niceSymbols) {
    						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, true);
    					}
    					GRDrawingAreaUtil.drawSymbolText( _da_lefty_graph, symbol, x, y, symbol_size,
    						TSData.toString ( label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
    						label_units ), 0.0, label_position,
    						GRUnits.DEVICE, GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
    					if (niceSymbols) {
    						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be
    						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
    					}
    				}
				}
				else {	
					// Just symbol
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias(_da_lefty_graph, true);
					}
					GRDrawingAreaUtil.drawSymbol(_da_lefty_graph, symbol, x, y, symbol_size,
						GRUnits.DEVICE, GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be
						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
					}
				}

				// Draw the line segment after the symbol
				GRDrawingAreaUtil.moveTo(_da_lefty_graph, x, y);
				lasty = y;
				++drawcount;
				continue;
			}

			// If here, need to draw the line segment or bar...
			if (__leftYAxisGraphType != TSGraphType.BAR && __leftYAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
				if (draw_line) {
					if (dashedLine) {
						GRDrawingAreaUtil.setLineDash( _da_lefty_graph, lineDash, 0);
					}
					
					GRDrawingAreaUtil.setLineWidth(	_da_lefty_graph, lineWidth);
					GRDrawingAreaUtil.lineTo ( _da_lefty_graph, x, y );
					GRDrawingAreaUtil.setLineWidth( _da_lefty_graph, 1);

					if (dashedLine) {
						// turn off the line dashes
						GRDrawingAreaUtil.setLineDash( _da_lefty_graph, null, 0);
					}
				}
				else {	
					GRDrawingAreaUtil.moveTo(_da_lefty_graph, x, y);
				}

				if (_is_reference_graph) {
				}
				else if (labelPointWithFlag && ((symbol == GRSymbol.SYM_NONE) || (symbol_size <= 0))) {
					// Text only
					GRDrawingAreaUtil.drawText(_da_lefty_graph,
						TSData.toString(label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(), 
						label_units), x, y, 0.0, label_position);
				}
				else if (labelPointWithFlag) {
					// Symbol and label
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, true);
					}

					GRDrawingAreaUtil.drawSymbolText( _da_lefty_graph, symbol, x, y, symbol_size,
					        TSData.toString( label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(), 
						label_units ), 0.0, label_position, GRUnits.DEVICE,
						GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);
						
					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be
						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
					}
				}
				else {	
					// Symbol only
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, true);
					}
					
					GRDrawingAreaUtil.drawSymbol( _da_lefty_graph, symbol, x, y, symbol_size, GRUnits.DEVICE,
					        GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y);

					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be
						GRDrawingAreaUtil.setDeviceAntiAlias( _da_lefty_graph, false);
					}
				}

				// Need to override last position from symbol
				GRDrawingAreaUtil.moveTo(_da_lefty_graph, x, y);
			}
			else {	
				// Drawing bars
				if (bar_position == -1) {
					// All bars left of date
                    if ( barOverlap ) {
                        centerx = x - full_bar_width_d2;
                    }
                    else {
                        centerx = x - full_bar_width_d2 - (nts - 1) * full_bar_width + its * full_bar_width;
                    }
				}
				else if (bar_position == 1) {
					// Bar right of date
                    if ( barOverlap ) {
                        centerx = x + full_bar_width_d2;
                    }
                    else {
                        centerx = x + full_bar_width_d2 + its * full_bar_width;
                    }
				}
				else {	
					// Center on date
                    if ( barOverlap ) {
                        centerx = x;
                    }
                    else {
                        centerx = x - (nts - 1) * full_bar_width_d2 + its * full_bar_width;
                    }
				}

				leftx = centerx - bar_width_d2;
				if ((leftx >= _data_lefty_limits.getMinX()) && ((leftx + bar_width) <= _data_lefty_limits.getMaxX())) {
					_da_lefty_graph.setColor(tscolor);
			
        			if (y >= 0.0) {
        				// Positive bars
        				if (miny >=0.0) {
        					// From miny up
        					GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, miny, bar_width, (y - miny));
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine(	_da_lefty_graph, leftx, miny, leftx, y);
        					}
        				}
        				else {	
        					// From zero up
        					GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, 0.0, bar_width, y);
        
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, 0.0, leftx, y);
        					}
        				}
        			}
        			else {	
        				// Negative bars
        				if (maxy >= 0.0) {
        					// Up to zero
        					GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, y, bar_width, -y);
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, y, leftx, 0.0);
        					}
        				}
        				else {	
        					// Up to top
        					GRDrawingAreaUtil.fillRectangle( _da_lefty_graph, leftx, y, bar_width, (maxy - y));
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine(	_da_lefty_graph, leftx, y, leftx, maxy);
        					}
        				}
        			}
        			GRDrawingAreaUtil.setColor(_da_lefty_graph, _background);
        			if (draw_bounding_rectangle) {
        				if (y >= 0.0) {
        					if (miny >= 0.0) {
        						GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, miny, leftx, y);
        					}
        					else {	
        						GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, 0.0, leftx, y);
        					}
        				}
        				else {	
        					if (maxy >= 0.0) {
        						GRDrawingAreaUtil.drawLine( _da_lefty_graph, leftx, 0.0, leftx, y);
        					}
        					else {	
        						GRDrawingAreaUtil.drawLine(	_da_lefty_graph, leftx, maxy, leftx, y);
        					}
        				}
        			}
				}
			}
			lasty = y;
			++drawcount;
		}
	}

	// Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds
	GRDrawingAreaUtil.setClip(_da_lefty_graph, (Shape)null);
	GRDrawingAreaUtil.setClip(_da_lefty_graph, clip);
}

/**
Determine the end date/time for rendering the time series
@param ts the time series being rendered
@return the end date/time for iterating time series data
*/
private DateTime drawTSHelperGetEndDateTime ( TS ts )
{
    DateTime end = new DateTime(_end_date);  
    end.setPrecision(ts.getDataIntervalBase());
    // If the time series data end date is less than the global end
    // date set to local (increases performance).
    if (ts.getDate2().lessThan(end)) {
        end = new DateTime(ts.getDate2());
    }
    return end;
}

/**
Determine the start date/time for drawing the time series.
@param ts the time series being rendered
@return the start date/time for iterating time series data
*/
private DateTime drawTSHelperGetStartDateTime ( TS ts ) {
    // When zoomed in really far, sometimes lines don't draw completely
    // across the edges.  Maybe should decrement the returned DateTime by
    // one time series data interval to make sure it starts outside the
    // page (and will get cropped correctly upon drawing) - need to evaluate this more.
    
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
    if ( ts.getDataIntervalBase() == TimeInterval.HOUR ) {
        DateTime start_withOffset = new DateTime();
        start_withOffset = TSGraph.getNearestDateTimeLessThanOrEqualTo(start, ts);
        start = start_withOffset;
    }
    
    /* TODO SAM 2006-09-28 not sure if Kurt's code is bulletproof for a list of monthly, daily,
    //irregular time series mixed - have him look at some more combinations and
    //possibly consider something like the following...
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
       // End REVISIT SAM 2006-09-28
     */
    
    // Make sure that the iterator for this time series is using a precision
    // that matches the time series data...
    start.setPrecision(ts.getDataIntervalBase());
    
    /*
    // The following was envisioned to optimize the processing.  For now,
    // handle in rendering code to skip data that do not need to be drawn...
    try {
        valid_dates = TSUtil.getValidPeriod ( ts, _start_date, _end_date );
    }
    catch ( Exception e ) {
        return;
    }
    DateTime start  = valid_dates.getDate1();
    DateTime end    = valid_dates.getDate2();
    */
    
    // If the time series data start date is greater than the global start
    // date set to local (increases performance).

    if ( ts.getDate1().greaterThan(start) ) {
        start = new DateTime(ts.getDate1());
    }
    return start;
}

/**
Determine the symbol size used for drawing a time series.
@param its the time series list position (0+, for retrieving properties and messaging)
@param overrideProps run-time override properties to consider when getting graph properties
@param return the symbol style from product properties
*/
private double drawTSHelperGetSymbolSize ( int its, PropList overrideProps )
{
    double symbolSize = StringUtil.atod(getLayeredPropValue("SymbolSize", _subproduct, its, false, overrideProps));
    return symbolSize;
}

/**
Determine the symbol style used for drawing a time series.
@param its the time series list position (0+, for retrieving properties and messaging)
@param overrideProps run-time override properties to consider when getting graph properties
@param flaggedData if true then the symbol is determined from the "FlaggedDataSymbolStyle" property (if not set
then "SymbolStyle" is used); if false the symbol is determined from the "SymbolStyle" property
@return the symbol style from product properties
*/
private int drawTSHelperGetSymbolStyle ( int its, PropList overrideProps, boolean flaggedData )
{
    int symbolStyle = GRSymbol.SYM_NONE;
    if (_is_reference_graph) {
        symbolStyle = GRSymbol.SYM_NONE;
    }
    else {
        String propValue = null;
        if ( flaggedData ) {
        	// Determine symbol for flagged data
            propValue = getLayeredPropValue("FlaggedDataSymbolStyle", _subproduct, its, false, overrideProps);
            //Message.printStatus(2,"","Value of FlaggedDataSymbolStyle=" + propValue);
            if ( (propValue == null) || propValue.equals("") || propValue.equalsIgnoreCase("None") ) {
                // Symbol for flags was specified so use the main symbol style
                propValue = getLayeredPropValue("SymbolStyle", _subproduct, its, false, overrideProps);
                //Message.printStatus(2,"","FlaggedDataSymbolStyle is empty, so use SymbolStyle=" + propValue);
            }
        }
        else {
        	// Determine symbol for not flagged data
            propValue = getLayeredPropValue("SymbolStyle", _subproduct, its, false, overrideProps);
        }
        try {   
            symbolStyle = GRSymbol.toInteger(propValue);
        }
        catch (Exception e) {
            symbolStyle = GRSymbol.SYM_NONE;
        }
    }
    return symbolStyle;
}

/**
Determine the current color used for drawing a time series.  Important - this accesses the product
properties, which must have handled null time series, because the null time series are retained in
data for the graph to preserve positioning.
@param its the time series list position (0+, for retrieving properties and messaging)
@param overrideProps run-time override properties to consider when getting graph properties
@param return the color that is set
*/
private GRColor drawTSHelperGetTimeSeriesColor ( int its, PropList overrideProps )
{
    GRColor tscolor = null;
    try {   
        tscolor = GRColor.parseColor( getLayeredPropValue("Color", _subproduct, its, false, overrideProps));
    }
    catch (Exception e) {
        tscolor = GRColor.black;
    }
    return tscolor;
}

/**
Draw a single time series as an area graph.  The time series values are used to create polygons that have as
a base the zero line.  An array is used to hold the points of the polygon for low-level rendering.  A new polygon
is drawn if a missing value is encountered, the previous and current values have different sign, or the array
buffer is filled.
@param its Counter for time series (starting at 0).
@param ts Single time series to draw.
@param graphType the graph type to use for the time series (may be needed for other calls).
@param overrideProps override run-time properties to consider when getting graph properties
*/
private void drawTSRenderAreaGraph ( int its, TS ts, TSGraphType graphType, PropList overrideProps )
{   //String routine = "TSGraph.drawTSRenderAreaGraph";
    if ( ts == null ) {
        // No data for time series
        return;
    }
    // Generate the clipping area that will be set so that no data are drawn outside of the graph
    Shape clip = GRDrawingAreaUtil.getClip(_da_lefty_graph);
    GRDrawingAreaUtil.setClip(_da_lefty_graph, _da_lefty_graph.getDataLimits());
    
    GRColor tscolor = drawTSHelperGetTimeSeriesColor ( its, overrideProps );
    _da_lefty_graph.setColor(tscolor);
    DateTime start = drawTSHelperGetStartDateTime(ts);
    DateTime end = drawTSHelperGetEndDateTime(ts);
    
    // Loop using addInterval
    DateTime date = new DateTime(start);
    // Make sure the time zone is not set
    date.setTimeZone("");

    double x = 0.0; // X coordinate converted from date/time
    double y = 0.0; // Y coordinate corresponding to data value
    double xPrev = 0.0; // The previous X value
    double yPrev = 0.0; // The previous Y value
    boolean label_symbol = false;
    String label_format = null;
    String label_value_format = null;
    String label_units = null;
    int label_position = 0;
    int countTotal = 0; // Total count of points processed
    int lineWidth = 2;
    // Array to create the polygon
    int arraySize = 5000; // Try this to see how it performs
    int arraySize2 = arraySize - 3; // Limit on number of points in array (to allow for closing points)
    double [] xArray = new double[arraySize];
    double [] yArray = new double[arraySize];
    int arrayCount = 0; // Number of data points put into the array
    // Iterate through data with the iterator
    TSData tsdata = null;
    TSIterator tsi = null;
    try {
        tsi = ts.iterator ( start, end );
    }
    catch ( Exception e ) {
        // Unable to draw (lack of data)
        return;
    }
    boolean haveMoreData = true;
    boolean yIsMissing = false;
    // TODO SAM 2010-11-19 Need a property to control this
    boolean anchorToZero = true; // If true, always anchor around zero.  If false, draw from bottom of graph
    while ( true ) {
        tsdata = tsi.next();
        yIsMissing = false;
        if ( tsdata == null ) {
            // Done with data, but may need to draw buffered points
            haveMoreData = false;
        }
        else {
            ++countTotal;
            xPrev = x;
            yPrev = y;
            date = tsdata.getDate();
            x = date.toDouble();
            y = tsdata.getDataValue();
            if (ts.isDataMissing(y)) {
                yIsMissing = true;
            }
        }
        
        // Determine if need to fill a polygon because of any of the following conditions:
        // 1) Missing value
        // 2) No more data
        // 3) Y is opposite sign of previous value
        // 4) Array buffer is full
        
        if ( yIsMissing || !haveMoreData || (arrayCount == arraySize2) || (y*yPrev < 0.0) ) {
            // Need to draw what is already buffered (but do not draw the current point)
            // Only draw if there was at least one value in the arrays
            if ( arrayCount > 0 ) {
                // If a sequence of missing values, then can skip drawing anything
                // Add two data points as using a y value anchored around zero and repeat the first value
                double yAnchor = 0.0;
                double miny = _data_lefty_limits.getMinY();
                if ( anchorToZero ) {
                    // Always wrap around zero
                    yAnchor = 0.0;
                }
                else {
                    // Anchor to the bottom of the graph
                    yAnchor = miny;
                }
                xArray[arrayCount] = xArray[arrayCount - 1];
                yArray[arrayCount] = yAnchor;
                ++arrayCount;
                xArray[arrayCount] = xArray[0];
                yArray[arrayCount] = yAnchor;
                ++arrayCount;
                xArray[arrayCount] = xArray[0];
                yArray[arrayCount] = yArray[0];
                ++arrayCount;
                //Message.printStatus(2,routine,"Filling polygon with " + arrayCount + " points." );
                GRDrawingAreaUtil.fillPolygon(_da_lefty_graph, arrayCount, xArray, yArray );
            }
            if ( !haveMoreData ) {
                // Done processing data
                break;
            }
            else {
                // Initialize the arrays for the next polygon.  If the value is missing, then there
                // will be a gap.  If the previous value was not missing, then use it to initialize
                // the array so that there will be continuity in the rending.
                arrayCount = 0;
                if ( (countTotal > 0) && !ts.isDataMissing(yPrev) ) {
                    xArray[0] = xPrev;
                    yArray[0] = yPrev;
                    ++arrayCount;
                }
            }
        }
        
        // Now add the current point to the arrays, but only if not missing

        if ( !yIsMissing ) {
            xArray[arrayCount] = x;
            yArray[arrayCount] = y;
            //Message.printStatus ( 2, routine, "Adding data point[" + arrayCount + "]: " + x + "," + y );
            arrayCount++;
        }
    }
    
    // Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds
    GRDrawingAreaUtil.setClip(_da_lefty_graph, (Shape)null);
    GRDrawingAreaUtil.setClip(_da_lefty_graph, clip);
}

/**
Draw a single time series as a raster.  The time series values are used to create rectangles that are color-coded.
@param ts Single time series to draw.
@param graphType the graph type to use for the time series (may be needed for other calls).
@param overrideProps override run-time properties to consider when getting graph properties
*/
private void drawTSRenderRasterGraph ( TS ts, TSGraphType graphType, PropList overrideProps )
{   //String routine = "TSGraph.drawTSRenderRasterGraph";
    if ( ts == null ) {
        // No data for time series
        return;
    }
    // Generate the clipping area that will be set so that no data are drawn outside of the graph
    Shape clip = GRDrawingAreaUtil.getClip(_da_lefty_graph);
    GRDrawingAreaUtil.setClip(_da_lefty_graph, _da_lefty_graph.getDataLimits());
    
    GRColor tscolor;
    //DateTime start = drawTSHelperGetStartDateTime(ts);
    //DateTime end = drawTSHelperGetEndDateTime(ts);
    // FIXME SAM 2013-07-21 The above gets messed up because the data limits are set to an integer range
    DateTime start = ts.getDate1();
    DateTime end = ts.getDate2();
    
    // Loop using addInterval
    DateTime date = new DateTime(start);
    // Make sure the time zone is not set
    date.setTimeZone("");
    
    // Set up the color table.  For now just base on limits of data
    double tsMin = ts.getDataLimits().getMinValue();
    double tsMax = ts.getDataLimits().getMaxValue();
    int nScaleColors = 10;
    int nScaleValues = nScaleColors - 1;
    double delta = (tsMax - tsMin)/nScaleValues;
    double [] scaleValues = new double[nScaleValues];
    GRColor [] scaleColors = (GRColor [])GRColorTable.createColorTable(GRColorTable.BLUE_TO_RED, nScaleColors, true).
        toArray(new GRColor[nScaleColors]);
    for ( int i = 0; i < nScaleValues; i++ ) {
        scaleValues[i] = tsMin + i*delta;
    }
    for ( int i = 0; i < nScaleColors; i++ ) {
        String value = "";
        if ( i < nScaleValues ) {
            value = "" + scaleValues[i];
        }
        Message.printStatus(2, "", "Scale " + value + " color [" + i + "] = " + scaleColors[i].getRed() + "," +
                scaleColors[i].getGreen() + "," + scaleColors[i].getBlue() );
    }

    double x0 = 0.0; // X coordinate converted from date/time, left edge of rectangle
    double y0 = 0.0; // Y coordinate corresponding to year, bottom edge of rectangle
    // Iterate through data with the iterator
    TSData tsdata = null;
    TSIterator tsi = null;
    try {
        tsi = ts.iterator ( start, end );
    }
    catch ( Exception e ) {
        // Unable to draw (lack of data)
        return;
    }
    double value;
    int intervalBase = ts.getDataIntervalBase();
    int yearDay; // Day of year for daily data
    GRColor tscolorPrev = null;
    Message.printStatus(2,"","Drawing raster graph for start=" + start + " end=" + end + " ts.date1=" + ts.getDate1() +
            " ts.date2=" + ts.getDate2() + " data limits = " + _da_lefty_graph.getDataLimits() );
    boolean isLeapYear = false;
    int day = -1;
    int month = -1;
    while ( (tsdata = tsi.next()) != null ) {
        date = tsdata.getDate();
        y0 = date.getYear();
        if ( intervalBase == TimeInterval.DAY ) {
            yearDay = date.getYearDay();
            isLeapYear = TimeUtil.isLeapYear(date.getYear());
            month = date.getMonth();
            day = date.getDay();
            if ( !isLeapYear && (yearDay >= 60)) {
                // Increment non-leap years after Feb 28 so the horizontal axis days will align
                ++yearDay;
            }
            x0 = yearDay;
        }
        else if ( intervalBase == TimeInterval.MONTH ) {
            x0 = date.getMonth();
        }
        value = tsdata.getDataValue();
        if (ts.isDataMissing(value)) {
            // Set color to missing (white);
            tscolor = GRColor.white;
        }
        else {
            // Color is determined from the value.
            // < first value break
            // > last value break
            // >= all other value breaks
            tscolor = GRColor.white;
            for ( int i = 0; i < scaleValues.length; i++ ) {
                if ( value >= scaleValues[scaleValues.length - 1] ) {
                    // Value is >= largest in scale so use the max color
                    tscolor = scaleColors[scaleColors.length - 1];
                    break;
                }
                else if ( value < scaleValues[i] ) {
                    // Searching from values small to high so this should properly select color
                    tscolor = scaleColors[i];
                    break;
                }
            }
        }
        if ( tscolor != tscolorPrev ) {
            // Do this to optimize a bit so color does not have be changed frequently
            _da_lefty_graph.setColor(tscolor);
            tscolorPrev = tscolor;
        }
        // Rectangle will be one "cell", either a day or month
        //if ( tscolor == GRColor.white ) {
        //    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " color=white");
        //}
        //else {
        //    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " color="+
        //            tscolor.getRed() + "," + tscolor.getGreen() + "," + tscolor.getBlue() + ",");
        //}
        GRDrawingAreaUtil.fillRectangle(_da_lefty_graph, x0, y0, 1.0, 1.0);
        // Also fill in the Feb 29 value for non-leap years to the same color as the Feb 28 value.
        // This ensures that a distracting white line is not shown 
        if ( (intervalBase == TimeInterval.DAY) && (month == 2) && (day == 28) && !isLeapYear ) {
            // Also draw the Feb 29, which will not otherwise be encountered becaue the time series is iterating
            // through the actual dates.  Use the same color as Feb 28.
            GRDrawingAreaUtil.fillRectangle(_da_lefty_graph, (x0 + 1.0), y0, 1.0, 1.0);
        }
    }
    
    // Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds
    GRDrawingAreaUtil.setClip(_da_lefty_graph, (Shape)null);
    GRDrawingAreaUtil.setClip(_da_lefty_graph, clip);
}

/**
Draw the X-axis grid.  This calls the drawXAxisDateLabels() if necessary.
*/
private void drawXAxisGrid ( )
{	if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (__leftYAxisGraphType == TSGraphType.DURATION) ||
        (__leftYAxisGraphType == TSGraphType.RASTER) ) {
		// Do the grid here because it uses simple numbers and not dates...
	
		String color_prop = _tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", _subproduct, -1, false );
		if ( (color_prop == null) || color_prop.equalsIgnoreCase("None") ) {
			return;
		}

		GRColor color;
		try {
		    color = GRColor.parseColor(color_prop);
		}
		catch ( Exception e ) {
			color = GRColor.black;
		}
		_da_lefty_graph.setLineWidth ( 1 );
		_da_lefty_graph.setColor ( color );
		double [] y = new double[2];
		y[0] = _data_lefty_limits.getBottomY();
		y[1] = _data_lefty_limits.getTopY();
		// Draw a vertical grid.
		GRAxis.drawGrid ( _da_lefty_graph, _xlabels.length, _xlabels, 2, y, GRAxis.GRID_SOLID );
	}
	else {
	    // Draw the grid in the same code that does the X-axis date/time labels so they are consistent...
		drawXAxisDateLabels ( __leftYAxisGraphType, true );
	}
}

/**
Draw the X-axis date/time labels.  This method can be called with "draw_grid"
set as true to draw the background grid, or "draw_grid" set to false to draw the labels.
@param graphType the graph type being drawn
@param drawGrid if true, draw the x-axis grid lines
@param grid_only If true, only draw the x-axis grid lines.  If false, only draw labels and tic marks.
*/
private void drawXAxisDateLabels ( TSGraphType graphType, boolean drawGrid ) {
	if (!__drawLeftyLabels) {
		return;
	}

	boolean log_y = false;
	boolean log_xy_scatter = false;
	String prop_value = _tsproduct.getLayeredPropValue ( "LeftYAxisType", _subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y = true;
	}
	prop_value = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
		log_xy_scatter = true;
	}	

	if ( drawGrid ) {
		prop_value = _tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", _subproduct, -1, false );
		if ( (prop_value == null) || prop_value.equalsIgnoreCase("None") ) {
			return;
		}
		GRColor color = null;
		try {
		    color = GRColor.parseColor(prop_value);
		}
		catch ( Exception e ) {
			color = GRColor.lightGray;
		}
		GRDrawingAreaUtil.setColor ( _da_lefty_graph, color );
	}
	else {
	    GRDrawingAreaUtil.setColor ( _da_bottomx_label, GRColor.black );
	}

	// Now draw all the labels...

	String fontname = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", _subproduct, -1, false );
	String fontsize = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", _subproduct, -1, false );
	String fontstyle = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", _subproduct, -1, false );
	String yaxisDir = _tsproduct.getLayeredPropValue ( "LeftYAxisDirection", _subproduct, -1, false );
	boolean yaxisDirReverse = false;
	if ( (yaxisDir != null) && yaxisDir.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
	    yaxisDirReverse = true;
	}
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, fontname, fontstyle,	StringUtil.atod(fontsize) );
	
	// Tic mark positions
    double[] xt = new double[2]; // Major ticks
    double[] xt2 = new double[2]; // Minor ticks
    double[] yt = new double[2]; // Major ticks
    double[] yt2 = new double[2]; // Minor ticks
    double tic_height = 0.0; // Height of major tic marks
    yt[0] = _ylabels_lefty[0];
    yt2[0] = _ylabels_lefty[0];
    if ( yaxisDirReverse ) {
        yt[0] = _ylabels_lefty[_ylabels_lefty.length - 1];
        yt2[0] = yt[0];
    }
    // Figure out the y-positions and tic height (same regardless of intervals being used for labels)...
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
    else {
        tic_height = _data_lefty_limits.getHeight()*.02;
        if ( yaxisDirReverse ) {
            // Reverse y axis direction so tics will properly be at bottom where labels are
            yt[1] = yt[0] - tic_height;
            yt2[1] = yt2[0] - tic_height/2.0;
        }
        else {
            // Normal y axis direction so tics will be at bottom
            yt[1] = yt[0] + tic_height;
            yt2[1] = yt2[0] + tic_height/2.0;
        }
    }
    if ( __leftYAxisGraphType == TSGraphType.PERIOD ) {
        // Reversed axes...
    	List<TS> tslist = getEnabledTSList(true,false);
        yt[0] = tslist.size() + 1;
        yt2[0] = tslist.size() + 1;
        tic_height = _data_lefty_limits.getHeight()*.02;
        yt[1] = yt[0] - tic_height;
        yt2[1] = yt2[0] - tic_height/2.0;
    }
    if ( drawGrid ) {
        // Reset with the maximum values...
        yt[0] = _data_lefty_limits.getMinY();
        yt[1] = _data_lefty_limits.getMaxY();
    }
	
    if ( graphType == TSGraphType.RASTER ) {
        // The axis should be labeled with months, with month labels centered between tics
        double x;
        for ( int i = 1; i <= 12; i++ ) {
            x = (_xlabels[i - 1] + _xlabels[i])/2.0;
            // Draw tick marks at the labels (only internal tics, not edges of graph)...
            if ( i != 12 ) {
                xt[0] = _xlabels[i];
                xt[1] = xt[0];
                GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
            }
            GRDrawingAreaUtil.drawText ( _da_bottomx_label, TimeUtil.monthAbbreviation(i), x,
                _datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
        }
        return;
    }

	// This logic for date labels ignores the _xlabels array that was used
	// elsewhere.  Instead, special care is given to check the precision of
	// the dates, the period that is visible, and the font size.  One or two
	// layers of date labels is shown with major (and possibly minor) tic marks.

	DateTime start = null, label_date = null;
	int buffer = 6; // 2*Pixels between labels (for readability)
	int label_width = 0; // Width of a sample label.
	int label0_devx, label1_devx; // X device coordinates for adjacent test labels.
	int label_spacing = 0; // Spacing of labels, center to center.

	if ( (_xaxis_date_precision == DateTime.PRECISION_YEAR) ||
	        ((_end_date.getAbsoluteMonth() - _start_date.getAbsoluteMonth()) > 36) ) {
		// Long periods where showing the year and possibly month are good enough.
		//
		// The top axis label is the year and the bottom label is not used.  Additional criteria are:
		//
		// *	If the period allows all years to be labeled, do it
		// *	If not, try to plot even years.
		// *	Then try every 5 years.
		// *	Then try every 10 years.
		// *	Then try every 20 years.
		// *	Then try every 25 years.
		// *	Then try every 50 years.
		//
		// Apparently "9999" is not the widest string for fonts and picking other numbers or letters
	    // does not always give nice spacing so to be sure try different numbers to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "" + ic + ic + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years
		// can be shown.  Determine by seeing if the first two overlap...
		int[] year_increments ={ 1, 2, 5, 10, 20, 25, 50, 100 };
		int year_increment = 1;
		boolean found = false;
		for (	int itry = 0;
			itry < year_increments.length; itry++ ) {
			start = new DateTime ( _start_date );
			start.setPrecision(DateTime.PRECISION_YEAR );
			
			year_increment = year_increments[itry];
			start.setYear((start.getYear()/year_increment)*year_increment );
			label_date = new DateTime ( start );
			label_date.addYear ( year_increment );
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
			year_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		for ( ; ; date.addYear ( year_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph. 
			if ( !drawGrid ) {
			    // Don't draw minor tics when drawing grid
    			if ( year_increment == 1 ) {
    				if ( label_spacing > 70 ) {
    					// Have enough room for a minor tic mark every month...
    					label_date = new DateTime ( date);
    					label_date.setPrecision ( DateTime.PRECISION_MONTH );
    					label_date.setMonth ( 1 );
    					// Work backwards...
    					for ( int it = 0; it < 11; it++ ) {
    						label_date.addMonth(-1);
    						x=label_date.toDouble();
    						if (x < _data_lefty_limits.getMinX()){
    							continue;
    						}
    						else if ( x > _data_lefty_limits.getMaxX()){
    							continue;
    						}
    						xt2[0] = x;
    						xt2[1] = x;
    						GRDrawingAreaUtil.drawLine ( _da_lefty_graph, xt2, yt2 );
    					}
    				}
    				else {
    				    // Have enough room for a minor tic mark at 6 month interval...
    					label_date = new DateTime ( date);
    					label_date.setPrecision ( DateTime.PRECISION_MONTH );
    					label_date.setMonth ( 1 );
    					label_date.addMonth ( -6 );
    					x=label_date.toDouble();
    					if((x>=_data_lefty_limits.getMinX())&& (x <= _data_lefty_limits.getMaxX())){
    						xt2[0] = x;
    						xt2[1] = x;
    						GRDrawingAreaUtil.drawLine ( _da_lefty_graph, xt2, yt2 );
    					}
    				}
    			}
    			else if ((year_increment == 5) && (label_spacing > 50) ) {
    				// Have enough room for a minor tic mark every year...
    				label_date = new DateTime ( date);
    				// Work backwards...
    				for ( int it = 0; it < 4; it++ ) {
    					label_date.addYear(-1);
    					x=label_date.toDouble();
    					if ( x <_data_lefty_limits.getMinX()){
    						continue;
    					}
    					else if ( x > _data_lefty_limits.getMaxX()){
    						continue;
    					}
    					xt2[0] = x;
    					xt2[1] = x;
    					GRDrawingAreaUtil.drawLine ( _da_lefty_graph, xt2, yt2 );
    				}
    			}
    			else if ( (year_increment == 2) || (year_increment == 10) || (year_increment == 20) ) {
    				// Have enough room for a minor tic in the middle...
    				label_date = new DateTime ( date);
    				label_date.addYear ( -year_increment/2);
    				x=label_date.toDouble();
    				if ( (x >= _data_lefty_limits.getMinX()) && (x <= _data_lefty_limits.getMaxX())){
    					xt2[0] = x;
    					xt2[1] = x;
    					GRDrawingAreaUtil.drawLine ( _da_lefty_graph, xt2, yt2 );
    				}
    			}
			} // end !draw_grid
			// Don't worry about others...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
			else {
			    // Draw the labels and tics...
				GRDrawingAreaUtil.drawText ( _da_bottomx_label, date.toString(), x,
				_datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
		}
	}
	else if ((_xaxis_date_precision==DateTime.PRECISION_MONTH)||
		((_end_date.getAbsoluteDay() - _start_date.getAbsoluteDay() > 90)) ){
		// Months less than 36 months or higher precision data more than 90 days...
		//
		// The top axis label is the month and the bottom label
		// is the year.  Additional criteria are:
		//
		// *	If the period allows all months to be labeled, do it
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
		for ( int itry = 0; itry < month_increments.length; itry++ ) {
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
			// Draw minor tick marks first because they may cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( _da_bottomx_label,
				"" + date.getMonth(),x, _datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getMonth() <= month_increment ) {
					// Label the year...
					GRDrawingAreaUtil.drawText ( _da_bottomx_label, "" + date.getYear(),
					x, _datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw at least one year label...
			date = new DateTime(start);
			for ( ; ; date.addMonth ( month_increment ) ) {
				x = date.toDouble();
				if ( x < _data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_lefty_limits.getMaxX() ) {
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
		// Days less than 60 days or higher precision data more than 7 days (168 hours)...
		//
		// The top axis label is the day and the bottom label is YYYY-MM.  Additional criteria are:
		//
		// *	If the period allows all days to be labeled, do it
		// *	If not, try to plot even days.
		// *	Then try every 7 days.
		// Apparently "99" is not the widest string for fonts and picking other numbers or letters does not always
		// give nice spacing so to be sure try different numbers to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width) {
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
		// When here, do the labeling.  Just label until the plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addDay ( day_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( _da_bottomx_label,	"" + date.getDay(), x,
					_datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getDay() <= day_increment ) {
					// Label the year and month...
					GRDrawingAreaUtil.drawText ( _da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM),
					x, _datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show the year...
			date = new DateTime ( start );
			for ( ; ; date.addDay ( day_increment ) ) {
				x = date.toDouble();
				if ( x < _data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label,	date.toString(DateTime.FORMAT_YYYY_MM), x,
				_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else if ((_xaxis_date_precision == DateTime.PRECISION_HOUR)||
		(TSUtil.calculateDataSize(_start_date,_end_date, TimeInterval.MINUTE,1) > 1440) ) {
		// Hours less than 7 days or minute data more than 1 day...
		//
		// The top axis label is the hour and the bottom label is YYYY-MM-DD.  Additional criteria are:
		//
		// *	If the period allows all hours to be labeled, do it
		// *	If not, try to plot even hours.
		// *	If not, try to plot every 3 hours.
		// *	If not, try to plot every 4 hours.
		// *	If not, try to plot every 6 hours.
		// *	If not, try to plot every 12 hours.
		//
		// Apparently "99" is not the widest string for fonts and picking other numbers or letters does not always
		// give nice spacing so to be sure try different numbers to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
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
			start.setHour((start.getHour()/hour_increment)*hour_increment );
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
		// When here, do the labeling.  Just label until the plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addHour ( hour_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
			else {
			    GRDrawingAreaUtil.drawText ( _da_bottomx_label, "" + date.getHour(), x,
				_datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getHour() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label YYYY-MM-DD...
						GRDrawingAreaUtil.drawText ( _da_bottomx_label,	date.toString(DateTime.FORMAT_YYYY_MM_DD), x,
						        _datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					else {
					    // Label MM-DD...
						GRDrawingAreaUtil.drawText ( _da_bottomx_label,	date.toString(DateTime.FORMAT_MM_DD), x,
						_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			for ( ; ; date.addHour ( hour_increment ) ) {
				x = date.toDouble();
				if ( x < _data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM_DD),
				x, _datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else {
	    // All that is left is minute data less than 1 day...
		//
		// The top axis label is the minute and the bottom label is YYYY-MM-DD HH.  Additional criteria are:
		//
		// *	If the period allows all minutes to be labeled, do it
		// *	If not, try to plot even minutes.
		// *	If not, try to plot every 5 minutes.
		// *	If not, try to plot every 10 minutes.
		// *	If not, try to plot every 15 minutes.
		// *	If not, try to plot every 20 minutes.
		// *	If not, try to plot every 30 minutes.
		//
		// Apparently "99" is not the widest string for fonts and
		// picking other numbers or letters does not always give nice
		// spacing so to be sure try different numbers to get the max likely label size...
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
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
			start.setMinute((start.getMinute()/minute_increment)*minute_increment );
			label_date = new DateTime ( start );
			label_date.addMinute ( minute_increment );
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
			minute_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph...
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addMinute ( minute_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph...
			x = date.toDouble();
			// Now do the major tick marks and labels...
			if ( x < _data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > _data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
			else {
			    GRDrawingAreaUtil.drawText ( _da_bottomx_label, "" + date.getMinute(), x,
					_datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getMinute() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label the YYYY-MM-DD:HH...
						GRDrawingAreaUtil.drawText ( _da_bottomx_label,
						date.toString( DateTime.FORMAT_YYYY_MM_DD)+	":" +
						StringUtil.formatString(date.getHour(),"%02d"), x,
						_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					else {
					    // Label the HH...
						GRDrawingAreaUtil.drawText ( _da_bottomx_label, "" +
						StringUtil.formatString( date.getHour(),"%02d"), x,
						_datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels...
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (_da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			//for ( ; ; date.addMinute ( minute_increment )) {}
			for ( ; ; date.addHour ( 1 )) {
				x = date.toDouble();
				if ( x < _data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > _data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( _da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM_DD)+
				":" + StringUtil.formatString(date.getHour(),"%02d"),
				x, _datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
}

/**
Draw a scatter plot.  One X-axis time series is drawn against multiple Y-axis time series.
@param daGraph drawing area to draw the graph
@param tsproduct product being processed
@param subproduct subproduct number on the product (0+)
@param dataLimits data limits for the drawing area, adjusted for nice axis limits, zoom, etc.
@param tslist list of time series to process - all will be drawn so filtering for left/right axis should already be done.
@param regressionDataList regression data for the time series, precomputed at graph initialization so drawing is fast
*/
private void drawXYScatterPlot ( GRDrawingArea daGraph, TSProduct tsproduct, int subproduct,
	GRLimits dataLimits, List<TS> tslist, List<TSRegression> regressionDataList )
{	String routine = "TSGraph.drawXYScatterPlot";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Scatter data limits are " + _data_lefty_limits.toString() );
	}
	TS ts0 = tslist.get(0);
	if ( ts0 == null) {
		return;
	}
	DateTime start = new DateTime ( ts0.getDate1() );
	DateTime end = new DateTime ( ts0.getDate2() );
	int interval_base = ts0.getDataIntervalBase();
	int interval_mult = ts0.getDataIntervalMult();

	// Assume regression data, even though some may be null...

	int nreg = tslist.size() - 1;

	// Loop through the x-axis (independent) time series and draw, using
	// the same y-axis (dependent) time series for each...

	DateTime date = null;
	TSRegression regressionData = null;
	GRColor plot_color = null;
	TS ts = null;
	String prop_val = null;
	boolean draw_line = true;
	boolean label_symbol = false; // Default...
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
	double F = 0.0, xbar = 0.0, ybar = 0.0, left, right, xsum = 0.0, yhatsum = 0.0;
	double x, y;
	int iyci;
	int n1;
	double dn1;
	String prop_value;
	double min_datay = dataLimits.getMinY();
	double max_datay = dataLimits.getMaxY();

	boolean niceSymbols = true;
	prop_value = tsproduct.getLayeredPropValue( "SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	for (int i = 0, its = 1; i < nreg; i++, its++) {
		ts = tslist.get(its);
		if (ts == null || !isTSEnabled(i)) {
			continue;
		}
		// Draw a the line of best fit (if can't do this can still draw the data below)...
	
		draw_line = true;
		regressionData = regressionDataList.get(i);
		if ( regressionData == null ) {
			Message.printWarning ( 2, routine, "Regression data for TS [" + i + "] is null." );
			draw_line = false;
		}

		prop_val = tsproduct.getLayeredPropValue (	"RegressionLineEnabled", subproduct, its, false );
		if ( (prop_val == null) || prop_val.equalsIgnoreCase("false") ){
			draw_line = false;
		}
		// For now use the font for the bottom x axis tic label for the curve fit line...
		String fontname = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", subproduct, -1, false );
		String fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", subproduct, -1, false );
		String fontstyle = "BOLD";//_tsproduct.getLayeredPropValue ("BottomXAxisLabelFontStyle", _subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( daGraph, fontname, fontstyle, StringUtil.atod(fontsize) );
		boolean [] analyze_month = null;
		// This applies whether monthly or one equation...
		if ( (regressionDataList != null) && draw_line ) {
			analyze_month = regressionData.getAnalyzeMonth();
			// Draw single and monthly lines, if available. For now always draw everything in black...
			// TODO SAM Need CurveFitLineColor with multiple colors.
			analyze_monthly = regressionData.isMonthlyAnalysis();
			GRDrawingAreaUtil.setColor ( daGraph, GRColor.black );
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
				if ( analyze_monthly && !regressionData.isAnalyzed(il) ) {
					continue;
				}
				else if ( !analyze_monthly && !regressionData.isAnalyzed() ) {
					continue;
				}
				if ( analyze_monthly ) {
					try {
					    A = regressionData.getA(il);
						B = regressionData.getB(il);
					}
					catch ( Exception le ) {
						continue;
					}
				}
				else {
				    A = regressionData.getA();
					B = regressionData.getB();
				}
				// Should always know this point...
				xp[0] = dataLimits.getMinX();
				yp[0] = A + xp[0]*B;
				// Make sure Y does not go off the page...
				xp[1] = dataLimits.getMaxX();
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
				GRDrawingAreaUtil.drawLine (daGraph, xp, yp );
				if ( analyze_monthly ) {
					// Number the month at the top of the graph.  Don't optimize the position yet...
					xlabel = xp[1];
					ylabel = yp[1];
					GRDrawingAreaUtil.drawText (daGraph, "" + il, xlabel, ylabel, 0.0, GRText.CENTER_X|GRText.CENTER_Y );
				}
				else {
				    label = StringUtil.formatNumberSequence(regressionData.getAnalysisMonths(), ",");
					if ( (label != null) && !label.equals("") ) {
						xlabel = xp[1];
						ylabel = yp[1];
						GRDrawingAreaUtil.drawText (
						daGraph, label, xlabel, ylabel, 0.0, GRText.CENTER_X|GRText.CENTER_Y );
					}
				}
				prop_value = tsproduct.getLayeredPropValue ( "XYScatterConfidenceInterval", subproduct, its, false );
				try {
				    if ( analyze_monthly ) {
						n1 = regressionData.getN1(il);
					}
					else {
					    n1 = regressionData.getN1();
					}
				}
				catch ( Exception e ) {
					n1 = 0;
				}
				if ( (prop_value != null) && (prop_value.equals("95") || prop_value.equals("99")) && (n1 > 0) ){
					// TODO SAM - need to figure out if this is done in the TSRegression constructor or here - don't want
					// to carry around a lot of points but don't want to hurt performance.  For
					// now do in the draw code since this really only makes sense when only a few points are analyzed.
					// Calculate the information that is necessary to draw the confidence
					// interval - only need to do this when regression data is calculated.
					yp = new double[n1];
					xp = new double[n1];
					yp2 = new double[n1];
					dn1 = (double)n1;
					try {
					F =
					FDistribution.getCumulativeFDistribution ( 2, (n1 - 2), (100 - StringUtil.atoi(prop_value)) );
					if ( analyze_monthly ) {
						ybar = regressionData.getMeanY1(il);
						xbar = regressionData.getMeanX1(il);
					}
					else {
					    ybar = regressionData.getMeanY1();
						xbar = regressionData.getMeanX1();
					}
					}
					catch ( Exception e ) {
						// Should never happen because of the check for N1 above.
					}
					xsum = 0.0;
					yhatsum = 0.0;
					date = new DateTime ( start );
					for (	;
						date.lessThanOrEqualTo( end );
						date.addInterval(interval_base, interval_mult) ) {
						if ( !analyze_month[date.getMonth() - 1] ) {
							continue;
						}
						if ( analyze_monthly && !regressionData.isAnalyzed( date.getMonth()) ) {
							continue;
						}
						else if ( !analyze_monthly && !regressionData.isAnalyzed() ){
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
						// Calculate the totals that are needed...
						yhatsum += (((A + B*x) - y)*((A + B*x) - y));
						xsum += ((x - xbar)*(x - xbar));
					}
					date = new DateTime ( start );
					iyci = 0;
					for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
						if ( !analyze_month[date.getMonth() - 1] ) {
							continue;
						}
						if ( analyze_monthly && !regressionData.isAnalyzed( date.getMonth()) ) {
							continue;
						}
						else if ( !analyze_monthly && !regressionData.isAnalyzed() ){
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
							Math.sqrt( (1.0/(dn1 - 2.0))* yhatsum) * Math.sqrt(1/dn1 + ((x - xbar)*(x - xbar))/xsum);
						xp[iyci] = x;
						yp[iyci] = left + right;
						yp2[iyci++] = left - right;
					}
					// Sort the X coordinates so that the line is drawn without zig-zagging back on itself...
					sort_order = new int[n1];
					MathUtil.sort ( xp, MathUtil.SORT_QUICK, MathUtil.SORT_ASCENDING, sort_order, true );
					yp_sorted = new double[n1];
					yp2_sorted = new double[n1];
					xp2_sorted = new double[n1];
					for ( int i2 = 0; i2 < iyci; i2++ ) {
						yp_sorted[i2] = yp[sort_order[i2]];
						xp2_sorted[i2] = xp[i2];
						yp2_sorted[i2] = yp2[sort_order[i2]];
					}
					// Adjust to make sure points are on the graph...
					adjustConfidenceCurve ( xp, yp_sorted, iyci );
					adjustConfidenceCurve ( xp2_sorted,	yp2_sorted, iyci );
					// Now draw lines...
					GRDrawingAreaUtil.drawPolyline ( daGraph, iyci, xp, yp_sorted );
					GRDrawingAreaUtil.drawPolyline ( daGraph, iyci, xp2_sorted, yp2_sorted );
				}
			}
		}
	
		// Now draw the data only for months that have been analyzed...
	
		try {
		    plot_color = GRColor.parseColor( tsproduct.getLayeredPropValue ( "Color", subproduct, its, false ) );
		}
		catch ( Exception e ) {
			plot_color = GRColor.black;
		}
		GRDrawingAreaUtil.setColor ( daGraph, plot_color );
		prop_value = tsproduct.getLayeredPropValue ( "SymbolStyle", subproduct, its, false );
		int symbol = GRSymbol.SYM_NONE;
		try {
		    symbol = GRSymbol.toInteger(prop_value);
		}
		catch ( Exception e ) {
			symbol = GRSymbol.SYM_NONE;
		}
		double symbol_size = StringUtil.atod( tsproduct.getLayeredPropValue ( "SymbolSize", subproduct, its, false ) );
		// First try to get the label format from the time series properties...
		label_format = tsproduct.getLayeredPropValue (	"DataLabelFormat", subproduct, its, false );
		if ( label_format.equals("") ) {
			// Try to get from the graph properties....
			label_format = tsproduct.getLayeredPropValue ( "DataLabelFormat", subproduct, -1, false );
			if ( !label_format.equals("") ) {
				// Label the format...
				label_symbol = true;
			}
		}
		else {
		    label_symbol = true;
		}
		if ( label_symbol ) {
			// Are drawing point labels so get the position, set the font, and get the format...
			label_position_string = tsproduct.getLayeredPropValue ( "DataLabelPosition", subproduct, its, false );
			if ( label_position_string.equals("") || label_position_string.equalsIgnoreCase("Auto")){
				// Try to get from the graph properties....
				label_position_string =	tsproduct.getLayeredPropValue ( "DataLabelPosition", subproduct, -1, false );
				if ( label_position_string.equals("") || label_position_string.equalsIgnoreCase( "Auto") ) {
					// Default position...
					label_position_string = "Right";
				}
			}
			label_position = GRText.CENTER_Y | GRText.LEFT;
			try {
			    label_position = GRText.parseTextPosition (	label_position_string );
			}
			catch ( Exception e ) {
				label_position = GRText.CENTER_Y | GRText.LEFT;
			}
			// The font is only defined at the graph level.  Set for point labels...
			fontname = tsproduct.getLayeredPropValue ( "DataLabelFontName", subproduct, -1, false );
			fontsize = tsproduct.getLayeredPropValue ( "DataLabelFontSize", subproduct, -1, false );
			fontstyle = tsproduct.getLayeredPropValue ( "DataLabelFontStyle", subproduct, -1, false );
			GRDrawingAreaUtil.setFont ( daGraph, fontname, fontstyle, StringUtil.atod(fontsize) );
			// Determine the format for the data value in case it is needed to format the label...
			label_units = ts.getDataUnits();
			label_value_format = DataUnits.getOutputFormatString( label_units, 0, 4 );
		}
		date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			// If drawing a line, only draw points that are appropriate for the line.  If not drawing the line
			// of best fit, draw all available data...
			if ( draw_line && !analyze_month[date.getMonth() - 1] ) {
				continue;
			}
			if ( draw_line && analyze_monthly && !regressionData.isAnalyzed(date.getMonth()) ) {
				continue;
			}
			else if ( draw_line && !analyze_monthly && (!regressionData.isAnalyzed() && (regressionData.getN1() != 1)) ) {
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

			//Message.printStatus ( 1, routine, "SAMX Drawing " + x + "," + y );
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
			}
			if ( label_symbol ) {
				GRDrawingAreaUtil.drawSymbolText ( daGraph, symbol, x, y, symbol_size,
					TSData.toString ( label_format,label_value_format, date, x, y, "", label_units ), 0.0,
					label_position,	GRUnits.DEVICE,GRSymbol.SYM_CENTER_X|GRSymbol.SYM_CENTER_Y );
			}
			else {
			    GRDrawingAreaUtil.drawSymbol ( daGraph, symbol, x, y, symbol_size,
					GRUnits.DEVICE,	GRSymbol.SYM_CENTER_X|GRSymbol.SYM_CENTER_Y );
			}
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(daGraph, false);
			}
		}
	}
}

/**
Draw the Y-axis grid lines.  Currently only the major grid lines are drawn.
*/
private void drawYAxisGrid()
{	String prop_value = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor", _subproduct, -1, false );
	if ( (prop_value == null) || prop_value.equalsIgnoreCase("None") ) {
		return;
	}

	GRColor color;
	try {
	    color = GRColor.parseColor(prop_value);
	}
	catch ( Exception e ) {
		color = GRColor.black;
	}
	_da_lefty_graph.setColor ( color );
	double [] x = new double[2];
	x[0] = _data_lefty_limits.getLeftX();
	x[1] = _data_lefty_limits.getRightX();
	// Draw a horizontal grid.
	GRAxis.drawGrid ( _da_lefty_graph, 2, x, _ylabels_lefty.length, _ylabels_lefty, GRAxis.GRID_SOLID );
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
	_data_lefty_limits = null;
	_max_tslimits_lefty = null;
	_tslimits_lefty = null;
	_max_lefty_data_limits = null;
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
	_da_lefty_graph = null;
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
	_drawlim_lefty_graph = null;
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
	_ylabels_lefty = null;
	super.finalize();
}

/**
Format a data point for a tracker "X: xxxxx,  Y: yyyyy".
If right y-axis is used, format as:  "LEFT X: xxxxx,  Y: yyyyy / RIGHT X: xxxxx,  Y:  yyyyy".
@param devpt Device point to format (needed to lookup right y-axis value).
@param datapt Data point to format.
*/
public String formatMouseTrackerDataPoint ( GRPoint devpt, GRPoint datapt )
{	if ( datapt == null ) {
		return "";
	}
	else if ((__leftYAxisGraphType == TSGraphType.DOUBLE_MASS) ||
		(__leftYAxisGraphType == TSGraphType.DURATION) ||
		(__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
		return "X:  " + StringUtil.formatString(datapt.x,"%.2f") +
			",  Y:  " + StringUtil.formatString(datapt.y,"%." + _lefty_precision + "f");
	}
    else if ( __leftYAxisGraphType == TSGraphType.RASTER ) {
        // If the maximum value is <= 12, then the x axis is months
        String x = "";
        String valueString = "";
        TSData tsdata;
        double value;
        String flag = "", flagString = "";
        int year = (int)(datapt.y);
        TS ts = null;
        if ( datapt.associated_object != null ) {
            if ( datapt.associated_object instanceof TS ) {
                ts = (TS)datapt.associated_object;
            }
        }
        DateTime d = new DateTime(DateTime.DATE_FAST);
        d.setYear(year);
        if ( _data_lefty_limits.getMaxX() <= 12.0 ) {
            // Monthly data
            int month = (int)datapt.x;
            x = "" + month;
            d.setMonth(month);
            if ( ts != null ) {
                tsdata = ts.getDataPoint(d, null);
                value = tsdata.getDataValue();
                flag = tsdata.getDataFlag();
                if ( (flag != null) && !flag.equals("") ) {
                    flagString = " (" + flag + ")";
                }
                if ( ts.isDataMissing(value) ) {
                    valueString = ", TS:  missing" + flagString;
                }
                else {
                    // TODO SAM 2013-07-31 Need to figure out precision from data, but don't look up each
                    // call to this method because a performance hit?
                    valueString = ", TS:  " + StringUtil.formatString(value,"%.2f" + ts.getDataUnits() + flagString );
                }
            }
        }
        else if ( _data_lefty_limits.getMaxX() <= 366.0 ) {
            // Graph was set up to always have leap year
            int dayInYear = (int)datapt.x;
            boolean isLeapYear = TimeUtil.isLeapYear(year);
            if ( (dayInYear == 60) && !isLeapYear ) {
                // Treat as missing since actual year does not have Feb 29
                return "No value (Feb 29 of non-leap year)";
            }
            else {
                int [] md;
                if ( !isLeapYear && (dayInYear > 59) ) {
                    // If not a leap year and past day 59, need to offset the day by one and recompute to get the
                    // actual day to retrieve the correct data value.  This is because Feb 29 always has a plotting
                    // position in order to ensure days line up with months.
                    --dayInYear;
                }
                md = TimeUtil.getMonthAndDayFromDayOfYear(year, dayInYear);
                x = "Day " + dayInYear + " (" + TimeUtil.monthAbbreviation(md[0]) + " " + md[1] + ")";
                d.setMonth(md[0]);
                d.setDay(md[1]);
                if ( ts != null ) {
                    tsdata = ts.getDataPoint(d, null);
                    value = tsdata.getDataValue();
                    flag = tsdata.getDataFlag();
                    if ( (flag != null) && !flag.equals("") ) {
                        flagString = " (" + flag + ")";
                    }
                    if ( ts.isDataMissing(value) ) {
                        valueString = ", TS:  missing" + flagString;
                    }
                    else {
                        valueString = ", TS:  " + StringUtil.formatString(value,"%.2f") + " " +
                            ts.getDataUnits() + flagString;
                    }
                }
            }
        }
        return "X:  " + x + ",  Y:  " + year + valueString;
    }
	else {
		// Simple graph type
	    DateTime mouse_date = new DateTime(datapt.x, true);
		mouse_date.setPrecision ( _xaxis_date_precision );
		GRPoint dataptRightYAxis = null;
		if ( __rightYAxisGraphType != TSGraphType.NONE ) {
			// Look up data point from the device coordinates
			dataptRightYAxis = _da_righty_graph.getDataXY( devpt.getX(), devpt.getY(), GRDrawingArea.COORD_DEVICE );
		}
		if ( _bottomx_date_format > 0 ) {
			String leftYString = "X:  " + mouse_date.toString(_bottomx_date_format) + ",  Y:  "
				+ StringUtil.formatString(datapt.y,"%." + _lefty_precision + "f");
			if ( (__rightYAxisGraphType != TSGraphType.NONE) && (dataptRightYAxis != null) ) {
				// Need to also show right y-axis
				String rightYString = "X:  " + mouse_date.toString(_bottomx_date_format) + ",  Y:  "
						+ StringUtil.formatString(datapt.y,"%." + _righty_precision + "f");
				return "LEFT: " + leftYString + " / RIGHT: " + rightYString;
			}
			else {
				return leftYString;
			}
		}
		else {
		    String leftYString = "X:  " + mouse_date.toString() + ",  Y:  " + StringUtil.formatString(datapt.y,"%." + _lefty_precision + "f");
			if ( (__rightYAxisGraphType != TSGraphType.NONE) && (dataptRightYAxis != null) ) {
				// Need to also show right y-axis
				String rightYString = "X:  " + mouse_date.toString() + ",  Y:  " + StringUtil.formatString(datapt.y,"%." + _righty_precision + "f");
				return "LEFT: " + leftYString + " / RIGHT: " + rightYString;
			}
			else {
				return leftYString;
			}
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
{	return _data_lefty_limits;
}

/**
Return the derived time series list used for graphing.
@param enabledOnly if true return only time series that are enabled.  If false, return all.
@return the derived time series list being graphed.
*/
public List<TS> getDerivedTSList()
{   return __derivedTSList;
}

/**
Returns a list of all the time series that are enabled.  This will never 
return null.  If no time series are enabled, an empty list will be returned.
@param includeLeftYAxis if true, include left y-axis time series.
@param includeRightYAxis if true, include right y-axis time series.
@return a list of all the time series that are enabled.
*/
public List<TS> getEnabledTSList(boolean includeLeftYAxis, boolean includeRightYAxis) {
	if (__tslist == null || __tslist.size() == 0) {
		return new ArrayList<TS>();
	}

	int size = __tslist.size();
	String propValue = null;
	List<TS> tslist = new ArrayList<TS>();
	for (int i = 0; i < size; i++) {
		propValue = _tsproduct.getLayeredPropValue("Enabled", _subproduct, i, false);
		if ( (propValue != null) && propValue.equalsIgnoreCase("False")) {
			// skip it
		}
		else {
			propValue = _tsproduct.getLayeredPropValue("Axis", _subproduct, i, false);
			if ( (propValue == null) || propValue.isEmpty() ) {
				propValue = "Left";
			}
			if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
				tslist.add(__tslist.get(i));
			}
			if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
				tslist.add(__tslist.get(i));
			}
		}
	}
	return tslist;
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
public TS getFirstEnabledTS(boolean includeLeftYAxis, boolean includeRightYAxis) {
	List<TS> v = getEnabledTSList(includeLeftYAxis, includeRightYAxis);
	if (v.size() == 0) {
		return null;
	}
	return v.get(0);
}

/**
Return the drawing area used for the graph.  The value may be null.
@return The drawing area used for the graph.
*/
public GRJComponentDrawingArea getGraphDrawingArea()
{	return _da_lefty_graph;
}

/**
Returns a prop value from the product, taking into account any override 
properties that may be set in the graph.  This method is used when drawing time series.
@param key the key of the property to return.
@param subproduct the subproduct of the property to return
@param its the number of the time series of the property to return
@param annotation if true, then the property is an annotation
@param overrideProps if not null, this is the proplist that will be checked for the property.
@return the prop value, or null if the property does not exist
*/
private String getLayeredPropValue(String key, int subproduct, int its,
boolean annotation, PropList overrideProps) {
	if (overrideProps == null) {
		return _tsproduct.getLayeredPropValue( key, subproduct, its, annotation);
	}
	else {
		String propValue = overrideProps.getValue(key);
		if (propValue == null) {
			return _tsproduct.getLayeredPropValue( key, subproduct, its, annotation);
		}
		else {
			return propValue;
		}
	}
}

/**
Return the graph type for the left y-axis.
@return the graph type for the left y-axis.
*/
public TSGraphType getLeftYAxisGraphType ()
{	return __leftYAxisGraphType;
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
		// This is consistent with how the legend drawing area was set up.
		return null;
	}
	
	// Subproduct legend format, which will provide a default if the time series legend format is "Auto"...
	String subproduct_legend_format = _tsproduct.getLayeredPropValue( "LegendFormat", _subproduct, -1, false);
	// Determine the legend format for the specific time series.  If the
	// label is "Auto", define using the default (however, if Auto and the
	// subproduct is not auto, use the subproduct format).  If blank, don't draw the legend...
	String legend_format = _tsproduct.getLayeredPropValue("LegendFormat", _subproduct, i, false );
	if ( legend_format == null ) {
		// Try the legend format for the subproduct...
		legend_format = subproduct_legend_format;
	}
	if ( (legend_format == null) || (legend_format.length() == 0) ) {
		// Do not draw a legend.  Later might add a LegendEnabled, which
		// would totally turn off the legend (LegendFormat is really more for the string label).
		return null;
	}
	else if ( !legend_format.equalsIgnoreCase("Auto") ) {
		// A specific legend has been specified...
		legend = ts.formatLegend ( legend_format );
	}
	// Below here "Auto" is in effect...
	else if ( (ts.getLegend() != null) && (ts.getLegend().length() != 0) ) {
		// The time series data itself has legend information so use it...
		// TODO SAM 2008-04-28 Should this even be allowed any more now that properties are used? -
	    // probably for applications that want more control.
		legend = ts.formatLegend ( ts.getLegend() );
	}
	else if ( !subproduct_legend_format.equalsIgnoreCase("Auto") ) {
		// Use the subproduct legend...
		legend = ts.formatLegend ( subproduct_legend_format );
	}
	else {
	    // "Auto", format the legend manually...
		if ( _ignoreLeftAxisUnits && !ts.getDataUnits().equals("") ) {
			// Add units to legend because they won't be on the axis label...
			if ( ts.getAlias().equals("") ) {
				legend = ts.getDescription() + ", " +
					ts.getIdentifierString() + ", " +
					ts.getDataUnits() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
			else {
			    legend = ts.getAlias() + " - " +
					ts.getDescription() + ", " +
					ts.getIdentifierString() + ", " +
					ts.getDataUnits() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
		}
		else {
		    // Don't put units in legend...
			if ( ts.getAlias().equals("") ) {
				legend = ts.getDescription() + ", " +
					ts.getIdentifierString() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
			else {
			    legend = ts.getAlias() + " - " +
					ts.getDescription() + ", " +
					ts.getIdentifierString() + " (" +
					ts.getDate1() + " to " +
					ts.getDate2() + ")";
			}
		}
	}
	if ( __leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
		if ( i == 0 ) {
			legend = "Y (dependent): " + legend;
		}
	}
	else if ( __leftYAxisGraphType == TSGraphType.PERIOD ) {
		legend = (i + 1) + ") " + legend;
	}
	return legend;
}

/**
Return the maximum data limits.
*/
public GRLimits getMaxDataLimits ()
{	return _max_lefty_data_limits;
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
@return the number of time series (useful for automated selection of colors, symbols, etc.)
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
		// the menu was already created in a previous call and can be returned 
		return _graph_JPopupMenu;
	}
	
	// The popups are not created by default because the graph may be
	// being created in batch mode, or the user made not use popups.
	_graph_JPopupMenu = new JPopupMenu("Graph");
	
	// Add properties specific to each graph type
	if ( (__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (__leftYAxisGraphType == TSGraphType.DURATION) ) {
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_ANALYSIS_DETAILS,__MENU_ANALYSIS_DETAILS,this));
		_graph_JPopupMenu.addSeparator();
	}
	
	// All graphs have properties
	_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_PROPERTIES, __MENU_PROPERTIES, this));

	// Add ability to set Y Maximum values
	_graph_JPopupMenu.addSeparator();

	if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_VISIBLE, __MENU_Y_MAXIMUM_VISIBLE, this));
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_VISIBLE, __MENU_Y_MINIMUM_VISIBLE, this));
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_AUTO, __MENU_Y_MAXIMUM_AUTO, this));
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_AUTO, __MENU_Y_MINIMUM_AUTO, this));
	}
	else {
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_VISIBLE, __MENU_Y_MAXIMUM_VISIBLE, this));
		_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_AUTO, __MENU_Y_MAXIMUM_AUTO, this));
	}
	
	// If UNIX, add a refresh option to help with X server differences
	if (IOUtil.isUNIXMachine()) {
		_graph_JPopupMenu.addSeparator();
		_graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_REFRESH, __MENU_REFRESH, this));
	}
	return _graph_JPopupMenu;
}

/**
Return the Vector of TSRegression data that applies to the graph, if available.
This can be displayed in a details window for the graph.
@return TSRegression data for graph (use with scatter plot) or null if no analysis has been performed.
*/
public List getRegressionData ()
{	return _regression_data;
}

/**
Return the graph type for the right y-axis.
@return the graph type for the right y-axis.
*/
public TSGraphType getRightYAxisGraphType ()
{	return __rightYAxisGraphType;
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
Get the time series graph type.  Determine from the TSProduct properties and if not set, return the
graph type for the graph.
 */
private TSGraphType getTimeSeriesGraphType ( TSGraphType mainGraphType, int its )
{
    // Do not request the layered property here.  Ask for time series property explicitly
    // and then set to the main graph type if no time series property
    String graphTypeProp = _tsproduct.getLayeredPropValue ( "GraphType", _subproduct, its, false );
    //Message.printStatus(2, "", "time series graph type is " + graphTypeProp );
    TSGraphType tsGraphType = TSGraphType.valueOfIgnoreCase(graphTypeProp);
    if ( tsGraphType == null ) {
        tsGraphType = mainGraphType;
    }
    return tsGraphType;
}

/**
Return the time series list used for graphing (all the time series).
@return the time series list being graphed (all the time series).
*/
public List<TS> getTSList()
{	return __tslist;
}

/**
Return the time series list used for graphing (time series for the left y-axis).
@return the time series list being graphed (time series for the left y-axis).
*/
public List<TS> getTSListForLeftYAxis ()
{
	return __left_tslist;
}

/**
Return the time series list used for graphing (time series for the right y-axis).
@return the time series list being graphed (time series for the right y-axis).
*/
public List<TS> getTSListForRightYAxis ()
{
	return __right_tslist;
}

/**
Return the time series list used for graphing.  Depending on the graph type, this may by the original
time series, the derived time series list, or both.  This method should be called to get the time series
that are actually drawn (with no need for further manipulation). This method is called by code that
determines graph limits, although actually rendering code may take more care.
@param enabledOnly if true, only return the list of enabled time series
@param includeLeftYAxis if true, include left y-axis time series.
@param includeRightYAxis if true, include right y-axis time series.
@return the time series list being rendered.
*/
private List<TS> getTSListToRender ( boolean enabledOnly, boolean includeLeftYAxis, boolean includeRightYAxis )
{   TSGraphType leftYAxisGraphType = getLeftYAxisGraphType();
	TSGraphType rightYAxisGraphType = getRightYAxisGraphType();
    List<TS> tslist = null;
    if ( enabledOnly ) {
        // Render only the enabled time series
        tslist = getEnabledTSList(includeLeftYAxis, includeRightYAxis);
    }
    else {
        // Render all time series
        tslist = getTSList();
        // Filter the time series list based on the axis
        String propValue;
        List<TS> tslist2 = new ArrayList<TS>();
        for (int i = 0; i < tslist.size(); i++) {
			propValue = _tsproduct.getLayeredPropValue("Axis", _subproduct, i, false);
			if ( (propValue == null) || propValue.isEmpty() ) {
				propValue = "Left";
			}
			if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
				tslist2.add(__tslist.get(i));
			}
			if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
				tslist2.add(__tslist.get(i));
			}
    	}
        tslist = tslist2;
    }
    if ( includeLeftYAxis && (leftYAxisGraphType == TSGraphType.AREA_STACKED) ) {
        // Return the derived time series list and the time series not in this list
        List<TS> tsToRender = new ArrayList<TS>();
        tsToRender.addAll(getDerivedTSList());
        // Now loop through and add the additional time series
        int size = tslist.size();
        for ( int its = 0; its < size; its++ ) {
            TSGraphType tsGraphType = getTimeSeriesGraphType(leftYAxisGraphType, its);
            if ( tsGraphType != leftYAxisGraphType ) {
                tsToRender.add ( tslist.get(its) );
            }
        }
        return tsToRender;
    }
    else if ( includeLeftYAxis && (leftYAxisGraphType == TSGraphType.RASTER) ) {
        // Return the first time series in the list since only one time series can be displayed
        List<TS> tsToRender = new ArrayList<TS>();
        if ( tslist.size() > 0 ) {
            tsToRender.add(tslist.get(0));
        }
        return tsToRender;
    }
    else {
    	// Simpler graph so render all that were found
        return tslist;
    }
}

/**
Indicate whether the left y-axis graph drawing area for this TSGraph contains the
device point that is specified.  This is used to determine whether a component
event should impact this TSGraph.
@param devpt a point of interest, in raw device units (not GR plotting units).
@return true if devpt is in the graph drawing area.
*/
public boolean graphContains ( GRPoint devpt )
{	// The check MUST be done in device units because more than one graph may share the same device units...
	// be able to optimize this when there is time.
	return _da_lefty_graph.getPlotLimits(GRDrawingArea.COORD_DEVICE).contains ( devpt);
}

/**
Indicate whether units are being ignored on the left axis.
*/
public boolean ignoreLeftYAxisUnits ()
{	return _ignoreLeftAxisUnits;
}

/**
Indicate whether units are being ignored on the right axis.
*/
public boolean ignoreRightYAxisUnits ()
{	return _ignoreRightAxisUnits;
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
	String propValue = _tsproduct.getLayeredPropValue("Enabled", _subproduct, its, false);
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
{	boolean log_y_left = false;
	boolean log_y_right = false;
	boolean log_xy_scatter = false;
	String prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisType", _subproduct, -1, false );
	if ( prop_val.equalsIgnoreCase("Log") ) {
		log_y_left = true;
	}
	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisType", _subproduct, -1, false );
	if ( prop_val.equalsIgnoreCase("Log") ) {
		log_y_right = true;
	}
	prop_val = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false );
	if ((prop_val != null) && prop_val.equalsIgnoreCase("Log")) {
		log_y_left = false;
		log_xy_scatter = true;
	}	
	// Full page...

	_da_page = new GRJComponentDrawingArea ( _dev, "TSGraph.Page",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_page = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_page.setDataLimits ( _datalim_page );

	// Drawing area for main title...

	_da_maintitle = new GRJComponentDrawingArea ( _dev,
			"TSGraph.MainTitle", GRAspect.FILL,	null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_maintitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_maintitle.setDataLimits ( _datalim_maintitle );

	// Drawing area for sub title...

	_da_subtitle = new GRJComponentDrawingArea ( _dev,
			"TSGraph.SubTitle", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_subtitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_subtitle.setDataLimits ( _datalim_subtitle );

	// Top X axis...

	_da_topx_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.TopXTitle", GRAspect.FILL,	null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_topx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_topx_title.setDataLimits ( _datalim_topx_title );

	_da_topx_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.TopXLabels", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_topx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_topx_label.setDataLimits ( _datalim_topx_label );
	if (log_xy_scatter) {
		// both axes are log
//		GRDrawingAreaUtil.setAxes(_da_topx_label, GRAxis.LOG, GRAxis.LINEAR);
	}

	// Y axis titles...

	_da_lefty_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftYTitle", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_lefty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_lefty_title.setDataLimits ( _datalim_lefty_title );

	_da_righty_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.RightYTitle", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_righty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_righty_title.setDataLimits ( _datalim_righty_title );

	// Left Y axis labels...

	_da_lefty_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftYLabel", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_lefty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_lefty_label.setDataLimits ( _datalim_lefty_label );
	if (log_y_left) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_lefty_label, GRAxis.LINEAR, GRAxis.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(_da_lefty_label, GRAxis.LINEAR, GRAxis.LOG);
	}

	// Drawing area for left y-axis graphing...

	_da_lefty_graph = new GRJComponentDrawingArea ( _dev, "TSGraph.LeftYAxisGraph", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	// Initial values that will be reset pretty quickly...
	GRLimits datalim_lefty_graph = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
	    datalim_lefty_graph.reverseY();
	}
	_da_lefty_graph.setDataLimits ( datalim_lefty_graph );

	if (log_y_left) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_lefty_graph, GRAxis.LINEAR, GRAxis.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(_da_lefty_graph, GRAxis.LINEAR, GRAxis.LINEAR);
	}
	
	// Drawing area for right y-axis graphing...

	_da_righty_graph = new GRJComponentDrawingArea ( _dev, "TSGraph.RightYAxisGraph", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	// Initial values that will be reset pretty quickly...
	GRLimits datalim_righty_graph = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	if ( __rightyDirection == GRAxisDirectionType.REVERSE ) {
	    datalim_righty_graph.reverseY();
	}
	_da_righty_graph.setDataLimits ( datalim_righty_graph );
	if (log_y_right) {
		// For now, only support log axes in the Y axis...
		GRDrawingAreaUtil.setAxes(_da_righty_graph, GRAxis.LINEAR, GRAxis.LOG);
	}

	// Right Y axis labels...

	_da_righty_label = new GRJComponentDrawingArea ( _dev,
		"TSGraph.RightYLabel", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_righty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_righty_label.setDataLimits ( _datalim_righty_label );

	// Drawing area for bottom X axis...

	_da_bottomx_label = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomXLabel",	GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_bottomx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );
	if (log_xy_scatter) {
		// both axes are log
//		GRDrawingAreaUtil.setAxes(_da_bottomx_label, GRAxis.LOG, GRAxis.LINEAR);
	}

	_da_bottomx_title = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomXTitle",	GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_bottomx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottomx_title.setDataLimits ( _datalim_bottomx_title );

	// Legend (open drawing areas for each legend area, although currently only one will be used)...

	_da_bottom_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.BottomLegend",	GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_bottom_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_bottom_legend.setDataLimits ( _datalim_bottom_legend );

	_da_left_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.LeftLegend", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_left_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_left_legend.setDataLimits ( _datalim_left_legend );

	_da_right_legend = new GRJComponentDrawingArea ( _dev,
			"TSGraph.RightLegend", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_right_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_right_legend.setDataLimits ( _datalim_right_legend );

	_da_inside_legend = new GRJComponentDrawingArea(_dev,
		"TSGraph.InsideLegend", GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null);
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
			Message.printDebug ( 1, routine, _gtype + "Painting TSGraph for printing..." );
		}
		else if ( _is_reference_graph ) {
			Message.printDebug ( 1, routine, _gtype + "Painting reference graph..." );
		}
		else {
		    Message.printDebug ( 1,	routine, _gtype + "Painting main graph..." );
		}
	}

	try {
		// Main try...
		// The following will be executed at initialization.  The code is here
		// because a valid Graphics is needed to check font sizes, etc.
	
		_graphics = g;
	
		// Now set the drawing limits based on the current size of the device.
	
		setDrawingLimits ( _drawlim_page );
	
		// If the graph type has changed, redo the data analysis.
		// Currently the Properties interface does not allow the graph type to
		// be changed.  However, XY Scatter parameters can be changed.
	
		boolean need_to_analyze = false;
		if ( __leftYAxisGraphType != __lastLeftYAxisGraphType ) {
			need_to_analyze = true;
		}
		if ( __leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			// Check the properties in the old regression results and see
			// if they differ from the current TSProduct properties.  If
			// they do, then reanalyze the data.  Assume for now that the
			// properties for the first regression apply to all regression data...
			
			if ((_regression_data != null) && (_regression_data.size()>0)) {
				if (!_tsproduct.getLayeredPropValue( "XYScatterMethod", _subproduct, -1, false).equalsIgnoreCase(
				      ""+_regression_data.get(0).getAnalysisMethod()) 
				  || !_tsproduct.getLayeredPropValue ( "XYScatterMonth", _subproduct, -1, false).equalsIgnoreCase(
			          StringUtil.formatNumberSequence(_regression_data.get(0).getAnalysisMonths(),",")) 
			  	  || !_tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", _subproduct, -1, false ).equalsIgnoreCase(
			  	      ""+_regression_data.get(0).getNumberOfEquations()) 
				  || !_tsproduct.getLayeredPropValue ( "XYScatterTransformation", _subproduct, -1, false ).equalsIgnoreCase(
			          ""+_regression_data.get(0).getTransformation()) ) {
					need_to_analyze = true;
				}
			}
		}
		if ( need_to_analyze ) {
			// Redo the analysis...
			doAnalysis(getLeftYAxisGraphType());
			// This is the place where the reference graph has its data set.
		}
		__lastLeftYAxisGraphType = __leftYAxisGraphType;
	
		// Compute the labels for the data, which will set the _datalim_graph, which is used to set other data labels...
		// -mainly this picks nice bounds to data so that labels will have even numbers
	
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Computing labels..." );
		}
		if ( !_is_reference_graph ) {
			computeLabels ( _tslimits_lefty, _tslimits_righty );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, _gtype + "Set initial data limits to " + _data_lefty_limits );
			}
		}
	
		if ( _is_reference_graph ) {
			// Fill in the background to gray...
			_da_lefty_graph.setColor ( GRColor.gray );
			GRDrawingAreaUtil.fillRectangle ( _da_lefty_graph, _max_lefty_data_limits.getLeftX(),
				_max_lefty_data_limits.getBottomY(), _max_lefty_data_limits.getWidth(), _max_lefty_data_limits.getHeight() );
			// Highlight current data (zoom) limits in white...
			_da_lefty_graph.setColor ( GRColor.white );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, _gtype + "Data limits for reference box are " + _data_lefty_limits.toString() );
			}
			// Get the Y-dimension from the maximum values...
			GRDrawingAreaUtil.fillRectangle ( _da_lefty_graph, _data_lefty_limits.getLeftX(),
				_data_lefty_limits.getBottomY(), _data_lefty_limits.getWidth(), _max_lefty_data_limits.getHeight() );
			// Also draw a line in case we are zoomed in so far that the rectangle does not draw...
			GRDrawingAreaUtil.drawLine ( _da_lefty_graph, _data_lefty_limits.getLeftX(),
				_data_lefty_limits.getBottomY(), _data_lefty_limits.getLeftX(), _data_lefty_limits.getTopY() );
			// The time series will graph over the background in the following code.
		}
	
		// Now draw the graph(s)...
	
		// There are checks in each method to see whether a reference graph should do anything.  This allows
		// some experimentation with reference graphs that display some information.
		checkInternalProperties (); // Update dynamic data
		drawTitles (); // Draw main titles
		drawAnnotations(_tsproduct, _subproduct, _da_lefty_graph, _da_righty_graph, TSGraphDrawingStepType.BEFORE_BACK_AXES); // Draw annotations under the back axes
		drawAxesBack (); // Draw the axes that are below data
		drawAnnotations(_tsproduct, _subproduct, _da_lefty_graph, _da_righty_graph, TSGraphDrawingStepType.BEFORE_DATA); // Draw annotations that are below data
		drawTimeSeriesAnnotations(); // Draw annotations for specific time series (currently does nothing)
		// Draw the time series based on axes
		TSGraphType graphType = getLeftYAxisGraphType();
		if ( graphType == TSGraphType.DURATION ) {
			// All time series are drawn together
			drawGraph ( getLeftYAxisGraphType(), _da_lefty_graph, _tsproduct, _subproduct, getTSList(), _data_lefty_limits );
		}
		else {
			// Else draw left axis time series and then right axis time series
			drawGraph ( getLeftYAxisGraphType(), _da_lefty_graph, _tsproduct, _subproduct, getTSListForLeftYAxis(), _data_lefty_limits );
			drawGraph ( getRightYAxisGraphType(), _da_righty_graph, _tsproduct, _subproduct, getTSListForRightYAxis(), _data_righty_limits );
		}
		drawAnnotations(_tsproduct, _subproduct, _da_lefty_graph, _da_righty_graph, TSGraphDrawingStepType.AFTER_DATA); // Draw annotations that are on top of data
		drawAxesFront ( _tsproduct, _subproduct,
			_da_maintitle,
			__drawLeftyLabels, __leftYAxisGraphType,
			_da_lefty_graph, _da_lefty_title, _da_lefty_label,
			_data_lefty_limits, _datalim_lefty_title, _datalim_lefty_label,
			_ylabels_lefty, _lefty_precision,
			__drawRightyLabels, __rightYAxisGraphType,
			_da_righty_graph, _da_righty_title, _da_righty_label,
			_data_righty_limits, _datalim_righty_title, _datalim_righty_label,
			_ylabels_righty, _righty_precision,
			//__drawBottomxLabels, // TODO SAM 2016-10-23 Enable in the future
			_da_bottomx_title, _da_bottomx_label,
			_datalim_bottomx_title, _datalim_bottomx_label,
			_xlabels ); // Draw axes components that are in front of data
		drawCurrentDateTime (); // Draw the current date/time line (TODO SAM 2016-10-20 should this be attempted multiple times like annotations?)
		drawLegend (); // Draw the legend, which may be on top of the graph
		// The following is in the product properties ShowDrawingAreaOutline and is useful during development...
		if ( _showDrawingAreaOutline ) {
			drawDrawingAreas ();
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e ); // Put first because sometimes does not output if after
		Message.printWarning ( 1, routine, _gtype + "Error drawing graph." );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "...done painting TSGraph." );
	}
}

/**
Set whether the start_date and end_date were set via the setEndDate and 
setStartDate methods and should be used for calculating the date limits.
*/
public void setComputeWithSetDates(boolean b) {
	__useSetDates = b;
}

/**
This method is called from user interface code to trigger redraws, typically from zooming and scrolling.
Redraws of the canvas trigger a call to paint().
Reset the data limits prior to redrawing.  For example call when a zoom event
occurs and tsViewZoom() is called.  If a reference graph, the overall limits
will remain the same but the box for the zoom location will move to the
specified limits.  For typical time series plots, the x-axis limits are the
floating point year and the y-axis are data values.  An exception, for example is a scatter plot, where the
y-axis limits are the data limits from the first time series and the x-axis
limits are the data limits from the second time series.
@param datalim_lefty_graph Data limits for the left y-axis graph.
The data limits are either the initial values or the values from a zoom.
*/
public void setDataLimitsForDrawing ( GRLimits datalim_lefty_graph )
{	String routine = "setDataLimitsForDrawing";
    if ( datalim_lefty_graph == null ) {
		return;
	}
	// FIXME JTS exceptions thrown when trying to zoom
	if (_end_date == null && _start_date == null) {
		return;
	}
	// FIXME JTS
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine,
			_gtype + "Setting [" +_subproduct + "] _data_lefty_limits to " + datalim_lefty_graph.toString());
	}

	GRLimits dataLimitsInDrawingArea = null;
	if ( _is_reference_graph ) {
		// Save the new data limits for drawing but do not reset the
		// actual GRDrawingArea.  Also make sure the Y limits are the maximum...
		_data_lefty_limits = new GRLimits ( datalim_lefty_graph );
		_data_lefty_limits.setTopY ( _max_lefty_data_limits.getTopY() );
		_data_lefty_limits.setBottomY ( _max_lefty_data_limits.getBottomY() );
		dataLimitsInDrawingArea = new GRLimits(_data_lefty_limits);
		if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
		    // Reverse the data limits used for the reference graph - will be set in drawing area below
		    dataLimitsInDrawingArea.reverseY();
		}
	}
	else {
	    // Do the full recalculation of the data limits and zoom...
		// Need to recompute new start and end dates...
		// Make sure to keep the same date precision.
		
		// TODO SAM 2016-10-24 the x-axis limits are currently shared between left and right y-axis
		// -need to confirm that dates are determined for full time series list
		
		// Left y-axis

		_start_date = new DateTime ( datalim_lefty_graph.getLeftX(), true );
		_start_date.setPrecision ( _start_date.getPrecision() );
		_end_date = new DateTime ( datalim_lefty_graph.getRightX(), true );
		_end_date.setPrecision ( _start_date.getPrecision() );

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			_gtype + "Set _start_date to " + _start_date + " _end_date to " + _end_date );
		}
		// Left y-axis time series only
		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = false;
		try {
		    // Recompute the limits, based on the period and data values...
			List<TS> tslistToRender = getTSListToRender(true,includeLeftYAxis,includeRightYAxis);
			if ( tslistToRender.size() == 0) {
				_tslimits_lefty = null;
				return;
			}
			else {
				//_tslimits = TSUtil.getDataLimits( getEnabledTSList(), _start_date, _end_date, "", false, _ignore_units);
			    _tslimits_lefty = TSUtil.getDataLimits( tslistToRender, _start_date, _end_date, "", false, _ignoreLeftAxisUnits);
				if (__leftYAxisGraphType == TSGraphType.PERIOD){
					// Set the minimum value to 0 and the maximum value to one more than 
					// the number of time series.  Reverse the limits to number the same as the legend...
					_tslimits_lefty.setMaxValue(0.0);
					_tslimits_lefty.setMinValue( getEnabledTSList(includeLeftYAxis,includeRightYAxis).size() + 1);
				}
				else if (__leftYAxisGraphType == TSGraphType.RASTER) {
				    // Reset the y-axis values to the year - use Max because don't allow zoom
	                _tslimits_lefty.setMinValue(_max_tslimits_lefty.getDate1().getYear());
	                _tslimits_lefty.setMaxValue(_max_tslimits_lefty.getDate2().getYear() + 1);
				}
				if (!_zoom_keep_y_limits) {
					// Keep the y limits to the maximum...
					_tslimits_lefty.setMinValue ( _max_tslimits_lefty.getMinValue() );
					_tslimits_lefty.setMaxValue ( _max_tslimits_lefty.getMaxValue() );
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, _gtype + " Error getting dates for plot." );
			Message.printWarning ( 2, routine + "(" + _gtype + ")", e );
			return;
		}
		
		// Right y-axis limits

		includeLeftYAxis = false;
		includeRightYAxis = true;
		try {
		    // Recompute the limits, based on the period and data values...
			// Right y-axis time series only
			List<TS> tslistToRender = getTSListToRender(true,includeLeftYAxis,includeRightYAxis);
			if ( tslistToRender.size() == 0) {
				_tslimits_righty = null;
				return;
			}
			else {
				//_tslimits = TSUtil.getDataLimits( getEnabledTSList(), _start_date, _end_date, "", false, _ignore_units);
			    _tslimits_righty = TSUtil.getDataLimits( tslistToRender, _start_date, _end_date, "", false, _ignoreRightAxisUnits);
				if (__rightYAxisGraphType == TSGraphType.PERIOD){
					// Set the minimum value to 0 and the maximum value to one more than 
					// the number of time series.  Reverse the limits to number the same as the legend...
					_tslimits_righty.setMaxValue(0.0);
					_tslimits_righty.setMinValue( getEnabledTSList(includeLeftYAxis,includeRightYAxis).size() + 1);
				}
				else if (__rightYAxisGraphType == TSGraphType.RASTER) {
				    // Reset the y-axis values to the year - use Max because don't allow zoom
	                _tslimits_righty.setMinValue(_max_tslimits_righty.getDate1().getYear());
	                _tslimits_righty.setMaxValue(_max_tslimits_righty.getDate2().getYear() + 1);
				}
				if (!_zoom_keep_y_limits) {
					// Keep the y limits to the maximum...
					_tslimits_righty.setMinValue ( _max_tslimits_righty.getMinValue() );
					_tslimits_righty.setMaxValue ( _max_tslimits_righty.getMaxValue() );
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, _gtype + " Error getting dates for plot." );
			Message.printWarning ( 2, routine + "(" + _gtype + ")", e );
			return;
		}
		// Set the graph data limits based on the labels, for example to increase the buffer
		// beyond the data range.
		// This will set _datalim_graph.  The Y limits are computed from
		// the max data limits.  The X limits are computed from _start_date and _end_date...
		/* THIS IS THE ORIGINAL CODE BEFORE ADDING RIGHT Y-AXIS
		if ( getTSListToRender(true,includeLeftYAxis,includeRightYAxis).size() > 0) {
			// Only process the left y-axis
			computeLabels ( _tslimits_lefty, null );
		    dataLimitsInDrawingArea = new GRLimits(_data_lefty_limits);
		    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph
		        dataLimitsInDrawingArea.reverseY();
		    }
			_da_lefty_graph.setDataLimits ( dataLimitsInDrawingArea );
		}
		*/
		// Set the graph data limits based on the labels, for example to increase the buffer
		// beyond the data range.
		// This will set _datalim_lefty_graph and _datalim_righty_graph.  The Y limits are computed from
		// the max data limits.  The X limits are computed from _start_date and _end_date...
		includeLeftYAxis = true;
		includeRightYAxis = true;
		if ( getTSListToRender(true,includeLeftYAxis,includeRightYAxis).size() > 0) {
			// Have something to draw
			// Process both axes because right axis may be linked to left.
			computeLabels ( _tslimits_lefty, _tslimits_righty );
			// Reverse left y-axis if requested
		    dataLimitsInDrawingArea = new GRLimits(_data_lefty_limits);
		    if ( __leftyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph
		        dataLimitsInDrawingArea.reverseY();
		    }
			_da_lefty_graph.setDataLimits ( dataLimitsInDrawingArea );
			// Reverse right y-axis if requested
		    dataLimitsInDrawingArea = new GRLimits(_data_righty_limits);
		    if ( __rightyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph
		        dataLimitsInDrawingArea.reverseY();
		    }
			_da_righty_graph.setDataLimits ( dataLimitsInDrawingArea );
		}
		
		// TODO SAM 2016-10-24 need to enable
	}
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, _gtype + " After reset, [" +_subproduct + "] _datalim_lefty_graph are " + datalim_lefty_graph );
	}
}

/**
Set the derived time series list.
*/
private void setDerivedTSList ( List<TS> derivedTSList )
{
    __derivedTSList = derivedTSList;
}

/**
Set the drawing limits for all drawing areas based on properties and window
size.  The drawing limits are all set to within the limits of the device limits
that are passed in (initially the limits from the GRJComponentDevice when the
TSGraph was constructed and later the limits from the GRJComponentDevice as it
resizes).  Axes are set to log if the properties indicate to do so.
@param drawlim_page Drawing limits for the full extent of the graph within a GRJComponentDevice,
corresponding to canvas/component width and height in pixels (or part of a canvas/component).
*/
public void setDrawingLimits ( GRLimits drawlim_page )
{	double buffer = 2.0;	// Buffer around drawing areas (helps separate
				// things and also makes it easier to see
				// drawing areas when in debug mode
	String routine = "TSGraph.setDrawingLimits";
	
	// Declare local variables to make it easier to know what is being retrieved
	// May in the future minimize global references
	//TSGraphType leftYAxisGraphType = __leftYAxisGraphType;
	TSGraphType rightYAxisGraphType = __rightYAxisGraphType;
	TSProduct tsproduct = this._tsproduct;
	int subproduct = this._subproduct;
	List<TS> tslist = this.__tslist; // All time series (for example when used with legend)

	boolean log_y_lefty = false;
	boolean log_y_righty = false;
	boolean log_xy_scatter_lefty = false;
	String prop_value = tsproduct.getLayeredPropValue ( "LeftYAxisType", subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y_lefty = true;
	}
	prop_value = tsproduct.getLayeredPropValue ( "XYScatterTransformation", subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y_lefty = false;
		// TODO JTS - 2005-09-07 -- this isn't working right now when the axis types below are set
		//		log_xy_scatter = true;
	}
	prop_value = tsproduct.getLayeredPropValue ( "RightYAxisType", subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y_righty = true;
	}

	// Figure out dimensions up front based on font size...

	// Drawing areas will have zero size if nothing is drawn in them...

	double mainTitleHeight = 0.0;
	String mainTitleString = tsproduct.getLayeredPropValue ( "MainTitleString", subproduct, -1, false );
	if ( (mainTitleString != null) && !mainTitleString.equals("") ) {
		// Get the text extents and set the height based on that...
		String mainTitleFontName = tsproduct.getLayeredPropValue ( "MainTitleFontName", subproduct, -1, false );
		String mainTitleFontSize = tsproduct.getLayeredPropValue ( "MainTitleFontSize", subproduct, -1, false );
		String mainTitleFontStyle = tsproduct.getLayeredPropValue ( "MainTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_maintitle, mainTitleFontName, mainTitleFontStyle, StringUtil.atod(mainTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_maintitle, mainTitleString, GRUnits.DEVICE );
		mainTitleHeight = textLimits.getHeight();
	}

	double subTitleHeight = 0.0;
	String subTitleString = tsproduct.getLayeredPropValue ( "SubTitleString", subproduct, -1, false );
	if ( (subTitleString != null) && !subTitleString.equals("") ) {
		// Get the text extents and set the height based on that...
		String subTitleFontName = tsproduct.getLayeredPropValue ( "SubTitleFontName", subproduct, -1, false );
		String subTitleFontSize = tsproduct.getLayeredPropValue ( "SubTitleFontSize", subproduct, -1, false );
		String subTitleFontStyle = tsproduct.getLayeredPropValue ( "SubTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_subtitle, subTitleFontName, subTitleFontStyle, StringUtil.atod(subTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_subtitle, subTitleString, GRUnits.DEVICE );
		subTitleHeight = textLimits.getHeight();
	}

	// TODO SAM 2016-10-17 Need to more intelligently set label width by
	// checking labels for the maximum values (SAMX - get max *10 and
	// compute label width so we don't have to rely on full label determination?).
	// For now, hard-code the y-axis label widths.
	// This has been done for some time so it should be OK.  

	double leftYAxisLabelWidth = 80.0; // Height is set to graph height below
	double rightYAxisLabelWidth = 30.0; // Height is set to graph height below, 30 allows for date overflow
	if ( rightYAxisGraphType != TSGraphType.NONE ) {
		// Right y-axis is requested so set width similar to left
		rightYAxisLabelWidth = 80.0;
	}

	// Left y-axis title

	// Maximum height of y-axis left and right top titles, used for vertical calculations
	double yAxisTitleTopHeight = 0.0;
	double leftYAxisTitleHeight = 0.0;
	double leftYAxisTitleWidth = 0.0;
	String leftYAxisTitleString = tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", subproduct, -1, false );
	String leftYAxisTitlePosition = tsproduct.getLayeredPropValue ( "LeftYAxisTitlePosition", subproduct, -1, false );
	if ( (leftYAxisTitleString != null) && !leftYAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that...
		String leftYAxisTitleFontName = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", subproduct, -1, false );
		String leftYAxisTitleFontSize = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", subproduct, -1, false );
		String leftYAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_lefty_title, leftYAxisTitleFontName, leftYAxisTitleFontStyle, StringUtil.atod(leftYAxisTitleFontSize) );
		double rotation = 0.0;
		String leftYAxisTitleRotation = tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", subproduct, -1, false );
		if ( (leftYAxisTitleRotation != null) && !leftYAxisTitleRotation.isEmpty() ) {
			try {
				rotation = Double.parseDouble(leftYAxisTitleRotation);
			}
			catch ( NumberFormatException e ) {
				// Just use 0
			}
		}
		// Get the limits without rotation
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_lefty_title, leftYAxisTitleString, GRUnits.DEVICE );
		leftYAxisTitleHeight = textLimits.getHeight();
		leftYAxisTitleWidth = textLimits.getWidth();
		// If the rotation is 90 or 270 swap the width and height because text is vertical
		// TODO SAM 2016-10-17 Later support angles other than perpendicular
		if ( rotation > 0.0 ) {
			if ( ((int)(rotation + .01) == 90) || ((int)(rotation + .01) == 270) ) {
				leftYAxisTitleHeight = textLimits.getWidth();
				leftYAxisTitleWidth = textLimits.getHeight();
			}
		}
		if ( rotation < 0.0 ) {
			if ( ((int)(rotation - .01) == -90) || ((int)(rotation - .01) == -270) ) {
				leftYAxisTitleHeight = textLimits.getWidth();
				leftYAxisTitleWidth = textLimits.getHeight();
			}
		}
		if ( leftYAxisTitlePosition.equalsIgnoreCase("AboveAxis") ) {
			yAxisTitleTopHeight = leftYAxisTitleHeight;
		}
	}
	
	// Right y-axis title

	double rightYAxisTitleWidth = 0.0;
	double rightYAxisTitleHeight = 0.0;
	
	String rightYAxisTitleString = tsproduct.getLayeredPropValue ( "RightYAxisTitleString", subproduct, -1, false );
	String rightYAxisTitlePosition = tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", subproduct, -1, false );
	if ( (rightYAxisTitleString != null) && !rightYAxisTitleString.equals("") &&
		(rightYAxisTitlePosition != null) && !rightYAxisTitlePosition.equalsIgnoreCase("None") ) {
		// Get the text extents and set the height based on that...
		String rightYAxisTitleFontName = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", subproduct, -1, false );
		String rightYAxisTitleFontSize = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", subproduct, -1, false );
		String rightYAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_righty_title, rightYAxisTitleFontName, rightYAxisTitleFontStyle, StringUtil.atod(rightYAxisTitleFontSize) );
		double rotation = 0.0;
		String rightYAxisTitleRotation = tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", subproduct, -1, false );
		if ( (rightYAxisTitleRotation != null) && !rightYAxisTitleRotation.isEmpty() ) {
			try {
				rotation = Double.parseDouble(rightYAxisTitleRotation);
			}
			catch ( NumberFormatException e ) {
				// Just use 0
			}
		}
		// Get the limits without rotation
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_righty_title, rightYAxisTitleString, GRUnits.DEVICE );
		rightYAxisTitleHeight = textLimits.getHeight();
		rightYAxisTitleWidth = textLimits.getWidth();
		// If the rotation is 90 or 270 swap the width and height because text is vertical
		// TODO SAM 2016-10-17 Later support angles other than perpendicular
		if ( rotation > 0.0 ) {
			if ( ((int)(rotation + .01) == 90) || ((int)(rotation + .01) == 270) ) {
				rightYAxisTitleHeight = textLimits.getWidth();
				rightYAxisTitleWidth = textLimits.getHeight();
			}
		}
		if ( rotation < 0.0 ) {
			if ( ((int)(rotation - .01) == -90) || ((int)(rotation - .01) == -270) ) {
				rightYAxisTitleHeight = textLimits.getWidth();
				rightYAxisTitleWidth = textLimits.getHeight();
			}
		}
		if ( rightYAxisTitlePosition.equalsIgnoreCase("AboveAxis") ) {
			// Use the largest vertical height of the left and right top y-axis to allow calculations below
			if ( rightYAxisTitleHeight > yAxisTitleTopHeight ) {
				yAxisTitleTopHeight = rightYAxisTitleHeight;
			}
		}
	}

	// X axis titles

	double topXAxisTitleHeight = 0.0;
	String topXAxisTitleString = tsproduct.getLayeredPropValue ( "TopXAxisTitleString", subproduct, -1, false );
	if ( (topXAxisTitleString != null) && !topXAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that...
		String topXAxisTitleFontName = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontName", subproduct, -1, false );
		String topXAxisTitleFontSize = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontSize", subproduct, -1, false );
		String topXAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_topx_title, topXAxisTitleFontName, topXAxisTitleFontStyle, StringUtil.atod(topXAxisTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_topx_title, topXAxisTitleString, GRUnits.DEVICE );
		topXAxisTitleHeight = textLimits.getHeight();
	}

	double bottomXAxisTitleHeight = 0.0;
	String bottomXAxisTitleString = tsproduct.getLayeredPropValue ( "BottomXAxisTitleString", subproduct, -1, false );
	if ( (bottomXAxisTitleString != null) && !bottomXAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that...
		String bottomXAxisTitleFontName = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", subproduct, -1, false );
		String bottomXAxisTitleFontSize = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontSize", subproduct, -1, false );
		String bottomXAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_bottomx_title, bottomXAxisTitleFontName, bottomXAxisTitleFontStyle,
			StringUtil.atod(bottomXAxisTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_title, bottomXAxisTitleString, GRUnits.DEVICE );
		bottomXAxisTitleHeight = textLimits.getHeight();
	}

	double bottomXAxisLabelHeight = 0.0;
	String bottomXAxisLabelFontName = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", subproduct, -1, false );
	String bottomXAxisLabelFontSize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", subproduct, -1, false );
	String bottomXAxisLabelFontStyle = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( _da_bottomx_label, bottomXAxisLabelFontName, bottomXAxisLabelFontStyle, StringUtil.atod(bottomXAxisLabelFontSize) );
	GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( _da_bottomx_label, "A string", GRUnits.DEVICE );
	if ( (__leftYAxisGraphType == TSGraphType.DURATION) || (__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
		bottomXAxisLabelHeight = textLimits.getHeight();
	}
	else {
	    // For X labels - leave room for two rows of labels for dates.
		bottomXAxisLabelHeight = 2*textLimits.getHeight();
	}

	// Make an initial determination of the legend height and width, based
	// on the font height and string width.  The dynamic nature of the plot
	// size really should only impact how many legend items are shown.  Try
	// to limit to a reasonable number.  This logic works for any legend position.

	String legendPosition = tsproduct.getLayeredPropValue ( "LegendPosition", subproduct, -1, false );

	double legendHeight = 0.0;
	double legendWidth = 0.0;
	// The following gets the left and right y-axis time series, assuming they will all be in one legend
	if ((tslist == null) || (tslist.size() == 0) || (getEnabledTSList(true,true).size() == 0) || legendPosition.equalsIgnoreCase("None")) {
		// Default to no legend...
		legendHeight = 0.0;
		legendWidth = 0.0;
	}
	else {	
		// The legend height is based on the legend font size = size*(nts + 1), with the buffer, where nts is the
		// number of enabled, non-null time series. The legend properties are for the subproduct.
		String legendFontName = tsproduct.getLayeredPropValue ( "LegendFontName", subproduct, -1, false );
		String legendFontSize = tsproduct.getLayeredPropValue ( "LegendFontSize", subproduct, -1, false );
		String legendFontStyle = tsproduct.getLayeredPropValue ( "LegendFontStyle", subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( _da_bottom_legend, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
		int nts = 0;
		int size = tslist.size();
		TS ts = null;
		String legend = null;

        for ( int i = 0; i < size; i++ ) {
        	ts = tslist.get(i);
        	if (ts == null || !isTSEnabled(i)) {
        		continue;
        	}
        	if ( ts.getEnabled() ) {
        		if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        			if (i == 0) {
        				// ignore the zeroth
        				continue;
        			}
        			// The time series will be plotted and will be shown in the legend...
        			legend = getLegendString(ts, i) + " (Residual)";
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( _da_bottom_legend, legend, GRUnits.DEVICE);
        				legendWidth = MathUtil.max( legendWidth, textLimits.getWidth());
        			}
        			++nts;			
        		}
        		else if (__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
        			// The time series will be plotted and will be shown in the legend...
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents(_da_bottom_legend, legend, GRUnits.DEVICE);
        				legendWidth = MathUtil.max(legendWidth, textLimits.getWidth());
        			}
        			++nts;
        			if (i > 0) {
        				// add the predicted time series, too
        				legend = getLegendString(ts, i) + " (Predicted)";
        				if (legend != null) {
        					textLimits = GRDrawingAreaUtil.getTextExtents( _da_bottom_legend, legend, GRUnits.DEVICE);
        					legendWidth = MathUtil.max( legendWidth, textLimits.getWidth());
        				}
        				++nts;				
        			}
        		}
        		else {
        			// The time series will be plotted and will be shown in the legend...
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( _da_bottom_legend, legend, GRUnits.DEVICE);
        				legendWidth = MathUtil.max( legendWidth, textLimits.getWidth());
        			}
        			++nts;
        		}
        	}
        }

		ts = null;
		// Estimate the overall height...
		textLimits = GRDrawingAreaUtil.getTextExtents ( _da_bottom_legend, "TEST STRING", GRUnits.DEVICE );
		legendHeight = nts*textLimits.getHeight();
		// The legend width is increased by the width of the symbol (currently always 25 pixels)...
		legendWidth += 25;
		textLimits = null;
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

	if ( legendPosition.equalsIgnoreCase("Bottom") || legendPosition.equalsIgnoreCase("Top") ) {
		if ( legendHeight > _drawlim_page.getHeight()*.5 ) {
			legendHeight = _drawlim_page.getHeight()*.5;
		}
	}

	// Compute specific legend height and width values for each legend position...

	double bottom_legend_height = 0.0;
	double left_legend_width = 0.0;
	double left_legend_buffer = 0.0;
	double right_legend_width = 0.0;
	double right_legend_buffer = 0.0;
	double inside_legend_height = 0.0;
	double inside_legend_width = 0.0;
	double inside_legend_buffer = 0.0;
	
	if ( legendPosition.equalsIgnoreCase("Bottom") ) {
		bottom_legend_height = legendHeight;
	}
	else if ( legendPosition.equalsIgnoreCase("Left") ) {
		left_legend_width = legendWidth;
		left_legend_buffer = buffer;
	}
	else if ( legendPosition.equalsIgnoreCase("Right") ) {
		right_legend_width = legendWidth;
		right_legend_buffer = buffer;
	}
	else if ( legendPosition.equalsIgnoreCase("Top") ) {
	}
	else if (StringUtil.startsWithIgnoreCase(legendPosition, "Inside")) {
		inside_legend_height = legendHeight;
		inside_legend_width = legendWidth;
		inside_legend_buffer = buffer * 4;
	}
	
	// Set the drawing limits based on what was determined above...

	// Graph main title is only impacted by overall (page) limits...

	if ( mainTitleHeight == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_maintitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),	drawlim_page.getTopY(),
			(drawlim_page.getRightX() - buffer), drawlim_page.getTopY() );
	}
	else {
	    _drawlim_maintitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),
			(drawlim_page.getTopY() - buffer - mainTitleHeight),
			(drawlim_page.getRightX() - buffer), (drawlim_page.getTopY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Main title drawing limits are: " + _drawlim_maintitle );
	}

	// Graph subtitle is only impacted by page limits and main title...

	if ( subTitleHeight == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_subtitle = new GRLimits ( (drawlim_page.getLeftX() + buffer), _drawlim_maintitle.getBottomY(),
			(drawlim_page.getRightX() - buffer), _drawlim_maintitle.getBottomY() );
	}
	else {
	    _drawlim_subtitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),
			(_drawlim_maintitle.getBottomY() - buffer -	subTitleHeight),
			(drawlim_page.getRightX() - buffer), (_drawlim_maintitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Sub title drawing limits are: " + _drawlim_subtitle.toString() );
	}

	// Top legend is impacted by page limits and position of subtitle...

	// Currently top legend is not enabled...

	// Top X axis title is impacted by the left and right legends and the
	// position of the subtitle.
	
	// y-axis label left edge is impacted by legend position
	// (for now include left y-axis title inside the label area if the title is on the left)
	// - currently the left y-axis title is within the label drawing area
	double leftYAxisLabelLeft = _drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer;

	if ( topXAxisTitleHeight == 0.0 ) {
		// Zero height drawing area place holder at same height as the bottom of the subtitle...
		_drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer),
			_drawlim_subtitle.getBottomY(),
			(drawlim_page.getRightX() - buffer - right_legend_width - right_legend_buffer - rightYAxisLabelWidth - buffer),
			_drawlim_subtitle.getBottomY() );
	}
	else {
	    _drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer),
			_drawlim_subtitle.getBottomY() - buffer - topXAxisTitleHeight,
			(drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth - buffer),
			_drawlim_subtitle.getBottomY() - buffer );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Top X title drawing limits are: " + _drawlim_topx_title.toString() );
	}

	// Top x labels are impacted by left and right legends and the position of the top x title.
	// Top x labels are not currently processed (until we can move the Y axis titles out of the way)...
	
	// Left y labels are always present.  Even if zero width, use buffer because other code does below...

	// Calculate the position of the bottom of the y-axis labels (essentially the bottom of the graph)
	// This is used by a number of calculations below.
	double yAxisLabelBottom = drawlim_page.getBottomY();
	if ( bottom_legend_height > 0.0 ) {
		yAxisLabelBottom += (buffer + bottom_legend_height);
	}
	if ( bottomXAxisTitleHeight > 0.0 ) {
		yAxisLabelBottom += (buffer + bottomXAxisTitleHeight);
	}
	else {
		yAxisLabelBottom += (2.0 * buffer);	// Bottomx title is always at least buffer in actuality.
	}
	if ( bottomXAxisLabelHeight > 0.0 ) {
		yAxisLabelBottom += (buffer + bottomXAxisLabelHeight);
	}
	yAxisLabelBottom += buffer;

	// Calculate the position of the top of the y-axis labels (essentially the top of the graph)
	// This is used by a number of calculations below.
	// y-axis label top is consistent for left and right y-axis and is impacted by y-axis title position and top titles
	double yAxisLabelTop = drawlim_page.getTopY() - buffer - mainTitleHeight - buffer - subTitleHeight;
	// TODO SAM 2016-10-24 No top x-axis labels are supported yet but will need to adjust if enabled
	if ( yAxisTitleTopHeight > 0.0 ) {
		// Left and/or right y-axis label is above the axis so add some vertical space
		yAxisLabelTop = yAxisLabelTop - buffer - yAxisTitleTopHeight - buffer;
	}
	
	// Left y-axis title

	if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
		// Draw the left y-axis title in the same space as the labels so as to avoid extra calculations
		// Can adjust this later if problematic
		double leftYAxisTitleLeft = leftYAxisLabelLeft;
	    _drawlim_lefty_title = new GRLimits ( leftYAxisTitleLeft, yAxisLabelBottom,
			(leftYAxisTitleLeft + leftYAxisTitleWidth),	yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis" to match legacy behavior
		double leftEdge = (drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer -
		        leftYAxisTitleWidth/2.0);
		// Center above left edge of graph
		_drawlim_lefty_title = new GRLimits (
			leftEdge, (yAxisLabelTop + buffer),
			(leftEdge + leftYAxisTitleWidth), (yAxisLabelTop + buffer + leftYAxisTitleHeight) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Left y title drawing limits are: " + _drawlim_lefty_title.toString() );
	}

	// Left y labels are impacted by left and legends and position of the y axis titles
	// - Currently left y-axis title if LeftOfAxis is within the label drawing area so no size impact
	if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
		// No left y-axis title vertically so compute from top down
		_drawlim_lefty_label = new GRLimits ( leftYAxisLabelLeft, yAxisLabelBottom,
			(leftYAxisLabelLeft + leftYAxisLabelWidth), yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis"
		// - currently same as above, may adjust logic later
		_drawlim_lefty_label = new GRLimits ( leftYAxisLabelLeft, yAxisLabelBottom,
				(leftYAxisLabelLeft + leftYAxisLabelWidth), yAxisLabelTop );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Left Y label drawing limits are: " + _drawlim_lefty_label.toString() );
	}

	// Right y axis labels (do before titles since for now the right y-axis title can sit inside the label drawing area)

	double rightYLabelLeftEdge = _drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth;
	_drawlim_righty_label = new GRLimits (
		rightYLabelLeftEdge, yAxisLabelBottom,
		(rightYLabelLeftEdge + rightYAxisLabelWidth), yAxisLabelTop );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Right y-axis label drawing limits are: " + _drawlim_righty_label.toString() );
	}
	
	// Right y-axis title (do after labels since for now title sits inside of label drawing area)

	if ( rightYAxisTitlePosition.equalsIgnoreCase("RightOfAxis") ) {
		// Draw the right y-axis title on the right inside edge of the labels so as to avoid extra calculations
		// Can adjust this later if problematic
		double rightEdge = _drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer;
	    _drawlim_righty_title = new GRLimits ( (rightEdge - rightYAxisTitleWidth), yAxisLabelBottom,
			rightEdge, yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis"
		double leftEdge = (drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth - buffer -
		        rightYAxisTitleWidth/2.0);
		// Center above right edge of graph
		_drawlim_righty_title = new GRLimits (
			leftEdge, (yAxisLabelTop + buffer),
			(leftEdge + rightYAxisTitleWidth), (yAxisLabelTop + buffer + rightYAxisTitleHeight) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Right y-axis title drawing limits are: " + _drawlim_righty_title.toString() );
	}

	// ...Skip the graph area for now because it will be the remainder...
	// ...see below for its definition...

	// Now work up from the bottom...
	// Drawing limits for the bottom legend (always independent of other legends)...

	if ( bottom_legend_height == 0.0 ) {
		// Make zero-height same Y as the bottom of the page area...
		_drawlim_bottom_legend = new GRLimits ( (_drawlim_page.getLeftX() + buffer), _drawlim_page.getBottomY(),
			(_drawlim_page.getRightX() - buffer), _drawlim_page.getBottomY());
	}
	else {
	    _drawlim_bottom_legend = new GRLimits ( (_drawlim_page.getLeftX() + buffer),
	        (_drawlim_page.getBottomY() + buffer), (_drawlim_page.getRightX() - buffer),
			(_drawlim_page.getBottomY() + buffer + bottom_legend_height) );
	}
	// Set the data limits for the legend to use device units...
	_datalim_bottom_legend = new GRLimits ( 0.0, 0.0, _drawlim_bottom_legend.getWidth(), _drawlim_bottom_legend.getHeight() );

	// The position of the bottom X axis title and labels is impacted by
	// left and right legends and the position of the bottom legend.
	// Bottom X axis title and labels - work up from the bottom legend...
	// For the bottomx title, add a little space around so it looks better, even if no title is given.

	if ( bottomXAxisTitleHeight == 0.0 ) {
		// Make zero-height same Y as the top of the legend area...
		_drawlim_bottomx_title = new GRLimits (
			(_drawlim_lefty_label.getRightX() + buffer), (_drawlim_bottom_legend.getTopY() + buffer),
			(_drawlim_righty_label.getLeftX() - buffer), (_drawlim_bottom_legend.getTopY() + buffer + buffer) );
	}
	else {
	    _drawlim_bottomx_title = new GRLimits (	(_drawlim_lefty_label.getRightX() + buffer),
	        (_drawlim_bottom_legend.getTopY() + buffer), (_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_bottom_legend.getTopY() + buffer + bottomXAxisTitleHeight + buffer));
	}

	if ( bottomXAxisLabelHeight == 0.0 ) {
		// Make zero-height same Y as the top of the X title...
		_drawlim_bottomx_label = new GRLimits (	(_drawlim_lefty_label.getRightX() + buffer),
			_drawlim_bottomx_title.getTopY(), (_drawlim_righty_label.getLeftX() - buffer),
			_drawlim_bottomx_title.getTopY());
	}
	else {
	    _drawlim_bottomx_label = new GRLimits ( (_drawlim_lefty_label.getRightX() + buffer),
			(_drawlim_bottomx_title.getTopY() + buffer), (_drawlim_righty_label.getLeftX() - buffer),
			(_drawlim_bottomx_title.getTopY() + buffer + bottomXAxisLabelHeight));
	}

	// Graph drawing area (always what is left)...

	if ( _is_reference_graph ) {
		_drawlim_lefty_graph = new GRLimits (	drawlim_page.getLeftX(), drawlim_page.getBottomY(),
			drawlim_page.getRightX(), drawlim_page.getTopY() );
	}
	else {
		// Left y-axis graph
		if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
			_drawlim_lefty_graph = new GRLimits ( (_drawlim_lefty_label.getRightX() + buffer),
				(_drawlim_bottomx_label.getTopY() + buffer), (_drawlim_righty_label.getLeftX() - buffer),
				_drawlim_lefty_title.getTopY() );
		}
		else {
			// "AboveAxis" (legacy)
			_drawlim_lefty_graph = new GRLimits ( (_drawlim_lefty_label.getRightX() + buffer),
				(_drawlim_bottomx_label.getTopY() + buffer), (_drawlim_righty_label.getLeftX() - buffer),
				(_drawlim_lefty_title.getBottomY() - buffer) );
		}
		// Right y-axis graph, same drawing limits as left y-axis graph (but data limits will be different)
		_drawlim_righty_graph = new GRLimits ( _drawlim_lefty_graph );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Graph drawing limits are: " + _drawlim_lefty_graph );
	}

	// If the legend is on the left or right, define the drawing area now
	// because it is impacted by the graph drawing area (typically the
	// left and right legends will draw down the side until the bottom of
	// the graph (this could be a problem if the graph ends - fix by not
	// drawing when negative y-coordinates are found for data).

	if ( legendPosition.equalsIgnoreCase("Left") ) {
		_drawlim_left_legend = new GRLimits ( (_drawlim_page.getLeftX() + left_legend_buffer),
		    _drawlim_lefty_graph.getBottomY(), (_drawlim_page.getLeftX() + left_legend_buffer + left_legend_width),
			_drawlim_lefty_graph.getTopY() );
	}
	else {	// Set to zero width...
		_drawlim_left_legend = new GRLimits ( _drawlim_page.getLeftX(), _drawlim_lefty_graph.getBottomY(),
			_drawlim_page.getLeftX(), _drawlim_lefty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units...
	_datalim_left_legend = new GRLimits ( 0.0, 0.0,	_drawlim_left_legend.getWidth(),
		_drawlim_left_legend.getHeight() );
	if ( legendPosition.equalsIgnoreCase("Right") ) {
		_drawlim_right_legend = new GRLimits ( (_drawlim_page.getRightX() - right_legend_buffer - right_legend_width),
			_drawlim_lefty_graph.getBottomY(), (_drawlim_page.getRightX() - right_legend_buffer),	_drawlim_lefty_graph.getTopY() );
	}
	else {	// Set to zero width...
		_drawlim_right_legend = new GRLimits ( _drawlim_page.getRightX(), _drawlim_lefty_graph.getBottomY(),
			_drawlim_page.getRightX(), _drawlim_lefty_graph.getTopY() );
	}

	if (legendPosition.equalsIgnoreCase("InsideUpperLeft")) {
		_drawlim_inside_legend = new GRLimits( _drawlim_lefty_graph.getLeftX() + inside_legend_buffer,
			_drawlim_lefty_graph.getTopY() - inside_legend_buffer	- inside_legend_height,
			_drawlim_lefty_graph.getLeftX() + inside_legend_buffer + inside_legend_width,
			_drawlim_lefty_graph.getTopY() - inside_legend_buffer);
	}
	else if (legendPosition.equalsIgnoreCase("InsideUpperRight")) {
		_drawlim_inside_legend = new GRLimits( _drawlim_lefty_graph.getRightX() - inside_legend_buffer - inside_legend_width,
			_drawlim_lefty_graph.getTopY() - inside_legend_buffer - inside_legend_height,
			_drawlim_lefty_graph.getRightX() - inside_legend_buffer, _drawlim_lefty_graph.getTopY() - inside_legend_buffer);
	}
	else if (legendPosition.equalsIgnoreCase("InsideLowerLeft")) {
		_drawlim_inside_legend = new GRLimits( _drawlim_lefty_graph.getLeftX() + inside_legend_buffer,
			_drawlim_lefty_graph.getBottomY() + inside_legend_buffer,
			_drawlim_lefty_graph.getLeftX() + inside_legend_buffer + inside_legend_width,
			_drawlim_lefty_graph.getBottomY() + inside_legend_buffer + inside_legend_height);
	}
	else if (legendPosition.equalsIgnoreCase("InsideLowerRight")) {
		_drawlim_inside_legend = new GRLimits( _drawlim_lefty_graph.getRightX() - inside_legend_buffer - inside_legend_width,
			_drawlim_lefty_graph.getBottomY() + inside_legend_buffer,
			_drawlim_lefty_graph.getRightX() - inside_legend_buffer,
			_drawlim_lefty_graph.getBottomY() + inside_legend_buffer + inside_legend_height);
	}
	else {
		_drawlim_inside_legend = new GRLimits( _drawlim_page.getLeftX(), _drawlim_page.getBottomY(),
			_drawlim_page.getRightX(), _drawlim_page.getTopY());
	}
	_datalim_inside_legend = new GRLimits(0.0, 0.0, _drawlim_inside_legend.getWidth(),
		_drawlim_inside_legend.getHeight());
	
	// Set the data limits for the legend to use device units...
	_datalim_right_legend = new GRLimits ( 0.0, 0.0, _drawlim_right_legend.getWidth(),
		_drawlim_right_legend.getHeight() );

	// Now set in the drawing areas...

	if ( (_da_page != null) && (_drawlim_page != null) ) {
		// _drawlim_page is set in the constructor - we just need to use it as is...
		_da_page.setDrawingLimits ( _drawlim_page, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_maintitle != null) && (_drawlim_maintitle != null) ) {
		_da_maintitle.setDrawingLimits ( _drawlim_maintitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_subtitle != null) && (_drawlim_subtitle != null) ) {
		_da_subtitle.setDrawingLimits ( _drawlim_subtitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_topx_title != null) && (_drawlim_topx_title != null) ) {
		_da_topx_title.setDrawingLimits ( _drawlim_topx_title, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_topx_title.setDataLimits ( _datalim_topx_title );
	}
	if ( (_da_topx_label != null) && (_drawlim_topx_label != null) ) {
		_da_topx_label.setDrawingLimits ( _drawlim_topx_label, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_topx_label.setDataLimits ( _datalim_topx_label );
		if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(_da_topx_label, GRAxis.LOG, GRAxis.LINEAR);
		}				
	}
	if ( (_da_lefty_title != null) && (_drawlim_lefty_title != null) ) {
		_da_lefty_title.setDrawingLimits ( _drawlim_lefty_title, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_lefty_title.setDataLimits ( _datalim_lefty_title );
	}
	if ( (_da_lefty_label != null) && (_drawlim_lefty_label != null) ) {
		_da_lefty_label.setDrawingLimits ( _drawlim_lefty_label, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_lefty_label.setDataLimits ( _datalim_lefty_label );
		if (log_y_lefty || log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(_da_lefty_label, GRAxis.LINEAR, GRAxis.LOG);
		}
	}
	if ( (_da_righty_title != null) && (_drawlim_righty_title != null) ) {
		_da_righty_title.setDrawingLimits ( _drawlim_righty_title, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_righty_title.setDataLimits ( _datalim_righty_title );
	}
	if ( (_da_righty_label != null) && (_drawlim_righty_label != null) ) {
		_da_righty_label.setDrawingLimits (	_drawlim_righty_label, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_righty_label.setDataLimits ( _datalim_righty_label );
		if (log_y_lefty || log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(_da_righty_label,	GRAxis.LINEAR, GRAxis.LOG);
		}
	}
	if ( (_da_lefty_graph != null) && (_drawlim_lefty_graph != null) ) {
		_da_lefty_graph.setDrawingLimits ( _drawlim_lefty_graph, GRUnits.DEVICE, GRLimits.DEVICE );
		if (log_y_lefty) {
			GRDrawingAreaUtil.setAxes(_da_lefty_graph, GRAxis.LINEAR, GRAxis.LOG);
		}
		else if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(_da_lefty_graph, GRAxis.LOG, GRAxis.LOG);
		}
	}
	if ( (_da_righty_graph != null) && (_drawlim_righty_graph != null) ) {
		_da_righty_graph.setDrawingLimits ( _drawlim_righty_graph, GRUnits.DEVICE, GRLimits.DEVICE );
		if (log_y_righty) {
			GRDrawingAreaUtil.setAxes(_da_righty_graph, GRAxis.LINEAR, GRAxis.LOG);
		}
	}
	if ( (_da_bottomx_label != null) && (_drawlim_bottomx_label != null) ) {
		_da_bottomx_label.setDrawingLimits ( _drawlim_bottomx_label, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_bottomx_label.setDataLimits ( _datalim_bottomx_label );
		if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(_da_bottomx_label, GRAxis.LOG, GRAxis.LINEAR);
		}		
	}
	if ( (_da_bottomx_title != null) && (_drawlim_bottomx_title != null) ) {
		_da_bottomx_title.setDrawingLimits ( _drawlim_bottomx_title, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_bottomx_title.setDataLimits ( _datalim_bottomx_title );
	}
	if ( (_da_bottom_legend != null) && (_drawlim_bottom_legend != null) ) {
		_da_bottom_legend.setDrawingLimits ( _drawlim_bottom_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_bottom_legend.setDataLimits ( _datalim_bottom_legend );
	}
	if ( (_da_left_legend != null) && (_drawlim_left_legend != null) ) {
		_da_left_legend.setDrawingLimits ( _drawlim_left_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_left_legend.setDataLimits ( _datalim_left_legend );
	}
	if ( (_da_right_legend != null) && (_drawlim_right_legend != null) ) {
		_da_right_legend.setDrawingLimits ( _drawlim_right_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		_da_right_legend.setDataLimits ( _datalim_right_legend );
	}
	if ((_da_inside_legend != null) && (_drawlim_inside_legend != null)) {
		_da_inside_legend.setDrawingLimits(_drawlim_inside_legend, GRUnits.DEVICE, GRLimits.DEVICE);
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
 * Sets whether drawing area outlines should be drawn, useful for development.
 */
protected void setShowDrawingAreaOutline ( boolean showDrawingAreaOutline ) {
	_showDrawingAreaOutline = showDrawingAreaOutline;
}

/**
Sets the start date.
@param startDate value to set the start date to.
*/
protected void setStartDate(DateTime startDate) {
	_start_date = startDate;
}

// TODO SAM 2006-10-01 Move this to TSUtil or similar for general use.
/**
Deterine the nearest DateTime equal to or before the candidate DateTime,
typically in order to determine an iterator start DateTime that is compatible
with the specific time series.  For example, when graphing time series with
data having different intervals, the graphing software will determine the
visible window using a DateTime range with a precision that matches the smallest
data interval.  Time series that use dates that are less precise may be able to
simply reset the more precise DateTime to a lower precision.  However, it may
also be necessary for calling code to decrement the returned DateTime by an
interval to make sure that a complete overlapping period is considered during iteration.

Time series that use higher precision DateTimes also need to check the candidate
DateTime for offsets.  For example, the candidate DateTime may have a precision
of DateTime.PRECISION_HOUR and an hour of 6.  The time series being checked may
be a 6-hour time series but observatins are recorded at 0300, 0900, 1500, 2100.
In this case, the first DateTime in the time series before the candidate must be
chosen, regardless of whether the candidate's hour aligns with the time series data.
@param candidate_DateTime The candidate DateTime, which may have a precision
greater than or less than those used by the indicated time series.
@param ts Time series to examine, which may be a regular or irregular time series.
@return DateTime matching the precision of DateTimes used in the specified time
series that is equal to or less than the candidate DateTime.  The returned
DateTime will align with the time series data and may NOT align evenly with the candidate DateTime.
*/
public static DateTime getNearestDateTimeLessThanOrEqualTo ( DateTime candidate_DateTime, TS ts )
{
	DateTime returnDate = new DateTime(candidate_DateTime);
	returnDate.round(-1, ts.getDataIntervalBase(), ts.getDataIntervalMult());
	
	// Compute the offset from exact interval breaks for this time series...
	DateTime start_rounded = new DateTime(ts.getDate1());
	start_rounded.round(-1, ts.getDataIntervalBase(), ts.getDataIntervalMult());
	DateTime start_not_rounded = new DateTime(ts.getDate1());
	DateTime diff = null;
	try {
		diff = new DateTime(TimeUtil.diff(start_not_rounded, start_rounded));
	}
	catch ( Exception e ) {
		// TODO SAM 2006-09-28 Need to handle?
	}

	// add the offset to the rounded base time if there is one
	if(!(start_not_rounded.equals(start_rounded))) {
		returnDate.add(diff);
	}

	// set precision for the time series
	returnDate.setPrecision(ts.getDate1().getPrecision());	
	
	return returnDate;
}

}