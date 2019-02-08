// GeoViewAnimationData - data for controlling layer animation.

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
// GeoViewAnimationData - Data for controlling layer animation.
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 2004-08-05	JTS, RTi		Threaded animation.
// 2004-08-10	JTS, RTi		Added helper time series (mostly for
//					use with teacup displays).
// 2004-08-12	JTS, RTi		Added GeoViewAnimationLayerData.
// 2004-08-24	JTS, RTi		Added setVisible().
// 2005-04-27	JTS, RTi		Added all member variables to finalize()
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import RTi.TS.TS;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.Table.DataTable;

import RTi.Util.Time.DateTime;

/**
This class holds data that tell a GeoViewAnimationJFrame how to initialize 
and control animation for layers on a GeoView.  The purpose of the 
GeoViewAnimationData objects is to set up all the information for controlling
animation of a particular time series and also to tell the GUI how it should
set itself for display of that time series.
*/
public class GeoViewAnimationData {

/**
Determines whether a data group will have a radio button or a checkbox for
turning time series displays on the map on or off.  Radio button data
should all be set up to put their data in the same field in the attribute table,
because only one can be visible at a time.  Checkbox data should all have
separate fields for entering data.
*/
public final static int	
	CHECKBOX = 0,
	RADIOBUTTON = 1;

/**
Whether the time series managed by this object is initially visible in an 
animation.
*/
private boolean __visible = false;

/**
The attribute table in which the animation data are stored.  This is a reference
to the attribute table in the GeoLayer.
*/
private DataTable __table = null;

/**
The maximum value for the animation field (used with equalize max).
*/
private double __animationFieldMax = -1;

/**
The object that controls how the layer that may hold this animation data
will be built.
*/
private GeoViewAnimationLayerData __layerData = null;

/**
The number of the field in the attribute table that stores the data for this
object's time series -- this is the data that will be animated.
*/
private int __attributeField = -1;

/**
The number of the field in the attribute table that stores the date that 
was last animated.
*/
private int __dateField = -1;

/**
The fields into which time series helper data are placed.  The correspond to
the __helpers[] array.  
*/
private int[] __helperFields = null;

/**
The type of selection done to turn a time series visible or not on the GeoView.
Must be one of RADIOBUTTON or CHECKBOX.
*/
private int __selectType = 0;

/**
If using CHECKBOX as the selection type, the checkbox that corresponds to this
object's data.
*/
private JCheckBox __checkBox = null;

/**
If using RADIOBUTTON as the selection type, the radio button that corresponds
to this object's data.
*/
private JRadioButton __radioButton = null;

/**
The text field on the GUI that can be used to name this layer if the layer
name provided with the GeoViewAnimationLayerData is not the one desired.
*/
private JTextField __layerNameTextField = null;

/**
The name of the field in the attribute table that holds this object's time
series data.
*/
private String __attributeFieldName = null;

/**
The name of the field in the attribute table that holds this object's date
data.
*/
private String __dateFieldName = null;

/**
The name of the group to which this object belongs.  This is used to group
time series together on the GUI.  Example: "Reservoir EOM Time Series".
*/
private String __groupName = null;

/**
The label put next to this object's checkbox or radio button on the GUI.
Example: "Reservoir EOM: ".  
*/
private String __guiLabel = null;

/**
Helper time series that are necessary to fill in all the data in the table.
Because every GeoViewAnimationData object is used to create a section in 
the GUI from which a time series animation can be turned on or off, the helpers
are used to fill in additional data for displays which use more than one
data point to draw the animation.<p>
For example, for teacup displays three time series are used: <p>
<ol><li>Drawing the maximum fill level</li>
<li>Drawing the minimum fill level</li>
<li>Drawing the current fill level</li></ol>
The main time series (current fill level) is passed into the data object when
it is created.  The helper time series (max and min) are simply constant 
time series where every data value is the same, and are passed in as helpers.<p>
__helpers[] corresponds to the __helperFields[] array, while __helpers[][] 
corresponds to the rows in the attribute table.
*/
private TS[][] __helpers = null;

/**
The time series this data object manages.
*/
private TS[] __tsArray = null;

/**
Constructor.  Builds with a selection type of CHECKBOX.
@param layerData a GeoViewAnimationLayerData object that has information 
on how to construct the layer on which this data will appear.
@param tsList a Vector of time series that correspond to rows in the 
attribute table of the animation layer view.  Element X in this Vector 
corresponds to row X in the table.
@param attributeFieldName the name of the field in the attribute table where
the values from this object's time series will be placed.
@param dateFieldName the name of the field in the attribute table where
the date of the data last animated is stored.
@param guiLabel the String that will be placed on the animation control GUI
next to the data managed by this object.  
@param groupname the name of the group to which this data object belongs.
Group names are used to organize groups of time series on the GUI, and are
used with the selectionType to know whether to use checkboxes or radiobuttons 
to select among different time series.
@param visible whether this time series is initially visible on the display.
@param selectType the way by which users will choose among time series with
the same group name for which should be visible.  All of the data objects 
passed to the animation control GUI that have the same group name (which is
compared without case sensitivity) are placed in the same section of the GUI.
If selectionType is CHECKBOX, any data object's time series can be turned on
or off and not affect the visibility of other time series in the same group.
If selectionType is RADIOBUTTON, turning on one time series so that it is 
visible will make all the other time series in the same group not visible.  If
different objects in the same group have different selectTypes, the type that
will be used is the first one the GUI finds for a given group.
@param animationFieldMax the maximum value of the data in the animation field.
@throws NullPointerException if any of the parameters are null.
@throws Exception if the selectionType is not CHECKBOX or RADIOBUTTON.
@throws Exception if the attribute field name passed to the constructor
cannot be found in the layer view's attribute table.
@throws Exception if the date field name passed to the constructor
cannot be found in the layer view's attribute table.
@throws Exception if the number of rows in the attribute table does not equal
the size of the TS Vector passed to the constructor.
*/
public GeoViewAnimationData(GeoViewAnimationLayerData layerData,
List<TS> tsList, String attributeFieldName, String dateFieldName, 
String guiLabel, String groupName, boolean visible, int selectType,
double animationFieldMax) 
throws Exception {
	if (tsList == null || attributeFieldName == null 
		|| dateFieldName == null || guiLabel == null 
		|| groupName == null || layerData == null) {
		throw new NullPointerException();
	}

	if (selectType != CHECKBOX && selectType != RADIOBUTTON) {
		throw new Exception ("Invalid select value: " + selectType);
	}

	__layerData = layerData;

	// put into an array for faster access

	int size = tsList.size();
	__tsArray = new TS[size];
	for (int i = 0; i < size; i++) {
		__tsArray[i] = (TS)tsList.get(i);
	}

	tsList = null;
	
	__attributeFieldName = attributeFieldName;
	__dateFieldName = dateFieldName;
	__guiLabel = guiLabel;
	__groupName = groupName;
	__visible = visible;
	__selectType = selectType;
	__animationFieldMax = animationFieldMax;

	initialize();	
}

/**
Puts data into the attribute table from the time series for the given date.
@param date the date for which to put data into the attribute table.  Cannot
be null.
*/
protected void fillData(DateTime date) {
	if (!__visible) {
		// not the visible field (in radio button sections,
		// only one data can be visible at a time), so return
		return;
	}

	for (int i = 0; i < __tsArray.length; i++) {
		if (__tsArray[i] != null) {

		//if (i == 0 && __visible) {
		//	Message.printStatus(1, "", "'" + __guiLabel + "'");
		//	Message.printStatus(1, "", "   " + date + ": " 
		//		+ __tsArray[i].getDataValue(date));
		//}
		//else if (i == 0 && !__visible) {
		//	Message.printStatus(1, "", "--" + __guiLabel);
		//}

			try {
				__table.setFieldValue(
					i, __attributeField,
					new Double(__tsArray[i].getDataValue(
					date)));
			}
			catch (Exception e) {
				Message.printWarning(2, 
					"GeoViewAnimationData.fillData",
					"Error putting data into the attribute "
					+ "table in row " + i);
				Message.printWarning(2,
					"GeoViewAnimationData.fillData", e);
			}

			try {
				__table.setFieldValue(i, __dateField, date);
			}
			catch (Exception e) {
				Message.printWarning(2, 
					"GeoViewAnimationData.fillData",
					"Error putting date data into the "
					+ "attribute table in row " + i);
				Message.printWarning(2,
					"GeoViewAnimationData.fillData", e);
			}			
		}

		if (__helpers != null) {
			for (int j = 0; j < __helpers.length; j++) {
				if (__helpers[j][i] != null) {
				try {
				__table.setFieldValue(
					i, __helperFields[j],
					new Double(__helpers[j][i].getDataValue(
					date)));
				}
				catch (Exception e) {
//					e.printStackTrace();
				}
				}
			}
		}
	}
}

/**
Cleans up member data.
*/
public void finalize()
throws Throwable {
	__table = null;
	__layerData = null;
	__helperFields = null;
	__checkBox = null;
	__radioButton = null;
	__attributeFieldName = null;
	__dateFieldName = null;
	__groupName = null;
	__guiLabel = null;
	__helpers = null;
	IOUtil.nullArray(__tsArray);
	__layerNameTextField = null;
	super.finalize();
}

/**
Returns the maximum value for the animation field.  
@return the maximum value for the animation field.
*/
public double getAnimationFieldMax() {
	return __animationFieldMax;
}

/**
Returns the attribute field number.
@return the attribute field number.
*/
public int getAttributeField() {
	return __attributeField;
}

/**
Returns the name of the attribute field.
@return the name of the attribute field.
*/
public String getAttributeFieldName() {
	return __attributeFieldName;
}

/**
Returns the GeoViewAnimationLayerData object used to build the layer for
this animation.
@return the GeoViewAnimationLayerData object for this data.
*/
public GeoViewAnimationLayerData getGeoViewAnimationLayerData() {
	return __layerData;
}

/**
Returns the group name.
@return the group name.
*/
public String getGroupName() {
	return __groupName;
}

/**
Returns the GUI label.
@return the GUI label.
*/
public String getGUILabel() {
	return __guiLabel;
}

/**
Returns the JCheckBox this object uses.  If it uses a JRadioButton instead,
this will return null.
@return the JCheckBox this object uses.
*/
public JCheckBox getJCheckBox() {
	return __checkBox;
}

/**
Returns the JRadioButton this object uses.  If it uses a JCheckBox instead,
this will return null.
@return the JCheckBox this object uses.
*/
public JRadioButton getJRadioButton() {
	return __radioButton;
}

/**
Returns the textfield into which users can enter a name for the layer this
data appears in.
@return the textfield into which users can enter a name for the layer this
data appears in.
*/
protected JTextField getLayerNameTextField() {
	return __layerNameTextField;
}

/**
Returns the number of time series managed by this object.
@return the number of time series managed by this object.
*/
public int getNumTimeSeries() {
	if (__tsArray == null) {
		return 0;
	}
	return __tsArray.length;
}

/**
Returns this object's select type.
@return this object's select type.
*/
public int getSelectType() {
	return __selectType;
}

/**
Returns the given time series.
@param num the number of the time series to return.
@return the given time series.
*/
public TS getTimeSeries(int num) {
	return __tsArray[num];
}

/**
Initializes internal settings.
@throws Exception if the attribute field name passed to the constructor
cannot be found in the layer view's attribute table.
@throws Exception if the attribute date field name passed to the constructor
cannot be found in the layer view's attribute table.
@throws Exception if the number of rows in the attribute table does not equal
the size of the TS Vector passed to the constructor.
*/
private void initialize() 
throws Exception {
	__table = __layerData.getTable();
	__attributeField = __table.getFieldIndex(__attributeFieldName);
	__dateField = __table.getFieldIndex(__dateFieldName);
		// the above will throw an exception if the fields
		// are not found

	int rows = __table.getNumberOfRecords();
	int size = __tsArray.length;

	if (rows != size) {
		throw new Exception ("Mismatch in number of rows in attribute "
			+ "table (" + rows + ") and number of time series ("
			+ size + ").  Both values must be the same.");
	}
}

/**
Returns whether this object's time series is visible.
@return whether this object's time series is visible.
*/
public boolean isVisible() {
	return __visible;
}

/**
Sets a helper time series in place.
@param helperNum the number of the helper time series (base 0)
@param tsNum the time series to which this data is a helper (corresponds to
the rows in the attribute table).
@param ts the time series to place
@param field the field in the table to which the time series corresponds.
*/
public void setHelperTS(int helperNum, int tsNum, TS ts) {
	__helpers[helperNum][tsNum] = ts;
}

public void setHelperTSField(int helperNum, int field) {
	__helperFields[helperNum] = field;
}

/**
Sets the JCheckBox this object will use.
@param checkBox the checkBox this object will use.
*/
public void setJCheckBox(JCheckBox checkBox) {
	__checkBox = checkBox;
}

/**
Sets the JRadioButton this object will use.
@param radioButton the radiobutton this object will use.
*/
public void setJRadioButton(JRadioButton radioButton) {
	__radioButton = radioButton;
}

/**
Sets the textfield that is used by the GUI to set a name for the layer this
data appears in.
@param textField the textfield that users can enter a name for this layer in.
*/
protected void setLayerNameTextField(JTextField textField) {
	__layerNameTextField = textField;
}

/**
Sets the number of helper time series to be used.  The number is the number
of fields in the attribute table that will be filled by helper data.
@param num the number of helper time series to be used.
*/
public void setNumHelperTS(int num) {
	__helpers = new TS[num][__tsArray.length];
	__helperFields = new int[num];
}

/**
Sets whether the data object should be visible during the animation.  This 
method cannot be used to turn animation data on and off on a map layer -- it
is only used by the GeoViewAnimationJFrame in setting up all the data for 
animation once the user presses 'Accept'.
@param visible whether the data are visible or not.
*/
protected void setVisible(boolean visible) {
	__visible = visible;
}

}
