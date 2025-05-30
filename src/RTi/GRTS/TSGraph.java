// TSGraph - class to manage drawing areas for one time series graph

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.GRTS;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import RTi.GR.GRAspectType;
import RTi.GR.GRAxis;
import RTi.GR.GRAxisDimensionType;
import RTi.GR.GRAxisDirectionType;
import RTi.GR.GRAxisEdgeType;
import RTi.GR.GRAxisScaleType;
import RTi.GR.GRColor;
import RTi.GR.GRColorRampType;
import RTi.GR.GRCoordinateType;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRLineConnectType;
import RTi.GR.GRPoint;
import RTi.GR.GRSymbolPosition;
import RTi.GR.GRSymbolShapeType;
import RTi.GR.GRSymbolTable;
import RTi.GR.GRSymbolTableRow;
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
The TSGraph class manages the drawing areas for displaying one or more time series in a single graph.
The drawing areas are set up by specifying a
GRJComponentDevice and information about how much of the device should be used for this graph.
Drawing properties are retrieved from a TSProduct,
where this graph is identified as a sub-product of the entire product.
This class also implements TSViewListener,
which is typically used to allow a reference other TSGraph so that zooming can occur similarly for all graphs.
The layout of the graph is as follows (see TSGraphJComponent for other layout features like the main title).
Because the AWT Canvas and Graphics do not allow for vertical text,
the Y axis label is currently added at the top of the axis.
This may change in the future.
Currently only one legend can be present and the position can be either the bottom (default, left, right, or top).
The following figure shows the placement of all the legends, although only one will be used.
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
| |                            Legend (_da_lefty_top_legend and/or _da_righty_top_legend)                            | |
| -------------------------------------------------------------------------------------------------------------------- |
| ---                                     -------------------------------                                          --- |
| | / _da_lefty_left_legend               |Top x axis title(not enabled)|                   _da_lefty_right_legend \ | |
| |/|  and/or                             | (_da_topx_title)            |                  and/or                  |\| |
| | | _da_righty_left_legend              -------------------------------                  _da_righty_right_legend | | |
| | |                                     -------------------------------                                          | | |
| | |                                     | Top x labels (not enabled)  |                                          | | |
| | |                                     | (_da_topx_label)            |                                          | | |
| | |                                     -------------------------------                                          | | |
| | |                                ---------------------   ----------------------                                | | |
| | |                                | Left y axis title |   | Right y axis title |                                | | |
| | |                                | (_da_lefty_title) |   | (_da_righty_title) |                                | | |
| | |                                | *if not on left*  |   | *if not on right*  |                                | | |
| | |                                ---------------------   ----------------------                                | | |
| | | -------------------- ---------------------- --------------------- --------------------- -------------------- | | |
| | | |                  | |                    | |      Graph        | |                   | |                  | | | |
| | | |Left y axis title | | Left y axis labels | | (_da_lefty_graph) | |Right y axis labels| |Right y axis title| | | |
| | | |(_da_lefty_title) | |  (_da_lefty_label) | | (_da_righty_graph)| |(_da_righty_label) | |(_da_righty_title)| | | |
| | | |*if not above axis| |                    | | they overlap      | |                   | |*if not above axis| | | |
| | | -------------------- ---------------------- --------------------- --------------------- -------------------- | | |
| | |  * currently _da_lefty_title                ----------------------                                           | | |
| | |    overlaps _da_lefty_label                 |Bottom x axis labels|                                           | | |
| | |                                             | (_da_bottomx_label)|                                           | | |
| | |                                             ----------------------                                           | | |
| | |                                             ----------------------                                           | | |
| | |                                             | Bottom x axis title|                                           | | |
| | |                                             | (_da_bottomx_title)|                                           | | |
| ---                                             ----------------------                                           --- |
| -------------------------------------------------------------------------------------------------------------------- |
| |                          Legend (_da_lefty_bottom_legend and/or _da_righty_bottom_legend)                        | |
| -------------------------------------------------------------------------------------------------------------------- |
------------------------------------------------------------------------------------------------------------------------
</pre>
*/
public class TSGraph	// extends GRGraph //Future development??
implements ActionListener
{

	/**
	 * Choices for leftYAxisViewYMin and similar, to allow dynamic y-axis scaling separate from product.
	 * Use product properties, Auto or specific value.
	 */
	protected static String YAXIS_LIMITS_USE_PRODUCT_PROPERTIES = "UseProductProperties";
	/**
	 * Automatically fill Y using data in current view and adjust every window.
	 */
	// TODO sam 2017-04-23 evaluate whether to enable.
	//protected final String YAXIS_LIMITS_AUTOFILL_EACH_VIEW = "AutoFillEachView";
	/**
	 * Automatically fill Y using data in current view and keep for other views.
	 */
	protected static String YAXIS_LIMITS_AUTOFILL_AND_KEEP = "AutoFillAndKeep";

/**
Popup menu options.
*/
private final String
	__MENU_ANALYSIS_DETAILS = "Analysis Details",
	__MENU_PROPERTIES = "Properties",
	__MENU_REFRESH = "Refresh",
	__MENU_Y_SET_Y_LIMITS = "Set Y Limits",
	__MENU_Y_MAXIMUM_AUTO = "Set Y Maximum to Auto (round maximum data value for full period)",
	__MENU_Y_MINIMUM_AUTO = "Set Y Minimum to Auto (round minimum data value for full period)",
	__MENU_Y_MAXIMUM_FILL_WINDOW = "Set Y Maximum to Fill Window",
	__MENU_Y_MINIMUM_FILL_WINDOW = "Set Y Minimum to Fill Window";

/**
If the graph type is Duration, this holds the results of the duration data for each time series.
*/
private List<TSDurationAnalysis> _duration_data = null;

/**
The following reference is used internally so that the _graphics does not need to be passed between methods in this class.
The Graphics is volatile and should be reset in each call to paint().
*/
private Graphics2D _graphics = null;

/**
Indicates whether units should be ignored for normal graphs, for left y-axis.
This allows line graphs to plot different units and units are put in the legend and removed from the y-axis label.
If the units are not the same, then one of the following will occur:
<ol>
<li>	If the "LeftYAxisIgnoreUnits" property is set, then it will be used to indicate how the data should be treated.</li>
<li>	If the property is not set, the user will be prompted as to what they want to do.
	They can choose to not continue.</li>
</ol>
The _ignore_units flag is then set.
If necessary, the TSGraphJComponent.needToClose() method will be called and the graph won't be displayed.
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
Null time series may be included to ensure that the number of time series aligns with graph 'Data' properties.
*/
private List<TS> __tslist = null;

/**
List of all the derived time series to plot.
For example, the stacked bar graph requires that total time series are used for plotting positions.
The contents of the list are determined by the graph type.
This list is guaranteed to be non-null but may be empty.
*/
private List<TS> __derivedTSList = new ArrayList<>();

/**
 * Raster graph symbol table that provides colors:
 * - currently must be the same for if multiple time series are specified
 */
private GRSymbolTable rasterSymbolTable = null;

/**
 * List of time series that are selected (by clicking on the legend).
 * These time series will be displayed using graph "SelectedTimeSeries*" properties, for example wider line, different color.
 */
private List<TS> _selectedTimeSeriesList = new ArrayList<>();

/**
 * Map of the time series in the legend matched against GRLimits() for drawing limits,
 * so that the legend area is a "hotspot" for selecting the time series.
 */
private HashMap<TS,GRLimits> __legendTimeSeriesDrawMap = new HashMap<>();

/**
 * TODO sam 2017-02-19 need to enable this, sort of like when Excel warns about opening files from the internet.
 * List of serious errors for the graph, to be draw in the upper left.
 */
private List<String> __errorMessageList = new ArrayList<>();

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
 * Left y-axis minimum y for viewing (separate from TS product), used with TSGrapyYAxisLimits_JDialog.
 */
private String leftYAxisViewMinY = YAXIS_LIMITS_USE_PRODUCT_PROPERTIES;

/**
 * Value computed from data at user's request, to be used for limit.
 */
private double leftYAxisViewMinYFromData = Double.NaN;

/**
 * Left y-axis maximum y for viewing (separate from TS product), used with TSGrapyYAxisLimits_JDialog.
 */
private String leftYAxisViewMaxY = YAXIS_LIMITS_USE_PRODUCT_PROPERTIES;

/**
 * Value computed from data at user's request, to be used for limit.
 */
private double leftYAxisViewMaxYFromData = Double.NaN;

/**
 * Right y-axis minimum y for viewing (separate from TS product), used with TSGrapyYAxisLimits_JDialog.
 */
private String rightYAxisViewMinY = YAXIS_LIMITS_USE_PRODUCT_PROPERTIES;

/**
 * Value computed from data at user's request, to be used for limit.
 */
private double rightYAxisViewMinYFromData = Double.NaN;

/**
 * Right y-axis maximum y for viewing (separate from TS product), used with TSGrapyYAxisLimits_JDialog.
 */
private String rightYAxisViewMaxY = YAXIS_LIMITS_USE_PRODUCT_PROPERTIES;

/**
 * Value computed from data at user's request, to be used for limit.
 */
private double rightYAxisViewMaxYFromData = Double.NaN;

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
TSProduct sub-product number (0+, which is 1 off the value in the product file).
*/
private int subproduct = 0;

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
The left and right y-axis limits may differ but the x-axis limits should be the same so that time aligns.
*/
private TSLimits _max_tslimits_lefty = null;

/**
Limits for time series data for full period, right y-axis, with no adjustments for nice labels.
The left and right y-axis limits may differ but the x-axis limits should be the same so that time aligns.
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
Drawing area for error message shown on graph.
*/
private GRJComponentDrawingArea _da_error = null;

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
Drawing area for left y-axis bottom legend.
*/
private GRJComponentDrawingArea _da_lefty_bottom_legend = null;

/**
Drawing area for left y-axis left legend.
*/
private GRJComponentDrawingArea _da_lefty_left_legend = null;

/**
Drawing area for left y-axis right legend.
*/
private GRJComponentDrawingArea _da_lefty_right_legend = null;

/**
Drawing area for inside legend.
*/
private GRJComponentDrawingArea _da_lefty_inside_legend = null;

/**
Drawing area for right y-axis bottom legend.
*/
private GRJComponentDrawingArea _da_righty_bottom_legend = null;

/**
Drawing area for right y-axis left legend.
*/
private GRJComponentDrawingArea _da_righty_left_legend = null;

/**
Drawing area for right y-axis right legend.
*/
private GRJComponentDrawingArea _da_righty_right_legend = null;

/**
Drawing area for right raster legend.
*/
private GRJComponentDrawingArea _da_right_raster_legend = null;

/**
Drawing area for inside legend.
*/
private GRJComponentDrawingArea _da_righty_inside_legend = null;

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
Data limits for error message.
*/
private GRLimits _datalim_error = null;

/**
Drawing limits for error message.
*/
private GRLimits _drawlim_error = null;

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

// Don't carry this around because it gets confusing.
//private GRLimits _datalim_graph = null;		// Limits for graph.
// _data_limits are the current viewable limits.  If a reference graph this is.
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
Drawing limits for bottom X labels.
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
Data limits for left y-axis bottom legend.
*/
private GRLimits _datalim_lefty_bottom_legend = null;

/**
Drawing limits for left y-axis bottom legend.
*/
private GRLimits _drawlim_lefty_bottom_legend = null;

/**
Data limits for left y-axis left legend.
*/
private GRLimits _datalim_lefty_left_legend = null;

/**
Drawing limits for left y-axis left legend.
*/
private GRLimits _drawlim_lefty_left_legend = null;

/**
Data limits for left y-axis right legend.
*/
private GRLimits _datalim_lefty_right_legend = null;

/**
Drawing limits for left y-axis right legend.
*/
private GRLimits _drawlim_lefty_right_legend = null;

/**
Data limits for left y-axis inside legend.
*/
private GRLimits _datalim_lefty_inside_legend = null;

/**
Drawing limits for left y-axis inside legend.
*/
private GRLimits _drawlim_lefty_inside_legend = null;

/**
Data limits for right y-axis bottom legend.
*/
private GRLimits _datalim_righty_bottom_legend = null;

/**
Drawing limits for right y-axis bottom legend.
*/
private GRLimits _drawlim_righty_bottom_legend = null;

/**
Data limits for right y-axis left legend.
*/
private GRLimits _datalim_righty_left_legend = null;

/**
Drawing limits for right y-axis left legend.
*/
private GRLimits _drawlim_righty_left_legend = null;

/**
Data limits for right y-axis right legend.
*/
private GRLimits _datalim_righty_right_legend = null;

/**
Data limits for the right raster legend.
*/
private GRLimits _datalim_right_raster_legend = null;

/**
Drawing limits for the right raster legend.
*/
private GRLimits _drawlim_right_raster_legend = null;

/**
Drawing limits for right y-axis right legend.
*/
private GRLimits _drawlim_righty_right_legend = null;

/**
Data limits for right y-axis inside legend.
*/
private GRLimits _datalim_righty_inside_legend = null;

/**
Drawing limits for right y-axis inside legend.
*/
private GRLimits _drawlim_righty_inside_legend = null;

// Dimensions for drawing areas.

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

//TODO sam 2017-04-23 when is this ever changed to true (seems to not be)?
/**
When zooming, keep the Y axis limits the same as for full period computed initially.
False indicates to recompute from data in the zoom window.
False also indicates that users can change the min and max values in the properties JFrame.
*/
private boolean _zoomKeepFullPeriodYLimits = false;

/**
Regression data used by scatter plot.
*/
private List<TSRegression> _regression_data = null;

// TODO SAM Evaluate adding double mass plot.
//private TSDoubleMass _double_mass_data = null;// Used by double mass plot.

/**
Maximum time interval for time series being plotted, used to control x-axis behavior.
*/
private int _interval_max = TimeInterval.SECOND;

/**
Minimum time interval for time series being plotted, used to control x-axis behavior.
*/
private int _interval_min = TimeInterval.YEAR;

/**
 * Indicate whether drawing area outlines should be shown.
 */
private boolean _showDrawingAreaOutline = false;

/**
Precision for x-axis date data.
This is not private because TSViewGraphJFrame uses the precision for the mouse tracker.
*/
protected int _xaxis_date_precision;

/**
DateTime format to use for bottom x-axis date data.
*/
private int _bottomx_date_format = DateTime.FORMAT_UNKNOWN;

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
Array for drawing shapes, in particular to avoid moveTo/lineTo.
*/
private double [] xCacheArray = null;

/**
Array for drawing shapes, in particular to avoid moveTo/lineTo.
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
If true, then the dates that were set for start_date and end_date will be used for computing the date limits.
Otherwise, start_date and end_date will be recomputed.
*/
private boolean __useSetDates = false;

/**
Construct a TSGraph and display the time series.
@param dev TSGraphJComponent that is managing this graph.
@param drawlim_page Initial device limits that should be used for this graph,
determined in the managing TSGraphJComponent (the limits will change as the component is resized.
@param tsproduct TSProduct containing information to control display of time series.
Most of these properties are documented in TSViewFrame, with the following additions:
@param display_props Additional properties used for displays.
ReferenceGraph can be set to "true" or "false" to indicate whether the graph is a reference graph.
ReferenceTSIndex can be set to a list index to indicate the reference time series for the reference graph
(the default is the time series with the longest overall period).
This value must be set for the local tslist list that is passed in.
@param subproduct The sub-product from the main product.
This is used to look up properties specific to this graph product.  The first product is 1.
@param tslist list of time series to graph.
Only the time series for this graph are expected but time series for other graphs can be reused in multiple graphs
(access them in the TSProduct).
Null time series will have been inserted as appropriate to retain the sequence with the time series product.
@param reference_ts_index Index in the "tslist" for the reference time series.
This may be different from the "ReferenceTSIndex" property in display_props,
which was for the original time series list (not the subset used just for this graph).
*/
public TSGraph ( TSGraphJComponent dev, GRLimits drawlim_page, TSProduct tsproduct, PropList display_props,
	int subproduct, List<TS> tslist, int reference_ts_index ) {
	String routine = getClass().getSimpleName() + ".TSGraph";

	// Keep a local reference.

	this._dev = dev;
	this._tsproduct = tsproduct;
	this._display_props = display_props;
	if ( this._display_props == null ) {
		this._display_props = new PropList ( "TSGraph" );
	}
	this.subproduct = subproduct;
    this.__leftYAxisGraphType = TSGraphType.valueOfIgnoreCase ( tsproduct.getLayeredPropValue ( "GraphType", this.subproduct, -1, false ) );
    if ( this.__leftYAxisGraphType == null ) {
        // Should never happen.
        this.__leftYAxisGraphType = TSGraphType.LINE;
    }
    this.__rightYAxisGraphType = TSGraphType.valueOfIgnoreCase ( tsproduct.getLayeredPropValue ( "RightYAxisGraphType", this.subproduct, -1, false ) );
    if ( this.__rightYAxisGraphType == null ) {
        // Should never happen.
    	this.__rightYAxisGraphType = TSGraphType.NONE;
    }
	if ( tslist == null ) {
		// Create an empty list so checks for null don't need to be added everywhere.
        Message.printStatus(2, routine, "Null list of time series for graph.  Using empty list for graph." );
		this.__tslist = new ArrayList<>();
	}
	else if ( this.__leftYAxisGraphType != TSGraphType.RASTER ) {
	    // OK to display all time series.
        this.__tslist = tslist;
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
	    // Only to graph or or multiple time series:
		// - code logic will branch depending on whether one or multiple time series are graphed
	    //__tslist = new ArrayList<>();
	    //if ( tslist.size() > 0 ) {
	        //__tslist.add(tslist.get(0));
	    //}
        this.__tslist = tslist;
	}
	// Get time series lists for each axis.
	this.__left_tslist = new ArrayList<>();
	this.__right_tslist = new ArrayList<>();
	String propVal = null;
	int lefttsCount = 0;
	int righttsCount = 0;
	for ( int its = 0; its < this.__tslist.size(); its++ ) {
		propVal = this._tsproduct.getLayeredPropValue ( "YAxis", this.subproduct, its, false );
		// Include time series associated with the left y-axis.
		if ( (propVal == null) || propVal.isEmpty() || propVal.equalsIgnoreCase("Left") ) {
			this.__left_tslist.add(this.__tslist.get(its));
			++lefttsCount;		}
		else {
			// Add a null so product properties will align properly with the full time series list.
			this.__left_tslist.add(null);
		}
		// Include time series associated with the right y-axis.
		if ( (propVal != null) && propVal.equalsIgnoreCase("Right") ) {
			this.__right_tslist.add(this.__tslist.get(its));
			++righttsCount;
		}
		else {
			// Add a null so product properties will align properly with the full time series list.
			this.__right_tslist.add(null);
		}
	}

	// Check a few properties to increase performance.
	// The only properties that are checked and set locally here are those that are not going
	// to change during the life of the graph, even if its properties are changed.
	// All other properties should be checked before being used
	// (e.g., axis properties should be checked in the drawAxesBack() method).
	// It is a little slower to look up the properties sometimes but the code is simpler to maintain.

	if ( this._dev.isReferenceGraph() ) {
		this._is_reference_graph = true;
		this._gtype = "Ref:";
	}

	// Might need to use this when try to process all null time series.
	int ssize = this.__tslist.size();
	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
		    Message.printStatus(2, routine, this._gtype + "Have " + ssize + " time series for all graphs." );
		    Message.printStatus(2, routine, this._gtype + "Have " + lefttsCount + " time series for left y-axis graphs." );
		    Message.printStatus(2, routine, this._gtype + "Have " + righttsCount + " time series for right y-axis graphs." );
		}
	}
	TS sts;
	for (int ii = 0; ii < ssize; ii++) {
		sts = this.__tslist.get(ii);
		if (sts == null) {
			Message.printStatus(3, routine, this._gtype + "TS[" + ii + "] is null");
		}
		else {
			Message.printStatus(3, routine, this._gtype + "TS[" + ii + "] is " + sts.getIdentifierString() +
			        "period " + sts.getDate1() + " to " + sts.getDate2() );
		}
	}

	// A reference TS index can be used in a main or reference graph.

	this._reference_ts_index = reference_ts_index;

	// TODO SAM 2013-02-06 maybe should put some of the other code in the following call?
	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Calling checkInternalProperties()..." );
		}
	}
	checkInternalProperties ();

	if (this._is_reference_graph) {
		this.__leftYAxisGraphType = TSGraphType.LINE;
	}

	this._drawlim_page = new GRLimits ( drawlim_page );

	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Calling openDrawingAreas()..." );
		}
	}
	openDrawingAreas ();

	// Perform the data analysis once to get data limits.
	// This is the place where the reference graph has its data set.
	// This is also checked in the paint() method in case any analysis settings change.

	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Calling doAnalysis()..." );
		}
	}
	doAnalysis(this.__leftYAxisGraphType);
	this.__lastLeftYAxisGraphType = this.__leftYAxisGraphType;

	// Initialize the data limits.

	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Calling setDataLimits()..." );
		}
	}
	if ( this._is_reference_graph ) {
	    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
	        GRLimits limits = new GRLimits(this._max_lefty_data_limits);
	        this._da_lefty_graph.setDataLimits ( limits.reverseY() );
	    }
	    else {
	        this._da_lefty_graph.setDataLimits ( this._max_lefty_data_limits );
	    }
	}
	else {
		// Left y-axis.
	    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
            GRLimits limits = new GRLimits(this._data_lefty_limits);
            this._da_lefty_graph.setDataLimits ( limits.reverseY() );
        }
        else {
            this._da_lefty_graph.setDataLimits ( this._data_lefty_limits );
        }
		// Right y-axis.
	    if ( this.__rightyDirection == GRAxisDirectionType.REVERSE ) {
            GRLimits limits = new GRLimits(this._data_righty_limits);
            this._da_righty_graph.setDataLimits ( limits.reverseY() );
        }
        else {
            this._da_righty_graph.setDataLimits ( this._data_righty_limits );
        }
	}

	// Determine the minimum and maximum time interval for time series.
	// Because the x-axis is shared with left and right y-axis, use all time series.

	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Determining limits to time series data interval..." );
		}
	}
	int size = 0;
	if ( this.__tslist != null ) {
		size = this.__tslist.size();
	}
	int its = -1;
	if ( this.__tslist != null ) {
		++its;
		TS ts = null;
		for ( int i = 0; i < size; i++ ) {
			ts = this.__tslist.get(i);
			if ( (ts == null) || !ts.getEnabled() || isTSEnabled(its) ) {
				continue;
			}
			// Check the interval so that can make decisions during plotting.
			try {
                this._interval_max = MathUtil.max ( this._interval_max, ts.getDataIntervalBase() );
				this._interval_min = MathUtil.min ( this._interval_min, ts.getDataIntervalBase() );
			}
			catch ( Exception e ) {
				// Probably never will occur.
                Message.printWarning (3, routine, e);
			}
		}
	}

	// If necessary, read the raster symbol table.
    if ( getLeftYAxisGraphType() == TSGraphType.RASTER ) {
		this.rasterSymbolTable = createRasterSymbolTable ( this.__tslist );
	}

	if ( !this._is_reference_graph ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "Calling computeXAxisDatePrecision()..." );
		}
	}
	computeXAxisDatePrecision ();
}

/**
Handle action events generated by the popup menu for this graph.
@param event action event to handle
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand ();

	if ( command.equals(__MENU_PROPERTIES) ) {
		// Only one properties window is shown per graph so let the TSViewFrame handle showing the properties.
		TSViewGraphJFrame frame = (TSViewGraphJFrame)this._dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES);

		// Display the properties for the graph of choice.
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(this.subproduct);
		// Immediately remove from the component container.
		// This works except that when escape is pressed on the popup menu never get an action event to remove the menu
		// (is there a better way to handle?
		// Could always add the popups up front but does this take more resources than having a few extra popups because of escapes?).
		this._dev.remove(this._graph_JPopupMenu);
		frame = null;
	}
	else if (command.equals(__MENU_ANALYSIS_DETAILS)) {
		// Display the results of the regression(s), duration plot, etc.
		if ( (this._duration_data != null) && (this._duration_data.size() > 0) ) {
			// Create a DataTable and then use generic table view component.
			DataTable table = new DataTable();
			List<TSDurationAnalysis> durationList = this._duration_data;
			List<DataTable> tableList = new ArrayList<>(durationList.size());
			String [] viewLabels = new String[durationList.size()];
			int its = -1;
			for ( TSDurationAnalysis a : durationList ) {
				// Each analysis has a time series and arrays of values and percents.
				// Create simple tables for each and pass to the display.
				++its;
				table = new DataTable();
				tableList.add(table);
				TS ts = a.getTS();
				// This is copied from TSViewTable_TableModel.getColumnName().
				boolean useExtendedLegend = false; // For now disable this to see how following works.
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
						// Swallow for now.
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
		if ( this._regression_data != null ) {
			int size = 0;
			if ( this._regression_data != null ) {
				size =  this._regression_data.size();
			}
			TSRegression r = null;
			List<String> v = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				r =  this._regression_data.get(i);
				if (r == null) {
					continue;
				}
				// Split by newlines so that report has separate lines of information.
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
		this._dev.remove(this._graph_JPopupMenu);
	}
	else if (command.equals(__MENU_REFRESH)) {
		// Refresh the image.  This is probably only used on UNIX because the refresh does not occur automatically.
		this._dev.refresh ();
	}
	else if (command.equals(__MENU_Y_SET_Y_LIMITS)) {
		// Show dialog to set Y-axis limits for viewing:
		// - limits are separate from product but may default to the product.
		// Get the frame that manages this component.
		Component c = SwingUtilities.getRoot(this._dev);
		if ( c instanceof JFrame ) {
			// TSGraph is only used in the TSViewGraphJFrame.
			new TSGraphYAxisLimits_JDialog ( (TSViewGraphJFrame)c, this._dev, this );
			// Pressing OK in the dialog will set the properties in the TSGraph for viewing y-axis properties.
			// Next reset the data used for drawing:
			// - use the existing data limits to ensure x-limits and starting y-limits,
			//   but the y-limits will be reset by the values set from above action (user-specified) or calculated
			//   from a view of the time series (below)
			if ( this.leftYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ||
				this.leftYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
				// Request to use current view of data to calculate limits for graph
				TSLimits limits = null;
				List<TS> tslist = null;
				if ( this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL ) {
			    	int nreg = 0;
					if ( this.__tslist != null ) {
						nreg = this.__tslist.size() - 1;
					}
					List<TS> v = new ArrayList<>();
					TSRegression regressionData = null;
					for (int i = 0; i < nreg; i++) {
						if (!isTSEnabled(i + 1) ) {
							continue;
						}
						regressionData = this._regression_data.get(i);
						v.add(regressionData.getResidualTS());
					}
					tslist = v;
				}
				else if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
			    	int nreg = 0;
					if (this.__tslist != null) {
						nreg = this.__tslist.size() - 1;
					}
					List<TS> v = new ArrayList<>();
					TSRegression regressionData = null;
					if ( isTSEnabled(0)) {
						v.add(this.__tslist.get(0));
					}
					for (int i = 0; i < nreg; i++) {
						if (!isTSEnabled(i + 1) ) {
							continue;
						}
						regressionData = this._regression_data.get(i);
						if (isTSEnabled(i + 1) ) {
							v.add(regressionData.getDependentTS());
							v.add(regressionData.getPredictedTS());
						}
					}
					tslist = v;
				}
				else {
					boolean includeLeftYAxis = true;
					boolean includeRightYAxis = false;
					// No nulls since just computing limits.
					boolean includeNulls = false;
					tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
				}

				try {
					limits = TSUtil.getDataLimits(tslist, this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
					computeDataLimits_CheckDisplayLimitProperties(tslist, limits);
					if ( limits.areLimitsFound() ) {
						if ( this.leftYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
							setLeftYAxisViewMinYFromData(limits.getMinValue());
						}
						if ( this.leftYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
							setLeftYAxisViewMaxYFromData(limits.getMaxValue());
						}
					}
				}
				catch (Exception e) {
					String routine = "TSGraph.actionPerformed";
					Message.printWarning(2, routine, "There was an error getting the limits for the period.  The zoom will not be changed.");
					Message.printWarning(2, routine, e);
					return;
				}
			}
			// Set the limits in the graph:
			// - this will trigger use of user-specified input
			setDataLimitsForDrawing(this._da_lefty_graph.getDataLimits());
			this._dev.setForceRedraw(true,true);
			this._dev.repaint();
		}
	}
	else if (command.equals(__MENU_Y_MAXIMUM_FILL_WINDOW)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)_dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);

		// Display the properties for the graph of choice.
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(this.subproduct);
		TSLimits limits = null;

		List<TS> tslist = null;
		if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	    	int nreg = 0;
			if (this.__tslist != null) {
				nreg = this.__tslist.size() - 1;
			}
			List<TS> v = new ArrayList<>();
			TSRegression regressionData = null;
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1) ) {
					continue;
				}
				regressionData = this._regression_data.get(i);
				v.add(regressionData.getResidualTS());
			}
			tslist = v;
		}
		else if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
	    	int nreg = 0;
			if (this.__tslist != null) {
				nreg = this.__tslist.size() - 1;
			}
			List<TS> v = new ArrayList<>();
			TSRegression regressionData = null;
			if (isTSEnabled(0)) {
				v.add(this.__tslist.get(0));
			}
			for (int i = 0; i < nreg; i++) {
				if (!isTSEnabled(i + 1) ) {
					continue;
				}
				regressionData = this._regression_data.get(i);
				if (isTSEnabled(i + 1) ) {
					v.add(regressionData.getDependentTS());
					v.add(regressionData.getPredictedTS());
				}
			}
			tslist = v;
		}
		else {
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			// No nulls since just computing limits.
			boolean includeNulls = false;
			tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		}

		try {
			limits = TSUtil.getDataLimits(tslist, this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
			computeDataLimits_CheckDisplayLimitProperties(tslist, limits);
		}
		catch (Exception e) {
			String routine = "TSGraph.actionPerformed";
			Message.printWarning(2, routine, "There was an error getting the limits for the period.  The zoom will not be changed.");
			Message.printWarning(2, routine, e);
			return;
		}

		if (IOUtil.testing()) {
			Message.printStatus(2, "", "Start: " + this._start_date);
			Message.printStatus(2, "", "  End: " + this._end_date);
			Message.printStatus(2, "", "  Max: " + limits.getMaxValue());
			Message.printStatus(2, "", "  Min: " + limits.getMinValue());
		}

		pframe.setMaximumYValue("" + limits.getMaxValue(), true);
	}
	else if (command.equals(__MENU_Y_MINIMUM_FILL_WINDOW)) {
		TSViewGraphJFrame frame = (TSViewGraphJFrame)this._dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);

		// Display the properties for the graph of choice.
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(this.subproduct);
		TSLimits limits = null;

    	int nreg = 0;
		if (this.__tslist != null) {
			nreg = this.__tslist.size() - 1;
		}
		List<TS> v = new ArrayList<>();
		TSRegression regressionData = null;
		for (int i = 0; i < nreg; i++) {
			if (!isTSEnabled(i + 1) ) {
				continue;
			}
			regressionData = this._regression_data.get(i);
			v.add(regressionData.getResidualTS());
		}

		try {
			limits = TSUtil.getDataLimits(v,this._start_date, this._end_date, "", false,this._ignoreLeftAxisUnits);
			computeDataLimits_CheckDisplayLimitProperties(v, limits);
		}
		catch (Exception e) {
			String routine = "TSGraph.actionPerformed";
			Message.printWarning(2, routine, "There was an error getting the limits for the period.  The zoom will not be changed.");
			Message.printWarning(2, routine, e);
			return;
		}

		if (IOUtil.testing()) {
			Message.printStatus(2, "", "Start: " + this._start_date);
			Message.printStatus(2, "", "  End: " + this._end_date);
			Message.printStatus(2, "", "  Max: " + limits.getMaxValue());
			Message.printStatus(2, "", "  Min: " + limits.getMinValue());
		}

		pframe.setMinimumYValue("" + limits.getMinValue(),true);
	}
	else if (command.equals(__MENU_Y_MAXIMUM_AUTO)) {
		// TODO sam 2017-04-23 has been replaced by __MENU_Y_SET_Y_LIMITS above - phase out after production use.
		TSViewGraphJFrame frame = (TSViewGraphJFrame)this._dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);

		// Display the properties for the graph of choice.
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(this.subproduct);
		pframe.setMaximumYValue("Auto");
	}
	else if (command.equals(__MENU_Y_MINIMUM_AUTO)) {
		// TODO sam 2017-04-23 has been replaced by __MENU_Y_SET_Y_LIMITS above - phase out after production use.
		TSViewGraphJFrame frame = (TSViewGraphJFrame)this._dev.getJFrame();
		frame.getTSViewJFrame().openGUI(TSViewType.PROPERTIES_HIDDEN);

		// Display the properties for the graph of choice.
		TSProductJFrame pframe = frame.getTSViewJFrame().getTSProductJFrame();
		pframe.setSubproduct(this.subproduct);
		pframe.setMinimumYValue("Auto", true);
	}
}

/**
Adjust a confidence curve for the XY Scatter plot to make sure the points lie within the graph.
Most likely some points will be off the graph.
In these cases, interpolate coordinates to the edge of the graph.
If the next/previous point is also off the graph, then set the point to the last endpoint.
This makes sure that have cleanly handled the end points without having to adjust the array lengths.
@param x X-coordinates for confidence curve.
@param y Y-coordinates for confidence curve.
@param npts Number of points to process (may be less than array size).
*/
private void adjustConfidenceCurve ( double [] x, double [] y, int npts ) {
	String routine = getClass().getSimpleName() + ".adjustConfidenceCurve";
    // First figure out if the slope of the line is up to the right or down to the right.
	int i = 0;
	double x_edge = 0.0;
	double min_datay = this._data_lefty_limits.getMinY();
	double max_datay = this._data_lefty_limits.getMaxY();
	if ( y[0] < y[npts - 1] ) {
		// Slope is up and to the right.  Adjust the points on the left.
		for ( i = 0; i < npts; i++ ) {
			if ( y[i] >= min_datay ) {
				// Found the first Y that is on the graph.  Interpolate the previous point.
				if ( i == 0 ) {
					// No need to do more.
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
				// Done adjusting first points.
				break;
			}
		}
		// Adjust the points on the right.
		for ( i = (npts - 1); i >= 0; i-- ) {
			if ( y[i] <= max_datay ) {
				// Found the first Y that is on the graph.  Interpolate the previous point.
				if ( i == (npts - 1) ) {
					// No need to do more.
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
				// Done adjusting last points.
				break;
			}
		}
	}
	else {
        // Slope is down to the right.
		// FIXME ... do later... inverse correlation not likely with what are doing.
        Message.printWarning(3, routine, "Negative slope is not supported for confidence curve.");
	}
}

/**
Indicate whether the graph can zoom.  Currently this is tied to the graph type.
Later it may be tied to a property.
@return true if the graph can zoom, false otherwise.
*/
public boolean canZoom() {
	if ((this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (this.__leftYAxisGraphType == TSGraphType.DURATION) ) {
		return false;
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
		// Default for single time series is not zoom.
		boolean canZoom = false;
		if ( this.__tslist.size() > 1 ) {
			// Allow zoom if more than one time series because X-axis is full period date/time.
			canZoom = true;
		}
		return canZoom;
	}
	else {
		return true;
	}
}

/**
Check properties that may change dynamically and, if necessary,
reset internal variables that correspond to properties.
Internal data are used to increase performance, especially for data that are used often
(e.g., the axis precision and units, which are used for the mouse tracker).
This method should be called immediately before drawing.
@param subproduct the subproduct being processed
*/
private void checkInternalProperties () {
	// "BottomXAxisLabelFormat" = this._bottomx_date_format;

	String prop_val = this._tsproduct.getLayeredPropValue ( "BottomXAxisLabelFormat", this.subproduct, -1, false );
	if ( prop_val != null ) {
		// Currently only handle special cases.
		if ( prop_val.equalsIgnoreCase("MM-DD") ) {
			this._bottomx_date_format = DateTime.FORMAT_MM_DD;
		}
	}

	// "LeftYAxisDirection"

    this.__leftyDirection = GRAxisDirectionType.NORMAL;
    prop_val = this._tsproduct.getLayeredPropValue ( "LeftYAxisDirection", this.subproduct, -1, false );
    if ( prop_val != null ) {
        this.__leftyDirection = GRAxisDirectionType.valueOfIgnoreCase(prop_val);
        if ( this.__leftyDirection == null ) {
            this.__leftyDirection = GRAxisDirectionType.NORMAL;
        }
    }
    //String routine = getClass().getName() + ".checkInternalProperties";
    //Message.printStatus(2, routine, "LeftYAxisDirection=" + this.__leftyDirection );

	// "LeftYAxisLabelPrecision" = this._lefty_precision;

	this._lefty_precision = 2;
	prop_val = this._tsproduct.getLayeredPropValue ( "LeftYAxisLabelPrecision", this.subproduct, -1, false );
	if ( prop_val != null ) {
		try {
			this._lefty_precision = Integer.parseInt ( prop_val );
		}
		catch ( NumberFormatException e ) {
			this._lefty_precision = 2;
		}
	}

	// "RightYAxisDirection"

    this.__rightyDirection = GRAxisDirectionType.NORMAL;
    prop_val = this._tsproduct.getLayeredPropValue ( "RightYAxisDirection", this.subproduct, -1, false );
    if ( prop_val != null ) {
        this.__rightyDirection = GRAxisDirectionType.valueOfIgnoreCase(prop_val);
        if ( this.__rightyDirection == null ) {
            this.__rightyDirection = GRAxisDirectionType.NORMAL;
        }
    }

	// "RightYAxisLabelPrecision" = this._righty_precision;

	this._righty_precision = 2;
	prop_val = this._tsproduct.getLayeredPropValue ( "RightYAxisLabelPrecision", this.subproduct, -1, false );
	if ( prop_val != null ) {
		try {
			this._righty_precision = Integer.parseInt( prop_val );
		}
		catch ( NumberFormatException e ) {
			this._righty_precision = 2;
		}
	}
}

/**
TODO SAM 2010-11-22 This only computes the max/min date/time range?  Evaluate SetDataLimits() use.
Compute the maximum data limits based on the time series.
This is normally only called from doAnalysis(), which is called at construction.
The maximum values and the current data limits are set to the limits,
which serve as the initial data limits until zooming occurs.
The x-axis limits (time) for left and right y-axes must be set to the same so that time will be accurate.
@param subproduct the subproduct being processed.
@param computeFromMaxPeriod whether to compute data limits from the max dates or not.
Currently, is only called from doAnalysis() when initializing the graphs at creation.
The method is also called by reinitializeGraphs() when recreating graphs after property edits,
using current limits (not max).
For other graphs, see setComputeWithSetDates().
*/
protected void computeDataLimits ( boolean computeFromMaxPeriod ) {
	String routine = getClass().getSimpleName() + ".computeDataLimits";
    TSGraphType graphType = getLeftYAxisGraphType();
	// Exceptions are thrown when trying to draw empty graph (no data).

	try {
	    // Because right y-axis graphs are currently simple, do a first cut at getting the maximum period.
	    // This is used when processing the left y-axis maximum start and end in the first pass below.
	    // Then, when the right y-axis is processed, its axis can be extended to include the left-yaxis maximum.
	    boolean includeLeftYAxis = false;
	    boolean includeRightYAxis = true;
	    // No nulls since computing data limits.
	    boolean includeNulls = false;
	    List<TS> enabledRightYAxisTSList = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
	    TSLimits rightYLimits0 = null;
	    if ( enabledRightYAxisTSList.size() > 0 ) {
	    	rightYLimits0 = TSUtil.getPeriodFromTS(enabledRightYAxisTSList, TSUtil.MAX_POR);
	    }

	    // Left y-axis is the most complex so process first.
	    includeLeftYAxis = true;
	    includeRightYAxis = false;
	    List<TS> enabledLeftYAxisTSList = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		if (enabledLeftYAxisTSList.size() == 0) {
			// All this is for left y-axis.
			this.__useSetDates = false;
			if (computeFromMaxPeriod) {
				if (this._max_start_date == null) {
					// No data so set limits to empty.
					this._data_lefty_limits = new GRLimits(0, 0, 0, 0);
					this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
				}
				else {
					// Set the data limits to maximum period.
					this._data_lefty_limits = new GRLimits( this._max_start_date.toDouble(), 0, this._max_end_date.toDouble(), 1);
					this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
				}
			}
			else {
				if ( this._start_date == null ) {
					// No data so set limits to empty.
					this._data_lefty_limits = new GRLimits(0, 0, 0, 0);
					this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
				}
				else {
					// Set the data limits to maximum period.
					this._data_lefty_limits = new GRLimits( this._start_date.toDouble(), 0, this._end_date.toDouble(), 1);
					this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
				}
			}
		}

		// Right y-axis.

		if (enabledRightYAxisTSList.size() == 0) {
			// All this is for right y-axis.
			if (computeFromMaxPeriod) {
				if ( this._max_start_date == null ) {
					this._data_righty_limits = new GRLimits(0, 0, 0, 0);
					this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
				}
				else {
					this._data_righty_limits = new GRLimits( this._max_start_date.toDouble(), 0, this._max_end_date.toDouble(), 1);
					this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
				}
			}
			else {
				if ( this._start_date == null ) {
					this._data_righty_limits = new GRLimits(0, 0, 0, 0);
					this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
				}
				else {
					this._data_righty_limits = new GRLimits( this._start_date.toDouble(), 0, this._end_date.toDouble(), 1);
					this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
				}
			}
		}

		// Left y-axis.

        // First get the date limits from the full set of time series.
		// For some graph types only the left y-axis is used.
		// If left and right y-axis are used the maximum date limits must include both
		// so that overlapping period is correctly aligned.
		TSLimits limits = null;
		if ( enabledLeftYAxisTSList.size() > 0 ) {
			if ( graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL ) {
				// Use only the left y-axis and assume all time series,
				// until there is time to evaluate whether right y-axis can be enabled.
		    	int nreg = this.__tslist.size() - 1;
				List<TS> v = new ArrayList<TS>();
				TSRegression regressionData = null;
				for (int i = 0; i < nreg; i++) {
					if ( !isTSEnabled ( i + 1) ) {
						continue;
					}
					regressionData = this._regression_data.get(i);
					v.add(regressionData.getResidualTS());
				}
				limits = TSUtil.getPeriodFromTS(v, TSUtil.MAX_POR);
				computeDataLimits_CheckDisplayLimitProperties(v, limits);
			}
			else if ( graphType == TSGraphType.AREA_STACKED ) {
			    limits = TSUtil.getPeriodFromTS(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), TSUtil.MAX_POR);
			    computeDataLimits_CheckDisplayLimitProperties(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), limits);
			}
			else {
			    // Get the limits from the enabled time series.
				limits = TSUtil.getPeriodFromTS(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), TSUtil.MAX_POR);
				computeDataLimits_CheckDisplayLimitProperties(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), limits);
			}
			// Extend x-axis limits for the right y-axis.
			if ( (rightYLimits0 != null) && (rightYLimits0.getDate1() != null) && (rightYLimits0.getDate1().lessThan(limits.getDate1())) ) {
				limits.setDate1(new DateTime(rightYLimits0.getDate1()));
			}
			if ( (rightYLimits0 != null) && (rightYLimits0.getDate2() != null) && (rightYLimits0.getDate2().greaterThan(limits.getDate2())) ) {
				limits.setDate2(new DateTime(rightYLimits0.getDate2()));
			}

			if ( this.__useSetDates ) {
				// TODO sam 2017-02-08 why does this not use the set dates?
			}
			else {
				this._start_date = new DateTime ( limits.getDate1() ); // This should be earliest for period, from lines above.
				this._end_date = new DateTime ( limits.getDate2() ); // This should be latest for period, from lines above.
				this._max_start_date = new DateTime ( this._start_date );
				this._max_end_date = new DateTime ( this._end_date );

			}
			this.__useSetDates = false;
			limits = null;	// Clean up.

			// Now get the data limits.  To do the check correctly, the data units must be considered.
			this._ignoreLeftAxisUnits = false;
			this._ignoreRightAxisUnits = false;

			// First set defaults.

			// Some graph types don't show the units on the axis.
			if ( (this.__leftYAxisGraphType == TSGraphType.DOUBLE_MASS)
				|| (this.__leftYAxisGraphType == TSGraphType.PERIOD)
				|| (this.__leftYAxisGraphType == TSGraphType.RASTER)
				|| (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
				this._ignoreLeftAxisUnits = true;
				this._ignoreRightAxisUnits = true;
			}
			// Now check the property (keep a separate copy so can avoid the prompt below if appropriate).
			String ignoreLeftAxisUnitsProp = this._tsproduct.getLayeredPropValue ( "LeftYAxisIgnoreUnits", this.subproduct, -1, false);
			boolean ignoreLeftAxisUnits = false;
			if ( (ignoreLeftAxisUnitsProp != null) && ignoreLeftAxisUnitsProp.equalsIgnoreCase("true") ) {
				this._ignoreLeftAxisUnits = true;
				if (TSUtil.areUnitsCompatible(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), true)) {
					this._ignoreLeftAxisUnits = false;
					this._tsproduct.setPropValue("LeftYAxisIgnoreUnits", "false", this.subproduct, -1);
					ignoreLeftAxisUnits = true;
				}
			}
			String ignoreRightAxisUnitsProp = this._tsproduct.getLayeredPropValue ( "RightYAxisIgnoreUnits", this.subproduct, -1, false);
			boolean ignoreRightAxisUnits = false;
			if ( (ignoreRightAxisUnitsProp != null) && ignoreRightAxisUnitsProp.equalsIgnoreCase("true") ) {
				this._ignoreRightAxisUnits = true;
				if (TSUtil.areUnitsCompatible(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), true)) {
					this._ignoreRightAxisUnits = false;
					this._tsproduct.setPropValue("RightYAxisIgnoreUnits", "false", this.subproduct, -1);
					ignoreRightAxisUnits = true;
				}
			}
			// TODO SAM 2016-10-17 Need to evaluate how to handle right y-axis - for now set to false.
			this._ignoreRightAxisUnits = false;
			try {
				if (this._ignoreLeftAxisUnits) {
					// Can ignore units.
					this._max_tslimits_lefty = TSUtil.getDataLimits( getTSListToRender(true,includeLeftYAxis,includeRightYAxis),
						this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
					computeDataLimits_CheckDisplayLimitProperties(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), this._max_tslimits_lefty);
				}
				else {
	                // Need to have consistent units.
					// For now require them to be the same because don't want to do units conversions on the fly or alter the original data.
					//
					// TODO - need to add on the fly conversion of units (slower but changing original data is a worse alternative).
					includeLeftYAxis = true; // Include left axis time series.
				    includeRightYAxis = false; // Do not include right axis time series.
				    boolean enabledOnly = true; // Want enabled time series only.
				    List<TS> tslistToRender = getTSListToRender(enabledOnly,includeLeftYAxis,includeRightYAxis);
				    if ( !this._is_reference_graph ) {
				    	if ( Message.isDebugOn ) {
				    		Message.printStatus(2, routine, "Calling areUnitsCompatible for left y-axis, have " + tslistToRender.size() + " time series");
				    	}
				    }
					if (!TSUtil.areUnitsCompatible(tslistToRender, true)) {
					    if ( !this._is_reference_graph ) {
					    	if ( Message.isDebugOn ) {
					    		Message.printStatus(2, routine, "Time series to render for left y-axis have incompatible units.");
					    	}
					    }
						if ( this._is_reference_graph ) {
							// Rely on the main graph to set the this._ignore_units flag and determine whether the graph view needs to be closed.
							// Assume that the this._ignore_units flag can be set to true since the reference graph only displays one graph.
							this._ignoreLeftAxisUnits = true;
							this._max_tslimits_lefty = TSUtil.getDataLimits( tslistToRender, this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
							computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._max_tslimits_lefty);
						}
						else if (ignoreLeftAxisUnits) {
							this._max_tslimits_lefty = TSUtil.getDataLimits( tslistToRender,
								this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
							computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._max_tslimits_lefty);
						}
						else {
							// Let the user interactively indicate whether to continue.
							// If running in batch mode, there may not be a parent.
							int x = ResponseJDialog.YES; // Default is to ignore units.
							// The problem is that the parent has not yet been centered itself,
							// so need to get the parent UI component.
							Object uiComponentObject = this._display_props.getContents( "TSViewParentUIComponent" );
							if ( Message.isDebugOn ) {
								Message.printStatus(2, routine, "TSViewParentUIComponent=" + uiComponentObject);
							}
							if ( (uiComponentObject != null) && (uiComponentObject instanceof Component)
								&& ((uiComponentObject instanceof JFrame) || (uiComponentObject instanceof JDialog))) {
								// Can center the dialog on parent JFrame or JDialog.
								if ( uiComponentObject instanceof JDialog ) {
									x = new ResponseJDialog( (JDialog)uiComponentObject, "Incompatible Units",
									"The left y-axis data units are incompatible.\n"
									+ "The graph can be split by editing its properties (right-click on graph when shown).\n\n"
									+ "Continue graphing with incompatible units?",
									ResponseJDialog.YES|ResponseJDialog.NO ).response();
								}
								else {
									x = new ResponseJDialog( (JFrame)uiComponentObject, "Incompatible Units",
									"The left y-axis data units are incompatible.\n"
									+ "The graph can be split by editing its properties (right-click on graph when shown).\n\n"
									+ "Continue graphing with incompatible units?",
									ResponseJDialog.YES|ResponseJDialog.NO ).response();
								}
							}
							else if ( this._dev.getJFrame() != null ) {
								// Fall through that shows the dialog on the first screen.
								x = new ResponseJDialog( this._dev.getJFrame(), "Incompatible Units",
									"The left y-axis data units are incompatible.\n"
									+ "The graph can be split by editing its properties (right-click on graph when shown).\n"
									+ "Continue graphing with incompatible units?",
								ResponseJDialog.YES|ResponseJDialog.NO ).response();
							}
							else {
	                            // No frame so default to ignore units.
								x = ResponseJDialog.YES;
							}
							if ( x == ResponseJDialog.NO ) {
								// Set this so that code that uses this component can check to see if the component needs to close itself.
								this._dev.needToClose( true);
							}
							else {
								this._ignoreLeftAxisUnits = true;
								this._max_tslimits_lefty = TSUtil.getDataLimits( tslistToRender, this._start_date,
								        this._end_date, "", false, this._ignoreLeftAxisUnits);
								computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._max_tslimits_lefty);
							}
						}
					}
					else {
	                	if (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
	                		int nreg = 0;
	                		if ( this.__tslist != null ) {
	                			nreg = this.__tslist.size() - 1;
	                		}

	                		List<TS> v = new ArrayList<>();
	                		TSRegression regressionData = null;
	                		TSLimits tempLimits = null;
	                		double maxValue = 0;
	                		double minValue = 0;
	                		for (int i = 0; i < nreg; i++) {
	                			if (!isTSEnabled(i + 1) ) {
	                				continue;
	                			}
	                			regressionData = this._regression_data.get(i);
	                			v.add(regressionData.getResidualTS());
	                			tempLimits = TSUtil.getDataLimits(v, this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits );
	                			computeDataLimits_CheckDisplayLimitProperties(v, tempLimits);
	                			if (tempLimits.getMaxValue() > maxValue) {
	                				maxValue = tempLimits.getMaxValue();
	                			}
	                			if (tempLimits.getMinValue() < minValue) {
	                				minValue = tempLimits.getMinValue();
	                			}
	                		}
	                		this._max_tslimits_lefty = TSUtil.getDataLimits(tslistToRender,
	                			this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
	                		computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._max_tslimits_lefty);
	                		this._max_tslimits_lefty.setMaxValue(maxValue);
	                		this._max_tslimits_lefty.setMinValue(minValue);
	                	}
	                	else {
	                		this._max_tslimits_lefty = TSUtil.getDataLimits(tslistToRender,
	                			this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
	                		computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._max_tslimits_lefty);
	                	}
					}
				}
				if ( graphType == TSGraphType.PERIOD ) {
					// Period of record graph:
					// - the limits should be a count of the time series,
					//   0 to 1 more than the time series count so that there is whitespace on top and bottom for readability
					// - reverse the axis so zero is at the top
					// - don't include null time series since they won't be drawn
					this._max_tslimits_lefty.setMaxValue(0.0);
					this._max_tslimits_lefty.setMinValue(getTSListForAxes(includeLeftYAxis, includeRightYAxis, includeNulls).size() + 1);
				}
			}
			catch ( Exception e ) {
				// This typically throws an exception if the data are not of consistent units.
				if ( !this._is_reference_graph ) {
					Message.printWarning ( 1, routine, "Data are not compatible (different units?).  Cannot graph." );
					Message.printWarning ( 2, routine, e );
				}
			}
			if ( this._max_tslimits_lefty == null ) {
				// Typically due to a cancel of the graph due to incompatible units.
				// In this case get to here but just need to gracefully handle nulls until the graph can be closed in parent container code.
				return;
			}
			if (this._is_reference_graph && (this._reference_ts_index >= 0)) {
				// Reset the coordinates based only on the reference time series but use the full period for dates.
				List<TS> ref_tslist = new ArrayList<>(1);
				ref_tslist.add(this.__tslist.get(this._reference_ts_index));
				TSLimits reflimits = TSUtil.getDataLimits (	ref_tslist, this._start_date, this._end_date, "", false,this._ignoreLeftAxisUnits );
				computeDataLimits_CheckDisplayLimitProperties(ref_tslist, reflimits);
				this._max_tslimits_lefty.setMinValue ( reflimits.getMinValue() );
				this._max_tslimits_lefty.setMaxValue ( reflimits.getMaxValue() );
				reflimits = null;	// clean up
				ref_tslist = null;
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, this._gtype + "Reference graph max data limits are " + this._max_tslimits_lefty );
				}
			}
			else {
				// Not a reference graph.
	            if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, this._gtype + "Main graph max data limits are " + this._max_tslimits_lefty );
				}
				// If the properties are given, set the limits to the given properties,
	            // but only if they are outside the range of the data that was determined.
				//
				// TODO SAM 2006-09-28 Still need to evaluate how switching between Auto and hard limits can be handled better.
	            // TODO SAM 2017-04-24 think this is working now but see how things go in production first.
				String prop_value = this._tsproduct.getLayeredPropValue ( "LeftYAxisMax", subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymax = StringUtil.atod(prop_value);
					if ( !this._zoomKeepFullPeriodYLimits && (ymax > this._max_tslimits_lefty.getMaxValue()) ) {
						this._max_tslimits_lefty.setMaxValue(ymax);
					}
				}
				else if (prop_value != null && prop_value.equalsIgnoreCase("Auto")) {
	            	if ( this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL ) {
	            		int nreg = 0;
	            		if (this.__tslist != null) {
	            			nreg = this.__tslist.size() - 1;
	            		}

	            		List<TS> v = new ArrayList<>();
	            		TSRegression regressionData = null;
	            		double minValue = 0;
	            		double maxValue = 0;
	            		TSLimits tempLimits = null;
	            		for (int i = 0; i < nreg; i++) {
	            			if (!isTSEnabled(i + 1) ) {
	            				continue;
	            			}
	            			regressionData = this._regression_data.get(i);
	            			v.add(regressionData.getResidualTS());
	            			tempLimits = TSUtil.getDataLimits(v, this._max_start_date,_max_end_date, "", false, this._ignoreLeftAxisUnits );
	            			computeDataLimits_CheckDisplayLimitProperties(v, tempLimits);
	            			if (tempLimits.getMaxValue() > maxValue) {
	            				maxValue = tempLimits.getMaxValue();
	            			}
	            			if (tempLimits.getMinValue() < minValue) {
	            				minValue = tempLimits.getMinValue();
	            			}
	            		}

	            		this._max_tslimits_lefty.setMaxValue(maxValue);
	            		this._max_tslimits_lefty.setMinValue(minValue);
	            	}
	            	else {
	            		TSLimits tempLimits = TSUtil.getDataLimits(getTSListToRender(true,includeLeftYAxis,includeRightYAxis),
	            			this._max_start_date, this._max_end_date, "", false, this._ignoreLeftAxisUnits);
	            		computeDataLimits_CheckDisplayLimitProperties(getTSListToRender(true,includeLeftYAxis,includeRightYAxis), tempLimits);
	            		this._max_tslimits_lefty.setMaxValue(tempLimits.getMaxValue());
	            	}
	            	// TODO SAM 2006-09-28 Still need to evaluate how switching between Auto and hard limits can be handled better.
	            	// TODO SAM 2017-04-24 think this is working now but see how things go in production first.
				}
				prop_value = this._tsproduct.getLayeredPropValue ("LeftYAxisMin", subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymin = StringUtil.atod(prop_value);
					if ( !this._zoomKeepFullPeriodYLimits && (ymin < this._max_tslimits_lefty.getMinValue()) ) {
						this._max_tslimits_lefty.setMinValue(ymin);
					}
				}
			}
			// Set the initial limits to the maximum.
			this._tslimits_lefty = new TSLimits ( this._max_tslimits_lefty );
			// Initialize this here because this is what the reference graph uses throughout (it does not need nice labels).
			if ( this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
				boolean xlimits_found = false;
				boolean ylimits_found = false;
				TSRegression regressionData = null;
				int nregression = 0;
				if ( this._regression_data != null ) {
					nregression = this._regression_data.size();
				}
				double xmin = 0.0, ymin = 0.0, xmax = 1.0, ymax = 1.0;
				for ( int ir = 0; ir < nregression; ir++ ) {
					regressionData = this._regression_data.get(ir);
					if ( (regressionData != null) && !regressionData.isMonthlyAnalysis() ) {
						// One equation.
						if ( regressionData.isAnalyzed() ) {
							// Analysis was successful so full data are available.
							//
							// Get the limits from the regression data since some data may have been ignored.
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
						else if ( regressionData.getN1() == 1 ) {
							// Regression was not successful but if no line was requested, still plot the points,
							// which is a special case found in snow GUI.
							/* TODO - for now do regardless of whether the line	was requested.
							Message.printStatus(1, "SAMX", "No line but number of"+
							String prop_val = this._tsproduct.getLayeredPropValue ("RegressionLineEnabled",this.subproduct, ir,false);
							if ( (prop_val != null) && !prop_val.equalsIgnoreCase("true") ) {
								Message.printStatus(1,"SAMX", "No line but number of points is regressionData.getN1() );
							}
							*/
							// Really want the common limits, but that may not be available - need to do some work in TSRegression.
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
						// Monthly analysis.
						//
						// Get the limits from the regression data since some data may have been ignored.
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
					// Use the full limits.
					TS ts0 = this.__tslist.get(0);
					TSLimits limits0 = ts0.getDataLimits();
					xmin = limits0.getMinValue();
					xmax = limits0.getMaxValue();
				}

				if (!ylimits_found) {
					// Loop through the Y axis time series (hopefully this code is never executed).
					TS ts = null;
					TSLimits ylimits = null;
					for ( int its = 1; its <= nregression; its++ ) {
						ts = this.__tslist.get(its);
						if ( (ts == null) || !isTSEnabled(its)) {
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
				// Set the limits regardless.  Worst case they will be zero to one.
				this._data_lefty_limits = new GRLimits ( xmin, ymin, xmax, ymax );
			}
			else if ( this.__leftYAxisGraphType == TSGraphType.DURATION ) {
				// X limits are 0 to 100.  Y limits are based on the time series.
				this._data_lefty_limits = new GRLimits ( 0.0, 0.0, 100.0, this._tslimits_lefty.getMaxValue() );
			}
			else if ( (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL)
			    || (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) ) {
			    boolean residual = true;
				if (this.__leftYAxisGraphType==TSGraphType.PREDICTED_VALUE) {
					residual = false;
				}
				TSRegression regressionData = null;
				int nregression = 0;
				if (this._regression_data != null) {
					nregression = this._regression_data.size();
				}
				TSLimits tsLimits = null;
				double maxValue = 0;
				double minValue = 0;
				List<TS> tempV = null;
				for (int ir = 0; ir < nregression; ir++) {
					regressionData = this._regression_data.get(ir);
					tempV = new ArrayList<TS>();
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
						tsLimits = TSUtil.getDataLimits( tempV, this._max_start_date, this._max_end_date, "", false, this._ignoreLeftAxisUnits);
						computeDataLimits_CheckDisplayLimitProperties(tempV, tsLimits);
					}
					else {
						// Ignore null regression data.
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
				this._data_lefty_limits = new GRLimits ( this._start_date.toDouble(), minValue, this._end_date.toDouble(), maxValue);
			}
	        else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
	        	if ( (this.__tslist.size() == 1)
	        		&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
	        		// Single time series:
	        		// - for month and day interval:
	        		//   - X limits are 0 to 367 if daily, 1 to 13 if monthly (right side is at edge of next interval)
	        		//   - Y limits are based on the year of the period of the time series
	        		// - for hour and minute interval:
	        		//   - X limits are 0 to 24 hour
	        		//   - Y limits are days with most recent at the top
	        		// - for year interval, above check will cause multiple time series logic to be used
	        		int intervalBase = TimeInterval.UNKNOWN;
	        		TS ts = this.__tslist.get(0);
	        		if ( ts != null ) {
	        			intervalBase = ts.getDataIntervalBase();
	        		}
	        		// List from largest to smallest.
	        		if ( intervalBase == TimeInterval.MONTH ) {
	        			this._data_lefty_limits = new GRLimits (
	        				1.0,                                         // First month of the year.
	        				this._tslimits_lefty.getDate1().getYear(),   // First year (oldest).
	        				13.0,                                        // Last month of year +1 to allow for full month pixel to be drawn.
	        				this._tslimits_lefty.getDate2().getYear() + 1 );  // Last year (newest) + 1 to allow for full year pixel to be drawn.
	        		}
	        		else if ( intervalBase == TimeInterval.DAY ) {
	        			// TODO SAM 2013-07-20 Need to figure out how to handle leap year, for now always include.
	        			this._data_lefty_limits = new GRLimits (
	        				1.0,                                         // First day of the year.
	        				this._tslimits_lefty.getDate1().getYear(),   // First year (oldest).
	        				367.0,                                       // Last day of year + 1 to allow for full day pixel to be drawn.
	        				this._tslimits_lefty.getDate2().getYear() + 1 );  // Last year (newest) + 1 to allow for full year pixel to be drawn.
	        		}
	        		else if ( (intervalBase == TimeInterval.HOUR) || (intervalBase == TimeInterval.MINUTE) ) {
	        			int day1 = this._tslimits_lefty.getDate1().getAbsoluteDay();
	        			int day2 = this._tslimits_lefty.getDate2().getAbsoluteDay();
	        			// Can't use YYYYMMDD because axis label code results in invalid months and days.
	        			//int day1 = this._tslimits_lefty.getDate1().getYear()*10000
	        			//	+ this._tslimits_lefty.getDate1().getMonth()*100 +
	        			//	+ this._tslimits_lefty.getDate1().getDay();
	        			//int day2 = this._tslimits_lefty.getDate2().getYear()*10000
	        			//	+ this._tslimits_lefty.getDate2().getMonth()*100 +
	        			//	+ this._tslimits_lefty.getDate2().getDay();
	        			this._data_lefty_limits = new GRLimits (
	        				0.0,                                         // Hour zero.
	        				day1,                                        // First day (oldest).
	        				24.0,                                        // Midnight to allow for full hour pixel to be drawn.
	        				day2 + 1 );                                  // Last day (newest) + 1 to allow for full day pixel to be drawn.
	        		}
	        	}
	        	else if ( (this.__tslist.size() > 1)
	       			|| ((this.__tslist.size() == 1)
	       				&& (this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() == TimeInterval.YEAR)) ) {
	        		// Multiple time series (or single year interval time series):
	        		// - X limits are date/time limits
	        		// - Y limits are 0 (top) to number of time series (bottom)
	        		// - TODO smalers 2023-04-09 need to adjust start and end to accommodate dates and times to render pixel
	        		TS ts = this.__tslist.get(0);
	        		int intervalBase = ts.getDataIntervalBase();
    	    		int intervalMult = ts.getDataIntervalMult();
        			boolean useTime = true;
        			if ( (intervalBase == TimeInterval.DAY)
        				|| (intervalBase == TimeInterval.MONTH)
        				|| (intervalBase == TimeInterval.YEAR) ) {
        				// Not using time.
        				useTime = false;
        			}
        			DateTime rasterStart = new DateTime(this._start_date);
        			DateTime rasterEnd = new DateTime(this._end_date);
        			// Offset the start and end depending on whether date or time is used.
        			if ( useTime ) {
        				// Times are used:
        				// - time series date/time will be end of interval
        				rasterStart.addInterval(intervalBase, -intervalMult);
        			}
        			else {
        				// Dates are used:
        				// - time series date/time will be start of interval
        				rasterEnd.addInterval(intervalBase, intervalMult);
        			}
        			this._data_lefty_limits = new GRLimits (
        				rasterStart.toDouble(),  // Starting date/time (left x).
        				this.__tslist.size(),    // Number of time series (bottom y).
        				rasterEnd.toDouble(),    // Ending date/time (right x).
        				0 );                     // Zero (top y).
	        	}
	            Message.printStatus(2,routine,"Data limits for raster graph: " + this._data_lefty_limits);
	        }
			else {
				// Typical time series graph with date/time on the X axis.
				this._data_lefty_limits = new GRLimits (this._start_date.toDouble(), this._tslimits_lefty.getMinValue(),
					this._end_date.toDouble(),this._tslimits_lefty.getMaxValue() );
			}

			if ( this._data_lefty_limits != null ) {
				if (computeFromMaxPeriod) {
					this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
				}
				else {
					if (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) {
					    this._max_lefty_data_limits = new GRLimits(this._data_lefty_limits);
					}
					else {
	    				GRLimits tempLimits = new GRLimits( this._max_start_date.toDouble(), this._tslimits_lefty.getMinValue(),
	    				    this._max_end_date.toDouble(),this._tslimits_lefty.getMaxValue() );
	    				this._max_lefty_data_limits = new GRLimits(tempLimits);
					}
				}

				if (Message.isDebugOn) {
					Message.printDebug ( 1, routine, this._gtype	+
					    "Initial computed _max_lefty_data_limits (including limit properties) are " + this._max_lefty_data_limits );
				}
			}
		}

		// Right y-axis.

		if ( !this._is_reference_graph && (enabledRightYAxisTSList.size() > 0) ) {
			// TODO SAM 2016-10-24 Need to do this only for allowed simple graph types.
			//if ( graph type allowed ) {
			try {
				if (this._ignoreRightAxisUnits) {
					// Can ignore units.
					this._max_tslimits_righty = TSUtil.getDataLimits( enabledRightYAxisTSList, this._start_date, this._end_date, "", false, this._ignoreRightAxisUnits);
					computeDataLimits_CheckDisplayLimitProperties(enabledRightYAxisTSList, this._max_tslimits_righty);
					Message.printStatus(2, routine, "Setting _max_tslimits_righty...units are ignored");
				}
				else {
	                // Need to have consistent units.  For now require them to be the same because don't
					// want to do units conversions on the fly or alter the original data.
					//
					// TODO - need to add on the fly conversion of units (slower but changing original data is.
					// a worse alternative).
				    Message.printStatus(2, routine, "Calling areUnitsCompatible for right y-axis, have " + enabledRightYAxisTSList.size() + " time series");
					if (!TSUtil.areUnitsCompatible(enabledRightYAxisTSList, true)) {
					   	Message.printStatus(2, routine, "Time series to render for right y-axis have incompatible units.");
						if ( this._ignoreRightAxisUnits ) {
							this._max_tslimits_righty = TSUtil.getDataLimits( enabledRightYAxisTSList,
								this._start_date, this._end_date, "", false, this._ignoreRightAxisUnits);
							computeDataLimits_CheckDisplayLimitProperties(enabledRightYAxisTSList, this._max_tslimits_righty);

							Message.printStatus(2, routine, "Setting _max_tslimits_righty...units are ignored because property says to do so");
						}
						else {
							// Let the user interactively indicate whether to continue.
							// If running in batch mode, there may not be a parent.
							int x = ResponseJDialog.YES;
							// The problem is that the parent has not yet been centered itself,
							// so need to get the parent UI component.
							Object uiComponentObject = this._display_props.getContents( "TSViewParentUIComponent" );
							if ( (uiComponentObject != null) && (uiComponentObject instanceof Component)
								&& ((uiComponentObject instanceof JFrame) || (uiComponentObject instanceof JDialog))) {
								// Can center the dialog on parent JFrame or JDialog.
								if ( uiComponentObject instanceof JDialog ) {
									x = new ResponseJDialog( (JDialog)uiComponentObject, "Incompatible Units",
									"The right y-axis data units are incompatible.\n"
									+ "The graph can be split by editing its properties (right-click on graph when shown).\n\n"
									+ "Continue graphing with incompatible units?",
									ResponseJDialog.YES|ResponseJDialog.NO ).response();
								}
								else {
									x = new ResponseJDialog( (JFrame)uiComponentObject, "Incompatible Units",
									"The right y-axis data units are incompatible.\n"
									+ "The graph can be split by editing its properties (right-click on graph when shown).\n\n"
									+ "Continue graphing with incompatible units?",
									ResponseJDialog.YES|ResponseJDialog.NO ).response();
								}
							}
							if ( this._dev.getJFrame() != null ){
								x = new ResponseJDialog( this._dev.getJFrame(), "Continue Graph?",
								"The right y-axis data units are incompatible\n" + "Continue graphing anyway?",
								ResponseJDialog.YES|ResponseJDialog.NO ).response();
							}
							else {
	                            // No frame so default to ignore units.
								x = ResponseJDialog.YES;
							}
							if ( x == ResponseJDialog.NO ) {
								// Set this so that code that uses this component can check to see if the component needs to close itself.
								this._dev.needToClose( true);
							}
							else {
								this._ignoreRightAxisUnits = true;
								this._max_tslimits_righty = TSUtil.getDataLimits( enabledRightYAxisTSList, this._start_date,
								    this._end_date, "", false, this._ignoreRightAxisUnits);
								computeDataLimits_CheckDisplayLimitProperties(enabledRightYAxisTSList, this._max_tslimits_righty);
								Message.printStatus(2, routine, "Setting _max_tslimits_righty...units are ignored because user said so or not visible so do so");
							}
						}
					}
					else {
						// Units are compatible so compute the maximum.
						this._max_tslimits_righty = TSUtil.getDataLimits( enabledRightYAxisTSList, this._start_date,
							this._end_date, "", false, this._ignoreRightAxisUnits);
						computeDataLimits_CheckDisplayLimitProperties(enabledRightYAxisTSList, this._max_tslimits_righty);
					}
				}
				// Property overrides for right y-axis limits.
				String prop_value = this._tsproduct.getLayeredPropValue ( "RightYAxisMax", this.subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymax = Double.parseDouble(prop_value);
					if ( !this._zoomKeepFullPeriodYLimits && (ymax > this._max_tslimits_righty.getMaxValue()) ) {
						this._max_tslimits_righty.setMaxValue(ymax);
					}
				}
				prop_value = this._tsproduct.getLayeredPropValue ("RightYAxisMin", this.subproduct, -1, false);
				if ( (prop_value != null) && StringUtil.isDouble(prop_value) ) {
					double ymin = Double.parseDouble(prop_value);
					if (!this._zoomKeepFullPeriodYLimits && (ymin < this._max_tslimits_righty.getMinValue()) ) {
						this._max_tslimits_righty.setMinValue(ymin);
					}
				}
				// Make sure that the.
				this._tslimits_righty = new TSLimits ( this._max_tslimits_righty );
			}
			catch ( Exception e ) {
				// This typically throws an exception if the data are not of consistent units.
				Message.printWarning ( 1, routine, "Data are not compatible (different units?).  Cannot graph." );
				Message.printWarning ( 2, routine, e );
			}

			if ( this._tslimits_righty != null ) {
				// Had data in the time series that could be used to compute data limits for drawing.
				this._data_righty_limits = new GRLimits (this._start_date.toDouble(), this._tslimits_righty.getMinValue(),
					this._end_date.toDouble(),this._tslimits_righty.getMaxValue() );
			}

			if ( this._data_righty_limits != null ) {
				if (computeFromMaxPeriod) {
					this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
				}
				else {
					if (this.__rightYAxisGraphType == TSGraphType.XY_SCATTER) {
					    this._max_righty_data_limits = new GRLimits(this._data_righty_limits);
					}
					else {
						if ( this._tslimits_righty != null ) {
		    				GRLimits tempLimits = new GRLimits( this._max_start_date.toDouble(), this._tslimits_righty.getMinValue(),
		    				    this._max_end_date.toDouble(),this._tslimits_righty.getMaxValue() );
		    				this._max_righty_data_limits = new GRLimits(tempLimits);
						}
					}
				}

				if (Message.isDebugOn) {
					Message.printDebug ( 1, routine, this._gtype	+
					    "Initial computed _max_righty_data_limits (including limit properties) are " + this._max_righty_data_limits );
				}
			}
		}
	}
	catch (Exception e) {
		Message.printWarning(3, routine, this._gtype + "Error getting dates for plot.");
		Message.printWarning(3, this._gtype + "TSGraph", e);
		this._dev.needToClose(true);
	}
}

/**
 * Check the data limits using properties.
 * Check each time series in the list for properties DataValueDisplayMin and DataValueDisplayMax.
 * If the existing limits, which will have been computed from data, are outside of the limits,
 * reset the computed limits to those bounded by the properties.
 * Any errors in processing will result in the properties not being used.
 * @param tslist list of time series to process.
 * @param limits the limits of the graph based on time series.
 */
private void computeDataLimits_CheckDisplayLimitProperties(List<TS> tslist, TSLimits limits ) {
	//Message.printWarning(2,"","Before checking display limit properties, tslimits="+limits);
	for ( TS ts : tslist ) {
		if ( ts == null ) {
			// Null time series are possible, such stacked graph with line on top.
			continue;
		}
		Double propMinValue = null;
		Double propMaxValue = null;
		Object propMinValueO = ts.getProperty("DataValueDisplayMin");
		if ( propMinValueO != null ) {
			// The property can be a string or a Double.
			if ( propMinValueO instanceof Double ) {
				propMinValue = (Double)propMinValueO;
			}
			else if ( propMinValueO instanceof String ) {
				try {
					propMinValue = Double.parseDouble((String)propMinValueO);
				}
				catch ( NumberFormatException e ) {
					Message.printWarning(3, "", "Time series \"" + ts.getIdentifierString() +
						"\" property DataValueDisplayMin \"" + propMinValueO + "\" is invalid number.");
				}
			}
		}
		Object propMaxValueO = ts.getProperty("DataValueDisplayMax");
		if ( propMaxValueO != null ) {
			// The property can be a string or a Double.
			if ( propMaxValueO instanceof Double ) {
				propMaxValue = (Double)propMaxValueO;
			}
			else if ( propMaxValueO instanceof String ) {
				try {
					propMaxValue = Double.parseDouble((String)propMaxValueO);
				}
				catch ( NumberFormatException e ) {
					Message.printWarning(3, "", "Time series \"" + ts.getIdentifierString() +
						"\" property DataValueDisplayMax \"" + propMaxValueO + "\" is invalid number.");
				}
			}
		}
		// If have the display limit values, set the limits from the properties:
		// - ensure that the max and min values are not the same
		if ( (propMinValue != null) && (propMaxValue != null) &&
			!propMinValue.equals(propMaxValue) && (propMinValue < propMaxValue)) {
			// Values are not the same so allow setting each individually.
			if ( propMinValue != null ) {
				// Check.
				if ( limits.areLimitsFound() ) {
					// Limits are found so can check.
					if ( limits.getMinValue() < propMinValue ) {
						// The value computed from the time series is out of range.
						limits.setMinValue(propMinValue);
						limits.setMinValueDate(null);
					}
				}
			}
			if ( propMaxValue != null ) {
				// Check.
				if ( limits.areLimitsFound() ) {
					// Limits are found so can check.
					if ( limits.getMaxValue() > propMaxValue ) {
						// The value computed from the time series is out of range.
						limits.setMaxValue(propMaxValue);
						limits.setMaxValueDate(null);
					}
				}
			}
		}
	}
	//Message.printWarning(2,"","After checking display limit properties, tslimits="+limits);
}

/**
Compute the x-axis and y-axis labels given the current zoomed data.
Call this after the data limits have initially been set.
The label values are computed based on the drawing area size and the axis font to make sure that labels do not overlap.
This resets _datalim_lefty_graph and _datalim_righty_graph to be nicer bounding limits.
@param limitsLeftYAxis For data that is being used
(generally the max or current limits - whatever the graph is supposed to display).
<b>This is time series data so for scatter plots, etc., it does not contain all that is needed.</b>
*/
private void computeLabels ( TSLimits limitsLeftYAxis, TSLimits limitsRightYAxis ) {
    String routine = getClass().getSimpleName() + ".computeLabels";
	String propValue = null;

	// Left y-axis.

	if ( (this._da_lefty_graph == null) || (limitsLeftYAxis == null) ) {
		// Have not initialized the drawing areas yet or bad graph data.
		// TODO JTS otherwise exceptions thrown when drawing an empty graph.
		this._ylabels_lefty = new double[1];
		this._ylabels_lefty[0] = 0;
		this.__drawLeftyLabels = false;
		return;
	}
	else {
		this.__drawLeftyLabels = true;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Computing left y-axis labels using TS limits: " + limitsLeftYAxis );
	}

	// Right y-axis.

	if ( (this._da_righty_graph == null) || (limitsLeftYAxis == null) ) {
		// Have not initialized the drawing areas yet or bad graph data.
		// TODO JTS otherwise exceptions thrown when drawing an empty graph.
		this._ylabels_righty = new double[1];
		this._ylabels_righty[0] = 0;
		this.__drawRightyLabels = false;
		return;
	}
	else {
		propValue = this._tsproduct.getLayeredPropValue( "RightYAxisGraphType", this.subproduct, -1, false);
		if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
			this.__drawRightyLabels = true;
		}
		else {
			this.__drawRightyLabels = false;
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Computing right y-axis labels using TS limits: " + limitsLeftYAxis );
	}

	boolean log_y_lefty = false;
	boolean log_xy_scatter = false;
	propValue = this._tsproduct.getLayeredPropValue(	"LeftYAxisType", this.subproduct, -1, false);
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_lefty = true;
	}

	propValue = this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_lefty = false;
		log_xy_scatter = true;
	}

	// Now get recompute the limits to be nice.  First do the Y axis.
	// The maximum number of labels is based on the font height and the drawing area height.
	// However, in most cases, want at least a spacing of 3 times the font height,
	// unless this results in less than 3 labels.
	double height, width;
	// Format a label based on the font for the Y axis.

	String fontname = this._tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", this.subproduct, -1, false );
	String fontsize = this._tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", this.subproduct, -1, false );
	String fontstyle = this._tsproduct.getLayeredPropValue (	"LeftYAxisLabelFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_lefty_label, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRLimits label_extents = GRDrawingAreaUtil.getTextExtents( this._da_lefty_label, "astring", GRUnits.DEVICE );
	height = label_extents.getHeight();
	width = label_extents.getWidth();
	int minlabels = (int)(this._drawlim_lefty_graph.getHeight()/(height*6.0));
	if ( minlabels < 3 ) {
		minlabels = 3;
	}
	int maxlabels = (int)(this._drawlim_lefty_graph.getHeight()/(height*2.0));
	if ( maxlabels < minlabels ) {
		maxlabels = minlabels*2;
	}

	// TODO (JTS - 2004-03-03)
	// logic for determining max and min number of labels is screwy when creating new graphs sometimes.
	// Puts fewer labels in than look like should be in there.

	if (log_y_lefty) {
		if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (this._regression_data != null) ) {
			// Old used data from the regression.
			//_ylabels = GRAxis.findLogLabels (this._regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data.
			this._ylabels_lefty = GRAxis.findLogLabels (this._data_lefty_limits.getMinY(),this._data_lefty_limits.getMaxY() );
		}
		else {
		    this._ylabels_lefty = GRAxis.findLogLabels ( limitsLeftYAxis.getMinValue(),	limitsLeftYAxis.getMaxValue() );
		}
	}
	else if (log_xy_scatter) {
		if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (this._regression_data != null) ) {
			// Old used data from the regression.
			//_ylabels = GRAxis.findLogLabels (this._regression_data.getMin2(),_regression_data.getMax2() );
			// New consider all regression data.
			this._ylabels_lefty = GRAxis.findLogLabels ( this._data_lefty_limits.getMinY(), this._data_lefty_limits.getMaxY() );
		}
		else {
		    this._ylabels_lefty = GRAxis.findLogLabels ( limitsLeftYAxis.getMinValue(),	limitsLeftYAxis.getMaxValue() );
		}
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.PERIOD ) {
		// Period of record graph:
		// - uses a reversed axis with 0 at the top and tslist size + 1 at the bottom
		// - time series are shown in the middle so the number of labels matches the time series count
		// - Y-labels are whole numbers matching the time series count 1+
    	// - don't include null time series since they won't be included in output
		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = false;
		boolean includeNulls = false;
		List<TS> enabledTSList = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		this._ylabels_lefty = new double[enabledTSList.size()];
		for ( int i = 0; i < enabledTSList.size(); i++ ) {
			this._ylabels_lefty[i] = i + 1;
		}
	}
    else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
    	// Raster graph labels depend on whether a single time series or multiple time series are visualized:
    	// - don't include null time series since just computing limits
        boolean includeLeftYAxis = true;
        boolean includeRightYAxis = false;
        boolean includeNulls = false;
        List<TS> tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
        int nts = tslist.size();
        if ( nts == 0 ) {
        	// No time series to process.
            return;
        }
        else if ( (nts == 1)
       		&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
        	// Single time series:
        	// - Y-axis is years
        	// - Y-labels are whole numbers integer years from data period.
        	TS intervalTs = TSUtil.getTSThatHasInterval(tslist);
        	int intervalBase = TimeInterval.UNKNOWN;
        	if ( intervalTs != null ) {
        		intervalBase = intervalTs.getDataIntervalBase();
        	}
       		// Y axis is the year.
       		while ( minlabels >= 3 ) {
       			if ( this._data_lefty_limits == null ) {
       				Message.printWarning(3,routine,
       					"Null left y-axis limits computing labels for for raster graph - unsupported time series interval?");
       				break;
       			}
       			else {
       				// Compute "nice" labels (ticks) based on the Y-axis extremes.
       				if ( (intervalBase == TimeInterval.MONTH) || (intervalBase == TimeInterval.DAY) ) {
	            		this._ylabels_lefty = GRAxis.findNLabels ( this._data_lefty_limits.getMinY(), this._data_lefty_limits.getMaxY(),
	            			true, minlabels, maxlabels );
       				}
       				else if ( (intervalBase == TimeInterval.HOUR) || (intervalBase == TimeInterval.MINUTE) ) {
       					// Allow maximum number of labels to.
       					maxlabels = (int)(this._drawlim_lefty_graph.getHeight()/(height));
        				this._ylabels_lefty = findYAxisDateLabels (
       						this._data_lefty_limits.getMinY(), this._data_lefty_limits.getMaxY(),
        					true, minlabels, maxlabels, DateTime.PRECISION_DAY );
       				}
	           		if ( this._ylabels_lefty != null ) {
	               		break;
	           		}
	           		--minlabels;
        		}
        	}
        }
        else if ( (nts > 1)
       		|| ((nts == 1) && (this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() == TimeInterval.YEAR)) ) {
        	// Multiple time series or single year interval time series:
        	// - Y-axis is similar to period of record graph
        	// - labels will be between ticks since raster is a rectangular pixel
        	// - Y-axis range is larger than label position range
        	// - for now always label for each time series
        	// - positions are at full ticks (labels will be drawn at mid-tick)
        	this._ylabels_lefty = new double[tslist.size() + 1];
        	for ( int i = 0; i <= tslist.size(); i++ ) {
        		this._ylabels_lefty[i] = i;
        	}
        }
    }
	else {
	    // Linear.  Minimum and maximum number of labels as computed above.
		if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (this._regression_data != null) ) {
			while ( minlabels >= 3 ) {
				// Make sure the max values properly account for the other axis.
				// Old...
				//_ylabels = GRAxis.findNLabels ( this._regression_data.getMin2(), this._regression_data.getMax2(),
					//false, minlabels, maxlabels );
				this._ylabels_lefty = GRAxis.findNLabels ( this._data_lefty_limits.getMinY(), this._data_lefty_limits.getMaxY(),
					false, minlabels, maxlabels );
				if ( this._ylabels_lefty != null ) {
					break;
				}
				--minlabels;
			}
		}
		else {
			// All other graph types.
		    while ( minlabels >= 3 ) {
				this._ylabels_lefty = GRAxis.findNLabels ( limitsLeftYAxis.getMinValue(),limitsLeftYAxis.getMaxValue(), false, minlabels, maxlabels );
				if ( this._ylabels_lefty != null ) {
					break;
				}
				--minlabels;
			}
		}
	}

	if ( (this._ylabels_lefty == null) || (this._ylabels_lefty.length == 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Unable to find left y-axis labels using " +
			minlabels + " to " + maxlabels + " labels.  Using end-point data values." );
		}
		this._ylabels_lefty = new double [2];
		if ( this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			this._ylabels_lefty[0] = this._data_lefty_limits.getMinY();
			this._ylabels_lefty[1] = this._data_lefty_limits.getMaxY();
			this._data_lefty_limits = new GRLimits ( this._max_lefty_data_limits.getMinX(), this._ylabels_lefty[0],
				this._max_lefty_data_limits.getMaxX(), this._ylabels_lefty[1] );
		}
		else {
		    if ( log_y_lefty ) {
		        // No data points so put in .1 to 100.
		        this._ylabels_lefty = new double [4];
		        this._ylabels_lefty[0] = .1;
                this._ylabels_lefty[1] = 1.0;
                this._ylabels_lefty[2] = 10.0;
                this._ylabels_lefty[3] = 100.0;
		    }
		    else {
    		    this._ylabels_lefty[0] = limitsLeftYAxis.getMinValue();
    			this._ylabels_lefty[1] = limitsLeftYAxis.getMaxValue();
	         }
    		this._data_lefty_limits = new GRLimits ( this._start_date.toDouble(), this._ylabels_lefty[0], this._end_date.toDouble(), this._ylabels_lefty[1] );
		}
	}
	else {
		// Have left axis Y labels:
		// - set the drawing area limits to align with the labels
		// - drawing the labels may have special logic
		// - don't include null time series since they won't be drawn
		if (this.__leftYAxisGraphType == TSGraphType.PERIOD) {
			// Period of record graph:
			// - reverse the y-axis
			// - use range 0 (top y) to time series size +1 (bottom y) to allow whitespace around the time series horizontal bars
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			boolean includeNulls = false;
			this._data_lefty_limits = new GRLimits (
				this._start_date.toDouble(),  // Left: period start.
				(getTSListForAxes(includeLeftYAxis, includeRightYAxis, includeNulls).size() + 1), // Bottom: time series count + 1 to allow for whitespace.
				this._end_date.toDouble(), // Right: period end.
				0.0); // Top:  zero to allow for whitespace
		}
		else if ( (this.__leftYAxisGraphType == TSGraphType.RASTER) &&
			((this.__tslist.size() > 1)
       		|| ((this.__tslist.size() == 1)
       			&& (this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() == TimeInterval.YEAR))) ) {
			// Raster graph for multiple time series or single year-interval time series:
			// - reverse the y-axis
			// - use range 0 (top y) to time series size +1 (bottom y) to allow raster pixel boxes to be drawn
			// - don't include null time series since they won't be drawn
			boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			boolean includeNulls = false;
			this._data_lefty_limits = new GRLimits (
				this._start_date.toDouble(),  // Left: period start.
				getTSListForAxes(includeLeftYAxis,includeRightYAxis, includeNulls).size(), // Bottom: time series count to allow for pixel rendering.
				this._end_date.toDouble(), // Right: period end.
				0.0); // Top:  zero to allow for pixel rendering.
		}
		else if (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			// XY scatter graph:
			// - data values (not) time are on both axes
			this._data_lefty_limits = new GRLimits ( this._max_lefty_data_limits.getMinX(), this._ylabels_lefty[0],
				this._max_lefty_data_limits.getMaxX(), this._ylabels_lefty[this._ylabels_lefty.length - 1]);
		}
		// TODO smalers 2023-04-09 what about Duration graph?
		else {
			// Graphs with time on the X-axis:
			// - use start and end for x-axis range
			// - use label position for y-axis range
			this._data_lefty_limits = new GRLimits ( this._start_date.toDouble(), this._ylabels_lefty[0],
				this._end_date.toDouble(), this._ylabels_lefty[this._ylabels_lefty.length - 1]);
		}

		if (Message.isDebugOn) {
			Message.printDebug(1, routine, this._gtype + "Found " + this._ylabels_lefty.length
				+ " labels requesting " + minlabels + " to " + maxlabels);
		}
	}
	if ( !this._is_reference_graph ) {
	    GRLimits dataLimits = new GRLimits(this._data_lefty_limits);
	    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
	        dataLimits.reverseY();
	    }
	    if ( Message.isDebugOn ) {
	        Message.printDebug ( 1, routine, this._gtype + "Y-axis labels (LeftYAxisDirection=" + this.__leftyDirection +
	            ") resulted in data limits " + dataLimits );
	    }
	    this._da_lefty_graph.setDataLimits ( dataLimits );
	}
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < this._ylabels_lefty.length; i++ ) {
			Message.printDebug ( 1, routine, this._gtype + "_ylabel_lefty[" + i + "]=" + this._ylabels_lefty[i] );
		}
	}

	// Right y-axis labels, only if right y-axis is enabled.

	if ( this._drawlim_righty_graph != null ) {

	boolean log_y_righty = false;
	propValue = this._tsproduct.getLayeredPropValue(	"RightYAxisType", this.subproduct, -1, false);
	if ((propValue != null) && propValue.equalsIgnoreCase("Log")) {
		log_y_righty = true;
	}

	// Now get recompute the limits to be nice.  First do the Y axis.
	// The maximum number of labels is based on the font height and the drawing area height.
	// However, in most cases, want at least a spacing of 3 times the font height, unless this results in less than 3 labels.
	// Format a label based on the font for the Y axis.

	fontname = this._tsproduct.getLayeredPropValue ( "RightYAxisLabelFontName", this.subproduct, -1, false );
	fontsize = this._tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", this.subproduct, -1, false );
	fontstyle = this._tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_righty_label, fontname, fontstyle, StringUtil.atod(fontsize) );
	label_extents = GRDrawingAreaUtil.getTextExtents( this._da_righty_label, "astring", GRUnits.DEVICE );
	height = label_extents.getHeight();
	width = label_extents.getWidth();
	minlabels = (int)(this._drawlim_righty_graph.getHeight()/(height*6.0));
	if ( minlabels < 3 ) {
		minlabels = 3;
	}
	maxlabels = (int)(this._drawlim_righty_graph.getHeight()/(height*2.0));
	if ( maxlabels < minlabels ) {
		maxlabels = minlabels*2;
	}

	// TODO (JTS - 2004-03-03)
	// logic for determining max and min number of labels is screwy when creating new graphs sometimes.
	// Puts fewer labels in than look like should be in there.

	if (log_y_righty) {
		this._ylabels_righty = GRAxis.findLogLabels ( limitsRightYAxis.getMinValue(), limitsRightYAxis.getMaxValue() );
	}
	else if ( this.__rightYAxisGraphType == TSGraphType.PERIOD ) {
		// Y-labels are whole numbers:
		// - don't include null time series since they won't be drawn
		// TODO smalers 2025-03-21 figure out why not used.
		//boolean includeLeftYAxis = false;
		boolean includeRightYAxis = true;
		boolean includeNulls = false;
		List<TS> enabledTSList = getTSListForAxes ( includeRightYAxis, includeRightYAxis, includeNulls );
		this._ylabels_righty = new double[enabledTSList.size()];
		for ( int i = 0; i < enabledTSList.size(); i++ ) {
			this._ylabels_righty[i] = i + 1;
		}
	}
	else {
	    // Linear.  Minimum and maximum number of labels as computed above.
		if ( limitsRightYAxis != null ) {
		    while ( minlabels >= 3 ) {
				this._ylabels_righty = GRAxis.findNLabels ( limitsRightYAxis.getMinValue(),limitsRightYAxis.getMaxValue(), false, minlabels, maxlabels );
				if ( this._ylabels_righty != null ) {
					break;
				}
				--minlabels;
			}
		}
	}

	if ( (this._ylabels_righty == null) || (this._ylabels_righty.length == 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Unable to find right y-axis labels using " +
			minlabels + " to " + maxlabels + " labels.  Using end-point data values." );
		}
		this._ylabels_righty = new double [2];
	    if ( log_y_righty ) {
	        // No data points so put in .1 to 100.
	        this._ylabels_righty = new double [4];
	        this._ylabels_righty[0] = .1;
            this._ylabels_righty[1] = 1.0;
            this._ylabels_righty[2] = 10.0;
            this._ylabels_righty[3] = 100.0;
	    }
	    else {
	    	if ( limitsRightYAxis == null ) {
	    		this._ylabels_righty[0] = 0.0;
	    		this._ylabels_righty[1] = 1.0;
	    	}
	    	else {
			    this._ylabels_righty[0] = limitsRightYAxis.getMinValue();
				this._ylabels_righty[1] = limitsRightYAxis.getMaxValue();
	    	}
         }
		this._data_righty_limits = new GRLimits ( this._start_date.toDouble(), this._ylabels_righty[0], this._end_date.toDouble(), this._ylabels_righty[1] );
	}
	else {
		if (this.__rightYAxisGraphType == TSGraphType.PERIOD) {
			// TODO smalers 2025-03-21 figure out why not used.
			//boolean includeLeftYAxis = true;
			boolean includeRightYAxis = false;
			// Do not include null time series since they won't be drawn.
			boolean includeNulls = false;
			this._data_righty_limits = new GRLimits (
				this._start_date.toDouble(), (getTSListForAxes(includeRightYAxis, includeRightYAxis, includeNulls).size() + 1),
				this._end_date.toDouble(), 0.0);
		}
		else {
			this._data_righty_limits = new GRLimits ( this._start_date.toDouble(), this._ylabels_righty[0],
				this._end_date.toDouble(), this._ylabels_righty[this._ylabels_righty.length - 1]);
		}

		if (Message.isDebugOn) {
			Message.printDebug(1, routine, this._gtype + "Found " + this._ylabels_righty.length
				+ " labels requesting " + minlabels + " to " + maxlabels);
		}
	}
	if ( !this._is_reference_graph ) {
	    GRLimits dataLimits = new GRLimits(this._data_righty_limits);
	    if ( this.__rightyDirection == GRAxisDirectionType.REVERSE ) {
	        dataLimits.reverseY();
	    }
	    if ( Message.isDebugOn ) {
	        Message.printDebug ( 1, routine, this._gtype + "Y-axis labels (RightYAxisDirection=" + this.__rightyDirection +
	            ") resulted in data limits " + dataLimits );
	    }
	    this._da_righty_graph.setDataLimits ( dataLimits );
	}
	if ( Message.isDebugOn ) {
		for ( int i = 0; i < this._ylabels_righty.length; i++ ) {
			Message.printDebug ( 1, routine, this._gtype + "_ylabel_righty[" + i + "]=" + this._ylabels_righty[i] );
		}
	}
	}

	// X axis labels, shared between left and right y-axis.
	//
	// If normal plot, based on the dates for the current zoom.
	// If a scatter plot, based on data limits.
	// If a duration plot, based on 0 - 100 percent.
	// If a raster plot, based on days or months in year (for day or month interval).

	fontname = this._tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontName", this.subproduct, -1, false );
	fontsize = this._tsproduct.getLayeredPropValue (	"BottomXAxisLabelFontSize", this.subproduct, -1, false );
	fontstyle = this._tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_bottomx_label, fontname, fontstyle,	StringUtil.atod(fontsize) );

	// More complex plots only can display using left y-axis graph.
	if ( this.__leftYAxisGraphType == TSGraphType.DURATION ) {
		// Limits are 0 to 100.0..
		String maxstring = StringUtil.formatString(	(double)100.0, "%.0f");
		label_extents = GRDrawingAreaUtil.getTextExtents( this._da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(this._drawlim_lefty_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(this._drawlim_lefty_graph.getHeight()/(width*1.5));
		if ( maxlabels < minlabels ) {
			maxlabels = minlabels*2;
		}
		while ( minlabels >= 3 ) {
			this._xlabels = GRAxis.findNLabels ( 0.0, 100.0, false, minlabels, maxlabels );
			if ( this._xlabels != null ) {
				break;
			}
			--minlabels;
		}
		if ( this._xlabels == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, this._gtype + "Unable to find X labels using " +
				minlabels + " to " + maxlabels + " labels.  Using data values." );
			}
			this._xlabels = new double [2];
			this._xlabels[0] = this._data_lefty_limits.getMinX();
			this._xlabels[1] = this._data_lefty_limits.getMaxX();
		}
		this._data_lefty_limits = new GRLimits ( this._xlabels[0], this._ylabels_lefty[0],
			this._xlabels[this._xlabels.length - 1], this._ylabels_lefty[this._ylabels_lefty.length - 1] );
		if ( !this._is_reference_graph ) {
		    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
		        GRLimits dataLimits = new GRLimits(this._data_lefty_limits);
		        this._da_lefty_graph.setDataLimits ( dataLimits.reverseY() );
		    }
		    else {
		        this._da_lefty_graph.setDataLimits ( this._data_lefty_limits );
		    }
		}
		return;
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
		// Raster (heat map) graph:
		// - if a single time series, both axes are time
        // - if multiple time series, X-axis is time and Y-axis is time series ordinal position (first at top)
		// - don't include null time series since they won't be drawn
        boolean includeLeftYAxis = true;
        boolean includeRightYAxis = false;
        boolean includeNulls = false;
        List<TS> tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
        int nts = tslist.size();
        if ( nts == 0 ) {
        	// No time series to process.
            return;
        }
        else if ( (nts == 1)
   			&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
        	// Single time series but not year interval:
        	// - X-axis is short time period (e.g., year for day or month interval)
        	// - Y-axis is historical years
        	TS ts = tslist.get(0);
        	int intervalBase = ts.getDataIntervalBase();
        	if ( intervalBase == TimeInterval.MONTH ) {
        		this._xlabels = new double[13];
            	for ( int ix = 1; ix <= 13; ix++ ) {
                	this._xlabels[ix - 1] = ix;
            	}
        	}
        	else if ( intervalBase == TimeInterval.DAY ) {
        		this._xlabels = new double[13];
            	DateTime d = new DateTime();
            	d.setYear(2000); // A leap year.
            	d.setDay(1);
            	for ( int ix = 1; ix <= 12; ix++ ) {
                	d.setMonth(ix);
                	this._xlabels[ix - 1] = TimeUtil.dayOfYear(d);
            	}
            	// Add end value for last day in year.
            	d.setDay(TimeUtil.numDaysInMonth(d));
            	this._xlabels[_xlabels.length - 1] = TimeUtil.dayOfYear(d);
        	}
        	else if ( (intervalBase == TimeInterval.HOUR) || (intervalBase == TimeInterval.MINUTE) ) {
        		// Number of labels is constant for hours:
        		// - always full 24 hours
        		// - graph can get pretty narrow
        		// - hour 0 on the left (midnight of previous day = start of current day)
        		// - hour 24 (midnight) on the right (also labeled as 0 when rendering)
        		this._xlabels = new double[25];
            	for ( int ix = 0; ix <= 24; ix++ ) {
                	this._xlabels[ix] = ix;
            	}
        	}
        	/* TODO SAM 2013-07-21 Decide if this is needed given special handling of labels.
        	String maxstring = "MMM"; // 3-letter month abbreviation
        	label_extents = GRDrawingAreaUtil.getTextExtents( this._da_lefty_label, maxstring, GRUnits.DEVICE );
        	width = label_extents.getWidth();
        	minlabels = (int)(this._drawlim_graph.getWidth()/(width*3.0));
        	if ( minlabels < 3 ) {
            	minlabels = 3;
        	}
        	maxlabels = (int)(this._drawlim_graph.getHeight()/(width*1.5));
        	if ( maxlabels < minlabels ) {
            	maxlabels = minlabels*2;
        	}
        	while ( minlabels >= 3 ) {
            	this._xlabels = GRAxis.findNLabels ( 0.0, 100.0, false, minlabels, maxlabels );
            	if ( this._xlabels != null ) {
                	break;
            	}
            	--minlabels;
        	}
        	if ( this._xlabels == null ) {
            	if ( Message.isDebugOn ) {
                	Message.printDebug ( 1, routine, this._gtype + "Unable to find X labels using " +
                	minlabels + " to " + maxlabels + " labels.  Using data values." );
            	}
            	this._xlabels = new double [2];
            	this._xlabels[0] = this._data_limits.getMinX();
            	this._xlabels[1] = this._data_limits.getMaxX();
        	}
        	*/
        	this._data_lefty_limits = new GRLimits ( this._xlabels[0], this._ylabels_lefty[0],
            	this._xlabels[_xlabels.length - 1], this._ylabels_lefty[this._ylabels_lefty.length - 1] );
        	this._da_lefty_graph.setDataLimits ( this._data_lefty_limits );
        	return;
        }
        else if ( (nts > 1)
   			|| ((nts == 1) && (this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() == TimeInterval.YEAR)) ) {
        	// Multiple time series or single year interval time series:
        	// - X-axis is limits of time series
        	// - Y-axis is ordinal position for time series (first at the top), similar to period of record
        	// - use the logic after these 'if' checks, similar to a normal time series
        }
    }
	else if ( this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
		// Labels are based on the this._data_limits.
		// Need to check precision for units but assume .1 for now.
		String maxstring = StringUtil.formatString(	this._data_lefty_limits.getMaxX(), "%." + this._xaxis_precision + "f");
		label_extents = GRDrawingAreaUtil.getTextExtents( this._da_lefty_label, maxstring, GRUnits.DEVICE );
		width = label_extents.getWidth();
		minlabels = (int)(this._drawlim_lefty_graph.getWidth()/(width*3.0));
		if ( minlabels < 3 ) {
			minlabels = 3;
		}
		maxlabels = (int)(this._drawlim_lefty_graph.getHeight()/(width*1.5));

		propValue = this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
		boolean asLog = false;
		if ((propValue != null) && propValue.equalsIgnoreCase("Log")){
			asLog = true;
		}

		if ( maxlabels < minlabels ) {
			maxlabels = minlabels*2;
		}
		while ( minlabels >= 3 ) {
			if ( this._regression_data != null ) {
				// Old.
				//_xlabels = GRAxis.findNLabels ( this._regression_data.getMin1(),
					//_regression_data.getMax1(), false, minlabels, maxlabels );
				if (asLog) {
				    this._xlabels = GRAxis.findNLabels (
//					    Math.pow(10, this._data_limits.getMinX()), Math.pow(10, this._data_limits.getMaxX()),
				        this._data_lefty_limits.getMinX(), this._data_lefty_limits.getMaxX(),	false, minlabels, maxlabels );
				}
				else {
				    this._xlabels = GRAxis.findNLabels ( this._data_lefty_limits.getMinX(),
					this._data_lefty_limits.getMaxX(), false, minlabels, maxlabels );
				}
			}
			else {
			    // Use the limits of the time series data.
				this._xlabels = GRAxis.findNLabels (	limitsLeftYAxis.getMinValue(),limitsLeftYAxis.getMaxValue(),
					false, minlabels, maxlabels );
			}
			if ( this._xlabels != null ) {
				break;
			}
			--minlabels;
		}
		if ( this._xlabels == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, this._gtype + "Unable to find X labels using " +
				minlabels + " to " + maxlabels + " labels.  Using data values." );
			}
			this._xlabels = new double [2];
			this._xlabels[0] = limitsLeftYAxis.getMinValue();
			this._xlabels[1] = limitsLeftYAxis.getMaxValue();
		}
		this._data_lefty_limits = new GRLimits ( this._xlabels[0], this._ylabels_lefty[0],
					this._xlabels[_xlabels.length - 1], this._ylabels_lefty[this._ylabels_lefty.length - 1] );
		if ( !this._is_reference_graph ) {
		    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
                GRLimits dataLimits = new GRLimits(this._data_lefty_limits);
                this._da_lefty_graph.setDataLimits ( dataLimits.reverseY() );
            }
            else {
                this._da_lefty_graph.setDataLimits ( this._data_lefty_limits );
            }
		}
		return;
	}

	// Remainder is x-axis with dates.

	// First get the extents of a typical label, based on graphics and precision of label.

	DateTime date = new DateTime ( 2000.5, true );
	date.setPrecision ( this._xaxis_date_precision );
	// Font for this._grda was set above.  Get limits - are interested in horizontal positioning based on dates.
	label_extents = GRDrawingAreaUtil.getTextExtents( this._da_bottomx_label, date.toString(), GRUnits.DEVICE );
	width = label_extents.getWidth();
	// Maintain spacing of at least one label width.
	int nlabels = (int)(this._drawlim_lefty_graph.getWidth()/(width*2.0));
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Number of X labels is " + nlabels );
	}

	// Number of months in data.

	int nmonths = this._end_date.getAbsoluteMonth() - this._start_date.getAbsoluteMonth() + 1;
	List<Double> x_axis_labels_temp = new ArrayList<Double>(10);

	int delta = 0;
	if ( this._xaxis_date_precision == DateTime.PRECISION_YEAR ) {
		// Yearly data.
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Determining Year labels" );
		}
		if ( nlabels != 0 ) {
			delta = (this._end_date.getYear() - this._start_date.getYear() + 1)/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( this._start_date );
		for ( ; date.lessThanOrEqualTo(this._end_date); ) {
			x_axis_labels_temp.add ( Double.valueOf(date.toDouble() ) );
			date.addYear ( delta );
		}
	}
	else if ( this._xaxis_date_precision == DateTime.PRECISION_MONTH ) {
		// Monthly data.
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Determining month labels" );
		}
		if ( nlabels != 0 ) {
			delta = nmonths/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( this._start_date );
		for ( ; date.lessThanOrEqualTo(this._end_date); ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1,	routine, this._gtype + "Label is for " + date );
			}
			x_axis_labels_temp.add ( Double.valueOf(date.toDouble() ) );
			date.addMonth ( delta );
		}
	}
	else if ( this._xaxis_date_precision == DateTime.PRECISION_DAY ) {
		// Daily data.
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1,	routine, this._gtype + "Determining day labels" );
		}
		int ndays = this._end_date.getAbsoluteDay() - this._start_date.getAbsoluteDay() + 1;
		if ( nlabels != 0 ) {
			delta = ndays/nlabels;
		}
		if ( delta == 0 ) {
			delta = 1;
		}
		date = new DateTime ( this._start_date );
		for ( ; date.lessThanOrEqualTo(this._end_date); ) {
			x_axis_labels_temp.add ( Double.valueOf(date.toDouble() ) );
			date.addDay ( delta );
		}
	}
	else if ( this._xaxis_date_precision == DateTime.PRECISION_HOUR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Determining hour labels." );
		}

		// Could be irregular data.
		int nhours = 0;

		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = true;
		TS ts = getFirstNonNullTS ( includeLeftYAxis, includeRightYAxis );
		// Try to find first non-null time series.
		int nts = this.__tslist.size();
		for ( int its = 0; its < nts; ++its ) {
	        ts = this.__tslist.get(its);
	        if ( ts != null ) {
	            break;
	        }
		}
		if (ts == null) {
		    // FIXME SAM 2008-01-13 What do do in this situation?
		    // Unable to get time series to evaluate Hopefully a check is occurring prior to this to detect all null data.
		    if ( Message.isDebugOn ) {
		        Message.printDebug ( 10, routine, "No time series are non-null.");
		    }
		    return;
		}
		int dataIntervalBase = ts.getDataIntervalBase();

		if (dataIntervalBase == TimeInterval.IRREGULAR) {
			nhours = (int)(
				TimeUtil.absoluteMinute(this._end_date.getYear(),this._end_date.getMonth(),
					this._end_date.getDay(), this._end_date.getHour(),this._end_date.getMinute() )
				- TimeUtil.absoluteMinute(this._start_date.getYear(),this._start_date.getMonth(),
					this._start_date.getDay(),this._start_date.getHour(), this._start_date.getMinute())) / 60;
		}
		else if (dataIntervalBase == TimeInterval.HOUR) {
			// Not likely but could happen.
			nhours = HourTS.calculateDataSize( this._start_date, this._end_date, 1);
		}
		else if (dataIntervalBase == TimeInterval.DAY) {
			// Not likely but could happen.
			nhours = DayTS.calculateDataSize(this._start_date, this._end_date, 1) * 24;
		}
		else {
			nhours = HourTS.calculateDataSize( this._start_date, this._end_date, 1);
		}

		if (nlabels != 0) {
			delta = nhours / nlabels;
		}

		if (delta == 0) {
			delta = 1;
		}

		date = new DateTime(this._start_date);

		for (; date.lessThanOrEqualTo(this._end_date); ) {
			x_axis_labels_temp.add( Double.valueOf(date.toDouble()));
			date.addHour(delta);
		}
	}
	else if ( this._xaxis_date_precision == DateTime.PRECISION_MINUTE ) {
		// Could be irregular data.
		int nminutes = 0;

		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = true;
		TS ts = getFirstNonNullTS(includeLeftYAxis,includeRightYAxis);
		if (ts == null) {
			ts = this.__tslist.get(0);
		}
		int dataIntervalBase = ts.getDataIntervalBase();

		if (dataIntervalBase == TimeInterval.IRREGULAR) {
			nminutes = (int)(
				TimeUtil.absoluteMinute(this._end_date.getYear(), this._end_date.getMonth(),
					this._end_date.getDay(), this._end_date.getHour(), this._end_date.getMinute())
				- TimeUtil.absoluteMinute( this._start_date.getYear(), this._start_date.getMonth(),
					this._start_date.getDay(), this._start_date.getHour(), this._start_date.getMinute()));
		}
		else if (dataIntervalBase == TimeInterval.HOUR) {
			// Not likely but could happen.
			nminutes = HourTS.calculateDataSize(this._start_date, this._end_date, 1) * 60;
		}
		else if (dataIntervalBase == TimeInterval.DAY) {
			// Not likely but could happen.
			nminutes = DayTS.calculateDataSize(	this._start_date, this._end_date, 1) * 1440;
		}
		else if (dataIntervalBase == TimeInterval.MONTH) {
			// Not likely but could happen.
			nminutes = MonthTS.calculateDataSize( this._start_date, this._end_date, 1) * 1440 * 31;
		}
		else {
			nminutes = MinuteTS.calculateDataSize( this._start_date, this._end_date, 1);
		}

		if (nlabels != 0) {
			delta = nminutes / nlabels;
		}

		if (delta == 0) {
			delta = 60;
		}

		// Round to even 60 minutes.
		delta = (delta / 60) * 60;

		// delta was being rounded to 0 in some cases above and that was causing infinite loops below.
		if (delta == 0) {
			delta = 60;
		}

		date = new DateTime(this._start_date);
		for ( ; date.lessThanOrEqualTo(this._end_date); ) {
			x_axis_labels_temp.add( Double.valueOf(date.toDouble()));
			date.addMinute(delta);
		}
	}

	// Now convert list to array of labels.

	int size = x_axis_labels_temp.size();
	this._xlabels = new double[size];
	for ( int i = 0; i < size; i++ ) {
		this._xlabels[i] = x_axis_labels_temp.get(i).doubleValue();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,this._gtype + "_xlabel[" + i + "]=" + this._xlabels[i] );
		}
	}
}

/**
Determine and set the precision for the X axis.
The precision is set to the most detailed time series data interval.
Call this in the constructor so the precision can be used in setDrawingLimits().
This information is not used for scatter plots or other plots that don't use date axes.
*/
private void computeXAxisDatePrecision ( ) {
	// Initialize to largest value.
	this._xaxis_date_precision = TimeInterval.YEAR;
	if ( this.__tslist == null ) {
		return;
	}

	// Loop through and find the smallest time unit from the time series intervals.

	int size = this.__tslist.size();
	TS ts = null;
	int interval = 0;
	DateTime date = null;
	for (int i = 0; i < size; i++) {
		ts = this.__tslist.get(i);
		if ( (ts == null) || !ts.getEnabled() || !isTSEnabled(i) ) {
			continue;
		}

		try {
		    // Set the axis precision to the smallest time interval of any data time series.
			interval = ts.getDataIntervalBase();
			if ( interval == TimeInterval.IRREGULAR ) {
				// Use the precision from the first date in the data.
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
			if ( interval < this._xaxis_date_precision ) {
				this._xaxis_date_precision = interval;
			}
		}
		catch ( Exception e ) {
			// Do nothing for now.
			;
		}
	}
	// Now convert the precision to a real DateTime precision.
	if ( this._xaxis_date_precision == TimeInterval.YEAR ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is year." );
		}
		this._xaxis_date_precision = DateTime.PRECISION_YEAR;
	}
	else if ( this._xaxis_date_precision == TimeInterval.MONTH ) {
		this._xaxis_date_precision = DateTime.PRECISION_MONTH;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is month." );
		}
	}
	else if ( this._xaxis_date_precision == TimeInterval.DAY ) {
		this._xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is day." );
		}
	}
	else if ( this._xaxis_date_precision == TimeInterval.HOUR ) {
		this._xaxis_date_precision = DateTime.PRECISION_HOUR;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is hour." );
		}
	}
	else if ( this._xaxis_date_precision == TimeInterval.MINUTE ) {
		this._xaxis_date_precision = DateTime.PRECISION_MINUTE;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is minute." );
		}
	}
	else {
	    // Default to day.
		this._xaxis_date_precision = DateTime.PRECISION_DAY;
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "TSGraph.setXAxisDatePrecision", this._gtype + "X axis date precision is day." );
		}
	}
}

/**
 * Create the symbol table used to defined the colors for a raster graph.
 * This should be called when a raster graph is created.
 * The symbol table is saved to this.rasterSymbolTable.
 * @param tslist list of time series for the product
 * @return GRSymbolTable (may be empty if no suitable time series are available)
 */
private GRSymbolTable createRasterSymbolTable ( List<TS> tslist ) {
	String routine = getClass().getSimpleName() + ".createRasterSymbolTable";
	GRSymbolTable symtable = null;
	String propValue = null;
	if ( tslist.size() == 1 ) {
		// Specify at the time series level.
		propValue = this._tsproduct.getLayeredPropValue ( "SymbolTablePath", this.subproduct, 0, false);
		if ( (propValue == null) || propValue.isEmpty() ) {
			Message.printStatus ( 2, routine, "SymbolTablePath is not defined for the single time series.");
		}
	}
	else {
		// Specify at the graph level.
		propValue = this._tsproduct.getLayeredPropValue ( "SymbolTablePath", this.subproduct, -1, false);
		if ( (propValue == null) || propValue.isEmpty() ) {
			Message.printStatus ( 2, routine, "SymbolTablePath is not defined for the graph (sub-product) for multiple time series.");
		}
	}

	if ( (propValue != null) && !propValue.isEmpty() ) {
		// The file is either absolute or relative to the time series product file.
		File f = new File( this._tsproduct.getPropList().getPersistentName() );
		String filename = IOUtil.verifyPathForOS( IOUtil.toAbsolutePath( f.getParent(), propValue) );
		try {
			symtable = GRSymbolTable.readFile ( filename );
			Message.printStatus ( 2, routine, "Read " + symtable.size() + " rows for raster symbol table from \"" + filename + "\".");
		}
		catch ( IOException e ) {
			Message.printWarning(3, routine, "Error reading symbol table (" + filename + ") - will use default.");
			Message.printWarning(3, routine, e);
		}
	}
	if ( symtable == null ) {
		// Define a default symbol table:
		// - base on data limits
		// - use a blue (small value) to red (large value color ramp,
		//   although different data types might go the opposite direction
		Message.printStatus ( 2, routine, "Creating a default symbol table.");
		TSLimits limits = null;
		try {
			// The limits reflect all time series.
			limits = TSUtil.getDataLimits(tslist);
		}
		catch ( Exception e ) {
			// Log the exception.
			Message.printWarning(3, routine, "Error creating default symbol table:");
			Message.printWarning(3, routine, e);
		}
		if ( limits != null ) {
			//double tsMin = ts.getDataLimits().getMinValue();
			//double tsMax = ts.getDataLimits().getMaxValue();
			double tsMin = limits.getMinValue();
			double tsMax = limits.getMaxValue();
			GRColor noDataColor = GRColor.white;
			symtable = GRSymbolTable.createForColorRamp ( tsMin, tsMax, 5, 10,
				GRColorRampType.BLUE_TO_RED, 2, noDataColor );
			Message.printStatus ( 2, routine,
				"Created a default SymbolTablePath using blue to red colors for time series data range " + tsMin + " to " + tsMax + ".");
		}
	}
	if ( symtable == null ) {
		// Create an empty symbol table:
		// - will probably result in full black raster graph
		Message.printStatus ( 2, routine,
			"Created an empty symbol table because could not get data limits.");
		symtable = new GRSymbolTable();
	}
	return symtable;
}

/**
Perform additional analysis on the data if other than a basic graph is indicated.
After the analysis, the data limits are recomputed (this is done for simple data also).
@param graphType the graph type
*/
private void doAnalysis ( TSGraphType graphType ) {
	String routine = getClass().getSimpleName() + ".doAnalysis";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Analyzing time series for " + this.__leftYAxisGraphType +
		    " graph - will produced derived data if necessary for output.");
	}
	List<TS> tslist = getTSList();
	if ( graphType == TSGraphType.XY_SCATTER ) {
		// Do a linear regression analysis.
		// The first time series is considered the independent (X) and the remaining time series the Y.
		// If a regression fails, set it to null in the list so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = tslist.size() - 1;
		if ( nreg > 0 ) {
			this._regression_data = new ArrayList<> ( nreg );
		}
		// Set the regression properties.  These may be changed by the properties interface.
		boolean analyzeForFilling = false;
		if ( this._tsproduct.getLayeredPropValue ( "XYScatterAnalyzeForFilling", this.subproduct, -1, false ).
		    equalsIgnoreCase("True") ) {
		    analyzeForFilling = true;
		}
		DateTime dependentAnalysisStart = null;
		String propValue = this._tsproduct.getLayeredPropValue ( "XYScatterDependentAnalysisPeriodStart", this.subproduct, -1, false );
		if ( TimeUtil.isDateTime(propValue) ) {
		    try {
		        dependentAnalysisStart = DateTime.parse(propValue);
		    }
		    catch ( Exception e ) {
		        // Should not happen.
		    }
		}
		DateTime dependentAnalysisEnd = null;
		propValue = this._tsproduct.getLayeredPropValue ( "XYScatterDependentAnalysisPeriodEnd", this.subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                dependentAnalysisEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen.
            }
        }
        DateTime independentAnalysisStart = null;
		propValue = this._tsproduct.getLayeredPropValue ( "XYScatterIndependentAnalysisPeriodStart", this.subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                independentAnalysisStart = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen.
            }
        }
        DateTime independentAnalysisEnd = null;
		propValue = this._tsproduct.getLayeredPropValue ( "XYScatterIndependentAnalysisPeriodEnd", this.subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                independentAnalysisEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen.
            }
        }
        DateTime fillStart = null;
        propValue = this._tsproduct.getLayeredPropValue ( "XYScatterFillPeriodStart", this.subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                fillStart = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen.
            }
        }
        DateTime fillEnd = null;
        propValue = this._tsproduct.getLayeredPropValue ( "XYScatterFillPeriodEnd", this.subproduct, -1, false );
        if ( TimeUtil.isDateTime(propValue) ) {
            try {
                fillEnd = DateTime.parse(propValue);
            }
            catch ( Exception e ) {
                // Should not happen.
            }
        }
		Double intercept = null;
		propValue = this._tsproduct.getLayeredPropValue ( "XYScatterIntercept", this.subproduct, -1, false );
		if ( StringUtil.isDouble(propValue)) {
		    intercept = Double.parseDouble(propValue);
		}
		RegressionType analysisMethod = RegressionType.valueOfIgnoreCase(
		    this._tsproduct.getLayeredPropValue ( "XYScatterMethod", this.subproduct, -1, false ) );
		int [] analysisMonths = StringUtil.parseIntegerSequenceArray(
		    this._tsproduct.getLayeredPropValue ( "XYScatterMonth", this.subproduct, -1, false ), ", ", StringUtil.DELIM_SKIP_BLANKS );
		NumberOfEquationsType numberOfEquations = NumberOfEquationsType.valueOfIgnoreCase(
		    this._tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", this.subproduct, -1, false ) );
		DataTransformationType transformation = DataTransformationType.valueOfIgnoreCase(
		    this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false ) );

		TSRegression regressionData = null;
		for ( int i = 1; i <= nreg; i++ ) {
			// The first time series [0] is always the dependent
			// time series and time series [1+] are the independent for each relationship.
			ts0 = tslist.get(0);
			ts = tslist.get(i);
			try {
			    regressionData = new TSRegression ( ts, ts0,
			            analyzeForFilling,
			            analysisMethod,
			            intercept, numberOfEquations, analysisMonths,
			            transformation,
			            null, // Default value for <= 0 data value when log transform.
	                    null, // Don't specify the confidence interval.
			            dependentAnalysisStart, dependentAnalysisEnd,
			            independentAnalysisStart, independentAnalysisEnd,
			            fillStart, fillEnd );
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, "Error performing regression for TS [" + (i - 1) + "]." );
				Message.printWarning ( 3, routine, e );
				regressionData = null;
			}
			// Always add something.
			this._regression_data.add ( regressionData );
		}
	}
	else if ( graphType == TSGraphType.DOUBLE_MASS ) {
		// Do a double mass analysis so the information is available.
		// TODO SAM 2007-05-09 Need to enable?
		//TS ts0 = null;
		//TS ts1 = null;
		//if ( tslist.size() == 2 ) {
		//	ts0 = this.__tslist.elementAt(0);
		//	ts1 = this.__tslist.elementAt(1);
		//}
/*
		if ( (ts0 != null) && (ts1 != null) ) {
			PropList rprops = new PropList("doublemass");
			try {
			    this._double_mass_data = new TSDoubleMass ( ts0, ts1, rprops );
			}
			catch ( Exception e ) {
				this._title = "Double Mass - Unable to analyze";
			}
		}
*/
	}
	else if ( (graphType == TSGraphType.DURATION) && (tslist.size() != 0) ) {
		// Generate TSDurationAnalysis for each time series.
		int size = tslist.size();
		this._duration_data = new ArrayList<> ( size );
		TSDurationAnalysis da = null;
		for ( int i = 0; i < size; i++ ) {
			try {
			    da = new TSDurationAnalysis ( tslist.get(i) );
				this._duration_data.add ( da );
			}
			catch ( Exception e ) {
				this._duration_data.add ( null );
			}
		}
	}
	else if (graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		// Do a linear regression analysis.
		// The first time series is considered the independent (X) and the remaining time series the Y.
		// If a regression fails, set it to null in the list so the plot positions are kept consistent...
		TS ts0 = null;
		TS ts = null;
		int nreg = tslist.size() - 1;
		if (nreg > 0 ) {
			this._regression_data = new ArrayList<>(nreg);
		}

		TSRegression regressionData = null;
		for (int i = 1; i <= nreg; i++) {
			// The first time series [0] is always the independent
			// time series and time series [1+] are the dependent for each relationship.
			ts0 = tslist.get(0);
			ts = tslist.get(i);
			try {
			    // Pick reasonable defaults for the regression analysis - they can be changed in the properties interface.
				regressionData = new TSRegression( ts0, ts,
				    false, // Analyze for filling.
				    RegressionType.MOVE2,
                    null, // intercept.
                    NumberOfEquationsType.ONE_EQUATION,
                    null, //analysisMonths,
                    DataTransformationType.NONE,
                    null, // Default value for <= 0 data value when log transform.
                    null, // Don't specify the confidence interval.
                    null, //dependentAnalysisStart,
                    null, //dependentAnalysisEnd,
                    null, //independentAnalysisStart,
                    null, //independentAnalysisEnd,
                    null, // FillStart
                    null ); // FillEnd
				// FIXME SAM 2009-08-30 These were being set previously for TSRegression but seem to have no effect:
		        //rprops.set("MinimumDataCount", "1");
		        //rprops.set("MinimumR", "0");
		        //rprops.set("BestFit", "SEP");
		        //rprops.set("OutputFile", "c:\\temp\\output.regress");
				regressionData.createPredictedTS();
			}
			catch (Exception e) {
				Message.printWarning(3, routine, "Error performing regression for TS [" + (i - 1) + "].");
				Message.printWarning(3, routine, e);
				regressionData = null;
			}

			// Always add something.
			this._regression_data.add(regressionData);
		}
	}
    else if ( graphType == TSGraphType.AREA_STACKED ) {
        doAnalysisAreaStacked();
    }
	// Compute the maximum data limits using the analysis objects that have been created.
	computeDataLimits ( true );
}

/**
Analyze the time series data for the stacked area graph.
This consists of creating new time series for each original time series,
where the new time series are the sum of the previous time series.
The resulting derived time series will be drawn as area graphs back to front.
Any original time series that do not have a graph type of stacked area, are a null time series,
or are not enabled in the time series product,
are set as null in the derived data to maintain the order in the time series list.
@param subproduct the subproduct to process
*/
private void doAnalysisAreaStacked () {
    String routine = getClass().getSimpleName() + ".doAnalysisAreaStacked";
	// Analysis is only enabled for left y-axis.
    // TODO smalers 2024-11-11 the enabled time series are enforced earlier.
	//boolean includeLeftYAxis = true;
	//boolean includeRightYAxis = false;
    //List<TS> tslist = getEnabledTSList(includeLeftYAxis,includeRightYAxis);

    // The left y-axis time series list should match the product.
    List<TS> tslist = this.__left_tslist;
    /*
    if ( tslist.size() == 0 ) {
        // None are explicitly enabled so process all.
        // TODO SAM 2010-11-22 Need to evaluate enabled.
        tslist = getTSList();
    }
    */
    if ( tslist.size() == 0 ) {
        // No time series to graph.
        return;
    }
    Message.printStatus(2,routine,"Have " + tslist.size() + " left y-axis time series to analyze for stacked area graph.  Some may be null.");
    // The original time series are considered to be incremental time series and are
    // used to create corresponding total time series,
    // which will be graphed with simple area graphs.
    // The first derived time series is a clone of the first actual time series.
    List<TS> derivedTSList = new ArrayList<>();
    // Get the overall period for the time series:
    // - all stacked time series will have the same period
    // - null time series are ignored
    TSLimits limits = null;
    try {
        limits = TSUtil.getPeriodFromTS(tslist, TSUtil.MAX_POR);
        if ( limits == null ) {
        	Message.printStatus(2, routine, "Unable to compute the limits for time series.  Unable to compute derived data.");
        }
        else {
        	Message.printStatus(2, routine, "The available data period for stacked area graph is " +
        	limits.getDate1() + " to " + limits.getDate2() );
        }
    }
    catch ( Exception e ) {
    	// Ignore the exception, typically for no data.
    }
    int its = -1; // Counter for time series being processed.
    TS newtsPrev = null;
    for ( TS ts : tslist ) {
        ++its;
        if ( !isTSEnabled(its) ) {
        	// If the time series is to be ignored, set as null.
        	Message.printStatus(2,routine,"Time series [" + its + "] is not enabled, adding null to the derived TS list." );
        	derivedTSList.add(null);
        }
        else if ( ts == null ) {
        	// Time series may be null if a data problem (also if not enabled, but that is handled above).
            Message.printStatus(2,routine,"Time series [" + its + "] is null - set to null in derived list." );
            derivedTSList.add(null);
        }
        else {
        	// If the time series graph type is not stacked area, then skip it as a derived time series, it will be drawn separately.
        	TSGraphType tsGraphType = getTimeSeriesGraphType(TSGraphType.AREA_STACKED, its);
        	if ( tsGraphType != TSGraphType.AREA_STACKED ) {
            	Message.printStatus(2,routine,"Time series [" + its + "] graph type (" + tsGraphType +
                	") is not stacked area type - set to null in derived list." );
            	derivedTSList.add(null);
        	}
        	else {
        		// Clone the original time series and adjust its values:
        		// - the y-value will therefore display stacked total
        		// - if the first time series clone, otherwise copy the previous and add to the value
        		// - the clone will contain the original TSID, etc., for use with the tracker
        		TS newts = null;
        		if ( newtsPrev == null ) {
        			// Previous time series was null so initialize the total without adding.
            		newts = (TS)ts.clone();
            		try {
                		newts.changePeriodOfRecord(limits.getDate1(), limits.getDate2());
            		}
            		catch ( Exception e ) {
                		Message.printWarning(3,routine,
                    		"Error changing period of record to " + limits.getDate1() + " to " + limits.getDate2()
                    		+ " for stacked area time series [" + its + "] - " +
                    		"setting to null in derived list (" + e + ")." );
                		derivedTSList.add(null);
                		continue;
            		}
        		}
        		else {
        			// Have a previous time series so can add to it.
            		newts = (TS)ts.clone();
            		try {
                		newts.changePeriodOfRecord(limits.getDate1(), limits.getDate2());
            		}
            		catch ( Exception e ) {
                		Message.printWarning(3,routine,
                    		"Error changing period of record to " + limits.getDate1() + " to " + limits.getDate2()
                    		+ " for stacked area time series [" + its + "] - " +
                    		"setting to null in derived list (" + e + ")." );
                		derivedTSList.add(null);
                		continue;
            		}
            		try {
                		TSUtil.add(newts, newtsPrev);
            		}
            		catch ( Exception e ) {
                		Message.printWarning(3, routine, "Error adding time series for stacked area graph [" + its + "] - " +
                				"setting to null in derived list (" + e + ").");
                		derivedTSList.add(null);
                		continue;
            		}
        		}
        		// if here, created a copy of the original time series.
        		// Set the description back to the original (don't want add, etc. in description).
        		newts.setDescription ( ts.getDescription() );
        		// Set the derived time series list so that it can be used in drawing methods.
        		derivedTSList.add ( newts );
        		Message.printStatus(2,routine,"Time series [" + its + "] \"" + ts.getIdentifierString()
        			+ "\" added to the derived TS list." );
        		// Keep track of the previous non-null time series, since each in stacked graph is an increment of the previous.
        		newtsPrev = newts;
    		}
        }
    }
    Message.printStatus(2, routine, "Created " + derivedTSList.size() + " time series in the derived TS list for stacked area graph.");
    setDerivedTSList ( derivedTSList );
}

/**
Draws any annotations on the graph.  This method can be called multiple times,
once with false before drawing data and then with true after data have been drawn.
@param tsproduct the time series product being processed
@param daLeftYAxisGraph the drawing area for the graph's left Y-axis
@param daRightAxisGraph the drawing area for the graph's right Y-axis
@param drawingStepType indicates the step during drawing that should be matched,
used to position the annotation in the correct rendering layer
If the annotation "Order" property does not match the drawing step then the annotation will not be drawn.
*/
private void drawAnnotations ( TSProduct tsproduct,
	GRDrawingArea daLeftYAxisGraph, GRDrawingArea daRightYAxisGraph, TSGraphDrawingStepType drawingStepType ) {
	if (this._is_reference_graph) {
		return;
	}
	String routine = getClass().getSimpleName() + ".drawAnnotations(" + drawingStepType + ")";
	if ( Message.isDebugOn ) {
		Message.printDebug(1,"","In \"" + routine + "\"." );
	}
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

	// Set clipping for both Y axes.
	// TODO SAM 2016-10-23 Evaluate whether annotations should be allowed to extend outside graph:
	// - this may be desirable for symbols, in particular because they get cut off
	// Left y-axis.
	Shape clip = GRDrawingAreaUtil.getClip(daLeftYAxisGraph);
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, daLeftYAxisGraph.getDataLimits());
	// Right y-axis.
	// TODO SAM 2016-10-23 Figure out how to clip in the proper sequence.
	//clip = GRDrawingAreaUtil.getClip(daRightYAxisGraph);
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, daRightYAxisGraph.getDataLimits());

	boolean drawLeftYAxis = true;
	boolean drawRightYAxis = false;
	int numAnnotations = tsproduct.getNumAnnotations(this.subproduct);
	boolean isAnnotation = true;
	boolean allowLayeredProp = false;
	if ( Message.isDebugOn ) {
		Message.printDebug(1,"","Drawing " + numAnnotations + " annotations for subproduct " + this.subproduct );
	}
	for ( int iAnnotation = 0; iAnnotation < numAnnotations; iAnnotation++ ) {
		// Only draw the annotation if it is enabled.
		String enabled = tsproduct.getLayeredPropValue("Enabled", subproduct, iAnnotation, allowLayeredProp, isAnnotation);
		if ( Message.isDebugOn ) {
			Message.printDebug(1,"","Drawing annotation for subproduct " + this.subproduct + " and annotation " + (iAnnotation + 1) + " enabled=" + enabled );
		}
		if ( (enabled != null) && enabled.equalsIgnoreCase("false") ) {
			// Ignore the annotation.
			continue;
		}

		annotation = new PropList("Annotation " + iAnnotation);
		valid = true;
		drawLeftYAxis = true; // Default is left y-axis.
		drawRightYAxis = false;
		type = tsproduct.getLayeredPropValue("ShapeType", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
		if (type == null) {
			Message.printWarning(2, routine, "Null shapetype.");
			valid = false;
		}
		else if (type.equalsIgnoreCase("Line")) {
			points = tsproduct.getLayeredPropValue("Points", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			if (points == null) {
				valid = false;
				Message.printWarning(2, routine, "Null points.");
			}
			else {
				pointsV = StringUtil.breakStringList(points, ",", 0);
				if (pointsV == null || pointsV.size() != 4) {
					valid = false;
					Message.printWarning(2, routine, "Invalid points declaration.");
				}
			}
		}
		else if (type.equalsIgnoreCase("Rectangle")) {
			points = tsproduct.getLayeredPropValue("Points", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			if (points == null) {
				valid = false;
				Message.printWarning(2, routine, "Null points.");
			}
			else {
				pointsV = StringUtil.breakStringList(points, ",", 0);
				if (pointsV == null || pointsV.size() != 4) {
					valid = false;
					Message.printWarning(2, routine, "Invalid points declaration.");
				}
			}
		}
		else if (type.equalsIgnoreCase("Symbol")) {
			point = tsproduct.getLayeredPropValue("Point", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point.");
			}
			else {
				pointV = StringUtil.breakStringList(point, ",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, "Invalid point declaration.");
				}
			}
			isSymbol = true;
		}
		else if (type.equalsIgnoreCase("Text")) {
			point = tsproduct.getLayeredPropValue("Point", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			if (point == null) {
				valid = false;
				Message.printWarning(2, routine, "Null point.");
			}
			else {
				pointV = StringUtil.breakStringList(point, ",", 0);
				if (pointV == null || pointV.size() != 2) {
					valid = false;
					Message.printWarning(2, routine, "Invalid point declaration.");
				}
			}
		}
		else {
			valid = false;
		}

		if (!valid) {
			// Some error encountered in checkProperties for this so skip.
			Message.printWarning(2, routine, "Invalid annotation: " + (this.subproduct + 1) + "." + (iAnnotation + 1));
			continue;
		}

		s = tsproduct.getLayeredPropValue("Order", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);

		if (s == null) {
			// Default to on top.
			s = "OnTopOfData";
		}

		if ((drawingStepType == TSGraphDrawingStepType.BEFORE_DATA) && !s.equalsIgnoreCase("BehindData")) {
			// Current drawing step is before data but that does not match the annotation.
			continue;
		}
		if ((drawingStepType == TSGraphDrawingStepType.AFTER_DATA) && !s.equalsIgnoreCase("OnTopOfData")) {
			// Current drawing step is after data but that does not match the annotation.
			continue;
		}
		if ((drawingStepType == TSGraphDrawingStepType.BEFORE_BACK_AXES) && !s.equalsIgnoreCase("BehindAxes")) {
			// Current drawing step is before drawing back axes but that does not match the annotation.
			continue;
		}

		// If the annotation uses an annotation table, have to loop through the table.

		String annotationTableID = tsproduct.getLayeredPropValue("AnnotationTableID", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);

		if ( (annotationTableID != null) && !annotationTableID.isEmpty() ) {
			// Annotations specified using an annotation table.
			// TODO SAM 2016-10-23 need to enable
			// Lookup annotation table.
			// Loop through records.
			// Draw each annotation.
			// Optimize to not draw if outside visible graph.
		}
		else {
			// Simple annotations:
			// - one shape per annotation
			// - the getLayeredPropValue method last argument is 'true' indicating that annotations are being processe3d
			// Properties for all annotations.
			String yAxis = null;
			annotation.set("Color", tsproduct.getLayeredPropValue("Color", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
			annotation.set("Order", tsproduct.getLayeredPropValue("Order", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
			annotation.set("ShapeType", tsproduct.getLayeredPropValue("ShapeType", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
			annotation.set("XAxisSystem", tsproduct.getLayeredPropValue("XAxisSystem", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
			annotation.set("YAxisSystem", tsproduct.getLayeredPropValue("YAxisSystem", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
			// Now check to see which axis annotations should be drawn on.
			yAxis = tsproduct.getLayeredPropValue("YAxis", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			if ( (yAxis != null) && !yAxis.isEmpty() && yAxis.equalsIgnoreCase("Right") ) {
				drawLeftYAxis = false;
				drawRightYAxis = true;
			}
			// Set whether the axis values for the annotation is number, DateTime:
			// - set the default up front to the product property
			// - resent below if not specified to a default based on the value
			String XFormat = tsproduct.getLayeredPropValue("XFormat", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			annotation.set("XFormat", XFormat);
			String YFormat = tsproduct.getLayeredPropValue("YFormat", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation);
			annotation.set("YFormat", YFormat);
			// Properties for shape type.
			if (type.equalsIgnoreCase("Line")) {
				// Properties for Line shape type.
				annotation.set("LineStyle", tsproduct.getLayeredPropValue("LineStyle", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("LineWidth", tsproduct.getLayeredPropValue("LineWidth", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("Points", tsproduct.getLayeredPropValue("Points", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				if ( (XFormat == null) || XFormat.isEmpty() ) {
					// Might be a DateTime based on point values.
				}
			}
			else if (type.equalsIgnoreCase("Rectangle")) {
				// Properties for Rectangle shape type.
				//annotation.set("OutlineColor", tsproduct.getLayeredPropValue("OutlineColor", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation)); // Future enhancement.
				annotation.set("Points", tsproduct.getLayeredPropValue("Points", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				if ( (XFormat == null) || XFormat.isEmpty() ) {
					// Might be a DateTime based on point values.
				}
			}
			else if (type.equalsIgnoreCase("Symbol")) {
				// Properties for Symbol shape type.
				annotation.set("OutlineColor", tsproduct.getLayeredPropValue("OutlineColor", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("Point", tsproduct.getLayeredPropValue("Point", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("SymbolSize", tsproduct.getLayeredPropValue("SymbolSize", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("SymbolStyle", tsproduct.getLayeredPropValue("SymbolStyle", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("SymbolPosition", tsproduct.getLayeredPropValue("SymbolPosition", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				if ( (XFormat == null) || XFormat.isEmpty() ) {
					// Might be a DateTime based on point values.
				}
			}
			else if (type.equalsIgnoreCase("Text")) {
				// Properties for Text shape type.
				annotation.set("FontName", tsproduct.getLayeredPropValue("FontName", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("FontSize", tsproduct.getLayeredPropValue("FontSize", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("FontStyle", tsproduct.getLayeredPropValue("FontStyle", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("Point", tsproduct.getLayeredPropValue("Point", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("Text", tsproduct.getLayeredPropValue("Text", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				annotation.set("TextPosition", tsproduct.getLayeredPropValue("TextPosition", this.subproduct, iAnnotation, allowLayeredProp, isAnnotation));
				if ( (XFormat == null) || XFormat.isEmpty() ) {
					// Might be a DateTime based on point values.
				}
			}

			if ( Message.isDebugOn ) {
				Message.printStatus(1, routine, "Annotation is:" + annotation);
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
			else if ( drawRightYAxis ) {
				GRDrawingAreaUtil.drawAnnotation(daRightYAxisGraph, annotation);
			}
		}

		if (isSymbol && niceSymbols) {
			if ( drawLeftYAxis ) {
				GRDrawingAreaUtil.setDeviceAntiAlias(daLeftYAxisGraph, false);
			}
			else if ( drawRightYAxis ) {
				GRDrawingAreaUtil.setDeviceAntiAlias(daRightYAxisGraph, false);
			}
		}
	}

	// Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds.
	// Left y-axis.
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, (Shape)null);
	GRDrawingAreaUtil.setClip(daLeftYAxisGraph, clip);
	// Right y-axis.
	// TODO SAM 2016-10-23 Figure out how to clip in the proper sequence.
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, (Shape)null);
	//GRDrawingAreaUtil.setClip(daRightYAxisGraph, clip);
}

/**
Draw the axes features that should be behind the plotted data, including the surrounding box, and the grid lines.
Call drawAxesFront() to draw features that are to be on top of the graph (tick marks and labels).
*/
private void drawAxesBack ( ) {
	// Previous code used the main this._da_graph to draw the axis labels.
	// Now that label areas are separate drawing areas, draw the labels in those drawing areas.
	// To make sure the drawing limits are OK, set to the this._data_limits values here.

	this._datalim_lefty_label = new GRLimits ( this._da_lefty_label.getDataLimits() );
	// TODO SAM 2013-01-22 Remove the following if code tests out.
	//_datalim_lefty_label.setBottomY ( this._data_limits.getBottomY() );
	//_datalim_lefty_label.setTopY ( this._data_limits.getTopY() );
	// This handles if the axis is reversed.
	this._datalim_lefty_label.setBottomY ( this._da_lefty_graph.getDataLimits().getBottomY() );
    this._datalim_lefty_label.setTopY ( this._da_lefty_graph.getDataLimits().getTopY() );
	this._da_lefty_label.setDataLimits ( this._datalim_lefty_label );

	this._datalim_righty_label = new GRLimits(this._da_righty_label.getDataLimits());
	if ( this._data_righty_limits != null ) {
		// TODO sam 2017-02-05 figure out why this might be null...was causing an exception.
		this._datalim_righty_label.setBottomY ( this._data_righty_limits.getBottomY() );
		this._datalim_righty_label.setTopY ( this._data_righty_limits.getTopY() );
	}
	this._da_righty_label.setDataLimits ( this._datalim_righty_label );

	this._datalim_bottomx_label = new GRLimits(this._da_bottomx_label.getDataLimits());
	this._datalim_bottomx_label.setLeftX ( this._data_lefty_limits.getLeftX() );
	this._datalim_bottomx_label.setRightX ( this._data_lefty_limits.getRightX() );
	this._da_bottomx_label.setDataLimits ( this._datalim_bottomx_label );

	if ( !this._is_reference_graph ) {
		drawXAxisGrid();
		drawYAxisGrid();
	}
	drawOutlineBox();
}

// TODO SAM 2016-10-23 Need to break up the following method into components:
// - for now work on refactoring to remove globals
/**
Draw axes features that show in front of the plotted data.  Currently this
includes the axes tick marks, titles, and labels.  The tick marks are currently always drawn in black.
@param tsproduct time series product containing properties
*/
private void drawAxesFront ( TSProduct tsproduct,
	GRDrawingArea daMainTitle,
	boolean drawLeftYAxisLabels, TSGraphType leftYAxisGraphType,
	GRDrawingArea daLeftYAxisGraph, GRDrawingArea daLeftYAxisTitle, GRDrawingArea daLeftYAxisLabel,
	GRLimits datalimLeftYAxisGraph, GRLimits datalimLeftYAxisTitle, GRLimits datalimLeftYAxisLabel,
	double ylabelsLeftYAxis[], int leftYAxisPrecision,
	boolean drawRightYAxisLabels, TSGraphType rightYAxisGraphType,
	GRDrawingArea daRightYAxisGraph, GRDrawingArea daRightYAxisTitle, GRDrawingArea daRightYAxisLabel,
	GRLimits datalimRightYAxisGraph, GRLimits datalimRightYAxisTitle, GRLimits datalimRightYAxisLabel,
	double ylabelsRightYAxis[], int rightYAxisPrecision,
	//boolean drawBottomXAxisLabels, // TODO SAM 2016-10-23 Enable in the future.
	GRDrawingArea daBottomXAxisTitle, GRDrawingArea daBottomXAxisLabel,
	GRLimits datalimBottomXAxisTitle, GRLimits datalimBottomXAxisLabel,
	double xlabelsBottomXAxis[]) {
	if ( this._is_reference_graph ) {
		return;
	}

	// Used throughout.

	String prop_value = null;
	String title;
	String rotation;
	String fontname;
	String fontsize;
	String fontstyle;

	// Draw text nice using anti-aliasing.
	GRDrawingAreaUtil.setDeviceAntiAlias( daMainTitle, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daLeftYAxisLabel, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daRightYAxisLabel, true);
	GRDrawingAreaUtil.setDeviceAntiAlias( daBottomXAxisLabel, true);

	boolean leftYAxisLogY = false;
	prop_value = tsproduct.getLayeredPropValue ( "LeftYAxisType", this.subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		leftYAxisLogY = true;
	}

	prop_value = tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		leftYAxisLogY = false;
	}

	// Left Y Axis labels, and ticks.

	fontname = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", this.subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", this.subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontStyle", this.subproduct, -1, false );
	String yaxisDir = tsproduct.getLayeredPropValue ( "LeftYAxisDirection", this.subproduct, -1, false );
    boolean yaxisDirReverse = false;
    if ( (yaxisDir != null) && yaxisDir.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
        yaxisDirReverse = true;
    }
	GRDrawingAreaUtil.setFont ( daLeftYAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.setColor ( daLeftYAxisLabel, GRColor.black );

	// Used to handle raster graphs.
    int dataInterval = TimeInterval.UNKNOWN;
    //int dataMult = -1;
    if ( this.__tslist.size() == 1 ) {
    	TS ts = this.__tslist.get(0);
    	if ( ts != null ) {
    		dataInterval = ts.getDataIntervalBase();
    		//dataMult = ts.getDataIntervalMult();
    	}
    }

	if ( leftYAxisLogY ) {
		// Only draw major labels.
		double [] ylabels_log = new double[(ylabelsLeftYAxis.length)/9 + 1];
		int j = 0;
		for ( int k = 0; k < ylabelsLeftYAxis.length; k++ ) {
			if ( ((k%9) == 0) || (k == 0)) {
				ylabels_log[j++] = ylabelsLeftYAxis[k];
			}
		}
		GRAxis.drawLabels ( daLeftYAxisLabel, ylabels_log.length,
		ylabels_log, datalimLeftYAxisLabel.getRightX(), GRAxisDimensionType.Y, "%.1f", GRText.RIGHT|GRText.CENTER_Y );
		ylabels_log = null;
	}
	else {
	    if (drawLeftYAxisLabels) {
			if ( leftYAxisGraphType == TSGraphType.PERIOD ) {
				// Only want to label with whole numbers that are > 0 and <= this.__tslist.size().
			    // FIXME SAM 2013-07-21 This is no different than below.  Precision is being set elsewhere for PERIOD graph.
				GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis.length,
					ylabelsLeftYAxis, datalimLeftYAxisLabel.getRightX(),
					GRAxisDimensionType.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
			else if ( (leftYAxisGraphType == TSGraphType.RASTER)
				&& ((this.__tslist.size() > 1) || ((this.__tslist.size() == 1) && (dataInterval == TimeInterval.YEAR))) ) {
				// Raster graph for multiple time series.
				// The legend labels were previously calculated at the edges of time series pixels,
				// but draw the labels shifted to the middle of the time series y-axis range.
				double [] ylabelsLeftYAxis2 = new double[ylabelsLeftYAxis.length - 1];
				for ( int i = 0; i < ylabelsLeftYAxis2.length; i++ ) {
					// OK to use fraction because label is formatted with no decimals.
					ylabelsLeftYAxis2[i] = ylabelsLeftYAxis[i] + .5;
				}
				GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis2.length,
					ylabelsLeftYAxis2, datalimLeftYAxisLabel.getRightX(),
					GRAxisDimensionType.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
			else if ( (leftYAxisGraphType == TSGraphType.RASTER) && (this.__tslist.size() == 1) &&
				( (dataInterval == TimeInterval.HOUR) || (dataInterval == TimeInterval.MINUTE)) ) {
				// Single time series raster graph:
				// - Y-axis positioning uses absolute day
				// - convert to a formatted date
				String [] labels = new String[ylabelsLeftYAxis.length];
				for ( int iLabel = 0; iLabel < labels.length; iLabel++ ) {
					// Convert floating point label to an integer.
					int ylabelInt = (int)(ylabelsLeftYAxis[iLabel] + .1);
					int [] dateParts = TimeUtil.getYearMonthDayFromAbsoluteDay(ylabelInt);
					labels[iLabel] = "" + dateParts[0] + "-"
						+ String.format("%02d", dateParts[1]) + "-"
						+ String.format("%02d", dateParts[2]);
				}
			    GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis.length,
			    	ylabelsLeftYAxis, labels, datalimLeftYAxisLabel.getRightX(),
			    	GRAxisDimensionType.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
			else {
				// All other graph types.
			    GRAxis.drawLabels ( daLeftYAxisLabel, ylabelsLeftYAxis.length,
			    	ylabelsLeftYAxis, datalimLeftYAxisLabel.getRightX(),
			    	GRAxisDimensionType.Y, "%." + leftYAxisPrecision + "f", GRText.RIGHT|GRText.CENTER_Y);
			}
		}
	}

	// Left Y-Axis title.
	title = tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", this.subproduct, -1, false );
	rotation = tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", this.subproduct, -1, false );
	double rotationDeg = 0.0;
	try {
		rotationDeg = Double.parseDouble(rotation);
	}
	catch ( NumberFormatException e) {
		// Ignore - should be empty or a valid number.
	}
	fontname = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", this.subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", this.subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daLeftYAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( daLeftYAxisTitle, title,
		datalimLeftYAxisTitle.getCenterX(), datalimLeftYAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y, rotationDeg );

	// Right Y Axis labels, and ticks.
	//Message.printStatus(2, "drawAxesFront", "Right y-axis da limits:" + this._da_righty_label.getDrawingLimits());
	//Message.printStatus(2, "drawAxesFront", "Right y-axis data limits:" + this._da_righty_label.getDataLimits());
	//Message.printStatus(2, "drawAxesFront", "Right y-axis this._datalim_righty_label:" + this._datalim_righty_label);

	if ( drawRightYAxisLabels ) {
		boolean rightYAxisLogY = false;
		prop_value = tsproduct.getLayeredPropValue ( "RightYAxisType", this.subproduct, -1, false );
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
			rightYAxisLogY = true;
		}

		fontname = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontName", this.subproduct, -1, false );
		fontsize = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", this.subproduct, -1, false );
		fontstyle = tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", this.subproduct, -1, false );
		String yaxisDirRight = tsproduct.getLayeredPropValue ( "RightYAxisDirection", this.subproduct, -1, false );
	    boolean yaxisDirRightReverse = false;
	    if ( (yaxisDirRight != null) && yaxisDirRight.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
	        yaxisDirRightReverse = true;
	    }
		GRDrawingAreaUtil.setFont ( daRightYAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );
		GRDrawingAreaUtil.setColor ( daRightYAxisLabel, GRColor.black );
		if ( Message.isDebugOn ) {
			if ( ylabelsRightYAxis != null ) {
				for ( int i = 0; i < ylabelsRightYAxis.length; i++ ) {
					Message.printDebug(1,"","Right y-axis label is " + ylabelsRightYAxis[i] );
				}
			}
		}
		if ( rightYAxisLogY ) {
			// Only draw major labels.
			double [] ylabels_log = new double[(ylabelsRightYAxis.length)/9 + 1];
			int j = 0;
			for ( int k = 0; k < ylabelsRightYAxis.length; k++ ) {
				if ( ((k%9) == 0) || (k == 0)) {
					ylabels_log[j++] = ylabelsRightYAxis[k];
				}
			}
			GRAxis.drawLabels ( daRightYAxisLabel, ylabels_log.length,
			ylabels_log, datalimRightYAxisLabel.getRightX(), GRAxisDimensionType.Y, "%.1f", GRText.RIGHT|GRText.CENTER_Y );
			ylabels_log = null;
		}
		else if ( (daRightYAxisLabel != null) && (ylabelsRightYAxis != null) && (datalimRightYAxisLabel != null) ) { // Can be null if no right y-axis.
			if ( rightYAxisGraphType == TSGraphType.PERIOD ) {
				// Only want to label with whole numbers that are > 0 and <= this.__tslist.size().
			    // FIXME SAM 2013-07-21 This is no different than below.  Precision is being set elsewhere for PERIOD graph.
				GRAxis.drawLabels ( daRightYAxisLabel, ylabelsRightYAxis.length,
					ylabelsRightYAxis, datalimRightYAxisLabel.getLeftX(),
					GRAxisDimensionType.Y, "%." + rightYAxisPrecision + "f", GRText.LEFT|GRText.CENTER_Y);
			}
			else {
			    GRAxis.drawLabels ( daRightYAxisLabel, ylabelsRightYAxis.length,
			    	ylabelsRightYAxis, datalimRightYAxisLabel.getLeftX(),
			    	GRAxisDimensionType.Y, "%." + rightYAxisPrecision + "f", GRText.LEFT|GRText.CENTER_Y);
			}
		}

		// Right Y-Axis title.
		String position = tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", this.subproduct, -1, false );
		if ( (position != null) && !position.equalsIgnoreCase("None") ) {
			title = tsproduct.getLayeredPropValue ( "RightYAxisTitleString", this.subproduct, -1, false );
			rotation = tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", this.subproduct, -1, false );
			rotationDeg = 0.0;
			try {
				rotationDeg = Double.parseDouble(rotation);
			}
			catch ( NumberFormatException e) {
				// Ignore - should be empty or a valid number.
			}
			fontname = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", this.subproduct, -1, false );
			fontsize = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", this.subproduct, -1, false );
			fontstyle = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", this.subproduct, -1, false );
			GRDrawingAreaUtil.setFont ( daRightYAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
			GRDrawingAreaUtil.drawText ( daRightYAxisTitle, title,
				datalimRightYAxisTitle.getCenterX(), datalimRightYAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y, rotationDeg );
		}
	}

	// Bottom X-Axis title.

	title = tsproduct.getLayeredPropValue ( "BottomXAxisTitleString", this.subproduct, -1, false );
	fontname = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", this.subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontSize", this.subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daBottomXAxisTitle, fontname, fontstyle, StringUtil.atod(fontsize) );
	GRDrawingAreaUtil.drawText ( daBottomXAxisTitle, title,
		datalimBottomXAxisTitle.getCenterX(), datalimBottomXAxisTitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Bottom X Axis labels, title, and ticks.

	fontname = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", this.subproduct, -1, false );
	fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", this.subproduct, -1, false );
	fontstyle = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( daBottomXAxisLabel, fontname, fontstyle, StringUtil.atod(fontsize) );

	// Label axis after drawing so ticks are on top of data.

	GRDrawingAreaUtil.setLineWidth(daLeftYAxisGraph, 1.0);
	if ( (leftYAxisGraphType == TSGraphType.XY_SCATTER) || (leftYAxisGraphType == TSGraphType.DURATION) ) {
		// Label the X axis with formatted numbers.
		GRAxis.drawLabels ( daBottomXAxisLabel, xlabelsBottomXAxis.length,
			xlabelsBottomXAxis, datalimBottomXAxisLabel.getTopY(), GRAxisDimensionType.X, "%.1f", GRText.TOP|GRText.CENTER_X );
		double[] xt = new double[2];
		double[] yt = new double[2];
		double[] yt2 = new double[2];
		double tick_height = 0.0; // Height of major tick marks.
		yt[0] = ylabelsLeftYAxis[0];
		yt2[0] = ylabelsLeftYAxis[0];
	    if ( yaxisDirReverse ) {
	        yt[0] = ylabelsLeftYAxis[ylabelsLeftYAxis.length - 1];
	        yt2[0] = yt[0];
	    }
		// Figure out the y-positions and tick height (same regardless of intervals being used for labels).
		if ( leftYAxisLogY ) {
			// Need to make sure the line is nice length.
			tick_height = yt[0]*.05;
			yt[1] = yt[0] + tick_height;
			yt2[1] = yt2[0] + tick_height/2.0;
		}
		else {
		    tick_height = datalimLeftYAxisGraph.getHeight()*.02;
		    if ( yaxisDirReverse ) {
		        // Reverse Y axis orientation.
                yt[1] = yt[0] - tick_height;
                yt2[1] = yt2[0] - tick_height/2.0;
		    }
		    else {
		        // Normal Y axis orientation.
    			yt[1] = yt[0] + tick_height;
    			yt2[1] = yt2[0] + tick_height/2.0;
		    }
		}
		for ( int i = 0; i < xlabelsBottomXAxis.length; i++ ) {
			xt[0] = xt[1] = xlabelsBottomXAxis[i];
			GRDrawingAreaUtil.drawLine ( daLeftYAxisGraph, xt, yt );
		}
	}
	else {
	    // Draw the X-axis date/time labels.
		if ( drawLeftYAxisLabels || drawRightYAxisLabels ) {
			// Only draw the X-axis if there is something on the y-axis also.
			drawXAxisDateLabels ( leftYAxisGraphType, false );
		}
	}

	// Turn off anti-aliasing since a performance hit for data.
	GRDrawingAreaUtil.setDeviceAntiAlias( daMainTitle, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daLeftYAxisLabel, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daRightYAxisLabel, false);
	GRDrawingAreaUtil.setDeviceAntiAlias( daBottomXAxisLabel, false);
}

/**
Draw the "current" time line, if properties are present to do so.
This checks to see if the CurrentDateTime property is set (it will be set in the override properties in the TSProduct).
If set and within the limits of the current graph, the current line will be drawn.
*/
private void drawCurrentDateTime () {
	if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (this.__leftYAxisGraphType == TSGraphType.DURATION) ) {
		return;
	}
	// Allow layered properties because the current time could be specified once for all graphs.
	String prop_value = this._tsproduct.getLayeredPropValue( "CurrentDateTime", this.subproduct, -1, true );
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
		    // Parse the date/time from the property.
			current_time = DateTime.parse ( prop_value );
		}
		prop_value = this._tsproduct.getLayeredPropValue( "CurrentDateTimeColor", this.subproduct, -1, true );
		// FIXME SAM 2008-01-10 Remove when done.
		//Message.printStatus(2, "", "Color for CurrentDateTimeColor is " + prop_value );
		try {
		    GRDrawingAreaUtil.setColor(this._da_lefty_graph, GRColor.parseColor( prop_value) );
		}
		catch ( Exception e2 ) {
			GRDrawingAreaUtil.setColor ( this._da_lefty_graph, GRColor.green );
		}
		double xp[] = new double[2];
		double yp[] = new double[2];
		xp[0] = current_time.toDouble();
		xp[1] = xp[0];
		// Get the drawing area limits.  This allows the check for reference and main graph windows.
		GRLimits data_limits = this._da_lefty_graph.getDataLimits();
		yp[0] = data_limits.getMinY();
		yp[1] = data_limits.getMaxY();
		if ( (xp[0] >= data_limits.getMinX()) && (xp[0] <= data_limits.getMaxX()) ) {
			GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, xp, yp );
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
public void drawDrawingAreas () {
	// This method is used by developers so OK to use global object data extensively.
	//boolean do_names = false;
	boolean do_names = true; // Display drawing area names.
	this._da_page.setColor ( GRColor.magenta ); // Sets color for all.
	GRDrawingAreaUtil.setFont ( this._da_page, "Helvetica", "Plain", 8 );
	// Reference and main.
	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_graph, this._data_lefty_limits.getLeftX(), this._data_lefty_limits.getBottomY(),
		this._data_lefty_limits.getWidth(), this._data_lefty_limits.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_lefty_graph, this._da_lefty_graph.getName(), this._data_lefty_limits.getCenterX(),
			this._data_lefty_limits.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
	if ( (this._da_righty_graph != null) && (this._data_righty_limits != null) ) {
		GRDrawingAreaUtil.drawRectangle ( this._da_righty_graph, this._data_righty_limits.getLeftX(), this._data_righty_limits.getBottomY(),
			this._data_righty_limits.getWidth(), this._data_righty_limits.getHeight() );
		if ( do_names ) {
			// Add a little bit to the height so does not overlap the left y-axis text.
			GRDrawingAreaUtil.drawText ( this._da_righty_graph, this._da_righty_graph.getName(), this._data_righty_limits.getCenterX(),
				(this._data_righty_limits.getCenterY() + this._data_righty_limits.getHeight()*.05), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
		}
	}
	if ( this._is_reference_graph ) {
		// Don't need to draw anything else.
		return;
	}

	// Everything else draw top to bottom and left to right.

	GRDrawingAreaUtil.drawRectangle ( this._da_page, this._datalim_page.getLeftX(), this._datalim_page.getBottomY(),
		this._datalim_page.getWidth(), this._datalim_page.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_page, this._da_page.getName(), this._datalim_page.getCenterX(),
			this._datalim_page.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_maintitle, this._datalim_maintitle.getLeftX(), this._datalim_maintitle.getBottomY(),
		this._datalim_maintitle.getWidth(), this._datalim_maintitle.getHeight());
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_maintitle, this._da_maintitle.getName(), this._datalim_maintitle.getCenterX(),
			this._datalim_maintitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_subtitle, this._datalim_subtitle.getLeftX(), this._datalim_subtitle.getBottomY(),
		this._datalim_subtitle.getWidth(), this._datalim_subtitle.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_subtitle, this._da_subtitle.getName(), this._datalim_subtitle.getCenterX(),
			this._datalim_subtitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

/*  Not enabled until can get Yaxis labels out of the way.
	GRDrawingAreaUtil.drawRectangle ( this._da_topx_title, this._datalim_topx_title.getLeftX(), this._datalim_topx_title.getBottomY(),
		this._datalim_topx_title.getWidth(), this._datalim_topx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_topx_title, this._da_topx_title.getName(), this._datalim_topx_title.getCenterX(),
			this._datalim_topx_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_topx_label, this._datalim_topx_label.getLeftX(), this._datalim_topx_label.getBottomY(),
		this._datalim_topx_label.getWidth(), this._datalim_topx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_topx_label, this._da_topx_label.getName(), this._datalim_topx_label.getCenterX(),
			this._datalim_topx_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
*/

	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_title, this._datalim_lefty_title.getLeftX(),
	    this._datalim_lefty_title.getBottomY(), this._datalim_lefty_title.getWidth(), this._datalim_lefty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_lefty_title, this._da_lefty_title.getName(), this._datalim_lefty_title.getCenterX(),
			this._datalim_lefty_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_label, this._datalim_lefty_label.getLeftX(),
	    this._datalim_lefty_label.getBottomY(), this._datalim_lefty_label.getWidth(), this._datalim_lefty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_lefty_label, this._da_lefty_label.getName(), this._datalim_lefty_label.getCenterX(),
			this._datalim_lefty_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_righty_title, this._datalim_righty_title.getLeftX(),
	    this._datalim_righty_title.getBottomY(), this._datalim_righty_title.getWidth(), this._datalim_righty_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_righty_title, this._da_righty_title.getName(), this._datalim_righty_title.getCenterX(),
			this._datalim_righty_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_righty_label, this._datalim_righty_label.getLeftX(),
	    this._datalim_righty_label.getBottomY(), this._datalim_righty_label.getWidth(), this._datalim_righty_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_righty_label, this._da_righty_label.getName(), this._datalim_righty_label.getCenterX(),
		    this._datalim_righty_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_bottomx_label, this._datalim_bottomx_label.getLeftX(),
	    this._datalim_bottomx_label.getBottomY(), this._datalim_bottomx_label.getWidth(), this._datalim_bottomx_label.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_bottomx_label, this._da_bottomx_label.getName(), this._datalim_bottomx_label.getCenterX(),
		    this._datalim_bottomx_label.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_bottomx_title, this._datalim_bottomx_title.getLeftX(),
	    this._datalim_bottomx_title.getBottomY(), this._datalim_bottomx_title.getWidth(), this._datalim_bottomx_title.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_bottomx_title, this._da_bottomx_title.getName(), this._datalim_bottomx_title.getCenterX(),
			this._datalim_bottomx_title.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_bottom_legend, this._datalim_lefty_bottom_legend.getLeftX(),
		this._datalim_lefty_bottom_legend.getBottomY(), this._datalim_lefty_bottom_legend.getWidth(), this._datalim_lefty_bottom_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText (this._da_lefty_bottom_legend, this._da_lefty_bottom_legend.getName(), this._datalim_lefty_bottom_legend.getCenterX(),
			this._datalim_lefty_bottom_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_left_legend, this._datalim_lefty_left_legend.getLeftX(),
		this._datalim_lefty_left_legend.getBottomY(), this._datalim_lefty_left_legend.getWidth(), this._datalim_lefty_left_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_lefty_left_legend, this._da_lefty_left_legend.getName(), this._datalim_lefty_left_legend.getCenterX(),
			this._datalim_lefty_left_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}

	GRDrawingAreaUtil.drawRectangle ( this._da_lefty_right_legend, this._datalim_lefty_right_legend.getLeftX(),
		this._datalim_lefty_right_legend.getBottomY(), this._datalim_lefty_right_legend.getWidth(), this._datalim_lefty_right_legend.getHeight() );
	if ( do_names ) {
		GRDrawingAreaUtil.drawText ( this._da_lefty_right_legend, this._da_lefty_right_legend.getName(), this._datalim_lefty_right_legend.getCenterX(),
			this._datalim_lefty_right_legend.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
	}
}

/**
Draw a duration plot.
@param tslist the list of time series to render
*/
private void drawDurationPlot ( GRDrawingArea daGraph, TSProduct tsproduct,
	List<TS> tslist, List<TSDurationAnalysis> durationAnalysisList,
	boolean isReferenceGraph ) {
	String routine = getClass().getSimpleName() + ".drawDurationPlot";
	if ( (tslist == null) || (tslist.size() == 0) ) {
		return;
	}
	int size = tslist.size();
	TS ts = null;
	TSDurationAnalysis da = null;
	double [] values, percents;
	GRSymbolShapeType shapeType = GRSymbolShapeType.NONE;
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
			Message.printWarning ( 2, routine, "Null time series to graph [" + i + "]." );
			return;
		}

		da = durationAnalysisList.get(i);

		if (da == null) {
			Message.printWarning(2, routine, "Null TSDurationAnalysis to graph [" + i + "].");
			return;
		}
		values = da.getValues();
		percents = da.getPercents();
		if ( (values == null) || (percents == null) ) {
			Message.printWarning ( 2, routine, "Null TSDurationAnalysis data graph [" + i + "]." );
			return;
		}
		// Set line color and width.
		try {
		    daGraph.setColor ( GRColor.parseColor( tsproduct.getLayeredPropValue ( "Color", this.subproduct, i, false ) ) );
		}
		catch ( Exception e ) {
			daGraph.setColor ( GRColor.black );
		}
        prop_value = getLayeredPropValue("LineWidth", this.subproduct, i, false, null);
        if (prop_value != null) {
            if (StringUtil.isInteger(prop_value)) {
                GRDrawingAreaUtil.setLineWidth( daGraph, Integer.parseInt(prop_value));
            }
        }
		GRDrawingAreaUtil.drawPolyline ( daGraph, values.length, percents, values );
		prop_value = tsproduct.getLayeredPropValue ( "SymbolStyle", this.subproduct, i, false );
		try {
		    shapeType = GRSymbolShapeType.valueOfIgnoreCase(prop_value);
		    if ( shapeType == null ) {
		    	shapeType = GRSymbolShapeType.NONE;
		    }
		}
		catch ( Exception e ) {
			shapeType = GRSymbolShapeType.NONE;
		}
		symbol_size = StringUtil.atod( tsproduct.getLayeredPropValue ( "SymbolSize", this.subproduct, i, false ) );
		if ( !isReferenceGraph && (shapeType != GRSymbolShapeType.NONE) && (symbol_size > 0) ) {
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
			}
			GRDrawingAreaUtil.drawSymbols (daGraph, shapeType, values.length, percents, values,
				symbol_size, GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
			}
		}
	}
	// Clean up.
	GRDrawingAreaUtil.setLineWidth( daGraph, 1.0 );
}

/**
 * Draw the major error message, typically a fatal issue that will help user know why the graph is not as expected.
 * @param daError drawing area for errors
 */
private void drawErrors ( GRDrawingArea daError ) {
	if ( this.__errorMessageList.size() > 0 ) {
		// Use default font for legend.
		String legendFontName = this._tsproduct.getLayeredPropValue ( "LegendFontName", this.subproduct, -1, false );
		String legendFontSize = this._tsproduct.getLayeredPropValue ( "LegendFontSize", this.subproduct, -1, false );
		String legendFontStyle = this._tsproduct.getLayeredPropValue ( "LegendFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( daError, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
		// For now only show the first error.
		String error = this.__errorMessageList.get(0);
		boolean doFill = false; // For now don't fill.
		if ( doFill ) {
			// Use black text in filled drawing area.
			GRDrawingAreaUtil.setColor(daError, GRColor.lightGray);
			GRDrawingAreaUtil.fillRectangle(daError, daError.getDataLimits().getLeftX(),
				daError.getDataLimits().getBottomY(),
				daError.getDataLimits().getWidth(), daError.getDataLimits().getHeight() );
			GRDrawingAreaUtil.setColor(daError, GRColor.black);
		}
		else {
			// Use red text in main drawing area>
			GRDrawingAreaUtil.setColor(daError, GRColor.red);
		}
		GRDrawingAreaUtil.drawText ( daError, " " + error, daError.getDataLimits().getLeftX(),
			daError.getDataLimits().getTopY(), 0.0, GRText.LEFT|GRText.TOP );
		// TODO sam 2017-02-08 if more errors were drawn, offset by font height and descend vertically.
	}
}

/**
Draw the time series graph for one axis.
This is the highest-level draw method for drawing the data part of the graph and calls the other time series drawing methods.
@param graphType graph type to draw, for the axis.
@param daGraph drawing area for the graph, could be aligned with left y-axis or right y-axis.
@param tsproduct time series product to process
@param tslist list of time series to render, might be all time series or only one axis, depending on graph type.
@param dataLimits data limits for drawing area, consistent with nice labels, and zoom.
*/
private void drawGraph ( TSGraphType graphType, GRDrawingArea daGraph, TSProduct tsproduct,
	List<TS> tslist, GRLimits dataLimits ) {
	String routine = getClass().getSimpleName() + ".drawGraph";
	int size = tslist.size();

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Drawing graph type " + graphType + ", " + size +
		    " time series total (some may be other graph types)." );
	}
	if ( size == 0 ) {
		return;
	}

	if ( dataLimits == null ) {
		// Not properly initialized, missing data, etc.
		return;
	}

	// Print the limits for debugging.

	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Drawing limits: " + daGraph.getDrawingLimits() );
		Message.printDebug ( 1, routine, this._gtype + "Data limits: " + daGraph.getDataLimits() );
		Message.printDebug ( 1, routine, this._gtype + "dataLimits: " + dataLimits );
		Message.printDebug ( 1, routine, this._gtype + "Plotting limits: " + daGraph.getPlotLimits(GRCoordinateType.PLOT) );
	}

	/* If need to use for development.
	Message.printStatus ( 1, routine, this._gtype + "Drawing: [" + this.subproduct + "]: " + daGraph.getName() );
	Message.printStatus ( 1, routine, this._gtype + "Drawing limits: " + daGraph.getDrawingLimits() );
	Message.printStatus ( 1, routine, this._gtype + "Data limits: " + daGraph.getDataLimits() );
	Message.printStatus ( 1, routine, this._gtype + "dataLimits: " + dataLimits );
	Message.printStatus ( 1, routine, this._gtype + "Plotting limits: " + daGraph.getPlotLimits(GRDrawingArea.COORD_PLOT) );
	*/

	// Graph the time series.  If a reference map, only draw one time series, as specified in the properties.
	TS ts = null;
	if ( graphType == TSGraphType.DURATION ) {
		// All the time series are graphed (no left or right list).
		drawDurationPlot (daGraph, tsproduct, tslist, this._duration_data, this._is_reference_graph);
	}
    if ( graphType == TSGraphType.RASTER ) {
        drawGraphRaster (tsproduct, tslist);
    }
	else if ( graphType == TSGraphType.XY_SCATTER ) {
		drawXYScatterPlot (daGraph, tsproduct, dataLimits, tslist, this._regression_data);
	}
	else if ( (graphType == TSGraphType.PREDICTED_VALUE) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
	    boolean residual = true;
		if ( graphType == TSGraphType.PREDICTED_VALUE ) {
			residual = false;
		}
		TSRegression regressionData = null;
		int nregression = 0;
		if ( this._regression_data != null ) {
			nregression = this._regression_data.size();
		}
		TS predicted = null;

		for (int ir = 0; ir < nregression; ir++) {
			regressionData = this._regression_data.get(ir);
			if (regressionData != null) {
			   	if (residual) {
					if ( isTSEnabled(ir + 1) ) {
						drawTS(tsproduct, ir + 1, regressionData.getResidualTS(), graphType );
					}
				}
				else {
					PropList props = new PropList("");

					if ( isTSEnabled(0) ) {
						drawTS(tsproduct, 0, regressionData.getIndependentTS(),graphType, props);
					}

					if ( isTSEnabled(ir + 1) ) {
						drawTS(tsproduct, ir + 1, regressionData.getDependentTS(), graphType, props);
						props.set("LineStyle=Dashed");
						props.set("SymbolStyle=None");
						predicted = regressionData.getPredictedTS();
						drawTS(tsproduct, ir + 1, predicted, graphType, props);
					}
				}
			}
			else {
				// Ignore null regression data.
			}
		}
	}
	else if ( graphType == TSGraphType.AREA_STACKED ) {
	    drawGraphAreaStacked (tsproduct, tslist, getDerivedTSList() );
	}
	else {
		// "Normal" graph that can be handled in general code.
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, this._gtype + "In drawGraph this._is_reference_graph=" + this._is_reference_graph  + " tslist.size()=" + tslist.size() );
		}
		for (int its = 0; its < size; its++) {
			ts = tslist.get(its);
			if ( ts == null ) {
				continue;
			}
			else if ( !this._is_reference_graph ) {
				// Not a reference graph.
				if ( !ts.getEnabled() ) {
					// Time series is not enabled at the data level so don't draw.
					continue;
				}
				if ( !isTSEnabled(its) ) {
					// Time series is not enabled in the product so don't draw.
					continue;
				}
			}
			else if (this._is_reference_graph ) {
				// Is a reference graph.
				if ( its != this._reference_ts_index ) {
					// Is a reference graph but have not found the reference time series yet.
					// Want the reference time series drawn in the same color as it occurs in the main graph.
					if (Message.isDebugOn) {
						Message.printDebug(1, routine, this._gtype + "Skipping time series " + its);
					}
					continue;
				}
			}

			// Draw each time series using the requested graph type.
		    // Allow the individual time series graph type to be different from the main graph type.
			// TODO SAM 2010-11-22 Need to evaluate defaults/checks consistent with other code.
		    TSGraphType tsGraphType = getTimeSeriesGraphType(graphType, its);
			drawTS ( tsproduct, its, ts, tsGraphType );
		}
	}
}

/**
Draw the time series graph for an "AreaStacked" graph.
@param tslist list of time series to draw, all time series for the axis.
@param tslist list of time series to graph
@param derivedTSList list of derived time series needed for stacked area graph, time series cumulative values.
*/
private void drawGraphAreaStacked ( TSProduct tsproduct, List<TS> tslist, List<TS> derivedTSList ) {
    // Loop through the derived time series that have been previously produced.
	// Draw in the reverse order because the last time series should have the biggest values and be drawn at the back.
    TSGraphType graphType = getLeftYAxisGraphType();
    // Note that that the derived list will contain time series that add to each other
    // as well as individual time series that are not stacked.
    // Check the time series graph type to know what to do.
    //List<TS> derivedTSlist = getDerivedTSList();
    // Also need the full list of time series, some of which may be drawn in addition to stacked area
    // (e.g., a line for the total of all time series).
    // This list is actually iterated because the time series positions match those in the derived time series,
    // for property purposes.
    //List<TS> tslist = getTSList();
    if ( derivedTSList == null ) {
        return;
    }
    int size = tslist.size();
    TS ts = null;
    for ( int its = size - 1; its >= 0; its-- ) {
        ts = derivedTSList.get(its);
        if ( ts == null ) {
            // No data to draw or this is not a stacked area time series.
            continue;
        }
        TSGraphType tsGraphType = getTimeSeriesGraphType ( graphType, its );
        if ( tsGraphType == TSGraphType.AREA_STACKED ) {
            drawTS ( tsproduct, its, ts, TSGraphType.AREA_STACKED );
        }
    }
    // TODO SAM 2010-11-22 Need to have property to draw on top or bottom - assume top.
    // Now also draw the other time series that may not be the same type.
    // Allow the individual time series graph type to be different from the main graph type.
    for ( int its = 0; its < size; its++ ) {
        TSGraphType tsGraphType = getTimeSeriesGraphType ( graphType, its );
        if ( tsGraphType != TSGraphType.AREA_STACKED ) {
            drawTS ( tsproduct, its, tslist.get(its), tsGraphType );
        }
    }
}

/**
Draw the time series graph for a "Raster" graph.
@param tsproduct time series product
@param tslist list of time series to draw, can be one or multiple time series
*/
private void drawGraphRaster ( TSProduct tsproduct, List<TS> tslist ) {
	String routine = getClass().getSimpleName() + ".drawGraphRaster";
	int nts = tslist.size();
	if ( nts == 0 ) {
		return;
	}
	else if ( (nts == 1)
  		&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
		// Single time series (but not year interval) is being drawn:
		// - currently only day and month interval are supported
		TS ts = null;
		int its = 0;
        ts = tslist.get(its);
        if ( ts == null ) {
    	    // Cannot draw.
            return;
        }
        int dataInterval = ts.getDataIntervalBase();
        int dataMult = ts.getDataIntervalMult();
        if ( ((dataInterval == TimeInterval.MONTH) && (dataMult == 1))
        	|| ((dataInterval == TimeInterval.DAY) && (dataMult == 1))
        	|| (dataInterval == TimeInterval.HOUR)
        	|| (dataInterval == TimeInterval.MINUTE) ) {
        	// OK.
        }
        else {
        	Message.printWarning(3, routine, "Raster graphs are only supported for 1-month, 1-day, 1-hour, and N-minute intervals.");
        	return;
        }
        // Draw the time series, which will fill in the graph are with "pixels".
        PropList overrideProps = null;
        drawTSRenderRasterGraphSingle ( ts, overrideProps );
        // Redraw the surrounding box and Y-axis ticks because the colors may have overdrawn:
        // - having a nice black border makes the graph look cleaner
        drawOutlineBox ();
        // Draw the Y axis grid and tick marks (but not the labels).
        drawYAxisGrid();
        // Draw the legend showing the color scale.
        drawLegendRaster ( tsproduct );
    }
	else {
		// Multiple time series (or single year interval time series).
		PropList overrideProps = null;
		// Do not call 'drawTS' but instead call the following, which handles multiple time series.
        drawTSRenderRasterGraphMultiple ( tslist, overrideProps );
        // Redraw the surrounding box:
        // - having a nice black border makes the graph look cleaner
        drawOutlineBox ();
        // Draw the Y axis grid and tick marks (and the labels).
        drawYAxisGrid();
        // Redraw the X as date labels and tick marks on top of the raster graph:
        // - the above code draws ticks at the top of the graph, not the bottom
        // - the following does the same
        // - TODO smalers 2023-04-09 need to fix dealing with the reversed axis
		drawXAxisDateLabels ( this.__leftYAxisGraphType, true );
        // Draw the legend showing the color scale.
        drawLegendRaster ( tsproduct );
	}
}

/**
Draw the legend.  The drawing is the same regardless of the legend position
(the legend items are draft from top to bottom and first time series to last,
except for special cases like stacked area plot where the order is reversed).
This should work well for left, right, and bottom legends.
Additional logic may need to be implemented when the top legend is supported.
@param axis GRAxis.LEFT or GRAxis.RIGHT, indicating which y-axis legend to draw.
*/
private void drawLegend ( GRAxisEdgeType axis ) {
    String routine = getClass().getSimpleName() + ".drawLegend";
	if ( this._is_reference_graph ) {
		// No legend in reference graph.
		return;
	}

	// The entire list of time series can be indicated in the legend in the following ways.
	// Only left y-axis is used (no right y-axis):
	// - legend includes all the time series
	// Have both left and right y-axis:
	// - if right y-axis legend is not shown, use left y-axis legend for all time series (? not sure ?)
	// - if right y-axis legend is shown, use right y-axis legend for time series in right y-axis
	//   and left y-axis legend for time series for left y-axis (what is done below)
	// List of time series in the legend, depends on whether left or right,
	// includes null time series, needed to ensure product time series positions.
	List<TS> tslistForAxis = new ArrayList<>();
	// List of time series in the legend, depends on whether left or right, no null time series,
	// used for sizing the legend.
	List<TS> tslistForAxisNoNulls = new ArrayList<>();
	// All the time series for the graph, including nulls, needed to get the right color, based on color defaults.
	//List<TS> tslist = this.__tslist; // TODO smalers 2024-11-11 Delete if the imatch code is removed.
	String legendPosition = "BottomLeft"; // Default..
	TSGraphType axisGraphType = null; // What is the graph type for the left or right y-axis.
	if ( axis == GRAxisEdgeType.LEFT ) {
		// Left y-axis may be only axis used or may be used with right y-axis.
		legendPosition = this._tsproduct.getLayeredPropValue("LeftYAxisLegendPosition", this.subproduct, -1, false);
		if ( legendPosition == null ) {
			// Legacy value being transitioned to "LeftYAxisLegendPosition" because of addition of right y-axis.
			legendPosition = this._tsproduct.getLayeredPropValue("LegendPosition", this.subproduct, -1, false);
		}
		// Get the left y-axis time series:
		// - include null time series so that the order matches the product
		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = false;
		boolean includeNulls = true;
		tslistForAxis = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		includeNulls = false;
		tslistForAxisNoNulls = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		axisGraphType = this.__leftYAxisGraphType;
		//Message.printStatus(2, routine, "Drawing left y-axis legend for " + tslistForAxis.size() + " time series, position=" + legendPosition + " graphType=" + axisGraphType);
	}
	else if ( axis == GRAxisEdgeType.RIGHT ) {
		legendPosition = this._tsproduct.getLayeredPropValue("RightYAxisLegendPosition", this.subproduct, -1, false);
		// Get the right y-axis time series:
		// - include null time series so that the order matches the product
		boolean includeLeftYAxis = false;
		boolean includeRightYAxis = true;
		boolean includeNulls = true;
		tslistForAxis = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		includeNulls = false;
		tslistForAxisNoNulls = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
		axisGraphType = this.__rightYAxisGraphType;
		//Message.printStatus(2, routine, "Drawing right y-axis legend for " + tslistForAxis.size() + " time series, position=" + legendPosition + " graphType=" + axisGraphType);
	}

	if ( tslistForAxisNoNulls.size() == 0 ) {
		// No non-null time series in legend to draw.
		Message.printStatus(2,routine,"There are no non-null time series - not drawing legend.");
		return;
	}

	// Figure out which legend drawing area to use.

	GRDrawingArea da_legend = null;
	GRLimits datalim_legend = null;
	if ( legendPosition.toUpperCase().startsWith("BOTTOM") ) {
		if ( axis == GRAxisEdgeType.LEFT ) {
			da_legend = this._da_lefty_bottom_legend;
			datalim_legend = this._datalim_lefty_bottom_legend;
		}
		else if ( axis == GRAxisEdgeType.RIGHT ) {
			da_legend = this._da_righty_bottom_legend;
			datalim_legend = this._datalim_righty_bottom_legend;
		}
	}
	else if ( legendPosition.equalsIgnoreCase("Left") ) {
		if ( axis == GRAxisEdgeType.LEFT ) {
			da_legend = this._da_lefty_left_legend;
			datalim_legend = this._datalim_lefty_left_legend;
		}
		else if ( axis == GRAxisEdgeType.RIGHT ) {
			da_legend = this._da_righty_left_legend;
			datalim_legend = this._datalim_righty_left_legend;
		}
	}
	else if ( legendPosition.equalsIgnoreCase("Right") ) {
		if ( axis == GRAxisEdgeType.LEFT ) {
			da_legend = this._da_lefty_right_legend;
			datalim_legend = this._datalim_lefty_right_legend;
		}
		else if ( axis == GRAxisEdgeType.RIGHT ) {
			da_legend = this._da_righty_right_legend;
			datalim_legend = this._datalim_righty_right_legend;
		}
	}
	else if (StringUtil.startsWithIgnoreCase(legendPosition, "Inside")) {
		if ( axis == GRAxisEdgeType.LEFT ) {
			da_legend = this._da_lefty_inside_legend;
			datalim_legend = this._datalim_lefty_inside_legend;
		}
		else if ( axis == GRAxisEdgeType.RIGHT ) {
			da_legend = this._da_righty_inside_legend;
			datalim_legend = this._datalim_righty_inside_legend;
		}

	    GRDrawingAreaUtil.setLineWidth( da_legend, 1 );
		GRDrawingAreaUtil.setColor(da_legend, GRColor.white);
		GRDrawingAreaUtil.fillRectangle(da_legend, -4, -4, (datalim_legend.getWidth() + 8) , (datalim_legend.getHeight() + 8) );
		GRDrawingAreaUtil.setColor(da_legend, GRColor.black);
		GRDrawingAreaUtil.drawRectangle(da_legend, -4, -4, (datalim_legend.getWidth() + 8) , (datalim_legend.getHeight() + 8) );
	}
	else {
		// Includes "None" case.
		// Don't know how to draw legend.
		Message.printStatus(2,routine,"Don't know how to draw legend in position \"" + legendPosition + "\"" );
		return;
	}

	// Get the properties for the legend.
	// TODO SAM 2017-02-07 Evaluate whether these properties should be split for left and right y-axis.
	String legendFontName = this._tsproduct.getLayeredPropValue ( "LegendFontName", this.subproduct, -1, false );
	String legendFontSize = this._tsproduct.getLayeredPropValue ( "LegendFontSize", this.subproduct, -1, false );
	String legendFontStyle = this._tsproduct.getLayeredPropValue ( "LegendFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( da_legend, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
	GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( da_legend, "TEST STRING", GRUnits.DEVICE );
	double ydelta = text_limits.getHeight();
	text_limits = null;

	// Draw legend from top down in case run out of room and need to omit some time series.
	// Can center vertically on the following
	// (line will be one font height down and font will be 1/2 height down to top).
	double ylegend = datalim_legend.getTopY() - ydelta;
	// Put first time series in list at the top.
	double symbol_size = 0;
	GRSymbolShapeType shapeType = GRSymbolShapeType.NONE;
	String prop_value = null;
	String legend = null;
	String line_style = null;
	double x[] = new double[2];
	double y[] = new double[2];
	// The number of time series associated with an axis.
	int size = tslistForAxis.size();
	boolean niceSymbols = true;
	prop_value = this._tsproduct.getLayeredPropValue( "SymbolAntiAlias", -1, -1, false);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	// Time series for drawing the legend:
	// - aligns with the product properties
	// - null time series will be skipped
	TS ts = null;

	// Currently complex graphs are only allowed for left y-axis:
	// - TODO smalers 2024-11-11 need to explain what the following is doing and how it impacts time series iteration
	if ( (axis == GRAxisEdgeType.LEFT) && (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) ) {
		size = 1 + ((size - 1) * 2);
	}
	if ( (axis == GRAxisEdgeType.LEFT) && (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		size = size - 1;
	}

	double[] lineDash = new double[2];
	lineDash[0] = 3;
	lineDash[1] = 5;

	boolean predicted = false;
	// Time series count in the subproduct (graph), used with some graphs.
	int tsNum = 0;
	TSRegression regressionData = null;

	// Position of the time series in the graph, used generally:
	// - may include null time series so handle that accordingly
	int iStart = 0;
	int iEnd = size; // One more than last index will break loop.
	int iIncrement = 1;
	// Used if any time series are being drawn as stacked area.
	boolean reverseLegendOrder = false;
	//Message.printStatus(2,routine,"Checking time series for stacked area graph type.");
	for ( int i = 0; i < tslistForAxis.size(); i++ ) {
	    TSGraphType tsGraphType = getTimeSeriesGraphType(axisGraphType, i);
    	if ( tsGraphType == TSGraphType.AREA_STACKED ) {
    		// Detected a stacked area graph:
    		// - reverse the legend order
    		// - time series that are not stacked will draw on top
    	    reverseLegendOrder = true;
    	    break;
    	}
	}
	if ( reverseLegendOrder ) {
	    // Reverse the order of drawing the time series so the legend order matches the area:
	    // - one less than first index will break loop
	    iStart = size - 1;
	    iEnd = -1;
	    iIncrement = -1;
	}
	else {
		// Normal order:
		// - one more than last index will break loop
		iStart = 0;
		iEnd = size;
		iIncrement = 1;
	}
	double xOffset = 0.0; // Offset used to position legend, when position is "BottomRight".
	double legendLineLength = 25.0; // Pixels width of the legend symbol area (line or filled rectangle).
	// TODO smalers 2024-11-11 delete the imatch code once tested out.
	//int imatch = -1; // Used to match a specific time series.

	// Position of time series that are not null (0+):
	// - initialize to -1 because it is incremented below
	int iNotNull = -1;
	// The following loop works when plotting the time series list forward or backward.
	for ( int i = iStart; i != iEnd; i = (i + iIncrement) ) {
		//imatch = -1; // Used below to match a specific time series in TSGraph index order.
		predicted = false;

		// Make sure that the legend is not drawing using negative data units.
		// If it is, then it will likely go into another graph (since there can be more than one graph in a window/page.
		if ( ylegend < 0.0 ) {
			 continue;
		}

		if ( (axis == GRAxisEdgeType.LEFT) && (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) ) {
			// Determine the correspondence of the TS to be drawn versus the actual time series that there is access to:
			// - ts 0 corresponds to 0
			// - ts 1 corresponds to 1 and 2 (for the dependent and predicted TS)
			// - ts 2 corresponds to 3 and 4 ...
			// - etc.

			if ( i == 0 ) {
				// The time series being processed.
				tsNum = 0;
			}
			else {
				if (i % 2 == 0) {
					// The ts is half of the even numbers.
					tsNum = i / 2;
					predicted = true;
				}
				else {
					// For odd numbers, the ts is half (the value plus one).
					tsNum = (i + 1) / 2;
				}
			}

			if ( !isTSEnabled(tsNum) ) {
				continue;
			}

			if (predicted) {
				// Predicted ones have to be retrieved from the regression data.
				regressionData = this._regression_data.get(tsNum - 1);
				ts = regressionData.getPredictedTS();
			}
			else {
				ts = tslistForAxis.get(tsNum);
			}

			legend = getLegendString(ts, tsNum);
			if (legend == null) {
				continue;
			}

			if (predicted) {
				legend = legend + " (Predicted)";
			}

			// Draw the legend line.
			prop_value = this._tsproduct.getLayeredPropValue( "Color", this.subproduct, tsNum, false);
			try {
				da_legend.setColor(GRColor.parseColor( prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}

			line_style = this._tsproduct.getLayeredPropValue( "LineStyle", this.subproduct, tsNum, false);

			if (line_style == null) {
				line_style = "None";
			}

			if (predicted) {
				line_style = "Dashed";
			}
		}
		else if ( (axis == GRAxisEdgeType.LEFT) && (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
			if ( !isTSEnabled(i + 1) ) {
				continue;
			}

			regressionData = this._regression_data.get(i);
			ts = regressionData.getResidualTS();

			legend = getLegendString(ts, (i + 1));
			if (legend == null) {
				continue;
			}
			else {
				legend += " (Residual)";
			}

			// Draw the legend line.
			prop_value = this._tsproduct.getLayeredPropValue( "Color", this.subproduct, (i + 1), false);

			try {
				da_legend.setColor(GRColor.parseColor(prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}
		}
		else {
			// All other graphs types:
			// - everything besides PREDICTED_VALUE and PREDICTED_VALUE_RESIDUAL
			// - 'i' is the time series position 0+
			if ( i >= size ) {
				// TODO smalers 2024-11-11 need to simplify the loop iteration for different graph types.
				break;
			}
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Drawing legend for time series [" + i + "].");
			}
			//if ( imatch < 0 ) {
				if ( !isTSEnabled(i) ) {
					// Time series is not being drawn so don't include in the legend.
					if ( Message.isDebugOn ) {
						Message.printDebug(1, routine, "  Time series [" + i + "] is not enabled - ignoring." );
					}
					continue;
				}
			//}
			//else {
				//if ( !isTSEnabled(imatch) ) {
					//continue;
				//}
			//}
			// Get the time series for the legend item:
			// - may be null if the time series had a read error or is disabled, so skip
			ts = tslistForAxis.get(i);
			if ( ts == null ) {
				// Null time series so don't draw the legend.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "  Time series [" + i + "] is null - ignoring." );
				}
				continue;
			}
			// If here have the time series that matches the position in the time series product:
			// - increment the index of time series that are not null (0+).
			++iNotNull;
			//legend = getLegendString(ts, iNotNull);
			legend = getLegendString(ts, iNotNull);
			if ( legend == null ) {
				// Something went wrong so can't draw the legend.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "  Skipping null legend for time series [" + i + "]." );
				}
				continue;
			}
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "  Drawing the legend using:  " + legend );
			}
			// If LegendPosition=BottomRight, set the X coordinate to the right side of the drawing area
			// subtracting the length of the longest string.
			if ( (iNotNull == 0) && legendPosition.equalsIgnoreCase("BottomRight") ) {
				double maxLength = 0.0;
				for ( int its = 0; its < size; its++ ) {
					if ( isTSEnabled(its) ) {
						String legend2 = getLegendString(ts, iNotNull);
						if (legend2 == null) {
							continue;
						}
						else {
							legend2 = " " + legend2;
						}
						GRLimits textLimits = GRDrawingAreaUtil.getTextExtents(da_legend, legend2, GRUnits.DATA);
						maxLength = Math.max(maxLength, textLimits.getWidth());
					}
				}
				if ( maxLength > datalim_legend.getWidth() ) {
					// Have layout issues and have gone past the left edge of the window so draw with offset zero.
					// The user will have to resize window or adjust legend format.
					xOffset = 0.0;
				}
				else {
					// The offset is computed from right edge of the drawing area.
					// This will position the BottomRight axes to start someone to the right of the left edge.
					xOffset = datalim_legend.getWidth() - maxLength - legendLineLength;
				}
			}

			// Draw the legend line.
			// Because the default colors are selected based on the full list of time series,
			// have to find the time series instance and use its position in the full list.
			// TODO sam 2017-02-07 figure out if there is a better way - is this fragile?
			/*
			imatch = -1;
			for ( TS ts2: tslist ) {
				++imatch;
				if ( ts2 == null ) {
					continue;
				}
				if ( ts2 == ts ) {
					// Found exact matching time series.
					break;
				}
			}
			*/
			//prop_value = this._tsproduct.getLayeredPropValue("Color", this.subproduct, imatch, false);
			prop_value = this._tsproduct.getLayeredPropValue("Color", this.subproduct, i, false);
			try {
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "  Color from property is \"" + prop_value + "\"");
				}
				da_legend.setColor(GRColor.parseColor(prop_value));
			}
			catch (Exception e) {
				da_legend.setColor(GRColor.black);
			}

			//line_style = this._tsproduct.getLayeredPropValue( "LineStyle", this.subproduct, imatch, false);
			line_style = this._tsproduct.getLayeredPropValue( "LineStyle", this.subproduct, i, false);

			if ( line_style == null ) {
				// Default is no line.
				line_style = "None";
			}
		}

		x[0] = datalim_legend.getLeftX() + xOffset;
		// Legend drawing limits are in device units so just use pixels.
		x[1] = x[0] + legendLineLength;
		y[0] = ylegend + ydelta/2.0;
		y[1] = y[0];
		//Message.printStatus(2,routine,"Checking time series for graph type.");
	    TSGraphType tsGraphType = null;
	    //if ( imatch >= 0 ) {
	    	//tsGraphType = getTimeSeriesGraphType(axisGraphType, imatch);
	    //}
	    //else {
	    	tsGraphType = getTimeSeriesGraphType(axisGraphType, i);
	    //}
		if ( (axis == GRAxisEdgeType.LEFT) && (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) && (iNotNull == 0) ) {
			// Do nothing.  Don't want the symbol (but do want the string label below.
		}
		else if ( (tsGraphType == TSGraphType.AREA) || (tsGraphType == TSGraphType.AREA_STACKED) ||
		    (tsGraphType == TSGraphType.BAR) || (tsGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
			// Legend symbol is a rectangle with the fill color.
			GRDrawingAreaUtil.fillRectangle ( da_legend, x[0], ylegend, (x[1] - x[0]), ydelta);
		}
		else if ( tsGraphType == TSGraphType.RASTER ) {
			// Don't draw any visual indicator, only the time series identifier, period, etc.
		}
		else {
			// Time series is drawn with lines and/or points.
			//if ( imatch < 0 ) {
				prop_value = getLayeredPropValue("LineWidth", this.subproduct, i, false, null);
			//}
			//else {
			//	prop_value = getLayeredPropValue("LineWidth", this.subproduct, imatch, false, null);
			//}
			if (prop_value != null) {
				if (StringUtil.isInteger(prop_value)) {
					// Have line width to set, get the initial value.
					int lineWidth = Integer.parseInt(prop_value);
					//Message.printStatus(2, routine, "Setting line width for legend, initial width=" + lineWidth );
					if ( isTimeSeriesSelected(ts) ) {
						//Message.printStatus(2, routine, "Time series is selected " + ts.getIdentifierString());
						// Time series is selected in the legend so need to highlight.
						// Change the line width property based on the selection properties.
						lineWidth = modifyLineWidthForSelectedTimeSeries(lineWidth);
						//Message.printStatus(2, routine, "Line width because of selection=" + lineWidth );
					}
					// Set the line width to either the original or the value modified for highlight.
					GRDrawingAreaUtil.setLineWidth( da_legend, lineWidth );
				}
			}

			if ( axisGraphType != TSGraphType.POINT ) {
				// Not point graph type so draw a line.
				if ( line_style.equalsIgnoreCase("Solid") ) {
					GRDrawingAreaUtil.drawLine(da_legend, x, y);
				}
				else if ( line_style.equalsIgnoreCase("Dashed") ) {
					GRDrawingAreaUtil.setLineDash( da_legend, lineDash, 0);
					GRDrawingAreaUtil.drawLine(da_legend, x, y);
					GRDrawingAreaUtil.setLineDash( da_legend, null, 0);
				}
			}

			// Draw the symbol if any is specified.
			//if ( imatch < 0 ) {
				prop_value = this._tsproduct.getLayeredPropValue ( "SymbolStyle", this.subproduct, i, false );
			//}
			//else {
			//	prop_value = this._tsproduct.getLayeredPropValue ( "SymbolStyle", this.subproduct, imatch, false );
			//}
			try {
				shapeType = GRSymbolShapeType.valueOfIgnoreCase(prop_value);
				if ( shapeType == null ) {
					shapeType = GRSymbolShapeType.NONE;
				}
			}
			catch (Exception e) {
				shapeType = GRSymbolShapeType.NONE;
			}
			//if ( imatch < 0 ) {
				symbol_size = StringUtil.atod( this._tsproduct.getLayeredPropValue ( "SymbolSize", this.subproduct, i, false ) );
			//}
			//else {
			//	symbol_size = StringUtil.atod( this._tsproduct.getLayeredPropValue ( "SymbolSize", this.subproduct, imatch, false ) );
			//}
			if ((shapeType != GRSymbolShapeType.NONE) && (symbol_size > 0)){
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias( da_legend, true);
				}
				GRDrawingAreaUtil.drawSymbol (da_legend, shapeType,
					(x[0] + x[1])/2.0, y[0], symbol_size, GRUnits.DEVICE,
					GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
				if (niceSymbols) {
					GRDrawingAreaUtil.setDeviceAntiAlias( da_legend, false);
				}
			}
		}

		// Output the time series name, alias, period, etc.
		da_legend.setColor ( GRColor.black );
		// Put some space so text does not draw right up against symbol.
		GRDrawingAreaUtil.drawText ( da_legend, " " + legend, x[1], ylegend, 0.0, GRText.LEFT|GRText.BOTTOM );

		if ( ts != null ) {
			// Save the limits of the legend text as a hot spot that can be clicked on to highlight the
			// time series during rendering.
			// Get the limits of the text that was actually drawn in drawing units.
			GRLimits textDrawLim = GRDrawingAreaUtil.getTextExtents(da_legend, " " + legend, GRUnits.DEVICE);
			// Lower left corner of the text.
			double xll = da_legend.scaleXData(x[1]);
			//Message.printStatus(2,routine,"Legend x[1]=" + x[1] + " xll=" + xll );
			double yll = da_legend.scaleYData(ylegend);
			//Message.printStatus(2,routine,"Legend ylegend=" + ylegend + " yll=" + yll );
			// Legend limits in device units use Y from top so subtract extents.
			GRLimits legendDrawLimits = new GRLimits(xll,yll,
				(xll + textDrawLim.getWidth()), (yll - textDrawLim.getHeight()) );
			this.__legendTimeSeriesDrawMap.put(ts, legendDrawLimits);
			//Message.printStatus(2,routine,"Legend \"" + legend + "\" drawing limits are " + legendDrawLimits );
		}

		// Decrement the legend for the next iteration, drawing from top to bottom.
		ylegend -= ydelta;
	}
}

/**
 * Draw the legend for raster graph, which shows the color scale for the pixel colors.
 * This is drawn the same whether a single or multiple time series graph.
 * @param tsproduct TSProduct describing the graph.
 */
private void drawLegendRaster ( TSProduct tsproduct ) {
	String routine = getClass().getSimpleName() + ".drawLegendRaster";
	if ( Message.isDebugOn ) {
		Message.printStatus(2, routine, "Drawing the raster legend.");
	}
	// Left y-axis may be only axis used or may be used with right y-axis.
	String legendPosition = this._tsproduct.getLayeredPropValue("RasterGraphLegendPosition", this.subproduct, 0, false);
	if ( (legendPosition == null) || (legendPosition.isEmpty())) {
		// Default to "Right".
		// TODO smalers 2021-08-28 need to make sure the property is set early on when graphing from TSTool UI
		legendPosition = "Right";
	}

	// Figure out which drawing area to use:
	// - currently only support on the right
	GRDrawingArea da_legend;
	GRLimits datalim_legend;
	GRLimits drawlim_legend;
	if ( legendPosition.equalsIgnoreCase("Right") ) {
		da_legend = this._da_right_raster_legend;
		datalim_legend = this._datalim_right_raster_legend;
		drawlim_legend = this._drawlim_right_raster_legend;
	}
	else {
		// Don't know how to handle.
		Message.printWarning(3, routine, "Don't know how to draw the raster legend other than in position Right.");
		return;
	}

	// Get the properties for the legend:
	// - use the normal legend fonts rather than requiring separate properties for the raster legend
	// - 'text_limits' are the extent of the text
	// - 'ydelta' is the y offest between each line in the legend
	String legendFontName = this._tsproduct.getLayeredPropValue ( "LegendFontName", this.subproduct, -1, false );
	String legendFontSize = this._tsproduct.getLayeredPropValue ( "LegendFontSize", this.subproduct, -1, false );
	String legendFontStyle = this._tsproduct.getLayeredPropValue ( "LegendFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( da_legend, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
	GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( da_legend, "TEST STRING", GRUnits.DEVICE );
	double ydelta = text_limits.getHeight();
	text_limits = null;

	// Get the symbol table.
	GRSymbolTable symtable = this.rasterSymbolTable;

	// Draw legend from top down in case run out of room and need to omit some time series.
	// Can center vertically on the following (line will be one font height down and
	// font will be 1/2 height down to top).
	double ylegend = datalim_legend.getTopY() - ydelta;
	String legend = null;
	double x[] = new double[2];
	double y[] = new double[2];
	double xOffset = 0.0;
	double xlegend = datalim_legend.getLeftX();
	double legendLineLength = 25.0; // Pixels width of the legend line (use same dimension for raster).

	x[0] = datalim_legend.getLeftX() + xOffset;
	// Legend drawing limits are in device units so just use pixels.
	x[1] = x[0] + legendLineLength;
	y[0] = ylegend;
	y[1] = ylegend + ydelta;

	// For troubleshooting.
	if ( Message.isDebugOn ) {
		Message.printStatus(2,routine,"Raster da_legend: " + da_legend);
		Message.printStatus(2,routine,"Raster datalim_legend: " + datalim_legend);
		Message.printStatus(2,routine,"Raster drawlim_legend: " + drawlim_legend);
		Message.printStatus(2,routine,"Raster xlegend: " + xlegend);
		Message.printStatus(2,routine,"Raster ylegend: " + ylegend);
		Message.printStatus(2,routine,"Raster ydelta: " + ydelta);
	}

	// Show the units of the first time series.
	// TODO smalers 2025-03-21 figure out why not used.
	String yAxisUnitsProperty = "LeftYAxisUnits"; // Used to look up units for time series.
	PropList overrideProps = null;
	// Get the units from the time series:
	// - depending layout the Y-axis title may have other words
	// - check for consistent units should have happened early on
   	String yAxisUnits = this.__tslist.get(0).getDataUnits();
	legend = " " + yAxisUnits;
	da_legend.setColor ( GRColor.black );
	GRDrawingAreaUtil.drawText ( da_legend, " " + legend, (xlegend + legendLineLength), ylegend, 0.0, GRText.LEFT|GRText.BOTTOM );
	ylegend -= ydelta;

	GRSymbolTableRow row;
	GRColor rowColor;
	for ( int irow = 0; irow < symtable.size(); irow++ ) {
		// Output the time series name, alias, period, etc.
		row = symtable.getSymbolTableRow(irow);
		// Fill in a rectangle with raster pixel color.
		rowColor = row.getFillColor();
		da_legend.setColor ( rowColor );
		GRDrawingAreaUtil.fillRectangle ( da_legend, x[0], ylegend, (x[1] - x[0]), ydelta);
		if ( rowColor.equals(GRColor.white) ) {
			// Also draw a surrounding box.
			da_legend.setColor ( GRColor.black );
			GRDrawingAreaUtil.drawRectangle ( da_legend, x[0], ylegend, (x[1] - x[0]), ydelta);
		}
		// Draw the text.
		da_legend.setColor ( GRColor.black );
		// Put some space so text does not draw right up against symbol:
		// - make sure that the spaces are included in the code that determines the legend width
		legend = " " + row.getValueMinFullString() + " " + row.getValueMaxFullString();
		legend = legend.replace("-Infinity","").replace("Infinity","").replace("NoData NoData","NoData").trim();
		if ( Message.isDebugOn ) {
			Message.printStatus(2,routine,"Drawing \"" + legend + "\" at " + xlegend + "," + ylegend);
		}
		GRDrawingAreaUtil.drawText ( da_legend, " " + legend, (xlegend + legendLineLength), ylegend, 0.0, GRText.LEFT|GRText.BOTTOM );
		// Decrement the y for the next legend line.
		ylegend -= ydelta;
		y[0] -= ydelta;
		y[1] -= ydelta;
	}
}

/**
Draw a box around the graph.
This is normally done after drawing grid lines so the box looks solid, and before drawing data.
*/
private void drawOutlineBox () {
	GRDrawingAreaUtil.setColor ( this._da_lefty_graph, GRColor.black );
	if ( this._is_reference_graph ) {
		// Just draw a box around the graph area to make it more visible.
		// Using GR seems to not always get the line (roundoff)?
		Rectangle bounds = this._dev.getBounds();

		this._graphics.drawRect ( 0, 0, (bounds.width - 1), (bounds.height - 1) );
		return;
	}
	else {
		// Normal drawing area.
		GRDrawingAreaUtil.drawRectangle ( this._da_lefty_graph, this._data_lefty_limits.getMinX(), this._data_lefty_limits.getMinY(),
			this._data_lefty_limits.getWidth(), this._data_lefty_limits.getHeight() );
	}
}

/**
Draw time series annotations, which are related to specific time series.
Currently this method does nothing.
*/
private void drawTimeSeriesAnnotations () {
}

/**
Draw the main and sub titles for the graph.
The properties are retrieved again in case they have been reset by a properties GUI.
*/
private void drawTitles ( ) {
	if ( this._is_reference_graph ) {
		return;
	}

	// Main title.

	this._da_maintitle.setColor ( GRColor.black );
	String maintitle_font = this._tsproduct.getLayeredPropValue ("MainTitleFontName", this.subproduct, -1, false );
	String maintitle_fontstyle = this._tsproduct.getLayeredPropValue ("MainTitleFontStyle", this.subproduct, -1, false );
	String maintitle_fontsize = this._tsproduct.getLayeredPropValue ("MainTitleFontSize", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_maintitle, maintitle_font,
		maintitle_fontstyle, StringUtil.atod(maintitle_fontsize) );
	String maintitle_string = this._tsproduct.expandPropertyValue(
	    this._tsproduct.getLayeredPropValue ( "MainTitleString", this.subproduct, -1, false));
	GRDrawingAreaUtil.drawText ( this._da_maintitle, maintitle_string, this._datalim_maintitle.getCenterX(),
		this._datalim_maintitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Sub title.

	this._da_subtitle.setColor ( GRColor.black );
	String subtitle_font = this._tsproduct.getLayeredPropValue ( "SubTitleFontName", this.subproduct, -1, false );
	String subtitle_fontstyle = this._tsproduct.getLayeredPropValue ( "SubTitleFontStyle", this.subproduct, -1, false );
	String subtitle_fontsize = this._tsproduct.getLayeredPropValue ( "SubTitleFontSize", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_subtitle, subtitle_font, subtitle_fontstyle, StringUtil.atod(subtitle_fontsize) );
	String subtitle_string = this._tsproduct.expandPropertyValue(
	    this._tsproduct.getLayeredPropValue ( "SubTitleString", this.subproduct, -1, false));
	GRDrawingAreaUtil.drawText ( this._da_subtitle, subtitle_string, this._datalim_subtitle.getCenterX(),
		this._datalim_subtitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );
}

/**
Draw (render) a single time series on the graph with no override properties.
@param tsproduct time series product
@param its the time series list position (0+, for retrieving properties and messaging)
@param ts the time series to render
@param graphType the graph type to use for the time series
*/
private void drawTS ( TSProduct tsproduct, int its, TS ts, TSGraphType graphType ) {
	// No override properties.
	drawTS(tsproduct, its, ts, graphType, null);
}

/**
Draw a single time series.
@param tsproduct time series product
@param its the time series list position (0+, for retrieving properties and messaging)
@param ts Single time series to draw.
@param tsGraphType the graph type to use for the time series, typically the same as the
graph type, but can be different if overlaying lines on area graph, for example.
@param overrideProps run-time override properties to consider when getting graph properties
*/
private void drawTS ( TSProduct tsproduct, int its, TS ts, TSGraphType tsGraphType, PropList overrideProps ) {
    String routine = getClass().getSimpleName() + ".drawTS";
	if ( Message.isDebugOn ) {
		Message.printStatus(2, routine, this._gtype + "Enter drawTS for TSID=" + ts.getIdentifierString() + " TSAlias=" + ts.getAlias());
	}
	if ((ts == null) || !ts.hasData() || (!this._is_reference_graph && !ts.getEnabled())) {
	    // No need or unable to draw.
		return;
	}
	// First check for graph types that have their own rendering method.
    // Take a new approach for the area graph by having a separate method.
	// This will duplicate some code, but the code below is getting too complex with multiple graph types handled in the same code.
	// The separate renderers also can be refactored into separate classes if appropriate.
	if ( (tsGraphType == TSGraphType.AREA) || (tsGraphType == TSGraphType.AREA_STACKED) ) {
	    drawTSRenderAreaGraph ( tsproduct, its, ts, tsGraphType, overrideProps );
	    return;
	}

	// Figure out if the graph is being drawn on the left or right axis.
	boolean drawUsingLeftYAxis = true; // Simplify logic to know when drawing using left Y-axis.
	boolean drawUsingRightYAxis = false; // Simplify logic to know when drawing using right Y-axis.
	GRDrawingArea daGraph = this._da_lefty_graph; // Drawing area to use, depending on whether left or right Y-axis is used.
	String yAxis = getLayeredPropValue( "YAxis", this.subproduct, its, false, overrideProps);
	TSGraphType yAxisGraphType = this.__leftYAxisGraphType; // Graph type for y-axis corresponding to time series.
	TSLimits tslimits = this._tslimits_lefty; // Time series limits for y-axis corresponding to time series.
	GRLimits dataLimits = this._data_lefty_limits; // Data limits for y-axis corresponding to time series.
	boolean ignoreYAxisUnits = this._ignoreLeftAxisUnits;
	String yAxisUnitsProperty = "LeftYAxisUnits"; // Used to look up units for time series.
	if ( (yAxis != null) && yAxis.equalsIgnoreCase("right") ) {
		drawUsingLeftYAxis = false;
		drawUsingRightYAxis = true;
		daGraph = this._da_righty_graph;
		yAxisGraphType = this.__rightYAxisGraphType;
		tslimits = this._tslimits_righty;
		dataLimits = this._data_righty_limits;
		ignoreYAxisUnits = this._ignoreRightAxisUnits;
		yAxisUnitsProperty = "RightYAxisUnits";
	}

	if ( (ts.getDataIntervalBase() == TimeInterval.IRREGULAR) &&
		((yAxisGraphType == TSGraphType.PERIOD) || (yAxisGraphType == TSGraphType.RASTER)) ) {
		// Can't draw irregular time series in period of record or raster graph.
		return;
	}

	GRColor tscolor = drawTSHelperGetTimeSeriesColor ( its, overrideProps );
    daGraph.setColor(tscolor);

    // Start and end are the same regardless of whether left or right Y-axis because X-axis is shared.
	DateTime start = drawTSHelperGetStartDateTime ( ts );
	DateTime end = drawTSHelperGetEndDateTime ( ts );

	/* Can uncomment for debug purposes on start and end dates.
	Message.printStatus(2, "",
	"----------------------------------------------------------");
	String tsStatus = "TS:" + (ts.getIdentifier()).getIdentifier() + " GRAPH_TYPE:" + this._gtype;
	String tsInfo   = "DIFF:" + diff.toString() + "  Interval:" +
	ts.getDataIntervalBase() + "   Mult:" + ts.getDataIntervalMult();
	Message.printStatus(2, "", tsInfo);
	Message.printStatus(2, "", tsStatus);
	String timeStatus = "START   " + this._start_date.toString() + "   " +
	start.toString() + "    END:" + end.toString();
	Message.printStatus(2, routine, timeStatus);
	Message.printStatus(2, "",
	"----------------------------------------------------------");
	*/

	if (Message.isDebugOn) {
		Message.printDebug(1, routine, this._gtype + "Drawing time series " + start + " to " + end +
		    " global period is: " + this._start_date + " to " + this._end_date + " yaxis=" + yAxis);
	}
	// TODO sam 2017-02-05 remove and use debug when confident things are working.
	//Message.printStatus(2, routine, this._gtype + "Drawing time series " + start + " to " + end +
	//	" global period is: " + this._start_date + " to " + this._end_date + " yaxis=" + yAxis);

	// Only draw the time series if the units are being ignored or can be converted.
	// The left axis units are determined at construction.

	if (!ignoreYAxisUnits) {
		if ( (yAxisGraphType != TSGraphType.DURATION) && (yAxisGraphType != TSGraphType.XY_SCATTER) ) {
		   	String yAxisUnits = getLayeredPropValue( yAxisUnitsProperty, this.subproduct, -1, false, overrideProps);

			//Message.printStatus(2, routine, this._gtype + "Checking time series units \""
			//	+ ts.getDataUnits() + "\" against y axis units \"" + yAxisUnits + "\"");
			if (!DataUnits.areUnitsStringsCompatible(ts.getDataUnits(),yAxisUnits,true)) {
				if (yAxisUnits.equals("")) {
					// New graph.  Set the units to those of the first time series.
					int how_set_prev = tsproduct.getPropList().getHowSet();
					tsproduct.getPropList().setHowSet( Prop.SET_AS_RUNTIME_DEFAULT);
					tsproduct.setPropValue(yAxisUnitsProperty, ts.getDataUnits(), this.subproduct, -1);
					tsproduct.getPropList().setHowSet(how_set_prev);
				}
				else {
					// No units, so can't draw the graph.
					return;
				}
			}
		}
	}

	// Previous point is used to connect the point.
	double yPrev = ts.getMissing();
	boolean yPrevIsMissing = true;
	double xPrev = ts.getMissing();
	// Previous date is used to check irregular time series for allowed gap between points.
	DateTime datePrev = null;
	// Next point is used if step is used with the next value.
	double yNext = ts.getMissing();
	boolean yNextIsMissing = true;
	double x;
	double y;
	boolean yIsMissing = false;
	int drawcount = 0; // Overall count of points in time series.
	int pointsInSegment = 0; // Points in current line segment being drawn.
	int interval_base = ts.getDataIntervalBase();
	int interval_mult = ts.getDataIntervalMult();

	String prop_value;

	GRSymbolShapeType symbolNoFlag = drawTSHelperGetSymbolStyle ( its, overrideProps, false );
	GRSymbolShapeType symbolWithFlag = drawTSHelperGetSymbolStyle ( its, overrideProps, true );
	GRSymbolShapeType symbol = symbolNoFlag; // Default symbol to use for a specific data point, checked below.
	//Message.printStatus ( 2, routine, this._gtype + "symbolNoFlag=" + symbolNoFlag + " symbolWithFlag=" + symbolWithFlag );
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
	label_format = getLayeredPropValue("DataLabelFormat", this.subproduct, its, false, overrideProps);
	if (label_format == null || label_format.equals("")) {
		// Try to get from the graph properties.
		label_format = getLayeredPropValue("DataLabelFormat", this.subproduct, -1, false, overrideProps);
		if (!label_format.equals("")) {
			// Label the format
			labelPointWithFlag = true;
			graphLabelFormatSet = true;
		}
	}
	else {
		labelPointWithFlag = true;
	}

	// If the label format has been set in the time series properties, the time series label position will be used, too.
	// If the label format has been set in the graph properties, the graph's label position should be used.
	//
	// What is not covered explicitly, though,
	// is what happens if the label format has been set in both the time series and the graph.
	// Currently, the time series properties will override the graph level properties.
	// As discussed with SAM, this may not be the desired behavior, but for now it will be the graph's behavior.

	if (labelPointWithFlag) {
		// Are drawing point labels so get the position, set the font, and get the format.
		label_position_string = getLayeredPropValue("DataLabelPosition", this.subproduct, its, false, overrideProps);
		// Determine the label position automatically, if necessary.
		if (graphLabelFormatSet || label_position_string.equals("") || label_position_string.equalsIgnoreCase("Auto")) {
			// Try to get from the graph properties.
			label_position_string = getLayeredPropValue( "DataLabelPosition", this.subproduct, -1, false, overrideProps);
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
		String fontname = getLayeredPropValue("DataLabelFontName", this.subproduct, -1, false, overrideProps);
		String fontsize = getLayeredPropValue("DataLabelFontSize", this.subproduct, -1, false, overrideProps);
		String fontstyle = getLayeredPropValue("DataLabelFontStyle", this.subproduct, -1, false, overrideProps);
		GRDrawingAreaUtil.setFont(daGraph, fontname, fontstyle, StringUtil.atod(fontsize));

		// Determine the format for the data value in case it is needed to format the label.
		label_units = ts.getDataUnits();
		label_value_format = DataUnits.getOutputFormatString(label_units, 0, 4);
	}

	// Bar graph parameters.
	double bar_width = 0.0; // Width actually drawn (single bar).
	double bar_width_d2 = 0.0; // bar_width/2.
	double full_bar_width = 0.0; // Width used for positioning (may be multiple bars if no overlap).
	double full_bar_width_d2 = 0.0; // full_bar_width/2.
	double maxy = 0.0;
	double miny = 0.0;

	int bar_position = 0; // position 0 means centered on the date, -1 to left, 1 to right.
	prop_value = getLayeredPropValue("BarPosition", this.subproduct, -1, false, overrideProps);
	if (prop_value != null) {
		if (prop_value.equalsIgnoreCase("LeftOfDate")) {
			bar_position = -1;
		}
		else if (prop_value.equalsIgnoreCase("RightOfDate")) {
			bar_position = 1;
		}
	}

    boolean barOverlap = false; // Whether bars are overlapped.
    prop_value = getLayeredPropValue("BarOverlap", this.subproduct, -1, false, overrideProps);
    if ( (prop_value != null) && prop_value.equalsIgnoreCase("True")) {
        barOverlap = true;
    }

	// Generate the clipping area that will be set so that no data are drawn outside of the graph.
	Shape clip = GRDrawingAreaUtil.getClip(daGraph);
	GRDrawingAreaUtil.setClip(daGraph, daGraph.getDataLimits());

	// If a bar graph, the bar width is the data interval/nts (if barOverlap=false) or interval (if barOverlap=true).
	// Rather than compute a bar width that may vary some with the plot zoom, always draw filled in and draw a border.
	// The position of the bar is determined by the "BarPosition" property.
	// Only include time series that are not null and will be drawn.

	boolean includeNulls = false;
	int nts = getTSListForAxes ( drawUsingLeftYAxis, drawUsingRightYAxis, includeNulls ).size();

	if ( drawUsingLeftYAxis && (yAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) && (this.__tslist != null) ) {
		int numReg = this.__tslist.size() - 1;
		nts = 0;
		for (int i = 0; i < numReg; i++) {
			if ( isTSEnabled(i + 1) ) {
				nts++;
			}
		}
	}

	boolean draw_bounding_rectangle = true;
	boolean draw_line = true;

	boolean niceSymbols = true;
	int lineWidth = 1;

	prop_value = getLayeredPropValue("LineStyle", this.subproduct, its, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("None")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("GraphType", this.subproduct, -1, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Point")) {
		draw_line = false;
	}

	prop_value = getLayeredPropValue("SymbolAntiAlias", -1, -1, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("False")) {
		niceSymbols = false;
	}

	GRLineConnectType lineConnectType = GRLineConnectType.CONNECT;
	if (this._is_reference_graph) {
		lineWidth = 1;
		draw_line = true;
	}
	else {
		prop_value = getLayeredPropValue("LineWidth", this.subproduct, its, false, overrideProps);
		if (prop_value != null) {
			if (StringUtil.isInteger(prop_value)) {
				lineWidth = Integer.parseInt(prop_value);
				if (lineWidth < 0) {
					lineWidth = 1;
				}
				if ( isTimeSeriesSelected(ts) ) {
					// Time series is selected in the legend so need to highlight.
					// Change the line width property based on the selection properties.
					lineWidth = modifyLineWidthForSelectedTimeSeries(lineWidth);
				}
			}
		}

		prop_value = getLayeredPropValue("LineConnectType", this.subproduct, its, false, overrideProps);
		lineConnectType = GRLineConnectType.valueOfIgnoreCase(prop_value);
		if ( lineConnectType == null ) {
			lineConnectType = GRLineConnectType.CONNECT; // Default.
		}

		if ( lineConnectType == GRLineConnectType.STEP_AUTO ) {
			// Determine the line connect type based on the time series interval.
			if ( interval_base == TimeInterval.IRREGULAR ) {
				lineConnectType = GRLineConnectType.STEP_USING_VALUE;
			}
			else {
				if ( ts.getDataIntervalBase() >= TimeInterval.DAY ) {
					// Date only:
					// - carry forward the value
					lineConnectType = GRLineConnectType.STEP_USING_VALUE;
				}
				else {
					// Date and time:
					// - use the interval ending value
					lineConnectType = GRLineConnectType.STEP_USING_NEXT_VALUE;
				}
			}
		}
	}

	// Only line dashes are currently supported (not dots).
	boolean dashedLine = false;
	double[] lineDash = null;
	prop_value = getLayeredPropValue("LineStyle", this.subproduct, its, false, overrideProps);
	if (prop_value != null && prop_value.equalsIgnoreCase("Dashed")) {
		dashedLine = true;
		lineDash = new double[2];
		lineDash[0] = 3;
		lineDash[1] = 5;
	}

	if ( (yAxisGraphType == TSGraphType.BAR) || (yAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		DateTime temp_date = new DateTime(tslimits.getDate1());
		// Convert date to a double.
		full_bar_width = temp_date.toDouble();
		// Subtract from the date.
		if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
			// Use largest number of days in month to prevent overlap.
			// Need to use day precision to make this work.
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
		miny = dataLimits.getMinY();
		maxy = dataLimits.getMaxY();

		// Account for the number of time series.
		if ( barOverlap == false ) {
		    full_bar_width /= nts;
		}

		// If bar width is <= 5 pixels in device units, do not draw bounding rectangle because it will hide the data.
		if ((daGraph.scaleXData(full_bar_width) - daGraph.scaleXData(0.0)) <= 5.0) {
			draw_bounding_rectangle = false;
		}
	}

	// Use the same plotting width as the position width for all but monthly since months have different numbers of days.
	if (ts.getDataIntervalBase() == TimeInterval.MONTH) {
		// No need for separator since rectangle is smaller.
		draw_bounding_rectangle = false;
		// This gives a little space between the bars.
		bar_width = full_bar_width*.85;
	}
	else {
		bar_width = full_bar_width;
	}

	full_bar_width_d2 = full_bar_width/2.0;
	bar_width_d2 = bar_width/2.0;

	// If set to true, always draw at least a line for the bar.
	// This makes sure that all the data are shown.  It is not that computationally intensive to do.
	// Make a property later?  REVISIT

	boolean always_draw_bar = true;
	double leftx = 0.0;	// Left edge of a bar.
	double centerx = 0.0;	// Center of a bar.
    String dataFlag = null;

	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the list of data points.
		// Currently do not use TSIterator because head-to-head performance tests have not been performed.
		// Need to do so before deciding which approach is faster.
		IrregularTS irrts = (IrregularTS)ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series.
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		// Whether need to skip drawing a line over a gap for irregular interval time series.
		boolean doGap = false;

		// Get whether checking the LineConnectAllowedGap:
		// - this is only used with irregular data
		prop_value = getLayeredPropValue("LineConnectAllowedGap", this.subproduct, its, false, overrideProps);
		int lineConnectAllowedGapSeconds = -1;
		if ( (prop_value == null) || prop_value.isEmpty() && ts.isIrregularInterval() ) {
			// The allowed gap was not specified so default based on the time series precision.
			prop_value = getDefaultLineConnectAllowedGap ( ts );
		}
		if ( (prop_value != null) && !prop_value.isEmpty() ) {
			try {
				TimeInterval lineConnectAllowedInterval = TimeInterval.parseInterval(prop_value);
				lineConnectAllowedGapSeconds = lineConnectAllowedInterval.toSeconds();
				Message.printStatus(2, routine, "lineConnectAllowedGapSeconds=" + lineConnectAllowedGapSeconds);
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Value of LineConnnectAllowedGap (" + prop_value +
					") is not a valid time interval - ignoring.");
			}
		}

		//Message.printStatus(2,routine,"Starting to draw time series.");
		for ( int i = 0; i < nalltsdata; i++ ) {
        	tsdata = alltsdata.get(i);
        	date = tsdata.getDate();
        	if (date.greaterThan(end)) {
        		// Past the end of where want to go so quit.
        		break;
        	}

        	doGap = false;
        	if ( lineConnectAllowedGapSeconds > 0 ) {
        		if ( (date != null) && (datePrev != null) ) {
        			// If the current date/time is > than the previous by more than the allowed gap, treat as a moveTo.
        			long dateSeconds = TimeUtil.absoluteSecond ( date );
        			long datePrevSeconds = TimeUtil.absoluteSecond ( datePrev );
        			//Message.printStatus(2, routine, "Date=" + date + " dateSeconds=" + dateSeconds + " datePrevSeconds=" + datePrevSeconds);
        			if ( (dateSeconds - datePrevSeconds) > lineConnectAllowedGapSeconds ) {
        				// Gap is longer than the allowed:
        				// - set the boolean to treat as a gap below
        				doGap = true;
        			}
        			//Message.printStatus(2, routine, "doGap=" + doGap);
        		}
        	}

            // TODO (JTS - 2006-04-26) All data flags (returned from getDataFlag()) are being trimmed below.
            // In the future, if the spacing of data flags becomes critical, this may need revisited.
        	if (date.greaterThanOrEqualTo(start)) {
        		y = tsdata.getDataValue();
        		yIsMissing = ts.isDataMissing(y);
        		//Message.printStatus(2, routine,"Date=" + date + " yIsMissing=" + yIsMissing);
        		if ( yIsMissing ) {
	        		if ( this.useXYCache ) {
	        			// Missing point is found. If pointsInSegment > 0, draw the line segment.
	        			if ( pointsInSegment > 0 ) {
	        				//Message.printStatus(2, routine, "Found missing value - drawing segment with " + pointsInSegment + " points.");
	        				GRDrawingAreaUtil.drawPolyline ( daGraph, pointsInSegment, this.xCacheArray, this.yCacheArray );
		        			pointsInSegment = 0;
	        			}
	        		}
	        		else {
	        			// No need to draw anything.
	        			// Need the x-value for the continue below, used for step line connections.
	        			x = date.toDouble();
        				xPrev = x;
	        			yPrev = y;
	        			yPrevIsMissing = yIsMissing;
	        			datePrev = date;
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

                if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
                	// Also need the next value.
                	TSData tsdataNext = tsdata.getNext();
                	if ( tsdataNext == null ) {
                		yNext = ts.getMissing();
                		yNextIsMissing = true;
                	}
                	else {
                		yNext = tsdataNext.getDataValue();
                		yNextIsMissing = ts.isDataMissing(yNext);
                	}
                }

        		// Else, see if need to moveTo or lineTo the point.
                x = date.toDouble();
        		if ( this.useXYCache ) {
        			if ( this.xCacheArray == null ) {
        				// Allocate initial cache arrays
        				this.xCacheArray = new double[100];
        				this.yCacheArray = new double[100];
        			}
        		}
        		if (((drawcount == 0) || doGap || ts.isDataMissing(yPrev)) && (yAxisGraphType != TSGraphType.BAR &&
        		    yAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) {
        			// First point in the entire time series or first non-missing point after a missing point.
        			// Always draw the symbol.
                    //if (tsdata != null)
                    //Message.printStatus(1, "", "JTS0" + date + ": '" + tsdata.getDataFlag()
                    //	+ "'  '" + label_position + "'  '" + y + "'");
        			if ( this._is_reference_graph ) {
        				// Don't draw symbols.
        			}
        			else if (labelPointWithFlag && ((symbol == GRSymbolShapeType.NONE) || (symbol_size <= 0))) {
        				// Text only.
        				GRDrawingAreaUtil.drawText(daGraph,
        				    TSData.toString(label_format,label_value_format, date, y, 0.0,
        					tsdata.getDataFlag().trim(),label_units),
        					x, y, 0.0, label_position);
        			}
        			else if (labelPointWithFlag) {
        				if (niceSymbols) {
        					GRDrawingAreaUtil.setDeviceAntiAlias(daGraph, true);
        				}

        				// Text and symbol.
        				GRDrawingAreaUtil.drawSymbolText(daGraph, symbol, x, y, symbol_size,
        					TSData.toString(label_format,label_value_format, date, y, 0.0,
        					tsdata.getDataFlag().trim(), label_units),
        					0.0, label_position, GRUnits.DEVICE,
        					GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );

        				if (niceSymbols) {
        					// Turn off anti-aliasing so that it only applies for symbols.
        					GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
        				}
        			}
        			else {
        				// Symbol only.
        				if (niceSymbols) {
        					GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
        				}

        				GRDrawingAreaUtil.drawSymbol(daGraph, symbol, x, y, symbol_size,
        				    GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );

        				if (niceSymbols) {
        					// Turn off anti-aliasing so that it only applies for symbols.
        					GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
        				}
        			}

        			// First point or skipping data. Put second so symbol coordinates do not set the last point.
        			if ( this.useXYCache ) {
        				//Message.printStatus(2, routine, "Initializing first point in segment to " + x + "," + y);
	        			this.xCacheArray[0] = x;
	        			this.yCacheArray[0] = y;
	        			pointsInSegment = 1;
        			}
        			else {
        				// Move/draw each point individually.
        				if ( lineConnectType == GRLineConnectType.CONNECT ) {
        					GRDrawingAreaUtil.moveTo(daGraph, x, y );
        					// Connect from the previous point to the current point:
        					//
        					//       + y
        					//
        					//
        					//    + yPrev (missing)
        					//
        					GRDrawingAreaUtil.moveTo(daGraph, x, y);
        				}
        				else if ( lineConnectType == GRLineConnectType.STEP_USING_VALUE ) {
        					// Position the point to carry forward the interval starting value.
        					//
        					//       + yNext
        					//	     |
        					//       |
        					//    +--+
        					//   y
        					//
        					GRDrawingAreaUtil.moveTo(daGraph, x, y);
        				}
        				else if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
        					// Position the point to carry forward the interval ending value.
        					//
        					//    +--+ yNext
        					//    |
        					//    |
        					//    +
        					//    y
        					//
        					if ( !yNextIsMissing ) {
        						GRDrawingAreaUtil.moveTo(daGraph, x, yNext);
        					}
        				}
        			}
        		}
        		else {
        			// Draw the line segment or bar.
        			if (yAxisGraphType != TSGraphType.BAR && yAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        				if (draw_line) {
        					GRDrawingAreaUtil.setLineWidth( daGraph, lineWidth);

        					if (dashedLine) {
        						GRDrawingAreaUtil.setLineDash( daGraph, lineDash, 0);
        					}

        					if ( this.useXYCache ) {
        						// Save the point but do not draw yet.
        						if ( pointsInSegment == this.xCacheArray.length ) {
        							// Increase the size of the cache arrays.
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
        						// Drawing each point with LineTo.
        						if ( lineConnectType == GRLineConnectType.CONNECT ) {
        							// Connect from the previous point to the current point:
        							//
        							//       + y
        							//      /
        							//     /
        							//    + yPrev
        							//
        							GRDrawingAreaUtil.lineTo ( daGraph, x, y );
        						}
        						else if ( lineConnectType == GRLineConnectType.STEP_USING_VALUE ) {
        							// Connect from the previous point to the current point:
        							// - current point will be carried forward in the next draw
        							//
        							//       + y
        							//       |
        							//       |
        							//    +--+
        							//  yPrev
        							//
        							if ( yPrevIsMissing ) {
        								GRDrawingAreaUtil.moveTo ( daGraph, x, y );
        							}
        							else {
        								GRDrawingAreaUtil.lineTo ( daGraph, x, yPrev );
        								GRDrawingAreaUtil.lineTo ( daGraph, x, y );
        							}
        						}
        						else if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
        							// Connect from the previous point to the current point:
        							// - previous line will have drawn to current X at the iteration
        							// - current point will be carried forward in the next draw
        							//
        							//      C--D yNext
        							//      |
        							//      |
        							//  A---B
        							//      y
        							//
        							// Horizontal line A-B.
        							if ( !yPrevIsMissing ) {
        								GRDrawingAreaUtil.lineTo ( daGraph, x, yPrev );
        							}
        							// Vertical line B-C.
        							if ( !yNextIsMissing ) {
        								GRDrawingAreaUtil.lineTo ( daGraph, x, yNext );
        							}
        						}
        					}

        					// Reset the line width to the normal setting for all other drawing.
        					GRDrawingAreaUtil.setLineWidth(	daGraph, 1);

        					if (dashedLine) {
        						// Reset the line dash so that only this line is drawn dashed.
        						GRDrawingAreaUtil.setLineDash( daGraph, null, 0);
        					}
        				}
        				else {
        					// No line to draw, so simply move the position of the stylus.
        					GRDrawingAreaUtil.moveTo(daGraph, x, y);
        				}
        				if ( this._is_reference_graph ) {
        					// No symbol or label to draw.
        				}
        				else if (labelPointWithFlag && ((symbol == GRSymbolShapeType.NONE) || (symbol_size <= 0))) {
        					// Just text.
        					GRDrawingAreaUtil.drawText(daGraph,
        						TSData.toString(label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
        						label_units), x, y, 0.0, label_position);
        				}
        				else if (labelPointWithFlag) {
        					if (niceSymbols) {
        						GRDrawingAreaUtil.setDeviceAntiAlias(daGraph, true);
        					}

        					// Symbol and label.
        					GRDrawingAreaUtil.drawSymbolText( daGraph, symbol, x, y, symbol_size,
        					    TSData.toString(label_format,label_value_format,date, y, 0.0, tsdata.getDataFlag().trim(),
        						label_units), 0.0, label_position, GRUnits.DEVICE,
        					    GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
        					if (niceSymbols) {
        						// Turn off anti-aliasing so it doesn't affect anything else.
        						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
        					}
        				}
        				else {
        					// Just symbol.
        					if (niceSymbols) {
        						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
        					}
        					GRDrawingAreaUtil.drawSymbol(daGraph, symbol, x, y, symbol_size,
        						GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
        					if (niceSymbols) {
        						// Turn off anti-aliasing so it doesn't affect anything else.
        						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
        					}
        				}

        				// Need because symbol coordinates have set the last point.
        				if ( !this.useXYCache ) {
        					GRDrawingAreaUtil.setLineWidth( daGraph, lineWidth);
    						if (dashedLine) {
        						GRDrawingAreaUtil.setLineDash( daGraph, lineDash, 0);
        					}
        					GRDrawingAreaUtil.moveTo(daGraph, x, y);
        				}
        				xPrev = x;
        				yPrev = y;
	        			yPrevIsMissing = yIsMissing;
	        			datePrev = date;
        				++drawcount;
        				continue;
        			}

        			// If get to here need to draw the line or bar.
        			// Shift the bars according to the BarPosition and BarOverlap properties.
        			if (bar_position == -1) {
        				// All bars left of date.
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

        			if ((leftx >=dataLimits.getMinX()) && ((leftx + bar_width) <= dataLimits.getMaxX())){
        				daGraph.setColor(tscolor);
        				if (y >= 0.0) {
        					// Positive bars.
        					if (miny >= 0.0) {
        						// From miny up.
        						GRDrawingAreaUtil.fillRectangle( daGraph, leftx, miny, bar_width, (y - miny));
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, miny, leftx, y);
        						}
        					}
        					else {
        						// From zero up.
        						GRDrawingAreaUtil.fillRectangle( daGraph, leftx, 0.0, bar_width, y);
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, 0.0, leftx, y);
        						}
        					}
        				}
        				else {
        					// Negative bars.
        					if (maxy >= 0.0) {
        						// Up to zero.
        						GRDrawingAreaUtil.fillRectangle( daGraph, leftx, y, bar_width, -y);
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, y, leftx, 0.0);
        						}
        					}
        					else {
        						// Up to top negative value.
        						GRDrawingAreaUtil.fillRectangle( daGraph, leftx, y, bar_width, (maxy - y));
        						if (always_draw_bar) {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, y, leftx, maxy);
        						}
        					}
        				}

        				GRDrawingAreaUtil.setColor(daGraph, this._background);

        				if (draw_bounding_rectangle) {
        					if (y >= 0.0) {
        						if (miny >= 0.0) {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, miny, leftx, y);
        						}
        						else {
        							GRDrawingAreaUtil.drawLine(	daGraph, leftx, 0.0, leftx, y);
        						}
        					}
        					else {
        						if (maxy >= 0.0) {
        							GRDrawingAreaUtil.drawLine(	daGraph, leftx, 0.0, leftx, y);
        						}
        						else {
        							GRDrawingAreaUtil.drawLine( daGraph, leftx, maxy, leftx, y);
        						}
        					}
        				}
        			}
        		}
        		// Set the previous values from the current to use in the next iteration.
        		xPrev = x;
        		yPrev = y;
       			yPrevIsMissing = yIsMissing;
       			datePrev = date;
        		++drawcount;
        	}
		}
    	// If here and the last line segment was not drawn, draw it.
    	//Message.printStatus(2,routine,"pointsInSegment="+pointsInSegment + " yIsMissing="+yIsMissing);
		if ( this.useXYCache && ((yAxisGraphType != TSGraphType.BAR) && (yAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) &&
			(pointsInSegment > 0) && !yIsMissing) {
			// This point is the last in the data so need to draw the line
			// Missing point is found. If the number of points is > 0, draw the line
			GRDrawingAreaUtil.drawPolyline ( daGraph, pointsInSegment, this.xCacheArray, this.yCacheArray );
		}
	}
	else {
		// Regular interval time series:
		// - loop using addInterval
		DateTime date = new DateTime(start);
		// Used if drawing steps and connect type is STEP_USING_NEXT_VALUE:
		// - always one interval after the current date/time
		DateTime dateNext = new DateTime(start);

		// Make sure the time zone is not set.
		date.setTimeZone("");

		TSData tsdata = new TSData();
		// Define a boolean to increase performance below:
		// - default is not to draw the point symbol
		// - symbol will be determined from "symbolNoFlag" value
		boolean doDataPoint = false;
		if ( labelPointWithFlag || (symbolNoFlag != symbolWithFlag) ) {
		    // Symbol will be determined by checking the flag, which is in the data point.
		    doDataPoint = true;
		}
		for (; date.lessThanOrEqualTo(end); date.addInterval(interval_base, interval_mult)) {
			// The starting date/time is the same as 'date' so increment to set to the end of interval.
			dateNext.addInterval(interval_base, interval_mult);

			// Use the actual data value.
			if ( doDataPoint ) {
				tsdata = ts.getDataPoint(date, tsdata);
				y = tsdata.getDataValue();
				yIsMissing = ts.isDataMissing(y);
				dataFlag = tsdata.getDataFlag();
				if ( (dataFlag == null) || (dataFlag.length() == 0) ) {
				    symbol = symbolNoFlag;
				}
				else {
				    symbol = symbolWithFlag;
				}
			}
			else {
			    // No text and no need to check flags for symbol.
				y = ts.getDataValue(date);
				yIsMissing = ts.isDataMissing(y);
				dataFlag = null;
			}

			if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
				// Also need the next value.
				yNext = ts.getDataValue(dateNext);
				yNextIsMissing = ts.isDataMissing(yNext);
			}

			if ( ts.isDataMissing(y) ) {
				xPrev = date.toDouble();
				yPrev = ts.getMissing();
       			yPrevIsMissing = true;
				continue;
			}

			if (yAxisGraphType == TSGraphType.PERIOD) {
				// Reset to use the plotting position of the time series, which will result in a horizontal line.
				// Want the y position to result in the same order as in the legend,
				// where the first time series is at the top of the legend.
				// This is accomplished by reversing the Y axis for plotting
				y = its + 1;
			}

			// Else, see if need to moveTo or lineTo the point.
			x = date.toDouble();

			// Uncomment this for debugging.
			//Message.printStatus(1, routine, "its=" + its + " date = " + date + " x = " + x + " y=" + y);

			if (((drawcount == 0) || yPrevIsMissing) && (yAxisGraphType != TSGraphType.BAR
			    && yAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL)) {
				// Previous point was missing (or first point drawn)
				// so all need to do is draw the symbol (if not a reference graph).
				if ( this._is_reference_graph ) {
					// Don't label or draw symbol.
				}
				else if ( labelPointWithFlag ) {
				    if ( (symbol == GRSymbolShapeType.NONE) || (symbol_size <= 0) ) {
    					// Just text.
    					GRDrawingAreaUtil.drawText(daGraph,
    						TSData.toString(label_format,
    						label_value_format, date, y, 0.0,
    						tsdata.getDataFlag().trim(), label_units), x, y, 0.0, label_position);
    				}
    				else {
    					// Symbol and label.
    					if (niceSymbols) {
    						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
    					}
    					GRDrawingAreaUtil.drawSymbolText( daGraph, symbol, x, y, symbol_size,
    						TSData.toString ( label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
    						label_units ), 0.0, label_position,
    						GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
    					if (niceSymbols) {
    						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be.
    						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
    					}
    				}
				}
				else {
					// Just symbol.
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias(daGraph, true);
					}
					GRDrawingAreaUtil.drawSymbol(daGraph, symbol, x, y, symbol_size,
						GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be.
						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
					}
				}

				// Moved to the next point.
				if ( lineConnectType == GRLineConnectType.CONNECT ) {
					// Connect from the previous point to the current point:
					//
					//       + y
					//
					//
					//    + yPrev (missing)
					//
					GRDrawingAreaUtil.moveTo(daGraph, x, y);
				}
				else if ( lineConnectType == GRLineConnectType.STEP_USING_VALUE ) {
					// Position the point to carry forward the interval starting value.
					//
					//       + yNext
					//       |
					//       |
					//    +--+
					//   y
					//
					GRDrawingAreaUtil.moveTo(daGraph, x, y);
				}
				else if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
					// Position the point to carry forward the interval ending value.
					//
					//    +--+ yNext
					//    |
					//    |
					//    +
					//    y
					//
					GRDrawingAreaUtil.moveTo(daGraph, x, yNext);
				}
				xPrev = x;
				yPrev = y;
				yPrevIsMissing = yIsMissing;
				++drawcount;
				continue;
			}

			// If here, need to draw the line segment from the previous point or draw a bar.

			if ( (yAxisGraphType != TSGraphType.BAR) && (yAxisGraphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
				// Drawing line segment to connect points.
				if (draw_line) {
					if (dashedLine) {
						// Turn on the dash dashes.
						GRDrawingAreaUtil.setLineDash( daGraph, lineDash, 0);
					}

					// Draw the line.
					GRDrawingAreaUtil.setLineWidth(	daGraph, lineWidth);
					if ( lineConnectType == GRLineConnectType.CONNECT ) {
						// Connect from the previous point to the current point:
						//
						//       + y
						//      /
						//     /
						//    + yPrev
						//
						GRDrawingAreaUtil.lineTo ( daGraph, x, y );
					}
					else if ( lineConnectType == GRLineConnectType.STEP_USING_VALUE ) {
						// Connect from the previous point to the current point:
						// - current point will be carried forward in the next draw
						//
						//       + y
						//       |
						//       |
						//    +--+
						//  yPrev
						//
						GRDrawingAreaUtil.lineTo ( daGraph, x, yPrev );
						GRDrawingAreaUtil.lineTo ( daGraph, x, y );
					}
					else if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
						// Connect from the previous point to the current point:
						// - current point will be carried forward in the next draw
						//
						//    +--+ y
						//    |
						//    |
						//    +
						//  yPrev
						//
						GRDrawingAreaUtil.lineTo ( daGraph, xPrev, y );
						GRDrawingAreaUtil.lineTo ( daGraph, x, y );
					}
					GRDrawingAreaUtil.setLineWidth( daGraph, 1);

					if (dashedLine) {
						// Turn off the line dashes.
						GRDrawingAreaUtil.setLineDash( daGraph, null, 0);
					}
				}
				else {
					// No connecting line.
					GRDrawingAreaUtil.moveTo(daGraph, x, y);
				}

				if ( this._is_reference_graph ) {
					// Don't draw labels or symbols.
				}
				else if (labelPointWithFlag && ((symbol == GRSymbolShapeType.NONE) || (symbol_size <= 0))) {
					// Text only.
					GRDrawingAreaUtil.drawText(daGraph,
						TSData.toString(label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
						label_units), x, y, 0.0, label_position);
				}
				else if (labelPointWithFlag) {
					// Symbol and label.
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
					}

					GRDrawingAreaUtil.drawSymbolText( daGraph, symbol, x, y, symbol_size,
					        TSData.toString( label_format, label_value_format, date, y, 0.0, tsdata.getDataFlag().trim(),
						label_units ), 0.0, label_position, GRUnits.DEVICE,
						GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );

					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be.
						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
					}
				}
				else {
					// Symbol only.
					if (niceSymbols) {
						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, true);
					}

					GRDrawingAreaUtil.drawSymbol( daGraph, symbol, x, y, symbol_size, GRUnits.DEVICE,
					        GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );

					if (niceSymbols) {
						// Turn off anti-aliasing so nothing is anti-aliased that shouldn't be.
						GRDrawingAreaUtil.setDeviceAntiAlias( daGraph, false);
					}
				}

				// Need to override last position from symbol.
				GRDrawingAreaUtil.moveTo(daGraph, x, y);
			}
			else {
				// Drawing bars.
				if (bar_position == -1) {
					// All bars left of date.
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
                        centerx = x - (nts - 1) * full_bar_width_d2 + its * full_bar_width;
                    }
				}

				leftx = centerx - bar_width_d2;
				if ((leftx >= dataLimits.getMinX()) && ((leftx + bar_width) <= dataLimits.getMaxX())) {
					daGraph.setColor(tscolor);

        			if (y >= 0.0) {
        				// Positive bars.
        				if (miny >=0.0) {
        					// From miny up.
        					GRDrawingAreaUtil.fillRectangle( daGraph, leftx, miny, bar_width, (y - miny));
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine(	daGraph, leftx, miny, leftx, y);
        					}
        				}
        				else {
        					// From zero up.
        					GRDrawingAreaUtil.fillRectangle( daGraph, leftx, 0.0, bar_width, y);

        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine( daGraph, leftx, 0.0, leftx, y);
        					}
        				}
        			}
        			else {
        				// Negative bars.
        				if (maxy >= 0.0) {
        					// Up to zero.
        					GRDrawingAreaUtil.fillRectangle( daGraph, leftx, y, bar_width, -y);
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine( daGraph, leftx, y, leftx, 0.0);
        					}
        				}
        				else {
        					// Up to top.
        					GRDrawingAreaUtil.fillRectangle( daGraph, leftx, y, bar_width, (maxy - y));
        					if (always_draw_bar) {
        						GRDrawingAreaUtil.drawLine(	daGraph, leftx, y, leftx, maxy);
        					}
        				}
        			}
        			GRDrawingAreaUtil.setColor(daGraph, this._background);
        			if (draw_bounding_rectangle) {
        				if (y >= 0.0) {
        					if (miny >= 0.0) {
        						GRDrawingAreaUtil.drawLine( daGraph, leftx, miny, leftx, y);
        					}
        					else {
        						GRDrawingAreaUtil.drawLine( daGraph, leftx, 0.0, leftx, y);
        					}
        				}
        				else {
        					if (maxy >= 0.0) {
        						GRDrawingAreaUtil.drawLine( daGraph, leftx, 0.0, leftx, y);
        					}
        					else {
        						GRDrawingAreaUtil.drawLine(	daGraph, leftx, maxy, leftx, y);
        					}
        				}
        			}
				}
			}
			xPrev = x;
			yPrev = y;
			yPrevIsMissing = yIsMissing;
			++drawcount;
		}
	}

	// Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds.
	GRDrawingAreaUtil.setClip(daGraph, (Shape)null);
	GRDrawingAreaUtil.setClip(daGraph, clip);
}

/**
Determine the end date/time for rendering the time series
@param ts the time series being rendered
@return the end date/time for iterating time series data
*/
private DateTime drawTSHelperGetEndDateTime ( TS ts ) {
    DateTime end = new DateTime(this._end_date);
    end.setPrecision(ts.getDataIntervalBase());
    // If the time series data end date is less than the global end date set to local (increases performance).
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
    // When zoomed in really far, sometimes lines don't draw completely across the edges.
	// Maybe should decrement the returned DateTime by one time series data interval to make sure it starts outside the page
	// (and will get cropped correctly upon drawing) - need to evaluate this more.

    // To draw the single time series, use the start and end dates for the graph,
	// using the correct precision for the time series.
	// The start and end dates for the graph are determined using all time series and
    // may have an offset that is not suitable for the data.
    // For example, 24-hour data may be stored at hour 12 rather than hour zero.
	// Therefore, reset the start date/time for this time series to match the specific time series.

    // Get the start date/time for the graph (for all data) and round it
    // down to nearest even interval based on the current time series.
    // Only do this if the ts has an hour interval type.
    DateTime start = new DateTime(this._start_date);
    if ( ts.getDataIntervalBase() == TimeInterval.HOUR ) {
        DateTime start_withOffset = new DateTime();
        start_withOffset = TSGraph.getNearestDateTimeLessThanOrEqualTo(start, ts);
        start = start_withOffset;
    }

    /* TODO SAM 2006-09-28 not sure if Kurt's code is bulletproof for a list of monthly, daily,
    // irregular time series mixed - have him look at some more combinations and possibly consider something like the following.
    // Need to evaluate the following code to see if it works in all situations.
    // Make sure that the time series actually has a date-time that aligns with the graph start.
    // This may involve, for example:
    //start = new DateTime ( findNearestDateTime ( start, ts, -1 ) );

    // Here is a start on some ideas that work more with integer math.
    // Get the biggest offset that could occur (the interval of the data).
    int offset_interval_mult = ts.getDataIntervalMult();
    // Now check whether the time series is recorded at an offset.
    if ( (ts.getDate1().getIntervalValue(ts.getIntervalBase())%
        ts.getIntervalMult() != 0 ) {
        // Time series is offset, so adjust the start by the offset.
    }
       // End REVISIT SAM 2006-09-28
     */

    // Make sure that the iterator for this time series is using a precision that matches the time series data.
    start.setPrecision(ts.getDataIntervalBase());

    /*
    // The following was envisioned to optimize the processing.
    // For now handle in rendering code to skip data that do not need to be drawn.
    try {
        valid_dates = TSUtil.getValidPeriod ( ts, this._start_date, this._end_date );
    }
    catch ( Exception e ) {
        return;
    }
    DateTime start  = valid_dates.getDate1();
    DateTime end    = valid_dates.getDate2();
    */

    // If the time series data start date is greater than the global start date set to local (increases performance).

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
private double drawTSHelperGetSymbolSize ( int its, PropList overrideProps ) {
    double symbolSize = StringUtil.atod(getLayeredPropValue("SymbolSize", this.subproduct, its, false, overrideProps));
    return symbolSize;
}

/**
Determine the symbol shape type used for drawing a time series.
@param its the time series list position (0+, for retrieving properties and messaging)
@param overrideProps run-time override properties to consider when getting graph properties
@param flaggedData if true then the symbol is determined from the "FlaggedDataSymbolStyle" property
(if not set then "SymbolStyle" is used); if false the symbol is determined from the "SymbolStyle" property
@return the symbol style from product properties
*/
private GRSymbolShapeType drawTSHelperGetSymbolStyle ( int its, PropList overrideProps, boolean flaggedData ) {
    GRSymbolShapeType symbolStyle = GRSymbolShapeType.NONE;
    if ( this._is_reference_graph ) {
        symbolStyle = GRSymbolShapeType.NONE;
    }
    else {
        String propValue = null;
        if ( flaggedData ) {
        	// Determine symbol for flagged data.
            propValue = getLayeredPropValue("FlaggedDataSymbolStyle", this.subproduct, its, false, overrideProps);
            //Message.printStatus(2,"","Value of FlaggedDataSymbolStyle=" + propValue);
            if ( (propValue == null) || propValue.equals("") || propValue.equalsIgnoreCase("None") ) {
                // Symbol for flags was specified so use the main symbol style.
                propValue = getLayeredPropValue("SymbolStyle", this.subproduct, its, false, overrideProps);
                //Message.printStatus(2,"","FlaggedDataSymbolStyle is empty, so use SymbolStyle=" + propValue);
            }
        }
        else {
        	// Determine symbol for not flagged data.
            propValue = getLayeredPropValue("SymbolStyle", this.subproduct, its, false, overrideProps);
        }
        try {
            symbolStyle = GRSymbolShapeType.valueOfIgnoreCase(propValue);
            if ( symbolStyle == null ) {
            	symbolStyle = GRSymbolShapeType.NONE;
            }
        }
        catch (Exception e) {
            symbolStyle = GRSymbolShapeType.NONE;
        }
    }
    return symbolStyle;
}

/**
Determine the current color used for drawing a time series.
Important - this accesses the product properties, which must have handled null time series,
because the null time series are retained in data for the graph to preserve positioning.
@param its the time series list position (0+, for retrieving properties and messaging)
@param overrideProps run-time override properties to consider when getting graph properties
@param return the color that is set
*/
private GRColor drawTSHelperGetTimeSeriesColor ( int its, PropList overrideProps ) {
    GRColor tscolor = null;
    try {
        tscolor = GRColor.parseColor( getLayeredPropValue("Color", this.subproduct, its, false, overrideProps));
    }
    catch (Exception e) {
        tscolor = GRColor.black;
    }
    return tscolor;
}

/**
Draw a single time series as an area graph, used with "Area" and "AreaStacked" graph types.
The time series values are used to create polygons that have as a base the zero line.
An array is used to hold the points of the polygon for low-level rendering.
A new polygon is drawn if a missing value is encountered,
the previous and current values have different sign, or the array buffer is filled.
@param its Counter for time series (starting at 0).
@param ts Single time series to draw.
@param graphType the graph type to use for the time series (may be needed for other calls).
@param overrideProps override run-time properties to consider when getting graph properties
*/
private void drawTSRenderAreaGraph ( TSProduct tsproduct, int its,
	TS ts, TSGraphType graphType, PropList overrideProps ) {
    String routine = getClass().getSimpleName() + ".drawTSRenderAreaGraph";
    if ( ts == null ) {
        // No data for time series.
        return;
    }
    // Generate the clipping area that will be set so that no data are drawn outside of the graph.
    Shape clip = GRDrawingAreaUtil.getClip(this._da_lefty_graph);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, this._da_lefty_graph.getDataLimits());

    GRColor tscolor = drawTSHelperGetTimeSeriesColor ( its, overrideProps );
    this._da_lefty_graph.setColor(tscolor);
    DateTime start = drawTSHelperGetStartDateTime(ts);
    DateTime end = drawTSHelperGetEndDateTime(ts);

    // Loop using addInterval.
    DateTime date = new DateTime(start);
    // Make sure the time zone is not set.
    date.setTimeZone("");

	int intervalBase = ts.getDataIntervalBase();

    // Get the line connect type.
	GRLineConnectType lineConnectType = GRLineConnectType.CONNECT;
	String prop_value = getLayeredPropValue("LineConnectType", this.subproduct, its, false, overrideProps);
	lineConnectType = GRLineConnectType.valueOfIgnoreCase(prop_value);
	int lineConnectAllowedGapSeconds = -1;
	if ( lineConnectType == null ) {
		// Did not have a LineConnectType property so use the default.
		lineConnectType = GRLineConnectType.CONNECT;
	}
	if ( lineConnectType == GRLineConnectType.STEP_AUTO ) {
		// Determine the line connect type based on the time series interval.
		if ( intervalBase == TimeInterval.IRREGULAR ) {
			lineConnectType = GRLineConnectType.STEP_USING_VALUE;
		}
		else {
			if ( ts.getDataIntervalBase() >= TimeInterval.DAY ) {
				// Date only:
				// - carry forward the value
				lineConnectType = GRLineConnectType.STEP_USING_VALUE;
			}
			else {
				// Date and time:
				// - use the interval ending value
				lineConnectType = GRLineConnectType.STEP_USING_NEXT_VALUE;
			}
		}
	}

	// Get whether checking the LineConnectAllowedGap.
	prop_value = getLayeredPropValue("LineConnectAllowedGap", this.subproduct, its, false, overrideProps);
	if ( (prop_value == null) || prop_value.isEmpty() && ts.isIrregularInterval() ) {
		// The allowed gap was not specified so default based on the time series precision.
		prop_value = getDefaultLineConnectAllowedGap ( ts );
	}
	if ( (prop_value != null) && !prop_value.isEmpty() ) {
		try {
			TimeInterval lineConnectAllowedInterval = TimeInterval.parseInterval(prop_value);
			lineConnectAllowedGapSeconds = lineConnectAllowedInterval.toSeconds();
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Value of LineConnnectAllowedGap (" + prop_value +
				") is not a valid time interval - ignoring.");
		}
	}

    double x = 0.0; // X coordinate converted from date/time.
    double y = 0.0; // Y coordinate corresponding to data value.
    double xPrev = 0.0; // The previous X value.
    double yPrev = ts.getMissing(); // The previous Y value.
    double yNext = ts.getMissing(); // The next Y value.
    DateTime datePrev = null; // The previous date.
    boolean label_symbol = false;
    String label_format = null;
    String label_value_format = null;
    String label_units = null;
    int label_position = 0;
    int countTotal = 0; // Total count of points processed.
    int lineWidth = 2;
    // Array to create the polygon.
    int arraySize = 5000; // Try this to see how it performs.
    int arraySize2 = arraySize - 3; // Limit on number of points in array (to allow for closing points).
    double [] xArray = new double[arraySize];
    double [] yArray = new double[arraySize];
    int arrayCount = 0; // Number of data points put into the array.
    // Iterate through data with the iterator.
    TSData tsdata = null;
    TSIterator tsi = null;
    try {
        tsi = ts.iterator ( start, end );
    }
    catch ( Exception e ) {
        // Unable to draw (lack of data).
        return;
    }
    boolean haveMoreData = true;
    boolean yIsMissing = false;
    boolean yPrevIsMissing = false;
    boolean yNextIsMissing = false;
    // TODO smalers 2010-11-19 Need a property to control this.
    boolean anchorToZero = true; // If true, always anchor around zero.  If false, draw from bottom of graph.
	// Whether need to skip drawing a line over a gap for irregular interval time series.
	boolean doGap = false;
	boolean isIrregularInterval = ts.isIrregularInterval();
    while ( true ) {
        tsdata = tsi.next();
        yIsMissing = false;
        yPrevIsMissing = false;
        yNextIsMissing = false;
        if ( tsdata == null ) {
            // Done with data, but may need to draw buffered points.
            haveMoreData = false;
        }
        else {
            ++countTotal;
            // Save the previous values.
            xPrev = x;
            yPrev = y;
            yPrevIsMissing = yIsMissing;
            datePrev = date;
            // Get the current value.
            date = tsdata.getDate();
            x = date.toDouble();
            y = tsdata.getDataValue();
            yIsMissing = ts.isDataMissing(y);
            // Get the next value:
            // - only needed when step uses the next value
            if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
            	// The following only works with irregular time series that are in a linked list.
            	TSData tsdataNext = null;
            	if ( isIrregularInterval ) {
            		tsdataNext = tsdata.getNext();
            	}
            	else {
            		tsdataNext = tsi.next();
            		// Back up for next iteration.
            		tsi.previous();
            	}
            	if ( tsdataNext == null ) {
            		yNext = ts.getMissing();
            		yNextIsMissing = true;
            	}
            	else {
            		yNext = tsdataNext.getDataValue();
            		yNextIsMissing = ts.isDataMissing(yNext);
            	}
            }
        }

       	doGap = false;
       	if ( lineConnectAllowedGapSeconds > 0 ) {
   			Message.printStatus(2, routine, "Checking for time gap > " + lineConnectAllowedGapSeconds + " seconds.");
       		if ( (date != null) && (datePrev != null) ) {
       			// If the current date/time is > than the previous by more than the allowed gap, treat as a moveTo.
       			long dateSeconds = TimeUtil.absoluteSecond ( date );
       			long datePrevSeconds = TimeUtil.absoluteSecond ( datePrev );
       			Message.printStatus(2, routine, "Date=" + date + " dateSeconds=" + dateSeconds + " datePrevSeconds=" + datePrevSeconds);
       			if ( (dateSeconds - datePrevSeconds) > lineConnectAllowedGapSeconds ) {
       				// Gap is longer than the allowed:
       				// - set the boolean to treat as a gap below
       				doGap = true;
       			}
       		}
   			Message.printStatus(2, routine, "doGap=" + doGap);
       	}

        // Determine if need to fill a polygon because of any of the following conditions:
        // 1) Missing value.
       	// 2) Gap larger than LineConnectAllowedGap.
        // 3) No more data.
        // 4) Y is opposite sign of previous value.
        // 5) Array buffer is full - will create another polygon if necessary.

        if ( yIsMissing || doGap || !haveMoreData || (arrayCount == arraySize2) ||
        	// Make sure the values are not missing because a special value like -999 can be used for missing
        	(!yPrevIsMissing && (y*yPrev < 0.0)) ) {
            // Need to draw what is already buffered (but do not draw the current point).
            // Only draw if there was at least one value in the arrays.
            if ( arrayCount > 0 ) {
                // If a sequence of missing values, then can skip drawing anything.
                // Add two data points as using a y value anchored around zero and repeat the first value.
                double yAnchor = 0.0;
                double miny = this._data_lefty_limits.getMinY();
                if ( anchorToZero ) {
                    // Always wrap around zero.
                    yAnchor = 0.0;
                }
                else {
                    // Anchor to the bottom of the graph.
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
                GRDrawingAreaUtil.fillPolygon(this._da_lefty_graph, arrayCount, xArray, yArray );
            }
            if ( !haveMoreData ) {
                // Done processing data.
                break;
            }
            else {
                // Initialize the arrays for the next polygon.  If the value is missing, then there will be a gap.
            	// If the previous value was not missing,
            	// then use it to initialize the array so that there will be continuity in the rendering.
                arrayCount = 0;
                if ( (countTotal > 0) && !ts.isDataMissing(yPrev) && !doGap) {
                    xArray[0] = xPrev;
                    yArray[0] = yPrev;
                    ++arrayCount;
                }
            }
        }

        // Now add the current point to the arrays, but only if not missing.

        if ( !yIsMissing ) {
        	if ( (lineConnectType == GRLineConnectType.CONNECT) ) {
				// Connect from the previous point to the current point:
        		// - also use this case if a gap is being processed, to start the area after the gap
				//
				//       + y
				//      /
				//     /
				//    + yPrev
				//
        		xArray[arrayCount] = x;
        		yArray[arrayCount] = y;
        		++arrayCount;
        	}
			else if ( lineConnectType == GRLineConnectType.STEP_USING_VALUE ) {
				// Connect from the previous point to the current point:
				// - current point will be carried forward in the next draw
				//
				//       C y
				//       |
				//       |
				//    A--B
				//  yPrev
				//
				// Horizontal line A-B.
				if ( !doGap ) {
					// No gap so OK to draw the horizontal line.
					if ( !yPrevIsMissing ) {
						xArray[arrayCount] = x;
						yArray[arrayCount] = yPrev;
						++arrayCount;
					}
				}
        		if ( arrayCount < arraySize2 ) {
        			// Have array space to add the point:
        			// - vertical line B-C
        			xArray[arrayCount] = x;
        			yArray[arrayCount] = y;
        			++arrayCount;
        		}
			}
			else if ( lineConnectType == GRLineConnectType.STEP_USING_NEXT_VALUE ) {
				// Connect from the previous point to the current point:
				// - previous rendering would have drawn to "yNext" (current x,y)
				// - current point will be carried forward in the next draw
				//
				//       C--D yNext
				//       |
				//       |
				//   A---B
				//       y
				//
				// Horizontal line A-B.
				if ( !doGap ) {
					// No gap so OK to draw the horizontal line.
					if ( !yPrevIsMissing ) {
						//Message.printStatus(2, routine, "yPrev is not missing.  Adding horizontal line.");
						xArray[arrayCount] = xPrev;
						yArray[arrayCount] = y;
						++arrayCount;
					}
				}
        		if ( arrayCount < arraySize2 ) {
        			// Vertical line B-C.
        			if ( !yNextIsMissing ) {
						//Message.printStatus(2, routine, "yNext is not missing.  Adding vertical line.");
        				xArray[arrayCount] = x;
        				yArray[arrayCount] = yNext;
        				++arrayCount;
        			}
        		}
        		// The horizontal line to the next point is drawn in the next iteration.
			}
            //Message.printStatus ( 2, routine, "Adding data point[" + arrayCount + "]: " + x + "," + y );
        }
    }

    // Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds.
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, (Shape)null);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, clip);
}

/**
 * Draw a raster graph for multiple time series.
 * Time series with no data are drawn as all missing.
 * @param tslist multiple time series to draw
 * @param overrideProps runtime override properties to use when drawing
 */
private void drawTSRenderRasterGraphMultiple ( List<TS> tslist, PropList overrideProps ) {
	String routine = getClass().getSimpleName() + ".drawTSRenderRasterGraphMultiple";
    // Default color for missing data.
	GRColor nodataColor = GRColor.white;
	GRSymbolTable symtable = this.rasterSymbolTable;

	// Look up the NoData color to use for missing data.
   	if ( symtable.getNoDataSymbolTableRow() == null ) {
   		// Symbol table does not have NoData fill color:
   		// - default to white
   		nodataColor = GRColor.white;
   	}
   	else {
  		nodataColor = symtable.getNoDataSymbolTableRow().getFillColor();
   	}

    // Generate the clipping area that will be set so that no data are drawn outside of the graph
    Shape clip = GRDrawingAreaUtil.getClip(this._da_lefty_graph);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, this._da_lefty_graph.getDataLimits());

    // Fill the entire raster graph with missing color:
    // - then don't need to draw missing for specific pixels below
    this._da_lefty_graph.setColor(nodataColor);
    double x0Full = this._da_lefty_graph.getPlotLimits(GRCoordinateType.DATA).getLeftX();
    double y0Full = this._da_lefty_graph.getPlotLimits(GRCoordinateType.DATA).getTopY();
    double width = this._da_lefty_graph.getPlotLimits(GRCoordinateType.DATA).getWidth();
    double height = this._da_lefty_graph.getPlotLimits(GRCoordinateType.DATA).getHeight();
    GRDrawingAreaUtil.fillRectangle(this._da_lefty_graph, x0Full, y0Full, width, height);

    GRColor tscolor = null;
    int its = -1;
    // Look through the list of time series:
    // - each time series will be drawn on a row
    for ( TS ts : tslist ) {
    	++its;
    	double x0 = 0.0; // X-coordinate converted from date/time (interval start), left edge of rectangle.
    	double x1 = 0.0; // X-coordinate converted from date/time (interval end), used to compute width of the rectangle.
    	double y0 = 0.0; // Y-coordinate corresponding to time series index 0+, upper edge of rectangle since reversed y-axis.
    	double intervalWidth = 0.0; // Width of interval to draw the pixel rectangle.

    	if ( Message.isDebugOn ) {
    		Message.printStatus(2, routine, "Drawing time series " + ts.getIdentifierString() );
    	}

    	//DateTime start = drawTSHelperGetStartDateTime(ts);
    	//DateTime end = drawTSHelperGetEndDateTime(ts);
    	// FIXME SAM 2013-07-21 The above gets messed up because the data limits are set to an integer range.

    	if ( ts == null ) {
    		// Totally missing time series.
        	// Unable to draw individual pixels (lack of data).
    		continue;
    	}

    	DateTime start = ts.getDate1();
    	DateTime end = ts.getDate2();
    	if ( (start == null) || (end == null) ) {
    		// Time series with no data:
        	// - can continue since the no color raw was drawn above
    		continue;
    	}

    	// Loop using addInterval.
    	DateTime date = new DateTime(start);
    	// Make sure the time zone is not set.
    	date.setTimeZone("");

    	// Iterate through the time series data with the iterator:
    	// - this will draw the full time series, which relies on clipping for data outside the visible extent
    	// - TODO smalers 2023-11-29 could optimize by using the visible extent as long as the edges overlap
    	// - users typically don't zoom in horizontally much on heat maps so drawing extra is not much of a hit
    	TSData tsdata = null;
    	TSIterator tsi = null;
    	try {
        	tsi = ts.iterator ( start, end );
    	}
    	catch ( Exception e ) {
    		// Time series with no data or other issue:
        	// - unable to draw individual pixels (lack of data)
        	// - can continue since the no color raw was drawn above
        	continue;
    	}
    	double value;
    	boolean isNaN;
    	boolean isNaNPrev = false;
    	double valuePrev = Double.MAX_VALUE;
    	int intervalBase = ts.getDataIntervalBase();
    	int intervalMult = ts.getDataIntervalMult();
    	if ( Message.isDebugOn ) {
    		Message.printStatus(2,"","Drawing raster graph for start=" + start + " end=" + end + " ts.date1=" + ts.getDate1() +
            	" ts.date2=" + ts.getDate2() + " data limits = " + this._da_lefty_graph.getDataLimits() );
    	}
    	// By default assume that time is used.
    	boolean useTime = true;
    	// Dates for the start and end of the interval.
    	DateTime dateLeft = null;
    	DateTime dateRight = null;
    	while ( (tsdata = tsi.next()) != null ) {
        	date = tsdata.getDate();
        	if ( dateLeft == null ) {
        		// Initialize the dates as copy of the input.
        		dateLeft = new DateTime(date);
        		dateRight = new DateTime(date);
       			if ( (intervalBase == TimeInterval.DAY)
       				|| (intervalBase == TimeInterval.MONTH)
       				|| (intervalBase == TimeInterval.YEAR) ) {
       				// Not using time.
        			useTime = false;
       			}
        	}
        	// Set the left and right edge of the date.
        	// For times:
        	// - x0 is the start of interval
        	// - x1 is the end of interval (time series Date/time)
        	// For dates:
        	// - x0 is the time series date (time is zero and does not contribute to the plotting position)
        	// - x1 must add interval
        	if ( useTime ) {
        		// DateTime precision includes time.
        		dateRight.setDate(date);
        		dateLeft.setDate(date);
        		// Start of interval is the previous interval boundary.
        		dateLeft.addInterval ( intervalBase, -intervalMult );
        	}
        	else {
        		// DateTime precision is only the date.
        		dateLeft.setDate(date);
        		dateRight.setDate(date);
        		// End of interval is the next date boundary.
        		dateRight.addInterval ( intervalBase, intervalMult );
        	}
        	// Left coordinate reflects the above check of date and time.
        	x0 = dateLeft.toDouble();
        	y0 = its;
        	// The pixel width depends on the interval and might not be consistent (e.g., width of month with different number of days).
        	x1 = dateRight.toDouble();
        	intervalWidth = x1 - x0;
        	value = tsdata.getDataValue();
        	isNaN = Double.isNaN(value);
       		if ( isNaN || isNaNPrev || (value != valuePrev) ) {
       			// Save current value in the previous for the next iteration:
      			// - useful for sequences of zeros, missing, etc.
       			// - can't compare NaN with itself because returns false and other weirdness
       			valuePrev = value;
       			isNaNPrev = Double.isNaN(valuePrev);
       			if ( ts.isDataMissing(value) ) {
            		// Set color to missing (typically white).
        			tscolor = nodataColor;
        		}
        		else {
        			// Look up the color because the current value is different than the previous value.
        			// Color is determined from the value and the color table.
        			tscolor = symtable.getFillColorForValue ( value );
        			if ( tscolor == null ) {
        				// Indicates a problem in the symbol table format:
        				// - use black rather than the NoData value
        				if ( Message.isDebugOn ) {
        					Message.printDebug(2,routine,"No color found for value " + value + " - using black.");
        				}
        				tscolor = GRColor.black;
        			}
        			else {
        				if ( Message.isDebugOn ) {
        					Message.printDebug(2,routine,"Found color for " + value + " - using: " + tscolor.toHex());
        				}
        			}
        		}
            	// Do this to optimize so color does not have be changed frequently.
        		// TODO smalers 2021-08-27 this will need to be changed if fill and outline are used for some reason.
            	this._da_lefty_graph.setColor ( tscolor );
        	}
       		else {
       			// Current value is the same as the previous value so the previous color is OK.
       		}
        	// Rectangle will be one "cell" wide matching the time series interval.
       		/*
        	if ( tscolor == nodataColor ) {
        	    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " y0=" + y0 + " x0=" + x0 +
        	    	" width=" + intervalWidth + " using nodata color.");
        	}
        	else {
        	    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " y0=" + y0 + " x0=" + x0 +
        	    	" width=" + intervalWidth + " color="+
       	            tscolor.getRed() + "," + tscolor.getGreen() + "," + tscolor.getBlue() + ",");
        	}
        	*/
        	GRDrawingAreaUtil.fillRectangle ( this._da_lefty_graph, x0, y0, intervalWidth, 1.0 );
    	}
    }

    // Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds.
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, (Shape)null);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, clip);
}

/**
Draw a single time series as a raster.
The time series values are used to create rectangles that are color-coded.
@param ts Single time series to draw.
@param overrideProps override run-time properties to consider when getting graph properties
*/
private void drawTSRenderRasterGraphSingle ( TS ts, PropList overrideProps ) {
    String routine = getClass().getSimpleName() + ".drawTSRenderRasterGraph";

	// Check that the time series is provided.
    if ( ts == null ) {
        // No data for time series
        return;
    }

    // Default color for missing data.
	GRColor nodataColor = GRColor.white;
	GRSymbolTable symtable = this.rasterSymbolTable;

	// Look up the NoData color to use for missing data.
   	if ( symtable.getNoDataSymbolTableRow() == null ) {
   		// Symbol table does not have NoData fill color:
   		// - default to white
   		nodataColor = GRColor.white;
   	}
   	else {
  		nodataColor = symtable.getNoDataSymbolTableRow().getFillColor();
   	}

    // Generate the clipping area that will be set so that no data are drawn outside of the graph
    Shape clip = GRDrawingAreaUtil.getClip(this._da_lefty_graph);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, this._da_lefty_graph.getDataLimits());

    GRColor tscolor = null;
    //DateTime start = drawTSHelperGetStartDateTime(ts);
    //DateTime end = drawTSHelperGetEndDateTime(ts);
    // FIXME SAM 2013-07-21 The above gets messed up because the data limits are set to an integer range.
    DateTime start = ts.getDate1();
    DateTime end = ts.getDate2();

    // Loop using addInterval.
    DateTime date = new DateTime(start);
    // Make sure the time zone is not set
    date.setTimeZone("");

    double x0 = 0.0; // X coordinate converted from date/time, left edge of rectangle.
    double y0 = 0.0; // Y coordinate corresponding to year, bottom edge of rectangle.
    // Iterate through data with the iterator.
    TSData tsdata = null;
    TSIterator tsi = null;
    try {
        tsi = ts.iterator ( start, end );
    }
    catch ( Exception e ) {
        // Unable to draw (lack of data).
        return;
    }
    double value;
    double valuePrev = Double.MAX_VALUE;
    int intervalBase = ts.getDataIntervalBase();
    int intervalMult = ts.getDataIntervalMult();
    int yearDay; // Day of year for daily data.
    if ( Message.isDebugOn ) {
    	Message.printStatus(2,"","Drawing raster graph for start=" + start + " end=" + end + " ts.date1=" + ts.getDate1() +
            " ts.date2=" + ts.getDate2() + " data limits = " + this._da_lefty_graph.getDataLimits() );
    }
    boolean isLeapYear = false;
    int month = -1;
    int day = -1;
    int hour = -1;
    // Width and height of the pixel to draw, in data units.
    double pixelWidth = 0.0, pixelHeight = 0.0;
    while ( (tsdata = tsi.next()) != null ) {
        date = tsdata.getDate();
        if ( intervalBase == TimeInterval.MONTH ) {
            x0 = date.getMonth();
            y0 = date.getYear();
            pixelWidth = 1.0;
            pixelHeight = 1.0;
        }
        else if ( intervalBase == TimeInterval.DAY ) {
            yearDay = date.getYearDay();
            isLeapYear = TimeUtil.isLeapYear(date.getYear());
            month = date.getMonth();
            day = date.getDay();
            if ( !isLeapYear && (yearDay >= 60)) {
                // Increment non-leap years after February 28 so the horizontal axis days will align.
                ++yearDay;
            }
            x0 = yearDay;
            y0 = date.getYear();
            pixelWidth = 1.0;
            pixelHeight = 1.0;
        }
        else if ( intervalBase == TimeInterval.HOUR ) {
        	// Whole hours.
            pixelWidth = intervalMult; // Hours, can be >= 1.
            pixelHeight = 1.0; // 1 day.
            hour = date.getHour(); // Time series hour is at the interval end.
            // Time series hour is interval-ending so subtract an hour to align with the left edge of the pixel.
            x0 = hour - pixelWidth;
            if ( x0 >= 0.0 ) {
            	// Day is OK.
            	y0 = date.getAbsoluteDay(); // Pixel at y0 and above (to next day) corresponds to the value.
            }
            else {
            	// Have shifted to the last hour of previous day.
            	// TODO smalers 2023-06-02 need to handle time series that don't evenly align with midnight.
            	x0 = 24.0 - pixelWidth;
            	y0 = date.getAbsoluteDay() - 1.0; // Pixel at y0 and above (to next day) corresponds to the value.
            }
        }
        else if ( intervalBase == TimeInterval.MINUTE ) {
        	// Fractional hours.
            pixelWidth = intervalMult/60.0; // Fraction of hour.
            pixelHeight = 1.0; // 1 day.
            hour = date.getHour();
            x0 = hour + date.getMinute()/60.0 - pixelWidth; // Adjust to the left edge of the pixel for TSTool interval-end data.
            if ( x0 >= 0.0 ) {
            	// Day is OK.
            	y0 = date.getAbsoluteDay();
            }
            else {
            	// Have shifted to the last hour of previous day.
            	// TODO smalers 2023-06-02 need to handle time series that don't evenly align with midnight.
            	x0 = 24.0 - pixelWidth;
            	y0 = date.getAbsoluteDay() - 1.0; // Pixel at y0 and above (to next day) corresponds to the value.
            }
        }
        value = tsdata.getDataValue();
       	if ( value != valuePrev ) {
       		valuePrev = value;
       		if (ts.isDataMissing(value)) {
            	// Set color to missing (white).
            	//tscolor = GRColor.white;
        		tscolor = nodataColor;
        	}
        	else {
        		// Look up the color because the value is different than the previous value.
        		// Color is determined from the value.
        		tscolor = symtable.getFillColorForValue ( value );
        		if ( tscolor == null ) {
        			// Indicates a problem in the symbol table format:
        			// - use black rather than the NoData value
        			if ( Message.isDebugOn ) {
        				Message.printDebug(2,routine,"No color found for value " + value + " - using black.");
        			}
        			tscolor = GRColor.black;
        		}
        		else {
        			if ( Message.isDebugOn ) {
        				Message.printDebug(2,routine,"Found color for " + value + " - using: " + tscolor.toHex());
        			}
        		}
        		// Save the color so can optimize lookups, useful for sequences of zeros, missing, etc.
        		valuePrev = value;
        	}
            /* TODO smalers 2021-08-27 old code without symbol table.
            for ( int i = 0; i < scaleValues.length; i++ ) {
                if ( value >= scaleValues[scaleValues.length - 1] ) {
                    // Value is >= largest in scale so use the max color
                    tscolor = scaleColors[scaleColors.length - 1];
                    break;
                }
                else if ( value < scaleValues[i] ) {
                    // Searching from values small to high so this should properly select color.
                    tscolor = scaleColors[i];
                    break;
                }
            }
            */

            // Do this to optimize so color does not have be changed frequently.
        	// TODO smalers 2021-08-27 this will need to be changed if fill and outline are used for some reason.
            this._da_lefty_graph.setColor(tscolor);
        }
        // Rectangle will be one "cell", either a day or month.
        //if ( tscolor == GRColor.white ) {
        //    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " color=white");
        //}
        //else {
        //    Message.printStatus(2,"","Drawing raster value " + date + " " + value + " color="+
        //            tscolor.getRed() + "," + tscolor.getGreen() + "," + tscolor.getBlue() + ",");
        //}
        GRDrawingAreaUtil.fillRectangle(this._da_lefty_graph, x0, y0, pixelWidth, pixelHeight);
        // Also fill in the February 29 value for non-leap years to the same color as the February 28 value.
        // This ensures that a distracting white line is not shown.
        if ( (intervalBase == TimeInterval.DAY) && (month == 2) && (day == 28) && !isLeapYear ) {
            // Also draw the February 29, which will not otherwise be encountered because the time series is iterating
            // through the actual dates.  Use the same color as February 28.
            GRDrawingAreaUtil.fillRectangle(this._da_lefty_graph, (x0 + pixelWidth), y0, pixelWidth, pixelHeight);
        }
    }

    // Remove the clip around the graph.  This allows other things to be drawn outside the graph bounds.
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, (Shape)null);
    GRDrawingAreaUtil.setClip(this._da_lefty_graph, clip);
}

/**
Draw the X-axis grid.  This calls the drawXAxisDateLabels() if necessary.
*/
private void drawXAxisGrid ( ) {
	if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (this.__leftYAxisGraphType == TSGraphType.DURATION) ||
        (this.__leftYAxisGraphType == TSGraphType.RASTER) ) {
		// Do the grid here because it uses simple numbers and not dates.

		String color_prop = this._tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", this.subproduct, -1, false );
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
		this._da_lefty_graph.setLineWidth ( 1 );
		this._da_lefty_graph.setColor ( color );
		double [] y = new double[2];
		y[0] = this._data_lefty_limits.getBottomY();
		y[1] = this._data_lefty_limits.getTopY();
		// Draw a vertical grid.
		GRAxis.drawGrid ( this._da_lefty_graph, this._xlabels.length, this._xlabels, 2, y, GRAxis.GRID_SOLID );
	}
	else {
	    // Draw the grid in the same code that does the X-axis date/time labels so they are consistent.
		drawXAxisDateLabels ( this.__leftYAxisGraphType, true );
	}
}

/**
Draw the X-axis date/time labels.
This method can be called with "drawGrid" set as true to draw the background grid,
or "drawGrid" set to false to draw the labels.
@param graphType the graph type being drawn
@param drawGrid if true, draw the x-axis grid lines
*/
private void drawXAxisDateLabels ( TSGraphType graphType, boolean drawGrid ) {
	if (!this.__drawLeftyLabels) {
		return;
	}

	boolean log_y = false;
	boolean log_xy_scatter = false;
	String prop_value = this._tsproduct.getLayeredPropValue ( "LeftYAxisType", this.subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y = true;
	}
	prop_value = this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y = false;
		log_xy_scatter = true;
	}

	if ( drawGrid ) {
		prop_value = this._tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", this.subproduct, -1, false );
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
		GRDrawingAreaUtil.setColor ( this._da_lefty_graph, color );
	}
	else {
	    GRDrawingAreaUtil.setColor ( this._da_bottomx_label, GRColor.black );
	}

	// Now draw all the labels.

	String fontname = this._tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", this.subproduct, -1, false );
	String fontsize = this._tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", this.subproduct, -1, false );
	String fontstyle = this._tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", this.subproduct, -1, false );
	String yaxisDir = this._tsproduct.getLayeredPropValue ( "LeftYAxisDirection", this.subproduct, -1, false );
	boolean yaxisDirReverse = false;
	if ( (yaxisDir != null) && yaxisDir.equalsIgnoreCase("" + GRAxisDirectionType.REVERSE) ) {
	    yaxisDirReverse = true;
	}
	GRDrawingAreaUtil.setFont ( this._da_bottomx_label, fontname, fontstyle,	StringUtil.atod(fontsize) );

	// Raster graph with multiple time series has count of time series for Y axis with 1 at the top:
	// - need to reverse the y-values so ticks are drawn in the correct location
	// - single year-interval time series is handled as multiple but reversing 1 time series won't matter
    if ( (graphType == TSGraphType.RASTER) && (this.__tslist.size() > 1) ) {
	    yaxisDirReverse = true;
    }

	// Tick mark positions.
    double[] xt = new double[2]; // Major ticks.
    double[] xt2 = new double[2]; // Minor ticks.
    double[] yt = new double[2]; // Major ticks.
    double[] yt2 = new double[2]; // Minor ticks.
    double tick_height = 0.0; // Height of major tick marks.
    yt[0] = this._ylabels_lefty[0];
    yt2[0] = this._ylabels_lefty[0];
    if ( yaxisDirReverse ) {
        yt[0] = this._ylabels_lefty[this._ylabels_lefty.length - 1];
        yt2[0] = yt[0];
    }

    // Figure out the y-positions and tick height:
    // - same regardless of intervals being used for labels
    // - default is to use 8 pixels converted back to data units
    // - override for log axes and other cases if necessary
    tick_height = this._data_lefty_limits.getHeight()*.02;
	GRLimits dalim = new GRLimits(8.0, 8.0);
	GRLimits datalim2 = GRDrawingAreaUtil.getDataExtents(this._da_lefty_graph, dalim, 0);
	tick_height = datalim2.getHeight();
    if (log_y) {
        // TODO sam 2017-02-08 Need to make sure the line is nice length.
        tick_height = yt[0]*.05;
        yt[1] = yt[0] + tick_height;
        yt2[1] = yt2[0] + tick_height/2.0;
    }
    else if (log_xy_scatter) {
    	// TODO sam 2017-02-08 Need to make sure the line is nice length.
        tick_height = yt[0]*.05;
        yt[1] = yt[0] + tick_height;
        yt2[1] = yt2[0] + tick_height/2.0;
    }
    else {
        if ( yaxisDirReverse ) {
            // Reverse y axis direction so ticks will properly be at bottom where labels are.
            yt[1] = yt[0] - tick_height;
            yt2[1] = yt2[0] - tick_height/2.0;
        }
        else {
            // Normal y axis direction so ticks will be at bottom.
            yt[1] = yt[0] + tick_height;
            yt2[1] = yt2[0] + tick_height/2.0;
        }
    }
    if ( this.__leftYAxisGraphType == TSGraphType.PERIOD ) {
        // Reversed axes:
    	// - only include time series that are not null since using for axes limits
    	boolean includeNulls = false;
    	List<TS> tslist = getTSListForAxes ( true, false, includeNulls );
        yt[0] = tslist.size() + 1;
        yt2[0] = tslist.size() + 1;
        tick_height = this._data_lefty_limits.getHeight()*.02;
        yt[1] = yt[0] - tick_height;
        yt2[1] = yt2[0] - tick_height/2.0;
    }
    if ( drawGrid ) {
        // Reset with the maximum values.
        yt[0] = this._data_lefty_limits.getMinY();
        yt[1] = this._data_lefty_limits.getMaxY();
    }

    if ( graphType == TSGraphType.RASTER ) {
    	if ( (this.__tslist.size() == 1)
    		&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
    		// Single time series:
    		// - if time is involved, the values are interval ending
    		// - if only date, the value applies to the entire date (day or month)
    		double x;
    		int intervalBase = this.__tslist.get(0).getDataIntervalBase();
    		if ( (intervalBase == TimeInterval.MONTH) || (intervalBase == TimeInterval.DAY) ) {
    			// Loop through all of the months:
    			// - label with month centered between the start/end ticks
    			for ( int i = 1; i <= 12; i++ ) {
    				x = (this._xlabels[i - 1] + this._xlabels[i])/2.0;
    				// Draw tick marks at the labels (only internal ticks, not edges of graph which should be drawn as a surrounding graph box).
    				if ( i != 12 ) {
    					xt[0] = this._xlabels[i];
    					xt[1] = xt[0];
    					// Draw at the bottom of the raster graph.
    					yt[0] = this._ylabels_lefty[0];
    					yt[1] = yt[0] + tick_height;
    					GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
    					// Draw at the top of the raster graph.
    					yt[0] = this._ylabels_lefty[this._ylabels_lefty.length - 1];
    					yt[1] = yt[0] - tick_height;
    					GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
    				}
    				// Draw the month names centered between ticks on the bottom.
    				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, TimeUtil.monthAbbreviation(i), x,
   						this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
    				// Also draw on the top:
    				// - TODO smalers 2023-04-09 need to use the upper X-axis label drawing area
    				boolean useLabelDrawingArea = false;
    				if ( useLabelDrawingArea ) {
    					GRDrawingAreaUtil.drawText ( this._da_topx_label, TimeUtil.monthAbbreviation(i), x,
   							this._datalim_topx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
    				}
    				else {
    					// Draw the upper labels using the graph drawing area.
    					GRDrawingAreaUtil.drawText ( this._da_lefty_graph, TimeUtil.monthAbbreviation(i), x,
    						this._da_lefty_graph.getDataLimits().getTopY(),
   							0.0, GRText.CENTER_X|GRText.BOTTOM );
    				}
    			}
    		}
    		else if ( (intervalBase == TimeInterval.HOUR) || (intervalBase == TimeInterval.MINUTE) ) {
    			// Loop through all of the hours:
    			// - label with hour at the tick, indicating that values are interval-ending
    			for ( int i = 0; i <= 24; i++ ) {
    				x = this._xlabels[i];
    				// Draw tick marks at the labels (only internal ticks, not edges of graph which should be drawn as a surrounding graph box).
    				//if ( i != 12 ) {
    					xt[0] = this._xlabels[i];
    					xt[1] = xt[0];
    					// Draw at the bottom of the raster graph.
    					yt[0] = this._ylabels_lefty[0];
    					yt[1] = yt[0] + tick_height;
    					GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
    					// Draw at the top of the raster graph.
    					yt[0] = this._ylabels_lefty[this._ylabels_lefty.length - 1];
    					yt[1] = yt[0] - tick_height;
    					GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
    				//}
    				// Draw the month names centered between ticks on the bottom.
    				int labelHour = i;
    				if ( i == 24 ) {
    					labelHour = 0;
    				}
    				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + labelHour, x,
   						this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
    				// Also draw on the top:
    				// - TODO smalers 2023-04-09 need to use the upper X-axis label drawing area
    				boolean useLabelDrawingArea = false;
    				if ( useLabelDrawingArea ) {
    					GRDrawingAreaUtil.drawText ( this._da_topx_label, "" + labelHour, x,
   							this._datalim_topx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
    				}
    				else {
    					// Draw the upper labels using the graph drawing area.
    					GRDrawingAreaUtil.drawText ( this._da_lefty_graph, "" + labelHour, x,
    						this._da_lefty_graph.getDataLimits().getTopY(),
   							0.0, GRText.CENTER_X|GRText.BOTTOM );
    				}
    			}
    		}
    		return;
    	}
    	else {
    		// Multiple time series or single year interval time series:
    		// - draw a normal time axis using the code below
    		// - by default, this results in date axis being drawn at the bottom and ticks being drawn at the top
    		// - therefore add some logic below to draw ticks on the bottom X=axis.
    	}
    }

	// This logic for date labels ignores the this._xlabels array that was used elsewhere.
    // Instead, special care is given to check the precision of the dates,
    // the period that is visible, and the font size.
    // One or two layers of date labels is shown with major (and possibly minor) tick marks.

	DateTime start = null, label_date = null;
	int buffer = 6; // 2*Pixels between labels (for readability).
	int label_width = 0; // Width of a sample label.
	int label0_devx, label1_devx; // X device coordinates for adjacent test labels.
	int label_spacing = 0; // Spacing of labels, center to center.

	if ( (this._xaxis_date_precision == DateTime.PRECISION_YEAR) ||
        ((this._end_date.getAbsoluteMonth() - this._start_date.getAbsoluteMonth()) > 36) ) {
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
	    // does not always give nice spacing so to be sure try different numbers to get the max likely label size.
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "" + ic + ic + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years can be shown.
		// Determine by seeing if the first two overlap.
		int[] year_increments = { 1, 2, 5, 10, 20, 25, 50, 100 };
		int year_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < year_increments.length; itry++ ) {
			start = new DateTime ( this._start_date );
			start.setPrecision(DateTime.PRECISION_YEAR );

			year_increment = year_increments[itry];
			start.setYear((start.getYear()/year_increment)*year_increment );
			label_date = new DateTime ( start );
			label_date.addYear ( year_increment );
			label0_devx = (int)this._da_bottomx_label.scaleXData( start.toDouble());
			label1_devx = (int)this._da_bottomx_label.scaleXData( label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label.
			year_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph.
		DateTime date = new DateTime(start);
		double x = 0.0;
		for ( ; ; date.addYear ( year_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph.
			if ( !drawGrid ) {
			    // Don't draw minor ticks when drawing grid.
    			if ( year_increment == 1 ) {
    				if ( label_spacing > 70 ) {
    					// Have enough room for a minor tick mark every month.
    					label_date = new DateTime ( date);
    					label_date.setPrecision ( DateTime.PRECISION_MONTH );
    					label_date.setMonth ( 1 );
    					// Work backwards.
    					for ( int it = 0; it < 11; it++ ) {
    						label_date.addMonth(-1);
    						x = label_date.toDouble();
    						if (x < this._data_lefty_limits.getMinX()){
    							continue;
    						}
    						else if ( x > this._data_lefty_limits.getMaxX()){
    							continue;
    						}
    						xt2[0] = x;
    						xt2[1] = x;
    						GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, xt2, yt2 );
    					}
    				}
    				else {
    				    // Have enough room for a minor tick mark at 6 month interval.
    					label_date = new DateTime ( date);
    					label_date.setPrecision ( DateTime.PRECISION_MONTH );
    					label_date.setMonth ( 1 );
    					label_date.addMonth ( -6 );
    					x = label_date.toDouble();
    					if((x>=this._data_lefty_limits.getMinX())&& (x <= this._data_lefty_limits.getMaxX())){
    						xt2[0] = x;
    						xt2[1] = x;
    						GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, xt2, yt2 );
    					}
    				}
    			}
    			else if ((year_increment == 5) && (label_spacing > 50) ) {
    				// Have enough room for a minor tick mark every year.
    				label_date = new DateTime ( date);
    				// Work backwards.
    				for ( int it = 0; it < 4; it++ ) {
    					label_date.addYear(-1);
    					x = label_date.toDouble();
    					if ( x <this._data_lefty_limits.getMinX()){
    						continue;
    					}
    					else if ( x > this._data_lefty_limits.getMaxX()){
    						continue;
    					}
    					xt2[0] = x;
    					xt2[1] = x;
    					GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, xt2, yt2 );
    				}
    			}
    			else if ( (year_increment == 2) || (year_increment == 10) || (year_increment == 20) ) {
    				// Have enough room for a minor tick in the middle.
    				label_date = new DateTime ( date);
    				label_date.addYear ( -year_increment/2);
    				x = label_date.toDouble();
    				if ( (x >= this._data_lefty_limits.getMinX()) && (x <= this._data_lefty_limits.getMaxX())){
    					xt2[0] = x;
    					xt2[1] = x;
    					GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, xt2, yt2 );
    				}
    			}
			} // end !draw_grid
			// Don't worry about others.
			x = date.toDouble();
			// Now do the major tick marks and labels.
			if ( x < this._data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > this._data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
			else {
			    // Draw the labels and ticks.
				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(), x,
				this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				// Draw tick marks at the labels.
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
		}
	}
	else if ((this._xaxis_date_precision==DateTime.PRECISION_MONTH) ||
		((this._end_date.getAbsoluteDay() - this._start_date.getAbsoluteDay() > 90)) ){
		// Months less than 36 months or higher precision data more than 90 days.
		//
		// The top axis label is the month and the bottom label is the year.  Additional criteria are:
		//
		// *	If the period allows all months to be labeled, do it
		// *	If not, try to plot even months.
		// *	Then try every 3 months.
		// *	Then try every 4 months.
		// *	Then try every 6 months.
		//
		// Apparently "99" is not the widest string for fonts and
		// picking other numbers or letters does not always give nice
		// spacing so to be sure try different numbers to get the max likely label size.
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years can be shown.
		// Determine by seeing if the first two overlap.
		int[] month_increments = { 1, 2, 3, 4, 6 };
		int month_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < month_increments.length; itry++ ) {
			start = new DateTime ( this._start_date, DateTime.DATE_FAST );
			start.setPrecision(DateTime.PRECISION_MONTH);

			month_increment = month_increments[itry];
			start.setMonth((start.getMonth()/month_increment)*month_increment );
			if ( start.getMonth() == 0 ) {
				start.setMonth(1);
			}
			label_date = new DateTime ( start );
			label_date.addMonth ( month_increment );
			label0_devx = (int)this._da_bottomx_label.scaleXData( start.toDouble());
			label1_devx = (int)this._da_bottomx_label.scaleXData( label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label.
			month_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph.
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addMonth ( month_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph.
			x = date.toDouble();
			// Now do the major tick marks and labels.
			if ( x < this._data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > this._data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( this._da_bottomx_label,
				"" + date.getMonth(),x, this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getMonth() <= month_increment ) {
					// Label the year.
					GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + date.getYear(),
					x, this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels.
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw at least one year label.
			date = new DateTime(start);
			for ( ; ; date.addMonth ( month_increment ) ) {
				x = date.toDouble();
				if ( x < this._data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > this._data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + date.getYear(), x,
					this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					break;
			}
		}
	}
	else if ((this._xaxis_date_precision == DateTime.PRECISION_DAY)||
		(TSUtil.calculateDataSize(this._start_date,_end_date, TimeInterval.HOUR, 1) > 168) ) {
		// Days less than 60 days or higher precision data more than 7 days (168 hours).
		//
		// The top axis label is the day and the bottom label is YYYY-MM.  Additional criteria are:
		//
		// *	If the period allows all days to be labeled, do it
		// *	If not, try to plot even days.
		// *	Then try every 7 days.
		//
		// Apparently "99" is not the widest string for fonts and picking other numbers or letters does not always
		// give nice spacing so to be sure try different numbers to get the max likely label size.
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width) {
				label_width=(int)text_limits.getWidth();
			}
		}
		// First try with the visible start date and see if all years can be shown.
		// Determine by seeing if the first two overlap.
		int[] day_increments = { 1, 2, 7 };
		int day_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < day_increments.length; itry++ ) {
            // The following may set the day to zero so use DATE_FAST.
			start = new DateTime ( this._start_date, DateTime.DATE_FAST );
			start.setPrecision(DateTime.PRECISION_DAY);

			day_increment = day_increments[itry];
			start.setDay((start.getDay()/day_increment)*day_increment );
			if ( start.getDay() == 0 ) {
				start.setDay(1);
			}
			label_date = new DateTime ( start );
			label_date.addDay ( day_increment );
			label0_devx = (int)this._da_bottomx_label.scaleXData(start.toDouble());
			label1_devx = (int)this._da_bottomx_label.scaleXData(label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label.
			day_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph.
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addDay ( day_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph.
			x = date.toDouble();
			// Now do the major tick marks and labels.
			if ( x < this._data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > this._data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
			else {
                GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + date.getDay(), x,
					this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getDay() <= day_increment ) {
					// Label the year and month.
					GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM),
					x, this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					++nlabel2;
				}
				// Draw tick marks at the labels.
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show the year.
			date = new DateTime ( start );
			for ( ; ; date.addDay ( day_increment ) ) {
				x = date.toDouble();
				if ( x < this._data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > this._data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( this._da_bottomx_label,	date.toString(DateTime.FORMAT_YYYY_MM), x,
				this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else if ((this._xaxis_date_precision == DateTime.PRECISION_HOUR)||
		(TSUtil.calculateDataSize(this._start_date,this._end_date, TimeInterval.MINUTE,1) > 1440) ) {
		// Hours less than 7 days or minute data more than 1 day.
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
		// give nice spacing so to be sure try different numbers to get the max likely label size.
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		int[] hour_increments = { 1, 2, 3, 4, 6, 12 };
		int hour_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < hour_increments.length; itry++ ) {
			start = new DateTime ( this._start_date, DateTime.DATE_FAST );
			start.setPrecision(DateTime.PRECISION_HOUR);
			hour_increment = hour_increments[itry];
			start.setHour((start.getHour()/hour_increment)*hour_increment );
			label_date = new DateTime ( start );
			label_date.addHour ( hour_increment );
			label0_devx = (int)this._da_bottomx_label.scaleXData( start.toDouble());
			label1_devx = (int)this._da_bottomx_label.scaleXData( label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label.
			hour_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph.
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addHour ( hour_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph.
			x = date.toDouble();
			// Now do the major tick marks and labels.
			if ( x < this._data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > this._data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
			else {
			    GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + date.getHour(), x,
				this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getHour() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label YYYY-MM-DD.
						GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM_DD), x,
						        this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					else {
					    // Label MM-DD.
						GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(DateTime.FORMAT_MM_DD), x,
						this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels.
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			for ( ; ; date.addHour ( hour_increment ) ) {
				x = date.toDouble();
				if ( x < this._data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > this._data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM_DD),
				x, this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
	else {
	    // All that is left is minute data less than 1 day.
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
		// spacing so to be sure try different numbers to get the max likely label size.
		GRLimits text_limits;
		label_width = 0;
		for ( int ic = 0; ic <= 9; ic++ ) {
			text_limits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "" + ic + ic, GRUnits.DEVICE );
			if ( (int)text_limits.getWidth() > label_width){
				label_width=(int)text_limits.getWidth();
			}
		}
		int[] minute_increments = { 1, 2, 5, 10, 15, 20, 30 };
		int minute_increment = 1;
		boolean found = false;
		for ( int itry = 0; itry < minute_increments.length; itry++ ) {
			start = new DateTime ( this._start_date );
			start.setPrecision( DateTime.PRECISION_MINUTE);
			minute_increment = minute_increments[itry];
			start.setMinute((start.getMinute()/minute_increment)*minute_increment );
			label_date = new DateTime ( start );
			label_date.addMinute ( minute_increment );
			label0_devx = (int)this._da_bottomx_label.scaleXData(start.toDouble());
			label1_devx = (int)this._da_bottomx_label.scaleXData(label_date.toDouble());
			label_spacing = label1_devx - label0_devx + 1;
			if ( label_spacing >= (label_width + buffer) ) {
				found = true;
				break;
			}
		}
		if ( !found ) {
			// Last resort is to draw the first label.
			minute_increment = 10000;
		}
		// When here, do the labeling.  Just label until the plot position is past the end of the graph.
		DateTime date = new DateTime(start);
		double x = 0.0;
		int nlabel2 = 0;
		for ( ; ; date.addMinute ( minute_increment ) ) {
			// Draw minor tick marks first because they may cover an area on the edge of the graph.
			x = date.toDouble();
			// Now do the major tick marks and labels.
			if ( x < this._data_lefty_limits.getMinX() ) {
				continue;
			}
			else if ( x > this._data_lefty_limits.getMaxX() ) {
				break;
			}
			if ( drawGrid ) {
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
			else {
			    GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" + date.getMinute(), x,
					this._datalim_bottomx_label.getTopY(), 0.0, GRText.CENTER_X|GRText.TOP );
				if ( date.getMinute() == 0 ) {
					if ( nlabel2 == 0 ) {
						// Label the YYYY-MM-DD:HH.
						GRDrawingAreaUtil.drawText ( this._da_bottomx_label,
						date.toString( DateTime.FORMAT_YYYY_MM_DD)+	":" +
						StringUtil.formatString(date.getHour(),"%02d"), x,
						this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					else {
					    // Label the HH.
						GRDrawingAreaUtil.drawText ( this._da_bottomx_label, "" +
						StringUtil.formatString( date.getHour(),"%02d"), x,
						this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
					}
					++nlabel2;
				}
				// Draw tick marks at the labels.
				xt[0] = x;
				xt[1] = x;
				GRDrawingAreaUtil.drawLine (this._da_lefty_graph, xt, yt );
			}
		}
		if ( !drawGrid && (nlabel2 == 0) ) {
			// Need to draw a label at the first point to show year.
			date = new DateTime ( start );
			//for ( ; ; date.addMinute ( minute_increment )) {}
			for ( ; ; date.addHour ( 1 )) {
				x = date.toDouble();
				if ( x < this._data_lefty_limits.getMinX() ) {
					continue;
				}
				else if ( x > this._data_lefty_limits.getMaxX() ) {
					break;
				}
				GRDrawingAreaUtil.drawText ( this._da_bottomx_label, date.toString(DateTime.FORMAT_YYYY_MM_DD)+
				":" + StringUtil.formatString(date.getHour(),"%02d"),
				x, this._datalim_bottomx_label.getBottomY(), 0.0, GRText.CENTER_X|GRText.BOTTOM );
				break;
			}
		}
	}
}

/**
Draw a scatter plot.  One X-axis time series is drawn against multiple Y-axis time series.
@param daGraph drawing area to draw the graph
@param tsproduct product being processed
@param dataLimits data limits for the drawing area, adjusted for nice axis limits, zoom, etc.
@param tslist list of time series to process - all will be drawn so filtering for left/right axis should already be done.
@param regressionDataList regression data for the time series, precomputed at graph initialization so drawing is fast
*/
private void drawXYScatterPlot ( GRDrawingArea daGraph, TSProduct tsproduct,
	GRLimits dataLimits, List<TS> tslist, List<TSRegression> regressionDataList ) {
	String routine = getClass().getSimpleName() + ".drawXYScatterPlot";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Scatter data limits are " + this._data_lefty_limits );
	}
	TS ts0 = tslist.get(0);
	if ( ts0 == null) {
		return;
	}
	DateTime start = new DateTime ( ts0.getDate1() );
	DateTime end = new DateTime ( ts0.getDate2() );
	int interval_base = ts0.getDataIntervalBase();
	int interval_mult = ts0.getDataIntervalMult();

	// Assume regression data, even though some may be null.

	int nreg = tslist.size() - 1;

	// Loop through the x-axis (independent) time series and draw,
	// using the same y-axis (dependent) time series for each.

	DateTime date = null;
	TSRegression regressionData = null;
	GRColor plot_color = null;
	TS ts = null;
	String prop_val = null;
	boolean draw_line = true;
	boolean label_symbol = false; // Default.
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
		if ( (ts == null) || !isTSEnabled(i)) {
			continue;
		}
		// Draw a the line of best fit (if can't do this can still draw the data below).

		draw_line = true;
		regressionData = regressionDataList.get(i);
		if ( regressionData == null ) {
			Message.printWarning ( 2, routine, "Regression data for TS [" + i + "] is null." );
			draw_line = false;
		}

		prop_val = tsproduct.getLayeredPropValue (	"RegressionLineEnabled", this.subproduct, its, false );
		if ( (prop_val == null) || prop_val.equalsIgnoreCase("false") ){
			draw_line = false;
		}
		// For now use the font for the bottom x axis tick label for the curve fit line.
		String fontname = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", this.subproduct, -1, false );
		String fontsize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", this.subproduct, -1, false );
		String fontstyle = "BOLD";//_tsproduct.getLayeredPropValue ("BottomXAxisLabelFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( daGraph, fontname, fontstyle, StringUtil.atod(fontsize) );
		boolean [] analyze_month = null;
		// This applies whether monthly or one equation.
		if ( (regressionDataList != null) && draw_line ) {
			analyze_month = regressionData.getAnalyzeMonth();
			// Draw single and monthly lines, if available. For now always draw everything in black.
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
				// Should always know this point.
				xp[0] = dataLimits.getMinX();
				yp[0] = A + xp[0]*B;
				// Make sure Y does not go off the page.
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
					// Number the month at the top of the graph.  Don't optimize the position yet.
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
				prop_value = tsproduct.getLayeredPropValue ( "XYScatterConfidenceInterval", this.subproduct, its, false );
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
					// to carry around a lot of points but don't want to hurt performance.
					// For now do in the draw code since this really only makes sense when only a few points are analyzed.
					// Calculate the information that is necessary to draw the confidence
					// interval - only need to do this when regression data is calculated.
					yp = new double[n1];
					xp = new double[n1];
					yp2 = new double[n1];
					dn1 = (double)n1;
					try {
					F =
					FDistribution.getCumulativeFDistribution ( 2, (n1 - 2), (100 - Integer.parseInt(prop_value)) );
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
						// Calculate the totals that are needed.
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
					// Sort the X coordinates so that the line is drawn without zig-zagging back on itself.
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
					// Adjust to make sure points are on the graph.
					adjustConfidenceCurve ( xp, yp_sorted, iyci );
					adjustConfidenceCurve ( xp2_sorted,	yp2_sorted, iyci );
					// Now draw lines.
					GRDrawingAreaUtil.drawPolyline ( daGraph, iyci, xp, yp_sorted );
					GRDrawingAreaUtil.drawPolyline ( daGraph, iyci, xp2_sorted, yp2_sorted );
				}
			}
		}

		// Now draw the data only for months that have been analyzed.

		try {
		    plot_color = GRColor.parseColor( tsproduct.getLayeredPropValue ( "Color", this.subproduct, its, false ) );
		}
		catch ( Exception e ) {
			plot_color = GRColor.black;
		}
		GRDrawingAreaUtil.setColor ( daGraph, plot_color );
		prop_value = tsproduct.getLayeredPropValue ( "SymbolStyle", this.subproduct, its, false );
		GRSymbolShapeType symbol = GRSymbolShapeType.NONE;
		try {
		    symbol = GRSymbolShapeType.valueOfIgnoreCase(prop_value);
		    if ( symbol == null ) {
		    	symbol = GRSymbolShapeType.NONE;
		    }
		}
		catch ( Exception e ) {
			symbol = GRSymbolShapeType.NONE;
		}
		double symbol_size = StringUtil.atod( tsproduct.getLayeredPropValue ( "SymbolSize", this.subproduct, its, false ) );
		// First try to get the label format from the time series properties.
		label_format = tsproduct.getLayeredPropValue (	"DataLabelFormat", this.subproduct, its, false );
		if ( label_format.equals("") ) {
			// Try to get from the graph properties.
			label_format = tsproduct.getLayeredPropValue ( "DataLabelFormat", this.subproduct, -1, false );
			if ( !label_format.equals("") ) {
				// Label the format.
				label_symbol = true;
			}
		}
		else {
		    label_symbol = true;
		}
		if ( label_symbol ) {
			// Are drawing point labels so get the position, set the font, and get the format.
			label_position_string = tsproduct.getLayeredPropValue ( "DataLabelPosition", this.subproduct, its, false );
			if ( label_position_string.equals("") || label_position_string.equalsIgnoreCase("Auto")){
				// Try to get from the graph properties.
				label_position_string =	tsproduct.getLayeredPropValue ( "DataLabelPosition", this.subproduct, -1, false );
				if ( label_position_string.equals("") || label_position_string.equalsIgnoreCase( "Auto") ) {
					// Default position.
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
			// The font is only defined at the graph level.  Set for point labels.
			fontname = tsproduct.getLayeredPropValue ( "DataLabelFontName", this.subproduct, -1, false );
			fontsize = tsproduct.getLayeredPropValue ( "DataLabelFontSize", this.subproduct, -1, false );
			fontstyle = tsproduct.getLayeredPropValue ( "DataLabelFontStyle", this.subproduct, -1, false );
			GRDrawingAreaUtil.setFont ( daGraph, fontname, fontstyle, StringUtil.atod(fontsize) );
			// Determine the format for the data value in case it is needed to format the label.
			label_units = ts.getDataUnits();
			label_value_format = DataUnits.getOutputFormatString( label_units, 0, 4 );
		}
		date = new DateTime ( start );
		for ( ; date.lessThanOrEqualTo( end ); date.addInterval(interval_base, interval_mult) ) {
			// If drawing a line, only draw points that are appropriate for the line.
			// If not drawing the line of best fit, draw all available data.
			if ( draw_line && !analyze_month[date.getMonth() - 1] ) {
				continue;
			}
			if ( draw_line && analyze_monthly && !regressionData.isAnalyzed(date.getMonth()) ) {
				continue;
			}
			else if ( draw_line && !analyze_monthly && (!regressionData.isAnalyzed() && (regressionData.getN1() != 1)) ) {
				continue;
			}
			// Dependent is always the first one.
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
					label_position,	GRUnits.DEVICE,GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
			}
			else {
			    GRDrawingAreaUtil.drawSymbol ( daGraph, symbol, x, y, symbol_size,
					GRUnits.DEVICE,	GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
			}
			if (niceSymbols) {
				GRDrawingAreaUtil.setDeviceAntiAlias(daGraph, false);
			}
		}
	}
}

/**
Draw the Y-axis grid lines and tick marks.
*/
private void drawYAxisGrid () {
	String routine = getClass().getSimpleName() + ".drawYAxisGrid";
	// Left y-axis grid lines.
	String propValue = this._tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor", this.subproduct, -1, false );
	if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
		GRColor color;
		try {
		    color = GRColor.parseColor(propValue);
		}
		catch ( Exception e ) {
			color = GRColor.black;
		}
		this._da_lefty_graph.setColor ( color );
		double [] x = new double[2];
		x[0] = this._data_lefty_limits.getLeftX();
		x[1] = this._data_lefty_limits.getRightX();
		// Draw a horizontal grid.
		this._da_lefty_graph.setLineWidth ( 1 );
		if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
			// For now, do not allow grid lines, but do want Y-axis tick marks draw below.
		}
		else {
			GRAxis.drawGrid ( this._da_lefty_graph, 2, x, this._ylabels_lefty.length, this._ylabels_lefty, GRAxis.GRID_SOLID );
		}
	}

	// Left y-axis tick marks (on top of grid lines).
	propValue = this._tsproduct.getLayeredPropValue ( "LeftYAxisMajorTickColor", this.subproduct, -1, false );
	if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
		GRColor color;
		try {
		    color = GRColor.parseColor(propValue);
		}
		catch ( Exception e ) {
			color = GRColor.black;
		}
		this._da_lefty_graph.setColor ( color );
		this._da_lefty_graph.setLineWidth ( 1 );
		GRAxis.drawTicks(this._da_lefty_graph, GRAxisDimensionType.Y, GRAxisEdgeType.LEFT, this._ylabels_lefty.length, this._ylabels_lefty, -1.0, 0.0 );
	}
	else {
		Message.printStatus(2, routine, "Not drawing left y-axis tick marks because LeftYAxisMajorTickColor=" + propValue);
	}

	// Right y-axis grid lines.
	if ( this.__drawRightyLabels ) {
		propValue = this._tsproduct.getLayeredPropValue ( "RightYAxisMajorGridColor", this.subproduct, -1, false );
		if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
			GRColor color;
			try {
			    color = GRColor.parseColor(propValue);
			}
			catch ( Exception e ) {
				color = GRColor.black;
			}
			this._da_righty_graph.setColor ( color );
			this._da_righty_graph.setLineWidth ( 1 );
			double [] x = new double[2];
			x[0] = this._data_righty_limits.getLeftX();
			x[1] = this._data_righty_limits.getRightX();
			// Draw a horizontal grid.
			if ( this._ylabels_righty != null ) {
				// Can be null for new graph, especially when splitting product.
				GRAxis.drawGrid ( this._da_righty_graph, 2, x, this._ylabels_righty.length, this._ylabels_righty, GRAxis.GRID_SOLID );
			}
		}
	}

	// Right y-axis tick marks (on top of grid lines).
	propValue = this._tsproduct.getLayeredPropValue ( "RightYAxisMajorTickColor", this.subproduct, -1, false );
	if ( (propValue != null) && !propValue.equalsIgnoreCase("None") ) {
		GRColor color;
		try {
		    color = GRColor.parseColor(propValue);
		}
		catch ( Exception e ) {
			color = GRColor.black;
		}
		this._da_righty_graph.setColor ( color );
		this._da_righty_graph.setLineWidth ( 1 );
		GRAxis.drawTicks(this._da_righty_graph, GRAxisDimensionType.Y, GRAxisEdgeType.RIGHT, this._ylabels_righty.length, this._ylabels_righty, -1.0, 0.0 );
	}
}

/**
 * Find Y-axis labels for date.
 * Currently this calls the normal double-number label generator and saves whole numbers only once (fractions are ignored),
 * to ensure that whole days are used.
 * @param minimum absolute day
 * @param maximum absolute day
 * @param includeEndPoints if true, the end values must be included in the labels
 * @param minLabels the minimum number of labels to include
 * @param maxLabels the maximum number of labels to include
 * @param precision DateTime precision for output, used to indicate the minimum spacing,
 * currently always handled as DateTime.PRECISION_DAY
 * @return array of floating point absolute day for the Y-axis ticks.
 */
double [] findYAxisDateLabels ( double minDay, double maxDay, boolean includeEndPoints, int minLabels, int maxLabels, int precision ) {
	double [] labels = new double[0];
	// First try to use the normal method.
    labels = GRAxis.findNLabels ( minDay, maxDay, includeEndPoints, minLabels, maxLabels );
    if ( labels == null ) {
    	return null;
    }
    // Loop through the list and remove redundant values, when formatted as an integer.
    double [] labels2 = new double[labels.length];
    int nlabel = 0;
   	double tolerance = .0001;
   	double fraction;
   	double day;
   	double dayPrev = -1;
    if ( minDay <= maxDay ) {
    	// Always add the first label.
    	//labels2[nlabel++] = labels[0];
    	for ( int i = 0; i < labels.length; i++ ) {
    		day = (int)labels[i];
    		if ( nlabel == 0 ) {
    			// Always add.
    			labels2[nlabel++] = day;
    		}
    		else {
    			// Only add if not previously added.
    			if ( day != dayPrev ) {
    				labels2[nlabel++] = day;
    			}
    		}
    		// Save the day for the next iteration.
    		dayPrev = day;
    	}
    }
    else {
    	// Moving backwards in time so iterate from the end value first.
    	// Always add the last label.
    	//labels2[nlabel++] = labels[labels.length - 1];
    	for ( int i = labels.length - 1; i >= 0; i-- ) {
    		day = (int)labels[i];
    		if ( nlabel == 0 ) {
    			// Always add.
    			labels2[nlabel++] = day;
    		}
    		else {
    			// Only add if not previously added.
    			if ( day != dayPrev ) {
    				labels2[nlabel++] = day;
    			}
    		}
    		// Save the day for the next iteration.
    		dayPrev = day;
    	}
    }
    // Size the returned array to contain only the final values.
    labels = new double[nlabel];
    for ( int i = 0; i < nlabel; i++ ) {
    	labels[i] = labels2[i];
    }
	return labels;
}

/**
Format a data point for a tracker "X: xxxxx,  Y: yyyyy".
If right y-axis is used, format as:  "LEFT X: xxxxx,  Y: yyyyy / RIGHT X: xxxxx,  Y:  yyyyy".
Handles special cases for graph types that have other than simple axes.
@param devpt Device point to format (needed to lookup right y-axis value).
@param datapt Data point to format.
*/
public String formatMouseTrackerDataPoint ( GRPoint devpt, GRPoint datapt ) {
	String routine = getClass().getSimpleName() + ".formatMouseTrackerDataPoint";
	try {
	if ( datapt == null ) {
		return "";
	}
	else if ((this.__leftYAxisGraphType == TSGraphType.DOUBLE_MASS) ||
		(this.__leftYAxisGraphType == TSGraphType.DURATION) ||
		(this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
		return "X:  " + StringUtil.formatString(datapt.x,"%.2f") +
			",  Y:  " + StringUtil.formatString(datapt.y,"%." + this._lefty_precision + "f");
	}
    else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
   		TS ts = null;
   		if ( datapt.associated_object != null ) {
   			if ( datapt.associated_object instanceof TS ) {
   				ts = (TS)datapt.associated_object;
   			}
   		}
    	if ( (this.__tslist.size() == 1)
    		&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
    		String xString = "";
    		String yString = "";
    		String valueString = "";
    		TSData tsdata;
    		double value = Double.NaN;
    		String flag = "", flagString = "";
    		int year = (int)(datapt.y);
    		boolean isMissing = false;
    		if ( ts == null ) {
    			if ( Message.isDebugOn ) {
    				Message.printStatus(2,"","Time series is null for data point x=" + datapt.x + " y=" + datapt.y);
    			}
    		}
    		else {
    			DateTime d = new DateTime(DateTime.DATE_FAST);
           		if ( ts.getDataIntervalBase() == TimeInterval.HOUR ) {
           			// Keep minutes because used for adjustments:
           			// - tracker is formatted manually and does not use DateTime.toString()
           			d.setPrecision(DateTime.PRECISION_MINUTE);
          		}
           		else if ( ts.getDataIntervalBase() == TimeInterval.MINUTE ) {
           			d.setPrecision(DateTime.PRECISION_MINUTE);
           		}
    			d.setYear(year);
    			if ( ts.getDataIntervalBase() == TimeInterval.MONTH ) {
    				//if ( this._data_lefty_limits.getMaxX() <= 12.0 ) { }
    				// Monthly data:
    				// - mouse coordinate might scale to outside the drawing area so constrain?
    				int month = (int)datapt.x;
    				if ( month < 1 ) {
    					month = 1;
    				}
    				else if ( month > 12 ) {
    					month = 12;
    				}
    				xString = "Month " + month + " (" + TimeUtil.monthAbbreviation(month) + ")";
    				yString = "Year " + year;
    				d.setMonth(month);
    				if ( ts != null ) {
    					// New TSData is created each time:
    					// - evaluate whether to keep an instance in memory between calls
    					tsdata = ts.getDataPoint(d, null);
    					value = tsdata.getDataValue();
    					flag = tsdata.getDataFlag();
    					if ( (flag != null) && !flag.isEmpty() ) {
    						flagString = " (" + flag + ")";
    					}
    					if ( ts.isDataMissing(value) ) {
    						valueString = "Value: missing" + flagString;
    						isMissing = true;
    					}
    					else {
    						// TODO SAM 2013-07-31 Need to figure out precision from data, but don't look up each
    						// call to this method because a performance hit?
    						valueString = "Value: " + StringUtil.formatString(value,"%.2f " + ts.getDataUnits() + flagString );
    					}
    				}
    			}
    			//else if ( this._data_lefty_limits.getMaxX() <= 366.0 ) {}
    			else if ( ts.getDataIntervalBase() == TimeInterval.DAY ) {
            		// Graph was set up to always have leap year.
            		int dayInYear = (int)datapt.x;
            		boolean isLeapYear = TimeUtil.isLeapYear(year);
            		if ( (dayInYear == 60) && !isLeapYear ) {
                		// Treat as missing since actual year does not have February 29.
                		return "No value (Feb 29 of non-leap year)";
            		}
            		else {
                		int [] md;
                		if ( !isLeapYear && (dayInYear > 59) ) {
                    		// If not a leap year and past day 59,
                			// need to offset the day by one and recompute to get the actual day to retrieve the correct data value.
                			// This is because Feb 29 always has a plotting position in order to ensure days line up with months.
                    		--dayInYear;
                		}
                		md = TimeUtil.getMonthAndDayFromDayOfYear(year, dayInYear);
                		xString = "Day " + dayInYear + " (" + TimeUtil.monthAbbreviation(md[0]) + " " + md[1] + ")";
                		yString = "Year " + year;
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
                        		valueString = "Value: missing" + flagString;
                        		isMissing = true;
                    		}
                    		else {
                        		valueString = "Value: " + StringUtil.formatString(value,"%.2f") + " " + ts.getDataUnits() + flagString;
                    		}
                		}
                	}
            	}
    			else if ( (ts.getDataIntervalBase() == TimeInterval.HOUR) || (ts.getDataIntervalBase() == TimeInterval.MINUTE) ) {
            		// X axis goes from 0 (start midnight) to 24 (end midnight):
    				// - mouse position will typically not be at exactly hour 0 or 24.
    				//
    				//  |             |
    				//  |      x      |
    				//  hour         hour+1
    				//               time series interval-ending hour
            		int hourOfDay = (int)datapt.x;
            		if ( hourOfDay >= 24 ) {
            			// Typically only when the mouse is exactly on the right border of the graph.
            			// - treat as if still in hour 23 of the last pixel in the day
            			hourOfDay = 23;
            		}
            		// Top of pixel is the later day, bottom of pixel is the earlier (time series value) day,
            		// so should be OK without shifting, because day will roll over at the top of the pixel.
					int [] dateParts = TimeUtil.getYearMonthDayFromAbsoluteDay((int)(datapt.y));
               		d.setYear(dateParts[0]);
               		d.setMonth(dateParts[1]);
               		d.setDay(dateParts[2]);
               		// Get the minutes as fraction of day:
               		// - dayFraction should be less than 1.0 until multiplied by 24
               		double hoursRemainder = datapt.x - (int)datapt.x;
               		// Above remainder should be minutes.
               		double minutesFraction = hoursRemainder*60;
               		int minutes = (int)minutesFraction;
               		// Initially set the hour of the day to the in-interval hour (not interval end):
               		// - will adjust below to agree with the time series value
               		d.setHour(hourOfDay);
               		d.setMinute(minutes);
               		//Message.printStatus(2, routine, "Mouse position from graph: " + d);
               		if ( ts.getDataIntervalBase() == TimeInterval.HOUR ) {
               			// Minutes are used only to see if mouse is right at an even hour.
               			if ( datapt.x > (24 - ts.getDataIntervalMult()) ) {
               				// Last interval of the day but in the time series is the first interval of the next day:
               				// - for example, for 1-hour data, 23.1 hours is > 23 hours so last interval
               				// - shift the date by the data interval to match what should be in the time series table view
               				d.addHour(ts.getDataIntervalMult());
               				// Minute does not matter.
               				d.setMinute(0);
               			}
               			else if ( d.getMinute() > 0 ) {
               				// In the middle of an hour:
               				// - set to the interval-ending time to agree with the time series
               				d.setMinute(0);
               				d.addHour(1);
               			}
               		}
               		else if ( ts.getDataIntervalBase() == TimeInterval.MINUTE ) {
               			if ( (d.getHour() > 23) && (d.getMinute() > (60 - ts.getDataIntervalMult())) ) {
               				// Last interval of the day but in the time series is the first interval of the next day:
               				// - for example, for 1-hour data, 23.1 hours is > 23 hours so last interval
               				// - shift the date by the data interval to match what should be in the time series table view
               				d.addHour(1);
               				d.setMinute(0);
               			}
               			else if ( d.getMinute()%ts.getDataIntervalMult() > 0 ) {
               				// In the middle of a minute interval:
               				// - set to the interval-ending time to agree with the time series
               				// - OK to roll over hour because won't be at the end of the day (which is handled above)
               				d.setMinute(((d.getMinute()/ts.getDataIntervalMult()) + 1)*ts.getDataIntervalMult());
               				if ( d.getMinute() == 60 ) {
               					d.setMinute(0);
               					d.addHour(1);
               				}
               			}
               		}
               		//Message.printStatus(2, routine, "Mouse position after adjusting for time series: " + d);
           			yString = "Date " + String.format("%04d", d.getYear())
			 			+ "-" + String.format("%02d", d.getMonth())
			 			+ "-" + String.format("%02d", d.getDay());
               		if ( ts.getDataIntervalBase() == TimeInterval.MINUTE ) {
               			// X includes hour and minute.
               			xString = "Time <= " + String.format("%02d", d.getHour()) + ":" + String.format("%02d", d.getMinute());
               		}
               		else {
               			// X includes only hour.
               			xString = "Time <= " + String.format("%02d", d.getHour());
               		}
               		if ( ts != null ) {
               			// For time series data retrieval, need interval end so add the interval.
               			//d.addInterval(ts.getDataIntervalBase(), ts.getDataIntervalMult());
                   		tsdata = ts.getDataPoint(d, null);
                   		value = tsdata.getDataValue();
                   		flag = tsdata.getDataFlag();
                   		if ( (flag != null) && !flag.equals("") ) {
                       		flagString = " (" + flag + ")";
                   		}
                   		if ( ts.isDataMissing(value) ) {
                       		valueString = "Value: missing" + flagString;
                       		isMissing = true;
                   		}
                   		else {
                       		valueString = "Value: " + StringUtil.formatString(value,"%.2f") + " " + ts.getDataUnits() + flagString;
                   		}
               		}
            	}
    		} // End ts != null
    		if ( valueString.isEmpty() ) {
        		return "";
        	}
        	else {
        		if ( isMissing ) {
        			return "X: " + xString + ",  Y: " + yString + ", " + valueString;
        		}
        		else {
        			String rangeString = getRasterRangeString(value);
        			if ( rangeString.isEmpty() ) {
        				return "X: " + xString + ",  Y: " + yString + ", " + valueString + ", Range: ?";
        			}
        			else {
        				return "X: " + xString + ",  Y: " + yString + ", " + valueString + ", Range: " + rangeString;
        			}
        		}
        	}
       	}
    	else {
    		// Multiple time series or single year-interval time series.
    		// The 'mouseDate' is calculated from the mouse position and not an even time series interval.
    		// However, since raster plot time axes is "blocked", can do a more precise mapping of time to data values
    		DateTime mouseDate = new DateTime(datapt.x, true);
    		mouseDate.setPrecision ( this._xaxis_date_precision );
    		// Increment the mouse pointer since zero index.
    		int y = (int)datapt.y + 1;
			String xString = "X:  " + mouseDate.toString();
			String yString = "Y:  " + StringUtil.formatString(y,"%d");
			String tsString = "TS: " + TSUtil.getTimeSeriesIdentifier(ts,false);
			String valueString = "";
			boolean isMissing = true;
			double value = Double.NaN;
           	// Make sure that the time series is OK:
			// - not null
			// - has data (start and end defined and data array exists)
			// - otherwise will get NullPointerException in called code
            if ( (ts != null) && ts.hasData() ) {
            	int intervalBase = ts.getDataIntervalBase();
            	int intervalMult = ts.getDataIntervalMult();
            	boolean useTime = true;
        		if ( (intervalBase == TimeInterval.DAY)
        			|| (intervalBase == TimeInterval.MONTH)
        			|| (intervalBase == TimeInterval.YEAR) ) {
        			// Not using time.
        			useTime = false;
        		}
            	TSData tsdata = null;
            	if ( useTime ) {
            		// Date/time precision is date and time:
            		// - time series value is the end of the interval
            		// - therefore actually want the value corresponding to the interval ending the 'mouseDate'
            		mouseDate.round(1, intervalBase, intervalMult);
            		tsdata = ts.getDataPoint(mouseDate, null);
            	}
            	else {
            		// Date/time precision is date:
            		// - date (when partial days are truncated) is retained until the next date
            		// - therefore the value should be OK.
            		tsdata = ts.getDataPoint(mouseDate, null);
            	}
            	value = tsdata.getDataValue();
            	String flag = tsdata.getDataFlag();
            	String flagString = "";
            	if ( (flag != null) && !flag.equals("") ) {
               		flagString = " (" + flag + ")";
            	}
            	if ( ts.isDataMissing(value) ) {
               		isMissing = true;
               		valueString = "Value: missing" + flagString;
            	}
            	else {
               		isMissing = false;
               		valueString = "Value: " + StringUtil.formatString(value,"%.2f") + " " + ts.getDataUnits() + flagString;
            	}
            }
    		if ( valueString.isEmpty() ) {
        		return "";
        	}
        	else {
        		if ( isMissing ) {
        			// Value was missing.
        			return xString + ", " + yString + ", " + valueString + ", " + tsString;
        		}
        		else {
        			// Value was not missing.
        			return xString + ", " + yString + ", " + valueString + ", Range: " + getRasterRangeString(value) + ", " + tsString;
        		}
        	}
    	}
    }
	else {
		// Simple graph type.
	    DateTime mouseDate = new DateTime(datapt.x, true);
		GRPoint dataptRightYAxis = null;
		if ( this.__rightYAxisGraphType != TSGraphType.NONE ) {
			// Look up data point from the device coordinates.
			dataptRightYAxis = this._da_righty_graph.getDataXY( devpt.getX(), devpt.getY(), GRCoordinateType.DEVICE );
		}
		if ( this._bottomx_date_format != DateTime.FORMAT_UNKNOWN ) {
			// Have a specific date/time format.
			String leftYString = "X:  " + mouseDate.toString(this._bottomx_date_format) + ",  Y:  "
				+ StringUtil.formatString(datapt.y,"%." + this._lefty_precision + "f");
			if ( (this.__rightYAxisGraphType != TSGraphType.NONE) && (dataptRightYAxis != null) ) {
				// Need to also show right y-axis.
				String rightYString = "X:  " + mouseDate.toString(this._bottomx_date_format) + ",  Y:  "
						+ StringUtil.formatString(dataptRightYAxis.y,"%." + this._righty_precision + "f");
				return "LEFT: " + leftYString + " / RIGHT: " + rightYString;
			}
			else {
				return leftYString;
			}
		}
		else {
			// Else the format was not determined so use the x-axis precision for the date/time.
			mouseDate.setPrecision ( this._xaxis_date_precision );
		    String leftYString = "X:  " + mouseDate.toString() + ",  Y:  "
		    	+ StringUtil.formatString(datapt.y,"%." + this._lefty_precision + "f");
			if ( (this.__rightYAxisGraphType != TSGraphType.NONE) && (dataptRightYAxis != null) ) {
				// Need to also show right y-axis.
				String rightYString = "X:  " + mouseDate.toString() + ",  Y:  "
					+ StringUtil.formatString(dataptRightYAxis.y,"%." + this._righty_precision + "f");
				return "LEFT: " + leftYString + " / RIGHT: " + rightYString;
			}
			else {
				return leftYString;
			}
		}
	}
	}
	catch ( Exception e ) {
		// For troubleshooting.
		Message.printWarning(3, routine, e);
		return "";
	}
}

/**
Return the data limits for the graph.
For a reference graph, this is the zoomed data limits but not the overall data limits.
For a normal graph, the limits from this method are the same as for the drawing area.
@return The current data limits.
*/
public GRLimits getDataLimits() {
	return this._data_lefty_limits;
}

/**
 * Get the default value for LineConnectAllowedGap, determined at runtime for a time series.
 * @param ts time series of interest
 * @return the property value based on the time series time interval, or null if no default is available
 */
private String getDefaultLineConnectAllowedGap ( TS ts ) {
	// - smaller interval have smaller precision
	int precision = ts.getDate1().getPrecision();
	String propValue = null;
	if ( precision <= TimeInterval.HOUR ) {
		propValue = "7Day";
	}
	else if ( precision <= TimeInterval.DAY ) {
		propValue = "1Year";
	}
	else if ( precision <= TimeInterval.MONTH ) {
		propValue = "1Year";
	}
	else if ( precision <= TimeInterval.YEAR ) {
		propValue = "2Year";
	}

	return propValue;
}

/**
Return the derived time series list used for graphing.
@return the derived time series list being graphed.
*/
private List<TS> getDerivedTSList() {
    return this.__derivedTSList;
}

/**
Returns a list of all the derived time series that are enabled.  This will never return null.
If no derived time series are enabled, an empty list will be returned.
@param includeLeftYAxis if true, include left y-axis time series.
@param includeRightYAxis if true, include right y-axis time series.
@return a list of all the derived time series that are enabled.
*/
public List<TS> getEnabledDerivedTSList ( boolean includeLeftYAxis, boolean includeRightYAxis ) {
	if ( (this.__derivedTSList == null) || (this.__derivedTSList.size() == 0) ) {
		// Don't have any time series for data.
		return new ArrayList<>();
	}

	int size = this.__derivedTSList.size();
	String propValue = null;
	List<TS> tslist = new ArrayList<>();
	for ( int its = 0; its < size; its++ ) {
		propValue = this._tsproduct.getLayeredPropValue("Enabled", this.subproduct, its, false);
		if ( (propValue != null) && propValue.equalsIgnoreCase("False") ) {
			// Time series is not enabled so set as null:
			// - this ensures that the order of the time series in the derived list is consistent with product properties
			propValue = this._tsproduct.getLayeredPropValue("YAxis", this.subproduct, its, false);
			if ( (propValue == null) || propValue.isEmpty() ) {
				// Default is time series is associated with left axis.
				propValue = "Left";
			}
			if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
				tslist.add(null);
			}
			if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
				tslist.add(null);
			}
		}
		else {
			// Time series is enabled, add to the list if for the requested axis.
			propValue = this._tsproduct.getLayeredPropValue("YAxis", this.subproduct, its, false);
			if ( (propValue == null) || propValue.isEmpty() ) {
				// Default is time series is associated with left axis.
				propValue = "Left";
			}
			if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
				tslist.add(this.__derivedTSList.get(its));
			}
			if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
				tslist.add(this.__derivedTSList.get(its));
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
	return this._end_date;
}

/**
 * Return the time series corresponding to a legend select event, or null if event was not in a legend hotspot.
 * @param eventPoint location of mouse click in original device units (y is zero at top).
 * @return the matching time series if clicked on time series legend text, or null if not a hit.
 */
protected TS getEventLegendTimeSeries ( GRPoint eventPoint ) {
	// Loop through the legend hotspots and see if any match the point.
	if ( Message.isDebugOn ) {
		Message.printStatus(2,"","Searching legend hotspots for event point (device/drawing units) " + eventPoint );
	}
	for ( Map.Entry<TS, GRLimits> entry : this.__legendTimeSeriesDrawMap.entrySet() ) {
		GRLimits value = entry.getValue();
		if ( value != null ) {
			GRLimits drawLimits = value; // Device units.
			if ( Message.isDebugOn ) {
				Message.printStatus(2,"","Legend hotspot device/drawing limits are " + drawLimits );
			}
			if ( drawLimits.contains(eventPoint) ) {
				// Return the TS associated with the hotspot.
				TS ts = entry.getKey();
				if ( Message.isDebugOn ) {
					Message.printStatus(2,"","Found time series in legend:  " + ts.getIdentifierString() );
				}
				return ts;
			}
		}
	}
	return null;
}

/**
Returns the first time series in the time series list that is enabled.
@param includeLeftYAxis whether to include time series for the left Y-axis
@param includeRightYAxis whether to include time series for the right Y-axis
@return the first time series in the time series list that is enabled.
*/
private TS getFirstNonNullTS ( boolean includeLeftYAxis, boolean includeRightYAxis ) {
	// Disabled time series will be null.
	boolean includeNulls = false;
	List<TS> tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
	if (tslist.size() == 0) {
		return null;
	}
	return tslist.get(0);
}

/**
Returns a prop value from the product, taking into account any override properties that may be set in the graph.
This method is used when drawing time series.
@param key the key of the property to return.
@param subproduct the subproduct of the property to return
@param its the number of the time series of the property to return
@param annotation if true, then the property is an annotation
@param overrideProps if not null, this is the proplist that will be checked for the property.
@return the prop value, or null if the property does not exist
*/
protected String getLayeredPropValue(String key, int subproduct, int its, boolean annotation, PropList overrideProps) {
	if (overrideProps == null) {
		return this._tsproduct.getLayeredPropValue( key, subproduct, its, annotation);
	}
	else {
		String propValue = overrideProps.getValue(key);
		if (propValue == null) {
			return this._tsproduct.getLayeredPropValue( key, subproduct, its, annotation);
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
public TSGraphType getLeftYAxisGraphType () {
 	return this.__leftYAxisGraphType;
}

/**
Return the drawing area used for the left y-axis graph.  The value may be null.
This is most often used by code that handles mouse events.
@return The drawing area used for the left y-axis graph.
*/
public GRJComponentDrawingArea getLeftYAxisGraphDrawingArea() {
	return this._da_lefty_graph;
}

/**
 * Return the left y-axis maximum y value for interactive viewing.
 */
protected String getLeftYAxisViewMaxY () {
	return this.leftYAxisViewMaxY;
}

/**
 * Return the left y-axis minimum y value for interactive viewing.
 */
protected String getLeftYAxisViewMinY () {
	return this.leftYAxisViewMinY;
}

/**
 * Return the right y-axis maximum y value for interactive viewing.
 */
protected String getRightYAxisViewMaxY () {
	return this.rightYAxisViewMaxY;
}

/**
 * Return the right y-axis minimum y value for interactive viewing.
 */
protected String getRightYAxisViewMinY () {
	return this.rightYAxisViewMinY;
}

/**
Return the legend string for a time series.
@return a legend string.  If null is returned, the legend should not be drawn.
@param ts Time series to get legend.
@param its Loop counter for time series (0+).
For graphs such as period of record, the loop counter should be for non-null time series.
*/
private String getLegendString ( TS ts, int its ) {
	String legend = "";
	if ( (ts == null) || !ts.getEnabled() ) {
		// Null and disabled time series are not shown in the legend.
		// This is consistent with how the legend drawing area was set up.
		return null;
	}

	// Subproduct legend format, which will provide a default if the time series legend format is "Auto".
	String subproduct_legend_format = this._tsproduct.getLayeredPropValue( "LegendFormat", this.subproduct, -1, false);
	// Determine the legend format for the specific time series.
	// If the label is "Auto", define using the default
	// (however, if Auto and the subproduct is not auto, use the subproduct format).
	// If blank, don't draw the legend.
	String legend_format = this._tsproduct.getLayeredPropValue("LegendFormat", this.subproduct, its, false );
	if ( legend_format == null ) {
		// Try the legend format for the subproduct.
		legend_format = subproduct_legend_format;
	}
	if ( (legend_format == null) || (legend_format.length() == 0) ) {
		// Do not draw a legend.  Later might add a LegendEnabled,
		// which would totally turn off the legend (LegendFormat is really more for the string label).
		return null;
	}
	else if ( !legend_format.equalsIgnoreCase("Auto") ) {
		// A specific legend has been specified.
		legend = ts.formatLegend ( legend_format );
	}
	// Below here "Auto" is in effect.
	else if ( (ts.getLegend() != null) && (ts.getLegend().length() != 0) ) {
		// The time series data itself has legend information so use it.
		// TODO SAM 2008-04-28 Should this even be allowed any more now that properties are used? -
	    // probably for applications that want more control.
		legend = ts.formatLegend ( ts.getLegend() );
	}
	else if ( !subproduct_legend_format.equalsIgnoreCase("Auto") ) {
		// Use the sub-product legend.
		legend = ts.formatLegend ( subproduct_legend_format );
	}
	else {
	    // "Auto", format the legend manually.
		if ( this._ignoreLeftAxisUnits && !ts.getDataUnits().equals("") ) {
			// Add units to legend because they won't be on the axis label.
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
		    // Don't put units in legend.
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
	if ( this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
		if ( its == 0 ) {
			legend = "Y (dependent): " + legend;
		}
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.PERIOD ) {
		legend = (its + 1) + ") " + legend;
	}
	else if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
		if ( (this.__tslist.size() > 1)
    		|| ((this.__tslist.size() == 1)
    			&& (this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() == TimeInterval.YEAR)) ) {
			legend = (its + 1) + ") " + legend;
		}
	}
	return legend;
}

/**
Return the maximum data limits.
*/
public GRLimits getMaxDataLimits () {
	return this._max_lefty_data_limits;
}

/**
Returns the max end date.
@return the max end date.
*/
protected DateTime getMaxEndDate() {
	return this._max_end_date;
}

/**
Returns the max start date.
@return the max start date.
*/
protected DateTime getMaxStartDate() {
	return this._max_start_date;
}

/**
Return the number of time series.
@return the number of time series (useful for automated selection of colors, symbols, etc.)
*/
public int getNumTS () {
	return this.__tslist.size();
}

/**
Return the drawing area used for the full page. The value may be null.
@return The drawing area used for the full page.
*/
public GRJComponentDrawingArea getPageDrawingArea() {
	return this._da_page;
}

/**
Return an instance of a PopupMenu that applies for the graph.
Because multiple graphs can be drawn in a component, the PopupMenu needs to be specific to the graph.
The PopupMenu can then be added to the and shown.
@return a PopupMenu instance appropriate for the graph, or null if no PopupMenu
choices are available (also return null if a reference graph).
*/
public JPopupMenu getJPopupMenu() {
	if (this._is_reference_graph) {
		// Currently no popups are available for reference graphs.
		return null;
	}

	if (this._graph_JPopupMenu != null) {
		// The menu was already created in a previous call and can be returned.
		return this._graph_JPopupMenu;
	}

	// The popups are not created by default because the graph may be
	// being created in batch mode, or the user made not use popups.
	this._graph_JPopupMenu = new JPopupMenu("Graph");

	// Add properties specific to each graph type.
	if ( (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) || (this.__leftYAxisGraphType == TSGraphType.DURATION) ) {
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_ANALYSIS_DETAILS,__MENU_ANALYSIS_DETAILS,this));
		this._graph_JPopupMenu.addSeparator();
	}

	// All graphs have properties.
	this._graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_PROPERTIES, __MENU_PROPERTIES, this));

	// Add ability to set Y Maximum values.
	this._graph_JPopupMenu.addSeparator();

	if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_FILL_WINDOW, __MENU_Y_MAXIMUM_FILL_WINDOW, this));
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_FILL_WINDOW, __MENU_Y_MINIMUM_FILL_WINDOW, this));
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_AUTO, __MENU_Y_MAXIMUM_AUTO, this));
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_AUTO, __MENU_Y_MINIMUM_AUTO, this));
	}
	else {
		this._graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_SET_Y_LIMITS, __MENU_Y_SET_Y_LIMITS, this));
		// TODO sam 2017-04-23 change from separate menu choices to a dialog that provides more clear control:
		// - figure out what to do with predicted value residual
		//_graph_JPopupMenu.addSeparator();
		//_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_AUTO, __MENU_Y_MAXIMUM_AUTO, this));
		//_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_AUTO, __MENU_Y_MINIMUM_AUTO, this));
		//_graph_JPopupMenu.addSeparator();
		//_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MAXIMUM_FILL_WINDOW, __MENU_Y_MAXIMUM_FILL_WINDOW, this));
		//_graph_JPopupMenu.add(new SimpleJMenuItem( __MENU_Y_MINIMUM_FILL_WINDOW, __MENU_Y_MINIMUM_FILL_WINDOW, this));
	}

	// If UNIX, add a refresh option to help with X server differences.
	// TODO sam 2017-04-23 need to evaluate if this is still needed.
	if (IOUtil.isUNIXMachine()) {
		this._graph_JPopupMenu.addSeparator();
		this._graph_JPopupMenu.add(new SimpleJMenuItem(__MENU_REFRESH, __MENU_REFRESH, this));
	}
	return this._graph_JPopupMenu;
}

/**
 * Return a string suitable for raster graph "Range:",
 * which indicates the range that a value falls into.
 * This is used for raster graphs.
 * @param value the time series value used to look up the range
 */
private String getRasterRangeString ( double value ) {
	if ( this.rasterSymbolTable == null ) {
		return "";
	}
	else {
		StringBuilder b = new StringBuilder();
		GRSymbolTableRow row = this.rasterSymbolTable.getSymbolTableRowForValue(value);
		if ( row == null ) {
			return "";
		}
		else {
			if ( !row.getValueMinFullString().equalsIgnoreCase("-Infinity") ) {
				b.append (row.getValueMinFullString());
			}
			if ( !row.getValueMinFullString().equalsIgnoreCase("-Infinity") &&
				!row.getValueMaxFullString().equalsIgnoreCase("Infinity") ) {
				b.append (" AND ");
			}
			if ( !row.getValueMaxFullString().equalsIgnoreCase("Infinity") ) {
				b.append ( row.getValueMaxFullString() );
			}
			return b.toString();
		}
	}
}

/**
Return the list of TSRegression data that applies to the graph, if available.
This can be displayed in a details window for the graph.
@return TSRegression data for graph (use with scatter plot) or null if no analysis has been performed.
*/
public List<TSRegression> getRegressionData () {
	return this._regression_data;
}

/**
Return the drawing area used for the right y-axis graph.  The value may be null.
This is most often used by code that handles mouse events.
@return The drawing area used for the right y-axis graph.
*/
public GRJComponentDrawingArea getRightYAxisGraphDrawingArea() {
	return this._da_righty_graph;
}

/**
Return the graph type for the right y-axis.
@return the graph type for the right y-axis.
*/
public TSGraphType getRightYAxisGraphType () {
	return this.__rightYAxisGraphType;
}

/**
 * Return the list of time series that are selected via legend interactions.
 * @return the list of time series that are selected via legend interactions,
 * guaranteed to be non-null but may be empty.
 */
protected List<TS> getSelectedTimeSeriesList () {
	return this._selectedTimeSeriesList;
}

/**
Returns the start date.
@return the start date.
*/
protected DateTime getStartDate() {
	return this._start_date;
}

/**
Return the subproduct number for the graph.
This is used for example, by TSGraphJComponent to get the ZoomGroup for a TSGraph when a zoom occurs in a TSGraph.
Other TSGraphcan then be zoomed similarly.
@return the subproduct number for the graph (0+)
*/
public int getSubProductNumber () {
	return this.subproduct;
}

/**
Get the time series graph type.
Determine from the TSProduct properties and if not set, return the graph type for the graph,
based on the left or right y-axis graph type.
@param mainGraphType the graph type for the graph, used for time series if time series graph type is not specified
@param its time series in the graph, 0+.
*/
private TSGraphType getTimeSeriesGraphType ( TSGraphType mainGraphType, int its ) {
	//String routine = getClass().getSimpleName() + ".getTimeSeriesGraphType";
    // Do not request the layered property here.
	// Ask for time series property explicitly and then set to the main graph type if no time series graph type property is defined.
    String graphTypeProp = this._tsproduct.getLayeredPropValue ( "GraphType", this.subproduct, its, false );
    TSGraphType tsGraphType = TSGraphType.valueOfIgnoreCase(graphTypeProp);
    if ( tsGraphType == null ) {
    	// Time series did not have a graph type so return the graph type for the graph.
        tsGraphType = mainGraphType;
    }
    //Message.printStatus(2, routine,
    //	"Time series graph type [" + its + "] is " + graphTypeProp + ", main graph type is " + mainGraphType
    //	+ ", returned type is " + tsGraphType);
    return tsGraphType;
}

/**
Return the time series list used for graphing (all the time series).
Some time series may be null if could not be read or are not enabled.
@return the time series list being graphed (all the time series).
*/
public List<TS> getTSList() {
	return this.__tslist;
}

/**
This was previously getEnabledTSList, which did not work as intended because null time series are needed to ensure the position.
Returns a list of all the time series that are enabled.  This will never return a null list.
If no time series are enabled, an empty list will be returned.
@param includeLeftYAxis if true, include left y-axis time series.
@param includeRightYAxis if true, include right y-axis time series.
@param includeNulls if true, include nulls (suitable in cases where the position of time series in the product is important)
or false (suitable in cases where the position of time series are not important).
A null may be due to a read error or time series that is ignored (and therefore not read on purpose)
@return a list of all the time series that are enabled.
*/
public List<TS> getTSListForAxes ( boolean includeLeftYAxis, boolean includeRightYAxis, boolean includeNulls ) {
	if ( (this.__tslist == null) || (this.__tslist.size() == 0) ) {
		// Don't have any time series for data.
		return new ArrayList<TS>();
	}

	int size = this.__tslist.size();
	String propValue = null;
	List<TS> tslist = new ArrayList<>();
	TS ts = null;
	for ( int its = 0; its < size; its++ ) {
		propValue = this._tsproduct.getLayeredPropValue("Enabled", this.subproduct, its, false);
		if ( !isTSEnabled(its) ) {
			// Time series is not enabled so set as null.
			if ( !includeNulls ) {
				// Don't include the time series.
				continue;
			}
			ts = null;
		}
		// Time series is enabled, add to the list if for the requested axis.
		propValue = this._tsproduct.getLayeredPropValue("YAxis", this.subproduct, its, false);
		if ( (propValue == null) || propValue.isEmpty() ) {
			// Default is time series is associated with left axis.
			propValue = "Left";
		}
		if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
			ts = this.__tslist.get(its);
		}
		if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
			ts = this.__tslist.get(its);
		}
		// Add the time series list.
		if ( ts == null ) {
			if ( includeNulls ) {
				tslist.add(ts);
			}
		}
		else {
			tslist.add(ts);
		}
	}
	return tslist;
}

/**
Return the time series list used for graphing (time series for the left y-axis).
@return the time series list being graphed (time series for the left y-axis).
*/
public List<TS> getTSListForLeftYAxis () {
	return this.__left_tslist;
}

/**
Return the time series list used for graphing (time series for the right y-axis).
@return the time series list being graphed (time series for the right y-axis).
*/
public List<TS> getTSListForRightYAxis () {
	return this.__right_tslist;
}

/**
Return the time series list used for graphing.
Depending on the graph type, this may by the original time series, the derived time series list, or both.
This method should be called to get the time series that are actually drawn (with no need for further manipulation).
This method is called by code that determines graph limits, although actually rendering code may take more care.
@param enabledOnly if true, only return the list of enabled time series
@param includeLeftYAxis if true, include left y-axis time series.
@param includeRightYAxis if true, include right y-axis time series.
@return the time series list being rendered.
*/
private List<TS> getTSListToRender ( boolean enabledOnly, boolean includeLeftYAxis, boolean includeRightYAxis ) {
    TSGraphType leftYAxisGraphType = getLeftYAxisGraphType();
	//TSGraphType rightYAxisGraphType = getRightYAxisGraphType();
    List<TS> tslist = null;
    if ( enabledOnly ) {
        // Render only the enabled time series.
    	boolean includeNulls = false;
        tslist = getTSListForAxes ( includeLeftYAxis, includeRightYAxis, includeNulls );
        if ( Message.isDebugOn ) {
	        Message.printStatus(2, "getTSListToRender", "Got " + tslist.size() +
	        	" time series that are enabled, includeLeftYAxis="+includeLeftYAxis + ", includeRightYAxis="+includeRightYAxis);
        }
    }
    else {
        // Render all time series whether enabled or not, but do check for requested axis.
        tslist = getTSList();
        // Filter the time series list based on the axis.
        String propValue;
        List<TS> tslist2 = new ArrayList<>();
        for (int i = 0; i < tslist.size(); i++) {
			propValue = this._tsproduct.getLayeredPropValue("YAxis", this.subproduct, i, false);
			if ( (propValue == null) || propValue.isEmpty() ) {
				propValue = "Left";
			}
			if ( includeLeftYAxis && propValue.equalsIgnoreCase("Left") ) {
				tslist2.add(this.__tslist.get(i));
			}
			if ( includeRightYAxis && propValue.equalsIgnoreCase("Right") ) {
				tslist2.add(this.__tslist.get(i));
			}
    	}
        tslist = tslist2;
    }
    if ( includeLeftYAxis && (leftYAxisGraphType == TSGraphType.AREA_STACKED) ) {
        // Return the derived time series list and the time series not in this list.
        List<TS> tsToRender = new ArrayList<>();
        tsToRender.addAll(getDerivedTSList());
        // Now loop through and add the additional time series.
        int size = tslist.size();
        for ( int its = 0; its < size; its++ ) {
            TSGraphType tsGraphType = getTimeSeriesGraphType(leftYAxisGraphType, its);
            if ( tsGraphType != leftYAxisGraphType ) {
                tsToRender.add ( tslist.get(its) );
            }
        }
        return tsToRender;
    }
    /* TODO smalers 2023-04-07 can now handle 1+ time series.
    else if ( includeLeftYAxis && (leftYAxisGraphType == TSGraphType.RASTER) ) {
        // Return the first time series in the list since only one time series can be displayed.
        List<TS> tsToRender = new ArrayList<>();
        if ( tslist.size() > 0 ) {
            tsToRender.add(tslist.get(0));
        }
        return tsToRender;
    }
    */
    else {
    	// Simpler graph so render all that were found above.
        return tslist;
    }
}

/**
 * Get the x-axis date precision.
 * @return x-axis date precision, useful for formatting mouse-tracker, etc.
 */
protected int getXAxisDateTimePrecision () {
	return this._xaxis_date_precision;
}

/**
Indicate whether the left y-axis graph drawing area for this TSGraph contains the device point that is specified.
This is used to determine whether a component event should impact this TSGraph.
@param devpt a point of interest, in raw device units (not GR plotting units).
@return true if devpt is in the graph drawing area.
*/
public boolean graphContains ( GRPoint devpt ) {
	// The check MUST be done in device units because more than one graph may share the same device units.
	// be able to optimize this when there is time.
	return this._da_lefty_graph.getPlotLimits(GRCoordinateType.DEVICE).contains ( devpt);
}

/**
Indicate whether units are being ignored on the left axis.
*/
public boolean ignoreLeftYAxisUnits () {
	return this._ignoreLeftAxisUnits;
}

/**
Indicate whether units are being ignored on the right axis.
*/
public boolean ignoreRightYAxisUnits () {
	return this._ignoreRightAxisUnits;
}

/**
Indicate whether the graph is for a reference graph.
*/
public boolean isReferenceGraph () {
	return this._is_reference_graph;
}

/**
 * Indicate whether the time series is selected (highlighted in legend).
 * @param ts time series to evaluate.
 * @return true if the time series is selected, false if not.
 */
protected final boolean isTimeSeriesSelected ( TS ts ) {
	for ( TS ts2 : this._selectedTimeSeriesList ) {
		if ( ts2 == ts ) {
			return true;
		}
	}
	return false;
}

/**
Returns whether the time series for the graph is enabled.
@param its the time series to check (0+).
@return true if the time series is enabled, false if not.
*/
private boolean isTSEnabled ( int its ) {
	String propValue = this._tsproduct.getLayeredPropValue("Enabled", this.subproduct, its, false);
	if ( (propValue != null) && propValue.equalsIgnoreCase("False") ) {
		return false;
	}
	return true;
}

/**
 * Modify the line with for the time series because it is selected.
 * @param lineWidth original line width.
 * @return modified line width to use for drawing.
 */
private int modifyLineWidthForSelectedTimeSeries ( int lineWidth ) {
	// TODO sam 2017-02-20 Put this in a method once figure it out.
	String propValue = getLayeredPropValue("SelectedTimeSeriesLineWidth", this.subproduct, -1, false, null);
	if ( propValue != null ) {
		// Adjust the line width.
		if ( StringUtil.isDouble(propValue) ) {
			lineWidth = (int)Double.parseDouble(propValue);
		}
		else if ( propValue.startsWith("x") && (propValue.length() > 1) && StringUtil.isDouble(propValue.substring(1).trim()) ) {
			// x5 would multiple width by 5.
			lineWidth = (int)(lineWidth*Double.parseDouble(propValue.substring(1).trim()));
		}
		else if ( propValue.startsWith("+") && (propValue.length() > 1) && StringUtil.isDouble(propValue.substring(1).trim()) ) {
			// +5 would add 5 to width.
			lineWidth = lineWidth + (int)Double.parseDouble(propValue.substring(1).trim());
		}
	}
	return lineWidth;
}

/**
Open the drawing areas and set the drawing limits to position all drawing "real estate".
This method is called at initialization and also from TSGraphJComponent.reinitializeGraphs()
to ensure that TSProduct properties are properly reflected in the product layout.
Some drawing areas may overlap but each has its own space.
Data limits are initialized to non-null values (and will be reset in setDataLimits()).
*/
private void openDrawingAreas () {
	boolean log_y_left = false;
	boolean log_y_right = false;
	boolean log_xy_scatter = false;
	String prop_val = this._tsproduct.getLayeredPropValue ( "LeftYAxisType", this.subproduct, -1, false );
	if ( prop_val.equalsIgnoreCase("Log") ) {
		log_y_left = true;
	}
	prop_val = this._tsproduct.getLayeredPropValue ( "RightYAxisType", this.subproduct, -1, false );
	if ( prop_val.equalsIgnoreCase("Log") ) {
		log_y_right = true;
	}
	prop_val = this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
	if ((prop_val != null) && prop_val.equalsIgnoreCase("Log")) {
		log_y_left = false;
		log_xy_scatter = true;
	}

	// Full page.

	this._da_page = new GRJComponentDrawingArea ( this._dev, "TSGraph.Page",
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_page = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_page.setDataLimits ( this._datalim_page );

	// Error drawing area for major issues that user must correct.

	this._da_error = new GRJComponentDrawingArea ( this._dev, "TSGraph.Error",
		GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_error = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_error.setDataLimits ( this._datalim_error );

	// Drawing area for main title.

	this._da_maintitle = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.MainTitle", GRAspectType.FILL,	null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_maintitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_maintitle.setDataLimits ( this._datalim_maintitle );

	// Drawing area for sub title.

	this._da_subtitle = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.SubTitle", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_subtitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_subtitle.setDataLimits ( this._datalim_subtitle );

	// Top X axis...

	this._da_topx_title = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.TopXTitle", GRAspectType.FILL,	null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_topx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_topx_title.setDataLimits ( this._datalim_topx_title );

	this._da_topx_label = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.TopXLabels", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_topx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_topx_label.setDataLimits ( this._datalim_topx_label );
	if (log_xy_scatter) {
		// Both axes are log.
//		GRDrawingAreaUtil.setAxes(this._da_topx_label, GRAxis.LOG, GRAxis.LINEAR);
	}

	// Y axis titles.

	this._da_lefty_title = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftYTitle", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_lefty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_lefty_title.setDataLimits ( this._datalim_lefty_title );

	this._da_righty_title = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.RightYTitle", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_righty_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_righty_title.setDataLimits ( this._datalim_righty_title );

	// Left Y axis labels.

	this._da_lefty_label = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftYLabel", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_lefty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_lefty_label.setDataLimits ( this._datalim_lefty_label );
	if (log_y_left) {
		// For now, only support log axes in the Y axis.
		GRDrawingAreaUtil.setAxes(this._da_lefty_label, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(this._da_lefty_label, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
	}

	// Drawing area for left y-axis graphing.

	this._da_lefty_graph = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftYAxisGraph", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	// Initial values that will be reset pretty quickly.
	GRLimits datalim_lefty_graph = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
	    datalim_lefty_graph.reverseY();
	}
	this._da_lefty_graph.setDataLimits ( datalim_lefty_graph );

	if (log_y_left) {
		// For now, only support log axes in the Y axis.
		GRDrawingAreaUtil.setAxes(this._da_lefty_graph, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
	}
	else if (log_xy_scatter) {
		GRDrawingAreaUtil.setAxes(this._da_lefty_graph, GRAxisScaleType.LINEAR, GRAxisScaleType.LINEAR);
	}

	// Drawing area for right y-axis graphing.

	this._da_righty_graph = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.RightYAxisGraph", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	// Initial values that will be reset pretty quickly.
	GRLimits datalim_righty_graph = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	if ( this.__rightyDirection == GRAxisDirectionType.REVERSE ) {
	    datalim_righty_graph.reverseY();
	}
	this._da_righty_graph.setDataLimits ( datalim_righty_graph );
	if (log_y_right) {
		// For now, only support log axes in the Y axis.
		GRDrawingAreaUtil.setAxes(this._da_righty_graph, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
	}

	// Right Y axis labels.

	this._da_righty_label = new GRJComponentDrawingArea ( this._dev,
		"TSGraph.RightYLabel", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_righty_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_righty_label.setDataLimits ( this._datalim_righty_label );

	// Drawing area for bottom X axis.

	this._da_bottomx_label = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.BottomXLabel",	GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_bottomx_label = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_bottomx_label.setDataLimits ( this._datalim_bottomx_label );
	if (log_xy_scatter) {
		// Both axes are log.
//		GRDrawingAreaUtil.setAxes(this._da_bottomx_label, GRAxis.LOG, GRAxis.LINEAR);
	}

	this._da_bottomx_title = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.BottomXTitle",	GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_bottomx_title = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_bottomx_title.setDataLimits ( this._datalim_bottomx_title );

	// Legend for left y-axis (open drawing areas for each legend area, although currently only one will be used).

	this._da_lefty_bottom_legend = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftyYAxisBottomLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_lefty_bottom_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_lefty_bottom_legend.setDataLimits ( this._datalim_lefty_bottom_legend );

	this._da_lefty_left_legend = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftYAxisLeftLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_lefty_left_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_lefty_left_legend.setDataLimits ( this._datalim_lefty_left_legend );

	this._da_lefty_right_legend = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.LeftYAxisRightLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_lefty_right_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_lefty_right_legend.setDataLimits ( this._datalim_lefty_right_legend );

	this._da_lefty_inside_legend = new GRJComponentDrawingArea(this._dev,
		"TSGraph.LeftYAxisInsideLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null);
	this._datalim_lefty_inside_legend = new GRLimits(0.0, 0.0, 1.0, 1.0);
	this._da_lefty_inside_legend.setDataLimits(this._datalim_lefty_inside_legend);

	// Legend for right y-axis (open drawing areas for each legend area, although currently only one will be used).

	this._da_righty_bottom_legend = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.RightYAxisBottomLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_righty_bottom_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_righty_bottom_legend.setDataLimits ( this._datalim_righty_bottom_legend );

	this._da_righty_left_legend = new GRJComponentDrawingArea ( this._dev,
			"TSGraph.RightYAxisLeftLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_righty_left_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_righty_left_legend.setDataLimits ( this._datalim_righty_left_legend );

	this._da_righty_right_legend = new GRJComponentDrawingArea ( this._dev,
		"TSGraph.RightYAxisRightLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_righty_right_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_righty_right_legend.setDataLimits ( this._datalim_righty_right_legend );

	// Raster graph legend overlaps this._da_righty_right_legend (can have one or the other).
	this._da_right_raster_legend = new GRJComponentDrawingArea ( this._dev,
		"TSGraph.RightRasterGraphLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	this._datalim_right_raster_legend = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	this._da_right_raster_legend.setDataLimits ( this._datalim_right_raster_legend );

	this._da_righty_inside_legend = new GRJComponentDrawingArea(this._dev,
		"TSGraph.RightYAxisInsideLegend", GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null);
	this._datalim_righty_inside_legend = new GRLimits(0.0, 0.0, 1.0, 1.0);
	this._da_righty_inside_legend.setDataLimits(this._datalim_righty_inside_legend);
}

/**
Update the TSGraph visible image.  For performance reasons,
it is assumed that the state of the GRDevice passed into the constructor will be consistent with the Graphics that is being used
(e.g., if the main device is printing then the graphics will be a print graphics).
It is assumed that the GRDevice will also control double buffering and the handling of the image and therefore if this method is called,
drawing must need to occur (because of a zoom or component resize).
Therefore, the drawing limits are checked for each call.
For a reference graph component, this method should only be called for the TSGraph that corresponds to the reference time series.
@param g Graphics instance to use for drawing.
*/
public void paint ( Graphics g ) {
	String routine = getClass().getSimpleName() + ".paint";

	if ( g == null ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(1, routine, this._gtype + "Null Graphics in paint()");
		}
		return;
	}

	// Use Graphics2D for advanced 2D rendering.
	Graphics2D g2d = (Graphics2D)g;
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	// Reset to Graphics2D for futher processing.
	g = g2d;

	// Print some messages so know what the paint is doing.

	if ( Message.isDebugOn ) {
		if ( this._dev.isPrinting() ) {
			Message.printDebug ( 1, routine, this._gtype + "Painting TSGraph for printing..." );
		}
		else if ( this._is_reference_graph ) {
			Message.printDebug ( 1, routine, this._gtype + "Painting reference graph..." );
		}
		else {
		    Message.printDebug ( 1,	routine, this._gtype + "Painting main graph..." );
		}
	}

	try {
		// Main try.
		// The following will be executed at initialization.
		// The code is here because a valid Graphics is needed to check font sizes, etc.

		this._graphics = g2d;

		// Now set the drawing limits based on the current size of the device.

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Calling setDrawingLimits..." );
		}
		setDrawingLimits ( this._drawlim_page );

		// If the graph type has changed, redo the data analysis.
		// Currently the Properties interface does not allow the graph type to be changed.
		// However, XY Scatter parameters can be changed.

		boolean needToAnalyze = false;
		if ( this.__leftYAxisGraphType != this.__lastLeftYAxisGraphType ) {
			needToAnalyze = true;
		}
		if ( this.__leftYAxisGraphType == TSGraphType.XY_SCATTER ) {
			// Check the properties in the old regression results and see if they differ from the current TSProduct properties.
			// If they do, then reanalyze the data.
			// Assume for now that the properties for the first regression apply to all regression data.

			if ((this._regression_data != null) && (this._regression_data.size()>0)) {
				if (!this._tsproduct.getLayeredPropValue( "XYScatterMethod", this.subproduct, -1, false).equalsIgnoreCase(
				      "" + this._regression_data.get(0).getAnalysisMethod())
				  || !this._tsproduct.getLayeredPropValue ( "XYScatterMonth", this.subproduct, -1, false).equalsIgnoreCase(
			          StringUtil.formatNumberSequence(this._regression_data.get(0).getAnalysisMonths(),","))
			  	  || !this._tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", this.subproduct, -1, false ).equalsIgnoreCase(
			  	      "" + this._regression_data.get(0).getNumberOfEquations())
				  || !this._tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false ).equalsIgnoreCase(
			          "" + this._regression_data.get(0).getTransformation()) ) {
					needToAnalyze = true;
				}
			}
		}
		if ( needToAnalyze ) {
			// Redo the analysis, which calls computeDataLimits() at the end.
			// - problem, if analysis does not need to be redone then computeDataLimits() is not called.
			doAnalysis ( getLeftYAxisGraphType() );
			// This is the place where the reference graph has its data set.
		}
		this.__lastLeftYAxisGraphType = this.__leftYAxisGraphType;

		// Compute the labels for the data, which will set the this._datalim_graph, which is used to set other data labels:
		// - mainly this picks nice bounds to data so that labels will have even numbers

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, this._gtype + "Computing labels..." );
		}
		if ( !this._is_reference_graph ) {
			computeLabels ( this._tslimits_lefty, this._tslimits_righty );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, this._gtype + "Set initial data limits to " + this._data_lefty_limits );
			}
		}

		if ( this._is_reference_graph ) {
			// Fill in the background to gray.
			this._da_lefty_graph.setColor ( GRColor.gray );
			GRDrawingAreaUtil.fillRectangle ( this._da_lefty_graph, this._max_lefty_data_limits.getLeftX(),
				this._max_lefty_data_limits.getBottomY(), this._max_lefty_data_limits.getWidth(),
				this._max_lefty_data_limits.getHeight() );
			// Highlight current data (zoom) limits in white.
			this._da_lefty_graph.setColor ( GRColor.white );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine, this._gtype + "Data limits for reference box are " + this._data_lefty_limits );
			}
			// Get the Y-dimension from the maximum values.
			GRDrawingAreaUtil.fillRectangle ( this._da_lefty_graph, this._data_lefty_limits.getLeftX(),
				this._data_lefty_limits.getBottomY(), this._data_lefty_limits.getWidth(),
				this._max_lefty_data_limits.getHeight() );
			// Also draw a line in case are zoomed in so far that the rectangle does not draw.
			GRDrawingAreaUtil.drawLine ( this._da_lefty_graph, this._data_lefty_limits.getLeftX(),
				this._data_lefty_limits.getBottomY(), this._data_lefty_limits.getLeftX(), this._data_lefty_limits.getTopY() );
			// The time series will graph over the background in the following code.
		}

		// Now draw the graph(s).

		// There are checks in each method to see whether a reference graph should do anything.
		// This allows some experimentation with reference graphs that display some information.
		checkInternalProperties (); // Update dynamic data.
		drawTitles (); // Draw main titles.
		drawAnnotations(this._tsproduct, this._da_lefty_graph, this._da_righty_graph, TSGraphDrawingStepType.BEFORE_BACK_AXES); // Draw annotations under the back axes.
		drawAxesBack (); // Draw the axes that are below data.
		drawAnnotations(this._tsproduct, this._da_lefty_graph, this._da_righty_graph, TSGraphDrawingStepType.BEFORE_DATA); // Draw annotations that are below data.
		drawTimeSeriesAnnotations(); // Draw annotations for specific time series (currently does nothing).
		// Draw the time series based on axes.
		TSGraphType graphType = getLeftYAxisGraphType();
		if ( graphType == TSGraphType.DURATION ) {
			// All time series are drawn together.
			drawGraph ( getLeftYAxisGraphType(), this._da_lefty_graph, this._tsproduct, getTSList(), this._data_lefty_limits );
		}
		else {
			// Else draw left axis time series and then right axis time series.
			drawGraph ( getLeftYAxisGraphType(), this._da_lefty_graph, this._tsproduct, getTSListForLeftYAxis(), this._data_lefty_limits );
			drawGraph ( getRightYAxisGraphType(), this._da_righty_graph, this._tsproduct, getTSListForRightYAxis(), this._data_righty_limits );
		}
		drawAnnotations(this._tsproduct, this._da_lefty_graph, this._da_righty_graph, TSGraphDrawingStepType.AFTER_DATA); // Draw annotations that are on top of data
		drawAxesFront ( this._tsproduct,
			this._da_maintitle,
			this.__drawLeftyLabels, this.__leftYAxisGraphType,
			this._da_lefty_graph, this._da_lefty_title, this._da_lefty_label,
			this._data_lefty_limits, this._datalim_lefty_title, this._datalim_lefty_label,
			this._ylabels_lefty, this._lefty_precision,
			this.__drawRightyLabels, this.__rightYAxisGraphType,
			this._da_righty_graph, this._da_righty_title, this._da_righty_label,
			this._data_righty_limits, this._datalim_righty_title, this._datalim_righty_label,
			this._ylabels_righty, this._righty_precision,
			//__drawBottomxLabels, // TODO SAM 2016-10-23 Enable in the future.
			this._da_bottomx_title, this._da_bottomx_label,
			this._datalim_bottomx_title, this._datalim_bottomx_label,
			this._xlabels ); // Draw axes components that are in front of data.
		drawCurrentDateTime (); // Draw the current date/time line (TODO SAM 2016-10-20 should this be attempted multiple times like annotations?).
		drawLegend ( GRAxisEdgeType.LEFT ); // Draw the left y-axis legend, which may be on top of the graph.
		drawLegend ( GRAxisEdgeType.RIGHT ); // Draw the right y-axis legend, which may be on top of the graph.
		// The following is in the product properties ShowDrawingAreaOutline and is useful during development.
		if ( this._showDrawingAreaOutline ) {
			drawDrawingAreas ();
		}
		drawErrors ( this._da_error );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e ); // Put first because sometimes does not output if after.
		Message.printWarning ( 1, routine, this._gtype + "Error drawing graph." );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "...done painting TSGraph." );
	}
}

/**
Set whether the start_date and end_date were set via the setEndDate and
setStartDate methods and should be used for calculating the date limits.
@param useSetDates if true, use the set dates to limit the maximum period,
generally passed in, for example, when time series product file constrains the period.
*/
public void setComputeWithSetDates(boolean useSetDates) {
	this.__useSetDates = useSetDates;
}

/**
This method is called from user interface code to trigger redraws, typically from zooming and scrolling.
Redraws of the canvas trigger a call to paint().
Reset the data limits prior to redrawing.  For example call when a zoom event occurs and tsViewZoom() is called.
If a reference graph, the overall limits will remain the same but the box for the zoom location will move to the specified limits.
For typical time series plots, the x-axis limits are the floating point year and the y-axis are data values.
An exception, for example is a scatter plot,
where the y-axis limits are the data limits from the first time series and the x-axis limits are the data limits from the second time series.
@param datalim_lefty_graph Data limits for the left y-axis graph.
The data limits are either the initial values or the values from a zoom.
*/
public void setDataLimitsForDrawing ( GRLimits datalim_lefty_graph ) {
	String routine = getClass().getSimpleName() + ".setDataLimitsForDrawing";
    if ( datalim_lefty_graph == null ) {
		return;
	}
	// FIXME JTS exceptions thrown when trying to zoom.
	if ( (this._end_date == null) && (this._start_date == null) ) {
		return;
	}
	// FIXME JTS.
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine,
			this._gtype + "Setting [" + this.subproduct + "] this._data_lefty_limits to " + datalim_lefty_graph.toString());
	}

	GRLimits dataLimitsInDrawingArea = null;
	if ( this._is_reference_graph ) {
		// Save the new data limits for drawing but do not reset the actual GRDrawingArea.
		// Also make sure the Y limits are the maximum.
		this._data_lefty_limits = new GRLimits ( datalim_lefty_graph );
		this._data_lefty_limits.setTopY ( this._max_lefty_data_limits.getTopY() );
		this._data_lefty_limits.setBottomY ( this._max_lefty_data_limits.getBottomY() );
		dataLimitsInDrawingArea = new GRLimits(this._data_lefty_limits);
		if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
		    // Reverse the data limits used for the reference graph - will be set in drawing area below.
		    dataLimitsInDrawingArea.reverseY();
		}
	}
	else {
	    // Do the full recalculation of the data limits and zoom.
		// Need to recompute new start and end dates.
		// Make sure to keep the same date precision.

		// TODO SAM 2016-10-24 the x-axis limits are currently shared between left and right y-axis:
		// - need to confirm that dates are determined for full time series list

		// Left y-axis

		this._start_date = new DateTime ( datalim_lefty_graph.getLeftX(), true );
		this._start_date.setPrecision ( this._start_date.getPrecision() );
		this._end_date = new DateTime ( datalim_lefty_graph.getRightX(), true );
		this._end_date.setPrecision ( this._start_date.getPrecision() );

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			this._gtype + "Set _start_date to " + this._start_date + " _end_date to " + this._end_date );
		}
		// Left y-axis time series only.
		boolean includeLeftYAxis = true;
		boolean includeRightYAxis = false;
		try {
		    // Recompute the limits, based on the period and data values.
			List<TS> tslistToRender = getTSListToRender ( true, includeLeftYAxis, includeRightYAxis );
			if ( tslistToRender.size() == 0) {
				this._tslimits_lefty = null;
				return;
			}
			else {
				//_tslimits = TSUtil.getDataLimits( getEnabledTSList(), this._start_date, this._end_date, "", false, this._ignore_units);
			    this._tslimits_lefty = TSUtil.getDataLimits( tslistToRender, this._start_date, this._end_date, "", false, this._ignoreLeftAxisUnits);
			    computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._tslimits_lefty);
				if (this.__leftYAxisGraphType == TSGraphType.PERIOD){
					// Set the minimum value to 0 and the maximum value to one more than the number of time series.
					// Reverse the limits to number the same as the legend.
					this._tslimits_lefty.setMaxValue(0.0);
					boolean includeNulls = false;
					// Do not include null time series since computing data limits.
					this._tslimits_lefty.setMinValue( getTSListForAxes(includeLeftYAxis, includeRightYAxis, includeNulls).size() + 1);
				}
				else if (this.__leftYAxisGraphType == TSGraphType.RASTER) {
				    // Reset the y-axis values to the year - use Max because don't allow zoom.
					if ( (this.__tslist.size() == 1)
						&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
    					// Single time series.
						this._tslimits_lefty.setMinValue(this._max_tslimits_lefty.getDate1().getYear());
						this._tslimits_lefty.setMaxValue(this._max_tslimits_lefty.getDate2().getYear() + 1);
					}
					else {
						// Multiple time series or single year interval time series.
						// Treat similar to period of record graph.
						// Set the minimum value to 0 and the maximum value to the number of time series.
						// Reverse the limits to number the same as the legend so the first time series is at the top.
						this._tslimits_lefty.setMaxValue(0.0);
						// Do not include null time series since computing data limits.
						boolean includeNulls = false;
						this._tslimits_lefty.setMinValue( getTSListForAxes(includeLeftYAxis, includeRightYAxis, includeNulls).size());
					}
				}
				else {
					// All other graphs.
					if (!_zoomKeepFullPeriodYLimits) {
						// False so allow user-specified y-axis limits to be used if specified.
						// Default to values set in the property.
						this._tslimits_lefty.setMinValue ( this._max_tslimits_lefty.getMinValue() );
						this._tslimits_lefty.setMaxValue ( this._max_tslimits_lefty.getMaxValue() );
						// If user has indicated overriding axis limits, use specified values.
						String leftYAxisViewMinY = getLeftYAxisViewMinY();
						if ( StringUtil.isDouble(leftYAxisViewMinY) ) {
							this._tslimits_lefty.setMinValue(Double.parseDouble(leftYAxisViewMinY));
							Message.printStatus(2, routine, "Setting min time series value to user minimum " + leftYAxisViewMinY);
						}
						String leftYAxisViewMaxY = getLeftYAxisViewMaxY();
						if ( StringUtil.isDouble(leftYAxisViewMaxY) ) {
							this._tslimits_lefty.setMaxValue(Double.parseDouble(leftYAxisViewMaxY));
							Message.printStatus(2, routine, "Setting max time series value to user maximum " + leftYAxisViewMaxY);
						}
						// Also check whether the value should have been taken from a data view.
						if ( leftYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
							if ( !Double.isNaN(this.leftYAxisViewMinYFromData) ) {
								this._tslimits_lefty.setMinValue(this.leftYAxisViewMinYFromData);
								Message.printStatus(2, routine, "Setting min time series value to data view minimum " + this.leftYAxisViewMinYFromData);
							}
						}
						if ( leftYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
							if ( !Double.isNaN(this.leftYAxisViewMaxYFromData) ) {
								this._tslimits_lefty.setMaxValue(this.leftYAxisViewMaxYFromData);
								Message.printStatus(2, routine, "Setting max time series value to data view maximum " + this.leftYAxisViewMaxYFromData);
							}
						}
					}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, this._gtype + " Error getting dates for plot." );
			Message.printWarning ( 2, routine + "(" + this._gtype + ")", e );
			return;
		}

		// Right y-axis limits.

		try {
		    // Recompute the limits, based on the visible period and time series data values in the period.
			// Right y-axis time series only.
			includeLeftYAxis = false;
			includeRightYAxis = true;
			boolean enabledOnly = true;
			List<TS> tslistToRender = getTSListToRender(enabledOnly,includeLeftYAxis,includeRightYAxis);
			if ( tslistToRender.size() == 0) {
				this._tslimits_righty = null;
				return;
			}
			else {
				//_tslimits = TSUtil.getDataLimits( getEnabledTSList(), this._start_date, this._end_date, "", false, this._ignore_units);
			    this._tslimits_righty = TSUtil.getDataLimits( tslistToRender, this._start_date, this._end_date, "", false, this._ignoreRightAxisUnits);
			    computeDataLimits_CheckDisplayLimitProperties(tslistToRender, this._tslimits_righty);
				if (this.__rightYAxisGraphType == TSGraphType.PERIOD){
					// Set the minimum value to 0 and the maximum value to one more than the number of time series.
					// Reverse the limits to number the same as the legend.
					this._tslimits_righty.setMaxValue(0.0);
					this._tslimits_righty.setMinValue( tslistToRender.size() + 1);
				}
				else if (this.__rightYAxisGraphType == TSGraphType.RASTER) {
					if ( (this.__tslist.size() == 1)
						&& ((this.__tslist.get(0) != null) && (this.__tslist.get(0).getDataIntervalBase() != TimeInterval.YEAR)) ) {
						// Single time series that is not year interval.
						// Reset the y-axis values to the year - use Max because don't allow zoom.
						this._tslimits_righty.setMinValue(this._max_tslimits_righty.getDate1().getYear());
						this._tslimits_righty.setMaxValue(this._max_tslimits_righty.getDate2().getYear() + 1);
					}
					else {
						// Multiple time series or single year interval time series.
						// Treat similar to period of record graph.
						// Set the minimum value to 0 and the maximum value to the number of time series.
						// Reverse the limits to number the same as the legend so the first time series is at the top.
						this._tslimits_righty.setMaxValue(0.0);
						this._tslimits_righty.setMinValue( tslistToRender.size() );
					}
				}
				if (!this._zoomKeepFullPeriodYLimits) {
					// False so allow user-specified y-axis limits to be used if specified.
					//Message.printStatus(2,routine,"_tslimits_righty="+_tslimits_righty + ", this._max_tslimits_righty=" + this._max_tslimits_righty);
					this._tslimits_righty.setMinValue ( this._max_tslimits_righty.getMinValue() );
					this._tslimits_righty.setMaxValue ( this._max_tslimits_righty.getMaxValue() );
					// If use has indicated overriding, use specified values.
					String rightYAxisViewMinY = getRightYAxisViewMinY();
					if ( StringUtil.isDouble(rightYAxisViewMinY) ) {
						this._tslimits_righty.setMinValue(Double.parseDouble(rightYAxisViewMinY));
						Message.printStatus(2, routine, "Setting min time series value to user minimum " + rightYAxisViewMinY);
					}
					String rightYAxisViewMaxY = getRightYAxisViewMaxY();
					if ( StringUtil.isDouble(rightYAxisViewMaxY) ) {
						this._tslimits_righty.setMaxValue(Double.parseDouble(rightYAxisViewMaxY));
						Message.printStatus(2, routine, "Setting max time series value to user maximum " + rightYAxisViewMaxY);
					}
					// Also check whether the value should have been taken from a data view.
					if ( rightYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
						if ( !Double.isNaN(this.rightYAxisViewMinYFromData) ) {
							this._tslimits_righty.setMinValue(this.rightYAxisViewMinYFromData);
							Message.printStatus(2, routine, "Setting min time series value to data view minimum " + this.rightYAxisViewMinYFromData);
						}
					}
					if ( rightYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_AUTOFILL_AND_KEEP) ) {
						if ( !Double.isNaN(this.rightYAxisViewMaxYFromData) ) {
							this._tslimits_righty.setMaxValue(this.rightYAxisViewMaxYFromData);
							Message.printStatus(2, routine, "Setting max time series value to data view maximum " + this.rightYAxisViewMaxYFromData);
						}
					}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, this._gtype + " Error getting dates for plot." );
			Message.printWarning ( 2, routine + "(" + this._gtype + ")", e );
			return;
		}
		// Set the graph data limits based on the labels, for example to increase the buffer beyond the data range.
		// This will set this._datalim_graph.
		// The Y limits are computed from the max data limits.
		// The X limits are computed from this._start_date and this._end_date.
		/* THIS IS THE ORIGINAL CODE BEFORE ADDING RIGHT Y-AXIS.
		if ( getTSListToRender(true,includeLeftYAxis,includeRightYAxis).size() > 0) {
			// Only process the left y-axis
			computeLabels ( this._tslimits_lefty, null );
		    dataLimitsInDrawingArea = new GRLimits(this._data_lefty_limits);
		    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph.
		        dataLimitsInDrawingArea.reverseY();
		    }
			this._da_lefty_graph.setDataLimits ( dataLimitsInDrawingArea );
		}
		*/
		// Set the graph data limits based on the labels, for example to increase the buffer beyond the data range.
		// This will set this._datalim_lefty_graph and this._datalim_righty_graph.
		// The Y limits are computed from the max data limits.
		// The X limits are computed from this._start_date and this._end_date.
		includeLeftYAxis = true;
		includeRightYAxis = true;
		if ( getTSListToRender(true,includeLeftYAxis,includeRightYAxis).size() > 0) {
			// Have something to draw.
			// Process both axes because right axis may be linked to left.
			computeLabels ( this._tslimits_lefty, this._tslimits_righty );
			// Reverse left y-axis if requested.
		    dataLimitsInDrawingArea = new GRLimits(this._data_lefty_limits);
		    if ( this.__leftyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph.
		        dataLimitsInDrawingArea.reverseY();
		    }
			this._da_lefty_graph.setDataLimits ( dataLimitsInDrawingArea );
			// Reverse right y-axis if requested.
		    dataLimitsInDrawingArea = new GRLimits(this._data_righty_limits);
		    if ( this.__rightyDirection == GRAxisDirectionType.REVERSE ) {
		        // Reverse the data limits used for the reference graph.
		        dataLimitsInDrawingArea.reverseY();
		    }
			this._da_righty_graph.setDataLimits ( dataLimitsInDrawingArea );
		}

		// TODO SAM 2016-10-24 need to enable.
	}
	if ( Message.isDebugOn ) {
		Message.printDebug(1, routine, this._gtype + "After reset, [" + this.subproduct + "] this._datalim_lefty_graph are " + datalim_lefty_graph );
	}
}

/**
Set the derived time series list.
@param derivedTSList derived time series list, needed when time series are computed in some way
*/
private void setDerivedTSList ( List<TS> derivedTSList ) {
    this.__derivedTSList = derivedTSList;
}

/**
Set the drawing limits for all drawing areas based on properties and window size.
The drawing limits are all set to within the limits of the device limits that are passed in
(initially the limits from the GRJComponentDevice when the
TSGraph was constructed and later the limits from the GRJComponentDevice as it resizes).
Axes are set to log if the properties indicate to do so.
@param drawlim_page Drawing limits for the full extent of the graph within a GRJComponentDevice,
corresponding to canvas/component width and height in pixels (or part of a canvas/component).
*/
public void setDrawingLimits ( GRLimits drawlim_page ) {
	// Buffer around drawing areas (helps separate things and also makes it easier to see drawing areas when in debug mode.
	double buffer = 2.0;
	String routine = getClass().getSimpleName() + ".setDrawingLimits";

	// Declare local variables to make it easier to know what is being retrieved.
	// May in the future minimize global references.
	//TSGraphType leftYAxisGraphType = this.__leftYAxisGraphType;
	TSGraphType rightYAxisGraphType = this.__rightYAxisGraphType;
	TSProduct tsproduct = this._tsproduct;
	List<TS> tslistLeftYAxis = this.__left_tslist; // Left y-axis time series.
	List<TS> tslistRightYAxis = this.__right_tslist; // Right y-axis time series.

	boolean log_y_lefty = false;
	boolean log_y_righty = false;
	boolean log_xy_scatter_lefty = false;
	String prop_value = tsproduct.getLayeredPropValue ( "LeftYAxisType", this.subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y_lefty = true;
	}
	prop_value = tsproduct.getLayeredPropValue ( "XYScatterTransformation", this.subproduct, -1, false );
	if ((prop_value != null) && prop_value.equalsIgnoreCase("Log")) {
		log_y_lefty = false;
		// TODO JTS - 2005-09-07 -- this isn't working right now when the axis types below are set.
		//		log_xy_scatter = true;
	}
	prop_value = tsproduct.getLayeredPropValue ( "RightYAxisType", this.subproduct, -1, false );
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("Log") ) {
		log_y_righty = true;
	}

	// Figure out dimensions up front based on font size.

	// Drawing areas will have zero size if nothing is drawn in them.

	double mainTitleHeight = 0.0;
	String mainTitleString = tsproduct.getLayeredPropValue ( "MainTitleString", this.subproduct, -1, false );
	if ( (mainTitleString != null) && !mainTitleString.equals("") ) {
		// Get the text extents and set the height based on that.
		String mainTitleFontName = tsproduct.getLayeredPropValue ( "MainTitleFontName", this.subproduct, -1, false );
		String mainTitleFontSize = tsproduct.getLayeredPropValue ( "MainTitleFontSize", this.subproduct, -1, false );
		String mainTitleFontStyle = tsproduct.getLayeredPropValue ( "MainTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_maintitle, mainTitleFontName, mainTitleFontStyle, StringUtil.atod(mainTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_maintitle, mainTitleString, GRUnits.DEVICE );
		mainTitleHeight = textLimits.getHeight();
	}

	double subTitleHeight = 0.0;
	String subTitleString = tsproduct.getLayeredPropValue ( "SubTitleString", this.subproduct, -1, false );
	if ( (subTitleString != null) && !subTitleString.equals("") ) {
		// Get the text extents and set the height based on that.
		String subTitleFontName = tsproduct.getLayeredPropValue ( "SubTitleFontName", this.subproduct, -1, false );
		String subTitleFontSize = tsproduct.getLayeredPropValue ( "SubTitleFontSize", this.subproduct, -1, false );
		String subTitleFontStyle = tsproduct.getLayeredPropValue ( "SubTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_subtitle, subTitleFontName, subTitleFontStyle, StringUtil.atod(subTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_subtitle, subTitleString, GRUnits.DEVICE );
		subTitleHeight = textLimits.getHeight();
	}

	// TODO smalers 2016-10-17 Need to more intelligently set label width by checking labels for the maximum values.
	// TODO smalers Get max *10 and compute label width so don't have to rely on full label determination?).
	// For now, hard-code the y-axis label widths.
	// This has been done for some time so it should be OK.

	double leftYAxisLabelWidth = 80.0; // Height is set to graph height below.
	double rightYAxisLabelWidth = 30.0; // Height is set to graph height below, 30 allows for date overflow.
	if ( rightYAxisGraphType != TSGraphType.NONE ) {
		// Right y-axis is requested so set width similar to left.
		rightYAxisLabelWidth = 80.0;
	}

	// Left y-axis title.

	// Maximum height of y-axis left and right top titles, used for vertical calculations.
	double yAxisTitleTopHeight = 0.0;
	double leftYAxisTitleHeight = 0.0;
	double leftYAxisTitleWidth = 0.0;
	String leftYAxisTitleString = tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", this.subproduct, -1, false );
	String leftYAxisTitlePosition = tsproduct.getLayeredPropValue ( "LeftYAxisTitlePosition", this.subproduct, -1, false );
	if ( (leftYAxisTitleString != null) && !leftYAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that.
		String leftYAxisTitleFontName = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", this.subproduct, -1, false );
		String leftYAxisTitleFontSize = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", this.subproduct, -1, false );
		String leftYAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_lefty_title, leftYAxisTitleFontName, leftYAxisTitleFontStyle, StringUtil.atod(leftYAxisTitleFontSize) );
		double rotation = 0.0;
		String leftYAxisTitleRotation = tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", this.subproduct, -1, false );
		if ( (leftYAxisTitleRotation != null) && !leftYAxisTitleRotation.isEmpty() ) {
			try {
				rotation = Double.parseDouble(leftYAxisTitleRotation);
			}
			catch ( NumberFormatException e ) {
				// Just use 0.
			}
		}
		// Get the limits without rotation.
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_lefty_title, leftYAxisTitleString, GRUnits.DEVICE );
		leftYAxisTitleHeight = textLimits.getHeight();
		leftYAxisTitleWidth = textLimits.getWidth();
		// If the rotation is 90 or 270 swap the width and height because text is vertical.
		// TODO SAM 2016-10-17 Later support angles other than perpendicular.
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

	// Right y-axis title.

	double rightYAxisTitleWidth = 0.0;
	double rightYAxisTitleHeight = 0.0;

	String rightYAxisTitleString = tsproduct.getLayeredPropValue ( "RightYAxisTitleString", this.subproduct, -1, false );
	String rightYAxisTitlePosition = tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", this.subproduct, -1, false );
	if ( (rightYAxisTitleString != null) && !rightYAxisTitleString.equals("") &&
		(rightYAxisTitlePosition != null) && !rightYAxisTitlePosition.equalsIgnoreCase("None") ) {
		// Get the text extents and set the height based on that.
		String rightYAxisTitleFontName = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", this.subproduct, -1, false );
		String rightYAxisTitleFontSize = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", this.subproduct, -1, false );
		String rightYAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_righty_title, rightYAxisTitleFontName, rightYAxisTitleFontStyle, StringUtil.atod(rightYAxisTitleFontSize) );
		double rotation = 0.0;
		String rightYAxisTitleRotation = tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", this.subproduct, -1, false );
		if ( (rightYAxisTitleRotation != null) && !rightYAxisTitleRotation.isEmpty() ) {
			try {
				rotation = Double.parseDouble(rightYAxisTitleRotation);
			}
			catch ( NumberFormatException e ) {
				// Just use 0.
			}
		}
		// Get the limits without rotation.
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_righty_title, rightYAxisTitleString, GRUnits.DEVICE );
		rightYAxisTitleHeight = textLimits.getHeight();
		rightYAxisTitleWidth = textLimits.getWidth();
		// If the rotation is 90 or 270 swap the width and height because text is vertical.
		// TODO SAM 2016-10-17 Later support angles other than perpendicular.
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
			// Use the largest vertical height of the left and right top y-axis to allow calculations below.
			if ( rightYAxisTitleHeight > yAxisTitleTopHeight ) {
				yAxisTitleTopHeight = rightYAxisTitleHeight;
			}
		}
	}

	// X axis titles.

	double topXAxisTitleHeight = 0.0;
	String topXAxisTitleString = tsproduct.getLayeredPropValue ( "TopXAxisTitleString", this.subproduct, -1, false );
	if ( (topXAxisTitleString != null) && !topXAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that.
		String topXAxisTitleFontName = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontName", this.subproduct, -1, false );
		String topXAxisTitleFontSize = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontSize", this.subproduct, -1, false );
		String topXAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "TopXAxisTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_topx_title, topXAxisTitleFontName, topXAxisTitleFontStyle, StringUtil.atod(topXAxisTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_topx_title, topXAxisTitleString, GRUnits.DEVICE );
		topXAxisTitleHeight = textLimits.getHeight();
	}

	double bottomXAxisTitleHeight = 0.0;
	String bottomXAxisTitleString = tsproduct.getLayeredPropValue ( "BottomXAxisTitleString", this.subproduct, -1, false );
	if ( (bottomXAxisTitleString != null) && !bottomXAxisTitleString.equals("") ){
		// Get the text extents and set the height based on that.
		String bottomXAxisTitleFontName = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", this.subproduct, -1, false );
		String bottomXAxisTitleFontSize = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontSize", this.subproduct, -1, false );
		String bottomXAxisTitleFontStyle = tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_bottomx_title, bottomXAxisTitleFontName, bottomXAxisTitleFontStyle,
			StringUtil.atod(bottomXAxisTitleFontSize) );
		GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_title, bottomXAxisTitleString, GRUnits.DEVICE );
		bottomXAxisTitleHeight = textLimits.getHeight();
	}

	double bottomXAxisLabelHeight = 0.0;
	String bottomXAxisLabelFontName = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", this.subproduct, -1, false );
	String bottomXAxisLabelFontSize = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", this.subproduct, -1, false );
	String bottomXAxisLabelFontStyle = tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", this.subproduct, -1, false );
	GRDrawingAreaUtil.setFont ( this._da_bottomx_label, bottomXAxisLabelFontName, bottomXAxisLabelFontStyle, StringUtil.atod(bottomXAxisLabelFontSize) );
	GRLimits textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_bottomx_label, "A string", GRUnits.DEVICE );
	if ( (this.__leftYAxisGraphType == TSGraphType.DURATION) || (this.__leftYAxisGraphType == TSGraphType.XY_SCATTER) ) {
		bottomXAxisLabelHeight = textLimits.getHeight();
	}
	else {
	    // For X labels - leave room for two rows of labels for dates.
		bottomXAxisLabelHeight = 2*textLimits.getHeight();
	}

	// Make an initial determination of the left y-axis legend height and width, based on the font height and string width.
	// The dynamic nature of the plot size really should only impact how many legend items are shown.
	// Try to limit to a reasonable number.  This logic works for any legend position.

	String leftYAxisLegendPosition = tsproduct.getLayeredPropValue ( "LeftYAxisLegendPosition", this.subproduct, -1, false );
	if ( leftYAxisLegendPosition == null ) {
		// Check legacy value.
		leftYAxisLegendPosition = tsproduct.getLayeredPropValue ( "LegendPosition", this.subproduct, -1, false );
	}

	double leftYAxisLegendHeight = 0.0;
	double leftYAxisLegendWidth = 0.0;
	// Do not include null time series for setting limits.
	boolean includeNulls = false;
	// The following gets the left y-axis time series, assuming there will be left and right y-axis legends.
	if ((tslistLeftYAxis == null) || (tslistLeftYAxis.size() == 0) || (getTSListForAxes(true,false,includeNulls).size() == 0) || leftYAxisLegendPosition.equalsIgnoreCase("None")) {
		// Default to no legend.
		leftYAxisLegendHeight = 0.0;
		leftYAxisLegendWidth = 0.0;
	}
	else {
		// The legend height is based on the legend font size = size*(nts + 1), with the buffer,
		// where nts is the number of enabled, non-null time series. The legend properties are for the subproduct.
		String legendFontName = tsproduct.getLayeredPropValue ( "LegendFontName", this.subproduct, -1, false );
		String legendFontSize = tsproduct.getLayeredPropValue ( "LegendFontSize", this.subproduct, -1, false );
		String legendFontStyle = tsproduct.getLayeredPropValue ( "LegendFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_lefty_bottom_legend, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
		int nts = 0;
		int size = tslistLeftYAxis.size();
		TS ts = null;
		String legend = null;

        for ( int i = 0; i < size; i++ ) {
        	ts = tslistLeftYAxis.get(i);
        	if ( (ts == null) || !isTSEnabled(i)) {
        		continue;
        	}
        	if ( ts.getEnabled() ) {
        		if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        			if (i == 0) {
        				// Ignore the zeroth.
        				continue;
        			}
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i) + " (Residual)";
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( this._da_lefty_bottom_legend, legend, GRUnits.DEVICE);
        				leftYAxisLegendWidth = MathUtil.max( leftYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        		}
        		else if (this.__leftYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents(this._da_lefty_bottom_legend, legend, GRUnits.DEVICE);
        				leftYAxisLegendWidth = MathUtil.max(leftYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        			if (i > 0) {
        				// add the predicted time series, too.
        				legend = getLegendString(ts, i) + " (Predicted)";
        				if (legend != null) {
        					textLimits = GRDrawingAreaUtil.getTextExtents( this._da_lefty_bottom_legend, legend, GRUnits.DEVICE);
        					leftYAxisLegendWidth = MathUtil.max( leftYAxisLegendWidth, textLimits.getWidth());
        				}
        				++nts;
        			}
        		}
        		else {
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( this._da_lefty_bottom_legend, legend, GRUnits.DEVICE);
        				leftYAxisLegendWidth = MathUtil.max( leftYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        		}
        	}
        }

		ts = null;
		// Estimate the overall height.
		textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_lefty_bottom_legend, "TEST STRING", GRUnits.DEVICE );
		leftYAxisLegendHeight = nts*textLimits.getHeight();
		// The legend width is increased by the width of the symbol (currently always 25 pixels).
		leftYAxisLegendWidth += 25;
		textLimits = null;
	}

	// The following handles normal graph legend and raster graph legend.
	// One or the other can be drawn, but not both because they would be on top of each other.

	// Make an initial determination of the right y-axis legend height and width,
	// based on the font height and string width.
	// The dynamic nature of the plot size really should only impact how many legend items are shown.
	// Try to limit to a reasonable number.  This logic works for any legend position.
	// TODO SAM 2017-02-07 put the following and above into reused code once it tests out.

	String rightYAxisLegendPosition = tsproduct.getLayeredPropValue ( "RightYAxisLegendPosition", this.subproduct, -1, false );
	double rightYAxisLegendHeight = 0.0;
	double rightYAxisLegendWidth = 0.0;
	// The following gets the right y-axis time series, assuming there will be left and right y-axis legends.
	if ((tslistRightYAxis == null) || (tslistRightYAxis.size() == 0) || rightYAxisLegendPosition.equalsIgnoreCase("None")) {
		// Default to no legend.
		rightYAxisLegendHeight = 0.0;
		rightYAxisLegendWidth = 0.0;
	}
	else {
		// The legend height is based on the legend font size = size*(nts + 1), with the buffer,
		// where nts is the number of enabled, non-null time series. The legend properties are for the subproduct.
		String legendFontName = tsproduct.getLayeredPropValue ( "LegendFontName", this.subproduct, -1, false );
		String legendFontSize = tsproduct.getLayeredPropValue ( "LegendFontSize", this.subproduct, -1, false );
		String legendFontStyle = tsproduct.getLayeredPropValue ( "LegendFontStyle", this.subproduct, -1, false );
		GRDrawingAreaUtil.setFont ( this._da_righty_bottom_legend, legendFontName, legendFontStyle, StringUtil.atod(legendFontSize) );
		int nts = 0;
		int size = tslistRightYAxis.size();
		TS ts = null;
		String legend = null;

        for ( int i = 0; i < size; i++ ) {
        	ts = tslistRightYAxis.get(i);
        	// FIXME sam 2017-02-09 need to fix this because "i" should be for the full time series list, not just right.
        	if ( (ts == null) || !isTSEnabled(i) ) {
        		continue;
        	}
        	if ( ts.getEnabled() ) {
        		if (this.__rightYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        			if (i == 0) {
        				// ignore the zeroth.
        				continue;
        			}
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i) + " (Residual)";
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( this._da_righty_bottom_legend, legend, GRUnits.DEVICE);
        				rightYAxisLegendWidth = MathUtil.max( rightYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        		}
        		else if (this.__rightYAxisGraphType == TSGraphType.PREDICTED_VALUE) {
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents(this._da_righty_bottom_legend, legend, GRUnits.DEVICE);
        				rightYAxisLegendWidth = MathUtil.max(rightYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        			if (i > 0) {
        				// Add the predicted time series, too.
        				legend = getLegendString(ts, i) + " (Predicted)";
        				if (legend != null) {
        					textLimits = GRDrawingAreaUtil.getTextExtents( this._da_righty_bottom_legend, legend, GRUnits.DEVICE);
        					rightYAxisLegendWidth = MathUtil.max( rightYAxisLegendWidth, textLimits.getWidth());
        				}
        				++nts;
        			}
        		}
        		else {
        			// The time series will be plotted and will be shown in the legend.
        			legend = getLegendString(ts, i);
        			if (legend != null) {
        				textLimits = GRDrawingAreaUtil.getTextExtents( this._da_righty_bottom_legend, legend, GRUnits.DEVICE);
        				rightYAxisLegendWidth = MathUtil.max( rightYAxisLegendWidth, textLimits.getWidth());
        			}
        			++nts;
        		}
        	}
        }

		ts = null;
		// Estimate the overall height.
		textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_righty_bottom_legend, "TEST STRING", GRUnits.DEVICE );
		rightYAxisLegendHeight = nts*textLimits.getHeight();
		// The legend width is increased by the width of the symbol (currently always 25 pixels).
		rightYAxisLegendWidth += 25;
		textLimits = null;
	}

    // Currently if a raster graph the legend position property is associated with the first time series.
	double rightRasterLegendWidth = 0.0;
  	//double rightRasterLegendHeight = 0.0;  // Not used since the full height of the graph area?
    if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
    	// Estimate the total text area, used regardless of whether the legend is on the left or right.
 	  	textLimits = GRDrawingAreaUtil.getTextExtents ( this._da_right_raster_legend, "TEST STRING", GRUnits.DEVICE );
 	  	//rightRasterLegendHeight = this.rasterSymbolTable.size()*textLimits.getHeight();
 	  	// The legend width is the maximum width of all strings.
 	  	GRSymbolTableRow row = null;
 	  	for ( int irow = 0; irow < this.rasterSymbolTable.size(); irow++ ) {
 		  	row = this.rasterSymbolTable.getSymbolTableRow(irow);
 		  	String legend2 = "  " + row.getValueMinFullString() + " " + row.getValueMaxFullString();
 		  	// Clean up the legend text:
 		  	// - trim but then add two spaces on the right to give a bit of buffer
 		  	legend2 = legend2.replace("-Infinity","").replace("Infinity","").replace("NoData NoData","NoData").trim() + "  ";
 		  	GRLimits textLimits2 = GRDrawingAreaUtil.getTextExtents ( this._da_right_raster_legend, legend2, GRUnits.DEVICE );
 		  	if ( textLimits2.getWidth() > rightRasterLegendWidth ) {
 			  	rightRasterLegendWidth = textLimits2.getWidth();
 		  	}
 	  	}
 	  	// Add some width for the filled rectangle for legend color:
 	  	// - set to the same as legendLineWidth in drawLegendRaset()
 	  	double legendLineWidth = 25;
 		rightRasterLegendWidth += legendLineWidth;
    }

	// Calculate legend limits for each potential legend position,
	// which is the maximum of the left and right y-axis legend dimensions.
	// Under normal cases, there should not be conflict
	// because the legends will be positioned in different locations.
	// However, more work may need to be done when both are in the bottom.
	//double leftLegendWidth = Math.max(leftYAxisLegendWidth, rightYAxisLegendWidth);
	//double leftLegendHeight = Math.max(leftYAxisLegendHeight, rightYAxisLegendHeight);
	//double rightLegendWidth = Math.max(leftYAxisLegendWidth, rightYAxisLegendWidth);
	//double rightLegendHeight = Math.max(leftYAxisLegendHeight, rightYAxisLegendHeight);

	// Now set the drawing limits based on the requested layout properties and baseline geometry data calculated above.
	// Drawing limits for the page are set first.

	this._drawlim_page = new GRLimits ( drawlim_page );

	this._drawlim_error = new GRLimits ( drawlim_page );

	// Do a check on the graph height and adjust some of the other heights if necessary (enhance this over time).

	// For legend position "Top" and "Bottom", limit the legend to 1/2 the page
	// (this can be an issue when traces or many time series are drawn
	// - in this case the legend position should probably be set to "Left"
	// or "Right" and labeled with the sequence number, scenario, or some other short string.
	// Use the page DA for checks because SAM's too lazy to do all the other checks here to find out what half the graph
	// height would be, with buffers.
	// Currently only one legend is supported but to simplify the logic below transfer the generic
	// "legend_height" and "legend_width" variables to specific variables.

	if ( leftYAxisLegendPosition.toUpperCase().startsWith("BOTTOM") || leftYAxisLegendPosition.equalsIgnoreCase("Top") ) {
		// Limit legend to half the full page height.
		if ( leftYAxisLegendHeight > this._drawlim_page.getHeight()*.5 ) {
			leftYAxisLegendHeight = this._drawlim_page.getHeight()*.5;
		}
	}
	if ( rightYAxisLegendPosition.toUpperCase().startsWith("BOTTOM") || rightYAxisLegendPosition.equalsIgnoreCase("Top") ) {
		// Limit legend to half the full page height.
		if ( rightYAxisLegendHeight > this._drawlim_page.getHeight()*.5 ) {
			rightYAxisLegendHeight = this._drawlim_page.getHeight()*.5;
		}
	}

	// Compute specific legend height and width values for each legend position.

	double bottom_legend_height = 0.0;
	double left_legend_width = 0.0;
	double left_legend_buffer = 0.0;
	double right_legend_width = 0.0;
	double right_legend_buffer = 0.0;
	// Used to calculate layout for left y-axis inside legend.
	double inside_lefty_legend_height = 0.0;
	double inside_lefty_legend_width = 0.0;
	double inside_lefty_legend_buffer = 0.0;
	// Used to calculate layout for right y-axis inside legend.
	double inside_righty_legend_height = 0.0;
	double inside_righty_legend_width = 0.0;
	double inside_righty_legend_buffer = 0.0;

	// Compute initial legend layout based on left y-axis legend.

	if ( leftYAxisLegendPosition.toUpperCase().startsWith("BOTTOM") ) {
		bottom_legend_height = leftYAxisLegendHeight;
	}
	else if ( leftYAxisLegendPosition.equalsIgnoreCase("Left") ) {
		left_legend_width = leftYAxisLegendWidth;
		left_legend_buffer = buffer;
	}
	else if ( leftYAxisLegendPosition.equalsIgnoreCase("Right") ) {
		right_legend_width = leftYAxisLegendWidth;
		right_legend_buffer = buffer;
	}
	else if ( leftYAxisLegendPosition.equalsIgnoreCase("Top") ) {
	}
	else if (StringUtil.startsWithIgnoreCase(leftYAxisLegendPosition, "Inside")) {
		inside_lefty_legend_height = leftYAxisLegendHeight;
		inside_lefty_legend_width = leftYAxisLegendWidth;
		inside_lefty_legend_buffer = buffer * 4;
	}

	// Now increase the initial legend layout dimensions based on right y-axis legend.
	// It is most likely that the legends will share the bottom legend
	// because if shown on left and right the space will not overlap legends.

	if ( rightYAxisLegendPosition.toUpperCase().startsWith("BOTTOM") ) {
		if ( leftYAxisLegendPosition.toUpperCase().startsWith("BOTTOM") ) {
			bottom_legend_height = Math.max(leftYAxisLegendHeight,rightYAxisLegendHeight);
		}
	}
	else if ( rightYAxisLegendPosition.equalsIgnoreCase("Left") ) {
		left_legend_width = Math.max(leftYAxisLegendWidth,rightYAxisLegendWidth);
		left_legend_buffer = buffer;
	}
	else if ( rightYAxisLegendPosition.equalsIgnoreCase("Right") ) {
		right_legend_width = Math.max(leftYAxisLegendWidth,rightYAxisLegendWidth);
		right_legend_buffer = buffer;
	}
	else if ( rightYAxisLegendPosition.equalsIgnoreCase("Top") ) {
	}
	else if (StringUtil.startsWithIgnoreCase(rightYAxisLegendPosition, "Inside")) {
		inside_righty_legend_height = rightYAxisLegendHeight;
		inside_righty_legend_width = rightYAxisLegendWidth;
		inside_righty_legend_buffer = buffer * 4;
	}

   	String rasterGraphLegendPosition = "None";
    if ( this.__leftYAxisGraphType == TSGraphType.RASTER ) {
    	// Adjust the legend dimensions based on the raster legend.
    	rasterGraphLegendPosition = tsproduct.getLayeredPropValue ( "RasterGraphLegendPosition", this.subproduct, 0, false );
    	if ( (rasterGraphLegendPosition == null) || (rasterGraphLegendPosition.isEmpty())) {
		   	// Default to "Right".
		   	// TODO smalers 2021-08-28 need to make sure the property is set early on when graphing from TSTool UI.
		   	rasterGraphLegendPosition = "Right";
	   	}
    	if ( rasterGraphLegendPosition.equalsIgnoreCase("Right") ) {
	  	  	// Raster graph legend is on the right so override the size from above.
    		right_legend_width = Math.max(right_legend_width, rightRasterLegendWidth);
    	}
  	}

	// Set the drawing limits based on what was determined above.

	// Graph main title is only impacted by overall (page) limits.

	if ( mainTitleHeight == 0.0 ) {
		// Zero height drawing area place holder.
		this._drawlim_maintitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),	drawlim_page.getTopY(),
			(drawlim_page.getRightX() - buffer), drawlim_page.getTopY() );
	}
	else {
	    this._drawlim_maintitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),
			(drawlim_page.getTopY() - buffer - mainTitleHeight),
			(drawlim_page.getRightX() - buffer), (drawlim_page.getTopY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Main title drawing limits are: " + this._drawlim_maintitle );
	}

	// Graph subtitle is only impacted by page limits and main title.

	if ( subTitleHeight == 0.0 ) {
		// Zero height drawing area place holder.
		this._drawlim_subtitle = new GRLimits ( (drawlim_page.getLeftX() + buffer), this._drawlim_maintitle.getBottomY(),
			(drawlim_page.getRightX() - buffer), this._drawlim_maintitle.getBottomY() );
	}
	else {
	    this._drawlim_subtitle = new GRLimits ( (drawlim_page.getLeftX() + buffer),
			(this._drawlim_maintitle.getBottomY() - buffer -	subTitleHeight),
			(drawlim_page.getRightX() - buffer), (this._drawlim_maintitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Sub title drawing limits are: " + this._drawlim_subtitle.toString() );
	}

	// Top legend is impacted by page limits and position of subtitle.

	// Currently top legend is not enabled.

	// Top X axis title is impacted by the left and right legends and the position of the subtitle.

	// y-axis label left edge is impacted by legend position
	// (for now include left y-axis title inside the label area if the title is on the left):
	// - currently the left y-axis title is within the label drawing area
	double leftYAxisLabelLeft = this._drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer;

	if ( topXAxisTitleHeight == 0.0 ) {
		// Zero height drawing area place holder at same height as the bottom of the subtitle.
		this._drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer),
			this._drawlim_subtitle.getBottomY(),
			(drawlim_page.getRightX() - buffer - right_legend_width - right_legend_buffer - rightYAxisLabelWidth - buffer),
			this._drawlim_subtitle.getBottomY() );
	}
	else {
	    this._drawlim_topx_title = new GRLimits (
			(drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer),
			this._drawlim_subtitle.getBottomY() - buffer - topXAxisTitleHeight,
			(drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth - buffer),
			this._drawlim_subtitle.getBottomY() - buffer );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Top X title drawing limits are: " + this._drawlim_topx_title.toString() );
	}

	// Top x labels are impacted by left and right legends and the position of the top x title.
	// Top x labels are not currently processed (until can move the Y axis titles out of the way).

	// Left y labels are always present.  Even if zero width, use buffer because other code does below.

	// Calculate the position of the bottom of the y-axis labels (essentially the bottom of the graph).
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

	// Calculate the position of the top of the y-axis labels (essentially the top of the graph).
	// This is used by a number of calculations below.
	// y-axis label top is consistent for left and right y-axis and is impacted by y-axis title position and top titles.
	double yAxisLabelTop = drawlim_page.getTopY() - buffer - mainTitleHeight - buffer - subTitleHeight;
	// TODO SAM 2016-10-24 No top x-axis labels are supported yet but will need to adjust if enabled.
	if ( yAxisTitleTopHeight > 0.0 ) {
		// Left and/or right y-axis label is above the axis so add some vertical space.
		yAxisLabelTop = yAxisLabelTop - buffer - yAxisTitleTopHeight - buffer;
	}

	// Left y-axis title.

	if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
		// Draw the left y-axis title in the same space as the labels so as to avoid extra calculations.
		// Can adjust this later if problematic.
		double leftYAxisTitleLeft = leftYAxisLabelLeft;
	    this._drawlim_lefty_title = new GRLimits ( leftYAxisTitleLeft, yAxisLabelBottom,
			(leftYAxisTitleLeft + leftYAxisTitleWidth),	yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis" to match legacy behavior.
		double leftEdge = (drawlim_page.getLeftX() + left_legend_buffer + left_legend_width + buffer + leftYAxisLabelWidth + buffer -
		        leftYAxisTitleWidth/2.0);
		// Center above left edge of graph.
		this._drawlim_lefty_title = new GRLimits (
			leftEdge, (yAxisLabelTop + buffer),
			(leftEdge + leftYAxisTitleWidth), (yAxisLabelTop + buffer + leftYAxisTitleHeight) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Left y title drawing limits are: " + this._drawlim_lefty_title.toString() );
	}

	// Left y labels are impacted by left and legends and position of the y axis titles:
	// - currently left y-axis title if LeftOfAxis is within the label drawing area so no size impact
	if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
		// No left y-axis title vertically so compute from top down
		this._drawlim_lefty_label = new GRLimits ( leftYAxisLabelLeft, yAxisLabelBottom,
			(leftYAxisLabelLeft + leftYAxisLabelWidth), yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis":
		// - currently same as above, may adjust logic later
		this._drawlim_lefty_label = new GRLimits ( leftYAxisLabelLeft, yAxisLabelBottom,
				(leftYAxisLabelLeft + leftYAxisLabelWidth), yAxisLabelTop );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Left Y label drawing limits are: " + this._drawlim_lefty_label.toString() );
	}

	// Right y axis labels (do before titles since for now the right y-axis title can sit inside the label drawing area).

	double rightYLabelLeftEdge = this._drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth;
	this._drawlim_righty_label = new GRLimits (
		rightYLabelLeftEdge, yAxisLabelBottom,
		(rightYLabelLeftEdge + rightYAxisLabelWidth), yAxisLabelTop );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Right y-axis label drawing limits are: " + this._drawlim_righty_label.toString() );
	}

	// Right y-axis title (do after labels since for now title sits inside of label drawing area).

	if ( rightYAxisTitlePosition.equalsIgnoreCase("RightOfAxis") ) {
		// Draw the right y-axis title on the right inside edge of the labels so as to avoid extra calculations.
		// Can adjust this later if problematic.
		double rightEdge = this._drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer;
	    this._drawlim_righty_title = new GRLimits ( (rightEdge - rightYAxisTitleWidth), yAxisLabelBottom,
			rightEdge, yAxisLabelTop );
	}
	else {
		// Default is "AboveAxis".
		double leftEdge = (drawlim_page.getRightX() - right_legend_buffer - right_legend_width - buffer - rightYAxisLabelWidth - buffer -
		        rightYAxisTitleWidth/2.0);
		// Center above right edge of graph.
		this._drawlim_righty_title = new GRLimits (
			leftEdge, (yAxisLabelTop + buffer),
			(leftEdge + rightYAxisTitleWidth), (yAxisLabelTop + buffer + rightYAxisTitleHeight) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Right y-axis title drawing limits are: " + this._drawlim_righty_title.toString() );
	}

	// Skip the graph area for now because it will be the remainder:
	// - see below for its definition

	// Now work up from the bottom.
	// Drawing limits for the bottom legend (always independent of other legends).

	// Bottom left y-axis legend.
	if ( bottom_legend_height == 0.0 ) {
		// Make zero-height same Y as the bottom of the page area.
		this._drawlim_lefty_bottom_legend = new GRLimits ( (this._drawlim_page.getLeftX() + buffer), this._drawlim_page.getBottomY(),
			(this._drawlim_page.getRightX() - buffer), this._drawlim_page.getBottomY());
	}
	else {
	    this._drawlim_lefty_bottom_legend = new GRLimits ( (this._drawlim_page.getLeftX() + buffer),
	        (this._drawlim_page.getBottomY() + buffer), (this._drawlim_page.getRightX() - buffer),
			(this._drawlim_page.getBottomY() + buffer + bottom_legend_height) );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_lefty_bottom_legend = new GRLimits ( 0.0, 0.0, this._drawlim_lefty_bottom_legend.getWidth(), this._drawlim_lefty_bottom_legend.getHeight() );

	// Bottom right y-axis legend.
	if ( bottom_legend_height == 0.0 ) {
		// Make zero-height same Y as the bottom of the page area.
		this._drawlim_righty_bottom_legend = new GRLimits ( (this._drawlim_page.getLeftX() + buffer), this._drawlim_page.getBottomY(),
			(this._drawlim_page.getRightX() - buffer), this._drawlim_page.getBottomY());
	}
	else {
	    this._drawlim_righty_bottom_legend = new GRLimits ( (this._drawlim_page.getLeftX() + buffer),
	        (this._drawlim_page.getBottomY() + buffer), (this._drawlim_page.getRightX() - buffer),
			(this._drawlim_page.getBottomY() + buffer + bottom_legend_height) );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_righty_bottom_legend = new GRLimits ( 0.0, 0.0, this._drawlim_righty_bottom_legend.getWidth(), this._drawlim_righty_bottom_legend.getHeight() );

	// The position of the bottom X axis title and labels is impacted by
	// left and right legends and the position of the bottom legend.
	// Bottom X axis title and labels - work up from the bottom legend.
	// For the bottomx title, add a little space around so it looks better, even if no title is given..

	if ( bottomXAxisTitleHeight == 0.0 ) {
		// Make zero-height same Y as the top of the legend area.
		this._drawlim_bottomx_title = new GRLimits (
			(this._drawlim_lefty_label.getRightX() + buffer), (this._drawlim_lefty_bottom_legend.getTopY() + buffer),
			(this._drawlim_righty_label.getLeftX() - buffer), (this._drawlim_lefty_bottom_legend.getTopY() + buffer + buffer) );
	}
	else {
	    this._drawlim_bottomx_title = new GRLimits ( (this._drawlim_lefty_label.getRightX() + buffer),
	        (this._drawlim_lefty_bottom_legend.getTopY() + buffer), (this._drawlim_righty_label.getLeftX() - buffer),
			(this._drawlim_lefty_bottom_legend.getTopY() + buffer + bottomXAxisTitleHeight + buffer));
	}

	if ( bottomXAxisLabelHeight == 0.0 ) {
		// Make zero-height same Y as the top of the X title.
		this._drawlim_bottomx_label = new GRLimits ( (this._drawlim_lefty_label.getRightX() + buffer),
			this._drawlim_bottomx_title.getTopY(), (this._drawlim_righty_label.getLeftX() - buffer),
			this._drawlim_bottomx_title.getTopY());
	}
	else {
	    this._drawlim_bottomx_label = new GRLimits ( (this._drawlim_lefty_label.getRightX() + buffer),
			(this._drawlim_bottomx_title.getTopY() + buffer), (this._drawlim_righty_label.getLeftX() - buffer),
			(this._drawlim_bottomx_title.getTopY() + buffer + bottomXAxisLabelHeight));
	}

	// Graph drawing area (always what is left).

	if ( this._is_reference_graph ) {
		this._drawlim_lefty_graph = new GRLimits ( drawlim_page.getLeftX(), drawlim_page.getBottomY(),
			drawlim_page.getRightX(), drawlim_page.getTopY() );
	}
	else {
		// Left y-axis graph.
		if ( leftYAxisTitlePosition.equalsIgnoreCase("LeftOfAxis") ) {
			this._drawlim_lefty_graph = new GRLimits ( (this._drawlim_lefty_label.getRightX() + buffer),
				(this._drawlim_bottomx_label.getTopY() + buffer), (this._drawlim_righty_label.getLeftX() - buffer),
				this._drawlim_lefty_title.getTopY() );
		}
		else {
			// "AboveAxis" (legacy).
			this._drawlim_lefty_graph = new GRLimits ( (this._drawlim_lefty_label.getRightX() + buffer),
				(this._drawlim_bottomx_label.getTopY() + buffer), (this._drawlim_righty_label.getLeftX() - buffer),
				(this._drawlim_lefty_title.getBottomY() - buffer) );
		}
	}
	// Right y-axis graph, same drawing limits as left y-axis graph (but data limits will be different).
	this._drawlim_righty_graph = new GRLimits ( this._drawlim_lefty_graph );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, this._gtype + "Graph drawing limits are: " + this._drawlim_lefty_graph );
	}

	// If the legend is on the left or right, define the drawing area now because it is impacted by the graph drawing area.
	// Typically the left and right legends will draw down the side until the bottom of the graph.
	// This could be a problem if the graph ends.
	// Fix by not drawing when negative y-coordinates are found for data.

	// Left y-axis left legend.
	if ( leftYAxisLegendPosition.equalsIgnoreCase("Left") ) {
		this._drawlim_lefty_left_legend = new GRLimits ( (this._drawlim_page.getLeftX() + left_legend_buffer),
		    this._drawlim_lefty_graph.getBottomY(), (this._drawlim_page.getLeftX() + left_legend_buffer + left_legend_width),
			this._drawlim_lefty_graph.getTopY() );
	}
	else {
		// Set to zero width.
		this._drawlim_lefty_left_legend = new GRLimits ( this._drawlim_page.getLeftX(), this._drawlim_lefty_graph.getBottomY(),
			this._drawlim_page.getLeftX(), this._drawlim_lefty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_lefty_left_legend = new GRLimits ( 0.0, 0.0, this._drawlim_lefty_left_legend.getWidth(),
		this._drawlim_lefty_left_legend.getHeight() );

	// Left y-axis right legend.

	if ( leftYAxisLegendPosition.equalsIgnoreCase("Right") ) {
		this._drawlim_lefty_right_legend = new GRLimits ( (this._drawlim_page.getRightX() - right_legend_buffer - right_legend_width),
			this._drawlim_lefty_graph.getBottomY(), (this._drawlim_page.getRightX() - right_legend_buffer), this._drawlim_lefty_graph.getTopY() );
	}
	else {
		// Set to zero width.
		this._drawlim_lefty_right_legend = new GRLimits ( this._drawlim_page.getRightX(), this._drawlim_lefty_graph.getBottomY(),
			this._drawlim_page.getRightX(), this._drawlim_lefty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_lefty_right_legend = new GRLimits ( 0.0, 0.0, this._drawlim_lefty_right_legend.getWidth(),
		this._drawlim_lefty_right_legend.getHeight() );

	// Right y-axis left legend.

	if ( rightYAxisLegendPosition.equalsIgnoreCase("Left") ) {
		this._drawlim_righty_left_legend = new GRLimits ( (this._drawlim_page.getLeftX() + left_legend_buffer),
		    this._drawlim_righty_graph.getBottomY(), (this._drawlim_page.getLeftX() + left_legend_buffer + left_legend_width),
			this._drawlim_righty_graph.getTopY() );
	}
	else {
		// Set to zero width.
		this._drawlim_righty_left_legend = new GRLimits ( this._drawlim_page.getLeftX(), this._drawlim_righty_graph.getBottomY(),
			this._drawlim_page.getLeftX(), this._drawlim_righty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_righty_left_legend = new GRLimits ( 0.0, 0.0, this._drawlim_righty_left_legend.getWidth(),
		this._drawlim_righty_left_legend.getHeight() );

	// Right y-axis right legend.

	if ( rightYAxisLegendPosition.equalsIgnoreCase("Right") ) {
		this._drawlim_righty_right_legend = new GRLimits ( (this._drawlim_page.getRightX() - right_legend_buffer - right_legend_width),
			this._drawlim_righty_graph.getBottomY(), (this._drawlim_page.getRightX() - right_legend_buffer), this._drawlim_righty_graph.getTopY() );
	}
	else {
		// Set to zero width.
		this._drawlim_righty_right_legend = new GRLimits ( this._drawlim_page.getRightX(), this._drawlim_righty_graph.getBottomY(),
			this._drawlim_page.getRightX(), this._drawlim_righty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_righty_right_legend = new GRLimits ( 0.0, 0.0, this._drawlim_righty_right_legend.getWidth(),
		this._drawlim_righty_right_legend.getHeight() );

	// Right raster legend:
	// - same as left y-axis right legend but use raster width as computed above
	if ( rasterGraphLegendPosition.equalsIgnoreCase("Right") ) {
		this._drawlim_right_raster_legend = new GRLimits ( (this._drawlim_page.getRightX() - right_legend_buffer - right_legend_width),
			this._drawlim_lefty_graph.getBottomY(), (this._drawlim_page.getRightX() - right_legend_buffer), this._drawlim_lefty_graph.getTopY() );
	}
	else {
		// Set to zero width.
		this._drawlim_right_raster_legend = new GRLimits ( this._drawlim_page.getRightX(), this._drawlim_lefty_graph.getBottomY(),
			this._drawlim_page.getRightX(), this._drawlim_lefty_graph.getTopY() );
	}
	// Set the data limits for the legend to use device units.
	this._datalim_right_raster_legend = new GRLimits ( 0.0, 0.0, this._drawlim_right_raster_legend.getWidth(),
		this._drawlim_right_raster_legend.getHeight() );

	// Left y-axis inner legend positions

	if (leftYAxisLegendPosition.equalsIgnoreCase("InsideUpperLeft")) {
		this._drawlim_lefty_inside_legend = new GRLimits( this._drawlim_lefty_graph.getLeftX() + inside_lefty_legend_buffer,
			this._drawlim_lefty_graph.getTopY() - inside_lefty_legend_buffer	- inside_lefty_legend_height,
			this._drawlim_lefty_graph.getLeftX() + inside_lefty_legend_buffer + inside_lefty_legend_width,
			this._drawlim_lefty_graph.getTopY() - inside_lefty_legend_buffer);
	}
	else if (leftYAxisLegendPosition.equalsIgnoreCase("InsideUpperRight")) {
		this._drawlim_lefty_inside_legend = new GRLimits( this._drawlim_lefty_graph.getRightX() - inside_lefty_legend_buffer - inside_lefty_legend_width,
			this._drawlim_lefty_graph.getTopY() - inside_lefty_legend_buffer - inside_lefty_legend_height,
			this._drawlim_lefty_graph.getRightX() - inside_lefty_legend_buffer, this._drawlim_lefty_graph.getTopY() - inside_lefty_legend_buffer);
	}
	else if (leftYAxisLegendPosition.equalsIgnoreCase("InsideLowerLeft")) {
		this._drawlim_lefty_inside_legend = new GRLimits( this._drawlim_lefty_graph.getLeftX() + inside_lefty_legend_buffer,
			this._drawlim_lefty_graph.getBottomY() + inside_lefty_legend_buffer,
			this._drawlim_lefty_graph.getLeftX() + inside_lefty_legend_buffer + inside_lefty_legend_width,
			this._drawlim_lefty_graph.getBottomY() + inside_lefty_legend_buffer + inside_lefty_legend_height);
	}
	else if (leftYAxisLegendPosition.equalsIgnoreCase("InsideLowerRight")) {
		this._drawlim_lefty_inside_legend = new GRLimits( this._drawlim_lefty_graph.getRightX() - inside_lefty_legend_buffer - inside_lefty_legend_width,
			this._drawlim_lefty_graph.getBottomY() + inside_lefty_legend_buffer,
			this._drawlim_lefty_graph.getRightX() - inside_lefty_legend_buffer,
			this._drawlim_lefty_graph.getBottomY() + inside_lefty_legend_buffer + inside_lefty_legend_height);
	}
	else {
		this._drawlim_lefty_inside_legend = new GRLimits( this._drawlim_page.getLeftX(), this._drawlim_page.getBottomY(),
			this._drawlim_page.getRightX(), this._drawlim_page.getTopY());
	}
	this._datalim_lefty_inside_legend = new GRLimits(0.0, 0.0, this._drawlim_lefty_inside_legend.getWidth(),
		this._drawlim_lefty_inside_legend.getHeight());

	// Right y-axis inner legend positions - same as left y-axis other than the drawing limits name.

	if (rightYAxisLegendPosition.equalsIgnoreCase("InsideUpperLeft")) {
		this._drawlim_righty_inside_legend = new GRLimits( this._drawlim_righty_graph.getLeftX() + inside_righty_legend_buffer,
			this._drawlim_righty_graph.getTopY() - inside_righty_legend_buffer - inside_righty_legend_height,
			this._drawlim_righty_graph.getLeftX() + inside_righty_legend_buffer + inside_righty_legend_width,
			this._drawlim_righty_graph.getTopY() - inside_righty_legend_buffer);
	}
	else if (rightYAxisLegendPosition.equalsIgnoreCase("InsideUpperRight")) {
		this._drawlim_righty_inside_legend = new GRLimits( this._drawlim_righty_graph.getRightX() - inside_righty_legend_buffer - inside_righty_legend_width,
			this._drawlim_righty_graph.getTopY() - inside_righty_legend_buffer - inside_righty_legend_height,
			this._drawlim_righty_graph.getRightX() - inside_righty_legend_buffer, this._drawlim_righty_graph.getTopY() - inside_righty_legend_buffer);
	}
	else if (rightYAxisLegendPosition.equalsIgnoreCase("InsideLowerLeft")) {
		this._drawlim_righty_inside_legend = new GRLimits( this._drawlim_righty_graph.getLeftX() + inside_righty_legend_buffer,
			this._drawlim_righty_graph.getBottomY() + inside_righty_legend_buffer,
			this._drawlim_righty_graph.getLeftX() + inside_righty_legend_buffer + inside_righty_legend_width,
			this._drawlim_righty_graph.getBottomY() + inside_righty_legend_buffer + inside_righty_legend_height);
	}
	else if (rightYAxisLegendPosition.equalsIgnoreCase("InsideLowerRight")) {
		this._drawlim_righty_inside_legend = new GRLimits( this._drawlim_righty_graph.getRightX() - inside_righty_legend_buffer - inside_righty_legend_width,
			this._drawlim_righty_graph.getBottomY() + inside_righty_legend_buffer,
			this._drawlim_righty_graph.getRightX() - inside_righty_legend_buffer,
			this._drawlim_righty_graph.getBottomY() + inside_righty_legend_buffer + inside_righty_legend_height);
	}
	else {
		this._drawlim_righty_inside_legend = new GRLimits( this._drawlim_page.getLeftX(), this._drawlim_page.getBottomY(),
			this._drawlim_page.getRightX(), this._drawlim_page.getTopY());
	}
	this._datalim_righty_inside_legend = new GRLimits(0.0, 0.0, this._drawlim_righty_inside_legend.getWidth(),
		this._drawlim_righty_inside_legend.getHeight());

	// Now set in the drawing areas.

	if ( (this._da_page != null) && (this._drawlim_page != null) ) {
		// this._drawlim_page is set in the constructor - just need to use it as is.
		this._da_page.setDrawingLimits ( this._drawlim_page, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (this._da_error != null) && (this._drawlim_error != null) ) {
		this._da_error.setDrawingLimits ( this._drawlim_error, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (this._da_maintitle != null) && (this._drawlim_maintitle != null) ) {
		this._da_maintitle.setDrawingLimits ( this._drawlim_maintitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (this._da_subtitle != null) && (this._drawlim_subtitle != null) ) {
		this._da_subtitle.setDrawingLimits ( this._drawlim_subtitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (this._da_topx_title != null) && (this._drawlim_topx_title != null) ) {
		this._da_topx_title.setDrawingLimits ( this._drawlim_topx_title, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_topx_title.setDataLimits ( this._datalim_topx_title );
	}
	if ( (this._da_topx_label != null) && (this._drawlim_topx_label != null) ) {
		this._da_topx_label.setDrawingLimits ( this._drawlim_topx_label, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_topx_label.setDataLimits ( this._datalim_topx_label );
		if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_topx_label, GRAxisScaleType.LOG, GRAxisScaleType.LINEAR);
		}
	}
	if ( (this._da_lefty_title != null) && (this._drawlim_lefty_title != null) ) {
		this._da_lefty_title.setDrawingLimits ( this._drawlim_lefty_title, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_lefty_title.setDataLimits ( this._datalim_lefty_title );
	}
	if ( (this._da_lefty_label != null) && (this._drawlim_lefty_label != null) ) {
		this._da_lefty_label.setDrawingLimits ( this._drawlim_lefty_label, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_lefty_label.setDataLimits ( this._datalim_lefty_label );
		if (log_y_lefty || log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_lefty_label, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
		}
	}
	if ( (this._da_righty_title != null) && (this._drawlim_righty_title != null) ) {
		this._da_righty_title.setDrawingLimits ( this._drawlim_righty_title, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_righty_title.setDataLimits ( this._datalim_righty_title );
	}
	if ( (this._da_righty_label != null) && (this._drawlim_righty_label != null) ) {
		this._da_righty_label.setDrawingLimits (	this._drawlim_righty_label, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_righty_label.setDataLimits ( this._datalim_righty_label );
		if (log_y_lefty || log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_righty_label, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
		}
	}
	if ( (this._da_lefty_graph != null) && (this._drawlim_lefty_graph != null) ) {
		this._da_lefty_graph.setDrawingLimits ( this._drawlim_lefty_graph, GRUnits.DEVICE, GRLimits.DEVICE );
		if (log_y_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_lefty_graph, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
		}
		else if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_lefty_graph, GRAxisScaleType.LOG, GRAxisScaleType.LOG);
		}
	}
	if ( (this._da_righty_graph != null) && (this._drawlim_righty_graph != null) ) {
		this._da_righty_graph.setDrawingLimits ( this._drawlim_righty_graph, GRUnits.DEVICE, GRLimits.DEVICE );
		if (log_y_righty) {
			GRDrawingAreaUtil.setAxes(this._da_righty_graph, GRAxisScaleType.LINEAR, GRAxisScaleType.LOG);
		}
	}
	if ( (this._da_bottomx_label != null) && (this._drawlim_bottomx_label != null) ) {
		this._da_bottomx_label.setDrawingLimits ( this._drawlim_bottomx_label, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_bottomx_label.setDataLimits ( this._datalim_bottomx_label );
		if (log_xy_scatter_lefty) {
			GRDrawingAreaUtil.setAxes(this._da_bottomx_label, GRAxisScaleType.LOG, GRAxisScaleType.LINEAR);
		}
	}
	if ( (this._da_bottomx_title != null) && (this._drawlim_bottomx_title != null) ) {
		this._da_bottomx_title.setDrawingLimits ( this._drawlim_bottomx_title, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_bottomx_title.setDataLimits ( this._datalim_bottomx_title );
	}
	// Left y-axis legend options.
	if ( (this._da_lefty_bottom_legend != null) && (this._drawlim_lefty_bottom_legend != null) ) {
		this._da_lefty_bottom_legend.setDrawingLimits ( this._drawlim_lefty_bottom_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_lefty_bottom_legend.setDataLimits ( this._datalim_lefty_bottom_legend );
	}
	if ( (this._da_lefty_left_legend != null) && (this._drawlim_lefty_left_legend != null) ) {
		this._da_lefty_left_legend.setDrawingLimits ( this._drawlim_lefty_left_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_lefty_left_legend.setDataLimits ( this._datalim_lefty_left_legend );
	}
	if ( (this._da_lefty_right_legend != null) && (this._drawlim_lefty_right_legend != null) ) {
		this._da_lefty_right_legend.setDrawingLimits ( this._drawlim_lefty_right_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_lefty_right_legend.setDataLimits ( this._datalim_lefty_right_legend );
	}
	if ((this._da_lefty_inside_legend != null) && (this._drawlim_lefty_inside_legend != null)) {
		this._da_lefty_inside_legend.setDrawingLimits(this._drawlim_lefty_inside_legend, GRUnits.DEVICE, GRLimits.DEVICE);
		this._da_lefty_inside_legend.setDataLimits(this._datalim_lefty_inside_legend);
	}
	// Right y-axis legend options.
	if ( (this._da_righty_bottom_legend != null) && (this._drawlim_righty_bottom_legend != null) ) {
		this._da_righty_bottom_legend.setDrawingLimits ( this._drawlim_righty_bottom_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_righty_bottom_legend.setDataLimits ( this._datalim_righty_bottom_legend );
	}
	if ( (this._da_righty_left_legend != null) && (this._drawlim_righty_left_legend != null) ) {
		this._da_righty_left_legend.setDrawingLimits ( this._drawlim_righty_left_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_righty_left_legend.setDataLimits ( this._datalim_righty_left_legend );
	}
	if ( (this._da_righty_right_legend != null) && (this._drawlim_righty_right_legend != null) ) {
		this._da_righty_right_legend.setDrawingLimits ( this._drawlim_righty_right_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_righty_right_legend.setDataLimits ( this._datalim_righty_right_legend );
	}
	// For raster graph.
	if ( (this._da_right_raster_legend != null) && (this._drawlim_right_raster_legend != null) ) {
		this._da_right_raster_legend.setDrawingLimits ( this._drawlim_right_raster_legend, GRUnits.DEVICE, GRLimits.DEVICE );
		this._da_right_raster_legend.setDataLimits ( this._datalim_right_raster_legend );
	}
	if ((this._da_righty_inside_legend != null) && (this._drawlim_righty_inside_legend != null)) {
		this._da_righty_inside_legend.setDrawingLimits(this._drawlim_righty_inside_legend, GRUnits.DEVICE, GRLimits.DEVICE);
		this._da_righty_inside_legend.setDataLimits(this._datalim_righty_inside_legend);
	}
}

/**
Sets the end date.
@param endDate value to set the end date to.
*/
protected void setEndDate(DateTime endDate) {
	this._end_date = endDate;
}

/**
 * Set the left y-axis maximum y value for interactive viewing.
 */
protected void setLeftYAxisViewMaxY ( String leftYAxisViewMaxY ) {
	this.leftYAxisViewMaxY = leftYAxisViewMaxY;
	if ( leftYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_USE_PRODUCT_PROPERTIES) ) {
		// Clear so it is not used.
		this.leftYAxisViewMaxYFromData = Double.NaN;
	}
	else if ( StringUtil.isDouble(leftYAxisViewMaxY) ) {
		// Clear so it is not used.
		this.leftYAxisViewMaxYFromData = Double.NaN;
	}
}

/**
 * Set the left y-axis maximum y value from a data page view.
 */
protected void setLeftYAxisViewMaxYFromData ( double leftYAxisViewMaxYFromData ) {
	this.leftYAxisViewMaxYFromData = leftYAxisViewMaxYFromData;
}

/**
 * Set the left y-axis minimum y value for interactive viewing.
 */
protected void setLeftYAxisViewMinY ( String leftYAxisViewMinY ) {
	this.leftYAxisViewMinY = leftYAxisViewMinY;
	if ( leftYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_USE_PRODUCT_PROPERTIES) ) {
		// Clear so it is not used.
		this.leftYAxisViewMinYFromData = Double.NaN;
	}
	else if ( StringUtil.isDouble(leftYAxisViewMinY) ) {
		// Clear so it is not used.
		this.leftYAxisViewMinYFromData = Double.NaN;
	}
}

/**
 * Set the left y-axis minimum y value from a data page view.
 */
protected void setLeftYAxisViewMinYFromData ( double leftYAxisViewMinYFromData ) {
	this.leftYAxisViewMinYFromData = leftYAxisViewMinYFromData;
}

/**
 * Set the right y-axis maximum y value for interactive viewing.
 */
protected void setRightYAxisViewMaxY ( String rightYAxisViewMaxY ) {
	this.rightYAxisViewMaxY = rightYAxisViewMaxY;
	if ( rightYAxisViewMaxY.equalsIgnoreCase(YAXIS_LIMITS_USE_PRODUCT_PROPERTIES) ) {
		// Clear so it is not used.
		this.rightYAxisViewMaxYFromData = Double.NaN;
	}
	else if ( StringUtil.isDouble(rightYAxisViewMaxY) ) {
		// Clear so it is not used.
		this.rightYAxisViewMaxYFromData = Double.NaN;
	}
}

/**
 * Set the right y-axis maximum y value from a data page view.
 */
protected void setRightYAxisViewMaxYFromData ( double rightYAxisViewMaxYFromData ) {
	this.rightYAxisViewMaxYFromData = rightYAxisViewMaxYFromData;
}

/**
 * Set the right y-axis minimum y value for interactive viewing.
 */
protected void setRightYAxisViewMinY ( String rightYAxisViewMinY ) {
	this.rightYAxisViewMinY = rightYAxisViewMinY;
	if ( rightYAxisViewMinY.equalsIgnoreCase(YAXIS_LIMITS_USE_PRODUCT_PROPERTIES) ) {
		// Clear so it is not used.
		this.rightYAxisViewMinYFromData = Double.NaN;
	}
	else if ( StringUtil.isDouble(rightYAxisViewMinY) ) {
		// Clear so it is not used.
		this.rightYAxisViewMinYFromData = Double.NaN;
	}
}

/**
 * Set the right y-axis minimum y value from a data page view.
 */
protected void setRightYAxisViewMinYFromData ( double rightYAxisViewMinYFromData ) {
	this.rightYAxisViewMinYFromData = rightYAxisViewMinYFromData;
}

/**
Sets the maximum end date.
@param d the end date.
*/
protected void setMaxEndDate(DateTime d) {
	this._max_end_date = new DateTime(d);
}

/**
Sets the maximum start date.
@param d the start date.
*/
protected void setMaxStartDate(DateTime d) {
	this._max_start_date = new DateTime(d);
}

/**
 * Set the list of selected time series, from legend selects.
 * This is used when the list of TSGraph in the TSGraphJComponent is reinitialized during interactive edits.
 * Because the selected time series are dynamic and not stored in properties, the selects must be transferred manually.
 * @param selectedTimeSeriesList list of time series that should be considered as selected,
 * as if they were clicked on in the legend.
 */
protected void setSelectedTimeSeriesList ( List<TS> selectedTimeSeriesList ) {
	this._selectedTimeSeriesList = selectedTimeSeriesList;
}

/**
 * Sets whether drawing area outlines should be drawn, useful for development.
 */
protected void setShowDrawingAreaOutline ( boolean showDrawingAreaOutline ) {
	this._showDrawingAreaOutline = showDrawingAreaOutline;
}

/**
Sets the start date.
@param startDate value to set the start date to.
*/
protected void setStartDate(DateTime startDate) {
	this._start_date = startDate;
}

// TODO SAM 2006-10-01 Move this to TSUtil or similar for general use.
/**
Determine the nearest DateTime equal to or before the candidate DateTime,
typically in order to determine an iterator start DateTime that is compatible with the specific time series.
For example, when graphing time series with data having different intervals,
the graphing software will determine the visible window using a DateTime range with a precision that matches the smallest data interval.
Time series that use dates that are less precise may be able to simply reset the more precise DateTime to a lower precision.
However, it may also be necessary for calling code to decrement the returned DateTime by an
interval to make sure that a complete overlapping period is considered during iteration.

Time series that use higher precision DateTimes also need to check the candidate DateTime for offsets.
For example, the candidate DateTime may have a precision of DateTime.PRECISION_HOUR and an hour of 6.
The time series being checked may be a 6-hour time series but observations are recorded at 0300, 0900, 1500, 2100.
In this case, the first DateTime in the time series before the candidate must be chosen,
regardless of whether the candidate's hour aligns with the time series data.
@param candidate_DateTime The candidate DateTime, which may have a precision greater than or less than those used by the indicated time series.
@param ts Time series to examine, which may be a regular or irregular time series.
@return DateTime matching the precision of DateTimes used in the specified time
series that is equal to or less than the candidate DateTime.
The returned DateTime will align with the time series data and may NOT align evenly with the candidate DateTime.
*/
public static DateTime getNearestDateTimeLessThanOrEqualTo ( DateTime candidate_DateTime, TS ts ) {
	DateTime returnDate = new DateTime(candidate_DateTime);
	returnDate.round(-1, ts.getDataIntervalBase(), ts.getDataIntervalMult());

	// Compute the offset from exact interval breaks for this time series.
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

	// Add the offset to the rounded base time if there is one.
	if(!(start_not_rounded.equals(start_rounded))) {
		returnDate.add(diff);
	}

	// Set precision for the time series.
	returnDate.setPrecision(ts.getDate1().getPrecision());

	return returnDate;
}

/**
 * Toggle whether a time series is selected or not (from a legend selection).
 * If selected, add to the list.  If not selected, remove from the list.
 * This keeps the list short for lookups.
 * @param ts time series to be (un)selected.
 * @return true if the time series is selected after call, false if it is unselected after call
 */
public boolean toggleTimeSeriesSelection ( TS ts ) {
	boolean found = false;
	boolean isSelected = false;
	TS ts2;
	for ( int i = (this._selectedTimeSeriesList.size() - 1); i >= 0; i-- ) {
		ts2 = this._selectedTimeSeriesList.get(i);
		if ( ts2 == ts ) {
			// Remove previous instance.
			this._selectedTimeSeriesList.remove(ts);
			isSelected = false;
			found = true;
		}
	}
	if ( !found ) {
		// Was not found so toggle on by adding to the list.
		this._selectedTimeSeriesList.add(ts);
		isSelected = true;
	}
	return isSelected;
}

}