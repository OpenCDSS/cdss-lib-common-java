// JWorksheet_AbstractTableCellRenderer - base class for JWorksheet cell renderers

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
// JWorksheet_AbstractTableCellRenderer - Base class from which other classes
//	that are to be used as cell renderers in the JWorksheet should be
//	built, as it gives all JWorksheet renderers a core set of base
//	functionality.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-03-20	J. Thomas Sapienza, RTi	Initial version.
// 2003-06-29	JTS, RTi		Added _tableModel
// 2003-11-18	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import javax.swing.table.DefaultTableCellRenderer;

/**
Base class from which other classes that are to be used as cell renderers in 
the JWorksheet should be built, as it provides a getWidths() routine (which 
is used by JWorksheet) if the other classes do not.<p>
TODO (JTS - 2006-05-25)<p>
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
public abstract class JWorksheet_AbstractTableCellRenderer 
extends DefaultTableCellRenderer {

/**
The table model associated with this cell renderer.  This should be set by
derived classes in their constructor.  
*/
protected JWorksheet_AbstractTableModel _tableModel = null;

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	_tableModel = null;
	super.finalize();
}

/**
Returns a null array.   In derived classes, this should return an array
of integers, each of which is the width (in characters) that column #i (at 
position array[i]) should be able to accommodate.
@return a null integer array.
*/
public int[] getColumnWidths() {
	return null;
}

}
