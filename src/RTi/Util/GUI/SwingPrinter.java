// ----------------------------------------------------------------------------
// SwingPrinter.java - Class for easy printing of Swing components.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2002-11-14	J. Thomas Sapienza, RTi	Initial version.
// 2002-11-15	JTS, RTi		Javadoc'd.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;

import javax.swing.RepaintManager;

import RTi.Util.Message.Message;

/**
Class to manage the printing of entire Swing components, ie, a graph,
a TextArea, etc.  
Code was derived from the Swing Tutorial's PrintUtilities.java (freely
available for download and modification), found at:
http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/
REVISIT (JTS - 2006-05-23)
Should this be used or should some other class be used instead?  There are a 
lot of printing classes now, which one should a developer use?  And if this
one, add exaqmples.
*/
public class SwingPrinter 
implements Printable{

/** 
The Swing component that will be printed by the SwingPrinter.
*/
private Component __componentToBePrinted;
/**
The PageFormat to use for printing the page (handles margins, etc.).
*/
private PageFormat __pageFormat;

/**
Constructor.  Sets up the component to be printed.
@param c the component that the SwingPrinter will print.
*/
public SwingPrinter (Component c) {
	__componentToBePrinted = c;
}

/**
Turns off Double Buffering for the given Component and all its subcomponents.
Double-buffering screws up printing.
@param c the Component to turn off the double buffering for
*/
public static void disableDoubleBuffering(Component c) {
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(false);
}

/**
Turns on Double Buffering for the given Component and all its subcomponents.
Double-buffering screws up printing.
@param c the Component to turn on the double buffering for
*/
public static void enableDoubleBuffering(Component c) {
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(true);
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__componentToBePrinted = null;
	__pageFormat = null;
	super.finalize();
}

/**
Sets up the dialog to control printing. 
*/
public void print() {
	String routine = "SwingPrinter.print";
	PrinterJob printJob = PrinterJob.getPrinterJob();
	// get the desired formatting for the page (margins, etc)
	__pageFormat = printJob.pageDialog(printJob.defaultPage());
	printJob.setPrintable(this);
	if (printJob.printDialog()) {
		try {
			printJob.print();
		}
		catch (PrinterException e) {
			Message.printWarning(2, routine, "Error printing.");
			Message.printWarning(2, routine, e);
		}
	}
}

/**
Prints with the given PageFormat.
@param pageFormat the pageFormat with which to print.
*/
public void print(PageFormat pageFormat) {
	String routine = "SwingPrinter.print";
	__pageFormat = pageFormat;
	PrinterJob printJob = PrinterJob.getPrinterJob();
	printJob.setPrintable(this);
	try {
		printJob.print();
	}
	catch (PrinterException e) {
		Message.printWarning(2, routine, "Error printing.");
		Message.printWarning(2, routine, e);
	}
}

/** 
Handles the actual printing of the component in the given pageFormat
and on the given page.  This is called automatically when the Component is
printed.
@param g the Graphics object of the component to be printed.
@param pageFormat the format in which the page will be printed.
@param pageIndex the index of the page being printed -- currently only
handles one page being printed.
@return NO_SUCH_PAGE if pageIndex > 0, or PAGE_EXISTS
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
	if (pageIndex > 0) {
		return (NO_SUCH_PAGE);
	}
	else {
		pageFormat = __pageFormat;
		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(pageFormat.getImageableX(),
			pageFormat.getImageableY());
		// applies the previously chosen margins
		Rectangle r = new Rectangle(0, 0,
			(int)pageFormat.getImageableWidth(),
			(int)pageFormat.getImageableHeight());
		g2d.clip(r);
		System.out.println("before the print --------------------");
		disableDoubleBuffering(__componentToBePrinted);
		__componentToBePrinted.paint(g2d);
		enableDoubleBuffering(__componentToBePrinted);
		System.out.println("after the print -------------------");
		return (PAGE_EXISTS);
	}
}

public PageFormat getPageFormat() {
	return __pageFormat;
}

/**
Prints the given component.  
@param c the component to be printed.
*/
public static PageFormat printComponent(Component c) {
	SwingPrinter sp = new SwingPrinter(c);
	sp.print();
	return sp.getPageFormat();
}

public static PageFormat printComponent(Component c, PageFormat pageFormat) {
	SwingPrinter sp = new SwingPrinter(c);
	sp.print(pageFormat);
	return pageFormat;
}

}
