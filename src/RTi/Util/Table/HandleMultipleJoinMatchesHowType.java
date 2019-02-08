// HandleMultipleJoinMatchesHowType - enumeration indicates how to handle multiple table join matches

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
