package RTi.TS;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import RTi.Util.Time.TimeScaleType;
import RTi.Util.Time.TimeUtil;

// TODO SAM 2009-10-14 Migrate computational code from TSAnalyst to here
/**
Compute a YearTS that has a statistic for each year in the period.
*/
public class TSUtil_NewStatisticYearTS
{
    
/**
Enumerations used when processing the statistic.
*/
private enum TestType {
    GE, // Test >=
    GT, // Test >
    LE, // Test <=
    LT, // Test <
    MAX, // Test for max()
    MIN, // Test for min()
    ACCUMULATE, // Accumulate values
    SAMPLE, // Hold values in a sample array
    UNKNOWN }; // Unknown test
    
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
Starting date/time for analysis window, within a year.
*/
private DateTime __analysisWindowStart = null;

/**
Ending date/time for analysis window, within a year.
*/
private DateTime __analysisWindowEnd = null;

/**
Date/time to start search within a year.
*/
private DateTime __searchStart = null;
    
/**
Construct the analysis object with required input.  Values will be checked for validity.
Execute the newStatisticYearTS() method to perform the analysis.
@param ts time series to analyze
@param analysisStart Starting date/time for analysis, in precision of the original data.
@param analysisEnd Ending date for analysis, in precision of the original data.
@param newTSID the new time series identifier to be assigned to the time series.
@param statisticType the statistic type for the output time series.
@param testValue test value (e.g., threshold value) needed to process some statistics.
@param allowMissingCount the number of values allowed to be missing in the sample.
@param minimumSampleSize the minimum sample size to allow to compute the statistic.
@param analysisWindowStart Starting date/time (year is ignored) for analysis within the year,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param analysisWindowEnd Ending date (year is ignored) for analysis within the year,
in precision of the original data.  If null, the entire year of data will be analyzed.
@param searchStart date/time for which to start the search within each year (year is ignored), needed
by some statistics.
*/
public TSUtil_NewStatisticYearTS ( TS ts, String newTSID, TSStatisticType statisticType, Double testValue,
    Integer allowMissingCount, Integer minimumSampleSize, DateTime analysisStart, DateTime analysisEnd,
    DateTime analysisWindowStart, DateTime analysisWindowEnd, DateTime searchStart )
{   String routine = getClass().getName();
    String message;
    
    if ( ts == null ) {
        // Nothing to do...
        message = "Null input time series - cannot calculate statistic time series.";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }
    setTimeSeries ( ts );
    setNewTSID ( newTSID );
    try {
        if ( (newTSID != null) && (newTSID.length() > 0) ) {
            TSIdent tsident = new TSIdent ( newTSID );
            // Make sure that the output interval is Year
            if ( !tsident.getInterval().equalsIgnoreCase("Year") ) {
                throw new IllegalArgumentException (
                    "New time series identifier \"" + newTSID + "\" must have an interval of Year - " +
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
Return the date/time within the year to start the search.
@return the date/time within the year to start the search.
*/
private DateTime getSearchStart ()
{
    return __searchStart;
}

/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale indicates whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.  CURRENTLY NOT USED.
*/
public static List<TSStatisticType> getStatisticChoicesForInterval ( int interval, TimeScaleType timescale )
{   List<TSStatisticType> statistics = new Vector();
    // Add in alphabetical order, splitting up by interval as appropriate.
    // All intervals...
    statistics.add ( TSStatisticType.COUNT_GE );
    statistics.add ( TSStatisticType.COUNT_GT );
    statistics.add ( TSStatisticType.COUNT_LE );
    statistics.add ( TSStatisticType.COUNT_LT );
    // Daily or finer...
    if ( (interval < TimeInterval.WEEK) || (interval == TimeInterval.UNKNOWN) ) {
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
    statistics.add ( TSStatisticType.MAX );
    // TODO SAM 2009-10-14 Need to support median
    //statistics.add ( TSStatisticType.MEDIAN );
    statistics.add ( TSStatisticType.MEAN );
    statistics.add ( TSStatisticType.MIN );
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
@param statisticType a statistic type to check.
*/
private boolean isStatisticCount ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.COUNT_GE) ||
        (statisticType == TSStatisticType.COUNT_GT) ||
        (statisticType == TSStatisticType.COUNT_LE) ||
        (statisticType == TSStatisticType.COUNT_LT) ||
        (statisticType == TSStatisticType.MISSING_COUNT) ||
        (statisticType == TSStatisticType.NONMISSING_COUNT) ) {
        return true;
    }
    else {
        return false;
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
    if ( (statisticType == TSStatisticType.COUNT_GE) ||
        (statisticType == TSStatisticType.COUNT_GT) ||
        (statisticType == TSStatisticType.COUNT_LE) ||
        (statisticType == TSStatisticType.COUNT_LT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Create a year time series that contains statistics in each data value (e.g.,
percent missing, percent not missing).
@return The statistics time series.
*/
public YearTS newStatisticYearTS ( )
{   String message, routine = "TSAnalyst.createStatisticYearTS";
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
        Message.printDebug ( dl, routine, "Trying to create statistic year TS for \"" +
        ts.getIdentifierString() + "\"" );
    }

    // Get valid dates for the output time series because the ones passed in may have been null...

    TSLimits valid_dates = TSUtil.getValidPeriod ( ts, analysisStart, analysisEnd );
    DateTime start = new DateTime ( valid_dates.getDate1() );
    DateTime end = new DateTime ( valid_dates.getDate2() );
    valid_dates = null;

    // Create a year time series to be filled...

    YearTS yearts = new YearTS();
    yearts.addToGenesis ( "Initialized statistic year time series from \"" + ts.getIdentifierString() + "\"" );
    yearts.copyHeader ( ts );

    // Reset the identifier if the user has specified it...

    try {
        if ( (newTSID != null) && (newTSID.length() > 0) ) {
            TSIdent tsident = new TSIdent ( newTSID );
            // Make sure that the output interval is Year
            if ( !tsident.getInterval().equalsIgnoreCase("Year") ) {
                tsident.setInterval("Year");
            }
            yearts.setIdentifier ( tsident );
        }
        else {
            // Default is to set the scenario to the statistic...
            yearts.getIdentifier().setScenario ( "" + statisticType );
        }
    }
    catch ( Exception e ) {
        message = "Unable to set new time series identifier \"" + newTSID + "\".";
        Message.printWarning ( 3, routine,message );
        // IllegalArgumentException would have be thrown in the constructor so this must be something else.
        throw new RuntimeException(message);
    }

    // Need to make sure the base and multiplier are correct...
    yearts.setDataInterval ( TimeInterval.YEAR, 1 );

    // Automatically sets the precision...
    yearts.setDate1 ( start );
    yearts.setDate2 ( end );

    // This will fill with missing data...
    yearts.allocateDataSpace();

    // Process the statistic of interest...

    /*
    if (    
        (statisticType == TSStatisticType.COUNT_GE) ||
        (statisticType == TSStatisticType.COUNT_GT) ||
        (statisticType == TSStatisticType.COUNT_LE) ||
        (statisticType == TSStatisticType.COUNT_LT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ||
        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ||
        (statisticType == TSStatisticType.DAY_OF_MAX) ||
        (statisticType == TSStatisticType.DAY_OF_MIN) ||
        (statisticType == TSStatisticType.MAX) ||
        (statisticType == TSStatisticType.MEAN) ||
        (statisticType == TSStatisticType.MEDIAN) ||
        (statisticType == TSStatisticType.MIN) ||
        (statisticType == TSStatisticType.TOTAL) ) {*/
        processStatistic ( ts, yearts, statisticType, testValue, start, end,
            allowMissingCount, minimumSampleSize, analysisWindowStart, analysisWindowEnd,
            searchStart );

    // Return the statistic result...
    return yearts;
}

/**
Determine whether it is OK to set a yearly statistic based on handling of missing data.
@param missingCount number of missing values in the year.
@param nonMissingCount number of non-missing values in the year.
@param allowMissingCount the number of values allowed to be missing in a year (or -1 if no limit).
@param minimumSampleSize the minimum sample size, or -1 if no limit.
 */
private boolean okToSetYearStatistic ( int missingCount, int nonMissingCount,
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
Process a time series to create the the following annual statistics:
<ol>
<li>    CountGE</li>
<li>    CountGT</li>
<li>    CountLE</li>
<li>    CountLT</li>
<li>    CountMissing</li>
<li>    CountNotMissing</li>
<li>    DayOfFirstGE</li>
<li>    DayOfFirstGT</li>
<li>    DayOfFirstLE</li>
<li>    DayOfFirstLT</li>
<li>    DayOfLastGE</li>
<li>    DayOfLastGT</li>
<li>    DayOfLastLE</li>
<li>    DayOfLastLT</li>
<li>    DayOfMax</li>
<li>    DayOfMin</li>
<li>    Max</li>
<li>    Mean</li>
<li>    Min</li>
<li>    Total</li>
</ol>
@param ts Time series to analyze.
@param yearts YearTS to fill with the statistic.
@param start Start of the analysis (precision matching ts).
@param end End of the analysis (precision matching ts).
@param analysisWindowStart If not null, specify the start of the window within
the year for data, for example to specify a season.
Currently only Month... to precision are evaluated (not day... etc.).
@param analysisWindowEnd If not null, specify the end of the window within
the year for data, for example to specify a season.
Currently only Month... to precision are evaluated (not day... etc.).
@param searchStart date/time to start search within each year.
*/
private void processStatistic ( TS ts, YearTS yearts, TSStatisticType statisticType,
    Double testValue, DateTime start, DateTime end,
    int allowMissingCount, int minimumSampleSize,
    DateTime analysisWindowStart, DateTime analysisWindowEnd, DateTime searchStart )
{   String routine = getClass().getName() + ".processStatistic", message;
    DateTime yearDate = new DateTime ( end, DateTime.PRECISION_YEAR );
    double yearValue = 0.0,    // Statistic value for year.
        value = 0.0,        // Time series data value.
        extremeValue = 0.0;    // Extreme value in a year.
    boolean iterateForward = true; // Direction for iteration (true = forward, false = backward)
    int dl = 1; // Debug level for this method

    int interval_base = ts.getDataIntervalBase();
    int interval_mult = ts.getDataIntervalMult();
    if ( interval_base != TimeInterval.DAY ) {
        message = "Only daily time series can be processed.";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }
    boolean isRegular = TimeInterval.isRegularInterval(interval_base);
    
    double testValueDouble = -1; // OK to initialize to this because checks will have verified real value
    if ( isTestValueNeeded( statisticType ) ) {
        testValueDouble = testValue.doubleValue();
    }

    String searchStartString = ""; // For output messages
    if ( searchStart != null ) {
        searchStartString = "" + searchStart;
    }
    
    TestType testType = TestType.UNKNOWN;

    boolean statisticIsCount = isStatisticCount(statisticType);
    if ( statisticType == TSStatisticType.COUNT_GE ) {
        iterateForward = true;
        testType = TestType.GE;
        yearts.setDescription ( "Count of values >= " + testValue );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "Count" );
    }
    else if ( statisticType == TSStatisticType.COUNT_GT ) {
        iterateForward = true;
        testType = TestType.GT;
        yearts.setDescription ( "Count of values > " + testValue );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "Count" );
    }
    else if ( statisticType == TSStatisticType.COUNT_LE ) {
        iterateForward = true;
        testType = TestType.LE;
        yearts.setDescription ( "Count of values <= " + testValue );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "Count" );
    }
    else if ( statisticType == TSStatisticType.COUNT_LT ) {
        iterateForward = true;
        testType = TestType.LT;
        yearts.setDescription ( "Count of values < " + testValue );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "Count" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_FIRST_GE ) {
        iterateForward = true;
        testType = TestType.GE;
        yearts.setDescription ( "Day of year for first value >= " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_FIRST_GT ) {
        iterateForward = true;
        testType = TestType.GT;
        yearts.setDescription ( "Day of year for first value > " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_FIRST_LE ) {
        iterateForward = true;
        testType = TestType.LE;
        yearts.setDescription ( "Day of year for first value <= " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_FIRST_LT ) {
        iterateForward = true;
        testType = TestType.LT;
        yearts.setDescription ( "Day of year for first value < " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_LAST_GE ) {
        iterateForward = false;
        testType = TestType.GE;
        yearts.setDescription ( "Day of year for last value >= " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_LAST_GT ) {
        iterateForward = false;
        testType = TestType.GT;
        yearts.setDescription ( "Day of year for last value > " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_LAST_LE ) {
        iterateForward = false;
        testType = TestType.LE;
        yearts.setDescription ( "Day of year for last value <= " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_LAST_LT ) {
        iterateForward = false;
        testType = TestType.LT;
        yearts.setDescription ( "Day of year for last value < " + testValue );
        yearts.setDataUnits ( "DayOfYear" );
    }
    // TODO SAM 2005-09-28 Need to decide if iteration direction and SearchStart should be a
    // parameter for max and min
    else if ( statisticType == TSStatisticType.DAY_OF_MAX ) {
        iterateForward = true;
        testType = TestType.MAX;
        yearts.setDescription ( "Day of year for maximum value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.DAY_OF_MIN ) {
        iterateForward = true;
        testType = TestType.MIN;
        yearts.setDescription ( "Day of year for minimum value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( "DayOfYear" );
    }
    else if ( statisticType == TSStatisticType.MAX ) {
        iterateForward = true;
        testType = TestType.MAX;
        yearts.setDescription ( "Maximum value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
    }
    else if ( statisticType == TSStatisticType.MEAN ) {
        iterateForward = true;
        testType = TestType.ACCUMULATE;
        yearts.setDescription ( "Mean value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
        //Statistic_isCount = true;   // Needed for computations
    }
    else if ( statisticType == TSStatisticType.MEDIAN ) {
        iterateForward = true;
        testType = TestType.SAMPLE;
        yearts.setDescription ( "Median value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
        //Statistic_isCount = true;   // Needed for computations
    }
    else if ( statisticType == TSStatisticType.MIN ) {
        iterateForward = true;
        testType = TestType.MIN;
        yearts.setDescription ( "Minimum value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
    }
    else if ( statisticType == TSStatisticType.TOTAL ) {
        iterateForward = true;
        testType = TestType.ACCUMULATE;
        yearts.setDescription ( "Total value" );
        searchStartString = "01-01";  // Always default to first of year
        yearts.setDataUnits ( ts.getDataUnits() );
        //Statistic_isCount = true;   // Needed for computation checks
    }
    else {
        message = "Unknown statistic (" + statisticType + ").";
        Message.printWarning ( 3, routine, message );
        throw new InvalidParameterException ( message );
    }

    // Loop through data, starting at the front of the time series...
    DateTime date = null;
    DateTime dateSearch = null; // DateTime corresponding to SearchStart for a particular year
    if ( iterateForward ) {
        date = new DateTime ( start );
    }
    else {
        date = new DateTime ( end );
    }
    int yearPrev = date.getYear();
    int year = 0;
    TSIterator tsi = null;
    try {
        tsi = ts.iterator();
    }
    catch ( Exception e ) {
        throw new RuntimeException ( "Error creating time series iterator (" + e + ").");
    }
    TSData data = null;
    DateTime AnalysisWindowEndInYear_DateTime = null; // End of analysis window in a year
    boolean needToAnalyze = true; // Need to analyze value for current year
    boolean firstInterval = true;
    int missingCount = 0; // missing count in a year
    int nonMissingCount = 0; // non-missing count in a year
    int gap = 0; // Number of missing values in a gap at the end of the period.
    boolean endOfData = false; // Use to indicate end of data because
                    // checking for data == null directly can't be done with SearchStart logic.
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
            date = tsi.getDate();
            year = date.getYear();
        }
        else {
            endOfData = true;
        }
        if ( (year != yearPrev) || // New year so save previous year's data value.
            (endOfData) ||    // End of data so save previous year's data value.
            firstInterval ) {  // First interval so initialize (but do not save).
            // New year or last interval so save the results from the previous interval analysis...
            if ( !firstInterval ) {
                yearDate.setYear ( yearPrev );
                if ( statisticIsCount && yearts.isDataMissing(yearValue) &&
                    okToSetYearStatistic(missingCount,nonMissingCount,allowMissingCount,minimumSampleSize) ) {
                    // Never assigned a count but missing data were not an issue so assign the value to 0.
                    yearValue = 0.0;
                }
                // Now re-check to see if the value should be set (not missing)...
                if ( !yearts.isDataMissing(yearValue) ) {
                    // Have a value to assign to the statistic...
                    if ( statisticType == TSStatisticType.TOTAL ) {
                        if ( !yearts.isDataMissing(yearValue) &&
                            okToSetYearStatistic(missingCount,nonMissingCount,allowMissingCount,minimumSampleSize)) {
                            if ( Message.isDebugOn ) {
                                Message.printDebug ( dl, routine, "Setting " + yearDate + " year value=" + yearValue );
                            }
                            yearts.setDataValue ( yearDate, yearValue );
                        }
                    }
                    else if ( statisticType == TSStatisticType.MEAN ) {
                        if ( !yearts.isDataMissing(yearValue) &&
                            okToSetYearStatistic(missingCount,nonMissingCount,allowMissingCount,minimumSampleSize)) {
                            yearValue = yearValue/(double)nonMissingCount;
                            if ( Message.isDebugOn ) {
                                Message.printDebug ( dl, routine, "Setting " + yearDate + " year value=" + yearValue );
                            }
                            yearts.setDataValue ( yearDate, yearValue );
                        }
                    }
                    else {
                        // Simple assignment of a statistic.
                        yearts.setDataValue ( yearDate, yearValue );
                        if ( Message.isDebugOn ) {
                            Message.printDebug ( dl, routine, "Setting value for "+ yearPrev + " to " + yearValue );
                        }
                    }
                }
            }
            if ( endOfData ) {
                // All data have been processed.
                break;
            }
            // Do the following for the first interval or if a new year has started...
            // Initialize for next processing interval...
            firstInterval = false; // Other checks will now control
            yearValue = yearts.getMissing();
            extremeValue = yearts.getMissing();
            missingCount = 0;
            nonMissingCount = 0;
            yearPrev = year;
            needToAnalyze = true;
            // Adjust the starting point if necessary.  Find the
            // nearest value later than or equal to the search start...
            // FIXME SAM 2008-02-05 Need to phase out SearchStart and just use the analysis window.
            // For now use the search start to be the start of the search.
            if ( (searchStart != null) || (analysisWindowStart != null) ) {
                dateSearch = new DateTime ( date );
                if ( searchStart != null ) {
                    dateSearch.setMonth( searchStart.getMonth());
                    dateSearch.setDay ( searchStart.getDay());
                    if (Message.isDebugOn) {
                        Message.printDebug ( dl, routine, "Will start processing in year on SearchStart: " + dateSearch );
                    }
                }
                if ( analysisWindowStart != null ) {
                    // The AnalysisWindow takes precedence.
                    dateSearch.setMonth( analysisWindowStart.getMonth());
                    dateSearch.setDay ( analysisWindowStart.getDay());
                    // Also set the end date in the year to include.
                    AnalysisWindowEndInYear_DateTime = new DateTime(dateSearch);
                    AnalysisWindowEndInYear_DateTime.setMonth( analysisWindowEnd.getMonth());
                    AnalysisWindowEndInYear_DateTime.setDay ( analysisWindowEnd.getDay());
                    if (Message.isDebugOn) {
                        Message.printDebug ( dl, routine,
                        "Will start processing in year on AnalysisWindowStart: " + dateSearch );
                        Message.printDebug ( dl, routine,
                        "Will end processing in year on AnalysisWindowEnd: " + AnalysisWindowEndInYear_DateTime );
                    }
                }
                data = tsi.goTo ( dateSearch, false );
                if ( data == null ) {
                    // Did not find the requested starting date so must have run out of data.
                    // The original date still applies in some cases.
                    // Also evaluate for missing data if a regular time series.
                    if (Message.isDebugOn) {
                        Message.printDebug ( dl, routine, "Did not find search start using " + dateSearch );
                    }
                    if ( isRegular ) {
                        // Need to skip over the end period to a date in the period,
                        // keeping track of the missing count...
                        if ( iterateForward ) {
                            if ( dateSearch.greaterThan( date) ) {
                                // Ran out of data at end...
                                gap = -1;
                            }
                            else {
                                // Not enough data before search start...
                                gap = TimeUtil.getNumIntervals( TimeUtil.min(dateSearch,start),
                                TimeUtil.max( dateSearch, start),
                                interval_base, interval_mult );
                            }
                        }
                        else {
                            // Iterate backward
                            if ( dateSearch.lessThan( start) ) {
                                // Ran out of data at end...
                                gap = -1;
                            }
                            else {
                                // Not enough data before search start...
                                gap = TimeUtil.getNumIntervals( TimeUtil.min( end, dateSearch),
                                TimeUtil.max( end, dateSearch),
                                interval_base, interval_mult );
                            }
                        }
                        if ( gap >= 0 ) {
                            if (Message.isDebugOn) {
                                Message.printDebug ( dl, routine,
                                "Found " + gap + " missing values between search start and data." );
                            }
                            missingCount += gap;
                        }
                        else {
                            // Don't have enough data...
                            needToAnalyze = false;
                            endOfData = true;
                            if (Message.isDebugOn) {
                                Message.printDebug ( dl, routine, "Don't have data at end of period to analyze." );
                            }
                        }
                    }
                    // Irregular...
                    // just process what is available.
                    // TODO SAM 2005-09-27 Need to review this when other than Day interval data are supported.
                }
                else {
                    // Able to position the iterator so reset the date of the iterator and process below...
                    date = data.getDate();
                }
            }
        }
        // Analyze...
        // If need_to_analyze is false, then the data can be skipped.
        // This will occur either if the value is found or too much
        // missing data have been found and the result cannot be used.
        // TODO SAM 2005-09-22 Some of the following is in place because a TSIterator is
        // being used to handle regular and irregular data.  It
        // should be possible to jump over data but for now the brute
        // force search is performed.
        if ( needToAnalyze && !endOfData ) {
            if ( (AnalysisWindowEndInYear_DateTime != null) && date.greaterThan(AnalysisWindowEndInYear_DateTime) ) {
                // Just skip the data.
                continue;
            }
            value = tsi.getDataValue ();
            if ( Message.isDebugOn ) {
                Message.printDebug ( dl, routine, "Processing " + date + " value=" + value +
                        " year value (before this value)=" + yearValue );
            }
            // Put an initial check because the missing data count
            // could have been set while setting the SearchStart...
            if ( okToSetYearStatistic(missingCount,nonMissingCount,allowMissingCount,minimumSampleSize) ) {
                // Have too much missing data to generate the statistic...
                needToAnalyze = false;
                Message.printDebug ( dl, "",
                "Will not analyze year because more than " + allowMissingCount +
                " missing values were found (" + missingCount + ") or sample size (" + nonMissingCount +
                " ) is less than " + minimumSampleSize );
                continue;
            }
            // If missing data have not been a problem so far, continue with the check...
            if ( ts.isDataMissing ( value ) ) {
                ++missingCount;
                if ( okToSetYearStatistic(missingCount,nonMissingCount,allowMissingCount,minimumSampleSize) ) {
                    // Have too much missing data to generate the statistic...
                    needToAnalyze = false;
                    Message.printDebug ( dl, "",
                    "Will not analyze year because more than " + allowMissingCount +
                    " missing values were found (" + missingCount + ") or sample size (" + nonMissingCount +
                    ") is less than " + minimumSampleSize );
                }
            }
            else {
                // Data value is not missing so evaluate the test...
                ++nonMissingCount;
                if ( (testType == TestType.GE) && (value >= testValueDouble) ) {
                    if (statisticType == TSStatisticType.COUNT_GE ){
                        if(yearts.isDataMissing( yearValue) ) {
                            yearValue = 1.0;
                        }
                        else {
                            yearValue += 1.0;
                        }
                    }
                    else if ((statisticType == TSStatisticType.DAY_OF_FIRST_GE) ||
                        (statisticType == TSStatisticType.DAY_OF_LAST_GE) ){
                        yearValue = TimeUtil.dayOfYear(date);
                        needToAnalyze = false;    
                        // Found value for the year.
                    }
                }
                else if((testType == TestType.GT) && (value > testValueDouble) ) {
                    if (statisticType == TSStatisticType.COUNT_GT ){
                        if(yearts.isDataMissing( yearValue) ) {
                            yearValue = 1.0;
                        }
                        else {
                            yearValue += 1.0;
                        }
                    }
                    else if ((statisticType == TSStatisticType.DAY_OF_FIRST_GT) ||
                        (statisticType == TSStatisticType.DAY_OF_LAST_GT) ){
                        yearValue = TimeUtil.dayOfYear( date);
                        needToAnalyze = false;    
                        // Found value for the year.
                    }
                }
                else if((testType == TestType.LE) && (value <= testValueDouble) ) {
                    if (statisticType == TSStatisticType.COUNT_LE) {
                        if(yearts.isDataMissing( yearValue) ) {
                            yearValue = 1.0;
                        }
                        else {
                            yearValue += 1.0;
                        }
                    }
                    else if ((statisticType == TSStatisticType.DAY_OF_FIRST_LE) ||
                        (statisticType == TSStatisticType.DAY_OF_LAST_LE) ){
                        yearValue = TimeUtil.dayOfYear( date);
                        needToAnalyze = false;    
                        // Found value for the year.
                    }
                }
                else if((testType == TestType.LT) && (value < testValueDouble) ) {
                    if (statisticType == TSStatisticType.COUNT_LT){
                        if(yearts.isDataMissing( yearValue) ) {
                            yearValue = 1.0;
                        }
                        else {
                            yearValue += 1.0;
                        }
                    }
                    else if ((statisticType == TSStatisticType.DAY_OF_FIRST_LT) ||
                        (statisticType == TSStatisticType.DAY_OF_LAST_LT) ){
                        yearValue = TimeUtil.dayOfYear( date);
                        needToAnalyze = false;    
                        // Found value for the year.
                    }
                }
                else if ( testType == TestType.MAX ) {
                    if(yearts.isDataMissing(extremeValue)||(value > extremeValue) ) {
                        // Set the max...
                        if ( statisticType == TSStatisticType.DAY_OF_MAX ) {
                            yearValue = TimeUtil.dayOfYear( date);
                        }
                        else if ( statisticType == TSStatisticType.MAX ) {
                            yearValue = value;
                        }
                        extremeValue = value;
                    }
                    // Need to continue analyzing period so do not set need_to_analyze to false.
                }
                else if ( testType == TestType.MIN ) {
                    if(yearts.isDataMissing(extremeValue)||(value < extremeValue) ) {
                        // Set the min...
                        if ( statisticType == TSStatisticType.DAY_OF_MIN ) {
                            yearValue = TimeUtil.dayOfYear( date);
                        }
                        else if ( statisticType == TSStatisticType.MIN ) {
                            yearValue = value;
                        }
                        extremeValue = value;
                    }
                    // Need to continue analyzing period so do not set need_to_analyze to false.
                }
                else if( testType == TestType.ACCUMULATE ) {
                    // Need to accumulate the value (for Mean or Total)
                    // Accumulate into the year_value
                    if(yearts.isDataMissing( yearValue) ) {
                        yearValue = value;
                    }
                    else {
                        yearValue += value;
                    }
                }
            }
        }
    }
    if ( ts.getAlias().length() > 0 ) {
        yearts.addToGenesis ( "Created " + statisticType + " statistic time series (TestValue=" + testValue +
            ",SearchStart=" + searchStartString + ",AllowMissingCount=" + allowMissingCount +
            ") from input: " + ts.getAlias() );
    }
    else {
        yearts.addToGenesis ( "Created " + statisticType + " statistic time series (TestValue=" + testValue +
            ",SearchStart=" + searchStartString + ",AllowMissingCount=" + allowMissingCount +
            ") from input: " + ts.getIdentifier() );
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
Set the date/time within the year to start searching.
@param searchStart date/time within the year to start searching.
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