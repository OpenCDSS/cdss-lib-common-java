package RTi.TS;

import java.security.InvalidParameterException;

/**
This exception should be thrown when a method does not support handling irregular time series.
*/
public class IrregularTimeSeriesNotSupportedException extends InvalidParameterException
{

/**
Construct with a string message.
@param s String message.
*/
public IrregularTimeSeriesNotSupportedException( String s )
{	super ( s );
}

}