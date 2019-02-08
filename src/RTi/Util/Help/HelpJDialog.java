// HelpJDialog - dialog to display initial help information with the potential to lead to the online help

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

//-----------------------------------------------------------------------------
// HelpJDialog - dialog to display initial help information with the
// 	potential to lead to the online help
//-----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// Notes:       (1)  When null is passed as the online help key, additional
//			information is not available and the "More help" 
//			button will not be displayed.  Otherwise, the 
//			"More help" button is enabled.
//		(2)  This doesn't implement the paging ability similar to that
//			of ReportGUI because this is intended for brief help.
//			Anything more should automatically use the 
//			URL.showHelpForKey 
//-----------------------------------------------------------------------------
// History:
// 25 Aug 1999	CEN, RTi		Created class.
// 01 Sep 1999	CEN, RTi		Changed name from PrelimHelpGUI to
//					HelpJDialog and extended from JDialog
//					rather than JFrame.  Added additional
//					javadoc.
// 2001-11-14	Steven A. Malers, RTi	Review javadoc.  Add finalize().  Remove
//					import *.  Verify that variables are set
//					to null when no longer used.  Use
//					GUIUtil instead of GUI.
// 2003-05-27	J. Thomas Sapienza, RTi	Made this class a window listener in 
//					order to remove the HelpJDialog$1.class
//					internal class.
//-----------------------------------------------------------------------------
// 2003-08-25	JTS, RTi		Initial Swing version.
//-----------------------------------------------------------------------------

package RTi.Util.Help;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.GUIUtil;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
The HelpJDialog displays a simple message and allows full help to be brought
up, if the user wants more information.
*/
@SuppressWarnings("serial")
public class HelpJDialog extends JDialog
implements ActionListener, WindowListener
{

private JTextArea 	_help_JTextArea;
private JButton		_help_JButton;
private JButton		_close_JButton;
private PropList	_props;
private String		_help_key = null;

/**
HelpJDialog constructor
@param helpInfo list of String elements to display
@param props PropList object as described in the following table
<table width = 80% cellpadding=2 cellspace=0 border=2>
<tr>
<td>Property</td>        <td>Description</td>      <td>Default</td>
</tr>

<tr>
<td>HelpKey</td>
<td>Search key for help.</td>
<td>Help button is disabled if not specified.</td>
</tr>

<tr>
<td>TotalWidth</td>
<td>Width of dialog.</td>
<td>600</td>
</tr>

<tr>
<td>TotalHeight</td>
<td>Height of dialog.</td>
<td>550</td>
</tr>

<tr>
<td>Title</td>
<td>Title of dialog.</td>
<td>"Help".</td>
</tr>

</table>
*/
public HelpJDialog ( JFrame parent, List<String> helpInfo, PropList props )
{	super ( parent );
	_props = props;
	setGUI ( );

	if ( helpInfo != null && helpInfo.size() > 0 ) {
		StringBuffer contents = new StringBuffer();
		String newLine = System.getProperty ( "line.separator" );

		int size = helpInfo.size();
		for ( int i=0; i<size; i++ ) {
			contents.append ( (String)helpInfo.get(i) + newLine );
			_help_JTextArea.setText ( contents.toString() );
		}
		contents = null;
		newLine = null;
	}
}

/**
Responds to ActionEvents, including the help and close button presses.
@param ae ActionEvent.
*/
public void actionPerformed ( ActionEvent ae ) 
{	Object source = ae.getSource ();
	if ( source == _help_JButton ) {
		URLHelp.showHelpForKey ( _help_key );
	}
	if ( source == _close_JButton ) {
		closeWindow();
	}
	source = null;
}

/**
Close the dialog.
*/
private void closeWindow ()
{	setVisible ( false );
	dispose();
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	_help_JTextArea = null;
	_props = null;
	_help_key = null;
	_close_JButton = null;
	_help_JButton = null;
	super.finalize();
}

/**
Sets up the awt portion of the GUI.
*/
private void setGUI ( ) 
{	GridBagLayout gbl = new GridBagLayout();
	String propValue;
	int width, height;

	addWindowListener (this);
	/*
		public void windowClosing ( WindowEvent evt ) {
			closeWindow();
		}
	} );
*/

	if ( _props == null )
		_props = new PropList ("PrelimHelpGUIProps");

	_help_key = _props.getValue ( "HelpKey" );	

	// Determine the width
	propValue = _props.getValue ( "TotalWidth" );
	if ( propValue == null )  {
		width = 600;
	}
	else {	width = StringUtil.atoi ( propValue );
	}

	// Determine the height
	propValue = _props.getValue ( "TotalHeight" );
	if ( propValue == null )  {
		height = 550;
	}
	else { height = StringUtil.atoi ( propValue );
	}

	propValue = _props.getValue ( "Title" );
	if ( propValue != null ) {
		setTitle ( propValue );
	}
	else {	setTitle ( "Help" );
	}
	
	// Center panel
	JPanel centerJPanel = new JPanel();
	centerJPanel.setLayout ( gbl );
	getContentPane().add ( "Center", centerJPanel );

	_help_JTextArea = new JTextArea();
	_help_JTextArea.setEditable ( false );

	// want to set to fixed width font
	Font oldFont = _help_JTextArea.getFont();
	Font newFont = null;
	if (oldFont == null) {
		newFont = new Font("Courier", Font.PLAIN, 11);
	}
	else {
		newFont = new Font("Courier", oldFont.getStyle(), 
			oldFont.getSize());
	}
	_help_JTextArea.setFont (newFont);
	
	GUIUtil.addComponent ( centerJPanel, new JScrollPane(_help_JTextArea),
		0, 0, 1, 1, 1, 1, 10, 10, 10, 10, 
		GridBagConstraints.BOTH, GridBagConstraints.WEST );

	// Bottom JPanel
	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout ( new FlowLayout(FlowLayout.CENTER));
	getContentPane().add ( "South", bottomJPanel );

	_close_JButton = new SimpleJButton ( "Close", this );
	bottomJPanel.add ( _close_JButton );
	
	if ( _help_key != null ) {
		_help_JButton = new SimpleJButton ( "More help", this );
		bottomJPanel.add ( _help_JButton );
	}
		
	pack();
	setSize ( width, height );
	GUIUtil.center ( this );

	setVisible ( true );

	// Clean up...

	gbl = null;
	propValue = null;
	centerJPanel = null;
	bottomJPanel = null;
}

public void windowActivated(WindowEvent e) {}
public void windowClosed(WindowEvent e) {}
public void windowClosing(WindowEvent e) {
	closeWindow();
}
public void windowDeactivated(WindowEvent e) {}
public void windowDeiconified(WindowEvent e) {}
public void windowIconified(WindowEvent e) {}
public void windowOpened(WindowEvent e) {}

}
