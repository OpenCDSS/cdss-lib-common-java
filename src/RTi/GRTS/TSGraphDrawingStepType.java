package RTi.GRTS;

/**
The steps that occur during drawing, to help code like annotations.
*/
public enum TSGraphDrawingStepType
{
	/**
	 * Before drawing anything related to the back axes, which is underlying border (appropriate for Rectangle annotations that should be drawn under axes).
	 */
    BEFORE_BACK_AXES("BeforeBackAxes"),
    /**
     * After drawing anything related to the back axes.
     */
    AFTER_BACK_AXES("AfterBackAxes"),
    /**
     * Before drawing any data (time series).
     */
    BEFORE_DATA("BeforeData"),
    /**
     * After drawing data (time series).
     */
    AFTER_DATA("AfterData");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphDrawingStepType(String displayName) {
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
    public static TSGraphDrawingStepType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSGraphDrawingStepType [] values = values();
        for ( TSGraphDrawingStepType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}