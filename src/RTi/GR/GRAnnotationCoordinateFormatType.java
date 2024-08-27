// GRAnnotationCoordinteFormatType - enumeration of annotation coordinate format types

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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
Annotation coordinate format types.
For example, use to indicate that an X or Y value is a date/time.
This is used internally to tell generic annotation code how to handle annotation coordinate data values.
*/
public enum GRAnnotationCoordinateFormatType {
    /**
     * No special formatting (numerical data value or percent formatted as a number using the label precision).
     */
    NONE("None"),

    /**
     * Label should be formatted as a date/time.
     */
    DATETIME("DateTime"),

    /**
     * Under development?
     */
    REPEAT_DATA("RepeatData"),

    /**
     * Under development?
     */
    REPEAT_PERCENT("RepeatPercent");

    /**
     * The string name that should be displayed.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRAnnotationCoordinateFormatType ( String displayName ) {
        this.displayName = displayName;
    }

    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     * @string axisLabelFormatType the axis label format type to compare to
     * @return true if the strings are equal
     */
    public boolean equals ( String axisLabelFormatType ) {
        if ( axisLabelFormatType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Return the display name for the line style type.
     * This is usually the same as the value but using appropriate mixed case.
     * @return the display name.
     */
    @Override
    public String toString() {
        return displayName;
    }

	/**
	 * Return the enumeration value given a string name (case-independent).
	 * @param name the string name to match
	 * @return the enumeration value given a string name (case-independent), or null if not matched.
	 */
	public static GRAnnotationCoordinateFormatType valueOfIgnoreCase ( String name ) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRAnnotationCoordinateFormatType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}