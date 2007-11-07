// ----------------------------------------------------------------------------
// TSIdent_JDialog - a dialog for editing a TSIdent.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2005-08-25	J. Thomas Sapienza, RTi	Initial version.
// 2005-08-26	Steven A. Malers, RTi	Do some cleanup as the dialog is phased
//					into use.  It is expected that
//					properties will be added as the dialog
//					is used more.
// 2005-09-21	SAM, RTi		Add a few more notes at the top so that
//					newTimeSeries_JDialog does not have
//					redundant information.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

// TODO SAM 2005-08-26
// * Need to add optional verification of fields (based on a "source" time
//	series)?  However, need to also support/allow wildcard entry from some
//	uses.
// * Need to pass in available choices (e.g., data types).
// * Need to evaluate whether sub-fields are shown (e.g., for data type).
// * Need to add sequence number for traces.

package RTi.TS;

import java.util.Vector;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
	TSIdent temp = (new TSIdent_JDialog(parentJFrame, true, tsident, null))
		.response();

	if (temp == null) {
		// user cancelled the dialog
	}
	else {
		// user made changes -- accept them.
		tsident = temp;
	}
</pre></code>
*/
public class TSIdent_JDialog
extends JDialog 
implements ActionListener, KeyListener, WindowListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = 	"Cancel",
	__BUTTON_OK = 		"OK";

/**
Components to hold values from the TSIdent.
*/
private JTextField 
	__locationTextField = null,
	__dataSourceTextField = null,
	__dataTypeTextField = null,
	__scenarioTextField = null,
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
	__response = null,	// TSIdent that is returned via response().
	__tsident = null;	// TSIdent that is passed in.

private boolean __first_time = true;	// Indicates if refresh() is being
					// called the first time.
private boolean __error_wait = false;	// Indicates if there is an error in
					// input (true) from checkInput().
private String __warning = "";		// Warnings generated in refresh() and
					// displayed in checkInput().

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
The original instance will not modified so use the returned value to access the
updated version.
@param props currently unused.
*/
public TSIdent_JDialog(JFrame parent, boolean modal, TSIdent tsident, 
PropList props)
{	super(parent, modal);

	__tsident = tsident;

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
	else {	// list...
		refresh();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{	String routine = "TSIdent_JDialog.checkInput";
	// Checks were done in refresh(), which should have been called before
	// this method...
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
Refresh the full TSIdent data member and displayed value, based on the
individual values displayed in dialog.
*/
private void refresh ()
{	__warning = "";

	String location = "";
	String datasource = "";
	String datatype = "";
	String interval = "";
	String scenario = "";
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
			inputtype = __tsident.getInputType();
			inputname = __tsident.getInputName();
		}
		__locationTextField.setText ( location );
		__dataSourceTextField.setText ( datasource );
		__dataTypeTextField.setText ( datatype );
		if (	JGUIUtil.isSimpleJComboBoxItem(
				__dataInterval_JComboBox,
				interval, JGUIUtil.NONE, null, null ) ) {
				__dataInterval_JComboBox.select ( interval );
		}
		else {	__warning += "Interval \"" + interval +
				"\" is not recognized.";
		}
		/* REVISIT SAM 2005-08-26
		 Add this for regular expressions, etc.?
		else {	// Automatically add to the list at the top...
			__dataInterval_JComboBox.insertItemAt ( interval, 0 );
			// Select...
			__dataInterval_JComboBox.select ( interval );
		}
		*/
		__scenarioTextField.setText ( scenario );
		__inputTypeTextField.setText ( inputtype );
		__inputNameTextField.setText ( inputname );
	}

	// Get from the dialog...

	location = __locationTextField.getText().trim();
	datasource = __dataSourceTextField.getText().trim();
	datatype = __dataTypeTextField.getText().trim();
	interval = __dataInterval_JComboBox.getSelected();
	scenario = __scenarioTextField.getText().trim();
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
	else {	__response.setLocation( location );
	}
	__response.setSource(datasource);
	__response.setType(datatype);
	// For now do not allow missing interval...
	if ( interval.length() == 0 ) {
		__warning += "\nA valid interval must be specified.";
	}
	else {	try {	__response.setInterval(interval);
		}
		catch (Exception e) {
			__warning += "\nThe interval \"" + interval +
				"\" is invalid.";
		}
	}
	__response.setScenario(scenario);
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

	int y = 0;

	__locationTextField = new JTextField(10);
	__locationTextField.addKeyListener ( this );
	__dataSourceTextField = new JTextField(10);
	__dataSourceTextField.addKeyListener ( this );
	__dataTypeTextField = new JTextField(10);
	__dataTypeTextField.addKeyListener ( this );
	__scenarioTextField = new JTextField(10);
	__scenarioTextField.addKeyListener ( this );
	__inputTypeTextField = new JTextField(10);
	__inputTypeTextField.addKeyListener ( this );
	__inputNameTextField = new JTextField(10);
	__inputNameTextField.addKeyListener ( this );

	JGUIUtil.addComponent(panel, 
		new JLabel(" The time series identifier (TSID) uniquely " +
		"identifies a time series, and conforms to the standard:"),
		0, y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     Location.DataSource.DataType.Interval.Scenario"),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" For example:"),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     XYZ.USGS.Streamflow.24Hour"),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(
		"     123.NOAA.MeanTemp.Month"),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" The input type and name indicate the format and "+
		"storage location of data."),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Specify TSID parts below and the full TSID will "+
		"automatically be created."),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, new JLabel(" "),
		0, ++y, 3, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Location: "),
		0, ++y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __locationTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" For example, a station or sensor identifier."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Data source: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __dataSourceTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Optional.  The source of the data " +
		"(e.g., agency abbreviation)."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Data type: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __dataTypeTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Optional.  A data type abbreviation."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Data interval: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__dataInterval_JComboBox = new SimpleJComboBox(false);
	Vector interval_Vector = TimeInterval.getTimeIntervalChoices(
		TimeInterval.MINUTE, TimeInterval.YEAR, false, 1, true);
	if ( (__tsident == null) || (__tsident.getInterval().length() == 0) ) {
		// Add a blank at the beginning since nothing has been
		// selected...
		interval_Vector.insertElementAt ( "", 0 );
	}
	__dataInterval_JComboBox.setData ( interval_Vector );
	__dataInterval_JComboBox.addActionListener ( this );
	JGUIUtil.addComponent(panel, __dataInterval_JComboBox,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Data interval."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Scenario: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __scenarioTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Optional string (e.g., \"Hist\", \"Test1\")."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Input type: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __inputTypeTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Optional input type (" +
		"e.g., database, file format)."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel("Input name: "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(panel, __inputNameTextField,
		1, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel,
		new JLabel(" Optional file or database name, for input type."),
		2, y++, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(panel, 
		new JLabel(" Time series identified (TSID): "),
		0, y, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__TSID_JTextArea = new JTextArea ( 4, 55 );
	__TSID_JTextArea.setLineWrap ( true );
	__TSID_JTextArea.setWrapStyleWord ( true );
	__TSID_JTextArea.setEditable ( false );
	JGUIUtil.addComponent(panel, new JScrollPane(__TSID_JTextArea),
		1, y, 2, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JPanel south = new JPanel();
	south.setLayout(new FlowLayout(FlowLayout.RIGHT));

	__okButton = new SimpleJButton(__BUTTON_OK, this);
	__okButton.setToolTipText("Press this button to accept any changes "
		+ "made to the time series identifier.");
	__cancelButton = new SimpleJButton(__BUTTON_CANCEL, this);
	__cancelButton.setToolTipText("Press this button to reject any changes "
		+ "made to the time series identifier.");

	south.add(__okButton);
	south.add(__cancelButton);

	getContentPane().add("South", south);

	refresh();	// Refresh the TSID from the parts.
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
