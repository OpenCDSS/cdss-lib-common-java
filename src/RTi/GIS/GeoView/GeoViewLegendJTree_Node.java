//-----------------------------------------------------------------------------
// GeoViewLegendJTree_Node - Convenience class to use when putting ESRI data
// into a SimpleJTree.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2003-05-12	J. Thomas Sapienza, RTI	Initial version
// 2003-05-13	JTS, RTi		Added support for popup menus
// 2003-05-14	JTS, RTi		* Text field selection works
//					* Lots of javadoc'ing.
// 2003-05-21	JTS, RTi		Layers were not being selected when the
//					nodes were selected; fixed that.
// 2003-05-22	JTS, RTi		* Added isSelected/setSelected/
//					  isVisible/setVisible.
//					* Added getLayerView
// 2004-09-16	JTS, RTi		Changed the call of isPopupTrigger to
//					use the one in the JPopupMenu.
// 2004-10-14	JTS, RTi		Added getFieldText().
// 2005-04-27	JTS, RTi		Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ItemSelectable;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;

import RTi.Util.GUI.JGUIUtil;

import RTi.Util.GUI.SimpleJTree_CellRenderer;
import RTi.Util.GUI.SimpleJTree_Node;

/**
This class is a convenience class for displaying CheckBox and label information
in a Tree similar to how ESRI handles its Table of Contents tree sections.
These nodes contain two components, a JCheckBox (with no text) and a separate
JLabel.  
*/
public class GeoViewLegendJTree_Node 
extends SimpleJTree_Node
implements MouseListener, ItemListener, ItemSelectable {

/**
Whether this node has been selected (i.e., the label has been clicked on) or not
*/
private boolean __selected = false;

/**
The Color in which the background of the non-selected node text should be 
drawn.
*/
private Color bg = null;
/**
The Color in which the foreground of the non-selected node text should be
drawn.
*/
private Color fg = null;

/**
Reference to the tree in which this component appears.
*/
private GeoViewLegendJTree __tree;

/**
Reference to the unlabelled checkbox that appears in this component.
*/
private JCheckBox __check = null;

/**
The popup menu associated with this node.  
*/
private JPopupMenu __popup = null;

/**
Reference to the text field label that appears in this component.
*/
private JTextField __field = null;

/**
The listeners that are registered to listen for this objects item state changed events.
*/
private List __listeners = null;

/**
Constructor.
@param text the Text to appear next to the JCheckBox in this component.
@param name the name of this node.
@param tree the tree in which this component appears
*/
public GeoViewLegendJTree_Node(String text, String name, 
GeoViewLegendJTree tree) {
	super(new JPanel(), name);
	initialize(text, name, tree, null);
}

/**
Constructor. 
@param text the Text to appear next to the JCheckbox in this component.
@param name the name of this node.
@param tree the tree in which this component appears
@param popupMenu the popupMenu that this node should display.
*/
public GeoViewLegendJTree_Node(String text, String name, 
GeoViewLegendJTree tree, JPopupMenu popupMenu) {
	super(new JPanel(), name);
	initialize(text, name, tree, popupMenu);
}

/**
Registeres an item listener for this component.
@param listener the listener to add to the list of listeners.
*/
public void addItemListener(ItemListener listener) {
	__listeners.add(listener);
}

/**
Deselects all the labels in all the other nodes in the tre.
*/
private void deselectAllOthers() {
	deselectAllOthers(__tree.getRoot());
}

/**
Utility method used by deselectAllOthers()
@param node the node from which to recurse the tree.
*/
private void deselectAllOthers(SimpleJTree_Node node) {
	if (node instanceof GeoViewLegendJTree_Node) {
		if (node != this) {
			((GeoViewLegendJTree_Node)node).deselectField();
		}		
	}
	if (node.getChildCount() >= 0) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();
			deselectAllOthers(n);
		}
	}
}

/**
Deselects the text field in this node.
*/
public void deselectField() {
	__field.setBackground(bg);
	__field.setForeground(fg);
	__field.repaint();
	__selected = false;
	GeoLayerView layerView = (GeoLayerView)getData(); 
	if (layerView != null) {
		layerView.isSelected(false);
	}
}

/**
Returns the text stored in this node.
@return the text stored in this node.
*/
public String getFieldText() {
	if (__field == null) {
		return null;
	}
	return __field.getText().trim();
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	bg = null;
	fg = null;
	__tree = null;
	__check = null;
	__popup = null;
	__field = null;
	__listeners = null;
	super.finalize();
}

/**
Returns the layer view stored in this node.
@return the layer view stored in this node.
*/
public GeoLayerView getLayerView() {
	return (GeoLayerView)getData();
}

/**
Gets the selected objects (from extending ItemSelectable; not used).
@return null.
*/
public Object[] getSelectedObjects() {
	return null;
}

/**
Initializes the settings in the GeoViewLegendJTree_Node.
@param text the Text to appear next to the JCheckBox in this component.
@param name the name of this node
@param tree the SimpleJTree that contains this component
@param listener the ItemListener to register for this component
@param popupMenu the popupMenu that this node should display.  If null, no
popup will be displayed.
*/
private void initialize(String text, String name, 
GeoViewLegendJTree tree, JPopupMenu popup) {
	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	__check = new JCheckBox();
	__check.setBackground(UIManager.getColor("Tree.textBackground"));
	__field = new JTextField();
	__tree = tree;

	// Because of the way these two components (the checkbox and the
	// label) are drawn, sometimes the first letter of the JLabel is
	// slightly (like, 2 pixels) overlapped by the CheckBox.  Adding
	// a single space at the front of the label text seems to avoid 
	// this.
	__field.setText(" " + text);

	__field.addMouseListener(this);
	__field.setEditable(false);
	__field.setBorder(null);
	__field.setFont((new SimpleJTree_CellRenderer()).getFont());
	__field.setBackground(UIManager.getColor("Tree.textBackground"));
	JGUIUtil.addComponent(panel, __check, 0, 0, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(panel, __field, 1, 0, 2, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);
	setComponent(panel);

	__check.addItemListener(this);
	__listeners = new Vector();
	addItemListener(tree);

	__popup = popup;

	// store the default label drawing colors
	bg = __field.getBackground();
	fg = __field.getForeground();
}

/**
Returns whether the check box is selected or not.
@return whether the check box is selected or not.
*/
public boolean isCheckBoxSelected() {
	return __check.isSelected();
}

/**
Returns whether the layer associated with this node is visible or not.
@return whether the layer associated with this node is visible or not.
*/
public boolean isVisible() {
	return isCheckBoxSelected();
}

/**
Returns whether the text field is selected or not.
@return whether the text field is selected or not.
*/
public boolean isTextSelected() {
	return __selected;
}

/**
Returns whether the layer associated with this node is selected or not.
@return whether the layer associated with this node is selected or not.
*/
public boolean isSelected() {
	return isTextSelected();
}

/**
Sets whether the layer associated with this node is selected or not.
@param sel whether the layer is selected or not.
*/
public void setSelected(boolean sel) {
	if (sel) {
		selectField();
	}
	else {
		
		deselectField();
	}
}

/**
Sets whether the layer associated with this node is visible or not.
@param vis whether the layer is visible or not.
*/
public void setVisible(boolean vis) {
	if (vis) {
		__check.setSelected(true);
	}
	else {
		__check.setSelected(false);
	}
}

/**
The internal item state changed event that occurs when the JCheckBox is clicked.
Internally, this class is its own listener for the JCheckBox's item state
changed event.  It catches the event and then RE-posts it so that the 
GeoViewLegendJTree that catches the new event can see which specific node
issued the event.
@param e the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent e) {
	ItemEvent newEvt = new ItemEvent(this, 0, null, e.getStateChange());
	for (int i = 0; i < __listeners.size(); i++) {
		ItemListener l = (ItemListener)__listeners.get(i);
		l.itemStateChanged(newEvt);
	}
}

/**
Checks to see if the mouse event would trigger display of the popup menu.
The popup menu does not display if it is null.
@param e the MouseEvent that happened.
*/
private void maybeShowPopup(MouseEvent e) {
	if (__popup != null && __popup.isPopupTrigger(e)) {
		__popup.show(e.getComponent(), e.getX(), e.getY());
	}
}

/**
Responds to mouse clicked events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseClicked ( MouseEvent event ) {}

/**
Responds to mouse dragged events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {}

/**
Responds to mouse entered events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent event) {}

/**
Responds to mouse exited events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseExited(MouseEvent event) {}

/**
Responds to mouse moved events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {}

/**
Responds to mouse pressed events.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	if (event.getButton() == 1) {
		if (!event.isControlDown()) {	
			deselectAllOthers();
			selectField();
		}
		else {
			if (__selected) {
				deselectField();
			}
			else {
				selectField();
			}	
		}
		__tree.repaint();
	}
	// a node was either selected or deselected -- repaint the buttons
	// in the geoviewjpanel as appropriate
	__tree.updateGeoViewJPanelButtons();	
}

/**
Responds to mouse released events.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	maybeShowPopup(event);
}

/**
Removes an item listener from the list of listeners.
@param listener the listener to remove.
*/
public void removeItemListener(ItemListener listener) {
	for (int i = 0; i < __listeners.size(); i++) {
		if ((ItemListener)__listeners.get(i) == listener) {
			__listeners.remove(i);
		}
	}
}

/**
Select's this node's text field.
*/
public void selectField() {
	__selected = true;
	__field.setBackground(__field.getSelectionColor());
	__field.setForeground(__field.getSelectedTextColor());
	__field.repaint();
	GeoLayerView layerView = (GeoLayerView)getData(); 
	if (layerView != null) {
		layerView.isSelected(true);
	}
}

/**
Sets the selected state of the JCheckBox.
@param selected the state to set the JCheckBox to
*/
public void setCheckBoxSelected(boolean selected) {
	__check.setSelected(selected);
}

}
