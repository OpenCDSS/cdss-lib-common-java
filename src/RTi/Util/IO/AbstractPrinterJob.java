package RTi.Util.IO;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;
import java.net.URISyntaxException;

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

/**
This abstract provides basic functionality to set up and execute a PrinterJob in batch or interactive mode.
This class should be extended by a class that implements Printable and optionally Pageable.
The constructor should be called and then startPrinting().
*/
public class AbstractPrinterJob
{
    
/**
The instance of Printable to print.
*/
//private Printable __printable = null;

/**
The instance of Pageable to print.
*/
//private Pageable __pageable = null;

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
Private constructor to protect abstract status.
*/
@SuppressWarnings("unused")
private AbstractPrinterJob ()
{
}

/**
Construct a printer job.  Default properties are provided but
can be changed in the printer dialog (if not in batch mode).
@param reqPrintJobName the name of the print job (default is to use the system default job name)
@param reqPrinterName the name of the requested printer (e.g., \\MyComputer\MyPrinter)
@param reqPaperSize the requested paper size (Media.toString(), MediaSizeName.toString(), e.g., "na-letter")
@param reqPaperSource the requested paper source - not currently supported
@param reqOrientation the requested orientation (e.g., "Portrait", "Landscape"), default is printer default
@param reqMarginLeft the requested left margin in inches, for the orientation
@param reqMarginRight the requested right margin in inches, for the orientation
@param reqMarginTop the requested top margin in inches, for the orientation
@param reqMarginBottom the requested bottom margin in inches, for the orientation
@param reqPages requested page ranges, where each integer pair is a start-stop page (pages 0+)
@param reqDoubleSided whether double-sided printing should be used - currently not supported
@param reqPrintFile name of a file to print to (for PDF, etc.), or null if not used.  If specified, a full
path should be given.
@param showDialog if true, then the printer dialog will be shown to change default printer properties
*/
public AbstractPrinterJob ( String reqPrintJobName, String reqPrinterName,
    String reqPaperSize, String reqPaperSource, String reqOrientation, double reqMarginLeft,
    double reqMarginRight, double reqMarginTop, double reqMarginBottom, int [][] reqPages, boolean reqDoubleSided,
    String reqPrintFile, boolean showDialog )
throws PrinterException, PrintException, URISyntaxException
{
    //setPrintable ( printable );
    //setPageable ( pageable );
    setRequestedPrintJobName ( reqPrintJobName );
    setRequestedPrinterName ( reqPrinterName );
    setRequestedPaperSize ( reqPaperSize );
    // paper source
    setRequestedOrientation ( reqOrientation );
    setRequestedMarginLeft ( reqMarginLeft );
    setRequestedMarginRight ( reqMarginRight );
    setRequestedMarginTop ( reqMarginTop );
    setRequestedMarginBottom ( reqMarginBottom );
    setRequestedPages ( reqPages );
    setRequestedDoubleSided ( reqDoubleSided );
    setRequestedPrintFile ( reqPrintFile );
    setShowDialog ( showDialog );
}

/**
Return the format to be used for the requested page (in this case the same for all pages).
*/
/*
public PageFormat getPageFormat ( int pageIndex )
{
    return getPrinterJob().getPageFormat(getPrintRequestAttributes());
}
*/

/**
Return the Pageable implementation.
*/
/*
public Pageable getPageable ()
{
    return __pageable;
}
*/

/**
Return the Printable implementation.
*/
/*
public Printable getPrintable ()
{
    return __printable;
}
*/

/**
Return the print job.
*/
/*
public PrinterJob getPrinterJob ()
{
    return __printerJob;
}*/

/**
Return the print job.
*/
public PrinterJob getPrinterJob ()
{
    return __printerJob;
}

/**
Return the printer request attributes.
*/
public PrintRequestAttributeSet getPrintRequestAttributes ()
{
    return __printRequestAttributes;
}

/**
Return whether double-sided was requested.
*/
public boolean getRequestedDoubleSided ()
{
    return __requestedDoubleSided;
}



/**
Return the requested margin, bottom.
*/
public double getRequestedMarginBottom ()
{
    return __requestedMarginBottom;
}

/**
Return the requested margin, left.
*/
public double getRequestedMarginLeft ()
{
    return __requestedMarginLeft;
}

/**
Return the requested margin, right.
*/
public double getRequestedMarginRight ()
{
    return __requestedMarginRight;
}

/**
Return the requested margin, top.
*/
public double getRequestedMarginTop ()
{
    return __requestedMarginTop;
}

/**
Return the requested orientation.
*/
public String getRequestedOrientation ()
{
    return __requestedOrientation;
}

/**
Return the requested paper size.
*/
public String getRequestedPaperSize ()
{
    return __requestedPaperSize;
}

/**
Return the requested pages.
*/
public int [][] getRequestedPages ()
{
    return __requestedPages;
}

/**
Return the requested printer name.
*/
public String getRequestedPrinterName ()
{
    return __requestedPrinterName;
}

/**
Return the requested print file.
*/
public String getRequestedPrintFile ()
{
    return __requestedPrintFile;
}

/**
Return the requested print job name.
*/
public String getRequestedPrintJobName ()
{
    return __requestedPrintJobName;
}

/**
Return the whether printing is in batch mode.
*/
public boolean getShowDialog ()
{
    return __showDialog;
}

/**
Set the Pageable instance.
*/
/*
private void setPageable ( Pageable pageable )
{
    __pageable = pageable;
}
*/

/**
Set the Printable instance.
*/
/*
private void setPrintable ( Printable printable )
{
    __printable = printable;
}*/

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
Set whether the print dialog should be shown.
*/
private void setShowDialog ( boolean showDialog )
{
    __showDialog = showDialog;
}

/**
Start the printing by setting up the printer job and calling its print() method
(which calls the print() method in the instances that are passed in.
@param printable an instance of Printable, to format content for printing
@param pageable an instance of Pageable, to handle multiple-page content
*/
public void startPrinting ( Printable printable, Pageable pageable )
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
    
    // TODO SAM 2011-06-25 The following commented code generally returned NO printers
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
    setPrinterJob ( printerJob );
    // Tell the printer job what is printing
    printerJob.setPrintable(printable);
    if ( pageable != null ) {
        printerJob.setPageable(pageable);
    }
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
        // Paper source - TODO SAM 2011-06-25 need to enable paper source (tray)
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
        // FIXME SAM 2011-07-01 The dialog may be slow (up to 30 seconds) as per:
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6539061
        // Hopefully this will be fixed in an upcoming release
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