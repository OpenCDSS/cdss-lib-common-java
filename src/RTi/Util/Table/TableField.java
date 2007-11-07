// ----------------------------------------------------------------------------
// TableField - define the field in a DataTable
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 23 Jun 1999	Catherine E.
//		Nutting-Lane, RTi	Initial version
// 2001-09-17	Steven A. Malers, RTi	Review code.  Add width and precision
//					to be able to fully store dBase field
//					properties.  Add finalize() and clean
//					up Javadoc.  Change "heading" to "name"
//					to be more consistent with other code.
// 2001-10-03	SAM, RTi		Add DATA_TYPE_DATE field.
// ----------------------------------------------------------------------------

package RTi.Util.Table;

/**
This class is used to help define the fields in a table.  A DataTable is created
by specifying a vector of TableField objects to pass into the DataTable
constructor.  Note that the field types have been implemented in a generic
sense; however, most application of this class has been with ESRI shapefiles,
which use Dbase data tables.  In Dbase files, it is somewhat ambiguous to know
when a numeric field is a floating point or integer.  It can be assumed that
a precision of zero for a numeric field indicates an integer.  However, at this
time, the DATA_TYPE_DOUBLE and DATA_TYPE_STRING types are used nearly
exclusively.
@see RTi.Util.Table.Table
@see RTi.Util.Table.TableRecord
*/
public class TableField {

/**
4-byte integer.
*/
public final static int DATA_TYPE_INT		= 0;

/**
2-byte integer.
*/
public final static int DATA_TYPE_SHORT		= 1;

/**
8-byte double.
*/
public final static int DATA_TYPE_DOUBLE	= 2;

/**
4-byte float.
*/
public final static int DATA_TYPE_FLOAT		= 3;

/**
1-byte characters as string.
*/
public final static int DATA_TYPE_STRING	= 4;

/**
Date.
*/
public final static int DATA_TYPE_DATE		= 5;

private int	_data_type;	// Data type - DATA_TYPE_* from above
private String	_name;		// Field name (also used for a column heading)
private int	_width;		// Field width (e.g., maximum characters for
				// strings or number width in characters).
private int	_precision;	// Precision applied to numbers (e.g., 3 in
				// 11.3 numbers).

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
Construct a new table field for the specified type and name.
The precision defaults to 10 characters, precision 0.
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
*/
public TableField ( int type, String name )
{	initialize ( type, name, 10, 0 );
}

/**
Construct a new table field for the specified type and name.
The precision defaults to zero (precision is only applicable to floating point
data).
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
@param width Field width in characters.
data.
*/
public TableField ( int type, String name, int width )
{	initialize ( type, name, width, 0 );
}

/**
Construct a new table field for the specified type and name.
@param type Type of data associated with a particular column within
a DataTable.  Use TableField.DATA_TYPE_*
@param name Field name.
@param width Field width in characters.
@param precision Field precision in characters.  Used only for floating point
data.
*/
public TableField ( int type, String name, int width, int precision )
{	initialize ( type, name, width, precision );
}

/**
Clean up for garbage collection.
*/
protected void finalize ()
throws Throwable
{	_name = null;
	super.finalize();
}

/**
Initialize the instance.
@param type Data type for field.
@param name Field name.
*/
private void initialize ( int type, String name, int width, int precision )
{	_width = width;
	_precision = precision;
	_data_type = type;
	_name = name;
}

/**
Get type of data represented in this field.
@return data type (DATA_TYPE_*)
*/
public int getDataType ( )
{	return _data_type;
}

/**
Get heading assigned to this field.
@return heading title for data field.
@deprecated Use getName();
*/
public String getHeading ()
{	return getName();
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
Get the field width.
@return field width (overall character width).
*/
public int getWidth ( )
{	return _width;
}

/**
Set the data type.
@param data_type data type using DATA_TYPE_*.
*/
public void setDataType ( int data_type )
{	_data_type = data_type;
}

/**
Set the heading for the field.
@param heading title value.
@deprecated Use setName().
*/
public void setHeading ( String heading )
{	setName ( heading );
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
Set the field width.
@param width precision field width (characters).
*/
public void setWidth ( int width )
{	_width = width;
}

} // End of TableField class
