// GRPoint2M - class to store a single 3D point with an optional 4th "measure"

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
This class stores a single 3D point with an optional 4th "measure".
Data are public but the set methods should be called to set data.
*/
public class GRPointZM extends GRPoint
{

/**
Third dimension value.
*/
public double z;

/**
Additional measure value.
*/
public double m;

// TODO SAM 2010-12-23 Maybe should initialize to NaN.
/**
Construct and initialize to (0,0,0,0).
*/
public GRPointZM ( )
{	super ( 0.0, 0.0 );
	type = POINT_ZM;
	z = 0.0;
	m = 0.0;
}

/**
Construct given an (X,Y,Z,M) point.
@param xset X-coordinate
@param yset Y-coordinate
@param zset Z-coordinate
@param mset measure value
*/
public GRPointZM ( double xset, double yset, double zset, double mset )
{	super ( xset, yset );
	type = POINT_ZM;
	z = zset;
	m = mset;
	limits_found = true;
}

/**
Construct given the attribute lookup key and an (X,Y) pair.
@param xset X-coordinate.
@param yset Y-coordinate.
@param attkey Attribute lookup key.
*/
public GRPointZM ( long attkey, double xset, double yset, double zset, double mset )
{	super ( attkey, xset, yset );
	type = POINT_ZM;
	z = zset;
	m = mset;
	limits_found = true;
}

/**
Copy constructor.
@param point Point to copy.
*/
public GRPointZM ( GRPointZM point )
{	super ( point );
	type = POINT_ZM;
	z = point.z;
	m = point.m;
}

/**
Returns true if the x, y, z coordinates for the shapes are equal.
@return true if the x, y, z coordinates for the shapes are equal.
*/
public boolean equals ( GRPointZM pt )
{	if ( (pt.x == x) && (pt.y == y) && (pt.z == z) ) {
		return true;
	}
	return false;
}

/**
Returns true if the x, y, and z coordinates for the shapes are equal.
@param xpt x coordinate to compare
@param ypt y coordinate to compare
@param zpt z coordinate to compare
@return true if the x, y, and z coordinates for the shapes are equal.
*/
public boolean equals ( double xpt, double ypt, double zpt )
{	if ( (xpt == x) && (ypt == y) && (zpt == z) ) {
		return true;
	}
	return false;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	super.finalize();
}


/**
Returns the measure value.  Access the public data directly to speed performance.
@return The measure value.
*/
public double getM ( )
{	return m;
}

/**
Returns the z coordinate.  Access the public data directly to speed performance.
@return the Z-coordinate.  
*/
public double getZ ( )
{	return z;
}

/**
Set the X and Y-coordinates.
@param xset X-coordinate to set.
@param yset Y-coordinate to set.
*/
public void setXYZ ( double xset, double yset, double zset )
{	setXY ( xset, yset );
	z = zset;
}

/**
Set the X and Y-coordinates.
@param xset X-coordinate to set.
@param yset Y-coordinate to set.
*/
public void setXYZM ( double xset, double yset, double zset, double mset )
{	setXY ( xset, yset );
	z = zset;
	m = mset;
}

/**
Returns a String representation of the point in the format "(x,y,z,m)".
@return A string representation of the point in the format "(x,y,z,m)".
*/
public String toString ()
{	return "(" + x + "," + y + "," + z + "," + m + ")";
}

}
