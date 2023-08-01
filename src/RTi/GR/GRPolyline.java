// GRPolyLine - class to store a sequence of non-closing points

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

package RTi.GR;

import RTi.Util.Message.Message;

/**
This class stores a sequence of non-closing points.
Data are public to increase performance during draws but the set methods should be used to set data.
Currently, the number of points cannot be dynamically extended.
*/
public class GRPolyline extends GRShape
{

/**
Number of points.
*/
public int npts = 0;

/**
List of points.
*/
public GRPoint[] pts = null;

/**
Construct with zero points.
*/
public GRPolyline () {
	super ();
	type = POLYLINE;
	npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with zero points and set index.
@param att_index attribute index.
*/
public GRPolyline ( long att_index ) {
	super ( att_index );
	type = POLYLINE;
	npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with the specified number of points.  The array space for the points is created but not initialized.
The setPoint() method should then be called to set the points.
@param npts_set Number of points.
*/
public GRPolyline ( int npts_set ) {
	super ();
	type = POLYLINE;
	setNumPoints ( npts_set );
}

/**
Copy constructor.  A deep copy is made.
@param polyline the polyLine to copy.
*/
public GRPolyline ( GRPolyline polyline ) {
	super ( polyline.index );
	type = POLYLINE;
	setNumPoints ( polyline.npts );
	for ( int i = 0; i < npts; i++ ) {
		setPoint ( i, new GRPoint ( polyline.pts[i]) );
	}
	// Set base class data here.
	xmin = polyline.xmin;
	xmax = polyline.xmax;
	ymin = polyline.ymin;
	ymax = polyline.ymax;
	limits_found = polyline.limits_found;
	is_visible = polyline.is_visible;
	is_selected = polyline.is_selected;
	associated_object = polyline.associated_object;
}

/**
Returns true if the shape matches the one being compared.  Each point is compared.  The number of points must agree.
@return true if the shape matches the one being compared.
*/
public boolean equals ( GRPolyline polyline ) {
	if ( npts != polyline.npts ) {
		return false;
	}
	for ( int i = 0; i < npts; i++ ) {
		if ( !pts[i].equals(polyline.pts[i]) ) {
			return false;
		}
	}
	return true;
}

/**
Returns the number of points.
@return the number of points.
*/
public int getNumPoints ( ) {
	return npts;
}

/**
Returns a point from the array or null if outside the bounds of the array.
A reference to the point is returned.  Reference the public data directly to speed performance.
@param i index position in point array (starting at zero).
@return a point from the array or null if outside the bounds of the array.
*/
public GRPoint getPoint ( int i ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return null;
	}
	else {
		return pts[i];
	}
}

/**
Returns the x coordinate for a point or zero if the array bounds are exceeded.
Reference the public data directly to speed performance.
@return the x coordinate for a point or zero if the array bounds is exceeded.
*/
public double getX ( int i ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return 0.0;
	}
	else {
		return pts[i].x;
	}
}

/**
Returns the y coordinate for a point or zero if the array bounds are exceeded.
Reference the public data directly to speed performance.
@return the y coordinate for a point or zero if the array bounds is exceeded.
*/
public double getY ( int i ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return 0.0;
	}
	else {
		return pts[i].y;
	}
}

/**
Reinitialize the points array to the specified size.  You must reset the point data.
@param npts_set Number of points to size the points array.
*/
public void setNumPoints ( int npts_set ) {
	try {
		pts = new GRPoint[npts_set];
		npts = npts_set;
		xmin = xmax = ymin = ymax = 0.0;
		limits_found = false;
	}
	catch ( Throwable t ) {
		Message.printWarning ( 2, "GRPolyline.setNumPoints", "Out of memory allocating " + npts_set + " points." );
	}
}

/**
Set the point at an index.
It is assumed that the number of points has already been specified, thus allocating space for the points.
A reference to the given point is saved, not a copy of the data.
@param i Array position for point (starting at zero).
@param pt Point to set (null points are allowed).
*/
public void setPoint ( int i, GRPoint pt ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return;
	}
	pts[i] = pt;
	if ( !limits_found ) {
		// Set the limits.
		xmin = xmax = pt.x;
		ymin = ymax = pt.y;
		limits_found = true;
	}
	else {
		if ( pt.x > xmax ) {
			xmax = pt.x;
		}
		if ( pt.x < xmin ) {
			xmin = pt.x;
		}
		if ( pt.y > ymax ) {
			ymax = pt.y;
		}
		if ( pt.y < ymin ) {
			ymin = pt.y;
		}
	}
}

/**
Set the point at an index.
It is assumed that the number of points has already been specified, thus allocating space for the points.
It is also assumed that the point being manipulated also has been instantiated.
@param i Array position for point (starting at zero).
@param x x-position of point to set.
@param y y-position of point to set.
*/
public void setPoint ( int i, double x, double y ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return;
	}
	else {
		pts[i].setXY ( x, y );
	}
	if ( !limits_found ) {
		// Set the limits.
		xmin = xmax = x;
		ymin = ymax = y;
		limits_found = true;
	}
	else {
		if ( x > xmax ) {
			xmax = x;
		}
		if ( x < xmin ) {
			xmin = x;
		}
		if ( y > ymax ) {
			ymax = y;
		}
		if ( y < ymin ) {
			ymin = y;
		}
	}
}

}