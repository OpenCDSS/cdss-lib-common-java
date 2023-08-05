// GRColorRampType - color ramp named types

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
Color ramp types, used to generate color tables.
The enumerated values were previously integers in GRColorTable.
*/
public enum GRColorRampType {
	/**
	Gray color table.
	*/
	GRAY ( "Grey"),

	/**
	Blue to cyan color gradient.
	*/
	BLUE_TO_CYAN ( "BlueToCyan" ),

	/**
	Blue to magenta color gradient.
	*/
	BLUE_TO_MAGENTA ( "BlueToMagenta" ),

	/**
	Blue to red color gradient.
	*/
	BLUE_TO_RED ( "BlueToRed" ),

	/**
	Cyan to yellow color gradient.
	*/
	CYAN_TO_YELLOW ( "CyanToYellow" ),

	/**
	Magenta to cyan color gradient.
	*/
	MAGENTA_TO_CYAN ( "MagentaToCyan" ),

	/**
	Magenta to red color gradient.
	*/
	MAGENTA_TO_RED ( "MagentaToRed" ),

	/**
	Yellow to magenta color gradient.
	*/
	YELLOW_TO_MAGENTA ( "YellowToMagenta" ),

	/**
	Yellow to red color gradient.
	*/
	YELLOW_TO_RED ( "YellowToRed" );

    /**
     * The string name that should be displayed.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private GRColorRampType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Equals method to prevent common programming error of using the equals method instead of ==.
     */
    public boolean equals ( String displayName ) {
        if ( displayName.equalsIgnoreCase(this.displayName) ) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Return the available display names, as an array.
     */
    public static String [] getDisplayNames () {
    	String [] displayNames = new String[values().length];
    	int i = -1;
	    for ( GRColorRampType t : values() ) {
	    	++i;
	    	displayNames[i] = t.displayName;
	    }
	    return displayNames;
    }

    /**
     * Return the display name for the enumeration.
     * This is similar to the value but using appropriate mixed case.
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
	public static GRColorRampType valueOfIgnoreCase(String name) {
	    if ( name == null ) {
	        return null;
	    }
	    // Currently supported values.
	    for ( GRColorRampType t : values() ) {
	        if ( name.equalsIgnoreCase(t.toString()) ) {
	            return t;
	        }
	    }
	    return null;
	}
}