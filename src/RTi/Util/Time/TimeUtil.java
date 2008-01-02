// ----------------------------------------------------------------------------
// TimeUtil - date/time utility data and methods
// ----------------------------------------------------------------------------
// History:
//
// 05 Jan 1998	Steven A. Malers,	Start getting documentation in order.
//		Riverside Technology,	Move TSDate.computeDayOfWeek to
//		inc.			TimeUtil.getCurrentDayOfWeek.
// 04 Mar 1998	SAM, RTi		Add overload for numDaysInMonths.
// 14 Mar 1998	SAM, RTi		Add javadoc.
// 08 May 1998  DLG, RTi		Added setLocalTimeZone functions.
// 15 May 1998  DLG, RTi		Fixed bug in isValidMinute, isValidHour
//					to restrict the range to 0-59 and 0-23
//					respectively.
// 21 Jun 1998	SAM, RTi		Add getDayAndMonthFromYearDay.
// 23 Jun 1998	SAM, RTi		Remove dependence on TS by adding time
//					zone information here.  Just copy the
//					TSTimeZone.getDefinedCode function to
//					here.
// 30 Jun 1998	SAM, RTi		Add getAbsoluteDay.
// 12 Jan 1999	SAM, RTi		Deprecate the old version of
//					getMonthAndDayFromDayOfYear and
//					replace with a more robust version.
// 28 Mar 2001	SAM, RTi		Change getAbsoluteDay() to
//					absoluteDay().  Add absoluteMinute().
//					Get rid of * imports.
// 08 Jul 2001	SAM, RTi		Add irrigation*FromCalendar() and
//					water*FromCalendar() methods.  Although
//					these are not generic to all code, they
//					are common enough in what RTi does to
//					put here.
// 22 Aug 2001	SAM, RTi		Add waitForFile().
// 2001-12-13	SAM, RTi		Add formatDateTime().  Transfer the
//					following from DateTime:
//						getDateTimeFromIndex()
//						getLocalTimeZone()
//						getNumIntervals()
//						isDateTime()
//					Use TZ and TZData instead of
//					TimeZoneData.
//					Verify that variables are set to null
//					when no longer used.  Change
//					getSystemTimeString() to use
//					formatDateTime().
//					Remove deprecated getTimeString(),
//					getAbsoluteMonth(), getDefinedCode().
//					Change setLocalTimeZone() to set the
//					same data used by
//					getLocalTimeZoneAbbr().
//					Add subtract() from DateTime.
//					Change %Z when dealing with dates to
//					use the TimeZone.getID() rather than
//					look up from a GMT offset (too
//					complicated).
// 2002-02-02	SAM, RTi		Change getDateFromIndex() to
//					addIntervals().
// 2002-05-21	SAM, RTi		Update formatDateTime() to pass through
//					unrecognized %X format specifiers so
//					that the string can be formatted in a
//					secondary formatter.
//					Add getDateTimeFormatSpecifiers().
// 2003-02-20	SAM, RTi		Fix numDaysInMonth(), which was not
//					checking for month 0 properly.
// 2003-06-03	SAM, RTi		Change diff() methods to be static.
// 2003-09-19	SAM, RTi		* formatDateTime() was not handling day
//					  of week abbreviations - was returning
//					  full day.
//					* Update formatDateTime() to use
//					  GregorianCalendar.
// 2003-10-10	SAM, RTi		* Add numDaysInMonth(DateTime).
// 2003-12-08	SAM, RTi		* Add dayOfYear(DateTime).
// 2005-02-24	SAM, RTi		Add highestPrecision() and
//					lowestPrecision().
// 2005-09-27	SAM, RTi		* Add max() and min() to simplify
//					  handling of DateTime comparisons.
//					* Change warning levels from 2 to 3 to
//					  facilitate use of the log file viewer.
// 2005-11-16	SAM, RTi		* Add
//					  convertCalendarMonthToCustomMonth().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.Time;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.IO.IOUtil;

import java.lang.Long;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
The TimeUtil class provides time utility methods for date/time data, independent
of use in time series or other classes.  There is no "Time" or "Date" class
other than what is supplied by Java or RTi's DateTime class (TSDate is being
phased out).  Conventions used for all methods are:
<p>
Years are 4-digit.<br>
Months are 1-12.<br>
Days are 1-31.<br>
Hours are 0-23.<br>
Minutes are 0-59.<br>
Seconds are 0-59.<br>
HSeconds are 0-99.<br>
<p>
*/

public abstract class TimeUtil {

/**
Datum for absolute day = days inclusive of Dec 31, 1799.
This has been computed by looping through years 1-1799 adding numDaysInYear.
This constant can be used when computing absolute days (e.g., to calculate the
number of days in a period).
*/
public final static int ABSOLUTE_DAY_DATUM = 657071;

/**
The following indicates how time zones are handled when getLocalTimeZone() is
called (which is used when DateTime instances are created).  The default is
LOOKUP_TIME_ZONE_ONCE, which results in the best performance when the time
zone is not expected to change within a run.  However, if a time zone change
will cause a problem, LOOKUP_TIME_ZONE_ALWAYS should be used (however, this
results in slower performance).
*/
public static final int LOOKUP_TIME_ZONE_ONCE = 1;

/**
The following indicates that for DateTime construction the local time zone is
looked up each time a DateTime is created.  This should be considered when
running a real-time application that runs continuously between time zone
changes.
*/
public static final int LOOKUP_TIME_ZONE_ALWAYS	= 2;

/**
Local Time Zone String (e.g, "MST", "MDT", "GMT"), based on the last call to
getLocalTimeZone().
@deprecated Use the getLocalTimeZone() or getLocalTimeZoneAbbr() methods or
use the protected _local_time_zone_string if in this package.
*/
public static String localTimeZone = "";

/**
Time zone Strings.
@deprecated Use TZ class.
*/
public final static String	MST	= "MST",
				MDT	= "MDT",
				GMT	= "GMT";

/**
Abbreviations for months.
*/
public static final String MONTH_ABBREVIATIONS[] = { "Jan", "Feb", "Mar", "Apr",
						"May", "Jun", "Jul", "Aug",
						"Sep", "Oct", "Nov", "Dec" };

/**
Full names for months.
*/
public static final String MONTH_NAMES[] = {	"January", "February", "March",	
						"April", "May", "June",
						"July", "August", "September",
						"October", "November",
						"December" };

/**
Abbreviations for days.
*/
public static final String DAY_ABBREVIATIONS[] = {	"Sun", "Mon", "Tue",
							"Wed", "Thu", "Fri",
							"Sat" };

/**
Full names for months.
*/
public static final String DAY_NAMES[] = {		"Sunday", "Monday",
							"Tuesday", "Wednesday",
							"Thursday", "Friday",
							"Saturday" };

/**
Days in months (non-leap year).
*/
public static final int MONTH_DAYS[] = {	31, 28, 31, 30, 31, 30,
						31, 31, 30, 31, 30, 31 };

/**
For a month, the number of days in the year passed on the first day of the
month (non-leap year).
*/
public static final int MONTH_YEARDAYS[] = {	0, 31, 59, 90, 120, 151,
						181, 212, 243, 273, 304, 334 };

// Static data shared in package (so DateTime can get to easily)...

protected static TimeZone _local_time_zone = null;
protected static String _local_time_zone_string = "";
protected static boolean _local_time_zone_retrieved = false;
protected static int _time_zone_lookup_method = LOOKUP_TIME_ZONE_ONCE;

/**
Compute the absolute day.  This can be used for determining the difference
between dates.
@return The absolute day, with respect to Dec 31, 1799.  The datum may change
in the future and should be used only in a dynamic fashion.
@param year Year (4-digit).
@param month Month number (1-12).
@param day Day number (1-31).
*/
public static int absoluteDay ( int year, int month, int day )
{	int leap = 0;
	if ( isLeapYear(year) ) {
		leap = 1;
	}
	int aday =
		day					// day of month
		+ numDaysInMonths (1, year, (month-1))	// days in prev months
		+ 365*year 				// days in prev years
		+ year/4 				// 1 if leap year
		- year/100 				// -3 every 400 years
		+ year/400 				// 1 every 400 years
		- ABSOLUTE_DAY_DATUM 			// Jan 1, 1800
		- leap;					// cancel /4 term - 
							// will be added by
							// numDaysInMonths
	return aday;
}

/**
Compute the absolute minute.  This can be used for determining the difference
between dates.
@return The absolute minute, with respect to Dec 31, 1799.  The datum may change
in the future and should be used only in a dynamic fashion.
@param year Year (4-digit).
@param month Month number (1-12).
@param day Day number (1-31).
@param hour Hour (0-23).
@param minute Minute (0-59).
*/
public static long absoluteMinute (	int year, int month, int day, int hour,
					int minute )
{	int aday = absoluteDay ( year, month, day );
	long aminute = aday*1440 + hour*60 + minute;
	return aminute;
}

/**
Return the absolute month, which is the year*12 + month.
@return The absolute month, which is the year*12 + month.
@param month Month number.
@param year Year.
*/
public static int absoluteMonth ( int month, int year )
{	return ( year*12 + month );
}

/**
Given a starting DateTime, an interval, and the number of intervals, increment
the DateTime, returning the final DateTime.
@return incremented DateTime (new instance is returned).
@param t1 Initial DateTime.
@param base Interval base (see TimeInterval).
@param mult Interval multiplier.
@param nintervals Number of times to increment.
*/
public static DateTime addIntervals ( 	DateTime t1, int base, int mult,
					int nintervals )
{	DateTime t = new DateTime ( t1 );
	for ( int count = 0; count < nintervals; count ++ ){
		t.addInterval( base, mult );
	}
	return t;
}

/**
Indicate whether a DateTime's precision matches the specified TimeInterval string.
This is useful, for example, in confirming that DateTime's for a time series are
consistent with the time series data interval.
@param dt DateTime to check.
@param interval TimeInterval string to check.
@return 0 if the percision of the DateTime is the same as the interval,
-1 if the precision of the DateTime is less than the interval,
1 if the precision of the DateTime is greater than the interval, and
null if the input is invalid.
*/
public static Integer compareDateTimePrecisionToTimeInterval ( DateTime dt, String interval_string )
{
    TimeInterval ti = null;
    try {
        ti = TimeInterval.parseInterval(interval_string);
    }
    catch ( Exception e ) {
        return null;
    }
    return compareDateTimePrecisionToTimeInterval(dt,ti.getBase());
}

/**
Indicate whether a DateTime's precision matches the specified TimeInterval string.
This is useful, for example, in confirming that DateTime's for a time series are
consistent with the time series data interval.
@param dt DateTime to check.
@param interval TimeInterval integer to check.
@return 0 if the percision of the DateTime is the same as the interval,
-1 if the precision of the DateTime is less than the interval, and
1 if the precision of the DateTime is greater than the interval, and
null if the input is invalid.
*/
public static Integer compareDateTimePrecisionToTimeInterval ( DateTime dt, int interval )
{
    int precision = dt.getPrecision();
    if ( precision < interval ) {
        return new Integer(-1);
    }
    else if ( precision > interval ) {
        return new Integer(1);
    }
    else {
        return new Integer(0);
    }
}

/**
Convert a calendar month (1=January,...,12=December) to a month in a special
calendar.  For example, water years are Oct to Sep.  To determine the month
number (1+) in a water year given a calendar year month, do the following:
<pre>
int month = convertCalendarMonthToSpecialMonth ( cal_month, 10 );
</pre>
@param cal_month The calendar month (1=January, etc.).
@param first_cal_month_in_year The calendar month corresponding to the first
month in the special calendar.  For example, for water years, specify 10 for
October.
@return the month number in the custom calendar (1 to 12).
*/
public static int convertCalendarMonthToCustomMonth (
				int cal_month, int first_cal_month_in_year )
{	if ( cal_month >= first_cal_month_in_year ) {
		// This is the only clause that is processed for calendar year
		// and is used for high calendar months in custom calendars.
		// For example for water year (first_cal_month_in_year = 10),
		// return cal_month - 3
		return (cal_month - (first_cal_month_in_year - 1) );
	}
	else {	// This will be processed for non-calendar year for early months
		// in the calendar year.
		// For example for water year, return cal_month + 3
		return (cal_month + (12 - first_cal_month_in_year + 1) );
	}
}

/**
Return the day of the year.
@return The day of the year (where 1 is the first day of the year and 365 or
366 is the last).  Return -1 if an error.
@param d Java Date.
*/
public static int dayOfYear ( Date d )
{	if ( d == null ) {
		return -1;
	}
	// First get a string...
	String s = formatTimeString ( d, "%j" );
	// Now convert to an integer...
	Integer i = new Integer (s);
	int day = i.intValue();
	s = null;
	i = null;
	return day;
}

/**
Return the day of the year.
@return The day of the year (where 1 is the first day of the year and 365 or
366 is the last).  Return -1 if an error.
@param d Java Date.
*/
public static int dayOfYear ( DateTime d )
{	if ( d == null ) {
		return -1;
	}
	int day = 0;
	// Days in previous months...
	if ( d.getMonth() > 1 ) {
		day += numDaysInMonths ( 1, d.getYear(), (d.getMonth() - 1) );
	}
	// Add the days from this month...
	day += d.getDay();
	return day;
}

/**
Compute the difference between dates.  See overloaded routine for more
information.
@return The offset from the date instance to the given date.  For example, if
"date" is before the instance date, then the offset will be positive.
@param date1 The date to be subtracted from.
@param date2 The date to subtract from date1.
REVISIT JAVADOC: see RTi.Util.Time.DateTime.add
@exception Exception if either date is null.
*/
public static DateTime diff ( DateTime date1, DateTime date2 )
throws Exception
{	return diff ( date1, date2, false );
}

/**
Subtract a date from another and return the offset.  This can then be used
with the DateTime.add method to shift a date.  It is important to only compare
dates of the same precision.
@return The offset from the date instance to the given date.  For example, if
"date" is before the instance date, then the offset will be positive.
@param date1 The DateTime to be subtracted from.
@param date2 The DateTime to subtract from date1.
@param use_month If true, the offset will be computed by setting the month also.
In many cases, this is not valid because the relationship between days and
months is dynamic.  If false, the month will be set to zero and the day will be
set to the total number of days in the year from the difference.
@exception Exception if either date is null.
*/
public static DateTime diff ( DateTime date1, DateTime date2, boolean use_month)
throws Exception
{	if ( date1 == null ) {
		Message.printWarning ( 3, "TimeUtil.subtract", "Null date" );
		throw new Exception ( "Null date for diff" );
	}
	if ( date2 == null ) {
		Message.printWarning ( 3, "TimeUtil.subtract", "Null date" );
		throw new Exception ( "Null date for diff" );
	}

	DateTime offset = new DateTime ( DateTime.DATE_ZERO|DateTime.DATE_FAST );
	offset.setYear ( 0 );
	offset.setMonth ( 0 );
	offset.setDay ( 0 );
	offset.setHour ( 0 );
	offset.setMinute ( 0 );
	offset.setSecond ( 0 );
	offset.setHSecond ( 0 );
	DateTime datecopy = new DateTime ( date2 );

	// First see if we need to add or subtract...

	if ( datecopy.equals(date1) ) {
		// Special case is no difference in the dates so the offset
		// date must be handled accordingly. This means setting the
		// month and day to zero instead month 1 and day 1 for a
		// DateTime(DateTime.DATE_ZERO) object.

		offset.setMonth ( 0 );
		offset.setDay ( 0 );

		// We are done...
		datecopy = null;
		return offset;
	}

	// Handle some special cases that are often encountered and handled the
	// others generically...

	if ( date1.getPrecision() == DateTime.PRECISION_DAY ) {
		// If Day and month are the same, return the year as the offset...
		if ( (date1.getDay() == date2.getDay()) && (date1.getMonth() == date2.getMonth()) ) {
			offset.setYear ( date1.getYear() - date2.getYear() );
		}
	}
	else {
        // All other cases...
		// For the moment, ignore the old logic and use floating
		// point numbers to do the offset.  Test to see if it works...
		double date_copy_double = datecopy.toDouble();
		double this_double = date1.toDouble();
		double offset_double = this_double - date_copy_double;
		offset = new DateTime ( offset_double, use_month );
	}

	datecopy = null;
	return offset;
}

/**
Format a DateTime using the given format.
@return The date/time as a string for the specified date using the specified
format.
@param d0 The date to format.  If the date is null, the current time is used.
@param format0 The date format.  If the format is null,
the default is as follows:  "Fri Jan 03 16:05:14 MST 1998" (the UNIX date
command output).  The date can be formatted using the format modifiers of the
C "strftime" routine, as follows:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Format Specifier</b></td>	<td><b>Description</b></td>
</tr

<tr>
<td><b>%a</b></td>
<td>The abbreviated weekday.</td>
</tr>

<tr>
<td><b>%A</b></td>
<td>The full weekday.</td>
</tr>

<tr>
<td><b>%b</b></td>
<td>The abbreviated month.</td>
</tr>

<tr>
<td><b>%B</b></td>
<td>The full month.</td>
</tr>

<tr>
<td><b>%c</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%d</b></td>
<td>Day of month in range 1-31.</td>
</tr>

<tr>
<td><b>%H</b></td>
<td>Hour of day in range 0-23.</td>
</tr>

<tr>
<td><b>%I</b></td>
<td>Hour of day in range 0-12.</td>
</tr>

<tr>
<td><b>%j</b></td>
<td>Day of year in range 1-366.</td>
</tr>

<tr>
<td><b>%m</b></td>
<td>Month of year in range 1-12.</td>
</tr>

<tr>
<td><b>%M</b></td>
<td>Minute of hour in range 0-59.</td>
</tr>

<tr>
<td><b>%p</b></td>
<td>"AM" or "PM".</td>
</tr>

<tr>
<td><b>%S</b></td>
<td>Seconds of minute in range 0-59.</td>
</tr>

<tr>
<td><b>%U</b> or <b>%W</b></td>
<td>Week of year in range 1-52.</td>
</tr>

<tr>
<td><b>%x</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%X</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%y</b></td>
<td>Two digit year since 1900 (this is use discouraged because the datum is
ambiguous).</td>
</tr>

<tr>
<td><b>%Y</b></td>
<td>Four digit year.</td>
</tr>

<tr>
<td><b>%Z</b></td>
<td>Three character time zone abbreviation.</td>
</tr>

<tr>
<td><b>%%</b></td>
<td>Literal % character.</td>
</tr>

</table>
*/
public static String formatDateTime ( DateTime d0, String format0 )
{	String	default_format = "%a %b %d %H:%M:%S %Z %Y";
	String	format;

	if ( format0 == null ) {
		format = default_format;
	}
	else if ( format0.length() == 0 ) {
		format = default_format;
	}
	else {
        format = format0;
	}

	DateTime d;
	if ( d0 == null ) {
		// Get the current time...
		d = new DateTime(DateTime.DATE_CURRENT);
	}
	else {
        d = d0;
	}

	if ( format.equals("datum_seconds") ) {
		// Want the number of seconds since the standard time datum	
		// Need to work on this...
		//long seconds = d.getTime ();
		//return Long.toString ( seconds/1000 );
		return "0";
	}
	// Convert format to string...
	GregorianCalendar cal = new GregorianCalendar ();
	cal.setTime ( d.getDate() );
	SimpleDateFormat sdf = new SimpleDateFormat();
	DateFormatSymbols dfs = sdf.getDateFormatSymbols();
	String[]	short_weekdays = dfs.getShortWeekdays();
	String[]	short_months = dfs.getShortMonths();
	String[]	months = dfs.getMonths();
	String[]	weekdays = dfs.getWeekdays();
	int		len = format.length();
	StringBuffer	formatted_string = new StringBuffer ();
	char		c = '\0';
	int		ifield;
	// The values returned are as follows:
	//
	//		Java		We use
	//
	// Year:	since 1900	4-digit
	// Month:	0 to 11		1 to 12
	// Day:		1 to 31		1 to 31
	// Hour:	0 to 59		same
	// Minute:	0 to 59		same
	// Second:	0 to 59		same
	// DayOfWeek:	0 to 7 with 0	same
	//		being Sunday
	//		in U.S.
	for ( int i = 0; i < len; i++ ) {
		c = format.charAt(i);
		if ( c == '%' ) {
			// We have a format character...
			++i;
			if ( i >= len ) {
				break;	// this will exit the whole loop
			}
			c = format.charAt(i);
			if ( c == 'a' ) {
				// Abbreviated weekday name.
				ifield = cal.get(Calendar.DAY_OF_WEEK);
				formatted_string.append(short_weekdays[ifield]);
			}
			else if ( c == 'A' ) {
				// Full weekday name.
				ifield = cal.get(Calendar.DAY_OF_WEEK);
				formatted_string.append(weekdays[ifield]);
			}
			else if ( c == 'b' ) {
				// Abbreviated month name.
				ifield = cal.get(Calendar.MONTH);
				formatted_string.append( short_months[ifield]);
			}
			else if ( c == 'B' ) {
				// Long month name.
				ifield = cal.get(Calendar.MONTH);
				formatted_string.append( months[ifield]);
			}
			else if ( c == 'c' ) {
				formatted_string.append( "%c not supported" );
			}
			else if ( c == 'd' ) {
				// Day of month
				formatted_string.append( StringUtil.formatString(d.getDay(),"%02d"));
			}
			else if ( c == 'H' ) {
				// Hour of day...
				formatted_string.append( StringUtil.formatString(d.getHour(),"%02d"));
			}
			else if ( c == 'I' ) {
				// Hour of day 1-12
				if ( d.getHour() > 12 ) {
					formatted_string.append(StringUtil.formatString((d.getHour() - 12),"%02d"));
				}
				else {
                    formatted_string.append(StringUtil.formatString(d.getHour(),"%02d"));
				}
			}
			else if ( c == 'j' ) {
				// Day of year...
				formatted_string.append( StringUtil.formatString(d.getYearDay(),"%03d"));
			}
			else if ( c == 'm' ) {
				// Month of year...
				formatted_string.append( StringUtil.formatString( d.getMonth(),"%02d"));
			}
			else if ( c == 'M' ) {
				// Minute of hour...
				formatted_string.append( StringUtil.formatString( d.getMinute(),"%02d"));
			}
			else if ( c == 'p' ) {
				// AM or PM...
				if ( d.getHour() < 12 ) {
					formatted_string.append("AM");
				}
				else {
                    formatted_string.append("PM");
				}
			}
			else if ( c == 'S' ) {
				// Seconds of minute...
				formatted_string.append(StringUtil.formatString( d.getSecond(),"%02d"));
			}
			else if ( (c == 'U') || (c == 'W')) {
				// Week of year...
				ifield = cal.get(Calendar.WEEK_OF_YEAR);
				formatted_string.append( StringUtil.formatString(ifield,"%02d"));
			}
			else if ( c == 'x' ) {
				formatted_string.append( "%x not supported" );
			}
			else if ( c == 'X' ) {
				formatted_string.append( "%X not supported" );
			}
			else if ( c == 'y' ) {
				// Two digit year...
				formatted_string.append(StringUtil.formatString(formatYear(d.getYear(),2,true),"%02d"));
			}
			else if ( c == 'Y' ) {
				formatted_string.append( StringUtil.formatString(d.getYear(),"%04d"));
			}
			else if ( c == 'Z' ) {
				formatted_string.append ( d.getTimeZoneAbbreviation() );
			}
			else if ( c == '%' ) {
				// Literal percent...
				formatted_string.append ( '%' );
			}
			else {	// Go ahead and add the % and the character
				// (e.g., so the format can be passed to a
				// secondary formatter).
				formatted_string.append ( '%' );
				formatted_string.append ( c );
			}
		}
		else {	// Just add the character to the string...
			formatted_string.append ( c );
		}
	}

	d = null;
	format = null;
	default_format = null;
	String s = formatted_string.toString();
	formatted_string = null;
	return s;
}

/**
Return the current date/time formatted using the default format.
@return The current date date/time as a string using the default format
(see the version that accepts a date and format).
*/
public static String formatTimeString ()
{	return formatTimeString ( new Date() );
}

/**
Return The current date/time formatted using the specified format string.
@return The current date/time formatted using the specified format string.
@param format The date format (see the version that accepts a date and format).
*/
public static String formatTimeString ( String format )
{	return formatTimeString ( new Date(), format );
}

/**
Return the date/time formatted using the default format string.
@return The date/time formatted using the default format string.
@param d0 The date to format (see the version that accepts a date and format).
*/
public static String formatTimeString ( Date d0 )
{	return formatTimeString ( d0, null );
}

/**
Format a Date using the given format.  See also formatDateTime().
@return The date/time formatted using the specified format.
@param d0 The date to format.  If the date is null, the current time is used.
@param format0 The date format.  If the format is null,
the default is as follows:  "Fri Jan 03 16:05:14 MST 1998" (the UNIX date
command output).  The date can be formatted using the format modifiers of the
C "strftime" routine, as follows:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Format Specifier</b></td>	<td><b>Description</b></td>
</tr

<tr>
<td><b>%a</b></td>
<td>The abbreviated weekday.</td>
</tr>

<tr>
<td><b>%A</b></td>
<td>The full weekday.</td>
</tr>

<tr>
<td><b>%b</b></td>
<td>The abbreviated month.</td>
</tr>

<tr>
<td><b>%B</b></td>
<td>The full month.</td>
</tr>

<tr>
<td><b>%c</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%d</b></td>
<td>Day of month in range 1-31.</td>
</tr>

<tr>
<td><b>%H</b></td>
<td>Hour of day in range 0-23.</td>
</tr>

<tr>
<td><b>%I</b></td>
<td>Hour of day in range 0-12.</td>
</tr>

<tr>
<td><b>%j</b></td>
<td>Day of year in range 1-366.</td>
</tr>

<tr>
<td><b>%m</b></td>
<td>Month of year in range 1-12.</td>
</tr>

<tr>
<td><b>%M</b></td>
<td>Minute of hour in range 0-59.</td>
</tr>

<tr>
<td><b>%p</b></td>
<td>"AM" or "PM".</td>
</tr>

<tr>
<td><b>%S</b></td>
<td>Seconds of minute in range 0-59.</td>
</tr>

<tr>
<td><b>%U</b> or <b>%W</b></td>
<td>Week of year in range 1-52.</td>
</tr>

<tr>
<td><b>%x</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%X</b></td>
<td>Not supported.</td>
</tr>

<tr>
<td><b>%y</b></td>
<td>Two digit year since 1900 (this is use discouraged because the datum is
ambiguous).</td>
</tr>

<tr>
<td><b>%Y</b></td>
<td>Four digit year.</td>
</tr>

<tr>
<td><b>%Z</b></td>
<td>Three character time zone abbreviation.</td>
</tr>

<tr>
<td><b>%%</b></td>
<td>Literal % character.</td>
</tr>

</table>
*/
public static String formatTimeString ( Date d0, String format0 )
{	String	routine = "TimeUtil.formatTimeString";	
	String	default_format = "%a %b %d %H:%M:%S %Z %Y";
	String	format;
	int	dl = 50;

	if ( format0 == null ) {
		format = new String ( default_format );
	}
	else if ( format0.length() == 0 ) {
		format = new String ( default_format );
	}
	else {	format = new String ( format0 );
	}

	Date d;
	if ( d0 == null ) {
		// Get the current time...
		d = new Date();
	}
	else {	d = d0;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Formatting \"" + d0 + "\" using \"" + format + "\"" );
	}

	if ( format.equals("datum_seconds") ) {
		/*
		** Want the number of seconds since the standard time datum	
		*/
		long seconds = d.getTime ();
		return Long.toString ( seconds/1000 );
	}
	else {	//
		// Convert format to string...
		//

		// First we get a Gregorian Calendar from the date...

		GregorianCalendar cal = new GregorianCalendar ();
		cal.setTime ( d );
		int		len = format.length();
		StringBuffer	formatted_string = new StringBuffer ();
		char		c = '\0';
		int		ifield;	// integer field value
		SimpleDateFormat sdf = new SimpleDateFormat();
		DateFormatSymbols dfs = sdf.getDateFormatSymbols();
		String[]	short_weekdays = dfs.getShortWeekdays();
		String[]	short_months = dfs.getShortMonths();
		String[]	months = dfs.getMonths();
		String[]	weekdays = dfs.getWeekdays();
		String[]	am_pm = dfs.getAmPmStrings();
		// The values returned are as follows:
		//
		//		Java		We use
		//
		// Year:	since 1900	4-digit
		// Month:	0 to 11		1 to 12
		// Day:		1 to 31		1 to 31
		// Hour:	0 to 59		same
		// Minute:	0 to 59		same
		// Second:	0 to 59		same
		// DayOfWeek:	0 to 7 with 0	same
		//		being Sunday
		//		in U.S.
		for ( int i = 0; i < len; i++ ) {
			c = format.charAt(i);
			if ( c == '%' ) {
				// We have a format character...
				++i;
				if ( i >= len ) {
					break;	// this will exit the whole
						// loop
				}
				c = format.charAt(i);
				if ( c == 'a' ) {
					// Abbreviated weekday name.
					ifield = cal.get(Calendar.DAY_OF_WEEK);
					formatted_string.append(
					short_weekdays[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Day of week =" +
						ifield + " \"" +
						short_weekdays[ifield] + "\"" );
					}
				}
				else if ( c == 'A' ) {
					// Full weekday name.
					ifield = cal.get(Calendar.DAY_OF_WEEK);
					formatted_string.append(
					weekdays[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Day of week =" +
						ifield + " \"" +
						weekdays[ifield] + "\"" );
					}
				}
				else if ( c == 'b' ) {
					// Abbreviated month name.
					ifield = cal.get(Calendar.MONTH);
					formatted_string.append(
					short_months[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Month=" +
						ifield + " \"" +
						short_months[ifield] + "\"" );
					}
				}
				else if ( c == 'B' ) {
					// Long month name.
					ifield = cal.get(Calendar.MONTH);
					formatted_string.append(
					months[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Month=" +
						ifield + " \"" +
						months[ifield] + "\"" );
					}
				}
				else if ( c == 'c' ) {
					formatted_string.append(
					"%c not supported" );
				}
				else if ( c == 'd' ) {
					// Day of month
					ifield = cal.get(Calendar.DAY_OF_MONTH);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Day =" + ifield );
					}
				}
				else if ( c == 'H' ) {
					// Hour of day...
					ifield = cal.get(Calendar.HOUR_OF_DAY);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Hour=" + ifield );
					}
				}
				else if ( c == 'I' ) {
					// Hour of day 1-12
					ifield = cal.get(Calendar.HOUR);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Hour =" + ifield );
					}
				}
				else if ( c == 'j' ) {
					// Day of year...
					ifield = cal.get(Calendar.DAY_OF_YEAR);
					formatted_string.append(
					StringUtil.formatString(ifield,"%03d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "DayofYear=" + ifield);
					}
				}
				else if ( c == 'm' ) {
					// Month of year...
					ifield = cal.get(Calendar.MONTH) + 1;
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Month =" + ifield );
					}
				}
				else if ( c == 'M' ) {
					// Minute of hour...
					ifield = cal.get(Calendar.MINUTE);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Minute =" + ifield );
					}
				}
				else if ( c == 'p' ) {
					// AM or PM...
					ifield = cal.get(Calendar.AM_PM);
					formatted_string.append(am_pm[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "AM/PM=" + ifield +
						" \"" + am_pm[ifield] + "\"" );
					}
				}
				else if ( c == 'S' ) {
					// Seconds of minute...
					ifield = cal.get(Calendar.SECOND);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Second =" + ifield );
					}
				}
				else if ( (c == 'U') || (c == 'W')) {
					// Week of year...
					// Don't worry now about whether Sunday
					// or Monday are the start of the week.
					ifield = cal.get(Calendar.WEEK_OF_YEAR);
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Weekofyear =" +
						ifield );
					}
				}
				else if ( c == 'x' ) {
					formatted_string.append(
					"%x not supported" );
				}
				else if ( c == 'X' ) {
					formatted_string.append(
					"%X not supported" );
				}
				else if ( c == 'y' ) {
					// Two digit year...
					ifield = cal.get(Calendar.YEAR);
					if ( ifield > 60000 ) {
						// Borland database bug...
						ifield -= 65536;
					}
					ifield = formatYear ( ifield, 2, true );
					formatted_string.append(
					StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "year =" +
						ifield );
					}
				}
				else if ( c == 'Y' ) {
					// Four digit year...
					ifield = cal.get(Calendar.YEAR);
					if ( ifield > 60000 ) {
						// Borland database bug...
						ifield -= 65536;
					}
					ifield = formatYear ( ifield, 4,
							true );
					formatted_string.append(
					StringUtil.formatString(ifield,"%04d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Year =" + ifield );
					}
				}
				else if ( c == 'Z' ) {
					// Time zone offset from GMT to local
					// time, in milliseconds...
					formatted_string.append (
					cal.getTimeZone().getID() );
				}
				else if ( c == '%' ) {
					// Literal percent...
					formatted_string.append ( '%' );
				}
			}
			else {	// Just add the character to the string...
				formatted_string.append ( c );
			}
		}

		d = null;
		routine = null;
		format = null;
		default_format = null;
		String s = formatted_string.toString();
		formatted_string = null;
		return s;
	}
}

/**
Convert between 2 and 4 digit year representations, assuming that a future year
is not allowed (this is mainly useful to convert a 4-digit year to 2-digit).
@param year The year to convert.
@param len The length of the output year (either 2 or 4).
@return The formatted 2 or 4 digit year.
*/
public static int formatYear ( int year, int len )
{	return formatYear ( year, len, false );
}

/**
Convert between 2 and 4 digit year representations.
@param year0 The year to convert.
@param len The length of the output year (either 2 or 4).
@param allow_future If false, indicates that the resulting 4-digit year cannot
be a future year, based on the system clock.
@return The formatted 2 or 4 digit year.  Return -1 if there is an error.
*/
public static int formatYear ( int year0, int len, boolean allow_future )
{	int	year;
	int	year_offset;

	// Initialize return value...

	year = year0;
	
	if ( len == 2 ) {
		if ( year0 < 100 ) {
			// OK as is...
			year = year0;
			return year;
		}
		else {	// Truncate the year to return only the last 2 digits...
			year = (year0 - ((year0/100)*100));
			return year;
		}
	}
	else if ( len == 4 ) {
		if ( year0 > 100 ) {
			// OK as is...
			year = year0;
			return year;
		}
		else {	// Get the year offset from the system (have to assume
			// this so old data may have problems!).
			year_offset = getYearOffset ();
			if ( year_offset < 0 ) {
				Message.printWarning ( 3, "TimeUtil.formatYear",
				"Unable to get system year offset" );
				return -1;
			}
			// Get the current system year...
			// This does not seem to work well - it converts to
			// Pacific time.
			//String message = "{0,date,yyyy}";
			//MessageFormat mf = new MessageFormat ( message );
			//Date now = new Date();
			//Object [] o = { now };
			int t_year = Integer.parseInt ( formatTimeString("%Y"));
			year = year0 + year_offset;
			if ( (year > t_year) && !allow_future ) {
				// Don't allow future years so subract 100.
				// This comes up, for example, if the input
				// is 70 and the current year is 2002.  In this
				// case, using the system offset would give a
				// year 2070, which is in the future.  Instead,
				// we actually want 1970.  There is no simple
				// way to deal with data that is older than
				// 100 years (the user would have to supply
				// some extra information and in that case this
				// routine is pretty worthless)!
				year -= 100;
			}
			return year;
		}
	}
	else {	// Unknown format request...
		Message.printWarning ( 3, "TimeUtil.formatYear",
		"Year ndigits " + len + " not 2 or 4!" );
		return -1;
	}
}

/**
Get the current day of the week as a number.
@return The current day of the week as a number, in the range 0-6, with 0 being
Sunday.  The system clock is used to get the current time.  Return -1 if an
error.
*/
public int getCurrentDayOfWeek ()
{	// First get the day of week as a string...
	String day = formatTimeString ( "%a" );
	// Now loop through and figure out the day...
	for ( int i = 0; i < 7; i++ ) {
		if ( day.equalsIgnoreCase(DAY_ABBREVIATIONS[i]) ) {
			// Have a match...
			return i;
		}
	}
	day = null;
	return -1;
}

/**
Return an array of valid format specifiers for the formatDateTime() method, in
the format %X - Description where X is the format specifier.  This is useful for
building graphical interfaces.
@return an array of format specifiers.
@param include_description If false, only the %X specifiers are returned.  if
True, the description is also returned.
*/
public static String[] getDateTimeFormatSpecifiers(boolean include_description )
{	String [] formats = new String[15];
	if ( include_description ) {
		formats[0] = "%a - Weekday, abbr";
		formats[1] = "%A - Weekday, full";
		formats[2] = "%b - Month, abbr";
		formats[3] = "%B - Month, full";
		//formats[3] = "%c - Not supported";
		formats[4] = "%d - Day, num";
		formats[5] = "%H - Hour (0-23)";
		formats[6] = "%I - Hour (1-12)";
		formats[7] = "%j - Day of year";
		formats[8] = "%m - Month, num";
		formats[9] = "%M - Minute";
		formats[10] = "%p - AM, PM";
		formats[11] = "%S - Second";
		//formats[12] = "%U, %W - not supported";
		//formats[12] = "%x - not supported";
		//formats[12] = "%X - not supported";
		formats[12] = "%y - Year, yy";
		formats[13] = "%Y - Year, yyyy";
		formats[14] = "%Z - Time zone";
	}
	else {	formats[0] = "%a";
		formats[1] = "%A";
		formats[2] = "%b";
		formats[3] = "%B";
		//formats[3] = "%c";
		formats[4] = "%d";
		formats[5] = "%H";
		formats[6] = "%I";
		formats[7] = "%j";
		formats[8] = "%m";
		formats[9] = "%M";
		formats[10] = "%p";
		formats[11] = "%S";
		//formats[12] = "%U, %W - not supported";
		//formats[12] = "%x - not supported";
		//formats[12] = "%X - not supported";
		formats[12] = "%y";
		formats[13] = "%Y";
		formats[14] = "%Z";
	}
	return formats;
}

/**
Return the local time zone abbreviation.
The method of lookup is either LOOKUP_TIME_ZONE_ONCE (default) or
LOOKUP_TIME_ZONE_ALWAYS.  The method can be changed by calling the overloaded
method.
@return the local time zone abbreviation.
*/
public static String getLocalTimeZoneAbbr ()
{	return getLocalTimeZoneAbbr ( _time_zone_lookup_method );
}

/**
Return the local time zone abbreviation.
@param time_zone_lookup_method If LOOKUP_TIME_ZONE_ONCE or
LOOKUP_TIME_ZONE_ALWAYS, this method will control how time zones are determined
for future time zone lookups, including those used in DateTime construction.
The default is LOOKUP_TIME_ZONE_ONCE.
@return the local time zone abbreviation.
*/
public static String getLocalTimeZoneAbbr ( int time_zone_lookup_method )
{	if (	(time_zone_lookup_method == LOOKUP_TIME_ZONE_ONCE) ||
		(time_zone_lookup_method == LOOKUP_TIME_ZONE_ALWAYS) ) {
		_time_zone_lookup_method = time_zone_lookup_method;
	}
	if ( _time_zone_lookup_method == LOOKUP_TIME_ZONE_ONCE ) {
		if ( !_local_time_zone_retrieved ) {
			_local_time_zone = TimeZone.getDefault();
			String date_as_string = new Date().toString();
			_local_time_zone_string =
				date_as_string.substring(
				(date_as_string.length() - 8),
				(date_as_string.length() - 5) );
/*
			_local_time_zone_string =
				_local_time_zone.getDisplayName(
				_local_time_zone.useDaylightTime(),
				TimeZone.SHORT);
*/
			date_as_string = null;
			_local_time_zone_retrieved = true;
		}
		else {	return _local_time_zone_string;
		}
	}
	else if ( _time_zone_lookup_method == LOOKUP_TIME_ZONE_ALWAYS ) {
		// Look up each time...
		_local_time_zone = TimeZone.getDefault();
		String date_as_string = new Date().toString();
		_local_time_zone_string =
			date_as_string.substring(
			(date_as_string.length() - 8),
			(date_as_string.length() - 5) );
/*
		_local_time_zone_string =
			_local_time_zone.getDisplayName(
			_local_time_zone.useDaylightTime(),
			TimeZone.SHORT);
*/
		date_as_string = null;
		_local_time_zone_retrieved = true;
	}
	return _local_time_zone_string;
}

/**
Get the month and day from the day of year.
@return A array of two integers containing the month and day given the
year and Julian day within the year, or null if there is a problem.
@param year Four digit year (used to determine if leap year).  If the leap
year is not important, use a non-leap year (e.g., 1997).
@param julian_day Julian day in a year where 1 = Jan 1.
*/
public static int [] getMonthAndDayFromDayOfYear ( int year, int julian_day )
{	int [] month_day = new int[2];

	try {
	boolean isleap = isLeapYear ( year );
	if ( julian_day > 366 ) {
		month_day[0] = 12;
		month_day[0] = 31;
	}
	else {	// Loop forwards subtracting the days for each month intil we
		// have only one complete or partial month.
		int	daysleft = julian_day;
		int	monthdays;
		for ( int i = 0; i < 12; i++ ) {
			if ( (i == 1) && isleap ) {
				// Set the offset to 1 to account for extra
				// days in febrary...
				monthdays = MONTH_DAYS[i] + 1;
			}
			else {	// All other months...
				monthdays = MONTH_DAYS[i];
			}
			if ( daysleft <= monthdays ) {
				// We have found our month...
				month_day[0] = i + 1;
				month_day[1] = daysleft;
				break;
			}
			daysleft -= monthdays;
		}
	}
	return month_day;
	}
	catch ( Exception e ) {
		Message.printWarning ( 3,"TimeUtil.getMonthAndDayFromDayOfYear",
		"Error getting month and day from year " + year +
		" Julian day " + julian_day );
		return null;
	}
}

/**
Return the number of intervals between two dates.
This uses a loop to count
the intervals and may be slow.  For time series, a faster method may be to call
the TSUtil.getDataSize method.  Zero is returned if the start date is after the
end date.
@return The number of intervals between two dates.
@param t1 Start date.
@param t2 End date.
@param base The time series base interval.
@param mult The time series interval multiplier.
*/
public static int getNumIntervals (	DateTime t1, DateTime t2, int base,
					int mult )
{	int	intervals=0;

	if( t2.lessThan(t1) ) {
		Message.printWarning( 3, "TimeUtil.getNumIntervals", 
		"End " + t2 + " is before start " + t1 + ".  Returning 0." );
		return 0;
	}

	// We want to remain less than t2, so if the two dates are the 
	// same we will return 0.

	DateTime t = new DateTime(t1);
	for (	; t.lessThan(t2); t.addInterval( base, mult ) ) {
		intervals++;	
	}
	t = null;

	return intervals;
}

/**
Return the current system time using the default format.
@return The current system time as a string, using the default format used by
formatDateTime.
REVISIT JAVADOC: see RTi.Util.Time.TimeUtil.formatDateTime
*/
public static String getSystemTimeString ( )
{	return formatDateTime ( null, null );
}

/**
Return the current system time using the specified format.
@return The current system time as a string, using the specified format, as
used by formatDateTime.
@param format Format for date (see formatDateTime).
*/
public static String getSystemTimeString ( String format )
{	return formatDateTime ( null, format );
}

// jan1_1800_days pre-computed by looping from 1-1799 adding numDaysInYear.
// Code taken from HMGetDateFromInternalJulianDay1900.  The logic is pretty
// confusing but is necessary because of the leap year checks.
/**
Convert an absolute day into its components.  This routine does not at this
time handle hours or time zone.
@return The year, month, and day in an int[3] array.
@param aday Absolute day with respect to Dec 31, 1799.  The datum may change
in the future and should be used only in a dynamic fashion.
@see #absoluteDay
*/
public static int[] getYearMonthDayFromAbsoluteDay ( int aday )
{	String	routine = "TimeUtil.getYearMonthDayFromAbsoluteDay";
	int	bflag,
		day,
		dl = 10,
		i, id1,
		jd = aday,	// So we don't have to change original logic...
		leap,
		month,
		year;

	// First guess at the year.  146097 is the number of days in 400 years,
	// accounting for leap years.  Take an initial guess at the year...

	year =	(jd*400)/146097 + 1800;

	while ( true ) {
		leap = 1;
		if ( (year%4) != 0 ) {
			leap = 0;
		}
		if ( ((year%100) == 0) && ((year%400) != 0) ) {
			leap = 0;
		}
		id1 =	365*(year) +
			year/4 -
			year/100 +
			year/400 -
			ABSOLUTE_DAY_DATUM -
			leap;
		if ( id1 < jd ) {
			break;
		}
		--year;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"After first pass, found year to be: " + year );
	}

	day	= jd - id1;
	while ( true ) {
		leap = 1;
		if ( (year%4) != 0 ) {
			leap = 0;
		}
		if ( ((year%100) == 0) && ((year%400) != 0) ) {
			leap = 0;
		}
		if ( day <= (365 + leap) ) {
			break;
		}
		++year;
		day = day - 365 - leap;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"After second pass, found year to be: " + year );
	}

	// Find month...

	month = 0;
	if ( day <= 31 ) {
		month = 1;
	}
	if ( month <= 0 ) {
		bflag = 0;
		for ( i = 3; i <= 12; i++ ) {
			month = i - 1;
			if ( day <= (MONTH_YEARDAYS[i - 1] + leap) ) {
				bflag = 1;
				break;
			}
		}
		if ( bflag == 0 ) {
			month = 12;
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"Found month to be: " + month );
	}

	//  Month known, compute day offset from month

	day -= MONTH_YEARDAYS[month - 1];
	if ( month >= 3 ) {
		day -= leap;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"Found day to be: " + day );
	}

	int [] date_info = new int[3];
	date_info[0] = year;
	date_info[1] = month;
	date_info[2] = day;
	routine = null;
	return date_info;
}

private static int _offset_year = -10000;

/**
@return The year offset for a 4-digit year (e.g., 1900 for 1981).  If a
two-digit year is passed in, the offset is determined using the current
system clock.
*/
public static int getYearOffset ( )
{	if ( _offset_year == -10000 ) {
		// This routine really only needs to be called once per run!
		// We are now using the C version...
		//string = getSystemTimeString ( "yyyy" );
		String string = getSystemTimeString ( "%Y" );
			//HMPrintWarning ( 2, routine,
			//"Trouble getting year offset" );
			//return HMSTATUS_FAILURE;
		int year = Integer.parseInt ( string );
		string = null;
		year = year - (year/100)*100;
		_offset_year = year;
	}
	return _offset_year;
}

/**
Determine the highest precision (smallest interval) for two DateTime instances.
@return the highest precision (smallest interval) for two DateTime instances
(see TimeInterval precision values).
@param datetime1 A DateTime instance to compare.
@param datetime2 Another DateTime instance to compare.
@exception Exception if either of the dates are null or have an imprecise
precsision (UNKNOWN or IRREGULAR).
*/
public static int highestPrecision ( DateTime datetime1, DateTime datetime2 )
throws Exception
{	if ( datetime1 == null ) {
		throw new Exception ( "First DateTime is null" );
	}
	if ( datetime2 == null ) {
		throw new Exception ( "Second DateTime is null" );
	}
	int precision1 = datetime1.getPrecision();
	if (	(precision1 < TimeInterval.HSECOND) ||
		(precision1 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for first DateTime is not " +
		"between hsecond and year." );
	}
	int precision2 = datetime2.getPrecision();
	if (	(precision2 < TimeInterval.HSECOND) ||
		(precision2 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for second DateTime is not " +
		"between hsecond and year." );
	}
	if ( precision1 < precision2 ) {
		return precision1;
	}
	return precision2;
}

// REVISIT SAM 2005-11-16
// Should this be deprecated in favor of the convert*Month methods?
/**
Get the irrigation month given a calendar month.  The irrigation year starts in
November of the previous calendar year and ends in October of the irrigation
year.
@return the irrigation month.
*/
public static int irrigationMonthFromCalendar ( int month )
{	if ( month >= 11 ) {
		return (month - 10);
	}
	else {	return (month + 2);
	}
}

// REVISIT SAM 2005-11-16
// Should this be deprecated in favor of the convert*Month methods?
/**
Get the irrigation year given a calendar month and year.  The irrigation year
starts in November of the previous calendar year and ends in October of the
irrigation year.
@return the irrigation year.
*/
public static int irrigationYearFromCalendar ( int month, int year )
{	if ( month >= 11 ) {
		return (year - 1);
	}
	else {	return year;
	}
}

/**
Return true if the string is a date/time, false otherwise.  The format must also
be supplied.  The DateTime class is used to parse the date and therefore only
date/time formats recognized by DateTime are recognized.
@return true if the string is a date (can be parsed).
@param date_string Date string to parse.
@param format Format to use for parsing (see FORMAT_*).
*/
public static boolean isDateTime ( String date_string, int format )
{	try {	DateTime.parse ( date_string, format );
		return true;
	} catch (Exception e) { 
		return false;
	}
}

/**
Return true if the string is a date, false otherwise.  The format is determined
from the string.  The DateTime class is used to parse the date and therefore
only date/time formats recognized by DateTime are recognized.
@param date_string Date string to parse.
@return true if the string is a date (can be parsed).
*/
public static boolean isDateTime ( String date_string )
{	try {	DateTime.parse ( date_string );
		return true;
	} catch ( Exception e ) { 
		return false;
	}
}

/**
Determine whether a year is a leap year.
Leap years occur on years evenly divisible by four.
However, years evenly divisible by 100 are not leap
years unless they are also evenly divisible by 400.
@return true if the specified year is a leap year and false if not.
@param year 4-digit year to check.
*/
public static boolean isLeapYear ( int year )
{	if (	(((year%4) == 0) && ((year%100) != 0)) ||
		(((year%100) == 0) && ((year%400) == 0)) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether a day is valid.
@return true if the day is valid (in the range 1-31), false if not.
@param day Day to check.
*/
public static boolean isValidDay ( int day )
{	if ( (day >= 0) && (day <= 31) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether the day is valid.
@return true if the day is valid (in the range 1-31), false if not.
@param day Day to check (an integer as a String).
*/
public static boolean isValidDay ( String day )
{	try {	int iday = Integer.parseInt ( day );
		return isValidDay ( iday );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether the day is valid.
@return true if the day is valid (in the range 1-31), false if not.
@param day Day to check.
@param month Month corresponding to day to check.
@param year Year corresponding to day to check.
*/
public static boolean isValidDay ( int day, int month, int year )
{	// First check month and year...

	if ( !isValidMonth(month) ) {
		return false;
	}
	if ( !isValidYear(year) ) {
		return false;
	}

	// Now check day, accounting for leap years...

	int daysinmonth = MONTH_DAYS[month - 1];
	if ( (month == 2) && isLeapYear(year) ) {
		++daysinmonth;
	}
	if ( (day > 0) && (day <= daysinmonth) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether a day is valid.
@return true if the day is valid (in the range 1-31), false if not.
@param day Day to check, an integer as a String.
@param month Month corresponding to day to check, an integer as a String.
@param year Year corresponding to day to check, an integer as a String.
*/
public static boolean isValidDay ( String day, String month, String year )
{	int iday, imonth, iyear;

	try {	iday = Integer.parseInt ( day );
	}
	catch ( Exception e ) {
		return false;
	}
	try {	imonth = Integer.parseInt ( month );
	}
	catch ( Exception e ) {
		return false;
	}
	try {	iyear = Integer.parseInt ( year );
	}
	catch ( Exception e ) {
		return false;
	}
	return isValidDay ( iday, imonth, iyear );
}

/**
Determine whether an hour is valid.
@return true if the hour is valid (in the range 0-23), false if not.
@param hour Hour to check.
*/
public static boolean isValidHour ( int hour )
{	if ( (hour >= 0) && (hour < 24) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether an hour is valid.
@return true if the hour is valid (in the range 0-23), false if not.
@param hour Hour to check, an integer as a String.
*/
public static boolean isValidHour ( String hour )
{	try {	int ihour = Integer.parseInt ( hour );
		return isValidHour ( ihour );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a minute is valid.
@return true if the minute is valid (in the range 0-59), false if not.
@param minute Minute to check.
*/
public static boolean isValidMinute ( int minute )
{	if ( (minute >= 0) && (minute <= 59) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether a minute is valid.
@return true if the minute is valid (in the range 0-59), false if not.
@param minute Minute to check, an integer as a String.
*/
public static boolean isValidMinute ( String minute )
{	try {	int iminute = Integer.parseInt ( minute );
		return isValidMinute ( iminute );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a month is valid.
@return true if the month is valid (in the range 1-12), false if not.
@param month Month to check.
*/
public static boolean isValidMonth ( int month )
{	if ( (month > 0) && (month < 13) ) {
		return true;
	}
	else {	return false;
	}
}

/**
Determine whether a month is valid.
@return true if the month is valid (in the range 1-12), false if not.
@param month Month to check, an integer as a String.
*/
public static boolean isValidMonth ( String month )
{	try {	int imonth = Integer.parseInt ( month );
		return isValidMonth ( imonth );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a year is valid.
@return true if the year is valid (in the range 1-12), false if not.
@param year Year to check.
*/
public static boolean isValidYear ( int year )
{	if ( year < 100 ) {
		return false;
	}
	else {	return true;
	}
}

/**
Determine whether a year is valid.
@return true if the year is valid (in the range 1-12), false if not.
@param year Year to check, an integer as a String.
*/
public static boolean isValidYear ( String year )
{	try {	int iyear = Integer.parseInt ( year );
		return isValidYear ( iyear );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine the lowest precision (largest interval) for two DateTime instances.
@return the lowest precision (largest interval) for two DateTime instances
(see TimeInterval precision values).
@param datetime1 A DateTime instance to compare.
@param datetime2 Another DateTime instance to compare.
@exception Exception if either of the dates are null or have an imprecise
precsision (UNKNOWN or IRREGULAR).
*/
public static int lowestPrecision ( DateTime datetime1, DateTime datetime2 )
throws Exception
{	if ( datetime1 == null ) {
		throw new Exception ( "First DateTime is null" );
	}
	if ( datetime2 == null ) {
		throw new Exception ( "Second DateTime is null" );
	}
	int precision1 = datetime1.getPrecision();
	if (	(precision1 < TimeInterval.HSECOND) ||
		(precision1 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for first DateTime is not " +
		"between hsecond and year." );
	}
	int precision2 = datetime2.getPrecision();
	if (	(precision2 < TimeInterval.HSECOND) ||
		(precision2 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for second DateTime is not " +
		"between hsecond and year." );
	}
	if ( precision1 > precision2 ) {
		return precision1;
	}
	return precision2;
}

/**
Return the maximum of two DateTime instances.  If one is null, return the
non-null value.  If both are null, return null.  The comparison is made as
follows:
<pre>
if ( dt1.greaterThan(dt2) ) {
	return dt1;
}
</pre>
@param dt1 First DateTime to compare.
@param dt2 Second DateTime to compare.
@return the maximum of two DateTime instances.
*/
public static DateTime max ( DateTime dt1, DateTime dt2 )
{	if ( (dt1 == null) && (dt2 == null) ) {
		return null;
	}
	else if ( dt1 == null ) {
		return dt2;
	}
	else if ( dt2 == null ) {
		return dt1;
	}
	else if ( dt1.greaterThan(dt2) ) {
		return dt1;
	}
	else {	return dt2;
	}
}

/**
Return the minimum of two DateTime instances.  If one is null, return the
non-null value.  If both are null, return null.  The comparison is made as
follows:
<pre>
if ( dt1.lessThan(dt2) ) {
	return dt1;
}
</pre>
@param dt1 First DateTime to compare.
@param dt2 Second DateTime to compare.
@return the minimum of two DateTime instances.
*/
public static DateTime min ( DateTime dt1, DateTime dt2 )
{	if ( (dt1 == null) && (dt2 == null) ) {
		return null;
	}
	else if ( dt1 == null ) {
		return dt2;
	}
	else if ( dt2 == null ) {
		return dt1;
	}
	else if ( dt1.lessThan(dt2) ) {
		return dt1;
	}
	else {	return dt2;
	}
}

/**
Return a string abbreviation for the month (e.g., "Jan").
@return A string abbreviation for the month, or "" if not a valid month.
@param month Month number, in range 1-12.
*/
public static String monthAbbreviation ( int month )
{	if ( (month < 1) || ( month > 12) ) {
		return "";
	}
	else {	return MONTH_ABBREVIATIONS[month - 1];
	}
}

/**
Convert an absolute month to its year and month values.
@return An array if int's indicating the month and year corresponding to the
given absolute month.  The first value will be the year, the second will be the
month.
@param amon Absolute month (year*12 + month).
*/
public static int[] monthFromAbsolute ( int amon )
{	int	month, year;
	int[]	monthyear = new int[2];

	monthyear[0] = 0;
	monthyear[1] = 0;

	if ( amon < 0 ) {
		return monthyear;
	}
	month = amon%12;
	year = amon/12;
	if ( month == 0 ) {
		month = 12;
		--year;
	}
	monthyear[0] = month;
	monthyear[1] = year;
	return monthyear;
}

/**
Return the number of days in a month, checking for leap year for February.
@return The number of days in a month, or zero if an error.
@param dt The DateTime object to examine.
*/
public static int numDaysInMonth ( DateTime dt )
{	return numDaysInMonth ( dt.getMonth(), dt.getYear() );
}

/**
Return the number of days in a month, checking for leap year for February.
@return The number of days in a month, or zero if an error.
@param month The month of interest (1-12).
@param year The year of interest.
*/
public static int numDaysInMonth ( int month, int year )
{	int	ndays;

	if ( month < 1 ) {
		// Assume that something is messed up...
		ndays = 0;
	}
	else if ( month > 12 ) {
		// Project out into the future...
		return numDaysInMonth ( (month%12), (year + month/12) );
	}
	else {	// Usual case...
		ndays = MONTH_DAYS[month - 1];
		if ( (month == 2) && isLeapYear(year) ) {
			++ndays;
		}
	}
	return ndays;
}

/**
Calculate the number of days in several months.
@return The number of days in several months.
@param month0 The initial month of interest (1-12).
@param year0 The initial year of interest (4-digit).
@param n The number of months, inclusive of the initial month.  For example, a
value of 1 would return the days in the initial month.  A value of 2 would
return the number of days in the initial month and its following month.
*/
static public int numDaysInMonths ( int month0, int year0, int n )
{	int	i, month, ndays = 0, year;

	month	= month0;
	year	= year0;
	for ( i = 0; i < n; i++ ) {
		ndays += numDaysInMonth ( month, year );
		++month;
		if ( month == 13 ) {
			month = 1;
			++year;
		}
	}
	return ndays;
}

/**
Calculate the number of days in several months.
@return The number of days in several months.
@param month0 The initial month of interest.
@param year0 The initial year of interest.
@param month1 The last month of interest.
@param year1 The last year of interest.
*/
static public int numDaysInMonths (	int month0, int year0, int month1,
					int year1 )
{	int nmonths = absoluteMonth ( month1, year1 ) -
			absoluteMonth ( month0, year0 ) + 1;
	return numDaysInMonths ( month0, year0, nmonths );
}

/**
Determine the number of days in a year.
@return The number of days in a year, accounting for leap years.
@param year The year of interest.
*/
public static int numDaysInYear ( int year )
{	if ( isLeapYear(year) ) {
		return 366;
	}
	else {	return 365;
	}
}

/**
Determine the integer month given the month abbreviation.
@return An integer in the range 1-12 corresponding to the month abbreviation,
or 0 if the abbreviation cannot be matched.
@param abbrev Month abbreviation (currently limited to 3-letter abbreviations
in the MONTH_ABBREVIATIONS array).
*/
public static int monthFromAbbrev( String abbrev )
{	for ( int i = 0; i < 12; i++ ) {
		if(abbrev.equalsIgnoreCase( MONTH_ABBREVIATIONS[i] ) ) {
			return i + 1;
		}
	}
	return 0;
}

/**
Parse a 4-digit military time into its hour and minute and return in an array
of int's.
@return An array of int's with the hour (index 0) and minute (index 1).
@param time 4-digit military time.
*/
public static int [] parseMilitaryTime ( int time )
{	int [] hour_min = new int[2];
	hour_min[0] = time/100;			// the hour
	hour_min[1] = time - hour_min[0]*100;	// the minutes
	return hour_min;
}

/**
Sets the local time zone according to the computer.  Note that in some cases
the computer time zone may not be recognized (e.g., rather than something like
"MST", a long time zone will be used).  Care should be taken when using the
time zone from the computer.
@exception Exception if the time zone is not recognized by the TimeUtil package.
*/
public static void setLocalTimeZone()
throws Exception
{	TimeZone zone = TimeZone.getDefault();
	setLocalTimeZone( zone.getID() );
	zone = null;
}

/**
Sets the local time zone abbreviation according to the String.
@param s local time zone.
@exception if the local time zone is not recognized.
*/
public static void setLocalTimeZone( String s )
throws Exception
{	if ( s == null ) {
		throw new Exception ( "Cannot set local time zone to null." );
	}
	// Try this to see if the zone is recognized.  If not, an exception
	// will be thrown.
	TZ.getDefinedTZ(s);
	// Old convention (deprecated)...
	localTimeZone = s;
	// Also set the new time zone...
	_local_time_zone_string = s;
	_local_time_zone_retrieved = true;
}

/**
Sleep the given number of milliseconds.  This code just loops and checks the
system clock during each loop.  The thread will be tied up during the sleep.
@param milliseconds The number of milliseconds to sleep.
*/
public static void sleep ( long milliseconds )
{	// Get the current date...
	Date now = new Date ();

	// Loop until we have slept long enough...

	Date newnow;
	for ( ;; ) {
		// Get the new date...
		newnow = new Date ();
		if ( (newnow.getTime() - now.getTime()) > milliseconds ) {
			break;
		} 
	}
	now = null;
	newnow = null;
}

/**
Wait for a file to exist before continuing.
@param filename Name of file to check for.
@param wait Number of milliseconds to wait for each time.
@param numtries Number of times to wait.
@return true if the file exists, false if the timeout period is exceeded without
the file being detected.
*/
public static boolean waitForFile ( String filename, int wait, int numtries )
{	for ( int i = 0; i < numtries; i++ ) {
		if ( IOUtil.fileExists(filename) ) {
			Message.printStatus ( 1, "",
			"File \"" + filename + "\" DOES exist at " +
			formatTimeString() );
			return true;
		}
		else {	Message.printStatus ( 1, "",
			"File \"" + filename + "\" DOES NOT exist at " +
			formatTimeString() );
			sleep ( wait );
		}
	}
	return false;
}

// REVISIT SAM 2005-11-16
// Should this be deprecated in favor of the convert*Month methods?
/**
Get the water year month given a calendar month.  The water year starts in
October of the previous calendar year and ends in September of the irrigation
year.
@return the water year month.
*/
public static int waterMonthFromCalendar ( int month )
{	if ( month >= 10 ) {
		return (month - 9);
	}
	else {	return (month + 3);
	}
}

// REVISIT SAM 2005-11-16
// Should this be deprecated in favor of the convert*Month methods?
/**
Get the water year given a calendar month and year.  The water year
starts in October of the previous calendar year and ends in September of the
irrigation year.
@return the water year.
*/
public static int waterYearFromCalendar ( int month, int year )
{	if ( month >= 10 ) {
		return (year - 1);
	}
	else {	return year;
	}
}

} // End TimeUtil
