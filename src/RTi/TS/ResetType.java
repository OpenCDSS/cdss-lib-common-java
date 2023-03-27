// ResetType - enumeration to store values for how time series accumulation resets

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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
This enumeration stores values for how a time series accumulation resets.
For example, cumulative precipitation may reset when it reaches the limit of a tipping bucket counter.
*/
public enum ResetType {
    /**
     * Automatically detect reset, typically when the accumulation value goes the other direction from expected.
     * Requires that the trend direction is specified.
     */
    AUTO("Auto"),

    /**
     * The data are not expected to reset (will continue increasing indefinitely, or decreasing indefinitely).
     */
    NONE("None"),

	/**
	 * The reset occurs at a value, such as roll-over of a maximum sensor value.
	 */
	ROLLOVER("Rollover"),

	/**
	 * Unknown reset type.
	 */
	UNKNOWN("Unknown");

	// TODO smalers 2023-03-31 RestTime, ResetDate, ResetDateTime to reset at a point in time such as day of the year.

    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private ResetType(String displayName) {
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
    public static ResetType valueOfIgnoreCase(String name) {
    	if ( name == null ) {
    		return null;
    	}

    	ResetType [] values = values();
    	// Currently supported values
    	for ( ResetType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

}