// DataTable_JFrame - Frame for displaying a data table in JWorksheet format.

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

package RTi.Util.Table;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

/**
This class is the frame in which the panel displaying DataTable data in a worksheet is displayed.
*/
@SuppressWarnings("serial")
public class DataTable_JFrame extends JFrame
{

/**
The data table that was passed in.
*/
private DataTable table = null;

/**
The panel containing the worksheet that will be displayed in the frame.
*/
private DataTable_JPanel dataTablePanel = null;

/**
 * Parent JFrame, used to center the table window.
 */
private JFrame parent = null;

/**
Message text fields.
*/
private JTextField messageJTextField = null;

/**
 * Status text field.
 */
private JTextField statusJTextField = null;

/**
The name of the file from which to read data.
*/
private String filename = null;

/**
Constructor.
@param title the title to put on the frame.
@param filename the name of the file to be read and displayed in the worksheet.
@throws Exception if table is null.
*/
public DataTable_JFrame(String title, DataTable table)
throws Exception {
	this ( null, title, table );
}

/**
Constructor.
@param parent parent JFrame, used to center the window on the parent.
@param title the title to put on the frame.
@param filename the name of the file to be read and displayed in the worksheet.
@throws Exception if table is null.
*/
public DataTable_JFrame(JFrame parent, String title, DataTable table)
throws Exception {
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	if ( title == null ) {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Table" );
		}
		else {
            setTitle( JGUIUtil.getAppNameForWindows() +	" - Table" );
		}
	}
	else {
        if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( title );
		}
		else {
            setTitle( JGUIUtil.getAppNameForWindows() +	" - " + title );
		}
	}
	this.parent = parent;
	this.table = table;

	setupGUI();
}

/**
Constructor.
@param title the title to put on the frame.
@param filename the name of the file to be read and displayed in the worksheet.
@throws Exception if filename is null.
*/
public DataTable_JFrame(JFrame parent, String title, String filename)
throws Exception {
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
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
            setTitle( JGUIUtil.getAppNameForWindows() +	" - " + title );
		}
	}
	this.parent = parent;
	this.filename = filename;

	setupGUI();
}

/**
Sets the status bar's message and status text fields.
@param message the value to put into the message text field.
@param status the value to put into the status text field.
*/
public void setMessageStatus(String message, String status) {
	if (message != null) {
		this.messageJTextField.setText(message);
	}
	if (status != null) {
		this.statusJTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI()
throws Exception {
	if ( this.table == null ) {
		this.dataTablePanel = new DataTable_JPanel(this, this.filename);
	}
	else {
		this.dataTablePanel = new DataTable_JPanel(this, this.table);
	}

	getContentPane().add("Center", this.dataTablePanel);

	JPanel statusBar = new JPanel();
	statusBar.setLayout(new GridBagLayout());

	this.messageJTextField = new JTextField(20);
	this.messageJTextField.setEditable(false);
	this.statusJTextField = new JTextField(10);
	this.statusJTextField.setEditable(false);

	JGUIUtil.addComponent(statusBar, this.messageJTextField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(statusBar, this.statusJTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add("South", statusBar);

	setSize(600, 400);
	if ( this.parent == null ) {
		JGUIUtil.center(this);
	}
	else {
		JGUIUtil.center(this, this.parent);
	}

	int count = this.dataTablePanel.getWorksheetRowCount();
	String plural = "s";
	if (count == 1) {
		plural = "";
	}
	int count_col = this.dataTablePanel.getWorksheetColumnCount();
	String plural_col = "s";
    if ( count_col == 1 ) {
        plural_col = "";
    }

	setMessageStatus("Displaying " + count + " row" + plural + ", " + count_col + " column" + plural_col + ".", "Ready");

	setVisible(true);

	this.dataTablePanel.setWorksheetColumnWidths();
}

}