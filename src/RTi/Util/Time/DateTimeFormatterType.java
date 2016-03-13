package RTi.Util.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
Date/time formatter types, to allow the DateTime object to be formatted with different format strings.
*/
public enum DateTimeFormatterType
{
/**
C-style formats (see http://linux.die.net/man/3/strftime).
*/
C ( "C", "C/UNIX" ),
/**
ISO-8601 formats (see http://dotat.at/tmp/ISO_8601-2004_E.pdf).
*/
ISO ( "ISO", "ISO 8601" ),
/**
Microsoft style formats (see http://msdn.microsoft.com/en-us/library/az4se3k1.aspx).
TODO SAM 2012-04-10 Is this compatible with Excel?
*/
MS( "MS", "Microsoft" );

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
public static List<DateTimeFormatterType> getDateTimeFormatterChoices()
{
    List<DateTimeFormatterType> choices = new ArrayList<DateTimeFormatterType>();
    choices.add ( DateTimeFormatterType.C );
    choices.add ( DateTimeFormatterType.ISO );
    choices.add ( DateTimeFormatterType.MS );
    return choices;
}

/**
Get the list of date/time formatter types.
@return the list of date/time formatter types as strings.
@param includeNote If true, the returned string will be of the form
"Excel - Excel date/time formatting", using the sort and verbose display names.
If false, the returned string will be of the form "Excel", using only the short display name.
*/
public static List<String> getDateTimeFormatterChoicesAsStrings( boolean includeNote )
{
    List<DateTimeFormatterType> choices = getDateTimeFormatterChoices();
    List<String> stringChoices = new ArrayList<String>();
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
@return the enumeration value given a string name (case-independent).
@exception IllegalArgumentException if the name does not match a valid date/time formatter type.
*/
public static DateTimeFormatterType valueOfIgnoreCase (String name)
{   if ( name == null ) {
        return null;
    }
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