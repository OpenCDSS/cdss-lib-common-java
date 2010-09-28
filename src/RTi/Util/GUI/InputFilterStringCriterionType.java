package RTi.Util.GUI;

/**
 * Enumeration of string conditions that can be checked for in an input filter.
 * String conditions are usually performed by ignoring case.
 * @author sam
 *
 */
public enum InputFilterStringCriterionType
{

/**
 * String contains a substring.
 */
CONTAINS("Contains"),
/**
 * String ends with a substring.
 */
ENDS_WITH ( "EndsWith" ),
/**
 * Full string matches.
 */
MATCHES ( "Matches" ),
/**
 * String starts with a substring.
 */
STARTS_WITH ( "StartsWith" );

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private InputFilterStringCriterionType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the condition.  This is usually the same as the
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
public static InputFilterStringCriterionType valueOfIgnoreCase(String name)
{
    // Legacy/alternate values
    if ( name.equalsIgnoreCase("Ends with") ) {
        return ENDS_WITH;
    }
    else if ( name.equalsIgnoreCase("Starts with") ) {
        return STARTS_WITH;
    }
    InputFilterStringCriterionType [] values = values();
    // Currently supported values
    for ( InputFilterStringCriterionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}