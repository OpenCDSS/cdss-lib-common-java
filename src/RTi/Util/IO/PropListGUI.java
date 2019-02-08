// PropListGUI - this class implements a GUI for the PropList class.

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

// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.

// TODO SAM 2007-05-09 Is this class used anymore?

package	RTi.Util.IO;

import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
This class implements a GUI for the PropList class.
<b>This class is under development.</b>
*/
@SuppressWarnings("serial")
public class PropListGUI extends Panel implements ActionListener
{

/*
public PropListGUI ( PropList prop_list )
{
	super( "Prop List" );

	_prop_list = prop_list;
	openGUI ();
}

*/
// Notes:	(1)	This is needed to implement the ActionListener
//			interface.
public void actionPerformed ( ActionEvent e )
{
/*
	String	routine = "PropListGUI.actionPerformed";
	// Check the names of the events.  These are tied to menu names.
	String command = e.getActionCommand();
	if ( command.equals("SecurityCheck") ) {
		// The main menu choice... Make the GUI visible...
		setVisible(true);
	}
	if ( command.equals("PropList.ClearProperty") ) {
		// Clear the property value...
		setVisible(false);
	}
	else if ( command.equals("SecurityCheck.Defaults") ) {
		// Use the defaults for displays...
		setToDefaults ();
		refresh();
	}
	else if ( command.equals("SecurityCheck.Exit") ) {
		closeGUI (0);
	}
	else if ( command.equals("SecurityCheck.Refresh") ) {
		refresh ();
	}
*/
}

/*
// Attach this GUI's menus to the calling code...
public void attachMainMenu ( Menu menu )
{
	menu.add ( new SimpleMenuItem("Security","SecurityCheck",this) );
}

// For now just hide.
public void closeGUI ( int status )
{
	setVisible ( false );
}

// Do the work of opening the GUI...
public void openGUI ( int mode )
{
	String routine = "SecurityFrameGUI";

	setBackground ( Color.lightGray );

	Panel top_p = new Panel();

	MenuBar menu_bar = new MenuBar();
	Menu file_menu = new Menu ( "File" );
	file_menu.add ( new SimpleMenuItem("Refresh","SecurityCheck.Refresh",
		this) );
	file_menu.add ( new SimpleMenuItem("Exit","SecurityCheck.Exit",
		this) );

	top_p.setLayout( new GridBagLayout() );
	GridBagConstraints gbc = new GridBagConstraints();

	// All we want to do right now is check some security stuff and print
	// messages...

	// See if a security manager is installed...

	int	y = 0;
	SecurityManager sm = System.getSecurityManager();
	GUI.addComponent(	top_p,
				new Label("SecurityManager installed:"),
				0, y, 3, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_security_manager_field = new Label ( "" );
	GUI.addComponent(	top_p,
				_security_manager_field,
				3, y, 2, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// See if we can write to the local system...

	++y;
	GUI.addComponent(	top_p,
				new Label("File:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_to_write_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_file_to_write_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can write?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_writeable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_file_writeable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// See if we can read a file...

	++y;
	GUI.addComponent(	top_p,
				new Label("File:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_to_read_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_file_to_read_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can read?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_readable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_file_readable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Now display the information about whether we can delete from disk...

	++y;
	GUI.addComponent(	top_p,
				new Label("File:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_to_delete_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_file_to_delete_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can delete?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_file_deleteable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_file_deleteable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check printer access...

	++y;
	GUI.addComponent(	top_p,
				new Label("Can submit print jobs?:"),
				0, y, 3, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_print_job_field = new Label ( "" );
	GUI.addComponent(	top_p,
				_print_job_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check clipboard access...

	++y;
	GUI.addComponent(	top_p,
				new Label("Can access clipboard?:"),
				0, y, 3, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_clipboard_access_field = new Label ( "" );
	GUI.addComponent(	top_p,
				_clipboard_access_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check the event queue...

	++y;
	GUI.addComponent(	top_p,
				new Label("Can access AWT queue?:"),
				0, y, 3, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_awt_access_field = new Label ( "" );
	GUI.addComponent(	top_p,
				_awt_access_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check the command...

	++y;
	GUI.addComponent(	top_p,
				new Label("Command:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_command_to_exec_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_command_to_exec_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can exec?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_command_execable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_command_execable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check SQL package load...

	++y;
	GUI.addComponent(	top_p,
				new Label("Package:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_package_to_load_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_package_to_load_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can load?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_package_loadable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_package_loadable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );

	// Check to see if we can load a DLL...

	++y;
	GUI.addComponent(	top_p,
				new Label("DLL:"),
				0, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_dll_to_link_field = new TextField ( "" );
	GUI.addComponent(	top_p,
				_dll_to_link_field,
				1, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	GUI.addComponent(	top_p,
				new Label("Can link?:"),
				2, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );
	_dll_linkable_field = new Label ( "No" );
	GUI.addComponent(	top_p,
				_dll_linkable_field,
				3, y, 1, 1, 0, 0,
				gbc.HORIZONTAL, gbc.WEST );


	add( "North", top_p );

	// Now add the buttons at the bottom...

	Panel bottom_p = new Panel();
	bottom_p.setLayout( new FlowLayout(FlowLayout.CENTER) );
	bottom_p.add (new SimpleButton("Refresh","SecurityCheck.Refresh",this));
	bottom_p.add (new SimpleButton("Set to Defaults",
			"SecurityCheck.Defaults",this));
	bottom_p.add (new SimpleButton("Close","SecurityCheck.Close",this));
	add( "South", bottom_p );

	// Now clean up...

	setMenuBar ( menu_bar );
	enableEvents( AWTEvent.WINDOW_EVENT_MASK );
	if ( (mode & GUI.GUI_VISIBLE) != 0 ) {
		// We want to see the GUI at creation...
		setVisible(true);
	}
	else {	// We don't want to see the GUI at creation...
		setVisible(false);
	}

	// Now we refresh what the GUI is displaying...
	setToDefaults();
	refresh ();
	pack ();
}

public void processWindowEvent( WindowEvent evt )
{
	if( evt.getID() == WindowEvent.WINDOW_CLOSING ){
		setVisible( false );
	}
}

// This function takes the data fields and refreshes the results by running
// the security checks again.
public void refresh ()
{	String	routine = "SecurityCheckGUI.refresh";

	SecurityManager sm = System.getSecurityManager();
	if ( sm == null ) {
		Message.printWarning ( 2, routine,
		"Get null SecurityManager - no SecurityManager installed!" );
		_security_manager_field.setText("None");
	}
	else {	// Get the class name...
		Class c = sm.getClass();
		_security_manager_field.setText(c.getName());
	}

	// See if we can write to the local system...

	String testfile = _file_to_write_field.getText();
	if ( SecurityCheck.canWriteFile(testfile) ) {
		_file_writeable_field.setText ( "Yes" );
	}
	else {	_file_writeable_field.setText ( "No" );
	}

	// Now display the information about whether we can delete from disk...

	testfile = _file_to_delete_field.getText();
	if ( SecurityCheck.canDeleteFile(testfile) ) {
		_file_deleteable_field.setText ( "Yes" );
	}
	else {	_file_deleteable_field.setText ( "No" );
	}

	// See if we can read a file...

	testfile = _file_to_read_field.getText();
	if ( SecurityCheck.canReadFile(testfile) ) {
		_file_readable_field.setText ( "Yes" );
	}
	else {	_file_readable_field.setText ( "No" );
	}

	// Check printer access...

	if ( SecurityCheck.canPrint() ) {
		_print_job_field.setText ( "Yes" );
	}
	else {	_print_job_field.setText ( "No" );
	}

	// Check clibboard access...

	if ( SecurityCheck.canUseClipboard() ) {
		_clipboard_access_field.setText ( "Yes" );
	}
	else {	_clipboard_access_field.setText ( "No" );
	}

	// Check the command...

	String command = _command_to_exec_field.getText();
	if ( SecurityCheck.canExec(command) ) {
		_command_execable_field.setText ( "Yes" );
	}
	else {	_command_execable_field.setText ( "No" );
	}

	// Check to see if we can load a DLL...

	String dll = _dll_to_link_field.getText();
	if ( SecurityCheck.canLinkDLL(dll) ) {
		_dll_linkable_field.setText ( "Yes" );
	}
	else {	_dll_linkable_field.setText ( "No" );
	}

	// Check SQL package...

	String some_package = _package_to_load_field.getText();
	if ( SecurityCheck.canLoadPackage(some_package) ) {
		_package_loadable_field.setText ( "Yes" );
	}
	else {	_package_loadable_field.setText ( "No" );
	}

	// Check the event queue...

	if ( SecurityCheck.canCheckAWTEventQueue() ) {
		_awt_access_field.setText ( "Yes" );
	}
	else {	_awt_access_field.setText ( "No" );
	}
}

// Set the field values to the defaults...
public void setToDefaults ()
{
	// Need to make this platform-specific...

	if ( IO.isUNIXMachine() ) {
		_command_to_exec_field.setText ( "ls /" );
		_dll_to_link_field.setText ( "???" );
		_file_to_delete_field.setText ( "/tmp/SecurityCheck.tmp" );
		_file_to_read_field.setText ( "/etc/hosts" );
		_file_to_write_field.setText ( "/tmp/SecurityCheck.tmp" );
		_package_to_load_field.setText ( "java.sql.ResultSet" );
	}
	else {	_command_to_exec_field.setText ( "dir C:\\" );
		_dll_to_link_field.setText (
			"C:\\Windows\\System\\JdbcOdbc.dll" );
		_file_to_delete_field.setText ( "C:\\SecurityCheck.tmp" );
		_file_to_read_field.setText ( "C:\\Autoexec.bat" );
		_file_to_write_field.setText ( "C:\\SecurityCheck.tmp" );
		_package_to_load_field.setText ( "java.sql.ResultSet" );
	}
}
*/
} // End SecurityCheckGUI class
