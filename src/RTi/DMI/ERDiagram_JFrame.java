// ----------------------------------------------------------------------------
// ERDiagram_JFrame - the abstract JFrame class off of which all the specific
//	database instances of JFrames are built.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-27	J. Thomas Sapienza, RTi	Initial changelog.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.print.PageFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.DMI.DMI;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Table.DataTable;

/**
This class is a JFrame inside of which is displayed the ER Diagram for a database.
*/
@SuppressWarnings("serial")
public //abstract
class ERDiagram_JFrame 
extends JFrame {

/**
The panel containing the ER diagram.
*/
private ERDiagram_JPanel __panel;

/**
The message field that appears at the bottom of the JFrame.
*/
private JTextField __messageField;

/**
The status field that appears at the bottom of the JFrame.
*/
private JTextField __statusField;

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which
the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains 
a list of all the tables.
@param tableNameField the name of the field within the above table that contains
the names of the tables.
@param erdXField the name of the field within the above table that contains the
X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the
Y position of the tables in the ER Diagram.
@param pageFormat the pageFormat with which the page will be printed.
*/
public ERDiagram_JFrame(DMI dmi, String tablesTableName, String tableNameField,
String erdXField, String erdYField, PageFormat pageFormat) {
	this (dmi, tablesTableName, tableNameField, erdXField, erdYField, null, pageFormat, false);
}

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which
the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains 
a list of all the tables.
@param tableNameField the name of the field within the above table that contains
the names of the tables.
@param erdXField the name of the field within the above table that contains the
X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the
Y position of the tables in the ER Diagram.
@param referenceTables a Vector of tables that will be marked as reference
tables.
@param pageFormat the pageFormat with which the page will be printed.
*/
public ERDiagram_JFrame(DMI dmi, String tablesTableName, String tableNameField,
String erdXField, String erdYField, List<String> referenceTables, 
PageFormat pageFormat) {
	this (dmi, tablesTableName, tableNameField, erdXField, erdYField, 
		referenceTables, pageFormat, false);
}

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which
the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains 
a list of all the tables.
@param tableNameField the name of the field within the above table that contains
the names of the tables.
@param erdXField the name of the field within the above table that contains the
X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the
Y position of the tables in the ER Diagram.
@param referenceTables a Vector of tables that will be marked as reference
tables.
@param pageFormat the pageFormat with which the page will be printed.
*/
public ERDiagram_JFrame(DMI dmi, String tablesTableName, String tableNameField,
String erdXField, String erdYField, PageFormat pageFormat, boolean debug) {
	this (dmi, tablesTableName, tableNameField, erdXField, erdYField, 
		null, pageFormat, debug);
}

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which
the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains 
a list of all the tables.
@param tableNameField the name of the field within the above table that contains
the names of the tables.
@param erdXField the name of the field within the above table that contains the
X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the
Y position of the tables in the ER Diagram.
@param pageFormat the pageFormat with which the page will be printed.
@param debug whether to turn on debugging options in the popup menu.  
*/
public ERDiagram_JFrame(DMI dmi, String tablesTableName, String tableNameField,
String erdXField, String erdYField, List<String> referenceTables, 
PageFormat pageFormat, boolean debug) {
	super(dmi.getDatabaseName());

	__panel = new ERDiagram_JPanel(this, dmi, tablesTableName, 
		tableNameField, erdXField, erdYField, referenceTables, 
		pageFormat, debug);
	
	setupGUI();

}

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which
the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains 
a list of all the tables.
@param tableNameField the name of the field within the above table that contains
the names of the tables.
@param erdXField the name of the field within the above table that contains the
X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the
Y position of the tables in the ER Diagram.
@param pageFormat the pageFormat with which the page will be printed.
@param debug whether to turn on debugging options in the popup menu.  
*/
public ERDiagram_JFrame(DMI dmi, DataTable tablesTable, String tableNameField,
String erdXField, String erdYField, List<String> referenceTables, 
PageFormat pageFormat, boolean debug)
{
	super(dmi.getDatabaseName());
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );

	__panel = new ERDiagram_JPanel(this, dmi, tablesTable, 
		tableNameField, erdXField, erdYField, referenceTables, 
		pageFormat, debug);
	
	setupGUI();
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(__panel);
	getContentPane().add(__panel);

	JPanel bottom = new JPanel();
	bottom.setLayout(new GridBagLayout());
	__statusField = new JTextField(10);
	__messageField = new JTextField(10);
	__statusField.setEditable(false);
	__messageField.setEditable(false);

	__panel.setMessageFields(__messageField, __statusField);

	SimpleJComboBox c = new SimpleJComboBox(true);
	c.add("10%");
	c.add("50%");
	c.add("100%");
	c.add("200%");
	c.select("100%");
	c.addActionListener(__panel.getDevice());
	__panel.getDevice().setScaleComboBox(c);
	getContentPane().add("North", c);

	JGUIUtil.addComponent(bottom, __messageField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	JGUIUtil.addComponent(bottom, __statusField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	getContentPane().add("South", bottom);	

	__messageField.setText("");
	__statusField.setText("READY");

	setSize(800, 600);
	setVisible(true);
	__panel.setInitialViewportPosition();
	//show();
}

// TODO SAM 2015-05-10 Calling this cases a StackOverflowException - call directly above
/**
Overrides the default JFrame setVisible(true) -- calls setInitialViewportPosition on the 
ERDiagram_JPanel to position the ER Diagram within its scroll pane.
*/
//public void show() {
//	setVisible(true);
//	__panel.setInitialViewportPosition();
//}

}
