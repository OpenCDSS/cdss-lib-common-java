// ----------------------------------------------------------------------------
// MeasLocType - measurement location types
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-10-31	Steven A. Malers, RTi	Initial version.  Define for use with
//					DataType, in particular for NWS work.
//					Later need to rectify with the fact that
//					RiversideDB has a table for this object.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

/**
The MeasLocType class currently defines static data for measurement location
types, for use with the DataType class.
*/
public class MeasLocType
{

/**
Indicates that a measurement is made for an area.
*/
public static final String AREA = "A";

/**
Indicates that a measurement is made for a point.
*/
public static final String POINT = "P";

/**
Indicates that a measurement is made for an area or point.
*/
public static final String AREA_OR_POINT = "AP";

} // End MeasLocType
