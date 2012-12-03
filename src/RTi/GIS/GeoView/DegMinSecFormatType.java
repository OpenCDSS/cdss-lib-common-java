package RTi.GIS.GeoView;

/**
Mode for formatting DegMinSec instances, for use with DegMinSec.toString() method.
*/
public enum DegMinSecFormatType
{
/**
Append to the end of the file.
*/
DEGMMSS("DegMMSS");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private DegMinSecFormatType(String displayName) {
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
public static DegMinSecFormatType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    DegMinSecFormatType [] values = values();
    for ( DegMinSecFormatType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}