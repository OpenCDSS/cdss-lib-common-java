// ----------------------------------------------------------------------------
// DataTable_JPanel - panel for displaying a worksheet containing data table
//	data.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-21	J. Thomas Sapienza, RTi	Initial version.
// 2004-01-22	JTS, RTi		Revised to use JScrollWorksheet
//					for displaying row headers.
// 2004-07-29	JTS, RTi		* In-memory DataTables can now be passed
//					  in, instead of just files containing
//					  data tables.
//					* Fixed bug where some of the 
//					  constructors were not calling
//					  setupGUI().
// 2004-10-13	JTS, RTi		When a the name of the file containing
//					a datatable is passed in, if the file
//					cannot be read properly a message
//					is printed where normally the worksheet
//					would appear.
// 2004-10-22	JTS, RTi		Corrected a bug where tables in 
//					memory (eg, not read from a file)
//					were not being displayed properly.
// 2004-10-28	JTS, RTi		When column widths are being set, if
//					there are no widths in the table model
//					column widths will now be estimated
//					using the header column names.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

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
		// assume that a table was passed in
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
Sets the worksheet's column widths.  This should be called after the frame
in which the panel is found has called setVisible(true).
*/
public void setWorksheetColumnWidths() {
	if (__worksheet != null) {
		__worksheet.calculateColumnWidths();
	}
	if (__worksheet != null && __widths != null) {
		__worksheet.setColumnWidths(__widths);
	}
}

}