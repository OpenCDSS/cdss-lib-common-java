package RTi.GIS.GeoView;

/**
Exception to throw when a geometry is not recognized.
*/
@SuppressWarnings("serial")
public class UnrecognizedGeometryException extends RuntimeException
{

/**
Construct with a string message.
@param s String message.
*/
public UnrecognizedGeometryException ( String s )
{   super ( s );
}

}