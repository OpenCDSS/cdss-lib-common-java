// GRDeviceUtil - Utility functions and data members for devices.

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

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class contains various utility functions and data members that are of use to all GR Devices.
*/
public class GRDeviceUtil {

/**
Output has portrait orientation.
*/
public final static int ORIENTATION_PORTRAIT = 1;

/**
Output has landscape orientation.
*/
public final static int ORIENTATION_LANDSCAPE = 2;

/**
A-size (8.5 x 11 in) paper.
*/
public final static int SIZE_A = 1;

/**
B-size (11 x 17 in) paper.
May no longer be needed -- maybe only for batch printing.
*/
public final static int SIZE_B = 2;

/**
C-size (17 x 22) paper.
*/
public final static int SIZE_C = 3;

/**
D-size (22 x 34 in) paper.
*/
public final static int SIZE_D = 4;

/**
E-size (unknownsize x unknownsize in) paper.
*/
public final static int SIZE_E = 5;

/**
Close a device.
@param dev GR device.
@param flag Flag indicating whether device is to be closed GR.CLOSE_HARD or GR.CLOSE_SOFT.
For example, the former is appropriate if a window is to go away,
the latter if the window is to remain (because it is ultimately controlled by some other code).
*/
public static void closeDevice ( GRDevice dev, int flag ) {

	// Call the driver routine to end the plot.
	dev.plotEnd ( flag);

// REVISIT NECESSARY? (JTS - 2003-05-05)
// SAM:
// Leave revisit; can't remember.
	// Now do the internal bookkeeping for the GR devices and drawing areas.
/*
	for ( int i = 0; i < GRnum_da; i++ ) {
		if ( GRda[i].dev == idev ) {
			// The drawing area is for the device that is being closed so close the drawing area also.
			GRda[i].status = GRSTAT_CLOSED;
		}
	}
*/
	//dev._status = STAT_CLOSED;
}

/**
Get a new GRDevice.  Depending on the flag that is passed in, this may be a GRCanvasDevice
(screen or for printing) or a GRPSDevice (PostScript file).
Additional devices will be added later.  This method helps to initialize graphical devices.
Once a device is returned, use the methods in the GRDevice class to manipulate the object.

// REVISIT (JTS - 2003-05-05) if the type parameter isn't used anymore, why is it even in here?

@param type GRDevice type (see GR.DEVICE*).  This argument is now ignored.
@param props Property list appropriate for the device.
Define a property of "Type" to be "PostScript" for a PostScript file, "AWT" for a GRCanvasDevice,
and "Swing" for a GRJComponentDevice.  Anything else will return a GRJComponentDevice.
If the props value is null, a GRJComponentDevice will be returned.
@return A new GRDevice appropriate for the desired output product.
*/
public static GRDevice getNewDevice ( int type, PropList props )
throws GRException {
	String propval = null;
	propval = props.getValue ( "Type" );
	if (propval != null) {
		if (propval.equalsIgnoreCase("PostScript")) {
			// PostScript file.
			return new GRPSDevice ( props );
		}
		else if (propval.equalsIgnoreCase("AWT")) {
			return new GRCanvasDevice(props);
		}
		else if (propval.equalsIgnoreCase("Swing")) {
			return new GRJComponentDevice(props);
		}
		else {
			return new GRJComponentDevice(props);
		}
	}
	else {
		return new GRJComponentDevice(props);
	}
}

/**
Version to maintain compatibility with legacy code.
This version sets the property list and then calls the version of this method that accepts a PropList.
// REVISIT X (JTS - 2003-05-05)
If this method is here to maintain compatibility with legacy code, is it necessary anymore?
SAM:
Yes, we can deprecate it later.
@param grdevice GRDevice to which the GRDrawingArea is associated.
@param name Name of the drawing area.
@param aspect Aspect to use for the X and Y axis of the drawing area.
@see GRAspect
*/
public static GRDrawingArea getNewDrawingArea ( GRDevice grdevice, String name, GRAspectType aspect )
throws GRException {
	PropList props = new PropList ( name );
	props.set ( "Name=" + name );
	if ( aspect == GRAspectType.TRUE ) {
		props.set ( "Aspect=True" );
	}
	else if ( aspect == GRAspectType.FILL ) {
		props.set ( "Aspect=Fill" );
	}
	else if ( aspect == GRAspectType.FILLX ) {
		props.set ( "Aspect=FillX" );
	}
	else if ( aspect == GRAspectType.FILLY ) {
		props.set ( "Aspect=FillY" );
	}
	try {
		return getNewDrawingArea ( grdevice, props );
	}
	catch ( GRException e ) {
		throw e;
	}
}

/**
Get a new GRDrawingArea.
Depending on the device that is passed in, this may be a GRCanvasDrawingArea (screen or for printing)
or a GRPSDrawingArea (PostScript file), or a GRJComponentDrawingArea.
Additional drawing area types will be added later.
This method helps to initialize graphical drawing areas.
Once a drawing area is returned, use the methods in the GRDrawingArea class to manipulate the object.
// REVISIT DOCUMENTAITON (JTS - 2003-05-05)
update for the other GR Drawing Areas
@return A new GRDrawingArea appropriate for the desired output product.
@param grdevice GRDevice that is receiving output.
@param props Property list appropriate for the drawing area.
*/
public static GRDrawingArea getNewDrawingArea ( GRDevice grdevice, PropList props )
throws GRException {
	String routine = GRDeviceUtil.class.getSimpleName() + ".getNewDrawingArea";
	String message;

	if ( grdevice == null ) {
		message= "Null GRDevice";
		Message.printWarning ( 2, routine, message );
		throw new GRException ( message );
	}

	try {
		if (grdevice instanceof GRPSDevice) {
			Message.printDebug ( 1, routine, "Creating new PostScript drawing area" );
			GRPSDrawingArea ps = null;
			ps = new GRPSDrawingArea ( (GRPSDevice)grdevice, props);
			grdevice.addDrawingArea ( ps );
			return ps;
		}
		else if (grdevice instanceof GRCanvasDevice) {
			Message.printDebug (1, routine, "Creating new canvas drawing area");
			GRCanvasDrawingArea c = new GRCanvasDrawingArea( (GRCanvasDevice)grdevice, props);
			grdevice.addDrawingArea(c);
			return c;
		}
		else if (grdevice instanceof GRJComponentDevice) {
			Message.printDebug(1, routine, "Creating new swing drawing area");
			GRJComponentDrawingArea s = new GRJComponentDrawingArea( (GRJComponentDevice)grdevice, props);
			grdevice.addDrawingArea(s);
			return s;
		}
		else {
			throw new GRException ("Unrecognized device type: " + grdevice);
		}
	}
	catch ( GRException e ) {
		throw e;
	}
}

}