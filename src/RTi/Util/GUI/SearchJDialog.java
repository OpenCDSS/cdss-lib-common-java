// SearchDialog - dialog to search a JTextComponent, similar to standard search tools

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

import java.awt.Color;
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

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import RTi.Util.String.StringUtil;

/**
The SearchJDialog searches a JTextComponent and positions the cursor at found text.
*/
@SuppressWarnings("serial")
public class SearchJDialog
extends JDialog
implements ActionListener, KeyListener, WindowListener {

private JTextField	_find_JTextField;	// Text response from user.
private JTextComponent	_search_JTextComponent;	// Original JTextComponent to search.
private String		_text;			// Text from JTextArea to search.
private	int		_last_find_pos = -1;	// Position of last find.
private int		__searchCount = 0;
private JCheckBox	_case_JCheckBox = null;	// Check box to match case.
private JCheckBox	__wrapAroundJCheckBox = null;

/**
SearchJDialog constructor
@param parent class instantiating this class.
@param text_component JTextComponent to be searched.
@param title Dialog title.
*/
public SearchJDialog(JFrame parent, JTextComponent text_component, String title) {
	super(parent, true);
	initialize(text_component, title);
}

/**
SearchJDialog constructor
@param parent class instantiating this class.
@param text_component JTextComponent to be searched.
@param title Dialog title.
*/
public SearchJDialog(JDialog parent, JTextComponent text_component, String title) {
	super(parent, true);
	initialize(text_component, title);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event ) {
	String command = event.getActionCommand();

	if ( command.equals("Cancel") ) {
		cancelClicked();
	}
	else if ( command.equals("Find Next") ) {
		__searchCount = 0;
		search();
	}
	else if ( command.equals("Reset") ) {
		// Reset for a new search.
		_last_find_pos = -1;
		_search_JTextComponent.setCaretPosition ( 0 );
		_search_JTextComponent.select ( 0, 0 );
	}
}

/**
Close the dialog.
*/
private void cancelClicked() {
	setVisible( false );
	dispose();
}

/**
Instantiates the dialog components.
@param text_area JTextComponent to search.
@param title JDialog title
*/
private void initialize(JTextComponent text_area, String title) {
	_search_JTextComponent = text_area;
	if ( text_area != null ) {
		_text = _search_JTextComponent.getText();
	}
	if ( (title != null) && (title.length() > 0) ) {
		setTitle ( title );
	}
	else {
		setTitle ( "Find Text" );
	}
	setModal ( false );

	addWindowListener( this );

	// Main panel.

    Insets insetsTLBR = new Insets(1,2,1,2);
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	// Main contents.

    JGUIUtil.addComponent(main_JPanel,
		new JLabel ( "Search for:"),
		0, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	_find_JTextField = new JTextField (40);
        JGUIUtil.addComponent(main_JPanel, _find_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	_find_JTextField.addKeyListener ( this );

	_case_JCheckBox = new JCheckBox ( "Match case", false );
        JGUIUtil.addComponent(main_JPanel, _case_JCheckBox,
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	__wrapAroundJCheckBox = new JCheckBox("Wrap around", false);
	JGUIUtil.addComponent(main_JPanel, __wrapAroundJCheckBox,
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Panel for buttons.
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel,
		0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	button_JPanel.add ( new SimpleJButton("Find Next", "Find Next", this) );
	button_JPanel.add ( new SimpleJButton("Reset", "Reset", this) );
	button_JPanel.add ( new SimpleJButton("Cancel", "Cancel", this) );

	setResizable ( true );
        pack();
        JGUIUtil.center( this );
        super.setVisible( true );
	setResizable ( false );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event ) {
	if ( event.getKeyCode() == KeyEvent.VK_ENTER ) {
		__searchCount = 0;
		search();
	}
}

public void keyReleased ( KeyEvent event ) {
}

public void keyTyped ( KeyEvent event ) {
}

/**
Do the search.
*/
private void search() {
	String search_text = _find_JTextField.getText();
	int length = search_text.length();
	if (length == 0 ) {
		return;
	}
	int pos = -1;
	if ( _case_JCheckBox.isSelected() ) {
		// Match the case.
		pos = _text.indexOf ( search_text, (_last_find_pos + 1) );
	}
	else {
		// Case-independent.
		pos = StringUtil.indexOfIgnoreCase ( _text, search_text, (_last_find_pos + 1) );
	}
	if ( pos >= 0 ) {
		// Set the selected text color to white.
		_search_JTextComponent.setSelectedTextColor( new Color(255, 255, 255));
		// Set the selection color as the default system selection color.
		_search_JTextComponent.setSelectionColor( UIManager.getColor("textHighlight"));
		// this makes the selection visible
		_search_JTextComponent.getCaret().setSelectionVisible(true);
		_search_JTextComponent.select ( pos, (pos + length) );
		// SAMX does not seem to be selecting with color???.
		//Message.printStatus ( 1, "", "Selection color is " + _search_JTextComponent.getSelectionColor() );
		_last_find_pos = pos;
	}
	else if (__wrapAroundJCheckBox.isSelected()) {
		_last_find_pos = -1;
		__searchCount++;
		if (__searchCount > 1) {
			// Deselect whatever is currently selected, because nothing matching was found.
			_search_JTextComponent.getCaret().setSelectionVisible( false);
			return;
		}
		else {
			search();
		}
	}
	else {
		// Deselect whatever is currently selected, because nothing matching was found.
		_search_JTextComponent.getCaret().setSelectionVisible(false);
	}

	search_text = null;
}

/**
Responds to WindowEvents.
@param event WindowEvent object.
*/
public void windowClosing( WindowEvent event ) {
	cancelClicked();
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}