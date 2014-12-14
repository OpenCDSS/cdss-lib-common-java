package RTi.Util.Math;

/**
Sort order, for example when ranking or determining plotting position.
*/
public enum SortOrderType
{
    /**
     * Low to high (first position in rank is smallest value).
     */
    LOW_TO_HIGH("LowToHigh"),
    /**
     * High to low (first position in rank is highest value).
     */
    HIGH_TO_LOW("HighToLow");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private SortOrderType(String displayName) {
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
    public static SortOrderType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        SortOrderType [] values = values();
        for ( SortOrderType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}