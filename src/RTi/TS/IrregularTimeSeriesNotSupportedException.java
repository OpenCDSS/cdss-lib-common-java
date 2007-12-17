package RTi.TS;

import java.lang.Exception;

/**
This exception should be thrown when a method does not support hangling irregular time series.
*/
public class IrregularTimeSeriesNotSupportedException extends Exception
{

/**
Construct with a string message.
@param s String message.
*/
public IrregularTimeSeriesNotSupportedException( String s )
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
