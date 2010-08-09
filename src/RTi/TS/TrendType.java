package RTi.TS;

/**
This enumeration stores values for a trend, meaning whether data values increase over time, decrease, or
are variable.  For example, the trend for accumulated precipitation data is that values increase over time.
*/
public enum TrendType
{
    /**
     * Trend in data is decreasing.
     */
    DECREASING("Decreasing"),
    /**
     * Trend in data is increasing.
     */
    INCREASING("Increasing"),
	/**
	 * Trend in values is variable (some increasing and decreasing).
	 */
	VARIABLE("Variable");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TrendType(String displayName) {
        this.displayName = displayName;
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
public static TrendType valueOfIgnoreCase(String name)
{
    TrendType [] values = values();
    // Currently supported values
    for ( TrendType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}