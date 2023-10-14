// TSGraphInteractionType - types for interacting with a TSGraph

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
How a user interacts with a TSGraph.
*/
public enum TSGraphInteractionType {
	/**
	Edit mode. Enables editing of points by clicking above or below a point to change the point's y value.
	X value editing is not supported.
	*/
	EDIT("Edit"),

	/**
	Interaction modes.
	No special interaction.
	*/
	NONE("None"),

	/**
	Select a feature.  When enabled, a mouse click causes the select() method of registered TSViewListeners to be called.
	*/
	SELECT("Select"),

	/**
	Zoom mode.  This enables a rubber-band line (if the extents are bigger than 5 pixels in both direction.
	The zoom() method of registered TSViewListeners are called.
	*/
	ZOOM("Zoom");

    private final String displayName;

    /**
     * Name for type, for user interface displays.
     * @param displayName
     */
    private TSGraphInteractionType(String displayName) {
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
    public static TSGraphInteractionType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        TSGraphInteractionType [] values = values();
        for ( TSGraphInteractionType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}