// ----------------------------------------------------------------------------
// ERDiagram_JPanel - A jpanel that can be added to frames that displays an
//	ER Diagram for a database connection.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-07-28	J. Thomas Sapienza, RTI	Initial version.
// 2003-08-11	JTS, RTi		Javadoc'd.
// 2003-08-27	JTS, RTi		Javadocs brought up to data again.
// 2004-01-19	Steven A. Malers, RTi	Change deprecated setDirty(boolean) call
//					to setDirty(boolean).
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.util.ArrayList;
import java.util.List;

import RTi.GR.GRLimits;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.Message.Message;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

/**
This class is a JPanel that displays an ER Diagram for a database.  It can be added to any JFrame.
*/
@SuppressWarnings("serial")
public class ERDiagram_JPanel extends JPanel
implements WindowListener {

/**
Whether debugging options have been turned on for the ERDiagram program.  These
options are indicated by additional menu items in the popup menu.
*/
private boolean __debug;

/**
The DMI connection that will be used to query meta information about the database and its tables.
*/
private DMI __dmi;

/**
The scaling factor to affect drawing.
*/
private double __scale = 0;

/**
The number of pixels to draw vertically.
*/
private double __vPixels;

/**
The device on which the drawing will take place.
*/
private ERDiagram_Device __device;

/**
The drawing area that the device will use for drawing the ER diagram.
*/
private ERDiagram_DrawingArea __drawingArea;

/**
The ERDiagram_JFrame in which this panel is located.
*/
private ERDiagram_JFrame __parent;

/**
Array of the table relationships that will be shown on the ER diagram.
*/
private ERDiagram_Relationship[] __rels;

/**
Array of the tables that will be shown on the ER Diagram.
*/
private ERDiagram_Table[] __tables;

/**
The number of elements in the __referenceTable list.
*/
private int __referenceTableCount;

/**
The scrollpane used to scroll around the panel.
*/
private JScrollPane __jsp;

/**
A reference to the text field in the parent JFrame that is on the left side of the status bar at the bottom.
*/
private JTextField __messageField;

/**
A reference to the status text field in the parent JFrame that is on the right
side of the status bar at the bottom.
*/
private JTextField __statusField;

/**
The format for the printed output of the ER diagram generation.
*/
private PageFormat __pageFormat;

/**
The name of the field in the table in the database that specifies where the tables' X positions are.
*/
private String __erdXField;

/**
The name of the field in the table in the database that specifies where the tables' Y positions are.
*/
private String __erdYField;

/**
The name of the table in the database that contains a list of the tables and
their positions in the ER Diagram (if DataTable not provided).
*/
private String __tablesTableName;

/**
The data table containing table layout information (if database table name not provided).
*/
private DataTable __tablesTable = null;

/**
The name of the field in the table in the database that specifies the names of the tables to put on the ER Diagram.
*/
private String __tableNameField = null;

/**
A list of the names of the reference tables in the ER diagram.  Will never be null.
*/
private List<String> __referenceTables;

/**
Constructor.
@param dmi an open and connected dmi that is hooked into the database for which the ER Diagram will be built.
@param tablesTableName the name of the table in the database that contains a list of all the tables.
@param tableNameField the name of the field within the above table that contains the names of the tables.
@param erdXField the name of the field within the above table that contains the X position of the tables in the ER Diagram.
@param erdYField the name of the field within the above table that contains the Y position of the tables in the ER Diagram.
@param pageFormat the pageFormat with which the page will be printed.
@param debug whether to turn on debugging options in the popup menu.
*/
public ERDiagram_JPanel(ERDiagram_JFrame parent, DMI dmi, DataTable tablesTable,
String tableNameField, String erdXField, 
String erdYField, List<String> referenceTables, PageFormat pageFormat, 
boolean debug)
{
	__dmi = dmi;
	__parent = parent;
	__debug = debug;

	// assumed for now
	__scale = .5;

	__tablesTable = tablesTable;
	__tableNameField = tableNameField;
	__erdXField = erdXField;
	__erdYField = erdYField;
	if (referenceTables == null) {
		__referenceTables = new ArrayList<String>();
	}
	else {
		__referenceTables = referenceTables;
	}
	__referenceTableCount = __referenceTables.size();

	__pageFormat = pageFormat;

	if (__debug) {
		System.out.println("width: " + pageFormat.getImageableWidth());
		System.out.println("height:" + pageFormat.getImageableHeight());
	}

	int widthPixels = (int)(pageFormat.getWidth() / __scale);
	int heightPixels = (int)(pageFormat.getHeight() / __scale);
	
	setupGUI(widthPixels, heightPixels, __scale);
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
public ERDiagram_JPanel(ERDiagram_JFrame parent, DMI dmi,
	String tablesTableName, String tableNameField, String erdXField, 
	String erdYField, List<String> referenceTables, PageFormat pageFormat, 
	boolean debug)
{
	__dmi = dmi;
	__parent = parent;
	__debug = debug;

	// assumed for now
	__scale = .5;

	__tablesTableName = tablesTableName;
	__tableNameField = tableNameField;
	__erdXField = erdXField;
	__erdYField = erdYField;
	if (referenceTables == null) {
		__referenceTables = new ArrayList<String>();
	}
	else {
		__referenceTables = referenceTables;
	}
	__referenceTableCount = __referenceTables.size();

	__pageFormat = pageFormat;

	if (__debug) {
		System.out.println("width: " + pageFormat.getImageableWidth());
		System.out.println("height:" + pageFormat.getImageableHeight());
	}

	int widthPixels = (int)(pageFormat.getWidth() / __scale);
	int heightPixels = (int)(pageFormat.getHeight() / __scale);
	
	setupGUI(widthPixels, heightPixels, __scale);
}

/**
Checks to see whether any of the tables' positions have been changed.
@return true if any table has been moved, otherwise return false.
*/
private boolean areTablesDirty() {
	for (int i = 0; i < __tables.length; i++) {
		if (__tables[i].isDirty()) {
			return true;
		}
	}
	return false;
}

/**
Called after windowClosing and windowClosed events; checks to see if the
positions of any of the tables have changed, and if so, prompts whether the
changes should be saved or not.
*/
protected void closeWindow() {
	if (areTablesDirty()) {
		ResponseJDialog dialog = new ResponseJDialog(
			getParentJFrame(),
			"Save changes?", "Changes have been made to the table "
			+ "positions.  Save changes?", 
			ResponseJDialog.YES | ResponseJDialog.NO);
		if (dialog.response() == ResponseJDialog.YES) {
			writeTables();
		}
	}
}

/**
Converts the list of relationships to remove all relationships that point
to reference tables (see the __referenceTables list), so that reference tables
appear to hover independent of all other tables.
@param rels the list of relationships to convert.
@return the converted relationships list.
*/
private List<ERDiagram_Relationship> convertReferenceRelationships(List<ERDiagram_Relationship> rels) {
	List<ERDiagram_Relationship> newRels = new ArrayList<ERDiagram_Relationship>();
	ERDiagram_Relationship rel;	
	for (int i = 0; i < rels.size(); i++) {	
		rel = rels.get(i);
		if (isReferenceTable(rel.getStartTable())
			&& isReferenceTable(rel.getEndTable())) {
//Message.printStatus(2, "", "Link from " + rel.getStartTable() + " to " 
//	+ rel.getEndTable() + ": both are refs, both removed.");
			rel.setReference(true);
			newRels.add(rel);
			rel.setNonReferenceTable(rel.getEndTable());
		}
		else if (isReferenceTable(rel.getStartTable())) {
//Message.printStatus(2, "", "Link from " + rel.getStartTable() + " to " 
//	+ rel.getEndTable() + ": removing " + rel.getStartTable());
			rel.setReference(true);
			newRels.add(rel);
			rel.setNonReferenceTable(rel.getEndTable());
		}
		else if (isReferenceTable(rel.getEndTable())) {
//Message.printStatus(2, "", "Link from " + rel.getStartTable() + " to " 
//	+ rel.getEndTable() + ": removing " + rel.getEndTable());
			rel.setReference(true);
			newRels.add(rel);
			rel.setNonReferenceTable(rel.getStartTable());
		}
		else {
			rel.setReference(false);
			newRels.add(rel);
		}
	}

	return newRels;
}

/**
Returns the device that is controlling all the drawing.
@return the device that is controlling all the drawing.
*/
public ERDiagram_Device getDevice() {
	return __device;
}

/**
Returns the DMI being used to connect to the database.
@return the DMI bring used to connect to the database.
*/
public DMI getDMI() {
	return __dmi;
}

/**
Returns the page format that was set up for this page.
@return the PageFormat that was set up for this page.
*/
public PageFormat getPageFormat() {
	return __pageFormat;
}

/**
Returns the ERDiagram_JFrame in which this panel is located.
@return the ERDiagram_JFrame in which this panel is located.
*/
protected ERDiagram_JFrame getParentJFrame() {
	return __parent;
}

/**
Returns the table from the __tables array that has the given name 
(using a case-sensitive comparison).
@param name the name of the table to return.
@return the table from __tables array with the given name, or null if none
match.
*/
private ERDiagram_Table getTableByName(String name) {
	for (int i = 0; i < __tables.length; i++) {
		if (__tables[i].getName().equals(name)) {			
			return __tables[i];
		}
	}
	return null;
}	

/**
Returns whether debugging was turned on.
@return whether debugging was turned on.
*/
protected boolean isDebug() {
	return __debug;
}

/**
Returns whether the table with the given name is a reference table or not.
@param name the name of the table to check (case sensitive).
@return true if the table is a reference table, false if not.
*/
private boolean isReferenceTable(String name) {
	String s = null;
	for (int i = 0; i < __referenceTableCount; i++) {
		s = (String)__referenceTables.get(i);
		if (s.equals(name)) {
			return true;
		}
	}
	return false;
}

/**
Returns true if the table with the specified name is in the __tables array.
@param tableName the name of the table to look for.
@return true if the table is in the __tables array, false otherwise.
*/
private boolean isTableInTablesArray(String tableName) {
	for (int i = 0; i < __tables.length; i++) {
		if (tableName.equals(__tables[i].getName())) {			
			return true;
		}
	}
	return false;
}

/**
Takes the list of relationship objects and removes all relationships for 
which either the start table or the end table is not visible.
@return the relationships list, minus all relationships connecting to 
non-visible tables.
*/
private List<ERDiagram_Relationship> pruneInvisibleRelationships(List<ERDiagram_Relationship> rels) {
	List<ERDiagram_Relationship> newRels = new ArrayList<ERDiagram_Relationship>();
	ERDiagram_Relationship rel;
	
	for (int i = 0; i < rels.size(); i++) {	
		rel = rels.get(i);
		if (isTableInTablesArray(rel.getStartTable())) {
			if (isTableInTablesArray(rel.getEndTable())) {
				if (getTableByName(rel.getStartTable()).isVisible() 
					&& getTableByName(rel.getEndTable()).isVisible()) {
						rel.setVisible(true);
				}
				newRels.add(rel);
			}
		}
	}

	return newRels;
}

/**
Calls the DMIUtil method to read in the list of relationships from the 
database and populate the array of ERDiagram_Relationship objects.
*/
protected ERDiagram_Relationship[] readRelationships() {
	setMessageStatus("Creating list of table relationships", "WAIT");
	List<ERDiagram_Relationship> rels = DMIUtil.createERDiagramRelationships(__dmi, null);

	rels = pruneInvisibleRelationships(rels);
	rels = convertReferenceRelationships(rels);
	__rels = new ERDiagram_Relationship[rels.size()];
	for (int i = 0; i < rels.size(); i++) {
		__rels[i] = rels.get(i);	
	}

	setMessageStatus("Done creating list", "READY");
	return __rels;
}

/**
Calls the DMIUtil method to read in the tables from the database and populate
the array of ERDiagram_Table object.
*/
protected ERDiagram_Table[] readTables()
{	String routine = getClass().getSimpleName() + ".readTables";
	setMessageStatus("Creating list of database tables", "WAIT");
	List<ERDiagram_Table> tables = new ArrayList<ERDiagram_Table>(0);
	// First read the table metadata from the database.  If the name of the table with database tables
	// is not specified, don't pass that to the following - coordinates for drawing won't be initialized.
	tables = DMIUtil.createERDiagramTables(__dmi, __tablesTableName, __tableNameField, __erdXField, __erdYField, null);
	// Next if a data table has been provided, get the X and Y coordinates from that table
	if ( __tablesTable != null ) {
		int nameCol = -1;
		try {
			nameCol = __tablesTable.getFieldIndex(__tableNameField);
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, e);
			nameCol = -1;
		}
		int xCol = -1;
		try {
			xCol = __tablesTable.getFieldIndex(__erdXField);
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, e);
			xCol = -1;
		}
		int yCol = -1;
		try {
			yCol = __tablesTable.getFieldIndex(__erdYField);
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, e);
			yCol = -1;
		}
		double x, y;
 		Object o = null;
		for ( ERDiagram_Table table : tables ) {
			// TODO SAM 2015-05-09 Implement robust error handling
			// Find a matching table name
			TableRecord rec = null;
			try {
				rec = __tablesTable.getRecord(nameCol, table.getName());
			}
			catch ( Exception e ) {
				Message.printWarning(3,routine,e);
				Message.printStatus(2,routine,"Could not match table name \"" + table.getName() + "\" in table list.");
				continue;
			}
			// Try to get the X and Y coordinates from the table
			try {
				o = rec.getFieldValue(xCol);
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, e);
				x = 0.0;
			}
			if ( o == null) {
				x = 0.0;
			}
			else {
				x = (Double)o;
			}
			try {
				o = rec.getFieldValue(yCol);
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, e);
				y = 0.0;
			}
			if ( o == null) {
				y = 0.0;
			}
			else {
				y = (Double)o;
			}
			table.setX(x);
			table.setY(y);
		}
	}
	Message.printStatus(2,"","Initialized " + tables.size() + " tables for diagram.");

	__tables = new ERDiagram_Table[tables.size()];
	for (int i = 0; i < tables.size(); i++) {
		__tables[i] = (ERDiagram_Table)tables.get(i);
	}
	setMessageStatus("Done creating list", "READY");
	return __tables;
}

/**
Sets the initial position of the scrollpane's viewport to be the lower-left
hand corner.
*/
protected void setInitialViewportPosition() {
	Point pt = new Point();
	pt.x = 0;
	pt.y = (int)(__vPixels 
		- (__jsp.getViewport().getExtentSize().getHeight()));
	__jsp.getViewport().setViewPosition(pt);
}

/**
Sets the message in the status bar of the parent JFrame.  This can be a 
short sentence as the message field is the longer one.  This can also be null,
in which case the message text field will be cleared.
@param text the text to put in the message field.
*/
public void setMessage(String text) {
	if (__messageField != null) {
		if (text != null) {
			__messageField.setText(text);
		}
		else {
			__messageField.setText("");
		}
		JGUIUtil.forceRepaint(__messageField);
	}
		
}

/**
Sets up the text fields that are in the status bar of the parent JFrame.
Either can be null.
@param messageField the longer text field.
@param statusField the shorted text field.
*/
protected void setMessageFields(JTextField messageField, 
JTextField statusField) {
	__messageField = messageField;
	__statusField = statusField;
}

/**
Sets up a message (which can be long) and a status message (which should 
generally be one word) in the status bar of the parent JFrame.
@param message the message to set in the status bar (can be null, in which 
case the message text field will be cleared).
@param status the status to set in the status bar (can be null, in which 
case the status text field will be cleared).
*/
public void setMessageStatus(String message, String status) {
	setMessage(message);
	setStatus(status);
}

/**
Sets the status message in the status bar of the parent JFrame.  This should
generally just be one word.  This can also be null, in which the case the
status text field will be cleared.
@param status the status to set in the status text field.
*/
public void setStatus(String status) {
	if (__statusField != null) {
		if (status != null) {
			__statusField.setText(status);
		} 
		else {
			__statusField.setText("");
		}
		JGUIUtil.forceRepaint(__statusField);		
	}
}

/**
Sets up the GUI.
@param hPixels the number of horizontal pixels in the drawing area
@param vPixels the number of vertical pixels in the drawing area
@param scale the scaling factor to adjust ppi by
*/
private void setupGUI(int hPixels, int vPixels, double scale) {
	JGUIUtil.setSystemLookAndFeel(true);

	readTables();
	readRelationships();

	__device = new ERDiagram_Device(__tables, __rels, this, scale);
	__device.setPreferredSize(new Dimension(hPixels, vPixels));
	GRLimits drawingLimits = new GRLimits(0.0, 0.0, hPixels, vPixels);
	__drawingArea = new ERDiagram_DrawingArea(__device, drawingLimits);
	__device.setDrawingArea(__drawingArea);

	setLayout(new GridBagLayout());
	__jsp = new JScrollPane(__device);
	__vPixels = vPixels;
	JGUIUtil.addComponent(this, __jsp,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.SOUTHWEST);
}

/**
Responds to window activated events; does nothing.
@param ev the WindowEvent that happened.
*/
public void windowActivated(WindowEvent ev) {}

/**
Responds to window closed events; calls closeWindow().
@param ev the WindowEvent that happened.
*/
public void windowClosed(WindowEvent ev) {
	closeWindow();
}

/**
Responds to window closing events; calls closeWindow().
@param ev the WindowEvent that happened.
*/
public void windowClosing(WindowEvent ev) {
	closeWindow();
}

/**
Responds to window deactivated events; does nothing.
@param ev the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent ev) {}

/**
Responds to window deiconified events; does nothing.
@param ev the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent ev) {}

/**
Responds to window iconified events; does nothing.
@param ev the WindowEvent that happened.
*/
public void windowIconified(WindowEvent ev) {}

/**
Responds to window opened events; does nothing.
@param ev the WindowEvent that happened.
*/
public void windowOpened(WindowEvent ev) {}

/**
Generates the SQL to write table positions back to the database.  Only tables
that have moved (they are dirty) have their positions saved.
*/
protected void writeTables() {
	setMessageStatus("Writing table changes to database", "WAIT");
	ERDiagram_Table table = null;
	String sql = null;
	for (int i = 0; i < __tables.length; i++) {
		table = __tables[i];
		if (table.isDirty()) {		
			sql = "UPDATE " + __tablesTableName + " SET " 
				+ __erdXField + " = " + table.getX()
				+ ", " + __erdYField + "= " + table.getY()
				+ " WHERE " + __tableNameField + " = '" 
				+ table.getName() + "'";
			try {
				__dmi.dmiWrite(sql);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			__tables[i].setDirty(false);
		}
	}
	setMessageStatus("Done writing table changes", "READY");
}

}
