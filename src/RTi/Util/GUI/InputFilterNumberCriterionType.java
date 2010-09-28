package RTi.Util.GUI;

/**
 * Enumeration of number conditions that can be checked for in an input filter.
 * @author sam
 *
 */
public enum InputFilterNumberCriterionType
{

/**
 * Number is exactly equal to a value.
 */
EQUALS("="),
/**
 * Number is greater than a value.
 */
GREATER_THAN ( ">" ),
/**
 * Number is less than or equal to a value.
 */
GREATER_THAN_OR_EQUAL_TO ( ">=" ),
/**
 * Number is less than a value.
 */
LESS_THAN ( "<" ),
/**
 * Number is less than or equal to a value.
 */
LESS_THAN_OR_EQUAL_TO ( "<=" );

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private InputFilterNumberCriterionType(String displayName) {
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
public static InputFilterNumberCriterionType valueOfIgnoreCase(String name)
{
    // Legacy/alternate values
    if ( name.equalsIgnoreCase("Equals") ) {
        return EQUALS;
    }
    else if ( name.equalsIgnoreCase("Greater than") ) {
        return GREATER_THAN;
    }
    else if ( name.equalsIgnoreCase("Less than") ) {
        return LESS_THAN;
    }
    InputFilterNumberCriterionType [] values = values();
    // Currently supported values
    for ( InputFilterNumberCriterionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}