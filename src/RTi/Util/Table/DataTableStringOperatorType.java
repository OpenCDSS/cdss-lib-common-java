// DataTableStringOperatorType - enumeration of simple string operators that can be performed on table cells

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

package RTi.Util.Table;

/**
Enumeration of simple string operators that can be performed on table cells.
*/
public enum DataTableStringOperatorType
{

/*
Append string values.
*/
APPEND("Append"),
/*
Prepend string values.
*/
PREPEND("Prepend"),
/*
Replace string substring.
*/
REPLACE("Replace"),
/*
Remove string substring.
*/
REMOVE("Remove"),
/*
Return a token split from the string based on a delimiter.
*/
SPLIT("Split"),
/*
Return a substring.
*/
SUBSTRING("Substring"),
/*
Cast a string value to a boolean.
*/
// TODO SAM 2015-04-29 Need to enable Boolean
//TO_BOOLEAN("ToBoolean"),
/*
Cast a string value to a date.
*/
TO_DATE("ToDate"),
/*
Cast a string value to a date/time.
*/
TO_DATE_TIME("ToDateTime"),
/*
Cast a string value to a double.
*/
TO_DOUBLE("ToDouble"),
/*
Cast a string value to an integer.
*/
TO_INTEGER("ToInteger"),
/*
Convert a string value to lower case.
*/
TO_LOWERCASE("ToLowerCase"),
/*
Convert a string value to mixed case.
*/
TO_MIXEDCASE("ToMixedCase"),
/*
Convert a string value to upper case.
*/
TO_UPPERCASE("ToUpperCase");

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct an enumeration value.
 * @param displayName name that should be displayed in choices, etc.
 */
private DataTableStringOperatorType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the string operator.  This is usually the same as the
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
public static DataTableStringOperatorType valueOfIgnoreCase(String name)
{
    if ( name == null ) {
        return null;
    }
    DataTableStringOperatorType [] values = values();
    // Currently supported values
    for ( DataTableStringOperatorType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}
