// GRPolygonList - GR polygon list

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
// GRPolygonList - GR polygon list
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 23 Jun 1999	Steven A. Malers	Initial version.  Copy GRPolylineList
//					and update.
// 2001-12-07	SAM, RTI		Add to copy is_selected and
//					associated_object.
// 2005-04-26	J. Thomas Sapienza, RTi	finalize() uses IOUtil.nullArray().
// ----------------------------------------------------------------------------

package RTi.GR;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

/**
This class stores a list of GRPolygon, which allows storage of ESRI Arc
shapes.  Data are public to
increase performance during draws but the set methods should be used to set
data.  Currently, the number of polygons cannot be dynamically extended.
*/
public class GRPolygonList extends GRShape
{

/**
Number of polygons.
*/
public int npolygons = 0;

/**
Total number of points.
*/
public int total_npts = 0;

/**
List of polygons.
*/
public GRPolygon[] polygons = null;

/**
Construct with zero polygons.
*/
public GRPolygonList ()
{	super ();
	type = POLYGON_LIST;
	total_npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with zero polygons and set index.
@param att_index attribute index.
*/
public GRPolygonList ( long att_index )
{	super ( att_index );
	type = POLYGON_LIST;
	total_npts = 0;
	xmin = xmax = 0.0;
	ymin = ymax = 0.0;
}

/**
Construct with the specified number of polygons.
The array space for the polygons
is created but not initialized.  setPolygon should then be called to set the
polygon.
@param npolygons_set Number of polygons.
*/
public GRPolygonList ( int npolygons_set )
{
	super ();
	type = POLYGON_LIST;
	setNumPolygons ( npolygons_set );
}

/**
Copy constructor.  A deep copy is made.
*/
public GRPolygonList ( GRPolygonList polygonlist )
{	super ( polygonlist.index );
	type = POLYGON_LIST;
	setNumPolygons ( polygonlist.npolygons );
	for ( int i = 0; i < npolygons; i++ ) {
		setPolygon ( i, new GRPolygon ( polygonlist.polygons[i]) );
	}
	// Set base class data here...
	xmin = polygonlist.xmin;
	xmax = polygonlist.xmax;
	ymin = polygonlist.ymin;
	ymax = polygonlist.ymax;
	limits_found = polygonlist.limits_found;
	is_visible = polygonlist.is_visible;
	is_selected = polygonlist.is_selected;
	associated_object = polygonlist.associated_object;
}

/**
Returns true if the shape matches the one being compared.  Each polygon is
compared.  The number of polygons must agree.
@return true if the shape matches the one being compared.  
*/
public boolean equals ( GRPolygonList polygonlist )
{	if ( npolygons != polygonlist.npolygons ) {
		return false;
	}
	for ( int i = 0; i < npolygons; i++ ) {
		if ( !polygons[i].equals(polygonlist.polygons[i]) ) {
			return false;
		}
	}
	return true;
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	IOUtil.nullArray(polygons);
	super.finalize();
}

/**
Returns the number of polygons.
@return the number of polygons.
*/
public int getNumPolygons ( )
{	return npolygons;
}

/**
Returns a polygon from the array or null if outside the bounds of the array.
Each polygon is compared.  The number of polygons must agree.
@return a polygon from the array or null if outside the bounds of the array.
@param i index position in polygon array (starting at zero).
*/
public GRPolygon getPolygon ( int i )
{	if ( (i < 0) || (i > (npolygons - 1)) ) {
		return null;
	}
	else {	return polygons[i];
	}
}

/**
Reinitialize the polygons array to the specified size.  The polygon data must
then be re-set.
@param npolygons_set Number of polygons to size the polygons array.
*/
public void setNumPolygons ( int npolygons_set )
{	try {	polygons = new GRPolygon[npolygons_set];
		npolygons = npolygons_set;
		xmin = xmax = ymin = ymax = 0.0;
		limits_found = false;
	}
	catch ( Throwable t ) {
		Message.printWarning ( 2, "GRPolygonList.setNumPolygons",
		"Error allocating memory for " + npolygons_set + " polygons." );
	}
}

/**
Set the polygon at an index.  It is assumed that the number of polygons has
already
been specified, thus allocating space for the polygons.  A reference to the
given polygon is saved, not a copy of the data.
@param i Array position for polygon (starting at zero).
@param polygon Polygon to set (null polygons are allowed).
*/
public void setPolygon ( int i, GRPolygon polygon )
{	if ( (i < 0) || (i > (npolygons - 1)) ) {
		return;
	}
	polygons[i] = polygon;
	if ( !limits_found ) {
		// Set the limits...
		xmin = polygon.xmin;
		xmax = polygon.xmax;
		ymin = polygon.ymin;
		ymax = polygon.ymax;
		limits_found = true;
	}
	else {	if ( polygon.xmax > xmax ) {
			xmax = polygon.xmax;
		}
		if ( polygon.xmin < xmin ) {
			xmin = polygon.xmin;
		}
		if ( polygon.ymax > ymax ) {
			ymax = polygon.ymax;
		}
		if ( polygon.ymin < ymin ) {
			ymin = polygon.ymin;
		}
	}
}

} // End of GRPolygonList
