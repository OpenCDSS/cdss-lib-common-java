// JWorksheet_HeaderCellRenderer - class to use as the cell renderer for the header of the JWorksheet

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

// TODO (JTS - 2004-01-20) needs renamed to JWorksheet_ColumnHeaderCellRenderer
// need to be able to center text in the column header

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.io.StringReader;
import java.io.BufferedReader;

import java.util.Vector;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.LineBorder;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
Class to use as cell renderer for the header of the JWorksheet, in order to be able to set the header fonts.
*/
@SuppressWarnings("serial")
public class JWorksheet_HeaderCellRenderer
extends DefaultTableCellRenderer {

/**
Whether the header can be rendered as a multiple-line header.
*/
private boolean __multiline = false;

/**
The background color in which to display the header.
*/
private Color __color;

/**
The Font in which to display the header.
*/
private Font __headerFont;

/**
The size of the font in which to display the header.
*/
private int __fontSize;

/**
The style of the font in which to display the header.
*/
private int __fontStyle;

/**
The name of the font to display header text in.
*/
private String __fontName = null;

/**
The justification to apply to the header text.
*/
private int __justification = SwingConstants.CENTER;

/**
Constructor.  Builds a default renderer with Arial 12-point plain as the font, and justification of CENTER.
*/
public JWorksheet_HeaderCellRenderer() {
	__fontName = "Arial";
	__fontStyle = Font.PLAIN;
	__fontSize = 11;
	__justification = SwingConstants.CENTER;
	JTableHeader header = new JTableHeader();
	__color = (Color)(header.getClientProperty("TableHeader.background"));
	__headerFont = new Font(__fontName, __fontStyle, __fontSize);
}

/**
Constructor.
Builds a renderer for the header with the given font and the given header text justification (as defined in SwingConstants).
@param fontName the name of the font for the header (e.g., "Courer")
@param fontStyle the style of the header's font (e.g., Font.PLAIN)
@param fontSize the size of the header font (e.g, 11)
@param justification the justification (CENTER, RIGHT,
or LEFT) in which to display the header text.  (e.g., SwingConstants.CENTER)
@param color the color for the header font (e.g., Color.LIGHT_GRAY)
*/
public JWorksheet_HeaderCellRenderer(String fontName, int fontStyle, int fontSize, int justification, Color color) {
	__fontName = fontName;
	__fontStyle = fontStyle;
	__fontSize = fontSize;
	__justification = justification;
	if (color == null) {
		JTableHeader header = new JTableHeader();
		__color = (Color)(header.getClientProperty( "TableHeader.background"));
	}
	else {
		__color = color;
	}
	__headerFont = new Font(__fontName, __fontStyle, __fontSize);
}

/**
Returns the font in which the header cells should be rendered.
@return the font in which the header cells should be rendered.
*/
public Font getFont() {
	//return new Font(__fontName, __fontStyle, __fontSize);
	return __headerFont;
}

/**
Renders a value for a cell in a JTable.
This method is called automatically by the JTable when it is rendering its cells.
This overrides the method in DefaultTableCellRenderer.
@param table the JTable (in this case, JWorksheet) in which the cell to be rendered will appear.
@param value the cell's value to be rendered.
@param isSelected whether the cell is selected or not.
@param hasFocus whether the cell has focus or not.
@param row the row in which the cell appears.
@param column the column in which the cell appears.
@return a properly-rendered cell that can be placed in the table.
*/
public Component getTableCellRendererComponent(JTable table, Object value,
boolean isSelected, boolean hasFocus, int row, int column) {
	String str = "";
 	if (value != null) {
		str = value.toString();
	}

	if (str.length() == 0) {
		str = " ";
	}

	// Call DefaultTableCellRenderer's version of this method so that all the cell highlighting is handled properly.
	if (!__multiline && table != null) {
		super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
		setHorizontalAlignment(__justification);
		setFont(new Font(__fontName, __fontStyle, __fontSize));
		setBackground(__color);
		setBorder(new LineBorder(Color.darkGray));
		return this;
	}
	else {
		JList<String> list = new JList<String>();
		BufferedReader br = new BufferedReader(new StringReader(str));
		Vector<String> v = new Vector<>();
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				if (line.equals("")) {
					line = "      ";
				}
				v.add(line);
				line = br.readLine();
			}
		}
		catch (Exception e) {}

		list.setFont(new Font(__fontName, __fontStyle, __fontSize));
		list.setOpaque(true);
		list.setForeground(UIManager.getColor( "TableHeader.foreground"));
		list.setBackground(__color);
		list.setBorder(new LineBorder(Color.darkGray));

		list.setListData(v);

		return list;
	}
}

/**
Sets the font in which the header cells should be rendered.
@param fontName the name of the font to display the header in
@param fontStyle the style to display the header font in
@param fontSize the size to display the header font in
*/
public void setFont(String fontName, int fontStyle, int fontSize) {
	__fontName = fontName;
	__fontStyle = fontStyle;
	__fontSize = fontSize;
}

/**
Sets whether the header should render the header as multiple lines, one above the other.
@param multiline whether to render on more than one line.
*/
public void setMultiLine(boolean multiline) {
	__multiline = multiline;
}

}