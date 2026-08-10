// ValueIntervalClosureType - enumeration to indicate how sample data are handled on interval boundaries

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2026 Colorado Department of Natural Resources

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

import java.util.List;

/**
This enumeration indicates how sample data are handled on interval boundaries,
for regular interval time series and duration-based irregular interval time series.
*/
public enum ValueIntervalClosureType {
    /**
     * Start of interval or duration is included.
     */
    START_INCLUSIVE("StartInclusive"),

    /**
     * End of interval or duration is included.
     */
    END_INCLUSIVE("EndInclusive"),

    /**
     * Start and end of interval or duration are included.
     */
    START_AND_END_INCLUSIVE("StartAndEndInclusive"),

    /**
     * Closure is not applicable (e..g, irregular or instantaneous data).
     */
    NA("NA"),

    /**
     * Closure is unknown for duration or interval data (e.g., don't know what source data uses).
     */
    UNKNOWN("Unknown");

    /**
     * The name that should be displayed.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private ValueIntervalClosureType ( String displayName ) {
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
    public static ValueIntervalClosureType valueOfIgnoreCase ( String name ) {
    	if ( name == null ) {
    		return null;
    	}

    	ValueIntervalClosureType [] values = values();
    	// Currently supported values.
    	for ( ValueIntervalClosureType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

    /**
     * Return the enumeration value given a string name (case-independent).
     * @param intervalClosures list of enumeration values to compare
     * @return the enumeration value given a string name (case-independent), or null if not matched
     */
    public static ValueIntervalClosureType valueOfIgnoreCase ( List<ValueIntervalClosureType> intervalClosures, String name ) {
    	if ( (intervalClosures == null) || (intervalClosures.size() == 0) ) {
    		return null;
    	}
    	ValueIntervalClosureType [] array = new ValueIntervalClosureType[intervalClosures.size()];
    	int i = 0;
    	for ( ValueIntervalClosureType temporalReference : intervalClosures ) {
    		array[i++] = temporalReference;
    	}
    	return valueOfIgnoreCase ( array, name );
    }

    /**
     * Return the enumeration value given a string name (case-independent).
     * @param intervalClosures array of enumeration values to compare
     * @return the enumeration value given a string name (case-independent), or null if not matched
     */
    public static ValueIntervalClosureType valueOfIgnoreCase ( ValueIntervalClosureType [] intervalClosures, String name ) {
    	if ( name == null ) {
        	return null;
    	}
    	for ( ValueIntervalClosureType t : intervalClosures ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

}