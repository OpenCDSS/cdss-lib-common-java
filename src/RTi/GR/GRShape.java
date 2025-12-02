// GRShape - base class for all GR shape classes

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
<p>
GRShape is the base class for all GR shape classes.
At some point, additional information like color may be added to this class but currently shapes only store geometry information.
Set/get methods are not implemented to keep objects small and to optimize performance.  Access the data directly.
</p>
<p>
GRShape represents common shapes for drawing and also can be used as geometry objects with
geographic information system (GIS) data.  For example, see the RTi.GIS.GeoView.GeoRecord class.
</p>
*/
public class GRShape
implements Cloneable
{

// TODO SAM 2013-12-01 Need to convert these to an enumeration and cross-reference with Well Known Text geometries.
/**
Types of shapes.  Where there is compatibility with ESRI shapes, use the Esri shape number.
Values below 100 are reserved for internal GR use.
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
Corresponds to GRPointZM (single point with Z and measure).
*/
public final static byte POINT_ZM = 11;

/**
Corresponds to GRPolyline (line segment).
*/
public final static byte POLYLINE = 20;

/**
Corresponds to GRPolylineZM (line segment with Z and measure).
*/
public final static byte POLYLINE_ZM = 99;

/**
Corresponds to GRPolylineList (list of line segments, suitable for storing to ESRI Arc).
*/
public final static byte POLYLINE_LIST = 3;

/**
Corresponds to GRPolylineZMList (list of line segments, suitable for storing to ESRI Arc), with Z and measure.
*/
public final static byte POLYLINE_ZM_LIST = 13;

/**
Corresponds to GRPolygon.
*/
public final static byte POLYGON = 21;

/**
Corresponds to GRPolygonList (list of polygons, suitable for storing to Esri Polygon).
*/
public final static byte POLYGON_LIST = 5;

/**
Corresponds to GRPolypoint (multiple points, suitable for storing to ESRI MultPoint).
*/
public final static byte POLYPOINT = 8;

/**
GRShape data are public to optimize performance.
*/

/**
Object to associate with the shape.  Use this, for example, to relate to a database or application object.
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
True if the shape is selected.  False if not.
This is used by higher-level code to select shapes from displays, etc.  The default is not selected.
*/
public boolean is_selected = false;

/**
True if the shape is visible.  False if not.
This is used to hide shapes to increase performance or make displays less busy.
*/
public boolean is_visible = true;

/**
The following should only need to be used by derived classes and indicates if the limits have been found.
The default limits are 0.0, 0.0.
*/
public boolean limits_found = false;

/**
Construct without assigning the attribute lookup key.
The shape will be visible and the limits are set to zeros.
*/
public GRShape () {
	index = -1;
	is_visible = true;
	type = UNKNOWN;
	xmin = xmax = ymin = ymax = 0.0;
	associated_object = null;
}

/**
Construct and set the attribute table lookup index.
The shape will be visible and the limits are set to zeros.
@param lookup_index Attribute lookup key.
*/
public GRShape ( long lookup_index ) {
	index = lookup_index;
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
Determine whether a shape contains another shape.
The minimum and maximum coordinates of the shape are used to make selections.
This should work in a course fashion for all shapes; however, if a shape is non-rectangular,
then a more complex method must be implemented to indicate if an intersection occurs.
To do so, override this method in derived classes.
@param shape GRShape to evaluate.
@param contains_completely If true, then the shape s must be completely within this shape to return true.
@return true if this shape contains the specified shape
*/
public boolean contains ( GRShape shape, boolean contains_completely ) {
	// Check the overall limits.
	if ( (shape.xmax < xmin) || (shape.xmin > xmax) || (shape.ymax < ymin) || (shape.ymin > ymax) ) {
		// Definitely not in.
		return false;
	}
	if ( (shape.xmin >= xmin) && (shape.xmax <= xmax) && (shape.ymin >= ymin) && (shape.ymax <= ymax) ) {
		// Totally in.
		return true;
	}
	if ( contains_completely ) {
		return false;
	}
	else {
		return true;
	}
}

/**
Return the attribute lookup index.
@deprecated Use the public data directly to increase performance.
@return The attribute lookup index.  Use the public data directly to increase performance.
*/
@Deprecated
public long getIndex () {
	return index;
}

}