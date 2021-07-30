// CheckType - data check types, typically used in analysis code that checks time series data or statistic values against some criteria

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
Data check types, typically used in analysis code that checks time series data or statistic
values against some criteria.  Not all of these checks may be appropriate for code that uses the enumeration and
therefore a subset should be created as appropriate (e.g., repeating values apply to time series data but
probably not statistics computed from the data).
*/
public enum CheckType
{
/**
 * Absolute change from one value to the next > (in data units).
 */
ABS_CHANGE_GREATER_THAN("AbsChange>"),
/**
 * Absolute change from one value to the next > (percent).
 */
ABS_CHANGE_PERCENT_GREATER_THAN("AbsChangePercent>"),
/**
 * Change from one value to the next > (in data units).
 */
CHANGE_GREATER_THAN("Change>"),
/**
 * Change from one value to the next < (in data units).
 */
CHANGE_LESS_THAN("Change<"),
/**
 * Change in range of values (in data units).
 */
IN_RANGE("InRange"),
/**
 * Change out of range of values (in data units).
 */
OUT_OF_RANGE("OutOfRange"),
/**
 * Is value missing.
 */
MISSING("Missing"),
/**
 * Does value repeat.
 */
REPEAT("Repeat"),
/**
 * Is value less than.
 */
LESS_THAN("<"),
/**
 * Is value less than or equal to.
 */
LESS_THAN_OR_EQUAL_TO("<="),
/**
 * Is value greater than.
 */
GREATER_THAN(">"),
/**
 * Is value greater than or equal to.
 */
GREATER_THAN_OR_EQUAL_TO(">="),
/**
 * Is value equal to.
 */
EQUAL_TO("=="),
/**
 * Is value not equal to.
 * Care should be taken when using with floating point numbers due to truncation and rounding,
 * but should work with integers, date/times, strings.
 */
NOT_EQUAL_TO("!=");

/**
 * The name that should be displayed when the best fit type is used in UIs and reports.
 */
private final String displayName;

/**
 * Construct a time series statistic enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private CheckType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the statistic.  This is usually the same as the
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
public static CheckType valueOfIgnoreCase(String name)
{
    CheckType [] values = values();
    for ( CheckType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
