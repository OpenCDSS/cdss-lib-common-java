package RTi.GIS.GeoView;

import RTi.GR.GRPoint;
import RTi.GR.GRPointZM;
import RTi.GR.GRPolygon;
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
<li> Empty
<li> Point (X Y)</li>
<li>
</ul>
@exception UnrecognizedWKTGeometryException if the geometry is not recognized.
*/
public GRShape parseWKT(String wkt)
{
    String wktShape = ""; // The first first part of the string, before (
    int pos2 = wkt.indexOf("(");
    boolean doZ = false;
    boolean doM = false;
    // If no starting ( check for empty
    if ( pos2 < 0 ) {
        if ( wkt.trim().toUpperCase().endsWith("EMPTY") ) {
            return null;
        }
        else {
            // Don't know how to handle geometry without (
            throw new UnrecognizedWKTGeometryException("Unrecognized geometry \"" + wkt + "\"" );
        }
    }
    // Have a starting ( so parse the first part of the string to determine shape type and whether Z, M
    int pos3 = pos2; // Character after last ( at start
    while ( true ) {
        if ( wkt.charAt(pos3 + 1) == '(' ) {
            ++pos3;
        }
        else {
            break;
        }
    }
    wktShape = wkt.substring(0,pos2).trim().toUpperCase(); // Shape name
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
    if ( wktShape.startsWith("POINT") ) {
        if ( wkt.startsWith("POINT EMPTY") ) {
            return null;
        }
        // get to "X Y . . , X Y . ."
        wkt = wkt.substring(pos2 + 1).replace(")","").trim();
        String [] parts = wkt.split(" ");
        //for ( int i = 0; i < parts.length; i++ ) {
        //    Message.printStatus(2,"","parts[" + i + "] = " + parts[i]);
        //}
        return parsePoint (parts, doZ, doM);
    }
    else if ( wktShape.startsWith("POLYGON") ) {
        if ( wkt.startsWith("POLYGON EMPTY") ) {
            return null;
        }
        // get to "X Y . ."
        wkt = wkt.substring(pos2 + 1).replace(")","").trim();
        String [] parts = wkt.split(",");
        String [] parts2;
        GRPolygon p = null;
        //if ( doZ || doM ) {
            //p = new GRPolygonZM(parts.length);
        //}
        //else {
            p = new GRPolygon(parts.length);
        //}
        for ( int i = 0; i < parts.length; i++ ) {
            //Message.printStatus(2,"","parts[" + i + "] = " + parts[i]);
            parts2 = parts[i].trim().split(" ");
            p.setPoint(i, parsePoint(parts2,doZ,doM));
        }
        return p;
    }
    else {
        throw new UnrecognizedWKTGeometryException("Unrecognized geometry starting with \"" + wktShape + "\"" );
    }
}

}