package RTi.Util.IO;

import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.net.URISyntaxException;

import javax.print.PrintException;

/**
This class provides a way to print graphics-oriented product, with minimal formatting.
*/
public class GraphicsPrinterJob extends AbstractPrinterJob
{

/**
Print a page by constructing the printer job.  Default properties are provided but
can be changed in the printer dialog (if not in batch mode).
@param printable the Printable object to print
@param reqPrintJobName the name of the print job (default is to use the system default job name)
@param reqPrinterName the name of the requested printer (e.g., \\MyComputer\MyPrinter)
@param reqPaperSize the requested paper size (Media.toString(), MediaSizeName.toString(), e.g., "na-letter")
@param reqPaperSource the requested paper source - not currently supported
@param reqOrientation the requested orientation (e.g., "Portrait", "Landscape"), default is printer default
@param reqMarginLeft the requested left margin in inches, for the orientation
@param reqMarginRight the requested right margin in inches, for the orientation
@param reqMarginTop the requested top margin in inches, for the orientation
@param reqMarginBottom the requested bottom margin in inches, for the orientation
@param reqPrintFile name of a file to print to (for PDF, etc.), or null if not used.  If specified, a full
path should be given.
@param showDialog if true, then the printer dialog will be shown to change default printer properties
*/
public GraphicsPrinterJob ( Printable printable, String reqPrintJobName, String reqPrinterName,
    String reqPaperSize, String reqPaperSource, String reqOrientation, double reqMarginLeft,
    double reqMarginRight, double reqMarginTop, double reqMarginBottom,
    String reqPrintFile, boolean showDialog )
throws PrinterException, PrintException, URISyntaxException
{   // Most data managed in parent class...
    super ( reqPrintJobName, reqPrinterName,
            reqPaperSize, reqPaperSource, reqOrientation, reqMarginLeft,
            reqMarginRight, reqMarginTop, reqMarginBottom,
            null, false, reqPrintFile, showDialog );
    // Start printing
    super.startPrinting (
        printable, // printable
        null ); // pageable
}

}