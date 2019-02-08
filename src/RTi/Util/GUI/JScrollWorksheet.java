// JScrollWorksheet - factory class for creating worksheets that use row headers and which need to be scrollable

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
// JScrollWorksheet - Factory class for creating worksheets that use 
//	Row Headers and which need to be scrollable.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-01-20	J. Thomas Sapienza, RTi	Initial version.
// 2004-01-22	JTS, RTi		* Added the constructor that takes a 
//					  parameter saying which columns to use
//					  as the row header.
// 					* Added the constructor that takes a
//					  parameter of the worksheet to use
//					  as the row header.
// 2004-01-23	JTS, RTi		Renamed to JScrollWorksheet.
// 2004-05-05	JTS, RTi		Expanded to allow use of a 
//					DragAndDropJWorksheet.
// 2005-04-26	JTS, RTi		Added finalize().
// 2006-01-31	JTS, RTi		On UNIX, scrolling events are now 
//					listened for by JWorksheets to correct
//					a repaint issue.  This is explained 
//					in much more detail at:
//					JWorksheet.adjustmentValueChanged().
// ----------------------------------------------------------------------------

// REVISIT (JTS - 2004-01-23)
// remove unnecssary constructors

package RTi.Util.GUI;

import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneLayout;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

/**
This class is a factory class for creating worksheets that use Row Headers and
which need to be scrollable.  It is a utility class that simplifies things for
developers.  While it's possible to implement scrollable row headers without
this class, the developer has to pay careful attention to the order in which
certain methods are called, and so it's safer to simply use this class.<p>

<b>CREATING A WORKSHEET</b><p>
To add a scrollable worksheet to a GUI using this class will require code similar to the following:<p>
<tt><blockquote>
	PropList props = new PropList("Worksheet Properties");
	// populate the properties
	...

	// create the scrolled worksheet
	JScrollWorksheet jsw = new JScrollWorksheet(0, 0, props);
	
	// get out the worksheet
	JWorksheet worksheet = jsw.getJWorksheet();
	
	// add the scrolled worksheet object (which functions
	// as a JScrollPane around the returned worksheet) to the GUI
	JGUIUtil.addComponent(mainPanel, jsw, ....);
</tt></blockquote>
*/
public class JScrollWorksheet extends JScrollPane {

/**
Whether this class is holding a drag and drop JWorksheet.
*/
private boolean __dnd = false;

/**
The worksheet created by this class, and around which this class will act as a JScrollPane.
*/
private JWorksheet __worksheet;

/**
The drag and drop worksheet created by this class, and around which this class will act as a JScrollPane.
*/
private DragAndDropJWorksheet __dndWorksheet;

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props) {
	__worksheet = new JWorksheet(cellRenderer, tableModel, props);

	initialize();
}

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
@param cols the columns of the worksheet to use as the row header.
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props, int[] cols) {
	__worksheet = new JWorksheet(cellRenderer, tableModel, props);

	initialize(new JWorksheet(cellRenderer, tableModel, props), cols);
}

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
@param worksheet the worksheet to use as the row header.
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props, JWorksheet worksheet){
	__worksheet = new JWorksheet(cellRenderer, tableModel, props);

	initialize(worksheet, null);
}

/**
Constructor.
@param rows the number of rows in the worksheet
@param cols the number of columns in the worksheet
@param props the properties for the worksheet
*/
public JScrollWorksheet(int rows, int cols, PropList props) {
	__worksheet = new JWorksheet(rows, cols, props);

	initialize();
}

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
@param dnd whether to create a DragAndDropJWorksheet (true) or not (false)
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props, boolean dnd) {
	__dnd = dnd;
	if (dnd) {
		__dndWorksheet = new DragAndDropJWorksheet(cellRenderer, tableModel, props);
	}
	else {
		__worksheet = new JWorksheet(cellRenderer, tableModel, props);
	}

	initialize();
}

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
@param cols the columns of the worksheet to use as the row header.
@param dnd whether to create a DragAndDropJWorksheet (true) or not (false)
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props, int[] cols, boolean dnd)
{
	__dnd = dnd;
	if (dnd) {
		__dndWorksheet = new DragAndDropJWorksheet(cellRenderer, tableModel, props);
	}
	else {
		__worksheet = new JWorksheet(cellRenderer, tableModel, props);
	}

	initialize(new JWorksheet(cellRenderer, tableModel, props), cols);
}

/**
Constructor.
@param cellRenderer the cell renderer for the worksheet
@param tableModel the table model for the worksheet
@param props the properties for the worksheet
@param worksheet the worksheet to use as the row header.
@param dnd whether to create a DragAndDropJWorksheet (true) or not (false)
*/
public JScrollWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props, JWorksheet worksheet, boolean dnd)
{
	__dnd = dnd;
	if (dnd) {
		__dndWorksheet = new DragAndDropJWorksheet(cellRenderer, tableModel, props);
	}
	else {
		__worksheet = new JWorksheet(cellRenderer, tableModel, props);
	}

	initialize(worksheet, null);
}

/**
Constructor.
@param rows the number of rows in the worksheet
@param cols the number of columns in the worksheet
@param props the properties for the worksheet
@param dnd whether to create a DragAndDropJWorksheet (true) or not (false)
*/
public JScrollWorksheet(int rows, int cols, PropList props, boolean dnd)
{
	__dnd = dnd;
	if (dnd) {
		__dndWorksheet = new DragAndDropJWorksheet(rows, cols, props);
	}
	else {
		__worksheet = new JWorksheet(rows, cols, props);
	}

	initialize();
}

/**
Required by the initialize() method.  Returns a new JViewport.
@return a new JViewport.
*/
protected JViewport createViewport() {
	return new JViewport();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__worksheet = null;
	__dndWorksheet = null;
	super.finalize();
}

/**
Returns the worksheet created and managed by this class.
@return the worksheet created and managed by this class.
*/
public JWorksheet getJWorksheet() {
	if (__dnd) {
		return __dndWorksheet;
	}
	else {
		return __worksheet;
	}
}

/**
Initializes the scrolling interface for the worksheet and if necessary, sets up the row headers.
*/
private void initialize() {
	// lifted from JScrollPane constructor.
	setLayout(new ScrollPaneLayout.UIResource());
	setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	setViewport(createViewport());
	setVerticalScrollBar(createVerticalScrollBar());
	setHorizontalScrollBar(createHorizontalScrollBar());

	if (IOUtil.isUNIXMachine()) {
		// See JWorksheet.adjustmentValueChanged() for an explanation of why the following is done.
		getVerticalScrollBar().addAdjustmentListener(__worksheet);
		getHorizontalScrollBar().addAdjustmentListener(__worksheet);
	}
	
	if (__dnd) {
		if (__dndWorksheet != null) {
			setViewportView(__dndWorksheet);
		}
	}
	else {
		if (__worksheet != null) {
			setViewportView(__worksheet);
		}
	}
	setOpaque(true);
	updateUI();

	if (!this.getComponentOrientation().isLeftToRight()) {
		viewport.setViewPosition(new Point(Integer.MAX_VALUE, 0));
	}
	
	// this is set up in the proplist that is used to create the worksheet.
	if (__dnd) {
		if (__dndWorksheet.isUsingRowHeaders()) {
			__dndWorksheet.enableRowHeader();
		}
	}
	else {
		if (__worksheet.isUsingRowHeaders()) {
			__worksheet.enableRowHeader();
		}
	}
}

/**
Initializes the scrolling interface for the worksheet and if necessary, sets up the row headers.
*/
private void initialize(JWorksheet worksheet, int[] cols) {
	// lifted from JScrollPane constructor.
	setLayout(new ScrollPaneLayout.UIResource());
	setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	setViewport(createViewport());
	setVerticalScrollBar(createVerticalScrollBar());
	setHorizontalScrollBar(createHorizontalScrollBar());

	if (IOUtil.isUNIXMachine()) {
		// See JWorksheet.adjustmentValueChanged() for an explanation of why the following is done.	
		getVerticalScrollBar().addAdjustmentListener(__worksheet);
		getHorizontalScrollBar().addAdjustmentListener(__worksheet);
	}

	if (__dnd) {
		if (__dndWorksheet != null) {
			setViewportView(__dndWorksheet);
		}
	}
	else {
		if (__worksheet != null) {
			setViewportView(__worksheet);
		}
	}
	setOpaque(true);
	updateUI();

	if (!this.getComponentOrientation().isLeftToRight()) {
		viewport.setViewPosition(new Point(Integer.MAX_VALUE, 0));
	}

	// this is set up in the proplist that is used to create the worksheet.
	if (__dnd) {
		if (__dndWorksheet.isUsingRowHeaders()) {
			__dndWorksheet.enableRowHeader(worksheet, cols);
		}
	}
	else {
		if (__worksheet.isUsingRowHeaders()) {
			__worksheet.enableRowHeader(worksheet, cols);
		}
	}
}

}
