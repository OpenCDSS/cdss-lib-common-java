// ----------------------------------------------------------------------------
// JWorksheet_RowHeader - class to create a worksheet header that tracks 
//	row numbers.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-01-20	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JList;
import javax.swing.UIManager;

import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;

import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
This class is a header for worksheets that goes on the left side of the 
worksheet and tracks the row number for each row.
*/
public class JWorksheet_RowHeader 
extends JLabel 
implements ListCellRenderer {

/**
Constructor.
@param worksheet the worksheet in which the header will appear.
@param font the Font to use for text in the header.
@param backgroundColor the color to set the scrolled area background to.
*/
public JWorksheet_RowHeader(JWorksheet worksheet, Font font, 
Color backgroundColor) {
	JTableHeader header = worksheet.getTableHeader();
	setOpaque(true);
	setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	setBorder(new LineBorder(Color.darkGray));	
	setHorizontalAlignment(CENTER);
	setForeground(header.getForeground());
	setBackground(header.getBackground());
	setFont(font);
}

/**
Constructor.
@param worksheet the worksheet in which the header appears.
*/
public JWorksheet_RowHeader(JWorksheet worksheet) {
	JTableHeader header = worksheet.getTableHeader();
	setOpaque(true);
	setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	setBorder(new LineBorder(Color.darkGray));	
	setHorizontalAlignment(CENTER);
	setForeground(header.getForeground());
	setBackground(header.getBackground());
	setFont(new Font("Arial", 12, Font.BOLD));
}

/**
Returns the rendered value for a given row.  
@param list ignored.
@param value the value to put in the given row's header.
@param index the row number for which to render the header cell.
@param isSelected ignored.
@param cellhasFocus ignored.
*/
public Component getListCellRendererComponent(JList list, Object value, 
int index, boolean isSelected, boolean cellHasFocus) {
	setText((value == null) ? "" : value.toString());
	return this;
}

}
