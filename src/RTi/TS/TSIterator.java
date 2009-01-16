// ----------------------------------------------------------------------------
// TSIterator - used to iterate through time series data
// ----------------------------------------------------------------------------
// History:
//
// 05 Jul 2000	Steven A. Malers, RTi	Copy TSDateIterator and update to have
//					basic needed functionality.
// 27 Jul 2000	Michael Thiemann, RTi   Use TSIterator as base class and use
//					as the default for regular time series
//					but create the IrregularTSIterator
//					class to handle irregular time series.
//					The base class will work with irregular
//					time series, but is slower.
// 11 Oct 2000	SAM, RTi		Port to java.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
//					Fix some potential bugs where internal
//					dates where not being set to copies
//					(therefore the original data may have
//					been getting modified).
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2003-07-24	SAM, RTi		* Minor cleanup to synchronize with C++.
//					* Make protected data members use full
//					  javadoc.
//					* Add more examples to the class
//					  summary to illustate use of the
//					  iterator.
//					* Throw Exception in the constructor
//					  to more explicitly handle null time
//					  series and dates.
//					* In the copy constructor, clone the
//					  DateTime objects (previously just set
//					  the reference).
//					* Remove checks for year=0.  DateTime
//					  robustly handles creation, etc and
//					  should only have year=0 for valid data
//					  (e.g., monthly average time series).
//					* Remove call to TS.getDataDate() - this
//					  method was originally implemented for
//					  iteration other than TSIterator, which
//					  never panned out.  The method does not
//					  do anything anyhow as called.
//					* Add previous() as per the C++ code.
//					* Move getDuration() to
//					  IrregularTSIterator() since it does
//					  not make sense for regular interval
//					  time series.
// 2005-09-14	SAM, RTi		* Enable the previous() method.
//					* Add goTo(), goToNearestNext(),
//					  goToNearestPrevious().
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The TSIterator allows iteration through a regular-interval time series.  Use the
IrregularTSIterator for irregular-interval time series.  In general,
this should be transparent because the proper iterator will be created from the TS.iterator() method.
Use the iterator as follows:
<pre>
TS somets;	// Construct and initialize a time series or use an
		// existing time series.
TSIterator tsi = somets.iterator ( somets.getDate1(), somets.getDate2() );
		// Can construct with any set of dates (ideally matching the
		// time series interval) or no dates, in which case the full
		// time series period will be used.
DateTime date;
double value;
TSData data;
for ( ; (data = tsi.next()) != null; ) {
			// The first call will set the pointer to the
			// first data value in the period.  next() will return
			// null when the last date in the processing period
			// has been passed.
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
backwards (in this case the date/times are still specified with the earliest date/time first).
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
protected boolean _firstDateProcessed = false;

/**
Indicates whether the last date has been processed.
Only next() and previous() should change the values of this data member.
*/
protected boolean _lastDateProcessed = false;

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

	_ts = ts;
	_intervalBase = _ts.getDataIntervalBase();
	_intervalMult = _ts.getDataIntervalMult();
	if ( ts == null ) {
		throw new Exception ( "Null TS for TSIterator" );
	}
	if ( ts.getDate1() == null ) {
		throw new Exception ( "Null TS.date1 for TSIterator" );
	}
	if ( ts.getDate2() == null ) {
		throw new Exception ( "Null TS.date2 for TSIterator" );
	}
	_date1 = new DateTime(ts.getDate1());
	_date2 = new DateTime(ts.getDate2());
	_currentDate = new DateTime(_date1);
}

/**
Construct and set the period for the iterator to the specified dates.
The current date/time for the iterator is set to the first specified date/time
and will be returned with the first call to next().
@param ts Time series to iterate on.
@param date1 First date/time in period to iterate through.  If null, the first
date in the time series will be used.
@param date2 Last date/time in period to iterate through.  If null, the last
date in the time series will be used.
@exception if the time series or dates for the time series are null.
*/
public TSIterator ( TS ts, DateTime date1, DateTime date2 )
throws Exception
{	initialize ();

	if ( ts == null ) {
		throw new Exception ( "Null TS for TSIterator" );
	}
	if ( (date1 == null) && (ts.getDate1() == null) ) {
		throw new Exception ( "Null TS.date1 for TSIterator" );
	}
	if ( (date2 == null) && (ts.getDate2() == null) ) {
		throw new Exception ( "Null TS.date2 for TSIterator" );
	}
	_ts = ts;
	_intervalBase = _ts.getDataIntervalBase();
	_intervalMult = _ts.getDataIntervalMult();

	if ( date1 != null ) {
		_date1 = new DateTime (date1);
	}
	else {
	    _date1 = new DateTime (ts.getDate1());
	}
	
	if ( date2 != null ) {
		_date2 = new DateTime(date2);
	}
	else {
	    _date2 = new DateTime ( ts.getDate2());
	}

	_currentDate = new DateTime(_date1);

	// It is possible that the date passed in does not agree with a date
	// in the time series.  For the initialization of the iterator, we want
	// the _current_date to be an actual date in the data...
	// TODO SAM 2009-01-15 Evaluate code
	if ( Message.isDebugOn ) {
		Message.printDebug ( 50, "TSIterator", "Requested start date is " + _currentDate.toString() );
	}
	if ( _currentDate.greaterThan(_ts.getDate2()) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, "TSIterator", "Initialized Current Date " + _currentDate.toString() +
			" is larger than last Date of TS " + _ts.getDate2().toString() + ". TS has only one value!" );
		}
		_currentDate = new DateTime (_ts.getDate2());
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 50, "TSIterator", "After check, start date changed to " + _currentDate.toString() );
	}
}

/**
Copy constructor.
@param i Iterator to copy.
*/
public TSIterator ( TSIterator i )
{	initialize();

	_ts = i._ts; // Don't need to make a deep copy because this is meant to be a reference.
	_intervalBase = i._intervalBase;
	_intervalMult = i._intervalMult;
	_date1 = (DateTime)i._date1.clone();
	_date2 = (DateTime)i._date2.clone();
	_currentDate = (DateTime)i._currentDate.clone();
}

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
		tsi._ts = _ts;
		// Now clone the mutable objects...
		if ( _date1 != null ) {
			tsi._date1 = (DateTime)_date1.clone();
		}
		if ( _date2 != null ) {
			tsi._date2 = (DateTime)_date2.clone();
		}
		if ( _currentDate != null ) {
			tsi._currentDate = (DateTime)_currentDate.clone();
		}
		return tsi;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	_date1 = null;
	_date2 = null;
	_currentDate = null;
	_tsdata = null;
	super.finalize();
}

/**
Return the current date for the iterator.
@return the current date for the iterator.
*/
public DateTime getDate ()
{	return _currentDate;
}

/**
Return the data flag for the current date.
@return the data flag for the current date.
*/
public String getDataFlag ()
{	return _tsdata.getDataFlag();
}

/**
Return the data value for the current date.
@return the data value for the current date.
*/
public double getDataValue ()
{ 	return _tsdata.getData ();
}

// TODO SAM 2005-09-14 Make this private for now since we call only when data should be available.
// This method may be useful if public.
/**
Return the TSData for the current date/time of the iterator.
@return the TSData for the current date/time of the iterator.  WARNING:  the contents of this object are
volatile and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
private TSData getCurrentTSData ()
{	if ( _ts.hasDataFlags() ) {
		// Transfer the values.  This has more overhead but currently
		// there is no way to get the data quality otherwise...
		TSData data = _ts.getDataPoint ( _currentDate );
		_tsdata.setDate(_currentDate);
		_tsdata.setData(data.getData());
		_tsdata.setDataFlag ( data.getDataFlag() );
	}
	else {
	    // A little more efficient...
		_tsdata.setDate(_currentDate);
		_tsdata.setData(_ts.getDataValue(_currentDate));
		_tsdata.setDataFlag ( "" );
	}
	return _tsdata;
}

/**
Return the reference to the time series being iterated.
@return reference to the TS.
*/
public TS getTS ()
{	return _ts;
}

/**
Go to the specified date/time, returning the matching data as if next() or previous() had been called.
The date/time in the time series MUST exactly match the date (dt.equals(date/time from time series) is called).
If unable to go to the date/time, null is returned.
@param dt Date/time to go to.
@param resetIfFail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile and
change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData goTo ( DateTime dt, boolean resetIfFail )
{	DateTime date = null;
	TSData data = null;
	// If the iterator has not fully initialized, call next() once to force it...
	if ( !_firstDateProcessed && !_lastDateProcessed ) {
		next();
	}
	DateTime currentDateOrig = null;
	boolean lastDateProcessedOrig = _lastDateProcessed;
	boolean firstDateProcessedOrig = _firstDateProcessed;
	if ( dt.equals(_currentDate) ) {
		// Just return...
		return getCurrentTSData ();
	}
	else if ( dt.greaterThan(_currentDate) ) {
		// Requested date/time is greater than current.
		// Need to move forward in time.
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails...
			if ( _currentDate != null ) {
				currentDateOrig = new DateTime (_currentDate);
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
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
	else {
	    // Requested date/time is earlier than the current date/time.  Need to move back in time...
		if ( resetIfFail ) {
			// Save the starting conditions in case the search fails...
			if ( _currentDate != null ) {
				currentDateOrig =new DateTime(_currentDate);
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
			_currentDate = currentDateOrig;
			_firstDateProcessed = firstDateProcessedOrig;
			_lastDateProcessed = lastDateProcessedOrig;
		}
		return null;
	}
}

/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest next (future) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
@param resetIfFail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
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

/**
Initialize data.
*/
private void initialize ()
{	_ts = null;
	_firstDateProcessed = false;
	_lastDateProcessed = false;
	_tsdata = new TSData();
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

	if ( (_ts == null) || (_ts.getDataSize() == 0) ) {
		return null;
	}
	if ( !_lastDateProcessed ) {
		// We only want to advance the date if we have not already gone past the end...
		if ( !_firstDateProcessed ) {
			// It is possible that the date specified as input does
			// not exactly align with a date in the time series.
			// But it is adjusted at construction.
			_firstDateProcessed = true;
		}
		else {
		    // Only increment if have processed the first date...
			// The following should only be used for IrregularTS.  However, IrregularTS now should
		    // return an IrregularTSIterator so the following should actually never get called...
			_currentDate.addInterval (_intervalBase, _intervalMult);
		}
		if ( _currentDate.greaterThan(_date2) ) {
			// We are at the end or have exceeded the limits of the data...
			_lastDateProcessed = true;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "TSIterator.next", "Have passed end date: " + _date2 );
			}
		}
	}
	if ( _lastDateProcessed ) {
		return null;
	}
	else {
	    // Set the values.  These are used by getDate, etc...
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

	if ( (_ts == null) || (_ts.getDataSize() == 0) ) {
		return null;
	}
	if ( !_firstDateProcessed ) {
		// We only want to decrement the date if we have not already gone past the end...
		if ( !_firstDateProcessed && !_lastDateProcessed ) {
			// Because at construction the current date/time is set
			// to the start, we need to override here and set to the
			// ending date/time.  Only do this if next() has not
			// been called (in this case _first_date_processed will be false).
			_currentDate = new DateTime ( _date2 );
			_lastDateProcessed = true;
		}
		else if ( !_lastDateProcessed ) {
			// It is possible that the date specified as input does
			// not exactly align with a date in the time series.
			// But it is adjusted at construction.
			_lastDateProcessed = true;
		}
		else {
		    // Only decrement if have processed the last date...
			// The following should only be used for IrregularTS.
			// However, IrregularTS now should return an
			// IrregularTSIterator so the following should actually never get called...
			_currentDate.addInterval (_intervalBase, -_intervalMult);
		}
		if ( _currentDate.lessThan(_date1) ) {
			// We are at the end or have exceeded the limits of the data...
			_firstDateProcessed = true;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, "TSIterator.previous", "Have passed start date: " + _date1 );
			}
		}
	}
	if ( _firstDateProcessed ) {
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
@param date1 New starting date/time for iterator.
*/
public void setBeginTime ( DateTime date1 )
{	_date1 = new DateTime(date1);
	_firstDateProcessed = false;
	_lastDateProcessed = false;
	_currentDate = new DateTime(_date1); // Default for next()
}

/**
Set the ending date/time for the iterator.
The iterator will be reset.  A call to next() will not return the first value
but will return the next value after the last value returned.
@param date2 New end date/time for iterator.
*/
public void setEndTime ( DateTime date2 )
{	_date2 = new DateTime(date2);
	_lastDateProcessed = false;
}

}