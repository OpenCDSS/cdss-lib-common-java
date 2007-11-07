// ----------------------------------------------------------------------------
// SimpleJMenuItem - a simple menu item to add a listener
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2001-11-03	Steven A. Malers, RTi	Initial version based on SimpleMenuItem.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import javax.swing.JMenuItem;
import java.awt.event.ActionListener;

/**
This class simplifies the addition of menu items by combining the construction
and event handler setup.  An example of use is:
<p>

<pre>
   JMenu mymenu = new JMenu ( "MyMenu" );
   mymenu.add ( new SimpleJMenuItem("Exit","Myapp.Exit", this );
</pre>
<p>

Then implement an ActionListener interface and check for events with a command
name of "Myapp.Exit".
*/
public class SimpleJMenuItem 
extends JMenuItem {

/**
Construct a menu item given a menu label and action listener.  The event
command will be the same as the label.
@param label Menu label.
@param al ActionListener.
*/
public SimpleJMenuItem(String label, ActionListener al) {
	this(label, null, al);
}

/**
Construct a menu item given a menu label, event command, and action listener.
@param label Menu label.
@param command Event command string.
@param al ActionListener.
*/
public SimpleJMenuItem(String label, String command, ActionListener al) {
	super(label);
	addActionListener(al);
	if (command != null) {
		if (command.length() > 0) {
			setActionCommand(command);
		}
	}
}

}
