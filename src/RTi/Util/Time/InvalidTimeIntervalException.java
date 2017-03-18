package RTi.Util.Time;

/**
An InvalidTimeIntervalException should be thrown when a TimeInterval string
is invalid (e.g., when it cannot be parsed or is not supported by a method).
*/
@SuppressWarnings("serial")
public class InvalidTimeIntervalException extends RuntimeException
{

public InvalidTimeIntervalException ( String message )
{	super ( message );
}

}