//-----------------------------------------------------------------------------
// SimpleJTree_TreeWillExpandListener - Class that allows control over
// whether JTrees can expand or collapse.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-05-01	J. Thomas Sapienza, RTi	* Initial version
//					* Javadoc'd.
// 2004-07-06	JTS, RTi		* Added setTree().
//					* Changed expansion code to notify 
//					  listeners of the node that is 
//					  expanding.
// 2005-04-26	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.List;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;

import javax.swing.tree.ExpandVetoException;

/**
This class is used when it is not desired that the SimpleJTree be able
to be collapsed.  It overrides the default behavior in the SimpleJTree's
oroginal TreeWillExpandListener such that collapsing and expansion of the
tree can be disabled.<p>
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
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__tree = null;
	super.finalize();
}

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
@param collapseAllowed if true, the tree can be collapsed.  If false, it 
can not.
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
Invoked whenever a node in the tree is about to be collapsed (overridden 
from TreeWillExpandListener).  Throws an exception if the tree cannot be
collapsed (this exception doesn't ever show up to the user; it is used by
the JTree to determine if it can collapse or not).
@param event event that happened.
*/
public void treeWillCollapse(TreeExpansionEvent event) 
throws ExpandVetoException {
	if (!__collapseAllowed) {
		throw new ExpandVetoException(event, 
			"Cannot collapse this tree");
	}	
}

/**
Invoked whenever a node in the tree is about to be expanded (overridden 
from TreeWillExpandListener).  Throws an exception if the tree cannot be
collapsed (this exception doesn't ever show up to the user; it is used by
the JTree to determine if it can expand or not).
@param event event that happened.
*/
public void treeWillExpand(TreeExpansionEvent event) 
throws ExpandVetoException {
	if (!__expandAllowed) {
		throw new ExpandVetoException(event, 
			"Cannot expand this tree");
	}		
	List v = __tree.getListeners();
	if (v == null) {
		return;
	}

	SimpleJTree_Node node = (SimpleJTree_Node)
		((event.getPath()).getLastPathComponent());
	
	int size = v.size();
	for (int i = 0; i < size; i++) {
		((SimpleJTree_Listener)v.get(i)).nodeExpanding(node);
	}
}

}
