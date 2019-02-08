// MeasLocType - measurement location types

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

// TODO SAM 2007-12-11 Evaluate using an Enum in Java 1.5.
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
