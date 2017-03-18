// ----------------------------------------------------------------------------
// TS_List_CellRenderer - class to render cells for a list of time series in
//			the TS_List_TableModel
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-29	Steven A. Malers, RTi	Initial version.  Copy and modify the
//					version used with TSTool.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.TS;

import RTi.Util.GUI.JWorksheet_DefaultTableCellRenderer;

/**
This class is used to render cells for TS_List_TableModel data.
*/
@SuppressWarnings("serial")
public class TS_List_CellRenderer extends JWorksheet_DefaultTableCellRenderer{

TS_List_TableModel __table_model = null;	// Table model to render

/**
Constructor.
@param table_model The TS_List_TableModel to render.
*/
public TS_List_CellRenderer ( TS_List_TableModel table_model )
{	__table_model = table_model;
}

/**
Returns the format for a given column.
@param column the colum for which to return the format.
@return the column format as used by StringUtil.formatString().
*/
public String getFormat(int column) {
	return __table_model.getFormat(column);	
}

/**
Returns the widths of the columns in the table.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __table_model.getColumnWidths();
}

} // End TS_List_CellRenderer
