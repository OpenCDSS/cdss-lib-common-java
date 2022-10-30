// TSUtil_CumulateTimeSeries - cumulate values in a time series.

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

package RTi.TS;

import java.util.ArrayList;
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
For irregular interval time series, whether to insert a zero data point at the reset date/time the first
point in a year is after the reset date/time.
*/
private boolean __insertResetPoint = false;

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
@param insertResetPoint for irregular interval time series,
insert a zero data point at the reset date/time if the first data point within the year is after
the reset date/time
@param allowMissingCount the number of values allowed to be missing in a year when using reset
@param minimumSampleSize the minimum sample size in a year when using reset
*/
public TSUtil_CumulateTimeSeries ( TS ts, DateTime analysisStart, DateTime analysisEnd,
    CumulateMissingType handleMissingHow,
    DateTime resetDateTime, Double resetValue, boolean resetValueToDataValue, boolean insertResetPoint,
    Integer allowMissingCount, Integer minimumSampleSize )
{   String routine = getClass().getSimpleName() + ".TSUtil_CumulateTimeSeries";
    String message;
    __ts = ts;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __handleMissingHow = handleMissingHow;
    __resetDateTime = resetDateTime;
    __resetValue = resetValue;
    __resetValueToDataValue = resetValueToDataValue;
    __insertResetPoint = insertResetPoint;
    __allowMissingCount = allowMissingCount;
    __minimumSampleSize = minimumSampleSize;
    
    if ( ts == null ) {
        // Nothing to do.
        message = "Null input time series";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    
    if ( resetDateTime != null ) {
    	// TODO smalers 2022-10-24 remove once tested out.
        //if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        //    message = "Using Reset to cumulate is not supported for irregular time series.";
        //    throw new IrregularTimeSeriesNotSupportedException ( message );
        //}
        if ( resetValue == null ) {
            // Default.
            __resetValue = 0.0;
        }
    }
}

/**
Calculate the starting missing count.
This is needed when a reset date/time is used in order to reflect that the first year may be partial.s
Consequently, the initial part of the year needs to be treated as missing.
*/
private int calculateStartingCountMissing ( DateTime start, DateTime Reset_DateTime, TS ts ) {
    if ( Reset_DateTime == null ) {
        return 0;
    }
    // Set the year in the reset check to match the start year.
    DateTime resetCheck = new DateTime(Reset_DateTime);
    resetCheck.setYear(start.getYear());

    if ( resetCheck.equals(start) ) {
        // If the start is equal to the reset, just return zero (no extra missing).
        return 0;
    }
    else if ( resetCheck.greaterThan(start) ) {
        // Have to decrement the year for the resetCheck and count from there to the start.
        resetCheck.addYear(-1);
    }
    // Missing is the number of points between resetCheck and start.
    int missingCount = TSUtil.calculateDataSize(resetCheck, start, ts.getDataIntervalBase(), ts.getDataIntervalMult() );
    // Subtract one because don't want to count the start as missing and it will be checked when processing starts.
    --missingCount;
    return missingCount;
}

// TODO SAM 2012-07-24 Need to figure out partial years at start.
// Seed the missing count with data points at the start of the year?
/**
Check to see whether the previous year that was just processed needs to be set to missing because not
enough data values were present.
@param ts time series being processed
@param analysisStart starting date/time for the analysis
@param resetDateTimePrev DateTime that started the previous year,
values >= this date/time will be set to missing if the year does not meet data requirements
@param resetDateTime DateTime that is the reset for the current year,
value will not be modified in this method in any case
@param countNonMissing number of non-missing values in the previous year
@param countMissing number of missing values in the previous year
@param allowMissingCount the number of missing values allowed to accept the year's data
@param minimumSampleSize the minimum sample size required to accept the year's data
*/
private void checkForIncompleteYear ( TS ts, DateTime analysisStart, DateTime resetDateTimePrev,
    DateTime resetDateTime, int countNonMissing, int countMissing, Integer allowMissingCount,
    Integer minimumSampleSize )
throws Exception
{   String routine = getClass().getSimpleName() + ".checkForIncompleteYear";
    boolean setMissing = false; // Whether to set the year missing.
    if ( resetDateTimePrev == null ) {
        // First time through so set to the start.
        resetDateTimePrev = analysisStart;
    }
    if ( (allowMissingCount != null) && (countMissing > allowMissingCount) ) {
        // Too many missing values to compute statistic.
        Message.printStatus ( 2, routine, "Setting cumulative time series to missing for " +
            resetDateTimePrev + " to " + resetDateTime + " because number of missing values " +
            countMissing + " is > allowed (" + allowMissingCount + ").");
        setMissing = true;
    }
    if ( (minimumSampleSize != null) && (countNonMissing < minimumSampleSize) ) {
        // Sample size too small to compute statistic.
        Message.printStatus ( 2, routine, "Not computing time series statistic for " +
            resetDateTimePrev + " to " + resetDateTime + " because sample size " +
            countNonMissing + " is < minimum required (" + minimumSampleSize + ").");
        setMissing = true;
    }
    if ( setMissing ) {
        double missing = ts.getMissing();
        int intervalBase = ts.getDataIntervalBase();
        int intervalMult = ts.getDataIntervalMult();
        if ( ts.getDataIntervalBase() == TimeInterval.IRREGULAR ) {
        	// Irregular interval time series.
        	TSIterator tsi = ts.iterator(resetDateTimePrev, resetDateTime);
        	TSData tsdata = null;
        	while ( (tsdata = tsi.next()) != null ) {
        		// Because the iterator is inclusive, need to check to exclude the 'resetDateTime'.
        		if ( tsi.getDate().lessThan(resetDateTime) ) {
        			ts.setDataValue(tsdata.getDate(), missing);
        		}
        	}
        }
        else {
        	// Regular interval time series.
        	for ( DateTime date = new DateTime(resetDateTimePrev); date.lessThan(resetDateTime);
            	date.addInterval(intervalBase,intervalMult) ) {
            	ts.setDataValue(date, missing);
        	}
        }
    }
}

/**
Add each time series value to the previous to create a cumulative value.
The original time series is modified.
The parameters provided in the constructor control the analysis.
*/
public void cumulate ()
throws Exception
{   String routine = getClass().getSimpleName() + ".cumulate";
    TS ts = getTimeSeries();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisEnd = getAnalysisEnd();
    CumulateMissingType handleMissingHow = getHandleMissingHow();
    Double resetValue = getResetValue();
    Integer allowMissingCount = getAllowMissingCount();
    Integer minimumSampleSize = getMinimumSampleSize();

    // Get valid dates because the ones passed in may have been null.
    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, analysisStart, analysisEnd );
    DateTime start = valid_dates.getDate1();
    DateTime end = valid_dates.getDate2();

    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();

    DateTime Reset_DateTime = null; // Reset date/time - will be modified during processing below.
    DateTime Reset_DateTime_Prev = null; // Used to make sure full years of missing don't have 0 at start.
    DateTime Reset_DateTime_Next = null; // Used with irregular interval to check that reset has occurred in a year.
    if ( getResetDateTime() != null ) {
        Reset_DateTime = new DateTime ( getResetDateTime() );
        // Next reset is important for irregular because can't exactly match.
        Reset_DateTime_Next = new DateTime ( Reset_DateTime );
    }
    boolean resetValueToDataValue = getResetValueToDataValue();

    double total = ts.getMissing();
    int countNonmissing = 0; // Count of values added - used with reset.
    int countMissing = 0; // Count of missing values (before carry forward) - used with reset.
    boolean isMissing = false;
    double oldvalue;
    // TODO SAM 2012-07-25 Evaluate level of support for irregular time series.
    if ( interval_base == TimeInterval.IRREGULAR ) {
        // Get the data and loop through the list.
        IrregularTS irrts = (IrregularTS)ts;
        List<TSData> alltsdata = irrts.getData();
        if ( alltsdata == null ) {
            // No data for the time series.
            return;
        }
        int nalltsdata = alltsdata.size();

        // Unlike regular interval time series, the missing count does not apply because irregular data may
        // not occur on a regular interval.

        TSData tsdata = null;
        DateTime date = null;
        // Used to speed up date/time checks.
        boolean startFound = false;
        // List of reset data points:
        // - add during processing and add at the end so as to not interfere
        //   with the iterator, which might cause logic problems and ConcurrentModificationException
        List<TSData> insertResetPoints = new ArrayList<>();
        boolean didReset = false;
        // Use to handle initialization for irregular time series processing.
        boolean firstTime = true;
        // Has the reset occurred in the current year?
        boolean currentYearDidReset = false;
        for ( int i = 0; i < nalltsdata; i++ ) {
        	didReset = false;
            tsdata = alltsdata.get(i);
            date = tsdata.getDate();
            
            if ( date.greaterThan(end) ) {
                // Past the end of where we want to go so quit.
                break;
            }
            if ( !startFound ) {
            	if ( date.greaterThanOrEqualTo(start) ) {
            		startFound = true;
            	}
            	else {
            		continue;
            	}
            }
            
            if ( firstTime ) {
            	// First data point being processed in the requested period.
            	if ( Reset_DateTime != null ) {
            		// Processing resets:
            		// - initialize the next reset so that code below has something to check to
            		//   detect a new year
            		// First check whether the first value being processed is at the reset.
            		Reset_DateTime_Next.setYear(date.getYear());
            		if ( date.equals(Reset_DateTime_Next) ) {
            			// First data point is exactly on the reset date/time.
            			// Nothing to do.  Reset is handled below.
            		}
            		else if ( date.greaterThan(Reset_DateTime_Next) ) {
            			// Current date/time is not on a reset and is in the first year so
            			// the next reset will be next year.
            			Reset_DateTime_Next.setYear(date.getYear() + 1);
            		}
            		else {
            			// Should not need to do anything because a partial year will
            			// be processed at the front and the above cases should always work.

            			// TODO smalers 2022-10-27 there is an edge case where for IrregSecond
            			// or smaller precision a sub-second value prior to the reset may be added
            			// to the previous year end value because comparison of the time rounds to zero.
            			// Fixing this will require some careful logic.
            		}
            	}
            	firstTime = false;
            }
            
            // If here have data past the start that can be processed.

            oldvalue = tsdata.getDataValue();
            isMissing = ts.isDataMissing(oldvalue);
            if ( !isMissing ) {
                // Not missing.  Add to total and set value.
                if ( ts.isDataMissing(total) ) {
                	// Total is missing so initialize to the time series value.
                    total = oldvalue;
                }
                else {
                	// Total has been initialized so add to it.
                    total += oldvalue;
                }
                tsdata.setDataValue(total);
                // Have to do this manually since TSData are being modified directly to improve performance.
                ts.setDirty ( true );
            }
            else {
            	// Current value is missing.
            	if ( handleMissingHow == CumulateMissingType.CARRY_FORWARD ) {
            		// Missing but want to carry forward previous total.
            		tsdata.setDataValue(total);
            		// Have to do this manually since TSData are being modified directly to improve performance.
            		ts.setDirty ( true );
            	}
            	else {
            		// Else, missing and don't want to carry forward so leave as is.
            	}
            }

           	// Check to determine whether the cumulative value needs to be reset to zero.
           	// Note that this will throw away the previous value that was at this point.

            if ( Reset_DateTime != null ) {
                // Set the reset year to the current year to allow for comparison.
                Reset_DateTime.setYear(date.getYear());
                if ( Reset_DateTime.equals(date) ) {
                	// Exact match for end of year reset:
                	// - the date/time comparison is done at the precision of the time series and works in most
                	//   cases, other than sub-second intervals where a data point may occur between midnight and 
                	//   the first interval timestamp
                	// - this is similar to the regular interval code
                	// - no need to insert a new data value at the reset date/time because a data point exists

                    // Check to see if the previous year needs to be set to missing,
                	// which will be the case if not enough data were available to make the cumulative values credible.
                	// For irregular time series, minimumSampleSize may control but it is possible for the
                	// data values to have special missing value.
                    checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                        countNonmissing, countMissing, allowMissingCount, minimumSampleSize );

                	if ( isMissing && this.__insertResetPoint ) {
                		// Value is missing and want to reset to zero.
                		total = 0.0;
                	}
                	else {
                		// Missing or non-missing.
                		if ( resetValueToDataValue ) {
                			// Reset the total to the original time series value (might be missing).
                			total = oldvalue;
                		}
                		else {
                			// Reset to the specific reset value:
                			// - TODO smalers 2022-10-27 this discards a value and may be phased out
                			total = oldvalue;
                			total = resetValue;
                		}
                	}

                    ts.setDataValue(date,total);
                    
                    // Save as the previous reset date/time in case it needs to be used to set a whole year to missing.
                    Reset_DateTime_Prev = new DateTime(Reset_DateTime);
                    
                    // Indicate that a reset occurred.
                    didReset = true;
                    currentYearDidReset = true;
                }
                else if ( date.greaterThan(Reset_DateTime_Next) ) {
                	// Current date/time is beyond the reset for the next year so need to reset.
                	// - for irregular may have skipped a year

                    // Check to see if the previous year needs to be set to missing,
                	// which will be the case if not enough data were available to make the cumulative values credible.
                	// For irregular time series only the minimumSampleSize is checked.
                    checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                        countNonmissing, countMissing, allowMissingCount, minimumSampleSize );
                	
                	// If requested, insert a reset data point with zero value:
                	// - because loop is going over the original TSData list and don't want to interfere with that,
                	//   keep a list of points and add at the end
                	if ( this.__insertResetPoint ) {
                		total = 0.0;
                		TSData newPoint = new TSData(Reset_DateTime_Next, total, "RESET");
                		insertResetPoints.add(newPoint);
                	}
                	
                	// Set the cumulative value at the data point.
                    if ( resetValueToDataValue ) {
               			// Reset the total to the original time series value (might be missing).
                        total = oldvalue;
                    }
                    else {
                        // Reset to the specific reset value:
               			// - TODO smalers 2022-10-27 this discards a value and may be phased out
                        total = resetValue;
                    }

                    // Also set the data value at the measurement.
                    ts.setDataValue(date,oldvalue);

                    // Indicate that a reset occurred.
                    didReset = true;
                    currentYearDidReset = true;
                }
                else {
                	// A normal data point handled at the start of the loop so no special handling needed.
                }
                
                if ( didReset ) {
                	// A reset occurred:
                	// - advance for the next reset
                	Reset_DateTime_Next.setYear(date.getYear() + 1);

                    // Save as the previous reset date/time in case it needs to be used to set a whole year to missing.
                    Reset_DateTime_Prev = new DateTime(Reset_DateTime);
                	
                }
            }
            
            if ( didReset ) {
              	// Reset the missing data counters.
                if ( isMissing ) {
                 	countNonmissing = 0;
                   	countMissing = 1;
                }
                else {
                   	countNonmissing = 1;
                   	countMissing = 0;
                }
            }
            else {
                if ( isMissing ) {
                	++countMissing;
                }
                else {
                	++countNonmissing;
                }
            }
        }

        // Add the inserted reset points, if any.
        for ( TSData insertPoint : insertResetPoints ) {
        	ts.setDataValue(insertPoint.getDate(), insertPoint.getDataValue(), insertPoint.getDataFlag(), insertPoint.getDuration());
        }
    }
    else {
    	// Regular interval time series:
        // - loop using addInterval

        // If using Reset, set the missing count at the start to reflect a start date that is a partial year.
        if ( Reset_DateTime != null ) {
            countMissing = calculateStartingCountMissing ( start, Reset_DateTime, ts );
            Message.printStatus(2,routine,"Missing count for start of first year = " + countMissing );
        }

        // Whether a reset has occurred for the data point.
        boolean didReset = false;
        for ( DateTime date = new DateTime ( start ); date.lessThanOrEqualTo( end );
            date.addInterval(interval_base, interval_mult) ) {
            oldvalue = ts.getDataValue(date);
            isMissing = ts.isDataMissing(oldvalue);
            didReset = false;
            if ( !isMissing ) {
                // Not missing.  Add to total and set value.
                if ( ts.isDataMissing(total) ) {
                    total = oldvalue;
                }
                else {
                    total += oldvalue;
                }
                ts.setDataValue(date,total);
            }
            else {
            	// Current value is missing.
                if ( handleMissingHow == CumulateMissingType.CARRY_FORWARD ) {
                    // Missing but want to carry forward previous total.
                    ts.setDataValue(date,total);
                }
            	else {
            		// Missing and don't want to carry forward so leave as is.
            	}
            }
            
            // Check to determine whether the cumulative value needs to be reset at the start of a year.
            // This will throw away the previous value that was at this point.

            if ( Reset_DateTime != null ) {
                // Set the reset year to the current year to allow for comparison.
                Reset_DateTime.setYear(date.getYear());
                if ( Reset_DateTime.equals(date) ) {
                	// The reset date/time is found.
                	// This should be the first date/time at (ending) the date/time.
                	didReset = true;

                    // Check to see if the previous year needs to be set to missing,
                	// which will be the case if not enough data were available to make the cumulative values credible.
                    checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                        countNonmissing, countMissing, allowMissingCount, minimumSampleSize );

                	if ( isMissing && this.__insertResetPoint ) {
                		// Value is missing and want to reset to zero.
                		total = 0.0;
                	}
                	else {
                		// Missing or non-missing.
                		if ( resetValueToDataValue ) {
                			// Reset the total to the original time series value (might be missing).
                			total = oldvalue;
                		}
                		else {
                			// Reset to the specific value:
                			// - TODO smalers 2022-10-27 this discards a value and may be phased out
                			total = resetValue;
                		}
                	}

                    ts.setDataValue(date,total);

                    // Save as the previous reset date/time in case it needs to be used to set a whole year to missing.
                    Reset_DateTime_Prev = new DateTime(Reset_DateTime);
                }
            }
            
            if ( didReset ) {
            	// Reset the counters based on the first point.
               	if ( isMissing ) {
               		countNonmissing = 0;
               		countMissing = 1;
               	}
               	else {
               		countNonmissing = 1;
               		countMissing = 0;
               	}
            }
            else {
            	// Data point did not result in a reset:
            	// - increment the counters
               	if ( isMissing ) {
               		++countMissing;
               	}
               	else {
               		++countNonmissing;
               	}
            }
        }
    }

    if ( interval_base != TimeInterval.IRREGULAR ) {
        // Need to check whether the last year, which may be a partial year,
    	// needs its first value set to missing if no data were processed, similar to logic above.
        if ( Reset_DateTime != null ) {
            // Check to see if the previous year needs to be set to missing,
        	// which will be the case if not enough data were available to make the cumulative values credible.
            checkForIncompleteYear ( ts, start, Reset_DateTime_Prev, Reset_DateTime,
                countNonmissing, countMissing, allowMissingCount, minimumSampleSize );
        }
    }

    // Fill in the genesis information.

    ts.addToGenesis ( "Cumulated " + start + " to " + end + "." );
    ts.setDescription ( ts.getDescription() + ", cumulative" );
}

/**
Return the number of missing values allowed in sample.
@return the number of missing values allowed in sample.
*/
public Integer getAllowMissingCount () {
    return __allowMissingCount;
}

/**
Return the analysis end date/time.
@return the analysis end date/time.
*/
public DateTime getAnalysisEnd () {
    return __analysisEnd;
}

/**
Return the analysis start date/time.
@return the analysis start date/time.
*/
public DateTime getAnalysisStart () {
    return __analysisStart;
}

/**
Return whether to insert a zero data point at the reset date/time
if irregular time series value is after in a year.
@return whether to insert a zero data point at the reset date/time
*/
public boolean getInsertResetPoint () {
    return __insertResetPoint;
}

/**
Return how to handle missing values.
@return how to handle missing values.
*/
public CumulateMissingType getHandleMissingHow () {
    return __handleMissingHow;
}

/**
Return the minimum sample size allowed to compute the statistic.
@return the minimum sample size allowed to compute the statistic.
*/
public Integer getMinimumSampleSize () {
    return __minimumSampleSize;
}

/**
Return the reset date/time.
@return the reset date/time.
*/
public DateTime getResetDateTime () {
    return __resetDateTime;
}

/**
Return the reset value.
@return the reset value.
*/
public Double getResetValue () {
    return __resetValue;
}

/**
Return whether to reset the value to the original time series value.
@return whether to reset the value to the original time series value.
*/
public boolean getResetValueToDataValue () {
    return __resetValueToDataValue;
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries () {
    return __ts;
}

}