// XMRGViewerCellRenderer - cell renderer for XMRGViewer tables.

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

package RTi.GIS.GeoView;

import java.awt.Component;

import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import RTi.DMI.DMIUtil;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

import RTi.Util.String.StringUtil;

/**
This class is the class from which other Cell Renderers for HydroBase should be built.
*/
@SuppressWarnings("serial")
public class XMRGViewerCellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class will render cells.
*/
private XMRGViewerTableModel __tableModel = null;

/**
Constructor.
@param tableModel the table model for which to render cells.
*/
public XMRGViewerCellRenderer(XMRGViewerTableModel tableModel) {	
	__tableModel = tableModel;
}

/**
Returns the format for a given column.
@param column the colum for which to return the format.
@return the format (as used by StringUtil.format) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Renders a value for a cell in a JTable.  This method is called automatically
by the JTable when it is rendering its cells.  This overrides some code from DefaultTableCellRenderer.
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
	
	int abscolumn = ((JWorksheet)table).getAbsoluteColumn(column);
	
	String format = getFormat(abscolumn);
	
	int justification = SwingConstants.LEFT;

	if (value instanceof Integer) {
		if (DMIUtil.isMissing(((Integer)value).intValue())) {
			str = "";
		} 
		else {
			justification = SwingConstants.RIGHT;
			str = StringUtil.formatString(value, format);
		}
	}	
	else if (value instanceof Double) {		
		if (DMIUtil.isMissing(((Double)value).doubleValue())) {
			str = "";
		}	
		else {
			justification = SwingConstants.RIGHT;
			str = StringUtil.formatString(value, format);
		}
	}
	else if (value instanceof Date) {
		justification = SwingConstants.LEFT;		
		// FYI: str has been set above with str = value.toString()
	}
	else if (value instanceof String) {
		justification = SwingConstants.LEFT;
		str = StringUtil.formatString(value, format);
	}
	else {
		justification = SwingConstants.LEFT;
	}

	str = str.trim();

	// Call DefaultTableCellRenderer's version of this method so that all the cell highlighting is handled properly.
	super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);	

	int tableAlignment = ((JWorksheet)table).getColumnAlignment(abscolumn);
	if (tableAlignment != JWorksheet.DEFAULT) {
		justification = tableAlignment;
	}
		
	setHorizontalAlignment(justification);
	setFont(((JWorksheet)table).getCellFont());

	return this;
}

}