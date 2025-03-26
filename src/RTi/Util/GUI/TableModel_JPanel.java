// TableModel_JPanel - this class is a generic JPanel to contain the JWorksheet that displays TableModel data

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

package RTi.Util.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class is a generic JPanel to contain the JWorksheet that displays
TableModel data.  It primarily supports the TableModel_JFrame class, although
it could be used independently.
*/
@SuppressWarnings("serial")
public class TableModel_JPanel extends JPanel
{

private JWorksheet_AbstractRowTableModel __tm = null; // Table model to display.
private JWorksheet_DefaultTableCellRenderer __cr = null; // Cell renderer for table model.

private TableModel_JFrame __parent = null; // Parent JFrame.

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
@param tm the table model to display in the panel.
@param cr the cell renderer to use for displays.
@throws Exception if any error occurs.
*/
public TableModel_JPanel ( TableModel_JFrame parent,
				JWorksheet_AbstractRowTableModel tm,
				JWorksheet_DefaultTableCellRenderer cr )
throws Exception
{
	if (parent == null || tm == null || cr == null ) {
		throw new NullPointerException();
	}

	__parent = parent;
	__tm = tm;
	__cr = cr;

	__props = new PropList ( "" );
	__props = new PropList("TableModel_JPanel.JWorksheet");
	__props.add("JWorksheet.ShowPopupMenu=true");
	__props.add("JWorksheet.SelectionMode=ExcelSelection");
	__props.add("JWorksheet.AllowCopy=true");

	setupGUI();
}

/**
Constructor.
@param parent the JFrame in which this panel is displayed.
@param tm the table model to display in the panel.
@param cr the cell renderer to use for displays.
@param props the Properties to use to define the worksheet's characteristics.
@throws Exception if an error occurs.
*/
public TableModel_JPanel ( TableModel_JFrame parent,
				JWorksheet_AbstractRowTableModel tm,
				JWorksheet_DefaultTableCellRenderer cr,
				PropList props )
throws Exception
{	if (parent == null || tm == null || cr == null ) {
		throw new NullPointerException();
	}

	__parent = parent;
	__tm = tm;
	__cr = cr;
	if ( props == null ) {
		__props = new PropList ( "" );
	}
	else {
		__props = props;
	}

	setupGUI();
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
	String routine = "TableModel_JPanel.setupGUI";

	JScrollWorksheet jsw = null;
	try {
		jsw = new JScrollWorksheet(__cr, __tm, __props);
		__worksheet = jsw.getJWorksheet();
		__widths = __cr.getColumnWidths();
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
	if (__worksheet != null && __widths != null) {
		__worksheet.setColumnWidths(__widths);
	}
}

}