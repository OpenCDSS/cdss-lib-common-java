//------------------------------------------------------------------------------
// TSData - class for storing data value and date.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 22 Sep 1997	Matthew J. Rutherford,	Created initial version of class.
//		Riverside Technology,
//		inc.
// 05 Jan 1998	Steven A. Malers, RTi	Update to be more generic for use with
//					all time series classes.  This will
//					allow, for example, a way to graph time
//					series and then inquire about the data
//					flag.  Change _quality_flag to
//					_data_flag.  Make it so the date that
//					is set has its own instance here and
//					return a new instance when a date is
//					requested.  This protects the integrity
//					of the data.
// 13 Apr 1999	SAM, RTi		Add finalize.
// 21 Feb 2001	SAM, RTi		Change so clone() is a deep clone and
//					add a copy constructor.
// 30 Aug 2001	SAM, RTi		Make sure clone() is working as
//					expected.  Clean up Javadoc. Set unused
//					variables to null.  Fix the copy
//					constructor.
// 2001-11-06	SAM, RTi		Change so set methods have void return
//					type.
// 2002-01-31	SAM, RTi		Add support for duration, similar to
//					C++.  The duration is normally only used
//					with irregular time series.
// 2003-06-02	SAM, RTi		Upgrade to use generic classes.
//					* Change TSDate to DateTime.
//					* Change TSUnits to DataUnits.
// 2004-03-08	J. Thomas Sapienza, RTi	Class is now serializable.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.TS;

import java.io.Serializable;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeUtil;

/**
This class provides a simple class for storing a time series data point
consisting of a date, a data value, units and a data flag.  It is used by
IrregularTS to store data returned by getDataPoint() methods.
@see IrregularTS
*/
public class TSData 
implements Cloneable, Serializable {

/**
Data value.
*/
private	double _data;
/**
Data flag (often used for quality, etc.).
*/
private String _data_flag;
/**
Date/time associated with data value.
*/
private DateTime _date;
/**
Duration of the value (seconds)
*/
private int _duration;
/**
Pointer to next TSData in list (an internally maintained linked list).
*/
private transient TSData _next;	
/**
Pointer to previous TSData in list (an internally maintained linked list).
*/
private transient TSData _previous;	
/**
Units of data.
*/
private String _units;

/**
Default constructor.  The date is set to null and the data value is set to zero.
*/
public TSData()
{	super();
	initialize();
}

/**
Construct and set the data values.
@param date date for data value.
@param value data value.
*/
public TSData ( DateTime date, double value )
{   super();
    initialize();
    setDate( date );
    setData( value );
}

/**
Construct and set the data values.
@param date date for data value.
@param value data value.
@param units data units.
@see DataUnits
*/
public TSData ( DateTime date, double value, String units )
{   super();
    initialize();
    setDate( date );
    setData( value );
    setUnits( units );
}

/**
Construct and set the data values.
@param date date for data value.
@param value data value.
@param units data units.
@param flag Data flag.
@see DataUnits
*/
public TSData ( DateTime date, double value, String units, String flag )
{   super();
    initialize();
    setDate( date );
    setData( value );
    setUnits( units );
    setDataFlag( flag );
}

/**
Create a copy of the object.
A deep copy is made except for the next and
previous pointers, which are copied as is.  If a sequence of new data are
being created, the next/previous pointers will need to be reset accordingly.
*/
public TSData ( TSData tsdata )
{	_data_flag = tsdata._data_flag;
	_duration = tsdata._duration;
	_units = tsdata._units;
	_data = tsdata._data;
	_date = new DateTime ( tsdata._date );
	_next = tsdata._next;
	_previous = tsdata._previous;
}

/**
Clone the object.   The Object base class clone() method is called and then the local data are cloned.
A deep copy is made except for the next and
previous pointers, which are copied as is.  If a sequence of new data are
being created, the next/previous pointers will need to be reset accordingly.
*/
public Object clone ()
{	try {
        TSData tsdata = (TSData)super.clone();
		tsdata._date = (DateTime)_date.clone();
		tsdata._next = _next;
		tsdata._previous = _previous;
		tsdata._duration = _duration;
		return tsdata;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Finalize before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_data_flag = null;
	_date = null;
	_next = null;
	_previous = null;
	_units = null;
	super.finalize();
}

/**
Return the data value.
@return The data value.
*/
public double getData()
{	return _data;
}

/**
Return the data flag.
@return The data flag.
*/
public String getDataFlag()
{	return _data_flag;
}

/**
Return the data for the data.
@return The date associated with the value.  A copy is returned.
*/
public DateTime getDate()
{	return new DateTime (_date);
}

/**
Return the duration (seconds) for the data.
@return The duration (seconds) associated with the value.
*/
public int getDuration()
{	return _duration;
}

/**
Return the reference to the next data item.
@return Return the reference to the next data item (used when an
internally-maintained linked list is used).
*/
public TSData getNext()
{	return _next;
}

/**
Return the reference to the previous data item.
@return Return the reference to the previous data item (used when an
internally-maintained linked list is used).
*/
public TSData getPrevious()
{	return _previous;
}

/**
Return the data units.
@return The units for the data.
REVISIT -- Incorrect tag usage: see DataUnits
*/
public String getUnits()
{	return _units;
}

/**
Initialize the data.
*/
private void initialize ( ) 
{	_data_flag = "";
	_date = null;
	_duration = 0;
	_units = "";
	_data = 0.0;
	_next = null;
	_previous = null;
}

/**
Set the data value.
@param d Data value.
*/
public void setData( double d )
{	_data = d;
}

/**
Set the data flag.  If the first character of the flag is "+", then the flag will be appended
with flag (without the +).
@param flag Data flag.
*/
public void setDataFlag( String flag )
{	if( flag != null ){
        if ( flag.startsWith("+") ) {
            // Append to the flag...
            if ( flag.length() > 1 ) {
                _data_flag += flag.substring(1);
            }
        }
        else {
            // Just set the flag...
            _data_flag = flag;
        }
	}
}

/**
Set the date.  A copy of the date is made.
@param d Date corresponding to data.
*/
public void setDate( DateTime d )
{	if( d != null ){
		_date = new DateTime ( d );
	}
}

/**
Set the duration (seconds).
@param duration Duration corresponding to data.
*/
public void setDuration ( int duration )
{	_duration = duration;
}

/**
Set the next reference (used when maintaining and internal linked-list).
@param d Reference to TSData.
*/
public void setNext( TSData d )
{	_next = d;
}

/**
Set the previous reference (used when maintaining and internal linked-list).
@param d Reference to TSData.
*/
public void setPrevious( TSData d )
{	_previous = d;
}

/**
Set the units for the data value.
@param units Data units.
@see DataUnits
*/
public void setUnits( String units )
{	if( units != null ){
		_units	= units;
	}
}

/**
Set the data values.
@param date Date for data value.
@param d Data value.
@param units Data units.
@param flag Data flag.
@see DataUnits
*/
public void setValues( DateTime date, double d, String units, String flag )
{	setDate( date );
	setData( d );
	setUnits( units );
	setDataFlag( flag );
}

/**
Set the data values.
@param date Date for data value.
@param d Data value.
@param units Data units.
@param flag Data flag.
@param duration Duration (seconds).
@see DataUnits
*/
public void setValues ( DateTime date, double d, String units, String flag, int duration )
{	setDate( date );
	_data = d;
	setUnits( units );
	setDataFlag( flag );
	_duration = duration;
}

/**
Return a string representation of the instance.
@return A string representation of a TSData.
*/
public String toString()
{	return "TSData: " +
	" Date: \"" + _date +
	"\" Value: " + _data +
	" Units: \"" + _units +
	"\" Flag: \"" + _data_flag + "\" Duration: " + _duration;
}

/**
Return a string representation of the instance using the format.  The format
can contain any of the format specifiers used in TimeUtil.formatDateTime() or
any of the following specifiers:  %v (data value, formatted based on units),
%U (data units), %q (data flag).  Any format information not recognized as a
format specifier is treated as a literal string.
@return A string representation of a TSData.
@param full_format Format string containing TimeUtil.formatDateTime() format specifiers, %v, %U, or %q.
@param value_format Format to use for data value (e.g., %.4f).  This should
in most cases be determined before calling this method repeatedly, in order to improve performance.
@param date Date for the data value (can be null if no date format specifiers are given).
@param value1 Data value to format (can be ignored if %v is not specified).
@param value2 Second value to format (can be ignored if %v is not specified) -
use when plotting time series on each axis.
@param flag Data quality flag to format (can be ignored if %q is not specified).
@param units Data units (can be ignored if %v and %U are not specified.
*/
public static String toString (	String full_format, String value_format,
    DateTime date, double value1, double value2, String flag, String units )
{	// Format the date first...
	String format = TimeUtil.formatDateTime ( date, full_format );
	// Now format the %v, %U, and %q (copy this code from
	// TimeUtil.formatDateTime() and modify...

	if ( format == null ) {
		return "";
	}

	String value_format2 = value_format;	// Used for %v
	if ( value_format == null ) {
		value_format2 = "%f";
	}

	// Convert format to string...
	int len = format.length();
	StringBuffer formatted_string = new StringBuffer ();
	char c = '\0';
	boolean value1_found = false;
	for ( int i = 0; i < len; i++ ) {
		c = format.charAt(i);
		if ( c == '%' ) {
			// We have a format character...
			++i;
			if ( i >= len ) {
				break;	// this will exit the whole loop
			}
			c = format.charAt(i);
			if ( c == 'v' ) {
				// Data value.
				if ( !value1_found ) {
					formatted_string.append(StringUtil.formatString(value1,value_format2) );
					value1_found = true;
				}
				else {
				    formatted_string.append(StringUtil.formatString(value2,value_format2) );
				}
			}
			else if ( c == 'U' ) {
				// Data units...
				formatted_string.append( units );
			}
			else if ( c == 'q' ) {
				// Data flag...
				formatted_string.append( flag );
			}
			else if ( c == '%' ) {
				// Literal percent...
				formatted_string.append ( '%' );
			}
		}
		else {
		    // Just add the character to the string...
			formatted_string.append ( c );
		}
	}

	return formatted_string.toString();
}

}