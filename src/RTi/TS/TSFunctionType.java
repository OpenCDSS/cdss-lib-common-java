// TSFunctionType - enumeration that defines time series function types, which are functions that are used to assign data to time series

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
This enumeration defines time series function types, which are functions that are used to assign data
to time series.  Consequently, x = f(y) indicates y as the independent date/time and x being the generated time series value.
*/
public enum TSFunctionType
{
    /**
    Assign the year to the whole number part of the value.
    */
    DATE_YYYY("DateYYYY"),
    /**
    Assign the year and month to the whole number part of the value.
    */
    DATE_YYYYMM("DateYYYYMM"),
    /**
    Assign the year, month, and day to the whole number part of the value.
    */
    DATE_YYYYMMDD("DateYYYYMMDD"),
    /**
    Assign the date and time (to hour) to the whole number part of the value and the hour as the
    fraction (hour 1 = .01, hour 24 = .24).
    */
    DATETIME_YYYYMMDD_HH("DateTimeYYYYMMDD_hh"),
    /**
    Assign the date to the whole number part of the value and the hour and minute as the
    fraction (hour 1, minute 1 = .0101, hour 24, 59 = .2459).
    */
    DATETIME_YYYYMMDD_HHMM("DateTimeYYYYMMDD_hhmm"),
    /**
    Assign a random number in the range 0.0 to 1.0.
    */
    RANDOM_0_1("Random_0_1"),
    /**
    Assign a random number in the range 0.0 to 1000.0.
    */
    RANDOM_0_1000("Random_0_1000");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;
    
    /**
     * Construct a time series statistic enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private TSFunctionType(String displayName) {
        this.displayName = displayName;
    }

/**
Return the display name for the type.  This is usually similar to the
value but using appropriate mixed case.
@return the display name.
*/
@Override
public String toString() {
    return displayName;
}

/**
Return the enumeration value given a string name (case-independent).
@return the enumeration value given a string name (case-independent), or null if not matched.
*/
public static TSFunctionType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    TSFunctionType [] values = values();
    for ( TSFunctionType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}
