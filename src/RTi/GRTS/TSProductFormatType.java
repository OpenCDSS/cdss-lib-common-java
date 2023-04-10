// TSProductFormatType - format for TSProduct text representation, indicating formatting before display or writing to file

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

package RTi.GRTS;

/**
Format for TSProduct text representation, indicating formatting before display or writing to file.
*/
public enum TSProductFormatType {
    /**
    Legacy properties list, similar to INI file.
    */
    PROPERTIES("Properties"),

    /**
    JSON without line breaks or pretty formatting.
    */
    JSON_COMPACT ("JSONCompact"),

    /**
    JSON with line breaks and pretty formatting.
    */
    JSON_PRETTY("JSONPretty");

    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSProductFormatType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
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
    public static TSProductFormatType valueOfIgnoreCase(String name) {
    	if ( name == null ) {
    		return null;
    	}
        TSProductFormatType [] values = values();
        for ( TSProductFormatType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}