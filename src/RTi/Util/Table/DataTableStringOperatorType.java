package RTi.Util.Table;

/**
Enumeration of simple string operators that can be performed on table cells.
*/
public enum DataTableStringOperatorType
{

/*
Append string values.
*/
APPEND("Append"),
/*
Prepend string values.
*/
PREPEND("Prepend"),
/*
Replace string substring.
*/
REPLACE("Replace"),
/*
Remove string substring.
*/
REMOVE("Remove"),
/*
Return a token split from the string based on a delimiter.
*/
SPLIT("Split"),
/*
Return a substring.
*/
SUBSTRING("Substring"),
/*
Cast a string value to a boolean.
*/
// TODO SAM 2015-04-29 Need to enable Boolean
//TO_BOOLEAN("ToBoolean"),
/*
Cast a string value to a date.
*/
TO_DATE("ToDate"),
/*
Cast a string value to a date/time.
*/
TO_DATE_TIME("ToDateTime"),
/*
Cast a string value to a double.
*/
TO_DOUBLE("ToDouble"),
/*
Cast a string value to an integer.
*/
TO_INTEGER("ToInteger");

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct an enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private DataTableStringOperatorType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the string operator.  This is usually the same as the
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
public static DataTableStringOperatorType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    DataTableStringOperatorType [] values = values();
    // Currently supported values
    for ( DataTableStringOperatorType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}