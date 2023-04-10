// GRUtil - utility methods and data for the entire GR package

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

public class GRUtil {

/**
Status of device or drawing area is closed.
*/
public static final int STATUS_CLOSED = 0;

/**
Status of device or drawing area is open.
*/
public static final int STATUS_OPEN = 1;

/**
Status of device or drawing area is active (and open).
*/
public static final int STATUS_ACTIVE = 2;

/**
Close a device or drawing area and free its resources.
*/
public final static int CLOSE_HARD = 0;

/**
Close a device or drawing area but allow its resources to be reused.
*/
public final static int CLOSE_SOFT = 1;

/**
Draw to the device.
*/
public static final int MODE_DRAW =	0x1;

/**
Record drawing to a file.
*/
public static final int MODE_RECORD =	0x2;

/**
Print help for drawing command.
*/
public static final int MODE_HELP =	0x4;

/**
Indicator of Device (or Drawing Area) being closed.
*/
public static final int STAT_CLOSED = 0;

/**
Indicator of Device (or Drawing Area) being open.
*/
public static final int STAT_OPEN = 1;

/**
Indicator of Device (or Drawing Area) being active.
*/
public static final int STAT_ACTIVE = 2;

/**
Convert the internal orientation number flag to the string representation.
@param orient Orientation as GRDeviceUtil.ORIENTATION_*.
@return String orientation.
@exception GRException if the orientation cannot be determined.
*/
protected static String getStringOrientation ( int orient )
throws GRException {
	if ( orient == GRDeviceUtil.ORIENTATION_LANDSCAPE ) {
		return "landscape";
	}
	else if ( orient == GRDeviceUtil.ORIENTATION_PORTRAIT ) {
		return "portrait";
	}
	else {
		throw new GRException ( "Orientation " + orient + " cannot be converted to string" );
	}
}

/**
Get size of page as a string.
@param pagesize	Page size as internal integer (see GRDeviceUtil.SIZE_*).
@return page size as string (e.g., "A").
@exception GRException if the page size cannot be determined.
*/
public static String getStringPageSize ( int pagesize )
throws GRException {
	if ( pagesize == GRDeviceUtil.SIZE_A ) {
		return "A";
	}
	else if ( pagesize == GRDeviceUtil.SIZE_B ) {
		return "B";
	}
	else if ( pagesize == GRDeviceUtil.SIZE_C ) {
		return "C";
	}
	else if ( pagesize == GRDeviceUtil.SIZE_D ) {
		return "D";
	}
	else {
		throw new GRException ( "Cannot convert page size " + pagesize + " to string" );
	}
}

}