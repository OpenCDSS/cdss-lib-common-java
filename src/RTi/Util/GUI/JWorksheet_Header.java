// ----------------------------------------------------------------------------
// JWorksheet_Header - The header for a JWorksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-07-01	J. Thomas Sapienza, RTi	Initial version.
// 2003-11-18	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.event.MouseEvent;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import RTi.Util.IO.IOUtil;

/**
This class is the header that a JWorksheet uses.  It provides additional 
functionality that normal headers do not, such as the ability to set tooltips
on columns.
*/
public class JWorksheet_Header 
extends JTableHeader {

/**
The number of columns in the table model.
*/
private int __numColumns;

/**
An array of tooltips, one per absolute column.  If a column has a 
<code>null</code> tooltip, no tooltip will be shown for that column.
*/
private String[] __tooltips;

/**
Constructor.
*/
public JWorksheet_Header() {
	super();
}

/**
Constructor. 
@param tcm the TableColumnModel for which this will be the header.
*/
public JWorksheet_Header(TableColumnModel tcm) {
	super(tcm);

	initialize();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__tooltips);
	super.finalize();
}

/**
Returns the tooltip text for a column.
@param e the MouseEvent that caused a tooltip to be shown.
*/
public String getToolTipText(MouseEvent e) {
	int col = columnAtPoint(e.getPoint());

	if (getTable() == null) {
		return null;
	}
	
	if (col < 0 || col >= getTable().getColumnCount()) {
		return "";
	}

	col = ((JWorksheet)getTable()).getAbsoluteColumn(col);
	
	String s = __tooltips[col];
	return s;
}

/**
Initializes settings and the tooltip array.
*/
private void initialize() {
	__numColumns = getColumnModel().getColumnCount();

	__tooltips = new String[__numColumns];

	for (int i = 0; i < __numColumns; i++) {
		__tooltips[i] = null;
	}
}

/**
Sets a tool tip for a column.
@param column the column for which to set a tooltip.
@param tip the tooltip to set for the column.
*/
public void setColumnToolTip(int column, String tip) {
	if (column < 0 || column > __numColumns) {
		return;
	}

	__tooltips[column] = tip;
}

/**
Sets the column model to use with the header.
@param tcm the column model to use with the header.
*/
public void setColumnModel(TableColumnModel tcm) {
	super.setColumnModel(tcm);
	initialize();
}

}
