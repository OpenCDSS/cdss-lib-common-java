// DateTime - general Date/Time class


/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.Util.Time;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
//import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import RTi.TS.UnsupportedTimeIntervalException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import RTi.Util.Time.TimeUtil;
import RTi.Util.String.StringUtil;

/**
The DateTime class provides date/time storage and manipulation for general use.
Unlike the Java Date and Calendar classes, this class allows date/time
data fields to be easily set and manipulated,
for example to allow fast iteration without having to recreate DateTime instances.
Specific features of the DateTime class are:
<ul>
<li>	An optional bitmask flag can be used at construction to indicate the
	precision (which matches the TimeInterval values),
	initialization (to zero or current date/time), and performance (fast or strict).
	
	TimeInterval.YEAR is equal to DateTime.PRECISION_YEAR, etc.</li>
<li>	The precision values are mutually exclusive; therefore,
    they can be compared as binary mask values or with ==.</li>
<li>	By default the time zone is not used in DateTime manipulation or output.
	However, if the PRECISION_TIME_ZONE flag is set during creation or with a call to setTimeZone(),
	then the time zone is intended to be used throughout (comparison, output, etc.).
	See the getDate*() methods for variations that consider time zone.</li>
<li>	DateTime objects can be used in TimeUtil methods in a generic way.</li>
<li>	Call isZero() to see whether the DateTime has zero values.
    A zero DateTime means that the date is not the current time and values have
	not been reset from the defaults.</li>
<li>	Precisions allow "abbreviating" a DateTime to consider only certain data fields.
    By default, the larger interval data (e.g., year) are
	included and only smaller data (e.g., seconds) can be cut out of the precision.
	If the TIME_ONLY bitmask is used at creation, then the date fields can be ignored.</li>
</ul>
*/
@SuppressWarnings("serial")
public class DateTime implements Cloneable, Comparable<DateTime>, Serializable
{

/**
Flags for constructing DateTime instances, which modify their behavior.
These flags have values that do not conflict with the TimeInterval base interval
values and the flags can be combined in a DateTime constructor.
The following flag indicates that a DateTime be treated strictly,
meaning that the following dependent data are reset each time a date field changes:
<p>
day of year<br>
whether a leap year<br>

<p>
This results in slower processing of dates and is the default behavior.
For iterators, it is usually best to use the DATE_FAST behavior.
*/
public static final int DATE_STRICT	= 0x1000;

/**
Indicates that dates need not be treated strictly.
This is useful for faster processing of dates in loop iterators,
where the DateTime 'add' methods ensure valid values.
*/
public static final int DATE_FAST = 0x2000;

/**
Create a DateTime with zero data and blank time zone, which is the default.
*/
public static final int DATE_ZERO = 0x4000;

/**
Create a DateTime with the current date and time.
*/
public static final int DATE_CURRENT = 0x8000;

/**
Create a DateTime and only use the time fields.  This works in conjunction with the precision flag.
*/
public static final int TIME_ONLY = 0x10000;

/**
The following are meant to be used in the constructor and will result in the
the precision for the date/time being limited only to the given date/time field.
These flags may at some point replace the flags used for the equals method.
If not specified, all of the date/time fields for the DateTime are carried (PRECISION_HSECOND).
Note that these values are consistent with the TimeInterval base interval values.
*/

/**
Create a DateTime with precision only to the year.
*/
public static final int PRECISION_YEAR = TimeInterval.YEAR;

/**
Create a DateTime with a precision only to the month.
*/
public static final int PRECISION_MONTH	= TimeInterval.MONTH;

/**
Create a DateTime with a precision only to the day.
*/
public static final int PRECISION_DAY = TimeInterval.DAY;

/**
Create a DateTime with a precision only to the hour.
*/
public static final int PRECISION_HOUR = TimeInterval.HOUR;

/**
Create a DateTime with a precision only to the minute.
*/
public static final int PRECISION_MINUTE = TimeInterval.MINUTE;

/**
Create a DateTime with a precision only to the second.
*/
public static final int PRECISION_SECOND = TimeInterval.SECOND;

/**
Create a DateTime with a precision to the hundredth-second.
*/
public static final int PRECISION_HSECOND = TimeInterval.HSECOND;

/**
Create a DateTime with a precision to the millisecond (1/1000 second).
*/
public static final int PRECISION_MILLISECOND = TimeInterval.MILLISECOND;

/**
Create a DateTime with a precision to the microsecond (1/1,000,000 second).
*/
public static final int PRECISION_MICROSECOND = TimeInterval.MICROSECOND;

/**
Create a DateTime with a precision to the nanosecond (1/1,000,000,000 second).
*/
public static final int PRECISION_NANOSECOND = TimeInterval.NANOSECOND;

/**
Create a DateTime with a precision that includes the time zone (and may include another precision flag).
*/
public static final int PRECISION_TIME_ZONE= 0x20000;

// Alphabetize the formats, but the numbers may not be in order because they
// are added over time (do not renumber because some dependent classes may not get recompiled).
/**
The following are used to format date/time output.
<pre>
	Y = year
	M = month
	D = day
	H = hour
	m = minute
	s = second
	h = 100th second
	Z = time zone
</pre>
*/
/**
The following returns an empty string for formatting but can be used to indicate no formatting in other code.
*/
public static final int FORMAT_NONE = 1;

/**
The following returns the default format and can be used to indicate automatic formatting in other code.
*/
public static final int FORMAT_AUTOMATIC = 2;

/**
The following formats a date as follows:  "DD/MM/YYYY"
This date format cannot be parsed properly by parse().
FORMAT_MM_SLASH_DD_SLASH_YYYY will be returned instead.
*/
public static final int FORMAT_DD_SLASH_MM_SLASH_YYYY = 27;

/**
The following formats a date as follows: "HH:mm"
*/
public static final int FORMAT_HH_mm = 3;

/**
The following formats a date as follows (military time): "HHmm"
*/
public static final int FORMAT_HHmm = 4;

/**
The following formats a date as follows: "MM"
Parsing of this date format without specifying the format is NOT supported because it is ambiguous.
*/
public static final int FORMAT_MM = 5;

/**
The following formats a date as follows: "MM-DD"
*/
public static final int FORMAT_MM_DD = 6;

/**
The following formats a date as follows: "MM/DD"
*/
public static final int FORMAT_MM_SLASH_DD = 7;

/**
The following formats a date as follows: "MM/DD/YY"
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YY = 8;

/**
The following formats a date as follows: "MM/DD/YYYY"
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY = 9;

/**
The following formats a date as follows: "MM/DD/YYYY HH"
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH = 10;

/**
The following formats a date as follows: "MM-DD-YYYY HH"
*/
public static final int FORMAT_MM_DD_YYYY_HH = 11;

/**
The following formats a date as follows: "MM/DD/YYYY HH:mm"
For the parse() method, months, days, and hours that are not padded with zeros will also be parsed properly.
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm = 12;

/**
The following formats a date as follows: "MM/YYYY"
*/
public static final int FORMAT_MM_SLASH_YYYY = 13;

/**
The following formats a date as follows: "YYYY"
*/
public static final int FORMAT_YYYY = 14;

/**
The following formats a date as follows:  "YYYY-MM".
*/
public static final int FORMAT_YYYY_MM = 15;

/**
The following formats a date as follows: "YYYY-MM-DD"
*/
public static final int FORMAT_YYYY_MM_DD = 16;

/**
The following is equivalent to:  "FORMAT_YYYY_MM_DD"
*/
public static final int FORMAT_Y2K_SHORT = FORMAT_YYYY_MM_DD;

/**
The following formats a date as follows:  "YYYY-MM-DD HH"
*/
public static final int FORMAT_YYYY_MM_DD_HH = 17;

/**
The following formats a date as follows: "YYYY-MM-DD HH ZZZ"
*/
public static final int FORMAT_YYYY_MM_DD_HH_ZZZ = 18;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm	= 19;

/**
The following is equivalent to FORMAT_YYYY_MM_DD_HH_mm.
*/
public static final int FORMAT_Y2K_LONG = FORMAT_YYYY_MM_DD_HH_mm;

/**
The following formats a date as follows: "YYYY-MM-DD HHmm"
*/
public static final int FORMAT_YYYY_MM_DD_HHmm = 20;

/**
The following formats a date as follows: "YYYYMMDDHHmm"
*/
public static final int FORMAT_YYYYMMDDHHmm = 21;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm ZZZ"
This format is currently only supported for toString() (not parse).
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_ZZZ = 22;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS = 23;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.hh"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_hh = 24;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.hh ZZZ"
This is nearly ISO 8601 but it does not include the T before time and the time zone has a space.
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ = 25;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS ZZZ"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ = 26;

/**
The following formats a date as follows: "MM/DD/YYYY HH:mm:SS"
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS = 28;

/**
The following formats a date as follows: "YYYYMMDD"
*/
public static final int FORMAT_YYYYMMDD = 29;

/**
The following formats a date/time according to ISO 8601,
for example for longest form:  "2017-06-30T23:03:33.123+06:00"
*/
public static final int FORMAT_ISO_8601 = 30;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxx"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI = 31;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxxxxx"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO = 32;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxxxxxxxx"
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_NANO = 33;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxx ZZZ"
This is nearly ISO 8601 but it does not include the T before time and the time zone has a space.
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI_ZZZ = 34;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxxxxx ZZZ"
This is nearly ISO 8601 but it does not include the T before time and the time zone has a space.
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO_ZZZ = 35;

/**
The following formats a date as follows: "YYYY-MM-DD HH:mm:SS.xxxxxxxxx ZZZ"
This is nearly ISO 8601 but it does not include the T before time and the time zone has a space.
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_NANO_ZZZ = 36;

/**
The following formats a date as follows, for debugging:  year=YYYY, month=MM, etc.
*/
public static final int FORMAT_VERBOSE = 200;

/**
Nanosecond fraction of a second (0 to 999,999,999).
Other values can e calculated from nanoseconds as follows:
  hundredths (/10000000)
  milliseconds (/1000000)
  microseconds (/1000)
*/
private int __nano;

/**
Seconds (0-59).
*/
private int __second;

/**
Minutes past hour (0-59).
*/
private int __minute;

/**
Hours past midnight (0-23).
Important - hour 24 in data should be handled as hour 0 of the next day.
*/
private int __hour;

/**
Day of month (1-31).
*/
private int __day;

/**
Month (1-12).
*/
private int __month;

/**
Year (4 digit).
*/
private int __year;

/**
Time zone abbreviation.
*/
private String __tz;

/**
Indicate whether the year a leap year (true) or not (false).
*/
private boolean	__isleap;

/**
Is the DateTime initialized to zero without further changes?
*/
private boolean	__iszero;

/**
Day of week (0=Sunday).  Will be calculated in getWeekDay().
*/
private int __weekday = -1;

/**
Day of year (0-365).
*/
private int	__yearday;

/**
Absolute month (year*12 + month).
*/
private int __abs_month;

/**
Precision of the DateTime (allows some optimization and automatic decisions when converting).
This is the PRECISION_* value only (not a bit mask).
_use_time_zone and _time_only control other precision information.
Default to PRECISION_SECOND, but is typically reset when dealing with time series or input data;
*/
private int __precision = PRECISION_SECOND;

/**
Flag for special behavior of dates.  Internally this contains all the
behavior flags but for the most part it is only used for ZERO/CURRENT and FAST/STRICT checks.
*/
private int __behavior_flag;

/**
Indicates whether the time zone should be used when processing the DateTime.
SetTimeZone() will set to true if the time zone is not empty, false if empty.
Setting the precision can override this if time zone flag is set.
*/
private boolean __use_time_zone = false;

/**
Use only times for the DateTime.
*/
private boolean	__time_only = false;

/**
Default constructor (set to zero time).
*/
public DateTime () {
	setToZero ();
	reset();
}

/**
Construct using the constructor modifiers (combination of PRECISION_*, DATE_CURRENT, DATE_ZERO, DATE_STRICT, DATE_FAST).
If no modifiers are given, the date/time is initialized to zeros and precision is PRECISION_MINUTE.
@param flag Constructor modifier.
*/
public DateTime ( int flag ) {
	if ( (flag & DATE_CURRENT) != 0 ) {
		setToCurrent ();
	}
	else {
		// Default.
		setToZero();
	}

	__behavior_flag = flag;
	setPrecision ( flag );
	reset();
}

/**
Construct from a Java Date.  The time zone is not set - use the overloaded method if necessary.
@param d Java Date.
*/
@SuppressWarnings("deprecation")
public DateTime ( Date d ) {
	// If a null date is passed in, behave like the default DateTime() constructor.
	if (d == null) {
		setToZero ();
		reset();
		return;
	}

	// use_deprecated indicates whether to use the deprecated Date functions.
	// These should be fast (no strings) but are, of course, deprecated.
	boolean	use_deprecated = true;

	if ( use_deprecated ) {
		// Returns the number of years since 1900.
		int year = d.getYear();
		setYear ( year + 1900 );
		// Returned month is 0 to 11.
		setMonth ( d.getMonth() + 1 );
		// Returned day is 1 to 31.
		setDay ( d.getDate() );
		setPrecision ( PRECISION_DAY );
		// Sometimes Dates are instantiated from data where hours, etc. are not available (e.g. from a database date/time).
		// Therefore, catch exceptions at each step.
		try {
            // Returned hours are 0 to 23.
			setHour ( d.getHours() );
			setPrecision ( PRECISION_HOUR );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned hours are 0 to 59.
			setMinute ( d.getMinutes() );
			setPrecision ( PRECISION_MINUTE );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned seconds are 0 to 59.
			setSecond ( d.getSeconds() );
			setPrecision ( PRECISION_SECOND );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		// TODO SAM 2015-08-12 For now do not set the hundredths of a second.
		__tz = "";
	}
	else {
        // Date/Calendar are ugly to work with, let's get information by formatting strings.

		// year month
		// Use the formatTimeString routine instead of the following.
		// String format = "yyyy M d H m s S";
		// String time_date = TimeUtil.getTimeString ( d, format );
		String format = "%Y %m %d %H %M %S";
		String time_date = TimeUtil.formatTimeString ( d, format );
		List<String> v = StringUtil.breakStringList ( time_date, " ", StringUtil.DELIM_SKIP_BLANKS );
		setYear ( Integer.parseInt(v.get(0)) );
		setMonth ( Integer.parseInt(v.get(1)) );
		setDay ( Integer.parseInt(v.get(2)) );
		setHour ( Integer.parseInt(v.get(3)) );
		setMinute ( Integer.parseInt(v.get(4)) );
		setSecond ( Integer.parseInt(v.get(5)) );
		// milliseconds not supported in formatTimeString.
		// Convert from milliseconds to 100ths of a second.
		// setHSecond ( Integer.parseInt(v.elementAt(6))/10 );
		// setTimeZone ( v.elementAt(7) );
		__tz = "";
	}

	reset();
	__iszero = false;
}

/**
Construct from a Java Date.  The time zone is not set unless the behavior flag includes PRECISION_TIME_ZONE.
@param d Java Date.
@param behavior_flag Flag indicating the behavior of the instance - see the defined bit mask values.
*/
@SuppressWarnings("deprecation")
public DateTime ( Date d, int behavior_flag ) {
	boolean	use_deprecated = true;

	if ( use_deprecated ) {
		// Returns the number of years since 1900.
		int year = d.getYear();
		setYear ( year + 1900 );
		// Returned month is 0 to 11.
		int month0 = d.getMonth();
		setMonth ( month0 + 1 );
		// Returned day is 1 to 31.
		setDay ( d.getDate() );
		// Sometimes Dates are instantiated from data where hours, etc. are not available (e.g. from a database date/time).
		// Therefore, catch exceptions at each step.
		try {
            // Returned hours are 0 to 23.
			setHour ( d.getHours() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned hours are 0 to 59.
			setMinute ( d.getMinutes() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned seconds are 0 to 59.
			setSecond ( d.getSeconds() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		__tz = "";
	}
	else {
        // Date/Calendar are ugly to work with, so get information by formatting strings.

		// year month
		// Use the formatTimeString routine instead of the following.
		// String format = "yyyy M d H m s S";
		// String time_date = TimeUtil.getTimeString ( d, format );
		String format = "%Y %m %d %H %M %S";
		String time_date = TimeUtil.formatTimeString ( d, format );
		List<String> v = StringUtil.breakStringList ( time_date, " ",	StringUtil.DELIM_SKIP_BLANKS );
		setYear ( Integer.parseInt(v.get(0)) );
		setMonth ( Integer.parseInt(v.get(1)) );
		setDay ( Integer.parseInt(v.get(2)) );
		setHour ( Integer.parseInt(v.get(3)) );
		setMinute ( Integer.parseInt(v.get(4)) );
		setSecond ( Integer.parseInt(v.get(5)) );
		// milliseconds not supported in formatTimeString.
		// Convert from milliseconds to 100ths of a second.
		// setHSecond ( Integer.parseInt((String)v.elementAt(6))/10 );
		// setTimeZone ( (String)v.elementAt(7) );
		__tz = "";
	}

	// Set the time zone.  Use TimeUtil directly to increase performance.

	if ( (behavior_flag&PRECISION_TIME_ZONE) != 0 ) {
		if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ONCE ) {
			if ( !TimeUtil._local_time_zone_retrieved ) {
				// Need to initialize.
				setTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
			}
			else {
                // Use the existing data.
				setTimeZone ( TimeUtil._local_time_zone_string);
			}
		}
		else if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ALWAYS ) {
			setTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
		}
	}
	
	__behavior_flag = behavior_flag;
	setPrecision ( behavior_flag );
	reset();
	__iszero = false;
}

/**
Copy constructor.  If the incoming date is null, the date will be initialized to zero information.
@param t DateTime to copy.
*/
public DateTime ( DateTime t ) {
	if ( t != null ) {
		__nano = t.__nano;
		__second = t.__second;
		__minute = t.__minute;
		__hour = t.__hour;
		__day = t.__day;
		__month	= t.__month;
		__year = t.__year;
		__isleap = t.__isleap;
		__weekday = t.__weekday;
		__yearday = t.__yearday;
		__abs_month	= t.__abs_month;
		__behavior_flag	= t.__behavior_flag;
		__precision	= t.__precision;
		__use_time_zone	= t.__use_time_zone;
		__time_only	= t.__time_only;
		__iszero = t.__iszero;
		__tz = t.__tz;
	}
	else {
        // Constructing from null usually means that there is a code logic problem with exception handling.
		Message.printWarning ( 20, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	reset();
}

/**
Construct using another DateTime and a time zone to convert to at construction.
@param t DateTime to copy.
@param newtz Time zone to use in the resulting DateTime.
*/
@SuppressWarnings("deprecation")
public DateTime ( DateTime t, String newtz ) {
	if ( t != null ) {
		// First copy.

		__nano = t.__nano;
		__second = t.__second;
		__minute = t.__minute;
		__hour = t.__hour;
		__day = t.__day;
		__month = t.__month;
		__year = t.__year;
		__isleap = t.__isleap;
		__iszero = t.__iszero;
		__weekday = t.__weekday;
		__yearday = t.__yearday;
		__abs_month	= t.__abs_month;
		__behavior_flag	= t.__behavior_flag;
		__precision	= t.__precision;
		__use_time_zone	= t.__use_time_zone;
		__time_only	= t.__time_only;
		__tz = t.__tz;

		// Now compute the time zone offset.

		int offset = 0;
		try {
            offset = TZ.calculateOffsetMinutes ( t.__tz, newtz, this );
		}
		catch ( Exception e ) {
			// Should not happen if the system is set up correctly.
		}
		addMinute ( offset );
		setTimeZone( newtz );
	}
	else {
        // Constructing from null usually means that there is a code logic problem with exception handling.
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	reset();
}

/**
Construct using a DateTime and a behavior flag.
@param t DateTime to copy.
@param flag Constructor flags.  Because the DATE_ZERO flag will override the copy, it is ignored.
*/
public DateTime ( DateTime t, int flag ) {
	if ( t != null ) {
		__year = t.__year;
		__month	= t.__month;
		__day = t.__day;
		__hour = t.__hour;
		__minute = t.__minute;
		__second = t.__second;
		__nano = t.__nano;
		__isleap = t.__isleap;
		__weekday = t.__weekday;
		__yearday = t.__yearday;
		__abs_month	= t.__abs_month;
		__behavior_flag	= flag;
		__iszero = t.__iszero;
		// Set the initial precision.
		// The call to setPrecision() will result in the final precision and, if necessary,
		// will reset data values back to initial values to prevent remainder values
		// from being used (e.g., for plot positions, offsets).
		__precision	= t.__precision;
		__use_time_zone	= t.__use_time_zone;
		__time_only	= t.__time_only;
		// The precision may be reset here.
		setPrecision ( flag );
		__tz = t.__tz;
	}
	else {
        // Constructing from null usually means that there is a code logic problem with exception handling.
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	reset();
}

/**
Construct from a double precision number containing the date (the inverse of the toDouble method.
The number consists of YYYY.DDHHMMSS, etc., where the remainder is the fractional part of the year, based on days.
Because the relationship between months and days is dynamic,
using this routine on the difference between two dates is not generally correct, and the "use_month" option is provided.
@param double_date Date as a double.
@param use_month If true, the resulting DateTime will treat the month and day as normal.
If false, the month will be set to 0 and the days will be the total number of days in the year.
The latter version can be used when processing an absolute date offset.
@see #toDouble
*/
public DateTime ( double double_date, boolean use_month ) {
	// Initialize.

	setToZero ();

	// First get the year as the whole part of the number.
	// Because don't want to have a date like xxx 59:59:59:99 result from round-off,
	// add .1 100th of a second to the number so that the number truncates correctly.
	// .001 100th of a second as a percentage of the day is 1/86400000 (1000*100*60*60*24).

	__year = (int)double_date;
	double temp = double_date - (double)__year + .00000000011574074;

	// Get the full number of days in the year.
	
	double	ydays = (double)TimeUtil.numDaysInYear(__year);
	temp *= ydays;

	// The days is the remainder times the number of days in the year.

	int monthdays;
	boolean isleap = TimeUtil.isLeapYear ( __year );
	if ( use_month ) {
		// Use the month and day as usual.
		int [] v = TimeUtil.getMonthAndDayFromDayOfYear ( __year, (int)temp );
		if ( v != null ) {
			__month	= v[0];
			__day = v[1] + 1; // Because the day is always at least one (always in a day even if it is a fractional day).
			if ( (__month == 2) && isleap ) {
				monthdays = TimeUtil.MONTH_DAYS[__month - 1] + 1;
			}
			else {
                monthdays = TimeUtil.MONTH_DAYS[__month - 1];
			}
			if ( __day > monthdays ) {
				// Have gone into the next month by incrementing the day.
				++__month;
				__day = 1;
			}
		}
	}
	else {
	    // Set the month to zero and the days to the number of days in the year.
		__month = 0;
		__day = (int)temp;
	}

	temp -= (double)((int)temp);

	// The remainder is the hours, etc.  Multiply by 24 to get the hour.

	temp *= 24.0;
	__hour = (int)temp;
	temp -= (double)((int)temp);

	// The remainder is the minutes, etc.  Multiply by 60 to get the minute.

	temp *= 60.0;
	__minute = (int)temp;
	temp -= (double)((int)temp);

	// The remainder is the seconds, etc.  Multiply by 60 to get the seconds.

	temp *= 60.0;
	__second = (int)temp;
	temp -= (double)((int)temp);

	// The remainder is hundredths of a second, etc.  Multiply by 1,000,000,000 to get the nanoseconds.

	temp *= 1000000000.0;
	__nano= (int)temp;

	// Reset.

	reset ();
	__iszero = false;
}

/**
Construct using an OffsetDateTime and a time zone to convert to at construction.
@param t Java OffsetDateTime to use as input to create a new DateTime.
@param behaviorFlag control behavior (see bitmasks) - if <= 0 do not change from default.
Typically the behaviorFlag is used to indicate the precision of the DateTime.
@param newtz Time zone to use in the resulting DateTime.
If null or blank then it is assumed that the application is aware of time zones and is making consistent
(or that time zone is not relevant because precision is for date).
If a time zone string is specified, it is expected to be a valid Java 8 time zone appropriate for the OffsetDateTime.
For example, if a series of OffsetDateTime are being processed from a database for Mountain time zone,
they will likely be returned with time zone -07:00 for Mountain standard time part of the year
and -06:00 for Mountain daylight time part of the year.
The zone shift specified should either be a named zone such as "America/Denver" indicating
that all the date/times are local time, or specify an offset such as -07:00.
The main issue is when used over a period where time zone changes,
the time zone should be appropriate for that when output.
*/
public DateTime ( OffsetDateTime t, int behaviorFlag, String newtz ) {
	if ( t != null ) {
		// First copy.

		__nano = t.getNano();
		__second = t.getSecond();
		__minute = t.getMinute();
		__hour = t.getHour();
		__day = t.getDayOfMonth();
		__month = t.getMonthValue();
		__year = t.getYear();
		// The following are calculated with reset() call below.
		//__isleap
		//__iszero
		//__weekday
		//__yearday
		//__abs_month
		
		// The following just saves the time zone string.
		setTimeZone( newtz );
		// Reset internal data like leap year, etc.
		reset();
	}
	else {
        // Constructing from null usually means that there is a code logic problem with exception handling.
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	if ( behaviorFlag > 0 ) {
		__behavior_flag = behaviorFlag;
		setPrecision(behaviorFlag);
	}
	reset();
}

/**
Construct using an Instant and a time zone to convert to at construction.
@param instant Java Instant to use as input to create a new DateTime.
@param behaviorFlag control behavior (see bitmasks) - if <= 0 do not change from default.
Typically the behaviorFlag is used to indicate the precision of the DateTime.
@param tz the time zone to use, if null use the local time
*/
public DateTime ( Instant instant, int behaviorFlag, String tz ) {
	if ( instant != null ) {
		// Create a ZonedDateTime.
		ZoneId zone = null;
		if ( tz == null ) {
			zone = ZoneId.systemDefault();
		}
		else {
			zone = ZoneId.of(tz);
		}
		ZonedDateTime zdt = instant.atZone( zone );

		__nano = zdt.getNano();
		__second = zdt.getSecond();
		__minute = zdt.getMinute();
		__hour = zdt.getHour();
		__day = zdt.getDayOfMonth();
		__month = zdt.getMonthValue();
		__year = zdt.getYear();
		// The following are calculated with reset() call below.
		//__isleap
		//__iszero
		//__weekday
		//__yearday
		//__abs_month
		
		// The following just saves the time zone string.
		setTimeZone( tz );
		// Reset internal data like leap year, etc.
		reset();
	}
	else {
        // Constructing from null usually means that there is a code logic problem with exception handling.
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	if ( behaviorFlag > 0 ) {
		__behavior_flag = behaviorFlag;
		setPrecision(behaviorFlag);
	}
	reset();
}

/**
Add a date offset to the date.
This is accomplished by adding the smallest unit of time first to allow for resets of larger units.
It is therefore important that all time components are zero except for the values to be added.
This will be the case if subtract() was used to compute the offset.
Negative offsets are allowed.
It may be desirable to overload this method to use the date precision or automatically
process the precision (enhancement for later).
Also, although the month value will be added if non-zero,
it is recommended that only days be specified (and days > 31 is allowed).
The output from subtract() will default to setting month to zero and is therefore compatible with this method.
<b>Currently the precision of the instance is not considered.
Therefore, the offset fields should be set to zero if not used.</b>
@param offset Date offset to add.
*/
public void add ( DateTime offset ) {
	// Add the values in increasing order of size.

	if ( offset == null ) {
		Message.printWarning ( 2, "DateTime.add", "Null offset" );
		return;
	}

	addNanosecond ( offset.__nano );
	addSecond ( offset.__second );
	addMinute ( offset.__minute );
	addHour ( offset.__hour );
	addDay ( offset.__day );
	addMonth ( offset.__month );
	addYear ( offset.__year );
	__iszero = false;
}

/**
Add day(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of days to add (can be a multiple and can be negative).
*/
public void addDay ( int add ) {
	int i;

	if ( add == 1 ) {
		int num_days_in_month = TimeUtil.numDaysInMonth (__month, __year);
		++__day;
		if ( __day > num_days_in_month ) {
			// Have gone into the next month.
			__day -= num_days_in_month;
			addMonth( 1 );
		}
		// Reset the private data members.
		setYearDay();
	}
	// Else.
	// Figure out if trying to add more than one day.
	// If so, recurse (might be a faster way, but this works).
	else if ( add > 0 ) {
		for ( i = 0; i < add; i++ ) {
			addDay ( 1 );
		}
	}
	else if ( add == -1 ) {
		--__day;
		if ( __day < 1 ) {
			// Have gone into the previous month.
			// Temporarily set day to 1, determine the day and year, and then set the day.
			__day = 1;
			addMonth( -1 );
			__day = TimeUtil.numDaysInMonth( __month, __year );
		}
		// Reset the private data members.
		setYearDay();
	}
	else if ( add < 0 ) {
		for ( i = add; i < 0; i++ ){
			addDay ( -1 );
		}
	}
	__iszero = false;
}

/**
Add hour(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of hours to add (can be a multiple and can be negative).
*/
public void addHour ( int add ) {
	int	daystoadd;

	// First add the days, if necessary.

	if ( add >= 24 || add <= -24 ) {
		// First need to add/subtract days to time.
		daystoadd = add/24;
		addDay( daystoadd );
	}

	// Add the remainder.

	if ( add > 0 ) {
		__hour += (add%24);
		if ( __hour > 23 ) {
			// Have gone into the next day.
			__hour -= 24;
			addDay( 1 );
		}
	}
	else if ( add < 0 ) {
		__hour += (add%24);
		if ( __hour < 0 ) {
			// Have gone into the previous day.
			__hour += 24;
			addDay( -1 );
		}
	}
	__iszero = false;
}

/**
Add hundredth-second(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of hundredth-seconds to add (can be a multiple and can be negative).
*/
public void addHSecond ( int add ) {
	// Call the nanosecond function.
	addNanosecond(add*10000000);
}

/**
Add a time series interval to the DateTime (see TimeInterval).  This is useful when iterating a date.
An irregular interval is ignored (the date is not changed).
@param interval time series base interval
@param add multiple of the base interval, can be negative
*/
public void addInterval ( int interval, int add ) {
	if ( add == 0 ) {
		return;
	}
	// Based on the interval, call lower-level routines.
	if( interval == TimeInterval.SECOND ) {
		addSecond( add );
	}
	else if( interval == TimeInterval.MINUTE ) {
		addMinute( add );
	}
	else if( interval == TimeInterval.HOUR ) {
		addHour( add );
    }
    else if ( interval == TimeInterval.DAY ) {
        addDay( add);
    }
    else if ( interval == TimeInterval.WEEK ) {
		addDay( 7*add);
    }
	else if ( interval == TimeInterval.MONTH ) {
		addMonth( add);
    }
    else if ( interval == TimeInterval.YEAR ) {
		addYear( add);
    }
    else if ( interval == TimeInterval.IRREGULAR ) {
		return;
    }
    else {
        // Unsupported interval.
        // TODO SAM 2007-12-20 Evaluate throwing InvalidTimeIntervalException.
        String message = "Interval " + interval + " is unsupported";
		Message.printWarning ( 2, "DateTime.addInterval", message );
		return;
    }
	__iszero = false;
}

/**
Add a time series interval to the DateTime (see TimeInterval).
This is useful when incrementing a date/time using event data relative to a starting date/time.
An irregular interval is ignored (the date is not changed).
@param interval Time series base interval, SECOND to DAY.
@param add multiple of the base interval to add, can be a fractional number
@exception UnsupportedTimeIntervalException if not SECOND to DAY interval
*/
public void addInterval ( int interval, double add ) throws UnsupportedTimeIntervalException {
	// Based on the interval, call lower-level routines:
	// - the whole number addition is handled immediate
	// - the fractional addition is handled by chaining to other functions until seconds are processed

	if ( interval == TimeInterval.SECOND ) {
		// May include fraction of seconds.
		// Add the whole seconds.
		int seconds = (int)add;
		//Message.printStatus(2, "addInterval", "Adding " + seconds + " seconds.");
		addSecond( seconds );
		// Add the fractional seconds:
		// - make sure it is not over 999999999 due to internal math
		int nano = (int)((add - (double)seconds)*1000000000);
		if ( nano > 999999999 ) {
			nano = 999999999;
		}
		//Message.printStatus(2, "addInterval", "Adding " + nano + " nanoseconds.");
		addNanosecond(nano);
	}
	else if ( interval == TimeInterval.MINUTE ) {
		// May include fraction of minutes.
		// Add the whole minutes.
		int minutes = (int)add;
		//Message.printStatus(2, "addInterval", "Adding " + minutes + " minutes.");
		addMinute( minutes );
		// Add the fractional minutes, converting to whole seconds and fraction of seconds.
		double seconds = (add - (double)minutes)*60.0;
		//Message.printStatus(2, "addInterval", "Adding " + seconds + " seconds.");
		addInterval(TimeInterval.SECOND,seconds);
	}
	else if ( interval == TimeInterval.HOUR ) {
		// May include fraction of hours.
		// Add the whole hours.
		int hours = (int)add;
		addHour( hours );
		addInterval(TimeInterval.MINUTE,(add - (double)hours)*60.0);
    }
    else if ( interval == TimeInterval.DAY ) {
		// May include fraction of days.
    	// Add the whole days.
		int days = (int)add;
        addDay( days);
		addInterval(TimeInterval.HOUR,(add - (double)days)*24.0);
    }
    else {
        // Unsupported interval.
        // TODO SAM 2007-12-20 Evaluate throwing InvalidTimeIntervalException
    	throw new UnsupportedTimeIntervalException("Interval " + interval + " is not supported.");
    }
	__iszero = false;
}

/**
Add minute(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of minutes to add (can be a multiple and can be negative).
*/
public void addMinute ( int add ) {
	int	hrs;

	if ( add == 0 ) {
		return;
	}

	// First see if multiple hours need to be added.

	if ( add >= 60 || add <= -60 ) {
		// Need to add/subtract hour(s) first.
		hrs = add/60;
		addHour( hrs );
	}

	if ( add > 0 ) {
		__minute += add % 60;
		if ( __minute > 59 ) {
			// Need to add an hour and subtract the same from minute.
			__minute -= 60;
			addHour( 1 );
		}
	}
	else if ( add < 0 ) {
		__minute += add % 60;
		if ( __minute < 0 ) {
			// Need to subtract an hour and add the same to minute.
			__minute += 60;
			addHour( -1 );
		}
	}
	__iszero = false;
}

/**
Add month(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of months to add (can be a multiple and can be negative).
*/
public void addMonth ( int add ) {
	int	i;

	if ( add == 0 ) {
		return;
	}

	if ( add == 1 ) {
		// Dealing with one month.
		__month += add;
		// Have added one month so check if went into the next year.
		if ( __month > 12 ) {
			// Have gone into the next year.
			__month = 1;
			addYear( 1 );
		}
	}
	// Else.
	// Loop through the number to add/subtract.
	// Use recursion because multi-month increments are infrequent
	// and the overhead of the multi-month checks is probably a wash.
	else if ( add > 0 ) {
		for ( i = 0; i < add; i++ ) {
			addMonth ( 1 );
		}
		// No need to reset because it was done int the previous call.
		return;
	}
	else if ( add == -1 ) {
		--__month;
		// Have subtracted the specified number so check if in the previous year.
		if ( __month < 1 ) {
			// Have gone into the previous year.
			__month = 12;
			addYear( -1 );
		}
	}
	else if ( add < 0 ) {
		for ( i = 0; i > add; i-- ) {
			addMonth ( -1 );
		}
		// No need to reset because it was done in the previous call.
		return;
	}
	else {
	    // Zero.
		return;
	}
	// Reset the date.
	setAbsoluteMonth();
	setYearDay();
	__iszero = false;
}

/**
Add nanosecond(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of nanoseconds to add (can be a multiple and can be negative).
*/
public void addNanosecond ( int add ) {
	int	secs;

	if ( add == 0 ) {
		return;
	}

	// First add to the second if necessary.

	if ( add >= 1000000000 || add <= -1000000000 ) {
		// Need to add/subtract seconds first.
		secs = add/1000000000;
		addSecond( secs );
	}

	if ( add > 0 ) {
		// Add the remainder of the whole number.
		__nano += add%1000000000;
		if ( __nano > 999999999 ) {
			// Need to add a second and subtract the same from nano.
			__nano -= 1000000000;
			addSecond( 1 );
		}
	}
	else if ( add < 0 ) {
		// Subtract the remainder of the whole number.
		__nano += add % 1000000000;
		if ( __nano < 0 ) {
			// Need to subtract a second and add the same to nano.
			__nano += 1000000000;
			addSecond( -1 );
		}
	}
	__iszero = false;
}

/**
Add second(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of seconds to add (can be a multiple and can be negative).
*/
public void addSecond ( int add ) {
	int	mins;

	if ( add == 0 ) {
		return;
	}

	// Add/subtract minutes, if necessary.

	if ( add >= 60 || add <= -60 ) {
		// Need to add/subtract minute(s) first.
		mins = add/60;
		addMinute( mins );
	}

	if ( add > 0 ) {
		__second += add % 60;
		if ( __second > 59 ) {
			// Need to add a minute and subtract the same from second.
			__second -= 60;
			addMinute( 1 );
		}
	}
	else if ( add < 0 ) {
		__second += add % 60;
		if ( __second < 0 ) {
			// Need to subtract a minute and add the same to second.
			__second += 60;
			addMinute( -1 );
		}
	}
	__iszero = false;
}

/**
Add week(s) to the DateTime.
@param add Indicates the number of weeks to add (can be a multiple and can be negative).
*/
public void addWeek ( int add ) {
	if ( add == 0 ) {
		return;
	}
	addDay ( add*7 );
}

// TODO SAM 2007-12-20 Evaluate what to do about adding a year if on Feb 29.
/**
Add year(s) to the DateTime.  The month and day are NOT adjusted if an
inconsistency occurs with leap year information.
@param add Indicates the number of years to add (can be a multiple and can be negative).
*/
public void addYear ( int add ) {
	if ( add == 0 ) {
		return;
	}
	__year += add;
	reset ();
	__iszero = false;
}

/**
 * Returns whether this date is within the specified date range, inclusive of the end-points.
 *
 * @param startDate beginning of date range
 * @param endDate end of date range
 */
public boolean between(DateTime startDate, DateTime endDate) {
    if (this.greaterThanOrEqualTo(startDate) && this.lessThanOrEqualTo(endDate)) {
        return true;
    }
    else {
        return false;
    }
}

/**
Clone the object.  The TS base class clone() method is called (all DateTime data are primitive).
@return a complete deep copy.
*/
public Object clone () {
	try {
        DateTime d = (DateTime)super.clone();
		return d;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is clone-able.
		throw new InternalError();
	}
}

/**
Determine if this DateTime is less than, equal to, or greater than another DateTime.
@return -1 if this DateTime is less than "t", 0 if this DateTime is the same as
"t", and 1 if this DateTime is greater than "t".
@param t Date to compare.
*/
public int compareTo ( DateTime t ) {
	if ( equals(t) ) {
		return 0;
	}
	else if ( lessThan(t) ) {
		return -1;
	}
	else {
		return 1;
	}
}

@Override
public boolean equals(Object o) {
    if (o instanceof DateTime) {
        return equals((DateTime) o);
    } else {
        return false;
    }
}

@Override
public int hashCode() {
    int hash = 3;
    hash = 67 * hash + this.__nano;
    hash = 67 * hash + this.__second;
    hash = 67 * hash + this.__minute;
    hash = 67 * hash + this.__hour;
    hash = 67 * hash + this.__day;
    hash = 67 * hash + this.__month;
    hash = 67 * hash + this.__year;
    hash = 67 * hash + (this.__tz != null ? this.__tz.hashCode() : 0);
    hash = 67 * hash + (this.__isleap ? 1 : 0);
    hash = 67 * hash + (this.__iszero ? 1 : 0);
    hash = 67 * hash + this.__precision;
    hash = 67 * hash + this.__behavior_flag;
    hash = 67 * hash + (this.__use_time_zone ? 1 : 0);
    return hash;
}

/**
Determine if a DateTime is equal to this instance, considering date, and time to the hundredth of a second.
The date precisions are considered in the comparison.
<b>If the instance is a time only (no date), then only the time data are compared.</b>
Time zone is not currently checked but may be checked in the future if the PRECISION_TIME_ZONE flag is set.
@return true if the date is the same as the instance.
@param t DateTime to compare.
*/
public boolean equals ( DateTime t ) {
	return equals ( t, __precision );
	// TODO SAM 2005-02-24 should the code from the overloaded method be inlined here to improve performance?
	// It does not seem that equals is used in iterations quite as much as other methods.
	// Don't inline the code for now.
}

/**
Determine if a DateTimes is equal to this instance.
<b>If the instance is a time only (no date), then only the time data are compared.</b>
Time zone is checked if it has been set for the instance.
@return true if the date is equivalent to the given precision.
@param precision Indicates the precision to use for the comparison.
*/
public boolean equals ( DateTime t, int precision ) {
	// Maybe can't do this because are more concerned with precision?
	//if ( isZero() != t.isZero() ) {
	//	return false;
	//}
	if ( !__time_only ) {
		if ( __year != t.__year ) {
			return false;
		}
		if ( precision == PRECISION_YEAR ) {
			if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
				return false;
			}
			return true;
		}
		if ( __month != t.__month ) {
			return false;
		}
		if ( precision == PRECISION_MONTH ) {
			if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
				return false;
			}
			return true;
		}
		if ( __day != t.__day ) {
			return false;
		}
		if ( precision == PRECISION_DAY ) {
			if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
				return false;
			}
			return true;
		}
	}
	if ( __hour != t.__hour ) {
		return false;
	}
	if ( precision == PRECISION_HOUR ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	if ( __minute != t.__minute ) {
		return false;
	}
	if ( precision == PRECISION_MINUTE ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	if ( __second != t.__second ) {
		return false;
	}
	if ( precision == PRECISION_SECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	// Check the higher precision values.
	// Hundreds of a second.
	if ( __nano/10000000 != t.__nano/10000000 ) {
		return false;
	}
	if ( precision == PRECISION_HSECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	// Milliseconds.
	if ( __nano/1000000 != t.__nano/1000000 ) {
		return false;
	}
	if ( precision == PRECISION_MICROSECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	// Microseconds.
	if ( __nano/1000 != t.__nano/1000 ) {
		return false;
	}
	if ( precision == PRECISION_MILLISECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}
	// Nanoseconds.
	if ( __nano != t.__nano ) {
		return false;
	}
	if ( precision == PRECISION_NANOSECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}

	// They are not equal.

	return false;
}

/**
Return the absolute day.
@return The absolute day.  This is a computed value.
@see RTi.Util.Time.TimeUtil#absoluteDay
*/
public int getAbsoluteDay() {
	return TimeUtil.absoluteDay ( __year, __month, __day );
}

/**
Return the absolute month.
@return The absolute month (year*12 + month).
*/
public int getAbsoluteMonth( ) {
	// Since some data are public, recompute.
	return (__year*12 + __month);
}

/**
Return the DateTime behavior flag.  Note that the higher bits of the behavior flag can be checked easily.
However, the precision must be determined from the behavior flag by disaggregating the flag.
Use getPrecision() to get an exact value for the precision.
@return The behavior flag (bit mask).
*/
public long getBehaviorFlag () {
	return __behavior_flag;
}

/**
Return the Java Date corresponding to the DateTime, using date/time values as is and time zone GMT (ignores time zone set for DateTime).
This is appropriate when the DateTime does not have time zone set (for example when precision is day or larger).
or time zone is not important (for example absolute difference between two date/times in same time zone).
@return Java Date corresponding to the DateTime, ignoring the time zone.
*/
public Date getDateForTimeZoneGMT () {
	GregorianCalendar c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
	// The following will work in any case because GMT will be recognized and if not GMT is returned by default.
	java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
	c.setTimeZone(tz);
	return c.getTime();
}

/**
Return the Java Date corresponding to the DateTime, using the specified time zone.
This should be called, for example, when the time zone in the object was not set but should be applied
when constructing the returned Date OR, when the time zone in the object should be ignored in favor
of the specified time zone.
An alternative that will modify the DateTime instance is to call setTimeZone() and then getDate().
@param tzId time zone string recognized by TimeZone.getTimeZone(), for example "America/Denver" or "MST".
@return Java Date corresponding to the DateTime.
@exception RuntimeException if there is no time zone set but defaultTimeZone = TimeZoneDefaultType.NONE
*/
public Date getDate ( String tzId ) {
	GregorianCalendar c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
	// Above is already in the default time zone.
	//Message.printStatus(2,"","Calendar after initialization with data:  " + c);
	if ( !TimeUtil.isValidTimeZone(tzId) ) {
		// The following will throw an exception in daylight savings time because "MDT" is not a valid time zone
		// (it is a display name for "MST" when in daylight savings).
		// The check is needed because java.util.TimeZone.getTimeZone() will return GMT if an invalid time zone.
		throw new RuntimeException ( "Time zone (" + __tz + ") in DateTime object is invalid - cannot return Date object." );
	}
	java.util.TimeZone tz = java.util.TimeZone.getTimeZone(tzId);
	// But this resets the time zone without changing the data so should be OK.
	c.setTimeZone(tz);
	//Message.printStatus(2,"","Calendar after setting time zone:  " + c);
	return c.getTime(); // This returns the UNIX time considering how the date/time was set above.
}

/**
Return the Java Date corresponding to the DateTime, using time zone set for the DateTime object.
This should be called, for example, when the time zone in the object has not been set and it is clear what the default should be.
@param defaultTimeZone indicates how to behave if the time zone is not set in the DateTime object.
TimeZoneDefaultType.LOCAL should match the legacy behavior, which was relying on the Calendar to set the
time zone to the default.
TimeZoneDefaultType.GMT can be used to treat the DateTime as GMT, which is appropriate if time zone is not relevant.
@return Java Date corresponding to the DateTime, ignoring the time zone.
@exception RuntimeException if there is no time zone set but defaultTimeZone = TimeZoneDefaultType.NONE
*/
public Date getDate ( TimeZoneDefaultType defaultTimeZone ) {
	GregorianCalendar c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
	java.util.TimeZone tz = null;
	if ( (__tz != null) && (!__tz.isEmpty()) ) {
		// Time zone is specified in object so use it.
		// Make sure time zone is recognized in the Java world because if not recognized GMT is assumed.
		// Hopefully the following is fast - otherwise will need to create a static array in TimeUtil.
		if ( !TimeUtil.isValidTimeZone(__tz) ) {
			// The following will throw an exception in daylight savings time because "MDT" is not a valid time zone
			// (it is a display name for "MST" when in daylight savings).
			throw new RuntimeException ( "Time zone (" + __tz + ") in DateTime object is invalid.  Cannot determine Java Date." );
		}
		// The following will now work.  Without the above check GMT is returned if the timezone is not found.
		tz = java.util.TimeZone.getTimeZone(__tz);
		c.setTimeZone(tz);
	}
	else {
		// No time zone in the object so default.
		if ( defaultTimeZone == TimeZoneDefaultType.LOCAL ) {
			c.setTimeZone(java.util.TimeZone.getDefault());
		}
		else if ( defaultTimeZone == TimeZoneDefaultType.GMT ) {
			tz = java.util.TimeZone.getTimeZone("GMT");
			c.setTimeZone(tz);
		}
		else if ( (defaultTimeZone == null) || (defaultTimeZone == TimeZoneDefaultType.NONE) ) {
			// No default allowed.
			throw new RuntimeException ( "Time zone in DateTime object is blank but default time zone is not allowed." );
		}
	}
	return c.getTime();
}

/**
Return the Java Date corresponding to the DateTime.
@return Java Date corresponding to the DateTime or null if unable to determine conversion.
@param flag Indicates whether time zone should be shifted from the DateTime to the Date time zone, currently ignored.
*/
/* Legacy method replaced by other getDate variations - this was always using the local time zone, which is not
 * what should be done for some time series because they have no time zone
 * (essentially local standard time or GMT or irrelevant because day, month, year data).
public Date getDate ( int flag ) {
	GregorianCalendar c = null;
	if ( (flag & DATE_STRICT) != 0 ) {
		// Do care what the time zone is.  Make the returned date exactly match the DateTime, but in GMT.
		// For now, just do the same for both cases.
		c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
		return c.getTime();
	}
	else {
        // Don't care about the time zone.  Just use the other data fields.
		c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
		return c.getTime();
	}
}
*/

/**
Return the day.
@return The day.
*/
public int getDay( ) {
	return __day;
}

/**
Return the hour.
@return The hour.
*/
public int getHour( ) {
	return __hour;
}

/**
Return the 100-th second. This is the number of nanoseconds divided by 10000000.
@return The hundredth-second.
*/
public int getHSecond( ) {
	return __nano/10000000;
}

/**
Return the minute.
@return The minute.
*/
public int getMinute( ) {
	return __minute;
}

/**
Return the month.
@return The month.
*/
public int getMonth( ) {
	return __month;
}

/**
Return the nano seconds.  This is a fraction of second, not nano-seconds in absolute time.
@return The nano seconds.
*/
public int getNanoSecond( ) {
	return __nano;
}

/**
Return the DateTime precision.
@return The precision (see PRECISION*).
*/
public int getPrecision () {
	return __precision;
}

/**
Return the second.
@return The second.
*/
public int getSecond( ) {
	return __second;
}

/**
Return the time zone abbreviation.
@return The time zone abbreviation.
*/
public String getTimeZoneAbbreviation ( ) {
	return __tz;
}

/**
Return the week day by returning getDate(TimeZoneDefaultType.GMT).getDay().
@return The week day (Sunday is 0).
*/
@SuppressWarnings("deprecation")
public int getWeekDay ( ) {
	// Always recompute because don't know if DateTime was copied and modified.
	// Does not matter what timezone because internal date/time values are used in absolute sense.
	__weekday = getDate(TimeZoneDefaultType.GMT).getDay();
    return __weekday;
}

/**
Return the year.
@return The year.
*/
public int getYear( ) {
	return __year;
}

/**
Return the Julian day in the year.
@return The day of the year where Jan 1 is 1.  If the behavior of the DateTime
is DATE_FAST, zero is likely to be returned because the day of the year is
not automatically recomputed.
*/
public int getYearDay() {
	// Need to set it.
	setYearDay();
	return __yearday;
}

/**
Determine if the instance is greater than another date.
Time zone is not considered in the comparison (no time zone shift is made).
The comparison is made at the precision of this instance instance.
@return true if the instance is greater than the given date.
@param t DateTime to compare.
*/
public boolean greaterThan ( DateTime t ) {
	return greaterThan ( t, this.__precision);
}

/**
Determine if the instance is greater than another date.
Time zone is not considered in the comparison (no time zone shift is made).
The comparison is made at the specified precision.
@return true if the instance is greater than the given date.
@param t DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean greaterThan ( DateTime t, int precision ) {
	// Inline the code to increase performance.
	if ( !__time_only ) {
		if ( __year < t.__year) {
			return false;
		}
		else {
            if(__year > t.__year) {
				return true;
			}
		}
	
		if ( precision == PRECISION_YEAR ) {
			// Equal so return false.
			return false;
		}

		// Otherwise years are equal so check months.
	
		if(__month < t.__month) {
			return false;
		}
		else {
            if(__month > t.__month) {
				return true;
			}
		}

		if ( precision == PRECISION_MONTH ) {
			// Equal so return false.
			return false;
		}

		// Months must be equal so check day.

		if (__day < t.__day) {
			return false;
		}
		else {
            if(__day > t.__day) {
				return true;
			}
		}

		if ( precision == PRECISION_DAY ) {
			// Equal so return false.
			return false;
		}
	}

	// Days are equal so check hour.

	if (__hour < t.__hour) {
		return false;
	}
	else {
        if(__hour > t.__hour) {
			return true;
		}
	}

	if ( precision == PRECISION_HOUR ) {
		// Equal so return false.
		return false;
	}

	// Means that hours match - so check minute.

	if( __minute < t.__minute ) {
		return false;
	}
	else {
        if( __minute > t.__minute ) {
			return true;
		}
	}

	if ( precision == PRECISION_MINUTE ) {
		// Equal so return false.
		return false;
	}

	// Means that minutes match - so check second.

	if( __second < t.__second ) {
		return false;
	}
	else {
        if( __second > t.__second ) {
			return true;
		}
	}

	if ( precision == PRECISION_SECOND ) {
		// Equal so return false.
		return false;
	}

	// Means that seconds match - so check hundredths of second.

	if( __nano/10000000 < t.__nano/10000000 ) {
		return false;
	}
	else {
        if( __nano/10000000 > t.__nano/10000000 ) {
			return true;
		}
	}

	if ( precision == PRECISION_HSECOND ) {
		// Equal so return false.
		return false;
	}

	// Means that hseconds match - so check millisecond.

	if( __nano/1000000 < t.__nano/1000000 ) {
		return false;
	}
	else {
        if( __nano/1000000 > t.__nano/1000000 ) {
			return true;
		}
	}

	if ( precision == PRECISION_MILLISECOND ) {
		// Equal so return false.
		return false;
	}

	// Means that milliseconds match - so check microsecond.

	if( __nano/1000 < t.__nano/1000 ) {
		return false;
	}
	else {
        if( __nano/1000 > t.__nano/1000 ) {
			return true;
		}
	}

	if ( precision == PRECISION_MICROSECOND ) {
		// Equal so return false.
		return false;
	}

	// Means that microseconds match - so check nanoseconds.

	if( __nano < t.__nano ) {
		return false;
	}
	else {
        if( __nano > t.__nano ) {
			return true;
		}
	}

	// Means they are equal.

	return false;
}

/**
Determine if the DateTime is >= another DateTime.
Time zone is not considered in the comparison (no time zone shift is made).
@return true if the instance is >= the given DateTime.
@param d DateTime to compare.
*/
public boolean greaterThanOrEqualTo ( DateTime d ) {
	if ( !lessThan(d) ) {
		return true;
	}
	else {
        return false;
	}
}

/**
Determine if the DateTime is >= another DateTime.
Time zone is not considered in the comparison (no time zone shift is made).
@return true if the instance is >= the given DateTime.
@param d DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean greaterThanOrEqualTo ( DateTime d, int precision ) {
	if ( !lessThan(d, precision) ) {
		return true;
	}
	else {
        return false;
	}
}

/**
Indicate whether a leap year.
@return true if a leap year.
*/
public boolean isLeapYear ( ) {
	// Reset to make sure.
	__isleap = TimeUtil.isLeapYear( __year );
    return __isleap;
}

/**
Indicate whether a zero DateTime, meaning a DateTime that was created as a zero date and never modified.
@return true if data are initialized to zero values, without further changes.
*/
public boolean isZero ( ) {
	if ( !__iszero ) {
		// Something has been modified.
		return __iszero;
	}
	else {
        // Check here whether anything is different from the default.
		// This will only be a performance hit if the DateTime never has anything changed.
		if ( (__year != 0) ||
			(__month != 1) ||
			(__day != 1) ||
			(__hour != 0) ||
			(__minute != 0) ||
			(__second != 0) ||
			(__nano != 0) ) {
			__iszero = false;
		}
	}
	return __iszero;
}

/**
Determine if the DateTime is less than another DateTime.
Time zone is not considered in the comparison (no time zone shift is made).
The precision of the instance is used for the comparison.
@return true if the instance is less than the given DateTime.
@param t DateTime to compare.
*/
public boolean lessThan ( DateTime t ) {
	return lessThan ( t, this.__precision);
}

/**
Determine if the DateTime is less than another DateTime.
Time zone is not considered in the comparison (no time zone shift is made).
The specified precision is used for the comparison.
@return true if the instance is less than the given DateTime.
@param t DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean lessThan ( DateTime t, int precision ) {
	// Inline the overall code and comparisons here even though could
	// call other methods because would have to call greaterThan() and equals() to know for sure.
	if ( !__time_only ) {
		if( __year < t.__year) {
			return true;
		}
		else {
            if(__year > t.__year) {
				return false;
			}
		}
	
		if ( precision == PRECISION_YEAR ) {
			// Equal so return false.
			return false;
		}

		// Otherwise years are equal so check months.
	
		if(__month < t.__month) {
			return true;
		}
		else {
            if(__month > t.__month) {
				return false;
			}
		}
	
		if ( precision == PRECISION_MONTH ) {
			// Equal so return false.
			return false;
		}

		// Months must be equal so check day.
	
		if (__day < t.__day) {
			return true;
		}
		else {
            if(__day > t.__day) {
				return false;
			}
		}

		if ( precision == PRECISION_DAY ) {
			// Equal so return false.
			return false;
		}
	}

	// Days are equal so check hour.

	if (__hour < t.__hour) {
		return true;
	}
	else {
        if(__hour > t.__hour) {
			return false;
		}
	}

	if ( precision == PRECISION_HOUR ) {
		// Equal so return false.
		return false;
	}

	// Hours are equal so check minutes.

	if( __minute < t.__minute ) {
		return true;
	}
	else {
        if( __minute > t.__minute ) {
			return false;
		}
	}

	if ( precision == PRECISION_MINUTE ) {
		// Equal so return false.
		return false;
	}

	// Means that minutes match - so check second.

	if( __second < t.__second ) {
		return true;
	}
	else {
        if( __second > t.__second ) {
			return false;
		}
	}

	if ( precision == PRECISION_SECOND ) {
		// Equal so return false.
		return false;
	}

	// Means that seconds match - so check hundredths of seconds.

	if( __nano/10000000 < t.__nano/10000000 ) {
		return true;
	}
	else {
        if( __nano/10000000 > t.__nano/10000000 ) {
			return false;
		}
	}

	if ( precision == PRECISION_HSECOND ) {
		// Equal so return false.
		return false;
	}
	
	// Means that hseconds match - so check milliseconds.

	if( __nano/1000000 < t.__nano/1000000 ) {
		return true;
	}
	else {
        if( __nano/1000000 > t.__nano/1000000 ) {
			return false;
		}
	}

	if ( precision == PRECISION_MILLISECOND ) {
		// Equal so return false.
		return false;
	}
	
	// Means that milliseconds match - so check microseconds.

	if( __nano/1000 < t.__nano/1000 ) {
		return true;
	}
	else {
        if( __nano/1000 > t.__nano/1000 ) {
			return false;
		}
	}

	if ( precision == PRECISION_MICROSECOND ) {
		// Equal so return false.
		return false;
	}
	
	// Check nanoseconds.

	if( __nano < t.__nano ) {
		return true;
	}
	else {
        if( __nano > t.__nano ) {
			return false;
		}
	}

	// Everything must be equal so not less than.

	return false;
}

/**
Determine if the DateTime is <= another.
Time zone is not considered in the comparison (no time zone shift is made).
@return true if the DateTime instance is less than or equal to given DateTime.
@param d DateTime to compare.
*/
public boolean lessThanOrEqualTo ( DateTime d ) {
	return !greaterThan(d);
}

/**
Determine if the DateTime is <= another.
Time zone is not considered in the comparison (no time zone shift is made).
@return true if the DateTime instance is less than or equal to given DateTime.
@param d DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean lessThanOrEqualTo ( DateTime d, int precision ) {
	if ( !greaterThan(d,precision) ) {
		return true;
	}
	else {
        return false;
	}
}

/**
Parse a string and initialize a DateTime.
By default time zone will be set but the PRECISION_TIME_ZONE flag will be set to false.
If only a time format is detected, then the TIME_ONLY flag will be set in the returned instance.
This routine is the inverse of toString() for simple DateTimes.
The string can be of the following form:
<pre>
YYYY-MM-DD (or any other valid DateTime format - this will result in calling
the other parse method - date/time strings cannot be mixed with the strings
described below)

CurrentToMinute	(current DateTime to the indicated precision)
...
CurrentToYear

YearStartToMinute	(current year start to the indicated precision)
...
YearStartToMonth

YearEndToMinute	(current year end to the indicated precision)
...
YearEndToMonth

MonthStartToMinute (similar for MonthEnd...)
...
MonthStartToDay

DayStartToMinute (similar for DayEnd...)
...
DayStartToHour

HourStartToMinute (similar for HourEnd...)

Recognize any named DateTime passed in via the datetime_props parameter.

NamedDateTime - Interval (e.g., "CurrentToHour - 6Hour")
NamedDateTime + Interval (e.g., "CurrentToHour + 6Hour")
</pre>
@return A DateTime corresponding to the date.
@param dtString Any of the formats supported by parse(String,int).
@param datetime_props Named DateTime instances that are to be recognized when parsing the string.
For example, an application may internally have a parameter called InputStart, which is referenced in the string.
If parsing only for syntax (where the value of the parsed result is not important), specify any instance of DateTime.<p>
The String value for the named DateTime is parsed, even though contents may be available. <p>
The named date/time instances <u>cannot</u> contain "+" or "-" characters.
@exception IllegalArgumentException If the string is not understood due to a bad date/time,
interval string or a missing named date/time.
@see #toString
*/
public static DateTime parse ( String dtString, PropList datetime_props ) {
	if (dtString == null) {
		Message.printWarning(3, "DateTime.parse", "Cannot get DateTime from null string.");
		throw new IllegalArgumentException("Null DateTime string to parse.");
	}

	String str = dtString.trim();

	if (str.length() == 0) {
		Message.printWarning(3, "DateTime.parse", "Cannot get DateTime from empty string.");
		throw new IllegalArgumentException("Empty DateTime string to parse.");
	}		

	if (Character.isDigit(dtString.charAt(0))) {
		// If the first character is a number then assume that this is a DateTime string that should be parsed as normal.
		// There is no support for parsing things like:
		//    "2005-10-12 10:13 + 15Minute"
		return DateTime.parse(dtString);
	}
	
	// Else parse special values like CurrentToMinute.
	
	String[] tokens = new String[3];
	// tokens[0] = the date represented by the first part of the string to
	//             be parsed (e.g., CurrentToMinute, "DateProperty", etc)
	// tokens[1] = the operator ("+" or "-").  Null if no operators.
	// tokens[2] = the interval to adjust be (e.g., "15Minute").  Null if no operator.
	// This assumes that no + or - are part of the date/time
	
	if (str.indexOf("-") > -1) {
		int index = str.indexOf("-");
		tokens[0] = str.substring(0, index).trim();
		tokens[1] = "-";
		tokens[2] = str.substring(index + 1).trim();
	}
	else if (str.indexOf("+") > -1) {
		int index = str.indexOf("+");
		tokens[0] = str.substring(0, index).trim();
		tokens[1] = "+";
		tokens[2] = str.substring(index + 1).trim();
	}
	else {
		// If neither a plus or minus was found, assume a single Date Variable to be parsed.
		tokens[0] = str;
		tokens[1] = null;
		tokens[2] = null;
	}

	// Try to find a match in the PropList and substitute with the value stored there.
	// Values in the PropList can override any of the hard-coded values (e.g., "CurrentToMinute").
	int size = 0;
	if (datetime_props == null) {
		size = 0;
	}
	else {
		size = datetime_props.size();
	}

	boolean matched = false;
	Prop prop = null;
	String value = null;

	for (int i = 0; i < size; i++) {
		prop = datetime_props.propAt(i);
		if (prop.getKey().equalsIgnoreCase(tokens[0])) {
			matched = true;
			value = prop.getValue();
			break;
		}
	}

	DateTime token0DateTime = null;
	if (matched) {
		if (value == null) {
			Message.printWarning(3, "DateTime.parse",
                    "Named date/time '" + tokens[0] + "' to be parsed, but its value is null.");
			throw new IllegalArgumentException("Null value for named date/time property '" + tokens[0] + "'");
		}
		else {
			// Parse the named date/time string. Allow an exception to be thrown.
			token0DateTime = DateTime.parse(value);
		}
	}
	else {
		// Baseline DateTime is the current DateTime.
		token0DateTime = new DateTime(DateTime.DATE_CURRENT);
	
		// Try to parse as one of the hard-coded values (CurrentToMinute, etc).
		String token0 = tokens[0];
		if (token0.toUpperCase().startsWith("CURRENTTOSECOND")) {
			token0DateTime.setPrecision(DateTime.PRECISION_SECOND);
		}
		else if (token0.toUpperCase().startsWith("CURRENTTOMINUTE")) {
			token0DateTime.setPrecision(DateTime.PRECISION_MINUTE);
		}
		else if (token0.toUpperCase().startsWith("CURRENTTOHOUR")) {
			token0DateTime.setPrecision(DateTime.PRECISION_HOUR);
		}
		else if (token0.toUpperCase().startsWith("CURRENTTODAY")) {
			token0DateTime.setPrecision(DateTime.PRECISION_DAY);
			// Don't use time zone.
			token0DateTime.setTimeZone("");
		}
		else if (token0.toUpperCase().startsWith("CURRENTTOMONTH")) {
			token0DateTime.setPrecision(DateTime.PRECISION_MONTH);
			// Don't use time zone.
			token0DateTime.setTimeZone("");
		}
		else if (token0.toUpperCase().startsWith("CURRENTTOYEAR")) {
			token0DateTime.setPrecision(DateTime.PRECISION_YEAR);
			// Don't use time zone.
			token0DateTime.setTimeZone("");
		}
		else {
			String message = "Requested special date/time value \"" + token0 + "\" is not recognized - cannot parse.";
			Message.printWarning(3, "DateTime.parse",message);
			throw new IllegalArgumentException(message);
		}
		// All "Current" versions set the time zone for intervals of hour or smaller.
		// Evaluate whether the above have modifiers such as CurrentToDay.round(5min).timezone()
		int modifierPos = 0;
		int modifierStartPos = 0;
		int modifierEndPos = 0;
		int roundDirection = -1; // Earlier in time.
		String roundDayOfWeek = null; // Day of week to round day to.
		TimeInterval roundInterval = null; // Interval to round to.
		while ( true ) {
			// Process as many modifiers as found until the string ends.
			if ( modifierPos >= token0.length() ) {
				break;
			}
			modifierPos = token0.indexOf(".",modifierPos);
			if ( modifierPos < 0 ) {
				// No more modifiers.
				break;
			}
			else {
				// Process the modifiers:
				// - order is not important
				// - if multiple modifiers work together, apply at the end
				if ( token0.substring(modifierPos).toUpperCase().startsWith(".ROUND(") ) {
					// Date/time needs to be rounded.
					modifierStartPos = modifierPos + 7; // Skip over .ROUND(
					modifierEndPos = token0.indexOf(")",modifierPos);
					String sRoundInterval = token0.substring(modifierStartPos, modifierEndPos);
					roundInterval = TimeInterval.parseInterval(sRoundInterval);
					modifierPos = modifierEndPos;
				}
				else if ( token0.substring(modifierPos).toUpperCase().startsWith(".ROUNDDIRECTION(") ) {
					// Direction of rounding:
					// - check this before modifiers that may depend on it
					modifierStartPos = modifierPos + 16; // Skip over .ROUNDDIRECTION(
					modifierEndPos = token0.indexOf(")",modifierPos);
					String sRoundDirection = token0.substring(modifierStartPos, modifierEndPos).trim();
					if ( sRoundDirection.trim().equals(">") ) { // Don't use + or - here because that is used in may on current time.
						roundDirection = 1;
					}
					else if ( sRoundDirection.trim().equals("<") ) { // Don't use + or - here because that is used in may on current time.
						roundDirection = -1; // Also the default set above if nothing matches.
					}
					modifierPos = modifierEndPos;
				}
				else if ( token0.substring(modifierPos).toUpperCase().startsWith(".ROUNDTODAYOFWEEK(") ) {
					// Date/time needs to be rounded to a day of the week:
					// - only the day is rounded
					// - direction is also considered
					// - the rounding occurs after checking modifiers
					modifierStartPos = modifierPos + 18; // Skip over .ROUNDTODAYOFWEEK(
					modifierEndPos = token0.indexOf(")",modifierPos);
					roundDayOfWeek = token0.substring(modifierStartPos, modifierEndPos);
					modifierPos = modifierEndPos;
				}
				else if ( token0.substring(modifierPos).toUpperCase().startsWith(".TIMEZONE(") ) {
					// Date/time needs timezone set.
					// The default from above is to set the timezone to local.
					modifierStartPos = modifierPos + 10; // Skip over .TIMEZONE(
					modifierEndPos = token0.indexOf(")",modifierPos);
					String tz = token0.substring(modifierStartPos, modifierEndPos).trim();
					if ( tz.equalsIgnoreCase("local") ) {
						// Set timezone to local using computer time zone (redundant with default but code is being evaluated).
						// Look up always in case it has changed during processing (unlikely).
						TimeUtil.getLocalTimeZoneAbbr(TimeUtil.LOOKUP_TIME_ZONE_ALWAYS);
					}
					else {
						// Set to the specified time zone, and setting to blank is OK.
						token0DateTime.setTimeZone(tz);
					}
					modifierPos = modifierEndPos;
				}
				else {
					// At least increment so don't stay in loop if not matched.
					++modifierPos;
				}
			}
		}
		if ( roundInterval != null ) {
			token0DateTime.round(roundDirection, roundInterval.getBase(), roundInterval.getMultiplier());
		}
		if ( roundDayOfWeek != null ) {
			// This can be done in addition to the above rounding.
			token0DateTime.roundToDayOfWeek(roundDirection, roundDayOfWeek, -1);
		}
	}

	if (tokens[1] == null) {
		// No operator, so the DateTime can be returned as is.
		return token0DateTime;
	}

	// Allow an exception to be thrown.
	TimeInterval ti = null;
	try {
	    ti = TimeInterval.parseInterval(tokens[2]);
	}
	catch ( Exception e ) {
	    throw new IllegalArgumentException ( "Invalid interval (" + tokens[2] + ") in date/time string." );
	}

	if (tokens[1].equals("-")) {
		// Subtract an interval.
		token0DateTime.addInterval(ti.getBase(), -1 * ti.getMultiplier());
	}
	else {
		// Add an interval.  Already checked above for "+" or "-" so don't need to check for anything else here.
		token0DateTime.addInterval(ti.getBase(), ti.getMultiplier());
	}
	//Message.printStatus(2,"","Date/time after parse:  " + token0DateTime.toString(FORMAT_VERBOSE));
	return token0DateTime;
}

/**
Parse a string and initialize a DateTime.
The time zone will be set by default but the PRECISION_TIME_ZONE flag will be set to false meaning that the time zone is not used.
If only a time format is detected, then the TIME_ONLY flag will be set in the returned instance.
This routine is the inverse of toString().
@param dateString A date/time string in any of the formats supported by parse(String,int).
The format will be automatically detected based on the contents of the string.
If more specific handling is needed, use the method version that accepts a format specifier.
@return A DateTime instance corresponding to the specified date/time string.
@exception IllegalArgumentException If the string is not understood.
@see #toString
*/
public static DateTime parse ( String dateTimeString ) {
	int	length = 0;
	char c;	// Use to optimize code below.
	int dl = 10; // Debug level.

	// First check to make sure that there is something to parse.
	if( dateTimeString == null ) {
		if ( Message.isDebugOn ) {
			// May be called by TimeUtil.isDateTime() so don't fill up the log file.
			Message.printWarning( 3, "DateTime.parse", "Cannot get DateTime from null string." );
		}
		throw new IllegalArgumentException ( "Null DateTime string to parse." );
	}
	// Length with time zone (if it is provided).
	length = dateTimeString.length();
	if( length == 0 ) {
		if ( Message.isDebugOn ) {
			// May be called by TimeUtil.isDateTime() so don't fill up the log file.
			Message.printWarning( 3, "DateTime.parse", "Cannot get DateTime from zero-length string." );
		}
		throw new IllegalArgumentException ( "Empty DateTime string to parse." );
	}
	
	// Determine if a timezone is included in the string.  The following forms are handled below:
	//   2010-07-01T03:16:11-06:00   - ISO 8601 format, see: https://en.wikipedia.org/wiki/ISO_8601
	//   2000-01-01 00 GMT-8.0       - used in TSTool build-in formats
	String timeZone = null;
	//String dateStringNoTimeZone = dateTimeString; // Assume no time zone and reset below if time zone is found.
	//int lengthNoTimeZone = length;

	// Check for the ISO 8601 form of date/time string:
	// - must have "T" in the string
	// - no spaces in the string
	if ( (dateTimeString.indexOf('T') > 0) && (dateTimeString.indexOf(' ') < 0) ) {
		/*
		// If the string ends in Z and preceding character is a digit it is UTC time.
		if ( dateTimeString.charAt((dateTimeString.length() - 1)) == 'Z' ) {
			timeZone = "Z";
			dateStringNoTimeZone = dateTimeString.substring(0, (dateTimeString.length() - 1));
		}
		else {
			// Let the remaining parse code get out as much information as possible.
			// - use the full string as previously initialized
			// - TODO smalers 2021-08-01 does the 'timeZone' need to be set here?
		}
		*/
		// Use the specific parser.
		int format = FORMAT_ISO_8601;
		return parse(dateTimeString, format);
	}
	// Else parse using code that has worked for a long time below:
	// - TODO smalers 2022-01-30 actually, the following did not work well with the above, remove when ready
	/*
	else {
		// Check for the second form of date/time string.
		// Try to determine if there is a time zone based on whether there is a space and then character at the end,
		// for example:
		//   2000-01-01 00 GMT-8.0
		// This will work except if the string had AM, PM, etc., but that has never been handled anyhow.
		// This also assumes that standard time zones are used, which will start with a character string (not number)
		// and don't themselves include spaces.
		// TODO SAM 2016-05-02 need to handle date/time format strings - maybe deal with in Java 8.
		int lastSpacePos = dateTimeString.lastIndexOf(' ');
		
		if ( lastSpacePos > 0 ) {
			timeZone = dateTimeString.substring(lastSpacePos).trim();
			if ( timeZone.length() == 0 ) {
				// Don't actually have anything at the end of the string.
				timeZone = null;
			}
			else {
				if ( !Character.isLetter(timeZone.charAt(0)) ) {
					// Assume that end is not a time zone (could just be the time specified after a space).
					timeZone = null;
				}
				if ( timeZone != null ) {
					// Actually had the time zone so save some data to help with parsing.
					dateStringNoTimeZone = dateTimeString.substring(0,lastSpacePos).trim();
					lengthNoTimeZone = dateStringNoTimeZone.length();
				}
			}
		}
	}
	*/
	
	if ( Message.isDebugOn ) {
		Message.printDebug(dl, "DateTime.parse", "Parsing \"" + dateTimeString + "\" length=" + length);
	}

	// This if-elseif structure is used to determine the format of the date represented by dtString.
	// All of these parse the string without time zone.  If time zone was detected, it is added at the end.
	// TODO SAM 2016-05-02 need to remove some cases now previously checked for time zone now that time zone is checked above.
	// The legacy code assumed 3-digit time zone but now longer time zone is accepted.
	DateTime dateTime = null;
	if ( length == 4 ) {
		// The date is: YYYY
		dateTime = parse( dateTimeString, FORMAT_YYYY, 0 );
	}
	else if ( length == 5 ) {
		// The date is:
		//   MM/DD
		//   MM-DD
		//   HH:mm
		// Don't allow:
		//   MM/YY
		//
		c = dateTimeString.charAt ( 2 );
		if ( c == ':' ) {
			dateTime = parse( dateTimeString, FORMAT_HH_mm, 0 );
		}
		else if ( (c == '/') || (c == '-') ) {
			// The following will work for both.
			dateTime = parse( dateTimeString, FORMAT_MM_SLASH_DD, 0 );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + dateTimeString + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + dateTimeString + "\"" );
		}
	}
	else if ( length == 6 ) {
		// The date is: M/YYYY
		if ( dateTimeString.charAt(1) == '/') {
			dateTime = parse(" "+ dateTimeString, FORMAT_MM_SLASH_YYYY,0);
		}
		else {
			Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + dateTimeString + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + dateTimeString + "\"" );
		}
	}
	else if ( length == 7 ) {
		// The date is:
		//   YYYY-MM
		//   MM/YYYY
		if ( dateTimeString.charAt(2) == '/' ) {
			dateTime = parse( dateTimeString, FORMAT_MM_SLASH_YYYY, 0 );
		}
		else {
			dateTime = parse( dateTimeString, FORMAT_YYYY_MM, 0 );
		}
	}
	else if ( length == 8 ) {
		if ( (dateTimeString.charAt(2) == '/') && (dateTimeString.charAt(5) == '/') ) {
			// The date is: MM/DD/YY
			dateTime = parse(dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YY, 0 );
		}
		else if ( (dateTimeString.charAt(1) == '/') && (dateTimeString.charAt(3) == '/') ) {
			// The date is: M/D/YYYY
			dateTime = parse(dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY, 8 );
		}
		else if ( StringUtil.isInteger(dateTimeString) ) {
			// Assume: YYYYMMDD
			dateTime = parse(dateTimeString, FORMAT_YYYYMMDD, 0 );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + dateTimeString + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + dateTimeString + "\"" );
		}
	}
	else if ( length == 9 ) {
		if ( (dateTimeString.charAt(2) == '/') && (dateTimeString.charAt(4) == '/') ) {
			// The date is: MM/D/YYYY
			dateTime = parse(dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY, 9 );
		}
		else if ( (dateTimeString.charAt(1) == '/') && (dateTimeString.charAt(4) == '/') ) {
			// The date is: M/DD/YYYY
			dateTime = parse(dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY, -9 );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + dateTimeString + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + dateTimeString + "\"" );
		}
	}
	else if ( length == 10 ) {
		// The date is:
		//   MM/DD/YYYY
		//   YYYY-MM-DD
		if ( dateTimeString.charAt(2) == '/' ) {
			dateTime = parse( dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY, 0 );
		}
		else {
			dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD, 0 );
		}
	}
    // Length 11 might be YYYYMMDDHmm, but this is not currently allowed.
	else if ( length == 12 ) {
		// The date is: YYYYMMDDHHmm
		dateTime = parse( dateTimeString, FORMAT_YYYYMMDDHHmm, 0 );
	}
	else if ( length == 13 ) {
		// The date is:
		//    YYYY-MM-DD HH
		//    MM/DD/YYYY HH
		//    MM-DD-YYYY HH
		if ( dateTimeString.charAt(2) == '/' ) {
			dateTime = parse( dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY_HH, 0 );
		}
		else if ( dateTimeString.charAt(2) == '-' ) {
			dateTime = parse( dateTimeString, FORMAT_MM_DD_YYYY_HH, 0 );
		}
		else {
			dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH, 0 );
		}
	}
	else if ( (length > 14) && Character.isLetter(dateTimeString.charAt(14) ) ) {
		// The date is:  YYYY-MM-DD HH Z...
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_ZZZ, 0 );
	}
	else if ( length == 15 ) {
		// The date is: YYYY-MM-DD HHmm
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HHmm, 0 );
	}
	else if ( length == 16 ) {
		// The date is:
		//   YYYY-MM-DD HH:mm
		//   MM/DD/YYYY HH:mm
		if ( dateTimeString.charAt(2) == '/' ) {
			dateTime = parse( dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm, 0 );
		}
		else {
			dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm,0);
		}
	}
	else if ( (length > 17) && Character.isLetter(dateTimeString.charAt(17) ) ) {
		// The date is: YYYY-MM-DD HH:MM Z...
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_ZZZ, 0 );
	}
	else if ( length == 19 ) {
		// The date is:
		//   YYYY-MM-DD HH:mm:SS
		//   MM/DD/YYYY HH:mm:SS
        if ( dateTimeString.charAt(2) == '/' ) {
        	dateTime = parse( dateTimeString, FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS, 0 );
		}
		else {
			dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS, 0 );
        }
	}
	else if ( (length == 22) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) ) {
		// The date is:
		//    YYYY-MM-DD HH:mm:SS:hh
		//    YYYY-MM-DD HH:mm:SS.hh
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_hh, 0 );
	}
	else if ( (length == 23) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) ) {
		// Put this before the >= length checks.
		// The date is:
		//    YYYY-MM-DD HH:mm:SS:sss
		//    YYYY-MM-DD HH:mm:SS.sss
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI, 0 );
	}
	else if ( (length == 26) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) ) {
		// The date is:
		//    YYYY-MM-DD HH:mm:SS:ssssss
		//    YYYY-MM-DD HH:mm:SS.ssssss
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO, 0 );
	}
	else if ( (length == 29) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) ) {
		// The date is:
		//    YYYY-MM-DD HH:mm:SS:sssssssss
		//    YYYY-MM-DD HH:mm:SS.sssssssss
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_NANO, 0 );
	}
    else if ( (length >= 30) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) && dateTimeString.charAt(29) == ' ' ) {
        // The date is:
    	//   YYYY-MM-DD HH:mm:SS:sssssssss ZZZ...
    	//   YYYY-MM-DD HH:mm:SS.sssssssss ZZZ...
        dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_NANO_ZZZ, 0 );
    }
    else if ( (length >= 27) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) && dateTimeString.charAt(26) == ' ' ) {
        // The date is:
    	//   YYYY-MM-DD HH:mm:SS:ssssss ZZZ...
    	//   YYYY-MM-DD HH:mm:SS.ssssss ZZZ...
        dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO_ZZZ, 0 );
    }
    else if ( (length >= 24) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) && dateTimeString.charAt(23) == ' ' ) {
        // The date is:
    	//   YYYY-MM-DD HH:mm:SS:sss ZZZ...
    	//   YYYY-MM-DD HH:mm:SS.sss ZZZ...
        dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI_ZZZ, 0 );
    }
    else if ( (length >= 23) && ((dateTimeString.charAt(19) == ':') || (dateTimeString.charAt(19) == '.')) && dateTimeString.charAt(22) == ' ' ) {
        // The date is:
    	//   YYYY-MM-DD HH:mm:SS:hh ZZZ...
    	//   YYYY-MM-DD HH:mm:SS.hh ZZZ...
        dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ, 0 );
    }
	else if ( (length >= 23) && dateTimeString.charAt(19) == ' ' ) {
		// The date/time is:
		//    YYYY-MM-DD HH:mm:SS ZZZ...
		//    YYYY-MM-DDTHH:mm:SS ZZZ...
		dateTime = parse( dateTimeString, FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ, 0);
	}
	else {
	    // Unknown format so throw an exception.
		throw new IllegalArgumentException ( "Date/time string \"" + dateTimeString +
			"\" format is not auto-recognized - may need to specify format." );
	}
	
	if ( dateTime == null ) {
		// Fall through... was not parsed.
		throw new IllegalArgumentException ( "Date/time string \"" + dateTimeString +
			"\" format is not auto-recognized - may need to specify format." );
	}
	// 2021-08-23 smalers latest code parses time zone as ISO 8601 or other formats.
	/* Time zone is set when the string is parsed.
	if ( timeZone == null ) {
		timeZone = "";
	}
	// Set the time zone to what was specified in the string.
	// If no time zone was specified then blank is used.
	dateTime.setTimeZone(timeZone);
	*/
	return dateTime;
}

/**
Parse a string and initialize a DateTime.
The calling code must specify the proper format for parsing.
This routine therefore has limited use but is relatively fast.
The precision for the date is set according to the format
(the precision is set to the smallest time interval used in the format).
This routine is the inverse of toString(int format).
@return A DateTime corresponding to the date.
@param dtString A string representation of a date/time.
@param format Date format (see FORMAT_*).
@exception IllegalArgumentException If there is an error parsing the date string.
@see #toString
*/
public static DateTime parse ( String dtString, int format ) {
	// Call the overloaded method with no special flag.
	return parse ( dtString, format, 0 );
}

/**
Parse a string and initialize a DateTime.  The calling code must specify the proper format for parsing.
This routine therefore has limited use but is relatively fast.
The precision for the date is set according to the format
(the precision is set to the smallest time interval used in the format).
This routine is the inverse of toString(int format).
@return A DateTime corresponding to the date.
@param dtString A string representation of a date/time.
@param format Date format (see FORMAT_*).
@exception IllegalArgumentException If there is an error parsing the date string.
@param flag A flag to use internally.  If > 0, this is used by some
internal code to indicate variations in formats.  For example, MM/DD/YYYY,
MM/D/YYYY, M/DD/YYYY, M/D/YYYY are all variations on the same format.
@see #toString
*/
private static DateTime parse ( String dtString, int format, int flag ) {
	int dl = 50;
	// Use to improve performance of checks at end of the method:
	// - use booleans rather than doing repeated bit mask checks
	boolean is_year = false,
			is_month = false,
			is_day = false,
			is_hour = false,
			is_minute = false;
	DateTime date = null;
	String routine = "DateTime.parse";
	List<Object> v = null;

	// Note that if the fixedRead routine has problems, it will just return zeros for the integers.
	// This allows defaults for the smaller date/time fields.

	if ( Message.isDebugOn ) {
		Message.printDebug(dl,routine, "Trying to parse string \"" + dtString + "\" using format " + format );
	}

	if ( format == FORMAT_DD_SLASH_MM_SLASH_YYYY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		// Various flavors of the format based on whether one or two digits are used for the month and day.
		if ( flag == 0 ) {
			v = StringUtil.fixedRead ( dtString, "i2x1i2x1i4" );
		}
		else if ( flag == 8 ) {
			v = StringUtil.fixedRead ( dtString, "i1x1i1x1i4" );
		}
		else if ( flag == 9 ) {
			v = StringUtil.fixedRead ( dtString, "i2x1i1x1i4" );
		}
		else if ( flag == -9 ) {
			v = StringUtil.fixedRead ( dtString, "i1x1i2x1i4" );
		}
		date.__day = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE | TIME_ONLY );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i2x1i2" );
		date.__hour = ((Integer)v.get(0)).intValue();
		date.__minute = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_HHmm ) {
		date = new DateTime ( PRECISION_MINUTE | TIME_ONLY );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i2i2" );
		date.__hour = ((Integer)v.get(0)).intValue();
		date.__minute = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_MM ) {
		date = new DateTime ( PRECISION_MONTH );
		is_month = true;
		v = StringUtil.fixedRead ( dtString, "i2" );
		date.__month = ((Integer)v.get(0)).intValue();
	}
	else if ( (format == FORMAT_MM_DD) || (format == FORMAT_MM_SLASH_DD) ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( dtString, "i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		// Various flavors of the format based on whether one or two digits are used for the month and day.
		if ( flag == 0 ) {
			v = StringUtil.fixedRead ( dtString, "i2x1i2x1i4" );
		}
		else if ( flag == 8 ) {
			v = StringUtil.fixedRead ( dtString, "i1x1i1x1i4" );
		}
		else if ( flag == 9 ) {
			v = StringUtil.fixedRead ( dtString, "i2x1i1x1i4" );
		}
		else if ( flag == -9 ) {
			v = StringUtil.fixedRead ( dtString, "i1x1i2x1i4" );
		}
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( dtString, "i2x1i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( (format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH) || (format == FORMAT_MM_DD_YYYY_HH) ) {
		date = new DateTime (PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( dtString, "i2x1i2x1i4x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		if ( dtString.length() < 16 ) {
		    // The date string is not padded with zeros.
			// Parse the string into its parts and then reform to a zero-padded string.
			// Use primitive formatting to increase performance.
		    String [] sarray = dtString.split("[/ :]" );
		    String monthPad= "", dayPad = "", hourPad = "", minutePad = "";
		    if ( (sarray != null) && (sarray.length > 4) ) {
		        // Assume that have all the needed parts.
		        if ( sarray[0].length() == 1 ) {
		            monthPad = "0";
		        }
                if ( sarray[1].length() == 1 ) {
                    dayPad = "0";
                }
                if ( sarray[3].length() == 1 ) {
                    hourPad = "0";
                }
                if ( sarray[4].length() == 1 ) {
                    minutePad = "0";
                }
                dtString = monthPad + sarray[0] + "/" + dayPad + sarray[1] + "/" +
                    sarray[2] + " " + hourPad + sarray[3] + ":" + minutePad + sarray[4];
		    }
		}
		v = StringUtil.fixedRead ( dtString, "i2x1i2x1i4x1i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
    else if (format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS) {
		date = new DateTime (PRECISION_SECOND );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i2x1i2x1i4x1i2x1i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_YYYY ) {
		date = new DateTime ( PRECISION_MONTH );
		is_month = true;
		if ( dtString.length() == 6 ) {
			v = StringUtil.fixedRead ( dtString, "i1x1i4" );
		}
		else {
			// Expect a length of 7.
			v = StringUtil.fixedRead ( dtString, "i2x1i4" );
		}
		date.__month = ((Integer)v.get(0)).intValue();
		date.__year = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_YYYY ) {
		date = new DateTime ( PRECISION_YEAR );
		is_year = true;
		v = StringUtil.fixedRead ( dtString, "i4" );
		date.__year = ((Integer)v.get(0)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM ) {
		date = new DateTime ( PRECISION_MONTH );
		is_month = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_YYYYMMDD ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( dtString, "i4i2i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH ) {
		date = new DateTime (PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_ZZZ ) {
		// YYYY-MM-DD hh ZZZ...
		date = new DateTime ( PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.setTimeZone ( dtString.substring(13).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYYMMDDHHmm ) {
		date = new DateTime (PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i4i2i2i2i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HHmm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS ) {
		date = new DateTime ( PRECISION_SECOND );
		v = StringUtil.fixedRead ( dtString,"i4x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh ) {
		date = new DateTime (PRECISION_HSECOND );
		v = StringUtil.fixedRead ( dtString,"i4x1i2x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*10000000;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI ) {
		// YYYY-MM-DDThh:mm:ss:xxx
		// YYYY-MM-DDThh:mm:ss.xxx
		date = new DateTime ( PRECISION_MILLISECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*1000000;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO ) {
		// YYYY-MM-DDThh:mm:ss:xxxxxx
		// YYYY-MM-DDThh:mm:ss.xxxxxx
		date = new DateTime ( PRECISION_MICROSECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i6" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*1000;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_NANO ) {
		// YYYY-MM-DDThh:mm:ss:xxxxxxxxx
		// YYYY-MM-DDThh:mm:ss.xxxxxxxxx
		date = new DateTime ( PRECISION_NANOSECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i9" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue();
		//Message.printStatus(2, routine, "Parsing FORMAT_YYYY_MM_DD_HH_mm_SS_NANO: " + date);
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_ZZZ ) {
		// YYYY-MM-DDTHH ZZZ...
		date = new DateTime ( PRECISION_HOUR );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.setTimeZone ( dtString.substring(13).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_ZZZ ) {
		// YYYY-MM-DDThh:mm ZZZ...
		date = new DateTime ( PRECISION_MINUTE );
		v = StringUtil.fixedRead ( dtString,"i4x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.setTimeZone ( dtString.substring(16).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ ) {
		// YYYY-MM-DDThh:mm:ss ZZZ...
		date = new DateTime ( PRECISION_SECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.setTimeZone ( dtString.substring(19).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ ) {
		// YYYY-MM-DDThh:mm:ss:xx ZZZ...
		// YYYY-MM-DDThh:mm:ss.xx ZZZ...
		date = new DateTime ( PRECISION_HSECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*10000000;
		date.setTimeZone ( dtString.substring(23).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI_ZZZ ) {
		// YYYY-MM-DDThh:mm:ss:xxx ZZZ...
		// YYYY-MM-DDThh:mm:ss.xxx ZZZ...
		date = new DateTime ( PRECISION_MILLISECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*1000000;
		date.setTimeZone ( dtString.substring(24).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO_ZZZ ) {
		// YYYY-MM-DDThh:mm:ss:xxxxxx ZZZ...
		// YYYY-MM-DDThh:mm:ss.xxxxxx ZZZ...
		date = new DateTime ( PRECISION_MICROSECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i6" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue()*1000;
		date.setTimeZone ( dtString.substring(27).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_NANO_ZZZ ) {
		// YYYY-MM-DDThh:mm:ss:xxxxxxxxx ZZZ...
		// YYYY-MM-DDThh:mm:ss.xxxxxxxxx ZZZ...
		date = new DateTime ( PRECISION_NANOSECOND );
		v = StringUtil.fixedRead ( dtString, "i4x1i2x1i2x1i2x1i2x1i2x1i9" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__nano = ((Integer)v.get(6)).intValue();
		date.setTimeZone ( dtString.substring(30).trim() );
	}
	else if ( format == FORMAT_ISO_8601 ) {
		// ISO 8601 formats:
		// - see:  https://en.wikipedia.org/wiki/ISO_8601
		// - do not support weeks
		// - cannot rely on something like OffsetDateTime to parse because don't know if time zone is included, etc.
		// Could have a variety of formats:
		// Date: 2017-06-30
		// Various date/time, possibly with date, time, fractional seconds, and time zone:
		// 2017-06-30T23:03:33Z
		// 2017-06-30T23:03:33+01:00
		// 2017-06-30T23:03:33-01:00
		// 2017-06-30T23:03:33.12-01:00
		// 2017-06-30T23:03:33.123-01:00
		// 2017-06-30T23:03:33.123456-01:00
		// 2017-06-30T23:03:33.123456789-01:00
		// 2017-06-30T23:03:33.123456789
		// 20170630T230333Z
		// 20170630T230333+0100
		// 20170630T230333+01
		// Ordinal date:  2017-181 (not yet supported below)
		// Date without year:  -06-30 (not yet supported below)
		//Message.printStatus(2, routine, "Processing date/time string \"" + dtString + "\"");
		int posT = dtString.indexOf("T");
		String d = null;
		String t = null;
		if ( posT > 0 ) {
			// Date and time.
			d = dtString.substring(0, posT);  // Before T.
			t = dtString.substring(posT + 1); // After T.
		}
		else {
			// Only date so no need to deal with time zone.
			d = dtString;
		}
		int dateLen = d.length();
		// Instantiate date/time to full precision, but will set precision more specifically below as it is determined.
		if ( (d != null) && (t != null) ) {
			// Full date/time.
			date = new DateTime ( PRECISION_HSECOND );
		}
		else if ( (d != null) && (t == null) ) {
			// Only Date.
			date = new DateTime ( PRECISION_DAY );
		}
		if ( d != null ) {
			String yearFormat = "i4";
			String monthFormat = null;
			String dayFormat = null;
			// Assume have delimiter for lengths and if not reset lengths below.
			int yearLen = 4;
			int monthLen = 7;
			int dayLen = 10;
			if ( d.indexOf("-") >= 0 ) {
				monthFormat = "i4x1i2";
				dayFormat = "i4x1i2x1i2";
			}
			else {
				monthFormat = "i4i2";
				monthLen = 6;
				dayFormat = "i4i2i2";
				dayLen = 8;
			}
			// Date fields are delimited by dash and may be truncated.
			if ( dateLen == yearLen ) {
				v = StringUtil.fixedRead ( d, yearFormat );
				date.__year = ((Integer)v.get(0)).intValue();
				date.setPrecision(DateTime.PRECISION_YEAR);				
			}
			else if ( dateLen == monthLen ) {
				v = StringUtil.fixedRead ( d, monthFormat );
				date.__year = ((Integer)v.get(0)).intValue();
				date.__month = ((Integer)v.get(1)).intValue();
				date.setPrecision(DateTime.PRECISION_MONTH);
			}
			else if ( dateLen == dayLen ) {
				v = StringUtil.fixedRead ( d, dayFormat );
				date.__year = ((Integer)v.get(0)).intValue();
				date.__month = ((Integer)v.get(1)).intValue();
				date.__day = ((Integer)v.get(2)).intValue();
				date.setPrecision(DateTime.PRECISION_DAY);
			}
			else {
				throw new IllegalArgumentException ( "Don't know how to parse \"" + dtString + "\" date \"" + d + "\" using ISO 8601." );
			}
		}
		if ( t != null ) {
			int timeLen = t.length();
			String hourFormat = "i2";
			String minuteFormat = null;
			// Assume have delimiter for lengths and if not reset lengths below.
			int hourLen = 2;
			int minuteLen = 5;
			int colonOffset = 1; // Used when processing seconds below.
			if ( t.indexOf(":") >= 0 ) {
				minuteFormat = "i2x1i2";
			}
			else {
				minuteFormat = "i4i2";
				minuteLen = 4;
				colonOffset = 0;
			}
			// Time fields are delimited by colon and may be truncated:
			// - read hour and minute using fixed read and then read second and time zone handling variable length
			date.__tz = ""; // Time zone unknown
			if ( timeLen >= minuteLen ) {
				v = StringUtil.fixedRead ( t, minuteFormat );
				date.__hour = ((Integer)v.get(0)).intValue();
				date.__minute = ((Integer)v.get(1)).intValue();
				date.setPrecision(DateTime.PRECISION_MINUTE);
			}
			else if ( timeLen >= hourLen ) {
				v = StringUtil.fixedRead ( t, hourFormat );
				date.__hour = ((Integer)v.get(0)).intValue();
				date.setPrecision(DateTime.PRECISION_HOUR);				
			}
			else {
				throw new IllegalArgumentException ( "Don't know how to parse \"" + dtString + "\" time \"" + t + "\" using ISO 8601." );
			}
			if ( timeLen > minuteLen ) {
				// Have to parse seconds and/or time zone.
				String secAndTz = t.substring(minuteLen + colonOffset); // +1 is to skip :
				//Message.printStatus(2, routine, "processing seconds and/or time zone in \"" + secAndTz + "\"");
				// See if time zone is specified, which will start with +, -, or Z
				String secString = "";
				int posZ = secAndTz.indexOf("Z");
				if ( posZ < 0 ) {
					posZ = secAndTz.indexOf("+");
				}
				if ( posZ < 0 ) {
					posZ = secAndTz.indexOf("-");
				}
				if ( posZ < 0 ) {
					// Default will be blank.
					date.setTimeZone("");
					secString = secAndTz;
				}
				else {
					// Have time zone, use as is.
					date.setTimeZone(secAndTz.substring(posZ));
					date.setPrecision(DateTime.PRECISION_TIME_ZONE);
					date.__use_time_zone = true;
					secString = secAndTz.substring(0,posZ);
				}
				// Figure out the seconds, which will be between the minute and time zone.
				//Message.printStatus(2, routine, "Parsing second string \"" + secString + "\"");
				if ( !secString.isEmpty() ) {
					// Have seconds.  Check for fractional seconds.
					int posPeriod = secString.indexOf(".");
					if ( posPeriod > 0 ) { // Not >= because expect seconds in front of decimal so 0 is not allowed.
						date.setSecond(Integer.parseInt(secString.substring(0,posPeriod)));
						// DateTime class recognizes nanoseconds so handle up to 9 digits.
						String fracsecString = null;
						if ( posPeriod == (secString.length() - 1) ) {
							// Special case of decimal but no trailing digits, treat as zero.
							fracsecString = "0";
						}
						else {
							// Get the string after the period.
							fracsecString = secString.substring(posPeriod + 1);
						}
						int fracLen = fracsecString.length();
						//if ( Message.isDebugOn ) {
						//	Message.printDebug(dl,routine, "fracsecString = \"" + fracsecString + "\" fracLen = " + fracLen );
						//}
						if ( fracLen >= 7 ) {
							// Anything more precise than microseconds:
							// - treating as nanoseconds may result in zeros at the end
							date.setPrecision(DateTime.PRECISION_NANOSECOND);
							// First get the string from input.
							String nanoString = fracsecString.substring(0, fracLen);
							if ( fracLen > 9 ) {
								// Truncate to 9 digits for nanoseconds, which is the highest precision to handle.
								nanoString = fracsecString.substring(0,9);
							}
							else {
								// Pad with zeros at the end to indicate nanoseconds.
								nanoString = StringUtil.pad ( fracsecString, 9, "0", StringUtil.PAD_BACK );
							}
							date.setNanoSecond(Integer.parseInt(nanoString));
							//Message.printStatus(2, routine, "After parsing fractional seconds \"" + fracsecString + "\", nanoseconds = " + date.getNanoSecond());
						}
						else if ( (fracLen > 3) && (fracLen <= 6) ) {
							// Anything more precise than milliseconds up to microseconds:
							// - treating as microseconds may result in zeros at the end
							date.setPrecision(DateTime.PRECISION_MICROSECOND);
							String nanoString = fracsecString.substring(0, fracLen);
							// Pad with zeros at the end to indicate nanoseconds.
							nanoString = StringUtil.pad ( fracsecString, 9, "0", StringUtil.PAD_BACK );
							date.setNanoSecond(Integer.parseInt(nanoString));
							//Message.printStatus(2, routine, "After parsing fractional seconds \"" + fracsecString + "\", nanoseconds = " + date.getNanoSecond());
						}
						else if ( fracLen == 3 ) {
							// Handle as exactly millisecond precision.
							date.setPrecision(DateTime.PRECISION_MILLISECOND);
							fracsecString = fracsecString.substring(0, 3);
							// Pad with zeros at the end to indicate nanoseconds.
							String nanoString = StringUtil.pad ( fracsecString, 9, "0", StringUtil.PAD_BACK );
							date.setNanoSecond(Integer.parseInt(nanoString));
							//Message.printStatus(2, routine, "After parsing fractional seconds \"" + fracsecString + "\", nanoseconds = " + date.getNanoSecond());
						}
						else {
							// 1-2 fractional digits.
							// Use hundredths.
							date.setPrecision(DateTime.PRECISION_HSECOND);
							fracsecString = fracsecString.substring(0, fracLen);
							// Pad with zeros at the end to indicate nanoseconds.
							String nanoString = StringUtil.pad ( fracsecString, 9, "0", StringUtil.PAD_BACK );
							date.setNanoSecond(Integer.parseInt(nanoString));
							//Message.printStatus(2, routine, "After parsing fractional seconds \"" + fracsecString + "\", nanoseconds = " + date.getNanoSecond());
						}
					}
					else {
						// No fractional seconds.
						date.setPrecision(DateTime.PRECISION_SECOND);
						int sec = Integer.parseInt(secString);
						date.setSecond(sec);
					}
				}
			}
		}
		//Message.printStatus(2, routine, "After parsing ISO 8601, date/time is: \"" + date + "\"");
	}
	else {
		throw new IllegalArgumentException ( "Date format " + format +	" is not recognized." );
	}
	// Check for hour 24.
	if ( date.__hour == 24 ) {
		// Assume the date that was parsed uses a 1-24 hour system. Change to hour 0 of the next day.
		date.__hour = 0;
		date.addDay(1);
	}

	if ( Message.isDebugOn ) {
		Message.printDebug(dl,routine, "After parsing, date/time: " + date );
	}

	// Verify that the date components are valid.  If not, throw an exception.
	// This degrades performance some but not much since all checks are integer based.
	// Limit year to a reasonable value.
	if ( (date.__year < -1000) || (date.__year > 10000) ) {
		throw new IllegalArgumentException ( "Invalid year " + date.__year + " in \"" + dtString + "\"" );
	}
	if ( is_year ) {
		date.reset();
		return date;
	}
	if ( (date.__month < 1) || (date.__month > 12) ) {
		throw new IllegalArgumentException ( "Invalid month " + date.__month + " in \"" + dtString + "\"" );
	}
	if ( is_month ) {
		date.reset();
		return date;
	}
	// Split out checks to improve performance.
	if ( date.__month == 2 ) {
		if ( TimeUtil.isLeapYear ( date.__year ) ) {
			if ( (date.__day < 1) || (date.__day > 29) ) {
				throw new IllegalArgumentException ( "Invalid day " + date.__day +	" in \"" + dtString + "\"" );
			}
		}
		else {
		    if ( (date.__day < 1) || (date.__day > 28) ) {
				throw new IllegalArgumentException ( "Invalid day " + date.__day + " in \"" + dtString + "\"" );
			}
		}
	}
	else {
	    // Not a leap year.
		if ( (date.__day < 1) || (date.__day > TimeUtil.MONTH_DAYS[date.__month - 1]) ) {
			throw new IllegalArgumentException ( "Invalid day " + date.__day + " in \"" + dtString + "\"" );
		}
	}
	if ( is_day ) {
		date.reset();
		return date;
	}
	if ( (date.__hour < 0) || (date.__hour > 23) ) {
		throw new IllegalArgumentException ( "Invalid hour " + date.__hour + " in \"" + dtString + "\"" );
	}
	if ( is_hour ) {
		date.reset();
		return date;
	}
	if ( (date.__minute < 0) || (date.__minute > 59) ) {
		throw new IllegalArgumentException ( "Invalid minute " + date.__minute + " in \"" + dtString + "\"" );
	}
	if ( is_minute ) {
		date.reset();
		return date;
	}
	date.reset();
	//Message.printStatus(2, routine, "After parsing, date/time=" + date );
	return date;
}

/**
Reset the derived data (year day, absolute month, and leap year).
This is normally called by other DateTime functions but can be called externally if data are set manually.
*/
public void reset () {
	// Always reset the absolute month since it is cheap.
	setAbsoluteMonth();
	if ( (__behavior_flag & DATE_FAST) != 0 ) {
		// Want to run fast so don't check.
		return;
	}
	setYearDay();
    __isleap = TimeUtil.isLeapYear( __year );
}

/**
Round the time to an even interval.
This is useful when setting the period for a time series from irregular end dates.
If a matching even interval is specified, then no change will occur.
Any reasonable combination of base and multiplier can be specified,
resulting in intervals that divide evenly into the next coarsest time interval (e.g., use 10 min, not 13 min).
Otherwise, results may be unexpected.
Time components smaller than the base are set to appropriate zero values
(e.g., rounding minutes results in seconds being set to zero).
The irregular interval results in no change to the date.
@param direction Specify 1 to round by incrementing the date.
Specify -1 to round by decrementing the date.  This flag may be modified in the future to have additional meaning.
@param interval_base See TimeInterval.
@param interval_mult Multiplier for the interval base.
*/
public void round ( int direction, int interval_base, int interval_mult ) {
	if( interval_base == TimeInterval.SECOND ) {
		__nano = 0;
	}
	else if( interval_base == TimeInterval.MINUTE ) {
		__second = 0;
		__nano = 0;
		if ( direction > 0 ) {
			// Rounding up (if the minute is already 0 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __minute != 0 ) {
					// Want an even hour and minute is not zero.
					// Increase the hour. Do so by incrementing the minutes.
					addMinute ( 60 - __minute );
				}
				// Else.  Do nothing since minute is already zero.
			}
			else {
			    // Want to increment to an even interval.
				if ( (__minute%interval_mult) != 0 ) {
					// Not exactly on interval time.
					addMinute ( interval_mult -	__minute%interval_mult );
				}
			}
		}
		else {
            // Rounding down (if the _minute is already 0 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __minute != 0 ) {
					// Want an even hour and minute is not zero.  Decrease the hour.
					// Do so by decrementing the minutes.
					addMinute ( -1*__minute );
				}
				// Else.  Do nothing since minute is already zero.
			}
			else {
			    // Want to decrement to an even interval.
				if ( (__minute%interval_mult) != 0 ) {
					// Not exactly on interval time.
					addMinute ( -1*__minute%interval_mult );
				}
			}
		}
	}
	else if ( interval_base == TimeInterval.HOUR ) {
		__minute = 0;
		__second = 0;
		__nano = 0;
		if ( direction > 0 ) {
			// Rounding up (if the hour is already 0 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Increase the day.
					// Do so by incrementing the hours.
					addHour ( 24 - __hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to increment to an even interval.
				addHour ( interval_mult - __hour%interval_mult );
			}
		}
		else {
            // Rounding down (if the _hour is already 0 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Decrease the day.
					// Do so by decrementing the hour.
					addHour ( -1*__hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to decrement to an even interval.
				addHour ( -1*__hour%interval_mult );
			}
		}
    }
    else if ( interval_base == TimeInterval.DAY ) {
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
		if ( direction > 0 ) {
			// Rounding up (if the _day is already 1 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Increase the day.
					// Do so by incrementing the hours.
					addHour ( 24 - __hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to increment to an even interval.
				addHour ( interval_mult - __hour%interval_mult );
			}
		}
		else {
            // Rounding down (if the _hour is already 0 then don't need to do anything).
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Decrease the day.
					// Do so by decrementing the hour.
					addHour ( -1*__hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to decrement to an even interval.
				addHour ( -1*__hour%interval_mult );
			}
		}
    }
    else if ( interval_base == TimeInterval.WEEK ) {
        Message.printWarning( 2, "DateTime.round", "Rounding to week not implemented yet." );
    }
    else if ( interval_base == TimeInterval.MONTH ) {
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
    }
    else if ( interval_base == TimeInterval.YEAR ) {
		__month = 1;
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
    }
    else if ( interval_base == TimeInterval.IRREGULAR ) {
		// Do nothing to the date.
	}
    else {
    	// Unsupported interval.
		Message.printWarning ( 2, "DateTime.round",	"Interval base " + interval_base + " is unsupported.");
    }
	reset();
}

/**
 * Round to a day of the week, for example to set a period of record to process.
 * The offset from the current day is determined and then applied,
 * which may cause other changes to occur such as month and year.
 * @param direction Specify 1 to round by incrementing the date.
 * Specify -1 to round by decrementing the date.
 * @param dayOfWeek the day of week to round to specified as a string
 * (Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday), or specify as null if dayOfWeekInt is specified
 * @param dayOfWeekInt the day of week to round to specified as an integer (1=Monday as per ISO 8601), or -1 if 'dayOfWeek' should be used
 */
public void roundToDayOfWeek ( int direction, String dayOfWeek, int dayOfWeekInt ) {
	// Get the day of week for the current date/time:
	// - use OffsetDateTime
	// - time zone is ignored
	OffsetDateTime dt = OffsetDateTime.of(
		this.__year,
		this.__month,
		this.__day,
		this.__hour,
		this.__minute,
		this.__second,
		this.__nano,
		ZoneOffset.ofHours(0));
	// Day of week is according to ISO 8601 so 1=Monday ... 7=Sunday
	int dowFrom = dt.getDayOfWeek().getValue();
	
	// Determine the requested day of week.
	int dowTo = -1;
	if ( (dayOfWeek != null) && !dayOfWeek.isEmpty() ) {
		// Use the string to determine the day of week.
		if ( dayOfWeek.equalsIgnoreCase("Monday") ) {
			dowTo = 1;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Tuesday") ) {
			dowTo = 2;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Wednesday") ) {
			dowTo = 3;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Thursday") ) {
			dowTo = 4;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Friday") ) {
			dowTo = 5;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Saturday") ) {
			dowTo = 6;
		}
		else if ( dayOfWeek.equalsIgnoreCase("Sunday") ) {
			dowTo = 7;
		}
		else {
			// Bad input.
			throw new IllegalArgumentException ( "Invalid day of week (" + dayOfWeek + ") for round." );
		}
	}
	
	// Calculate the day shift.
	int dayShift = dowTo - dowFrom;
	if ( (dayShift > 0) && (direction > 0) ) {
		// Do the shift forward.
		addDay(dayShift);
	}
	else if ( (dayShift < 0) && (direction < 0) ) {
		// Do the shift backwards.
		addDay(dayShift);
	}
	else if ( (dayShift > 0) && (direction < 0) ) {
		// Do the shift backwards by partial week.
		addDay(dayShift - 7);
	}
	else if ( (dayShift < 0) && (direction > 0) ) {
		// Do the shift backwards by partial week.
		addDay(dayShift + 7);
	}
}

/**
Set the absolute month from the month and year.  This is called internally.
*/
private void setAbsoluteMonth() {
	__abs_month = (__year * 12) + __month;
}

/**
Set value of time series using another as input (equivalent to C++ = operator).
A new instance is not allocated.
@param t A DateTime to copy.
*/
public void setDate ( DateTime t ) {
	if ( t == null ) {
		return;
	}
	__nano = t.__nano;
	__second = t.__second;
	__minute = t.__minute;
	__hour = t.__hour;
	__day = t.__day;
	__month = t.__month;
	__year = t.__year;
	__isleap = t.__isleap;
	__iszero = t.__iszero;
	__weekday = t.__weekday;
	__yearday = t.__yearday;
	__abs_month	= t.__abs_month;
	__behavior_flag	= t.__behavior_flag;
	__precision	= t.__precision;
	__use_time_zone	= t.__use_time_zone;
	__time_only	= t.__time_only;
	setTimeZone( t.__tz );
	reset();
}

/**
Set value of the date/time using a Date as input.
A new instance is not allocated.  This is useful when iterating through database records that use Date.
Only fields appropriate for the DateTime precision are set (year through nanosecond) as appropriate,
and data that are too precise are ignored.
Currently time zone is NOT set.
@param d A Date to assign from.
*/
@SuppressWarnings("deprecation")
public void setDate ( Date d ) {
	if ( d == null ) {
		reset();
		return;
	}
	// Returns the number of years since 1900 so add 1900 to get full four-digit year.
	setYear ( d.getYear() + 1900 );
	if ( __precision == PRECISION_YEAR ) {
		reset();
		return;
	}
	// Returned month is 0 to 11 so need to increment to get 1 to 12 range.
	setMonth ( d.getMonth() + 1 );
	if ( __precision == PRECISION_MONTH ) {
		reset();
		return;
	}
	// Returned day is 1 to 31.
	setDay ( d.getDate() );
	if ( __precision == PRECISION_DAY ) {
		reset();
		return;
	}
	// Returned hours are 0 to 23.
	setHour ( d.getHours() );
	if ( __precision == PRECISION_HOUR ) {
		reset();
		return;
	}
	// Returned hours are 0 to 59.
	setMinute ( d.getMinutes() );
	if ( __precision == PRECISION_MINUTE ) {
		reset();
		return;
	}
	setSecond ( d.getSeconds() );
	// Java 'Date' class does not include more precise data.
	reset();
}

/**
Set the day (1 to number of days in month, depending on month; year and month should be set first for valid check).
@param d Day.
*/
public void setDay ( int d ) {	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
		if(	(d > TimeUtil.numDaysInMonth( __month, __year )) || (d < 1) ) {
            String message = "Trying to set invalid day (" + d + ") in DateTime for year=" + __year + " and month=" + __month;
            Message.printWarning( 10, "DateTime.setDay", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __day = d;
	setYearDay();
	// This has the flaw of not changing the flag when the value is set to 1.
	if ( __day != 1 ) {
		__iszero = false;
	}
}

/**
Set the hour (0-23).
@param h Hour.
*/
public void setHour( int h ) {	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
		if( (h > 23) || (h < 0) ) {
			String message = "Trying to set invalid hour (" + h + ") in DateTime.  Must be in range 0 - 23.";
            Message.printWarning( 2, "DateTime.setHour", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __hour = h;
	// TODO smalers 2022-05-24 This has the flaw of not changing the flag when the value is set to 0.
	if ( __hour != 0 ) {
		__iszero = false;
	}
}

/**
Set the hundredths of seconds.
@param hs Hundredths of seconds.
*/
public void setHSecond( int hs) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( (hs > 99) || (hs < 0) ) {
            String message = "Trying to set invalid hsecond (" + hs + ") in DateTime, must be between 0 and 99.";
            Message.printWarning( 2, "DateTime.setHSecond", message );
            throw new IllegalArgumentException ( message );
        }
	}
	if ( hs >= 100 ) {
		// Truncate to first two digits.
		String s = "" + hs;
		s = s.substring(0, 2);
		hs = Integer.parseInt(s);
	}
	__nano = hs*10000000;
	// This has the flaw of not changing the flag when the value is set to 0 but usually other data are set.
	if ( hs != 0 ) {
		__iszero = false;
	}
}

/**
Set the minute (0-59).
@param m Minute.
*/
public void setMinute( int m) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if ( (m > 59) || (m < 0) ) {
            String message = "Trying to set invalid minute (" + m + ") in DateTime.  Must be in range 0-59.";
            Message.printWarning( 2, "DateTime.setMinute", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __minute = m;
	// This has the flaw of not changing the flag when the value is set to 0.
	if ( m != 0 ) {
		__iszero = false;
	}
}

/**
Set the month (1-12).
@param m Month.
*/
public void setMonth ( int m) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if ( (m > 12) || (m < 1) ) {
            String message = "Trying to set invalid month (" + m + ") in DateTime.  Must be in range 1-12.";
            Message.printWarning( 2, "DateTime.setMonth", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __month = m;
	setYearDay();
	setAbsoluteMonth();
	// This has the flaw of not changing the flag when the value is set to 1.
	if ( m != 1 ) {
		__iszero = false;
	}
}

/**
Set the nano seconds.  This is the fraction of the second, not absolute nanoseconds.
@param nano nano seconds, up to nine digits (0-999999999).
*/
public void setNanoSecond( int nano ) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if ( (nano > 999999999) || (nano < 0) ) {
            String message = "Trying to set invalid nanosecond (" + nano + ") in DateTime, must be between 0 and 999999999.";
            Message.printWarning( 2, "DateTime.setNanoSecond", message );
            throw new IllegalArgumentException ( message );
        }
	}
	if ( nano > 999999999 ) {
		// Set to the maximum value.
		__nano = 999999999;
	}
	else {
		__nano = nano;
	}
	// This has the flaw of not changing the flag when the value is set to 0 but usually other data are set.
	if ( nano != 0 ) {
		__iszero = false;
	}
}

/**
Set the precision using a bit mask.
The precision can be used to optimize code (avoid performing unnecessary checks) and set more intelligent dates.
The overloaded version is called with a "cumulative" value of true.
@param behavior_flag Full behavior mask containing precision bit (see PRECISION_*).
The precision is set when the first valid precision bit is found (starting with PRECISION_YEAR).
@return this DateTime instance, which allows chained calls.
*/
public DateTime setPrecision ( int behavior_flag ) {
	return setPrecision ( behavior_flag, true );
}

/**
Set the precision using a bit mask.  The precision can be used to optimize code
(avoid performing unnecessary checks) and set more intelligent dates.
This call automatically truncates unused date fields (sets them to initial values as appropriate).
Subsequent calls to getPrecision(), timeOnly(),
and useTimeZone() will return the separate field values (don't need to handle as a bit mask upon retrieval).
@param behavior_flag Full behavior mask containing precision bit (see PRECISION_*).
The precision is set when the first valid precision bit is found (starting with PRECISION_YEAR).
@param cumulative If true, the bit-mask values will be set cumulatively.
If false, the values will be reset to defaults and only new values will be set.
@return this DateTime instance, which allows chained calls.
*/
public DateTime setPrecision ( int behavior_flag, boolean cumulative ) {
	// The behavior flag contains the precision (small bits) and higher bit masks.
	// The lower precision values are not unique bit masks.
	// Therefore, get the actual precision value by cutting off the higher values > 100 (the maximum precision value is 70).
	//_precision = behavior_flag - ((behavior_flag/100)*100);
	// Need to remove the effects of the higher order masks.
	//int behavior_flag_no_precision = behavior_flag;
	int precision = behavior_flag;
	if ( (behavior_flag & DATE_STRICT) != 0 ) {
		//behavior_flag_no_precision |= DATE_STRICT;
		precision ^= DATE_STRICT;
	}
	if ( (behavior_flag & DATE_FAST) != 0 ) {
		//behavior_flag_no_precision |= DATE_FAST;
		precision ^= DATE_FAST;
	}
	if ( (behavior_flag & DATE_ZERO) != 0 ) {
		//behavior_flag_no_precision |= DATE_ZERO;
		precision ^= DATE_ZERO;
	}
	if ( (behavior_flag & DATE_CURRENT) != 0 ) {
		//behavior_flag_no_precision |= DATE_CURRENT;
		precision ^= DATE_CURRENT;
	}
	if ( (behavior_flag & TIME_ONLY) != 0 ) {
		//behavior_flag_no_precision |= TIME_ONLY;
		precision ^= TIME_ONLY;
	}
	if ( (behavior_flag & PRECISION_TIME_ZONE) != 0 ) {
		//behavior_flag_no_precision |= PRECISION_TIME_ZONE;
		precision ^= PRECISION_TIME_ZONE;
	}
	// Now the precision should be what is left.
	if ( precision == PRECISION_YEAR ) {
		__month = 1;
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_MONTH ) {
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_DAY ) {
		__hour = 0;
		__minute = 0;
		__second = 0;
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_HOUR ) {
		__minute = 0;
		__second = 0;
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_MINUTE ) {
		__second = 0;
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_SECOND ) {
		__nano = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_HSECOND ) {
		// Keep full nanoseconds, will be formatted as needed on output.
		__precision = precision;
	}
	else if ( precision == PRECISION_MILLISECOND ) {
		// Keep full nanoseconds, will be formatted as needed on output.
		__precision = precision;
	}
	else if ( precision == PRECISION_MICROSECOND ) {
		// Keep full nanoseconds, will be formatted as needed on output.
		__precision = precision;
	}
	else if ( precision == PRECISION_NANOSECOND ) {
		// Keep full nanoseconds, will be formatted as needed on output.
		__precision = precision;
	}

	// Else do not set _precision - assume that it was set previously (e.g., in a copy constructor).

	// Time zone is separate and always gets set.

	if ( (behavior_flag & PRECISION_TIME_ZONE) != 0 ) {
		__use_time_zone = true;
	}
	else if ( !cumulative ) {
		__use_time_zone = false;
	}

	// Time only is separate and always gets set.

	if ( (behavior_flag & TIME_ONLY) != 0 ) {
		__time_only = true;
	}
	else if ( !cumulative ) {
		__time_only = false;
	}
	return this;
}

/**
Set the second (0-59).
@param s Second.
*/
public void setSecond( int s ) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( s > 59 || s < 0 ) {
            String message = "Trying to set invalid second (" + s + ") in DateTime.  Must be in range 0-59";
            Message.printWarning( 2, "DateTime.setSecond", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __second = s;
	// This has the flaw of not changing the flag when the value is set to 0.
	if ( s != 0 ) {
		__iszero = false;
	}
}

/**
Set the string time zone.  No check is made to verify that it is a valid time zone abbreviation.
The time zone should normally only be set for DateTime that have a time component.
For most analytical purposes the time zone should be GMT or a standard zone like MST.
Time zones that use daylight savings or otherwise change over history or during the year are
problematic to maintaining continuity.
The getDate*() methods will consider the time zone if requested.
@param zone Time zone abbreviation.  If non-null and non-blank, the
DateTime precision is automatically set so that PRECISION_TIME_ZONE is on.
If null or blank, PRECISION_TIME_ZONE is off.
@return the same DateTime instance, which allows chained calls
*/
public DateTime setTimeZone( String zone ) {
	if ( (zone == null) || (zone.length() == 0) ) {
		// No time zone is used.
		this.__tz = "";
		this.__use_time_zone = false;
	}
	else {
		// Time zone is used.
        this.__use_time_zone = true;
		this.__tz = zone;
	}
    return this;
}

/**
Set to the current date/time.
The default precision is PRECISION_SECOND and the time zone is set.
This method is usually only called internally to initialize dates.
If called externally, the precision should be set separately.
*/
public void setToCurrent ()  {
 	// First get the current time (construct a new date because this code is not executed that much).

	Date d = new Date (); // This will use local time zone.
	DateTime now = new DateTime ( d );

	// Now set.

	__nano = now.__nano;
	__second = now.__second;
	__minute = now.__minute;
	__hour = now.__hour;
	__day = now.__day;
	__month = now.__month;
	__year = now.__year;
	__isleap = now.isLeapYear();
	__weekday = now.getWeekDay();
	__yearday = now.getYearDay();
	__abs_month	= now.getAbsoluteMonth();
	__tz = now.__tz;
	__behavior_flag	= DATE_STRICT;
	// Use precision to second, which is sufficient for most work.
	__precision = PRECISION_SECOND;
	__use_time_zone = false;
	__time_only = false;

	// Set the time zone.  Use TimeUtil directly to increase performance.
	// TODO SAM 2016-03-12 Need to rework this - legacy timezone was needed at one point but should use java.util.time or Java 8 API.
	if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ONCE ) {
		if ( !TimeUtil._local_time_zone_retrieved ) {
			// Need to initialize.
			shiftTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
		}
		else {
            // Use the existing data.
			shiftTimeZone ( TimeUtil._local_time_zone_string );
		}
	}
	else if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ALWAYS ) {
		shiftTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
	}

	__iszero = false;
}

/**
Set the date using the year and a Julian day.
@param y 4-digit year.
@param julday Julian day since start of year where 1 = Jan 1).
*/
public void setToJulianDay ( int y, int julday ) {
	__year = y;
    // Need to set here because leap year is tested in the following loop.
    __isleap = TimeUtil.isLeapYear( __year );
	// Loop through the static Julian day data.
	int offset = 0;
	for ( int i = 1; i < 12; i++ ) {
		if ( i > 1 ) {
			// Since counts are for days prior to month, this only kicks in after February.
			if ( __isleap ) {
				offset = 1;
			}
		}
		if ( julday <= (TimeUtil.MONTH_YEARDAYS[i] + offset) ) {
			// Month is previous (but use i since zero-index).
			__month = i;
			if ( i > 2 ) {
				// Need to subtract the offset to get the right day.
				__day = julday - (TimeUtil.MONTH_YEARDAYS[i - 1] + offset);
			}
			else {
                // Don't consider the offset.
				__day = julday - TimeUtil.MONTH_YEARDAYS[i - 1];
			}
			reset();
			return;
		}
	}
	// If here then the month is December.
	__month = 12;
	int d = julday - (TimeUtil.MONTH_YEARDAYS[11] + offset);
	if ( d > 31 ) {
		d = 31;
	}
	__day = d;
	reset();
}

/**
Set the date/time to all zeros, except day and month are 1.  The time zone is set to "".
The default precision is PRECISION_SECOND and the time zone is not used.
This method is usually only called internally to initialize dates.
The behavior is DATE_STRICT, meaning that trying to set any invalid parts will result in an exception.
If called externally, the precision should be set separately.
*/
public void setToZero ( ) {
	__nano = 0;
	__second = 0;
	__minute = 0;
	__hour = 0;
	__day = 1;
	__month = 1;
	__year = 0;
	__isleap = false;
	__weekday = 0;
	__yearday = 0;
	__abs_month	= 0;
	__tz = "";
	__behavior_flag	= DATE_STRICT;
	// TODO smalers 2022-03-07 don't set precision since it will have been set before calling this function:
	// - default value is PRECISION_SECOND when an object is created
	// Use precision to second, which is sufficient for most work.
	//__precision = PRECISION_SECOND;
	__use_time_zone = false;
	__time_only = false;

	// Indicate that the date/time has been zero to zeros.

	__iszero = true;
}

/**
Set the year.  Currently no restrictions are imposed.
@param year year to set
*/
public void setYear( int year ) {
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        /* TODO SAM 2007-12-20 Evaluate whether negative year should be allowed.
        if( y < 0 ) {
            String message = "Trying to set invalid year (" + y + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setYear", message );
            throw new IllegalArgumentException ( message );
        }
        */
	}
    __year = year;
	setYearDay();
	setAbsoluteMonth();
    __isleap = TimeUtil.isLeapYear( __year );
	if ( year != 0 ) {
		__iszero = false;
	}
}

/**
Set the year day from other data.
The information is set ONLY if the DATE_FAST bit is not set in the behavior mask.
*/
private void setYearDay() {
	if ( (__behavior_flag & DATE_FAST) != 0 ) {
		// Want to run fast so don't check.
		return;
	}

	int i;

   	// Calculate the year day.

   	__yearday = 0;

   	// Get the days from the previous months.

   	for( i = 1; i < __month; i++ ) {
       	__yearday += TimeUtil.numDaysInMonth( i, __year );
   	}

   	// Add the days from the current month.

   	__yearday += __day;
}

/**
Shift the data to the specified time zone, resulting in the hours and possibly minutes being changed.
@param zone Time zone to switch to.  This method shifts the hour/minutes and
then sets the time zone for the instance to the requested time zone.
@exception Exception if the time zone cannot be shifted (unknown time zone).
*/
public void shiftTimeZone ( String zone ) {
	String routine = getClass().getSimpleName() + ".shiftTimeZone";
	if ( Message.isDebugOn ) {
		// Not sure why this is printed.
		Message.printStatus(2, routine, "Shifting to time zone \"" + zone + "\"");
	}
	if ( zone.isEmpty() ) {
		// Just set the time zone to blank to make times timezone-agnostic.
		setTimeZone ( "" );
	}
	else if ( zone.equalsIgnoreCase(this.__tz) ) {
		// The requested time zone is the same as original.  Do nothing.
	}
	else if ( zone.startsWith("+") || zone.startsWith("-") ) {
		// Special case - expect an ISO 8601 offset timezone such as -07 or -07:00
		Message.printStatus(2, "", "Assume ISO 8601 time zone \"" + zone + "\"");
		// Get the offset from the existing time zone
		ZoneOffset offsetOrig = TimeUtil.getTimeZoneOffset(this.__tz);
		if ( offsetOrig == null ) {
			throw new RuntimeException ( "Trying to shift time zone from unrecognized existing time zone \"" + this.__tz + "\".");
		}
		// Calculate the time zone offset from the requested zone.
		ZoneOffset offsetNew = TimeUtil.getTimeZoneOffset(zone);
		if ( offsetNew == null ) {
			throw new RuntimeException ( "Trying to shift time zone to unrecognized time zone \"" + zone + "\".");
		}
		// Shift the time:
		// - for example if original is -07:00 and new is -06:00, time to add (using hours for example) is: -6 -(-7) = 1
		// - for example if original is -06:00 and new is -07:00, time to add is:  -7 -(-6) = -1
		addSecond(offsetNew.getTotalSeconds() - offsetOrig.getTotalSeconds());
		// Set the time zone to the requested.
		setTimeZone ( zone );
	}
	else {
		// All other time zones.
		// TODO smalers 2017-07-13 need to phase in java.time.
		// Want to change the time zone so compute an offset and apply.
		try {
	        @SuppressWarnings("deprecation")
			int offset = TZ.calculateOffsetMinutes ( __tz, zone, this );
			addMinute ( offset );
			setTimeZone ( zone );
			// TODO SAM 2016-03-11 See getDate(String tz) treatment of time zone - could add check here.
		}
		catch ( Exception e ) {
			// For now rethrow as RuntimeException because legacy code would need to be updated to handle Exception.
			throw new RuntimeException ( e );
		}
	}
}

/**
Subtract a time series interval from the DateTime (see TimeInterval).
An irregular interval is ignored (the date is not changed).
@param interval Time series base interval.
@param subtract Multiplier for base interval.  This should be a positive number.
*/
public void subtractInterval(int interval, int subtract) {
	addInterval(interval, -1 * subtract);
}

/**
Indicate whether the DateTime is only storing a time.
This will be the case if the TIME_ONLY flag is in effect during construction.
@return true if only time fields are considered.
*/
public boolean timeOnly () {
	return __time_only;
}

/**
Convert to a double, with the whole number being the year.
This is useful for graphics.  The precision is checked and remainder fields are ignored.
If the instance is only storing time, then the whole number part of the value will be zero.
@return Date/time representation as a double.
*/
public double toDouble ( ) {
	double dt = 0.0, d = 0;
	double ydays = (double)TimeUtil.numDaysInYear(__year);

	if ( !__time_only ) {
		dt = (double)__year;
		if ( __precision == PRECISION_YEAR ) {
			return dt;
		}
	
		d = ((double)(TimeUtil.numDaysInMonths(1, __year, (__month-1))));
		if ( __precision == PRECISION_MONTH ) {
			return (dt + d/ydays);
		}

		d += (double)(__day - 1);
		if ( __precision == PRECISION_DAY ) {
			return (dt + d/ydays);
		}
	}

	// Normalize to day for hours, minutes, seconds, etc.

	d += ((double)(__hour))/24.0;
	if ( __precision == PRECISION_HOUR ) {
		return (dt + d/ydays);
	}
	d += ((double)(__minute))/1440.0; // 60*24
	if ( __precision == PRECISION_MINUTE ) {
		return (dt + d/ydays);
	}
	d += ((double)(__second))/86400.0; // 60*60*24
	if ( __precision == PRECISION_SECOND ) {
		return (dt + d/ydays);
	}
	// First convert nanoseconds to hundredths of seconds and then convert to double, similar to older version of the code.
	d += ((double)(__nano/10000000))/8640000; // 100*60*60*24
	return (dt + d/ydays);
}

/**
Return string representation of the date and time.
@return String representation of the date, using a format consistent with
the precision for the date (see PRECISION_* and TIME_ONLY).
In general, the default output formats ISO-8601 strings like YYYY-MM-DD or YYYY-MM-DD HH:mm:ss.
*/
public String toString () {
	// Arrange these in probable order of use.
	if ( __precision == PRECISION_YEAR ) {
		return toString ( FORMAT_YYYY );
	}
	else if ( __precision == PRECISION_MONTH ) {
		return toString ( FORMAT_YYYY_MM );
	}
	else if ( __precision == PRECISION_DAY ) {
		return toString ( FORMAT_YYYY_MM_DD );
	}
	else if ( __precision == PRECISION_HOUR ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH );
		}
	}
	else if ( __precision == PRECISION_MINUTE ) {
		if ( __time_only ) {
			return toString ( FORMAT_HH_mm );
		}
		else {
            if ( __use_time_zone && (__tz.length() > 0) ) {
    			char prefix = __tz.charAt(0);
    			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
    				return toString ( FORMAT_ISO_8601 );
    			}
    			else {
    				return toString ( FORMAT_YYYY_MM_DD_HH_mm_ZZZ );
    			}
			}
			else {
                return toString ( FORMAT_YYYY_MM_DD_HH_mm );
			}
		}
	}
	else if ( __precision == PRECISION_SECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS );
		}
	}
	else if ( __precision == PRECISION_HSECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_hh );
		}
	}
	else if ( __precision == PRECISION_MILLISECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI );
		}
	}
	else if ( __precision == PRECISION_MICROSECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO );
		}
	}
	else if ( __precision == PRECISION_NANOSECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_NANO_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_NANO );
		}
	}
	else {
        // Assume that hours and minutes but NOT time zone are desired.
		// TODO smalers 2022-03-06 probably should include seconds by default?
		if ( __use_time_zone && (__tz.length() > 0) ) {
			char prefix = __tz.charAt(0);
			if ( (prefix == '-') || (prefix == '+') || __tz.equals("Z") ) {
				return toString ( FORMAT_ISO_8601 );
			}
			else {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_ZZZ );
			}
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm );
		}
	}
}

// Remember to also update the parse() method.
/**
Convert to a string using the given format (see FORMAT_*).
This is not as flexible as formatTimeString but is useful where date formats need to be consistent.
Currently if a time zone is detected, it is set in the data but the PRECISION_TIME_ZONE flag is not set to true.
@return A String representation of the date.
@param format The format to use for the string.
*/
public String toString ( int format ) {
	if ( format == FORMAT_NONE ) {
		return "";
	}
	else if ( format == FORMAT_AUTOMATIC ) {
		return toString();
	}
	else if ( format == FORMAT_DD_SLASH_MM_SLASH_YYYY ) {
		return
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d");
	}	
	else if ( format == FORMAT_HH_mm ) {
		return
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_HHmm ) {
		// This format is NOT parsed automatically (the 4-digit year parse is done instead).
		return
		StringUtil.formatString(__hour,"%02d") +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_MM ) {
		return StringUtil.formatString(__month,"%02d");
	}
	else if ( format == FORMAT_MM_DD ) {
		return
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_DD ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d");
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YY ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(TimeUtil.formatYear( __year,2),"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d") + " " +
		StringUtil.formatString(__hour,"%02d");
	}
	else if ( format == FORMAT_MM_DD_YYYY_HH ) {
		return
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + "-" +
		StringUtil.formatString(__year,"%04d") + " " +
		StringUtil.formatString(__hour,"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__day,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d");
	}
	else if ( format == FORMAT_MM_SLASH_YYYY ) {
		return
		StringUtil.formatString(__month,"%02d") + "/" +
		StringUtil.formatString(__year,"%04d");
	}
	else if ( format == FORMAT_YYYY ) {
		return StringUtil.formatString(__year,"%04d");
	}
	else if ( format == FORMAT_YYYY_MM ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + " " + __tz;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_YYYYMMDDHHmm ) {
		return
		StringUtil.formatString(__year,"%04d") +
		StringUtil.formatString(__month,"%02d") +
		StringUtil.formatString(__day,"%02d") +
		StringUtil.formatString(__hour,"%02d") +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HHmm ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") +
		StringUtil.formatString(__minute,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d" + " " + __tz );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano/10000000,"%02d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + " " + __tz;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano/1000000,"%03d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano/1000,"%06d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_NANO ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano,"%09d");
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + ":" +
		StringUtil.formatString(__nano/10000000,"%02d") + " " + __tz;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano/1000000,"%03d") + " " + __tz;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano/1000,"%06d") + " " + __tz;
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_NANO_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + "." +
		StringUtil.formatString(__nano,"%09d") + " " + __tz;
	}
	else if ( format == FORMAT_VERBOSE ) {
		return "year=" + __year + ", month=" + __month + ", day=" + __day
			+ ", hour=" + __hour + ", min=" + __minute + ", second=" + __second + ", hsecond=" + __nano/10000000
			+ ", tz=\"" + __tz + ", useTimeZone=" + __use_time_zone + ", isLeap=" + __isleap;
	}
	else if ( format == FORMAT_ISO_8601 ) {
		// Output is sensitive to the precision, and use more verbose version for readability:
		// - use dash for date delimiter, colon for time delimiter
		// Precision values sort with Year as largest.
		StringBuilder b = new StringBuilder(); // TODO smalers 2017-07-01 Is this efficient or should there be a shared formatter?
		String dDelim = "-";
		String tDelim = ":";
		if ( __precision <= PRECISION_YEAR ) {
			// Include year.
			b.append(StringUtil.formatString(__year, "%04d") );
		}
		if ( __precision <= PRECISION_MONTH ) {
			// Include month.
			b.append(dDelim);
			b.append(StringUtil.formatString(__month, "%02d") );
		}
		if ( __precision <= PRECISION_DAY ) {
			// Include day.
			b.append(dDelim);
			b.append(StringUtil.formatString(__day, "%02d") );
		}
		if ( __precision <= PRECISION_HOUR ) {
			// Include hour.
			b.append("T");
			b.append(StringUtil.formatString(__hour, "%02d") );
		}
		if ( __precision <= PRECISION_MINUTE ) {
			// Include minute.
			b.append(tDelim);
			b.append(StringUtil.formatString(__minute, "%02d") );
		}
		if ( __precision <= PRECISION_SECOND ) {
			// Include second.
			b.append(tDelim);
			b.append(StringUtil.formatString(__second, "%02d") );
		}
		// Digits of fractional second depend on the precision:
		// - Java Instant resolves to up to 9 digits
		// - check from smallest to largest to avoid ambiguity
		if ( __precision <= PRECISION_NANOSECOND ) {
			b.append(".");
			b.append(StringUtil.formatString(__nano, "%09d") );
		}
		else if ( __precision <= PRECISION_MICROSECOND ) {
			b.append(".");
			b.append(StringUtil.formatString(__nano/1000, "%06d") );
		}
		else if ( __precision <= PRECISION_MILLISECOND ) {
			b.append(".");
			b.append(StringUtil.formatString(__nano/1000000, "%03d") );
		}
		else if ( __precision <= PRECISION_HSECOND ) {
			b.append(".");
			b.append(StringUtil.formatString(__nano/10000000, "%02d") );
		}
		// According to ISO-8601 a missing time zone is ambiguous and will be interpreted as local time zone.
		// TSTool, for example, allows no time zone because often it is not relevant; however, to comply
		// with the standard include the time zone as best as possible.
		if ( __precision <= PRECISION_HOUR ) { // TODO smalers 2017-07-01 should this check for __use_time_zone?
			if ( !__tz.isEmpty() ) {
				// Only output if the time zone is Z, or starts with + or -
				char prefix = __tz.charAt(0);
				if ( (prefix == '+') || (prefix == '-') || __tz.equals("Z") ) {
					b.append(__tz);
				}
				else {
					// Invalid time zone for ISO 8601 formatting:
					// - throw an exception since this format is being phased in and want to be compliant
					// - this format should not be used by default yet as of 2017-07-01 so hopefully is not an issue
					// - may need another variant on this format, for example to not output delimiter
					throw new RuntimeException ( "Time zone \"" + __tz + "\" is incompatile with ISO 8601 format.  Should be Z or +NN:NN, etc.");
				}
			}
		}
		return b.toString();
	}
	else {
		// Use this as default for historical reasons.
		// TODO smalers 2017-07-01 Need to evaluate switching to ISO.
	    return toString( FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ );
	}
}

/**
 * Format a date/time given a format string.
 * @param formatterType formatter type that describes the format
 * @param format string format for the date/time
 */
public String toString ( DateTimeFormatterType formatterType, String format ) {
	YearType yearType = YearType.CALENDAR;
	if ( formatterType == DateTimeFormatterType.C ) {
	    //Message.printStatus(2, routine, "Internal DateTime before formatting=\"" + dt + "\"");
	    return TimeUtil.formatDateTime(this, yearType, format);
	}
	else {
		return "";
	}
}
	
/**
Indicate whether the time zone should be used when processing the date.
The time zone will be considered if the PRECISION_TIME_ZONE flag is in effect
during construction or if the setPrecision() method is called.
@return true if the time zone should be considered when processing the date.
*/
public boolean useTimeZone() {
	return __use_time_zone;
}

}