// TableColumnType - enumeration of data table column types.

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
Enumeration of data table column types.
These eventually will replace the numerical values in the TableField class.
*/
public enum TableColumnType
{

/**
Java Boolean.
*/
BOOLEAN(TableField.DATA_TYPE_BOOLEAN,"Boolean"),
/**
4-byte integer, Java Integer.
*/
INT(TableField.DATA_TYPE_INT,"Integer"),
/**
2-byte integer, Java Short.
*/
SHORT(TableField.DATA_TYPE_SHORT,"Short"),
/**
8-byte double, Java Double.
*/
DOUBLE(TableField.DATA_TYPE_DOUBLE,"Double"),
/**
4-byte float, Java Float.
*/
FLOAT(TableField.DATA_TYPE_FLOAT,"Float"),
/**
Java String.
*/
STRING(TableField.DATA_TYPE_STRING,"String"),
/**
Java date and optionally time.
*/
DATE(TableField.DATA_TYPE_DATE,"Date"),
/**
8-byte integer, Java Long.
*/
LONG(TableField.DATA_TYPE_LONG,"Long"),
/**
DateTime.
*/
DateTime(TableField.DATA_TYPE_DATETIME,"DateTime");

/**
The name that should be displayed when used in UIs and reports.
*/
private final String displayName;

/**
The internal code for the enumeration, matches the TableField definitions to bridge legacy code.
*/
private final int code;

/**
Construct an enumeration value from a string.
@param displayName name that should be displayed in choices, etc.
*/
private TableColumnType(int code, String displayName) {
    this.code = code;
    this.displayName = displayName;
}

/**
 * Return the display name for the table column data type.  This is usually the same as the
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
public static TableColumnType valueOfIgnoreCase(String name)
{   if ( name == null ) {
        return null;
    }
    TableColumnType [] values = values();
    // Special case
    if ( name.equalsIgnoreCase("DateTime") ) {
        return DATE;
    }
    // Currently supported values
    for ( TableColumnType t : values ) {
        if ( name.equalsIgnoreCase(t.toString()) ) {
            return t;
        }
    }
    return null;
}

/**
Return the enumeration value given a code value.
@return the enumeration value given a code value, or null if not matched.
*/
public static TableColumnType valueOf(int code)
{
    TableColumnType [] values = values();
    // Currently supported values
    for ( TableColumnType t : values ) {
        if ( code == t.code ) {
            return t;
        }
    } 
    return null;
}
    
}