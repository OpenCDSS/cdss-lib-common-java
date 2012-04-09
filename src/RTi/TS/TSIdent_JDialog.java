// TODO SAM 2005-08-26
// * Need to add optional verification of fields (based on a "source" time
//	series)?  However, need to also support/allow wildcard entry from some
//	uses.
// * Need to pass in available choices (e.g., data types).
// * Need to evaluate whether sub-fields are shown (e.g., for data type).
// * Need to add sequence number for traces.

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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;

/**
This class is a dialog for editing the values in a TSIdent.  This dialog 
operates similarly to ResponseJDialog in RTi.Util.GUI, where the changes are
returned via a response() call.  The following is sample code for using it:
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
	__locationTextField = null,
	__dataSourceTextField = null,
	__dataTypeTextField = null,
	__scenarioTextField = null,
	__sequenceNumberTextField = null,
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
private boolean __EnableLocation_boolean = true;
private boolean __EnableSource_boolean = true;
private boolean __EnableType_boolean = true;
private boolean __EnableInterval_boolean = true;
private boolean __EnableScenario_boolean = true;
private boolean __EnableSequenceNumber_boolean = true;
private boolean __EnableInputType_boolean = true;
private boolean __EnableInputName_boolean = true;

/**
Constructor.
@param parent the parent JFrame on which the dialog will appear.  This cannot
be null.  If necessary, pass in an empty JFrame via:<p>
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
<td><b>EnableSequenceNumber</b></td>
<td><b>Indicates (True/False) whether the sequence number should be enabled/verified.</b>
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
        // All fields are to be disabled...
        __EnableLocation_boolean = false;
        __EnableSource_boolean = false;
        __EnableType_boolean = false;
        __EnableInterval_boolean = false;
        __EnableScenario_boolean = false;
        __EnableSequenceNumber_boolean = false;
        __EnableInputType_boolean = false;
        __EnableInputName_boolean = false;
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
    enabled = props.getValue("EnableSequenceNumber");
    if ( (enabled != null) && enabled.equalsIgnoreCase("False") ) {
         __EnableSequenceNumber_boolean = false;
    }
    else if ( (enabled != null) && enabled.equalsIgnoreCase("True") ) {
        __EnableSequenceNumber_boolean = true;
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
public void actionPerformed(ActionEvent event)
{	String s = event.getActionCommand();

	if (s.equals(__BUTTON_CANCEL)) {
		response ( false );
	}
	else if (s.equals(__BUTTON_OK)) {
		refresh ();
		checkInput ();
		if ( !__error_wait ) {
			response( true );
		}
	}
	else {
        // list...
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	String routine = "TSIdent_JDialog.checkInput";
	// Checks were done in refresh(), which should have been called before this method...
	__error_wait = false;
	if ( __warning.length() > 0 ) {
		__error_wait = true;
		__warning += "\n\nCorrect or Cancel.";
		Message.printWarning ( 1, routine, __warning );
	}
}

/**
Clean up for garbage collection.
@exception Throwable if an error.
*/
protected void finalize()
throws Throwable {
	__locationTextField = null;
	__dataSourceTextField = null;
	__dataTypeTextField = null;
	__dataInterval_JComboBox = null;
	__scenarioTextField = null;
	__inputTypeTextField = null;
	__inputNameTextField = null;
	__cancelButton = null;
	__okButton = null;	
	__tsident = null;
	super.finalize();
}

/**
Does nothing.
*/
public void keyPressed(KeyEvent e)
{	refresh();
}

/**
Does nothing.
*/
public void keyReleased(KeyEvent e)
{	refresh();
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent e) {}

/**
Refresh the full TSIdent data member and displayed value, based on the individual values displayed in dialog.
*/
private void refresh ()
{	__warning = "";

	String location = "";
	String datasource = "";
	String datatype = "";
	String interval = "";
	String scenario = "";
	String sequenceNumber = "";
	String inputtype = "";
	String inputname = "";
	if ( __first_time ) {
		// Get the interface contents from the incoming TSIdent.
		__first_time = false;
		if ( __tsident != null ) {
			location = __tsident.getLocation();
			datasource = __tsident.getSource();
			datatype = __tsident.getType();
			interval = __tsident.getInterval();
			scenario = __tsident.getScenario();
			if ( __tsident.getSequenceNumber() == -1 ) {
			    sequenceNumber = "";
			}
			else {
			    sequenceNumber = "" + __tsident.getSequenceNumber();
			}
			inputtype = __tsident.getInputType();
			inputname = __tsident.getInputName();
		}
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
		else {	// Automatically add to the list at the top...
			__dataInterval_JComboBox.insertItemAt ( interval, 0 );
			// Select...
			__dataInterval_JComboBox.select ( interval );
		}
		*/
		__scenarioTextField.setText ( scenario );
		__sequenceNumberTextField.setText ( sequenceNumber );
		__inputTypeTextField.setText ( inputtype );
		__inputNameTextField.setText ( inputname );
	}

	// Get from the dialog...

	location = __locationTextField.getText().trim();
	datasource = __dataSourceTextField.getText().trim();
	datatype = __dataTypeTextField.getText().trim();
	interval = __dataInterval_JComboBox.getSelected();
	scenario = __scenarioTextField.getText().trim();
	sequenceNumber = __sequenceNumberTextField.getText().trim();
	inputtype = __inputTypeTextField.getText().trim();
	inputname = __inputNameTextField.getText().trim();

	// Form the TSIdent string...

	__response = new TSIdent();

	// These would normally be in checkInput() but since the TSID is needed
	// to output the string, also do the checks here...

	// For now do not allow missing location...
	if ( location.length() == 0 ) {
		__warning += "\nThe location must be specified.";
	}
	else {
        __response.setLocation( location );
	}
	__response.setSource(datasource);
	__response.setType(datatype);
	// For now do not allow missing interval...
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
	__response.setScenario(scenario);
	if ( StringUtil.isInteger(sequenceNumber) ) {
	    __response.setSequenceNumber(Integer.parseInt(sequenceNumber));
	}
	else {
	    __response.setSequenceNumber(-1);
	}
	__response.setInputType(inputtype);
	__response.setInputName(inputname);

	if ( __warning.length() > 0 ) {
		__warning +="\nSome time series identifier parts may be blank.";
	}

	// Set the string...

	// Long version (if parts are available)...
	__TSID_JTextArea.setText ( __response.toString(true) );
}

/**
Return the user response and dispose the dialog.
@return the dialog response.  If <code>null</code>, the user pressed Cancel.
*/
public void response ( boolean ok )
{
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
public TSIdent response ()
{
	return __response;
}

/**
Sets up the GUI.
*/
private void setupGUI()
{
	setTitle("Edit the Time Series Identifier (TSID)");

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	getContentPane().add("Center", panel);
	
	Insets insetsTLBR = new Insets(2,2,2,2);

	__locationTextField = new JTextField(10);
	__locationTextField.addKeyListener ( this );
	__dataSourceTextField = new JTextField(10);
	__dataSourceTextField.addKeyListener ( this );
	__dataTypeTextField = new JTextField(10);
	__dataTypeTextField.addKeyListener ( this );
	__scenarioTextField = new JTextField(10);
	__scenarioTextField.addKeyListener ( this );
    __sequenceNumberTextField = new JTextField(10);
    __sequenceNumberTextField.addKeyListener ( this );
	__inputTypeTextField = new JTextField(10);
	__inputTypeTextField.addKeyListener ( this );
	__inputNameTextField = new JTextField(10);
	__inputNameTextField.addKeyListener ( this );

	int y = -1;
	JGUIUtil.addComponent(panel, 
		new JLabel(" The time series identifier (TSID) uniquely identifies a time series, " +
		    "and provides key information about the time series."),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" The TSID conforms to the following convention, " +
			"where Scenario and SequenceNumber are optional:"),
       0, ++y, 3, 1, 0, 0, insetsTLBR,
       GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     Location.DataSource.DataType.Interval.Scenario[SequenceNumber]"),
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
       "     123.USGS.Streamflow.6Hour.Raw[1950]  (example using a sequence number for a trace in an ensemble)"),
       0, ++y, 3, 1, 0, 0, 
       GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel,
        new JLabel(" Location and DataType parts can internally use a dash (-) to " +
        	"indicate a sub-location and sub-type, and underscores are also useful as separators within a part."),
        0, ++y, 3, 1, 0, 0, insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( __EnableInputName_boolean && __EnableInputType_boolean ) {
    	JGUIUtil.addComponent(panel,
    		new JLabel(" The input type and name indicate the format and storage location of data."),
    		0, ++y, 3, 1, 0, 0, insetsTLBR,
    		GridBagConstraints.NONE, GridBagConstraints.WEST);
    }
	JGUIUtil.addComponent(panel,
		new JLabel(" Specify TSID parts below and the full TSID will automatically be created from the parts."),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" "),
		0, ++y, 3, 1, 0, 0, insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
    
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
		// Add a blank at the beginning since nothing has been selected...
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
    
    JLabel sequenceNumber_JLabel = new JLabel("Sequence number: ");
    JGUIUtil.addComponent(panel, sequenceNumber_JLabel,
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    JGUIUtil.addComponent(panel, __sequenceNumberTextField,
        1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(panel, new JLabel(" Optional - sequence number for ensemble trace."),
        2, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    if ( !__EnableSequenceNumber_boolean) {
        sequenceNumber_JLabel.setEnabled(false);
        __sequenceNumberTextField.setEnabled(false);
    }

    JLabel inputtype_JLabel = new JLabel("Input type: ");
	JGUIUtil.addComponent(panel, inputtype_JLabel,
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __inputTypeTextField,
		1, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" Optional - input type (e.g., database, file format)."),
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
public void windowActivated(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent evt) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent evt) {}

}