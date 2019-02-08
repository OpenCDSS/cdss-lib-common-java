// TSUtil_RunningAverage - create a new time series that is a running average of values from the input time series.

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

package RTi.TS;

import java.security.InvalidParameterException;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeUtil;

/**
Create a new time series that is a running average of values from the input time series.
*/
public class TSUtil_RunningAverage
{
    
/**
Time series to process.
*/
private TS __ts = null;

/**
Running average type.
*/
private RunningAverageType __runningAverageType = null;

/**
Bracket or N for N-year running average, as per the running average type.
*/
private int __n;

/**
Construct the object and check for valid input.
@param ts regular-interval time series for which to create the running average time series
@param n N for N-year running average and otherwise the bracket for centered, previous, and future running average 
@param type type of running average
*/
public TSUtil_RunningAverage ( TS ts, int n, RunningAverageType runningAverageType )
{   String message;
    String routine = getClass().getName();

    if ( ts == null ) {
        message = "Input time series is null.";
        Message.printWarning ( 2, routine, message );
        throw new InvalidParameterException ( message );
    }
    
    if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        message = "Converting irregular time series to running average is not supported.";
        Message.printWarning ( 2, routine, message );
        throw new IrregularTimeSeriesNotSupportedException ( message );
    }

    boolean found = false;
    for ( RunningAverageType t : getRunningAverageTypeChoices() ) {
        if ( t == runningAverageType ) {
            found = true;
            break;
        }
    }
    if ( !found ) {
        message = "Running average type \"" + runningAverageType + "\" is not supported.";
        Message.printWarning ( 2, routine, message );
        throw new InvalidParameterException ( message );
    }
    
    setTS ( ts );
    setN ( n );
    setRunningAverageType ( runningAverageType );
}

/**
Return the N-year N or bracket.
*/
public int getN ()
{
    return __n;
}

/**
Return the running average type.
*/
public RunningAverageType getRunningAverageType ()
{
    return __runningAverageType;
}

/**
Return the running average types that are supported by the method.
*/
public static RunningAverageType[] getRunningAverageTypeChoices ()
{
    RunningAverageType[] types = {
            RunningAverageType.CENTERED,
            RunningAverageType.FUTURE,
            RunningAverageType.FUTURE_INCLUSIVE,
            RunningAverageType.NYEAR,
            RunningAverageType.N_ALL_YEAR,
            RunningAverageType.PREVIOUS,
            RunningAverageType.PREVIOUS_INCLUSIVE
    };
    return types;
}

/**
Return the input time series being processed.
*/
public TS getTS ()
{
    return __ts;
}
    
/**
Create a running average time series where the time series value is the
average of 1 or more values from the original time series.  The description is
appended with ", centered [N] running average" or ", N-year running average".
@return The new running average time series, which is a copy of the original metadata
but with data being the running average.
@exception RTi.TS.TSException if there is a problem creating and filling the new time series.
*/
public TS runningAverage ()
throws TSException, IrregularTimeSeriesNotSupportedException
{   String  genesis = "", message, routine = getClass().getName() + ".runningAverage";
    TS newts = null;

    TS ts = getTS();
    RunningAverageType type = getRunningAverageType();
    int n = getN();
  
    if ( type == RunningAverageType.NYEAR ) {
        if ( n <= 1 ) {
            // Just return the original time series...
            return ts;
        }
    }
    else if ( (type != RunningAverageType.N_ALL_YEAR) && (n == 0) ) {
        // Just return the original time series...
        return ts;
    }

    // Get a new time series of the proper type...

    int intervalBase = ts.getDataIntervalBase();
    int intervalMult = ts.getDataIntervalMult();
    String newinterval = "" + intervalMult + TimeInterval.getName(intervalBase,1);
    try {
        newts = TSUtil.newTimeSeries ( newinterval, false );
    }
    catch ( Exception e ) {
        message = "Unable to create new time series of interval \"" + newinterval + "\"";
        Message.printWarning ( 3, routine, message );
        throw new RuntimeException ( message );
    }
    newts.copyHeader ( ts );
    newts.setDate1 ( ts.getDate1() );
    newts.setDate2 ( ts.getDate2() );
    newts.allocateDataSpace();

    // Set the offsets for getting data around the current date/time

    int neededCount = 0, offset1 = 0, offset2 = 0;
    if ( type == RunningAverageType.N_ALL_YEAR ) {
        genesis = "NAll-year";
    }
    else if ( type == RunningAverageType.CENTERED ) {
        genesis = "bracket=" + n + " centered";
        // Offset brackets the date...
        offset1 = -1*n;
        offset2 = n;
        neededCount = n*2 + 1;
    }
    else if ( type == RunningAverageType.FUTURE ) {
        genesis = "bracket=" + n + " future (not inclusive)";
        // Offset brackets the date...
        offset1 = 1;
        offset2 = n;
        neededCount = n;
    }
    else if ( type == RunningAverageType.FUTURE_INCLUSIVE ) {
        genesis = "bracket=" + n + " future (inclusive)";
        // Offset brackets the date...
        offset1 = 0;
        offset2 = n;
        neededCount = n + 1;
    }
    else if ( type == RunningAverageType.NYEAR ) {
        genesis = n + "-year";
        // Offset is to the left but remember to include the time step itself...
        offset1 = -1*(n - 1);
        offset2 = 0;
        neededCount = n;
    }
    else if ( type == RunningAverageType.PREVIOUS ) {
        genesis = "bracket=" + n + " previous (not inclusive)";
        // Offset brackets the date...
        offset1 = -n;
        offset2 = -1;
        neededCount = n;
    }
    else if ( type == RunningAverageType.PREVIOUS_INCLUSIVE ) {
        genesis = "bracket=" + n + " previous (inclusive)";
        // Offset brackets the date...
        offset1 = -n;
        offset2 = 0;
        neededCount = n + 1;
    }
    
    // Iterate through the full period of the output time series

    DateTime date = new DateTime ( ts.getDate1() );
    DateTime end = new DateTime ( ts.getDate2() );
    double sum;
    DateTime valueDateTime = new DateTime(newts.getDate1());  // Used to access data values for average
    int count, i;
    double value = 0.0;
    double missing = ts.getMissing();
    for ( ; date.lessThanOrEqualTo( end ); date.addInterval(intervalBase, intervalMult) ) {
        // Initialize the date for looking up values to the initial offset from the loop date...
        valueDateTime.setDate ( date );
        // Offset from the current date/time to the start of the bracket
        if ( type == RunningAverageType.NYEAR ) {
            valueDateTime.addInterval ( TimeInterval.YEAR, offset1 );
        }
        else if ( type == RunningAverageType.N_ALL_YEAR ) {
            // Reset to the start of the period and set the offsets to process the start year to the
            // current year
            valueDateTime.setYear ( newts.getDate1().getYear() );
            if ( valueDateTime.lessThan(newts.getDate1())) {
                // Has wrapped around since the first date/time was not the start of a year so add another year
                valueDateTime.addYear(1);
            }
            offset1 = valueDateTime.getYear();
            offset2 = date.getYear();
        }
        else {
            valueDateTime.addInterval ( intervalBase, offset1*intervalMult );
        }
        // Now loop through the intervals in the bracket and get the right values to average...
        count = 0;
        sum = 0.0;
        for ( i = offset1; i <= offset2; i++ ) {
            // This check should fail harmlessly if dealing with intervals greater than a day
            if ( (valueDateTime.getMonth() == 2) && (valueDateTime.getDay() == 29) &&
                !TimeUtil.isLeapYear(valueDateTime.getYear()) ) {
                // The Feb 29 that we are requesting in another year does not exist.  Set to missing
                // This will result in the final output also being missing.
                value = missing;
            }
            else {
                // Normal data access.
                value = ts.getDataValue ( valueDateTime );
            }
            if ( ts.isDataMissing(value) ) {
                if ( type != RunningAverageType.N_ALL_YEAR ) {
                    // Break because no missing are allowed.
                    // Below detect whether have the right count to do the average...
                    break;
                }
            }
            else {
                // Add the value to the sum (which has been initialized to zero above...
                sum += value;
                ++count;
            }
            // Reset the dates for the averaging input data value...
            if ( (type == RunningAverageType.NYEAR) || (type == RunningAverageType.N_ALL_YEAR) ) {
                // Get the value for the next year (last value will be the current year).
                valueDateTime.addInterval ( TimeInterval.YEAR, 1 );
            }
            else {
                // Just move forward incrementally between end points
                valueDateTime.addInterval ( intervalBase, intervalMult );
            }
        }
        // Now set the data value to the computed average...
        if ( type == RunningAverageType.N_ALL_YEAR ) {
            // Always compute for count > 0
            if ( count > 0 ) {
                newts.setDataValue(date,sum/count);
            }
        }
        else if ( count == neededCount ) {
            if ( count > 0 ) {
                newts.setDataValue(date,sum/count);
            }
        }
    }

    // Add to the genesis...

    newts.addToGenesis ( "Created " + genesis + " running average time series from original data" );
    newts.setDescription ( newts.getDescription() + ", " + genesis + " run ave" );
    return newts;
}

/**
Set the N for N-Year or bracket for other running average types.
*/
private void setN ( int n )
{
    __n = n;
}

/**
Set the running average type.
*/
private void setRunningAverageType ( RunningAverageType runningAverageType )
{
    __runningAverageType = runningAverageType;
}

/**
Set the time series to process.
*/
private void setTS ( TS ts )
{
    __ts = ts;
}

}
