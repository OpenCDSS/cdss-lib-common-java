// DataStores_JFrame - UI class to display a list of DataStores

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

package riverside.datastore;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

/**
This class is a frame that displays data store information.  Currently data store information cannot be edited.
*/
@SuppressWarnings("serial")
public class DataStores_JFrame extends JFrame
{

/**
The data table that was passed in.
*/
private List<DataStore> __dataStoreList = null;

/**
The datastore substitute list.
*/
private List<DataStoreSubstitute> __dataStoreSubstituteList = null;

/**
The panel containing the worksheet that will be displayed in the frame.
*/
private DataStores_JPanel __dataTablePanel = null;

/**
Message bar text field.
*/
private JTextField __messageJTextField = null;

/**
Status text field.
*/
private JTextField __statusJTextField = null;

/**
Constructor.
@param title the title to put on the frame.
@param dataStoreList the list of data stores to display in the worksheet.
@param dataStoreSubstituteList list of substitute datastore names
@throws Exception if table is null.
*/
public DataStores_JFrame(String title, Component parent, List<DataStore> dataStoreList, List<DataStoreSubstitute> dataStoreSubstituteList )
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
	this.__dataStoreList = dataStoreList;
	this.__dataStoreSubstituteList = dataStoreSubstituteList;

	setupGUI(parent);
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
private void setupGUI(Component parent)
throws Exception {
	__dataTablePanel = new DataStores_JPanel(this, this.__dataStoreList, this.__dataStoreSubstituteList);

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

	setSize(800, 400);
	if ( parent == null ) {
		JGUIUtil.center(this);
	}
	else {
		JGUIUtil.center(this, parent);
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

	setMessageStatus("Displaying " + count + " row" + plural + ", " + count_col + " column" + plural_col + ".", "Ready");

	setVisible(true);

	__dataTablePanel.setWorksheetColumnWidths();
}

}