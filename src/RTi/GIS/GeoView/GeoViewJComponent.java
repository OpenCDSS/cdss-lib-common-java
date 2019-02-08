// GeoViewJComponent - class to control drawing of geographic data window

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

// ----------------------------------------------------------------------------
// GeoViewJComponent - class to control drawing of geographic data window
// ============================================================================
// Copyright:	See the COPYRIGHT file.
// ============================================================================
// Notes:	(1)	For now assume that each coverage has only one shape
//			type.
// ----------------------------------------------------------------------------
// History:
//
// 8 Jul 1996	Steven A. Malers	Initial version.
//		Riverside Technology,
//		inc.
// 05 Mar 1997	Matthew J. Rutherford,	Added functions and members for use in
//		RTi			linked list, and getFile() routine.
// 25 Mar 1997	MJR, RTi		Write MFC versions.
// 09 Jul 1997	Jay J. Fucetola		Converted to Java.
// 14 Jun 1999	SAM, RTi		Clean up the code.  Start updating to
//					use the new GR.
// 29 Jun 1999	CEN, RTi		Implemented drawBigPictureLayerView
// 07 Jul 1999	SAM, RTi		Start adding legend capabilities.
// 08 Jul 1999	CEN, RTi		Added getDataLimits and setDataLimits.
// 12 Jul 1999	CEN, RTi		Changed clearView private to public and
//					added getDrawingArea.
// 					Added reference map.
// 13 Jul 1999	SAM, RTi		Implement GeoViewListener here so this
//					class can listen for imposed zoom events
//					from a ReferenceGeoView, etc.  Add
//					GeoRecord searches in layers if the
//					SelectGeoRecords property is true.
//					Add a PropList to track GeoView
//					propeties.
// 28 Jul 1999	CEN, RTi		Modified drawBigPictureLayerView
//					to take a vector of layers rather than
//					a single layer to associate BP with.
// 30 Aug 1999	SAM, RTi		Added geoViewGetLabel logic.
// 08 Sep 1999	CEN, RTi		Added geoViewLegend.
// 09 Sep 1999	CEN, RTi		Created members for legend to access
//					for __bigPictureZMax, __bigPictureActive
// 01 Dec 1999	SAM, RTi		Add selectShapesUsingObjects() and
//					ability to draw selected shapes in
//					select color.
// 21 Jun 2000	CEN, RTi		Modified selectShapesUsingObjects
//					so code could compile
// 10 Jul 2000	SAM, RTi		Try to fix rubber band line not drawing
//					nicely.
// 19 Feb 2001	SAM, RTi		Change GUI to JGUIUtil.
// 27 Jun 2001	SAM, RTi		Minor cleanup.  Start supporting
//					use in GeoViewPanel.  Add boolean to
//					indicate whether the GeoView is a
//					reference GeoView (will start to phase
//					in so the ReferenceGeoView class can be
//					eliminated).  Add support for
//					transparent polygons.
// 06 Aug 2001	SAM, RTi		Finalize the general features for CDSS.
//					Fix so the reference map draws the
//					zoom box correctly.
// 25 Sep 2001	SAM, RTi		Keep working on CDSS features,
//					especially to speed performance and
//					minimize memory use.  Do the following:
//					*	Don't draw shape if it does not
//						intersect the plotting area data
//						limits.  This is OK since we
//						currently expect the data to be
//						already projected.
//					*	Allow non-point data to be
//						selected.
//					*	Fix so that when zoomed, the
//						data limits are set to the full
//						extent of the view window.
//					*	Check colors more thoroughly
//						when drawing.
//					*	Add projection for view.
// 04 Oct 2001	SAM, RTi		Add INTERACTION_INFO mode and
//					appropriate listener methods.  Add
//					removeGeoViewListener().
// 11 Oct 2001	SAM, RTi		Add code to use the new GRSymbol label
//					methods and data - use to create labels
//					from layer attribute data.  Fix but
//					where a window resize was not resetting
//					the data limits and hence some shapes
//					were not getting drawn.
// 2001-10-18	SAM, RTi		Everything seems to be working now with
//					the new GeoViewPanel, etc.  Do a code
//					sweep to clean up the code, set
//					variables to null when done, update
//					javadoc.
// 2001-11-20	SAM, RTi		Fix bug where map was going white when
//					small zoom box was drawn.
// 2001-11-27	SAM, RTi		Allow projections in normal layers
//					(previously had implemented for grids).
// 2001-12-04	SAM, RTi		Change name from GeoView to
//					GeoViewCanvas and update to use Swing.
//					Add the ability to do appended
//					selections.
//					Update printView() to not throw an
//					exception on cancel (just return).
// 2002-01-08	SAM, RTi		Change name from GeoViewCanvas to
//					GeoViewJComponent and extend from
//					GRJComponentDevice.
// 2002-07-23	SAM, RTi		Change GRSymbol "pointSymbol" methods
//					to "style".
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Updated to incorporate changes made to
//					GR package.
// 2003-05-08	JTS, RTi		Made further changes to incorporate
//					non-Swing code.
// 2003-05-12	JTS, RTi		* Added code to support use of popup
//					  menus.
//					* Corrected private variable naming
//				 	  convention.
// 2003-05-21	JTS, RTi		Implemented Graphics2D printing 
//					(first draft)
// 2003-05-22	JTS, RTi		* Added getInteractionMode().
//					* Changed the selection color to pink.
// 2003-05-23	JTS, RTi		* Added code for "reminded repainters",
//					  mainly so that a redraw can be forced
//					  on the reference geoview by the main
//					  geoview.
//					* Map no longer redraws at a different
//					  zoom after printing.
// 2004-08-02	JTS, RTi		Added support for animated layers.
// 2004-08-10	JTS, RTi		Added support for drawing summary layer
//					teacups.
// 2004-09-16	JTS, RTi		Changed the call of isPopupTrigger to
//					use the one in the JPopupMenu.
// 2004-10-06	JTS, RTi		Added support for unsigned vertical
//					bars.
// 2004-10-13	JTS, RTi		Added the ability to draw a legend
//					on the map, which is mostly useful
//					for making hard copies.
// 2005-04-27	JTS, RTi		* Added all data members to finalize().
//					* Reworked finalize() to clean up
//					  layer view memory better.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.print.PageFormat;
import java.awt.print.Printable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import RTi.GR.GRAspect;
import RTi.GR.GRAxis;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingArea;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLegend;
import RTi.GR.GRLimits;
import RTi.GR.GRPoint;
import RTi.GR.GRPolygon;
import RTi.GR.GRScaledClassificationSymbol;
import RTi.GR.GRScaledTeacupSymbol;
import RTi.GR.GRShape;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJTree_Node;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PrintUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;

import RTi.Util.Time.StopWatch;

/**
This class provides a JComponent for displaying geographic information common to
GIS.  The GeoView allows multiple GeoLayerViews to be displayed.  The code
provides methods for initiating printing as well as handling zooming and
selects.  Performance options include double-buffering.
This class also implements GeoViewListener.  This is typically used to allow
a ReferenceGeoView to be enabled that calls the listener methods in this
man GeoView.  Therefore, zoom control can occur in both GeoViews.
*/
@SuppressWarnings("serial")
public class GeoViewJComponent extends GRJComponentDevice
implements GeoViewListener, MouseListener, MouseMotionListener, Printable
{

/**
Interaction modes.
No special interaction.
*/
public static final int INTERACTION_NONE = 0;
/**
Select a feature.  When enabled, a mouse click or rubber-band box draw causes
the geoViewSelect() method of registered GeoViewListeners to be called.
*/
public static final int INTERACTION_SELECT = 1;
/**
Zoom mode.  This enables a rubber-band line (if the extents are bigger than
5 pixels in both direction).  The geoViewZoom() method of registered
GeoViewListeners are called.
*/
public static final int INTERACTION_ZOOM = 2;
/**
Select and get information for a feature.  When enabled, a mouse click or
rubber-band box causes the geoViewInfo() method of registered GeoViewListeners 
to be called.
*/
public static final int INTERACTION_INFO = 3;

/**
Private data used internally to indicate label sources.
*/
private static final int __LABEL_NODE = 0;
private static final int __LABEL_USING_ATTRIBUTE_TABLE = 1;
private static final int __LABEL_USING_GEOVIEW_LISTENER = 2;

private static final double __BORDER = 0.0;	// Border for view.

/**
GeoLayerViews that have been added to this GeoView for display, guaranteed to be non-null.
*/
private List<GeoLayerView> __layerViews = new ArrayList<GeoLayerView>();
/**
GeoViewAnnotationRenderers to display extra information as annotations on the map.
*/
private List<GeoViewAnnotationData> __annotationDataList = new ArrayList<GeoViewAnnotationData>();
/**
PropList for storing GeoView properties.
*/
private PropList __props = null;

/**
Data limits of displayed data.
*/
private GRLimits __dataLimits = null;

/**
Data limits of all GeoViewLayers (for zoom out) and auto-zooming to portion of map.
*/
private GRLimits __maxDataLimits = null;

/**
Drawing limits (JComponent dimensions) of current JComponent.
*/
private GRLimits __drawLimits = null;

/**
Projection for the GeoView.
*/
private GeoProjection __projection = null;

/**
Project for the GeoView.
*/
private GeoViewProject __project = null;

/**
Bounds of the JComponent (not the same as __drawLimits, which are in right-hand coordinate system).
*/
private Rectangle __bounds = null;		// Set with update.

/**
Frame that is including the GeoView as a component.  This is set to null if used in an applet.
*/
private JFrame __parent = null;

/**
Drawing area for GR drawing.
*/
private GRJComponentDrawingArea __grda = null;

private GeoViewLegend __geoViewLegend = null;

/**
Indicates that the paint method should force a redraw.
*/
private boolean __forceRedraw = true;

/**
Indicates whether the GeoView is a reference GeoView.
*/
private boolean __isReferenceGeoview = false;

private String __prefix = "Main: ";

/**
Listeners that are registered using addGeoViewListener() and which are called
to notify other components of changes in the GeoView.
*/
private GeoViewListener [] __listeners = null;

/**
Interaction mode for the GeoView (see INTERACTION_*).
*/
private int __interactionMode = INTERACTION_NONE;

/**
List to process labels.  This is re-used throughout drawing.
*/
private List<Object> __labelFieldList = new ArrayList<Object> ( 5 );

/**
Indicates if drawing should wait.  Use this if multiple layers are being
loaded and you don't want to see redraws between each one.
*/
private boolean _waiting = false;

/**
Used for drawing the legend of big picture information
*/
private boolean __bigPictureActive = false; 
private double __bigPictureZMax = 0;

/**
Indicates if the mouse tracker is enabled (default is true).  Mouse tracking
slows the GeoView slightly.
*/
private boolean __mousetrackerEnabled = true;

/**
Indicates if during selects GeoRecords should be found and returned.  If
enabled (the default is enabled), selects may be substantially slower.
Only visible and selected geolayers are searched when enabled.
*/
private boolean __selectGeoRecords = true;

/**
Coordinates for mouse events.
1 = first location<br>
2 = Current location<br>
prev = previous location
*/
private int __mouseX1 = -1;
private int __mouseY1 = -1;
private int __mouseX2 = -1;
private int __mouseY2 = -1;

/**
Indicates if rubber-banding is active (communication between zoom methods and
paint).  When rubber-banding, the paint draws rectangles using XOR graphics.
*/
private boolean __rubberBanding = false;

/**
Color to use when drawing selected shapes (default to ESRI select color).
The select color can also be specified for each layer view.
*/
private GRColor __selectColor = GRColor.yellow;

/**
Color for the select region.
*/
private GRColor __rubberBandColor = GRColor.red;

private JTextField __statusJTextField = null;

// Use to optimize performance.
GeoRecord _tmp_record = new GeoRecord();

private JPopupMenu __popup = null;

/**
Used to keep track of when the left button has been pressed (true) versus any
other mouse button.  This is so that only the left mouse button may draw rubber-banding lines.
*/
private boolean __leftMouseButton = false;

/**
Whether a page is currently being printed or not.
*/
private boolean __inPrinting = false;

/**
The list of other objects that need to know to repaint themselves when this object is repainted.
*/
private List<GeoViewJComponent> __remindedRepainters = null;

/**
The number of other objects that must repaint themselves when this object
is repainted.  Stored here because it is accessed a lot.
*/
private int __remindedRepaintersCount = 0;

/**
Used to know when to put up a wait cursor after a repaint.
*/
private boolean __checkWaitStatus = false;

/**
Whether the wait cursor was already up prior to a paint.
*/
private boolean __wasWaiting = false;

private Rectangle _bounds = null;

/**
Used to route around a Java weirdness and make the printed page be sent to
the printer only once.
*/
private int __lastPage = -1;

/**
Whether to redraw the reference map or not.
*/
boolean __redrawReference = true;

/**
Whether to do some drawing in an antialiased fashion or not.
*/
private boolean __antiAliased = false;

/**
Construct a blank JComponent with no GeoLayerViews.  Properties are initialized
to the defaults.
@param parent Parent Frame in which the GeoView is embedded.
*/
public GeoViewJComponent ( JFrame parent )
{	super ( "GeoView" );
	initialize ( null );
	__parent = parent;
}

/**
Construct a blank JComponent with no GeoLayerViews and the specified properties.
@param props Properties for GoeView.  The following properties are recognized:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>MouseTracker</b></td>
<td>Indicates whether the mouse tracker GeoViewListener feature should be
enabled.  If true, there will be a slight performance hit.</td>
<td>true (enabled)</td>
</tr>

<tr>
<td><b>SelectGeoRecords</b></td>
<td>Indicates for a select event whether GeoRecords should be returned.
<td>true (feature selects are processed)
</td>
</tr>

</table>
*/
public GeoViewJComponent ( JFrame parent, PropList props )
{	super ( "GeoView" );
	initialize ( props );
	__parent = parent;
}

/**
Add an annotation renderer.  This allows generic objects to be drawn on top of the map, allowing
rendering to occur by external code that is familiar with domain issues.  The GeoViewJPanel is passed
back to the renderer to allow full access to layer information, symbols, etc.
@param renderer the renderer that will be called when it is time to draw the object
@param objectToRender the object to render (will be passed back to the renderer)
@param objectLabel label for the object, to list in the GeoViewJPanel
@param limits the limits of the rendered feature, in data coordinates, used to scroll to annotations
@param the projection for the data
@return the annotation data that was added, or null if not added (usually due to duplicate).
*/
public GeoViewAnnotationData addAnnotationRenderer ( GeoViewAnnotationRenderer renderer,
	Object objectToRender, String objectLabel, GRLimits limits, GeoProjection projection )
{
	// Only add if the annotation is not already in the list
	List<GeoViewAnnotationData> annotationDataList = getAnnotationData();
	for ( GeoViewAnnotationData annotationData: annotationDataList ) {
		if ( (annotationData.getObject() == objectToRender) &&
			annotationData.getLabel().equalsIgnoreCase(objectLabel) ) {
			// Don't add again.
			return null;
		}
	}
	GeoViewAnnotationData annotationData =
		new GeoViewAnnotationData(renderer,objectToRender,objectLabel,limits,projection);
	annotationDataList.add ( annotationData );
	// Redraw the map with annotations
	repaint ();
	return annotationData;
}

/**
Add a GeoViewListener to receive GeoView events.  Multiple listeners can be
registered.  If an attempt is made to register the same listener more than
once, the later attempt is ignored.
@param listener GeoViewListener to add.
*/
public void addGeoViewListener ( GeoViewListener listener )
{	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added...
	// Resize the listener array...
	int size = 0;
	if ( __listeners != null ) {
		size = __listeners.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( __listeners[i] == listener ) {
			return;
		}
	}
	if ( __listeners == null ) {
		__listeners = new GeoViewListener[1];
		__listeners[0] = listener;
		Message.printStatus ( 1, "GeoView.addGeoViewListener",
		__prefix + "Added GeoViewListener" );
	}
	else {	// Need to resize and transfer the list...
		size = __listeners.length;
		GeoViewListener [] newlisteners =
			new GeoViewListener[size + 1];
		for ( int i = 0; i < size; i++ ) {
				newlisteners[i] = __listeners[i];
		}
		__listeners = newlisteners;
		__listeners[size] = listener;
		newlisteners = null;
	}
}

/**
Add a GeoLayerView to the GeoView.  This includes a GeoLayer and specific
view features (legend, etc.).  The limits will be recomputed to be the
maximum of the layers.
@param layer_view GeoLayerView to add.
*/
public void addLayerView ( GeoLayerView layer_view )
{	addLayerView ( layer_view, true );
}

/**
Add a GeoLayerView to the GeoView.  This includes a GeoLayer and specific view features (legend, etc.).
@param layerView GeoLayerView to add.
@param reset_limits true if the overall limits should be reset and used for
the redraw (use false if adding a layer and zoom has already been made).
Use true to zoom to see all layers.
*/
public void addLayerView ( GeoLayerView layerView, boolean reset_limits )
{	String routine = "GeoView.addLayerView";

	if ( layerView == null ) {
		return;
	}
	layerView.setView ( this );
	__layerViews.add ( layerView );

	// Do most of the following so we know the maximum limits...

	// Need to update the limits.  For now use the last one set...
	GeoLayer layer = layerView.getLayer();
	// Seems like this does not get done in paint in the right order?
	GRLimits new_drawLimits = getLimits(true); // Gets the JComponent size.
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, __prefix +"Drawing limits from JComponent are: " +
		new_drawLimits );
	}
	new_drawLimits.setLeftX ( __BORDER );
	new_drawLimits.setBottomY ( __BORDER );
	new_drawLimits.setRightX ( new_drawLimits.getRightX() - __BORDER );
	new_drawLimits.setTopY ( new_drawLimits.getTopY() - __BORDER );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		__prefix + "Drawing limits after border for device are: " + new_drawLimits );
	}
	// Comparison is based on after border...
	//if ( !__drawLimits.equals(new_drawLimits) ) {
		// Set to the new drawing limits for the redraw...
		__drawLimits = new GRLimits ( new_drawLimits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Drawing limits after reset: " + __drawLimits );
		}
	//}
	new_drawLimits = null;
	// Reset the data limits...
	boolean need_to_redraw = false;
	if ( layer != null ) {
		// If the "MaximumExtent" property is set, make sure that it is recognized used...
		boolean max_set = false;
		if ( (__project != null) && (__project.getPropList() != null) ){
			String prop_val = __project.getPropList().getValue ( "GeoView.MaximumExtent" );
			if ( prop_val != null ) {
				List<String> tokens = StringUtil.breakStringList ( prop_val, " \t,", StringUtil.DELIM_SKIP_BLANKS );
				if ( (tokens != null) && (tokens.size() == 4) ){
					__maxDataLimits = new GRLimits (
					StringUtil.atod(tokens.get(0)),
					StringUtil.atod(tokens.get(1)),
					StringUtil.atod(tokens.get(2)),
					StringUtil.atod(tokens.get(3)) );
					max_set = true;
				}
			}
		}
		if ( !max_set ) {
			// Save the maximum.  This will allow ZoomOut to go to
			// the full extent of the data layers.
			if ( __maxDataLimits == null ) {
				// Initialize...
				__maxDataLimits = new GRLimits (getProjectedLayerLimits(layer) );
			}
			else {
				__maxDataLimits = new GRLimits (__maxDataLimits.max(getProjectedLayerLimits(layer) ) );
			}
		}
		if ( (__dataLimits == null) || reset_limits ) {
			// Need something to draw...
			__dataLimits = new GRLimits(__maxDataLimits);
			need_to_redraw = true;
		}
	}
	if ( __grda == null ) {
		// Have not had data to draw but do now...
		// Set up one drawing area on the view...
		__grda = new GRJComponentDrawingArea( this, "GeoView", GRAspect.TRUE,
			__drawLimits, GRUnits.DEVICE, GRLimits.DEVICE, __dataLimits );
	}
	else {
		// Now can set the data limits...
		__grda.setDataLimits ( __dataLimits );
	}
	// Now repaint the canvas since we have added data...
	if ( need_to_redraw ) {
		__forceRedraw = true;
		repaint();
	}

	/*
	if ( layer != null ) {
		if ( __dataLimits == null ) {
			__dataLimits = new GRLimits(getProjectedLayerLimits(
				layer ));
		}
		else {	if ( reset_limits ) {
				__dataLimits = __dataLimits.max (
					getProjectedLayerLimits(layer) );
			}
		}
		// Save the maximum regardless of reset...
		__maxDataLimits = new GRLimits ( __dataLimits.max(
					getProjectedLayerLimits(layer) ) );
	}
	if ( __grda == null ) {
		// Have not had data to draw but do now...
		// Set up one drawing area on the view...
		__grda = new GRJComponentDrawingArea ( this, "GeoView",
			GRAspect.TRUE, __drawLimits,
			GRUnits.DEVICE, GRLimits.DEVICE, __dataLimits );
	}
	else {	// Now can set the data limits...
		__grda.setDataLimits ( __dataLimits );
	}
	// Now repaint the JComponent since we have added data...
	__forceRedraw = true;
	repaint();

	// Clean up...

	routine = null;
	layer = null;
	*/
}

/**
Adds a reminded repainted -- an object that will be informed every time this object is repainted.
@param c the GeoViewJComponent object to be reminded.
*/
public void addRemindedRepainter(GeoViewJComponent c) {
	__remindedRepainters.add(c);
	__remindedRepaintersCount++;
}

/**
Clear annotations.
*/
public void clearAnnotations ()
{
	List<GeoViewAnnotationData> annotationDataList = getAnnotationData();
	int size = annotationDataList.size();
	annotationDataList.clear();
	// Also redraw
	if ( size > 0 ) {
		// Previously had some annotations and now do not so redraw
		redraw();
	}
}

/**
Clear the view.  Need to do this manually rather than rely on default update()
to make sure it happens at the right time.
*/
public void clearView ()
{	if ( !_printing ) {
		// Fill in the background color.  Need this because
		// update() does not do (becaus of zooming)...
		_graphics.setColor ( getBackground() );
		__bounds = getBounds();
		_graphics.fillRect ( 0, 0, __bounds.width, __bounds.height );
	}
}

/**
Delete all GeoLayerView from the GeoView.  This will result in nothing being
drawn.  The limits are also reset to null data.
*/
public void deleteLayerViews ()
{
	__layerViews.clear();
	__layerViews = new ArrayList<GeoLayerView>();
	__dataLimits = null;
	__maxDataLimits = null;
	// __drawLimits are whatever is set for the current window...
	__forceRedraw = true;
	repaint();
}

/**
Delete a GeoLayerView from the GeoView.  For now, this deletes the first
layer view with a layer matching the file name.
@param filename Name of file for layer to delete.
*/
public void deleteLayerViewUsingFileName ( String filename )
{	if ( filename == null ) {
		return;
	}
	int size = __layerViews.size();
	boolean found = false;
	GeoLayer layer = null;
	GeoLayerView layerView = null;
	String layer_filename = null;
	for ( int i = 0; i < size; i++ ) {
		layerView = __layerViews.get(i);
		if ( layerView == null ) {
			continue;
		}
		layer = layerView.getLayer();
		if ( layer == null ) {
			continue;
		}
		layer_filename = layer.getFileName ();
		if ( filename == null ) {
			continue;
		}
		if ( layer_filename.equalsIgnoreCase(filename) ) {
			found = true;
			__layerViews.remove(i);
			i--;
			size--;
		}
	}

	if ( !found ) {
		// No need to redraw...
		return;
	}

	// Now repaint the JComponent since we have removed data...
	__forceRedraw = true;
	repaint();
}

/**
Deselect all of the shapes in all layer views.  More control may be added later
to allow only selected layers to be operated on.
*/
public void deselectAllShapes ()
{
	GeoLayer layer = null;
	for ( GeoLayerView layerView : getLayerViews() ) {
		layer = layerView.getLayer();
		layer.deselectAllShapes();
	}
}

// TODO SAM 2010-12-31 Consider moving to GR package
/**
Determine the label position for shapes, but looking at the centroid, etc.
*/
private double [] determineLabelXY ( GRShape shape, double[] labelXY )
{
	if ( shape.type == GRShape.POLYGON ) {
		//if ( shape.limits_found ) {
			labelXY[0] = (shape.xmax + shape.xmin)/2.0;
			labelXY[1] = (shape.ymax + shape.ymin)/2.0;
			return labelXY;
		//}
		//else {
		//	return null;
		//}
	}
	else {
		// Default is middle of limits...
		//if ( shape.limits_found ) {
			labelXY[0] = (shape.xmax + shape.xmin)/2.0;
			labelXY[1] = (shape.ymax + shape.ymin)/2.0;
			return labelXY;
		//}
		//else {
		//	return null;
		//}
	}
}

/**
Turns off double-buffering for the specified component.  Used in printing.
@param c the Component to turn off double-buffering for.
*/
private boolean disableDoubleBuffering(Component c) {
	if (c instanceof JComponent == false) {
		return false;
	}
	JComponent jc = (JComponent)c;
	boolean wasBuffered = jc.isDoubleBuffered();
	jc.setDoubleBuffered(false);
	return wasBuffered;
}

/**
Draw the big picture shapes to the view.
More than one bar can be drawn at a point.
@param layerView GeoLayerView to draw in current GeoView.
*/
private void drawBigPictureLayerView ( GeoLayerView layerView)
{	String rtn = "drawBigPictureLayerView";

	// break up try/catch later
	try {
	// Loop through the points and draw bars at each point.
	// Right justify the bars bottom on the point and always make the bar
	// 4 pixels/points wide and make the bar height for the maximum 
	// value always 50 pixels (either above or below the 
	// baseline).  Then interpolate to get the bar height for each data value.
	BigPictureLayer layer = (BigPictureLayer)layerView.getLayer();
	GRLimits big_picture_limits = layer.getBigPictureLimits();
	
	double ztop = 0, zbot = 0, largest_value=0,
		zmin = big_picture_limits.getBottomY(),
		zmax = big_picture_limits.getTopY();

	if ( Math.abs(zmin) <= zmax ) {
		// then the positive value controls the height of the
		// bar and equalize the bottom based on that ...
		largest_value = zmax;
	}
	else if ( zmin < 0.0 ) {
		// the negative value controls the height of the bar and equalize the top based on that ...
		largest_value = -zmin;
	}
	// next, convert largest_value to "nice" values
	double nice_labels [] = GRAxis.findLabels ( 0, largest_value, false, 1);
	if ( nice_labels == null ) {
		// Zero range?
		Message.printWarning ( 3, rtn, "Unable to get labels - zero data range?" );
		return;
	}
	int last_nice_labels_index = nice_labels.length -1;
	largest_value = nice_labels[last_nice_labels_index];
	ztop = largest_value;
	zbot = -largest_value;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, rtn, "zmin: " + zmin + ", zmax: " + zmax );
		Message.printDebug ( 10, rtn, "zbot: " + zbot + ", ztop: " + ztop );
	}
	__bigPictureZMax = largest_value;

	// ... big picture information
	DataTable bigPictureTable = layer.getBigPictureTable();

	int num_records = bigPictureTable.getNumberOfRecords();
	int num_fields = bigPictureTable.getNumberOfFields();
	double dataValue, barheight2, xp[]=new double[2], yp[]=new double[2];
	String id="", att_id;
	int id_index=0, att_index=0;
	boolean found_match;
	GRShape shape;
	GRPoint pt;

	// Width - set width of each big picture rectangle to 4 device units.
	GRLimits devlim = new GRLimits ( 4.0, 4.0 );
	GRLimits datalim = GRDrawingAreaUtil.getDataExtents ( __grda, devlim , 0 );
	double barwidth = datalim.getWidth();
	// calculate half_barwidth - this will be the space between big picture rectangles
	double half_barwidth = barwidth / 2.0;

	// Height - set height of largest big picture rectangle to 50 device units.
	GRLimits devlimh = new GRLimits ( 50.0, 50.0 );
	GRLimits datalimh = GRDrawingAreaUtil.getDataExtents ( __grda, devlimh , 0 );
	double barheight = datalimh.getHeight();
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, rtn, "barwidth: " + barwidth + ", barheight: " + barheight );
	}

	int numAssociatedLayers = layer.getNumAssociatedLayers();
	Message.printStatus ( 1, rtn, "Drawing " + num_records + " big picture records." );	
	for ( int z=0; z<numAssociatedLayers; z++ ) {
	// ... coordinates for layer that is being searched
	List<GRShape> shapes = layer.getShapes(z);
	// ... table which allows us to tie together coordinates with bigpice );
	DataTable attributeTable = layer.getAttributeTable(z);
	// Vector tableRecords = attributeTable.getTableRecords();
	int num_att_records = attributeTable.getNumberOfRecords();
	// loop through each record in the attribute table
	for ( int k=0; k<num_att_records; k++ ) {
		// Searching for the identifier field.
		att_id = ("" + attributeTable.getFieldValue (k, 0 )).trim();
		found_match = false;
		// loop through each record in the big picture attribute table
		for ( int i=0; i<num_records; i++ ) {
			id = ("" + bigPictureTable.getFieldValue (i, 0 )).trim();
			if ( id.equalsIgnoreCase ( att_id )) {
				// Found a match !!!!
				id_index = i;
				att_index = k;
				found_match = true;
				i=num_records; // end for loop
			}
		}

		if ( Message.isDebugOn )
			Message.printStatus ( 2, rtn, "found_match for att_id \"" + att_id + "\" = " + found_match );
		if ( !found_match ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 10, rtn, "Didn't find bigpicture match for att_id " + att_id );
			}
			continue;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, rtn, "Found attribute match for bigpicture id " + id );
		}

		// all the drawing is done with reference to the
		// center fo the bar but offset this point
		// from the x, y so that the bars are to the
		// left of the point.  This allows labels to be put to the right.

		shape = shapes.get(att_index);
		if ( (shape.type == GRShape.POINT) || (shape.type == GRShape.POINT_ZM) ) {
			// Draw a black line across the bottom of all the rectangles.
			// Start from the number of rectangles * width of recs
			// plus the number of spaces between recs * 1/2 width of recs.
			pt = (GRPoint)shape;
			xp[0] = pt.x - (((num_fields-2)*barwidth) + ((num_fields-2)*half_barwidth));
			xp[1] = pt.x - half_barwidth;
			yp[0] = pt.y;
			yp[1] = pt.y;

			__grda.setColor ( GRColor.black );
			GRDrawingAreaUtil.drawLine ( __grda, xp, yp );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 20, rtn,
					"Draw line: " + xp[0] + ", " + yp[0] + " to " + xp[1] + ", " + yp[1] );
			}

			// Loop through each field.
			// Big picture columns will be draw left to right.
			for ( int j=2; j<num_fields; j++ ) {
				dataValue = ((Double)bigPictureTable.getFieldValue ( id_index, j )).doubleValue();

				// set color
				if ( dataValue < 0.0 ) {
					__grda.setColor ( GRColor.red );
				}
				else if ( dataValue > 0.0 ) {
					__grda.setColor ( GRColor.blue );
				}
				else {
					__grda.setColor ( GRColor.black );
				}

				// Calculate the x,y coordinate for the rectangle.  For x, subtract the width
				// of the bars plus the width of the space between the bars.
				xp[0] = pt.x - (((num_fields-j)*barwidth) + ((num_fields-j)*half_barwidth));
				yp[0] = pt.y;

				// The height of this bar is interpolated
				// using the max/min barheight and the max/min data values.
				barheight2 = MathUtil.interpolate( dataValue, zbot, ztop, -1.0*barheight, barheight );

				// Now draw the rectangle ...
				if ( Message.isDebugOn ) {
					Message.printDebug ( 20, rtn, "Drawing rectangle from " + xp[0] + ", " + yp[0] + 
					" of width/height " + barwidth + " " + barheight2 );
				}
				GRDrawingAreaUtil.fillRectangle (__grda, xp[0], yp[0], barwidth, barheight2 );
			}

		}
	}
	}
	} catch ( Exception e ) {
		Message.printWarning ( 3, rtn, "Problems in drawBigPictureLayerView" );
		Message.printWarning ( 3, rtn, e );
	}
	__bigPictureActive = true;
}

/**
Draw a grid GeoLayerView.  Currently, the grid is draw cell by cell as polygons.
This is not the most efficient way to do it but can improve it later.  Much of
the low-level code deals with polygons because often only cells with non-zero
data are of interest.  This method is called from drawLayerView() so all the
checks for visibility, null symbol, etc., have been done.
@param layerView GeoLayerView to draw in current GeoView.
*/
private void drawGridLayerView ( GeoLayerView layerView )
{	GeoLayer layer = layerView.getLayer();
	if ( layer == null ) {
		return;
	}

	GRLegend legend = layerView.getLegend();
	GRSymbol symbol = legend.getSymbol();
	int classification_type = symbol.getClassificationType();
	GRColor outline_color = symbol.getOutlineColor();
	// Get the color once.  It will be used for single classification if necessary...
	GRColor color = symbol.getColor();

	// Loop through the cells in the grid...

	double xmin = __dataLimits.getMinX(),	// Use local copy of data limits
		xmax = __dataLimits.getMaxX(),	// to increase performance
		ymin = __dataLimits.getMinY(),
		ymax = __dataLimits.getMaxY();
	GeoGrid grid = (GeoGrid)(layer.getShapes().get(0));
	int rmin = grid.getMinRow();
	int rmax = grid.getMaxRow();
	int cmin = grid.getMinColumn();
	int cmax = grid.getMaxColumn();
	// Determine if checks should be made on limits to draw...
	boolean have__dataLimits = false;
	double min_to_draw = 0.0, max_to_draw = 0.0;
	String prop_value = layerView.getPropList().getValue("IgnoreDataOutside");
	if ( prop_value != null ) {
		List<String> v = StringUtil.breakStringList ( prop_value,",",0);
		if ( (v != null) && (v.size() == 2) ) {
			have__dataLimits = true;
			min_to_draw = StringUtil.atod((String)v.get(0));
			max_to_draw = StringUtil.atod((String)v.get(1));
			v = null;
		}
	}
	prop_value = null;
	int r = 0, c = 0;
	GRPolygon shape = null;
	boolean dodraw = true;
	GeoProjection layerProjection = layer.getProjection();
	boolean do_project = GeoProjection.needToProject ( layerProjection, __projection );
	double data_value;	// Data value in grid.
	for ( r = rmin; r <= rmax; r++ ) {
		for ( c = cmin; c <= cmax; c++ ) {
			dodraw = true;
			//Message.printStatus ( 2, "", "cell " + c + " " + r );
			if ( do_project ) {
				shape = (GRPolygon)GeoProjection.projectShape(
					layerProjection, __projection, grid.getCellPolygon ( c, r ), true );
			}
			else {
				// Faster to use raw data...
				shape = grid.getCellPolygon ( c, r );
			}
			// Only draw the shape if part of it is in the drawing
			// area.  This should work if all shape data are
			// pre-projected (which is usually the case).  If
			// printing, since we are not tracking the print extents, just do all...
			if ( !_printing && !__isReferenceGeoview &&
				((shape.xmax < xmin) || (shape.xmin > xmax) ||
				(shape.ymax < ymin) || (shape.ymin > ymax)) ) {
				// No reason to draw because it will not be
				// visible and we don't support panning...
				//Message.printWarning ( 3, "", "not in region" );
				continue;
			}
			// If necessary, set the color based on the classification...
			if ( classification_type == GRSymbol.CLASSIFICATION_SINGLE ) {
				// Set each time because currently the outline color gets reset every time...
				if ( (color == null) || color.isTransparent()) {
					dodraw = false;
				}
			}
			else {
				// Get the data value from the attribute table.
				// Currently this always works on doubles.
				// Need to make more generic so it works on
				// integers and strings.
				try {
					data_value = grid.getDataValue(c,r);
				}
				catch ( Exception e ) {
					continue;
				}
				if ( have__dataLimits && ((data_value < min_to_draw) || (data_value > max_to_draw)) ) {
					continue;
				}
				try {
					color = symbol.getColor ( data_value );
				}
				catch ( Exception e ) {
					color = null;
				}
				if ( (color == null) || color.isTransparent() ){
					Message.printWarning ( 3, "", "null color" );
					continue;
				}
			}
			// Just call the GR method to draw (filled)...
			if ( dodraw ) {
				__grda.setColor ( color );
				GRDrawingAreaUtil.drawShape(__grda, shape,true);
			}
			// Draw with the outline color (not filled)...
			if ( (outline_color != null) && !outline_color.isTransparent() ) {
				__grda.setColor ( outline_color );
				GRDrawingAreaUtil.drawShape(__grda,shape,false);
			}
		}
	}
	__forceRedraw = false;	// Already did it.
}

/**
Draw the shapes in the GeoLayer associated with the GeoLayerView.
@param layerView GeoLayerView to draw in current GeoView.
*/
private void drawLayerView ( GeoLayerView layerView )
{	drawLayerView ( layerView, false );
}

/**
Draw the shapes in the GeoLayer associated with the GeoLayerView.
@param layerView GeoLayerView to draw in current GeoView.
@param selectedOnly Indicates that only shapes that are selected
should be drawn.  Normally this method should be called only by itself, when
a layer with selected shapes is detected.
*/
private void drawLayerView ( GeoLayerView layerView, boolean selectedOnly )
{	String routine = "GeoView.drawLayerView";
	if ( !__isReferenceGeoview && !layerView.isVisible() ) {
		// Don't bother drawing if not visible in the main window...
		return;
	}

	if ( Message.isDebugOn ) {
		if ( selectedOnly ) {
			Message.printStatus ( 2, routine, __prefix + "Drawing layer (only selected shapes)...");
		}
		else {
			Message.printStatus ( 2, routine, __prefix + "Drawing layer (all shapes)...");
		}
	}
	if ( layerView == null ) {
		return;
	}
	GeoLayer layer = layerView.getLayer();
	if ( layer == null ) {
		return;
	}
	// TODO SAM 2010-12-27 Can this be removed?
	boolean drawingWaterDistricts = false;
	if (layerView.getLegend().getText().equals("Water Districts")) {
		drawingWaterDistricts = true;
	}

	// If drawing the big picture, don't care about null legend, etc...

	int shapeType = layer.getShapeType();
	if ( shapeType == GeoLayer.BIG_PICTURE ) {
		drawBigPictureLayerView (layerView);
		__forceRedraw = false;	// Already did it.
		return;
	}

	GRLegend legend = layerView.getLegend();
	if ( legend == null ) {
		Message.printWarning ( 2, routine, "No legend for layer view.  Not drawing." );
		return;
	}
	int nsymbols = legend.size();
	// Loop through the number of symbols for the layer view...
	GRSymbol symbol = null;
	GRColor color = null; // Color used when drawing
	GRColor singleColor = null; // Color for single symbol classification
	GRShape shape = null;
	List<GRShape> shapes = null;
	GRPoint pt = null;
	double symbolMax = 0.0; // When using a scaled symbol, the maximum attribute value.
	PropList layerViewProps = null;
	String labelField;
	String labelFormat;
	String propValue;
	String label = null; // Label for symbols.
	String appType = layer.getAppLayerType();
	DataTable attributeTable = layer.getAttributeTable();
	GeoProjection layerProjection = layer.getProjection();
	double symbolOffsetX = 0.0; // Offsets for multiple symbols.
	double symbolOffsetY = 0.0;
	double symbolSizeX = 0.0;
	double symbolSizeXPrev = 0.0;
	double symbolSizeY = 0.0;
	double [] labelXY = new double[2]; // For calculated label position 

	double missing = layerView.getMissingDoubleValue();
	double replace = layerView.getMissingDoubleReplacementValue();
	double pct = -1;

	int positioning = -1;

	PropList props = null;	// proplist for specifying additional 
				// drawing instructions for symbols -- currently only used by teacups

	for ( int isym = 0; isym < nsymbols; isym++ ) {
		// this is the position that the symbols will be drawn at relative
		// to their X and Y values.  This will be different for teacup symbols, and set below ...
		positioning = GRSymbol.SYM_CENTER_X | GRSymbol.SYM_CENTER_Y;
	
		symbol = legend.getSymbol(isym);	
		if ( symbol == null ) {
			Message.printWarning ( 2, routine, "No symbol for layer view.  Not drawing." );
			return;
		}
	
		// Print the limits for printing...
	
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Drawing limits: " + __grda.getDrawingLimits() );
			Message.printDebug ( 1, routine, __prefix + "Data limits: " + __grda.getDataLimits() );
			Message.printDebug ( 1, routine, __prefix + "Plotting limits: "
				+ __grda.getPlotLimits(GRDrawingArea.COORD_PLOT) );
		}
	
		// Set the symbol, color, etc., based on the layer view settings...
	
		int classification_type = symbol.getClassificationType();
		color = null;
		singleColor = symbol.getColor();
		int symbolStyle = symbol.getStyle();
		symbolSizeXPrev = symbolSizeX;
		symbolSizeX = symbol.getSizeX();
		symbolSizeY = symbol.getSizeY();
		double symbolData[] = null;
		
		if (classification_type != GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
			symbolData = new double[1]; // Used with scaled symbols.
		}
		else {
			symbolData = new double[4];
		}
		
		if ( (shapeType == GeoLayer.LINE) || (shapeType == GeoLayer.POLYLINE_ZM) ) {
			// Set the line width, style, etc....
			// TODO SAM 2010-12-30 This results in very wide lines
			//__grda.setLineWidth ( symbolSizeX );
		}
		else if ( shapeType == GeoLayer.POLYGON ) {
			// Set the fill pattern...
		}
		// Else, Set the symbol below when drawing...
	
		layerProjection = layer.getProjection();
		boolean doProject = GeoProjection.needToProject ( layerProjection, __projection );
	
		// Get the list of shapes for the layer.
	
		shapes = layer.getShapes();
		if ( shapes == null ) {
			return;
		}
		// Now loop through the shapes and draw according to the GeoLayerView's settings...
	
		int nshapes = shapes.size();
		if ( Message.isDebugOn ) {
			Message.printStatus ( 2, routine, "Layer has " + nshapes + " shapes." );
		}
		// Determine how labels for the GeoLayerView are to be generated.  This
		// information is used when calling getShapeLabel for each shape below...
	
		layerViewProps = layerView.getPropList ();
		int labelSource = __LABEL_NODE;
		int fieldNumbers[] = null;
		// Only print this for non-grids since grid data are currently assumed
		// to have one value per grid...
		if ( (shapeType != GeoLayer.GRID) && (attributeTable == null) ) {	
			Message.printStatus ( 2, "", "Attribute table is null" );
		}
		int classification_field = -1;
	
		if (classification_type != GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
			if ( !symbol.getClassificationField().equals("") ) {
				try {	
					classification_field = attributeTable.getFieldIndex( symbol.getClassificationField() );
				}
				catch ( Exception e ) {
					// Just won't label below.
					Message.printWarning ( 2, routine, "Classification field \"" +
						symbol.getClassificationField() + " not found in attribute table." );
						classification_field = -1;
				}
			}
		
			if (classification_field > -1 && layerView.isAnimatedField(classification_field)) {
				if (!layerView.isAnimationFieldVisible(classification_field)) {
					continue;
				}	    
			}
		}
	
		if ( layerViewProps != null ) {
			// Determine which attribute fields are used for labels and symbol classification...
			propValue = layerViewProps.getValue ("Label");
			if ( propValue != null ) {
				// There is a label property so figure out how labels are to be determined.
				if ( propValue.equalsIgnoreCase("UsingGeoViewListener") ) {
					labelSource = __LABEL_USING_GEOVIEW_LISTENER;
				}
				else if ( propValue.equalsIgnoreCase("UsingAttributeTable") ) {
					labelSource = __LABEL_USING_ATTRIBUTE_TABLE;
					attributeTable = layer.getAttributeTable();
					if ( attributeTable != null ) {
						labelField = layerView.getLabelField();
						try {
							fieldNumbers = new int[1];
							fieldNumbers[0] = attributeTable.getFieldIndex(labelField );
						}
						catch ( Exception e ) {
							// Just won't label below.
							labelSource = __LABEL_NODE;
						}
					}
				}
				// Else no labels.
			}
		}
	
		// Newer is to get properties directly out of the GRSymbol.  Use these
		// to override the old values...
	
		labelField = symbol.getLabelField();
		labelFormat = null;
		propValue = null;
		if ( (labelField != null) && !labelField.equals("") ) {
			// Have a label field.  Parse by comma and determine each of the field indices...
			labelSource = __LABEL_USING_ATTRIBUTE_TABLE;
			labelFormat = symbol.getLabelFormat();
			propValue = labelFormat;	// Use this to check for null
							// below because the original
							// label_format will be getting added to
			// Get the individual label fields...
			List<String> v = StringUtil.breakStringList ( labelField, ",", 0 );
			int vsize = 0;
			if ( v != null ) {
				vsize = v.size();
			}
			if ( vsize != 0 ) {
				fieldNumbers = new int[vsize];
				attributeTable = layer.getAttributeTable();
				if ( (propValue == null) || propValue.equals("") ) {
					labelFormat = "";
				}
				// The following loop figures out the field indices in
				// the attribute table and also formats a label format if one was not specified...
				for ( int iv = 0; iv < vsize; iv++ ) {
					try {
						fieldNumbers[iv] = attributeTable.getFieldIndex( v.get(iv).trim() );
					}
					catch ( Exception e ) {
						// This should not happen!
						Message.printWarning ( 3, routine,
						"Can't get table field index for \"" + v.get(iv).trim() + "\" (" + e + ").");
						fieldNumbers[iv] = -1;
					}
					if ( fieldNumbers[iv] < 0 ) {
						// This should not happen!
						Message.printWarning ( 3, routine,
							"Can't get table field index for \"" + v.get(iv).trim() + "\"");
					}
					//Message.printStatus(2, routine, "Label field for \"" + v.get(iv) + "\" is " +
					//		fieldNumbers[iv] );
					if ( (propValue == null) || propValue.equals("") ) {
						// Need to append to the default format
						if ( iv != 0 ) {
							labelFormat += ",";
						}
						labelFormat += attributeTable.getFieldFormat(fieldNumbers[iv]);
					}
				}
			}
			//Message.printStatus ( 2, routine, "for labeling, label format is \"" + label_format + "\"" );
		}
	
		boolean fill = false;	// Is the (polygon) shape filled?
		int transparency = 0;	// Transparency factor (255=transparent, 0 = opaque).
		boolean isTransparent = false;	// Faster to draw when not.
		if ( shapeType == GeoLayer.GRID ) {
			drawGridLayerView ( layerView );
			// For now return and don't do selection, etc...
			return;
		}
		//JGUIUtil.setWaitCursor(__parent, true);
		if ( shapeType == GeoLayer.POLYGON ) {
			fill = true;
			transparency = symbol.getTransparency();
			if ( transparency != 0 ) {
				isTransparent = true;
			}
		}
	
		double	xmin = __dataLimits.getMinX(),	// Use local copy of data limits
			xmax = __dataLimits.getMaxX(),	// to increase performance
			ymin = __dataLimits.getMinY(),
			ymax = __dataLimits.getMaxY();
		label = null;
		Message.printStatus ( 2, routine, __prefix + "Drawing layer \"" + layer.getFileName() + "\" type \""+
			appType + "\" with layer limits " + layer.getLimits() + " labelField=\"" + labelField +
			"\" labelFormat=\"" + labelFormat + "\"");
		boolean drawLayer = true;
					// Indicates if a layer should be drawn.  The
					// only time it should not is if it does not
					// have a drawable color.
		boolean labelSelectedOnly = symbol.labelSelectedOnly();
					// Indicates whether only selected features should be labeled.
	
		// Default the select color to the global color...
		GRColor selectSolor = __selectColor;
		// If the layer view also has a select color specified, use it (this is
		// normally set from within software - e.g., a dynamic layer)...
		String propVal = layerView.getPropList().getValue("SelectColor");
		if ( (propVal == null) && (__project != null) && (__project.getPropList() != null) ) {
			// Get the select color from the project property.  This is
			// normally set in the GeoView project.
			propVal = layerView.getPropList().getValue("Number");
			if ( propVal != null ) {
				propVal = __project.getPropList().getValue (
					"GeoLayerView " + StringUtil.atoi(propVal) + ".SelectColor" );
			}
			if ( propVal == null ) {
				// Try getting the color from the global select color.
				propVal = __project.getPropList().getValue ( "GeoView.SelectColor" );
			}
		}
		if ( propVal != null ) {
			try {
				selectSolor = GRColor.parseColor ( propVal );
			}
			catch ( Exception e ) {
				// Default to global...
				selectSolor = __selectColor;
			}
		}
	
		if ( classification_type == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
			// Get the maximum value for the symbol, which is used to scale the symbol...
			symbolMax = ((GRScaledClassificationSymbol)symbol).getClassificationDataDisplayMax();
			// For now assume that only the X is being offset to prevent
			// the symbols from overlapping...
			if ( isym != 0 ) {
				symbolOffsetX += symbolSizeXPrev + 2;
			}
		}
		else if (classification_type == GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
			symbolMax = ((GRScaledTeacupSymbol)symbol).getMaxCapacity();
		}
	
		// Draw the shapes (and possibly labels)...
	
		for ( int ishape = 0; ishape < nshapes; ishape++ ) {
			props = null;
			
			shape = shapes.get(ishape);
			//Message.printStatus(2, routine, "Drawing shape " + ishape + " " + shape );
			if ( shape == null ) {
				// Null shape...
				//Message.printStatus ( 1, "", "SAM: null shape" );
				continue;
			}
			if ( !shape.is_visible ) {
				// Don't need to draw...
				//Message.printStatus ( 1, "", "SAM: Shape not visible" );
				continue;
			}
			if ( selectedOnly && !shape.is_selected ) {
				// We are only drawing selected shapes and this one is not selected...
				//Message.printStatus ( 1, "", "SAM: Shape not selected");
				continue;
			}
			// See if we need to do a projection.  Unlike grids, need
			// to leave the original data alone.  This results in more
			// memory and processing being used.  It is therefore desirable
			// to use data sources that are in the original projection, if available...
			if ( doProject ) {
				shape = GeoProjection.projectShape( layerProjection, __projection, shape, false );
			}
			//Message.printStatus ( 1, "", "SAM: Shape limits: " +
			//	shape.xmin + "," + shape.ymin + " " + shape.xmax + "," + shape.ymax );
			// Only draw the shape if part of it is in the drawing area.
			// This should work if all shape data are pre-projected (which
			// is usually the case).  If printing, since we are not tracking
			// the print extents, just do all...
			if ( !_printing && !__isReferenceGeoview &&
				((shape.xmax < xmin) || (shape.xmin > xmax) ||
				(shape.ymax < ymin) || (shape.ymin > ymax)) ) {
				// No reason to draw because it will not be
				// visible and we don't support panning...
				//Message.printStatus ( 1, "", "SAM: Not drawing - outside limits" );
				continue;
			}
			// If necessary, set the color based on the classification...
			// If only drawing selected shapes, then all the shapes that
			// make it this far will be selected...
			if ( (selectedOnly) && (color == null) ) {
				// Only need to set the color once...
				color = selectSolor;
				__grda.setColor ( color );
			}
			else if ( classification_type==GRSymbol.CLASSIFICATION_SINGLE ){
				// Only need to set the color once unless it is a
				// layer that has selections, in which case the color
				// needs to be checked for each item.
				if ( color == null ) {
					color = singleColor;
					if ( (color != null) && !color.isTransparent()){
						// Have a color to draw with...
						__grda.setColor ( color );
					}
					else {
						//Message.printStatus ( 1, "", "SAM: Not drawing - no color" );
						drawLayer = false;
					}
				}
			}
			else if ( classification_type == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
				// Special symbols...
				if ( symbolStyle == GRSymbol.SYM_VBARSIGNED ) {
					// For now only handle numeric data and handle
					// the conversion from Object to double using strings...
					try {
						if ( symbolMax == 0.0 ) {
							symbolData[0] = 0.0;
						}
						else {	
							int fieldN=classification_field;
							symbolData[0] = StringUtil.atod (
							   layer.getShapeAttributeValue(shape.index,fieldN).toString());
						
							if (symbolData[0] == missing) {
								symbolData[0]= replace;
							}
						
							symbolData[0] /= symbolMax;
						}
						// All symbols have two colors set in them.  For bars, getColor() returns
						// the color for positive values.
						// getColor2() returns the color for negative values.  
						if ( symbolData[0] >= 0 ) {
							color = symbol.getColor();
						}
						else {
							color = symbol.getColor2();
						}
						__grda.setColor ( color );
					}
					catch ( Exception e ) {
						Message.printWarning ( 3, routine, e);
					}
				}
				else if (symbolStyle == GRSymbol.SYM_VBARUNSIGNED) {
					try {	
						if (symbolMax == 0.0) {
							symbolData[0] = 0.0;
						}
						else {	
							int fieldN = classification_field;
							symbolData[0] = StringUtil.atod (
								layer.getShapeAttributeValue(shape.index, fieldN).toString());
							if (symbolData[0] == missing) {
								symbolData[0]= replace;
							}
						
							symbolData[0] /= symbolMax;
						}
	
						// All symbols have two colors set in them.  For bars, getColor() returns
						// the color for positive values.
						// getColor2() returns the color for negative values.  
						// For unsigned bars, only the positive color matters.
						__grda.setColor ( symbol.getColor() );
					}
					catch ( Exception e ) {
						Message.printWarning ( 3, routine, e);
					}
				}			
			}			
			else if ( classification_type == GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL ) {
				// For now only handle numeric data and handle
				// the conversion from Object to double using strings...
				symbolSizeX = symbol.getSizeX();
				symbolSizeY = symbol.getSizeY();			
				try {	
					GRScaledTeacupSymbol teacup = (GRScaledTeacupSymbol)symbol;
	
					// fill the symbol_data array with the 
					// following values, which MUST be in the specified order:
					// 0 - the maximum capacity of the teacup
					// 1 - the minimum capacity of the teacup
					// 2 - the current capacity of the teacup
					symbolData[0] = StringUtil.atod( layer.getShapeAttributeValue(
						shape.index, teacup.getMaxCapacityField()).toString()); 
					symbolData[1] = StringUtil.atod( layer.getShapeAttributeValue(
						shape.index, teacup.getMinCapacityField()).toString()); 
					symbolData[2] = StringUtil.atod( layer.getShapeAttributeValue(
						shape.index, teacup.getCurrentCapacityField()).toString()); 
	
					// missing data for the current capacity should
					// be replaced with the replacement values.
					// Other missing data is an error in the
					// database and should be caught and fixed there
					if (symbolData[2] == missing) {
						symbolData[2] = replace;
					}
				
					// Scale the size of the teacup appropriately as compared to the largest teacup
					pct = symbolData[0] / symbolMax;
					symbolSizeX *= pct;
					symbolSizeY *= pct;
	
					/*
					Message.printStatus(1, "", "Max: " + symbol_data[0] + "  Symbol_max: " + symbol_max);
					Message.printStatus(1, "", "Size: " + symbol_size_x);
					Message.printStatus(1, "", "Size: " + symbol_size_x);
					*/
	
					// There shouldn't be negative data (teacups measure capacity, after all),
					// so mark in the alternate color if it happens.
	
					if (symbolData[2] >= 0) {
						color = symbol.getColor();
					}
					else {	
						color = symbol.getColor2();
					}
	
					__grda.setColor(color);
	
					// Teacups are positioned so that the center of the bottom of the teacup is on the 
					// point at which they are located.
	
					positioning = GRSymbol.SYM_CENTER_X | GRSymbol.SYM_BOTTOM;
	
					// Set the proplist to not null -- perhaps later this will be used to control
					// other aspects of drawing the teacup, but for now all that goes into 
					// symbol_data[] instead.
	
					props = new PropList("");
				}
				catch (Exception e) {
					Message.printWarning(3, routine, e);
				}
			}			
			else {
				// Get the data value from the attribute table.
				// Currently this always works on doubles.  Need to make
				// more generic so it works on integers and strings.
				try {
					color = symbol.getColor (
						layer.getShapeAttributeValue(shape.index, classification_field ) );
					//Message.printStatus(2, "", "Color: " + color + "  Att: " 
					//	+ layer.getShapeAttributeValue(shape.index, classification_field)
					//	+ "  I: " + shape.index + "  C: " + classification_field);
				}
				catch ( Exception e ) {
					color = null;
				}
				if ( (color == null) || color.isTransparent()){
					//Message.printStatus ( 2, routine, "Not drawing shape - no color" );
					continue;
				}
				__grda.setColor ( color );
			}
			label = null; // Initialize
			if ( (labelSource != __LABEL_NODE) &&
				(!labelSelectedOnly || (labelSelectedOnly && shape.is_selected)) ) {
				// Get the label for the shape...
				label = getShapeLabel ( shape, labelSource, fieldNumbers, labelFormat, layer, attributeTable );
				if ( Message.isDebugOn && (label != null) ) {
					Message.printDebug ( 10, routine, "Retrieved " + appType + " label \"" + label + "\"");
				}
				//Message.printStatus ( 2, routine, "Retrieved " + appType + " label \"" + label + "\"");
				if (label != null) {
					label = label.trim();
				}
			}
			// Just call the GR method to draw...
			if ( (shape.type == GRShape.POINT) || (shape.type == GRShape.POINT_ZM) ) {
				pt = (GRPoint)shape;
				// Need to handle the symbol here since a point does not transparently know...
				if ( label == null ) {
					//Message.printStatus(1, "", "Style: " + symbol_style + "  x: " + pt.x
					//+ " y: " + pt.y + "  sizex: " + symbol_size_x + "  sizey: " + symbol_size_y
					//+ " offx: " + symbol_offset_x + " offy: " + symbol_offset_y + " data: "
					//+ symbol_data + "  Pos: " + positioning);
					// Just the symbol...
					setAntiAlias(__antiAliased);
					GRDrawingAreaUtil.drawSymbol (__grda, 
						symbolStyle, pt.x, pt.y, 
						symbolSizeX, symbolSizeY,
						symbolOffsetX, symbolOffsetY,
						symbolData, GRUnits.DEVICE,
						positioning, props);
					setAntiAlias(false);
				}
				else {
					// Draw the symbol and the text from the indicated field...
	
					// Note that drawSymbolText is passing in
					// a symbol type of -1 -- this means no symbol
					// will be drawn in that call.  This is because
					// this code was NOT drawing scaled 
					// classification symbols properly.  The 
					// symbol is drawn first with a regular 
					// drawSymbol() call, and then the text 
					// is drawn, using drawSymbolText() to space
					// the text over horizontally, but not to 
					// actually drawn any symbols.
					setAntiAlias(__antiAliased);
					GRDrawingAreaUtil.drawSymbol (__grda, 
						symbolStyle, pt.x, pt.y, 
						symbolSizeX, symbolSizeY,
						symbolOffsetX, symbolOffsetY,
						symbolData, GRUnits.DEVICE,
						positioning, props);
					setAntiAlias(false);
	
					try {	
						setAntiAlias(__antiAliased);
						GRDrawingAreaUtil.drawSymbolText(__grda,
						-1, pt.x, pt.y, 
						symbolSizeX, label,
						GRColor.black, 0.0,
						symbol.getLabelPosition(),
	//					GRText.LEFT | GRText.CENTER_Y,
						GRUnits.DEVICE,
						positioning);
						setAntiAlias(false);
					}
					catch ( Exception e ) {
						// Just draw symbol...
						setAntiAlias(__antiAliased);
						GRDrawingAreaUtil.drawSymbol (__grda,
							symbolStyle, pt.x, pt.y,
							symbolSizeX, symbolSizeY,
							symbolOffsetX,symbolOffsetY,
							symbolData, GRUnits.DEVICE,
							positioning,
							props);
						setAntiAlias(false);
					}
				}
			}
			else {
				// Other shape types can be drawn using the current
				// graphics context (line width, color, etc.)...
				if ( drawLayer ) {
					//Message.printStatus(2, routine, "Drawing shape with drawShape()...");
					if ( (label != null) && (label.length() > 0) ) {
						__grda.setColor ( color );
					}
					if ( isTransparent ) {
						GRDrawingAreaUtil.drawShape( __grda, shape, fill, transparency);
					}
					else {	
						GRDrawingAreaUtil.drawShape ( __grda, shape, fill);
					}
					if ( !__isReferenceGeoview && (label != null) && (label.length() > 0) ) {
						double [] labelXY2 = determineLabelXY ( shape, labelXY );
						//Message.printStatus(2, routine, "label coordinates=" + labelXY2[0] + " " + labelXY2[1] +
						//	" label=\"" + label + "\" position=" + positioning );
						if ( labelXY2 != null ) {
							__grda.setColor(GRColor.black);
							GRDrawingAreaUtil.drawText(__grda, label, labelXY2[0], labelXY2[1], 0,
								positioning );
						}
					}
				}
			}
		}
		// If a polygon, draw with the outline color also...
		if ( shapeType == GeoLayer.POLYGON ) {
			color = symbol.getOutlineColor();
			if ( (color != null) && !color.isTransparent() ) {
				__grda.setColor ( color );
				for ( int ishape = 0; ishape < nshapes; ishape++ ) {
					shape = shapes.get(ishape);
					if ( shape == null ) {
						continue;
					}
					// Only draw the shape if part of it is in the
					// drawing area.  This should work if all shape
					// data are pre-projected int he data (which is
					// usually the case).  If printing, since we are
					// not tracking the print extents, just do all...
					if ( !_printing && !__isReferenceGeoview &&
						((shape.xmax < xmin) || (shape.xmin > xmax) ||
						(shape.ymax < ymin) || (shape.ymin > ymax)) ) {
						// No reason to draw because it will not
						// be visible and we don't support panning...
						continue;
					}
					if ( doProject ) {
						shape = GeoProjection.projectShape( layerProjection, __projection, shape, false );
					}
					GRDrawingAreaUtil.drawShape(__grda,shape,false);
				}
			}
		}
		__forceRedraw = false;	// Already did it.
	
		if ( !selectedOnly && (layer.getNumSelected() > 0) ) {
			// Then the method was called the first time and we need to
			// draw the layer again but only with the selected shapes.  This
			// way the selected shapes will be drawn on the top.
			drawLayerView ( layerView, true );
		}
	} // End loop on symbols in the layer view

	//JGUIUtil.setWaitCursor(__parent, false);

	if (drawingWaterDistricts) {
		if (__remindedRepaintersCount > 0) {
			for (int i = 0; i < __remindedRepaintersCount; i++) {
				GeoViewJComponent c = (GeoViewJComponent)__remindedRepainters.get(i);
				c.redraw();
			}
		}
	}
	// Clean up...

	String propVal = layerView.getPropList().getValue("SelectColor");
	if ( (propVal == null) && (__project != null) && (__project.getPropList() != null) ) {
		// Get the select color from the project property.  This is
		// normally set in the GeoView project.
		propVal = layerView.getPropList().getValue("Number");
		if ( propVal != null ) {
			propVal = __project.getPropList().getValue (
				"GeoLayerView " + StringUtil.atoi(propVal) + ".SelectColor" );
		}
		if ( propVal == null ) {
			// Try getting the color from the global select color.
			propVal = __project.getPropList().getValue ( "GeoView.SelectColor" );
		}
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable
{	__dataLimits = null;
	__drawLimits = null;
	__grda = null;
	_image = null;
	__labelFieldList = null;
	__layerViews = null;
	IOUtil.nullArray(__listeners);
	__prefix = null;
	__props = null;
	__maxDataLimits = null;
	__projection = null;
	__selectColor = null;
	__rubberBandColor = null;
	_tmp_record = null;
	__bounds = null;
	__parent = null;
	__geoViewLegend = null;
	__project = null;
	__statusJTextField = null;
	__popup = null;
	__remindedRepainters = null;
	_bounds = null;
	__legendJTree = null;
	__layout = null;
	__legendDataLimits = null;
	__legendDrawLimits = null;

	
	super.finalize();
}

/**
Return the big picture max value.
@return the big picture max value.
*/
public double getBigPictureZMax ()
{	return __bigPictureZMax;
}

/**
Determine whether a Big Picture layer is active.
@return true if the bigpicture is currently active; false otherwise
*/
public boolean getBigPictureActive ()
{	return __bigPictureActive;
}

/**
Return the current data limits that map to the edges of the device.
@return the current data limits that map to the edges of the device.
*/
public GRLimits getDataLimits ( )
{	return __grda.getDataLimits ( );
}

/**
Return the current data limits that map to the edges of the device.
@return the current data limits that map to the edges of the device.
*/
public GRLimits getDataLimitsMax ( )
{	return __maxDataLimits;
}

/**
Return the GRDrawingArea used for drawing.  This allows external code to draw on the drawing area.
@return the GRDrawingArea used for drawing.
*/
public GRJComponentDrawingArea getDrawingArea ()
{	return __grda;
}

/**
Return the number of layer views.
@return the number of layer views (useful for automated selection of colors, symbols, etc.)
*/
public int getNumLayerViews ()
{	return __layerViews.size();
}

/**
Return the list of GeoLayerView used for the display.
@return the GeoLayerView vector that is used for the display.  This list
can be manipulated (reordered, etc.).  Call isVisible() on the layer view to turn off and on.
*/
public List<GeoLayerView> getLayerViews ()
{	return __layerViews;
}

/**
Get a string to use to label a feature.
@return a string to use for labeling the shape, or null if a label cannot be determined.
@param shape Shape to get label for.
@param labelSource Source of label.
@param labelFieldNumbers Attribute table fields to use if needed for label.
@param fieldFormat Attribute table field format to use for label.
@param layer GeoLayer that is being drawn.
@param attributeTable Attribute table for layer.
*/
private String getShapeLabel ( GRShape shape, int labelSource, int labelFieldNumbers[], String fieldFormat,
	GeoLayer layer, DataTable attributeTable )
{	String label = null;
	//Message.printStatus(2, "", "Getting shape label for labelSource=" + labelSource +
	//	" labelFieldNumbers=" + labelFieldNumbers + " fieldformat=\"" + fieldFormat + "\"" );
	/*
	if ( (shape.type != GRShape.POINT) && (shape.type == GRShape.POINT_ZM) ) {
		// Not labeling anything other than points right now.  Fill this out later...
		return null;
	}*/
	// Else, use the label_source to decide how to get label (should check
	// for this case in calling code to increase performance some)...
	if ( labelSource == __LABEL_NODE ) {
		return null;
	}
	else if ( labelSource == __LABEL_USING_ATTRIBUTE_TABLE ) {
		// If field_format is not null, format the label fields using
		// the format.  Else, format the field using the default field format.
		// Get the label from the attribute table...
		try {	
			if ( fieldFormat != null ) {
				__labelFieldList.clear();
				for ( int i = 0; i < labelFieldNumbers.length; i++ ) {
					//Message.printStatus ( 2, "", "Printing \"" + attribute_table.getFieldValue(
					//shape.index, field[i]) + "\"" );
					__labelFieldList.add (attributeTable.getFieldValue(shape.index, labelFieldNumbers[i]) );
				}
				//Message.printStatus ( 2, "", "Formatting using \"" + field_format+"\"");
				label = StringUtil.formatString ( __labelFieldList, fieldFormat );
			}
			else {
				// Need to implement the default format here
				// but the calling code will likely always specify the format...
				for ( int i = 0; i < labelFieldNumbers.length; i++ ) {
					if ( i != 0 ) {
						label += ",";
					}
					// FIXME SAM 2010-12-23 evaluate whether shape.index needs to be used here or
					// is the shape loop index OK.
					label += ("" + attributeTable.getFieldValue(shape.index, labelFieldNumbers[i]));
				}
			}
			return label;
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, "", "Error processing labels for fields (" + e + ")." );
			Message.printWarning ( 3, "", e );
			return null;
		}
	}
	else if ( labelSource == __LABEL_USING_GEOVIEW_LISTENER ) {
		// Call listeners until a non-null String is returned...
		if ( __listeners == null ) {
			return null;
		}
		// Set the shape and layer so that the called method can use...
		_tmp_record._shape = shape;
		_tmp_record._layer = layer;
		int size = 0;
		if ( __listeners != null ) {
			size = __listeners.length;
		}
		for ( int i = 0; i < size; i++ ) {
			label = __listeners[i].geoViewGetLabel ( _tmp_record );
			if ( label != null ) {
				return label;
			}
		}
		return null;
	}
	return null;
}

/**
Handle the label redraw event from another GeoView (likely a ReferenceGeoView).
Do not do anything here because we assume that application code is setting the labels.
@param record Feature being draw.
*/
public String geoViewGetLabel ( GeoRecord record )
{	return null;
}

/**
Handle the info event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devpt Coordinates of mouse in device coordinates (pixels).
@param datapt Coordinates of mouse in data coordinates.
@param selected list of selected GeoRecord.  Currently ignored.
*/
public void geoViewInfo ( GRPoint devpt, GRPoint datapt, List<GeoRecord> selected )
{
}

/**
Handle the info event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devlimits Limits of select in device coordinates (pixels).
@param datalimits Limits of select in data coordinates.
@param selected list of selected GeoRecord.  Currently ignored.
*/
public void geoViewInfo ( GRLimits devlimits, GRLimits datalimits, List<GeoRecord> selected )
{
}

/**
Handle the info event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devshape shape of select in device coordinates (pixels).
@param datashape shape of select in data coordinates.
@param selected list of selected GeoRecord.  Currently ignored.
*/
public void geoViewInfo(GRShape devshape, GRShape datashape, List<GeoRecord> selected) {
}

/**
Handle the mouse motion event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devpt Coordinates of mouse in device coordinates (pixels).
@param datapt Coordinates of mouse in data coordinates.
*/
public void geoViewMouseMotion ( GRPoint devpt, GRPoint datapt )
{
}

/**
Handle the select event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devpt Coordinates of mouse in device coordinates (pixels).
@param datapt Coordinates of mouse in data coordinates.
@param selected list of selected GeoRecord.  Currently ignored.
@param append Indicates whether selections should be appended.
*/
public void geoViewSelect (	GRPoint devpt, GRPoint datapt, List<GeoRecord> selected, boolean append )
{
}

/**
Handle the select event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devlimits Limits of select in device coordinates (pixels).
@param datalimits Limits of select in data coordinates.
@param selected list of selected GeoRecord.  Currently ignored.
@param append Indicates whether selections should be appended.
*/
public void geoViewSelect (	GRLimits devlimits, GRLimits datalimits, List<GeoRecord> selected, boolean append )
{
}

/**
Handle the select event from another GeoView (likely a ReferenceGeoView).
Currently this does nothing.
@param devshape shape of select in device coordinates (pixels).
@param datashape shape of select in data coordinates
@param selected list of selected GeoRecord.  Currently ignored.
@param append Indicates whether selections should be appended.
*/
public void geoViewSelect(GRShape devshape, GRShape datashape, List<GeoRecord> selected, boolean append) {}

/**
Handle the zoom event from another GeoView (likely a reference GeoView).
This resets the data limits for this GeoView to those specified (if not
null) and redraws the GeoView.
@param devlimits Limits of zoom in device coordinates (pixels).  Currently not used.
@param datalimits Limits of zoom in data coordinates.
*/
public void geoViewZoom ( GRLimits devlimits, GRLimits datalimits )
{	setDataLimits ( datalimits );
}

/**
Handle the zoom event from another GeoView (likely a reference GeoView).
This resets the data limits for this GeoView to those specified (if not 
null) and redraws the GeoView.
@param devshape limits of zoom in device coordinates (pixels).  Currently not used.
@param datashape limits of zoom in data coordinates.
*/
public void geoViewZoom(GRShape devshape, GRShape datashape) {}

/**
Return the list of GeoViewAnnotationData to be processed when rendering the map.
*/
protected List<GeoViewAnnotationData> getAnnotationData ()
{
	return __annotationDataList;
}

/**
Returns the current interaction mode.
@return the current interaction mode.
*/
public int getInteractionMode() {
	return __interactionMode;
}

/**
Returns a reference to the popup menu associated with this geo view.
@return a reference to the popup menu associated with this geo view.
*/
public JPopupMenu getPopupMenu() {
	return __popup;
}

/**
Get the projected limits for a layer.  The layer's limits are projected to
that of the GeoView.  This is imperfect because projecting the corners may not
give the full limits.  A more complicated approach may be implemented later.
@param layer Layer to get projected limits.
*/
private GRLimits getProjectedLayerLimits ( GeoLayer layer )
{	GeoProjection layer_projection = layer.getProjection();
	if (!GeoProjection.needToProject(layer_projection, __projection) ) {
		// No need to project (same projection or one is unknown)...
		layer_projection = null;
		return layer.getLimits();
	}
	// Else need to do the projection.  Do so by converting the layer's
	// limit coordinates to latitude and longitude and then back to the
	// layer view's projection...
	GRLimits limits = layer.getLimits();
	//Message.printStatus ( 1, "", "Limits from data =" +limits.toString());
	GRPoint p = new GRPoint();
	p.x = limits.getMinX();
	p.y = limits.getMinY();
	layer_projection.unProject ( p, true );
	__projection.project ( p, true );
	double maxx, maxy, minx, miny;
	minx = maxx = p.x;
	miny = maxy = p.y;
	p.x = limits.getMinX();
	p.y = limits.getMaxY();
	layer_projection.unProject ( p, true );
	__projection.project ( p, true );
	minx = MathUtil.min ( minx, p.x );
	miny = MathUtil.min ( miny, p.y );
	maxx = MathUtil.max ( maxx, p.x );
	maxy = MathUtil.max ( maxy, p.y );
	p.x = limits.getMaxX();
	p.y = limits.getMaxY();
	layer_projection.unProject ( p, true );
	__projection.project ( p, true );
	minx = MathUtil.min ( minx, p.x );
	miny = MathUtil.min ( miny, p.y );
	maxx = MathUtil.max ( maxx, p.x );
	maxy = MathUtil.max ( maxy, p.y );
	p.x = limits.getMaxX();
	p.y = limits.getMinY();
	layer_projection.unProject ( p, true );
	__projection.project ( p, true );
	minx = MathUtil.min ( minx, p.x );
	miny = MathUtil.min ( miny, p.y );
	maxx = MathUtil.max ( maxx, p.x );
	maxy = MathUtil.max ( maxy, p.y );
	layer_projection = null;
	p = null;
	limits = null;
	//Message.printStatus ( 1, "", "Limits after projection =" +
	//(new GRLimits ( minx, miny, maxx, maxy )).toString() );
	return new GRLimits ( minx, miny, maxx, maxy );
}

/**
Get the projection that is used for the GeoView.
@return the projection.
*/
public GeoProjection getProjection ()
{	return __projection;
}

/**
Get the GeoView project that is used for the Geoview.
@return the GeoView project.
*/
public GeoViewProject getProject() {
	return __project;
}

/**
Initialize data.
@param props Properties for GeoView.
*/
private void initialize ( PropList props )
{	__dataLimits = null;
	__drawLimits = new GRLimits ();		// Will get set in paint().
	__grda = null;
	__layerViews = new ArrayList<GeoLayerView>(10);	// Non-null to minimize checks.
	__listeners = null;
	__mousetrackerEnabled = true;
	__selectGeoRecords = true;
	__layout = new GeoViewLegendLayout();
	__layout.setTitle("LEGEND");
	_waiting = false;

	__remindedRepainters = new ArrayList<GeoViewJComponent>();

	// Make sure we have a non-null PropList...

	if ( props == null ) {
		// Make a default...
		__props = new PropList ( "GeoView.defaults" );
	}
	else {
		__props = props;
	}

	// Interpret properties and set flags...

	String prop_value = __props.getValue ( "MouseTracker" );
	if ( prop_value != null ) {
		if ( prop_value.equalsIgnoreCase("false") ) {
			__mousetrackerEnabled = false;
		}
	}
	prop_value = __props.getValue ( "SelectGeoRecords" );
	if ( prop_value != null ) {
		if ( prop_value.equalsIgnoreCase("false") ) {
			__selectGeoRecords = false;
		}
	}
	prop_value = __props.getValue ( "Projection" );
	if ( prop_value != null ) {
		try {
			__projection = GeoProjection.parseProjection ( prop_value );
		}
		catch ( Exception e ) {
			__projection = new UnknownProjection ();
		}
	}

	// Add the listeners...

	addMouseListener ( this );
	addMouseMotionListener ( this );
}

/**
Indicate whether the GeoView is a reference GeoView.
@return true if a reference GeoView, false if not.
*/
public boolean isReference ()
{	return __isReferenceGeoview;
}

/**
Set whether the GeoView is a reference GeoView.
@return true if a reference GeoView, false if not.
@param is_reference true if the GeoView is a reference GeoView.
*/
public boolean isReference ( boolean is_reference )
{	__isReferenceGeoview = is_reference;
	if ( __isReferenceGeoview ) {
		__prefix = "Ref: ";
	}
	else {
		__prefix = "Main: ";
	}
	return __isReferenceGeoview;
}

/**
Handle mouse clicked event.  Don't do anything.  Rely on mousePressed().
*/
public void mouseClicked ( MouseEvent event ) {}

/**
Handle mouse drag event.  If in zoom mode, redraw the rubber-band line.
This method also calls the geoViewMouseMoved() methods for registered GeoViewListeners.
@param event Mouse drag event.
*/
public void mouseDragged ( MouseEvent event )
{	
	if (!__leftMouseButton) {
		return;
	}
	// Data units...

	int x = event.getX();
	int y = event.getY();
	if ( __grda == null ) {
		return;
	}
	GRPoint datapt = __grda.getDataXY ( x, y, GRDrawingArea.COORD_DEVICE );
	if ( __isReferenceGeoview ) {
		if ( !__maxDataLimits.contains(datapt) ) {
			// Mouse not within drawing area so don't track...
			return;
		}
	}
	else {
		if ( !__dataLimits.contains(datapt) ) {
			// Mouse not within drawing area so don't track...
			return;
		}
	}

	// Device units...

	GRPoint devpt = new GRPoint ( x, y );

	int size = __listeners.length;	// Checked for null above...
	for ( int i = 0; i < size; i++ ) {
		__listeners[i].geoViewMouseMotion ( devpt, datapt );
	}

	if ( (__interactionMode == INTERACTION_SELECT) ||
		(__interactionMode == INTERACTION_INFO) || (__interactionMode == INTERACTION_ZOOM) ) {
		// Get the coordinates used
		__mouseX2 = event.getX();
		__mouseY2 = event.getY();
		__rubberBanding = true;
		// Force a redraw...  The __rubberBanding flag will be checked
		// so a full redraw is not done.
		repaint ();
	}
}

/**
Handle mouse enter event.  Currently does not do anything.
*/
public void mouseEntered ( MouseEvent event )
{
}

/**
Handle mouse enter event.  Currently does not do anything.
*/
public void mouseExited ( MouseEvent event )
{
}

/**
Handle mouse motion event.
This method calls the geoViewMouseMoved() methods for registered GeoViewListeners.
*/
public void mouseMoved ( MouseEvent event )
{	if ( (__grda == null) || (__dataLimits == null) ) {
		return;
	}
	if ( !__mousetrackerEnabled ) {
		return;
	}

	if ( __listeners == null ) {
		return;
	}

	// Get the mouse position...

	int x = event.getX();
	int y = event.getY();

	// Data units...

	GRPoint datapt = __grda.getDataXY ( x, y, GRDrawingArea.COORD_DEVICE );
	if ( __isReferenceGeoview ) {
		if ( !__maxDataLimits.contains(datapt) ) {
			// Mouse not within drawing area so don't track...
			return;
		}
	}
	else {
		if ( !__dataLimits.contains(datapt) ) {
			// Mouse not within drawing area so don't track...
			return;
		}
	}

	// Device units...

	GRPoint devpt = new GRPoint ( x, y );

	int size = __listeners.length;	// Checked for null above...
	for ( int i = 0; i < size; i++ ) {
		__listeners[i].geoViewMouseMotion ( devpt, datapt );
	}
}

/**
Handle mouse pressed event.  Start a select or zoom.  The event is completed
when the mouse is released.
*/
public void mousePressed ( MouseEvent event )
{	
	if (event.getButton() != MouseEvent.BUTTON1) {
		__leftMouseButton = false;
		maybeShowPopup(event);
		return;
	}
	
	__leftMouseButton = true;
	__mouseX1 = __mouseY1 = __mouseX2 = __mouseY2 = -1;
	if ( (__interactionMode == INTERACTION_SELECT) ||
		(__interactionMode == INTERACTION_INFO) || (__interactionMode == INTERACTION_ZOOM) ) {
		// Save the point that was selected so that the drag and released events will work...
		__mouseX1 = event.getX();
		__mouseY1 = event.getY();
	}
}

/**
Handle mouse released event.  If in INTERACTION_ZOOM mode, call the
geoViewZoom() method of registered GeoViewListeners.  If in INTERACTION_SELECT
mode, call geoViewSelect().  If in INTERACTION_INFO, call the geoViewInfo()
method of registered GeoViewListeners.  Only return a region if the mouse has
moved at least 5 pixels in both directions.
*/
public void mouseReleased ( MouseEvent event )
{
	if (event.getButton() != MouseEvent.BUTTON1) {
		__leftMouseButton = false;
		maybeShowPopup(event);
		return;
	}
	__leftMouseButton = true;

	__wasWaiting = JGUIUtil.getWaitCursor();
	JGUIUtil.setWaitCursor(__parent, true);
	int x = event.getX();
	int y = event.getY();
	if ( __grda == null ) {
		if (!__wasWaiting) {
			JGUIUtil.setWaitCursor(__parent, false);
		}	
		return;
	}
	if ( __interactionMode == INTERACTION_SELECT ) {
		// Select all the shapes so that the select will reflect the current select action...
		int numlayerviews = 0;
		if ( __layerViews != null ) {
			numlayerviews = __layerViews.size();
		}
		GeoLayerView layer_view = null;
		GeoLayer layer = null;
		for ( int i = 0; i < numlayerviews; i++ ) {
			layer_view = (GeoLayerView)__layerViews.get(i);
			layer = (GeoLayer)layer_view.getLayer();
			// Only deselect if the user has not pressed the Ctrl key...
			if ( !event.isControlDown() ) {
				layer.deselectAllShapes();
			}
		}
	}
	if (	(__interactionMode == INTERACTION_SELECT) ||
		(__interactionMode == INTERACTION_INFO) ||
		(__interactionMode == INTERACTION_ZOOM) ) {
		// Only process if box is 5 pixels or bigger...
		int deltax = x - __mouseX1;
		if ( deltax < 0 ) {
			deltax *= -1;
		}
		int deltay = y - __mouseY1;
		if ( deltay < 0 ) {
			deltay *= -1;
		}
		if ( (deltax <= 5) && (deltay <= 5) ) {
			if ( (__interactionMode == INTERACTION_SELECT) || (__interactionMode == INTERACTION_INFO) ) {
				// Selecting a point.
				// Assume they want the original point...
				GRPoint devpt = new GRPoint ( (double)__mouseX1, (double)__mouseY1 );
				GRPoint datapt = __grda.getDataXY ( __mouseX1, __mouseY1, GRDrawingArea.COORD_DEVICE );
				List<GeoRecord> records = null;
				if ( __listeners != null ) {
					if ( __selectGeoRecords ) {
						records = selectGeoRecords ( datapt, null, __interactionMode, event.isControlDown() );
					}
					int size = __listeners.length;
					if ( __interactionMode == INTERACTION_SELECT ) {
						for ( int i = 0; i < size; i++){
							__listeners[i].geoViewSelect ( devpt, datapt, records, event.isControlDown() );
						}
					}
					else if (__interactionMode == INTERACTION_INFO ) {
						for ( int i = 0; i < size; i++){
							__listeners[i].geoViewInfo ( devpt, datapt, records);
						}
					}
				}
				__rubberBanding = false;
				// Reset zoom coordinates...
				__mouseX2 = -1;
				if ( (x != __mouseX1) && (y != __mouseY1) ) {
					repaint();
				}
				int rsize = 0;
				if ( records != null ) {
					rsize = records.size();
				}
				if ( (__interactionMode==INTERACTION_SELECT) && (rsize > 0) ) {
					// Force a redraw so that selected shapes are highlighted...
					redraw();
				}
				if (!__wasWaiting) {				
					JGUIUtil.setWaitCursor(__parent, false);
				}	
				return;
			}
			else if ( __interactionMode == INTERACTION_ZOOM ) {
				// Too small, don't allow...
				// Reset zoom coordinates...
				__mouseX2 = -1;
				if ( (x != __mouseX1) && (y != __mouseY1) ) {
					//repaint();
					// Need to redraw because a small box
					// will be on the screen.  Just do a
					// full redraw because repaint() results
					// in the map getting lost.
					__rubberBanding = false;
					redraw();
				}
				// Don't need to do anything else...
				if (!__wasWaiting) {				
					JGUIUtil.setWaitCursor(__parent, false);
				}					
				return;
			}
		}
		// If we get to here we are selecting or zooming using a box...
		//
		// Save the point that was selected so that the drag and
		// released events will work...
		// Reset the data limits to those from the zoom box...
		// Make sure that the limits are always specified 
		int xmin, xmax, ymin, ymax;
		xmin = xmax = __mouseX1;
		ymin = ymax = __mouseY1;
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
		GRLimits mouse_limits = new GRLimits ( xmin, ymin, xmax, ymax );
		// Reverse Y so we get the right values in GR...
		GRPoint pt1 = __grda.getDataXY ( xmin, ymax, GRDrawingArea.COORD_DEVICE );
		GRPoint pt2 = __grda.getDataXY ( xmax, ymin, GRDrawingArea.COORD_DEVICE );
		GRLimits newdata_limits = new GRLimits ( pt1, pt2 );
		pt1 = null;
		pt2 = null;
		// Call the listener (or should this happen after the paint?)...
		if ( (__interactionMode == INTERACTION_SELECT) || (__interactionMode == INTERACTION_INFO) ) {
			// Just return the select information..
			List<GeoRecord> records = null;
			if ( __listeners != null ) {
				try {
					if ( __selectGeoRecords ) {
						records = selectGeoRecords (
							newdata_limits, null, __interactionMode, event.isControlDown() );
					}
				}
				catch ( Exception e ) {
					// Ignore for now...
					Message.printWarning ( 3, "GeoView.mouseReleased", "Error searching for select." );
				}
				int size = __listeners.length;
				for ( int i = 0; i < size; i++ ) {
					if ( __interactionMode == INTERACTION_SELECT ) {
						__listeners[i].geoViewSelect (
						mouse_limits, newdata_limits, records, event.isControlDown());
					}
					else {
						__listeners[i].geoViewInfo ( mouse_limits, newdata_limits, records );
					}
				}
			}
			int rsize = 0;
			if ( records != null ) {
				rsize = records.size();
			}
			if ( (__interactionMode == INTERACTION_SELECT) && (rsize > 0) ) {
				// Force a redraw so that selected shapes are highlighted...
				__forceRedraw = true;
				__checkWaitStatus = true;
			}
			records = null;
		}
		else if ( __interactionMode == INTERACTION_ZOOM ) {
			// The new data limits from the user are passed, even
			// though the drawing limits are recomputed below...
			if ( __listeners != null ) {
				for ( int i = 0; i < __listeners.length; i++ ) {
					__listeners[i].geoViewZoom ( mouse_limits, newdata_limits );
				}
			}
			// Repaint...
			// Fill in the background color.  Need this because
			// update() does not do (because of zooming)...
			clearView();
			// Before redrawing, set the data limits to the
			// plotting limits that result in the full device being
			// used.  Otherwise, mouse tracking, etc. may not
			// allow selects from outside the actual data limits...
			// First set so the plotting limits will be recomputed...
			if ( !__isReferenceGeoview ) {
				__grda.setDataLimits ( newdata_limits );
			}

			// Now get the data limits that correspond to the plot limits...
			GRPoint plot1 = __grda.getDataXY ( 0, 0, GRDrawingArea.COORD_PLOT );
			GRPoint plot2 = __grda.getDataXY ( __bounds.width,
				__bounds.height, GRDrawingArea.COORD_PLOT );
			// Now reset the data limits for the full device...
			if ( !__isReferenceGeoview ) {
				__dataLimits = new GRLimits ( plot1, plot2 );
				__grda.setDataLimits ( __dataLimits );
			}
			else {
				// Since the reference map is not resizing, need
				// to use the new limits from above.  At some
				// point, perhaps have a link to the main
				// GeoView to get an exact box, based on aspect for the window.
				__dataLimits = new GRLimits ( newdata_limits );
			}
			__forceRedraw = true;
			__checkWaitStatus = true;
		}
		__rubberBanding = false;
		// Reset zoom coordinates...
		__mouseX2 = -1;
		repaint();
	}
	if (!__wasWaiting && !__checkWaitStatus) {
		JGUIUtil.setWaitCursor(__parent, false);
	}		
}

private void maybeShowPopup(MouseEvent e) {
	if (__popup != null && __popup.isPopupTrigger(e)) {
		__popup.show(e.getComponent(), e.getX(), e.getY());
	}
}

/**
Update the image on the GeoView.  If __forceRedraw is true the JComponent will
be cleared and then drawn.
@param g Graphics instance either from the JComponent event handling or from an
explicit printView() call.
*/
// public void paintComponent ( Graphics g ) {}

public void paint(Graphics g) {
	if (!__redrawReference) {
		__redrawReference = true;
		return;
	}
	String routine = "GeoView.paint";
	boolean wait = JGUIUtil.getWaitCursor();
	int dl = 10;
	boolean resizing = false;
	__bigPictureActive = false;	// set to true in drawBigPictureLayer

	if (!__inPrinting) {
		_doubleBuffered = true;
	}
	
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, __prefix + "Painting GeoView..." );
	}

	if ( _waiting ) {
		return;
	}
	GRLimits new_drawLimits = null;

	if ( __rubberBanding ) {
		// Just need to redraw the rubber-band line to the on-screen image...
		// Figure out the coordinates...
	
		if (_doubleBuffered && _buffer != null) {
			g.drawImage(_buffer, 0, 0, this);
		}
		
		int xmin, xmax, ymin, ymax;
		g.setColor ( __rubberBandColor );
		g.setXORMode ( getBackground() );
		// Erase the last line...
		/*
		if ( __mouseXPrev != -1 ) {
			xmin = xmax = __mouseX1;
			ymin = ymax = __mouseY1;
			if ( __mouseXPrev < xmin ) {
				xmin = __mouseXPrev;
			}
			if ( __mouseYPrev < ymin ) {
				ymin = __mouseYPrev;
			}
			if ( __mouseXPrev > xmax ) {
				xmax = __mouseXPrev;
			}
			if ( __mouseYPrev > ymax ) {
				ymax = __mouseYPrev;
			}
			g.drawRect ( xmin, ymin, (xmax - xmin), (ymax - ymin) );
		}
		// Now draw the new line...
		*/
		if ( __mouseX2 != -1 ) {
			xmin = xmax = __mouseX1;
			ymin = ymax = __mouseY1;
			if ( __mouseX2 < xmin ) {
				xmin = __mouseX2;
			}
			if ( __mouseY2 < ymin ) {
				ymin = __mouseY2;
			}
			if ( __mouseX2 > xmax ) {
				xmax = __mouseX2;
			}
			if ( __mouseY2 > ymax ) {
				ymax = __mouseY2;
			}
			g.drawRect ( xmin, ymin, (xmax - xmin), (ymax - ymin) );
		}
		// Done drawing...
		// Reset to normal paint mode...
		g.setPaintMode ();
		return;
	}
	else {
	}

	// See if the graphics is for printing or screen.  Set the base class _printing flag accordingly...

	if (__inPrinting) {
		// Set in the printView method
		// Bounds will have been set in printView...
		new_drawLimits = new GRLimits(); // Gets the page size...
		new_drawLimits.setLeftX ( _bounds.x );
		new_drawLimits.setBottomY ( _bounds.y );
		new_drawLimits.setRightX ( _bounds.width + _bounds.x );
		new_drawLimits.setTopY ( _bounds.height + _bounds.y);

		// set the limits for drawing to the printed page --
		// the old limits will be reset after the print() call is done
		setLimits(new_drawLimits);

		/* *********************
		OLD CODE, pre 2003-08-11
		// Set in the printView method
		// Bounds will have been set in printView...
		new_drawLimits = new GRLimits(); // Gets the page size...
		int print_border = 20;
		new_drawLimits.setLeftX ( __BORDER + print_border );
		new_drawLimits.setBottomY ( __BORDER + print_border );
		new_drawLimits.setRightX ( __bounds.width - __BORDER - (2*print_border));
		new_drawLimits.setTopY ( __bounds.height - __BORDER - (2*print_border));
		********************* */

		// Base class...		
		_graphics = (Graphics2D)g;
		_printing = true;		
	}
	else {
		// Screen graphics...
		// This handles the GR size...
		new_drawLimits = getLimits(true);
		// Need the following for other code (image new)...
		__bounds = getBounds();
		// Now set the drawing limits to the bounds minus a border...
		new_drawLimits.setLeftX ( __BORDER );
		new_drawLimits.setBottomY ( __BORDER );
		new_drawLimits.setRightX ( new_drawLimits.getRightX() - __BORDER );
		new_drawLimits.setTopY ( new_drawLimits.getTopY() - __BORDER);
		_graphics = (Graphics2D)g;	
		// Base class...		
		_printing = false;
	}

	// See if the limits need to be reset for drawing.
	// Comparison is based on limits after accounting for border.
	// If the size has changed and double-buffering, create a new
	// image for the off-screen buffer...

	if ( !__drawLimits.equals(new_drawLimits) ) {
		//||
		//( ((_image == null) && _doubleBuffered) && !_printing) ) {
		// Set to the new drawing limits for the redraw.  This will
		// cause a recompute of the data used for scaling.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Device size has changed." );
		}
		__drawLimits = new GRLimits ( new_drawLimits );
		resizing = true;
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Setting drawing limits to: " + __drawLimits );
		}
		// If double buffering, create a new image...
		if ( _doubleBuffered && !_printing ) {
			setupDoubleBuffer(0, 0, __bounds.width,__bounds.height);
		}
	}
	else {
		// JComponent size has not changed...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Device size has not changed." );
		}
		resizing = false;
	}
	// if ( _doubleBuffered && !_printing && (_image == null) ) {
	if ( _doubleBuffered && !_printing && (_buffer == null) ) {
		// Safety check in case the events somehow show the same size
		// but the image is not created...
		//_image = createImage ( __bounds.width, __bounds.height );
		setupDoubleBuffer(0, 0, __bounds.width, __bounds.height);
	}
	if ( _doubleBuffered && !_printing) {
		// Use the image graphics...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Using image graphics." );
		}
		//_graphics = _image.getGraphics();
		_graphics = (Graphics2D)(_buffer.getGraphics());
	}

	if ( (__forceRedraw || resizing) && (__grda != null) ) {
		// Resize the drawing area by setting its drawing limits...
		__grda.setDrawingLimits ( __drawLimits, GRUnits.DEVICE, GRLimits.DEVICE );
		clearView ();
	}
	if ( resizing ) {
		// Reset the data limits.  This call will make sure that in the
		// event of a resize that the data limits are reset to the
		// full extent of the visible window.  Otherwise, shapes that
		// are outside the previous limits may not get drawn...
		setDataLimits ( __dataLimits );
	}

	// Loop through the GeoLayerViews and redraw the shapes from each if
	// any of the following conditions apply:
	//
	// * not double buffering (redraw every time)
	// * a draw is forced (because of changes in the views)
	// * printing has been requested (and need to redraw given page extents, etc.)
	// * double-buffering is on and a resize has occurred.

	boolean clearCursor = false;

	if ( __forceRedraw || _printing || !_doubleBuffered || (_doubleBuffered && resizing) ) {
		int size = __layerViews.size();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Drawing " + size + " GeoLayerViews..." );
		}
		if ( __parent != null ) {
//			JGUIUtil.setWaitCursor ( __parent, true );
		}
		int i = -1;
		// Process all the layers.
		try {	
			for ( GeoLayerView layerView: __layerViews ) {
				++i;
				if ( layerView != null ) {
					drawLayerView ( layerView );
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "Error drawing layer " + i );
			Message.printWarning ( 3, routine, e );
		}
		if ( !__isReferenceGeoview ) {
			// Draw annotations on the top
			try {
				Message.printStatus(2,routine,"Drawing " + getAnnotationData().size() + " annotations.");
				for ( GeoViewAnnotationData annotationData: getAnnotationData() ) {
					GeoViewAnnotationRenderer annotationRenderer = annotationData.getGeoViewAnnotationRenderer();
					annotationRenderer.renderGeoViewAnnotation(this, annotationData.getObject(),
						annotationData.getLabel() );
				}
			}
			catch ( Exception e ) {
				Message.printWarning ( 3, routine, "Error drawing annotations (" + e + ")." );
				Message.printWarning ( 3, routine, e );
			}
		}
		
		// If a reference GeoView, draw the current zoom...
		// Might have null if a blank map.

		if ( __isReferenceGeoview && (__grda != null) && (__dataLimits != null) ) {
			GRDrawingAreaUtil.setColor ( __grda, GRColor.red );
			GRDrawingAreaUtil.drawRectangle ( __grda, __dataLimits.getLeftX(), __dataLimits.getBottomY(),
				__dataLimits.getWidth(), __dataLimits.getHeight() );
		}
	
		if ( __geoViewLegend != null ) {
			__geoViewLegend.paint(g);
		}

		/* for whatever reason, not working
		if ( _printing && (__grda != null) ) {
			GRLimits datalim = new GRLimits ( __drawLimits );
			GR.setColor ( __grda, GRColor.black );
			GR.drawRectangle ( __grda, datalim.getLeftX()+1,
			datalim.getBottomY()+1, datalim.getWidth()-2, 
			datalim.getHeight()-2);
		}
		*/

		if ( __parent != null ) {
			//JGUIUtil.setWaitCursor ( __parent, false );
		}
		if (__checkWaitStatus) {
			clearCursor = true;
		}
	}

	drawLegend();	

	// If double buffering, copy the image from the buffer to the JComponent...

	if ( _doubleBuffered && !_printing ) {
		// The graphics is for the display...
		// Draw to the screen...
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Copying internal image to display." );
		}
		//g.drawImage ( _image, 0, 0,  this );
		g.drawImage(_buffer, 0, 0, this);
		// Only do this if double buffering to screen because that is
		// the only time the graphics is created locally...
		// ?? _graphics.dispose();
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, __prefix + "...done painting GeoView." );
	}

	// This code prevents the map from redrawing at a different zoom level after printing.
	if (__inPrinting) {
		// This handles the GR size...
		new_drawLimits = getLimits(true);
		// Need the following for other code (image new)...
		__bounds = getBounds();
		// Now set the drawing limits to the bounds minus a border...
		new_drawLimits.setLeftX ( __BORDER );
		new_drawLimits.setBottomY ( __BORDER );
		new_drawLimits.setRightX ( new_drawLimits.getRightX() - __BORDER );
		new_drawLimits.setTopY ( new_drawLimits.getTopY() - __BORDER);
		_graphics = (Graphics2D)g;	
		// Base class...		
		_printing = false;
		__drawLimits = new GRLimits ( new_drawLimits );		
	}

	if (clearCursor) {
		__checkWaitStatus = false;
		if (!__wasWaiting) {
			JGUIUtil.setWaitCursor(__parent, false);
		}
		else {
			JGUIUtil.setWaitCursor(__parent, true);
		}
	}
	else {
		if (!wait) {
			JGUIUtil.setWaitCursor(__parent, false);
		}
	}
}

/**
Prints the map.
*/
public void print() {
	try {
		PageFormat pageFormat = PrintUtil.getPageFormat("letter");
		PrintUtil.setPageFormatOrientation(pageFormat,PageFormat.LANDSCAPE);
		PrintUtil.setPageFormatMargins(pageFormat, .5, .5, .5, .5);
		PrintUtil.print(this, pageFormat);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

/**
Prints the map on paper.
@param g the Grahpics object on which to render the page.
@param pageFormat the PageFormat to use to know how to format the page's margins
@param pageIndex the number of the page to be printed.
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
	if (pageIndex > 0) {
		return NO_SUCH_PAGE;
	}
	else {
		// next bit of code ensures that a page is only sent to the printer once
		if (__lastPage == -1) {
			// this happens the first time print(...) is called.
			__lastPage = 0;
			return PAGE_EXISTS;
		}
		else {
			// this happens the second time print(...) is called.
			__lastPage = -1;
		}

		_bounds = new Rectangle( (int)pageFormat.getImageableX(), (int)pageFormat.getHeight(),
			(int)(pageFormat.getImageableWidth()), (int)(pageFormat.getImageableHeight()));

		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(0, pageFormat.getImageableY());
		
		// Set for the GRDevice because we will temporarily use that to do the drawing...
		__inPrinting = true;
		
		StopWatch sw = new StopWatch();
		sw.start();
		boolean buffering = disableDoubleBuffering(this);	
		boolean hold = _doubleBuffered;
		_doubleBuffered = false;
		paint(g);
		restoreDoubleBuffering(this, buffering);
		_doubleBuffered = hold;
		sw.stop();
//		Message.printStatus(1, "", "Printing took " + sw.getSeconds() + " seconds. ");
		//setLimits(oldLimits);
		__inPrinting = false;
		return PAGE_EXISTS;
	}
}

private void restoreDoubleBuffering(Component c, boolean wasBuffered) {
	if (c instanceof JComponent) {
		((JComponent)c).setDoubleBuffered(wasBuffered); }
}


/**
Print the view.
*/
public void printView ()
throws IOException {
	print();
}

/**
Redraw the GeoView.  Forces a paint with a redraw.
*/
public void redraw () {
	redraw(true);
}

/**
Redraws the GeoView.
@param redrawReference whether to redraw the reference view.  If false, 
the reference graph will not be redrawn.
*/
public void redraw(boolean redrawReference) {
	__redrawReference = redrawReference;
	__forceRedraw = true;
	repaint();
}

/**
Remove a GeoViewListener.  The matching object address is removed, even if
it was regestered multiple times.
@param listener GeoViewListener to remove.
*/
public void removeGeoViewListener ( GeoViewListener listener )
{	if ( listener == null ) {
		return;
	}
	if ( __listeners != null ) {
		// Loop through and set to null any listeners that match the
		// requested listener...
		int size = __listeners.length;
		int count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( (__listeners[i] != null) && (__listeners[i] == listener) ) {
				__listeners[i] = null;
			}
			else {
				++count;
			}
		}
		// Now resize the listener array...
		GeoViewListener [] newlisteners = new GeoViewListener[count];
		count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( __listeners[i] != null ) {
				newlisteners[count++] = __listeners[i];
			}
		}
		__listeners = newlisteners;
	}
}

/**
Remove a GeoLayerView from the GeoView.  This includes a GeoLayer and specific
view features (legend, etc.).  The limits will be recomputed to be the maximum of the layers.
@param layer_view GeoLayerView to remove.
@param re_draw Indicates if view should be refreshed.
*/
public void removeLayerView ( GeoLayerView layer_view, boolean re_draw )
{	removeLayerView ( layer_view, re_draw, true );
}

/**
Remove a GeoLayerView to the GeoView.  This includes a GeoLayer and specific
view features (legend, etc.).
@param layer_view GeoLayerView to remove.
@param re_draw Indicates if view should be refreshed.
@param reset_limits true if the overall limits should be reset.
*/
public void removeLayerView ( GeoLayerView layer_view, boolean re_draw, boolean reset_limits )
{	if ( layer_view == null ) {
		return;
	}
	// Rely on the binary reference value to locate the layer_view...

	__layerViews.remove ( layer_view );

	// Do most of the following so we know the maximum limits...

/* SAMX Decide on this later...
	// Need to update the limits.  For now use the last one set...
	GeoLayer layer = layer_view.getLayer();
	// Seems like this does not get done in paint in the right
	// order?
	GRLimits new_drawLimits = getLimits(true);	// Gets the canvas size.
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, __prefix +"Drawing limits from canvas are: " + new_drawLimits);
	}
	new_drawLimits.setLeftX ( __BORDER );
	new_drawLimits.setBottomY ( __BORDER );
	new_drawLimits.setRightX ( new_drawLimits.getRightX() - __BORDER );
	new_drawLimits.setTopY ( new_drawLimits.getTopY() - __BORDER );
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		__prefix + "Drawing limits after border for device are: " + new_drawLimits );
	}
	// Comparison is based on after border...
	//if ( !__drawLimits.equals(new_drawLimits) ) {
		// Set to the new drawing limits for the redraw...
		__drawLimits = new GRLimits ( new_drawLimits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, routine, __prefix + "Drawing limits after reset: " + __drawLimits );
		}
	//}
	new_drawLimits = null;
	// Reset the data limits...
	if ( layer != null ) {
		if ( __dataLimits == null ) {
			__dataLimits = new GRLimits(getProjectedLayerLimits(layer ));
		}
		else {
		    if ( reset_limits ) {
				__dataLimits = __dataLimits.max ( getProjectedLayerLimits(layer) );
			}
		}
		// Save the maximum regardless of reset...
		__maxDataLimits = new GRLimits ( __dataLimits.max(getProjectedLayerLimits(layer) ) );
	}
	if ( __grda == null ) {
		// Have not had data to draw but do now...
		// Set up one drawing area on the view...
		__grda = new GRCanvasDrawingArea ( this, "GeoView", GRAspect.TRUE,
			__drawLimits, GRUnits.DEVICE, GRLimits.DEVICE, __dataLimits );
	}
	else {
		// Now can set the data limits...
		__grda.setDataLimits ( __dataLimits );
	}
	layer = null;
*/
	// Now repaint the canvas since we have removed data...

	if ( re_draw ) {
		__forceRedraw = true;
		repaint();
	}
}

/**
Determine the shapes and associated layers for a select point or region.
Only one of the arguments should be non-null and will indicate which type of
search is done.  Before the search, all layers and shapes are set to not have
any selected shapes so that the resulting selections are only for the current operation.
Polygons will be searched by checking the extents when comparing against a
region, or the centroid when comparing a point.
Only visible shapes within visible layers are searched.
@return GeoRecords for the specified point or region.
@param selectShape Shape to do selection of shapes.  The shape may be a
GRPoint, a GRLimits (rectangular select region), GRArc (circular select
region), or GRLocatorArc (arc with crosshairs). 
@param appLayerTypes If not null, indicate the application layer types that
should be processed (e.g., "Station", "Reservoir").
@param interaction_mode Pass in the interaction mode to be used during the
selection.  Passing the argument rather than taking directly from the geoview
allows selects to be made programatically, regardless of the current GeoView interaction mode.
@param append For the shapes that are selected by the data point or limits:
if append is true, then the shape will be selected if it is not already
selected.  If it is already selected, it will be de-selected (e.g., user has
previously selected and is now deselecting).
*/
public List<GeoRecord> selectGeoRecords ( GRShape selectShape, List<String> appLayerTypes,
	int interaction_mode, boolean append )
{	List<GeoRecord> records = new ArrayList<GeoRecord>(10);
	GeoLayer layer = null;
	List<GRShape> shapes = null;
	int nshapes = 0;
	double delta, deltamin = -1.0, deltax, deltay;
	GeoRecord record = new GeoRecord();	// Fill below...
	GRShape shape = null;
	GRPoint pt = null;
	int j = 0;
	double x = 0.0, y = 0.0;
	if ( (selectShape.type == GRShape.POINT) || (selectShape.type == GRShape.POINT_ZM) ) {
		// The search region is actually a point.  Therefore, we need
		// to select only the feature that is nearest the point.
		// Consequently, the entire layer may be searched if points are being displayed.
		GRPoint datapt = (GRPoint)selectShape;
//		Message.printStatus ( 2, "", "Selecting features at " + datapt.x + "," + datapt.y );
		showStatus ( "Selecting features at " + datapt.x + "," + datapt.y );
		// Search for shape nearest the point for all the layers...
		if ( __layerViews == null ) {
			return records;
		}
		// Loop through layer views...
		for ( GeoLayerView layerView: __layerViews ) {
			//Message.printStatus ( 1, "", "SAM: Searching layer view [" + i + "]" );
			if ( layerView == null ) {
				continue;
			}
			layer = layerView.getLayer();
			if ( layer == null ) {
				continue;
			}
			// Only search if the layer view:
			//
			// * matches the requestd App layer types
			// * is visible
			String appLayerType = "";
			if ( appLayerTypes != null ) {
				int asize = appLayerTypes.size();
				for ( int ia = 0; ia < asize; ia++ ) {
					appLayerType = appLayerTypes.get(ia);
					if ( !appLayerType.equalsIgnoreCase(layer.getAppLayerType()) ) {
						// Requested app layer type does not match.
						//Message.printStatus ( 1, "", "SAM: Given app type does not match");
						continue;
					}
					else {
						// It does match...
						break;
					}
				}
			}
			if ( (appLayerType != null) && appLayerType.equalsIgnoreCase(layer.getAppLayerType()) ) {
				// OK to process.
				//Message.printStatus ( 1, "", "SAM: Given app type does match" );
			}
			else {
				// Other checks to perform...
				if ( !layerView.isVisible() ) {
					//Message.printStatus ( 1, "", "SAM: Layer view is not visible" );
					continue;
				}
				if ( !layerView.isSelected() ) {
					//Message.printStatus ( 1, "", "SAM: Layer view is not selected" );
					continue;
				}
			}
			// Loop through shapes in layer views and find the
			// nearest shape.  Need to update this so that
			// shapes that surround the point are selected.
			// However, until this is done correctly, just find the nearest centroid...
			shapes = layer.getShapes ();
			if ( shapes == null ) {
				continue;
			}
			nshapes = shapes.size();
			//Message.printStatus ( 1, "", "SAM: searching " + nshapes + " shapes" );
			for ( j = 0; j < nshapes; j++ ) {
				shape = shapes.get(j);
				if ( !shape.is_visible ) {
					// Don't search shapes unless visible.
					continue;
				}
				if ( (shape.type == GRShape.POINT) || (shape.type == GRShape.POINT_ZM) ) {
					// Use the point
					pt = (GRPoint)shape;
					x = pt.x;
					y = pt.y;
				}
				else {
					// Use the average of the limits...
					x = (shape.xmin + shape.xmax)/2.0;
					y = (shape.ymin + shape.ymax)/2.0;
				}
				// Compute the distance between the point and the selected data point.  Right
				// now we just support finding the closest one.  Use -1 as the initializer since no
				// distance can be negative.  To save on some processing, just square the distance
				// components but don't take the square root.
				deltax = datapt.x - x;
				deltay = datapt.y - y;
				delta = deltax*deltax + deltay*deltay;
				if ( (deltamin < 0.0) || ((deltamin >= 0.0) && (delta < deltamin)) ) {
					// Re-use the record's data.  This will need to change some if selecting with
					// a point returns more than one record.
					deltamin = delta;
					record.setShape ( shape );
					record.setLayer ( layer );
					record.setLayerView ( layerView );
					record.setTableRecord ( layer.getTableRecord((int)shape.index));
				}
			}
		}
		if ( deltamin >= 0.0 ) {
			// Have a record so add to the Vector...
			records.add ( record );
			if ( interaction_mode == INTERACTION_SELECT ) {
				shape = record.getShape();
				layer = record.getLayer();
				//Message.printStatus ( 1, "", "SAM: append is " + append );
				if ( !append ) {
					// Always select...
					// Only increment count if not already selected	
					if ( !shape.is_selected ) {
						//Message.printStatus ( 1, "", "SAM: Shape not already selected");
						layer.setNumSelected ( layer.getNumSelected() + 1 );
						//Message.printStatus ( 1, "", "SAM: Count now " + layer.getNumSelected() );
					}
					shape.is_selected = true;
				}
				else {
					// Appending so reverse selection...
					if ( !shape.is_selected ) {
						shape.is_selected = true;
						layer.setNumSelected ( layer.getNumSelected() + 1 );
					}
					else {
						// Already selected...
						shape.is_selected = false;
						layer.setNumSelected ( layer.getNumSelected() - 1 );
					}
				}
				//Message.printStatus ( 1, "", "SAM: Shape.is_selected = " + shape.is_selected );
			}
		}
	}
	else {
		// The search region is a shape and therefore any shape that is
		// totally or partially in the search region should be returned.
		// Search for shapes in the limits...
		//Message.printStatus ( 1, "", "Getting shapes in " + datalimits.toString() );

		Message.printStatus ( 2, "", "Selecting features using region." );
		showStatus ( "Selecting features using region." );
		if ( __layerViews == null ) {
			return records;
		}
		// Loop through layer views...
		for ( GeoLayerView layerView: __layerViews ) {		
			//Message.printStatus ( 1, "", "Searching layer view " + i + " for shapes" );
			if ( layerView == null ) {
				continue;
			}
			layer = layerView.getLayer();
			if ( layer == null ) {
				continue;
			}
			// If the app layer type is a match, do the search
			// regardless if the layer is visible (should this be the case?).
			String appLayerType = "";
			if ( appLayerTypes != null ) {
				int asize = appLayerTypes.size();
				for ( int ia = 0; ia < asize; ia++ ) {
					appLayerType = appLayerTypes.get(ia);
					if ( !appLayerType.equalsIgnoreCase(
						layer.getAppLayerType()) ) {
						// Requested app layer type does not match.
						//Message.printStatus ( 1, "", "SAM: Given app type does not match");
						continue;
					}
					else {
						// It does match...
						break;
					}
				}
			}
			if ( (appLayerType != null) && appLayerType.equalsIgnoreCase(layer.getAppLayerType()) ) {
				// OK to process.
			}
			else {
				if ( !layerView.isVisible() ) {
					continue;
				}
				if ( !layerView.isSelected() ) {
					continue;
				}
			}
			// Loop through shapes in layer views...
			shapes = layer.getShapes ();
			if ( shapes == null ) {
				continue;
			}
			nshapes = shapes.size();
			for ( j = 0; j < nshapes; j++ ) {
				shape = shapes.get(j);
				// Use the shape limits and return shapes that 
				// intersect (but may not be totally within the
				// region).  If the shape is a point, the flag should not matter...
				if ( !selectShape.contains ( shape, false ) ){
					continue;
				}
				// TODO SAM 2006-03-02 The call to getTableRecord() is not
				// implemented for DbaseDataTable for on the fly
				// reads and will throw an exception here.  A
				// null will be returned.  Other code may need
				// to go through the attribute table via GeoLayer to get to specific data values.
				records.add (
					new GeoRecord( shape, layer.getTableRecord( (int)shape.index), layer, layerView ) );
				if ( interaction_mode != INTERACTION_SELECT ) {
					// No need to do anything else
					continue;
				}
				// Now select the shapes if in selection mode...
				if ( !append ) {
					// Always select...
					// Only increment count if not selected	
					if (!shape.is_selected){
						layer.setNumSelected ( layer.getNumSelected() + 1 );
					}
					shape.is_selected = true;
				}
				else {
					// Reverse selection...
					if ( !shape.is_selected ) {
						shape.is_selected = true;
						layer.setNumSelected ( layer.getNumSelected() + 1 );
					}
					else {
						shape.is_selected = false;
						layer.setNumSelected ( layer.getNumSelected() - 1 );
					}
				}
				//Message.printStatus ( 2, "", "SAM: Shape.is_selected = " + shape.is_selected );
			}
		}
	}
	Message.printStatus ( 2, "", "Found " + records.size() + " features." );
	showStatus ( "Ready" );
	return records;
}

/**
Sets the popup menu to be associated with this GeoViewJComponent.  The 
actionPerformed events are handled elsewhere.
@param popup the popup to use.
*/
public void setPopupMenu(JPopupMenu popup) {
	__popup = popup;
}

/**
Indicate the status by setting the GeoViewPanel status TextField text.
@param status Status text to display.
*/
private void showStatus ( String status )
{	if ( (__statusJTextField != null) && (status != null) ) {
		__statusJTextField.setText ( status );
	}
}

/**
Select (and redraw in the select color) the shapes corresponding to the objects
passed in.  The search is done based on memory address for the objects, not the contents of the objects.
@param objects Objects to search for in shapes used by all layers.
@param selectedLayersOnly Indicates that only shapes in the currently selected
layers should be searched (currently not active).
*/
public void selectShapesUsingObjects ( List<? extends Object> objects, boolean selectedLayersOnly )
{	String routine = "selectShapesUsingObjects";
	if ( objects == null ) {
		return;
	}

	// Loop through all the shapes in all the layers.  Assume that the
	// number of objects is going to be small compared to the number of
	// total shapes so loop through the layers first...

	if ( __layerViews == null ) {
		return;
	}
	int size = __layerViews.size();
	if ( size == 0 ) {
		return;
	}
	for ( GeoLayerView layerView: __layerViews ) {
		if ( layerView == null ) {
			continue;
		}
		// Loop through each shape...
		List<GRShape> shapes = layerView.getLayer().getShapes();
		if ( shapes == null ) {
			return;
		}
		for ( GRShape shape : shapes ) {
			if ( shape == null ) {
				// Null shape...
				continue;
			}
			// No reason to compare if shape object is null...
			if ( shape.associated_object == null ) {
				continue;
			}
			// Now loop through the objects...
			for ( Object object: objects ) {
				if ( object == null ) {
					continue;
				}
				if ( object.equals(shape.associated_object) ) {
					// Have a match.  Need to draw the shape in the select color.  Need
					// to put in a drawShape method here but will that kill performance
					// if we use in drawLayerView also??
					// For now, print a message so we can see that it works.
					Message.printStatus ( 2, routine, "Found shape for object." );
				}
			}
		}
	}
}

/**
Reset the data limits and force a redraw.  For example call when a zoom event
occurs and geoViewZoom is called.  If the data limits are a point, a buffer of
5% of the maximum data limits is added around the point.
@param datalim Data limits.
*/
public void setDataLimits ( GRLimits datalim )
{	if ( datalim != null ) {
		// Make sure that a zoom is not to a point in a direction (zero
		// width or height).  If zero, increase the dimension by 5%...
		double width = 0.0, height = 0.0;
		// Do this before setting so the increase is based on the
		// original data limits (so vertical is not double-resized)...
		if ( datalim.getLeftX() == datalim.getRightX() ) {
			width = __dataLimits.getWidth()*.05;
		}
		if ( datalim.getBottomY() == datalim.getTopY() ) {
			height = __dataLimits.getHeight()*.05;
		}
		datalim.increase ( width, height );	
		__dataLimits = new GRLimits ( datalim );
		if ( !__isReferenceGeoview ) {
			__grda.setDataLimits ( datalim );
			// Now reset the data limits to those of the full device
			// so that they match the displayed extents.  Otherwise,
			// some of the optimization checks that are used to
			// increase performance won't work.
			__dataLimits = GRDrawingAreaUtil.getDataExtents(__grda, __drawLimits,0);
		}
			
		__forceRedraw = true;
		repaint();
	}

//	Message.printStatus(1, "", "SetDataLimits datalim   : " + datalim);
//	Message.printStatus(1, "", "SetDataLimits datalimits: " + __dataLimits);
}

/**
Set the interaction mode, currently either INTERACTION_SELECT,
INTERACTION_ZOOM, INTERACTION_INFO, or INTERACTION_NONE.
@param mode Interaction mode.
*/
public void setInteractionMode ( int mode )
{	if ( (mode == INTERACTION_NONE) || (mode == INTERACTION_SELECT) ||
		(mode == INTERACTION_INFO) || (mode == INTERACTION_ZOOM) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 1, "GeoView.setInteractionMode", "Set interaction mode to " + mode );
		}
		__interactionMode = mode;
	}
}

/**
Set the legend location, when a GeoViewLegend has been provided.
*/
public void setLegendLocation ( int corner )
{	if ( corner == GeoViewLegend.NONE ) {
		__geoViewLegend = null;
		__forceRedraw = true;
		repaint();
		return;
	}

	if ( __geoViewLegend == null ) {
		// The GeoViewLegend is currently only used in the StateMod
		// GUI and will be reworked when that GUI is fully updated to
		// use the new map interface.  Set the limit defaults to the
		// values that have been used in the StateMod GUI...
		__geoViewLegend = new GeoViewLegend ( this );
		__geoViewLegend.setMinWidth ( 120 );
		__geoViewLegend.setMaxWidth ( 200 );
		__geoViewLegend.setMinHeight ( 95 );
		__geoViewLegend.setMaxHeight ( 200 );

	}
	__geoViewLegend.setLocation ( corner );
	__forceRedraw = true;
	repaint();
}

/**
Set the GeoView project used for the GeoView.
@param project GeoView project to use.
*/
public void setProject ( GeoViewProject project )
{	__project = project;
}

/**
Set the projection used for the GeoView.
@param projection GeoProjection to use.
*/
public void setProjection ( GeoProjection projection )
{	__projection = projection;
}

/**
Set the TextField to use for status messages.  Only major status messages are printed.
*/
public void setStatusTextField ( JTextField status_JTextField )
{	__statusJTextField = status_JTextField;
}

/**
Set whether drawing should wait.  If true, calling paint() will have no
effect.  Calling redraw() will cause a redraw (but must still call
setWait(false) before doing so).  This is useful when a lot of data are read
in up front and waiting to display improves performance.
@param waiting True if drawing should wait.
*/
public void setWait ( boolean waiting )
{	_waiting = waiting;
}

/**
Turns off the wait cursor after repainting.
@param value the value to set the wait cursor to after painting
*/
public void setWaitCursorAfterRepaint(boolean value) {
	__checkWaitStatus = true;
	__wasWaiting = value;
}

/**
Overrule the default update to not clear the screen.
@param g Graphics instance to use for update.
*/
public void update(Graphics g)
{	// Just call paint...
	paint ( g );
}

/**
Zoom out to the full extent of the data.
*/
public void zoomOut ()
{	if ( __maxDataLimits != null ) {
		// Set the maximum so the plot limits recalculate...
		__grda.setDataLimits ( __maxDataLimits );
		// Now get the data limits that correspond to the plot limits...
		GRPoint plot1 = __grda.getDataXY ( 0, 0, GRDrawingArea.COORD_PLOT );
		//__bounds = getBounds();
		GRPoint plot2 = __grda.getDataXY ( __bounds.width, __bounds.height, GRDrawingArea.COORD_PLOT );
		// Now reset the data limits for the full device...
		__dataLimits = new GRLimits ( plot1, plot2 );
		plot1 = null;
		plot2 = null;
		if ( !__isReferenceGeoview ) {
			__grda.setDataLimits ( __dataLimits );
		}
		__forceRedraw = true;
		repaint();
		// Call the listeners so that they can zoom all the way out also...
		if ( __listeners != null ) {
			int size = __listeners.length;
			for ( int i = 0; i < size; i++ ) {
				__listeners[i].geoViewZoom (__grda.getPlotLimits(
					GRDrawingArea.COORD_DEVICE), __dataLimits );
			}
		}
	}
}

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
// 
// TESTING AREA FOR LEGEND DRAWING CODE (2004-10-13 - ?)
// JTS will merge with main code once development has finished

/**
Static array used for when teacup symbols are drawn in the legend -- used to 
avoid creating lots of arrays.
*/
private static double[] __teacupData = new double[3];

/**
Static proplist used when drawing teacups in the legend -- used because it
must be provided to the draw method, but does not need created each time.
*/
private static PropList __teacupProps = new PropList("");

/**
Whether to draw the legend or not.
*/
private boolean __drawLegend = false;

/**
The legend JTree (in the left-hand panel of the GeoView panel) that is used to build the legend.
*/
private GeoViewLegendJTree __legendJTree = null;

/**
The layout object that controls information on how the layout is drawn.
*/
private GeoViewLegendLayout __layout;

/**
Data limits used internally in the legend drawing methods.
*/
private GRLimits 
	__legendDataLimits,
	__legendDrawLimits;

public double convertX(double d, boolean isDistance) {
	double w = __dataLimits.getRightX() - __dataLimits.getLeftX();
	w = w / __drawLimits.getWidth();

	// w is the __dataLimits height per pixel
	if (isDistance) {
		return (d * w);
	}
	else {
		return ((d * w) + __dataLimits.getLeftX());
	}
}

public double convertY(double d, boolean isDistance) {
	double h = __dataLimits.getTopY() - __dataLimits.getBottomY();
	h = h / __drawLimits.getHeight();

	// h is the __dataLimits height per pixel
	if (isDistance) {
		return (d * h);
	}
	else {
		return ((d * h) + __dataLimits.getBottomY());
	}
}

/**
Converts X from drawing units to data units for the legend.
@param d the double drawing X value to convert.
@param isDistance whether or not the X value being converted is a distance 
(ie, a height) or an absolute point (ie, the lower-left point of a rectangle).
Distances are converted and then returned.  Non-distances are converted and 
then added to the left-most data value before being returned.
@return the X value in data units.
*/
public double convertLegendX(double d, boolean isDistance) {
	double w = __legendDataLimits.getRightX() - __legendDataLimits.getLeftX();
	w = w / __legendDrawLimits.getWidth();

	// w is the __legendDataLimits height per pixel
	if (isDistance) {
		return (d * w);
	}
	else {
		return ((d * w) + __legendDataLimits.getLeftX());
	}
}

/**
Converts Y from drawing units to data units for the legend.
@param d the double drawing Y value to convert.
@param isDistance whether or not the Y value being converted is a distance 
(ie, a height) or an absolute point (ie, the lower-left point of a rectangle).
Distances are converted and then returned.  Non-distances are converted and 
then added to the bottom-most data value before being returned.
@return the Y value in data units.
*/
public double convertLegendY(double d, boolean isDistance) {
	double h = __legendDataLimits.getTopY() - __legendDataLimits.getBottomY();
	h = h / __legendDrawLimits.getHeight();

	// h is the __legendDataLimits height per pixel
	if (isDistance) {
		return (d * h);
	}
	else {
		return ((d * h) + __legendDataLimits.getBottomY());
	}
}

/**
Draws the legend.
*/
public void drawLegend() {
	// this will happen when the GUI is first opened.
	if (__grda == null) {
		return;
	}
	
	// the legend should only be drawn if desired
	if (!__drawLegend) {
		return;
	}

	// the legendJTree MUST be set in order for this method to work (see setLegendJTree()).
	if (__legendJTree == null) { 
		return;
	}

	// if ( printing or saving to a file ...) {
	//	return;
	//}

	if (!__inPrinting) {		
		// if not printing then set the data units to be the drawable screen area
		GRPoint plot1 = __grda.getDataXY(0, 0,GRDrawingArea.COORD_PLOT);
		__bounds = getBounds();
		GRPoint plot2 = __grda.getDataXY(__bounds.width, __bounds.height, GRDrawingArea.COORD_PLOT);	
		__legendDataLimits = new GRLimits(plot1, plot2);
	}
	else {
		// if printing, use the data limits set up for printing
		__legendDataLimits = new GRLimits(__dataLimits);
	}

	__legendDrawLimits = __grda.getDrawingLimits();
	
	// draw a black box around the entire map area
	GRDrawingAreaUtil.setColor(__grda, GRColor.black);
	// width is set to 2 so that the line will be visible inside the visible area
	GRDrawingAreaUtil.setLineWidth(__grda, 2);
	GRDrawingAreaUtil.drawRectangle(__grda, 
		__legendDataLimits.getLeftX(), __legendDataLimits.getBottomY(),
		__legendDataLimits.getWidth(), __legendDataLimits.getHeight());
	GRDrawingAreaUtil.setLineWidth(__grda, 1);

	List<SimpleJTree_Node> nodes = __legendJTree.getAllNodes();
	int size = nodes.size();

	// keeps track of the nodes that can be skipped in doing the 
	// computation and drawing of the network -- they are skipped because
	// they were turned off in the legend layout
	boolean[] skippedNodes = new boolean[size];
	// the offset that all the text in the legend will sit at, in pixels
	// from the left side of the legend box
	double textOffset = 0;
	GeoLayerViewLegendJComponent symbolCanvas = null;
	GRLimits limits = null;
	// the number of lines of text in the legend
	int textLines = 0;
	SimpleJTree_Node node = null;

	// first get the size of the largest symbol so the offset for text can be determined
	for (int i = 0; i < size; i++) {
		node = nodes.get(i);
		if (__layout.findNode(node) > -1) {
			// if the node is found in the layout, check to see
			// whether it should be skipped or not.
			skippedNodes[i] = !__layout.isNodeLegendVisible(node);
		}
		else {
			// by default, nodes that are not in the layout are
			// shown (that is, they are NOT skipped).  This is 
			// because in a brand new layout that hasn't been set
			// up there will be no nodes, and all nodes' values should be shown
			skippedNodes[i] = false;
		}

		if (node instanceof GeoViewLegendJTree_Node) {
			// label node -- holds no symbols.  Just count it as a line of text
			textLines++;
		}
		else {
			// node that holds a symbol.  Get the limits of its
			// symbol, for calculating how far to the right the legend text will be aligned
			symbolCanvas = (GeoLayerViewLegendJComponent)node.getData();
			limits = symbolCanvas.getLimits();
			if (limits.getWidth() > textOffset) {
				textOffset = limits.getWidth();
			}
		}
	}

	// calculate the total height and width of the legend.
	// First calculate the width of the widest text and how tall the legend is going to be

	// whether, once a row is skipped because skippedNodes[i] == true, 
	// to keep skipping rows that come after it.  Only the initial node
	// of a row and its children will be skipped
	boolean keepSkipping = false;
	// the running height of the legend area
	double height = 0;
	// the maximum height of a row in the legend (based on the symbol
	// and font size -- the higher of the two)
	double maxRowHeight = 0;
	// the running width of the legend area
	double width = 0;
	GeoViewLegendJTree_Node legendNode = null;
	// a counter used to track which line of text is currently being calculated for
	int textLine = 0;
	// a String array into which will be placed all the lines of text to appear in the legend
	String[] legendLines = new String[textLines + 1];
	// a list that holds all the heights of the rows, used to know how
	// to finally draw the legend
	List<Double> rowHeightsV = new ArrayList<Double>();

	for (int i = 0; i < size; i++) {
		node = nodes.get(i);
		if (node instanceof GeoViewLegendJTree_Node) {
			keepSkipping = false;
			if (skippedNodes[i]) {
				// if this node is to be skipped, then just
				// continue in the loop.  All of this node's
				// child nodes will be skipped as well, because of this
				keepSkipping = true;
				continue;
			}
			
			// a text line has been found, increment the counter.
			// The counter starts at 0 and is incremented to 1 by
			// the first node -- this means that it takes into 
			// account the legend title line
			textLine++;

			// by this point, maxRowHeight has alreayd been 
			// calculated for the previous label node and its
			// children, or is 0.  Increase the legend height by its value
			height += maxRowHeight;

			// also store the height of this legend line
			rowHeightsV.add(new Double(maxRowHeight));

			// determine the width of the line -- the legend
			// must accommodate the widest text that will appear in it
			legendNode = (GeoViewLegendJTree_Node)node;
			limits = GRDrawingAreaUtil.getTextExtents(__grda, legendNode.getFieldText(), GRUnits.DEVICE);
			maxRowHeight = limits.getHeight();
			if (limits.getWidth() > width) {	
				width = limits.getWidth();
			}

			// store the line of text for easy retrieval when actually drawing it
			legendLines[textLine] = legendNode.getFieldText();	
		}
		else {
			if (keepSkipping) {
				// if the parent node of this child node has
				// been skipped, this node needs skipped too
				continue;
			}

			// determine how much height will need added to
			// the legend to accomodate the symbol
			symbolCanvas = (GeoLayerViewLegendJComponent)node.getData();
			limits = symbolCanvas.getLimits();
			if (limits.getHeight() > maxRowHeight) {
				maxRowHeight = limits.getHeight();
			}

			// if getLegendText() is not null then that means 
			// special text was put into the canvas that must 
			// appear instead of the normal label text.  Normally,
			// this will be for instance the maximum value of a
			// signed bar.  Use this text instead to calculate 
			// row height and width for text.
			if (symbolCanvas.getLegendText() != null) {
				limits = GRDrawingAreaUtil.getTextExtents(
					__grda, symbolCanvas.getLegendText(), GRUnits.DEVICE);
				legendLines[textLine] = symbolCanvas.getLegendText();
				if (limits.getHeight() > maxRowHeight) {
					maxRowHeight = limits.getHeight();
				}
				if (limits.getWidth() > width) {	
					width = limits.getWidth();
				}						
			}
		}
	}

	// by this point, maxRowHeight has already been 
	// calculated for the last label node and its
	// children, or is 0.  Increase the legend height by ts value
	height += maxRowHeight;

	// create a double array that stores the heights of all the rows
	// in the legend, where row 0 is the legend title.
	rowHeightsV.add(new Double(maxRowHeight));
	int rowHeightsVSize = rowHeightsV.size();
	double[] rowHeights = new double[rowHeightsVSize];
	for (int i = 0; i < rowHeightsVSize; i++) {
		rowHeights[i] = rowHeightsV.get(i).doubleValue();
	}

	// now add spaces.  Spaces go between every line, between the symbols
	// and the text, and on every border
	// TODO (JTS - 2004-10-18) maybe make this definable later in the legend layout setup panel
	double BORDER_T = 2;	// space between top border and legend title
	double BORDER_B = 2;	// space between bottom border and last line
	double BORDER_L = 4;	// space between left border and symbols
	double BORDER_R = 2; 	// space between longest line and right border
	double SYMBOL_SPACE = 10;	// space between symbols and text
	double LINE_SPACE = 5;	// space between each line
	double LEGEND_EXTRA_SPACE = 5;	// extra spacing put below the legend
					// title line to space away from any
					// errant symbols

	// get the Legend title -- if none has been set up the default is
	// "LEGEND" -- and determine how much height will need added to the
	// legend to accommodate it.  Also see if its width means the width 
	// of the legend will need adjusted
	String LEGEND_TITLE = __layout.getTitle();
	limits = GRDrawingAreaUtil.getTextExtents(__grda, LEGEND_TITLE,
		GRUnits.DEVICE);
	height += limits.getHeight() + LEGEND_EXTRA_SPACE;
	rowHeights[0] = limits.getHeight() + LEGEND_EXTRA_SPACE;
	if (limits.getWidth() > width) {
		width = limits.getWidth();
	}

	// add extra spaces to the width to account for the distance between
	// symbols and text and also between the left and right edges of the legend box
	width += BORDER_L + BORDER_R + textOffset + SYMBOL_SPACE;
	// add extra spaces to the height to accomodate spaces between the top
	// and bottom edges of the box, and also for spaces between all of the lines of text
	height += BORDER_T + BORDER_B + (rowHeightsVSize * LINE_SPACE);

	// used in convertLegendX() and convertLegendY() to easily refer to 
	// "distances" that are being calculated (see convertLegendX() and convertLegendY())
	boolean D = true;
	// used in convertLegendX() and convertLegendY() to easily refer to 
	// "points" that are being calculated (see convertLegendX() and convertLegendY())
	boolean P = false;

	// the left-most point of the legend box.  Defaults to be in the
	// upper-left of the map area.
	double LEGEND_LX = 10;
	// the bottom-most point of the legend box.  Defaults to be in the
	// upper-left of the map area.
	double LEGEND_BY = __legendDrawLimits.getHeight() - height - 10;

	// based on the actual position defined in the layout, change the
	// lower-left point of the legend so that it will be drawn in the correct corner.
	switch (__layout.getPosition()) {
		case GeoViewLegendLayout.NORTHWEST:	
			break;
		case GeoViewLegendLayout.NORTHEAST:
			LEGEND_LX = __legendDrawLimits.getWidth() - width - 10;
			break;
		case GeoViewLegendLayout.SOUTHEAST:
			LEGEND_LX = __legendDrawLimits.getWidth() - width - 10;
			LEGEND_BY = 10;
			break;		
		case GeoViewLegendLayout.SOUTHWEST:
			LEGEND_BY = 10;
			break;
	}
	
	// blank out the area behind the legend with white at first
	GRDrawingAreaUtil.setColor(__grda, GRColor.white);
	GRDrawingAreaUtil.fillRectangle(__grda, convertLegendX(LEGEND_LX, P), 
		convertLegendY(LEGEND_BY, P), convertLegendX(width, D), 
		convertLegendY(height, D));

	// draw the border of the legend
	GRDrawingAreaUtil.setColor(__grda, GRColor.black);
	GRDrawingAreaUtil.drawRectangle(__grda, convertLegendX(LEGEND_LX, P), 
		convertLegendY(LEGEND_BY, P), convertLegendX(width, D), convertLegendY(height, D));		

	// calculate the top-most point from which drawing the legend 
	// should begin
	double topY = LEGEND_BY + height - BORDER_T;

	// get the limits of the legend title so to draw it in the legend
	limits = GRDrawingAreaUtil.getTextExtents(__grda, LEGEND_TITLE, GRUnits.DEVICE);
	GRDrawingAreaUtil.drawText(__grda, LEGEND_TITLE, 
		convertLegendX(LEGEND_LX + BORDER_L, P), 
		convertLegendY(topY - limits.getHeight(), P), 0, GRText.BOTTOM | GRText.LEFT);

	// counter used to know where the current line should be drawn at
	double currentHeight = rowHeights[0];
	// counter of the current text line being drawn.
	int count = 0;

	for (int i = 0; i < size; i++) {
		node = (SimpleJTree_Node)nodes.get(i);
		if (node instanceof GeoViewLegendJTree_Node) {
			keepSkipping = false;
			if (skippedNodes[i]) {
				// if this node should be skipped, then continue
				// and make sure that all its child nodes are skipped, too
				keepSkipping = true;
				continue;
			}

			// calculate the new height at which to draw the current line of text
			currentHeight += rowHeights[count++];
			legendNode = (GeoViewLegendJTree_Node)node;

			GRDrawingAreaUtil.setColor(__grda, GRColor.black);
			GRDrawingAreaUtil.drawText(__grda, legendLines[count],
				convertLegendX(LEGEND_LX + BORDER_L + textOffset + SYMBOL_SPACE, P), 
				convertLegendY(topY - currentHeight - (count * LINE_SPACE), P),
				0, GRText.BOTTOM | GRText.LEFT);
		}
		else {
			if (keepSkipping) {
				// if the parent was skipped, so is this
				continue;
			}
			symbolCanvas = (GeoLayerViewLegendJComponent)node.getData();
			drawLegendSymbol(symbolCanvas, LEGEND_LX + BORDER_L,
				topY - currentHeight - (count * LINE_SPACE), rowHeights[count]);
		}
	}
}

/**
Draws legend symbols on the map display in the legend.  This code was borrowed
wholesale from the GeoLayerViewLegendJComponent drawSymbol() code, as there
doesn't appear to be a nice way to share the code.
@param com the GeoLayerViewLegendJComponent whose symbol needs to be drawn in the legend.
@param gx the GeoView X value (in DRAWING units) of the lower-left point of the symbol.
@param gy the GeoView Y value (in DRAWING units) of the lower-left point of the symbol.
@param rowHeight the height of the current row in which the symbol is being
drawn (calculated in drawLegend()).
*/
private void drawLegendSymbol (GeoLayerViewLegendJComponent com, 
double gx, double gy, double rowHeight) {
	// back up the original x and y values at which to draw the point
	double ogy = gy;
	// convert the x and y values to data units
	gx = convertLegendX(gx, false);
	gy = convertLegendY(gy, false);

	GRSymbol sym = null;
	GRColor color = null;
	boolean dodraw = true;
	GeoLayerView layerView = com.getLayerView();
	try {	
		// Draw the symbol, depending on the layer data shape type...
		int layerType = layerView.getLayer().getShapeType();
		sym = layerView.getLegend().getSymbol(com.getIsym());
		if ( sym == null ) {
			return;
		}
		if ( sym.getClassificationType() == GRSymbol.CLASSIFICATION_SINGLE ) {
			color = sym.getColor();
		}
		else if (
			sym.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
			color = sym.getColor();
		}		
		else if (sym.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
			color = sym.getColor();
		}
		else {	
			color = sym.getClassificationColor ( com.getClassification() );
		}
		if ( (color == null) || color.isTransparent() ) {
			// No need to draw the symbol but may have an outline...
			dodraw = false;
		}
		else {	
			GRDrawingAreaUtil.setColor ( __grda, color );
		}
		if ( dodraw && ((layerType == GeoLayer.POINT) || (layerType == GeoLayer.POINT_ZM) ||
			(layerType == GeoLayer.MULTIPOINT)) ) {
			if ( sym.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
				gy = convertLegendY(ogy - 5, false);
				if ( sym.getStyle() == GRSymbol.SYM_VBARSIGNED ) {
					// Draw the symbol twice, once with a positive value in the first color
					// and once with a negative value in the second color.
					color = sym.getColor();
					double [] sym_data = new double[1];
					sym_data[0] = 1.0;
					GRDrawingAreaUtil.drawSymbol( __grda, sym.getStyle(), gx, gy,
						convertLegendX(sym.getSizeX(), true), convertLegendY(sym.getSizeY(), true),
						0.0, 0.0, sym_data, GRUnits.DATA, GRSymbol.SYM_LEFT | GRSymbol.SYM_TOP);
					color = sym.getColor2();
					if ( color != null ) {
						GRDrawingAreaUtil.setColor(__grda, color);
					}
					sym_data[0] = -1.0;
					GRDrawingAreaUtil.drawSymbol ( __grda, sym.getStyle(), gx, gy,
						convertLegendX(sym.getSizeX(), true), convertLegendY(sym.getSizeY(), true),
						0.0, 0.0, sym_data, GRUnits.DATA, GRSymbol.SYM_LEFT|GRSymbol.SYM_TOP );
				}
				else if (sym.getStyle() == GRSymbol.SYM_VBARUNSIGNED) {
					// since unsigned bars only show positive values only get the 
					// first color to use to draw the bar.
					color = sym.getColor();
					double [] sym_data = new double[1];
					sym_data[0] = 1.0;
					GRDrawingAreaUtil.drawSymbol( __grda, sym.getStyle(), gx, gy,
						convertLegendX(sym.getSizeX(), true), convertLegendY(sym.getSizeY(), true),
						0.0, 0.0, sym_data, GRUnits.DATA, GRSymbol.SYM_LEFT	| GRSymbol.SYM_TOP);
				}				
			}
			else {
				// A simple symbol...			
				double size = sym.getSize();
				if (sym.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_TEACUP_SYMBOL) {
					size = 15;
					gy = convertLegendY(ogy + (rowHeight / 4), false);
					GRDrawingAreaUtil.setColor(__grda, GRColor.blue);
					__teacupData[0] = 20;
					__teacupData[1] = 0;
					__teacupData[2] = 14;
					GRDrawingAreaUtil.drawSymbol ( __grda, sym.getStyle(), gx, gy,
						convertLegendX(size, true), convertLegendY(size, true),
						0.0, 0.0, __teacupData, GRUnits.DATA,
						GRSymbol.SYM_LEFT| GRSymbol.SYM_CENTER_Y, __teacupProps);
				}
				else {
					gy = convertLegendY(ogy + (rowHeight / 4), false);
					GRDrawingAreaUtil.drawSymbol ( __grda, sym.getStyle(), gx, gy,
						convertLegendX(size, true), convertLegendY(size, true),
						0.0, 0.0, null, GRUnits.DATA, GRSymbol.SYM_LEFT | GRSymbol.SYM_CENTER_Y);
				}
			}
		}
		else if ( dodraw && ((layerType == GeoLayer.LINE) || (layerType == GeoLayer.POLYLINE_ZM)) ) {
			GRLimits limits = new GRLimits ( com.getDrawLimits());
			// Later need to add a standard GRSymbol for this shape but draw manually for now.
			double x[] = new double[4];
			double y[] = new double[4];
			x[0] = 0;
			x[1] = x[0] + 6.0;
			x[2] = x[1] + 3.0;
			x[3] = x[2] + 6.0;
			y[0] = limits.getBottomY();
			y[1] = limits.getHeight()*.6;
			y[2] = limits.getHeight()*.4;
			y[3] = limits.getTopY();

			for (int i = 0; i < 4; i++) {	
				x[i] = convertLegendX(x[i], true) + gx;
				y[i] = convertLegendY(y[i], true) + gy;
			}
			
			GRDrawingAreaUtil.drawPolyline ( __grda, 4, x, y );
			x = null;
			y = null;
		}
		else if ( (layerType == GeoLayer.POLYGON) || (layerType == GeoLayer.GRID) ) {
			GRLimits limits = new GRLimits ( com.getDrawLimits());
			// First fill in the box...
			if ( dodraw ) {
				GRDrawingAreaUtil.fillRectangle ( __grda, gx, gy,
					convertLegendX(limits.getWidth(), true),
					convertLegendY(limits.getHeight(), true));
			}
			// Now draw the outline...
			GRColor outline_color = sym.getOutlineColor();
			if ( (outline_color != null) && !outline_color.isTransparent() ) {
				GRDrawingAreaUtil.setColor ( __grda, outline_color );
				GRDrawingAreaUtil.drawRectangle ( __grda, gx, gy + 1,
					convertLegendX(limits.getWidth() - 1.0, true),
					convertLegendY((limits.getHeight() - 1.0), true));
			}
		}
		// Else currently not supported....
	}
	catch ( Exception e ) {
		// May throw exception if not initialized yet.  This is OK
		// as exception will not occur after initialization.
		Message.printWarning ( 3, "", e );
	}
}

/**
Returns whether the legend should be drawn or not.
@return whether the legend should be drawn or not.
*/
public boolean getDrawLegend() {
	return __drawLegend;
}

/**
Returns the layout object used to set up the legend.
@return the layout object used to set up the legend.
*/
public GeoViewLegendLayout getLegendLayout() {
	return __layout;
}

/**
Sets whether the GeoViewJComponent should drawn in an antialiased mode.  
Currently only affects point symbols.  Affecting other things slows it down too much.
@param antiAliased if true, graphics will be drawn antialiased.
*/
public void setAntiAliased(boolean antiAliased) {
	__antiAliased = antiAliased;
}

/**
Sets whether the legend should be drawn or not.  If the legend is currently 
not drawn and is set to be drawn, the GeoView will be refreshed.  If the legend
is currently drawn and is set to be not drawn, the GeoView will be refreshed.
Otherwise, the GeoView will not be refreshed.
@param drawLegend true if the legend should be drawn, false otherwise.
*/
public void setDrawLegend(boolean drawLegend) {	
	if (drawLegend != __drawLegend) {
		__drawLegend = drawLegend;
		__forceRedraw = true;		
		repaint();
	}
	else {
		__drawLegend = drawLegend;
	}	
}

/**
Sets the legend tree to use for building the legend.
@param legendJTree the legend tree to use to build the legend.
*/
public void setLegendJTree(GeoViewLegendJTree legendJTree) {
	__legendJTree = legendJTree;
}

// JTS -- note that the above code to about line 4300 was done during 
// development of legend work.  It's kept here for now for easy access.
// Should be merged later, but in case wholesale chunks need removed or 
// modified, this offers easy access for now.
//////////////////////////////////////////

}
