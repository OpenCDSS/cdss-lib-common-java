// CommandLog_TableModel - table model that displays a list of CommandLogRecord in a worksheet

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

package RTi.Util.IO;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This table model displays a list of CommandLogRecord in a worksheet.
*/
@SuppressWarnings("serial")
public class CommandLog_TableModel extends JWorksheet_AbstractRowTableModel<CommandLogRecord> {

/**
Number of columns in the table model.
*/
private final int __COLUMNS = 5;

/**
References to columns.
*/
public final static int
	COL_SEVERITY = 0,
	COL_TYPE = 1,
	COL_COMMAND = 2,
	COL_PROBLEM = 3,
	COL_RECOMMENDATION = 4;
	// TODO SAM 2009-02-27 Add reference to data object that caused problem
	// COL_DATA;

/**
Constructor.  
@param data the data that will be displayed in the table.
@throws Exception if an invalid data was passed in.
*/
public CommandLog_TableModel(List<CommandLogRecord> data)
throws Exception {
	if (data == null) {
		throw new Exception ("Invalid data list passed to " + getClass().getName() + " constructor.");
	}
	_rows = data.size();
	_data = data;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_SEVERITY:
			return String.class;//CommandStatusType.class;
		case COL_TYPE:
			return String.class;// from class name
		case COL_COMMAND:
			return String.class;//CommandStatusProvider.class;
		case COL_PROBLEM:
			return String.class;
		case COL_RECOMMENDATION:
			return String.class;
		default:
			return String.class;
	}
}

/**
Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __COLUMNS;
}

/**
Returns the name of the column at the given position.
@param columnIndex the position of the column for which to return the name.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_SEVERITY:
			return "Severity";
		case COL_TYPE:
			return "Type";
		case COL_COMMAND:
			return "Command";
		case COL_PROBLEM:
			return "Problem";
		case COL_RECOMMENDATION:
			return "Recommendation";
		default:
			return " ";
	}	
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public String[] getColumnToolTips() {
    String[] tips = new String[__COLUMNS];
    tips[COL_SEVERITY] = "Severity of problem, WARNING and FAILURE typically need to be resolved.";
    tips[COL_TYPE] = "Type of problem.";
    tips[COL_COMMAND] = "Command associated with problem.";
    tips[COL_PROBLEM] = "Problem description.";
    tips[COL_RECOMMENDATION] = "Recommendation for resolving problem.";
    return tips;
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths()
{
	int[] widths = new int[__COLUMNS];
	widths[COL_SEVERITY] = 6;
	widths[COL_TYPE] = 15;
	widths[COL_COMMAND] = 23;
	widths[COL_PROBLEM] = 23;
	widths[COL_RECOMMENDATION] = 23;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_SEVERITY:
			return "%-8s";
		case COL_TYPE:
			return "%-s";
		case COL_COMMAND:
			return "%-40s";
		case COL_PROBLEM:
			return "%-40s";
		case COL_RECOMMENDATION:
			return "%-40s";
		default:
			return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	CommandLogRecord log = _data.get(row);

	switch (col) {
		case COL_SEVERITY:
			return log.getSeverity().toString();
		case COL_TYPE:
			return CommandStatusUtil.getCommandLogRecordDisplayName(log);
		case COL_COMMAND:
			CommandStatusProvider csp = log.getCommandStatusProvider();
			if ( csp == null ) {
				return "";
			}
			else {
				return csp.toString();
			}
		case COL_PROBLEM:
			return log.getProblem();
		case COL_RECOMMENDATION:
			return log.getRecommendation();
		default:
			return "";
	}
}

/**
Returns whether the cell at the given position is editable or not.  In this
table model all columns are not editable.
@param rowIndex unused
@param columnIndex the index of the column to check for whether it is editable.
@return whether the cell at the given position is editable.
*/
public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
}

/**
Sets the value at the specified position to the specified value.
@param value the value to set the cell to.
@param row the row of the cell for which to set the value.
@param col the col of the cell for which to set the value.
*/
public void setValueAt(Object value, int row, int col) {
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}

	CommandLogRecord log = (CommandLogRecord)_data.get(row);

	switch (col) {
		case COL_SEVERITY:	
			// Not editable and currently no way to set...log.setSeverity((CommandStatusType)value);
			break;
		case COL_TYPE:
			// Not editable and currently no way to set
			break;
		case COL_COMMAND:
			// Not editable and currently no way to set...log.setCommand((Command)value);
			break;
		case COL_PROBLEM:
			log.setProblem((String)value);
			break;
		case COL_RECOMMENDATION:
			log.setRecommendation((String)value);
			break;
	}
	
	super.setValueAt(value, row, col);
}

}
