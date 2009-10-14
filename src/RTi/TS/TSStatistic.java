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
public enum TSStatistic
{
    /**
     * Count of missing and non-missing values (total count).
     */
    COUNT("Count"),
    /**
     * Count of values >= test value.
     */
	COUNT_GE ( "CountGE" ),
	/**
	 * Count of values > test value.
	 */
	COUNT_GT ( "CountGT" ),
	/**
	 * Count of values <= test value.
	 */
	COUNT_LE ( "CountLE" ),
	/**
	 * Count of values < test value.
	 */
	COUNT_LT ( "CountLT" ),
	/**
	 * Day of first value >= test value.
	 */
	DAY_OF_FIRST_GE ( "DayOfFirstGE" ),
	/**
	 * Day of first value > test value.
	 */
	DAY_OF_FIRST_GT ( "DayOfFirstGT" ),
	/**
	 * Day of first value <= test value.
	 */
	DAY_OF_FIRST_LE ( "DayOfFirstLE" ),
	/**
	 * Day of first value < test value.
	 */
	DAY_OF_FIRST_LT ( "DayOfFirstLT" ),
	/**
	 * Day of last value >= test value.
	 */
	DAY_OF_LAST_GE ( "DayOfLastGE" ),
	/**
	 * Day of last value > test value.
	 */
	DAY_OF_LAST_GT ( "DayOfLastGT" ),
	/**
	 * Day of last value <= test value.
	 */
	DAY_OF_LAST_LE ( "DayOfLastLE" ),
	/**
	 * Day of last value < test value.
	 */
	DAY_OF_LAST_LT ( "DayOfLastLT" ),
	/**
	 * Day of maximum value.
	 */
	DAY_OF_MAX ( "DayOfMax" ),
	/**
	 * Day of minimum value.
	 */
	DAY_OF_MIN ( "DayOfMin" ),
    /**
     * Maximum of number of consecutive intervals with values below the mean.
     */
	DEFICIT_LENGTH_MAX ( "DeficitLengthMax" ),
    /**
     * Mean of number of consecutive intervals with values below the mean.
     */
    DEFICIT_LENGTH_MEAN ( "DeficitLengthMean" ),
    /**
     * Mean of number of consecutive intervals with values below the mean.
     */
    DEFICIT_LENGTH_MIN ( "DeficitLengthMin" ),
    /**
     * Maximum of (mean - value) sum when consecutive values are below the mean.
     */
    DEFICIT_MAX ( "DeficitMax" ),
    /**
     * Mean of (mean - value) sum when consecutive values are above the mean.
     */
    DEFICIT_MEAN ( "DeficitMean" ),
    /**
     * Minimum of (mean - value) sum when consecutive values are above the mean.
     */
    DEFICIT_MIN ( "DeficitMin" ),
	/**
	 * Probability of exceeding value is >= 10%.
	 */
    EXCEEDANCE_PROBABILITY_GE10 ( "ExceedanceProbabilityGE10" ),
    /**
     * Probability of exceeding value is >= 50%.
     */
    EXCEEDANCE_PROBABILITY_GE50 ( "ExceedanceProbabilityGE50" ),
    /**
     * Probability of exceeding value is >= 90%.
     */
    EXCEEDANCE_PROBABILITY_GE90 ( "ExceedanceProbabilityGE90" ),
    /**
     * Maximum value in the sample.
     */
	MAX ( "Max" ),
	/**
	 * Median value in the sample.
	 */
	MEDIAN ( "Median" ),
	/**
	 * Mean value in the sample.
	 */
	MEAN ( "Mean" ),
	/**
	 * Minimum value in the sample.
	 */
	MIN ( "Min" ),
	/**
	 * Count of missing values in the sample.
	 */
    MISSING_COUNT ( "MissingCount" ),
    /**
     * Percent of missing values in the sample.
     */
    MISSING_PERCENT ( "MissingPercent" ),
    /**
     * Count of non-missing values in the sample.
     */
    NONMISSING_COUNT ( "NonmissingCount" ),
    /**
     * Percent of non-missing values.
     */
    NONMISSING_PERCENT ( "NonmissingPercent" ),
    /**
     * For daily data: 10-year recurrence interval for lowest 7-day average flow (each year) is 7q10
     */
    NQYY ( "NqYY" ),
    /**
     * Coefficient of skew.
     */
    SKEW ( "Skew" ),
    /**
     * Standard deviation.
     */
    STD_DEV ( "StdDev" ),
    /**
     * Maximum of number of consecutive intervals with values above the mean.
     */
    SURPLUS_LENGTH_MAX ( "SurplusLengthMax" ),
    /**
     * Mean of number of consecutive intervals with values above the mean.
     */
    SURPLUS_LENGTH_MEAN ( "SurplusLengthMean" ),
    /**
     * Mean of number of consecutive intervals with values below the mean.
     */
    SURPLUS_LENGTH_MIN ( "SurplusLengthMin" ),
    /**
     * Maximum of value sum when consecutive values are above the mean.
     */
    SURPLUS_MAX ( "SurplusMax" ),
    /**
     * Mean of value sum when consecutive values are above the mean.
     */
    SURPLUS_MEAN ( "SurplusMean" ),
    /**
     * Minimum of value sum when consecutive values are above the mean.
     */
    SURPLUS_MIN ( "SurplusMin" ),
    /**
     * Total of values in the sample.
     */
	TOTAL ( "Total" ),
	/**
	 * Sample variance.
	 */
	VARIANCE ( "Variance" );
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TSStatistic(String displayName) {
        this.displayName = displayName;
    }

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
@deprecated due to the large number of statistics, choices should be determined for the specific
computational code to avoid inappropriate choices.
*/
public static List getStatisticChoicesForInterval ( int interval, String timescale )
{	List statistics = new Vector();
	if ( (interval >= TimeInterval.MONTH) || (interval == TimeInterval.UNKNOWN) ) {
		statistics.add ( COUNT_GE );
		statistics.add ( COUNT_GT );
		statistics.add ( COUNT_LE );
		statistics.add ( COUNT_LT );
		statistics.add ( DAY_OF_FIRST_GE );
		statistics.add ( DAY_OF_FIRST_GT );
		statistics.add ( DAY_OF_FIRST_LE );
		statistics.add ( DAY_OF_FIRST_LT );
		statistics.add ( DAY_OF_LAST_GE );
		statistics.add ( DAY_OF_LAST_GT );
		statistics.add ( DAY_OF_LAST_LE );
		statistics.add ( DAY_OF_LAST_LT );
		statistics.add ( DAY_OF_MAX );
		statistics.add ( DAY_OF_MIN );
		statistics.add ( MAX );
		statistics.add ( MEDIAN );
		statistics.add ( MEAN );
		statistics.add ( MIN );
		statistics.add ( TOTAL );
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
@deprecated due to the large number of statistics, choices should be determined for the specific
computational code to avoid inappropriate choices.
*/
public static List getStatisticChoicesForSimpleSample ( int interval, String timescale )
{	List statistics = new Vector();
	// TODO SAM 2007-11-05 Could add the CountLE, etc.
	//statistics.addElement ( Max );
    //statistics.addElement ( ExceedanceProbabilityGE10 );
    //statistics.addElement ( ExceedanceProbabilityGE50 );
    //statistics.addElement ( ExceedanceProbabilityGE90 );
    statistics.add ( MEAN );
    statistics.add ( MEDIAN );
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

/**
 * Return the display name for the statistic.  This is usually the same as the
 * value but using appropriate mixed case.
 * @return the display name.
 */
@Override
public String toString() {
    return displayName;
}

/**
 * Return the enumeration value given a string name (case-independent).
 * @return the enumeration value given a string name (case-independent), or null if not matched.
 */
public static TSStatistic valueOfIgnoreCase(String name)
{
    TSStatistic [] values = values();
    for ( TSStatistic t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}