// TSGraphDrawingStepType - the steps that occur during drawing, to help code like annotations

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
The rendering sequence steps that occur during drawing, to help code like annotations.
*/
public enum TSGraphDrawingStepType {
	/**
	 * Before drawing anything related to the back axes, which is underlying border
	 * (appropriate for Rectangle annotations that should be drawn under axes).
	 */
    BEFORE_BACK_AXES("BeforeBackAxes"),

    /**
     * After drawing anything related to the back axes.
     */
    AFTER_BACK_AXES("AfterBackAxes"),

    /**
     * Before drawing any data (time series).
     */
    BEFORE_DATA("BeforeData"),

    /**
     * After drawing data (time series).
     */
    AFTER_DATA("AfterData");

    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphDrawingStepType(String displayName) {
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
    public static TSGraphDrawingStepType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        TSGraphDrawingStepType [] values = values();
        for ( TSGraphDrawingStepType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}
