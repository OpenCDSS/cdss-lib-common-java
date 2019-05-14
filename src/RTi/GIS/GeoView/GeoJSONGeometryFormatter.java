// GeoJSONGeometryFormatter - GeoJSON formatter for geometry

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

package RTi.GIS.GeoView;

import RTi.GR.GRPoint;
import RTi.GR.GRPolygon;
import RTi.GR.GRShape;

/**
This class formats shapes into GeoJSON feature text.  To use, declare an instance of this class
and then call formatGeoJSON() to format a GRShape.
*/
public class GeoJSONGeometryFormatter
{
	
/**
Number of spaces for indent levels, for nice formatting.
*/
private String indent = "";

/**
 * Precision for coordinates.
 */
private int coordinatePrecision = 6;

/**
 * Format for coordinates, determined from coordinatePrecision value >= 0.
 */
private String coordinateFormat = null;

/**
 * Precision for elevation.
 */
//private int elevationPrecision = 2;

/**
 * Format for elevation, determined from elevationPrecision value >= 0.
 */
//private String elevationFormat = null;

/**
Construct a GeoJSONGeometryFormatter.
@param indent number of spaces to indent for nice formatting
*/
public GeoJSONGeometryFormatter(int indent)
{	this(indent,-1,-1);
}

/**
Construct a GeoJSONGeometryFormatter.
@param indent number of spaces to indent for nice formatting
@param coordinatePrecision number of digits after decimal point for coordinates
@param elevationPrecision number of digits after decimal point for elevation
*/
public GeoJSONGeometryFormatter(int indent, int coordinatePrecision, int elevationPrecision)
{	StringBuilder sb = new StringBuilder();
	for ( int i = 0; i < indent; i++ ) {
		sb.append(" ");
	}
	this.indent = sb.toString();
	this.coordinatePrecision = coordinatePrecision;
	//this.elevationPrecision = elevationPrecision;
	if ( this.coordinatePrecision >= 0 ) {
		// Format will be something like "%.6f"
		this.coordinateFormat = "%." + this.coordinatePrecision + "f";
	}
	//if ( this.elevationPrecision >= 0 ) {
	//	// Format will be something like "%.6f"
	//	this.elevationFormat = "%." + this.elevationPrecision + "f";
	//}
}

/**
Format a GRPoint as a GeoJSON geometry string.
@param point point object to process
@param niceFormat if true format with newlines to be more readable
*/
private String formatPoint ( GRPoint point, boolean niceFormat, String lineStart )
{
    StringBuilder b = new StringBuilder();
    String nl = "";
    String prefix0 = "";
    String prefix1 = "";
    if ( niceFormat ) {
    	nl = "\n";
    	if ( lineStart != null ) {
    		prefix0 = lineStart;
    		prefix1 = lineStart + this.indent;
    	}
    }
    b.append ( "{" + nl );
    b.append ( prefix1 );
    b.append ( "\"type\": \"Point\"," + nl );
    b.append ( prefix1 );
    b.append ( "\"coordinates\": [" );
    if ( this.coordinatePrecision < 0 ) {
    	// Format based on data value precision
    	b.append ( point.x );
    }
    else {
    	// Format to specific precision
    	b.append ( String.format(this.coordinateFormat, point.x) );
    }
    b.append ( ", " );
    if ( this.coordinatePrecision < 0 ) {
    	// Format based on data value precision
    	b.append ( point.y );
    }
    else {
    	// Format to specific precision
    	b.append ( String.format(this.coordinateFormat, point.y) );
    }
    b.append ( "]" + nl );
    b.append ( prefix0 );
    b.append ( "}" + nl );
    return b.toString();
}

/**
Format a GRPolygon as a GeoJSON geometry string.
@param polygon polygon object to process
@param niceFormat if true format with newlines to be more readable
*/
private String formatPolygon ( GRPolygon polygon, boolean niceFormat, String lineStart )
{
    StringBuilder b = new StringBuilder();
    String nl = "";
    String prefix0 = "";
    String prefix1 = "";
    String prefix2 = "";
    if ( niceFormat ) {
    	nl = "\n";
    	if ( lineStart != null ) {
    		prefix0 = lineStart;
    		prefix1 = lineStart + this.indent;
    		prefix2 = prefix1 + this.indent;
    	}
    }
    b.append ( "{" + nl );
    b.append ( prefix1 );
    b.append ( "\"type\": \"Polygon\"," + nl );
    b.append ( prefix1 );
    b.append ( "\"coordinates\": [" + nl );
    int npts0 = polygon.npts - 1;
    for ( int i = 0; i < polygon.npts; i++ ) {
    	if ( i == 0 ) {
    	    b.append ( prefix2 );
    		b.append ( "[ ");
    	}
    	b.append ( "[");
	    b.append ( polygon.pts[i].x);
	    b.append ( ", " );
	    b.append ( polygon.pts[i].y );
	    b.append ( "]" );
	    if ( i != npts0 ) {
	    	b.append ( ", " );
	    }
	    if ( (i != 0) && (i != npts0) && niceFormat && ((i % 10) == 0) ) {
	    	// Maximum of 10 points on a line
	    	b.append ( nl );
	    	if ( (i != 0) && (i != npts0) ) {
	    		b.append ( prefix2 );
	    	}
	    }
	    if ( i == npts0 ) {
    		b.append ( " ]"); // Close array of points
    	}
    }
    b.append ( nl + prefix1 + "]" + nl ); // Close coordinates
    b.append ( prefix0 );
    b.append ( "}" + nl );
    return b.toString();
}

/**
Format a GRShape into a GeoJSON feature which is the text after the GeoJSON "geometry:" text.
Recognized geometry types include:
<ul>
<li> GRPoint
<li> GRPolygon</li>
</ul>
@param shape shape to format
@param niceFormat use newline at end of lines (improves readability but increases file size slightly)
@param lineStart a string with spaces to insert at the front of lines, to indent the geometry for nice formatting
@exception UnrecognizedGeometryException if the geometry is not recognized.
*/
public String format ( GRShape shape, boolean niceFormat, String lineStart )
{
    if ( shape instanceof GRPoint ) {
    	return formatPoint ( (GRPoint)shape, niceFormat, lineStart );
    }
    else if ( shape instanceof GRPolygon ) {
    	return formatPolygon ( (GRPolygon)shape, niceFormat, lineStart );
    }
    else {
        throw new UnrecognizedGeometryException("Unrecognized geometry type " + shape.getClass().getSimpleName() + " - don't know how to format GeoJSON" );
    }
}

}
