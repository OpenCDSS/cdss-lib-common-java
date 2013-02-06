package RTi.TS;

/**
This enumeration defines the value to be returned when converting a time series to an array.
Typically the data value is returned; however, there are cases when the date/time is returned.
*/
public enum TSToArrayReturnType
{
/**
Return the time series data value.
*/
DATA_VALUE("DataValue"),
/**
Return the date/time associated with data values.
*/
DATE_TIME("DateTime");

/**
The name that should be displayed when the best fit type is used in UIs and reports.
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TSToArrayReturnType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the type.  This is usually similar to the
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
public static TSToArrayReturnType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    TSToArrayReturnType [] values = values();
    for ( TSToArrayReturnType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}