//-----------------------------------------------------------------------------
// GRShape - GR shape abstract class
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
//
// 14 Aug 1997	Steven A. Malers, RTi	Port from C++
// 01 Nov 1998	SAM, RTi		Change "attkey" to index to enfoce use
//					of integer and avoid possible confusion
//					with attribute names used as keys
//					during lookups.
// 21 Jun 1999	SAM, RTi		Add equals and finalize.  Add shape
// 					type, max min, and is_visible.
// 23 Jun 1999	SAM, RTi		Remove shape type from constructor
//					arguments.  It is too easy to get
//					mixed up with setting the size of the
//					shape.
// 30 Aug 1999	SAM, RTI		Add associated_object to member data
//					so that a shape can be directly related
//					to another object.
// 01 Dec 1999	SAM, RTi		Add is_selected to support editing, etc.
// 17 Sep 2001	SAM, RTi		Add GRID shape type.
// 2001-12-08	SAM, RTi		Add ARC shape type.  Clean up javadoc.
//					Change so GRLimits is a shape.  This
//					allows GRLimits to be passed generically
//					to methods that intersect shapes, search
//					using shape areas, etc.
//					Add contains() - to be overruled by
//					derived classes.
// 2002-05-17	SAM, RTi		Add LOCATOR_ARC shape type.
// 2004-10-27	J. Thomas Sapienza, RTi	Implements Cloneable.
// ----------------------------------------------------------------------------

package RTi.GR;

/**
GRShape is the base class for all GR shape classes.  At some point, additional
information like color may be added to this class but currently shapes only
store geometry information.  Set/Get methods are not implemented to keep objects
small and to optimize performance.  Access the data directly.
*/
public class GRShape
implements Cloneable
{

/**
Types of shapes.  Where there is compatibility with ESRI shapes, use the
ESRI shape number.  Values below 100 are reserved for internal GR use.
*/
public final static byte UNKNOWN = 0;

/**
Corresponds to GRArc shape type.
*/
public final static byte ARC = 23;

/**
Corresponds to GRLocatorArc.
*/
public final static byte LOCATOR_ARC = 25;

/**
Corresponds to GRGrid (regular or irregular grid).
*/
public final static byte GRID = 22;

/**
Corresponds to GRLimits.
*/
public final static byte LIMITS = 24;

/**
Corresponds to GRPoint (single point).
*/
public final static byte POINT = 1;

/**
Corresponds to GRPolyline (line segment).
*/
public final static byte POLYLINE = 20;

/**
Corresponds to GRPolylineList (list of line segments,
suitable for storing to ESRI Arc).
*/
public final static byte POLYLINE_LIST = 3;

/**
Corresponds to GRPolygon.
*/
public final static byte POLYGON = 21;

/**
Corresponds to GRPolygonList (list of polygons,
suitable for storing to ESRI Polygon).
*/
public final static byte POLYGON_LIST = 5;

/**
Corresponds to GRPolypoint (multiple points,
suitable for storing to ESRI MultPoint).
*/
public final static byte POLYPOINT = 8;

/**
GRShape data are public to optimize performance.
*/

/**
Object to associate with the shape.  Use this, for example, to relate to a
database or application object.
*/
public Object associated_object;

/**
Index to attribute to GIS/DB information.
*/
public long index;

/**
Shape type (see shape types defined in this class).
*/
public byte type;

/**
Minimum x data coordinate.
*/
public double xmin;

/**
Maximum x data coordinate.
*/
public double xmax;

/**
Minimum y data coordinate.
*/
public double ymin;

/**
Maximum y data coordinate.
*/
public double ymax;

/**
True if the shape is selected.  False if not.  This is used by higher-level
code to select shapes from displays, etc.  The default is not selected.
*/
public boolean is_selected = false;

/**
True if the shape is visible.  False if not.  This is used to hide shapes
to increase performance or make displays less busy.
*/
public boolean is_visible = true;

/**
The following should only need to be used by derived classes and indicates
if the limits have been found.  The deault limits are 0.0, 0.0.
*/
public boolean limits_found = false;

/**
Construct without assigning the attribute lookup key.  The shape will be
visible and the limits are set to zeros.
*/
public GRShape ()
{	index = -1;
	is_visible = true;
	type = UNKNOWN;
	xmin = xmax = ymin = ymax = 0.0;
	associated_object = null;
}

/**
Construct and set the attribute table lookup index.  The shape will be
visible and the limits are set to zeros.
@param lookup_index Attribute lookup key.
*/
public GRShape ( long lookup_index )
{	index = lookup_index;
	type = 0;
	xmin = xmax = ymin = ymax = 0.0;
	is_visible = true;
	is_selected = false;
	associated_object = null;
}

/**
Clones the object.
@return a clone of this object.
*/
public Object clone() {
	try {
		return (GRShape)super.clone();
	}
	catch (Exception e) {
		return null;
	}
}

/**
Determine whether a shape contains another shape.  The minimum and maximum
coodinates of the shape are used to make selections.  This should work in a
course fashion for all shapes; however, if a shape is non-rectangular, then
a more complex method must be implemented to indicate if an intersection occurs.
To do so, override this method in derived classes.
@param shape GRShape to evaluate.
@param contains_completely If true, then the shape s must be completely within
this shape to return true.
@return true if this shape contains the specified shape
*/
public boolean contains ( GRShape shape, boolean contains_completely )
{	// Check the overall limits...
	if (	(shape.xmax < xmin) || (shape.xmin > xmax) ||
		(shape.ymax < ymin) || (shape.ymin > ymax) ) {
		// Definitely not in...
		return false;
	}
	if (	(shape.xmin >= xmin) && (shape.xmax <= xmax) &&
		(shape.ymin >= ymin) && (shape.ymax <= ymax) ) {
		// Totally in...
		return true;
	}
	if ( contains_completely ) {
		return false;
	}
	else {	return true;
	}
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	associated_object = null;
	super.finalize();
}

/**
Return the attribute lookup index.
@deprecated Use the public data directly to increase performance.
@return The attribute lookup index.  Use the public data directly to increase
performance.
*/
public long getIndex ()
{	return index;
}

} // End GRShape class
