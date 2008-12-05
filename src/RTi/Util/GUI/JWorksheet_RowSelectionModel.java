// ----------------------------------------------------------------------------
// JWorksheet_RowSelectionModel.java - Class to handle row selections in the 
//	JWorksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-12-XX	J. Thomas Sapienza, RTi	Initial version.
// 2003-03-04	JTS, RTi		Javadoc'd, revised.  
// 2003-03-13	JTS, RTi		Added code to determine which rows
//					are selected.
// 2003-04-14	JTS, RTi		Added getSelectedColumn() and 
//					getSelectedColumns()
// 2003-05-22	JTS, RTi		Revised to allow selection of an entire
//					row if the first column is selected.
// 2003-09-23	JTS, RTi		* Renamed selectRow to 
//					  selectAllCellsInRow().
//					* Renamed selectCurrentRow to
//					  selectAllCellsInCurrentRow().
//					* Added selectRow() and made it public.
// 					* Added clearSelection().
// 2003-10-15	JTS, RTi		Added selectAllRows().
// 2003-10-24	JTS, RTi		Added selectColumn().
// 2003-10-27	JTS, RTi		__oneClickRowSelection is now by 
//					default 'false'.
// 2003-11-18	JTS, RTi		* Added finalize().
//					* Added selectCell().
// 2004-01-22	JTS, RTi		Added selectable code so that worksheets
//					can be made unselectable.
// 2004-06-07	JTS, RTi		Corrected bug in selectCell() that
//					was causing nothing to be selected.
// 2006-01-17	JTS, RTi		Added workaround for errors caused by
//					by changes in Java 1.5 to 
//					addSelectionInterval().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import RTi.Util.Message.Message;

/**
This class, in conjunction with the JWorksheet_ColSelectionModel, allows a JTable to have
a selection model that is like that of Microsoft Excel.  This class shares
data with the JWorksheet_ColSelectionModel that is in the same JTable, and 
JWorksheet_ColSelectionModel shares its data with this one.  The two need 
to interoperate very closely in order to get the desired effect.<p>

JTables by default have two selection models:<ol>
<li>The main one is a row selection model that keeps track of which rows 
are selected.  With this one in place users can either choose single rows,
multiple rows, or SINGLE individual cells.</li>
<li>Each column also has a selection model in place that keeps track of which
columns are selected.  If single cell selection is turned on, then only
one column can be selected.  If multiple row or single row selection is 
used, then all the columns in a row are selected when a row is selected.</li>
</ol><p>

The row selection model tells the JTable which rows are selected, and the 
column selection model tells which columns are selected.  
The interesting thing about how these are implemented by default is that 
there is no mechanism to say something like:
"Column 1 is selected in row 1, 2 and 4, but columns 2 and 3 are selected in row 3"<p>

If a column is selected in one row, it is selected in ALL rows.<p>

The JWorksheet_ColSelectionModel and JWorksheet_RowSelectionModel overcome these limitations.

Under 1.5, some selection method calls changed. 
1.4 actions and methods:
click - setSelectionInterval
ctrl-click - addSelectionInterval
shift-click - setLeadSelectionIndex
click-drag - setLeadSelectionIndex

1.5 actions and methods
click - setSelectionInterval
ctrl-click - addSelectionInterval
shift-click - setSelectionInterval
click-drag - setSelectionInterval
  
The partial fix is to reroute the method calls to the 1.4 behavior.  Additionally, current row/column needed to be set
in some instances such as selecting or deselection cells.
*/
public class JWorksheet_RowSelectionModel 
extends DefaultListSelectionModel {

/**
The JWorksheet_ColSelectionModel that is used in conjunction with this JWorksheet_RowSelectionModel in a JWorksheet.
*/
private JWorksheet_ColSelectionModel __colsm = null;

/**
A 1-D representation of the 2-D array of cells.  This is a sort of double
buffer on top of the cellsSelected array of highlighted cells so that 
cells can be selected and drawn as highlighted, and then the selection 
cancelled (e.g., by dragging the mouse back and de-highlighting them) and
only the originally-selected cells remain highlighted.
*/
protected boolean[] _buffer = null;

/**
A 1-D representation of the 2-D array of cells.  This specifies which ones 
are highlighted (have a value of true) so the JTable renderer can highlight
them.  This represents the entire size of the table (row * height), but each
cell is held in a single bit.
*/
protected boolean[] _cellsSelected = null;

/**
Whether a potential drag was started on the row selection model.  
This happens when a cell is clicked on, and then clicked on again while it was already selected.
*/
public boolean __possibleDrag = false;

/**
Whether the last cell drawing operation was done to the double buffer
(_buffer) or to the main "canvas" (_cellsSelected).  If drawnToBuffer is true,
the cell representation from _buffer will be used for the JTable renderer
to determine which cells are highlighted.
*/
protected boolean _drawnToBuffer = false;

/**
Whether all the cells in a row will be selected if the 0th column is clicked on.  
*/
private boolean __oneClickRowSelection = false;

/**
Whether any data can be selected in the worksheet.
*/
private boolean __selectable = true;

/**
Number of columns in the JTable.
*/
protected int _cols = -1;

/**
The last-selected column.
*/
protected int _currCol = -1;

/**
The last-selected row.
*/
protected int _currRow = -1;

/**
Number of rows in the JTable.
*/
protected int _rows = -1;

/**
The total size (rows * cols) of the JTable.
*/
protected int _size = -1;

/**
The Column at which drawing started for a dragged selection.
*/
protected int _startCol = -1;

/**
The Row at which drawing started for a dragged selection.
*/
protected int _startRow = -1;

/**
The anchor position from which all values are initially selected and dragged.
*/
protected int _anchor = -1;

/**
The most recently-selected position.
*/
protected int _lead = -1;

/**
The lowest row number selected.
*/
protected int _min = Integer.MAX_VALUE;

/**
The highest row number selected.
*/
protected int _max = -1;

/**
Sets the "partner" row selection model for use when two worksheets work 
together to do the same selection style.  See the TSViewTable code in GRTS for an example of how it works.
*/
private JWorksheet_RowSelectionModel __partner = null;

/**
Indicate whether the Java Runtime Environment is 1.5 or greater.  This is used because
some JTable code originally developed with Java 1.4 does not work with 1.5+ and special checks are needed.
*/
private static final boolean is15 = JWorksheet_ColSelectionModel.is15;

/**
Constructor.  Sets up the buffers and other internal variables.
@param tRows the number of rows in the JTable.
@param tCols the number of columns in the JTable.
*/
public JWorksheet_RowSelectionModel(int tRows, int tCols) {
	_rows = tRows;
	_cols = tCols;
	_size = _rows * _cols;

	_cellsSelected = new boolean [_size];
	_buffer = new boolean [_size];
	zeroArray(_cellsSelected);
	zeroArray(_buffer);
}

/**
Overrides method in DefaultListSelectionModel.  
Adds a selection interval to the list of selected intervals.
@param row0 the first row of the selection interval
@param row1 the last row of the selection interval.
*/
public void addSelectionInterval(int row0, int row1)
{   String routine = "JWorksheet_RowSelectionModel.addSelectionInterval";
    int dl = 10;
    
    if ( is15 ) {
        _currRow = row1;
        if (row0 != row1) {
            setLeadSelectionIndex(row1);
            return;
        }
    }
    if (__oneClickRowSelection && __partner != null) {
        if (__partner.allCellsInRowSelected(row0)) {
            __partner.forceDeselectRow(row0);
        }
        else {
            __partner.forceSelectAllCellsInRow(row0);
        }
    }

    if (!__selectable) {
        return;
    }
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "ROW: addSelectionInterval(" + row0 + ", " + row1 + ")");
    }
	if (row0 < _min) {
		_min = row0;
	}
	if (row1 < _min) {
		_min = row1;
	}
	if (row0 > _max) {
		_max = row0;
	}
	if (row1 > _max) {
		_max = row1;
	}

	_anchor = row0;
	_lead = row1;
	_drawnToBuffer = true;
	_startRow = row0;
	if ( Message.isDebugOn ) {
	    Message.printDebug ( dl, routine,"  _anchor: " + _anchor);
	    Message.printDebug ( dl, routine,"  _lead: " + _lead);
	    Message.printDebug ( dl, routine,"  _startRow: " + _startRow);
        Message.printDebug ( dl, routine,"  _currRow: " + _currRow);
	    Message.printDebug ( dl, routine,"  _cols: " + _cols);
	    Message.printDebug ( dl, routine,"  _startCol: " + _startCol);
	}
	int index = ((_currRow * _cols) + _startCol);
	if (index < 0 || index > _buffer.length) {
		// ignore -- this is an error encountered when running with Java 1.5.
	}
	else {
		_buffer[index] = true;		
	}

	System.arraycopy(_buffer, 0, _cellsSelected, 0, _size);
	if (_currCol == 0) {
		selectAllCellsInCurrentRow();
	}
	notifyAllListeners(_startRow, _startRow);

	__colsm._reset = true;
}

/**
Checks to see if all the cells in a row are selected.
@param row the row to check.
@return true if all cells are selected, false if not.
*/
public boolean allCellsInRowSelected(int row) {
	for (int i = 0; i < _cols; i++) {
		if (_buffer[(row * _cols) + i] == false) {
			return false;
		}
	}
	return true;
}

/**
Clears all selected cells and notifies their listeners that they were deselected.
*/
public void clearSelection() {
	zeroArray(_buffer);
	System.arraycopy(_buffer, 0, _cellsSelected, 0, _size);	
	notifyAllListeners(0, _rows);
}

/**
Deselects all the cells in the current row.
*/
public void deselectCurrentRow() {
	deselectRow(_currRow);
}

/**
Deselects all the cells in the specified row.
@param row the row to deselect.
*/
public void deselectRow(int row) {
	if (!__oneClickRowSelection) {
		return;
	}
	for (int i = 0; i < _cols; i++) {
		_buffer[(row * _cols) + i] = false;
	}
	if (__partner != null) {
		__partner.forceDeselectRow(row);
	}
	notifyAllListeners(row, row);
}

/**
Returns whether a drag (for drag and drop) was started on a cell in the selection model.
@return whether a drag was started.
*/
public boolean dragWasTriggered() {
	return __possibleDrag;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__colsm = null;
	_buffer = null;
	_cellsSelected = null;
	super.finalize();
}

/**
Forces all the cells in the specified row to be deselected, no matter what the
other internal settings of the row selection model are.
@param row the row to deselect.
*/
protected void forceDeselectRow(int row) {
	for (int i = 0; i < _cols; i++) {
		_buffer[(row * _cols) + i] = false;
	}
	System.arraycopy(_buffer, 0, _cellsSelected, 0, _size);	
	notifyAllListeners(row, row);
}

/**
Forces all the cells in the specified row to be selected.
@param row the row to select.
*/
private void forceSelectAllCellsInRow(int row) {
	for (int i = 0; i < _cols; i++) {
		_buffer[(row * _cols) + i] = true;
	}
	System.arraycopy(_buffer, 0, _cellsSelected, 0, _size);		
	notifyAllListeners(row, row);
}

/**
Returns the JWorksheet_ColSelectionModel being used.
@return the JWorksheet_ColSelectionModel being used.
*/
public JWorksheet_ColSelectionModel getJWorksheet_ColSelectionModel() {
	return __colsm;
}

/**
Returns whether all the cells in a row should be selected when the 0th column is clicked on.
@return whether to do one click row selection or not
*/
public boolean getOneClickRowSelection() {
	return __oneClickRowSelection;
}

/**
Returns the partner row selection model.  See the GRTS code for an example of how it works.
@return the partner row selection model.
*/
public JWorksheet_RowSelectionModel getPartner() {
	return __partner;
}

/**
Returns an integer of the first column that is selected or -1 if none are selected.
@return an integer of the first column that is selected or -1 if none are selected.
*/
public int getSelectedColumn() {
	for (int i = 0; i < _cols; i++) {
		for (int j = 0; j < _rows; j++) {
			if (_buffer[(j * _cols) + i] == true) {
				return i;
			}
		}
	}

	return -1;
}

/**
Returns an integer array of the columns that have had some of their cells selected.
@return an integer array of the columns that have had some of their cells selected.
*/
public int[] getSelectedColumns() {
	List v = new Vector();

	for (int i = 0; i < _cols; i++) {
		for (int j = 0; j < _rows; j++) {
			if (_buffer[(j * _cols) + i] == true) {
				v.add(new Integer(i));
				j = _rows + 1;
			}
		}
	}

	int[] arr = new int[v.size()];
//	Message.printStatus(1, "", "" + v.size() + " columns selected");
	for (int i = 0; i < arr.length; i++) {
		arr[i] = ((Integer)v.get(i)).intValue();
//		Message.printStatus(1, "", "column: " + arr[i]);
	}
	return arr;
}

/**
Returns an integer of the first row that is selected or -1 if none are selected.
@return an integer of the first row that is selected or -1 if none are selected.
*/
public int getSelectedRow() {
	for (int i = 0; i < _rows; i++) {
		for (int j = 0; j < _cols; j++) {
			if (_buffer[(i * _cols) + j] == true) {
				return i;
			}
		}
	}

	return -1;
}

/**
Returns an integer array of the rows that have had some of their cells selected.
@return an integer array of the rows that have had some of their cells selected.
*/
public int[] getSelectedRows() {
	List v = new Vector();

	for (int i = 0; i < _rows; i++) {
		for (int j = 0; j < _cols; j++) {
			if (_buffer[(i * _cols) + j] == true) {
				v.add(new Integer(i));
				j = _cols + 1;
			}
		}
	}

	int[] arr = new int[v.size()];
	for (int i = 0; i < arr.length; i++) {
		arr[i] = ((Integer)v.get(i)).intValue();
	}
	return arr;
}

/**
Returns the anchor selection index.
@return the anchor selection index.
*/
public int getAnchorSelectionIndex() {
	return _anchor;
}

/**
Returns the lead selection index.
@return the lead selection index.
*/
public int getLeadSelectionIndex() {
	return _lead;
}

/**
Returns the maximum selection index.
@return the maximum selection index.
*/
public int getMaxSelectionIndex() {
	return _max;
}

/**
Returns the minimum selection index.
@return the minimum selection index.
*/
public int getMinSelectionIndex() {
	return _min;
}

/**
Overrides method in DefaultListSelectionModel.  Returns whether the given row is selected or not.  Always returns true.
@return true.
*/
public boolean isSelectedIndex(int row)
{   /*String routine = "JWorksheet_RowSelectionModel.isSelectedIndex";
    int dl = 10;
    if ( Message.isDebugOn ) {
    Message.printDebug ( dl, routine, "ROW: isSelectedIndex(" + row + ")");
    }*/
	_currRow = row;
	return true;
}

/**
Notifies all listeners that something has changed in the selection model.
@param startRow the first row at which a change has occurred.
@param endRow the last row at which a change has occurred.
*/
private void notifyAllListeners(int startRow, int endRow)
{   String routine = "JWorksheet_RowSelectionModel.notifyAllListeners";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, " startRow: " + startRow + "  endRow: " + endRow);
    }
	ListSelectionListener[] listeners = getListSelectionListeners();
	ListSelectionEvent e = null;

	for (int i = 0; i < listeners.length; i++) {
		if (e == null) {
			e = new ListSelectionEvent(this, startRow - 30, endRow + 30, true);
		}
		((ListSelectionListener)listeners[i]).valueChanged(e);
	}	
}

/**
Overrides method in DefaultListSelectionModel.  Removes an row interval from those already selected.  Does nothing.
@param row0 the first row.
@param row1 the last row.
*/
public void removeIndexInterval(int row0, int row1) {
    throw new RuntimeException("Developer thinks method not called");
}

/**
Overrides method in DefaultListSelectionModel.  Removes a selection interval from those already selected.  
@param row0 the first row to remove
@param row1 the last row to remove.
*/
public void removeSelectionInterval(int row0, int row1)
{   String routine = "JWorksheet_RowSelectionModel.removeSelectionInterval";
    int dl = 10;
	if (__oneClickRowSelection && __partner != null) {	
		__partner.forceDeselectRow(row0);
	}

	if (!__selectable) {
		return;
	}
	if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "ROW: removeSelectionInterval(" + row0 + ", "+row1 + ")");
	}
	if (row0 < _min) {
		_min = row0;
	}
	if (row1 < _min) {
		_min = row1;
	}
	if (row0 > _max) {
		_max = row0;
	}
	if (row1 > _max) {
		_max = row1;
	}
    if ( is15 ) {
        _currRow = row1;
    }
	_anchor = row0;
	_lead = row1;

	// used to avoid weird calls like:
	//   removeSelectionInterval(2147483647, -1)
	// that get called when setting up a table
	if ( row0 > 100000000 || row0 < 0 || row1 > 100000000 || row1 < 0) {
		return;
	}

	__colsm._reset = true;
	_drawnToBuffer = true;
	_startRow = row0;
	_buffer[((_currRow * _cols) + _startCol)] = false;		
	if (_currCol == 0) {
		deselectCurrentRow();
	}
	System.arraycopy(_buffer, 0, _cellsSelected, 0, _size);
	notifyAllListeners(_startRow, _startRow);
}	

/**
Selects all the cells the current row.
*/
private void selectAllCellsInCurrentRow() {
	selectAllCellsInRow(_currRow);
}

/**
Selects all the cells in the specified row.
@param row the row to select.
*/
private void selectAllCellsInRow(int row) {
	if (!__oneClickRowSelection) {
		return;
	}
	for (int i = 0; i < _cols; i++) {
		_buffer[(row * _cols) + i] = true;
	}
	if (__partner != null) {
		__partner.forceSelectAllCellsInRow(row);
	}	
	notifyAllListeners(row, row);
}

/**
Selects an individual cell.
@param row the row of the cell.
@param visibleColumn the <b>visible</b> column of the cell.
*/
public void selectCell(int row, int visibleColumn) {
	__colsm._reset = true;
	_drawnToBuffer = true;
	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);

	int currRow = -1;
	boolean found = false;
	int j = 0;
	for (; j < _buffer.length && !found; j++) {
		// the very first time in this loop, this will increment the currRow # to 0
		if ((j % _cols) == 0) {
			currRow++;
		}
		
		if ((j % _cols) == visibleColumn) {
			if (currRow == row) {
				_buffer[j] = true;
				found = true;
			}
		}
		else {
			_buffer[j] = false;
		}
	}

	for (; j < _buffer.length; j++) {
		_buffer[j] = false;
	}
	
	notifyAllListeners(0, _rows);
}

/**
Forces a row to be selected and sets up the anchor points for selecting more cells based on the 0th column
of the specified row.  This sets up variables as if setSelectionInterval were called.
@param row the row to select.
*/
public void selectRow(int row)
{
    int dl = 10;
    String routine = "selectRow";
    if ( Message.isDebugOn ) {
        Message.printDebug(dl,routine,"ROW: selectRow(" + row + ")");
    }
	__colsm._reset = true;
	_drawnToBuffer = true;
	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);
	_startRow = row;
	_currRow = row;
	_startCol = 0;
	_currCol = 0;
	
	int sstartCol = 0;
	int endCol = _cols;

	for (int j = sstartCol; j < endCol; j++) {
		_buffer[(row * _cols) + j] = true;
	}
	notifyAllListeners(row, row);
}

public void selectRowWithoutDeselecting(int row) {
	__colsm._reset = true;
	_drawnToBuffer = true;
//	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);
	_startRow = row;
	_currRow = row;
	_startCol = 0;
	_currCol = 0;
	
	int sstartCol = 0;
	int endCol = _cols;

	for (int j = sstartCol; j < endCol; j++) {
		_buffer[(row * _cols) + j] = true;
	}
	notifyAllListeners(row, row);
}

/**
Selects all the rows in the table model
*/
public void selectAllRows() {
	__colsm._reset = true;
	_drawnToBuffer = true;
	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);
	
	for (int j = 0; j < _buffer.length; j++) {
		_buffer[j] = true;
	}
	notifyAllListeners(0, _rows);
}

/**
Selects a column and highlights all its cells.
@param visibleColumn the <b>visible</b> column to select.
*/
public void selectColumn(int visibleColumn) {
	__colsm._reset = true;
	_drawnToBuffer = true;
	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);
	
	for (int j = 0; j < _buffer.length; j++) {
		if ((j % _cols) == visibleColumn) {
			_buffer[j] = true;
		}
		else {
			_buffer[j] = false;
		}
	}
	notifyAllListeners(0, _rows);	
}

/**
Overrides method in DefaultListSelectionModel.  Sets the anchor's selection row.  Currently does nothing.
@param anchorIndex the row of the anchor position.
*/
public void setAnchorSelectionIndex(int anchorIndex) {
    // called when ctrl-shift-click
    int dl = 10;    // Debug level, to help track down Java 1.4 to 1.5 changes in behavior
    String routine = "JWorksheet_RowSelectModel.setAnchorSelectionIndex";
    
    if ( Message.isDebugOn ) {
        Message.printDebug(dl,routine,"ROW: setAnchorSelectionIndex(" + anchorIndex + ")");
    }
}

/**
Overrides method in DefaultListSelectionModel.  Sets the lead selection row.  
@param leadIndex the lead row.
*/
public void setLeadSelectionIndex(int leadIndex)
{
    int dl = 10;    // Debug level, to help track down Java 1.4 to 1.5 changes in behavior
    String routine = "JWorksheet_RowSelectModel.setLeadSelectionIndex";
    System.out.println("ROW.setLeadSelectionIndex " + leadIndex);
    debug();
	if (__oneClickRowSelection && __partner != null) {
		return;
	}
	if (!__selectable) {
	    // Worksheet is not selectable in any form so return.
		return;
	}
    if ( Message.isDebugOn ) {
        Message.printDebug(dl,routine,"ROW: setLeadSelectionIndex(" + leadIndex + ")");
    }
	__colsm._reset = true;
	_drawnToBuffer = true;
	System.arraycopy(_cellsSelected, 0, _buffer, 0, _size);
	int sstartRow = 0;
	int endRow = 0;

	// Order the rows to check to be increasing, despite the drag direction.
	if (_startRow < _currRow) {
		sstartRow = _startRow;
		endRow = _currRow;
	} else {
		sstartRow = _currRow;
		endRow = _startRow;
	}

	int sstartCol = 0;
	int endCol = 0;
	// Order the columns to check to be increasing, despite the drag direction.
	if (_startCol < _currCol ) {
		sstartCol = _startCol;
		endCol = _currCol;
	} else {
		sstartCol = _currCol;
		endCol = _startCol;
	}

	boolean selectRows = false;
	if (endCol == 0 && __colsm._zeroCol && __oneClickRowSelection) {
		selectRows = true;
	}
	if ( Message.isDebugOn ) {
	    Message.printDebug ( dl, routine, "Setting selected, StartCol=" + sstartCol + " EndCol=" + endCol +
	        " StartRow=" + sstartRow + " EndRow=" + endRow);
	}
	// Loop through selected columns (may or may not have selected rows).
	if (sstartCol >= 0 && endCol >= 0) {
	    // Loop through selected rows in column.
    	for (int i = sstartRow; i <= endRow; i++) {
    		if (selectRows) {
                if ( Message.isDebugOn ) {
                    Message.printDebug(dl,routine, "selectRows=true, selecting all rows in column " + i );
                }
    			selectAllCellsInRow(i);
    		}
    		else {
    		    if ( Message.isDebugOn ) {
    		        Message.printDebug(dl,routine, "In row: " + i + " Selecting columns StartCol=" +
    		            sstartCol + " EndCol=" + endCol + " #ColsTotal=" + _cols);
    		    }
    			for (int j = sstartCol; j <= endCol; j++) {
    				_buffer[(i * _cols) + j] = true;
    			}
    		}
    	}
	}
	notifyAllListeners(sstartRow, endRow);
}

/**
Sets whether all the cells in a row should be selected when the 0th column is clicked on.
@param oneClick whether to turn on one click row selectio or not.
*/
public void setOneClickRowSelection(boolean oneClick) {
	__oneClickRowSelection = oneClick;
}

/**
Sets the row selection model's partner selection model (from another worksheet).
@param partner the partner row selection model.  If null, partner row selection is turned off.
*/
public void setPartner(JWorksheet_RowSelectionModel partner) {
	__partner = partner;
}

/**
Sets whether any cells in the worksheet can be selected.
@param selectable whether any cells can be selected.  If false, all calls to
cell selection routines return immediately.
*/
public void setSelectable(boolean selectable) {
	__selectable = selectable;
}

/**
Overrides method in DefaultListSelectionModel.  Sets the selection interval.
@param row0 the first selection interval.
@param row1 the last selection interval.
*/
public void setSelectionInterval(int row0, int row1)
{   String routine = "JWorksheet_RowSelectionModel.setSelectionInterval";
    int dl = 10;
    System.out.println("ROW.setSelectionInterval " + row0+","+row1);
    
    if ( is15 ) {
        _currRow = row1;
        if ( row0 != row1) {
            setLeadSelectionIndex(row1);
            return;
        }
    }
    if (__oneClickRowSelection && __partner != null) {
        __partner.clearSelection();
        __partner.forceSelectAllCellsInRow(row0);
    }

    if (!__selectable) {
        return;
    }
	
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "ROW: setSelectionInterval(" + row0 + ", " + row1 + ")");
    }
	_max = -1;
	_min = Integer.MAX_VALUE;
	if (row0 < _min) {
		_min = row0;
	}
	if (row1 < _min) {
		_min = row1;
	}
	if (row0 > _max) {
		_max = row0;
	}
	if (row1 > _max) {
		_max = row1;
	}

	_anchor = row0;
	_lead = row1;
	__colsm._reset = true;

	if (_buffer[((_currRow * _cols) + _startCol)] == true) {
		__possibleDrag = true;
	}
	else {
		__possibleDrag = false;
	}
	
	if (_drawnToBuffer == true) {
		zeroArray(_cellsSelected);
	}
	else {
		_drawnToBuffer = true;
	}
	zeroArray(_buffer);
	_startRow = _currRow;

	if (__colsm._zeroCol && __oneClickRowSelection) {
		selectAllCellsInRow(row0);
	}

	// This can happen if a call is made to selectAllCellsInRow() in the worksheet
	if (_currRow == -1 && _startCol == -1) {
		_currRow = row0;
		_startCol = 0;
		selectAllCellsInRow(row0);
	}
	if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "_buffer.length: " + _buffer.length);
        Message.printDebug ( dl, routine,"  _currRow: " + _currRow);
        Message.printDebug ( dl, routine,"  _cols: " + _cols);
        Message.printDebug ( dl, routine,"  _startCol: " + _startCol);
	}
	_buffer[((_currRow * _cols) + _startCol)] = true;
	notifyAllListeners(_startRow, _startRow);
}

/**
Sets the JWorksheet_ColSelectionModel to use.
@param csm the JWorksheet_ColSelectionModel to use.
*/
public void setColSelectionModel(
JWorksheet_ColSelectionModel csm) {
	__colsm = csm;
	__colsm._rCols = _cols;	
	addListSelectionListener(__colsm);
}

/**
Sets a boolean array to all false.
@param array the array to "zero" out.
*/
private void zeroArray(boolean[] array) {
	for (int i = 0; i < _size; i++) {
		array[i] = false;
	}
}

public void moveLeadSelectionIndex(int leadIndex) {
    throw new RuntimeException("Developer thinks method not called");
}

private void debug() {
//    System.out.println(_anchor);
//    System.out.println(_lead);
//    System.out.println(_startRow);
//    System.out.println(_startCol);
//    System.out.println(_min);
//    System.out.println(_max);
//    System.out.println(_currRow);
}

}