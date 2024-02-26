// JWorksheet_CellAttributes - class for storing cell attributes for JWorksheet cells

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Font;

/**
This class stores attributes for formatting JWorksheet cells.
All member variables are public for speed's sake (this class could be reference many times when rendering a JWorksheet).<p>
Variables that are not set will not affect a cell's rendering.
To disable an attribute in a rendered cell,
set it to <pre>null</pre> if it is an object or <pre>-1</pre> if it is an <pre>int</pre>.
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
Whether the cell attributes are enabled (true) or not.
By default the attributes are enabled,
though they can be turned off for a cell by getting the attributes for a certain cell and setting enabled to false.
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
If the font is not set from the <pre>font</pre> member variable this is the name of the font to use.
*/
public String fontName = null;

/**
The size of the font to display in.
If the font is not set from the <pre>font</pre> member variable this is the size of the font to use.
*/
public int fontSize = -1;

/**
The style of the font to display in.
If the font is not set from the <pre>font</pre> member variable,
this is the style of the font to use (Font.PLAIN, Font.BOLD, etc.).
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
public JWorksheet_CellAttributes() {
}

/**
Copy constructor.
@param ca the cell attributes to copy into this one.
*/
public JWorksheet_CellAttributes ( JWorksheet_CellAttributes ca ) {
	backgroundColor = ca.backgroundColor;
	backgroundSelectedColor = ca.backgroundSelectedColor;
	foregroundColor = ca.foregroundColor;
	foregroundSelectedColor = ca.foregroundSelectedColor;
	borderColor = ca.borderColor;
	font = ca.font;
	fontName = new String(ca.fontName);
	fontSize = ca.fontSize;
	fontStyle = ca.fontStyle;
	horizontalAlignment = ca.horizontalAlignment;
	verticalAlignment = ca.verticalAlignment;
	enabled = ca.enabled;
	editable = ca.editable;
}

/**
Clones the cell attributes.
*/
public Object clone() {
	try {
		super.clone();
	}
	catch ( CloneNotSupportedException e ) {
	}
	return this;
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