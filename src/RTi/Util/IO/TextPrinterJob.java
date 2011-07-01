package RTi.Util.IO;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class provides a way to print a list of strings, with minimal formatting.
A 10-point Courier font is used by default for printing to preserve spacing and to
allow a fairly wide output to be printed.
*/
public class TextPrinterJob implements Pageable, Printable
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
Whether printing should show the print dialog to adjust printer job properties.
*/
private boolean __showDialog = false;

/**
The PrinterJob used for printing, initialized in startPrinting().
*/
private PrinterJob __printerJob = null;

/**
The print request attributes, initialized in startPrinting().
*/
private PrintRequestAttributeSet __printRequestAttributes = null;

// Requested printer attributes, specified by calling code...

/**
Print job name.
*/
private String __requestedPrinterName = null;

/**
Print job name.
*/
private String __requestedPrintJobName = null;

/**
Page size.
*/
private String __requestedPaperSize = null;

/**
Page orientation.
*/
private String __requestedOrientation = null;

/**
Margin, left.
*/
private double __requestedMarginLeft = .75;

/**
Margin, right.
*/
private double __requestedMarginRight = .75;

/**
Margin, top.
*/
private double __requestedMarginTop = .75;

/**
Margin, bottom.
*/
private double __requestedMarginBottom = .75;

// TODO SAM 2011-06-30 possible allow user to specify units
/**
Margin, units.
*/
//private int __requestedMarginUnits = MediaPrintableArea.INCH;

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
Requested pages to print.
*/
private int [][] __requestedPages = null;

/**
Requested print file to write.
*/
private String __requestedPrintFile = null;

/**
Requested whether to print double-sided.
*/
private boolean __requestedDoubleSided = false;

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
{
    setTextList ( textList );
    setRequestedPrintJobName ( reqPrintJobName );
    setRequestedPrinterName ( reqPrinterName );
    setRequestedPaperSize ( reqPaperSize );
    // paper source
    setRequestedOrientation ( reqOrientation );
    setRequestedMarginLeft ( reqMarginLeft );
    setRequestedMarginRight ( reqMarginRight );
    setRequestedMarginTop ( reqMarginTop );
    setRequestedMarginBottom ( reqMarginBottom );
    setRequestedLinesPerPage ( reqLinesPerPage );
    setRequestedHeader ( reqHeader );
    setRequestedFooter ( reqFooter );
    setRequestedShowLineCount ( reqShowLineCount );
    setRequestedShowPageCount ( reqShowPageCount );
    setRequestedPages ( reqPages );
    setRequestedDoubleSided ( reqDoubleSided );
    setRequestedPrintFile ( reqPrintFile );
    setShowDialog ( showDialog );
    startPrinting ();
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
Return the print job.
*/
private PrinterJob getPrinterJob ()
{
    return __printerJob;
}

/**
Return the printer request attributes.
*/
private PrintRequestAttributeSet getPrintRequestAttributes ()
{
    return __printRequestAttributes;
}

/**
Return whether double-sided was requested.
*/
private boolean getRequestedDoubleSided ()
{
    return __requestedDoubleSided;
}

/**
Return the requested footer.
*/
private String getRequestedFooter ()
{
    return __requestedFooter;
}

/**
Return the requested header.
*/
private String getRequestedHeader ()
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
Return the requested margin, bottom.
*/
private double getRequestedMarginBottom ()
{
    return __requestedMarginBottom;
}

/**
Return the requested margin, left.
*/
private double getRequestedMarginLeft ()
{
    return __requestedMarginLeft;
}

/**
Return the requested margin, right.
*/
private double getRequestedMarginRight ()
{
    return __requestedMarginRight;
}

/**
Return the requested margin, top.
*/
private double getRequestedMarginTop ()
{
    return __requestedMarginTop;
}

/**
Return the requested orientation.
*/
private String getRequestedOrientation ()
{
    return __requestedOrientation;
}

/**
Return the requested paper size.
*/
private String getRequestedPaperSize ()
{
    return __requestedPaperSize;
}

/**
Return the requested pages.
*/
private int [][] getRequestedPages ()
{
    return __requestedPages;
}

/**
Return the requested printer name.
*/
private String getRequestedPrinterName ()
{
    return __requestedPrinterName;
}

/**
Return the requested print file.
*/
private String getRequestedPrintFile ()
{
    return __requestedPrintFile;
}

/**
Return the requested print job name.
*/
private String getRequestedPrintJobName ()
{
    return __requestedPrintJobName;
}

/**
Return the requested line count.
*/
private boolean getRequestedShowLineCount ()
{
    return __requestedShowLineCount;
}

/**
Return the requested page count.
*/
private boolean getRequestedShowPageCount ()
{
    return __requestedShowPageCount;
}

/**
Return the whether printing is in batch mode.
*/
private boolean getShowDialog ()
{
    return __showDialog;
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
Set the printer job.
*/
private void setPrinterJob ( PrinterJob printerJob )
{
    __printerJob = printerJob;
}

/**
Set the printer request attributes.
*/
private void setPrintRequestAttributes ( PrintRequestAttributeSet printRequestAttributes )
{
    __printRequestAttributes = printRequestAttributes;
}

/**
Set the whether double sided was requested.
*/
private void setRequestedDoubleSided ( boolean requestedDoubleSided )
{
    __requestedDoubleSided = requestedDoubleSided;
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
Set the requested margin, bottom.
*/
private void setRequestedMarginBottom ( double requestedMarginBottom )
{
    __requestedMarginBottom = requestedMarginBottom;
}

/**
Set the requested margin, left.
*/
private void setRequestedMarginLeft ( double requestedMarginLeft )
{
    __requestedMarginLeft = requestedMarginLeft;
}

/**
Set the requested margin, bottom.
*/
private void setRequestedMarginRight ( double requestedMarginRight )
{
    __requestedMarginRight = requestedMarginRight;
}

/**
Set the requested margin, top.
*/
private void setRequestedMarginTop ( double requestedMarginTop )
{
    __requestedMarginTop = requestedMarginTop;
}

/**
Set the requested orientation.
*/
private void setRequestedOrientation ( String requestedOrientation )
{
    __requestedOrientation = requestedOrientation;
}

/**
Set the requested page size.
*/
private void setRequestedPaperSize ( String requestedPaperSize )
{
    __requestedPaperSize = requestedPaperSize;
}

/**
Set the requested pages.
*/
private void setRequestedPages ( int [][] requestedPages )
{
    __requestedPages = requestedPages;
}

/**
Set the requested printer name.
*/
private void setRequestedPrinterName ( String requestedPrinterName )
{
    __requestedPrinterName = requestedPrinterName;
}

/**
Set the requested print file to write.
*/
private void setRequestedPrintFile ( String requestedPrintFile )
{
    __requestedPrintFile = requestedPrintFile;
}

/**
Set the name of the print job.
*/
private void setRequestedPrintJobName ( String requestedPrintJobName )
{
    __requestedPrintJobName = requestedPrintJobName;
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
Set whether the print dialog should be shown.
*/
private void setShowDialog ( boolean showDialog )
{
    __showDialog = showDialog;
}

/**
Set the list of strings to print.
*/
private void setTextList ( List<String> textList )
{
    __textList = textList;
}

/**
Start the printing by setting up the printer job and calling its print() method
(which calls the print() method in this class.
*/
private void startPrinting ()
throws PrintException, PrinterException, URISyntaxException
{   String routine = getClass().getName() + ".startPrinting";
    
    /*
    StopWatch sw = new StopWatch();
    sw.start();
    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    sw.stop();
    Message.printStatus(2,routine,"Took " + sw.getSeconds() + " seconds to get print services." );
    */
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    /*
    DocFlavor[] supportedFlavors = defaultPrintService.getSupportedDocFlavors();
    for ( int i = 0; i < supportedFlavors.length; i++ ) {
        Message.printStatus(2, routine, "Supported flavor for " + defaultPrintService.getName() +
            ":  " + supportedFlavors[i] );
    }
    */

    // Get available printers...
    
    // TODO SAM 2011-06-25
    // The following commented code generally returned NO printers
    // And, because formatting below uses Graphics, it is necessary to use something other than
    // a text printer.  Leave in the code for illustration for now, but take out once print code
    // has been used for awhile.
    /*
    PrintService printService = ServiceUI.printDialog(
       null, // To select screen (parent frame?) - null is use default screen
       100, // x location of dialog on screen
       100, // y location of dialog on screen
       services, // Printer services to browse
       defaultPrintService, // default (initial) service to display
       null, // the document flavor to print (null for all)
       printRequestAttributes ); // Number of copies, orientation, etc. - will be updated
    */
   /* This should work but does not automatically format text and printer does not support text flavor
    DocPrintJob job = printService.createPrintJob();
    // Create the document to print
    Doc myDoc = new SimpleDoc(
        getText(), // Text to print
        DocFlavor.STRING.TEXT_PLAIN, // Document (data object) flavor
        null ); // Document attributes
    if ( printService != null ) {
        // Null indicates cancel
        job.print(myDoc, printRequestAttributes);
    }
    */
    
    // Try going the PrinterJob route which should be compatible with PrintService
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    // Tell the printer job what is printing
    printerJob.setPrintable(this);
    printerJob.setPageable(this);
    // TODO SAM 2011-06-23 enable Pageable for more flexibility printing
    // Get the list of all print services available for the print job...
    PrintService [] printServices = PrinterJob.lookupPrintServices();
    if ( printServices.length > 0 ) {
        // Select a specific printer to use
        PrintService printService = null;
        // Make sure that the requested printer name is matched
        String reqPrinterName = getRequestedPrinterName();
        Message.printStatus(2,routine,"Requested printer name is \"" + reqPrinterName + "\"" );
        if ( (reqPrinterName == null) || (reqPrinterName.length() == 0) ) {
            // Specific printer was not requested so use default
            printService = defaultPrintService;
        }
        else {
            // Find the matching printer
            for ( int i = 0; i < printServices.length; i++ ) {
                Message.printStatus(2,routine,"Comparing requested printer name to \"" + printServices[i].getName() + "\"" );
                if ( printServices[i].getName().equalsIgnoreCase(reqPrinterName) ) {
                    printService = printServices[i];
                    break;
                }
            }
            if ( printService == null ) {
                // Requested printer was not matched
                throw new PrintException ( "Unable to locate printer \"" + reqPrinterName + "\"" );
            }
        }
        // Pre-populate some attributes, in the order of the constructor parameters
        PrintRequestAttributeSet printRequestAttributes = new HashPrintRequestAttributeSet();
        // Print job name
        String reqPrintJobName = getRequestedPrintJobName();
        if ( reqPrintJobName != null ) {
            printRequestAttributes.add(new JobName(reqPrintJobName,null));
        }
        // Page size - Media.toString()
        String paperSize = getRequestedPaperSize();
        PaperSizeLookup psl = null; // Can also be reused below for margins
        if ( (paperSize != null) && (paperSize.length() > 0) ) {
            psl = new PaperSizeLookup();
            // Look up the media value from the string
            Media media = psl.lookupMediaFromName ( printService, paperSize );
            if ( media == null ) {
                throw new PrintException ( "Printer does not have media available:  \"" + paperSize + "\"" );
            }
            printRequestAttributes.add(media);
        }
        // Paper source - TODO SAM 2011-06-25 need to enable
        // Orientation
        String orientation = getRequestedOrientation();
        if ( (orientation != null) && (orientation.length() > 0) ) {
            if ( orientation.equalsIgnoreCase("Landscape") ) {
                printRequestAttributes.add(OrientationRequested.LANDSCAPE);
            }
            else if ( orientation.equalsIgnoreCase("Portrait") ) {
                printRequestAttributes.add(OrientationRequested.PORTRAIT);
            }
        }
        // Margins - can't set margins because API wants context based on page size, so specify as imageable area
        // compared to paper size
        // From MediaPrintableArea doc:  "The rectangular printable area is defined thus: The (x,y) origin is 
        // positioned at the top-left of the paper in portrait mode regardless of the orientation specified in
        // the requesting context. For example a printable area for A4 paper in portrait or landscape
        // orientation will have height > width. 
        float marginLeft = (float)getRequestedMarginLeft();
        float marginRight = (float)getRequestedMarginRight();
        float marginTop = (float)getRequestedMarginTop();
        float marginBottom = (float)getRequestedMarginBottom();
        //Message.printStatus ( 2, routine, "Requested margins left=" + marginLeft + " right=" + marginRight +
        //    " top="+ marginTop + " bottom=" + marginBottom );
        if ( (marginLeft >= 0.0) && (marginRight >= 0.0) && (marginTop >= 0.0) && (marginBottom >= 0.0) &&
            (psl != null)) { // psl being instantiated means the paper size was specified - required to set margins
            // Check the orientation.  If landscape, the paper is rotated 90 degrees clockwise,
            // and the user-specified margins need to be converted to the portrait representations
            // for the following code
            if ( (orientation != null) && (orientation.length() > 0) && orientation.equalsIgnoreCase("Landscape")) {
                // Convert the requested margins (landscape) to portrait
                float marginLeftOrig = marginLeft;
                float marginRightOrig = marginRight;
                float marginTopOrig = marginTop;
                float marginBottomOrig = marginBottom;
                marginLeft = marginTopOrig;
                marginRight = marginBottomOrig;
                marginBottom = marginLeftOrig;
                marginTop = marginRightOrig;
            }
            // The media size always comes back in portrait mode
            // TODO SAM 2011-06-26 Need to figure out portrait and landscape for the large paper sizes
            MediaSizeName mediaSizeName = psl.lookupMediaSizeNameFromString(paperSize);
            MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaSizeName);
            //Message.printStatus(2, routine, "paper size for \"" + paperSize + "\" is " + mediaSize );
            float pageWidth = mediaSize.getX(MediaSize.INCH);
            float pageHeight = mediaSize.getY(MediaSize.INCH);
            //Message.printStatus ( 2, routine, "Media size width = " + pageWidth + " height=" + pageHeight );
            // Imageable area is the total page minus the margins, with the origin at the top left
            MediaPrintableArea area = new MediaPrintableArea( marginLeft, marginTop,
                (pageWidth - marginLeft - marginRight),(pageHeight - marginTop - marginBottom),MediaSize.INCH);
            printRequestAttributes.add(area);
            //Message.printStatus(2,routine,"MediaPrintableArea x=" + area.getX(MediaSize.INCH) +
            //    " y=" + area.getY(MediaSize.INCH) + " width=" + area.getWidth(MediaSize.INCH) +
            //    " height=" + area.getHeight(MediaSize.INCH) );
        }
        // Pages
        int [][] reqPages = getRequestedPages();
        if ( reqPages != null ) {
            printRequestAttributes.add(new PageRanges(reqPages));
        }
        // Print file (e.g., used with PDF)
        // TODO SAM 2011-07-01 Going through this sequence seems to generate a warning about
        // fonts needing to be included, but there is no way to edit native printer properties to do this - ARG!!!
        // Microsoft XPS and image files seem to work OK.
        String reqPrintFile = getRequestedPrintFile();
        if ( reqPrintFile != null ) {
            File f = new File(reqPrintFile);
            printRequestAttributes.add(new Destination(f.toURI()));
            Message.printStatus(2, routine, "Printing to file \"" + f.toURI() + "\"" );
        }
        // Double-sided TODO SAM 2011-06-25 This is actually more complicated with short-edge, long-edge, etc.
        boolean reqDoubleSided = getRequestedDoubleSided();
        if ( reqDoubleSided ) {
            printRequestAttributes.add(Sides.DUPLEX);
        }
        if ( getShowDialog() ) {
            // User may want to modify the print job properties
            // Now let the user interactively review and edit...
            Message.printStatus(2,routine,"Opening print dialog for printer \"" + printService.getName() + "\"" );
            printService = ServiceUI.printDialog(
                null, // To select screen (parent frame?) - null is use default screen
                100, // x location of dialog on screen
                100, // y location of dialog on screen
                printServices, // Printer services to browse
                printService, // initial service to display
                null, // the document flavor to print (null for all)
                printRequestAttributes ); // Number of copies, orientation, etc. - will be updated
        }
        if ( printService != null ) { // If null the print job was canceled
            // Save the PrinterJob and attributes for use elsewhere in this class
            setPrinterJob ( printerJob );
            setPrintRequestAttributes ( printRequestAttributes );
            // User has selected a printer, so set for the job
            printerJob.setPrintService ( printService );
            // Now print with the print job assets that may have been defined in the print dialog...
            printerJob.print(printRequestAttributes);
        }
    }
}

}