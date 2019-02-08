// SimpleJToggleButton - a simple button using a listener

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
// SimpleJToggleButton - a simple button using a listener
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-05-15	J. Thomas Sapienza, RTi	Initial version from SimpleJButton.
// 2003-10-27	JTS, RTi		Overrode paintBorder and removed call
//					to setPaintBorder() in response to 
//					changes made to the class between 
//					1.4.0 and 1.4.2.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import javax.swing.border.Border;

import java.awt.event.ActionListener;

/**
This class is used to simplify construction of a button and linkage to
an action listener.  An example of it is as follows:
<p>

<pre>
// "this" is a GUI component, like a JFrame.
button = new SimpleJToggleButton("My Button", "MyButton" + this);
</pre>
*/
public class SimpleJToggleButton extends JToggleButton {

/**
Whether to paint the border when the button is not selected.
*/
private boolean __paintDeselectedBorder = true;

/**
Construct a button by specifying the label and action listener.
The command use for events is the same as the label.
@param label String label for button.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(String label, ActionListener al, boolean selected) {
	super(label, selected);
	initialize(null, null, null, true, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(String label, String command, ActionListener al,
boolean selected) {
	super(label, selected);
	initialize(command, null, null, true, al);
}

/**
Construct a button by specifying the label, command string, insets, margin,
and action listener.
@param label String label for button.
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed even when the 
button is disable.  If false, it will not.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(String label, String command, Insets insets, 
boolean margin, ActionListener al, boolean selected) {
	super(label, selected);
	initialize(command, null, insets, margin, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(String label, String command, String toolTipText,
ActionListener al, boolean selected) {
	super(label, selected);
	initialize(command, toolTipText, null, true, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed even when the
button is disabled.  If false, it will not.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(String label, String command, String toolTipText,
Insets insets, boolean margin, ActionListener al, boolean selected) {
	super(label, selected);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(ImageIcon icon, String command, ActionListener al,
boolean selected)
{	super(icon, selected);
	initialize(command, null, null, true, al);
}

/**
Construct a button by specifying the icon, command string, insets, margin,
and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed even when the 
button is disabled.  If false, it will not.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(ImageIcon icon, String command, Insets insets, 
boolean margin, ActionListener al, boolean selected) {
	super(icon, selected);
	initialize(command, null, insets, margin, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(ImageIcon icon, String command, String toolTipText,
ActionListener al, boolean selected) {
	super(icon, selected);
	initialize(command, toolTipText, null, true, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed even when the
button is disabled.  If false, it will not.
@param al Action listener.
@param selected whether the button should initially be selected or not
*/
public SimpleJToggleButton(ImageIcon icon, String command, String toolTipText,
Insets insets, boolean margin, ActionListener al, boolean selected) {
	super(icon, selected);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Initialize the button data.
@param command Command string (button label) for events.
@param toolTipText the text to display as the button tool tip
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed even when the 
button is disabled.  If false, it will not.
@param al Action listener.
*/
private void initialize(String command, String toolTipText, Insets insets, 
boolean margin, ActionListener al) {
	addActionListener(al);
	if (command != null) {
		if (command.length() > 0) {
			setActionCommand(command);
		}
	}

	if (toolTipText != null) {
		setToolTipText(toolTipText);
	}

	if (insets != null) {
		setMargin(insets);
	}

	__paintDeselectedBorder = margin;
}

/**
Overrides the default paintBorder() method from JToggleButton in order that
the behavior is what we desire.  Namely, we want to be able to set the button
so that the border only paints when the button is selected.
@param g the Graphics context on which to draw the button.
*/
protected void paintBorder(Graphics g) {	
	if (isBorderPainted()) {
		if (!isSelected() && !__paintDeselectedBorder) {
			return;
		}
		else {
		        Border border = getBorder();
		        if (border != null) {
				border.paintBorder(this, g, 0, 0, getWidth(), 
					getHeight());
        		}
		}
	}
}

}
