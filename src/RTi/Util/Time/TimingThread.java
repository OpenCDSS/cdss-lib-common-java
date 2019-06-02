// TimingThread - Thread that performs an action at regular interval.

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

//------------------------------------------------------------------------------
// TimingThread - Thread that performs an action at regular interval.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//
//------------------------------------------------------------------------------
// History:
// 
// 01 Dec 1997	Matthew J. Rutherford, RTi	Created initial version.
// 18 Mar 1998	MJR	Put in documentation.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
//
//------------------------------------------------------------------------------
// 2005-04-26	J. Thomas Sapienza, RTi		Added finalize().
//------------------------------------------------------------------------------

package RTi.Util.Time;

import RTi.Util.Message.Message;

/**
This class is used to perform the <B>Timable.performTimedAction()</B> function
at a regular interval. The sleep time can be specified in minutes, seconds or
milliseconds. The class is used as follows:

<PRE>
	public class MyClass implements Timable
	{
	...

	public int performTimedAction()
	{
		checkStatus();
	}
	...
	}

	{
	...
		MyClass	mc = new MyClass();

		TimingThread tt = new TimingThread( mc, 1, TimingThread.MINUTES );
	...
		// We don't need to check anymore.
		tt.stop();
	}
</PRE>

In the above example, the performTimedAction() method of <B>MyClass</B> is
performed every minute.<BR>
<P>
An alternate constructor lets the programmer supply another thread that can
be suspended while the performTimedAction() method is fired.
<P>
@see Timable
*/

public class TimingThread extends Thread
{
// Some defines that are used when passing in the time to sleep:
/**
Used to specify the sleep time in minutes.
*/
public static final int MINUTES 	= 0;
/**
Used to specify the sleep time in milliseconds.
*/
public static final int MSECONDS 	= 1;
/**
Used to specify the sleep time in seconds.
*/
public static final int SECONDS 	= 2;

// Private data members:
private	long	_sleep_time	= 0;
private Thread	_thread 	= null;
private Timable _timable 	= null;

/**
Construct and start the timing thread with the desired settings.
@param timable Instance of the Timable interface which has the 
performTimedAction() methods.
@param sleep_time Time to sleep.
@param uflag Units of the sleep time (MINUTES, SECONDS, or MSECONDS).
@exception IllegalArgumentException Throws this if any of the arguments are not
valid.
*/

public 	TimingThread( Timable timable, long sleep_time, int uflag ) 
	throws IllegalArgumentException
{
	if( timable == null ){
		throw( 
		new IllegalArgumentException( "NULL Incoming Timable!" ) );
	}
	_timable	= timable;

	setSleepTime( sleep_time, uflag );

	start();
}
/**
Construct and start the timing thread with the desired settings.
@param timable Instance of the Timable interface which has the 
performTimedAction() methods.
@param thread A thread that is to be suspended while the performTimedAction()
method is fired.
@param sleep_time Time to sleep.
@param uflag Units of the sleep time (MINUTES, SECONDS, or MSECONDS).
@exception IllegalArgumentException Throws this if any of the arguments are not
valid.
*/

public 	TimingThread( 
	Timable timable, Thread thread, long sleep_time, int uflag ) 
	throws IllegalArgumentException
{
	if( timable == null ){
		throw( 
		new IllegalArgumentException( "NULL Incoming Timable!" ) );
	}
	_timable	= timable;

	if( thread == null ){
		throw( 
		new IllegalArgumentException( "NULL Incoming Thread!" ) );
	}
	_thread		= thread;

	setSleepTime( sleep_time, uflag );

	start();
}

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	_thread 	= null;
	_timable 	= null;
	super.finalize();
}

/**
The run function is called automatically from the constructors. It does the 
majority of the work of this class: sleeping for desired time, suspending the
secondary thread if desired, and performing the <B>performTimedAction</B>
function.
*/
@SuppressWarnings("deprecation")
public void run()
{
	String	routine="TimingThread.run";

	while( true ){
		try {
			Message.printDebug( 10, routine,
			"Sleeping for " + _sleep_time + " ms." );
			sleep( _sleep_time );
		}
		catch( InterruptedException e ){
			Message.printWarning( 1, routine,
			"Exception: " + e.toString() );
			return;
		}
		if( _thread != null ){
			Message.printDebug( 10, routine,
			"Suspending " + _thread );
			_thread.suspend();
		}

		Message.printDebug( 10, routine,
		"Performing timed action." );

		_timable.performTimedAction();

		Message.printDebug( 10, routine,
		"Done performing timed action." );

		if( _thread != null ){
			Message.printDebug( 10, routine,
			"Resuming " + _thread );
			_thread.resume();
		}
	}
}

private void setSleepTime( long sleep_time, int uflag )
	throws IllegalArgumentException
{
	String	routine="TimingThread.setSleepTime";

	if( sleep_time == 0 ){
		throw( 
		new IllegalArgumentException( "Zero Sleep Time!" ) );
	}

	if( uflag == MSECONDS ){
		// Just save as MS.
		_sleep_time	= sleep_time;
	}
	else if( uflag == SECONDS ){
		_sleep_time	= sleep_time*1000;
	}
	else if( uflag == MINUTES ){
		_sleep_time	= sleep_time*1000*60;
	}
	else {
		throw( new IllegalArgumentException(
		"Unrecognized units flag " + uflag + "." ) );
	}
	Message.printStatus( 1, routine,
	"Sleeping for " + _sleep_time + " milliseconds." );
}

}
