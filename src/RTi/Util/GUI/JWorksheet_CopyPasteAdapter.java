// JWorksheet_CopyPasteAdapter - this class copies data from selected rows and
// columns into a format that can be easily pasted into Microsoft Excel

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.Util.GUI;

import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import RTi.DMI.DMIUtil;

import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class copies data from selected rows and columns into a format that can be easily pasted into Microsoft Excel.
That format is (for two rows of three columns):<pre>
	value<b>[tab]</b>value<b>[tab]</b>value<b>[newline]</b>
	value<b>[tab]</b>value<b>[tab]</b>value<b>[newline]</b>
</pre>
It also has code for pasting values from Excel back into the JWorksheet;
however that has been commented out and will need further review.

The copy and paste code right now only responds to control-C and control-insert (Copy) and control-V and shift-insert (paste) actions.
*/
public class JWorksheet_CopyPasteAdapter
implements ActionListener {

/**
Whether the table has data for formatting the output.
*/
private boolean __canFormat = false;

/**
Whether copying is enabled or not.
*/
private boolean __copyEnabled = false;

/**
Whether pasting is enabled or not.
*/
private boolean __pasteEnabled = false;

/**
Cache of the classes for all the columns.
*/
@SuppressWarnings("rawtypes")
private Class[] __classes = null;

/**
Reference to the clipboard.
*/
private Clipboard __system;

/**
The JWorksheet for which this adapter is used.
*/
private JWorksheet __worksheet;

/**
Strings to refer to copy and paste operations.
*/
private final String
	__COPY = "Copy",
	__COPY_HEADER = "Copy Header",
	__COPY_ALL = "Copy All",
	__COPY_ALL_HEADER = "Copy All Header",
	__PASTE = "Paste";

/**
Constructor.
@param worksheet the JWorksheet for which to do copy and paste handling.
*/
public JWorksheet_CopyPasteAdapter (JWorksheet worksheet) {
	__worksheet = worksheet;
	KeyStroke copyC = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
	__worksheet.registerKeyboardAction(this, __COPY, copyC, JComponent.WHEN_FOCUSED);

	KeyStroke copyIns = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.CTRL_MASK, false);
	__worksheet.registerKeyboardAction(this, __COPY, copyIns, JComponent.WHEN_FOCUSED);

	KeyStroke pasteV = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
	__worksheet.registerKeyboardAction(this, __PASTE, pasteV, JComponent.WHEN_FOCUSED);

	KeyStroke pasteIns = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.SHIFT_MASK, false);
	__worksheet.registerKeyboardAction(this, __PASTE, pasteIns, JComponent.WHEN_FOCUSED);

	__system = Toolkit.getDefaultToolkit().getSystemClipboard();
}

/**
Copies or pastes worksheet data, depending on the action that is to be performed.
@param e the event that happened.
*/
public void actionPerformed(ActionEvent e) {
	if (__worksheet == null) {
		return;
	}

	String action = e.getActionCommand();

	JGUIUtil.setWaitCursor(__worksheet.getHourglassJFrame(), true);

	boolean copyHeader = false;

	if (action.equalsIgnoreCase(__COPY_HEADER)) {
		action = __COPY;
		copyHeader = true;
	}
	if (action.equalsIgnoreCase(__COPY_ALL_HEADER)) {
		action = __COPY_ALL;
		copyHeader = true;
	}

	if (action.equalsIgnoreCase(__COPY_ALL) || action.equalsIgnoreCase(__COPY_ALL_HEADER)) {
		// Copy all is easier than the normal copy because no checks need to be made for contiguous rows and columns.
		StringBuffer sbf = new StringBuffer();
		int numCols = __worksheet.getColumnCount();
		int numRows = __worksheet.getRowCount();

		__classes = new Class[numCols];
		for (int i = 0; i < numCols; i++) {
			__classes[i] = __worksheet.getColumnClass(__worksheet.getAbsoluteColumn(i));
		}

		if (__worksheet.getCellRenderer() instanceof JWorksheet_AbstractExcelCellRenderer) {
			__canFormat = true;
		}

		ProgressJDialog progressDialog = new ProgressJDialog( __worksheet.getHourglassJFrame(), "Copy progress", 0, (numRows * numCols));

		int count = 1;

		progressDialog.setVisible(true);

		__worksheet.startNewConsecutiveRead();

		if (copyHeader) {
			for (int j = 0; j < numCols; j++) {
				sbf.append(__worksheet.getColumnName(j, true));
				if (j < numCols - 1) {
					sbf.append("\t");
				}
			}
			sbf.append("\n");
		}

		try {
    		int[] absCols = new int[numCols];
    		for (int i = 0; i < numCols; i++) {
    			absCols[i] = __worksheet.getAbsoluteColumn(i);
    		}

    		for (int i = 0; i < numRows; i++) {
    			for (int j = 0; j < numCols; j++) {
    				progressDialog.setProgressBarValue(count++);
    				sbf.append(getValue(i, absCols[j]));
    				if (j < numCols - 1) {
    					sbf.append("\t");
    				}
    			}
    			sbf.append("\n");
    		}

    		progressDialog.dispose();

    		StringSelection stsel = new StringSelection(sbf.toString());
    		__system = Toolkit.getDefaultToolkit().getSystemClipboard();
    		__system.setContents(stsel, stsel);
		}
		catch (Exception ex) {
			String routine = getClass().getSimpleName() + ".actionPerformed";
			Message.printWarning(2, routine, "Error copying the worksheet to the clipboard.");
			Message.printWarning(2, routine, ex);
    		if ( progressDialog != null ) {
    			progressDialog.dispose();
    		}
			new ResponseJDialog(__worksheet.getHourglassJFrame(), "Copy Error", "Copy Error", ResponseJDialog.OK).response();
		}
	}
	else if (action.equalsIgnoreCase(__COPY) && __copyEnabled) {
		StringBuffer sbf = new StringBuffer();
		int numCols = __worksheet.getSelectedColumnCount();
		int numRows = __worksheet.getSelectedRowCount();
		int[] selectedRows = __worksheet.getSelectedRows();
		int[] selectedCols = __worksheet.getSelectedColumns();
		int[] visibleCols = new int[selectedCols.length];
		for (int i = 0; i < selectedCols.length; i++) {
			visibleCols[i] = __worksheet.getVisibleColumn(selectedCols[i]);
		}

		if (numCols == 0 || numRows == 0) {
			JGUIUtil.setWaitCursor(__worksheet.getHourglassJFrame(), false);
			return;
		}

		if (numRows == 1 && numCols == 1) {
			// Trivial case that will always be a successful copy.  This case is just a placeholder.
		}
		else if (numRows == 1) {
			// The rows are valid; the only thing left to check is whether the columns are contiguous.
			if (!areCellsContiguous(numRows, selectedRows, numCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block of columns.");
				return;
			}
		}
		else if (numCols == 1) {
			// The cols are valid; the only thing left to check is whether the rows are contiguous.
			if (!areCellsContiguous(numRows, selectedRows, numCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block of rows.");
				return;
			}
		}
		else {
			// There are multiple rows selected and multiple columns selected.  Make sure both are contiguous.
			if (!areCellsContiguous(numRows, selectedRows, numCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block\nof rows and columns.");
				return;
			}
		}

		int numColumns = __worksheet.getColumnCount();
		__classes = new Class[numColumns];
		for (int i = 0; i < numColumns; i++) {
			__classes[i] = __worksheet.getColumnClass(__worksheet.getAbsoluteColumn(i));
		}

		if (__worksheet.getCellRenderer() instanceof JWorksheet_AbstractExcelCellRenderer) {
			__canFormat = true;
		}

		ProgressJDialog progressDialog = new ProgressJDialog( __worksheet.getHourglassJFrame(), "Copy progress", 0, (numRows * numCols));

		int count = 1;

		progressDialog.setVisible(true);

		__worksheet.startNewConsecutiveRead();

		if (copyHeader) {
			for (int j = 0; j < numCols; j++) {
				sbf.append(__worksheet.getColumnName(visibleCols[j], true));
				if (j < numCols - 1) {
					sbf.append("\t");
				}
			}
			sbf.append("\n");
		}

		try {
    		for (int i = 0; i < numRows; i++) {
    			for (int j = 0; j < numCols; j++) {
    			/*
    				if (test) {
    					Message.printStatus(1, "", ""
    						+ "Copying row, col: "
    						+ selectedRows[i] + ", "
    						+ selectedCols[j]);
    				}
    			*/
    				progressDialog.setProgressBarValue(count++);
    				sbf.append(getValue(selectedRows[i],selectedCols[j]));
    				if (j < numCols - 1) {
    					sbf.append("\t");
    				}
    			}
    			sbf.append("\n");
    		}

    		progressDialog.dispose();

    		StringSelection stsel = new StringSelection(sbf.toString());
    		__system = Toolkit.getDefaultToolkit().getSystemClipboard();
    		__system.setContents(stsel, stsel);
		}
		catch (Exception ex) {
			new ResponseJDialog(__worksheet.getHourglassJFrame(), "Copy Error", "Copy Error", ResponseJDialog.OK).response();
			Message.printWarning(2, "", ex);
    		if ( progressDialog != null ) {
    			progressDialog.dispose();
    		}
		}
	}
	else if (action.equalsIgnoreCase(__PASTE) && __pasteEnabled) {
		int startRow = (__worksheet.getSelectedRows())[0];
		int startCol = (__worksheet.getSelectedColumns())[0];
		int numCols = __worksheet.getSelectedColumnCount();
		int numRows = __worksheet.getSelectedRowCount();
		int[] selectedRows = __worksheet.getSelectedRows();
		int[] selectedCols = __worksheet.getSelectedColumns();
		int[] visibleCols = new int[selectedCols.length];
		for (int i = 0; i < selectedCols.length; i++) {
			visibleCols[i] = __worksheet.getVisibleColumn(selectedCols[i]);
		}

		if (!areCellsContiguous(numRows, selectedRows, numCols, visibleCols)) {
			new ResponseJDialog(__worksheet.getHourglassJFrame(),
				"Paste Error", "Must select a contiguous range of cells.", ResponseJDialog.OK);
				JGUIUtil.setWaitCursor(__worksheet.getHourglassJFrame(),false);
			return;
		}
		int totalCells = numCols * numRows;
		int relCol = 0;
		try {
			String trstring = (String)(__system.getContents(this).getTransferData(DataFlavor.stringFlavor));
			List<String> v1 = StringUtil.breakStringList(trstring,"\n",0);

			int size1 = v1.size();
			int size2 = -1;
			boolean columnPasteCheck = false;
			if (size1 == 1) {
				columnPasteCheck = true;
			}
			String rowString = "";
			String value = "";
			for (int i = 0; i < size1; i++) {
				rowString = v1.get(i);
				if (rowString.equals("")) {
					rowString = " ";
				}
				List<String> v2 = StringUtil.breakStringList( rowString, "\t", 0);
				size2 = v2.size();
				if (columnPasteCheck && (size2 == 1) && (totalCells > 1) ) {
					fillCells(v2.get(0), selectedRows, selectedCols);
				}
				columnPasteCheck = false;
				for (int j = 0; j < size2; j++) {
					value = v2.get(j);
					relCol = __worksheet.getVisibleColumn( startCol + j);
					if ( (startRow + i < __worksheet.getRowCount()) && (relCol < __worksheet.getColumnCount()) ) {
						if (__worksheet.isCellEditable(	startRow + i, relCol)) {
							__worksheet.setValueAt(value, startRow + i, relCol);
						}
					}
				}
			}
		}
		catch (Exception ex) {
			new ResponseJDialog(__worksheet.getHourglassJFrame(), "Paste Error", "Paste Error", ResponseJDialog.OK).response();
			Message.printWarning(2, "", ex);
		}
		JGUIUtil.forceRepaint(__worksheet);
	}
	JGUIUtil.setWaitCursor(__worksheet.getHourglassJFrame(), false);
}

/**
Checks whether a selection of rows and columns is contiguous.
@param numRows the number of rows that are selected
@param selectedRows an integer array containing the numbers of the rows that are selected.
@param numCols the number of columns that are selected.
@param selectedCols an integer array containing the numbers of the columns that are selected.
*/
private boolean areCellsContiguous(int numRows, int[] selectedRows, int numCols, int[] selectedCols) {
	// There are two assumptions made about the data passed in:
	//    1) numCols/numRows is > 1.  It should have been checked already in the calling code.
	//    2) the values in selectedCols/selectedRows are sorted from lowest (at pos 0) to highest.

	// Trivial case is to make sure that the number of selected rows/columns
	// is equal to the difference between the number of the highest and lowest-selected rows/columns.

	if (Message.isDebugOn) {
		for (int i = 0; i < selectedRows.length; i++) {
			Message.printDebug(2, "", "selectedRows[" + i + "]: " + selectedRows[i]);
		}
		for (int i = 0; i < selectedCols.length; i++) {
			Message.printDebug(2, "", "selectedCols[" + i + "]: " + selectedCols[i]);
		}
	}

	if ((selectedCols[selectedCols.length - 1] - selectedCols[0]) + 1 != numCols) {

		if (Message.isDebugOn) {
			Message.printDebug(2, "", "Number of columns doesn't match column span (("
				+ selectedCols[selectedCols.length - 1] + " - " + selectedCols[0] + ") + 1 != " + numCols +")");
		}

		return false;
	}

	if ( ((selectedRows[selectedRows.length - 1] - selectedRows[0]) + 1) != numRows) {

		if (Message.isDebugOn) {
			Message.printDebug(2, "", "Number of rows doesn't match row span (("
				+ selectedRows[selectedRows.length - 1] + " - " + selectedRows[0] + ") + 1 != " + numRows +")");
		}

		return false;
	}

	// Otherwise, need to scan through the block made by the top-left-most
	// (lowest row and col) cell and the bottom-right-most (biggest row and col) cell.
	for (int i = selectedRows[0]; i <= selectedRows[numRows-1]; i++) {

		if (Message.isDebugOn) {
			Message.printDebug(2, "", "Checking row " + i + " for unselected cells ...");
		}

		for (int j = selectedCols[0]; j <=selectedCols[numCols-1]; j++){
			if (!__worksheet.isCellSelected(i, j)) {

				if (Message.isDebugOn) {
					Message.printDebug(2, "", "Cell at row: " + i + ", " + "col: " + j
						+ " is not selected, cells are non-contiguous.");
				}

				return false;
			}
		}
	}
	return true;
}

/**
Copies all table cells to the clipboard.
*/
public void copyAll() {
	copyAll(false);
}

/**
Copies all table cells to the clipboard.
@param includeHeader whether to include the header data for the copied cells
in the first line of copied information.
*/
public void copyAll(boolean includeHeader) {
	if (includeHeader) {
		ActionEvent e = new ActionEvent(this, 0, __COPY_ALL_HEADER);
		actionPerformed(e);
	}
	else {
		ActionEvent e = new ActionEvent(this, 0, __COPY_ALL);
		actionPerformed(e);
	}
}

/**
Copies the selected table cells to the clipboard.
*/
public void copy() {
	copy(false);
}

/**
Copies the selected table cells to the clipboard.
@param includeHeader whether to include the header data for the copied cells in the first line of copied information.
*/
public void copy(boolean includeHeader) {
	if (includeHeader) {
		ActionEvent e = new ActionEvent(this, 0, __COPY_HEADER);
		actionPerformed(e);
	}
	else {
		ActionEvent e = new ActionEvent(this, 0, __COPY);
		actionPerformed(e);
	}
}

/**
Fills a contiguous range of cells with a single value.
@param value the value to fill the cells with.
@param selectedRows the array of the selected rows (already checked that it is contiguous).
@param selectedCols the array of the selected cols (already checked that is is contiguous).
*/
private void fillCells(String value, int selectedRows[], int selectedCols[])
throws Exception {
	for (int i = 0; i < selectedCols.length; i++) {
		selectedCols[i] = __worksheet.getVisibleColumn(selectedCols[i]);
	}
	for (int i = 0; i < selectedRows.length; i++) {
		for (int j = 0; j < selectedCols.length; j++) {
			if (__worksheet.isCellEditable(selectedRows[i], selectedCols[j])) {
				__worksheet.setValueAt(value, selectedRows[i], selectedCols[j]);
			}
		}
	}
}

/**
Returns the worksheet used with this adapter.
@return the JWorksheet used with this adapter.
*/
public JWorksheet getJWorksheet() {
	return __worksheet;
}

/**
Pulls a value out of the worksheet from the specified cell and formats it
according to the formatting instructions stored in the cell renderer.
In addition, blank data are returned as "".
@param row the row of the cell
@param absoluteCol the <b>absolute</b> column of the cell
@return the cell data formatted properly in string format.
*/
private String getValue(int row, int absoluteCol) {
	int visibleCol = __worksheet.getVisibleColumn(absoluteCol);
	Object o = __worksheet.getConsecutiveValueAt(row, absoluteCol);

	if (!__canFormat) {
	    if ( o == null ) {
	        return "";
	    }
	    else {
	        return o.toString();
	    }
	}

	String format = __worksheet.getColumnFormat(absoluteCol);

/*
	Message.printStatus(1, "", "Class[" + visibleCol + "]: " + __classes[visibleCol]);
	Message.printStatus(1, "", "    o: " + o.getClass() + "  (" + o + ")");
	System.out.println("Class[" + visibleCol + "]: " + __classes[visibleCol]);
	System.out.println("    o: " + o.getClass() + "  (" + o + ")");
*/

	try {
    	if (__classes[visibleCol] == Double.class) {
    		Double DD = (Double)o;
    		if (DMIUtil.isMissing(DD.doubleValue())) {
    			return "";
    		}
    		else {
    			return StringUtil.formatString(DD,format);
    		}
    	}
    	else if (__classes[visibleCol] == Float.class) {
    		Float F = (Float)o;
    		if (DMIUtil.isMissing(F.floatValue())) {
    			return "";
    		}
    		else {
    			return StringUtil.formatString(F,format);
    		}
    	}
    	else if (__classes[visibleCol] == Long.class) {
    		Long L = (Long)o;
    		if (DMIUtil.isMissing(L.longValue())) {
    			return "";
    		}
    		else {
    			return StringUtil.formatString(L,format);
    		}
    	}
    	else if (__classes[visibleCol] == Integer.class) {
    		Integer I = (Integer)o;
    		if (DMIUtil.isMissing(I.intValue())) {
    			return "";
    		}
    		else {
    			return StringUtil.formatString(I,format);
    		}
    	}
    	else {
    		return "" + o;
    	}
	}
	catch (Exception e) {
		Message.printWarning(2,"JWorksheet_CopyPasteAdapter.getValue()", "Error while copying value.");
		Message.printWarning(2,"JWorksheet_CopyPasteAdapter.getValue()", "   Class[" + visibleCol + "]: " + __classes[visibleCol]);
		Message.printWarning(2,"JWorksheet_CopyPasteAdapter.getValue()", "    o: " + o.getClass() + "  (" + o + ")");
		Message.printWarning(2,"JWorksheet_CopyPasteAdapter.getValue()", e);
		return "" + o;
	}
}

/**
Pastes the clipboard into the table.
*/
public void paste() {
	ActionEvent e = new ActionEvent(this, 0, __PASTE);
	actionPerformed(e);
}

/**
Sets whether copying is enabled.
@param setting whether copying is enabled.
*/
public void setCopyEnabled(boolean setting) {
	__copyEnabled = setting;
}

/**
Sets whether pasting is enabled.
@param setting whether pasting is enabled.
*/
public void setPasteEnabled(boolean setting) {
	__pasteEnabled = setting;
}

/**
Sets the JWorksheet to use with this adapter.
@param worksheet the JWorksheet to use with this adapter.  If null, disables the adapter.
*/
public void setJWorksheet(JWorksheet worksheet) {
	__worksheet = worksheet;
}

/**
Shows an error dialog indicating that the selected cells cannot be copied.
@param addendum extra text to add on a newline after the line: "Invalid copy selection."
*/
private void showCopyErrorDialog(String addendum) {
	String message = "Invalid copy selection.";
	if (addendum != null) {
		message += "\n" + addendum;
	}
	new ResponseJDialog(new JFrame(), "Invalid Copy Selection", message, ResponseJDialog.OK);
	JGUIUtil.setWaitCursor(__worksheet.getHourglassJFrame(), false);
}

}