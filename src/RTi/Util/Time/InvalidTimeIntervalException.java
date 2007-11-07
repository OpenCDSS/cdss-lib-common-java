//------------------------------------------------------------------------------
// InvalidTimeIntervalException - an Exception to be thrown when a TimeInterval
//					string is invalid
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2006-04-25	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.Time;

/**
An InvalidTimeIntervalException should be thrown when a TimeInterval string
is invalid (e.g., when it cannot be parsed).
*/
public class InvalidTimeIntervalException extends Exception
{

public InvalidTimeIntervalException ( String message )
{	super ( message );
}

}
