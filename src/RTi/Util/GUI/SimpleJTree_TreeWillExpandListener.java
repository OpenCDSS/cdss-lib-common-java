// SimpleJTree_TreeWillExpandListener - class that allows control over whether JTrees can expand or collapse

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

import javax.swing.tree.ExpandVetoException;

/**
This class is used when it is not desired that the SimpleJTree be able to be collapsed.
It overrides the default behavior in the SimpleJTree's original TreeWillExpandListener
such that collapsing and expansion of the tree can be disabled.<p>
The default behavior is that expansion and collapsing are both allowed.
*/
public class SimpleJTree_TreeWillExpandListener
implements TreeWillExpandListener {

/**
Determines whether collapsing is allowed in the tree or not.
*/
private boolean __collapseAllowed = true;

/**
Determines whether expanding is allowed in the tree or not.
*/
private boolean __expandAllowed = true;

/**
The SimpleJTree this listener is operating on.
*/
private SimpleJTree __tree = null;

/**
Returns true if the tree can be collapsed, false otherwise.
@return true if the tree can be collapsed, false otherwise.
*/
public boolean isCollapseAllowed() {
	return __collapseAllowed;
}

/**
Returns true if the tree can be expanded, false otherwise.
@return true if the tree can be expanded, false otherwise.
*/
public boolean isExpandAllowed() {
	return __expandAllowed;
}

/**
Sets whether the tree can be collapsed or not.
@param collapseAllowed if true, the tree can be collapsed.  If false, it can not.
*/
public void setCollapseAllowed(boolean collapseAllowed) {
	__collapseAllowed = collapseAllowed;
}

/**
Sets whether the tree can be expanded or not.
@param expandAllowed if true, the tree can be expanded.  If false, it can not.
*/
public void setExpandAllowed(boolean expandAllowed) {
	__expandAllowed = expandAllowed;
}

/**
Sets the tree this listener is operating on.
@param tree the SimpleJTree this node is listening for.
*/
public void setTree(SimpleJTree tree) {
	__tree = tree;
}

/**
Invoked whenever a node in the tree is about to be collapsed (overridden from TreeWillExpandListener).
Throws an exception if the tree cannot be collapsed (this exception doesn't ever show up to the user;
it is used by the JTree to determine if it can collapse or not).
@param event event that happened.
*/
public void treeWillCollapse(TreeExpansionEvent event)
throws ExpandVetoException {
	if (!__collapseAllowed) {
		throw new ExpandVetoException(event, "Cannot collapse this tree");
	}
}

/**
Invoked whenever a node in the tree is about to be expanded (overridden from TreeWillExpandListener).
Throws an exception if the tree cannot be collapsed (this exception doesn't ever show up to the user;
it is used by the JTree to determine if it can expand or not).
@param event event that happened.
*/
public void treeWillExpand(TreeExpansionEvent event)
throws ExpandVetoException {
	if (!__expandAllowed) {
		throw new ExpandVetoException(event, "Cannot expand this tree.");
	}
	List<SimpleJTree_Listener> v = __tree.getListeners();
	if (v == null) {
		return;
	}

	SimpleJTree_Node node = (SimpleJTree_Node)((event.getPath()).getLastPathComponent());

	int size = v.size();
	for (int i = 0; i < size; i++) {
		v.get(i).nodeExpanding(node);
	}
}

}