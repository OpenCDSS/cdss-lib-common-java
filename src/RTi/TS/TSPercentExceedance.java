// TSPercentExceedance - simple class to hold and manipulate data for percent exceedance calculation

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

// ----------------------------------------------------------------------------
// TSPercentExceedance - simple class to hold and manipulate data for
//			percent exceedance calculation
// ----------------------------------------------------------------------------
// History:
//
// 21 Oct 1998	Steven A. Malers, RTi	Initial version.  Copy TSPatterStats and
//					modify.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TS.INTERVAL* to TimeInterval.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
The TSPercentExceedance class stores percent exceedance data for a time series.
<b>This class is under development.</b>
*/
public class TSPercentExceedance
{

// Data...

private TS _ts = null;			// Time series being analyzed.
private double [] _values = null;	// Data values.
private double [] _percent = null;	// Exceedance percent for data values.
private boolean _dirty = true;		// Indicates whether values have been
					// computed.
private int _ndata = 0;			// Number of non-missing data points.

/**
Default constructor.
@param ts Time series to compute percent exceedance data for.
@exception RTi.TS.TSException if there is a problem analyzing the data.
*/
public TSPercentExceedance ( TS ts )
throws TSException
{	try {	initialize ( ts );
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	_values = null;
	_percent = null;
	super.finalize();
}

/**
Return the number of points in the data.
@return the number of points in the data.
*/
public int getNumberOfPoints ()
{	return _ndata;
}

/**
Return the exceedance percentage (0-100) given the index in the data.
@return the exceedance percentage (0-100) given the index in the data.
@param i Index in data.
*/
public double getPercent ( int i )
{	return _percent[i];
}

/**
Return the data value given the index in the data.
@return the data value given the index in the data.
@param i Index in data.
*/
public double getValue ( int i )
{	return _values[i];
}

/**
Initialize the data.
@param ts Time series to analyze.
@exception TSException if an error occurs.
*/
private void initialize ( TS ts )
throws TSException
{	_ts = ts;
	_dirty = true;
	try {	refresh();
	}
	catch ( TSException e ) {
		throw e;
	}
}

/**
Refresh the derived values (averages).
*/
public void refresh ()
throws TSException
{	String routine = "TSPercentExceedance.refresh";

	try { // Main try...
	if ( !_dirty ) {
		return;
	}

	TSLimits valid_dates = TSUtil.getValidPeriod ( _ts, _ts.getDate1(),
			_ts.getDate2() );
	DateTime start	= valid_dates.getDate1();
	DateTime end	= valid_dates.getDate2();
	valid_dates = null;
	if ( (start == null) || (end == null) ) {
		_dirty = false;
		return;
	}

	// First get the data limits so we know the number of non-missing
	// data...
	TSLimits limits = TSUtil.getDataLimits ( _ts, start, end );
	_ndata = limits.getNonMissingDataCount();
	_values = new double [_ndata];
	_percent = new double [_ndata];
	// Now loop through the data and store the non-missing data values...


	int interval_base = _ts.getDataIntervalBase();
	int interval_mult = _ts.getDataIntervalMult();
	int count = 0;
	double value;
	if ( interval_base == TimeInterval.IRREGULAR ) {
		// Get the data and loop through the vector...
		IrregularTS irrts = (IrregularTS)_ts;
		List<TSData> alltsdata = irrts.getData();
		if ( alltsdata == null ) {
			// No data for the time series...
			return;
		}
		int nalltsdata = alltsdata.size();
		TSData tsdata = null;
		DateTime date = null;
		for ( int i = 0; i < nalltsdata; i++ ) {
			tsdata = alltsdata.get(i);
			date = tsdata.getDate();
			if ( date.greaterThan(end) ) {
				// Past the end of where we want to go so quit...
				break;
			}
			if ( date.greaterThanOrEqualTo(start) ) {
				value = tsdata.getDataValue();
				if ( !_ts.isDataMissing(value) ) {
					_values[count] = value;
					++count;
				}
			}
		}
		irrts = null;
		alltsdata = null;
		tsdata = null;
		date = null;
	}
	else {	// Loop using addInterval...
		DateTime date = new DateTime ( start );
		for (	;
			date.lessThanOrEqualTo( end );
			date.addInterval(interval_base, interval_mult) ) {
			value = _ts.getDataValue(date);
			if ( !_ts.isDataMissing(value) ) {
				_values[count] = value;
				++count;
			}
		}
		date = null;
	}
	// Now sort the data...
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Sorting data points..." );
	}
	MathUtil.sort ( _values, MathUtil.SORT_QUICK,
		MathUtil.SORT_ASCENDING, null, false );
	// Now loop through the data and set the percentages...
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Computing percentages..." );
	}
	for ( int i = 0; i < count; i++ ) {
		_percent[i] = ((double)(i + 1)/(double)(count + 1))*100.0;
	}
	// Reset _ndata in the chance that it is different from count...
	_ndata = count;
	// Now indicate that the data have been processed...
	_dirty = false;
	// Clean up...
	start = null;
	end = null;
	limits = null;
	}
	catch ( Exception e ) {
		throw new TSException ( e.getMessage() );
	}
	routine = null;
}

/**
Return a string representation of the object.
*/
public String toString ()
{	
	try {	refresh();
		return new String (
		"No string representation for TSPercentExceedance yet" );
	}
	catch ( TSException e  ) {
		return new String ();
	}
}

} // End of TSPercentExceedance class definition
