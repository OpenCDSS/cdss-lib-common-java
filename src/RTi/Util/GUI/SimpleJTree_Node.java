//-----------------------------------------------------------------------------
// SimpleJTree_Node - The class that makes up each node of a SimpleJTree,
// able to store either normal JTree text or a Component, and with other
// utility functions that make it easie to use than normal nodes.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-04-30	J. Thomas Sapienza, RTI	Initial version
// 2003-05-01	JTS, RTi		* Initial version complete
//					* Javadoc'd.
// 2003-05-13	JTS, RTi		* Added code so that nodes can now
//					  store data.
// 2003-05-27	JTS, RTi		Added equals().
// 2005-04-26	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;

import javax.swing.Icon;

import javax.swing.tree.DefaultMutableTreeNode;

/**
This is a specialized node used by SimpleJTree's that overrides 
DefaultMutableTreeNode and can be used inside of JTrees.  SimpleJTree_Nodes
are used instead of DefaultMutableTreeNodes because these nodes keep 
track of additional data that comes in very handy, such as:<ul>
<li>The Name associated with the node</li>
<li>Whether the node contains a component or text<li>
</ul>
<p>
The most useful data is the name, because if names are used then nodes can
be programatically referred to in the tree by a pre-determined name.  
Instead of finding a node by manually recursing through a tree or by
keeping a reference to a specific node, the node can be referred to be name, instead.
<p>
<b>Note:</b> This class is ready-to-go for insertion in a SimpleJTree, and 
applications can just use this class, but at the same time this class
is easily extensible for more specialized purposes.  The developer should
choose which way to go.  For an example of a specialized version, see
RTi.GIS.GeoView.GeoViewLegendJTree_Node.
*/
public class SimpleJTree_Node 
extends DefaultMutableTreeNode 
implements Cloneable {

/**
Whether the node contains a component (true) or just text (true).
*/
private boolean __containsComponent = false;

/**
Whether the node has been marked for future deletion or not.
*/
private boolean __delete = false;

/**
Whether the node should be visible or not.
*/
private boolean __visible = true;

/**
The Icon associated with the text in the node.
*/
private Icon __icon = null;

/**
The node's name.
*/
private String __name = null;

/**
The text held in the node.
*/
private String __text = null;

/**
Optional data of any type that can be held in the node.
*/
private Object __data = null;

/**
Constructor.  Creates a node with the given text and a name the same as the text
@param text the text to display in the node
*/
public SimpleJTree_Node(String text) {
	super(text);
	__containsComponent = false;
	__text = text;
	initialize(text);
}

/**
Constructor.  Creates a node that holds the given object and has the given name.
@param userObject the Component to store in this node.
@param name the name of this node.
*/
public SimpleJTree_Node(Component userObject, String name) {
	super(userObject);
	__containsComponent = true;
	initialize(name);
}

/**
Constructor.  Creates a node that holds the given text and has the given name.
@param text the text to hold in this node.
@param name the name of this node.
*/
public SimpleJTree_Node(String text, String name) {
	super(text);
	__text = text;
	__containsComponent = false;
	initialize(name);
}

/**
Constructor.  Creates a node that holds the given text and uses the given Icon 
and has the given name.
@param text the text to hold in this node.
@param icon the icon to display in this node.
@param name the name of this node.
*/
public SimpleJTree_Node(String text, Icon icon, String name) {
	super(text);
	__text = text;
	__icon = icon;
	__containsComponent = false;
	initialize(name);
}

/**
Clones this node and returns a copy with the identical settings.  If the 
node being cloned contains a Component, that Component is <b>NOT</b> cloned,
too.  The cloned node will contain a reference to the same Component held in
the original node.  The data stored in the cloned object is not a clone
of the original data: a reference is made to the same data object as in the original.
@return a cloned copy of this node.
*/
public Object clone() {
	SimpleJTree_Node cloned = null;
	if (__containsComponent) {
		cloned = new SimpleJTree_Node((Component)getUserObject(), __name);
	}
	else {
		cloned = new SimpleJTree_Node(__text, __icon, __name);
	}
	cloned.setData(__data);
	cloned.markForDeletion(__delete);
	cloned.markVisible(__visible);

	return cloned;
}

/**
Returns true if this node contains a component, otherwise false.
@return true if this node contains a component, otherwise false.
*/
public boolean containsComponent() {
	return __containsComponent;
}

/**
Returns true if the nodes are equal.  This is equivalent to String's equals() 
method, which is different from doing String comparisons with the == operator.
<p>
If this node (not the passed-in node) contains a component then only the names
of the nodes are checked to see if they match.
<p>
If this node (not the passed-in node) does not contain a component, then the 
name of the node, the node's icon, and the nodes text are all checked to see if they are equal.
@param node the node to compare against.
@return true if the nodes are equal.
*/
public boolean equals(SimpleJTree_Node node) {
	if (__containsComponent) {
		if (!node.containsComponent()) {
			return false;
		}
		if (!(__name.equals(node.getName()))) {
			return false;
		}
	}
	else {
		if (node.containsComponent()) {
			return false;
		}
		if (!(__name.equals(node.getName()))) {
			return false;
		}
		if (__icon == null) {
			if (node.getIcon() != null) {
				return false;
			}
		}
		else {
			if (__icon != node.getIcon()) {
				return false;
			}
		}
		if (__text == null) {
			if (node.getText() != null) {
				return false;
			}
		}
		else {
			if ((!__text.equals(node.getText()))) {
				return false;
			}
		}
	}

	return true;
}

/**
Returns the data stored in this node.
@return the data stored in this node.
*/
public Object getData() {
	return __data;
}

/**
Returns the Icon stored in this node.
@return the Icon stored in this node.
*/
public Icon getIcon() {
	return __icon;
}

/**
Return the name of this node.
@return the name of this node.
*/
public String getName() {
	return __name;
}

/**
Returns the value stored in the superclass of this node.  This is used by 
the SimpleJTree_CellRenderer to determine the value of the node <i>after</i> editing has finished.
@return the value stored in the superclass of this node.
*/
public String getSuperString() {
	return super.toString();
}

// TODO SAM 2015-12-13 Why is this here?  Doesn't the base class have already for node labels
/**
Return the text of this node (which is what is stored in the node if a Component is not stored in it).
@return the text of this node.
*/
public String getText() {
	return __text;
}

/**
Initialization method common to all constructors.  
@param name the name of the node.  If name is null, it will be created from
the <tt>toString()</tt> of this node, but that is not recommended as the
node will then be hard to refer to by name.
*/
private void initialize(String name) {
	if (name == null) {	
		__name = toString();
	}
	else {
		__name = name;
	}
}

/**
Returns whether this node is visible or not.
@return whether this node is visible or not.
*/
public boolean isVisible() {
	return __visible;
}

/**
Sets whether this node should be deleted in the future or not.
@param markDelete whether this node should be deleted in the future (true) or not (false).
*/
public void markForDeletion(boolean markDelete) {
	__delete = markDelete;
}

/**
Sets whether this node should be made visible in the future or not.
@param visible whether this node should be made visible in the future (true) or not (false).
*/
public void markVisible(boolean visible) {
	__visible = visible;
}

/**
Sets the component to store in this node.
@param component the Component to store in this node.
*/
public void setComponent(Component component) {
	setUserObject(component);
}

/**
Sets whether this node contains a component or not.
@param containsComponent whether this node contains a component (true) or not (false).
*/
public void setComponent(boolean containsComponent) {
	__containsComponent = containsComponent;
}

/**
Sets the data stored in this node.
@param data the data to store in this node.
*/
public void setData(Object data) {
	__data = data;
}

/**
Sets the Icon used if this displays text.
@param icon the icon to show if this displays text.  If null, no icon will be shown.
*/
public void setIcon(Icon icon) {
	__icon = icon;
}

/**
Sets the name of this node.
@param name the value to set the node name to
*/
public void setName(String name) {
	__name = name;
}

/**
Sets the text stored in this node.
@param text the text to store in this node.
*/
public void setText(String text) {
	__text = text;
}

/**
Returns whether this node should be deleted sometime in the future.
@return whether this node should be deleted sometime in the future (true) or not (false).
*/
public boolean shouldBeDeleted() {
	return __delete;
}

/**
Returns a String representation of the node -- with information from the super
object too, if available.  The returned string will be:<br>
SimpleJTree_Node Name: [Name as returned by getName()]<br>
  [result of super.toString()]<br>
@return a String representation of the node.
*/
public String toString() {
	return "SimpleJTree_Node Name: " + __name + " " + super.toString();
}

}