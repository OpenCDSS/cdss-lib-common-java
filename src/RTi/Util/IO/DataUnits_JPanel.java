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

package RTi.Util.IO;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
Panel to contain the JWorksheet that displays data units data.
*/
@SuppressWarnings("serial")
public class DataUnits_JPanel extends JPanel
{

/**
The list of units to display in the worksheet.
*/
private List<DataUnits> __dataUnitsList = null;

/**
The parent frame containing this panel.
*/
private DataUnits_JFrame __parent = null;

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
Constructor.  This sets up the worksheet with a default set of properties:<br>
<ul>
<li>JWorksheet.ShowPopupMenu=true</li>
<li>JWorksheet.SelectionMode=SingleRowSelection</li>
<li>JWorksheet.AllowCopy=true</li>
</ul>
To display with other properties, use the other constructor.
@param parent the JFrame in which this panel is displayed.
@param dataUnitsList the list of data units to display in the panel.
@throws NullPointerException if any of the parameters are null.
*/
public DataUnits_JPanel(DataUnits_JFrame parent, List<DataUnits> dataUnitsList ) 
throws Exception
{
	if (parent == null || dataUnitsList == null) {
		throw new NullPointerException();
	}

	__parent = parent;
	__dataUnitsList = dataUnitsList;

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
public DataUnits_JPanel(DataUnits_JFrame parent, List<DataUnits> dataUnitsList, PropList props) 
throws Exception
{
	if (parent == null || dataUnitsList == null ) {	
		throw new NullPointerException();
	}
	if ( props == null ) {
	    props = new PropList("DataTable_JPanel.JWorksheet");
	    props.add("JWorksheet.ShowPopupMenu=true");
	    props.add("JWorksheet.SelectionMode=ExcelSelection");
	    props.add("JWorksheet.AllowCopy=true");
	}

	__parent = parent;
	__dataUnitsList = dataUnitsList;
	__props = props;

	setupGUI();
}

/**
Constructor.  
@param parent the JFrame in which this panel is displayed.
@param filename the name of the file from which to read worksheet data.
@param props the Properties to use to define the worksheet's characteristics.
@throws NullPointerException if any of the parameters are null.
*/
public DataUnits_JPanel(DataUnits_JFrame parent, String filename, 
PropList props) 
throws Exception {
	if (parent == null || filename == null || props == null) {
		throw new NullPointerException();
	}

	__parent = parent;
	__props = props;

	setupGUI();
}

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable
{
	__dataUnitsList = null;
	__parent = null;
	__widths = null;
	__worksheet = null;
	__props = null;
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

	JScrollWorksheet jsw = null;
	try {
		DataUnits_TableModel tm = new DataUnits_TableModel(__dataUnitsList);
		DataUnits_CellRenderer cr = new DataUnits_CellRenderer(tm);
	
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