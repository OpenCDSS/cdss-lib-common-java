package RTi.TS;

/**
This exception should be thrown when a method requires that time intervals be equal for processing.
*/
@SuppressWarnings("serial")
public class UnequalTimeIntervalException extends RuntimeException
{

/**
Construct with a string message.
@param s String message.
*/
public UnequalTimeIntervalException( String s )
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