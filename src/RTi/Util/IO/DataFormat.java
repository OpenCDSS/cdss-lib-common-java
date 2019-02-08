// DataFormat - data format class

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

// ----------------------------------------------------------------------------
// DataFormat - data format class
// ----------------------------------------------------------------------------
// History:
//
// 12 Jan 1998	Steven A. Malers, RTi	Initial version.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 18 May 2001	SAM, RTi		Change toString() to return
//					_format_string like C++ and make so
//					the format string is created whenever
//					a field is set.
// 2001-11-06	SAM, RTi		Review javadoc.  Verify that variables
//					are set to null when no longer used.
// 2001-12-09	SAM, RTi		Copy all TSUnits* classes to Data*
//					to allow general use.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import RTi.Util.String.StringUtil;

/**
The DataFormat is a simple class used to hold information about data
formatting (e.g., precision for output).  It is primarily used by the DataUnits class.
@see DataUnits
*/
public class DataFormat
{

private String	_format_string;		// C-style format string for data.
private int	_precision;		// The number of digits of precision
					// after the decimal point on output.
private int	_width;			// The total width of the format.

/**
Construct and set the output width to 10 digits, 2 after the decimal
point, and use a %g format.
*/
public DataFormat ()
{	initialize ();
}

/**
Copy constructor.
@param format DataFormat to copy.
*/
public DataFormat ( DataFormat format )
{	initialize();
	setFormatString ( format._format_string );
	setPrecision ( format._precision );
	setWidth ( format._width );
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize ()
throws Throwable
{	_format_string = null;
	super.finalize();
}

/**
Return the format string to use for output.
@return The format string to use for output.  This is a C-style format string
(use with StringUtil.formatString()).
@see RTi.Util.String.StringUtil#formatString
*/
public String getFormatString ( )
{	return _format_string;
}

/**
Return the precision (number of digits after the decimal point).
@return The precision (number of digits after the decimal point) to use for
formatting.
*/
public int getPrecision ( )
{	return _precision;
}

/**
Return the width of the output when formatting.
@return The width of the output when formatting.
*/
public int getWidth ( )
{	return _width;
}

/**
Initialize data members.
*/
private void initialize ()
{	_precision = 2;
	_width = 10;
	setFormatString();
}

/**
Refresh the value of the format string based on the width and precision.
*/
private void setFormatString ()
{	if ( _width <= 0 ) {
		_format_string = "%." + _precision + "f";
	}
	else {	_format_string = "%" + _width + "." + _precision + "f";
	}
}

/**
Set the format string.  This is a C-style format string.
@param format_string Format string to use for output.
@see RTi.Util.String.StringUtil#formatString
*/
public void setFormatString ( String format_string )
{	if ( format_string == null ) {
		return;
	}
	_format_string = format_string;
	int dot_pos = format_string.indexOf(".");
	if ( dot_pos < 0 ) {
		// Like %f
		_width = 0;
		_precision = 0;
	}
	else {	// Get the width...
		_width = 0;
		StringBuffer b1 = new StringBuffer();
		if ( dot_pos > 0 ) {
			for (	int i = dot_pos - 1;
				(i >= 0) &&
				Character.isDigit(format_string.charAt(i));
				i++ ) {
				b1.append ( format_string.charAt(i) );
			}
			if ( b1.length() > 0 ) {
				b1.reverse();
				_width = StringUtil.atoi(b1.toString() );
			}
		}
		b1 = null;
		// Get the precision...
		_precision = 0;
		StringBuffer b2 = new StringBuffer();
		int length = format_string.length();
		for (	int i = dot_pos + 1;
			(i < length) &&
			Character.isDigit(format_string.charAt(i));
			i++ ) {
			b2.append ( format_string.charAt(i) );
		}
		if ( b2.length() > 0 ) {
			_precision = StringUtil.atoi(b2.toString() );
		}
		b2 = null;
	}
}

/**
Set the number of digits after the decimal point to use for output.
@param precision Number of digits after the decimal point.
*/
public void setPrecision ( int precision )
{	_precision = precision;
	setFormatString();
}

/**
Set the total number of characters to use for output.
@param width Total number of characters for output.
*/
public void setWidth ( int width )
{	_width = width;
	setFormatString();
}

/**
Return string version.
@return A string representation of the format (e.g., "%10.2f").
*/
public String toString ( )
{	return _format_string;
}

} // End of DataFormat
