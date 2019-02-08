// DataStores_CellRenderer - cell renderer to display list of DataStores

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

package riverside.datastore;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_CellAttributes;
import RTi.Util.String.StringUtil;

/**
This class is a cell renderer for cells in DataStores JWorksheets.
*/
public class DataStores_CellRenderer extends JWorksheet_AbstractExcelCellRenderer
{

/**
The table model for which this class will render cells.
*/
private DataStores_TableModel __tableModel;

/**
Constructor.
@param tableModel the tableModel for which to render cells.
*/
public DataStores_CellRenderer(DataStores_TableModel tableModel) {
	__tableModel = tableModel;
}

/**
Renders a cell for the worksheet.
@param table the JWorksheet for which a cell will be renderer.
@param value the value in the cell.
@param isSelected whether the cell is selected or not.
@param hasFocus whether the cell has the input focus or not.
@param row the row in the worksheet where the cell is located.
@param column the column in the worksheet where the cell is located.
@return the rendered cell.
*/
public Component getTableCellRendererComponent(JTable table, Object value,
boolean isSelected, boolean hasFocus, int row, int column) {
	String str = "";
 	if (value != null) {
		str = value.toString();
	}

 	JWorksheet ws = (JWorksheet)table;
	int abscolumn = ws.getAbsoluteColumn(column);
	
	String format = getFormat(abscolumn);
	
	int justification = SwingConstants.LEFT;

	if (value instanceof Integer) {
		justification = SwingConstants.RIGHT;
		str = StringUtil.formatString(value, format);
	}	
	else if (value instanceof Double) {		
		justification = SwingConstants.RIGHT;
		str = StringUtil.formatString(value, format);
	}
	else if (value instanceof Date) {
		justification = SwingConstants.LEFT;		
		// FYI: str has been set above with str = value.toString()
	}
	else if (value instanceof String) {
		justification = SwingConstants.LEFT;
		str = StringUtil.formatString(value, format);
	}
	else if (value instanceof Float) {
		justification = SwingConstants.RIGHT;
		str = StringUtil.formatString(value, format);
	}
	else {
		justification = SwingConstants.LEFT;
	}

	str = str.trim();

	// call DefaultTableCellRenderer's version of this method so that
	// all the cell highlighting is handled properly.
	super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);	
	
	int tableAlignment = ws.getColumnAlignment(abscolumn);
	if (tableAlignment != JWorksheet.DEFAULT) {
		justification = tableAlignment;
	}
	
	setHorizontalAlignment(justification);
	setFont(ws.getCellFont());
	
	if ( column == __tableModel.COL_NAME ) {
		// Set the foreground to yellow if the column is the datastore name and is a duplicate
		// This is brute force but there are not that many datastores so OK
		// Yellow is easier to read as background.
		boolean duplicate = false;
		for ( int irow = 0; irow < __tableModel.getRowCount(); irow++ ) {
			if ( irow == row ) {
				continue;
			}
			if ( str.equalsIgnoreCase(""+__tableModel.getValueAt(irow, column)) ) {
				duplicate = true;
				break;
			}
		}
		if ( duplicate ) {
			JWorksheet_CellAttributes ca = ws.getCellAttributes(row, column);
			if ( ca == null ) {
				ca = new JWorksheet_CellAttributes();
			}
			ca.backgroundColor = Color.yellow;
			ws.setCellAttributes(row, column, ca);
		}
	}
	else if ( column == __tableModel.COL_STATUS ) {
		if ( str.toUpperCase().indexOf("ERROR") >= 0 ) {
			// Some type of error so highlight
			JWorksheet_CellAttributes ca = ws.getCellAttributes(row, column);
			if ( ca == null ) {
				ca = new JWorksheet_CellAttributes();
			}
			ca.backgroundColor = Color.yellow;
			ws.setCellAttributes(row, column, ca);
		}
	}
	else if ( column == __tableModel.COL_STATUS_MESSAGE ) {
		if ( !str.isEmpty() ) {
			// Some type of error so highlight
			JWorksheet_CellAttributes ca = ws.getCellAttributes(row, column);
			if ( ca == null ) {
				ca = new JWorksheet_CellAttributes();
			}
			ca.backgroundColor = Color.yellow;
			ws.setCellAttributes(row, column, ca);
		}
	}

	return this;
}

/**
Returns the data format for the given column.
@param column the column for which to return the data format.
@return the data format for the given column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Returns the widths the columns should be set to.
@return the widths the columns should be set to.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

}
