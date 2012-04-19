package RTi.TS;

/**
This enumeration defines time series function types, which are functions that are used to assign data
to time series.  Consequently, x = f(y) would translate to y being the date/time and x being the time series value.
*/
public enum TSFunctionType
{
    /**
    Assign the year to the whole number part of the value.
    */
    DATE_YYYY("DateYYYY"),
    /**
    Assign the year and month to the whole number part of the value.
    */
    DATE_YYYYMM("DateYYYYMM"),
    /**
    Assign the year, month, and day to the whole number part of the value.
    */
    DATE_YYYYMMDD("DateYYYYMMDD"),
    /**
    Assign the date and time (to hour) to the whole number part of the value and the hour as the
    fraction (hour 1 = .01, hour 24 = .24).
    */
    DATETIME_YYYYMMDD_HH("DateTimeYYYYMMDD_hh"),
    /**
    Assign the date to the whole number part of the value and the hour and minute as the
    fraction (hour 1, minute 1 = .0101, hour 24, 59 = .2459).
    */
    DATETIME_YYYYMMDD_HHMM("DateTimeYYYYMMDD_hhmm");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TSFunctionType(String displayName) {
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
public static TSFunctionType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    TSFunctionType [] values = values();
    for ( TSFunctionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}