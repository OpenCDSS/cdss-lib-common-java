// StopWatch - this class provides a way to track execution time similar to a physical stopwatch

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.Time;

import java.lang.String;
import java.util.Date;

/**
This class provides a way to track execution time similar to a physical stopwatch.  To
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

/**
Total elapsed running time in milliseconds.
*/
private long _total_milliseconds;
/**
Start date for a StopWatch session.
*/
private Date _start_date;
/**
Indicates if the start time has been set.
*/
private boolean _start_set;
/**
Stop date for a StopWatch session.
*/
private Date _stop_date;

/**
Construct and initialize the StopWatch count to zero milliseconds.
Call 'start()' to start the stopwatch.
*/
public StopWatch ()
{	this ( false );
}

/**
Construct and initialize the StopWatch count to zero milliseconds,
and start the stopwatch.
*/
public StopWatch ( boolean start )
{	initialize ( 0 );
	if ( start ) {
		start();
	}
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
Add the time from another stopwatch to the elapsed time for this stopwatch.
@param sw the StopWatch from which to get additional time.
*/
public void add ( StopWatch sw )
{
    _total_milliseconds += sw.getMilliseconds();
}

/**
Reset the StopWatch to zero.
*/
public void clear ()
{	_total_milliseconds = 0;
}

/**
Reset the StopWatch to zero and call start().
*/
public void clearAndStart ()
{   _total_milliseconds = 0;
    start();
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
	// Compute the difference and add to the elapsed time.
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

}