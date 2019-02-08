// ERDiagram_Table_TableModel - table model for displaying ERDiagram_Table information

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
// ERDiagram_Table_TableModel - Table model for displaying ERDiagram_Table
//	information.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-09-03	J. Thomas Sapienza, RTi	Initial version.
// 2005-03-23	JTS, RTi		* Removed the row count (0th) column.
//					* Added column variables.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
This class is a table model for displaying data from ERDiagram_Table objects.
*/
@SuppressWarnings("serial")
public class ERDiagram_Table_TableModel 
extends JWorksheet_AbstractRowTableModel {

/**
The number of columns in the table model. 

AML:
This should be changed to represent the number of columns you will have in
your tables.  Here's something, though:
- the columns count should be +1 the actual number of columns your table will
have, because the 0th column (the left-most one) actually contains the
row number in it. You can turn this column off if you want, but you still
need to provide room for it in the table model
*/
private int __columns = 3;

/**
Column reference variables.
*/
private final int
	__COL_NAME = 0,
	__COL_X = 1,
	__COL_Y = 2;

/**
Constructor.  This builds the Model for displaying the given ERDiagram_Table objects' information.
@param data list of ERDiagram_Table objects for which to display data.
@throws Exception if an invalid data or dmi was passed in.
*/
public ERDiagram_Table_TableModel(List data) 
throws Exception {
	if (data == null) {
		throw new Exception ("Null data Vector passed to " 
			+ "ERDiagram_Table_TableModel constructor.");
	}	

	// AML: _data is a Vector inherited from 
	// JWorksheet_AbstractRowTableModel.  It stores the Vector of data
	// that you'll show in the table.
	_data = data;

	// AML: _rows is an integer that contains the number of rows of
	// data to be displayed in the table.  Make sure to set the 
	// value 
	_rows = _data.size();
}

/**
Returns the class of the data stored in a given column.  This method is 
inherited from the very base-most java TableModel class and is required.
@param columnIndex the column for which to return the data class.

AML: this method is used so that the class that renders table cells knows
what kind of data type it is dealing with.  what we use it for is in the
cell renderer base class, so that we know if a cell should be right-justified
(numbers) or left-justified (strings and dates). 

Also, make sure the class type returned from this method matches the kind of
object you return in the call to getValueAt(), otherwise an exception will get thrown.
*/
public Class<?> getColumnClass (int columnIndex) {
	switch (columnIndex) {
		case __COL_NAME: 	return String.class;
		case __COL_X: 		return Double.class;
		case __COL_Y:	 	return Double.class;
		default: 		return String.class;
	}
}

/**
Returns the number of columns of data.  This method is inherited from
the very base-most Java TableModel class and is required.
@return the number of columns of data.
*/
public int getColumnCount() {
	return __columns;
}

/**
Returns the name of the column at the given position.  This method is inherited
from the very base-most Java TableModel class and is required.
@return the name of the column at the given position.

AML: steve decided that the 0th column (the one that holds the row number)
shouldn't have a column name, so that's what I've been doing on other tables.
But you can put "ROW #" or whatever you want in there.

If you ever try to change a table name on the fly (say you start displaying a 
table, then a user changes a setting and you want to change a column name
from one value to another), it's not as easy as simply returning a new value
in this method.  I know I've dealt with it before, and it's a pain in the butt. 
It's easier to just rebuild the table entirely ... but you only need to worry
about this if you ever need to do that.
*/
public String getColumnName(int columnIndex) {
	switch (columnIndex) {
		case __COL_NAME:	return "Table Name";
		case __COL_X:		return "X";
		case __COL_Y:		return "Y";
		default:		return "";
		
	}

}

/**
Returns the format that the specified column should be displayed in when
the table is being displayed in the given table format.  This method is
required for JWorksheet table models.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString() in which to display the column.

AML:
you'll see that in all these methods that use a switch() statement to switch
between columns that I have a 'default:' tag.  Really, strictly-speaking,
this isn't completely necessary, since if you set the __columns variable 
correctly up top then the switch() will never be out of bounds ... but it's just a good idea ... 
*/
public String getFormat(int column) {
	switch (column) {
		case __COL_NAME:	return "%-20s";	
		case __COL_X:		return "%12.4f";
		case __COL_Y:		return "%12.4f";
		default:		return "%1s";
	}
}

/**
Returns the number of rows of data in the table.  This method is inherited from
the very base-most Java TableModel class and is required.
@return the number of rows of data in the table.
*/
public int getRowCount() {
	return _rows;
}

/**
Returns the data that should be placed in the JTable at the given row and 
column.  This method is inherited from the very base-most Java TableModel class and is required.
@param row the row for which to return data.
@param col the column for which to return data.
@return the data that should be placed in the JTable at the given row and col.
*/
public Object getValueAt(int row, int col) {
	// AML: _sortOrder is an inherited int array that maps sorted row 
	// numbers
	// to regular row numbers (or elements within the _data Vector).  
	// If it's null, then that means the table currently isn't sorted.
	if (_sortOrder != null) {
		row = _sortOrder[row];
	}
	
	// AML: this is where the most important part of the table model is.
	// It gets the object out of the array and returns the right values
	// for each column.  You can do a lot in here, I've done some really
	// complicated stuff for some tables.  But for the most part,
	// something simple like below will work
	ERDiagram_Table table = (ERDiagram_Table)_data.get(row);

	switch (col) {
		case __COL_NAME:	return table.getName();
		case __COL_X:		return new Double(table.getX());
		case __COL_Y:		return new Double(table.getY());
		default:		return "";
	}
}

/**
Returns an array containing the widths (in number of characters) that the 
fields in the table should be sized to.
@return an integer array containing the widths for each field.

AML:
the widths are specified in terms of the number of characters that should
fit across in the field.  Here's how the table actually determines the real
size ... (in the following, W is the width you specify in the array)

1) it gets the font setting for the table header and writes out W "X" characters
to a string.  So a W of 6 would make "XXXXXX" ... it then sees how many pixels
in the header font it would take to fit this string.

2) it gets the font setting for the cell fonts and writes out W "X" characters
and sees how many pixels would be required to fit this string.

3) whichever one is LARGER, it pads it out with a few more pixels (about 10, I
think) and that's the width of the column in pixels
*/
public int[] getColumnWidths() {
	int[] widths = new int[__columns];
	widths[__COL_NAME] = 20;
	widths[__COL_X] = 5;	
	widths[__COL_Y] = 5;	
	return widths;
}

}
