// ----------------------------------------------------------------------------
// ERDiagram_FindDialog - A dialog for finding a table by name.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-27	J. Thomas Sapienza, RTi	* Initial changelog.  
//					* Added JFrame parameter to constructor.
//					* Added labels describing the search.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import RTi.Util.GUI.JGUIUtil;

/**
This class is a dialog box used when finding a table in the ER Diagram.
It contains a text field that responds to the 'enter' key being pressed.  
The user types in the first few characters of the table name and the tables
are searched for all those tables that start with the same case-insensitive 
character.
REVISIT (JTS - 2006-05-22)
Should probably use TextResponseJDialog instead and remove this class.
*/
public class ERDiagram_FindDialog 
extends JDialog {

/**
The text field into which the user types the name of the table.
*/
private JTextField __tableNameTextField;

/**
Constructor.
@param parent the parent JFrame on which this dialog will be displayed.
@param al the ActionListener that will respond when the user press enter
in the text field.
*/
public ERDiagram_FindDialog(JFrame parent, ActionListener al) {
	super(parent, false);
	setTitle("Find Table");

	JPanel panel = new JPanel();
	__tableNameTextField = new JTextField(30);
	
	panel.add(__tableNameTextField);
	__tableNameTextField.addActionListener(al);

	JPanel labelPanel = new JPanel();
	labelPanel.setLayout(new GridBagLayout());
	JGUIUtil.addComponent(labelPanel, 
		new JLabel("Enter a substring of the name of the"),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	JGUIUtil.addComponent(labelPanel,
		new JLabel("table for which you are looking and press enter."),
		0, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	
	getContentPane().add("North", labelPanel);

	getContentPane().add("Center", panel);
	pack();
	show();
	JGUIUtil.center(this);
}

/**
Returns the text entered in the text field before the user hit 'enter'.
@return the text entered in the text field before the user hit 'enter'.
*/
public String getText() {
	return __tableNameTextField.getText();
}

}
