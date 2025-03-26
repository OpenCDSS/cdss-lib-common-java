// TSUtil_CalculateTimeSeriesStatistic - check time series values.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Math.MathUtil;
import RTi.Util.Math.Regression;
import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeWindow;

/**
Check time series values.
*/
public class TSUtil_CalculateTimeSeriesStatistic
{

/**
Time series to process.
*/
private TS __ts = null;

/**
Statistic to calculate.
*/
private TSStatisticType __statisticType = null;

/**
Statistic result that was calculated, a Double for floating-point statistics like Mean,
or an Integer for integer statistics like Count.
*/
private Object __statisticResult = null;

/**
Date/time for statistic result that was calculated (e.g., date/time for max value).
*/
private DateTime __statisticResultDateTime = null;

/**
Whether the statistic result has a date/time, for evaluation of single value statistic.
*/
private boolean __statisticResultHasDateTime = false;

/**
Start of analysis (null to analyze all).
*/
private DateTime __analysisStart = null;

/**
End of analysis (null to analyze all).
*/
private DateTime __analysisEnd = null;

/**
Starting date/time for analysis window, within a year.
*/
private DateTime __analysisWindowStart = null;

/**
Ending date/time for analysis window, within a year.
*/
private DateTime __analysisWindowEnd = null;

/**
Value as input to analysis, depending on checkType.
*/
private Double __value1 = null;

/**
Start of analysis (null to analyze all).
*/
private Double __value2 = null;

/**
Start of analysis (null to analyze all).
*/
private Double __value3 = null;

/**
Constructor.
@param ts time series to analyze
@param statistic statistic to calculate
@param analysisStart Starting date/time for analysis, in precision of the original data.
@param analysisEnd Ending date for analysis, in precision of the original data.
@param analysisWindowStart Starting date/time (year is ignored) for analysis within the year,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param analysisWindowEnd Ending date (year is ignored) for analysis within the year,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param value1 first additional value needed as input to compute the statistic
@param value2 second additional value needed as input to compute the statistic
@param value3 third additional value needed as input to compute the statistic
*/
public TSUtil_CalculateTimeSeriesStatistic ( TS ts, TSStatisticType statistic,
    DateTime analysisStart, DateTime analysisEnd, DateTime analysisWindowStart, DateTime analysisWindowEnd,
    Double value1, Double value2, Double value3 ) {
    // Save data members.
    __ts = ts;
    __statisticType = statistic;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __analysisWindowStart = analysisWindowStart;
    __analysisWindowEnd = analysisWindowEnd;
    __value1 = value1;
    __value2 = value2;
    __value3 = value3;
}

/**
Calculate the deficit/surplus sequence statistics (where statistics are based on a sequence of consecutive values).
A deficit is when a value is below the mean.  A surplus is when a a value is above the mean.
The length statistics indicates the number of intervals where each interval has a deficit (or surplus) value.
@param statisticType one of the DEFICIT_SEQ* and SURPLUS_SEQ* statistics
@param ts time series to analyze
@param start starting date/time for analysis period
@param end ending date/time for analysis period
@return the computed statistic value
*/
private void calculateDeficitSurplusSeqStatistic(TSStatisticType statisticType, TS ts, DateTime start, DateTime end )
throws Exception {
   String routine = getClass().getSimpleName() + ".calculateDeficitSurplusStatistic";
    // First compute the mean of the data, needed to evaluate the statistic.
    double [] values = TSUtil.toArrayNoMissing ( ts, start, end );
    double mean = MathUtil.mean(values);

    boolean checkSurplus = true; // Default when computing surplus statistics.
    if ( (statisticType == TSStatisticType.DEFICIT_SEQ_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MIN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MIN) ) {
        checkSurplus = false; // Check for deficit.
    }

    // Now loop through the period and analyze.
    int tsBase = ts.getDataIntervalBase();
    int tsMult = ts.getDataIntervalMult();
    double value; // Time series value.
    boolean isMissing; // Whether "value" is missing.
    double conditionTotal = 0.0; // Total of deficit or surplus.
    double conditionTotalSum = 0.0; // Sum of conditionTotal, for computing mean.
    boolean inCondition = false; // Current time-step is in required condition, based on checkSurplus.
    int inConditionCount = 0; // Number of time-steps in required condition.
    int inConditionCountSum = 0; // Sum of inConditionCount, for computing mean length.
    int inConditionInstanceCount = 0; // Number of times in condition.
    boolean inConditionPrev = false; // Whether previous time-step was in condition.
    double statistic = -999.0; // Value of statistic being computed (if never reset, will return null).
    DateTime statisticDate = null; // Time-step at start of condition.
    for ( DateTime date = start; date.lessThanOrEqualTo(end); date.addInterval(tsBase,tsMult) ) {
        // Get the data value and check for missing.
        value = ts.getDataValue ( date );
        if ( ts.isDataMissing(value) ) {
            isMissing = true;
        }
        else {
            isMissing = false;
        }
        // Check the current value for surplus or deficit condition.
        if ( checkSurplus ) {
            // Checking for surplus.
            if ( isMissing || (value <= mean) ) {
                // Not in surplus condition.
                inCondition = false;
            }
            else {
                // In surplus condition.
                inCondition = true;
                ++inConditionCount;
                conditionTotal += (value - mean);
            }
        }
        else {
            // Checking for deficit.
            if ( isMissing || (value >= mean) ) {
                // Not in deficit condition.
                inCondition = false;
            }
            else {
                // In deficit condition.
                inCondition = true;
                ++inConditionCount;
                conditionTotal += (mean - value);
            }
        }
        Message.printStatus ( 2, routine, "Value="+ value + " inCondition=" + inCondition);
        // If not in the condition, check the previous condition and to see if at the end of
        // processing for in condition sequence - in these cases, update the statistic if necessary
        // (means are computed after the loop).
        if ( (!inCondition && inConditionPrev) || (inCondition && date.equals(end) ) ) {
            // Need to complete processing the condition instance.
            if ( (statisticType == TSStatisticType.DEFICIT_SEQ_MAX) ||
                (statisticType == TSStatisticType.SURPLUS_SEQ_MAX) ) {
                if ( conditionTotal > statistic ) {
                    statistic = conditionTotal;
                    statisticDate = new DateTime(date);
                }
            }
            else if ( (statisticType == TSStatisticType.DEFICIT_SEQ_MIN) ||
                (statisticType == TSStatisticType.SURPLUS_SEQ_MIN) ) {
                if ( (statistic < 0.0) || (conditionTotal < statistic) ) {
                    statistic = conditionTotal;
                    statisticDate = new DateTime(date);
                }
            }
            else if ( (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MAX) ||
                (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MAX) ) {
                if ( inConditionCount > statistic ) {
                    statistic = inConditionCount;
                    statisticDate = new DateTime(date);
                }
            }
            else if ( (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MIN) ||
                (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MIN) ) {
                if ( (statistic < 0.0) || (inConditionCount < statistic) ) {
                    statistic = inConditionCount;
                    statisticDate = new DateTime(date);
                }
            }
            Message.printStatus ( 2, routine, "Statistic="+ statistic + " at=" + statisticDate);
            // Add to the totals.
            conditionTotalSum += conditionTotal;
            inConditionCountSum += inConditionCount;
            ++inConditionInstanceCount;
            // Reset for next condition.
            conditionTotal = 0.0;
            inConditionCount = 0;
        }
        // Now save the condition for the next loop iteration.
        inConditionPrev = inCondition;
    }
    Message.printStatus ( 2, routine, "conditionTotalSum="+ conditionTotalSum +
        " inConditionCountSum=" + inConditionCountSum +
        " inConditionInstanceCount=" + inConditionInstanceCount );
    // If the statistic is one of the means, compute it here.
    if ( (statisticType == TSStatisticType.DEFICIT_SEQ_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MEAN) ) {
        if ( inConditionInstanceCount > 0 ) {
            statistic = conditionTotalSum/inConditionInstanceCount;
        }
    }
    else if ( (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MEAN) ) {
        if ( inConditionInstanceCount > 0 ) {
            statistic = inConditionCountSum/inConditionInstanceCount;
        }
    }
    // A valid statistic should always be >= 0.
    if ( statistic >= 0.0 ) {
        Class<?> c = getStatisticDataClass();
        if ( c == Double.class ) {
            setStatisticResult ( Double.valueOf(statistic) );
        }
        else if ( c == Integer.class ) {
            setStatisticResult ( Integer.valueOf((int)(statistic + .01)) );
        }
        setStatisticResultDateTime ( statisticDate );
    }
}

/**
Calculate the deficit/surplus statistics.  A deficit is when a value is below the mean.
A surplus is when a a value is above the mean.
@param statisticType one of the DEFICIT* and SURPLUS* statistics (not the *SEQ* statistics).
@param ts time series to analyze
@param start starting date/time for analysis period
@param end ending date/time for analysis period
@return the computed statistic value
*/
private void calculateDeficitSurplusStatistic(TSStatisticType statisticType, TS ts, DateTime start, DateTime end )
throws Exception {
    //String routine = getClass().getName() + ".calculateDeficitSurplusStatistic";
    // First compute the mean of the data, needed to evaluate the statistic.
    double [] values = TSUtil.toArrayNoMissing ( ts, start, end );
    double mean = MathUtil.mean(values);

    boolean checkSurplus = true; // Default when computing surplus statistics.
    if ( (statisticType == TSStatisticType.DEFICIT_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_MIN) ) {
        checkSurplus = false; // Check for deficit.
    }

    // Loop through the values and determine if they are a surplus or deficit.
    double [] values2 = new double[values.length];
    int values2Count = 0; // Count of number of deficit or surplus values.
    for ( int i = 0; i < values.length; i++ ) {
        if ( checkSurplus && (values[i] > mean) ) {
            values2[values2Count++] = (values[i] - mean);
        }
        else if ( !checkSurplus && (values[i] < mean) ) {
            values2[values2Count++] = (mean - values[i]);
        }
        // Else 0 is not in either category.
    }

    if ( (statisticType == TSStatisticType.DEFICIT_MAX) || (statisticType == TSStatisticType.SURPLUS_MAX) ) {
        if ( values2Count > 0 ) {
            setStatisticResult ( MathUtil.max(values2Count, values2) );
        }
    }
    else if ( (statisticType == TSStatisticType.DEFICIT_MEAN) || (statisticType == TSStatisticType.SURPLUS_MEAN) ) {
        if ( values2Count > 0 ) {
            setStatisticResult ( MathUtil.mean(values2Count, values2) );
        }
    }
    else if ( (statisticType == TSStatisticType.DEFICIT_MIN) || (statisticType == TSStatisticType.SURPLUS_MIN) ) {
        if ( values2Count > 0 ) {
            setStatisticResult ( MathUtil.min(values2Count, values2) );
        }
    }
}

/**
Calculate the missing value sequence statistics (where statistics are based on a sequence of missing values).
@param statisticType one of the MISSING_SEQ_LENGTH* statistics
@param ts time series to analyze
@param start starting date/time for analysis period
@param end ending date/time for analysis period
@return the computed statistic value
*/
private void calculateMissingSeqStatistic(TSStatisticType statisticType, TS ts, DateTime start, DateTime end )
throws Exception {
    String routine = getClass().getSimpleName() + ".calculateMissingSeqStatistic";

    // Loop through the period and analyze.
    int tsBase = ts.getDataIntervalBase();
    int tsMult = ts.getDataIntervalMult();
    double value; // Time series value.
    boolean isMissing; // Whether "value" is missing.
    boolean inCondition = false; // Current time-step is in required condition, based on checkSurplus.
    int inConditionCount = 0; // Number of time-steps in required condition.
    // TODO smalers 2019-06-01 evaluate how needed.
    //int inConditionInstanceCount = 0; // Number of times in condition.
    boolean inConditionPrev = false; // Whether previous time-step was in condition.
    double statistic = 0.0; // Value of statistic being computed, zero is ok.
    DateTime statisticDate = null; // Time-step at start of condition, null if never have condition.
    for ( DateTime date = start; date.lessThanOrEqualTo(end); date.addInterval(tsBase,tsMult) ) {
        // Get the data value and check for missing.
        value = ts.getDataValue ( date );
        if ( ts.isDataMissing(value) ) {
            isMissing = true;
        }
        else {
            isMissing = false;
        }
        // Check the current value for missing.
        // Checking for surplus.
        if ( !isMissing ) {
            // Not in missing sequence.
            inCondition = false;
        }
        else {
            // In missing sequence.
            inCondition = true;
            ++inConditionCount;
        }
        //Message.printStatus ( 2, routine, "Value="+ value + " inCondition=" + inCondition);
        // If not in the condition, check the previous condition and to see if at the end of
        // processing for in condition sequence - in these cases, update the statistic if necessary
        // (means are computed after the loop).
        if ( (!inCondition && inConditionPrev) || (inCondition && date.equals(end) ) ) {
            // Need to complete processing the condition instance.
            if ( statisticType == TSStatisticType.MISSING_SEQ_LENGTH_MAX ) {
                if ( inConditionCount > statistic ) {
                    statistic = inConditionCount;
                    statisticDate = new DateTime(date);
                }
            }
            Message.printStatus ( 2, routine, "Statistic=" + statistic + " at=" + statisticDate);
            // Add to the totals.
            //++inConditionInstanceCount;
            // Reset for next condition.
            inConditionCount = 0;
        }
        // Now save the condition for the next loop iteration.
        inConditionPrev = inCondition;
    }
    // Statistic should always be >= 0.
    if ( statistic >= 0 ) {
        setStatisticResult ( Integer.valueOf((int)(statistic + .001)) );
        setStatisticResultDateTime ( statisticDate );
    }
}

/**
Check the time series.
*/
public void calculateTimeSeriesStatistic ()
throws Exception {
    TSStatisticType statisticType = getStatisticType();
    double value1 = (getValue1() == null) ? -999.0 : getValue1().doubleValue();
    double value2 = (getValue2() == null) ? -999.0 : getValue2().doubleValue();
    double value3 = (getValue3() == null) ? -999.0 : getValue3().doubleValue();
    TS ts = getTimeSeries();
    DateTime analysisEnd = getAnalysisEnd();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisWindowStart = getAnalysisWindowStart();
    DateTime analysisWindowEnd = getAnalysisWindowEnd();
    boolean haveAnalysisWindow = false;
    if ( (analysisWindowStart != null) && (analysisWindowEnd != null) ) {
        haveAnalysisWindow = true;
        // TODO SAM 2013-02-10 Throw exception below if analysis window is specified but is not supported.
    }
    // Initialize the results to null.
    setStatisticResult((Double)null);
	setStatisticResultDateTime(null);
    // If statistic takes more work, call other supporting code and then return.
    // Statistics computed (further below) also store the date/time corresponding to a statistic value,
    // whereas the ones immediately below don't utilize this information.
    if ( statisticType == TSStatisticType.NQYY ) {
        if ( !(ts instanceof DayTS) ) {
            throw new InvalidParameterException ( "Attempting to calculate statistic \"" + statisticType +
                "\" on other than daily time series (TSID=" + ts.getIdentifierString() + ")");
        }
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        NqYYFrequencyAnalysis nqyy = new NqYYFrequencyAnalysis ( (DayTS)ts, (int)(value1 + .01), value2,
            getAnalysisStart(), getAnalysisEnd(), (int)(value3 + .01) );
        nqyy.analyze();
        Double result = nqyy.getAnalysisResult();
        if ( result != null ) {
            setStatisticResult ( result );
        }
        return;
    }
    else if ( statisticType == TSStatisticType.LAG1_AUTO_CORRELATION ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double auto = MathUtil.lagAutoCorrelation(values.length,values,1);
        setStatisticResult ( Double.valueOf(auto) );
        return;
    }
    else if ( statisticType == TSStatisticType.TREND_OLS ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double [] dts = TSUtil.toArray ( ts, getAnalysisStart(), getAnalysisEnd(),
            null, // No month list.
            false, // Don't include missing.
            false, // Don't match paired time series non-missing.
            null, // No paired time series.
            TSToArrayReturnType.DATE_TIME);
        Regression regress = MathUtil.regress(dts, values,
            false, // Do not check for missing values (they are already removed).
            0.0, // Missing X, not used.
            0.0, // Missing Y, not used.
            null ); // No assigned Y intercept.
        setStatisticResult ( regress );
        return;
    }
    else if ( statisticType == TSStatisticType.LAST ) {
        // Get the last non-missing or missing value in the analysis period, considering the analysis window.
        if ( analysisEnd == null ) {
            analysisEnd = ts.getDate2();
        }
        if ( analysisStart == null ) {
            analysisStart = ts.getDate1();
        }
        DateTimeWindow win = null;
        if ( haveAnalysisWindow ) {
        	win = new DateTimeWindow(analysisWindowStart,analysisWindowEnd);
        	DateTime dt = win.getLastMatchingDateTime(analysisStart, analysisEnd, ts.getDataIntervalBase(), ts.getDataIntervalMult());
        	if ( dt != null ) {
        		setStatisticResult(ts.getDataValue(dt));
        		setStatisticResultDateTime(dt);
        		setStatisticResultHasDateTime(true);
        	}
        }
        else {
        	// Just use the last value in the analysis period.
    		setStatisticResult(ts.getDataValue(analysisEnd));
    		setStatisticResultDateTime(analysisEnd);
    		setStatisticResultHasDateTime(true);
        }
        return;
    }
    else if ( statisticType == TSStatisticType.LAST_NONMISSING ) {
        // Get the last non-missing value in the analysis period, considering the analysis window.
		setStatisticResultHasDateTime(true);
        if ( analysisEnd == null ) {
            analysisEnd = ts.getDate2();
        }
        if ( analysisStart == null ) {
            analysisStart = ts.getDate1();
        }
        DateTimeWindow win = null;
        if ( haveAnalysisWindow ) {
        	win = new DateTimeWindow(analysisWindowStart,analysisWindowEnd);
        }
        // Iterate backwards.
        TSIterator tsi = ts.iterator(analysisStart,analysisEnd);
        TSData tsdata = null;
        DateTime dt = null;
        double last;
        while ( (tsdata = tsi.previous()) != null ) {
        	dt = tsdata.getDate();
            if ( haveAnalysisWindow ) {
            	// Don't check unless in the analysis window.
            	if ( !win.isDateTimeInWindow(dt) ) {
            		continue;
            	}
            }
        	last = tsdata.getDataValue();
        	if ( !ts.isDataMissing(last) ) {
        		setStatisticResult(Double.valueOf(last));
        		setStatisticResultDateTime(dt);
        		break;
        	}
        }
        return;
    }
    else if ( statisticType == TSStatisticType.MEAN ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double mean = MathUtil.mean(values);
        setStatisticResult ( Double.valueOf(mean) );
        return;
    }
    else if ( statisticType == TSStatisticType.SKEW ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double skew = MathUtil.skew(values.length, values);
        setStatisticResult ( Double.valueOf(skew) );
        return;
    }
    else if ( statisticType == TSStatisticType.STD_DEV ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double stdDev = MathUtil.standardDeviation(values);
        setStatisticResult ( Double.valueOf(stdDev) );
        return;
    }
    else if ( statisticType == TSStatisticType.TOTAL ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double sum = MathUtil.sum(values);
        setStatisticResult ( Double.valueOf(sum) );
        return;
    }
    else if ( statisticType == TSStatisticType.VARIANCE ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Convert all the values to an array and then call the math method.
        double [] values = TSUtil.toArrayNoMissing ( ts, getAnalysisStart(), getAnalysisEnd() );
        double var = MathUtil.variance(values);
        setStatisticResult ( Double.valueOf(var) );
        return;
    }
    else if ( (statisticType == TSStatisticType.DEFICIT_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_MIN) ||
        (statisticType == TSStatisticType.SURPLUS_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_MIN) ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Get period for the specific time series.
        TSLimits limits = TSUtil.getValidPeriod(ts, getAnalysisStart(), getAnalysisEnd());
        calculateDeficitSurplusStatistic(statisticType, ts, limits.getDate1(), limits.getDate2() );
        return;
    }
    else if ( (statisticType == TSStatisticType.DEFICIT_SEQ_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MIN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MIN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MIN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MIN) ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Get period for the specific time series.
        TSLimits limits = TSUtil.getValidPeriod(ts, getAnalysisStart(), getAnalysisEnd());
        calculateDeficitSurplusSeqStatistic(statisticType, ts, limits.getDate1(), limits.getDate2() );
        return;
    }
    else if ( statisticType == TSStatisticType.MISSING_SEQ_LENGTH_MAX ) {
        if ( haveAnalysisWindow ) {
            throw new InvalidParameterException ( "Analysis window is not supported for statistic \"" + statisticType );
        }
        // Get period for the specific time series.
        TSLimits limits = TSUtil.getValidPeriod(ts, getAnalysisStart(), getAnalysisEnd());
        calculateMissingSeqStatistic(statisticType, ts, limits.getDate1(), limits.getDate2() );
        return;
    }

    // Else, iterate through the time series and do the work below - typically "search" or accumulation
    // type statistics rather than analyzing a sample array in code supported above.
    TSIterator tsi = ts.iterator(getAnalysisStart(), getAnalysisEnd());
    TSData data = null;

    /*
    String tsid = ts.getIdentifier().toString();
    if ( (ts.getAlias() != null) && !ts.getAlias().equals("") ) {
        tsid = ts.getAlias();
    }
    */
    double tsvalue; // time series data value
    //double tsvaluePrev = 0; // time series data value (previous) - will be used for change in value
    DateTime date; // Date corresponding to data value
    boolean isMissing;
    int countNotMissing = 0;
    int countMissing = 0;
    int countStatistic = 0; // Statistic value for counts.
    //double diff;
    double statisticResult = ts.getMissing();
    DateTime statisticResultDateTime = null;
    boolean statisticCalculated = false;
    DateTime analysisWindowStartForIterator = null;
    DateTime analysisWindowEndForIterator = null;
    // Set whether a date/time corresponds to the result.
    if ( (statisticType == TSStatisticType.MAX) || (statisticType == TSStatisticType.MIN) ) {
		setStatisticResultHasDateTime(true);
    }
    while ( (data = tsi.next()) != null ) {
        // Analyze the value.
        date = tsi.getDate();
        if ( haveAnalysisWindow ) {
            // If the iterator window date/times are null, initialize to get precision, etc.
            if ( analysisWindowStartForIterator == null ) {
                analysisWindowStartForIterator = new DateTime(analysisWindowStart);
                analysisWindowStartForIterator.setPrecision(date.getPrecision());
                analysisWindowEndForIterator = new DateTime(analysisWindowEnd);
                analysisWindowEndForIterator.setPrecision(date.getPrecision());
            }
            // Always set the year since the other parts will be initialized.
            analysisWindowStartForIterator.setYear(date.getYear());
            analysisWindowEndForIterator.setYear(date.getYear());
            // Check to make sure the date/time is in the analysis window.
            if ( date.lessThan(analysisWindowStartForIterator) || date.greaterThan(analysisWindowEndForIterator) ) {
                continue;
            }
        }
        tsvalue = data.getDataValue();
        isMissing = ts.isDataMissing(tsvalue);
        if ( isMissing ) {
            ++countMissing;
        }
        else {
            ++countNotMissing;
        }
        if ( statisticType == TSStatisticType.GE_COUNT ) {
            if ( !isMissing ) {
                if ( tsvalue >= value1 ) {
                    ++countStatistic;
                    statisticCalculated = true;
                }
            }
        }
        else if ( statisticType == TSStatisticType.GT_COUNT ) {
            if ( !isMissing ) {
                if ( tsvalue > value1 ) {
                    ++countStatistic;
                    statisticCalculated = true;
                }
            }
        }
        else if ( statisticType == TSStatisticType.LE_COUNT ) {
            if ( !isMissing ) {
                if ( tsvalue <= value1 ) {
                    ++countStatistic;
                    statisticCalculated = true;
                }
            }
        }
        else if ( statisticType == TSStatisticType.LT_COUNT ) {
            if ( !isMissing ) {
                if ( tsvalue < value1 ) {
                    ++countStatistic;
                    statisticCalculated = true;
                }
            }
        }
        else if ( statisticType == TSStatisticType.MAX ) {
            if ( !isMissing ) {
                if ( !statisticCalculated || (tsvalue > statisticResult)) {
                    statisticResult = tsvalue;
                    statisticResultDateTime = new DateTime(date);
                    statisticCalculated = true;
                }
            }
        }
        else if ( statisticType == TSStatisticType.MIN ) {
            if ( !isMissing ) {
                if ( !statisticCalculated || (tsvalue < statisticResult)) {
                    statisticResult = tsvalue;
                    statisticResultDateTime = new DateTime(date);
                    statisticCalculated = true;
                }
            }
        }
        //tsvaluePrev = tsvalue;
    }
    // Set the final statistic value.
    // Handle counts differently to improve performance.
    if ( statisticType == TSStatisticType.COUNT ) {
        setStatisticResult ( countNotMissing + countMissing );
    }
    else if ( (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LT_COUNT) ) {
        setStatisticResult ( countStatistic );
    }
    else if ( statisticType == TSStatisticType.MISSING_COUNT ) {
        setStatisticResult ( countMissing );
    }
    else if ( statisticType == TSStatisticType.MISSING_PERCENT ) {
        int countTotal = countNotMissing + countMissing;
        if ( countTotal != 0 ) {
            setStatisticResult ( Double.valueOf(100.0*countMissing/(double)countTotal) );
        }
    }
    else if ( statisticType == TSStatisticType.NONMISSING_COUNT ) {
        setStatisticResult ( countNotMissing );
    }
    else if ( statisticType == TSStatisticType.NONMISSING_PERCENT ) {
        int countTotal = countNotMissing + countMissing;
        if ( countTotal != 0 ) {
            setStatisticResult ( Double.valueOf(100.0*countNotMissing/(double)countTotal) );
        }
    }
    else if ( statisticCalculated ) {
        // All other statistics - numerical values - just set from values computed locally above
    	// The date may be null and whether a date is used was set above.
        setStatisticResult ( statisticResult );
        setStatisticResultDateTime ( statisticResultDateTime );
    }
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
Return the analysis window end date/time.
@return the analysis window end date/time.
*/
private DateTime getAnalysisWindowEnd () {
    return __analysisWindowEnd;
}

/**
Return the analysis window start date/time.
@return the analysis window start date/time.
*/
private DateTime getAnalysisWindowStart () {
    return __analysisWindowStart;
}

/**
Return the number of values that are required to evaluate a statistic.
@return the number of values that are required to evaluate a statistic.
@param statisticType the statistic type that is being evaluated.
*/
public static int getRequiredNumberOfValuesForStatistic ( TSStatisticType statisticType ) {
    // Many basic statistics do not need additional input.
    if ( (statisticType == TSStatisticType.COUNT) ||
        (statisticType == TSStatisticType.DEFICIT_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_MIN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_LENGTH_MIN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MAX) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MEAN) ||
        (statisticType == TSStatisticType.DEFICIT_SEQ_MIN) ||
        (statisticType == TSStatisticType.LAG1_AUTO_CORRELATION) ||
        (statisticType == TSStatisticType.LAST) ||
        (statisticType == TSStatisticType.LAST_NONMISSING) ||
        (statisticType == TSStatisticType.MAX) ||
        (statisticType == TSStatisticType.MEAN) ||
        (statisticType == TSStatisticType.MIN) ||
        (statisticType == TSStatisticType.MISSING_COUNT) ||
        (statisticType == TSStatisticType.MISSING_PERCENT) ||
        (statisticType == TSStatisticType.MISSING_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.NONMISSING_COUNT) ||
        (statisticType == TSStatisticType.NONMISSING_PERCENT) ||
        (statisticType == TSStatisticType.SKEW) ||
        (statisticType == TSStatisticType.STD_DEV) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_LENGTH_MIN) ||
        (statisticType == TSStatisticType.SURPLUS_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_MIN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MAX) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MEAN) ||
        (statisticType == TSStatisticType.SURPLUS_SEQ_MIN) ||
        (statisticType == TSStatisticType.TOTAL) ||
        (statisticType == TSStatisticType.TREND_OLS) ||
        (statisticType == TSStatisticType.VARIANCE) ) {
        return 0;
    }
    else if ( (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LT_COUNT) ) {
        return 1;
    }
    // The following statistics need additional input.
    else if ( statisticType == TSStatisticType.NQYY ) {
        // Need days to average (N), return frequency (YY), and allowed missing in average.
        return 3;
    }
    else {
        String message = "Requested statistic is not recognized: " + statisticType;
        String routine = "TSUtil_CalculateTimeSeriesStatistic.getRequiredNumberOfValuesForStatistic";
        Message.printWarning(3, routine, message);
        throw new InvalidParameterException ( message );
    }
}

/**
Get the list of statistics that can be performed.
*/
public static List<TSStatisticType> getStatisticChoices() {
    List<TSStatisticType> choices = new ArrayList<>();
    choices.add ( TSStatisticType.COUNT );
    choices.add ( TSStatisticType.DEFICIT_MAX );
    choices.add ( TSStatisticType.DEFICIT_MEAN );
    choices.add ( TSStatisticType.DEFICIT_MIN );
    choices.add ( TSStatisticType.DEFICIT_SEQ_LENGTH_MAX );
    choices.add ( TSStatisticType.DEFICIT_SEQ_LENGTH_MEAN );
    choices.add ( TSStatisticType.DEFICIT_SEQ_LENGTH_MIN );
    choices.add ( TSStatisticType.DEFICIT_SEQ_MAX );
    choices.add ( TSStatisticType.DEFICIT_SEQ_MEAN );
    choices.add ( TSStatisticType.DEFICIT_SEQ_MIN );
    choices.add ( TSStatisticType.GE_COUNT );
    choices.add ( TSStatisticType.GT_COUNT );
    choices.add ( TSStatisticType.LAG1_AUTO_CORRELATION );
    choices.add ( TSStatisticType.LAST );
    choices.add ( TSStatisticType.LAST_NONMISSING );
    choices.add ( TSStatisticType.LE_COUNT );
    choices.add ( TSStatisticType.LT_COUNT );
    choices.add ( TSStatisticType.MAX );
    choices.add ( TSStatisticType.MEAN );
    choices.add ( TSStatisticType.MIN );
    choices.add ( TSStatisticType.MISSING_COUNT );
    choices.add ( TSStatisticType.MISSING_PERCENT );
    choices.add ( TSStatisticType.MISSING_SEQ_LENGTH_MAX );
    choices.add ( TSStatisticType.NONMISSING_COUNT );
    choices.add ( TSStatisticType.NONMISSING_PERCENT );
    choices.add ( TSStatisticType.NQYY );
    choices.add ( TSStatisticType.SKEW );
    choices.add ( TSStatisticType.STD_DEV );
    choices.add ( TSStatisticType.SURPLUS_MAX );
    choices.add ( TSStatisticType.SURPLUS_MEAN );
    choices.add ( TSStatisticType.SURPLUS_MIN );
    choices.add ( TSStatisticType.SURPLUS_SEQ_LENGTH_MAX );
    choices.add ( TSStatisticType.SURPLUS_SEQ_LENGTH_MEAN );
    choices.add ( TSStatisticType.SURPLUS_SEQ_LENGTH_MIN );
    choices.add ( TSStatisticType.SURPLUS_SEQ_MAX );
    choices.add ( TSStatisticType.SURPLUS_SEQ_MEAN );
    choices.add ( TSStatisticType.SURPLUS_SEQ_MIN );
    choices.add ( TSStatisticType.TOTAL );
    choices.add ( TSStatisticType.TREND_OLS );
    choices.add ( TSStatisticType.VARIANCE );
    return choices;
}

/**
Get the list of statistics that can be performed.
@return the statistic display names as strings.
*/
public static List<String> getStatisticChoicesAsStrings() {
    List<TSStatisticType> choices = getStatisticChoices();
    List<String> stringChoices = new ArrayList<String>();
    for ( TSStatisticType choice : choices ) {
        stringChoices.add ( "" + choice );
    }
    return stringChoices;
}

/**
Return the statistic data class.  This is useful for data management and ensures that
the class is known even if the statistic is null or NaN.
*/
public Class<?> getStatisticDataClass () {
    TSStatisticType t = getStatisticType();
    // Most are Double so check for integers and DateTime.
    // Note that some of the MEAN statistics are computed from integers but the final statistic is a double
    // and therefore not included in the following integer list.
    if ( (t == TSStatisticType.COUNT) ||
        (t == TSStatisticType.DEFICIT_SEQ_LENGTH_MAX) ||
        (t == TSStatisticType.DEFICIT_SEQ_LENGTH_MIN) ||
        (t == TSStatisticType.GE_COUNT) ||
        (t == TSStatisticType.GT_COUNT) ||
        (t == TSStatisticType.LE_COUNT) ||
        (t == TSStatisticType.LT_COUNT) ||
        (t == TSStatisticType.MISSING_COUNT) ||
        (t == TSStatisticType.MISSING_SEQ_LENGTH_MAX) ||
        (t == TSStatisticType.NONMISSING_COUNT) ||
        (t == TSStatisticType.SURPLUS_SEQ_LENGTH_MAX) ||
        (t == TSStatisticType.SURPLUS_SEQ_LENGTH_MIN) ) {
        return Integer.class;
    }
    else if ( t == TSStatisticType.TREND_OLS ) {
        return Regression.class;
    }
    else {
        return Double.class;
    }
}

/**
Return the value of the statistic that was calculated.
@return the value of the statistic that was calculated, as a Double for floating point statistics and
Integer for integer statistics.
*/
public Object getStatisticResult() {
    return __statisticResult;
}

/**
Return the date/time for the statistic that was calculated.
@return the date/time for the statistic that was calculated.
*/
public DateTime getStatisticResultDateTime() {
    return __statisticResultDateTime;
}

/**
Return whether the statistic has a date/time result in addition to statistic.
This will be true .
*/
public boolean getStatisticResultHasDateTime() {
	return __statisticResultHasDateTime;
}

/**
Return the name of the statistic being calculated.
@return the name of the statistic being calculated.
*/
public TSStatisticType getStatisticType () {
    return __statisticType;
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries () {
    return __ts;
}

/**
Return Value1 for the check.
@return Value1 for the check.
*/
public Double getValue1 () {
    return __value1;
}

/**
Return Value2 for the check.
@return Value2 for the check.
*/
public Double getValue2 () {
    return __value2;
}

/**
Return Value3 for the check.
@return Value3 for the check.
*/
public Double getValue3 () {
    return __value3;
}

/**
Set the statistic result.  Use this when setting to null.
*/
private void setStatisticResult ( Double statisticResult ) {
    __statisticResult = statisticResult;
}

/**
Set the statistic result.
*/
private void setStatisticResult ( double statisticResult ) {
    __statisticResult = Double.valueOf(statisticResult);
}

/**
Set the statistic result, for integer statistics like count.
*/
private void setStatisticResult ( int statisticResult ) {
    __statisticResult = Integer.valueOf(statisticResult);
}

/**
Set the statistic result for regression analysis.
*/
private void setStatisticResult ( Regression statisticResult ) {
    __statisticResult = statisticResult;
}

/**
Set the date/time for statistic result.  A copy is made because the instance may be changing during iteration.
*/
private void setStatisticResultDateTime ( DateTime statisticResultDateTime ) {
	if ( statisticResultDateTime != null ) {
		statisticResultDateTime = new DateTime(statisticResultDateTime);
	}
    __statisticResultDateTime = statisticResultDateTime;
}

/**
Set whether the statistic has a date/time result in addition to statistic.
*/
private void setStatisticResultHasDateTime(boolean statisticResultHasDateTime) {
	__statisticResultHasDateTime = statisticResultHasDateTime;
}

}