// TSGraphMouseTrackerType - the mouse tracker mode, which controls the behavior of the tracker

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

package RTi.GRTS;

/**
The mouse tracker mode, which controls the behavior of the tracker.
*/
public enum TSGraphMouseTrackerType
{
	/**
	 * Track the single nearest point to the mouse.
	 */
    NEAREST("Nearest"),
	/**
	 * Same as NEAREST, but also label with the Alias or TSID.
	 */
    NEAREST_WITH_ID("NearestWithId"),
    /**
     * Track single nearest point to the mouse, for only selected time series.
     */
    NEAREST_SELECTED("NearestSelected"),
    /**
     * Track the nearest point(s) to the time for the mouse.
     */
    NEAREST_TIME("NearestTime"),
    /**
     * Track the nearest point(s) to the time for the mouse, for only selected time series.
     */
    NEAREST_TIME_SELECTED("NearestTimeSelected"),
    /**
     * Used with raster graph to show cross-hair drawn to the axes.
     */
    XYAXES("XYAxes"),
    /**
     * Do not track the mouse.
     */
    NONE("None");
    
    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphMouseTrackerType(String displayName) {
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
    public static TSGraphMouseTrackerType valueOfIgnoreCase(String name)
    {
        if ( name == null ) {
            return null;
        }
        TSGraphMouseTrackerType [] values = values();
        for ( TSGraphMouseTrackerType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}
