// GRPolypoint - GR Polypoint class

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
GR Polypoint class.
*/
public class GRPolypoint extends GRShape
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
public GRPolypoint ( ) {
	super ();
	type = POLYPOINT;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
	npts = 0;
}

/**
Construct with the specified number of points.  The array space for the points is created but not initialized.
The setPoint() method should be called to set the points.
@param npts Number of points.
*/
public GRPolypoint ( int npts ) {
	super ();
	type = POLYPOINT;
	setNumPoints ( npts );
}

/**
Construct and set the shape index and number of points.
@param index Attribute index.
@param npts Number of points.
*/
public GRPolypoint ( long index, int npts ) {
	super ( index );
	type = POLYPOINT;
	setNumPoints ( npts );
}

/**
Copy constructor.  A deep copy is made.
@param polypoint the polypoint to duplicate.
*/
public GRPolypoint ( GRPolypoint polypoint ) {
	super ( polypoint.index );
	type = POLYPOINT;
	setNumPoints ( polypoint.npts );
	for ( int i = 0; i < npts; i++ ) {
		setPoint ( i, new GRPoint ( polypoint.pts[i]) );
	}
	// Set base class data here.
	xmin = polypoint.xmin;
	xmax = polypoint.xmax;
	ymin = polypoint.ymin;
	ymax = polypoint.ymax;
	limits_found = polypoint.limits_found;
	is_visible = polypoint.is_visible;
	is_selected = polypoint.is_selected;
	associated_object = polypoint.associated_object;
}

/**
Reinitialize the points array to the specified size.  The point data must be re-set.
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
		Message.printWarning ( 2, "GRPolypoint.setNumPoints", "Error allocating array for " + npts_set + " points." );
	}
}

/**
Set the point at an index in the list.
@param i Point index.
@param pt Point to set.
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
	else {	if ( pt.x > xmax ) {
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
Set the point at an index in the list.
@param i Point index.
@param x X coordinate of point.
@param y Y coordinate of point.
*/
public void setPoint ( int i, double x, double y ) {
	if ( (i < 0) || (i > (npts - 1)) ) {
		return;
	}
	pts[i].setXY ( x, y );
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