package RTi.Util.IO;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.print.PrintService;
import javax.print.attribute.standard.Media;

import RTi.Util.Message.Message;

// TODO (JTS - 2004-03-30) make it so that paper sizes can be specified for plotter with .X precision

/**
This is a utility class for setting up paper for printing.  An example of use:
<br><pre>
	try {
		PageFormat pageFormat = PrintUtil.getPageFormat("11x17");
		PrintUtil.setPageFormatOrientation(pageFormat, PrintUtil.LANDSCAPE);
		PrintUtil.setPageFormatMargins(pageFormat, .5, 1, 2, 4);
		PrintUtil.print(printable, pageFormat);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
</pre>	
*/
public class PrintUtil
{

/**
Debugging routine.  Prints information about the PageFormat object at status level 2.
*/
public static void dumpPageFormat(PageFormat pageFormat) {
	String routine = "PrintUtil.dumpPageFormat";

	Message.printStatus(2, routine, "" + pageFormatToString(pageFormat));
	Message.printStatus(2, routine, "  Height         : " + pageFormat.getHeight());
	Message.printStatus(2, routine, "  Width          : " + pageFormat.getWidth());
	Message.printStatus(2, routine, "  ImageableHeight: " + pageFormat.getImageableHeight());
	Message.printStatus(2, routine, "  ImageableWidth : " + pageFormat.getImageableWidth());
	Message.printStatus(2, routine, "  ImageableX     : " + pageFormat.getImageableX());
	Message.printStatus(2, routine, "  ImageableY     : " + pageFormat.getImageableY());
	Message.printStatus(1, routine, "  Orient         : " + pageFormat.getOrientation());
}

/**
Returns the orientation in the PageFormat as a string (e.g., "Portrait").
@return the orientation in the PageFormat as a string (e.g., "Portrait").
*/
public static String getOrientationAsString(PageFormat pageFormat) {
	int orientation = pageFormat.getOrientation();
	return getOrientationAsString ( orientation );
}

/**
Returns the orientation in the PageFormat as a string (e.g., "Portrait").
@return the orientation in the PageFormat as a string (e.g., "Portrait").
*/
public static String getOrientationAsString(int orientation)
{
	if ( orientation == PageFormat.LANDSCAPE ) {
		return "Landscape";
	}
	else if ( orientation == PageFormat.PORTRAIT ) {
		return "Portrait";
	}
	else if ( orientation == PageFormat.REVERSE_LANDSCAPE ) {
        return "ReverseLandscape";
    }
	else {
		// Mac-only orientation -- unsupported
		return "Unsupported";
	}
}

// TODO SAM 2011-06-25 When dealing with the StateMod network, need to ensure that the Media.toString() values
// are also checked, to have flexibility
/**
Returns a PageFormat object corresponding to the specified format type.
The margins of the PageFormat object are 0 on all sides.  This is used for getting the sizes of page types.
Possible values are:<br>
<ul>
<li><b>11x17</b> - 11 x 17 inches</li>
<li><b>A</b> - Letter size - 8.5 x 11 inches</li>
<li><b>A3</b> - 11.69 x 16.54 inches</li>
<li><b>A4</b> - 8.27 x 11.69 inches</li>
<li><b>A5</b> - 5.83 x 8.27 inches</li>
<li><b>B</b> - Ledger size - 11 x 17 inches</li>
<li><b>C</b> - 17 x 22 inches</li>
<li><b>D</b> - 22 x 34 inches</li>
<li><b>E</b> - 34 x 44 inches</li>
<li><b>Executive</b> - 7.5 x 10 inches</li>
<li><b>Letter</b> - 8.5 x 11 inches</li>
<li><b>Legal</b> - 8.5 x 14 inches</li>
<li><b>Plotter HxW</b> - Specifies that a plotter page will be used for printing
and that the height of the page will be H inches and the width will be W.</li>
</ul>
<b>Keep in mind that the values specified above are the widths and heights
(respectively) of the pages -- and that these values are for PORTRAIT mode.
If the page is put into LANDSCAPE mode, the widths will become the heights and vice versa.</b>
@param format the name of the format for which to return the page format.
@return the PageFormat that corresponds to the specified format.
*/
public static PageFormat getPageFormat(String format) {
	if (format == null) {
		return null;
	}

	format = format.toLowerCase();

	PageFormat pageFormat = new PageFormat();
	pageFormat.setOrientation(PageFormat.PORTRAIT);
	Paper paper = new Paper();

	if (format.equals("11x17") || format.equals("b")) {
		paper.setSize(792, 1224);
		paper.setImageableArea(0, 0, 792, 1224);
	}
	else if (format.equals("a3")) {
		paper.setSize(841, 1190);
		paper.setImageableArea(0, 0, 841, 1190);
	}
	else if (format.equals("a4")) {
		paper.setSize(595, 841);
		paper.setImageableArea(0, 0, 595, 841);
	}
	else if (format.equals("a5")) {
		paper.setSize(419, 595);
		paper.setImageableArea(0, 0, 419, 595);
	}
	else if (format.equals("c")) {
		paper.setSize(1224, 1584);
		paper.setImageableArea(0, 0, 1224, 1584);
	}
	else if (format.equals("d")) {
		paper.setSize(1584, 2448);
		paper.setImageableArea(0, 0, 1584, 2448);
	}
	else if (format.equals("e")) {
		paper.setSize(2448, 3168);
		paper.setImageableArea(0, 0, 1584, 2448);
	}
	else if (format.equals("executive")) {
		paper.setSize(540, 720);
		paper.setImageableArea(0, 0, 540, 720);
	}
	else if (format.equals("letter") || format.equals("a")) {
		paper.setSize(612, 792);
		paper.setImageableArea(0, 0, 612, 792);
	}
	else if (format.equals("legal")) {
		paper.setSize(612, 1008);
		paper.setImageableArea(0, 0, 612, 1008);
	}
	else if (format.startsWith("plotter")) {
		String size = format.substring(8).trim();
		int loc = size.indexOf("x");
		if (loc < 0 || loc == size.length()) {
			return null;
		}
		String H = size.substring(0, loc);
		String W = size.substring(loc + 1);
		int height = (new Integer(H)).intValue() * 72;
		int width = (new Integer(W)).intValue() * 72;
		paper.setSize(width, height);
		paper.setImageableArea(0, 0, width, height);		
	}
	else {
		return null;
	}

	pageFormat.setPaper(paper);

	return pageFormat;
}

/**
Return the list of supported media for the print service.  If includeNote=false, strings are Media.toString()
(e.g., "na-letter").  If includeNote=True, additional readable equivalents are added
(e.g., "na-letter - North American Letter").
@param printService the print service (printer) for which media sizes are being requested
@param includeSize if false, only the media size name is returned, if true the size is appended after " - "
(e.g., "na-letter - North American Letter - 8.5 x 11 in").  The size information is for information.
THIS IS NOT YET IMPLEMENTED.
*/
public static List<String> getSupportedMediaSizeNames ( PrintService printService,
    boolean includeNote, boolean includeSize )
{
    Media [] supportedMediaArray =
        (Media [])printService.getSupportedAttributeValues(Media.class, null, null);
    List<String> mediaList = new Vector();
    // The list has page sizes (e.g., "na-letter"), trays, and named sizes (e.g., "letterhead")
    // To find only sizes, look up the string in PaperSizeLookup and return matches
    PaperSizeLookup psl = new PaperSizeLookup();
    if ( supportedMediaArray != null ) {
        for ( int i = 0; i < supportedMediaArray.length; i++ ) {
            Media media = supportedMediaArray[i];
            String displayName = psl.lookupDisplayName(media.toString());
            if ( displayName != null ) {
                // Media name matched a paper size (otherwise was tray name, etc.)
                if ( !includeNote || (displayName == null) ) {
                    mediaList.add ( media.toString() );
                }
                else {
                    // TODO SAM 2011-06-25 if displayName is false, remove the trailing " - xxxx" size information
                    mediaList.add(media.toString() + " - " + displayName);
                }
                if ( includeSize ) {
                    // Additionally append the size information
                    // TODO SAM 2011-06-25 Need to enable this
                }
            }
        }
        Collections.sort(mediaList);
    }
    return mediaList;
}

/**
Returns a string representation of the specified pageFormat (e.g., "A3").
@param pageFormat the pageFormat to return a String representation of.
@return a String representation of the specified page format (e.g., "A3").
*/
public static String pageFormatToString(PageFormat pageFormat) {
	int longSide = 0;
	int shortSide = 0;

	if (pageFormat.getHeight() > pageFormat.getWidth()) {
		longSide = (int)pageFormat.getHeight();
		shortSide = (int)pageFormat.getWidth();
	}
	else {
		longSide = (int)pageFormat.getWidth();
		shortSide = (int)pageFormat.getHeight();
	}

	if (longSide == 1224 && shortSide == 792) {
		return "11x17";
	}	
	else if (longSide == 1190 && shortSide == 841) {
		return "A3";
	}
	else if (longSide == 841 && shortSide == 595) {
		return "A4";
	}
	else if (longSide == 595 && shortSide == 419) {
		return "A5";
	}
	else if (longSide == 1584 && shortSide == 1224) {
		return "C";
	}
	else if (longSide == 2448 && shortSide == 1584) {
		return "D";
	}
	else if (longSide == 3168 && shortSide == 2448) {
		return "E";
	}
	else if (longSide == 720 && shortSide == 540) {
		return "Executive";
	}
	else if (longSide == 792 && shortSide == 612) {
		return "Letter";
	}
	else if (longSide == 1008 && shortSide == 612) {
		return "Legal";
	}
	else {
		return "Plotter " + (int)(pageFormat.getWidth() / 72) + "x" + (int)(pageFormat.getHeight() / 72);
	}
}

/**
Prints the print job set up on a Printable class using the specified pageFormat.
@param printable the Printable that will be printing.
@param pageFormat the page format to use for printing the page.
@throws Exception if printable is null.
*/
public static void print(Printable printable, PageFormat pageFormat) 
throws Exception {
	if (printable == null) {
		throw new Exception ("Printable parameter is null");
	}
	PrinterJob pj = PrinterJob.getPrinterJob();
	if (setupPrinterJobPageDialog(pj, printable, pageFormat)) {
		showPrintDialogAndPrint(pj);
	}
}	

/**
Sets up margins on a page format object; 
<b>this method must be called <u>AFTER</u> setPageFormatOrientation() is called</b>!
@param pageFormat the page format to set up margins for.
@param topInches the number of inches of the margin on the top of the page.
@param bottomInches the number of inches of the margin on the bottom of the page.
@param leftInches the number of inches of the margin on the left of the page.
@param rightInches the number of inches of the margin on the right of the page.
@throws Exception if the margins are negative or are too large for the specified page format.
*/
public static void setPageFormatMargins(PageFormat pageFormat,
double topInches, double bottomInches, double leftInches, double rightInches) 
throws Exception {
	Paper paper = pageFormat.getPaper();
	
	double height = pageFormat.getHeight();
	double width = pageFormat.getWidth();
	
	if (topInches < 0) {
		throw new Exception("Top margin is negative.");
	}
	if (bottomInches < 0) {
		throw new Exception("Bottom margin is negative.");
	}
	if (leftInches < 0) {
		throw new Exception("Left margin is negative.");
	}
	if (rightInches < 0) {
		throw new Exception("Right margin is negative.");
	}
	
	if (topInches >= height) {
		throw new Exception("Top margin size in inches (" + topInches + ") is too large compared to the "
			+ "overall height of the page (" + height + ")");
	}

	if (bottomInches >= height) {
		throw new Exception("Bottom margin size in inches (" + bottomInches + ") is too large compared to "
			+ "the overall height of the page (" + height + ")");
	}
	
	if ((topInches + bottomInches) >= height) {
		throw new Exception("Top margin size (" + topInches + ") and bottom margin size (" + bottomInches 
			+ ") combined are greater than the overall height of the page (" + height + ")");
	}

	if (leftInches >= width) {
		throw new Exception("Left margin size in inches (" + leftInches + ") is too large compared to the "
			+ "overall width of the page (" + width + ")");
	}
	
	if (rightInches >= width) {
		throw new Exception("Right margin size in inches (" + rightInches + ") is too large compared to "
			+ "the overall width of the page (" + width + ")");
	}
	
	if ((leftInches + rightInches) >= width) {
		throw new Exception("Left margin size (" + leftInches + ") and right margin size (" 
			+ rightInches + ") combined are greater than the overall width of the page (" + width + ")");
	}

	if (pageFormat.getOrientation() == PageFormat.PORTRAIT) {
		paper.setImageableArea((leftInches * 72), (topInches * 72),
			(width - (leftInches * 72) - (rightInches * 72)),
			(height - (topInches * 72) - (bottomInches * 72)));
	}
	else {
		paper.setImageableArea( (topInches * 72), (rightInches * 72),
			(height - (topInches * 72) - (bottomInches * 72)),
			(width - (leftInches * 72) - (rightInches * 72)));
	}

	pageFormat.setPaper(paper);
}

/**
Sets up the page dialog for entering margins and formats for the page format
and also sets the Printable object for the PrinterJob to use.
@param pj the PrinterJob that will be used to print.
@param printable the Printable interface that will actually print to the page.
@param pf the pageFormat to use (and possibly modify in the PrinterJob's page dialog).
@return true if the printer job page dialog was OK'd and printing can continue,
false if the dialog was CANCEL'd.
*/
public static boolean setupPrinterJobPageDialog(PrinterJob pj, Printable printable, PageFormat pf) {
	PageFormat pageFormat = pj.pageDialog(pf);
	if (pageFormat == pf) {
		return false;
	}
	pj.setPrintable(printable, pageFormat);
	return true;
}

/**
Shows the print dialog (from which a printer can be chosen) and if the print was not cancelled,
prints the page.
@param pj the PrinterJob being used.
@throws Exception if an error occurs.
*/
public static void showPrintDialogAndPrint(PrinterJob pj) 
throws Exception {
	if (pj.printDialog()) {
		pj.print();
	}
}

/**
Sets the page orientation for the PageFormat object.  This must be done before
any call is made to setPageFormatMargins().
@param pageFormat the pageformat for which to set the orientation.
@param orientation the orientation of the page (either PORTRAIT or LANDSCAPE).
*/
public static void setPageFormatOrientation(PageFormat pageFormat, int orientation) {
	pageFormat.setOrientation(orientation);
}

/**
Sets the page orientation for the PageFormat object.  This must be done before
any call is made to setPageFormatMargins().
@param pageFormat the page format for which to set the orientation.
@param orientation the orientation of the page (either "Portrait" or "Landscape").
@throws Exception if an invalid orientation is passed in.
*/
public static void setPageFormatOrientation(PageFormat pageFormat, String orientation) 
throws Exception {
	if (orientation.equalsIgnoreCase("Landscape")) {		
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
	}
	else if (orientation.equalsIgnoreCase("Portrait")) {
		pageFormat.setOrientation(PageFormat.PORTRAIT);
	}
    else if (orientation.equalsIgnoreCase("ReverseLandscape")) {
        pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
    }
	else {
		throw new Exception ("Invalid orientation: '" + orientation + "'");
	}
}

}