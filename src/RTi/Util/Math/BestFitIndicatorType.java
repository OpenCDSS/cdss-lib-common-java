package RTi.Util.Math;

/**
Best fit indicators that may be determined, for example when performing a regression analysis.
*/
public enum BestFitIndicatorType
{
    /**
     * Correlation coefficient.
     */
    R("r"),
    /**
     * Standard Error of Prediction, defined as the square root
     * of the sum of differences between the known dependent value, and the value
     * determined from the equation used to estimate the data.
     */
    SEP("SEP"),
    SEP_TOTAL("SEPTotal");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private BestFitIndicatorType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
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
    public static BestFitIndicatorType valueOfIgnoreCase(String name)
    {
        BestFitIndicatorType [] values = values();
        for ( BestFitIndicatorType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}