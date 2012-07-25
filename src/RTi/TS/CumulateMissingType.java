package RTi.TS;

/**
This enumeration stores values how to handle missing values when cumulating data
(e.g., with TSUtil_CumulateTimeSeries).
*/
public enum CumulateMissingType
{

/**
Trend in data is decreasing.
*/
CARRY_FORWARD("CarryForwardIfMissing"),

/**
Trend in data is increasing.
*/
SET_MISSING("SetMissingIfMissing");

/**
The name that should be displayed when the best fit type is used in UIs and reports.
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private CumulateMissingType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the statistic.  This is usually the same as the
value but using appropriate mixed case.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@return the enumeration value given a string name (case-independent), or null if not matched.
*/
public static CumulateMissingType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    CumulateMissingType [] values = values();
    // Currently supported values
    for ( CumulateMissingType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}