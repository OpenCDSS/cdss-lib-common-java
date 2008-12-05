//-----------------------------------------------------------------------------
// SimpleJTree - Class to simplify use of JTrees
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-04-30	J. Thomas Sapienza, RTI	Initial version
// 2003-05-01	JTS, RTi		* Initial version complete
//					* Javadoc'd.
// 2003-05-13	JTS, RTi		* Added suport for getting and setting
//					  the node data.
//					* Added code to find the node that has
//					  a certain datum
// 2003-05-19	JTS, RTi		* Added code to get a Vector of the
//					  nodes selected in the tree.
//					* Added code to easily get the count of
//					  a tree node's children
// 2003-05-27	JTS, RTi		* Changed showIcons and hideIcons so
//					  that bit masks can be used to specify
//					  the specific icons that should be 
//					  shown or hidden
//					* Added getChildrenVector and 
//					  getChildrenArray
// 2003-06-12	JTS, RTi		Added the __fastAdd code.
// 2003-07-28	JTS, RTi		Tree now override's super.getNextMatch()
//					to avoid some exceptions.
// 2003-07-31	JTS, RTi		Added get*Icon() methods.
// 2003-09-04	JTS, RTi		* Corrected null pointer error caused
//					  when getSelectionPaths() is called
//					  and no nodes are selected.
//					* getSelectedNode() now returns null if
//					  no nodes are selected.
// 2003-10-01	JTS, RTi		* Added removeChildren() methods.
//					* Changed all the instances of 
//					  'getChildCount() >= 0' to 
//					  'getChildCount() > 0'.
// 2003-10-24	JTS, RTi		Added removeAllNodes().
// 2003-12-22	JTS, RTi		* Scrolled-to nodes are now 
//					  automatically selected, unless the 
//					  programmer chooses that they not be.
//					* Nodes can be programmatically
//					  selected now.
// 2004-01-06	JTS, RTi		* Removed some long-deprecated methods.
//					* Added debugging info to many methods.
// 2004-01-27	JTS, RTi		* Added refresh(boolean) method.
//					* Added setFastAdd(boolean, boolean)
//					  method.
// 2004-07-06	JTS, RTi		* SimpleJTree_TreeWillExpandListener now
//					  calls setTree().
//					* Added addSimpleJTreeListener().
// 2004-08-18	JTS, RTi		* Added the getAllChildren*() methods.
//					* Added the selectNode() methods that
//					  allow previous selections to remain.
// 2006-05-16	JTS, RTi		Made findTopLevelNode() public.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;

import javax.swing.plaf.basic.BasicTreeUI;

import javax.swing.text.Position;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class is a convenience wrapper around the standard JTree implementation.
Together with:<ul>
<li>SimpleJTree_CellEditor</li>
<li>SimpleJTree_CellRenderer</li>
<li>SimpleJTree_Node</li>
<li>SimpleJTree_TreeWillExpandListener</li>
it provides an easier interface to working with JTrees, both in a GUI and
programmatically.</ul><p>
Example of setting up a tree with some nodes:<p>
<code>
	SimpleJTree tree = new SimpleJTree();
	tree.addMouseListener(this);
	tree.addTreeSelectionListener(this);

	getContentPane().add(new JScrollPane(tree));
	// no lines connecting nodes to other nodes
	tree.setLineStyle(SimpleJTree.LINE_NONE);	

	// turn off some of the icons, but not all
	tree.setClosedIcon(null);
	tree.setOpenIcon(null);
	tree.setLeafIcon(null);

	// create some nodes
	SimpleJTree_Node[] nodes = new SimpleJTree_Node[10];
	for (int i = 0; i < 10; i++) {
		nodes[i] = new SimpleJTree_Node(("node " + i), 
			"node " + i);
	}

	// add a button node
	JButton testThis = new JButton("test");
	testThis.addActionListener(this);
	tree.addNode(new SimpleJTree_Node(testThis, "Test This"));

	// add a text node that has no icon
	tree.addNode(new SimpleJTree_Node("Text", null, "No Icon"));

	// add some of the nodes from the array above
	tree.addNode(nodes[0]);
	tree.addNode(nodes[1], nodes[0]);
	tree.addNode(nodes[2], nodes[0]);
	for (int i = 0; i < 10; i++) {	
		tree.addNode(new SimpleJTree_Node("Temp " + i, null, ""),
			nodes[0]);
	}
	pack();
	tree.setLineStyle(SimpleJTree.LINE_NONE);
	tree.setShowsRootHandles(true);
	tree.collapseAllNodes();	
</code>
*/
public class SimpleJTree 
extends JTree {

/**
The class name.
*/
public final static String CLASS = "SimpleJTree";

/**
The level at which most debug messages are printed.
*/
public final static int dl = 10;

/**
The default closed icon is a closed file folder.
*/
public static final int CLOSED_ICON = 1;

/**
The default collapsed icon is a plus sign ('+') in a box.
*/
public static final int COLLAPSED_ICON = 2;

/**
The default expanded icon is a minus ('-') in a box.
*/
public static final int EXPANDED_ICON = 4;

/**
The default leaf icon in Windows and Motif is a dot; in Metal, it is a 
paper-like icon.
*/
public static final int LEAF_ICON = 8;

/**
The default open icon is an open file folder.
*/
public static final int OPEN_ICON = 16;

/**
JTree line style where there are no lines along the side connecting the 
nodes.  This can be used, for instance, to emulate ESRI's Table of Contents.
*/
public static final int LINE_NONE = 0;
/**
JTree line style where each node and its children are connected by a series
of lines.  This is the default JTree style.  It is called the "angled" style
simply because that is the name of the style as defined by java.
*/
public static final int LINE_ANGLED = 1;
/**
JTree line style where the root nodes are separated by horizontal lines.  This
is only supported by the metal look and feel and is used to separate the 
children of separate nodes.
*/
public static final int LINE_HORIZONTAL = 2;

/**
Used to refer internally to the windows-type look and feel
*/
private static final int __WINDOWS = 0;

/**
Used to refer internally to the motif-type look and feel.
*/
private static final int __MOTIF = 1;

/**
Used to refer internally to the metal-type look and feel.
*/
private static final int __METAL = 2;

/**
Whether nodes should be added with the fast add method, which doesn't do any
tree error checking.
*/
private boolean __fastAdd = false;

/**
The Icon used by the JTree to represent a closed node.
If a call is made to <tt>showIcons(false)</tt>, the previous value of the
closed icon stored in the JTree is stored here.  If a call is made to
<tt>enableIcons()</tt> and this value is not null, the Icon stored here
is set as the JTree's closed icon.<p>.  The default closed icon is a closed
file folder.
*/
private Icon __closedIcon = null;

/**
The Icon used by the JTree's UI Manager to represent a collapsed node.
If a call is made to <tt>showIcons(false)</tt>, the previous value of the
collapsed icon stored in the JTree is stored here.  If a call is made to
<tt>enableIcons()</tt> and this value is not null, the Icon stored here
is set as the JTree's collapsed icon.  The default collapsed icon is a 
plus sign ('+') in a box.
*/
private Icon __collapsedIcon = null;

/**
The Icon used by the JTree's UI Manager to represent an expanded node.
If a call is made to <tt>showIcons(false)</tt>, the previous value of the
expanded icon stored in the JTree is stored here.  If a call is made to
<tt>enableIcons()</tt> and this value is not null, the Icon stored here
is set as the JTree's expanded icon.  The default expanded icon is a minus
('-') in a box.
*/
private Icon __expandedIcon = null;

/**
The Icon used by the JTree to represent a leaf node.
If a call is made to <tt>showIcons(false)</tt>, the previous value of the
leaf icon stored in the JTree is stored here.  If a call is made to
<tt>enableIcons()</tt> and this value is not null, the Icon stored here
is set as the JTree's leaf icon.
The default leaf icon in Windows and Motif is a dot.  In Metal, it is a 
paper-like icon.
*/
private Icon __leafIcon = null;

/**
The Icon used by the JTree to represent an open node.
If a call is made to <tt>showIcons(false)</tt>, the previous value of the
open icon stored in the JTree is stored here.  If a call is made to
<tt>enableIcons()</tt> and this value is not null, the Icon stored here
is set as the JTree's open icon.  The default open icon is an open 
file folder.
*/
private Icon __openIcon = null;

/**
The look and feel that the tree is operating with.  By default this is metal
because that is the look and feel when a java app starts unless a call is made
to change the UIManager.  The UI should be set to the desired type (e.g., 
changing the UI to be a Windows UI) before the tree is initialized or else
the tree will be initialized with the UI that is in effect, probably METAL.
*/
private int __treeType = __METAL;

/**
The Tree Expansion Listener that is used to determine the rules for 
collapsing or expanding the tree.
*/
private SimpleJTree_TreeWillExpandListener __collapseExpandListener = null;

/**
List of SimpleJTree_Listeners that will listen for tree events.
*/
private List __listeners = null;

/**
Constructor.
This creates an empty tree containing only an invisible root node.
*/
public SimpleJTree() {
	SimpleJTree_Node root = new SimpleJTree_Node("JTree.rootNode");
	DefaultTreeModel model = new DefaultTreeModel(root);
	setModel(model);
	initialize();
}

/**
Constructor.
This creates a tree with an invisible root node that is programmatically 
editable and which has the default tree node selection model and populates 
it with the values stored in the provided Vector as follows:<ul>
<li>
If the Vector contains Strings, each String is placed in a new 
SimpleJTree_Node and placed in the tree.  This method of operation is
similar to how JTree's are most-often used (i.e., to show a list of files
in a directory tree, with each String being the name of a file).
</li>
<li>
If the Vector contains Vectors ... <ul>
<li>
... and the Vectors within the main Vector have <b>2</b> values in them,
the first value is cast as a <b>Component</b> and the second is cast
as a <b>String</b> and passed to the <tt>SimpleJTree_Node(Component, String)
</tt> constructor.
</li>
<li>
... and the Vectors within the main Vector have <b>3</b> values in them,
the first value is cast as a <b>String</b>, the second as an <b>Icon</b> and
the third as a <b>String</b> and passed to the 
<tt>SimpleJTree_Node(String, Icon, String)</tt> constructor.
</li>
</li>
</ul>
<p>
<b>Note:</b> the tree is assigned a root SimpleJTree_node with the name
"JTree.rootNode".  This is the TRUE root node of the tree, and it cannot be 
overridden or replaced because it is used internally to speed up some 
operations.  But as far as programmers need be concerned, it doesn't actually
exist and it will never be visible.  They can work with the tree as normal, 
but they cannot have total control over the root node.  They can use it to 
store date by calling getRoot() and working with the node, but renaming the
node or assigning a new node to be the root node could cause problems with the tree's operation.
@param values a Vector of values with which to populate the root of the tree.  See above for notes.
@throws Exception if an error occurs inserting values in the tree
*/
public SimpleJTree(List values) 
throws Exception {
	SimpleJTree_Node root = new SimpleJTree_Node("JTree.rootNode");
	DefaultTreeModel model = new DefaultTreeModel(root);
	setModel(model);
	initialize();

	// avoid null-pointer errors
	if (values == null) {
		// this will create an empty model, like if the 
		// other constructor (with no parameters) is called
		return;
	}

	int size = values.size();
	for (int i = 0; i < size; i++) {
		Object o = values.get(i);

		SimpleJTree_Node node = null;
		// A Vector of Strings is placed in the Tree in the
		// normal JTree fashion.
		if (o instanceof String) {
			String text = (String)o;
			node = new SimpleJTree_Node(text, null);
		}
		// Vectors of Vectors are assumed to be either 
		// Vectors of [0 - Component, 1 - String] or 
		// [0 - String, 1 - Icon, 2 - String]
		else if (o instanceof List) {
			List v = (List)o;

			if (v.size() == 2) {
				Component c = (Component)v.get(0);
				String name = (String)v.get(1);
				node = new SimpleJTree_Node(c, name);
			}
			else if (v.size() == 3) {
				String text = (String)v.get(0);
				Icon icon = (Icon)v.get(1);
				String name = (String)v.get(2);
				node = new SimpleJTree_Node(text, icon, name);
			}
		}
		// the node would be null if something other than a String 
		// or a 2- or 3-element Vector was in the Vector.
		if (node != null) {
			addNode(node);
		}
	}
}

/**
Adds a listener to the Vector of listeners.
@param listener the listener to add.
*/
public void addSimpleJTreeListener(SimpleJTree_Listener listener) {
	if (__listeners == null) {
		__listeners = new Vector();
	}
	__listeners.add(listener);
}

/**
Adds a node to the tree under the node with the given name (as found by 
String.equalsIgnoreCase), after all the other nodes already under the node 
with the given name (as found by String.equalsIgnoreCase).
@param node the node to add to the tree
@param parentName the name of the node under which to add the node.  If it 
doesn't exist in the tree, an exception will be thrown.
@throws Exception if the named parent node can not be found.
*/
public void addNode(SimpleJTree_Node node, String parentName) 
throws Exception {
	addNode(node, parentName, -1);
}

/**
Adds a node to the tree in the specified position under the node with 
the given name.
@param node the node to add to the tree
@param parentName the name of the node under which to add the node. If it 
doesn't exist in the tree, an exception will be thrown.
@param pos the position under the parent node at which to add the node. 
If -1, the node will be added after all the other nodes already under the
named node.  If &lt; -1 or &gt; the number of nodes under the parent 
node, the node will be added after all the other nodes already under the 
parent node.
@throws Exception if the named parent node can not be found.
*/
public void addNode(SimpleJTree_Node node, String parentName, int pos) 
throws Exception {
	addNode(node, findNodeByName(parentName), pos);
}

/**
Adds a node to the tree under the root, after all the other nodes already 
under the root node.
@param node the node to add to the tree
@throws Exception if an error occurs
*/
public void addNode(SimpleJTree_Node node) 
throws Exception {
	addNode(node, -1);
}

/**
Adds a node to the tree under the root, in the position specified.
@param node the node to add to the tree
@param pos the position under the root node at which to add the node.
If -1, the node will be added after all the other nodes already under the 
root node.  If &lt; -1 or &gt; the number of nodes under the root 
node, the node will be added after all the other nodes already under the 
root node.
@throws Exception if an error occurs
*/
public void addNode(SimpleJTree_Node node, int pos) 
throws Exception {
	addNode(node, getRoot(), pos);
}

/**
Adds a node to the tree under the parent node, after all the
other nodes already under the parent node
@param node the node to add to the tree
@param parent the parent node under which to add the node
@throws Exception if the parent node can not be found in the tre
*/
public void addNode(SimpleJTree_Node node, SimpleJTree_Node parent) 
throws Exception {
	addNode(node, parent, -1);
}

/**
Adds a node to the tree under the parent node, after all the
other nodes already under the parent node
@param node the node to add to the tree
@param parent the parent node under which to add the node.  If it doesn't
exist, the node will be added under the root of the tree.
@param pos the position under the parent node at which to insert the 
node.  If -1, the node will be added after all the other nodes already under
the parent node.  If &lt; -1 or &gt; the number of nodes under the parent 
node, the node will be added after all the other nodes already under the 
parent node.
@throws Exception if the parent node cannot be found in the tree.
*/
public void addNode(SimpleJTree_Node node, SimpleJTree_Node parent, int pos) 
throws Exception {
	DefaultTreeModel model = (DefaultTreeModel)getModel();
	SimpleJTree_Node temp = null;

	// Check to see if the specified parent node exists in the tree.  If
	// it doesn't, throw an exception
	if (!__fastAdd && !hasNode(parent)) {
		throw new Exception ("Unable to find parent node: '"
			+ parent.getName() + "' in tree.");
	}
	else {
		temp = parent;
	}

	// make sure the specified position is valid.  If the position is -1
	// the node will be added after all the other nodes already under 
	// the parent node.  If the position is less than -1 or greater than
	// the number of nodes under the parent, it will also be added
	// after all the other nodes under the parent.
	int position = pos;
	int childCount = temp.getChildCount();
	if (position > childCount) {
		position = childCount;
	}
	else if (position == -1) {
		position = childCount;
	}
	else if (position < -1) {
		position = childCount;
	}

	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".addNode", 
			"Node to be added at position: " + position);
	}
	
	if (!__fastAdd) {
		// keep track of which nodes were visible prior 
		// to adding the new node
		markVisibleNodes();
	}

	// insert the node and notify the model that the structure of the 
	// model has changed
	model.insertNodeInto(node, temp, position);

	if (!__fastAdd) {
		model.nodeStructureChanged(parent);

		// make sure that all the nodes visible prior to adding 
		// this node are visible again
		resetVisibleNodes();
	
		treeDidChange();
	
		scrollPathToVisible(new TreePath(node));
	}
}

/**
Collapses all nodes so that only the top-level ones are visible and marks 
all nodes as collapsed.  Will not work if <tt>setCollapse()</tt> 
has been called and collapsing has been turned off.
@throws Exception if unable to collapse nodes because collapsing has been
turned off.
*/
public void collapseAllNodes()
throws Exception {
	if (!isCollapsible()) {
		throw new Exception ("Cannot collapse nodes; table has "
			+ "set un-collapsible");
	}
	
	SimpleJTree_Node node = getRoot();
	
	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".collapseAllNodes",
			"Collapsing from root node (" + node.toString() + ")");
	}

	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			collapseAllNodes(n);
		}
	}	
}

/**
Private function used by <tt>collapseAllNodes()</tt> to recursively collapse
all the tree's nodes.  This goes out to the very end leaves of the tree 
and collapses all the nodes on its way back up the tree.
@param node the node to recurse through the children of, and then collapse.
*/
private void collapseAllNodes(SimpleJTree_Node node) {
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			collapseAllNodes(n);
		}
	}		

	// mark the node itself as visible, too, so that calls to
	// resetVisibleNodes() work correctly
	node.markVisible(false);

	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".collapseAllNodes",
			"  Collapsing node: " + node.toString());
	}

	// close the node
	TreePath tp = getTreePath(node);
	collapsePath(tp);	
}

/**
Collapses a node with the given name (as found by String.equalsIgnoreCase).
@param name name of the node to collapse
@throws Exception if the named node cannot be found, or if the tree has 
been set un-collapsible
*/
public void collapseNode(String name)
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Node '" + name + "' not found");
	}
	collapseNode(node);
}

/**
Collapses the given node.
@param node the node to collapse.
@throws Exception if the tree has been set un-collapsible.
*/
public void collapseNode(SimpleJTree_Node node)
throws Exception {
	if (!isCollapsible()) {
		throw new Exception ("Cannot collapse nodes; table has "
			+ "set un-collapsible");
	}
	
	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".collapseNode",
			"Collapsing node: " + node.toString());
	}
	
	node.markVisible(false);
	TreePath tp = getTreePath(node);
	collapsePath(tp);	
}

/**
Dumps out the tree on the standard output.  Used for debugging.
*/
public void dumpTree() {
	SimpleJTree_Node root = getRoot();
	System.out.println("'" + root.getName() + "'");	
	if (root.getChildCount() > 0) {
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			dumpTree(1, n);
		}
	}	
}

/**
Private utility method used by dumpTree().
*/
public void dumpTree(int indent, SimpleJTree_Node node) {
	String s = "";
	for (int i = 0; i < indent; i++) {
		s += "\t";
	}
	System.out.println(s + "'" + node.getName() + "'");
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			dumpTree((indent + 1), n);
		}
	}		
}

/**
Expands all nodes in the tree.
*/
public void expandAllNodes() {
	SimpleJTree_Node node = getRoot();

	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".expandAllNodes",
			"Expanding from root node (" + node.toString() + ")");
	}
	
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			expandAllNodes(n);
		}
	}	
}

/**
Private utility method used by <tt>expandAllNodes()</tt> to recursively 
expand all the nodes in the tree.
*/
private void expandAllNodes(SimpleJTree_Node node) {
	node.markVisible(true);
	TreePath tp = getTreePath(node);
	makeVisible(tp);

	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".expandAllNodes",
			"  Expanding node:  " + node.toString());
	}

	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			expandAllNodes(n);
		}
	}		
}

/**
Expands the node with the given name  (as found by String.equalsIgnoreCase).
@param name the name of the node to expand.
@throws Exception if the named node cannot be found in the tree.
*/
public void expandNode(String name) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Node '" + name + "' not found");
	}
	expandNode(node);
}

/**
Expands the specified node.
@param node the node to expand.
*/
public void expandNode(SimpleJTree_Node node) {
	node.markVisible(true);
	TreePath tp = getTreePath(node);
	makeVisible(tp);

	if (Message.isDebugOn) {
		Message.printStatus(dl, CLASS + ".expandNode",
			" Expanding node: " + node.toString());
	}	
}

/**
Locates a node in the tree that has the same name, matched with use of
<tt>String.equalsIgnoreCase(String)</tt>.
@param name the name of the node to find.
@return the node that has the given name, or null if no matching nodes could
be found.
*/
public SimpleJTree_Node findNodeByName(String name) {
	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".findNodeByName",
			"Searching for node named '" + name + "'");
	}

	SimpleJTree_Node root = getRoot();
	return findNodeByName(root, name);
}

/**
Private utility method used by <tt>findNodeByName()</tt> to recursively 
search through the tree for the node with the given name  (as found by 
String.equalsIgnoreCase).
@param node the node from which to start searching.
@param name the name of the node to look for.
@return the node that has the given name, or null if no matching nodes could
be found.
*/
private SimpleJTree_Node findNodeByName(SimpleJTree_Node node,String name) {
	if (node.getName().equalsIgnoreCase(name)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".findNodeByName",
				"  Node matched: '" + node.getName());
		}	
	//if (node.getName().startsWith(name)) {
		return node;
	}
	else {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".findNodeByName",
				"  Not a match: '" + node.getName() + "'");
		}
	}	

	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			SimpleJTree_Node o = findNodeByName(n, name);
			if (o != null) {
				return o;
			}
		}
	}
	return null;
}

/**
Finds the node that contains the given data.
@param data the data that the node to find has.
@return the node that contains the given data.
*/
public SimpleJTree_Node findNodeWithData(Object data) {
	SimpleJTree_Node root = getRoot();
	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".findNodeWithData",
			"Looking for node with data: " + data.toString());
	}
	return findNodeWithData(root, data);
}

/**
Utility method used by findNodeWithData.
@param node the node at which to start going down the tree.
@param data the data in the node to find.
@return the node that contains the given data.
*/
private SimpleJTree_Node findNodeWithData(SimpleJTree_Node node, Object data) {
	if (node.getData() == data) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".findNodeWithData",
				"Node '" + node.getName() + "' has matching "
				+ "data.");
		}
		return node;
	}
	else {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".findNodeWithData",
				"  No matching data in '" + node.getName() 
				+ "'");
		}
	}

	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			SimpleJTree_Node o = findNodeWithData(n, data);
			if (o != null) {
				return o;
			}
		}
	}
	return null;
}

/**
Finds a top-level node with the given name.  This only searches the nodes
immediately beneath the (hidden) root node.
@param name the name of the node for which to search.  Cannot be null.
@return the first SimpleJTree_Node with the same name, or null if none could be 
found.
*/
public SimpleJTree_Node findTopLevelNode(String name) {
	SimpleJTree_Node root = getRoot();
	if (root.getName().equals(name)) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".findTopLevelNode",
				"Root node is the matching node.");
		}
		return root;
	}
	
	SimpleJTree_Node n = null;
	if (root.getChildCount() > 0) {
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			n = (SimpleJTree_Node)e.nextElement();
			if (n.getName().equals(name)) {
				return n;
			}
		}
	}
	return null;
}

/**
Cleans up container variables.
*/
public void finalize() 
throws Throwable {
	__closedIcon = null;
	__collapsedIcon = null;
	__expandedIcon = null;
	__leafIcon = null;
	__openIcon = null;
	__collapseExpandListener = null;
	__listeners = null;
}

/**
Returns an array of all the children nodes of the given node, and all the 
children of their children, so on and so on.
@param name the name of the node to return the children of.
@return an array of all the children nodes of the given node.
*/
public Object[] getAllChildrenArray(String name) {
	return getAllChildrenArray(findNodeByName(name));
}

/**
Returns an array of all the children nodes of the given node, and all the 
children of their children, so on and so on.
@param node the node to get the children of
@return an array of all the children nodes of the given node.
*/
public Object[] getAllChildrenArray(SimpleJTree_Node node) {
	List v = getAllChildrenList(node);
	int size = v.size();
	Object[] array = new Object[size];
	for (int i = 0; i < size; i++) {
		array[i] = v.get(i);
	}
	return array;
}

/**
Returns a list of all the children nodes of the given node, and all the 
children of their children, so on and so on.
@param name the name of the node to return the children of.
@return a list of all the children nodes of the given node.
*/
public List getAllChildrenList(String name) {
	return getAllChildrenList(findNodeByName(name));
}

/**
Returns a list of all the children nodes of the given node, and all the 
children of their children, so on and so on.
@param node the node to get the children of
@return a list of all the children nodes of the given node, guaranteed to be non-null.
*/
public List getAllChildrenList(SimpleJTree_Node node) {
	Enumeration e = getChildren(node);
	if (e == null) {
		return new Vector();
	}

	int size = 0;
	SimpleJTree_Node tempNode = null;
	List v = new Vector();
	List temp = null;
	while (e.hasMoreElements()) {
		tempNode = (SimpleJTree_Node)e.nextElement();
		v.add(tempNode);
		temp = getAllChildrenList(tempNode);
		size = temp.size();
		for (int i = 0; i < size; i++) {
			v.add(temp.get(i));
		}
	}
	return v;
}

/**
Returns an enumeration of all the children nodes of the given node.
@param name the name of the node to return the children of.
@return an enumeration of all the children nodes of the given node.
*/
public Enumeration getChildren(String name) {
	return getChildren(findNodeByName(name));
}

/**
Returns an enumeration of all the children nodes of the given node.
@param node the node to get the children of
@return an enumeration of all the children nodes of the given node.
*/
public Enumeration getChildren(SimpleJTree_Node node) {
	if (node == null) {
		return null;
	}
	return node.children();
}

/**
Returns an array of all the children nodes of the given node.
@param name the name of the node to return the children of.
@return an array of all the children nodes of the given node.
*/
public Object[] getChildrenArray(String name) {
	return getChildrenArray(findNodeByName(name));
}

/**
Returns an array of all the children nodes of the given node.
@param node the node to get the children of
@return an array of all the children nodes of the given node.
*/
public Object[] getChildrenArray(SimpleJTree_Node node) {
	List v = getChildrenList(node);
	int size = v.size();
	Object[] array = new Object[size];
	for (int i = 0; i < size; i++) {
		array[i] = v.get(i);
	}
	return array;
}

/**
Returns a list of all the children nodes of the given node.
@param name the name of the node to return the children of.
@return a list of all the children nodes of the given node.
*/
public List getChildrenList(String name) {
	return getChildrenList(findNodeByName(name));
}

/**
Returns a list of all the children nodes of the given node.
@param node the node to get the children of
@return a list of all the children nodes of the given node.
*/
public List getChildrenList(SimpleJTree_Node node) {
	Enumeration e = getChildren(node);
	List v = new Vector();
	while (e.hasMoreElements()) {
		v.add(e.nextElement());
	}
	return v;
}

/**
Returns a count of the number of children of the named node.
@return a count of the number of children of the named node.
*/
public int getChildCount(String name) 
throws Exception {
	return getChildCount(findNodeByName(name));
}

/**
Returns a count of the number of children of the specified node.
@return a count of the number of children of the specified node.
*/
public int getChildCount(SimpleJTree_Node node) {
	return node.getChildCount();
}

/**
Gets the Icon being used as the closed icon.  This is the icon that appears 
in the label next to text in a node that has children (e.g., a closed folder
to represent an un-opened subdirectory).
@return the Icon being used as the closed icon.
*/
public Icon getClosedIcon() {
	return __closedIcon;
}

/**
Gets the icon being used as the collapsed icon.  This is used when a branch of a
tree is closed (e.g., a box with a plus in it representing a branch that 
can be expanded).
@return the Icon being used as the collapsed icon.
*/
public Icon getCollapsedIcon() {
	return __collapsedIcon;
}

/**
Gets the icon being used as the expanded icon.  This is the icon used when a 
branch of a tree has been opened (e.g., a box with a minux sign in it 
representing a branch that can be closed).
@return the Icon being used as the expanded icon.
*/
public Icon getExpandedIcon() {
	return __expandedIcon;
}

/**
Returns whether the tree is speeding up the addNode() process, at the expense 
of some safety checks.  See setFastAdd().
@return whether the tree is speeding up the addNode() process.
*/
public boolean getFastAdd() {
	return __fastAdd;
}

/**
Gets the Icon being used as the leaf icon.  This is the icon that appears next 
to labels on tree leaves.
@return the Icon being used as the leaf icon.
*/
public Icon getLeafIcon() {
	return __leafIcon;
}

/**
Returns the Vector of listeners.
@return the Vector of listeners.
*/
protected List getListeners() {	
	return __listeners;
}

/**
Overrides JTree's getNextMatch(); returns the TreePath to the next tree 
element that begins with a prefix.  Overrides getNextMatch() to avoid throwing
exceptions when the tree captures keypresses and is empty.  
@param prefix the string to test for a match
@param startingRow the row for starting the search
@param bias the search direction, either Position.Bias.Forward or 
Position.Bias.Backward.
*/
public TreePath getNextMatch(String prefix, int startingRow, 
Position.Bias bias) {
	if (startingRow >= getRowCount()) {
		return null;
	}
	return super.getNextMatch(prefix, startingRow, bias);
}

/**
Returns the node with the given name  (as found by String.equalsIgnoreCase).
@param name the name of the node to return
@return the node with the matching name, or null if no matching node could
be found.
*/
public SimpleJTree_Node getNode(String name) {
	return findNodeByName(name);
}

/**
Returns the data from the node with the given name  (as found by 
String.equalsIgnoreCase).
@param name the name of the node for which to return its data.
@return the data from the node with the given name, or null if the node could
not be found.
*/
public Object getNodeData(String name) {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		return null;
	}
	return node.getData();
}

/**
Gets the Icon being used as the open icon.  This is the icon that appears next
to branches that have been expanded (e.g., a folder representing a directory
branch that has been opened).
@return the Icon being used as the open icon.
*/
public Icon getOpenIcon() {
	return __openIcon;
}

/**
Returns the parent of the given node.
@param node the node for which to return the parent.
@return the parent of the node.
*/
public SimpleJTree_Node getParentOfNode(SimpleJTree_Node node) {
	return (SimpleJTree_Node)node.getParent();
}

/**
Returns the root of the tree.
@return the root of the tree.
*/
public SimpleJTree_Node getRoot() {
	return (SimpleJTree_Node)(getModel().getRoot());
}

/**
Returns the first node selected in the tree or null if nothing is selected.
@return the first node selected in the tree or null if nothing is selected.
*/
public SimpleJTree_Node getSelectedNode() {
	List v = getSelectedNodes();
	if (v.size() == 0) {
		return null;
	}
	return ((SimpleJTree_Node)v.get(0));
}

/**
Returns a Vector of all the nodes selected in the tree, or an empty Vector if none are selected.
@return a Vector of all the nodes selected in the tree, or an empty Vector if none are selected.
*/
public List getSelectedNodes() {
	TreePath[] tps = getSelectionPaths();

	List v = new Vector();
	if (tps == null) {
		return v;
	}
	for (int i = 0; i < tps.length; i++) {
		v.add(tps[i].getLastPathComponent());
	}

	return v;
}

/**
Returns a TreePath value for the specified node.
@param node the node for which to return a TreePath.
@return a TreePath for the specified node.
*/
public TreePath getTreePath(SimpleJTree_Node node) {
	List list = new ArrayList();

	// Add all nodes to list
	while (node != null) {
		list.add(node);
		node = (SimpleJTree_Node)node.getParent();
	}
	Collections.reverse(list);
    
	// Convert array of nodes to TreePath
	return new TreePath(list.toArray());
}

/**
Checks to see if the tree contains the specified node.
@param node the node for which to check the tree.
@return true if the tree contains the node, false if not.
*/
public boolean hasNode(SimpleJTree_Node node) {
	SimpleJTree_Node root = getRoot();
	if (root == node) {
		return true;
	}
	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".hasNode",
			"Looking for node: " + node.toString());
	}
	return hasNode(root, node);
}

/**
Private utility function used by <tt>hasNode()</tt> to recurse through the
nodes and see if the tree has the specified node.
@param node the node beneath which to search for a node
@param nodeToFind the node that is being located
@return true if the node was found, false if not.
*/
private boolean hasNode(SimpleJTree_Node node, SimpleJTree_Node nodeToFind) {
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			if (n == nodeToFind) {
				if (Message.isDebugOn) {
					Message.printDebug(dl, CLASS 
						+ ".hasNode",
						"  Match: " + n.toString());
				}
				return true;
			}
			else {
				if (Message.isDebugOn) {
					Message.printDebug(dl, CLASS 
						+ ".hasNode",
						"  Not a match: " 
						+ n.toString());
				}
			}			
			boolean b = hasNode(n, nodeToFind);
			if (b == true) {
				return true;
			}
		}
	}
	return false;

}

/**
SimpleJTree initialization code common to both constructors.
This does the following:<ul>
<li>Sets the root node to be invisible.</li>
<li>Sets the root handles to be invisible -- this makes the nodes flat 
against the left side of whatever the tree is resting in.</li>
<li>Sets up the SimpleJTree_CellRenderer to be used.</li>
<li>Sets up the SimpleJTree_CellEditor to be used.</li>
<li>Stores the original icon settings used by JTree and the JTree UI Manager.
</li>
</ul>
*/
private void initialize() {
	if (getUI() instanceof com.sun.java.swing.plaf.windows.WindowsTreeUI) {
		__treeType = __WINDOWS;
		setUI(new SimpleJTree_WindowsUI());
	}
	else if (getUI() instanceof com.sun.java.swing.plaf.motif.MotifTreeUI) {
		// REVISIT (JTS - 2003-05-12)
		// need to write Motif UI code for the tree
		__treeType = __MOTIF;
	}
	else {
		// default to the normal Metal one
		__treeType = __METAL;
	}
	setRootVisible(false);
	setShowsRootHandles(false);

	// store the default icons for the renderer (as determined by the
	// UI type)
	DefaultTreeCellRenderer renderer = 
		(DefaultTreeCellRenderer)getCellRenderer();
	__openIcon = renderer.getOpenIcon();
	__closedIcon = renderer.getClosedIcon();
	__leafIcon = renderer.getLeafIcon();
	BasicTreeUI ui = (BasicTreeUI)getUI();
	__collapsedIcon = ui.getCollapsedIcon();
	__expandedIcon = ui.getExpandedIcon();

	// this is the default (that the tree's text is editable).  Call
	// setTreeTextEditable(false) to turn it off.
	setEditable(true);

	// set up the specialized editors and renderers for the SimpleJTree
	SimpleJTree_CellRenderer simpleTreeRenderer = 
		new SimpleJTree_CellRenderer();
	setCellRenderer(simpleTreeRenderer);
	setCellEditor(new SimpleJTree_CellEditor(this, simpleTreeRenderer));

	// set a listener so that the behavior of collapsing and
	// expanding can be controlled
	__collapseExpandListener = new SimpleJTree_TreeWillExpandListener();
	__collapseExpandListener.setTree(this);
	addTreeWillExpandListener(__collapseExpandListener);	
}

/**
Returns whether the tree can be collapsed or not.  The default is to return true
unless <tt>setCollapse()</tt> has been called with false.
@return whether the tree can be collapsed or not.
*/
public boolean isCollapsible() {
	return __collapseExpandListener.isCollapseAllowed();
}

/**
Returns whether the tree can be collapsed or not.  The default is to return true
unless <tt>setCollapse()</tt> has been called with false.
@return whether the tree can be collapsed or not.
*/
public boolean getCollapse() {
	return isCollapsible();
}

/**
Returns whether the tree can be collapsed or not.  The default is to return true
unless <tt>setExpand()</tt> has been called with false.
@return whether the tree can be collapsed or not.
*/
public boolean isExpandable() {
	return __collapseExpandListener.isExpandAllowed();
}

/**
Recurses through the tree and marks which nodes are visible and which are not,
so that if the tree is modified through an add, move, remove or replace call
the tree can be re-displayed with the same nodes open.
*/
public void markVisibleNodes() {
	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".markVisibleNodes",
			"Marking visible nodes");
	}
	markVisibleNodes(getRoot());
}

/**
Private utility function used by <tt>markVisibleNodes()</tt> to determine which
nodes are visible and which are not.
@param node the node from which to start recursively checking
*/
private void markVisibleNodes(SimpleJTree_Node node) {
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			if (isVisible(getTreePath(n))) {
				if (Message.isDebugOn) {
					Message.printDebug(dl, 
						CLASS + ".markVisibleNodes",
						"  Node '" + n.getName() 
						+ "' marked visible");
				}
				n.markVisible(true);
			}
			else {
				if (Message.isDebugOn) {
					Message.printDebug(dl, 
						CLASS + ".markVisibleNodes",
						"  Node '" + n.getName() 
						+ "' marked not visible");
				}			
				n.markVisible(false);
			}

			markVisibleNodes(n);
		}
	}
}

/**
Moves the node with the given name to be under the parent with the given name 
(both found by String.equalsIgnoreCase).
None of the node's children will be moved when the node is moved.
@param nodeName the name of the node to move
@param newParentName the name of the node under which to move the node
@throws Exception if either of the nodes cannot be found in the tree or if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(String nodeName, String newParentName)
throws Exception {
	moveNode(nodeName, newParentName, false);
}

/**
Moves the node with the given name to be under the parent with the given name
(both found by String.equalsIgnoreCase).
@param nodeName the name of the node to move
@param newParentName the name of the node under which to move the node
@param moveChildren whether the children of the node should be moved when 
the node is moved (true) or whether they should simply be deleted (false)
@throws Exception if either of the nodes cannot be found in the tree or if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(String nodeName, String newParentName, 
boolean moveChildren) 
throws Exception {
	moveNode(nodeName, newParentName, moveChildren, -1);
}

/**
Moves the node with the given name to be under the parent with the given name
and places it in the specified position.
@param nodeName the name of the node to move
@param newParentName the name of the node under which to move the node
@param moveChildren whether the children of the node should be moved when 
the node is moved (true) or whether they should simply be deleted (false)
@param pos the position under the new parent node to which to move the node.
If -1, the node will be placed after all the other nodes already under the 
root node.  If &lt; -1 or &gt; the number of nodes under the root 
node, the node will be placed after all the other nodes already under the 
root node.
@throws Exception if either of the nodes cannot be found in the tree or if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(String nodeName, String newParentName, 
boolean moveChildren, int pos)
throws Exception {
	moveNode(findNodeByName(nodeName), findNodeByName(newParentName), 
		moveChildren, pos);
}

/**
Moves the node with the given name to be under the parent with the given name.
None of the node's children will be moved when the node is moved.
@param nodeToMove the node to move
@param newParent the node under which to move the node
@throws Exception if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(SimpleJTree_Node nodeToMove, SimpleJTree_Node newParent)
throws Exception {
	moveNode(nodeToMove, newParent, false, -1);
}

/**
Moves the node with the given name to be under the parent with the given name.
@param nodeToMove the node to move
@param newParent the node under which to move the node
@param moveChildren whether the children of the node should be moved when 
the node is moved (true) or whether they should simply be deleted (false)
@throws Exception if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(SimpleJTree_Node nodeToMove, SimpleJTree_Node newParent, 
boolean moveChildren) 
throws Exception {
	moveNode(nodeToMove, newParent, moveChildren, -1);
}

/**
Moves the node with the given name to be under the parent with the given name.
@param nodeToMove the node to move
@param newParent the node under which to move the node
@param moveChildren whether the children of the node should be moved when 
the node is moved (true) or whether they should simply be deleted (false)
@param pos the position under the parent node to which to move the node.
If -1, the node will be placed after all the other nodes already under the 
root node.  If &lt; -1 or &gt; the number of nodes under the root 
node, the node will be placed after all the other nodes already under the 
root node.
@throws Exception if<ul>
<li>the node to move is the same as the parent node<li>
<li>the node to move is the root node</li>
<li>the node to move has no parent (i.e., it's not yet in the tree)</li>
<li>the parent is a descendant of the node to move</li>
</ul>
*/
public void moveNode(SimpleJTree_Node nodeToMove, SimpleJTree_Node newParent,
boolean moveChildren, int pos) 
throws Exception {
	if (nodeToMove == null) {
		throw new Exception ("null nodeToMove");
	}
	if (newParent == null) {
		throw new Exception ("null newParent");
	}

	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".moveNode",
			"Moving node '" + nodeToMove.getName() + "' to new "
			+ "parent '" + newParent.getName() + "'.  Moving "
			+ "children: " + moveChildren + ".  New position will "
			+ "be " + pos + ".");
	}

	// stop any cell editing that is in progress
	stopEditing();
	DefaultTreeModel model = (DefaultTreeModel)getModel();
	SimpleJTree_Node parent = getParentOfNode(nodeToMove);

	if (parent == newParent) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".moveNode",
				"Current parent node is the same as the new "
				+ "parent node -- nothing will be changed.");
		}
		// the node is being moved to the same parent it already
		// has; do nothing and return.
		return;
	}

	if (nodeToMove == getRoot()) {
		throw new Exception ("Node to move '" + nodeToMove.getName()
			+ "' is the root node.");
	}

	if (nodeToMove == newParent) {
		throw new Exception ("Node to move '" + nodeToMove.getName()
			+ "' is the same as the parent node.");
	}

	TreePath tpNode = getTreePath(nodeToMove);
	if (((SimpleJTree_Node)tpNode.getPathComponent(0)) == nodeToMove) {
		throw new Exception ("Node to move '" + nodeToMove.getName()
			+ "' has no parent.");
	}

	TreePath tpParent = getTreePath(newParent);
	if (tpNode.isDescendant(tpParent)) {
		throw new Exception ("Parent node '" + newParent.getName()
			+ "' is a descendant of the node to move '"
			+ nodeToMove.getName() + "'");
	}

	// if the children don't have to move when the node moves it 
	// is easier, so that case is handled here.
	if (!moveChildren) {
		model.removeNodeFromParent(nodeToMove);
		model.nodeStructureChanged(parent);	
		addNode(nodeToMove, newParent, pos);
		model.nodeStructureChanged(newParent);

		return;
	}

	// otherwise, move the node and all its children to the new parent
	moveNodes(nodeToMove, newParent, pos);	
}

/**
Private utility function used by <tt>moveNode()</tt> to move a node and all
its children to a new parent.  It does this by duplicating the node and
all its children at the new place under the new parent, then removing the 
old node and all its children from the tree.
@param nodeToMove the node to move to a new parent.
@param newParent the parent node to which to move the node
@param pos the position under the new parent at which to place the new node
*/
private void moveNodes(SimpleJTree_Node nodeToMove, SimpleJTree_Node newParent,
int pos) {
	DefaultTreeModel model = (DefaultTreeModel)getModel();
	markVisibleNodes();
	moveNodesRecurse(nodeToMove, newParent, pos);
	SimpleJTree_Node parent = getParentOfNode(nodeToMove);
	trimOldNodes(nodeToMove);
	model.nodeStructureChanged(parent);
	model.nodeStructureChanged(newParent);
	resetVisibleNodes();
}

/**
Private utility function used by <tt>moveNodes()</tt> to recursively 
duplicate a node and all its children to a new parent node and mark all the
original nodes as needing deletion (by means of <tt>trimOldNodes()</tt>.
@param node the node to move to a new parent.
@param newParent the parent under which to move the node.
@param pos the position under the new parent atw hich to place the new node.
*/
private void moveNodesRecurse(SimpleJTree_Node node, SimpleJTree_Node newParent,
int pos) {
	SimpleJTree_Node clonedNode = (SimpleJTree_Node)node.clone();
	int childCount = newParent.getChildCount();
	if (pos > childCount) {
		pos = childCount;
	}
	if (pos == -1) {
		pos = childCount;
	}
	
	DefaultTreeModel model = (DefaultTreeModel)getModel();
	model.insertNodeInto(clonedNode, newParent, pos);

	node.markForDeletion(true);

	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".moveNodes",
			"Node " + node.getName() + " cloned, inserted, and "
			+ "marked for deletion.");
	}

	int count = 0;
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			moveNodesRecurse(n, clonedNode, count);
			count++;
		}
	}
}

/**
Sets the tree to not allow the collapsing of nodes.
*/
public void preventCollapsing() {
	// REVISIT (JTS - 2004-01-06)
}

/**
Refreshes the tree and forces the model to redraw all the nodes.  The nodes'
visibility will be maintained so that those that were visible before will 
be visible after.
*/
public void refresh() {
	refresh(true);
}

/**
Refreshes the tree and forces the model to redraw all the nodes.  
@param keepVisibility if true then the nodes that were visible prior to the
refresh will be visible after the refresh.  If false, then only the top-most
nodes will be visible and expanded.
*/
public void refresh(boolean keepVisibility) {
	if (keepVisibility) {
		markVisibleNodes();
	}
	DefaultTreeModel model = (DefaultTreeModel)getModel();
	model.nodeStructureChanged(getRoot());
	if (keepVisibility) {
		resetVisibleNodes();
	}
}

/**
Removes all of the nodes from the tree.
*/
public void removeAllNodes() 
throws Exception {
	SimpleJTree_Node root = getRoot();
	removeChildren(root);
}

/**
Removes all the children of the specified node.
@param name the name of the node from which to remove the children.
*/
public void removeChildren(String name) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Node with name '" + name + "' not "
			+ "found");
	}
	removeChildren(node);
}

/**
Removes all the children of the specified node.
@param node the node from which to remove the child nodes.
*/
public void removeChildren(SimpleJTree_Node node) 
throws Exception {
	DefaultTreeModel model = (DefaultTreeModel)getModel();

	// remove all the children from the model
	if (node.getChildCount() > 0) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".removeChildren",
				"Node '" + node.getName() + "' has " 
				+ node.getChildCount() + " children to be "
				+ "deleted.");
		}
		Enumeration e = node.children();
		List v = new Vector();
		for (; e.hasMoreElements();) {
			v.add(e.nextElement());
		}

		for (int i = 0; i < v.size(); i++) {
			SimpleJTree_Node n = (SimpleJTree_Node)v.get(i);
			if (Message.isDebugOn) {
				Message.printDebug(dl, CLASS +".removeChildren",
					"  Removing child '" + n.getName()+"'");
			}
			removeNode(n, false);
		}
	}

	// and then refresh the structure of the tree
	model.nodeStructureChanged(node);	
}

/**
Removes the node with the given name  (as found by String.equalsIgnoreCase) 
and all its children from the tree.
@param name the name of the node to remove.
@throws Exception if the named node could not be found.
*/
public void removeNode(String name) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Node with name '" + name + "' not "
			+ "found");
	}
	removeNode(node, false);
}

/**
Removes the node with the given name  (as found by String.equalsIgnoreCase) 
from the tree.
@param name the name of the node to remove.
@param saveChildren if true, then the children of the node to be deleted
are not deleted, but are instead made as new child nodes of the deleted node's
parent.  If false, the child nodes are also deleted.
@throws Exception if the named node could not be found.
*/
public void removeNode(String name, boolean saveChildren) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Node with name '" + name + "' not "
			+ "found");
	}
	removeNode(node, saveChildren);
}

/**
Removes the specified node and all its children from the tree.
@param node the node to remove from the tree.
@throws Exception if an error occurs
*/
public void removeNode(SimpleJTree_Node node) 
throws Exception {
	removeNode(node, false);
}

/**
Removes the specified node from the tree.
@param node the node to remove from the tree.
@param saveChildren if true, then the children of the node to be deleted
are not deleted, but are instead made as new child nodes of the deleted node's
parent.  If false, the child nodes are also deleted.
@throws Exception if an error occurs.
*/
public void removeNode(SimpleJTree_Node node, boolean saveChildren) 
throws Exception {
	SimpleJTree_Node parent = getParentOfNode(node);
	DefaultTreeModel model = (DefaultTreeModel)getModel();

	// stop any cell editing that is in progress
	stopEditing();
	
	markVisibleNodes();
	// If the child nodes do not need to be saved, it's a lot easier.
	// handle that case here and return.
	if (!saveChildren) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".removeNode",
				"Deleting node '" + node.getName() + "' and "
				+ "all its children.");
		}
		model.removeNodeFromParent(node);
		model.nodeStructureChanged(parent);
		return;
	}

	// Otherwise, the child nodes are all moved to be nodes of the parent
	if (node.getChildCount() > 0) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".removeNode",
				"Node '" + node.getName() + "' has "
				+ node.getChildCount() + " children to save.");
		}
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			if (Message.isDebugOn) {
				Message.printDebug(dl, CLASS + ".removeNode",
					"Moving child '" + n.getName() + "' to "
					+ "node '" + node.getName() + "''s "
					+ "parent.");
			}
			moveNode(n, parent, true);
		}
	}

	// and then the node itself is removed.
	model.removeNodeFromParent(node);
	model.nodeStructureChanged(parent);	
	resetVisibleNodes();
}

/**
Removes a listener from the listeners Vector.
@param listener the listener to remove.
*/
public void removeSimpleJTreeListener(SimpleJTree_Listener listener) {
	if (__listeners == null) {
		return;
	}
	int size = __listeners.size();
	for (int i = 0; i < size; i++) {
		if (((SimpleJTree_Listener)__listeners.get(i))
			== listener) {
			__listeners.remove(i);
			return;
		}
	}
}
			
/**
Replaces a node in the tree with a new node.  All the children of the 
original node become children of the new node. 
If the changes to the node aren't very drastic, it might be worthwhile to
simply call SimpleJTree_Node's setComponent(), setData(), setIcon(), or 
setText() methods.
@param nodeToReplaceName the name of the node to replace.
@param newNode the node to put in the old node's stead.
@throws Exception if the named node could not be found or if the newNode
is already in the tree
*/
public void replaceNode(String nodeToReplaceName, SimpleJTree_Node newNode) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(nodeToReplaceName);
	if (node == null) {
		throw new Exception ("Node '" + nodeToReplaceName
			+ "' not found");
	}
	replaceNode(node, newNode);
}

/**
Replaces a node in the tree with a new node.  All the children of the
original node become children of the new node.
If the changes to the node aren't very drastic, it might be worthwhile to
simply call SimpleJTree_Node's setComponent(), setData(), setIcon(), or 
setText() methods.
@param nodeToReplace the the node to replace.
@param newNode the node to put in the old noed's stead.
@throws Exception if an error occurs of if the newNode already is
in the tree.
*/
public void replaceNode(SimpleJTree_Node nodeToReplace, 
SimpleJTree_Node newNode) 
throws Exception {
	if (hasNode(newNode)) {
		throw new Exception ("Node '" + newNode.getName() + "' "
			+ "already exists in tree.");
	}

	if (Message.isDebugOn) {
		Message.printDebug(dl, CLASS + ".replaceNode",
			"Replacing node '" + nodeToReplace.toString() + "' "
			+ "with node '" + newNode.toString() + "'");
	}

	SimpleJTree_Node parent = getParentOfNode(nodeToReplace);
	DefaultTreeModel model = (DefaultTreeModel)getModel();

	// give the new node the same visibility that the original
	// node had, so that the tree looks the same after
	// the replacement
	newNode.markVisible(nodeToReplace.isVisible());
	
	// get the location of the original node in the tree so that
	// the new node is placed in the same point, and then add
	// the new node into the tree
	int index = model.getIndexOfChild(parent, nodeToReplace);
	addNode(newNode, parent, index);
	
	// move the original node's children over onto the new node
	if (nodeToReplace.getChildCount() > 0) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".replaceNode",
				"The node to be replaced has " 
				+ nodeToReplace.getChildCount() + " children "
				+ "to be stored under the new node.");
		}
		for (Enumeration e = nodeToReplace.children(); 
			e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			if (Message.isDebugOn) {
				Message.printDebug(dl, CLASS + ".replaceNode",
					"Saving child node '" + n.getName() 
					+ "'");
			}
			moveNode(n, newNode, true);
		}
	}

	// remove the original node
	removeNode(nodeToReplace);

	// make the tree visible like it was before the replacement
	resetVisibleNodes();
}

/**
Sets the node visibility the same as it was when a call was made to 
<tt>markVisibleNodes()</tt>.  This is used by methods that add, remove, 
replace or move nodes because when the default tree behavior is to close
up all the nodes after a major restructuring, and this allows the 
look of the tree (in terms of which nodes are open and which are closed)
to remain the same.<p>
<b>Note:</b> When nodes are added to the tree, they are initially set to
be visible (though if their parents aren't visible, they won't be).  When
nodes are moved within the tree or replaced, the visibilty of the 
original node is kept with the moved or replaced node. 
*/
public void resetVisibleNodes() {
	SimpleJTree_Node root = getRoot();
	if (root.getChildCount() > 0) {
		for (Enumeration e = root.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			resetVisibleNodes(n);
		}
	}	
}

/**
Private utility function used by <tt>resetVisibleNodes()</tt> to recurse
through the tree and set the node visibility based on what it was when
<tt>markVisibleNodes()</tt> was called.
@param node the node to check the previous visibility of and make visible
or invisible, accordingly.
*/
private void resetVisibleNodes(SimpleJTree_Node node) {
	if (node.getChildCount() > 0) {
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".resetVisibleNodes",
				"Node '" + node.getName() + "' has "
				+ node.getChildCount() + " children.");
		}
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			if (n.isVisible()) {
				if (Message.isDebugOn) {
					Message.printDebug(dl, 
						CLASS + ".resetVisibleNodes",
						"  Node '" + n.getName() + "' "
						+ "set visible.");
				}
				makeVisible(getTreePath(n));
			}
			else {
				if (Message.isDebugOn) {
					Message.printDebug(dl, 
						CLASS + ".resetVisibleNodes",
						"  Node '" + n.getName() + "' "
						+ " not set visible.");
				}			
			}
			
			resetVisibleNodes(n);
		}
	}
}

/**
Private utility function that goes through the tree looking for nodes which 
have been marked for deletion with 
<tt>SimpleJTree_Node.markForDeletion()</tt> and removes them from the
tree.
@param node the node from which to start checking for nodes that need to be
deleted.
*/
private void trimOldNodes(SimpleJTree_Node node) {
	if (node.getChildCount() > 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			trimOldNodes(n);	
		}
	}
	if (node.shouldBeDeleted()) {
		DefaultTreeModel model = (DefaultTreeModel)getModel();
		if (Message.isDebugOn) {
			Message.printDebug(dl, CLASS + ".trimOldNodes",
				"Trimming old node '" + node.getName() + "' "
				+ "from tree.");
		}
		model.removeNodeFromParent(node);
	}
}

/**
Scrolls to a visible node with the given name (as found by 
String.equalsIgnoreCase) and selects the node.
@param name the name of a node to scroll to.
@throws Exception if the named node could not be found.
*/
public void scrollToVisibleNode(String name) 
throws Exception {
	scrollToVisibleNode(name, true);
}

/**
Scrolls to a visible node with the given name (as found by 
String.equalsIgnoreCase).
@param name the name of a node to scroll to.
@param select whether to select the node or not.
@throws Exception if the named node could not be found.
*/
public void scrollToVisibleNode(String name, boolean select) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Could not find node '" + name + "'");
	}
	scrollToVisibleNode(node, select);
}

/**
Scrolls to the specified node and selects it.
@param node the node to scroll to.
*/
public void scrollToVisibleNode(SimpleJTree_Node node) {
	scrollToVisibleNode(node, true);
}

/**
Scrolls to the specified node.
@param select whether to select the node once it is scrolled to.
@param node the node to scroll to.
*/
public void scrollToVisibleNode(SimpleJTree_Node node, boolean select) {
	if (hasNode(node)) {
		scrollRowToVisible(getRowCount() - 1);
		expandPath(getTreePath(node));
		scrollPathToVisible(getTreePath(node));
	}
	else {
		Message.printStatus(1, "", "Node node found in tree: " 
			+ node);
	}
	if (select) {
		selectNode(node);
	}
}

/**
Selects a node in the tree so that it is the only node selected.
@param name the name of the node to select.
@throws Exception if no nodes with the given name can be found.
*/
public void selectNode(String name) 
throws Exception {
	selectNode(name, true);
}

/**
Selects a node in the tree, but possibly leaves the selection state of the
other nodes alone.
@param name the name of the node to select.
@param clearSelection if true, then all the other nodes will be deselected 
prior to this node being selected.  If false, any other selected nodes will
remain selected.
@throws Exception if no nodes with the given name can be found.
*/
public void selectNode(String name, boolean clearSelection) 
throws Exception {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		throw new Exception ("Could not find node '" + name + "'");
	}
	selectNode(node, clearSelection);
}

/**
Selects a node in the tree so that it is the only node selected.
@param node the node to select.
*/
public void selectNode(SimpleJTree_Node node) {
	selectNode(node, true);
}

/**
Selects a node in the tree, but possibly leaves the selection state of the
other nodes alone.
@param node the node to select.
@param clearSelection if true, then all the other nodes will be deselected 
prior to this node being selected.  If false, any other selected nodes will
remain selected.
*/
public void selectNode(SimpleJTree_Node node, boolean clearSelection) {
	TreePath path = getTreePath(node);
	if (clearSelection) {
		setSelectionPath(path);
	}
	else {
		addSelectionPath(path);
	}
}

/**
Sets the Icon to use as the closed icon.  This is the icon that appears 
in the label next to text in a node that has children (e.g., a closed folder
to represent an un-opened subdirectory).
@param icon the Icon to use as the closed icon.
*/
public void setClosedIcon(Icon icon) {
	SimpleJTree_CellRenderer renderer = 
		(SimpleJTree_CellRenderer)getCellRenderer();
	renderer.setClosedIcon(icon);
	__closedIcon = icon;
}

/**
Sets whether the tree can collapse or not.  If collapsing is turned off, then
<b>NO</b> collapsing is allowed, <i>even if the collapsing is done 
programmatically</i>.
@param collapse if true, collapsing is allowed (default behavior).  If false,
it is not.
*/
public void setCollapse(boolean collapse) {
	__collapseExpandListener.setCollapseAllowed(collapse);
}

/**
Sets the Icon to use as the collapsed icon.  This is used when a branch of a
tree is closed (e.g., a box with a plus in it representing a branch that 
can be expanded).
@param icon the Icon to use as the collapsed icon.
*/
public void setCollapsedIcon(Icon icon) {
	BasicTreeUI ui = (BasicTreeUI)getUI();	
	ui.setCollapsedIcon(icon);
	__collapsedIcon = icon;
}

/**
Sets whether the tree can expand or not.  If expanding is turned off, then
<b>NO</b> expanding is allowed, <i>even if the expanding is done 
programmatically</i>.
@param expand if true, expanding is allowed (default behavior).  If false,
it is not.
*/
public void setExpandAllowed(boolean expand ) {
	__collapseExpandListener.setExpandAllowed(expand);
}

/**
Sets the Icon to use as the expanded icon.  This is the icon used when a 
branch of a tree has been opened (e.g., a box with a minux sign in it 
representing a branch that can be closed).
@param icon the Icon to use as the expanded icon.
*/
public void setExpandedIcon(Icon icon) {
	BasicTreeUI ui = (BasicTreeUI)getUI();	
	ui.setExpandedIcon(icon);
	__expandedIcon = icon;
}

/**
Sets whether the tree should disable some safety/convenience checks in order
to dramatically speed up the addNode() process. <p>
<b>Notes:</b>
Turning on <u>fast add</u> disables the following things in the addNode() 
method:<p>
<ul>
<li>The tree does not check whether the parent node of the node that is
being added is an actual node in the tree.</li>
<li>The tree does not notify the model that the node structure changed
after a node is inserted.</li>
<li>The tree does not keep track of which nodes are visible prior to adding
the new node, nor does it reset those nodes that are visible after adding 
the node.  This means that all the nodes added when <u>fast add</u> is on
will be initially non-visible, unless they are root nodes.</li>
<li>The tree does not scroll automatically to the newly-added node.</li>
</ul>
<p>
In general, on JTS's computer, <u>fast add</u> made a dramatic difference 
in the node-adding speeds, particularly in large trees.  For example, in a
tree that consisted of 10 nodes, each with 100 child nodes beneath them, it
took <b>18.531</b> seconds to add all the nodes without <u>fast add</u>.  When 
<u>fast add</u> was turned on, it took <b>0.093</b> seconds.
<p>
On a tree with 25 nodes, each with 250 child nodes, it took <b>629.016</b>
seconds.  When <u>fast add</u> was turned on, the same tree took 
<b>0.469</b> seconds to build.
<p>
When <u>fast add</u> is off, the tree does a lot of recursion through all
its nodes, and the time to add new nodes grows linearly longer as more nodes
are added.
<p>
Programmers should realize the potential pitfalls of running with 
<u>fast add</u> on. Since nodes' parents aren't checked for validity (i.e., 
that they are in the tree already), programmers should be absolutely sure 
that they are adding a node to an existing parent node.
<p>
Programmers also need to expand the tree manually once they add nodes with
<u>fast add</u> since the addNode() doesn't keep track of the open and closed
nodes.<p>
<b>Note:</b> when called with a parameter of 'false', this method also calls
refresh(true).  To avoid this, use the method setFastAdd(boolean, boolean).
@param fastAdd whether to speed up the addNode() process
*/
public void setFastAdd(boolean fastAdd) {
	setFastAdd(fastAdd, true);
}

/**
Sets whether the tree should disable some safety/convenience checks in order
to dramatically speed up the addNode() process. <p>
<b>Notes:</b>
Turning on <u>fast add</u> disables the following things in the addNode() 
method:<p>
<ul>
<li>The tree does not check whether the parent node of the node that is
being added is an actual node in the tree.</li>
<li>The tree does not notify the model that the node structure changed
after a node is inserted.</li>
<li>The tree does not keep track of which nodes are visible prior to adding
the new node, nor does it reset those nodes that are visible after adding 
the node.  This means that all the nodes added when <u>fast add</u> is on
will be initially non-visible, unless they are root nodes.</li>
<li>The tree does not scroll automatically to the newly-added node.</li>
</ul>
<p>
In general, on JTS's computer, <u>fast add</u> made a dramatic difference 
in the node-adding speeds, particularly in large trees.  For example, in a
tree that consisted of 10 nodes, each with 100 child nodes beneath them, it
took <b>18.531</b> seconds to add all the nodes without <u>fast add</u>.  When 
<u>fast add</u> was turned on, it took <b>0.093</b> seconds.
<p>
On a tree with 25 nodes, each with 250 child nodes, it took <b>629.016</b>
seconds.  When <u>fast add</u> was turned on, the same tree took 
<b>0.469</b> seconds to build.
<p>
When <u>fast add</u> is off, the tree does a lot of recursion through all
its nodes, and the time to add new nodes grows linearly longer as more nodes
are added.
<p>
Programmers should realize the potential pitfalls of running with 
<u>fast add</u> on. Since nodes' parents aren't checked for validity (i.e., 
that they are in the tree already), programmers should be absolutely sure 
that they are adding a node to an existing parent node.
<p>
Programmers also need to expand the tree manually once they add nodes with
<u>fast add</u> since the addNode() doesn't keep track of the open and closed
nodes.<p>
@param fastAdd whether to speed up the addNode() process
@param keepVisibility when this method is called with fastAdd == false, 
refresh() is also called with the keepVisibility parameter passed in.
*/
public void setFastAdd(boolean fastAdd, boolean keepVisibility) {
	__fastAdd = fastAdd;
	if (!__fastAdd) {
		refresh(keepVisibility);
	}
}

/**
Sets the Icon to use as the leaf icon.  This is the icon that appears next 
to labels on tree leaves.
@param icon the Icon to use as the leaf icon.
*/
public void setLeafIcon(Icon icon) {
	SimpleJTree_CellRenderer renderer = 
		(SimpleJTree_CellRenderer)getCellRenderer();
	renderer.setLeafIcon(icon);
	__leafIcon = icon;
}

/**
Sets the line style to use for separating and demarcating nodes, their
children, and their parents.  
@param style the style to use.  For Windows, it is one of LINE_NONE or 
LINE_ANGLED.  For Motif, it is REVISIT (JTS - 2003-05-12).  For the Swing
Metal Look and Feel, it is one of LINE_NONE, LINE_ANGLED or LINE_HORIZONTAL.
Any other value will result in no change to the style.
*/
public void setLineStyle(int style) {
	if (__treeType == __WINDOWS) {
		if (style == LINE_NONE) {
			((SimpleJTree_WindowsUI)getUI()).setLineStyle(style);
		}
		else if (style == LINE_ANGLED) {
			((SimpleJTree_WindowsUI)getUI()).setLineStyle(style);
		}
	}
	else if (__treeType == __MOTIF) {
		// REVISIT (JTS - 2003-05-12)
	}
	else if (__treeType == __METAL) {
		String styleString = null;
		if (style == LINE_NONE) {
			styleString = "None";	
		}
		else if (style == LINE_ANGLED) {
			styleString = "Angled";
		}
		else if (style == LINE_HORIZONTAL) {
			styleString = "Horizontal";
		}
		putClientProperty("JTree.lineStyle", styleString);
	}
	else {
		return;
	}
}

/**
Sets the data for the node with the given name (as found by 
String.equalsIgnoreCase).
@param name the name of the node to set the data for
@param data the data to set in the node.
*/
public void setNodeData(String name, Object data) {
	SimpleJTree_Node node = findNodeByName(name);
	if (node == null) {
		return;
	}
	node.setData(data);
}

/**
Sets the Icon to use as the open icon.  This is the icon that appears next
to branches that have been expanded (e.g., a folder representing a directory
branch that has been opened).
@param icon the Icon to use as the open icon.
*/
public void setOpenIcon(Icon icon) {
	SimpleJTree_CellRenderer renderer = 
		(SimpleJTree_CellRenderer)getCellRenderer();
	renderer.setOpenIcon(icon);
	__openIcon = icon;
}

/**
Sets whether the text in the tree can be clicked on and edited or not.  The
default is that the text is editable.
@param editable if true, then the SimpleJTree_CellEditor will allow text
values stored in the tree (in the fashion of the original JTree 
functionality) to be edited.  If false, these values can not be edited.
*/
public void setTreeTextEditable(boolean editable) {
	((SimpleJTree_CellEditor)getCellEditor()).setEditable(editable);
}

/**
Show root handles on the tree.  The root handles are the
handles that extend from the root node to its children node.  Even if the root
node is not displayed, there will be space for these handles -- if a line
style of LINE_ANGLED is specified to <tt>setLineStyle()</tt>, connection
lines will run from each of the nodes under the root, too.  If this method
is called, these handles will be show and the tree will not press up flush
against the left-side of whatever container it is in.
@param state if true, the root handles will be shown.  If false, they will not.
*/
public void showRootHandles(boolean state) {
	setShowsRootHandles(state);
}

/**
Sets the tree to show its icons.  If a call has been made to <tt>hideIcons</tt>,
then the icons that were originally used by the tree will be restored.  If
any of the icons have been changed with calls to <tt>set*Icon()</tt>, those
icons will be shown.
@param state if true, the icons will be shown.  If false, they won't.
*/
public void showIcons(boolean state) {
	showIcons(OPEN_ICON | CLOSED_ICON | LEAF_ICON | COLLAPSED_ICON | 
		EXPANDED_ICON, state);
}

/**
Sets the tree to show its icons.  If a call has been made to <tt>hideIcons</tt>,
then the icons that were originally used by the tree will be restored.  If
any of the icons have been changed with calls to <tt>set*Icon()</tt>, those
icons will be shown.
@param iconFlag a logical OR ('|') of all the icon values (e.g., CLOSED_ICON,
OPEN_ICON, etc) that should be shown.
@param state if true, the icons will be shown.  If false, they will not.
*/
public void showIcons(int iconFlag, boolean state) {
	SimpleJTree_CellRenderer renderer = 
		(SimpleJTree_CellRenderer)getCellRenderer();

	Icon tempIcon;
		
	if ((iconFlag & OPEN_ICON) == OPEN_ICON) {
		if (state) {
			if (__openIcon != null) {
				renderer.setOpenIcon(__openIcon);
			}
		}
		else {
			tempIcon = renderer.getOpenIcon();
			if (tempIcon != null) {
				__openIcon = tempIcon;
			}
			renderer.setOpenIcon(null);
		}	
	}

	if ((iconFlag & CLOSED_ICON) == CLOSED_ICON) {
		if (state) {
			if (__closedIcon != null) {
				renderer.setClosedIcon(__closedIcon);
			}
		}
		else {
			tempIcon = renderer.getClosedIcon();
			if (tempIcon != null) {
				__closedIcon = tempIcon;
			}
			renderer.setClosedIcon(null);
		}		
	}
	
	if ((iconFlag & LEAF_ICON) == LEAF_ICON) {
		if (state) {
			if (__leafIcon != null) {
				renderer.setLeafIcon(__leafIcon);
			}
		}
		else {
			tempIcon = renderer.getLeafIcon();
			if (tempIcon != null) {
				__leafIcon = tempIcon;
			}
			renderer.setLeafIcon(null);
		}					
	}
	
	BasicTreeUI ui = (BasicTreeUI)getUI();
	if ((iconFlag & COLLAPSED_ICON) == COLLAPSED_ICON) {
		if (state) {
			if (__collapsedIcon != null) {
				ui.setCollapsedIcon(__collapsedIcon);
			}
		}
		else {
			tempIcon = ui.getCollapsedIcon();
			if (tempIcon != null) {
				__collapsedIcon = tempIcon;
			}
			ui.setCollapsedIcon(null);
		}				
	}

	if ((iconFlag & EXPANDED_ICON) == EXPANDED_ICON) {
		if (state) {
			if (__expandedIcon != null) {
				ui.setExpandedIcon(__expandedIcon);
			}
		}
		else {
			tempIcon = ui.getExpandedIcon();
			if (tempIcon != null) {
				__expandedIcon = tempIcon;
			}
			ui.setExpandedIcon(null);
		}					
	}
}

/**
A proplist that can be used for storing extra data in the tree.  This data is
never used by the tree.  May be removed in future (REVISIT (JTS - 2006-04-12))
but for now leave in for DataTest.
*/
private PropList __props = null;

public void setDataPropList(PropList props) {
	__props = props;
}

public PropList getDataPropList() {
	return __props;
}

}
