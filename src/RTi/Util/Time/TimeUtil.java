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
Blank values for DateTime parts, used to "mask out" unused information.
If these are used as data values, then DateTime.DATE_FAST should be used to
prevent exceptions for invalid values.  For example, it may be necessary to show a DateTime
as a string parameter to represent a window in a year ("MM-DD").  In this case the other
date/time components are not used, but are needed in the string to allow for proper parsing.
*/
public final static int BLANK_YEAR = 9999;
public final static int BLANK_MONTH = 99;
public final static int BLANK_DAY = 99;
public final static int BLANK_HOUR = 99;
public final static int BLANK_MINUTE = 99;
public final static int BLANK_SECOND = 99;

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
running a real-time application that runs continuously between time zone changes.
*/
public static final int LOOKUP_TIME_ZONE_ALWAYS	= 2;

/**
Local Time Zone String (e.g, "MST", "MDT", "GMT"), based on the last call to getLocalTimeZone().
@deprecated Use the getLocalTimeZone() or getLocalTimeZoneAbbr() methods or
use the protected _local_time_zone_string if in this package.
*/
public static String localTimeZone = "";

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
public static final int MONTH_DAYS[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

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
Compute the absolute day.  This can be used for determining the difference between dates.
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
		day // day of month
		+ numDaysInMonths (1, year, (month-1)) // days in previous months
		+ 365*year // days in previous years
		+ year/4 // 1 if leap year
		- year/100 // -3 every 400 years
		+ year/400 // 1 every 400 years
		- ABSOLUTE_DAY_DATUM // Jan 1, 1800
		- leap; // cancel /4 term - will be added by numDaysInMonths
	return aday;
}

/**
Compute the absolute minute.  This can be used for determining the difference between dates.
@return The absolute minute, with respect to Dec 31, 1799.  The datum may change
in the future and should be used only in a dynamic fashion.
@param year Year (4-digit).
@param month Month number (1-12).
@param day Day number (1-31).
@param hour Hour (0-23).
@param minute Minute (0-59).
*/
public static long absoluteMinute (	int year, int month, int day, int hour, int minute )
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
public static DateTime addIntervals ( DateTime t1, int base, int mult, int nintervals )
{	DateTime t = new DateTime ( t1 );
	for ( int count = 0; count < nintervals; count ++ ){
		t.addInterval( base, mult );
	}
	return t;
}

/**
Indicate whether a DateTime's precision matches the specified TimeInterval string.
This is useful, for example, in confirming that DateTime's for a time series are
consistent with the time series data interval.  This only checks the precision of the base
but does not check the multiplier.
@param dt DateTime to check.
@param intervalString TimeInterval string to check.
@return 0 if the precision value of the DateTime is the same as the interval (or interval is irregular),
-1 if the precision value of the DateTime is less than the interval (date/time is more precise),
1 if the precision value of the DateTime is greater than the interval (date/time is less precise),
and null if the input is invalid.
*/
public static Integer compareDateTimePrecisionToTimeInterval ( DateTime dt, String intervalString )
{
    TimeInterval ti = null;
    try {
        ti = TimeInterval.parseInterval(intervalString);
    }
    catch ( Exception e ) {
        return null;
    }
    return compareDateTimePrecisionToTimeInterval(dt,ti);
}

/**
Indicate whether a DateTime's precision matches the specified TimeInterval string.
This is useful, for example, in confirming that DateTime's for a time series are
consistent with the time series data interval.  This only checks the precision of the base
but does not check the multiplier.
@param dt DateTime to check.
@param interval interval to check.
@return 0 if the precision value of the DateTime is the same as the interval (or interval is irregular),
-1 if the precision value of the DateTime is less than the interval (date/time is more precise),
1 if the precision value of the DateTime is greater than the interval (date/time is less precise),
and null if the input is invalid.
*/
public static Integer compareDateTimePrecisionToTimeInterval ( DateTime dt, TimeInterval interval )
{
    int precision = dt.getPrecision();
    int intervalBase = interval.getBase();
    if ( intervalBase == TimeInterval.IRREGULAR ) {
        return 0;
    }
    else if ( precision < intervalBase ) {
        return -1;
    }
    else if ( precision > intervalBase ) {
        return 1;
    }
    else {
        return 0;
    }
}

/**
Indicate whether a DateTime's precision matches the specified TimeInterval,
including base and multiplier.
This is useful, for example, in confirming that DateTime's for a time series are
consistent with the time series data interval.  Optionally, allow the precisions to
be different when the DateTime is more precise but "remainder" date/time parts are
zero.  This may be the case when a date/time is read from a source that always provides
more precision in the data (e.g., databases).
@param dt DateTime to check.
@param interval TimeInterval to check.
@param checkParts if true, then a date/time that is more precise than the interval is
allowed as long as the higher precision values are zeros.
@return 0 if the precision value of the DateTime is the same as the interval (or interval is irregular),
-1 if the precision value of the DateTime is less than the interval (date/time is more precise),
1 if the precision value of the DateTime is greater than the interval (date/time is less precise),
and null if the input is invalid.
*/
public static Integer compareDateTimePrecisionToTimeInterval ( DateTime dt, TimeInterval interval, boolean checkParts )
{
    int intervalBase = interval.getBase();
    int intervalMult = interval.getMultiplier();
    Integer compare = compareDateTimePrecisionToTimeInterval(dt, interval);
    if ( (compare == null) || (compare == 1) || (intervalBase == TimeInterval.IRREGULAR) ) {
        // Cases that are independent of checkParts value
        return compare;
    }
    if ( !checkParts ) {
        // No need to check parts
        return compare;
    }
    else {
        // Precision of date/time is >= than specified interval but may be OK if:
        //   1) the remaining parts are DateTime initial values (0 or 1, depending on part)
        //   2) the date/time part at the same precision must evenly divide into the interval
        //      (e.g., 15Min data will allow minute values only 0, 15, 30, 45)
        // If the extra information is OK, return 0
        // First check to see if any date/time information in the remainder is not the initial values
        // Numerical value of the interval is bigger for larger intervals (e.g., YEAR > MONTH)
        int dtMorePrecise = -1;
        if ( compare == dtMorePrecise ) {
            // Date/time precision is greater than the interval so need to check if the end parts 
            if ( intervalBase >= TimeInterval.MINUTE ) {
                if ( dt.getSecond() != 0 ) {
                    return dtMorePrecise;
                }
            }
            if ( intervalBase >= TimeInterval.HOUR ) {
                if ( dt.getMinute() != 0 ) {
                    return dtMorePrecise;
                }
            }
            if ( intervalBase >= TimeInterval.DAY ) {
                if ( dt.getHour() != 0 ) {
                    return dtMorePrecise;
                }
            }
            if ( intervalBase >= TimeInterval.MONTH ) {
                if ( dt.getDay() > 1 ) {
                    return dtMorePrecise;
                }
            }
            if ( intervalBase >= TimeInterval.YEAR ) {
                if ( dt.getMonth() > 1 ) {
                    return dtMorePrecise;
                }
            }
        }
        // Now check the multiplier on the date/time part of the interval base...
        if ( intervalBase == TimeInterval.MINUTE ) {
            if ( (dt.getMinute() % intervalMult) != 0 ) {
                return dtMorePrecise;
            }
        }
        else if ( intervalBase == TimeInterval.HOUR ) {
            if ( (dt.getHour() % intervalMult) != 0 ) {
                return dtMorePrecise;
            }
        }
        else if ( intervalBase == TimeInterval.DAY ) {
            if ( (dt.getDay() % intervalMult) != 0 ) {
                return dtMorePrecise;
            }
        }
        else if ( intervalBase == TimeInterval.MONTH ) {
            if ( dt.getMonth() != 1 ) {
                return dtMorePrecise;
            }
        }
        else if ( intervalBase == TimeInterval.YEAR ) {
            if ( dt.getYear() != 1 ) {
                return dtMorePrecise;
            }
        }
        // More detailed parts do not introduce any additional data
        return 0;
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
month in the special calendar.  For example, for water years, specify 10 for October.
@return the month number in the custom calendar (1 to 12).
*/
public static int convertCalendarMonthToCustomMonth ( int cal_month, int first_cal_month_in_year )
{	if ( cal_month >= first_cal_month_in_year ) {
		// This is the only clause that is processed for calendar year
		// and is used for high calendar months in custom calendars.
		// For example for water year (first_cal_month_in_year = 10), return cal_month - 3
		return (cal_month - (first_cal_month_in_year - 1) );
	}
	else {
		// This will be processed for non-calendar year for early months in the calendar year.
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
@param d Datetime to evaluate.
*/
public static int dayOfYear ( DateTime d )
{	if ( d == null ) {
		return -1;
	}
	int days = 0;
	// Days in previous months...
	if ( d.getMonth() > 1 ) {
		days += numDaysInMonths ( 1, d.getYear(), (d.getMonth() - 1) );
	}
	// Add the days from this month...
	days += d.getDay();
	return days;
}

/**
Return the day of the year, where day 1 is the first day in the year type (Jan 1 for calendar year type).
For example, for calendar year day 1 is Jan 1 and for water year day 1 is Oct 1.
@return The day of the year (where 1 is the first day of the year and 365 or 366 is the last).
@param d Datetime to evaluate, in calendar year.
@param yearType year type.
@exception IllegalArgumentException if there is an error.
*/
public static int dayOfYear ( DateTime d, YearType yearType )
{   if ( (yearType == YearType.CALENDAR) || (yearType == null) ) {
        return dayOfYear ( d );
    }
    // Else have non-calendar year so evaluation is a bit more complex
    if ( d == null ) {
        throw new IllegalArgumentException ( "Date is null - cannot calculate day of year.");
    }
    int days = 0;
    // Else have non-calendar year so evaluation is a bit more complex
    int calMonth = d.getMonth();
    int startMonth = yearType.getStartMonth();
    if ( calMonth < startMonth ) {
        // First add the number of days in the months in the previous year
        days += numDaysInMonths ( startMonth, d.getYear() + yearType.getStartYearOffset(),
            (12 - startMonth + 1) );
        // Add the days in previous months of the current year
        if ( calMonth > 1 ) {
            // Add the days from the previous months...
            days += numDaysInMonths ( 1, d.getYear(), (calMonth -1) );
        }
        // Add the days from the current month...
        days += d.getDay();
    }
    else {
        // At the start of the year (previous calendar year)
        if ( calMonth > startMonth ) {
            // Add the days from the previous months...
            days += numDaysInMonths ( startMonth, d.getYear() + yearType.getStartYearOffset(),
                (calMonth - startMonth) );
        }
        // Add the days from the current month...
        days += d.getDay();
    }
    return days;
}

/**
Determine the inclusive output years given a time range in calendar years.  This is used to ensure that
the output year fully includes any data from the calendar dates, even if partial output years.
*/
public static DateTimeRange determineOutputYearTypeRange ( DateTime start, DateTime end, YearType outputYearType )
{   DateTime outputStart = new DateTime(start);
    outputStart.setPrecision(DateTime.PRECISION_YEAR);
    DateTime outputEnd = new DateTime(end);
    outputEnd.setPrecision(DateTime.PRECISION_YEAR);
    if ( (outputYearType == YearType.CALENDAR) ) {
        // Just return the original dates...
        return new DateTimeRange ( outputStart, outputEnd );
    }
    // Otherwise, deal with offsets
    
    // Start offset will be either 0 or -1
    if ( outputYearType.getStartYearOffset() < 0 ) {
        // Year types where the year starts in the previous calendar year and ends in the current calendar year.
        if (start.getMonth() >= outputYearType.getStartMonth() ) {
            // The old time series starts >= after the beginning of the output year and therefore the first
            // output year actually starts a year later.
            //
            //              ++++++++++++  Calendar +++++++++++++++++++++
            //              Jan                            start     Dec
            // Oct                                   Sep   start
            // Water
            outputStart.addYear ( 1 );
        }
        // Else the start is within the output year so no need to adjust the output year
        if ( end.getMonth() >= outputYearType.getStartMonth() ) {
            // The old time series ends after the beginning of the next output year so increment the year
            //
            //               ++++++++++++  Calendar +++++++++++++++++++++
            //               Jan                             end      Dec
            // Oct                                    Sep    end  
            // Water
            outputEnd.addYear ( 1 );
        }
    }
    else {
        // Year types where the year starts in the current calendar year and ends in the next calendar year
        if ( start.getMonth() < outputYearType.getStartMonth() ) {
            // The old time series starts before the beginning of the output year and would result
            // in a missed year at the start so decrement the first year, as shown in the following example:
            //
            //           ++++++++++++  Calendar +++++++++++++++++++++
            //           Jan   start                              Dec
            //                 start    May                                   Apr
            // YearMayToApr
            outputStart.addYear ( -1 );
        }
        if ( end.getMonth() < outputYearType.getStartMonth() ) {
            // The old time series ends in the previous output year so decrement the last year,
            // as shown in the following example:
            //
            //           ++++++++++++  Calendar +++++++++++++++++++++
            //           Jan   end                                Dec
            //                 end      May                                   Apr
            // YearMayToApr
            outputEnd.addYear ( -1 );
        }
    }
    return new DateTimeRange(outputStart,outputEnd);
}

/**
Compute the difference between dates.  See overloaded routine for more information.
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
	return offset;
}

/**
Format a DateTime using the given format.  The year type defaults to CALENDAR.
@param dt DateTime object to format
@param format format string (see overloaded version for details)
*/
public static String formatDateTime ( DateTime dt, String format )
{
    return formatDateTime ( dt, YearType.CALENDAR, format );
}

/**
Format a DateTime using the given format.
@return The date/time as a string for the specified date using the specified format.
@param d0 The date to format.  If the date is null, the current time is used.
@param yearType the year type, for example NOV_TO_OCT will result in date/times in Nov-Dec of the first year having a year
of the following calendar year for ${dt:YearForYearType} property
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
<td>Two digit year since 1900 (this is use discouraged because the datum is ambiguous).</td>
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
public static String formatDateTime ( DateTime d0, YearType yearType, String format0 )
{	String default_format = "%a %b %d %H:%M:%S %Z %Y";
	String format;

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
	// Get the date as if GMT if no timezone was specified
	// This is OK because no converstio to/from UNIX time is being used
	// Use the date to format and then use DateTime time zone
	Date date = d.getDate(TimeZoneDefaultType.GMT);

	if ( format.equals("datum_seconds") ) {
		// Want the number of seconds since the standard time datum	
		// Need to work on this...
		//long seconds = d.getTime ();
		//return Long.toString ( seconds/1000 );
		return "0";
	}
	// Convert format to string...
	GregorianCalendar cal = new GregorianCalendar ();
	cal.setTime ( date );
	SimpleDateFormat sdf = new SimpleDateFormat();
	DateFormatSymbols dfs = sdf.getDateFormatSymbols();
	String[] short_weekdays = dfs.getShortWeekdays();
	String[] short_months = dfs.getShortMonths();
	String[] months = dfs.getMonths();
	String[] weekdays = dfs.getWeekdays();
	int len = format.length();
	StringBuffer formatted_string = new StringBuffer ();
	char c = '\0';
	int ifield;
	// The values returned are as follows:
	//
	//            Java              This code
	//
	// Year:      since 1900        4-digit
	// Month:     0 to 11           1 to 12
	// Day:       1 to 31           1 to 31
	// Hour:      0 to 59           same
	// Minute:    0 to 59           same
	// Second:    0 to 59           same
	// DayOfWeek: 0 to 7 with 0     same
	//            being Sunday
	//            in U.S.
	// First go through the % format specifiers
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
			else if ( c == 's' ) {
				// Seconds since 1970-01-01 00:00:00+0000 (UTC)...
				formatted_string.append(""+date.getTime()/1000);
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
			else {
				// Go ahead and add the % and the character
				// (e.g., so the format can be passed to a secondary formatter).
				formatted_string.append ( '%' );
				formatted_string.append ( c );
			}
		}
		else if ( (c == '$') && (yearType != null) ) {
		    // TODO SAM 2013-12-23 For now hard-code one check but as more properties are added, make the code more elegant
		    String prop = "${dt:YearForYearType}";
		    int iEnd = i + prop.length(); // Last char, zero index
		    String year = "";
		    if ( iEnd <= format.length() ) {
		        //Message.printStatus(2, "", "substring=\"" + format.substring(i,iEnd) + "\" prop=\"" + prop + "\"");
		        if ( format.substring(i,iEnd).equalsIgnoreCase(prop) ) {
		            year = "" + TimeUtil.getYearForYearType(d, yearType);
	                formatted_string.append(year);
	                i = i + prop.length() - 1; // -1 because the iterator will increment by one
		        }
		        else {
		            // Just add the $ and march on
	                formatted_string.append ( c );
		        }
		    }
		    else {
		        // Just add the $ and march on
		        formatted_string.append ( c );
		    }
		}
		else {
			// Just add the character to the string...
			formatted_string.append ( c );
		}
	}
	// Next go through the ${dt:property} specifiers

	return formatted_string.toString();
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
<td>Two digit year since 1900 (this is use discouraged because the datum is ambiguous).</td>
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
	else {
		format = new String ( format0 );
	}

	Date d;
	if ( d0 == null ) {
		// Get the current time...
		d = new Date();
	}
	else {
		d = d0;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Formatting \"" + d0 + "\" using \"" + format + "\"" );
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
		int len = format.length();
		StringBuffer formatted_string = new StringBuffer ();
		char c = '\0';
		int ifield;	// integer field value
		SimpleDateFormat sdf = new SimpleDateFormat();
		DateFormatSymbols dfs = sdf.getDateFormatSymbols();
		String[] short_weekdays = dfs.getShortWeekdays();
		String[] short_months = dfs.getShortMonths();
		String[] months = dfs.getMonths();
		String[] weekdays = dfs.getWeekdays();
		String[] am_pm = dfs.getAmPmStrings();
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
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Day of week =" + ifield + " \"" + short_weekdays[ifield] + "\"" );
					}
				}
				else if ( c == 'A' ) {
					// Full weekday name.
					ifield = cal.get(Calendar.DAY_OF_WEEK);
					formatted_string.append(weekdays[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Day of week =" + ifield + " \"" + weekdays[ifield] + "\"" );
					}
				}
				else if ( c == 'b' ) {
					// Abbreviated month name.
					ifield = cal.get(Calendar.MONTH);
					formatted_string.append(short_months[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Month=" + ifield + " \"" + short_months[ifield] + "\"" );
					}
				}
				else if ( c == 'B' ) {
					// Long month name.
					ifield = cal.get(Calendar.MONTH);
					formatted_string.append(months[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Month=" + ifield + " \"" + months[ifield] + "\"" );
					}
				}
				else if ( c == 'c' ) {
					formatted_string.append("%c not supported" );
				}
				else if ( c == 'd' ) {
					// Day of month
					ifield = cal.get(Calendar.DAY_OF_MONTH);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Day =" + ifield );
					}
				}
				else if ( c == 'H' ) {
					// Hour of day...
					ifield = cal.get(Calendar.HOUR_OF_DAY);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Hour=" + ifield );
					}
				}
				else if ( c == 'I' ) {
					// Hour of day 1-12
					ifield = cal.get(Calendar.HOUR);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Hour =" + ifield );
					}
				}
				else if ( c == 'j' ) {
					// Day of year...
					ifield = cal.get(Calendar.DAY_OF_YEAR);
					formatted_string.append(StringUtil.formatString(ifield,"%03d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "DayofYear=" + ifield);
					}
				}
				else if ( c == 'm' ) {
					// Month of year...
					ifield = cal.get(Calendar.MONTH) + 1;
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Month =" + ifield );
					}
				}
				else if ( c == 'M' ) {
					// Minute of hour...
					ifield = cal.get(Calendar.MINUTE);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Minute =" + ifield );
					}
				}
				else if ( c == 'p' ) {
					// AM or PM...
					ifield = cal.get(Calendar.AM_PM);
					formatted_string.append(am_pm[ifield]);
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "AM/PM=" + ifield + " \"" + am_pm[ifield] + "\"" );
					}
				}
				else if ( c == 'S' ) {
					// Seconds of minute...
					ifield = cal.get(Calendar.SECOND);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Second =" + ifield );
					}
				}
				else if ( (c == 'U') || (c == 'W')) {
					// Week of year...
					// Don't worry now about whether Sunday or Monday are the start of the week.
					ifield = cal.get(Calendar.WEEK_OF_YEAR);
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine, "Weekofyear =" +
						ifield );
					}
				}
				else if ( c == 'x' ) {
					formatted_string.append("%x not supported" );
				}
				else if ( c == 'X' ) {
					formatted_string.append("%X not supported" );
				}
				else if ( c == 'y' ) {
					// Two digit year...
					ifield = cal.get(Calendar.YEAR);
					if ( ifield > 60000 ) {
						// Borland database bug...
						ifield -= 65536;
					}
					ifield = formatYear ( ifield, 2, true );
					formatted_string.append(StringUtil.formatString(ifield,"%02d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "year =" + ifield );
					}
				}
				else if ( c == 'Y' ) {
					// Four digit year...
					ifield = cal.get(Calendar.YEAR);
					if ( ifield > 60000 ) {
						// Borland database bug...
						ifield -= 65536;
					}
					ifield = formatYear ( ifield, 4, true );
					formatted_string.append(StringUtil.formatString(ifield,"%04d"));
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl, routine, "Year =" + ifield );
					}
				}
				else if ( c == 'Z' ) {
					// Time zone offset from GMT to local time, in milliseconds...
					formatted_string.append (
					cal.getTimeZone().getID() );
				}
				else if ( c == '%' ) {
					// Literal percent...
					formatted_string.append ( '%' );
				}
			}
			else {
				// Just add the character to the string...
				formatted_string.append ( c );
			}
		}
		return formatted_string.toString();
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
		else {
			// Truncate the year to return only the last 2 digits...
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
		else {
			// Get the year offset from the system (have to assume this so old data may have problems!).
			year_offset = getYearOffset ();
			if ( year_offset < 0 ) {
				Message.printWarning ( 3, "TimeUtil.formatYear", "Unable to get system year offset" );
				return -1;
			}
			// Get the current system year...
			// This does not seem to work well - it converts to Pacific time.
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
	else {
		// Unknown format request...
		Message.printWarning ( 3, "TimeUtil.formatYear", "Year ndigits " + len + " not 2 or 4!" );
		return -1;
	}
}

/**
Create a DateTime object from the specified UNIX time, which is the number of seconds since Jan 1, 1970 00:00:00.
@param unixTimeMs UNIX time as milliseconds since Jan 1, 1970 00:00:00.
@param dt DateTime containing date/time to receive results. If null then a new DateTime will be returned.
Specify to reuse a DateTime instance, such as when iterating.  Only data values computed from the UNIX time are set.
Time zone, etc. are not modified.
@return DateTime object resulting from the conversion. A new instance will be created if dt=null.
A new instance will have the time zone set to GMT.
@exception RuntimeException if there is an error processing the UNIX time
*/
public static DateTime fromUnixTime ( long unixTimeMs, DateTime dt )
throws RuntimeException
{
	// For now do not support negative
	if ( unixTimeMs < 0 ) {
		throw new RuntimeException("Negative UNIX time is not supported." );
	}
	DateTime dtout = null;
	if ( dt != null ) {
		dtout = dt;
	}
	else {
		dt = new DateTime();
		dt.setTimeZone("GMT");
	}
	// Number of seconds
	long unixTimeSeconds = unixTimeMs/1000;
	long seconds = 0;
	int year = 1970;
	int month = 1, day = 1, hour = 0, minute = 0, second = 0;
	// For now loop, but could improve performance
	for ( int i = 1970; ; i++ ) {
		if ( isLeapYear(i) ) {
			seconds = seconds + 366*86400;
		}
		else {
			seconds = seconds + 365*86400;
		}
		if ( seconds > unixTimeSeconds ) {
			// Have gone too many years
			if ( isLeapYear(i) ) {
				seconds = seconds - 366*86400;
			}
			else {
				seconds = seconds - 365*86400;
			}
			--year;
			break;
		}
		else {
			// Add another year
			++year;
		}
	}
	// Loop through months of year
	for ( month = 1; month <= 12; month++ ) {
		for ( day = 1; day <= numDaysInMonth(month,year); day++ ) {
			if ( (unixTimeSeconds - seconds) < 86400 ) {
				// Need to break because time is in previous day
				break;
			}
			seconds = seconds + 86400;
		}
	}
	// Now are in the correct day so may for hours, minutes, and seconds is simple.
	long secondsLeft = unixTimeSeconds - seconds;
	hour = (int)secondsLeft/3600;
	minute = (int)(secondsLeft%3600)/60;
	second = (int)(secondsLeft - hour*3600 - minute*60);
	dtout.setYear(year);
	dtout.setYear(month);
	dtout.setYear(day);
	dtout.setYear(hour);
	dtout.setYear(minute);
	dtout.setYear(second);
	return dt;
}

/**
Get the current day of the week as a number.
@return The current day of the week as a number, in the range 0-6, with 0 being
Sunday.  The system clock is used to get the current time.  Return -1 if an error.
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
	return -1;
}

/**
Return an array of valid format specifiers for the formatDateTime() method, in
the format "%X - Description" where X is the format specifier.  The specifiers correspond to the strftime
formatting routine.
@return an array of format specifiers.
@param includeDescription If false, only the %X specifiers are returned.  if
True, the description is also returned.
@param forOutput if true, return specifiers for formatting; if false only include formatters for parsing
@param includeProps if true, include properties like ${dt:YearForYearType}, currently only used for output
*/
public static String[] getDateTimeFormatSpecifiers(boolean includeDescription, boolean forOutput, boolean includeProps )
{	int nProps = 0;
    if ( forOutput && includeProps ) {
        nProps = 1;
    }
    String [] formats = new String[16 + nProps];
    int i = -1;
	formats[++i] = "%a - Weekday, abbreviation";
	formats[++i] = "%A - Weekday, full";
	formats[++i] = "%b - Month, abbreviation";
	formats[++i] = "%B - Month, full";
	//formats[++i] = "%c - Not supported";
	formats[++i] = "%d - Day (01-31)";
	formats[++i] = "%H - Hour (00-23)";
	formats[++i] = "%I - Hour (01-12)";
	formats[++i] = "%j - Day of year (001-366)";
	formats[++i] = "%m - Month (01-12)";
	formats[++i] = "%M - Minute (00-59)";
	formats[++i] = "%p - AM, PM";
	formats[++i] = "%s - seconds since Jan 1, 1970 UTC";
	formats[++i] = "%S - Second (00-59)";
	//formats[++i] = "%U, %W - not supported";
	//formats[++i] = "%x - not supported";
	//formats[++i] = "%X - not supported";
	formats[++i] = "%y - Year (00-99)";
	formats[++i] = "%Y - Year (0000-9999)";
	formats[++i] = "%Z - Time zone";
	if ( forOutput && includeProps ) {
	    formats[++i] = "${dt:YearForYearType} - year for year type";
	}
	if ( !forOutput ) {
	    // Only include formats suitable for parsing
	    String [] formats2 = {
	    	"%S - Second (00-59)",
	    	"%M - Minute (00-59)",
	        "%H - Hour (00-23)",
	        "%d - Day (01-31)",
            //"%j - Day of year (001-366)", // day of year - TODO SAM 2012-04-18 should be able to do
	        "%b - Month, abbreviation",
	        "%m - Month (01-12)",
	        "%y - Year (00-99)",
	        "%Y - Year (0000-9999)"
        };
	    formats = formats2;
	}
	if ( !includeDescription ) {
        // Remove the text including and after the dash
        for ( int j = 0; j < formats.length; j++ ) {
            formats[j] = formats[j].substring(0,formats[j].indexOf("-")).trim();
        }
	}
	return formats;
}

/**
Return an array of valid format specifiers for the strftime formatter in
the format "%X - Description" where X is the format specifier.  This is useful
for building graphical interfaces.
@return an array of format specifiers.
@param includeDescription If false, only the %X specifiers are returned.  if
True, the description is also returned.
*/
/* TODO SAM 2011-11-14 Merge with the above method
public static String[] getStrftimeFormatSpecifiers(boolean includeDescription )
{   String [] formats = new String[23];
    int i = 0;
    // These are from Linux strftime
    formats[i++] = "%a - abbreviated weekday name"; // Should depend on locale
    formats[i++] = "%A - full weekday name"; // Should depend on locale
    formats[i++] = "%b - abbreviated month name"; // Should depend on locale
    formats[i++] = "%B - full month name"; // Should depend on locale
    //formats[i++] = "%c - preferred date and time representation"; // Not supported here
    formats[i++] = "%C - century number (year/100)r";
    formats[i++] = "%d - day of month (01-31)";
    formats[i++] = "%D - %m/%d/%y";
    formats[i++] = "%e - like %d but pad with space";
    //formats[i++] = "%E - modifier"; // Not supported here
    formats[i++] = "%F - %Y-%m-%d";
    //formats[i++] = "%G - week-based year"; // Not supported here
    //formats[i++] = "%g - like %G but without century"; // Not supported here
    //formats[i++] = "%h - equivalent to %b"; // Redundant, not used
    formats[i++] = "%H - hour (00-23)";
    //formats[i++] = "%I - hour (01-12)"; // TODO SAM 2011-11-14 add later
    formats[i++] = "%j - day of year (001-366)";
    //formats[i++] = "%l - hour (1-12)"; // TODO SAM 2011-11-14 add later
    formats[i++] = "%m - month (01-12)";
    formats[i++] = "%M - minute (00-59)";
    formats[i++] = "%n - newline";
    //formats[i++] = "%O - modifier"; // Not supported here
    formats[i++] = "%p - AM or PM (noon=PM, midnight=AM)";
    formats[i++] = "%P - am or pm (noon=pm, midnight=am)";
    //formats[i++] = "%r - time as %I:%M:%S %p";
    formats[i++] = "%R - time as %H:%M";
    //formats[i++] = "%s - seconds since 1970-01-01 00:00:00"; // Not supported here
    formats[i++] = "%S - seconds (00-59)";
    formats[i++] = "%t - tab";
    formats[i++] = "%T - time as %H:%M:%S";
    //formats[i++] = "%u - day of week (1-7, Monday=1)";
    //formats[i++] = "%U - week number (00-53, 1st Sunday=week 01)";
    //formats[i++] = "%V - week number (01-53, 1st week has > 4 days)";
    //formats[i++] = "%w - day of week (0-6, Sunday=0)";
    //formats[i++] = "%W - week number (00-53, 1st Monday=week 01)";
    //formats[i++] = "%x - preferred local date";
    //formats[i++] = "%X - preferred local time";
    formats[i++] = "%y - year (00-99)";
    formats[i++] = "%Y - year (YYYY)";
    //formats[i++] = "%z - +hhmm or -hhmm offset from UTC";
    //formats[i++] = "%Z - time zone";
    //formats[i++] = "%+ - date(1) format";
    formats[i++] = "%% - literal %";
    if ( !includeDescription ) {
        // Remove the text including and after the dash
        for ( int j = 0; j < formats.length; j++ ) {
            formats[j] = formats[j].substring(0,formats[j].indexOf("-")).trim();
        }
    }
    return formats;
}
*/

/**
Return the local time zone abbreviation.
The method of lookup is either LOOKUP_TIME_ZONE_ONCE (default) or
LOOKUP_TIME_ZONE_ALWAYS.  The method can be changed by calling the overloaded method.
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
{	if ( (time_zone_lookup_method == LOOKUP_TIME_ZONE_ONCE) ||
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
		else {
			return _local_time_zone_string;
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
		else {
			// Loop forwards subtracting the days for each month until
			// have only one complete or partial month.
			int	daysleft = julian_day;
			int	monthdays;
			for ( int i = 0; i < 12; i++ ) {
				if ( (i == 1) && isleap ) {
					// Set the offset to 1 to account for extra days in February...
					monthdays = MONTH_DAYS[i] + 1;
				}
				else {
					// All other months...
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
		"Error getting month and day from year " + year + " Julian day " + julian_day );
		return null;
	}
}

/**
Return the number of intervals between two dates.  This uses a loop to count
the intervals and may be slow.  For time series, a faster method may be to call
the TSUtil.getDataSize method.  Zero is returned if the start date is after the end date.
Zero is returned if the end date equals the start date since no interval will be traversed.
@return The number of intervals between two dates.
@param t1 Start date.
@param t2 End date.
@param base The time series base interval.
@param mult The time series interval multiplier.
*/
public static int getNumIntervals (	DateTime t1, DateTime t2, int base, int mult )
{
	if( t2.lessThan(t1) ) {
		Message.printWarning( 3, "TimeUtil.getNumIntervals", 
		"End " + t2 + " is before start " + t1 + ".  Returning 0." );
		return 0;
	}

	// Want to remain less than t2, so if the two dates are the same return 0.
	int intervals=0;
	for ( DateTime t = new DateTime(t1); t.lessThan(t2); t.addInterval( base, mult ) ) {
		intervals++;	
	}

	return intervals;
}

/**
Return the current system time using the default format.
@return The current system time as a string, using the default format used by formatDateTime.
REVISIT JAVADOC: see RTi.Util.Time.TimeUtil.formatDateTime
*/
public static String getSystemTimeString ( )
{	return formatDateTime ( null, null );
}

/**
Return the current system time using the specified format.
@return The current system time as a string, using the specified format, as used by formatDateTime.
@param format Format for date (see formatDateTime).
*/
public static String getSystemTimeString ( String format )
{	return formatDateTime ( null, format );
}

/**
Determine the year type year given a DateTime and the year type.
For example, return the water or irrigation year.
@param dt DateTime to examine, should be in normal calendar year
@param yt year type
@return the year for the requested year type
*/
public static int getYearForYearType ( DateTime dt, YearType yt )
{
    int y = dt.getYear();
    int m = dt.getMonth();
    if ( (m >= yt.getStartMonth()) && (m <= 12) ) {
        // Month is in the start of the year
        if ( yt.yearMatchesStart() ) {
            return y;
        }
        else {
            return y - yt.getStartYearOffset(); // Subtract because offset is negative
        }
    }
    else {
        // Month is in the end of the year
        if ( yt.yearMatchesStart() ) {
            return y + yt.getStartYearOffset(); // Add because offset is negative
        }
        else {
            // Year matches the end
            return y;
        }
    }
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

	year = (jd*400)/146097 + 1800;

	while ( true ) {
		leap = 1;
		if ( (year%4) != 0 ) {
			leap = 0;
		}
		if ( ((year%100) == 0) && ((year%400) != 0) ) {
			leap = 0;
		}
		id1 = 365*(year) +
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
		Message.printDebug ( dl, routine, "After first pass, found year to be: " + year );
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
		Message.printDebug ( dl, routine, "After second pass, found year to be: " + year );
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
		Message.printDebug ( 1, routine, "Found month to be: " + month );
	}

	//  Month known, compute day offset from month

	day -= MONTH_YEARDAYS[month - 1];
	if ( month >= 3 ) {
		day -= leap;
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine, "Found day to be: " + day );
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
two-digit year is passed in, the offset is determined using the current system clock.
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
precision (UNKNOWN or IRREGULAR).
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
	if ( (precision1 < TimeInterval.HSECOND) || (precision1 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for first DateTime is not between hsecond and year." );
	}
	int precision2 = datetime2.getPrecision();
	if ( (precision2 < TimeInterval.HSECOND) || (precision2 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for second DateTime is not between hsecond and year." );
	}
	if ( precision1 < precision2 ) {
		return precision1;
	}
	return precision2;
}

// TODO SAM 2005-11-16 Should this be deprecated in favor of the convert*Month methods?
/**
Get the irrigation month given a calendar month.  The irrigation year starts in
November of the previous calendar year and ends in October of the irrigation year.
@return the irrigation month.
*/
public static int irrigationMonthFromCalendar ( int month )
{	if ( month >= 11 ) {
		return (month - 10);
	}
	else {
	    return (month + 2);
	}
}

// TODO SAM 2005-11-16
// Should this be deprecated in favor of the convert*Month methods?
/**
Get the irrigation year given a calendar month and year.  The irrigation year
starts in November of the previous calendar year and ends in October of the irrigation year.
@return the irrigation year.
*/
public static int irrigationYearFromCalendar ( int month, int year )
{	if ( month >= 11 ) {
		return (year - 1);
	}
	else {
	    return year;
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
{	try {
        DateTime.parse ( date_string, format );
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
{	try {
	    DateTime.parse ( date_string );
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
{	if ((((year%4) == 0) && ((year%100) != 0)) || (((year%100) == 0) && ((year%400) == 0)) ) {
		return true;
	}
	else {
	    return false;
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
	else {
	    return false;
	}
}

/**
Determine whether the day is valid.
@return true if the day is valid (in the range 1-31), false if not.
@param day Day to check (an integer as a String).
*/
public static boolean isValidDay ( String day )
{	try {
        int iday = Integer.parseInt ( day );
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
	else {
	    return false;
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

	try {
	    iday = Integer.parseInt ( day );
	}
	catch ( Exception e ) {
		return false;
	}
	try {
	    imonth = Integer.parseInt ( month );
	}
	catch ( Exception e ) {
		return false;
	}
	try {
	    iyear = Integer.parseInt ( year );
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
	else {
	    return false;
	}
}

/**
Determine whether an hour is valid.
@return true if the hour is valid (in the range 0-23), false if not.
@param hour Hour to check, an integer as a String.
*/
public static boolean isValidHour ( String hour )
{	try {
        int ihour = Integer.parseInt ( hour );
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
	else {
	    return false;
	}
}

/**
Determine whether a minute is valid.
@return true if the minute is valid (in the range 0-59), false if not.
@param minute Minute to check, an integer as a String.
*/
public static boolean isValidMinute ( String minute )
{	try {
        int iminute = Integer.parseInt ( minute );
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
	else {
	    return false;
	}
}

/**
Determine whether a month is valid.
@return true if the month is valid (in the range 1-12), false if not.
@param month Month to check, an integer as a String.
*/
public static boolean isValidMonth ( String month )
{	try {
        int imonth = Integer.parseInt ( month );
		return isValidMonth ( imonth );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Determine whether a year is valid.
@return true if the year is valid (> 100 and not 9999), false if not.
@param year Year to check.
*/
public static boolean isValidYear ( int year )
{	if ( (year < 100) || (year == 9999) ) {
		return false;
	}
	else {
	    return true;
	}
}

/**
Determine whether a year is valid.
@return true if the year is valid (in the range 1-12), false if not.
@param year Year to check, an integer as a String.
*/
public static boolean isValidYear ( String year )
{	try {
    int iyear = Integer.parseInt ( year );
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
precision (UNKNOWN or IRREGULAR).
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
	if ( (precision1 < TimeInterval.HSECOND) || (precision1 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for first DateTime is not between hsecond and year." );
	}
	int precision2 = datetime2.getPrecision();
	if ( (precision2 < TimeInterval.HSECOND) || (precision2 > TimeInterval.YEAR) ) {
		throw new Exception ( "Precision for second DateTime is not between hsecond and year." );
	}
	if ( precision1 > precision2 ) {
		return precision1;
	}
	return precision2;
}

/**
Return the maximum of two DateTime instances.  If one is null, return the
non-null value.  If both are null, return null.  The comparison is made as follows:
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
	else {
		return dt2;
	}
}

/**
Return the minimum of two DateTime instances.  If one is null, return the
non-null value.  If both are null, return null.  The comparison is made as follows:
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
	else {
		return MONTH_ABBREVIATIONS[month - 1];
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
{   for ( int i = 0; i < 12; i++ ) {
        if(abbrev.equalsIgnoreCase( MONTH_ABBREVIATIONS[i] ) ) {
            return i + 1;
        }
    }
    return 0;
}

/**
Convert an absolute month to its year and month values.
@return An array if int's indicating the month and year corresponding to the
given absolute month.  The first value will be the year, the second will be the month.
@param amon Absolute month (year*12 + month).
*/
public static int[] monthFromAbsolute ( int amon )
{	int	month, year;
	int[] monthyear = new int[2];

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
Return the month of the year, where month 1 is the first month in the year type.
For example Jan is month 1 for calendar year type and Oct is month 1 for water year type.
@return The month of the year (where 1 is the first month of the year and 12 is the last).
@param d Datetime to evaluate, in calendar year.
@param yearType year type.
@exception IllegalArgumentException if input is invalid.
*/
public static int monthOfYear ( DateTime d, YearType yearType )
{   int calMonth = d.getMonth();
    if ( yearType == YearType.CALENDAR ) {
        return calMonth;
    }
    // Else have non-calendar year so evaluation is a bit more complex
    if ( calMonth < yearType.getStartMonth() ) {
        return 12 - yearType.getStartMonth() + 1 + calMonth;
    }
    else {
        return calMonth - yearType.getStartMonth() + 1;
    }
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
	else {
		// Usual case...
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

	month = month0;
	year = year0;
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
static public int numDaysInMonths (	int month0, int year0, int month1, int year1 )
{	int nmonths = absoluteMonth ( month1, year1 ) - absoluteMonth ( month0, year0 ) + 1;
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
	else {
		return 365;
	}
}


/**
Parse a 4-digit military time into its hour and minute and return in an array of int's.
@return An array of int's with the hour (index 0) and minute (index 1).
@param time 4-digit military time.
*/
public static int [] parseMilitaryTime ( int time )
{	int [] hour_min = new int[2];
	hour_min[0] = time/100; // the hour
	hour_min[1] = time - hour_min[0]*100; // the minutes
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
{	if ( milliseconds == 0 ) {
        return;
    }
    // TODO SAM 2014-05-12 Figure out how to handle exception
    try {
        Thread.sleep(milliseconds);
    } catch(InterruptedException ex) {
        //Thread.currentThread().interrupt();
    }
    /*
    // Get the current date...
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
	*/
}

/**
Calculate the UNIX time, which is the number of seconds since Jan 1, 1970 00:00:00.
The calculation is only implemented for 1970 to 2036.
@param dt DateTime containing date/time to process.  All parts of the date/time are processed, to seconds.
@return the UNIX time as milliseconds since Jan 1, 1970 00:00:00.
@exception RuntimeException if there is an error computing the UNIX time
*/
public static long toUnixTime ( DateTime dt )
throws RuntimeException
{
	long seconds = 0;
	int year = dt.getYear();
	int yearm1 = year - 1;
	if ( (year > 1970) || (year > 2036) ) {
		throw new RuntimeException ( "Year (" + year + ") is not between 1970 and 2036");
	}
	// For now loop, but could save a static array to improve performance
	for ( int i = 1970; i < yearm1; i++ ) {
		if ( isLeapYear(i) ) {
			seconds = seconds + 366*86400;
		}
		else {
			seconds = seconds + 365*86400;
		}
	}
	// Days of year...
	int daysPrev = dt.getYearDay() - 1;
	if ( daysPrev > 0 ) {
		seconds = seconds + daysPrev*86400;
	}
	// Hours in current day
	seconds = seconds + dt.getHour()*3600;
	// Minutes in current day
	seconds = seconds + dt.getMinute()*60;
	// Seconds in current day
	seconds = seconds + dt.getSecond();
	// Now multiply by 1000;
	return seconds*1000;
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
			Message.printStatus ( 1, "", "File \"" + filename + "\" DOES exist at " + formatTimeString() );
			return true;
		}
		else {
			Message.printStatus ( 1, "", "File \"" + filename + "\" DOES NOT exist at " + formatTimeString() );
			sleep ( wait );
		}
	}
	return false;
}

// TODO SAM 2005-11-16 Should this be deprecated in favor of the convert*Month methods?
/**
Get the water year month given a calendar month.  The water year starts in
October of the previous calendar year and ends in September of the irrigation year.
@return the water year month.
*/
public static int waterMonthFromCalendar ( int month )
{	if ( month >= 10 ) {
		return (month - 9);
	}
	else {
		return (month + 3);
	}
}

// TODO SAM 2005-11-16 Should this be deprecated in favor of the convert*Month methods?
/**
Get the water year given a calendar month and year.  The water year
starts in October of the previous calendar year and ends in September of the irrigation year.
@return the water year.
*/
public static int waterYearFromCalendar ( int month, int year )
{	if ( month >= 10 ) {
		return (year - 1);
	}
	else {
		return year;
	}
}

}