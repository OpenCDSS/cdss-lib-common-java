// ERDiagram_Device - the graphical device that controls the drawing of the ER Diagram.

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

// ----------------------------------------------------------------------------
// ERDiagram_Device - the device that controls all the drawing of the
//	ER Diagram.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-27	J. Thomas Sapienza, RTi	* Initial changelog.  
//					* Javadocs brought up to date.
//					* Alphabetized functions, variables, etc
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;

import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRLimits;
import RTi.GR.GRText;
import RTi.GR.GRUnits;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SaveImageGUI;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.PrintUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class extends GRJComponentDevice to be the device that manages the ER Diagram drawing area.
*/
@SuppressWarnings("serial")
public class ERDiagram_Device extends GRJComponentDevice
implements ActionListener, MouseListener, MouseMotionListener, Printable, 
Scrollable {

/**
Labels for menu items.
*/
private final String 
	__MENU_COLOR_CODE_RELATIONSHIPS = "Color Code Relationships",
	__MENU_DISPLAY_TABLES_TABLE = "Display Tables Table",
	__MENU_DUMP_MARKED_TABLES = "Dump Information about Selected Tables",
	__MENU_EXIT = "Exit",
	__MENU_FIND = "Find Table",
	__MENU_FRAMEWORK = "Show Table Framework",
	__MENU_HIDE_SELECTED = "Hide Selected Tables",
	__MENU_INCH_GRID = "Show Half-Inch Grid",
	__MENU_MARGIN = "Show Margins",
	__MENU_PIXEL_GRID = "Show 50 Pixel Grid",
	__MENU_PRINT = "Print",
	__MENU_REFRESH = "Refresh from database",
	__MENU_SAVE = "Save Changes to Database",
	__MENU_SAVE_AS_IMAGE = "Save as Image",
	__MENU_TEXT = "Show Text",
	__MENU_TITLE_ONLY = "Show Table Name Only";
		
/**
Whether to color code relationships to be able to tell which drawing routine was used for each.
*/
protected boolean __colorCodeRelationships = false;
		
/**
Whether to draw tables as frameworks or not.
*/
protected boolean __drawFramework = false;

/**
Whether to draw the grid or not.
*/
private boolean __drawInchGrid = false;

/**
Whether to draw the printable area margin or not.
*/
private boolean __drawMargin = false;

/**
Whether to draw a 50-pixel grid or not.
*/
private boolean __drawPixelGrid = false;

/**
Whether to draw the table text or not.
*/
private boolean __drawText = true;

/**
Whether to erase the currently-selected table when the screen is redrawn, by not drawing it at all.
*/
private boolean __eraseTable = false;

/**
Whether to force paint() to refresh the entire drawing area or not.
*/
private boolean __forceRefresh = true;

/**
Whether the mouse is currently being dragged on the screen.
*/
private boolean __inDrag = false;

/**
Whether drawing settings need to be initialized because it is the first time paint() has been called.
*/
private boolean __initialize = true;

/**
Used to specify painting commands that will occur only once ever for the entire life of the class.
*/
private boolean __onceEver = true;

/**
Used to specify whether to draw only the table name, or the table name and table fields
*/
private boolean __titleOnly = false;

/**
The printing scale factor of the drawing.  This is the amount by which the
72 dpi printable pixels are scaled.  A printing scale value of 1 means that
the ER diagram will be printed at 72 pixels per inch (ppi), which is the 
java standard.   A scale factor of .5 means that the ER Diagram will be 
printed at 144 ppi.  A scale factor of 3 means that the ER Diagram will be printed at 24 ppi.
*/
private double __printScale = 1;

/**
The actual scaling factor of the drawing, as determined by a combo box at the
top of the frame containing the device.  A display scale of 1 means that no 
scaling is done.  A value of 2 means that a 100-pixel wide rectangle will 
be drawn as 200 pixels wide.  A value of .4 means that a 100-pixel wide 
rectangle wil be drawn as 40 pixels wide.
*/
private double __drawingScale = 1;

/**
The height of the drawing area, in pixels.
*/
private double __height = 0;

/**
The width of the drawing area, in pixels.
*/
private double __width = 0;

/**
The X location of the last mouse press.
*/
private double __x;

/**
The distance from the last mouse press (if it was on a table) to the X-position
of the table (its lower-left corner).
*/
private double __xAdjust;

/**
THe Y location of the last mouse press.
*/
private double __y;

/**
The distance from the last mouse press (if it was on a table) to the Y-position
of the table (its lower-left corner).
*/
private double __yAdjust;

/**
The drawing area on which the ER Diagram is drawn.
*/
private ERDiagram_DrawingArea __drawingArea;

/**
The dialog box used for searching the table for a specific table.
*/
private ERDiagram_FindDialog __findDialog;

/**
The parent panel on which this device and its drawing area appears.
*/
private ERDiagram_JPanel __parent;

/**
Array of all the table relationships in the ER diagram.
*/
private ERDiagram_Relationship[] __rels;

/**
Array of all the tables in the ER diagram.
*/
private ERDiagram_Table[] __tables;

/**
Limits of the currently-selected table.
*/
private GRLimits __tableLimits = null;

/**
The currently-selected element of the tables array.
*/
private int __table = -1;

/**
The popup menu that appears when the pane is right-clicked on.
*/
private JPopupMenu __popup;

/**
Reference to the SimpleJComboBox the main JFrame uses to specify the drawing
scale of the ER Diagram.
*/
private SimpleJComboBox __scaleComboBox;

/**
Constructor.  Builds the device for the specified tables and relationships with the specified scale.
@param tables array of ERDiagram_Table objects, each of which is a table in the ER Diagram.
@param rels array of ERDiagram_Relationship objects, each of which is a 
relationship between tables in the ER Diagram.
@param parent the panel in which this device is used.
@param scale the scaling factor for drawing.
*/
public ERDiagram_Device(ERDiagram_Table[] tables, ERDiagram_Relationship[] rels,
ERDiagram_JPanel parent, double scale) {
	super("ERDiagram_Device");

	addMouseListener(this);
	addMouseMotionListener(this);

	__parent = parent;
	__tables = tables;
	__rels = rels;
	__printScale = scale;

	// build the popup menu
	__popup = new JPopupMenu();
	JMenuItem mi = new JMenuItem(__MENU_PRINT);
	mi.addActionListener(this);
	__popup.add(mi);
	mi = new JMenuItem(__MENU_SAVE_AS_IMAGE);
	mi.addActionListener(this);
	__popup.add(mi);
	mi = new JMenuItem(__MENU_SAVE);
	mi.addActionListener(this);
	__popup.add(mi);
	mi = new JMenuItem(__MENU_REFRESH);
	mi.addActionListener(this);
	__popup.add(mi);
	__popup.addSeparator();
	mi = new JMenuItem(__MENU_FIND);
	mi.addActionListener(this);
	__popup.add(mi);	
	JCheckBoxMenuItem jcbmi;
	__popup.addSeparator();		
	jcbmi = new JCheckBoxMenuItem(__MENU_MARGIN);
	jcbmi.addActionListener(this);
	__popup.add(jcbmi);
	jcbmi = new JCheckBoxMenuItem(__MENU_TEXT, true);
	jcbmi.addActionListener(this);
	__popup.add(jcbmi);

	if (__parent.isDebug()) {
		jcbmi = new JCheckBoxMenuItem(__MENU_TITLE_ONLY, false);
		jcbmi.addActionListener(this);
		__popup.add(jcbmi);	
		mi = new JMenuItem(__MENU_HIDE_SELECTED);
		mi.addActionListener(this);
		__popup.add(mi);		
		jcbmi = new JCheckBoxMenuItem(__MENU_INCH_GRID);
		jcbmi.addActionListener(this);
		__popup.add(jcbmi);
		jcbmi = new JCheckBoxMenuItem(__MENU_PIXEL_GRID);
		jcbmi.addActionListener(this);
		__popup.add(jcbmi);
		jcbmi = new JCheckBoxMenuItem(__MENU_FRAMEWORK);
		jcbmi.addActionListener(this);		
		__popup.add(jcbmi);
		jcbmi = new JCheckBoxMenuItem(__MENU_COLOR_CODE_RELATIONSHIPS);
		jcbmi.addActionListener(this);
		__popup.add(jcbmi);
		__popup.addSeparator();
		mi = new JMenuItem(__MENU_DUMP_MARKED_TABLES);
		mi.addActionListener(this);
		__popup.add(mi);
		mi = new JMenuItem(__MENU_DISPLAY_TABLES_TABLE);
		mi.addActionListener(this);
		__popup.add(mi);
		__popup.addSeparator();
		mi = new JMenuItem(__MENU_EXIT);
		mi.addActionListener(this);
		__popup.add(mi);
	}
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	String routine = "ERDiagram_Device.actionPerformed";
	String action = event.getActionCommand();

	if (action.equals(__MENU_COLOR_CODE_RELATIONSHIPS)) {
		if (__colorCodeRelationships) {
			__colorCodeRelationships = false;
			for (int i = 0; i < __rels.length; i++) {
				__rels[i].setColorCodeRelationships(false);
			}
		}
		else {
			__colorCodeRelationships = true;
			for (int i = 0; i < __rels.length; i++) {
				__rels[i].setColorCodeRelationships(true);
			}
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_DISPLAY_TABLES_TABLE)) {
		List<ERDiagram_Table> v = new Vector<ERDiagram_Table>();
		for (int i = 0; i < __tables.length; i++) {
			v.add(__tables[i]);
		}
		new ERDiagram_Table_JFrame(v);
	}
	else if (action.equals(__MENU_DUMP_MARKED_TABLES)) {
		Message.printStatus(2, routine, "\nDumping information about "
			+ "marked tables ...");
		Message.printStatus(2, routine, "----------------------------");
		for (int i = 0; i < __tables.length; i++) {
			if (__tables[i].isMarked()) {
				Message.printStatus(2, routine, ""+__tables[i]);
			}
		}
		Message.printStatus(2, routine, "----------------------------");
	}
	else if (action.equals(__MENU_EXIT)) {
		__parent.closeWindow();
		try {
			__parent.getDMI().close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}	
	else if (action.equals(__MENU_FIND)) {
		if (__findDialog == null) {
			__findDialog = new ERDiagram_FindDialog(
				__parent.getParentJFrame(), this);
		}
	}	
	else if (action.equals(__MENU_FRAMEWORK)) {
		if (!__drawFramework) {
			__drawFramework = true;
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setDrawFramework(true);
			}						
		}
		else {
			__drawFramework = false;
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setDrawFramework(false);
			}						
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_HIDE_SELECTED)) {
		Message.printStatus(2, routine, "----------------------------");
		for (int i = 0; i < __tables.length; i++) {
			if (__tables[i].isMarked()) {
				__tables[i].setVisible(false);
			}
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_INCH_GRID)) {	
		if (__drawInchGrid) {	
			__drawInchGrid = false;
		}
		else {
			__drawInchGrid = true;
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_MARGIN)) {
		if (__drawMargin) {
			__drawMargin = false;
		}
		else {
			__drawMargin = true;
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_PIXEL_GRID)) {
		if (__drawPixelGrid) {
			__drawPixelGrid = false;
		}
		else {
			__drawPixelGrid = true;
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_PRINT)) {
		print();
	}
	else if (action.equals(__MENU_REFRESH)) {
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), true);
		__tables = __parent.readTables();
		__rels = __parent.readRelationships();
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), false);
		__forceRefresh = true;
		repaint();
	}	
	else if (action.equals(__MENU_SAVE)) {
		__parent.writeTables();
	}		
	else if (action.equals(__MENU_SAVE_AS_IMAGE)) {
		SaveImageGUI sig = new SaveImageGUI(getImage(), 
			__parent.getParentJFrame());
		String s = sig.getReturnStatus();
		int index = s.indexOf(")");
		__parent.setMessageStatus(s.substring(index + 1),
			s.substring(1, index));
	}
	else if (action.equals(__MENU_TEXT)) {
		if (__drawText) {
			__drawText = false;
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setTitleVisible(false);
				__tables[i].setTextVisible(false);
			}
		}
		else {
			__drawText = true;
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setTitleVisible(true);
				__tables[i].setTextVisible(true);
			}
		}
		__forceRefresh = true;
		repaint();
	}
	else if (action.equals(__MENU_TITLE_ONLY)) {
		if (!__titleOnly) {
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setTitleVisible(true);
				__tables[i].setTextVisible(false);
			}
		}
		else {
			for (int i = 0; i < __tables.length; i++) {
				__tables[i].setTitleVisible(true);
				__tables[i].setTextVisible(true);
			}
		}
		__forceRefresh = true;
		repaint();
	}	
	else if (event.getSource() instanceof SimpleJComboBox) {
		String scale = __scaleComboBox.getSelected();
		if (scale.equals("10%")) {
			__drawingScale = .1;
		}
		else if (scale.equals("50%")) {
			__drawingScale = .5;
		}
		else if (scale.equals("100%")) {
			__drawingScale = 1;
		}
		else if (scale.equals("200%")) {
			__drawingScale = 2;
		}
		else {
			int index = scale.indexOf("%");
			if (index > -1) {
				scale = scale.substring(0, index);
			}
			scale = scale.trim();
			try {
				double dtemp = Double.valueOf(scale).doubleValue();
				__drawingScale = (dtemp / 100);
			}
			catch (Exception e) {
				// not a number, no sweat, just ignore it.
			}
		}
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), true);
		setPreferredSize(
			new Dimension(
			(int)(getDrawingWidth() * getDrawingScale()),
			(int)(getDrawingHeight() * getDrawingScale())));
		revalidate();

		__drawingArea.setDataLimits(0, 0, (int)(__width), 
			(int)(__height));

		resize2((int)(__width * __drawingScale), 
			(int)(__height *__drawingScale));
		__drawingArea.setDrawingLimits(0, 0, 
			(int)(__width * __drawingScale), 
			(int)(__height *__drawingScale), 
			GRUnits.DEVICE, GRLimits.DEVICE);

		__initialize = true;
		__forceRefresh = true;
		getLimits(true);
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), false);
		repaint();
	}
	else {
		String table = __findDialog.getText();
		__parent.setMessageStatus("Finding table '" + table + "*'",
			"WAIT");
		int count = 0;
		for (int i = 0; i < __tables.length; i++) {
			if (__tables[i].getName().toLowerCase().indexOf(table)
				> -1) {
				__parent.setMessageStatus("Match: "
					+ __tables[i].getName() + "' at X: " 
					+ __tables[i].getX() + " Y: " 
					+ __tables[i].getY(), "WAIT");
				__tables[i].markSelected(true);
				count++;
			}
			else {
				__tables[i].markSelected(false);
			}
		}
		String plural = "s";
		if (count == 1) {
			plural = "";
		}
		__parent.setMessageStatus("Found " + count + " matching "
			+ "table" + plural + ".", "READY");
		__forceRefresh = true;
		__findDialog.dispose();
		__findDialog = null;		
		repaint();
	}
}

/**
Clears the screen; fills with white.
*/
public void clear() {
	_graphics.setColor(Color.white);
	_graphics.fillRect( 0, 0, getBounds().width, getBounds().height);
}

/**
Returns the index (in the __tables array) of the table that contains the
specified X and Y location.
@param x the x location of a mouse press
@param y the y location of a mouse press
@return the index of the table that contains the mouse press, or -1 if none do.
*/
private int findTableAtXY(double x, double y) {
	for (int i = (__tables.length - 1); i >= 0; i--) {
		if (__tables[i].contains(x, y) && __tables[i].isVisible()) {
			return i;
		}
	}
	return -1;
}

/**
Returns the drawing scale of the ER Diagram.
A display scale of 1 means that no 
scaling is done.  A value of 2 means that a 100-pixel wide rectangle will 
be drawn as 200 pixels wide.  A value of .4 means that a 100-pixel wide 
rectangle wil be drawn as 40 pixels wide.
@return the drawing scale of the ER Diagram.
*/
public double getDrawingScale() {
	return __drawingScale;
}

/**
Returns the height of the drawing area (in pixels).  
@return the height of the drawing area (in pixels).
*/
public double getDrawingHeight() {
	return __height;
}

/**
Returns the width of the drawing area (in pixels).
@return the width of the drawing area (in pixels).
*/
public double getDrawingWidth() {	
	return __width;
}

/**
Returns the preferred scrollable viewport size (from Scrollable interface).
@return the preferred scrollable viewport size.
*/
public Dimension getPreferredScrollableViewportSize() {
	return __parent.getSize();
}

/**
Returns 20, the scrollable block increment.  Default was 1 pixel of scroll
per time, which was too slow.  From Scrollable interface.
@param visibleRect see Scrollable.getScrollableBlockIncrement().
@param orientation see Scrollable.getScrollableBlockIncrement().
@param direction see Scrollable.getScrollableBlockIncrement().
@return 20.
*/
public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, 
int direction) {
	return 20;
}

/**
Returns false.  From Scrollable interface.
@return false.
*/
public boolean getScrollableTracksViewportHeight() {
	return false;
}

/**
Returns false.  From Scrollable interface.
@return false.
*/
public boolean getScrollableTracksViewportWidth() {
	return false;
}

/**
Returns 20.  
From Scrollable interface.
@param visibleRect see Scrollable.getScrollableBlockIncrement().
@param orientation see Scrollable.getScrollableBlockIncrement().
@param direction see Scrollable.getScrollableBlockIncrement().
@return 20.
*/
public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, 
int direction) {
	return 20;
}

/**
Finds a table in the array of tables that has the specified name.
@param name the name of the table to find.
@return the number of the table in the tables array, or -1 if no matching
table was found.
*/
/* TODO SAM 2007-05-08 Evaluate whether needed
private int getTableByName(String name) {
	for (int i = 0; i < __tables.length; i++) {
		if (__tables[i].getName().equals(name)) {			
			return i;
		}
	}
	return -1;
}
*/

/**
Inverts the value of Y so that Y runs from 0 at the bottom to MAX at the top.
@param y the value of Y to invert.
@return the inverted value of Y.
*/
private double invertY(double y) {
	return _devy2 - y;
}

/**
Responds to mouse clicked events; does nothing.
@param event the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent event) {}

/**
Responds to mouse dragged events (MouseMotionListener event).
@param event the MouseEvent that happened.
*/
public void mouseDragged(MouseEvent event) {
	if (__inDrag) {
		__x = event.getX() / __drawingScale;
		__y = invertY(event.getY()) / __drawingScale;
		repaint();
	}
}

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
Responds to mouse moved events (MouseMotionListener event); does nothing.
@param event the MouseEvent that happened.
*/
public void mouseMoved(MouseEvent event) {}

/**
Responds to mouse pressed events.
@param event the MouseEvent that happened.
*/
public void mousePressed(MouseEvent event) {
	// do not respond to popup events
	if (event.getButton() == MouseEvent.BUTTON1) {
		__table = findTableAtXY(event.getX() / __drawingScale, 
		(invertY(event.getY())/__drawingScale));
		// if a table was clicked on ...
		if (__table > -1) {
			__x = event.getX() / __drawingScale;
			__y = invertY(event.getY()) / __drawingScale;
			__tableLimits = __tables[__table].getLimits();
			__xAdjust = (event.getX() / __drawingScale) 
				- __tableLimits.getMinX();
			__yAdjust = (invertY(event.getY()) / __drawingScale) 
				- __tableLimits.getMinY();
			__inDrag = true;
			__forceRefresh = true;
			__tables[__table].setVisible(false);
			__eraseTable = true;
			repaint();
		}
		else {
			// ... otherwise, check to see if a relationship was
			// clicked on or near.
			__inDrag = false;
			__tableLimits = null;
			double x = event.getX() / __drawingScale;
			double y = invertY(event.getY()) / __drawingScale;
			boolean refresh = false;
			boolean bold;
			for (int i = 0; i < __rels.length; i++) {
				if (__rels[i].contains(x, y)) {
					bold = __rels[i].bold;
					if (bold != true) {
						refresh = true;
						__rels[i].bold = true;
					}
				}
				else {
					if (__rels[i].bold != false) {
						refresh = true;
						__rels[i].bold = false;
					}
				}
			}

			if (refresh) {
				__forceRefresh = true;
				repaint();
			}
		}
	}
}

/**
Responds to mouse released events.
@param event the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent event) {
	if (event.isPopupTrigger()) {
		__popup.show(event.getComponent(), event.getX(), event.getY());
	}
	else {
		__inDrag = false;
		if (__table > -1) {
			__tables[__table].setVisible(true);
			__forceRefresh = true;
			__x = (event.getX() / __drawingScale) - __xAdjust;
			__y = (invertY(event.getY()) / __drawingScale) 
				- __yAdjust;

			// prevent tables from being dragged off the drawing
			// area completely.
			if (__x < 0) {
				__x = 0;
			}
			if (__y < 0) {
				__y = 0;
			}
			if (__x > __width) {
				__x = __width - __tables[__table].getWidth();
			}
			if (__y > __height) {
				__y = __height - __tables[__table].getHeight();
			}
			
			__tables[__table].setX(__x);
			__tables[__table].setY(__y);
			__tables[__table].setDirty(true);
			__table = -1;
			repaint();
		}
	}
}

/**
Paints the screen.
@param g the Graphics context to use for painting.
*/
public void paint(Graphics g)
{
	String routine = "paint";
	Message.printStatus(2,routine,"Rendering ER diagram.");
	// sets the graphics in the base class appropriately (double-buffered
	// if doing double-buffered drawing, single-buffered if not)
	setGraphics(g);
	setAntiAlias(true);
	// Set up drawing limits based on current window size...
	setLimits(getLimits(true));

	// first time through, do the following ...
	if (__initialize) {
		Message.printStatus(2,routine,"Initializing.");
		// one time ONLY, do the following.
		if (__onceEver) {
			__height = getBounds().height;
			__width = getBounds().width;	
			__onceEver = false;
		}
		__initialize = false;
		setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
		
		for (int i = 0; i < __tables.length; i++) {
			__tables[i].calculateBounds(__drawingArea);
		}
		Message.printStatus(2,routine,"Calling repaint().");
		repaint();
		__forceRefresh = true;
	}

	// only do the following if explicitly instructed to ...
	if (__forceRefresh) {
		Message.printStatus(2,routine,"Doing __forceRefresh=true block");
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), true);
		// if the currently specified table is set to be erased, then
		// a box that is 200 pixels wider and taller (to account for
		// dropshadows and anything else) is created around the table
		// to erase, and clipping bounds are set on that.  The call
		// to clear() after this if(){} will then only clear the 
		// region inside the clipping box.  Drawing (such as of 
		// relationship lines) will only occur inside the clipping box.
		__eraseTable = false;
		if (__eraseTable) {
			GRLimits limits = 
			new GRLimits(
				(__x - __xAdjust - 100),
				invertY(__y - __yAdjust - 100),
				__x - __xAdjust + __tableLimits.getWidth()+ 100,
				invertY(__y + __tableLimits.getHeight() - __yAdjust + 100));

			setClip(limits);
		}

		clear();	

		// First draw all the relationship lines.  Tables are drawn over the top of these
		setAntiAlias(true);
		Message.printStatus(2,routine,"Drawing " + __rels.length + " relationships");
		for (int i = 0; i < __rels.length; i++) {
			__rels[i].draw(__drawingArea, __tables);
		}

		// Draw the tables -- if they are visible
		setAntiAlias(true);
		Message.printStatus(2,routine,"Drawing " + __tables.length + " tables, limits = " + getLimits());
		for (int i = 0; i < __tables.length; i++) {
			Message.printStatus(2,routine,"Table[" + i + "] visible= " + __tables[i].isVisible() + " isWithinLimits=" +
				__tables[i].isWithinLimits(getLimits(false), __drawingScale));
			if (__tables[i].isVisible() && __tables[i].isWithinLimits(getLimits(false), __drawingScale)) {
			    __tables[i].draw(__drawingArea);
			}
		}
	
		// if the grid should be drawn, do so ...
		setAntiAlias(true);
		if (__drawInchGrid) {
			GRDrawingAreaUtil.setColor(__drawingArea, GRColor.red);
			for (int i = 0; i < 100000; i+= ((72/__printScale)/2)) {
				GRDrawingAreaUtil.drawLine(__drawingArea, i, 0, i, 100000);
				GRDrawingAreaUtil.drawLine(__drawingArea, 0, i, 100000, i);
				GRDrawingAreaUtil.drawText(__drawingArea, 
					("" + ((double)i/(72/__printScale))),i, 
					0, 0, GRText.CENTER_X | GRText.BOTTOM);
				GRDrawingAreaUtil.drawText(__drawingArea, 
					("" + ((double)i/(72/__printScale))),0, 
					i, 0, GRText.CENTER_Y | GRText.LEFT);
			}
		}
		
		setAntiAlias(true);
		if (__drawPixelGrid) {
			GRDrawingAreaUtil.setColor(__drawingArea,GRColor.green);
			for (int i = 0; i < 100000; i+= 50) {
				GRDrawingAreaUtil.drawLine(__drawingArea, i, 0, i, 100000);
				GRDrawingAreaUtil.drawLine(__drawingArea, 0, i, 100000, i);
				GRDrawingAreaUtil.drawText(__drawingArea, ("" + i) ,i, 0, 0,
					GRText.CENTER_X | GRText.BOTTOM);
				GRDrawingAreaUtil.drawText(__drawingArea, ("" + i),0, i, 0,
					GRText.CENTER_Y | GRText.LEFT);
			}			
		}
		__forceRefresh = false;		

		setAntiAlias(true);
		if (__drawMargin) {
			GRDrawingAreaUtil.setColor(__drawingArea, 
				GRColor.cyan);
			PageFormat pageFormat = __parent.getPageFormat();
			PrintUtil.dumpPageFormat(pageFormat);
			double leftX = pageFormat.getImageableX() /__printScale;
			double topY = (pageFormat.getHeight() 
				- pageFormat.getImageableY()) / __printScale;
			double rightX = (leftX + pageFormat.getImageableWidth()
				/ __printScale) - 1;
			double bottomY = ((pageFormat.getHeight()
				- (pageFormat.getImageableY() 
					+ pageFormat.getImageableHeight()))
				/ __printScale) + 1;
			GRDrawingAreaUtil.drawLine(__drawingArea,
				leftX, topY, leftX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea,
				rightX, topY, rightX, bottomY);
			GRDrawingAreaUtil.drawLine(__drawingArea,
				leftX, topY, rightX, topY);
			GRDrawingAreaUtil.drawLine(__drawingArea,
				leftX, bottomY, rightX, bottomY);
		}

		// if the clipping area was set up above, un-set it with a 
		// call to setClip(null)
		if (__eraseTable) {
			setClip(null);
			__eraseTable = false;
		}
		JGUIUtil.setWaitCursor(__parent.getParentJFrame(), false);
	}
	
	// displays the graphics
	showDoubleBuffer(g);

	// if a table is currently being dragged around the screen, draw the
	// outline of the table on top of the double-buffer
	setAntiAlias(true);
	if (__inDrag) {
		_graphics = (Graphics2D)g;
		GRDrawingAreaUtil.setColor(__drawingArea, GRColor.black);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__x - __xAdjust, 
			__y - __yAdjust, 
			__tableLimits.getWidth() + __x - __xAdjust, 
			__y - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__x - __xAdjust, 
			__y + __tableLimits.getHeight() - __yAdjust,
			__tableLimits.getWidth() + __x - __xAdjust,
			__y + __tableLimits.getHeight() - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea, 
			__x - __xAdjust, 
			__y - __yAdjust,
			__x - __xAdjust, 
			__y + __tableLimits.getHeight() - __yAdjust);
		GRDrawingAreaUtil.drawLine(__drawingArea,
			__x + __tableLimits.getWidth() - __xAdjust,	
			__y - __yAdjust, 
			__x + __tableLimits.getWidth() - __xAdjust,
			__y + __tableLimits.getHeight() - __yAdjust);
	}	
}

/**
Sets up a print job and submits it.
*/
public void print() {
	PrinterJob printJob = PrinterJob.getPrinterJob();

	PageFormat pageFormat = __parent.getPageFormat();

	printJob.setPrintable(this, pageFormat);

	if (__parent.isDebug()) {
		PrintUtil.dumpPageFormat(pageFormat);
	}

	try {
	PrintUtil.print(this, pageFormat);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}

/**
Prints a page.
@param g the Graphics context to which to print.
@param pageFormat the pageFormat to use for printing.
@param pageIndex the index of the page to print.
@return Printable.NO_SUCH_PAGE if no page should be printed, or 
Printable.PAGE_EXISTS if a page should be printed.
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
	String routine = "ERDiagram_Device.print";
	if (pageIndex > 0) {
		return NO_SUCH_PAGE;
	}

	Graphics2D g2d = (Graphics2D)g;
	// Set for the GRDevice because we will temporarily use 
	// that to do the drawing...	

	double transX = 0;
	double transY = 0;
	
	if (!StringUtil.startsWithIgnoreCase(
		PrintUtil.pageFormatToString(pageFormat), 
		"Plotter")) {
	if (pageFormat.getOrientation() == PageFormat.LANDSCAPE) {
		transX = pageFormat.getImageableX() * (1 / __printScale);
//		transY = pageFormat.getHeight() 
//			- (pageFormat.getImageableHeight() 
//			+ pageFormat.getImageableY());
		transY = pageFormat.getImageableY() * (1 / __printScale);
	}
	else {
		transX = pageFormat.getImageableX() * (1 / __printScale);
		transY = pageFormat.getImageableY() * (1 / __printScale);
	}
	}

	g2d.scale(__printScale, __printScale);
//	g2d.translate(transX, transY);

/*
	JTS - keep this in here, it's useful debugging information for
	when/if printing malfunctions
*/
	Message.printStatus(2, routine, "print() --------------");
	Message.printStatus(2, routine, "  __height: " + __height);
	Message.printStatus(2, routine, "  __width: " + __width);
	Message.printStatus(2, routine, "  Scale: " + __printScale);
	Message.printStatus(2, routine, "  TransX: " + transX);
	Message.printStatus(2, routine, "  TransY: " + transY);
	Message.printStatus(2, routine, "  Height : " + pageFormat.getHeight());
	Message.printStatus(2, routine, "  Width  : " + pageFormat.getWidth());
	Message.printStatus(2, routine, "  IHeight: " 
		+ pageFormat.getImageableHeight());
	Message.printStatus(2, routine, "  IWidth : " 
		+ pageFormat.getImageableWidth());
	Message.printStatus(2, routine, "  IX     : " 
		+ pageFormat.getImageableX());
	Message.printStatus(2, routine, "  IY     : " 
		+ pageFormat.getImageableY());
	Message.printStatus(2, routine, "  Orient : " 
		+ pageFormat.getOrientation());

	paint(g2d);

	return PAGE_EXISTS;
}

/**
Sets the drawing area to be used with this device.
@param drawingArea the drawingArea to use with this device.
*/
public void setDrawingArea(ERDiagram_DrawingArea drawingArea) {
	__drawingArea = drawingArea;
}

/**
Sets the combo box on the main JFrame that is used to specify the drawing scale
of the ER Diagram.  This is needed because this classes needs to be able to tell
what the combo box is currently set to.
@param comboBox the combobox in which drawing scale is specified.
*/
public void setScaleComboBox(SimpleJComboBox comboBox) {
	__scaleComboBox = comboBox;
}

}
