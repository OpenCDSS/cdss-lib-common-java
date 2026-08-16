// ValueTemporalReferenceType - enumeration to indicate how interval time stamp aligns

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
This enumeration indicates how the interval timestamp aligns with interval time series.
*/
public enum ValueTemporalReferenceType {
    /**
     * Timestamp aligns with the end of the duration for irregular interval time series.
     */
    DURATION_END("DurationEnd"),

    /**
     * Timestamp aligns with the start of the duration for irregular interval data.
     */
    DURATION_START("DurationStart"),

    /**
     * Timestamp aligns with an instance for the precision of date/time.
     */
    INSTANT("Instant"),

    /**
     * Timestamp aligns with the end of the interval for regular interval time series.
     */
    INTERVAL_END("IntervalEnd"),

    /**
     * Timestamp aligns with the start of the interval for regular interval time series.
     */
    INTERVAL_START("IntervalStart"),

    /**
     * Similar to INSTANT, but for a date.
     */
    DATE("Date"),

    /**
     * Temporal reference is unknown. Typically will be INSTANT for irregular data and INTERVAL_END for interval data.
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
    private ValueTemporalReferenceType ( String displayName ) {
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
    public static ValueTemporalReferenceType valueOfIgnoreCase ( String name ) {
    	if ( name == null ) {
    		return null;
    	}

    	ValueTemporalReferenceType [] values = values();
    	// Currently supported values.
    	for ( ValueTemporalReferenceType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

    /**
     * Return the enumeration value given a string name (case-independent).
     * @param temporalReferences list of enumeration values to compare.
     * @return the enumeration value given a string name (case-independent), or null if not matched.
     */
    public static ValueTemporalReferenceType valueOfIgnoreCase ( List<ValueTemporalReferenceType> temporalReferences, String name ) {
    	if ( (temporalReferences == null) || (temporalReferences.size() == 0) ) {
    		return null;
    	}
    	ValueTemporalReferenceType [] array = new ValueTemporalReferenceType[temporalReferences.size()];
    	int i = 0;
    	for ( ValueTemporalReferenceType temporalReference : temporalReferences ) {
    		array[i++] = temporalReference;
    	}
    	return valueOfIgnoreCase ( array, name );
    }

    /**
     * Return the enumeration value given a string name (case-independent).
     * @param temporalReferences array of enumeration values to compare.
     * @return the enumeration value given a string name (case-independent), or null if not matched.
     */
    public static ValueTemporalReferenceType valueOfIgnoreCase ( ValueTemporalReferenceType [] temporalReferences, String name ) {
    	if ( name == null ) {
        	return null;
    	}
    	for ( ValueTemporalReferenceType t : temporalReferences ) {
        	if ( name.equalsIgnoreCase(t.toString()) ) {
            	return t;
        	}
    	}
    	return null;
	}

}