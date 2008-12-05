// ----------------------------------------------------------------------------
// GRPSDrawingArea - GR PostScript drawing area class
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// Notes:	(1)	This class is the driver for drawing to PostScript
//			files.  These drawing capabilities are independent of
//			the Java API and can be used to create true PostScript
//			files (desirable for high resolution output).
//		(2)	All commands should assume that they can print with
//			no leading space and end with a newline.
// ----------------------------------------------------------------------------
// History:
//
// 10 Aug 1997	Steven A. Malers, RTi	Initial Java version as port of C++/C
//					code.
// 28 Mar 1998	SAM, RTi		Revisit the code and implement a
//					a totally new design that is 100%
//					object oriented.
// 29 Aug 1998	SAM, RTi		Copy the GRDrawingArea class and fill
//					in code from the C/C++ code.
// 08 Nov 1999	SAM, RTi		Enable line dash code.
// 2002-01-18	SAM, RTi		Change setFont() to take a style.
// 2003-05-01	J. Thomas Sapienza, RTi	Made changes to accomodate the massive
//					restructuring of GR.java.
// 2003-05-02	JTS, RTi		Added some methods because GRDrawingArea
//					was made abstract.
// 2005-04-20	JTS, RTi		* Added setClip().
//					* Added getClip().
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

import java.awt.Shape;

import java.io.PrintWriter;

import java.util.List;

import RTi.Util.IO.PropList;

import RTi.Util.Math.MathUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class implements a driver to PostScript files.
TODO (SAM - 2003-05-08) Revisit code later -- it may have limited use.
*/
public class GRPSDrawingArea extends GRDrawingArea
{

class GRPSFontData {
	String grname;
	String psname;
	GRPSFontData ( String g, String p ){
		grname = new String (g);
		psname = new String (p);
	}
}


/**
Set to the GRDevice file.
*/
private PrintWriter _fp = null;

/**
Line separator.
*/
private static String _nl = null;

/**
Postscript fonts.
*/
private static GRPSFontData [] _psfonts = null;

/**
GRPSDevice associated with this GRPSDrawingArea.  This is a cast of the 
device stored in GRDevice.
*/
private GRPSDevice _psdev = null;

/**
General constructor.
@param dev GRDevice associated with the drawing area.
@param name A name for the drawing area.
@param aspect Aspect for the axes of the drawing area.
@param draw_limits Drawing limits (device coordinates to attach the lower-left
and upper-right corner of the drawing area).
@param units Units of the limits (will be converted to device units).
@param flag Modifier for drawing limits.  If GRLimits.UNIT, then the limits are
assumed to be percentages of the device (0.0 to 1.0) and the units are not
used.
@param data_limits Data limits associated with the lower-left and upper-right
corners of the drawing area.
@see GRAspect
*/
public GRPSDrawingArea ( GRPSDevice dev, String name, int aspect,
			GRLimits draw_limits, int units, int flag,
			GRLimits data_limits )
{	String routine = "GRPSDrawingArea(...)";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine,
		"Constructing using all arguments, name=\"" + name + "\"" );
	}
	initialize ( dev, name, aspect, draw_limits, units, flag, data_limits );
}

/**
Constructor.
@param dev the GRDevice associated with the drawing area.
@param props PropList with drawing area settings.
*/
public GRPSDrawingArea ( GRPSDevice dev, PropList props )
throws GRException
{	
	// Call parent...

	super ( dev, props );
	String routine = "GRPSDrawingArea(GRPSDevice,PropList)";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine,
		"Constructing using PropList" );
	}
	try {	initialize ( dev, props );
	}
	catch ( GRException e ) {
		throw e;
	}
}

/**
Clear the drawing area (draw in the current color).
This has no effect in PostScript.
*/
public void clear ( )
{
}

/**
Set the clip on/off for the drawing area.  The full drawing area is used for
clipping (ignoring aspect).
@param flag Indicates whether clipping should be on or off.
*/
public void clip ( boolean flag )
{
	if ( flag ) {
		// Set clip path to drawing area limits...
		_fp.print ( "gsave" + " " +
				_drawx1 + " " + _drawy1 + " moveto " +
				_drawx2 + " " + _drawy1 + " lineto " +
				_drawx2 + " " + _drawy2 + " lineto " +
				_drawx1 + " " + _drawy2 +
				" lineto closepath clip newpath " + _nl );
	}
	else {	// Set the clip path to the previous limits (assume to be full
		// page)...
		_fp.print ( "grestore" + _nl );
	}
}

/**
Add a comment to the output file.
@param comment Comment to add to file.
*/
public void comment ( String comment )
{
	_fp.print ( "%% " + comment + _nl );
}

/**
Draw an arc using the current color, line, etc.
@param a1 Initial angle to start drawing (0 is at 3 o'clock, then
counterclockwise).
@param a2 Ending angle.
@param x X-coordinate of center.
@param xr X-radius.
@param y Y-coordinate of center.
@param yr Y-radius.
*/
public void drawArc (	double x, double y, double xr, double yr, double a1,
			double a2 )
{
	// For the time being, assume we are dealing with circles...

	_fp.print ( "newpath" + _nl + x + " " + y + " " + xr + " " + a1 + " " +
	a2 + " arc" + _nl );
	stroke ();
}

/**
Not implemented
*/
public void drawCompoundText (	List text, GRColor color, double x, double y, double angle, int flag )
{	String routine = "GRPSDrawingArea.drawCompoundText";
	Message.printWarning ( 1, routine,
	"GRPSDrawingArea.drawCompoundText not implemnted" );
}

/**
Draw a line.
@param x array of x points.
@param y array of y points.
*/
public void drawLine ( double x[], double y[] )
{	moveTo ( x[0], y[0] );
	lineTo ( x[1], y[1] );
}

/**
Draw a line
@param x0 the first x coordinate
@param y0 the first y coordinate
@param x1 the end x coordinate
@param y1 the end y coordinate
*/
public void drawLine ( double x0, double y0, double x1, double y1 )
{	moveTo ( x0, y0 );
	lineTo ( x1, y1 );
}

/**
Draws a polygon.
@param x an array of x coordinates
@param y an array of y coordinates.
*/
public void drawPolygon ( double x[], double y[] )
{
	int npts = x.length;
	drawPolygon ( npts, x, y );
}

/**
Draws a polygon.
@param npts the number of points in the polygon.
@param x an array of x coordinates
@param y an array of y coordinates.
*/
public void drawPolygon ( int npts, double x[], double y[] )
{
	int i = 0;
	if ( npts > 0 ) {
		moveTo ( x[0], y[0] );
	}
	for ( i = 1; i < npts; i++ ) {
		lineTo ( x[i], y[i] );
	}
	lineTo ( x[0], y[0] );
	if ( i > 2 ) {
		lineTo ( x[1], y[1] );
	}
	stroke ();
}

/**
Draws a polyline 
@param x an array of x coordinates
@param y an array of y coordinates.
*/
public void drawPolyline ( double x[], double y[] )
{
	drawPolyline ( x.length, x, y );
}

/**
Draws a polyline 
@param npts the number of points in the polyline
@param x an array of x coordinates
@param y an array of y coordinates.
*/
public void drawPolyline ( int npts, double x[], double y[] )
{
	if ( npts > 0 ) {
		moveTo ( x[0], y[0] );
	}
	for ( int i = 1; i < npts; i++ ) {
		lineTo ( x[i], y[i] );
	}
	stroke();
}

/**
Draws a rectangle in the current color.
@param xll the lower-left x coordinate
@param yll the lower-left y coordinate
@param width the width of the rectangle
@param height the height of the rectangle
*/
public void drawRectangle ( double xll, double yll, double width, double height)
{
	moveTo ( xll, yll );
	lineTo ( xll + width, yll );
	lineTo ( xll + width, yll + height );
	lineTo ( xll, yll + height );
	lineTo ( xll, yll );
	stroke ();
}

/**
Draw text.
@param text the text string to draw.
@param x the x location at which to draw the string.
@param y the y location at which to draw the string.
@param a the angle of rotation of the text, counter-clockwise
@param flag one of the GRText.* values specifying how to draw the string.
*/
public void drawText ( String text, double x, double y, double a, int flag ) {
	drawText(text, x, y, a, flag, 0);
}

/**
Draw text.
@param text the text string to draw.
@param x the x location at which to draw the string.
@param y the y location at which to draw the string.
@param a the angle of rotation of the text, counter-clockwise
@param flag one of the GRText.* values specifying how to draw the string.
@param rotationDegrees degrees the text is rotated (UNUSED)
*/
public void drawText ( String text, double x, double y, double a, int flag,
double rotationDegrees)
{	int	aflag;
	double	px, py;

	if ( (a < .001) && (a > -.001) ) {
		aflag = 0;
	}
	else {	aflag = 1;
	}
	if ( aflag != 0 ) {
		_fp.print ( "gsave " + x + " " + y + " translate " + a +
		" rotate " );
		px = 0.0;
		py = 0.0;
	}
	else {	px = x;
		py = y;
	}
	if ( (flag & GRText.CENTER_Y) != 0 ) {
		py -= _fontht/2.0;
	}
	else if ( (flag & GRText.TOP) != 0 ) {
		py -= _fontht;
	}
	if ( (flag & GRText.CENTER_X) != 0 ) {
		_fp.print ( "(" );
		drawText2 ( text );
		_fp.print ( ") " + px + " " + py + " CS" + _nl);
	}
	else if ( (flag & GRText.RIGHT) != 0 ) {
		_fp.print ( "(" );
		drawText2 ( text );
		_fp.print ( ") " + px + " " + py + " RS" + _nl);
	}
	else {	_fp.print ( "(" );	// default
		drawText2 ( text );
		_fp.print ( ") " + px + " " + py + " LS" + _nl);
	}
	if ( aflag != 0) {
		_fp.print ( " grestore" + _nl );
	}
	stroke();
}

/**
Draw text to file, replacing ( with \( and ) with \)
@param text the text string to draw.
*/
public void drawText2 ( String text )
{
	if ( text == null ) {
		return;
	}
	int length = text.length();
	StringBuffer buffer = new StringBuffer();
	char c;
	for ( int i = 0; i < length; i++ ) {
		c = text.charAt(i);
		if ( (c == ')') || (c == '(') || (c == '\\') ) {
			buffer.append ( '\\' );
		}
		buffer.append ( c );
	}
	_fp.print ( buffer.toString() );
}

/**
Fill an arc using the current color, line, etc.
@param x X-coordinate of center.
@param rx X-radius.
@param y Y-coordinate of center.
@param ry Y-radius.
@param a1 Initial angle to start drawing (0 is at 3 o'clock, then
counterclockwise).
@param a2 Ending angle.
@param fillmode Fill mode for arc (see GR.FILL_CHORD or GR.FILL_PIE).
*/
public void fillArc (		double x, double y, double rx, double ry,
				double a1, double a2, int fillmode )
{
	// For the time being, assume we are dealing with circles...

	_fp.print ( "newpath" + _nl + x + " " + y + " " + rx + " " + a1 + " " +
	a2 + " arc closepath fill" + _nl );
	stroke ();
}

/**
FIlls a polygon with the current color.
@param x an array of x coordinates
@param y an array of y coordinates
*/
public void fillPolygon ( double [] x, double [] y )
{
	int n = x.length;
	fillPolygon ( n, x, y );
}

/**
Fill a polygon with the current color.
@param n the number of coordinates
@param x X-coordinates of points in polygon.
@param y Y-coordinates of points in polygon.
*/
public void fillPolygon ( int n, double [] x, double [] y )
{
	int ny = y.length;
	if ( ny < n ) {
		n = ny;
	}
	if ( n < 2 ) {
		return;
	}
	stroke ();
	_fp.print ( "newpath " );
	moveTo ( x[0], y[0] );
	for ( int i = 1; i < n; i++ ) {
		lineTo ( x[i], y[i] );
	}
	lineTo ( x[0], y[0] );
	_fp.print ( " closepath fill " + _nl );
	stroke();
}

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	_fp = null;
	_psdev = null;
	super.finalize();
}

/**
Flush the drawing area (and device).
*/
public void flush ()
{
	stroke();
}

/**
Not implemented.
*/
public GRLimits getBoundingBoxFromFile ( String filename )
{	String routine = "GRPSDrawingArea.getBoundingBoxFromFile";
	Message.printWarning ( 1, routine, routine + " not implemented" );
	return null;
}

/**
Not implemented.
*/
public Shape getClip() {
	return null;
}

/**
Get the device extents of data limits.  Simple returns <tt>limits</tt>.
@param limits value returned
@param flag unused
@return the limits parameter.
*/
public GRLimits getDataExtents ( GRLimits limits, int flag )
{
	// temporary...
	return limits;
}

/**
Get the data values for device coordinates.  This is typically used when
interpreting a mouse action and therefore for a PostScript file has little use.
@return A GRPoint with the data point.
@param devx Device x-coordinate.
@param devy Device y-coordinate.
@param flag GR.COORD_DEVICE if the coordinates are originating with the device
(e.g., a mouse) or GR.COORD_PLOT if the coordinates are plotting coordinates
(this flag affects how the y-axis is reversed on some devices).
*/
public GRPoint getDataXY ( double devx, double devy, int flag )
{	double x = MathUtil.interpolate ( devx, _plotx1, _plotx2, 
		_datax1, _datax2 );
	double y = MathUtil.interpolate ( devy, _ploty1, _ploty2, 
		_datay1, _datay2 );
	return new GRPoint ( x, y );
}
/**
Initialize drawing area settings.
@param dev GRDevice associated with the drawing area.
@param name A name for the drawing area.
@param aspect Aspect for the axes of the drawing area.
@param draw_limits Drawing limits (device coordinates to attach the lower-left
and upper-right corner of the drawing area).
@param units Units of the limits (will be converted to device units).
@param flag Modifier for drawing limits.  If GRLimits.UNIT, then the limits are
assumed to be percentages of the device (0.0 to 1.0) and the units are not
used.
@param data_limits Data limits associated with the lower-left and upper-right
corners of the drawing area.
*/
private void initialize ( GRPSDevice dev, String name, int aspect,
			GRLimits draw_limits, int units, int flag,
			GRLimits data_limits )
{
	String	routine = "GRDrawingArea.initialize(args)";
	Message.printWarning ( 1, routine, "Use PropList version" );
}

/**
Initialize drawing area settings.
@param dev GRDevice associated with the drawing area.
@param props PropList with drawing area settings.
*/
private void initialize ( GRPSDevice dev, PropList props )
throws GRException
{	String	routine = "GRDrawingArea.initialize(PropList)";
	int	dl = 10;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Initializing" );
	}

	// Initialize the basic data members...

	_status		= GRUtil.STAT_OPEN;
	_axisx		= GRAxis.LINEAR;
	_axisy		= GRAxis.LINEAR;
	_color		= GRColor.white;
	_dataset	= false;
	_datax1		= 0.0;
	_datax2		= 0.0;
	_datay1		= 0.0;
	_datay2		= 0.0;
	_drawset	= false;
	_drawx1		= 0.0;
	_drawx2		= 0.0;
	_drawy1		= 0.0;
	_drawy2		= 0.0;
	_font		= "Helvetica";
	_fontht		= 8.0;
	_lastx		= 0.0;
	_lasty		= 0.0;
	_linearx1	= 0.0;
	_linearx2	= 0.0;
	_lineary1	= 0.0;
	_lineary2	= 0.0;
	_name		= new String ();

	// Device data that we want reference to here...

	_psdev		= (GRPSDevice)_dev;
	_fp		= _psdev._fp;

	// Set passed-in values that are not set in the base GRPSDevice class...

/* Need to make separate call or add props...
	setDrawingLimits ( draw_limits, units, flag );
	setDataLimits ( data_limits );
	_devyshift = dev.getLimits().getTopY ();
	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine, "Device height is " +
		_devyshift );
	}
*/
	// Set the newline...

	if ( _nl == null ) {
		_nl = System.getProperty ( "line.separator" );
	}

	// Set the shared font data...

	if ( _psfonts == null ) {
		_psfonts = new GRPSFontData[14];
		_psfonts[0] = new GRPSFontData ( "courier", "Courier" );
		_psfonts[1] = new GRPSFontData ( "courier-bold",
				"Courier-Bold" );
		_psfonts[2] = new GRPSFontData ( "courier-boldoblique",
				"Courier-BoldOblique");
		_psfonts[3] = new GRPSFontData ( "courier-oblique",
				"Courier-Oblique" );
		_psfonts[4] = new GRPSFontData ( "helvetica",
				"Helvetica" );
		_psfonts[5] = new GRPSFontData ( "helvetica-bold",
				"Helvetica-Bold" );
		_psfonts[6] = new GRPSFontData ( "helvetica-boldoblique",
				"Helvetica-BoldOblique" );
		_psfonts[7] = new GRPSFontData ( "helvetica-oblique",
				"Helvetica-Oblique" );
		_psfonts[8] = new GRPSFontData ( "symbol",
				"Symbol" );
		_psfonts[9] = new GRPSFontData ( "times",
				"Times-Roman" );
		_psfonts[10] = new GRPSFontData ( "times-bold",
				"Times-Bold" );
		_psfonts[11] = new GRPSFontData ( "times-bolditalic",
				"Times-BoldItalic" );
		_psfonts[12] = new GRPSFontData ( "times-italic",
				"Times-Italic" );
		_psfonts[13] = new GRPSFontData ( "times-roman",
				"Times-Roman" );
	}
}

/**
Draws a line to a point from the last-drawn or position point.
@param x the x coordinate
@param y the y coordinate
*/
public void lineTo ( double x, double y )
{	
	_fp.print ( x + " " + y + " LT" + _nl );
	++_psdev._LineTo_count;
	if ( _psdev._LineTo_count >= GRPSDevice._MAXLineTo_count ) {
		stroke ();
		moveTo ( x, y );
	}
}

/**
Draws a line to a point from the last-drawn or positioned point.
@param point GRPoint defining where to draw to.
*/
public void lineTo ( GRPoint point )
{
	lineTo ( point.getX(), point.getY() );
}

/**
Moves the pen to a point
@param x the x coordinate to move to
@param y the y coordinate to move to
*/
public void moveTo ( double x, double y )
{
	_fp.print ( x + " " + y + " MT" + _nl );
}

/**
Moves the pen to a point
@param point the GRPoint to move to
*/
public void moveTo ( GRPoint point )
{
	moveTo ( point.getX(), point.getY() );
}

/**
Ends the page.
*/
public void pageEnd ( )
{
	_psdev.pageEnd();
}

/**
Starts the page.
*/
public void pageStart ( )
{
	_psdev.pageStart();
}

/**
Not implemented.
*/
public void print ()
{
}

/**
Not implemented.
*/
public void setClip(GRLimits deviceLimits) {}

/**
Not implemented.
*/
public void setClip(Shape clipShape) {}

/**
Set the current color.
@param color GRColor to use.
@see GRColor
*/
public void setColor ( GRColor color )
{	
	if ( color == null ) {
		String routine = "GRPSDrawingArea.setColor";
		Message.printWarning ( 2, routine, "Null color" );
	}
	_color = color;
	setColor (	(double)color.getRed()/255.0,
			(double)color.getGreen()/255.0,
			(double)color.getBlue()/255.0 );
}

/**
Set the current color.
@param r Red component in range 0.0 to 1.0.
@param g Green component in range 0.0 to 1.0.
@param b Blue component in range 0.0 to 1.0.
*/
public void setColor ( double r, double g, double b )
{	_color = new GRColor ( r, g, b );
	_fp.print ( r + " " + g + " " + b + " setrgbcolor" + _nl );
}

/**
Set the current color.
@param r Red component in range 0.0 to 1.0.
@param g Green component in range 0.0 to 1.0.
@param b Blue component in range 0.0 to 1.0.
*/
public void setColor ( float r, float g, float b )
{	setColor ( (double)r, (double)g, (double)b );
}

/**
Set the font for the drawing area.
@param font Font name (e.g., "Helvetica").
@param style Font style ("Plain", "Bold", or "Italic").  Currently this is
ignored for PostScript.
@param fontht Font height in points.
*/
public void setFont ( String font, String style, double fontht )
{	int nfonts = _psfonts.length;
	for ( int i = 0; i < nfonts; i++ ) {
		if (	font.equalsIgnoreCase( _psfonts[i].grname) ) {
			_font = font;
			_fontht = fontht;
			_fp.print (
			"/" + _psfonts[i].psname +
			" findfont " +
			StringUtil.formatString(fontht,"%.3f") +
			" scalefont setfont" + _nl );
		}
	}
}

/**
Sets the line cap style, as defined in GRDrawingAreaUtil.CAP*
@param linecap the linecap style.
*/
public void setLineCap ( int linecap )
{
	if ( linecap == GRDrawingAreaUtil.CAP_BUTT ) {
		_fp.print ( "0 setlinecap" + _nl );
	}
	else if ( linecap == GRDrawingAreaUtil.CAP_ROUND ) {
		_fp.print ( "1 setlinecap" + _nl );
	}
	else if ( linecap == GRDrawingAreaUtil.CAP_PROJECT ) {
		_fp.print ( "2 setlinecap" + _nl );
	}
}

/**
Sets the line dash pattern.
@param dash array defining the dash pattern
@param offset line offset.
*/
public void setLineDash ( double dash[], double offset )
{
	if ( dash == null ) {
		// Set to solid line...
		_fp.print ( "[] 0 setdash" + _nl );
		return;
	}
	int ndash = dash.length;
	if ( ndash == 0 ) {
		// Set to solid line...
		_fp.print ( "[] 0 setdash" + _nl );
		return;
	}
	//if ( ndash < 1 ) {
		//return;
	//}
	_fp.print ( "[" );
	for ( int i = 0; i < ndash; i++ ) {
		_fp.print ( dash[i] + " " );
	}
	_fp.print ( "] " + offset + " setdash" + _nl );
}

/**
Sets the line join style.
@param join the line join style, as definied in GRDrawingAreaUtil.JOIN*
*/
public void setLineJoin ( int join )
{
	if ( join == GRDrawingAreaUtil.JOIN_MITER ) {
		_fp.print ( "0 setlinejoin" + _nl );
	}
	else if ( join == GRDrawingAreaUtil.JOIN_MITER ) {
		_fp.print ( "1 setlinejoin" + _nl );
	}
	else if ( join == GRDrawingAreaUtil.JOIN_MITER ) {
		_fp.print ( "2 setlinejoin" + _nl );
	}
}

/**
Sets the line width.
@param linewidth the width of the line
*/
public void setLineWidth ( double linewidth )
{
	_fp.print ( "" + linewidth + " setlinewidth" + _nl );
	_linewidth = linewidth;
}

/**
Stroke.
*/
public void stroke ()
{
	_fp.print ( "ST" + _nl );
	_psdev._LineTo_count = 0;
}

/**
Not implemented.
*/
public void grid (int nxg, double xg[], int nyg, double yg[], int flag) {}

/**
Not implemented.
*/
public double getYData(double ydev) { return -999.99; }

/**
Not implemented.
*/
public double getXData(double xdev) { return -999.99; }

/**
Not implemented.
*/
public int getUnits() { return -999; }

/**
Not implemented.
*/
public GRLimits getTextExtents (String text, int flag) { return null; }

/**
Not implemented.
*/
public void fillRectangle (double xll, double yll, double width, double height)
{}

/**
Not implemented.
*/
public void fillRectangle (GRLimits limits) {}

/**
Not implemented.
*/
public void fillPolygon (int npts, double x[], double y[], int transparency) {}

} // End class GRPSDrawingArea
