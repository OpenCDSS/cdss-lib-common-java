// ----------------------------------------------------------------------------
// MinuteTS - base class from which all minute time series are derived
// ----------------------------------------------------------------------------
// Notes:	(1)	This base class is provided so that specific minute
//			time series can derived from this class.
//		(2)	Data for this time series interval is stored as follows:
//
//			The first dimension is the absolute month, similar to
//			HourTS.  The second dimension is the days in the month.
//			The third dimension is the number of minute data
//			intervals within the day.
//
//			So, the base block of storage is the month and day.
//			This lends itself to very fast data retrieval but may
//			waste some memory for short time series in which full
//			days are not stored.  This is considered a reasonable
//			tradeoff.
// ----------------------------------------------------------------------------
// History:
//
// 26 Feb 1998	Steven A. Malers, RTi	Copy HourTS and modify.  Compare to the
//					existing C++ version.
// 06 Aug 1998	SAM, RTi		Optimize data set/get by modifying
//					getDataPosition to use class data.
// 22 Aug 1998	SAM, RTi		Change getDataPosition to return int[].
// 13 Apr 1999	SAM, RTi		Add finalize.
// 21 Feb 2001	SAM, RTi		Add clone() and copy constructor.
//					Remove printSample() and read/write
//					methods.
// 28 Aug 2001	SAM, RTi		Fix clone().  Clean up javadoc.  Set
//					unused variables to null.
// 09 Sep 2001	SAM, RTi		Update formatOutput() to actually do a
//					nice report (previously was not
//					supported - copy the HourTS code and
//					modify as needed).  Only support summary
//					output since the delimited and other
//					formats can be produced by DateValueTS,
//					etc.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
//					Remove constructor that takes a file
//					name.  Change some methods to have void
//					return type to agree with base class.
// 2003-01-08	SAM, RTi		Add hasData().
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TSTimeZone to TZ.
//					* Change TS.INTERVAL* to TimeInterval.
// 2004-01-26	SAM, RTi		* Add OutputStart and OutputEnd
//					  properties to formatOutput().
//					* In formatOutput(), convert the file
//					  name to a full path.
// 2004-03-04	J. Thomas Sapienza, RTi	* Class now implements Serializable.
//					* Class now implements Transferable.
//					* Class supports being dragged or 
//					  copied to clipboard.
// 2005-06-02	SAM, RTi		* Fix getDataPoint() to return a TSData
//					  with missing data if the date is
//					  outside the period of record.
//					* Add _tsdata to increase peformance.
//					* Remove warning about reallocating
//					  memory.
//					* Add support for data flags.
//					* Fix bug in clone - it was not properly
//					  handling unallocated space at the
//					  beginning and ends of months.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

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
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
The MinuteTS class is the base class for time series used to store minute data.
Derive classes from this class for specific minute time series data.
*/
@SuppressWarnings("serial")
public class MinuteTS extends TS implements Cloneable, Serializable, Transferable {

// Data members...

//FIXME SAM 2009-01-15 Need to separate the data transfer code from normal classes
/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor minuteTSFlavor = new DataFlavor(RTi.TS.MinuteTS.class, "RTi.TS.MinuteTS");

/**
Data space for minute time series.  The dimensions are [month][day][minute value].
*/
private	double[][][] _data;

/**
Data flags for each data value.  The dimensions are [month][day][minute value].
*/
private	String [][][] _dataFlags;

/**
Month position in data array, set by getDataPosition() and used internally.
*/
private int _month_pos;

/**
Day position in data array, set by getDataPosition() and used internally.
*/
private int _day_pos;

/**
Interval position in data array, set by getDataPosition() and used internally.
*/
private int _interval_pos;

/**
Return for the following to optimize memory use.
*/
private int [] _pos = null;

/**
Default constructor.
*/
public MinuteTS ( )
{	super ();
	init();
}

/**
Copy constructor.  Everything is copied by calling copyHeader() and then copying the data values.
@param ts MinuteTS to copy.
*/
public MinuteTS ( MinuteTS ts )
{	if ( ts == null ) {
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
}

/**
Allocate the data flag space for the time series.  This requires that the data
interval base and multiplier are set correctly and that _date1 and _date2 have
been set.  The allocateDataSpace() method will allocate the data flags if
appropriate.  Use this method when the data flags need to be allocated after the initial allocation.
@param initialValue Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retain_previous_values If true, the array size will be increased if necessary, but
previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initialValue, boolean retain_previous_values )
throws Exception
{	String	routine="MinuteTS.allocateDataFlagSpace", message;
	int	i;

	if ( (_date1 == null) || (_date2 == null) ) {
		message ="Dates have not been set.  Cannot allocate data space";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	if ( (_data_interval_mult < 1) || (_data_interval_mult > 60) ) {
		message = "Only know how to handle 1-60 minute data, not " + _data_interval_mult + "-minute";
		Message.printWarning ( 3, routine, message );
		throw new Exception ( message );
	}
	
	if ( initialValue == null ) {
	    initialValue = "";
	}
	
	int nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if ( nmonths == 0 ) {
		message="TS has 0 months POR, maybe Dates haven't been set yet";
		Message.printWarning( 2, routine, message );
		throw new Exception ( message );
	}

	String [][][] dataFlagsPrev = null;
	if ( _has_data_flags && retain_previous_values ) {
		// Save the reference to the old flags array...
		dataFlagsPrev = _dataFlags;
	}
	else {
	    // Turn on the flags...
		_has_data_flags = true;
	}
	// Top-level allocation...
	_dataFlags = new String[nmonths][][];

	// Set the counter date to match the starting month.  This date is used
	// to determine the number of days in each month.

	DateTime date = new DateTime(DateTime.DATE_FAST);
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	// Allocate memory...

	int j = 0, k = 0;
	int nvals = 0;
	int ndays_in_month = 0;
	int day;
	boolean internDataFlagStrings = getInternDataFlagStrings();
	for ( i = 0; i < nmonths; i++, date.addMonth(1) ) {
		ndays_in_month = TimeUtil.numDaysInMonth ( date );

		_dataFlags[i] = new String[ndays_in_month][];
		for ( j = 0; j < ndays_in_month; j++ ) {
			if ( i == 0 ) {
				// In the first month.  If the day is less than
				// the first day in the period, do not use up memory...
				day = j + 1;
				if ( day < _date1.getDay() ) {
					continue;
				}
			}
			else if ( (i + 1) == nmonths ) {
				// In the last month.  If the day is greater
				// than the last day in the period, do not use up memory...
				day = j + 1;
				if ( day > _date2.getDay() ) {
					continue;
				}
			}
			// Else we do allocate memory for some data during the day.
			// If a non-valid interval, an exception was thrown above...
			nvals = 24*(60/_data_interval_mult);
			_dataFlags[i][j] = new String[nvals];

			// Now fill with the initial data value...

			for ( k = 0; k < nvals; k++ ) {
			    if ( internDataFlagStrings ) {
			        _dataFlags[i][j][k] = initialValue.intern();
			    }
			    else {
			        _dataFlags[i][j][k] = initialValue;
			    }
				if ( retain_previous_values && (dataFlagsPrev != null)){
					// Copy over the old values (typically shorter character arrays)...
				    if ( internDataFlagStrings ) {
						_dataFlags[i][j][k] = dataFlagsPrev[i][j][k].intern();
				    }
				    else {
				        _dataFlags[i][j][k] = dataFlagsPrev[i][j][k];
				    }
				}
			}
		}
	}
}

/**
Allocate the data space for the time series.  The beginning and ending dates
and interval multiplier must have been set.
@return 0 if successful, 1 if failure.
*/
public int allocateDataSpace( )
{	String	routine="MinuteTS.allocateDataSpace";
	int	dl = 10, imon, ndays_in_month, nmonths=0, nvals;

	if ( (_date1 == null) || (_date2 == null) ) {
		Message.printWarning ( 3, routine, "No dates set for memory allocation" );
		return 1;
	}
	if ( (_data_interval_mult < 1) || (_data_interval_mult > 60) ) {
		Message.printWarning ( 3, routine,
		"Only know how to handle 1-60 minute data, not " + _data_interval_mult + "-minute" );
		return 1;
	}
	nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if( nmonths == 0 ){
		Message.printWarning( 2, routine, "TS has 0 months POR, maybe Dates haven't been set yet" );
		return 1;
	}

	_data = new double[nmonths][][];
	if ( _has_data_flags ) {
		_dataFlags = new String[nmonths][][];
	}

	// Probably need to catch an exception here in case we run out of memory.

	// Set the counter date to match the starting month.  This date is used
	// to determine the number of days in each month.

	DateTime date = new DateTime ( DateTime.DATE_FAST );
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	int day, iday, k;
	for ( imon = 0; imon < nmonths; imon++, date.addMonth(1) ) {
		ndays_in_month = TimeUtil.numDaysInMonth ( date );

		// Allocate the memory for the number of days in the month...

		_data[imon] = new double [ndays_in_month][];
		if ( _has_data_flags ) {
			_dataFlags[imon] = new String[ndays_in_month][];
		}

		// Now allocate the memory for the number of intervals in the
		// day.  Need to do this to save on memory:  if a day is not in
		// our period, skip this step but do allocate the full day for
		// any day that does occur in the period.

		for ( iday = 0; iday < ndays_in_month; iday++ ) {
			if ( imon == 0 ) {
				// In the first month.  If the day is less than
				// the first day in the period, do not use up
				// memory.  The position will never be accessed.
				day = iday + 1;
				if ( day < _date1.getDay() ) {
					continue;
				}
			}
			else if ( (imon + 1) == nmonths ) {
				// In the last month.  If the day is greater than the last day in the period, do not use
				// up memory.  The position will never be accessed.
				day = iday + 1;
				if ( day > _date2.getDay() ) {
					continue;
				}
			}
			// Else we do allocate memory for some data during the day...
			// Easy to handle 1-60 minute data...
			// 24 = number of hours in the day...
			nvals = 24*(60/_data_interval_mult);
			_data[imon][iday] = new double[nvals];
			if ( _has_data_flags ) {
				_dataFlags[imon][iday] = new String[nvals];
			}

			// Now fill the entire month with the missing data value...

			for ( k = 0; k < nvals; k++ ) {
				_data[imon][iday][k] = _missing;
				if ( _has_data_flags ) {
					_dataFlags[imon][iday][k] = "";
				}
			}
		}
	}

	// Use the static routine to compute the data size...

	int nactual = calculateDataSize ( _date1, _date2, _data_interval_mult );
	setDataSize ( nactual );

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Allocated " + _data_interval_mult + "-minute data space from " +
		_date1.toString() + " to " + _date2.toString() + " (" + nactual + " values)" );
	}
	return 0;
}

/**
Determine the number of points between two dates.
@return The number of data points for a minute time series
given the data interval multiplier for the specified period.
@param start_date The first date of the period.
@param end_date The last date of the period.
@param interval_mult The time series data interval multiplier.
*/
public static int calculateDataSize ( DateTime start_date, DateTime end_date, int interval_mult )
{	String routine = "MinuteTS.calculateDataSize";
	int datasize = 0;

	if ( start_date == null ) {
		Message.printWarning ( 2, routine, "Start date is null" );
		routine = null;
		return 0;
	}
	if ( end_date == null ) {
		Message.printWarning ( 2, routine, "End date is null" );
		routine = null;
		return 0;
	}

	// First set to the number of data in the months...
	datasize = TimeUtil.numDaysInMonths (
		start_date.getMonth(), start_date.getYear(),
		end_date.getMonth(), end_date.getYear() )
		*24*60/interval_mult;
	// Now subtract off the data at the ends that are missing...
	// Start by subtracting the full day at the beginning of the
	// month that is not included...
	datasize -= (start_date.getDay() - 1)*24*60/interval_mult;
	// Now subtract the recordings on the first day before the first time...
	datasize -= start_date.getHour()*60/interval_mult;
	// Now subtract off the data at the end...
	// Start by subtracting the full days off the end of the month...
	int ndays_in_month = TimeUtil.numDaysInMonth ( end_date.getMonth(), end_date.getYear() );
	datasize -= (ndays_in_month - end_date.getDay())*24*60/interval_mult;
	// Now subtract the readings at the end of the last day...
	datasize -= (23 - end_date.getHour())*60/interval_mult;
	return datasize;
}

/** 
Change the period of record to the specified dates.  If the period is extended,
missing data will be used to fill the time series.  If the period is shortened, data will be lost.
@param date1 New start date of time series.
@param date2 New end date of time series.
@exception RTi.TS.TSException if there is a problem extending the data.
*/
public void changePeriodOfRecord ( DateTime date1, DateTime date2 )
throws TSException
{	String	routine="MinuteTS.changePeriodOfRecord";
	String	message;

	// To transfer, allocate a new data space.  In any case, need to get the dates established...
	if ( (date1 == null) && (date2 == null) ) {
		// No dates.  Cannot change.
		message = "\"" + _id + "\": period dates are null.  Cannot change the period.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Allowed to have one be null...

	DateTime new_date1 = null;
	if ( date1 == null ) {
		// Use the original date...
		new_date1 = new DateTime ( _date1 );
	}
	else {
	    // Use the date passed in...
		new_date1 = new DateTime ( date1 );
	}
	DateTime new_date2 = null;
	if ( date2 == null ) {
		// Use the original date...
		new_date2 = new DateTime ( _date2 );
	}
	else {
	    // Use the date passed in...
		new_date2 = new DateTime ( date2 );
	}

	// Do not change the period if the dates are the same...

	if ( _date1.equals(new_date1) && _date2.equals(new_date2) ) {
		// No need to change period...
		return;
	}

	// To transfer the data (later), get the old position and then set in
	// the new position.  To get the right data position, declare a
	// temporary HourTS with the old dates and save a reference to the old data...

	double [][][] dataSave = _data;
	String [][][] dataFlagsSave = _dataFlags;
	MinuteTS temp_ts = new MinuteTS ();
	temp_ts.setDataInterval ( TimeInterval.MINUTE, _data_interval_mult );
	temp_ts.setDate1 ( _date1 );
	temp_ts.setDate2 ( _date2 );

	// Also compute limits for the transfer to optimize performance...

	DateTime transfer_date1 = null;
	DateTime transfer_date2 = null;
	if ( new_date1.lessThan(_date1) ) {
		// Extending so use the old date...
		transfer_date1 = new DateTime ( _date1 );
	}
	else {
	    // Shortening so use the new...
		transfer_date1 = new DateTime ( new_date1 );
	}
	if ( new_date2.greaterThan(_date2) ) {
		// Extending so use the old date...
		transfer_date2 = new DateTime ( _date2 );
	}
	else {
	    // Shortening so use the new...
		transfer_date2 = new DateTime ( new_date2 );
	}

	// Now reset the dates and reallocate the period...

	setDate1 ( new_date1 );
	setDate2 ( new_date2 );
	allocateDataSpace();

	// At this point the data space will be completely filled with missing data.

	// Now transfer the data.  To do so, get the
	// old position and then set in the new position.  We are only concerned
	// with transferring the values for the the old time series that are within the new period...

	double value;
	int [] data_pos;
	for ( DateTime date = new DateTime (transfer_date1,DateTime.DATE_FAST);
		date.lessThanOrEqualTo (transfer_date2);
		date.addInterval( _data_interval_base, _data_interval_mult ) ) {
		// Get the data position for the old data...
		data_pos = temp_ts.getDataPosition(date);
		// Now get the value...
		value = dataSave[data_pos[0]][data_pos[1]][data_pos[2]];
		// Now set in the new period...
		// Also transfer the data flag...
		if ( _has_data_flags ) {
			// Transfer the value and flag...
		    if ( _internDataFlagStrings ) {
		        setDataValue ( date, value, dataFlagsSave[data_pos[0]][data_pos[1]][data_pos[2]].intern(), 1);
		    }
		    else {
		        setDataValue ( date, value, dataFlagsSave[data_pos[0]][data_pos[1]][data_pos[2]], 1);
		    }
		}
		else {
		    // Transfer the value...
			setDataValue ( date, value );
		}
	}

	// Add to the genesis...

	addToGenesis ( "Changed period from: " + temp_ts.getDate1() + " - " +
		temp_ts.getDate2() + " to " + new_date1 + " - " + new_date2 );
}

/**
Clone the object.  The TS base class clone() method is called and then the
the data array is cloned.  The result is a complete deep copy.
*/
public Object clone ()
{	MinuteTS ts = (MinuteTS)super.clone();	// Clone data stored in the base
	// Does not appear to work...
	//ts._data = (double[][][])_data.clone();
	//ts._pos = (int[])_pos.clone();
    if ( _data == null ) {
        ts._data = null;
    }
    else {
    	ts._data = new double[_data.length][][];
    	for ( int imon = 0; imon < _data.length; imon++ ) { // Months
    		ts._data[imon] = new double[_data[imon].length][];
    		for ( int iday = 0; iday < _data[imon].length; iday++ ) { // Days in month
    			if ( _data[imon][iday] == null ) {
    				// Memory is not allocated for days at the beginning and
    				// ends of months (outside the period), in order
    				// to save memory and increase performance.
    				continue;
    			}
    			ts._data[imon][iday] = new double[_data[imon][iday].length];
    			System.arraycopy ( _data[imon][iday], 0, ts._data[imon][iday], 0,_data[imon][iday].length);
    		}
    	}
    }
	boolean internDataFlagStrings = getInternDataFlagStrings();
	if ( _has_data_flags ) {
	    if ( _dataFlags == null ) {
	        // Original time series did not have flags so can't copy
	        // TODO SAM 2011-12-07 Should this be an error?
	        ts._dataFlags = null;
	    }
	    else {
    		// Allocate months...
    		ts._dataFlags = new String[_dataFlags.length][][];
    		for ( int imon = 0; imon < _dataFlags.length; imon++ ) {
    			// Allocate days in month...
    			ts._dataFlags[imon] = new String[_dataFlags[imon].length][];
    			for( int iday = 0; iday < _dataFlags[imon].length; iday++){
	                if ( _dataFlags[imon][iday] == null ) {
	                    // Memory is not allocated for days at the beginning and
	                    // ends of months (outside the period), in order
	                    // to save memory and increase performance.
	                    continue;
	                }
    				// Allocate data values in day...
    				ts._dataFlags[imon][iday] = new String[_dataFlags[imon][iday].length];
    				for ( int ival = 0; ival < _dataFlags[imon][iday].length; ival++ ) {
    				    // Allocate values for minute intervals in day...
    				    if ( internDataFlagStrings ) {
    				        ts._dataFlags[imon][iday][ival] = _dataFlags[imon][iday][ival].intern();
    				    }
    				    else {
    				        ts._dataFlags[imon][iday][ival] = _dataFlags[imon][iday][ival];
    				    }
    				}
    			}
    		}
	    }
	}
	ts._pos = new int[3];
	ts._pos[0] = _pos[0];
	ts._pos[1] = _pos[1];
	ts._pos[2] = _pos[2];
	ts._month_pos = _month_pos;
	ts._day_pos = _day_pos;
	ts._interval_pos = _interval_pos;
	return ts;
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_data = null;
	_dataFlags = null;
	_pos = null;
	super.finalize();
}

/**
Format the time series for output.  This is not meant to be used for time
series conversion but to produce a general summary of the output.  At this time,
minute time series are always output in a matrix summary format.
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
<td>Print the comments associated with the time series.  This may contain
information about the quality of data, station information, etc.  This
information is usually variable-length text, and may not be available.
</td>
<td>true</td>
</tr>

<tr>
<td><b>PrintAllStats</b></td>
<td>Print all the statistics (currently maximum, minimum, and mean, although
standard deviation and others are being added).  Because statistics are being
added to output, it is advised that if formatting is to remain the same over
time, that output items be individually specified.  One way of doing this is
to turn all the statistics off and then turn specific items on (to true).
</td>
<td>false</td>
</tr>

<tr>
<td><b>PrintGenesis</b></td>
<td>Print the time series genesis information after the header in a
format as follows:
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
<td>Print notes about the output.  This consists of helpful information used
to understand the output (but does not consist of data).  For example:
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
<td>Use the time series comments for the header and do not print other header
information.  This can be used when the entire header is formatted elsewhere.
</td>
<td>false</td>
</tr>

</table>
@exception RTi.TS.TSException Throws if there is a problem formatting the output.
*/
public List<String> formatOutput( PropList proplist )
throws TSException
{	String message = "", routine = "MinuteTS.formatOutput", year_column = "";
	List<String> strings = new ArrayList<String>();
	PropList props = null;
	String data_format = "%9.1f", prop_value = null;

	// Only know how to do this for 24-hour time series (in the future may
	// automatically convert to correct interval)...

	if ( _data_interval_mult > 60 ) {
		message = "Can only do summary for <= 60 minute time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// If the property list is null, allocate one here so we don't have to
	// constantly check for null...

	if ( proplist == null ) {
		// Create a PropList so we don't have to check for nulls all the time.
		props = new PropList ( "formatOutput" );
	}
	else {
		props = proplist;
	}

	// Get the important formatting information from the property list...

	// Determine the units to output.  For now use what is in the time series...

	String req_units = _data_units;

	// Need to check the data type to determine if it is an average
	// or a total.  For now, make some guesses based on the units...

	if ( req_units.equalsIgnoreCase("AF") ||
		req_units.equalsIgnoreCase("ACFT") ||
		req_units.equalsIgnoreCase("FT") ||
		req_units.equalsIgnoreCase("FEET") ||
		req_units.equalsIgnoreCase("FOOT") ||
		req_units.equalsIgnoreCase("IN") ||
		req_units.equalsIgnoreCase("INCH") ) {
		// Assume totals...
		year_column = "Total";
	}
	else {
	    // Assume averages...
		year_column = "Average";
	}

	// Get the precision...

	prop_value = props.getValue ( "OutputPrecision" );
	if ( prop_value == null ) {
		// Older, being phased out...
		Message.printWarning ( 2, routine, "Need to switch Precision property to OutputPrecision" );
		prop_value = props.getValue ( "Precision" );
	}
	if ( prop_value == null ) {
		// Try to get units information for default...
		try {	DataUnits u = DataUnits.lookupUnits ( req_units );
			data_format = "%9." + u.getOutputPrecision() + "f";
			u = null;
		}
		catch ( Exception e ) {
			// Default...
			data_format = "%9.1f";
		}
	}
	else {
	    // Set to requested precision...
		data_format = "%9." + prop_value + "f";
	}

	// Determine whether water or calendar year...

	prop_value = props.getValue ( "CalendarType" );
	if ( prop_value == null ) {
		// Default to "CalendarYear"...
		prop_value = "" + YearType.CALENDAR;
	}
	YearType calendar = YearType.valueOfIgnoreCase(prop_value);

	// Determine the period to output.  For now always output the total...

	if ( (_date1 == null) || (_date2 == null) ) {
		message = "Null period dates for time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}
	DateTime start_date = new DateTime (_date1);
	prop_value = props.getValue ( "OutputStart" );
	if ( prop_value != null ) {
		try {
		    start_date = DateTime.parse ( prop_value );
			start_date.setPrecision ( DateTime.PRECISION_MINUTE );
		}
		catch ( Exception e ) {
			// Default to the time series...
			start_date = new DateTime ( _date1 );
		}
	}
	DateTime end_date = new DateTime (_date2);
	prop_value = props.getValue ( "OutputEnd" );
	if ( prop_value != null ) {
		try {
			end_date = DateTime.parse ( prop_value );
			end_date.setPrecision ( DateTime.PRECISION_MINUTE );
		}
		catch ( Exception e ) {
			// Default to the time series...
			end_date = new DateTime ( _date2 );
		}
	}

	// Now generate the output based on the format...

	prop_value = props.getValue ( "PrintHeader" );
	String print_header = null;
	if ( prop_value == null ) {
		// Default is true...
		print_header = "true";
	}
	else {
		print_header = prop_value;
	}
	prop_value = props.getValue ( "UseCommentsForHeader" );
	String use_comments_for_header = null;
	if ( prop_value == null ) {
		// Default is false...
		use_comments_for_header = "false";
	}
	else {
		use_comments_for_header = prop_value;
	}
	if ( print_header.equalsIgnoreCase("true") ) {
		if ( !use_comments_for_header.equalsIgnoreCase("true") ||
			(_comments.size() == 0) ){
			// Format the header from data (not comments)...
			strings.add ( "" );
			List<String> strings2 = formatHeader();
			StringUtil.addListToStringList ( strings, strings2 );
		}
	}
		
	// Add comments if available...

	prop_value = props.getValue ( "PrintComments" );
	String print_comments = null;
	if ( prop_value == null ) {
		// Default is true...
		print_comments = "true";
	}
	else {
		print_comments = prop_value;
	}
	if ( print_comments.equalsIgnoreCase("true") ||
		use_comments_for_header.equalsIgnoreCase("true")){
		strings.add ( "" );
		if ( _comments != null ) {
			int ncomments = _comments.size();
			if ( !use_comments_for_header.equalsIgnoreCase("true")){
				strings.add ( "Comments:" );
			}
			if ( ncomments > 0 ) {
				for ( int i = 0; i < ncomments; i++ ) {
					strings.add((String)_comments.get(i));
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
	
	// Print the genesis information...

	prop_value = props.getValue ( "PrintGenesis" );
	String print_genesis = null;
	if ( prop_value == null ) {
		// Default is true...
		print_genesis = "true";
	}
	else {
		print_genesis = prop_value;
	}
	if ( (_genesis != null) && print_genesis.equalsIgnoreCase("true") ) {
		int size = _genesis.size();
		if ( size > 0 ) {
			strings.add ( "" );
			strings.add ( "Time series creation history:" );
			strings = StringUtil.addListToStringList( strings, _genesis );
		}
	}
	// Currently no difference in how the output is formatted but in the
	// future might do it differently for different intervals...
	formatOutputNMinute ( strings, props, calendar, data_format, start_date, end_date, req_units, year_column );

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
throws TSException
{	List<String> formatted_output = null;
	String routine = "MinuteTS.formatOutput";
	int	dl = 20;
	String message;

	if ( fp == null) {
		message = "Null PrintWriter for output";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// First get the formatted output...

	try {
		formatted_output = formatOutput ( props );
		if ( formatted_output != null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Formatted output is " + formatted_output.size() + " lines" );
			}
	
			// Now write each string to the writer...

			String newline = System.getProperty ( "line.separator");
			int size = formatted_output.size();
			for ( int i = 0; i < size; i++ ) {
				fp.print ( formatted_output.get(i) + newline );
			}
			newline = null;
		}
	}
	catch ( TSException e ) {
		// Rethrow...
		throw e;
	}

	// Also return the list...

	return formatted_output;
}

/**
Format the time series for output.
@return List of strings that are written to the file.
@param fname Name of output.
@param props Property list containing output modifiers.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( String fname, PropList props )
throws TSException
{	String message = null;
	List<String> formatted_output = null;
	PrintWriter	stream = null;
	String full_fname = IOUtil.getPathUsingWorkingDir(fname);

	// First open the output file...

	try {
		stream = new PrintWriter ( new FileWriter(full_fname) );
	}
	catch ( Exception e ) {
		message = "Unable to open file \"" + full_fname + "\"";
		throw new TSException ( message );
	}

	try {
		formatted_output = formatOutput ( stream, props );
	}
	catch ( TSException e ) {
		// Rethrow...
		throw e;
	}
	finally {
		if ( stream != null ) {
			stream.close();
		}
	}

	// Also return the list (consistent with C++ single return type).

	return formatted_output;
}

/**
Format the body of the report for an N-minute data, assuming <= 60 minutes.
The output is a simple format with YYYY-MM-DD HH on the left and then the
minutes filling out the row.  If 1 minute data, two rows of values are used
(might need to change later but 1 minute is unlikely).  Otherwise,
a single row is used.  The right column is the total for the hour.  The data
values are always a maximum of 9 characters.
@param strings Vector of strings to be used as output.
@param props Properties to control output.
@param calendar Calendar to use for output.
@param data_format Format for data values (C printf style).
@param start_date Start date for output.
@param end_date End date for output.
@param req_units Requested units for output.
@param total_column indicates whether total column is total or average.
*/
private void formatOutputNMinute ( List<String> strings, PropList props,
					YearType calendar, String data_format,
					DateTime start_date, DateTime end_date,
					String req_units, String total_column )
{	StringBuffer b = new StringBuffer();
	// Loop through the data starting at the appropriate first hour for the period...
	DateTime date = new DateTime ( start_date );
	date.setMinute ( 0 );	// Always want full hours
	DateTime end = new DateTime ( end_date );
	end.setMinute ( 60 - _data_interval_mult );
	int col = 0; // col=0 is date, col=1 is first data column
	int row = 1; // Row within a day
	double total = 0.0, value = 0.0;
	int count = 0;
	boolean do_total = false;
	if ( total_column.equalsIgnoreCase("Total") ) {
		do_total = true;
	}
	boolean first_header = true;
	int numcol = 60/_data_interval_mult;
	if ( _data_interval_mult == 1 ) {
		numcol = 30;
	}
	for ( ; date.lessThanOrEqualTo(end); date.addInterval(_data_interval_base,_data_interval_mult) ) {
		// Print a header if the first time or the day is 1...
		if ( first_header || ((date.getHour() == 0) && (date.getMinute() == 0)) ) {
			first_header = false;
			strings.add ( "" );
			b.setLength(0);
			b.append ( "  Date/Hour  " );
			for ( int i = 0; i < numcol; i++ ) {
				if ( _data_interval_mult == 1 ) {
					b.append ( "   "+ StringUtil.formatString(i,"%2d") + "/"+
					StringUtil.formatString((i+12),"%2d") + "  ");
				}
				else {
					b.append ( "    " +
					StringUtil.formatString(i*_data_interval_mult,"%2d") + "    ");
				}
			}
			b.append ( " " + total_column );
			strings.add ( b.toString() );
			// Now add the underlines for the headings...
			b.setLength(0);
			b.append ( "-------------" );
			for ( int i = 0; i < (numcol + 1); i++ ) {
				b.append ( " ---------" );
			}
			strings.add ( b.toString() );
		}
		// Now do the data...
		if ( col == 0 ) {
			b.setLength(0);
			if ( row == 1 ) {
				// Add the date at the start of the line...
				b.append ( date.toString(DateTime.FORMAT_YYYY_MM_DD_HH));
			}
			else {
				b.append ( "          " );
			}
			++col;
		}
		// Add a data value...
		value = getDataValue ( date );
		if ( isDataMissing(value) ) {
			b.append ( "          " );
		}
		else {
			b.append(" " + StringUtil.formatString(value,
			data_format) );
			total += value;
			++count;
		}
		++col;
		// Now check for the end of the line.  The last data value for
		// the day will be in numcol (e.g. 30 for 1minute).  The above
		// will then have incremented the value to 31.
		if ( col > numcol ) {
			if ( (_data_interval_mult == 1) && (row == 1) ) {
				// Need to start a new row...
				row = 2;
				strings.add ( b.toString() );
			}
			else {
				// Need to output the row and the total...
				if ( do_total || (count == 0) ) {
					b.append(" " + StringUtil.formatString(total,data_format) );
				}
				else {
					b.append(" " + StringUtil.formatString(total/count, data_format) );
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
Return a data point for the date.
@param date date/time to get data.
@param tsdata if null, a new instance of TSData will be returned.  If non-null, the provided
instance will be used (this is often desirable during iteration to decrease memory use and
increase performance).
@return a TSData for the specified date/time.
*/
public TSData getDataPoint ( DateTime date, TSData tsdata )
{	
    if ( tsdata == null ) {
		// Allocate it...
		tsdata = new TSData();
	}
	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, "MinuteTS.getDataValue",
			date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		tsdata.setValues ( date, _missing, _data_units, "", 0 );
		return tsdata;
	}
	// This computes the _month_pos, _day_pos and _interval_pos...
	getDataPosition ( date );
	if ( _has_data_flags ) {
	    if ( _internDataFlagStrings ) {
	        tsdata.setValues ( date, _data[_month_pos][_day_pos][_interval_pos],
				 _data_units, _dataFlags[_month_pos][_day_pos][_interval_pos].intern(), 0 );
	    }
	    else {
            tsdata.setValues ( date, _data[_month_pos][_day_pos][_interval_pos],
                 _data_units, _dataFlags[_month_pos][_day_pos][_interval_pos], 0 );
	    }
	}
	else {
		tsdata.setValues ( date, _data[_month_pos][_day_pos][_interval_pos], _data_units, "", 0 );
	}
	return tsdata;
}

/**
Compute the data position and set in class data.  This method is used primarily by get/set routines.
Minute data are stored as: [absolute month][days in month][interval in day].
The position array is re-used and values should be copied if there is a need to use between calls.
@param date Date of interest.
*/
private int [] getDataPosition ( DateTime date )
{	// Do not define routine here to improve performance.
	String tz, tz1;

	// This is where we would calculate the shift between the requested
	// time zone and the time zone that we have stored.  For now, just check
	// the time zones against each other and print a warning if not
	// compatible.  When there is time, calculate the shift.

	tz = date.getTimeZoneAbbreviation();
	tz1 = _date1.getTimeZoneAbbreviation();
	if ( (tz.length() == 0) || tz.equals(tz1) ) {
		// We are OK doing a straight retrieval
		//tzshift = 0;
	}
	else {
		if ( Message.isDebugOn ) {
			Message.printWarning ( 10, "MinuteTS.getDataPosition",
			"Do not know how to shift time zones yet (\"" + tz1 + "\" to \"" + tz + "\"" );
			//tzshift = 0;
		}
	}

	// Calculate the row position of the data...

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "MinuteTS.getDataPosition",
		"Using " + date + "(" + date.getAbsoluteMonth() + ") and start date: " + 
		_date1 + "(" + _date1.getAbsoluteMonth() + ") for row-col calculation." );
	}

	_month_pos = date.getAbsoluteMonth() - _date1.getAbsoluteMonth();

    // Calculate the day position of the data...

	_day_pos = date.getDay() - 1;

	// Calculate the interval position of the data.  We know that minute
	// data are stored in a 3D array with the last dimension being the
	// minute data by interval.  Note that the recording at 00:00 of the
	// current day is the first reading of the day!

	_interval_pos = (date.getHour()*60 + date.getMinute())/_data_interval_mult;

	if ( Message.isDebugOn ) {
		Message.printDebug ( 50, "MinuteTS.getDataPosition", "Month=[" + _month_pos +
		"] Day=[" + _day_pos +"] interval=[" + _interval_pos +"]" );
	}

	_pos[0] = _month_pos;
	_pos[1] = _day_pos;
	_pos[2] = _interval_pos;
	return _pos;
}

/**
Return the data value for a date.
Minute data are stored in a three-dimensional array:
[absolute month][days in month][interval].
@return The data value corresponding to the date.
@param date Date of interest.
*/
public double getDataValue( DateTime date )
{	// Do not define routine here to increase performance.

	if ( (date == null) || (_data == null) ) {
		return _missing;
	}

	// Check the date coming in...

	if(	(date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to increase performance...
			Message.printWarning( 3, "MinuteTS.getDataValue",
			date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		return _missing;
	}

	// Set the data position in the class data.  There should be no problem
	// since we already checked the dates above...

	getDataPosition(date);

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "MinuteTS.getDataValue",
		_data[_month_pos][_day_pos][_interval_pos] + " for " + date +
		" from _data[" + _month_pos + "][" + _day_pos + "][" + _interval_pos + "]" );
	}

	return _data[_month_pos][_day_pos][_interval_pos];
}

/**
Returns the data in the specified DataFlavor, or null if no matching flavor
exists.  From the Transferable interface.  Supported data flavors are:<br>
<ul>
<li>MinuteTS - MinuteTS.class / RTi.TS.MinuteTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(minuteTSFlavor)) {
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

/**
Returns the flavors in which data can be transferred.  From the Transferable interface.  
The order of the dataflavors that are returned are:<br>
<ul>
<li>MinuteTS - MinuteTS.class / RTi.TS.MinuteTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[3];
	flavors[0] = minuteTSFlavor;
	flavors[1] = TS.tsFlavor;
	flavors[2] = TSIdent.tsIdentFlavor;
	return flavors;
}

/**
Indicate whether the time series has data, determined by checking to see whether
the data space has been allocated.  This method can be called after a time
series has been read - even if no data are available, the header information
may be complete.  The alternative of returning a null time series from a read
method if no data are available results in the header information being
unavailable.  Instead, return a TS with only the header information and call
hasData() to check to see if the data space has been assigned.
@return true if data are available (the data space has been allocated).
Note that true will be returned even if all the data values are set to the missing data value.
*/
public boolean hasData ()
{	if ( _data != null ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Initialize the data.
*/
private void init()
{	_data = null;
	_data_interval_base = TimeInterval.MINUTE;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.MINUTE;
	_data_interval_mult_original = 1;
	_pos = new int[3];
	_pos[0] = 0;
	_pos[1] = 0;
	_pos[2] = 0;
	_month_pos = 0;
	_day_pos = 0;
	_interval_pos = 0;
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.  Supported data flavors are:<br>
<ul>
<li>MinuteTS - MinuteTS.class / RTi.TS.MinuteTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(minuteTSFlavor)) {
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
Refresh the derived data (e.g., data limits) if the time series has changed.
This is generally only called by methods within the package.
*/
public void refresh ()
{	TSLimits limits = null;

	// If the data is not dirty, then we do not have to refresh the other information...

	if ( !_dirty ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "MinuteTS.refresh", "Time series is not dirty.  Not recomputing limits" );
		}
		return;
	}

	// Else we need to refresh...

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "MinuteTS.refresh", "Time Series is dirty. Recomputing limits" );
	}

	limits = TSUtil.getDataLimits ( this, _date1, _date2, false );
	if ( limits.areLimitsFound() ) {
		// Now reset the limits for the time series...
		setDataLimits ( limits );
	}

	_dirty = false;
	limits = null;
}

/**
Set the data value at a date.
@param date Date of interest.
@param value Data value corresponding to date.
*/
public void setDataValue( DateTime date, double value )
{	// Do not define routine here to increase performance.

	if ( date == null ) {
		return;
	}

	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			// Wrap in debug to perform better...
			Message.printWarning( 10, "MinuteTS.setDataValue",
			"Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return;
	}

	// Get the data position...

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "MinuteTS.setDataValue",
		"Setting " + value + " for " + date + " at [" + _month_pos + "][" + _day_pos + "][" + _interval_pos + "]" );
	}

	// Set the dirty flag so that we know to recompute the limits if desired...

	_dirty = true;
	_data[_month_pos][_day_pos][_interval_pos] = value;
}

/**
Set the data value and associated information for the date.
@param date Date of interest.
@param value Data value corresponding to date.
@param dataFlag data_flag Data flag for value.
@param duration Duration for value (ignored - assumed to be 1-day or
instantaneous depending on data type).
*/
public void setDataValue ( DateTime date, double value, String dataFlag, int duration )
{	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 10, "MinuteTS.setDataValue",
			"Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return;
	}

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "MinuteTS.setDataValue",
		"Setting " + value + " for " + date + " at [" + _month_pos + "][" + _day_pos + "][" + _interval_pos + "]" );
	}

	// Set the dirty flag so that we know to recompute the limits if desired...

	_dirty = true;

	_data[_month_pos][_day_pos][_interval_pos] = value;
    if ( (dataFlag != null) && (dataFlag.length() > 0) ) {
        if ( !_has_data_flags ) {
            // Trying to set a data flag but space has not been allocated, so allocate the flag space
            try {
                allocateDataFlagSpace(null, false );
            }
            catch ( Exception e ) {
                // Generally should not happen - log as debug because could generate a lot of warnings
                if ( Message.isDebugOn ) {
                    Message.printDebug(30, "MinuteTS.setDataValue", "Error allocating data flag space (" + e +
                        ") - will not use flags." );
                }
                // Make sure to turn flags off
                _has_data_flags = false;
            }
        }
    }
	if ( _has_data_flags && (dataFlag != null) ) {
		if ( _internDataFlagStrings ) {
			_dataFlags[_month_pos][_day_pos][_interval_pos] = dataFlag.intern();
		}
		else {
		    _dataFlags[_month_pos][_day_pos][_interval_pos] = dataFlag; 
		}
	}
}

}