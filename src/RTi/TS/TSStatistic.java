package RTi.TS;

import java.util.List;
import java.util.Vector;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;

/**
This class stores a time series statistic, which is a value or values determined
from the sample given by the time series data points.  Some statistics are general in
nature and could be applied outside of time series.  Some are specific to time series
(e.g., day of year that some condition occurs).
*/
public class TSStatistic
{

/**
Statistics that are available for analysis.
*/
public final static String
    Count = "Count", // Count of missing and non-missing values
	CountGE = "CountGE", // Count of values >= test
	CountGT = "CountGT", // Count of values > test
	CountLE = "CountLE", // Count of values <= test
	CountLT = "CountLT", // Count of values < test
	DayOfFirstGE = "DayOfFirstGE", // Day of first value >= test
	DayOfFirstGT = "DayOfFirstGT", // Day of first value > test
	DayOfFirstLE = "DayOfFirstLE", // Day of first value <= test
	DayOfFirstLT = "DayOfFirstLT", // Day of first value < test
	DayOfLastGE = "DayOfLastGE", // Day of last value >= test
	DayOfLastGT = "DayOfLastGT", // Day of last value > test
	DayOfLastLE = "DayOfLastLE", // Day of last value <= test
	DayOfLastLT = "DayOfLastLT", // Day of last value < test
	DayOfMax = "DayOfMax", // Day of maximum value
	DayOfMin = "DayOfMin", // Day of minimum value
    ExceedanceProbabilityGE10 = "ExceedanceProbabilityGE10", // Probability of exceeding value is >= 10%
    ExceedanceProbabilityGE50 = "ExceedanceProbabilityGE50", // Probability of exceeding value is >= 50%
    ExceedanceProbabilityGE90 = "ExceedanceProbabilityGE90", // Probability of exceeding value is >= 90%
	Max = "Max", // Maximum value in the sample
	Median = "Median", // Median value in the sample
	Mean = "Mean", // Mean value in the sample
	Min = "Min", // Minimum value in the sample
    MissingCount = "MissingCount", // Count of missing values
    MissingPercent = "MissingPercent", // Percent of missing values
    NonmissingCount = "NonmissingCount", // Count of non-missing values
    NonmissingPercent = "NonmissingPercent", // Percent of non-missing values
    NqYY = "NqYY", // for daily data: 10-year recurrence interval for lowest 7-day average flow (each year)
	Total = "Total"; // Total of values in the sample

// TODO SAM 2005-09-30
// Need to add:
//
// Sum/Total
// Mean
// Median
// StdDev
// Var
// Delta (Prev/Next?)
// DataCoveragePercent
// MissingPercent
// Percentile01 (.01), etc.
//
// Need to store in time series history as analyzing?

// TODO SAM 2005-09-12
// Need to figure out how to handle irregular data.
/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.
*/
public static List getStatisticChoicesForInterval ( int interval, String timescale )
{	List statistics = new Vector();
	if ( (interval >= TimeInterval.MONTH) || (interval == TimeInterval.UNKNOWN) ) {
		statistics.add ( CountGE );
		statistics.add ( CountGT );
		statistics.add ( CountLE );
		statistics.add ( CountLT );
		statistics.add ( DayOfFirstGE );
		statistics.add ( DayOfFirstGT );
		statistics.add ( DayOfFirstLE );
		statistics.add ( DayOfFirstLT );
		statistics.add ( DayOfLastGE );
		statistics.add ( DayOfLastGT );
		statistics.add ( DayOfLastLE );
		statistics.add ( DayOfLastLT );
		statistics.add ( DayOfMax );
		statistics.add ( DayOfMin );
		statistics.add ( Max );
		statistics.add ( Median );
		statistics.add ( Mean );
		statistics.add ( Min );
		statistics.add ( Total );
	}
	return statistics;
}

// TODO SAM 2009-07-27 More robust to move method like the following to specific computation classes so
// that only appropriate statistics are listed for users
/**
Return a list of statistic choices for the requested interval and scale, for a simple
sample.  For example, if all Jan 1 daily values are in the sample, the statistics would
be those that can be determined for the sample.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.
*/
public static List getStatisticChoicesForSimpleSample ( int interval, String timescale )
{	List statistics = new Vector();
	// TODO SAM 2007-11-05 Could add the CountLE, etc.
	//statistics.addElement ( Max );
    //statistics.addElement ( ExceedanceProbabilityGE10 );
    //statistics.addElement ( ExceedanceProbabilityGE50 );
    //statistics.addElement ( ExceedanceProbabilityGE90 );
    statistics.add ( Mean );
    statistics.add ( Median );
	//statistics.addElement ( Min );
	return statistics;
}

// TODO SAM 2009-07-27 evaluate using enumeration, etc. to have properties for statistic
/**
Return the statistic data type as double, integer, etc., to facilitate handling by other code.
@param statistic name of statistic
@return the statistic data type.
*/
public static Class getStatisticDataType ( String statistic )
{
    if ( (StringUtil.indexOfIgnoreCase(statistic, "Count", 0) >= 0) ||
        StringUtil.startsWithIgnoreCase(statistic,"Day") ) {
        return Integer.class;
    }
    else {
        return Double.class;
    }
}

}