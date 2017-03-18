//------------------------------------------------------------------------------
// HTMLPage - interface to parse and create a HTML page
//------------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	For the most part, this class is used to print HTML pages, e.g.,
//		for CGI interfaces.  Be very careful using printStatus because
//		it may print to the HTML page!
//------------------------------------------------------------------------------
// History:
//
// 05 Feb 1999	Steven A. Malers, RTi	Initial version.
// 08 Mar 1999	SAM, RTi		Add form, table capabilities.
//------------------------------------------------------------------------------

package RTi.Util.Net;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import RTi.Util.Message.Message;

/**
This class provides an interface to parse and print HTML pages, e.g., for
CGI programs.  Currently the class does not automatically detect errors (e.g.,
it will not automatically insert a head if you print a body before the head).
You must insert the page
segments programatically by calling the proper methods.  For example, call using
the following sequence:
<p>
<pre>
	String outfile = new String ( "CGI.out" );
	HTMLPage htmlpage = new HTMLPage ( outfile );
	htmlpage.printContentType();	// If doing CGI.
	htmlpage.printHTML(true);	// Start HTML.
	htmlpage.printHead(true);	// Start Head.
	htmlpage.printTitle("CGI Web Interface");
					// Title.
	htmlpage.printHead(false);	// End Head.
	htmlpage.printBody(true);	// Start body.
	htmlpage.printPreformattedText(true);
					// Start pre-formatted text.
	// Print content here using htmlpage.print(), etc..
	htmlpage.printPreformattedText(false);
					// End pre-formatted text.
	htmlpage.printBody(false);	// End body.
	htmlpage.printHTML(false);	// End HTML page.
	htmlpage.close();		// Close HTML file.
</pre>
*/
public class HTMLPage
{

// Data Members...

private PrintWriter _out = null;	// PrintWriter for output.
private String _htmlfile = null;	// HTML file for output.
//private static String _nl = System.getProperty ( "line.separator" );
private static String _nl = "\n";
					// Use the UNIX variant for now.

// Constructors...

/**
Construct an HTMLPage given a PrintWriter to write the page.
@param out PrintWriter to write HTML to.
@exception IOException if the PrintWriter is null.
*/
public HTMLPage ( PrintWriter out )
throws IOException
{
	if ( out == null ) {
		String message = "PrintWriter for HTMLPage is null.";
		String routine = "HTMLPage(PrintWriter)";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	_out = out;
}

/**
Construct an HTMLPage given the name of a file to write.
@param htmlfile File to write HTML to.
@exception IOException if the file name is null or cannot be opened.
*/
public HTMLPage ( String htmlfile )
throws IOException
{	String message, routine = "HTMLPage(String)";

	if ( htmlfile == null ) {
		message = "HTML file name is null.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
		
	}
	else if ( htmlfile.equals("stdout") ) {
		// Request that standard output is used for output...
		try {	if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Writing HTML to standard output." );
			}
			_out = new PrintWriter(System.out);
			_htmlfile = htmlfile;
		}
		catch ( Exception e ) {
			message = "Cannot create PrintWriter for System.out.";
			Message.printWarning ( 2, routine, message );
			throw new IOException ( message );
		}
	}
	else {	try {	if ( Message.isDebugOn ) {
				Message.printDebug ( 1, routine,
				"Writing HTML to file \"" + htmlfile + "\"." );
			}
			_out = new PrintWriter(new FileWriter(htmlfile));
			_htmlfile = htmlfile;
		}
		catch ( Exception e ) {
			message = "Cannot open HTML file \"" + htmlfile + "\".";
			Message.printWarning ( 2, routine, message );
			throw new IOException ( message );
		}
	}
}

/**
Flush and close the page.
*/
public void close ()
{
	if ( _out != null ) {
		_out.flush();
		_out.close();
	}
}

/**
Clean up instance before garbage collection.
*/
protected void finalize ()
throws Throwable
{
	// Only close the HTML file if it was originally opened with the name.
	// Otherwise, the code that created the instance may have other plans
	// for the PrintWriter.

	if ( _htmlfile != null ) {
		if ( _out != null ) {
			close ();
		}
		_htmlfile = null;
	}
	_out = null;
	super.finalize();
}

/**
Print text to the HTML page.
@param string String to print.
*/
public void print ( String string )
{
	if ( string != null ) {
		_out.print ( string + _nl );
	}
}

/**
Print text to the HTML page.
@param strings list of Strings to print.
*/
public void print ( List<String> strings )
{
	if ( strings != null ) {
		int size = strings.size();
		String string = null;
		for ( int i = 0; i < size; i++ ) {
			string = strings.get(i);
			if ( string != null ) {
				_out.print ( string + _nl );
			}
		}
	}
}

/**
Print a comment the HTML page.
@param string String to print.
*/
public void printComment ( String string )
{
	_out.print ( "<!-- " );
	if ( string != null ) {
		_out.print ( string );
	}
	_out.print ( " -->" + _nl );
}

/**
Print a comment to the HTML page.
@param strings list of String to print.
*/
public void printComment ( List<String> strings )
{
	_out.print ( "<!--" + _nl );
	print ( strings );
	_out.print ( "-->" + _nl );
}

/**
Print the MIME text "Content-type:  text/html" to the HTML page.
*/
public void printContentType ()
{
	_out.print ( "Content-type: text/html" + _nl + _nl );
}

/**
Print a BODY tag.
@param start If true, start the body.  If false, end the body.
*/
public void printBody ( boolean start )
{
	if ( start ) {
		_out.print ( "<BODY>" + _nl );
	}
	else {	_out.print ( "</BODY>" + _nl );
	}
}

/**
Print a FORM tag.
@param start If true, start form.  If false, end form.
*/
public void printForm ( boolean start )
{
	printForm ( start, "" );
}

/**
Print a FORM tag.
@param start If true, start form.  If false, end form.
@param modifiers Modifiers to included in the FORM tag (e.g., "action=XX").
You need to understand HTML syntax because the string is not checked for
errors.
*/
public void printForm ( boolean start, String modifiers )
{
	if ( start ) {
		if ( modifiers != null ) {
			if ( !modifiers.equals("") ) {
				_out.print ( "<FORM " + modifiers + ">" + _nl);
			}
		}
		else {	_out.print ( "<FORM>" + _nl );
		}
	}
	else {	_out.print ( "</FORM>" + _nl );
	}
}

/**
Print a HEAD tag.
@param start If true, start the header.  If false, end the header.
*/
public void printHead ( boolean start )
{
	if ( start ) {
		_out.print ( "<HEAD>" + _nl );
	}
	else {	_out.print ( "</HEAD>" + _nl );
	}
}

/**
Print an HTML tag.
@param start If true, start the HTML page.  If false, end the HTML page.
*/
public void printHTML ( boolean start )
{
	if ( start ) {
		_out.print ( "<HTML>" + _nl );
	}
	else {	_out.print ( "</HTML>" + _nl );
	}
}

/**
Print a PRE tag.
@param start If true, start preformatted text.  If false, end preformatted
text.
*/
public void printPreformattedText ( boolean start )
{
	if ( start ) {
		_out.print ( "<PRE>" + _nl );
	}
	else {	_out.print ( "</PRE>" + _nl );
	}
}

/**
Print pre-formatted text.
@param string String to print.
@param surround If true, surround with PRE HTML tags.
*/
public void printPreformattedText ( String string, boolean surround )
{
	if ( surround ) {
		_out.print ( "<PRE>" + _nl );
	}
	if ( string != null ) {
		_out.print ( string + _nl );
	}
	if ( surround ) {
		_out.print ( "</PRE>" + _nl );
	}
}

/**
Print pre-formatted text.
@param strings Vector of strings to print.
@param surround If true, surround with PRE HTML tags.
*/
public void printPreformattedText ( List<String> strings, boolean surround )
{
	if ( surround ) {
		_out.print ( "<PRE>" + _nl );
	}
	print ( strings );
	if ( surround ) {
		_out.print ( "</PRE>" + _nl );
	}
}

/**
Print a SELECT tag.
@param start If true, start select.  If false, end select.
*/
public void printSelect ( boolean start )
{
	printSelect ( start, "" );
}

/**
Print a SELECT tag.
@param start If true, start select.  If false, end select.
@param modifiers Modifiers to included in the SELECT tag (e.g., "width=XX").
You need to understand HTML syntax because the string is not checked for
errors.
*/
public void printSelect ( boolean start, String modifiers )
{
	if ( start ) {
		if ( modifiers != null ) {
			if ( !modifiers.equals("") ) {
				_out.print ( "<SELECT " + modifiers + ">" +_nl);
			}
		}
		else {	_out.print ( "<SELECT>" + _nl );
		}
	}
	else {	_out.print ( "</SELECT>" + _nl );
	}
}

/**
Print a TABLE tag.
@param start If true, start table.  If false, end table.
*/
public void printTable ( boolean start )
{
	printTable ( start, "" );
}

/**
Print a TABLE tag.
@param start If true, start table.  If false, end table.
@param modifiers Modifiers to included in the TABLE tag (e.g., "width=XX").
You need to understand HTML syntax because the string is not checked for
errors.
*/
public void printTable ( boolean start, String modifiers )
{
	if ( start ) {
		if ( modifiers != null ) {
			if ( !modifiers.equals("") ) {
				_out.print ( "<TABLE " + modifiers + ">" + _nl);
			}
		}
		else {	_out.print ( "<TABLE>" + _nl );
		}
	}
	else {	_out.print ( "</TABLE>" + _nl );
	}
}

/**
Print a TD tag.
@param start If true, start table cell.  If false, end table cell.
*/
public void printTableCell ( boolean start )
{
	printTableCell ( start, "" );
}

/**
Print a TD tag.
@param start If true, start table cell.  If false, end table cell.
@param modifiers Modifiers to included in the TD tag (e.g., "width=XX").
You need to understand HTML syntax because the string is not checked for
errors.
*/
public void printTableCell ( boolean start, String modifiers )
{
	if ( start ) {
		if ( modifiers != null ) {
			if ( !modifiers.equals("") ) {
				_out.print ( "<TD " + modifiers + ">" + _nl);
			}
		}
		else {	_out.print ( "<TD>" + _nl );
		}
	}
	else {	_out.print ( "</TD>" + _nl );
	}
}

/**
Print a TR tag.
@param start If true, start table row.  If false, end table row.
*/
public void printTableRow ( boolean start )
{
	printTableRow ( start, "" );
}

/**
Print a TR tag.
@param start If true, start table row.  If false, end table row.
@param modifiers Modifiers to included in the TR tag (e.g., "width=XX").
You need to understand HTML syntax because the string is not checked for
errors.
*/
public void printTableRow ( boolean start, String modifiers )
{
	if ( start ) {
		if ( modifiers != null ) {
			if ( !modifiers.equals("") ) {
				_out.print ( "<TR " + modifiers + ">" + _nl);
			}
		}
		else {	_out.print ( "<TR>" + _nl );
		}
	}
	else {	_out.print ( "</TR>" + _nl );
	}
}

/**
Print a title to the HTML page.  If the title is null, an empty title is used.
*/
public void printTitle ( String title )
{
	_out.print ( "<TITLE>" );
	if ( title != null ) {
		_out.print ( title );
	}
	_out.print ( "</TITLE>" + _nl );
}

} // End of HTMLPage class
