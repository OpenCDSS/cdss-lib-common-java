// GRSymbolType - symbol types, point, line, etc.

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
Enumeration for symbol types, which indicate whether a point, line, or polygon is used.
This may be phased out given that symbols are primarily points (shapes, icon images, etc.).
*/
public enum GRSymbolType {
	/**
	No symbol.
	*/
	NONE("None"),

	/**
	Point symbol.  Color and symbol size can be specified.
	*/
	POINT("Point"),

	/**
	Line symbol.  Color, line width, and pattern can be specified.
	*/
	LINE("Line"),

	/**
	Line with points.  This is a combination of lines and points, suitable for a graph.
	*/
	LINE_AND_POINTS("LineAndPoints"),

	/**
	Polygon.  Color, outline color, and pattern can be specified.
	*/
	POLYGON("Polygon"),

	/**
	Polygon and draw a point symbol.  Symbol, color, outline color, and pattern can be specified.
	*/
	POLYGON_AND_POINT("PolygonAndPoint");

    /**
     * The string name that should be displayed.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRSymbolType(String displayName) {
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
	public static GRSymbolType valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRSymbolType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}