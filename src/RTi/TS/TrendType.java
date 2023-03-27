// TrendType - enumeration to store values for a trend, meaning whether data values increase over time, decrease, or are variable

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

package RTi.TS;

/**
This enumeration stores values for a trend, meaning whether data values increase over time, decrease, or are variable.
For example, the trend for accumulated precipitation data is that values increase over time.
*/
public enum TrendType {
    /**
     * Trend in data is decreasing.
     */
    DECREASING("Decreasing"),

    /**
     * Trend in data is increasing.
     */
    INCREASING("Increasing"),

	/**
	 * Trend in values is variable (some increasing and decreasing).
	 */
	VARIABLE("Variable");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TrendType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name for the enumeration.
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
    public static TrendType valueOfIgnoreCase(String name) {
    	if ( name == null ) {
    		return null;
    	}
        TrendType [] values = values();
        // Currently supported values.
        for ( TrendType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }

}