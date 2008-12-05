//-----------------------------------------------------------------------------
// SelectDIADvisorDialog - general login dialog for DIADvisorDMI connections
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-03-29	Steven A. Malers, RTi	Copy and modify
//					SelectRiversideDBJDialog.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.DMI.DIADvisorDMI;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.GUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.Help.URLHelp;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.IO.ProcessManager;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This SelectDIADvisorDialog class provides the user with an interface to
select a DIADvisor host and ODBC connection.  Currently it is assumed that
DIADvisor always uses Microsoft Acccess.  Two connections are made, one for
the operational database and one for the archive database.
*/
public class SelectDIADvisorDialog extends Dialog
implements ActionListener, KeyListener, ItemListener, WindowListener {

/**
Whether the Dialog was closed with the cancel button.
*/

private TextField	__login_TextField, 		// User login
							// (not currently used).
                        __status_TextField;		// Text field at bottom
							// of the dialog
private TextField	__password_TextField;		// Text field for the
							// password
							// (not currently used).
private Choice		__OdbcDsn_Choice;		// List of available
							// ODBC DSN (operational
							// database).
private Choice		__OdbcDsn_archive_Choice;	// List of available
							// ODBC DSN (operational
							// database).
private SimpleJButton	__ok_Button, 
			__cancel_Button,
			__help_Button;    

private final String	__HELP_STRING	= "DIADvisorDMI.SelectDIADvisor";

private DIADvisorDMI __DIADvisor_dmi = null;		// Existing DIADvisorDMI
							// from calling
							// application
private DIADvisorDMI __DIADvisor_archive_dmi = null;	// Existing archive
							// DIADvisorDMI
							// from calling
							// application
private String	__user_login = null,	// User login
		__user_password = null,	// User password
		__OdbcDsn = null,	// ODBC DSN (operational databse)
		__OdbcDsn_archive= null;// ODBC DSN (archive databse)
private boolean __validate_login=false;	// Indicates whether the user
					// login/password should be entered and
					// validated
private List __available_OdbcDsn=null;// List of available ODBC DSN.

/**
Construct and display a SelectDIADvisorDialog.
@param parent Calling class.
@param DIADvisor_dmi DIADvisorDMI for the operational database that is currently
being used.  If the result
of the SelectDIADvisorDialog "OK", then whatever DIADvisorDMI that is in
effect will be available to the calling code (it will be null if the connection
failed).
@param DIADvisor_archive_dmi DIADvisorDMI for the archive database that is
currently being used.  If the result
of the SelectDIADvisorDialog "OK", then whatever DIADvisorDMI that is in
effect will be available to the calling code (it will be null if the connection
failed).
@param props Properties for the selection ("ValidateLogin"=true|false).
*/
public SelectDIADvisorDialog (	Frame parent, DIADvisorDMI DIADvisor_dmi,
				DIADvisorDMI DIADvisor_archive_dmi,
				PropList props )
{	super ( parent, true );
	try {	initialize (parent, DIADvisor_dmi, DIADvisor_archive_dmi,props);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, "", e );
	}
}

public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	
	if(command.equals("OK")) {
		ok_clicked();
	}  
	else if(command.equals("Cancel")) {
        	cancel_clicked();
	}
	else if(command.equals("Help")) {
		// Display on-line help for component.
        	URLHelp.showHelpForKey ( __HELP_STRING );
	}
}      

/**
Close the dialog without transferring any settings to the internal data.
*/
private void cancel_clicked()
{	// The DIADvisorDMIs are the ones that were passed into the constructor
	// (no need to do anything)...
	closeDialog();
}

/**
Check the database version.  If it is not recognized, print a warning for the
user.
@param DIADvisor_dmi DIADvisorDMI operational database instance to check
version.
@param DIADvisor_archive dmi DIADvisorDMI archive database instance to check
version.
*/
private void checkDatabaseVersion (	DIADvisorDMI DIADvisor_dmi,
					DIADvisorDMI DIADvisor_archive_dmi )
{	String message, routine = "SelectDIADvisorJDialog.checkDatabaseVersion";
	long dbversion = DIADvisor_dmi.getDatabaseVersion();
	long dbversion_archive = DIADvisor_archive_dmi.getDatabaseVersion();
	if ( dbversion == 0 ) {
		message = "The operational DIADvisor version (" + dbversion +
		") is not recognized.  " + IOUtil.getProgramName() +
		" may not run correctly.";
		Message.printWarning ( 1, routine, message );
	}
	else if ( dbversion < DIADvisorDMI.VERSION_LATEST ) {
		message = "The operational database design version (" +
		dbversion +
		") is not the latest version\n" +
		"recognized by the software (" + DIADvisorDMI.VERSION_LATEST
		+ ").\n" + IOUtil.getProgramName() +
		" is backward-compatible where possible.";
		Message.printWarning ( 2, routine, message );
	}
	if ( dbversion_archive == 0 ) {
		message = "The archive DIADvisor version (" + dbversion_archive+
		") is not recognized.  " + IOUtil.getProgramName() +
		" may not run correctly.";
		Message.printWarning ( 1, routine, message );
	}
	else if ( dbversion_archive < DIADvisorDMI.VERSION_LATEST ) {
		message = "The archive database design version (" +
		dbversion_archive + ") is not the latest version\n" +
		"recognized by the software (" + DIADvisorDMI.VERSION_LATEST
		+ ").\n" + IOUtil.getProgramName() +
		" is backward-compatible where possible.";
		Message.printWarning ( 2, routine, message );
	}
	if ( dbversion != dbversion_archive ) {
		message = "The operational DIADvisor version (" + dbversion +
		") and archive version (" + dbversion_archive+
		") are not the same.  " + IOUtil.getProgramName() +
		" may not run correctly.";
		Message.printWarning ( 1, routine, message );
	}
}

/**
Close the dialog and dispose of graphical resources.
*/
private void closeDialog()
{	super.setVisible(false);
	dispose();
}

/**
Return the DIADvisorDMI for the archive database associated with this dialog,
which is either the original archive DIADvisorDMI or a new one as the result of
current input.
*/
public DIADvisorDMI getArchiveDIADvisorDMI ()
{	return __DIADvisor_archive_dmi;
}

/**
Return the DIADvisorDMI for the operational database associated with this
dialog, which is either the original DIADvisorDMI or a new one as the result of
current input.
*/
public DIADvisorDMI getDIADvisorDMI ()
{	return __DIADvisor_dmi;
}

/**
Initialize the dialog components.
*/
private void initialize (	Frame parent, DIADvisorDMI DIADvisor_dmi,
				DIADvisorDMI DIADvisor_archive_dmi,
				PropList props )
{	String routine = "SelectDIADvisorDialog.initialize";
	__DIADvisor_dmi = DIADvisor_dmi;
	__DIADvisor_archive_dmi = DIADvisor_archive_dmi;
	String prop_value = props.getValue ( "ValidateLogin" );
	if ( prop_value != null ) {
		if ( prop_value.equalsIgnoreCase("true") ) {
			__validate_login = true;
		}
		else {	__validate_login = false;
		}
	}
	String	user_login = "";
	if ( __DIADvisor_dmi == null ) {
		// This is the first time a database connection has been
		// defined for the calling application so use the database
		// host information that was set on the command line for the
		// application and passed into this class...
		user_login = "";
	}
	else {	// A previous connection is in effect.
		user_login = __DIADvisor_dmi.getUserLogin();
	}

	addWindowListener(this);
                
        // used in the GridBagLayouts
        Insets LTB_insets = new Insets(7,7,0,0);
        Insets RTB_insets = new Insets(7,0,0,7);
        GridBagLayout gbl = new GridBagLayout();

        // North Panel
	Panel north_Panel = new Panel();
	north_Panel.setLayout(new BorderLayout());
	add("North", north_Panel);
        
        // North West JPanel
	Panel northW_Panel = new Panel();
	northW_Panel.setLayout(gbl);
	north_Panel.add("West", northW_Panel);

	int y = -1;	// Vertical position of components.

	// Always create the components so that they can be assigned default
	// values.  However, only actually add to the interface if they are
	// needed for interactive use...

	// Login...
	if ( __validate_login ) {
        	GUIUtil.addComponent(northW_Panel,
			new Label("DIADvisor Login:"), 
			0, ++y, 1, 1, 0, 0, LTB_insets, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
        __login_TextField = new TextField(25);
	__login_TextField.addKeyListener(this);
	__login_TextField.setText ( user_login );
	if ( __validate_login ) {
	        GUIUtil.addComponent(northW_Panel, __login_TextField, 
			1, y, 1, 1, 0, 0, RTB_insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}

	// Password...
        
	if ( __validate_login ) {
	        GUIUtil.addComponent(northW_Panel,
			new Label("DIADvisor Password:"), 
			0, ++y, 1, 1, 0, 0, LTB_insets, GridBagConstraints.NONE, GridBagConstraints.EAST);
	}
	__password_TextField = new TextField(25);
	//__password_TextField.setEchoChar('*');
	//__password_TextField.setText ( user_password );
	//__password_TextField.addKeyListener(this);
	if ( __validate_login ) {
		GUIUtil.addComponent(northW_Panel, __password_TextField, 
			1, y, 1, 1, 0, 0, RTB_insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	}

	// Try to get the data source names from the system...

	// Run a process to get the Available ODBC DSN.  Rely on the timeout
	// rather than a STOP code so that the shellcon program does not need to
	// print a stop code.
	List output = null;
	if (!IOUtil.isUNIXMachine()) {
		try {	String [] command_array = new String[2];
			command_array[0] = "shellcon";
			command_array[1] = "-dsn";
			ProcessManager pm = new ProcessManager(command_array);
			pm.saveOutput(true);
			pm.run ();
			output = pm.getOutputList();
			Message.printStatus ( 1, routine, "Exit status from shellcon for ODBC is: " + pm.getExitStatus() );
			// Finish the process...
			pm = null;
		}
		catch (Exception e) {
			// Won't work if running as an Applet!
			Message.printWarning ( 2, routine, e );
			output = null;
		}
	}

	__available_OdbcDsn = new Vector();
	if ((output != null) && (output.size() > 0)) {
		output = StringUtil.sortStringList (output, StringUtil.SORT_ASCENDING, null, false, true);
		int size = output.size();
		String dsn = "";
		// Only add DSN that have "DIAD" in the name...
		for (int i = 0; i < size; i++) {
			dsn = ((String)output.get(i)).trim();
			if ( (StringUtil.indexOfIgnoreCase(dsn,"DIAD",0) >= 0)
				&& !dsn.regionMatches(true,0,"STOP ",0,5) ) {
				__available_OdbcDsn.add(dsn);
			}
		}
	}
	// Items will be added to this in "connectionSelected"...
	Label definedDataSourceNames_Label =
		new Label("DIADvisor Operational ODBC DSNs:");
       	GUIUtil.addComponent(northW_Panel, definedDataSourceNames_Label, 
		0, ++y, 1, 1, 0, 0, LTB_insets, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OdbcDsn_Choice = new Choice();
	if (	(__available_OdbcDsn == null) ||
		(__available_OdbcDsn.size() == 0) ) {
		__OdbcDsn_Choice.addItem ( "Unable to Determine");
	}
	else {	int size = __available_OdbcDsn.size();
		for ( int i = 0; i < size; i++ ) {
			__OdbcDsn_Choice.addItem(
				(String)__available_OdbcDsn.get(i) );
		}
	}
       	GUIUtil.addComponent(northW_Panel, __OdbcDsn_Choice,
		1, y, 1, 1, 0, 0, RTB_insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
	Label definedDataSourceNames_archive_Label =
		new Label("DIADvisor Archive ODBC DSNs:");
       	GUIUtil.addComponent(northW_Panel, definedDataSourceNames_archive_Label,
		0, ++y, 1, 1, 0, 0, LTB_insets, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__OdbcDsn_archive_Choice = new Choice();
	if (	(__available_OdbcDsn == null) ||
		(__available_OdbcDsn.size() == 0) ) {
		__OdbcDsn_Choice.addItem ( "Unable to Determine");
	}
	else {	int size = __available_OdbcDsn.size();
		for ( int i = 0; i < size; i++ ) {
			__OdbcDsn_archive_Choice.addItem(
				(String)__available_OdbcDsn.get(i) );
		}
	}
       	GUIUtil.addComponent(northW_Panel, __OdbcDsn_archive_Choice,
		1, y, 1, 1, 0, 0, RTB_insets, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	
	if (IOUtil.isApplet()) {
		definedDataSourceNames_Label.setVisible(false);
		__OdbcDsn_Choice.setVisible(false);
		definedDataSourceNames_archive_Label.setVisible(false);
		__OdbcDsn_archive_Choice.setVisible(false);
	}

	// South Panel
	Panel south_Panel = new Panel();
	south_Panel.setLayout(new BorderLayout());
	add("South", south_Panel);

	// South North Panel
	Panel southN_Panel = new Panel();
	southN_Panel.setLayout(new FlowLayout(FlowLayout.CENTER));
	south_Panel.add("North", southN_Panel);

	__ok_Button = new SimpleJButton("OK","OK",this);
	southN_Panel.add(__ok_Button);

	__cancel_Button = new SimpleJButton("Cancel","Cancel",this);
	southN_Panel.add(__cancel_Button);

	__help_Button = new SimpleJButton("Help","Help",this);
	__help_Button.setEnabled ( false );
	southN_Panel.add(__help_Button);

	// South South JPanel
	Panel southS_Panel = new Panel();
	southS_Panel.setLayout(gbl);
	south_Panel.add("South", southS_Panel);

	__status_TextField = new TextField();
	__status_TextField.setBackground(Color.lightGray);
	__status_TextField.setEditable(false);
	GUIUtil.addComponent(southS_Panel, __status_TextField, 
		0, 0, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// frame settings        
	setBackground(Color.lightGray);        
	setTitle("Select DIADvisor Databases");
	if ( __validate_login ) {
		__status_TextField.setText (
		"Enter login information and select the DIADvisor databases" +
		" to open." );
	}
	else {	__status_TextField.setText (
		"Select the DIADvisor databases to open." );
	}
	pack();
	GUIUtil.center(this);
	setResizable(false);    	
	setVisible ( true );
}
   
/**
Handle item state changed events.
*/
public void itemStateChanged(ItemEvent event)
{
}

public void keyPressed(KeyEvent evt) {
	int code = evt.getKeyCode(); 

	// enter key is the same as ok
	if (code == KeyEvent.VK_ENTER) {
       		ok_clicked();
	}
	// F1 envokes help
	else if (code == KeyEvent.VK_F1) {
        	URLHelp.showHelpForKey ( __HELP_STRING );
	}
	else if ((code == KeyEvent.VK_TAB) &&
		(evt.getComponent() == __login_TextField)) {
		// Deselect the login...
		__login_TextField.select(0,0);
	}
	else if ((code == KeyEvent.VK_TAB) &&
		(evt.getComponent() == __password_TextField)) {
		// Deselect the password...
		__password_TextField.select(0,0);
	}
}

public void keyReleased(KeyEvent event) {
	int code = event.getKeyCode();
	if (	(code == KeyEvent.VK_TAB) &&
		(event.getComponent() == __login_TextField)) {
		__login_TextField.setCaretPosition(0);
		__login_TextField.selectAll();
	}
	else if ((code == KeyEvent.VK_TAB) &&
		(event.getComponent() == __password_TextField)) {
		__password_TextField.setCaretPosition(0);
		__password_TextField.selectAll();
	}
}

/**
Ignore key typed events - deal with in the other key events.
*/
public void keyTyped(KeyEvent event)
{
}

/**
Use the information in the dialog to try to instantiate a new DIADvisorDMI
instance.  If successful, save the new DIADvisorDMI information and close the
dialog.
*/
private void ok_clicked()
{	String routine = "SelectDIADvisorDialog.ok_clicked";

	DIADvisorDMI DIADvisor_dmi = null;
	DIADvisorDMI DIADvisor_archive_dmi = null;
	try {	DIADvisor_dmi = openDatabase ( 0 );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
		// Enough messages should be in openDatabase()...
		DIADvisor_dmi = null;
	}
	try {	DIADvisor_archive_dmi = openDatabase ( 1 );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
		// Enough messages should be in openDatabase()...
		DIADvisor_archive_dmi = null;
	}
	if ( (DIADvisor_dmi != null) && (DIADvisor_archive_dmi != null) ) {
		checkDatabaseVersion ( DIADvisor_dmi, DIADvisor_archive_dmi );
		// Save the connection.  The calling code can retrieve this to
		// store for additional queries.
		__DIADvisor_dmi = DIADvisor_dmi;
		__DIADvisor_archive_dmi = DIADvisor_archive_dmi;
		closeDialog ();
	}
	// Set the cross-reference information since DIADvisor needs a DMI for
	// the operational and archive databases...
	if ( __DIADvisor_dmi != null ) {
		__DIADvisor_dmi.setArchiveDMI ( __DIADvisor_archive_dmi );
		__DIADvisor_dmi.setOperationalDMI ( __DIADvisor_dmi );
	}
	if ( __DIADvisor_archive_dmi != null ) {
		__DIADvisor_archive_dmi.setArchiveDMI (__DIADvisor_archive_dmi);
		__DIADvisor_archive_dmi.setOperationalDMI ( __DIADvisor_dmi );
	}
	// else... User has been warned about problems in openDatabase() and
	// needs to cancel or get it right.
}

/**
Attempt to use the information in the dialog to try to instantiate a new
DIADvisorDMI instance.
@param db Flag indicating wether operational (0) or archive (1) database should
be opened.
@return DIADvisorDMI instance for the new connection, or null if the connection
failed.
*/
private DIADvisorDMI openDatabase ( int db ) 
throws Exception
{	String routine = "SelectDIADvisorDialog.openDataBase";

	// Fill information in a new DIADvisorDMI instance.  Intantiating using
	// no arguments defaults the system login and system password to the
	// correct values...

	DIADvisorDMI DIADvisor_dmi = null;

	// Save this information in case it is needed elsewhere...

	String odbc_dsn = null;
	if ( db == 0 ) {
		__OdbcDsn = (String)__OdbcDsn_Choice.getSelectedItem();
		odbc_dsn = __OdbcDsn;
	}
	else {	__OdbcDsn_archive =
			(String)__OdbcDsn_archive_Choice.getSelectedItem();
		odbc_dsn = __OdbcDsn_archive;
	}
	if (__validate_login) {
		// Validation is requested so use the the values from the
		// dialog...
		__user_login = __login_TextField.getText().trim();
		__user_password = __password_TextField.getText().trim();
	}
	else {	// Default to guest...
		__user_login = "";
		__user_password = "";
	}

	DIADvisor_dmi = new DIADvisorDMI ( "Access", odbc_dsn, null, null );
	try {	DIADvisor_dmi.open();
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, routine,
		"Unable to open DIADvisor database \"" + odbc_dsn + "\"" );
		DIADvisor_dmi = null;
		Message.printWarning ( 2, routine, e );
		__status_TextField.setText (
		"DIADvisor connection failed.  Ready" );
		return null;
	}
	if ( DIADvisor_dmi != null ) {
		DIADvisor_dmi.setUserLogin ( __user_login );
		DIADvisor_dmi.setUserPassword ( __user_password );
	}
	return DIADvisor_dmi;
}

public void setVisible(boolean state) {
	if (state) {
		// Setting visible.  Position the cursor on the first visible
		// component...
		if ( __validate_login ) {
        		__login_TextField.setText(__user_login);
                	__password_TextField.setText(__user_password);

			// set the initial caret position ...
			//__passwordJasswordField.setCaretPosition(0);
			//__passwordJPasswordField.select(0, 0);
			__login_TextField.setCaretPosition(0);
			__login_TextField.selectAll();
		}
		else {	// Position in the connection display.
		}
        }
        super.setVisible(state);
}

public void windowActivated(WindowEvent evt)	{}
public void windowClosed(WindowEvent evt)	{}
public void windowClosing(WindowEvent evt) {
	cancel_clicked();
}
public void windowDeactivated(WindowEvent evt)	{}
public void windowDeiconified(WindowEvent evt)	{}
public void windowIconified(WindowEvent evt)	{}
public void windowOpened(WindowEvent evt) 	{}

} // end SelectDIADvisorDialog
