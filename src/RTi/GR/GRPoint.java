// GRPoint - GR point

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

package RTi.GR;

/**
This class stores a single 2D point.
Data are public but the set methods should be called to set data.
*/
public class GRPoint extends GRShape
{

// Data members.

public double x, y;	// Just one coordinate pair.

// TODO SAM 2010-12-23 Maybe should initialize to NaN.
/**
Construct and initialize to (0,0).
*/
public GRPoint ( ) {
	super ();
	type = POINT;
	x = xmin = xmax = 0.0;
	y = ymin = ymax = 0.0;
}

/**
Construct given an (X,Y) pair.
@param xset X-coordinate.
@param yset Y-coordinate.
*/
public GRPoint ( double xset, double yset ) {
	super ();
	type = POINT;
	x = xmax = xmin = xset;
	y = ymax = ymin = yset;
	limits_found = true;
}

/**
Construct given the attribute lookup key and an (X,Y) pair.
@param xset X-coordinate.
@param yset Y-coordinate.
@param attkey Attribute lookup key.
*/
public GRPoint ( long attkey, double xset, double yset ) {
	super ( attkey );
	type = POINT;
	x = xmax = xmin = xset;
	y = ymax = ymin = yset;
	limits_found = true;
}

/**
Copy constructor.
@param point Point to copy.
*/
public GRPoint ( GRPoint point ) {
	super ( point.index );
	type = POINT;
	x = xmin = xmax = point.x;
	y = ymin = ymax = point.y;
	// Base class does not have a constructor for this yet.
	is_visible = point.is_visible;
	is_selected = point.is_selected;
	associated_object = point.associated_object;
	limits_found = point.limits_found;
}

/**
Returns true if the x and y coordinates for the shapes are equal.
@return true if the x and y coordinates for the shapes are equal.
*/
public boolean equals ( GRPoint pt ) {
	if ( (pt.x == x) && (pt.y == y) ) {
		return true;
	}
	return false;
}

/**
Returns true if the x and y coordinates for the shapes are equal.
@param xpt x coordinate to compare to.
@param ypt y coordinate to compare to.
@return true if the x and y coordinates for the shapes are equal.
*/
public boolean equals ( double xpt, double ypt ) {
	if ( (xpt == x) && (ypt == y) ) {
		return true;
	}
	return false;
}

/**
Returns the x coordinate.  Access the public data directly to speed performance.
@return the X-coordinate.
*/
public double getX ( ) {
	return x;
}

/**
Returns the Y-coordinate.  Access the public data directly to speed performance.
@return The Y-coordinate.
*/
public double getY ( ) {
	return y;
}

/**
Set the X-coordinate.
@param xset X-coordinate to set.
@deprecated Use the version that sets both coordinates because setting one
makes it difficult to know if limits have been completely set.
*/
public void setX ( double xset ) {
	x = xmin = xmax = xset;
}

/**
Set the Y-coordinate.
@param yset Y-coordinate to set.
@deprecated Use the version that sets both coordinates because setting one
makes it difficult to know if limits have been completely set.
*/
public void setY ( double yset ) {
	y = ymin = ymax = yset;
}

/**
Set the X and Y-coordinates.
@param xset X-coordinate to set.
@param yset Y-coordinate to set.
*/
public void setXY ( double xset, double yset ) {
	x = xmin = xmax = xset;
	y = ymin = ymax = yset;
	limits_found = true;
}

/**
Returns a String representation of the point in the format "(x,y)".
@return A string representation of the point in the format "(x,y)".
*/
public String toString () {
	return "(" + x + "," + y + ")";
}

}