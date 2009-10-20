package RTi.TS;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;

/**
Compute a YearTS that has a statistic for each year in the period.
*/
public class TSUtil_NewStatisticTimeSeries
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
Starting date/time for output.
*/
private DateTime __outputStart = null;

/**
Ending date/time for output.
*/
private DateTime __outputEnd = null;

/**
New time series identifier to assign.
*/
private String __newTSID = null;

/**
Statistic to analyze.
*/
private TSStatisticType __statisticType = null;

/**
Number of missing allowed to compute sample.
*/
private Integer __allowMissingCount = null;

/**
Minimum required sample size.
*/
private Integer __minimumSampleSize = null;
    
/**
Create a new time series that contains statistics in each data value.  The period of
the result is the same as the source.  Each value in the result corresponds to a
statistic computed from a sample taken from the years in the period.  For example,
for a daily time series, the "Mean" statistic would return for every Jan 1, the mean
of all the non-missing Jan 1 values.  After constructing an instance of this class, call the
newStatisticTimeSeries() method to perform the analysis.  
@param ts Time series to analyze (must be a regular interval).
@param analysisStart Starting date/time for analysis, in precision of the original data.
@param analysisEnd Ending date for analysis, in precision of the original data.
@param outputStart Output start date/time.
If null, the period of the original time series will be output.
@param outputEnd Output end date/time.
If null, the period of the original time series will be output.
@param newTSID the new time series identifier to be assigned to the time series.
@param statisticType the statistic type for the output time series.
@param allowMissingCount the number of values allowed to be missing in the sample.
@param minimumSampleSize the minimum sample size to allow to compute the statistic.
@exception Exception if there is an error analyzing the time series.
*/
public TSUtil_NewStatisticTimeSeries ( TS ts, DateTime analysisStart, DateTime analysisEnd,
    DateTime outputStart, DateTime outputEnd, String newTSID, TSStatisticType statisticType,
    Integer allowMissingCount, Integer minimumSampleSize )
{   String routine = getClass().getName();
    String message;
    
    __ts = ts;
    __analysisStart = analysisStart;
    __analysisEnd = analysisEnd;
    __outputStart = outputStart;
    __outputEnd = outputEnd;
    __newTSID = newTSID;
    __statisticType = statisticType;
    __allowMissingCount = allowMissingCount;
    __minimumSampleSize = minimumSampleSize;
    
    if ( ts == null ) {
        // Nothing to do...
        message = "Null input time series";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }
    
    if ( statisticType != TSStatisticType.MEAN ) {
        throw new InvalidParameterException ( "Statistic \"" + statisticType + "\" is not supported.");
    }
}
    
/**
Create a time series that contains statistics in each data value.  The period of
the result is the same as the source.  Each value in the result corresponds to a
statistic computed from a sample taken from the years in the period.  For example,
for a daily time series, the "Mean" statistic would return for every Jan 1, the mean
of all the non-missing Jan 1 values.
@return The statistics time series.
@param ts Time series to analyze (must be a regular interval).
@param AnalysisStart_DateTime Starting date/time for analysis, in precision of the original data.
@param AnalysisEnd_DateTime Ending date for analysis, in precision of the original data.
@param OutputStart_DateTime Output start date/time.
If null, the period of the original time series will be output.
@param OutputEnd_DateTime Output end date/time.
If null, the entire period will be analyzed.
*/
public TS newStatisticTimeSeries ()
throws Exception
{   String message, routine = "TSAnalyst.createStatisticTimeSeries";
    int dl = 10;
    
    TS ts = getTimeSeries();
    DateTime analysisStart0 = getAnalysisStart();
    DateTime analysisEnd0 = getAnalysisEnd();
    DateTime outputStart0 = getOutputStart();
    DateTime outputEnd0 = getOutputEnd();
    String newTSID = getNewTSID();
    TSStatisticType statisticType = getStatisticType();
    Integer allowMissingCount = getAllowMissingCount();
    Integer minimumSampleSize = getMinimumSampleSize();
    
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "Trying to create statistic time series using time series \"" +
        ts.getIdentifierString() + "\" as input." );
    }

    // Get valid dates for the output time series because the ones passed in may have been null...

    // The period over which to analyze the time series (within a trace when dealing with an ensemble)...
    TSLimits validDates = TSUtil.getValidPeriod ( ts, analysisStart0, analysisEnd0 );
    DateTime analysisStart = new DateTime ( validDates.getDate1() );
    DateTime analysisEnd = new DateTime ( validDates.getDate2() );
    
    // The period to create the output statistic time series (within a trace when dealing with an ensemble)...
    validDates = TSUtil.getValidPeriod ( ts, outputStart0, outputEnd0 );
    DateTime outputStart = new DateTime ( validDates.getDate1() );
    DateTime outputEnd = new DateTime ( validDates.getDate2() );

    // Create an output time series to be filled...

    TS output_ts = null;
    try {
        output_ts = TSUtil.newTimeSeries(ts.getIdentifierString(), true);
    }
    catch ( Exception e ) {
        message = "Unable to create initial new time series using identifier \""+ ts.getIdentifierString() + "\".";
        Message.printWarning ( 3, routine,message );
        throw new InvalidParameterException(message);
    }
    output_ts.copyHeader ( ts );
    output_ts.addToGenesis ( "Initialized statistic time series as copy of \"" + ts.getIdentifierString() + "\"" );

    // Reset the identifier if the user has specified it...

    try {
        if ( (newTSID != null) && (newTSID.length() > 0) ) {
            TSIdent tsident = new TSIdent ( newTSID );
            output_ts.setIdentifier ( tsident );
        }
        else {
            // Default is to reset the scenario to the statistic...
            // FIXME SAM 2009-10-20 probably should not allow a default
            output_ts.getIdentifier().setScenario ( "" + statisticType );
        }
    }
    catch ( Exception e ) {
        message = "Unable to set new time series identifier \""+ newTSID + "\".";
        Message.printWarning ( 3, routine,message );
        throw new InvalidParameterException(message);
    }

    // Automatically sets the precision...
    output_ts.setDate1 ( outputStart );
    output_ts.setDate2 ( outputEnd );

    // This will fill with missing data...
    output_ts.allocateDataSpace();

    // Process the statistic of interest...

    // Analyze the single time series to get a statistic...
    TS stat_ts = computeStatistic ( ts, analysisStart, analysisEnd, statisticType, allowMissingCount,
        minimumSampleSize );
    // Now use the statistic to repeat every year...
    fillOutput ( stat_ts, output_ts, outputStart, outputEnd );

    // Return the statistic result...
    return output_ts;
}

/**
Create the statistic data from another time series.  The results are saved in a time
series having the interval of the input data, from January 1, 2000 to December 31, 2000 (one interval from
the start of the next year).  Year 2000 is used because it was a leap year.  This allows
February 29 data to be computed as a statistic, although it may not be used in the final output if the
final time series period does not span a leap year.
@param ts Time series to be analyzed.
@param analysis_start Start of period to analyze.
@param analysis_end End of period to analyze.
@param statisticType Statistic to compute.
@param allowMissingCount0 number of missing values allowed in sample to compute statistic.
@param minimumSampleSize0 minimum sample size required to compute statistic.
@return The statistics in a single-year time series.
*/
private TS computeStatistic ( TS ts, DateTime analysis_start, DateTime analysis_end,
    TSStatisticType statisticType, Integer allowMissingCount0, Integer minimumSampleSize0 )
throws Exception
{   String routine = getClass().getName() + ".computeStatistic";
    // Get the controlling parameters as simple integers to simplify code
    int allowMissingCount = -1;
    if ( allowMissingCount0 != null ) {
        allowMissingCount = allowMissingCount0.intValue();
    }
    int minimumSampleSize = -1;
    if ( minimumSampleSize0 != null ) {
        minimumSampleSize = minimumSampleSize0.intValue();
    }
    // Get the dates for the one-year statistic time series - treat as instantaneous, with leap year
    DateTime date1 = new DateTime ( analysis_start );   // To get precision
    date1.setYear( 2000 );
    date1.setMonth ( 1 );
    date1.setDay( 1 );
    date1.setHour ( 0 );
    date1.setMinute ( 0 );
    DateTime date2 = new DateTime ( date1 );
    // Add one year...
    date2.addYear( 1 );
    // Now subtract one interval...
    date2.addInterval( ts.getDataIntervalBase(), -ts.getDataIntervalMult());
    // Create time series for the sum, count of data values, and final statistic,
    // in the interval of the original time series...
    TS sum_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
    TS count_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
    TS countMissing_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
    TS stat_ts = TSUtil.newTimeSeries ( ts.getIdentifierString(), true );
    // Copy the header information...
    sum_ts.copyHeader ( ts );
    count_ts.copyHeader ( ts );
    stat_ts.copyHeader ( ts );
    // Reset the data type...
    sum_ts.setDataType("Sum");
    count_ts.setDataType("CountNonMissing");
    countMissing_ts.setDataType("CountMissing");
    stat_ts.setDataType("" + statisticType);
    // Set the dates in the statistic time series...
    sum_ts.setDate1( date1 );
    sum_ts.setDate2 ( date2 );
    count_ts.setDate1( date1 );
    count_ts.setDate2 ( date2 );
    countMissing_ts.setDate1( date1 );
    countMissing_ts.setDate2 ( date2 );
    stat_ts.setDate1( date1 );
    stat_ts.setDate2 ( date2 );
    // Now allocate the data space...
    sum_ts.allocateDataSpace();
    count_ts.allocateDataSpace();
    countMissing_ts.allocateDataSpace();
    stat_ts.allocateDataSpace();
    // Iterate through the raw data and increment the statistic time series.
    // For now, only handle a few statistics.
    TSIterator tsi = ts.iterator();
    DateTime date;
    double value; // Value from the input time series
    double sum_value; // Value in the sum time series
    DateTime date_tmp = new DateTime ( date1 ); // For setting data into stat_ts
    while ( tsi.next() != null ) {
        // Get the date out of the iterator (may skip over leap years).
        date = tsi.getDate();
        value = tsi.getDataValue();
        // Set the date information in the reused date (year is set above)...
        date_tmp.setMonth(date.getMonth());
        date_tmp.setDay(date.getDay());
        date_tmp.setHour(date.getHour());
        date_tmp.setMinute(date.getMinute());
        if ( ts.isDataMissing(value) ) {
            // Ignore missing data but increment the missing data counter...
            if ( ts.isDataMissing(countMissing_ts.getDataValue(date_tmp)) ) {
                countMissing_ts.setDataValue(date_tmp,1.0);
            }
            else {
                countMissing_ts.setDataValue(date_tmp,(countMissing_ts.getDataValue(date_tmp) + 1.0) );
            }
            continue;
        }
        sum_value = sum_ts.getDataValue ( date_tmp );
        if ( ts.isDataMissing(sum_value) ) {
            // Just assign the value
            sum_ts.setDataValue ( date_tmp, value );
            count_ts.setDataValue ( date_tmp, 1.0 );
        }
        else {
            // Increment the total in the time series...
            sum_ts.setDataValue( date_tmp, sum_value + value);
            count_ts.setDataValue ( date_tmp, (count_ts.getDataValue(date_tmp) + 1.0) );
        }
    }
    // Now loop through the sum time series and compute the final one-year statistic time series...
    tsi = count_ts.iterator();
    // TODO SAM 2007-11-05 Fix this if statistics other than Mean are added
    double countNonMissingDouble, countMissingDouble;
    int countNonMissing = 0, countMissing;
    while ( tsi.next() != null ) {
        date = tsi.getDate();
        countNonMissingDouble = tsi.getDataValue();
        countNonMissing = (int)(countNonMissingDouble + .01);
        countMissingDouble = countMissing_ts.getDataValue(date);
        countMissing = (int)(countMissingDouble + .01);
        if ( countNonMissing == 0 ) {
            // No data values so keep the initial missing value
            continue;
        }
        if ( (allowMissingCount >= 0) && (countMissing > allowMissingCount) ) {
            // Too many missing values to compute statistic
            Message.printStatus ( 2, routine, "Not computing time series statistic at " + date +
                " because number of missing values " + countMissing + " is > allowed (" + allowMissingCount + ").");
            stat_ts.setDataValue ( date, stat_ts.getMissing() );
            continue;
        }
        if ( (minimumSampleSize >= 0) && (countNonMissing < minimumSampleSize) ) {
            // Sample size too small to compute statistic
            Message.printStatus ( 2, routine, "Not computing time series statistic at " + date +
                " because sample size " + countNonMissing + " is < minimum required (" + minimumSampleSize + ").");
            stat_ts.setDataValue ( date, stat_ts.getMissing() );
            continue;
        }
        if ( statisticType == TSStatisticType.MEAN ) {
            stat_ts.setDataValue ( date, sum_ts.getDataValue(date)/countNonMissingDouble);
        }
        else if ( statisticType == TSStatisticType.MEDIAN ) {
            // FIXME SAM 2008-10-30 Need to finish
            //stat_ts.setDataValue ( date, MathUtil.median(countNonMissing, x));
        }
    }
    // Return the result.
    return stat_ts;
}

/**
Fill the output repeating time series with the statistic values.
@param stat_ts Year-long time series with statistic.
@param output_ts Output time series to fill.
@param output_start Output period start.
@param output_end Output period end.
*/
private void fillOutput ( TS stat_ts, TS output_ts, DateTime output_start, DateTime output_end )
throws Exception
{
    TSIterator tsi = output_ts.iterator();
    DateTime date;
    DateTime stat_date = null;
    while ( tsi.next() != null ) {
        // Get the date of interest in the output...
        date = tsi.getDate();
        if ( stat_date == null ) {
            // Initialize to the same precision, etc., by copying the other.
            stat_date = new DateTime ( date );
            stat_date.setYear ( 2000 );
        }
        // Get the corresponding value from the one-year statistic time series.  Do this
        // by resetting the stat_date information to that of the other date but use a year of
        // 2000 since that is used in the statistics time series processing.
        stat_date.setMonth ( date.getMonth() );
        stat_date.setDay ( date.getDay() );
        stat_date.setHour ( date.getHour() );
        stat_date.setMinute ( date.getMinute() );
        output_ts.setDataValue ( date, stat_ts.getDataValue(stat_date));
    }
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
Return the time series identifier for the new time series.
@return the time series identifier for the new time series.
*/
private String getNewTSID ()
{
    return __newTSID;
}

/**
Return the output start date/time.
@return the output start date/time.
*/
public DateTime getOutputStart ()
{
    return __outputStart;
}

/**
Return the output end date/time.
@return the output end date/time.
*/
public DateTime getOutputEnd ()
{
    return __outputEnd;
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
Return the minimum sample size allowed to compute the statistic.
@return the minimum sample size allowed to compute the statistic.
*/
public Integer getMinimumSampleSize ()
{
    return __minimumSampleSize;
}

/**
Get the list of statistics that can be performed.
*/
public static List<TSStatisticType> getStatisticChoices()
{
    // TODO SAM 2009-10-14 Need to enable more statistics
    List<TSStatisticType> choices = new Vector();
    //choices.add ( TSStatisticType.MAX );
    choices.add ( TSStatisticType.MEAN );
    //choices.add ( TSStatisticType.MEDIAN );
    //choices.add ( TSStatisticType.MIN );
    //choices.add ( TSStatisticType.SKEW );
    //choices.add ( TSStatisticType.STD_DEV );
    //choices.add ( TSStatisticType.VARIANCE );
    return choices;
}

/**
Get the list of statistics that can be performed.
@return the statistic display names as strings.
*/
public static List<String> getStatisticChoicesAsStrings()
{
    List<TSStatisticType> choices = getStatisticChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
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
Return the time series being analyzed.
@return the time series being analyzed.
*/
public TS getTimeSeries ()
{
    return __ts;
}

}