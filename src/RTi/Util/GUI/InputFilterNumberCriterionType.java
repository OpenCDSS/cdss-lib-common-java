// InputFilterNumberCriterionType - enumeration of number conditions that can be checked for in an input filter

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

package RTi.Util.GUI;

/**
 * Enumeration of number conditions that can be checked for in an input filter.
 * @author sam
 *
 */
public enum InputFilterNumberCriterionType {

/**
 * Number is exactly equal to a value.
 */
EQUALS("="),
/**
 * Number is greater than a value.
 */
GREATER_THAN ( ">" ),
/**
 * Number is less than or equal to a value.
 */
GREATER_THAN_OR_EQUAL_TO ( ">=" ),
/**
 * Number is less than a value.
 */
LESS_THAN ( "<" ),
/**
 * Number is less than or equal to a value.
 */
LESS_THAN_OR_EQUAL_TO ( "<=" );

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct an enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private InputFilterNumberCriterionType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the enumeration.
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
public static InputFilterNumberCriterionType valueOfIgnoreCase(String name) {
    // Legacy/alternate values
    if ( name.equalsIgnoreCase("Equals") ) {
        return EQUALS;
    }
    else if ( name.equalsIgnoreCase("Greater than") ) {
        return GREATER_THAN;
    }
    else if ( name.equalsIgnoreCase("Less than") ) {
        return LESS_THAN;
    }
    InputFilterNumberCriterionType [] values = values();
    // Currently supported values
    for ( InputFilterNumberCriterionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}