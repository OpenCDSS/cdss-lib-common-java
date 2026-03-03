// PrintJGUI - print utility

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.PrintJob;

import java.io.IOException;

import java.util.Properties;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This class provides a simple process for printing a vector of strings.  It is
implemented similar to the ExportGUI class.  Printing is accomplished by calling
a static print() method, which displays the standard print dialog for the
platform.  In the future, the PrintJGUI class will provide a GUI to set output
attributes, but Java provides limited print control capabilities as of version
1.1.  A 10-point Courier font is used by default for printing to preserve
spacing and to
allow a fairly wide output to be printed.  60-line output is assumed at this
time since the 1.1 toolkit does not guarantee that requesting the page size
will return a valid answer.
<p>

<b>
At this time, printing to the local machine is always attempted.  Although it
is possible to expor to a browser and then print from the browser, this process
is cumbersome and is not supported.
</b>
*/
public class PrintJGUI
{

private static int	LEFT_BORDER = 36;	// Left border in points
private static int	TOP_BORDER = 15;	// Top border in points
//private static int	BOTTOM_BORDER = 15;	// Bottom border in points

private static JDialog		_parentJDialog;
private static List<String> _export;	// List containing the formatted data to print
private static int		_default_fontsize = 10;
						// Default font size.
private static int		_default_lines_per_page = 60;
						// Default lines per page.
private static int		_fontsize = _default_fontsize;
						// Font size.
private static int		_lines_per_page = _default_lines_per_page;
						// Lines per page.

/**
Print to the local printer given the calling Frame and a vector of String's to
print.  The default 10-point font is used.
@param parent JFrame from which printing occurs.
@param export list of String's to print.
*/
public static void print ( JFrame parent, List<String> export ) {
	print ( parent, export, null );
}

/**
Print to the local printer given the calling Frame, a vector of String's to
print, and the font size.
@param parent JFrame from which printing occurs.
@param export list of String's to print.
@param fontsize Font size to use (points).
*/
public static void print ( JFrame parent, List<String> export, int fontsize )
{	PropList props = new PropList ( "PrintJGUI" );
	props.set ( "FontSize", "" + fontsize );
	print ( parent, export, props );
}

/**
Print to the local printer given the calling Frame, a vector of String's to
print, and a help key to be used with RTi.Util.Help.URLHelp (however, help is
not supported at this time because the standard print dialog is used).
@param parent JFrame from which printing occurs.
@param export list of String's to print.
@param helpkey Help key to use with URLHelp.
@param fontsize Font size for output (font is still fixed-width Courier).
@see RTi.Util.Help.URLHelp
*/
public static void print (	JFrame parent, List<String> export, String helpkey, int fontsize )
{	PropList props = new PropList ( "PrintJGUI" );
	props.set ( "HelpKey", helpkey );
	props.set ( "FontSize", "" + fontsize );
	print ( parent, export, props );
}

/**
Print to the local printer given the calling JFrame, a vector of String's to
print, and a PropList containing modifiers.  
The help key to be used with RTi.Util.Help.URLHelp (however, help is
not supported at this time because the standard print dialog is used).
@param parent JFrame from which printing occurs.
@param export list of String's to print.
@param proplist PropList of properties to modify output.  Valid properties are
as shown below:
<p>

<table width=100% cellpadding=2 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>Font  </b></td>
<td>The font to use for printing.  <b>This is currently ignored (always set
to the default.</b></td>
<td>Courier (Plain)</td>
</tr>

<tr>
<td><b>FontSize</b></td>
<td>The font size for printing (points).</td>
<td>10</td>
</tr>

<tr>
<td><b>LinesPerPage</b></td>
<td>The number of lines per page.
<b>If this is specified, the font height will be determined by dividing
the page height by the number of lines.</b></td>
<td>60</td>
</tr>
</table>
<p>

@see RTi.Util.Help.URLHelp
*/
public static void print ( JFrame parent, List<String> export, PropList proplist )
{	String	routine="PrintJGUI.export";

	_export = export;
	// Do some checks.  It is possible that the Vector has one string that itself needs to be parsed...
	if ( (export != null) && (export.size() == 1) ) {
		_export = StringUtil.breakStringList ( (String)export.get(0), "\n", 0 );
	}

	//
	// determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to print");
		return;
	}

	// If the PropList is null, initialize one so we don't have to
	// constantly check for nulls...

	PropList props = null;
	if ( proplist == null ) {
		props = new PropList ( "PrintJGUI" );
	}
	else {	props = proplist;
	}

	// Now get the properties for use here...

	String prop_value = null;
	prop_value = props.getValue ( "LinesPerPage" );
	if ( prop_value != null ) {
		_lines_per_page = StringUtil.atoi ( prop_value );
		if ( _lines_per_page == 0 ) {
			_lines_per_page = _default_lines_per_page;
		}
	}

	prop_value = props.getValue ( "FontSize" );
	if ( prop_value != null ) {
		_fontsize = StringUtil.atoi( prop_value );
	}
       
	// Always try to print to the local machine.  If we are trusted, we do it.
	try {
		printToLocalPrinter( parent );
	}
	catch ( IOException e ) {
	}
}

/**
Print to the local printer given the calling JFrame, a JTextArea to
print, and a status JTextField.
@param parent JFrame from which printing occurs.
@param status_TextField JTextField to receive status messages.
@param textArea JTextArea to print.
*/
public static void printJTextAreaObject(JFrame parent,
					JTextField status_TextField,
					JTextArea textArea )
{	String 	statusString;		// contains status information
	String	routine = "PrintJGUI.printTextAreaObject()";
			
	// display status
	statusString = "Exporting query results...";		   
	parent.setCursor( new Cursor (Cursor.WAIT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );        
      
	/* This does not work because getText returns a newline-delimited
	** string (one big string) and we need carriage returns on the PC!
	** SAM submitted a bug report (feature request) to JavaSoft on
	** 12 Nov 1997

	// get the formatted export Vector for the list object
	Vector export_Vector = new Vector( 10, 5);
		
	export_Vector.addElement( textArea.getText() );
	**
	** So use the following instead...
	*/

	List<String> exportList = StringUtil.breakStringList ( textArea.getText(), "\n", 0 );
	
	print ( parent, exportList, 8 );	// Use small font
	exportList = null;

	// display status
	statusString = "Finished printing.";		   
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );        
}

//-----------------------------------------------------------------------------
// Notes:	(1)	This function prints the _export Vector to a local
//			printer if running an application.
//		(2)	The following comments apply to the code used in this
//			function.  The code has been modified from the original:
//
//				This example is from the book _Java AWT
//				Reference_ by John Zukowski.   Written by John
//				Zukowski.  Copyright (c) 1997 O'Reilly &
//				Associates.  You may study, use, modify, and
//				distribute this example for any purpose.
//				This example is provided WITHOUT WARRANTY
//				either expressed or
//		(3)	At this time, it seems to be difficult to really know
//			how big the page is that we are printing to.  The
//			page height that we use here may result in extra white
//			space on each page.
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//--------------------------------------------------------------------------
/**
A utility routine used internally in this class.
*/
private static void printToLocalPrinter ( JFrame parent ) throws IOException
{	String	routine="PrintJGUI.printToLocalPrinter";
	int	dl = 20;
		
	// First check to see if we are allowed to print...

	if ( !SecurityCheck.canPrint() ) {
		Message.printWarning ( 1, routine,
		"This application does not have sufficient permissions\n" +
		"to print to the local machine.  Please configure as a\n" +
		"trusted application using your browser security settings.\n");
		throw ( new IOException ( "Security violation" ) );
	}

	// Get a print job...

	PrintJob pjob = parent.getToolkit().getPrintJob(parent, "Printing...", (Properties)null);
	if (pjob == null) {
		/* For now, ignore because the user can cancel...
		Message.printWarning ( 1, routine, "Unable to start print job" );
		throw ( new IOException ( "Security violation" ) );
		*/
		return; // Just return as if we did it.
	}
        Graphics pg = pjob.getGraphics();
        if ( pg == null ) {
		Message.printWarning ( 1, routine, "Unable to get graphics handle for print job" );
		throw ( new IOException ( "Security violation" ) );
	}

	// Print strings to graphics via printjob.  Does not deal with word wrap or tabs

	int pageNum = 1;
	int linesForThisPage = 0;
	//int linesForThisJob = 0;

	String nextLine;

	// Get the printable page height...

	int pageHeight = pjob.getPageDimension().height;
	int printable_pageHeight = pageHeight - TOP_BORDER; // Maximum coordinate (NOT PRINTABLE HEIGHT)
	int pageWidth = pjob.getPageDimension().width;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Page dimensions are: width=" + pageWidth + " height=" + pageHeight );
	}

	// Have to set the font to get any output.  For now use a fixed-width
	// font.  If the number of lines were specified, scale the font
	// accordingly.  Otherwise, use the requested font size.

/* For now always go with the font size.
	if ( _lines_per_page_specified ) {
		_fontsize = pageHeight/_lines_per_page;
	}
*/
	// Else, font size has been specified or is defaulted...

	//Font helv = new Font("Helvetica", Font.PLAIN, 12);
	Font font = new Font("Courier", Font.PLAIN, _fontsize);

	pg.setFont (font);
	FontMetrics fm = pg.getFontMetrics(font);
	int fontHeight = fm.getHeight();
	int fontDescent = fm.getDescent();
	int curHeight = TOP_BORDER;

	// Print the lines.  Remember that y=0 at the top of the page.

	int size = _export.size();
	try {
	    for ( int i=0; i < size; i++ ) {
			// Don't do a trim() here because it may shift the line if there are leading spaces...
			nextLine = StringUtil.removeNewline ( (String)_export.get(i) );
			if ((curHeight + fontHeight) > printable_pageHeight) {
				// New Page
				if ( Message.isDebugOn ) {
					Message.printDebug ( 30, routine, "" + linesForThisPage + " lines printed for page " + pageNum);
				}
				pageNum++;
				linesForThisPage = 0;
				pg.dispose();
				pg = pjob.getGraphics();
        			if ( pg == null ) {
					Message.printWarning ( 1, routine, "Unable to get graphics handle for print job" );
					throw ( new IOException ( "Security violation" ) );
				}
				pg.setFont (font);
				curHeight = TOP_BORDER;
			}
			curHeight += fontHeight;
			// Printing does not seem to like empty strings...
			if ( (nextLine != null) && (nextLine.length() > 0) ) {
				pg.drawString (nextLine, LEFT_BORDER, (curHeight - fontDescent));
			}
			linesForThisPage++;
			//linesForThisJob++;
		}
	}
	catch ( Throwable t ) {
		t.printStackTrace ();
	}

	// Now get rid of the graphics and end the job...

	pg.dispose();
    pjob.end();
}

/**
Print to the local printer given the calling Frame and a vector of String's to
print.  The default 10-point font is used.
@param parent JDialog from which printing occurs.
@param export list of String to print.
*/
public static void print ( JDialog parent, List<String> export )
{	print ( parent, export, null );
}

/**
Print to the local printer given the calling Frame, a vector of String's to print, and the font size.
@param parent JDialog from which printing occurs.
@param export list of String to print.
@param fontsize Font size to use (points).
*/
public static void print ( JDialog parent, List<String> export, int fontsize )
{	PropList props = new PropList ( "PrintJGUI" );
	props.set ( "FontSize", "" + fontsize );
	print ( parent, export, props );
}

/**
Print to the local printer given the calling Frame, a vector of String's to
print, and a help key to be used with RTi.Util.Help.URLHelp (however, help is
not supported at this time because the standard print dialog is used).
@param parent JDialog from which printing occurs.
@param export list of String to print.
@param helpkey Help key to use with URLHelp.
@param fontsize Font size for output (font is still fixed-width Courier).
@see RTi.Util.Help.URLHelp
*/
public static void print ( JDialog parent, List<String> export, String helpkey, int fontsize )
{	PropList props = new PropList ( "PrintJGUI" );
	props.set ( "HelpKey", helpkey );
	props.set ( "FontSize", "" + fontsize );
	print ( parent, export, props );
}

/**
Print to the local printer given the calling JDialog, a vector of String's to
print, and a PropList containing modifiers.  
The help key to be used with RTi.Util.Help.URLHelp (however, help is
not supported at this time because the standard print dialog is used).
@param parent JDialog from which printing occurs.
@param export list of String's to print.
@param proplist PropList of properties to modify output.  Valid properties are
as shown below:
<p>

<table width=100% cellpadding=2 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>Font  </b></td>
<td>The font to use for printing.  <b>This is currently ignored (always set
to the default.</b></td>
<td>Courier (Plain)</td>
</tr>

<tr>
<td><b>FontSize</b></td>
<td>The font size for printing (points).</td>
<td>10</td>
</tr>

<tr>
<td><b>LinesPerPage</b></td>
<td>The number of lines per page.
<b>If this is specified, the font height will be determined by dividing
the page height by the number of lines.</b></td>
<td>60</td>
</tr>
</table>
<p>

@see RTi.Util.Help.URLHelp
*/
public static void print ( JDialog parent, List<String> export, PropList proplist ) {
	String	routine="PrintJGUI.export";

	_parentJDialog 	= parent;
	_export 	= export;
	// Do some checks.  It is possible that the Vector has one string that
	// itself needs to be parsed...
	if ( (export != null) && (export.size() == 1) ) {
		_export = StringUtil.breakStringList ( (String)export.get(0), "\n", 0 );
	}

	//
	// determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to print");
		return;
	}

	// If the PropList is null, initialize one so we don't have to
	// constantly check for nulls...

	PropList props = null;
	if ( proplist == null ) {
		props = new PropList ( "PrintJGUI" );
	}
	else {	props = proplist;
	}

	// Now get the properties for use here...

	String prop_value = null;
	prop_value = props.getValue ( "LinesPerPage" );
	if ( prop_value != null ) {
		_lines_per_page = StringUtil.atoi ( prop_value );
		if ( _lines_per_page == 0 ) {
			_lines_per_page = _default_lines_per_page;
		}
	}

	prop_value = props.getValue ( "FontSize" );
	if ( prop_value != null ) {
		_fontsize = StringUtil.atoi( prop_value );
	}
       
	// Always try to print to the local machine.  If trusted, do it.
	try {
		printToLocalPrinter( parent );
	}
	catch ( IOException e ) {
	}
}

/**
Print to the local printer given the calling JDialog, a JTextArea to print, and a status JTextField.
@param parent JDialog from which printing occurs.
@param status_TextField JTextField to receive status messages.
@param textArea JTextArea to print.
*/
public static void printJTextAreaObject(JDialog parent, JTextField status_TextField, JTextArea textArea )
{	String 	statusString;		// contains status information
	String	routine = "PrintJGUI.printTextAreaObject()";
			
	// display status
	statusString = "Exporting query results...";		   
	parent.setCursor( new Cursor (Cursor.WAIT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );        
      
	/* This does not work because getText returns a newline-delimited
	** string (one big string) and we need carriage returns on the PC!
	** SAM submitted a bug report (feature request) to JavaSoft on
	** 12 Nov 1997

	// get the formatted export Vector for the list object
	Vector export_Vector = new Vector( 10, 5);
		
	export_Vector.addElement( textArea.getText() );
	**
	** So use the following instead...
	*/

	List<String> exportList = StringUtil.breakStringList ( textArea.getText(), "\n", 0 );
	
	print ( parent, exportList, 8 );	// Use small font
	exportList = null;

	// display status
	statusString = "Finished printing.";		   
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );        
}

//-----------------------------------------------------------------------------
// Notes:	(1)	This function prints the _export Vector to a local
//			printer if running an application.
//		(2)	The following comments apply to the code used in this
//			function.  The code has been modified from the original:
//
//				This example is from the book _Java AWT
//				Reference_ by John Zukowski.   Written by John
//				Zukowski.  Copyright (c) 1997 O'Reilly &
//				Associates.  You may study, use, modify, and
//				distribute this example for any purpose.
//				This example is provided WITHOUT WARRANTY
//				either expressed or
//		(3)	At this time, it seems to be difficult to really know
//			how big the page is that we are printing to.  The
//			page height that we use here may result in extra white
//			space on each page.
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//--------------------------------------------------------------------------
/**
A utility routine used internally in this class.
*/
private static void printToLocalPrinter ( JDialog parent ) throws IOException
{	String	routine="PrintJGUI.printToLocalPrinter";
	int	dl = 20;
		
	// First check to see if we are allowed to print...

	if ( !SecurityCheck.canPrint() ) {
		Message.printWarning ( 1, routine,
		"This application does not have sufficient permissions\n" +
		"to print to the local machine.  Please configure as a\n" +
		"trusted application using your browser security settings.\n");
		throw ( new IOException ( "Security violation" ) );
	}

	// Get a print job...

	JFrame frame = new JFrame();
	java.awt.Dimension d = _parentJDialog.getSize();
	frame.setSize(d.width, d.height);	
	frame.setVisible(true);
	RTi.Util.GUI.JGUIUtil.center(frame);
	frame.setVisible(false);
	String program_name = IOUtil.getProgramName();
	String jobname = "";
	if ( (program_name == null) || program_name.equals("") ) {
	    jobname = IOUtil.getProgramUser() + " print job";
	}
	else {
	    jobname = IOUtil.getProgramUser() + " " + program_name + " print job.";
	}
	PrintJob pjob = parent.getToolkit().getPrintJob(frame, jobname,	(Properties)null);
	frame.dispose();
	if (pjob == null) {
		/* For now, ignore because the user can cancel...
		Message.printWarning ( 1, routine, "Unable to start print job" );
		throw ( new IOException ( "Security violation" ) );
		*/
		return; // Just return as if we did it.
	}
        Graphics pg = pjob.getGraphics();
        if ( pg == null ) {
		Message.printWarning ( 1, routine, "Unable to get graphics handle for print job" );
		throw ( new IOException ( "Security violation" ) );
	}

	// Print strings to graphics via printjob.  Does not deal with word wrap or tabs

	int pageNum = 1;
	int linesForThisPage = 0;
	//int linesForThisJob = 0;

	String nextLine;

	// Get the printable page height...

	int pageHeight = pjob.getPageDimension().height;
	int printable_pageHeight = pageHeight - TOP_BORDER; // Maximum coordinate (NOT PRINTABLE HEIGHT)
	int pageWidth = pjob.getPageDimension().width;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Page dimensions are: width=" + pageWidth + " height=" + pageHeight );
	}

	// Have to set the font to get any output.  For now use a fixed-width
	// font.  If the number of lines were specified, scale the font
	// accordingly.  Otherwise, use the requested font size.

/* For now always go with the font size.
	if ( _lines_per_page_specified ) {
		_fontsize = pageHeight/_lines_per_page;
	}
*/
	// Else, font size has been specified or is defaulted...

	//Font helv = new Font("Helvetica", Font.PLAIN, 12);
	Font font = new Font("Courier", Font.PLAIN, _fontsize);

	pg.setFont (font);
	FontMetrics fm = pg.getFontMetrics(font);
	int fontHeight = fm.getHeight();
	int fontDescent = fm.getDescent();
	int curHeight = TOP_BORDER;

	// Print the lines.  Remember that y=0 at the top of the page.

	int size = _export.size();
	try {
	    for ( int i=0; i < size; i++ ) {
			// Don't do a trim() here because it may shift the line if there are leading spaces...
			nextLine = StringUtil.removeNewline ( (String)_export.get(i) );
			if ((curHeight + fontHeight) > printable_pageHeight) {
				// New Page
				if ( Message.isDebugOn ) {
					Message.printDebug ( 30, routine, "" + linesForThisPage + " lines printed for page " + pageNum);
				}
				pageNum++;
				linesForThisPage = 0;
				pg.dispose();
				pg = pjob.getGraphics();
        			if ( pg == null ) {
					Message.printWarning ( 1, routine, "Unable to get graphics handle for print job" );
					throw ( new IOException ( "Security violation" ) );
				}
				pg.setFont (font);
				curHeight = TOP_BORDER;
			}
			curHeight += fontHeight;
			// Printing does not seem to like empty strings...
			if ( (nextLine != null) && (nextLine.length() > 0) ) {
				pg.drawString (nextLine, LEFT_BORDER,(curHeight - fontDescent));
			}
			linesForThisPage++;
			//linesForThisJob++;
		}
	}
	catch ( Throwable t ) {
		t.printStackTrace ();
	}

	// Now get rid of the graphics and end the job.

	pg.dispose();
    pjob.end();
}

}