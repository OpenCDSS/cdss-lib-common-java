// DataTableMathOperatorType - enumeration of simple math operators that can be performed on table cells

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
 * Enumeration of simple math operators that can be performed on table cells.
 * @author sam
 *
 */
public enum DataTableMathOperatorType
{

/**
 * Add values.
 */
ADD("+"),
/**
 * Assign values.
 */
ASSIGN("="),
/**
 * Divide values.
 */
DIVIDE ( "/" ),
/**
 * Multiply values.
 */
MULTIPLY ( "*" ),
/**
 * Subtract values.
 */
SUBTRACT ( "-" ),
/**
TODO SAM 2013-08-26 Need to enable
Convert to double.
*/
//TO_DOUBLE ( "ToDouble" ),
/**
Convert to integer.
*/
TO_INTEGER ( "ToInteger" );

/**
 * The name that should be displayed when used in UIs and reports.
 */
private final String displayName;

/**
 * Construct an enumeration value from a string.
 * @param displayName name that should be displayed in choices, etc.
 */
private DataTableMathOperatorType(String displayName) {
    this.displayName = displayName;
}

/**
 * Return the display name for the math operator.  This is usually the same as the
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
public static DataTableMathOperatorType valueOfIgnoreCase(String name)
{
    DataTableMathOperatorType [] values = values();
    // Currently supported values
    for ( DataTableMathOperatorType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    } 
    return null;
}
    
}
