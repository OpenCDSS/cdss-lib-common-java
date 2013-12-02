package RTi.GIS.GeoView;

import RTi.GR.GRPoint;
import RTi.GR.GRShape;
import RTi.Util.Message.Message;

/**
This class parses Well Known Text (WKT) representations of geometry.  To use, declare an instance of this class
and then call parseWKT() to return a GRShape, which can then be used to initialize other data.
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
Parse a WKT string into a GRShape.  Recognized geometry types include:
<ul>
<li> Empty
<li> Point (X Y)
</ul>
@exception UnrecognizedWKTGeometryException if the geometry is not recognized.
*/
public GRShape parseWKT(String wkt)
{
    // Grab the first few characters of the string to use for case-independent comparisons.
    // For now use 15 characters corresponding to MultiLineString or the entire string if shorter.
    String wktStart = "";
    String [] parts;
    if ( wkt.length() < 15 ) {
        wktStart = wkt.toUpperCase();
    }
    else {
        wktStart = wkt.substring(0,15).toUpperCase();
    }
    if ( wktStart.startsWith("EMPTY") ) {
        return new GRPoint(); // Type will be unknown
    }
    else if ( wktStart.startsWith("POINT ") ) {
        parts = wkt.split("[ ()]+");
        for ( int i = 0; i < parts.length; i++ ) {
            Message.printStatus(2,"","parts[" + i + "] = " + parts[i]);
        }
        // Tokens from "Point (X Y)" will be Point, x, and Y
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        return new GRPoint(x, y);
    }
    else {
        throw new UnrecognizedWKTGeometryException("Unrecognized geometry starting with \"" + wktStart + "\"" );
    }
}

}