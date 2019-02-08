// WKTGeometryParser - parser for WKT string to geometry

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

import java.util.ArrayList;
import java.util.List;

import RTi.GR.GRPoint;
import RTi.GR.GRPointZM;
import RTi.GR.GRPolygon;
import RTi.GR.GRShape;
import RTi.Util.Message.Message;

/**
This class parses Well Known Text (WKT) representations of geometry.  To use, declare an instance of this class
and then call parseWKT() to return a GRShape, which can then be used to initialize other data.
This class is not very robust.  Need to use something like the following but have to figure out how much
code baggage comes along:  https://github.com/geotools/geotools/blob/master/modules/library/main/src/main/java/org/geotools/geometry/text/WKTParser.java
*/
public class WKTGeometryParser
{

/**
Construct a WKTGeometryParser.
*/
public WKTGeometryParser()
{
}

/**
Utility method to determine the positions in the WKT string where data start and end.
For example, for POINT(X Y) it would be the position of X (character after left-paren) and Y (character before right-paren).
For shape types that may include multiple shape parts, such as polygons with internal holes,
the start and end will be for each closing polygon, as indicated by ")," for ending of part.
In some cases the individual points may be surrouned by ( ) - this will be stripped out when the substrings are parsed.
For example MULTIPOINT((X Y), (X Y)) from this method will return the array positions from the overall shape - TODO need to implement.
@param shapeType shape type using GRShape (TODO need to convert to enumeration)
@param wkt full WKT string
@param posFirstLeftParen character position (0+) of first (.  Prior to this is the geometry type.
@return list of array of positions where [0] is position in WKT for start of shape data, [1] is the end of the shape data
*/
private List<int []> getDataStartAndEndPos ( int shapeType, String wkt, int posFirstLeftParen ) {
    // Also position the string pointer to ignore multiple (( at start - basically skip to the actual content
	List<int[]> startAndEndList = new ArrayList<int[]>();
    if ( shapeType == GRShape.POINT ) {	// POINT
    	// also LINESTRING
    	// Shapes that have single surrounding ( )
    	int [] startAndEndArray = new int[2];
    	startAndEndArray[0] = posFirstLeftParen + 1;
    	startAndEndArray[1] = wkt.indexOf(")",posFirstLeftParen) - 1;
    	if ( (startAndEndArray[0] > 1) && (startAndEndArray[1] > 1) ) {
    		// Found what was expected
    		startAndEndList.add(startAndEndArray);
    	}
    }
    else if ( shapeType == GRShape.POLYGON ) { // POLYGON
    	// Shapes that have surrounding ( ) with internal () for each shape, for example
    	// POLYGON ((X Y, X Y, ...))
    	// POLYGON ((Xout Yout, Xout Yout, ...),(Xhole Yhole, Xhole Yhole))
    	int count = 0;
    	int startPos = -1, endPos = -1;
    	while ( true ) {
    		if ( count == 0 ) {
        		startPos = wkt.indexOf("((",posFirstLeftParen);
        		if ( startPos < 0 ) {
        			break;
        		}
        		startPos = startPos + 2; // Character after ((
    		}
    		else {
    			// Get from the previous end ),
    			startPos = wkt.indexOf("(",endPos); // Character after ", ("
    			if ( startPos < 0 ) {
    				break;
    			}
    			startPos = startPos + 1;
    		}
    		endPos = wkt.indexOf("),",startPos); // If multiple holes
    		if ( endPos < 0 ) {
    			endPos = wkt.indexOf("))",startPos); // If last list of points
    		}
    		if ( endPos < 0 ) {
    			break;
    		}
    		endPos = endPos - 1; // Character before ending )
    		++count;
    		int [] startAndEndArray = new int[2];
    		startAndEndArray[0] = startPos;
    		startAndEndArray[1] = endPos;
    		startAndEndList.add(startAndEndArray);
    	}
    }
    return startAndEndList;
}

/**
Parse a point string X, Y, M, Z, where M and/or Z are optional
*/
private GRPoint parsePoint ( String [] parts, boolean doZ, boolean doM  )
{
    double x = Double.parseDouble(parts[0].trim());
    double y = Double.parseDouble(parts[1].trim());
    if ( doZ && doM ) {
        double z = Double.parseDouble(parts[2].trim());
        double m = Double.parseDouble(parts[3].trim());
        return new GRPointZM(x,y,z,m);
    }
    else if ( doM ) {
        double z = Double.parseDouble(parts[2].trim());
        return new GRPointZM(x,y,z,0.0);
    }
    else if ( doM ) {
        double m = Double.parseDouble(parts[2].trim());
        return new GRPointZM(x,y,0.0,m);
    }
    else {
        return new GRPoint(x, y);
    }
}

/**
Parse a WKT string into a GRShape.  Recognized geometry types include:
<ul>
<li> EMPTY</li>
<li> POINT (X Y)</li>
<li> POLYGON(X Y, X Y, X Y, ...)</li>
<li> POLYGON((X Y, X Y, X Y, ...))</li>
<li> POLYGON EMPTY</li>
</ul>
@exception UnrecognizedWKTGeometryException if the geometry is not recognized.
*/
public GRShape parseWKT(String wkt)
{
    String wktShape = ""; // The first first part of the string, before (
    int posFirstLeftParen = wkt.indexOf("(");
    boolean doZ = false;
    boolean doM = false;
    // If no starting ( check for empty
    if ( posFirstLeftParen < 0 ) {
        if ( wkt.trim().toUpperCase().endsWith("EMPTY") ) {
            return null;
        }
        else {
            // Don't know how to handle geometry without (
            throw new UnrecognizedWKTGeometryException("Unrecognized geometry \"" + wkt + "\"" );
        }
    }
    // If here have a starting ( so parse the first part of the string before the ( to determine shape type and whether Z, M
    wktShape = wkt.substring(0,posFirstLeftParen).trim().toUpperCase(); // Shape name
    if ( wktShape.endsWith(" Z") ) {
        doZ = true;
    }
    else if ( wktShape.endsWith(" M") ) {
        doM = true;
    }
    else if ( wktShape.endsWith(" ZM") || wktShape.endsWith(" MZ") ) {
        doM = true;
        doZ = true;
    }
    //Message.printStatus(2,"","wktShape=\"" + wktShape + "\" doZ=" + doZ + " doM=" + doM );
    List<int []> startAndEndList = null;
    if ( wktShape.startsWith("POINT") ) {
        if ( wkt.startsWith("POINT EMPTY") ) {
            return null;
        }
        startAndEndList = getDataStartAndEndPos(GRShape.POINT,wkt,posFirstLeftParen);
        // Convert to only the parts separated by commas
        wkt = wkt.substring(startAndEndList.get(0)[0],(startAndEndList.get(0)[1] + 1)).trim().replace(", ",",").replace(" ",","); // Add 1 because substring decrements
        String [] parts = wkt.split(",");
        //for ( int i = 0; i < parts.length; i++ ) {
        //    Message.printStatus(2,"","parts[" + i + "] = " + parts[i]);
        //}
        return parsePoint (parts, doZ, doM);
    }
    else if ( wktShape.startsWith("POLYGON") ) {
        if ( wkt.startsWith("POLYGON EMPTY") ) {
            return null;
        }
        // For now only handle the outside of the polygon
        // get to "X Y . ."
        startAndEndList = getDataStartAndEndPos(GRShape.POLYGON,wkt,posFirstLeftParen);
        // Convert to only the parts separated by commas
        if ( Message.isDebugOn ) {
        	Message.printStatus(2, "", "WKT raw:"+wkt);
        }
        wkt = wkt.substring(startAndEndList.get(0)[0],(startAndEndList.get(0)[1] + 1)).trim().replace(", ",",").replace(" ", ","); // Add 1 because substring decrements
        if ( Message.isDebugOn ) {
        	Message.printStatus(2, "", "WKT after extraction:"+wkt);
        }
        String [] parts = wkt.split(",");
        Message.printStatus(2,"","Have " + parts.length + " parts from splitting WKT coordinates");
        GRPolygon p = null;
        //if ( doZ || doM ) {
            //p = new GRPolygonZM(parts.length);
        //}
        //else {
        int dataInPoint = 2;
        if ( doZ ) {
        	++dataInPoint;
        }
        if ( doM ) {
        	++dataInPoint;
        }
        int nPoints = parts.length/dataInPoint;
        p = new GRPolygon(nPoints);
        //}
        String [] parts1 = new String[dataInPoint];
        for ( int i = 0; i < nPoints; i++ ) {
            //Message.printStatus(2,"","parts[" + i + "] = " + parts[i]);
        	// Pass enough values to parse
        	for ( int j = 0; j < dataInPoint; j++ ) {
	        	parts1[j] = parts[i*dataInPoint + j];
        	}
            p.setPoint(i, parsePoint(parts1,doZ,doM));
        }
        return p;
    }
    else {
        throw new UnrecognizedWKTGeometryException("Unrecognized geometry starting with \"" + wktShape + "\"" );
    }
}

}
