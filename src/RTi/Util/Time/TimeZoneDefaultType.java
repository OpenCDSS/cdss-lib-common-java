package RTi.Util.Time;

import java.util.ArrayList;
import java.util.List;

/**
How to handle missing time zone, for example when DateTime.getDate() is called.
*/
public enum TimeZoneDefaultType
{
/**
Use local computer time zone.
*/
LOCAL ( "Local" ),
/**
No default timezone allowed allowed.
*/
NONE ( "None" ),
/**
GMT time zone as default.
*/
GMT( "GMT" );

/**
The name that is used for choices and other technical code (terse).
*/
private final String displayName;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TimeZoneDefaultType(String displayName) {
    this.displayName = displayName;
}

/**
Get the list of time zone default types.
@return the list of time zone default types.
*/
public static List<TimeZoneDefaultType> getTimeZoneDefaultChoices()
{
    List<TimeZoneDefaultType> choices = new ArrayList<TimeZoneDefaultType>();
    choices.add ( TimeZoneDefaultType.GMT );
    choices.add ( TimeZoneDefaultType.LOCAL );
    choices.add ( TimeZoneDefaultType.NONE );
    return choices;
}

/**
Get the list of time zone default types.
@return the list of time zone default types as strings.
*/
public static List<String> getTimeZoneDefaultChoicesAsStrings( boolean includeNote )
{
    List<TimeZoneDefaultType> choices = getTimeZoneDefaultChoices();
    List<String> stringChoices = new ArrayList<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        TimeZoneDefaultType choice = choices.get(i);
        String choiceString = "" + choice;
        stringChoices.add ( choiceString );
    }
    return stringChoices;
}

/**
Return the short display name for the type.  This is the same as the value.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@param name the display name for the time zone default.  
@return the enumeration value given a string name (case-independent).
@exception IllegalArgumentException if the name does not match a valid time zone default type.
*/
public static TimeZoneDefaultType valueOfIgnoreCase (String name)
{   if ( name == null ) {
        return null;
    }
    TimeZoneDefaultType [] values = values();
    for ( TimeZoneDefaultType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    throw new IllegalArgumentException ( "The following does not match a valid time zone default type: \"" + name + "\"");
}

}