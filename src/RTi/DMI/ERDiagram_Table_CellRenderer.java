// ERDiagram_Table_CellRenderer - class for rendering cells for in ERDiagram_Table objects

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
// ERDiagram_Table_CellRenderer - Class for rendering cells for a table
//	that displays information from ERDiagram_Table objects.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-09-03	JTS, RTi		Initial version.
// ----------------------------------------------------------------------------

package RTi.DMI;

import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;

/**
This class is used to render cells for a table that displays information from
ERDiagram_Table objects.

AML:
Every table needs two things:
1) a table model (so that it knows what values to show in cells)
2) a cell renderer (the cell renderer is a sort of tie between the table
and aspects of its table model).  I'm actually thinking of reworking the 
JWorksheet to eliminate the cell renderer ... but that would be a lot of work
so for now we keep it ...

but basically, all your cell renderers will look 100% like this one.  Copy, 
paste, search and replace (to change the class names).
*/
@SuppressWarnings("serial")
public class ERDiagram_Table_CellRenderer
extends JWorksheet_AbstractExcelCellRenderer {

/**
The table model for which this class will render cells.
*/
private ERDiagram_Table_TableModel __tableModel;

/**
Constructor.  
@param tableModel the table model for which to render cells
*/
public ERDiagram_Table_CellRenderer(ERDiagram_Table_TableModel tableModel) {
	__tableModel = tableModel;
}

/**
Returns the format for a given column.  Formats are in a form that will
be understood by StringUtil.format().
@param column the colum for which to return the format.
@return the format (as used by StringUtil.format) for a column.
*/
public String getFormat(int column) {
	return __tableModel.getFormat(column);
}

/**
Returns the widths of the columns in the table.  Widths are specified in 
number of characters, not number of pixels.
@return an integer array of the widths of the columns in the table.
*/
public int[] getColumnWidths() {
	return __tableModel.getColumnWidths();
}

}
