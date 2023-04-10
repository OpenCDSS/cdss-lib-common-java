// TSProductLayoutJComponent - handles drawing the TSProduct layout preview in the upper-left-hand corner of the TSProductJFrame

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

package RTi.GRTS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import RTi.GR.GRAspectType;
import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.TS.DayTS;
import RTi.TS.HourTS;
import RTi.TS.IrregularTS;
import RTi.TS.MinuteTS;
import RTi.TS.MonthTS;
import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.YearTS;

import RTi.Util.GUI.DragAndDropControl;
import RTi.Util.GUI.DragAndDropTransferPrimitive;
import RTi.Util.GUI.DragAndDropUtil;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.DragAndDrop;
import RTi.Util.GUI.JComboBoxResponseJDialog;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class handles drawing the TSProduct layout preview in the upper-left-hand corner of the TSProductJFrame.
It also handles all the mouse interaction between the user and the layout preview.
An instance is normally only created when instantiating a TSProductJFrame.
*/
@SuppressWarnings("serial")
public class TSProductLayoutJComponent
extends GRJComponentDevice
implements ActionListener, MouseListener,
DragGestureListener, DragSourceListener, DropTargetListener, DragAndDrop {

/**
Class name.
*/
private final String __CLASS = "TSProductLayoutJComponent";

/**
Constant values for the drawing area.
*/
private final int
	__HEIGHT = 111, // Total height (includes 10 for the lower bar and 1 for the divider line between the two areas).
	__MAX_FONT_SIZE = 70, // Fonts will never be larger than this.
	__MIN_FONT_SIZE = 6, // Fonts will never be smaller than this.
	__WIDTH = 100; // Total width.

/**
More constant values.
*/
private final int
	__BOTTOM_Y = 21, // The bottom Y value.
	__X_OFFSET = 10, // The amount that previews should be offset from the left and right of the drawing area edges.
	__GRAPHS_HEIGHT = __WIDTH - (__X_OFFSET * 2), // The height graph previews should be because it's always a square.
	__GRAPHS_WIDTH = __GRAPHS_HEIGHT;

/**
The font in which text should be drawn on the preview.
*/
private final String __FONT = "Arial";

/**
GUI labels.
*/
private final String
	__MENU_SHOW_ALL_PROPERTIES = "Show all properties",
	__MENU_SHOW_ANNOTATION_PROPERTIES = "Show all annotation properties",
	__MENU_SHOW_DATA_PROPERTIES = "Show all data properties",
	__POPUP_ADD_GRAPH_ABOVE = "Add Graph Above Selected",
	__POPUP_ADD_GRAPH_BELOW = "Add Graph Below Selected",
	__POPUP_ADD_GRAPH_END = "Add Graph at Bottom",
	__POPUP_REMOVE_GRAPH = "Remove Graph",
	__POPUP_MOVE_GRAPH_UP = "Move Graph Up",
	__POPUP_MOVE_GRAPH_DOWN = "Move Graph Down";

/**
Whether the current paint() call is the first time ever.  Used for setting up some initial settings.
*/
private boolean __firstPaint = true;

/**
Data that explain how drag and drop is to be handled for this component.
*/
private DragAndDropControl __dndData;

/**
The drawing area on which the preview will be drawn.
*/
private GRJComponentDrawingArea __da = null;

/**
Menu items for the popup menu.
*/
private JMenuItem
	__addAboveJMenuItem,
	__addBelowJMenuItem,
	__moveUpJMenuItem,
	__moveDownJMenuItem,
	__removeJMenuItem;

/**
The menu that appears when the layout is right-clicked on.
*/
private JPopupMenu __popup;

/**
The drawing area bounds.
*/
private Rectangle _bounds = null;

/**
The product for which the graph previews are being drawn.
*/
private TSProduct __product = null;

/**
The frame on which this preview is located.
*/
private TSProductJFrame __parent = null;

/**
Constructor.
@param parent the parent TSProductJFrame on which this appears.
@param product the product being represented.
*/
public TSProductLayoutJComponent(TSProductJFrame parent, TSProduct product) {
	super("TSProductLayoutJComponent");

	__parent = parent;
	__product = product;
	// Create a new DragAndDropControl object that specifies how drag and drop will behave in this component.
	// In this case, the data object is being set up to not allow dragging (ACTION_NONE)
	// and to accept copy or move drops (ACTION_COPY_OR_MOVE).
	__dndData = new DragAndDropControl(DragAndDropUtil.ACTION_NONE, DragAndDropUtil.ACTION_COPY_OR_MOVE);

	// This should always be true, but in the future it may be that dropping data is disabled for some instances of the component.
	if (__dndData.allowsDrop()) {
		// Sets this component as a drop target for drag and drop operations,
		// and specifies that it should allow drops from either copy or move actions.
		__dndData.setDropTarget( DragAndDropUtil.createDropTarget(this, DragAndDropUtil.ACTION_COPY_OR_MOVE, this));
	}

	GRLimits limits = new GRLimits(0, 0, __WIDTH, __HEIGHT);
	__da = new GRJComponentDrawingArea(this, __CLASS + ".DA",
		GRAspectType.FILL, limits, GRUnits.DEVICE,
		GRLimits.DEVICE, limits);
	setLimits(limits);

	buildPopup();

	repaint();
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	// Commit any changes to the product prior to doing anything.
	__parent.updateTSProduct();

	if (command.equals(__POPUP_ADD_GRAPH_ABOVE)
	    || command.equals(__POPUP_ADD_GRAPH_BELOW)
	    || command.equals(__POPUP_ADD_GRAPH_END)) {
		int selectedGraph = __parent.getSelectedGraph();

		if (command.equals(__POPUP_ADD_GRAPH_ABOVE)) {
		}
		else if (command.equals(__POPUP_ADD_GRAPH_BELOW)) {
			selectedGraph++;
		}
		else {
		    // __POPUP_ADD_GRAPH_END
			selectedGraph = __parent.getGraphList().size();
		}
		if (!__parent.areGraphsDefined()) {
			selectedGraph = -1;
		}

		__parent.addGraph(selectedGraph);
	}
	else if (command.equals(__POPUP_REMOVE_GRAPH)) {
		removeGraphClicked();
	}
	else if (command.equals(__POPUP_MOVE_GRAPH_UP)) {
		moveGraphUpClicked();
	}
	else if (command.equals(__POPUP_MOVE_GRAPH_DOWN)) {
		moveGraphDownClicked();
	}
	else if (command.equals(__MENU_SHOW_ALL_PROPERTIES)) {
		__product.showProps(2);
		return;
	}
	else if (command.equals(__MENU_SHOW_ANNOTATION_PROPERTIES)) {
		__product.showPropsStartingWith(2, "Annotation");
	}
	else if (command.equals(__MENU_SHOW_DATA_PROPERTIES)) {
		__product.showPropsStartingWith(2, "Data");
		return;
	}

	// Do this any time graphs are changed.
	__parent.getTSViewJFrame().getViewGraphJFrame().getMainJComponent().reinitializeGraphs(__product);
	if (__parent.getTSViewJFrame().getViewGraphJFrame().getReferenceGraph() != null) {
		__parent.getTSViewJFrame().getViewGraphJFrame().getReferenceGraph().reinitializeGraphs(__product);
	}

	repaint();
	__parent.checkGUIState();
}

/**
Builds the list of graphs that are 'down' from the selected graph, for use in the 'move graph' dialog box.
@param selectedGraph the graph to be moved down.
@return a list of the graphs that are 'down' from the selected graph.
*/
private List<String> buildDownList(int selectedGraph) {
	List<String> v = new Vector<String>();
	List<String> graphs = __parent.getGraphList();
	String s = null;
	int count = 1;
	String plural = "";
	for (int i = selectedGraph + 1; i < graphs.size(); i++) {
		s = StringUtil.getToken(graphs.get(i), "-", 0, 0);
		s = s.trim();
		v.add("" + count + " step" + plural + ", below graph #" + (i + 1) + " (\"" + s + "\")");
		if (count == 1) {
			plural = "s";
		}
		count++;
	}
	return v;
}

/**
Builds the popup menu.
*/
private void buildPopup() {
	__popup = new JPopupMenu();
	__addAboveJMenuItem = new JMenuItem(__POPUP_ADD_GRAPH_ABOVE);
	__addAboveJMenuItem.addActionListener(this);
	__popup.add(__addAboveJMenuItem);
	__addBelowJMenuItem = new JMenuItem(__POPUP_ADD_GRAPH_BELOW);
	__addBelowJMenuItem.addActionListener(this);
	__popup.add(__addBelowJMenuItem);
	JMenuItem mi = new JMenuItem(__POPUP_ADD_GRAPH_END);
	mi.addActionListener(this);
	__popup.add(mi);
	__removeJMenuItem = new JMenuItem(__POPUP_REMOVE_GRAPH);
	__removeJMenuItem.addActionListener(this);
	__popup.add(__removeJMenuItem);
	__popup.addSeparator();
	__moveUpJMenuItem = new JMenuItem(__POPUP_MOVE_GRAPH_UP);
	__moveUpJMenuItem.addActionListener(this);
	__popup.add(__moveUpJMenuItem);
	__moveDownJMenuItem = new JMenuItem(__POPUP_MOVE_GRAPH_DOWN);
	__moveDownJMenuItem.addActionListener(this);
	__popup.add(__moveDownJMenuItem);
	if (IOUtil.testing()) {
		__popup.addSeparator();
		mi = new JMenuItem(__MENU_SHOW_ALL_PROPERTIES);
		mi.addActionListener(this);
		__popup.add(mi);
		mi = new JMenuItem(__MENU_SHOW_DATA_PROPERTIES);
		mi.addActionListener(this);
		__popup.add(mi);
		mi = new JMenuItem(__MENU_SHOW_ANNOTATION_PROPERTIES);
		mi.addActionListener(this);
		__popup.add(mi);
	}
}

/**
Builds the list of graphs that are 'up' from the selected graph, for use in the 'move graph' dialog box.
@param selectedGraph the graph to be moved up.
@return a list of the graphs that are 'up' from the selected graph.
*/
private List<String> buildUpList(int selectedGraph) {
	List<String> v = new Vector<>();
	List<String> graphs = __parent.getGraphList();
	String s = null;
	int count = 1;
	String plural = "";
	for (int i = selectedGraph - 1; i >= 0; i--) {
		s = StringUtil.getToken(graphs.get(i), "-", 0, 0);
		s = s.trim();
		v.add("" + count + " step" + plural + ", above graph #" + (i + 1) + " ('" + s + "')");
		if (count == 1) {
			plural = "s";
		}
		count++;
	}
	return v;
}

/**
Calculates the proper font size to be drawn inside a graph preview.
@param graphInsideHeight the height inside the graph in which the text can be drawn.
Typically it is 4 less than graph height, to account for the 1 pixel border on top
and bottom and then 1 pixel of space between the border and the text.
// REVISIT (JTS - 2004-04-27) probably move to a utility function in GR.
@param fontSize the desired fontSize in points.
@param text text to size -- usually the graph number.
@return the font size to draw in so that text fits best, in points.
Returned as an integer because java fonts only accept integer-based font point sizes.
*/
private int calculateFontSize(int graphInsideHeight, int fontSize, String text){
	boolean done = false;
	int height = -1;
	int smaller = -1;
	int bigger = -1;

	while (!done) {
		if (fontSize >= __MAX_FONT_SIZE) {
			return __MAX_FONT_SIZE;
		}
		if (fontSize <= __MIN_FONT_SIZE) {
			return __MIN_FONT_SIZE;
		}

		height = calculateTextHeight(fontSize, text);
		if (height >= __MAX_FONT_SIZE) {
			return __MAX_FONT_SIZE;
		}
		if (height <= __MIN_FONT_SIZE) {
			return __MIN_FONT_SIZE;
		}

		if (height < graphInsideHeight) {
			bigger = calculateTextHeight(fontSize + 1, text);
			if (bigger > graphInsideHeight) {
				return fontSize;
			}
			else if (bigger == graphInsideHeight) {
				return fontSize + 1;
			}
			else {
				fontSize++;
			}
		}
		else if (height == graphInsideHeight) {
			return height;
		}
		else {
			// height > graphInsideHeight
			smaller = calculateTextHeight(fontSize - 1, text);

			if (smaller > graphInsideHeight) {
				fontSize--;
			}
			else if (smaller == graphInsideHeight) {
				return fontSize - 1;
			}
			else {
				return fontSize - 1;
			}
		}
	}
	return -1;
}

/**
Calculates the height of the given text in the given font size in pixels.
@param fontSize the size of the font in points.
@param text the text to draw.
@return the height of the given text in the given font size in pixels.
*/
private int calculateTextHeight(int fontSize, String text) {
	GRDrawingAreaUtil.setFont(__da, __FONT, fontSize);
	GRLimits limits = GRDrawingAreaUtil.getTextExtents(__da, text, GRUnits.DEVICE);
	return (int)limits.getHeight();
}

////////////////////////////////////////////////////////////////
// DragAndDrop methods
/**
Called when a drag is about to start.
*/
public boolean dragAboutToStart() {
	return true;
}

/**
Called when a drag is started.
*/
public void dragStarted() {
}

/**
Called when a drag is successful.
*/
public void dragSuccessful(int action) {
}

/**
Called when a drag is unsuccessful.
*/
public void dragUnsuccessful(int action) {
}

/**
Called when data are over this component and can be dropped.
*/
public void dropAllowed() {
}

/**
Called when data are dragged outside of this component's area.
*/
public void dropExited() {
}

/**
Called when data are over this component and cannot be dropped.
*/
public void dropNotAllowed() {
}

/**
Called when a drop was completed successfully.
*/
public void dropSuccessful() {
}

/**
Called when a drop was not completed successfully.
*/
public void dropUnsuccessful() {
}

/**
Does nothing.
*/
public void setAlternateTransferable(Transferable t) {
}

////////////////////////////////////////////////////////////////
// Drag Gesture events
/**
Called when a drag gesture was recognized and a drag can start.
*/
public void dragGestureRecognized(DragGestureEvent dge) {
}

////////////////////////////////////////////////////////////////
// Drag events
/**
Called when the drag ends.
*/
public void dragDropEnd(DragSourceDropEvent dsde) {
}

/**
Called when a drag entered a component's area.
*/
public void dragEnter(DragSourceDragEvent dsde) {
	// REVISIT (JTS - 2004-04-28) change the cursor depending on whether over a graph or not.
}

/**
Called when a drag exits a component's area.
*/
public void dragExit(DragSourceEvent dse) {
}

/**
Called when dragged data is over a component.
*/
public void dragOver(DragSourceDragEvent dsde) {
}

/**
Called if the drop action changes for the drop component.
*/
public void dropActionChanged(DragSourceDragEvent dsde) {
}

////////////////////////////////////////////////////////////////
// Drop events
/**
Called when the action for a drop changes.
*/
public void dropActionChanged(DropTargetDragEvent dtde) {
	DragAndDropUtil.dropActionChanged(this, dtde);
}

/**
Called when a drag enters this component's area.
*/
public void dragEnter(DropTargetDragEvent dtde) {
	DragAndDropUtil.dragEnter(this, dtde);
}

/**
Called when a drag exits this components's area.
*/
public void dragExit(DropTargetEvent dte) {
	DragAndDropUtil.dragExit(this, dte);
}

/**
Called when a drag is over this component.
*/
public void dragOver(DropTargetDragEvent dtde) {
	DragAndDropUtil.dragOver(this, dtde);
}

/**
Called when a drop occurs.
*/
public void drop(DropTargetDropEvent dtde) {
	DragAndDropUtil.drop(this, dtde);
}

/**
Determines the graph that was clicked on.
@return the number of the graph that was clicked on, or -1 if no graph was clicked on.
*/
private int findClickedGraph(int x, int y) {
	int numGraphs = __product.getNumSubProducts();

	if (numGraphs == 0) {
		return -1;
	}

	// Change y to accommodate RTi's inverted Y style as compared to the Y value received by a mouse click
	// (in which the origin is at the upper-left).
	y = invertY(y);

	if (x < __X_OFFSET || x > (__X_OFFSET + __GRAPHS_WIDTH)) {
		// Outside the bounds of any graph.
		return -1;
	}
	if (y < __BOTTOM_Y || y > (__BOTTOM_Y + __GRAPHS_HEIGHT)) {
		// Above or below any graph.
		return -1;
	}

	int bottomY = __BOTTOM_Y;
	int graphHeight = (int)(__GRAPHS_HEIGHT / numGraphs);
	int topY = bottomY + graphHeight;
	for (int i = 0; i < numGraphs; i++) {
		if (i > 0) {
			bottomY += graphHeight;
			topY += graphHeight;
		}
		if (y < topY && y > bottomY) {
			return (numGraphs - i - 1);
		}
	}
	return -1;
}

/**
Gets the number of positions that a graph should be moved as selected from the move graph dialog box.
@param s the String to get the amount of change from.
@return the number of steps of change.
*/
private int getChangeAmount(String s) {
	// Given a string like "1 step", "2 steps", etc.
	int index = s.indexOf("step");
	s = s.substring(0, index);
	s = s.trim();
	try {
		Integer I = new Integer(s);
		return I.intValue();
	}
	catch (Exception e) {
		return -1;
	}
}

/**
Returns the valid data flavors for data that can be dropped on this TSProductLayoutJComponent.
Recognizes most Time Series and also string and text flavors.
If a String or text was dropped on this, it assumes it to be a local transfer of a time
series from one graph to another and will try to do so.
@return the valid data flavors for data that can be dropped on this.
For more information on data flavors, check each time series.
*/
public DataFlavor[] getDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[9];
	flavors[0] = MinuteTS.minuteTSFlavor;
	flavors[1] = HourTS.hourTSFlavor;
	flavors[2] = DayTS.dayTSFlavor;
	flavors[3] = MonthTS.monthTSFlavor;
	flavors[4] = YearTS.yearTSFlavor;
	flavors[5] = IrregularTS.irregularTSFlavor;

	// The following allow a TSID to be dropped.
	flavors[6] = TSIdent.tsIdentFlavor;
	flavors[7] = DragAndDropTransferPrimitive.stringFlavor;
	flavors[8] = DragAndDropTransferPrimitive.textFlavor;
	return flavors;
}

/**
Returns this component's DragAndDropControl object.
@return this component's DragAndDropControl object.
*/
public DragAndDropControl getDragAndDropControl() {
	return __dndData;
}

/**
Returns null.  This component does not support dragging.
*/
public Transferable getTransferable() {
	return null;
}

/**
Handles data dropped on this component.
@param o the data that was dropped.
@param p the Point at which data was dropped.
@return true if the data was handled successfully, false if not.
*/
public boolean handleDropData(Object o, Point p) {
	if (!__parent.areGraphsDefined()) {
		return false;
	}

	String id = null;
	List<TS> v = null;

	if (o instanceof TS) {
		if (o instanceof YearTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Dropping Year time series");
			}
		}
		else if (o instanceof MonthTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Dropping Month time series");
			}
		}
		else if (o instanceof DayTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Dropping Day time series");
			}
		}
		else if (o instanceof HourTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Dropping Hour time series");
			}
		}
		else if (o instanceof MinuteTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1,"", "Dropping Minute time series");
			}
		}
		else if (o instanceof IrregularTS) {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Dropping Irregular time series");
			}
		}
		else {
			if (IOUtil.testing()) {
				Message.printStatus(1, "", "Unknown time series: " + o);
			}
		}

		TS ts = (TS)o;
		id = ts.getIdentifier().toString();
		int x = p.x;
		int graph = findClickedGraph(x, p.y);

		__parent.addData(graph, id);
		v = __product.getTSList();
		v.add(ts);
		__product.setTSList(v);
		__parent.getTSViewJFrame().getViewGraphJFrame().getMainJComponent().reinitializeGraphs(__product);
		if (__parent.getTSViewJFrame().getViewGraphJFrame().getReferenceGraph() != null) {
			__parent.getTSViewJFrame().getViewGraphJFrame().getReferenceGraph().reinitializeGraphs(__product);
		}
		return true;
	}
	else if (o instanceof DragAndDropTransferPrimitive) {
		DragAndDropTransferPrimitive d =(DragAndDropTransferPrimitive)o;
		String s = (String)d.getData();

		int x = p.x;
		int graph = findClickedGraph(x, p.y);

		int selectedGraph = __parent.getSelectedGraph();

		int index = s.indexOf("-");
		String tsString = s.substring(0, index).trim();
		int tsNum = StringUtil.atoi(tsString) - 1;

		if (graph != -1 && graph != selectedGraph) {
			__parent.moveSelectedData(selectedGraph, graph, tsNum);
			return true;
		}
		else {
			return false;
		}
	}
	else {
		Message.printStatus(1, "", "Unknown drop: " + o + " (" + o.getClass() + ")");
		return false;
	}
}

// REVISIT (JTS - 2004-04-27) move into some GR util?
/**
Takes a Y value returned from the Java-based coordinate system and converts to RTi's coordinate system.
@return the RTi-based Y coordinate.
*/
public int invertY(int y) {
	return (int)(_devy2 - y);
}

/**
Shows a popup menu if the layout was right-clicked on in a graph area.
@param even the MouseEvent that happened.
*/
private void showPopup(MouseEvent event) {
	if (event.getButton() != MouseEvent.BUTTON1) {
		int selectedGraph = __parent.getSelectedGraph();
		int numGraphs = __product.getNumSubProducts();
		if (numGraphs == 0) {
			__moveUpJMenuItem.setEnabled(false);
			__moveDownJMenuItem.setEnabled(false);
			__removeJMenuItem.setEnabled(false);
			__addAboveJMenuItem.setEnabled(false);
			__addBelowJMenuItem.setEnabled(false);
		}
		else {
			__removeJMenuItem.setEnabled(true);
			__addAboveJMenuItem.setEnabled(true);
			__addBelowJMenuItem.setEnabled(true);
			if (selectedGraph == 0) {
				__moveUpJMenuItem.setEnabled(false);
			}
			else {
				__moveUpJMenuItem.setEnabled(true);
			}
			if (selectedGraph == (numGraphs - 1)) {
				__moveDownJMenuItem.setEnabled(false);
			}
			else {
				__moveDownJMenuItem.setEnabled(true);
			}
		}
		__popup.show(event.getComponent(), event.getX(), event.getY());
	}
}

/**
Responds to mouse clicked events.  Selects a graph if one is clicked on.
@param event the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent event) {
	if (event.getButton() != MouseEvent.BUTTON1) {
		return;
	}
	int x = event.getX();
	int y = event.getY();

	int graphNum = findClickedGraph(x, y);

	if (graphNum == -1) {
		return;
	}

	__parent.setSelectedGraph(graphNum);
}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
Possibly shows a popup menu if the right mouse button was pressed.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	showPopup(event);
}

/**
Does nothing.
*/
public void mouseReleased(MouseEvent event) {}

/**
Responds when the "Move Graph Down" menu item is clicked by opening a dialog to determine how far to move the graph.
*/
private void moveGraphDownClicked() {
	int selectedGraph = __parent.getSelectedGraph();
	List<String> v = buildDownList(selectedGraph);
	if (v.size() == 1) {
		__product.swapSubProducts(selectedGraph, selectedGraph + 1);
		__parent.redisplayProperties();
		__parent.setSelectedGraph(selectedGraph + 1);
	}
	else {
		String s = (new JComboBoxResponseJDialog(__parent,
			"Move Graph Down",
			"Move graph down: ", v,
		ResponseJDialog.OK | ResponseJDialog.CANCEL)).response();
		if (s == null) {
			return;
		}
		int change = getChangeAmount(s);
		if (change == -1) {
			return;
		}
		for (int i = 0; i < change; i++) {
			__product.swapSubProducts(selectedGraph, selectedGraph + 1);
			selectedGraph++;
//			Message.printStatus(1, "", "Swapped graphs "
//				+ selectedGraph + " and " + (selectedGraph + 1)
//				+ ".  Selected graph now: " + selectedGraph);
		}
		__parent.redisplayProperties();
		__parent.setSelectedGraph(selectedGraph);
	}
}

/**
Responds when the "Move Graph Up" menu item is clicked by opening a dialog to determine how far to move the graph.
*/
private void moveGraphUpClicked() {
	int selectedGraph = __parent.getSelectedGraph();
	List<String> v = buildUpList(selectedGraph);
	if (v.size() == 1) {
		__product.swapSubProducts(selectedGraph, selectedGraph - 1);
		__parent.redisplayProperties();
		__parent.setSelectedGraph(selectedGraph - 1);
	}
	else {
		String s = (new JComboBoxResponseJDialog(__parent,
			"Move Graph Up",
			"Move graph up: ", v,
		ResponseJDialog.OK | ResponseJDialog.CANCEL)).response();
		if (s == null) {
			return;
		}
		int change = getChangeAmount(s);
		if (change == -1) {
			return;
		}
		for (int i = 0; i < change; i++) {
			__product.swapSubProducts(selectedGraph, selectedGraph - 1);
			selectedGraph--;
//			Message.printStatus(1, "", "Swapped graphs "
//				+ selectedGraph + " and " + (selectedGraph - 1)
//				+ ".  Selected graph now: " + selectedGraph);
		}
		__parent.redisplayProperties();
		__parent.setSelectedGraph(selectedGraph);
	}
}

/**
Responds when the remove graph menu item is selected by opening a dialog to make sure the graph should really be removed.
*/
private void removeGraphClicked() {
	if ((new ResponseJDialog(__parent, "Remove Graph", "Are you sure you want to remove this graph?",
		ResponseJDialog.YES | ResponseJDialog.NO)).response() == ResponseJDialog.NO) {
		return;
	}
	int selectedGraph = __parent.getSelectedGraph();
	int origPos = selectedGraph + 1;
	List<String> graphs = __parent.getGraphList();
	int count = graphs.size();
	int shifts = count - (selectedGraph + 1);
	for (int i = 0; i < shifts; i++) {
		__product.swapSubProducts(selectedGraph, selectedGraph + 1);
		selectedGraph++;
	}

	__product.removeSubProduct(selectedGraph);

	count--;
	if (count == 0) {
		__parent.setSelectedSubProductAndData(-1, 0);
		selectedGraph = -1;
	}
	else if (count == 1) {
		__parent.setSelectedSubProductAndData(0, 0);
		selectedGraph = 0;
	}
	else {
		if (origPos > count) {
			__parent.setSelectedSubProductAndData(origPos - 2, 0);
			selectedGraph = (origPos - 2);
		}
		else {
			__parent.setSelectedSubProductAndData(origPos - 1, 0);
			selectedGraph = (origPos - 1);
		}
	}
	//	__product.dumpProps();
	__parent.redisplayProperties();
	if (selectedGraph > -1) {
		__parent.setSelectedGraph(selectedGraph);
	}
}

/**
Paints the display, showing a rectangle for every graph in the TSProduct with
the number of the graph in the center of the graph.
The currently-selected graph is shaded grey.
@param g the Graphics context on which to paint.
*/
public void paint(Graphics g) {
	setGraphics(g);
	_bounds = getBounds();

	if (__firstPaint) {
		// If double buffering, create a new image.
		if ((_buffer == null) || _double_buffering) {
			setupDoubleBuffer(0, 0, _bounds.width, _bounds.height);
		}
		__firstPaint = false;
	}

	_graphics.setColor(Color.white);
	_bounds = getBounds();
	_graphics.fillRect(0, 0, _bounds.width, _bounds.height);

	// Separate main graphs from reference graph.
	GRDrawingAreaUtil.setColor(__da, GRColor.black);
	GRDrawingAreaUtil.drawLine(__da, 0, 10, __WIDTH, 10);

	int numGraphs = __product.getNumSubProducts();

	if (numGraphs == 0) {
		showDoubleBuffer(g);
		return;
	}

	int graphHeight = (int)(__GRAPHS_HEIGHT / numGraphs);
	int y = __BOTTOM_Y;

	GRDrawingAreaUtil.setColor(__da, GRColor.black);

	String num = "";
	int fontSize = 0;
	int textPos = ((int)(__WIDTH / 2));
	int selectedGraph = __parent.getSelectedGraph();

	// Draw the rectangle for each graph with the number of the graph in the center.
	for (int i = numGraphs - 1; i >= 0; i--) {
		num = "" + (i + 1);
		fontSize = calculateFontSize(graphHeight - 4, graphHeight - 4, num);
		GRDrawingAreaUtil.setFont(__da, __FONT, fontSize);

		if (i == selectedGraph) {
			GRDrawingAreaUtil.setColor(__da, GRColor.grey50);
			GRDrawingAreaUtil.fillRectangle(__da, __X_OFFSET, y, __GRAPHS_WIDTH, graphHeight);
			GRDrawingAreaUtil.setColor(__da, GRColor.black);
		}

		GRDrawingAreaUtil.drawRectangle(__da, __X_OFFSET, y, __GRAPHS_WIDTH, graphHeight);
		GRDrawingAreaUtil.drawText(__da, "" + (i + 1), textPos,
			(y + graphHeight), 0,
			GRText.CENTER_X | GRText.TOP);
		y += graphHeight;
	}

	showDoubleBuffer(g);
}

}