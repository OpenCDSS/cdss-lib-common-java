// FileManager_JFrame - frame for displaying a data table with FileManagerFile as data

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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

/**
This class is the frame in which the panel displaying FileManager data in a worksheet is displayed.
*/
@SuppressWarnings("serial")
public class FileManager_JFrame extends JFrame
{

	/**
	The FileManager that is being displayed.
	*/
	private FileManager fileManager = null;
	
	/**
	The panel containing the worksheet that will be displayed in the frame.
	*/
	private FileManager_JPanel fileManagerPanel = null;

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
	//private String __filename = null;

	/**
	Constructor.
	@param title the title to put on the frame.
	@param fileManager the FileManager to display
	@throws Exception if table is null.
	*/
	public FileManager_JFrame ( String title, FileManager fileManager )
	throws Exception {
		// Call the overloaded method with no parent.
		this ( null, title, fileManager );
	}

	/**
	Constructor.
	@param parent parent JFrame, used to center the window on the parent.
	@param title the title to put on the frame.
	@param fileManager the FileManager to display
	@throws Exception if table is null.
	*/
	public FileManager_JFrame ( JFrame parent, String title, FileManager fileManager )
	throws Exception {
		JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
		if ( title == null ) {
			if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
				setTitle ( "File Manager" );
			}
			else {
            	setTitle( JGUIUtil.getAppNameForWindows() +	" - File Manager" );
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
		this.fileManager = fileManager;
	
		setupUI();
	}

	/**
	Sets the status bar's message and status text fields.
	@param message the value to put into the message text field.
	@param status the value to put into the status text field.
	*/
	public void setMessageStatus ( String message, String status ) {
		if ( message != null ) {
			this.messageJTextField.setText(message);
		}
		if ( status != null ) {
			this.statusJTextField.setText(status);
		}
	}
	
	/**
	Sets up the UI.
	*/
	private void setupUI()
	throws Exception {
		this.fileManagerPanel = new FileManager_JPanel(this, this.fileManager);
	
		getContentPane().add("Center", this.fileManagerPanel);
	
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

		int count = this.fileManagerPanel.getWorksheetRowCount();
		String plural = "s";
		if ( count == 1 ) {
			plural = "";
		}
		int countCol = this.fileManagerPanel.getWorksheetColumnCount();
		String pluralCol = "s";
    	if ( countCol == 1 ) {
        	pluralCol = "";
    	}

		setMessageStatus("Displaying " + count + " row" + plural + ", " + countCol + " column" + pluralCol + ".", "Ready");

		setVisible(true);

	this.fileManagerPanel.setWorksheetColumnWidths();
}

}