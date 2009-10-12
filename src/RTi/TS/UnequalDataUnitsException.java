package RTi.TS;

/**
This exception should be thrown when a method requires that data units be equal for processing.
*/
public class UnequalDataUnitsException extends RuntimeException
{

/**
Construct with a string message.
@param s String message.
*/
public UnequalDataUnitsException( String s )
{	super ( s );
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	super.finalize();
}

}