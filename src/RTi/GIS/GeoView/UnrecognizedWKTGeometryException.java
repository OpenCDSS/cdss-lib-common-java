package RTi.GIS.GeoView;

/**
Exception to throw when parsing a Well Known Text (WKT) geometry and the geometry is not recognized.
*/
@SuppressWarnings("serial")
public class UnrecognizedWKTGeometryException extends RuntimeException
{

/**
Construct with a string message.
@param s String message.
*/
public UnrecognizedWKTGeometryException ( String s )
{   super ( s );
}

}