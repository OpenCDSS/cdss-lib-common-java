// GeoViewSummaryFileJDialog - dialog for selecting fields from a summary file that will be used for the id field and the data fields

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

//-----------------------------------------------------------------------------
// GeoViewSummaryFileJDialog - Dialog for selecting the fields from a summary
//	file that will be used for the id field and the data fields.
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 2004-08-05	J. Thomas Sapienza, RTi	Initial version.
// 2004-08-07	JTS, RTi		Revised the threading model.
// 2004-08-09	JTS, RTi		Revised the GUI setup.
// 2005-04-27	JTS, RTi		* Added finalize().
//					* Replaced the MutableJList with a
//					  SimpleJList.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.SimpleJList;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTable_CellRenderer;
import RTi.Util.Table.DataTable_TableModel;
import RTi.Util.Table.TableField;

/**
This class is a small dialog that provides a preview of the table records plus
combo boxes from which the user can choose the fields they want to use as
the ID fields and the data fields.<p>
<b>Using this Class</b>
To use this class, simply instantiate it and let it run.  It's modal, so it 
will stop the thread it is called from.  After execution returns to the calling
method, get the id field value.  If it is null, the user pressed cancel.  
Otherwise, get the values and use them.<p>
<pre>
	GeoViewSummaryFileJDialog d = GeoViewSummaryFileJDialog(parent, 
		filename, tableFields, delimiter);
	if (d.cancelled()) {
		// user pressed cancel
		return;
	}
</pre>
*/
@SuppressWarnings("serial")
public class GeoViewSummaryFileJDialog 
extends JDialog 
implements ActionListener {

/**
Button labels.
*/
private final String 
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

/**
Whether the user pressed cancel to close the GUI or not.  True by default -- 
only set to false if they actually press OKAY.  This is to handle the times
when they close the GUI via the upper-right-hand X.
*/
private boolean __cancelled = true;

/**
Whether field maximums will be equalized for a layer.
*/
private boolean __equalizeMax = false;

/**
The table of data read from the file.  At most, 5 lines will be contained
in this table.
*/
private DataTable __table = null;

/**
The fields that the user selected as the data fields.
*/
private int[] __dataFields = null;

/**
The field that the user selected as the id field.
*/
private int[] __idFields = null;

/**
The checkbox for choosing to equalize maximum values.
*/
private JCheckBox __equalizeCheckBox;

/**
The parent JFrame.
*/
private JFrame __parent = null;

/**
The textfield for the layer name.
*/
private JTextField __layerNameTextField = null;

/**
A list for selecting the app layer types to include.
*/
private SimpleJList __list;

/**
Combo boxes for choosing fields.
*/
private SimpleJComboBox 
	__dataFieldsComboBox = null,
	__idFieldsComboBox = null;

/**
Name of the file from which data is read.
*/
private String __filename = null;

/**
Name of the layer.
*/
private String __layerName = null;

/**
The app layer views on the geo view display.
*/
private List<GeoLayerView> __appLayerViews = null;

/**
The app layer types the user selected.
*/
private List<String> __appLayerTypes = null;

/**
Constructor.
@param parent the parent JFrame on which this dialog is opened.
@param filename the name of the file to read the table from.  This is not
checked for validity.
@param tableFields the table fields that define the table.
@param delimiter the delimiter that separates fields in the table.
@param appLayers the names of the layers in the GeoView.
@throws Exception if there is an error reading the table file.
@throws NullPointerException if any parameter is null.
*/
public GeoViewSummaryFileJDialog(JFrame parent, String filename,
List<TableField> tableFields, String delimiter, List<GeoLayerView> appLayers) 
throws Exception {
	super(parent, true);
	
	if (parent == null || filename == null || tableFields == null 
		|| delimiter == null || appLayers == null) {
		throw new NullPointerException();
	}
	
	__table = DataTable.parseDelimitedFile(filename, delimiter, tableFields, 1, false, 6);
	__filename = filename;
	__appLayerViews = appLayers;

	String title = "Select Summary Fields";
	if (JGUIUtil.getAppNameForWindows() == null
		|| JGUIUtil.getAppNameForWindows().equals("")) {
		setTitle(title);
	}
	else {	
		setTitle(JGUIUtil.getAppNameForWindows() + " - " + title);
	}								

	setupGUI();	
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String action = event.getActionCommand();
	
	if (action.equals(__BUTTON_CANCEL)) {
		__idFields = null;		
		__dataFields = null;
		__appLayerTypes = null;
		__layerName = null;
		dispose();
	}
	else if (action.equals(__BUTTON_OK)) {	
		if (determineFieldValues()) {
			__cancelled = false;
			dispose();
		}
		else {
		/*
			new ResponseJDialog(this, "Invalid Field Range",
				"The range of fields in the data fields "
				+ "entry field is not valid.  Please "
				+ "correct.", 
				ResponseJDialog.OK).response();
		*/
		}
	}
	else if (event.getSource() == __dataFieldsComboBox) {
	}
	else if (event.getSource() == __idFieldsComboBox) {
	}
}

/**
Returns whether the user pressed CANCEL or not.
@return true if the user cancelled out of the GUI.  false if not.
*/
public boolean cancelled() {
	return __cancelled;
}

/**
Determines what the fields are the user selected from the combo boxes and 
fills in the __idFields and __dataFields variables appropriately.
@return true if the field values could be determined.  False if there was
a problem parsing them.
*/
private boolean determineFieldValues() {
	String routine = "GeoViewSummaryFileJDialog.determineFieldValues";
	
	// overarching try/catch will capture any bad integer parses, etc,
	// and return false. 
	try {
		__idFields = getRange(__idFieldsComboBox);
		if (__idFields == null) {
			return false;
		}
		
		__dataFields = getRange(__dataFieldsComboBox);
		if (__dataFields == null) {
			return false;
		}

	}
	catch (Exception e) {
		Message.printWarning(1, routine,
			"There was an error with the specified column ranges.");
		Message.printWarning(2, routine, e);
		return false;
	}

	__equalizeMax = __equalizeCheckBox.isSelected();

	__layerName = __layerNameTextField.getText();
	if (__layerName.trim().equals("")) {
		Message.printWarning(1, routine,
			"A layer name must be entered.");
		return false;
	}
	
	__appLayerTypes = __list.getSelectedItems();

	return true;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__table = null;
	__dataFields = null;
	__idFields = null;
	__equalizeCheckBox = null;
	__parent = null;
	__layerNameTextField = null;
	__list = null;
	__dataFieldsComboBox = null;
	__idFieldsComboBox = null;
	__filename = null;
	__layerName = null;
	__appLayerViews = null;
	__appLayerTypes = null;
	super.finalize();
}
/**
Returns the app layers the user selected.  If null, the user hit cancel.
@return the app layers the user selected.
*/
public List<String> getAppLayerTypes() {
	return __appLayerTypes;
}

/**
Returns the information for all the app layers on the GeoView display, 
formatted so that it can be placed into a Vector and selected.
@param layerViews list of layer views on the GeoView display.
@return a list of Strings describing the layer views and their join fields,
suitable for use in the list from which users select.
*/
public List<String> getAppLayersInfo(List<GeoLayerView> layerViews) {
	if (layerViews == null) {
		return new ArrayList<String>();
	}

	GeoLayer layer = null;
	GeoLayerView layerView = null;
	int size = layerViews.size();
	String joinFields = null;
	String s = null;
	List<String> joinFieldsList = null;
	List<String> v = new ArrayList<String>();	
	for (int i = 0; i < size; i++) {
		layerView = layerViews.get(i);
		layer = layerView.getLayer();
		s = layer.getAppLayerType() + " - " 
			+ layerView.getLegend().getText() + " - ";

		joinFields = layerView.getPropList().getValue("AppJoinField");
		if (joinFields == null) {
			s += "[No join field]";
		}
		else {
			joinFieldsList = StringUtil.breakStringList(
				joinFields, ",", 0);
			for (int j = 0; j < joinFieldsList.size(); j++) {
				if (j != 0) {
					s += ", ";
				}
				s += joinFieldsList.get(j);
			}
		}
		v.add(s);
	}
	return v;
}

/**
Returns the data fields the user selected.  If null, the user hit cancel.
@return the data fields the user selected.
*/
public int[] getDataFields() {
	return __dataFields;
}

/**
Returns whether field values will be equalized for a layer.
@return whether field values will be equalized for a layer.
*/
public boolean getEqualizeMax() {
	return __equalizeMax;
}

/**
Returns the id field the user selected.  If null, the user hit cancel.
@return the id field the user selected.
*/
public int[] getIDFields() {
	return __idFields;
}

/**
Returns the name of the layer.  If null, the user hit cancel.
@return the name of the layer.
*/
public String getLayerName() {
	return __layerName;
}

/**
Returns the range of values selected in a combo box.  Users can either choose
one of the items from the combo box, in which case only a single field is
selected, or they can provide a series of numbers where ranges are specified
by commas and dashes.
@param comboBox the comboBox to check to return the selected fields.
@return an int array where each array position contains the number of a field
that was selected.  No field number will be included in the array more than
once.  If null, an error occurred.
@throws Exception if there are any errors getting the fields.
*/
private int[] getRange(SimpleJComboBox comboBox) 
throws Exception {
	// Data fields
	String dataS = comboBox.getSelected();
	int index = dataS.indexOf("(");
	if (index > -1) {
		dataS = dataS.substring(0, index).trim();
	}

	boolean[] fields = new boolean[__table.getNumberOfFields() + 1];
	for (int i = 0; i < fields.length; i++) {
		fields[i] = false;
	}
	
	List<String> v = StringUtil.breakStringList(dataS, ",", 0);

	// basically, first parse out all the comma-separated values in
	// the string.  Then, check each one to see if it's actually a 
	// field range (has a dash).  Set the field[] value to true if
	// the field is specified anywhere, in a range or in the comma
	// list.  Ranges can overlap previously-specified numbers and
	// vice versa with no ill effect.

	int size = v.size();
	String s = null;
	List<String> v2 = null;
	String ss1 = null;
	String ss2 = null;
	int i1 = -1;
	int i2 = -1;
	for (int i = 0; i < size; i++) {
		s = v.get(i);
		s = s.trim();
		if (s.indexOf("-") > -1) {
			v2 = StringUtil.breakStringList(s, "-", 0);
			if (v2.size() != 2) {
				return null;
			}
			ss1 = v2.get(0).trim();
			ss2 = v2.get(1).trim();

			i1 = Integer.decode(ss1).intValue();
			i2 = Integer.decode(ss2).intValue();

			if (i1 <= 0 || i2 <= 0) {
				return null;
			}

			for (int j = i1; j <= i2; j++) {
				fields[j] = true;
			}
		}
		else {
			i1 = Integer.decode(s).intValue();
			if (i1 <= 0) {
				return null;
			}
			fields[i1] = true;
		}
	}

	size = 0;
	for (int i = 1; i < fields.length; i++) {
		if (fields[i]) {
			size++;
		}
	}

	int count = 0;
	int[] finalFields = new int[size];
	for (int i = 1; i < fields.length; i++) {
		if (fields[i]) {
			finalFields[count++] = i - 1;
		}
	}

	return finalFields;
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	String routine = "GeoViewSummaryFileJDialog.setupGUI";

	JPanel panel = new JPanel();	
	panel.setLayout(new GridBagLayout());

	List<String> v = new ArrayList<String>();
	int fieldCount = __table.getNumberOfFields();	
	String fieldName = null;
	for (int i = 0; i < fieldCount; i++) {
		fieldName = __table.getFieldName(i);
		if (fieldName == null || fieldName.trim().equals("")) {
			v.add("" + (i + 1));
		}
		else {
			v.add("" + (i + 1) + " (" + fieldName + ")");
		}
	}

	__dataFieldsComboBox = new SimpleJComboBox(v, true);
	__idFieldsComboBox = new SimpleJComboBox(v, true);
	__equalizeCheckBox = new JCheckBox((String)null, true);
	__layerNameTextField = new JTextField(15);

	int y = 0;
	
	JPanel temp1 = new JPanel();
	temp1.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(temp1, new JLabel("Layer name: "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp1, __layerNameTextField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(panel, temp1,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel,
	new JLabel("Select the columns that will be used for the summary layer "
		+ "display."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
	new JLabel("The ID columns will be used to relate data to "
		+ "specific features on the map."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
	new JLabel("The data columns contain numerical data that will be "
		+ "shown as symbols."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
	new JLabel("To select one data column, choose it from the list."),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
	new JLabel("To enter multiple columns, specify as "
		+ "comma-separated numeric values or as a "),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel("pair of numbers separated by a dash.  e.g:"),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel("     1,3,4-10,15"),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel("     11-19, 12, 13, 1"),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(""),
		0, y++, 2, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel temp2 = new JPanel();
	temp2.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(temp2, new JLabel("ID Column(s): "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp2, __idFieldsComboBox,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp2, new JLabel(
		"    The numbers of the columns to be used for ID matching."),
		2, 0, 1, 1, 1, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, temp2,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel temp3 = new JPanel();
	temp3.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(temp3, new JLabel("Data Column(s): "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp3, __dataFieldsComboBox,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp3, new JLabel(
		"    The numbers of the columns with data values to plot."),
		2, 0, 1, 1, 1, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, temp3,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel temp4 = new JPanel();
	temp4.setLayout(new GridBagLayout());

	JGUIUtil.addComponent(temp4, new JLabel("Equalize max values: "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp4, __equalizeCheckBox,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(temp4, new JLabel(
		"    Whether all layer data should be plotted to the same "
		+ "maximum value."),
		2, 0, 1, 1, 1, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, temp4,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	PropList props = new PropList("JWorksheet");
	props.add("JWorksheet.ShowPopupMenu=true");
	props.add("JWorksheet.SelectionMode=ExcelSelection");
	props.add("JWorksheet.AllowCopy=true");
	props.add("JWorksheet.ColumnNumbering=Base1");

	JScrollWorksheet jsw = null;
	JWorksheet worksheet = null;
	try {
		DataTable_TableModel tm = new DataTable_TableModel(__table);
		DataTable_CellRenderer cr = new DataTable_CellRenderer(tm);
	
		jsw = new JScrollWorksheet(cr, tm, props);
		worksheet = jsw.getJWorksheet();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		jsw = new JScrollWorksheet(0, 0, props);
		worksheet = jsw.getJWorksheet();
	}
	worksheet.setPreferredScrollableViewportSize(null);
	worksheet.setHourglassJFrame(__parent);

	JLabel label = null;
	int num = __table.getNumberOfRecords();
	if (num == 1) {
		label = new JLabel("Only row of " + __filename);
	}
	else {
		label = new JLabel("First " + num + " rows of " + __filename);
	}
	JGUIUtil.addComponent(panel, label,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);	
	JGUIUtil.addComponent(panel, jsw, 
		0, y++, 10, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	List<String> layerInfo = getAppLayersInfo(__appLayerViews);
	__list = new SimpleJList(layerInfo);
	__list.setVisibleRowCount(4);

	JLabel label2 = new JLabel("Available layer types, names, "
		+ "and join fields:");
	JGUIUtil.addComponent(panel, label2,
		0, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JScrollPane(__list),
		0, y++, 10, 1, 0, 0,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);

	JPanel buttons = new JPanel();
	buttons.setLayout(new GridBagLayout());

	JButton ok = new JButton(__BUTTON_OK);
	ok.addActionListener(this);
	JGUIUtil.addComponent(buttons, ok,
		0, 0, 1, 1, 1, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JButton cancel = new JButton(__BUTTON_CANCEL);
	cancel.addActionListener(this);
	JGUIUtil.addComponent(buttons, cancel,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	JGUIUtil.addComponent(panel, buttons,
		1, y++, 1, 1, 1, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	getContentPane().add(panel, "Center");	

	__dataFieldsComboBox.addActionListener(this);
	__idFieldsComboBox.addActionListener(this);
	pack();
	setSize(getWidth(), getHeight() + 125);
	JGUIUtil.center(this);
	setVisible(true);
}

}
