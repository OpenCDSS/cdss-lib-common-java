// TSUtil_CumulateTimeSeries - cumulate values in a time series.

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

import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
Cumulate values in a time series.
*/
public class TSUtil_CumulateTimeSeries
{
    
/**
Time series to analyze.
*/
private TS __ts = null;

/**
Starting date/time for analysis.
*/
private DateTime __analysisStart = null;

/**
Ending date/time for analysis.
*/
private DateTime __analysisEnd = null;

/**
How to handle missing data.
*/
private CumulateMissingType __handleMissingHow = null;

/**
Date/time to reset, or null if not resetting.
*/
private DateTime __resetDateTime = null;

/**
Value to reset to.
*/
private Double __resetValue = null;

/**
Whether the value should be reset to the input time series value.
*/
private boolean __resetValueToDataValue = false;

/**
Number of missing allowed to compute sample.
*/
private Integer __allowMissingCount = null;

/**
Minimum required sample size.
*/
private Integer __minimumSampleSize = null;

/**
Constructor.
@param ts Time series to process.
@param analysisStart Date to start cumulating.
@param analysisEnd Date to stop cumulating.
@param handleMissingHow If CarryForwardIfMissing, a missing value will be set to the
previous cumulative value (carry forward).  If SetMissingIfMissing,
the result will be set to missing.  Subsequent non-missing data will in any
case increment the last non-missing total.
@param resetDateTime Indicate when to reset the cumulative value to zero.  The year is ignored.
@param resetValue value to reset to, ignored if resetValueToDataValue=true
@param resetValueToDataValue if true, reset the first value to the data value in the original time series,
even if missing
@param allowMissingCount the number of values allowed to be missing in a year when using reset
@param minimumSampleSize the minimum sample size in a year when using reset
*/
public TSUtil_CumulateTimeSeries ( TS ts, DateTime analysisStart, DateTime analysisEnd,
    CumulateMissingType handleMissingHow, DateTime resetDateTime, Double resetValue, boolean resetValueToDataValue,
    Integer allowMissingCount, Integer minimumSampleSize )
{   String routine = "TSUtil_CumulateTimeSeries";
    String message;
    __ts = ts;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __handleMissingHow = handleMissingHow;
    __resetDateTime = resetDateTime;
    __resetValue = resetValue;
    __resetValueToDataValue = resetValueToDataValue;
    __allowMissingCount = allowMissingCount;
    __minimumSampleSize = minimumSampleSize;
    
    if ( ts == null ) {
        // Nothing to do...
        message = "Null input time series";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    
    if ( resetDateTime != null ) {
        if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
            message = "Using Reset to cumulate is not supported for irregular time series.";
            throw new IrregularTimeSeriesNotSupportedException ( message );
        }
        if ( resetValue == null ) {
            // Default
            __resetValue = 0.0;
        }
    }
}

/**
Calculate the starting missing count.  This is needed when a reset date/time is used in order to reflect
that the first year may be partial.  Consequently, the initial part of the year needs to be treated as
missing.
*/
private int calculateStartingCountMissing ( DateTime start, DateTime Reset_DateTime, TS ts )
{
    if ( Reset_DateTime == null ) {
        return 0;
    }
    // Set the year in the reset check to match the start year
    DateTime resetCheck = new DateTime(Reset_DateTime);
    resetCheck.setYear(start.getYear());

    if ( resetCheck.equals(start) ) {
        // If the start is equal to the reset, just return zero (no extra missing)
        return 0;
    }
    else if ( resetCheck.greaterThan(start) ) {
        // Have to decrement the year for the resetCheck and count from
        // there to the start
        resetCheck.addYear(-1);
    }
    // Missing is the number of points between resetCheck and start
    int missingCount = TSUtil.calculateDataSize(resetCheck, start, ts.getDataIntervalBase(), ts.getDataIntervalMult() );
    // Subtract one because don't want to count the start as missing and it will be checked when
    // processing starts
    --missingCount;
    return missingCount;
}

// TODO SAM 2012-07-24 Need to figure out partial years at start.  Seed the missing count with data points
// at the start of the year?
/**
Check to see whether the previous year that was just processed needs to be set to missing because not
enough data values were present.
@param ts time series being processed
@param analysisStart starting date/time for the analysis
@param resetDateTimePrev DateTime that started the year
@param resetDateTime DateTime that ended the year (OK to set this to missing because the value will be reset
appropriate in subsequent code)
@param countNonMissing number of non-missing values in the previous year
@param countMissing number of missing values in the previous year
*/
private void checkForIncompleteYear ( TS ts, DateTime analysisStart, DateTime resetDateTimePrev,
    DateTime resetDateTime, int countNonMissing, int countMissing, Integer allowMissingCount,
    Integer minimumSampleSize )
{   String routine = getClass().getName() + ".checkForIncompleteYear";
    boolean setMissing = false; // Whether to set the year missing
    if ( resetDateTimePrev == null ) {
        // First time through so set to the start
        resetDateTimePrev = analysisStart;
    }
    if ( (allowMissingCount != null) && (countMissing > allowMissingCount) ) {
        // Too many missing values to compute statistic
        Message.printStatus ( 2, routine, "Setting cumulative time series to missing for " +
            resetDateTimePrev + " to " + resetDateTime + " because number of missing values " +
            countMissing + " is > allowed (" + allowMissingCount + ").");
        setMissing = true;
    }
    if ( (minimumSampleSize != null) && (countNonMissing < minimumSampleSize) ) {
        // Sample size too small to compute statistic
        Message.printStatus ( 2, routine, "Not computing time series statistic for " +
            resetDateTimePrev + " to " + resetDateTime + " because sample size " +
            countNonMissing + " is < minimum required (" + minimumSampleSize + ").");
        setMissing = true;
    }
    if ( setMissing ) {
        double missing = ts.getMissing();
        int intervalBase = ts.getDataIntervalBase();
        int intervalMult = ts.getDataIntervalMult();
        for ( DateTime date = new DateTime(resetDateTimePrev); date.lessThanOrEqualTo(resetDateTime);
            date.addInterval(intervalBase,intervalMult) ) {
            ts.setDataValue(date, missing);
        }
    }
}

/**
Add each value over time to create a cumulative value.  The original time series is modified.
*/
public void cumulate (  )
throws Exception
{   String routine = getClass().getName() + ".cumulate";
    TS ts = getTimeSeries();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisEnd = getAnalysisEnd();
    CumulateMissingType handleMissingHow = getHandleMissingHow();
    Double resetValue = getResetValue();
    Integer allowMissingCount = getAllowMissingCount();
    Integer minimumSampleSize = getMinimumSampleSize();

    // Get valid dates because the ones passed in may have been null...
    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, analysisStart, analysisEnd );
    DateTime start = valid_dates.getDate1();
    DateTime end = valid_dates.getDate2();

    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();

    DateTime Reset_DateTime = null; // Reset date/time - will be modified during processing below
    DateTime Reset_DateTime_Prev = null; // Used to make sure full years of missing don't have 0 at start
    if ( getResetDateTime() != null ) {
        Reset_DateTime = new DateTime ( getResetDateTime() );
    }
    boolean resetValueToDataValue = getResetValueToDataValue();

    double total = ts.getMissing();
    int countNonmissing = 0; // Count of values added - used with reset
    int countMissing = 0; // Count of missing values (before carry forward) - used with reset
    boolean isMissing = false;
    double oldvalue;
    // TODO SAM 2012-07-25 Evaluate level of support for irregular time series
    if ( interval_base == TimeInterval.IRREGULAR ) {
        // Get the data and loop through the vector...
        IrregularTS irrts = (IrregularTS)ts;
        List<TSData> alltsdata = irrts.getData();
        if ( alltsdata == null ) {
            // No data for the time series...
            return;
        }
        int nalltsdata = alltsdata.size();
        TSData tsdata = null;
        DateTime date = null;
        for ( int i = 0; i < nalltsdata; i++ ) {
            tsdata = alltsdata.get(i);
            date = tsdata.getDate();
            if ( date.greaterThan(end) ) {
                // Past the end of where we want to go so quit...
                break;
            }
            if ( date.greaterThanOrEqualTo(start) ) {
                oldvalue = tsdata.getDataValue();
                isMissing = ts.isDataMissing(oldvalue);
                if ( !isMissing ) {
                    // Not missing.  Add to total and set value...
                    if ( ts.isDataMissing(total) ) {
                        total = oldvalue;
                    }
                    else {
                        total += oldvalue;
                        ++countNonmissing;
                    }
                    tsdata.setDataValue(total);
                    // Have to do this manually since TSData are being modified directly to improve performance...
                    ts.setDirty ( true );
                }
                else if ( handleMissingHow == CumulateMissingType.CARRY_FORWARD ) {
                    // Missing but want to carry forward previous total...
                    tsdata.setDataValue(total);
                    // Have to do this manually since TSData are being modified directly to improve performance...
                    ts.setDirty ( true );
                }
                // Else, missing and don't want to carry forward so leave as is.
            }
        }
    }
    else {
        // Loop using addInterval
        // If using Reset, set the missing count at the start to reflect a start date that is a
        // partial year
        if ( Reset_DateTime != null ) {
            countMissing = calculateStartingCountMissing ( start, Reset_DateTime, ts );
            Message.printStatus(2,routine,"Missing count for start of first year = " + countMissing );
        }
        for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
            date.addInterval(interval_base, interval_mult) ) {
            oldvalue = ts.getDataValue(date);
            isMissing = ts.isDataMissing(oldvalue);
            if ( !isMissing ) {
                // Not missing.  Add to total and set value...
                if ( ts.isDataMissing(total) ) {
                    total = oldvalue;
                }
                else {
                    total += oldvalue;
                    ++countNonmissing;
                }
                ts.setDataValue(date,total);
            }
            else {
                if ( handleMissingHow == CumulateMissingType.CARRY_FORWARD ) {
                    // Missing but want to carry forward previous total...
                    ts.setDataValue(date,total);
                }
                ++countMissing;
            }
            // Else, missing and don't want to carry forward so leave as is.
            // Check to determine whether the cumulative value needs to be reset to zero.  Note that this
            // will throw away the previous value that was at this point.
            if ( Reset_DateTime != null ) {
                // Set the reset year to the current year to allow for comparison
                Reset_DateTime.setYear(date.getYear());
                if ( Reset_DateTime.equals(date) ) {
                    // Check to see if the previous year needs to be set to missing, which will be the
                    // case if not enough data were available to make the cumulative values credible
                    checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                        countNonmissing, countMissing, allowMissingCount, minimumSampleSize );
                    // Reset the total to the requested value
                    if ( resetValueToDataValue ) {
                        total = oldvalue;
                    }
                    else {
                        // Reset to the specific value
                        total = resetValue;
                    }
                    countNonmissing = 0;
                    countMissing = 0;
                    ts.setDataValue(date,total);
                    // Save as the previous reset date/time in case it needs to be used to set a whole
                    // year to missing
                    Reset_DateTime_Prev = new DateTime(Reset_DateTime);
                }
            }
        }
    }
    if ( interval_base != TimeInterval.IRREGULAR ) {
        // Need to check whether the last year, which may be a partial year, needs its first
        // value set to missing if no data were processed, similar to logic above
        if ( Reset_DateTime != null ) {
            // Check to see if the previous year needs to be set to missing, which will be the
            // case if not enough data were available to make the cumulative values credible
            checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                countNonmissing, countMissing, allowMissingCount, minimumSampleSize );
        }
    }

    // Fill in the genesis information...

    ts.addToGenesis ( "Cumulated " + start + " to " + end + "." );
    ts.setDescription ( ts.getDescription() + ", cumulative" );
}

/**
Return the number of missing values allowed in sample.
@return the number of missing values allowed in sample.
*/
public Integer getAllowMissingCount ()
{
    return __allowMissingCount;
}

/**
Return the analysis end date/time.
@return the analysis end date/time.
*/
public DateTime getAnalysisEnd ()
{
    return __analysisEnd;
}

/**
Return the analysis start date/time.
@return the analysis start date/time.
*/
public DateTime getAnalysisStart ()
{
    return __analysisStart;
}

/**
Return how to handle missing values.
@return how to handle missing values.
*/
public CumulateMissingType getHandleMissingHow ()
{
    return __handleMissingHow;
}

/**
Return the minimum sample size allowed to compute the statistic.
@return the minimum sample size allowed to compute the statistic.
*/
public Integer getMinimumSampleSize ()
{
    return __minimumSampleSize;
}

/**
Return the reset date/time.
@return the reset date/time.
*/
public DateTime getResetDateTime ()
{
    return __resetDateTime;
}

/**
Return the reset value.
@return the reset value.
*/
public Double getResetValue ()
{
    return __resetValue;
}

/**
Return whether to reset the value to the original time series value.
@return whether to reset the value to the original time series value.
*/
public boolean getResetValueToDataValue ()
{
    return __resetValueToDataValue;
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries ()
{
    return __ts;
}

}
