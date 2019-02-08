// RunningAverageType - enumeration for running average types, used to determine the sample for the analysis.

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
This enumeration defines running average types, used to determine the sample for the analysis.
The enumeration can be used with more than average statistic.
*/
public enum RunningAverageType
{
    /**
    Average values from all the available years.
    */
    ALL_YEARS("AllYears"),
    /**
    Average values from both sides of the time step, inclusive of the center value.
    */
    CENTERED("Centered"),
    /**
    Custom bracket using custom offsets.  For example, at an interval, the sample may be determined by a future bracket.
    */
    CUSTOM("Custom"),
    /**
    Average values from the same time step for each of the previous N -1 years and the current year.
    */
    NYEAR("NYear"),
    /**
    Average values from the same time step for all of the previous years and the current year (similar to
    N-Year but for all years.
    */
    N_ALL_YEAR("NAllYear"),
    /**
    Average values prior to the current time step, not inclusive of the current point.
    */
    PREVIOUS("Previous"),
    /**
    Average values prior to the current time step, inclusive of the current point.
    */
    PREVIOUS_INCLUSIVE("PreviousInclusive"),
    /**
    Average values after to the current time step, not inclusive of the current point.
    */
    FUTURE("Future"),
    /**
    Create a running average by averaging values after to the current time step, inclusive of the current point.
    */
    FUTURE_INCLUSIVE("FutureInclusive");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private RunningAverageType(String displayName) {
        this.displayName = displayName;
    }

/**
 * Return the display name for the running average type.  This is usually similar to the
 * value but using appropriate mixed case.
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
public static RunningAverageType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    // Legacy
    if ( name.equalsIgnoreCase("N-Year") ) {
        // Replaced with newer "NYear"
        return NYEAR;
    }
    RunningAverageType [] values = values();
    for ( RunningAverageType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
