// ----------------------------------------------------------------------------
// StopWatch - a timer class to help time processing
// ----------------------------------------------------------------------------
// History:
//
// ?		Steven A. Malers, RTi	Initial version to help diagnose
//					performance issues.
// 2001-02-06	SAM, RTi		Clean up javadoc to simplify port to
//					C++.  Add finalize() method and make
//					sure values are set to null when not
//					used.
// 2001-05-04	SAM, RTi		Rename to StopWatch from Timer because
//					MS VC++ has a conflict with Timer.
//					Deprecate the other class and start
//					using this class.
// ----------------------------------------------------------------------------

package RTi.Util.Time;

import java.lang.String;
import java.util.Date;

/**
This class provides a way to track execution time similar to a physical
stopwatch.  To
use the class, declare an instance and then call "start" and "stop" as necessary
to add to the time.  Use "clear" to reset the timer to zero.  The time amounts
are tracked internally in milliseconds.  Note that the StopWatch features do
introduce overhead into program execution because it requests the system time
and should only be used when debugging or in
cases where the performance issues are not large.  For example, put start/stop
calls outside of loops, or, if in loops, consider only using if wrapped in
Message.isDebugOn() checks.
*/
public class StopWatch {

// Data members...

private long	_total_milliseconds;	// Total time in milliseconds.
private Date	_start_date;		// Start date for a StopWatch session.
private boolean	_start_set;		// Indicates if the start time has been
					// set.
private Date	_stop_date;		// Stop date for a StopWatch session.

/**
Constructor and initialize the StopWatch count to zero milliseconds.
*/
public StopWatch ()
{	initialize ( 0 );
}

/**
Construct given an initial time count (for example, use if a second time is
storing an initial time plus new accumulations of time).
@param total Total time to initialize StopWatch to, milliseconds.
*/
public StopWatch ( long total )
{	initialize ( total );
}

/**
Reset the StopWatch to zero.
*/
public void clear ()
{	_total_milliseconds = 0;
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	_start_date = null;
	_stop_date = null;
}

/**
Return the accumulated milliseconds.
@return The number of milliseconds accumulated in the StopWatch.
*/
public long getMilliseconds ()
{	return _total_milliseconds;
}

/**
Return the accumulated seconds.
@return The number of seconds accumulated in the StopWatch (as a double so that
milliseconds are also reflected).
*/
public double getSeconds ()
{	return (double)_total_milliseconds/(double)1000.0;
}

/**
Initialize StopWatch.
@param initial StopWatch value in milliseconds.
*/
private void initialize ( long total )
{	_total_milliseconds = total;
	_start_date = null;
	_start_set = false;
	_stop_date = null;
}

/**
Start accumulating time in the StopWatch.
*/
public void start ()
{	_start_set = true;
	_start_date = new Date ();
}

/**
Stop accumulating time in the StopWatch.  This does not clear the StopWatch and
subsequent calls to "start" can be made to continue adding to the StopWatch.
*/
public void stop ()
{	_stop_date = new Date ();
	// now compute the difference and add to the
	if ( _start_set ) {
		long add = _stop_date.getTime() - _start_date.getTime();
		_total_milliseconds += add;
	}
	_start_set = false;
}

/**
Print the StopWatch value as seconds.
*/
public String toString ()
{	return "StopWatch(seconds)=" + getSeconds();
}

} // End class StopWatch
