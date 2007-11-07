// ----------------------------------------------------------------------------
// XMRGViewerJFrame - a JFrame for displaying XMRG files.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-10-14	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;

/**
A JFrame that contains a panel that can be used to display XMRG file contents.
*/
public class XMRGViewerJFrame
extends JFrame 
implements ActionListener {

/**
Menu strings.
*/
private final String
	__MENU_FILE = "File",
	__MENU_FILE_OPEN = "Open XMRG File ...",
	__MENU_FILE_EXIT = "Exit";

/**
The directory to browse for xmrg files.
*/
private String __xmrgDir = null;

/**
The file that was opened and displayed in the frame.
*/
private String __xmrgFilename = null;

/**
The panel in which the XMRG data is shown.
*/
private XMRGViewerJPanel __xmrgPanel = null;

/**
Constructor.
*/
public XMRGViewerJFrame() {
	super(JGUIUtil.getAppNameForWindows() 
		+ " - XMRG File Viewer - [No file opened]");
	
	setupGUI();
}

/**
@deprecated use XMRGViewJFrame(String, boolean).
*/
public XMRGViewerJFrame(String xmrgDir) {
	this(xmrgDir, false);
}

/**
Constructor.
@param xmrgDir the directory in which xmrg files are being opened.
@param xmrgFilename the file to open and display.
*/
public XMRGViewerJFrame(String xmrgDir, String xmrgFilename) {
	super(JGUIUtil.getAppNameForWindows() 
		+ " - XMRG File Viewer - [No file opened]");
	
	__xmrgDir = xmrgDir;
	JGUIUtil.setLastFileDialogDirectory(__xmrgDir);
	__xmrgFilename = xmrgFilename;

	setupGUI();
}

/**
Constructor.
@param xmrgStr if the 'dir' param is true, this is the directory in which
to browse for XMRG files.  If 'dir' is false, this is the file to open and 
display.
@param dir whether the first parameter is a directory (true) or a filename 
(false).
*/
public XMRGViewerJFrame(String xmrgStr, boolean dir) {
	super(JGUIUtil.getAppNameForWindows() 
		+ " - XMRG File Viewer - [No file opened]");

	if (dir) {
		__xmrgDir = xmrgStr;
		JGUIUtil.setLastFileDialogDirectory(__xmrgDir);
	}
	else {
		__xmrgFilename = xmrgStr;
	}

	setupGUI();
}

/**
Responds to action events.
@param event the event that happened.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(__MENU_FILE_OPEN)) {
		openFile();
	}
	else if (command.equals(__MENU_FILE_EXIT)) {
		setVisible(false);
		dispose();
	}
}

/**
Opens an XMRG file and displays its contents.
*/
private void openFile() {
	JGUIUtil.setWaitCursor(this, true);
	String lastDirectorySelected = 
		JGUIUtil.getLastFileDialogDirectory();
	
	JFileChooser fc = JFileChooserFactory.createJFileChooser(
		lastDirectorySelected);

	fc.setDialogTitle("Create Data Dictionary");
	fc.setAcceptAllFileFilterUsed(true);
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
	JGUIUtil.setWaitCursor(this, true);

	__xmrgPanel.openFile(path);

	setTitle(JGUIUtil.getAppNameForWindows() 
		+ " - XMRG File Viewer - [" + path + "]");

	JGUIUtil.setWaitCursor(this, false);
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	if (JGUIUtil.getIconImage() != null) {
		JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	}
	
	JGUIUtil.setSystemLookAndFeel(true);
	
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu(__MENU_FILE);
	JMenuItem mi = new JMenuItem(__MENU_FILE_OPEN);
	mi.addActionListener(this);
	fileMenu.add(mi);
	fileMenu.addSeparator();

	mi = new JMenuItem(__MENU_FILE_EXIT);
	mi.addActionListener(this);
	fileMenu.add(mi);
	menuBar.add(fileMenu);

	setJMenuBar(menuBar);

	__xmrgPanel = new XMRGViewerJPanel(__xmrgFilename);

	getContentPane().add(new JScrollPane(__xmrgPanel));

	pack();

	setSize(600, 540);

	JGUIUtil.center(this);

	setVisible(true);
}

}
