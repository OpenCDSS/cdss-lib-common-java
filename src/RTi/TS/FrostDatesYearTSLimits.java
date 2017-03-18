// ----------------------------------------------------------------------------
// FrostDatesYearTSLimits - data limits for FrostDatesYearTS
// ----------------------------------------------------------------------------
// Notes:	(1)	This class stores the data limits for a
//			FrostDatesYearTS time series.
//			It stores the maximum and minimum values and the dates
//			associated with the values, as well as the average
//			values.
// ----------------------------------------------------------------------------
// History:
//
// 10 Jan 1999	Steven A. Malers, RTi	Initial version.  Extend TSLimits.
// 12 Apr 1999	SAM, RTi		Add finalize.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
The FrostDatesYearTSLimits class stores information about the data and date
limits of a FrostDatesYearTS time series.
*/
@SuppressWarnings("serial")
public class FrostDatesYearTSLimits extends TSLimits
{

// Time series that is being studied...

FrostDatesYearTS _ts = null;

// Data are for the total period but since we are not dealing with floating
// point numbers (like TSLimits), use special data here.

private DateTime _max_last_28F_spring;
private DateTime _max_last_32F_spring;
private DateTime _max_first_32F_fall;
private DateTime _max_first_28F_fall;

private DateTime _min_last_28F_spring;
private DateTime _min_last_32F_spring;
private DateTime _min_first_32F_fall;
private DateTime _min_first_28F_fall;

private DateTime _mean_last_28F_spring;
private DateTime _mean_last_32F_spring;
private DateTime _mean_first_32F_fall;
private DateTime _mean_first_28F_fall;

private int _count_last_28F_spring;
private int _count_last_32F_spring;
private int _count_first_32F_fall;
private int _count_first_28F_fall;

private int _missing_count_last_28F_spring;
private int _missing_count_last_32F_spring;
private int _missing_count_first_32F_fall;
private int _missing_count_first_28F_fall;

/**
Constructor.  Initialize the dates to null and the limits to zeros.
@exception TSException if there is an creating the limits.
*/
public FrostDatesYearTSLimits ()
throws TSException
{	super ();
	try {	initialize ();
	}
	catch ( Exception e ) {
		String message = "Error creating FrostDatesYearTSLimits";
		Message.printWarning ( 2, "FrostDatesYearTSLimits", message );
		throw new TSException ( message );
	}
}

/**
Copy constructor.
@param limits Instance to copy.
@exception TSException if there is an error c creating the limits.
*/
public FrostDatesYearTSLimits ( FrostDatesYearTSLimits limits )
throws TSException
{	super ();
	try {	initialize ();

	_max_last_28F_spring = limits._max_last_28F_spring;
	_max_last_32F_spring = limits._max_last_32F_spring;
	_max_first_32F_fall = limits._max_first_32F_fall;
	_max_first_28F_fall = limits._max_first_28F_fall;

	_min_last_28F_spring = limits._min_last_28F_spring;
	_min_last_32F_spring = limits._min_last_32F_spring;
	_min_first_32F_fall = limits._min_first_32F_fall;
	_min_first_28F_fall = limits._min_first_28F_fall;

	_mean_last_28F_spring = limits._mean_last_28F_spring;
	_mean_last_32F_spring = limits._mean_last_32F_spring;
	_mean_first_32F_fall = limits._mean_first_32F_fall;
	_mean_first_28F_fall = limits._mean_first_28F_fall;

	_count_last_28F_spring = limits._count_last_28F_spring;
	_count_last_32F_spring = limits._count_last_32F_spring;
	_count_first_32F_fall = limits._count_first_32F_fall;
	_count_first_28F_fall = limits._count_first_28F_fall;

	_missing_count_last_28F_spring = limits._missing_count_last_28F_spring;
	_missing_count_last_32F_spring = limits._missing_count_last_32F_spring;
	_missing_count_first_32F_fall = limits._missing_count_first_32F_fall;
	_missing_count_first_28F_fall = limits._missing_count_first_28F_fall;

	}
	catch ( Exception e ) {
		String message = "Error creating FrostDatesYearTSLimits";
		Message.printWarning ( 2, "FrostDatesYearTSLimits(copy)",
		message );
		throw new TSException ( message );
	}
}

/**
Constructor to compute the limits given a FrostDatesYearTS.  This is the main
constructor and is overloaded in a variety of ways.  If a variant of this
constructor that does not take a FrostDatesYearTS is used,
the limits are not computed
in this class and must be set from calling code.
@param ts Time series of interest.
@param startdate Starting date for the check.
@param enddate Ending date for the check.
@exception TSException if there is an error c creating the limits.
*/
public FrostDatesYearTSLimits (	FrostDatesYearTS ts,
				DateTime startdate, DateTime enddate )
throws TSException
{	super ();
	try {	initialize ();
		_ts = ts;
		getDataLimits ( ts, startdate, enddate );
	}
	catch ( Exception e ) {
		String message = "Error creating FrostDatesYearTSLimits";
		Message.printWarning ( 2, "FrostDatesYearTSLimits(...)",
			message );
		throw new TSException ( message );
	}
}

/**
Construct the FrostDatesYearTS limits for the full period.
@param ts Time series of interest.
@exception TSException if there is an error computing the limits.
*/
public FrostDatesYearTSLimits ( FrostDatesYearTS ts )
throws TSException
{	super ();
	try {	// Compute the monthly limits...
		initialize ();
		_ts = ts;
		getDataLimits ( ts, (DateTime)null, (DateTime)null );
	}
	catch ( Exception e ) {
		String message = "Error creating FrostDatesYearTSLimits";
		Message.printWarning ( 2, "FrostDatesYearTSLimits(ts)",message);
		throw new TSException ( message );
	}
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_ts = null;
	_max_last_28F_spring = null;
	_max_last_32F_spring = null;
	_max_first_32F_fall = null;
	_max_first_28F_fall = null;

	_min_last_28F_spring = null;
	_min_last_32F_spring = null;
	_min_first_32F_fall = null;
	_min_first_28F_fall = null;

	_mean_last_28F_spring = null;
	_mean_last_32F_spring = null;
	_mean_first_32F_fall = null;
	_mean_first_28F_fall = null;

	super.finalize();
}

/**
Compute the data limits for the time series given a period.
@param ts Time series of interest.
@param start0 Starting date for the check.
@param end0 Ending date for the check.
@exception TSException if there is an error computing the detailed limits.
*/
private void getDataLimits (	FrostDatesYearTS ts, DateTime start0,
				DateTime end0 )
throws TSException
{	String		message, routine="FrostDatesYearTSLimits.getDataLimits";
	int		base=0, mult = 0;
	DateTime	t = null;

	try {	// Overall try...

	if ( ts == null ) {
		message = "NULL time series";
		Message.printWarning ( 2, routine, message );
		throw new TSException ( message );
	}

	// Get valid date limits because the ones passed in may have been
	// null...

	TSLimits valid_dates = TSUtil.getValidPeriod ( ts, start0, end0 );
	DateTime start	= valid_dates.getDate1();
	DateTime end	= valid_dates.getDate2();
	valid_dates = null;
	setDate1 ( start );
	setDate2 ( end );

	// Make sure that the time series has current limits...

	base = ts.getDataIntervalBase();
	mult = ts.getDataIntervalMult();

	// Get the variables that are used often in this function

	// Dates to track max and min without the year.

	DateTime short_max_last_28F_spring = null;
	DateTime short_max_last_32F_spring = null;
	DateTime short_max_first_32F_fall = null;
	DateTime short_max_first_28F_fall = null;

	DateTime short_min_last_28F_spring = null;
	DateTime short_min_last_32F_spring = null;
	DateTime short_min_first_32F_fall = null;
	DateTime short_min_first_28F_fall = null;

	// First loop through and find the data limits and the
	// minimum non-missing date...

	t = new DateTime ( start, DateTime.DATE_FAST );
	double sum_last_28F_spring = 0.0;
	double sum_last_32F_spring = 0.0;
	double sum_first_32F_fall = 0.0;
	double sum_first_28F_fall = 0.0;
	DateTime full_date = null, short_date = null;	// Working dates.
	int year;
	for (	; t.lessThanOrEqualTo(end); t.addInterval( base, mult )) {
		year = t.getYear();

		// Process each date...

		// During processing we don't really care about the year so
		// set it to zero.  However, to show the full year for max,
		// min, etc., we need to add and subtract the year as
		// appropriate.  If this is a memory problem, we can always just
		// save the year and add/subtract as necessary.  The comparisons
		// are made using the short dates but the final result is a
		// full date to show the year that the max/min was recorded.

		short_date = ts.getLast28Spring ( year );
		if ( short_date == null ) {
			++_missing_count_last_28F_spring;
		}
		else {	full_date = new DateTime ( short_date );
			short_date.setYear ( 0 );
			sum_last_28F_spring += short_date.toDouble();
			++_count_last_28F_spring;
			if ( short_max_last_28F_spring == null ) {
				short_max_last_28F_spring =
				new DateTime ( short_date );
				_max_last_28F_spring = new DateTime(full_date );
			}
			else if ( short_date.greaterThan(
				short_max_last_28F_spring)) {
				short_max_last_28F_spring =
				new DateTime ( short_date );
				_max_last_28F_spring = new DateTime(full_date );
			}
			if ( short_min_last_28F_spring == null ) {
				short_min_last_28F_spring =
				new DateTime ( short_date );
				_min_last_28F_spring = new DateTime(full_date );
			}
			else if ( short_date.lessThan(
				short_min_last_28F_spring) ) {
				short_min_last_28F_spring =
				new DateTime ( short_date );
				_min_last_28F_spring = new DateTime(full_date );
			}
		}

		short_date = ts.getLast32Spring ( year );
		if ( short_date == null ) {
			++_missing_count_last_32F_spring;
		}
		else {	full_date = new DateTime ( short_date );
			short_date.setYear ( 0 );
			sum_last_32F_spring += short_date.toDouble();
			++_count_last_32F_spring;
			if ( short_max_last_32F_spring == null ) {
				short_max_last_32F_spring =
				new DateTime ( short_date );
				_max_last_32F_spring = new DateTime(full_date );
			}
			else if ( short_date.greaterThan(
				short_max_last_32F_spring) ) {
				short_max_last_32F_spring =
				new DateTime ( short_date );
				_max_last_32F_spring = new DateTime(full_date );
			}
			if ( short_min_last_32F_spring == null ) {
				short_min_last_32F_spring =
				new DateTime ( short_date );
				_min_last_32F_spring = new DateTime(full_date );
			}
			else if ( short_date.lessThan(
				short_min_last_32F_spring) ) {
				short_min_last_32F_spring =
				new DateTime ( short_date );
				_min_last_32F_spring = new DateTime(full_date );
			}
		}

		short_date = ts.getFirst32Fall ( year );
		if ( short_date == null ) {
			++_missing_count_first_32F_fall;
		}
		else {	full_date = new DateTime ( short_date );
			short_date.setYear ( 0 );
			sum_first_32F_fall += short_date.toDouble();
			++_count_first_32F_fall;
			if ( short_max_first_32F_fall == null ) {
				short_max_first_32F_fall =
				new DateTime ( short_date );
				_max_first_32F_fall = new DateTime (full_date );
			}
			else if ( short_date.greaterThan(
				short_max_first_32F_fall) ) {
				short_max_first_32F_fall =
				new DateTime ( short_date );
				_max_first_32F_fall = new DateTime (full_date );
			}
			if ( short_min_first_32F_fall == null ) {
				short_min_first_32F_fall =
				new DateTime ( short_date );
				_min_first_32F_fall = new DateTime (full_date );
			}
			else if ( short_date.lessThan(
				short_min_first_32F_fall) ) {
				short_min_first_32F_fall =
				new DateTime ( short_date );
				_min_first_32F_fall = new DateTime (full_date );
			}
		}

		short_date = ts.getFirst28Fall ( year );
		if ( short_date == null ) {
			++_missing_count_first_28F_fall;
		}
		else {	full_date = new DateTime ( short_date );
			short_date.setYear ( 0 );
			sum_first_28F_fall += short_date.toDouble();
			++_count_first_28F_fall;
			if ( short_max_first_28F_fall == null ) {
				short_max_first_28F_fall =
				new DateTime ( short_date );
				_max_first_28F_fall = new DateTime (full_date );
			}
			else if ( short_date.greaterThan(
				short_max_first_28F_fall) ) {
				short_max_first_28F_fall =
				new DateTime ( short_date );
				_max_first_28F_fall = new DateTime (full_date );
			}
			if ( short_min_first_28F_fall == null ) {
				short_min_first_28F_fall =
				new DateTime ( short_date );
				_min_first_28F_fall = new DateTime (full_date );
			}
			else if ( short_date.lessThan(
				short_min_first_28F_fall) ) {
				short_min_first_28F_fall =
				new DateTime ( short_date );
				_min_first_28F_fall = new DateTime (full_date );
			}
		}
	}

/*
	if( !found ){
		message = "\"" + ts.getIdentifierString() +
		"\": problems finding limits, whole POR missing!";
		Message.printWarning( 2, routine, message );
		throw new TSException ( message );
	}
*/

	// Now compute the mean...

	if ( _count_last_28F_spring != 0 ) {
		_mean_last_28F_spring = new DateTime ( sum_last_28F_spring/
					_count_last_28F_spring, true );
	}
	if ( _count_last_32F_spring != 0 ) {
		_mean_last_32F_spring = new DateTime ( sum_last_32F_spring/
					_count_last_32F_spring, true );
	}
	if ( _count_first_32F_fall != 0 ) {
		_mean_first_32F_fall = new DateTime ( sum_first_32F_fall/
					_count_first_32F_fall, true );
	}
	if ( _count_first_28F_fall != 0 ) {
		_mean_first_28F_fall = new DateTime ( sum_first_28F_fall/
					_count_first_28F_fall, true );
	}
	// Clean up...
	t = null;
	start = null;
	end = null;
	full_date = null;
	short_date = null;
	short_max_last_28F_spring = null;
	short_max_last_32F_spring = null;
	short_max_first_32F_fall = null;
	short_max_first_28F_fall = null;

	short_min_last_28F_spring = null;
	short_min_last_32F_spring = null;
	short_min_first_32F_fall = null;
	short_min_first_28F_fall = null;
	}
	catch ( Exception e ) {
		message = "Error computing data limits.";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new TSException ( message );
	}
	message = null;
	routine = null;
}

/**
Return the maximum date corresponding to the first 28 F temperature.
@return The maximum date corresponding to the first 28 F temperature in the
fall, or null if not available.
*/
public DateTime getMaxFirst28Fall ()
{	return _max_first_28F_fall;
}

/**
Return the maximum date corresponding to the first 32 F temperature.
@return The maximum date corresponding to the first 32 F temperature in the
fall, or null if not available.
*/
public DateTime getMaxFirst32Fall ()
{	return _max_first_32F_fall;
}

/**
Return the maximum date corresponding to the last 28 F temperature.
@return The maximum date corresponding to the last 28 F temperature in the
spring, or null if not available.
*/
public DateTime getMaxLast28Spring ()
{	return _max_last_28F_spring;
}

/**
Return the maximum date corresponding to the last 32 F temperature.
@return The maximum date corresponding to the last 32 F temperature in the
spring, or null if not available.
*/
public DateTime getMaxLast32Spring ()
{	return _max_last_32F_spring;
}

/**
Return the mean date corresponding to the first 28 F temperature.
@return The mean date corresponding to the first 28 F temperature in the
fall, or null if not available.
*/
public DateTime getMeanFirst28Fall ()
{	return _mean_first_28F_fall;
}

/**
Return the mean date corresponding to the first 32 F temperature.
@return The mean date corresponding to the first 32 F temperature in the
fall, or null if not available.
*/
public DateTime getMeanFirst32Fall ()
{	return _mean_first_32F_fall;
}

/**
Return The mean date corresponding to the last 28 F temperature.
@return The mean date corresponding to the last 28 F temperature in the
spring, or null if not available.
*/
public DateTime getMeanLast28Spring ()
{	return _mean_last_28F_spring;
}

/**
Return the mean date corresponding to the last 32 F temperature.
@return The mean date corresponding to the last 32 F temperature in the
spring, or null if not available.
*/
public DateTime getMeanLast32Spring ()
{	return _mean_last_32F_spring;
}

/**
Return the minimum date corresponding to the first 28 F temperature.
@return The minimum date corresponding to the first 28 F temperature in the
fall, or null if not available.
*/
public DateTime getMinFirst28Fall ()
{	return _min_first_28F_fall;
}

/**
Return the minimum date corresponding to the first 32 F temperature.
@return The minimum date corresponding to the first 32 F temperature in the
fall, or null if not available.
*/
public DateTime getMinFirst32Fall ()
{	return _min_first_32F_fall;
}

/**
Return the minimum date corresponding to the last 28 F temperature.
@return The minimum date corresponding to the last 28 F temperature in the
spring, or null if not available.
*/
public DateTime getMinLast28Spring ()
{	return _min_last_28F_spring;
}

/**
Return the minimum date corresponding to the last 32 F temperature.
@return The minimum date corresponding to the last 32 F temperature in the
spring, or null if not available.
*/
public DateTime getMinLast32Spring ()
{	return _min_last_32F_spring;
}

/**
Initialize the instance data.
*/
private void initialize ()
{	_ts = null;	// No time series specified in constructor.

	_max_last_28F_spring = null;
	_max_last_32F_spring = null;
	_max_first_32F_fall = null;
	_max_first_28F_fall = null;

	_min_last_28F_spring = null;
	_min_last_32F_spring = null;
	_min_first_32F_fall = null;
	_min_first_28F_fall = null;

	_mean_last_28F_spring = null;
	_mean_last_32F_spring = null;
	_mean_first_32F_fall = null;
	_mean_first_28F_fall = null;

	_count_last_28F_spring = 0;
	_count_last_32F_spring = 0;
	_count_first_32F_fall = 0;
	_count_first_28F_fall = 0;

	_missing_count_last_28F_spring = 0;
	_missing_count_last_32F_spring = 0;
	_missing_count_first_32F_fall = 0;
	_missing_count_first_28F_fall = 0;
}

/**
Return A verbose string representation of the limits.
@return A verbose string representation of the limits.
*/
public String toString ( )
{	String nl = System.getProperty ( "line.separator" );

	StringBuffer buffer = new StringBuffer ( );
	if ( _ts != null ) {
		buffer.append ( "Time series:  " + _ts.getIdentifierString() +
		" (" + _ts.getDataUnits() + ")" +nl );
	}
	buffer.append (
"Data limits for period " + getDate1() + " to " + getDate2() +
" are as follows.  Maximum and minimum" + nl +
"dates shown were computed using month and day." + nl +
"                                                     Number    Number Not"+nl +
"Frost Temp.        MinDate    MaxDate    MeanDate    Missing   Missing" + nl +
"------------------------------------------------------------------------------" + nl );
	String empty_date_string = "          ";
	// Handle the individual data that may be null...
	// First row...
	buffer.append ( "Last 28 F Spring " );
	if ( _min_last_28F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _min_last_28F_spring.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _max_last_28F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _max_last_28F_spring.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _mean_last_28F_spring == null ) {
		buffer.append ( empty_date_string );
	}
	else {	buffer.append ( "    " + _mean_last_28F_spring.toString(
			DateTime.FORMAT_MM_DD) + " " );
	}
	buffer.append ( StringUtil.formatString(
			_missing_count_last_28F_spring, "%9d") );
	buffer.append ( "    " + StringUtil.formatString(
			_count_last_28F_spring, "%9d") + nl );
	// Second row...
	buffer.append ( "Last 32 F Spring " );
	if ( _min_last_32F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _min_last_32F_spring.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _max_last_32F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _max_last_32F_spring.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _mean_last_32F_spring == null ) {
		buffer.append ( empty_date_string );
	}
	else {	buffer.append ( "    " + _mean_last_32F_spring.toString(
			DateTime.FORMAT_MM_DD) + " " );
	}
	buffer.append ( StringUtil.formatString(
			_missing_count_last_32F_spring, "%9d") );
	buffer.append ( "    " + StringUtil.formatString(
			_count_last_32F_spring, "%9d") + nl );
	// Third row...
	buffer.append ( "First 32 F Fall  " );
	if ( _min_last_32F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _min_first_32F_fall.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _max_first_32F_fall == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _max_first_32F_fall.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _mean_first_32F_fall == null ) {
		buffer.append ( empty_date_string );
	}
	else {	buffer.append ( "    " + _mean_first_32F_fall.toString(
			DateTime.FORMAT_MM_DD) + " " );
	}
	buffer.append ( StringUtil.formatString(
			_missing_count_first_32F_fall, "%9d") );
	buffer.append ( "    " + StringUtil.formatString(
			_count_first_32F_fall, "%9d") + nl );
	// Fourth row...
	buffer.append ( "First 28 F Fall  " );
	if ( _min_last_28F_spring == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _min_first_28F_fall.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _max_first_28F_fall == null ) {
		buffer.append ( empty_date_string + " " );
	}
	else {	buffer.append ( _max_first_28F_fall.toString(
			DateTime.FORMAT_YYYY_MM_DD) + " " );
	}
	if ( _mean_first_28F_fall == null ) {
		buffer.append ( empty_date_string );
	}
	else {	buffer.append ( "    " + _mean_first_28F_fall.toString(
			DateTime.FORMAT_MM_DD) + " " );
	}
	buffer.append ( StringUtil.formatString(
			_missing_count_first_28F_fall, "%9d") );
	buffer.append ( "    " + StringUtil.formatString(
			_count_first_28F_fall, "%9d") + nl );
	buffer.append (
"------------------------------------------------------------------------------" + nl );
	String s = buffer.toString();
	nl = null;
	empty_date_string = null;
	buffer = null;
	return s;
}

} // End of FrostDatesYearTSLimits class definition
