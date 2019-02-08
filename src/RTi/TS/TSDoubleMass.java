// TSDoubleMass - class to hold and build data for a double mass plot

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
// TSDoubleMass - class to hold and build data for a double mass plot
// ----------------------------------------------------------------------------
// History:
//
// 23 Jul 1998	Steven A. Malers, RTi	Initial version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
The TSDoubleMass class stores double mass data for time series, consisting of
the accumulated values of each time series.  Missing data values are skipped in
the accumulation.  The resulting data set includes the following, which can be
used for plotting:
<p>
<pre>
Common          Independent TS                     TSi             ...
Date       AccumVal    IsMissing    AccumVal    IsMissing   ...
</pre>
<p>

If a data point is missing, the accumulated value will carried forward and
the missing flag will be set to true.
*/
public class TSDoubleMass
{

// Data...

private double [][] _accum_values;	// Accumulation values.
private boolean [][] _accum_missing_flag;
					// Flag indicating whether data are
					// missing.

/**
Constructor.  This constructs and does the analysis using the maximum
period available from the data.  Use accessor functions to retrieve the data.
@param ts list of TS to analyze.
@exception TSException if an error occurs.
*/
public TSDoubleMass ( List<TS> ts )
throws TSException
{
	// Find the maximum period for all the time series involved in the
	// computations...

	TSLimits limits = TSUtil.getPeriodFromTS ( ts, TSUtil.MAX_POR );
	initialize ( ts, limits.getDate1(), limits.getDate2() );
	limits = null;
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_accum_values = null;
	_accum_missing_flag = null;
	super.finalize();
}

/**
Perform the double mass analysis.
@param ts list of TS to analyze.
@param date1 Start of analysis.
@param date2 End of analysis.
@exception TSException if an error occurs during the analysis.
*/
private void initialize ( List<TS> ts, DateTime date1, DateTime date2 )
throws TSException
{	String message, routine = "TSDoubleMass.initialize";
	int	i;

	if ( ts == null ) {
		message = "TS is null for double mass computation";
		Message.printWarning ( 1, routine, message );
		throw new TSException ( message );
	}
	int size = ts.size();
	if ( size <= 1 ) {
		message =
		"TS list has size " + size +
		" for double mass computation (needs to be >= 2)";
		Message.printWarning ( 1, routine, message );
		throw new TSException ( message );
	}

	// Make sure that all the time series have the same interval...
	
	if ( !TSUtil.intervalsMatch ( ts ) ) {
		message = "Time series have different intervals.  " +
		"Unable to compute double mass";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Now allocate the memory for data.  This consists of an array of
	// DateTime, and, for each time series, an array of accumulated values
	// and indicators for whether at the date the time series contains
	// missing data....

	// Get the data size...

	TS ts_i = (TS)ts.get ( 0 );
	int interval_base = ts_i.getDataIntervalBase();
	int interval_mult = ts_i.getDataIntervalMult();
	int data_size = TSUtil.calculateDataSize ( date1, date2, interval_base, interval_mult );

	_accum_values = new double[size][];
	_accum_missing_flag = new boolean[size][];
	double [] last_accum = new double[size];
	for ( i = 0; i < size; i++ ) {
		ts_i = (TS)ts.get(i);
		_accum_values[i] = new double[data_size];
		_accum_missing_flag[i] = new boolean[data_size];
		last_accum[i] = ts_i.getMissing();
	}

	// Now loop through the period and accumulate the time series...

	int	datapos = 0;	// Position in accumulation.
	double	value;
	DateTime	date;
	for (	date = new DateTime ( date1 ), datapos = 0;
		date.lessThanOrEqualTo(date2);
		date.addInterval(interval_base, interval_mult), datapos++ ) {
		for ( i = 0; i < size; i++ ) {
			// First get the data value...
			ts_i = (TS)ts.get(i);
			value = ts_i.getDataValue ( date );
			// Now accumulate...
			if ( ts_i.isDataMissing(value) ) {
				// Carry the previous accumulated value and
				// set the missing flag to true...
				_accum_values[i][datapos] = last_accum[i];
				_accum_missing_flag[i][datapos] = true;
			}
			else {	// Accumulate, taking care to handle missing
				// data in the accumulated value...
				if ( ts_i.isDataMissing(last_accum[i]) ) {
					// Reset the value...
					_accum_values[i][datapos] = value;
				}
				else {	// Add to the accumulation...
					_accum_values[i][datapos] += last_accum[i] + value;
				}
				_accum_missing_flag[i][datapos] = false;
			}
			// Now reset the last accumulation value to the one we
			// just computed...
			last_accum[i] = _accum_values[i][datapos];
		}
	}
	message = null;
	routine = null;
	ts_i = null;
	last_accum = null;
	date = null;
}

} // End of TSDoubleMass
