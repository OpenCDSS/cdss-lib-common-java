// MessageLogCellRenderer - the renderer for the worksheet that displays a log file.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// MessageLogCellRenderer - the renderer for the worksheet that displays
//	a log file.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-03-22	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
// 2005-05-23	JTS, RTi		Added getTableCellRendererComponent()
//					and modified it from the base class
//					version so that it only supports 
//					Strings and also doesn't trim Strings.
// ----------------------------------------------------------------------------

package RTi.Util.Message;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import RTi.Util.String.StringUtil;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is the cell renderer for a worksheet that displays a log file.
*/
@SuppressWarnings("serial")
public class MessageLogCellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders the cell.
*/
private MessageLogTableModel __tableModel;

/**
Constructor.  
@param model the model for which this class will render cells.
*/
public MessageLogCellRenderer(MessageLogTableModel model) {
	__tableModel = model;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__tableModel = null;
	super.finalize();
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

/**
Returns the format for a given column.
@param column the column for which to return the format (0-based).
@return the format (as used by StringUtil.formatString()) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Renders a value for a cell in a JTable.  This method is called automatically
by the JTable when it is rendering its cells.  This overrides some code from
DefaultTableCellRenderer.
@param table the JTable (in this case, JWorksheet) in which the cell
to be rendered will appear.
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
	
	int abscolumn = ((JWorksheet)table).getAbsoluteColumn(column);
	
	String format = getFormat(abscolumn);
	
	int justification = SwingConstants.LEFT;
	str = StringUtil.formatString(value, format);

	// call DefaultTableCellRenderer's version of this method so that
	// all the cell highlighting is handled properly.
	super.getTableCellRendererComponent(table, str, 
		isSelected, hasFocus, row, column);	
	
	int tableAlignment = ((JWorksheet)table).getColumnAlignment(abscolumn);
	if (tableAlignment != JWorksheet.DEFAULT) {
		justification = tableAlignment;
	}
	
	setHorizontalAlignment(justification);
	setFont(((JWorksheet)table).getCellFont());

	return this;
}

}
