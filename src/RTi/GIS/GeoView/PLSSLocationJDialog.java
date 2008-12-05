//-----------------------------------------------------------------------------
// PLSSLocationJDialog - a dialog for entering a PLSS location.
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
//
// 2005-01-14	J. Thomas Sapienza, RTi	Initial version from 
//					HydroBase_GUI_BuildLocationQuery.
// 2005-02-01	JTS, RTi		The wildcard character can now be
//					changed.
// 2005-02-02	JTS, RTi		* Added support for half sections.
//					* Added Clear button.
// 2005-04-27	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
Class that assists various forms in building a location query.
TODO (JTS - 2006-05-23) Example of using this.
*/
public class PLSSLocationJDialog 
extends JDialog 
implements WindowListener, KeyListener, ActionListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = 	"Cancel",
	__BUTTON_CLEAR = 	"Clear",
	__BUTTON_OK = 		"OK";

/**
A string used to represent an unset value in the location.
*/
private String __wildcard = " ";

/**
The interaction the user had with the dialog to close it (either 
ResponseJDialog.OK or ResponseJDialog.CANCEL).
*/
private int __response = -1;

/**
JTextField to hold the range.
*/
private JTextField __rangeJTextField;

/**
JTextField to hold the section.
*/
private JTextField __sectionJTextField;

/**
JTextField to hold the township.
*/
private JTextField __tsJTextField;

// TODO SAM 2007-05-09 Evaluate how used
/**
The location object that stores the data initially passed into this dialog.
*/
//private PLSSLocation __location = null;

/**
Combo box for holding the half section.
*/
private SimpleJComboBox __halfSectionJComboBox;

/**
Combo box for holding pm.
*/
private SimpleJComboBox __pmJComboBox;

/**
Combo box for holding q10 (1/4 1/4 1/4 section).
*/
private SimpleJComboBox __q10JComboBox;

/**
Combo box for holding q40 (1/4 1/4 section).
*/
private SimpleJComboBox __q40JComboBox;

/**
Combo box for holding q160 (1/4 section).
*/
private SimpleJComboBox __q160JComboBox;

/**
Combo box for holding range.
*/
private SimpleJComboBox __rangeJComboBox;

/**
Combo box for holding township.
*/
private SimpleJComboBox __tsJComboBox;
         
/**
Constructor. 
@param parent the parent frame on which to build this modal dialog.
@param cdssFormat whether the location data are in CDSS format.
@throws Exception if an error occurs
*/
public PLSSLocationJDialog(JFrame parent, boolean cdssFormat)
throws Exception {
	super(parent, true);

	if (cdssFormat) {	
		__wildcard = "*";
	}

	//__location = new PLSSLocation();

	setupGUI();

        super.setVisible(true);
}

/**
Constructor.  Builds a dialog and populates the components with the data
stored in the given location object.
@param parent the parent frame on which to build this modal dialog.
@param location the location object to use for filling in data in the dialog.
@param cdssFormat whether the location data are in CDSS format.
@throws Exception if an error occurs
*/
public PLSSLocationJDialog(JFrame parent, PLSSLocation location, 
boolean cdssFormat)
throws Exception {
	super(parent, true);

	if (cdssFormat) {
		__wildcard = "*";
	}

	//__location = location;

	setupGUI();

	fillComponentData(location);

        super.setVisible(true);
}

/**
Responds to action performed events.
@param evt the action event that happened.
*/
public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand().trim();

        if (command.equals(__BUTTON_CANCEL)) {
		__response = ResponseJDialog.CANCEL;
		response();
        }
	else if (command.equals(__BUTTON_CLEAR)) {
		clear();
	}
        else if (command.equals(__BUTTON_OK)) {
                __response = ResponseJDialog.OK;
		if (checkData()) {
			response();
		}
        }
}

/**
Builds a PLSSLocation object from the data in the dialog.
@return a PLSSLocation object filled with data from the dialog.
*/
public PLSSLocation buildLocation() {
	String routine = "PLSSLocationJDialog.buildLocation";

	PLSSLocation location = new PLSSLocation();
	
	try {
		String s = __pmJComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setPM(s);
		}

		s = __tsJTextField.getText();
		if (!s.trim().equals("")) {
			location.setTownship(StringUtil.atoi(s));
		}

		s = __tsJComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setTownshipDirection(s);
		}

		s = __rangeJTextField.getText();
		if (!s.trim().equals("")) {
			location.setRange(StringUtil.atoi(s));
		}

		s = __rangeJComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setRangeDirection(s);
		}

		s = __sectionJTextField.getText();
		if (!s.trim().equals("")) {
			location.setSection(StringUtil.atoi(s));
		}

		s = __halfSectionJComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setHalfSection(s);
		}

		s = __q160JComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setQ(s);
		}

		s = __q40JComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setQQ(s);
		}

		s = __q10JComboBox.getSelected();
		if (!s.equals(__wildcard)) {
			location.setQQQ(s);
		}
	}
	catch (Exception e) {
		// this should never happen since the inputs are constrained
		// (mostly) by the dialog
		Message.printWarning(1, routine, "Error creating location.");
		Message.printWarning(2, routine, e);
		return null;
	}

	return location;
}

/**
Checks the data values to make sure they are valid.
@return true if the data values are valid, false if they are not.
*/
private boolean checkData() {
	String message = "";

	String s = __tsJTextField.getText();
	int i = StringUtil.atoi(s);
	if (!s.trim().equals("")) {
		if (i <= 0) {
			message += "Township (" + s + ") must be greater "
				+ "than 0\n";
		}
	}
	
	s = __rangeJTextField.getText();
	i = StringUtil.atoi(s);
	if (!s.trim().equals("")) {
		if (i <= 0) {
			message += "Range (" + s + ") must be greater than 0\n";
		}
	}

	s = __sectionJTextField.getText();
	i = StringUtil.atoi(s);
	if (!s.trim().equals("")) {
		if (i <= 0) {
			message += "Section (" + s + ") must be greater "
				+ "than 0\n";
		}
		else if (i > 36) {
			message += "Section (" + s + ") must be less than 37\n";
		}
	}

	if (message.length() == 0) {
		return true;
	}
	
	new ResponseJDialog(this, "Error in Location data",
		message, ResponseJDialog.OK);
	return false;
}

/**
Clears any data that has been entered on the GUI, clearing all the text fields 
and setting all the combo boxes to the wildcard value.
*/
private void clear() {
	__pmJComboBox.select(__wildcard);
	__tsJTextField.setText("");
	__tsJComboBox.select(__wildcard);
	__rangeJTextField.setText("");
	__rangeJComboBox.select(__wildcard);
	__sectionJTextField.setText("");
	__halfSectionJComboBox.select(__wildcard);
	__q160JComboBox.select(__wildcard);
	__q40JComboBox.select(__wildcard);
	__q10JComboBox.select(__wildcard);
}

/**
Fills the components on the dialog with the location data stored in the 
specified PLSSLocation object.
@param location the PLSSLocation object to use for filling in the components.
*/
private void fillComponentData(PLSSLocation location) {
	String s = location.getPM();
	if (s == null) {
		__pmJComboBox.select(__wildcard);
	}
	else {
		__pmJComboBox.setText(s);
	}
	
	int i = location.getTownship();
	if (i == PLSSLocation.UNSET) {
		__tsJTextField.setText("");
	}
	else {
		__tsJTextField.setText("" + i);
	}

	s = location.getTownshipDirection();
	if (s == null) {
		__tsJComboBox.select(__wildcard);
	}
	else {
		__tsJComboBox.setText(s);
	}

	i = location.getRange();
	if (i == PLSSLocation.UNSET) {
		__rangeJTextField.setText("");
	}
	else {
		__rangeJTextField.setText("" + i);
	}

	s = location.getRangeDirection();
	if (s == null) {
		__rangeJComboBox.select(__wildcard);
	}
	else {
		__rangeJComboBox.setText(s);
	}

	i = location.getSection();
	if (i == PLSSLocation.UNSET) {
		__sectionJTextField.setText("");
	}
	else {
		__sectionJTextField.setText("" + i);
	}

	s = location.getHalfSection();
	if (s == null) {
		__halfSectionJComboBox.select(__wildcard);
	}
	else {
		__halfSectionJComboBox.setText(s);
	}

	s = location.getQ();
	if (s == null) {
		__q160JComboBox.select(__wildcard);
	}
	else {
		__q160JComboBox.setText(s);
	}

	s = location.getQQ();
	if (s == null) {
		__q40JComboBox.select(__wildcard);
	}
	else {
		__q40JComboBox.setText(s);
	}

	s = location.getQQQ();
	if (s == null) {
		__q10JComboBox.select(__wildcard);
	}
	else {
		__q10JComboBox.setText(s);
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__wildcard = null;
	__rangeJTextField = null;
	__sectionJTextField = null;
	__tsJTextField = null;
	//__location = null;
	__halfSectionJComboBox = null;
	__pmJComboBox = null;
	__q10JComboBox = null;
	__q40JComboBox = null;
	__q160JComboBox = null;
	__rangeJComboBox = null;
	__tsJComboBox = null;
         super.finalize();
}

/**
Responds to key pressed events.
@param event the key event that happened.
*/
public void keyPressed(KeyEvent event) {
	int code = event.getKeyCode();
	
        // enter key acts as an ok action event
	if (code == KeyEvent.VK_ENTER) {
		__response = ResponseJDialog.OK;
		response();
	}
}

/**
Responds to key released events.
@param event the key event that happened.
*/
public void keyReleased(KeyEvent event) {}

/**
Responds to key typed events.
@param event the key event that happened.
*/
public void keyTyped(KeyEvent event) {}

/**
Return the user response.
@return the dialog response string.
*/
public PLSSLocation response() {
	setVisible(false);
	dispose();
	if (__response == ResponseJDialog.CANCEL) {
		return null;
	}
	else {	
		return buildLocation();
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(this);

        // objects used throughout the GUI layout
        Insets insetsTLNR = new Insets(7,7,0,7);                         
        Insets insetsTNNN = new Insets(7,0,0,0);
        Insets insetsNLNR = new Insets(0,7,0,7);
        GridBagLayout gbl = new GridBagLayout();

        // Center panel
        JPanel centerJPanel = new JPanel();
        centerJPanel.setLayout(gbl);
        getContentPane().add("Center", centerJPanel);

        JGUIUtil.addComponent(centerJPanel, new JLabel("PM:"), 
		0, 0, 1, 1, 0, 0, insetsTLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __pmJComboBox = new SimpleJComboBox();
        __pmJComboBox.add(__wildcard);
        __pmJComboBox.add("B");
        __pmJComboBox.add("C");
        __pmJComboBox.add("N");
        __pmJComboBox.add("S");
        __pmJComboBox.add("U");
        __pmJComboBox.select(0);
        JGUIUtil.addComponent(centerJPanel, __pmJComboBox, 
		1, 0, 1, 1, 0, 0, insetsTNNN, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent(centerJPanel, new JLabel("Township:"), 
		0, 1, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __tsJTextField = new JTextField(10);
	__tsJTextField.addKeyListener(this);
        JGUIUtil.addComponent(centerJPanel, __tsJTextField, 
		1, 1, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

        __tsJComboBox = new SimpleJComboBox();
        __tsJComboBox.add(__wildcard);
        __tsJComboBox.add("N");
        __tsJComboBox.add("S");
        __tsJComboBox.select(0);
        JGUIUtil.addComponent(centerJPanel, __tsJComboBox, 
		2, 1, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(centerJPanel, new JLabel("Range:"), 
		0, 2, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __rangeJTextField = new JTextField(10);
	__rangeJTextField.addKeyListener(this);
        JGUIUtil.addComponent(centerJPanel, __rangeJTextField, 
		1, 2, 1, 1, 0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST);

        __rangeJComboBox = new SimpleJComboBox();
        __rangeJComboBox.add(__wildcard);
        __rangeJComboBox.add("E");
        __rangeJComboBox.add("W");
        __rangeJComboBox.select(0);
        JGUIUtil.addComponent(centerJPanel, __rangeJComboBox, 
		2, 2, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(centerJPanel, new JLabel("Section:"), 
		0, 3, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __sectionJTextField = new JTextField(10);
	__sectionJTextField.addKeyListener(this);
        JGUIUtil.addComponent(centerJPanel, __sectionJTextField, 
		1, 3, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

      __halfSectionJComboBox = new SimpleJComboBox();
	__halfSectionJComboBox.add(__wildcard);
	__halfSectionJComboBox.add("U");
      JGUIUtil.addComponent(centerJPanel, __halfSectionJComboBox, 
		2, 3, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(centerJPanel, new JLabel("1/4 Section:"), 
		0, 4, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __q160JComboBox = new SimpleJComboBox();
        __q160JComboBox.add(__wildcard);
        __q160JComboBox.add("NE");
        __q160JComboBox.add("NW");
        __q160JComboBox.add("SE");
        __q160JComboBox.add("SW");
        JGUIUtil.addComponent(centerJPanel, __q160JComboBox, 
		1, 4, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(centerJPanel, new JLabel("1/4 1/4 Section:"), 
		0, 5, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __q40JComboBox = new SimpleJComboBox();
        __q40JComboBox.add(__wildcard);
        __q40JComboBox.add("NE");
        __q40JComboBox.add("NW");
        __q40JComboBox.add("SE");
        __q40JComboBox.add("SW");
        JGUIUtil.addComponent(centerJPanel, __q40JComboBox, 
		1, 5, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        JGUIUtil.addComponent(centerJPanel, new JLabel("1/4 1/4 1/4 Section:"), 
		0, 6, 1, 1, 0, 0, insetsNLNR, GridBagConstraints.NONE, GridBagConstraints.EAST);

        __q10JComboBox = new SimpleJComboBox();
        __q10JComboBox.add(__wildcard);
        __q10JComboBox.add("NE");
        __q10JComboBox.add("NW");
        __q10JComboBox.add("SE");
        __q10JComboBox.add("SW");
        JGUIUtil.addComponent(centerJPanel, __q10JComboBox, 
		1, 6, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

        // Bottom panel
        JPanel bottomJPanel = new JPanel();
        bottomJPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add("South", bottomJPanel);
	
	JButton clearJButton = new JButton(__BUTTON_CLEAR);
	clearJButton.addActionListener(this);
	clearJButton.setToolTipText("Clear all text fields and set all choices "
		+ "to the wildcard value (" + __wildcard + ").");
	bottomJPanel.add(clearJButton);

        JButton okJButton = new JButton(__BUTTON_OK);
	okJButton.addActionListener(this);
	okJButton.setToolTipText("Accept PLSS location.");
        bottomJPanel.add(okJButton);
 
        JButton cancelJButton = new JButton(__BUTTON_CANCEL);
	cancelJButton.addActionListener(this);
	cancelJButton.setToolTipText("Close this window and discard the "
		+ "PLSS location.");
        bottomJPanel.add(cancelJButton);

	String app = JGUIUtil.getAppNameForWindows();
	if (app == null || app.trim().equals("")) {
		app = "";
	}
	else {
		app += " - ";	
	}
        setTitle(app + "Specify PLSS Location");
        pack();
	JGUIUtil.center(this);
        setResizable(false);
}

/**
Responds to window activated events. 
@param evt the window event that happened.
*/
public void windowActivated(WindowEvent evt) {}

/**
Responds to window closed events.
@param evt the window event that happened.
*/
public void windowClosed(WindowEvent evt) {}

/**
Responds to window closing events.
@param evt the window event that happened.
*/
public void windowClosing(WindowEvent evt) {
	__response = ResponseJDialog.CANCEL;
	response();
}

/**
Responds to window deactivated events.
@param evt the window event that happened.
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Responds to window deiconified events.
@param evt the window event that happened.
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Responds to window iconified events.
@param evt the window event that happened.
*/
public void windowIconified(WindowEvent evt) {}

/**
Responds to window opened events.
@param evt the window event that happened.
*/
public void windowOpened(WindowEvent evt) {}

}
