// TableField - this class defines the fields (columns) in a table

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

import java.util.ArrayList;
import java.util.List;

/**
This class defines the fields (columns) in a table.  A DataTable is created
by specifying a list of TableField objects to pass into the DataTable
constructor.  Note that the field types have been implemented in a generic
sense; however, for historical reasons, the table design somewhat mimics Dbase data tables.
In Dbase files, it is somewhat ambiguous to know
when a numeric field is a floating point or integer.  It can be assumed that
a precision of zero for a numeric field indicates an integer.  However, at this
time, the DATA_TYPE_DOUBLE and DATA_TYPE_STRING types are used nearly exclusively.
@see RTi.Util.Table.Table
@see RTi.Util.Table.TableRecord
*/
public class TableField {

/**
4-byte integer.
*/
public final static int DATA_TYPE_INT = 0;

/**
2-byte integer.
*/
public final static int DATA_TYPE_SHORT = 1;

/**
8-byte double.
*/
public final static int DATA_TYPE_DOUBLE = 2;

/**
4-byte float.
*/
public final static int DATA_TYPE_FLOAT = 3;

/**
1-byte characters as string.
*/
public final static int DATA_TYPE_STRING = 4;

/**
Date and time, stored internally as Java Date object (use when simple data are
being manipulated).
*/
public final static int DATA_TYPE_DATE = 5;

/**
8-byte integer (long).
*/
public final static int DATA_TYPE_LONG = 6;

/**
Date and time, stored internally as DateTime object (advantage is can use precision and
other data to control object).
*/
public final static int DATA_TYPE_DATETIME = 7;

/**
Boolean.
*/
public final static int DATA_TYPE_BOOLEAN = 8;

/**
Used to indicate an array type of array.  See DATA_TYPE_ARRAY for more information.
*/
public final static int DATA_TYPE_ARRAY_BASE = 1000;

/**
Array of other types, for compatibility with java.sql.Types.ARRAY.
Subtract DATA_TYPE_ARRAY_BASE to get the type of objects in the array.
*/
public final static int DATA_TYPE_ARRAY = DATA_TYPE_ARRAY_BASE;

/**
Data type (DATA_TYPE_*) for the field (column).
If a simple data type the value corresponds to the data type.
If a complex data type such as ARRAY, the data type within the complex type requires some math,
which is handled by methods like isColumnArray().
*/
private int _data_type;

/**
Field name (also used for a column heading).
*/
private String _name;

/**
Field description, can be used for column tool tip when displaying table.
*/
private String __description = "";

/**
Units for the column, for example "mm".
*/
private String _units = "";

// TODO SAM 2011-04-27 Why is this needed - is it related to dBase constraint?
/**
Field width (e.g., maximum characters for strings or number width in characters).
*/
private int _width;

/**
Precision applied to numbers (e.g., 3 in 11.3 numbers).
*/
private int _precision;	

/**
Construct a new table of default type String.
The precision defaults to 10 characters, precision 0.
*/
public TableField ()
{	initialize ( DATA_TYPE_STRING, "", 10, 0);
}

/**
Construct a new table field for the specified type.
The precision defaults to 10 characters, precision 0.
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
*/
public TableField ( int type )
{	initialize ( type, "", 10, 0 );
}

/**
Copy constructor.
@param field table field to copy
*/
public TableField ( TableField field )
{
    initialize ( field.getDataType(), field.getName(), field.getWidth(), field.getPrecision() );
}

// TODO SAM 2011-12-25 Evaluate defaulting width to -1 for strings.
/**
Construct a new table field for the specified type and name.
The width defaults to 10 characters, precision 0.
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
*/
public TableField ( int type, String name )
{	initialize ( type, name, 10, 0 );
}

/**
Construct a new table field for the specified type and name.
The precision defaults to zero (precision is only applicable to floating point data).
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
@param width Field width in characters (-1 is allowed for variable-length strings).
*/
public TableField ( int type, String name, int width )
{	initialize ( type, name, width, 0 );
}

/**
Construct a new table field for the specified type and name.
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
@param width Field width in characters (-1 is allowed for variable-length strings).
@param precision Field precision in characters.  Used only for floating point data.
*/
public TableField ( int type, String name, int width, int precision )
{	initialize ( type, name, width, precision );
}

/**
Get type of data represented in this field.
@return data type (DATA_TYPE_*)
*/
public int getDataType ( )
{	return _data_type;
}

/**
TODO SAM 2009-07-22 Need to use an enum type class for the types but need to refactor code.
Get type of data represented in this field, as a String.
@return data type as string (e.g., DATA_TYPE_INT = "integer") or null if unknown.
*/
public static String getDataTypeAsString ( int dataType )
{   if ( dataType == DATA_TYPE_DATE ) {
        // Internally represented as a Java Date
        return "date";
    }
    else if ( dataType == DATA_TYPE_DATETIME ) {
        // Internally treated as DateTime object.
        return "datetime";
    }
    else if ( dataType == DATA_TYPE_DOUBLE ) {
        return "double";
    }
    else if ( dataType == DATA_TYPE_FLOAT ) {
        return "float";
    }
    else if ( dataType == DATA_TYPE_INT ) {
        return "integer";
    }
    else if ( dataType == DATA_TYPE_LONG ) {
        return "long";
    }
    else if ( dataType == DATA_TYPE_SHORT ) {
        return "short";
    }
    else if ( dataType == DATA_TYPE_STRING ) {
        return "string";
    }
    else {
        return null;
    }
}

/**
TODO SAM 2009-04-22 Need to use enum construct for data types.
Get the list of available data types, useful for displaying choices to users.
@return a list of data type strings, suitable for choices for users.
@param includeNote if true, include a note describing the data type using form "dataType - note".
*/
public static List<String> getDataTypeChoices ( boolean includeNote )
{
    List<String> dataTypeList = new ArrayList<String>();
    if ( includeNote ) {
    	dataTypeList.add ( "boolean - boolean (true/false)" );
        dataTypeList.add ( "datetime - date and time" );
        dataTypeList.add ( "double - double precision number" );
        dataTypeList.add ( "float - single precision number" );
        dataTypeList.add ( "integer - integer" );
        dataTypeList.add ( "long - long integer" );
        dataTypeList.add ( "short - short integer" );
        dataTypeList.add ( "string" );  
    }
    else {
    	dataTypeList.add ( "boolean" );
        dataTypeList.add ( "datetime" );
        dataTypeList.add ( "double" );
        dataTypeList.add ( "float" );
        dataTypeList.add ( "integer" );
        dataTypeList.add ( "long" );
        dataTypeList.add ( "short" );
        dataTypeList.add ( "string" );
    }
    return dataTypeList;
}

/**
Get field description.
@return field description
*/
public String getDescription ()
{   return __description;
}

/**
Get field name.
@return field name.
*/
public String getName ()
{	return _name;
}

/**
Get the field precision.
@return field precision (digits after .).
*/
public int getPrecision ( )
{	return _precision;
}

/**
Get the units for the column.
@return column units
*/
public String getUnits ()
{	return _units;
}

/**
Get the field width.
@return field width (overall character width).
*/
public int getWidth ( )
{	return _width;
}

/**
Initialize the instance.
@param type Data type for field.
@param name Field name.
@param width field width for output (-1 is allowed for variable-length strings).
@param precision digits after decimal for numbers.
*/
private void initialize ( int type, String name, int width, int precision )
{	_width = width;
	_precision = precision;
	_data_type = type;
	_name = name;
}

/**
TODO SAM 2009-07-22 Need to use an enum type class for the types and refactor code.
Lookup the type of data represented in this field as an internal integer given the string type representation.
@return data type as internal integer representation (e.g., DATA_TYPE_INT = "integer") or -1 if unknown.
*/
public static int lookupDataType ( String dataType )
{   if ( dataType.equalsIgnoreCase("date") ) {
        return DATA_TYPE_DATETIME;
    }
    else if ( dataType.equalsIgnoreCase("datetime") ) {
        return DATA_TYPE_DATETIME;
    }
    else if ( dataType.equalsIgnoreCase("double") ) {
        return DATA_TYPE_DOUBLE;
    }
    else if ( dataType.equalsIgnoreCase("float") ) {
        return DATA_TYPE_FLOAT;
    }
    else if ( dataType.equalsIgnoreCase("int") || dataType.equalsIgnoreCase("integer")) {
        return DATA_TYPE_INT;
    }
    else if ( dataType.equalsIgnoreCase("long") ) {
        return DATA_TYPE_LONG;
    }
    else if ( dataType.equalsIgnoreCase("short") ) {
        return DATA_TYPE_SHORT;
    }
    else if ( dataType.equalsIgnoreCase("string") ) {
        return DATA_TYPE_STRING;
    }
    else {
        return -1;
    }
}

/**
Set the data type.
@param data_type data type using DATA_TYPE_*.
*/
public void setDataType ( int data_type )
{	_data_type = data_type;
}

/**
Set the field description.
@param description field description
*/
public void setDescription ( String description )
{   if ( description != null ) {
        __description = description;
    }
}

/**
Set the field name.
@param name field name.
*/
public void setName ( String name )
{	if ( name != null ) {
		_name = name;
	}
}

/**
Set the field precision.
@param precision field precision (characters).
*/
public void setPrecision ( int precision )
{	_precision = precision;
}

/**
Set the units for the column.
@param units column units.
*/
public void setUnits ( String units )
{	if ( units != null ) {
		_units = units;
	}
}

/**
Set the field width.
@param width precision field width (characters).
*/
public void setWidth ( int width )
{	_width = width;
}

}
