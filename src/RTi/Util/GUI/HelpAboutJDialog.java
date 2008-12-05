//-----------------------------------------------------------------------------
// HelpAboutJDialog - provides a Help About JDialog with general features
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2002-10-03	J. Thomas Sapienza, RTi	Initial version from HelpAboutJDialog
// 2002-11-03	Steven A. Malers, RTi	Update to extend ResponseJDialog instead
//					of ResponseDialog.  Update the Javadoc
//					to be consistent with Swing usage.
// 2005-11-16	JTS, RTi		Moved the majority of the guts of 
//					ResponseJDialog into this class.  The
//					classes are diverging because the help
//					dialog is being tasked with showing 
//					Jar manifest information when debug is
//					on.  This class still extends 
//					ResponseJDialog in order to be backward
//					compatible with applications that
//					may cast it.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class provides a simple "Help About" JDialog that appears as follows.
<p>

<img src="HelpAboutJDialog.gif"><p>

The events for the JDialog are handled according to the ResponseJDialog base
class.
Construct the JDialog using code as in the following example:
<p>

<pre>
	// Tie to a GUI's action listener and create in the
	// actionPerformed routine...
	else if (action.equals("Help.About")) {
		// Start a modal JDialog...
		String help_string = new String (
		IO.getProgramName() + " (TM) - Internet Edition\n" +
		"Version " + IO.getProgramVersion() + "\n" +
		"Copyright 1997\n" +
		"\n" +
		"Riverside Technology, inc.\n" +
		"2290 East Prospect Road, Suite 1\n" +
		"Fort Collins, CO 80525 U.S.A.\n" +
		"(970) 484-7573\n" +
		"(970) 484-7593 FAX\n" +
		"info@riverside.com\n" +
		"http://www.riverside.com");

		HelpAboutJDialog help_about_gui = new HelpAboutJDialog (this,
			"About " + IO.getProgramName(), help_string );
	}
</pre>
<p>

The text for the help JDialog can contain newline characters (\n) to cause
line breaks.  Additional formatting options may be added.
<p>

At this time, it is best to handle the menus from the parent GUI, but the
attachMainMenu approach is being studied.  Future updates of this JDialog may
support a graphic image and other buttons used in other applications.<br>
REVISIT (SAM)<br>
See last paragraph
@see ResponseDialog
*/
public class HelpAboutJDialog 
extends ResponseJDialog 
implements ActionListener, KeyListener, WindowListener {

/**
Button labels.
*/
private String 
	__BUTTON_JAR = "Show Software/System Details",
	__BUTTON_OK = "OK";

/**
GUI buttons.
*/
private SimpleJButton
	__jarJButton = null,
	__okJButton = null;

/**
Constructor.
@param parent The parent JFrame.
@param title Title for the JDialog.
@param text Text for the JDialog.  Use newline characters to break lines.
*/
public HelpAboutJDialog (JFrame parent, String title, String text) {
	super(parent, true);
	initialize(title, text);
}

/**
Attach this GUI's menu to the calling code.  This approach is being studied.
Not sure yet whether this sophistication is needed for this simple JDialog!
REVISIT (SAM)<br>
See above javadocs.
*/
public void attachMainMenu (JMenu menu) {
	attachMainMenu(menu, "About...");
}

/**
Attach a "Help About..." menu item given a menu and the string label to be
used for the menu label.
*/
public void attachMainMenu (JMenu menu, String menu_label) {
	menu.add(new SimpleJMenuItem(menu_label,"Help.About",this));
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed(ActionEvent event) {
	String s = event.getActionCommand();
	if (s.equals(__BUTTON_OK)) {
		setVisible(false);
		dispose();	
	}
	else if (s.equals(__BUTTON_JAR)) {
		List v1 = IOUtil.getSystemProperties();
		List v2 = IOUtil.getJarFilesManifests();

		List v3 = new Vector();
		for (int i = 0; i < v1.size(); i++) {
			v3.add(v1.get(i));
		}
		for (int i = 0; i < v2.size(); i++) {
			v3.add(v2.get(i));
		}
		PropList props = new PropList("HelpAboutJDialog");
		props.set("Title=System Information");
		new ReportJDialog(this, v3, props, true);
	}
}

/**
Clean up for garbage collection.
@exception Throwable if an error.
*/
protected void finalize()
throws Throwable {
	__okJButton = null;
	super.finalize();
}

/**
Instantiate the dialog components
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
*/
private void initialize(String title, String label) {
	addWindowListener(this);

	// Split the text based on the new-line delimiter (we use \n, not the platform's separator!
	List vec = StringUtil.breakStringList(label, "\n", 0);
	int size = vec.size();
	
        // North Panel
	JPanel north_JPanel = new JPanel();
	
	if (vec != null) {
		Insets insets = new Insets( 1, 5, 1, 5);
		// New approach where alignment can be CENTER (because
		// used by the HelpAboutDialog)...
       		north_JPanel.setLayout(new GridBagLayout());
		if (size > 20) {
			//add message String to a JListthat is within a JScrollPane
			JList list = null;
			if ( vec instanceof Vector ) {
				list = new JList((Vector)vec);
			}
			else {
				list = new JList(new Vector(vec));
			}
			list.setBackground(Color.LIGHT_GRAY);
			JScrollPane pane = new JScrollPane(list);
			Dimension d = new Dimension(400, 200);
			pane.setPreferredSize(d);
			pane.setMinimumSize(d);
			pane.setMaximumSize(d);

			//add JScrollPane to JPanel
       			JGUIUtil.addComponent(north_JPanel,
				pane, 0,0,1,1,0,0,insets,
				GridBagConstraints.NONE, 
				GridBagConstraints.CENTER);
		}
		else {
			// Add each string as a JLabel...
			for (int i = 0; i < size; i++) {
       				JGUIUtil.addComponent(north_JPanel,
					new JLabel((String)vec.get(i)),
					0, i, 1, 1, 0, 0, insets,
					GridBagConstraints.NONE, 
					GridBagConstraints.CENTER);
			}
		}
	}

        getContentPane().add("North", north_JPanel);

	// Now add the buttons...

        // South Panel
	JPanel south_JPanel = new JPanel();
        south_JPanel.setLayout(new BorderLayout());
        getContentPane().add("South", south_JPanel);
        
        // South Panel: North
        JPanel southNorth_JPanel = new JPanel();
        southNorth_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        south_JPanel.add("North", southNorth_JPanel);

       	__okJButton = new SimpleJButton(__BUTTON_OK, this);
	__okJButton.addKeyListener(this);
        southNorth_JPanel.add(__okJButton);

	if (Message.isDebugOn || IOUtil.testing()) {
		__jarJButton = new SimpleJButton(__BUTTON_JAR, this);
        	southNorth_JPanel.add(__jarJButton);
	}

	if (title != null) {
		setTitle(title);
	}

	// Dialogs do not need to be resizable...
	setResizable(false);
        pack();
        JGUIUtil.center(this);
        super.setVisible(true);
	addKeyListener(this);
}

/**
Responds to key pressed events.  Pressing 'Enter' will activate the OK 
button.
@param e the KeyEvent that happened.
*/
public void keyPressed(KeyEvent e) {
	int code = e.getKeyCode();

	if (code == KeyEvent.VK_ENTER) {
		actionPerformed(new ActionEvent(this, 0, __BUTTON_OK));
	}
}

/**
Responds to key released events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyReleased(KeyEvent e) {}

/**
Responds to key typed events; does nothing.
@param e the KeyEvent that happened.
*/
public void keyTyped(KeyEvent e) {}

public void windowClosing(WindowEvent event) {}
public void windowActivated(WindowEvent evt) {}
public void windowClosed(WindowEvent evt) {}	
public void windowDeactivated(WindowEvent evt) {}
public void windowDeiconified(WindowEvent evt) {}
public void windowIconified(WindowEvent evt) {}
public void windowOpened(WindowEvent evt) {}

}
