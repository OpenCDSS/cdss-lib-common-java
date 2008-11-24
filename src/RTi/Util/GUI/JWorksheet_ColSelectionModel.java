// ----------------------------------------------------------------------------
// JWorksheet_JWorksheet_ColSelectionModel.java - Class to handle col 
//	selections in the JWorksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-12-XX	J. Thomas Sapienza, RTi	Initial version.
// 2003-03-04	JTS, RTi		Javadoc'd, revised.  
// 2003-03-20	JTS, RTi		Revised after SAM's review.
// 2003-05-22	JTS, RTi		Revised to allow selection of entire
//					row if the first column is selected.
// 2003-11-18	JTS, RTi		Added finalize().
// 2006-01-17	JTS, RTi		Added workaround for errors cause by 
//					changes in Java 1.5 in 
//					isSelectedIndex().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import javax.swing.DefaultListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class, in conjunction with the JWorksheet_RowSelectionModel, allows a 
JTable to have
a selection model that is like that of Microsoft Excel.  This class shares
data with the JWorksheet_RowSelectionModel that is in the same JTable, and 
JWorksheet_RowSelectionModel shares its data with this one.  The two need 
to interoperate very closely in order to get the desired effect.

JTables by default have two selection models:<ol>
<li>The main one is a row selection model that keeps track of which rows 
are selected.  With this one in place users can either choose single rows,
multiple rows, or SINGLE individual cells.</li>
<li>Each column also has a selection model in place that keeps track of which
columns are selected.  If single cell selection is turned on, then only
one column can be selected.  If multiple row or single row selection is 
used, then all the columns in a row are selected when a row is selected.</li>
</ol>

The row selection model tells the JTable which rows are selected, and the 
column selection model tells which columns are selected.  
The interesting thing about how these are implemented by default is that 
there is no mechanism to say something like:
"Column 1 is selected in row 1, 2 and 4, but columns 2 and 3 are selected in
row 3"

If a column is selected in one row, it is selected in ALL rows.

The JWorksheet_ColSelectionModel and JWorksheet_RowSelectionModel overcome 
these limitations.
*/
public class JWorksheet_ColSelectionModel extends DefaultListSelectionModel 
implements ListSelectionListener {

/**
The JWorksheet_RowSelectionModel that is used in conjunction with this 
JWorksheet_ColSelectionModel
in a JTable.
*/
private JWorksheet_RowSelectionModel __rowsm = null;

/**
A local reference to the _buffer in the JWorksheet_RowSelectionModel.  
*/
private boolean[] __buffer = null;
/**
A local reference to the _cellsSelected in the JWorksheet_RowSelectionModel.
*/
private boolean[] __cellsSelected = null;
/**
Whether to re-read the _buffer and _cellsSelected from the 
JWorksheet_RowSelectionModel.
*/
protected boolean _reset = true;

/**
The value of the JWorksheet_RowSelectionModel's _cols
*/
protected int _rCols = -1;
/**
The value of the JWorksheet_RowSelectionModel's _currRow.
*/
private int __rCurrRow = -1;

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
Whether the zeroth column was clicked in or not.
*/
protected boolean _zeroCol = false;

static final boolean is15 = isVersion15OrGreater ();
        

/**
Constructor.  Initializes to no columns selected.
*/
public JWorksheet_ColSelectionModel() {}	

private static boolean isVersion15OrGreater ()
{
    String version = System.getProperty("java.vm.version");
    version = version.substring (0,3);
    // System.out.println("returning to is15: " +(StringUtil.atof(version) >= 1.5));
    return (StringUtil.atof(version) >= 1.5);
}

/**
Overrised the method in DefaultListSelectionModel.  
Adds a selection interval to the list of selected intervals.  It marks a new
series of cells as selected.
@param col0 the first col of the selection interval
@param col1 the last col of the selection interval.
*/
public void addSelectionInterval(int col0, int col1)
{   String routine = "JWorksheet_ColSelectionModel.addSelectionInterval";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "COL: addSelectionInterval(" + col0 + ", " + col1 + ")");
    }
    
    // iws - fix for 1.5 selection issues, see RowSelectionModel class javadoc
    if (is15 && col0 != col1) {
    setLeadSelectionIndex(col1);
    return;
    }
    if (col0 == 0) {
            // System.out.println("   ZEROCOL = TRUE");
            _zeroCol = true;
    }
    if ( is15 )
        __rowsm._currCol = col1;
    else
        __rowsm._currCol = col0;
    if (col0 < _min) {
            _min = col0;
    }
    if (col1 < _min) {
            _min = col1;
    }
    if (col0 > _max) {
            _max = col0;
    }
    if (col1 > _max) {
            _max = col1;
    }
    _anchor = col0;
    _lead = col1;
    __rowsm._startCol = col0;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__rowsm = null;
	__buffer = null;
	__cellsSelected = null;
	super.finalize();
}

/**
Returns the JWorksheet_RowSelectionModel being used.
@return the JWorksheet_RowSelectionModel being used.
*/
public JWorksheet_RowSelectionModel getJWorksheet_RowSelectionModel() {
	return __rowsm;
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
Returns the max selection index.
@return the max selection index.
*/
public int getMaxSelectionIndex() {
	return _max;
}

/**
Returns the min selection index.
@return the min selection index.
*/
public int getMinSelectionIndex() {
	return _min;
}

/**
Overrides the method in DefaultListSelectionModel.  
Returns whether the given col is selected or not.
@param col the column to check whether it is selected or not.
@return true.
*/
public boolean isSelectedIndex(int col)
{   String routine = "JWorksheet_ColSelectionModel.isSelectedIndex";
    int dl = 10;
    /*if ( Message.isDebugOn ) {
    Message.printDebug ( dl, routine, "COL: isSelectedIndex(" + col + ")");
    }*/
	__rCurrRow = __rowsm._currRow;

	// First check to see if the selected value has been drawn to the 
	// buffer (i.e., it is a new drag-selection) or if it is drawn 
	// to cellsSelected (i.e., the user dragged a new selected and released
	// the mouse button).

	   /*if ( Message.isDebugOn ) {
    Message.printDebug ( dl, routine, "  __rCurrRow: " + __rCurrRow);
    Message.printDebug ( dl, routine,"  _rCols: " + _rCols);
    Message.printDebug ( dl, routine,"  col: " + col);
    }*/
	if (__rowsm._drawnToBuffer) {
		if (_reset) {
			_reset = false;
			__buffer = __rowsm._buffer;
		}
		// The following was added because in 1.5 invalid array
		// indices were being generated sometimes (despite the code
		// working, and having worked, perfectly on Windows for over
		// 2 years).
		int index = ((__rCurrRow * _rCols) + col);
		if ( (__buffer.length == 0) || index < 0 || index > __buffer.length) {
			return false;
		}
		else if (__buffer[index]) {
			return true;
		}
	} 
	else {
		if (_reset) {
			_reset = false;
			__cellsSelected = __rowsm._cellsSelected;
		}
		if (__cellsSelected == null || __cellsSelected.length == 0) {
			return false;
		}
		// The following was added because in 1.5 invalid array
		// indices were being generated sometimes (despite the code
		// working, and having worked, perfectly on Windows for over
		// 2 years).		
		int index = ((__rCurrRow * _rCols) + col);
		if ( (__cellsSelected.length == 0) || index < 0 || index > __cellsSelected.length) {
			return false;
		}
		else if (__cellsSelected[index]) {
			return true;
		}
		          /*if ( Message.isDebugOn ) {
            Message.printDebug ( dl, routine, "  __cellsSelected size: " + __cellsSelected.length);
            }*/
	}
	return false;
}

/**
Overrides the method in DefaultListSelectionModel.  
Removes an col interval from those already selected.  Does nothing.
@param col0 the first col.
@param col1 the last col.
*/
public void removeIndexInterval(int col0, int col1) {}	

/**
Overrides the method in DefaultListSelectionModel.  
Removes a selection interval from those already selected.  
@param col0 the first col to remove
@param col1 the last col to remove.
*/
public void removeSelectionInterval(int col0, int col1)
{   String routine = "JWorksheet_ColSelectionModel.removeSelectionInterval";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "COL: removeSelectionInterval(" + col0 + ", "+col1 + ")");
    }
        if ( is15 )
            __rowsm._currCol = col1;
        else
            __rowsm._currCol = col0;
	if (col0 < _min) {
		_min = col0;
	}
	if (col1 < _min) {
		_min = col1;
	}
	if (col0 > _max) {
		_max = col0;
	}
	if (col1 > _max) {
		_max = col1;
	}
	_anchor = col0;
	_lead = col1;
	__rowsm._startCol = col0;
}	

/**
Overrides the method in DefaultListSelectionModel.  
Sets the anchor's selection col.  Currently does nothing.
@param anchorCol the col of the anchor position, the initial point clicked
when the user is dragging the mouse.
*/
public void setAnchorSelectionIndex(int anchorCol)
{
    String routine = "JWorksheet_ColSelectionModel.setAnchorSelectionIndex";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "COL: setAnchorSelectionIndex(" + anchorCol + ")");
    }
}

/**
Overrides the method in DefaultListSelectionModel.  
Sets the lead selection col.  
@param leadCol the lead col.
*/
public void setLeadSelectionIndex(int leadCol)
{
    String routine = "JWorksheet_ColSelectionModel.setLeadSelectionIndex";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "COL: setLeadSelectionIndex(" + leadCol + ")");
    }
	__rowsm._currCol = leadCol;
}

/**
Sets the JWorksheet_RowSelectionModel to use.
@param rsm the JWorksheet_RowSelectionModel to use.
*/
public void setRowSelectionModel(
JWorksheet_RowSelectionModel rsm) {
	__rowsm = rsm;
}

/**
From DefaultListSelectionModel.  Sets the setlection interval.
@param col0 the first selection interval.
@param col1 the last selection interval.
*/
public void setSelectionInterval(int col0, int col1)
{   String routine = "JWorksheet_ColSelectionModel.setSelectionInterval";
    int dl = 10;
    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine,"COL: setSelectionInterval(" + col0 + ", " + col1 + ")");
    }
    
    // iws - fix for 1.5 selection issues, see RowSelectionModel class javadoc
    if (is15 && col0 != col1) {
    setLeadSelectionIndex(col1);
    return;
    }
	_zeroCol = false;
	if (col0 == 0) {
		_zeroCol = true;
//		System.out.println("   ZEROCOL = TRUE");
	}
        if ( is15 )
            __rowsm._currCol = col1;
        else
            __rowsm._currCol = col0;
	_max = -1;
	_min = Integer.MAX_VALUE;
	if (col0 < _min) {
		_min = col0;
	}
	if (col1 < _min) {
		_min = col1;
	}
	if (col0 > _max) {
		_max = col0;
	}
	if (col1 > _max) {
		_max = col1;
	}
	_anchor = col0;
	_lead = col1;
	__rowsm._startCol = col0;
}

/**
From ListSelectionListener.  This is notified if any changes have been made
to the selection model (by the JWorksheet_RowSelectionModel) and the 
JTable rendered needs
to check which ones need highlighted again.
@param e the ListSelectionEvent that happened.
*/
public void valueChanged(ListSelectionEvent e) {
	if (!(e.getValueIsAdjusting())) {
		System.arraycopy(__rowsm._buffer, 0, 
			__rowsm._cellsSelected, 0, __rowsm._size);
	}
}

}
