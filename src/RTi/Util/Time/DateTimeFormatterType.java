package RTi.Util.Time;

import java.util.List;
import java.util.Vector;

/**
Date/time formatter types, to allow the DateTime object to be formatted with different format strings.
*/
public enum DateTimeFormatterType
{
/**
"strftime" from standard C library, Python, etc.
*/
STRFTIME ( "Strftime", "C library date/time formatting" ),
/**
Microsoft Excel formatting.
*/
EXCEL ( "Excel", "Excel date/time formatting" ),
/**
TODO SAM 2011-11-14 Possible provide "Microsoft" option for more complex formats beyond Excel?
*/
//MICROSOFT ( "Microsoft", "Microsoft formatting library" )
;

/**
The name that is used for choices and other technical code (terse).
*/
private final String displayName;

/**
The name that is used for notes and explanations (more verbose).
*/
private final String displayNameVerbose;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private DateTimeFormatterType(String displayName, String displayNameVerbose ) {
    this.displayName = displayName;
    this.displayNameVerbose = displayNameVerbose;
}

/**
Get the list of date/time formatter types.
@return the list of date/time formatter types.
*/
public static List<DateTimeFormatterType> getDateTimeFormatChoices()
{
    List<DateTimeFormatterType> choices = new Vector();
    choices.add ( DateTimeFormatterType.EXCEL );
    choices.add ( DateTimeFormatterType.STRFTIME );
    //choices.add ( DateTimeFormatType.MICROSOFT );
    return choices;
}

/**
Get the list of date/time formatter types.
@return the list of date/time formatter types as strings.
@param includeNote If true, the returned string will be of the form
"Excel - Excel date/time formatting", using the sort and verbose display names.
If false, the returned string will be of the form "Excel", using only the short display name.
*/
public static List<String> getDateTimeFormatChoicesAsStrings( boolean includeNote )
{
    List<DateTimeFormatterType> choices = getDateTimeFormatChoices();
    List<String> stringChoices = new Vector();
    for ( int i = 0; i < choices.size(); i++ ) {
        DateTimeFormatterType choice = choices.get(i);
        String choiceString = "" + choice;
        if ( includeNote ) {
            choiceString = choiceString + " - " + choice.toStringVerbose();
        }
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
Return the verbose display name for the type.
@return the verbose display name for the type.
*/
public String toStringVerbose() {
    return displayNameVerbose;
}

/**
Return the enumeration value given a string name (case-independent).
@param name the date/time format string to match, as either the short or verbose display name, or the
concatenated version "displayName - displayNameVerbose".  
@return the enumeration value given a string name (case-independent), or null if not matched.
@exception IllegalArgumentException if the name does not match a valid date/time formatter type.
*/
public static DateTimeFormatterType valueOfIgnoreCase (String name)
{
    DateTimeFormatterType [] values = values();
    for ( DateTimeFormatterType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) || name.equalsIgnoreCase(t.toStringVerbose()) ||
            name.equalsIgnoreCase(t.toString() + " - " + t.toStringVerbose() )) {
            return t;
        }
    }
    throw new IllegalArgumentException ( "The following does not match a valid date/time formatter type: \"" + name + "\"");
}

}