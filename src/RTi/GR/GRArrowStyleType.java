package RTi.GR;

/**
Arrow styles.  If a size is required, then other data will be needed to specified in addition to the style.
*/
public enum GRArrowStyleType
{
    /**
     * No arrow.
     */
    NONE("None"),
    
    /**
     * Solid arrowhead. 
     */
    SOLID("Solid");
    
    /**
     * The string name that should be displayed.
     */
    private final String displayName;
    
    /**
     * Construct a time series list type enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRArrowStyleType(String displayName) {
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
	public static GRArrowStyleType valueOfIgnoreCase(String name)
	{
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values
	    for ( GRArrowStyleType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    } 
	    return null;
	}
}