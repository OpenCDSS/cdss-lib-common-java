//-----------------------------------------------------------------------------
// JWizard - JWizard top level GUI class.
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
// 15 Jan 1998 DLG, RTi		Created initial class description.
// 05 Mar 1998 DLG, RTi		Added the icon image functionality.
// 11 Mar 1998 DLG, RTi		Changed class to public from public abstract
//				as this was causing a conflict with IE 4.0.
//				Functions are no longer abstract in this class.
// 31 Mar 1998 DLG, RTi		Added Constructor for help key.
// 07 May 1998 DLG, RTi		Added javadoc comments.
// 07 Jan 2001 SAM, RTi		Change import * to specific imports, GUI to
//				GUIUtil, and IO to IOUtil.
//-----------------------------------------------------------------------------
// 2003-11-28	J. Thomas Sapienza, RTi	Initial Swing version.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.String.StringUtil;

/**
This class assist may be extended from JFrame class in an effort to 
minimize redundant code when constructing a JWizard type GUI.
Classes extended from this class will inherit the following GUI 
objects:
<pre>
		1. Cancel JButton - Closes the JWizard GUI and cancels
				all selections made in the JWizard.
		2. Back JButton - returns to the previous JPanel in the 
				JWizard, if available.
		3. Next JButton - proceeds to the next JPanel in the 
				JWizard, if available.
		4. Finish JButton - Signifies that use of the JWizard is 
				complete.
</pre>
It is up to the user of this class to generate the code necessary 
to perform the aforementioned behavoir. This class merely instantiates
the objects and enforces the appropriate functions neccesary to handle
the events.<p>

Addtional objects may be added at a lower level but they MUST be added
to the content pane in the "Center" with this code:<p>
<pre>
	getContentPane().add("Center", [component to be added]);
</pre>

Note that this object may be further
subdivided at the lower level if more JPanels are desired.
A JTextField object, __statusJTextField, is available which is useful
for printing status information.<p>

Note that all swing objects are protected and may be accessed from 
the lower level.<p>

This class implements an ActionListener and WindowListener interface;
however, the necessary functions must be defined at a lower-level if
specific event handling is needed for: cancel, back, next, and finish.
The windowClosing event is defined in this class, but should
be overriden is dispose of the object is not desired.<p>

*/

public class JWizard extends JFrame 
implements ActionListener, WindowListener {

/**
Button labels.
*/
public final static String	
	BUTTON_BACK = 	"< Back",
	BUTTON_CANCEL = "Cancel",
	BUTTON_FINISH = "Finish",
	BUTTON_HELP = 	"Help",
	BUTTON_NEXT = 	"Next >";

/**
Total number of wizard steps.
*/
private	int __maxStep;

/**
Current wizard step.
*/
private int __wizardStep;

/**
Cancel JButton
*/
protected JButton __cancelJButton;

/**
Help JButton
*/
protected JButton __helpJButton;

/**
Finish JButton
*/
protected JButton __finishJButton;

/**
Next JButton
*/
protected JButton __nextJButton;

/**
Back JButton
*/
protected JButton __backJButton;

/**
GUI labels.
*/
private JLabel		
	__stepJLabel;
/**
Displays status information
*/
protected JTextField __statusJTextField;

/**
JPanel for which specfic GUI components are to be added by inherited classes.
*/
protected JPanel __centerJPanel;

/**
JPanel to add explanation or information string as multiple JLabel
*/
protected JPanel __infoJPanel;

/**
The help to load if the help button is pressed.
*/
private String __helpKey;

/**
Constructor.
*/
public JWizard() {
	__helpKey = null;
	setupGUI();
}

/**
Constructor.
@param helpKey	Help Key String
*/
public JWizard(String helpKey) {
	// set the help key
	if (helpKey != null) {
		__helpKey = helpKey.trim();
	}
	else {
		__helpKey = null;
	}
	setupGUI();
}

/**
Responds to ActionEvents.  This function MUST be caught in the classes that 
extend this one, as it is not recognized when the event occurs at this level.
Only help events are handled at this level.
*/
public void actionPerformed(ActionEvent evt)
{
	String s = evt.getActionCommand();

	if (s.equals(BUTTON_HELP)) {
		// REVISIT HELP (JTS - 2003-11-28)
	}
	else if (s.equals(BUTTON_BACK)) {
		backClicked();
	}
	else if (s.equals(BUTTON_CANCEL)) {
		cancelClicked();
	}
	else if (s.equals(BUTTON_FINISH)) {
		finishClicked();
	}
	else if (s.equals(BUTTON_NEXT)) {
		nextClicked();
	}
}

/**
This function responds to the __backJButton action performed event.
*/
protected boolean backClicked() {
	__wizardStep--;
	__stepJLabel.setText("Wizard Step " + __wizardStep 
		+ " of " + __maxStep);

	// disable back button if first panel is encountered
	if (__wizardStep == 1) {
		__backJButton.setEnabled(false);
	}
	else {
		__backJButton.setEnabled(true);
	}
	__finishJButton.setEnabled(false);
	__nextJButton.setEnabled(true);

	return true;
}

/**
This function is responsible for handling GUI closing behavior.
*/
protected boolean cancelClicked() {
	return true;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__cancelJButton = null;
	__helpJButton = null;
	__finishJButton = null;
	__nextJButton = null;
	__backJButton = null;
	__stepJLabel = null;
	__statusJTextField = null;
	__centerJPanel = null;
	__infoJPanel = null;
	__helpKey = null;
	super.finalize();
}

/**
This function responds to the __finishJButton action performed event.
*/
protected boolean finishClicked() {
	return true;
}

/**
Returns the current wizard step.
@return the current wizard step.
*/
protected int getWizardStep() {
	return __wizardStep;
}

/**
This function initializes data members.
*/
private void initialize() {
	__maxStep = -1;
	__wizardStep = -1;
	return;
}

/**
This function responds to the __nextJButton action performed event.
*/
protected boolean nextClicked() {
	__wizardStep++;

	if (__wizardStep <= __maxStep) {
		__stepJLabel.setText("Wizard Step " + __wizardStep 
			+ " of " + __maxStep);
	}

	// disable next button and enable finish JButton 
	// if last panel is encountered 
	if (__wizardStep == __maxStep) {
		__nextJButton.setEnabled(false);
		__backJButton.setEnabled(true);
		__finishJButton.setEnabled(true);
	}
	else {
		__nextJButton.setEnabled(true);
		__backJButton.setEnabled(true);
		__finishJButton.setEnabled(false);
	}
	return true;
}

/**
This function sets up the GUI.
*/
private void setupGUI() {
	this.addWindowListener(this);

	// objects to be used in the GUI layout
	GridBagLayout gbl = new GridBagLayout();
	Insets TLNR_Insets = new Insets(7,7,0,7);
	
	// North JPanel
	JPanel northJPanel = new JPanel();
	northJPanel.setLayout(new BorderLayout());
	getContentPane().add("North", northJPanel);

	// North West JPanel
	JPanel northWJPanel = new JPanel();
	northWJPanel.setLayout(gbl);
	northJPanel.add("West", northWJPanel);

	__stepJLabel = new JLabel("Please Insert WIZARD Step Information Here");
	__stepJLabel.setFont(new Font("Helvetica", Font.BOLD, 18));
	JGUIUtil.addComponent(northWJPanel, __stepJLabel, 1, 0, 1, 1, 0, 0, 
		TLNR_Insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	__infoJPanel = new JPanel();
	__infoJPanel.setLayout(gbl);
	northJPanel.add("South", __infoJPanel);

	// Center JPanel
	__centerJPanel = new JPanel();
	getContentPane().add("Center", __centerJPanel);

	// South JPanel
	JPanel southJPanel = new JPanel();
	southJPanel.setLayout(new BorderLayout());
	getContentPane().add("South", southJPanel);

	// South: North JPanel
	JPanel southNJPanel = new JPanel();
	southNJPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	southJPanel.add("North", southNJPanel);

	JPanel navigateJPanel = new JPanel();
	navigateJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	southNJPanel.add(navigateJPanel);

	__backJButton = new JButton(BUTTON_BACK);
	__backJButton.addActionListener(this);
	navigateJPanel.add(__backJButton);	

	__nextJButton = new JButton(BUTTON_NEXT);
	__nextJButton.addActionListener(this);
	navigateJPanel.add(__nextJButton);	

	__finishJButton = new JButton(BUTTON_FINISH);
	__finishJButton.addActionListener(this);
	navigateJPanel.add(__finishJButton);

	JPanel controlJPanel = new JPanel();
	controlJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
	southNJPanel.add(controlJPanel);

	__cancelJButton = new JButton(BUTTON_CANCEL);
	__cancelJButton.addActionListener(this);
	controlJPanel.add(__cancelJButton);

	__helpJButton = new JButton(BUTTON_HELP);
	__helpJButton.addActionListener(this);
	if (__helpKey == null) {
		__helpJButton.setVisible(false);
	}
	controlJPanel.add(__helpJButton);

	// South: South JPanel
	JPanel southSJPanel = new JPanel();
	southSJPanel.setLayout(gbl);
	southJPanel.add("South", southSJPanel);

	__statusJTextField = new JTextField();
	__statusJTextField.setEditable(false);
	JGUIUtil.addComponent(southSJPanel, __statusJTextField, 0, 0, 1, 1, 1, 
		0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// JFrame settings
	pack();
	initialize();	

	return;
}

/**
Sets the state of the back button.
@param state if true, the button is enabled.  If false, it is disabled.
*/
public void setBackEnabled(boolean state) {
	__backJButton.setEnabled(state);
}

/**
Sets the state of the finish button.
@param state if true, the button is enabled.  If false, it is disabled.
*/
public void setFinishEnabled(boolean state) {
	__finishJButton.setEnabled(state);
}

/**
Sets the state of the next button.
@param state if true, the button is enabled.  If false, it is disabled.
*/
public void setNextEnabled(boolean state) {
	__nextJButton.setEnabled(state);
}

/**
This function sets the information string.
@param info string to display path icon file path
*/
protected void setInfoString(String info) {
	Vector vec = StringUtil.breakStringList(info, "\n", 0);

        Insets NLNR_Insets = new Insets(0,7,0,7);

	// clear out any existing labels
	__infoJPanel.removeAll();
	Font font =  new Font("Helvetica", Font.BOLD, 12);
	JLabel label;
	int size = 0;
	int count = 0;

	if (vec != null) {
		// Add each string...
		size = vec.size();
		//__infoJPanel.setLayout(new GridLayout(size, 1));

		for(int i = 0; i < size; i++) {
			label = new JLabel("          " + vec.elementAt(i)+
			  "          ");
			label.setFont(font);

        		JGUIUtil.addComponent(__infoJPanel, label, 
			0, count, 1, 1, 0, 0, NLNR_Insets, GridBagConstraints.NONE, GridBagConstraints.WEST);
			count++;
		}
	}
	//pack();
	validate();
}

/**
This function sets the current wizard step.
@param step wizard step
*/
protected void setWizardStep(int step) {
	if (__maxStep == -999) {
		new ResponseJDialog(this, "Error: Total Wizard steps unknown.",
			ResponseJDialog.OK).response();
		new ResponseJDialog(this, "Set the total steps before setting"
					+ " the wizard step.", 
					ResponseJDialog.OK).response();
		return;			
	}
	
	__stepJLabel.setText("Wizard Step " + step + " of " + __maxStep);

	// disable buttons accordingly
	if (step == 1 && step == __maxStep) {
		__finishJButton.setEnabled(true);
		__backJButton.setEnabled(false);
		__nextJButton.setEnabled(false);
	}
	else if (step == 1) {
		__finishJButton.setEnabled(false);
		__backJButton.setEnabled(false);
	}

	if (__maxStep > 1) {
		__nextJButton.setEnabled(true);
	}

	__wizardStep = step;
	// setVisible(true);
}

/**
This function sets the total number of wizard steps.
@param step total number of wizard steps
*/
protected void setTotalSteps(int step) {
	__maxStep = step;
}

/**
This function responds to the window Closing window event.
@param evt WindowEvent object
*/
public void windowClosing(WindowEvent evt) {
	setVisible(false);
	dispose();
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent evt) {;}

/**
Does nothing.
*/
public void windowClosed(WindowEvent evt) {;}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent evt) {;}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent evt) {;}

/**
Does nothing.
*/
public void windowOpened(WindowEvent evt) {;}

/**
Does nothing.
*/
public void windowIconified(WindowEvent evt) {;}

}
