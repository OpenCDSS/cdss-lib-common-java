// XMRGViewerTableModel - a table model for displaying XMRG information.

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
// XMRGViewerTableModel - a table model for displaying XMRG information.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-10-14	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import RTi.GIS.GeoView.GeoGrid;
import RTi.GIS.GeoView.XmrgGridLayer;

import RTi.GR.GRLimits;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

import RTi.Util.Message.Message;

@SuppressWarnings("serial")
public class XMRGViewerTableModel extends JWorksheet_AbstractRowTableModel {

/**
The grid from the XMRG that contains the data values.
*/
private GeoGrid __grid = null;

/**
Number of columns in the table model.
*/
private int __numColumns = 0;

/**
The layer for which data will be displayed in the table model.
*/
private XmrgGridLayer __xmrg = null;

/**
Constructor.
@param xmrg the xmrg grid layer for which to show data in the table.
@throws Exception if the layer is null.
*/
public XMRGViewerTableModel(XmrgGridLayer xmrg) 
throws Exception {
	if (xmrg == null) {
		throw new Exception("Null XmrgGridLayer passed to "
			+ "XMRGViewerTableModel constructor.");
	}

	__xmrg = xmrg;
	__grid = __xmrg.getGrid();
	
	_rows = __grid.getNumberOfRows();
	__numColumns = __grid.getNumberOfColumns();

	//dumpXmrg(xmrg);
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given
column.
@param columnIndex the column for which to return the data class.
*/
public Class getColumnClass (int columnIndex) {
	return Double.class;
}

/**
From AbstractTableMode.  Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __numColumns;
}

/**
From AbstractTableMode.  Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	return "" + (__grid.getMinColumn() + columnIndex);
}


/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the
column.
*/
public String getFormat(int column) {
	return "%10.3f";
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable
at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	try {
		return new Double(__grid.getAbsDataValue(col, 
			(_rows - 1) - row));	
	}
	catch (Exception e) {
		return new Double(-999.99);
	}
}

/**
Being used for testing.
*/
public void dumpXmrg(XmrgGridLayer xmrg) {
	if (Message.isDebugOn) {
		Message.printStatus(1, "", "Dump of Xmrg file information ...");
	
		GRLimits limits = xmrg.getLimits();
	
		Message.printStatus(1, "", "Limits: " + limits);
		double max = Double.MIN_VALUE;
		int maxX = -999;
		int maxY = -999;
		double min = Double.MAX_VALUE;
		int minX = -999;
		int minY = -999;
		try {
			double d = 0;
			for (int i = (int)limits.getLeftX(); 
				i < (int)limits.getRightX(); i++) {
				for (int j = (int)limits.getBottomY(); 
					j < (int)limits.getTopY(); j++) {
					d = xmrg.getDataValue(i, j);
					if (d > max) {
						max = d;
						maxX = i;
						maxY = j;
					}
					if (d < min) {
						min = d;
						minX = i;
						minY = j;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
		Message.printStatus(1, "", "Min value: " + min 
			+ " at " + minX + ", " + minY);
		Message.printStatus(1, "", "Max value: " + max
			+ " at " + maxX + ", " + maxY);
		Message.printStatus(1, "", "Max value header: " + 
			xmrg.getMaxValueHeader());
		Message.printStatus(1, "", "Saved date: " 
			+ xmrg.getSavedDate());
		Message.printStatus(1, "", "Valid date: " 
			+ xmrg.getValidDate());
		Message.printStatus(1, "", "Operating system: '" 
			+ xmrg.getOperSys() + "'");
		Message.printStatus(1, "", "Big Endian? " + xmrg.isBigEndian());
		Message.printStatus(1, "", "Process flag: '" 
			+ xmrg.getProcessFlag()	+ "'");
		Message.printStatus(1, "", "User ID: '" + xmrg.getUserID() 
			+ "'");
		Message.printStatus(1, "", "Version: " + xmrg.getVersion());
	}
}

}
