// IrregularTSIterator - used to iterate through irregular time series data

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
	 * The time series being iterated cast to IrregularTS,
	 * to avoid having to cast elsewhere.
	 */
	private IrregularTS irregularTS = null;

/**
Construct an iterator for the full period of the time series.
@param ts Time series to iterate through.
@exception Exception if the time series or its start and end dates are null.
*/
public IrregularTSIterator ( IrregularTS ts )
throws Exception
{	super ( ts );
	irregularTS = ts;
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
	irregularTS = ts;
}

/**
Return the duration of the data value in seconds.  This method is only defined
for the IrregularTSIterator and will return zero for regular TS.
@return Data value duration in seconds.
*/
public int getDuration ()
{ 	return 0;
}

// TODO SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  The date/time in the time series MUST exactly
match the date (dt.equals(...)).  If unable to go to the date/time, null is returned.
@param dt Date/time to go to.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData goTo ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goTo",
	"The goTo() method is not yet implemented in IrregularTSIterator." );
	return null;
}

// TODO SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest next (future) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.  WARNING:  the contents of this object are volatile
and change with each iteration.  Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData goToNearestNext ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestNext",
	"The goToNearestNext() method is not yet implemented in IrregularTSIterator." );
	return null;
}

// TODO SAM 2005-09-16 Remove the warning when implemented
/**
Go to the specified date/time, returning the matching data as if next() or
previous() had been called.  If an exact match for the requested date/time
cannot be made, return the nearest previous (past) data.  Return null if the
search cannot find a matching date/time (e.g., due to the end of the period).
@param reset_if_fail If true and the search fails, reset to the starting
position, but still return null.  If false and the search fails, the current
date/time of the iterator will be at the end of data and null will be returned.
@return the TSData for the requesting date/time.
THIS METHOD IS NOT YET IMPLEMENTED FOR IrregularTSIterator.  null IS ALWAYS RETURNED.
WARNING:  the contents of this object are volatile and change with each iteration.
Use the get*() methods in TSIterator to retrieve data directly.
*/
public TSData goToNearestPrevious ( DateTime dt, boolean reset_if_fail )
{	Message.printWarning (1, "IrregularTSIterator.goToNearestPrevious",
	"The goToNearestPrevious() method is not yet implemented in IrregularTSIterator." );
	return null;
}

/**
Indicate if next() can be called to return data.
If false is returned, then the iterator is positioned at the last date with data.
*/
public boolean hasNext ()
{
	// For an irregular time series, get the date/time for the next value and check whether it is past _date2
	TSData nextData = this._tsdata.getNext();
	if ( nextData == null ) {
		// There is no next point
		return false;
	}
	else {
		DateTime dt = nextData.getDate();
		if ( dt == null ) {
			// Next point does not have a date
			return false;
		}
		else if ( dt.greaterThan(this._date2) ) {
			// Next point is past the end
			return false;
		}
	}

	return true;
}

/**
Advance the iterator.  When called the first time,
the initial value will be returned as follows:
<ul>
<li>If no period was specified in the constructor,
return the first data point in the time series.</li>
<li>If a period was specified in the constructor,
return the first data point >= the start time.</li>
</ul>
@return null if the data is past the end or a pointer to an internal
TSData object containing the data for the current time step (WARNING:  the
contents of this object are volatile and change with each iteration).  Use the
get* methods in TSIterator to retrieve data directly.
*/
public TSData next ( )
{	int dl = 30;
	TSData theData = null; // Use this for returns.

	// Clearly no data available so return null

	if ( (this._ts == null) || (this._ts.getDataSize() == 0) ) {
		return null;
	}

	// First advance the current date/time.

	//if ( _firstDateProcessed ) {
	if ( ! this._isIterationComplete ) {
		// if ( _firstDateProcessed ) {
		if ( this._nextWasCalledFirst || this._previousWasCalledFirst ) {
			// Have previously called next() or previous() so initialization has occurred.
			// - just return the next item in the list, which is very fast
			theData = this._tsdata.getNext();
		}
		else {
			// This is the first call to next() so need to find the first data point:
			// - if no period was specified, return the first value
			// - if a period was specified, return the first point >= the start
			List<TSData> tsdataList = this.irregularTS.getData();
			if ( (tsdataList == null) || (tsdataList.size() == 0) ) {
				// No data.
				return null;
			}
			int size = tsdataList.size();
			TSData ptr = null;
			for ( int i = 0; i < size; i++ ) {
				ptr = tsdataList.get(i);
				// this._currentDate is set in the constructor as the initial date/time as requested or from the time series.
				// Because irregular time series may not exactly match, use >= to find the first value.
				// For example, may be computing statistics on a regular interval but irregular interval data don't
				// align with those intervals.
				// Old code
				// - TODO smalers 2020-08-04 remove when tested out
				//if ( ptr.getDate().equals(this._currentDate) ) {
				if ( ptr.getDate().greaterThanOrEqualTo(this._currentDate) ) {
					theData = ptr;
					break;
				}
			}
			// TODO smalers 2020-08-04 does this need to check whether this._prevWasCalledFirst?
			this._nextWasCalledFirst = true;
		}

		if ( theData == null ) {
			// Have called next() and reached the end of the data list...
			// - can call next() past the end of the requested period - does not return null in that case
			//_lastDateProcessed = true;
			this._isIterationComplete = true;
		}
		else {
			// Have some data to return
	    	this._currentDate = theData.getDate();
			if ( this._currentDate.greaterThan(this._date2) ) {
				// Have gone past the requested end date/time...
				//_lastDateProcessed = true;
				this._isIterationComplete = true;
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, "IrregularTSIterator.next", "Have passed end date: " + _date2.toString() );
				}
			}
		}
	}

	// Now return data or null.

	//if ( _lastDateProcessed ) { 
	if ( this._isIterationComplete ) { 
		return null;
	}
	else {
	    this._tsdata = theData;
		return theData;
	}
}

// TODO - need to add previous also need to add clone().

public TSData previous () {
	throw new RuntimeException("IrregularTSIterator.previous() is not implemented.");
}

}