//-----------------------------------------------------------------------------
// GeoViewAnimationJFrame - GUI for controlling a layer animation.
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 2004-08-03	J. Thomas Sapienza, RTi	Initial version.
// 2004-08-04	JTS, RTi		Threaded animation.
// 2004-08-05	JTS, RTi		Revised to use GeoViewAnimationData
//					objects.
// 2004-08-11	JTS, RTi		Due to change in the logic of how
//					animation layers are handled and
//					controlled, the GUI was reorganized
//					and changed so that it builds layers.
// 2004-08-12	JTS, RTi		Added support for 
//					GeoViewAnimationLayerData layer control.
// 2004-08-24	JTS, RTi		Corrected bug in Radio button selected
//					time series where by all the time 
//					series' data were being set to the same
//					value as the very last one.
// 2005-04-27	JTS, RTi		Added finalize().
// 2006-03-06	JTS, RTi		JToggleButtons are sized to be the same
//					size as the JButtons.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import RTi.GR.GRSymbol;

import RTi.TS.TS;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessListener;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.DateTimeBuilderJDialog;

/**
This class provides a gui for controlling the animation of animation layers
in geo view.
REVISIT (JTS - 2006-05-23)
How to use the animation stuff to animate data on a map?
*/
public class GeoViewAnimationJFrame
extends JFrame
implements ActionListener, ProcessListener, WindowListener {

/**
Button labels.
*/
private final String
	__BUTTON_ACCEPT = 	"Accept",
	__BUTTON_END = 		">>",
	__BUTTON_NEXT =		">",
	__BUTTON_PAUSE = 	"Pause",
	__BUTTON_PREV = 	"<",
	__BUTTON_RUN = 		"Start",
	__BUTTON_SET_DATE = 	"Set Current Date",
	__BUTTON_START = 	"<<",
	__BUTTON_STOP = 	"Stop";

/**
Dates used internally.  One is the current date -- this is the date for which
data are currently shown on the GUI.  The start and end date are the first
and last available dates of data in the time series.
*/
private DateTime 
	__currentDate = null,
	__endDate = null,
	__startDate = null;

/**
Data objects that control what is animated and how.
*/
private GeoViewAnimationData[] __data = null;

/**
The processor that actually runs the animations.
*/
private GeoViewAnimationProcessor __processor = null;

/**
The component that actually draws the map display.
*/
private GeoViewJComponent __viewComponent = null;

/**
The panel in which the GeoView is located in the main gui.
*/
private GeoViewJPanel __geoViewJPanel = null;

/**
The number of data objects being managed.
*/
private int __numData = -1;

/**
The interval between time steps in the animation.  Determined from the first
TS found.
*/
private int __interval = -1;

/**
GUI buttons.
*/
private JButton
	__acceptButton,
	__endButton,
	__nextButton,
	__prevButton,
	__setDateButton,
	__startButton,
	__stopButton;

/**
Array of textfields, each of which corresponds to a time series group.
The textfields hold the name for each group's layer.
*/
private JTextField[] __layerNameTextField;

/**
The text field that displays the current date of data being shown.
*/
private JTextField __currentTextField;

/**
Text field that holds the amount of time to pause between animation updates.
*/
private JTextField __pauseTextField = null;

/**
The status bar.
*/
private JTextField __statusBar = null;

/**
The button for starting a run.
*/
private JToggleButton
	__pauseButton,
	__runButton;

/**
GUI combo boxes for selecting the start and end dates of an animation.
*/
private SimpleJComboBox 
	__endComboBox,
	__startComboBox;

/**
Vector to hold all the layers that were added to the GeoView, so that they 
can be removed when the GUI is closed, if desired.
*/
private List __layers = null;

/**
Constructor.
@param parent the parent JFrame on which this gui was opened.
@param geoViewJPanel the panel in which the geoview is found on the main gui.
@param dataVector the Vector of GeoViewAnimationData objects that defines
how the GUI should be set up.
@param start the earliest date of data to animate.
@param end the last date of data to animate.
@throws NullPointerException if any of the parameters are null
@throws Exception if the data Vector is empty
*/
public GeoViewAnimationJFrame(JFrame parent, GeoViewJPanel geoViewJPanel,
List dataVector, DateTime start, DateTime end) 
throws Exception {
	super();

	if (parent == null || dataVector == null || start == null 
		|| end == null || geoViewJPanel == null) {
		throw new NullPointerException();
	}

	if (dataVector.size() == 0) {
		throw new Exception ("Empty data Vector");
	}

	__geoViewJPanel = geoViewJPanel;
	__viewComponent = __geoViewJPanel.getGeoView();

	setTitle("Animation Control");
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());

	// move the data objects into an array for better speed -- also, 
	// because objects won't be cast every time they are pulled from
	// the Vector, this will run faster

	__numData = dataVector.size();
	__data = new GeoViewAnimationData[__numData];
	for (int i = 0; i < __numData; i++) {
		__data[i] = (GeoViewAnimationData)dataVector.get(i);
	}

	dataVector = null;

	determineInterval();

	__startDate = new DateTime(start);
	__currentDate = new DateTime(__startDate);
	__endDate = new DateTime(end);

	setupGUI();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String routine = "GeoViewAnimationJFrame.actionPerformed";
	
	String action = event.getActionCommand();

	int e = __endComboBox.getSelectedIndex();
	int index = __startComboBox.indexOf(
		__currentDate.toString(DateTime.FORMAT_YYYY_MM));
	int s = __startComboBox.getSelectedIndex();
	int size = __startComboBox.getItemCount();

	if (action.equals(__BUTTON_ACCEPT)) {
		try {
			buildLayers();
		}
		catch (Exception ex) {
			Message.printWarning(1, routine, "Error building "
				+ "GeoView layers.");
			Message.printWarning(2, routine, ex);
			return;
		}

		// Diable all the setup parts of the GUI
		List groupNames = getGroupNames();
		int groupSize = groupNames.size();		
		for (int i = 0; i < groupSize; i++) {
			__layerNameTextField[i].setEditable(false);
		}
		for (int i = 0; i < __numData; i++) {
			if (__data[i].getJCheckBox() != null) {
				__data[i].getJCheckBox().setEnabled(false);
			}
			if (__data[i].getJRadioButton() != null) {
				__data[i].getJRadioButton().setEnabled(false);
			}
		}
	
		__acceptButton.setEnabled(false);
		__endButton.setEnabled(true);
		__nextButton.setEnabled(true);
		__prevButton.setEnabled(true);
		__setDateButton.setEnabled(true);
		__startButton.setEnabled(true);
		__runButton.setEnabled(true);
		__pauseTextField.setEditable(true);
		__endComboBox.setEnabled(true);
		__startComboBox.setEnabled(true);
		processStatus(1, "Ready, displaying data for "
			+ __currentDate.toString(DateTime.FORMAT_YYYY_MM));
	}
	else if (action.equals(__BUTTON_END)) {
		if (index == (size - 1)) {
			// already at the last date
			return;
		}

		__currentDate.addMonth(((size - 1) - index) * __interval);
		__currentTextField.setText(__currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		fillData(__currentDate);
		__endComboBox.select(0);
		__viewComponent.redraw();
	}
	if (action.equals(__BUTTON_NEXT)) {
		if (index >= e) {
			// already at or beyond the last date
			return;
		}

		__currentDate.addMonth(__interval);
		__currentTextField.setText(__currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		fillData(__currentDate);
		__viewComponent.redraw();
	}
	else if (action.equals(__BUTTON_PAUSE)) {
		if (__pauseButton.isSelected()) {
			__processor.pause(true);
		}
		else {
			__processor.pause(false);
		}
	}
	else if (action.equals(__BUTTON_PREV)) {
		if (index <= s) {
			// already at or before the first date
			return;
		}
		
		__currentDate.addMonth(-1 * __interval);
		__currentTextField.setText(__currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		fillData(__currentDate);
		__viewComponent.redraw();
	}
	else if (action.equals(__BUTTON_RUN)) {
		startAnimation();
		__endComboBox.setEnabled(false);
		__startComboBox.setEnabled(false);		
		__runButton.setEnabled(false);
		__startButton.setEnabled(false);
		__prevButton.setEnabled(false);
		__nextButton.setEnabled(false);
		__endButton.setEnabled(false);		
		__pauseButton.setEnabled(true);
		__stopButton.setEnabled(true);
	}
	else if (action.equals(__BUTTON_SET_DATE)) {
		PropList props = new PropList("");
		props.set("DatePrecision", "Month");
		new DateTimeBuilderJDialog(this, __currentTextField,
			__currentDate, props);
		try {
			__currentDate = DateTime.parse(
				__currentTextField.getText().trim());
		}
		catch (Exception ex) {}
		__startComboBox.select(__currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		fillData(__currentDate);
		__viewComponent.redraw();
	}
	else if (action.equals(__BUTTON_START)) {
		if (index == 0) {
			// already at the first date
			return;
		}
		__currentDate.addMonth(-1 * (index - 0) * __interval);
		__currentTextField.setText(__currentDate.toString(
			DateTime.FORMAT_YYYY_MM));
		fillData(__currentDate);
		__viewComponent.redraw();
		__startComboBox.select(0);
	}
	else if (action.equals(__BUTTON_STOP)) {
		__processor.cancel();
		__runButton.setSelected(false);
		__pauseButton.setSelected(false);
		__pauseTextField.setEditable(true);
		__runButton.setEnabled(true);
		__startButton.setEnabled(true);
		__prevButton.setEnabled(true);
		__nextButton.setEnabled(true);
		__endButton.setEnabled(true);		
		__stopButton.setEnabled(false);
		__pauseButton.setEnabled(false);
		__endComboBox.setEnabled(true);
		__startComboBox.setEnabled(true);				
	}

	else if (event.getSource() == __startComboBox) {
		if (s > e) {
			// if the start date was changed so that it is now
			// later than the end date, adjust the end date 
			// selection to be equal to the start date
			__endComboBox.select(s);
		}
		if (index < s) {
			__currentDate = new DateTime(__startDate);
			__currentDate.addMonth(s * __interval);
			__currentTextField.setText(__currentDate.toString(
				DateTime.FORMAT_YYYY_MM));		
			fillData(__currentDate);
			__viewComponent.redraw();
		}
	}
	else if (event.getSource() == __endComboBox) {
		if (e < s) {
			// if the end date was changed so that it is now
			// earlier than the start date, adjust the start date
			// selection to be equal to the end date
			__startComboBox.select(e);
		}
		if (index > e) {
			__currentDate = new DateTime(__startDate);
			__currentDate.addMonth(e * __interval);
			__currentTextField.setText(__currentDate.toString(
				DateTime.FORMAT_YYYY_MM));		
			fillData(__currentDate);
			__viewComponent.redraw();
		}
	}
}

/**
Called by the threaded animator when the animation has completed.
*/
protected void animationDone() {
	__runButton.setSelected(false);
	__viewComponent.redraw();
	JGUIUtil.forceRepaint(__viewComponent);
}

/**
Builds layers in the GeoView display as desired by the choices the user made in the GUI.
*/
private void buildLayers() 
throws Exception {
	double[] maxValues = null;
	GeoLayerView layerView = null;
	GeoViewAnimationData data = null;
	GeoViewAnimationLayerData layerData = null;
	int[] animationFields = null;
	int[] dataFields = null;
	int[] temp = null;
	int dataSize = -1;
	int size = -1;
	String layerName = null;
	List animationFieldsV = null;
	List dataV = null;
	List groupDataV = null;
	List maxValuesV = null;

	__layers = new Vector();

	List groupNames = getGroupNames();
	int groupSize = groupNames.size();		
	for (int i = 0; i < groupSize; i++) {
		processStatus(1, "Building map layer #" + (i + 1) + " of " + 
			groupSize);
		// go through each group and find out how many data items
		// are selected in each group.  For each group with at 
		// least one item selected, a layer will be built and placed
		// on the GeoView display.
		
		groupDataV = getGroupData((String)groupNames.get(i));

		// mark all group data items as not visible so they will not
		// be taken into account when filling data fields.  Later,
		// only the data that are actually going to be drawn on the
		// map will be marked as visible.
		size = groupDataV.size();
		for (int j = 0; j < size; j++) {
			data = (GeoViewAnimationData)groupDataV.get(j);
			data.setVisible(false);
		}
		
		dataV = findSelectedData(groupDataV);
		dataSize = dataV.size();

		// if none of the data items are selected, skip to the next
		// group

		if (dataSize == 0) {
			continue;
		}

		animationFieldsV = new Vector();
		maxValuesV = new Vector();

		// find out the animation fields and the max values that will
		// need to be passed in to addSummaryView for this layer.

		for (int j = 0; j < dataSize; j++) {
			data = (GeoViewAnimationData)dataV.get(j);
			data.setVisible(true);
			animationFieldsV.add( new Integer(data.getAttributeField()));
			maxValuesV.add(new Double(data.getAnimationFieldMax()));
		}

		// get the GeoViewAnimationLayerData object that tells much
		// about how this layer should be built

		layerData = ((GeoViewAnimationData)dataV.get(0)).getGeoViewAnimationLayerData();

		// build the animation fields array from the fields in the
		// data that are set as being animation fields

		size = animationFieldsV.size();
		animationFields = new int[size];
		for (int j = 0; j < size; j++) {
			animationFields[j] = ((Integer)animationFieldsV.get(j)).intValue();
		}

		temp = layerData.getDataFields();

		// dataFields are handled differently for different symbols.
		// For non-complicated symbols, the data fields specified in the
		// animation layer data are the fields that will always appear
		// on the display, regardless of whether they are animated or
		// not.  These fields will be combined with the fields stored
		// in the GeoViewAnimationData objects as animation fields.

		// For complicated symbols, such as teacups, the data fields
		// are used specially within addSummaryLayerView() to define
		// some settings.  For teacups, the first element is the field
		// with the maximum content of the teacup, the second element
		// is the field with the minimum content of the teacup, and
		// the third element is the field with the current content of
		// the teacup.

		if (layerData.getSymbolType() == GRSymbol.SYM_TEACUP) {
			dataFields = new int[temp.length];
			for (int j = 0; j < temp.length; j++) {
				dataFields[j] = temp[j];
			}
			animationFields = dataFields;
		}
		else {
			dataFields = new int[temp.length + size];
			for (int j = 0; j < temp.length; j++) {
				dataFields[j] = temp[j];
			}
			for (int j = temp.length; j < size; j++) {
				dataFields[j] 
					= animationFields[j - temp.length];
			}
		}

		// get out the maximum values for the animation fields

		size = maxValuesV.size();
		maxValues = new double[size];
		for (int j = 0; j < size; j++) {
			maxValues[j] = ((Double)maxValuesV.get(j)).doubleValue();
		}

		// if the user has set up an alternate layer name in the GUI
		// use it instead of the default one defined in GeoViewAnimationLayerData
		
		layerName = ((GeoViewAnimationData)dataV.get(0)).getLayerNameTextField().getText().trim();

		if (layerName.equals("")) {
			layerName = layerData.getLayerName();
		}

		// build the layer

		PropList props = layerData.getProps();
		/*
		if (props == null) {
			props = new PropList("");
		}
		props.add("PositiveBarColor.1 = Red");
		props.add("PositiveBarColor.2 = Yellow");
		props.add("NegativeBarColor.1 = Green");
		props.add("NegativeBarColor.2 = Blue");
		*/
		layerView = __geoViewJPanel.addSummaryLayerView(
			layerData.getTable(),
			layerData.getSymbolType(),
			layerName,
			layerData.getIDFields(),
			dataFields,
			layerData.getAvailAppLayerTypes(),
			layerData.getEqualizeMax(),
			animationFields,
			maxValues,
			props
		);

		// set some final settings on the layer

		layerView.setAnimationControlJFrame(this);	
		layerView.setMissingDoubleValue(
			layerData.getMissingDoubleValue());
		layerView.setMissingDoubleReplacementValue(
			layerData.getMissingDoubleReplacementValue());

		__layers.add(layerView);
	}
}

/**
Builds the time series panel from which different time series can be 
turned on or off in the animation layer.
@param panel the JPanel on which to build the GUI information.
*/
private void buildTimeSeriesPanel(JPanel panel) {
	String groupName = null;
	List dataNums = null;
	List groupNames = getGroupNames();
	int size = groupNames.size();
	__layerNameTextField = new JTextField[size];
	for (int i = 0; i < size; i++) {
		groupName = (String)groupNames.get(i);
		dataNums = getGroupDataNums(groupName);
		processGroup(panel, (i + 1), groupName, dataNums);
	}
}

/**
Determines the interval for the time series.  It does this by looking through
all the data objects for time series.  It takes the interval from the very 
first time series it finds.
@throws Exception if no valid interval could be found
*/
private void determineInterval() 
throws Exception {
	int num = 0;
	TS ts = null;
	for (int i = 0; i < __numData; i++) {
		num = __data[i].getNumTimeSeries();
		for (int j = 0; j < num; j++) {
			ts = __data[i].getTimeSeries(j);
			if (ts != null) {
				__interval 
					= ts.getIdentifier().getIntervalMult();
				return;
			}
		}
	}

	throw new Exception ("Could not find a valid time series from which "
		+ "to determine the interval");
}

/**
Populates the date combo boxes.
*/
private void fillComboBoxes() {	
	// REVISIT (JTS - 2004-08-04)
	// pretty much configured solely for months right now.
	// Will worry about hourly/etc TS later.  At least provides
	// support for doing different intervals right now

	DateTime d = new DateTime(__startDate);
	List v = new Vector();

	v.add(__startDate.toString(DateTime.FORMAT_YYYY_MM));
	
	d.addMonth(__interval);

	while (d.lessThan(__endDate)) {
		v.add(d.toString(DateTime.FORMAT_YYYY_MM));
		d.addMonth(__interval);
	}

	v.add(__endDate.toString(DateTime.FORMAT_YYYY_MM));
		
	__startComboBox.setData(v);
	__endComboBox.setData(v);
	__endComboBox.select(__endComboBox.getItemCount() - 1);

	__currentTextField.setText(
		__startDate.toString(DateTime.FORMAT_YYYY_MM));
}

/**
Fills attribute table data from time series for the given date.
@param date the date for which to put data into the attribute table.
*/
protected void fillData(DateTime date) {
	for (int i = 0; i < __numData; i++) {
		__data[i].fillData(date);
	}
}

/**
Cleans up member variables.
*/
public void finalize() 
throws Throwable {
	__currentDate = null;
	__endDate = null;
	__startDate = null;
	IOUtil.nullArray(__data);
	__processor = null;
	__viewComponent = null;
	__geoViewJPanel = null;
	__acceptButton = null;
	__endButton = null;
	__nextButton = null;
	__prevButton = null;
	__setDateButton = null;
	__startButton = null;
	__stopButton = null;
	IOUtil.nullArray(__layerNameTextField);
	__currentTextField = null;
	__pauseTextField = null;
	__statusBar = null;
	__pauseButton = null;
	__runButton = null;
	__endComboBox = null;
	__startComboBox = null;
	__layers = null;
	super.finalize();
}

/**
Given a Vector of GeoViewAnimationData objects, this searches it and returns
a Vector of all the data objects that have their radio button or check box
selected.
@param dataV a Vector of GeoViewAnimationData objects.  Can be null.
@return a Vector of all the GeoViewAnimationData objects in the passed-in 
Vector that have their radio button or check box selected.  Guaranteed to be non-null.
*/
private List findSelectedData(List dataV) {
	if (dataV == null || dataV.size() == 0) {
		return new Vector();
	}

	GeoViewAnimationData data = null;
	int size = dataV.size();
	List v = new Vector();

	for (int i = 0; i < size; i++) {
		data = (GeoViewAnimationData)dataV.get(i);

		// data objects can either use a checkbox for selection
		// or a radio button, but not both.  If one is null, the other must be non-null.

		if (data.getJCheckBox() != null 
			&& data.getJCheckBox().isSelected()) {
			v.add(data);
		}
		else if (data.getJRadioButton() != null
			&& data.getJRadioButton().isSelected()) {
			v.add(data);
		}
	}
	return v;
}

/**
Returns all the GeoViewAnimationData objects that have the given group name.
@param groupName the name of the group for which to return data.
@return a Vector of GeoViewAnimationData objects which have the given group
name.  This Vector may be empty if the group name is not matched, but it will never be null.
*/
private List getGroupData(String groupName) {
	List found = new Vector();
	for (int i = 0; i < __numData; i++) {
		if (groupName.equalsIgnoreCase(__data[i].getGroupName())) {
			found.add(__data[i]);
		}
	}
	return found;
}

/**
Returns all the data objects that have the given group name.  The group name
is compared without case sensitivity.
@param groupName the name of the group for which to return data objects.
@return a Vector of the data objects that match the group name.  Guaranteed
to be non-null.
*/
private List getGroupDataNums(String groupName) {
	List found = new Vector();
	for (int i = 0; i < __numData; i++) {
		if (groupName.equalsIgnoreCase(__data[i].getGroupName())) {
			found.add(new Integer(i));
		}
	}
	return found;
}

/**
Gets the names of all the data object groups.  Data group names are compared
case-insensitively.
@return a list of all the unique data object group names.
*/
private List getGroupNames() {
	boolean found = false;
	String s;
	List foundV = new Vector();
	for (int i = 0; i < __numData; i++) {
		found = false;
		for (int j = 0; j < foundV.size(); j++) {
			s = (String)foundV.get(j);
			if (s.equalsIgnoreCase(__data[i].getGroupName())) {
				found = true;
			}
		}

		if (!found) {
			foundV.add(__data[i].getGroupName());
		}
	}	

	return foundV;
}

/**
Adds a group section to the display.
@param panel the main panel on which the group display sections will be added.
@param panelY the y position (in a GridBagLayout) at which the current group 
section will be placed.
@param groupName the name of the group for which to add a section.
@param dataNums a Vector of Integers, each of which is the index within the
__data array of one of the members of the group to add to the panel.
*/
private void processGroup(JPanel panel, int panelY, String groupName, List dataNums) {
	// TODO SAM 2007-05-09 Evaluate if needed
	//boolean visible = false;
	ButtonGroup buttonGroup = new ButtonGroup();
	int dataNum = -1;
	int selectType = -1;
	int size = dataNums.size();
	JCheckBox checkBox = null;
	JRadioButton radioButton = null;
	JPanel subPanel = new JPanel();
	subPanel.setLayout(new GridBagLayout());
	subPanel.setBorder(BorderFactory.createTitledBorder(groupName));

	int y = 0;

	__layerNameTextField[panelY - 1] 
		= new JTextField(20);

	JGUIUtil.addComponent(subPanel, new JLabel("Layer Name: "),
		0, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(subPanel, __layerNameTextField[panelY - 1],
		1, y++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	for (int i = 0; i < size; i++) {
		dataNum = ((Integer)dataNums.get(i)).intValue();

		// take the type of selection (CheckBox or RadioButton)
		// from the very first data object for this group
		if (i == 0) {
			selectType = __data[dataNum].getSelectType();
		}

		if (selectType == GeoViewAnimationData.CHECKBOX) {
			checkBox = new JCheckBox((String)null,
				__data[dataNum].isVisible());
			__data[dataNum].setJCheckBox(checkBox);
		}
		else {
			// for radio buttons, the very first radio button 
			// is selected by default (so all the others are not)
			if (i == 0) {
				radioButton = new JRadioButton((String)null, 
					true);
			}		
			else {
				radioButton = new JRadioButton();
			}
			buttonGroup.add(radioButton);
			__data[dataNum].setJRadioButton(radioButton);
		}

		JGUIUtil.addComponent(subPanel, 
			new JLabel(__data[dataNum].getGUILabel()),
			0, y, 1, 1, 0, 0,
			GridBagConstraints.NONE, GridBagConstraints.EAST);

		if (selectType == GeoViewAnimationData.CHECKBOX) {
			JGUIUtil.addComponent(subPanel, checkBox,
				1, y, 1, 1, 0, 0,
				GridBagConstraints.NONE, 
				GridBagConstraints.WEST);
		}
		else {
			JGUIUtil.addComponent(subPanel, radioButton,
				1, y, 1, 1, 0, 0,
				GridBagConstraints.NONE, 
				GridBagConstraints.WEST);
		}
		/* TODO SAM 2007-05-09 Evaluate if needed
		if (selectType == GeoViewAnimationData.RADIOBUTTON) {
			if (i == 0) {
				visible = true;
			}		
			else {
				visible = false;
			}
		}
		else {
			visible = __data[dataNum].isVisible();
		}
		*/
		
		__layerNameTextField[panelY - 1].setText(
			__data[dataNum].getGeoViewAnimationLayerData()
			.getLayerName());
		__data[dataNum].setLayerNameTextField(
			__layerNameTextField[panelY - 1]);
		y++;
	}
	JGUIUtil.addComponent(panel, subPanel,
		0, panelY, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);	
}

/**
Handles error processing from the animation processor.  From ProcessListener.
@param error the error message that occurred.
*/
public void processError(String error) {}

/**
Handles output processing messages from the animation processor.  From 
ProcessListener.
@param output the output message that occurred.
*/
public void processOutput(String output) {}

/**
Handles status messages from the animation processor.  From ProcessListener.
@param code 0 for sending messages to the current date textfield, 1 for sending
messages to the status bar.
@param message the message to send.
*/
public void processStatus(int code, String message) {
	if (code == 0) {
		__currentTextField.setText(message);
		JGUIUtil.forceRepaint(__currentTextField);
	}
	else if (code == 1) {
		__statusBar.setText(message);
		JGUIUtil.forceRepaint(__statusBar);
	}
}

/**
Sets up the GUI.  Does not make the GUI visible, as developers must add
time series to the frame first, and then call setVisible() manually.
*/
private void setupGUI() {
	addWindowListener(this);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	int y = 0;

	__startComboBox = new SimpleJComboBox();
	__endComboBox = new SimpleJComboBox();
	__currentTextField = new JTextField(15);

	JPanel topPanel = new JPanel();
	topPanel.setLayout(new GridBagLayout());
	topPanel.setBorder(BorderFactory.createTitledBorder(
		"Animation Setup"));
	int topY = 0;

	JPanel bottomPanel = new JPanel();
	bottomPanel.setLayout(new GridBagLayout());
	bottomPanel.setBorder(BorderFactory.createTitledBorder(	
		"Animation Control"));

	JPanel timePanel = new JPanel();
	timePanel.setLayout(new GridBagLayout());
	timePanel.setBorder(BorderFactory.createTitledBorder(
		"Animation Times"));

	__pauseTextField = new JTextField("XXXXX");

	int ty = 0;

	JGUIUtil.addComponent(timePanel, new JLabel("Start:"),
		0, ty, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(timePanel, __startComboBox,
		1, ty++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__startComboBox.setEnabled(false);
	
	JGUIUtil.addComponent(timePanel, new JLabel("End:"),
		0, ty, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(timePanel, __endComboBox,
		1, ty++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__endComboBox.setEnabled(false);
	
	JGUIUtil.addComponent(timePanel, new JLabel("Current:"),
		0, ty, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(timePanel, __currentTextField,
		1, ty++, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__currentTextField.setEditable(false);

	__setDateButton = new JButton(__BUTTON_SET_DATE);
	__setDateButton.addActionListener(this);
	__setDateButton.setToolTipText("Set the current date.");
	JGUIUtil.addComponent(timePanel, __setDateButton,
		1, ty++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__setDateButton.setEnabled(false);

	JPanel timeSeriesPanel = new JPanel();
	timeSeriesPanel.setLayout(new GridBagLayout());
	buildTimeSeriesPanel(timeSeriesPanel);

	JPanel buttons = new JPanel();
	buttons.setLayout(new GridBagLayout());
	__startButton = new JButton(__BUTTON_START);
	__startButton.addActionListener(this);
	__startButton.setToolTipText("Set current date to start.");
	__startButton.setEnabled(false);
	__prevButton = new JButton(__BUTTON_PREV);
	__prevButton.addActionListener(this);
	__prevButton.setToolTipText("Go to previous date.");
	__prevButton.setEnabled(false);
	__runButton = new JToggleButton(__BUTTON_RUN);
	__runButton.addActionListener(this);
	__runButton.setToolTipText("Start animating at current date.");
	__runButton.setEnabled(false);
	__pauseButton = new JToggleButton(__BUTTON_PAUSE);
	__pauseButton.addActionListener(this);
	__pauseButton.setToolTipText("Pause at current date.");
	__pauseButton.setEnabled(false);
	__stopButton = new JButton(__BUTTON_STOP);
	__stopButton.addActionListener(this);
	__stopButton.setToolTipText("Stop at current date.");
	__stopButton.setEnabled(false);
	__nextButton = new JButton(__BUTTON_NEXT);
	__nextButton.addActionListener(this);
	__nextButton.setToolTipText("Go to next date.");
	__nextButton.setEnabled(false);
	__endButton = new JButton(__BUTTON_END);
	__endButton.addActionListener(this);
	__endButton.setToolTipText("Set current date to end.");
	__endButton.setEnabled(false);

	__acceptButton = new JButton(__BUTTON_ACCEPT);
	__acceptButton.addActionListener(this);
	__acceptButton.setToolTipText("Accept layer settings and set up "
		+ "GeoView display for animation.");

	int x = 0;
	JGUIUtil.addComponent(buttons, __startButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(buttons, __prevButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(buttons, __runButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(buttons, __pauseButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	JGUIUtil.addComponent(buttons, __stopButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(buttons, __nextButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(buttons, __endButton,
		x++, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
		
	timeSeriesPanel.setBorder(BorderFactory.createTitledBorder(
		"Time Series Data"));
	JGUIUtil.addComponent(topPanel, timeSeriesPanel,
		0, topY++, 2, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(topPanel, __acceptButton,
		3, topY++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, topPanel,
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	JGUIUtil.addComponent(bottomPanel, new JLabel("Pause (seconds):"),
		2, y, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
	JGUIUtil.addComponent(bottomPanel, __pauseTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(bottomPanel, timePanel,
		0, y++, 2, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(bottomPanel, buttons,
		0, y++, 10, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, bottomPanel,
		0, y++, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

	getContentPane().add("Center", panel);

	__statusBar = new JTextField("");
	__statusBar.setEditable(false);
	getContentPane().add("South", __statusBar);

	__pauseTextField.setText("5");
	__pauseTextField.setEditable(false);

	fillComboBoxes();

	__startComboBox.addActionListener(this);
	__endComboBox.addActionListener(this);

	JGUIUtil.center(this);

	pack();

	// By default, toggle buttons are smaller than normal JButtons.  The
	// following resizes them to be the same size.

	__pauseButton.setPreferredSize(
		new java.awt.Dimension(__pauseButton.getWidth(),
		__prevButton.getHeight()));
	__runButton.setPreferredSize(
		new java.awt.Dimension(__runButton.getWidth(),
		__prevButton.getHeight()));
	__pauseButton.setSize(
		new java.awt.Dimension(__pauseButton.getWidth(),
		__prevButton.getHeight()));
	__runButton.setSize(
		new java.awt.Dimension(__runButton.getWidth(),
		__prevButton.getHeight()));
	__pauseButton.setMinimumSize(
		new java.awt.Dimension(__pauseButton.getWidth(),
		__prevButton.getHeight()));
	__runButton.setMinimumSize(
		new java.awt.Dimension(__runButton.getWidth(),
		__prevButton.getHeight()));

	// put the fist date's data into the attribute table
	fillData(__startDate);
}

/**
Starts the threaded animation.
*/
public void startAnimation() {
	__pauseTextField.setEditable(false);

	String p = __pauseTextField.getText().trim();
	int millis = 1500;
	try {
		double d = (new Double(p)).doubleValue() * 1000;
		millis = (int)d;
	}
	catch (Exception e) {
		__pauseTextField.setText("1.5");
	}	
	
	int s = __startComboBox.getSelectedIndex();
	int e = __endComboBox.getSelectedIndex();
	int steps = (e - s) + 1;
	DateTime curr = new DateTime(__startDate);
	curr.addMonth(s * __interval);
	DateTime end = new DateTime(curr);
	end.addMonth(steps * __interval);
	
	if (__processor == null) {
		__processor = new GeoViewAnimationProcessor(
			this, __viewComponent, curr, end, millis);
		__processor.addProcessListener(this);
	}
	else {
		__processor.setStartDate(curr);	
		__processor.setEndDate(end);
		__processor.setPause(millis);
	}

	// REVISIT (JTS - 2004-08-11)
	// single thread ever?
	Thread thread = new Thread(__processor);
	thread.start();
}
	
/**
Forces the window to repaint.
*/
public void windowActivated(WindowEvent event) {
	invalidate();
	validate();
	repaint();
	if (__processor != null) { 
		try {
			__processor.sleep(200);
		}
		catch (Exception e) {}
	}
}

/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

/**
Removes layers from the GeoView if the user desires.
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
	if (__layers == null || __layers.size() == 0) {
		return;
	}

	if (__processor != null) {
		__processor.cancel();
	}

	ResponseJDialog r = new ResponseJDialog(this,
		"Remove Animation Layers?",
		"Should the animation layers that were added from this "
		+ "display be removed from the map?",
		ResponseJDialog.YES | ResponseJDialog.NO);
	int x = r.response();

	if (x == ResponseJDialog.NO) {
		return;
	}

	boolean redraw = false;
	GeoLayerView layerView = null;
	int size = __layers.size();
	for (int i = 0; i < size; i++) {
		layerView = (GeoLayerView)__layers.get(i);

		if (i == (size - 1)) {
			redraw = true;
		}
		else {
			redraw = false;
		}
		__geoViewJPanel.removeLayerView(layerView, redraw);
	}
	__viewComponent.redraw();
}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

}
