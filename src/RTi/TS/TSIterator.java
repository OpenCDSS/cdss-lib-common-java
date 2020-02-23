// TSIterator - used to iterate through time series data

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

package RTi.TS;

import java.security.InvalidParameterException;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The TSIterator allows iteration through a regular-interval time series.
Use the IrregularTSIterator for irregular-interval time series.
In general, this should be transparent because the proper iterator will be created from the TS.iterator() method.
Use the iterator as follows:
<pre>
TS somets;	// Construct and initialize a time series or use an existing time series.
TSIterator tsi = somets.iterator ( somets.getDate1(), somets.getDate2() );
		// Can construct with any set of dates (ideally matching the
		// time series interval) or no dates, in which case the full
		// time series period will be used.
DateTime date;
double value;
TSData data;
for ( ; (data = tsi.next()) != null; ) {
	// The first call will set the pointer to the first data value in the period.
	// next() will return null when the last date in the processing period has been passed.
	date = tsi.getDate();
	value = tsi.getDataValue();
}
</pre>
Alternatively, operate on a TSData object as follows:
<pre>
TSData data;
for ( data = tsi.next(); data != null; data = tsi.next() ) {
	date = data.getDate();
	value = data.getData();
}
</pre>
The previous() method can be substituted for next() if iteration is to occur
backwards (in this case the date/times in the constructor are still specified with the earliest date/time first).

Calls to next() and previous() can be mixed to a degree, for example to navigate over leap years.
However, the iterator is intended to traverse primarily in one direction until the end is reached,
at which point the end of iteration is reached and null will be returned in subsequent calls to next() and previous().
*/
public class TSIterator
{

/**
Time series to iterate on.
*/
protected TS _ts = null;

/**
Interval base for time series, to optimize code.
*/
protected int _intervalBase = 0;

/**
Interval multiplier for time series, to optimize code.
*/
protected int _intervalMult = 0;

/**
Date/time to start iteration.
*/
protected DateTime _date1 = null;

/**
Date/time to end iteration.
*/
protected DateTime _date2 = null;

/**
Date/time for current position.
*/
protected DateTime _currentDate = null;

/**
Indicates whether the first date has been processed.
Only next() and previous() should change the values of this data member.
*/
//protected boolean _firstDateProcessed = false;
//protected boolean _firstDateProcessed2 = false;
protected boolean _nextWasCalledFirst = false;

/**
Indicates whether the last date has been processed.
Only next() and previous() should change the values of this data member.
*/
//protected boolean _lastDateProcessed = false;
//protected boolean _lastDateProcessed2 = false;
protected boolean _previousWasCalledFirst = false;

/**
 * Indicates if iteration is complete, one of:
 * _nextWasCalledFirst and have reached the biggest date/time.
 * _previousWasCalledFirst and have reached the smallest date/time.
 * Calls to next() or previous() once iteration is complete will always return null.
 */
protected boolean _isIterationComplete = false;

/**
The data object to return.  It is reused for each return to avoid numerous memory allocation operations.
*/
protected TSData _tsdata = null;

/**
Construct a time series iterator and set the period to be the full limits
of the time series data.  The current date/time is set to the first date/time
of the time series and will be returned by the first next() call.
@param ts Time series to iterate on.
@exception if the time series or date/times for the time series are null.
*/
public TSIterator ( TS ts )
throws Exception
{	initialize ();

	this._ts = ts;
	this._intervalBase = this._ts.getDataIntervalBase();
	this._intervalMult = this._ts.getDataIntervalMult();
	if ( ts == null ) {
		throw new Exception ( "Null time series for TSIterator" );
	}
	if ( ts.getDate1() == null ) {
		throw new Exception ( "Null starting date/time for TSIterator" );
	}
	if ( ts.getDate2() == null ) {
		throw new Exception ( "Null ending date/time for TSIterator" );
	}
	this._date1 = new DateTime(ts.getDate1());
	this._date2 = new DateTime(ts.getDate2());
	if ( this._date2.lessThan(this._date1) ) {
		throw new InvalidParameterException("The second TSIterator date/time is before the first.");
	}
	
	// Initialize the current time to the start, assuming next() will be called first.
	// - if previous() is called first, this._currentDate will be set to the this._date2.
	this._currentDate = new DateTime(this._date1);
}

/**
Construct and set the period for the iterator to the specified dates.
The current date/time for the iterator is set to the first specified date/time
and will be returned with the first call to next().
@param ts Time series to iterate on.
@param date1 First (earliest) date/time in period to iterate through.  If null, the first
date in the time series will be used.
@param date2 Last (latest) date/time in period to iterate through.  If null, the last
date in the time series will be used.
@exception if the time series or dates for the time series are null.
*/
public TSIterator ( TS ts, DateTime date1, DateTime date2 )
throws Exception
{	initialize ();

	if ( ts == null ) {
		throw new Exception ( "Null time series for TSIterator" );
	}
	if ( (date1 == null) && (ts.getDate1() == null) ) {
		throw new Exception ( "Null starting date/time for TSIterator" );
	}
	if ( (date2 == null) && (ts.getDate2() == null) ) {
		throw new Exception ( "Null ending date/time for TSIterator" );
	}
	this._ts = ts;
	this._intervalBase = _ts.getDataIntervalBase();
	this._intervalMult = _ts.getDataIntervalMult();

	if ( date1 != null ) {
		this._date1 = new DateTime (date1);
	}
	else {
	    this._date1 = new DateTime (ts.getDate1());
	}
	
	if ( date2 != null ) {
		this._date2 = new DateTime(date2);
	}
	else {
	    this._date2 = new DateTime ( ts.getDate2());
	}

	this._currentDate = new DateTime(this._date1);

	if ( this._date2.lessThan(this._date1) ) {
		throw new InvalidParameterException("The second TSIterator date/time is before the first.");
	}
}

/**
Copy constructor.
@param i Iterator to copy.
*/
/* TODO smalers 2020-02-22 Re-enable if needed
public TSIterator ( TSIterator i )
{	initialize();

	this._ts = i._ts; // Don't need to make a deep copy because this is meant to be a reference.
	this._intervalBase = i._intervalBase;
	this._intervalMult = i._intervalMult;
	this._isIterationComplete = i._isIterationComplete;
	this._date1 = (DateTime)i._date1.clone();
	this._date2 = (DateTime)i._date2.clone();
	this._currentDate = (DateTime)i._currentDate.clone();
}
*/

/**
Clone this TSIterator object.  The Object base class clone() method is called and then the TSIterator
objects are cloned.  The result is a complete deep copy.  The time series that is being iterated is
NOT cloned because only a reference is maintained by the TSIterator.
*/
public Object clone ()
{	try {
        // Clone the base class...
		TSIterator tsi = (TSIterator)super.clone();
		// Don't want to clone the TS reference, just set the reference...
		tsi._ts = this._ts;
		// Now clone the mutable objects...
		if ( this._date1 != null ) {
			tsi._date1 = (DateTime)this._date1.clone();
		}
		if ( _date2 != null ) {
			tsi._date2 = (DateTime)this._date2.clone();
		}
		if ( this._currentDate != null ) {
			tsi._currentDate = (DateTime)this._currentDate.clone();
		}
		return tsi;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Indicate if next() can be called to return data.
If false is returned, then the iterator is positioned at the last date with data.
This is less efficient than just calling next() or previous() and checking the return value for null.
*/
public boolean hasNext ()
{
	// For a regular time series, add one interval to the current date and see if
	// it is past the end of the iterator
	DateTime dt = new DateTime(this._currentDate);
	dt.addInterval(this._intervalBase, this._intervalMult);
	if ( dt.greaterThan(this._date2) ) {
		return false;
	}
	return true;
}

/**
Return the current state of the isLastDateProcessed flag.
If true, then the iterator has processed the last value in the requested period.
@return the current state of isLastDateProcessed flag for the iterator.
*/
public boolean isIterationComplete() {
    //return _lastDateProcessed;
    return this._isIterationComplete;
}

/**
Return the current state of the isLastDateProcessed flag.
If true, then the iterator has processed the last value in the requested period.
@return the current state of isLastDateProcessed flag for the iterator.
*/
/*
public boolean isLastDateProcessed() {
    //return _lastDateProcessed;
    return _lastDateProcessed2;
}
*/

/**
Return the current date/time for the iterator.
@return the current date/time for the iterator.
*/
public DateTime getDate ()
{	return this._currentDate;
}

/**
Return the data flag for the current date/time.
@return the data flag for the current date/time, taken from the current TSData.
*/
public String getDataFlag ()
{	return this._tsdata.getDataFlag();
}

/**
Return the data value for the current date/time.
@return the data value for the current date/time, taken from the current TSData.
*/
public double getDataValue ()
{ 	return this._tsdata.getDataValue ();
}

// TODO SAM 2005-09-14 Make this private for now since called only when data should be available.
// This method may be useful if public.
/**
Return the TSData for the current date/time of the iterator.
@return the TSData for the current date/time of the iterator.  WARNING:  the contents of this object are
volatile and change with each call.  Use the get*() methods in TSIterator to retrieve data directly prior
to making subsequent calls.
*/
private TSData getCurrentTSData ()
{	return this._ts.getDataPoint ( this._currentDate, this._tsdata );
}

/**
Return the reference to the time series being iterated.
@return reference to the TS.
*/
public TS getTS ()
{	return this._ts;
}

/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
The date/time in the time series MUST exactly match the date (dt.equals(date/time from time series) is called).
If unable to go to the date/time, null is returned.
@param dt Date/time to go to.
@param resetIfFail If true and the search fails, reset to the starting position when called, but still return null.
If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile and
change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData goTo ( DateTime dt, boolean resetIfFail )
{	DateTime date = null;
	TSData data = null;
	// If the iterator has not fully initialized, call next() once to force it...
	if ( ! this._nextWasCalledFirst && ! this._previousWasCalledFirst ) {
		next();
	}
	DateTime currentDateOrig = null;
	//boolean lastDateProcessedOrig = _lastDateProcessed;
	//boolean firstDateProcessedOrig = _firstDateProcessed;
	boolean isIterationCompleteOrig = this._isIterationComplete;
	if ( dt.equals(this._currentDate) ) {
		// Just return...
		return getCurrentTSData ();
	}
	else if ( dt.greaterThan(this._currentDate) ) {
		// Requested date/time is greater than current.
		// Need to move forward in time.
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails...
			if ( this._currentDate != null ) {
				currentDateOrig = new DateTime (this._currentDate);
			}
		}
		while ( (data = next()) != null ) {
			date = data.getDate();
			if ( dt.equals(date) ) {
				return data;
			}
			else if ( dt.lessThan(date) ) {
				// Have passed the requested date/time.
				break;
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			this._currentDate = currentDateOrig;
			//_firstDateProcessed = firstDateProcessedOrig;
			//_lastDateProcessed = lastDateProcessedOrig;
			this._isIterationComplete = isIterationCompleteOrig;
		}
		return null;
	}
	else {
	    // Requested date/time is earlier than the current date/time.  Need to move back in time...
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails...
			if ( _currentDate != null ) {
				currentDateOrig = new DateTime(this._currentDate);
			}
		}
		while ( (data = previous()) != null ) {
			date = data.getDate();
			if ( dt.equals(date) ) {
				return data;
			}
			else if ( dt.greaterThan(date) ) {
				// Have passed the requested date/time.
				break;
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			this._currentDate = currentDateOrig;
			//_firstDateProcessed = firstDateProcessedOrig;
			//_lastDateProcessed = lastDateProcessedOrig;
			this._isIterationComplete = isIterationCompleteOrig;
		}
		return null;
	}
}

/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest next (future) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
@param resetIfFail If true and the search fails, reset to the starting position when called, but still return null.
If false and the search fails, the current date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
/* TODO smalers 2020-02-22 re-enable if needed
public TSData goToNearestNext ( DateTime dt, boolean resetIfFail )
{	DateTime date = null;
	TSData data = null;
	// If the iterator has not fully initialized, call next() once to force it...
	if ( !_firstDateProcessed && !_lastDateProcessed ) {
		next();
	}
	DateTime currentDateOrig = null;
	boolean lastDateProcessedOrig = _lastDateProcessed;
	boolean firstDateProcessedOrig = _firstDateProcessed;
	if ( resetIfFail ) {
		// Save the starting conditions in case the search fails...
		if ( _currentDate != null ) {
			currentDateOrig = new DateTime ( _currentDate );
		}
	}
	if ( dt.equals(_currentDate) ) {
		// Just return...
		return getCurrentTSData ();
	}
	else if ( dt.greaterThanOrEqualTo(_currentDate) ) {
		// Requested date/time is greater than the iterator current time so need to move forward in time.
		while ( (data = next()) != null ) {
			date = data.getDate();
			if ( dt.greaterThan(date) ) {
				// Still not there...
				continue;
			}
			else {
			    // Have matched/passed the requested date/time...
				return data;
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
	else {
	    // Requested date/time is less than the current time so need to move back in time...
		while ( (data = previous()) != null ) {
			date = data.getDate();
			if ( dt.lessThan(date) ) {
				// Still not there...
				continue;
			}
			else if ( dt.equals(date) ) {
				// Have matched the requested date/time so return it...
				return data;
			}
			else {
			    // Have passed the requested date/time.  Return the next item (which should be after the
				// requested date/time...
				data = next();
				if ( data != null ) {
					return data;
				}
				else {
				    break;	// Return null below.
				}
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
}
*/

/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest previous (past) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
@param resetIfFail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
/* TODO smalers 2020-02-22 reenable if needed
public TSData goToNearestPrevious ( DateTime dt, boolean resetIfFail )
{	DateTime date = null;
	TSData data = null;
	// If the iterator has not fully initialized, call previous() once to force it...
	if ( !_firstDateProcessed && !_lastDateProcessed ) {
		previous();
	}
	DateTime currentDateOrig = null;
	boolean lastDateProcessedOrig = _lastDateProcessed;
	boolean firstDateProcessedOrig = _firstDateProcessed;
	if ( resetIfFail ) {
		// Save the starting conditions in case the search fails...
		if ( _currentDate != null ) {
			currentDateOrig = new DateTime ( _currentDate );
		}
	}
	if ( dt.equals(_currentDate) ) {
		// Just return...
		return getCurrentTSData ();
	}
	else if ( dt.greaterThan(_currentDate) ) {
		// Requested date/time is after current date/time.  Need to move forward in time.
		while ( (data = next()) != null ) {
			date = data.getDate();
			if ( dt.equals(date) ) {
				return data;
			}
			else if ( dt.lessThan(date) ) {
				// Moved one past so return the previous...
				data = previous();
				if ( data != null ) {
					return data;
				}
				else {
				    break;	// return null below.
				}
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
	else {
	    // Requested date/time is less than current date/time.  Need to move back in time...
		while ( (data = previous()) != null ) {
			date = data.getDate();
			if ( dt.lessThan(date) ) {
				// Still searching...
				continue;
			}
			else {
			    // Have matched/passed the requested date/time...
				return data;
			}
		}
		// If here the search failed.
		if ( resetIfFail ) {
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
}
*/

/**
Initialize data.
*/
private void initialize ()
{	this._ts = null;
	//_firstDateProcessed = false;
	//_lastDateProcessed = false;
	this._nextWasCalledFirst = false;
	this._previousWasCalledFirst = false;
	this._isIterationComplete = false;
	this._tsdata = new TSData();
}

/**
Advance the iterator one data point.  When called the first time, the first data
point will be returned.  This method is used to advance forward through a time series.
@return null if the time series has no data value or the date/time is past the
end.  If the previous situation does not apply, return a pointer to an internal
TSData object containing the data for the current time step (WARNING:  the
contents of this object are volatile and change with each iteration).  Use the
get*() methods in TSIterator to retrieve data directly.
*/
public TSData next ()
{	int dl = 30;

	// Clearly don't have any data so return null

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		return null;
	}

	// If iteration is already complete return null
	
	if ( this._isIterationComplete ) { 
		return null;
	}
	
	// First advance the current date/time.
	
	//if ( !_lastDateProcessed ) {
	if ( ! this._isIterationComplete ) {
		// Only advance the date if have not already gone past the end...
		if ( ! this._nextWasCalledFirst && ! this._previousWasCalledFirst ) {
			// next() or previous() has not yet been called - this is the first time so assume forward iteration
			//
			// It is possible that the date specified as input does
			// not exactly align with a date in the time series.
			// But it is adjusted and _currentDate is set in the constructor.
			//_firstDateProcessed = true;
	
			// this_.currentDate does not need to be advanced because it was set to the initial value in the constructor.
			this._nextWasCalledFirst = true;
		}
		else {
			// Either next() or previous() has been called previously and therefore can increment the date/time.
			// The following should not be used for IrregularTS.  However, IrregularTS now should
		    // return an IrregularTSIterator so the following should actually never get called
			// for IrregularTS.
			this._currentDate.addInterval (this._intervalBase, this._intervalMult);
		}
		if ( this._currentDate.greaterThan(this._date2) ) {
			// At the end or have exceeded the limits of the data...
			//_lastDateProcessed = true;
			this._isIterationComplete = true;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "TSIterator.next", "Have passed end date: " + this._date2 );
			}
		}
	}

	// Now return data or null.
	
	//if ( _lastDateProcessed ) {
	if ( this._isIterationComplete ) {
		// Have gone past the last date/time so return null
		return null;
	}
	else {
		// Still advancing through the period.
	    // Return data for the current date.  These are used by getDate, etc...
		return getCurrentTSData();
	}
}

/**
Decrement the iterator one data point.  When called the first time, the last
data point will be returned.  This method can be used to iterate backwards
through a time series.  The previous() method can be used with next().
Because the default construction of this class assumes that forward iteration
with next() will occur, the first call to previous() will reset the current
date/time to the end of the period (from the time series or as specified in
the constructor).  This occurs only if next() has not been called.
@return null if there are no data or the date/time is past the first date in the
iteration period.  If the previous situation does not apply, return a pointer
to an internal TSData object containing the data for the current time step
(WARNING:  the contents of this object are volatile and change with each
iteration).  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData previous ()
{	int dl = 30;

	// Clearly don't have any data so return null

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		return null;
	}

	// First advance the current date/time.

	//if ( !_firstDateProcessed ) {
	if ( ! this._isIterationComplete ) {
		// Only decrement the date if have not already gone past the beginning...
		//if ( !_firstDateProcessed && !_lastDateProcessed ) {
		if ( ! this._nextWasCalledFirst && ! this._previousWasCalledFirst ) {
			// Neither next() and previous() or have been called.
			//
			// Because at construction the current date/time is set
			// to the start because default behavior is forward iteration,
			// need to override here and set to the ending date/time.
			// Only do this if next() has not been called (in this case _first_date_processed will be false).

			// this_.currentDate needs to be set to the end date/time
			// (default was _date1 in constructor assuming that next() would be called first)
			this._currentDate = new DateTime ( this._date2 );
			// Also indicate that last date has been processed because backward iteration is initializing.
			//_lastDateProcessed = true;
			
			// Indicate that previous() was called first, indicating direction of iteration
			this._previousWasCalledFirst = true;
		}
		/* TODO smalers 2020-02-22 This does not seem to be needed and causes issue if next() and then previous() is called.
		else if ( !_lastDateProcessed ) {
			// next() has been called but previous() has not been called.
			// It is possible that the date specified as input does
			// not exactly align with a date in the time series.
			// But it is adjusted and _currentDate is set in the constructor.
			Message.printStatus(2, "", "next() has been called but previous() has not been called.");
			_lastDateProcessed = true;
		}
		*/
		else {
		    // Decrement the current date/time.
			// The following should not be used for IrregularTS.
			// However, IrregularTS now should return an
			// IrregularTSIterator so the following should actually never get called for IrregularTS.
			this._currentDate.addInterval (this._intervalBase, -this._intervalMult);
		}
		if ( this._currentDate.lessThan(this._date1) ) {
			// Are at the beginning or have exceeded the limits of the data...
			//_firstDateProcessed = true;
			this._isIterationComplete = true;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "TSIterator.previous", "Have passed start date: " + this._date1 );
			}
		}
	}

	// Now return data or null.

	//if ( _firstDateProcessed ) {
	if ( this._isIterationComplete ) {
		// FIXME SAM 2015-10-17 If next() is called to initialize iteration, then calling previous() always returns null
		// - I think I fixed this by updating the code to have more clear handling of next() and previous() calls but confirm with testing.
		return null;
	}
	else {
	    // Set the values.  These are used by getDate, etc...
		return getCurrentTSData();
	}
}

/**
Set the starting date/time for the iterator.  Use this to reset the start date.
The iterator will be reset and a call to next() will return the first value.
This is used in cases where iteration is jumping through the period,
for example processing a seasonal window in each year.
@param date1 New starting date/time for iterator.
*/
public void setBeginTime ( DateTime date1 )
{	this._date1 = new DateTime(date1);
	//_firstDateProcessed = false;
	//_lastDateProcessed = false;
	this._isIterationComplete = false;
	this._nextWasCalledFirst = false;
	this._previousWasCalledFirst = false;
	this._currentDate = new DateTime(_date1); // Default for next()
}

/**
Set the ending date/time for the iterator.
The iterator will be reset.  A call to next() will not return the first value
but will return the next value after the last value returned.
@param date2 New end date/time for iterator.
*/
/* TODO smalers 2020-02-22 this is confusing, just create a new iterator
public void setEndTime ( DateTime date2 )
{	_date2 = new DateTime(date2);
	//_lastDateProcessed = false;
	this._isIterationComplete = false;
}
*/

}