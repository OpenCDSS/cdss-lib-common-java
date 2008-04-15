// ----------------------------------------------------------------------------
// ImageQualityJDialog - a dialog for easily selecting the quality setting
//	of a JPEG.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-08-27	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import RTi.Util.GUI.SimpleJButton;

/**
This class provides a simple dialog for specifying the quality of a JPEG in a 
way that can be used in the JPEG encoder RTi uses.  The Dialog consists of a 
slider, an OK button, a CANCEL button, and some information for the user.  
The user can select a value from 0 - 100, and this value will be accessible 
after the OK button is pressed. 
If the user presses, CANCEL, the value that is returned is -1.  
If the user closes the dialog from the X button in the upper-right-hand 
corner, the value returned is also -1.
*/
public class ImageQualityJDialog 
extends JDialog 
implements ActionListener, WindowListener {

/**
Button labels.
*/
private final String
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

/**
The quality the user selected from the slider, or -1 if they hit CANCEL.
*/
private int __quality;

/**
The slider that the user will use to select image quality.
*/
private JSlider __slider;

/**
Constructor.
@param parent the parent JFrame on which the JDialog will appear.
*/
public ImageQualityJDialog(JFrame parent) {
	super(parent, "Select Image Quality", true);

	setupGUI();
}

/**
Responds when the user presses the OK or Cancel button and sets the quality
value accordingly.
@param e the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent e) {
	String action = e.getActionCommand();

	if (action.equals(__BUTTON_CANCEL)) {
		__quality = -1;
		dispose();
	}
	else if (action.equals(__BUTTON_OK)) {
		__quality = __slider.getValue();
		dispose();
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__slider = null;
	super.finalize();
}

/**
Returns the quality the user selected.
@return the quality the user selected.
*/
public int getQuality() {
	return __quality;
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	addWindowListener(this);

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());

	__slider = new JSlider(0, 100, 70);
	__slider.setPaintLabels(true);
	__slider.setPaintTicks(true);
	__slider.setMajorTickSpacing(10);
	__slider.setMinorTickSpacing(5);

	JGUIUtil.addComponent(panel, __slider,
		0, 0, 1, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(panel, 
		new JLabel("Select the quality of the saved image."),
		0, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(panel,
		new JLabel("(Towards 0 means more compression, "),
		0, 2, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(panel,
		new JLabel("towards 100 means higher quality)"),
		0, 3, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);		
	
	getContentPane().add("Center", panel);

	JPanel bottom = new JPanel();
	bottom.setLayout(new FlowLayout());
	SimpleJButton okButton = new SimpleJButton(__BUTTON_OK, this);
	SimpleJButton cancelButton = new SimpleJButton(__BUTTON_CANCEL, this);
	bottom.add(okButton);
	bottom.add(cancelButton);

	getContentPane().add("South", bottom);

	pack();
	JGUIUtil.center(this);
	setVisible(true);
}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowActivated(WindowEvent e) {}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowClosed(WindowEvent e) {}

/**
Sets the value that will be returned from getQuality() to -1.
@param e the WindowEvent that happened.
*/
public void windowClosing(WindowEvent e) {
	__quality = -1;
}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent e) {}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent e) {}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowIconified(WindowEvent e) {}

/**
Does nothing.
@param e the WindowEvent that happened.
*/
public void windowOpened(WindowEvent e) {}

}
