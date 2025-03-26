// HourTS - base class from which all hourly time series are derived

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
// HourTS - base class from which all hourly time series are derived
// ----------------------------------------------------------------------------
// Notes:	(1)	This base class is provided so that specific hourly
//			time series can derive from this class.
//		(2)	Data for this time series interval is stored as follows:
//
//			data interval within month  ->
//
//			------------------------
//              	|  |  |.......|  |  |  |     first month in period
//			------------------------
//			|  |  |.......|  |  |
//			------------------------
//			|  |  |.......|  |  |  |
//			------------------------
//			|  |  |.......|  |  |
//			---------------------
//			.
//			.
//			.
//			------------------------
//			|  |  |.......|  |  |  |
//			------------------------
//			|  |  |.......|  |  |        last month in period
//			---------------------
//
//			The base block of storage is the month.  This lends
//			itself to very fast data retrieval but may waste some
//			memory for short time series in which full months are
//			not stored.  This is considered a reasonable tradeoff.
// ----------------------------------------------------------------------------

package RTi.TS;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
The HourTS class is the base class for hourly time series.
Additional hour time series can be extended if the allocateDataSpace() and set/get methods are overridden.
*/
@SuppressWarnings("serial")
public class HourTS extends TS implements Cloneable, Serializable, Transferable {

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor hourTSFlavor = new DataFlavor(RTi.TS.HourTS.class, "RTi.TS.HourTS");

// Data members.

/**
Data space for hourly time series.
*/
private	double[][] _data;

/**
Data flags for each hourly value.  The dimensions are [month][value_in_month]
*/
private String [][] _dataFlags;

// TODO SAM 2012-05-03 After initial addition, this feature is not currently needed.
// Enable later if needed but don't have time to test impacts on performance and memory now.
// Also, duration may only be needed for minute and irregular intervals.
/**
Durations for each hourly value. The dimensions are [month][value_in_month].
*/
//private int[][] _durations;

/**
Indicate whether the duration is used.
*/
//private boolean _has_durations = false;

/**
Data position, used internally by get/set methods.
*/
private int[] _pos;

/**
Row position in data.
*/
private int _row;

/**
Column position in data.
*/
private int _column;

/**
Default constructor.
*/
public HourTS ( ) {
	super ();
	init();
}

/**
Copy constructor.  Everything is copied by calling copyHeader() and then copying the data values.
@param ts HourTS to copy.
*/
public HourTS ( HourTS ts ) {
	if ( ts == null ) {
		return;
	}
	copyHeader ( ts );
	allocateDataSpace();
	DateTime date2 = new DateTime ( _date2 );
	DateTime date = new DateTime ( _date1 );
	for ( ;	date.lessThanOrEqualTo(date2);
		date.addInterval(_data_interval_base,_data_interval_mult) ) {
		setDataValue ( date, ts.getDataValue(date) );
	}
	date2 = null;
	date = null;
}

/**
Allocate the data duration space for the time series.
This requires that the data interval base and multiplier are set correctly and that _date1 and _date2 have been set.
The allocateDataSpace() method will not allocate duration space.
Use this method when the data durations need to be allocated upon seeing the data after the initial allocation.
@param initialValue Initial value (null is allowed and will result in the durations being initialized to 0).
@param retainPreviousValues If true, the array size will be increased if necessary,
but previous data values will be retained.  If false, the array will be reallocated and initialized to 0s.
@exception Exception if there is an error allocating the memory.
*/
/* TODO SAM 2012-05-03 Enable later.
public void allocateDataDurationSpace ( Integer initialValue, boolean retainPreviousValues ) throws Exception {
    String routine = getClass().getSimpleName() + ".allocateDataDurationSpace", message;
	int	i;

	if ( (_date1 == null) || (_date2 == null) ) {
		message ="Dates have not been set.  Cannot allocate data space.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	if ( (_data_interval_mult < 1) || (_data_interval_mult > 24) ) {
		message = "Only know how to handle 1-24 hour data, not " + _data_interval_mult + "-hour";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}

	if ( initialValue == null ) {
	    initialValue = 0;
	}

	int nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if ( nmonths == 0 ) {
		message = "TS has 0 months POR, maybe Dates haven't been set yet.";
		Message.printWarning( 2, routine, message );
		throw new Exception ( message );
	}

    int[][] prevDurations = null;
    if (_has_durations && retainPreviousValues) {
        prevDurations = _durations;
    }
    else {
        _has_durations = true;
    }

    // Top-level allocation...
	_durations = new int[nmonths][];

	// Set the counter date to match the starting month.
	// This date is used to determine the number of days in each month.

	DateTime date = new DateTime(DateTime.DATE_FAST);
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	// Allocate memory.

	int j = 0;
	int nvals = 0;
	int ndaysInMonth = 0;
	for ( i = 0; i < nmonths; i++, date.addMonth(1) ) {
		// If a non-valid interval, an exception was thrown above.
		ndaysInMonth = TimeUtil.numDaysInMonth ( date );
		nvals = ndaysInMonth*(24/_data_interval_mult);

		_durations[i] = new int[nvals];
		for ( j = 0; j < nvals; j++ ) {
			if(retainPreviousValues && (prevDurations != null)){
			    _durations[i][j] = prevDurations[i][j];
			}
            else {
                _durations[i][j] = initialValue.intValue();
            }
		}
	}
}
*/

/**
Allocate the data flag space for the time series.
This requires that the data interval base and multiplier are set correctly and that _date1 and _date2 have been set.
The allocateDataSpace() method will allocate the data flags if appropriate.
Use this method when the data flags need to be allocated after the initial allocation.
@param initialValue Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retainPreviousValues If true, the array size will be increased if necessary,
but previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initialValue, boolean retainPreviousValues )
throws Exception {
	String routine = getClass().getSimpleName() + ".allocateDataFlagSpace", message;
	int	i;

	if ( (_date1 == null) || (_date2 == null) ) {
		message = "Dates have not been set.  Cannot allocate data space.";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	if ( (_data_interval_mult < 1) || (_data_interval_mult > 24) ) {
		message = "Only know how to handle 1-24 hour data, not " + _data_interval_mult + "-hour.";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}

	if ( initialValue == null ) {
	    initialValue = "";
	}

	int nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if ( nmonths == 0 ) {
		message = "TS has 0 months POR, maybe Dates haven't been set yet.";
		Message.printWarning( 2, routine, message );
		throw new Exception ( message );
	}

	String [][] dataFlagsPrev = null;
	if ( _has_data_flags && retainPreviousValues ) {
		// Save the reference to the old flags array.
		dataFlagsPrev = _dataFlags;
	}
	else {
	    // Turn on the flags.
		_has_data_flags = true;
	}
	// Top-level allocation.
	_dataFlags = new String[nmonths][];

	// Set the counter date to match the starting month.
	// This date is used to determine the number of days in each month.

	DateTime date = new DateTime(DateTime.DATE_FAST);
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	// Allocate memory.

	int j = 0;
	int nvals = 0;
	int ndaysInMonth = 0;
	boolean internDataFlagStrings = getInternDataFlagStrings();
	for ( i = 0; i < nmonths; i++, date.addMonth(1) ) {
		// If a non-valid interval, an exception was thrown above.
		ndaysInMonth = TimeUtil.numDaysInMonth ( date );
		nvals = ndaysInMonth*(24/_data_interval_mult);

		_dataFlags[i] = new String[nvals];

		// Now fill with the initial data value.

		for ( j = 0; j < nvals; j++ ) {
		    if ( internDataFlagStrings ) {
		        _dataFlags[i][j] = initialValue.intern();
		    }
		    else {
		        _dataFlags[i][j] = initialValue;
		    }
			// Initialize with blanks (spaces).
			if(retainPreviousValues && (dataFlagsPrev != null)){
				// Copy over the old values (typically shorter character arrays).
			    if ( internDataFlagStrings ) {
			        _dataFlags[i][j] = dataFlagsPrev[i][j].intern();
			    }
			    else {
			        _dataFlags[i][j] = dataFlagsPrev[i][j];
			    }
			}
		}
	}
}

/**
Allocate the data space.  The start and end dates and the interval multiplier should have been set.
@return 0 if success, 1 if failure.
*/
public int allocateDataSpace( ) {
	String routine = getClass().getSimpleName() + ".allocateDataSpace";
	int	dl = 30, i, ndaysInMonth, nvals;

	if ( (_date1 == null) || (_date2 == null) ) {
		Message.printWarning ( 2, routine, "No dates set for memory allocation." );
		return 1;
	}
	if ( (_data_interval_mult < 1) || (_data_interval_mult > 24) ) {
		Message.printWarning ( 2, routine,
		"Only know how to handle 1-24 hour data, not " + _data_interval_mult + "-hour." );
		return 1;
	}
	int nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if( nmonths == 0 ){
		Message.printWarning( 2, routine, "TS has 0 months POR, maybe dates haven't been set yet or end is not after start." );
		return 1;
	}

	_data = new double[nmonths][];
	if ( _has_data_flags ) {
		_dataFlags = new String[nmonths][];
	}

	// Probably need to catch an exception here in case run out of memory.

	// Set the counter date to match the starting month.
	// This date is used to determine the number of days in each month.

	DateTime date = new DateTime ( DateTime.DATE_FAST );
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	for ( i = 0; i < nmonths; i++, date.addMonth(1) ) {
		ndaysInMonth = TimeUtil.numDaysInMonth ( date );
		// 1-24 hour data.
		nvals = ndaysInMonth*(24/_data_interval_mult);
		_data[i] = new double[nvals];
		if ( _has_data_flags ) {
			_dataFlags[i] = new String[nvals];
		}

		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Allocated " + nvals + " values for data[" + i + "]" );
		}

		// Again, need to check exceptions for lack of memory.

		// Now fill with the missing data value.

		for ( int j = 0; j < nvals; j++ ) {
			_data[i][j] = _missing;
			if ( _has_data_flags ) {
				_dataFlags[i][j] = "";
			}
		}
	}

	int nactual = calculateDataSize ( _date1, _date2, _data_interval_mult );
	setDataSize ( nactual );

	if ( Message.isDebugOn ) {
		Message.printDebug( 10, routine,
		"Successfully allocated " + nmonths + " months of memory from " + _date1 + " to " + _date2 );
	}
	return 0;
}

/**
Determine the total number of points between two dates, given an interval multiplier.
@return The number of data points for an hour time series given the data interval multiplier for the specified period.
@param startDate The first date of the period.
@param endDate The last date of the period.
@param interval_mult The time series data interval multiplier.
*/
public static int calculateDataSize ( DateTime startDate, DateTime endDate, int interval_mult ) {
	String routine = HourTS.class.getSimpleName() + ".calculateDataSize";
	int datasize = 0;

	if ( startDate == null ) {
		Message.printWarning ( 2, routine, "Start date/time is null." );
		return 0;
	}
	if ( endDate == null ) {
		Message.printWarning ( 2, routine, "End date/time is null." );
		return 0;
	}
	if ( interval_mult > 24 ) {
		Message.printWarning ( 1, routine, "Greater than 24-hour TS not supported." );
		return 0;
	}
	// First set to the number of data in the months.
	datasize = TimeUtil.numDaysInMonths ( startDate.getMonth(), startDate.getYear(),
		endDate.getMonth(), endDate.getYear() )*24/interval_mult;
	// Now subtract off the data at the ends that are missing.
	// Start by subtracting the full day at the beginning of the month that is not included.
	datasize -= (startDate.getDay() - 1)*24/interval_mult;
	// Now subtract the recordings on the first day before the first time.
	datasize -= startDate.getHour()/interval_mult;
	// Now subtract off the data at the end.
	// Start by subtracting the full days off the end of the month.
	int ndays_in_month = TimeUtil.numDaysInMonth ( endDate.getMonth(), endDate.getYear() );
	datasize -= (ndays_in_month - endDate.getDay())*24/interval_mult;
	// Now subtract the readings at the end of the last day.
	datasize -= (23 - endDate.getHour())/interval_mult;
	return datasize;
}

/**
Change the period of record to the specified dates.
If the period is extended, missing data will be used to fill the time series.
If the period is shortened, data will be lost.
@param date1 New start date of time series.
@param date2 New end date of time series.
@exception RTi.TS.TSException if there is a problem extending the data.
*/
public void changePeriodOfRecord ( DateTime date1, DateTime date2 )
throws TSException {
	String routine = getClass().getSimpleName() + ".changePeriodOfRecord";
	String message;

	// To transfer, allocate a new data space.  In any case, need to get the dates established.
	if ( (date1 == null) && (date2 == null) ) {
		// No dates.  Cannot change.
		message = "\"" + _id + "\": period dates are null.  Cannot change the period.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	DateTime newDate1 = null;
	if ( date1 == null ) {
		// Use the original date.
		newDate1 = new DateTime ( _date1 );
	}
	else {
	    // Use the date passed in.
		newDate1 = new DateTime ( date1 );
	}
	DateTime newDate2 = null;
	if ( date2 == null ) {
		// Use the original date.
		newDate2 = new DateTime ( _date2 );
	}
	else {
	    // Use the date passed in.
		newDate2 = new DateTime ( date2 );
	}

	// Do not change the period if the dates are the same.

	if ( _date1.equals(newDate1) && _date2.equals(newDate2) ) {
		// No need to change period.
		return;
	}

	// To transfer the data (later), get the old position and then set in the new position.
	// To get the right data position, declare a temporary HourTS with the old dates and save a reference to the old data.

	double [][] dataSave = _data;
	String [][] dataFlagsSave = _dataFlags;
	HourTS tempTs = new HourTS ();
	tempTs.setDataInterval ( TimeInterval.HOUR, _data_interval_mult );
	tempTs.setDate1 ( _date1 );
	tempTs.setDate2 ( _date2 );

	// Also compute limits for the transfer to optimize performance.

	DateTime transferDate1 = null;
	DateTime transferDate2 = null;
	if ( newDate1.lessThan(_date1) ) {
		// Extending so use the old date.
		transferDate1 = new DateTime ( _date1 );
	}
	else {
	    // Shortening so use the new.
		transferDate1 = new DateTime ( newDate1 );
	}
	if ( newDate2.greaterThan(_date2) ) {
		// Extending so use the old date.
		transferDate2 = new DateTime ( _date2 );
	}
	else {
	    // Shortening so use the new.
		transferDate2 = new DateTime ( newDate2 );
	}

	// Now reset the dates and reallocate the period.

	setDate1 ( newDate1 );
	setDate2 ( newDate2 );
	allocateDataSpace();

	// At this point the data space will be completely filled with missing data.

	// Now transfer the data.
	// To do so, get the old position and then set in the new position.
	// Are only concerned with transferring the values for the the old time series that are within the new period.

	double value;
	int [] dataPos;
	boolean internDataFlagStrings = getInternDataFlagStrings();
	for ( DateTime date = new DateTime (transferDate1,DateTime.DATE_FAST);
		date.lessThanOrEqualTo (transferDate2);
		date.addInterval( _data_interval_base, _data_interval_mult ) ) {
		// Get the data position for the old data.
		dataPos = tempTs.getDataPosition(date);
		// Now get the value.
		value = dataSave[dataPos[0]][dataPos[1]];
		// Now set in the new period.
		// Also transfer the data flag.
		if ( _has_data_flags ) {
			// Transfer the value and flag.
		    if ( internDataFlagStrings ) {
		        setDataValue ( date, value, dataFlagsSave[dataPos[0]][dataPos[1]].intern(), 1 );
		    }
		    else {
		        setDataValue ( date, value, dataFlagsSave[dataPos[0]][dataPos[1]], 1 );
		    }
		}
		else {
		    // Transfer the value.
			setDataValue ( date, value );
		}
	}

	// Add to the genesis.

	addToGenesis ( "Changed period from: " + tempTs.getDate1() + " - " +
		tempTs.getDate2() + " to " + newDate1 + " - " + newDate2 );
}

/**
Clone the object.
The TS base class clone() method is called and then the the data array is cloned.  The result is a complete deep copy.
*/
public Object clone () {
	HourTS ts = (HourTS)super.clone();	// Clone data stored in the base.
	// Does not appear to work.
	//ts._data = (double [][])_data.clone();
	//ts._pos = (int [])_data.clone();
    if ( _data == null ) {
        ts._data = null;
    }
    else {
    	ts._data = new double[_data.length][];
    	for ( int i = 0; i < _data.length; i++ ) {
    		ts._data[i] = new double[_data[i].length];
    		System.arraycopy ( _data[i], 0, ts._data[i], 0,_data[i].length);
    	}
    }
	int ival = 0;
	boolean internDataFlagStrings = getInternDataFlagStrings();
	if ( _has_data_flags ) {
	    if ( _dataFlags == null ) {
	        ts._dataFlags = null;
	    }
	    else {
    		// Allocate months.
    		ts._dataFlags = new String[_dataFlags.length][];
    		for ( int imon = 0; imon < _dataFlags.length; imon++ ) {
    			// Allocate flag arrays for values in month one flag array for each value in month.
    			ts._dataFlags[imon] = new String[_dataFlags[imon].length];
    			for(ival = 0; ival < _dataFlags[imon].length; ival++){
    			    // Allocate data flags array for hour values.
    			    if ( internDataFlagStrings ) {
    			        ts._dataFlags[imon][ival] = _dataFlags[imon][ival].intern();
    			    }
    			    else {
    			        ts._dataFlags[imon][ival] = _dataFlags[imon][ival];
    			    }
    			}
    		}
	    }
	}
	ts._pos = new int[2];
	ts._pos[0] = _pos[0];
	ts._pos[1] = _pos[1];
	ts._row = _row;
	ts._column = _column;
	return ts;
}

/**
 * Indicate whether the data interval uses time.
 * @return true always
 */
public boolean dataIntervalUsesTime () {
	return true;
}

/**
Format the time series for output.
This is not meant to be used for time series conversion but to produce a general summary of the output.
At this time, hourly time series are always output in a matrix summary format.
@return Vector of strings that can be displayed, printed, etc.
@param proplist Properties of the output, as described in the following table:

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>	<td><b>Default</b></td>
</tr>

<tr>
<td><b>CalendarType</b></td>
<td>The type of calendar, either "Water" (Oct through Sep), "NovToOct"
(Nov through Oct), or "Calendar" (Jan through Dec), consistent with YearType enumeration.
</td>
<td>CalanderYear (but may be made sensitive to the data type or units in the future).</td>
</tr>

<tr>
<td><b>OutputEnd</b></td>
<td>
The ending date/time for output, in a format that can be parsed by DateTime.parse().
</td>
<td>null - output all available data.
</td>
</tr>

<tr>
<td><b>OutputStart</b></td>
<td>
The starting date/time for output, in a format that can be parsed by DateTime.parse().
</td>
<td>null - output all available data.
</td>
</tr>

<tr>
<td><b>PrintHeader</b></td>
<td>Print the time series header information in a format as follows:
<p>
<pre>
Time Series Identifier  = 07126500.CRDSS_USGS.streamflow.24
Description             = PURGATOIRE RIVER AT NINEMILE DAM, NR HIGBEE, CO.
Data source             = CRDSS_USGS
Data type               = streamflow
Data interval           = 24Hour
Data units              = CFS
Requested Period        = 1980-01 to 1990-01
Available Period        = 1924-01 to 1995-12
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintComments</b></td>
<td>Print the comments associated with the time series.
This may contain information about the quality of data, station information, etc.
This information is usually variable-length text, and may not be available.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintAllStats</b></td>
<td>Print all the statistics (currently maximum, minimum, and mean,
although standard deviation and others are being added).
Because statistics are being added to output, it is advised that if formatting is to remain the same over time,
that output items be individually specified.
One way of doing this is to turn all the statistics off and then turn specific items on (to true).
</td>
<td>false</td>
</tr>

<tr>
<td><b>PrintGenesis</b></td>
<td>Print the time series genesis information after the header in a format as follows:
<p>
<pre>
Time series creation history:
Read from XXXX database.
Filled missing data with...
etc.
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMinStats</b></td>
<td>Print the minimum value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMaxStats</b></td>
<td>Print the maximum value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintMeanStats</b></td>
<td>Print the mean value statistics.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintNotes</b></td>
<td>Print notes about the output.
This consists of helpful information used to understand the output (but does not consist of data).  For example:
<p>
<pre>
Notes:
  Years shown are calendar years.
  Annual values and statistics are computed only on non-missing data.
  NC indicates that a value is not computed because of missing data or the data value itself is missing.
</pre>
</td>
<td>true</td>
</tr>

<tr>
<td><b>UseCommentsForHeader</b></td>
<td>Use the time series comments for the header and do not print other header information.
This can be used when the entire header is formatted elsewhere.
</td>
<td>false</td>
</tr>

</table>
@exception RTi.TS.TSException Throws if there is a problem formatting the output.
*/
public List<String> formatOutput( PropList proplist )
throws TSException {
	String message = "", routine = getClass().getSimpleName() + ".formatOutput", year_column = "";
    List<String> strings = new ArrayList<>(20);
	PropList props = null;
	String dataFormat = "%9.1f", propValue = null;

	// Only know how to do this 1 to 24-hour time series
	// (in the future may automatically convert to correct interval).

	if ( _data_interval_mult > 24 ) {
		message = "Can only do summary for <= 24 hour time series.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// If the property list is null, allocate one here so don't have to constantly check for null.

	if ( proplist == null ) {
		// Create a PropList so don't have to check for nulls all the time.
		props = new PropList ( "formatOutput" );
	}
	else {
	    props = proplist;
	}

	// Get the important formatting information from the property list.

	// Determine the units to output.  For now use what is in the time series.

	String req_units = _data_units;

	// Need to check the data type to determine if it is an average or a total.
	// For now, make some guesses based on the units.

	if ( req_units.equalsIgnoreCase("AF") ||
		req_units.equalsIgnoreCase("ACFT") ||
		req_units.equalsIgnoreCase("FT") ||
		req_units.equalsIgnoreCase("FEET") ||
		req_units.equalsIgnoreCase("FOOT") ||
		req_units.equalsIgnoreCase("IN") ||
		req_units.equalsIgnoreCase("INCH") ) {
		// Assume totals.
		year_column = "Total";
	}
	else {
	    // Assume averages.
		year_column = "Average";
	}

	// Get the precision.

	propValue = props.getValue ( "OutputPrecision" );
	if ( propValue == null ) {
		// Older, being phased out.
		Message.printWarning ( 2, routine, "Need to switch Precision property to OutputPrecision" );
		propValue = props.getValue ( "Precision" );
	}
	if ( propValue == null ) {
		// Try to get units information for default.
		try {
		    DataUnits u = DataUnits.lookupUnits ( req_units );
			dataFormat = "%9." + u.getOutputPrecision() + "f";
		}
		catch ( Exception e ) {
			// Default.
			dataFormat = "%9.1f";
		}
	}
	else {
	    // Set to requested precision.
		dataFormat = "%9." + propValue + "f";
	}

	// Determine whether water or calendar year.

	propValue = props.getValue ( "CalendarType" );
	if ( propValue == null ) {
		// Default to "CalendarYear".
		propValue = "" + YearType.CALENDAR;
	}
	YearType calendar = YearType.valueOfIgnoreCase(propValue);

	// Determine the period to output.  For now always output the total.

	if ( (_date1 == null) || (_date2 == null) ) {
		message = "Null period dates for time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	DateTime startDate = new DateTime (_date1);
	propValue = props.getValue ( "OutputStart" );
	if ( propValue != null ) {
		try {
		    startDate = DateTime.parse ( propValue );
			startDate.setPrecision ( DateTime.PRECISION_HOUR );
		}
		catch ( Exception e ) {
			// Default to the time series.
			startDate = new DateTime ( _date1 );
		}
	}
	DateTime endDate = new DateTime (_date2);
	propValue = props.getValue ( "OutputEnd" );
	if ( propValue != null ) {
		try {
		    endDate = DateTime.parse ( propValue );
			endDate.setPrecision ( DateTime.PRECISION_HOUR );
		}
		catch ( Exception e ) {
			// Default to the time series.
			endDate = new DateTime ( _date2 );
		}
	}

	// Now generate the output based on the format.

	propValue = props.getValue ( "PrintHeader" );
	String printHeader = null;
	if ( propValue == null ) {
		// Default is true.
		printHeader = "true";
	}
	else {
	    printHeader = propValue;
	}
	propValue = props.getValue ( "UseCommentsForHeader" );
	String useCommentsForHeader = null;
	if ( propValue == null ) {
		// Default is false.
		useCommentsForHeader = "false";
	}
	else {
	    useCommentsForHeader = propValue;
	}
	if ( printHeader.equalsIgnoreCase("true") ) {
		if ( !useCommentsForHeader.equalsIgnoreCase("true") || (_comments.size() == 0) ){
			// Format the header from data (not comments).
			strings.add ( "" );
			List<String> strings2 = formatHeader();
			StringUtil.addListToStringList ( strings, strings2 );
		}
	}

	// Add comments if available.

	propValue = props.getValue ( "PrintComments" );
	String printComments = null;
	if ( propValue == null ) {
		// Default is true.
		printComments = "true";
	}
	else {
	    printComments = propValue;
	}
	if ( printComments.equalsIgnoreCase("true") || useCommentsForHeader.equalsIgnoreCase("true")){
		strings.add ( "" );
		if ( _comments != null ) {
			int ncomments = _comments.size();
			if ( !useCommentsForHeader.equalsIgnoreCase("true")){
				strings.add ( "Comments:" );
			}
			if ( ncomments > 0 ) {
				for ( int i = 0; i < ncomments; i++ ) {
					strings.add( (String)_comments.get(i));
				}
			}
			else {
			    strings.add( "No comments available.");
			}
		}
		else {
		    strings.add( "No comments available.");
		}
	}

	// Print the genesis information.

	propValue = props.getValue ( "PrintGenesis" );
	String print_genesis = null;
	if ( propValue == null ) {
		// Default is true.
		print_genesis = "true";
	}
	else {
	    print_genesis = propValue;
	}
	if ( (_genesis != null) && print_genesis.equalsIgnoreCase("true") ) {
		int size = _genesis.size();
		if ( size > 0 ) {
			strings.add ( "" );
			strings.add ( "Time series creation history:" );
			strings = StringUtil.addListToStringList( strings, _genesis );
		}
	}
	if ( _data_interval_mult == 24 ) {
		formatOutput24Hour ( strings, props, calendar, dataFormat, startDate, endDate, req_units, year_column );
	}
	else {
	    formatOutputNHour (	strings, props, calendar, dataFormat, startDate, endDate, req_units, year_column );
	}
	return strings;
}

/**
Format the time series for output.
@return list of strings that are written to the file.
@param fp Writer to receive output.
@param props Properties to modify output.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( PrintWriter fp, PropList props )
throws TSException {
	List<String> formattedOutput = null;
	String routine = getClass().getSimpleName() + ".formatOutput";
	int	dl = 20;
	String message;

	if ( fp == null) {
		message = "Null PrintWriter for output.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// First get the formatted output.

	try {
	    formattedOutput = formatOutput ( props );
		if ( formattedOutput != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Formatted output is " + formattedOutput.size() + " lines." );
			}

			// Now write each string to the writer.

			String newline = System.getProperty ( "line.separator");
			int size = formattedOutput.size();
			for ( int i = 0; i < size; i++ ) {
				fp.print ( (String)formattedOutput.get(i) + newline );
			}
			newline = null;
		}
	}
	catch ( TSException e ) {
		// Rethrow.
		throw e;
	}

	// Also return the list.

	return formattedOutput;
}

/**
Format the time series for output.
@return list of strings that are written to the file.
@param fname Name of output.
@param props Property list containing output modifiers.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( String fname, PropList props )
throws TSException {
	String message = null;
	List<String> formattedOutput = null;
	PrintWriter	stream = null;
	String full_fname = IOUtil.getPathUsingWorkingDir(fname);

	// First open the output file.

	try {
	    stream = new PrintWriter ( new FileWriter(full_fname) );
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + full_fname + "\"";
		throw new TSException ( message );
	}

	try {
	    formattedOutput = formatOutput ( stream, props );
	}
	catch ( TSException e ) {
		// Rethrow.
		throw e;
	}
	finally {
		if ( stream != null ) {
			stream.close();
		}
	}

	// Also return the list (consistent with C++ single return type.

	return formattedOutput;
}

/**
Format the body of the report for 24-hour data.
@param strings list of strings to be used as output.
@param props Properties to control output.
@param calendar Calendar to use for output.
@param dataFormat Format for data values (C printf style).
@param startDate Start date for output.
@param endDate End date for output.
@param reqUnits Requested units for output.
@param totalColumn indicates whether total column is total or average.
*/
private void formatOutput24Hour ( List<String> strings, PropList props, YearType calendar, String dataFormat,
					DateTime startDate, DateTime endDate, String reqUnits, String totalColumn ) {
	String propValue = null, routine = getClass().getSimpleName() + ".formatOutput24Hour";
	int	column = 0, dl = 20, row = 0;

	strings.add ( "" );

	// Now transfer the monthly data into a summary matrix, that looks like:
	//
	// Day Month....
	// 1
	// ...
	// 31
	// statistics
	//
	// Repeat for each year.

	// Adjust the start and end dates to be on full years for the calendar that is requested.

	int yearOffset = 0;
	int monthToStart = 1;	// First month in year.
	int monthToEnd = 12; // Last month in year.
	if ( calendar == YearType.CALENDAR ) {
		// Just need to output for the full year.
		startDate.setMonth ( 1 );
		startDate.setDay ( 1 );
		endDate.setMonth ( 12 );
		endDate.setDay ( 31 );
		monthToStart = 1;
		monthToEnd = 12;
	}
	else if ( calendar == YearType.NOV_TO_OCT ) {
		// Need to adjust for the irrigation year to make sure that the first month is November and the last is October.
		if ( startDate.getMonth() < 11 ) {
			// Need to shift to include the previous irrigation year.
			startDate.addYear ( -1 );
		}
		// Always set the start month to November.
		startDate.setMonth ( 11 );
		startDate.setDay ( 1 );
		if ( endDate.getMonth() > 11 ) {
			// Need to include the next irrigation year.
			endDate.addYear ( 1 );
		}
		// Always set the end month to Oct.
		endDate.setMonth ( 10 );
		endDate.setDay ( 31 );
		// The year that is printed in the summary is actually later than the calendar for the November month.
		yearOffset = 1;
		monthToStart = 11;
		monthToEnd = 10;
	}
	else if ( calendar == YearType.WATER ) {
		// Need to adjust for the water year to make sure that the first month is October and the last is September.
		if ( startDate.getMonth() < 10 ) {
			// Need to shift to include the previous water year.
			startDate.addYear ( -1 );
		}
		// Always set the start month to October.
		startDate.setMonth ( 10 );
		startDate.setDay ( 1 );
		if ( endDate.getMonth() > 9 ) {
			// Need to include the next water year.
			endDate.addYear ( 1 );
		}
		// Always set the end month to September.
		endDate.setMonth ( 9 );
		endDate.setDay ( 30 );
		// The year that is printed in the summary is actually later than the calendar for the October month.
		yearOffset = 1;
		monthToStart = 10;
		monthToEnd = 9;
	}
	// Calculate the number of years.
	// Line up with the same month as the start month.
	int num_years = (endDate.getAbsoluteMonth() - startDate.getAbsoluteMonth() + 1)/12;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Printing " + num_years + " years of summary for " +
		startDate.toString(DateTime.FORMAT_YYYY_MM) + " to " + endDate.toString(DateTime.FORMAT_YYYY_MM) );
	}
	// Reuse for each month that is printed.
	double data[][] = new double[31][12];

	// Now loop through the time series and transfer to the proper location in the matrix.
	// Since days are vertical, cannot print any results until have completed a month.
	double dataValue;
	DateTime date = new DateTime(startDate,DateTime.DATE_FAST);
	StringBuffer buffer = new StringBuffer();	// Allocate up front and then reuse.
	// Have adjusted the dates above, so always start in column 0 (first day of first month in year).
	column = 0;
	row = 0;
	double missing = getMissing();
	int ndaysInMonth = 0;
	int day, month;
	for ( ; date.lessThanOrEqualTo(endDate); date.addInterval(_data_interval_base, _data_interval_mult) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Processing " + date.toString(DateTime.FORMAT_YYYY_MM_DD) +
			" row:" + row + " column:" + column );
		}
		// Figure out if this is a new year.  If so, reset the headers, etc.
		day = date.getDay();
		month = date.getMonth();
		if ( day == 1 ) {
			if ( month == monthToStart ) {
				// Reset the data array.
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Resetting output array..." );
				}
				for ( int irow = 0; irow < 31; irow++ ){
					for ( int icolumn = 0; icolumn < 12; icolumn++){
						data[irow][icolumn] = missing;
					}
				}
			}
			ndaysInMonth = TimeUtil.numDaysInMonth ( month, date.getYear() );
		}
		dataValue = getDataValue ( date );
		// Save the data value for later use in output and statistics.  Allow missing data values to be saved.
		data[row][column] = dataValue;
		// Check to see if at the end of the year.  If so, print out one year's values.
		if ((month == monthToEnd) && (day == ndaysInMonth)){
			// Print the header for the year.
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				    "Printing output for summary year " + (date.getYear() + yearOffset) );
			}
			strings.add ( "" );
			// "date" will be at the end of the year.
			if ( calendar == YearType.WATER ) {
				strings.add ("                                                Water Year " +
				date.getYear() + " (Oct " + (date.getYear() - 1) + " to Sep " + date.getYear() + ")" );
			}
			else if ( calendar == YearType.NOV_TO_OCT ){
				strings.add ( "                                                 Irrigation Year " +
				date.getYear() + " (Nov " + (date.getYear() - 1) + " to Oct " + date.getYear() + ")" );
			}
			else {
			    strings.add ( "                                                                    Calendar Year " +
				date.getYear() );
			}
			strings.add ( "" );
			if ( calendar == YearType.WATER ) {
				// Water year.
				strings.add (
"Day     Oct       Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep       " );
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
			}
			else if ( calendar == YearType.NOV_TO_OCT ){
				// Irrigation year.
				strings.add (
"Day     Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul       Aug        Sep      Oct       " );
					strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
			}
			else {
			    // Calendar year.
				strings.add (
"Day     Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep       Oct       Nov       Dec       " );
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
			}
			// Now print the summary for the year.
			int rowDay, columnMonth;
			int irow, icolumn;
			int year, nvalidDaysInMonth;
			for ( irow = 0; irow < 31; irow++ ) {
				rowDay = irow + 1;
				for ( icolumn = 0; icolumn < 12; icolumn++ ) {
					columnMonth = monthToStart + icolumn;
					if ( icolumn == 0 ) {
						// Allocate a new buffer and print the day for all 12 months.
						buffer.setLength(0);
						buffer.append ( StringUtil.formatString(rowDay, " %2d ") );
					}
					// Print the daily value.
					// Figure out if the day is valid for the month.
					// The date is for the end of the year (last month) from the loop.
					year = date.getYear();
					if ( (calendar == YearType.WATER) && (columnMonth > 9) ) {
						--year;
					}
					else if ( (calendar == YearType.NOV_TO_OCT) && (columnMonth > 10) ) {
						--year;
					}
					nvalidDaysInMonth = TimeUtil.numDaysInMonth ( columnMonth, year );
					if ( rowDay > nvalidDaysInMonth ) {
						buffer.append ( "    ---   " );
					}
					else if ( isDataMissing( data[irow][icolumn])){
						buffer.append ( "    NC    " );
					}
					else {
					    buffer.append (	StringUtil.formatString(data[irow][icolumn], " " + dataFormat) );
					}
					if ( icolumn == 11 ) {
						// Have processed the last month in the year print the row.
						strings.add( buffer.toString() );
					}
				}
			}
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
			// Now to do the statistics.  Loop through each column.
			// First check to see if all statistics should be printed (can be dangerous if add new statistics).
			propValue = props.getValue ( "PrintAllStats" );
			String printAllStats = null;
			if ( propValue == null ) {
				// Default is false.
				printAllStats = "false";
			}
			else {
			    printAllStats = propValue;
			}
			// Now start on the minimum.
			propValue = props.getValue ( "PrintMinStats" );
			String printMin = null;
			if ( propValue == null ) {
				// Default is true.
				printMin = "true";
			}
			else {
			    printMin = propValue;
			}
			if ( printMin.equalsIgnoreCase("true") || printAllStats.equalsIgnoreCase("true") ) {
				strings = StringUtil.addListToStringList ( strings, formatOutputStats ( data, "Min ", dataFormat ) );
			}
			propValue = props.getValue ( "PrintMaxStats" );
			String printMax = null;
			if ( propValue == null ) {
				// Default is true.
				printMax = "true";
			}
			else {
			    printMax = propValue;
			}
			if ( printMax.equalsIgnoreCase("true") || printAllStats.equalsIgnoreCase("true") ) {
				strings = StringUtil.addListToStringList ( strings, formatOutputStats ( data, "Max ", dataFormat ) );
			}
			propValue = props.getValue ( "PrintMeanStats");
			String printMean = null;
			if ( propValue == null ) {
				// Default is true.
				printMean = "true";
			}
			else {
			    printMean = propValue;
			}
			if ( printMean.equalsIgnoreCase("true") || printAllStats.equalsIgnoreCase("true") ) {
				strings = StringUtil.addListToStringList ( strings, formatOutputStats ( data, "Mean", dataFormat ) );
			}
			column = -1;	// Will be incremented in next step.
		}
		if ( day == ndaysInMonth ) {
			// Reset to the next column.
			++column;
			row = 0;
		}
		else {
		    ++row;
		}
	}

	// Now do the notes.

	propValue = props.getValue ( "PrintNotes" );
	String printNotes = null;
	if ( propValue == null ) {
		// Default is true.
		printNotes = "true";
	}
	else {
	    printNotes = propValue;
	}
	if ( printNotes.equalsIgnoreCase("true") ) {
		strings.add ( "" );
		strings.add ( "Notes:" );
		if ( calendar == YearType.WATER ) {
			strings.add ( "  Years shown are water years." );
			strings.add ( "  A water year spans Oct of the previous calendar year to Sep of the current " +
			"calendar year (all within the indicated water year).");
		}
		else if ( calendar == YearType.NOV_TO_OCT ) {
			strings.add ( "  Years shown span Nov of the previous calendar year to Oct of the current calendar year." );
		}
		else {
		    strings.add("  Years shown are calendar years.");
		}
		strings.add ( "  Annual values and statistics are computed only on non-missing data." );
		strings.add ( "  NC indicates that a value is not " +
		"computed because of missing data or the data value itself is missing." );
	}
}

/**
Format the body of the report for an N-hour data, less than 24 hours.
The output is a simple format with YYYY-MM-DD on the left and then the hours filling out the row.
If 1 hour data, two rows of values are used.  Otherwise, a single row is used.
The right column is the total for the day.  The data values are always a maximum of 9 characters.
@param strings Vector of strings to be used as output.
@param props Properties to control output.
@param calendar Calendar to use for output.
@param dataFormat Format for data values (C printf style).
@param startDate Start date for output.
@param endDate End date for output.
@param reqUnits Requested units for output.
@param totalColumn indicates whether total column is total or average.
*/
private void formatOutputNHour ( List<String> strings, PropList props, YearType calendar, String dataFormat,
					DateTime startDate, DateTime endDate, String reqUnits, String totalColumn ) {
	StringBuffer b = new StringBuffer();
	// Loop through the data starting at the appropriate first hour for the period.
	DateTime date = new DateTime ( startDate );
	date.setHour ( 0 );	// Always want full days.
	DateTime end = new DateTime ( endDate );
	end.setHour ( 24 - _data_interval_mult );
	int col = 0;	// col=0 is date, col=1 is first data column.
	int row = 1;	// Row within a day.
	double total = 0.0, value = 0.0;
	int count = 0;
	boolean do_total = false;
	if ( totalColumn.equalsIgnoreCase("Total") ) {
		do_total = true;
	}
	boolean firstHeader = true;
	int numcol = 24/_data_interval_mult;
	if ( _data_interval_mult == 1 ) {
		numcol = 12;
	}
	for ( ; date.lessThanOrEqualTo(end); date.addInterval(_data_interval_base,_data_interval_mult) ) {
		// Print a header if the first time or the day is 1.
		if ( firstHeader || ((date.getDay() == 1) && (date.getHour() == 0)) ) {
			firstHeader = false;
			strings.add ( "" );
			b.setLength(0);
			b.append ( "   Date   " );
			for ( int i = 0; i < numcol; i++ ) {
				if ( _data_interval_mult == 1 ) {
					b.append ( "   "+
					StringUtil.formatString(i,"%2d") + "/"+ StringUtil.formatString((i+12),"%2d") + "  ");
				}
				else {
				    b.append ( "    " + StringUtil.formatString( i*_data_interval_mult,"%2d") + "    ");
				}
			}
			b.append ( " " + totalColumn );
			strings.add ( b.toString() );
			// Now add the underlines for the headings.
			b.setLength(0);
			b.append ( "----------" );
			for ( int i = 0; i < (numcol + 1); i++ ) {
				b.append ( " ---------" );
			}
			strings.add ( b.toString() );
		}
		// Now do the data.
		if ( col == 0 ) {
			b.setLength(0);
			if ( row == 1 ) {
				// Add the date at the start of the line.
				b.append ( date.toString( DateTime.FORMAT_YYYY_MM_DD));
			}
			else {
			    b.append ( "          " );
			}
			++col;
		}
		// Add a data value.
		value = getDataValue ( date );
		if ( isDataMissing(value) ) {
			b.append ( "          " );
		}
		else {
		    b.append(" " + StringUtil.formatString(value,
			dataFormat) );
			total += value;
			++count;
		}
		++col;
		// Now check for the end of the line.
		// The last data value for the day will be in numcol (e.g. 12 for 1hour).
		// The above will then have incremented the value to 13.
		if ( col > numcol ) {
			if ( (_data_interval_mult == 1) && (row == 1) ) {
				// Need to start a new row.
				row = 2;
				strings.add ( b.toString() );
			}
			else {
			    // Need to output the row and the total.
				if ( do_total || (count == 0) ) {
					b.append(" " + StringUtil.formatString(total, dataFormat) );
				}
				else {
				    b.append(" " + StringUtil.formatString(total/count, dataFormat) );
				}
				strings.add ( b.toString() );
				row = 1;
				total = 0.0;
				count = 0;
			}
			col = 0;
		}
	}
}

/**
Format the output statistics row given the data array.
@param data Data to analyze.
@param label label for output (e.g., "Mean").
@param dataFormat Format to use for floating point data.
@return strings to be added to the output.
*/
private List<String> formatOutputStats ( double[][] data, String label, String dataFormat ) {
	List<String> strings = new ArrayList<>();
	double stat = 0.0;
	StringBuffer buffer = null;
	double [] array = new double[31];
	int column, row;

	for ( column = 0; column < 12; column++ ) {
		if ( column == 0 ) {
			buffer = new StringBuffer();
			// Label needs to be 4 characters.
			buffer.append ( label );
		}
		// Extract the non-missing values.
		int numNotMissing = 0;
		for ( row = 0; row < 31; row++ ) {
			if ( !isDataMissing( data[row][column])){
				++numNotMissing;
			}
		}
		if ( numNotMissing > 0 ) {
			// Transfer to an array.
			array = new double[numNotMissing];
			numNotMissing = 0;
			for ( row = 0; row < 31; row++ ){
				if ( !isDataMissing( data[row][column]) ) {
					array[numNotMissing] = data[row][column];
					++numNotMissing;
				}
			}
			stat = 0.0;
			try {
			    if ( label.startsWith ("Min") ) {
					stat = MathUtil.min ( array );
				}
				else if ( label.startsWith ("Max") ) {
					stat = MathUtil.max ( array );
				}
				else if ( label.startsWith ("Mean") ) {
					stat = MathUtil.mean ( array );
				}
			}
			catch ( Exception e ) {
			}
			buffer.append ( StringUtil.formatString( stat," " + dataFormat) );
		}
		else {
		    buffer.append ( "    NC    " );
		}
	}
	strings.add(buffer.toString() );
	strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	return strings;
}

/**
Return the data point corresponding to the date.
<pre>
             Hour data is stored in a two-dimensional array:
  		     |----------------> intervals
  		     |
  		    \|/
  		   month
</pre>
@param date date/time to get data.
@param tsdata if null, a new instance of TSData will be returned.
If non-null, the provided instance will be used
(this is often desirable during iteration to decrease memory use and increase performance).
@return a TSData for the specified date/time.
@see TSData
*/
public TSData getDataPoint ( DateTime date, TSData tsdata ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".getDataPoint";
	}
	if ( tsdata == null ) {
		// Allocate it.
		tsdata = new TSData();
	}
	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, routine, date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		tsdata.setValues ( date, _missing, _data_units, "", 0 );
		return tsdata;
	}
	// This computes the _row and _column.
	// TODO SAM 2012-05-03 Enable duration later.
	getDataPosition ( date );
	if ( _has_data_flags ) {
	    if ( _internDataFlagStrings ) {
	        tsdata.setValues ( date, getDataValue(date), _data_units,
                    _dataFlags[_row][_column].intern(), 0 ); //_has_durations ? _durations[_row][_column] : 0 );
	    }
	    else {
	        tsdata.setValues ( date, getDataValue(date), _data_units,
                    _dataFlags[_row][_column], 0 ); //_has_durations ? _durations[_row][_column] : 0 );
	    }
	}
	else {
	    tsdata.setValues ( date, getDataValue(date), _data_units, "", 0 );
               // _has_durations ? _durations[_row][_column] : 0 );
	}

	return tsdata;
}

/**
Compute the data position corresponding to the date.
<pre>
            Hour data is stored in a two-dimensional array:
  		     |----------------> intervals
  		     |
  		    \|/
  		   month
</pre>
@return row and column positions in an int[].
Note that this array is reused and the contents of the array may change.
This information is typically only used internally.
@param date Date of interest.
*/
private int [] getDataPosition ( DateTime date ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".getDataPosition";
	}
	// String tzshift;

	if ( date == null ) {
		return null;
	}

	// This is where would calculate the shift between the requested time zone and the time zone that have stored.
	// For now, just check the time zones against each other and print a warning if not compatible.
	// When there is time, use the HM routines to calculate the shift.

	/* TODO SAM Comment out for now - don't know how to handle.
	tz = date.getTimeZoneAbbr();
	tz1 = _date1.getTimeZoneAbbr();
	if ( (tz.length() == 0) || tz.equals(tz1) ) {
		// Are OK doing a straight retrieval.
		tzshift = 0;
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printWarning ( 10, routine,
			"Do not know how to shift time zones yet (\"" + tz1 + "\" to \"" + tz + "\"" );
			tzshift = 0;
		}
	}
	 */

	// Don't check date range because calling routine should have done so.

	// Calculate the row position of the data.

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, routine,
		"Using " + date + "(" + date.getAbsoluteMonth() + ") and start date: " +
		_date1 + "(" + _date1.getAbsoluteMonth() + ") for row-col calculation." );
	}

	_row = date.getAbsoluteMonth() - _date1.getAbsoluteMonth();

    // Calculate the column position of the data.
	// Hourly data is stored in a 2 dimensional array with the column being the hourly data by interval.

    _column = ((date.getDay()-1)*24 + date.getHour())/_data_interval_mult;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 50, routine, "Row=" + _row + " Column=" + _column );
	}

	_pos[0] = _row;
	_pos[1] = _column;
	return _pos;
}

/**
Return the data value for the date.
<pre>
             Hour data is stored in a two-dimensional array:
  		     |----------------> intervals
  		     |
  		    \|/
  		   month
</pre>
@return The data value corresponding to the date, or the missing data value if the date is not found.
@param date Date of interest (checked to hourly precision).
*/
public double getDataValue( DateTime date ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".getDataPosition";
	}

	// Check the date coming in.

	if ( (date == null) || !hasData() ) {
		return _missing;
	}

	if(	(date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to improve performance.
			Message.printWarning( 2, routine, date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		return _missing;
	}

	// Calculate the data position.  This should be safe to call since checked dates above.

	getDataPosition(date);

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, routine,
		_data[_row][_column] + " for " + date + " from _data[" + _row + "][" + _column + "]" );
	}

	return _data[_row][_column];
}

// TODO smalers 2023-04-16 need to move UI code out of this data class.
/**
Returns the data in the specified DataFlavor, or null if no matching flavor exists.
From the Transferable interface.  Supported data flavors are:<br>
<ul>
<li>HourTS - HourTS.class / RTi.TS.HourTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(hourTSFlavor)) {
		return this;
	}
	else if (flavor.equals(TS.tsFlavor)) {
		return this;
	}
	else if (flavor.equals(TSIdent.tsIdentFlavor)) {
		return _id;
	}
	else {
		return null;
	}
}

// TODO smalers 2023-04-16 need to move UI code out of this data class.
/**
Returns the flavors in which data can be transferred.  From the Transferable interface.
The order of the data flavors that are returned are:<br>
<ul>
<li>HourTS - HourTS.class / RTi.TS.HourTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[3];
	flavors[0] = hourTSFlavor;
	flavors[1] = TS.tsFlavor;
	flavors[2] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Indicate whether the time series has data, determined by checking to see whether the data space has been allocated.
This method can be called after a time series has been read.
Even if no data are available, the header information may be complete.
The alternative of returning a null time series from a read method if no data
are available results in the header information being unavailable.
Instead, return a TS with only the header information and call hasData() to check to see if the data space has been assigned.
@return true if data are available (the data space has been allocated).
Note that true will be returned even if all the data values are set to the missing data value.
*/
public boolean hasData () {
	if ( _data != null ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Initialize the data.
*/
private void init() {
	_data = null;
	_data_interval_base = TimeInterval.HOUR;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.HOUR;
	_data_interval_mult_original = 1;
	_pos = new int[2];
	_pos[0] = 0;
	_pos[1] = 0;
	_row = 0;
	_column = 0;
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.  Supported data flavors are:<br>
<ul>
<li>HourTS - HourTS.class / RTi.TS.HourTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(hourTSFlavor)) {
		return true;
	}
	else if (flavor.equals(TS.tsFlavor)) {
		return true;
	}
	else if (flavor.equals(TSIdent.tsIdentFlavor)) {
		return true;
	}
	else {
		return false;
	}
}

/**
 * Indicate whether an irregular interval time series (always false).
 * @return false always
 */
@Override
public boolean isIrregularInterval () {
	return false;
}

/**
 * Indicate whether a regular interval time series (always true).
 * @return true always
 */
@Override
public boolean isRegularInterval () {
	return true;
}

/**
Refresh the derived data (e.g., data limits) if data have been set.
This is normally only called from other package routines.
*/
public void refresh () {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".refresh";
	}

	// If the data are not dirty, then do not have to refresh the other information.

	if ( !_dirty ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, routine, "Time series is not dirty.  Not recomputing limits" );
		}
		return;
	}

	// Else need to refresh.

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, routine, "Time Series is dirty. Recomputing limits" );
	}

	TSLimits limits = TSUtil.getDataLimits ( this, _date1, _date2, false );
	if ( limits.areLimitsFound() ) {
		// Now reset the limits for the time series.
		setDataLimits ( limits );
	}

	_dirty = false;
}

/**
Set the data value for the date.
@param date Date of interest.
@param value Data value corresponding to date.
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue( DateTime date, double value ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setDataValue";
	}

	if ( date == null ) {
		return 0;
	}

	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to optimize performance.
			Message.printWarning( 10, routine, "Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return 0;
	}

	// Get the data position.  This should be safe because checked dates above.

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, routine, "Setting " + value + " for " + date + " at " + _row + "," + _column );
	}

	// Set the dirty flag so that know to recompute the limits if desired.

	_dirty = true;

	_data[_row][_column] = value;

	return 1;
}

/**
Set the data value and associated information for the date.
@param date Date of interest.
@param value Data value corresponding to date.
@param data_flag data_flag Data flag for value.
@param duration Duration for value (if null, ignored - assumed to be 1-day or instantaneous depending on data type).
@param return the number of values set, 0 or 1, useful to know when a value is outside the allocated period
*/
public int setDataValue ( DateTime date, double value, String data_flag, int duration ) {
	String routine = null;
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setDataValue";
	}
	if( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 10, routine, "Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return 0;
	}

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, routine, "Setting " + value + " for " + date + " at " + _row + "," + _column );
	}

	// Set the dirty flag so that know to recompute the limits if desired.

	_dirty = true;

	_data[_row][_column] = value;
    if ( (data_flag != null) && (data_flag.length() > 0) ) {
        if ( !_has_data_flags ) {
            // Trying to set a data flag but space has not been allocated, so allocate the flag space.
            try {
                allocateDataFlagSpace(null, false );
            }
            catch ( Exception e ) {
                // Generally should not happen - log as debug because could generate a lot of warnings.
                if ( Message.isDebugOn ) {
                    Message.printDebug(30, routine, "Error allocating data flag space (" + e + ") - will not use flags." );
                }
                // Make sure to turn flags off.
                _has_data_flags = false;
            }
        }
    }
	if ( _has_data_flags && (data_flag != null) ) {
	    if ( _internDataFlagStrings ) {
	        _dataFlags[_row][_column] = data_flag.intern();
	    }
	    else {
	        _dataFlags[_row][_column] = data_flag;
	    }
	}

	/** TODO SAM 2012-05-03 Enable later.
    if ( (duration != 0) && Integer.valueOf(duration) != null) {
        if (!_has_durations) {
            // Space has not been allocated for durations, so allocate it.
            try {
                allocateDataDurationSpace(null, false);
            }
            catch (Exception e) {
                if (Message.isDebugOn) {
                    Message.printDebug(30, "HourTS.setDataValue", "Error allocating data duration space (" + e
                            + ") - will not use durations.");
                }
                _has_durations = false;
            }
        }
    }
    if (_has_durations && Integer.valueOf(duration) != null) {
        _durations[_row][_column] = duration;
    }
    */
	return 1;
}

}