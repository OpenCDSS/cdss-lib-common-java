package RTi.GRTS;

/**
Time series view window types, used to manage windows.
*/
public enum TSViewType
{
    /**
    Graph view.
    */
    GRAPH("Graph"),
    /**
    Properties view, currently only used with graph but may properties may be used with other views.
    */
    PROPERTIES("Properties"),
    /**
    Properties view (not visible), used to programatically make quick changes to properties.
    */
    PROPERTIES_HIDDEN("PropertiesHidden"),
    /**
    Summary view.
    */
    SUMMARY("Summary"),
    /**
    Table view.
    */
    TABLE("Table");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSViewType(String displayName) {
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
    public static TSViewType valueOfIgnoreCase(String name)
    {
        TSViewType [] values = values();
        for ( TSViewType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}