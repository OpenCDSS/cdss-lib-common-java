package RTi.Util.Table;

/**
This enumeration indicates how to handle multiple table join matches (e.g., with the JoinTable command).
*/
public enum HandleMultipleJoinMatchesHowType
{

/**
Add rows for each match.
*/
//ADD_ROWS("AddRows"),

/**
Number columns to be copied.  For example, 2nd match results in a new column with "_2" in column name.
*/
NUMBER_COLUMNS("NumberColumns"),

/**
Use the last match.
*/
USE_LAST_MATCH("UseLastMatch");//,

/**
Use the last non-missing value.
*/
//USE_FIRST_MATCH("UseFirstMatch");

/**
The name that should be displayed when the type is used in UIs and reports.
*/
private final String displayName;

/**
Construct with the display name.
@param displayName name that should be displayed in choices, etc.
*/
private HandleMultipleJoinMatchesHowType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the statistic.  This is usually the same as the
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
public static HandleMultipleJoinMatchesHowType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    HandleMultipleJoinMatchesHowType [] values = values();
    // Currently supported values
    for ( HandleMultipleJoinMatchesHowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}