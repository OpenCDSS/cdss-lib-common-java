// IrregularTSIterator - used to iterate through irregular time series data

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.TS;

import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The IrregularTSIterator can be used to iterate through data in an IrregularTS.
It is used similarly to the TSIterator.  See TSIterator for examples of use.
*/
public class IrregularTSIterator extends TSIterator {

	/**
	 * The time series being iterated cast to IrregularTS,
	 * to avoid having to cast elsewhere.
	 */
	private IrregularTS irregularTS = null;

/**
Construct an iterator for the full period of the time series.
@param ts Time series to iterate through.
@exception Exception if the time series or its start and end dates are null.
*/
public IrregularTSIterator ( IrregularTS ts ) throws Exception {
	super ( ts );
	this.irregularTS = ts;
}

/**
Construct an iterator for the given period of the time series.
The given date/times do not need to exactly match a time series date/time.
The first call to next() or previous() will position the initial data pointer.
@param ts Time series to iterate.
@param date1 First date to iterate.
@param date2 Last date to iterate.
@exception Exception if the time series or its start and end dates are null.
*/
public IrregularTSIterator ( IrregularTS ts, DateTime date1, DateTime date2 )
throws Exception {
	super ( ts, date1, date2 );
	this.irregularTS = ts;
}

/**
Return the duration of the data value in seconds from the current TSData.
This method is only defined for the IrregularTSIterator and will return zero for regular TS.
@return Data value duration in seconds, or -1 if not defined.
*/
public int getDuration () {
	if ( this._tsdata != null ) {
		return this._tsdata.getDuration();
	}
	else {
		return -1;
	}
}

/**
 * Go to a data point in the time series data array.
 * This method coordinates setting the iterator current time to that of the data object.
 * This method is faster than searching for a date/time using the other version of 'goTo' function.
 * No check is done to ensure that the 'tsdata' is within the iterator period,
 * so should only pass in data within the period.
 * @param tsdata the TSData instance to go to.
 * This must be a reference to the original time series data, not a copy because the internal TSData pointer.
 * @return the input 'tsdata'
 */
public TSData goTo ( TSData tsdata ) { //, boolean resetIfFail ) {}
	int dl = 30;
	// If the iterator has not been initialized because next() or previous() has not been called:
	// - call next() once to force it
	// - iteration will always be forward
	if ( this.calledFirst == TSIteratorMoveType.UNKNOWN ) {
		next();
	}

	// Evaluate the direction of the move.
	if ( this._currentDate == null ) {
		// At an end point.
		if ( this.calledLast == TSIteratorMoveType.NEXT ) {
			// Must move backward so treat as if previous() was called.
			this.calledLast = TSIteratorMoveType.PREVIOUS;
		}
		else if ( this.calledLast == TSIteratorMoveType.PREVIOUS ) {
			// Must move forward so treat as if next() was called.
			this.calledLast = TSIteratorMoveType.NEXT;
		}
	}
	else {
		// Current date/time is not null.
		if ( this._currentDate.lessThan(tsdata.getDate()) ) {
			// Must move forward so treat as if next() was called.
			this.calledLast = TSIteratorMoveType.NEXT;
		}
		else if ( this._currentDate.greaterThan(tsdata.getDate()) ) {
			// Must move backward so treat as if previous() was called.
			this.calledLast = TSIteratorMoveType.PREVIOUS;
		}
	}
	// Reset the current date to that used in the TSData object.
	this._tsdata = tsdata;
	this._currentDate = tsdata.getDate();
	if ( Message.isDebugOn ) {
		Message.printStatus(dl, "IrregularTSIterator.goTo", "Moved iterator to: " + this._currentDate + " calledLast=" + this.calledLast);
	}
	// Data pointer should match what was passed in.
	return this._tsdata;
}

/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called to find the data.
The date/time in the time series MUST exactly match the date (dt.equals(date/time from time series) is called).
If unable to go to the date/time, null is returned and the iterator state will be the initial state before the call.
@param dt Date/time to go to.
@return the TSData for the requesting date/time or null if past the end of the iterator limits
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get*() methods in TSIterator to retrieve data directly once the iterator is positioned.
*/
public TSData goTo ( DateTime dt ) {
	DateTime date = null;
	TSData data = null;

	// Test equality and end points first since fast.
	if ( (this._currentDate != null) && dt.equals(this._currentDate) ) {
		// Requested date/time matches current date/time:
		// - no need to moves so return the current data pointer
		return this._tsdata;
	}
	else if ( dt.lessThan(this._date1) ) {
		// Requested date/time is less than the iterator start:
		// - don't move the data pointer and return null
		return null;
	}
	else if ( dt.greaterThan(this._date2) ) {
		// Requested date/time is greater than the iterator end:
		// - don't move the data pointer and return null
		return null;
	}

	// Save the original data used when resetting on failure:
	// - save the original pointer in case any other code is referencing
	// - OK if null
	TSData tsdataOrig = this._tsdata;
	DateTime currentDateOrig = this._currentDate;
	boolean isIterationCompleteOrig = this._isIterationComplete;

	// Create a new DateTime for iteration:
	// - make a copy of this._currentDate
	// - ok to reset this._tsdata because have saved a pointer
	//   (no need to make a copy because IrregularTS manages the data list)
	this._currentDate = new DateTime (this._currentDate);

	// If the iterator has not been initialized because next() or previous() has not been called:
	// - call next() once to force it
	// - iteration will always be forward
	if ( this.calledFirst == TSIteratorMoveType.UNKNOWN ) {
		next();
	}

	if ( dt.greaterThan(this._currentDate) ) {
		// Requested date/time is greater than current.
		// Need to move forward in time.

		// TODO smalers 2022-03-09 could use bisection or other method to locate faster.
		while ( (data = next()) != null ) {
			date = data.getDate();
			if ( dt.equals(date) ) {
				// OK to leave the new this._currentDate and this._tsdata in place and return.
				// Treat as if a next() was called.
				this.calledLast = TSIteratorMoveType.NEXT;
				return data;
			}
			else if ( dt.lessThan(date) ) {
				// Have passed the requested date/time without matching.  Quit searching.
				break;
			}
		}

		// If here the search failed:
		// - reset the data to the original

		this._tsdata = tsdataOrig;
		this._currentDate = currentDateOrig;
		this._isIterationComplete = isIterationCompleteOrig;
		return null;
	}
	else {
	    // Requested date/time is earlier than the current date/time.
		// Need to move back in time.

		// TODO smalers 2022-03-09 could use bisection or other method to locate faster.
		while ( (data = previous()) != null ) {
			date = data.getDate();
			if ( dt.equals(date) ) {
				// OK to leave the new this._currentDate and this._tsdata in place and return.
				// Treat as if a previous() was called.
				this.calledLast = TSIteratorMoveType.PREVIOUS;
				return data;
			}
			else if ( dt.greaterThan(date) ) {
				// Have passed the requested date/time.  Quit searching.
				break;
			}
		}

		// If here the search failed:
		// - reset the data to the original

		this._tsdata = tsdataOrig;
		this._currentDate = currentDateOrig;
		this._isIterationComplete = isIterationCompleteOrig;
		return null;
	}
}

// TODO smalers 2023-07-10 remove this?
/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
The date/time in the time series MUST exactly match the date as evaluated by DateTime.equals().
If unable to go to the date/time, null is returned and the current time is set.
@param dt Date/time to go to.
@return the TSData for the requesting date/time or null if the DateTime cannot be exactly matched.
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData x_goTo ( DateTime dt ) {
	DateTime date = null;
	TSData data = null;
	String routine = null;
	boolean resetIfFail = true; // Always use this behavior.
	int dl = 1;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".goTo";
		Message.printDebug(dl, routine, "Request to position iterator at: " + dt);
	}
	// If the iterator has not fully initialized, call next() once to force it.
	if ( this.calledFirst == TSIteratorMoveType.UNKNOWN ) {
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "First attempt to get data.  Calling next() to force data initialization.");
		}
		next();
	}
	DateTime currentDateOrig = null;
	TSData tsdataOrig = null;
	//boolean lastDateProcessedOrig = _lastDateProcessed;
	//boolean firstDateProcessedOrig = _firstDateProcessed;
	boolean isIterationCompleteOrig = this._isIterationComplete;
	if ( dt.equals(this._currentDate) ) {
		// Just return.
		// TODO smalers 2022-03-07 the following is for regular interval time series.
		//return getCurrentTSData ();
		// The following is for irregular interval time series.
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "  Already at the requested date/time=" + dt +
				", currentDate=" + this._currentDate + " _tsdata.date=" + this._tsdata.getDate());
		}
		return this._tsdata;
	}
	else if ( dt.greaterThan(this._currentDate) ) {
		// Requested date/time is greater than current.
		// Need to move forward in time.
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails.
			if ( this._currentDate != null ) {
				currentDateOrig = new DateTime (this._currentDate);
				// For irregular time series the data is a reference to time series data.
				tsdataOrig = this._tsdata;
			}
		}
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "  Need to search forward because request=" + dt + " is after current=" + this._currentDate);
		}
		while ( true ) {
			data = next();
			if ( data == null ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Null returned from next().");
				}
				break;
			}
			date = data.getDate();
			if ( Message.isDebugOn ) {
				Message.printDebug(dl, routine, "  Moving forward, checking requested=" + dt + " against " + date );
			}
			if ( dt.equals(date) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Moving forward, found the requested date/time=" + dt +
						", currentDate=" + this._currentDate + " _tsdata.date=" + this._tsdata.getDate());
				}
				return data;
			}
			else if ( dt.lessThan(date) ) {
				// Have passed the requested date/time.
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Have moved past the requested date/time=" + dt + " without finding a match." );
				}
				break;
			}
			// Else, continue searching.
		}
		// If here the search failed.
		if ( resetIfFail ) {
			this._currentDate = currentDateOrig;
			this._tsdata = tsdataOrig;
			//_firstDateProcessed = firstDateProcessedOrig;
			//_lastDateProcessed = lastDateProcessedOrig;
			this._isIterationComplete = isIterationCompleteOrig;
		}
		return null;
	}
	else {
	    // Requested date/time is earlier than the current date/time.  Need to move back in time.
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails.
			if ( _currentDate != null ) {
				currentDateOrig = new DateTime(this._currentDate);
				// For irregular time series the data is a reference to time series data.
				tsdataOrig = this._tsdata;
			}
		}
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "  Need to search backward because request=" + dt + " is before current=" + this._currentDate);
		}
		while ( true ) {
			data = previous();
			if ( data == null ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Null returned from previous().");
				}
				break;
			}
			date = data.getDate();
			if ( Message.isDebugOn ) {
				Message.printDebug(dl, routine, "  Moving backward, checking requested=" + dt + " against " + date );
			}
			if ( dt.equals(date) ) {
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Moving backward, found the requested date/time=" + dt +
						", currentDate=" + this._currentDate + " _tsdata.date=" + this._tsdata.getDate());
				}
				return data;
			}
			else if ( dt.greaterThan(date) ) {
				// Have passed the requested date/time.
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Have moved past the requested date/time=" + dt + " without finding a match." );
				}
				break;
			}
			// Else, continue searching.
		}
		// If here the search failed.
		if ( resetIfFail ) {
			this._currentDate = currentDateOrig;
			this._tsdata = tsdataOrig;
			//_firstDateProcessed = firstDateProcessedOrig;
			//_lastDateProcessed = lastDateProcessedOrig;
			this._isIterationComplete = isIterationCompleteOrig;
		}
		return null;
	}
}

// TODO smalers 2023-07-10 remove this?
/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
The date/time in the time series MUST exactly match the date (dt.equals(...)).
If unable to go to the date/time, null is returned.
@param dt Date/time to go to.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
@param resetIfFail If true and the search fails, reset to the starting position (before the method call), but still return null.
If false and the search fails, the current date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData x_goTo ( DateTime dt, boolean resetIfFail ) {
	// TODO smalers 2022-03-07 enhance to have more advanced searching such as bisection:
	// - for now assume that the requested date/time is near the current,
	//   such as repositioning in the neighborhood
	TSData tsdata = null;
	TSData tsdata0 = this._tsdata; // Initial iterator position.
	String routine = null;
	int dl = 1;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".goTo";
		Message.printDebug(dl, routine, "Request to position iterator at: " + dt);
	}
	if ( this._currentDate == null ) {
		// Perhaps the iterator has not been initialized yet so reset data and call next().
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "Iterator is at null.  Reinitializing and calling next().");
		}
		this.calledFirst = TSIteratorMoveType.UNKNOWN;
		this._isIterationComplete = false;
		tsdata = this.next();
		if ( tsdata == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug(dl, routine, "  Iterator after calling next() has null data.");
			}
			if ( resetIfFail ) {
				this._tsdata = tsdata0;
			}
			return null;
		}
	}
	if ( dt.equals(this._currentDate) ) {
		// Already at the requested date/time.
		if ( Message.isDebugOn ) {
			Message.printDebug(dl, routine, "  Already at the requested date/time, currentDate=" + this._currentDate + " _tsdata.date=" + this._tsdata.getDate());
		}
		return this._tsdata;
	}
	else if ( dt.greaterThan(this._currentDate) ) {
		// Requested date/time is after the current so loop with "next" method.
		while ( true ) {
			tsdata = next();
			if ( tsdata == null ) {
				// Ran out of data.
				if ( resetIfFail ) {
					this._tsdata = tsdata0;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Ran out of data searching forward to the end.");
				}
				return null;
			}
			else if ( tsdata.getDate().equals(dt) ) {
				// Found a match:
				// - set the current pointer
				// - return the TSData object
				this._tsdata = tsdata;
				this._currentDate = tsdata.getDate();
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Found the requested date/time: " + this._currentDate);
				}
				return tsdata;
			}
			else if ( tsdata.getDate().greaterThan(dt) ) {
				// Did not find an exact match and went past.
				if ( resetIfFail ) {
					this._tsdata = tsdata0;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Went past the request searching forward, maybe a precision issue.");
				}
				return null;
			}
		}
	}
	else {
		// Requested date/time is before the current so loop with "previous" method.
		tsdata = null;
		while ( true ) {
			tsdata = previous();
			if ( tsdata == null ) {
				// Ran out of data.
				if ( resetIfFail ) {
					this._tsdata = tsdata0;
				}
				return null;
			}
			else if ( tsdata.getDate().equals(dt) ) {
				// Found a match:
				// - set the current pointer
				// - return the TSData object
				this._tsdata = tsdata;
				this._currentDate = tsdata.getDate();
				return tsdata;
			}
			else if ( tsdata.getDate().greaterThan(dt) ) {
				// Did not find an exact match and went past.
				if ( resetIfFail ) {
					this._tsdata = tsdata0;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug(dl, routine, "  Went past the request searching backward, maybe a precision issue.");
				}
				return null;
			}
		}
	}
}

// TODO SAM 2005-09-16 Remove the warning when implemented.
/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
If an exact match for the requested date/time cannot be made, return the nearest next (future) data.
Return null if the search cannot find a matching date/time (e.g., due to the end of the period).
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
// TODO smalers 2022-03-09 implement if needed, otherwise remove.
/*
public TSData goToNearestNext ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestNext",
	"The goToNearestNext() method is not yet implemented in IrregularTSIterator." );
	return null;
}
*/

// TODO SAM 2005-09-16 Remove the warning when implemented.
/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
If an exact match for the requested date/time cannot be made, return the nearest previous (past) data.
Return null if the search cannot find a matching date/time (e.g., due to the end of the period).
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get*() methods in TSIterator to retrieve data directly.
*/
// TODO smalers 2022-03-09 implement if needed, otherwise remove.
/*
public TSData goToNearestPrevious ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestPrevious",
	"The goToNearestPrevious() method is not yet implemented in IrregularTSIterator." );
	return null;
}
*/

/**
Indicate if next() can be called to return data.
If false is returned, then the iterator is positioned at the last date with data.
*/
public boolean hasNext () {
	// Need to make sure that next() or previous() have been called to initialize the data.
	if ( this.calledFirst == TSIteratorMoveType.UNKNOWN ) {
		throw new RuntimeException ( "hasNext() can only be called after next() or previous() are called at least once.");
	}

	// For an irregular time series, get the date/time for the next value and check whether it is past _date2.
	if ( this._tsdata == null ) {
		if ( this.calledLast == TSIteratorMoveType.NEXT) {
			// next() was called last and no more data
			return false;
		}
		else {
			// previous() was called last:
			// - null indicates have reached the start of the iteration period
			// - could have data at the start so search forward
			List<TSData> tsdataList = this.irregularTS.getData();
			if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
				// No data.
				return false;
			}
			for ( TSData ptr : tsdataList ) {
				// Because irregular time series may not exactly match, use >= to find the first date/time.
				// For example, may be computing statistics on a regular interval but irregular interval data don't
				// align with those intervals.
				if ( ptr.getDate().greaterThanOrEqualTo(this._date1) && ptr.getDate().lessThanOrEqualTo(this._date2) ) {
					return true;
				}
			}
		}
	}
	TSData nextData = this._tsdata.getNext();
	if ( nextData == null ) {
		// There is no next point.
		return false;
	}
	else {
		// Have a next data point.
		DateTime dt = nextData.getDate();
		if ( dt == null ) {
			// Next point does not have a date.
			return false;
		}
		else if ( dt.greaterThan(this._date2) ) {
			// Next point is past the end.
			return false;
		}
	}

	// Fall through is that do have a data point.
	return true;
}

/**
Move the iterator forward.
When called the first time, the initial value will be returned as follows:
<ul>
<li>If no period was specified in the constructor, return the first data point in the time series
    (should not happen since the default is to use the time series period).</li>
<li>If a period was specified in the constructor, return the first data point >= the start time.</li>
</ul>
Subsequent calls will advance the data pointer.
If the data pointer is null and the last call was to previous(), set to to the first data value in the iteration period.
@return null if the data is past the end or a pointer to an internal
TSData object containing the data for the current time step (may contain missing data)
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get* methods in TSIterator to retrieve data directly.
*/
public TSData next ( ) {
	int dl = 30;
	//dl = 1; // Use higher debug level unless troubleshooting.
	TSData theData = null; // TSData if time series list has valid next value.
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".next";
	}

	// Set next() as the last call.
	this.calledLast = TSIteratorMoveType.NEXT;

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		// Clearly no data available so return null.
		return null;
	}

	// Handle null data cases.
	if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
		// Data was previous initialized.
		if ( this._tsdata == null ) {
			// No data since reached an end of the iteration period.
			if ( this.calledLast == TSIteratorMoveType.NEXT ) {
				// next() was called last:
				// - no more data.
				this._currentDate = null;
				return null;
			}
			else {
				// previous() was called last:
				// - attempt to reset to the first point in the iteration period
				reset();
				return next();
			}
		}
		else if ( this._tsdata.getNext() == null ) {
			// Current data is not null.
			// Cannot advanced the pointer since no next data point.
			this._tsdata = null;
			this._currentDate = null;
			return null;
		}
	}

	// If here this._tsdata and this._tsdata.next are not null.
	// Move the current date/time forward.

	if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
		// Have previously called next() or previous() so initialization has occurred:
		// - data is the next item in the list
		// - it may be after the iteration end but check that below
		theData = this._tsdata.getNext();
	}
	else {
		// This is the first call to next() so need to find the first data point:
		// - if no period was specified, return the first value
		//   (should not occur since default is time series full period)
		// - if a period was specified, return the first point >= the start
		this.calledFirst = TSIteratorMoveType.NEXT;
		List<TSData> tsdataList = this.irregularTS.getData();
		if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
			// No data.
			theData = null;
		}
		for ( TSData ptr : tsdataList ) {
			// Because irregular time series may not exactly match, use >= to find the first date/time.
			// For example, may be computing statistics on a regular interval but irregular interval data don't
			// align with those intervals.
			if ( ptr.getDate().greaterThanOrEqualTo(this._date1) && (ptr.getDate().lessThanOrEqualTo(this._date2))) {
				theData = ptr;
				break;
			}
		}
	}

	if ( theData == null ) {
		// Have called next() and reached the end of the data list:
		// - make sure that no data are available for calling code
		this._tsdata = null;
		this._currentDate = null;
	}
	else {
		// Have some data to return:
		// - set the current date/time consistent with regular interval iterator
		// - may be outside the iterator period
		DateTime theDataDate = theData.getDate();
		if ( (theDataDate == null) || theDataDate.greaterThan(this._date2) ) {
			// Have gone past the iterator end date/time:
			// - this is not allowed
			// - set values to null to ensure no data are returned
			this._tsdata = null;
			this._currentDate = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Have passed the iterator end date: " + _date2 );
			}
		}
		else {
			// Have data in the iterator period.
			this._tsdata = theData;
			this._currentDate = theData.getDate();
		}
	}

	// Return the data, may be null.
	if ( Message.isDebugOn ) {
		if ( this._tsdata == null ) {
			Message.printDebug ( dl, routine, "this._tsdata is null" );
		}
	}
	return this._tsdata;
}

/**
Move the iterator backward.
When called the first time, the initial value will be returned as follows:
<ul>
<li>If no period was specified in the constructor, return the last data point in the time series
    (should not happen since default is to use the time series period).</li>
<li>If a period was specified in the constructor, return the last data point <= the iteration start time.</li>
</ul>
Subsequent calls will advance the data pointer.
If the data pointer is null and the last call was to next(), set to to the last data value in the iteration period.
@return null if the data is past the start or a pointer to an internal
TSData object containing the data for the current time step (may contain missing data)
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get* methods in TSIterator to retrieve data directly.
*/
public TSData previous ( ) {
	int dl = 30;
	//dl = 1; // Use higher debug level unless troubleshooting.
	TSData theData = null; // TSData if time series list has valid previous value.
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".previous";
	}

	// Set previous() as the last call.
	this.calledLast = TSIteratorMoveType.PREVIOUS;
	if ( Message.isDebugOn ) {
		Message.printDebug(dl,routine,"In previous(), currentDate = " + this._currentDate + ", calledFirst = " + this.calledFirst);
	}

	// Clearly no data available so return null.

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug(dl,routine,"  Iterator time series data data are null.");
		}
		return null;
	}

	// Handle cases where return value would involves evaluating null.
	if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
		// The data pointer was previously initialized so data pointers should be initialized.
		if ( Message.isDebugOn ) {
			Message.printDebug(dl,routine,"  Iterator data have been previously initialized.");
		}
		if ( this._tsdata == null ) {
			// Previous call reached the end of data on either end of the iteration period.
			if ( this.calledLast == TSIteratorMoveType.PREVIOUS ) {
				// previous() was called last:
				// - still no more data.
				if ( Message.isDebugOn ) {
					Message.printDebug(dl,routine,"  Iterator data is null and previous() was called last - no more data at start.");
				}
				this._currentDate = null;
				return null;
			}
			else if ( this.calledLast == TSIteratorMoveType.NEXT ) {
				// next() was called last:
				// - ran out of data on the end
				// - attempt to reset to the last point with data
				// - keep the original 'calledFirst' because calling previous() below will set to PREVIOUS
				reset();
				// The following will get the last point.
				TSIteratorMoveType moveSave = this.calledFirst;
				// The following will reposition the internal data pointer.
				theData = previous();
				this.calledFirst = moveSave;
				if ( Message.isDebugOn ) {
					Message.printDebug(dl,routine,"  Iterator data is null and next() ws called last - back up to end: " + this._currentDate);
				}
				return theData;
			}
			else {
				// Should not happen if calledFirst != UNKNOWN.
				String message = "Error in code.  TSIteratorMoveType is UNKNOWN.  Should not happen.";
				Message.printWarning ( 3, routine, message );
				throw new RuntimeException ( message );
			}
		}
		else if ( this._tsdata.getPrevious() == null ) {
			// Current data is not null but previous is.
			// Cannot advance the pointer since no previous data point.
			this._tsdata = null;
			this._currentDate = null;
			if ( Message.isDebugOn ) {
				Message.printDebug(dl,routine,"  Iterator data is not null but previous() is null - returning null since at start.");
			}
			return null;
		}
	}

	// If here data are initialized and can move the current date/time backward:
	// - start by getting the data pointer 'theData' for previous

	if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
		// Have previously called next() or previous() so initialization has occurred:
		// - data is the previous item in the list
		// - it may be before the iteration start but check that below
		theData = this._tsdata.getPrevious();
	}
	else {
		// This is the first call to previous() (and next() has not been called) so need to find the last data point:
		// - if no period was specified, return the last value
		//   (should not occur since default is time series full period)
		// - if a period was specified, return the last point <= the start
		this.calledFirst = TSIteratorMoveType.PREVIOUS;
		List<TSData> tsdataList = this.irregularTS.getData();
		if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
			// No data.
			theData = null;
		}
		TSData ptr;
		for ( int i = tsdataList.size() - 1; i >= 0; i-- ) {
			ptr = tsdataList.get(i);
			// Because irregular time series may not exactly match, use <= to find the first date/time.
			// For example, may be computing statistics on a regular interval but irregular interval data don't
			// align with those intervals.
			if ( ptr.getDate().greaterThanOrEqualTo(this._date1) && (ptr.getDate().lessThanOrEqualTo(this._date2))) {
				theData = ptr;
				break;
			}
		}
	}

	if ( theData == null ) {
		// Have called previous() and reached the end of the data list:
		// - make sure that no data are available for calling code
		this._tsdata = null;
		this._currentDate = null;
	}
	else {
		// Have some data to return:
		// - set the current date/time consistent with regular interval iterator
		// - may be outside the iterator period
		DateTime theDataDate = theData.getDate();
		if ( (theDataDate == null) || theDataDate.lessThan(this._date1) ) {
			// Have gone past the iterator start date/time:
			// - this is not allowed
			// - set values to null to ensure no data are returned
			this._tsdata = null;
			this._currentDate = null;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "  Have passed the iterator start dates (" + _date1 +") setting data and current to null." );
			}
		}
		else {
			// Have data in the iterator period.
			this._tsdata = theData;
			this._currentDate = theData.getDate();
		}
	}

	// Return the data, may be null.
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "  Iterator moved to previous: " + this._currentDate );
	}
	return this._tsdata;
}

// TODO - need to add previous also need to add clone().

/**
Get the previous data value.
This simply returns the pointer TSData.getPrevious() for the irregular time series data list.
@return null if the data is past the end or a pointer to an internal
TSData object containing the data for the current time step
(WARNING:  the contents of this object are volatile and change with each iteration).
Use the get* methods in TSIterator to retrieve data directly.
*/
public TSData x_previous () {
	int dl = 30;
	TSData theData = null; // Use this for returns.

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		// Clearly don't have any data so return null.
		return null;
	}

	if ( ! this._isIterationComplete ) {
		// Only decrement the date if have not already gone past the beginning.
		//if ( !_firstDateProcessed && !_lastDateProcessed ) {
		if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
			// Have previously called next() or previous() so initialization has occurred:
			// - just return the previous item in the list, which is very fast
			theData = this._tsdata.getPrevious();
		}
		else {
			// This is the first call to previous() so need to find the last data point:
			// - if no period was specified, return the last value
			// - if a period was specified, return the last point <= the end
			List<TSData> tsdataList = this.irregularTS.getData();
			if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
				// No data.
				return null;
			}
			int size = tsdataList.size();
			TSData ptr = null;
			for ( int i = (size - 1); i >= 0; i-- ) {
				ptr = tsdataList.get(i);
				// this._currentDate is set in the constructor as the initial date/time as requested or from the time series.
				// Because irregular time series may not exactly match, use >= to find the first value.
				// For example, may be computing statistics on a regular interval but irregular interval data don't
				// align with those intervals.
				// Old code
				// - TODO smalers 2020-08-04 remove when tested out
				//if ( ptr.getDate().equals(this._currentDate) ) { // }
				if ( ptr.getDate().lessThanOrEqualTo(this._currentDate) ) {
					theData = ptr;
					break;
				}
			}
			// TODO smalers 2020-08-04 does this need to check whether this._prevWasCalledFirst?
			this.calledFirst = TSIteratorMoveType.PREVIOUS;
		}

		if ( theData == null ) {
			// Have called previous() and reached the end of the data list:
			// - can call previous() past the end of the requested period - does not return null in that case
			//_lastDateProcessed = true;
			this._isIterationComplete = true;
		}
		else {
			// Have some data to return.
	    	this._currentDate = theData.getDate();
			if ( this._currentDate.lessThan(this._date1) ) {
				// Have gone past the requested start date/time.
				//_lastDateProcessed = true;
				this._isIterationComplete = true;
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, "IrregularTSIterator.previous", "Have passed starting date: " + _date1 );
				}
			}
		}
	}

	if ( this._isIterationComplete ) {
		return null;
	}
	else {
	    this._tsdata = theData;
		return theData;
	}
}

// TODO smalers 2022-03-07 evaluate if this needs to be more complex.
/**
Get the previous data value.
This simply returns the pointer TSData.getPrevious() for the irregular time series data list.
@return null if the data is past the end or a pointer to an internal
TSData object containing the data for the current time step
(WARNING:  the contents of this object are volatile and change with each iteration).
Use the get* methods in TSIterator to retrieve data directly.
*/
public TSData x2_previous () {
	int dl = 30;
	TSData theData = null; // Use this for returns.

	// Set previous() as the last call.
	this.calledLast = TSIteratorMoveType.PREVIOUS;

	boolean doSimple = true;
	if ( doSimple ) {
		if ( (this._tsdata == null) ) {
			return null;
		}
		else {
			theData = this._tsdata.getPrevious();
			if ( theData == null ) {
				//this._currentDate = null;
				return null;
			}
			else {
				// Position the data at previous.
				this._tsdata = theData;
				this._currentDate = this._tsdata.getDate();
			}
			return theData;
		}
	}

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		// Clearly don't have any data so return null.
		return null;
	}

	if ( ! this._isIterationComplete ) {
		// Only decrement the date if have not already gone past the beginning.
		//if ( !_firstDateProcessed && !_lastDateProcessed ) {
		if ( this.calledFirst != TSIteratorMoveType.UNKNOWN ) {
			// Have previously called next() or previous() so initialization has occurred:
			// - just return the previous item in the list, which is very fast
			theData = this._tsdata.getPrevious();
		}
		else {
			// This is the first call to previous() so need to find the last data point:
			// - if no period was specified, return the last value
			// - if a period was specified, return the last point <= the end
			List<TSData> tsdataList = this.irregularTS.getData();
			if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
				// No data.
				return null;
			}
			int size = tsdataList.size();
			TSData ptr = null;
			for ( int i = (size - 1); i >= 0; i-- ) {
				ptr = tsdataList.get(i);
				// this._currentDate is set in the constructor as the initial date/time as requested or from the time series.
				// Because irregular time series may not exactly match, use >= to find the first value.
				// For example, may be computing statistics on a regular interval but irregular interval data don't
				// align with those intervals.
				// Old code
				// - TODO smalers 2020-08-04 remove when tested out
				//if ( ptr.getDate().equals(this._currentDate) ) {
				if ( ptr.getDate().lessThanOrEqualTo(this._currentDate) ) {
					theData = ptr;
					break;
				}
			}
			// TODO smalers 2020-08-04 does this need to check whether this._prevWasCalledFirst?
			this.calledFirst = TSIteratorMoveType.PREVIOUS;
		}

		if ( theData == null ) {
			// Have called previous() and reached the end of the data list:
			// - can call previous() past the end of the requested period - does not return null in that case
			//_lastDateProcessed = true;
			this._isIterationComplete = true;
		}
		else {
			// Have some data to return.
	    	this._currentDate = theData.getDate();
			if ( this._currentDate.lessThan(this._date1) ) {
				// Have gone past the requested start date/time.
				//_lastDateProcessed = true;
				this._isIterationComplete = true;
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, "IrregularTSIterator.previous", "Have passed starting date: " + _date1 );
				}
			}
		}
	}

	if ( this._isIterationComplete ) {
		return null;
	}
	else {
	    this._tsdata = theData;
		return theData;
	}
}

/**
 * Reset the iterator to initial conditions.
 * The next() or previous() method should be called to begin iterating.
 */
public void reset () {
	super.reset();
}

}