package RTi.TS;

import RTi.Util.String.StringUtil;

/**
This class stores time series statistic types, which are a value or values determined
from the sample given by the time series data points.  Some statistics are general in
nature and could be applied outside of time series.  Some are specific to time series
(e.g., day of year that some condition occurs).  This enumeration only provides a list of potential
statistic types.  Computation and management of results occurs in other classes.  Code that uses
these statistics types should typically decide which statistics are supported.
*/
public enum TSStatisticType
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
     * Maximum of (mean - value) when value is below the mean.
     */
    DEFICIT_MAX ( "DeficitMax" ),
    /**
     * Mean of (mean - value) when value is below the mean.
     */
    DEFICIT_MEAN ( "DeficitMean" ),
    /**
     * Minimum of (mean - value) when value is below the mean.
     */
    DEFICIT_MIN ( "DeficitMin" ),
    /**
     * Maximum of number of sequential intervals with values below the mean.
     */
	DEFICIT_SEQ_LENGTH_MAX ( "DeficitSeqLengthMax" ),
    /**
     * Mean of number of sequential intervals with values below the mean.
     */
    DEFICIT_SEQ_LENGTH_MEAN ( "DeficitSeqLengthMean" ),
    /**
     * Mean of number of sequential intervals with values below the mean.
     */
    DEFICIT_SEQ_LENGTH_MIN ( "DeficitSeqLengthMin" ),
    /**
     * Maximum of (mean - value) sum when sequential values are below the mean.
     */
    DEFICIT_SEQ_MAX ( "DeficitSeqMax" ),
    /**
     * Mean of (mean - value) sum when sequential values are above the mean.
     */
    DEFICIT_SEQ_MEAN ( "DeficitSeqMean" ),
    /**
     * Minimum of (mean - value) sum when sequential values are above the mean.
     */
    DEFICIT_SEQ_MIN ( "DeficitSeqMin" ),
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
     * Auto-correlation with previous interval.
     */
    LAG1_AUTO_CORRELATION ( "Lag-1AutoCorrelation" ),
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
     * Maximum of of (value - mean), if a surplus.
     */
    SURPLUS_MAX ( "SurplusMax" ),
    /**
     * Mean of (value - mean), if a surplus.
     */
    SURPLUS_MEAN ( "SurplusMean" ),
    /**
     * Minimum of (value - mean), if a surplus.
     */
    SURPLUS_MIN ( "SurplusMin" ),
    /**
     * Maximum of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MAX ( "SurplusSeqLengthMax" ),
    /**
     * Mean of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MEAN ( "SurplusSeqLengthMean" ),
    /**
     * Mean of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MIN ( "SurplusSeqLengthMin" ),
    /**
     * Maximum of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MAX ( "SurplusSeqMax" ),
    /**
     * Mean of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MEAN ( "SurplusSeqMean" ),
    /**
     * Minimum of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MIN ( "SurplusSeqMin" ),
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
    private TSStatisticType(String displayName) {
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

// TODO SAM 2009-07-27 evaluate using enumeration, etc. to have properties for statistic or add this method
// to specific calculation classes.
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
public static TSStatisticType valueOfIgnoreCase(String name)
{
    TSStatisticType [] values = values();
    for ( TSStatisticType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}