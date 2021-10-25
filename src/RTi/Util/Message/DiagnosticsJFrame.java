// DiagnosticsJFrame - generic diagnostic preferences window.

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

package RTi.Util.Message;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJMenuItem;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;

/**
This class provides a simple GUI for setting various diagnostic information for
a Java application.  Quite often, this GUI should only be enabled when
diagnostics need to be provided.
An example of how to implement the GUI is as follows (note that this uses
resources at program initialization but it does allow for very simple use of
the component):
<p>

<pre>
	DiagnosticsJFrame diagnostics_gui = new DiagnosticsJFrame();
	diagnostics_gui.attachMainMenu( _view_menu );
</pre>
<p>

The GUI provides access to various methods of the Message class.
It is assumed that the log file has been set.
<b>In the future, greater control of the log file will be added.</b>
@see Message
*/
@SuppressWarnings("serial")
public class DiagnosticsJFrame extends JFrame
implements ActionListener, ItemListener, WindowListener
{

private JTextField __consoleDebugLevel_JTextField;
private JTextField __consoleWarningLevel_JTextField;
private JTextField __consoleStatusLevel_JTextField;
private JTextField __guiDebugLevel_JTextField;
private JTextField __guiWarningLevel_JTextField;
private JTextField __guiStatusLevel_JTextField;
private JTextField __logDebugLevel_JTextField;
private JTextField __logWarningLevel_JTextField;
private JTextField __logStatusLevel_JTextField;

private JLabel __logfileName_JLabel;

private JCheckBox __debug_JCheckBox;
private JCheckBox __message_JCheckBox;
private JCheckBoxMenuItem __menu_JCheckBoxMenuItem = null;
private JList<String> __message_JList;
private DefaultListModel<String> __message_JListModel;

private int __list_max;

/**
 * Parent component is used to position this component.
 */
private Component __parent = null;

/**
Constructor the GUI but do not make visible.
@param parent UI parent component, used to center dialog
*/
public DiagnosticsJFrame(Component parent)
{	super ( "Diagnostics" );
	__list_max = 100;
	__parent = parent;
	openGUI ( 0 );
}

/**
Constructor the GUI but do not make visible.
*/
public DiagnosticsJFrame()
{	super ( "Diagnostics" );
	__list_max = 100;
	openGUI ( 0 );
}

/**
Construct and specify the help key (the GUI will not be visible at construction).
@param help_key Help key for the URLHelp class.
@see RTi.Util.Help.URLHelp
@deprecated.
*/
public DiagnosticsJFrame ( String help_key )
{	super ( "Diagnostics" );
	__list_max = 100;
	openGUI ( 0 );
}

/**
Handle ActionEvent events.
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e )
{   String rtn = "DiagnosticsGUI.actionPerformed";
    String command = e.getActionCommand();

    if ( command.equals("Apply") ) {
        applySettings();
    }
    else if ( command.equals("Cancel") ) {
        setVisible(false);
        if ( __menu_JCheckBoxMenuItem != null ) {
            __menu_JCheckBoxMenuItem.setState ( false );
        }
    }
    else if ( command.equals("Close") ) {
        applySettings();
        setVisible(false);
        if ( __menu_JCheckBoxMenuItem != null ) {
            __menu_JCheckBoxMenuItem.setState ( false );
        }
    }
    else if ( command.equals ( "DiagnosticsGUI" )) {
        setVisible(true);
        __debug_JCheckBox.setSelected ( Message.isDebugOn );
        if ( __menu_JCheckBoxMenuItem != null ) {
            __menu_JCheckBoxMenuItem.setState ( true );
        }
    }
    else if (command.equals("ViewLogFile")) {
        String logfile = Message.getLogFile();
        if (logfile.equals("")) {
            return;
        }

        try {
            PropList p = new PropList("");
            p.set("NumberSummaryListLines=10");
            p.set("NumberLogLines=25");
            p.set("ShowSummaryList=true");
            MessageLogJFrame logJFrame = null;
            if ( (__parent != null) && (__parent instanceof JFrame) ) {
            	logJFrame = new MessageLogJFrame((JFrame)__parent, p);
            }
            else {
            	logJFrame = new MessageLogJFrame(this, p);
            }
            logJFrame.processLogFile(Message.getLogFile());
            logJFrame.setVisible(true);
        }
        catch (Exception ex) {
            Message.printWarning(1, rtn, "Unable to view log file \"" + Message.getLogFile() + "\"");
            Message.printWarning(2, rtn, ex);
        }       
    }
    else if ( command.equals ("Flush") ) {
        Message.flushOutputFiles(0);
        Message.printStatus ( 1, rtn, "Log file updated." );
    }
    else if ( command.equals("LaunchLogFileViewer") ) {
        String [] command_array = new String[2];
        // Try using external viewer.  Otherwise, display a warning.
        command_array[0] = getExternalFileViewerProgram ();
        try {
            Message.flushOutputFiles(0);
            command_array[1] = Message.getLogFile();
            ProcessManager p = new ProcessManager ( command_array );
            // This will run as a thread until the process is shut down...
            Thread t = new Thread ( p );
            t.start ();
        }
        catch ( Exception e2 ) {
            Message.printWarning ( 1, rtn, "Unable to view log file \"" + Message.getLogFile() + "\" using " +
                    command_array[0] );
        }
        Message.flushOutputFiles(0);
    }
    else if (command.equals("restart")) {
        int x = (new ResponseJDialog(this, "Start a new log file?",
            "Are you sure you want to restart the log file?  All "
            + "contents currently in the log file will be lost.",
            ResponseJDialog.YES | ResponseJDialog.NO)).response();
        if (x == ResponseJDialog.NO) {
            return;
        }
        try {
            Message.restartLogFile();
        }
        catch (Exception ex) {
            Message.printWarning(1, rtn, "Unable to re-open log file.");
            Message.printWarning(2, rtn, ex);
        }
    }
    else if (command.equals("new")) {
        Message.closeLogFile();
        JGUIUtil.setWaitCursor(this, true);
        String lastDirectorySelected = JGUIUtil.getLastFileDialogDirectory();
    
        JFileChooser fc = JFileChooserFactory.createJFileChooser ( lastDirectorySelected );
    
        fc.setDialogTitle("Select Log File");
        SimpleFileFilter htmlFF = new SimpleFileFilter("log", "Log Files");
        fc.addChoosableFileFilter(htmlFF);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(htmlFF);
        fc.setDialogType(JFileChooser.OPEN_DIALOG); 
    
        JGUIUtil.setWaitCursor(this, false);
        int retVal = fc.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
    
        String currDir = (fc.getCurrentDirectory()).toString();
    
        if (!currDir.equalsIgnoreCase(lastDirectorySelected)) {
            JGUIUtil.setLastFileDialogDirectory(currDir);
        }
        String path = fc.getSelectedFile().getPath();

        try {
            Message.openNewLogFile(path);
        }
        catch (Exception ex) {
            Message.printWarning(1, rtn, "Unable to open new log file: " + path);
            Message.printWarning(2, rtn, ex);
        }                   
    }
}

/**
Apply the settings in the GUI.  This is called when "Apply" or "Close" are pressed.
*/
private void applySettings () {
    String rtn = "DiagnosticsGUI.applySettings";
    Message.setDebugLevel ( Message.TERM_OUTPUT,
        StringUtil.atoi ( __consoleDebugLevel_JTextField.getText() ) );
    Message.setWarningLevel ( Message.TERM_OUTPUT,
        StringUtil.atoi ( __consoleWarningLevel_JTextField.getText() ));
    Message.setStatusLevel ( Message.TERM_OUTPUT,
        StringUtil.atoi ( __consoleStatusLevel_JTextField.getText() ) );
    Message.setDebugLevel ( Message.STATUS_HISTORY_OUTPUT,
        StringUtil.atoi ( __guiDebugLevel_JTextField.getText() ) );
    Message.setWarningLevel ( Message.STATUS_HISTORY_OUTPUT,
        StringUtil.atoi ( __guiWarningLevel_JTextField.getText() ) );
    Message.setStatusLevel ( Message.STATUS_HISTORY_OUTPUT,
        StringUtil.atoi ( __guiStatusLevel_JTextField.getText() ) );
    Message.setDebugLevel ( Message.LOG_OUTPUT,
        StringUtil.atoi ( __logDebugLevel_JTextField.getText() ) );
    Message.setWarningLevel ( Message.LOG_OUTPUT,
        StringUtil.atoi ( __logWarningLevel_JTextField.getText() ) );
    Message.setStatusLevel ( Message.LOG_OUTPUT,
        StringUtil.atoi ( __logStatusLevel_JTextField.getText() ) );

    Message.isDebugOn = __debug_JCheckBox.isSelected();

    if ( Message.isDebugOn ) {
        Message.printStatus ( 1, rtn, "Setting debug to " + 
        Message.getDebugLevel ( Message.STATUS_HISTORY_OUTPUT ) +
        " (STATUS_HISTORY_OUTPUT) " + Message.getDebugLevel ( Message.LOG_OUTPUT ) + " (LOG_OUTPUT)");
    }
    else {
        Message.printStatus ( 1, rtn, "Debug has been turned off" );
    }
}

/**
Attach the DiagnosticsGUI menus to the given menu.  The menu will be labeled "Diagnostics Preferences...".
@param menu Menu to attach to.
*/
public void attachMainMenu ( JMenu menu )
{   menu.add ( new SimpleJMenuItem ( "Diagnostics...", "DiagnosticsGUI", this) );
    menu.add(new SimpleJMenuItem("Diagnostics - View Log File ...", "ViewLogFile", this));
}

/**
Attach the DiagnosticsGUI menus to the given menu.  The menu will be labeled "Diagnostics Preferences...".
@param menu Menu to attach to.
@param use_checkbox If true, use a CheckboxMenuItem.  If false, use a normal MenuItem.
*/
public void attachMainMenu ( JMenu menu, boolean use_checkbox )
{   if ( use_checkbox ) {
        __menu_JCheckBoxMenuItem = new JCheckBoxMenuItem ( "Diagnostics", isVisible());
        __menu_JCheckBoxMenuItem.addItemListener ( this );
            
        menu.add ( __menu_JCheckBoxMenuItem );
        menu.add(new SimpleJMenuItem("Diagnostics - View Log File ...", "ViewLogFile", this));      
    }
    else {
        attachMainMenu ( menu );
    }
}

/**
Return the name of the external program to use for viewing log files.  The program
should be in the path.  Currently this is not configurable.
@return the name of the external program to use to view the log file.
*/
private String getExternalFileViewerProgram ()
{
    if ( IOUtil.isUNIXMachine() ) {
        return "nedit";
    }
    else {
        return "notepad";
    }
}

/**
Handle ItemEvent events.
@param e ItemEvent to handle.
*/
public void itemStateChanged ( ItemEvent e )
{	if ( e.getSource() == __message_JCheckBox ) {
		if ( __message_JCheckBox.isSelected() ) {
			__message_JList.setVisible ( true );
		}
		else {
		    __message_JList.setVisible ( false );
		}
		pack();
	}
	else if ( e.getSource() == __menu_JCheckBoxMenuItem ) {
		// Use the CheckboxMenuItem
		setVisible(__menu_JCheckBoxMenuItem.getState());
		__debug_JCheckBox.setSelected ( Message.isDebugOn );
	}
}

/**
Construct with the flag.
@param mode Indicates how the GUI should be constructed.  Currently, the mode
can be 1 to indicated that the GUI should be visible on creation, and zero to
indicate that it should be hidden on creation.
*/
public void openGUI ( int mode )
{   String rtn = "openGUI";

    try {
        JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
    if ( (JGUIUtil.getAppNameForWindows() == null) || (JGUIUtil.getAppNameForWindows().length() == 0) ) {
        setTitle ( "Diagnostics" );
    }
    else {
        setTitle( JGUIUtil.getAppNameForWindows() + " - Diagnostics" );
    }

    // Set the visibility up front so that if not visible it does not
    // flash when drawing.  Reset at the end to the final value...

    setVisible ( false );

    __consoleDebugLevel_JTextField = new JTextField ( 4 );
    __consoleWarningLevel_JTextField = new JTextField ( 4 );
    __consoleStatusLevel_JTextField = new JTextField ( 4 );
    __guiDebugLevel_JTextField = new JTextField ( 4 );
    __guiWarningLevel_JTextField = new JTextField ( 4 );
    __guiStatusLevel_JTextField = new JTextField ( 4 );
    __logDebugLevel_JTextField = new JTextField ( 4 );
    __logWarningLevel_JTextField = new JTextField ( 4 );
    __logStatusLevel_JTextField = new JTextField ( 4 );
    __debug_JCheckBox = new JCheckBox ( "Allow debug", Message.isDebugOn );
    __message_JCheckBox = new JCheckBox ( "Show messages", true );
    __message_JListModel = new DefaultListModel<String> ();
    __message_JList = new JList<String>(__message_JListModel);
    JScrollPane message_JScrollPane = new JScrollPane ( __message_JList );

    JPanel p1 = new JPanel();
    GridLayout gl1 = new GridLayout ( 4, 4, 4, 6 );
    p1.setLayout ( gl1 );
    gl1 = null;

    p1.add ( new JLabel ("Message type") );
    p1.add ( new JLabel ("Console output") );
    p1.add ( new JLabel ("Status bar") );
    p1.add ( new JLabel ("Log file") );
    p1.add ( new JLabel ("") );
    p1.add ( new JLabel ("Status") );
    p1.add ( __consoleStatusLevel_JTextField );
    p1.add ( __guiStatusLevel_JTextField );
    p1.add ( __logStatusLevel_JTextField );
    p1.add ( new JLabel ("") );
    p1.add ( new JLabel ("Warning") );
    p1.add ( __consoleWarningLevel_JTextField );
    p1.add ( __guiWarningLevel_JTextField );
    p1.add ( __logWarningLevel_JTextField );
    p1.add ( new JLabel ("") );
    p1.add ( new JLabel ("Debug") );
    p1.add ( __consoleDebugLevel_JTextField );
    p1.add ( __guiDebugLevel_JTextField );
    p1.add ( __logDebugLevel_JTextField );
    p1.add ( __debug_JCheckBox );

    JPanel p2 = new JPanel();
    SimpleJButton launch_button = new SimpleJButton("Launch Log File Viewer", "LaunchLogFileViewer", this);
    launch_button.setToolTipText("Use " + getExternalFileViewerProgram() + " to view the log file.");
    // Disable if the log file is not known to the Message class or does not exist...
    String logfile = Message.getLogFile ();
    if ( logfile == null ) {
        launch_button.setEnabled ( false );
    }
    if ( !IOUtil.fileReadable(logfile) ) {
        launch_button.setEnabled ( false );
    }
    logfile = null;

    SimpleJButton viewButton = new SimpleJButton("View Log File", "ViewLogFile", this);
    viewButton.setToolTipText("Open the log file viewer/navigator.");
    p2.add(viewButton);
    p2.add ( launch_button );
    launch_button = null;

    SimpleJButton applyButton = new SimpleJButton("Apply",this);
    applyButton.setToolTipText("Apply changes to the logging levels.");
    p2.add(applyButton);

    SimpleJButton closeButton = new SimpleJButton("Close",this);
    closeButton.setToolTipText("Apply changes to the logging levels and close the window.");
    p2.add(closeButton);

    SimpleJButton cancelButton = new SimpleJButton("Cancel",this);
    cancelButton.setToolTipText("Close the window, losing any changes unless 'Apply' has been pressed.");
    p2.add(cancelButton);

    int y=0;
    JPanel main_JPanel = new JPanel();
    main_JPanel.setLayout ( new GridBagLayout() );
    getContentPane().add ( "Center", main_JPanel );
    JGUIUtil.addComponent ( 
        main_JPanel, new JLabel ( "More detailed messages are printed as the message level increases."),
        0, y, 1, 1, 
        1, 0,
        20, 1, 0, 1, 
        GridBagConstraints.NONE, GridBagConstraints.CENTER );
    JGUIUtil.addComponent ( 
        main_JPanel, new JLabel ( "(0 results in none of the messages being printed)"),
        0, ++y, 1, 1, 
        1, 0,
        0, 1, 0, 1, 
        GridBagConstraints.NONE, GridBagConstraints.CENTER );
    JGUIUtil.addComponent ( 
            main_JPanel, __logfileName_JLabel = new JLabel ( ""),
            0, ++y, 1, 1, 
            1, 0,
            0, 1, 0, 1, 
            GridBagConstraints.NONE, GridBagConstraints.CENTER );
    JGUIUtil.addComponent ( 
        main_JPanel, p1,
        0, ++y, 1, 1, 
        0, 0,
        4, 1, 14, 1, 
        GridBagConstraints.NONE, GridBagConstraints.CENTER );
    p1 = null;

    JGUIUtil.addComponent ( 
        main_JPanel, new JLabel ("Message history:"),
        0, ++y, 1, 1, 
        0, 0,
        GridBagConstraints.NONE, GridBagConstraints.WEST );
    JGUIUtil.addComponent ( 
        main_JPanel, __message_JCheckBox, 
        0, y, 1, 1, 
        0, 0,
        GridBagConstraints.NONE, GridBagConstraints.EAST );
    __message_JCheckBox.addItemListener(this);
    JGUIUtil.addComponent (
        main_JPanel, message_JScrollPane,
        0, ++y, 1, 1, 
        1, 1,
        GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    SimpleJButton flushButton = new SimpleJButton("Flush Log File","Flush",this);
    flushButton.setToolTipText("Force the log file buffer to flush, in "
        + "order to guarantee that all messages have been logged to the file.");

    SimpleJButton restartButton = new SimpleJButton("Restart Log File", "restart", this);
    restartButton.setToolTipText("Re-open the log file, "
        + "overwriting all text that currently is in the log file.");
    SimpleJButton newButton = new SimpleJButton("New Log File", "new", this);
    newButton.setToolTipText("Open a new log file for writing.");

    JPanel buttons = new JPanel();
    buttons.add(flushButton);
    buttons.add(restartButton);
    buttons.add(newButton);
    
    JGUIUtil.addComponent (
        main_JPanel, buttons,
        0, ++y, 1, 1, 
        1, 0, 
        GridBagConstraints.NONE, GridBagConstraints.CENTER );   
    
    JGUIUtil.addComponent ( 
        main_JPanel, p2,
        0, ++y, 1, 1, 
        0, 0,
        10, 1, 0, 1, 
        GridBagConstraints.NONE, GridBagConstraints.CENTER );
    p2 = null;

    Message.setOutputFunction ( Message.STATUS_HISTORY_OUTPUT, this, "printStatusMessages" );
    
    // Refresh the contents of the graphical components...
    refreshContents();

    // Listen for window events (close, etc.)...

    addWindowListener ( this );

    pack();
    JGUIUtil.center(this,__parent);

    if ( ( mode & JGUIUtil.GUI_VISIBLE) != 0 ) {
        setVisible(true);
    }
    else {
        setVisible(false);
    }

    } catch (Exception e) {
        e.printStackTrace();
        Message.printWarning ( 2, rtn, e );
    }
    rtn = null;
}

/**
Routine that is registered with the Message.printStatus routine and which
results in messages being printed to the history.
@param level Message level.
@param rtn Routine that is printing the message.
@param message Message to be printed.
*/
public void printStatusMessages ( int level, String rtn, String message )
{	while ( __message_JListModel.size() > __list_max ) {
		__message_JListModel.removeElementAt(0);
	}
	__message_JListModel.addElement ( message );
	int index = __message_JListModel.size() - 1;
	__message_JList.setSelectedIndex ( index );
	__message_JList.ensureIndexIsVisible ( index );
}

/**
Refresh the contents of the display.  This is called by setVisible() to ensure that
the display is consistent with memory.
*/
private void refreshContents ()
{
    String logfile = Message.getLogFile();
    if ( (logfile == null) || logfile.equals("") ) {
        __logfileName_JLabel.setText ( "No log file has been opened.  Verify the software configuration." );
    }
    else {
        __logfileName_JLabel.setText ( "Most recent log file = \"" + logfile + "\"" );
    }
    
    __consoleDebugLevel_JTextField.setText ( String.valueOf (
        Message.getDebugLevel ( Message.TERM_OUTPUT )));
    __consoleWarningLevel_JTextField.setText ( String.valueOf (
        Message.getWarningLevel ( Message.TERM_OUTPUT )));
    __consoleStatusLevel_JTextField.setText ( String.valueOf (
        Message.getStatusLevel ( Message.TERM_OUTPUT )));
    __guiDebugLevel_JTextField.setText ( String.valueOf (
        Message.getDebugLevel ( Message.STATUS_HISTORY_OUTPUT )));
    __guiWarningLevel_JTextField.setText ( String.valueOf (
        Message.getWarningLevel ( Message.STATUS_HISTORY_OUTPUT )));
    __guiStatusLevel_JTextField.setText ( String.valueOf (
        Message.getStatusLevel ( Message.STATUS_HISTORY_OUTPUT )));
    __logDebugLevel_JTextField.setText ( String.valueOf (
        Message.getDebugLevel ( Message.LOG_OUTPUT )));
    __logWarningLevel_JTextField.setText ( String.valueOf (
        Message.getWarningLevel ( Message.LOG_OUTPUT )));
    __logStatusLevel_JTextField.setText ( String.valueOf (
        Message.getStatusLevel ( Message.LOG_OUTPUT )));
}

/**
Set the window visible, refreshing the contents from settings that may have been
applied programatically elsewhere.
@param isVisible If true, set the window to visible and refresh the contents.
If false, set the window to invisible.
*/
public void setVisible ( boolean isVisible )
{
    if ( isVisible ) {
        // Refresh the contents
        refreshContents ();
    }
    // Always center on the main interface since people will think it is popping up new each time
    if ( isVisible ) {
	    JGUIUtil.center(this,__parent);
    }
    super.setVisible(isVisible);
}

public void windowActivated(WindowEvent e)
{
}

/**
If the window is closing, set to visible but do not dispose.
*/
public void windowClosing(WindowEvent e)
{	setVisible(false);
	if ( __menu_JCheckBoxMenuItem != null ) {
		__menu_JCheckBoxMenuItem.setState ( false );
	}
}

public void windowClosed(WindowEvent e)
{
}

public void windowDeactivated(WindowEvent e)
{
}

public void windowDeiconified(WindowEvent e)
{
}

public void windowIconified(WindowEvent e)
{
}

public void windowOpened(WindowEvent e)
{
}

} // End DiagnosticsJFrame class
