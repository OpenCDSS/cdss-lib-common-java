package RTi.Util.Table;

/**
 * Enumeration of simple math operators that can be performed on table cells.
 * @author sam
 *
 */
public enum DataTableMathOperatorType
{

/**
 * Add values.
 */
ADD("+"),
/**
 * Divide values.
 */
DIVIDE ( "/" ),
/**
 * Multiply values.
 */
MULTIPLY ( "*" ),
/**
 * Subtract values.
 */
SUBTRACT ( "-" );

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private DataTableMathOperatorType(String displayName) {
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
public static DataTableMathOperatorType valueOfIgnoreCase(String name)
{
    DataTableMathOperatorType [] values = values();
    // Currently supported values
    for ( DataTableMathOperatorType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}