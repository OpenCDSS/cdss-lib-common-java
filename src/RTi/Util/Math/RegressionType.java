package RTi.Util.Math;

/**
Regression types.
*/
public enum RegressionType
{
    /**
     * Ordinary Least Squares regression.
     */
    OLS_REGRESSION("OLSRegression"),
    /**
     * Maintenance of Variation 1.
     */
    MOVE1("MOVE1"),
    /**
     * Maintenance of Variation 2.
     */
    MOVE2("MOVE2");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private RegressionType(String displayName) {
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
    public static RegressionType valueOfIgnoreCase(String name)
    {
        RegressionType [] values = values();
        for ( RegressionType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}