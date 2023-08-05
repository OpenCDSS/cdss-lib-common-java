// GeoViewLegendJTree - tree to display legend layers

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

package RTi.GIS.GeoView;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import RTi.GR.GRClassificationType;
import RTi.GR.GRLegend;
import RTi.GR.GRScaledClassificationSymbol;
import RTi.GR.GRSymbol;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ResponseJDialog;

import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTable_JFrame;

/**
The GeoViewLegendJTree is a Tree that displays the layers available in a GeoView using a typical GIS legend.
TODO (JTS - 2006-05-23) How to see this tree?  Is it automatic?
*/
@SuppressWarnings("serial")
public class GeoViewLegendJTree extends SimpleJTree
implements ActionListener, ItemListener, MouseListener
{

/**
Menu item labels.
*/
private final String
	__MENU_VIEW_ATTRIBUTE_TABLE = "View Attribute Table",
	__MENU_SHOW_ANIMATION_CONTROL_GUI = "Show Animation Control";

/**
The GeoViewJPanel in which this tree appears.
*/
private GeoViewJPanel __theGeoViewJPanel = null;

/**
The root-most and visible node in the tree.
All the layer nodes are children of this node.  When this node is null, the tree is empty.
*/
private GeoViewLegendJTree_Node __projectNode = null;

/**
The popup menu to be assigned to the project nodes.
It is displayed when a project node is right-clicked on in the tree.
*/
private JPopupMenu __projectNodePopup = null;

/**
The popup menu to be assigned to all the layer nodes except those that are animated.
It is displayed when any non-animation layer node is right-clicked on in the tree.
*/
private JPopupMenu __layerPopup = null;

/**
The popup menu to be displayed when an animated layer is right-clicked on in the tree.
*/
private JPopupMenu __animatedPopup = null;

/**
The popup menu to be displayed when the tree is right-clicked on.
*/
private JPopupMenu __treePopup = null;

/**
Constructor.
@param gvp GeoViewJPanel to contain and manage the legend panel.
*/
public GeoViewLegendJTree(GeoViewJPanel gvp) {
	// Initialize all the SimpleJTree settings.
	super();

	__theGeoViewJPanel = gvp;

	// These calls set up the SimpleJTree to display in a fashion similar to ESRI's Table of Contents.
	setClosedIcon(null);
	setOpenIcon(null);
	setLeafIcon(null);
	setLineStyle(SimpleJTree.LINE_NONE);

	// Create the popup menu for the tree nodes.
	// This popup menu has a "Properties" and a "Remove" menu item.
	__layerPopup = new JPopupMenu();
	JMenuItem mi = new JMenuItem(__theGeoViewJPanel.REMOVE);
	mi.addActionListener(this);
	__layerPopup.add(mi);
	__layerPopup.addSeparator();
	mi = new JMenuItem(__theGeoViewJPanel.PROPERTIES);
	mi.addActionListener(this);
	__layerPopup.add(mi);
	mi = new JMenuItem(__MENU_VIEW_ATTRIBUTE_TABLE);
	mi.addActionListener(this);
	__layerPopup.add(mi);	

	// Create the popup for nodes of views that are animated.
	__animatedPopup = new JPopupMenu();
	mi = new JMenuItem(__theGeoViewJPanel.REMOVE);
	mi.addActionListener(this);
	__animatedPopup.add(mi);
	__animatedPopup.addSeparator();
	mi = new JMenuItem(__theGeoViewJPanel.PROPERTIES);
	mi.addActionListener(this);
	__animatedPopup.add(mi);
	mi = new JMenuItem(__MENU_VIEW_ATTRIBUTE_TABLE);
	mi.addActionListener(this);
	__animatedPopup.add(mi);	
	mi = new JMenuItem(__MENU_SHOW_ANIMATION_CONTROL_GUI);
	mi.addActionListener(this);
	__animatedPopup.add(mi);
	
	// Create the popup menu for the tree.  This has an "Add" menu item.
	__treePopup = new JPopupMenu();
	mi = new JMenuItem(__theGeoViewJPanel.ADD);
	mi.addActionListener(this);
	__treePopup.add(mi);
	__treePopup = null;

	// Create the popup menu for project nodes.  This has an "Open Project" menu item.
	__projectNodePopup = new JPopupMenu();
	mi = new JMenuItem(__theGeoViewJPanel.OPEN_GVP);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	__projectNodePopup.addSeparator();
	
	mi = new JMenuItem(__theGeoViewJPanel.ADD);
	mi.addActionListener(this);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.ADD_SUMMARY_LAYER_TO_GEOVIEW);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.SET_ATTRIBUTE_KEY);
	mi.addActionListener(__theGeoViewJPanel);	
	__projectNodePopup.add(mi);
	__projectNodePopup.addSeparator();

	mi = new JMenuItem(__theGeoViewJPanel.SELECT_GEOVIEW_ITEM);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.GEOVIEW_ZOOM);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.GEOVIEW_ZOOM_OUT);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	__projectNodePopup.addSeparator();

	mi = new JMenuItem(__theGeoViewJPanel.PRINT_GEOVIEW);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.SAVE_AS_IMAGE_MENU);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);
	mi = new JMenuItem(__theGeoViewJPanel.SAVE_AS_SHAPEFILE_MENU);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);	

	__projectNodePopup.addSeparator();
	mi = new JMenuItem(__theGeoViewJPanel.GEOVIEW_PROPERTIES);
	mi.addActionListener(__theGeoViewJPanel);
	__projectNodePopup.add(mi);

	__treePopup = __projectNodePopup;
	
	addMouseListener(this);

	// Allow each row's height to be computed individually.
	setRowHeight(0);
}

/**
Handle action events from the popup menus.
@param evt ActionEvent from popup menus
*/
public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();

	if (command.equals(__theGeoViewJPanel.ADD_LAYER_TO_GEOVIEW) || command.equals(__theGeoViewJPanel.ADD)) {
		__theGeoViewJPanel.addLayerView();
	}
	else if (command.equals(__theGeoViewJPanel.REMOVE)) {
		List<GeoLayerView> selectedLayerViews = getSelectedLayerViews(false);
		if (selectedLayerViews.size() == 0) {
			// This should not happen.
			return;
		}

		String plural = "";
		if (selectedLayerViews.size() > 1) {
			plural = "s";
		}

		int response = new ResponseJDialog( __theGeoViewJPanel.getParentJFrame(), "Remove layer" + plural + "?",
			"Are you sure you want to remove the selected layer" + plural + " from the view?",
			ResponseJDialog.YES | ResponseJDialog.NO ).response();
		if (response == ResponseJDialog.NO) {
			return;
		}	
		
		for (int i = 0; i < selectedLayerViews.size(); i++) {
			__theGeoViewJPanel.removeLayerView( selectedLayerViews.get(i), true);
		}
	}
	else if (command.equals(__theGeoViewJPanel.PROPERTIES)) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)getSelectedNode();
		GeoLayerView layerView = (GeoLayerView)node.getData();
		__theGeoViewJPanel.showLayerViewProperties(layerView);
	}
	else if (command.equals(__MENU_SHOW_ANIMATION_CONTROL_GUI)) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)getSelectedNode();
		GeoLayerView layerView = (GeoLayerView)node.getData();
		layerView.getAnimationControlJFrame().setVisible(true);
	}
	else if (command.equals(__MENU_VIEW_ATTRIBUTE_TABLE)) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)getSelectedNode();	
		GeoLayerView layerView = (GeoLayerView)node.getData(); 	
		GRLegend legend = layerView.getLegend();
		String layerName = legend.getText();
		String filename = layerView.getLayer().getFileName();
		if (filename != null && layerView.getLayer().getAttributeTable() == null) {
			int index = filename.lastIndexOf(".");
			if (index == -1) {
				Message.printStatus(1,"GeoViewPropertiesJFrame.actionPerformed",
					"Error trying to parse layer file name (" + filename + ") into the associated .dbf file");
				return;
			}
			String base = filename.substring(0, index + 1);
			String dbfFilename = base + "dbf";
	
			try {
				new DataTable_JFrame(this.__theGeoViewJPanel.getParentJFrame(), "Attributes of " + layerName, dbfFilename);
			}
			catch (Exception e) {
				Message.printWarning(1, "GeoViewLegendJTree.actionPerformed", "Error opening DataTable_JFrame (" + e +").");
				Message.printWarning(2, "GeoViewLegendJTree.actionPerformed",e);
			}
		}
		else {
			GeoLayer layer = layerView.getLayer();
			DataTable table = layer.getAttributeTable();
			try {
				new DataTable_JFrame("Attributes of " + layerName, table);
			}
			catch (Exception e) {
				Message.printWarning(1, "GeoViewLegendJTree.actionPerformed", "Error opening DataTable_JFrame");
				Message.printWarning(2, "GeoViewLegendJTree.actionPerformed",e);
			}
		}
	}	
}

/**
Add an item to the legend.  Make sure the GeoLayerView has already been added to the main GeoView.
Because the number of layer views to add may not initially be known,
the legend panels are removed at each call and are then re-added so
the highest numbered layer view is at the top of the legend panel.
@param layerView GeoLayerView for which to draw the legend panel.
@param count of layers added (1 is first, displayed on bottom).
*/
public void addLayerView(GeoLayerView layerView, int count) {
	String routine = "addLayerView";
	String layerName = layerView.getLegend().getText();

	GeoViewLegendJTree_Node node = null;
	String layerLabel = " " + layerName; // Space helps with presentation.
	if ( !layerView.getLayer().isSourceAvailable() ) {
		// The data for the layer is not available so add a red ! to the front of the label.
		// TODO SAM 2010-12-15 HTML and JEditPane does weird things when the strings need to wrap.
		//layerLabel = "<html><p style=\"color:red\">! " + layerName + "</p></html>";
		layerLabel = "** NO DATA ** " + layerName;
	}
	if (layerView.isAnimated()) {
		node = new GeoViewLegendJTree_Node( layerLabel, layerName, this, __animatedPopup);
	}
	else {
		node = new GeoViewLegendJTree_Node( layerLabel, layerName, this, __layerPopup);
	}

	node.setCheckBoxSelected(layerView.isVisible());

	try {
		if (__projectNode == null) {
			__projectNode =  new GeoViewLegendJTree_Node("GeoView Layers", "Project Node Name", this, __projectNodePopup);
			__projectNode.setCheckBoxSelected(true);
			addNode(__projectNode);
		}
	
		node.setData(layerView);
		
		addNode(node, __projectNode, 0);
		// Now draw the symbol(s)...
		// Get the number of symbols for the layer view.
		// First need to determine the layer view number, which is stored in the "Number" property for the layerview.
		// Currently this is supported only for CLASSIFICATION_SINGLE and CLASSIFICATION_SCALED_SYMBOL.
	
		int nsymbol = layerView.getLegend().size();
		GRSymbol symbol = null;
		GeoLayerViewLegendJComponent[] layerCanvas = null;
		JLabel[] layerClassJLabel = null;
		int y = 0;
		Insets insets_none = new Insets (1, 1, 1, 1);
		for (int isym = 0; isym < nsymbol; isym++) {
			symbol = layerView.getLegend().getSymbol(isym);
			if (symbol.getClassificationType() == GRClassificationType.SINGLE
			|| symbol.getClassificationType() == GRClassificationType.SCALED_TEACUP_SYMBOL) {
				if (isym == 0) {
					// For now assume that symbol types will not be mixed for a layer.
					layerCanvas = new GeoLayerViewLegendJComponent[nsymbol];
				}
				layerCanvas[isym] = new GeoLayerViewLegendJComponent(layerView, isym, 0);
				SimpleJTree_Node symNode = new SimpleJTree_Node((Component)layerCanvas[isym],
					"" + layerName + " symbol #" + isym);
	
				// This data is used when drawing the legend on the map later, so it's important that it be set.
				symNode.setData(layerCanvas[isym]);
				addNode(symNode, node);
			}
			else if (symbol.getClassificationType() == GRClassificationType.SCALED_SYMBOL) {
				// This is currently enabled only for vertical signed
				// bars where the bar is centered vertically on the point,
				// positive values are drawn with the main foreground color and
				// negative values are drawn with the secondary foreground color.
				JPanel panel = new JPanel();
				panel.setBackground(Color.white);
				panel.setLayout(new GridBagLayout());
				if (isym == 0) {
					// For now assume that symbol types will not be mixed for a layer.
					layerCanvas = new GeoLayerViewLegendJComponent[nsymbol];
					layerClassJLabel = new JLabel[nsymbol];
				}
				
				layerCanvas[isym] = new GeoLayerViewLegendJComponent(layerView, isym, 0);
				JGUIUtil.addComponent (panel, layerCanvas[isym],
					1, ++y, 1, 1, 1, 1,
					insets_none, GridBagConstraints.NONE,
					GridBagConstraints.SOUTH);
				if (!symbol.getClassificationField().isEmpty()) {
					// Get the maximum value for the symbol, which is used to scale the symbol.
					// TODO SAM 2009-07-02 - need to streamline this - store with symbol at creation?
					DataTable attribute_table = layerView.getLayer().getAttributeTable();
					int classification_field = -1;
					String cf = symbol.getClassificationField();
					if (attribute_table != null) {
						try {	
							classification_field = attribute_table.getFieldIndex(cf);
						}
						catch (Exception e) {
							// Just won't label below.
							classification_field = -1;
						}
					}
	
					if ((classification_field >= 0)) {
						double symbol_max = ((GRScaledClassificationSymbol)symbol).getClassificationDataDisplayMax();
						// Do this to keep legend a reasonable width.
						if (cf.length() > 20) {
							cf = cf.substring(0,20) + "...";
						}
						layerClassJLabel[isym] = new JLabel( cf + ", Max = " + StringUtil.formatString(symbol_max,"%.3f"));
					}
					else {	
						if (cf.length() > 20) {
							cf = cf.substring(0,20) + "...";
						}
						layerClassJLabel[isym] = new JLabel(cf);
					}
				}
				else {
					// Add a label with the field and maximum value.
					layerClassJLabel[isym] = new JLabel("");
				}
				JGUIUtil.addComponent (panel,
					layerClassJLabel[isym],
					2, y, 1, 1, 1, 0,
					insets_none, GridBagConstraints.HORIZONTAL,
					GridBagConstraints.NORTH);
				SimpleJTree_Node symNode = new SimpleJTree_Node((Component)panel, "" + layerName + " symbol #" + isym);
	
				// The text that is drawn on the legend to label the height of the bar.
				// It is essential that this is set!
				// This information cannot easily be pulled from the tree,
				// so it is just set here so that the legend will be labeled with "Max = XXX.XX", or such.
				layerCanvas[isym].setLegendText( layerClassJLabel[isym].getText());
	
				// This data is used when drawing the legend on the map later, so it's important that it be set.
				symNode.setData(layerCanvas[isym]);
				addNode(symNode, node);	
			}
			else {	
				// Multiple legend items need to be drawn.
				int numclass = symbol.getNumberOfClassifications();
				layerCanvas = new GeoLayerViewLegendJComponent[numclass];
				layerClassJLabel = new JLabel[numclass];
				for (int i = 0; i < numclass; i++) {
					JPanel panel = new JPanel();
					panel.setLayout(new GridBagLayout());
					panel.setBackground(getBackground());
					layerCanvas[i] = new GeoLayerViewLegendJComponent(layerView, isym, i);
					JGUIUtil.addComponent (panel, layerCanvas[i],
						1, ++y, 1, 1, 0, 0,
						insets_none, GridBagConstraints.NONE,
						GridBagConstraints.SOUTH);
					// Add a label for the classification.
					layerClassJLabel[i] = new JLabel(symbol.getClassificationLabel(i));
					layerClassJLabel[i].setBackground(getBackground());
					JGUIUtil.addComponent (panel,
						layerClassJLabel[i],
						2, y, 1, 1, 1, 0,
						insets_none,
						GridBagConstraints.HORIZONTAL,
						GridBagConstraints.SOUTH);
					SimpleJTree_Node symNode = new
						SimpleJTree_Node( (Component)panel, "" + layerName + " symbol #" + isym);
					addNode(symNode, node);
				}
			}
		}	
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error adding layer to display");
		Message.printWarning(2, routine, e);
	}
}

/**
Empties the tree of all nodes and sets the project node to null.
*/
public void emptyTree() {
	try {
		if (__projectNode != null) {
			removeNode("Project Node Name");
			__projectNode = null;
		}
	}
	catch (Exception e) {
		Message.printWarning(1, "GeoViewLegendJTree.emptyTree", "Error emptying tree.");
		Message.printWarning(2, "GeoViewLegendJTree.emptyTree", e);
	}
}

/**
Returns a list of all the nodes that contain layer information (i.e., are GeoViewLegendJTree_Nodes),
but not the project node.
@return a list of the nodes with layer information.
*/
public List<GeoViewLegendJTree_Node> getAllLayerNodes() {
	List<GeoViewLegendJTree_Node> v = new ArrayList<>();

	if (__projectNode != null) {
		if (__projectNode.getChildCount() >= 0) {
			for ( @SuppressWarnings("unchecked")
			Enumeration<GeoViewLegendJTree_Node> e = __projectNode.children();
				e.hasMoreElements();) {
				GeoViewLegendJTree_Node n = (GeoViewLegendJTree_Node)e.nextElement();
				getAllLayerNodes(n, v);
			}
		}		
	}
	return v;
}

/**
Utility method used by getAllLayerNodes()
@param node the node to check to see if to add to the list
@param v the list to which to add the nodes.
*/
private void getAllLayerNodes(GeoViewLegendJTree_Node node, List<GeoViewLegendJTree_Node> v) {
	if (node instanceof GeoViewLegendJTree_Node) {
		v.add(node);
	}
	if (node.getChildCount() >= 0) {
		for (@SuppressWarnings("unchecked")
		Enumeration<SimpleJTree_Node> e = node.children(); e.hasMoreElements();) {
			GeoViewLegendJTree_Node n = (GeoViewLegendJTree_Node)e.nextElement();
			getAllLayerNodes(n, v);
		}
	}	
}

/**
Returns a list of all the nodes that contain layer information (i.e., are GeoViewLegendJTree_Nodes),
but not the project node.
@return a list of the nodes with layer information.
*/
public List<GeoViewLegendJTree_Node> getAllNodes() {
	List<GeoViewLegendJTree_Node> v = new ArrayList<>();

	if (__projectNode == null || __projectNode.getChildCount() <= 0) {
		return v;
	}

	GeoViewLegendJTree_Node node = null;

	for (@SuppressWarnings("unchecked")
	Enumeration<GeoViewLegendJTree_Node> e = __projectNode.children(); e.hasMoreElements();) {
		node = e.nextElement();	
		v.add(node);
		for (@SuppressWarnings("unchecked")
		Enumeration<GeoViewLegendJTree_Node> e2 = node.children(); e2.hasMoreElements();) {
			v.add(e2.nextElement());
		}
	}
	return v;
}

/**
Return the GeoViewPanel associated with the legend panel.
@return the GeoViewPanel associated with the legend panel.
*/
public GeoViewJPanel getGeoViewJPanel() {
	return __theGeoViewJPanel;
}

/**
Returns the names of all the layers in the JTree.
@param visibleOnly whether to only return the names of the visible layers.
@return a list (guaranteed to be non-null) with the names of the specified kind of layers.
*/
@SuppressWarnings("rawtypes")
public List[] getLayersNamesAndNodes(boolean visibleOnly) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	List<GeoLayerView> layers = new ArrayList<>();
	List<String> names = new ArrayList<>();
	List<GeoViewLegendJTree_Node> matchNodes = new ArrayList<>();
	for (int i = 0; i < allNodes.size(); i++) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(i);
		if (((visibleOnly && node.isCheckBoxSelected()) || (!visibleOnly))) {
		   	layers.add((GeoLayerView)node.getData());
			names.add(node.getFieldText());
			matchNodes.add(node);
		}
	}
	List[] lists = { layers, names, matchNodes };
	return lists;
}

/**
Returns the names of all the layers in the JTree.
@param visibleOnly whether to only return the names of the visible layers.
@return a list (guaranteed to be non-null) with the names of the specified kind of layers.
*/
public List<String> getLayerNames(boolean visibleOnly) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	List<String> names = new Vector<String>();
	for (int i = 0; i < allNodes.size(); i++) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(i);
		if (((visibleOnly && node.isCheckBoxSelected()) || (!visibleOnly))) {
			names.add(node.getFieldText());
		}
	}
	return names;
}

/**
Return the number of items in the legend.
@return the number of items in the legend.
*/
public int getNumLegend() {
	if (__projectNode != null) {	
		return getChildCount(__projectNode);
	}
	return 0;
}

/**
Return the list of application layer views that are selected in the legend.
@return a list of application layer types (Strings) that are selected in the legend.
The list can be of zero size.
Currently the list is not sorted and duplicates and blanks may be present.
@param visibleOnly If true only return app layer types that are visible and selected.
If false, return selected app layer types, whether visible or not.
*/
public List<String> getSelectedAppLayerTypes(boolean visibleOnly) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	List<String> appLayerTypes = new ArrayList<>();
	for (int i = 0; i < allNodes.size(); i++) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(i);
		if (((visibleOnly && node.isCheckBoxSelected())
		   || (!visibleOnly)) && node.isTextSelected()) {
			GeoLayerView layerView = (GeoLayerView)node.getData();
			appLayerTypes.add(layerView.getLayer().getAppLayerType());
		}
	}
	return appLayerTypes;
}

/**
Return the list of layer views that are selected in the legend.
@return a list of layer views that are selected in the legend. The list can be of zero size.
@param visibleOnly If true only return layers that are visible and selected.
If false, return selected layers, whether visible or not.
*/
public List<GeoLayerView> getSelectedLayerViews(boolean visibleOnly) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	List <GeoLayerView>appLayerTypes = new ArrayList<>();
	for (int i = 0; i < allNodes.size(); i++) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(i);
		if (((visibleOnly && node.isCheckBoxSelected()) || (!visibleOnly)) && node.isTextSelected()) {
			GeoLayerView layerView = (GeoLayerView)node.getData();
			appLayerTypes.add(layerView);
		}
	}
	return appLayerTypes;
}

/**
Checks to see if the specified layer is visible.
@param num the number of the layer to check.
@return true if the layer is visible, false if not.
*/
public boolean isLayerVisible(int num) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(num);
	if (node.isCheckBoxSelected()) {
		return true;
	}
	return false;
}

/**
Checks to see if the layer with the given name is visible.
@param name the name of the layer to check for visibility.
@return true if the layer is visible, false if not.
*/
public boolean isLayerVisible(String name) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	for (int i = 0; i < allNodes.size(); i++) {
		GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)allNodes.get(i);
		if (node.getFieldText().equals(name) && node.isCheckBoxSelected()) {
			return true;
		}
	}
	return false;
}

/**
Respond to item state changed events.
These happen when the checkbox for a layer is selected or deselected.
@param event the ItemEvent that happened.
*/
public void itemStateChanged(ItemEvent event) {
	boolean select = false;
	if (event.getStateChange() == ItemEvent.SELECTED) {
		select = true;
	}

	// Mark the node as selected, and also select (or deselect) all its immediate children nodes.
	GeoViewLegendJTree_Node node = (GeoViewLegendJTree_Node)event.getItemSelectable();
	if (node instanceof GeoViewLegendJTree_Node) {
		GeoLayerView layerView = (GeoLayerView)node.getData();
		if (layerView != null) {		
			layerView.isVisible(select);
		}
	}	
	@SuppressWarnings("rawtypes")
	Enumeration e = getChildren(node);
	for (; e.hasMoreElements();) {
		SimpleJTree_Node n = (SimpleJTree_Node)e.nextElement();

		if (n instanceof GeoViewLegendJTree_Node) {
			((GeoViewLegendJTree_Node)n).setCheckBoxSelected(select);
			GeoLayerView layerView = (GeoLayerView)n.getData();
			layerView.isVisible(select);
		}
	}

	// Redraw the map according to the new settings.
	boolean waitState = JGUIUtil.getWaitCursor();
	JGUIUtil.setWaitCursor(__theGeoViewJPanel.getParentJFrame(), true);
	__theGeoViewJPanel.setWaitCursorAfterRepaint(waitState);
	repaint();
	__theGeoViewJPanel.refreshAfterSelection();
}

/**
Determines whether the popup menu should be shown based on data in the MouseEvent.
@param e the MouseEvent that happened.
*/
private void maybeShowPopup(MouseEvent e) {
	if ( (__treePopup != null) && __treePopup.isPopupTrigger(e)) {
		__treePopup.show(e.getComponent(), e.getX(), e.getY());
	}
}

/**
Responds to mouse clicked events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseClicked (MouseEvent event) {
}

/**
Responds to mouse dragged events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {
}

/**
Responds to mouse entered events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent event) {
}

/**
Responds to mouse exited events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseExited(MouseEvent event) {
}

/**
Responds to mouse moved events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {
}

/**
Responds to mouse pressed events and sees if the popup should be shown.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	maybeShowPopup(event);
}

/**
Responds to mouse released events and sees if the popup should be shown.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	maybeShowPopup(event);
}

/**
Remove all layer views from the legend.
This should be called when clearing the GeoViewJPanel of all data.
The GeoViewLegendJTree will have no memory of its previous contents.
*/
public void removeAllLayerViews () {
	emptyTree();	
}

/**
Remove an item from the legend.
@param layerView_to_remove GeoLayerView to remove from the legend panel.
*/
public void removeLayerView(GeoLayerView layerView_to_remove) {
	SimpleJTree_Node node = findNodeWithData(layerView_to_remove);
	if (node == null) {
		return;
	}

	try {
		markVisibleNodes();
		removeNode(node);
		resetVisibleNodes();
	}
	catch (Exception e) {
		Message.printWarning(1, "GeoViewLegendJTree.removeLayerView", "Error removing layer view.");
		Message.printWarning(2, "GeoViewLegendJTree.removeLayerView",e);
	}
}

/**
Sets whether a layer should be visible or not.
If the layer is turned off, it will be deselected in the tree and not drawn on the map.
@param layerView the layer to affect.
@param visible whether to make the layer visible or not.
*/
public void setLayerVisible(GeoLayerView layerView, boolean visible) {
	List<GeoViewLegendJTree_Node> allNodes = getAllLayerNodes();
	GeoLayerView layer = null;
	GeoViewLegendJTree_Node node = null;
	for (int i = 0; i < allNodes.size(); i++) {
		node = allNodes.get(i);
		layer = (GeoLayerView)node.getData();
		if (layer == layerView) {
			node.setVisible(visible);
		}
	}
}

/**
Sets the text of the project node (the top-most visible node in the tree).
If a project node doesn't exist (is null), one will be added.
@param name name to set the project node to.
*/
public void setProjectNodeText(String name) {
	try {
		if (__projectNode == null) {
			__projectNode = new GeoViewLegendJTree_Node(name, "Project Node Name", this, __projectNodePopup);
			__projectNode.setCheckBoxSelected(true);
			addNode(__projectNode);
		}
		else {
			__projectNode = new GeoViewLegendJTree_Node(name, "Project Node Name", this, __projectNodePopup);
			__projectNode.setCheckBoxSelected(true);
			replaceNode("Project Node Name", __projectNode);
		}
	}
	catch (Exception e) {
		Message.printWarning(1, "GeoViewLegendJTree.setProjectNodeText", "Error setting project node text.");
		Message.printWarning(2, "GeoViewLegendJTree.setProjectNodeText", e);
	}
}

/**
Updates the geo view JPanel's button state depending on whether any of the layers are selected or not.
*/
public void updateGeoViewJPanelButtons() {
	// Count how many children of the projectNode are selected and if more than 0,
	// tell the GeoViewJPanel to update how the buttons are enabled or not.
	@SuppressWarnings("rawtypes")
	Enumeration e = getChildren(__projectNode);
	for (; e.hasMoreElements();) {
		GeoViewLegendJTree_Node n = (GeoViewLegendJTree_Node)e.nextElement();

		if (n instanceof GeoViewLegendJTree_Node) {
			if (((GeoViewLegendJTree_Node)n).isSelected()) {
				__theGeoViewJPanel.setButtonsEnabledByLayersSelected(true);
				return;
			}
		}
	}	
	__theGeoViewJPanel.setButtonsEnabledByLayersSelected(false);
}

}