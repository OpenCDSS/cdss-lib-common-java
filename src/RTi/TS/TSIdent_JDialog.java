// TSIdent_JDialog - dialog for editing the values in a TSIdent

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

// TODO SAM 2005-08-26
// * Need to add optional verification of fields (based on a "source" time
//	series)?  However, need to also support/allow wildcard entry from some uses.
// * Need to pass in available choices (e.g., data types).
// * Need to evaluate whether sub-fields are shown (e.g., for data type).

package RTi.TS;

import java.util.List;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;

/**
This class is a dialog for editing the values in a TSIdent.
The updated TSIdent object is returned via a response() method call.
The following is sample code for using it:
<code><pre>
	TSIdent tsident = ...;
	// tsident initialized and filled in
	...

	// create a dialog and allow users to change the TSIdent:
	TSIdent temp = (new TSIdent_JDialog(parentJFrame, true, tsident, null)).response();

	if (temp == null) {
		// user canceled the dialog
	}
	else {
		// user made changes -- accept them.
		tsident = temp;
	}
</pre></code>
*/
@SuppressWarnings("serial")
public class TSIdent_JDialog extends JDialog implements ActionListener, KeyListener, WindowListener
{

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

/**
Components to hold values from the TSIdent.
*/
private JTextField
    __locationTypeTextField = null,
	__locationTextField = null,
	__dataSourceTextField = null,
	__dataTypeTextField = null,
	__scenarioTextField = null,
	__sequenceIDTextField = null,
	__inputTypeTextField = null,
	__inputNameTextField = null;

private SimpleJComboBox __dataInterval_JComboBox = null;

private JTextArea __TSID_JTextArea = null;

/**
Dialog buttons.
*/
private SimpleJButton
	__cancelButton = null,
	__okButton = null;

private TSIdent
	__response = null, // TSIdent that is returned via response().
	__tsident = null; // TSIdent that is passed in.

private boolean __first_time = true; // Indicates if refresh() is being called the first time.
private boolean __error_wait = false; // Indicates if there is an error in input (true) from checkInput().
private String __warning = ""; // Warnings generated in refresh() and displayed in checkInput().

/**
Indicators for which parts of the identifier should be enabled and verified (default is true for all).
*/
private boolean __EnableLocationType_boolean = true;
private boolean __EnableLocation_boolean = true;
private boolean __EnableSource_boolean = true;
private boolean __EnableType_boolean = true;
private boolean __EnableInterval_boolean = true;
private boolean __EnableScenario_boolean = true;
private boolean __EnableSequenceID_boolean = true;
private boolean __EnableInputType_boolean = true;
private boolean __EnableInputName_boolean = true;

/**
Constructor.
@param parent the parent JFrame on which the dialog will appear.
This cannot be null.  If necessary, pass in an empty JFrame via:<p>
<code><pre>
	new TSident_JDialog(new JFrame(), ...);
</pre></code>
@param modal whether the dialog is modal.
@param tsident the TSIdent to edit.  Can be null, in which case <code>response()
</code> will return a new TSIdent filled with the values entered on the form.
The original instance will not modified so use the returned value to access the updated version.
@param props Properties to control the display and validation of the identifier, for example
to allow editing of partial identifier information, as follows:
<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>    <td><b>Description</b></td> <td><b>Default</b></td>
</tr>

<tr>
<td><b>EnableAll</b></td>
<td><b>Indicates (True/False) whether all fields should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableLocationType</b></td>
<td><b>Indicates (True/False) whether the location type data should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableLocation</b></td>
<td><b>Indicates (True/False) whether the location data should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableSource</b></td>
<td><b>Indicates (True/False) whether the data source should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableType</b></td>
<td><b>Indicates (True/False) whether the data type should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableScenario</b></td>
<td><b>Indicates (True/False) whether the scenario should be enabled/verified.</b>
<td>True</td>
</tr>

<tr>
<td><b>EnableSequenceNumber or EnableSequenceID</b></td>
<td><b>Indicates (True/False) whether the sequence ID should be enabled/verified.</b>
<td>True</td>
</tr>

</table>
*/
public TSIdent_JDialog(JFrame parent, boolean modal, TSIdent tsident, PropList props)
{	super(parent, modal);

	__tsident = tsident;
    if ( props == null ) {
        props = new PropList ("");
    }
    String enabled = props.getValue("EnableAll");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
        // All fields are to be disabled.
        __EnableLocationType_boolean = false;
        __EnableLocation_boolean = false;
        __EnableSource_boolean = false;
        __EnableType_boolean = false;
        __EnableInterval_boolean = false;
        __EnableScenario_boolean = false;
        __EnableSequenceID_boolean = false;
        __EnableInputType_boolean = false;
        __EnableInputName_boolean = false;
    }
    enabled = props.getValue("EnableLocationType");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableLocationType_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableLocationType_boolean = true;
    }
    enabled = props.getValue("EnableLocation");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableLocation_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableLocation_boolean = true;
    }
    enabled = props.getValue("EnableSource");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableSource_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableSource_boolean = true;
    }
    enabled = props.getValue("EnableType");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableType_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableType_boolean = true;
    }
    enabled = props.getValue("EnableInterval");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableInterval_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableInterval_boolean = true;
    }
    enabled = props.getValue("EnableScenario");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableScenario_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableScenario_boolean = true;
    }
    enabled = props.getValue("EnableSequenceID");
    if ( enabled == null ) {
        // Older...
        enabled = props.getValue("EnableSequenceNumber");
    }
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableSequenceID_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableSequenceID_boolean = true;
    }
    enabled = props.getValue("EnableInputType");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableInputType_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableInputType_boolean = true;
    }
    enabled = props.getValue("EnableInputName");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableInputName_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableInputName_boolean = true;
    }

	setupGUI();
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();

	if (s.equals(__BUTTON_CANCEL)) {
		response ( false );
	}
	else if (s.equals(__BUTTON_OK)) {
		refresh ();
		checkInputAndCommit ();
		if ( !__error_wait ) {
			response( true );
		}
	}
	else {
        // List.
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
This should be called before response() is allowed to complete.
*/
private void checkInputAndCommit () {
	String routine = getClass().getSimpleName() + ".checkInput";
    // Previously show all input to user, even if in error, but check before saving.
	__error_wait = false;
	__warning = "";
    String locationType = "";
    String location = "";
    String datasource = "";
    String datatype = "";
    String interval = "";
    String scenario = "";
    String sequenceID = "";
    String inputtype = "";
    String inputname = "";

    // Get from the dialog.

    locationType = __locationTypeTextField.getText().trim();
    location = __locationTextField.getText().trim();
    datasource = __dataSourceTextField.getText().trim();
    datatype = __dataTypeTextField.getText().trim();
    interval = __dataInterval_JComboBox.getSelected();
    scenario = __scenarioTextField.getText().trim();
    sequenceID = __sequenceIDTextField.getText().trim();
    inputtype = __inputTypeTextField.getText().trim();
    inputname = __inputNameTextField.getText().trim();
    String badChars;

    // Form the TSIdent string.

    __response = new TSIdent();

    // These would normally be in checkInput() but since the TSID is needed to output the string, also do the checks here.

    if ( (locationType.length() > 0) && StringUtil.containsAny(locationType, ".:", false) ) {
        __warning += "\nThe location type cannot contain a period (.) or colon (:).";
    }
    else {
        __response.setLocationType( locationType );
    }
    // Do not allow missing location.
    if ( location.length() == 0 ) {
        __warning += "\nThe location must be specified.";
    }
    else {
        badChars = TSIdent.SEPARATOR;
        if ( StringUtil.containsAny(location, badChars, false) ) {
            __warning += "\nThe location cannot contain:  " + badChars;
        }
        else {
            __response.setLocation( location );
        }
    }
    badChars = TSIdent.SEPARATOR;
    if ( StringUtil.containsAny(datasource, badChars, false) ) {
        __warning += "\nThe data source cannot contain:  " + badChars;
    }
    else {
        __response.setSource(datasource);
    }
    badChars = TSIdent.SEPARATOR;
    if ( StringUtil.containsAny(datatype, badChars, false) ) {
        __warning += "\nThe data type cannot contain:  " + badChars;
    }
    else {
        __response.setType(datatype);
    }
    // Do not allow missing interval.
    if ( __EnableInterval_boolean ) {
        if ( interval.length() == 0 ) {
            __warning += "\nA valid interval must be specified.";
        }
        else {
            try {
                __response.setInterval(interval);
            }
            catch (Exception e) {
                __warning += "\nThe interval \"" + interval + "\" is invalid.";
            }
        }
    }
    badChars = TSIdent.SEPARATOR;
    if ( StringUtil.containsAny(scenario, badChars, false) ) {
        __warning += "\nThe scenario cannot contain:  " + badChars;
    }
    else {
        __response.setScenario(scenario);
    }
    badChars = TSIdent.SEPARATOR + TSIdent.SEQUENCE_NUMBER_LEFT + TSIdent.SEQUENCE_NUMBER_RIGHT;
    if ( StringUtil.containsAny(sequenceID, badChars, false) ) {
        __warning += "\nThe sequence ID cannot contain:  " + badChars;
    }
    else {
        __response.setSequenceID(sequenceID);
    }
    badChars = TSIdent.SEPARATOR + TSIdent.TYPE_SEPARATOR;
    if ( StringUtil.containsAny(inputtype, badChars, false) ) {
        __warning += "\nThe input type (datastore) cannot contain:  " + badChars;
    }
    else {
        __response.setInputType(inputtype);
    }
    if ( StringUtil.containsAny(inputname, badChars, false) ) {
        __warning += "\nThe input name cannot contain:  " + badChars;
    }
    else {
        __response.setInputName(inputname);
    }
	if ( __warning.length() > 0 ) {
		__error_wait = true;
		__warning += "\n\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, __warning );
	}
}

/**
Does nothing.
*/
public void keyPressed(KeyEvent e) {
	refresh();
}

/**
Does nothing.
*/
public void keyReleased(KeyEvent e) {
	refresh();
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent e) {
}

/**
Refresh the full TSIdent displayed value, based on the individual values displayed in dialog.
*/
private void refresh () {
	String locationType = "";
	String location = "";
	String datasource = "";
	String datatype = "";
	String interval = "";
	String scenario = "";
	String sequenceID = "";
	String inputtype = "";
	String inputname = "";
	if ( __first_time ) {
		// Get the interface contents from the incoming TSIdent.
		__first_time = false;
		if ( __tsident != null ) {
		    locationType = __tsident.getLocationType();
			location = __tsident.getLocation();
			datasource = __tsident.getSource();
			datatype = __tsident.getType();
			interval = __tsident.getInterval();
			scenario = __tsident.getScenario();
			sequenceID = __tsident.getSequenceID();
			inputtype = __tsident.getInputType();
			inputname = __tsident.getInputName();
		}
		__locationTypeTextField.setText ( locationType );
		__locationTextField.setText ( location );
		__dataSourceTextField.setText ( datasource );
		__dataTypeTextField.setText ( datatype );
        if ( __EnableInterval_boolean ) {
            // Do case-insensitive select to avoid problems.
			try {
			    JGUIUtil.selectIgnoreCase(__dataInterval_JComboBox, interval );
			}
			catch ( Exception e ) {
			    __warning += "Interval \"" + interval + "\" is not recognized.";
			}
        }
		/* TODO SAM 2005-08-26
		 Add this for regular expressions, etc.?
		else {
			// Automatically add to the list at the top.
			__dataInterval_JComboBox.insertItemAt ( interval, 0 );
			// Select...
			__dataInterval_JComboBox.select ( interval );
		}
		*/
		__scenarioTextField.setText ( scenario );
		__sequenceIDTextField.setText ( sequenceID );
		__inputTypeTextField.setText ( inputtype );
		__inputNameTextField.setText ( inputname );
	}

	// Get from the dialog.

	locationType = __locationTypeTextField.getText().trim();
	location = __locationTextField.getText().trim();
	datasource = __dataSourceTextField.getText().trim();
	datatype = __dataTypeTextField.getText().trim();
	interval = __dataInterval_JComboBox.getSelected();
	scenario = __scenarioTextField.getText().trim();
	sequenceID = __sequenceIDTextField.getText().trim();
	inputtype = __inputTypeTextField.getText().trim();
	inputname = __inputNameTextField.getText().trim();

	// Display whatever is in the UI, even if incorrect.  Errors will be checked if OK is pressed.
	// This is somewhat redundant with TSIdent.toString() but need to handle as strings.
	if ( locationType.length() != 0 ) {
	    locationType = locationType + TSIdent.LOC_TYPE_SEPARATOR;
	}
	if ( scenario.length() != 0 ) {
	    scenario = TSIdent.SEPARATOR + scenario;
	}
    if ( sequenceID.length() != 0 ) {
        sequenceID = TSIdent.SEQUENCE_NUMBER_LEFT + sequenceID + TSIdent.SEQUENCE_NUMBER_RIGHT;
    }
    if ( inputtype.length() != 0 ) {
        inputtype = "~" + inputtype;
    }
    if ( inputname.length() != 0 ) {
        inputname = "~" + inputname;
    }
	__TSID_JTextArea.setText ( locationType + location + TSIdent.SEPARATOR + datasource + TSIdent.SEPARATOR +
	     datatype + TSIdent.SEPARATOR + interval + scenario + sequenceID + inputtype + inputname );
}

/**
Return the user response and dispose the dialog.
@return the dialog response.  If <code>null</code>, the user pressed Cancel.
*/
public void response ( boolean ok ) {
	setVisible(false);
	dispose();
	if ( !ok ) {
		__response = null;
	}
}

/**
Return the user response and dispose the dialog.
@return the dialog response.  If <code>null</code>, the user pressed Cancel.
*/
public TSIdent response () {
	return __response;
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	setTitle("Edit the Time Series Identifier (TSID)");

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	getContentPane().add("Center", panel);

	Insets insetsTLBR = new Insets(2,2,2,2);

	int fieldWidth = 30;
    __locationTypeTextField = new JTextField(fieldWidth);
    __locationTypeTextField.addKeyListener ( this );
	__locationTextField = new JTextField(fieldWidth);
	__locationTextField.addKeyListener ( this );
	__dataSourceTextField = new JTextField(fieldWidth);
	__dataSourceTextField.addKeyListener ( this );
	__dataTypeTextField = new JTextField(fieldWidth);
	__dataTypeTextField.addKeyListener ( this );
	__scenarioTextField = new JTextField(fieldWidth);
	__scenarioTextField.addKeyListener ( this );
    __sequenceIDTextField = new JTextField(fieldWidth);
    __sequenceIDTextField.addKeyListener ( this );
	__inputTypeTextField = new JTextField(fieldWidth);
	__inputTypeTextField.addKeyListener ( this );
	__inputNameTextField = new JTextField(fieldWidth);
	__inputNameTextField.addKeyListener ( this );

	int y = -1;
	JGUIUtil.addComponent(panel,
		new JLabel(" The time series identifier (TSID) uniquely identifies a time series, " +
		    "and provides key information about the time series."),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" The TSID conforms to the following convention, " +
			"where LocationType, Scenario and SequenceID are optional:"),
       0, ++y, 3, 1, 0, 0, insetsTLBR,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     [LocationType:]Location.DataSource.DataType.Interval.Scenario[SequenceID]"),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" For example:"),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
       "     123.NOAA.MeanTemp.Month  (example of interval with no multiplier)"),
       0, ++y, 3, 1, 0, 0, insetsTLBR,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     XYZ.USGS.Streamflow.24Hour  (example of interval with multipler)"),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
       "     123.USGS.Streamflow.15Minute.Raw  (example using a scenario)"),
       0, ++y, 3, 1, 0, 0, insetsTLBR,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
       "     123.USGS.Streamflow.6Hour.Raw[1950]  (example using a sequence ID for a trace in an ensemble, often start year)"),
       0, ++y, 3, 1, 0, 0,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
       "     Station:0451.NOAA.MeanTemp.Month  (example of location type)"),
       0, ++y, 3, 1, 0, 0, insetsTLBR,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel,
        new JLabel(" Location and DataType parts can internally use a dash (-) to " +
        	"indicate a sub-location and sub-type, and underscores are also useful as separators within a part."),
        0, ++y, 3, 1, 0, 0, insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel,
        new JLabel( "<html><b>&nbsp Periods can only be used in input type/name and datastore.  Do not use : in location parts.</b></html>"),
        0, ++y, 3, 1, 0, 0, insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __EnableInputName_boolean && __EnableInputType_boolean ) {
    	JGUIUtil.addComponent(panel,
    		new JLabel(" The input type and name (or datastore) indicate the format and storage location of data."),
    		0, ++y, 3, 1, 0, 0, insetsTLBR,
    		GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
	JGUIUtil.addComponent(panel,
		new JLabel(" Specify TSID parts below and the full TSID will automatically be created from the parts."),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    JLabel locationType_JLabel = new JLabel("Location type: ");
    JGUIUtil.addComponent(panel, locationType_JLabel,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    JGUIUtil.addComponent(panel, __locationTypeTextField,
        1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel,
        new JLabel(" Optional - use when location ID results in ambiguous TSID."),
        2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableLocationType_boolean) {
        locationType_JLabel.setEnabled(false);
        __locationTypeTextField.setEnabled(false);
    }

    JLabel location_JLabel = new JLabel("Location: ");
	JGUIUtil.addComponent(panel, location_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __locationTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Required - for example, a station, sensor, area, or process identifier."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableLocation_boolean) {
        location_JLabel.setEnabled(false);
        __locationTextField.setEnabled(false);
    }

    JLabel source_JLabel = new JLabel("Data source: ");
	JGUIUtil.addComponent(panel, source_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __dataSourceTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional - the source of the data (e.g., agency abbreviation)."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableSource_boolean) {
        source_JLabel.setEnabled(false);
        __dataSourceTextField.setEnabled(false);
    }

    JLabel type_JLabel = new JLabel("Data type: ");
	JGUIUtil.addComponent(panel, type_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __dataTypeTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional (recommended) - a data type abbreviation."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableType_boolean) {
        type_JLabel.setEnabled(false);
        __dataTypeTextField.setEnabled(false);
    }

    JLabel interval_JLabel = new JLabel("Data interval: ");
	JGUIUtil.addComponent(panel, interval_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__dataInterval_JComboBox = new SimpleJComboBox(false);
	List<String> intervalChoices = TimeInterval.getTimeIntervalChoices(
		TimeInterval.MINUTE, TimeInterval.YEAR, false, 1, true);
	if ( (__tsident == null) || (__tsident.getInterval().length() == 0) ) {
		// Add a blank at the beginning since nothing has been selected.
		intervalChoices.add ( 0, "" );
	}
	__dataInterval_JComboBox.setData ( intervalChoices );
	__dataInterval_JComboBox.addActionListener ( this );
	JGUIUtil.addComponent(panel, __dataInterval_JComboBox,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Required - data interval."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableInterval_boolean) {
        interval_JLabel.setEnabled(false);
        __dataInterval_JComboBox.setEnabled(false);
    }

    JLabel scenario_JLabel = new JLabel("Scenario: ");
	JGUIUtil.addComponent(panel, scenario_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __scenarioTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional - scenerio string (e.g., \"Hist\", \"Raw\", \"Filled\")."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableScenario_boolean) {
        scenario_JLabel.setEnabled(false);
        __scenarioTextField.setEnabled(false);
    }

    JLabel sequenceNumber_JLabel = new JLabel("Sequence ID: ");
    JGUIUtil.addComponent(panel, sequenceNumber_JLabel,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    JGUIUtil.addComponent(panel, __sequenceIDTextField,
        1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel(" Optional - sequence ID (e.g., for ensemble trace)."),
        2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableSequenceID_boolean) {
        sequenceNumber_JLabel.setEnabled(false);
        __sequenceIDTextField.setEnabled(false);
    }

    JLabel inputtype_JLabel = new JLabel("Input type (or datastore): ");
	JGUIUtil.addComponent(panel, inputtype_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __inputTypeTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional - input type or datastore (indicates database, data format)."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableInputType_boolean) {
        inputtype_JLabel.setEnabled(false);
        __inputTypeTextField.setEnabled(false);
    }

    JLabel inputname_JLabel = new JLabel("Input name: ");
	JGUIUtil.addComponent(panel, inputname_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __inputNameTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional - file name, for input type."),
		2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableInputName_boolean) {
        inputname_JLabel.setEnabled(false);
        __inputNameTextField.setEnabled(false);
    }

	JGUIUtil.addComponent(panel, new JLabel(" Time series identifier (TSID): "),
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__TSID_JTextArea = new JTextArea ( 4, 55 );
	__TSID_JTextArea.setLineWrap ( true );
	__TSID_JTextArea.setWrapStyleWord ( true );
	__TSID_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(panel, new JScrollPane(__TSID_JTextArea),
		1, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel south = new JPanel();
	south.setLayout(new FlowLayout(FlowLayout.RIGHT));

	__okButton = new SimpleJButton(__BUTTON_OK, this);
	__okButton.setToolTipText("Press this button to accept any changes made to the time series identifier.");
	__cancelButton = new SimpleJButton(__BUTTON_CANCEL, this);
	__cancelButton.setToolTipText("Press this button to reject any changes made to the time series identifier.");

	south.add(__okButton);
	south.add(__cancelButton);

	getContentPane().add("South", south);

	refresh(); // Refresh the TSID from the parts.
	pack();
	JGUIUtil.center(this);
	setVisible(true);
	JGUIUtil.center(this);
}

/**
Respond to WindowEvents.
@param event WindowEvent object.
*/
public void windowClosing(WindowEvent event) {
	__response = null;
	response ( false );
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent evt) {
}

/**
Does nothing.
*/
public void windowClosed(WindowEvent evt) {
}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent evt) {
}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent evt) {
}

/**
Does nothing.
*/
public void windowIconified(WindowEvent evt) {
}

/**
Does nothing.
*/
public void windowOpened(WindowEvent evt) {
}

}