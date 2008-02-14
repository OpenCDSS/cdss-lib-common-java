package RTi.TS;

import java.util.Vector;

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
	CountGE = "CountGE",		// Count of values >= test
	CountGT = "CountGT",		// Count of values > test
	CountLE = "CountLE",		// Count of values <= test
	CountLT = "CountLT",		// Count of values < test
	DayOfFirstGE = "DayOfFirstGE",	// Day of first value >= test
	DayOfFirstGT = "DayOfFirstGT",	// Day of first value > test
	DayOfFirstLE = "DayOfFirstLE",	// Day of first value <= test
	DayOfFirstLT = "DayOfFirstLT",	// Day of first value < test
	DayOfLastGE = "DayOfLastGE",	// Day of last value >= test
	DayOfLastGT = "DayOfLastGT",	// Day of last value > test
	DayOfLastLE = "DayOfLastLE",	// Day of last value <= test
	DayOfLastLT = "DayOfLastLT",	// Day of last value < test
	DayOfMax = "DayOfMax",		// Day of maximum value
	DayOfMin = "DayOfMin",		// Day of minimum value
    ExceedanceProbabilityGE10 = "ExceedanceProbabilityGE10", // Probability of exceeding value is >= 10%
    ExceedanceProbabilityGE50 = "ExceedanceProbabilityGE50", // Probability of exceeding value is >= 10%
    ExceedanceProbabilityGE90 = "ExceedanceProbabilityGE90", // Probability of exceeding value is >= 10%
	Max = "Max",			// Maximum value in the sample
	Mean = "Mean",			// Mean value in the sample
	Min = "Min",			// Minimum value in the sample
	Total = "Total";       // Total value in the sample

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

/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get
all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.
@deprecated Use getStatisticChoicesForInterval
*/
public static Vector getStatisticChoices ( int interval, String timescale )
{	return getStatisticChoicesForInterval ( interval, timescale );
}

// TODO SAM 2005-09-12
// Need to figure out how to handle irregular data.
/**
Return a list of statistic choices for the requested interval and scale.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get
all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.
*/
public static Vector getStatisticChoicesForInterval ( int interval, String timescale )
{	Vector statistics = new Vector();
	if ( (interval >= TimeInterval.MONTH) || (interval == TimeInterval.UNKNOWN) ) {
		statistics.addElement ( CountGE );
		statistics.addElement ( CountGT );
		statistics.addElement ( CountLE );
		statistics.addElement ( CountLT );
		statistics.addElement ( DayOfFirstGE );
		statistics.addElement ( DayOfFirstGT );
		statistics.addElement ( DayOfFirstLE );
		statistics.addElement ( DayOfFirstLT );
		statistics.addElement ( DayOfLastGE );
		statistics.addElement ( DayOfLastGT );
		statistics.addElement ( DayOfLastLE );
		statistics.addElement ( DayOfLastLT );
		statistics.addElement ( DayOfMax );
		statistics.addElement ( DayOfMin );
		statistics.addElement ( Max );
		statistics.addElement ( Mean );
		statistics.addElement ( Min );
		statistics.addElement ( Total );
	}
	return statistics;
}

/**
Return a list of statistic choices for the requested interval and scale, for a simple
sample.  For example, if all Jan 1 daily values are in the sample, the statistics would
be those that can be determined for the sample.
These strings are suitable for listing in a user interface.  The statistics are
listed in ascending alphabetical order.  Parameters can be used to limit the
choices (these features will be phased in over time as statistics are added).
@param interval TimeInterval.DAY, etc., indicating the interval of data for the
statistic (e.g., average value for the year).  Pass TimeInterval.UNKNOWN to get
all choices.
@param timescale MeasTimeScale.ACCM, etc., indicating whether the statistic is
expected on accumulated, mean, instantaneous data.  Pass null to get all choices.
*/
public static Vector getStatisticChoicesForSimpleSample ( int interval, String timescale )
{	Vector statistics = new Vector();
	// TODO SAM 2007-11-05 Could add the CountLE, etc.
	//statistics.addElement ( Max );
    //statistics.addElement ( ExceedanceProbabilityGE10 );
    //statistics.addElement ( ExceedanceProbabilityGE50 );
    //statistics.addElement ( ExceedanceProbabilityGE90 );
	statistics.addElement ( Mean );
	//statistics.addElement ( Min );
	return statistics;
}

} // End of TSStatistic
