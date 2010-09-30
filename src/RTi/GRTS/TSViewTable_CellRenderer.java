package RTi.GRTS;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.String.StringUtil;

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
@param column the column for which to return the format.
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

/**
Renders a value for a cell in a JTable.  This method is called automatically
by the JTable when it is rendering its cells.  This overrides some code from DefaultTableCellRenderer.
It handles the justification, which is important with numerical values.
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

	if ( value instanceof Double ) {
		// Time series data value
		// TODO SAM 2010-07-15 If necessary add a method in the table model if the display
		// becomes more complicated with data flags, etc.
		// Currently column 0 is the date/time and columns 1+ are time series.
		if (__tableModel.getTS(column - 1).isDataMissing(((Double)value).doubleValue())) {
			str = "";
		}	
		else {
			justification = SwingConstants.RIGHT;
			str = StringUtil.formatString(value, format);
		}
	}
	else if (value instanceof String) {
		// Date/times are formatted as strings.
		justification = SwingConstants.LEFT;
		str = StringUtil.formatString(value, format);
	}
	else {
		justification = SwingConstants.LEFT;
	}

	str = str.trim();

	// call DefaultTableCellRenderer's version of this method so that
	// all the cell highlighting is handled properly.
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