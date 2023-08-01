// GRSymbolShapeTypeOrder - enumeration of symbol type order

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
Symbol shape types order to indicate what is included in shape type lists.
*/
public enum GRSymbolShapeTypeListContents {
	/**
	 * Shape types to use for UI choices, for example user-facing symbols,
	 * pretty close to an alphabetical listing of the display names.
	 */
	APP_CHOICES ( "AppChoices"),

	/**
	 * Shape types to use GeoLayer product defaults, to preserve legacy behavior.
	 */
	GEOLAYER_DEFAULT ( "GeoLayerDefault");

    /**
     * The string name that should be displayed with toString().
     * This will likely never be used because the enumeration is used in low-level code,
     * but may be useful for logging and troubleshooting.
     */
    private final String displayName;
    
    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRSymbolShapeTypeListContents(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     */
    public boolean equals ( String lineStyleType ) {
        if ( lineStyleType.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Return the display name for the symbol type list contents.
     * This is usually the same as the value but using appropriate mixed case.
     * @return the display name.
     */
    @Override
    public String toString() {
        return this.displayName;
    }

	/**
	 * Return the enumeration value given a string name (case-independent).
	 * @return the enumeration value given a string name (case-independent), or null if not matched.
	 */
	public static GRSymbolShapeTypeListContents valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRSymbolShapeTypeListContents t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}