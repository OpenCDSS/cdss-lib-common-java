// ----------------------------------------------------------------------------
// IrregularTSIterator - used to iterate through irregular time series data
// ----------------------------------------------------------------------------
// History:
//
// 05 Jul 2000	Steven A. Malers, RTi	Copy TSDateIterator and update to have
//					basic needed functionality.
// 27 Jul 2000	Michael Thiemann, RTi   Use TSIterator as base class and derive
//					this class to improve performance.
// 28 Jul 2000	SAM, RTi		Clean up code to remove redundant
//					code in derived class.
// 11 Oct 2000	SAM, RTi		Port to Java from C++.
// 10 Sep 2001	SAM, RTi		Change iterator to be more similar to
//					C++, which was updated after the initial
//					port.  Fix a problem in IrregularTS that
//					impacted iteration.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2003-07-24	SAM, RTi		* Synchronize with C++ code.
//					* Have the constructors throw an
//					  Exception if the time series or dates
//					  are null.
//					* Add getDuration().
//					* Add finalize().
// 2005-09-14	SAM, RTi		Change _last_date_encountered to
//					_last_date_processed, as per the
//					TSIterator base class.
// 2005-09-16	SAM, RTi		* Add skeleton goTo(),
//					  goToNearestNext(),
//					  goToNearestPrevious().
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The IrregularTSIterator can be used to iterate through data in an IrregularTS.
It is used similarly to the TSIterator.  See TSIterator for examples of use.
*/
public class IrregularTSIterator extends TSIterator
{

/**
Construct an iterator for the full period of the time series.
@param ts Time series to iterate through.
@exception Exception if the time series or its start and end dates are null.
*/
public IrregularTSIterator ( IrregularTS ts )
throws Exception
{	super ( ts );
}	

/**
Construct an iterator for the given period of the time series.
@param ts Time series to iterate.
@param date1 First date to iterate.
@param date2 Last date to iterate.
@exception Exception if the time series or its start and end dates are null.
*/
public IrregularTSIterator ( IrregularTS ts, DateTime date1, DateTime date2 )
throws Exception
{	super ( ts, date1, date2 );
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	super.finalize();
}

/**
Return the duration of the data value in seconds.  This method is only defined
for the IrregularTSIterator and will return zero for regular TS.
@return Data value duration in seconds.
*/
public int getDuration ()
{ 	return 0;
}

// REVISIT SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  The date/time in the time series MUST exactly
match the date (dt.equals(...)).  If unable to go to the date/time, null is
returned.
@param dt Date/time to go to.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.
null IS ALWAYS RETURNED.
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.
WARNING:  the contents of this
object are volatile and change with each iteration.  Use the get*() methods in
TSIterator to retrieve data directly.
*/
public TSData goTo ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goTo",
	"The goTo() method is not yet implemented in IrregularTSIterator." );
	return null;
}

// REVISIT SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest next (future) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.
null IS ALWAYS RETURNED.
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.
WARNING:  the contents of this
object are volatile and change with each iteration.  Use the get*() methods in
TSIterator to retrieve data directly.
*/
public TSData goToNearestNext ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestNext",
	"The goToNearestNext() method is not yet implemented in " +
		"IrregularTSIterator." );
	return null;
}

// REVISIT SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest previous (past) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.
null IS ALWAYS RETURNED.
WARNING:  the contents of this
object are volatile and change with each iteration.  Use the get*() methods in
TSIterator to retrieve data directly.
*/
public TSData goToNearestPrevious ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestPrevious",
	"The goToNearestPrevious() method is not yet implemented in " +
		"IrregularTSIterator." );
	return null;
}

/**
Advance the iterator.  When called the first time, the initial values will be
returned.
@return null if the data is past the end or a pointer to an internal
TSData object containing the data for the current time step (WARNING:  the
contents of this object are volatile and change with each iteration).  Use the
get* methods in TSIterator to retrieve data directly.
*/
public TSData next ( )
{	int dl = 30;
	TSData theData = null;	// Use this for returns.

	if ( (_ts == null) || (_ts.getDataSize() == 0) ) {
		return null;
	}
	if ( _last_date_processed ) { 
		return null;
	}

	// We only want to advance the date if we have not already
	// gone past the end...

	if ( _first_date_processed ) {
		theData = _tsdata.getNext();                         	
	}
	else {	// SAM fixing some things 2001-09-10. Make look like C++ but
		// this old code is probably not a problem...
		// This returns a copy which has null pointers.???
		//_tsdata = _ts.getDataPoint(_current_date); 
		//theData  = _tsdata;
		// instead, loop the first time...
		List v = ((IrregularTS)_ts).getData();
		if ( (v == null) || (v.size() == 0) ) {
			return null;
		}
		int size = v.size();
		TSData ptr = null;
		for (	int i = 0; i < size; i++ ) {
			ptr = (TSData)v.get(i);
			if  ( ptr.getDate().equals(_current_date) ) {
				theData = ptr;
				break;
			}
		}
		v = null;
		ptr = null;
		_first_date_processed = true;
	}

	if ( theData == null ) {
		// We are at the end or have exceeded the
		// limits he data...
		_last_date_processed = true;
	}
	else {	_current_date = theData.getDate();
		if ( _current_date.greaterThan(_date2) ) {
			// We are further than we want to go ...
			_last_date_processed = true;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl,
				"IrregularTSIterator.next",
				"Have passed end date: " +
				_date2.toString() );
			}
		}
	}

	if ( _last_date_processed ) { 
		return null;
	}
	else {	_tsdata = theData;
		return theData;
	}
}

// REVISIT - need to add previous  also need to add clone().

} // End IrregularTSIterator
