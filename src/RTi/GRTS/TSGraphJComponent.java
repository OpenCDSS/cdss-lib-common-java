// TSGraphJComponent - component for displaying one or more time series graphs

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; // Needed for some standard methods.
import java.awt.Graphics2D; // Will be used for most drawing.
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import RTi.GR.GRAspectType;
import RTi.GR.GRColor;
import RTi.GR.GRCoordinateType;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRShape;
import RTi.GR.GRSymbolPosition;
import RTi.GR.GRSymbolShapeType;
import RTi.GR.GRText;
import RTi.GR.GRUnits;
import RTi.TS.IrregularTS;
import RTi.TS.TS;
import RTi.TS.TSData;
import RTi.TS.TSIterator;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.GraphicsPrinterJob;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The TSGraphJComponent class provides a component for displaying one or more time series graphs, each with one or more time series.
Methods are available for initiating printing as well as handling zooming and selects.
Performance options include double-buffering.
This class also implements TSViewListener, which typically is used to allow a reference TSGraphJComponent
to be enabled that calls the listener methods in a main TSGraphJComponent.
Therefore, zoom control can occur in both TSGraphJComponent.
The TSGraphJComponent controls all redrawing through the standard paint() method.
Each graph is drawn within the TSGraph class.
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
The positioning of the graphs is by default top to bottom but features will be added to allow row/cell positioning.
*/
@SuppressWarnings("serial")
public class TSGraphJComponent extends GRJComponentDevice
implements KeyListener, MouseListener, MouseMotionListener, Printable, TSViewListener
{

/**
Use this to force the drawing limits to be reset.
Otherwise, the graphics is not available for setting font-dependent area sizes.
*/
private boolean _first_paint = true;

/**
List of time series to plot.  Not sure if this needs to be saved here.
*/
private List<TS> _tslist = null;

/**
Background color.
*/
private GRColor _background_color = GRColor.white;

/**
 * Used with legacy data editing:
 * - currently disabled (use the newer mouse tracker features)
 */
private TSCursorDecorator _cursorDecorator = null;

/**
 * Whether to use the TSCursorDecorator:
 * - current default is to not use it because the tracker is used
 */
private boolean useCursorDecorator = false;

/**
External image used when processing in batch mode.
*/
private BufferedImage _external_Image = null;

/**
Rubber band color for zooming.
*/
private GRColor _rubber_band_color = GRColor.red;

/**
Flag indicating whether the component is a reference canvas, in which case only one graph will be shown.
*/
private boolean _is_reference_graph = false;

/**
 * History of x-axis zooms maintained with the reference graph (not used if not a reference graph).
 */
private TSViewZoomHistory zoomHistory = new TSViewZoomHistory();

/**
The subproduct (graph) that should be used for reference graph information.
*/
private int _reference_sub = -1;

/**
Flag indicating whether the graph should be closed due to a user confirmation (because of data problem, etc).
*/
private boolean _need_to_close = false;

/**
TSProduct that describes the time series product to create.
If an old-style PropList is passed in to the constructor, a new TSProduct is created.
If a TSProduct is passed in to the constructor, it is used.
*/
private TSProduct _tsproduct = null;

/**
Indicate whether Y-limits should be kept when zooming.
If false, that means that users can change the min and max values in the properties JFrame.
*/
private boolean _zoom_keep_y_limits = false;

/**
TODO SAM 2013-01-28 Seems like this includes override properties?
PropList for display properties that are not to be confused with the TSProduct properties (or its override properties).
For example, this list of properties is used to indicate whether the component is a reference graph.
*/
private PropList _displayProps = null;

/**
Drawing areas used for the main component.
Only a few are necessary because most of the drawing occurs in the TSGraph objects.
List in the order of the layout top to bottom.
*/

/**
Drawing area for full canvas.
*/
private GRJComponentDrawingArea _da_page = null;

/**
Drawing area for main title.
*/
private GRJComponentDrawingArea _da_maintitle =null;

/**
Drawing area for subtitle.
*/
private GRJComponentDrawingArea _da_subtitle = null;

/**
Drawing area for graphs (actual drawing is done in TSGraph) but this drawing area may draw borders or background color).
*/
private GRJComponentDrawingArea _da_graphs = null;

/**
Drawing area for left footer.
*/
private GRJComponentDrawingArea _da_leftfoot = null;

/**
Drawing area for center footer.
*/
private GRJComponentDrawingArea _da_centerfoot=null;

/**
Drawing area for right footer.
*/
private GRJComponentDrawingArea _da_rightfoot =null;

// Data and drawing limits for the above drawing areas.

/**
Full page drawing area limits.
*/
private GRLimits _datalim_page = null;
private GRLimits _drawlim_page = null;

/**
Limits for main title drawing area.
*/
private GRLimits _datalim_maintitle = null;
private GRLimits _drawlim_maintitle = null;

/**
Limits for subtitle drawing area.
*/
private GRLimits _datalim_subtitle = null;
private GRLimits _drawlim_subtitle = null;

/**
Limits for graphs drawing area.
*/
private GRLimits _datalim_graphs = null;
private GRLimits _drawlim_graphs = null;

/**
Limits for left footer drawing area.
*/
private GRLimits _datalim_leftfoot = null;
// TODO SAM 2007-05-09 Enable handling of the following
//private GRLimits _drawlim_leftfoot = null;

/**
Limits for center footer drawing area.
*/
private GRLimits _datalim_centerfoot = null;
//private GRLimits _drawlim_centerfoot = null;

/**
Limits for right footer drawing area.
*/
private GRLimits _datalim_rightfoot = null;
//private GRLimits _drawlim_rightfoot = null;

/**
Set with update.  Useful for clearing the component.
*/
private Rectangle _bounds = null;

/**
Set with update.  Useful for clearing the component.
*/
private GRLimits _printBounds = null;

/**
Parent JFrame.
*/
private JFrame _parent = null;

/**
List of TSGraph being drawn, guaranteed to be non-null.
*/
private List<TSGraph> _tsgraphs = new ArrayList<>();

/**
Starting date/time for visible graph, used for first draw only.
*/
private DateTime __visibleStart = null;

/**
Ending date/time for visible graph, used for first draw only.
*/
private DateTime __visibleEnd = null;

// Dimensions for drawing areas.

/**
Used for messages so we can tell whether in a main or reference graph.
*/
private String _gtype = "Main:";

/**
Force a redraw in paint even if size, etc. has not changed.
*/
private boolean _force_redraw = true;

/**
Force a redraw in paint even if size, etc. has not changed and additionally create clean component.
This is necessary, for example, when clicking on the legend to highlight a time series and then click again to un-higlight.
If the redraw is done, the unselect will probably draw a thinner line on the previous thicker
line and thicker line will appear to be still present.
*/
private boolean _force_redraw_clean = true;

/**
Listeners that want to know when the TSView changes.
*/
private TSViewListener [] _listeners = null;

/**
Interaction mode.
*/
private TSGraphInteractionType _interaction_mode = TSGraphInteractionType.NONE;

/**
Coordinates for events.
*/
private int _mouse_x1 = -1; // First location.
private int _mouse_y1 = -1;
private int _mouse_x2 = -1; // Current location.
private int _mouse_y2 = -1;
private int _mouse_xprev = -1; // Previous location.
private int _mouse_yprev = -1;

/**
TSGraph where initial mouse press occurs when drawing a box.
*/
private TSGraph _mouse_tsgraph1 = null;

/**
Indicates if drawing should wait.
Use this if multiple layers are being loaded and you don't want to see redraws between each one.
*/
private boolean _waiting = false;

/**
Indicate whether to display cross-hairs on the graph to the edges
*/
//private boolean _displayCrossHairCursor = false;

/**
Indicate whether the paint is to an SVG file.  See the saveAsSVG() method.
*/
private boolean __paintForSVG = false;

/**
 * Graph editor, which allows time series to be edited,
 * will be non-null if one or more time series in the graph have editable=true.
 */
private TSGraphEditor _tsGraphEditor;

/**
Indicate whether Batik SVG functionality is present.
*/
public static boolean batikSvgEnabled = false;

/**
Indicate whether JFreeSVG functionality is present.
*/
public static boolean jFreeSvgEnabled = false;

static {
        // By default, SVG features are not enabled:
		// - if the classes are present, then SVG is enabled
		// - an old version of Batik was previously used, and it is a large package
		// - as of July 16, 2024 experiment with JFreeSVG
        batikSvgEnabled = false;
        try {
            Class.forName("org.apache.batik.svggen.SVGGeneratorContext");
            batikSvgEnabled = true;
        } catch (ClassNotFoundException cnfe) {
            // Do nothing.
        }

        jFreeSvgEnabled = false;
        try {
            Class.forName("org.jfree.graphics2d.svg.SVGGraphics2D");
            jFreeSvgEnabled = true;
        } catch (ClassNotFoundException cnfe) {
            // Do nothing.
        }
}

/**
Construct a TSGraphJComponet and display the time series.
@param parent Parent Frame object (often a TSViewGraphJFrame).
@param tslist list of time series to graph.
@param displayProps Properties to control display of time series.
Most of these properties are documented in TSViewJFrame, with the following additions:
<ul>
<li> ReferenceGraph can be set to "true" or "false" to indicate whether the graph is a reference graph.</li>
<li> ReferenceTSIndex can be set to a list index (0+) to indicate the reference time series for the reference graph
     (the default is the time series with the longest overall period).</li>
<li> TSViewParentUIComponent can be set to the parent UI component
     (e.g., TSTool JFrame) so that message/warning dialogs can center if the graph JFrame is not yet available.
     If not set it will be set to 'parent'.</li>
</ul>
*/
public TSGraphJComponent ( TSViewGraphJFrame parent, List<TS> tslist, PropList displayProps ) {
	super ( "TSGraphJComponent" );
	String routine = "TSGraphJComponent";
	if ( displayProps != null ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, "In TSGraphJComponent constructor for TS list, TSViewParentUIComponent=" +
				displayProps.getContents("TSViewParentUIComponent"));
		}
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, "In TSGraphJComponent constructor for TS list, null props so can't check TSViewParentUIComponent");
		}
	}
	this._force_redraw = true;
	this._parent = parent;
	this._tslist = tslist;
	// Convert the old-style PropList to a TSProduct instance:
	// - this also fills _display_props
	this._tsproduct = createTSProductFromPropList ( displayProps, tslist );
	// Now use _displayProps for further processing.
	checkDisplayProperties ( this._tsproduct, this._displayProps );
	this._tsproduct.checkProperties();
	// Need to figure out the requested height and width because this information is used to create the TSGraph instances.
	double height = 400.0, width = 400.0;

	String propVal = this._tsproduct.getLayeredPropValue ( "TotalHeight", -1, -1, false );
	if ( (propVal != null) && StringUtil.isDouble(propVal) ) {
		height = StringUtil.atod(propVal);
	}
	propVal = this._tsproduct.getLayeredPropValue ( "TotalWidth", -1, -1,false);
	if ( (propVal != null) && StringUtil.isDouble(propVal) ) {
		width = StringUtil.atod(propVal);
	}

	if ( !this._is_reference_graph ) {
		setSize ( (int)width, (int)height );
		// Swing seems to need this (the above does not do anything).
		setPreferredSize ( new Dimension((int)width, (int)height) );
	}

	// Now create the TSGraph instances that will manage the individual graphs.
	this._tsgraphs = createTSGraphsFromTSProduct ( this._tsproduct, this._displayProps,
		tslist, new GRLimits(0.0,0.0,width,height) );
	checkTSProductGraphs ( this._tsproduct, this._tsgraphs );
	// Open the drawing areas with initial limits.
	openDrawingAreas ();

	// For now background color cannot be changed.
	setBackground ( this._background_color );

	// Enable events on the component.

	requestFocus();
	addMouseListener ( this );
	addMouseMotionListener ( this );
	addKeyListener ( this );

	// Force a paint on construction.
	repaint();
}

/**
Construct a TSGraphJComponent and display the time series.
@param parent Parent JFrame object (often a TSViewGraphGUI).
@param tsproduct TSProduct describing the graph(s).
The following override properties can be set in the TSProduct to affect this graph component.
@param displayProps Display properties that control the display and which are separate from the TSProduct.
Valid properties include those described below.
<ul>
<li> ReferenceGraph can be set to "true" or "false" to indicate whether the graph is a reference graph.</li>
<li> ReferenceTSIndex can be set to a list index to indicate the reference time series for the reference graph
     (the default is the time series with the longest overall period).</li>
<li> TSViewParentUIComponent can be set to the parent UI component (e.g., TSTool JFrame) so that
     message/warning dialogs can center if the graph JFrame is not yet available.
     If not set it will be set to 'parent'.</li>
</ul>
*/
public TSGraphJComponent ( TSViewGraphJFrame parent, TSProduct tsproduct, PropList displayProps ) {
	super ( "TSGraphJComponent" );
	String routine = "TSGraphJComponent";
	this._force_redraw = true;
	this._parent = parent;
	this._tslist = tsproduct.getTSList(); // Do this for now.  Later remove _tslist.
	// Set this components display properties to those passed in.
	this._displayProps = displayProps;
	if ( displayProps != null ) {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, "In TSViewGraphJFrame constructor for tsproduct, product TSViewParentUIComponent=" +
				displayProps.getContents("TSViewParentUIComponent"));
		}
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, routine, "In TSGraphJComponent constructor for tsproduct, null props so can't check TSViewParentUIComponent");
		}
	}
	if ( this._displayProps == null ) {
		// Create empty properties to avoid null pointer issues.
		this._displayProps = new PropList ( "display" );
	}
	if ( (this._tslist == null) || (this._tslist.size() == 0) ) {
		Message.printWarning ( 2, routine, "Time series list is null.  Product will be empty." );
	}
	// The following confirms that the display properties are OK.
	checkDisplayProperties ( tsproduct, this._displayProps );
	this._tsproduct = tsproduct;
	this._tsproduct.checkProperties();
	// Set the requested (or if not specified, default) height and width
	// because this information is used to create the TSGraph instances.
	double height = 400.0, width = 400.0;

	String prop_val = this._tsproduct.getLayeredPropValue ( "TotalHeight", -1, -1, false );
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		height = StringUtil.atod(prop_val);
	}
	prop_val = this._tsproduct.getLayeredPropValue ( "TotalWidth", -1, -1,false);
	if ( (prop_val != null) && StringUtil.isDouble(prop_val) ) {
		width = StringUtil.atod(prop_val);
	}

	setSize ( (int)width, (int)height );	// Set Java component size.
	// Swing seems to need this (the above does not do anything!).
	setPreferredSize ( new Dimension((int)width, (int)height) );

	// Now create the TSGraph instances that will manage the individual graphs.
	// The limits of the graphs will not be correct because the titles, etc., have not been considered.
	// The limits will be reset in the first paint() method call and every resize after that.
	this._tsgraphs = createTSGraphsFromTSProduct ( this._tsproduct, this._displayProps, this._tslist,
		new GRLimits( 0.0,0.0,width,height) );
	checkTSProductGraphs ( this._tsproduct, this._tsgraphs );

	// Open the drawing areas with initial limits.
	openDrawingAreas ();

	// For now background color cannot be changed.
	setBackground ( this._background_color );

	// Enable events on the component.

	requestFocus();
	addMouseListener ( this );
	addMouseMotionListener ( this );
	addKeyListener ( this );

	// Install decorator for cross hair cursor.
	if ( this.useCursorDecorator ) {
		this._cursorDecorator = new TSCursorDecorator ( this, this._rubber_band_color, this._background_color );
	}
	// Force a paint on construction.
	repaint();
}

/**
Add a TSListener to receive TSView events.  Multiple listeners can be registered.
Graph events, including mouse events and zoom events, will result in registered listeners being called.
@param listener TSViewListener to add.
*/
public void addTSViewListener ( TSViewListener listener ) {
	// Use arrays to make a little simpler than lists to use later.
	if ( listener != null ) {
		// Resize the listener array.
		if ( _listeners == null ) {
			_listeners = new TSViewListener[1];
			_listeners[0] = listener;
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10, _gtype + "TSGraphJComponent.addTSViewListener", "Added TSViewListener" );
			}
		}
		else {
		    // Need to resize and transfer the list.
			int size = _listeners.length;
			TSViewListener [] newlisteners = new TSViewListener[size + 1];
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
Indicate whether a reference graph would be useful, based on the graphs that are shown.
This is called by TSViewGraphJFrame to let it decide whether to display a reference graph.
This method does not currently evaluate the zoom group property.
@return true if at least one graph allows zooming.
*/
public boolean canUseReferenceGraph() {
	return canUseZoom();
}

/**
Indicate whether enabling zoom should occur.  This will be the case if any of the graphs can zoom.
Later, may allow completely if we figure out how to zoom scatter, duration, etc.
This is called by TSViewGraphJFrame to let it decide whether to enable the zoom button.
@return true if at least one graph allows zooming.
*/
public boolean canUseZoom() {
	int size = _tsgraphs.size();
	String prop_value;
	for ( int i = 0; i < size; i++ ) {
		prop_value = _tsproduct.getLayeredPropValue ( "ZoomEnabled", i, -1, false );
		if ( prop_value.equalsIgnoreCase("true") ) {
			return true;
		}
	}
	return false;
}

/**
Check to make sure that the display properties are set.
This checks the display properties and sets internal variables that are commonly used.
@param displayProps display properties to provide additional runtime information above and beyond the time series product properties
*/
private void checkDisplayProperties ( TSProduct tsproduct, PropList displayProps ) {
	String routine = getClass().getSimpleName() + ".checkDisplayProperties";

	this._is_reference_graph = false;
	this._gtype = "Main:";
	if ( displayProps == null ) {
		throw new RuntimeException ( "Display properties are null - software bug." );
	}
	String propVal = displayProps.getValue ( "ReferenceGraph" );
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		this._is_reference_graph = true;
		this._gtype = "Ref:";
		this._interaction_mode = TSGraphInteractionType.ZOOM;
	}
    propVal = tsproduct.getLayeredPropValue("VisibleStart", -1, -1);
    if ( propVal != null ) {
        this.__visibleStart = null;
        try {
            this.__visibleStart = DateTime.parse(propVal);
        }
        catch ( Exception e ) {
            this.__visibleStart = null;
        }
        Message.printStatus(2, routine, "Setting VisibleStart=" + this.__visibleStart );
    }
    propVal = tsproduct.getLayeredPropValue("VisibleEnd", -1, -1);
    if ( propVal != null ) {
        this.__visibleEnd = null;
        try {
            this.__visibleEnd = DateTime.parse(propVal);
        }
        catch ( Exception e ) {
            this.__visibleEnd = null;
        }
        Message.printStatus(2, routine, "Setting VisibleEnd=" + this.__visibleEnd );
    }

	if ( this._external_Image == null ) {
		// Don't want to reset if already reset when converted from a PropList - need to clean this up?
		this._external_Image = (BufferedImage)displayProps.getContents("Image");
		if ( this._external_Image != null ) {
			Message.printDebug( 1, "", this._gtype + "Using external Image for drawing." );
			// Disable reference time series (don't want labels in legend).
			displayProps.set ( "ReferenceTSIndex=" + -1 );
		}
	}
}

/**
Check the TSProduct properties that defines the graphs to make sure that properties are defined.
This is done so that defaults do not need to be assigned throughout the class.
Later, needed properties (like fonts) are always assumed to be defined.
In many cases, only defaults need to be defined at the upper property levels (e.g., for Product or SubProduct).
<b>This method should be called AFTER createTSGraphsFromTSProduct() because the
time series data are checked to determine some properties that impact other properties.</b>
@param tsproduct TSProduct to check.
@param tsgraphs list of TSGraph describing the graphs.
*/
private void checkTSProductGraphs ( TSProduct tsproduct, List<TSGraph> tsgraphs ) {
	// Put here any checks that cannot be performed in the initial call to checkTSProduct(),
	// which primarily checks that depend on the time series (units for axis labels, etc.).
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet (Prop.SET_AS_RUNTIME_DEFAULT);

	int nsubs = tsproduct.getNumSubProducts();
	//Message.printStatus ( 1, "", _gtype + "Checking " + nsubs + " graphs after graphs were created." );
	TSGraphType graphType = TSGraphType.LINE;
	List<TS> tslist = null;
	List<TS> tslistLeftYAxis = null;
	List<TS> tslistRightYAxis = null;
	TSGraph tsgraph = null;
	int nts = 0, ntsLeftYAxis = 0, ntsRightYAxis = 0;
	String prop_val;
	for ( int isub = 0; isub < nsubs; isub++ ) {
		tsgraph = tsgraphs.get(isub);
		// Get the graph type to simplify later checks.
		prop_val = tsproduct.getLayeredPropValue ( "GraphType", isub, -1, false );
		if ( prop_val == null ) {
			prop_val = "Line";
			tsproduct.setPropValue ( "GraphType", prop_val, isub, -1);
		}
		graphType = TSGraphType.valueOfIgnoreCase( prop_val);

		// Get the list of time series for the sub-product.
		// This is a subset of the graphs used for the full product.

		tslist = tsgraph.getTSList();
		tslistLeftYAxis = tsgraph.getTSListForLeftYAxis();
		tslistRightYAxis = tsgraph.getTSListForRightYAxis();
		nts = 0;
		if ( tslist != null ) {
			nts = tslist.size();
		}
		ntsLeftYAxis = 0;
		if ( tslistLeftYAxis != null ) {
			ntsLeftYAxis = tslistLeftYAxis.size();
		}
		ntsRightYAxis = 0;
		if ( tslistRightYAxis != null ) {
			ntsRightYAxis = tslistRightYAxis.size();
		}
		//Message.printStatus ( 2, "", _gtype + "Checking " + nts + " time series for graph [" + isub + "]" );

		// "LeftYAxisIgnoreUnits"
		//
		// If this is set in the TSProduct (e.g., a product file), then don't need to do anything.
		// If not set, then it will have been evaluated in the TSGraph (where an internal boolean is set).
		// Set the property here so it is visible and is compatible with the internal flag.

		if ( tsproduct.getLayeredPropValue("LeftYAxisIgnoreUnits", isub, -1, false ) == null ) {
			if ( tsgraph.ignoreLeftYAxisUnits() ) {
				tsproduct.setPropValue ( "LeftYAxisIgnoreUnits", "true", isub, -1 );
			}
			else {
			    tsproduct.setPropValue ( "LeftYAxisIgnoreUnits", "false", isub, -1 );
			}
		}

		// "LeftYAxisTitleString" - depends on graph type and units.

		// TODO SAM 2006-09-28 Why was the following always evaluated as true in previous code with 1 == 1?
		// Was this a work-around for some other problem.  Need to evaluate further when regression tests are in place.
		if ( tsproduct.getLayeredPropValue("LeftYAxisTitleString", isub, -1, false ) == null ) {
			tsproduct.setPropValue ( "LeftYAxisTitleString", "", isub, -1 );
			if ( graphType == TSGraphType.XY_SCATTER ) {
				// Units will be 1st (dependent) time series:
				// - need to do something else if more than one time series
				if ( nts >= 1 ) {
					TS ts0 = tslist.get(0);
					tsproduct.setPropValue ( "LeftYAxisTitleString", ts0.getDataUnits(), isub, -1 );
				}
				else {
				    tsproduct.setPropValue ( "LeftYAxisTitleString", "", isub, -1 );
				}
			}
			else if ( graphType == TSGraphType.PERIOD ) {
				// Special Y-axis title:
				// - legend entries have the time series number (1+)
				tsproduct.setPropValue ( "LeftYAxisTitleString", "Legend Index", isub, -1 );
			}
			else if ( graphType == TSGraphType.RASTER ) {
				if ( this._tslist.size() == 1 ) {
					// Special Y-axis title:
					// - units are in the raster legend so no additional note is needed
					// - use one space so that the vertical space does allow for the upper y-axis labels
					// - months are shown above the graph and below the graph to make it easier to read the graph
					tsproduct.setPropValue ( "LeftYAxisTitleString", " ", isub, -1 );
				}
				if ( this._tslist.size() > 1 ) {
					// Special Y-axis title:
					// - legend entries have the time series number (1+)
					tsproduct.setPropValue ( "LeftYAxisTitleString", "Legend Index", isub, -1 );
				}
			}
			else {
			    // Title is the units that are displayed on the axis.
				if ( tsgraph.ignoreLeftYAxisUnits() ) {
					// Units are not different in data and are indicated in the legend.
					tsproduct.setPropValue ( "LeftYAxisTitleString", "See units in legend", isub, -1 );
				}
				else {
				    // Get the units from the first non-null time series.
					String units = "";
					TS ts = null;
					for ( int its = 0; its < ntsLeftYAxis; its++ ) {
						ts = tslistLeftYAxis.get(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue ( "LeftYAxisTitleString", units, isub, -1 );
				}
			}
		}

		// "LeftYAxisUnits" - determined from graph type.

		if ( tsproduct.getLayeredPropValue("LeftYAxisUnits", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.XY_SCATTER ) {
				// Units will be 1st (dependent) time series:
				// - need to do something else if more than one time series
				if ( nts >= 1 ) {
					TS ts0 = tslist.get(0);
					tsproduct.setPropValue ( "LeftYAxisUnits", ts0.getDataUnits(), isub, -1 );
				}
				else {
				    tsproduct.setPropValue ( "LeftYAxisUnits", "", isub, -1 );
				}
			}
			else if ( graphType == TSGraphType.PERIOD ) {
				// Count of time series (not really used for anything).
				tsproduct.setPropValue ( "LeftYAxisUnits", "COUNT", isub, -1 );
			}
			else {
			    // Units are time series data units.
				if ( tsgraph.ignoreLeftYAxisUnits() ) {
					// Units are not different in data and are indicated in the legend so set blank.
					tsproduct.setPropValue ( "LeftYAxisUnits", "", isub, -1 );
				}
				else {
				    // Get the units from the first non-null time series associated with the left axis.
					String units = "";
					TS ts = null;
					for ( int its = 0; its < ntsLeftYAxis; its++ ) {
						ts = tslistLeftYAxis.get(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue ( "LeftYAxisUnits", units, isub, -1 );
				}
			}
		}

		// "LeftYAxisLabelPrecision" - DO THIS AFTER "LeftYAxisUnits".

		if ( tsproduct.getLayeredPropValue("LeftYAxisLabelPrecision", isub, -1, false ) == null ) {
			if ( (graphType == TSGraphType.PERIOD) || (graphType == TSGraphType.RASTER) ) {
				tsproduct.setPropValue ( "LeftYAxisLabelPrecision", "0", isub, -1 );
			}
			else if ( tsgraph.ignoreLeftYAxisUnits() ) {
				// Set the precision to the maximum precision for the units of all time series.
				int yaxis_precision = 2;
				tsproduct.setPropValue ( "LeftYAxisLabelPrecision", "" + yaxis_precision, isub, -1 );
			}
			else {
			    // Determine the precision from the axis units.
				String lefty_units = _tsproduct.getLayeredPropValue ( "LeftYAxisUnits", isub, -1, false );
				if ( lefty_units.equals("") ) {
					// Default.
					tsproduct.setPropValue ( "LeftYAxisLabelPrecision", "2", isub, -1 );
				}
				else {
				    try {
				        DataUnits u = DataUnits.lookupUnits ( lefty_units );
						int precision = u.getOutputPrecision();
						tsproduct.setPropValue ( "LeftYAxisLabelPrecision", "" + precision, isub, -1 );
					}
					catch ( Exception e ) {
						// Default.
						tsproduct.setPropValue ( "LeftYAxisLabelPrecision", "2", isub, -1 );
					}
				}
			}
		}

		// BottomXAxisTitleString may have units.

		if ( tsproduct.getLayeredPropValue("BottomXAxisTitleString", isub, -1, false ) == null ) {
			tsgraph = _tsgraphs.get(isub);
			if ( tsgraph == null ) {
				continue;
			}
			if ( graphType == TSGraphType.DURATION ) {
				tsproduct.setPropValue ( "BottomXAxisTitleString","Percent of values >= y-axis value", isub, -1 );
			}
			else if (graphType == TSGraphType.XY_SCATTER){
				// Bottom axis is the units of the first independent time series.
				if ( nts >= 2 ) {
					TS ts1 = tslist.get(1);
					String units = ts1.getDataUnits();
					tsproduct.setPropValue ( "BottomXAxisTitleString", units, isub, -1 );
					try {
					    DataUnits u = DataUnits.lookupUnits ( units );
						// Also set the precision.
						tsproduct.setPropValue ( "BottomXAxisPrecision", "" + u.getOutputPrecision(), isub, -1 );
					}
					catch ( Exception e ) {
						tsproduct.setPropValue ( "BottomXAxisPrecision", "1", isub, -1 );
					}
				}
				else {
				    tsproduct.setPropValue ( "BottomXAxisTitleString", "", isub, -1);
				}
			}
			else {
			    tsproduct.setPropValue ( "BottomXAxisTitleString", "", isub, -1 );
			}
		}

		// "RightYAxisTitleString" - depends on graph type and units.

		// TODO SAM 2016-10-17 Copy the left axis to enable right axis.
		// TODO SAM 2006-09-28 Why was the following always evaluated as true in previous
		// code with 1 == 1?  Was this a work-around for some other problem?
		// Need to evaluate further when regression tests are in place.
		if ( tsproduct.getLayeredPropValue("RightYAxisTitleString", isub, -1, false ) == null ) {
			tsproduct.setPropValue ( "RightYAxisTitleString", "", isub, -1 );
			// TODO SAM 2016-10-17 not sure if scatter logic even makes sense on right axis
			if ( graphType == TSGraphType.XY_SCATTER ) {
				// Units will be 1st (dependent) time series:
				// - need to do something else if more than one time series
				if ( nts >= 1 ) {
					// FIXME SAM 2016-10-17 Need to get right axis time series.
					TS ts0 = tslist.get(0);
					tsproduct.setPropValue ( "RightYAxisTitleString", ts0.getDataUnits(), isub, -1 );
				}
				else {
				    tsproduct.setPropValue ( "RightYAxisTitleString", "", isub, -1 );
				}
			}
			else if ( graphType == TSGraphType.PERIOD ) {
				tsproduct.setPropValue ( "RightYAxisTitleString", "Legend Index", isub, -1 );
			}
			else {
			    // Title is the units that are displayed on the axis.
				if ( tsgraph.ignoreRightYAxisUnits() ) {
					// Units are not different in data and are indicated in the legend.
					tsproduct.setPropValue ( "RightYAxisTitleString", "See units in legend", isub, -1 );
				}
				else {
				    // Get the units from the first non-null time series.
					String units = "";
					TS ts = null;
					for ( int its = 0; its < ntsRightYAxis; its++ ) {
						ts = tslistRightYAxis.get(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue ( "RightYAxisTitleString", units, isub, -1 );
				}
			}
		}

		// "RightYAxisUnits" - determined from graph type.

		if ( tsproduct.getLayeredPropValue("RightYAxisUnits", isub, -1, false ) == null ) {
			if ( graphType == TSGraphType.XY_SCATTER ) {
				// Units will be 1st (dependent) time series -
				// need to do something else if more than one time series.
				if ( nts >= 1 ) {
					TS ts0 = tslist.get(0);
					tsproduct.setPropValue ( "RightYAxisUnits", ts0.getDataUnits(), isub, -1 );
				}
				else {
				    tsproduct.setPropValue ( "RightYAxisUnits", "", isub, -1 );
				}
			}
			else if ( graphType == TSGraphType.PERIOD ) {
				// Count of time series (not really used for anything).
				tsproduct.setPropValue ( "RightYAxisUnits", "COUNT", isub, -1 );
			}
			else {
			    // Units are time series data units.
				if ( tsgraph.ignoreRightYAxisUnits() ) {
					// Units are not different in data and are indicated in the legend.
					tsproduct.setPropValue ( "RightYAxisUnits", "", isub, -1 );
				}
				else {
				    // Get the units from the first non-null time series.
					String units = "";
					TS ts = null;
					for ( int its = 0; its < ntsRightYAxis; its++ ) {
						ts = tslistRightYAxis.get(its);
						if ( ts == null ) {
							continue;
						}
						units = ts.getDataUnits();
						break;
					}
					tsproduct.setPropValue ( "RightYAxisUnits", units, isub, -1 );
				}
			}
		}

		// "RightYAxisLabelPrecision" - DO THIS AFTER "RightYAxisUnits".

		if ( tsproduct.getLayeredPropValue("RightYAxisLabelPrecision", isub, -1, false ) == null ) {
			if ( (graphType == TSGraphType.PERIOD) || (graphType == TSGraphType.RASTER) ) {
				tsproduct.setPropValue ( "RightYAxisLabelPrecision", "0", isub, -1 );
			}
			else if ( tsgraph.ignoreRightYAxisUnits() ) {
				// Set the precision to the maximum precision for the units of all time series.
				int yaxis_precision = 2;
				tsproduct.setPropValue ( "RightYAxisLabelPrecision", "" + yaxis_precision, isub, -1 );
			}
			else {
			    // Determine the precision from the axis units.
				String righty_units = _tsproduct.getLayeredPropValue ( "RightYAxisUnits", isub, -1, false );
				if ( righty_units.equals("") ) {
					// Default...
					tsproduct.setPropValue ( "RightYAxisLabelPrecision", "2", isub, -1 );
				}
				else {
				    try {
				        DataUnits u = DataUnits.lookupUnits ( righty_units );
						int precision = u.getOutputPrecision();
						tsproduct.setPropValue ( "RightYAxisLabelPrecision", "" + precision, isub, -1 );
					}
					catch ( Exception e ) {
						// Default.
						tsproduct.setPropValue ( "RightYAxisLabelPrecision", "2", isub, -1 );
					}
				}
			}
		}
	}
	_tsproduct.getPropList().setHowSet (how_set_prev);
}

/**
Create a list of TSGraph from a TSProduct and a list of time series.
The list always contains the graphs but if a graph is disabled its size will not be shown in the graph.
This typically would be called in the first paint where the component size is known.
@param tsproduct TSProduct describing what to graph.
@param displayProps Display properties (e.g., whether a reference graph).
Set "TSViewParentUIComponent" to the parent UI component (e.g., the main TSTool frame)
so that warning dialogs can center prior to the graph JFrame being fully constructed.
This is a special case that improves the user experience on multiple displays.
@param tsproduct_tslist List of time series to graph for the full TSProduct.
@param drawlim_graphs Drawing limits of the area set aside for graphs.
This area is divided among the individual graphs.
@return List of TSGraph to use when drawing.  The list is guaranteed to be non-null but may contain zero graphs.
*/
private List<TSGraph> createTSGraphsFromTSProduct ( TSProduct tsproduct, PropList displayProps,
	List<TS> tsproduct_tslist, GRLimits drawlim_graphs ) {
	String routine = getClass().getSimpleName() + ".createTSGraphsFromTSProduct";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "Creating graphs from TSProduct." );
	}
	int nsubs = tsproduct.getNumSubProducts( false );
	if ( nsubs == 0 ) {
		// Return an empty list (should not happen).
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine,
			    _gtype + "Created 0 graphs from TSProduct (no enabled subproducts defined)." );
		}
		return new ArrayList<> ( 1 );
	}

	// For now, assume that graphs will be listed vertically with the first one on top.

	// Loop through the number of graphs and create a TSGraph for each one.
	// For each TSID, locate the time series in the supplied time series list, or set to null if not available.
	// That way all of the data properties in the TSProduct will exactly agree with the list of TS.

	TSGraph tsgraph = null;
	// Initialize with the number of enabled sub-products.
	double height = drawlim_graphs.getHeight()/nsubs;
	if ( _is_reference_graph ) {
		// Reset the height to the full height since we currently only allow one reference graph on a component.
		height = drawlim_graphs.getHeight();
	}
	GRLimits drawlim = null;
	List<TS> tslist = null;
	int nts = 0;
	if ( tsproduct_tslist != null ) {
		nts = tsproduct_tslist.size();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "There are " + nts + " time series associated with the TSProduct" );
	}
	TS ts, tsfound;
	String prop_val;
	List<TSGraph> tsgraphs = new ArrayList<> ( nsubs );
	// This is the value for the tslist that is used by the graph, NOT the value in the entire list.
	int reference_ts_index = -1;

	int display_props_reference_ts_index = -1;
	String prop_value = displayProps.getValue("ReferenceTSIndex");
	if ( prop_value != null ) {
		display_props_reference_ts_index = StringUtil.atoi ( prop_value );
	}
	prop_value = tsproduct.getLayeredPropValue ( "ShowDrawingAreaOutline", -1, -1, false );
	boolean showDrawingAreaOutline = false;
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
		showDrawingAreaOutline = true;
	}

	// Indicate the subproduct that will be used to get reference graph information.
	_reference_sub = -1;

	// Reset nsubs to all the products (even disabled, so that the product indexes work out).
	nsubs = tsproduct.getNumSubProducts();
	String TSID_prop_val; // To hold value of TSID.
	// Indicates whether to check the input fields of the TSID against available time series.
	boolean check_input;
	String TSAlias_prop_val; // To hold value of TSAlias.

	for ( int isub = 0; isub < nsubs; isub++ ) {
		// Set the drawing limits for the graph on the page as an even fraction of the whole page in the vertical direction
		// (taking up the full horizontal dimension).
		// Later can allow more options to place graphs on the page.
		// The drawing limits will be reset whenever the component size changes.
		// The first graph is at the top of the page, then going down.
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
		// Loop through the data properties for the subproduct looking for "TSID" and "TSAlias" properties.
		// If a TSProduct was passed in the constructor, it is possible that a "TS" property was set to a time series,
		// but this functionality is not being used yet (NEED TO SUPPORT LATER).
		tslist = new Vector<>(); // Need new list for every graph.
		// If cannot match a TSID for the current graph, then the graph is NOT used for a reference graph.
		reference_ts_index = -1;
		for ( int jtsid = 0; ; jtsid++ ) { // Loop for TSIDs in graph.
			// First get the TSID.
			TSID_prop_val = tsproduct.getLayeredPropValue (	"TSID", isub, jtsid, false );
			TSAlias_prop_val = tsproduct.getLayeredPropValue ( "TSAlias", isub, jtsid, false );
			// Allow one or the other (but not both) to be missing.
			if ( (TSID_prop_val == null) &&	(TSAlias_prop_val == null) ) {
				// Done with data for the graph.
				break;
			}
			if ( TSID_prop_val == null ) {
				// Assign blank.
				TSID_prop_val = "";
			}
			if ( TSAlias_prop_val == null ) {
				// Assign blank.
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
			    Message.printDebug ( 2, routine, _gtype + "Looking for time series needed for graph:  TSID_prop_val=\"" + TSID_prop_val +
                    "\" TSAlias_prop_val = \"" + TSAlias_prop_val + "\"." );
			}
			// Now find a matching time series in the available data.
			// If a match is not found, set the time series to null so the properties line up.
			tsfound = null;
			for ( int kts = 0; kts < nts; kts++ ) {
				ts = tsproduct_tslist.get(kts);
				if ( ts == null ) {
					continue;
				}
				if ( Message.isDebugOn ) {
				    Message.printDebug ( 2, routine, _gtype + "Comparing to TS in available data: " +
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
				    // If an alias is specified, just match the alias.
				    if ( ts.getAlias().equalsIgnoreCase( TSAlias_prop_val) ) {
				        if ( Message.isDebugOn ) {
				            Message.printStatus ( 2, routine, _gtype + "Time series aliases match.");
				        }
					    tsfound = ts;
                    }
                }
                else {
					// No alias so use the full TSID with input type.
					if ( ts.getIdentifier().equals( TSID_prop_val,check_input) ) {
					    if ( Message.isDebugOn ) {
					        Message.printDebug ( 2, routine, _gtype + "Time series identifiers match.");
					    }
                        tsfound = ts;
                    }
                }
                if ( tsfound != null ) {
                	if ( Message.isDebugOn ) {
                		Message.printDebug ( 1, routine, _gtype + "Found a time series, display_props_reference_ts_index=" + display_props_reference_ts_index);
                	}
					if(	display_props_reference_ts_index == kts ) {
						// Set the TSID index within the graph's TS indicating which main graph TS is in the the reference graph.
						// If a main graph, the reference graph is indicated in the legend.
						reference_ts_index = jtsid;
						_reference_sub = isub;
					}
					break;
				}
				else {
				    if ( Message.isDebugOn ) {
				        Message.printDebug ( 2, routine, _gtype + "TSIDs and TSAliases are not equal");
				    }
				}
			}
			// Could put the following in the if statement in the loop but for now put here.
			//
			// If the "TSIndex" property is found, just use it to match up the time series.
			// This works well when a PropList and list of TS is used to create the TSProduct, as long as only one graph is used.
			// Do this after the above loop so that the reference graph can be set up
			// (note that there is a possibility of the reference graph using the wrong TS because of
			// duplicate IDs but that usually won't be that much of a problem).
			//
			// This approach might be extended to true TSProduct processing if we can assume that a TSID is always
			// matched with a TS (even if null) and that the order of the TSID in the product description file matches
			// the order of the time series passed in.
			prop_val = tsproduct.getLayeredPropValue ( "TSIndex", isub, jtsid, false );
			if ( (nsubs == 1) && (prop_val != null) && StringUtil.isInteger(prop_val) ) {
				tsfound = tsproduct_tslist.get( StringUtil.atoi(prop_val) );
			}
			// Now add the time series or null reference.
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
			tslist.add ( tsfound );
		}
		// Supply the short time series list for the graph and add the graph to the main list to be managed.
		tsgraph = new TSGraph ( this, drawlim, tsproduct, displayProps, isub, tslist, reference_ts_index );
		tsgraphs.add ( tsgraph );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Added graph [" + isub + "] reference_ts_index = " + reference_ts_index);
		}
		tsgraph.setShowDrawingAreaOutline(showDrawingAreaOutline);
	}
	//Message.printStatus(2,"TSGraphJComponent.createTSGraphsFromTSProduct",
	//_gtype + "Created " + tsgraphs.size() + " graphs from TSProduct" );
	return tsgraphs;
}

/**
Create a TSProduct from a simple old-style PropList. The member data _tsproduct is created.
It is assumed that a single graph is created from the PropList, using old-style conventions.
Old-style PropList properties for TSViewFrame are transferred to TSProduct properties.
Any properties that match expanded TSProduct properties are transferred as is.
@param proplist PropList passed in during construction.
@param a TSProduct containing values from the PropList.  If the list is null,
an empty TSProduct is created.
The checkTSProduct() method assigns appropriate default values for the resulting TSProduct.
@param tslist List of time series for the product.
*/
private TSProduct createTSProductFromPropList ( PropList proplist, List<TS> tslist ) {
	String routine = getClass().getSimpleName() + ".createTSProductFromPropList";

    try {

	//Message.printStatus ( 2, "", "Creating TSProduct from PropList and TS list" );
	// Create a new TSProduct.
	TSProduct tsproduct = new TSProduct ();
	tsproduct.setTSList ( tslist );
	if ( proplist == null ) {
		Message.printStatus(2,routine,"Creating product from property list - no properties to process.");
		return tsproduct;
	}

	// The graph type and other major properties are interpreted as being set by the user because the user initially picked the graph type.
	// If this is not set as a user-defined property (and is instead set as a run-time default) it will not be saved.

	int how_set_prev = tsproduct.getPropList().getHowSet();
	tsproduct.getPropList().setHowSet ( Prop.SET_AT_RUNTIME_FOR_USER );

	//---------------------------------------------------------------------
	// Product properties
	//---------------------------------------------------------------------

	// Currently only graph product types are recognized so set the type.
	// Interpret the graph height and width as total values in the TSProduct since we are assuming one graph.

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

	// Transfer "Product." properties.

	List<Prop> v = proplist.getPropsMatchingRegExp ( "Product.*" );
	int size = 0;
	if ( v != null ) {
		size = v.size();
	}
	Prop prop = null;
	PropList tsproduct_props = tsproduct.getPropList();
	for ( int i = 0; i < size; i++ ) {
		prop = v.get(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	//---------------------------------------------------------------------
	// SubProduct properties
	//---------------------------------------------------------------------

	prop_val = proplist.getValue ( "GraphType" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "GraphType", prop_val, 0, -1 );
	}
	else {
	    // Default...
		tsproduct.setPropValue ( "GraphType", "Line", 0, -1 );
	}

	prop_val = proplist.getValue ( "Title" );	// Older.
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "MainTitleString", prop_val, 0, -1 );
	}

	prop_val = proplist.getValue ( "TitleString" );	// Newer.
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "MainTitleString", prop_val, 0, -1 );
	}

	// "XAxis.Format" and "XAxisFormat" -> BottomXAxisLabelFormat

	prop_val = proplist.getValue ( "XAxis.Format" );
	if ( prop_val != null ) {
		tsproduct.setPropValue ( "BottomXAxisLabelFormat", prop_val, 0, -1 );
	}
	else {
	    // Older version...
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

	// Transfer "SubProduct" properties.

	v = proplist.getPropsMatchingRegExp ( "SubProduct *" );
	size = 0;
	if ( v != null ) {
		size = v.size();
	}
	for ( int i = 0; i < size; i++ ) {
		prop = v.get(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	// Associate data for the time series.

	int nts = 0;
	if ( tslist != null ) {
		nts = this._tslist.size();
	}
	TS ts;
	int how_set_prev2 = tsproduct.getPropList().getHowSet();
	for ( int i = 0; i < nts; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			tsproduct.setPropValue ( "TSID", "", 0, i );
		}
		else {
            tsproduct.setPropValue ( "TSID", ts.getIdentifier().toString(true), 0, i );
			// Set the alias if available.
			if ( !ts.getAlias().equals("") ) {
				tsproduct.setPropValue ( "TSAlias",	ts.getAlias(), 0, i );
			}
			tsproduct.getPropList().setHowSet (	Prop.SET_AS_RUNTIME_DEFAULT );
			// Set the index.
			// This is a more robust way to connect the time series with graphs when the time series come in from a list and PropList.
			tsproduct.setPropValue ( "TSIndex", "" + i, 0, i );
			tsproduct.getPropList().setHowSet ( how_set_prev2 );
			//Message.printStatus ( 2, "", "Graph has TSID \"" + ts.getIdentifier().toString(true) + "\"" );
		}
	}

	// Transfer "Data" properties in case these are being set manually in the calling code.

	v = proplist.getPropsMatchingRegExp ( "Data *" );
	size = 0;
	if ( v != null ) {
		size = v.size();
	}
	for ( int i = 0; i < size; i++ ) {
		prop = v.get(i);
		tsproduct_props.set ( prop.getKey(), prop.getValue() );
	}

	//---------------------------------------------------------------------
	// Display properties
	//
	// Properties that are treated as override properties in the TSProduct because they control GUI appearance.
	//---------------------------------------------------------------------

	// Other properties that might be passed in by TSViewGraphFrame.
	//
	// MaximizeGraphSpace - default true
	// Graph.EnableTracker - default true

	this._displayProps = new PropList ( "display" );

	// Double buffering is seldom specified.  The default is true (set in the base class).
	this._double_buffering = true;
	prop_val = proplist.getValue ( "DoubleBuffer" );
	if ( (prop_val != null) && prop_val.equalsIgnoreCase("false") ) {
		this._double_buffering = false;
	}

	// A reference graph is used for zooming and has few other features besides data.
	prop_val = proplist.getValue ( "ReferenceGraph" );
	if ( prop_val != null ) {
		this._displayProps.set ( "ReferenceGraph=" + prop_val );
	}

	prop_val = proplist.getValue ( "ReferenceTSIndex" );
	if ( prop_val != null ) {
		this._displayProps.set ( "ReferenceTSIndex=" + prop_val );
	}

	Object propObject = proplist.getContents ( "TSViewParentUIComponent" );
	if ( propObject != null ) {
		this._displayProps.setUsingObject ( "TSViewParentUIComponent", propObject );
	}

	// Image passed in when processing products in batch mode.

	this._external_Image = (BufferedImage)proplist.getContents("Image");
	if ( this._external_Image != null ) {
		Message.printDebug( 1, "", _gtype + "Using external Image for drawing." );
		// Disable reference time series (don't want labels in legend).
		this._displayProps.set ( "ReferenceTSIndex=" + -1 );
	}

	tsproduct.getPropList().setHowSet ( how_set_prev );

	// Return the new TSProduct.
	return tsproduct;

	}
	catch ( Exception e ) {
		// Should never happen.
		Message.printWarning(3, routine, "Exception creating TSProduct from properties (" + e + ").");
		Message.printWarning(3, routine, e);
	}
	return null;
}

/**
Clear the entire graph.
Need to do this manually rather than rely on default update() to make sure it happens at the right time.
This uses Graphics calls on the entire window.
*/
private void clearView () {
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.clearView", _gtype +	"Clearing the graph." );
	}
	if ( !_printing && (_graphics != null) ) {
		// Fill in the background color.  Need this because update() does not do (because of zooming).
		_graphics.setColor ( getBackground() );
		_bounds = getBounds();
		_graphics.fillRect ( 0, 0, _bounds.width, _bounds.height );
	}
}

/**
 * Notifies TSGraphEditor of point edit.
 *
 * @param event
 * @param tsgraph
 */
private void editPoint ( MouseEvent event, TSGraph tsgraph ) {
  GRLimits daLimits = tsgraph.getLeftYAxisGraphDrawingArea().getPlotLimits( GRCoordinateType.DEVICE);
  if ( editPointIsInside(event, daLimits) ) {
      GRPoint datapt = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY( event.getX(), event.getY(), GRCoordinateType.DEVICE );
      _tsGraphEditor.editPoint(datapt);
    }
}

/**
 * Indicate whether mouse is inside drawing area.
 * @param event Mouse event such as click
 * @param grLimits drawing area limits to check
 * @return true if the mouse is inside the drawing area
 */
private final boolean editPointIsInside ( MouseEvent event, GRLimits grLimits ) {
  return (
	(event.getX() > (int)grLimits.getLeftX())
      && (event.getX() < (int)grLimits.getRightX())
      && (event.getY() > (int)grLimits.getTopY())
      && (event.getY() < (int)grLimits.getBottomY()))
    ?true:false;
}

/**
Creates a list TSGraphDataLimits for each graph, where each object stores information for corresponding graphs.
The number of time series associated with the graph, the ids of the time series associated with the graph,
and the data limits of the graph.
This is done so that the graphs can be rebuilt properly during a call to reinitializeGraphs().
The data are used in resetGraphDataLimits().
*/
private List<TSGraphDataLimits> determineDataLimits() {
	List<TSGraphDataLimits> dataLimits = new ArrayList<>();
	List<String> ids = null;
	List<TS> tslist = null;

	for (int i = 0; i < _tsgraphs.size(); i++) {
		tslist = _tsgraphs.get(i).getTSList();

		ids = new Vector<>();
		for ( TS ts : tslist ) {
			if (ts == null) {
				ids.add("null");
			}
			else {
				ids.add(ts.getIdentifierString());
			}
		}
		ids = StringUtil.sortStringList(ids);
		dataLimits.add(new TSGraphDataLimits(tslist.size(),ids,_tsgraphs.get(i).getDataLimits()));
	}

	return dataLimits;
}

/**
Draw the drawing area boundaries, for troubleshooting.
*/
public void drawDrawingAreas () {
	_da_page.setColor ( GRColor.cyan );
	// Reference and main.
	GRDrawingAreaUtil.drawRectangle ( _da_graphs,
				_datalim_graphs.getLeftX(),	_datalim_graphs.getBottomY(),
				_datalim_graphs.getWidth(),	_datalim_graphs.getHeight() );
	if ( _is_reference_graph ) {
		// Don't need to draw anything else.
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

// TODO sam 2017-03-03 could move this to the TSGraphJComponentGlassPane class if suitable
// accessor methods were in place to get data out of TSGraphJComponent.
/**
 * Draw the mouse tracker.
 * Currently the focus is to draw the glass pane tracker here but may put in the tracker data and draw elsewhere.
 * @param trackerData mouse tracker data to pass back to calling code,
 * typically a TSGraphJComponentGlassPaneMouseListener
 * @param devx device (JComponent) pixel coordinate (0=left edge) of mouse motion
 * @param devy device (JComponent) pixel coordinate (0=top edge) of mouse motion
 */
protected void drawMouseTracker(TSGraphJComponentGlassPane glassPane, Graphics2D g, int devx, int devy ) {
	// Only track if requested to do so.
	TSGraphMouseTrackerType trackerType = glassPane.getMouseTrackerType();
	if ( trackerType == TSGraphMouseTrackerType.NONE ) {
		return;
	}
	// Recreate drawing areas for the glass pane consistent with this component:
	// - only the drawing areas for the graphs are needed
	// - this is perhaps inefficient but ensures agreement with this class, in particular if the
	//   graph has resized or been split, etc.
	// - whereas the drawing areas are grouped by TSGraph in this component,
	//   they are in a generic list in the glass pane component and are looked up by name,
	//   prepended with the TSGraph list position
	// TODO sam 2017-03-03 figure out where to create the drawing areas so they are recreated each time this method is called.
	for ( int itsgraph = 1; itsgraph <= _tsgraphs.size(); itsgraph++ ) {
		TSGraph tsgraph = _tsgraphs.get(itsgraph - 1);
		// Left y-axis drawing area:
		// - give it a name GraphNum.LeftDaName
		GRJComponentDrawingArea da = new GRJComponentDrawingArea ( glassPane,
			"" + itsgraph + "." + tsgraph.getLeftYAxisGraphDrawingArea().getName(),
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
		da.setDataLimits(new GRLimits(tsgraph.getLeftYAxisGraphDrawingArea().getDataLimits()));
		da.setDrawingLimits(new GRLimits(tsgraph.getLeftYAxisGraphDrawingArea().getDrawingLimits()),GRUnits.DEVICE, GRLimits.DEVICE);
		GRDrawingAreaUtil.setAxes(da, tsgraph.getLeftYAxisGraphDrawingArea().getXAxisType(), tsgraph.getLeftYAxisGraphDrawingArea().getYAxisType());
		glassPane.addDrawingArea(da, true);
		// Right y-axis drawing area.
		if ( tsgraph.getRightYAxisGraphDrawingArea() != null ) {
			da = new GRJComponentDrawingArea ( glassPane,
				"" + itsgraph + "." + tsgraph.getRightYAxisGraphDrawingArea().getName(),
				GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
			da.setDataLimits(new GRLimits(tsgraph.getRightYAxisGraphDrawingArea().getDataLimits()));
			da.setDrawingLimits(new GRLimits(tsgraph.getRightYAxisGraphDrawingArea().getDrawingLimits()),GRUnits.DEVICE, GRLimits.DEVICE);
			GRDrawingAreaUtil.setAxes(da, tsgraph.getRightYAxisGraphDrawingArea().getXAxisType(), tsgraph.getRightYAxisGraphDrawingArea().getYAxisType());
			glassPane.addDrawingArea(da, true);
		}
	}
	// Loop through TSGraph and see if the device point fits in the drawing area.
	// If so, draw the tracker on the glass pane according to mouse tracker rules.
	int itsgraph = -1;
	TSData tsdata = null; // Data point extracted from time series.
	int devHeight = this.getHeight(); // Device units need to be transformed since graphics uses y=0 at top.
	boolean drawTrackerLine = false; // Only draw when in graph type that supports tracker.
	for ( TSGraph tsgraph : _tsgraphs ) {
		++itsgraph;
		GRDrawingArea daLeftYAxisGraph = tsgraph.getLeftYAxisGraphDrawingArea();
		GRDrawingArea daRightYAxisGraph = tsgraph.getRightYAxisGraphDrawingArea();
		TSGraphType leftYAxisGraphType = tsgraph.getLeftYAxisGraphType();
		TSGraphType rightYAxisGraphType = tsgraph.getRightYAxisGraphType();
		// Loop through a list of the left and/or right y-axis drawing areas, maximum of 2 in the list.
		List<GRDrawingArea> daGraphList = new ArrayList<>();
		// Currently can only handle graph types that are one of the following because additional
		// logic would be needed to handle the plotting positions of other graph types:
		// - Line
		// - Area
		// - Bar
		// - Point
		// - Raster (only on left Y axis)
		if ( daLeftYAxisGraph != null ) {
			if (
				(leftYAxisGraphType == TSGraphType.AREA) ||
				(leftYAxisGraphType == TSGraphType.AREA_STACKED) ||
				(leftYAxisGraphType == TSGraphType.BAR) ||
				(leftYAxisGraphType == TSGraphType.LINE) ||
				(leftYAxisGraphType == TSGraphType.POINT) ||
				(leftYAxisGraphType == TSGraphType.RASTER) ) {
				daGraphList.add(daLeftYAxisGraph);
				drawTrackerLine = true;
			}
		}
		if ( daRightYAxisGraph != null ) {
			if (
				(rightYAxisGraphType == TSGraphType.AREA) ||
				(rightYAxisGraphType == TSGraphType.AREA_STACKED) ||
				(rightYAxisGraphType == TSGraphType.BAR) ||
				(rightYAxisGraphType == TSGraphType.LINE) ||
				(rightYAxisGraphType == TSGraphType.POINT) ) {
				//daGraphList.add(daRightYAxisGraph);
				drawTrackerLine = true;
			}
		}
		//if ( leftYAxisGraphType == TSGraphType.RASTER ) { // }
		if ( trackerType == TSGraphMouseTrackerType.XYAXES ) {
			// Raster tracker.  No reason for all the logic below.
			GRLimits daDrawLimits = daLeftYAxisGraph.getDrawingLimits();
			if ( daDrawLimits.contains(devx, (devHeight - devy)) ) {
				// Only draw the tracker if the mouse is inside the graphing area.
				drawMouseTrackerCrossHairs ( glassPane, g, daGraphList, devx, devy );
			}
			return;
		}
		// The following are used to indicate the nearest point to the mouse:
		// - sized for the number of time series on the axis being processed
		// - left and right axis are processed separately
		List<Double> distNearestList = null;
		List<TSData> tsdataNearestList = null;
		List<TS> tsNearestList = null;
		List<DateTime> dtNearestList = null;
		List<GRDrawingArea> daGraphNearestList = null;
		List<TSGraph> graphNearestList = null;
		List<Integer> itsgraphNearestList = null;
		for ( GRDrawingArea daGraph : daGraphList ) {
			// The time series for the axis may be original data or derived data, depending on the graph type.
			List<TS> tsForAxis = new ArrayList<>();
			if ( daGraph == daLeftYAxisGraph ) {
				// Include left axis.
				boolean includeLeftYAxis = true;
				boolean includeRightYAxis = false;
				if  ( leftYAxisGraphType == TSGraphType.AREA_STACKED ) {
					// Use derived time series data.
					tsForAxis = tsgraph.getEnabledDerivedTSList(includeLeftYAxis, includeRightYAxis);
				}
				else {
					// Use the original time series data.
					tsForAxis = tsgraph.getEnabledTSList(includeLeftYAxis, includeRightYAxis);
				}
			}
			else if ( daGraph == daRightYAxisGraph ) {
				// Include right axis.
				boolean includeLeftYAxis = false;
				boolean includeRightYAxis = true;
				if  ( rightYAxisGraphType == TSGraphType.AREA_STACKED ) {
					// Use derived time series data.
					tsForAxis = tsgraph.getEnabledDerivedTSList(includeLeftYAxis, includeRightYAxis);
				}
				else {
					// Use the original time series data.
					tsForAxis = tsgraph.getEnabledTSList(includeLeftYAxis, includeRightYAxis);
				}
			}
			if ( (trackerType == TSGraphMouseTrackerType.NEAREST_TIME)
				|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED) ) {
				// Reset the lists because time series are tracked on each axis.
				distNearestList = new ArrayList<>(tsForAxis.size());
				tsdataNearestList = new ArrayList<>(tsForAxis.size());
				tsNearestList = new ArrayList<>(tsForAxis.size());
				dtNearestList = new ArrayList<>(tsForAxis.size());
				daGraphNearestList = new ArrayList<>(tsForAxis.size());
				graphNearestList = new ArrayList<>(tsForAxis.size());
				itsgraphNearestList = new ArrayList<>(tsForAxis.size());
				for ( int i = 0; i < tsForAxis.size(); i++ ) {
					distNearestList.add(null);
					tsdataNearestList.add(null);
					tsNearestList.add(null);
					dtNearestList.add(null);
					daGraphNearestList.add(null);
					graphNearestList.add(null);
					itsgraphNearestList.add(null);
					// Set the instances to null.
				}
			}
			GRLimits daDrawLimits = daGraph.getDrawingLimits();
			GRLimits daDataLimits = daGraph.getDataLimits();
			boolean doDraw = false;
			if (
				(trackerType == TSGraphMouseTrackerType.NEAREST)
				|| (trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID)
				|| (trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED) ) {
				if ( daDrawLimits.contains(devx, (devHeight - devy)) ) {
					// Must contain X and Y coordinate.
					doDraw = true;
				}
			}
			else if (
				(trackerType == TSGraphMouseTrackerType.NEAREST_TIME)
				|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED) ) {
				// Only needs to contain X coordinate.
				if ( daDrawLimits.containsX(devx) ) {
					doDraw = true;
				}
			}
			if ( drawTrackerLine && doDraw ) {
				// Mouse coordinates are in the drawing area so draw a line vertically at the point where mouse is.
				g.setColor(Color.gray);
				g.drawLine(devx,(devHeight-(int)daDrawLimits.getBottomY()),devx,(devHeight-(int)daDrawLimits.getTopY()));
				// Loop through the time series and get the data closest to the horizontal coordinate.
				// First back-calculate the data x-coordinate (date) so it can be used to look up time series values.
				GRPoint datapt = daGraph.getDataXY( devx, devy, GRCoordinateType.DEVICE );
				// The X coordinate is a floating-point representation of the date/time.
				DateTime dt = new DateTime(datapt.getX(),true);
				//System.out.println("Mouse date/time=" + dt);
				// Loop through the time series and find the point that is nearest to the mouse.
				// To make this behave well, adjust the search period to align with time series interval and bracket the point:
				// - add several intervals each direction if finding nearest point because a rapid jump in the time series
				//    could lead to not finding the point
				int its = -1;
				for ( TS ts : tsForAxis ) {
					++its; // Always increment because array/list positions are important (don't "continue" before this).
					if ( (ts == null) || (ts.getDate1() == null) ) {
						// Time series has no data.
						continue;
					}
					if ( ((trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED)
						|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED))
						&& !tsgraph.isTimeSeriesSelected(ts) ) {
						// Only time series that are selected should be considered.
						continue;
					}
					int intervalBase = ts.getDataIntervalBase();
					int intervalMult = ts.getDataIntervalMult();
					DateTime searchStart = new DateTime(dt); // These will be adjusted below to set a search window.
					DateTime searchEnd = new DateTime(dt);
					if ( TimeInterval.isRegularInterval(ts.getDataIntervalBase()) ) {
						// Regular interval time series.
						searchStart.round(-1, intervalBase, intervalMult );
						searchStart.setPrecision ( ts.getDate1().getPrecision() );
						// N-hour interval where there is a time zone offset can be problematic,
						// for example 24-hour data aligned with hour 23 so shift if necessary.
						// This may result in extra intervals being processed during the search, but that is OK.
						if ( ts.getDate1().getPrecision() == TimeInterval.HOUR ) {
							if ( ts.getDate1().getHour()%ts.getDataIntervalMult() != 0 ) {
								searchStart.addHour(-(ts.getDataIntervalMult() - ts.getDate1().getHour()%ts.getDataIntervalMult()));
							}
						}
						if ( searchStart.equals(dt) ) {
							searchStart.addInterval(ts.getDataIntervalBase(), -ts.getDataIntervalMult());
						}
						searchEnd.round(1, intervalBase, intervalMult );
						searchEnd.setPrecision ( ts.getDate1().getPrecision() );
						if ( ts.getDate1().getPrecision() == TimeInterval.HOUR ) {
							if ( ts.getDate1().getHour()%ts.getDataIntervalMult() != 0 ) {
								searchEnd.addHour(-(ts.getDataIntervalMult() - ts.getDate1().getHour()%ts.getDataIntervalMult()));
							}
						}
						if ( searchEnd.equals(dt) ) {
							searchEnd.addInterval(ts.getDataIntervalBase(), ts.getDataIntervalMult());
						}
						if ( (trackerType == TSGraphMouseTrackerType.NEAREST)
							|| (trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID)
							|| (trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED) ) {
							// Extend the search window on each side a few intervals to try to match rapid jump.
							searchStart.addInterval(intervalBase, -10*intervalMult);
							searchEnd.addInterval(intervalBase, 10*intervalMult);
						}
					}
					else {
						// Irregular interval time series.
						// Find the nearest DateTime in the irregular time series and then expand the search window around that,
						// 2 data points on each side.
						IrregularTS irrts = (IrregularTS)ts;
						TSData tsdataIrr = irrts.findNearestNext ( dt, null, null, true );
						if ( tsdataIrr == null ) {
							// Likely that the time series period does not overlap the mouse coordinate.
							// Skip the time series.
							continue;
						}
						TSData tsdataIrr2 = tsdataIrr;
						if ( tsdataIrr.getPrevious() != null ) {
							tsdataIrr = tsdataIrr.getPrevious();
						}
						if ( tsdataIrr.getPrevious() != null ) {
							tsdataIrr = tsdataIrr.getPrevious();
						}
						if ( tsdataIrr2.getNext() != null ) {
							tsdataIrr2 = tsdataIrr2.getNext();
						}
						if ( tsdataIrr2.getNext() != null ) {
							tsdataIrr2 = tsdataIrr2.getNext();
						}
						searchStart = new DateTime(tsdataIrr.getDate());
						searchEnd = new DateTime(tsdataIrr2.getDate());
						if ( (trackerType == TSGraphMouseTrackerType.NEAREST)
							|| (trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID)
							|| (trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED) ) {
							// Extend the search window on each side a few intervals to try to match rapid jump.
							// TODO sam 2017-04-22 need to find a way to iterate back without being a performance hit.
						}
					}
					// Calculate the distance between the mouse and search start and end, for non-missing values.
					// Check the starting bounding value.
					//System.out.println("Searching for point xdev=" + devx + " , ydev=" + devy + " in range " + searchStart + " to " + searchEnd );
					TSIterator tsi = null;
					try {
						tsi = ts.iterator(searchStart, searchEnd);
					}
					catch ( Exception e ) {
						// Skip the time series.
						continue;
					}
					DateTime searchTime;
					for ( ; (tsdata = tsi.next()) != null; ) {
					//for ( DateTime searchTime = new DateTime(searchStart); searchTime.lessThanOrEqualTo(searchEnd);
						//searchTime.addInterval(ts.getDataIntervalBase(),ts.getDataIntervalMult())) { }
						//tsdata = ts.getDataPoint(searchTime, null);
						searchTime = tsdata.getDate();
						double tsvalue = tsdata.getDataValue();
						double xSearchTime = searchTime.toDouble();
						// Only consider non-missing points in the graph drawing area.
						if ( !ts.isDataMissing(tsvalue) && daDataLimits.containsX(xSearchTime)) {
							// Finding the closest point uses device (pixel) units:
							// - y axis in pixels is measured from top down
							double xDiff = devx - daGraph.scaleXData(xSearchTime);
							double tsValueAsPixel = daGraph.scaleYData(tsvalue);
							double yDiff = devy - tsValueAsPixel;
							if ( (trackerType == TSGraphMouseTrackerType.NEAREST_TIME)
								|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED) ) {
								// Ignore y-axis
								yDiff = 0.0;
							}
							double dist = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
							//Message.printStatus(2, "", "searchTime=" + searchTime + " tsvalue=" + tsvalue +
							//	" devx=" + devx + " timeAsX=" + daGraph.scaleXData(xSearchTime) +
							//	" devy=" + devy + " tsValueAsPixel0=" + tsValueAsPixel0 + " tsValueAsPixel=" + tsValueAsPixel + " xDiff="+xDiff + " yDiff="+yDiff + " dist=" + dist );//+ " TrackerType=" + trackerType + " daGraph=" + daGraph.getName());
							// Make copies of objects when new nearest is found so original instances won't be used (they are dynamic).
							if ( (trackerType == TSGraphMouseTrackerType.NEAREST)
								|| (trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID)
								|| (trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED) ) {
								// Only the single nearest point is drawn (multiple axes and graphs can't draw because only one can be nearest).
								if ( distNearestList == null ) {
									// First instance and only 0-index instance will be compared.
									distNearestList = new ArrayList<>();
									distNearestList.add(new Double(dist));
									tsdataNearestList = new ArrayList<>();
									tsdataNearestList.add(new TSData(tsdata));
									tsNearestList = new ArrayList<>();
									tsNearestList.add(ts);
									dtNearestList = new ArrayList<>();
									dtNearestList.add(new DateTime(searchTime));
									daGraphNearestList = new ArrayList<>();
									daGraphNearestList.add(daGraph);
									graphNearestList = new ArrayList<>();
									graphNearestList.add(tsgraph);
									itsgraphNearestList = new ArrayList<>();
									itsgraphNearestList.add(itsgraph);
								}
								else if ( dist < distNearestList.get(0) ) {
									// Don't care which time series or axis - find closest.
									// Found a closer point so save its data by replacing the previously-added value.
									distNearestList.set(0,new Double(dist));
									tsdataNearestList.set(0,new TSData(tsdata));
									tsNearestList.set(0,ts);
									dtNearestList.set(0,new DateTime(searchTime));
									daGraphNearestList.set(0,daGraph);
									graphNearestList.set(0,tsgraph);
									itsgraphNearestList.set(0,new Integer(itsgraph));
									//Message.printStatus(2, "", "Found nearest.");
								}
							}
							else if ( (trackerType == TSGraphMouseTrackerType.NEAREST_TIME)
								|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED)) {
								// Nearest point to time for each time series.
								if ( (distNearestList.get(its) == null) || (dist < (distNearestList.get(its))) ) {
									// Found a closer point for the specific time series so save its data.
									distNearestList.set(its,new Double(dist));
									tsdataNearestList.set(its,new TSData(tsdata));
									tsNearestList.set(its,ts);
									dtNearestList.set(its,new DateTime(searchTime));
									daGraphNearestList.set(its,daGraph);
									graphNearestList.set(its,tsgraph);
									itsgraphNearestList.set(its,new Integer(itsgraph));
								}
							}
						}
					}
				}
			}
			if ( (trackerType == TSGraphMouseTrackerType.NEAREST_TIME)
				|| (trackerType == TSGraphMouseTrackerType.NEAREST_TIME_SELECTED) ) {
				// Drawing nearest for all time series so draw for the drawing area:
				// - lists will be sized for the number of time series in the drawing area
				drawMouseTrackerPoints ( trackerType, glassPane,
					distNearestList, tsNearestList, dtNearestList,
					tsdataNearestList, daGraphNearestList, itsgraphNearestList);
			}
		}
		if ( (trackerType == TSGraphMouseTrackerType.NEAREST)
			|| (trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID)
			|| (trackerType == TSGraphMouseTrackerType.NEAREST_SELECTED)) {
			// Drawing nearest time series point(s), which could be a time series in left or right y-axis graph:
			// - lists will only have one item
			drawMouseTrackerPoints ( trackerType, glassPane,
				distNearestList, tsNearestList, dtNearestList,
				tsdataNearestList, daGraphNearestList, itsgraphNearestList);
		}
	} // End TSGraphs loop - will be either left, right, or left and right drawing areas for TSProduct graph.
}

/**
 * Draw the mouse tracker as cross-hairs that extend to each axis.
 * In the future may allow control for only using one axis or other crosshair behavior.
 * @param glassPane the glass pane on which to draw
 * @param g Graphics2D for drawing
 * @param daGraphList list of drawing areas to draw in (known to contain the point)
 * @param devx device (JComponent) pixel coordinate (0=left edge) of mouse motion
 * @param devy device (JComponent) pixel coordinate (0=top edge) of mouse motion
 */
private void drawMouseTrackerCrossHairs ( TSGraphJComponentGlassPane glassPane, Graphics2D g,
	List<GRDrawingArea> daGraphList, int devx, int devy ) {
	// Draw using drawing area corresponding to the graph drawing area:
	// - note that TSGraph index is 1-offset to match TSProduct conventions
	// For now this is being used only for raster so draw in the first drawing area.
	GRDrawingArea da0 = daGraphList.get(0);
	String daName = "1." + da0.getName();
	Message.printStatus(2,"", "daName=" + daName);
	GRDrawingArea da = glassPane.getDrawingArea(daName);
	if ( da == null ) {
		// Should not happen.
		//Message.printStatus(2,"", "Unable to find drawing area \"" + daName + "\"");
		//System.out.println("Unable to find drawing area \"" + daName + "\"");
	}
	else {
		//System.out.println("Drawing tracker in DA \"" + daName + "\" at " + dtNearest + ", " + tsdataNearest.getDataValue());
		//System.out.println("DA data limits: " + da.getDataLimits());
		//System.out.println("DA drawing limits: " + da.getDrawingLimits());
		GRLimits daDrawLimits = da.getDrawingLimits();
		int devHeight = this.getHeight(); // Device units need to be transformed since graphics uses y=0 at top.
	    // Vertical line.
		g.setColor(Color.gray);
		g.drawLine(
			devx,
			(devHeight - (int)daDrawLimits.getBottomY()),
			devx,
			(devHeight - (int)daDrawLimits.getTopY()));
	    // Horizontal line.
		g.drawLine(
			(int)daDrawLimits.getLeftX(),
			devy,
			(int)daDrawLimits.getRightX(),
			devy);
	}
}

/**
 * Determine the label to show for the mouse tracker.
 */
private String drawMouseTrackerLabel ( TSGraphMouseTrackerType trackerType, TS ts, TSData tsdata ) {
	DateTime dt = tsdata.getDate();
	double value = tsdata.getDataValue();
	String flag = tsdata.getDataFlag();
	String flagString = "";
	String valueString = "";
	String traceString = "";
	if ( (flag != null) && !flag.equals("") ) {
	    flagString = " (" + flag + ") ";
	}
    // TODO SAM 2013-07-31 Need to figure out precision from data, but don't look up each
    // call to this method because a performance hit?
    valueString = StringUtil.formatString(value,"%.2f");
	String dateTimeString = ", " + dt;
	if ( !ts.getSequenceID().isEmpty() ) {
		traceString = " [" + ts.getSequenceID() + "]";
	}
	String idString = "";
	if ( trackerType == TSGraphMouseTrackerType.NEAREST_WITH_ID ) {
		if ( (ts.getAlias() != null) && !ts.getAlias().isEmpty() ) {
			idString = ", " + ts.getAlias();
		}
		else {
			idString = ", " + ts.getIdentifierString();
		}
	}
	String pointLabel = valueString + flagString + dateTimeString + traceString + idString;
	return pointLabel;
}

/**
 * Draw the mouse tracker points on the tracker glass pane.
 * @param glassPane glass pane on which the tracker is drawn
 * @param distNearestList
 * @param tsNearestList
 * @param dtNearestList
 * @param tsdataNearestList
 * @param daGraphNearestList
 * @param itsgraphNearestList
 */
private void drawMouseTrackerPoints (
	TSGraphMouseTrackerType trackerType,
	TSGraphJComponentGlassPane glassPane,
	List<Double> distNearestList, List<TS> tsNearestList, List<DateTime> dtNearestList,
	List<TSData> tsdataNearestList, List<GRDrawingArea> daGraphNearestList, List<Integer> itsgraphNearestList ) {
	if ( distNearestList != null ) {
		// Have something to draw.
		//System.out.println("Have " + distNearestList.size() + " points to draw");
		for ( int i = 0; i < distNearestList.size(); i++ ) {
			Double distNearest = distNearestList.get(i);
			if ( distNearest == null ) {
				// There was not a value at the time series near the mouse,
				// perhaps due to missing data, so can't draw anything.
				// TODO sam 2017-03-04 decide whether the search period should be wider.
				continue;
			}
			TS tsNearest = tsNearestList.get(i);
			DateTime dtNearest = dtNearestList.get(i);
			TSData tsdataNearest = tsdataNearestList.get(i);
			GRDrawingArea daGraphNearest = daGraphNearestList.get(i);
			Integer itsgraphNearest = itsgraphNearestList.get(i);
			//System.out.println("Nearest date/time is " + dtNearest + ", value=" + tsdataNearest.getDataValue() );
			String trackerLabel = drawMouseTrackerLabel ( trackerType, tsNearest, tsdataNearest );
			// Draw the time series data point as a larger symbol.
			// TODO smalers 2017-03-03 it is tricky to find value of its because sublist of axis time series
			// is processed rather than full TSProduct list.  For now hard-code size.
			//String symbolSizeProp = graphNearest.getLayeredPropValue("SymbolSize", itsgraph, its, false, null);
			String symbolSizeProp = "8";
			int symbolSize = 8;
			if ( (symbolSizeProp == null) || symbolSizeProp.isEmpty() ) {
				symbolSize = 8;
			}
			else {
				symbolSize = Integer.parseInt(symbolSizeProp);
				if ( symbolSize == 0 ) {
					symbolSize = 8;
				}
				else {
					// Increase the time series symbol size by 2 pixels on each edge (4 total).
					symbolSize = symbolSize + 4;
				}
			}
			// Draw using drawing area corresponding to the graph drawing area:
			// - note that TSGraph index is 1-offset to match TSProduct conventions
			String daName = "" + (itsgraphNearest + 1) + "." + daGraphNearest.getName();
			GRDrawingArea da = glassPane.getDrawingArea(daName);
			if ( da == null ) {
				// Should not happen.
				//System.out.println("Unable to find drawing area \"" + daName + "\"");
			}
			else {
				//System.out.println("Drawing tracker in DA \"" + daName + "\" at " + dtNearest + ", " + tsdataNearest.getDataValue());
				//System.out.println("DA data limits: " + da.getDataLimits());
				//System.out.println("DA drawing limits: " + da.getDrawingLimits());
				GRDrawingAreaUtil.setColor(da, new GRColor(211,211,211,200));
				GRDrawingAreaUtil.setFont ( da, "Helvetica", "Bold", 12);
				// Get the text extents
				GRLimits textExtents = GRDrawingAreaUtil.getTextExtents ( da, trackerLabel, GRUnits.DATA );
				// It is OK to let the tracker text overflow outside the graph because there is usually enough boundary due to axis labels.
				// If through experience this is problematic, add additional logic below to improve the user experience.
				int textAlign = GRText.LEFT;
				if ( (dtNearest.toDouble() + textExtents.getWidth()) > da.getDataLimits().getRightX() ) {
					textAlign = GRText.RIGHT;
				}
				if ( (tsdataNearest.getDataValue() + textExtents.getHeight()/2) > da.getDataLimits().getTopY() ) {
					// The text will extend above the graph, which might overlap with legend or titles.
					// Therefore, draw the tracker text below the top edge.
					GRDrawingAreaUtil.drawSymbolText(da, GRSymbolShapeType.CIRCLE_FILLED, dtNearest.toDouble(), tsdataNearest.getDataValue(),
						symbolSize, trackerLabel, GRColor.black, 0.0, textAlign | GRText.TOP,
						GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
				}
				else {
					// Normally, draw the text center-left of the point.
					GRDrawingAreaUtil.drawSymbolText(da, GRSymbolShapeType.CIRCLE_FILLED, dtNearest.toDouble(), tsdataNearest.getDataValue(),
						symbolSize, trackerLabel, GRColor.black, 0.0, textAlign | GRText.CENTER_Y,
						GRUnits.DEVICE, GRSymbolPosition.CENTER_X | GRSymbolPosition.CENTER_Y );
				}
			}
		}
	}
}

/**
Draw the titles for the component.
The properties are retrieved again in case they have been reset by a properties GUI.
*/
private void drawTitles () {
	if ( _is_reference_graph ) {
		// Don't need to draw anything.
		return;
	}

	// Main title.

	_da_maintitle.setColor ( GRColor.black );
	String maintitle_font = _tsproduct.getLayeredPropValue ( "MainTitleFontName", -1, -1, false );
	String maintitle_fontstyle = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", -1, -1, false );
	String maintitle_fontsize = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", -1, -1, false );
	GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font, maintitle_fontstyle,
		StringUtil.atod(maintitle_fontsize) );
	String maintitle_string = _tsproduct.expandPropertyValue(
	    _tsproduct.getLayeredPropValue ( "MainTitleString", -1, -1, false));
	GRDrawingAreaUtil.drawText ( _da_maintitle, maintitle_string,
		_datalim_maintitle.getCenterX(), _datalim_maintitle.getCenterY(), 0.0, GRText.CENTER_X|GRText.CENTER_Y );

	// Sub title.

	_da_subtitle.setColor ( GRColor.black );
	String subtitle_font = _tsproduct.getLayeredPropValue ( "SubTitleFontName", -1, -1, false );
	String subtitle_fontstyle = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", -1, -1, false );
	String subtitle_fontsize = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", -1, -1, false );
	GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font, subtitle_fontstyle, StringUtil.atod(subtitle_fontsize) );
	String subtitle_string = _tsproduct.expandPropertyValue(
	    _tsproduct.getLayeredPropValue ( "SubTitleString", -1, -1, false ));
	GRDrawingAreaUtil.drawText ( _da_subtitle, subtitle_string,
		_datalim_subtitle.getCenterX(),	_datalim_subtitle.getCenterY(), 0.0,GRText.CENTER_X|GRText.CENTER_Y );
}

/**
Draw the footers for the component.  Nothing is done at this time.
*/
private void drawFooters () {
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
	_displayProps = null;
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
	_tsgraphs = new Vector<>();
	_gtype = null;
	IOUtil.nullArray(_listeners);
	_mouse_tsgraph1 = null;
	super.finalize();
}

/**
Determine the TSGraph that an event occurred in, checking only the graph drawing areas in the TSGraph.
@return the TSGraph that an event occurred in or null if not within the bounds of a TSGraph.
@param pt point for the raw device units where the event occurred.
*/
private TSGraph getEventTSGraph ( GRPoint pt ) {
	return getEventTSGraph ( pt, true, false );
}

/**
Determine the TSGraph that an event occurred in, checking only the graph drawing areas in the TSGraph.
@return the TSGraph that an event occurred in or null if not within the bounds of a TSGraph.
@param pt point for the raw device units where the event occurred, such as mouse click.
@param includeGraphArea if true, evaluate whether the click was in the main graph drawing area.
@param includePage if true, evaluate whether the click was anywhere on the device (page).
*/
private TSGraph getEventTSGraph ( GRPoint pt, boolean includeGraphArea, boolean includePage ) {
	int size = _tsgraphs.size();
	TSGraph tsgraph = null;
	if ( includeGraphArea ) {
		for ( int isub = 0; isub < size; isub++ ) {
			tsgraph = _tsgraphs.get(isub);
			if ( _is_reference_graph && (isub != _reference_sub) ) {
				// Don't check the graph.
				continue;
			}
			if ( tsgraph.getLeftYAxisGraphDrawingArea().getPlotLimits(GRCoordinateType.DEVICE).contains(pt) ) {
				return tsgraph;
			}
		}
	}
	if ( includePage ) {
		// Did not find the graph area above but also check full page.
		for ( int isub = 0; isub < size; isub++ ) {
			tsgraph = _tsgraphs.get(isub);
			if ( _is_reference_graph && (isub != _reference_sub) ) {
				// Don't check the graph.
				continue;
			}
			if ( tsgraph.getPageDrawingArea().getPlotLimits(GRCoordinateType.DEVICE).contains(pt) ) {
				return tsgraph;
			}
		}
	}
	return null;
}

/**
Return the graph interaction mode.
@return the interaction type (see INTERACTION_*).
*/
public TSGraphInteractionType getInteractionMode () {
	return _interaction_mode;
}

/**
Return the JFrame that includes this component.
This can be used, for example, to display a dialog in lower-level code.
@return the JFrame for the component.
*/
protected JFrame getJFrame() {
	return _parent;
}

/**
 * Return the reference graph X axis zoom history.
 */
protected TSViewZoomHistory getReferenceGraphZoomHistory () {
	return this.zoomHistory;
}

/**
 * Return the list of graphs being drawn.
 * Use of the returned object should generally be for information given that painting will be done in the graph code.
 */
public List<TSGraph> getTSGraphs () {
	return _tsgraphs;
}

/**
Return the TSProduct that corresponds to the TSGraphJComponent.
A TSProduct is either passed in during construction (new convention) or is created from a PropList (old convention) during construction.
@return TSProduct associated with the component.
*/
public TSProduct getTSProduct () {
	return _tsproduct;
}

/**
Indicate whether this component is currently printing.
@return true if the component is currently being printed, false if not.
*/
public boolean isPrinting () {
	return _printing;
}

/**
 * Indicate whether this component contains a raster graph.
 * Currently TSTool only allows one raster graph so can do this check.
 * This will need to be revisited in the future if raster graphs can be mixed with other graphs on a product page.
 */
public boolean isRasterGraph() {
	List<TSGraph> tsgraphs = getTSGraphs();
	boolean isRaster = false;
	for ( TSGraph tsgraph : tsgraphs ) {
		// If any are raster, return true.
		if ( tsgraph.getLeftYAxisGraphType() == TSGraphType.RASTER ) {
			isRaster = true;
			break;
		}
	}
	return isRaster;
}

/**
Indicate whether this component is a reference graph.
@return true if the component contains a reference graph, false if not.
*/
public boolean isReferenceGraph () {
	return _is_reference_graph;
}

/**
Respond to KeyEvents.
Most single-key events are handled in keyReleased to prevent multiple events.  Do track when the shift is pressed here.
*/
public void keyPressed ( KeyEvent event ) {
	int code = event.getKeyCode();
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
public void keyReleased ( KeyEvent event ) {
}

public void keyTyped ( KeyEvent event ) {
}

/**
Look up the color to use for a time series.
@param index Zero-referenced index in time series list.
@return the color for a time series, as a string color name.
*/
public static String lookupTSColor(int index) {
	// the following is done so that for Point graphs, time series X and X + 10 don't have the same symbol style AND color.
	// This makes it so that their symbol styles will be the same,
	// but there won't be any time series with the same symbol and color until over 100 time series are in the graph.
	if (index > 9) {
		index += (index / 10);
	}

	int remainder = index % 10;

	if ( index == 0 ) {
		// First color red.
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
Look up the symbol to use for a time series.  This is only used for point graphs.
@param index Zero-referenced index in time series list.
@return the symbol for a time series, as a string symbol name.
*/
public static String lookupTSSymbol(int index) {
	int remainder = index % 10;

	if ( index == 0 ) {
		// First color red.
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
public void mouseClicked ( MouseEvent event ) {
	if ( getInteractionMode() != TSGraphInteractionType.EDIT ) {
		// Not editing, return.
		return;
	}
	else {
		// Editing the time series.
		TSGraph tsgraph = getEventTSGraph ( new GRPoint(event.getX(), event.getY()));
		if ( tsgraph == null ) {
			// Not in a graph.
			return;
		}
		editPoint(event, tsgraph);
		refresh(false);
	}
}

/**
Handle mouse drag event.  If in zoom mode, redraw the rubber-band line.
If a mouse tracker is enabled, call the TSViewListener.mouseMotion() method.
@param event Mouse drag event.
*/
public void mouseDragged ( MouseEvent event ) {
	//event.consume();
	if ( (_interaction_mode != TSGraphInteractionType.SELECT) && (_interaction_mode != TSGraphInteractionType.ZOOM) ) {
		return;
	}

	int mods = event.getModifiers();
	if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		// Don't want rubber band box.
		return;
	}

	// Figure out which graph the event occurred in.

	TSGraph tsgraph = getEventTSGraph (	new GRPoint(event.getX(),event.getY() ) );
	if ( tsgraph == null ) {
		// User is dragging outside a graph.
		// If they started outside the graph, nothing would have been.
		return;
	}

	// Else dragging in a valid graph.
	// If they started in outside and are now crossing into a graph, initialize the coordinates similar to mousePressed().

	if ( _mouse_x1 == -1 ) {
		// Let the other method do the work.
		mousePressed ( event );
		// Don't need to do anything else.
		return;
	}

	// Get the coordinates used.

	_mouse_x2 = event.getX();
	_mouse_y2 = event.getY();

	// Figure out which drawing area the event occurred in.

	TSGraphType graphType = tsgraph.getLeftYAxisGraphType();
	if ((graphType == TSGraphType.DURATION) || (graphType == TSGraphType.XY_SCATTER)) {
		// Don't allow zoom.
		_rubber_banding = false;
		return;
	}
	_rubber_banding = true;
	// Let listeners know so the tracker can be updated to help size the rubber band box.
	// Data units.
	GRPoint datapt = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY ( _mouse_x2, _mouse_y2, GRCoordinateType.DEVICE );
	if ( datapt == null ) {
		// Generally only happens when the graph cannot be displayed.
		return;
	}
	boolean dotrack = true;
	if ( dotrack ) {
		// Device units.
		GRPoint devpt = new GRPoint ( _mouse_x2, _mouse_y2 );
	    if ( tsgraph.getLeftYAxisGraphType() == TSGraphType.RASTER ) {
	        // Also associate the time series for the graph, so value can be shown.
	    	// Tracking is initially tied to the left y-axis.
	    	boolean includeLeftYAxis = true;
	    	boolean includeRightYAxis = false;
	        List<TS> tslist = tsgraph.getEnabledTSList(includeLeftYAxis,includeRightYAxis);
	        if ( tslist.size() > 0 ) {
	            datapt.associated_object = tslist.get(0);
	            devpt.associated_object = tslist.get(0);
	        }
	    }
		int size = _listeners.length;
		for ( int i = 0; i < size; i++ ) {
			_listeners[i].tsViewMouseMotion(tsgraph, devpt, datapt);
		}
	}
	// Force a redraw. The _rubber_banding flag will be checked so a full redraw is not done.
	repaint ();
}

/**
Handle mouse enter event.  Currently does not do anything.
@param event MouseEvent to handle.
*/
public void mouseEntered ( MouseEvent event ) {
}

/**
Handle mouse exit event.  Currently does not do anything.
@param event MouseEvent to handle.
*/
public void mouseExited ( MouseEvent event ) {
}

/**
Handle mouse motion event.
If a mouse tracker is enabled, call the TSViewListener.mouseMotion() method.
@param event MouseEvent to handle.
*/
public void mouseMoved ( MouseEvent event ) {
	if ( _listeners == null ) {
		return;
	}

	// Get the mouse position.

	int x = event.getX();
	int y = event.getY();

	TSGraph tsgraph = getEventTSGraph ( new GRPoint ( x, y ) );
	if ( tsgraph == null ) {
		// Mouse is not in a graph area so don't track.
		return;
	}

	// Update cross-hair cursor.
	if ( this.useCursorDecorator ) {
		if ( getInteractionMode() == TSGraphInteractionType.EDIT ) {
	    	_cursorDecorator.mouseMoved(event,tsgraph.getLeftYAxisGraphDrawingArea().getPlotLimits( GRCoordinateType.DEVICE));
	    	//  refresh(false);
		}
	}

	// Get coordinates in data units.

	GRPoint datapt = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY( x, y, GRCoordinateType.DEVICE );

	GRPoint devpt = new GRPoint ( x, y );
	if ( tsgraph.getLeftYAxisGraphType() == TSGraphType.RASTER ) {
	    // Also associate the time series for the graph, so value can be shown.
		// Only left y-axis data are uses with raster.
    	boolean includeLeftYAxis = true;
    	boolean includeRightYAxis = false;
	    List<TS> tslist = tsgraph.getEnabledTSList(includeLeftYAxis,includeRightYAxis);
	    if ( tslist.size() == 1 ) {
	    	// Single time series in the raster graph use the first time series.
	        datapt.associated_object = tslist.get(0);
	        devpt.associated_object = tslist.get(0);
	    }
	    else {
	    	// Multiple time series so must look up the time series from the Y-position:
	    	// - y is the time series position, should be >= 0 and <= tslist.size(),
	    	//   where the maximum is used allow drawing a pixel block but is not allowed for the time series position
	    	// - y is zero index so just round
	    	int yPos = (int)datapt.y;
	    	if ( yPos < 0 ) {
	    		// Right on the edge so use zero index.
	    		yPos = 0;
	    	}
	    	else if ( yPos >= tslist.size() ) {
	    		// Right on the edge so decrement by one.
	    		--yPos;
	    	}
	        datapt.associated_object = tslist.get(yPos);
	        devpt.associated_object = tslist.get(yPos);
	    }
	}

	// Call the listeners.

	for ( int ilist = 0; ilist< _listeners.length; ilist++ ) {
		_listeners[ilist].tsViewMouseMotion ( tsgraph, devpt, datapt );
	}
}

/**
Handle mouse pressed event.
First check whether clicked in a legend hotspot and if so, select the time series to highlight and redraw.
Otherwise, start a select or zoom.  The event is completed when the mouse is released.
@param event MouseEvent to handle.
*/
public void mousePressed ( MouseEvent event ) {
	//event.consume();
	requestFocus();
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, _gtype + "TSGraphJComponent.mousePressed",
			"Mouse pressed at device coordinates " + event.getX() + "," + event.getY() );
	}
	// Initialize the coordinates.  If the click is outside any graph,
	// these values will signal to mouseDragged() that a valid starting point for a box has not been given.
	_mouse_tsgraph1 = null;
	_mouse_x1 = _mouse_y1 =_mouse_x2=_mouse_y2=_mouse_xprev=_mouse_yprev=-1;
	// Figure out which TSGraph the event occurred in.
	GRPoint eventPoint = new GRPoint ( event.getX(), event.getY() );
	// First get the TSGraph considering whether in the graph area and total page.
	TSGraph tsgraphForGraph = getEventTSGraph ( eventPoint, true, false );
	TSGraph tsgraphForPage = getEventTSGraph ( eventPoint, true, true );
	boolean forceRepaint = false; // In cases where need to redraw, for example, when time series is selected in legend.
	if ( tsgraphForPage != null ) {
		// Click was somewhere in a page.
		// Check to see if a legend hot-spot was clicked on.
		TS eventTS = tsgraphForPage.getEventLegendTimeSeries(eventPoint);
		if ( eventTS != null ) {
			if ( Message.isDebugOn ) {
				Message.printStatus(2,"","Mouse click - found legend time series " + eventTS.getIdentifierString());
			}
			// Indicate that the time series is selected for the graph, so special treatment occurs during rendering.
			// The TSGraph instance should be the same whether graph or page because it contains both.
			boolean isSelected = tsgraphForPage.toggleTimeSeriesSelection ( eventTS );
			if ( Message.isDebugOn ) {
				Message.printStatus(2,"","Mouse click - legend time series selected=" + isSelected + " " + eventTS.getIdentifierString());
			}
			// Indicate to force a repaint so (un)selected time series will draw with proper style.
			forceRepaint = true;
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printStatus(2,"","Mouse click - did not find legend time series");
			}
		}
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printStatus(2, "", "Click was not on any page drawing area" );
		}
	}
	if ( tsgraphForGraph != null ) {
		// Click was in a graph drawing area.
		_mouse_tsgraph1 = tsgraphForGraph;
		int mods = event.getModifiers();
		if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
			// Each graph provides its own right-click popup menu to edit properties and view analysis details.
			JPopupMenu popup_menu = tsgraphForGraph.getJPopupMenu();
			if ( popup_menu != null ) {
				// Add to the component.  It will be removed when the menu event is processed.
				add ( popup_menu );
				// Show the popup menu.  It is modal.
				popup_menu.show ( event.getComponent(), event.getX(), event.getY() );
				// Now try removing from the component.
				// Doing this seems to disable the menu action.
				// See notes in TSGraph related to the the popup menu.
				//remove ( popup_menu );
			}
		}
		else if ( (_interaction_mode == TSGraphInteractionType.SELECT) ||
			(_interaction_mode == TSGraphInteractionType.ZOOM) ) {
			// Save the point that was selected so that the drag and released events will work.
			// Also save the initial graph so that we can make sure not to drag outside a valid graph.
			_mouse_x1 = event.getX();
			_mouse_y1 = event.getY();
		}
	}
	if ( forceRepaint ) {
		// Cause a redraw, due to time series being (un)selected by legend click.
		setForceRedraw(true,true);
		repaint();
	}
}

/**
Handle mouse released event.
If in INTERACTION_ZOOM mode, call the tsViewZoom() method of registered TSViewListeners.
If in INTERACTION_SELECT mode, call tsViewSelect().
Only return a region if the mouse has moved at least 5 pixels in one direction.
*/
public void mouseReleased ( MouseEvent event ) {
	//event.consume();

	// Figure out which graph the event occurred in.

	TSGraph tsgraph = getEventTSGraph( new GRPoint ( event.getX(), event.getY() ) );
	int x = 0;
	int y = 0;
	if ( tsgraph == null ) {
		// The mouse was released outside a graph valid area.
		// If a valid previous point is available, use that to close the box.
		if ( _mouse_tsgraph1 == null ) {
			// Zoom box never was initialized.
			return;
		}
		tsgraph = _mouse_tsgraph1;
		x = _mouse_xprev;
		y = _mouse_yprev;
	}
	else {
	    // use the valid point within the graph.
		x = event.getX();
		y = event.getY();
	}
	TSGraphType graphType = TSGraphType.UNKNOWN;
	if ( tsgraph != null ) {
		graphType = tsgraph.getLeftYAxisGraphType();
	}
	if ((graphType == TSGraphType.XY_SCATTER) || (graphType == TSGraphType.DURATION) ) {
		// Currently do not allow zoom, etc.
		return;
	}
	int mods = event.getModifiers();
	if ( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		// Right click so don't do anything.
		return;
	}
	if ( (_interaction_mode == TSGraphInteractionType.SELECT) || (_interaction_mode == TSGraphInteractionType.ZOOM) ) {
		// Only process if box is "delta" pixels or bigger.
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
			if ( _interaction_mode == TSGraphInteractionType.SELECT ) {
				// Assume they want the original point.
				GRPoint devpt = new GRPoint ( (double)_mouse_x1, (double)_mouse_y1 );
				GRPoint datapt = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY(_mouse_x1, _mouse_y1, GRCoordinateType.DEVICE );
				if ( _listeners != null ) {
					int size = _listeners.length;
					// Need to figure out which time series was selected.
					for ( int i = 0; i < size; i++ ) {
						_listeners[i].tsViewSelect ( tsgraph, devpt, datapt, (List<Object>)null );
					}
				}
				_rubber_banding = false;
				// Reset zoom coordinates.
				_mouse_x2 = _mouse_xprev = -1;
				if ( (x != _mouse_x1) && (y != _mouse_y1) ) {
					repaint();
				}
				// Don't need to do anything else.
				return;
			}
			else if ( _interaction_mode == TSGraphInteractionType.ZOOM ) {
				// Too small, don't allow.
				// Reset zoom coordinates and force a redraw to clear the box.
				_mouse_x2 = _mouse_xprev = -1;
				if ( (x != _mouse_x1) && (y != _mouse_y1) ) {
					// There was some motion so need to redraw to clear the box.
					refresh();
					_rubber_banding = false;
					//repaint();
				}
				// Don't need to do anything else.
				return;
			}
		}
		// Save the point that was selected so that the drag and released events will work.
		// Reset the data limits to those from the zoom box.
		// Make sure that the limits are always specified.
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

		GRLimits mouseLimits = new GRLimits ( xmin, ymin, xmax, ymax );
		// Reverse Y so we get the right values in GR.
		GRPoint pt1 = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY ( xmin, ymax, GRCoordinateType.DEVICE );
		GRPoint pt2 = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY ( xmax, ymin, GRCoordinateType.DEVICE );

		GRLimits newDataLimits = new GRLimits ( pt1, pt2 );

		if ( _interaction_mode == TSGraphInteractionType.ZOOM ) {
			// Reset the limits to more appropriate values.
			if ( _zoom_keep_y_limits ) {
				// Set the Y limits to the maximum values.
				newDataLimits.setTopY ( tsgraph.getMaxDataLimits().getTopY() );
				newDataLimits.setBottomY( tsgraph.getMaxDataLimits().getBottomY() );
			}
		}

		tsgraph.setDataLimitsForDrawing ( newDataLimits );
/* TODO sam 2017-04-23 old comment - need to evaluate
		// Adjust the limits slightly if monthly or annual data since the data values are plotted in the middle of the interval.
		if ( _interval_max >= TimeInterval.MONTH ) {
			// For monthly time series, data are plotted on the middle of the month so adjust back 15/365.
			_data_limits.setLeftX ( _data_limits.getLeftX()- .0411);
		}
		if ( _interval_max >= TimeInterval.YEAR ) {
			// For annual time series, data are plotted on June 30 so adjust back 1/2 year.
			_data_limits.setLeftX ( _data_limits.getLeftX() - .5);
		}
*/
		// Call the listener (or should this happen after the paint?).
		if ( _interaction_mode == TSGraphInteractionType.SELECT ) {
			// Just return the select information
			if ( _listeners != null ) {
				int size = _listeners.length;
				for ( int i = 0; i < size; i++ ) {
					_listeners[i].tsViewSelect ( tsgraph, mouseLimits, newDataLimits, (List<Object>)null );
				}
			}
		}
		else if ( _interaction_mode == TSGraphInteractionType.ZOOM ) {
			// Actually reset the data limits.
			// Only set new drawing area data limits for the main graph.
			if ( !_is_reference_graph ) {
				tsgraph.setDataLimitsForDrawing ( newDataLimits );
				//_grda.setDataLimits ( _data_limits );
			}
			// Call external listeners to let them know that a graph has been zoomed.
			if ( _listeners != null ) {
				int size = _listeners.length;
				for ( int i = 0; i < size; i++ ) {
					_listeners[i].tsViewZoom ( tsgraph, mouseLimits, newDataLimits );
				}
			}
			// Apply the zoom to other graphs that need to be zoomed.
			zoom ( tsgraph, mouseLimits, newDataLimits );
			// Repaint.
			// Fill in the background color.  Need this because update() does not do (because of zooming).
			clearView();
			// Before redrawing, set the data limits to the plotting limits that result in the full device being used.
			// Otherwise, mouse tracking, etc. may not allow selects from outside the actual data limits.
			// First set so the plotting limits will be recomputed.
			_force_redraw = true;
		}
		_rubber_banding = false;
		// Reset zoom coordinates.
		_mouse_x2 = _mouse_xprev = -1;
		repaint();
	}
}

/**
Indicate whether during initialization the user has indicated that the graph should not continue and should be closed.
This should be called immediately after construction in higher-level code.
@return true if the TSGraphJComponent (and the graph) should be closed due to a user confirmation.
*/
public boolean needToClose() {
	return _need_to_close;
}

/**
Set whether during initialization the user has indicated that the graph should not continue and should be closed.
This should be called immediately when a problem occurs in TSGraph constructors and should be checked when constructing this class.
@param need_to_close True if the TSGraphJComponent should be closed because of user verification not to go on.
@return true if the TSGraphJComponent (and the graph) should be closed due to a user confirmation.
*/
public boolean needToClose ( boolean need_to_close ) {
	_need_to_close = need_to_close;
	return _need_to_close;
}

/**
Open the drawing areas and set the data limits (all are unit limits).
These drawing areas are used for page drawing, such as main component.
TSGraph drawing areas are overlaid within the overall component.
*/
private void openDrawingAreas () {
	// Full page.

	_da_page = new GRJComponentDrawingArea ( this, "TSGraphJComponent.Page",
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_page = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_page.setDataLimits ( _datalim_page );

	// Drawing area for main title (data are just unit).

	_da_maintitle = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.MainTitle", GRAspectType.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_maintitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_maintitle.setDataLimits ( _datalim_maintitle );

	// Drawing area for sub title (data are just unit).

	_da_subtitle = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.SubTitle", GRAspectType.FILL,
			null, GRUnits.DEVICE, GRLimits.DEVICE, null );
	_datalim_subtitle = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_subtitle.setDataLimits ( _datalim_subtitle );

	// Drawing area for graphing.

	_da_graphs = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.Graphs",
			GRAspectType.FILL, null, GRUnits.DEVICE,
			GRLimits.DEVICE, null );
	_datalim_graphs = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_graphs.setDataLimits ( _datalim_graphs );

	// Drawing area for left footer (data are just unit).

	_da_leftfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.LeftFooter",
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_leftfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_leftfoot.setDataLimits ( _datalim_leftfoot );

	// Drawing area for center footer (data are just unit).

	_da_centerfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.CenterFooter",
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_centerfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_centerfoot.setDataLimits ( _datalim_centerfoot );

	// Drawing area for right footer (data are just unit).

	_da_rightfoot = new GRJComponentDrawingArea ( this,
			"TSGraphJComponent.RightFooter",
			GRAspectType.FILL, null, GRUnits.DEVICE, GRLimits.DEVICE,
			null );
	_datalim_rightfoot = new GRLimits ( 0.0, 0.0, 1.0, 1.0 );
	_da_rightfoot.setDataLimits ( _datalim_rightfoot );
}

/**
Update the TSGraphJComponent visible image, render to printer (set _printing=true before calling),
or render to SVG file (set __printForSVG=true before calling).
@param g Graphics instance either from the component event handling or from an explicit printView() call.
*/
public void paint ( Graphics g ) {
	String routine = getClass().getSimpleName() + ".paint";
	int dl = 10;
	boolean resizing = false;

	_double_buffering = true;

	if ( g == null ) {
		Message.printDebug( 1, routine, "Null Graphics in paint()." );
		return;
	}

	// Print some messages so we know what the paint is doing.

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

	try { // Main try.

	// The following will be executed at initialization.
	// The code is here because a valid Graphics is needed to check font sizes, etc.

	if ( _da_page == null ) {
		//
		// This will be executed the first paint.
		//
		// The graphics will be for the screen
		// (it may actually be set for an image below but setting it here should be OK for initialization).
		// Once the graphics is set, font sizes can be determined.
		// Cast to Graphiucs2D, which provides more functionality.
		_graphics = (Graphics2D)g;
		// Now set the drawing limits these are necessary to compute the labels and are for the most part independent of data limits.
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

	// If rubber-banding, can do before anything else and return.
	boolean drawingTracker = true;
	if ( _rubber_banding ) {
		if (_double_buffering && _buffer != null) {
			g.drawImage(_buffer, 0, 0, this);
		}

		g.setColor ( _rubber_band_color );
		g.setXORMode ( _background_color );
		int xmin, xmax, ymin, ymax;
		// Now draw the new rectangle (still in XOR mode).
		// If _mouse_x2 = -1, the code is getting called from mouseReleased() and only the previous rectangle needs to be cleared.
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
			// Save the previous coordinates.
			_mouse_xprev = _mouse_x2;
			_mouse_yprev = _mouse_y2;
		}
		// Done drawing.  Reset paint mode to normal just in case the code is changed later to not return.
		g.setPaintMode ();
		return;
	}

	// See if the graphics is for printing or screen and make a few adjustments accordingly.
	// Set the base class _printing flag if necessary...

	GRLimits new_draw_limits = null;
	if ( _printing ) {
		// Set in the printView method.
		// Bounds will have been set in printView.
		new_draw_limits = new GRLimits();
		// Gets the page size.
		if ( Message.isDebugOn ) {
		    Message.printDebug(1, "", "PB: " + _printBounds);
		}

		// Get the drawing limits from the bounds set by the print job.
		// These limits are the valid extents on the paper to which the print job can print.
		// These take into account the margins the user set up around the page.
		// There is no need to worry at this point about landscape versus portrait.
		// When the Print Job is told that the paper will be Landscape,
		// it automatically knows formats the output for a landscape paper.
		// All developers need to know is that the page is longer than it is wide.
		// For RTi's purposes the GR pixel (0, 0) is still in the lower-left corner.
		new_draw_limits.setLeftX(_printBounds.getLeftX());
		new_draw_limits.setTopY(_printBounds.getTopY());
		new_draw_limits.setRightX( _printBounds.getRightX());
		new_draw_limits.setBottomY(_printBounds.getBottomY());
		if ( Message.isDebugOn ) {
		    Message.printDebug(1, "", "NL: " + new_draw_limits);
		}

		// Set the limits for drawing to the printed page:
		// - the old limits will be reset after the print() call is done
		setLimits(new_draw_limits);

		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Using print graphics." );
		}
		_graphics = (Graphics2D)g;
		// Base class.
	} // Done _printing.
	else {
	    // Drawing to screen or SVG file.
	    // Gets the component size.
		// GR handles this.
		new_draw_limits = getLimits(true);
		// Need the full device limits elsewhere.
		_bounds = getBounds ();
		// Now set the drawing limits to the bounds.
		new_draw_limits.setLeftX ( 0.0 );
		new_draw_limits.setBottomY ( 0.0 );
		new_draw_limits.setRightX ( new_draw_limits.getRightX() );
		new_draw_limits.setTopY ( new_draw_limits.getTopY() );
		// Need the full device limits elsewhere.
		if ( Message.isDebugOn ) {
		    if ( __paintForSVG ) {
		        Message.printDebug ( 1, routine, _gtype + "Using SVG graphics." );
		    }
		    else {
		        Message.printDebug ( 1, routine, _gtype + "Using display graphics." );
		    }
		}
		if ( !_double_buffering || __paintForSVG ) {
			// Use this graphics.
			_graphics = (Graphics2D)g;
		}
		// Base class.
	}

	// See if the drawing limits need to be reset for drawing.
	// If the size has changed and double-buffering, create a new image for the off-screen buffer.

	if ( _first_paint || !_drawlim_page.equals(new_draw_limits) ) {
		// Set to the new drawing limits for the redraw.
		// This will cause a recompute of the data used for scaling.
		//setDrawingLimits ();
		resizing = true;
		// If double buffering, create a new image.
		if ( ((_buffer == null) || _double_buffering) && !_printing && !__paintForSVG) {
			// This needs to be the size of the full component, NOT the drawing limits.
			// Clean up since images can take a lot of memory.
			_buffer = null;
			// TODO SAM 2013-01-28 Delete following line after sufficient time has passed.
			//System.gc();
			if ( _external_Image != null ) {
				// Image was created external to this class.
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Using external Image from properties." );
				}
				System.out.flush();
				_buffer = _external_Image;
			}
			else {
			    // Image is maintained in this class.
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Creating new image because of resize." );
				}
				// Base class method.
				setupDoubleBuffer(0, 0, _bounds.width, _bounds.height);
			}
		}
	}
	else {
	    // component size has not changed.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, _gtype + "Device size has not changed." );
		}
		// All that needs to be done is to copy the image to the current screen.
		// However, if this is the first time, need to execute the following draw code at least once.
		resizing = false;
	}
	if ( _double_buffering && !_printing && !__paintForSVG) {
		// Use the image graphics.
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Using image graphics." );
		}
		_graphics = (Graphics2D)(_buffer.getGraphics());
	}

	// Do the following the first time so that the drawing can use the graphics for font-based sizing.

	boolean didClearView = false;
	if ( _first_paint || resizing ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, _gtype + "Device size has changed." );
		}
		// Resize the drawing area by setting its drawing limits.
		setDrawingLimits ();
		setGraphDrawingLimits ();
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, _gtype + routine, "Setting graph drawing limits to: " + _da_graphs );
		}
		// Now clear the view so that drawing occurs on a clean background.
		clearView ();
		didClearView = true;
	}
	if ( _force_redraw_clean && !didClearView ) {
		// Another case where a clear background is needed.
		clearView ();
	}

	// Redraw the graph(s) if any of the following conditions apply:
	//
	// * not double buffering (redraw every time)
	// * a draw is forced (because of changes in the views or time series are (un)selected by clicking on legend)
	// * printing has been requested (and need to redraw given page extents, etc.)
	// * double-buffering is on and a resize has occurred.

	if ( _force_redraw || _force_redraw_clean || _printing || __paintForSVG || !_double_buffering || resizing ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Drawing graph (_force_redraw=" +
			_force_redraw + " _force_redraw_clean=" + _force_redraw_clean + " resizing=" + resizing + ")..." );
		}
		try {
		    JGUIUtil.setWaitCursor(_parent, true );
			// If the first time, force a zoom out to synchronize the data limits on all the graphs that
		    // are at the same zoom level (tried this above but seems to work when called from here).
			if ( _first_paint ) {
                if ( Message.isDebugOn ) {
                    Message.printDebug ( 1, routine, _gtype + "Zooming out the first time to sync graphs." );
                }
                // The following causes a call to paint so then _first_paint is false and the round trip back to here does not occur?
                zoomOut ( false );
                if ( Message.isDebugOn ) {
                    Message.printDebug ( 1, routine, _gtype + "After call to zoomOut() __visibleStart=" +
                        __visibleStart + " __visibleEnd=" + __visibleEnd );
                }
			    if ( (__visibleStart != null) && (__visibleEnd != null) ) {
			        if ( Message.isDebugOn ) {
			            Message.printDebug ( 1, routine, _gtype + "Zooming to initial visible extent." );
			        }
			        zoomToVisiblePeriod(__visibleStart, __visibleEnd, false );
			    }
			}
			// Draw the titles, footers, etc.
			drawTitles ();
			drawFooters ();
			if ( IOUtil.testing() ) {
				// Comment out unless we are reworking something drawDrawingAreas();
			}
			// Now loop through and draw each graph on the page.
			// Only graph enabled subproducts and if a reference graph only graph the corresponding subproduct.
			int size = _tsgraphs.size();
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, _gtype + "Have " + size + " time series to graph.");
			}
			TSGraph tsgraph;
			String prop_val;
			for ( int isub = 0; isub < size; isub++ ) {
				// If the graph is disabled, do not even draw.
				prop_val = _tsproduct.getLayeredPropValue ( "Enabled", isub, -1, false );
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, routine, _gtype + "Graph["+ isub + "].Enabled is " + prop_val );
				}
				if ( prop_val.equalsIgnoreCase("false") ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine, _gtype + "Graph is not enabled so skipping." );
					}
					continue;
				}
				if ( _is_reference_graph &&	(isub != _reference_sub) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine, _gtype + "Graph[" + isub + "] is not the same as reference graph (" + _reference_sub + ") - skipping." );
					}
					continue;
				}
				tsgraph = _tsgraphs.get(isub);
				if ( tsgraph == null ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, routine, _gtype + "TSGraph is null - skipping." );
					}
					continue;
				}
				// Debug whether graphs are enabled.
				//Message.printStatus ( 1, "", _gtype + "Graph ["+ isub + "] is " +
				// _tsproduct.getLayeredPropValue("MainTitleString", isub, -1, false) );
				tsgraph.paint ( _graphics );
				if (_is_reference_graph) {
					continue;
				}
			}
		}
		catch ( Exception e ) {
			// FIXME SAM 2010-11-29 Changing to level 1 and drawing the dialog causes a TSTool crash in some cases.
			Message.printWarning ( 2, routine, "Error drawing graph(s) (" + e + ")." );
			Message.printWarning ( 3, routine, e );
		}
		finally {
			JGUIUtil.setWaitCursor(_parent, false );
		}
		_force_redraw = false;
		_force_redraw_clean = false;
	}

	if ( drawingTracker ) {
		// TODO SAM 2016-10-17 Enable tracking similar to zoom but on top.
		// Done drawing.  Reset paint mode to normal just in case we change code later to not return.
		//g.setPaintMode ();
	}

	// Finally, if double buffering and not printing, copy the image from the buffer to the component.

	if ( _double_buffering && !_printing && !__paintForSVG ) {
		// The graphics is for the display.
		// Draw to the screen.
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, _gtype + "Copying internal image to display." );
		}
		g.drawImage ( _buffer, 0, 0, this );
		// Use the following to troubleshoot.
		//saveAsFile(System.getProperty("java.io.tmpdir") + File.separator + "junk.png");
		// Only do this if double buffering to screen because that is the only time the graphics is created locally.
		// ?? _graphics.dispose();
	}

	} // Main try wrapped around entire method.
	catch ( Exception e ) {
		Message.printWarning ( 3, _gtype + routine, e );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, _gtype + "...done painting TSGraphJComponent." );
	}
	// Should always get to here the first time.
	_first_paint = false;
	if (_printing) {
		_printing = false;
	}
	if (__paintForSVG) {
	    __paintForSVG = false;
	}
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
		// print() is called for every page to be printed.
		// The graph will only have a 0th page (the 1 page to be printed),
		// so when this method is called for any page above one,
		// return the standard NO_SUCH_PAGE value (inherited from Printable).
		return NO_SUCH_PAGE;
	}
	else {
		// The following is a debug utility that prints information
		// about the pageformat (its imageable area, etc) on status level 1.
//		PrintUtil.dumpPageFormat(pageFormat);

		// Get the printing bounds of the image.
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

	[IX] - is the number of pixels from the side of the paper to the very first part of the paper upon which printing can occur.
		It is called the "Imageable X".
		Every pixel to the left of Imageable X is in the left-side margin.

	[IY] - is the number of pixels from the top of the paper to the very first part of the paper upon which printing can occur.
		It is called the "Imageable Y".
		Every pixel above Imageable Y is in the top margin.

	[IW] - is the number of horizontal pixels that can actually be printed.
		It is the "Imageable Width" and is the number of pixels between the left and right margins.

	[IH] - is the number of vertical pixels that can actually be printed.
		It is the "Imageable Height" and is the number of pixels between the top and bottom margins.

	[W] - is the total paper width in pixels.
		[W] - ([IW] + [IX]) == the size of the right-side margin

	[H] - is the total paper height in pixels.
		[H] - ([IH] + [IY]) == the size of the bottom margin
	*/

	/*
	GRLimits parameters are in the order:
	Bottom-left X, Bottom-left Y, Top-right X, Top-right y

	So the printable limits above (ImageableX, etc, which go from 0 at the
	top-left instead of bottom-left) need to be translated to limits in the GR mode.
	The only ones that will actually change are the Y values, as X still goes from 0 at the left to MAX at the right.

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

			if ( Message.isDebugOn ) {
				Message.printDebug(1, "", "PrintBounds after print selection:\n" + _printBounds);
			}

		// Set the printing flag to true.
		_printing = true;

/*
	The following is necessary because of some errors that can arise when
	printing with GR-style coordinates in the Java-style coordinate system.

	In order to explain, imagine a theoretical piece of paper that is 5 inches across by 4 inches down,
	into which will be printed a graph that is 3 inches across by 2 inches down:

	/--+--+--+--+--+
	| (margins)    |
	+  +--+--+--+  +
	|  |        |  |
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|              |
	\--+--+--+--+--+

	Java always numbers it coordinates from the upper-left down (from the '/').
	GR coordinates go from the bottom-left up (from the '\').

	When a pageformat is used in the print() method, it sets up the boundaries for GR coordinates,
	so that the extent of the left and bottom margins are known.
	Margins are enforced by the internal Java printing,
	so that whatever the imageable area of the page format is set to will be the only parts of the page to which something can be drawn.
	A drawing area is sized to accommodate the margins, and the graphic is drawn within this, like so:

	...+--+--+--+
	.  |        |
	.  + (graph)+
	.  |        |
	.  +--+--+--+
	.(margins)  .
	.............

	The problem is that the highest GR pixel is equal to the 0th Java pixel,
	so that when the above graphic is drawn to a piece of paper, it will be drawn like this:

	/--+--+--+--+--+
	|  |        |  |
	+  + (graph)+  +
	|  |        |  |
	+  +--+--+--+  +
	|           .  |
	+............  |
	|              |
	\--+--+--+--+--+

	Something needs to be done to take into account the upper margin.
	The extent of the graphic cannot simply be increased,
	because then the graph will expand upward to fill the available space.

	There are two options.

	1) Create a blank drawing area only during printing and use it as a spacer above the graph:

	/--+--+--+--+--+
	|(draw area)|  |     Where (draw area) is a drawing area that is high
	+--+--+--+--+  +     enough (width doesn't matter in this case) to
	|  |        |  |     put enough space so that the graph is not drawn
	+  + (graph)+  +     over the margins of the printed page.
	|  |        |  |
	+  +--+--+--+  +
	|              |
	\--+--+--+--+--+

	2) Use Java2D's Graphics2D class and translate all of the drawing down to not draw over the top margin.

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

	Of the two, translation is much easier,
	requiring only three lines of code and no special code within the paint() method to handle special circumstances in printing.
*/

		Graphics2D g2d = (Graphics2D)g;

		// The imageable Y is the first Java pixel (going DOWN) at which drawing can occur.
		// It is therefore the LAST GR pixel at which drawing can occur.
		// The bottom margin of the graph is already enforced by setting the size of the drawing area.
		// This makes sure the graph is shifted to fit in the printable area of the page.
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
Print the graph.  The user is prompted to select a printer, etc.
*/
public void printGraph() {
    String routine = getClass().getName() + ".printGraph";
	try {
	    // Keep around the old code for a bit but the new dialog is much more straightforward.
	    boolean useOldCode = false;
	    if ( useOldCode ) {
    		// For all the below, additionally check the docs for PrintUtil.java.

    		// Create a page format object for printing to letter paper.
    		PageFormat pageFormat = PrintUtil.getPageFormat("letter");

    		// Sets the orientation for a page format.
    		PrintUtil.setPageFormatOrientation(pageFormat, PageFormat.LANDSCAPE);

    		// Set the margins for the page format and pop up a dialog box in which the user can change the margins.
    		PrintUtil.setPageFormatMargins(pageFormat, .5, .5, .5, .5);

    		// Print the job by popping up a dialog from which users can select the printer on which to print and other information.
    		PrintUtil.print(this, pageFormat);
	    }
	    else {
            new GraphicsPrinterJob ( this,
                "Graph",
                null, // Printer name.
                "na-letter", // Paper size.
                null, // Paper source.
                "Landscape", // Page orientation.
                .5, // Left margin.
                .5, // Right.
                .5, // Top.
                .5, // Bottom.
                null, // krint file.
                true ); // Show print configuration dialog.
        }
   	}
    catch ( Exception e ) {
        Message.printWarning ( 1, routine, "Error printing graph (" + e + ").");
        Message.printWarning ( 3, routine, e );
    }
}

/**
Refresh the plot based on changes in properties.
This is typically called from outside code, e.g., when time series have been enabled/disabled in the TSViewPropertiesGUI.
The drawing areas ARE recomputed.
*/
public void refresh () {
	refresh ( true );
}

/**
Refresh the plot based on changes in properties.
This is typically called from outside code, e.g., when time series have been enabled/disabled in the TSViewPropertiesGUI.
@param recompute_drawing_limits If true, the drawing areas will be recomputed.
This is only necessary when the component size changes or axes are turned on/off.
If zooming, the drawing areas can remain the same size
(this assumes that a reasonable estimate of the maximum data value has been used to set the y-axis label widths).
*/
public void refresh ( boolean recompute_drawing_limits ) {
	clearView ();
	// This will recompute drawing limits for the main component and the TSGraphs.
	if ( recompute_drawing_limits ) {
		setDrawingLimits ();
		setGraphDrawingLimits ();
	}
	_force_redraw = true;
	repaint ();
}

/**
Sets up the list of TSGraph objects again based on the provided TSProduct.
This ensures that the graphs match exactly the properties in the TSProduct that is being manipulated in the JFrame.
Since these properties can be changed in the JFrame, in the layout component, and possibly in other places,
it's safest to simply re-read the properties and rebuild all the graphs completely when major changes occur.
In particular, if time series are moved between graphs, new graphs are added the internal numbering changes,
or axis labels and legend properties are changed.
@param product the TSProduct from which to read TSGraph information.
This will most likely be the product that's already resident in memory, but not necessarily.
*/
public void reinitializeGraphs(TSProduct product) {
    //String routine = getClass().getSimpleName() + ".reinitializeGraphs";
	// If any graphs lack start and end dates (i.e., they're brand new and lack any time series),
	// pull out a start and end date from any of the other graphs and use it, so that zoom outs work correctly.
	DateTime end = null;
	DateTime maxEnd = null;
	DateTime start = null;
	DateTime maxStart = null;
	DateTime temp = null;

	// Find the latest end date and the earliest start date from the graphs.

	for ( TSGraph g: _tsgraphs ) {
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

	// Find the latest end date and the earliest start date from all the time series.

	for ( TS ts : _tslist ) {
		temp = ts.getDate1();
		if ( (maxStart == null) || (temp != null && temp.lessThan(maxStart))) {
			// Reset the earliest start if it has not been set or time series has earlier period start.
			maxStart = temp;
		}

		temp = ts.getDate2();
		if ( (maxEnd == null) || (temp != null && maxEnd.lessThan(temp))) {
			// Reset the latest start if it has not been set or time series has later period end.
			maxEnd = temp;
		}
	}

	List<TSGraphDataLimits> v = determineDataLimits();

	// Some internal data in the previous list of TSGraph was created through user
	// action and is not stored as product properties and must be transferred to the new list of TSGraph.
	// TODO sam 2017-02-2017 need to confirm that the new list of TSGraph always align with the old?
	// - It should? as long as the interactive add of TSGraph was handled gracefully prior to this point.
	List<List<TS>> selectedTimeSeriesListOld = new ArrayList<>();
	List<TSGraph> tsgraphListOld = _tsgraphs;
	for ( TSGraph tsgraph : tsgraphListOld ) {
		selectedTimeSeriesListOld.add(tsgraph.getSelectedTimeSeriesList());
	}

	_tsgraphs = createTSGraphsFromTSProduct(_tsproduct, _displayProps, _tslist,
		new GRLimits(0.0,0.0, getWidth(), getHeight()));
	checkTSProductGraphs(_tsproduct, _tsgraphs);
	// Add the saved information.
	if ( tsgraphListOld.size() == _tsgraphs.size() ) {
		for ( int igraph = 0; igraph < _tsgraphs.size(); igraph++ ) {
			_tsgraphs.get(igraph).setSelectedTimeSeriesList(selectedTimeSeriesListOld.get(igraph));
		}
	}

	setDrawingLimits();
	setGraphDrawingLimits();
	resetGraphDataLimits(v);
	_force_redraw = true;
	clearView();
	repaint();

	if ( (end == null) && (start == null) ) {
		return;
	}

	// Set the dates into the graphs.

	// Because the graphs might be zoomed in,
	// their start and end dates might compose a smaller range than the start and end dates from the time series.
	// For this reason, the max start and max end dates are set from the time series (above)
	// and the current graph zoom dates are set from the current graph dates (above).

	for ( TSGraph g: _tsgraphs ) {
		if (!g.isReferenceGraph()) {
			g.setEndDate(end);
			g.setMaxEndDate(maxEnd);
			g.setStartDate(start);
			g.setMaxStartDate(maxStart);
			g.setComputeWithSetDates(true); // By here the graphs need to be created with the last period that was displayed.
			g.computeDataLimits(false); // This says to use the set dates, not the maximum from initial graph creation.
		}
	}
}

/**
Resets the data limits for each graph during a call to reinitializeGraphs().
@param graphDataLimitList a list of TSGraphDataLimits to control the graph limits
*/
private void resetGraphDataLimits(List<TSGraphDataLimits> graphDataLimitsList ) {
	int tsCount = 0;
	int vCount = 0;
	List<String> ids = null;
	List<TS> tslist = null;
	for ( TSGraph graph: _tsgraphs ) {
		tslist = graph.getTSList();
		ids = new ArrayList<>();
		for ( TS ts: tslist ) {
			if (ts == null) {
				ids.add("null");
			}
			else {
				ids.add(ts.getIdentifierString());
			}
		}
		ids = StringUtil.sortStringList(ids);
		tsCount = tslist.size();
		for ( TSGraphDataLimits graphDataLimits : graphDataLimitsList ) {
			vCount = graphDataLimits.getNumTimeSeries();
			if ( vCount == tsCount) {
				List<String> vids = graphDataLimits.getTimeSeriesIds();
				if (stringListsAreEqual(ids, vids)) {
					GRLimits dataLimits = graphDataLimits.getDataLimits();
					graph.setDataLimitsForDrawing(dataLimits);
				}
			}
		}
	}
}

/**
Save the graph to an SVG file.
This is essentially equivalent to printing, but use an SVG graphics driver instead.
@param path Path to SVG file to save.  The path is not adjusted and therefore
should generally be specified as absolute and with the *.svg extension.
@param driver the SVG driver (library) to use, either "Batik" or "JFreeSVG"
*/
public void saveAsSVG ( String path, String driver )
throws FileNotFoundException, IOException {
    Graphics g = null;
    if ( driver.equalsIgnoreCase("Batik") ) {
    	g = TSGraphJComponent_SaveAsSVG.createGraphics();
    	// Render into the SVG Graphics2D implementation:
    	// - the Graphics instance will be cast to Graphics2D in the 'paint' method
    	__paintForSVG = true;
    	paint(g);
    	__paintForSVG = false;

    	// Render the graph to the file.
    	TSGraphJComponent_SaveAsSVG.saveGraphics ( g, path );
    }
    else {
    	g = new SVGGraphics2D ((int)this._drawlim_page.getWidth(), (int)this._drawlim_page.getHeight());

    	__paintForSVG = true;
    	paint(g);
    	__paintForSVG = false;
    	
    	// Write the graphics to the file.
    	SVGUtils.writeToSVG(new File(path), ((SVGGraphics2D)g).getSVGElement());
    }

}

/**
Scroll the current zoom group a multiple of the visible page.
@param pages Number of pages to scroll (-1.0 is one page left, 1.0 is one page right).
@param notifyListeners If true, the tsViewZoom() method is called for the new data limits.
*/
public void scroll ( double pages, boolean notifyListeners ) {
	// Loop through the zoom levels and zoom each group of graphs to the maximum data extent within the zoom group.
	// Need a concept of an active zoom group.

	if ( pages == 0.0 ) {
		// Don't need to do anything.
		return;
	}

	TSGraph tsgraph;
	GRLimits maxDataLimits = null;
	String propValue;
	int zoomGroup = 0;
	int numZoomGroups = _tsproduct.getNumZoomGroups();
	GRLimits currentDataLimits = null;
	GRLimits newDataLimits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < numZoomGroups; iz++ ) {
		// Loop through and determine the maximum and current limits for the zoom group (zoom groups are 1...N).
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi(_tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			if ( maxDataLimits == null ) {
				maxDataLimits = new GRLimits(tsgraph.getMaxDataLimits() );
			}
			else {
			    maxDataLimits = maxDataLimits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " + max_data_limits );
		// Now loop through again and set the data limits for graph in the zoom group.
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi(_tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			propValue = _tsproduct.getLayeredPropValue ("ZoomEnabled", isub, -1, false );
			if ( !propValue.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the same, don't do anything.
			currentDataLimits = tsgraph.getDataLimits();
			if ( maxDataLimits.equals( currentDataLimits) ) {
				continue;
			}
			// Else, reset the visible window to the end of the period with a data width equal to the current limits.
			// Start by copying the current limits.
			newDataLimits = new GRLimits ( currentDataLimits );
			if ( pages > 0.0 ) {
				// First reset the right X value.
				newDataLimits.setRightX ( currentDataLimits.getRightX() + pages*currentDataLimits.getWidth() );
				// If the right X is past the end, set it to the end.
				if ( newDataLimits.getRightX() > maxDataLimits.getRightX() ) {
					newDataLimits.setRightX ( maxDataLimits.getRightX() );
				}
				// Now set the left edge relative to the right.
				newDataLimits.setLeftX ( newDataLimits.getRightX() - currentDataLimits.getWidth() );
				// Do a final check against the overall limits.
				if ( newDataLimits.getLeftX() < maxDataLimits.getLeftX() ) {
					newDataLimits.setLeftX ( maxDataLimits.getLeftX() );
				}
			}
			else {
			    // First reset the left X value (pages is negative so add the second term).
				newDataLimits.setLeftX ( currentDataLimits.getLeftX() + pages*currentDataLimits.getWidth() );
				// If the left X is past the end, set it to the end.
				if ( newDataLimits.getLeftX() < maxDataLimits.getLeftX() ) {
					newDataLimits.setLeftX ( maxDataLimits.getLeftX() );
				}
				// Now set the right edge relative to the left.
				newDataLimits.setRightX ( newDataLimits.getLeftX() + currentDataLimits.getWidth() );
				// Do a final check against the overall limits.
				if ( newDataLimits.getRightX() > maxDataLimits.getRightX() ) {
					newDataLimits.setRightX ( maxDataLimits.getRightX() );
				}
			}
			tsgraph.setDataLimitsForDrawing ( newDataLimits );
			if ( notifyListeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so need to notify all other graphs in
				//	the same zoom group (currently assume all).
				//
				// 2)	This is not a reference graph. Assume for now it is in the same zoom group as the reference window
				//
				// Notify the TSViewListeners that we are zooming.
				// This is currently only done between a reference graph in this component and
				// another graph that is managing the notify the reference graph.
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but a cast is done later so don't pass null.
					_listeners[il].tsViewZoom ( tsgraph, new GRLimits(), newDataLimits );
				}
			}
		}
	}
	// Refresh the component.
	refresh();
}

/**
Scroll the current zoom group to the end of the available data.  If zoomed out, no action is taken.
@param notifyListeners If true, the tsViewZoom() method is called for the new data limits.
*/
public void scrollToEnd ( boolean notifyListeners ) {
	// Loop through the zoom levels and zoom each group of graphs to the maximum data extent within the zoom group.
	// Need a concept of an active zoom group.

	TSGraph tsgraph;
	GRLimits maxDataLimits = null;
	String propValue;
	int zoomGroup = 0;
	int numZoomGroups = _tsproduct.getNumZoomGroups();
	GRLimits currentDataLimits = null;
	GRLimits newDataLimits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < numZoomGroups; iz++ ) {
		// Loop through and determine the maximum and current limits for the zoom group (zoom groups are 1...N).
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi(_tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			if ( maxDataLimits == null ) {
				maxDataLimits = new GRLimits(tsgraph.getMaxDataLimits() );
			}
			else {
			    maxDataLimits = maxDataLimits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " + max_data_limits );
		// Now loop through again and set the data limits for graph in the zoom group.
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi( _tsproduct.getLayeredPropValue ( "ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			propValue = _tsproduct.getLayeredPropValue ("ZoomEnabled", isub, -1, false );
			if ( !propValue.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the same, don't do anything.
			currentDataLimits = tsgraph.getDataLimits();
			if ( maxDataLimits.equals( currentDataLimits) ) {
				continue;
			}
			// Else, reset the visible window to the end of the period with a data width equal to the current
			// limits.  Start by copying the maximum limits.
			newDataLimits = new GRLimits ( maxDataLimits );
			// Now reset the end X value.
			newDataLimits.setLeftX ( maxDataLimits.getRightX() - currentDataLimits.getWidth() );
			tsgraph.setDataLimitsForDrawing ( newDataLimits );
			if ( notifyListeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so need to notify all other graphs in
				//	the same zoom group (currently assume all).
				//
				// 2)	This is not a reference graph. Assume for now it is in the same zoom group as the reference window
				//
				// Notify the TSViewListeners that are zooming.
				// This is currently only done between a reference graph in this component and another graph that is managing the
				// notify the reference graph.
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but a cast is done later so don't pass null.
					_listeners[il].tsViewZoom ( tsgraph, new GRLimits(), newDataLimits );
				}
			}
		}
	}
	// Refresh the component.
	refresh();
}

/**
Scroll the current zoom group to the start of the available data.  If zoomed out, no action is taken.
@param notifyListeners If true, the tsViewZoom() method is called for the new data limits.
*/
public void scrollToStart ( boolean notifyListeners ) {
	// Loop through the zoom levels and zoom each group of graphs to the maximum data extent within the zoom group.
	// Need a concept of an active zoom group.

	TSGraph tsgraph;
	GRLimits maxDataLimits = null;
	String propValue;
	int zoomGroup = 0;
	int numZoomGroups = _tsproduct.getNumZoomGroups();
	GRLimits currentDataLimits = null;
	GRLimits newDataLimits = null;
	int size = _tsgraphs.size();
	for ( int iz = 0; iz < numZoomGroups; iz++ ) {
		// Loop through and determine the maximum and current limits for the zoom group (zoom groups are 1...N).
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi(_tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			if ( maxDataLimits == null ) {
				maxDataLimits = new GRLimits( tsgraph.getMaxDataLimits() );
			}
			else {
			    maxDataLimits = maxDataLimits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " + max_data_limits );
		// Now loop through again and set the data limits for graph in the zoom group.
		for ( int isub = 0; isub < size; isub++ ) {
			zoomGroup = StringUtil.atoi(_tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoomGroup != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			propValue = _tsproduct.getLayeredPropValue ( "ZoomEnabled", isub, -1, false );
			if ( !propValue.equalsIgnoreCase("true") ) {
				continue;
			}
			// If the maximum limits and the current limits are the same, don't do anything.
			currentDataLimits = tsgraph.getDataLimits();
			if ( maxDataLimits.equals( currentDataLimits) ) {
				continue;
			}
			// Else, reset the visible window to the start of the period with a data width equal to the current limits.
			// Start by copying the maximum limits.
			newDataLimits = new GRLimits ( maxDataLimits );
			// Now reset the end X value...
			newDataLimits.setRightX ( maxDataLimits.getLeftX() + currentDataLimits.getWidth() );
			tsgraph.setDataLimitsForDrawing ( newDataLimits );
			if ( notifyListeners ) {
				//if (	!_is_reference_graph ||
					//(_is_reference_graph &&
					//() {
					//continue;
				//}
				// Two cases may be true:
				//
				// 1)	This is a reference graph component so need to notify all other graphs in the same zoom group
				//		(currently assume all).
				//
				// 2)	This is not a reference graph.
				//		Assume for now it is in the same zoom group as the reference window
				//
				// Notify the TSViewListeners that we are zooming.
				// This is currently only done between a reference graph in this component and another graph that is managing the
				// notify the reference graph...
				int nl = 0;
				if ( _listeners != null ) {
					nl = _listeners.length;
				}
				for ( int il = 0; il < nl; il++ ) {
					// The device limits are not used but a cast is done later so don't pass null.
					_listeners[il].tsViewZoom ( tsgraph, new GRLimits(), newDataLimits );
				}
			}
		}
	}
	// Refresh the component.
	refresh();
}

/**
Set the drawing limits for all drawing areas based on properties and window size.
The font sizes are considered when sizing the drawing areas.
*/
void setDrawingLimits() {
	// Buffer around drawing areas (helps separate things and also makes it easier to see drawing areas when in debug mode).
	double buffer = 2.0;
	// Figure out dimensions up front.

	// Main and sub-titles and footers will have zero size unless the titles are specified.

	double maintitle_height = 0.0;
	String maintitle_string = _tsproduct.getLayeredPropValue ( "MainTitleString", -1, -1, false );

	if ( (maintitle_string != null) && !maintitle_string.equals("") ) {
		// Get the text extents and set the height based on that.
		String maintitle_font = _tsproduct.getLayeredPropValue ( "MainTitleFontName", -1, -1, false );
		String maintitle_fontsize = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", -1, -1, false );
		String maintitle_fontstyle = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", -1, -1, false );
		GRDrawingAreaUtil.setFont ( _da_maintitle, maintitle_font, maintitle_fontstyle,
			StringUtil.atod(maintitle_fontsize) );
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( _da_maintitle, maintitle_string, GRUnits.DEVICE );
		maintitle_height = text_limits.getHeight();
		text_limits = null;
	}

	double subtitle_height = 0.0;
	String subtitle_string = _tsproduct.getLayeredPropValue ( "SubTitleString", -1, -1, false );

	if ( (subtitle_string != null) && !subtitle_string.equals("") ) {
		// Get the text extents and set the height based on that.
		String subtitle_font = _tsproduct.getLayeredPropValue ( "SubTitleFontName", -1, -1, false );
		String subtitle_fontsize = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", -1, -1, false );
		String subtitle_fontstyle = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", -1, -1, false );
		GRDrawingAreaUtil.setFont ( _da_subtitle, subtitle_font,
			subtitle_fontstyle, StringUtil.atod(subtitle_fontsize));
		GRLimits text_limits = GRDrawingAreaUtil.getTextExtents ( _da_subtitle, subtitle_string, GRUnits.DEVICE );
		subtitle_height = text_limits.getHeight();
		text_limits = null;
	}

	// Get the window limits.

	GRLimits window_limits = null;
	if ( _printing ) {
		window_limits = new GRLimits(); // Gets the page size.
		window_limits.setLeftX(_printBounds.getLeftX());
		window_limits.setBottomY(_printBounds.getBottomY());
		window_limits.setRightX(_printBounds.getRightX());
		window_limits.setTopY(_printBounds.getTopY());
		Message.printDebug(1, "", "PB: " + _printBounds);
		Message.printDebug(1, "", "WL: " + window_limits);
	}
	else {
	    window_limits = getLimits ( true );
	}

	_drawlim_page = new GRLimits ( window_limits );

	if (_printing) {
		Message.printDebug(1, "", "DL: " + _drawlim_page);
	}
	window_limits = null;

	// Set the drawing limits for the graph based on the dimensions of the other drawing areas.

	if ( _bounds == null ) {
		_bounds = getBounds();
		//checkAndSetBounds();
	}

	// Set the drawing limits based on what was determined above.

	// Drawing limits for the title and subtitle.

	if ( maintitle_height == 0.0 ) {
		// Zero height drawing area place holder.
		_drawlim_maintitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer), _drawlim_page.getTopY(),
			(_drawlim_page.getRightX() - buffer), _drawlim_page.getTopY() );
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
		_gtype + "Main title drawing limits are: " + _drawlim_maintitle );
	}

	if ( subtitle_height == 0.0 ) {
		// Zero height drawing area place holder.
		_drawlim_subtitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer), _drawlim_maintitle.getBottomY(),
			(_drawlim_page.getRightX() - buffer), _drawlim_maintitle.getBottomY() );
	}
	else {
	    _drawlim_subtitle = new GRLimits (
			(_drawlim_page.getLeftX() + buffer),
			(_drawlim_maintitle.getBottomY() - buffer - subtitle_height),
			(_drawlim_page.getRightX() - buffer),
			(_drawlim_maintitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.setDrawingLimits",
		_gtype + "Sub title drawing limits are: " + _drawlim_subtitle );
	}

	// Graph drawing area (always what is left).

	if ( _is_reference_graph ) {
		_drawlim_graphs = new GRLimits (
			_drawlim_page.getLeftX(), _drawlim_page.getBottomY(),
			_drawlim_page.getRightX(), _drawlim_page.getTopY() );
	}
	else {
	    _drawlim_graphs = new GRLimits (
			(_drawlim_page.getLeftX() + buffer), (_drawlim_page.getBottomY() + buffer),
			(_drawlim_page.getRightX() - buffer), (_drawlim_subtitle.getBottomY() - buffer) );
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "TSGraphJComponent.setDrawingLimits",
		_gtype + "Graph drawing limits are: " + _drawlim_graphs.toString() );
	}

	// Now set in the drawing areas.

	if ( (_da_maintitle != null) && (_drawlim_maintitle != null) ) {
		_da_maintitle.setDrawingLimits ( _drawlim_maintitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_subtitle != null) && (_drawlim_subtitle != null) ) {
		_da_subtitle.setDrawingLimits ( _drawlim_subtitle, GRUnits.DEVICE, GRLimits.DEVICE );
	}
	if ( (_da_graphs != null) && (_drawlim_graphs != null) ) {
		_da_graphs.setDrawingLimits ( _drawlim_graphs, GRUnits.DEVICE, GRLimits.DEVICE );
	}
}

/**
 * Set the flags indicating that redrawing (repaint) should be done no matter what.
 * This is needed because some logic may just copy the double buffer if the drawing limits have not changed,
 * rather than repaint.  For example, call from mousePressed() when a legend hotspot is pressed.
 * @param forceRedraw if true, redraw the graph (but don't clear it first), suitable for mouse zoom box.
 * @param forceRedrawClean if true, redraw the graph (and force clear), suitable for legend time series selection clicks
 */
void setForceRedraw ( boolean forceRedraw, boolean forceRedrawClean ) {
	this._force_redraw = forceRedraw;
	this._force_redraw_clean = forceRedrawClean;
}

/**
Set the drawing limits for the graphs on the full component.
This is typically called at initialization and when the component size changes
(or a property changes that controls the positioning and size of graphs).
The logic in this method needs to be consistent with that in the createTSGraphsFromTSProduct() code.
*/
public void setGraphDrawingLimits () {
	int nsubs = _tsgraphs.size();
	TSGraph tsgraph;
	GRLimits drawlim;
	// Loop through the graphs.  If a reference graph, we should only go through once (but leave in the loop for now).
	String propVal = _displayProps.getValue("ReferenceGraph");
	boolean referenceGraph = false;
	if ( (propVal != null) && propVal.equalsIgnoreCase("true") ) {
		referenceGraph = true;
	}

	double totalHeight = _drawlim_graphs.getHeight();

	// Loop through and find all the subproducts that have had y size percents defined.  Total their percents.
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
		prop_val = _tsproduct.getLayeredPropValue("LayoutYPercent", isub, -1, false);
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

	// Go back through and set the calculated size for all the ones that have not had it defined.
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
		tsgraph = _tsgraphs.get(i);
		if ( tsgraph == null ) {
			continue;
		}
		if ( referenceGraph ) {
			// A reference graph so the dimension will be all of the component.
			drawlim = new GRLimits ( _drawlim_graphs );
		}
		else {
		    // Determine the drawing limits from the number of graphs and their layout.
			// Set the drawing limits for the graph on the page as an even fraction of the whole page in the vertical direction
			// (taking up the full horizontal dimension).
			// Later can allow more options to place graphs on the page.
			// The drawing limits will be reset whenever the component size changes.

			runningTotal += heights[i];
			drawlim = new GRLimits(
				_drawlim_graphs.getLeftX(), _drawlim_graphs.getTopY() - runningTotal,
				_drawlim_graphs.getRightX(), _drawlim_graphs.getTopY() - runningTotal + heights[i]);
//System.out.println("" + drawlim);
			/*
			drawlim = new GRLimits (
				_drawlim_graphs.getLeftX(), _drawlim_graphs.getTopY() - (i + 1)*height,
				_drawlim_graphs.getRightX(), _drawlim_graphs.getTopY() - i*height );
			*/
		}
		// The following triggers resetting the dimensions of all the drawing limits for the graph.
		tsgraph.setDrawingLimits ( drawlim );
	}
}

/**
Set the interaction mode.
@param mode Interaction mode.
*/
public void setInteractionMode ( TSGraphInteractionType mode ) {
	if ( (mode == TSGraphInteractionType.NONE) || (mode == TSGraphInteractionType.SELECT) ||
		(mode == TSGraphInteractionType.ZOOM) || (mode == TSGraphInteractionType.EDIT)) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, _gtype + "TSGraphJComponent.setInteractionMode",
			"Set interaction mode to " + mode );
		}
		_interaction_mode = mode;
	}
}

// TODO SAM 2007-05-09 Need to evaluate whether to implement.
/**
Show an information dialog indicating the closest time series and value to the click.
@param tsgraph TSGraph where the select occurred.
@param x X-coordinate of mouse click, from event handler.
@param y Y-coordinate of mouse click, from event handler.
*/
public void showInfoDialog ( TSGraph tsgraph, int x, int y ) {
	GRPoint datapt = tsgraph.getLeftYAxisGraphDrawingArea().getDataXY ( x, y, GRCoordinateType.DEVICE );
	if ( datapt == null ) {
		return;
	}

	// Convert the y-coordinate to a date.
	DateTime d = new DateTime ( datapt.x, true );
	// Find the time series with a y-value closest to the data point.
	int size = 0;
	List<TS> tslist = tsgraph.getTSList();
	if ( tslist != null ) {
		size = tslist.size();
	}
	double ts_data = 0.0;
	TS ts = null, mints = null;
	double mindiff = 1.0e10;
	double diff = 0.0;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts == null ) {
			continue;
		}
		// Set the date precision to that of the time series.
		d.setPrecision ( ts.getDataIntervalBase() );
		// Get the data value for the time series.
		ts_data = ts.getDataValue ( d );
		if ( ts.isDataMissing(ts_data) ) {
			continue;
		}
		// Compare to the nearest one (avoid abs() for speed).
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
		new ResponseJDialog ( _parent, "Time Series Information",
		"Unable to find nearest time series.", ResponseJDialog.OK );
	}
	else {
	    d.setPrecision ( mints.getDataIntervalBase() );
		new ResponseJDialog ( _parent, "Time Series Information",
		"Nearest time series is:\n" +
		"Time Series Identifier:  " + mints.getIdentifier() + "\n" +
		"Time Series Description:  " + mints.getDescription() + "\n"+
		"Date:  " + d.toString() + "\n" +
		"Data value:  " + StringUtil.formatString(mints.getDataValue(d),
		DataUnits.getOutputFormatString(mints.getDataUnits(),0,2)) +
			" " + mints.getDataUnits(), ResponseJDialog.OK );
	}
}

/**
Tests to see whether two sorted lists, each of which contain Strings, contain the same strings in the same order.
@param v1 the first list to check.
@param v2 the second list to check.
@return true if the lists contain the same strings (compared by equalsIgnoreCase()) in the same order, or are both null.
Returns false if only one is null or the sizes of the lists are different or they don't contain the same strings, element-by-element.
*/
private boolean stringListsAreEqual(List<String> v1, List<String> v2) {
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
			s1 = v1.get(i);
			s2 = v2.get(i);
			if (!s1.equalsIgnoreCase(s2)) {
				return false;
			}
		}
		return true;
	}
}

/**
 * Return a property=value list of properties, separated by newline character.
 * @return property list string for instance.
 */
public String toString () {
	// For now return the parent version, outputting drawing areas.
	return super.toString(true);
}

/**
Handle the mouse motion event from another TSView (likely a Reference TSView).
Currently this does nothing.
@param g TSGraph that mouse motion occurred in.
@param devpt Coordinates of mouse in device coordinates (pixels).
@param datapt Coordinates of mouse in data coordinates.
*/
public void tsViewMouseMotion (TSGraph g, GRPoint devpt, GRPoint datapt ) {
}

/**
Handle the select event from another TSGraphJComponent (likely a Reference TSGraphJComponent).
Currently this does nothing.
@param g TSGraph that select occurred in.
@param dev_shape Coordinates of mouse in device coordinates (pixels).
@param data_shape Coordinates of mouse in data coordinates.
@param selected list of selected TS.  Currently ignored.
*/
public void tsViewSelect ( TSGraph g, GRShape dev_shape, GRShape data_shape, List<Object> selected ) {
}

/**
Handle the zoom event from another TSView (likely a Reference TSView).
This resets the data limits for this TSGraphJComponent to those specified (if not null) and redraws the TSGraphJComponent.
If a reference map, no zooming will occur in the reference window.
@param g TSGraph where the zoom event occurred.
@param dev_shape Limits of zoom in device coordinates (pixels).
@param data_shape Limits of zoom in data coordinates.
*/
public void tsViewZoom (TSGraph g, GRShape dev_shape, GRShape data_shape) {
	if ( data_shape == null ) {
		// Do nothing.
		return;
	}
	//Message.printStatus ( 1, "", _gtype + "Received a zoom event from " +
	//	_tsproduct.getLayeredPropValue("MainTitleString",
	//		g.getSubProductNumber(), -1, false) + ":" +
	//		" isref:" + g.isReferenceGraph() + " :" + (GRLimits)data_shape );
	zoom ( g, (GRLimits)dev_shape, (GRLimits)data_shape );
}

/**
Overrule the default update to not clear the screen.
@param g Graphics instance to use for drawing.
*/
public void update(Graphics g) {
	// Just call paint:
	// - the Graphics instance will be cast to Graphics2D inside the 'paint' method
	paint ( g );
}

/**
 * Apply a zoom, such as the reference graph zoom history from "previous" and "next" actions.
 * Currently this method has only been enabled for the reference graph, to enable responding
 * to previous/next zoom actions in the TSViewGraphJFrame interface.
 * @param newdataLimits new zoom limits, in data units
 */
public void zoom ( GRLimits newdataLimits ) {
	// Call the other method with necessary data.
	GRLimits mouseLimits = null;
	if ( _is_reference_graph ) {
		if ( _tsgraphs.size() > 0 ) {
			TSGraph tsgraph = _tsgraphs.get(0);
			zoom ( tsgraph, mouseLimits, newdataLimits );
		}
	}
}

/**
Apply a zoom that has occurred in one graph to other graphs.
This is done by setting the data limits in the related graphs.
Only graphs in the same zoom group are updated and it assumed that each graph will interpret the limits appropriately
(e.g., use the new X axis date limits but handle its own Y axis limits).
@param tsgraph TSGraph that generated the zoom event.
@param mouse_limits Component limits for zoom extent (currently not used).
@param newdata_limits Data limits for zoom extent in the original graph.
*/
public void zoom ( TSGraph tsgraph, GRLimits mouse_limits, GRLimits newdata_limits ) {
	String zoom_group = _tsproduct.getLayeredPropValue ( "ZoomGroup", tsgraph.getSubProductNumber(), -1, false );
	//Message.printStatus ( 1, "", _gtype + "zoom() data limits are" + newdata_limits.toString() );
	int size = _tsgraphs.size();
	TSGraph tsgraph2;
	for ( int isub = 0; isub < size; isub++ ) {
		tsgraph2 = _tsgraphs.get(isub);
		if ( tsgraph2.canZoom() && zoom_group.equalsIgnoreCase(
			_tsproduct.getLayeredPropValue ( "ZoomGroup", isub, -1, false ) ) ) {
			//if ( tsgraph2.isReferenceGraph() ) {
				tsgraph2.setDataLimitsForDrawing ( newdata_limits );
			//}
		}
	}
	refresh ();
}

/**
Zoom out for all the graphs and refresh the component.
This is typically called from a container like the TSViewGraphFrame.
*/
public void zoomOut() {
	zoomOut(true);
}

/**
Zoom out for all the graphs and optionally redraw.
This method is also called one time in the first paint to force the different graphs to synchronize their zoom levels.
@param re_draw If true, redraw the component after zooming.  If false, just set
the data limits in the graphs but do not redraw (a redraw may occur elsewhere after this method is called).
*/
public void zoomOut ( boolean re_draw ) {
	int size = _tsgraphs.size();
	// Loop through the zoom levels and zoom each group of graphs to the maximum data extent within the zoom group.

	TSGraph tsgraph;
	GRLimits max_data_limits = null;
	String prop_value;
	int zoom_group = 0;
	int num_zoom_groups = _tsproduct.getNumZoomGroups();
	for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
		// Loop through and determine the maximum limits for the zoom group (zoom groups are 1...N).
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi( _tsproduct.getLayeredPropValue ("ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			if ( max_data_limits == null ) {
				max_data_limits = new GRLimits( tsgraph.getMaxDataLimits() );
			}
			else {
				max_data_limits = max_data_limits.max(tsgraph.getMaxDataLimits());
			}
		}
		//Message.printStatus ( 1, "",
		//"Maximum limits for zoom group [" + iz + "] are " + max_data_limits );
		// Now loop through again and set the data limits for graph in the zoom group.
		for ( int isub = 0; isub < size; isub++ ) {
			zoom_group = StringUtil.atoi( _tsproduct.getLayeredPropValue ( "ZoomGroup", isub, -1, false) );
			if ( zoom_group != (iz + 1) ) {
				continue;
			}
			tsgraph = _tsgraphs.get(isub);
			prop_value = _tsproduct.getLayeredPropValue ( "ZoomEnabled", isub, -1, false );
			if ( prop_value.equalsIgnoreCase("true") ) {
				if (tsgraph.getNumTS() > 0) {
					tsgraph.setDataLimitsForDrawing(max_data_limits);
				}
			}
		}
	}
	if ( re_draw )  {
		// Refresh the component.
		refresh();
	}
}

/**
Zoom to the specified period for all the graphs and optionally redraw.
It is assumed that zoomOut() has already been called to compute the maximum limits.
@param re_draw If true, redraw the component after zooming.  If false, just set
the data limits in the graphs but do not redraw (a redraw may occur elsewhere after this method is called).
*/
public void zoomToVisiblePeriod ( DateTime visibleStart, DateTime visibleEnd, boolean re_draw ) {
    int size = _tsgraphs.size();
    // Loop through the zoom levels and zoom each group of graphs to the maximum data extent within the zoom group.

    TSGraph tsgraph;
    int zoom_group = 0;
    int num_zoom_groups = _tsproduct.getNumZoomGroups();
    for ( int iz = 0; iz < num_zoom_groups; iz++ ) {
        // Loop through and set the data limits for graph in the zoom group to the specified period (leave the value axis the same).
        for ( int isub = 0; isub < size; isub++ ) {
            zoom_group = StringUtil.atoi( _tsproduct.getLayeredPropValue ( "ZoomGroup", isub, -1, false) );
            if ( zoom_group != (iz + 1) ) {
                continue;
            }
            tsgraph = _tsgraphs.get(isub);
            if (tsgraph.getNumTS() > 0) {
                GRLimits limits = tsgraph.getDataLimits();
                // Set the period.
                limits.setLeftX(visibleStart.toDouble());
                limits.setRightX(visibleEnd.toDouble());
                tsgraph.setDataLimitsForDrawing(limits);
            }
        }
    }
    if ( re_draw )  {
        // Refresh the component.
        refresh();
    }
}

/**
 * Controls display of cross hair cursor on graph.
 * @param display If true displays cross hair
 */
public void setDisplayCursor(boolean display) {
    //_displayCrossHairCursor = display;
}

/**
 * Set the editor used to edit a time series that have editable=true.
 * @param tsGraphEditor editor instance
 */
public void setEditor ( TSGraphEditor tsGraphEditor ) {
    _tsGraphEditor = tsGraphEditor;
}

}