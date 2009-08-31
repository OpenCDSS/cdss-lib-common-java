package RTi.Util.Math;

/**
Number of equations, for example when performing a regression analysis.
*/
public enum NumberOfEquationsType
{
    /**
     * One equation for entire sample.
     */
    ONE_EQUATION("OneEquation"),
    /**
     * Monthly equations.
     */
    MONTHLY_EQUATIONS("MonthlyEquations");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private NumberOfEquationsType(String displayName) {
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
    public static NumberOfEquationsType valueOfIgnoreCase(String name)
    {
        NumberOfEquationsType [] values = values();
        for ( NumberOfEquationsType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}