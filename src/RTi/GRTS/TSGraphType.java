package RTi.GRTS;

/**
Time series graph types to be used for graph properties.
This can be used for a graph space, or for individual time series in a graph space.
Other properties influence the actual appearance (e.g., log/linear axis in conjunction with line graph).
*/
public enum TSGraphType
{
    UNKNOWN("Unknown"),
    AREA("Area"),
    AREA_STACKED("AreaStacked"),
    BAR("Bar"),
    DOUBLE_MASS("Double-Mass"),
    DURATION("Duration"),
    LINE("Line"),
    PERIOD("PeriodOfRecord"),
    POINT("Point"),
    PREDICTED_VALUE("PredictedValue"),
    PREDICTED_VALUE_RESIDUAL("PredictedValueResidual"),
    RASTER("Raster"),
    XY_SCATTER("XY-Scatter");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphType(String displayName) {
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
    public static TSGraphType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSGraphType [] values = values();
        for ( TSGraphType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}