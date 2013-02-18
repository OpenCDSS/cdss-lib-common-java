package RTi.Util.Table;

/**
Enumeration of data table column types.
These eventually will replace the numerical values in the TableField class.
*/
public enum TableColumnType
{

/**
4-byte integer, Java Integer.
*/
INT(0,"Integer"),
/**
2-byte integer, Java Short.
*/
SHORT(1,"Short"),
/**
8-byte double, Java Double.
*/
DOUBLE(2,"Double"),
/**
4-byte float, Java Float.
*/
FLOAT(3,"Float"),
/**
8-byte integer, Java Long.
*/
LONG(6,"Long"),
/**
Java String.
*/
STRING(4,"String"),
/**
Java Date.
*/
DATE(5,"Date");

/**
The name that should be displayed when used in UIs and reports.
*/
private final String displayName;

/**
The internal code for the enumeration, matches the TableField definitions to bridge legacy code.
*/
private final int code;

/**
Construct an enumeration value from a string.
@param displayName name that should be displayed in choices, etc.
*/
private TableColumnType(int code, String displayName) {
    this.code = code;
    this.displayName = displayName;
}

/**
 * Return the display name for the math operator.  This is usually the same as the
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
public static TableColumnType valueOfIgnoreCase(String name)
{
    TableColumnType [] values = values();
    // Currently supported values
    for ( TableColumnType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

/**
Return the enumeration value given a code value.
@return the enumeration value given a code value, or null if not matched.
*/
public static TableColumnType valueOf(int code)
{
    TableColumnType [] values = values();
    // Currently supported values
    for ( TableColumnType t : values ) {
        if ( code == t.code ) {
            return t;
        }
    } 
    return null;
}
    
}