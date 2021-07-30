// HelpAboutJDialog - provides a Help About JDialog with general features

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

package RTi.Util.GUI;

import java.awt.BorderLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class provides a simple "Help About" JDialog.
The events for the JDialog are handled according to the ResponseJDialog base class.
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
		"Copyright 1997...\n");

		HelpAboutJDialog help_about_gui = new HelpAboutJDialog (this,
			"About " + IO.getProgramName(), help_string );
	}
</pre>
</p>
<p>
The text for the help JDialog can contain newline characters (\n) to cause
line breaks.  Additional formatting options may be added.
</p>
<p>
@see ResponseDialog
</p>
*/
@SuppressWarnings("serial")
public class HelpAboutJDialog 
extends ResponseJDialog 
implements ActionListener, KeyListener, WindowListener {

/**
Button labels.
*/
private String 
	__BUTTON_SYSTEM_DETAILS = "Show Software/System Details",
	__BUTTON_OK = "OK";

/**
GUI buttons.
*/
private SimpleJButton
	__detailsJButton = null,
	__okJButton = null;

/**
Constructor.
@param parent The parent JFrame.
@param title Title for the JDialog.
@param text Text for the JDialog.  Use newline characters to break lines.
@param showSystemDetails if true, show a button that allows the user to display system details, useful
for troubleshooting; if false, the button is only available when debug logging is turned on
*/
public HelpAboutJDialog (JFrame parent, String title, String text, boolean showSystemDetails) {
	super(parent, false); // Not modal because may need to remain visible during troubleshooting in other windows
	initialize(title, text, showSystemDetails);
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
	else if (s.equals(__BUTTON_SYSTEM_DETAILS)) {
		List<String> systemDetails = new ArrayList<>();
		List<String> systemProperties = IOUtil.getSystemProperties();
		List<String> jarManifests = IOUtil.getJarFilesManifests();

		systemDetails.addAll(systemProperties);
		systemDetails.addAll(jarManifests);

		PropList props = new PropList("HelpAboutJDialog");
		props.set("Title=System Information");
		props.set("TotalWidth=800");
		props.set("TotalHeight=500");
		new ReportJDialog(this, systemDetails, props, true);
	}
}

/**
Instantiate the dialog components
@param parent JFrame class instantiating this class.
@param title Dialog title
@param label Label to display in the GUI.
@param showSystemDetails if true, show a button that allows the user to display system details, useful
for troubleshooting; if false, the button is only available when debug logging is turned on
*/
private void initialize(String title, String label, boolean showSystemDetails) {
	addWindowListener(this);

	// Split the text based on the new-line delimiter - internally use \n, not the platform's separator!
	List<String> vec = StringUtil.breakStringList(label, "\n", 0);
	int size = vec.size();
	
    // Main panel
	JPanel main_JPanel = new JPanel();
	
	if (vec != null) {
		Insets insets = new Insets( 1, 5, 1, 5);
		// New approach where alignment can be CENTER (because
		// used by the HelpAboutDialog)...
       	main_JPanel.setLayout(new GridBagLayout());
		if (size > 20) {
			//add message String to a JList that is within a JScrollPane
			JList<String> list = null;
     		list = new JList<String>(new Vector<String>(vec));
			//list.setBackground(Color.LIGHT_GRAY);
			JScrollPane pane = new JScrollPane(list);
			Dimension d = new Dimension(500, 500);
			pane.setPreferredSize(d);
			pane.setMinimumSize(d);
			//pane.setMaximumSize(d);

			//add JScrollPane to JPanel
       		JGUIUtil.addComponent(main_JPanel,
				pane, 0,0,1,1,1,1,insets, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
		}
		else {
			// Add each string as a JLabel...
			for (int i = 0; i < size; i++) {
       			JGUIUtil.addComponent(main_JPanel, new JLabel((String)vec.get(i)),
					0, i, 1, 1, 0, 0, insets, GridBagConstraints.NONE, GridBagConstraints.CENTER);
			}
		}
	}

    getContentPane().add("Center", main_JPanel);

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

	if ( showSystemDetails || Message.isDebugOn ) {
		__detailsJButton = new SimpleJButton(__BUTTON_SYSTEM_DETAILS, this);
        southNorth_JPanel.add(__detailsJButton);
	}

	if (title != null) {
		setTitle(title);
	}

	setResizable(true);
    pack();
    JGUIUtil.center(this);
    super.setVisible(true);
	addKeyListener(this);
}

/**
Responds to key pressed events.  Pressing 'Enter' will activate the OK button.
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