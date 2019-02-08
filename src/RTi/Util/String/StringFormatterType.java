// StringFormatterType - string formatter types, to allow the input objects to be formatted with different format strings

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.String;

import java.util.List;
import java.util.Vector;

/**
String formatter types, to allow the input objects to be formatted with different format strings.
*/
public enum StringFormatterType
{
/**
C-style formats (see "sprintf").
*/
C ( "C", "C/UNIX" );

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
private StringFormatterType(String displayName, String displayNameVerbose ) {
    this.displayName = displayName;
    this.displayNameVerbose = displayNameVerbose;
}

/**
Get the list of date/time formatter types.
@return the list of date/time formatter types.
*/
public static List<StringFormatterType> getDateTimeFormatterChoices()
{
    List<StringFormatterType> choices = new Vector<StringFormatterType>();
    choices.add ( StringFormatterType.C );
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
    List<StringFormatterType> choices = getDateTimeFormatterChoices();
    List<String> stringChoices = new Vector<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        StringFormatterType choice = choices.get(i);
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
public static StringFormatterType valueOfIgnoreCase (String name)
{   if ( name == null ) {
        return null;
    }
    StringFormatterType [] values = values();
    for ( StringFormatterType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) || name.equalsIgnoreCase(t.toStringVerbose()) ||
            name.equalsIgnoreCase(t.toString() + " - " + t.toStringVerbose() )) {
            return t;
        }
    }
    throw new IllegalArgumentException ( "The following does not match a valid string formatter type: \"" + name + "\"");
}

}
