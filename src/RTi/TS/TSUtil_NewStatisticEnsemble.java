// TSUtil_NewStatisticEnsemble - compute an ensemble with time series that each contain a statistic computed from the input sample time series.

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

import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
Compute an ensemble with time series that each contain a statistic computed from the input sample time series.
This is intended to compute multiple output time series, for example CountLE(A), CountLE(B), etc.  Computing a "simple"
statistic from an ensemble can be done using the TSUtil_NewStatisticTimeSeriesFromEnsemble class.
*/
public class TSUtil_NewStatisticEnsemble
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
List of time series to analyze.
*/
private List<TS> __tslist = null;

/**
New ensemble identifier to assign.
*/
private String __newEnsembleID = null;

/**
New ensemble name.
*/
private String __newEnsembleName = null;

/**
Alias for new time series.
*/
private String __alias = null;

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
Test values used when analyzing the statistic.
*/
private double [] __testValues = null;

/**
Number of missing allowed to compute sample (default is no limit).
*/
private int __allowMissingCount = -1;

/**
Minimum required sample size (default is no limit).
*/
private int __minimumSampleSize = -1;

/**
Construct the analysis object with required input.  Values will be checked for validity.
Execute the newStatisticYearTS() method to perform the analysis.
@param tslist list of time series to analyze
@param newEnsembleID new ensemble identifier
@param newEnsembleName new ensemble name
@param alias the alias to be assigned to each created time series
@param newTSID the new time series identifier to be assigned to each created time series
@param statisticType the statistic type for the output time series
@param testValues test value array (e.g., threshold value)
@param allowMissingCount the number of values allowed to be missing in the sample
@param minimumSampleSize the minimum sample size to allow to compute the statistic
@param analysisStart Starting date/time for analysis, in precision of the original data
@param analysisEnd Ending date for analysis, in precision of the original data
*/
public TSUtil_NewStatisticEnsemble ( List<TS> tslist, String newEnsembleID, String newEnsembleName,
    String alias, String newTSID, TSStatisticType statisticType, double [] testValues,
    Integer allowMissingCount, Integer minimumSampleSize,
    DateTime analysisStart, DateTime analysisEnd )
{   String routine = getClass().getName();
    String message;

    if ( tslist == null ) {
        // Nothing to do...
        message = "Time series list is null - cannot calculate statistic ensemble.";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    else if ( tslist.size() == 0 ) {
        // Nothing to do...
        message = "Time series list is empty - cannot calculate statistic ensemble.";
        Message.printWarning ( 3, routine, message );
        throw new IllegalArgumentException ( message );
    }
    setTimeSeriesList ( tslist );
    setNewEnsembleID ( newEnsembleID );
    setNewEnsembleName ( newEnsembleName );
    setAlias ( alias );
    setNewTSID ( newTSID );
    try {
        // TODO SAM 
    }
    catch ( Exception e ) {
        throw new IllegalArgumentException (
            "New time series identifier \"" + newTSID + "\" is invalid (" + e + ")." );
    }
    setStatisticType ( statisticType );
    setTestValues ( testValues );
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
    if ( !isStatisticSupported(statisticType) ) {
        throw new IllegalArgumentException ( "Statistic \"" + statisticType + "\" is not supported.");
    }
}

/**
Calculate the number of values in the sample that meet the statistic criteria
@param countNonMissing number of nonmissing values in the start of the sampleData array
@param sampleData the sample data array to analyze
@param testValue the test value to use when evaluating the statistic
@param testType the type of test when comparing a sample value and test value
@return the count of sample values that meet the statistic criteria
*/
private int calculateCount ( int countNonMissing, double [] sampleData, double testValue, TestType testType )
{
    int count = 0;
    for ( int i = 0; i < countNonMissing; i++ ) {
        if ( (testType == TestType.GE) && (sampleData[i] >= testValue) ) {
            ++count;
        }
        else if ( (testType == TestType.GT) && (sampleData[i] > testValue) ) {
            ++count;
        }
        else if ( (testType == TestType.LE) && (sampleData[i] <= testValue) ) {
            ++count;
        }
        else if ( (testType == TestType.LT) && (sampleData[i] < testValue) ) {
            ++count;
        }
    }
    return count;
}

/**
Process a list of input time series to create output statistic time series.
@param tslist time series to analyze
@param stattsList list of output statistic time series, previously constructed and memory allocated
@param statisticType statistic to calculate
@param testValues one or more numbers to test against for some statistics (e.g., COUNT_LE).
@param analysisStart Start of the analysis (precision matching tslist)
@param analysisEnd End of the analysis (precision matching tslist)
@param allowMissingCount the number of missing values allowed in input and still compute the statistic.
@param minimumSampleSize the minimum number of values required in input to compute the statistic.
*/
private void calculateStatistic (
    List<TS> tslist, List<TS> stattsList, TSStatisticType statisticType, double [] testValues,
    DateTime analysisStart, DateTime analysisEnd, int allowMissingCount, int minimumSampleSize )
{   String routine = getClass().getName() + ".calculateStatistic";
    int size = tslist.size();
    Message.printStatus(2,routine,"Have " + size + " time series to analyze.");
    TS ts; // Time series in the ensemble
    // To improve performance, initialize an array of time series...
    TS [] ts_array = tslist.toArray(new TS[tslist.size()]);
    if ( ts_array.length == 0 ) {
        throw new RuntimeException ( "Ensemble has 0 traces - cannot analyze statistic.");
    }
    DateTime date = new DateTime(analysisStart);
    int i; // Index for time series in loop.
    double value; // Value from the input time series
    double [] sampleData = new double[size]; // One value from each ensemble
    int countNonMissing; // Count of sampleData that are non-missing
    int countMissing; // Count of sampleData that are missing
    Message.printStatus(2, routine, "Analyzing time series list for period " + analysisStart + " to " +
        analysisEnd );
    // Loop through the analysis period
    int intervalBase = ts_array[0].getDataIntervalBase();
    int intervalMult = ts_array[0].getDataIntervalMult();
    int statCount;
    boolean statisticIsCount = isStatisticCount(statisticType);
    boolean statisticIsPercent = isStatisticPercent(statisticType);
    TestType testType = getStatisticTestType(statisticType);
    for ( ; date.lessThanOrEqualTo(analysisEnd); date.addInterval(intervalBase, intervalMult) ) {
        // Loop through the time series for the date/time...
        countNonMissing = 0;
        countMissing = 0;
        for ( i = 0; i < size; i++ ) {
            ts = ts_array[i];
            value = ts.getDataValue(date);
            if ( ts.isDataMissing(value) ) {
                // Ignore missing data...
                ++countMissing;
                continue;
            }
            else {
                // Save non-missing value in the sample...
                sampleData[countNonMissing++] = value;
            }
        }
        // Now analyze the statistic for each output time series
        TS statts;
        for ( i = 0; i < testValues.length; i++ ) {
            statts = stattsList.get(i);
            // Now compute the statistic from the sample if missing values are not a problem.
            if ( (allowMissingCount >= 0) && (countMissing > allowMissingCount) ) {
                // Too many missing values to compute statistic
                Message.printStatus ( 2, routine, "Not computing time series statistic at " + date +
                    " because number of missing values " + countMissing + " is > allowed (" + allowMissingCount + ").");
                continue;
            }
            if ( (minimumSampleSize >= 0) && (countNonMissing < minimumSampleSize) ) {
                // Sample size too small to compute statistic
                Message.printStatus ( 2, routine, "Not computing time series statistic at " + date +
                    " because sample size " + countNonMissing + " is < minimum required (" + minimumSampleSize + ").");
                continue;
            }
            // Else have enough data so compute the statistic.
            statCount = calculateCount(countNonMissing,sampleData,testValues[i],testType);
            if ( statisticIsCount ) {
                statts.setDataValue ( date, (double)statCount );
            }
            else if ( statisticIsPercent && (countNonMissing > 0) ) {
                // Default is percent of non-missing stations
                // TODO SAM 2012-07-13 Evaluate whether should be percent of total stations?
                statts.setDataValue ( date, 100.0*(double)statCount/(double)countNonMissing );
            }
        }
    }
}

/**
Return the alias for new time series.
@return the alias for new time series.
*/
private String getAlias ()
{
    return __alias;
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
Return the minimum sample size allowed to compute the statistic.
@return the minimum sample size allowed to compute the statistic.
*/
private Integer getMinimumSampleSize ()
{
    return __minimumSampleSize;
}

/**
Return the new ensemble identifier
@return the new ensemble identifier
*/
private String getNewEnsembleID ()
{
    return __newEnsembleID;
}

/**
Return the new ensemble name
@return the new ensemble name
*/
private String getNewEnsembleName ()
{
    return __newEnsembleName;
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
Return a list of statistic choices.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.
*/
public static List<TSStatisticType> getStatisticChoices ()
{   List<TSStatisticType> statistics = new ArrayList<TSStatisticType>();
    // Add in alphabetical order
    statistics.add ( TSStatisticType.GE_COUNT );
    statistics.add ( TSStatisticType.GE_PERCENT );
    statistics.add ( TSStatisticType.GT_COUNT );
    statistics.add ( TSStatisticType.GT_PERCENT );
    statistics.add ( TSStatisticType.LE_COUNT );
    statistics.add ( TSStatisticType.LE_PERCENT );
    statistics.add ( TSStatisticType.LT_COUNT );
    statistics.add ( TSStatisticType.LT_PERCENT );
    // TODO SAM 2012-07-12 Should (non)exceedance probability be included?
    return statistics;
}

/**
Return a list of statistic choices.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.
*/
public static List<String> getStatisticChoicesAsStrings ()
{
    List<TSStatisticType> choices = getStatisticChoices();
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
        (statisticType == TSStatisticType.GE_PERCENT) ) {
        return TestType.GE;
    }
    else if ( (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.GT_PERCENT) ) {
        return TestType.GT;
    }
    else if ( (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LE_PERCENT) ) {
        return TestType.LE;
    }
    else if ( (statisticType == TSStatisticType.LT_COUNT) ||
        (statisticType == TSStatisticType.LT_PERCENT) ) {
        return TestType.LT;
    }
    else {
        return TestType.NOT_USED;
    }
}

/**
Determine the statistic time series data units.
@param statisticType a statistic type to check.
@return the data units for the time series, given the statistic.
*/
private String getStatisticTimeSeriesDataUnits ( TSStatisticType statisticType )
{
    if ( isStatisticPercent(statisticType) ) {
        return "Percent";
    }
    else if ( isStatisticCount(statisticType) ) {
        return "Count";
    }
    else return "";
}

/**
Determine the statistic time series description.
@param statisticType a statistic type to check.
@param testType test type that is being performed.
@param testValue the test value to be checked.
@param statisticIsCount if true, then the statistic is a count
@param statisticIsPercent if true, the statistic is a percent
@return the description for the time series, given the statistic and test types.
*/
private String getStatisticTimeSeriesDescription ( TSStatisticType statisticType, TestType testType,
    Double testValue )
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
    boolean statisticIsCount = isStatisticCount(statisticType);
    boolean statisticIsPercent = isStatisticPercent(statisticType);
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
    else if ( statisticIsPercent ) {
        if ( statisticType == TSStatisticType.MISSING_COUNT ) {
            desc = "Percent of missing values";
        }
        else if ( statisticType == TSStatisticType.NONMISSING_COUNT ) {
            desc = "Percent of nonmissing values";
        }
        else {
            desc = "Percent of values " + testString + " " + testValueString;
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
Return the test values used to calculate some statistics.
@return the test values used to calculate some statistics.
*/
private double [] getTestValues ()
{
    return __testValues;
}

/**
Return the list of time series being analyzed.
@return the list of time series being analyzed.
*/
public List<TS> getTimeSeriesList ()
{
    return __tslist;
}

/**
Indicate whether the statistic is a count.
@param statisticType a statistic type to check.
*/
private boolean isStatisticCount ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.GE_COUNT) ||
        (statisticType == TSStatisticType.GT_COUNT) ||
        (statisticType == TSStatisticType.LE_COUNT) ||
        (statisticType == TSStatisticType.LT_COUNT) ) {
        return true;
    }
    else {
        return false;
    }
}

/**
Indicate whether the statistic is a percent.
@param statisticType a statistic type to check.
*/
private boolean isStatisticPercent ( TSStatisticType statisticType )
{
    if ( (statisticType == TSStatisticType.GE_PERCENT) ||
        (statisticType == TSStatisticType.GT_PERCENT) ||
        (statisticType == TSStatisticType.LE_PERCENT) ||
        (statisticType == TSStatisticType.LT_PERCENT) ) {
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
public static boolean isStatisticSupported ( TSStatisticType statisticType )
{
    List<TSStatisticType> choices = getStatisticChoices ();
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
    if ((statisticType == TSStatisticType.GE_COUNT) ||
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
Create an ensemble that contains time series statistics in each data value (e.g.,
percent greater than a threshold value).
@param createData if true, calculate the data value array; if false, only assign metadata
@return the statistics ensemble
*/
public TSEnsemble newStatisticEnsemble ( boolean createData )
{   String routine = getClass().getName() + ".newStatisticEnsemble";
    int dl = 10;

    // Get the data needed for the analysis - originally provided in the constructor
    
    List<TS> tslist = getTimeSeriesList();
    String newEnsembleID = getNewEnsembleID();
    String newEnsembleName = getNewEnsembleName();
    String newTSID = getNewTSID();
    TSStatisticType statisticType = getStatisticType();
    double [] testValues = getTestValues();
    Integer allowMissingCount = getAllowMissingCount();
    Integer minimumSampleSize = getMinimumSampleSize();
    DateTime analysisStart = getAnalysisStart();
    DateTime analysisEnd = getAnalysisEnd();
    
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "Trying to create statistic ensemble for " + tslist.size() +
        " input time series." );
    }

    // Get valid dates to use for the output time series because the ones passed in may have been null.
    // Use the overlapping period of all the time series

    if ( (analysisStart != null) && (analysisEnd != null) ) {
        // Create a local copy to protect from passed parameters
        analysisStart = new DateTime ( analysisStart );
        analysisEnd = new DateTime ( analysisEnd );
    }
    else {
        // Get the analysis start and end from the overall period
        TSLimits validDates = null;
        try {
            validDates = TSUtil.getPeriodFromTS( tslist, TSUtil.MAX_POR );
        }
        catch ( Exception e ) {
            throw new RuntimeException ( "Error getting period from input time series (" + e + ")." );
        }
        analysisStart = new DateTime ( validDates.getDate1() );
        analysisEnd = new DateTime ( validDates.getDate2() );
    }

    // Create a statistic time series for each value being processed
    // Also create an ensemble and add the individual time series to the ensemble, if requested

    List<TS> stattsList = new ArrayList<TS>();
    for ( int i = 0; i < testValues.length; i++ ) {
        // Create the time series identifier using the newTSID, but may need to expand for the statistic
        String tsid = newTSID;
        TS statts = null;
        try {
            statts = TSUtil.newTimeSeries(tsid, true);
            statts.setIdentifier(tsid);
        }
        catch ( Exception e ) {
            throw new RuntimeException ( "Error creating statistic time series using TSID=\"" +
                tsid + "\" (" + e + ")." );
        }
        statts.addToGenesis ( "Initialized statistic time series using TSID=\"" + statts.getIdentifierString() + "\"" );
        // Set the period to that determined above
        statts.setDate1 ( analysisStart );
        statts.setDate1Original ( analysisStart );
        statts.setDate2 ( analysisEnd );
        statts.setDate2Original ( analysisEnd );
        if ( createData ) {
            statts.allocateDataSpace();
        }
        // Set the units and description based on the statistic
        statts.setDataUnits ( getStatisticTimeSeriesDataUnits(statisticType) );
        statts.setDataUnitsOriginal ( statts.getDataUnits() );
        statts.setDescription( getStatisticTimeSeriesDescription(statisticType,
            getStatisticTestType(statisticType), testValues[i]));
        stattsList.add ( statts );
    }
    
    // Always create an ensemble even if the ID is empty.  Calling code can ignore the ensemble and use the
    // list of time series directly if needed.
    
    TSEnsemble ensemble = null;
    if ( newEnsembleID == null ) {
        newEnsembleID = "";
    }
    if ( newEnsembleName == null ) {
        newEnsembleName = "";
    }
    ensemble = new TSEnsemble(newEnsembleID, newEnsembleName, stattsList);
    
    if ( !createData ) {
        return ensemble;
    }

    // Calculate the statistic time series...
    calculateStatistic (
            tslist, stattsList, statisticType, testValues,
            analysisStart, analysisEnd, allowMissingCount, minimumSampleSize );

    return ensemble;
}

/**
Set the alias for new time series.
@param alias the alias for new time series
*/
private void setAlias ( String alias )
{
    __alias = alias;
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
Set the minimum sample size.
@param minimumSampleSize the minimum sample size.
*/
private void setMinimumSampleSize ( int minimumSampleSize )
{
    __minimumSampleSize = minimumSampleSize;
}

/**
Set the new ensemble identifier.
@param newEnsembleID the new ensemble identifier.
*/
private void setNewEnsembleID ( String newEnsembleID )
{
    __newEnsembleID = newEnsembleID;
}

/**
Set the new ensemble name.
@param newEnsembleName the new ensemble name.
*/
private void setNewEnsembleName ( String newEnsembleName )
{
    __newEnsembleName = newEnsembleName;
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
Set the statistic type.
@param statisticType statistic type to calculate.
*/
private void setStatisticType ( TSStatisticType statisticType )
{
    __statisticType = statisticType;
}

/**
Set the test values used for statistics.
@param testValue the test values used with statistics.
*/
private void setTestValues ( double [] testValues )
{
    __testValues = testValues;
}

/**
Set the time series list being analyzed.
@param ts time series list being analyzed.
*/
private void setTimeSeriesList ( List<TS> tslist )
{
    __tslist = tslist;
}

}
