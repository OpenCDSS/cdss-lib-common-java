package RTi.Util.Table;

/**
Enumeration of table join methods.
*/
public enum DataTableJoinMethodType
{

/*
Always join table records.
*/
JOIN_ALWAYS("JoinAlways"),
/*
Only join if the join column values match in both tables.
*/
JOIN_IF_IN_BOTH("JoinIfInBoth");

/**
The name that should be displayed when used in UIs and reports.
*/
private final String displayName;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private DataTableJoinMethodType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the string operator.  This is usually the same as the
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
public static DataTableJoinMethodType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    DataTableJoinMethodType [] values = values();
    // Currently supported values
    for ( DataTableJoinMethodType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}