package RTi.TS;

/**
Data check types, typically used in analysis code that checks time series data or statistic
values against some criteria.  Not all of these checks may be appropriate for code that uses the enumeration and
therefore a subset should be created as appropriate (e.g., repeating values apply to time series data but
probably not statistics computed from the data).
*/
public enum CheckType
{
/**
 * Absolute change from one value to the next > (in data units).
 */
ABS_CHANGE_GREATER_THAN("AbsChange>"),
/**
 * Absolute change from one value to the next > (percent).
 */
ABS_CHANGE_PERCENT_GREATER_THAN("AbsChangePercent>"),
/**
 * Change from one value to the next > (in data units).
 */
CHANGE_GREATER_THAN("Change>"),
/**
 * Change from one value to the next < (in data units).
 */
CHANGE_LESS_THAN("Change<"),
/**
 * Change in range of values (in data units).
 */
IN_RANGE("InRange"),
/**
 * Change out of range of values (in data units).
 */
OUT_OF_RANGE("OutOfRange"),
/**
 * Is value missing.
 */
MISSING("Missing"),
/**
 * Does value repeat.
 */
REPEAT("Repeat"),
/**
 * Is value less than.
 */
LESS_THAN("<"),
/**
 * Is value less than or equal to.
 */
LESS_THAN_OR_EQUAL_TO("<="),
/**
 * Is value greater than.
 */
GREATER_THAN(">"),
/**
 * Is value greater than or equal to.
 */
GREATER_THAN_OR_EQUAL_TO(">="),
/**
 * Is value equal to.
 */
EQUAL_TO("==");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private CheckType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the statistic.  This is usually the same as the
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
public static CheckType valueOfIgnoreCase(String name)
{
    CheckType [] values = values();
    for ( CheckType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}