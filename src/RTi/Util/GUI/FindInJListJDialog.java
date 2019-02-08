// FindInJListJDialog - dialog to search and manipulate a JList

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// ----------------------------------------------------------------------------
// FindInJListJDialog - dialog to search and manipulate a JList
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 16 Mar 2001	Steven A. Malers, RTi	Copy TextResponseDialog and modify.
// 2002-03-01	SAM, RTi		Add to popup the ability to select the
//					first found item (previously could only
//					select all).
// 2002-11-12	SAM, RTi		Copy FindInListDialog and update to use
//					Swing.
// 2004-03-15	SAM, RTi		* Fix bug where not using a
//					  DefaultListModel was causing a class
//					  cast exception.
//					* Put the results in a JScrollPane.
//					* Add option to select all not found.
// 2004-07-26	J. Thomas Sapienza, RTi	Mouse events only trigger a popup
//					menu now when the left button is not 
//					pressed.
// 2005-08-03	JTS, RTi		* Added tool tips to the text field
//					  in which search terms are typed and
//					  the button at the bottom.
//					* Now when new searches are done, the 
//					  old results are cleared.
//					* The size has been adjusted so the 
//					  button at the bottom doesn't get cut
//					  off.
//					* OK button changed to Close to better
//					  represent what it does, and position
//					  changed slightly.
// 2005-08-15	JTS, RTi		Changed text from "Search For:" to a
//					more expressive phrase.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import RTi.Util.GUI.SimpleJList;
import RTi.Util.GUI.SimpleJButton;

/**
The FindInJListJDialog is a dialog containing a list through which users can 
search to find desired information.
*/
public class FindInJListJDialog extends JDialog
implements ActionListener, KeyListener, MouseListener, WindowListener
{
private JTextField	__find_JTextField;	// text response from user
private JList		__original_JList;	// Original List to search
private SimpleJList	__find_JList;		// List containing found items
						// in the original list.
/* SAMX not needed??
private ListSelectionListener	__selection_listener;
						// Selection listener for the
						// original list.
*/
private JPopupMenu	__find_JPopupMenu;	// Popup to edit list.

private String		__GO_TO_ITEM = "Go To First (Selected) Found Item in " +
					"Original List";
private String		__SELECT_FIRST_ITEM = "Select First (Selected) Found " +
				"Item in Original List (deselect others)";
private String		__SELECT_ALL_FOUND_ITEMS =
					"Select All Found Items in Original"+
					" List (deselect others)";
private String		__SELECT_ALL_NOT_FOUND_ITEMS =
					"Select All NOT Found Items in Original"
					+ " List (deselect found items)";
private int[]		__find_index = null;	// Positions in original List
						// that are found.

/**
FindInJListJDialog Constructor.
@param parent JFrame class instantiating this class.
@param list JList to operate on.
@param title JDialog title.
*/
public FindInJListJDialog ( JFrame parent, JList list, String title )
{	this ( parent, true, list, title );
}

/**
FindInJListJDialog Constructor.
@param parent JFrame class instantiating this class.
@param modal If true, the dialog is modal.  If false, it is not.
@param list JList to operate on.
@param title JDialog title.
*/
public FindInJListJDialog ( JFrame parent, boolean modal, JList list, String title )
{	super ( parent, modal );
	initialize ( parent, list, title );//, null );
}

/**
FindInJListJDialog Constructor.
@param parent JFrame class instantiating this class.
@param list JList to operate on.
@param title JDialog title.
@param selection_listener An object that is listening to events on the original
JList.  If supplied, then in addition to adjustments made to the JList, a
ListSelectionEvent will be sent to indicate the adjustment.  This will allow the
JList to be interpreted in container graphical interfaces.  Currently this is
only implemented for the "select first" action.
*/
/* TODO SAM 2007-06-22 Evaluate if needed
public FindInJListJDialog (	JFrame parent, JList list, String title,
				ListSelectionListener selection_listener )
{	super ( parent, true );
	initialize ( parent, list, title, selection_listener );
}
*/

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed ( ActionEvent event )
{	String command = event.getActionCommand();

	if ( command.equals("Close") ) {
		okClicked();
	}
	else if ( command.equals(__GO_TO_ITEM) ) {
		if ( __find_index != null ) {
			if ( JGUIUtil.selectedSize(__find_JList) == 0 ) {
				// Go to first item...
				__original_JList.ensureIndexIsVisible(
					__find_index[0]);
			}
			else {	// Go to first selected item...
				__original_JList.ensureIndexIsVisible(
				__find_index[
				JGUIUtil.selectedIndex(__find_JList,0)]);
			}
		}
	}
	else if ( command.equals(__SELECT_ALL_FOUND_ITEMS) ) {
		// Select in the original list all the found items...
		__original_JList.clearSelection();
		__original_JList.setSelectedIndices ( __find_index );
	}
	else if ( command.equals(__SELECT_ALL_NOT_FOUND_ITEMS) ) {
		// Select in the original list all the not found items and
		// deselect the found items (useful for deleting not found
		// items)...
		int original_size = __original_JList.getModel().getSize();
		if ( (original_size == 0) && (__find_index.length > 0) ) {
			return;
		}
		// Initialize all to 1 (selected)...
		int selected_size = original_size - __find_index.length;
		if ( selected_size == 0 ) {
			return;
		}
		int [] selected_indices = new int[selected_size];
		// REVISIT 2004-03-15 Need to optimize code...
		// Do this the brute force way for now.
		int count = 0;	// Count for selected.
		boolean found = false;
		for ( int i = 0; i < original_size; i++ ) {
			found = false;
			for ( int j = 0; j < __find_index.length; j++ ) {
				if ( __find_index[j] == i ) {
					// Don't want selected...
					found = true;
					break;
				}
			}
			if ( !found ) {
				// We want to select all non-matching rows...
				selected_indices[count++] = i;
			}
		}
		// Clear out the old list...
		__original_JList.clearSelection();
		// Now select what we thing should be selected...
		__original_JList.setSelectedIndices ( selected_indices );
		selected_indices = null;
	}
	else if ( command.equals(__SELECT_FIRST_ITEM) ) {
		if ( __find_index != null ) {
			if ( JGUIUtil.selectedSize(__find_JList) == 0 ) {
				// Go to first item...
				__original_JList.ensureIndexIsVisible(
					__find_index[0]);
				__original_JList.setSelectedIndex(
					__find_index[0]);
				/* SAMX not needed??
				if ( __selection_listener != null ) {
					__selection_listener.valueChanged (
					new ListSelectionEvent (
					__original_JList,
					__find_index[0], __find_index[0],true));
				}
				*/
			}
			else {	// Go to first selected item...
				__original_JList.ensureIndexIsVisible(
				__find_index[
				JGUIUtil.selectedIndex(__find_JList,0)]);
				int found_index = __find_index[
				JGUIUtil.selectedIndex(__find_JList,0)];
				__original_JList.setSelectedIndex(found_index);
				/* SAMX not needed??
				if ( __selection_listener != null ) {
					__selection_listener.valueChanged (
					new ListSelectionEvent (
					__original_JList,
					__found_index, __found_index,true));
				}
				*/
			}
		}
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable
{	__find_JTextField = null;
	__find_JList = null;
	__original_JList = null;
	__GO_TO_ITEM = null;
	__SELECT_FIRST_ITEM = null;
	__SELECT_ALL_FOUND_ITEMS = null;
	__SELECT_ALL_NOT_FOUND_ITEMS = null;
	__find_index = null;
	super.finalize();
}

/**
Instantiates the components.
@param parent JFrame class instantiating this class.
@param list JList that is being operated on.
@param title Dialog title.
*/
//@param selection_listener ListSelectionListener to pass ListSelectionEvents to
//for the list.
private void initialize (	JFrame parent, JList list, String title )//,
				//ListSelectionListener selection_listener )
{	__original_JList = list;
	//__selection_listener = selection_listener;
	if ( (title != null) && (title.length() > 0) ) {
		setTitle ( title );
	}
	else {	setTitle ( "Find Text in List" );
	}

	addWindowListener( this );

	// Main panel...

        Insets insetsTLBR = new Insets(2,2,2,2);
	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "Center", main_JPanel );
	int y = 0;

	// Main contents...

        JGUIUtil.addComponent(main_JPanel,
		new JLabel ( "Search for rows containing:"),
		0, y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	__find_JTextField = new JTextField (30);
	__find_JTextField.setToolTipText(
		"<html>Type the text to search for and press Enter.<br>" +
		"Then right click on the list below for more options.</html>");
        JGUIUtil.addComponent(main_JPanel, __find_JTextField,
		1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	__find_JTextField.addKeyListener ( this );

        JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Search Results (found items):" ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	__find_JList = new SimpleJList ();
	__find_JList.setToolTipText(
		"Right click to see actions to perform on the original list.");
	__find_JList.setVisibleRowCount ( 10 );
	__find_JList.setSelectionMode (
		ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
	// Initially a blank list.
	__find_JList.addKeyListener ( this );
	__find_JList.addMouseListener ( this );	// For popup
        JGUIUtil.addComponent(main_JPanel, new JScrollPane(__find_JList),
		0, ++y, 7, 1, 1, 1, insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	SimpleJButton Close = new SimpleJButton("Close", this);
	Close.setToolTipText("Closes the dialog window.");
	button_JPanel.add ( Close );

	// Add the JPopupMenu...

	// Pop-up menu to manipulate the list...
	__find_JPopupMenu = new JPopupMenu("Search Actions");
	__find_JPopupMenu.add( new SimpleJMenuItem ( __GO_TO_ITEM, this ) );
	__find_JPopupMenu.add( new SimpleJMenuItem ( __SELECT_FIRST_ITEM,this));
	if (	__original_JList.getSelectionMode() ==
		ListSelectionModel.MULTIPLE_INTERVAL_SELECTION ) {
		// Only makes sense if we can select more than one thing in the
		// original list...
		__find_JPopupMenu.add( new SimpleJMenuItem (
		__SELECT_ALL_FOUND_ITEMS,this));
		__find_JPopupMenu.add( new SimpleJMenuItem (
		__SELECT_ALL_NOT_FOUND_ITEMS,this));
	}
	setResizable ( true );
        pack();
	setSize(getWidth(), getHeight() + 10);
        JGUIUtil.center( this );
        super.setVisible( true );
}

/**
Respond to KeyEvents.  If enter is pressed, refreshes the dialog.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh();
	}
}

/**
Does nothing.
*/
public void keyReleased ( KeyEvent event )
{	
}

/**
Does nothing.
*/
public void keyTyped ( KeyEvent event ) {;}

/**
Does nothing.
*/
public void mouseClicked ( MouseEvent event )
{
}

/**
Does nothing.
*/
public void mouseEntered ( MouseEvent event )
{
}

/**
Does nothing.
*/
public void mouseExited ( MouseEvent event )
{
}

/**
Handle mouse pressed event.  Shows the popup menu if the popup menu trigger
(right mouse button usually) was pressed.
*/
public void mousePressed ( MouseEvent event ) {
	if (__find_JList.getItemCount() > 0
		&& event.getButton() != MouseEvent.BUTTON1){
		Point pt = JGUIUtil.computeOptimalPosition (
			event.getPoint(), event.getComponent(),
			__find_JPopupMenu );
		__find_JPopupMenu.show ( event.getComponent(), pt.x, pt.y );
	}
}

/**
Does nothing.
*/
public void mouseReleased ( MouseEvent event )
{
}

/**
Close the dialog.
*/
private void okClicked()
{	setVisible( false );
	dispose();
}

/**
Refresh the list based on the current find string.
*/
private void refresh()
{	// First clear the list...
	__find_JList.removeAll();
	// Now search the original list...
	if ( __original_JList == null ) {
		return;
	}
	int size = __original_JList.getModel().getSize();
	String item = null, item_up = null;
	String find_text = __find_JTextField.getText().trim().toUpperCase();
	int find_count = 0;
	// First cut at index...
	int [] find_index = new int[size];
	JGUIUtil.setWaitCursor ( this, true );
	for ( int i = 0; i < size; i++ ) {
		item = "" + __original_JList.getModel().getElementAt(i);
		item_up = item.toUpperCase();
		if ( item_up.indexOf(find_text) >= 0 ) {
			((DefaultListModel)
			__find_JList.getModel()).addElement(item);
			find_index[find_count] = i;
			// Set selection to match original list...
			if ( __original_JList.isSelectedIndex(i) ) {
				__find_JList.setSelectedIndex(find_count);
			}
			++find_count;
		}
	}
	// Now resize the find index to the final...
	__find_index = new int[find_count];
	for ( int i = 0; i < find_count; i++ ) {
		__find_index[i] = find_index[i];
	}
	JGUIUtil.setWaitCursor ( this, false );
	find_index = null;
	item = null;
	item_up = null;
	find_text = null;
}

/**
Responds to WindowEvents.  Closes the window.
@param event WindowEvent object.
*/
public void windowClosing( WindowEvent event )
{	okClicked();
}

/**
Does nothing.
*/
public void windowActivated( WindowEvent evt ){;}

/**
Does nothing.
*/
public void windowClosed( WindowEvent evt ){;}

/**
Does nothing.
*/
public void windowDeactivated( WindowEvent evt ){;}

/**
Does nothing.
*/
public void windowDeiconified( WindowEvent evt ){;}

/**
Does nothing.
*/
public void windowIconified( WindowEvent evt ){;}

/**
Does nothing.
*/
public void windowOpened( WindowEvent evt ){;}

} // end FindInJListJDialog
