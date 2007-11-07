// ----------------------------------------------------------------------------
// GRDevice - GR interface for a drawable device
// ----------------------------------------------------------------------------
// History:
//
// 2002-01-07	Steven A. Malers, RTi	Major rework.  The previous GRDevice
//					class extended Canvas.  In order to
//					support JComponent (lightweight Swing)
//					and Canvas (heavyweight AWT), the
//					GRDevice has been folded into the
//					GRCanvasDevice, GRJComponentDevice, and
//					GRPSDevice classes and the GRDevice
//					interface has been defined to allow
//					GRDrawingArea to remain generic.  This
//					results in some redundant data and code
//					in the classes but is necessary to
//					support the different windowing
//					environments.  Define in this interface
//					all the "virtual" methods that were
//					defined in the GRDevice class
//					previously.
// 2003-05-07	J. Thomas Sapienza, RTi	Made changes following review by SAM.
// 2005-04-29	JTS, RTi		Added isAntiAliased() and
//					setAntiAlias().
// ----------------------------------------------------------------------------

package RTi.GR;

import java.awt.Graphics;

// REVISIT (JTS - 2003-05-05)
// since this class is abstract, should the methods below be made abstract?

/**
GR interface for a drawable device.
*/
public abstract interface GRDevice
{

/**
Add a drawing area to the device.  The device will then manage the drawing
areas as much as possible.
@param grda GRDrawingArea to add.
*/
public void addDrawingArea ( GRDrawingArea grda );

/**
Return the current limits of the device, in device units.  This should be
defined in the derived class.
@return the current limits of the device.
*/
public GRLimits getLimits ();

/**
Return the Graphics instance to use for drawing.  This is typically a shared
resource set when a GRCanvasDevice or GRJComponentDevice paint() method is
called.
@return the Graphics instance to use for drawing.
*/
public Graphics getPaintGraphics();

/**
Indicate whether the device has a "reversed" Y axis (one that starts at the top
and increments down).
@return true if the Y axis starts at the top (upper left) and goes down (lower
left).
*/
public boolean getReverseY();

/**
Return the device units (GRUnits.*).
@return The device units.
*/
public int getUnits ( );

/**
Returns whether the graphics are currently being drawn antialiased.
@return true if antialiased, false if not.
*/
public boolean isAntiAliased();

/**
Indicate whether the device is in the process of printing.
@return true if the device is in the process of printing, false if not.
*/
public boolean isPrinting ();

/**
Complete the plot.
@param close_flag GRUtil.CLOSE_HARD to close the output completely (e.g., 
to close an output file).
*/
public void plotEnd ( int close_flag );

/**
Sets whether the graphics should be drawn anti aliased.
@param antiAlias if true, the graphics will be drawn anti aliased.
*/
public void setAntiAlias(boolean antiAlias);

/**
Set the Graphics used by the device for drawing.  This Graphics should be
reset at each paint() call in code that implements a GRDevice because Graphics
resources are typically created and destroyed dynamically by the application.
The Graphics will be used by all GRDrawingArea associated with the device.
@param graphics instance to use for drawing to the device.
*/
public void setPaintGraphics ( Graphics graphics );

} // End GRDevice
