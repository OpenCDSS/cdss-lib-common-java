// ----------------------------------------------------------------------------
// GRTest_Device - the device that controls the test drawing.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-06-03	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.GR;

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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;

import RTi.Util.GUI.*;

import RTi.Util.IO.PrintUtil;

import RTi.Util.String.StringUtil;

/**
This class extends GRJComponentDevice to be the device that manages the ER
Diagram drawing area.
*/
@SuppressWarnings("serial")
public class GRTest_Device extends GRJComponentDevice
implements ActionListener, MouseListener, MouseMotionListener, Printable, 
Scrollable {

/**
Labels for menu items.
*/
private final String 
	__MENU_EXIT = "Exit",
	__MENU_PRINT = "Print",
	__MENU_REFRESH = "Refresh",
	__MENU_SAVE_AS_IMAGE = "Save as Image";
		
/**
Whether to force paint() to refresh the entire drawing area or not.
*/
private boolean __forceRefresh = true;

/**
Whether drawing settings need to be initialized because it is the first
time paint() has been called.
*/
private boolean __initialize = true;

/**
Whether the axes are currently in normal or log/std mode.
*/
//private boolean __normalAxes = true;

/**
Used to specify painting commands that will occur only once ever for the entire
life of the class.
*/
private boolean __onceEver = true;

/**
The printing scale factor of the drawing.  This is the amount by which the
72 dpi printable pixels are scaled.  A printing scale value of 1 means that
the ER diagram will be printed at 72 pixels per inch (ppi), which is the 
java standard.   A scale factor of .5 means that the ER Diagram will be 
printed at 144 ppi.  A scale factor of 3 means that the ER Diagram will be 
printed at 24 ppi.
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
The Y location of the last mouse press.
*/
private double __y;

/**
The data limits.
*/
//private GRLimits __dataLimits = null;

/**
The drawing limits.
*/
//private GRLimits __drawingLimits = null;

/**
The drawing area on which the ER Diagram is drawn.
*/
private GRTest_DrawingArea __drawingArea;

/**
The parent JFrame.
*/
private GRTest_JFrame __parent;

/**
The popup menu that appears when the pane is right-clicked on.
*/
private JPopupMenu __popup;

/**
Constructor.  Builds the device for the specified tables and relationships with
the specified scale.
@param parent the jframe in which this device is used.
@param scale the scaling factor for drawing.
*/
public GRTest_Device(GRTest_JFrame parent, double scale) {
	super("GRTest_Device");

	addMouseListener(this);
	addMouseMotionListener(this);

	__parent = parent;
	__printScale = scale;

	// build the popup menu
	__popup = new JPopupMenu();
	JMenuItem mi = new JMenuItem(__MENU_PRINT);
	mi.addActionListener(this);
	__popup.add(mi);
	mi = new JMenuItem(__MENU_SAVE_AS_IMAGE);
	mi.addActionListener(this);
	__popup.add(mi);
	mi = new JMenuItem(__MENU_REFRESH);
	mi.addActionListener(this);
	__popup.add(mi);
	__popup.addSeparator();
	//JCheckBoxMenuItem jcbmi;
	mi = new JMenuItem(__MENU_EXIT);
	mi.addActionListener(this);
	__popup.add(mi);
}

/**
Responds to action events.
@param event the ActionEvent that happened.
*/
public void actionPerformed(ActionEvent event) {
	//String routine = "GRTest_Device.actionPerformed";
	String action = event.getActionCommand();

	if (action.equals(__MENU_EXIT)) {
		__parent.closeWindow();
		System.exit(0);
	}	
	else if (action.equals(__MENU_PRINT)) {
		print();
	}
	else if (action.equals(__MENU_REFRESH)) {
		__forceRefresh = true;
		repaint();
	}	
	else if (action.equals(__MENU_SAVE_AS_IMAGE)) {
		SaveImageGUI sig = new SaveImageGUI(getImage(), 
			__parent);
		String s = sig.getReturnStatus();
		int index = s.indexOf(")");
		__parent.setMessageStatus(s.substring(index + 1),
			s.substring(1, index));
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
	__x = event.getX() / __drawingScale;
	__y = invertY(event.getY()) / __drawingScale;
	
	__parent.setMessageStatus("Location: " + __x + ", "
		+ __y, "");

	repaint();
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
		__x = event.getX() / __drawingScale;
		__y = invertY(event.getY()) / __drawingScale;	

		__parent.setMessageStatus("Location: " + __x + ", "
			+ __y, "");

		__forceRefresh = true;
		repaint();
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
		__x = event.getX() / __drawingScale;
		__y = invertY(event.getY()) / __drawingScale;	

		__parent.setMessageStatus("Location: " + __x + ", "
			+ __y, "");

		__forceRefresh = true;
		repaint();
	}
}

/**
Paints the screen.
@param g the Graphics context to use for painting.
*/
public void paint(Graphics g) {
	// sets the graphics in the base class appropriately (double-buffered
	// if doing double-buffered drawing, single-buffered if not)
	setGraphics(g);

	// Set up drawing limits based on current window size...
	setLimits(getLimits(true));

	// first time through, do the following ...
	if (__initialize) {
		// one time ONLY, do the following.
		if (__onceEver) {
			__height = getBounds().height;
			__width = getBounds().width;	
			__onceEver = false;
		}
		__initialize = false;
		setupDoubleBuffer(0, 0, getBounds().width, getBounds().height);
		
		repaint();
		__forceRefresh = true;
	}

	// only do the following if explicitly instructed to ...
	if (__forceRefresh) {
		JGUIUtil.setWaitCursor(__parent, true);

		clear();	

		draw();

		JGUIUtil.setWaitCursor(__parent, false);
	}
	
	__forceRefresh = false;
	
	// displays the graphics
	showDoubleBuffer(g);
}

/**
Sets up a print job and submits it.
*/
public void print() {
	PrinterJob printJob = PrinterJob.getPrinterJob();
	PageFormat pageFormat = __parent.getPageFormat();
	printJob.setPrintable(this, pageFormat);

	PrintUtil.dumpPageFormat(pageFormat);

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
	//String routine = "GRTest_Device.print";
	if (pageIndex > 0) {
		return NO_SUCH_PAGE;
	}

	Graphics2D g2d = (Graphics2D)g;
	// Set for the GRDevice because we will temporarily use 
	// that to do the drawing...	

	//double transX = 0;
	//double transY = 0;
	
	if (!StringUtil.startsWithIgnoreCase(
		PrintUtil.pageFormatToString(pageFormat), 
		"Plotter")) {
		if (pageFormat.getOrientation() == PageFormat.LANDSCAPE) {
			//transX = pageFormat.getImageableX() * (1 /__printScale);
			//transY = pageFormat.getImageableY() * (1 /__printScale);
		}
		else {
			//transX = pageFormat.getImageableX() * (1 /__printScale);
			//transY = pageFormat.getImageableY() * (1 /__printScale);
		}
	}

	g2d.scale(__printScale, __printScale);

	paint(g2d);

	return PAGE_EXISTS;
}

/**
Sets the data limits.
@param dataLimits the data limits.
*/
public void setDataLimits(GRLimits dataLimits) {
	//__dataLimits = dataLimits;
}

/**
Sets the drawing area to be used with this device.
@param drawingArea the drawingArea to use with this device.
*/
public void setDrawingArea(GRTest_DrawingArea drawingArea) {
	__drawingArea = drawingArea;
}

/**
Sets the drawing limits.
@param drawingLimits the drawing limits.
*/
public void setDrawingLimits(GRLimits drawingLimits) {
	//__drawingLimits = drawingLimits;
}

private void draw() {
	double x = 0;
	double y = 0;/*
	String paragraph = "1 Lorem ipsum dolor sit amet, consectetuer1\n"
		+ "2adipiscing elit. Ut gravida. Nam pellentesque2\n"
		+ "3erat et est condimentum viverra. Nam orci.3\n"
		+ "4Suspendisse vulputate ultricies sem.4\n"
		+ "5Aliquam molestie leo id urna.5\n"
		+ "6Vestibulum convallis metus6\n"
		+ "7ac ante.7";
		*/
	String paragraph2 = "1 Lorem ipsum dolor sit amet, consectetuer1\n"
		+ "2adipiscing elit. Ut gravida. Nam pellentesque2\n"
		+ "3erat et est condimentum viverra. Nam orci.3\n"
		+ "4Suspendisse vulputate ultricies sem.4\n"
		+ "5Aliquam molestie leo id urna.5\n"
		+ "6Vestibulum convallis metus6";
	String line = "CORRECT CORRECT";
	setAntiAlias(true);
	int[] flags = new int[9];
	flags[0] = GRText.TOP| GRText.RIGHT;
	flags[1] = GRText.TOP | GRText.CENTER_X;
	flags[2] = GRText.TOP | GRText.LEFT;
	flags[3] = GRText.CENTER_Y | GRText.RIGHT;
	flags[4] = GRText.CENTER_Y | GRText.CENTER_X;
	flags[5] = GRText.CENTER_Y | GRText.LEFT;
	flags[6] = GRText.BOTTOM | GRText.RIGHT;
	flags[7] = GRText.BOTTOM | GRText.CENTER_X;
	flags[8] = GRText.BOTTOM | GRText.LEFT;
	
	for (int i = 0; i < 9; i++) {
		GRDrawingAreaUtil.setColor(__drawingArea, 
			GRColor.parseColor(GRColor.COLOR_NAMES[i + 1]));
		x = (i % 3) * 30 + 20;
		y = (i / 3) * 30 + 20;
//	Message.printStatus(1, "", "I: " + i + "  X: " + x + "  Y: " + y);	
		GRDrawingAreaUtil.drawSymbol(__drawingArea, GRSymbol.SYM_FCIR,
			x, y, 1, GRUnits.DATA, 0);
		
		GRDrawingAreaUtil.drawText(__drawingArea, paragraph2,
			x, y, 0, flags[i], 0);
		GRDrawingAreaUtil.drawText(__drawingArea, line,
			x, y, 0, flags[i], 0);
	}
}

}
