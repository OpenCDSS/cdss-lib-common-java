// GRClassificationType - classification types, used for symbols

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

package RTi.GR;

/**
Names for symbol classifications, for use with persistent storage and graphical user interfaces.
*/
public enum GRClassificationType {
	/**
	Single symbol.
	*/
	SINGLE("Single"),

	/**
	One symbol per value, but all symbols are
	the same other than color or other characteristics that can automatically be assigned.
	*/
	UNIQUE_VALUES("UnqueValues"),

	/**
	Class breaks, in which the symbol is graded based on
	the data value into groups of colors, sizes, patterns, etc.
	*/
	CLASS_BREAKS("ClassBreaks"),

	/**
	Single symbol style, but the size of the symbol is
	scaled to "exact" data size based on a data value (unlike unique values
	where different symbols are used for each value or class breaks where symbols have definite breaks).
	*/
	SCALED_SYMBOL("ScaledSymbol"),

	/**
	Single symbol style, where the size of the teacup
	is scaled and the amount that the teacup is filled is dependent on values read from an attribute table.
	*/
	SCALED_TEACUP_SYMBOL("ScaledTeacupSymbol");

    /**
     * The string name that should be displayed.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRClassificationType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     * @type the type name for the enumeration, checked against the display name.
     */
    public boolean equals ( String type ) {
        if ( type.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Return the display name for the type.
     * This is usually the same as the value but using appropriate mixed case.
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
	public static GRClassificationType valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRClassificationType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}