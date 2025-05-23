// DataTable_JPanel - panel for displaying a worksheet containing data table data.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;

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
