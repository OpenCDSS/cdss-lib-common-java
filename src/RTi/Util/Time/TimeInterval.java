// ----------------------------------------------------------------------------
// TimeInterval - time interval class
// ----------------------------------------------------------------------------
// History:
//
// 22 Sep 1997	Steven A. Malers, RTi	First version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 01 May 2001	SAM, RTi		Add toString(), compatible with C++.
//					Add equals().
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
//					Change set methods to have void return
//					type.
// 2001-12-13	SAM, RTi		Copy TSInterval to TimeInterval and
//					make changes to make the class more
//					generic.  parseInterval() now throws an
//					exception if unable to parse.
// 2001-04-19	SAM, RTi		Add constructor to take integer base and
//					multiplier.
// 2002-05-30	SAM, RTi		Add getMultiplierString() to better
//					support exact lookups of interval parts
//					(e.g., for database queries that require
//					parts).
// 2003-05-30	SAM, RTi		Add multipliersForIntervalBase()
//					to return reasonable multipliers for a
//					base string.  Add support for seconds
//					in parseInterval().
// 2003-10-27	SAM, RTi		Add UNKNOWN for time interval. 
// 2005-02-16	SAM, RTi		Add getTimeIntervalChoices() to
//					facilitate use in interfaces.
// 2005-03-03	SAM, RTi		Add lessThan(), lessThanOrEquivalent(),
//					greaterThan(),greaterThanOrEquivalent(),
//					equivalent().  REVISIT - only put in the
//					comments but did not implement.
// 2005-08-26	SAM, RTi		Overload getTimeIntervalChoices() to
//					include Irregular.
// 2005-09-27	SAM, RTi		Add isRegularInterval().
// 2006-04-24	SAM, RTi		Change parseInterval() to throw an
//					InvalidTimeIntervalException if the
//					interval is not recognized.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.Time;

import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;

/**
The TimeInterval class provide methods to convert intervals from
integer to string representations.  Common usage is to call the parseInterval()
method to parse a string and then use the integer values to increase
performance.  The TimeInterval data members can be used when creating DateTime instances.
A lookup of string interval names from the integer values may not return
exactly the string that is allowed in a parse (due to case being ignored, etc.).
*/
public class TimeInterval
{
/**
Time interval base values.  These intervals are guaranteed to have values less
than 256 (this should allow for addition of other intervals if necessary).  The
interval values may change in the future.  The values assigned to intervals
increase with the magnitude of the interval (e.g., YEAR > MONTH).  Only irregular has no place in
the order.  Flags above >= 256 are reserved for DateTime constructor flags.
These values are set as the DateTime.PRECISION* values to maintain consistency.
*/
public static final int UNKNOWN = -1; // Unknown, e.g., for initialization
public static final int IRREGULAR = 0;
public static final int HSECOND = 5;
public static final int SECOND = 10;
public static final int MINUTE = 20;
public static final int HOUR = 30;
public static final int DAY = 40;
public static final int WEEK = 50;
public static final int MONTH = 60;
public static final int YEAR = 70;

/**
The string associated with the base interval (e.g, "Month").
*/
private String __intervalBaseString;
/**
The string associated with the interval multiplier (may be "" if
not specified in string used with the constructor).
*/
private String __intervalMultString;
/**
The base data interval.
*/
private int	__intervalBase;
/**
The data interval multiplier.
*/
private int	__intervalMult;

/**
Construct and initialize data to zeros and empty strings.
*/
public TimeInterval ()
{	init();
}

/**
Copy constructor.
@param interval TSInterval to copy.
*/
public TimeInterval ( TimeInterval interval )
{	init();
	__intervalBase = interval.getBase ();
	__intervalMult = interval.getMultiplier ();
	__intervalBaseString = interval.getBaseString ();
	__intervalMultString = interval.getMultiplierString ();
}

/**
Constructor from the integer base and multiplier.  The string base name is
set to defaults.  The multiplier is not relevant if the base is IRREGULAR, although in the future
intervals like IrregMonth may be allowed.
@param base Interval base.
@param mult Interval multiplier.  If set to <= 0, the multiplier string returned
from getMultiplierString() will be set to "" and the integer multiplier will be set to 1.
*/
public TimeInterval ( int base, int mult )
{	init();
	__intervalBase = base;
	__intervalMult = mult;
	__intervalBaseString = getName ( base );
	if ( __intervalBase == IRREGULAR ) {
		__intervalMultString = "";
	}
	else if ( mult <= 0 ) {
		__intervalMultString = "";
		__intervalMult = 1;
	}
	else {
	    __intervalMultString = "" + mult;
	}
}

/**
Determine if two instances are equal.  The base and multiplier are checked.  This
method does not check for cases like 60Minute = 1Hour (false will be returned).
Instead use equivalent(), lessThanOrEqualTo(), or greterThanOrEqualTo().
@param interval TimeInterval to compare.
@return true if the integer interval base and multiplier are equal, false otherwise.
*/
public boolean equals ( TimeInterval interval )
{	if ( (__intervalBase == interval.getBase() ) && (__intervalMult == interval.getMultiplier()) ) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Determine if two instances are equal.  The base and multiplier are checked.  This
method does not check for cases like 60Minute = 1Hour (false will be returned).
Instead use equivalent(), lessThanOrEqualTo(), or greterThanOrEqualTo().
Makes sure the object passed in is a TimeInterval and then calls equals(TimeInterval).
@param o an Object to compare with
@return true if the object is a time interval and if the integer interval base 
and multiplier are equal, false otherwise.
*/
public boolean equals(Object o) {
    boolean eq = o instanceof TimeInterval;
    if (eq) {
        eq = equals((TimeInterval) o);
    }
    return eq;
}

/*
Determine if two instances are equivalent.  The intervals are equivalent if the
interval information matches exactly and in cases like the following:
60Minute = 1Hour.
@param interval TimeInterval to compare.
@return true if the integer interval base and multiplier are equivalent, false
otherwise.
*/
/* TODO SAM 2005-03-03 Need to implement when there is time
public boolean equivalent ( TimeInterval interval )
{	// Do simple check...
	if ( equals(interval) ) {
		return true;
	}
	// Else check for equivalence...
}
*/

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	__intervalBaseString = null;
	__intervalMultString = null;
	super.finalize();
}

/**
Return the interval base (see TimeInterval.INTERVAL*).
@return The interval base (see TimeInterval.INTERVAL*).
*/
public int getBase ()
{	return __intervalBase;
}

/**
Return the interval base as a string.
@return The interval base as a string.
*/
public String getBaseString ()
{	return __intervalBaseString;
}

/**
Return the interval multiplier.
@return The interval multiplier.
*/
public int getMultiplier ()
{	return __intervalMult;
}

/**
Return the interval base as a string.
@return The interval base as a string.
*/
public String getMultiplierString ()
{	return __intervalMultString;
}

/**
Look up an interval name as a string (e.g., "MONTH").  Note that the string is
upper-case.  Call the version with the format if other format is desired.
@return The interval string, or an empty string if not found.
@param interval Time series interval to look up).
@deprecated the version that specifies format should be used.
*/
public static String getName ( int interval )
{
	return getName ( interval, 1 ); // Historical default
}

/**
Look up an interval name as a string (e.g., "MONTH").  Note that the string is
upper-case.  Convert the 2nd+ characters to lower-case if necessary.
@return The interval string, or an empty string if not found.
@param interval Time series interval to look up).
@param format if -1 return lowercase (e.g., "day"),
if 0 return mixed/camel case (e.g., "Day", which will be useful if additional irregular data interval strings are supported),
if 1 return uppercase (e.g., "DAY").
*/
public static String getName ( int interval, int format )
{	String name = "";
	if ( interval == YEAR ) {
		name = "Year";
	}
	else if ( interval == MONTH ) {
		name = "Month";
	}
	else if ( interval == WEEK ) {
		name = "Week";
	}
	else if ( interval == DAY ) {
		name = "Day";
	}
	else if ( interval == HOUR ) {
		name = "Hour";
	}
	else if ( interval == MINUTE ) {
		name = "Minute";
	}
	else if ( interval == SECOND ) {
		name = "Second";
	}
	else if ( interval == HSECOND ) {
		name = "Hsecond";
	}
	else if ( interval == IRREGULAR ) {
		name = "Irregular";
	}
	if ( format > 0 ) {
		// Legacy default
		return name.toUpperCase();
	}
	else if ( format == 0 ) {
		// Trying to move to this as default
		return name;
	}
	else {
		return name.toLowerCase();
	}
}

/**
Return a list of interval strings (e.g., "Year", "6Hour").  Only evenly
divisible choices are returned (no "5Hour" because it does not divide into the
day).  This version does NOT include the Irregular time step.
@return a list of interval strings.
@param start_interval The starting (smallest) interval base to return.
@param end_interval The ending (largest) interval base to return.
@param pad_zeros If true, pad the strings with zeros (e.g., "06Hour").  If false do not pad (e.g., "6Hour").
@param sort_order Specify zero or 1 to sort ascending (small interval to large), -1 to sort descending.
*/
public static List<String> getTimeIntervalChoices ( int start_interval, int end_interval, boolean pad_zeros, int sort_order )
{	return getTimeIntervalChoices ( start_interval, end_interval, pad_zeros, sort_order, false );
}

/**
Return a list of base time interval strings (e.g., "Year", "Hour"), optionally
including the Irregular time step.  No multipliers are prefixed on the time intervals.
@return a list of interval strings.
@param start_interval The starting (smallest) interval base to return.
@param end_interval The ending (largest) interval base to return.
@param sort_order Specify zero or 1 to sort ascending (small interval to large), -1 to sort descending.
@param include_irregular Indicate whether the "Irregular" time step should be
included.  If included, "Irregular" is always at the end of the list.
*/
public static List getTimeIntervalBaseChoices ( int start_interval, int end_interval,
                        int sort_order, boolean include_irregular )
{   // Add in ascending order and sort to descending later if requested...
    List v = new Vector();
    if ( start_interval > end_interval ) {
        // Swap (only rely on sort_order for ordering)...
        int temp = end_interval;
        end_interval = start_interval;
        start_interval = temp;
    }
    if ( (HSECOND >= start_interval) && (HSECOND <= end_interval) ) {
        // TODO SAM 2005-02-16 We probably don't need to support this at all.
    }
    if ( (SECOND >= start_interval) && (SECOND <= end_interval) ) {
        v.add ( "Second" );
    }
    if ( (MINUTE >= start_interval) && (MINUTE <= end_interval) ) {
        v.add ( "Minute" );
    }
    if ( (HOUR >= start_interval) && (HOUR <= end_interval) ) {
        v.add ( "Hour" );
    }
    if ( (DAY >= start_interval) && (DAY <= end_interval) ) {
        v.add ( "Day" );
    }
    // TODO SAM 2005-02-16 Week is not yet supported
    //if ( (WEEK >= start_interval) && (WEEK <= end_interval) ) {
    //}
    if ( (MONTH >= start_interval) && (MONTH <= end_interval) ) {
        v.add ( "Month" );
    }
    if ( (YEAR >= start_interval) && (YEAR <= end_interval) ) {
        v.add ( "Year" );
    }
    if ( sort_order >= 0 ) {
        if ( include_irregular ) {
            v.add ( "Irregular" );
        }
        return v;
    }
    else {
        // Change to descending order...
        int size = v.size();
        List v2 = new Vector ( size );
        for ( int i = size -1; i >= 0; i-- ) {
            v2.add ( v.get(i) );
        }
        if ( include_irregular ) {
            v2.add ( "Irregular" );
        }
        return v2;
    }
}

/**
Return a list of interval strings (e.g., "Year", "6Hour"), optionally
including the Irregular time step.  Only evenly divisible choices are returned
(no "5Hour" because it does not divide into the day).
@return a list of interval strings.
@param start_interval The starting (smallest) interval base to return.
@param end_interval The ending (largest) interval base to return.
@param pad_zeros If true, pad the strings with zeros (e.g., "06Hour").  If false
do not pad (e.g., "6Hour").
@param sort_order Specify zero or 1 to sort ascending (small interval to large), -1 to sort descending.
@param include_irregular Indicate whether the "Irregular" time step should be
included.  If included, "Irregular" is always at the end of the list.
*/
public static List<String> getTimeIntervalChoices ( int start_interval, int end_interval,
	boolean pad_zeros, int sort_order, boolean include_irregular )
{	// Add in ascending order and sort to descending later if requested...
	List<String> v = new Vector<String>();
	if ( start_interval > end_interval ) {
		// Swap (only rely on sort_order for ordering)...
		int temp = end_interval;
		end_interval = start_interval;
		start_interval = temp;
	}
	if ( (HSECOND >= start_interval) && (HSECOND <= end_interval) ) {
		// TODO SAM 2005-02-16 We probably don't need to support this at all.
	}
	if ( (SECOND >= start_interval) && (SECOND <= end_interval) ) {
	    v.add ( "Second" );
		if ( pad_zeros ) {
			v.add ( "01Second" );
			v.add ( "02Second" );
			v.add ( "03Second" );
			v.add ( "04Second" );
			v.add ( "05Second" );
			v.add ( "06Second" );
		}
		else {
		    v.add ( "1Second" );
			v.add ( "2Second" );
			v.add ( "3Second" );
			v.add ( "4Second" );
			v.add ( "5Second" );
			v.add ( "6Second" );
		}
		v.add ( "10Second" );
		v.add ( "15Second" );
		v.add ( "20Second" );
		v.add ( "30Second" );
		v.add ( "60Second" );
	}
	if ( (MINUTE >= start_interval) && (MINUTE <= end_interval) ) {
	    v.add ( "Minute" );
		if ( pad_zeros ) {
			v.add ( "01Minute" );
			v.add ( "02Minute" );
			v.add ( "03Minute" );
			v.add ( "04Minute" );
			v.add ( "05Minute" );
			v.add ( "06Minute" );
		}
		else {
		    v.add ( "1Minute" );
			v.add ( "2Minute" );
			v.add ( "3Minute" );
			v.add ( "4Minute" );
			v.add ( "5Minute" );
			v.add ( "6Minute" );
		}
		v.add ( "10Minute" );
		v.add ( "15Minute" );
		v.add ( "20Minute" );
		v.add ( "30Minute" );
		v.add ( "60Minute" );
	}
	if ( (HOUR >= start_interval) && (HOUR <= end_interval) ) {
        v.add ( "Hour" );
		if ( pad_zeros ) {
			v.add ( "01Hour" );
			v.add ( "02Hour" );
			v.add ( "03Hour" );
			v.add ( "04Hour" );
			v.add ( "06Hour" );
			v.add ( "08Hour" );
		}
		else {
            v.add ( "1Hour" );
			v.add ( "2Hour" );
			v.add ( "3Hour" );
			v.add ( "4Hour" );
			v.add ( "6Hour" );
			v.add ( "8Hour" );
		}
		v.add ( "12Hour" );
        // Add this because often hourly data aggregate up to 24-hour data.
        v.add ( "24Hour" );
	}
	if ( (DAY >= start_interval) && (DAY <= end_interval) ) {
		v.add ( "Day" );
	}
	// TODO SAM 2005-02-16 Week is not yet supported
	//if ( (WEEK >= start_interval) && (WEEK <= end_interval) ) {
	//}
	if ( (MONTH >= start_interval) && (MONTH <= end_interval) ) {
		v.add ( "Month" );
	}
	if ( (YEAR >= start_interval) && (YEAR <= end_interval) ) {
		v.add ( "Year" );
	}
	if ( sort_order >= 0 ) {
		if ( include_irregular ) {
			v.add ( "Irregular" );
		}
		return v;
	}
	else {
	    // Change to descending order...
		int size = v.size();
		List<String> v2 = new Vector<String>( size );
		for ( int i = size -1; i >= 0; i-- ) {
			v2.add ( v.get(i) );
		}
		if ( include_irregular ) {
			v2.add ( "Irregular" );
		}
		return v2;
	}
}

/*
Determine whether the given TimeInterval is greater than the instance based on
a comparison of the length of the interval.
Only intervals that can be explicitly compared should be evaluated with this method.
@return true if the instance is greater than the given TimeInterval.
@param interval The TimeInterval to compare to the instance.
*/
/* TODO 2005-03-03 SAM do later no time.
public boolean greaterThan ( TimeInterval interval )
{	int seconds1 = toSeconds();
	int seconds2 = interval.toSeconds();
	if ( (seconds1 >= 0) && (seconds2 >= 0) ) {
		// Intervals are less than month so a simple comparison can be made...
		if ( seconds1 > seconds2 ) {
			return true;
		}
		else {
		    return false;
		}
	}
}
*/

/**
Determine whether the interval is regular.
@param intervalBase the time interval base to check
@return true if the interval is regular, false if not (unknown or irregular).
*/
public boolean isRegularInterval ( )
{	if ( (__intervalBase >= HSECOND) && (__intervalBase <= YEAR) ) {
		return true;
	}
    // Irregular and unknown are what are left.
	return false;
}

/**
Determine whether an interval is regular.
@param intervalBase the time interval base to check
@return true if the interval is regular, false if not (unknown or irregular).
*/
public static boolean isRegularInterval ( int intervalBase )
{	if ( (intervalBase >= HSECOND) && (intervalBase <= YEAR) ) {
		return true;
	}
    // Irregular and unknown are what are left.
	return false;
}

// TODO need to put in lessThanOrEquivalent()

/*
Determine whether the given TimeInterval is less than the instance based on
a comparison of the length of the interval, for intervals with base Second to Year.
For the sake of comparing largely different intervals, months are assumed to
have 30 days.  Time intervals of 28Day, 29Day, and 31Day will explicitly be
treated as 1Month.  Comparisons for intervals < Month are done using the number
of seconds in the interval.  Comparisons
@return true if the instance is less than the given TimeInterval.
@param interval The TimeInterval to compare to the instance.
*/
/* TODO SAM 2005-03-03 No time - do later.
public boolean lessThan ( TimeInterval interval )
{	int seconds1 = toSecondsApproximate();
	int seconds2 = interval.toSecondsApproximate();
	if ( (seconds1 >= 0) && (seconds2 >= 0) ) {
		// Intervals are less than month so a simple comparison can be made...
		if ( seconds1 < seconds2 ) {
			return true;
		}
		else {
		    return false;
		}
	}
	// Check comparison between intervals involving only month and year...
	int base1 = interval.getBase();
	int mult1 = interval.getMultiplier();
	int base2 = interval.getBase();
	int mult2 = interval.getMultiplier();
}
*/

// TODO need to put in lessThanOrEquivalent()

/**
Initialize the data.
*/
private void init ()
{	__intervalBase = 0;
	__intervalBaseString = "";
	__intervalMult = 0;
	__intervalMultString = "";
}

/**
Determine the time interval multipliers for a base interval string.
This is useful in code where the user picks the interval base and a reasonable
multiplier is given as a choice.  Currently some general decisions are made.
For example, year, week, and regular interval always returns a multiple of 1.
Day interval always returns 1-31 and should be limited by the calling code
based on a specific month, if necessary.
Note that this method returns valid interval multipliers, which may not be the
same as the maximum value for a date/time component.  For example, the maximum
multiplier for an hourly interval is 24 whereas the maximum hour value is 23.
@return an array of multipliers for for the interval string, or null if the
interval_base string is not recognized.
@param interval_base An interval base string that is recognized by parseInterval().
@param divisible If true, the interval multipliers that are returned will result
in intervals that divide evenly into the next interval base.  If false, then all
valid multipliers for the base are returned.
@param include_zero If true, then a zero multiplier is included with all
returned output.  Normally zero is not included.
*/
public static int [] multipliersForIntervalBase ( String interval_base, boolean divisible, boolean include_zero )
{	TimeInterval interval = null;
	try {
	    interval = parseInterval ( interval_base );
	}
	catch ( Exception e ) {
		return null;
	}
	int base = interval.getBase();
	int [] mult = null;
	int offset = 0;		// Used when include_zero is true
	int size = 0;
	if ( include_zero ) {
		offset = 1;
	}
	if ( base == YEAR ) {
		mult = new int[1 + offset];
		if ( include_zero ) {
			mult[0] = 0;
		}
		mult[offset] = 1;
	}
	else if ( base == MONTH ) {
		if ( divisible ) {
			mult = new int[6 + offset];
			if ( include_zero ) {
				mult[0] = 0;
			}
			mult[offset] = 1;
			mult[1 + offset] = 2;
			mult[2 + offset] = 3;
			mult[3 + offset] = 4;
			mult[4 + offset] = 6;
			mult[5 + offset] = 12;
		}
		else {
		    size = 12 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			for ( int i = 0; i < 12; i++ ) {
				mult[i + offset] = i + 1;
			}
		}
	}
	else if ( base == WEEK ) {
		mult = new int[1 + offset];
		if ( include_zero ) {
			mult[0] = 0;
		}
		mult[offset] = 1;
	}
	else if ( base == DAY ) {
		size = 31 + offset;
		mult = new int[size];
		if ( include_zero ) {
			mult[0] = 0;
		}
		for ( int i = 0; i < 31; i++ ) {
			mult[i + offset] = i + 1;
		}
	}
	else if ( base == HOUR ) {
		if ( divisible ) {
			size = 8 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			mult[offset] = 1;
			mult[1 + offset] = 2;
			mult[2 + offset] = 3;
			mult[3 + offset] = 4;
			mult[4 + offset] = 6;
			mult[5 + offset] = 8;
			mult[6 + offset] = 12;
			mult[7 + offset] = 24;
		}
		else {
		    size = 24 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			for ( int i = 0; i < 24; i++ ) {
				mult[i + offset] = i + 1;
			}
		}
	}
	else if ( (base == MINUTE) || (base == SECOND) ) {
		if ( divisible ) {
			size = 12 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			mult[offset] = 1;
			mult[1 + offset] = 2;
			mult[2 + offset] = 3;
			mult[3 + offset] = 4;
			mult[4 + offset] = 5;
			mult[5 + offset] = 6;
			mult[6 + offset] = 10;
			mult[7 + offset] = 12;
			mult[8 + offset] = 15;
			mult[9 + offset] = 20;
			mult[10 + offset] = 30;
			mult[11 + offset] = 60;
		}
		else {
		    size = 60 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			for ( int i = 0; i < 60; i++ ) {
				mult[i + offset] = i + 1;
			}
		}
	}
	else if ( base == HSECOND ) {
		if ( divisible ) {
			size = 8 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			mult[offset] = 1;
			mult[1 + offset] = 2;
			mult[2 + offset] = 4;
			mult[3 + offset] = 5;
			mult[4 + offset] = 10;
			mult[5 + offset] = 20;
			mult[6 + offset] = 50;
			mult[7 + offset] = 100;
		}
		else {
		    size = 100 + offset;
			mult = new int[size];
			if ( include_zero ) {
				mult[0] = 0;
			}
			for ( int i = 0; i < 100; i++ ) {
				mult[i + offset] = i + 1;
			}
		}
	}
	else if ( base == IRREGULAR ) {
		mult = new int[1];
		mult[0] = 1;
	}
	return mult;
}

/**
Parse an interval string like "6Day" into its parts and return as a
TimeInterval.  If the multiplier is not specified, the value returned from
getMultiplierString() will be "", even if the getMultiplier() is 1.
@return The TimeInterval that is parsed from the string.
@param interval_string Time series interval as a string, containing an
interval string and an optional multiplier.
@exception InvalidTimeIntervalException if the interval string cannot be parsed.
*/
public static TimeInterval parseInterval ( String interval_string )
throws InvalidTimeIntervalException
{	String	routine = "TimeInterval.parseInterval";
	int	digit_count=0, dl = 50, i=0, length;

	TimeInterval interval = new TimeInterval ();

	length = interval_string.length();

	// Need to strip of any leading digits.

	while( i < length ){
		if( Character.isDigit(interval_string.charAt(i)) ){
			digit_count++;
			i++;
		}
		else {
		    // We have reached the end of the digit part of the string.
			break;
		}
	}

	if( digit_count == 0 ){
		//
		// The string had no leading digits, interpret as one.
		//
		interval.setMultiplier ( 1 );
	}
	else if( digit_count == length ){
		//
		// The whole string is a digit.
		//
		interval.setBase ( HOUR );
		interval.setMultiplier ( Integer.parseInt( interval_string ));
		if ( Message.isDebugOn ) {
			Message.printDebug( dl, routine, interval.getMultiplier() + " Hourly" );
		}
		return interval;
	}
	else {
	    String interval_mult_string = interval_string.substring(0,digit_count);
		interval.setMultiplier ( Integer.parseInt((interval_mult_string)) );
		interval.setMultiplierString ( interval_mult_string );
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Multiplier: " + interval.getMultiplier() );
	}

	// Now parse out the Base interval

	if(	interval_string.regionMatches(true,digit_count,"minute",0,6) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 6)));
		interval.setBase ( MINUTE );
	}
	else if(interval_string.regionMatches(true,digit_count,"min",0,3) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 3)));
		interval.setBase ( MINUTE );
	}
	else if(interval_string.regionMatches(true,digit_count,"hour",0,4) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 4)));
		interval.setBase ( HOUR );
	}
	else if(interval_string.regionMatches(true,digit_count,"hr",0,2) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 2)));
		interval.setBase ( HOUR );
	}
	else if(interval_string.regionMatches(true,digit_count,"day",0,3) ||
		interval_string.regionMatches(true,digit_count,"dai",0,3) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 3)));
		interval.setBase ( DAY );
	}
	else if(interval_string.regionMatches(true,digit_count,"sec",0,2) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 2)));
		interval.setBase ( SECOND );
	}
	else if(interval_string.regionMatches(true,digit_count,"week",0,4) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 4)));
		interval.setBase ( WEEK );
	}
	else if(interval_string.regionMatches(true,digit_count,"wk",0,2) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 2)));
		interval.setBase ( WEEK );
	}
	else if(interval_string.regionMatches(true,digit_count,"month",0,5) ) {
		interval.setBaseString ( interval_string.substring(digit_count,(digit_count + 5)));
		interval.setBase ( MONTH );
	}
	else if(interval_string.regionMatches(true,digit_count,"mon",0,3) ) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 3)));
		interval.setBase ( MONTH );
	}
	else if(interval_string.regionMatches(true,digit_count,"year",0,4) ) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 4)));
		interval.setBase ( YEAR );
	}
	else if(interval_string.regionMatches(true,digit_count,"yr",0,2) ) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 2)));
		interval.setBase ( YEAR );
	}
	else if(interval_string.regionMatches(true,digit_count,"irregular",0,9)) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 9)));
		interval.setBase ( IRREGULAR );
	}
	else if(interval_string.regionMatches(true,digit_count,"irreg",0,5)) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 5)));
		interval.setBase ( IRREGULAR );
	}
	else if(interval_string.regionMatches(true,digit_count,"irr",0,3) ) {
		interval.setBaseString (interval_string.substring(digit_count,(digit_count + 3)));
		interval.setBase ( IRREGULAR );
	}
	else {
	    if ( interval_string.length() == 0 ) {
			Message.printWarning( 2, routine, "No interval specified." );
		}
		else {
		    Message.printWarning( 2, routine, "Unrecognized interval \"" +
			interval_string.substring(digit_count) + "\"" );
		}
		routine = null;
		throw new InvalidTimeIntervalException ( "Unrecognized time interval \"" + interval_string + "\"" );
	}

	if ( Message.isDebugOn ) {
		Message.printDebug( dl, routine, "Base: " +
		interval.getBase() + " (" + interval.getBaseString() + "), Mult: " + interval.getMultiplier() );
	}

	routine = null;
	return interval;
}

/**
Set the interval base.
@return Zero if successful, non-zero if not.
@param base Time series interval.
*/
public void setBase ( int base )
{	__intervalBase = base;
}

/**
Set the interval base string.  This is normally only called by other methods within this class.
@return Zero if successful, non-zero if not.
@param base_string Time series interval base as string.
*/
public void setBaseString ( String base_string )
{	if ( base_string != null ) {
		__intervalBaseString = base_string;
	}
}

/**
Set the interval multiplier.
@param mult Time series interval.
*/
public void setMultiplier ( int mult )
{	__intervalMult = mult;
}

/**
Set the interval multiplier string.  This is normally only called by other methods within this class.
@param multiplier_string Time series interval base as string.
*/
public void setMultiplierString ( String multiplier_string )
{	if ( multiplier_string != null ) {
		__intervalMultString = multiplier_string;
	}
}

/**
Return the number of seconds in an interval, accounting for the base interval
and multiplier.  Only regular intervals with a base less than or equal to a week
can be processed because of the different number of days in a month.  See
toSecondsApproximate() for a version that will handle all intervals.
@return Number of seconds in an interval, or -1 if the interval cannot be processed.
*/
public int toSeconds ()
{	if ( __intervalBase == SECOND ) {
		return __intervalMult;
	}
	else if ( __intervalBase == MINUTE ) {
		return 60*__intervalMult;
	}
	else if ( __intervalBase == HOUR ) {
		return 3600*__intervalMult;
	}
	else if ( __intervalBase == DAY ) {
		return 86400*__intervalMult;
	}
	else if ( __intervalBase == WEEK ) {
		return 604800*__intervalMult;
	}
	else {
	    return -1;
	}
}

/**
Return the number of seconds in an interval, accounting for the base interval
and multiplier.  For intervals greater than a day, the seconds are computed
assuming 30 days per month (360 days per year).  Intervals of HSecond will
return 0.  The result of this method can then be used to perform relative comparisons of intervals.
@return Number of seconds in an interval.
*/
public int toSecondsApproximate ()
{	if ( __intervalBase == HSECOND ) {
		return 0;
	}
	else if ( __intervalBase == SECOND ) {
		return __intervalMult;
	}
	else if ( __intervalBase == MINUTE ) {
		return 60*__intervalMult;
	}
	else if ( __intervalBase == HOUR ) {
		return 3600*__intervalMult;
	}
	else if ( __intervalBase == DAY ) {
		return 86400*__intervalMult;
	}
	else if ( __intervalBase == WEEK ) {
		return 604800*__intervalMult;
	}
	else if ( __intervalBase == MONTH ) {
		// 86400*30
		return 2592000*__intervalMult;
	}
	else if ( __intervalBase == YEAR ) {
		// 86400*30*13
		return 31104000*__intervalMult;
	}
	else {
	    // Should not happen...
		return -1;
	}
}

/**
Return a string representation of the interval (e.g., "1Month").
If irregular, the base string is returned.  If regular, the multiplier + the
base string is returned (the multiplier may be "" or a number).
@return a string representation of the interval (e.g., "1Month").
*/
public String toString ()
{	return __intervalMultString + __intervalBaseString;
}

}