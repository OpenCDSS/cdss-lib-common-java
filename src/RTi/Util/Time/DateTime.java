// ----------------------------------------------------------------------------
// DateTime - general Date/Time class
// ----------------------------------------------------------------------------
// History:
//
// May 96	Steven A. Malers, RTi	Start developing the class based on the
//					HMData HMTimeData structure.
// 24 Jan 97	Matthew J. Rutherford, 	Added TSDATE_STRICT, and TSDATE_FAST
//		RTi.			to help with speed issues brought
//					about by the reset function.
// 11 Mar 97	MJR, RTi		Put in cast to char* for use in print
//					statements. Couldn't figure out how
//					the syntax for defining the operator
//					so I had to inline it.
// 21 May 1997	MJR, RTi		Put in operator= (char*) to handle
//					string to date conversions.
// 16 Jun 1997	MJR, RTi		Added string as private member to 
//					be used on (char*) cast.
// 05 Jan 1998	SAM, RTi		Update to be consistent with C++.
//					Remove unused code.
// 04 Mar 1998	SAM, RTi		Change *absMonth* routines to
//					*absoluteMonth*.  Depricate the old.
// 28 Apr 1998 	DLG, RTi		Added 
//					FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm
//					for toString.
// 29 Apr 1998	SAM, RTi		Add parse().  Add _precision and
//					set/get routines.  Also add to behavior
//					mask so we can construct.  Add zero
//					case checks to the add routines.
//					Really do something with DATE_FAST.
// 08 Jun 1998	CEN, RTi		Add isDate().
// 09 Jun 1998 	CEN, RTi		Added FORMAT_MM_SLASH_YYYY
// 22 Jun 1998	SAM, RTi		Add subtract to get an offset and
//					overload add to take a TSDate.
// 26 Jun 1998	SAM, RTi		When constructing from a TSDate, also
//					set the precision flag.
// 13 Jul 1998	SAM, RTi		Port C++ parse ( String ) code to Java
//					to generically handle date parsing.
// 27 Jul 1998	SAM, RTi		Add DATE_CURRENT behavior flag to
//					bring in line with C++ behavior.  At
//					some convenient point in the future,
//					make DATE_ZERO the default.
// 02 Sep 1998	SAM, RTi		Added FORMAT_MM_SLASH_YYYY to parse and
//					fix bug in DD/YY parse.
// 15 Oct 1998	SAM, RTi		Change the time zone to MST for default
//					conversion of local time.  Hopefully
//					we will go to Java 1.2 soon and that
//					problem will go away.
// 25 Nov 1998	SAM, RTi		Add constructor to take Date and flag
//					so precision can be set.  Add
//					getBehaviorFlag so that the DMI package
//					can use to set precision on query date
//					strings.
// 02 Jan 1998	SAM, RTi		Overload isDate to take any string.
// 06 Jan 1998	SAM, RTi		Use StringUtil.atoi to do some date
//					conversions to avoid problems with
//					spaces.  Change the TSDate constructor
//					that takes a double to call the
//					TimeUtil.getMonthAndDayFromDayOfYear to
//					pass the year to be more robust.
//					Change the init routines to
//					setToZero and setToCurrent and make
//					public to make useful (e.g., in
//					iterations).  Also make it a little
//					clearer how to switch the defaults (want
//					zero to be default in future).
// 12 Apr 1999	SAM, RTi		Add finalize.  Add
//					FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ to
//					toString().
// 29 Apr 1999	SAM, RTi		Update the time zone code now that
//					Java 1.2 retrieves a local time zone
//					correctly.  TimeUtil will also have
//					similar code but for performance also
//					have here.  Add getLocalTimeZoneAbbr()
//					to support shifts of database data.
// 30 May 2000	SAM, RTi		Update to call reset() in constructors
//					so absolute date, etc., will be set.
// 12 Oct 2000	SAM, RTi		Add setDate() to assign one date to
//					another without allocating a new date.
// 23 Nov 2000	SAM, RTi		Add FORMAT_HHmm and FORMAT_HH_mm to
//					support plots.  Previously updated to
//					change hour 24 to hour 0 of the next day
//					to agree with C++ version.
//					Add FORMAT_MM_SLASH_DD_SLASH_YYYY_HH.
// 20 Dec 2000	SAM, RTi		Add FORMAT_YYYYMMDDHHmm to agree with
//					C++.
// 20 Mar 2001	SAM, RTi		Add a little more smarts to the subtract
//					method to get it to perform better.
//					Fix bug where TSDate(TSDate,int)
//					constructor was not correctly setting
//					the precision.
// 11 Apr 2001	SAM, RTi		Add MM_DD_YYYY_HH format to better
//					support Excel, Access.
// 18 May 2001	SAM, RTi		Add setPrecision ( TS ) to simplify
//					interval handling.
// 31 May 2001	SAM, RTi		Change toDouble() to check precision so
//					that accidental remainder junk is not
//					used.  Change so that the copy
//					constructor that takes a date and a
//					flag checks the precision during the
//					copy and ignores unneeded information.
// 01 Aug 2001	SAM, RTi		Add FORMAT_NONE, FORMAT_AUTOMATIC, and
//					FORMAT_MM to be consistent with C++.
// 28 Aug 2001	SAM, RTi		Implement clone().  A copy constructor
//					is already implemented but clone() is
//					used by TS and might be preferred by
//					some developers.  Delete all the old
//					C++/C style documentation and fold into
//					the javadoc.  Remove debug information
//					where no longer needed.  Enable
//					addInterval() to handle week as multiple
//					of 7 days, in case it ever is needed.
// 06 Sep 2001	SAM, RTi		Fix so isDate() returns false if a null
//					date on parse.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that veriables
//					are set to null when no longer used.
//					Change all the add and set methods to
//					have a void return type, consistent with
//					C++ code.  Fix getNumIntervals() to use
//					a local copy of a TSDate for iteration -
//					previously the start date was modified
//					in the method.
// 2001-11-20	SAM, RTi		Add FORMAT_YYYY_MM_DD_HH_ZZZ to better
//					handle real-time data.  The parse() and
//					toString() methods will automatically
//					handle hour dates with a time zone,
//					regardless of the precision.
// ===============
// 2001-12-13	SAM, RTi		Copy the TSDate class and modify to
//					make generic.  Assume some of the C++
//					conventions, like making the default
//					initialization be to a zero date.
//					Move the following to TimeUtil:
//						getDateFromIndex()
//						getLocalTimeZoneAbbr()
//						getNumIntervals()
//					Use TimeUtil.getLocalTimeZone().
//					Add a _use_time_zone data member to
//					handle time zone separately from the
//					other parts of the precision.
//					Add FORMAT_YYYY_MM_DD_HH_mm_ZZZ to
//					parse and toString().
// 2001-12-19	SAM, RTi		Update to use new TZ class.  Add method
//					isZero() to indicate whether the
//					DateTime has been initialized to zero
//					but has had no changes since then.
//					Change to only store the time zone
//					abbreviation, especially since time zone
//					shifts in minutes is now the standard.
//					Change getLeapFlag() to isLeapYear().
//					Move subtract() to TimeUtil.diff() since
//					it is more of a utility and does not
//					operate on the instance.
// 2001-12-27	SAM, RTi		Changed fixedRead() calls to new
//					standard - blank fields are not
//					returned.  Hopefully this increases
//					performance some.
// 2002-01-16	SAM, RTi		Fix bug where _behavior_flag is not
//					being disagreggated correctly.
//					Fix some places where result of & was
//					being compared to 1 rather than != 0.
// 2002-07-05	SAM, RTi		Add setDate() to allow a DateTime to be
//					set using a Date.  This increates
//					performance (e.g., when iterating using
//					dates from a database record).
// 2003-01-27	SAM, RTi		Fix bug in behavior of the comareTo()
//					method.
// 2003-11-03	SAM, RTi		When creating a zero DateTime,
//					initialize the time zone to an empty
//					String.  Previously, it was getting set
//					to the local computer time zone, which
//					causes unexpected results.
// 2004-01-19	J. Thomas Sapienza, RTi	Added check in the DateTime(Date) 
//					constructor for null dates.
// 2004-03-01	SAM, RTi		Fix bug where parsing an hour of 24 was
//					setting the day to 1, not day + 1.
// 2004-03-04	JTS, RTi		Class is now serializable.
// 2004-03-12	SAM, RTi		Add support for parsing permutations of
//					M/D/YYYY, M/DD/YYYY, MM/D/YYYY,
//					MM/DD/YYYY for daily precision dates.
//					This involved overloading parse() to
//					take an additional flag in the private
//					method.  The original public method is
//					still available.  Additional work may
//					be needed if non-USA standard dates are
//					also parsed.  The preference is still to
//					use ISO standard formats.
// 2004-04-05	SAM, RTi		Fix a bug in setPrecision() where if the
//					flag does not contain precision
//					information, the precision was
//					defaulting to IRREGULAR.
// 2004-04-06	SAM, RTi		Fix a bug in setSecond() where check for
//					_iszero was against 1 - it is now
//					changed to check against 0.
// 2004-04-14	SAM, RTi		* Overload setPrecision() to have a flag
//					  indicating whether the set should be
//					  cumulative or a reset.  This resolves
//					  problems where, for example, a date is
//					  parsed with time zone and later the
//					  precision is set, ignoring the time
//					  zone flag.  The change can now be
//					  cumulative so the previous settings
//					  are not totally reset.
//					* Change setTimeZone() to set the
//					  _use_time_zone flag to true.
//					* Fix setDate() since it was treating
//					  _precision as a bit mask and not a
//					  simple integer.
//					* When constructing from a Date, do NOT
//					  set the time zone unless the behavior
//					  flags asks for the time zone.
//					* When setting to current time, DO use
//					  the time zone (will be ignored in code
//					  if daily or courser precision).
// 2004-04-27	SAM, RTi		* Change parse for FORMAT_MM_SLASH_YYYY
//					  to handle 1-digit month without using
//					  StringTokenizer.
// 2004-10-21	JTS, RTi		Added FORMAT_DD_SLASH_MM_SLASH_YYYY.
// 2004-10-27	JTS, RTi		Class now implements Comparable so that
//					it can easily be sorted using the
//					Collections.sort() method.
// 2005-02-23	SAM, RTi		Overload lessThan(),
//					lessThanOrEqualTo(), greaterThan(), and
//					greaterThanOrEqualTo() to take a
//					precision, to facilitate processing of
//					data with different precision.  So as to
//					not reduce performance, inline the code
//					rather than having one method call the
//					other (these methods are used
//					extensively when iterating).
// 2005-09-01	SAM, RTi		parse() was not throwing exceptions in
//					all cases if a string did not match a
//					criteria.  Add the exception.
// 2005-12-14	SAM, RTi		Overload parse() to recognize DateTime
//					expressions.
// 2006-04-16	SAM, RTi		Throw an exception if a date/time string
//					length is not recognized in parsing.
// 2006-04-20	JTS, RTi		Added subtractInterval().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// 2012-04-20   HSK, RTi        Added equals(Object) and hashCode overrides.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.Time;

import java.io.Serializable;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.Time.TimeUtil;

import RTi.Util.String.StringUtil;

/**
The DateTime class provides date/time storage and manipulation for general use.
Unlike the Java Date and Calendar classes, this class allows date/time
data fields to be easily set and manipulated.
Specific features of the DateTime class are:
<ul>
<li>	An optional bitmask flag can be used at construction to indicate the
	precision (which matches the TimeInterval values), initialization
	(to zero or current date/time), and performance (fast or strict).
	TimeInterval.YEAR is equal to DateTime.PRECISION_YEAR, etc.</li>
<li>	The precision values are mutually exclusive; therefore, they can be
	compared as binary mask values or with ==.</li>
<li>	By default the time zone is not used in DateTime manipulation or output.
	However, if the PRECISION_TIME_ZONE flag is set during creation or with
	a call to setTimeZone(), then the time zone is intended to be used
	throughout (comparison, output, etc.).</li>
<li>	DateTime objects can be used in TimeUtil methods in a generic way.</li>
<li>	Call isZero() to see whether the DateTime has zero values.  A zero
	DateTime means that the date is not the current time and values have
	not been reset from the defaults.</li>
<li>	Precisions allow "abbreviating" a DateTime to consider only certain
	data fields.  By default, the larger interval data (e.g., year) are
	included and only smaller data (e.g., seconds) can be cut out of the
	precision.  If the TIME_ONLY bitmask is used at creation, then the
	date fields can be ignored.</li>
</ul>
*/
public class DateTime implements Cloneable, Comparable, Serializable
{

/**
Flags for constructing DateTime instances, which modify their behavior.
These flags have values that do not conflict with the TimeInterval base interval
values and the flags can be combined in a DateTime constructor.
The following flag indicates that a DateTime be treated strictly, meaning that
the following dependent data are reset each time a date field changes:
<p>
day of year<br>
whether a leap year<br>

<p>
This results in slower processing of dates and is the default behavior.  For
iterators, it is usually best to use the DATE_FAST behavior.
*/
public static final int DATE_STRICT	= 0x1000;

/**
Indicates that dates need not be treated strictly.  This is useful for faster
processing of dates in loop iterators.
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
These flags may at some
point replace the flags used for the equals method.  If not specified, all of
the date/time fields for the DateTime are carried (PRECISION_HSECOND).  Note
that these values are consistent with the TimeInterval base interval values.
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
Create a DateTime with a precision that includes the time zone (and may include
another precision flag).
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
The following returns an empty string for formatting but can be used to
indicate no formatting in other code.
*/
public static final int FORMAT_NONE = 1;
/**
The following returns the default format and can be used to
indicate automatic formatting in other code.
*/
public static final int FORMAT_AUTOMATIC = 2;
/**
The following formats a date as follows:  "DD/MM/YYYY".  This date format
cannot be parsed properly by parse(); FORMAT_MM_SLASH_DD_SLASH_YYYY will be returned instead.
*/
public static final int FORMAT_DD_SLASH_MM_SLASH_YYYY = 27;
/**
The following formats a date as follows:  "HH:mm".
*/
public static final int FORMAT_HH_mm = 3;
/**
The following formats a date as follows (military time):  "HHmm".
*/
public static final int FORMAT_HHmm = 4;
/**
The following formats a date as follows:  "MM".  Parsing of this date format
without specifying the format is NOT supported because it is ambiguous.
*/
public static final int FORMAT_MM = 5;
/**
The following formats a date as follows:  "MM-DD".
*/
public static final int FORMAT_MM_DD = 6;
/**
The following formats a date as follows:  "MM/DD".
*/
public static final int FORMAT_MM_SLASH_DD = 7;
/**
The following formats a date as follows:  "MM/DD/YY".
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YY = 8;
/**
The following formats a date as follows:  "MM/DD/YYYY".
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY = 9;
/**
The following formats a date as follows:  "MM/DD/YYYY HH".
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH = 10;
/**
The following formats a date as follows:  "MM-DD-YYYY HH".
*/
public static final int FORMAT_MM_DD_YYYY_HH = 11;
/**
The following formats a date as follows:  "MM/DD/YYYY HH:mm".  For the parse() method,
months, days, and hours that are not padded with zeros will also be parsed properly.
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm = 12;
/**
The following formats a date as follows:  "MM/YYYY".
*/
public static final int FORMAT_MM_SLASH_YYYY = 13;
/**
The following formats a date as follows:  "YYYY".
*/
public static final int FORMAT_YYYY = 14;
/**
The following formats a date as follows:  "YYYY-MM".
*/
public static final int FORMAT_YYYY_MM = 15;
/**
The following formats a date as follows:  "YYYY-MM-DD".
*/
public static final int FORMAT_YYYY_MM_DD = 16;
/**
The following is equivalent to FORMAT_YYYY_MM_DD.
*/
public static final int FORMAT_Y2K_SHORT = FORMAT_YYYY_MM_DD;
/**
The following formats a date as follows:  "YYYY-MM-DD HH".
*/
public static final int FORMAT_YYYY_MM_DD_HH = 17;
/**
The following formats a date as follows:  "YYYY-MM-DD HH ZZZ".
*/
public static final int FORMAT_YYYY_MM_DD_HH_ZZZ = 18;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm".
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm	= 19;
/**
The following is equivalent to FORMAT_YYYY_MM_DD_HH_mm.
*/
public static final int FORMAT_Y2K_LONG = FORMAT_YYYY_MM_DD_HH_mm;
/**
The following formats a date as follows:  "YYYY-MM-DD HHmm".
*/
public static final int FORMAT_YYYY_MM_DD_HHmm = 20;
/**
The following formats a date as follows:  "YYYYMMDDHHmm".
*/
public static final int FORMAT_YYYYMMDDHHmm = 21;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm ZZZ".
This format is currently only supported for toString() (not parse).
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_ZZZ = 22;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm:SS".
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS = 23;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm:SS:hh".
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_hh = 24;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm:SS:hh ZZZ".
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ = 25;
/**
The following formats a date as follows:  "YYYY-MM-DD HH:mm:SS ZZZ".
*/
public static final int FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ = 26;
/**
The following formats a date as follows:  "MM/DD/YYYY HH:mm:SS".
*/
public static final int FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS = 28;


/**
Hundredths of a second (0-99).
*/
private int __hsecond;

/**
Seconds (0-59).
*/
private int __second;

/**
Minutes past hour (0-59).
*/
private int __minute;

/**
Hours past midnight (0-23).  Important - hour 24 in data should be handled as
hour 0 of the next day.
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
Day of week (0=Sunday).
*/
private int __weekday;

/**
Day of year (0-365).
*/
private int	__yearday;

/**
Absolute month (year*12 + month).
*/
private int __abs_month;

/**
Precision of the DateTime (allows some optimization and automatic
decisions when converting). This is the PRECISION_* value only
(not a bit mask).  _use_time_zone and _time_only control other precision information.
*/
private int __precision;

/**
Flag for special behavior of dates.  Internally this contains all the
behavior flags but for the most part it is only used for ZERO/CURRENT and FAST/STRICT checks.
*/
private int __behavior_flag;

/**
Indicates whether the time zone should be used when processing the DateTime
*/
private boolean __use_time_zone = false;

/**
Use only times for the DateTime.
*/
private boolean	__time_only = false;

/**
Default constructor (set to zero time).
*/
public DateTime ()
{	setToZero ();
	reset();
}

/**
Construct using the constructor modifiers (combination of PRECISION_*,
DATE_CURRENT, DATE_ZERO, DATE_STRICT, DATE_FAST).  If no modifiers are given,
the date/time is initialized to zeros and precision is PRECISION_MINUTE.
@param flag Constructor modifier.
*/
public DateTime ( int flag )
{	if ( (flag & DATE_CURRENT) != 0 ) {
		setToCurrent ();
	}
	else {	// Default...
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
public DateTime ( Date d )
{	// If a null date is passed in, behave like the default DateTime() constructor.
	if (d == null) {
		setToZero ();
		reset();
		return;
	}

	// use_deprecated indicates whether to use the deprecated Date
	// functions.  These should be fast (no strings) but are, of course, deprecated.
	boolean	use_deprecated = true;

	if ( use_deprecated ) {
		// Returns the number of years since 1900!
		int year = d.getYear();
		setYear ( year + 1900 );
		// Returned month is 0 to 11!
		setMonth ( d.getMonth() + 1 );
		// Returned day is 1 to 31
		setDay ( d.getDate() );
		setPrecision ( PRECISION_DAY );
		// Sometimes Dates are instantiated from data where hours, etc.
		// are not available (e.g. from a database date/time).
		// Therefore, catch exceptions at each step...
		try {
            // Returned hours are 0 to 23
			setHour ( d.getHours() );
			setPrecision ( PRECISION_HOUR );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned hours are 0 to 59 
			setMinute ( d.getMinutes() );
			setPrecision ( PRECISION_MINUTE );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned seconds are 0 to 59
			setSecond ( d.getSeconds() );
			setPrecision ( PRECISION_SECOND );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		// TODO SAM 2015-08-12 For now do not set the hundredths of a second
		__tz = "";
	}
	else {
        // Date/Calendar are ugly to work with, let's get information by formatting strings...

		// year month
		// Use the formatTimeString routine instead of the following...
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
		// milliseconds not supported in formatTimeString...
		// Convert from milliseconds to 100ths of a second...
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
@param behavior_flag Flag indicating the behavior of the instance - see the
defined bit mask values.
*/
public DateTime ( Date d, int behavior_flag )
{	boolean	use_deprecated = true;

	if ( use_deprecated ) {
		// Returns the number of years since 1900!
		int year = d.getYear();
		setYear ( year + 1900 );
		// Returned month is 0 to 11!
		setMonth ( d.getMonth() + 1 );
		// Returned day is 1 to 31
		setDay ( d.getDate() );
		// Sometimes Dates are instantiated from data where hours, etc.
		// are not available (e.g. from a database date/time).
		// Therefore, catch exceptions at each step...
		try {
            // Returned hours are 0 to 23
			setHour ( d.getHours() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned hours are 0 to 59 
			setMinute ( d.getMinutes() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		try {
            // Returned seconds are 0 to 59
			setSecond ( d.getSeconds() );
		}
		catch ( Exception e ) {
			// Don't do anything.  Just leave the DateTime default.
		}
		__tz = "";
	}
	else {
        // Date/Calendar are ugly to work with, so get information by formatting strings...

		// year month
		// Use the formatTimeString routine instead of the
		// following...
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
		// milliseconds not supported in formatTimeString...
		// Convert from milliseconds to 100ths of a second...
		// setHSecond ( Integer.parseInt((String)v.elementAt(6))/10 );
		// setTimeZone ( (String)v.elementAt(7) );
		__tz = "";
	}

	// Set the time zone.  Use TimeUtil directly to increase performance...

	if ( (behavior_flag&PRECISION_TIME_ZONE) != 0 ) {
		if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ONCE ) {
			if ( !TimeUtil._local_time_zone_retrieved ) {
				// Need to initialize...
				setTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
			}
			else {
                // Use the existing data...
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
public DateTime ( DateTime t )
{	if ( t != null ) {
		__hsecond = t.__hsecond;
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
        // Constructing from null usually means that there is a code
		// logic problem with exception handling...
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
public DateTime ( DateTime t, String newtz )
{	if ( t != null ) {
		// First copy...

		__hsecond = t.__hsecond;
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

		// Now compute the time zone offset...

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
        // Constructing from null usually means that there is a code
		// logic problem with exception handling...
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	reset();
}

/**
Construct using a DateTime and a behavior flag.
@param t DateTime to copy.
@param flag Constructor flags.  Because the DATE_ZERO flag will override the
copy, it is ignored.
*/
public DateTime ( DateTime t, int flag )
{	if ( t != null ) {
		__year = t.__year;
		__month	= t.__month;
		__day = t.__day;
		__hour = t.__hour;
		__minute = t.__minute;
		__second = t.__second;
		__hsecond = t.__hsecond;
		__isleap = t.__isleap;
		__weekday = t.__weekday;
		__yearday = t.__yearday;
		__abs_month	= t.__abs_month;
		__behavior_flag	= flag;
		__iszero = t.__iszero;
		// Set the precision.  The call to setPrecision() will result
		// in the final precision and, if necessary, will reset data
		// values back to initial values to prevent remainder values
		// from being used (e.g., for plot positions, offsets)...
		__precision	= t.__precision;
		__use_time_zone	= t.__use_time_zone;
		__time_only	= t.__time_only;
		// May be reset here...
		setPrecision ( flag );
		__tz = t.__tz;
	}
	else {
        // Constructing from null usually means that there is a code
		// logic problem with exception handling...
		Message.printWarning ( 2, "DateTime", "Constructing DateTime from null - will have zero date!" );
		setToZero ();
	}
	reset();
}

/**
Construct from a double precision number containing the date (the inverse of
the toDouble method.  The number consists of YYYY.DDHHMMSS, etc., where the
remainder is the fractional part of the year, based on days.  Because the
relationship between months and days is dynamic, using this routine on the
difference between two dates is not generally correct, and the "use_month" option is provided.
@param double_date Date as a double.
@param use_month If true, the resulting DateTime will treat the month and day
as normal.  If false, the month will be set to 0 and the days will be the
total number of days in the year.  The latter version can be used when
processing an absolute date offset.
@see #toDouble
*/
public DateTime ( double double_date, boolean use_month )
{	// Initialize...

	setToZero ();

	// First get the year as the whole part of the number.  Because we
	// don't want to have a date like xxx 59:59:59:99 result from
	// round-off, add .1 100th of a second to the number so that the number truncates correctly.
	// .001 100th of a second as a percentage of the day is...
	// 1/86400000;	// 1000*100*60*60*24

	__year = (int)double_date;
	double temp = double_date - (double)__year + .00000000011574074;

	// Get the full number of days in the year...
	
	double	ydays = (double)TimeUtil.numDaysInYear(__year);
	temp *= ydays;

	// The days is the remainder times the number of days in the year...

	int monthdays;
	boolean isleap = TimeUtil.isLeapYear ( __year );
	if ( use_month ) {
		// Use the month and day as usual...
		int [] v = TimeUtil.getMonthAndDayFromDayOfYear ( __year, (int)temp );
		if ( v != null ) {
			__month	= v[0];
			__day = v[1] + 1; // Because the day is always at least one (always in a day
						// even if it is a fractional day).
			if ( (__month == 2) && isleap ) {
				monthdays = TimeUtil.MONTH_DAYS[__month - 1] + 1;
			}
			else {
                monthdays = TimeUtil.MONTH_DAYS[__month - 1];
			}
			if ( __day > monthdays ) {
				// Have gone into the next month by incrementing the day...
				++__month;
				__day = 1;
			}
		}
	}
	else {
	    // Set the month to zero and the days to the number of days in the year...
		__month = 0;
		__day = (int)temp;
	}

	temp -= (double)((int)temp);

	// Now the remainder is the hours, etc.  Multiply by 24 to get the hour...

	temp *= 24.0;
	__hour = (int)temp;
	temp -= (double)((int)temp);

	// Now the remainder is the minutes, etc.  Multiply by 60 to get the minute...

	temp *= 60.0;
	__minute = (int)temp;
	temp -= (double)((int)temp);

	// Now the remainder is the seconds, etc.  Multiply by 60 to get the seconds...

	temp *= 60.0;
	__second = (int)temp;
	temp -= (double)((int)temp);

	// Now the remainder is the hseconds, etc.  Multiply by 100 to get the hseconds...

	temp *= 100.0;
	__hsecond= (int)temp;

	// Reset...

	reset ();
	__iszero = false;
}

/**
Add a date offset to the date.  This is accomplished by adding the smallest
unit of time first to allow for resets of larger units.  It is therefore
important that all time components are zero except for the values to be added.
This will be the case if subtract() was used to compute the offset.  Negative
offsets are allowed.  It may be desirable to overload this method to use the
date precision or automatically process the precision (enhancement for later).
Also, although the month value will be added if non-zero, it is recommended that
only days be specified (and days > 31 is allowed).  The output from subtract()
will default to setting month to zero and is therefore compatible with this method.
<b>Currently the precision of the instance is not considered.  Therefore, the
offset fields should be set to zero if not used.</b>
@param offset Date offset to add.
*/
public void add ( DateTime offset )
{	// Add the values in increasing order of size...

	if ( offset == null ) {
		Message.printWarning ( 2, "DateTime.add", "Null offset" );
		return;
	}

	addHSecond ( offset.__hsecond );
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
public void addDay ( int add )
{	int i;

	if ( add == 1 ) {
		int num_days_in_month = TimeUtil.numDaysInMonth (__month, __year);
		++__day;
		if ( __day > num_days_in_month ) {
			// Have gone into the next month...
			__day -= num_days_in_month;
			addMonth( 1 );
		}
		// Reset the private data members.
		setYearDay();
	}
	// Else...
	// Figure out if we are trying to add more than one day.
	// If so, recurse (might be a faster way, but this works)...
	else if ( add > 0 ) {
		for ( i = 0; i < add; i++ ) {
			addDay ( 1 );
		}
	}
	else if ( add == -1 ) {
		--__day;
		if ( __day < 1 ) {
			// Have gone into the previous month...
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
public void addHour ( int add )
{	int	daystoadd;

	// First add the days, if necessary...

	if ( add >= 24 || add <= -24 ) {
		// First need to add/subtract days to time...
		daystoadd = add/24;
		addDay( daystoadd );
	}

	// Now add the remainder

	if ( add > 0 ) {
		__hour += (add%24);
		if ( __hour > 23 ) {
			// Have gone into the next day...
			__hour -= 24;
			addDay( 1 );
		}
	}
	else if ( add < 0 ) {
		__hour += (add%24);
		if ( __hour < 0 ) {
			// Have gone into the previous day...
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
public void addHSecond ( int add )
{	int	secs;

	// First add to the second if necessary...

	if ( add >= 100 || add <= -100 ) {
		// Need to add/subtract seconds first
		secs = add/100;
		addSecond( secs );
	}

	if ( add > 0 ) {
		__hsecond += add % 100;
		if ( __hsecond > 99 ) {
			// Need to add a second and subtract the same from hsecond
			__hsecond -= 100;
			addSecond( 1 );
		}
	}
	else if ( add < 0 ) {
		__hsecond += add % 100;
		if ( __hsecond < 0 ) {
			// Need to subtract a second and add the same to second
			__hsecond += 100;
			addSecond( -1 );
		}
	}
	__iszero = false;
}

/**
Add a time series interval to the DateTime (see TimeInterval).  This is useful when iterating a date.
An irregular interval is ignored (the date is not changed).
@param interval Time series base interval.
@param add Multiplier for base interval.
*/
public void addInterval ( int interval, int add )
{	// Based on the interval, call lower-level routines...

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
        // Unsupported interval...
        // TODO SAM 2007-12-20 Evaluate throwing InvalidTimeIntervalException
        String message = "Interval " + interval + " is unsupported";
		Message.printWarning ( 2, "DateTime.addInterval", message );
		return;
    }
	__iszero = false;
}

/**
Add minute(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of minutes to add (can be a multiple and can be negative).
*/
public void addMinute ( int add )
{	int	hrs;

	// First see if multiple hours need to be added...

	if ( add >= 60 || add <= -60 ) {
		// Need to add/subtract hour(s) first
		hrs = add/60;
		addHour( hrs );
	}

	if ( add > 0 ) {
		__minute += add % 60;
		if ( __minute > 59 ) {
			// Need to add an hour and subtract the same from minute
			__minute -= 60;
			addHour( 1 );
		}
	}
	else if ( add < 0 ) {
		__minute += add % 60;
		if ( __minute < 0 ) {
			// Need to subtract an hour and add the same to minute
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
public void addMonth ( int add )
{	int	i;

	if ( add == 0 ) {
		return;
	}
	if ( add == 1 ) {
		// Dealing with one month...
		__month += add;
		// Have added one month so check if went into the next year
		if ( __month > 12 ) {
			// Have gone into the next year...
			__month = 1;
			addYear( 1 );
		}
	}
	// Else...
	// Loop through the number to add/subtract...
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
		// Have subtracted the specified number so check if in the previous year
		if ( __month < 1 ) {
			// Have gone into the previous year...
			__month = 12;
			addYear( -1 );
		}
	}
	else if ( add < 0 ) {
		for ( i = 0; i > add; i-- ) {
			addMonth ( -1 );
		}
		// No need to reset because it was done int the previous call.
		return;
	}
	else {
	    // Zero...
		return;
	}
	// Reset time
	setAbsoluteMonth();
	setYearDay();
	__iszero = false;
}

/**
Add second(s) to the DateTime.  Other fields will be adjusted if necessary.
@param add Indicates the number of seconds to add (can be a multiple and can be negative).
*/
public void addSecond ( int add )
{	int	mins;

	// Add/subtract minutes, if necessary...

	if ( add >= 60 || add <= -60 ) {
		// Need to add/subtract minute(s) first
		mins = add/60;
		addMinute( mins );
	}

	if ( add > 0 ) {
		__second += add % 60;
		if ( __second > 59 ) {
			// Need to add a minute and subtract the same from second
			__second -= 60;
			addMinute( 1 );
		}
	}
	else if ( add < 0 ) {
		__second += add % 60;
		if ( __second < 0 ) {
			// Need to subtract a minute and add the same to second
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
public void addWeek ( int add )
{	addDay ( add*7 );
}

// TODO SAM 2007-12-20 Evaluate what to do about adding a year if on Feb 29.
/**
Add year(s) to the DateTime.  The month and day are NOT adjusted if an
inconsistency occurs with leap year information.
@param add Indicates the number of years to add (can be a multiple and can be negative).
*/
public void addYear ( int add )
{	__year += add;
	reset ();
	__iszero = false;
}

/**
 * Returns whether this date is within the specified date range, inclusive of the end-points.
 * 
 * @param startDate beginning of date range
 * @param endDate end of date range
 */
public boolean between(DateTime startDate, DateTime endDate)
{
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
public Object clone ()
{	try {
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
public int compareTo ( DateTime t )
{	if ( equals(t) ) {
		return 0;
	}
	else if ( lessThan(t) ) {
		return -1;
	}
	else {	return 1;
	}
}

/**
Determine if this DateTime is less than, equal to, or greater than another DateTime.
@return -1 if this DateTime is less than "t", 0 if this DateTime is the same as
"t", and 1 if this DateTime is greater than "t".
@param t Date to compare.
*/
public int compareTo ( Object t )
{	
	if (t instanceof DateTime) {
		return compareTo((DateTime)t);
	}
	else {
		return -1;
	}
}

@Override
public boolean equals(Object o) 
{
    if (o instanceof DateTime) {
        return equals((DateTime) o);
    } else {
        return false;
    }
}

@Override
public int hashCode() {
    int hash = 3;
    hash = 67 * hash + this.__hsecond;
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
Time zone is not currently checked but may be checked in the future if the
PRECISION_TIME_ZONE flag is set.
@return true if the date is the same as the instance.
@param t DateTime to compare.
*/
public boolean equals ( DateTime t )
{	return equals ( t, __precision );
	// TODO SAM 2005-02-24 should the code from the overloaded method
	// be inlined here to improve performance?  It does not seem that
	// equals is used in iterations quite as much as other methods.
	// Don't inline the code for now.
}

/**
Determine if a DateTimes is equal to this instance.
<b>If the instance is a time only (no date), then only the time data are compared.</b>
Time zone is checked if it has been set for the instance.
@return true if the date is equivalent to the given precision.
@param precision Indicates the precision to use for the comparison.
*/
public boolean equals ( DateTime t, int precision )
{	// Maybe can't do this because we are more concerned with precision?
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
	if ( __hsecond != t.__hsecond ) {
		return false;
	}
	if ( precision == PRECISION_HSECOND ) {
		if ( __use_time_zone && !__tz.equalsIgnoreCase(t.__tz) ) {
			return false;
		}
		return true;
	}

	// They are not equal

	return false;
}

/**
Return the absolute day.
@return The absolute day.  This is a computed value.
@see RTi.Util.Time.TimeUtil#absoluteDay
*/
public int getAbsoluteDay()
{	return TimeUtil.absoluteDay ( __year, __month, __day );
}

/**
Return the absolute month.
@return The absolute month (year*12 + month).
*/
public int getAbsoluteMonth( )
{	// Since some data are public, recompute...
	return (__year*12 + __month);
}

/**
Return the DateTime behavior flag.  Note that the higher bits of the behavior
flag can be checked easily.  However, the precision must be determined from
the behavior flag by disaggregating the flag.  Use getPrecision() to get an
exact value for the precision.
@return The behavior flag (bit mask).
*/
public long getBehaviorFlag ()
{	return __behavior_flag;
}

/**
Return the Java Date corresponding to the DateTime, ignoring the time zone.
@return Java Date corresponding to the DateTime, ignoring the time zone.
*/
public Date getDate ()
{	return getDate ( 0 );
}

/**
Return the Java Date corresponding to the DateTime.
@return Java Date corresponding to the DateTime or null if unable to determine conversion.
@param flag Indicates whether time zone should be shifted from the DateTime to
the Date time zone (GMT) (true).
*/
public Date getDate ( int flag )
{	GregorianCalendar c = null;
	if ( (flag & DATE_STRICT) != 0 ) {
		// Do care what the time zone is.  Make the returned date exactly match the DateTime, but in GMT.
		// For now, just do the same for both cases...
		c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
		Date d = c.getTime();
		c = null;
		return d;
	}
	else {
        // Don't care about the time zone.  Just use the other data fields...
		c = new GregorianCalendar ( __year, (__month - 1), __day, __hour, __minute, __second );
	}
	if ( c != null ) {
		Date d = c.getTime();
		c = null;
		return d;
	}
	else {
        return null;
	}
}

/**
Return the day.
@return The day.
*/
public int getDay( ) 
{	return __day;
}

/**
Return the hour.
@return The hour.
*/
public int getHour( )
{	return __hour;
}

/**
Return the 100-th second.
@return The hundredth-second.
*/
public int getHSecond( )
{	return __hsecond;
}

/**
Return the minute.
@return The minute.
*/
public int getMinute( )
{	return __minute;
}

/**
Return the month.
@return The month.
*/
public int getMonth( )
{	return __month;
}

/**
Return the DateTime precision.
@return The precision (see PRECISION*).
*/
public int getPrecision ()
{	return __precision;
}

/**
Return the second.
@return The second.
*/
public int getSecond( )
{	return __second;
}

/**
Return the time zone abbreviation.
@return The time zone abbreviation.
*/
public String getTimeZoneAbbreviation ( )
{	return __tz;
}

/**
Return the week day by returning getDate().getDay().
@return The week day (Sunday is 0).
*/
public int getWeekDay ( )
{	//return _weekday;
    return getDate().getDay();
}

/**
Return the year.
@return The year.
*/
public int getYear( )
{	return __year;
}

/**
Return the Julian day in the year.
@return The day of the year where Jan 1 is 1.  If the behavior of the DateTime
is DATE_FAST, zero is likely to be returned because the day of the year is
not automatically recomputed.
*/
public int getYearDay()
{	// Need to set it...
	setYearDay();
	return __yearday;
}

/**
Determine if the instance is greater than another date.  Time zone is not
considered in the comparison (no time zone shift is made).  The comparison is
made at the precision of the instance.
@return true if the instance is greater than the given date.
@param t DateTime to compare.
*/
public boolean greaterThan ( DateTime t )
{	if ( !__time_only ) {
		if ( __year < t.__year) {
			return false;
		}
		else {
            if(__year > t.__year) {
				return true;
			}
		}
	
		if ( __precision == PRECISION_YEAR ) {
			// Equal so return false...
			return false;
		}

		// otherwise years are equal so check months
	
		if(__month < t.__month) {
			return false;
		}
		else {
            if(__month > t.__month) {
				return true;
			}
		}

		if ( __precision == PRECISION_MONTH ) {
			// Equal so return false...
			return false;
		}

		// months must be equal so check day

		if (__day < t.__day) {
			return false;
		}
		else {
            if(__day > t.__day) {
				return true;
			}
		}

		if ( __precision == PRECISION_DAY ) {
			// Equal so return false...
			return false;
		}
	}

	// days are equal so check hour

	if (__hour < t.__hour) {
		return false;
	}
	else {
        if(__hour > t.__hour) {
			return true;
		}
	}

	if ( __precision == PRECISION_HOUR ) {
		// Equal so return false...
		return false;
	}

	// means that hours match - so check minute

	if( __minute < t.__minute ) {
		return false;
	}
	else {
        if( __minute > t.__minute ) {
			return true;
		}
	}

	if ( __precision == PRECISION_MINUTE ) {
		// Equal so return false...
		return false;
	}

	// means that minutes match - so check second

	if( __second < t.__second ) {
		return false;
	}
	else {
        if( __second > t.__second ) {
			return true;
		}
	}

	if ( __precision == PRECISION_SECOND ) {
		// Equal so return false...
		return false;
	}

	// means that seconds match - so check hundredths of seconds

	if( __hsecond < t.__hsecond ) {
		return false;
	}
	else {
        if( __hsecond > t.__hsecond ) {
			return true;
		}
	}
	// means they are equal

	return false;
}

/**
Determine if the instance is greater than another date.  Time zone is not
considered in the comparison (no time zone shift is made).  The comparison is
made at the specified precision.
@return true if the instance is greater than the given date.
@param t DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean greaterThan ( DateTime t, int precision )
{	// Inline the code to increase performance
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
			// Equal so return false...
			return false;
		}

		// otherwise years are equal so check months
	
		if(__month < t.__month) {
			return false;
		}
		else {
            if(__month > t.__month) {
				return true;
			}
		}

		if ( precision == PRECISION_MONTH ) {
			// Equal so return false...
			return false;
		}

		// months must be equal so check day

		if (__day < t.__day) {
			return false;
		}
		else {
            if(__day > t.__day) {
				return true;
			}
		}

		if ( precision == PRECISION_DAY ) {
			// Equal so return false...
			return false;
		}
	}

	// days are equal so check hour

	if (__hour < t.__hour) {
		return false;
	}
	else {
        if(__hour > t.__hour) {
			return true;
		}
	}

	if ( precision == PRECISION_HOUR ) {
		// Equal so return false...
		return false;
	}

	// means that hours match - so check minute

	if( __minute < t.__minute ) {
		return false;
	}
	else {
        if( __minute > t.__minute ) {
			return true;
		}
	}

	if ( precision == PRECISION_MINUTE ) {
		// Equal so return false...
		return false;
	}

	// means that minutes match - so check second

	if( __second < t.__second ) {
		return false;
	}
	else {
        if( __second > t.__second ) {
			return true;
		}
	}

	if ( precision == PRECISION_SECOND ) {
		// Equal so return false...
		return false;
	}

	// means that seconds match - so check hundredths of seconds

	if( __hsecond < t.__hsecond ) {
		return false;
	}
	else {
        if( __hsecond > t.__hsecond ) {
			return true;
		}
	}
	// means they are equal

	return false;
}

/**
Determine if the DateTime is >= another DateTime.  Time zone is not
considered in the comparison (no time zone shift is made).
@return true if the instance is >= the given DateTime.
@param d DateTime to compare.
*/
public boolean greaterThanOrEqualTo ( DateTime d )
{	if ( !lessThan(d) ) {
		return true;
	}
	else {
        return false;
	}
}

/**
Determine if the DateTime is >= another DateTime.  Time zone is not
considered in the comparison (no time zone shift is made).
@return true if the instance is >= the given DateTime.
@param d DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean greaterThanOrEqualTo ( DateTime d, int precision )
{	if ( !lessThan(d, precision) ) {
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
public boolean isLeapYear ( )
{	// Reset to make sure...
	__isleap = TimeUtil.isLeapYear( __year );
    return __isleap;
}

/**
Indicate whether a zero DateTime, meaning a DateTime that was created as a
zero date and never modified.
@return true if data are initialized to zero values, without further changes.
*/
public boolean isZero ( )
{	if ( !__iszero ) {
		// Something has been modified...
		return __iszero;
	}
	else {
        // Check here whether anything is different from the default.  This will
		// only be a performance hit if the DateTime never has anything changed.
		if ( (__year != 0) ||
			(__month != 1) ||
			(__day != 1) ||
			(__hour != 0) ||
			(__minute != 0) ||
			(__second != 0) ||
			(__hsecond != 0) ) {
			__iszero = false;
		}
	}
	return __iszero;
}

/**
Determine if the DateTime is less than another DateTime.  Time zone is not
considered in the comparison (no time zone shift is made).  The precision of the
instance is used for the comparison.
@return true if the instance is less than the given DateTime.
@param t DateTime to compare.
*/
public boolean lessThan ( DateTime t )
{	// Inline the comparisons here even though we could call other methods
	// because we'd have to call greaterThan() and equals() to know for sure.
	if ( !__time_only ) {
		if( __year < t.__year) {
			return true;
		}
		else {
            if(__year > t.__year) {
				return false;
			}
		}
	
		if ( __precision == PRECISION_YEAR ) {
			// Equal so return false...
			return false;
		}

		// otherwise years are equal so check months
	
		if(__month < t.__month) {
			return true;
		}
		else {
            if(__month > t.__month) {
				return false;
			}
		}
	
		if ( __precision == PRECISION_MONTH ) {
			// Equal so return false...
			return false;
		}

		// months must be equal so check day
	
		if (__day < t.__day) {
			return true;
		}
		else {
            if(__day > t.__day) {
				return false;
			}
		}

		if ( __precision == PRECISION_DAY ) {
			// Equal so return false...
			return false;
		}
	}

	// days are equal so check hour

	if (__hour < t.__hour) {
		return true;
	}
	else {
        if(__hour > t.__hour) {
			return false;
		}
	}

	if ( __precision == PRECISION_HOUR ) {
		// Equal so return false...
		return false;
	}

	// hours are equal so check minutes

	if( __minute < t.__minute ) {
		return true;
	}
	else {
        if( __minute > t.__minute ) {
			return false;
		}
	}

	if ( __precision == PRECISION_MINUTE ) {
		// Equal so return false...
		return false;
	}

	// means that minutes match - so check second

	if( __second < t.__second ) {
		return true;
	}
	else {
        if( __second > t.__second ) {
			return false;
		}
	}

	if ( __precision == PRECISION_SECOND ) {
		// Equal so return false...
		return false;
	}

	// means that seconds match - so check hundredths of seconds

	if( __hsecond < t.__hsecond ) {
		return true;
	}
	else {
        if( __hsecond > t.__hsecond ) {
			return false;
		}
	}

	// everything must be equal so not less than

	return false;
}

/**
Determine if the DateTime is less than another DateTime.  Time zone is not
considered in the comparison (no time zone shift is made).  The specified
precision is used for the comparison.
@return true if the instance is less than the given DateTime.
@param t DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean lessThan ( DateTime t, int precision )
{	// Inline the overall code and comparisons here even though we could
	// call other methods because we'd have to call greaterThan() and
	// equals() to know for sure.
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
			// Equal so return false...
			return false;
		}

		// otherwise years are equal so check months
	
		if(__month < t.__month) {
			return true;
		}
		else {
            if(__month > t.__month) {
				return false;
			}
		}
	
		if ( precision == PRECISION_MONTH ) {
			// Equal so return false...
			return false;
		}

		// months must be equal so check day
	
		if (__day < t.__day) {
			return true;
		}
		else {
            if(__day > t.__day) {
				return false;
			}
		}

		if ( precision == PRECISION_DAY ) {
			// Equal so return false...
			return false;
		}
	}

	// days are equal so check hour

	if (__hour < t.__hour) {
		return true;
	}
	else {
        if(__hour > t.__hour) {
			return false;
		}
	}

	if ( precision == PRECISION_HOUR ) {
		// Equal so return false...
		return false;
	}

	// hours are equal so check minutes

	if( __minute < t.__minute ) {
		return true;
	}
	else {
        if( __minute > t.__minute ) {
			return false;
		}
	}

	if ( precision == PRECISION_MINUTE ) {
		// Equal so return false...
		return false;
	}

	// means that minutes match - so check second

	if( __second < t.__second ) {
		return true;
	}
	else {
        if( __second > t.__second ) {
			return false;
		}
	}

	if ( precision == PRECISION_SECOND ) {
		// Equal so return false...
		return false;
	}

	// means that seconds match - so check hundredths of seconds

	if( __hsecond < t.__hsecond ) {
		return true;
	}
	else {
        if( __hsecond > t.__hsecond ) {
			return false;
		}
	}

	// everything must be equal so not less than

	return false;
}

/**
Determine if the DateTime is <= another.  Time zone is not
considered in the comparison (no time zone shift is made).
@return true if the DateTime instance is less than or equal to given DateTime.
@param d DateTime to compare.
*/
public boolean lessThanOrEqualTo ( DateTime d )
{	return !greaterThan(d);
}

/**
Determine if the DateTime is <= another.  Time zone is not
considered in the comparison (no time zone shift is made).
@return true if the DateTime instance is less than or equal to given DateTime.
@param d DateTime to compare.
@param precision The precision used when comparing the DateTime instances.
*/
public boolean lessThanOrEqualTo ( DateTime d, int precision )
{	if ( !greaterThan(d,precision) ) {
		return true;
	}
	else {
        return false;
	}
}

/**
Parse a string and initialize a DateTime.  By default time zone will be set
but the PRECISION_TIME_ZONE flag will be set to false.  If only a time format
is detected, then the TIME_ONLY flag will be set in the returned instance.
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
@param date_string Any of the formats supported by parse(String,int).
@param datetime_props Named DateTime instances that are to be recognized when
parsing the string.  For example, an application may internally have a parameter
called InputStart, which is referenced in the string.  If parsing only for
syntax (where the value of the parsed result is not important), specify any instance of DateTime.<p>
The String value for the named DateTime is parsed, even though contents may be available. <p>
The named date/time instances <u>cannot</u> contain "+" or "-" characters.
@exception IllegalArgumentException If the string is not understood due to a bad date/time,
interval string or a missing named date/time.
@see #toString
*/
public static DateTime parse ( String date_string, PropList datetime_props )
{
	if (date_string == null) {
		Message.printWarning(3, "DateTime.parse", "Cannot get DateTime from null string.");
		throw new IllegalArgumentException("Null DateTime string to parse");
	} 

	String str = date_string.trim();

	if (str.length() == 0) {
		Message.printWarning(3, "DateTime.parse", "Cannot get DateTime from empty string.");
		throw new IllegalArgumentException("Empty DateTime string to parse");
	}		

	if (Character.isDigit(date_string.charAt(0))) {
		// If the first character is a number then assume that this is a
		// DateTime string that should be parsed as normal.  There is 
		// no support for parsing things like:
		//    "2005-10-12 10:13 + 15Minute"
		return DateTime.parse(date_string);
	}
	
	String[] tokens = new String[3];
	// tokens[0] = the date represented by the first part of the string to
	//	be parsed (e.g., CurrentToMinute, "DateProperty", etc)
	// tokens[1] = the operator ("+" or "-").  Null if no operators.
	// tokens[2] = the interval to adjust be (e.g., "15Minute").  Null if no operator.
	
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

	// Try to find a match in the PropList and substitute with 
	// the value stored there.  Values in the PropList can override any 
	// of the hard-coded values (e.g., "CurrentToMinute").
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
	
		// Try to parse as one of the hard-coded values ( CurrentToMinute, etc).
		if (tokens[0].equalsIgnoreCase("CurrentToMinute")) {
			token0DateTime.setPrecision(DateTime.PRECISION_MINUTE);
		}
		else if (tokens[0].equalsIgnoreCase("CurrentToHour")) {
			token0DateTime.setPrecision(DateTime.PRECISION_HOUR);
		}
		else if (tokens[0].equalsIgnoreCase("CurrentToDay")) {
			token0DateTime.setPrecision(DateTime.PRECISION_DAY);
		}
		else if (tokens[0].equalsIgnoreCase("CurrentToMonth")) {
			token0DateTime.setPrecision(DateTime.PRECISION_MONTH);
		}
		else if (tokens[0].equalsIgnoreCase("CurrentToYear")) {
			token0DateTime.setPrecision(DateTime.PRECISION_YEAR);
		}
		else {	
			Message.printWarning(3, "DateTime.parse","Could not find the named date/time'" + tokens[0] + "'");
			throw new IllegalArgumentException("Named date/time'" + tokens[0] + "' not found.  Cannot parse.");
		}
	}

	if (tokens[1] == null) {
		// no operator, so the DateTime can be returned as is.
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
		// Subtract an interval
		token0DateTime.addInterval(ti.getBase(), -1 * ti.getMultiplier());
	}
	else {
		// Add an interval.  Already checked above for "+" or "-" 
		// so don't need to check for anything else here.
		token0DateTime.addInterval(ti.getBase(), ti.getMultiplier());
	}

	return token0DateTime;
}

/**
Parse a string and initialize a DateTime.  By default time zone will be set
but the PRECISION_TIME_ZONE flag will be set to false.  If only a time format
is detected, then the TIME_ONLY flag will be set in the returned instance.
This routine is the inverse of toString().
@return A DateTime corresponding to the date.
@param date_string Any of the formats supported by parse(String,int).
@exception IllegalArgumentException If the string is not understood.
@see #toString
*/
public static DateTime parse ( String date_string )
{	int	length = 0;
	char c;	// Use to optimize code below

	// First check to make sure we have something...
	if( date_string == null ) {
		Message.printWarning( 3, "DateTime.parse", "Cannot get DateTime from null string." );
		throw new IllegalArgumentException ( "Null DateTime string to parse" );
	} 
	length = date_string.length();
	if( length == 0 ) {
		Message.printWarning( 3, "DateTime.parse", "Cannot get DateTime from zero-length string." );
		throw new IllegalArgumentException ( "Empty DateTime string to parse" );
	}

	// This if-elseif structure is used to determine the format of the date represented by date_string.
	if( length == 4 ){
		//
		// the date is YYYY 
		//
		return( parse( date_string, FORMAT_YYYY, 0 ) );
	}
	else if( length == 5 ){
		//
		// the date is MM/DD or MM-DD or HH:mm
		// Don't allow MM/YY!!!
		//
		c = date_string.charAt ( 2 );
		if ( c == ':' ) {
			return( parse( date_string, FORMAT_HH_mm, 0 ) );
		}
		else if ( (c == '/') || (c == '-') ) {
			// The following will work for both...
			return( parse( date_string, FORMAT_MM_SLASH_DD, 0 ) );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + date_string + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + date_string + "\"" );
		}
	}
	else if( length == 6 ){
		//
		// the date is M/YYYY
		//
		if ( date_string.charAt(1) == '/') {
			return(parse(" "+ date_string, FORMAT_MM_SLASH_YYYY,0));
		}
		else {	Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + date_string + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + date_string + "\"" );
		}
	}
	else if( length == 7 ){
		//
		// the date is YYYY-MM or MM/YYYY
		//
		if( date_string.charAt(2) == '/' ) {
			return( parse( date_string, FORMAT_MM_SLASH_YYYY, 0 ) );
		}
		else {
            return( parse( date_string, FORMAT_YYYY_MM, 0 ) );
		}
	}
	else if ( length == 8 ) {
		if ( (date_string.charAt(2) == '/') && (date_string.charAt(5) == '/') ) {
			//
			// the date is MM/DD/YY
			//
			return( parse(date_string, FORMAT_MM_SLASH_DD_SLASH_YY, 0 ) );
		}
		else if((date_string.charAt(1) == '/') && (date_string.charAt(3) == '/') ) {
			//
			// the date is M/D/YYYY
			//
			return( parse(date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY, 8 ) );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + date_string + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + date_string + "\"" );
		}
	}
	else if ( length == 9 ) {
		if ( (date_string.charAt(2) == '/') && (date_string.charAt(4) == '/') ) {
			//
			// the date is MM/D/YYYY
			//
			return( parse(date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY, 9 ) );
		}
		else if((date_string.charAt(1) == '/') && (date_string.charAt(4) == '/') ) {
			//
			// the date is M/DD/YYYY
			//
			return( parse(date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY, -9 ) );
		}
		else {
            Message.printWarning( 2, "DateTime.parse", "Cannot get DateTime from \"" + date_string + "\"" );
			throw new IllegalArgumentException ( "Invalid DateTime string \"" + date_string + "\"" );
		}
	}
	else if( length == 10 ){
		//
		// the date is MM/DD/YYYY or YYYY-MM-DD 
		//
		if( date_string.charAt(2) == '/' ) {
			return( parse( date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY, 0 ) );
		}
		else {
            return( parse( date_string, FORMAT_YYYY_MM_DD, 0 ) );
		}
	}
        //
        // Length 11 would presumably by YYYYMMDDHmm, but this is not currently allowed.
        //
	else if( length == 12 ){
		//
		// the date is YYYYMMDDHHmm
		//
		return( parse( date_string, FORMAT_YYYYMMDDHHmm, 0 ) );
	}
	else if( length == 13 ){
		//
		// the date is YYYY-MM-DD HH
		// or          MM/DD/YYYY HH
		// or          MM-DD-YYYY HH
		//
		if ( date_string.charAt(2) == '/' ) {
			return( parse( date_string,	FORMAT_MM_SLASH_DD_SLASH_YYYY_HH, 0 ) );
		}
		else if ( date_string.charAt(2) == '-' ) {
			return( parse( date_string, FORMAT_MM_DD_YYYY_HH, 0 ) );
		}
		else {
            return( parse( date_string, FORMAT_YYYY_MM_DD_HH, 0 ) );
		}
	}
	else if ( (length > 14) && Character.isLetter(date_string.charAt(14))){
		//
		// the date is YYYY-MM-DD HH Z...
		//
		return parse( date_string, FORMAT_YYYY_MM_DD_HH_ZZZ, 0 );
	}
	else if( length == 15 ){
		//
		// the date is YYYY-MM-DD HHmm
		//
		return( parse( date_string, FORMAT_YYYY_MM_DD_HHmm, 0 ) );
	}
	else if( length == 16 ){
		//
		// the date is YYYY-MM-DD HH:mm or MM/DD/YYYY HH:mm
		//
		if( date_string.charAt(2) == '/' ) {
			return( parse( date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm, 0 ) );
		}
		else {
            return( parse( date_string, FORMAT_YYYY_MM_DD_HH_mm,0));
		}
	}
	else if ( (length > 17) && Character.isLetter(date_string.charAt(17))){
		//
		// the date is YYYY-MM-DD HH:MM Z...
		//
		return parse( date_string, FORMAT_YYYY_MM_DD_HH_mm_ZZZ, 0 );
	}
	else if( length == 19 ){
		//
		// the date is YYYY-MM-DD HH:mm:SS or MM/DD/YYYY HH:mm:SS
		//
        if( date_string.charAt(2) == '/' ) {
			return( parse( date_string, FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS, 0 ) );
		}
		else {
            return( parse( date_string, FORMAT_YYYY_MM_DD_HH_mm_SS, 0 ) );
        }
	}
	else if( length == 22 ){
		//
		// the date is YYYY-MM-DD HH:mm:SS:hh
		//
		return( parse( date_string, FORMAT_YYYY_MM_DD_HH_mm_SS_hh, 0 ));
	}
	else if( length >= 23 && date_string.charAt(19) == ' ' ){
		//
		// the date is YYYY-MM-DD HH:mm:SS ZZZ...
		//
		return( parse( date_string, FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ, 0));
	}
    else if( length > 23 && date_string.charAt(19) == ':' && date_string.charAt(22) == ' ' ){
        //
        // the date is YYYY-MM-DD HH:mm:SS:hh ZZZ...
        //
        return( parse( date_string, FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ, 0 ));
    }
	else {
	    // Unknown length so throw an exception...
		throw new IllegalArgumentException ( "Date/time string \"" + date_string +	"\" format is not recognized." );
	}
}

/**
Parse a string and initialize a DateTime.  The calling code must specify the
proper format for parsing.  This routine therefore has limited use but is
relatively fast.  The precision for the date is set according to the format (the
precision is set to the smallest time interval used in the format).
This routine is the inverse of toString(int format).
@return A DateTime corresponding to the date.
@param date_string A string representation of a date/time.
@param format Date format (see FORMAT_*).
@exception Exception If there is an error parsing the date string.
@see #toString
*/
public static DateTime parse ( String date_string, int format )
throws Exception
{	// Call the overloaded method with no special flag...
	return parse ( date_string, format, 0 );
}

/**
Parse a string and initialize a DateTime.  The calling code must specify the
proper format for parsing.  This routine therefore has limited use but is
relatively fast.  The precision for the date is set according to the format (the
precision is set to the smallest time interval used in the format).
This routine is the inverse of toString(int format).
@return A DateTime corresponding to the date.
@param date_string A string representation of a date/time.
@param format Date format (see FORMAT_*).
@exception IllegalArgumentException If there is an error parsing the date string.
@param flag A flag to use internally.  If > 0, this is used by some
internal code to indicate variations in formats.  For example, MM/DD/YYYY,
MM/D/YYYY, M/DD/YYYY, M/D/YYYY are all variations on the same format.
@see #toString
*/
private static DateTime parse ( String date_string, int format, int flag )
{	int dl = 50;
	boolean is_year = false,	// Use to improve performance
			is_month = false,	// of checks at end of the
			is_day = false,		// method - use booleans rather
			is_hour = false,	// than doing repeated bit mask
			is_minute = false;	// checks
	DateTime date = null;
	String routine = "DateTime.parse";
	List v = null;

	// Note that if the fixedRead routine has problems, it will just return
	// zeros for the integers.  This allows defaults for the smaller date/time fields...

	if ( Message.isDebugOn ) {
		Message.printDebug(dl,routine, "Trying to parse string \"" + date_string + "\" using format " + format );
	}

	if ( format == FORMAT_DD_SLASH_MM_SLASH_YYYY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		// Various flavors of the format based on whether one or two
		// digits are used for the month and day...
		if ( flag == 0 ) {
			v = StringUtil.fixedRead ( date_string, "i2x1i2x1i4" );
		}
		else if ( flag == 8 ) {
			v = StringUtil.fixedRead ( date_string, "i1x1i1x1i4" );
		}
		else if ( flag == 9 ) {
			v = StringUtil.fixedRead ( date_string, "i2x1i1x1i4" );
		}
		else if ( flag == -9 ) {
			v = StringUtil.fixedRead ( date_string, "i1x1i2x1i4" );
		}
		date.__day = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE | TIME_ONLY );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i2x1i2" );
		date.__hour = ((Integer)v.get(0)).intValue();
		date.__minute = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_HHmm ) {
		date = new DateTime ( PRECISION_MINUTE | TIME_ONLY );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i2i2" );
		date.__hour = ((Integer)v.get(0)).intValue();
		date.__minute = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_MM ) {
		date = new DateTime ( PRECISION_MONTH );
		is_month = true;
		v = StringUtil.fixedRead ( date_string, "i2" );
		date.__month = ((Integer)v.get(0)).intValue();
	}
	else if ( (format == FORMAT_MM_DD) || (format == FORMAT_MM_SLASH_DD) ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( date_string, "i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		// Various flavors of the format based on whether one or two
		// digits are used for the month and day...
		if ( flag == 0 ) {
			v = StringUtil.fixedRead ( date_string, "i2x1i2x1i4" );
		}
		else if ( flag == 8 ) {
			v = StringUtil.fixedRead ( date_string, "i1x1i1x1i4" );
		}
		else if ( flag == 9 ) {
			v = StringUtil.fixedRead ( date_string, "i2x1i1x1i4" );
		}
		else if ( flag == -9 ) {
			v = StringUtil.fixedRead ( date_string, "i1x1i2x1i4" );
		}
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YY ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( date_string, "i2x1i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
	}
	else if ( (format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH) || (format == FORMAT_MM_DD_YYYY_HH) ) {
		date = new DateTime (PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( date_string, "i2x1i2x1i4x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
	}
	else if ( format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		if ( date_string.length() < 16 ) {
		    // The date string is not padded with zeros.  Parse the string
		    // into its parts and then reform to a zero-padded string.  Use primitive
		    // formatting to increase performance.
		    String [] sarray = date_string.split("[/ :]" );
		    String monthPad= "", dayPad = "", hourPad = "", minutePad = "";
		    if ( (sarray != null) && (sarray.length > 4) ) {
		        // Assume that have all the needed parts
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
                date_string = monthPad + sarray[0] + "/" + dayPad + sarray[1] + "/" +
                    sarray[2] + " " + hourPad + sarray[3] + ":" + minutePad + sarray[4];
		    }
		}
		v = StringUtil.fixedRead ( date_string, "i2x1i2x1i4x1i2x1i2" );
		date.__month = ((Integer)v.get(0)).intValue();
		date.__day = ((Integer)v.get(1)).intValue();
		date.__year = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
    else if (format == FORMAT_MM_SLASH_DD_SLASH_YYYY_HH_mm_SS) {
		date = new DateTime (PRECISION_SECOND );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i2x1i2x1i4x1i2x1i2x1i2" );
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
		if ( date_string.length() == 6 ) {
			v = StringUtil.fixedRead ( date_string, "i1x1i4" );
		} 
		else {	// Expect a length of 7...
			v = StringUtil.fixedRead ( date_string, "i2x1i4" );
		}
		date.__month = ((Integer)v.get(0)).intValue();
		date.__year = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_YYYY ) {
		date = new DateTime ( PRECISION_YEAR );
		is_year = true;
		v = StringUtil.fixedRead ( date_string, "i4" );
		date.__year = ((Integer)v.get(0)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM ) {
		date = new DateTime ( PRECISION_MONTH );
		is_month = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD ) {
		date = new DateTime ( PRECISION_DAY );
		is_day = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH ) {
		date = new DateTime (PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_ZZZ ) {
		date = new DateTime ( PRECISION_HOUR );
		is_hour = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2x1s3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.setTimeZone ( ((String)v.get(4)).trim() );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYYMMDDHHmm ) {
		date = new DateTime (PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i4i2i2i2i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HHmm ) {
		date = new DateTime ( PRECISION_MINUTE );
		is_minute = true;
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS ) {
		date = new DateTime ( PRECISION_SECOND );
		v = StringUtil.fixedRead ( date_string,"i4x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh ) {
		date = new DateTime (PRECISION_HSECOND );
		v = StringUtil.fixedRead ( date_string,"i4x1i2x1i2x1i2x1i2x1i2x1i2" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__hsecond = ((Integer)v.get(6)).intValue();
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_ZZZ ) {
		date = new DateTime ( PRECISION_HOUR );
		v = StringUtil.fixedRead ( date_string,
		"i4x1i2x1i2x1i2x1s3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.setTimeZone ( (String)v.get(4) );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_ZZZ ) {
		date = new DateTime ( PRECISION_MINUTE );
		v = StringUtil.fixedRead ( date_string,"i4x1i2x1i2x1i2x1i2x1s3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.setTimeZone ( (String)v.get(5) );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ ) {
		date = new DateTime ( PRECISION_SECOND );
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2x1i2x1i2x1s3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.setTimeZone ( (String)v.get(6) );
	}
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ ) {
		date = new DateTime ( PRECISION_HSECOND );
		v = StringUtil.fixedRead ( date_string, "i4x1i2x1i2x1i2x1i2x1i2x1i2x1s3" );
		date.__year = ((Integer)v.get(0)).intValue();
		date.__month = ((Integer)v.get(1)).intValue();
		date.__day = ((Integer)v.get(2)).intValue();
		date.__hour = ((Integer)v.get(3)).intValue();
		date.__minute = ((Integer)v.get(4)).intValue();
		date.__second = ((Integer)v.get(5)).intValue();
		date.__hsecond = ((Integer)v.get(6)).intValue();
		date.setTimeZone ( (String)v.get(7) );
	}
	else {
        v = null;
		routine = null;
		throw new IllegalArgumentException ( "Date format " + format +	" is not recognized." );
	}
	// Cleanup...
	v = null;
	routine = null;
	// Check for hour 24...
	if ( date.__hour == 24 ) {
		// Assume the date that was parsed uses a 1-24 hour system. Change to hour 0 of the next day...
		date.__hour = 0;
		date.addDay(1);
	}
	// Verify that the date components are valid.  If not, throw an
	// exception.  This degrades performance some but not much since all checks are integer based.
	// Limit year to a reasonable value...
	if ( (date.__year < -1000) || (date.__year > 10000) ) {
		throw new IllegalArgumentException ( "Invalid year " + date.__year + " in \"" + date_string + "\"" );
	}
	if ( is_year ) {
		date.reset();
		return date;
	}
	if ( (date.__month < 1) || (date.__month > 12) ) {
		throw new IllegalArgumentException ( "Invalid month " + date.__month + " in \"" + date_string + "\"" );
	}
	if ( is_month ) {
		date.reset();
		return date;
	}
	// Split out checks to improve performance...
	if ( date.__month == 2 ) {
		if ( TimeUtil.isLeapYear ( date.__year ) ) {
			if ( (date.__day < 1) || (date.__day > 29) ) {
				throw new IllegalArgumentException ( "Invalid day " + date.__day +	" in \"" + date_string + "\"" );
			}
		}
		else {
		    if ( (date.__day < 1) || (date.__day > 28) ) {
				throw new IllegalArgumentException ( "Invalid day " + date.__day + " in \"" + date_string + "\"" );
			}
		}
	}
	else {
	    // Not a leap year...
		if ( (date.__day < 1) || (date.__day > TimeUtil.MONTH_DAYS[date.__month - 1]) ) {
			throw new IllegalArgumentException ( "Invalid day " + date.__day + " in \"" + date_string + "\"" );
		}
	}
	if ( is_day ) {
		date.reset();
		return date;
	}
	if ( (date.__hour < 0) || (date.__hour > 23) ) {
		throw new IllegalArgumentException ( "Invalid hour " + date.__hour + " in \"" + date_string + "\"" );
	}
	if ( is_hour ) {
		date.reset();
		return date;
	}
	if ( (date.__minute < 0) || (date.__minute > 59) ) {
		throw new IllegalArgumentException ( "Invalid minute " + date.__minute + " in \"" + date_string + "\"" );
	}
	if ( is_minute ) {
		date.reset();
		return date;
	}
	date.reset();
	return date;
}

/**
Parse an ISO standard string with no year.  Recognized formats are therefore 
"MM", "MM-DD", "MM-DD HH", and "MMDD HH:mm".
*/

/**
Reset the derived data (year day, absolute month, and leap year).  This is
normally called by other DateTime functions but can be called externally if
data are set manually.
*/
public void reset ()
{	// Always reset the absolute month since it is cheap...
	setAbsoluteMonth();
	if ( (__behavior_flag & DATE_FAST) != 0 ) {
		// Want to run fast so don't check...
		return;
	}
	setYearDay();
    __isleap = TimeUtil.isLeapYear( __year );
}

/**
Round the time to an even interval.  This is useful when setting the period
for a time series from irregular end dates.  If a matching even interval is
specified, then no change will occur.  Any reasonable combination of base and
multiplier can be specified, resulting in intervals that divide evenly into the
next coarsest time interval (e.g., use 10 min, not 13 min).  Otherwise, results
may be unexpected.  Time components smaller than the base are set to appropriate
zero values (e.g., rounding minutes results in seconds being set to zero).
The irregular interval results in no change to the date.
@param direction Specify 1 to round by incrementing the date.  Specify -1 to
round by decrementing the date.  This flag may be modified in the future to have additional meaning.
@param interval_base See TimeInterval.
@param interval_mult Multiplier for the interval base.
*/
public void round ( int direction, int interval_base, int interval_mult )
{	if( interval_base == TimeInterval.SECOND ) {
		__hsecond = 0;
	}
	else if( interval_base == TimeInterval.MINUTE ) {
		__second = 0;
		__hsecond = 0;
		if ( direction > 0 ) {
			// Rounding up (if the minute is already 0 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __minute != 0 ) {
					// Want an even hour and minute is not zero.  Increase the hour.  Do so by
					// incrementing the minutes...
					addMinute ( 60 - __minute );
				}
				// Else.  Do nothing since minute is already zero.
			}
			else {
			    // Want to increment to an even interval...
				if ( (__minute%interval_mult) != 0 ) {
					// Not exactly on interval time
					addMinute ( interval_mult -	__minute%interval_mult );
				}
			}
		}
		else {
            // Rounding down (if the _minute is already 0 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __minute != 0 ) {
					// Want an even hour and minute is not zero.  Decrease the hour.  Do so by
					// decrementing the minutes...
					addMinute ( -1*__minute );
				}
				// Else.  Do nothing since minute is already zero.
			}
			else {
			    // Want to decrement to an even interval...
				if ( (__minute%interval_mult) != 0 ) {
					// Not exactly on interval time
					addMinute ( -1*__minute%interval_mult );
				}
			}
		}
	}
	else if ( interval_base == TimeInterval.HOUR ) {
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		if ( direction > 0 ) {
			// Rounding up (if the hour is already 0 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Increase the day.  Do so by
					// incrementing the hours...
					addHour ( 24 - __hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to increment to an even interval...
				addHour ( interval_mult - __hour%interval_mult );
			}
		}
		else {
            // Rounding down (if the _hour is already 0 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Decrease the day.  Do so by
					// decrementing the hour...
					addHour ( -1*__hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to decrement to an even interval...
				addHour ( -1*__hour%interval_mult );
			}
		}
    }
    else if ( interval_base == TimeInterval.DAY ) {
		__hour = 0;
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		if ( direction > 0 ) {
			// Rounding up (if the _day is already 1 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Increase the day.  Do so by
					// incrementing the hours...
					addHour ( 24 - __hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to increment to an even interval...
				addHour ( interval_mult - __hour%interval_mult );
			}
		}
		else {
            // Rounding down (if the _hour is already 0 then don't need to do anything)...
			if ( interval_mult == 0 ) {
				if ( __hour != 0 ) {
					// Want an even day and hour is not zero.  Decrease the day.  Do so by
					// decrementing the hour...
					addHour ( -1*__hour );
				}
				// Else.  Do nothing since hour is already zero.
			}
			else {
                // Want to decrement to an even interval...
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
		__hsecond = 0;
    }
    else if ( interval_base == TimeInterval.YEAR ) {
		__month = 1;
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__hsecond = 0;
    }
    else if ( interval_base == TimeInterval.IRREGULAR ) {
		// Do nothing to the date.
	}
    else {  // Unsupported interval...
		Message.printWarning ( 2, "DateTime.round",	"Interval base " + interval_base + " is unsupported.");
    }
	reset();
}

/**
Set the absolute month from the month and year.  This is called internally.
*/
private void setAbsoluteMonth()
{	__abs_month = (__year * 12) + __month;
}

/**
Set value of time series using another as input (equivalent to C++ = operator).
A new instance is not allocated.
@param t A DateTime to copy.
*/
public void setDate ( DateTime t )
{	if ( t == null ) {
		return;
	}
	__hsecond = t.__hsecond;
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
A new instance is not allocated.  This is useful when iterating through
database records that use Date.  Only fields appropriate for the DateTime precision are set.
@param d A Date to assign from.
*/
public void setDate ( Date d )
{	if ( d == null ) {
		return;
	}
	// Returns the number of years since 1900!
	setYear ( d.getYear() + 1900 );
	if ( __precision == PRECISION_YEAR ) {
		return;
	}
	// Returned month is 0 to 11!
	setMonth ( d.getMonth() + 1 );
	if ( __precision == PRECISION_MONTH ) {
		return;
	}
	// Returned day is 1 to 31
	setDay ( d.getDate() );
	if ( __precision == PRECISION_DAY ) {
		return;
	}
	// Returned hours are 0 to 23
	setHour ( d.getHours() );
	if ( __precision == PRECISION_HOUR ) {
		return;
	}
	// Returned hours are 0 to 59 
	setMinute ( d.getMinutes() );
	if ( __precision == PRECISION_MINUTE ) {
		return;
	}
	setSecond ( d.getSeconds() );
	reset();
}

/**
Set the day.
@param d Day.
*/
public void setDay ( int d )
{	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
		if(	(d > TimeUtil.numDaysInMonth( __month, __year )) || (d < 1) ) {
            String message = "Trying to set invalid day (" + d + ") in DateTime for year " + __year;
            Message.printWarning( 10, "DateTime.setDay", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __day = d;
	setYearDay();
	// This has the flaw of not changing the flag when the value is set to 1!
	if ( __day != 1 ) {
		__iszero = false;
	}
}

/**
Set the hour.
@param h Hour.
*/
public void setHour( int h )
{	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
		if( (h > 23) || (h < 0) ) {
			String message = "Trying to set invalid hour (" + h + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setHour", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __hour = h;
	// This has the flaw of not changing the flag when the value is set to 0!
	if ( __hour != 0 ) {
		__iszero = false;
	}
}

/**
Set the hundredths of seconds.
@param hs Hundredths of seconds.
*/
public void setHSecond( int hs)
{	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( hs > 99 || hs < 0 ) {
            String message = "Trying to set invalid hsecond (" + hs + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setHSecond", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __hsecond = hs;
	// This has the flaw of not changing the flag when the value is set to 0!
	if ( hs != 0 ) {
		__iszero = false;
	}
}

/**
Set the minute.
@param m Minute.
*/
public void setMinute( int m)
{	
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( m > 59 || m < 0 ) {
            String message = "Trying to set invalid minute (" + m + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setMinute", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __minute = m;
	// This has the flaw of not changing the flag when the value is set to 0!
	if ( m != 0 ) {
		__iszero = false;
	}
}

/**
Set the month.
@param m Month.
*/
public void setMonth ( int m)
{
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( m > 12 || m < 1 ) {
            String message = "Trying to set invalid month (" + m + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setMonth", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __month = m;
	setYearDay();
	setAbsoluteMonth();
	// This has the flaw of not changing the flag when the value is set to 1!
	if ( m != 1 ) {
		__iszero = false;
	}
}

/**
Set the precision using a bit mask.  The precision can be used to optimize code
(avoid performing unnecessary checks) and set more intelligent dates.  The
overloaded version is called with a "cumulative" value of true.
@param behavior_flag Full behavior mask containing precision bit (see
PRECISION_*).  The precision is set when the first valid precision bit
is found (starting with PRECISION_YEAR).
@return this DateTime instance, which allows chained calls.
*/
public DateTime setPrecision ( int behavior_flag )
{	return setPrecision ( behavior_flag, true );
}

/**
Set the precision using a bit mask.  The precision can be used to optimize code
(avoid performing unnecessary checks) and set more intelligent dates.  This
call automatically truncates unused date fields (sets them to initial values
as appropriate).  Subsequent calls to getPrecision(), timeOnly(), and
useTimeZone() will return the separate field values (don't need to handle as a bit mask upon retrieval).
@param behavior_flag Full behavior mask containing precision bit (see
PRECISION_*).  The precision is set when the first valid precision bit
is found (starting with PRECISION_YEAR).
@param cumulative If true, the bit-mask values will be set cumulatively.  If
false, the values will be reset to defaults and only new values will be set.
@return this DateTime instance, which allows chained calls.
*/
public DateTime setPrecision ( int behavior_flag, boolean cumulative )
{	// The behavior flag contains the precision (small bits) and higher
	// bit masks.  The lower precision values are not unique bit masks.
	// Therefore, get the actual precision value by cutting off the higher
	// values > 100 (the maximum precision value is 70).
	//_precision = behavior_flag - ((behavior_flag/100)*100);
	// Need to remove the effects of the higher order masks...
	int behavior_flag_no_precision = behavior_flag;
	int precision = behavior_flag;
	if ( (behavior_flag & DATE_STRICT) != 0 ) {
		behavior_flag_no_precision |= DATE_STRICT;
		precision ^= DATE_STRICT;
	}
	if ( (behavior_flag & DATE_FAST) != 0 ) {
		behavior_flag_no_precision |= DATE_FAST;
		precision ^= DATE_FAST;
	}
	if ( (behavior_flag & DATE_ZERO) != 0 ) {
		behavior_flag_no_precision |= DATE_ZERO;
		precision ^= DATE_ZERO;
	}
	if ( (behavior_flag & DATE_CURRENT) != 0 ) {
		behavior_flag_no_precision |= DATE_CURRENT;
		precision ^= DATE_CURRENT;
	}
	if ( (behavior_flag & TIME_ONLY) != 0 ) {
		behavior_flag_no_precision |= TIME_ONLY;
		precision ^= TIME_ONLY;
	}
	if ( (behavior_flag & PRECISION_TIME_ZONE) != 0 ) {
		behavior_flag_no_precision |= PRECISION_TIME_ZONE;
		precision ^= PRECISION_TIME_ZONE;
	}
	// Now the precision should be what is left...
	if ( precision == PRECISION_YEAR ) {
		__month = 1;
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_MONTH ) {
		__day = 1;
		__hour = 0;
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_DAY ) {
		__hour = 0;
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_HOUR ) {
		__minute = 0;
		__second = 0;
		__hsecond = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_MINUTE ) {
		__second = 0;
		__hsecond = 0;
		__precision = precision;
	}
	else if ( precision == PRECISION_SECOND ) {
		__hsecond = 0;
		__precision = precision;
	}
	// Else do not set _precision - assume that it was set previously (e.g., in a copy constructor).

	// Time zone is separate and always gets set...

	if ( (behavior_flag & PRECISION_TIME_ZONE) != 0 ) {
		__use_time_zone = true;
	}
	else if ( !cumulative ) {
		__use_time_zone = false;
	}

	// Time only is separate and always gets set...

	if ( (behavior_flag & TIME_ONLY) != 0 ) {
		__time_only = true;
	}
	else if ( !cumulative ) {
		__time_only = false;
	}
	return this;
}

/**
Set the second.
@param s Second.
*/
public void setSecond( int s )
{
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        if( s > 59 || s < 0 ) {
            String message = "Trying to set invalid second (" + s + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setSecond", message );
            throw new IllegalArgumentException ( message );
        }
	}
    __second = s;
	// This has the flaw of not changing the flag when the value is set to 0!
	if ( s != 0 ) {
		__iszero = false;
	}
}

/**
Set the string time zone.  No check is made to verify that it is a valid time zone abbreviation.
@param zone Time zone abbreviation.  If non-null and non-blank, the
DateTime precision is automatically set so that PRECISION_TIME_ZONE is on.
If null or blank, PRECISION_TIME_ZONE is off.
@return the same DateTime instance, which allows chained calls
*/
public DateTime setTimeZone( String zone ) 
{	if ( (zone == null) || (zone.length() == 0) ) {
		__tz = "";
		__use_time_zone = false;
	}
	else {
        __use_time_zone = true;
		__tz = zone;
	}
    return this;
}

/**
Set the date to the current date/time.
The default precision is PRECISION_SECOND and the time zone is set.  This
method is usually only called internally to initialize dates.  If called
externally, the precision should be set separately.
*/
public void setToCurrent () 
{	// First get the current time (construct a new date because this code
	// is not executed that much).  If we call this a lot, inline the
	// code rather than constructing...

	Date d = new Date ();
	DateTime now = new DateTime ( d );
	d = null;

	// Now set...

	__hsecond = now.__hsecond;
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
	__precision = PRECISION_SECOND;
	__use_time_zone = false;
	__time_only = false;

	// Set the time zone.  Use TimeUtil directly to increase performance...

	if ( TimeUtil._time_zone_lookup_method == TimeUtil.LOOKUP_TIME_ZONE_ONCE ) {
		if ( !TimeUtil._local_time_zone_retrieved ) {
			// Need to initialize...
			shiftTimeZone ( TimeUtil.getLocalTimeZoneAbbr() );
		}
		else {
            // Use the existing data...
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
public void setToJulianDay ( int y, int julday )
{	__year = y;
    // Need to set here because leap year is tested in the following loop
    __isleap = TimeUtil.isLeapYear( __year );
	// Loop through the static Julian day data...
	int offset = 0;
	for ( int i = 1; i < 12; i++ ) {
		if ( i > 1 ) {
			// Since counts are for days prior to month, this only kicks in after February...
			if ( __isleap ) {
				offset = 1;
			}
		}
		if ( julday <= (TimeUtil.MONTH_YEARDAYS[i] + offset) ) {
			// Month is previous (but use i since zero-index)...
			__month = i;
			if ( i > 2 ) {
				// Need to subtract the offset to get the right day...
				__day = julday - (TimeUtil.MONTH_YEARDAYS[i - 1] + offset);
			}
			else {
                // Don't consider the offset...
				__day = julday - TimeUtil.MONTH_YEARDAYS[i - 1];
			}
			reset();
			return;
		}
	}
	// If here then the month is December...
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
The default precision is PRECISION_SECOND and the time zone is not used.  This
method is usually only called internally to initialize dates.  If called
externally, the precision should be set separately.
*/
public void setToZero ( ) 
{	__hsecond = 0;
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
	__precision = PRECISION_SECOND;
	__use_time_zone = false;
	__time_only = false;

	// Indicate that the date/time has been zero to zeros...

	__iszero = true;
}

/**
Set the year.
*/
public void setYear( int y )
{
	if( (__behavior_flag & DATE_STRICT) != 0 ){
        /* TODO SAM 2007-12-20 Evaluate whether negative year should be allowed.
        if( y < 0 ) {
            String message = "Trying to set invalid year (" + y + ") in DateTime.";
            Message.printWarning( 2, "DateTime.setYear", message );
            throw new IllegalArgumentException ( message );
        }
        */
	}
    __year = y;
	setYearDay();
	setAbsoluteMonth();
    __isleap = TimeUtil.isLeapYear( __year );
	if ( y != 0 ) {
		__iszero = false;
	}
}

/**
Set the year day from other data.
The information is set ONLY if the DATE_FAST bit is not set in the behavior mask.
*/
private void setYearDay()
{	if ( (__behavior_flag & DATE_FAST) != 0 ) {
		// Want to run fast so don't check...
		return;
	}

	int i;

   	// Calculate the year day...

   	__yearday = 0;

   	// Get the days from the previous months...

   	for( i = 1; i < __month; i++ ) {
       	__yearday += TimeUtil.numDaysInMonth( i, __year );
   	}

   	// Add the days from the current month...

   	__yearday += __day;
}

/**
Shift the data to the specified time zone, resulting in the hours and possibly minutes being changed.
@param zone Time zone to switch to.  This method shifts the hour/minutes and
then sets the time zone for the instance to the requested time zone.
@exception Exception if the time zone cannot be shifted (unknown time zone).
*/
public void shiftTimeZone ( String zone )
{	try {
        int offset = TZ.calculateOffsetMinutes ( __tz, zone, this );
		addMinute ( offset );
		setTimeZone ( zone );
	}
	catch ( Exception e ) {
		// Nothing for now - should not happen if system is set up correctly
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
Indicate whether the DateTime is only storing a time.  This will be the case if
the TIME_ONLY flag is in effect during construction.
@return true if only time fields are considered.
*/
public boolean timeOnly ()
{	return __time_only;
}

/**
Convert to a double, with the whole number being the year.  This is useful for
graphics.  The precision is checked and remainder fields are ignored.
If the instance is only storing time, then the whole number part of the value will be zero.
@return Date/time representation as a double.
*/
public double toDouble ( )
{	double dt = 0.0, d = 0;
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
	d += ((double)(__hsecond))/8640000; // 100*60*60*24
	return (dt + d/ydays);
}

/**
Return string representation of the date and time.
@return String representation of the date, using a format consistent with
the precision for the date (see PRECISION_* and TIME_ONLY).  In general, the
output formats are Y2K strings like YYYY-MM-DD or YYYY-MM-DD HH:mm.
*/
public String toString ()
{	// Arrange these in probable order of use...
	if ( __precision == PRECISION_MONTH ) {
		return toString ( FORMAT_YYYY_MM );
	}
	else if ( __precision == PRECISION_DAY ) {
		return toString ( FORMAT_YYYY_MM_DD );
	}
	else if ( __precision == PRECISION_HOUR ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			return toString ( FORMAT_YYYY_MM_DD_HH_ZZZ );
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH );
		}
	}
	else if ( __precision == PRECISION_YEAR ) {
		return toString ( FORMAT_YYYY );
	}
	else if ( __precision == PRECISION_MINUTE ) {
		if ( __time_only ) {
			return toString ( FORMAT_HH_mm );
		}
		else {
            if ( __use_time_zone && (__tz.length() > 0) ) {
				return toString ( FORMAT_YYYY_MM_DD_HH_mm_ZZZ );
			}
			else {
                return toString ( FORMAT_YYYY_MM_DD_HH_mm );
			}
		}
	}
	else if ( __precision == PRECISION_SECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_ZZZ );
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS );
		}
	}
	else if ( __precision == PRECISION_HSECOND ) {
		if ( __use_time_zone && (__tz.length() > 0) ) {
			return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ );
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm_SS_hh );
		}
	}
	else {
        // Assume that hours and minutes but NOT time zone are desired...
		if ( __use_time_zone && (__tz.length() > 0) ) {
			return toString ( FORMAT_YYYY_MM_DD_HH_mm_ZZZ );
		}
		else {
            return toString ( FORMAT_YYYY_MM_DD_HH_mm );
		}
	}
}

// Remember to update parse also!
/**
Convert to a string using the given format (see FORMAT_*).  This is not as
flexible as formatTimeString but is useful where date formats need to be
consistent.  Currently if a time zone is detected, it is set in the data but
the PRECISION_TIME_ZONE flag is not set to true.
@return A String representation of the date.
@param format The format to use for the string.
*/
public String toString ( int format )
{	if ( format == FORMAT_NONE ) {
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
		StringUtil.formatString(__second,"%02d") + ":" +
		StringUtil.formatString(__hsecond,"%02d");
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
	else if ( format == FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ ) {
		return
		StringUtil.formatString(__year,"%04d") + "-" +
		StringUtil.formatString(__month,"%02d") + "-" +
		StringUtil.formatString(__day,"%02d") + " " +
		StringUtil.formatString(__hour,"%02d") + ":" +
		StringUtil.formatString(__minute,"%02d") + ":" +
		StringUtil.formatString(__second,"%02d") + ":" +
		StringUtil.formatString(__hsecond,"%02d") + " " + __tz;
	}
	else {
	    return toString( FORMAT_YYYY_MM_DD_HH_mm_SS_hh_ZZZ );
	}
}

/**
Indicate whether the time zone should be used when processing the date.  The
time zone will be considered if the PRECISION_TIME_ZONE flag is in effect
during construction or if the setPrecision() method is called.
@return true if the time zone should be considered when processing the date.
*/
public boolean useTimeZone()
{	return __use_time_zone;
}

}