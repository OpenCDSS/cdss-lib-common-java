// ReportPrinter - class for printing out Vectors of text and JTextAreas

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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;

import javax.swing.JTextArea;

import java.util.ArrayList;
import java.util.List;

/**
A class to print out a report from a text.  Use by calling one of the static printText() methods.  Do not instantiate this class.
<p>Note: <br>
It is commonly thought that there are two orientations in which a page can be printed.  Landscape and Portrait.
However, in Java there is a third one: ReverseLandscape.
This is the Macintosh's default mode of Landscape printing and is different from normal in that the origin starts at the upper-right,
not the upper-left corner.  The printing code in this class probably won't work for Macintosh Landscape.
Not a big deal, perhaps, but it might come up in the future.
*/
public class ReportPrinter
implements Printable {

/**
Whether this printjob is being done in batch mode
(no dialog pops up to ask the user about the number of copies to print, etc.) or not.
*/
private boolean __batch = false;

/**
Whether the font size needed to fit X lines per page has been calculated yet or not.
*/
private boolean __fontSizeCalculated = false;

/**
Whether to print a test page or not.
*/
private boolean __printTestPage = false;

/**
The total vertical space between lines including the height of a line of text and the height of the spacing between each line.
*/
private double __slope = 0;

/**
The font ascent for the calculated font size.
*/
private int __as = 0;

/**
The font descent for the calculated font size.
*/
private int __ds = 0;

/**
The font size calculated to fit X lines per page.
*/
private int __fontSize = 0;

/**
The number of lines of text to be printed.
*/
private int
	__linesPerPage = 0,
	__linesPerPageL = 0,
	__linesPerPageP = 0;

/**
No idea.  Maybe can be removed?
*/
private int __loc = 0;

/**
The number of pages of text to be printed.
*/
private int __numPages = 0;

/**
The number of pages that have been prepped (see print(...) for more info).
*/
private int __pagesPrepped = 0;

/**
The number of pages that have been printed (see print(...) for more info).
*/
private int __pagesPrinted = 0;

/**
The PageFormat used to specify margins, orientation, etc. for this printjob.
*/
private PageFormat __pageFormat = null;

/**
The text to appear at the top of each page.
*/
private String __header = null;

/**
The list of text to be printed out.
*/
private List<String> __linesVector;

/**
Constructor.  Creates a ReportPrinter that will print the text in the given JTextArea with the given number of lines per page.
@param ta the JTextArea with the text to be printed.
@param linesPerPageP the number of lines to print on a single page in Portrait orientation.
@param linesPerPageL the number of lines to print on a single page in Landscape orientation.
@param header the text that will appear at the top of each page as a header.
If null, no header will be shown.
@param testPage whether this is just printing a testPage or will actually print whatever is in ta.
If testPage is set to true, only the testPage data will be printed, even if there is data in ta.
@param batch whether a dialog will pop up asking the user to confirm the print, the printer to which the job will go to, and other print job
information.  If false, the print job will just be sent to the default printer, whatever that is.
*/
private ReportPrinter (JTextArea ta, int linesPerPageP, int linesPerPageL, String header, boolean testPage, boolean batch) {
	__linesVector = taToStringList(ta);
	__printTestPage = testPage;
	__batch = batch;

	__linesPerPageL = linesPerPageL;
	__linesPerPageP = linesPerPageP;

	if (__linesVector == null) {
		if (testPage) {
			__numPages = 1;
		} else {
			__numPages = 0;
		}
	}
}

/**
Constructor.  Creates a ReportPrinter that will print the text in the given list with the given number of lines per page.
@param text the list of text to print.
@param linesPerPageP the number of lines to print on a single page in Portrait orientation.
@param linesPerPageL the number of lines to print on a single page in Landscape orientation.
@param header the text that will appear at the top of each page as a header. If null, no header will be shown.
@param testPage whether this is just printing a testPage or will actually
print whatever is in ta.  If testPage is set to true, only the testPage data will be printed, even if there is data in ta.
@param batch whether a dialog will pop up asking the user to confirm the print,
the printer to which the job will go to, and other print job information.
If false, the print job will just be sent to the default printer, whatever that is.
*/
private ReportPrinter (List<String> text, int linesPerPageP, int linesPerPageL,
String header, boolean testPage, boolean batch) {
	__linesVector = text;
	__printTestPage = testPage;
	__batch = batch;
	__header = header;
	__linesPerPageP = linesPerPageP;
	__linesPerPageL = linesPerPageL;

	if (__linesVector == null) {
		if (testPage) {
			__numPages = 1;
		} else {
			__numPages = 0;
		}
	}
}



/**
Calculates the font size necessary to fit X lines on a single page.
@param g2 the Graphics2D context being used to print the page.
*/
private void calculateFontSize(Graphics2D g2) {
	// Get the printable height of the paper.
	int paperHeight = (int)__pageFormat.getImageableHeight();

	// Adds room at the bottom and top for header and footer in 8 point // font.
	paperHeight -= 30;

	// Start with a reasonably-size, though large, font size and work down until one is found that will fit on the page.
	int fontSize = 50;
	boolean done = false;
	int as, ds;
	do {
		// Create a font, and get its relevant metrics from the graphics object.

		Font f = new Font("Monospaced", Font.PLAIN, fontSize);
		g2.setFont(f);
		FontMetrics fm = g2.getFontMetrics();
		as = fm.getAscent();
		ds = fm.getDescent();

		if (((as + ds) * __linesPerPage) <= paperHeight) {
			done = true;
			fontSize++;
		}
		else if ((((as + ds) * __linesPerPage) - __linesPerPage) <= paperHeight) {
			// Because this if checks to see whether the current font size could fit on the paper if the descent was reduced by one,
			// if it was successful then reduce the descent by one and use the font.
			// For an explanation of Descent, see the FontMetrics javadocs.
			ds--;
			done = true;
		}

		// if the current font size didn't fit, try with the next smaller one.
		if (done != true) {
			fontSize--;
		}
	} while (fontSize > 0 && !done);

	__fontSize = fontSize;
	__as = as;
	__ds = ds;
	__fontSizeCalculated = true;

	int space = paperHeight - ((__as + __ds) * __linesPerPage);

	if (__linesPerPage < space) {
		__loc = -1;
		__slope = space / __linesPerPage;
	}
	else if (__linesPerPage > space) {
		__loc = 1;
		__slope = (double)space / (double)__linesPerPage;
	}
}

/**
Creates a test page for demonstrating printer output.  The test page prints at least 5 lines of text.  The 5 lines are:
<ol>
<li>A line describing the font and font size being used.</li>
<li>A line with the page width and height.</li>
<li>A line with the imageable width and height.</li>
<li>A line with the imageable X and Y.</li>
<li>A line with the paper orientation.</li>
</ol>
After these lines, the rest of the lines to be printed are filled with "A quick brown fox ...",
"Ipsem lorum", and all the numbers and characters that can be printed.
@param g2 the Graphics object that will be used to draw the text.
*/
private void createTestPage(Graphics2D g2) {
	Font f =new Font("Monospaced", Font.PLAIN, __fontSize);
	g2.setFont(f);

	String line;
	line = "  1 (of " + __linesPerPage + " lines): Font: " + __fontSize + "-point Monospaced, Plain";
	g2.drawString(line, 0, __as);

	line = "  2: Page Width: " + __pageFormat.getWidth() + "  Page Height: " + __pageFormat.getHeight();
	g2.drawString(line, 0, __as + (1 * (__as + __ds)));

	line = "  3: Imageable Width: " + __pageFormat.getImageableWidth() + "  Imageable Height: " + __pageFormat.getImageableHeight();
	g2.drawString(line, 0, __as + (2 * (__as + __ds)));

	line = "  4: Imageable X: " + __pageFormat.getImageableX() + "  Imageable Y: " + __pageFormat.getImageableY();
	g2.drawString(line, 0, __as + (3 * (__as + __ds)));

	if (__pageFormat.getOrientation() == PageFormat.LANDSCAPE) {
		line = "  5: Paper Orientation: Lan__dscape";
	} else {
		line = "  5: Paper Orientation: Portrait";
	}
	g2.drawString(line, 0, __as + (4 * (__as + __ds)));

	// check a few test boundary cases to be sure to print out the right number of lines.
	// If there are to be 5 or less lines per page printed, ignore anything less than 5 and print those 5.
	if (__linesPerPage <= 5) {
		if (__pagesPrinted == 1) {
			__printTestPage = false;
		}
		return;
	}
	// If 6 lines are to be printed, print a single line more and then return.
	else if (__linesPerPage == 6) {
		line = "  6: the quick brown fox jumps over a lazy dog THE QUICK BROWN FOX JUMPS OVER A LAZY DOG";
		g2.drawString(line, 0, __as + (5 * (__as + __ds)));
		if (__pagesPrinted == 1) {
			__printTestPage = false;
		}
		return;
	}

	// Otherwise, calculate how many lines of character text and how many lines of numbers and symbols need to be printed.
	int half = (__linesPerPage - 5)/ 2;
	if (half < 5) {
		half = half + 5;
	}

	for (int i = 5; i < half; i++) {
		if (i < 9) {
			line = "  " + (i + 1);
		} else if (i < 99) {
			line = " " + (i + 1);
		} else {
			line = "" + (i + 1);
		}
		line += ": the quick brown fox jumps over a lazy "
			+ "dog THE QUICK BROWN FOX JUMPS OVER A "
			+ "LAZY DOG.  "
			+ "Lorem ipsum dolor sit amet, consectetaur "
			+ "adipisicing elit, sed do eiusmod tempor incididunt "
			+ "ut labore et dolore magna aliqua. Ut enim ad minim "
			+ "veniam, quis nostrud exercitation ullamco laboris "
			+ "nisi ut aliquip ex ea commodo consequat. Duis aute "
			+ "irure dolor in reprehenderit in voluptate velit "
			+ "esse cillum dolore eu fugiat nulla pariatur. "
			+ "Excepteur sint occaecat cupidatat non proident, "
			+ "sunt in culpa qui officia deserunt mollit anim id "
			+ "est laborum.";
		g2.drawString(line, 0, __as + (i * (__as + __ds)));
	}
	for (int i = half; i < __linesPerPage; i++) {
		if (i < 9) {
			line = "  " + (i + 1);
		} else if (i < 99) {
			line = " " + (i + 1);
		} else {
			line = "" + (i + 1);
		}
		line += ": 1234567890 `~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
		g2.drawString(line, 0, __as + (i * (__as + __ds)));
	}

	// __pagesPrnted will be == 1 after the page has first been prepped and then print() has been called again to print it,
	// so setting __printTestPage to false will break out of the loop in the top of the print(....) method.  See that method for more info.
	if (__pagesPrinted == 1) {
		__printTestPage = false;
	}
}

/**
Returns the PageFormat being used for this job.
@return the PageFormat being used for this job.
*/
public PageFormat getPageFormat() {
	return __pageFormat;
}

/**
Method called by the static print methods to gather and set up page and print job information before the printing starts.
*/
private void print() {
	PrinterJob printJob = PrinterJob.getPrinterJob();

	// Get the desired formatting for the page (margins, etc).
	if (__pageFormat == null) {
		__pageFormat = printJob.pageDialog(printJob.defaultPage());
	}

	int orientation = __pageFormat.getOrientation();

	Paper p = new Paper();
	// If the paper orientation (as gathered from the PageFormat) is in PageFormat.LANDSCAPE mode,
	// then switch around the X and Y and Height and Width attributes.
	// If this is not done, the page will not be printed correctly.
	if (orientation == PageFormat.LANDSCAPE) {
		__linesPerPage = __linesPerPageL;
		p.setImageableArea(	__pageFormat.getImageableY(),
					__pageFormat.getImageableX(),
					__pageFormat.getImageableHeight(),
					__pageFormat.getImageableWidth());
	} else {
		__linesPerPage = __linesPerPageP;
		p.setImageableArea(	__pageFormat.getImageableX(),
					__pageFormat.getImageableY(),
					__pageFormat.getImageableWidth(),
					__pageFormat.getImageableHeight());
	}


	if (__linesVector.size() > 0) {
		// Calculates the number of pages necessary to print all the text.  Changes the starting number from 0-b_ased to 1-b_ased.
		__numPages = (__linesVector.size() / (__linesPerPage + 1)) + 1;
	} else {
		__numPages = 0;
	}

	String plural = "s";
	if (__numPages == 1) {
		plural = "";
	}
	printJob.setJobName("ReportPrinter (" + __numPages + ") page" + plural);

	// This next section of code may appear odd, but without it the
	// user-specified margins do not get into the PageFormat object,
	// and the default margins of 1-inch on all sides are applied.
	__pageFormat.setPaper(p);
	__pageFormat.setOrientation(orientation);
	printJob.setPrintable(this, __pageFormat);

	// If in batch mode, don't bother opening the dialog box asking the user to which printer they wish to print.
	// Force it to the default system printer (whatever that may be).
	if (__batch) {
		try {
			printJob.print();
		}
		catch (PrinterException e) {
			// REVISIT Error printing.
		}
	}
	else {
		// Open a dialog box asking the User to which printer they wish to print, how many copies they wish to print, etc.
		// Dialog box varies from system to system, so not all options may be available.
		if (printJob.printDialog()) {
			try {
				printJob.print();
			}
			catch (PrinterException e) {
				// REVISIT Error printing.
			}
		}
	}
}

/**
Method that actually gets called for doing the printing.
This method calculates the font size necessary to fit the given number of lines on the page.
This method is called once for every page to be printed, and is called by the object controlling the print job.
Users need never worry about this.<p>
<b>Do not call this method.  It is public because this class uses the Printable interface.  Call one of the static printText() methods.</b>
@param g the graphics context for what is being printed.
@param pageFormat the format the page will be printed in
@param pageIndex the number of the page being printed (0, 1, 2 ...)
@return NO_SUCH_PAGE if failed, PAGE_EXISTS if successful.
*/
public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
	if ((pageIndex >= __numPages) && (__printTestPage == false)) {
		return NO_SUCH_PAGE;
	}
	else {
		// This method gets called twice for every page that gets printed.
		// The first time it appears to just do prep work and see how the page will be rendered.
		// The second time the page actually gets printed.
		// In order to keep track of where the print job is at (especially for use in printing the test page),
		// a count is kept of every time a page is prepped (this method is called for the first time)
		// and when the page is printed (this method gets called again for the same page).
		if (__pagesPrinted == __pagesPrepped) {
			__pagesPrepped++;
		}
		else {
			__pagesPrinted++;
		}

		// Get the Graphics object which will draw the text that gets printed,
		// and then translate its drawing origin to the paper's printable area.
		// That way (0,0) is ALWAYS the upper-lefthand-most point on the paper.
		Graphics2D g2 = (Graphics2D)g;
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		// Apply clipping to enforce the right and bottom margins.
		Rectangle r = new Rectangle(0, 0,
			(int)pageFormat.getImageableWidth(),
			(int)pageFormat.getImageableHeight());
		g2.clip(r);

		// Calculate the font size necessary to fit X lines on a single page, if it has not been done yet.
		if (__fontSizeCalculated == false) {
			calculateFontSize(g2);
		}

		// If the ReportWriter is to print a test page, do that instead of printing the values in the ta.
		if (__printTestPage == true) {
			createTestPage(g2);
			return PAGE_EXISTS;
		}

		int slop = 0;
		double slop2 = 0;

		Font f = new Font("Monospaced", Font.PLAIN, 8);
		g2.setFont(f);

		if (__header != null) {
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(__header,
				(int)(pageFormat.getImageableWidth() / 2)
				- (fm.stringWidth(__header) / 2), 11);
		}

		// Set up the font.
		f = new Font("Monospaced", Font.PLAIN, __fontSize);
		g2.setFont(f);
		// Print the text for the current page.
		for (int i = 0; i < __linesPerPage; i++) {
			if (((pageIndex * __linesPerPage) + i) >=
				__linesVector.size()) {
				// Break out of this loop.
				i = __linesPerPage + 1;
			} else {
				g2.drawString(
					(String)__linesVector.get(
					(pageIndex * __linesPerPage) + i),
					0, __as + (i * (__as + __ds) + slop +15)
				);
			}
			if (__loc < 0) {
				slop += __slope;
			}
			else if (__loc > 0) {
				slop2 += __slope;
				slop = (int)slop2;
			}
		}

		// Print the "Page" thing at the bottom of the page.
		f = new Font("Monospaced", Font.PLAIN, 8);
		g2.setFont(f);
		FontMetrics fm = g2.getFontMetrics();
		String s = "Page " + (pageIndex + 1) + " of " + __numPages;
		g2.drawString(s,
			(int)(pageFormat.getImageableWidth() / 2)
			- (fm.stringWidth(s) / 2),
			(int)(pageFormat.getImageableHeight() - 3));

		return PAGE_EXISTS;
	}
}

/**
Print a test page with 50 lines per page.
@return the PageFormat set up or used for this print job.
*/
/*
public static PageFormat printTestPage() {
	return printTestPage(50, false, null);
}
*/

/**
Print a test page with the given number of lines per page.
@param linesPerPage the number of lines of text to print per page.
@return the PageFormat set up or used for this print job.
*/
/*
public static PageFormat printTestPage(int linesPerPage) {
	if (linesPerPage < 5) {
		linesPerPage = 5;
	}
	return printTestPage(linesPerPage, false, null);
}
*/

/**
Print a test page with the given number of lines per page, possibly in batch mode, and use the given Page Format.
@param linesPerPage the number of lines to print per page.
@param batch if true, no dialog is brought up to ask the user about printing options.
@param pf the PageFormat to use with this printjob.
@return the PageFormat set up or used for this print job.
*/
/*
public static PageFormat printTestPage(int linesPerPage, boolean batch,
PageFormat pf) {
	if (linesPerPage < 5) {
		linesPerPage = 5;
	}
	ReportPrinter r = new ReportPrinter((Vector)null, linesPerPage, true, batch);
	if (pf != null) {
		r.setPageFormat(pf);
	}
	r.print();
	return r.getPageFormat();
}
*/

/**
Print the text in the text area with the given number of lines per page,
possibly in batch mode, and use the given PageFormat.
@param ta the JTextArea to print out the text for.
@param linesPerPageP the number of lines of text per printed page in portrait orientation.
@param linesPerPageL the number of lines of text per printed page in landscape orientation.
@param batch if true, no dialog will pop up asking the user for print job information like page range and number of copies.
@param pf the PageFormat to use for this print job.
@return the PageFormat set up or used for this print job.
*/
public static PageFormat printText(JTextArea ta, int linesPerPageP,
int linesPerPageL, String header, boolean batch, PageFormat pf) {
	ReportPrinter r = new ReportPrinter(ta, linesPerPageP, linesPerPageL, header, false, batch);
	if (pf != null) {
		r.setPageFormat(pf);
	}
	r.print();
	return r.getPageFormat();
}

/**
Print the text in the Vector with the given number of lines per page,
possibly in batch mode, and use the given PageFormat.
@param v a list of text to print.
@param linesPerPageP the number of lines of text per printed page in portrait orientation.
@param linesPerPageL the number of lines of text per printed page in landscape orientation.
@param batch if true, no dialog will pop up asking the user for print job information like page range and number of copies.
@param pf the PageFormat to use for this print job.
@return the PageFormat set up or used for this print job.
*/
public static PageFormat printText(List<String> v, int linesPerPageP,
int linesPerPageL, String header, boolean batch, PageFormat pf) {
	ReportPrinter r = new ReportPrinter(v, linesPerPageP, linesPerPageL, header, false, batch);
	if (pf != null) {
		r.setPageFormat(pf);
	}
	r.print();
	return r.getPageFormat();
}

/**
Sets the PageFormat for this print job.
@param pf the PageFormat to set for the print job.
*/
public void setPageFormat(PageFormat pf) {
	__pageFormat = pf;
}

/**
Take a JTextArea and turn it into a Vector of Strings, with the default tabstop size of 8.
@param ta the JTextArea to turn into a Vector of Strings.
@return a list of Strings.
*/
private List<String> taToStringList(JTextArea ta) {
	return taToStringList(ta, 8);
}

/**
Take a JTextArea and turn it into a Vector of Strings, with the given
tabstop size.  Each Vector element will be a line in the JTextArea, broken
at a newline. Tabs are translated into spaces based on the size of the tabstop.
@param ta the JTextArea to turn into a Vector of Strings.
@param tabStop the number of spaces to replace each tab with.
@return A list of Strings representing the lines in the JTextArea.
*/
private List<String> taToStringList(JTextArea ta, int tabStop) {
	if (ta == null) {
		return null;
	}
	char[] arr = ta.getText().toCharArray();
	List<String> v = new ArrayList<>();

	String s = "";
	boolean working = false;
	for (int i = 0; i < arr.length; i++) {
		char c = arr[i];
		if (c == '\n') {
			working = false;
			v.add(s);
			s = "";
		}
		else if (c == '\t') {
			working = true;
			for (int j = 0; j < tabStop; j++) {
				s += " ";
			}
		}
		else {
			working = true;
			s += c;
		}
	}
	if (working) {
		v.add(s);
	}
	return v;
}

}