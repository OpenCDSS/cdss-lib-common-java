// TSUtil_ChangeInterval_HandleMissingInputHowType - To be used with the TSUtil_ChangeInterval class,
// indicating how to handle missing values in input.

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

package RTi.TS;

import java.util.ArrayList;
import java.util.List;

/**
To be used with the TSUtil_ChangeInterval class, indicating how to handle missing values in input.
*/
public enum TSUtil_ChangeInterval_HandleMissingInputHowType
{
/**
Keep the missing values in input.
*/
KEEP_MISSING ( "KeepMissing" ),
/**
Repeat non-missing values.
*/
REPEAT( "Repeat" ),
/**
Set missing values to zero.
*/
SET_TO_ZERO( "SetToZero" );

/**
The name that is used for choices and other technical code (terse).
*/
private final String displayName;

/**
Construct an enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TSUtil_ChangeInterval_HandleMissingInputHowType(String displayName) {
    this.displayName = displayName;
}

/**
Get the list of enumerations.
@return the list of enumerations.
*/
public static List<TSUtil_ChangeInterval_HandleMissingInputHowType> getHandleMissingInputHowChoices()
{
    List<TSUtil_ChangeInterval_HandleMissingInputHowType> choices = new ArrayList<TSUtil_ChangeInterval_HandleMissingInputHowType>();
    choices.add ( TSUtil_ChangeInterval_HandleMissingInputHowType.KEEP_MISSING );
    choices.add ( TSUtil_ChangeInterval_HandleMissingInputHowType.REPEAT );
    choices.add ( TSUtil_ChangeInterval_HandleMissingInputHowType.SET_TO_ZERO );
    return choices;
}

/**
Get the list of enumerations as strings.
@return the list of enumerations strings.
*/
public static List<String> getHandleMissingInputHowChoicesAsStrings()
{
    List<TSUtil_ChangeInterval_HandleMissingInputHowType> choices = getHandleMissingInputHowChoices();
    List<String> stringChoices = new ArrayList<String>();
    for ( int i = 0; i < choices.size(); i++ ) {
        stringChoices.add ( "" + choices.get(i) );
    }
    return stringChoices;
}

/**
Return the short display name for the statistic.  This is the same as the value.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@param name the time scale string to match.
@return the enumeration value given a string name (case-independent).
@exception IllegalArgumentException if the name does not match a valid time scale.
*/
public static TSUtil_ChangeInterval_HandleMissingInputHowType valueOfIgnoreCase (String name)
{
    TSUtil_ChangeInterval_HandleMissingInputHowType [] values = values();
    for ( TSUtil_ChangeInterval_HandleMissingInputHowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    throw new IllegalArgumentException (
        "The following does not match a recognized HandleMissingInputHow value: \"" + name + "\"");
}

}
