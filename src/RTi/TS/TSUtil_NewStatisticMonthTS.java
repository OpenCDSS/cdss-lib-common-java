package RTi.TS;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeScaleType;
import RTi.Util.Time.TimeUtil;

/**
Compute a MonthTS that has a statistic for each month in the period.
*/
public class TSUtil_NewStatisticMonthTS
{
    
/**
Enumerations used when processing the statistic.
*/
private enum TestType {
    GE, // Test >=
    GT, // Test >
    LE, // Test <=
    LT, // Test <
    NOT_USED }; // Unknown test
    
/**
Time series to analyze.
*/
private TS __ts = null;

/**
New time series identifier to assign.
*/
private String __newTSID = null;

/**
Starting date/time for analysis.
*/
private DateTime __analysisStart = null;

/**
Ending date/time for analysis.
*/
private DateTime __analysisEnd = null;

/**
Statistic to analyze.
*/
private TSStatisticType __statisticType = null;

/**
Test value used when analyzing the statistic.
*/
private Double __testValue = null;

/**
Number of missing allowed to compute sample (default is no limit).
*/
private int __allowMissingCount = -1;

/**
Minimum required sample size (default is no limit).
*/
private int __minimumSampleSize = -1;

/**
Starting date/time for analysis window, within a month.
*/
private DateTime __analysisWindowStart = null;

/**
Ending date/time for analysis window, within a month.
*/
private DateTime __analysisWindowEnd = null;

/**
Search start date/time for analysis window, within a month.
*/
private DateTime __searchStart = null;
    
/**
Construct the analysis object with required input.  Values will be checked for validity.
Execute the newStatisticMonthTS() method to perform the analysis.
@param ts time series to analyze
@param analysisStart Starting date/time for analysis, in precision of the original data.
@param analysisEnd Ending date for analysis, in precision of the original data.
@param newTSID the new time series identifier to be assigned to the time series.
@param statisticType the statistic type for the output time series.
@param testValue test value (e.g., threshold value) needed to process some statistics.
@param allowMissingCount the number of values allowed to be missing in the sample.
@param minimumSampleSize the minimum sample size to allow to compute the statistic.
@param analysisWindowStart Starting date/time (year and month are ignored) for analysis within the month,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param analysisWindowEnd Ending date (year and month are ignored) for analysis within the month,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param searchStart search start date (year and month are ignored) for analysis within the month,
in precision of the original data.  If null, the entire month of data will be analyzed.
This is used when a starting point is needed, such as when first and last values >, < in a month.
*/
public TSUtil_NewStatisticMonthTS ( TS ts, String newTSID, TSStatisticType statisticType, Double testValue,
    Integer allowMissingCount, Integer minimumSampleSize,
    DateTime analysisStart, DateTime analysisEnd,
    DateTime analysisWindowStart, DateTime analysisWindowEnd, DateTime searchStart )
{   String routine = getClass().getName();
    String message;
    
    if ( ts == null ) {
        // Nothing to do...
        message = "Null input time series - cannot calculate statistic time series.";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }
    // TODO SAM 2014-04-03 Enable for other than daily time series
    if ( ts.getDataIntervalBase() != TimeInterval.DAY ) {
        message = "Input time series must have an interval of day (support for additional intervals will be added in the future).";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }
    setTimeSeries ( ts );
    setNewTSID ( newTSID );
    try {
        if ( (newTSID != null) && (newTSID.length() > 0) ) {
            TSIdent tsident = new TSIdent ( newTSID );
            // Make sure that the output interval is Month
            if ( !tsident.getInterval().equalsIgnoreCase("Month") ) {
                throw new IllegalArgumentException (
                    "New time series identifier \"" + newTSID + "\" must have an interval of Month - " +
                    	"cannot calculate statistic time series." );
            }
        }
    }
    catch ( Exception e ) {
        throw new IllegalArgumentException (
            "New time series identifier \"" + newTSID + "\" is invalid (" + e + ")." );
    }
    setStatisticType ( statisticType );
    setTestValue ( testValue );
    if ( allowMissingCount == null ) {
        allowMissingCount = new Integer(-1); // default
    }
    setAllowMissingCount ( allowMissingCount.intValue() );
    if ( minimumSampleSize == null ) {
        minimumSampleSize = new Integer(-1); // default
    }
    setMinimumSampleSize ( minimumSampleSize );
    setAnalysisStart ( analysisStart );
    setAnalysisEnd ( analysisEnd );
    
    // FIXME SAM 2009-11-04 Need to make this check specific to the time series interval and time scale
    if ( !isStatisticSupported(statisticType, TimeInterval.UNKNOWN, null) ) {
        throw new InvalidParameterException ( "Statistic \"" + statisticType + "\" is not supported.");
    }
    
    setAnalysisWindowStart ( analysisWindowStart );
    setAnalysisWindowEnd ( analysisWindowEnd );
    setSearchStart ( searchStart );
}

/**
Process a time series to create a statistic from getStatisticChoices().
@param ts Time series to analyze.
@param monthts MonthTS to fill with the statistic.
@param statisticType statistic to calculate.
@param testValue a number to test against for some statistics (e.g., COUNT_LE).
@param analysisStart Start of the analysis (precision matching ts).
@param analysisEnd End of the analysis (precision matching ts).
@param allowMissingCount the number of missing values allowed in input and still compute the statistic.
@param minimumSampleSize the minimum number of values required in input to compute the statistic.
@param analysisWindowStart If not null, specify the start of the window within
the month for data, , for example an operational constraint.
Currently only Month... to precision are evaluated (not day... etc.).
@param analysisWindowEnd If not null, specify the end of the window within
the month for data, , for example an operational constraint.
Currently only Month... to precision are evaluated (not day... etc.).
@param searchStart starting date/time in the month to analyze, for example an operational constraint.
*/
private void calculateStatistic (
    TS ts, MonthTS monthts, TSStatisticType statisticType, Double testValue,
    DateTime analysisStart, DateTime analysisEnd, int allowMissingCount, int minimumSampleSize,
    DateTime analysisWindowStart, DateTime analysisWindowEnd, DateTime searchStart )
{   String routine = getClass().getSimpleName() + ".calculateStatistic";
    // Initialize the settings to evaluate the statistic and set appropriate information in the time series
    boolean statisticIsCount = isStatisticCount(statisticType);
    boolean statisticIsDayOf = isStatisticDayOf(statisticType);
    boolean statisticIsFirst = isStatisticFirst(statisticType);
    boolean statisticIsLast = isStatisticLast(statisticType);
    boolean iterateForward = isStatisticIterateForward(statisticType);
    TestType testType = getStatisticTestType(statisticType);
    monthts.setDescription ( getStatisticTimeSeriesDescription(statisticType, testType, testValue,
        statisticIsCount, statisticIsDayOf, statisticIsFirst, statisticIsLast ) );
    monthts.setDataUnits(getStatisticTimeSeriesDataUnits(statisticType, statisticIsCount, statisticIsDayOf,
        ts.getDataUnits()) );
    
    Message.printStatus ( 2, routine, "Overall analysis period is " + analysisStart + " to " + analysisEnd );
    
    double testValueDouble = Double.NaN; // OK to initialize to this because checks will have verified real value
    if ( isTestValueNeeded( statisticType ) ) {
        testValueDouble = testValue.doubleValue();
    }
    // For debugging...
    //Message.printStatus(2,routine,"StatisticType=" + statisticType + " TestType=" + testType +
    //    " testValueDouble=" + testValueDouble );

    double value;  // The value in the original time series
    // Dates for iteration in a year, adjusted for the analysis window
    // Use these internal to the loop below so as to not interfere with the full-year iteration
    //DateTime yearStartForAnalysisWindow = null;
    //DateTime yearEndForAnalysisWindow = null;
    // Dates for the start and end of the month worth of data
    DateTime date = new DateTime(ts.getDate1(),DateTime.DATE_FAST),
        dataStart = new DateTime(ts.getDate1(),DateTime.DATE_FAST),
        dataEnd = new DateTime(ts.getDate2(),DateTime.DATE_FAST);
    // Loop through the monthly analysis period.  The sample will be retrieved for the month
    for ( DateTime monthDate = new DateTime(analysisStart); monthDate.lessThanOrEqualTo(analysisEnd); monthDate.addMonth(1) ) {
        // Initialize for the new month
        double sum = 0.0;
        int nMissing = 0;
        int nNotMissing = 0;
        TSData data; // Data from the input time series
        // Initialize the value of the statistic in the year to missing.  Do this each time an output month is processed.
        double monthValue = monthts.getMissing(); // Data value for output time series
        double dayMoment = 0.0; // Day of month * value, for DayOfCentroid statistic
        double extremeValue = monthts.getMissing(); // Used for DayOfMin, etc., where check and day are tracked.
        // Dates bounding month, with precision of the input time series
        // TODO SAM 2014-04-03 Need to enable other than daily input.  Need to handle offset if odd hour, etc.
        dataStart.setDate(monthDate);
        dataStart.setDay(1);
        dataStart.setHour(0);
        dataStart.setMinute(0);
        dataEnd.setDate(monthDate);
        dataEnd.setDay(TimeUtil.numDaysInMonth(monthDate));
        TSIterator tsi = null;
        try {
            tsi = ts.iterator(dataStart,dataEnd);
        }
        catch ( Exception e ) {
            throw new RuntimeException ( "Exception initializing time series iterator for month's data period " +
                dataStart + " to " + dataEnd + " (" + e + ")." );
        }
        boolean doneAnalyzing = false; // If true, there is no more data or a statistic is complete
        while ( true ) {
            if ( iterateForward ) {
                // First call will initialize and return first point.
                data = tsi.next();  
            }
            else {
                // First call will initialize and return last point.
                data = tsi.previous();  
            }
            if ( data != null ) {
                // Still analyzing data in the analysis window
                date = tsi.getDate();
                value = tsi.getDataValue();
                // For debugging...
                if ( Message.isDebugOn ) {
                    Message.printDebug ( 10, routine, "Data value on " + date + " is " + value );
                }
                if ( ts.isDataMissing(value) ) {
                    // Data value is missing so increment the counter and continue to the next value.
                    // "data == null" will be true even for missing values and therefore when data
                    // run out, statistics that depend on the missing count will be processed correctly
                    // below.
                    ++nMissing;
                    continue;
                }
                else {
                    // Data value is not missing so process below.
                    ++nNotMissing;
                    // Always increment the sum because it is easy to compute and is needed by
                    // some statistics...
                    sum += value;
                    // Evaluate the test and/or statistic type to compute the value to be assigned to
                    // the year.  In some cases this is a single value and then done.  In other cases
                    // the value gets updated as more values are examined.
                    // The year value is examined to see if it is missing in order to know whether to
                    // initialize or update the value.
                    // If evaluating statistics like FIRST_DAY_GE, missing data will accumulate prior
                    // to the non-missing value, and the first non-missing value will cause further
                    // evaluation of input data to be skipped.  The determination on whether missing
                    // data are an issue is made when setting the yearly value at the end of the year loop.
                    if ( (testType == TestType.GE) && (value >= testValueDouble) ) {
                        if ( (statisticType == TSStatisticType.GE_COUNT) ||
                            (statisticType == TSStatisticType.GE_PERCENT) ){
                            if(monthts.isDataMissing( monthValue) ) {
                                monthValue = 1.0;
                            }
                            else {
                                monthValue += 1.0;
                            }
                        }
                        else if ((statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
                            (statisticType == TSStatisticType.DAY_OF_LAST_GE) ){
                            monthValue = date.getDay();
                            doneAnalyzing = true; // Found value for the month.
                        }
                    }
                    else if((testType == TestType.GT) && (value > testValueDouble) ) {
                        if ( (statisticType == TSStatisticType.GT_COUNT) ||
                            (statisticType == TSStatisticType.GT_PERCENT) ) {
                            if(monthts.isDataMissing( monthValue) ) {
                                monthValue = 1.0;
                            }
                            else {
                                monthValue += 1.0;
                            }
                        }
                        else if ((statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
                            (statisticType == TSStatisticType.DAY_OF_LAST_GT) ){
                            monthValue = date.getDay();
                            doneAnalyzing = true; // Found value for the year.
                        }
                    }
                    else if((testType == TestType.LE) && (value <= testValueDouble) ) {
                        Message.printStatus(2, routine, "Found value " + value + " <= " +
                            testValueDouble + " on " + date );
                        if ( (statisticType == TSStatisticType.LE_COUNT) ||
                            (statisticType == TSStatisticType.LE_PERCENT) ) {
                            if(monthts.isDataMissing( monthValue) ) {
                                monthValue = 1.0;
                            }
                            else {
                                monthValue += 1.0;
                            }
                        }
                        else if ((statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
                            (statisticType == TSStatisticType.DAY_OF_LAST_LE) ){
                            monthValue = date.getDay();
                            doneAnalyzing = true; // Found value for the year.
                        }
                    }
                    else if((testType == TestType.LT) && (value < testValueDouble) ) {
                        if ( (statisticType == TSStatisticType.LT_COUNT) ||
                            (statisticType == TSStatisticType.LT_PERCENT) ) {
                            if(monthts.isDataMissing( monthValue) ) {
                                monthValue = 1.0;
                            }
                            else {
                                monthValue += 1.0;
                            }
                        }
                        else if ((statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
                            (statisticType == TSStatisticType.DAY_OF_LAST_LT) ){
                            monthValue = date.getDay();
                            doneAnalyzing = true; // Found value for the year.
                        }
                    }
                    else if ( (statisticType == TSStatisticType.DAY_OF_CENTROID) ) {
                        dayMoment += date.getDay()*value;
                    }
                    else if ( (statisticType == TSStatisticType.DAY_OF_MAX) ||
                        (statisticType == TSStatisticType.MAX) ) {
                        if(monthts.isDataMissing(extremeValue)||(value > extremeValue) ) {
                            // Set the max...
                            if ( statisticType == TSStatisticType.DAY_OF_MAX ) {
                                extremeValue = value;
                                monthValue = date.getDay();
                            }
                            else if ( statisticType == TSStatisticType.MAX ) {
                                extremeValue = value;
                                monthValue = value;
                            }
                        }
                        // Need to continue analyzing period so do not set doneAnalyzing to false.
                    }
                    else if ( (statisticType == TSStatisticType.DAY_OF_MIN) ||
                        (statisticType == TSStatisticType.MIN) ) {
                        if(monthts.isDataMissing(extremeValue)||(value < extremeValue) ) {
                            Message.printStatus(2,routine,"Found new min " + value + " on " + date );
                            // Set the min...
                            if ( statisticType == TSStatisticType.DAY_OF_MIN ) {
                                extremeValue = value;
                                monthValue = date.getDay();
                            }
                            else if ( statisticType == TSStatisticType.MIN ) {
                                extremeValue = value;
                                monthValue = value;
                            }
                        }
                        // Need to continue analyzing period so do not set doneAnalyzing to false.
                    }
                    else if( (statisticType == TSStatisticType.MEAN) ||
                        (statisticType == TSStatisticType.TOTAL) ) {
                        // Need to accumulate the value (for Mean or Total)
                        // Accumulate into the year_value
                        if(monthts.isDataMissing( monthValue) ) {
                            monthValue = value;
                        }
                        else {
                            monthValue += value;
                        }
                    }
                }
            }
            else {
                // End of the data.  Compute the statistic and assign for the year
                doneAnalyzing = true;
            }
            if ( doneAnalyzing ) {
                // Save the results
                Message.printStatus(2, routine, "For " + monthDate + ", sum=" + sum + ", nMissing=" +
                    nMissing + ", nNotMissing=" + nNotMissing );
                if ( statisticType == TSStatisticType.DAY_OF_CENTROID ) {
                    if ( (nNotMissing > 0) &&
                        okToSetMonthStatistic(nMissing, nNotMissing, allowMissingCount, minimumSampleSize) ) {
                        monthts.setDataValue ( monthDate, dayMoment/sum );
                    }
                }
                else if ( statisticType == TSStatisticType.MEAN ) {
                    if ( (nNotMissing > 0) &&
                        okToSetMonthStatistic(nMissing, nNotMissing, allowMissingCount, minimumSampleSize) ) {
                        monthts.setDataValue ( monthDate, sum/nNotMissing );
                    }
                }
                else if ( statisticType == TSStatisticType.MISSING_COUNT ) {
                    // Always assign
                    monthts.setDataValue ( monthDate, (double)nMissing );
                }
                else if ( statisticType == TSStatisticType.MISSING_PERCENT ) {
                    // Always assign
                    if ( (nMissing + nNotMissing) > 0 ) {
                        monthts.setDataValue ( monthDate, 100.0*(double)nMissing/(double)(nMissing + nNotMissing) );
                    }
                }        
                else if ( statisticType == TSStatisticType.NONMISSING_COUNT ) {
                    // Always assign
                    monthts.setDataValue ( monthDate, (double)nNotMissing );
                }
                else if ( statisticType == TSStatisticType.NONMISSING_PERCENT ) {
                    // Always assign
                    if ( (nMissing + nNotMissing) > 0 ) {
                        monthts.setDataValue ( monthDate, 100.0*(double)nNotMissing/(double)(nMissing + nNotMissing) );
                    }
                }
                else if ( statisticType == TSStatisticType.TOTAL ) {
                    if ( (nNotMissing > 0) &&
                        okToSetMonthStatistic(nMissing, nNotMissing, allowMissingCount, minimumSampleSize) ) {
                        monthts.setDataValue ( monthDate, sum );
                    }
                }
                else {
                    Message.printStatus(2,routine,"Year " + monthDate + " value=" + monthValue );
                    if ( !monthts.isDataMissing(monthValue) &&
                        okToSetMonthStatistic(nMissing, nNotMissing, allowMissingCount, minimumSampleSize) ) {
                        // No additional processing is needed.
                        if ( (statisticType == TSStatisticType.GE_PERCENT) ||
                            (statisticType == TSStatisticType.GT_PERCENT) ||
                            (statisticType == TSStatisticType.LE_PERCENT) ||
                            (statisticType == TSStatisticType.LT_PERCENT) ) {
                            if ( (nMissing + nNotMissing) > 0 ) {
                                // Note that these statistics are for total number of points (not just
                                // non-missing points).
                                monthts.setDataValue ( monthDate, 100.0*monthValue/(nMissing + nNotMissing) );
                            }
                        }
                        else {
                            // Statistic has already been calculated.
                            monthts.setDataValue ( monthDate, monthValue );
                        }
                    }
                }
                break; // Will go to the next month of input, with new iterator
            }
        }
    }
}

/**
Return the number of missing values allowed in sample.
@return the number of missing values allowed in sample.
*/
private Integer getAllowMissingCount ()
{
    return __allowMissingCount;
}

/**
Return the analysis end date/time.
@return the analysis end date/time.
*/
private DateTime getAnalysisEnd ()
{
    return __analysisEnd;
}

/**
Return the analysis start date/time.
@return the analysis start date/time.
*/
private DateTime getAnalysisStart ()
{
    return __analysisStart;
}

/**
Return the analysis window end date/time.
@return the analysis window end date/time.
*/
private DateTime getAnalysisWindowEnd ()
{
    return __analysisWindowEnd;
}

/**
Return the analysis window start date/time.
@return the analysis window start date/time.
*/
private DateTime getAnalysisWindowStart ()
{
    return __analysisWindowStart;
}

/**
Return the minimum sample size allowed to compute the statistic.
@return the minimum sample size allowed to compute the statistic.
*/
private Integer getMinimumSampleSize ()
{
    return __minimumSampleSize;
}

/**
Return the time series identifier for the new time series.
@return the time series identifier for the new time series.
*/
private String getNewTSID ()
{
    return __newTSID;
}

/**
Return the search start date/time.
@return the search start date/time.
*/
private DateTime getSearchStart ()
{
    return __searchStart;
}

/**
Return a list of statistic choices for the requested input time series interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale indicates whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.  CURRENTLY NOT USED.
*/
public static List<TSStatisticType> getStatisticChoicesForInterval ( int interval, TimeScaleType timescale )
{   List<TSStatisticType> statistics = new ArrayList<TSStatisticType>();
    // Add in alphabetical order, splitting up by interval as appropriate.
    // Daily or finer...
    if ( (interval <= TimeInterval.DAY) || (interval == TimeInterval.UNKNOWN) ) {
        statistics.add ( TSStatisticType.DAY_OF_CENTROID );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_GE );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_GT );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_LE );
        statistics.add ( TSStatisticType.DAY_OF_FIRST_LT );
        statistics.add ( TSStatisticType.DAY_OF_LAST_GE );
        statistics.add ( TSStatisticType.DAY_OF_LAST_GT );
        statistics.add ( TSStatisticType.DAY_OF_LAST_LE );
        statistics.add ( TSStatisticType.DAY_OF_LAST_LT );
        statistics.add ( TSStatisticType.DAY_OF_MAX );
        statistics.add ( TSStatisticType.DAY_OF_MIN );
    }
    // All intervals...
    statistics.add ( TSStatisticType.GE_COUNT );
    statistics.add ( TSStatisticType.GE_PERCENT );
    statistics.add ( TSStatisticType.GT_COUNT );
    statistics.add ( TSStatisticType.GT_PERCENT );
    statistics.add ( TSStatisticType.LE_COUNT );
    statistics.add ( TSStatisticType.LE_PERCENT );
    statistics.add ( TSStatisticType.LT_COUNT );
    statistics.add ( TSStatisticType.LT_PERCENT );
    statistics.add ( TSStatisticType.MAX );
    // TODO SAM 2009-10-14 Need to support median
    //statistics.add ( TSStatisticType.MEDIAN );
    statistics.add ( TSStatisticType.MEAN );
    statistics.add ( TSStatisticType.MIN );
    statistics.add ( TSStatisticType.MISSING_COUNT );
    statistics.add ( TSStatisticType.MISSING_PERCENT );
    statistics.add ( TSStatisticType.NONMISSING_COUNT );
    statistics.add ( TSStatisticType.NONMISSING_PERCENT );
    statistics.add ( TSStatisticType.TOTAL );
    return statistics;
}

/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.  CURRENTLY NOT USED.
*/
public static List<String> getStatisticChoicesForIntervalAsStrings ( int interval, TimeScaleType timescale )
{
    List<TSStatisticType> choices = getStatisticChoicesForInterval( interval, timescale);
    List<String> stringChoices = new ArrayList<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Determine the statistic test type, when comparing against a test value.
@param statisticType a statistic type to check.
@return the test type for the statistic or NOT_USED if the test is not used for a statistic.
*/
private TestType getStatisticTestType ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GE_PERCENT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GE) ) {
        return TestType.GE;
    }
    else if ( (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.GT_PERCENT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ) {
        return TestType.GT;
    }
    else if ( (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LE_PERCENT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ) {
        return TestType.LE;
    }
    else if ( (statisticType == TSStatisticType.LT_COUNT) ||
        (statisticType == TSStatisticType.LT_PERCENT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ) {
        return TestType.LT;
    }
    else {
        return TestType.NOT_USED;
    }
}

/**
Determine the statistic time series data units.
@param statisticType a statistic type to check.
@param testType test type that is being performed.
@param testValue the test value to be checked.
@return the description for the time series, given the statistic and test types.
*/
private String getStatisticTimeSeriesDataUnits ( TSStatisticType statisticType,
    boolean statisticIsCount, boolean statisticIsDayOf, String tsUnits )
{
    if ( statisticIsCount ) {
        if ( (statisticType == TSStatisticType.GE_PERCENT) ||
            (statisticType == TSStatisticType.GT_PERCENT) ||
            (statisticType == TSStatisticType.LE_PERCENT) ||
            (statisticType == TSStatisticType.LT_PERCENT) ||
            (statisticType == TSStatisticType.MISSING_PERCENT) ||
            (statisticType == TSStatisticType.NONMISSING_PERCENT)) {
            return "Percent";
        }
        else {
            return "Count";
        }
    }
    else if ( statisticIsDayOf ) {
        return "DayOfYear";
    }
    else {
        return tsUnits;
    }
}

/**
Determine the statistic time series description.
@param statisticType a statistic type to check.
@param testType test type that is being performed.
@param testValue the test value to be checked.
@return the description for the time series, given the statistic and test types.
*/
private String getStatisticTimeSeriesDescription ( TSStatisticType statisticType, TestType testType,
    Double testValue, boolean statisticIsCount, boolean statisticIsDayOf, boolean statisticIsFirst, boolean statisticIsLast )
{   String testString = "?test?";
    String testValueString = "?testValue?";
    String desc = "?";
    if ( testValue != null ) {
        testValueString = StringUtil.formatString(testValue.doubleValue(),"%.6f");
    }
    if ( testType == TestType.GE ) {
        testString = ">=";
    }
    else if ( testType == TestType.GT ) {
        testString = ">";
    }
    else if ( testType == TestType.LE ) {
        testString = "<=";
    }
    else if ( testType == TestType.LT ) {
        testString = "<";
    }
    if ( statisticIsCount ) {
        if ( statisticType == TSStatisticType.MISSING_COUNT ) {
            desc = "Count of missing values";
        }
        else if ( statisticType == TSStatisticType.NONMISSING_COUNT ) {
            desc = "Count of nonmissing values";
        }
        else {
            desc = "Count of values " + testString + " " + testValueString;
        }
    }
    else if ( statisticIsDayOf ) {
        if ( statisticIsFirst ) {
            desc = "Day of year for first value " + testString + " " + testValueString;
        }
        else if ( statisticIsLast ) {
            desc = "Day of year for last value " + testString + " " + testValueString;
        }
        else if ( statisticType == TSStatisticType.DAY_OF_CENTROID ) {
            desc = "Day of year for centroid";
        }
        else if ( statisticType == TSStatisticType.DAY_OF_MAX ) {
            desc = "Day of year for maximum value";
        }
        else if ( statisticType == TSStatisticType.DAY_OF_MIN ) {
            desc = "Day of year for minimum value";
        }
    }
    else {
        // MAX, MEAN, etc.
        desc = "" + statisticType;
    }
    // If not set will fall through to default
    return desc;
}

/**
Return the name of the statistic being calculated.
@return the name of the statistic being calculated.
*/
public TSStatisticType getStatisticType ()
{
    return __statisticType;
}

/**
Return the test value used to calculate some statistics.
@return the test value used to calculate some statistics.
*/
private Double getTestValue ()
{
    return __testValue;
}

/**
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries ()
{
    return __ts;
}

/**
Indicate whether the statistic is a count.
Percents are handled as counts initially and are then converted to percent as the last assignment
@param statisticType a statistic type to check.
*/
private boolean isStatisticCount ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GE_PERCENT) ||
        (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.GT_PERCENT) ||
        (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LE_PERCENT) ||
        (statisticType == TSStatisticType.LT_COUNT) ||
        (statisticType == TSStatisticType.LT_PERCENT) ||
        (statisticType == TSStatisticType.MISSING_COUNT) ||
        (statisticType == TSStatisticType.NONMISSING_COUNT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Indicate whether the statistic is a "DayOf" statistic.
@param statisticType a statistic type to check.
*/
private boolean isStatisticDayOf ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.DAY_OF_CENTROID) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_MAX) ||
        (statisticType == TSStatisticType.DAY_OF_MIN)) {
        return true;
    }
    else {
        return false;
    }
}

/**
Indicate whether the statistic is a "First" statistic.
@param statisticType a statistic type to check.
*/
private boolean isStatisticFirst ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Indicate whether the statistic is a "Last" statistic.
@param statisticType a statistic type to check.
*/
private boolean isStatisticLast ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.DAY_OF_LAST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Indicate whether the statistic is determined by iterating forward or backward.
@param statisticType a statistic type to check.
@return true if the statistic is determined by iterating forward, false if iterating backward.
*/
private boolean isStatisticIterateForward ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.DAY_OF_LAST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ) {
        // Iterate backward
        return false;
    }
    else {
        return true; // By default most statistics are determined by iterating forward
    }
}

/**
Indicate whether a statistic is supported.
@param statisticType a statistic type to check.
@param interval time interval base to check, or TimeInterval.UNKNOWN if interval is not to be considered.
@param timeScale time scale to check, or null if not considered.
*/
public static boolean isStatisticSupported ( TSStatisticType statisticType, int interval, TimeScaleType timeScale )
{
    List<TSStatisticType> choices = getStatisticChoicesForInterval ( interval, timeScale );
    for ( int i = 0; i < choices.size(); i++ ) {
        if ( choices.get(i) == statisticType ) {
            return true;
        }
    }
    return false;
}

/**
Indicate whether the statistic requires that a test value be supplied.
@param statisticType a statistic type to check.
*/
public static boolean isTestValueNeeded ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ||
        (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GE_PERCENT) ||
        (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.GT_PERCENT) ||
        (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LE_PERCENT) ||
        (statisticType == TSStatisticType.LT_COUNT) ||
        (statisticType == TSStatisticType.LT_PERCENT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Create a year time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).
@param createData if true, calculate the data value array; if false, only assign metadata
@return The statistics time series.
*/
public MonthTS newStatisticMonthTS ( boolean createData )
{   String message, routine = getClass().getSimpleName() + ".newStatisticMonthTS";
    int dl = 10;

    // Get the data needed for the analysis - originally provided in the constructor
    
    TS ts = getTimeSeries();
    String newTSID = getNewTSID();
    TSStatisticType statisticType = getStatisticType();
    Double testValue = getTestValue();
    Integer allowMissingCount = getAllowMissingCount();
    Integer minimumSampleSize = getMinimumSampleSize();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisEnd = getAnalysisEnd();
    DateTime analysisWindowStart = getAnalysisWindowStart();
    DateTime analysisWindowEnd = getAnalysisWindowEnd();
    DateTime searchStart = getSearchStart();
    
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "Trying to create statistic month TS for \"" +
        ts.getIdentifierString() + "\"" );
    }

    // Get valid dates for the output time series because the ones passed in may have been null...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, analysisStart, analysisEnd );
    // Reset because these are handled generically below whether passed in or defaulted to "ts"
    analysisStart = new DateTime ( valid_dates.getDate1() );
    analysisEnd = new DateTime ( valid_dates.getDate2() );

    // Create a month time series to be calculated...

    MonthTS monthts = new MonthTS();
    monthts.addToGenesis ( "Initialized statistic month time series from \"" + ts.getIdentifierString() + "\"" );
    monthts.copyHeader ( ts );

    // Reset the identifier if the user has specified it...

    try {
        if ( (newTSID != null) && (newTSID.length() > 0) ) {
            TSIdent tsident = new TSIdent ( newTSID );
            // Make sure that the output interval is Month
            if ( !tsident.getInterval().equalsIgnoreCase("Month") ) {
                tsident.setInterval("Month");
            }
            monthts.setIdentifier ( tsident );
        }
        else {
            // Default is to set the scenario to the statistic...
            monthts.getIdentifier().setScenario ( "" + statisticType );
        }
    }
    catch ( Exception e ) {
        message = "Unable to set new time series identifier \"" + newTSID + "\".";
        Message.printWarning ( 3, routine,message );
        // IllegalArgumentException would have be thrown in the constructor so this must be something else.
        throw new RuntimeException(message);
    }

    // Need to make sure the base and multiplier are correct...
    monthts.setDataInterval ( TimeInterval.MONTH, 1 );

    // Automatically sets the precision to month for these dates...
    if ( analysisStart != null ) {
        monthts.setDate1 ( analysisStart );
    }
    else {
        monthts.setDate1 ( ts.getDate1() );
    }
    if ( analysisEnd != null ) {
        monthts.setDate2 ( analysisEnd );
    }
    else {
        monthts.setDate2 ( ts.getDate2() );
    }
    
    if ( !createData ) {
        return monthts;
    }

    // This will fill with missing data...
    monthts.allocateDataSpace();

    // Process the statistic of interest...

    calculateStatistic (
            ts, monthts, statisticType, testValue,
            analysisStart, analysisEnd, allowMissingCount, minimumSampleSize,
            analysisWindowStart, analysisWindowEnd, searchStart );

    // Return the statistic result...
    return monthts;
}

/**
Determine whether it is OK to set a monthly statistic based on handling of missing data.
@param missingCount number of missing values in the month.
@param nonMissingCount number of non-missing values in the month.
@param allowMissingCount the number of values allowed to be missing in a month (or -1 if no limit).
@param minimumSampleSize the minimum sample size, or -1 if no limit.
 */
private boolean okToSetMonthStatistic ( int missingCount, int nonMissingCount,
    int allowMissingCount, int minimumSampleSize )
{
    // Check the missing count...
    if ( (allowMissingCount < 0) || (missingCount <= allowMissingCount) ) {
        // So far OK to set, but check sample size
        if ( (minimumSampleSize < 0) || (nonMissingCount >= minimumSampleSize) ) {
            return true;
        }
        else {
            return false;
        }
    }
    else {
        return false;
    }
}

/**
Set the number of values allowed to be missing in the sample.
@param allowMissingCount the number of values allowed to be missing in the sample.
*/
private void setAllowMissingCount ( int allowMissingCount )
{
    __allowMissingCount = allowMissingCount;
}

/**
Set the end for the analysis.
@param analysisEnd end date/time for the analysis.
*/
private void setAnalysisEnd ( DateTime analysisEnd )
{
    __analysisEnd = analysisEnd;
}

/**
Set the start for the analysis.
@param analysisStart start date/time for the analysis.
*/
private void setAnalysisStart ( DateTime analysisStart )
{
    __analysisStart = analysisStart;
}

/**
Set the end for the analysis window.
@param analysisEnd end date/time for the analysis window.
*/
private void setAnalysisWindowEnd ( DateTime analysisWindowEnd )
{
    __analysisWindowEnd = analysisWindowEnd;
}

/**
Set the start for the analysis window.
@param analysisStart start date/time for the analysis window.
*/
private void setAnalysisWindowStart ( DateTime analysisWindowStart )
{
    __analysisWindowStart = analysisWindowStart;
}

/**
Set the minimum sample size.
@param minimumSampleSize the minimum sample size.
*/
private void setMinimumSampleSize ( int minimumSampleSize )
{
    __minimumSampleSize = minimumSampleSize;
}

/**
Set the new time series identifier.
@param newTSID the new time series identifier.
*/
private void setNewTSID ( String newTSID )
{
    __newTSID = newTSID;
}

/**
Set the search start.
@param searchStart start date/time processing in a month.
*/
private void setSearchStart ( DateTime searchStart )
{
    __searchStart = searchStart;
}

/**
Set the statistic type.
@param statisticType statistic type to calculate.
*/
private void setStatisticType ( TSStatisticType statisticType )
{
    __statisticType = statisticType;
}

/**
Set the test value used with some statistics.
@param testValue the test value used with some statistics.
*/
private void setTestValue ( Double testValue )
{
    __testValue = testValue;
}

/**
Set the time series being analyzed.
@param ts time series being analyzed.
*/
private void setTimeSeries ( TS ts )
{
    __ts = ts;
}

}