// SimpleJButton - a simple button using a listener

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.event.ActionListener;

/**
This class is used to simplify construction of a button and linkage to an action listener.
An example of its is as follows:
<p>
<pre>
// "this" is a GUI component, like a JFrame.
button = new SimpleJButton("My Button", "MyButton" + this );
</pre>
*/
@SuppressWarnings("serial")
public class SimpleJButton
extends JButton {

/**
Construct a button by specifying the label and action listener.
The command use for events is the same as the label.
@param label String label for button.
@param al Action listener.
*/
public SimpleJButton ( String label, ActionListener al ) {
	super (label);
	initialize (null, null, null, true, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param al Action listener.
*/
public SimpleJButton ( String label, String command, ActionListener al ) {
	super (label);
	initialize (command, null, null, true, al );
}

/**
Construct a button by specifying the label, command string, insets, margin, and action listener.
@param label String label for button.
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of the button from its contents
@param margin if true, the button's margin will be displayed.  If false, it will not.
@param al Action listener.
*/
public SimpleJButton (String label, String command, Insets insets, boolean margin, ActionListener al) {
	super(label);
	initialize(command, null, insets, margin, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param al Action listener.
*/
public SimpleJButton (String label, String command, String toolTipText, ActionListener al) {
	super(label);
	initialize(command, toolTipText, null, true, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param insets the insets inside of the JButton that separate the edge of the button from its contents
@param margin if true, the button's margin will be displayed.  If false, it will not.
@param al Action listener.
*/
public SimpleJButton (String label, String command, String toolTipText, Insets insets, boolean margin, ActionListener al) {
	super(label);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, ActionListener al ) {
	super (icon);
	initialize (command, null, null, true, al );
}

/**
Construct a button by specifying the icon, command string, insets, margin, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of the button from its contents
@param margin if true, the button's margin will be displayed. If false, it will not.
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, Insets insets, boolean margin, ActionListener al) {
	super(icon);
	initialize(command, null, insets, margin, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, String toolTipText, ActionListener al) {
	super(icon);
	initialize(command, toolTipText, null, true, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param insets the insets inside of the JButton that separate the edge of the button from its contents
@param margin if true, the button's margin will be displayed.  If false, it will not.
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, String toolTipText, Insets insets, boolean margin, ActionListener al) {
	super(icon);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Initialize the button data.
@param command Command string (button label)for events.
@param al Action listener.
*/
private void initialize (String command, String toolTipText, Insets insets, boolean margin, ActionListener al) {
	if ( al != null ) {
		addActionListener (al);
	}
	if ( command != null ) {
		if ( command.length() > 0 ) {
			setActionCommand (command);
		}
	}

	if (toolTipText != null) {
		setToolTipText(toolTipText);
	}

	if (insets != null) {
		setMargin(insets);
	}

	setBorderPainted(margin);
}

}