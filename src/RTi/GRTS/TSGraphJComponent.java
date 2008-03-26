// ----------------------------------------------------------------------------
// TSGraphJComponent - JComponent for drawing one or more time series graphs
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 25 Jun 1999	Steven A. Malers	Initial version.
//		Riverside Technology,	Rely on GeoView and older
//		inc.			GRTS code.
// 23 May 2000	SAM, RTi		Fix labelling problems.  Enable zooming.
//					For mouse events, consume events.
// 11 Jul 2000	SAM, RTi		Fix so rubber-band line does not leave
//					residue on screen.  Remove consume to
//					see if that helps with rubber band
//					problem not seen in GeoView.
// 12 Oct 2000	SAM, RTi		Enable the ReferenceDate property, which
//					causes the graphs to be shifted to the
//					reference date (e.g., to plot traces).
//					This did not work well so don't use it!
//					Needs a lot more work!
// 26 Oct 2000	SAM, RTi		Add Scatter graph type.  Add check for
//					GraphType property.  Add getGraphType()
//					and setGraphType() to allow
//					communication with the other TSView
//					components.
// 30 Oct 2000	SAM, RTi		Add Duration graph type.
//					Add bar graph.
// 04 Apr 2001	SAM, RTi		Fix so units don't have to be exactly
//					the same, only consistent.
// 13 Apr 2001	SAM, RTi		Make so units don't have to be the same
//					for scatter plots.  Allow units to be
//					ignored and handle gracefully if they
//					are not.  Add precision to units based
//					on units file.
// 04 May 2001	SAM, RTi		Fix annoying problem where after resize
//					the data limits were not getting reset
//					and drawing overwrote the title and
//					legend.  Rework the drawing limits to
//					be more robust.  Fix problem where
//					resize to small horizontal dimension
//					threw exceptions determining labels.
//					Add YAxisPrecision property.
//					Add right-click on graph to find closest
//					time series.  Set the maximum legend
//					height based only on enabled time
//					series.
// 28 Aug 2001	SAM, RTi		Handle better when y-axis units are "".
//					Change GR.SYM* to GRSymbol.SYM*.
//					Handle irregular time series that have
//					date precision to hour.  To alleviate
//					problems with bar position confusion in
//					bar graphs, recognize the "BarPosition"
//					property and default to
//					"BarsCenteredOnDate".
// 13 Sep 2001	SAM, RTi		Take out the reference date logic.  It
//					is not being used and makes things a
//					mess.
// 23 Sep 2001	SAM, RTi		Another fix to bar charts.  The height
//					of the bar needs to consider the y-axis
//					position for the start of the bar.
// 2001-11-01	SAM, RTi		Enable the PeriodOfRecord graph type.
// 2001-11-05	SAM, RTi		Clean up javadoc.  Verify that variables
//					are set to null when no longer being
//					used.  Make more of the data private
//					since the reference graph is now handled
//					within this class (previously was a
//					derived class).
// 2001-11-20	SAM, RTi		TSDates for hourly data that have a time
//					zone specified now show the time zone
//					when toString() is called.  Therefore,
//					set the time zone to "" when iterating.
//					Fix bug where drawing a zoom box under
//					the box size tolerance causes the graph
//					to go white on resize, etc.
// 2002-01-08	SAM, RTi		Update to use new GR classes, which
//					support Swing and AWT.
// ===================================
// 2002-01-17	SAM, RTi		Change the name of the class from
//					TSGraph to TSGraphCanvas and use the
//					new GRTS class names that will allow
//					Swing versions of components to be
//					written.  Change so when printing if a
//					null print job comes back the graph is
//					just not printed (indicates cancel by
//					user).  Take out the mouse tracker
//					boolean since it has never been used.
//					Add _rubber_banding to base class.
// 2002-02-26	SAM, RTi		Update so that when creating a TSProduct
//					from a Vector of TS and a PropList the
//					time series are guaranteed to be
//					connected correctly, even if there are
//					redundant identifiers.
// 2002-04-25	SAM, RTi		Start adding support for "TSAlias"
//					in addition to TSID.
// 2002-06-06	SAM, RTi		Fix bug where units labels for XY
//					Scatter plot were reversed (due to
//					recent reordering of the axes).
// 2002-11-05	J. Thomas Sapienza, RTi	Use BufferedImage instead of createImage
//					when creating a Jpeg.  Wasn't working
//					with the other way on the Linux box.
// 2002-11-18	JTS, RTi		Add stub code for compiling in 1.1.8 and
//					1.4.0
// 2002-11-27	JTS, RTi		Added checkAndSetBounds() workaround to
//					get the graph working on Linux. Also
//					updated to check for the right version
//					String on HPUX.
// =====================================
// 2003-06-03	SAM, RTi		Copy GRGraphCanvas to GRGraphJComponent
//					and implement a full Swing version.
// 2003-06-05	JTS, RTi		Implemented swing printing code.
// 2003-06-18	SAM, RTi		* TotalWidth and TotalHeight properties
//					  were not getting transferred correctly
//					  from the PropList constructor.
//					* Use Swing setPreferredSize() on the
//					  graph.
//					* Remove checkAndSetBounds() since this
//					  code is designed to not be used with
//					  Java 1.1.8.
//					* Fix problem in
//					  createTSGraphsFromTSProduct() where
//					  the TSID input fields where not being
//					  handled correctly.
// 2003-09-07	SAM, RTi		* Product.TotalWidth and
//					  Product.TotalHeight properties were
//					  not being recognized.
// 2004-02-24	JTS, RTi		Added reinitializeGraphs().
// 2004-03-22	JTS, RTi		Changed reinitializeGraphs() so that
//					the limits for the drawing area and
//					graphs are set properly when it is
//					called.
// 2005-04-27	JTS, RTi		Corrected bug in lookupTSColor() that
//					was skipping every Xth color.
// 2004-05-10	SAM, RTi		Change so that when creating a TSProduct
//					from a PropList, the how set information
//					is SET_AT_RUNTIME_FOR_USER so that the
//					product is not marked as dirty.
// 2004-06-09	JTS, RTi		Added code to reinitializeGraphs() to
//					correctly determine and set the maximum
//					date extents.  As it was, if a time 
//					series with larger date extents than any
//					others in any of the graphs was added to
//					the graph, the max start and max end
//					dates were not being set right and
//					the graph could not be zoomed out past
//					the previous maximum date extents.
// 2005-04-27	JTS, RTi		Added all data members to finalize().
// 2005-04-29	JTS, RTi		Added lookupTSSymbol().
// 2006-09-28	SAM, RTi		A user-specified property for
//					LeftYAxisTitleString was always being
//					ignored - value was being determined
//					from time series.  Change so that a
//					specified property will be used.  This
//					was a little odd because a conditional
//					check for 1 == 1 was introduced in
//					code in the 2004-08-06 release.  Put a
//					comment in the code to check later -
//					perhaps there is a side-effect?
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.GRTS;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;

import java.awt.print.PageFormat;
import java.awt.print.Printable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import RTi.GR.GRAspect;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRShape;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.TS.TS;
import RTi.TS.TSUtil;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

// Needed to support writing SVG...
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;


/**
The TSGraphJComponent class provides a component for displaying one or more time
series graphs, each with one or more time series.  Methods are available for
initiating printing as well as handling zooming and selects.  Performance
options include double-buffering.  This class also implements
TSViewListener, which is typically used to allow a reference TSGraphJComponent
to be enabled that calls the listener methods in a main TSGraphJComponent.
Therefore, zoom control can occur in both TSGraphJComponent.  The
TSGraphJComponent controls all redrawing through the
standard paint() method.  Each graph is drawn within the TSGraph class.
The layout of the component is as follows:
<pre>
-----------------------------------------------------------------------------
|                             Full page (_da_page)                          |
| ------------------------------------------------------------------------- |
| |                            Main Title (_da_maintitle)                 | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                            Subtitle (_da_subtitle)                    | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                                                                       | |
| |  TSGraph 0 (for TSProduct subproduct 1) (_da_graphs)                  | |
| |                                                                       | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                                                                       | |
| |             TSGraph 1 (for TSProduct subproduct 1)                    | |
| |                                                                       | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| |                                                                       | |
| |             TSGraph 1 (for TSProduct subproduct 1)                    | |
| |                                                                       | |
| ------------------------------------------------------------------------- |
| ------------------------------------------------------------------------- |
| | Left Footer        ||       Center Footer       ||       Right Footer | |
| | (_da_leftfoot)     ||       (_da_centerfoot)    ||    (_da_rightfoot) | |
| ------------------------------------------------------------------------- |
-----------------------------------------------------------------------------
</pre>
The positioning of the graphs is by default top to bottom but features will be
added to allow row/cell positioning.
*/
public class TSGraphJComponent extends GRJComponentDevice
implements KeyListener, MouseListener, MouseMotionListener, Printable, TSViewListener
{
        
/**
Edit mode. Enables editing of points by clicking above or below a point
to change the point's y value. (X value editing is not supported)
*/
public static final int INTERACTION_EDIT = 3;
/**
Interaction modes.
No special interaction.
*/
public static final int INTERACTION_NONE = 0;

/**
Select a feature.  When enabled, a mouse click causes the select() method
of registered TSViewListeners to be called.
*/
public static final int INTERACTION_SELECT = 1;

/**
Zoom mode.  This enables a rubber-band line (if the extents are bigger than
5 pixels in both direction.  The zoom() method of registered TSViewListeners
are called.
*/
public static final int INTERACTION_ZOOM = 2;

/**
Use this to force the drawing limits to be reset.  Otherwise, the graphics is
not available for setting font-dependent area sizes.
*/
private boolean _first_paint = true;

/**
Vector of time series to plot.  Not sure if this needs to be saved here.
*/
private Vector _tslist = null;	

/**
Background color.
*/
private GRColor _background_color = GRColor.white;

private TSCursorDecorator _cursorDecorator;
/**
External image used when processing in batch mode.
*/
private BufferedImage _external_Image = null;

/**
Rubber band color for zooming.
*/
private GRColor _rubber_band_color = GRColor.red;

/**
Flag indicating whether the component is a reference canvas, in which case only
one graph will be shown.
*/
private boolean _is_reference_graph = false;

/**
The subproduct (graph) that should be used for reference graph information.
*/
private int _reference_sub = -1;

/**
Flag indicating whether the graph should be closed due to a user confirmation
(because of data problem, etc).
*/
private boolean _need_to_close = false;

/**
TSProduct that describes the time series product to create.  If an old-style
PropList is passed in to the constructor, a new TSProduct is created.  If a
TSProduct is passed in to the constructor, it is used.
*/
private TSProduct _tsproduct = null;

/**
Indicate whether Y-limits should be kept when zooming.  
If false, that means that users can change the min and max values
in the properties JFrame.
*/
private boolean _zoom_keep_y_limits = false;

/**
PropList for display properties that are not to be confused with the TSProduct
properties (or its override properties).  For example, this list of properties
is used to indicate whether the component is a reference graph.
*/
private PropList _display_props = null;

/**
Drawing areas used for the main component.  Only a few are necessary because
most of the drawing occurs in the TSGraph objects.  List in the order of the
layout top to bottom.
*/

private GRJComponentDrawingArea _da_page = null;// Drawing area for full canvas
private GRJComponentDrawingArea _da_maintitle =null;
						// Drawing area for main title
private GRJComponentDrawingArea _da_subtitle = null;
						// Drawing area for subtitle
private GRJComponentDrawingArea _da_graphs = null;
						// Drawing area for graphs
						// (actual drawing is done in
						// TSGraph) but this
						// drawing area may draw borders
						// or background color).
private GRJComponentDrawingArea _da_leftfoot = null;
						// Drawing area for left footer
private GRJComponentDrawingArea _da_centerfoot=null;
						//Drawing area for center footer
private GRJComponentDrawingArea _da_rightfoot =null;
						// Drawing area for right footer

/**
Data and drawing limits for the above drawing areas.
*/
private GRLimits _datalim_page = null;		// Full page drawing area limits
private GRLimits _drawlim_page = null;
private GRLimits _datalim_maintitle = null;	// Limits for main title drawing
private GRLimits _drawlim_maintitle = null;	// area
private GRLimits _datalim_subtitle = null;	// Limits for subtitle drawing
private GRLimits _drawlim_subtitle = null;	// area
private GRLimits _datalim_graphs = null;	// Limits for graphs drawing
private GRLimits _drawlim_graphs = null;	// area
private GRLimits _datalim_leftfoot = null;	// Limits for left footer
// TODO SAM 2007-05-09 Enable handling of the following
//private GRLimits _drawlim_leftfoot = null;	// drawing area
private GRLimits _datalim_centerfoot = null;	// Limits for center footer
//private GRLimits _drawlim_centerfoot = null;	// drawing area
private GRLimits _datalim_rightfoot = null;	// Limits for right footer
//private GRLimits _drawlim_rightfoot = null;	// drawing area

private Rectangle _bounds = null;		// Set with update.  Useful for
						// clearing the component.
private GRLimits _printBounds = null;		// Set with update.  Useful for
						// clearing the component.

private JFrame _parent = null;			// Parent JFrame

private Vector _tsgraphs = new Vector();	// Vector of TSGraph.
						// Allocate so don't have to
						// checked for null repeatedly.

// Dimensions for drawing areas...

private String _gtype = "Main:";		// Used for messages so we can
						// tell whether in a main or
						// reference graph.

private boolean _force_redraw = true;		// Force a redraw.

private TSViewListener [] _listeners = null;
						// Listeners that want to know
						// when the TSView changes.
private int _interaction_mode = INTERACTION_NONE;
						// Interaction mode.

private int _mouse_x1 = -1;			// Coordinates for events.
private int _mouse_y1 = -1;			// 1 = First location
private int _mouse_x2 = -1;			// 2 = Current location 
private int _mouse_y2 = -1;			//
private int _mouse_xprev = -1;			// Previous location
private int _mouse_yprev = -1;
private TSGraph _mouse_tsgraph1 = null;		// TSGraph where initial mouse
						// press occurs when drawing a
						// box

/**
Indicates if drawing should wait.  Use this if multiple layers are being
loaded and you don't want to see redraws between each one.
*/
private boolean _waiting = false;

// TODO SAM 2008-02-21 Need to enable
/**
Indicate whether to display cross-hairs on the graph to the edges
*/
private boolean _displayCrossHairCursor = false;

/**
Indicate whether the paint is to an SVG file.
See the saveAsSVG() method.
*/
private boolean __paintForSVG = false;

private TSGraphEditor _tsGraphEditor;

/**
Indicate whether SVG functionality is present. 
 */
public static final boolean svgEnabled;

static {
        // by default, its not enabled - if the classes are present,
        // then its enabled.
        boolean enabled = false;
        try {
            Class.forName("org.apache.batik.svggen.SVGGeneratorContext");
            enabled = true;
        } catch (ClassNotFoundException cnfe) {
            // do nothing
        }
        svgEnabled = enabled;
}

/**
Construct a TSGraphJComponet and display the time series.
@param parent Parent Frame object (often a TSViewGraphJFrame).
@param tslist Vector of time series to graph.
@param props Properties to control display of time series.  Most of these
properties are documented in TSViewJFrame, with the following additions:
ReferenceGraph can be set to "true" or "false" to indicate whether the graph is
a reference graph.  ReferenceTSIndex can be set to a Vector index to indicate
the reference time series for the reference graph (the default is the time
series with the longest overall period).
*/
public TSGraphJComponent ( TSViewGraphJFrame parent, Vector tslist, PropList props )
{	super ( "TSGraphJComponent" );
	_force_redraw = true;
	_parent = parent;
	_tslist = tslist;
	// Convert the old-style PropList to a TSProduct instance.  This also
	// fills _display_props...
	_tsproduct = createTSProductFromPropList ( props, tslist );
	checkDisplayProperties ( _display_props );
	_tsproduct.checkProperties();
	// Need to figure out the requested height and width because this
	// information is used to create the TSGraph instances...
	double height = 400.0, width = 400.0;

	String prop_val = _tsproduct.getLayeredPropValue (
				"TotalHeight", -1, -1, false );
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		height = StringUtil.atod(prop_val);
	}
	prop_val = _tsproduct.getLayeredPropValue ( "TotalWidth", -1, -1,false);
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		width = StringUtil.atod(prop_val);
	}

	if ( !_is_reference_graph ) {
		setSize ( (int)width, (int)height );
		// Swing seems to need this (the above does not do anything!)...
		setPreferredSize ( new Dimension((int)width, (int)height) );
	}
	
	// Now create the TSGraph instances that will manage the
	// individual graphs...
	_tsgraphs = createTSGraphsFromTSProduct ( _tsproduct,
						_display_props,
						tslist,
						new GRLimits(
						0.0,0.0,width,height) );
	checkTSProductGraphs ( _tsproduct, _tsgraphs );
	// Open the drawing areas with initial limits...
	openDrawingAreas ();

	// For now background color cannot be changed.
	setBackground ( _background_color );

	// Enable events on the component...

	requestFocus();
	addMouseListener ( this );
	addMouseMotionListener ( this );
	addKeyListener ( this );

	// Force a paint on construction...
	repaint();
}

/**
Construct a TSGraphJComponent and display the time series.
@param parent Parent JFrame object (often a TSViewGraphGUI).
@param tsproduct TSProduct describing the graph(s).
The following override properties can be set in the TSProduct to affect this
graph component.
@param display_props Display properties that control the display and which are
separate from the TSProduct.  Valid properties include those described below.
ReferenceGraph can be set to "true" or "false" to indicate whether the graph is
a reference graph.  ReferenceTSIndex can be set to a Vector index to indicate
the reference time series for the reference graph (the default is the time
series with the longest overall period).
*/
public TSGraphJComponent (	TSViewGraphJFrame parent, TSProduct tsproduct,
				PropList display_props )
{	super ( "TSGraphJComponent" );
	String routine = "TSGraphJComponent";
	_force_redraw = true;
	_parent = parent;
	_tslist = tsproduct.getTSList();// Do this for now.  Later remove
					// _tslist
	_display_props = display_props;
	if ( _display_props == null ) {
		_display_props = new PropList ( "display" );
	}
	if ( (_tslist == null) || (_tslist.size() == 0) ) {
		Message.printWarning ( 2, routine,
		"Time series list is null.  Product will be empty." );
	}
	checkDisplayProperties ( _display_props );
	_tsproduct = tsproduct;
	_tsproduct.checkProperties();
	// Set the requested (or if not specified, default) height and width
	// because this information is used to create the TSGraph instances...
	double height = 400.0, width = 400.0;

	String prop_val = _tsproduct.getLayeredPropValue (
				"TotalHeight", -1, -1, false );
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		height = StringUtil.atod(prop_val);
	}
	prop_val = _tsproduct.getLayeredPropValue ( "TotalWidth", -1, -1,false);
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		width = StringUtil.atod(prop_val);
	}

	setSize ( (int)width, (int)height );	// Set Java component size.
	// Swing seems to need this (the above does not do anything!)...
	setPreferredSize ( new Dimension((int)width, (int)height) );
	
	// Now create the TSGraph instances that will manage the
	// individual graphs.  The limits of the graphs will not be correct
	// because the titles, etc., have not been considered.  The limits will
	// be reset in the first paint() method call and every resize after
	// that.
	_tsgraphs = createTSGraphsFromTSProduct ( _tsproduct,
						_display_props,
						_tslist,
						new GRLimits(
						0.0,0.0,width,height) );
	checkTSProductGraphs ( _tsproduct, _tsgraphs );

	// Open the drawing areas with initial limits...
	openDrawingAreas ();

	// For now background color cannot be changed.
	setBackground ( _background_color );

	// Enable events on the component...

	requestFocus();
	addMouseListener ( this );
	addMouseMotionListener ( this );
	addKeyListener ( this );

	// Install decorator for cross hair cursor
	_cursorDecorator = new TSCursorDecorator(this, _rubber_band_color,_background_color);
	// Force a paint on construction...
	repaint();
}

/**
Add a TSListener to receive TSView events.  Multiple listeners can be
registered.  Graph events, including mouse events and zoom events, will result
in registered listeners being called.
@param listener TSViewListener to add.
*/
public void addTSViewListener ( TSViewListener listener )
{	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener != null ) {
		// Resize the listener array...
		if ( _listeners == null ) {
			_listeners = new TSViewListener[1];
			_listeners[0] = listener;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10,
				_gtype + "TSGraphJComponent.addTSViewListener",
				"Added TSViewListener" );
			}
		}
		else {	// Need to resize and transfer the list...
			int size = _listeners.length;
			TSViewListener [] newlisteners =
				new TSViewListener[size + 1];
			for ( int i = 0; i < size; i++ ) {
				newlisteners[i] = _listeners[i];
			}
			_listeners = newlisteners;
			_listeners[size] = listener;
			newlisteners = null;
		}
	}
}

/**
Indicate whether a reference graph would be useful, based on the graphs that
are shown.  This is called by TSViewGraphJFrame to let it decide whether to
display a reference graph.  This method does not currently evaluate the zoom
group property.
@return true if at least one graph allows zooming.
*/
public boolean canUseReferenceGraph()
{	return canUseZoom();
}

/**
Indicate whether enabling zoom should occur.  This will be the case if any of
the graphs can zoom.  Later, may allow completely if we figure out how
to zoom scatter, duration, etc.
This is called by TSViewGraphJFrame to let it decide whether to enable the zoom
button.
@return true if at least one graph allows zooming.
*/
public boolean canUseZoom()
{	int size = _tsgraphs.size();
	String prop_value;
	for ( int i = 0; i < size; i++ ) {
		prop_value = _tsproduct.getLayeredPropValue (
				"ZoomEnabled", i, -1, false );
		if ( prop_value.equalsIgnoreCase("true") ) { 
			return true;
		}
	}
	return false;
}

/**
Check to make sure that the display properties are set.  This checks the
display properties and sets internal variables that are commonly used.
@param 
*/
private void checkDisplayProperties ( PropList display_props )
{	// "ReferenceGraph"...

	_is_reference_graph = false;
	_gtype = "Main:";
	String prop_val = display_props.getValue ( "ReferenceGraph" );
	if ( (prop_val != null) && prop_val.equalsIgnoreCase("true") ) {
		_is_reference_graph = true;
		_gtype = "Ref:";
		_interaction_mode = INTERACTION_ZOOM;
	}

	if ( _external_Image == null ) {
		// Don't want to reset if already reset when converted from a
		// PropList - need to clean this up?
		_external_Image = 
			(BufferedImage)display_props.getContents("Image");
		if ( _external_Image != null ) {
			Message.printDebug( 1, "", _gtype +
			"Using external Image for drawing." );
			// Disable reference time series (don't want labels in
			// legend).
			display_props.set ( "ReferenceTSIndex=" + -1 );
		}
	}
}

/**
Check the TSProduct properties that defines the graphs to make sure that
properties are defined.  This is done so that defaults do not need to be
assigned throughout the class.  Later, needed properties (like fonts) are always
assumed to be defined.  In many cases, only defaults need to be defined at the
upper property levels (e.g., for Product or SubProduct).
<b>This method should be called AFTER createTSGraphsFromTSProduct() because the
time series data are checked to determine some properties.</b>
@param tsproduct TSProduct to check.
@praam tsgraphs Vector of TSGraph describing the graphs.
*/
private void checkTSProductGraphs ( TSProduct tsproduct, Vector tsgraphs )
{	// Put here any checks that cannot be performed in the initial call
	// to checkTSProduct() - primarily checks that depend on the time
	// series (units for axis labels, etc.).
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet (Prop.SET_AS_RUNTIME_DEFAULT);

	int nsubs = tsproduct.getNumSubProducts();
	//Message.printStatus ( 1, "",
	//_gtype + "Checking " + nsubs + " graphs after graphs were created." );
	int graph_type = TSProduct.GRAPH_TYPE_LINE;
	Vector tslist = null;
	TSGraph tsgraph = null;
	int nts = 0;
	String prop_val;
	for ( int isub = 0; isub < nsubs; isub++ ) {
		tsgraph = (TSGraph)tsgraphs.elementAt(isub);
		// Get the graph type to simplify later checks...
		prop_val = tsproduct.getLayeredPropValue (
					"GraphType", isub, -1, false );
		if ( prop_val == null ) {
			prop_val = "Line";
			tsproduct.setPropValue ( "GraphType", "Line", isub, -1);
		}
		graph_type = TSProduct.lookupGraphTypeNumber( prop_val);

		// Get the list of time series for the sub-product.  This is a
		// subset of the graphs used for the full product...

		tslist = tsgraph.getTSList();
		nts = 0;
		if ( tslist != null ) {
			nts = tslist.size();
		}
		/*
		Message.printStatus ( 2, "", _gtype +
		"Checking " + nts + " time series for graph [" + isub + "]" );
		*/

		// "LeftYAxisIgnoreUnits"
		//
		// If this is set in the TSProduct (e.g., a product file), then
		// don't need to do anything.  If not set, then it will have
		// been evaluated in the TSGraph (where an internal
		// boolean is set).  Set the property here so it is visible and
		// is compatible with the internal flag...

		if (	tsproduct.getLayeredPropValue("LeftYAxisIgnoreUnits",
			isub, -1, false ) == null ) {
			if ( tsgraph.ignoreLeftYAxisUnits() ) {
				tsproduct.setPropValue (
				"LeftYAxisIgnoreUnits", "true", isub, -1 );
			}
			else {	tsproduct.setPropValue (
				"LeftYAxisIgnoreUnits", "false", isub, -1 );
			}
		}

		// "LeftYAxisTitleString" - depends on graph type and units...

		// REVISIT SAM 2006-09-28
		// Why was the following always evaluted as true in previous
		// code with 1 == 1?  Was this a work-around for some other
		// problem.  Need to evaluate further when regression tests are
		// in place.
		if (	tsproduct.getLayeredPropValue("LeftYAxisTitleString",
			isub, -1, false ) == null ) {
			tsproduct.setPropValue ( "LeftYAxisTitleString",
				"", isub, -1 );
			if ( graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
				// Units will be 1st (dependent) time series -
				// need to do something else if more than one
				// time series...
				if ( nts >= 1 ) {
					TS ts0 = (TS)tslist.elementAt(0);
					tsproduct.setPropValue (
					"LeftYAxisTitleString",
					ts0.getDataUnits(), isub, -1 );
					ts0 = null;
				}
				else {	tsproduct.setPropValue (
					"LeftYAxisTitleString", "", isub, -1 );
				}
			}
			else if ( graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
				tsproduct.setPropValue (
				"LeftYAxisTitleString", "Legend Index",
				isub, -1 );
			}
			else {	// Title is the units that are displayed on
				// the axis.
				if ( tsgraph.ignoreLeftYAxisUnits() ) {
					// Units are not different in data and
					// are indicated in the legend.
					tsproduct.setPropValue (
					"LeftYAxisTitleString",
					"See units in legend", isub, -1 );
				}
				else {	// Get the units from the first non-null
					// time series...
					String units = "";
					TS ts = null;
					for ( int its = 0; its < nts; its++ ) {
						ts = (TS)tslist.elementAt(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue (
					"LeftYAxisTitleString",
					units, isub, -1 );
				}
			}
		}

		// "LeftYAxisUnits" - determined from graph type.

		if (	tsproduct.getLayeredPropValue("LeftYAxisUnits",
			isub, -1, false ) == null ) {
			if ( graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER ) {
				// Units will be 1st (dependent) time series -
				// need to do something else if more than one
				// time series...
				if ( nts >= 1 ) {
					TS ts0 = (TS)tslist.elementAt(0);
					tsproduct.setPropValue (
					"LeftYAxisUnits",
					ts0.getDataUnits(), isub, -1 );
					ts0 = null;
				}
				else {	tsproduct.setPropValue (
					"LeftYAxisUnits", "", isub, -1 );
				}
			}
			else if ( graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
				// Count of time series (not really used for
				// anything)...
				tsproduct.setPropValue (
				"LeftYAxisUnits", "COUNT", isub, -1 );
			}
			else {	// Units are time series data units.
				if ( tsgraph.ignoreLeftYAxisUnits() ) {
					// Units are not different in data and
					// are indicated in the legend.
					tsproduct.setPropValue (
					"LeftYAxisUnits", "", isub, -1 );
				}
				else {	// Get the units from the first non-null
					// time series...
					String units = "";
					TS ts = null;
					for ( int its = 0; its < nts; its++ ) {
						ts = (TS)tslist.elementAt(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue (
					"LeftYAxisUnits", units, isub, -1 );
				}
			}
		}

		// "LeftYAxisLabelPrecision" - DO THIS AFTER
		// "LeftYAxisUnits"

		if (	tsproduct.getLayeredPropValue("LeftYAxisLabelPrecision",
			isub, -1, false ) == null ) {
			if ( graph_type == TSProduct.GRAPH_TYPE_PERIOD ) {
				tsproduct.setPropValue (
				"LeftYAxisLabelPrecision", "0", isub, -1 );
			}
			else if ( tsgraph.ignoreLeftYAxisUnits() ) {
				// Set the precision to the maximum precision
				// for the units of all time series...
				int yaxis_precision = 2;
				tsproduct.setPropValue (
				"LeftYAxisLabelPrecision",
				"" + yaxis_precision, isub, -1 );
			} 
			else {	// Determine the precision from the axis units..
				String lefty_units =
				_tsproduct.getLayeredPropValue (
				"LeftYAxisUnits", isub, -1, false );
				if ( lefty_units.equals("") ) {
					// Default...
					tsproduct.setPropValue (
					"LeftYAxisLabelPrecision",
					"2", isub, -1 );
				}
				else {	try {	DataUnits u =
							DataUnits.lookupUnits
							( lefty_units );
						int precision =
							u.getOutputPrecision();
						u = null;
						tsproduct.setPropValue (
						"LeftYAxisLabelPrecision",
						"" + precision, isub, -1 );
					}
					catch ( Exception e ) {
						// Default...
						tsproduct.setPropValue (
						"LeftYAxisLabelPrecision",
						"2", isub, -1 );
					}
				}
			}
		}

		// BottomXAxisTitleString may have units...

		if (	tsproduct.getLayeredPropValue("BottomXAxisTitleString",
			isub, -1, false ) == null ) {
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			if ( tsgraph == null ) {
				continue;
			}
			if ( graph_type == TSProduct.GRAPH_TYPE_DURATION ) {
				tsproduct.setPropValue (
				"BottomXAxisTitleString",
				"Percent of values >= y-axis value", isub, -1 );
			}
			else if (graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER){
				// Bottom axis is the units of the first
				// independent time series...
				if ( nts >= 2 ) {
					TS ts1 = (TS)tslist.elementAt(1);
					String units = ts1.getDataUnits();
					tsproduct.setPropValue (
					"BottomXAxisTitleString", units, isub,
					-1 );
					try {	DataUnits u =
							DataUnits.lookupUnits (
							units );
						// Also set the precision...
						tsproduct.setPropValue (
						"BottomXAxisPrecision",
						"" + u.getOutputPrecision(),
						isub, -1 );
						u = null;
					}
					catch ( Exception e ) {
						tsproduct.setPropValue (
						"BottomXAxisPrecision",
						"1", isub, -1 );
					}
					ts1 = null;
				}
				else { tsproduct.setPropValue (
					"BottomXAxisTitleString", "", isub, -1);
				}
			}
			else {	tsproduct.setPropValue (
				"BottomXAxisTitleString",
				"", isub, -1 );
			}
		}
	}
	_tsproduct.getPropList().setHowSet (how_set_prev);
}

/**
Create a vector of TSGraph from a TSProduct and a list of time series.
The vector always contains the graphs but if a graph is disabled its size will
not be shown in the graph.
This typically would be called in the first paint where the component size is
known.
@param tsproduct TSProduct describing what to graph.
@param display_props Display properties (e.g., whether a reference graph).
@param tsproduct_tslist List of time series to graph for the full TSProduct.
@praam drawlim_graphs Drawing limits of the area set aside for graphs.  This
area is divided among the individual graphs.
@return Vector of TSGraph to use when drawing.  The Vector is
guaranteed to be non-null but may contain zero graphs.
*/
private Vector createTSGraphsFromTSProduct (	TSProduct tsproduct,
						PropList display_props,
						Vector tsproduct_tslist,
						GRLimits drawlim_graphs )
{	String routine = "TSGraphJComponent.createTSGraphsFromTSProduct";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Creating graphs from TSProduct" );
	}
	// XJTSX
	// change the false parameter from "true"
	int nsubs = tsproduct.getNumSubProducts( false );
	if ( nsubs == 0 ) {
		// Return an empty Vector (should not happen)
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			_gtype + "Created 0 graphs from TSProduct (no enabled subproducts " + "defined)" );
		}
		return new Vector ( 1 );
	}

	// For now, assume that graphs will be listed vertically with the
	// first one on top.

	// Loop through the number of graphs and create a TSGraph for each one.
	// For each TSID, locate the time series in the supplied time series
	// list, or set to null if not available.  That way all of the data
	// properties in the TSProduct will exactly agree with the Vector of TS

	TSGraph tsgraph = null;
	// Initialize with the number of enabled sub-products...
	double height = drawlim_graphs.getHeight()/nsubs;
	if ( _is_reference_graph ) {
		// Reset the height to the full height since we currently only
		// allow one reference graph on a component...
		height = drawlim_graphs.getHeight();
	}
	GRLimits drawlim = null;
	Vector tslist = null;
	int nts = 0;
	if ( tsproduct_tslist != null ) {
		nts = tsproduct_tslist.size();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "There are " + nts +
		" time series associated with the TSProduct" );
	}
	TS ts, tsfound;
	String prop_val;
	Vector tsgraphs = new Vector ( nsubs );
	int reference_ts_index = -1;	// This is the value for the tslist
					// that is used by the graph, NOT the
					// value in the entire list.
	
	int display_props_reference_ts_index = -1;
	String prop_value = display_props.getValue("ReferenceTSIndex");
	if ( prop_value != null ) {
		display_props_reference_ts_index =
		StringUtil.atoi ( prop_value );
	}
	
	// Indicate the subproduct that will be used to get reference graph
	// information...
	_reference_sub = -1;

	// Reset nsubs to all the products (even disabled, so that the product
	// indexes work out)...
	nsubs = tsproduct.getNumSubProducts();
	String TSID_prop_val;		// To hold value of TSID
	boolean check_input;		// Indicates whether to check the input
					// fields of the TSID against available
					// time series.
	String TSAlias_prop_val;	// To hold value of TSAlias

	for ( int isub = 0; isub < nsubs; isub++ ) {
		// Set the drawing limits for the graph on the page as an even
		// fraction of the whole page in the vertical direction (taking
		// up the full horizontal dimension).  Later we can allow more
		// options to place graphs on the page.  The drawing limits will
		// be reset whenever the component size changes.  The first
		// graph is at the top of the page, then going down.
		drawlim = new GRLimits ( drawlim_graphs.getLeftX(),
					drawlim_graphs.getTopY() - (isub + 1)*height,
					drawlim_graphs.getRightX(),	drawlim_graphs.getTopY() - isub*height);
		/*
		runningTotal += heights[isub];
		drawlim = new GRLimits(
			drawlim_graphs.getLeftX(),
			drawlim_graphs.getTopY() - runningTotal,
			drawlim_graphs.getRightX(),
			drawlim_graphs.getTopY() - runningTotal +heights[isub]);
		*/
		// Loop through the data properties for the subproduct looking
		// for "TSID" and "TSAlias" properties.  If a TSProduct was
		// passed in the constructor, it is possible that a "TS"
		// property was set to a time series, but this functionality is
		// not being used yet (NEED TO SUPPORT LATER).
		tslist = new Vector();	// Need new list for every graph
		reference_ts_index = -1;	// If we cannot match a TSID for
						// the current graph, then the
						// graph is NOT used for a
						// reference graph.
		for ( int jtsid = 0; ; jtsid++ ) {// Loop for TSIDs in graph
			// First get the TSID...
			TSID_prop_val = tsproduct.getLayeredPropValue (	"TSID", isub, jtsid, false );
			TSAlias_prop_val = tsproduct.getLayeredPropValue ( "TSAlias", isub, jtsid, false );
			// Allow one or the other (but not both) to be missing...
			if ( (TSID_prop_val == null) &&	(TSAlias_prop_val == null) ) {
				// Done with data for the graph...
				break;
			}
			if ( TSID_prop_val == null ) {
				// Assign blank...
				TSID_prop_val = "";
			}
			if ( TSAlias_prop_val == null ) {
				// Assign blank...
				TSAlias_prop_val = "";
			}
			if ( TSID_prop_val.indexOf("~") >= 0 ) {
				// The TSID has input fields so need to compare when searching for time series.
				check_input = true;
			}
			else {
                check_input = false;
			}
			if ( Message.isDebugOn ) {
			    Message.printDebug ( 2, routine, "Looking for time series needed for graph:  TSID_prop_val=\"" + TSID_prop_val +
                    "\" TSAlias_prop_val = \"" +TSAlias_prop_val + "\"." );
			}
			// Now find a matching time series in the available data.  If a match is not found, set the time series
			// to null so the properties line up.
			tsfound = null;
			for ( int kts = 0; kts < nts; kts++ ) {
				ts = (TS)tsproduct_tslist.elementAt(kts);
				if ( ts == null ) {
					continue;
				}
				if ( Message.isDebugOn ) {
				    Message.printDebug ( 2, routine, "Comparing to TS in available data: " +
				            "TSID=\"" + ts.getIdentifier().toString(true) + "\" Alias=\"" + ts.getAlias() + "\"" );
				}
				//if ( Message.isDebugOn ) {
					//Message.printDebug ( 1, routine,
					//_gtype+"Creating TSGraph, tsid is \""+
					//ts.getIdentifierString() +
					//"\" TSID prop is \"" + TSID_prop_val +
					//"\"" );
				//}
                if ( !TSAlias_prop_val.equals("") ) {
				    // If an alias is specified, just match the alias...
				    if ( ts.getAlias().equalsIgnoreCase( TSAlias_prop_val) ) {
				        if ( Message.isDebugOn ) {
				            Message.printStatus ( 2, routine, "Time series aliases match.");
				        }
					    tsfound = ts;         
                    }
                }
                else {
					// No alias so use the full TSID with input type...
					if ( ts.getIdentifier().equals( TSID_prop_val,check_input) ) {
					    if ( Message.isDebugOn ) {
					        Message.printDebug ( 2, routine, "Time series identifiers match.");
					    }
                        tsfound = ts;
                    }
                }
                if ( tsfound != null ) {        
					if(	display_props_reference_ts_index == kts ) {
						// Set the TSID index within the graph's TS indicating which main graph TS is in the
						// the reference graph.  If a main graph, the reference graph is indicated in the legend...
						reference_ts_index = jtsid;
						_reference_sub = isub;
					}
					break;
				}
				else {
				    if ( Message.isDebugOn ) {
				        Message.printDebug ( 2, routine, "TSIDs and TSAliases are not equal");
				    }
				}
			}
			// Could put the following in the if statement in the loop but for now put here...
			//
			// If the "TSIndex" property is found, just use it to
			// match up the time series.  This works well when a
			// PropList and Vector of TS is used to create the
			// TSProduct, as long as only one graph is used.
			// Do this after the above loop so that the reference
			// graph can be set up (note that there is a possibility
			// of the reference graph using the wrong TS because of
			// duplicate IDs but that usually won't be that much of
			// a problem).
			//
			// This approach might be extended to true TSProduct
			// processing if we can assume that a TSID is always
			// matched with a TS (even if null) and that the order
			// of the TSID in the product description file matches
			// the order of the time series passed in.
			prop_val = tsproduct.getLayeredPropValue ( "TSIndex", isub, jtsid, false );
			if ( (nsubs == 1) && (prop_val != null) && StringUtil.isInteger(prop_val) ) {
				tsfound = (TS)tsproduct_tslist.elementAt( StringUtil.atoi(prop_val) );
			}
			// Now add the time series or null reference...
            if ( tsfound == null ) {
                if ( Message.isDebugOn ) {
                    Message.printDebug(2, routine, "Could not find time series for graph." );
                }
            }
            else {
                if ( Message.isDebugOn ) {
                    Message.printDebug( 2, routine, "Found time series \"" + tsfound.getIdentifierString() +
                            "\" alias=\"" + tsfound.getAlias() + "\" for graph." );
                }
            }
			tslist.addElement ( tsfound );
		}
		// Supply the short time series list for the graph and add the graph to the main Vector to be managed.
		tsgraph = new TSGraph ( this, drawlim, tsproduct, display_props, isub, tslist, reference_ts_index );
		tsgraphs.addElement ( tsgraph );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Added graph " + isub + " reference_ts_index = " + reference_ts_index);
		}
	}
	ts = null;
	tsfound = null;
	tsgraph = null;
	tslist = null;
	/*
	Message.printStatus(2,"TSGraphJComponent.createTSGraphsFromTSProduct",
	_gtype + "Created " + tsgraphs.size() + " graphs from TSProduct" );
	*/
	return tsgraphs;
}

/**
Create a TSProduct from a simple old-style PropList.  The member data
_tsproduct is created.  It is assumed that a single graph is created from the
PropList, using old-style conventions.  Old-style PropList properties for
TSViewFrame are transferred to TSProduct properties.  Any properties that match
expanded TSProduct properties are transferred as is.
@param proplist PropList passed in during construction.
@param a TSProduct containing values from the PropList.  If the list is null, 
an empty TSProduct is created.  The checkTSProduct() method assigns appropriate
default values for the resulting TSProduct.
@param tslist List of time series for the product.
*/
private TSProduct createTSProductFromPropList ( PropList proplist, Vector tslist )
{	//String routine = "TSGraphJComponent.createTSProductFromPropList";

    try {

	//Message.printStatus ( 2, "", "Creating TSProduct from PropList and TS list" );
	// Create a new TSProduct...
	TSProduct tsproduct = new TSProduct ();
	tsproduct.setTSList ( tslist );
	if ( proplist == null ) {
		return tsproduct;
	}

	// The graph type and other major properties are interpreted as being
	// set by the user because the user initially picked the graph type.  If
	// this is not set as a user-defined property (and is instead set as a
	// run-time default) it will not be saved.

	int how_set_prev = tsproduct.getPropList().getHowSet();
	tsproduct.getPropList().setHowSet ( Prop.SET_AT_RUNTIME_FOR_USER );

	//---------------------------------------------------------------------
	// Product properties
	//---------------------------------------------------------------------

	// Currently only graph product types are recognized so set the type.
	// Interpret the graph height and width as total values in the TSProduct
	// since we are assuming one graph...

	tsproduct.setPropValue ( "ProductType", "Graph", -1, -1 );

/* TODO - obsolete properties?
	String prop_val = proplist.getValue ( "GraphWidth" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "TotalWidth", prop_val, -1, -1 );
	}

	prop_val = proplist.getValue ( "GraphHeight" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "TotalHeight", prop_val, -1, -1 );
	}
*/
	String prop_val = proplist.getValue ( "TotalWidth" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "TotalWidth", prop_val, -1, -1 );
	}

	prop_val = proplist.getValue ( "TotalHeight" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "TotalHeight", prop_val, -1, -1 );
	}

	// Transfer "Product." properties...

	Vector v = proplist.getPropsMatchingRegExp ( "Product.*" );
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	Prop prop = null;
	PropList tsproduct_props = tsproduct.getPropList();
	for ( int i = 0; i < size; i++ ) {
		prop = (Prop)v.elementAt(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	//---------------------------------------------------------------------
	// SubProduct properties
	//---------------------------------------------------------------------

	prop_val = proplist.getValue ( "GraphType" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "GraphType", prop_val, 0, -1 );
	}
	else {	// Default...
		tsproduct.setPropValue ( "GraphType", "Line", 0, -1 );
	}

	prop_val = proplist.getValue ( "Title" );	// Older
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "MainTitleString", prop_val, 0, -1 );
	}

	prop_val = proplist.getValue ( "TitleString" );	// Newer
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "MainTitleString", prop_val, 0, -1 );
	}

	// "XAxis.Format" and "XAxisFormat" -> BottomXAxisLabelFormat

	prop_val = proplist.getValue ( "XAxis.Format" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "BottomXAxisLabelFormat",
			prop_val, 0, -1 );
	}
	else {	// Older version...
		prop_val = proplist.getValue ( "XAxisFormat" );
		if ( prop_val != null ) {
			tsproduct.setPropValue ( "BottomXAxisLabelFormat", prop_val, 0, -1 );
		}
	}

	prop_val = proplist.getValue ( "XAxisLabelString" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "LeftXAxisTitleString", prop_val, 0, -1 );
	}

	prop_val = proplist.getValue ( "YAxisPrecision" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "LeftYAxisLabelPrecision",	prop_val, 0, -1 );
	}

	prop_val = proplist.getValue ( "YAxisType" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "LeftYAxisType", prop_val, 0, -1 );
	}

	prop_val = proplist.getValue ( "BarPosition" );
	if ( prop_val != null ) {
		// Convert to new values...
		if ( prop_val.equalsIgnoreCase("BarsLeftOfDate") ) {
			tsproduct.setPropValue ( "BarPosition", "LeftOfDate", 0, -1 );
		}
		else if ( prop_val.equalsIgnoreCase("BarsRightOfDate") ) {
			tsproduct.setPropValue ( "BarPosition",	"RightOfDate", 0, -1 );
		}
		else {
            tsproduct.setPropValue ( "BarPosition",	"CenteredOnDate", 0, -1 );
		}
	}

	// Transfer "SubProduct" properties...

	v = proplist.getPropsMatchingRegExp ( "SubProduct *" );
	size = 0;
	if ( v != null ) {
		size = v.size();
	}
	for ( int i = 0; i < size; i++ ) {
		prop = (Prop)v.elementAt(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	// Associate data for the time series...

	int nts = 0;
	if ( tslist != null ) {
		nts = _tslist.size();
	}
	TS ts;
	int how_set_prev2 = tsproduct.getPropList().getHowSet();
	for ( int i = 0; i < nts; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts == null ) {
			tsproduct.setPropValue ( "TSID", "", 0, i );
		}
		else {
            tsproduct.setPropValue ( "TSID", ts.getIdentifier().toString(true), 0, i );
			// Set the alias if available...
			if ( !ts.getAlias().equals("") ) {
				tsproduct.setPropValue ( "TSAlias",	ts.getAlias(), 0, i );
			}
			tsproduct.getPropList().setHowSet (	Prop.SET_AS_RUNTIME_DEFAULT );
			// Set the index.  This is actually a more robust way
			// to connect the time series with graphs when the time
			// series come in from a Vector and PropList.
			tsproduct.setPropValue ( "TSIndex", "" + i, 0, i );
			tsproduct.getPropList().setHowSet ( how_set_prev2 );
			//Message.printStatus ( 2, "", "Graph has TSID \"" + ts.getIdentifier().toString(true) + "\"" );
		}
	}
	ts = null;

	// Transfer "Data" properties in case these are being set manually in
	// the calling code...

	v = proplist.getPropsMatchingRegExp ( "Data *" );
	size = 0;
	if ( v != null ) {
		size = v.size();
	}
	for ( int i = 0; i < size; i++ ) {
		prop = (Prop)v.elementAt(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	//---------------------------------------------------------------------
	// Display properties
	//
	// Properties that are treated as override properties in the TSProduct
	// because they control GUI appearance...
	//---------------------------------------------------------------------

	// Other properties that might be passed in by TSViewGraphFrame...
	//
	// MaximizeGraphSpace - default true
	// Graph.EnableTracker - default true

	_display_props = new PropList ( "display" );

	// Double buffering is seldom specified.  The default is true (set in the base class)...
	_double_buffering = true;
	prop_val = proplist.getValue ( "DoubleBuffer" );
	if ( (prop_val != null) && prop_val.equalsIgnoreCase("false") ) {
		_double_buffering = false;
	}

	// A reference graph is used for zooming and has few other features besides data...
	prop_val = proplist.getValue ( "ReferenceGraph" );
	if ( prop_val != null ) {
		_display_props.set ( "ReferenceGraph=" + prop_val );
	}

	prop_val = proplist.getValue ( "ReferenceTSIndex" );
	if ( prop_val != null ) {
		_display_props.set ( "ReferenceTSIndex=" + prop_val );
	}

	// Image passed in when processing products in batch mode...

	_external_Image = (BufferedImage)proplist.getContents("Image");
	if ( _external_Image != null ) {
		Message.printDebug( 1, "", _gtype + "Using external Image for drawing." );
		// Disable reference time series (don't want labels in legend).
		_display_props.set ( "ReferenceTSIndex=" + -1 );
	}

	tsproduct.getPropList().setHowSet ( how_set_prev );

	// Return the new TSProduct...
	return tsproduct;

	}
	catch ( Exception e ) {
		// Should never happen.
	}
	return null;
}

/**
Clear the entire graph.  Need to do this manually rather than rely on default
update() to make sure it happens at the right time.  This uses Graphics calls on
the entire window.
*/
private void clearView ()
{	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.clearView", _gtype +	"Clearing the graph." );
	}
	if ( !_printing && (_graphics != null) ) {
		// Fill in the background color.  Need this because update() does not do (becaus of zooming)...
		_graphics.setColor ( getBackground() );
		_bounds = getBounds();
		_graphics.fillRect ( 0, 0, _bounds.width, _bounds.height );
	}
}

/**
Creates a Vector of vectors of data that store the number of time series 
associated with each graph, the ids of the time series associated with each
graph, and the data limits of each graph.  This is done so that the graphs
can be rebuilt properly during a call to reinitializeGraphs().  The data is used
in resetGraphDataLimits().  The Vector is messy, but since RTi doesn't use
private internal classes, this will have to do.
@return a Vector, each element of which is:<br>
A Vector with data for the graph numbered the same as this element, 
each element of which is:<br>
0 - an integer (the number of time series in the graph)<br>
1 - a Vector of Strings (each of which is an id of a time series in the graph)
<br>
2 - data limits for the graphs<br>
*/
private Vector determineDataLimits() {
	TS tempTS = null;
	Vector limitsData = new Vector();
	Vector ids = null;
	Vector ts = null;
	Vector v = null;

	for (int i = 0; i < _tsgraphs.size(); i++) {
		ts = ((TSGraph)(_tsgraphs.elementAt(i))).getTSList();

		v = new Vector();
		v.add(new Integer(ts.size()));
		
		ids = new Vector();
		for (int j = 0; j < ts.size(); j++) {
			tempTS = (TS)(ts.elementAt(j));
			if (tempTS == null) {
				ids.add("null");
			}
			else {
				ids.add(tempTS.getIdentifierString());
			}
		}
		ids = StringUtil.sortStringList(ids);
		v.add(ids);
		v.add(((TSGraph)(_tsgraphs.elementAt(i))).getDataLimits());
		limitsData.add(v);
	}

	return limitsData;
}

/**
Draw the drawing areas.  This is used for testing only.
*/
private void drawDrawingAreas ()
{	_da_page.setColor ( GRColor.cyan );
	// Reference and main...
	GRDrawingAreaUtil.drawRectangle ( _da_graphs,
				_datalim_graphs.getLeftX(),	_datalim_graphs.getBottomY(),
				_datalim_graphs.getWidth(),	_datalim_graphs.getHeight() );
	if ( _is_reference_graph ) {
		// Don't need to draw anything else...
		return;
	}
	GRDrawingAreaUtil.drawRectangle ( _da_page,
				_datalim_page.getLeftX(), _datalim_page.getBottomY(),
				_datalim_page.getWidth(), _datalim_page.getHeight() );
	GRDrawingAreaUtil.drawRectangle ( _da_maintitle,
				_datalim_maintitle.getLeftX(), _datalim_maintitle.getBottomY(),
				_datalim_maintitle.getWidth(),_datalim_maintitle.getHeight());
	GRDrawingAreaUtil.drawRectangle ( _da_subtitle,
				_datalim_subtitle.getLeftX(), _datalim_subtitle.getBottomY(),
				_datalim_subtitle.getWidth(), _datalim_subtitle.getHeight() );
}

/**
Draw the titles for the component.  The properties are retrieved again in case
they have been reset by a properties GUI.
*/
private void drawTitles ()
{	if ( _is_reference_graph ) {
		// Don't need to draw anything...
		return;
	}

	// Main title.

	_da_maintitle.setColor ( GRColor.black );
	String maintitle_font = _tsproduct.getLayeredPropValue ( "MainTitleFontName", -1, -1, false );
	String maintitle_fontstyle = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", -1, -1, false );
	String maintitle_fontsize = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", -1, -1, false );
	GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font, maintitle_fontstyle,
			StringUtil.atod(maintitle_fontsize) );
	String maintitle_string = _tsproduct.getLayeredPropValue ( "MainTitleString", -1, -1, false );
	GRDrawingAreaUtil.drawText ( _da_maintitle, maintitle_string,
			_datalim_maintitle.getCenterX(), _datalim_maintitle.getCenterY(), 0.0,
			GRText.CENTER_X|GRText.CENTER_Y );

	// Sub title....

	_da_subtitle.setColor ( GRColor.black );
	String subtitle_font = _tsproduct.getLayeredPropValue ( "SubTitleFontName", -1, -1, false );
	String subtitle_fontstyle = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", -1, -1, false );
	String subtitle_fontsize = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", -1, -1, false );
	GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font, subtitle_fontstyle, StringUtil.atod(subtitle_fontsize) );
	String subtitle_string = _tsproduct.getLayeredPropValue ( "SubTitleString", -1, -1, false );
	GRDrawingAreaUtil.drawText ( _da_subtitle, subtitle_string,
			_datalim_subtitle.getCenterX(),	_datalim_subtitle.getCenterY(), 0.0,
			GRText.CENTER_X|GRText.CENTER_Y );
}

/**
Draw the footers for the component.  Nothing is done at this time.
*/
private void drawFooters ()
{
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable {	
	_tslist = null;	
	_background_color = null;
	_external_Image = null;
	_rubber_band_color = null;
	_tsproduct = null;
	_display_props = null;
	_da_page = null;
	_da_maintitle =null;
	_da_subtitle = null;
	_da_graphs = null;
	_da_leftfoot = null;
	_da_centerfoot=null;
	_da_rightfoot =null;
	_datalim_page = null;		
	_drawlim_page = null;
	_datalim_maintitle = null;	
	_drawlim_maintitle = null;	
	_datalim_subtitle = null;	
	_drawlim_subtitle = null;	
	_datalim_graphs = null;	
	_drawlim_graphs = null;	
	_datalim_leftfoot = null;	
	//_drawlim_leftfoot = null;	
	_datalim_centerfoot = null;	
	//_drawlim_centerfoot = null;	
	_datalim_rightfoot = null;	
	//_drawlim_rightfoot = null;	
	_bounds = null;		
	_printBounds = null;		
	_parent = null;			
	_tsgraphs = new Vector();	
	_gtype = null;
	IOUtil.nullArray(_listeners);
	_mouse_tsgraph1 = null;
	super.finalize();
}

/**
Determine the TSGraph that an event occurred in, checking only the graph
drawing areas in the TSGraph.
@return the TSGraph that an event occurred in or null if not within the
bounds of a TSGraph.
@param pt point for the raw device units where the event occurred.
*/
private TSGraph getEventTSGraph ( GRPoint pt )
{	int size = _tsgraphs.size();
	TSGraph tsgraph = null;
	for ( int isub = 0; isub < size; isub++ ) {
		tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
		if ( _is_reference_graph && (isub != _reference_sub) ) {
			// Don't check the graph
			continue;
		}
		if ( tsgraph.getGraphDrawingArea().getPlotLimits(
			GRDrawingArea.COORD_DEVICE).contains(pt) ) {
			return tsgraph;
		}
	}
	return null;
}

/**
Return the graph interaction mode.
@return the interaction mode (see INTERACTION_*).
*/
public int getInteractionMode ()
{	return _interaction_mode;
}

/**
Return the JFrame that includes this component.  This can be used, for example,
to display a dialog in lower-level code.
@return the JFrame for the component.
*/
protected JFrame getJFrame()
{	return _parent;
}

/**
Return the TSProduct that corresponds to the TSGraphJComponent.  A TSProduct is
either passed in during construction (new convention) or is created from a
PropList (old convention) during construction.
@return TSProduct associated with the component.
*/
public TSProduct getTSProduct ()
{	return _tsproduct;
}

/**
Indicate whether this component is currently printing.
@return true if the component is currently being printed, false if not.
*/
public boolean isPrinting ()
{	return _printing;
}

/**
Indicate whether this component is a reference graph.
@return true if the component contains a reference graph, false if not.
*/
public boolean isReferenceGraph ()
{	return _is_reference_graph;
}

/**
Respond to KeyEvents.  Most single-key events are handled in keyReleased to
prevent multiple events.  Do track when the shift is pressed here.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();
	//Message.printStatus ( 1, "", "Key pressed = " + code );

	if ( code == KeyEvent.VK_HOME ) {
		scrollToStart(true);
	}
	else if ( code == KeyEvent.VK_END ) {
		scrollToEnd(true);
	}
	else if ( code == KeyEvent.VK_PAGE_DOWN ) {
		scroll(-1.0,true);
	}
	else if ( code == KeyEvent.VK_PAGE_UP ) {
		scroll(1.0,true);
	}
	else if ( code == KeyEvent.VK_LEFT ) {
		scroll(-.5,true);
	}
	else if ( code == KeyEvent.VK_RIGHT ) {
		scroll(.5,true);
	}
}

/**
Respond to KeyEvents.
*/
public void keyReleased ( KeyEvent event )
{
}

public void keyTyped ( KeyEvent event )
{
}

/**
Look up the color to use for a time series.
@param index Zero-referenced index in time series vector.
@return the color for a time series, as a string color name.
*/
public static String lookupTSColor(int index) {
	// the following is done so that for Point graphs, time series X and 
	// X + 10 don't have the same symbol style AND color.  This makes it
	// so that their symbol styles will be the same, but there won't be
	// any time series with the same symbol and color until over 100 
	// time series are in the graph.
	if (index > 9) {
		index += (index / 10);
	}
	
	int remainder = index % 10;

	if ( index == 0 ) {
		// First color red...
		return "red";
	}
	else if (remainder == 0) {
		return "black";
	}
	else if (remainder == 1) {
		return "blue";
	}
	else if (remainder == 2) {
		return "green";
	}
	else if (remainder == 3) {
		return "cyan";
	}
	else if (remainder == 4) {
		return "magenta";
	}
	else if (remainder == 5) {
		return "black";
	}
	else if (remainder == 6) {
		return "yellow";
	}
	else if (remainder == 7) {
		return "pink";
	}
	else if (remainder == 8) {
		return "gray";
	}
	else {	
		return "orange";
	}
}

/**
Look up the symbol to use for a time series.  This is only used for point 
graphs.
@param index Zero-referenced index in time series vector.
@return the symbol for a time series, as a string symbol name.
*/
public static String lookupTSSymbol(int index) {
	int remainder = index % 10;
	
	if ( index == 0 ) {
		// First color red...
		return "Circle-Filled";
	}
	else if (remainder == 0) {	
		return "Circle-Filled";
	}	
	else if (remainder == 1) {
		return "Triangle-Up-Filled";
	}
	else if (remainder == 2) {
		return "Square-Filled";
	}
	else if (remainder == 3) {
		return "Diamond-Filled";
	}
	else if (remainder == 4) {
		return "Plus";
	}
	else if (remainder == 5) {
		return "X";
	}
	else if (remainder == 6) {
		return "Asterisk";
	}
	else if (remainder == 7) {
		return "Circle-Hollow";
	}
	else if (remainder == 8) {
		return "Triangle-Up-Hollow";
	}
	else if (remainder == 9) {
		return "Square-Hollow";
	}
	else {
		return "Circle-Hollow";
	}	
}

/**
Handle mouse clicked event.
@param event MouseEvent.
*/
public void mouseClicked ( MouseEvent event )
{
  if (getInteractionMode()!= INTERACTION_EDIT)
    {
      // Not editing, return
      return;
    }
  else
    {
      TSGraph tsgraph = getEventTSGraph ( new GRPoint(event.getX(), event.getY()));
      if ( tsgraph == null ) 
        {
          // Not in a graph
          return;
        }
      editPoint(event, tsgraph);
      refresh(false);
    }
}

/**
 * Notifies TSGraphEditor of point edit.
 * 
 * @param event
 * @param tsgraph
 */
private void editPoint(MouseEvent event,TSGraph tsgraph)
{
  GRLimits daLimits = tsgraph.getGraphDrawingArea().getPlotLimits( GRDrawingArea.COORD_DEVICE); 
  if (isInside(event, daLimits))
    {
      GRPoint datapt = tsgraph.getGraphDrawingArea().getDataXY(
          event.getX(), event.getY(), GRDrawingArea.COORD_DEVICE );
      _tsGraphEditor.editPoint(datapt);
    }
}

/** 
 * Returns whether mouse is inside drawing area
 * @return
 */
// TODO:dre refactor
private final boolean isInside(MouseEvent event, GRLimits grLimits)
{
  return (event.getX() > (int)grLimits.getLeftX()
      && event.getX() < (int)grLimits.getRightX()
      && event.getY() > (int)grLimits.getTopY()
      && event.getY() < (int)grLimits.getBottomY())
    ?true:false;
}

/**
Handle mouse drag event.  If in zoom mode, redraw the rubber-band line.
If a mouse tracker is enabled, call the TSViewListener.mouseMotion() method.
@param event Mouse drag event.
*/
public void mouseDragged ( MouseEvent event )
{	//event.consume();
	if ( (_interaction_mode != INTERACTION_SELECT) && (_interaction_mode != INTERACTION_ZOOM) ) {
		return;
	}

	int mods = event.getModifiers();
	if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		// Don't want rubber band box...
		return;
	}

	// Figure out which graph the event occurred in...

	TSGraph tsgraph = getEventTSGraph (	new GRPoint(event.getX(),event.getY() ) );
	if ( tsgraph == null ) {
		// User is dragging outside a graph.  If they started
		// outside the graph, nothing would have been
		return;
	}

	// Else dragging in a valid graph.  If they started in outside and are
	// now crossing into a graph, initialize the coordinates similar to mousePressed()...

	if ( _mouse_x1 == -1 ) {
		// Let the other method do the work...
		mousePressed ( event );
		// Don't need to do anything else...
		return;
	}

	// Get the coordinates used...

	_mouse_x2 = event.getX();
	_mouse_y2 = event.getY();

	// Figure out which drawing area the event occurred in...

	int graph_type = tsgraph.getGraphType();
	if ((graph_type == TSProduct.GRAPH_TYPE_DURATION) || (graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER)) {
		// Don't allow zoom.
		_rubber_banding = false;
		return;
	}
	_rubber_banding = true;
	// Let listeners know so the tracker can be updated to help
	// size the rubber band box...
	// Data units...
	GRPoint datapt = tsgraph.getGraphDrawingArea().getDataXY ( _mouse_x2, _mouse_y2, GRDrawingArea.COORD_DEVICE );
	if ( datapt == null ) {
		// Generally only happens when the graph cannot be displayed.
		return;
	}
	boolean dotrack = true;
	if ( dotrack ) {
		// Device units...
		GRPoint devpt = new GRPoint ( _mouse_x2, _mouse_y2 );
		int size = _listeners.length;
		for ( int i = 0; i < size; i++ ) {
			_listeners[i].tsViewMouseMotion(tsgraph, devpt, datapt);
		}
		devpt = null;
	}
	datapt = null;
	// Force a redraw...  The _rubber_banding flag will be checked so a full redraw is not done.
	repaint ();
}

/**
Handle mouse enter event.  Currently does not do anything.
@param event MouseEvent to handle.
*/
public void mouseEntered ( MouseEvent event )
{	
}

/**
Handle mouse exit event.  Currently does not do anything.
@param event MouseEvent to handle.
*/
public void mouseExited ( MouseEvent event )
{
}

/**
Handle mouse motion event.
If a mouse tracker is enabled, call the TSViewListener.mouseMotion() method.
@param event MouseEvent to handle.
*/
public void mouseMoved ( MouseEvent event )
{	if ( _listeners == null ) {
		return;
	}

	// Get the mouse position...

	int x = event.getX();
	int y = event.getY();

	TSGraph tsgraph = getEventTSGraph ( new GRPoint ( x, y ) );
	if ( tsgraph == null ) {
		// Mouse is not in a graph area so don't track...
		return;
	}
	
	// Update cross-hair cursor
	if (getInteractionMode()== INTERACTION_EDIT)
	  {
	    _cursorDecorator.mouseMoved(event,tsgraph.getGraphDrawingArea().getPlotLimits(
	        GRDrawingArea.COORD_DEVICE));
	  //  refresh(false);
	  }

	// Get coordinates in data units...

	GRPoint datapt = tsgraph.getGraphDrawingArea().getDataXY(
		x, y, GRDrawingArea.COORD_DEVICE );

	GRPoint devpt = new GRPoint ( x, y );

	// Call the listeners...

	for ( int ilist = 0; ilist< _listeners.length; ilist++ ) {
		_listeners[ilist].tsViewMouseMotion (
			tsgraph, devpt, datapt );
	}
	tsgraph = null;
}

/**
Handle mouse pressed event.  Start a select or zoom.  The event is completed
when the mouse is released.
@param event MouseEvent to handle.
*/
public void mousePressed ( MouseEvent event )
{	//event.consume();
	requestFocus();
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, _gtype +
		"TSGraphJComponent.mousePressed", "Mouse pressed." );
	}
	// Initialize the coordinates.  If the click is outside any graph, these
	// values will signal to mouseDragged() that a valid starting point for
	// a box has not been given.
	_mouse_tsgraph1 = null;
	_mouse_x1 = _mouse_y1 =_mouse_x2=_mouse_y2=_mouse_xprev=_mouse_yprev=-1;
	// Figure out which drawing area the event occurred in...
	TSGraph tsgraph = getEventTSGraph (
		new GRPoint ( event.getX(), event.getY() ) );
	if ( tsgraph == null ) {
		// Did not click on a valid graph and currently don't allow
		// graph legends, etc. to be selected for properties...
		return;
	}
	_mouse_tsgraph1 = tsgraph;
	int mods = event.getModifiers();
	if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		// Each graph provides its own popup menu to edit properties and
		// view analysis details...
		JPopupMenu popup_menu = tsgraph.getJPopupMenu();
		if ( popup_menu != null ) {
			// Add to the component.  It will be removed
			// when the menu event is processed...
			add ( popup_menu );
			// Show the popup menu.  It is modal.
			popup_menu.show ( event.getComponent(),
				event.getX(), event.getY() );
			// Now try removing from the component...
			// Doing this seems to disable the menu action.
			// See notes in TSGraph related to the the popup
			// menu.
			//remove ( popup_menu );
		}
	}
	else if ( (_interaction_mode == INTERACTION_SELECT) ||
		(_interaction_mode == INTERACTION_ZOOM) ) {
		// Save the point that was selected so that the drag and
		// released events will work.  Also save the initial graph
		// so that we can make sure not to drag outside a valid
		// graph.
		_mouse_x1 = event.getX();
		_mouse_y1 = event.getY();
	}
}

/**
Handle mouse released event.  If in INTERACTION_ZOOM mode, call the
tsViewZoom() method of registered TSViewListeners.  If in INTERACTION_SELECT
mode, call tsViewSelect().  Only return a region if the mouse has moved at
least 5 pixels in one direction.
*/
public void mouseReleased ( MouseEvent event )
{	//event.consume();

	// Figure out which graph the event occurred in...

	TSGraph tsgraph = getEventTSGraph(
		new GRPoint ( event.getX(), event.getY() ) );
	int x = 0;
	int y = 0;
	if ( tsgraph == null ) {
		// The mouse was released outside a graph valid area.  If a
		// valid previous point is available, use that to close the
		// box...
		if ( _mouse_tsgraph1 == null ) {
			// Zoom box never was initialized...
			return;
		}
		tsgraph = _mouse_tsgraph1;
		x = _mouse_xprev;
		y = _mouse_yprev;
	}
	else {	// use the valid point within the graph...
		x = event.getX();
		y = event.getY();
	}
	int graph_type = 0;
	if ( tsgraph != null ) {
		graph_type = tsgraph.getGraphType();
	}
	if (	(graph_type == TSProduct.GRAPH_TYPE_XY_SCATTER) ||
		(graph_type == TSProduct.GRAPH_TYPE_DURATION) ) {
		// Currently do not allow zoom, etc...
		return;
	}
	int mods = event.getModifiers();
	if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		// Right click so don't do anything...
		return;
	}
	if (	(_interaction_mode == INTERACTION_SELECT) ||
		(_interaction_mode == INTERACTION_ZOOM) ) {
		// Only process if box is "delta" pixels or bigger...
		int deltax = x - _mouse_x1;
		int delta_min = 2;
		if ( deltax < 0 ) {
			deltax *= -1;
		}
		int deltay = y - _mouse_y1;
		if ( deltay < 0 ) {
			deltay *= -1;
		}
		if ( (deltax <= delta_min) || (deltay <= delta_min) ) {
			if ( _interaction_mode == INTERACTION_SELECT ) {
				// Assume they want the original point...
				GRPoint devpt = new GRPoint (
					(double)_mouse_x1, (double)_mouse_y1 );
				GRPoint datapt =
					tsgraph.getGraphDrawingArea().getDataXY(
					_mouse_x1, _mouse_y1,
					GRDrawingArea.COORD_DEVICE );
				if ( _listeners != null ) {
					int size = _listeners.length;
					// Need to figure out which time series
					// was selected...
					for ( int i = 0; i < size; i++ ) {
						_listeners[i].tsViewSelect (
						tsgraph, devpt, datapt,
						(Vector)null );
					}
				}
				devpt = null;
				datapt = null;
				_rubber_banding = false;
				// Reset zoom coordinates...
				_mouse_x2 = _mouse_xprev = -1;
				if ( (x != _mouse_x1) && (y != _mouse_y1) ) {
					repaint();
				}
				// Don't need to do anything else...
				return;
			}
			else if ( _interaction_mode == INTERACTION_ZOOM ) {
				// Too small, don't allow...
				// Reset zoom coordinates and force a redraw
				// to clear the box...
				_mouse_x2 = _mouse_xprev = -1;
				if ( (x != _mouse_x1) && (y != _mouse_y1) ) {
					// There was some motion so need to
					// redraw to clear the box...
					refresh();
					_rubber_banding = false;
					//repaint();
				}
				// Don't need to do anything else...
				return;
			}
		}
		// Save the point that was selected so that the drag and
		// released events will work...
		// Reset the data limits to those from the zoom box...
		// Make sure that the limits are always specified 
		int xmin, xmax, ymin, ymax;
		xmin = xmax = _mouse_x1;
		ymin = ymax = _mouse_y1;
		if ( x < xmin ) {
			xmin = x;
		}
		if ( y < ymin ) {
			ymin = y;
		}
		if ( x > xmax ) {
			xmax = x;
		}
		if ( y > ymax ) {
			ymax = y;
		}
		
		GRLimits mouse_limits =
				new GRLimits ( xmin, ymin, xmax, ymax );
		// Reverse Y so we get the right values in GR...
		GRPoint pt1 = tsgraph.getGraphDrawingArea().getDataXY (
				xmin, ymax,
				GRDrawingArea.COORD_DEVICE );
		GRPoint pt2 = tsgraph.getGraphDrawingArea().getDataXY (
				xmax, ymin,
				GRDrawingArea.COORD_DEVICE );

		GRLimits newdata_limits = new GRLimits ( pt1, pt2 );

		if ( _interaction_mode == INTERACTION_ZOOM ) {
			// Reset the limits to more appropriate values...
			if ( _zoom_keep_y_limits ) {
				// Set the Y limits to the maximum values...
				newdata_limits.setTopY (
				tsgraph.getMaxDataLimits().getTopY() );
				newdata_limits.setBottomY(
				tsgraph.getMaxDataLimits().getBottomY() );
			}
		}

		tsgraph.setDataLimits ( newdata_limits );
/* Do later...
		// Adjust the limits slightly if monthly or annual data since
		// the data values are plotted in the middle of the interval...
		if ( _interval_max >= TimeInterval.MONTH ) {
			// For monthly time series, data are plotted on
			// the middle of the month so adjust back 15/365...
			_data_limits.setLeftX ( _data_limits.getLeftX()- .0411);
		}
		if ( _interval_max >= TimeInterval.YEAR ) {
			// For annual time series, data are plotted on June 30
			// so adjust back 1/2 year...
			_data_limits.setLeftX ( _data_limits.getLeftX() - .5);
		}
*/
		// Call the listener (or should this happen after the paint?)...
		if ( _interaction_mode == INTERACTION_SELECT ) {
			// Just return the select information..
			if ( _listeners != null ) {
				int size = _listeners.length;
				for ( int i = 0; i < size; i++ ) {
					_listeners[i].tsViewSelect (
						tsgraph, mouse_limits,
						newdata_limits, (Vector)null );
				}
			}
		}
		else if ( _interaction_mode == INTERACTION_ZOOM ) {
			// Actually reset the data limits...
			// Only set new drawing area data limits for the main
			// graph...
			if ( !_is_reference_graph ) {
				tsgraph.setDataLimits ( newdata_limits );
				//_grda.setDataLimits ( _data_limits );
			}
			// Call external listeners to let them know that a
			// graph has been zoomed...
			if ( _listeners != null ) {
				int size = _listeners.length;
				for ( int i = 0; i < size; i++ ) {
					_listeners[i].tsViewZoom (
						tsgraph,
						mouse_limits, newdata_limits );
				}
			}
			// Apply the zoom to other graphs that need to be
			// zoomed...
			zoom ( tsgraph, mouse_limits, newdata_limits );
			// Repaint...
			// Fill in the background color.  Need this because
			// update() does not do (because of zooming)...
			clearView();
			// Before redrawing, set the data limits to the
			// plotting limits that result in the full device being
			// used.  Otherwise, mouse tracking, etc. may not
			// allow selects from outside the actual data limits...
			// First set so the plotting limits will be
			// recomputed...
			_force_redraw = true;
		}
		mouse_limits = null;
		pt1 = null;
		pt2 = null;
		newdata_limits = null;
		_rubber_banding = false;
		// Reset zoom coordinates...
		_mouse_x2 = _mouse_xprev = -1;
		repaint();
	}
}

/**
Indicate whether during initialization the user has indicated that the graph
should not continue and should be closed.  This should be called immediately
after construction in higher-level code.
@return true if the TSGraphJComponent (and the graph) should be closed due to a
user confirmation.
*/
public boolean needToClose()
{	return _need_to_close;
}

/**
Set whether during initialization the user has indicated that the graph
should not continue and should be closed.  This should be called immediately
when a problem occurs in TSGraph constructors and should be checked when
constructing this class.
@param need_to_close True if the TSGraphJComponent should be closed because of
user verification not to go on.
@return true if the TSGraphJComponent (and the graph) should be closed due to a
user confirmation.
*/
public boolean needToClose ( boolean need_to_close )
{	_need_to_close = need_to_close;
	return _need_to_close;
}

/**
Open the drawing areas and set the data limits (all are unit limits).
*/
private void openDrawingAreas ()
{	// Full page...

	_da_page = new GRJComponentDrawingArea ( this, "TSGraphJComponent.Page",
			GRAspect.FILL, null, GRUnits.DEVICE,
			GRLimits.DEVICE, null );
	_datalim_page = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_page.setDataLimits ( _datalim_page );

	// Drawing area for main title (data are just unit)...

	_da_maintitle = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.MainTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_maintitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_maintitle.setDataLimits ( _datalim_maintitle );

	// Drawing area for sub title (data are just unit)...

	_da_subtitle = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.SubTitle", GRAspect.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_subtitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_subtitle.setDataLimits ( _datalim_subtitle );

	// Drawing area for graphing...

	_da_graphs = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.Graphs",
			GRAspect.FILL, null, GRUnits.DEVICE,
			GRLimits.DEVICE, null );
	_datalim_graphs = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_graphs.setDataLimits ( _datalim_graphs );

	// Drawing area for left footer (data are just unit)...

	_da_leftfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.LeftFooter",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_leftfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_leftfoot.setDataLimits ( _datalim_leftfoot );

	// Drawing area for center footer (data are just unit)...

	_da_centerfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.CenterFooter",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_centerfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_centerfoot.setDataLimits ( _datalim_centerfoot );

	// Drawing area for right footer (data are just unit)...

	_da_rightfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.CenterFooter",
			GRAspect.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_rightfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_rightfoot.setDataLimits ( _datalim_rightfoot );
}

/**
Update the TSGraphJComponent visible image, render to printer (set _printing=true before calling),
or render to SVG file (set __printForSVG=true before calling).
@param g Graphics instance either from the component event handling or from an
explicit printView() call.
*/
public void paint ( Graphics g )
{	String routine = "TSGraphJComponent.paint";
	int dl = 10;
	boolean resizing = false;

	_double_buffering = true;

	if ( g == null ) {
		Message.printDebug( 1, "", "Null Graphics in paint()" );
		return;
	}

	// Print some messages so we know what the paint is doing...

	if ( Message.isDebugOn ) {
		if (_printing ) {
			Message.printDebug ( 1, routine, _gtype + "Printing graph(s)..." );
		}
		else if ( __paintForSVG ) {
	         Message.printDebug ( 1, routine, _gtype + "Saving graph(s) to SVG file..." );
	    }
		else if ( _is_reference_graph ) {
			Message.printDebug ( 1, routine, _gtype + "Painting reference graph..." );
		}
		else {
		    Message.printDebug ( 1, routine, _gtype + "Painting main graph..." );
		}
	}

	try { // Main try...

	// The following will be executed at initialization.  The code is here
	// because a valid Graphics is needed to check font sizes, etc.

	if ( _da_page == null ) {
		//
		// This will be executed the first paint.
		//
		// The graphics will be for the screen (it may actually be
		// set for an image below but setting it here should be ok
		// for initialization).  Once the graphics is set, font sizes
		// can be determined.
		_graphics = (Graphics2D)g;
		// Now set the drawing limits these are necessary to compute
		// the labels and are for the most part independent of data limits...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Set drawing limits..." );
		}
		setDrawingLimits ();
		setGraphDrawingLimits ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			_gtype + "Set initial graph drawing limits to " + _drawlim_graphs.toString() );
		}
	}

	if ( _waiting ) {
		return;
	}

	// If rubber-banding, can do before anything else and return...

	if ( _rubber_banding ) {
		if (_double_buffering && _buffer != null) {
			g.drawImage(_buffer, 0, 0, this);
		}
	
		g.setColor ( _rubber_band_color );
		g.setXORMode ( _background_color );
		int xmin, xmax, ymin, ymax;
		// Now draw the new rectangle (still in XOR mode)...
		// If _mouse_x2 = -1, the code is getting called from
		// mouseReleased() and only the previous rectangle needs to be cleared...
		if ( _mouse_x2 != -1 ) {
			if ( _mouse_x1 < _mouse_x2 ) {
				xmin = _mouse_x1;
				xmax = _mouse_x2;
			}
			else {
			    xmin = _mouse_x2;
				xmax = _mouse_x1;
			}
			if ( _mouse_y1 < _mouse_y2 ) {
				ymin = _mouse_y1;
				ymax = _mouse_y2;
			}
			else {
			    ymin = _mouse_y2;
				ymax = _mouse_y1;
			}
			g.drawRect ( xmin, ymin, (xmax - xmin),(ymax - ymin) );
			// Save the previous coordinates...
			_mouse_xprev = _mouse_x2;
			_mouse_yprev = _mouse_y2;
		}
		// Done drawing.  Reset paint mode to normal just in case we
		// change code later to not return...
		g.setPaintMode ();
		return;
	}

	// See if the graphics is for printing or screen and make a few
	// adjustments accordingly.  Set the base class _printing flag if necessary...

	GRLimits new_draw_limits = null;
	if ( _printing ) {
		// Set in the printView method
		// Bounds will have been set in printView...
		new_draw_limits = new GRLimits();
						// Gets the page size...
		if ( Message.isDebugOn ) {
		    Message.printDebug(1, "", "PB: " + _printBounds);
		}

		// get the drawing limits from the bounds set by the
		// print job.  These limits are the valid extents on the
		// paper to which the print job can print.  These take
		// into account the margins the user set up around
		// the page.  Thers is no need to worry at this point
		// about landscape versus portrait.  When the Print Job
		// is told that the paper will be Landscape, it automatically
		// knows formats the output for a landscape paper.  All 
		// developers need to know is that the page is longer 
		// than it is wide.  For RTi's purposes the GR pixel (0, 0)
		// is still in the lower-left corner.
		new_draw_limits.setLeftX(_printBounds.getLeftX());
		new_draw_limits.setTopY(_printBounds.getTopY());
		new_draw_limits.setRightX( _printBounds.getRightX());
		new_draw_limits.setBottomY(_printBounds.getBottomY());
		if ( Message.isDebugOn ) {
		    Message.printDebug(1, "", "NL: " + new_draw_limits);
		}

		// set the limits for drawing to the printed page --
		// the old limits will be reset after the print() call
		// is done
		setLimits(new_draw_limits);

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Using print graphics." );
		}
		_graphics = (Graphics2D)g;
		// Base class...
	}
	else {
	    // Drawing to screen or SVG file.
	    // Gets the component size...
		// GR handles this...
		new_draw_limits = getLimits(true);
		// Need the full device limits elsewhere...
		_bounds = getBounds ();
		// Now set the drawing limits to the bounds...
		new_draw_limits.setLeftX ( 0.0 );
		new_draw_limits.setBottomY ( 0.0 );
		new_draw_limits.setRightX ( new_draw_limits.getRightX() );
		new_draw_limits.setTopY ( new_draw_limits.getTopY() );
		// Need the full device limits elsewhere...
		if ( Message.isDebugOn ) {
		    if ( __paintForSVG ) {
		        Message.printDebug ( 1, routine, _gtype + "Using SVG graphics." );
		    }
		    else {
		        Message.printDebug ( 1, routine, _gtype + "Using display graphics." );
		    }
		}
		if ( !_double_buffering || __paintForSVG ) {
			// Use this graphics...
			_graphics = (Graphics2D)g;
		}
		// Base class...
	}

	// See if the drawing limits need to be reset for drawing.
	// If the size has changed and double-buffering, create a new
	// image for the off-screen buffer...

	if ( _first_paint || !_drawlim_page.equals(new_draw_limits) ) {
		// Set to the new drawing limits for the redraw.  This will
		// cause a recompute of the data used for scaling.
		//setDrawingLimits ();
		resizing = true;
		// If double buffering, create a new image...
		if ( ((_buffer == null) || _double_buffering) && !_printing && !__paintForSVG) {
			// This needs to be the size of the full component, NOT
			// the drawing limits!
			// Clean up since images can take a lot of memory...
			_buffer = null;
			System.gc();
			if ( _external_Image != null ) {
				// Image was created external to this class...
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, "Using external Image from properties." );
				}
				System.out.flush();
				_buffer = _external_Image;
			}
			else {
			    // Image is maintained in this class...
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Creating new image because of resize." );
				}
				// Base class method...
				setupDoubleBuffer(0, 0, _bounds.width, _bounds.height);
			}
		}
	}
	else {
	    // component size has not changed...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, _gtype + "Device size has not changed." );
		}
		resizing = false;	// All that needs to be done is to
					// copy the image to the current
					// screen.  However, if this is the
					// first time, need to execute the
					// following draw code at least once...
	}
	if ( _double_buffering && !_printing && !__paintForSVG) {
		// Use the image graphics...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Using image graphics." );
		}
		_graphics = (Graphics2D)(_buffer.getGraphics());
	}

	// Do the following the first time so that the drawing can use the
	// graphics for font-based sizing.

	if ( _first_paint || resizing ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, _gtype + "Device size has changed." );
		}
		// Resize the drawing area by setting its drawing limits...
		setDrawingLimits ();
		setGraphDrawingLimits ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, _gtype + routine, "Setting graph drawing limits to: " + _da_graphs );
		}
		// Now clear the view so that drawing occurs on a clean background...
		clearView ();
	}

	// Redraw the graph(s) if any of the following conditions apply:
	//
	// * not double buffering (redraw every time)
	// * a draw is forced (because of changes in the views)
	// * printing has been requested (and need to redraw given page
	//   extents, etc.)
	// * double-buffering is on and a resize has occurred.

	if ( _force_redraw || _printing || __paintForSVG || !_double_buffering || resizing ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			_gtype + "Drawing graph (_force_redraw=" +
			_force_redraw + " resizing=" + resizing + ")..." );
		}
		try {
		    JGUIUtil.setWaitCursor(_parent, true );
			// If the first time, force a zoom out to synchronize
			// the data limits on all the graphs that are at the
			// same zoom level (tried this above but seems to work
			// when called from here)...
			if ( _first_paint ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, "", _gtype + "Zooming out the first time to sync graphs." );
				}
				zoomOut ( false );
			}
			// Draw the titles, footers, etc.
			drawTitles ();
			drawFooters ();
			if ( IOUtil.testing() ) {
				// Comment out unless we are reworking something
				//drawDrawingAreas();
			}
			// Now loop through and draw each graph on the page.
			// Only graph enabled subproducts and if a reference
			// graph only graph the corresponding subproduct.
			int size = _tsgraphs.size();
			TSGraph tsgraph;
			String prop_val;
			for ( int isub = 0; isub < size; isub++ ) {
				// If the graph is disabled, do not even
				// draw...
				prop_val = _tsproduct.getLayeredPropValue ( "Enabled", isub, -1, false );
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, "", _gtype + "Graph ["+ isub + "] is " + prop_val );
				}
				if ( prop_val.equalsIgnoreCase("false") ) {
					continue;
				}
				if ( _is_reference_graph &&	(isub != _reference_sub) ) {
					continue;
				}
				tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
				if ( tsgraph == null ) {
					continue;
				}
				// Debug whether graphs are enabled...
				//Message.printStatus ( 1, "", _gtype +
				// "Graph ["+ isub + "] is " +
				// _tsproduct.getLayeredPropValue(
				//"MainTitleString", isub, -1, false) );
				tsgraph.paint ( _graphics );
				if (_is_reference_graph) {
					continue;
				}
			}
			JGUIUtil.setWaitCursor(_parent, false );
		}
		catch ( Exception e ) {
			JGUIUtil.setWaitCursor(_parent, false );
			Message.printWarning ( 1, routine, "Error drawing graph(s)." );
			Message.printWarning ( 2, routine, e );
		}
		_force_redraw = false;
	}

	// Finally, if double buffering and not printing, copy the image from
	// the buffer to the component...

	if ( _double_buffering && !_printing && !__paintForSVG ) {
		// The graphics is for the display...
		// Draw to the screen...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Copying internal image to display." );
		}
		g.drawImage ( _buffer , 0, 0,  this );
		// Only do this if double buffering to screen because that is
		// the only time the graphics is created locally...
		// ?? _graphics.dispose();
	}
	new_draw_limits = null;

	} // Main try wrapped around entire method.
	catch ( Exception e ) {
		Message.printWarning ( 2, _gtype + routine, e );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "...done painting TSGraphJComponent." );
	}
	// Should always get to here the first time...
	_first_paint = false;
	routine = null;
	if (_printing) {
		_printing = false;
	}
	if (__paintForSVG) {
	    __paintForSVG = false;
	}
	/*
	if (IOUtil.testing()) {
	_da_page.setColor ( GRColor.cyan );
	// Reference and main...
	GRDrawingAreaUtil.drawRectangle ( _da_graphs,
				_datalim_graphs.getLeftX(),
				_datalim_graphs.getBottomY(),
				_datalim_graphs.getWidth(),
				_datalim_graphs.getHeight() );
	}
	*/
}

/**
Print method from the Printable interface.
@param g the Graphics context to use for drawing the printed graphics.
@param pageFormat the PageFormat to use for printing.
@param the pageIndex (0-based) of the page to print.
@return whether a page could be printed (PAGE_EXISTS) or not (NO_SUCH_PAGE).
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
	if (pageIndex > 0) {
		// print() is called for every page to be printed.  The graph
		// will only have a 0th page (the 1 page to be printed), so
		// when this method is called for any page above one, return
		// the standard NO_SUCH_PAGE value (inherited from Printable).
		return NO_SUCH_PAGE;
	}
	else {
		// The following is a debug utility that prints information
		// about the pageformat (its imageable area, etc) on 
		// status level 1
//		PrintUtil.dumpPageFormat(pageFormat);

		// get the printing bounds of the image.
/*
	In Java pixel terms, given a page like this:

	+---------------------------------------+
	|        Top Margin                     |
	|                                       |
	|   +-----[IY]---------------------+    |
	| L |                              | R  |
	| e |                              | i  |
	| f |   Printable page area        | g  |
	| t |                              | h  |
	|   |                              | t  |
	| M |                              |    |
	| a |                              | M  |
	| r |                              | a  |
	| g |                              | r  |
	| i |                              | g  |
	| n |                              | i  |
	|   |                              | n  |
	|   |                              |    |
	|  [IX]                          [IW]  [W]
	|   |                              |    |
	|   +----[IH]----------------------+    |
	|                                       |
	|                                       |
	|                                       |
	|         Bottom Margin                 |
	|                                       |
	|                                       |
	|                                       |
	+-------[H]-----------------------------+
	
	[IX] - is the number of pixels from the side of the paper to the very 
		first part of the paper upon which printing can occur.  It is 
		called the "Imageable X".  Every pixel to the left of Imageable 
		X is in the left-side margin.
	
	[IY] - is the number of pixels from the top of the paper to the very 
		first part of the paper upon which printing can occur.  It is 
		called the "Imageable Y".  Every pixel above Imageable Y is in 
		the top margin.
	
	[IW] - is the number of horizontal pixels that can actually be printed.
		It is the "Imageable Width" and is the number of pixels 
		between the left and right margins.
	
	[IH] - is the number of vertical pixels that can actually be printed.  
		It is the "Imageable Height" and is the number of pixels 
		between the top and bottom margins.
	
	[W] - is the total paper width in pixels.  
		[W] - ([IW] + [IX]) == the size of the right-side margin
	
	[H] - is the total paper height in pixels.  
		[H] - ([IH] + [IY]) == the size of the bottom margin
	*/
	
	/*
	GRLimits parameters are in the order:
	Bottom-left X, Bottom-left Y, Top-right X, Top-right y

	So the printable limits above (ImageableX, etc, which go from 0 at the 
	top-left instead of bottom-left) need to be translated to limits in the 
	GR mode.  The only ones that will actually change are the Y values, as 
	X still goes from 0 at the left to MAX at the right.
	
	Bottom-left X == Imageable X
	Top-right X == Imageable X + Imageable Width
	
	Bottom-left Y == Total Height - (Imageable Y + Imageable Height)
	Top-right Y == Total Height - Imageable Y
*/

		_printBounds = new GRLimits(
			(int)pageFormat.getImageableX(),
			(int)(pageFormat.getHeight() 
				- (pageFormat.getImageableY()
					+ pageFormat.getImageableHeight())),
			(int)(pageFormat.getImageableWidth()
				+ pageFormat.getImageableX()),
			(int)(pageFormat.getHeight() 
				- pageFormat.getImageableY()));

	Message.printDebug(1, "", "PrintBounds after print selection:\n"
		+ _printBounds);

		// set the printing flag to true
		_printing = true;

/*
	The following is necessary because of some errors that can arise when
	printing with RTi-style coordinates in the Java-style coordinate system.
	
	In order to explain, imagine a theoretical piece of paper that is
	5 inches across by 4 inches down, into which will be printed a graph
	that is 3 inches across by 2 inches down:
	
	/--+--+--+--+--+
	| (margins)    |
	+  +--+--+--+  +
	|  |        |  |
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|              |
	\--+--+--+--+--+
	
	Java always numbers it coordinates from the upper-left down (from 
	the '/'). RTi coordinates go from the bottom-left up (from the '\').
	
	When a pageformat is used in the print() method, it sets up the 
	boundaries for RTi coordinates, so that the extent of the left and 
	bottom margins are known.  Margins are enforced by the internal Java 
	printing, so that whatever the imageable area of the page format is set 
	to will be the only parts of the page to which something can be drawn.  
	A drawing area is sized to accomodate the margins, and the graphic is 
	drawn within this, like so:
	
	...+--+--+--+
	.  |        |
	.  + (graph)+
	.  |        |
	.  +--+--+--+
	.(margins)  .
	.............
	
	The problem is that the highest RTi pixel is equal to the 0th Java 
	pixel, so that when the above graphic is drawn to a piece of paper, it 
	will be drawn like this:
	
	/--+--+--+--+--+
	|  |        |  |
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|           .  |
	+............  |
	|              |
	\--+--+--+--+--+
	
	Something needs to be done to take into account the upper margin.  The 
	extent of the graphic cannot simply be increased, because then the 
	graph will expand upward to fill the available space.
	
	There are two options.
	
	1) Create a blank drawing area only during printing and use it as a 
	spacer above the graph:
	
	/--+--+--+--+--+
	|(draw area)|  |     Where (draw area) is a drawing area that is high 
	+--+--+--+--+  +     enough (width doesn't matter in this case) to 
	|  |        |  |     put enough space so that the graph is not drawn 
	+  + (graph)+  +     over the margins of the printed page.
	|  |        |  |
	+  +--+--+--+  +
	|              |
	\--+--+--+--+--+
	   
	2) Use Java2D's Graphics2D class and translate all of the drawing down
	   to not draw over the top margin.

	From this:
	/--+--+--+--+--+
	|  |        |  |
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|           .  |
	+............  |
	|              |
	\--+--+--+--+--+
	
	... to this ...
	
	/--+--+--+--+--+
	| (translated) |      Where (translated) is the amount by which the
	+--+--+--+--+  +      drawing code was shifted up in Java pixels 
	|  |        |  |      and down in RTi pixels.
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|              |
	\--+--+--+--+--+
	
	Of the two, translation is much easier, requiring only three lines of 
	code and no special code within the paint() method to handle special
	circumstances in printing.
*/

		Graphics2D g2d = (Graphics2D)g;

		// the imageable Y is the first Java pixel (going DOWN)
		// at which drawing can occur.  It is therefore the 
		// LAST RTi pixel at which drawing can occur.  The
		// bottom margin of the graph is already enforced by 
		// setting the size of the drawing area.  This makes sure
		// the graph is shifted to fit in the printable area
		// of the page.
		// See all the above comments.
		double transY = pageFormat.getImageableY();
		g2d.translate(0, transY);
		paint(g2d);

		_printing = false;
		_printBounds = null;
		return PAGE_EXISTS;
	}
}

/**
Print the graph.  The user is prompted to select a printer, etc..
*/
public void printGraph() {
	try {
		// For all the below, additionally check the docs for
		// PrintUtil.java.
		
		// create a page format object for printing to letter paper
		PageFormat pageFormat = PrintUtil.getPageFormat("letter");

		// sets the orientation for a page format
		PrintUtil.setPageFormatOrientation(pageFormat,
			PrintUtil.LANDSCAPE);
		
		// set the margins for the page format and pop up a dialog 
		// box in which the user can change the margins
		PrintUtil.setPageFormatMargins(pageFormat, .5, .5, .5, .5);

		// print the job by popping up a dialog from which users can
		// select the printer on which to print and other information
		PrintUtil.print(this, pageFormat);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

/**
Refresh the plot based on changes in properties.  This is typically called
from outside code, e.g., when time series have been enabled/disabled in the
TSViewPropertiesGUI.  The drawing areas ARE recomputed.
*/
public void refresh ()
{	refresh ( true );
}

/**
Refresh the plot based on changes in properties.  This is typically called
from outside code, e.g., when time series have been enabled/disabled in the
TSViewPropertiesGUI.
@param recompute_drawing_limits If true, the drawing areas will be recomputed.
This is only necessary when the component size changes or axes are turned
on/off.  If zooming, the drawing
areas can remain the same size (this assumes that a reasonable estimate of the
maximum data value has been used to set the y-axis label widths).
*/
public void refresh ( boolean recompute_drawing_limits )
{	clearView ();
	// This will recompute drawing limits for the main component and the
	// TSGraphs.
	if ( recompute_drawing_limits ) {
		setDrawingLimits ();
		setGraphDrawingLimits ();
	}
	_force_redraw = true;
	repaint ();
}

/**
Sets up the Vector of TSGraph objects again based on the provided TSProduct.
This ensures that the graphs match exactly the properties in the TSProduct that
is being manipulated in the JFrame.  Since these properties can be changed in
the JFrame, in the layout component, and possibly in other places, it's safest
to simply re-read the properties and rebuild all the graphs completely when 
major changes occur.
@param product the TSProduct from which to read TSGraph information.  This will
most likely be the product that's already resident in memory, but not 
necessarily. 
*/
public void reinitializeGraphs(TSProduct product) {
	// if any graphs lack start and end dates (i.e., they're brand new
	// and lack any time series), pull out a start and end date from any
	// of the other graphs and use it, so that zoom outs work correctly
	DateTime end = null;
	DateTime maxEnd = null;
	DateTime start = null;
	DateTime maxStart = null;
	DateTime temp = null;
	TSGraph g = null;

	// Find the latest end date and the earliest start date from the
	// graphs.

	for (int i = 0; i < _tsgraphs.size(); i++) {
		g = (TSGraph)_tsgraphs.elementAt(i);
		if (g.getEndDate() != null) {
			temp = g.getEndDate();
			if (end == null || end.lessThanOrEqualTo(temp)) {
				end = temp;
			}
		}
		if (g.getStartDate() != null) {
			temp = g.getStartDate();
			if (start == null || temp.lessThan(start)) {
				start = temp;
			}
		}
	}

	_tsproduct = product;
	_tslist = _tsproduct.getTSList();

	// Find the latest end date and the earliest start date from the
	// time series

	TS ts = null;
	int size = _tslist.size();
	for (int i = 0; i < size; i++) {
		ts = (TS)_tslist.elementAt(i);
		temp = ts.getDate1();
		if (maxStart == null 
		    || (temp != null && temp.lessThan(maxStart))) {
			maxStart = temp;
		}

		temp = ts.getDate2();
		if (maxEnd == null 
		    || (temp != null && maxEnd.lessThan(temp))) {
			maxEnd = temp;
		}
	}
	
	Vector v = determineDataLimits();
	
	_tsgraphs = createTSGraphsFromTSProduct(_tsproduct,
		_display_props, _tslist, 
		new GRLimits(0.0,0.0, getWidth(), getHeight()));
	checkTSProductGraphs(_tsproduct, _tsgraphs);
	
	setDrawingLimits();
	setGraphDrawingLimits();
	resetGraphDataLimits(v);
	_force_redraw = true;
	clearView();
	repaint();

	if (end == null && start == null) {
		return;
	}
	
	// Set the dates into the graphs.
	
	// Because the graphs might be zoomed in, their start and end dates 
	// might compose a smaller range than the start and end dates from 
	// the time series.  For this reason, the max start and max end dates
	// are set from the time series (above) and the current graph zoom
	// dates are set from the current graph dates (above).
	
	for (int i = 0; i < _tsgraphs.size(); i++) {
		g = (TSGraph)_tsgraphs.elementAt(i);
		if (!g.isReferenceGraph()) {
			g.setEndDate(end);
			g.setMaxEndDate(maxEnd);
			g.setStartDate(start);
			g.setMaxStartDate(maxStart);
			g.setComputeWithSetDates(true);
			g.computeDataLimits(false);
		}
	}
}

/**
Resets the data limits for each graph during a call to reinitializeGraphs().
@param v a Vector, each element of which is:<br>
A Vector with data for the graph numbered the same as this element, 
each element of which is:<br>
0 - an integer (the number of time series in the graph)<br>
1 - a Vector of Strings (each of which is an id of a time series in the graph)
<br>
2 - data limits for the graphs<br>
*/
private void resetGraphDataLimits(Vector v) {
	int tsCount = 0;
	int vCount = 0;
	TS ts = null;
	TSGraph graph = null;
	Vector ids = null;
	Vector tslist = null;
	for (int i = 0; i < _tsgraphs.size(); i++) {
		graph = (TSGraph)_tsgraphs.elementAt(i);
		tslist = graph.getTSList();		
		ids = new Vector();
		for (int j = 0; j < tslist.size(); j++) {
			ts = (TS)(tslist.elementAt(j));
			if (ts == null) {
				ids.add("null");
			}
			else {
				ids.add(ts.getIdentifierString());
			}
		}
		ids = StringUtil.sortStringList(ids);		
		tsCount = tslist.size();
		for (int j = 0; j < v.size(); j++) {		
			Vector dataVector = (Vector)v.elementAt(j);
			Integer I = (Integer)dataVector.elementAt(0);
			vCount = I.intValue();
			if (vCount == tsCount) {
				Vector vids = (Vector)dataVector.elementAt(1);
				if (stringVectorsAreEqual(ids, vids)) {
					GRLimits dataLimits = (GRLimits)
						dataVector.elementAt(2);
					graph.setDataLimits(dataLimits);
				}
			}
		}
	}
}

/**
Save the graph to an SVG file.  This is essentially equivalent to printing,
but use an SVG graphics driver instead.
@param path Path to SVG file to save.  The path is not adjusted and therefore
should generally be specified as absolute and with the *.svg extension.
*/
public void saveAsSVG ( String path )
throws FileNotFoundException, IOException
{
    Graphics g = TSGraphJComponent_SaveAsSVG.createGraphics();

    // Render into the SVG Graphics2D implementation.
    __paintForSVG = true;
    paint(g);
    __paintForSVG = false;

    TSGraphJComponent_SaveAsSVG.saveGraphics(g,path);
    
}

/**
Scroll the current zoom group a multiple of the visible page.
@param pages Number of pages to scroll (-1.0 is one page left, 1.0 is one page
right).
@param notify_listeners If true, the tsViewZoom() method is called for the
new data limits.
*/
public void scroll ( double pages, boolean notify_listeners )
{	// Loop through the zoom levels and zoom each group of graphs to the
	// maximum data extent within the zoom group.  Need a concept of an
	// active zoom group...

	if ( pages == 0.0 ) {
		// Don't need to do anything...
		return;
	}

	TSGraph tsgraph;
	GRLimits max_data_limits = null;
	String prop_value;
	int zoom_group = 0;
	int num_zoom_groups = _tsproduct.getNumZoomGroups();
	GRLimits current_data_limits = null;
	GRLimits new_data_limits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
		// Loop through and determine the maximum and current limits for
		// the zoom group (zoom groups are 1...N)...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			if ( max_data_limits == null ) {
				max_data_limits = new GRLimits(
					tsgraph.getMaxDataLimits() );
			}
			else {	max_data_limits =
				max_data_limits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " +
			//max_data_limits );
		// Now loop through again and set the data limits for
		// graph in the zoom group...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			prop_value = _tsproduct.getLayeredPropValue (
				"ZoomEnabled", isub, -1, false );
			if ( !prop_value.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the
			// same, don't do anything...
			current_data_limits = tsgraph.getDataLimits();
			if ( max_data_limits.equals( current_data_limits) ) {
				continue;
			}
			// Else, reset the visible window to the end of the
			// period with a data width equal to the current
			// limits.  Start by copying the current limits...
			new_data_limits = new GRLimits ( current_data_limits );
			if ( pages > 0.0 ) {
				// First reset the right X value...
				new_data_limits.setRightX (
					current_data_limits.getRightX() +
					pages*current_data_limits.getWidth() );
				// If the right X is past the end, set it to the
				// end...
				if (	new_data_limits.getRightX() >
					max_data_limits.getRightX() ) {
					new_data_limits.setRightX (
					max_data_limits.getRightX() );
				}
				// Now set the left edge relative to the right..
				new_data_limits.setLeftX (
					new_data_limits.getRightX() -
					current_data_limits.getWidth() );
				// Do a final check against the overall limits..
				if (	new_data_limits.getLeftX() <
					max_data_limits.getLeftX() ) {
					new_data_limits.setLeftX (
					max_data_limits.getLeftX() );
				}
			}
			else {	// First reset the left X value (pages is
				// negative so add the second term)...
				new_data_limits.setLeftX (
					current_data_limits.getLeftX() +
					pages*current_data_limits.getWidth() );
				// If the left X is past the end, set it to the
				// end...
				if (	new_data_limits.getLeftX() <
					max_data_limits.getLeftX() ) {
					new_data_limits.setLeftX (
					max_data_limits.getLeftX() );
				}
				// Now set the right edge relative to the left..
				new_data_limits.setRightX (
					new_data_limits.getLeftX() +
					current_data_limits.getWidth() );
				// Do a final check against the overall limits..
				if (	new_data_limits.getRightX() >
					max_data_limits.getRightX() ) {
					new_data_limits.setRightX (
					max_data_limits.getRightX() );
				}
			}
			tsgraph.setDataLimits ( new_data_limits );
			if ( notify_listeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so
				//	need to notify all other graphs in
				//	the same zoom group (currently assume
				//	all).
				//
				// 2)	This is not a reference graph.
				//	Assume for now it is in the same
				//	zoom group as the reference window
				//
				// Notify the TSViewListeners that we are
				// zooming.  This is currently only done between
				// a reference graph in this component and
				// another graph that is managing the notify the
				// reference graph...
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but
					// a cast is done later so don't pass
					// null...
					_listeners[il].tsViewZoom (
					tsgraph, new GRLimits(),
					new_data_limits );
				}
			}
		}
	}
	// Refresh the component...
	refresh();
}

/**
Scroll the current zoom group to the end of the available data.  If zoomed out,
no action is taken.
@param notify_listeners If true, the tsViewZoom() method is called for the
new data limits.
*/
public void scrollToEnd ( boolean notify_listeners )
{	// Loop through the zoom levels and zoom each group of graphs to the
	// maximum data extent within the zoom group.  Need a concept of an
	// active zoom group...

	TSGraph tsgraph;
	GRLimits max_data_limits = null;
	String prop_value;
	int zoom_group = 0;
	int num_zoom_groups = _tsproduct.getNumZoomGroups();
	GRLimits current_data_limits = null;
	GRLimits new_data_limits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
		// Loop through and determine the maximum and current limits for
		// the zoom group (zoom groups are 1...N)...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			if ( max_data_limits == null ) {
				max_data_limits = new GRLimits(
					tsgraph.getMaxDataLimits() );
			}
			else {	max_data_limits =
				max_data_limits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " +
			//max_data_limits );
		// Now loop through again and set the data limits for
		// graph in the zoom group...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			prop_value = _tsproduct.getLayeredPropValue (
				"ZoomEnabled", isub, -1, false );
			if ( !prop_value.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the
			// same, don't do anything...
			current_data_limits = tsgraph.getDataLimits();
			if ( max_data_limits.equals( current_data_limits) ) {
				continue;
			}
			// Else, reset the visible window to the end of the
			// period with a data width equal to the current
			// limits.  Start by copying the maximum limits...
			new_data_limits = new GRLimits ( max_data_limits );
			// Now reset the end X value...
			new_data_limits.setLeftX (
				max_data_limits.getRightX() -
				current_data_limits.getWidth() );
			tsgraph.setDataLimits ( new_data_limits );
			if ( notify_listeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so
				//	need to notify all other graphs in
				//	the same zoom group (currently assume
				//	all).
				//
				// 2)	This is not a reference graph.
				//	Assume for now it is in the same
				//	zoom group as the reference window
				//
				// Notify the TSViewListeners that we are
				// zooming.  This is currently only done between
				// a reference graph in this component and
				// another graph that is managing the
				// notify the reference graph...
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but
					// a cast is done later so don't pass
					// null...
					_listeners[il].tsViewZoom (
					tsgraph, new GRLimits(),
					new_data_limits );
				}
			}
		}
	}
	// Refresh the component...
	refresh();
}

/**
Scroll the current zoom group to the start of the available data.  If zoomed
out, no action is taken.
@param notify_listeners If true, the tsViewZoom() method is called for the
new data limits.
*/
public void scrollToStart ( boolean notify_listeners )
{	// Loop through the zoom levels and zoom each group of graphs to the
	// maximum data extent within the zoom group.  Need a concept of an
	// active zoom group...

	TSGraph tsgraph;
	GRLimits max_data_limits = null;
	String prop_value;
	int zoom_group = 0;
	int num_zoom_groups = _tsproduct.getNumZoomGroups();
	GRLimits current_data_limits = null;
	GRLimits new_data_limits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
		// Loop through and determine the maximum and current limits for
		// the zoom group (zoom groups are 1...N)...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			if ( max_data_limits == null ) {
				max_data_limits = new GRLimits(
					tsgraph.getMaxDataLimits() );
			}
			else {	max_data_limits =
				max_data_limits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " +
			//max_data_limits );
		// Now loop through again and set the data limits for
		// graph in the zoom group...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			prop_value = _tsproduct.getLayeredPropValue (
				"ZoomEnabled", isub, -1, false );
			if ( !prop_value.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the
			// same, don't do anything...
			current_data_limits = tsgraph.getDataLimits();
			if ( max_data_limits.equals( current_data_limits) ) {
				continue;
			}
			// Else, reset the visible window to the start of the
			// period with a data width equal to the current
			// limits.  Start by copying the maximum limits...
			new_data_limits = new GRLimits ( max_data_limits );
			// Now reset the end X value...
			new_data_limits.setRightX (
				max_data_limits.getLeftX() +
				current_data_limits.getWidth() );
			tsgraph.setDataLimits ( new_data_limits );
			if ( notify_listeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so
				//	need to notify all other graphs in
				//	the same zoom group (currently assume
				//	all).
				//
				// 2)	This is not a reference graph.
				//	Assume for now it is in the same
				//	zoom group as the reference window
				//
				// Notify the TSViewListeners that we are
				// zooming.  This is currently only done between
				// a reference graph in this component and
				// another graph that is managing the
				// notify the reference graph...
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but
					// a cast is done later so don't pass
					// null...
					_listeners[il].tsViewZoom (
					tsgraph, new GRLimits(),
					new_data_limits );
				}
			}
		}
	}
	// Refresh the component...
	refresh();
}

/**
Set the drawing limits for all drawing areas based on properties and window
size.  The font sizes are considered when sizing the drawing areas.
*/
void setDrawingLimits()
{	double buffer = 2.0;	// Buffer around drawing areas (helps separate
				// things and also makes it easier to see
				// drawing areas when in debug mode)
	// Figure out dimensions up front...

	// Main and sub-titles and footers will have zero size unless the titles
	// are specified...

	double maintitle_height = 0.0;
	String maintitle_string = _tsproduct.getLayeredPropValue (
				"MainTitleString", -1, -1, false );

	if ( (maintitle_string != null) && !maintitle_string.equals("") ) {
		// Get the text extents and set the height based on that...
		String maintitle_font = _tsproduct.getLayeredPropValue (
				"MainTitleFontName", -1, -1, false );
		String maintitle_fontsize = _tsproduct.getLayeredPropValue (
				"MainTitleFontSize", -1, -1, false );
		String maintitle_fontstyle = _tsproduct.getLayeredPropValue (
				"MainTitleFontStyle", -1, -1, false );
		GRDrawingAreaUtil.setFont (
			_da_maintitle, maintitle_font, maintitle_fontstyle,
			StringUtil.atod(maintitle_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_maintitle, maintitle_string,
				GRUnits.DEVICE );
		maintitle_height = text_limits.getHeight();
		text_limits = null;
	}

	double subtitle_height = 0.0;
	String subtitle_string = _tsproduct.getLayeredPropValue (
				"SubTitleString", -1, -1, false );

	if ( (subtitle_string != null) && !subtitle_string.equals("") ) {
		// Get the text extents and set the height based on that...
		String subtitle_font = _tsproduct.getLayeredPropValue (
				"SubTitleFontName", -1, -1, false );
		String subtitle_fontsize = _tsproduct.getLayeredPropValue (
				"SubTitleFontSize", -1, -1, false );
		String subtitle_fontstyle = _tsproduct.getLayeredPropValue (
				"SubTitleFontStyle", -1, -1, false );
		GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font,
			subtitle_fontstyle, StringUtil.atod(subtitle_fontsize));
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( 
				_da_subtitle, subtitle_string,
				GRUnits.DEVICE );
		subtitle_height = text_limits.getHeight();
		text_limits = null;
	}

	// Get the window limits...

	GRLimits window_limits = null;
	if ( _printing ) {
		window_limits = new GRLimits();
						// Gets the page size...
		window_limits.setLeftX(_printBounds.getLeftX());
		window_limits.setBottomY(_printBounds.getBottomY());
		window_limits.setRightX(_printBounds.getRightX());
		window_limits.setTopY(_printBounds.getTopY());
		Message.printDebug(1, "", "PB: " + _printBounds);
		Message.printDebug(1, "", "WL: " + window_limits);
	}
	else {	window_limits = getLimits ( true );
	}

	_drawlim_page = new GRLimits ( window_limits );

	if (_printing) {
		Message.printDebug(1, "", "DL: " + _drawlim_page);
	}
	window_limits = null;

	// Set the drawing limits for the graph based on the dimensions of the
	// other drawing areas...

	if ( _bounds == null ) {
		_bounds = getBounds();
		//checkAndSetBounds();
	}

	// Set the drawing limits based on what was determined above...

	// Drawing limits for the title and subtitle...

	if ( maintitle_height == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_maintitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			_drawlim_page.getTopY(),
			(_drawlim_page.getRightX() - buffer),
			_drawlim_page.getTopY() );
	}
	else {	
		_drawlim_maintitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			(_drawlim_page.getTopY() - buffer - maintitle_height),
			(_drawlim_page.getRightX() - buffer),
			(_drawlim_page.getTopY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.setDrawingLimits",
		_gtype + "Main title drawing limits are: " +
		_drawlim_maintitle.toString() );
	}

	if ( subtitle_height == 0.0 ) {
		// Zero height drawing area place holder...
		_drawlim_subtitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			_drawlim_maintitle.getBottomY(),
			(_drawlim_page.getRightX() - buffer),
			_drawlim_maintitle.getBottomY() );
	}
	else {	_drawlim_subtitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			(_drawlim_maintitle.getBottomY() - buffer -
			subtitle_height),
			(_drawlim_page.getRightX() - buffer),
			(_drawlim_maintitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.setDrawingLimits",
		_gtype + "Sub title drawing limits are: " +
		_drawlim_subtitle.toString() );
	}

	// Graph drawing area (always what is left)...

	if ( _is_reference_graph ) {
		_drawlim_graphs = new GRLimits (
			_drawlim_page.getLeftX(),
			_drawlim_page.getBottomY(),
			_drawlim_page.getRightX(),
			_drawlim_page.getTopY() );
	}
	else {	_drawlim_graphs = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			(_drawlim_page.getBottomY() + buffer),
			(_drawlim_page.getRightX() - buffer),
			(_drawlim_subtitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.setDrawingLimits",
		_gtype + "Graph drawing limits are: " +
		_drawlim_graphs.toString() );
	}

	// Now set in the drawing areas...

	if ( (_da_maintitle != null) && (_drawlim_maintitle != null) ) {
		_da_maintitle.setDrawingLimits ( _drawlim_maintitle,
			GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_subtitle != null) && (_drawlim_subtitle != null) ) {
		_da_subtitle.setDrawingLimits ( _drawlim_subtitle,
			GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_graphs != null) && (_drawlim_graphs != null) ) {
		_da_graphs.setDrawingLimits ( _drawlim_graphs, GRUnits.DEVICE,
			GRLimits.DEVICE );
	}
}

/**
Set the drawing limits for the graphs on the full component.  This is typically
called at initialization and when the component size changes (or a property
changes that controls the positioning and size of graphs.  The logic in this
method needs to be consistent with that in the createTSGraphsFromTSProduct()
code.
*/
public void setGraphDrawingLimits ()
{	int nsubs = _tsgraphs.size();
	TSGraph tsgraph;
	GRLimits drawlim;
	// Loop through the graphs.  If a reference graph, we should only go
	// through once (but leave in the loop for now)...
	String prop_value = _display_props.getValue("ReferenceGraph");

	double totalHeight = _drawlim_graphs.getHeight();

	// loop through and find all the subproducts that have had y size
	// percents defined.  Total their percents.
	int percent = 0;
	double d = 0;
	int i = 0;
	int count = 0;

	int[] percents = new int[nsubs];
	int[] heights = new int[nsubs];
	for (i = 0; i < percents.length; i++) {
		percents[i] = -1;
		heights[i] = 0;
	}
	
	String prop_val = null;
	for (int isub = 0; isub < nsubs; isub++) {
		prop_val = _tsproduct.getLayeredPropValue(
			"LayoutYPercent", isub, -1, false);
		if (prop_val != null) {
			if (StringUtil.isInteger(prop_val)) {
				i = StringUtil.atoi(prop_val);
				percents[isub] = i;
				percent += i;
				count++;
			}
			else if (StringUtil.isDouble(prop_val)) {
				d = StringUtil.atod(prop_val);
				percents[isub] = (int)d;
				percent += (int)d;
				count++;
			}
		}
	}

	int leftover = 100 - percent;
	int num = nsubs - count;	
	int percentPerOtherGraphs = 1;
	if (num > 0) {
		percentPerOtherGraphs = leftover / num;
		if (percentPerOtherGraphs == 0) {
			percentPerOtherGraphs = 1;
		}
	}

	// go back through and set the calculated size for all the ones that
	// have not had it defined.
	for (i = 0; i < nsubs; i++) {
		if (percents[i] == -1) {
			percents[i] = percentPerOtherGraphs;
		}
		heights[i] = (int)(totalHeight * percents[i] * .01);
	}

//	System.out.println("Total height: " + totalHeight);
	for (i = 0; i < nsubs; i++) {
//		System.out.println("" + i + ": " + percents[i]);
//		System.out.println("   " + heights[i]);
	}

	int runningTotal = 0;

//	System.out.println("");
		
	for ( i = 0; i < nsubs; i++ ) {
		tsgraph = (TSGraph)_tsgraphs.elementAt(i);
		if ( tsgraph == null ) {
			continue;
		}
		if (	(prop_value != null) &&
			prop_value.equalsIgnoreCase("true") ) {
			// A reference graph so the dimension will be all of the
			// component...
			drawlim = new GRLimits ( _drawlim_graphs );
		}
		else {	// Determine the drawing limits from the number of
			// graphs and their layout...
			// Set the drawing limits for the graph on the page as
			// an even fraction of the whole page in the vertical
			// direction (taking up the full horizontal dimension).
			// Later we can allow more options to place graphs on
			// the page.  The drawing limits will be reset whenever
			// the component size changes.

			runningTotal += heights[i];
			drawlim = new GRLimits(
				_drawlim_graphs.getLeftX(),
				_drawlim_graphs.getTopY() - runningTotal,
				_drawlim_graphs.getRightX(),
				_drawlim_graphs.getTopY() - runningTotal 
					+ heights[i]);
//System.out.println("" + drawlim);					
			/*	
			drawlim = new GRLimits (
				_drawlim_graphs.getLeftX(),
				_drawlim_graphs.getTopY() - (i + 1)*height,
				_drawlim_graphs.getRightX(),
				_drawlim_graphs.getTopY() - i*height );
			*/
		}
		tsgraph.setDrawingLimits ( drawlim );
	}
	tsgraph = null;
	drawlim = null;
}

/**
Set the interaction mode, currently either INTERACTION_SELECT,
INTERACTION_ZOOM, or INTERACTION_NONE.
@param mode Interaction mode.
*/
public void setInteractionMode ( int mode )
{	if (	(mode == INTERACTION_NONE) ||
		(mode == INTERACTION_SELECT) ||
		(mode == INTERACTION_ZOOM) ||
		(mode == INTERACTION_EDIT)) 
 {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, _gtype +
			"TSGraphJComponent.setInteractionMode",
			"Set interaction mode to " + mode );
		}
		_interaction_mode = mode;
	}
}

// TODO SAM 2007-05-09 Need to evaluate whether to implement
/**
Show an information dialog indicating the closest time series and value to
the click.
@param tsgraph TSGraph where the select occurred.
@param x X-coordinate of mouse click, from event handler.
@param y Y-coordinate of mouse click, from event handler.
*/
private void showInfoDialog ( TSGraph tsgraph, int x, int y )
{	GRPoint datapt = tsgraph.getGraphDrawingArea().getDataXY (
			x, y, GRDrawingArea.COORD_DEVICE );
	if ( datapt == null ) {
		return;
	}

	// Convert the y-coordinate to a date...
	DateTime d = new DateTime ( datapt.x, true );
	// Find the time series with a y-value closest to the data point...
	int size = 0;
	Vector tslist = tsgraph.getTSList();
	if ( tslist != null ) {
		size = tslist.size();
	}
	double ts_data = 0.0;
	TS ts = null, mints = null;
	double mindiff = 1.0e10;
	double diff = 0.0;
	for ( int i = 0; i < size; i++ ) {
		ts = (TS)tslist.elementAt(i);
		if ( ts == null ) {
			continue;
		}
		// Set the date precision to that of the time series.
		d.setPrecision ( ts.getDataIntervalBase() );
		// Get the data value for the time series...
		ts_data = ts.getDataValue ( d );
		if ( ts.isDataMissing(ts_data) ) {
			continue;
		}
		// Compare to the nearest one (avoid abs() for speed)...
		diff = ts_data - datapt.y;
		if ( diff < 0.0 ) {
			diff *= -1.0;
		}
		if ( diff < mindiff ) {
			mindiff = diff;
			mints = ts;
		}
	}
	if ( mints == null ) {
		new ResponseJDialog ( _parent,
		"Time Series Information",
		"Unable to find nearest time series.",
		ResponseJDialog.OK );
	}
	else {	d.setPrecision ( mints.getDataIntervalBase() );
		new ResponseJDialog ( _parent,
		"Time Series Information",
		"Nearest time series is:\n" +
		"Time Series Identifier:  " + mints.getIdentifier() + "\n" +
		"Time Series Description:  " + mints.getDescription() + "\n"+
		"Date:  " + d.toString() + "\n" +
		"Data value:  " +
		StringUtil.formatString(mints.getDataValue(d),
		DataUnits.getOutputFormatString(mints.getDataUnits(),0,2)) +
			" " + mints.getDataUnits(), ResponseJDialog.OK );
	}
	datapt = null;
	d = null;
	mints = null;
	ts = null;
}

/**
Tests to see whether two sorted Vectors, each of which contain Strings, contain
the same strings in the same order.
@param v1 the first Vector to check.
@param v2 the second Vector to check.
@return true if the Vectors contain the same strings (compared by 
equalsIgnoreCase()) in the same order, or are both null.  Returns false if only
one is null or the sizes of the Vectors are different or they don't contain 
the same strings, element-by-element.
*/
private boolean stringVectorsAreEqual(Vector v1, Vector v2) {
	if (v1 == null && v2 == null) {
		return true;
	}
	else if (v1 == null || v2 == null) {
		return false;
	}
	else if (v1.size() != v2.size()) {
		return false;
	}
	else {
		int size = v1.size();
		String s1 = null;
		String s2 = null;
		for (int i = 0; i < size; i++) {
			s1 = (String)v1.elementAt(i);
			s2 = (String)v2.elementAt(i);
			if (!s1.equalsIgnoreCase(s2)) {
				return false;
			}
		}
		return true;
	}
}		

/**
Handle the mouse motion event from another TSView (likely a Reference TSView).
Currently this does nothing.
@param g TSGraph that mouse motion occurred in.
@param devpt Coordinates of mouse in device coordinates (pixels).
@param datapt Coordinates of mouse in data coordinates.
*/
public void tsViewMouseMotion (TSGraph g, GRPoint devpt, GRPoint datapt )
{
}

/**
Handle the select event from another TSGraphJComponent (likely a Reference
TSGraphJComponent).
Currently this does nothing.
@param g TSGraph that select occurred in.
@param dev_shape Coordinates of mouse in device coordinates (pixels).
@param data_shape Coordinates of mouse in data coordinates.
@param selected Vector of selected TS.  Currently ignored.
*/
public void tsViewSelect (	TSGraph g, GRShape dev_shape,
				GRShape data_shape, Vector selected )
{
}

/**
Handle the zoom event from another TSView (likely a Reference TSView).  This
resets the data limits for this TSGraphJComponent to those specified (if not
null) and redraws the TSGraphJComponent.  If a reference map, no zooming will
occur in the reference window.
@param g TSGraph where the zoom event occurred.
@param dev_shape Limits of zoom in device coordinates (pixels).
@param data_shape Limits of zoom in data coordinates.
*/
public void tsViewZoom (TSGraph g, GRShape dev_shape, GRShape data_shape)
{	if ( data_shape == null ) {
		// Do nothing...
		return;
	}
	//Message.printStatus ( 1, "", _gtype + "Received a zoom event from " +
	//	_tsproduct.getLayeredPropValue("MainTitleString",
	//		g.getSubProductNumber(), -1, false) + ":" +
	//		" isref:" + g.isReferenceGraph() + " :" +
	//		(GRLimits)data_shape );
	zoom ( g, (GRLimits)dev_shape, (GRLimits)data_shape );
}

/**
Overrule the default update to not clear the screen.
@param g Graphics instance to use for drawing.
*/
public void update(Graphics g)
{	// Just call paint...
	paint ( g );
}

/**
Apply a zoom that has occurred in one graph to other graphs.  This is done by
setting the data limits in the related graphs.  Only graphs in the same
zoom group are updated and it assumed that each graph will interpret the
limits appropriately (e.g., use the new X axis date limits but handle its own
Y axis limits).
@param tsgraph TSGraph that generated the zoom event.
@param mouse_limits Component limits for zoom extent.
@param newdata_limits Data limits for zoom extent in the original graph.
*/
public void zoom (	TSGraph tsgraph,
			GRLimits mouse_limits, GRLimits newdata_limits )
{	String zoom_group = _tsproduct.getLayeredPropValue ( "ZoomGroup",
			tsgraph.getSubProductNumber(), -1, false );
	//Message.printStatus ( 1, "", _gtype + "zoom() data limits are" +
	//	newdata_limits.toString() );
	int size = _tsgraphs.size();
	TSGraph tsgraph2;
	for ( int isub = 0; isub < size; isub++ ) {
		tsgraph2 = (TSGraph)_tsgraphs.elementAt(isub);
		if (	tsgraph2.canZoom() &&
			zoom_group.equalsIgnoreCase(
			_tsproduct.getLayeredPropValue ( "ZoomGroup",
			isub, -1, false ) ) ) {
			//if ( tsgraph2.isReferenceGraph() ) {
				tsgraph2.setDataLimits ( newdata_limits );
			//}
		}
	}
	refresh ();
}

/**
Zoom out for all the graphs and refresh the component.  This is typically 
called from a container like the TSViewGraphFrame.
*/
public void zoomOut() {
	zoomOut(true);
}

/**
Zoom out for all the graphs and optionally redraw.  This method is also called
one time in the first paint to force the different graphs to synchronize their
zoom levels.
@param re_draw If true, redraw the component after zooming.  If false, just set
the data limits in the graphs but do not redraw (a redraw may occur elsewhere
after this method is called).
*/
public void zoomOut ( boolean re_draw )
{	int size = _tsgraphs.size();
	// Loop through the zoom levels and zoom each group of graphs to the
	// maximum data extent within the zoom group.

	TSGraph tsgraph;
	GRLimits max_data_limits = null;
	String prop_value;
	int zoom_group = 0;
	int num_zoom_groups = _tsproduct.getNumZoomGroups();
	for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
		// Loop through and determine the maximum limits for the zoom
		// group (zoom groups are 1...N)...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			if ( max_data_limits == null ) {
				max_data_limits = new GRLimits(
					tsgraph.getMaxDataLimits() );
			}
			else {	max_data_limits =
				max_data_limits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " +
			//max_data_limits );
		// Now loop through again and set the data limits for
		// graph in the zoom group...
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi(
				_tsproduct.getLayeredPropValue (
				"ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = (TSGraph)_tsgraphs.elementAt(isub);
			prop_value = _tsproduct.getLayeredPropValue (
				"ZoomEnabled", isub, -1, false );
			if ( prop_value.equalsIgnoreCase("true") ) {
				if (tsgraph.getNumTS() > 0) {
					tsgraph.setDataLimits(max_data_limits);
				}
			}
		}
	}
	if ( re_draw )  {
		// Refresh the component...
		refresh();
	}
}

/**
 * Controls display of cross hair cursor on graph.
 * @param display If true displays cross hair
 */
public void setDisplayCursor(boolean display)
{
  _displayCrossHairCursor = display;
}

public void setEditor(TSGraphEditor tsGraphEditor)
{
_tsGraphEditor = tsGraphEditor;
  
}

} // End TSGraphJComponent class
