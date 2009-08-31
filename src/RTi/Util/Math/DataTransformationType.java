package RTi.Util.Math;

/**
Data transformations that may be applied, for example before performing a regression analysis.
*/
public enum DataTransformationType
{
    LOG("Log"),
    NONE("None");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private DataTransformationType(String displayName) {
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
    public static DataTransformationType valueOfIgnoreCase(String name)
    {
        DataTransformationType [] values = values();
        for ( DataTransformationType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}