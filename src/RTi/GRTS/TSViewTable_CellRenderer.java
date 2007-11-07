// ----------------------------------------------------------------------------
// TSViewTable_CellRenderer - Class for rendering cells for TS tables.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-10	JTS, RTi		Initial version.
// ----------------------------------------------------------------------------

package RTi.GRTS;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render cells for TS view tables.
*/
public class TSViewTable_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which to render cells.
*/
private TSViewTable_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which to render cells
*/
public TSViewTable_CellRenderer(TSViewTable_TableModel tableModel) {
	__tableModel = tableModel;
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
