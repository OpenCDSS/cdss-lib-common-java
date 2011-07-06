package RTi.Util.IO;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.net.URISyntaxException;
import java.util.List;

import javax.print.PrintException;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class provides a way to print a list of strings, with minimal formatting.
A 10-point Courier font is used by default for printing to preserve spacing and to
allow a fairly wide output to be printed.
*/
public class TextPrinterJob extends AbstractPrinterJob implements Pageable, Printable
{

/**
List containing the text strings to print.
*/
private List<String> __textList;

/**
Font used for printing, initialized on first page.
*/
private Font __font = null;

/**
Requested lines per page.
*/
private int __requestedLinesPerPage = 100;

/**
Requested header.
*/
private String __requestedHeader = null;

/**
Requested footer.
*/
private String __requestedFooter = null;

/**
Requested whether to show line count.
*/
private boolean __requestedShowLineCount = false;

/**
Requested whether to show page count.
*/
private boolean __requestedShowPageCount = false;

/**
Printing a list of strings by constructing the printer job.  Default properties are provided but
can be changed in the printer dialog (if not in batch mode).
@param textList list of strings to print
@param reqPrintJobName the name of the print job (default is to use the system default job name)
@param reqPrinterName the name of the requested printer (e.g., \\MyComputer\MyPrinter)
@param reqPaperSize the requested paper size (Media.toString(), MediaSizeName.toString(), e.g., "na-letter")
@param reqPaperSource the requested paper source - not currently supported
@param reqOrientation the requested orientation (e.g., "Portrait", "Landscape"), default is printer default
@param reqMarginLeft the requested left margin in inches, for the orientation
@param reqMarginRight the requested right margin in inches, for the orientation
@param reqMarginTop the requested top margin in inches, for the orientation
@param reqMarginBottom the requested bottom margin in inches, for the orientation
@param reqLinesPerPage the requested lines per page - default is determined from imageable page height
@param reqHeader header to add at top of every page
@param reqFooter footer to add at bottom of every page
@param reqShowLineCount whether to show the line count to the left of lines in output
@param reqShowPageCount whether to show the page count in the footer
@param reqPages requested page ranges, where each integer pair is a start-stop page (pages 0+)
@param reqDoubleSided whether double-sided printing should be used - currently not supported
@param reqPrintFile name of a file to print to (for PDF, etc.), or null if not used.  If specified, a full
path should be given.
@param showDialog if true, then the printer dialog will be shown to change default printer properties
*/
public TextPrinterJob ( List<String> textList, String reqPrintJobName, String reqPrinterName,
    String reqPaperSize, String reqPaperSource, String reqOrientation, double reqMarginLeft,
    double reqMarginRight, double reqMarginTop, double reqMarginBottom, int reqLinesPerPage,
    String reqHeader, String reqFooter,
    boolean reqShowLineCount, boolean reqShowPageCount, int [][] reqPages, boolean reqDoubleSided,
    String reqPrintFile, boolean showDialog )
throws PrinterException, PrintException, URISyntaxException
{   // Most data managed in parent class...
    super ( reqPrintJobName, reqPrinterName,
            reqPaperSize, reqPaperSource, reqOrientation, reqMarginLeft,
            reqMarginRight, reqMarginTop, reqMarginBottom,
            reqPages, reqDoubleSided,
            reqPrintFile, showDialog );
    // Locally relevant data...
    setTextList ( textList );
    setRequestedHeader ( reqHeader );
    setRequestedFooter ( reqFooter );
    setRequestedLinesPerPage ( reqLinesPerPage );
    setRequestedShowLineCount ( reqShowLineCount );
    setRequestedShowPageCount ( reqShowPageCount );
    // Start printing
    super.startPrinting (
        this, // printable
        this ); // pageable
}

/**
Determine the number of lines per page to be printed, based on the printable area and user preference.
Want the font size to be >= 6 (too difficult to read) and <= 12 (starts to look comical)
For some standard page sizes always pick a certain size because the imageable page height
should not vary much from normal defaults.
*/
private int determineLinesPerPage ( PageFormat pageLayout )
{   int linesPerPage = getRequestedLinesPerPage();
    if ( linesPerPage > 0 ) {
        return linesPerPage;
    }
    
    // TODO SAM 2011-06-23 Need to include European sizes
    // Standard page sizes...
    double pageHeight = pageLayout.getHeight();
    if ( (pageHeight > 611.0) && (pageHeight < 613.0) ) {
        // 8.5 in
        return 75;
    }
    else if ( (pageHeight > 791.0) && (pageHeight < 793.0) ) {
        // 11 in
        return 100;
    }
    else if ( (pageHeight > 1223.0) && (pageHeight < 1225.0) ) {
        // 17 in
        return 200;
    }
    // Other page sizes, generally want to use a smaller font if possible
    // Some of these are unlikely but someone may try to print on large media for a presentation
    int [] candidates = { 50, 60, 70, 80, 100, 125, 150, 200, 250, 300, 400, 500 };
    double imageablePageHeight = pageLayout.getImageableHeight();
    for ( int i = candidates.length - 1; i >= 0; i-- ) {
        double fontHeight = imageablePageHeight/candidates[i];
        if ( (fontHeight >= 7.0) && (fontHeight <= 12.0) ) {
            return candidates[i];
        }
    }
    // Default
    return (int)(imageablePageHeight/10.0); // 10 point font
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	__textList = null;
	super.finalize();
}

/**
Return the font.
*/
public Font getFont ()
{
    return __font;
}

/**
Return the number of pages being printed.
*/
public int getNumberOfPages ()
{
    int linesPerPage = determineLinesPerPage ( getPrinterJob().getPageFormat(getPrintRequestAttributes()) );
    int numberOfPages = 0;
    List<String> textList = getTextList();
    if ( textList != null ) {
        numberOfPages = textList.size()/linesPerPage;
        if ( textList.size()%linesPerPage != 0 ) {
            ++numberOfPages;
        }
    }
    return numberOfPages;
}

/**
Return the format to be used for the requested page (in this case the same for all pages).
*/
public PageFormat getPageFormat ( int pageIndex )
{
    return getPrinterJob().getPageFormat(getPrintRequestAttributes());
}

/**
Return the Printable implementation for paging (this class).
*/
public Printable getPrintable ( int pageIndex )
{
    return this;
}

/**
Return the requested footer.
*/
public String getRequestedFooter ()
{
    return __requestedFooter;
}

/**
Return the requested header.
*/
public String getRequestedHeader ()
{
    return __requestedHeader;
}

/**
Return the requested lines per page.
*/
private int getRequestedLinesPerPage ()
{
    return __requestedLinesPerPage;
}

/**
Return whether to show the line count at the bottom of the page.
*/
public boolean getRequestedShowLineCount ()
{
    return __requestedShowLineCount;
}

/**
Return whether to show the page count at the bottom of the page.
*/
public boolean getRequestedShowPageCount ()
{
    return __requestedShowPageCount;
}

/**
Return the list of strings to print.
*/
private List<String> getTextList ()
{
    return __textList;
}

/**
Print the report.
*/
public int print ( Graphics g, PageFormat pageFormat, int pageIndex )
{   String routine = getClass().getName() + ".print";
    int dl = 20;

    Graphics2D g2d = (Graphics2D)g;

    // Get the printable page dimensions (points)...

    double pageHeight = pageFormat.getHeight();
    double imageablePageHeight = pageFormat.getImageableHeight();
    double pageWidth = pageFormat.getWidth();
    double imageablePageWidth = pageFormat.getImageableWidth();
    double imageableX = pageFormat.getImageableX();
    double imageableY = pageFormat.getImageableY();

    if ( Message.isDebugOn ) {
        Message.printDebug ( dl, routine, "Page dimensions are: width=" + pageWidth + " height=" + pageHeight );
    }
    Message.printStatus ( 2, routine, "Page dimensions are: width=" + pageWidth + " height=" + pageHeight );
    Message.printStatus ( 2, routine, "Imageable page dimensions are: width=" + imageablePageWidth + " height=" + imageablePageHeight );
    Message.printStatus ( 2, routine, "Imageable origin: X=" + imageableX + " Y=" + imageableY );
    
    // Use a fixed-width font to properly display fixed-with computer input/output/code.
    // Select a reasonable number of lines based on the printable size and scale the font accordingly.

    int linesPerPage = determineLinesPerPage ( pageFormat );
    String header = getRequestedHeader();
    String footer = getRequestedFooter();
    boolean showPageCount = getRequestedShowPageCount();
    int headerLines = 0; // Lines on page dedicated to header
    int footerLines = 0; // Lines on page dedicated to footer
    if ( (header != null) && (header.length() > 0) ) {
        headerLines = 2;
    }
    if ( ((footer != null) && (footer.length() > 0)) || showPageCount ) {
        footerLines = 2;
    }
    float yDelta = (float)imageablePageHeight/(linesPerPage + headerLines + footerLines);
    Font font = getFont ();
    // If font was not set (this is the first page to print), save it for reuse
    if ( font == null ) {
        double fontHeight = yDelta; // Try 100% fill but reduce later if necessary
        Message.printStatus(2, routine, "Calculated font height is " + fontHeight );
        // Fonts can only have integer height
        font = new Font("Courier", Font.PLAIN, (int)fontHeight);
        setFont ( font );
    }

    g2d.setFont (font);
    g2d.setColor(Color.black);
    FontMetrics fm = g2d.getFontMetrics(font);
    //int fontHeight = fm.getHeight();
    int fontDescent = fm.getDescent();
    //int curHeight = TOP_BORDER;
    
    // Determine the starting line to print based on the requested page
    
    List<String> textList = getTextList();
    int firstLine = pageIndex*linesPerPage;
    if ( firstLine > textList.size() ) {
        return Printable.NO_SUCH_PAGE;
    }
    int lastLine = firstLine + linesPerPage - 1;
    if ( lastLine >= textList.size() ) {
        lastLine = textList.size() - 1;
    }
    
    // Print the header and footer - for now use the same font as the text with one line blank line between
    // header/footer and the main body
    // TODO SAM 2011-06-25 Work on other fonts, etc., to make look nicer

    double headerHeight = 0.0;
    if ( (header != null) && (header.length() > 0) ) {
        headerHeight = yDelta*2.0;
        g2d.drawString (header, (float)imageableX, (float)imageableY + yDelta - fontDescent);
    }
    if ( (footer != null) && (footer.length() > 0) ) {
        g2d.drawString (footer, (float)imageableX, (float)imageableY + (float)imageablePageHeight - fontDescent);
    }
    if ( showPageCount ) {
        // Same line as the footer, but in the middle of the imageable width
        // TODO SAM 2011-06-25 To truly be centered should account for width of text
        g2d.drawString ("- " + (pageIndex + 1) + " -",
            (float)imageableX + (float)imageablePageWidth/2,
            (float)imageableY + (float)imageablePageHeight - fontDescent);
    }

    // Print the lines.  Remember that y=0 at the top of the page.

    float x = (float)imageableX;
    // Initialize vertical position on the page, slightly offset from full line
    // because character is drawn at point of descent
    float y = (float)imageableY + (float)headerHeight + yDelta - fontDescent;
    String textLine;
    boolean showLineCount = getRequestedShowLineCount();
    String lineCountFormat = "%" + ((int)Math.log10(textList.size()) + 1) + "d"; 
    try {
        for ( int iLine=firstLine; iLine <= lastLine; iLine++ ) {
            // Don't do a trim() here because it may shift the line if there are leading spaces...
            textLine = textList.get(iLine);
            if ( showLineCount ) {
                // Prepend the line with the line number and a space
                textLine = StringUtil.formatString((iLine + 1),lineCountFormat) + " " + textLine;
            }
            if ( (textLine != null) && (textLine.length() > 0) ) {
                Message.printStatus(2, routine, "Printing \"" + textLine + "\" at " + x + "," + y );
                g2d.drawString (textLine, (int)x, (int)y);
            }
            y += yDelta;
        }
    }
    catch ( Throwable t ) {
        Message.printWarning(3, routine, t);
    }

    return Printable.PAGE_EXISTS;
}

/**
Set the font to use for printing, initialized on first page.
*/
private void setFont ( Font font )
{
    __font = font;
}

/**
Set the requested footer.
*/
private void setRequestedFooter ( String requestedFooter )
{
    __requestedFooter = requestedFooter;
}

/**
Set the requested header.
*/
private void setRequestedHeader ( String requestedHeader )
{
    __requestedHeader = requestedHeader;
}

/**
Set the requested lines per page.
*/
private void setRequestedLinesPerPage ( int requestedLinesPerPage )
{
    __requestedLinesPerPage = requestedLinesPerPage;
}

/**
Set whether to show the line count.
*/
private void setRequestedShowLineCount ( boolean reqShowLineCount )
{
    __requestedShowLineCount = reqShowLineCount;
}

/**
Set whether to show the page count.
*/
private void setRequestedShowPageCount ( boolean reqShowPageCount )
{
    __requestedShowPageCount = reqShowPageCount;
}

/**
Set the list of strings to print.
*/
private void setTextList ( List<String> textList )
{
    __textList = textList;
}

}