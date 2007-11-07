// ---------------------------------------------------------------------------
// GRColor - class to store GRColors and color methods.
// ---------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file
// ---------------------------------------------------------------------------
// History:
//
// ?		Steven A. Malers, RTi	Initial version.
// 2000-10-15	SAM, RTi		Add COLOR_NAMES[], parseColor(),
//					toIngeter(), and toString().
// 2001-06-28	SAM, RTi		Change parseColor() to return a GRColor.
//					Try using 0, 0, -1 for None color for
//					transparency - does not work.  To
//					support, transparency, add
//					isTransparent().  Allow parseColor() to
//					parse strings with floating point or
//					integer RGB values.
// 2003-05-07	J. Thomas Sapienza, RTi	Made changes following review by SAM.
// 2004-10-27	JTS, RTi		Implements Cloneable.
// ---------------------------------------------------------------------------

package RTi.GR;

import java.awt.Color;
import java.util.Vector;

import RTi.Util.String.StringUtil;

/**
Class to store a color.  This class extends Color and adds features like using
named colors to simplify use with GUIs.
*/
public class GRColor 
extends Color
implements Cloneable
{

/**
Names of colors.
*/
public static final String[] COLOR_NAMES = {	"None",	// Transparent
						"Black",
						"Blue",
						"Cyan",
						"DarkGray",
						"Gray",
						"Green",
						"LightGray",
						"Magenta",
						"Orange",
						"Pink",
						"Red",
						"White",
						"Yellow"
					};

// The following works.  Casting does not because it is forward referencing.
/**
All of the static GR colors are available.
*/

public static final GRColor black = new GRColor ( 0.0, 0.0, 0.0 );
public static final GRColor blue = new GRColor ( 0.0, 0.0, 1.0 );
public static final GRColor cyan = new GRColor ( 0.0, 1.0, 1.0 );
public static final GRColor darkGray = new GRColor ( .184, .31, .31 );
public static final GRColor gray = new GRColor ( .753, .753, .753 );
public static final GRColor green = new GRColor ( 0.0, 1.0, 0.0 );
public static final GRColor lightGray = new GRColor ( .659, .659, .659 );
public static final GRColor magenta = new GRColor ( 1.0, 0.0, 1.0 );
public static final GRColor orange = new GRColor ( .8, .196, .196 );
public static final GRColor pink = new GRColor ( .737, .561, .561 );
public static final GRColor red = new GRColor ( 1.0, 0.0, 0.0 );
public static final GRColor white = new GRColor ( 1.0, 1.0, 1.0 );
public static final GRColor yellow = new GRColor ( 1.0, 1.0, 0.0 );

public static final GRColor gray10 = new GRColor ( 0.1, 0.1, 0.1 );
public static final GRColor gray20 = new GRColor ( 0.2, 0.2, 0.2 );
public static final GRColor gray30 = new GRColor ( 0.3, 0.3, 0.3 );
public static final GRColor gray40 = new GRColor ( 0.4, 0.4, 0.4 );
public static final GRColor gray50 = new GRColor ( 0.5, 0.5, 0.5 );
public static final GRColor gray60 = new GRColor ( 0.6, 0.6, 0.6 );
public static final GRColor gray70 = new GRColor ( 0.7, 0.7, 0.7 );
public static final GRColor gray80 = new GRColor ( 0.8, 0.8, 0.8 );
public static final GRColor gray90 = new GRColor ( 0.9, 0.9, 0.9 );

public static final GRColor grey10 = new GRColor ( 0.1, 0.1, 0.1 );
public static final GRColor grey20 = new GRColor ( 0.2, 0.2, 0.2 );
public static final GRColor grey30 = new GRColor ( 0.3, 0.3, 0.3 );
public static final GRColor grey40 = new GRColor ( 0.4, 0.4, 0.4 );
public static final GRColor grey50 = new GRColor ( 0.5, 0.5, 0.5 );
public static final GRColor grey60 = new GRColor ( 0.6, 0.6, 0.6 );
public static final GRColor grey70 = new GRColor ( 0.7, 0.7, 0.7 );
public static final GRColor grey80 = new GRColor ( 0.8, 0.8, 0.8 );
public static final GRColor grey90 = new GRColor ( 0.9, 0.9, 0.9 );

/**
Indicates whether the color is transparent or not.
*/
private boolean _is_transparent = false;

/**
Constructor.  Builds a GRColor with the given red, green and blue values.
@param r the red value in the range (0.0 - 1.0)
@param g the green value in the range (0.0 - 1.0)
@param b the blue value in the range (0.0 - 1.0)
*/
public GRColor ( double r, double g, double b )
{	super ( (float)r, (float)g, (float)b );
}

/**
Constructor.  Builds a GRColor with the given color.  <p>
From the Color.java javadocs: <p>Creates an opaque 
sRGB color with the specified combined RGB value consisting of the red 
component in bits 16-23, the green component in bits 8-15, and the blue 
component in bits 0-7. The actual color used in rendering depends on 
finding the best match given the color space available for a particular 
output device. Alpha is defaulted to 255.
@param rgb the color to set this GRColor to.
*/
public GRColor ( int rgb )
{	super ( rgb );
}

/**
Constructor.  Builds a GRColor with the given red, green and blue values.
@param r the red value in the range (0 - 255)
@param g the green value in the range (0 - 255)
@param b the blue value in the range (0 - 255)
*/
public GRColor ( int r, int g, int b )
{	super ( r, g, b );
}

/**
Constructor.  Builds a GRColor with the given red, green and blue values.
@param r the red value in the range (0.0 - 1.0)
@param g the green value in the range (0.0 - 1.0)
@param b the blue value in the range (0.0 - 1.0)
*/
public GRColor ( float r, float g, float b )
{
// NOTE!!
// This code is UGLY, but it's that way for a reason.  The original intent 
// was to have the constructor check to make sure that the color levels are
// in the bounds of 0.0 to 1.0, but since the call to "super" has be the
// first statement in a constructor, this was the only way to do it.

// Each parameter of the call to super (float, float, float) is a decision
// tree that first checks:
// is the color (r, g, or b) less than 0.0?
//     if so --> pass in 0.0 as the parameter
//     otherwise ...
//     is the color greater than 1.0?
//         if so --> pass in 1.0 as the parameter
//         otherwise ...
//         pass in the color itself.  
	super (
		((r < 0) ? 
			(float)0.0 :
			(r > 1.0) ?
				(float)1.0 :
				r),
		((g < 0) ? 
			(float)0.0 :
			(g > 1.0) ?
				(float)1.0 :
				g),
		((b < 0) ? 
			(float)0.0 :
			(b > 1.0) ?
				(float)1.0 :
				b));	
}

/**
Clones the Object.
@return a clone of the object.
*/
public Object clone() {
	try {
		return (GRColor)super.clone();
	}
	catch (Exception e) {
		return null;
	}
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	super.finalize();
}

/**
Indicate whether color is transparent (no color).
@return true if transparent.
*/
public boolean isTransparent ()
{	return _is_transparent;
}

/**
Set whether color is transparent (no color).
@param is_transparent True if color is transparent.
@return true if transparent, after set.
*/
public boolean isTransparent ( boolean is_transparent )
{	_is_transparent = is_transparent;
	return _is_transparent;
}

/**
Define a new color determined by parsing the string.  This version is used
because the base class already has a getColor() method.  Constructing from a
String is not supported because the Color base class cannot be easily
initialized.  Valid color strings include the following:
<ul>
<li>	Color name (e.g., "Black", or any other recognized color name included
	in COLOR_NAMES).</li>
<li>	Floating point RGB in the range 0 to 1 (e.g., "0.0,1.0,1.0").  The
	numbers must be floating point numbers (a period in the string will
	indicate that all are floating point numbers).</li>
<li>	Integer RGB in the range 0 to 255 (e.g., 0,255,255).</li>
<li>	Integer color where leftmost bits are ignored and others are in order
	RGB (e.g., 255 is blue).</li>
<li>	Hexadecimal similar to previous version (e.g., 0x000000ff).</li>
</ul>
@param color Name of color (see COLOR_NAMES).
@return a new Color instance, or black if the name cannot be matched or an
error occurs.
*/
public static GRColor parseColor ( String color )
{	if ( color.indexOf('.') >= 0 ) {
		// Assume 0.0-1.0 RGB values separated by commas
		Vector v = StringUtil.breakStringList(color,",",
			StringUtil.DELIM_SKIP_BLANKS );
		if ( (v == null) || (v.size() != 3) ) {
			v = null;
			return new GRColor(0);
		}
		GRColor grc = new GRColor (
			StringUtil.atof((String)v.elementAt(0)),
			StringUtil.atof((String)v.elementAt(1)),
			StringUtil.atof((String)v.elementAt(2)) );
		v = null;
		return grc;
	}
	else if ( color.indexOf(',') >= 0 ) {
		// Assume 0-255 RGB values separated by commas
		Vector v = StringUtil.breakStringList(color,",",
			StringUtil.DELIM_SKIP_BLANKS );
		if ( (v == null) || (v.size() != 3) ) {
			v = null;
			return new GRColor(0);
		}
		GRColor grc = new GRColor (
			StringUtil.atoi((String)v.elementAt(0)),
			StringUtil.atoi((String)v.elementAt(1)),
			StringUtil.atoi((String)v.elementAt(2)) );
		v = null;
		return grc;
	}
	else if ( color.equalsIgnoreCase("black") ) {
		return new GRColor ( 0, 0, 0 );
	}
	else if ( color.equalsIgnoreCase("blue") ) {
		return new GRColor ( 0, 0, 255 );
	}
	else if ( color.equalsIgnoreCase("cyan") ) {
		return new GRColor ( 0, 255, 255 );
	}
	else if ( color.equalsIgnoreCase("darkgray") ||
		color.equalsIgnoreCase("darkgrey") ) {
		return new GRColor ( 84, 84, 84 );
	}
	else if ( color.equalsIgnoreCase("gray") ||
		color.equalsIgnoreCase("grey") ) {
		return new GRColor ( 192, 192, 192 );
	}
	else if ( color.equalsIgnoreCase("green") ) {
		return new GRColor ( 0, 255, 0 );
	}
	else if ( color.equalsIgnoreCase("lightgray") ||
		color.equalsIgnoreCase("lightgrey") ) {
		return new GRColor ( 168, 168, 168 );
	}
	else if ( color.equalsIgnoreCase("magenta") ) {
		return new GRColor ( 255, 0, 255 );
	}
	else if ( color.equalsIgnoreCase("none") ) {
		GRColor grc = new GRColor ( 0, 0, 0 );
		grc.isTransparent(true);
		return grc;
	}
	else if ( color.equalsIgnoreCase("orange") ) {
		return new GRColor ( 204, 50, 50 );
	}
	else if ( color.equalsIgnoreCase("pink") ) {
		return new GRColor ( 188, 143, 143 );
	}
	else if ( color.equalsIgnoreCase("red") ) {
		return new GRColor ( 255, 0, 0 );
	}
	else if ( color.equalsIgnoreCase("white") ) {
		return new GRColor ( 255, 255, 255 );
	}
	else if ( color.equalsIgnoreCase("yellow") ) {
		return new GRColor ( 255, 255, 0 );
	}
	try {	// Try base class method to decode hex into an integer...
		Color c = decode ( color );
		GRColor grc = new GRColor (
		c.getRed(), c.getGreen(), c.getBlue() );
		c = null;
		return grc;
	}
	catch ( Exception e ) {
		//Message.printWarning ( 1, "", e );
		; // just return black
	}
	return new GRColor(0);
}

/**
Return the RGB integer value for a named color (00RRGGBB).  If the color
cannot be matched, the integer version of the color is returned (e.g., if the
String is "0x000000ff", then 255 will be returned.  If no conversion can be
made, then zero (black) is returned.
@param color Name of color (see COLOR_NAMES).
@return integer value corresponding to the color or -1 if not found as a
named color.
*/
public static int toInteger ( String color )
{	if ( color.equalsIgnoreCase("black") ) {
		return 0x00000000;
	}
	else if ( color.equalsIgnoreCase("blue") ) {
		return 0x000000ff;
	}
	else if ( color.equalsIgnoreCase("cyan") ) {
		return 0x0000ffff;
	}
	else if ( color.equalsIgnoreCase("darkgray") ||
		color.equalsIgnoreCase("darkgrey") ) {
		return 0x003f3f3f;
	}
	else if ( color.equalsIgnoreCase("gray") ||
		color.equalsIgnoreCase("grey") ) {
		return 0x007f7f7f;
	}
	else if ( color.equalsIgnoreCase("green") ) {
		return 0x0000ff00;
	}
	else if ( color.equalsIgnoreCase("lightgray") ||
		color.equalsIgnoreCase("lightgrey") ) {
		return 0x00bebebe;
	}
	else if ( color.equalsIgnoreCase("magenta") ) {
		return 0x00ff00ff;
	}
	else if ( color.equalsIgnoreCase("none") ) {
		return -1;
	}
	else if ( color.equalsIgnoreCase("orange") ) {
		return 0x00ff7f00;
	}
	else if ( color.equalsIgnoreCase("pink") ) {
		return 0x00ff7f7f;
	}
	else if ( color.equalsIgnoreCase("red") ) {
		return 0x00ff0000;
	}
	else if ( color.equalsIgnoreCase("white") ) {
		return 0x00ffffff;
	}
	else if ( color.equalsIgnoreCase("yellow") ) {
		return 0x00ffff00;
	}
	return StringUtil.atoi ( color );
}

/**
Return the named String value matching an integer RGB color.  If a named color
cannot be determined, then the integer value is returned as a string.
@param color Color as integer.
@return String value corresponding to the color or null if not found as a
named color.
*/
public static String toString ( int color )
{	if ( color == 0x00000000 ) {
		return "Black";
	}
	else if ( color == 0x000000ff ) {
		return "Blue";
	}
	else if ( color == 0x0000ffff ) {
		return "Cyan";
	}
	else if ( color == 0x003f3f3f ) {
		return "DarkGray";
	}
	else if ( color == 0x007f7f7f ) {
		return "Gray";
	}
	else if ( color == 0x0000ff00 ) {
		return "Green";
	}
	else if ( color == 0x00bebebe ) {
		return "LightGray";
	}
	else if ( color == 0x00ff00ff ) {
		return "Magenta";
	}
	else if ( color == -1 ) {
		return "None";
	}
	else if ( color == 0x00ff7f00 ) {
		return "Orange";
	}
	else if ( color == 0x00ff7f7f ) {
		return "Pink";
	}
	else if ( color == 0x00ff0000 ) {
		return "Red";
	}
	else if ( color == 0x00ffffff ) {
		return "White";
	}
	else if ( color == 0x00ffff00 ) {
		return "Yellow";
	}
	return "" + color;
}

/**
Return string value.
@return String representation of color.
*/
public String toString ()
{	return toString ( getRGB() );
}

} // End of GRColor class
