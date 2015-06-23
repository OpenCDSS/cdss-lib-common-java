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
     * Day of centroid (equal sum(Day x Value) on each side).
     */
    DAY_OF_CENTROID ( "DayOfCentroid" ),
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
     * Exceedance probability, percent.
     */
    EXCEEDANCE_PROBABILITY ( "ExceedanceProbability" ),
	/**
	 * Value for which there is a 10% probability of exceeding the value (0 to 10% chance).
	 */
    EXCEEDANCE_PROBABILITY_10 ( "ExceedanceProbability10" ),
    /**
     * Value for which there is a 30% probability of exceeding the value (0 to 30% chance).
     */
    EXCEEDANCE_PROBABILITY_30 ( "ExceedanceProbability30" ),
    /**
     * Value for which there is a 50% probability of exceeding the value (0 to 50% chance).
     */
    EXCEEDANCE_PROBABILITY_50 ( "ExceedanceProbability50" ),
    /**
     * Value for which there is a 70% probability of exceeding the value (0 to 70% chance).
     */
    EXCEEDANCE_PROBABILITY_70 ( "ExceedanceProbability70" ),
    /**
     * Value for which there is a 90% probability of exceeding the value (0 to 90% chance).
     */
    EXCEEDANCE_PROBABILITY_90 ( "ExceedanceProbability90" ),
    /**
     * Count of values >= test value.
     */
    GE_COUNT ( "GECount" ),
    /**
     * Percent of values >= test value.
     */
    GE_PERCENT ( "GEPercent" ),
    /**
     * Count of values > test value.
     */
    GT_COUNT ( "GTCount" ),
    /**
     * Percent of values > test value.
     */
    GT_PERCENT ( "GTPercent" ),
    /**
     * Mean value in the sample.
     */
    GEOMETRIC_MEAN ( "GeometricMean" ),
    /**
     * Auto-correlation with previous interval.
     */
    LAG1_AUTO_CORRELATION ( "Lag-1AutoCorrelation" ),
    /**
     * Last value in the sample (typically the last value in an analysis period).
     */
    LAST ( "Last" ),
    /**
     * Last non-missing value in the sample.
     */
    LAST_NONMISSING ( "LastNonmissing" ),
    /**
     * Count of values <= test value.
     */
    LE_COUNT ( "LECount" ),
    /**
     * Percent of values <= test value.
     */
    LE_PERCENT ( "LEPercent" ),
    /**
     * Count of values < test value.
     */
    LT_COUNT ( "LTCount" ),
    /**
     * Percent of values < test value.
     */
    LT_PERCENT ( "LTPercent" ),
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
     * Maximum sequence length of missing values.
     */
    MISSING_SEQ_LENGTH_MAX ( "MissingSeqLengthMax" ),
    /**
     * Month of centroid (equal sum(Month x Value) on each side).
     */
    MONTH_OF_CENTROID ( "MonthOfCentroid" ),
    /**
     * Month of first value >= test value.
     */
    MONTH_OF_FIRST_GE ( "MonthOfFirstGE" ),
    /**
     * Day of first value > test value.
     */
    MONTH_OF_FIRST_GT ( "MonthOfFirstGT" ),
    /**
     * Month of first value <= test value.
     */
    MONTH_OF_FIRST_LE ( "MonthOfFirstLE" ),
    /**
     * Month of first value < test value.
     */
    MONTH_OF_FIRST_LT ( "MonthOfFirstLT" ),
    /**
     * Month of last value >= test value.
     */
    MONTH_OF_LAST_GE ( "MonthOfLastGE" ),
    /**
     * Month of last value > test value.
     */
    MONTH_OF_LAST_GT ( "MonthOfLastGT" ),
    /**
     * Month of last value <= test value.
     */
    MONTH_OF_LAST_LE ( "MonthOfLastLE" ),
    /**
     * Month of last value < test value.
     */
    MONTH_OF_LAST_LT ( "MonthOfLastLT" ),
    /**
     * Month of maximum value.
     */
    MONTH_OF_MAX ( "MonthOfMax" ),
    /**
     * Month of minimum value.
     */
    MONTH_OF_MIN ( "MonthOfMin" ),
    /**
     * Nonexceedance probability, percent.
     */
    NONEXCEEDANCE_PROBABILITY ( "NonexceedanceProbability" ),
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
     * Percent of max.
     */
    PERCENT_OF_MAX ( "PercentOfMax" ),
    /**
     * Percent of min.
     */
    PERCENT_OF_MIN ( "PercentOfMin" ),
    /**
     * Percent of mean.
     */
    PERCENT_OF_MEAN ( "PercentOfMean" ),
    /**
     * Percent of median.
     */
    PERCENT_OF_MEDIAN ( "PercentOfMedian" ),
    /**
     * Plotting position (depends on distribution and sort order imposed by other code).
     */
    PLOTTING_POSITION ( "PlottingPosition" ),
    /**
     * Rank (depends on sort order imposed by other code).
     */
    RANK ( "Rank" ),
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
     * Trend represented as ordinary least squares line of best fit.  Output statistics are
     * TrendOLS_Intercept, TrendOLS_Slope, and TrendOLS_R2.  If analyzing time series values over time,
     * the X-coordinate (time) generally is computed as year, absolute month, or absolute day.
     */
    TREND_OLS ( "TrendOLS" ),
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
    if ( name == null ) {
        return null;
    }
    TSStatisticType [] values = values();
    // Legacy conversions
    if ( name.equalsIgnoreCase("COUNT_GE") ) {
        return GE_COUNT;
    }
    else if ( name.equalsIgnoreCase("COUNT_GT") ) {
        return GT_COUNT;
    }
    else if ( name.equalsIgnoreCase("COUNT_LE") ) {
        return LE_COUNT;
    }
    else if ( name.equalsIgnoreCase("COUNT_LT") ) {
        return LT_COUNT;
    }
    // Currently supported values
    for ( TSStatisticType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}