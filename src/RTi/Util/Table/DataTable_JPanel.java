// DataTable_JPanel - panel for displaying a worksheet containing data table data.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

package RTi.Util.Table;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
Panel to contain the JWorksheet that displays DataTable data.
*/
@SuppressWarnings("serial")
public class DataTable_JPanel extends JPanel {

/**
The table of data to display in the worksheet.
*/
private DataTable __table = null;

/**
The parent frame containing this panel.
*/
private JFrame __parent = null;

/**
Column widths for the worksheet's fields.
*/
private int[] __widths;

/**
The worksheet to display the data.
*/
private JWorksheet __worksheet = null;

/**
Properties for how the worksheet should display.
*/
private PropList __props;

/**
The name of the file from which to read data for the worksheet.
*/
private String __filename = null;

/**
Constructor.  This sets up the worksheet with a default set of properties:<br>
<ul>
<li>JWorksheet.ShowPopupMenu=true</li>
<li>JWorksheet.SelectionMode=SingleRowSelection</li>
<li>JWorksheet.AllowCopy=true</li>
</ul>
To display with other properties, use the other constructor.
@param parent the JFrame in which this panel is displayed.
@param table the table to display in the panel.
@throws NullPointerException if any of the parameters are null.
*/
public DataTable_JPanel(JFrame parent, DataTable table) 
throws Exception {
	if (parent == null || table == null) {
		throw new NullPointerException();
	}

	__parent = parent;
	__filename = null;
	__table = table;

	__props = new PropList("DataTable_JPanel.JWorksheet");
	__props.add("JWorksheet.ShowPopupMenu=true");
	__props.add("JWorksheet.SelectionMode=ExcelSelection");
	__props.add("JWorksheet.AllowCopy=true");

	setupGUI();
}

/**
Constructor.  
@param parent the JFrame in which this panel is displayed.
@param table the table to display in this panel.
@param props the Properties to use to define the worksheet's characteristics.
@throws NullPointerException if any of the parameters are null.
*/
public DataTable_JPanel(DataTable_JFrame parent, DataTable table, PropList props) 
throws Exception {
	if (parent == null || table == null || props == null) {	
		throw new NullPointerException();
	}

	__parent = parent;
	__filename = null;
	__table = table;
	__props = props;

	setupGUI();
}

/**
Constructor.  This sets up the worksheet with a default set of properties:<br>
<ul>
<li>JWorksheet.ShowPopupMenu=true</li>
<li>JWorksheet.SelectionMode=SingleRowSelection</li>
<li>JWorksheet.AllowCopy=true</li>
</ul>
To display with other properties, use the other constructor.
@param parent the JFrame in which this panel is displayed.
@param filename the name of the file from which to read worksheet data.
@throws NullPointerException if any of the parameters are null.
*/
public DataTable_JPanel(DataTable_JFrame parent, String filename) 
throws Exception {
	if (parent == null || filename == null) {	
		throw new NullPointerException();
	}

	__parent = parent;
	__filename = filename;

	__props = new PropList("DataTable_JPanel.JWorksheet");
	__props.add("JWorksheet.ShowPopupMenu=true");
	__props.add("JWorksheet.SelectionMode=ExcelSelection");
	__props.add("JWorksheet.AllowCopy=true");

	setupGUI();
}

/**
Constructor.  
@param parent the JFrame in which this panel is displayed.
@param filename the name of the file from which to read worksheet data.
@param props the Properties to use to define the worksheet's characteristics.
@throws NullPointerException if any of the parameters are null.
*/
public DataTable_JPanel(DataTable_JFrame parent, String filename, PropList props) 
throws Exception {
	if (parent == null || filename == null || props == null) {
		throw new NullPointerException();
	}

	__parent = parent;
	__filename = filename;
	__props = props;

	setupGUI();
}

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	if (__table != null) {
		if (__table instanceof DbaseDataTable) {
			((DbaseDataTable)__table).close();
		}
	}
	__table = null;
	__parent = null;
	__widths = null;
	__worksheet = null;
	__props = null;
	__filename = null;	
	super.finalize();
}

/**
Returns the number of columns in the worksheet.
@return the number of columns in the worksheet.
*/
public int getWorksheetColumnCount() {
    if (__worksheet == null) {
        return 0;
    }
    return __worksheet.getColumnCount();
}

/**
Returns the number of rows in the worksheet.
@return the number of rows in the worksheet.
*/
public int getWorksheetRowCount() {
	if (__worksheet == null) {
		return 0;
	}
	return __worksheet.getRowCount();
}

/**
Sets up the GUI.
*/
private void setupGUI() 
throws Exception {
	setLayout(new GridBagLayout());
	String routine = "DataTable_JPanel.setupGUI";

	boolean fileReadOK = false;

	if (__filename == null) {
		// Assume that a table was passed in.
		fileReadOK = true;
	}
	else {
		if (StringUtil.endsWithIgnoreCase(__filename, "dbf")) {
			try {
				DbaseDataTable dbaseTable = new DbaseDataTable(__filename, false, true);
				__table = dbaseTable;
				fileReadOK = true;
			}
			catch (Exception e) {
				Message.printWarning(2, routine, "Error reading '" + __filename + "'");
				Message.printWarning(2, routine, e);
				fileReadOK = false;
			}
		}
		else {
			Message.printStatus(2, routine, 
				"Data table in filename '" + __filename + "' is not supported for reading.");
			throw new Exception("Data table type in filename '" + __filename 
				+ "' is not supported for reading.");
		}
	}

	if (!fileReadOK) {
		JGUIUtil.addComponent(this, new JLabel("Attributes are not available for this layer."),
			0, 0, 1, 1, 1, 1, 
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		return;
	}

	JScrollWorksheet jsw = null;
	try {
		DataTable_TableModel tm = new DataTable_TableModel(__table);
		DataTable_CellRenderer cr = new DataTable_CellRenderer(tm);
	
		jsw = new JScrollWorksheet(cr, tm, __props);
		__worksheet = jsw.getJWorksheet();
		__widths = cr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, __props);
		__worksheet = jsw.getJWorksheet();
	}
	__worksheet.setPreferredScrollableViewportSize(null);
	__worksheet.setHourglassJFrame(__parent);
	//__worksheet.addMouseListener(this);	
	//__worksheet.addKeyListener(this);

	JGUIUtil.addComponent(this, jsw, 
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
}

/**
Sets the worksheet's column widths.
Use this when the defaults are not correct.
This should be called after the frame in which the panel is found has called setVisible(true).
@param colWidths column widths for each column
*/
public void setWorksheetColumnWidths( int [] colWidths ) {
	__worksheet.setColumnWidths(colWidths);
}

/**
Sets the worksheet's column widths using internal defaults.
This should be called after the frame in which the panel is found has called setVisible(true).
*/
public void setWorksheetColumnWidths() {
	if (__worksheet != null) {
		__worksheet.calculateColumnWidths();
		if ( __widths != null ) {
			// There are cases where the column widths are very large.
			// May need to put the check here to guard against because UI may freeze.
			// For now changed ResultSetToDataTableFactory code to handle better at front end.
			/*
			for ( int i = 0; i < __widths.length; i++ ) {
				if ( __widths[i] > 5000 ) {
					Message.printStatus(2, "", "Column width [" + i + "] \"" + __table.getFieldName(i) + "\" = " + __widths[i] + " resetting to 5000");
					__widths[i] = 5000;
				}
			}
				*/
			__worksheet.setColumnWidths(__widths);
		}
	}
}

}