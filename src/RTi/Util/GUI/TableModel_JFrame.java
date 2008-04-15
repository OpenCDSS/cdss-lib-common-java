// ----------------------------------------------------------------------------
// TableModel_JFrame - generic Frame for displaying a table model in JWorksheet
//	format.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2004-10-27	Steven A. Malers, RTi	Copy DataTable_JFrame and modify.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

import RTi.Util.IO.PropList;

/**
This class manages and displays data given a TableModel and CellRenderer by
using a TableModel_JPanel.  The JFrame is suitable for general displays where
a table model and renderer are available.
*/
public class TableModel_JFrame extends JFrame
{

private JWorksheet_AbstractRowTableModel __tm = null;	// Table model to
							// display
private JWorksheet_DefaultTableCellRenderer __cr = null;// Cell renderer for
							// the table model.
private TableModel_JPanel __tm_JPanel = null;		// The panel to hold
							// the worksheet.
private PropList __worksheet_props = null;		// Properties to control
							// the worksheet.

private JTextField					// Message and status
	__message_JTextField,				// bars.
	__status_JTextField;

/**
Constructor.
@param tm The table model to display.
@param cr The cell renderer for the table model.
@param frame_props Properties to control the frame.
Currently only "Title" can be set.
@param worksheet_props Properties to control the worksheet.  Pass null for
defaults.
@throws Exception if table is null.
*/
public TableModel_JFrame (	JWorksheet_AbstractRowTableModel tm,
				JWorksheet_DefaultTableCellRenderer cr,
				PropList frame_props, PropList worksheet_props )
throws Exception
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	if ( frame_props == null ) {
		frame_props = new PropList ( "" );
	}
	String title = frame_props.getValue ( "Title" );
	if ( title == null ) {
		if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Table" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() +
			" - Table" );
		}
	}
	else {	if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( title );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() +
			" - " + title );
		}
	}
	__tm = tm;
	__cr = cr;
	// Can be null...
	__worksheet_props = worksheet_props;
	
	setupGUI();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable
{	__tm = null;
	__cr = null;
	__tm_JPanel = null;
	__message_JTextField = null;
	__status_JTextField = null;
	__worksheet_props = null;
	super.finalize();
}

/**
Sets the status bar's message and status text fields.
@param message the value to put into the message text field.
@param status the value to put into the status text field.
*/
public void setMessageStatus(String message, String status) {
	if (message != null) {
		__message_JTextField.setText(message);
	}
	if (status != null) {
		__status_JTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() 
throws Exception
{	if ( __worksheet_props != null ) {
		__tm_JPanel = new TableModel_JPanel ( this, __tm, __cr,
						__worksheet_props );
	}
	else {	// Use defaults...
		__tm_JPanel = new TableModel_JPanel ( this, __tm, __cr );
	}

	getContentPane().add("Center", __tm_JPanel);

	JPanel statusBar = new JPanel();
	statusBar.setLayout(new GridBagLayout());

	__message_JTextField = new JTextField(20);
	__message_JTextField.setEditable(false);
	__status_JTextField = new JTextField(10);
	__status_JTextField.setEditable(false);

	JGUIUtil.addComponent(statusBar, __message_JTextField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(statusBar, __status_JTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	getContentPane().add("South", statusBar);

	setSize(600, 400);
	JGUIUtil.center(this);

	int count = __tm_JPanel.getWorksheetRowCount();
	String plural = "s";
	if (count == 1) {
		plural = "";
	}

	setMessageStatus("Displaying " + count + " record" + plural + ".", "Ready");

	setVisible(true);

	__tm_JPanel.setWorksheetColumnWidths();
}

}
