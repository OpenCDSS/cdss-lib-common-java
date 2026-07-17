// DeltaValueType - enumeration to store values for what value is computed as a delta between time series values

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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
This enumeration stores values indicating the delta value between time series values.
*/
public enum DeltaValueType {
    /**
     * Delta is the difference between the time series values.
     */
    DATA_VALUE("DataValue"),

    /**
     * Delta is the difference between the time series time stamps, in seconds.
     */
    TIME_SECONDS("TimeSeconds"),

    /**
     * Delta is the difference between the time series time stamps, in minutes.
     */
    TIME_MINUTES("TimeMinutes"),

    /**
     * Delta is the difference between the time series time stamps, in hours.
     */
    TIME_HOURS("TimeHours"),

    /**
     * Delta is the difference between the time series time stamps, in days.
     */
    TIME_DAYS("TimeDays"),

    /**
     * Delta is the difference between the time series time stamps, in months.
     */
    TIME_MONTHS("TimeMonths");

    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private DeltaValueType ( String displayName ) {
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
    public static DeltaValueType valueOfIgnoreCase(String name) {
    	if ( name == null ) {
    		return null;
    	}
        DeltaValueType [] values = values();
        // Currently supported values.
        for ( DeltaValueType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }

}