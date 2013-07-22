// ----------------------------------------------------------------------------
// TSProduct - define a time series product
// ----------------------------------------------------------------------------
// History:
//
// 2001-11-09	Steven A. Malers, RTi	Initial version.  For now use to define
//					graph products but in the future may
//					use for report products also.
// 2002-01-18	SAM, RTi		Add getNumSubProducts().  Add a
//					constructor that takes no arguments, for
//					use with TSGraphCanvas.
//					Add setPropValue() to simplify setting
//					TSProduct properties.  Fix a number of
//					bugs found while testing.  Add _tslist
//					to carry around the time series for
//					the product so we have less things to
//					pass around.  Move checkTSProduct()
//					from TSGraphCanvas() to
//					checkProperties() in this class.  Add
//					getDefaultPropValue().
//					Move the graph type static data here
//					to be more generic (work with Swing and
//					NonSwing).  Add writeFile() to save the
//					file.
// 2002-02-06	SAM, RTi		Add grid properties.
// 2002-04-25	SAM, RTi		Add TSAlias property in cases where a
//					time series has an alias.  This allows
//					better interfacing with TSEngine.
//					Fix so default graph title is "" (was
//					null before).
//					Add LegendPosition property to position
//					the legend on the page.
//					Change default fonts to Arial.
// 2002-05-20	SAM, RTi		Update to include XYScatter properties
//					for changing the analysis properties.
//					Later these may be switched to time
//					series properties but currently the
//					analysis applies to the graph.  Add
//					DataLabelFormat, DataLabelPosition, and
//					DataLabelFont* properties for the graph
//					and data, similar to the legend.
//					Add XYScatterConfidenceInterval
//					property.
// 2003-05-08	SAM, RTi		Override the constructor to take a
//					PropList to facilitate in-memory
//					creation of TSProducts.
// 2003-05-14	SAM, RTi		* Add the XYScatterIntercept property.
//					* Add the XYScatterAnalyzeForFilling
//					  property.
//					* Add the XYScatterFillPeriodStart and
//					  XYScatterFillPeriodEnd properties.
//					* Add the
//				 	 XYScatterIndependentAnalysisPeriodStart
//					  and
//					  XYScatterIndependentAnalysisPeriodEnd
//					  properties.
//					* Add the
// 				 	  XYScatterDependentAnalysisPeriodStart
//					  and
//					  XYScatterDependentAnalysisPeriodEnd
//					  properties.
// 2003-06-04	SAM, RTi		* Change TSGraphCanvas reference to
//					  TSGraphJComponent for Swing update.
//					* Update to handle templates in
//					  getNumSubProducts().
// 2004-02-24	J. Thomas Sapienza, RTi	Added methods, including:
//					* showProps()
//					* removeSubProduct()
//					* renameSubProductProps()
//					* sortProps()
//					* swapSubProducts()
// 2004-03-02	JTS, RTi		Added methods, including:
//					* showProps(String)
//					* removeData()
//					* renameDataProps()
//					* swapData()
// 2004-04-20	JTS, RTi		* Added default properties for 
//					  annotations.
//					* Layered properties can now be 
//					  specified to search for annotations.
//					* Added checkAnnotationProperties().
//					* Changed getDefaultProperty() to 
//					  recognize annotations.
//					* Changed setPropValue() to recognize
//					  annotations.
//					* Added getNumAnnotations().
//					* Changed writeFile() and 
//					  writeDataTableFile() to write
//					  annotation properties.
// 2004-04-21	JTS, RTi		* Added swapAnnotations().
//					* Added renameAnnotationProps().
//					* Added removeAnnotation().
//					* Removed swapData().
//					* Removed removeData().
// 2004-04-27	JTS, RTi		Added property defaults for 
//					shapetype, points, and point.
// 2004-05-03	JTS, RTi		* Added __dirty and isDirty().
//					* Added setDirty().
//					* Added the ProductID property.
//					* Added getAllProps().
//					* Added setPropsHowSet().
// 2004-05-13	JTS, RTi		Added the ProductName property.
// 2004-05-25	JTS, RTi		Added unSet().
// 2004-06-09	JTS, RTi		Change removeSubProduct() so that now
//					when a subproduct is removed, its time
//					series are also removed from the
//					__tslist Vector.
// 2004-07-26	JTS, RTi		Added propsSaved() so that properties
//					are marked not dirty properly once 
//					a product is saved.
// 2005-04-29	JTS, RTi		* Added GRAPH_TYPE_POINT.
// 2005-05-02	JTS, RTi		Added another version of 
//					getDefaultPropValue() that takes a 
//					parameter called graphType, in order
//					to easily allow defaulting different
//					values for different graph types.
// 2005-05-04	JTS, RTi		* Added GRAPH_TYPE_PREDICTED_VALUE.
//					* Added 
//					  GRAPH_TYPE_PREDICTED_VALUE_RESIDUAL.
// 2005-06-09	JTS, RTi		LegendPosition now includes "None"
// 2005-10-05	JTS, RTi		Added default values for Symbol 
//					annotations.
// 2005-10-18	JTS, RTi		Added "AnnotationProvider" property.
// 2005-10-20	JTS, RTi		* Added "LayoutYPercent" property.
//					* Corrected some code for saving symbol
//					  annotations.
//					* Added code to support adding hidden
//					  properties.
//					* Made the code that saves to a TSP
//					  more verbose, as it was getting 
//					  too tight and unreadable.
// 2005-10-26	JTS, RTi		Added support for the "XInputFormat"
//					annotation property.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GRTS;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.lang.Math;

import java.util.List;
import java.util.Vector;

import RTi.TS.TS;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
The TSProduct class provides methods to read a time series product (tsp) file,
store, and manipulate the properties of the product.  Each product is defined
in a product file as follows:
<pre>
#
# Comments start with #
#
[Product]

# product properties - surround with double quotes if values contain spaces
xxxxx=xxxxxx

[SubProduct 1]

# "sub-product", e.g., a graph on a page (page is product and may have multiple
# graphs)

[Data 1.1]

# First data item in the SubProduct (e.g., first time series).

[Data 1.2]

# Second data item in the SubProduct (e.g., first time series).

[SubProduct 2]

[Data 2.1]

[Data 2.2]

... etc.
</pre> 

<br>
<p>
The properties for each section are defined in the following table.  Most of
the properties are recognized in some form in the TSViewFrame class properties.
Properties will be enabled as development continues.  Unless specifically noted,
all properties can be overridden (e.g., a product file can be read and then
a dynamic property like CurrentDateTime can be set at run-time by the
application before processing the product).
</p>

<p>
All colors can be specified using formats compatible with the
GRColor.parseColor() method.  Examples are common named colors (e.g., "red"),
RGB triplets (e.g., "255,128,0"), and hexadecimal RGB colors (e.g., "0xRRGGBB").
</p>

<table width=100% cellpadding=10 cellspacing=0 border=2>

<tr>
<td colspan=3 align="center" bgcolor="yellow">
<b>Product Properties (e.g, a graph page or report)</b></td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>CurrentDateTime</b></td>
<td>The current date and time to be drawn as a vertical line on all graphs.  If
the property is not specified, no current
date/time line will be drawn.  If specified as "Auto", the current system time
will be used for the date/time.  If specified as a valid date/time string (e.g.,
"2002-02-05 15"), the string will be parsed to obtain the date/time (in this
case the date/time is often specified by the application at run time).
A vertical line will be drawn on the graph to indicate the current time.</td>
<td>Not drawn</td>
</tr>

<tr>
<td><b>CurrentDateTimeColor</b></td>
<td>Color to use to draw the current date and time.</td>
<td>Green</td>
</tr>

<tr>
<td><b>Enabled</b></td>
<td>Indicates whether the product should be processed.  Specify as "true" or
"false".</td>
<td>true</td>
</tr>

<tr>
<td><b>MainTitleFontName</b></td>
<td>Name of font to use for main title (Arial, Courier, Helvetica,
TimesRoman)</td>
<td>Arial</td>
</tr>

<tr>
<td><b>MainTitleFontSize</b></td>
<td>Size, in points, for main title.</td>
<td>20</td>
</tr>

<tr>
<td><b>MainTitleFontStyle</b></td>
<td>Font style (Bold, Italic, Plain).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>MainTitleString</b></td>
<td>Main title for the product, centered at the top of the page.</td>
<td>No main title.</td>
</tr>

<tr>
<td><b>OutputFile</b></td>
<td>Output file when product is generated in batch mode.  <B>This property
is often set at run time.</b></td>
<td><i>C:\TEMP\tmp.jpg</i> on personal computers, <i>/tmp/tmp.jpg</i> on
UNIX</td>
</tr>

<tr>
<td><b>PeriodEnd</b></td>
<td>Ending date for time series data in the product.
The date should be formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>PeriodStart</b></td>
<td>Starting date for time series data in the product.
The date should be formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>PreviewOutput</b></td>
<td>Indicates whether the product should be visually previewed before
output.  <B>This property
is often set at run time.</b></td>
<td>False</td>
</tr>

<tr>
<td><b>ProductType</b></td>
<td>Currently only "Graph" is supported.</td>
<td>"Graph"</td>
</tr>

<tr>
<td><b>SubTitleFontName</b></td>
<td>Name of font to use for Sub title (e.g., Arial)</td>
<td>Arial</td>
</tr>

<tr>
<td><b>SubTitleFontSize</b></td>
<td>Size, in points, for Sub title.</td>
<td>10</td>
</tr>

<tr>
<td><b>SubTitleFontStyle</b></td>
<td>Font style (Bold, Italic, Plain).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>SubTitleString</b></td>
<td>Sub title for the product.</td>
<td>No subtitle.</td>
</tr>

<tr>
<td><b>SymbolAntiAlias</b></td>
<td>Whether symbols drawn on the map should be anti-aliased or not.  
Anti-aliased symbols (especially those with curves) look better, though they
may not be as distinct as non-anti-aliased ones.</td>
<td>True</td>
</tr>

<tr>
<td><b>TotalHeight</b></td>
<td>Height of the total drawing space, which may include multiple graphs,
pixels.
</td>
<td>400</td>
</tr>

<tr>
<td><b>TotalWidth</b></td>
<td>Width of the total drawing space, which may include multiple graphs,
pixels.
</td>
<td>400</td>
</tr>

<tr>
<td align=center colspan=3 bgcolor="yellow">
<b>SubProduct Properties (e.g., for each graph on page)</b></td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>BarOverlap</b></td>
<td>For use with bar graphs.  This property controls how bars are positioned
relative to each other and can have the value of False (bars will be separated horizontally)
or True (bars will overlap, with the first time series drawn first and the last time series on top).
This property is useful for creating exceedance probability plots.</td>
<td>CenteredOnDate</td>
</tr>

<tr>
<td><b>BarPosition</b></td>
<td>For use with bar graphs.  This property controls how bars are positioned
relative to the date for the data and can have the values CenteredOnDate,
LeftOfDate, or RightOfDate.</td>
<td>CenteredOnDate</td>
</tr>

<tr>
<td><b>BottomXAxisLabelFontName</b></td>
<td>Name of font for bottom x-axis labels (see Product.MainLabelFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>BottomXAxisLabelFontSize</b></td>
<td>Bottom x-axis labels font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>BottomXAxisLabelFontStyle</b></td>
<td>Bottom x-axis labels font style (see Product.MainLabelFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>BottomXAxisTitleFontName</b></td>
<td>Name of font for bottom x-axis title (see Product.MainTitleFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>BottomXAxisTitleFontSize</b></td>
<td>Bottom x-axis title font size, points.</td>
<td>12</td>
</tr>

<tr>
<td><b>BottomXAxisTitleFontStyle</b></td>
<td>Bottom x-axis title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>BottomXAxisLabelFormat</b></td>
<td>Format for X axis labels.  Currently this is confined to date/time axes
and only "MM-DD" is recognized.</td>
<td>Determined automatically.</td>
</tr>

<tr>
<td><b>BottomXAxisMajorGridColor</b></td>
<td>Color to use for the major grid.</td>
<td>Most graph types automatically set to "None".</td>
</tr>

<tr>
<td><b>BottomXAxisMinorGridColor</b></td>
<td>Color to use for the minor grid.
<b>This property is not implemented</b>.</td>
<td>"None".</td>
</tr>

<tr>
<td><b>BottomXAxisTitleString</b></td>
<td>Bottom X axis title string.</td>
<td>As appropriate for the graph type (often none if dates).</td>
</tr>

<tr>
<td><b>DataLabelFontName</b></td>
<td>Name of font for data labels (see Product.MainLabelFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>DataLabelFontSize</b></td>
<td>Data label font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>DataLabelFontStyle</b></td>
<td>Data label font style (see Product.MainLabelFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>DataLabelFormat</b></td>
<td>Format for data point labels.  Currently only %D (date/time), %v (data
value, formatted based on units), and string literals are recognized.  If this
property is specified, it will be used for all time series unless a time
series property is specified.</td>
<td>None (no label)</td>
</tr>

<tr>
<td><b>DataLabelPosition</b></td>
<td>Position for data point labels (UpperRight, Right, LowerRight, Below,
LowerLeft, Left, UpperLeft, Above, Center).  If this
property is specified, it will be used for all time series unless a time
series property is specified.</td>
</td>
<td>Right</td>
</tr>

<tr>
<td><b>Enabled</b></td>
<td>Indicates whether the sub-product should be processed.  Specify as "true" or
"false".</td>
<td>true</td>
</tr>

<tr>
<td><b>GraphHeight</b></td>
<td>Graph height in pixels.
</td>
<td>Product.TotalHeight (minus space for titles, etc.) if one graph, or an even
fraction of Product.TotalHeight (minus space for titles, etc.) if multiple
graphs.
</td>
</tr>

<tr>
<td><b>GraphType</b></td>
<td>Indicates the graph type for all data in a graph product.  Available
options are: "Bar", "Duration", "Line", "PeriodOfRecord", "XY-Scatter", "Point".
</td>
<td>"Line"</td>
</tr>

<tr>
<td><b>GraphWidth</b></td>
<td>Graph width in pixels.
</td>
<td>Product.TotalWidth (minus space for titles, etc.).  Default is to stack
graphs vertically so width will be the same for all graphs.
</tr>

<tr>
<td><b>LeftYAxisDirection</b></td>
<td>The direction of values, where "Normal" is the normal orientation (generally positive values increase up except
for graph types where it goes the other way) and
"Reverse" indicates that values should be in the reverse orientation of normal.</td>
<td>Normal.</td>
</tr>

<tr>
<td><b>LeftYAxisIgnoreUnits</b></td>
<td>Indicates whether to ignore units for the lefty Y axis.  Normally, units
are checked to make sure that data can be plotted consistently.  If this
property is set, then the user will not be prompted at run-time to make a
decision.</td>
<td>If not specified, the units will be checked at run-time and, if not
compatible, the user will be prompted to indicate whether to ignore units in
the graphs.  The property will not be reset automatically but will be handled
internally using the interactively supplied value.</td>
</tr>

<tr>
<td><b>LeftYAxisLabelFontName</b></td>
<td>Name of font for left y-axis labels (see Product.MainLabelFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>LeftYAxisLabelFontSize</b></td>
<td>Left y-axis labels font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>LeftYAxisLabelFontStyle</b></td>
<td>Left y-axis labels font style (see Product.MainLabelFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>LeftYAxisLabelPrecision</b></td>
<td>If numeric data, the number of digits after the decimal point in
labels.</td>
<td>Automatically determined from graph type and/or data units.
</td>
</tr>

<tr>
<td><b>LeftYAxisMajorGridColor</b></td>
<td>Color to use for the major grid.</td>
<td>Most graph types automatically set to "lightgray".</td>
</tr>

<tr>
<td><b>LeftYAxisMax</b></td>
<td>Maximum value for the left Y Axis.</td>
<td>"Auto", automatically determined.  If the actual data exceed the value, the
property will be ignored.</td>
</tr>

<tr>
<td><b>LeftYAxisMin</b></td>
<td>Minimum value for the left Y Axis.</td>
<td>"Auto", automatically determined.  If the actual data exceed the value, the
property will be ignored.</td>
</tr>

<tr>
<td><b>LeftYAxisMinorGridColor</b></td>
<td>Color to use for the minor grid.
<b>This property is not implemented</b>.</td>
<td>"None".</td>
</tr>

<tr>
<td><b>LeftYAxisTitleFontName</b></td>
<td>Name of font for left y-axis title (see Product.MainTitleFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>LeftYAxisTitleFontSize</b></td>
<td>Left y-axis title font size, points.</td>
<td>12</td>
</tr>

<tr>
<td><b>LeftYAxisTitleFontStyle</b></td>
<td>Left y-axis title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>LeftYAxisTitleString</b></td>
<td>Left y axis title string.  <b>Note that due to limitations in Java graphics,
the left y-axis title is placed at the top of the left y-axis so that it takes
up roughly the same space as the y-axis labels.  The top-most label is shifted
down to make room for the title.</b></td>
<td>As appropriate for the graph type (often the data units).</td>
</tr>

<tr>
<td><b>LeftYAxisType</b></td>
<td>Left y-axis type (Log, or Linear).</td>
<td>Linear</td>
</tr>

<tr>
<td><b>LeftYAxisUnits</b></td>
<td>Left y-axis units.  <b>This property is currently used internally and full
support is being phased in.</b>  See also "LeftYAxisIgnoreUnits".
</td>
<td>Units from first valid time series, or as appropriate for the graph
type.</td>
</tr>

<tr>
<td><b>LegendFontName</b></td>
<td>Name of font for legend (see Product.MainTitleFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>LegendFontSize</b></td>
<td>Legend font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>LegendFontStyle</b></td>
<td>Legend font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>LegendFormat</b></td>
<td>Legend format using the formats recognized by the TS.formatLegend() method,
as follows:
<ul>
<li>	%% = literal percent</li>
<li>	%A = time series alias</li>
<li>	%D = description (e.g., "RED RIVER BELOW MY TOWN")</li>
<li>	%F = full time series identifier (e.g.,
	<font size=-2>"XX_FREE.HydroBase_USGS.QME.24HOUR.Trace1</font>)</li>
<li>	%I = Full interval part of the identifier (e.g., "24Hour").</li>
<li>    %i = input name part of the identifier (e.g., filename).</li>
<li>	%b = base part of the interval (e.g., "24").</li>
<li>	%m = multiplier part of the interval (e.g., "Hour").</li>
<li>	%L = full location part of the identifier (e.g., "XX_FREE").</li>
<li>	%l = main part of the location (e.g., "XX").</li>
<li>	%w = sub-location (e.g., "FREE").</li>
<li>	%S = The full source part of the identifier (e.g.,
	HydroBase_USGS).</li>
<li>	%s = main data source (e.g., "HydroBase").</li>
<li>	%x = sub-source (e.g., "USGS").</li>
<li>	%T = Full data type (e.g., "QME").</li>
<li>	%t = Main data source (currently same as full source, reserved for
	future use).</li>
<li>	%y = Sub-source (reserved for future use).</li>
<li>	%U = Data units (e.g., "CFS").</li>
<li>	%Z = scenario part of identifier (e.g., "Trace1").</li>
</ul>
If a blank legend format is specified, no legend will be displayed.
</td>
<td>Auto, which uses Description, Identifier, Units, Period</td>
</tr>

<tr>
<td><b>LegendPosition</b></td>
<td>Position for the legend (Left, Right, Top, Bottom, None).</td>
<td>Bottom</td>
</tr>

<tr>
<td><b>MainTitleFontName</b></td>
<td>Name of font to use for graph main title (see
Product.MainTitleFontName)</td>
<td>Arial</td>
</tr>

<tr>
<td><b>MainTitleFontSize</b></td>
<td>Size, in points, for graph main title.</td>
<td>10</td>
</tr>

<tr>
<td><b>MainTitleFontStyle</b></td>
<td>Graph main title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>MainTitleString</b></td>
<td>Main title for the graph.</td>
<td>None, or appropriate for graph type.</td>
</tr>

<tr>
<td><b>PeriodEnd</b></td>
<td>Ending date for time series data in the sub-product.  The date should be
formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>PeriodStart</b></td>
<td>Starting date for time series data in the sub-product.  The date should be
formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>RightYAxisLabelFontName</b></td>
<td>Name of font for right y-axis labels (see Product.MainLabelFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>RightYAxisLabelFontSize</b></td>
<td>Right y-axis labels font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>RightYAxisLabelFontStyle</b></td>
<td>Right y-axis labels font style (see Product.MainLabelFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>RightYAxisTitleFontName</b></td>
<td>Name of font for right y-axis title (see Product.MainTitleFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>RightYAxisTitleFontSize</b></td>
<td>Right y-axis title font size, points.</td>
<td>12</td>
</tr>

<tr>
<td><b>RightYAxisTitleFontStyle</b></td>
<td>Right y-axis title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>RightYAxisTitleString</b></td>
<td>Right y axis title string.  <b>Note that due to limitations in Java
graphics, the right y-axis title is placed at the top of the right y-axis so
that it takes up roughly the same width as the y-axis labels.  The top-most
label is shifted down to make room for the title.</b></td>
<td>As appropriate for the graph type (often the data units).</td>
</tr>

<tr>
<td><b>SubTitleFontName</b></td>
<td>Name of font to use for graph Sub title (see Product.MainTitleFontName)</td>
<td>Arial</td>
</tr>

<tr>
<td><b>SubTitleFontSize</b></td>
<td>Size, in points, for graph sub title.</td>
<td>10</td>
</tr>

<tr>
<td><b>SubTitleFontStyle</b></td>
<td>Graph sub title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>SubTitleString</b></td>
<td>Sub title for the graph.</td>
<td>No subtitle.</td>
</tr>

<tr>
<td><b>TopXAxisLabelFontName</b></td>
<td>Name of font for Top x-axis labels (see Product.MainLabelFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>TopXAxisLabelFontSize</b></td>
<td>Top x-axis labels font size, points.</td>
<td>10</td>
</tr>

<tr>
<td><b>TopXAxisLabelFontStyle</b></td>
<td>Top x-axis labels font style (see Product.MainLabelFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>TopXAxisTitleFontName</b></td>
<td>Name of font for Top x-axis title (see Product.MainTitleFontName).</td>
<td>Arial</td>
</tr>

<tr>
<td><b>TopXAxisTitleFontSize</b></td>
<td>Top x-axis title font size, points.</td>
<td>12</td>
</tr>

<tr>
<td><b>TopXAxisTitleFontStyle</b></td>
<td>Top x-axis title font style (see Product.MainTitleFontStyle).</td>
<td>Plain</td>
</tr>

<tr>
<td><b>TopXAxisTitleString</b></td>
<td>Top X axis title string.</td>
<td>As appropriate for the graph type (often none if dates).</td>
</tr>

<tr>
<td><b>XYScatterAnalyzeForFilling</b></td>
<td>For XY Scatter Graph, if true, then the RMSE value that is reported is
calculated using the difference between Y and Y estimated from the best-fit
equation.</td>
<td>false</td>
</tr>

<tr>
<td><b>XYScatterDependentAnalysisPeriodEnd</b></td>
<td>For XY Scatter Graph, indicate the
end of the period used to analyze the dependent time series.
</td>
<td>Blank (analyze the full period).</td>
</tr>

<tr>
<td><b>XYScatterDependentAnalysisPeriodStart</b></td>
<td>For XY Scatter Graph, indicate the
start of the period used to analyze the dependent time series.
</td>
<td>Blank (analyze the full period).</td>
</tr>

<tr>
<td><b>XYScatterFillPeriodEnd</b></td>
<td>For XY Scatter Graph when XYScatterAnalyzeForFilling is true, indicate the
end of the fill period as a standard date/time string.
</td>
<td>Blank (fill the full period).</td>
</tr>

<tr>
<td><b>XYScatterFillPeriodStart</b></td>
<td>For XY Scatter Graph when XYScatterAnalyzeForFilling is true, indicate the
start of the fill period as a standard date/time string.
</td>
<td>Blank (fill the full period).</td>
</tr>

<tr>
<td><b>XYScatterIndependentAnalysisPeriodEnd</b></td>
<td>For XY Scatter Graph, indicate the
end of the period used to analyze the independent time series.
</td>
<td>Blank (analyze the full period).</td>
</tr>

<tr>
<td><b>XYScatterIndependentAnalysisPeriodStart</b></td>
<td>For XY Scatter Graph, indicate the
start of the period used to analyze the independent time series.
</td>
<td>Blank (analyze the full period).</td>
</tr>

<tr>
<td><b>XYScatterIntercept</b></td>
<td>For XY Scatter Graph, a forced intercept value (A in equation A + Bx).
This is only valid with Linear transformation.</td>
<td>Blank (no forced intercept)</td>
</tr>

<tr>
<td><b>XYScatterMethod</b></td>
<td>For XY Scatter Graph, method used to determine curve fit (OLSRegression or
MOVE2).</td>
<td>OLSRegression</td>
</tr>

<tr>
<td><b>XYScatterMonth</b></td>
<td>For XY Scatter Graph, month(s) of data used to determine curve fit.
If one equation is used, zero or more months can be specified.  If monthly
equations are used, only one month currently can be specified.</td>
<td>Blank</td>
</tr>

<tr>
<td><b>XYScatterNumberOfEquations</b></td>
<td>For XY Scatter Graph, number of equations used to analyze the data for the
curve fit (OneEquation or MonthlyEquations).
</td>
<td>OneEquation</td>
</tr>

<tr>
<td><b>XYScatterTransformation</b></td>
<td>For XY Scatter Graph, transformation that is applied to the data for the
curve fit (None or Log).
</td>
<td>None</td>
</tr>

<tr>
<td><b>ZoomEnabled</b></td>
<td>Indicates whether the graph can be zoomed (true) or not (false).
<td>Graph types are evaluated and the property is automatically set.
XY-Scatter and Duration graphs are not zoomable.
</td>
</tr>

<tr>
<td><b>ZoomGroup</b></td>
<td>Indicate a group identifier that is used to associate graphs for zooming
purposes.  For example, there may be more than one distinct group of graphs,
each with its own overall period or data limits.  The graph types may also
be incompatible for zooming.  <b>This is an experimental feature and should
currently not be specified in product files.</b></td>
<td>All graphs are assigned to group "1".</td>
</tr>

<tr>
<td align=center colspan=3 bgcolor="yellow">
<b>Data Properties (e.g., for each time series)</b></td>
</tr>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr>

<tr>
<td><b>Color</b></td>
<td>Color to use when drawing the data, as allowed by the GRColor.parse()
method.  Examples are named colors (e.g., "red"), RGB triplets (e.g.,
"255,0,128"), and hexadecimal RGB (e.g., "0xFF0088").</td>
<td>Repeating, using common colors.</td>
</tr>

<tr>
<td><b>DataLabelFormat</b></td>
<td>Format for data point labels.  Currently only %D (date/time), %v (data
value, formatted based on units), and string literals are recognized.
</td>
<td>None (no label)</td>
</tr>

<tr>
<td><b>DataLabelPosition</b></td>
<td>Position for data point labels (UpperRight, Right, LowerRight, Below,
LowerLeft, Left, UpperLeft, Above, Center.
</td>
<td>Right</td>
</tr>

<tr>
<td><b>Enabled</b></td>
<td>Indicates whether the data should be processed.  Specify as "true" or
"false".</td>
<td>true</td>
</tr>

<tr>
<td><b>FlaggedDataSymbolStyle</b></td>
<td>Symbol style for flagged data (see SymbolStyle for valid values).</td>
<td>None</td>
</tr>

<tr>
<td><b>GraphType</b></td>
<td>Indicates the graph type for the data in a graph product.  Available
options are: "Bar", "Duration", "Line", "PeriodOfRecord", "XY-Scatter", "Point".
<b>Currently the sub-product property is used for all data.  It is envisioned
that this property will be enabled in the future to allow different data
representations to be plotted together (e.g., monthly as bars, daily as line).
</b>
</td>
<td>Property not enabled.</td>
</tr>

<tr>
<td><b>LegendFormat</b></td>
<td>Legend format using the formats recognized by the TS.formatLegend() method.
If a blank legend format is specified, no legend will be displayed.
</td>
<td>Auto, which uses Description, Identifier, Units, Period</td>
</tr>

<tr>
<td><b>LineStyle</b></td>
<td>Line style.  Currently only "None" (e.g., for symbols only) and "Solid"
are allowed.</td>
<td>"Solid"</td>
</tr>

<tr>
<td><b>LineWidth</b></td>
<td>Line width, pixels.  Currently a line width of 1 pixel is always used.
<td>1</td>
</tr>

<tr>
<td><b>PeriodEnd</b></td>
<td>Ending date for time series data in the data item.  The date should be
formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>PeriodStart</b></td>
<td>Starting date for time series data in the data item.  The date should be
formatted according
to common conventions (e.g., YYYY-MM-DD HH:mm), and should ideally be of
appropriate precision for the data being queried.
This property is currently applied in the
TSProcessor.processProduct() method as time series are read.
<b>This property is often set at run time.</b>
</td>
<td>Full period is read.</td>
</tr>

<tr>
<td><b>RegressionLineEnabled</b></td>
<td>Indicates whether the regression line should be shown (currently only used
with the XY-Scatter graph type).  The line is drawn in black (there is currently
not a property to set the line color).</td>
<td>true (if an XY scatter plot)</td>
</tr>

<tr>
<td><b>SymbolSize</b></td>
<td>Symbol size in pixels.</td>
<td>0 (no symbol)</td>
</tr>

<tr>
<td><b>SymbolStyle</b></td>
<td>Symbol style.  Symbols are defined in the GRSymbol class.
Recognized symbols are:
<ul>
<li>	None</li>
<li>	Arrow-Down</li>
<li>	Arrow-Left</li>
<li>	Asterisk</li>
<li>	Circle-Hollow, Circle-Filled</li>
<li>	Diamond-Hollow, Diamond-Filled</li>
<li>	Plus, Plus-Square</li>
<li>	Square-Hollow, Square-Filled</li>
<li>	Triangle-Down-Hollow, Triangle-Down-Filled,
	Triangle-Left-Hollow, Triangle-Left-Filled,
	Triangle-Right-Hollow, Triangle-Right-Filled,
	Triangle-Up-Hollow, Triangle-Up-Filled</li>
<li>	X-Cap, X-Diamond, X-Edge, X-Square</li>
</ul>
</td>
<td>None</td>
</tr>

<tr>
<td><b>TSAlias</b></td>
<td>Time series alias.  The alias is read by calling the
readTimeSeries() method in registered TSSupplier objects (see
TSProcessor.addTSSupplier()).
The TSSuppliers can provide very specific functionality to read time series
from a variety of sources.
</td>
<td>Must specify either a TSAlias and/or TSID.</td>
</tr>

<tr>
<td><b>TSID</b></td>
<td>Time series identifier.  The identifier is read by calling the
readTimeSeries() method in registered TSSupplier objects (see
TSProcessor.addTSSupplier()).
The TSSuppliers can provide very specific functionality to read time series
from a variety of sources.
</td>
<td>Must specify a TSID.</td>
</tr>

<tr>
<td><b>XAxis</b></td>
<td>X-axis to use (Bottom or Top).  <b>This currently always defaults to
bottom.</b></td>
<td>Bottom</td>
</tr>

<tr>
<td><b>YAxis</b></td>
<td>Y-axis to use (Left or Right).  <b>This currently always defaults to
left.</b></td>
<td>Left</td>
</tr>

</table>
*/
public class TSProduct
{

/**
Main property list describing the product.
*/
private PropList __proplist = null;

/**
Run-time properties that will override the file properties.  These are specified as simple strings
with no layering information (e.g., "InitialView").
*/
private PropList __override_proplist = null;

/**
Time series associated with the product.
*/
private List<TS> __tslist = null;

/**
The number of zoom groups among all the different graphs.
*/
private int __num_zoom_groups = 1;

/**
The way in which properties were being added to the proplist prior to a call
being made to startAddingHiddenProps().
*/
private int __howSet = -1;

/**
A dirty flag used for checking if anything has been deleted, or added, or the
order has been changed -- these are things that can't be checked by looking at the proplist.
*/
private boolean __dirty = false;

/**
The annotation providers that will be used to generate annotations on a
graph.  An element in this list at position X corresponds to the
element in the __annotationsProvidersPropLists Vector at position X.
*/
private List<TSProductAnnotationProvider> __annotationProviders = null;

/**
Control properties that determine how the annotation providers put annotations
on a graph.  An element in this list at position X corresponds to the
element in the __annotationsProviders Vector at position X.
*/
private List<PropList> __annotationProviderPropLists = null;

/**
Checks for the annotation providers that were already used on this product,
so that they can be checked and not used twice.
*/
private List<TSProductAnnotationProvider> __usedProviders = new Vector();

/**
Construct a blank TSProduct.  It is assumed that properties will be added by
getting the PropList and adding to it.
@exception Exception if there is an error processing the file.
*/
public TSProduct ()
throws Exception
{	// Just read in as a simple PropList...
	__proplist = new PropList ( "TSProduct" );
	__override_proplist = new PropList ( "override" );
}

/**
Construct a TSProduct from a product file (.tsp).  The product file is read into a PropList.
@param filename Name of gvp file to process.
@param overridePropList Properties that override the properties in the file.
@exception Exception if there is an error processing the file.
*/
public TSProduct ( String filename, PropList overridePropList )
throws Exception
{	// Just read in as a simple PropList...
	__proplist = new PropList ( "TSProduct" );
	__proplist.setPersistentName ( filename );
	__proplist.readPersistent ();
	__override_proplist = overridePropList;
	transferPropList();
}

/**
Construct a TSProduct from a PropList.  The properties in the PropList must
conform to the organization of a time series product file.
@param proplist Properties describing the time series product.
@param override_proplist Properties that override the properties first
PropList (e.g., display properties).  Specify as null if no override properties are given.
@exception Exception if there is an error processing the properties.
*/
public TSProduct ( PropList proplist, PropList override_proplist )
throws Exception
{	// Just read in as a simple PropList...
	__proplist = proplist;
	__override_proplist = override_proplist;
	transferPropList();
}

/**
Adds an annotation provider to the list of providers that will generate
annotations on this product.  If this product currently has any time series
defined in it, the provider will add annotations as soon as this method is 
called.  Otherwise, once all the annotation providers have been added to the
product and time series are set in the product with setTSList(), the annotations
will be set automatically at that point.
@param provider the provider to add.  Cannot be null.
@param controlProps properties that define for each provider how they are to
add annotations.  These properties are provider-specific.  Can be null.
*/
public void addTSProductAnnotationProvider(TSProductAnnotationProvider provider, PropList controlProps) {
	String routine = "TSProduct.addAnnotationProvider";
	if (hasTimeSeries()) {
		try {
			provider.addAnnotations(this, controlProps);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "An error occurred while adding annotations to a TSProduct.");
			Message.printWarning(3, routine, e);
		}
	}
	else {
		if (__annotationProviders == null) {
			__annotationProviders = new Vector();
			__annotationProviderPropLists = new Vector();
		}
	
		__annotationProviders.add(provider);
		__annotationProviderPropLists.add(controlProps);
	}
}

/**
For each annotation provider set in this product, adds the annotations for each
to the time series in the graph.  This is called automatically by setTSList().
*/
private void addAnnotations() {
	String routine = "TSProduct.addAnnotations";
	
	int size = __annotationProviders.size();

	PropList controlProps = null;
	TSProductAnnotationProvider provider = null;
	
	for (int i = 0; i < size; i++) {
		provider = __annotationProviders.get(i);
		if (alreadyUsed(provider)) {	
			continue;
		}
		else {
			__usedProviders.add(provider);
		}
		controlProps = __annotationProviderPropLists.get(i);

		try {
			provider.addAnnotations(this, controlProps);
		}
		catch (Exception e) {
			Message.printWarning(2, routine, "An error occurred while adding annotations to a TSProduct.");
			Message.printWarning(3, routine, e);
		}
	}
}

/**
Checks to see if an annotation provider was already used with this product,
so that duplicate annotations are not set on it.
@param provider the provider to check.
@return true if the provider was already used to set annotations, false if not.
*/
private boolean alreadyUsed(TSProductAnnotationProvider provider) {
	int size = __usedProviders.size();
	TSProductAnnotationProvider p = null;
	for (int i = 0; i < size; i++) {
		 p = __usedProviders.get(i);
		 if (p == provider) {
		 	return true;
		}
	}

	return false;
}

/**
Checks annotation properties to make sure that the annotation is fully-defined.
@param isub the subproduct of the annotation.
@param iann the annotation number.
*/
public void checkAnnotationProperties(int isub, int iann) {
	String shapeType = null;

	if (getLayeredPropValue("AnnotationID", isub, iann, false, true) == null) {
		setPropValue("AnnotationID", getDefaultPropValue("AnnotationID",
			isub, iann, true), isub, iann, true);
	}
	
	if (getLayeredPropValue("Color", isub, iann, false, true) == null) {
		setPropValue("Color", getDefaultPropValue("Color", isub, iann, true), isub, iann, true);
	}
	if (getLayeredPropValue("Order", isub, iann, false, true) == null) {
		setPropValue("Order", getDefaultPropValue("Order", isub, iann, true), isub, iann, true);
	}
	
	shapeType = getLayeredPropValue("ShapeType", isub, iann, false, true);
	if (shapeType == null) {
		setPropValue("ShapeType", getDefaultPropValue("ShapeType", isub, iann, true), isub, iann, true);	
		shapeType = getDefaultPropValue("ShapeType", isub, iann, true);
	}
	
	if (getLayeredPropValue("XInputFormat", isub, iann, false, true) == null) {
		setPropValue("XInputFormat", getDefaultPropValue("XInputFormat", isub, iann, true), isub, iann, true);	
	}

	if (getLayeredPropValue("XAxisSystem", isub, iann, false, true) == null) {
		setPropValue("XAxisSystem", getDefaultPropValue("XAxisSystem", isub, iann, true), isub, iann, true);
	}
	if (getLayeredPropValue("YAxisSystem", isub, iann, false, true) == null) {
		setPropValue("YAxisSystem", getDefaultPropValue("YAxisSystem", isub, iann, true), isub, iann, true);
	}
	
	if (shapeType.equalsIgnoreCase("Text")) {
		if (getLayeredPropValue("FontSize", isub, iann, false, true) == null) {
			setPropValue("FontSize", getDefaultPropValue("FontSize", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("FontStyle", isub, iann, false, true) == null) {
			setPropValue("FontStyle", getDefaultPropValue("FontStyle", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("FontName", isub, iann, false, true) == null) {
			setPropValue("FontName", getDefaultPropValue("FontName", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("Point", isub, iann, false, true) == null) {
			setPropValue("Point", getDefaultPropValue("Points", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("Text", isub, iann, false, true) == null) {
			setPropValue("Text", getDefaultPropValue("Text", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("TextPosition", isub, iann, false, true) == null) {
			setPropValue("TextPosition", getDefaultPropValue("TextPosition", isub, iann, true), isub, iann, true);
		}
	}
	else if (shapeType.equalsIgnoreCase("Line")) {
		if (getLayeredPropValue("LineStyle", isub, iann, false, true) == null) {
			setPropValue("LineStyle",getDefaultPropValue("LineStyle",isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("LineWidth", isub, iann, false, true) == null) {
			setPropValue("LineWidth", getDefaultPropValue( "LineWidth", isub, iann, true), isub, iann, true);
		}
		if (getLayeredPropValue("Points", isub, iann, false, true) == null) {
			setPropValue("Points", getDefaultPropValue("Points",isub, iann, true), isub, iann, true);
		}
	}
	else if (shapeType.equalsIgnoreCase("Symbol")) {
		if (getLayeredPropValue("Point", isub, iann, false, true) == null) {
		    setPropValue("Point", getDefaultPropValue("Point", isub, iann, true), isub, iann, true);
		}

		if (getLayeredPropValue("SymbolPosition", isub, iann, false, true) == null) {
		    setPropValue("SymbolPosition", getDefaultPropValue("SymbolPosition", isub, iann, true), 
				isub, iann, true);
		}

		if (getLayeredPropValue("SymbolStyle", isub, iann, false, true) == null) {
	    	setPropValue("SymbolStyle", getDefaultPropValue("SymbolStyle", isub, iann, true), 
			isub, iann, true);
		}

		if (getLayeredPropValue("SymbolSize", isub, iann, false, true) == null) {
		    setPropValue("SymbolSize", getDefaultPropValue("SymbolSize", isub, iann, true), isub, iann, true);
		}
	}
}

/**
Checks data properties to make sure that the data are fully-defined for a time series in a graph.
Data that are not fully-defined will have the property set to a default value
@param isub the subproduct of the annotation.
@param iann the annotation number.
*/
public void checkDataProperties(int isub, int its) {
//	Message.printStatus ( 1, "", "Checking TS [" + its+"]");
	// "Color"...
	int nts = getNumData(isub);
	String prop_val = getLayeredPropValue("GraphType", isub, -1, false);
	if ( prop_val == null ) {
		prop_val = getDefaultPropValue("GraphType", isub, -1);
	}

	TSGraphType graphType = TSGraphType.valueOfIgnoreCase(prop_val);

	if (getLayeredPropValue("Color",isub,its,false) == null) {
		if ( (graphType == TSGraphType.XY_SCATTER) && (nts <= 2) ) {
			// Force black to be used...
			setPropValue ( "Color", "black", isub, its );
		}
		else {	
			// Use colors for time series...
			setPropValue("Color", TSGraphJComponent.lookupTSColor(its),	isub, its);
		}
	}

	if (graphType == TSGraphType.POINT) {
	    // Point graphs are required to have a symbol
		if (getLayeredPropValue("SymbolStyle",isub,its,false) == null) {
			setPropValue("SymbolStyle",	TSGraphJComponent.lookupTSSymbol(its),isub, its);
		}
	}

	// "DataLabelFormat"...

	if ( getLayeredPropValue ( "DataLabelFormat", isub, its, false ) == null ){
		setPropValue ( "DataLabelFormat",getDefaultPropValue("DataLabelFormat",isub,its),isub, its);
	}

	// "DataLabelPosition"...

	if ( getLayeredPropValue ( "DataLabelPosition", isub, its, false ) ==null){
		setPropValue ( "DataLabelPosition",	getDefaultPropValue("DataLabelPosition",isub,its), isub, its);
	}

	// "Enabled"...

	if ( getLayeredPropValue ("Enabled", isub, its, false ) == null ) {
		setPropValue ( "Enabled", getDefaultPropValue("Enabled",isub,its), isub, its);
	}
	
    if (getLayeredPropValue("FlaggedDataSymbolStyle", isub, its, false) == null) {
        // Use the SymbolStyle value if specified
        String symbolStyle = getLayeredPropValue("SymbolStyle", isub, its, false);
        if ( symbolStyle == null ) {
            setPropValue("FlaggedDataSymbolStyle",
                getDefaultPropValue("FlaggedDataSymbolStyle", isub,its,false,graphType),isub, its);
        }
        else {
            setPropValue("FlaggedDataSymbolStyle",symbolStyle,isub,its);
        }
    }

	if ( getLayeredPropValue ( "GraphType", isub, its, false ) == null ) {
		//setPropValue ( "GraphType", getDefaultPropValue("GraphType",isub,its), isub, its);
	    // Set the default graph type for the line to the same as the graph
	    String graphTypeProp = getLayeredPropValue("GraphType", isub, -1, false);
	    if ( graphTypeProp == null ) {
	        setPropValue ( "GraphType", getDefaultPropValue("GraphType",isub,its), isub, its);
	    }
	    else {
	        setPropValue ( "GraphType", graphTypeProp, isub, its );
	    }
	}

	if ( getLayeredPropValue ( "LegendFormat", isub, its, false ) == null ) {
		setPropValue ( "LegendFormat",getDefaultPropValue("LegendFormat",isub,its),	isub, its);
	}

	if (getLayeredPropValue("LineStyle", isub, its, false) == null) {
		setPropValue ( "LineStyle", getDefaultPropValue("LineStyle",isub, its, false, graphType), isub, its);
	}

	if (getLayeredPropValue("LineWidth", isub, its, false) == null) {
		setPropValue("LineWidth", getDefaultPropValue("LineWidth",isub,its, false, graphType),	isub, its);
	}

	if (graphType == TSGraphType.XY_SCATTER
	   || graphType == TSGraphType.PREDICTED_VALUE
	   || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if (getLayeredPropValue("RegressionLineEnabled", 
		    isub, its, false) == null ) {
			setPropValue("RegressionLineEnabled", getDefaultPropValue("RegressionLineEnabled", isub,its), isub, its);
		}
		if (getLayeredPropValue("XYScatterConfidenceInterval", isub, its, false) == null) {
			setPropValue("XYScatterConfidenceInterval",
                    getDefaultPropValue("XYScatterConfidenceInterval",	isub,its), isub, its);
		}
	}

	if (getLayeredPropValue("SymbolSize", isub, its, false) == null) {
		setPropValue("SymbolSize",getDefaultPropValue("SymbolSize", isub,its, false,graphType),isub, its);
	}

	// PeriodEnd, Period Start set at run-time

	if (getLayeredPropValue("SymbolStyle", isub, its, false) == null) {
		setPropValue("SymbolStyle",getDefaultPropValue("SymbolStyle", isub,its,false,graphType),isub, its);
	}

	if ( getLayeredPropValue ( "TSAlias", isub, its, false ) == null ) {
		setPropValue ( "TSAlias", getDefaultPropValue("TSAlias",isub,its), isub, its);
	}

	if ( getLayeredPropValue ( "TSID", isub, its, false ) == null ) {
		setPropValue ( "TSID", getDefaultPropValue("TSID",isub,its), isub, its);
	}

	if ( getLayeredPropValue ("XAxis", isub, its, false ) == null ) {
		setPropValue ( "XAxis",getDefaultPropValue("XAxis",isub,its),isub, its);
	}

	if ( getLayeredPropValue ("YAxis", isub, its, false ) == null ) {
		setPropValue ( "YAxis",getDefaultPropValue("YAxis",isub,its),isub, its);
	}
}

/**
Check the TSProduct that make sure that properties are defined.  This is done so
that defaults do not need to be assigned by other classes that use the
TSProduct.  Later, needed properties (like fonts) are always assumed to be
defined.  In many cases, only defaults need to be defined at the upper
property levels (e.g., for Product or SubProduct).
*/
public void checkProperties ()
{	int nsubs = getNumSubProducts();
	String prop_val;
	//Message.printStatus ( 1, "TSProduct.checkProperties",
	//"Checking properties for " + nsubs + " subproducts" );

	int how_set_prev = __proplist.getHowSet();
	__proplist.setHowSet ( Prop.SET_AS_RUNTIME_DEFAULT );

	//---------------------------------------------------------------------
	// Product properties
	//---------------------------------------------------------------------
	
    if (getLayeredPropValue("CurrentDateTime", -1, -1, false) == null) {
        setPropValue("CurrentDateTime", getDefaultPropValue("CurrentDateTime",-1,-1), -1, -1);
    }
    
    if (getLayeredPropValue("CurrentDateTimeColor", -1, -1, false) == null) {
        setPropValue("CurrentDateTimeColor", getDefaultPropValue("CurrentDateTimeColor",-1,-1), -1, -1);
    }

	if (getLayeredPropValue("Enabled", -1, -1, false) == null) {
		setPropValue("Enabled",getDefaultPropValue("Enabled",-1,-1), -1, -1);
	}

	// Product MainTitle properties...

	if (getLayeredPropValue("MainTitleFontName", -1, -1, false) == null) {
		setPropValue("MainTitleFontName",getDefaultPropValue("MainTitleFontName",-1,-1), -1, -1);
	}
	if (getLayeredPropValue("MainTitleFontSize", -1, -1, false) == null) {
		setPropValue("MainTitleFontSize",getDefaultPropValue("MainTitleFontSize",-1,-1), -1, -1);
	}
	if (getLayeredPropValue("MainTitleFontStyle", -1, -1, false) == null){
		setPropValue("MainTitleFontStyle",getDefaultPropValue("MainTitleFontStyle",-1,-1), -1,-1);
	}
	if (getLayeredPropValue("MainTitleString", -1, -1, false) == null) {
		setPropValue("MainTitleString",getDefaultPropValue("MainTitleString",-1,-1), -1, -1);
	}

	// OutputFile

	if (getLayeredPropValue("OutputFile", -1, -1, false) == null) {
		setPropValue("OutputFile",getDefaultPropValue("OutputFile",-1,-1), -1, -1);
	}

	// PeriodEnd, PeriodStart - set at run time

	// ProductID
	if (getLayeredPropValue("ProductID", -1, -1, false) == null) {
		setPropValue("ProductID",getDefaultPropValue("ProductID", -1, -1), -1, -1);
	}

	// ProductName
	if (getLayeredPropValue("ProductName", -1, -1, false) == null) {
		setPropValue("ProductName",getDefaultPropValue("ProductName", -1, -1), -1, -1);
	}

	// ProductType.

	if (getLayeredPropValue("ProductType", -1, -1, false) == null) {
		setPropValue("ProductType",getDefaultPropValue("ProductType",-1,-1), -1, -1);
	}

	// Product Subtitle properties...

	if (getLayeredPropValue("SubTitleFontName", -1, -1, false) == null) {
		setPropValue("SubTitleFontName",getDefaultPropValue("SubTitleFontName",-1,-1),-1,-1);
	}
	if (getLayeredPropValue("SubTitleFontSize", -1, -1, false) == null) {
		setPropValue("SubTitleFontSize",getDefaultPropValue("SubTitleFontSize",-1,-1),-1,-1);
	}
	if (getLayeredPropValue("SubTitleFontStyle", -1, -1, false) == null) {
		setPropValue("SubTitleFontStyle",getDefaultPropValue("SubTitleFontStyle",-1,-1),-1,-1);
	}
	if (getLayeredPropValue("SubTitleString", -1, -1, false) == null) {
		setPropValue("SubTitleString",getDefaultPropValue("SubTitleString",-1,-1), -1, -1);
	}

	// Product dimensions...

	if (getLayeredPropValue("TotalHeight", -1, -1, false) == null) {
		setPropValue("TotalHeight",getDefaultPropValue("TotalHeight",-1,-1),-1,-1);
	}
	if (getLayeredPropValue("TotalWidth", -1, -1, false) == null) {
		setPropValue("TotalWidth",getDefaultPropValue("TotalWidth",-1,-1),-1,-1);
	}

	// Layout ...

	if (getLayeredPropValue("LayoutType", -1, -1, false) == null) {
		setPropValue("LayoutType", getDefaultPropValue("LayoutType",-1, -1), -1, -1);
	}
	if (getLayeredPropValue("LayoutNumberOfRows", -1, -1, false) == null) {
		setPropValue("LayoutNumberOfRows",getDefaultPropValue("LayoutNumberOfRows", -1, -1),-1, -1);
	}
	if (getLayeredPropValue("LayoutNumberOfCols", -1, -1, false) == null) {
		setPropValue("LayoutNumberOfCols",getDefaultPropValue("LayoutNumberOfCols", -1, -1),-1, -1);
	}

	//---------------------------------------------------------------------
	// Subproduct properties.
	//---------------------------------------------------------------------

	prop_val = getLayeredPropValue("ProductType", -1, -1, false);
	if ( prop_val.equalsIgnoreCase("Graph" ) ) {
		checkGraphProperties ( nsubs );
	}

	__proplist.setHowSet ( how_set_prev );
}

/**
Check the graph product type (as opposed to report properties) properties.
@param nsubs Number of subproducts.
*/
public void checkGraphProperties ( int nsubs )
{	String routine = "TSProduct.checkGraphProperties";
	int nts = 0;
	TSGraphType graphType = TSGraphType.LINE;
	__num_zoom_groups = 1;
	String prop_val;
	int how_set_prev = __proplist.getHowSet();
	__proplist.setHowSet ( Prop.SET_AS_RUNTIME_DEFAULT );
	for ( int isub = 0; isub < nsubs; isub++ ) {
		//Message.printStatus ( 1, "", "Checking subproduct [" + isub + "]" );
		// Check "GraphType" property because its value is used below to make decisions

		prop_val = getLayeredPropValue ( "GraphType", isub, -1, false );
		if ( prop_val == null ) {
			prop_val = getDefaultPropValue ( "GraphType", isub, -1);
			setPropValue ( "GraphType", prop_val, isub, -1);
		}
		graphType = TSGraphType.valueOfIgnoreCase(prop_val);

		// Now alphabetize the properties...

        if ( (graphType == TSGraphType.BAR) && getLayeredPropValue("BarOverlap",isub, -1, false ) == null ) {
            setPropValue ( "BarOverlap", getDefaultPropValue("BarOverlap",isub,-1), isub, -1 );
        }

		if ( (graphType == TSGraphType.BAR) && getLayeredPropValue("BarPosition",isub, -1, false ) == null ) {
			setPropValue ( "BarPosition", getDefaultPropValue("BarPosition",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue( "BottomXAxisLabelFontName", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisLabelFontName",
			getDefaultPropValue("BottomXAxisLabelFontName",isub,-1),
			isub, -1 );
		}
		if ( getLayeredPropValue( "BottomXAxisLabelFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisLabelFontStyle",
			getDefaultPropValue("BottomXAxisLabelFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "BottomXAxisLabelFontSize", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisLabelFontSize", getDefaultPropValue("BottomXAxisLabelFontSize",
			isub,-1), isub, -1 );
		}

		// BottomXAxisTitleString is checked in
		// TSGraphJComponent.checkTSProductGraphs()

		// "BottomXAxisMajorGridColor"...

		if ( getLayeredPropValue("BottomXAxisMajorGridColor", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.PERIOD ) {
				// Don't usually draw the horizontal grid...
				setPropValue ( "BottomXAxisMajorGridColor", "None",isub,-1);
			}
			else {
			    setPropValue ( "BottomXAxisMajorGridColor",
				getDefaultPropValue("BottomXAxisMajorGridColor", isub,-1), isub, -1 );
			}
		}

		// "BottomXAxisMinorGridColor"...

		if ( getLayeredPropValue("BottomXAxisMinorGridColor", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.PERIOD ) {
				// Don't usually draw the horizontal grid...
				setPropValue ( "BottomXAxisMinorGridColor","None",isub,-1);
			}
			else {
			    setPropValue ( "BottomXAxisMinorGridColor",
				getDefaultPropValue("BottomXAxisMinorGridColor", isub,-1), isub, -1 );
			}
		}

		if ( getLayeredPropValue( "BottomXAxisTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisTitleFontName",
			getDefaultPropValue("BottomXAxisTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "BottomXAxisTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisTitleFontStyle",
			getDefaultPropValue("BottomXAxisTitleFontStyle", isub,-1),isub, -1 );
		}
		if ( getLayeredPropValue( "BottomXAxisTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "BottomXAxisTitleFontSize",
			getDefaultPropValue("BottomXAxisTitleFontSize",isub,-1), isub, -1 );
		}

		// "DataLabelFontName" property...

		if ( getLayeredPropValue("DataLabelFontName", isub, -1, false ) == null ) {
			setPropValue ( "DataLabelFontName",
			getDefaultPropValue("DataLabelFontName",isub,-1),isub, -1);
		}

		// "DataLabelFontSize" property...

		if ( getLayeredPropValue("DataLabelFontSize", isub, -1, false ) == null ) {
			setPropValue ( "DataLabelFontSize",
			getDefaultPropValue("DataLabelFontSize",isub,-1),isub, -1);
		}

		// "DataLabelFontStyle" property...

		if ( getLayeredPropValue("DataLabelFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "DataLabelFontStyle",
			getDefaultPropValue("DataLabelFontStyle",isub,-1),isub, -1);
		}

		// "DataLabelFormat" property...

		if ( getLayeredPropValue("DataLabelFormat", isub, -1, false ) == null ) {
			setPropValue ( "DataLabelFormat", getDefaultPropValue("DataLabelFormat",isub,-1),isub,-1);
		}

		// "DataLabelPosition" property...

		if ( getLayeredPropValue("DataLabelPosition", isub, -1, false ) == null ) {
			setPropValue ( "DataLabelPosition", getDefaultPropValue("DataLabelPosition",isub,-1), isub,-1);
		}

		// "Enabled" property...

		if ( getLayeredPropValue("Enabled", isub, -1, false ) == null ){
			setPropValue ( "Enabled",
			getDefaultPropValue("Enabled",isub,-1), isub, -1);
		}

		// GraphHeight, GraphWidth calculated
		
        if ( getLayeredPropValue("LeftYAxisDirection", isub, -1, false ) == null ) {
            setPropValue ( "LeftYAxisDirection", getDefaultPropValue("LeftYAxisDirection",isub,-1), isub, -1 );
        }

		// LeftYAxisIgnoreUnits calculated

		if ( getLayeredPropValue("LeftYAxisLabelFontName", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisLabelFontName",
			getDefaultPropValue("LeftYAxisLabelFontName",isub,-1),
			isub, -1 );
		}
		if ( getLayeredPropValue("LeftYAxisLabelFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisLabelFontStyle",
			getDefaultPropValue("LeftYAxisLabelFontStyle",isub,-1),
			isub, -1 );
		}
		if ( getLayeredPropValue("LeftYAxisLabelFontSize", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisLabelFontSize",
			getDefaultPropValue("LeftYAxisLabelFontSize",isub,-1),
			isub, -1 );
		}

		// "LeftYAxisLabelPrecision" determined at run-time

		// "LeftYAxisMajorGridColor"...

		if ( getLayeredPropValue("LeftYAxisMajorGridColor", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.PERIOD ) {
				// Don't usually draw the horizontal grid...
				setPropValue ( "LeftYAxisMajorGridColor", "None",isub,-1);
			}
			else {
			    setPropValue ( "LeftYAxisMajorGridColor",
				getDefaultPropValue("LeftYAxisMajorGridColor", isub,-1), isub, -1 );
			}
		}

		if ( getLayeredPropValue("LeftYAxisMax", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisMax", getDefaultPropValue("LeftYAxisMax",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue("LeftYAxisMin", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisMin", getDefaultPropValue("LeftYAxisMin",isub,-1), isub, -1 );
		}

		// "LeftYAxisMinorGridColor"...

		if ( getLayeredPropValue("LeftYAxisMinorGridColor", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.PERIOD ) {
				// Don't usually draw the horizontal grid...
				setPropValue ( "LeftYAxisMinorGridColor", "None",isub,-1);
			}
			else {
			    setPropValue ( "LeftYAxisMinorGridColor",
				getDefaultPropValue("LeftYAxisMinorGridColor", isub,-1), isub, -1 );
			}
		}

		if ( getLayeredPropValue("LeftYAxisTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisTitleFontName",
			getDefaultPropValue("LeftYAxisTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("LeftYAxisTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisTitleFontStyle",
			getDefaultPropValue("LeftYAxisTitleFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("LeftYAxisTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisTitleFontSize",
			getDefaultPropValue("LeftYAxisTitleFontSize",isub,-1), isub, -1 );
		}

		// Check LeftYAxisTitleString in in
		// TSGraphJComponent.checkTSProductGraphs().

		// Left Y axis type...

		if ( getLayeredPropValue("LeftYAxisType", isub, -1, false ) == null ) {
			setPropValue ( "LeftYAxisType", getDefaultPropValue("LeftYAxisType",isub,-1), isub, -1 );
		}

		// LeftYAxisUnits determined at run time

		if ( getLayeredPropValue("LegendFontName", isub, -1, false ) == null ) {
			setPropValue ( "LegendFontName", getDefaultPropValue("LegendFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("LegendFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "LegendFontStyle", getDefaultPropValue("LegendFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("LegendFontSize", isub, -1, false ) == null ) {
			setPropValue ( "LegendFontSize", getDefaultPropValue("LegendFontSize",isub,-1),isub, -1);
		}

		if ( getLayeredPropValue("LegendFormat", isub, -1, false ) == null ) {
			setPropValue ( "LegendFormat", getDefaultPropValue("LegendFormat",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue("LegendPosition", isub, -1, false ) == null ) {
			setPropValue ( "LegendPosition", getDefaultPropValue("LegendPosition",isub,-1),isub, -1);
		}

		if ( getLayeredPropValue("MainTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "MainTitleFontName", getDefaultPropValue("MainTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("MainTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "MainTitleFontStyle", getDefaultPropValue("MainTitleFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("MainTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "MainTitleFontSize", getDefaultPropValue("MainTitleFontSize",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue("MainTitleString", isub, -1, false ) == null ) {
			// Assign a default based on the graph type...
			if ( graphType == TSGraphType.DURATION ) {
				setPropValue ( "MainTitleString", "Duration Curve", isub, -1);
			}
			else if ( graphType == TSGraphType.PERIOD ) {
				setPropValue ( "MainTitleString", "Period of Record", isub, -1);
			}
			else if ( graphType == TSGraphType.XY_SCATTER ) {
				setPropValue ( "MainTitleString", "XY-Scatter Plot", isub, -1);
			}
			// No general title.  If needed, pass in from the application...
			else {
			    setPropValue ( "MainTitleString", "", isub, -1);
			}
		}

		// PeriodEnd, PeriodStart determined at run time.

		if ( getLayeredPropValue("RightYAxisLabelFontName", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisLabelFontName",
			getDefaultPropValue("RightYAxisLabelFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "RightYAxisLabelFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisLabelFontStyle",
			getDefaultPropValue("RightYAxisLabelFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("RightYAxisLabelFontSize", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisLabelFontSize",
			getDefaultPropValue("RightYAxisLabelFontSize",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue("RightYAxisTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisTitleFontName",
			getDefaultPropValue("RightYAxisTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "RightYAxisTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisTitleFontStyle",
			getDefaultPropValue("RightYAxisTitleFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("RightYAxisTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisTitleFontSize",
			getDefaultPropValue("RightYAxisTitleFontSize",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("RightYAxisTitleString", isub, -1, false ) == null ) {
			setPropValue ( "RightYAxisTitleString", "", isub, -1 );
		}

		if ( getLayeredPropValue("SubTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "SubTitleFontName",
			getDefaultPropValue("SubTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("SubTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "SubTitleFontStyle",
			getDefaultPropValue("SubTitleFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue("SubTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "SubTitleFontSize",
			getDefaultPropValue("SubTitleFontSize",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue("SubTitleString", isub, -1, false ) == null ) {
			setPropValue ( "SubTitleString",
			getDefaultPropValue("SubTitleString",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue( "TopXAxisLabelFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisLabelFontStyle",
			getDefaultPropValue("TopXAxisLabelFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "TopXAxisLabelFontSize", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisLabelFontSize",
			getDefaultPropValue("TopXAxisLabelFontSize",isub,-1), isub, -1 );
		}

		if ( getLayeredPropValue( "TopXAxisTitleFontName", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisTitleFontName",
			getDefaultPropValue("TopXAxisTitleFontName",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "TopXAxisTitleFontStyle", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisTitleFontStyle",
			getDefaultPropValue("TopXAxisTitleFontStyle",isub,-1), isub, -1 );
		}
		if ( getLayeredPropValue( "TopXAxisTitleFontSize", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisTitleFontSize",
			getDefaultPropValue("TopXAxisTitleFontSize",isub,-1), isub, -1 );
		}

		// TopXAxisTitleString is checked in
		// TSGraphJComponent.checkTSProductGraphs()

		if ( getLayeredPropValue( "TopXAxisLabelFontName", isub, -1, false ) == null ) {
			setPropValue ( "TopXAxisLabelFontName",
			getDefaultPropValue("TopXAxisLabelFontName",isub,-1), isub, -1 );
		}

		// Zoom features...
		if ( getLayeredPropValue("ZoomEnabled", isub, -1, false ) == null ) {
			setPropValue ( "ZoomEnabled",
			getDefaultPropValue("ZoomEnabled",isub,-1), isub, -1 );
		}
		if ( (graphType == TSGraphType.XY_SCATTER) || (graphType == TSGraphType.DURATION) ||
		    (graphType == TSGraphType.RASTER)) {
			// For now disable zooming on these graph types...
			setPropValue ( "ZoomEnabled", "False", isub, -1 );
		}

		if (getLayeredPropValue("AnnotationProvider", isub, -1, false) == null) {
			setPropValue("AnnotationProvider", getDefaultPropValue("AnnotationProvider", isub, -1), isub, -1);
		}

		if (getLayeredPropValue("LayoutYPercent", isub, -1, false) == null) {
			setPropValue("LayoutYPercent", getDefaultPropValue("LayoutYPercent", isub, -1), isub, -1);
		}
		
		prop_val = getLayeredPropValue("ZoomGroup", isub, -1, false );
		if ( prop_val == null ) {
			prop_val = getDefaultPropValue("ZoomGroup",isub,-1);
			setPropValue ( "ZoomGroup", prop_val, isub, -1 );
		}
		try {
		    __num_zoom_groups = MathUtil.max ( __num_zoom_groups, StringUtil.atoi(prop_val) );
		}
		catch ( Exception e ) {
			// Should not happen.
		}

		// Analysis for XY-Scatter...

		if (graphType == TSGraphType.XY_SCATTER
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			if ( getLayeredPropValue( "XYScatterAnalyzeForFilling", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterAnalyzeForFilling",
				     getDefaultPropValue("XYScatterAnalyzeForFilling",isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterDependentAnalysisPeriodEnd", isub, -1, false ) == null ) {
				setPropValue ("XYScatterDependentAnalysisPeriodEnd",
				    getDefaultPropValue("XYScatterDependentAnalysisPeriodEnd",isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterDependentAnalysisPeriodStart", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterDependentAnalysisPeriodStart",
				    getDefaultPropValue("XYScatterDependentAnalysisPeriodStart", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue( "XYScatterFillPeriodEnd", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterFillPeriodEnd",
				    getDefaultPropValue( "XYScatterFillPeriodEnd", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue( "XYScatterFillPeriodStart", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterFillPeriodStart",
				    getDefaultPropValue( "XYScatterFillPeriodStart", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue( "XYScatterIndependentAnalysisPeriodEnd", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterIndependentAnalysisPeriodEnd",
				    getDefaultPropValue( "XYScatterIndependentAnalysisPeriodEnd", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue( "XYScatterIndependentAnalysisPeriodStart", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterIndependentAnalysisPeriodStart",
				    getDefaultPropValue( "XYScatterIndependentAnalysisPeriodStart", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterIntercept", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterIntercept", getDefaultPropValue("XYScatterIntercept", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterMethod", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterMethod", getDefaultPropValue("XYScatterMethod",isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterMonth", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterMonth", getDefaultPropValue("XYScatterMonth",isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue( "XYScatterNumberOfEquations", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterNumberOfEquations",
				    getDefaultPropValue( "XYScatterNumberOfEquations", isub,-1), isub, -1 );
			}
			if ( getLayeredPropValue("XYScatterTransformation", isub, -1, false ) == null ) {
				setPropValue ( "XYScatterTransformation",
				    getDefaultPropValue("XYScatterTransformation", isub,-1), isub, -1 );
			}
		}

		//-------------------------------------------------------------
		// Time series within a subproduct.
		//-------------------------------------------------------------

		nts = getNumData ( isub );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, "Checking properties for subproduct " + (isub + 1) + " graphs (" + nts + ")" );
		}
		for ( int its = 0; its < nts; its++ ) {
			checkDataProperties(isub, its);
		}

		int nann = getNumAnnotations(isub);
		for (int iann = 0; iann < nann; iann++) {
			checkAnnotationProperties(isub, iann);
		}
	}

	__proplist.setHowSet ( how_set_prev );
}

/**
Expand a parameter value to recognize processor-level properties.  For example, a parameter value like
"${WorkingDir}/morepath" will be expanded to include the working directory.
@param processor the CommandProcessor that has a list of named properties.
@param command the command that is being processed (may be used later for context sensitive values).
@param propertyValue the parameter value being expanded.
*/
public String expandPropertyValue( String propertyValue )
{   String routine = "TSCommandProcessorUtil.expandParameterValue";
    if ( (propertyValue == null) || (propertyValue.length() == 0) ) {
        // Just return what was provided.
        return propertyValue;
    }
    // Else see if the parameter value can be expanded to replace $ symbolic references with other values
    // Search the parameter string for $ until all processor parameters have been resolved
    int searchPos = 0;  // Position in the "parameter_val" string to search for $ references
    int foundPos;       // Position when leading ${ is found
    int foundPosEnd;   // Position when ending } is found
    String foundProp = null;    // Whether a property is found that matches the $ symbol
    String delimStart = "${";
    String delimEnd = "}";
    while ( searchPos < propertyValue.length() ) {
        foundPos = propertyValue.indexOf(delimStart, searchPos);
        foundPosEnd = propertyValue.indexOf(delimEnd, (searchPos + delimStart.length()));
        if ( (foundPos < 0) && (foundPosEnd < 0)  ) {
            // No more $ property names, so return what we have.
            return propertyValue;
        }
        // Else found the delimiter so continue with the replacement
        Message.printStatus ( 2, routine, "Found " + delimStart + " at position [" + foundPos + "]");
        // Get the name of the property
        foundProp = propertyValue.substring((foundPos+2),foundPosEnd);
        // Try to get the property from the processor
        // TODO SAM 2007-12-23 Evaluate whether to skip null.  For now show null in result.
        Object propval = null;
        String propvalString = null;
        try {
            //propval = processor.getPropContents ( foundProp );
            // Look up known properties.  For now define them here also
            // FIXME SAM 2009-01-15 Figure out a more generic way to do this
            // Also need to standarize these, add to the documentation, and to the properties editor
            PropList props = new PropList("props");
            DateTime now = new DateTime(DateTime.DATE_CURRENT);
            props.set( "CurrentToYear",now.toString(DateTime.FORMAT_YYYY));
            props.set( "CurrentToMonth",now.toString(DateTime.FORMAT_YYYY_MM));
            props.set( "CurrentToDay",now.toString(DateTime.FORMAT_YYYY_MM_DD));
            props.set( "CurrentToHour",now.toString(DateTime.FORMAT_YYYY_MM_DD_HH));
            props.set( "CurrentToMinute",now.toString(DateTime.FORMAT_YYYY_MM_DD_HH_mm));
            props.set( "CurrentToSecond",now.toString(DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS));
            propval = props.getValue(foundProp);
            if ( propval != null ) {
                propvalString = "" + propval;
            }
        }
        catch ( Exception e ) {
            // Keep the original value
            propvalString = delimStart + propval + delimEnd;
        }
        StringBuffer b = new StringBuffer();
        // Append the start of the string
        if ( foundPos > 0 ) {
            b.append ( propertyValue.substring(0,foundPos) );
        }
        // Now append the value of the property...
        b.append ( propvalString );
        // Now append the end of the original string if anything is at the end...
        if ( propertyValue.length() > (foundPosEnd + 1) ) {
            b.append ( propertyValue.substring(foundPosEnd + 1) );
        }
        // Now reset the search position to finish evaluating whether to expand the string.
        propertyValue = b.toString();
        searchPos = foundPos + propvalString.length();   // Expanded so no need to consider delim*
        Message.printStatus( 2, routine, "Expanded property value is \"" + propertyValue +
                "\" searchpos is now " + searchPos + " in string \"" + propertyValue + "\"" );
    }
    return propertyValue;
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	__override_proplist = null;
	__proplist = null;
	__tslist = null;
	super.finalize();
}

/**
Returns all the properties in the TSProduct (from both the regular and the
override proplist) as a sorted Vector.
@return a sorted Vector of all the properties in the TSProduct.
*/
public List getAllProps() {
	List v = new Vector();
	
	int size = __proplist.size();
	for (int i = 0; i < size; i++) {
		v.add(__proplist.elementAt(i));
	}

	if (__override_proplist != null) {
		size = __override_proplist.size();
		for (int i = 0; i < size; i++) {
			v.add(__override_proplist.elementAt(i));
		}
	}

	java.util.Collections.sort(v);	
	return v;
}

/**
Return the default value for a property.  This can be used to internally
assign properties.  Currently the defaults are hard-coded.  At some point code
may be added to get the defaults from a database, etc.
@param param Property to get value for.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will not be checked.
@return value of property or null if not found.
*/
public String getDefaultPropValue ( String param, int subproduct, int its ) {
	return getDefaultPropValue(param, subproduct, its, false);
}

/**
Return the default value for a property.  This can be used to internally
assign properties.  Currently the defaults are hard-coded.  At some point code
may be added to get the defaults from a database, etc.
@param param Property to get value for.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will
not be checked.  This is also used for annotations -- see the isAnnotation property.
@param isAnnotation is true, then its will be treated as the number of an 
annotation under the given subproduct, rather than a time series under the given subproduct.
@return value of property or null if not found.
*/
public String getDefaultPropValue ( String param, int subproduct, int its, boolean isAnnotation) {
	return getDefaultPropValue(param, subproduct, its, isAnnotation, null);
}

/**
Return the default value for a property.  This can be used to internally
assign properties.  Currently the defaults are hard-coded.  At some point code
may be added to get the defaults from a database, etc.
@param param Property to get value for.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will
not be checked.  This is also used for annotations -- see the isAnnotation property.
@param isAnnotation is true, then its will be treated as the number of an 
annotation under the given subproduct, rather than a time series under the given subproduct.
@param graphType the kind of graph for which the default prop is being 
returned.  Certain properties have different default values for certain kinds
of graphs.  If null, then the value will be ignored.
@return value of property or null if not found.
*/
public String getDefaultPropValue ( String param, int subproduct, int its,
boolean isAnnotation, TSGraphType graphType) {
	//
	//
	// Annotation properties
	//
	if (isAnnotation) {
		if (param.equalsIgnoreCase("AnnotationID")) {
			return "Annotation " + (its + 1);
		}
		else if (param.equalsIgnoreCase("Color")) {
			return "Black";
		}
		else if (param.equalsIgnoreCase("FontName")) {
			return "Arial";
		}
		else if (param.equalsIgnoreCase("FontSize")) {
			return "10";
		}
		else if (param.equalsIgnoreCase("FontStyle")) {
			return "Plain";
		}
		else if (param.equalsIgnoreCase("LineStyle")) {
			return "Solid";
		}
		else if (param.equalsIgnoreCase("LineWidth")) {
			return "1";
		}
		else if (param.equalsIgnoreCase("Order")) {
			return "OnTopOfData";
		}
		else if (param.equalsIgnoreCase("Point")) {
			return "0,0";
		}
		else if (param.equalsIgnoreCase("Points")) {
			return "0,0,1,1,";
		}
		else if (param.equalsIgnoreCase("ShapeType")) {
			return "Text";
		}
		else if (param.equalsIgnoreCase("XInputFormat")) {
			return "None";
		}		
		else if (param.equalsIgnoreCase("Text")) {
			return "";
		}
		else if (param.equalsIgnoreCase("TextPosition")) {
			return "Right";
		}	
		else if (param.equalsIgnoreCase("XAxisSystem")) {
			return "Data";
		}
		else if (param.equalsIgnoreCase("YAxisSystem")) {
			return "Data";
		}
		else if (param.equalsIgnoreCase("SymbolSize")) {
			return "0";
		}
		else if (param.equalsIgnoreCase("SymbolStyle")) {
			return "None";
		}
		else if (param.equalsIgnoreCase("SymbolPosition")) {
			return "Center";
		}
		return null;
	}

	if ( subproduct < 0 ) {
		// Product property...
        if ( param.equalsIgnoreCase("CurrentDateTime") ) {
            return "None";
        }
        else if ( param.equalsIgnoreCase("CurrentDateTimeColor") ) {
			return "Green";
		}
		else if ( param.equalsIgnoreCase("Enabled") ) {
			return "True";
		}
		else if (param.equalsIgnoreCase("LayoutType")) {
			return "Grid";
		}
		else if (param.equalsIgnoreCase("LayoutNumberOfCols")) {
			return "1";
		}
		else if (param.equalsIgnoreCase("LayoutNumberOfRows")) {
			return "" + getNumSubProducts();
		}
		else if ( param.equalsIgnoreCase("MainTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("MainTitleFontStyle") ) {
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("MainTitleFontSize") ) {
			return "20";
		}
		else if ( param.equalsIgnoreCase("MainTitleString") ) {
			return "";
		}
		else if ( param.equalsIgnoreCase("OutputFile") ) {
			if ( IOUtil.isUNIXMachine() ) {
				// TODO SAM
				return "/tmp/tmp.jpg";
			}
			else {
			    return "C:\\temp\\tmp.jpg";
			}
		}
		// "PeriodEnd" set at run-time
		// "PeriodStart" set at run-time
		else if ( param.equalsIgnoreCase("PreviewOutput") ) {
			return "False";
		}
		else if ( param.equalsIgnoreCase("ProductID")) {
			return "Product1";
		}
		else if ( param.equalsIgnoreCase("ProductName")) {
			return "";
		}
		else if ( param.equalsIgnoreCase("ProductType") ) {
			return "Graph";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontSize") ) {
			return "10";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontStyle") ) {
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("SubTitleString") ) {
			return "";
		}
		else if ( param.equalsIgnoreCase("TotalHeight") ) {
			return "400";
		}
		else if ( param.equalsIgnoreCase("TotalWidth") ) {
			return "400";
		}
	}

	//
	// Subproduct properties
	//

	else if ( (subproduct >= 0) && (its < 0) ) {
		// Subproduct property...
        if ( param.equalsIgnoreCase("BarOverlap") ) {
            return "False";
        }
        else if ( param.equalsIgnoreCase("BarPosition") ) {
			return "CenteredOnDate";
		}
		else if (param.equalsIgnoreCase("AnnotationProvider")) {
			return "";
		}		
		else if ( param.equalsIgnoreCase("BottomXAxisLabelFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisLabelFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisLabelFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisMajorGridColor") ){
			return "None";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisMinorGridColor") ){
			return "None";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisTitleFontSize") ){
			return "12";
		}
		else if ( param.equalsIgnoreCase("BottomXAxisTitleString") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("DataLabelFontName") ){
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("DataLabelFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("DataLabelFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("DataLabelFormat") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("DataLabelPosition") ){
			return "Right";
		}
		else if ( param.equalsIgnoreCase("Enabled") ) {
			return "True";
		}
		// "GraphHeight" calculated?
		else if ( param.equalsIgnoreCase("GraphType") ) {
			return "Line";
		}
		// "GraphWidth" calculated?
		else if (param.equalsIgnoreCase("LayoutYPercent")) {
			return "";
		}
        else if ( param.equalsIgnoreCase("LeftYAxisDirection") ) {
            return "Normal";
        }
		else if ( param.equalsIgnoreCase("LeftYAxisIgnoreUnits") ) {
			return "False";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisLabelFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisLabelFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisLabelFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisMajorGridColor") ){
			return "lightgray";
		}
		// "LeftYAxisLabelPrecision" determined at run-time
		else if ( param.equalsIgnoreCase("LeftYAxisMax") ) {
			return "Auto";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisMin") ) {
			return "Auto";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisMinorGridColor") ){
			return "None";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisTitleFontSize") ){
			return "12";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisTitleString") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("LeftYAxisType") ){
			return "Linear";
		}
		// "LeftYAxisUnits" determined at run-time
		else if ( param.equalsIgnoreCase("LegendFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("LegendFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("LegendFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("LegendFormat") ){
			return "Auto";
		}
		else if ( param.equalsIgnoreCase("LegendPosition") ){
			return "Bottom";
		}
		else if ( param.equalsIgnoreCase("MainTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("MainTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("MainTitleFontSize") ){
			return "20";
		}
		else if ( param.equalsIgnoreCase("MainTitleString") ){
			return "";
		}
		// "PeriodStart", "PeriodEnd" set at runtime
		else if ( param.equalsIgnoreCase("RightYAxisLabelFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("RightYAxisLabelFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("RightYAxisLabelFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("RightYAxisTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("RightYAxisTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("RightYAxisTitleFontSize") ){
			return "12";
		}
		else if ( param.equalsIgnoreCase("RightYAxisTitleString") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("SubTitleFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("SubTitleString") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("TopXAxisLabelFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("TopXAxisLabelFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("TopXAxisLabelFontSize") ){
			return "10";
		}
		else if ( param.equalsIgnoreCase("TopXAxisTitleFontName") ) {
			return "Arial";
		}
		else if ( param.equalsIgnoreCase("TopXAxisTitleFontStyle") ){
			return "Plain";
		}
		else if ( param.equalsIgnoreCase("TopXAxisTitleFontSize") ){
			return "12";
		}
		else if ( param.equalsIgnoreCase("TopXAxisTitleString") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterAnalyzeForFilling")){
			return "false";
		}
		else if ( param.equalsIgnoreCase(
			"XYScatterDependentAnalysisPeriodEnd")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterDependentAnalysisPeriodStart")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterFillPeriodEnd")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterFillPeriodStart")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterIndependentAnalysisPeriodEnd")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterIndependentAnalysisPeriodStart")){
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterIntercept") ) {
			return "";	// No intercept - calculate it
		}
		else if ( param.equalsIgnoreCase("XYScatterMethod") ) {
			return "OLSRegression";
		}
		else if ( param.equalsIgnoreCase("XYScatterMonth") ) {
			return "";
		}
		else if ( param.equalsIgnoreCase("XYScatterNumberOfEquations")){
			return "OneEquation";
		}
		else if ( param.equalsIgnoreCase("XYScatterTransformation") ) {
			return "None";
		}
		else if ( param.equalsIgnoreCase("ZoomEnabled") ){
			return "True";
		}
		else if ( param.equalsIgnoreCase("ZoomGroup") ){
			return "1";
		}
	}

	//
	// Data properties
	//

	else if ( (subproduct >= 0) && (its >= 0) ) {
		// Data property...
		if ( param.equalsIgnoreCase("Color") ){
			return "black";
		}
		else if ( param.equalsIgnoreCase("DataLabelFormat") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("DataLabelPosition") ){
			return "Right";
		}
		else if ( param.equalsIgnoreCase("Enabled") ){
			return "True";
		}
        else if ( param.equalsIgnoreCase("FlaggedDataSymbolStyle") ){
            // Same as "SymbolStyle"
            return getDefaultPropValue ( "SymbolStyle", subproduct, its, isAnnotation, graphType );
        }
		else if ( param.equalsIgnoreCase("GraphType") ){
			return "Line";
		}
		else if ( param.equalsIgnoreCase("LegendFormat") ){
			return "Auto";
		}
		else if ( param.equalsIgnoreCase("LineStyle") ){
			if (graphType == TSGraphType.XY_SCATTER) {
				return "None";
			}
			else if (graphType == TSGraphType.POINT) {
				return "None";
			}
			else {
				return "Solid";
			}
		}
		else if ( param.equalsIgnoreCase("LineWidth") ){
			if (graphType == TSGraphType.XY_SCATTER) {
				return "0";
			}
			else if (graphType == TSGraphType.POINT) {
				return "0";
			}
			else {
				return "1";
			}
		}
		// PeriodEnd, PeriodStart set at runtime
		// This should only be called for XY-Scatter plots...
		else if ( param.equalsIgnoreCase("RegressionLineEnabled") ){
			return "true";
		}
		else if ( param.equalsIgnoreCase("SymbolSize") ){
			if (graphType == TSGraphType.XY_SCATTER) {
				return "4";
			}
			else if (graphType == TSGraphType.POINT) {
				return "5";
			}
			else {
				return "0";
			}
		}
		else if ( param.equalsIgnoreCase("SymbolStyle") ){
			if (graphType == TSGraphType.XY_SCATTER) {
				return "Diamond-filled";
			}
			else if (graphType == TSGraphType.POINT) {
				return "Circle-filled";
			}
			else {
				return "None";
			}
		}
		else if ( param.equalsIgnoreCase("TSAlias") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("TSID") ){
			return "";
		}
		else if ( param.equalsIgnoreCase("XAxis") ){
			return "Bottom";
		}
		else if (param.equalsIgnoreCase("XYScatterConfidenceInterval")){
			return "";
		}
		else if ( param.equalsIgnoreCase("YAxis") ){
			return "Left";
		}
	}
	return null;
}

/**
Return the property value for a requested property.  This first searches the
override properties and then the main properties.  The main properties are
searched in layered fashion, starting with the product, then the sub-product,
and then the time series.  The last value specified will be used (but will
always be override if an override property is specified.
@param property Property to get value for.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will not be checked.
@return value of property or null if not found.
TODO SAM make sure that override properties can contain annotations.  Problem: how to
dynamically add annotations without conflict.
*/
public String getLayeredPropValue ( String property, int subproduct, int its )
{	return getLayeredPropValue ( property, subproduct, its, true );
}

/**
Return the property value for a requested property.  This first searches the
override properties and then the main properties.  The main properties are
searched in layered fashion, starting with the product, then the sub-product,
and then the time series.  The last value specified will be used (but will
always be overridden if an override property is specified).
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will not be checked.  
@param property Property to get value for.
@param allowLayeredProps If true, properties are allowed to be layered, with
determination of the property starting with the most general scope, through the most specific scope.
If false, only properties at the requested level of the finest detail are used (no
layering).  An example of a property that may occur in several layers is "Enabled".
@return value of property or null if not found.
*/
public String getLayeredPropValue (	String property, int subproduct, int its, boolean allowLayeredProps ) {
	return getLayeredPropValue(property, subproduct, its, allowLayeredProps, false);
}

/**
Return the property value for a requested property.  This first searches the
override properties and then the main properties.  The main properties are
searched in layered fashion, starting with the product, then the sub-product,
and then the time series.  The last value specified will be used (but will
always be overridden if an override property is specified).
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked.
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will
not be checked.  This is also used for specifying annotations.  See the
isAnnotation parameter for more info.
@param property Property to get value for.
@param allowLayeredProps If true, properties are allowed to be layered, with
the most general scope property applying to the most specific if not overridden.
If false, only properties at the level of the finest detail are used (no
layering).  An example of a property that may occur in several layers is "Enabled".
@param isAnnotation if true, then its will be treated as the number of an
annotation under the given subproduct, rather than the number of a time series
under the given subproduct.
@return value of property or null if not found.
*/
public String getLayeredPropValue (	String property, int subproduct,
	int its, boolean allowLayeredProps, boolean isAnnotation )	
{	String value = null;
	String value2 = null;
	//Message.printStatus ( 2, "", "Looking up \"" + property + "\" " + subproduct + " " + its );
	// First search the override properties...
	if ( __override_proplist != null ) {
		value = __override_proplist.getValue ( property );
		if ( value != null ) {
			return value;
		}
	}

	// Next search the main proplist...

	if ( allowLayeredProps ) {
		// Search to find the most specific property...
		if ( __proplist != null ) {
			// First search the generic property (not a strict
			// product file format, or pre-formatted to do what is done below)...
			value = __proplist.getValue ( property );
			// Next search the main product...
			if (isAnnotation) {
				value2 = null;
				// this is done because both a subproduct and
				// an annotation number must be specified to bring back an annotation.
			}
			else {
				value2 = __proplist.getValue( "Product." + property);
			}
			if ( value2 != null ) {
				value = value2;
			}
			// Now search the sub-product...
			if ( subproduct >= 0 ) {
				if (isAnnotation) {
					value2 = null;
					// this is done because both a 
					// subproduct and an annotation number 
					// must be specified to bring back an 
					// annotation.
				}
				else {
					value2 = __proplist.getValue("SubProduct " + (subproduct + 1) + "." + property);
				}
				
				if ( value2 != null ) {
					value = value2;
				}
			}
			// Now search the data or annotation item...
			if ( its >= 0 ) {
			// TODO SAM Math.abs() doesn't make sense, but it's been like that for years now.
				if (isAnnotation) {
					value2 = __proplist.getValue( "Annotation " + (Math.abs(subproduct) + 1)
						+ "." + (its + 1) + "." + property);
				}
				else {
					value2 = __proplist.getValue("Data " 
						+ (Math.abs(subproduct) + 1) + "." + (its + 1) + "." + property);
				}
				if ( value2 != null ) {
					value = value2;
				}
			}
		}
		return value;
	}
	else {
	    // The request is specifically for a certain level...
		if ( subproduct < 0 ) {
			// Product property...
			if (isAnnotation) {
				// this is done because both a subproduct and
				// an annotation number must be specified
				// to bring back an annotation.
				return null;
			}
			else {
				return __proplist.getValue("Product." + property);
			}
		}
		else if ((subproduct >= 0) && (its < 0)) {
			// Subproduct property...
			if (isAnnotation) {
				// this is done because both a subproduct and
				// an annotation number must be specified
				// to bring back an annotation.
				return null;
			}
			else {
				return __proplist.getValue("SubProduct " + (subproduct + 1) + "." + property);
			}
		}
		else if ( (subproduct >=0) && (its >= 0) ) {
			// Data or annotation property...
			if (isAnnotation) {
				return __proplist.getValue("Annotation " + (subproduct + 1) + "." + (its + 1)
					+ "." + property);
			}
			else {
				return __proplist.getValue("Data "
					+ (subproduct + 1) + "." + (its + 1) + "." + property);
			}
		}
	}
	// Requested combination is not found...
	return null;
}

/**
Returns the number of annotations for the given subproduct.  This is determined
by determining the number of consecutive "ShapeType" definitions starting 
from 0 that return a valid value.
@param subproduct the subproduct to check for annotations.
@return the number of annotations for the given subproduct.
*/
public int getNumAnnotations(int subproduct) {
	int ndata = -1;
	String prop_value = null;
	for (int i = 0; ; i++) {
		// Use false to make sure we are getting the specific property..
		prop_value = getLayeredPropValue("ShapeType", subproduct, i, false, true);
		if (prop_value != null) {
			ndata = i;
			continue;
		}
		
		if (ndata != i) {
			// Previous loop had the number we need.
			break;
		}
	}
	return (ndata + 1);
}

/**
Return the number of data items that are defined for a subproduct.  This is
determined by checking the properties for "Data S.N.XXXX", where S is the
subproduct number (minus 1, starting at 0) and XXXX is "TSID", "TS", and "TSAlias".
The largest N that returns a value is assumed to be the number of data sets.
@param subproduct The subproduct to check (zero or greater).
@return the number of data sets for a subproduct or zero if none are defined.
*/
public int getNumData ( int subproduct )
{	int ndata = -1;
	String prop_value = null;
	for ( int i = 0; ; i++ ) {
		// Use false to make sure we are getting the specific property..
		prop_value = getLayeredPropValue ("TSID", subproduct, i, false);
		if ( prop_value != null ) {
			ndata = i;
			continue;
		}
	    prop_value = getLayeredPropValue ("TSAlias", subproduct, i, false);
        if ( prop_value != null ) {
            ndata = i;
            continue;
        }
		prop_value = getLayeredPropValue ( "TS", subproduct, i, false );
		if ( prop_value != null ) {
			ndata = i;
			continue;
		}
		prop_value = getLayeredPropValue ("GraphType", subproduct, i, false);
		if ( prop_value != null ) {
			ndata = i;
			continue;
		}
		if ( ndata != i ) {
			// Previous loop had the number we need.
			break;
		}
	}
	return (ndata + 1);
}

/**
Return the total number of subproducts (enabled and disabled) that are defined for the product.
@return the number of subproducts or zero if none are defined.
*/
public int getNumSubProducts ()
{	return getNumSubProducts ( false );
}

/**
Return the number of enabled subproducts that are defined.  This is determined
by checking the properties for "Data N.1.XXXX", where XXXX is "TSID", "TSAlias", "TS",
"GraphType", "TemplateTSID", and "MainTitleString" .  The largest N
that returns a value is assumed to be the number of subproducts.
@return the number of subproducts or zero if none are defined.
@param enabled_only If true, only enabled subproducts are counted.
*/
public int getNumSubProducts ( boolean enabled_only )
{	int nsubs = -1;
	String prop_value = null;

    // TODO (JTS - 2005-05-06)
    // enabled_only isn't working properly -- it's checking for whether any 
    // TIME SERIES are enabled or not, and if not, marking the entire subproduct as not enabled.  
	
	int count = 0;
	// the following makes sure that data exists in the product.  If not,
	// then the data number is forced to -1
	int numData = -1;
	int data = 0;
	for ( int i = 0; ; i++ ) {
		numData = getNumData(i);
		if (numData == 0) {
			data = -1;
		}
		// Use false to make sure we are getting the specific property..
		prop_value = getLayeredPropValue ( "TSID", i, data, false );
		if ( prop_value != null ) {
			nsubs = i;
			if ( enabled_only ) {
				prop_value = getLayeredPropValue ( "Enabled", i, data, false );
				if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
					++count;
				}
			}
			else {
                ++count;
			}
			continue;
		}
	    prop_value = getLayeredPropValue ( "TSAlias", i, data, false );
        if ( prop_value != null ) {
            nsubs = i;
            if ( enabled_only ) {
                prop_value = getLayeredPropValue ( "Enabled", i, data, false );
                if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
                    ++count;
                }
            }
            else {
                ++count;
            }
            continue;
        }
		prop_value = getLayeredPropValue ( "TemplateTSID", i, data, false);
		if ( prop_value != null ) {
			nsubs = i;
			if ( enabled_only ) {
				prop_value = getLayeredPropValue ( "Enabled", i, data, false );
				if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
					++count;
				}
			}
			else {
                ++count;
			}
			continue;
		}
		prop_value = getLayeredPropValue ( "TS", i, data, false );
		if ( prop_value != null ) {
			nsubs = i;
			if ( enabled_only ) {
				prop_value = getLayeredPropValue ( "Enabled", i, data, false );
				if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
					++count;
				}
			}
			else {
                ++count;
			}
			continue;
		}
		prop_value = getLayeredPropValue ("GraphType", i, data, false);
		if ( prop_value != null ) {
			nsubs = i;
			if ( enabled_only ) {
				prop_value = getLayeredPropValue ( "Enabled", i, data, false );
				if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
					++count;
				}
			}
			else {
                ++count;
			}
			continue;
		}
		prop_value = getLayeredPropValue ( "MainTitleString", i, data, false);
		if ( prop_value != null ) {
			nsubs = i;
			if ( enabled_only ) {
				prop_value = getLayeredPropValue ( "Enabled", i, data, false );
				if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
					++count;
				}
			}
			else {
                ++count;
			}
			continue;
		}
		if ( nsubs != i ) {
			// Ran out of subproducts...
			break;
		}
	}
	return count;
}

/**
Return the number of zoom groups used with graphs.
*/
public int getNumZoomGroups ()
{	return __num_zoom_groups;
}

/**
Return the override PropList for the TSProduct.
@return the override PropList for the TSProduct.
*/
public PropList getOverridePropList ()
{	return __override_proplist;
}

/**
Return the property value for an override property.
@param property Property to get value for.
@return value of property or null if not found.
*/
public String getOverridePropValue ( String property )
{	if ( __override_proplist != null ) {
		return __override_proplist.getValue ( property );
	}
	return null;
}

/**
Return the PropList for the TSProduct.
@return the PropList for the TSProduct.
*/
public PropList getPropList ()
{	return __proplist;
}

/**
Returns how the properties are being set.
@return how the properties are being set.
*/
public int getPropsHowSet() {
	return __proplist.getHowSet();
}

/**
Return the property value for a requested property.  This first searches the
override properties and then the main properties.  The fully-expanded property
name is used.  Use getLayeredPropValue() to request a property using product, subproduct, etc.
@param property Property to get value for.
@return value of property or null if not found.
*/
public String getPropValue ( String property )
{	String value = null;
	if ( __override_proplist != null ) {
		value = __override_proplist.getValue ( property );
		if ( value != null ) {
			return value;
		}
	}
	if ( __proplist != null ) {
		value = __proplist.getValue ( property );
		return value;
	}
	return null;
}

/**
Return the list of time series associated with the TSProduct.
@return the list of time series associated with the TSProduct.
*/
public List<TS> getTSList ()
{	return __tslist;
}

/**
Returns true if the product has any time series in the internal ts list, false otherwise.
@return true if the product has any time series in the internal ts list, false otherwise.
*/
public boolean hasTimeSeries() {
	if (__tslist == null || __tslist.size() == 0) {
		return false;
	}
	else {
		return true;
	}
}

/**
Checks to see whether the TSProduct has been modified in any way.  First checks
whether the TSProduct has been set as dirty, and then if that's not true, loops
through all the properties to see if any have been set at runtime by the user
(properties set with SET_AT_RUNTIME_BY_USER).
@return true if anything has been changed in the TSProduct.
*/
public boolean isDirty() {
//	Message.printStatus(1, "", "isDirty: " + __dirty);
	if (__dirty) {
		return true;
	}

	Prop p = null;
	int size = __proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__proplist.elementAt(i);
//		Message.printStatus(1, "", " (" + p.getHowSet() + ") "
//			+ p.getKey());
		if (p.getHowSet() == Prop.SET_AT_RUNTIME_BY_USER) {
			return true;
		}
	}
	
	if (__override_proplist == null) {
		return false;
	}
	
	size = __override_proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__override_proplist.elementAt(i);
//		Message.printStatus(1, "", " (" + p.getHowSet() + ") "
//			+ p.getKey());
		if (p.getHowSet() == Prop.SET_AT_RUNTIME_BY_USER) {
			return true;
		}	
	}
//	Message.printStatus(1, "", "-- not dirty --");
	return false;
}

/**
Called after a TSProduct is saved to a database or file, it marks all the
props that are SET_AT_RUNTIME_BY_USER to be SET_FROM_PERSISTENT.
*/
protected void propsSaved() {
	Prop p = null;
	int size = __proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__proplist.elementAt(i);
		if (p.getHowSet() == Prop.SET_AT_RUNTIME_BY_USER) {
			p.setHowSet(Prop.SET_FROM_PERSISTENT);
		}
	}
	
	if (__override_proplist == null) {
		return;
	}
	
	size = __override_proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__override_proplist.elementAt(i);
		if (p.getHowSet() == Prop.SET_AT_RUNTIME_BY_USER) {
			p.setHowSet(Prop.SET_FROM_PERSISTENT);
		}	
	}
}

/**
Removes an annotation from a subproduct.  If the annotation is not the 
last one, the other annotation numbers will be renumbered to be 
consecutive (excluding the one to be deleted).
@param sp the number of the subproduct (0-based) in which the annotation is found.
@param iann the number of the annotation (0-based) to remove.
*/
protected void removeAnnotation(int sp, int iann) {
	int num = getNumAnnotations(sp);
//Message.printStatus(1, "", "Remove annotation (" + sp + ", " + iann + ")");
//Message.printStatus(1, "", "Num Ann: " + num);
	// First swap this annotation to the end of the annotation list before 
	// deleting so all the other annotation props are numbered correctly.
	for (int i = iann + 1; i < num; i++) {
//Message.printStatus(1, "", "Swapping " + (i - 1) + " with " + i);
		swapAnnotations(sp, (i - 1), sp, i);
	}

	__dirty = true;

	iann = num;
	Prop p = null;
	String key = "";
	int indexSpace = -1;
	int indexDot1 = -1;
	int indexDot2 = -1;
	String num1 = "";
	String num2 = "";
	String rest = "";
	String delsp = "" + (sp + 1);
	String dsp = "" + iann;

	// next, go through the properties and unset the appropriate annotations
//Message.printStatus(1, "", "Size: " + __proplist.size());
	for (int i = 0; i < __proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();
		if (StringUtil.startsWithIgnoreCase(key, "Annotation ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			if (indexSpace > -1 && indexDot1 > -1) {
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
//Message.printStatus(1, "", "" + key + " (" + num1 + " / " + delsp + ") ("
//	+ num2 + " / " + dsp + ") [" + indexDot2 + "]");
				if (num1.equals(delsp) && indexDot2 > -1
					&& num2.equals(dsp)) {
//Message.printStatus(1, "", "Unset: " + key);
					__proplist.unSet(key);
					i--;
				}
			}
		}
	}

	// check the override proplist and delete from there, too
	if (__override_proplist == null) {
		return;
	}
	for (int i = 0; i < __override_proplist.size(); i++) {
		p = (Prop)__override_proplist.elementAt(i);
		key = p.getKey();
		if (StringUtil.startsWithIgnoreCase(key, "Annotation ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			if (indexSpace > -1 && indexDot1 > -1) {
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
				if (num1.equals(delsp) && indexDot2 > -1 && num2.equals(dsp)) {
					__override_proplist.unSet(key);
					i--;
				}
			}
		}	
	}
}

/**
Removes a subproduct from the product.  No renumbering will be done for the
other subproducts -- this is handled (along with some other special work) in
the layout component already, which calls this method.  
@param sp the number of the subproduct (0-based) to remove.
*/
protected void removeSubProduct(int sp) {
	Prop p = null;
	String key = "";
	int indexSpace = -1;
	int indexDot = -1;
	String num = "";
	String del = "" + (sp + 1);
	__dirty = true;

	// remove the time series that are in this subproduct
	// FIXME SAM 2008-01-29 Evaluate how TSAlias is handled.
	int numData = getNumData(sp);
	String id = null;
	TS ts = null;
	for (int i = 0; i < numData; i++) {
		id = getPropValue("Data " + (sp + 1) + "." + (i + 1) + ".TSID");
		for (int j = 0; j < __tslist.size(); j++) {
			ts = (TS)__tslist.get(j);
			if (ts.getIdentifierString().equals(id)) {
				__tslist.remove(j);
				break;
			}
		}
	}

	// Loop through the main proplist and unset all the appropriate properties
	for (int i = 0; i < __proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();
		indexSpace = key.indexOf(" ");
		indexDot = key.indexOf(".");
		if (indexSpace > -1 && indexDot > -1) {
			num = key.substring(indexSpace + 1, indexDot);
			if (num.equals(del)) {
				__proplist.unSet(key);
				i--;
			}
		}
	}

	// check the override proplist and if it contains anything, remove
	// the properties from it as well
	if (__override_proplist == null) {
		return;
	}
	for (int i = 0; i < __override_proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();
		indexSpace = key.indexOf(" ");
		indexDot = key.indexOf(".");
		if (indexSpace > -1 && indexDot > -1) {
			num = key.substring(indexSpace + 1, indexDot);
			if (num.equals(del)) {
				__override_proplist.unSet(key);
				i--;
			}
		}
	}
}

/**
Renames annotation properties from one number to another.  In any property
named in the style 'Annotation X.Y.VALUE', this method replaces the X.Y 
section with the new values passed in as parameters newSub and newAnn, 
respectively.  For this reason, the parameters are not 0-based.  This is 
used to maintain proper ordered numbering schemes in the proplist when deleting annotations.
@param origSub the original subproduct number of the annotation properties
@param origAnn the original annotation number of the properties
@param newSub the new subproduct number of the annotation properties
@param newAnn the new annotation number of the properties
*/
protected void renameAnnotationProps(String origSub, String origAnn, String newSub, String newAnn)
{   Prop p = null;
	String key = "";
	int indexSpace = -1;
	int indexDot1 = -1;
	int indexDot2 = -1;
	String num1 = "";
	String num2 = "";
	String rest = "";
	List v = new Vector();
	__dirty = true;

	// loop through the proplist
	for (int i = 0; i < __proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();

		// only be concerned with properties starting with 'Annotation '
		if (StringUtil.startsWithIgnoreCase(key, "Annotation ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			
			if (indexSpace > -1 && indexDot1 > -1) {
				// Pull out the subproduct and annotation number from the property and compare to origSub
				// and origAnn
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
				if (num1.equals(origSub) && indexDot2 > -1 && num2.equals(origAnn)) {
					// If they match, replace them with the newSub and newAnn and put the
					// new property in the prop list
					__proplist.unSet(key);
					i--;
					p.setKey( key.substring(0, indexSpace) + " " + newSub + "." + newAnn
						+ rest.substring(indexDot2));
					v.add(p);
				}
			}
		}
	}
	for (int i = 0; i < v.size(); i++) {
		p = (Prop)v.get(i);
		if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
			__proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		}
		else {
			__proplist.setHowSet(p.getHowSet());
		}
		__proplist.set(p);
	}

	v = new Vector();
	if (__override_proplist == null) {
		return;
	}
	for (int i = 0; i < __override_proplist.size(); i++) {
		p = (Prop)__override_proplist.elementAt(i);
		key = p.getKey();
		if (StringUtil.startsWithIgnoreCase(key, "Annotation ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			if (indexSpace > -1 && indexDot1 > -1) {
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
				if (num1.equals(origSub) && indexDot2 > -1 && num2.equals(origAnn)) {
					__override_proplist.unSet(key);
					i--;
					p.setKey( key.substring(0, indexSpace) + " " + newSub + "." + newAnn
						+ rest.substring(indexDot2));
					v.add(p);
				}				
			}
		}	
	}
	for (int i = 0; i < v.size(); i++) {
		p = (Prop)v.get(i);
		if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
			__override_proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		}
		else {
			__override_proplist.setHowSet(p.getHowSet());
		}
		__override_proplist.set(p);
	}	
}

/**
Renames data properties from one number to another.  In any property
named in the style 'Data X.Y.VALUE', this method replaces the X.Y 
section with the new values passed in as parameters newSub and newData, 
respectively.  For this reason, the parameters are not 0-based.  This is 
used to maintain proper ordered numbering schemes in the proplist when deleting data properties.
@param origSub the original subproduct number of the data properties
@param origD the original data number of the properties
@param newSub the destination subproduct number of the data properties
@param newData the destination data number of the properties
*/
protected void renameDataProps(String origSub, String origD, String newSub, String newData)
{	Prop p = null;
	String key = "";
	int indexSpace = -1;
	int indexDot1 = -1;
	int indexDot2 = -1;
	String num1 = "";
	String num2 = "";
	String rest = "";
	List v = new Vector();
	__dirty = true;

	// loop through the proplist
	for (int i = 0; i < __proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();

		// only be concerned with data properties
		if (StringUtil.startsWithIgnoreCase(key, "Data ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			if (indexSpace > -1 && indexDot1 > -1) {
				// pull out the subproduct and data number
				// from the property and compare to origSub and origData
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
				if (num1.equals(origSub) && indexDot2 > -1 && num2.equals(origD)) {
					// if they match, replace them with the newSub and newAnn and put the
					// new property in the prop list
					__proplist.unSet(key);
					i--;
					p.setKey(key.substring(0, indexSpace) + " " + newSub + "." + newData
						+ rest.substring(indexDot2));
					v.add(p);
				}
			}
		}
	}
	for (int i = 0; i < v.size(); i++) {
		p = (Prop)v.get(i);
		if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
			__proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		}
		else {
			__proplist.setHowSet(p.getHowSet());
		}
		__proplist.set(p);
	}

	v = new Vector();
	if (__override_proplist == null) {
		return;
	}
	for (int i = 0; i < __override_proplist.size(); i++) {
		p = (Prop)__override_proplist.elementAt(i);
		key = p.getKey();
		if (StringUtil.startsWithIgnoreCase(key, "Data ")) {
			indexSpace = key.indexOf(" ");
			indexDot1 = key.indexOf(".");		
			if (indexSpace > -1 && indexDot1 > -1) {
				num1 = key.substring(indexSpace + 1, indexDot1);
				rest = key.substring(indexDot1 + 1);
				indexDot2 = rest.indexOf(".");
				num2 = rest.substring(0, indexDot2);
				if (num1.equals(origSub) && indexDot2 > -1 && num2.equals(origD)) {
					__override_proplist.unSet(key);
					i--;
					p.setKey( key.substring(0, indexSpace) + " " + newSub + "." + newData
						+ rest.substring(indexDot2));
					v.add(p);
				}				
			}
		}	
	}
	for (int i = 0; i < v.size(); i++) {
		p = (Prop)v.get(i);
		if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
			__override_proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		}
		else {
			__override_proplist.setHowSet(p.getHowSet());
		}
		__override_proplist.set(p);
	}	
}

/**
Renames properties from one number to another.  In any property
named in the style 'Name X.Y.VALUE', this method replaces the X 
section with the new value passed in as parameter newSub.
For this reason, the parameters are not 0-based.  This is 
used to maintain proper ordered numbering schemes in the proplist when deleting subproduct properties.
@param origSub the original number of the properties
@param destSub the destination number of the properties
*/
private void renameSubProductProps(String origSub, String destSub) {
	Prop p = null;
	String key = "";
	int indexSpace = -1;
	int indexDot = -1;
	String num = "";
	// loop through the proplist
	for (int i = 0; i < __proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();
		indexSpace = key.indexOf(" ");
		indexDot = key.indexOf(".");
		// only be concerned with data properties
		if (indexSpace > -1 && indexDot > -1) {
			num = key.substring(indexSpace + 1, indexDot);
			// pull out the subproduct from the property and compare to origSub
			if (num.equals(origSub)) {
				// if they match, replace it with the newSub and put the new property in the prop list
				if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
					__proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
				}
				else {
					__proplist.setHowSet(p.getHowSet());
				}			
				p.setKey(key.substring(0, indexSpace) + " " + destSub + key.substring(indexDot));
			}
		}
	}
	if (__override_proplist == null) {
		return;
	}
	for (int i = 0; i < __override_proplist.size(); i++) {
		p = (Prop)__proplist.elementAt(i);
		key = p.getKey();
		indexSpace = key.indexOf(" ");
		indexDot = key.indexOf(".");
		if (indexSpace > -1 && indexDot > -1) {
			num = key.substring(indexSpace + 1, indexDot);
			if (num.equals(origSub)) {
				if (p.getHowSet() == Prop.SET_FROM_PERSISTENT) {
					__override_proplist.setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
				}
				else {
					__override_proplist.setHowSet(p.getHowSet());
				}			
				p.setKey(key.substring(0, indexSpace) + " " + destSub + key.substring(indexDot));
			}
		}
	}
}

/**
Allows the entire TSProduct to set be dirty (as opposed to individual props).
This is used when subproducts are moved, removed, or added to the product.
@param dirty whether the TSProduct has been changed in a major way that's not
detectable at a prop level (true) or not.
*/
protected void setDirty(boolean dirty) {
	__dirty = dirty;
}

/**
Set a property in the override PropList.  This is typically used for properties
associated with a TSProduct that are supplemental to describing the TSProduct or
are set dynamically.  For example, a graphing display may show a reference
graph.  This information is controlled by the GUI and not the TSProduct (which
may be read from a file).  The TSProduct may indicate which time series to use
in the reference graph but the TSProduct itself will be used for both the full and reference graphs.
@param property Override property to set.
@param value Value of override property.
*/
public void setOverridePropValue ( String property, String value )
{	if ( __override_proplist == null ) {
		// Create a new list...
		__override_proplist = new PropList ( "Override" );
	}
	__override_proplist.set ( property, value );
}

/**
For every property in the TSProduct, changes its setHowSet value to the specified value.
@param how the value to set every Prop's setHowSet value to.
TODO (2005-11-01) change name to 'changeAllPropsHowSet'
@deprecated
*/
public void setPropsHowSet(int how) {
	int size = __proplist.size();
	for (int i = 0; i < size; i++) {
		((Prop)__proplist.elementAt(i)).setHowSet(how);
	}

	if (__override_proplist == null) {
		return;
	} 
	size = __override_proplist.size();
	for (int i = 0; i < __override_proplist.size(); i++) {
		((Prop)__override_proplist.elementAt(i)).setHowSet(how);
	}
	
}

/**
Specifies the HOW_SET for new properties that are set in the product.
@param how the value to set new Prop's setHowSet value to.
TODO (JTS - 2005-11-01) once the other method with the same name is corrected, remove the dummy
parameter in this method.
*/
public void setPropsHowSet(int how, boolean dummy) {
	__proplist.setHowSet(how);
	if (__override_proplist == null) {
		return;
	} 
	__override_proplist.setHowSet(how);
}

/**
Set a property value.  This method sets the property specifically at
the level that is specified (it does not consider layers of properties).  The
calling code should specifically set properties at the proper level.  The
property is set in the main PropList (not the override PropList).
@param property Property to set.  Use only the property name (e.g., "MyProp").
The leading property, sub-property, or data prefixes will be added based on the other parameter values.
@param value String value of the property.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked (indicating a full product property).
@param its Time series number within a sub-product (starting at zero).  A
prefix of "Data X.Y." will be used for the property, where X is
(subproduct) and Y is (its).  If negative, the data item property will
not be checked (indicating a sub-product or product property).
*/
public void setPropValue (	String property, String value, int subproduct, int its ) {
	setPropValue(property, value, subproduct, its, false);
}

/**
Set a property value.  This method sets the property specifically at
the level that is specified (it does not consider layers of properties).  The
calling code should specifically set properties at the proper level.  The
property is set in the main PropList (not the override PropList).
@param property Property to set.  Use only the property name (e.g., "MyProp").
The leading property, sub-property, or data prefixes will be added based on the other parameter values.
@param value String value of the property.
@param subproduct Sub-product number (starting at zero).  A prefix of
"SubProduct X." will be used for the property, where X is (subproduct.
If negative, the sub-product property will not be checked (indicating a full product property).
@param its Time series or annotation number within a sub-product (starting at 
zero).  A prefix of "Data X.Y." or "Annotation X.Y." will be used for the 
property, where X is (subproduct) and Y is (its).  If negative, the data item 
property will not be checked (indicating a sub-product or product property).  
See isAnnotation for more information.
@param isAnnotation is true, then its is treated as the number of an annotation
under the given subproduct, rather than the number of a time series under the given subproduct.
*/
public void setPropValue ( String property, String value, int subproduct, int its, boolean isAnnotation )
{
	if (isAnnotation) {
		if (subproduct < 0 || its < 0) {
			Message.printWarning(2, "setPropValue", "Negative value"
				+ " in call to setPropValue for annotation property.  (SubProduct: " 
				+ subproduct + "  Annotation: " + its + ").  Nothing will be set.");
			return;
		}
		__proplist.set("Annotation " + (subproduct + 1) + "." + (its + 1) + "." + property, value);
		return;
	}
			
	// Make these if statements explicit so it is easy to understand...
	if ( subproduct < 0 ) {
		// Product property...
		__proplist.set ( "Product." + property, value );
	}
	else if ( (subproduct >= 0) && (its < 0) ) {
		// Subproduct property...
		__proplist.set ( "SubProduct " + (subproduct + 1) + "." + property, value );
	}
	else if ( (subproduct >=0) && (its >= 0) ) {
		// Data property...
		__proplist.set ( "Data " + (subproduct + 1) + "." + (its + 1) + "." + property, value );
	}
}

/**
Set the list of time series associated with the TSProduct.  If any 
annotation providers have been added with addTSProductAnnotationProvider(),
the annotations from those providers will be set on the graph at this point.
@param tslist list of TS associated with the TSProduct.
*/
public void setTSList ( List<TS> tslist )
{	__tslist = tslist;

	if (__annotationProviders != null) {
		addAnnotations();
	}
}

/**
Dumps all the properties in the TSProduct to the specified status level.
This is only used for troubleshooting.
@param statusLevel the status level at which to print the properties.
*/
public void showProps(int statusLevel) {
	int size = __proplist.size();
	Message.printStatus(statusLevel, "", "--------------------------------------");
	for (int i = 0; i < size; i++) {
		Message.printStatus(statusLevel, "", "" + i + ": " + __proplist.elementAt(i));
	}
	
	if (__override_proplist == null) {
		return;
	}
	size = __override_proplist.size();
	Message.printStatus(statusLevel, "", "--------------------------------------");
	for (int i = 0; i < size; i++) {
		Message.printStatus(statusLevel, "", "" + i + ": " + __override_proplist.elementAt(i));
	}
}

/**
Dumps all the properties in the TSProduct where the property name begins
with the specified string to the specified status level.
This is only used for troubleshooting.
@param statusLevel the status level at which to print the properties.
*/
public void showPropsStartingWith(int statusLevel, String start) {
	Prop p = null;
	Message.printStatus(statusLevel, "", "--------------------------------------");
	int size = __proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__proplist.elementAt(i);
		if (StringUtil.startsWithIgnoreCase(p.getKey(), start)) {
			Message.printStatus(statusLevel, "", "" + p);
		}
	}
	if (__override_proplist == null) {
		return;
	}
	Message.printStatus(statusLevel, "", "--------------------------------------");
	size = __override_proplist.size();
	for (int i = 0; i < size; i++) {
		p = (Prop)__override_proplist.elementAt(i);
		if (StringUtil.startsWithIgnoreCase(p.getKey(), start)) {
			Message.printStatus(statusLevel, "", "" + p);
		}		
	}
}

/**
Sorts the properties in the proplist.  Used for troubleshooting in order 
to quickly locate a property when printing it out with the showProps() methods.
*/
protected void sortProps() {
	__proplist.sortList();
	if (__override_proplist == null) {
		return;
	}
	__override_proplist.sortList();
}

/**
This method should be called before hidden properties are to be added to the 
product.  Hidden properties are never shown to a user and are never saved to a file.  
*/
public void startAddingHiddenProps() {
	__howSet = __proplist.getHowSet();
	__proplist.setHowSet(Prop.SET_HIDDEN);
}

/**
This method should be called after hidden properties are done being added to the
product.  Hidden properties are never shown to a user and are never saved to
a file.  Any future properties added to the TSProduct will be added with the 
same HowSet value that the internal PropList was using prior to the call to startAddingHiddenProps().
*/
public void stopAddingHiddenProps() {
	__proplist.setHowSet(__howSet);
	__howSet = -1;
}

/**
Swaps the data section with the given original subproduct and data number with
the one with the given new subproduct and data number.
For instance, if swapping data 1.1 and 3.2, all subproducts and subproperties 
that were numbered 1.1 will now be numbered 3.2 and vice versa.  
@param origSub the original subproduct (base 0)
@param origAnn the original annotation (base 0)
@param newSub the destination subproduct (base 0)
@param newAnn the destination annotation (base 0)
*/
protected void swapAnnotations(int origSub, int origAnn, int newSub, int newAnn) {
	renameAnnotationProps("" + (origSub + 1), "" + (origAnn + 1), "TEMP", "TEMP");
	renameAnnotationProps("" + (newSub + 1), "" + (newAnn + 1), "" + (origSub + 1), "" + (origAnn + 1));
	renameAnnotationProps("TEMP", "TEMP", "" + (newSub + 1), "" + (newAnn + 1));
	__dirty = true;
}

/**
Swaps subproducts with the 'origSub' number and those with the 'newSub' number. 
For instance, if swapping subproducts 1 and 3, all subproducts and 
subproperties that were numbered 1 will now be numbered 3 and vice versa.
*/
protected void swapSubProducts(int origSub, int newSub) {
	renameSubProductProps("" + (origSub + 1), "TEMP");
	renameSubProductProps("" + (newSub + 1), "" + (origSub + 1));
	renameSubProductProps("TEMP", "" + (newSub + 1));
	__dirty = true;
}

/**
Transfer the properties into objects that can be used by other code more
efficiently.  For now don't do anything until we explore the concept of just
getting everything out of the PropList.
*/
private void transferPropList ()
{
}

/**
Unsets a property in the product with the given key.
@param key the key of the property to unset.
*/
protected void unSet(String key) {
	__proplist.unSet(key);
}

/**
Write the TSProduct as a file.  If the file exists, it will be replaced with
the new contents and comments will not be transferred.
@param filename Name of file to save.
@param save_all If true, all properties will be saved, even those that have been
assigned internally at run-time.  If false, only the properties read from a
persistent source and modified by the user during the run will be saved.  The
former is useful to see the full list of properties, the latter to save the
minimum amount of information.
@exception if there is an error writing the file.
*/
public void writeFile ( String filename, boolean save_all )
throws Exception
{	__proplist.setPersistentName ( filename );
	// This writes everything in unsorted order...
	//__proplist.writePersistent ();

	PrintWriter out = new PrintWriter(new FileOutputStream (filename ));

	// First write the main product properties, then subproduct, and within
	// each subproduct the data properties.  Use the prefix notation and
	// shave the prefix off each property as it is written...

	List<Prop> v = __proplist.getPropsMatchingRegExp ( "Product.*" );
	Prop prop = null;
	int how_set = 0;
	out.println ( "[Product]" );
	out.println ( "" );
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}

	boolean save = false;
	
	/*
	NOTE REGARDING THE LOGIC FOR DETERMINING WHEN TO SAVE:
	The code was originally rather densely packed, particularly in 
	regard to the right-side of the screen, and was becoming confusing
	as to in which conditions saving of a property actually occurred.
	The code was expanded to be much more verbose and clearer, at 
	the expense of some performance.  The result should be acceptable
	as saving is not a common activity.

	Further, note that it was also expanded to be very clear as to the
	logical intentions.  Instead of writing:
	
		...
		else if (save_all) {
			...
		}
		else {
			...
		}
	
	The following was done:
	
		...
		else if (save_all) {
			...
		}
		else if (!save_all) {
			...
		}
	
	Even though (!save_all) is the logical opposite of (save_all), it might
	be mis-read, and so this "redundant" bit of logic is included.

	Finally, the "save" boolean is another redundancy.  If anything should
	not be saved, the loop in which it is located is 'continue'ed.  
	"save = true;" is used as a way of marking the locations where logic
	dictates that saving a property is finally determined to be legal.
	*/
	
	for (int i = 0; i < size; i++) {
		save = false;
		prop = v.get(i);
		how_set = prop.getHowSet();
		
		if (how_set == Prop.SET_HIDDEN) {
			// these are never saved
			continue;
		}
		else if (save_all) {
			save = true;
		}
		else if (!save_all) {
			if (how_set == Prop.SET_FROM_PERSISTENT) {
				save = true;
			}
			else if (how_set == Prop.SET_AT_RUNTIME_BY_USER) {
				save = true;
			}
			else if (how_set == Prop.SET_AT_RUNTIME_FOR_USER) {
				save = true;
			}
		}

		if (save) {
			out.println(prop.getKey().substring(8) + " = \"" + prop.getValue() + "\"" );
		}
	}

	// Loop through the subproducts...

	int nsubs = getNumSubProducts();
	List<Prop> vdata = null;
	int dsize = 0;
	String sub_prefix;
	int sub_prefix_length = 0;
	String data_prefix;
	int data_prefix_length = 0;

	String type = null;
	String key = null;
	
	for (int isub = 0; isub < nsubs; isub++) {
		v = __proplist.getPropsMatchingRegExp("SubProduct " + (isub + 1) + ".*");
		sub_prefix = "[SubProduct " + (isub + 1) + "]";
		sub_prefix_length = sub_prefix.length();
		out.println ( "" );
		out.println ( sub_prefix );
		out.println ( "" );
		
		size = 0;
		if ( v != null ) {
			size = v.size();
		}
		
		for ( int i = 0; i < size; i++ ) {
			save = false;
			prop = v.get(i);
			key = prop.getKey();
			how_set = prop.getHowSet();

			if (how_set == Prop.SET_HIDDEN) {
				continue;
			}
			else if (save_all) {
				save = true;
			}
			else if (!save_all) {
				if (how_set == Prop.SET_FROM_PERSISTENT || how_set == Prop.SET_AT_RUNTIME_BY_USER
				    || how_set == Prop.SET_AT_RUNTIME_FOR_USER){
				    // ok
				}
				else {
					// not ok
					continue;
				}

				if (key.toUpperCase().endsWith("PRODUCTIDORIGINAL")) {
					continue;
				}
				else if (key.toUpperCase().endsWith("ORIGINALGRAPHTYPE")) {
				    continue;
				}

				save = true;
			}

			if (save) {
				out.println(prop.getKey().substring(sub_prefix_length - 1) + " = \"" + prop.getValue() + "\"");
			}
		}

		// Now write the data properties...
		int ndata = getNumData(isub);
		for ( int idata = 0; idata < ndata; idata++ ) {
			vdata = __proplist.getPropsMatchingRegExp ("Data " + (isub + 1) + "." + (idata + 1) +".*");
			data_prefix = "[Data " + (isub + 1) + "." + (idata + 1) + "]";
			data_prefix_length = data_prefix.length();
			out.println ( "" );
			out.println ( data_prefix );
			out.println ( "" );
			dsize = 0;
			
			if ( vdata != null ) {
				dsize = vdata.size();
			}
			
			for ( int j = 0; j < dsize; j++ ) {
				save = false;
				prop = vdata.get(j);
				how_set = prop.getHowSet();
				key = prop.getKey().substring(data_prefix_length - 1);

				if (how_set == Prop.SET_HIDDEN) {
					continue;
				}
				else if (save_all) {
					save = true;
				}
				else if (!save_all) {
					if (how_set == Prop.SET_FROM_PERSISTENT
					    || how_set == Prop.SET_AT_RUNTIME_BY_USER
					    || how_set ==Prop.SET_AT_RUNTIME_FOR_USER){
						// ok
					}
					else {
						// not ok
						continue;
					}
					
					if (key.toUpperCase().endsWith("PRODUCTIDORIGINAL")) {
					     continue;
					}

					save = true;
				}

				if (save) {
					out.println(prop.getKey().substring(
						data_prefix_length - 1) + " = \"" + prop.getValue() + "\"");
				}
			}
		}

		// now write the annotations
		int nann = getNumAnnotations(isub);
		for (int iann = 0; iann < nann; iann++) {
			vdata = __proplist.getPropsMatchingRegExp("Annotation " + (isub + 1) + "." + (iann + 1) + ".*");
			type = getPropValue("Annotation " + (isub + 1) + "." + (iann + 1) + ".ShapeType");
			data_prefix = "[Annotation " + (isub + 1) + "." + (iann + 1) + "]";
			data_prefix_length = data_prefix.length();
			out.println("");
			out.println(data_prefix);
			out.println("");
			dsize = 0;
			if (vdata != null) {
				dsize = vdata.size();
			}
			for (int j = 0; j < dsize; j++) {
        		save = false;
        		prop = vdata.get(j);
        		how_set = prop.getHowSet();
        	
        		if (how_set == Prop.SET_HIDDEN) {
        			continue;
        		}
        		else if (save_all) {
        			save = true;
        		}
        		else if (!save_all) {
        			if (how_set == Prop.SET_FROM_PERSISTENT
    			    	|| how_set == Prop.SET_AT_RUNTIME_BY_USER
    			    	|| how_set ==Prop.SET_AT_RUNTIME_FOR_USER) {
        				// ok
        			}
        			else {
        				// not ok
        				continue;
        			}
        
        			key = prop.getKey().substring(data_prefix_length - 1);
        
        			if (type.equalsIgnoreCase("Text")) {
        				if (key.equalsIgnoreCase("Points")
        				    || key.equalsIgnoreCase("FlaggedDataSymbolStyle")
        					|| key.equalsIgnoreCase("LineStyle")
        					|| key.equalsIgnoreCase("LineWidth")
        					|| key.equalsIgnoreCase("SymbolStyle")
        					|| key.equalsIgnoreCase("SymbolSize")
        					|| key.equalsIgnoreCase("SymbolPosition")) {
        					continue;
        				}
        				else {
        					save = true;
        				}
        			}
        			else if (type.equalsIgnoreCase("Line")) {
        				if ( key.equalsIgnoreCase("FlaggedDataSymbolStyle")
        				    || key.equalsIgnoreCase("FontSize")
        					|| key.equalsIgnoreCase("FontStyle")
        					|| key.equalsIgnoreCase("FontName")
        					|| key.equalsIgnoreCase("Point")
        					|| key.equalsIgnoreCase("Text")
        					|| key.equalsIgnoreCase("TextPosition")
        					|| key.equalsIgnoreCase("SymbolStyle")
        					|| key.equalsIgnoreCase("SymbolSize")
        					|| key.equalsIgnoreCase("SymbolPosition")) {
        					continue;
        				}
        				else {
        					save = true;
        				}
        			}
        			else if (type.equalsIgnoreCase("Symbol")) {
        				if (key.equalsIgnoreCase("FontSize")
        					|| key.equalsIgnoreCase("FontStyle")
        					|| key.equalsIgnoreCase("FontName")
        					|| key.equalsIgnoreCase("Text")
        					|| key.equalsIgnoreCase("TextPosition")
        					|| key.equalsIgnoreCase("Points")
        					|| key.equalsIgnoreCase("LineStyle")
        					|| key.equalsIgnoreCase("LineWidth")) {
        					continue;
        				}
        				else {
        					save = true;
        				}
        			}
        		}
        
        		if (save) {		
        			out.println(prop.getKey().substring(data_prefix_length - 1) + " = \"" + prop.getValue() + "\"");
        		}
			}
		}
	}

	out.close();
}

}