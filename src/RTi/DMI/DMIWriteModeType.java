package RTi.DMI;

/**
This enumeration stores values for a modes of writing to a DMI (e.g., database).  Different modes may be
appropriate for performance reasons or because of business processes that expect certain behavior in
business processes.
*/
public enum DMIWriteModeType
{

/**
Delete the items first and then insert.
*/
DELETE_INSERT(DMI.DELETE_INSERT,"DeleteInsert"),

/**
Insert only.
*/
INSERT(DMI.INSERT,"Insert"),

/**
Try inserting first and if an exception occurs (data already exist), update the existing values.
*/
INSERT_UPDATE(DMI.INSERT_UPDATE,"InsertUpdate"),

/**
Update only.
*/
UPDATE(DMI.UPDATE,"Update"),

/**
Try updating the values first and if that fails (previous values do not exist) insert the values.
*/
UPDATE_INSERT(DMI.UPDATE_INSERT,"UpdateInsert");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
The internal code for the enumeration, matches the matches the DMI write mode
definitions to bridge legacy code..
*/
private final int code;

/**
Construct an enumeration value.
@param code the internal numeric code for the enumeration
@param displayName name that should be displayed in choices, etc.
*/
private DMIWriteModeType(int code, String displayName) {
    this.code = code;
    this.displayName = displayName;
}

/**
Return the internal code used with the enumeration, which is the same as DMI definitions, to allow
transition of code to the enumeration.
*/
public int getCode() {
    return this.code;
}

/**
Return the display name for the enumeration string.  This is usually the same as the
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
public static DMIWriteModeType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    DMIWriteModeType [] values = values();
    // Currently supported values
    for ( DMIWriteModeType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}