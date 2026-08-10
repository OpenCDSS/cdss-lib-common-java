// TableModel_JFrame - generic Frame for displaying a table model in JWorksheet format

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.IO.PropList;

/**
This class manages and displays data given a TableModel and CellRenderer by using a TableModel_JPanel.
The JFrame is suitable for general displays where a table model and renderer are available.
*/
@SuppressWarnings("serial")
public class TableModel_JFrame extends JFrame
{

private JWorksheet_AbstractRowTableModel tm = null;	// Table model to display.
private JWorksheet_DefaultTableCellRenderer cr = null;// Cell renderer for the table model.
private TableModel_JPanel tm_JPanel = null;		// The panel to hold the worksheet.
private PropList worksheet_props = null;		// Properties to control the worksheet.

private JTextField					// Message and status
	message_JTextField,				// bars.
	status_JTextField;

/**
Constructor.
@param tm The table model to display.
@param cr The cell renderer for the table model.
@param frame_props Properties to control the frame.
Currently only "Title" can be set.
@param worksheet_props Properties to control the worksheet.  Pass null for defaults.
@throws Exception if table is null.
*/
public TableModel_JFrame ( JWorksheet_AbstractRowTableModel tm,
				JWorksheet_DefaultTableCellRenderer cr,
				PropList frame_props, PropList worksheet_props )
throws Exception
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	if ( frame_props == null ) {
		frame_props = new PropList ( "" );
	}
	String title = frame_props.getValue ( "Title" );
	if ( title == null ) {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Table" );
		}
		else {
			setTitle( JGUIUtil.getAppNameForWindows() + " - Table" );
		}
	}
	else {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( title );
		}
		else {
			setTitle( JGUIUtil.getAppNameForWindows() + " - " + title );
		}
	}
	this.tm = tm;
	this.cr = cr;
	// Can be null.
	this.worksheet_props = worksheet_props;

	setupGUI();
}

/**
Sets the status bar's message and status text fields.
@param message the value to put into the message text field.
@param status the value to put into the status text field.
*/
public void setMessageStatus(String message, String status) {
	if (message != null) {
		this.message_JTextField.setText(message);
	}
	if (status != null) {
		this.status_JTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI()
throws Exception {
	if ( this.worksheet_props != null ) {
		this.tm_JPanel = new TableModel_JPanel ( this, this.tm, this.cr, this.worksheet_props );
	}
	else {
		// Use defaults.
		this.tm_JPanel = new TableModel_JPanel ( this, this.tm, this.cr );
	}

	getContentPane().add("Center", this.tm_JPanel);

	JPanel statusBar = new JPanel();
	statusBar.setLayout(new GridBagLayout());

	this.message_JTextField = new JTextField(20);
	this.message_JTextField.setEditable(false);
	this.status_JTextField = new JTextField(10);
	this.status_JTextField.setEditable(false);

	JGUIUtil.addComponent(statusBar, this.message_JTextField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(statusBar, this.status_JTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add("South", statusBar);

	setSize(600, 400);
	JGUIUtil.center(this);

	int count = this.tm_JPanel.getWorksheetRowCount();
	String plural = "s";
	if (count == 1) {
		plural = "";
	}

	setMessageStatus("Displaying " + count + " record" + plural + ".", "Ready");

	setVisible(true);
	toFront(); // Needed because sometimes gets hidden.

	this.tm_JPanel.setWorksheetColumnWidths();
}

}