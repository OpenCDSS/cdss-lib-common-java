package RTi.GRTS;

/**
The mouse tracker mode, which controls the behavior of the tracker.
*/
public enum TSGraphMouseTrackerType
{
	/**
	 * Track the single nearest point to the mouse.
	 */
    NEAREST("Nearest"),
    /**
     * Track single nearest point to the mouse, for only selected time series.
     */
    NEAREST_SELECTED("NearestSelected"),
    /**
     * Track the nearest point(s) to the time for the mouse.
     */
    NEAREST_TIME("NearestTime"),
    /**
     * Track the nearest point(s) to the time for the mouse, for only selected time series.
     */
    NEAREST_TIME_SELECTED("NearestTimeSelected"),
    /**
     * Do not track the mouse.
     */
    NONE("None");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphMouseTrackerType(String displayName) {
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
    public static TSGraphMouseTrackerType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSGraphMouseTrackerType [] values = values();
        for ( TSGraphMouseTrackerType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}