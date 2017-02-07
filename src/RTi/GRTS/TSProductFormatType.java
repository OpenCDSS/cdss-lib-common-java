package RTi.GRTS;

/**
Format for TSProduct text representation, indicating formatting before display or writing to file.
*/
public enum TSProductFormatType
{
    /**
    Legacy properties list, similar to INI file.
    */
    PROPERTIES("Properties"),
    /**
    JSON without line breaks or pretty formatting.
    */
    JSON_COMPACT ("JSONCompact"),
    /**
    JSON with line breaks and pretty formatting.
    */
    JSON_PRETTY("JSONPretty");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSProductFormatType(String displayName) {
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
    public static TSProductFormatType valueOfIgnoreCase(String name)
    {
        TSProductFormatType [] values = values();
        for ( TSProductFormatType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}