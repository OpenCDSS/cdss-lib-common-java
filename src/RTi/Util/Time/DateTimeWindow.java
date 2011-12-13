package RTi.Util.Time;

/**
This class stores a window defined by two DateTime end-points, which is used to specify a processing window
within a year, for example to represent as season.
It is NOT the same as DateTimeRange, which is a period that may span multiple years.
Currently the instance is immutable and copies of the DateTime data are copied at construction.
Null date/times are allowed.  Currently there is no validation done.
*/
public class DateTimeWindow
{
    
/**
Year for the window, which can be used when parsing window strings.
Using 2000 allows for a leap year to be considered.  For example, a window string MM-DD can be appended
to WINDOW_YEAR + "-" to parse the date used in the window.  This is necessary because the window internally
uses DateTime instances, which require a year.
*/
public static final int WINDOW_YEAR = 2000;

/**
Starting DateTime for the window.
*/
private DateTime __start = null;

/**
Ending DateTime for the window.
*/
private DateTime __end = null;

/**
Constructor.
@param start starting date/time in the window.  Can be null to indicate open-ended window (from start
of year to the specified end).
@param end ending date/time in the window.  Can be null to indicate open-ended range (from the specified start
to the end of the year).
*/
public DateTimeWindow ( DateTime start, DateTime end )
{
    if ( start == null ) {
        __start = null;
    }
    else {
        __start = new DateTime ( start );
    }
    if ( end == null ) {
        __end = null;
    }
    else {
        __end = new DateTime ( end );
    }
}

/**
Return the ending date/time in the window (can be null) if open-ended.
@return the ending date/time in the window (can be null) if open-ended.
*/
public DateTime getEnd ()
{
    return __end;
}

/**
Return the starting date/time in the window (can be null) if open-ended.
@return the starting date/time in the window (can be null) if open-ended.
*/
public DateTime getStart ()
{
    return __start;
}

/**
Determine whether the specified date/time is in the requested window.
@param dt a DateTime to compare with the window
*/
public boolean isDateTimeInWindow ( DateTime dt )
{   DateTime start = __start;
    if ( start != null ) {
        start = new DateTime ( __start );
        start.setYear ( dt.getYear() );
        start.setPrecision(dt.getPrecision());
    }
    DateTime end = __end;
    if ( end != null ) {
        end = new DateTime ( __end );
        end.setYear ( dt.getYear() );
        end.setPrecision(dt.getPrecision());
    }
    if ( dt == null ) {
        return false;
    }
    if ( (start == null) && (end == null) ) {
        // No constraint...
        return true;
    }
    boolean afterStart = false;
    boolean beforeEnd = false;
    if ( (start == null) || dt.greaterThanOrEqualTo(start) ) {
        afterStart = true;
    }
    if ( (end == null) || dt.lessThanOrEqualTo(end) ) {
        beforeEnd = true;
    }
    if ( afterStart && beforeEnd ) {
        return true;
    }
    else {
        return false;
    }
}

}