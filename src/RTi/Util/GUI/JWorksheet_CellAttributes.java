// ----------------------------------------------------------------------------
// JWorksheet_CellAttributes - Class for storing cell attributes for JWorksheet
//	cells.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-03	J. Thomas Sapienza, RTi	Initial version.
// 2003-10-07	JTS, RTi		Added the editable flag.
// 2003-10-20	JTS, RTi		Added toString().
// 2003-10-21	JTS, RTi		* Implemented Cloneable.
//					* Added copy constructor.
// 2003-11-18	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Font;

/**
This class stores attributes for formatting JWorksheet cells.  All member 
variables are public for speed's sake (this class could be reference many times
when rendering a JWorksheet).<p>
Variables that are not set will not affect a cell's rendering.  To disable
an attribute in a renderered cell, set it to <pre>null</pre> if it is an 
object or <pre>-1</pre> if it is an <pre>int</pre>.
*/
public class JWorksheet_CellAttributes 
implements Cloneable {

/**
Cell background color.
*/
public Color backgroundColor = null;

/**
Cell background color when selected.
*/
public Color backgroundSelectedColor = null;

/**
Cell border color.
*/
public Color borderColor = null;

/**
Whether the cell is editable or not.
*/
public boolean editable = true;

/**
Whether the cell attributes are enabled (true) or not.  By default the 
attributes are enabled, though they can be turned off for a cell by getting
the attributes for a certain cell and setting enabled to false;
*/
public boolean enabled = true;

/**
Cell foreground color.
*/
public Color foregroundColor = null;

/**
Cell foreground color when selected.
*/
public Color foregroundSelectedColor = null;

/**
Font in which to render the cell.
*/
public Font font = null;

/**
The name of the font to display in.  
If the font is not set from the <pre>font</pre> member variable this is 
the name of the font to use.
*/
public String fontName = null;

/**
The size of the font to display in.
If the font is not set from the <pre>font</pre> member variable this is 
the size of the font to use.
*/
public int fontSize = -1;

/**
The style of the font to display in.
If the font is not set from the <pre>font</pre> member variable this is 
the style of the font to use (Font.PLAIN, Font.BOLD, etc.).
*/
public int fontStyle = -1;

/**
The horizontal alignment of text in the cell, from SwingConstants.
*/
public int horizontalAlignment = -1;

/**
The vertical alignment of text in the cell, from SwingConstants.
*/
public int verticalAlignment = -1;

/**
Constructor.
*/
public JWorksheet_CellAttributes() {}

/**
Copy constructor.
@param ca the cell attributes to copy into this one.
*/
public JWorksheet_CellAttributes(JWorksheet_CellAttributes ca) {
	backgroundColor = 		ca.backgroundColor;
	backgroundSelectedColor = 	ca.backgroundSelectedColor;
	foregroundColor = 		ca.foregroundColor;
	foregroundSelectedColor = 	ca.foregroundSelectedColor;
	borderColor = 			ca.borderColor;
	font = 				ca.font;
	fontName = 			new String(ca.fontName);
	fontSize = 			ca.fontSize;
	fontStyle = 			ca.fontStyle;
	horizontalAlignment = 		ca.horizontalAlignment;
	verticalAlignment = 		ca.verticalAlignment;
	enabled = 			ca.enabled;
	editable = 			ca.editable;
}

/**
Clones the cell attributes.
*/
public Object clone() {
	try {
		super.clone();
	}
	catch (CloneNotSupportedException e) {}
	return this;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	backgroundColor = null;
	backgroundSelectedColor = null;
	borderColor = null;
	foregroundColor = null;
	foregroundSelectedColor = null;
	font = null;
	fontName = null;
	super.finalize();
}

/**
Returns a String representation of these attributes.
@return a String representation of these attributes.
*/
public String toString() {
	return	
		"backgroundColor:         " + backgroundColor + "\n" + 
		"backgroundSelectedColor: " + backgroundSelectedColor + "\n" +
		"foregroundColor:         " + foregroundColor + "\n" + 
		"foregroundSelectedColor: " + foregroundSelectedColor + "\n" + 
		"borderColor:             " + borderColor + "\n" + 
		"font:                    " + font + "\n" + 
		"fontName:                " + fontName + "\n" + 
		"fontSize:                " + fontSize + "\n" +
		"fontStyle:               " + fontStyle + "\n" +
		"horizontalAlignment:     " + horizontalAlignment + "\n" + 
		"verticalAlignment:       " + verticalAlignment + "\n" +
		"enabled:                 " + enabled + "\n" + 
		"editable:                " + editable + "\n";
}

}
