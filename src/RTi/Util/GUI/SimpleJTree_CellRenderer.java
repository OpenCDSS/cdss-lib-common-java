// SimpleJTree_CellRenderer - class to control rendering of Components and regular JTree text values in a SimpleJTree

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

//-----------------------------------------------------------------------------
// SimpleJTree_CellRenderer - Class to control rendering of both Components
// and regular JTree text values in a SimpleJTree
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-04-30	J. Thomas Sapienza, RTI	Initial version
// 2003-05-01	JTS, RTi		Javadoc'd.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
This is the cell renderer used by the SimpleJTree to allow Components to be
shown and clicked on but at the same time to allow the standard JTree 
functionality whereby text can be shown and edited.
*/
public class SimpleJTree_CellRenderer 
extends DefaultTreeCellRenderer
implements TreeCellRenderer {

/**
Constructor.
*/
public SimpleJTree_CellRenderer() {}

/**
Configures the renderer based on the passed-in components.  The value is set
from messaging the tree with <tt>convertValueToText</tt>, which ultimately
invokes <tt>toString</tt> on <tt>value</tt>.  The foreground color is set
based on the selected and the icon is set based on leaf and expanded.
<p>(Overridden from DefaultTreeCellRenderer)<p>
This figures out what is the node that should be rendered and if it is 
a Component, then the Component is rendered properly so that it shows up 
correctly.  If the thing to be rendered is actually text, then it
renders it with the default JTree functionality.<p>
<b>Note:</b> All the components that might need to be rendered properly 
need to be listed in here.  Those that are currently supported are:<ul>
<li>JButton</li>
<li>JCheckBox</li>
<li>JLabel</li>
<li>JPanel</li>
</ul>
Any others that do not match the above are turned into Strings and placed in
a JLabel.
@param tree the JTree that is asking the editor to edit; this parameter
can be null.
@param value the value of the cell to be edited.
@param selected true if the cell is to be rendererd with selection
highlighting.
@param expanded true if the node is expanded.
@param leaf true if the node is a leaf node.
@param row the row index of the node being edited.
@return the Component for editing.
*/
public Component getTreeCellRendererComponent(JTree tree, Object value,
boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	// This might not be necessary, but it may have caught an error during
	// testing ...
	if (row == -1) {
		return new JLabel("");
	}

	// Get the node that is being rendered
	SimpleJTree_Node temp = (SimpleJTree_Node) value;

	// if the node has a Component (i.e., it doesn't hold just a String),
	// then the Components need rendered properly.
	if (temp.containsComponent()) {	
		Object o = temp.getUserObject();

		if (o == null) {
			return new JLabel("-- NULL --");
		}

		if (o instanceof JCheckBox) {
			JCheckBox temp2=(JCheckBox)o;
			temp2.setBackground(UIManager.getColor(
				"Tree.textBackground"));
			return temp2;
		}
		else if (o instanceof JLabel) {
			JLabel temp2 = (JLabel)o;
			temp2.setBackground(UIManager.getColor(
				"Tree.textBackground"));
			return temp2;
		}
		else if (o instanceof JButton) {
			JButton temp2 = (JButton)o;
			return temp2;
		}
		else if (o instanceof JPanel) {
			JPanel temp2 = (JPanel)o;
			return temp2;
		}
		else if (o instanceof JComponent) {
			JComponent temp2 = (JComponent)o;
			return temp2;
		}
		else {
			return new JLabel(o.toString());
		}
	}
	// Otherwise, render the node in the default JTree functionality.
	else { 
		String superString = temp.getSuperString();
		if (superString != null) {
			if (!superString.equals(temp.getText())) {
				temp.setText(superString);
			}
		}
		super.getTreeCellRendererComponent(tree, temp.getText(), 
			selected, expanded, leaf, row, hasFocus);
		Icon icon = temp.getIcon();			
		if (icon != null) {
			setIcon(icon);
		}
		return this;
	}
}

}
