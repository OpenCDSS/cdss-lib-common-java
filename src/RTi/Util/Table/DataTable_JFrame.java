// DataTable_JFrame - Frame for displaying a data table in JWorksheet format.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// DataTable_JFrame - Frame for displaying a data table in JWorksheet
//	format.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-21	J. Thomas Sapienza, RTi	Initial version.
// 2004-01-08	Steven A. Malers, RTi	Set the icon and title from JGUIUtil.
// 2004-07-29	JTS, RTi		In-memory DataTables can now be passed
//					in, instead of just files containing
//					data tables.
// 2005-04-26	JTS, RTi		Added all data members to finalize().
// ----------------------------------------------------------------------------

package RTi.Util.Table;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

/**
This class is the frame in which the panel displaying DataTable data in a
worksheet is displayed.
*/
@SuppressWarnings("serial")
public class DataTable_JFrame extends JFrame
{

/**
The data table that was passed in.
*/
private DataTable __table = null;

/**
The panel containing the worksheet that will be displayed in the frame.
*/
private DataTable_JPanel __dataTablePanel = null;

/**
 * Parent JFrame, used to center the table window.
 */
private JFrame __parent = null;

/**
Message bar text fields.
*/
private JTextField
	__messageJTextField,
	__statusJTextField;

/**
The name of the file from which to read data.
*/
private String __filename = null;

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
throws Exception
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
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
	__parent = parent;
	__table = table;
	
	setupGUI();
}

/**
Constructor.
@param title the title to put on the frame.
@param filename the name of the file to be read and displayed in the worksheet.
@throws Exception if filename is null.
*/
public DataTable_JFrame(String title, String filename) 
throws Exception
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
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
	__filename = filename;
	
	setupGUI();
}

/**
Sets the status bar's message and status text fields.
@param message the value to put into the message text field.
@param status the value to put into the status text field.
*/
public void setMessageStatus(String message, String status) {
	if (message != null) {
		__messageJTextField.setText(message);
	}
	if (status != null) {
		__statusJTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() 
throws Exception {
	if (__table == null) {
		__dataTablePanel = new DataTable_JPanel(this, __filename);
	}
	else {
		__dataTablePanel = new DataTable_JPanel(this, __table);
	}

	getContentPane().add("Center", __dataTablePanel);

	JPanel statusBar = new JPanel();
	statusBar.setLayout(new GridBagLayout());

	__messageJTextField = new JTextField(20);
	__messageJTextField.setEditable(false);
	__statusJTextField = new JTextField(10);
	__statusJTextField.setEditable(false);

	JGUIUtil.addComponent(statusBar, __messageJTextField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(statusBar, __statusJTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add("South", statusBar);

	setSize(600, 400);
	if ( this.__parent == null ) {
		JGUIUtil.center(this);
	}
	else {
		JGUIUtil.center(this, this.__parent);
	}

	int count = __dataTablePanel.getWorksheetRowCount();
	String plural = "s";
	if (count == 1) {
		plural = "";
	}
	int count_col = __dataTablePanel.getWorksheetColumnCount();
	String plural_col = "s";
    if (count_col == 1) {
        plural_col = "";
    }

	setMessageStatus("Displaying " + count + " row" + plural +
	        ", " + count_col + " column" + plural_col + ".", "Ready");

	setVisible(true);

	__dataTablePanel.setWorksheetColumnWidths();
}

}