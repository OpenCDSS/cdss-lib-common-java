package RTi.GRTS;

/**
Indicate how data flags should be visualized in table displays
*/
public enum TSDataFlagVisualizationType
{
    /**
    Data flags are not shown.
    */
    NOT_SHOWN("Not shown"),
    /**
    Data flags are shown in separate column of table.
    */
    SEPARATE_COLUMN("Separate column"),
    /**
    Data flags are shown as superscript on data values.
    */
    SUPERSCRIPT("Superscript");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSDataFlagVisualizationType(String displayName) {
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
    public static TSDataFlagVisualizationType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSDataFlagVisualizationType [] values = values();
        for ( TSDataFlagVisualizationType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}