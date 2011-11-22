// ----------------------------------------------------------------------------
// Generic_CellRenderer - class to render cells for a generic worksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-12-10	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render cells for a generic worksheet.
<b>For information on how to build a worksheet that uses
generic data, see the documentation for Generic_TableModel</b>
*/
public class Generic_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
Table model for which this class renders the cell.
*/
private Generic_TableModel __tableModel;

/**
Private constructor.  Can't use this.
*/
@SuppressWarnings("unused")
private Generic_CellRenderer() {}

/**
Constructor.  
@param model the model for which this class will render cells
*/
public Generic_CellRenderer(Generic_TableModel model) {
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
Returns the format for a given column.
@param column the colum for which to return the format.
@return the format (as used by StringUtil.format) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

}
