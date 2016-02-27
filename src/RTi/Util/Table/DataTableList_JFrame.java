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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import RTi.Util.GUI.JGUIUtil;

/**
This class is the frame in which a tabbed pane displays multiple DataTable data, each in a JWorksheet is displayed.
*/
public class DataTableList_JFrame extends JFrame implements ChangeListener
{

/**
The data tables that were passed in.
*/
private List<DataTable> __tableList = null;

/**
Tab labels for each table.
*/
private String [] __tabLabels = null;

/**
The panel containing the worksheet that will be displayed in the frame.
*/
private List<DataTable_JPanel> __dataTablePanelList = new ArrayList<DataTable_JPanel>();

/**
Message bar text fields.
*/
private JTextField __messageJTextField;
private JTextField __statusJTextField;

/**
Constructor.
@param title the title to put on the frame.
@throws Exception if table is null.
*/
public DataTableList_JFrame(String title, String [] tabLabels, List<DataTable> tableList) 
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
	__tableList = tableList;
	__tabLabels = tabLabels;
	
	setupGUI();
}

/**
Constructor.
@param title the title to put on the frame.
@param filename the name of the file to be read and displayed in the worksheet.
@throws Exception if filename is null.
*/
public DataTableList_JFrame(String title, String filename) 
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
	
	setupGUI();
}

/**
Sets the status bar's message and status text fields.
@param message the value to put into the message text field.
@param status the value to put into the status text field.
*/
public void setMessageStatus(String message, String status) {
	if ( (message != null) && (__messageJTextField != null) ) {
		__messageJTextField.setText(message);
	}
	if ( (status != null) && (__statusJTextField != null) ) {
		__statusJTextField.setText(status);
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() 
throws Exception {
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout( new GridBagLayout() );
	getContentPane().add(mainPanel);
	// Add a tabbed pane and within that tabs for each data table
	JTabbedPane mainTabbedPane = new JTabbedPane();
	mainTabbedPane.addChangeListener(this);
    JGUIUtil.addComponent(mainPanel, mainTabbedPane,
        0, 0, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	//mainPanel.add(mainTabbedPane);
	int iTab = -1;
	for ( DataTable table: __tableList ) {
		++iTab;
		DataTable_JPanel panel = new DataTable_JPanel(this, table);
		__dataTablePanelList.add(panel);
		// TODO SAM 2016-02-27 Would be nice here to set tool tips on columns to help understand content
		// - could pass into constructor
		mainTabbedPane.addTab(__tabLabels[iTab],panel);
	}

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
	JGUIUtil.center(this);

	tabClicked(__dataTablePanelList.get(0));
	setVisible(true);

	for ( DataTable_JPanel p : __dataTablePanelList ) {
		p.setWorksheetColumnWidths();
	}
}

/**
Event handler for tab selection.
*/
public void stateChanged(ChangeEvent e) {
	JTabbedPane sourceTabbedPane = (JTabbedPane)e.getSource();
    int index = sourceTabbedPane.getSelectedIndex();
    tabClicked(__dataTablePanelList.get(index));
}

/**
Call when a tab is clicked.
*/
private void tabClicked ( DataTable_JPanel panel) {
	int countRows = panel.getWorksheetRowCount();
	int countCols = panel.getWorksheetColumnCount();
	setMessageStatus("Displaying " + countRows + " rows, " + countCols + " columns.", "Ready");
}

}