package RTi.Util.IO;

/**
Mode for writing files, intended to be used as needed, but currently with no tight
bundling to other code.
*/
public enum FileWriteModeType
{
/**
Append to the end of the file.
*/
APPEND("Append"),
/**
Overwrite the file with the new content.
*/
OVERWRITE("Overwrite"),
/**
Update the file contents by updating overlapping data and appending the rest.
*/
UPDATE("Update");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private FileWriteModeType(String displayName) {
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
public static FileWriteModeType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    FileWriteModeType [] values = values();
    for ( FileWriteModeType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}