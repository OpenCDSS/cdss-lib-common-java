// GRArc - GR arc (ellipsoid)

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

/**
The GRArc class stores an ellipsoid shape.
Data are public but the set methods should be called to set data so that coordinate limits can be computed.
*/
public class GRArc extends GRShape
{

/**
Angle to start drawing, in degrees counter-clockwise from East.
*/
public double angle1;

/**
Angle to stop drawing, in degrees counter-clockwise from East.
*/
public double angle2;

/**
Radius in X direction.
*/
public double xradius;

/**
Radius in Y direction.
*/
public double yradius;

/**
Coordinates of the center.
*/
public GRPoint pt;

/**
Construct and initialize to (0,0).
*/
public GRArc () {
	super ();
	type = ARC;
	xmax = xmin = xradius = yradius = 0.0;
	pt = new GRPoint();
	angle1 = 0.0;
	angle2 = 0.0;
}

/**
Construct given necessary data pair.
@param pt_set Coordinates of center.
@param xradius_set Radius in X direction.
@param yradius_set Radius in Y direction.
@param angle1_set Starting angle for draw.
@param angle2_set Ending angle for draw.
*/
public GRArc (	GRPoint pt_set,
		double xradius_set, double yradius_set,
		double angle1_set, double angle2_set ) {
	super ();
	type = ARC;
	pt = pt_set;
	xradius = xradius_set;
	yradius = yradius_set;
	angle1 = angle1_set;
	angle2 = angle2_set;
	xmin = pt.x - xradius;
	xmax = pt.x + xradius;
	ymin = pt.y - yradius;
	ymax = pt.y + yradius;
	limits_found = true;
}

/**
Construct given necessary data pair.
@param x_set X-coordinate of center.
@param y_set Y-coordinate of center.
@param xradius_set Radius in X direction.
@param yradius_set Radius in Y direction.
@param angle1_set Starting angle for draw.
@param angle2_set Ending angle for draw.
*/
public GRArc ( double x_set, double y_set,
			double xradius_set, double yradius_set,
			double angle1_set, double angle2_set ) {
	super ();
	type = ARC;
	pt = new GRPoint ( x_set, y_set );
	xradius = xradius_set;
	yradius = yradius_set;
	angle1 = angle1_set;
	angle2 = angle2_set;
	xmin = pt.x - xradius;
	xmax = pt.x + xradius;
	ymin = pt.y - yradius;
	ymax = pt.y + yradius;
	limits_found = true;
}

/**
Construct given the attribute lookup key and shape data.
@param attkey Attribute lookup key.
@param pt_set Coordinates of center.
@param xradius_set Radius in X direction.
@param yradius_set Radius in Y direction.
@param angle1_set Starting angle for draw.
@param angle2_set Ending angle for draw.
*/
public GRArc ( long attkey, GRPoint pt_set,
		double xradius_set, double yradius_set,
		double angle1_set, double angle2_set ) {
	super ( attkey );
	type = ARC;
	pt = pt_set;
	xradius = xradius_set;
	yradius = yradius_set;
	angle1 = angle1_set;
	angle2 = angle2_set;
	xmin = pt.x - xradius;
	xmax = pt.x + xradius;
	ymin = pt.y - yradius;
	ymax = pt.y + yradius;
	limits_found = true;
}

/**
Copy constructor.
@param arc GRArc to copy.
*/
public GRArc ( GRArc arc ) {
	super ( arc.index );
	type = ARC;
	pt = new GRPoint ( arc.pt );
	xradius = arc.xradius;
	yradius = arc.yradius;
	angle1 = arc.angle1;
	angle2 = arc.angle2;
	// Base class does not have a constructor for this yet.
	is_visible = arc.is_visible;
	is_selected = arc.is_selected;
	associated_object = arc.associated_object;
	limits_found = arc.limits_found;
}

/**
Determine whether an arc contains a shape.
Currently only GRPoint shapes are supported and the check only uses the X radius
for the check.  Additional capability will be added later.
@param shape Shape to check.
@return true if the arc contains the shape, false if it does not
*/
public boolean contains ( GRShape shape, boolean contains_completely ) {
	if ( shape.type == GRShape.POINT ) {
		GRPoint pt2 = (GRPoint)shape;
		double dx = pt2.x - pt.x;
		double dy = pt2.y - pt.y;
		if ( (Math.sqrt(dx*dx + dy*dy)) < xradius ) {
			return true;
		}
		pt2 = null;
		return false;
	}
	// For other shapes would need to loop through the coordinates and do something similar.
	// For now return the more course method in the base class.
	return super.contains ( shape, contains_completely );
}

/**
Determine whether shapes are equal.  The center coordinates, radii, and angles are checked.
@param arc the arc to compare against this arc
@return true if the shapes are equal.
*/
public boolean equals ( GRArc arc ) {
	if ( (arc.pt == pt) &&
		(arc.xradius == xradius) && (arc.yradius == yradius) &&
		(arc.angle1 == angle1) && (arc.angle2 == angle2) ) {
		return true;
	}
	return false;
}

/**
Set the center point.
It is assumed that the radius is in the same units as the center point so that the shape extents can be properly computed.
A reference to the given point is saved, not a copy of the data.
@param pt_set Point to set (null points are not allowed).
*/
public void setPoint ( GRPoint pt_set ) {
	pt = pt_set;
	xmin = pt.x - xradius;
	xmax = pt.x + xradius;
	ymin = pt.y - yradius;
	ymax = pt.y + yradius;
}

/**
Return a string representation of the arc.
@return A string representation of the arc in the format "x,y,xradius,yradius,angle1,angle2".
*/
public String toString () {
	return "GRArc(" + pt.x + "," + pt.y + "," + xradius + "," + yradius + "," + angle1 + "," + angle2 + ")";
}

}