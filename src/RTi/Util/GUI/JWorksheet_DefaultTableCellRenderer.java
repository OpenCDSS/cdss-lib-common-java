// ----------------------------------------------------------------------------
// JWorksheet_DefaultTableCellRenderer - Base class to use as a cell renderer
//	for the JWorksheet, if no other renderers are to be used.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-12-XX	J. Thomas Sapienza, RTi	Initial version.
// 2003-03-04	JTS, RTi		Javadoc'd, revised.  
// 2003-03-20	JTS, RTi		* Revised after SAM's review.
//					* getWidths renamed to getColumnWidths
//					* Extends JWorksheet_AbstractTable ...
//					  CellRenderer now.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;

import javax.swing.JTable;

/**
Base class from which other classes that are to be used as cell renderers in 
the JWorksheet should be built, as it provides a getWidths() routine (which 
is used by JWorksheet) if the other classes do not.  If a JTable is not
assigned a specific cell renderer, this one will do the job.<p>
REVISIT (JTS - 2006-05-25)<p>
If I could do this over again, I would have combined AbstractTableCellRenderer,
DefaultTableCellRenderer and AbstractExcelCellRenderer into a single cell 
renderer.  The reasoning for having the separation came about from the 
way the JWorksheet was designed originally.<p>
AbstractTableCellRenderer was supposed to be The Base Class for all other 
renderers, providing the basic outline of what they would do.<p>
DefaultTableCellRenderer was supposed to be used for worksheets that didn't
require any special cell formatting.<p>
AbstractExcelCellRenderer was supposed to be the base class for cell renderers
that would do formatting of cell contents.<p>
In theory.<p>
In practice, ALL cell renderers are doing cell formatting, so the 
AbstractTableCellRenderer and DefaultTableCellRenderer are unnecessary overhead.
<p>
<b>Also</b><p>
I really don't see much of a good reason to even REQUIRE cell renderers for
most classes.  There are a lot of cell renderers out there that are almost 100%
the same class.  At this point there's little chance of going back and 
eliminating them, but if I could I would.  Use a default cell renderer for all
those classes and eliminate a lot of maintenance woes.
*/
public class JWorksheet_DefaultTableCellRenderer 
extends JWorksheet_AbstractTableCellRenderer {

/**
Returns the table cell renderer used to render a cell in a table. 
@param table the JWorksheet in which the cell is being renderer.
@param value the value to put in the cell.
@param isSelected whether the cell is selected or not.
@param hasFocus whether the cell currently has focus or not.
@param row the row in which the cell can be found.
@param column the column in which the cell can be found.
@return a component that can be displayed in a table cell, which contains 
the value of the cell being renderered.
*/
public Component getTableCellRendererComponent(JTable table, Object value,
boolean isSelected, boolean hasFocus, int row, int column) {
	return super.getTableCellRendererComponent(table, value,
		isSelected, hasFocus, row, column);
}
}
