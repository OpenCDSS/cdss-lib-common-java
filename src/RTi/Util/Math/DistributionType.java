package RTi.Util.Math;

/**
Distribution types, can be used with statistics such as plotting position.
The list should not be used generically but should be selected from as appropriate.
*/
public enum DistributionType
{
    /**
    Gringorten.
    */
    GRINGORTEN("Gringorten"),
    /**
    Weibull.
    */
    WEIBULL("Weibull");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private DistributionType(String displayName) {
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
    public static DistributionType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        DistributionType [] values = values();
        for ( DistributionType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}