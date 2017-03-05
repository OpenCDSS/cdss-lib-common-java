// ----------------------------------------------------------------------------
// GRJComponentDevice - GR device corresponding to a JComponent
// ----------------------------------------------------------------------------
// History:
//
// 2002-01-07	Steven A. Malers, RTi	Copy and combine GRDevice and
//					GRJavaDevice.  Add _rubber_banding.
//					Add isPrinting().
// 2003-05-01	J. Thomas Sapienza, RTi	Made changes to accomodate the massive
//					restructuring of GR.java.
// 2003-05-02	JTS, RTi		Incorporated changes to allow double-
//					buffering
// 2003-05-06	JTS, RTi		Split the setupDoubleBuffer() method
//					into two methods.
// 2004-03-19	JTS, RTi		Started using Graphics2D instead of
//					Graphics.
// 2004-08-06	JTS, RTi		Added support for writing PNG files.
// 2005-04-26	JTS, RTi		Added all data members to finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
TODO (JTS - 2006-05-23) Document example of use (e.g., from ERDiagram_Device)
*/
public class GRJComponentDevice 
extends JComponent 
implements GRDevice
{

//TODO sam 2017-03-03 need to make all of these private and add appropriate methods to encapsulate
/**
BufferedImage used in double-buffering.
*/
protected BufferedImage _buffer = null;

/**
If true, drawing will be performed to the double-buffer.
*/
protected boolean _doubleBuffered = false;

/**
For backwards compatibility with older code.  Probably should be removed soon.
*/
protected boolean _double_buffering = _doubleBuffered;

/**
Whether the device is drawing anti-aliased or not.
Anti-aliased with smooth curves by filling in transition color pixels.
*/
private boolean __isAntiAliased = false;

/**
Indicates if the device is being used for printing.  For example, the
GRCanvasDevice is used for screen and printed output but for printed output
the Y-Axis does not need to be shifted.
*/
protected boolean _printing;

/**
Indicates that the Y axis must be reversed for the GR zero at the bottom.
*/
protected boolean _reverse_y;

/**
Indicates whether rubber-banding from a select is in effect, in which case the
drawing code should implement some type of XOR logic.
*/
protected boolean _rubber_banding = false;

/**
Minimum X coordinate, absolute (relative to screen)
*/
protected double _dev0x1;
/**
Maximum X coordinate, absolute (relative to screen)
*/
protected double _dev0x2;
/**
Minimum Y coordinate, absolute (relative to screen)
*/
protected double _dev0y1;
/**
Maximum Y coordinate, absolute (relative to screen)
*/
protected double _dev0y2;

/**
Minimum X coordinate, relative.
*/
protected double _devx1;
/**
Maximum X coordinate, relative.
*/
protected double _devx2;
/**
Minimum Y coordinate, relative.
*/
protected double _devy1;
/**
Maximum Y coordinate, relative.
*/
protected double _devy2;

/**
GRLimits containing the relative points of the display device.
*/
protected GRLimits _limits;

/**
The following should be set by derived classes in the paint() method.
The graphics will be used in drawing throughout the paint() call.
*/
protected Graphics2D _graphics = null;

/**
Image used in double-buffering in paint() method in derived classes.
*/
protected Image _image = null;

/**
Display mode (allows recording).
*/
protected int _mode;
/**
Page orientation -- shouldn't be necessary for any class other than GRPSDevice.
*/
protected int _orientation;
/**
Page count -- shouldn't be necessary for any class other than GRPSDevice.
*/
protected int _page;
/**
Size that is used by calling drawing routines.  Used in Postscript/page 
systems where drawing can be to one page size with a single "scale" command.  
*/
protected int _sizedrawn;
/**
Size of output after scaling.  Used in Postscript/page 
systems where drawing can be to one page size with a single "scale" command.  
*/
protected int _sizeout;
/**
Indicates the status of the drawing area.  See GRUtil.STATUS_*.  Might be an equivalent of a C++ option.
*/
protected int _status;
/**
Graphics driver type.  Offered because different graphics code might make
different decisions, e.g., Postscript draws thick lines, Canvas does not.
*/
protected int _type;
/**
Device units.
*/
protected int _units;

/**
Name of this device (assigned by creating code).  It will be used as a window name if necessary.
*/
protected String _name;
/**
Note for this device.  Used for simple on-line help for the GUI.
*/
protected String _note;

/**
List of GRDrawingArea objects for this device, guaranteed to be non-null.
*/
private List<GRDrawingArea> drawingAreaList = null;

/**
Construct using name.
@param name the name of the device.
*/
public GRJComponentDevice ( String name )
{	PropList props = new PropList ( "GRJComponentDevice.default" );
	props.set ( "Name", name );
	initialize ( props );
}

/**
Construct using name and size.  Currently, the size is ignored (controlled by layout managers).
*/
public GRJComponentDevice ( String name, GRLimits size )
{	PropList props = new PropList ( "GRJComponentDevice.default" );
	props.set ( "Name", name );
	// For now do not support size...
	initialize ( props );
}

/**
Construct using a property list.
@param props PropList containing settings for this device.
*/
public GRJComponentDevice ( PropList props )
{	if ( Message.isDebugOn ) {
		String routine = "GRDevice(PropList)";
		Message.printDebug ( 1, routine, "Contructing using PropList");
	}
	initialize ( props );
}

/**
Add a drawing area to the device.  The device will then manage the drawing areas as much as possible.
Drawing areas with names the same as previously added drawing areas will still be added.
@param grda GRJComponentDrawingArea to add.
*/
public void addDrawingArea ( GRDrawingArea grda ) {
	addDrawingArea ( grda, false );
}

/**
Add a drawing area to the device.  The device will then manage the drawing areas as much as possible.
@param grda GRJComponentDrawingArea to add.
@param replaceMatching if true, then a drawing area that matches an existing drawing area (same name)
will replaced the previous drawing area.
*/
public void addDrawingArea ( GRDrawingArea grda, boolean replaceMatching )
{	String routine = "GRJComponentDevice.addDrawingArea";

	if ( grda == null ) {
		Message.printWarning ( 2, routine, "NULL drawing area" );
		return;
	}

	if ( replaceMatching ) {
		// This currently will cause the drawing area to be added at the end.
		// This could be an issue if order is a problem.
		// Could do a replace at the same location but that also has implications and a new "replace" method might be better.
		for ( int ida = this.drawingAreaList.size() - 1; ida >= 0; --ida ) {
			GRDrawingArea da = this.drawingAreaList.get(ida);
			if ( da.getName().equals(grda.getName()) ) {
				// Remove the match
				this.drawingAreaList.remove(ida);
			}
		}
	}

	// Add the drawing area at the end.
	
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine,
		"Adding drawing area \"" + grda.getName() + "\" to device \"" + _name + "\"" );
	}
	this.drawingAreaList.add ( grda );
}

/**
Clear the device and fill with white.  Should be defined in derived class.
*/
public void clear ()
{
}

/**
Clears the double-buffer.
*/
public void clearDoubleBuffer() {
	int width = _buffer.getWidth();
	int height = _buffer.getHeight();
	_buffer = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
	_graphics = (Graphics2D)_buffer.getGraphics();
}

/**
Close the device (used with PS files).  Should be defined in derived class.
*/
public void close ()
{
}

/**
Fill the device with the current color.  Should be defined in derived class.
*/
public void fill ()
{
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_graphics = null;
	_buffer = null;
	_limits = null;
	_image = null;
	_name = null;
	_note = null;
	this.drawingAreaList = null;
	super.finalize();
}

/**
Flush the device (used by PS devices and X-Windows).  Should be defined in derived class.
*/
public void flush ()
{
}

/**
Unsure of just what this is meant to do.  
@deprecated deprecated (2006-05-22) to see if any other classes are using this
method. If so, evaluate what they're doing, javadoc, and undeprecate.  
Alternately, REVISIT (JTS - 2006-05-23) in a few months and if it's still here, remove.
*/
public void forceGraphics(Graphics g) {
	_graphics = (Graphics2D)g;
}

/**
 * Return a drawing area requested by name.
 * @return the first matching drawing area (exact string match) or null if not matched.
 */
public GRDrawingArea getDrawingArea ( String daName ) {
	for ( GRDrawingArea da : this.drawingAreaList ) {
		if ( da.getName().equals(daName) ) {
			return da;
		}
	}
	return null;
}

/**
Return the internal Image used for double-buffering or null if image is null or no double-buffering.
@return Image for the device.
*/
//public Image getImage () {
public BufferedImage getImage () {
//	return _image;
	return _buffer;
}

/**
Return the current limits of the device.
@param recompute If true, the limits are retrieved from the JComponent.
Otherwise the previous limits are returned (use the former when creating 
new drawing areas, the latter when operating from within a drawing area, 
assuming that resizing is being handled somewhere).
*/
public GRLimits getLimits ( boolean recompute )
{	if ( recompute ) {
		GRLimits limits = new GRLimits ();

		// Get the size of the JComponent...

		Rectangle bounds = getBounds ();
		// Now transfer into GR, where the origin is at the lower left...

		limits.setLeftX ( 0.0 );
		limits.setBottomY ( 0.0 );
		limits.setRightX ( bounds.width );
		limits.setTopY ( bounds.height );
		// This will set _limits...
		setLimits ( limits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "GRJComponentDevice.getLimits",
			"Device limits are: 0.0,0.0 " + bounds.width + "," + bounds.height );
		}
	}
	return _limits;
}

public GRLimits getLimits ( boolean recompute, double scale )
{	if ( recompute ) {
		GRLimits limits = new GRLimits ();

		// Get the size of the JComponent...

		Rectangle bounds = getBounds ();
		// Now transfer into GR, where the origin is at the lower left...

		limits.setLeftX ( 0.0 );
		limits.setBottomY ( 0.0 );
		limits.setRightX ( bounds.width  / scale);
		limits.setTopY ( bounds.height / scale);
		// This will set _limits...
		setLimits ( limits );
		if ( Message.isDebugOn ) {
			Message.printDebug ( 30, "GRJComponentDevice.getLimits",
			"Device limits are: 0.0,0.0 " + bounds.width + "," + bounds.height );
		}
	}
	return _limits;
}

/**
Returns the current limits of the device.  This checks the size of the JComponent.
@return the current limits of the device.
*/
public GRLimits getLimits ()
{	return _limits;
}

/**
Returns the Graphics instance that is being shared for drawing.
@return the Graphics instance that is being shared for drawing.
*/
public Graphics getPaintGraphics()
{	return _graphics;
}

/**
Indicate whether the device Y axis starts at the upper left.
@return true if the device Y axis starts at the upper left.
*/
public boolean getReverseY()
{	return _reverse_y;
}

/**
Returns the device units (GRUnits.*).
@return The device units (GRUnits.*).
*/
public int getUnits ( )
{	return _units;
}

/**
Initializes member variables.
@param props a PropList with settings for the device.
*/
private void initialize ( PropList props )
{	// For now we always manage the double-buffer internally for JComponents...

	// This turns off the double buffering.  Double-buffering is always
	// on by default in Swing, but RTi's preferred mode of operation
	// is for it to always be OFF by default, and enabled only when selected.

// TODO (JTS - 2003-05-2)
// Instead of doing the double-buffering as we are
// doing it now (with an off-screen buffer that we manage), maybe
// we could just make the calls to stopDoubleBuffering and 
// startDoubleBuffering be wrappers around calls to this?
// i.e.:
// 
// public void stopDoubleBuffering() {
//	RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
// }
//
// public void startDoubleBuffer() {
//	RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
// }
//
// and then ignore the rest of it?  Tests should be run as to the performance
// effects of each.  One advantage of NOT doing it that way is the enhanced control it gives us.  
//
// Food for thought.

	//RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);

	setDoubleBuffered ( false );

	// Set the general information...

	this.drawingAreaList = new ArrayList<GRDrawingArea>(5);
	_mode = GRUtil.MODE_DRAW;
	_name = new String ();
	_note = new String ();
	_orientation = GRDeviceUtil.ORIENTATION_PORTRAIT;
	_page = 0;
	_printing = false;
	//_reverse_y = false;
	_sizedrawn = -1;
	_sizeout = -1;
	_status = GRUtil.STATUS_OPEN;
	_type = 0;
	//_units = GRUnits.MM;	// Default but needs to be reset.
	_units = GRUnits.PIXEL;	// GRJComponentDevice

	/// Set the values that were passed in...

	if ( props == null ) {
		return;
	}

	String prop_value;
	prop_value = props.getValue("Name");
	if ( prop_value != null ) {
		_name = new String ( prop_value );
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
		else {
			_orientation = GRDeviceUtil.ORIENTATION_LANDSCAPE;
		}
	}
	prop_value = props.getValue("Note");
	if ( prop_value != null ) {
		_note = new String ( prop_value );
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
	else {
		// Set the limits...
		setLimits ( limits );
	}

	// The size of the device will have been set in the constructor, but
	// the base class only knows how to store the data.  Force a resize here.
	//resize ( _devx1, _devy1, _devx2, _devy2 );

	// Set to null.  Wait for the derived class to set the graphics for use throughout the drawing event...

	// Set information used by the base class and other code...

	_graphics = null;
	_reverse_y = true;	// Java uses y going down.  This is handled properly in
				// GRJComponentDrawingArea.scaleYData().
	// Set the limits to the JComponent size...
	setLimits ( getLimits ( true ) );
}

/**
Returns whether the device is drawing in anti-aliased mode or not.
@return whether the device is drawing in anti-aliased mode or not.
*/
public boolean isAntiAliased() {
	return __isAntiAliased;
}

/**
Indicate whether the device is in the process of printing.
@return true if the device is in the process of printing, false if not.
*/
public boolean isPrinting()
{	return _printing;
}

// TODO sam 2017-03-03 remove this method as it interferes with newer paint() behavior
// that calls paintComponent() and derived classes should call setPaintGraphics() to save the graphics.
/**
This method is called when the JComponent is to be drawn.  It is expected that
classes extended from this base class will implement a paint() method that
either itself sets the graphics or calls super.paint() to call this method to
set the graphics.  Using a GRJComponentDevice directly will result in this method
being called for resize, etc., and the graphics in effect at the time is set as
the current graphics.   The Graphics can then be used by subsequent calls for
drawing.  The base class paint() is not called from this method.
*/
//public void paint ( Graphics graphics )
//{	_graphics = (Graphics2D)graphics;
//}

/**
Indicates the end of a page of output.  Used in PS and should be defined in derived classes.
*/
public void pageEnd ()
{
}

/**
End of a plot of output, used for PS.  This should be implemented in derived classes.
*/
public void plotEnd ( int flag )
{
}

/**
Resize the device to the given size.
@param limits Size of device (JComponent) as GRLimits.
@see GRLimits
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
@param x1 New lower-left X-coordinate of device (usually zero).
@param y1 New lower-left Y-coordinate of device (usually zero).
@param x2 New top-right X-coordinate of device.
@param y2 New top-right Y-coordinate of device.
*/
public void resize ( double x1, double y1, double x2, double y2 )
{	resize2 ( (int)(x2 - x1), (int)(y2 - y1) );
}

/**
Resize the device to the given size.  Rename the routine resize2 because there
is a deprecated resize method in JComponent.
@param width New width of the device (JComponent).
@param height New height of the device (JComponent).
*/
public void resize2 ( int width, int height )
{	setSize ( width, height );
}

/**
Set the device limits (size) using a GRLimits.  This only sets the limits.  The
device must be resized in the derived class.
@param limits GRLimits indicating the size of the device.
*/
public void setLimits ( GRLimits limits )
{	_devx1 = limits.getLeftX();
	_devy1 = limits.getBottomY();
	_devx2 = limits.getRightX();
	_devy2 = limits.getTopY();
	_limits = new GRLimits ( limits );
	if ( Message.isDebugOn ) {
		String routine = "GRDevice.setLimits";
		Message.printDebug ( 1, routine, "Setting \"" + _name + "\" device limits to " + limits );
	}
}

/**
Set the Graphics used by the device for drawing.  This Graphics should be
reset at each paint in code that extends a GRJComponentDevice because Graphics
resources are typically created and destroyed dynamically by the application.
@param graphics instance to use for drawing to the device.
*/
public void setPaintGraphics ( Graphics graphics )
{	_graphics = (Graphics2D)graphics;
}

/**
Save as an image file.  The file name will be examined for the extension
to determine what kind of file to save as (currently JPEG, JPG, and PNG 
are supported).  If the file extension is not supported, a .jpg extension
is added and it is saved as a JPEG.
@throws IOException if the image used for double-buffering is null.
@param filename File name to write.  An appropriate extension will be added.
*/
public void saveAsFile ( String filename )
throws IOException
{	saveAsFile ( filename, (PropList)null );
}

/**
Save as an image file.  The file name will be examined for the extension
to determine what kind of file to save as (currently JPEG, JPG, and PNG 
are supported).  If the file extension is not supported, a .jpg extension
is added and it is saved as a JPEG.
@throws IOException if the image used for double-buffering is null.
@param filename File name to write.  An appropriate extension will be added.
@param props Properties for the image.  Currently the only accepted property
is Quality, which can be 0 (low quality, high compression) to 100 (high
quality, no compression).  It might be useful at some point to enable an
Interactive=true option to allow a pop-up dialog to specify JPEG information.
TODO (JTS - 2003-05-05) Evaluate pop-up dialog to query for quality - not as important now that PNG is supported.
*/
public void saveAsFile ( String filename, PropList props )
throws IOException
{
	String routine = "GRJComponentDevice.saveAsFile";

	if (_buffer == null) {
		throw new IOException ( "No internal image to save" );
	}
	// Else, create an image from the JComponent???

	// Defaults...

	int image_quality = 90; // Go for quality versus compression.

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
	boolean jpeg = true;
	String upFilename = filename.toUpperCase().trim();
	if (upFilename.endsWith(".JPG") || upFilename.endsWith(".JPEG")) {
		newfilename = filename;
	}
	else if (upFilename.endsWith(".PNG")) {
		newfilename = filename;
		jpeg = false;
	}
	else {
		// Add a standard extension...
		newfilename = filename + ".jpg";
	}

	if (jpeg) {
		try {	
			FileOutputStream os = new FileOutputStream(newfilename);
			JpegEncoder jpg = new JpegEncoder(_buffer, image_quality, os);
			jpg.Compress();
			os.flush();
			os.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			Message.printWarning(2, routine, "Error saving image file \"" + newfilename + "\"");
			Message.printWarning(2, routine, e);
			throw new IOException("Writing JPEG file \"" + newfilename + "\" failed." );
		}
	}
	else {
		// TODO SAM 2011-07-09 why not use ImageIO for all image types so we can do away with
		// the custom JPEG encoder?
		try {
			File file = new File(newfilename);
			ImageIO.write(_buffer, "png", file);
		}
		catch (Exception e) {
			throw new IOException ( "Error writing png file: " + newfilename);
		}
	}
}

/**
Sets whether the device is drawing in anti-aliased mode or not, for general drawing.
Anti-aliasing for text is always set as per the following
(see: https://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html)
<pre>
graphics2D.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
</pre>
@param antiAlias whether the device is drawing in anti-aliased mode or not.
*/
public void setAntiAlias(boolean antiAlias)
{
	if (antiAlias) {
		_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	}
	else {
		_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	__isAntiAliased = antiAlias;
	// TODO sam 2017-02-05 need to decide if this is the right place to put the text default
	// Text is always optimized
	_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
	// TODO sam 2017-02-05 LCD could use the following if could guarantee LCD on all of multiple screens
	//_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
}

/**
Sets up clipping on the current device and clips all drawing calls to the
rectangle specified in the GRLimits.  If GRLimits is null, clipping is turned off.
@param clipLimits the limits to which to clip drawing.  Any values that lie
outside of the rectangle specified by the GRLimits will not be drawn.  If
clipLimits is null, clipping will be turned off.
*/
public void setClip(GRLimits clipLimits) {
	if (clipLimits == null) {
		_graphics.setClip(null);
		return;
	}

	int lx = (int)clipLimits.getLeftX();
	int ty = (int)clipLimits.getTopY();
	int w = (int)clipLimits.getWidth();
	int h = (int)clipLimits.getHeight();

	_graphics.setClip(lx, ty, w, h);
}

/**
Sets the graphics to use in drawing.  Turns off anti aliasing when called.
@param g the Graphics to use in drawing.
*/
public void setGraphics(Graphics g) {
	__isAntiAliased = false;
	if (!_doubleBuffered) {
		_graphics = (Graphics2D)g;
	}
	else {
		_graphics = (Graphics2D)_buffer.getGraphics();
	}
}	

/**
Sets the graphics to use in drawing.  Turns off anti aliasing when called.
@param g the Graphics2D to use in drawing.
*/
public void setGraphics(Graphics2D g) {
	__isAntiAliased = false;
	if (!_doubleBuffered) {
		_graphics = g;
	}
	else {
		_graphics = (Graphics2D)_buffer.getGraphics();
	}
	// Make fonts look better
	// See:  https://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html
	_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
}	

/**
Set the printing flag.  Set to true when the device is being used for printing,
false when drawing to the screen.
@param printing printing flag.
*/
public void setPrinting ( boolean printing )
{	_printing = printing;
}

/**
Sets up a double buffer for the device, with a buffer size equal to the
size with which the device was initialized.  This method calls 
the other setupDoubleBuffer((int)_devx1, (int)_devy1, (int)_devx2, (int)_devy2);).<p>
This method sets up the buffer region using a BufferedImage that has been
initialized to be of type TYPE_4BYTE_ABGR.  Any pixels that are not drawn in
the buffer will not be drawn to the screen when the buffer is transferred to
the screen; they have a 0 alpha level.
*/
public void setupDoubleBuffer() {
	setupDoubleBuffer((int)_devx1, (int)_devy1, (int)_devx2, (int)_devy2);
}

/**
Sets up a double buffer for the device, with a buffer size equal to the
size with which the device was initialized.  This method calls startDoubleBuffer().<p>
This method sets up the buffer region using a BufferedImage that has been
initialized to be of type TYPE_4BYTE_ABGR.  Any pixels that are not drawn in
the buffer will not be drawn to the screen when the buffer is transferred to
the screen; they have a 0 alpha level.
@param x1 the lower left X of the double buffer.
@param y1 the lower left Y of the double buffer.
@param x2 the upper right X of the double buffer.
@param y2 the upper right Y of the double buffer.
*/
public void setupDoubleBuffer(int x1, int y1, int x2, int y2) {
	int width = x2 - x1;
	int height = y2 - y1;
    
	if (_buffer != null) {
		_buffer = null;
        /* FIXME SAM 2008-01-01 Evaluate why is this here - probably a performance hit
		for (int i = 0; i < 10; i++) {
			System.gc();
		}
         */
	}

	// Message.printStatus(2, "", "Setting up double buffer size: " + width + "x" + height);
	_buffer = new BufferedImage(width, height,BufferedImage.TYPE_4BYTE_ABGR);
	startDoubleBuffer();
}	

/**
Shows what has been drawn to the double buffer by drawing it to the screen.
*/
public void showDoubleBuffer() {
	if (_doubleBuffered) {
		_graphics.drawImage(_buffer, 0, 0, null);
	}
}

/**
Shows what has been drawn to the double buffer by drawing it to the provided Graphics object.
@param g the Graphics object to which to draw the double buffer.
*/
public void showDoubleBuffer(Graphics g) {
	if (_doubleBuffered) {
		g.drawImage(_buffer, 0, 0, null);
	}
}

/**
Starts double buffering the drawing done with this device.  Note: This method is called by setupDoubleBuffer.
*/
public void startDoubleBuffer() {
	_graphics = (Graphics2D)_buffer.getGraphics();
	_doubleBuffered = true;
	_double_buffering = _doubleBuffered;
}

/**
Stops double buffering drawing calls.
*/
public void stopDoubleBuffer() {
	_doubleBuffered = false;
	_double_buffering = _doubleBuffered;
}

/**
 * Create a string representation of the device, useful for troubleshooting,
 * will include embedded newlines (\n).
 * @param outputDrawingAreas if true, properties for drawing areas will also be output.
 * @return a simple property=value list of device (and optionally drawing area) properties.
 */
public String toString ( boolean outputDrawingAreas ) {
	StringBuilder s = new StringBuilder();
	String nl = "\n";
	s.append ( "isAntiAliased=" + __isAntiAliased + nl );
	s.append ( "name=" + _name + nl );
	s.append ( "reverseY=" + _reverse_y + nl );
	if ( outputDrawingAreas ) {
		// Loop through the drawing areas
		// Make a copy and then sort by name
		List<GRDrawingArea> das = new ArrayList<GRDrawingArea>();
		for ( GRDrawingArea da : drawingAreaList ) {
			das.add(da);
		}
		// Sort by name
		//java.util.Collections.sort(das);
		// TODO sam 2017-02-05 decide whether should implement comparable or not
		for ( int ida = 0; ida < drawingAreaList.size(); ida++ ) {
			GRDrawingArea da = drawingAreaList.get(ida);
			s.append ( nl + "drawingAreaIndex = " + ida + nl );
			s.append ( da.toString() + nl );
		}
	}
	return s.toString();
}

/**
Translates the image a specified number of X and Y values.  Calls _graphics.translate();
@param x the x value to translate (can be negative).
@param y the y value to translate (can be negative).  Note that increasing Y
values of translation will move the image Down, as this is a java call and Y
gets larger the farther down the screen it goes.
*/
public void translate(int x, int y) {
	_graphics.translate(x, y);
}

}