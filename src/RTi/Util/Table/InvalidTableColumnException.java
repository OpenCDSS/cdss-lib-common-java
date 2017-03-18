package RTi.Util.Table;

/**
Exception to use when a requested table column does not exist.
*/
@SuppressWarnings("serial")
public class InvalidTableColumnException extends RuntimeException
{

/**
Constructor.
@param message exception string
*/
public InvalidTableColumnException ( String message )
{
	super ( message );
}

/**
Constructor.
@param message exception string
@param e exception to pass to super
*/
public InvalidTableColumnException ( String message, Exception e )
{
	super ( message, e );
}

}