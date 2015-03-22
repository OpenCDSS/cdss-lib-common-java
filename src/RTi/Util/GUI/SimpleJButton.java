// ----------------------------------------------------------------------------
// SimpleJButton - a simple button using a listener
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 10 Nov 1997	Steven A. Malers, RTi	Initial version based on several
//					examples.
// 2001-11-27	SAM, RTi		Remove import *.
// 2001-12-03	SAM, RTi		Update to use Swing.
// 2003-05-15	J. Thomas Sapienza, RTi	Added a couple new constructors:
//					* one to take an image icon
//					* image icon & insets & margin
//					* image icon & tooltip
//					* image icon & insets & margin & tooltip
//					* text & insets & margin
//					* text & tooltip
//					* text & insets & margin & tooltip
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.event.ActionListener;

/**
This class is used to simplify construction of a button and linkage to
an action listener.  An example of its is as follows:
<p>
<pre>
// "this" is a GUI component, like a JFrame.
button = new SimpleJButton("My Button", "MyButton" + this );
</pre>
*/
public class SimpleJButton 
extends JButton {

/**
Construct a button by specifying the label and action listener.
The command use for events is the same as the label.
@param label String label for button.
@param al Action listener.
*/
public SimpleJButton ( String label, ActionListener al )
{	super (label);
	initialize (null, null, null, true, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param al Action listener.
*/
public SimpleJButton ( String label, String command, ActionListener al )
{	super (label);
	initialize (command, null, null, true, al );
}

/**
Construct a button by specifying the label, command string, insets, margin, and action listener.
@param label String label for button.
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of 
the button from its contents
@param margin if true, the button's margin will be displayed.  If false, it will not.
@param al Action listener.
*/
public SimpleJButton (String label, String command, Insets insets, boolean margin, ActionListener al)
{	super(label);
	initialize(command, null, insets, margin, al);
}

/**
Construct a button by specifying the label, command string, and action listener.
@param label String label for button.
@param command Command string for events.
@param toolTipText the text to display as the button tool tip
@param al Action listener.
*/
public SimpleJButton (String label, String command, String toolTipText, ActionListener al)
{
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
public SimpleJButton (String label, String command, String toolTipText, Insets insets, boolean margin, ActionListener al)
{
	super(label);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Construct a button by specifying the icon, command string, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, ActionListener al )
{	super (icon);
	initialize (command, null, null, true, al );
}

/**
Construct a button by specifying the icon, command string, insets, margin, and action listener.
@param icon the icon to display in the button
@param command Command string for events.
@param insets the insets inside of the JButton that separate the edge of the button from its contents
@param margin if true, the button's margin will be displayed.  If false, it will not.
@param al Action listener.
*/
public SimpleJButton (ImageIcon icon, String command, Insets insets, boolean margin, ActionListener al)
{
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
public SimpleJButton (ImageIcon icon, String command, String toolTipText, ActionListener al)
{
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
public SimpleJButton (ImageIcon icon, String command, String toolTipText, Insets insets, boolean margin, ActionListener al)
{
	super(icon);
	initialize(command, toolTipText, insets, margin, al);
}

/**
Initialize the button data.
@param command Command string (button label)for events.
@param al Action listener.
*/
private void initialize (String command, String toolTipText, Insets insets, boolean margin, ActionListener al)
{
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