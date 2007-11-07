// ----------------------------------------------------------------------------
// MeasTimeScale - measurement time scales
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-10-31	Steven A. Malers, RTi	Initial version.  Define for use with
//					DataType, in particular for NWS work.
//					Later need to rectify with the fact that
//					RiversideDB has a table for this object.
// 2005-02-16	SAM, RTi		Add getTimeScaleChoices() to be used
//					with displays.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import java.util.Vector;

/**
The MeasTimeScale class currently defines static data for measurement time
scales, for use with the DataType class.
*/
public class MeasTimeScale
{

/**
Indicates that a measurement is an accumulation over time.
*/
public static final String ACCM = "ACCM";

/**
Indicates that a measurement is an average over time.
*/
public static final String MEAN = "MEAN";

/**
Indicates that a measurement is an instantaneous reading.
*/
public static final String INST = "INST";

/**
Return a Vector of String containing available time scale choices.  This is
useful for presenting in displays.
@return a Vector of String containing available time scale choices.
@param include_note If true, the returned string will be of the form
"ACCM - Accumulated".  If false, the returned string will be of the form
"ACCM".
*/
public static Vector getTimeScaleChoices ( boolean include_note )
{	Vector v = new Vector ( 3 );
	if ( include_note ) {
		v.addElement ( ACCM + " - Accumulated" );
		v.addElement ( INST + " - Instantaneous" );
		v.addElement ( MEAN + " - Mean" );
	}
	else {	v.addElement ( "ACCM" );
		v.addElement ( "INST" );
		v.addElement ( "MEAN" );
	}
	return v;
}

} // End MeasTimeScale
