// JComboBoxResponseJDialog - provides a pop-up dialog allowing user to enter a text response

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

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

import java.util.List;

import RTi.Util.String.StringUtil;

/**
Class that provides a dialog from which the user can select a response in a combo box.
*/
@SuppressWarnings("serial")
public class JComboBoxResponseJDialog
extends JDialog
implements ActionListener, KeyListener, WindowListener {

private boolean __editable = false;

/**
The mode in which the class is instantiated.
*/
private int __mode;

/**
The value returned when one of the buttons was pressed.
*/
private int __response;

/**
Dialog buttons.
*/
private JButton		
	__cancelButton,
	__okButton;

/**
The combo box from which values are selected on the dialog.
*/
private SimpleJComboBox __comboBox;	

/**
The title of the string.
*/
private static String __frameTitle;

/**
JComboBoxResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param choices the choices to populate the combo box with.
@param mode mode in which this gui is to be used(i.e., OK, OK | CANCEL)
process different types of yes responses from the calling form.
*/
public JComboBoxResponseJDialog(JFrame parent, String title, String label, List<String> choices, int mode) {	
	this(parent, title, label, choices, mode, false);
}

/**
JComboBoxResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param choices the choices to populate the combo box with.
@param mode mode in which this gui is to be used(i.e., OK, OK | CANCEL)
process different types of yes reponses from the calling form.
@param editable whether the combo box is editable or not.
*/
public JComboBoxResponseJDialog(JFrame parent, String title, String label,
List<String> choices, int mode, boolean editable) {	
	this(parent, title, label, choices, mode, editable, -1);
}

/**
JComboBoxResponseJDialog constructor
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param choices the choices to populate the combo box with.
@param mode mode in which this UI is to be used(i.e., OK, OK | CANCEL)
process different types of yes responses from the calling form.
@param editable whether the combo box is editable or not.
@param numRowsVisible the number of rows in the JComboBox to ensure are visible
when the user clicks the combo box to select something.
If less than or equal to 0, will not be considered and the default will be used.
*/
public JComboBoxResponseJDialog(JFrame parent, String title, String label,
List<String> choices, int mode, boolean editable, int numRowsVisible) {	
	super(parent, true);
	__editable = editable;
	initialize(parent, title, label, choices, mode, numRowsVisible);
}

/**
Responds to ActionEvents
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {	
	String s = event.getActionCommand();
	if (s.equals("Cancel")) {
		__response = ResponseJDialog.CANCEL;
	}
	else if (s.equals("OK")) {
		__response = ResponseJDialog.OK;
	}
	response();
}

/**
Instantiates the GUI components
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param choices choices to populate the combo box with.
@param mode mode in which this UI is to be used(i.e., OK, OK | CANCEL)
process different types of yes responses from the calling form.
@param numRowsVisible the number of rows in the JComboBox to ensure are visible
when the user clicks the combo box to select something.
If less than or equal to 0, will not be considered and the default will be used.
*/
private void initialize(JFrame parent, String title, String label, List<String> choices, int mode, int numRowsVisible) {
	__mode = mode;

	addWindowListener(this);

        // North Panel
        JPanel north_Panel = new JPanel();

	// Split the text based on the new-line delimiter(we use \n, not the platform's separator!

	List<String> vec = StringUtil.breakStringList(label, "\n", 0);

	if (vec != null) {
		// Add each string.
		for(int i = 0; i < vec.size(); i++) {
        		north_Panel.add(new JLabel("    " + vec.get(i) + "     "));
		}
	}
	__comboBox = new SimpleJComboBox(choices, __editable);
	if (numRowsVisible > 0) {
		__comboBox.setMaximumRowCount(numRowsVisible);
	}
	if (__editable) {
		__comboBox.addTextFieldKeyListener(this);
	}
	__comboBox.select(0);
        north_Panel.add(__comboBox);

        north_Panel.setLayout(new GridLayout(vec.size()+1, 1));
        getContentPane().add("North", north_Panel);

	// Now add the buttons.

        // South Panel
        JPanel south_Panel = new JPanel();
        south_Panel.setLayout(new BorderLayout());
        getContentPane().add("South", south_Panel);

        // Panel for buttons.
        JPanel southNorth_Panel = new JPanel();
        southNorth_Panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        south_Panel.add("North", southNorth_Panel);

	if ((__mode & ResponseJDialog.CANCEL) != 0) {
		__cancelButton = new JButton("Cancel");
		__cancelButton.addActionListener(this);
	}

	if ((__mode & ResponseJDialog.OK) != 0) {
        	__okButton = new JButton("OK");
		__okButton.addActionListener(this);
	}

	// Show the appropriate buttons depending upon the selected mode.
	if ((__mode & ResponseJDialog.OK) != 0) {
	        southNorth_Panel.add(__okButton);
	}
	if ((__mode & ResponseJDialog.CANCEL) != 0) {
	        southNorth_Panel.add(__cancelButton);
	}

        // Frame settings.
	if (title != null) {
		setTitle(title);
	}
	else if (__frameTitle != null) {
		setTitle(__frameTitle);
	}
	// Dialogs do no need to be resizable.
	setResizable(false);
        pack();
        JGUIUtil.center(this);
        super.setVisible(true);
}

/**
If the combo box is editable and ENTER is pressed in it, this makes the GUI respond as if OK were pressed.
@param event the KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		__response = ResponseJDialog.OK;
		response();
	}
}

/**
Does nothing.
*/
public void keyReleased(KeyEvent event) {}

/**
Does nothing.
*/
public void keyTyped(KeyEvent event) {}


/**
Return the user response.
@return the dialog response string.
*/
public String response() {
	setVisible(false);
	dispose();
	if (__response == ResponseJDialog.CANCEL) {
		return null;
	}
	else {	
		return __comboBox.getSelected();
	}
}

/**
This function sets the JFrame Title variable that is used for all instances of this class.
@param title Frame title
*/
public static void setFrameTitle(String title) {
	if (title != null) {
		__frameTitle = title;
	}
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing(WindowEvent event) {
	__response = ResponseJDialog.CANCEL;
	response();
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowClosed(WindowEvent event) {}

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