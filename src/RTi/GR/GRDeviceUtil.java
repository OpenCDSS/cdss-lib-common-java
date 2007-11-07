// ----------------------------------------------------------------------------
// GRDeviceUtil - Utility functions and data members for devices.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-05-02	J. Thomas Sapienza, RTi	Initial version made by pulling stuff
//					out of other classes and putting it 
//					here.
// 2003-05-07	JTS, RTi		Made changes following review by SAM.
// ----------------------------------------------------------------------------

package RTi.GR;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
This class contains various utility functions and data members that are
of use to all GR Devices.
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
@param flag Flag indicating whether device is to be closed GR.CLOSE_HARD or
GR.CLOSE_SOFT.  For example, the former is appropriate if a window is to 
go away, the latter if the window is to remain (because it is ultimately 
controlled by some other code).
*/
public static void closeDevice ( GRDevice dev, int flag )
{

	// Call the driver routine to end the plot...
	dev.plotEnd ( flag);

// REVISIT NECESSARY? (JTS - 2003-05-05)
// SAM:
// Leave revisit; can't remember.
	// Now do the internal bookkeeping for the GR devices and drawing
	// areas...
/*
	for ( int i = 0; i < GRnum_da; i++ ) {
		if ( GRda[i].dev == idev ) {
			// The drawing area is for the device that is being
			// closed so close the drawing area also...
			GRda[i].status = GRSTAT_CLOSED;
		}
	}
*/
	//dev._status = STAT_CLOSED;
}

/**
Get a new GRDevice.  Depending on the flag that is passed in, this may be a
GRCanvasDevice (screen or for printing) or a GRPSDevice (PostScript file).
Additional devices will be added later.  This method helps to initialize
graphical devices.  Once a device is returned, use the methods in the
GRDevice class to manipulate the object.

// REVISIT (JTS - 2003-05-05)
if the type parameter isn't used anymore, why is it even in here?

@param type GRDevice type (see GR.DEVICE*).  This argument is now ignored.
@param props Property list appropriate for the device.  Define a property of
"Type" to be "PostScript" for a PostScript file, "AWT" for a GRCanvasDevice,
and "Swing" for a GRJComponentDevice.  Anything else will return a 
GRJComponentDevice.  If the props value is null, a GRJComponentDevice will
be returned.
@return A new GRDevice appropriate for the desired output product.
*/
public static GRDevice getNewDevice ( int type, PropList props )
throws GRException
{	String propval = null;
	propval = props.getValue ( "Type" );
	if (propval != null) {
		if (propval.equalsIgnoreCase("PostScript")) {
			// PostScript file...
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
Version to maintain compatibility with legacy code.  This version sets the
property list and then calls the version of this method that accepts a
PropList.
// REVISIT X (JTS - 2003-05-05)
If this method is here to maintain compatibility with legacy code, is it
necessary anymore?
SAM:
Yes, we can deprecate it later.
@param grdevice GRDevice to which the GRDrawingArea is associated.
@param name Name of the drawing area.
@param aspect Aspect to use for the X and Y axis of the drawing area.
@see GRAspect
*/
public static GRDrawingArea getNewDrawingArea ( GRDevice grdevice,
						String name, int aspect )
throws GRException
{	PropList props = new PropList ( name );
	props.set ( "Name=" + name );
	if ( aspect == GRAspect.TRUE ) {
		props.set ( "Aspect=True" );
	}
	else if ( aspect == GRAspect.FILL ) {
		props.set ( "Aspect=Fill" );
	}
	else if ( aspect == GRAspect.FILLX ) {
		props.set ( "Aspect=FillX" );
	}
	else if ( aspect == GRAspect.FILLY ) {
		props.set ( "Aspect=FillY" );
	}
	try {	return getNewDrawingArea ( grdevice, props );
	}
	catch ( GRException e ) {
		throw e;
	}
}

/**
Get a new GRDrawingArea.  Depending on the device that is passed in,
this may be a GRCanvasDrawingArea (screen or for printing) or a 
GRPSDrawingArea (PostScript file), or a GRJComponentDrawingArea.  
Additional drawing area types will be added later.  
This method helps to initialize graphical drawing areas.  
Once a drawing area is returned, use the methods in the GRDrawingArea 
class to manipulate the object.
// REVISIT DOCUMENTAITON (JTS - 2003-05-05)
update for the other GR Drawing Areas
@return A new GRDrawingArea appropriate for the desired output product.
@param grdevice GRDevice that is receiving output.
@param props Property list appropriate for the drawing area.
*/
public static GRDrawingArea getNewDrawingArea ( GRDevice grdevice,
						PropList props )
throws GRException
{	String routine = "GR.getNewDrawingArea";
	String message;

	if ( grdevice == null ) {
		message= "Null GRDevice";
		Message.printWarning ( 2, routine, message );
		throw new GRException ( message );
	}

	// SAMX - this is only used with PostScript makenet in CDSS - need to
	// remove this code which does not to seem as useful as it was
	// originally

	//int type = grdevice.getType();
	try {	/* SAMX
	// REVISIT NECESSARY? (JTS - 2003-05-05)
	can this commented-code be removed?
	SAM:
	I think that I only use for the makenet PostScript.  Revisit later
	once that is folded into a GUI in StateDMI.
		if ( type == GRDevice.DEFAULT_VISUAL ) {
			Message.printDebug ( 1, routine,
			"Creating new Java drawing area" );
//			return new GRCanvasDrawingArea ( (GRCanvasDevice)grdevice,
//				props );
		}
		else if	( type == GRDevice.DEFAULT_PRINTER ) {
			// Need to figure this out...
			Message.printDebug ( 1, routine,
			"Creating new Java Printer drawing area" );
//			return new GRCanvasDrawingArea ( (GRCanvasDevice)grdevice,
//			props );
		}
*/
		//else if ( type == GRDevice.POSTSCRIPT ) {
			// PostScript file...
		if (grdevice instanceof GRPSDevice) {
			Message.printDebug ( 1, routine,
			"Creating new PostScript drawing area" );
			GRPSDrawingArea ps = null;
			ps = new GRPSDrawingArea ( (GRPSDevice)grdevice, props);
			if ( ps == null ) {
				message=
				"Unable to create PostScript drawing area";
				Message.printWarning ( 2, routine, message );
				throw new GRException ( message );
			}
			else {	grdevice.addDrawingArea ( ps );
				return ps;
			}
		}
		else if (grdevice instanceof GRCanvasDevice) {
			Message.printDebug (1, routine, 
				"Creating new canvas drawing area");
			GRCanvasDrawingArea c = new GRCanvasDrawingArea(
				(GRCanvasDevice)grdevice, props);
			if (c == null) {
				message ="Unable to create canvas drawing area";
				Message.printWarning(2, routine, message);
				throw new GRException(message);
			}
			else {
				grdevice.addDrawingArea(c);
				return c;
			}
		}
		else if (grdevice instanceof GRJComponentDevice) {
			Message.printDebug(1, routine, 
				"Creating new swing drawing area");
			GRJComponentDrawingArea s = new 
				GRJComponentDrawingArea(
				(GRJComponentDevice)grdevice, props);
			if (s == null) {
				message = "Unable to create swing drawing area";
				Message.printWarning(2, routine, message);
				throw new GRException (message);
			}
			else {
				grdevice.addDrawingArea(s);
				return s;
			}
		}
		else {
			throw new GRException ("Unrecognized device type: " 
				+ grdevice);
		}
		//}
	}
	catch ( GRException e ) {
		throw e;
	}

	//message="Device is of unknown type.  Unable to create drawing area.";
	//Message.printWarning ( 2, routine, message );
	//throw new GRException ( message );
}



}
