// ----------------------------------------------------------------------------
// SearchDialog - dialog to search a JTextComponent, similar to standard search
//			tools
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 29 Mar 2001	Steven A. Malers, RTi	Copy FindInListDialog and modify.
// 16 May 2001	SAM, RTi		Change to highlight found text.
// 2001-11-19	SAM, RTi		Change default to case-insensitive
//					search and add a "Match case" checkbox.
//					Change "Next" button to "Find Next".
//					Add "Reset" button.
// ============================================================================
// 2002-10-24	SAM, RTi		Copy SearchDialog to this class and
//					update to use Swing.
// 2003-06-02	J. Thomas Sapienza, RTi	Corrected code so that text selection
//					now works.
// 2003-10-02	JTS, RTi		* Added code for wrapping around in the
//					  text area during a search.
//					* Added code so that if nothing is found
//					  during a search, any previously-
//					  selected text is deselected.
// 2005-11-16	JTS, RTi		* Added a constructor that will accept
//					  a JDialog parent.
//					* Eliminated the unused member variable
//					  _parent.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

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

import RTi.Util.GUI.SimpleJButton;

import RTi.Util.String.StringUtil;

/**
The SearchJDialog searches a JTextComponent and positions the cursor at found
text.
*/
public class SearchJDialog 
extends JDialog 
implements ActionListener, KeyListener, WindowListener {

private JTextField	_find_JTextField;	// text response from user
private JTextComponent	_search_JTextComponent;	// Original JTextComponent to
						// search.
private String		_text;			// Text from JTextArea to search
private	int		_last_find_pos = -1;	// Position of last find.
private int		__searchCount = 0;	
private JCheckBox	_case_JCheckBox = null;	// Check box to match case
private JCheckBox	__wrapAroundJCheckBox = null;

/**
SearchJDialog constructor
@param parent class instantiating this class.
@param text_component JTextComponent to be searched.
@param title Dialog title.
*/
public SearchJDialog(JFrame parent, JTextComponent text_component,
String title) {	
	super(parent, true);
	initialize(text_component, title);
}

/**
SearchJDialog constructor
@param parent class instantiating this class.
@param text_component JTextComponent to be searched.
@param title Dialog title.
*/
public SearchJDialog(JDialog parent, JTextComponent text_component,
String title) {	
	super(parent, true);
	initialize(text_component, title);
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	String command = event.getActionCommand();

	if ( command.equals("Cancel") ) {
		cancelClicked();
	}
	else if ( command.equals("Find Next") ) {
		__searchCount = 0;
		search();
	}
	else if ( command.equals("Reset") ) {
		// Reset for a new search...
		_last_find_pos = -1;
		_search_JTextComponent.setCaretPosition ( 0 );
		_search_JTextComponent.select ( 0, 0 );
	}
	command = null;
}

/**
Close the dialog.
*/
private void cancelClicked()
{	setVisible( false );
	dispose();
}

/**
Clean up before garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable
{	_find_JTextField = null;
	_search_JTextComponent = null;
	_text = null;
	_case_JCheckBox = null;
	__wrapAroundJCheckBox = null;
	super.finalize();
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
	else {	setTitle ( "Find Text" );
	}
	setModal ( false );

	addWindowListener( this );

	// Main panel...

        Insets insetsTLBR = new Insets(1,2,1,2);
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

	// Main contents...

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

	// South Panel: North
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

        insetsTLBR = null;
	main_JPanel = null;
	button_JPanel = null;
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	if ( event.getKeyCode() == KeyEvent.VK_ENTER ) {
		__searchCount = 0;
		search();
	}
}

public void keyReleased ( KeyEvent event )
{	
}

public void keyTyped ( KeyEvent event )
{
}

/**
Do the search.
*/
private void search()
{	String search_text = _find_JTextField.getText();
	int length = search_text.length();
	if (length == 0 ) {
		return;
	}
	int pos = -1;
	if ( _case_JCheckBox.isSelected() ) {
		// Match the case...
		pos = _text.indexOf ( search_text, (_last_find_pos + 1) );
	}
	else {	// Case-independent...
		pos = StringUtil.indexOfIgnoreCase ( _text, search_text,
			(_last_find_pos + 1) );
	}
	if ( pos >= 0 ) {
		// set the selected text color to white
		_search_JTextComponent.setSelectedTextColor(
			new Color(255, 255, 255));
		// set the selection color as the default system selection
		// color
		_search_JTextComponent.setSelectionColor(
			UIManager.getColor("textHighlight"));
		// this makes the selection visible
		_search_JTextComponent.getCaret().setSelectionVisible(true);
		_search_JTextComponent.select ( pos, (pos + length) );
		// SAMX does not seem to be selecting with color???
		//Message.printStatus ( 1, "", "Selection color is " +
		//_search_JTextComponent.getSelectionColor() );
		_last_find_pos = pos;
	}
	else if (__wrapAroundJCheckBox.isSelected()) {
		_last_find_pos = -1;
		__searchCount++;
		if (__searchCount > 1) {
			// deselect whatever is currently selected, because 
			// nothing matching was found
			_search_JTextComponent.getCaret().setSelectionVisible(
				false);		
			return;
		}
		else {
			search();
		}
	}
	else {
		// deselect whatever is currently selected, because nothing
		// matching was found
		_search_JTextComponent.getCaret().setSelectionVisible(false);
	}

	search_text = null;
}

/**
Responds to WindowEvents.
@param event WindowEvent object.
*/
public void windowClosing( WindowEvent event )
{	cancelClicked();
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}
