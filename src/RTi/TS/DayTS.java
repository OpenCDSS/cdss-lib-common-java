// DayTS - base class from which all daily time series are derived

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
// DayTS - base class from which all daily time series are derived
// ----------------------------------------------------------------------------
// Notes:	(1)	This base class is provided so that specific daily
//			time series can derive from this class.
//		(2)	Data for this time series interval is stored as follows:
//
//			day within month  ->
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
// History:
//
// 09 Apr 1998	Steven A. Malers, RTi	Copy the HourTS code and modify as
//					necessary.  Start to use this instead
//					of a 24-hour time series to make
//					storage more straightforward.
// 22 Aug 1998	SAM, RTi		In-line up getDataPosition in this
//					class to increase performance. Change
//					so that getDataPosition returns an
//					array.
// 09 Jan 1999	SAM, RTi		Add more exception handling due to
//					changes in other classes.
// 05 Apr 1999	CEN, RTi		Optimize by adding _row, _column, 
//					similar to HourTS.
// 21 Apr 1999	SAM, RTi		Add precision lookup for formatOutput.
//					Add genesis to output.
// 09 Aug 1999	SAM, RTi		Add changePeriodOfRecord to support
//					regression.
// 21 Feb 2001	SAM, RTi		Add clone().  Add copy constructor.
//					Remove printSample(), read and write
//					methods.
// 04 May 2001	SAM, RTi		Add OutputPrecision property, which is
//					more consistent with TS notation.
// 29 Aug 2001	SAM, RTi		Fix clone() to work correctly.  Remove
//					old C-style documentation.  Change _pos
//					from static - trying to minimize the
//					amount of static data that is used.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
//					Remove constructor that takes a file
//					name.  Change some methods to have void
//					return type to agree with base class.
// 2002-01-31	SAM, RTi		Add support for data flags.  Change so
//					getDataPoint() returns a reference to an
//					internal object that is reused.
// 2002-05-24	SAM, RTi		Add total period statistics for each
//					month.
// 2002-09-05	SAM, RTi		Remove hasDataFlags().  Let the base TS
//					class method suffice.  Change so that
//					hasDataFlags() does not allocate the
//					data flags memory but instead do it in
//					allocateDataSpace().
// 2003-01-08	SAM, RTi		Add hasData().
// 2003-05-02	SAM, RTi		Fix bug in getDataPoint() - was not
//					recalculationg the row/column position
//					for the data flag.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
//					* Change TS.INTERVAL* to TimeInterval.
// 2003-10-21	SAM, RTi		Overload allocateDataSpace(), similar
//					to MonthTS to take an initial value.
// 2003-12-09	SAM, RTi		* Handle data flags in clone().
// 2004-01-26	SAM, RTi		* Add OutputStart and OutputEnd
//					  properties to formatOutput().
//					* In formatOutput(), convert the file
//					  name to a full path.
// 2004-03-04	J. Thomas Sapienza, RTi	* Class now implements Serializable.
//					* Class now implements Transferable.
//					* Class supports being dragged or 
//					  copied to clipboard.
// 2005-06-02	SAM, RTi		* Add allocateDataFlagSpace(), similar
//					  to MonthTS.
//					* Remove warning about reallocating data
//					  space.
// 2005-12-07	JTS, RTi		Added copy constructor to create a DayTS
//					from an HourTS.
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
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;
import RTi.Util.Time.YearType;

/**
The DayTS class is the base class for daily time series.  The class can be
extended for variations on daily data.  Override the allocateDataSpace() and set/get methods to do so.
*/
@SuppressWarnings("serial")
public class DayTS extends TS implements Cloneable, Serializable, Transferable {

// Data members...

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor dayTSFlavor = new DataFlavor(RTi.TS.DayTS.class, "RTi.TS.DayTS");

protected double[][] _data; // This is the data space for daily time series.
protected String [][] _dataFlags; // Data flags for each daily value, with dimensions [month][day_in_month]
private int [] _pos = null; // Used to optimize performance when getting data.
protected int _row, // Row position in data.
			_column; // Column position in data.

/**
Constructor.
*/
public DayTS ()
{	super ();
	init();
}

/**
Copy constructor.  Everything is copied by calling copyHeader() and then copying the data values.
*/
public DayTS ( DayTS ts )
{	if ( ts == null ) {
		return;
	}
	copyHeader ( ts );
	allocateDataSpace();
	DateTime date2 = new DateTime ( _date2 );
	DateTime date = new DateTime ( _date1 );
	for ( ;	date.lessThanOrEqualTo(date2); date.addInterval(_data_interval_base,_data_interval_mult) ) {
		setDataValue ( date, ts.getDataValue(date) );
	}
}

/**
Copy constructor that can convert an HourTS to a DayTS.  Everything is copied 
by calling copyHeader() and then copying the data values.<p>
<b>Note:</b> Currently assumes that the interval is 24Hour, and offers no 
support for other HourTS intervals.
*/
public DayTS ( HourTS ts )
{	if ( ts == null ) {
		return;
	}
	copyHeader ( ts );

	_data_interval_base = TimeInterval.DAY;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.HOUR;
	_data_interval_mult_original = 24;

	_pos = new int[2];
	_pos[0] = 0;
	_pos[1] = 0;

	allocateDataSpace();
	DateTime date2 = new DateTime ( _date2 );
	DateTime date = new DateTime ( _date1 );
	for ( ;	date.lessThanOrEqualTo(date2); date.addInterval(_data_interval_base,_data_interval_mult) ) {
		setDataValue ( date, ts.getDataValue(date) );
	}

	TSIdent tsid = getIdentifier();
	tsid.setInterval(TimeInterval.DAY, 1);
	try {
		setIdentifier(tsid);
	}
	catch (Exception e) {
		// this ought never to happen
		Message.printWarning(2, "DayTS.constructor", "Error setting Time Series Idnetifier: " + tsid);
		Message.printWarning(3, "DayTS.constructor", e);
	}
}

/**
Allocate the data flag space for the time series.  This requires that the data
interval base and multiplier are set correctly and that _date1 and _date2 have
been set.  The allocateDataSpace() method will allocate the data flags if
appropriate.  Use this method when the data flags need to be allocated after the initial allocation.
@param initialValue Initial value (null is allowed and will result in the flags being initialized to spaces).
@param retainPreviousValues If true, the array size will be increased if necessary, but
previous data values will be retained.  If false, the array will be reallocated and initialized to spaces.
@exception Exception if there is an error allocating the memory.
*/
public void allocateDataFlagSpace (	String initialValue, boolean retainPreviousValues )
throws Exception
{	String	routine="DayTS.allocateDataFlagSpace", message;
	int	i;

	if ( (_date1 == null) || (_date2 == null) ) {
		message ="Dates have not been set.  Cannot allocate data space";
		Message.printWarning ( 2, routine, message );
		throw new Exception ( message );
	}
	if ( _data_interval_mult != 1 ) {
		// Do not know how to handle N-day interval...
		message = "Only know how to handle 1-day data, not " + _data_interval_mult + "-day";
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

	String [][] dataFlagsPrev = null;
	if ( _has_data_flags && retainPreviousValues ) {
		// Save the reference to the old flags array...
		dataFlagsPrev = _dataFlags;
	}
	else {
	    // Turn on the flags...
		_has_data_flags = true;
	}
	// Top-level allocation...
	_dataFlags = new String[nmonths][];

	// Set the counter date to match the starting month.  This date is used
	// to determine the number of days in each month.

	DateTime date = new DateTime(DateTime.DATE_FAST);
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	// Allocate memory...

	int iday = 0;
	int nvals = 0;
	int ndays_in_month = 0;
	boolean internDataFlagStrings = getInternDataFlagStrings();
	for ( i = 0; i < nmonths; i++, date.addMonth(1) ) {
		// Easy to handle 1-day data, otherwise an exception was thrown above...
		ndays_in_month = TimeUtil.numDaysInMonth ( date );
		// Easy to handle 1-day data, otherwise an exception was thrown above.
		// Here would change the number of values if N-day was supported.
		nvals = ndays_in_month;

		_dataFlags[i] = new String[nvals];

		// Now fill with the initial data value...

		for ( iday = 0; iday < nvals; iday++ ) {
		    if ( internDataFlagStrings ) {
		        _dataFlags[i][iday] = initialValue.intern();
		    }
		    else {
		        _dataFlags[i][iday] = initialValue;
		    }
			if(retainPreviousValues && (dataFlagsPrev != null)){
				// Copy over the old values (typically shorter character arrays)...
			    if ( internDataFlagStrings ) {
			        _dataFlags[i][iday] = dataFlagsPrev[i][iday].intern();
			    }
			    else {
			        _dataFlags[i][iday] = dataFlagsPrev[i][iday];
			    }
			}
		}
	}
}

/**
Allocate the data space for the time series.  The start and end dates and the
data interval multiplier must have been set.  Initialize the space with the missing data value.
*/
public int allocateDataSpace()
{	return allocateDataSpace ( _missing );
}

/**
Allocate the data space.  The start and end dates and the interval multiplier should have been set.
@param value The value to initialize the time series.
@return 0 if successful, 1 if failure.
*/
public int allocateDataSpace ( double value )
{	String	routine = "DayTS.allocateDataSpace";
	int	ndays_in_month, nmonths=0, nvals;

	if ( (_date1 == null) || (_date2 == null) ) {
		Message.printWarning ( 2, routine, "No dates set for memory allocation" );
		return 1;
	}
	if ( _data_interval_mult != 1 ) {
		// Do not know how to handle N-day interval...
		String message = "Only know how to handle 1-day data, not " + _data_interval_mult + "Day";
		Message.printWarning ( 3, routine, message );
		return 1;
	}
	nmonths = _date2.getAbsoluteMonth() - _date1.getAbsoluteMonth() + 1;

	if( nmonths == 0 ){
		Message.printWarning( 2, routine, "TS has 0 months POR, maybe dates haven't been set yet" );
		return 1;
	}

	_data = new double[nmonths][];
	if ( _has_data_flags ) {
		_dataFlags = new String[nmonths][];
	}

	// May need to catch an exception here in case we run out of memory.

	// Set the counter date to match the starting month.  This date is used
	// to determine the number of days in each month.

	DateTime date = new DateTime ( DateTime.DATE_FAST );
	date.setMonth( _date1.getMonth() );
	date.setYear( _date1.getYear() );

	int iday = 0;
	for ( int imon = 0; imon < nmonths; imon++, date.addMonth(1) ) {
		ndays_in_month = TimeUtil.numDaysInMonth ( date );
		// Handle 1-day data, otherwise an exception was thrown above.
		// Here would change the number of values if N-day was supported.
		nvals = ndays_in_month;
		_data[imon] = new double[nvals];
		if ( _has_data_flags ) {
			_dataFlags[imon] = new String[nvals];
		}

		// Now fill with the missing data value for each day in the month...

		for ( iday = 0; iday < nvals; iday++ ) {
			_data[imon][iday] = value;
			if ( _has_data_flags ) {
				_dataFlags[imon][iday] = "";
			}
		}
	}

	int nactual = calculateDataSize ( _date1, _date2, _data_interval_mult );
	setDataSize ( nactual );

	if ( Message.isDebugOn ) {
		Message.printDebug( 10, routine, "Allocated " + nmonths + " months of memory for daily data from "
		+ _date1 + " to " + _date2 );
	}

	date = null;
	routine = null;
	return 0;
}

/**
Determine the number of points between two dates for the given interval multiplier.
@return The number of data points for a day time series
given the data interval multiplier for the specified period.
@param start_date The first date of the period.
@param end_date The last date of the period.
@param interval_mult The time series data interval multiplier.
*/
public static int calculateDataSize ( DateTime start_date, DateTime end_date, int interval_mult )
{	String routine = "DayTS.calculateDataSize";
	int datasize = 0;

	if ( start_date == null ) {
		Message.printWarning ( 2, routine, "Start date is null" );
		return 0;
	}
	if ( end_date == null ) {
		Message.printWarning ( 2, routine, "End date is null" );
		return 0;
	}
	if ( interval_mult > 1 ) {
		Message.printWarning ( 1, routine, "Greater than 1-day TS not supported" );
		return 0;
	}
	// First set to the number of data in the months...
	datasize = TimeUtil.numDaysInMonths (
		start_date.getMonth(), start_date.getYear(), end_date.getMonth(), end_date.getYear() );
	// Now subtract off the data at the ends that are missing...
	// Start by subtracting the full day at the beginning of the month that is not included...
	datasize -= (start_date.getDay() - 1);
	// Now subtract off the data at the end...
	// Start by subtracting the full days off the end of the month...
	int ndays_in_month = TimeUtil.numDaysInMonth ( end_date.getMonth(), end_date.getYear() );
	datasize -= (ndays_in_month - end_date.getDay());
	routine = null;
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
{	String routine="DayTS.changePeriodOfRecord";
	String message;

	// To transfer, we need to allocate a new data space.  In any case, need to get the dates established...
	if ( (date1 == null) && (date2 == null) ) {
		// No dates.  Cannot change.
		message = "\"" + _id + "\": period dates are null.  Cannot change the period.";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

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
	// temporary DayTS with the old dates and save a reference to the old data...

	double [][] data_save = _data;
	String [][] dataFlagsSave = _dataFlags;
	DayTS temp_ts = new DayTS ();
	temp_ts.setDataInterval ( TimeInterval.DAY, _data_interval_mult );
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

	int column, row, temp_ts_am1 = temp_ts.getDate1().getAbsoluteMonth();
	boolean internDataFlagStrings = getInternDataFlagStrings();
	for ( DateTime date = new DateTime (transfer_date1,DateTime.DATE_FAST);
		date.lessThanOrEqualTo (transfer_date2);
		date.addInterval( _data_interval_base, _data_interval_mult ) ) {
		// Get the data position for the old data...
		row = date.getAbsoluteMonth() - temp_ts_am1;
       	column = date.getDay() - 1;	// Zero offset!
		// Also transfer the data flag...
		if ( _has_data_flags ) {
			// Transfer the value and flag...
		    if ( internDataFlagStrings ) {
		        setDataValue ( date, data_save[row][column], dataFlagsSave[row][column].intern(), 1 );
		    }
		    else {
		        setDataValue ( date, data_save[row][column], dataFlagsSave[row][column], 1 );
		    }
		}
		else {
		    // Transfer the value...
			setDataValue ( date, data_save[row][column] );
		}
	}

	// Add to the genesis...

	addToGenesis ( "Changed period from: " + temp_ts.getDate1() + " - " +
		temp_ts.getDate2() + " to " + new_date1 + " - " + new_date2 );
	routine = null;
	message = null;
	new_date1 = null;
	new_date2 = null;
	temp_ts = null;
	transfer_date1 = null;
	transfer_date2 = null;
	data_save = null;
}

/**
Clone the object.  Similar to a copy constructor but can be used polymorphically.
*/
public Object clone ()
{	DayTS ts = (DayTS)super.clone();
	// Does not seem to work...
	//ts._data = (double[][])_data.clone();
	//ts._pos = (int[])_pos.clone();
    if ( _data == null ) {
        ts._data = null;
    }
    else {
       ts._data = new double[_data.length][];
	   for ( int imon = 0; imon < _data.length; imon++ ) {
	       ts._data[imon] = new double[_data[imon].length];
		   System.arraycopy ( _data[imon], 0, ts._data[imon], 0,_data[imon].length);
	   }
    }
	int iday = 0;
	if ( _has_data_flags ) {
	    if ( _dataFlags == null ) {
	        ts._dataFlags = null;
	    }
	    else {
    		// Allocate months...
    		ts._dataFlags = new String[_dataFlags.length][];
    		for ( int imon = 0; imon < _dataFlags.length; imon++ ) {
    			// Allocate days in month...
    			ts._dataFlags[imon] = new String[_dataFlags[imon].length];
    			for ( iday = 0; iday < _dataFlags[imon].length; iday++ ) {
    			    ts._dataFlags[imon][iday] = _dataFlags[imon][iday];
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
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_data = null;
	_dataFlags = null;
	_pos = null;
	super.finalize();
}

/**
Format the time series for output.  This is not meant to be used for time
series conversion but to produce a general summary of the output.  At this time,
daily time series are always output in a matrix summary format.
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
<td><b>Delimiter</b></td>
<td>The delimiter to use for spreadsheet output.  Can be a single or multiple characters.
</td>
<td>|</td>
</tr>

<tr>
<td><b>Format</b></td>
<td>The overall format of the output.  Can be either "Summary" for a matrix
similar to the USGS flow data reports or "Spreadsheet" for a delimited
file suitable for a spreadsheet (see the "Delimiter" property).  At this time,
the determination of whether total or average annual values are shown is made
based on units.  AF, ACFT, IN, INCH, FEET, and FOOT are treated as totals and
all others as averages.  A more rigorous handling is being implemented to
use the units dimension to determine totals, etc.</td>
<td>Summary</td>
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
Time Series Identifier  = 07126500.CRDSS_USGS.streamflow.DAY
Description             = PURGATOIRE RIVER AT NINEMILE DAM, NR HIGBEE, CO.
Data source             = CRDSS_USGS
Data type               = streamflow
Data interval           = DAY
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
<td>Print the time series genesis information after the header in a format as follows:
<p>
<pre>
Time series genesis (creation history):
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
{	String message = "", routine = "DayTS.formatOutput()";	
	int column, dl = 20, row;
	List<String> strings = new ArrayList<String>();
	PropList props = null;
	String data_format = "%9.1f", format = "", prop_value = null;

	// Only know how to do this for 1-day time series (in the future may
	// automatically convert to correct interval)...

	if ( _data_interval_mult != 1 ) {
		message = "Can only do summary for 1-day time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// If the property list is null, allocate one here so we don't have to constantly check for null...

	if ( proplist == null ) {
		// Create a PropList so we don't have to check for nulls all the time.
		props = new PropList ( "formatOutput" );
	}
	else {
		props = proplist;
	}

	// Get the important formatting information from the property list...

	// Get the overall format...

	prop_value = props.getValue ( "Format" );
	if ( prop_value == null ) {
		// Default to "Summary"...
		format = "Summary";
	}
	else {
	    // Set to requested format...
		format = prop_value;
	}

	// Determine the units to output.  For now use what is in the time series...

	String req_units = _data_units;

	// Get the precision...

	prop_value = props.getValue ( "OutputPrecision" );
	if ( prop_value == null ) {
		// Being phased out...
		Message.printWarning ( 2, routine, "Need to switch Precision property to OutputPrecision" );
		prop_value = props.getValue ( "Precision" );
	}
	if ( prop_value == null ) {
		// Try to get units information for default...
		try {
		    DataUnits u = DataUnits.lookupUnits ( req_units );
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
			start_date.setPrecision ( DateTime.PRECISION_DAY );
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
			end_date.setPrecision ( DateTime.PRECISION_DAY );
		}
		catch ( Exception e ) {
			// Default to the time series...
			end_date = new DateTime ( _date2 );
		}
	}

	// Now generate the output based on the format...

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Creating output in format \"" + format + "\"" );
	}
	if ( format.equalsIgnoreCase("Spreadsheet") ) {
		// Spreadsheet
		prop_value = props.getValue ( "Delimiter" );
		if ( prop_value == null ) {
			// Default to "|"...
			format = "|";
		}
		else {
		    // Set to requested delimiter...
			format = prop_value;
		}
		Message.printWarning ( 3, routine, "Spreadsheet output format is not implemented" );
		return strings;
	}
	else if ( format.equalsIgnoreCase("Summary") ) {
		// The default output.  In this case, we produce a matrix like
		// the one produced by the legacy TSPrintSummaryMatrix.
		// However, since we now limit the formatting to only daily
		// time series and the data are already in a daily time step,
		// we do not have to change interval.

		// Print the header for the matrix...

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
			if ( !use_comments_for_header.equalsIgnoreCase("true")){
				// Format the header...
				strings.add ( "" );
				List<String> strings2 = formatHeader();
				StringUtil.addListToStringList ( strings, strings2 );
			}
		}
		print_header = null;
		
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
		if ( print_comments.equalsIgnoreCase("true") || use_comments_for_header.equalsIgnoreCase("true")){
			strings.add ( "" );
			if ( _comments != null ) {
				int ncomments = _comments.size();
				if ( !use_comments_for_header.equalsIgnoreCase("true")){
					strings.add ( "Comments:" );
				}
				if ( ncomments > 0 ) {
					for ( int i = 0; i < ncomments; i++ ) {
						strings.add( _comments.get(i));
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
		use_comments_for_header = null;
		print_comments = null;
	
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
		print_genesis = null;
	
		// Print the body of the summary...

		// Need to check the data type to determine if it is an average
		// or a total.  For now, make some guesses based on the units...

		strings.add ( "" );

		// Now transfer the daily data into a summary matrix, which looks like:
		//
		// Day Month....
		// 1
		// ...
		// 31
		// statistics
		//
		// Repeat for each year.
		
		// Adjust the start and end dates to be on full years for the calendar that is requested...

		int year_offset = 0;
		int month_to_start = 1;	// First month in year.
		int month_to_end = 12; // Last month in year.
		if ( calendar == YearType.CALENDAR ) {
			// Just need to output for the full year...
			start_date.setMonth ( 1 );
			start_date.setDay ( 1 );
			end_date.setMonth ( 12 );
			end_date.setDay ( 31 );
			month_to_start = 1;
			month_to_end = 12;
		}
		else if ( calendar == YearType.NOV_TO_OCT ) {
			// Need to adjust for the irrigation year to make sure that the first month is Nov and the last is Oct...
			if ( start_date.getMonth() < 11 ) {
				// Need to shift to include the previous irrigation year...
				start_date.addYear ( -1 );
			}
			// Always set the start month to Nov...
			start_date.setMonth ( 11 );
			start_date.setDay ( 1 );
			if ( end_date.getMonth() > 11 ) {
				// Need to include the next irrigation year...
				end_date.addYear ( 1 );
			}
			// Always set the end month to Oct...
			end_date.setMonth ( 10 );
			end_date.setDay ( 31 );
			// The year that is printed in the summary is actually later than the calendar for the Nov month...
			year_offset = 1;
			month_to_start = 11;
			month_to_end = 10;
		}
		else if ( calendar == YearType.WATER ) {
			// Need to adjust for the water year to make sure that
			// the first month is Oct and the last is Sep...
			if ( start_date.getMonth() < 10 ) {
				// Need to shift to include the previous water year...
				start_date.addYear ( -1 );
			}
			// Always set the start month to Oct...
			start_date.setMonth ( 10 );
			start_date.setDay ( 1 );
			if ( end_date.getMonth() > 9 ) {
				// Need to include the next water year...
				end_date.addYear ( 1 );
			}
			// Always set the end month to Sep...
			end_date.setMonth ( 9 );
			end_date.setDay ( 30 );
			// The year that is printed in the summary is actually
			// later than the calendar for the Oct month...
			year_offset = 1;
			month_to_start = 10;
			month_to_end = 9;
		}
		// Calculate the number of years...
		// up with the same month as the start month...
		int num_years = (end_date.getAbsoluteMonth() - start_date.getAbsoluteMonth() + 1)/12;
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Printing " + num_years + " years of summary for " +
			start_date.toString(DateTime.FORMAT_YYYY_MM) + " to " + end_date.toString(DateTime.FORMAT_YYYY_MM) );
		}
		// Reuse for each month that is printed.
		double data[][] = new double[31][12];

		// Now loop through the time series and transfer to the proper
		// location in the matrix.  Since days are vertical, we cannot
		// print any results until we have completed a month...
		double data_value;
		DateTime date = new DateTime(start_date,DateTime.DATE_FAST);
		StringBuffer buffer = null;
		// We have adjusted the dates above, so we always start in
		// column 0 (first day of first month in year)...
		column = 0;
		row = 0;
		double missing = getMissing();
		int ndays_in_month = 0;
		int day, month;
		for ( ; date.lessThanOrEqualTo(end_date); date.addInterval(_data_interval_base,_data_interval_mult) ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine, "Processing " + date.toString(DateTime.FORMAT_YYYY_MM_DD) +
				" row:" + row + " column:" + column );
			}
			// Figure out if this is a new year.  If so, we reset the headers, etc...
			day = date.getDay();
			month = date.getMonth();
			if ( day == 1 ) {
				if ( month == month_to_start ) {
					// Reset the data array...
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Resetting output array..." );
					}
					for ( int irow = 0; irow < 31; irow++ ){
						for ( int icolumn = 0; icolumn < 12; icolumn++){
							data[irow][icolumn] = missing;
						}
					}
				}
				ndays_in_month = TimeUtil.numDaysInMonth ( month, date.getYear() );
			}
			// Save the data value for later use in output and statistics.  Allow missing data values to be saved...
			data_value = getDataValue ( date );
			data[row][column] = data_value;
			// Check to see if at the end of the year.  If so, print out one year's values...
			if ((month == month_to_end) && (day == ndays_in_month)){
				// Print the header for the year...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine, "Printing output for summary year " + (date.getYear() + year_offset) );
				}
				strings.add ( "" );
				// "date" will be at the end of the year...
				if ( calendar == YearType.WATER ) {
					strings.add ( "                                                 Water Year " +
					date.getYear() + " (Oct " + (date.getYear() - 1) + " to Sep " + date.getYear() + ")" );
				}
				else if ( calendar == YearType.NOV_TO_OCT ) {
					strings.add ( "                                                 " + calendar + " " +
					date.getYear() + " (Nov " + (date.getYear() - 1) + " to Oct " + date.getYear() + ")" );
				}
				else {
				    strings.add ("                                                 Calendar Year " +
					date.getYear() );
				}
				strings.add ( "" );
				if ( calendar == YearType.WATER ) {
					// Water year...
					strings.add (
"Day     Oct       Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep       " );
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
				}
				else if ( calendar == YearType.NOV_TO_OCT ) {
					// Irrigation year...
					strings.add (
"Day     Nov       Dec       Jan       Feb       Mar       Apr       May       Jun       Jul       Aug        Sep      Oct       " );
					strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
				}
				else {
				    // Calendar year...
					strings.add (
"Day     Jan       Feb       Mar       Apr       May       Jun       Jul        Aug      Sep       Oct       Nov       Dec       " );
			strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
				}

				// Now print the summary for the year...
				int row_day, column_month;
				int irow, icolumn;
				int year, nvalid_days_in_month;
				for ( irow = 0; irow < 31; irow++ ) {
					row_day = irow + 1;
					for ( icolumn = 0; icolumn < 12; icolumn++ ) {
						column_month = month_to_start + icolumn;
						if ( icolumn == 0 ) {
							// Allocate a new buffer and print the day for all 12 months...
							buffer = new
							StringBuffer();
							buffer.append ( StringUtil.formatString(row_day, " %2d ") );
						}
						// Print the daily value...
						// Figure out if the day is valid for the month.  The
						// date is for the end of the year (last month) from the loop.
						year = date.getYear();
						if ( (calendar == YearType.WATER) && (column_month > 9) ) {
							--year;
						}
						else if ( (calendar == YearType.NOV_TO_OCT) && (column_month > 10) ) {
							--year;
						}
						nvalid_days_in_month = TimeUtil.numDaysInMonth (column_month, year );
						if ( row_day > nvalid_days_in_month ) {
							buffer.append ( "    ---   " );
						}
						else if ( isDataMissing( data[irow][icolumn])) {
							buffer.append ( "    NC    " );
						}
						else {
							buffer.append (StringUtil.formatString(data[irow][icolumn], " " + data_format) );
						}
						if ( icolumn == 11 ) {
							// Have processed the last month in the year print the row...
							strings.add(buffer.toString() );
						}
					}
				}
				strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
				// Now do the statistics.  Loop through each column...
				// First check to see if all stats should be printed (can be dangerous if we add new statistics)..
				prop_value = props.getValue ( "PrintAllStats" );
				String print_all_stats = null;
				if ( prop_value == null ) {
					// Default is false...
					print_all_stats = "false";
				}
				else {
					print_all_stats = prop_value;
				}
				// Now start on the minimum...
				prop_value = props.getValue ( "PrintMinStats" );
				String print_min = null;
				if ( prop_value == null ) {
					// Default is true...
					print_min = "true";
				}
				else {
					print_min = prop_value;
				}
				if ( print_min.equalsIgnoreCase("true") || print_all_stats.equalsIgnoreCase("true") ) {
					strings = StringUtil.addListToStringList (strings, formatOutputStats ( data, "Min ", data_format ) );
				}
				prop_value = props.getValue ( "PrintMaxStats" );
				String print_max = null;
				if ( prop_value == null ) {
					// Default is true...
					print_max = "true";
				}
				else {
					print_max = prop_value;
				}
				if ( print_max.equalsIgnoreCase("true") || print_all_stats.equalsIgnoreCase("true") ) {
					strings = StringUtil.addListToStringList (strings, formatOutputStats ( data, "Max ", data_format ) );
				}
				prop_value = props.getValue ( "PrintMeanStats");
				String print_mean = null;
				if ( prop_value == null ) {
					// Default is true...
					print_mean = "true";
				}
				else {
					print_mean = prop_value;
				}
				if ( print_mean.equalsIgnoreCase("true") || print_all_stats.equalsIgnoreCase("true") ) {
					strings = StringUtil.addListToStringList (strings, formatOutputStats ( data, "Mean", data_format ) );
				}
				column = -1; // Will be incremented in next step.
				print_all_stats = null;
				print_mean = null;
				print_min = null;
				print_max = null;
			}
			if ( day == ndays_in_month ) {
				// Reset to the next column...
				++column;
				row = 0;
			}
			else {
				++row;
			}
		}

		// Now do the notes...

		prop_value = props.getValue ( "PrintNotes" );
		String print_notes = null;
		if ( prop_value == null ) {
			// Default is true...
			print_notes = "true";
		}
		else {
		    print_notes = prop_value;
		}
		if ( print_notes.equalsIgnoreCase("true") ) {
			strings.add ( "" );
			strings.add ( "Notes:" );
			if ( calendar == YearType.WATER ) {
				strings.add ( "  Years shown are water years." );
				strings.add (
				"  A water year spans Oct of the previous calendar year to Sep of the current calendar year (all within the indicated water year)." );
			}
			else if ( calendar == YearType.NOV_TO_OCT ) {
				strings.add ( "  Years shown span Nov of the previous calendar year to Oct of the current calendar year." );
			}
			else {
				strings.add ( "  Years shown are calendar years." );
			}
			strings.add ( "  Annual values and statistics are computed only on non-missing data." );
			strings.add (
			    "  NC indicates that a value is not computed because of missing data or the data value itself is missing." );
		}
	}
	else {
	    message = "Unrecognized format: \"" + format + "\"";
		Message.printWarning ( 3, routine, message );
		throw new TSException ( message );
	}

	return strings;
}

/**
Format the time series for output.
@return list of strings that are written to the file.
@param fp PrintWriter to receive output.
@param props Properties to modify output.
@exception RTi.TS.TSException Throws if there is an error writing the output.
*/
public List<String> formatOutput ( PrintWriter fp, PropList props )
throws TSException
{	List<String> formatted_output = null;
	String routine = "DayTS.formatOutput(Writer,props)";
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

	// Also return the list (consistent with C++ single return type.

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
	//String routine = "DayTS.formatOutput";
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
		stream.close();
		stream = null;
	}
	catch ( TSException e ) {
		// Rethrow...
		throw e;
	}

	// Also return the list

	return formatted_output;
}

/**
Format the output statistics row given the data array.
@param data Data array to generate statistics from.
@param label Label for statistics row (e.g., "Mean").
@param data_format Format for individual floating point values.
*/
private List<String> formatOutputStats ( double[][] data, String label, String data_format )
{	List<String> strings = new ArrayList<String>();
	double stat = 0.0;
	StringBuffer buffer = null;
	double [] array = new double[31];
	int column, row;

	for ( column = 0; column < 12; column++ ) {
		if ( column == 0 ) {
			buffer = new StringBuffer();
			// Label needs to be 4 characters...
			buffer.append ( label );
		}
		// Extract the non-missing values...
		int num_not_missing = 0;
		for ( row = 0; row < 31; row++ ) {
			if ( !isDataMissing(data[row][column])){
				++num_not_missing;
			}
		}
		if ( num_not_missing > 0 ) {
			// Transfer to an array...
			array = new double[num_not_missing];
			num_not_missing = 0;
			for ( row = 0; row < 31; row++ ){
				if ( !isDataMissing(data[row][column]) ) {
					array[num_not_missing] = data[row][column];
					++num_not_missing;
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
			buffer.append (	StringUtil.formatString( stat, " " + data_format) );
		}
		else {
		    buffer.append ( "    NC    " );
		}
	}
	strings.add( buffer.toString() );
	strings.add (
"---- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- --------- ---------" );
	return strings;
}

/**
Return the data point corresponding to the date.
<pre>
             	Day data is stored in a two-dimensional array:
  		     |----------------> days
  		     |
  		    \|/
  		   month 

</pre>
@param date date/time to get data.
@param tsdata if null, a new instance of TSData will be returned.  If non-null, the provided
instance will be used (this is often desirable during iteration to decrease memory use and
increase performance).
@return a TSData for the specified date/time.
@see TSData
*/
public TSData getDataPoint ( DateTime date, TSData tsdata )
{	// Initialize data to most of what we need...
	if ( tsdata == null ) {
		// Allocate it...
		tsdata = new TSData();
	}
	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, "DayTS.getDataValue",
			date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		tsdata.setValues ( date, _missing, _data_units, "", 0 );
		return tsdata;
	}
	getDataPosition ( date );	// This computes _row and _column
	if ( _has_data_flags ) {
	    if ( _internDataFlagStrings ) {
	        tsdata.setValues ( date, getDataValue(date), _data_units, _dataFlags[_row][_column].intern(), 0 );
	    }
	    else {
	        tsdata.setValues ( date, getDataValue(date), _data_units, _dataFlags[_row][_column], 0 );
	    }
	}
	else {
        tsdata.setValues ( date, getDataValue(date), _data_units, "", 0 );
	}
	return tsdata;
}

/**
Return the position corresponding to the date.  The position array is volatile
and is reused for each call.  Copy the values to make persistent.
<pre>
             	Day data is stored in a two-dimensional array:
  		     |----------------> days
  		     |
  		    \|/
  		   month 

</pre>
@return The data position corresponding to the date.
@param date Date of interest.
*/
private int [] getDataPosition ( DateTime date )
{	// Do not define the routine or debug level here so we can optimize.

	// Note that unlike HourTS, do not need to check the time zone!

	// Check the date coming in...

	if ( date == null ) {
		return null;
	}

	// Calculate the row position of the data...

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "DayTS.getDataPosition", "Using " + date + "(" + date.getAbsoluteMonth() +
		") and start date: " + _date1 + "(" + _date1.getAbsoluteMonth() + ") for row-col calculation." );
	}

	_row = date.getAbsoluteMonth() - _date1.getAbsoluteMonth();

    // Calculate the column position of the data. We know that Daily data
    // is stored in a 2 dimensional array with the column being the daily data by interval.

    _column = date.getDay() - 1;
	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "DayTS.getDataPosition", "Row=" + _row + " Column=" + _column );
	}

	_pos[0] = _row;
	_pos[1] = _column;
	return _pos;
}

/**
Return the data value for a date.
<pre>
             	Day data is stored in a two-dimensional array:
  		     |----------------> days
  		     |
  		    \|/
  		   month 
</pre>
@return The data value corresponding to the date, or the missing data value if the date is not found.
@param date Date of interest.
*/
public double getDataValue( DateTime date )
{	// Do not define routine here to improve performance.

	// Check the date coming in 

    if ( (date == null) || !hasData() ) {
        return _missing;
    }
	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 50, "DayTS.getDataValue", date + " not within POR (" + _date1 + " - " + _date2 + ")" );
		}
		return _missing;
	}

	getDataPosition(date);

	if ( Message.isDebugOn ) {
		Message.printDebug( 50, "DayTS.getDataValue",
		    _data[_row][_column] + " for " + date + " from _data[" + _row + "][" + _column + "]" );
	}

	double value = 0;
	// FIXME SAM 2010-08-20 Possible to throw exceptions if the date is not the right precision and
	// illegal math results in negative values in arrays
	//try {
	    value = _data[_row][_column];
	//}
	//catch ( Exception e ) {
	//    Message.printWarning(3, "", "Error getting value for date " + date + "date1=" + _date1 + "_date2=" + _date2 + " row=" + _row + " col=" + _column);
	//    Message.printWarning(3, "", e);
	//}
	return value;
}

/**
Returns the data in the specified DataFlavor, or null if no matching flavor
exists.  From the Transferable interface.  Supported data flavors are:<br>
<ul>
<li>DayTS - DayTS.class / RTi.TS.DayTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(dayTSFlavor)) {	
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
Static method to return the flavors in which data can be transferred.  From the Transferable interface.
The order of the data flavors that are returned are:<br>
<ul>
<li>DayTS - DayTS.class / RTi.TS.DayTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[3];
	flavors[0] = dayTSFlavor;
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
Indicate whether the time series has data flags, determined by checking to see whether
the data flag space has been allocated and data flags are enabled for the time series.
@return true if data flags are available (the data flag space has been allocated).
Note that true will be returned even if all the data flag values are set to the missing data flag value.
*/
public boolean hasDataFlags ()
{	if ( _dataFlags != null ) {
		return super.hasDataFlags();
	}
	else {
	    return false;
	}
}

/**
Initialize data members.
*/
private void init()
{	_data = null;
	_data_interval_base = TimeInterval.DAY;
	_data_interval_mult = 1;
	_data_interval_base_original = TimeInterval.DAY;
	_data_interval_mult_original = 1;
	_pos = new int[2];
	_pos[0] = 0;
	_pos[1] = 0;
	_row = 0;
	_column = 0;
}

/**
Determines whether the specified flavor is supported as a transfer flavor.
From the Transferable interface.  Supported dataflavors are:<br>
<ul>
<li>DayTS - DayTS.class / RTi.TS.DayTS</li>
<li>TS - TS.class / RTi.TS.TS</li>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul> 
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(dayTSFlavor)) {
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
Refresh the derived data (e.g., data limits) if data have been set.  This is
normally only called from other package routines.
*/
public void refresh ()
{	TSLimits limits = null;

	// If the data is not dirty, then we do not have to refresh the other information...

	if ( !_dirty ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "DayTS.refresh", "Time series is not dirty.  Not recomputing limits" );
		}
		return;
	}

	// Else need to refresh...

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "DayTS.refresh", "Time Series is dirty. Recomputing limits" );
	}

	try {
	    limits = TSUtil.getDataLimits ( this, _date1, _date2, false );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, "DayTS.refresh", "Error getting data limits" );
		Message.printWarning ( 3, "DayTS.refresh", e );
		limits = null;
	}

	if ( (limits != null) && limits.areLimitsFound() ) {
		// Now reset the limits for the time series...
		setDataLimits ( limits );
	}

	_dirty = false;
}

/**
Set the data value for the date.
@param date Date of interest.
@param value Data value corresponding to date.
*/
public void setDataValue( DateTime date, double value )
{	if( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 10, "DayTS.setDataValue",
			"Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return;
	}

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "DayTS.setDataValue", "Setting " + value + " for " + date + " at " + _row + "," + _column );
	}

	// Set the dirty flag so that we know to recompute the limits if desired...

	_dirty = true;

	_data[_row][_column] = value;
}

/**
Set the data value and associated information for the date.
@param date Date of interest.
@param value Data value corresponding to date.
@param data_flag data_flag Data flag for value.
@param duration Duration for value (ignored - assumed to be 1-day or
instantaneous depending on data type).
*/
public void setDataValue ( DateTime date, double value, String data_flag, int duration )
{	if ( (date.lessThan(_date1)) || (date.greaterThan(_date2)) ) {
		if ( Message.isDebugOn ) {
			Message.printWarning( 10, "DayTS.setDataValue", "Date " + date + " is outside bounds " + _date1 + " - " + _date2 );
		}
		return;
	}

	getDataPosition ( date );

	if ( Message.isDebugOn ) {
		Message.printDebug( 30, "DayTS.setDataValue",
		"Setting " + value + " flag=" + data_flag + " for " + date + " at " + _row + "," + _column );
	}

	// Set the dirty flag so that we know to recompute the limits if desired...

	_dirty = true;

	_data[_row][_column] = value;
	if ( (data_flag != null) && (data_flag.length() > 0) ) {
	    if ( !_has_data_flags ) {
	        // Trying to set a data flag but space has not been allocated, so allocate the flag space
	        try {
	            allocateDataFlagSpace(null, false );
	        }
	        catch ( Exception e ) {
	            // Generally should not happen - log as debug because could generate a lot of warnings
	            if ( Message.isDebugOn ) {
    	            Message.printDebug(30, "DayTS.setDataValue", "Error allocating data flag space (" + e +
    	                ") - will not use flags." );
	            }
	            // Make sure to turn flags off
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
}

}
