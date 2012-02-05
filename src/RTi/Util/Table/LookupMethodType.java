package RTi.Util.Table;

/**
Lookup method types when using a lookup table.
*/
public enum LookupMethodType
{
    /**
    Interpolate between known values.
    */
    INTERPOLATE("Interpolate"),
    /**
    Use the next value in the table.
    */
    NEXT_VALUE("NextValue"),
    /**
    Use the previous value in the table.
    */
    PREVIOUS_VALUE("PreviousValue");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private LookupMethodType(String displayName) {
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
    public static LookupMethodType valueOfIgnoreCase(String name)
    {
        LookupMethodType [] values = values();
        for ( LookupMethodType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}