package RTi.GR;

/**
Ways to connect points when drawing, needed for example to represent instantaneous values, averages, etc.
Handling of missing values with gaps is expected to occur as appropriate but is not indicated by this type.
*/
public enum GRPointConnectType
{
    /**
     * Connect points.
     */
    CONNECT("Connect"),
    
    /**
     * Step-function with line drawn backward from the point (point's y-coordinate is end of step). 
     */
    STEP_BACKWARD("StepBackward"),
    
    /**
     * Step-function with line drawn forward from the point (point's y-coordinate is start of step). 
     */
    STEP_FORWARD("StepForward");
    
    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Construct a point connect type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRPointConnectType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     */
    public boolean equals ( String arrowStyleType ) {
        if ( arrowStyleType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }
	
    /**
     * Return the display name for the arrow style type.  This is usually the same as the
     * value but using appropriate mixed case.
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
	public static GRPointConnectType valueOfIgnoreCase(String name)
	{
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values
	    for ( GRPointConnectType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    } 
	    return null;
	}
}