// DayBoundaryType - enumeration to store values for how midnight is handled for day boundary

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
This enumeration stores values for how a day's boundary is handled when time is considered.
For example, when computing interval output time series,
should input time series considers before, at, or after midnight,
depending on whether at the start or end of the interval.
*/
public enum DayBoundaryType {
    /**
     * Values before midnight are considered to be OK for a dataset.
     */
    BEFORE_MIDNIGHT("BeforeMidnight"),

    /**
     * The data are not expected to reset (will continue increasing indefinitely, or decreasing indefinitely).
     */
    MIDNIGHT("Midnight"),

	/**
	 * The reset occurs at a value, such as roll-over of a maximum sensor value.
	 */
	AFTER_MIDNIGHT("AfterMidnight");

    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private DayBoundaryType(String displayName) {
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
    public static DayBoundaryType valueOfIgnoreCase(String name) {
    	if ( name == null ) {
    		return null;
    	}

    	DayBoundaryType [] values = values();
    	// Currently supported values.
    	for ( DayBoundaryType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

}