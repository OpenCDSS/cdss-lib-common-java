// ----------------------------------------------------------------------------
// GRCanvasDevice - GR device corresponding to an AWT canvas
// ----------------------------------------------------------------------------
// History:
//
// 2001-01-13	Steven A. Malers, RTi	Add getImage().
// 2001-07-31	SAM, RTi		Add getGraphics(), setGraphics().
// 2002-02-07	SAM, RTi		Major rework.  The GRDevice class is now
//					an interface and its non-interface code
//					is folded into this class.  This class
//					used to be GRCanvasDevice but has been
//					renamed to be compatible with the
//					GRJComponentDevice.  Add _rubber_banding
//					to this class.  Add isPrinting() method.
// 2005-04-26	J. Thomas Sapienza, RTi	Added all member variables to finalize()
// 2005-04-29	JTS, RTi		Added anti alias methods (they do 
//					nothing but are now required by 
//					GRDevice).
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
GR Device that corresponds to an AWT canvas.  This class is the base class for
on-screen-related drawing, with secondary printing off-screen or image
file creation.
*/
@SuppressWarnings("serial")
public class GRCanvasDevice 
extends Canvas 
implements GRDevice
{

/**
Indicates whether double buffering is used.
*/
protected boolean _double_buffering = true;
/**
Indicates if the device is being used for printing.  For example, the
GRCanvasDevice is used for screen and printed output but for printed output
the Y-Axis does not need to be shifted.
*/
protected boolean	_printing;
/**
Indicates that the Y axis must be reversed for the GR zero at the bottom.
*/
protected boolean	_reverse_y;
/**
Indicates whether rubber-banding from a select is in effect, in which case the
drawing code should implement some type of XOR logic.
*/
protected boolean _rubber_banding = false;

/**
Minimum X coordinate, absolute (relative to screen)
*/
protected double	_dev0x1;
/**
Maximum X coordinate, absolute (relative to screen)
*/
protected double	_dev0x2;
/**
Minimum Y coordinate, absolute (relative to screen)
*/
protected double	_dev0y1;
/**
Maximum Y coordinate, absolute (relative to screen)
*/
protected double	_dev0y2;

/**
Minimum X coordinate, relative.
*/
protected double	_devx1;
/**
Maximum X coordinate, relative.
*/
protected double	_devx2;
/**
Minimum Y coordinate, relative.
*/
protected double	_devy1;
/**
Maximum Y coordinate, relative.
*/
protected double	_devy2;

/**
GRLimits containing the relative points of the display device.
*/
protected GRLimits	_limits;

/**
The following should be set by derived classes in the paint() method.
The graphics will be used in drawing throughout the paint() method in 
derived classes.
*/
protected Graphics _graphics = null;

/**
Image used in double-buffering in paint() method in derived classes.
*/
protected Image _image = null;

/**
Display mode (allows recording).
*/
protected int		_mode;
/**
Page orientation -- shouldn't be necessary for any class other than 
GRPSDevice.
*/
protected int		_orientation;
/**
Page count -- shouldn't be necessary for any class other than 
GRPSDevice.
*/
protected int		_page;
/**
Size that is used by calling drawing routines.  Used in Postscript/page 
systems where drawing can be to one page size with a single "scale"
command.  
*/
protected int		_sizedrawn;
/**
Size of output after scaling.  Used in Postscript/page 
systems where drawing can be to one page size with a single "scale"
command.  
*/
protected int		_sizeout;
/**
Indicates the status of the drawing area.  See GRUtil.STATUS_*.  Might be
an equivalent of a C++ option.
*/
protected int		_status;
/**
Graphics driver type.  Offered because different graphics code might make
different decisions, e.g., Postscript draws thick lines, Canvas does not.
*/
protected int		_type;
/**
Device units.
*/
protected int		_units;

/**
Name of this device (assigned by creating code).  It will be used as a window
name if necessary.
*/
protected String	_name;
/**
Note for this device.  Used for simple on-line help for the GUI.
*/
protected String	_note;

/**
List of GRDrawingArea objects for this device.
*/
protected List<GRDrawingArea> _drawing_area_list;

/**
Construct using name.
@param name the name of the device.
*/
public GRCanvasDevice ( String name )
{	super();
	PropList props = new PropList ( "GRCanvasDevice.default" );
	props.set ( "Name", name );
	initialize ( props );
}

/**
Construct using name and size.  Currently, the size is ignored (controlled by
layout managers) but in the future may use to cause a setSize() to be done
at creation.
@param name the name of the device.
@param size the grlimits specifying the size of the device.
*/
public GRCanvasDevice ( String name, GRLimits size )
{ 	super();
	PropList props = new PropList ( "GRCanvasDevice.default" );
	props.set ( "Name", name );
	// For now do not support size...
	initialize ( props );
}

/**
Construct using a property list.
@param props a PropList specifying settings for the device.
*/
public GRCanvasDevice ( PropList props )
{	super();
	if ( Message.isDebugOn ) {
		String routine = "GRCanvasDevice(PropList)";
		Message.printDebug ( 1, routine, "Contructing using PropList");
	}
	initialize ( props );
}

/**
Add a drawing area to the device.  The device will then manage the drawing
areas as much as possible.
@param grda GRDrawingArea to add.
*/
public void addDrawingArea ( GRDrawingArea grda )
{	String routine = "GRDevice.addDrawingArea";

	if ( grda == null ) {
		Message.printWarning ( 2, routine, "NULL drawing area" );
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine,
		"Adding drawing area \"" + grda.getName() + "\" to device \"" +
		_name + "\"" );
	}
	_drawing_area_list.add ( (GRCanvasDrawingArea)grda );
}

/**
Clear the device and fill with white.  Should be defined in derived class.
*/
public void clear () {}

/**
Close the device (used with PS files).  Should be defined in derived class.
*/
public void close () {}

/**
Fill the device with the current color.  Should be defined in derived class.
*/
public void fill () {}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_graphics = null;
	_name = null;
	_note = null;
	_drawing_area_list = null;
	_image = null;
	_limits = null;
	super.finalize();
}

/**
Flush the device (used by PS devices and X-Windoes).  Should be defined in 
derived class.
*/
public void flush () {}

/**
Return the internal Image used for double-buffering or null if image is null
or no double-buffering.  This image is used by the paint() method in derived
classes.
@return Image for the device.
*/
public Image getImage ()
{	return _image;
}

/**
Return the current limits of the device.
@param recompute If true, the limits are retrieved from the canvas.  Otherwise
the previous limits are returned (use the former when creating new drawing
areas, the latter when operating from within a drawing area, assuming that
resizing is being handled somewhere).
@return the current limits of the device.
*/
public GRLimits getLimits ( boolean recompute )
{	if ( recompute ) {
		GRLimits limits = new GRLimits ();

		// Get the size of the canvas...

		Rectangle bounds = getBounds ();

		// Now transfer into GR, where the origin is at the lower
		// left...

		limits.setLeftX ( 0.0 );
		limits.setBottomY ( 0.0 );
		limits.setRightX ( bounds.width );
		limits.setTopY ( bounds.height );
		// This will set _limits...
		setLimits ( limits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "GRCanvasDevice.getLimits",
			"Device limits are: 0.0,0.0 " +
			bounds.width + "," + bounds.height );
		}
	}
	return _limits;
}

/**
Returns the current limits of the device.    This checks the size of the canvas.
@return the current limits of the device.
*/
public GRLimits getLimits ()
{	return _limits;
}

/**
Return the Graphics instance used for drawing.  The graphics instance is 
set when the paint() method for this instance is called in derived classes.
@return the Graphics instance used for drawing.
*/
public Graphics getPaintGraphics()
{	return _graphics;
}

/**
Indicate whether the device Y axis starts at the upper left.
@return true if the device Y axies starts at the upper left.
*/
public boolean getReverseY()
{	return _reverse_y;
}

/**
Returns the device units (GRUnits.*)
@return The device units (GRUnits.*).
*/
public int getUnits ( )
{	return _units;
}

/**
Initializes member variables.
*/
private void initialize ( PropList props )
{	// Set the general information...

	_drawing_area_list = new Vector<GRDrawingArea>();
	_mode = GRUtil.MODE_DRAW;
	_name = "";
	_note = "";
	_orientation = GRDeviceUtil.ORIENTATION_PORTRAIT;
	_page = 0;
	_printing = false;
	_reverse_y = false;
	_sizedrawn = -1;
	_sizeout = -1;
	_status = GRUtil.STATUS_OPEN;
	_type = 0;
	_units = GRUnits.MM;	// Default but needs to be reset.

	/// Set the values that were passed in...

	if ( props == null ) {
		return;
	}

	String prop_value;
	prop_value = props.getValue("Name");
	if ( prop_value != null ) {
		_name = prop_value;
	}
	prop_value = props.getValue("PageSizeDrawn");
	if ( prop_value != null ) {
		if ( prop_value.charAt(0) == 'A' ) {
			_sizedrawn = GRDeviceUtil.SIZE_A;
		}
		else if ( prop_value.charAt(0) == 'B' ) {
			_sizedrawn = GRDeviceUtil.SIZE_B;
		}
		else if ( prop_value.charAt(0) == 'C' ) {
			_sizedrawn = GRDeviceUtil.SIZE_C;
		}
		else if ( prop_value.charAt(0) == 'D' ) {
			_sizedrawn = GRDeviceUtil.SIZE_D;
		}
		else if ( prop_value.charAt(0) == 'E' ) {
			_sizedrawn = GRDeviceUtil.SIZE_E;
		}
	}
	prop_value = props.getValue("PageSizeOutput");
	if ( prop_value != null ) {
		if ( prop_value.charAt(0) == 'A' ) {
			_sizeout = GRDeviceUtil.SIZE_A;
		}
		else if ( prop_value.charAt(0) == 'B' ) {
			_sizeout = GRDeviceUtil.SIZE_B;
		}
		else if ( prop_value.charAt(0) == 'C' ) {
			_sizeout = GRDeviceUtil.SIZE_C;
		}
		else if ( prop_value.charAt(0) == 'D' ) {
			_sizeout = GRDeviceUtil.SIZE_D;
		}
		else if ( prop_value.charAt(0) == 'E' ) {
			_sizeout = GRDeviceUtil.SIZE_E;
		}
	}
	if ( (_sizeout < 0) && (_sizedrawn < 0) ) {
		// Neither specified...
		_sizedrawn = GRDeviceUtil.SIZE_A;
		_sizeout = GRDeviceUtil.SIZE_A;
	}
	else if ( _sizedrawn < 0 ) {
		_sizedrawn = _sizeout;
	}
	else if ( _sizeout < 0 ) {
		_sizeout = _sizedrawn;
	}
	prop_value = props.getValue("Orientation");
	if ( prop_value != null ) {
		if (	prop_value.charAt(0) == 'p' ||
			prop_value.charAt(0) == 'P' ) {
			_orientation = GRDeviceUtil.ORIENTATION_PORTRAIT;
		}
		else {	_orientation = GRDeviceUtil.ORIENTATION_LANDSCAPE;
		}
	}
	prop_value = props.getValue("Note");
	if ( prop_value != null ) {
		_note = prop_value;
	}

	// Fill in later...
	GRLimits limits = null;
	if ( limits == null ) {
		// Use default sizes...
		_dev0x1 = 0.0;
		_dev0x2 = 1.0;
		_dev0y1 = 0.0;
		_dev0y2 = 1.0;

		_devx1 = 0.0;
		_devx2 = 1.0;
		_devy1 = 0.0;
		_devy2 = 1.0;
		_limits = new GRLimits ( _devx1, _devy1, _devx2, _devy2 );
	}
	else {	// Set the limits...
		setLimits ( limits );
	}
	// The size of the device will have been set in the constructor, but
	// the base class only knows how to store the data.  Force a resize
	// here.
	//resize ( _devx1, _devy1, _devx2, _devy2 );

	// Set to null.  Wait for the derived class to set the graphics for
	// use throughout the drawing event...

	// Set information used by the base class and other code...

	_graphics = null;
	_reverse_y = true;	// Java uses y going down.  This is handled
				// properly in GRCanvasDrawingArea.scaleYData().
	_units = GRUnits.PIXEL;
	// Set in the super because there is some redundant data there...
	setLimits ( getLimits ( true ) );
}

/**
Not implemented.
*/
public boolean isAntiAliased() {
	return false;
}

/**
Indicate whether the device is in the process of printing.  The paint()
method in derived classes should call setPrinting() as appropriate to
indicate when prints starts and stops.
@return true if the device is in the process of printing, false if not.
*/
public boolean isPrinting()
{	return _printing;
}

/**
Indicates the end of a page of output.  Used in PS and should be defined in
derived classes.
*/
public void pageEnd () {}

/**
This method is called when the canvas is to be drawn.  It is expected that
classes extended from this base class will implement a paint() method that
either itself sets the graphics or calls super.paint() to call this method to
set the graphics.  Using a GRCanvasDevice directly will result in this method
being called for resize, etc., and the graphics in effect at the time is set as
the current graphics.   The Graphics can then be used by subsequent calls for
drawing.  The base class paint() is not called from this method.

REVISIT (SAM - 2003-05-07)
This is where if we wanted to give base class more functionality for simple 
drawing we would implement somthing like:
addPainter(painter)
then 
for (loop through painters)
{
	painter.paint(graphics);
}
*/
public void paint ( Graphics graphics )
{	_graphics = graphics;
}

/**
End of a plot of output, used for PS.  This should be implemented 
in derived classes.
*/
public void plotEnd ( int flag )
{
}

/**
Resize the device to the given size.  This should be implemented in 
derived classes.
*/
public void resize ( GRLimits limits )
{
	if ( limits == null ) {
		return;
	}
	resize2 ( (int)limits.getWidth(), (int)limits.getHeight() );
}

/**
Resize the device to the given size.
@param x1 New lower-left X-coordinte of device (usually zero).
@param y1 New lower-left Y-coordinte of device (usually zero).
@param x2 New top-right X-coordinte of device.
@param y2 New top-right Y-coordinte of device.
*/
public void resize ( double x1, double y1, double x2, double y2 )
{	resize2 ( (int)(x2 - x1), (int)(y2 - y1) );
}

/**
Resize the device to the given size.  Rename the routine resize2 because there
is a deprecated resize method in canvas.
@param width New width of the device (canvas).
@param height New height of the device (canvas).
*/
public void resize2 ( int width, int height )
{	setSize ( width, height );
}

/**
Save as an image file.  Currently the file is always a JPEG.  In the future,
the file name will be examined for the extension.
REVISIT (JTS - 2003-05-05)
Are there any plans to examine the file name for extension?
SAM:
Sure as we get into it I'd really like to try PNG for Vector-oriented graphics
so time series plots are not fuzzy.
@param filename File name to write.  An appropriate extension will be added.
@throws IOException if the image used for double-buffering is null.
*/
public void saveAsFile ( String filename )
throws IOException
{
	saveAsFile ( filename, (PropList)null );
}

/**
Save as an image file.  Currently the file is always a JPEG.  In the future,
the file name will be examined for the extension.  For Java 1.1.8 or earlier,
TrueColor screen resolution will save as gray-scale.  There is no work-around
unless an upgrade to Java 1.2.x is made.
@param filename File name to write.  An appropriate extension will be added.
@param props Properties for the image.  Currently the only accepted property
is Quality, which can be 0 (low quality, high compression) to 100 (high
quality, no compression).  It might be useful at some point to enable an
Interactive=true option to allow a pop-up dialog to specify JPEG information.
REVISIT (JTS - 2003-05-05)
Any plans for the above?
SAM:
Sure as we get into it.
@throws IOException if the image used for double-buffering is null.
*/
public void saveAsFile ( String filename, PropList props )
throws IOException
{	String routine = "GRCanvasDevice.saveAsFile";
	if ( _image == null ) {
		throw new IOException ( "No internal image to save" );
	}
	// Else, create an image from the canvas???

	// Defaults...

	int image_quality = 90;		// Go for quality versus compression.

	// Make sure there is a property list...

	PropList proplist = props;
	if ( proplist == null ) {
		proplist = new PropList ( "ImageProps" );
	}
	String prop_value = proplist.getValue ( "Quality" );
	if ( prop_value != null ) {
		image_quality = StringUtil.atoi(prop_value);
	}

	String newfilename = null;
	if (	// Check for a standard extension...
		filename.endsWith(".jpg") || filename.endsWith(".JPG") ||
		filename.endsWith(".jpeg") || filename.endsWith(".JPEG") ) {
		newfilename = filename;
	}
	else {	// Add a standard extension...
		newfilename = filename + ".jpg";
	}
	try {	
		FileOutputStream os = new FileOutputStream ( newfilename );
		JpegEncoder jpg = new JpegEncoder ( _image, image_quality, os );
		jpg.Compress();
		os.flush();
		os.close();
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error saving image file \"" + newfilename + "\"" );
		Message.printWarning ( 2, routine, e );
		throw new IOException ( "Writing JPEG file \"" +
		newfilename + "\" failed." );
	}
}

/**
Not implemented.
*/
public void setAntiAlias(boolean antiAlias) {}

/**
Set the device limits (size) using a GRLimits.  This only sets the limits.  The
device must be resized in the derived class.
@param limits GRLimits indicating the size of the device.
*/
public void setLimits ( GRLimits limits )
{
	_devx1 = limits.getLeftX();
	_devy1 = limits.getBottomY();
	_devx2 = limits.getRightX();
	_devy2 = limits.getTopY();
	_limits = new GRLimits ( limits );
	if ( Message.isDebugOn ) {
		String routine = "GRDevice.setLimits";
		Message.printDebug ( 1, routine,
		"Setting \"" + _name + "\" device limits to " + limits );
	}
}

/**
Set the Graphics used by the device for drawing.  This Graphics should be
reset each time that paint() is called in code that implements a 
GRDevice because Graphics
resources are typically created and destroyed dynamically by the application.
@param graphics instance to use for drawing to the device.
REVISIT (SAM - 2003-05-07)
Need to see how printer graphics is handled.  I don't think it hurts to
temporarily save it here during printing.
*/
public void setPaintGraphics ( Graphics graphics )
{	_graphics = graphics;
}

/**
Set the printing flag.  Set to true when the device is being used for printing,
false when drawing to the screen.
@param printing printing flag.
*/
public void setPrinting ( boolean printing )
{
	_printing = printing;
}

} // End GRCanvasDevice class
