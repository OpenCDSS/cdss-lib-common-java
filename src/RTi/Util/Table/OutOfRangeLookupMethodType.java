package RTi.Util.Table;

/**
Lookup method types when using a lookup table, for out of range values.
*/
public enum OutOfRangeLookupMethodType
{
    /**
    Interpolate between known values.
    */
    EXTRAPOLATE("Extrapolate"),
    /**
    Set the resulting value missing.
    */
    SET_MISSING("SetMissing"),
    /**
    Use the end value in the table.
    */
    USE_END_VALUE("UseEndValue");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private OutOfRangeLookupMethodType(String displayName) {
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
    public static OutOfRangeLookupMethodType valueOfIgnoreCase(String name)
    {
        OutOfRangeLookupMethodType [] values = values();
        for ( OutOfRangeLookupMethodType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}