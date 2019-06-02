// TS_ListSelector_JFrame - a GUI for selecting a group of time series from a large list of time series.

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
// TS_ListSelector_JFrame - a GUI for selecting a group of time series from
//	a large list of time series.
// ----------------------------------------------------------------------------
// History:
//
// 2005-03-29	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-04	JTS, RTi		GUI revised to allow display of time
//					series as graph, summary, table, or
//					to initiate other actions, depending
//					on the values passed into the 
//					constructor in a PropList.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.TS;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;

/**
This class is a GUI for selecting time series from a list.
The class should be used as follows:<p><ul>
<li>declare an instance of this class that will display a list of time 
series</li>
<li>add all the TS_ListSelector_Listeners that should be informed when the 
user selects time series from the list and presses OK</li>
<li>when the user presses okay, theTS_ListSelector_Listeners will be 
notified.</li>
</ul>
<p><b>Example:</b><p>
<pre>
	PropList props = new PropList("TS_ListSelector_JFrame");
	props.add("ActionButtons=Graph,Table,Summary");
	TS_ListSelector_JFrame listGUI = new TS_ListSelector_JFrame(data,props);
	listGUI.addTSListSelectorListener(this);
</pre>
Buttons will be added for each "ActionButtons" value that is listed, and when
the button is pressed, the timeSeriesSelected() method will be called with the
list of selected time series.  The button label will be passed to the calling
code and can be interpreted as appropriate.
*/
@SuppressWarnings("serial")
public class TS_ListSelector_JFrame 
extends JFrame 
implements ActionListener, MouseListener {

/**
Button labels.  "Cancel", "Deselect All", and "Select All" are always present
on the GUI.  "OK" is only added if no ActionButtons are specified in the 
constructor PropList.
*/
private String
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK",
	__BUTTON_DESELECT_ALL = "Deselect All",
	__BUTTON_SELECT_ALL = "Select All";

/**
The worksheet that appears in the GUI.
*/
private JWorksheet __worksheet = null;

/**
The PropList passed to the constructor to control GUI behavior.
*/
private PropList __props = null;

/**
After a PropList is processed this contains the order of the buttons that
should appear on the GUI.
*/
private List<String> __buttonLabels = null;

/**
Once the GUI is set up, this contains the 
instantiated buttons so that they can be enabled or disabled as appropriate.
*/
private List<SimpleJButton> __buttons = null;

/**
The TS_ListSelector_Listener that have been registered to be informed when OK is pressed.
*/
private List<TS_ListSelector_Listener> __listeners = null;

/**
Constructor.  
@param data the list of time series to display in the worksheet.
@param props a PropList to control the behavior of the GUI.  Recognized 
properties are:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
	<td><b>Property</b></td>   
	<td><b>Description</b></td>   
	<td><b>Default</b></td>
</tr>

<tr>
	<td><b>ActionButtons</b></td>
	<td>Defines the buttons that will be present on the
	GUI.  The buttons appear at the bottom of the GUI between the 
	"Select All" and "Cancel" buttons, and are added in
	the order they appear in this property.<p>
	</ul><p>
	The following buttons will ALWAYS be present on the GUI:<p>
	<ul>
	<li>Deselect All -- deselects all selected time series in the list.</li>
	<li>Select all -- selects all time series in the list.</li>
	<li>Cancel -- closes the GUI.</li>
	</ul><p>
	If this property is not set, the GUI will contain the select/deselect
	buttons, a cancel button, and an OK button.
	<td>OK, Cancel</td>
</tr>
</table>
REVISIT (JTS - 2005-04-05)
Perhaps later add a "CloseButtons=OK" property that specifies the button that, 
when pressed, will cause the GUI to be closed.
*/
public TS_ListSelector_JFrame(List<? extends TS> data, PropList props) {
	super();
	
	processPropList(props);
	
	__listeners = new Vector<TS_ListSelector_Listener>();
	
	if (data == null) {
		setupGUI(new Vector<TS>());
	}
	else {
		setupGUI(data);	
	}
}

/**
Responds to action events.
@param event the event that happened.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();
	
	if (command.equals(__BUTTON_CANCEL)) {
		setVisible(false);
		dispose();
	}
	else if (command.equals(__BUTTON_DESELECT_ALL)) {
		__worksheet.deselectAll();
		enableButtons(false);
	}
	else if (command.equals(__BUTTON_SELECT_ALL)) {
		__worksheet.selectAllRows();
		enableButtons(true);
	}
	else if (event.getSource() instanceof SimpleJButton) {
		notifyListeners(getSelectedRows(), command);
	}
}

/**
Adds a TS_ListSelector_Listener.  These listeners are notified when the user
has selected some time series and pressed a button.
@param listener the listener to register.
*/
public void addTSListSelectorListener(TS_ListSelector_Listener listener) {
	__listeners.add(listener);
}

/**
Enables or disables the buttons on the form that are dependent on rows being selected.
@param enabled if true, the buttons will be enabled.  If false, they will be disabled.
*/
private void enableButtons(boolean enabled) {
	for ( SimpleJButton button: __buttons ) {
		button.setEnabled(enabled);
	}
}

/**
Gets the rows that are currently selected in the worksheet and returns them as
a non-null list of TS.  If 0 rows are selected, an empty list will be returned.
@return the rows selected in the worksheet.
*/
private List<TS> getSelectedRows() {
	List<TS> v = new Vector<TS>();
	int[] rows = __worksheet.getSelectedRows();
	for (int i = 0; i < rows.length; i++) {
		v.add((TS)__worksheet.getRowData(rows[i]));
	}

	return v;
}

/**
Returns the Vector of registered TS_ListSelector_Listeners.
@return the Vector of registered TS_ListSelector_Listeners.
*/
public List<TS_ListSelector_Listener> getTSListSelectorListeners() {
	return __listeners;
}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Does nothing.
*/
public void mousePressed(MouseEvent event) {}

/**
Enables or disabled buttons depending on whether any rows in the worksheet are
selected.
*/
public void mouseReleased(MouseEvent event) {
	boolean enabled = false;
	int num = __worksheet.getSelectedRowCount();
	if (num > 0) {
		enabled = true;
	}

	enableButtons(enabled);
}

/**
Notifies listeners that data have been selected.  
@param data the Vector of time series that were selected from the worksheet.
*/
private void notifyListeners(List<TS> data, String action) {
	int size = __listeners.size();
	TS_ListSelector_Listener l = null;
	for (int i = 0; i < size; i++) {
		l = __listeners.get(i);
		l.timeSeriesSelected(this, data, action);
	}
}

/**
Processes the PropList passed into the constructor.
@param props the PropList passed into the constructor.
*/
private void processPropList(PropList props) {
	__props = props;

	String s = __props.getValue("ActionButtons");
	__buttonLabels = new Vector<String>();
	if (s != null) {
		List<String> v = StringUtil.breakStringList(s, ",", 0);
		int size = v.size();
		for (int i = 0; i < size; i++) {
			s = v.get(i);
			__buttonLabels.add(s.trim());
		}
	}
	else {
		__buttonLabels.add(__BUTTON_OK);
	}
	
}

/**
Removes the specified TS_List_Selector_Listener from the registered list of
listeners.  Even if the listener was registered more than once, all instances 
of it are removed.
@param listener the listener to remove.
*/
public void removeTSListSelectorListener(TS_ListSelector_Listener listener) {
	int size = __listeners.size();
	TS_ListSelector_Listener l = null;
	for (int i = (size - 1); i >= 0; i--) {
		l = __listeners.get(i);
		if (l == listener) {
			__listeners.remove(i);
		}
	}
}

/**
Sets up the GUI.
@param data the Vector of TS to display in the worksheet.
*/
private void setupGUI(List<? extends TS> data) {
	TS_List_TableModel tableModel = new TS_List_TableModel((List<TS>)data);
	TS_List_CellRenderer cr = new TS_List_CellRenderer(tableModel);

	JPanel worksheetPanel = new JPanel();
	worksheetPanel.setLayout(new GridBagLayout());

	PropList props = new PropList("");
	props.set("JWorksheet.ShowRowHeader=true");
	props.add("JWorksheet.ShowPopupMenu=true");
	props.add("JWorksheet.SelectionMode=MultipleDiscontinuousRowSelection");

	JScrollWorksheet jsw = new JScrollWorksheet(cr, tableModel, props);
	__worksheet = jsw.getJWorksheet();
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);
	JGUIUtil.addComponent(worksheetPanel, jsw,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	getContentPane().add("Center", worksheetPanel);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridBagLayout());
	SimpleJButton deselectAll = new SimpleJButton(__BUTTON_DESELECT_ALL, this);
	SimpleJButton selectAll = new SimpleJButton(__BUTTON_SELECT_ALL, this);

	JGUIUtil.addComponent(buttonPanel, deselectAll,
		0, 0, 1, 1, 1, 0,
		2, 5, 2, 5,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(buttonPanel, selectAll,
		1, 0, 1, 1, 0, 0,
		2, 5, 2, 5,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	int size = __buttonLabels.size();
	SimpleJButton button = null;
	String s = null;
	List<SimpleJButton> v = new Vector<SimpleJButton>();
	for (int i = 0; i < size; i++) {
		s = __buttonLabels.get(i);
		button = new SimpleJButton(s, this);
		button.setEnabled(false);
		v.add(button);

		JGUIUtil.addComponent(buttonPanel, button,
			(2 + i), 0, 1, 1, 0, 0,
			2, 5, 2, 5,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
	}

	__buttons = v;

	SimpleJButton cancel = new SimpleJButton(__BUTTON_CANCEL, this);

	JGUIUtil.addComponent(buttonPanel, cancel,
		(2 + size), 0, 1, 1, 0, 0,
		2, 5, 2, 5,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	getContentPane().add("South", buttonPanel);

	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());

	pack();

	String app = JGUIUtil.getAppNameForWindows();
	String title = "Select Time Series";
	if (app == null) {
		setTitle(title);
	}
	else {
		setTitle(app + " - " + title);
	}

	JGUIUtil.center(this);

	setVisible(true);
}

}