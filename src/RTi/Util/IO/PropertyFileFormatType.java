package RTi.Util.IO;

/**
Format type for a property file, intended to be used as needed, but currently with no tight
bundling to other code.  For example, use the types for an applications configuration file.
*/
public enum PropertyFileFormatType
{
// TODO SAM 2012-07-27 Evaluate adding JSON, XML, and CSV
/**
Format of properties is PropertyName=Value, using double quotes for the value if necessary.
*/
NAME_VALUE("NameValue"),
/**
Format of properties is PropertyName=Type(Value), using double quotes for the value if necessary.
Type is only used as appropriate to remove ambiguity of parsing to strings,
for example DateTime("2010-01-15").
*/
NAME_TYPE_VALUE("NameTypeValue"),
/**
Format of properties is the same as NAME_TYPE_VALUE except that objects are formatted to be consistent
with Python, which allows the property file to be used directly in Python to assign variables.
*/
NAME_TYPE_VALUE_PYTHON("NameTypeValuePython");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private PropertyFileFormatType(String displayName) {
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
public static PropertyFileFormatType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    PropertyFileFormatType [] values = values();
    for ( PropertyFileFormatType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}