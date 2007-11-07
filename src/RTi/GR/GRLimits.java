// -----------------------------------------------------------------------------
// GRLimits - GR bounding box type class
// -----------------------------------------------------------------------------
// Notes:	(1)	This class stores bounding-box data but in a common
//			right-hand system for all data values (not just screen
//			pixels.
// -----------------------------------------------------------------------------
// History:
//
// 21 Jun 1999	Steven A. Malers, RTi	Add finalize, equals, max methods.
//					Change data to protected.  Add more
//					documentation.  Allow two GRPoints in
//					constructor.
// 28 Jun 1999	SAM, RTi		Add contains().
// 09 Oct 2000	SAM, RTi		Fix bug where getMinX() was returning
//					min Y!
// 23 May 2001	SAM, RTi		Fix bug where setLimits(doublex4) was
//					not setting _left_x properly.
// 03 Oct 2001	SAM, RTi		Overload max() to take a flag to reuse
//					the limits.  This saves memory in cases
//					where the limits may be reset many
//					times.  Add increase() to make it easy
//					to increase limits.
// 2001-12-10	SAM, RTi		Extend GRLimits from GRShape so that it
//					can be passed as a shape.  It is
//					expected that a GRRectangle will be
//					added that will have a different
//					purpose than GRLimits and GRLimits may
//					be phased out of search region code
//					in favor of GRRectangle (perhaps extend
//					GRLimits from GRRectangle?).  There is
//					now a generic base class contains()
//					method.  For now, some of the limits
//					data are redundant with the base class.
// -----------------------------------------------------------------------------
// 2003-05-27	J. Thomas Sapienza, RTi	Changed increase so that the test for
//					location is now <=, instead of just <
// 2004-10-27	JTS, RTi		Implements Cloneable.
// -----------------------------------------------------------------------------

package RTi.GR;

import java.awt.Rectangle;

/**
This class stores the limits of a rectangular area.  The coordinate systems
can be in either direction.  This allows directions to be swapped during
projection (e.g., to correct for downward Y-axis for screen graphics).
Note that all data are specified in terms of the left, right, top, and bottom
coordinates and that minimum, maximum, center, width, and height are computed
from these values.  <b>This object should not be treated as a drawing primitive
but can be passed as a shape if necessary.</b>
*/
public class GRLimits 
extends GRShape
implements Cloneable
{

/**
Indicates that the limits are in device units.  These settings are usually
used as parameters to methods in other classes.
*/
public static final int DEVICE = 1;

/**
Indicates that the limits are for a unit square.
*/
public static final int UNIT = 2;

/**
Bottom-most Y.
*/
protected double _bottom_y;
/**
Left-most X.
*/
protected double _left_x;
/**
Right-most X.
*/
protected double _right_x;
/**
Top-most Y.
*/
protected double _top_y;

/**
Overall height.
*/
protected double _height;
/**
Overall width.
*/
protected double _width;

/**
Center X.
*/
protected double _center_x;
/**
Center Y.
*/
protected double _center_y;

/**
Maximum X.
*/
protected double _max_x;
/**
Maximum Y.
*/
protected double _max_y;
/**
Minimum X.
*/
protected double _min_x;
/**
Minimum Y.
*/
protected double _min_y;

/**
Constructor.  Initialize to a (0,0) to (1,1) square.
*/
public GRLimits ( )
{	initialize ();
}

/**
Constructor.  Build a GRLimits. using the given width and height.  
The origin is (0.0).
@param width Width of limits.
@param height Height of limits.
*/
public GRLimits ( double width, double height )
{	_left_x = 0.0;
	_right_x = width;
	_bottom_y = 0.0;
	_top_y = height;
	type = LIMITS;
	reset ();
}

/**
Constructor.  Use the points for the corners.
@param left_bottom_pt Left, bottom point.
@param right_top_pt Right, top point.
*/
public GRLimits ( GRPoint left_bottom_pt, GRPoint right_top_pt )
{	_left_x = left_bottom_pt.getX();
	_bottom_y = left_bottom_pt.getY();;
	_right_x = right_top_pt.getX();
	_top_y = right_top_pt.getY();
	type = LIMITS;
	reset ();
}

/**
Constructor.  Use the coordinates for the corners, as integers.
@param left_x Left X-coordinate.
@param bottom_y Bottom Y-coordinate.
@param right_x Right X-coordinate.
@param top_y Top Y-coordinate.
*/
public GRLimits ( int left_x, int bottom_y, int right_x, int top_y )
{	_left_x = (double)left_x;
	_bottom_y = (double)bottom_y;
	_right_x = (double)right_x;
	_top_y = (double)top_y;
	type = LIMITS;
	reset ();
}

/**
Constructor.  Use the coordinates for the corners.
@param left_x Left X-coordinate.
@param bottom_y Bottom Y-coordinate.
@param right_x Right X-coordinate.
@param top_y Top Y-coordinate.
*/
public GRLimits ( double left_x, double bottom_y, double right_x, double top_y )
{	_left_x = left_x;
	_bottom_y = bottom_y;
	_right_x = right_x;
	_top_y = top_y;
	type = LIMITS;
	reset ();
}

public GRLimits(Rectangle r) {
	_left_x = r.x;
	_bottom_y = r.y;
	_right_x = r.x + r.width;
	_top_y = r.y + r.height;
	type = LIMITS;
	reset();
}

/**
Copy constructor.
@param limits GRlimits to copy.
*/
public GRLimits ( GRLimits limits )
{	type = LIMITS;
	if ( limits != null ) {
		_bottom_y = limits.getBottomY() ;
		_left_x = limits.getLeftX();
		_right_x = limits.getRightX();
		_top_y = limits.getTopY();
		reset ();
	}
}

/**
Clones this object.
@return a clone of this object.
*/
public Object clone() {
	try {
		return (GRLimits)super.clone();
	}
	catch (Exception e) {
		return null;
	}
}

/**
Indicate whether the limits contain the point in question.
@param pt GRPoint of interest.
@return true if the GRLimits region contains the specified point.
The orientation of the GRLimits axes can be in either direction.
*/
public boolean contains ( GRPoint pt )
{	if (	(((pt.x >= _left_x) && (pt.x <= _right_x)) ||
		((pt.x <= _left_x) && (pt.x >= _right_x))) &&
		(((pt.y >= _bottom_y) && (pt.y <= _top_y)) ||
		((pt.y <= _bottom_y) && (pt.y >= _top_y))) ) {
		return true;
	}
	return false;
}

/**
Indicate whether the limits contain the point in question.
The orientation of the GRLimits axes can be in either direction.
@param x X-coordinate of interest.
@param y Y-coordinate of interest.
@return true if the GRLimits region contains the specified point.
*/
public boolean contains ( double x, double y )
{	if (	(((x >= _left_x) && (x <= _right_x)) ||
		((x <= _left_x) && (x >= _right_x))) &&
		(((y >= _bottom_y) && (y <= _top_y)) ||
		((y <= _bottom_y) && (y >= _top_y))) ) {
		return true;
	}
	return false;
}

/**
Indicate whether the limits contain the region in question.
Currently the orientation of both regions needs to be min x on the left and min
y on the bottom.
@param xmin the lowest x value of the region
@param ymin the lowest y value of the region
@param xmax the highest x value of the region
@param ymax the highest y value of the region
@param contains_completely If true, the region must completely be contained.  If
false, the region must only intersect.  This parameter is not currently checked.
@return true if the GRLimits region contains the specified region.
*/
public boolean contains ( double xmin, double ymin, double xmax, double ymax,
			boolean contains_completely )
{	if (	(xmax < _left_x) || (xmin > _right_x) ||
		(ymax < _bottom_y) || (ymin > _top_y) ) {
		return false;
	}
	return true;
}

/**
Returns true if the limits are the same as those passed in.  The corner
coordinates are checked but if coordinates systems for the limits are not the
same then the limits will not match.
@return true if the limits are the same as those passed in.  
*/
public boolean equals ( GRLimits limits )
{	if ( limits.getLeftX() != _left_x ) {
		return false;
	}
	if ( limits.getRightX() != _right_x ) {
		return false;
	}
	if ( limits.getBottomY() != _bottom_y ) {
		return false;
	}
	if ( limits.getTopY() != _top_y ) {
		return false;
	}
	return true;
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	super.finalize();
}

/**
Return the left X-coordinate.
@return The left X-coordinate.
*/
public double getLeftX ( )
{	return _left_x;
}

/**
Return the right X-coordinate.
@return The right X-coordinate.
*/
public double getRightX ( )
{	return _right_x;
}

/**
Return the bottom Y-coordinate.
@return The bottom Y-coordinate.
*/
public double getBottomY ( )
{	return _bottom_y;
}

/**
Return the top Y-coordinate.
@return The top Y-coordinate.
*/
public double getTopY ( )
{	return _top_y;
}

/**
Return the maximum X-coordinate.
@return The maximum X-coordinate.
*/
public double getMaxX ( )
{	return _max_x;
}

/**
Return the maximum Y-coordinate.
@return The maximum Y-coordinate.
*/
public double getMaxY ( )
{	return _max_y;
}

/**
Return the minimum X-coordinate.
@return The minimum X-coordinate.
*/
public double getMinX ( )
{	return _min_x;
}

/**
Return the minimum Y-coordinate.
@return The minimum Y-coordinate.
*/
public double getMinY ( )
{	return _min_y;
}

/**
Return the width.
@return The width.
*/
public double getWidth ( )
{	return _width;
}

/**
Return the height.
@return The height.
*/
public double getHeight ( )
{	return _height;
}

/**
Return the center X-coordinate.
@return The center X-coordinate.
*/
public double getCenterX ( )
{	return _center_x;
}

/**
Return the center Y-coordinate.
@return The center Y-coordinate.
*/
public double getCenterY ( )
{	return _center_y;
}

/**
Increase the size of the limits.  The left and right limits are widened by
increase_x/2.  The top and bottom limits are widened by increase_y/2.
@param increase_x Amount to increase width.
@param increase_y Amount to increase height.
*/
public void increase ( double increase_x, double increase_y )
{	if ( _left_x <= _right_x ) {
		_left_x -= increase_x/2.0;
		_right_x += increase_x/2.0;
	}
	else {	_right_x -= increase_x/2.0;
		_left_x += increase_x/2.0;
	}
	if ( _bottom_y <= _top_y ) {
		_bottom_y -= increase_x/2.0;
		_top_y += increase_x/2.0;
	}
	else {	_top_y -= increase_x/2.0;
		_bottom_y += increase_x/2.0;
	}
	reset();
}

/**
Initialize the data.
*/
private void initialize ()
{	_bottom_y = 0.0;
	_left_x = 0.0;
	_right_x = 1.0;
	_top_y = 1.0;
	reset ();
}

/**
Return the maximum combined extents of the current limits and another GRLimits.
All coordinates are compared and the maximum bounds are used.
Therefore, orientation of the limits is ignored.  A new GRLimits instance is
returned.
@return the maximum combined extents of the current limits and another GRLimits
instance.  
@param other Other GRLimits instance.
*/
public GRLimits max ( GRLimits other )
{	return max ( other, false );
}

/**
Return the maximum of two GRLimits. All coordinates are compared and the 
maximum bounds are used.  Therefore, orientation of the limits is ignored.
@return the maximum combined extents of the current limits and another GRLimits
instance.  
@param other Other GRLimits instance.
@param reuse_limits If true, the limits will be reused; if false, a new
instance will be created.
*/
public GRLimits max ( GRLimits other, boolean reuse_limits )
{	if ( other == null ) {
		if ( reuse_limits ) {
			return ( this );
		}
		else {	return new GRLimits ( this );
		}
	}
	return max ( other._min_x, other._min_y, other._max_x, other._max_y,
			reuse_limits );
}

/**
Return the maximum of two GRLimits.
@return the maximum combined extents of the current limits and another GRLimits
instance.  All coordinates are compared and the maximum bounds are used.
Therefore, orientation of the limits is ignored.
@param xmin Minimum X value to check.
@param ymin Minimum Y value to check.
@param xmax Maximum X value to check.
@param ymax Maximum Y value to check.
@param reuse_limits If true, the limits will be reused; if false, a new
instance will be created.
*/
public GRLimits max ( double xmin, double ymin, double xmax, double ymax,
			boolean reuse_limits )
{	if ( _min_x < xmin ) {
		xmin = _min_x;
	}
	if ( _min_y < ymin ) {
		ymin = _min_y;
	}
	if ( _max_x > xmax ) {
		xmax = _max_x;
	}
	if ( _max_y > ymax ) {
		ymax = _max_y;
	}
	if ( reuse_limits ) {
		_left_x = xmin;
		_bottom_y = ymin;
		_right_x = xmax;
		_top_y = ymax;
		reset();
		return this;
	}
	else {	return new GRLimits ( xmin, ymin, xmax, ymax );
	}
}

/**
Reset the secondary data.
*/
private void reset ()
{	_center_x = (_left_x + _right_x)/2.0;
	_center_y = (_bottom_y + _top_y)/2.0;
	if ( _left_x <= _right_x ) {
		_min_x = _left_x;
		_max_x = _right_x;
	}
	else {	_min_x = _right_x;
		_max_x = _left_x;
	}
	if ( _bottom_y <= _top_y ) {
		_min_y = _bottom_y;
		_max_y = _top_y;
	}
	else {	_min_y = _top_y;
		_max_y = _bottom_y;
	}
	_width = _max_x - _min_x;
	_height = _max_y - _min_y;
	// Base class...
	xmin = _left_x;
	xmax = _right_x;
	ymin = _bottom_y;
	ymax = _top_y;
}

/**
Set the left X-coordinate.
@param left_x The left X-coordinate.
*/
public void setLeftX ( double left_x )
{	_left_x = left_x;
	reset ();
}

/**
Set the right X-coordinate.
@param right_x The right X-coordinate.
*/
public void setRightX ( double right_x )
{	_right_x = right_x;
	reset ();
}

/**
Set the top Y-coordinate.
@param top_y The top Y-coordinate.
*/
public void setTopY ( double top_y )
{	_top_y = top_y;
	reset ();
}

/**
Set the bottom Y-coordinate.
@param bottom_y The bottom Y-coordinate.
*/
public void setBottomY ( double bottom_y )
{	_bottom_y = bottom_y;
	reset ();
}

/**
Set the limits using the corner points
@param left_x Left X-coordinate.
@param bottom_y Bottom Y-coordinate.
@param right_x Right X-coordinate.
@param top_y Top Y-coordinate.
*/
public void setLimits (	double left_x, double bottom_y, double right_x,
			double top_y )
{	_left_x = left_x;
	_bottom_y = bottom_y;
	_right_x = right_x;
	_top_y = top_y;
	reset ();
}

/**
Return a string representation of the object.
*/
public String toString ()
{	return new String ( "(" + _left_x + "," + _bottom_y + ") (" +
	_right_x + "," + _top_y + ") (" + _center_x + "," + _center_y + ") " +
	_width + "x" + _height );
}

} // End of GRLimits class
