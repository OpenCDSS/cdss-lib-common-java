// InputFilterType - enumeration of InputFilter criterion (operator)

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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
 * Enumeration of InputFilter criterion (operators).
 */
public enum InputFilterCriterionType {

	/**
	Input filter type, for use with strings that exactly match a pattern.
	*/
	INPUT_MATCHES("Matches"),

	/**
	Input filter type, for use with strings and numbers that match a case in a list.
	*/
	INPUT_ONE_OF("One of"),

	/**
	Input filter type, for use with strings starting with a pattern.
	*/
	INPUT_STARTS_WITH("Starts with"),

	/**
	Input filter type, for use with strings ending with a pattern.
	*/
	INPUT_ENDS_WITH("Ends with"),

	/**
	Input filter type, for use with strings containing a pattern.
	*/
	INPUT_CONTAINS("Contains"),

	/**
	Input filter type, for use with numbers that exactly match.
	*/
	INPUT_EQUALS("="),

	/**
	Legacy INPUT_EQUALS, phasing out.
	*/
	INPUT_EQUALS_LEGACY("Equals"),

	/**
	Input filter type, for use with numbers that are between two values.
	*/
	INPUT_BETWEEN("Is between"),

	/**
	Input filter type, for use with strings that are null or empty.
	*/
	INPUT_IS_EMPTY("Is empty"),

	/**
	Input filter type, for use with numbers that are less than a value.
	*/
	INPUT_LESS_THAN("<"),

	/**
	Legacy INPUT_LESS_THAN, phasing out.
	*/
	INPUT_LESS_THAN_LEGACY("Less than"),

	/**
	Input filter type, for use with numbers that are less than or equal to a value.
	*/
	INPUT_LESS_THAN_OR_EQUAL_TO("<="),

	/**
	Input filter type, for use with numbers that are greater than a value.
	*/
	INPUT_GREATER_THAN(">"),

	/**
	Legacy INPUT_GREATER_THAN, phasing out.
	*/
	INPUT_GREATER_THAN_LEGACY("Greater than"),

	/**
	Input filter type, for use with numbers that are greater than or equal to a value.
	*/
	INPUT_GREATER_THAN_OR_EQUAL_TO(">=");

	/**
 	* The name that should be displayed when used in UIs and reports.
 	*/
	private final String displayName;

	/**
 	* Construct a time series statistic enumeration value.
 	* @param displayName name that should be displayed in choices, etc.
 	*/
	private InputFilterCriterionType(String displayName) {
    	this.displayName = displayName;
	}

	/**
 	* Return the display name for the enumeration value.
 	* @return the display name.
 	*/
	@Override
	public String toString() {
    	return displayName;
	}

/**
 * Return the enumeration value given a string name (case-independent).
 * @param name enumeration display value to look up
 * @return the enumeration value given a string name (case-independent), or null if not matched.
 */
public static InputFilterCriterionType valueOfIgnoreCase(String name) {
	if ( name == null ) {
		return null;
	}
    InputFilterCriterionType [] values = values();
    // Currently supported values
    for ( InputFilterCriterionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    return null;
}

}