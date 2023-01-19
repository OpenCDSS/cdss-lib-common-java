// TimeInterval - time interval class

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

import java.util.ArrayList;
import java.util.List;

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
public static final int UNKNOWN = -1; // Unknown, e.g., for initialization.
public static final int IRREGULAR = 0;    // Value is important as 0 to allow checks for regular interval.
public static final int NANOSECOND = 2;   // 0 - 999999999
public static final int MICROSECOND = 3;  // 0 - 999999
public static final int MILLISECOND = 4;  // 0 - 999
public static final int HSECOND = 5;      // 0 - 99
public static final int SECOND = 10;
public static final int MINUTE = 20;
public static final int HOUR = 30;
public static final int DAY = 40;
public static final int WEEK = 50;
public static final int MONTH = 60;
public static final int YEAR = 70;

/**
The string associated with the base interval:
- for regular interval, will be something like "Month" or "1Month"
- for irregular interval, will be the entire string like "Irregular", "IrregSecond"
*/
private String __intervalBaseString;

/**
The string associated with the interval multiplier:
- "15" if the full interval string is "15Minute"
- may be "" if not specified in string used with the constructor
- not used for irregular interval
*/
private String __intervalMultString;

/**
The base data interval:
- static value such as MONTH, IRREGULAR
- if IRREGULAR the precision is stored separately as 'irregularPrecision'
*/
private int	__intervalBase;

/**
The data interval multiplier:
- for regular interval time series
- not used for irregular time series
*/
private int	__intervalMult;

/**
 * Precision used with irregular interval:
 * - for example SECOND for "IrregSecond", MONTH for "IrregMonth"
 */
private int irregularPrecision = UNKNOWN;

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
Constructor from the integer base and multiplier.
The string base name is set to defaults.
The multiplier is not relevant if the base is IRREGULAR, although in the future
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
Determine if two instances are equal.  The base and multiplier are checked.
This method does not check for cases like 60Minute = 1Hour (false will be returned).
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
Determine if two instances are equal.  The base and multiplier are checked.
This method does not check for cases like 60Minute = 1Hour (false will be returned).
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
{	// Do simple check.
	if ( equals(interval) ) {
		return true;
	}
	// Else check for equivalence.
}
*/

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
 * Return the precision for irregular interval time series.
 * This is more explicit than relying on date/time used in irregular time series.
 * @return the precision for irregular interval time series, for example SECOND is returned if interval is "IrregSecond"
 */
public int getIrregularIntervalPrecision () {
	return this.irregularPrecision;
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
Look up an interval name as a string (e.g., "MONTH").
Note that the string is upper-case.  Call the version with the format if other format is desired.
@return The interval string, or an empty string if not found.
@param interval Time series interval to look up).
@deprecated the version that specifies format should be used.
*/
public static String getName ( int interval ) {
	return getName ( interval, 1 ); // Historical default.
}

/**
Look up an interval name as a string (e.g., "MONTH").
Note that the string is upper-case.  Convert the 2nd+ characters to lower-case if necessary.
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
	else if ( interval == MILLISECOND ) {
		name = "Millisecond";
	}
	else if ( interval == MICROSECOND ) {
		name = "Microsecond";
	}
	else if ( interval == NANOSECOND ) {
		name = "Nanosecond";
	}
	else if ( interval == IRREGULAR ) {
		name = "Irregular";
	}
	else if ( interval == UNKNOWN ) {
		name = "Unknown";
	}
	if ( format > 0 ) {
		// Legacy default.
		return name.toUpperCase();
	}
	else if ( format == 0 ) {
		// Trying to move to this as default.
		return name;
	}
	else {
		return name.toLowerCase();
	}
}

/**
Return a list of interval strings (e.g., "Year", "6Hour").
Only evenly divisible choices are returned (no "5Hour" because it does not divide into the day).
This version does NOT include the Irregular time step.
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
Return a list of base time interval strings (e.g., "Year", "Hour"),
optionally including the Irregular time step.  No multipliers are prefixed on the time intervals.
@return a list of interval strings.
@param startInterval The starting (smallest) interval base to return.
@param endInterval The ending (largest) interval base to return.
@param sortOrder Specify zero or 1 to sort ascending (small interval to large), -1 to sort descending.
@param includeIrregular Indicate whether the "Irregular" time step should be included.
If included, "Irregular" is always at the end of the list.
*/
public static List<String> getTimeIntervalBaseChoices ( int startInterval, int endInterval,
    int sortOrder, boolean includeIrregular )
{   // Add in ascending order and sort to descending later if requested.
    List<String> v = new ArrayList<>();
    if ( startInterval > endInterval ) {
        // Swap (only rely on sort_order for ordering).
        int temp = endInterval;
        endInterval = startInterval;
        startInterval = temp;
    }
    if ( (NANOSECOND >= startInterval) && (NANOSECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Nanosecond" );
    }
    if ( (MICROSECOND >= startInterval) && (MICROSECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Microsecond" );
    }
    if ( (MILLISECOND >= startInterval) && (MILLISECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Millisecond" );
    }
    if ( (HSECOND >= startInterval) && (HSECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 We probably don't need to support this?
        //v.add ( "Hsecond" );
    }
    if ( (SECOND >= startInterval) && (SECOND <= endInterval) ) {
        v.add ( "Second" );
    }
    if ( (MINUTE >= startInterval) && (MINUTE <= endInterval) ) {
        v.add ( "Minute" );
    }
    if ( (HOUR >= startInterval) && (HOUR <= endInterval) ) {
        v.add ( "Hour" );
    }
    if ( (DAY >= startInterval) && (DAY <= endInterval) ) {
        v.add ( "Day" );
    }
    // TODO SAM 2005-02-16 Week is not yet supported.
    //if ( (WEEK >= start_interval) && (WEEK <= end_interval) ) {
    //}
    if ( (MONTH >= startInterval) && (MONTH <= endInterval) ) {
        v.add ( "Month" );
    }
    if ( (YEAR >= startInterval) && (YEAR <= endInterval) ) {
        v.add ( "Year" );
    }
    if ( sortOrder >= 0 ) {
        if ( includeIrregular ) {
        	// Newer code handles precision on irregular time series date/times.
            v.add ( "IrregNanoSecond" );
            v.add ( "IrregMicroSecond" );
            v.add ( "IrregMilliSecond" );
            v.add ( "IrregHSecond" );
            v.add ( "IrregSecond" );
            v.add ( "IrregMinute" );
            v.add ( "IrregHour" );
            v.add ( "IrregDay" );
            v.add ( "IrregMonth" );
            v.add ( "IrregYear" );
            // Legacy value goes to seconds by default or whatever precision is detected when parsing date/time.
            v.add ( "Irregular" );
        }
        return v;
    }
    else {
        // Change to descending order.
        int size = v.size();
        List<String> v2 = new ArrayList<> ( size );
        for ( int i = size -1; i >= 0; i-- ) {
            v2.add ( v.get(i) );
        }
        if ( includeIrregular ) {
        	// Newer code handles precision on irregular time series date/times.
            v2.add ( "IrregYear" );
            v2.add ( "IrregMonth" );
            v2.add ( "IrregDay" );
            v2.add ( "IrregHour" );
            v2.add ( "IrregMinute" );
            v2.add ( "IrregSecond" );
            v2.add ( "IrregHSecond" );
            v2.add ( "IrregMilliSecond" );
            v2.add ( "IrregMicroSecond" );
            v2.add ( "IrregNanoSecond" );
            // Legacy value goes to seconds by default or whatever precision is detected when parsing date/time.
            v2.add ( "Irregular" );
        }
        return v2;
    }
}

/**
Return a list of interval strings (e.g., "Year", "6Hour"),
optionally including the Irregular time step.  Only evenly divisible choices are returned
(no "5Hour" because it does not divide into the day).
@return a list of interval strings.
@param startInterval The starting (smallest) interval base to return.
@param endInterval The ending (largest) interval base to return.
@param pad_zeros If true, pad the strings with zeros (e.g., "06Hour").
If false do not pad (e.g., "6Hour").
@param sort_order Specify zero or 1 to sort ascending (small interval to large), -1 to sort descending.
@param include_irregular Indicate whether the "Irregular" interval should be included.
If included, "Irregular" is always at the end of the list.
*/
public static List<String> getTimeIntervalChoices ( int startInterval, int endInterval,
	boolean pad_zeros, int sort_order, boolean include_irregular )
{	// Add in ascending order and sort to descending later if requested.
	List<String> v = new ArrayList<>();
	if ( startInterval > endInterval ) {
		// Swap (only rely on sort_order for ordering).
		int temp = endInterval;
		endInterval = startInterval;
		startInterval = temp;
	}
    if ( (NANOSECOND >= startInterval) && (NANOSECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Nanosecond" );
    }
    if ( (MICROSECOND >= startInterval) && (MICROSECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Microsecond" );
    }
    if ( (MILLISECOND >= startInterval) && (MILLISECOND <= endInterval) ) {
        // TODO SAM 2005-02-16 Probably don't need to support this?
        //v.add ( "Millisecond" );
    }
	if ( (HSECOND >= startInterval) && (HSECOND <= endInterval) ) {
		// TODO SAM 2005-02-16 Probably don't need to support this.
	}
	if ( (SECOND >= startInterval) && (SECOND <= endInterval) ) {
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
	if ( (MINUTE >= startInterval) && (MINUTE <= endInterval) ) {
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
	if ( (HOUR >= startInterval) && (HOUR <= endInterval) ) {
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
	if ( (DAY >= startInterval) && (DAY <= endInterval) ) {
		v.add ( "Day" );
	}
	// TODO SAM 2005-02-16 Week is not yet supported.
	//if ( (WEEK >= start_interval) && (WEEK <= end_interval) ) {
	//}
	if ( (MONTH >= startInterval) && (MONTH <= endInterval) ) {
		v.add ( "Month" );
	}
	if ( (YEAR >= startInterval) && (YEAR <= endInterval) ) {
		v.add ( "Year" );
	}
	if ( sort_order >= 0 ) {
		if ( include_irregular ) {
        	// Newer code handles precision on irregular time series date/times.
            v.add ( "IrregNanoSecond" );
            v.add ( "IrregMicroSecond" );
            v.add ( "IrregMilliSecond" );
            v.add ( "IrregHSecond" );
            v.add ( "IrregSecond" );
            v.add ( "IrregMinute" );
            v.add ( "IrregHour" );
            v.add ( "IrregDay" );
            v.add ( "IrregMonth" );
            v.add ( "IrregYear" );
            // Legacy value goes to seconds by default or whatever precision is detected when parsing date/time.
			v.add ( "Irregular" );
		}
		return v;
	}
	else {
	    // Change to descending order.
		int size = v.size();
		List<String> v2 = new ArrayList<>( size );
		for ( int i = size -1; i >= 0; i-- ) {
			v2.add ( v.get(i) );
		}
		if ( include_irregular ) {
        	// Newer code handles precision on irregular time series date/times.
            v2.add ( "IrregYear" );
            v2.add ( "IrregMonth" );
            v2.add ( "IrregDay" );
            v2.add ( "IrregHour" );
            v2.add ( "IrregMinute" );
            v2.add ( "IrregSecond" );
            v2.add ( "IrregHSecond" );
            v2.add ( "IrregMilliSecond" );
            v2.add ( "IrregMicroSecond" );
            v2.add ( "IrregNanoSecond" );
            // Legacy value goes to seconds by default or whatever precision is detected when parsing date/time.
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
		// Intervals are less than month so a simple comparison can be made.
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
Determine whether the interval is irregular.
@return true if the interval is irregular, false if not (unknown or regular).
*/
public boolean isIrregularInterval ( ) {
	if ( __intervalBase == IRREGULAR ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Determine whether the interval is regular.
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
For the sake of comparing largely different intervals, months are assumed to have 30 days.
Time intervals of 28Day, 29Day, and 31Day will explicitly be treated as 1Month.
Comparisons for intervals < Month are done using the number
of seconds in the interval.
@return true if the instance is less than the given TimeInterval.
@param interval The TimeInterval to compare to the instance.
*/
/* TODO SAM 2005-03-03 No time - do later.
public boolean lessThan ( TimeInterval interval )
{	int seconds1 = toSecondsApproximate();
	int seconds2 = interval.toSecondsApproximate();
	if ( (seconds1 >= 0) && (seconds2 >= 0) ) {
		// Intervals are less than month so a simple comparison can be made.
		if ( seconds1 < seconds2 ) {
			return true;
		}
		else {
		    return false;
		}
	}
	// Check comparison between intervals involving only month and year.
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
This is useful in code where the user picks the interval base and a reasonable multiplier is given as a choice.
Currently some general decisions are made.
For example, year, week, and regular interval always returns a multiple of 1.
Day interval always returns 1-31 and should be limited by the calling code based on a specific month, if necessary.
Note that this method returns valid interval multipliers,
which may not be the same as the maximum value for a date/time component.
For example, the maximum multiplier for an hourly interval is 24 whereas the maximum hour value is 23.
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
	int offset = 0;		// Used when include_zero is true.
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
Parse an interval string like "6Day" into its parts and return as a TimeInterval.
If the multiplier is not specified, the value returned from
getMultiplierString() will be "", even if the getMultiplier() is 1.
The following are example interval strings:
   Hour
   1Hour
   Irregular
   IrregSecond
   IrregMillisecond
@return The TimeInterval that is parsed from the string.
@param intervalString Time series interval as a string, containing an
interval string and an optional multiplier.
@exception InvalidTimeIntervalException if the interval string cannot be parsed.
*/
public static TimeInterval parseInterval ( String intervalString )
throws InvalidTimeIntervalException {
	String routine = "TimeInterval.parseInterval";
	int	digitCount = 0; // Count of digits at start of the interval string.
	int dl = 10;
	int i = 0;
	int length = intervalString.length();

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Parsing interval \"" + intervalString + "\"." );
	}

	TimeInterval interval = new TimeInterval ();

	// Parse the leading digits to get the multiplier.

	while ( i < length ) {
		if( Character.isDigit(intervalString.charAt(i)) ){
			digitCount++;
			i++;
		}
		else {
		    // Have reached the end of the digit part of the string.
			break;
		}
	}

	if ( digitCount == 0 ) {
		// The string had no leading digits, interpret as multiplier of "1".
		interval.setMultiplier ( 1 );
	}
	else if ( digitCount == length ) {
		// The whole string is a digit, default to hourly (legacy behavior).
		interval.setBase ( HOUR );
		interval.setMultiplier ( Integer.parseInt( intervalString ));
		if ( Message.isDebugOn ) {
			Message.printDebug( dl, routine, interval.getMultiplier() + " Hourly" );
		}
		return interval;
	}
	else {
		// Parse the multiplier from the start of the interval string.
	    String intervalMultString = intervalString.substring(0,digitCount);
		interval.setMultiplier ( Integer.parseInt((intervalMultString)) );
		interval.setMultiplierString ( intervalMultString );
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Multiplier: " + interval.getMultiplier() );
	}

	// Now parse out the base interval:
	// - check for irregular first because substrings may also show up in regular interval
	// - if irregular can also have a precision
	// - if regular, will have base and multiplier

	String intervalBaseString = intervalString.substring(digitCount).trim();
	String intervalBaseStringUpper = intervalBaseString.toUpperCase();

	boolean intervalFound = false; // Use this to check for bad interval, more explicit than checking other values.
	if ( intervalBaseStringUpper.startsWith("IRR") ) {
		// Handles:
		//  IRREGULAR
		//  IrregHsecond
		//  IrregSecond
		//  ...
		//  IrregYear
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Detected irregular interval.  Determining precision." );
		}
		interval.setBaseString ( intervalBaseString );
		interval.setBase ( IRREGULAR );
		intervalFound = true;
		// Set the irregular interval precision:
		// - list date first and then time
		// - don't allow the modifiers to be at the start of the string so search for index > 0
		if( (intervalBaseStringUpper.indexOf("YEAR") > 0) || (intervalBaseStringUpper.indexOf("YR") > 0) ) {
			interval.irregularPrecision = YEAR;
		}
		else if ( intervalBaseStringUpper.indexOf("MON") > 0 ) {
			interval.irregularPrecision = MONTH;
		}
		else if ( (intervalBaseStringUpper.indexOf("WEEK") > 0) || (intervalBaseStringUpper.indexOf("WK") > 0) ) {
			interval.irregularPrecision = WEEK;
		}
		else if ( (intervalBaseStringUpper.indexOf("DAY") > 0) ||  (intervalBaseStringUpper.indexOf("DAI") > 0) ) {
			interval.irregularPrecision = ( DAY );
		}
		else if ( (intervalBaseStringUpper.indexOf("HOUR") > 0) || (intervalBaseStringUpper.indexOf("HR") > 0) ) {
			interval.irregularPrecision = HOUR;
		}
		else if ( intervalBaseStringUpper.indexOf("MIN") > 0 ) {
			interval.irregularPrecision = MINUTE;
		}
		else if ( intervalBaseStringUpper.indexOf("NANO") > 0 ) {
			interval.irregularPrecision = NANOSECOND;
		}
		else if ( intervalBaseStringUpper.indexOf("MICRO") > 0 ) {
			interval.irregularPrecision = MICROSECOND;
		}
		else if ( intervalBaseStringUpper.indexOf("MILLI") > 0 ) {
			interval.irregularPrecision = MILLISECOND;
		}
		else if ( intervalBaseStringUpper.indexOf("HSEC") > 0 ) {
			interval.irregularPrecision = HSECOND;
		}
		else if ( intervalBaseStringUpper.indexOf("SEC") > 0 ) {
			interval.irregularPrecision = SECOND;
		}
		else {
			if ( Message.isDebugOn ) {
				Message.printDebug( dl, routine, "No precision specified for irregular interval \"" + intervalBaseString + "\", defaulting to second." );
				interval.irregularPrecision = SECOND;
			}
		}
	}
	else {
		// Regular interval:
		// - list date first and then time
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Detected regular interval.  Determining base interval." );
		}
		if ( intervalBaseStringUpper.startsWith("YEAR") || intervalBaseStringUpper.startsWith("YR") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( YEAR );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("MON") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( MONTH );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("WEEK") || intervalBaseStringUpper.startsWith("WK") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( WEEK );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("DAY") || intervalBaseStringUpper.startsWith("DAI") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( DAY );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("HOUR") || intervalBaseStringUpper.startsWith("HR") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( HOUR );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("MIN") ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( MINUTE );
			intervalFound = true;
		}
		// List times in decreasing size but put seconds at end since test string may show up in other intervals.
		else if ( intervalBaseStringUpper.indexOf("HSEC") >= 0 ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( HSECOND );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.indexOf("MILLI") >= 0 ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( MILLISECOND );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.indexOf("MICRO") >= 0 ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( MICROSECOND );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.indexOf("NANO") >= 0 ) {
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( NANOSECOND );
			intervalFound = true;
		}
		else if ( intervalBaseStringUpper.startsWith("SEC") ) {
			// Make sure this is after other interval strings that might include "sec".
			interval.setBaseString ( intervalBaseString );
			interval.setBase ( SECOND );
			intervalFound = true;
		}
		else {
			// Interval does not match a known regular interval.
	    	if ( intervalString.length() == 0 ) {
				Message.printWarning( 2, routine, "No interval specified.  Can't determine if irregular or regular." );
			}
			else {
		    	Message.printWarning( 2, routine, "Unrecognized interval \"" + intervalString.substring(digitCount) + "\"." );
			}
			throw new InvalidTimeIntervalException ( "Unrecognized time interval \"" + intervalString + "\"." );
		}
	}

	if ( !intervalFound ) {
		throw new InvalidTimeIntervalException ( "Unrecognized time interval \"" + intervalString + "\"." );
	}

	if ( Message.isDebugOn ) {
		Message.printDebug( dl, routine, "Base: " +
		interval.getBase() + " (" + interval.getBaseString() + "), Mult: " + interval.getMultiplier() );
	}

	return interval;
}

/**
Set the interval base.
@param base Time series interval.
*/
public void setBase ( int base ) {
	__intervalBase = base;
}

/**
Set the interval base string.  This is normally only called by other methods within this class.
@param base_string Time series interval base as string.
*/
public void setBaseString ( String base_string ) {
	if ( base_string != null ) {
		__intervalBaseString = base_string;
	}
}

/**
Set the irregular interval precision.
This method should only be called in particular cases.
Normally the 'parseInterval' method handles setting.
@param base Time series interval.
*/
public void setIrregularIntervalPrecision ( int precision ) {
	this.irregularPrecision = precision;
}

/**
Set the interval multiplier.
@param mult Time series interval.
*/
public void setMultiplier ( int mult ) {
	__intervalMult = mult;
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
Return the number of seconds in an interval, accounting for the base interval and multiplier.
Only regular intervals with a base less than or equal to a week
can be processed because of the different number of days in a month.
See toSecondsApproximate() for a version that will handle all intervals.
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
Return the number of seconds in an interval, accounting for the base interval and multiplier.
For intervals greater than a day, the seconds are computed assuming 30 days per month (360 days per year).
Intervals of HSecond or smaller will return 0.
The result of this method can then be used to perform relative comparisons of intervals.
@return Number of seconds in an interval.
*/
public int toSecondsApproximate () {
	if ( __intervalBase == NANOSECOND ) {
		return 0;
	}
	else if ( __intervalBase == MICROSECOND ) {
		return 0;
	}
	else if ( __intervalBase == MILLISECOND ) {
		return 0;
	}
	else if ( __intervalBase == HSECOND ) {
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
	    // Should not happen.
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