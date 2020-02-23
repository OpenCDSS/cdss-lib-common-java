// TransferDataHowType - enumeration that stores values for how to handle missing values when cumulating data

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
This enumeration indicates 
The main issue is how to deal with leap years.
*/
public enum TransferDataHowType
{

/**
Data from Feb 29 of a leap year will be transferred to Mar 1 on the non-leapyear time series.
*/
SEQUENTIALLY ("Sequentially"),

/**
Indicates that a time series transfer or analysis
should occur strictly by date/time.  In other words, if using
TRANSFER_BYDATETIME, Mar 1 will always line up with Mar 1, regardless of whether
leap years are encountered.
*/
BY_DATETIME ("ByDateTime");

/**
The name that should be displayed when the best fit type is used in UIs and reports.
*/
private final String displayName;

/**
Construct a time series statistic enumeration value.
@param displayName name that should be displayed in choices, etc.
*/
private TransferDataHowType(String displayName) {
    this.displayName = displayName;
}

/**
Return the display name for the enumeration.
This is usually the same as the value but using appropriate mixed case.
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
public static TransferDataHowType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    TransferDataHowType [] values = values();
    // Currently supported values
    for ( TransferDataHowType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}

}