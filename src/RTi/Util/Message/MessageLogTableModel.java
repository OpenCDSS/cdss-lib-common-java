// MessageLogTableModel - this class is the table model for displaying a log file in a worksheet.

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
// MessageLogTableModel - this class is the table model for displaying a log 
//	file in a worksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2005-03-22	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.Message;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying log file data within a worksheet.  
The table contains a single column, each row of which is a line in a log file.
The worksheet text is all monospaced, and there is no header on the right side
for listing line numbers.
*/
@SuppressWarnings("serial")
public class MessageLogTableModel 
extends JWorksheet_AbstractRowTableModel<String> {

/**
Number of columns in the table model.
*/
private final static int __COLUMNS = 2;

/**
Reference to the column.
*/
public final static int 
	COL_MESSAGE =	1;

/**
Constructor.  
@param messages the messages that will be displayed in the table.  Each element
in the Vector is a line in the log file, and must be a String.
*/
public MessageLogTableModel(List<String> messages) {
	if (messages == null) {
		messages = new Vector<String>();
	}
	_rows = messages.size();
	_data = messages;
}

/**
Returns the class of the data stored in a given column.
@param columnIndex the column for which to return the data class.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case COL_MESSAGE:	return String.class;
		default:		return String.class;
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
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case COL_MESSAGE:	return "MESSAGE";
		default:		return " ";
	}	
}

/**
Returns an array containing the widths that the fields in the table should 
be sized to.  The widths roughly correspond to the number of characters wide
the column should be.
@return an integer array containing the widths for each column.
*/
public int[] getColumnWidths() {
	int[] widths = new int[__COLUMNS];
	for (int i = 0; i < __COLUMNS; i++) {
		widths[i] = 0;
	}
	widths[COL_MESSAGE] = 256;
	return widths;
}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format. 
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString()) in which to display 
the column.
*/
public String getFormat(int column) {
	switch (column) {
		case COL_MESSAGE:	return "%-256s";
		default:		return "%-8s";
	}
}

/**
Returns the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and column
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	return _data.get(row);
}

}