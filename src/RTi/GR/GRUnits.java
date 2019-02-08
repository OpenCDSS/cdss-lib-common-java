// GRUnits - class that defines units for devices and drawing areas

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

package RTi.GR;

import RTi.Util.Message.Message;

// TODO SAM 2010-12-25 Need to evaluate converting to enumeration, at least the Data and Device values.

/**
This class defines units for devices and drawing areas.
@see GRDevice
@see GRDrawingArea
*/
public class GRUnits
{

/**
Device units
*/
public static final int DEVICE = 0;
private static final int UNIT_MIN = DEVICE;
/**
Data units
Device default.
*/
public static final int DATA = 1;
/**
Centimeters.
*/
public static final int CM = 1;
/**
Foot.
*/
public static final int FOOT = 2;
/**
HPGL units.
*/
public static final int HPGL = 3;
/**
Inch.
*/
public static final int INCH = 4;
/**
Kilometers.
*/
public static final int KM = 5;
/**
Meters.
*/
public static final int M = 6;
/**
Millimeters.
*/
public static final int MM = 7;
/**
Pixels.
*/
public static final int PIXEL = 8;
/**
Points.
*/
public static final int POINT = 9;
/**
Yards.
*/
public static final int YARD = 10;
/**
The maximum unit type.
*/
private static final int UNIT_MAX = YARD;

/**
Array used for doing unit conversion.
*/
private static GRUnitsData[] _GRUnits_conversion_data = null;

/**
Converts from one unit type to another.
@param x the value to convert.
@param from the units from which to convert
@param to the units to which to convert
@return the value in units of the converted value.
*/
public static double convert ( double x, int from, int to )
{
	if ( from == to ) {
		// No conversion necessary...
		return x;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, "GRUnits.convert",
		"Converting " + x + " from units " + from + " to units " + to );
	}

	// Base unit is millimeter...

	if ( _GRUnits_conversion_data == null ) {
		// Assign the data...
		_GRUnits_conversion_data = new GRUnitsData[UNIT_MAX + 1];
		_GRUnits_conversion_data[0] = new GRUnitsData (DEVICE,0.0,"device default");
		_GRUnits_conversion_data[1] = new GRUnitsData (CM,10.0,"cm");
		_GRUnits_conversion_data[2] = new GRUnitsData (FOOT,304.8,"foot");
		_GRUnits_conversion_data[3] = new GRUnitsData (HPGL,.02488,"HPGL 7475A");
		_GRUnits_conversion_data[4] = new GRUnitsData (INCH,25.4,"in");
		_GRUnits_conversion_data[5] = new GRUnitsData (KM,1000000.0,"km");
		_GRUnits_conversion_data[6] = new GRUnitsData (M,1000.0,"m");
		_GRUnits_conversion_data[7] = new GRUnitsData (MM,1.0,"mm");
		_GRUnits_conversion_data[8] = new GRUnitsData (PIXEL,.35278,"pixel");
		_GRUnits_conversion_data[9] = new GRUnitsData (POINT,.35278,"point");
		_GRUnits_conversion_data[10] = new GRUnitsData (YARD,914.4,"yard");
	}

	String routine = "GRUnits.convert";

	if ( (from < UNIT_MIN) || (from > UNIT_MAX) ) {
		Message.printWarning ( 3, routine,
		"\"from\" unit " + from + " is outside acceptable range (" +
		UNIT_MIN + "=" +_GRUnits_conversion_data[UNIT_MIN].abbreviation + ", " +
		UNIT_MAX + "=" +_GRUnits_conversion_data[UNIT_MAX].abbreviation+ ")" );
		return x;
	}
	if ( (to < UNIT_MIN) || (to > UNIT_MAX) ) {
		Message.printWarning ( 3, routine,
		"\"to\" unit " + to + " is outside acceptable range (" +
		UNIT_MIN + "=" +_GRUnits_conversion_data[UNIT_MIN].abbreviation+ ", " +
		UNIT_MAX + "=" +_GRUnits_conversion_data[UNIT_MAX].abbreviation+ ")" );
		return x;
	}
	return	( x*_GRUnits_conversion_data[from].conversionFactor /
			_GRUnits_conversion_data[to].conversionFactor);
}

}
