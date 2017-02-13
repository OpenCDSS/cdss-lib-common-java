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
    COUNT("Count", "Count", "Count of missing and nonmissing values in a sample."),
    /**
     * Day of centroid (equal sum(Day*Value) on each side).
     */
    DAY_OF_CENTROID ( "DayOfCentroid", "DayOfCentroid", "Day in time series where sum(day*value) is the same on each side of the day." ),
	/**
	 * Day of first value >= test value.
	 */
	DAY_OF_FIRST_GE ( "DayOfFirstGE", "DayOfFirstGE", "Day in time series for first value >= a specified value." ),
	/**
	 * Day of first value > test value.
	 */
	DAY_OF_FIRST_GT ( "DayOfFirstGT", "DayOfFirstGT", "Day in time series for first value > a specified value." ),
	/**
	 * Day of first value <= test value.
	 */
	DAY_OF_FIRST_LE ( "DayOfFirstLE", "DayOfFirstLE", "Day in time series for first value <= a specified value." ),
	/**
	 * Day of first value < test value.
	 */
	DAY_OF_FIRST_LT ( "DayOfFirstLT", "DayOfFirstLT", "Day in time series for first value < a specified value." ),
	/**
	 * Day of last value >= test value.
	 */
	DAY_OF_LAST_GE ( "DayOfLastGE", "DayOfLastGE", "Day in time series for last value >= a specified value." ),
	/**
	 * Day of last value > test value.
	 */
	DAY_OF_LAST_GT ( "DayOfLastGT", "DayOfLastGT", "Day in time series for last value > a specified value." ),
	/**
	 * Day of last value <= test value.
	 */
	DAY_OF_LAST_LE ( "DayOfLastLE", "DayOfLastLE", "Day in time series for last value <= a specified value." ),
	/**
	 * Day of last value < test value.
	 */
	DAY_OF_LAST_LT ( "DayOfLastLT", "DayOfLastLT", "Day in time series for last value < a specified value." ),
	/**
	 * Day of maximum value.
	 */
	DAY_OF_MAX ( "DayOfMax", "DayOfMax", "Day in time series for the maximum value (Max)." ),
	/**
	 * Day of minimum value.
	 */
	DAY_OF_MIN ( "DayOfMin", "DayOfMin", "Day in time series for the minimum value (Min)." ),
    /**
     * Maximum of (mean - value) when value is below the mean.
     */
    DEFICIT_MAX ( "DeficitMax", "DeficitMax", "Maximum of (Mean - value) when value is below the mean." ),
    /**
     * Mean of (mean - value) when value is below the mean.
     */
    DEFICIT_MEAN ( "DeficitMean", "DeficitMean", "Mean of (Mean - value) when value is below the mean." ),
    /**
     * Minimum of (mean - value) when value is below the mean.
     */
    DEFICIT_MIN ( "DeficitMin", "DeficitMin", "Minimum of (Mean = value) when value is below the mean" ),
    /**
     * Maximum of number of sequential intervals with values below the mean.
     */
	DEFICIT_SEQ_LENGTH_MAX ( "DeficitSeqLengthMax", "DeficitSeqLengthMax", "Maximum of number of sequential intervals with values below the mean." ),
    /**
     * Mean of number of sequential intervals with values below the mean.
     */
    DEFICIT_SEQ_LENGTH_MEAN ( "DeficitSeqLengthMean", "DeficitSeqLengthMean", "Mean of number of sequential intervals with values below the mean." ),
    /**
     * Mean of number of sequential intervals with values below the mean.
     */
    DEFICIT_SEQ_LENGTH_MIN ( "DeficitSeqLengthMin", "DeficitSeqLengthMin", "Mean of number of sequential intervals with values below the mean." ),
    /**
     * Maximum of (mean - value) sum when sequential values are below the mean.
     */
    DEFICIT_SEQ_MAX ( "DeficitSeqMax", "DeficitSeqMax", "Maximum of (Mean - value) sum when sequential values are below the mean." ),
    /**
     * Mean of (mean - value) sum when sequential values are above the mean.
     */
    DEFICIT_SEQ_MEAN ( "DeficitSeqMean", "DeficitSeqMean", "Mean of (Mean - value) sum when sequential values are above the mean." ),
    /**
     * Minimum of (mean - value) sum when sequential values are above the mean.
     */
    DEFICIT_SEQ_MIN ( "DeficitSeqMin", "DeficitSeqMin", "Minimum of (mean - value) sum when sequential values are above the mean." ),
    /**
     * Exceedance probability, may be percent or fraction.
     */
    EP ( "EP", "Exceedance Probability", "Exceedance probability, may be percent or fraction." ),
    /**
     * Exceedance probability, may be percent or fraction.
     */
    EXCEEDANCE_PROBABILITY ( "ExceedanceProbability", "ExceedanceProbability", "Exceedance probability, may be percent or fraction." ),
	/**
	 * Value for which there is a 10% probability of exceeding the value (0 to 10% chance).
	 */
    EXCEEDANCE_PROBABILITY_10 ( "ExceedanceProbability10", "ExceedanceProbability10", "Value for which there is a 10% probability of exceeding the value (0 to 10% chance)." ),
    /**
     * Value for which there is a 30% probability of exceeding the value (0 to 30% chance).
     */
    EXCEEDANCE_PROBABILITY_30 ( "ExceedanceProbability30", "ExceedanceProbability30", "Value for which there is a 30% probability of exceeding the value (0 to 30% chance)." ),
    /**
     * Value for which there is a 50% probability of exceeding the value (0 to 50% chance).
     */
    EXCEEDANCE_PROBABILITY_50 ( "ExceedanceProbability50", "ExceedanceProbability50", "Value for which there is a 50% probability of exceeding the value (0 to 50% chance)." ),
    /**
     * Value for which there is a 70% probability of exceeding the value (0 to 70% chance).
     */
    EXCEEDANCE_PROBABILITY_70 ( "ExceedanceProbability70", "ExceedanceProbability70", "Value for which there is a 70% probability of exceeding the value (0 to 70% chance)." ),
    /**
     * Value for which there is a 90% probability of exceeding the value (0 to 90% chance).
     */
    EXCEEDANCE_PROBABILITY_90 ( "ExceedanceProbability90", "ExceedanceProbability90", "Value for which there is a 90% probability of exceeding the value (0 to 90% chance)." ),
    /**
     * Count of values >= test value.
     */
    GE_COUNT ( "GECount", "GECount", "Count of values >= test value." ),
    /**
     * Percent of values >= test value.
     */
    GE_PERCENT ( "GEPercent", "GEPercent", "Percent of values >= test value." ),
    /**
     * Count of values > test value.
     */
    GT_COUNT ( "GTCount", "GTCount", "Count of values > test value." ),
    /**
     * Percent of values > test value.
     */
    GT_PERCENT ( "GTPercent", "GTPercent", "Percent of values > test value." ),
    /**
     * Geometric mean of sample values.
     */
    GEOMETRIC_MEAN ( "GeometricMean", "Geometric Mean", "Geometric mean of sample values." ),
    /**
     * Auto-correlation with previous interval's value.
     */
    LAG1_AUTO_CORRELATION ( "Lag-1AutoCorrelation", "Lag-1AutoCorrelation", "Auto-correlation with previous interval's value." ),
    /**
     * Last value in the sample (typically the last value in an analysis period).
     */
    LAST ( "Last", "Last", "Last value in the sample (typically the last value in an analysis period)." ),
    /**
     * Last non-missing value in the sample.
     */
    LAST_NONMISSING ( "LastNonmissing", "LastNonmissing", "Last non-missing value in the sample." ),
    /**
     * Count of values <= test value.
     */
    LE_COUNT ( "LECount", "LECount", "Count of values <= test value." ),
    /**
     * Percent of values <= test value.
     */
    LE_PERCENT ( "LEPercent", "LEPercent", "Percent of values <= test value." ),
    /**
     * Count of values < test value.
     */
    LT_COUNT ( "LTCount", "LTCount", "Count of values < test value." ),
    /**
     * Percent of values < test value.
     */
    LT_PERCENT ( "LTPercent", "LTPercent", "Percent of values < test value." ),
    /**
     * Maximum value in the sample.
     */
	MAX ( "Max", "Maximum", "Maximum value in the sample." ),
	/**
	 * Median value in the sample.
	 */
	MEDIAN ( "Median", "Median", "Median value in the sample." ),
	/**
	 * Mean value in the sample.
	 */
	MEAN ( "Mean", "Mean", "Mean value in the sample." ),
	/**
	 * Minimum value in the sample.
	 */
	MIN ( "Min", "Min", "Minimum value in the sample." ),
	/**
	 * Count of missing values in the sample.
	 */
    MISSING_COUNT ( "MissingCount", "MissingCount", "Count of missing values in the sample." ),
    /**
     * Percent of missing values in the sample.
     */
    MISSING_PERCENT ( "MissingPercent", "MissingPercent", "Percent of missing values in the sample." ),
    /**
     * Maximum sequence length of missing values.
     */
    MISSING_SEQ_LENGTH_MAX ( "MissingSeqLengthMax", "MissingSeqLengthMax", "Maximum sequence length of missing values." ),
    /**
     * Month of centroid (equal sum(Month x Value) on each side).
     */
    MONTH_OF_CENTROID ( "MonthOfCentroid", "MonthOfCentroid", "Month in time series where sum(month*value) is the same on each side of the month." ),
    /**
     * Month of first value >= test value.
     */
    MONTH_OF_FIRST_GE ( "MonthOfFirstGE", "MonthOfFirstGE", "Month in time series for first value >= a specified value." ),
    /**
     * Day of first value > test value.
     */
    MONTH_OF_FIRST_GT ( "MonthOfFirstGT", "MonthOfFirstGT", "Month in time series for first value > a specified value." ),
    /**
     * Month of first value <= test value.
     */
    MONTH_OF_FIRST_LE ( "MonthOfFirstLE", "MonthOfFirstLE", "Month in time series for first value <= a specified value." ),
    /**
     * Month of first value < test value.
     */
    MONTH_OF_FIRST_LT ( "MonthOfFirstLT", "MonthOfFirstLT", "Month in time series for first value < a specified value." ),
    /**
     * Month of last value >= test value.
     */
    MONTH_OF_LAST_GE ( "MonthOfLastGE", "MonthOfLastGE", "Month in time series for last value >= a specified value." ),
    /**
     * Month of last value > test value.
     */
    MONTH_OF_LAST_GT ( "MonthOfLastGT", "MonthOfLastGT", "Month in time series for last value > a specified value." ),
    /**
     * Month of last value <= test value.
     */
    MONTH_OF_LAST_LE ( "MonthOfLastLE", "MonthOfLastLE", "Month in time series for last value <= a specified value." ),
    /**
     * Month of last value < test value.
     */
    MONTH_OF_LAST_LT ( "MonthOfLastLT", "MonthOfLastLT", "Month in time series for last value < a specified value." ),
    /**
     * Month of maximum value.
     */
    MONTH_OF_MAX ( "MonthOfMax", "MonthOfMax", "Month of maximum value." ),
    /**
     * Month of minimum value.
     */
    MONTH_OF_MIN ( "MonthOfMin", "MonthOfMin", "Month of minimum value." ),
    /**
     * Nonexceedance probability, can be percent or fraction.
     */
    NEP ( "NEP", "Nonexceedance Probability", "Nonexceedance probability, can be percent or fraction." ), // Make the same as NONEXCEEDANCE_PROBABILITY
    /**
     * Nonexceedance probability, percent.
     */
    NONEXCEEDANCE_PROBABILITY ( "NonexceedanceProbability", "Nonexceedance Probability", "Nonexceedance probability, can be percent or fraction." ),
	/**
	 * Value for which there is a 10% probability of not exceeding the value (0 to 10% chance).
	 */
    NONEXCEEDANCE_PROBABILITY_10 ( "NonexceedanceProbability10", "NonexceedanceProbability10", "Value for which there is a 10% probability of not exceeding the value (0 to 10% chance)." ),
    /**
     * Value for which there is a 30% probability of not exceeding the value (0 to 30% chance).
     */
    NONEXCEEDANCE_PROBABILITY_30 ( "NonexceedanceProbability30", "NonexceedanceProbability30", "Value for which there is a 30% probability of not exceeding the value (0 to 30% chance)." ),
    /**
     * Value for which there is a 50% probability of not exceeding the value (0 to 50% chance).
     */
    NONEXCEEDANCE_PROBABILITY_50 ( "NonexceedanceProbability50", "NonexceedanceProbability50", "Value for which there is a 50% probability of not exceeding the value (0 to 50% chance)." ),
    /**
     * Value for which there is a 70% probability of not exceeding the value (0 to 70% chance).
     */
    NONEXCEEDANCE_PROBABILITY_70 ( "NonexceedanceProbability70", "NonexceedanceProbability70", "Value for which there is a 70% probability of not exceeding the value (0 to 70% chance)." ),
    /**
     * Value for which there is a 90% probability of not exceeding the value (0 to 90% chance).
     */
    NONEXCEEDANCE_PROBABILITY_90 ( "NonexceedanceProbability90", "NonexceedanceProbability90", "Value for which there is a 90% probability of not exceeding the value (0 to 90% chance)." ),
    /**
     * Count of non-missing values in the sample.
     */
    NONMISSING_COUNT ( "NonmissingCount", "NonmissingCount", "Count of non-missing values in the sample." ),
    /**
     * Percent of non-missing values.
     */
    NONMISSING_PERCENT ( "NonmissingPercent", "NonmissingPercent", "Percent of non-missing values." ),
    /**
     * For daily data: 10-year recurrence interval for lowest 7-day average flow (each year) is 7q10
     */
    NQYY ( "NqYY", "NqYY", "For example: 10-year recurrence interval for lowest 7-day average flow (each year) is 7q10" ),
    /**
     * Percent of max for a value.
     */
    PERCENT_OF_MAX ( "PercentOfMax", "PercentOfMax", "Percent of Max for a value." ),
    /**
     * Percent of min.
     */
    PERCENT_OF_MIN ( "PercentOfMin", "PercentOfMin", "Percent of Min for a value." ),
    /**
     * Percent of mean.
     */
    PERCENT_OF_MEAN ( "PercentOfMean", "PercentOfMean", "Percent of Mean for a value." ),
    /**
     * Percent of median.
     */
    PERCENT_OF_MEDIAN ( "PercentOfMedian", "PercentOfMedian", "Percent of Median for a value." ),
    /**
     * Plotting position (depends on distribution and sort order imposed by other code).
     * TODO SAM 2016-06-17 need to confirm whether this should be used or the other variations.
     */
    PLOTTING_POSITION ( "PlottingPosition", "PlottingPosition", "Plotting position for a distribution, values sorted low to high." ),
    /**
     * Plotting position based on ascending sort.
     * TODO SAM 2016-06-17 evaluate phasing out.
     */
    PLOTTING_POSITION_ASCENDING ( "PlotPosAscending", "PlotPosAscending", "Plotting position based on ascending sort." ),
    /**
     * Plotting position based on descending sort.
     * TODO SAM 2016-06-17 evaluate phasing out.
     */
    PLOTTING_POSITION_DESCENDING ( "PlotPosDescending", "PlotPosDescending", "Plotting position based on descending sort." ),
    /**
     * Rank (depends on sort order imposed by other code).
     * @deprecated confusing because sort order is not tied to statistic
     */
    RANK ( "Rank", "Rank", "Rank (depends on sort order imposed by other code)." ),
    /**
     * Rank based on minimum to maximum sort.
     */
    RANK_ASCENDING ( "RankAscending", "RankAscending", "Rank based on minimum to maximum sort." ),
    /**
     * Rank based on maximum to minimum sort.
     */
    RANK_DESCENDING ( "RankDescending", "RankDescending", "Rank based on maximum to minimum sort." ),
    /**
     * Coefficient of skew.
     */
    SKEW ( "Skew", "Skew", "Coefficient of skew." ),
    /**
     * Standard deviation.
     */
    STD_DEV ( "StdDev", "StdDev", "Standard deviation." ),
    /**
     * Maximum of of (value - mean), if a surplus.
     */
    SURPLUS_MAX ( "SurplusMax", "SurplusMax", "Maximum of of (value - Mean), if a surplus." ),
    /**
     * Mean of (value - mean), if a surplus.
     */
    SURPLUS_MEAN ( "SurplusMean", "SurplusMean", "Mean of (value - Mean), if a surplus." ),
    /**
     * Minimum of (value - mean), if a surplus.
     */
    SURPLUS_MIN ( "SurplusMin", "SurplusMin", "Minimum of (value - Mean), if a surplus." ),
    /**
     * Maximum of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MAX ( "SurplusSeqLengthMax", "SurplusSeqLengthMax", "Maximum of number of sequential intervals with values above the mean." ),
    /**
     * Mean of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MEAN ( "SurplusSeqLengthMean", "SurplusSeqLengthMean", "Mean of number of sequential intervals with values above the mean." ),
    /**
     * Mean of number of sequential intervals with values above the mean.
     */
    SURPLUS_SEQ_LENGTH_MIN ( "SurplusSeqLengthMin", "SurplusSeqLengthMin", "Mean of number of sequential intervals with values above the mean." ),
    /**
     * Maximum of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MAX ( "SurplusSeqMax", "SurplusSeqMax", "Maximum of (value - Mean) sum when sequential values are above the mean." ),
    /**
     * Mean of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MEAN ( "SurplusSeqMean", "SurplusSeqMean", "Mean of (value - Mean) sum when sequential values are above the mean." ),
    /**
     * Minimum of (value - mean) sum when sequential values are above the mean.
     */
    SURPLUS_SEQ_MIN ( "SurplusSeqMin", "SurplusSeqMin", "Minimum of (value - mean) sum when sequential values are above the mean." ),
    /**
     * Total of values in the sample.
     */
	TOTAL ( "Total", "Total", "Total of values in the sample." ),
    /**
     * Trend represented as ordinary least squares line of best fit.  Output statistics are
     * TrendOLS_Intercept, TrendOLS_Slope, and TrendOLS_R2.  If analyzing time series values over time,
     * the X-coordinate (time) generally is computed as year, absolute month, or absolute day.
     */
    TREND_OLS ( "TrendOLS", "TrendOLS", "Trend represented as ordinary least squares line of best fit." ),
	/**
	 * Sample variance.
	 */
	VARIANCE ( "Variance", "Variance", "Sample variance." );
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports,
     * typically an abbreviation or short name without spaces.
     */
    private final String displayName;
    
    /**
     * A longer name than the display name, for example expanded acronym.
     */
    private final String longDisplayName;
    
    /**
     * Definition of the statistic, for use in tool tips, etc.
     */
    private final String definition;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc., typically a terse but understandable abbreviation,
     * guaranteed to be unique.
     * @param longDisplayName long name that can be used for displays, for example expanded abbreviation.
     * @param definition the definition of the statistic, for example for use in help tooltips.
     */
    private TSStatisticType(String displayName, String longDisplayName, String definition ) {
        this.displayName = displayName;
        this.longDisplayName = longDisplayName;
        this.definition = definition;
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