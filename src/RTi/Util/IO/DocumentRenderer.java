// DocumentRenderer - prints objects of type Document

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

/*****************************************************************************
* DocumentRenderer.java
* Authors: Kei G. Gauthier, Stephen E. Sugermeyer, KAT
* This class is a helper class to print documents.  By calling the correct
* method you can print from a JEditorPane (html|text) or File.
* It has functions to open print dialogs and scale before printing.  
* 
* This document was obtained from the Web from the site: 
* http://www.fawcette.com/javapro/2002_12/online/print_kgauthier_12_10_02
* It is a freely available document and is mainly being used for the
* HTML and JEditorPane printing and scaling.
*
*****************************************************************************
* Revisions
* 2007-04-17	Kurt Tometich, RTi		Added some methods to format the
* 									HTML content type JEditorPane when printed
* 									because the original print scale function
* 									was based on image scaling.  This caused the
* 									image to shrink to an unreadable size.  The
* 									functions added change HTML style and line
* 									length to provide a more readable,
* 									text-wrapped printout.
*/
package RTi.Util.IO;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;

public class DocumentRenderer implements Printable {
/*  DocumentRenderer prints objects of type Document. Text attributes, including
    fonts, color, and icons, will be rendered to a printed page.
    DocumentRenderer computes line breaks, paginates, and performs other
    formatting.

    An HTMLDocument is printed by sending it as an argument to the
    print(HTMLDocument) method. A PlainDocument is printed the same way. Other
    types of documents must be sent in a JEditorPane as an argument to the
    print(JEditorPane) method. Printing Documents in this way will automatically
    display a print dialog.  

    As objects which implement the Printable Interface, instances of the
    DocumentRenderer class can also be used as the argument in the setPrintable
    method of the PrinterJob class. Instead of using the print() methods
    detailed above, a programmer may gain access to the formatting capabilities
    of this class without using its print dialog by creating an instance of
    DocumentRenderer and setting the document to be printed with the
    setDocument() or setJEditorPane(). The Document may then be printed by
    setting the instance of DocumentRenderer in any PrinterJob.
*/
  protected int currentPage = -1;               //Used to keep track of when
                                                //the page to print changes.

  protected JEditorPane jeditorPane;            //Container to hold the
                                                //Document. This object will
                                                //be used to lay out the
                                                //Document for printing.
  												//If the content is html then
  												//the pane contains a graphic
  												//otherwise the text of the
  												//pane is printed.

  protected double pageEndY = 0;                //Location of the current page
                                                //end in pixels.

  protected double pageStartY = 0;              //Location of the current page
                                                //start in pixels.

  protected boolean scaleWidthToFit = true;     //boolean to allow control over
                                                //whether pages too wide to fit
                                                //on a page will be scaled.
  
  //string used to replace the style content of old html file
  // this is needed to format the page for printing
  // If this style is not used then certain strings of rendered
  // HTML will exceed the width of printable area and thus
  // text will be cut off.
  
  // TODO KAT 2007-03-22
  // may need to try to read in the current style and somehow
  // keep some formatting but change formatting of widths.
  private String __style = "<style>\n"
		+ "#titles { font-weight:bold; color:#303044 }\n"
		+ "table { background-color:black; text-align:left;"
		+ " width:500; table-layout:fixed;}\n"  
		+ "th {background-color:#333366; text-align:center;"
		+ " vertical-align:bottom; color:white }\n" 
		+ "td {background-color:white; text-align:center;"
		+ " vertical-align:bottom; word-break:break-all; }\n" 
		+ "hr { width:500; }\n" 
		+ "body { text-align:left; font-size:12 }\n" 
		+ "pre { font-size:12; }\n" 
		+ "p { font-size:12; }\n"
		+ "</style>\n";

  protected PageFormat pFormat;
  protected PrinterJob pJob;

/*  The constructor initializes the pFormat and PJob variables.
*/
  public DocumentRenderer() {
    pFormat = new PageFormat();
    pJob = PrinterJob.getPrinterJob();
  }

  /**
  Creates a formatted printable string from the current JEditor pane
  instance.  If the content type is text then no formatting is needed
  and the original pane is returned.  If the content type is html then
  a newly formatted pane is returned.  This is needed because html is
  rendered as a graphic on the pane and if it does not fit within the 
  boundaries of the printable page it will be cut off.  If it is scaled
  it will be unreadable; therefore, the html is formatted and a new pane
  is rendered for printing.
  @param jep Pane that contains the text or html to print.
  @return If the content type of the JEditorPane is text then the same
  JEditorPane is returned.  If the content type is html then the pane is
  formatted to fit the printable page and is returned.
   */
  private JEditorPane createPrintableJEditorPane( JEditorPane jep )
  {
  	return createPrintableJEditorPane(null, jep );
  }

  /**
  Creates a temporary JEditorPane with formatted text to enable printing
  for the JEditorPane component.
  @param file File to populate the JEditorPane with, using setPage().
  @return JEditorPane formatted for printing.
   */
  private JEditorPane createPrintableJEditorPane( File file, 
  JEditorPane jep )
  {
  	JEditorPane tmpJEP = new JEditorPane();
  	tmpJEP.setEditable(false);	// this is required for the html to be
  								// rendered
  	tmpJEP.setContentType( "text/html" );
  	if ( file == null || !file.exists()) {
  		try {
  			tmpJEP.setPage(jep.getPage());
  		} catch (IOException e1) {
  			return null;
  		}
  	}
  	else {
  		try {	
  			tmpJEP.setPage( "file:" + file );
  		} catch (IOException e) {
  			// new page could not be set so print current one by default
  			try {
  				tmpJEP.setPage(jep.getPage());
  			} catch (IOException e1) {
  				return null;
  			}
  		}
  	}
  	return tmpJEP;
  }

//KAT 2007-04-18
// phased out this method since it took too long to process
// decided to go with getText() method
/**
Reads current file being displayed in the JEditorPane.  If a URL
is being displayed or the file is not available then the text
of the JEditorPane is retrieved.  The only problem with this function
is that it overrides style content from html since some sizes need to be
reset in order for them to fit on the page for printing.
@param Current JEditorPane that was sent for printing.
@return content Contents of the file or text from the JEditorPane.

  private String getOrigFileContents( JEditorPane jep ) 
  {
	 String content = "";
	 boolean inStyle = false;
	  	  
	  // read from the original html file first 
	  try {
		  BufferedReader in = new BufferedReader(
				  new FileReader(jep.getPage().getFile()));
		  String str;
		  while ( (str = in.readLine()) != null ) {
			  if (str.toLowerCase().startsWith("<style>")) {
				  inStyle = true;
				  content += __style;
			  }
			  if ( !inStyle ) {
				  content += (str + "\n");
			  }
			  if ( str.toLowerCase().startsWith("</style>")) {
				  inStyle = false;
			  }
		  }
		  in.close();
		  // Couldn't read from file so use whatever is in the pane since
		  // thats all that's available.  This may cause the print
	  } catch (IOException e) {
		  Message.printWarning(2, "DocumentRenderer.getOrigFileContents",
				"Couldn't read from file:" + jep.getPage().getFile() +
				"\nUsing whatever text is in the current pane.");
		  content = jep.getText();
	  }
return content;
  
  }
  */

  /**
  Formats the input String for printing by adding new lines where appropriate.
  This is only used for HTML content type panes.  If the text is not formatted
  then it will be scaled to a unreadable size since the HTML is rendered as a
  graphic.
  @param orig_contents Contents of the current JEditorPane. 
  @return Formatted String that can be printed without text
  being cutoff or scaled to a unreasonable size.
   */
  private String formatForPrinting( String orig_contents )
  {
	  // inject the new style and change all pre tags to paragraph tags
	  String new_str = orig_contents.replaceAll( "<pre>", "<p>" );
	  String contents = new_str.replaceAll( "</pre>", "</p>" );
	  int beg_index = contents.indexOf("<style");
	  int end_index = contents.indexOf("</head");
	  if ( beg_index != -1 && end_index != -1 ) {
		  String sub_str1 = contents.substring( 0, beg_index );
		  String sub_str2 = contents.substring( end_index );
		  contents = sub_str1 + __style + sub_str2; 
	  }
	  return contents;
	 
	  /**	KAT 2007-04-18  Commented out old way for performance
	  boolean inTag = false;
	  String result = "";
	  String content = orig_contents;
	  String newline = "\r\n";
	  int charCount = 0;
	  // Add format for printing based on char line length
	  // and HTML tags
	  for ( int i = 0; i < content.length(); i++,charCount++ ) {
		  if ( content.charAt(i) == '<' || content.charAt(i) == '{')
			  inTag = true;
		  // 72 is the cutoff for chars per line
		  if ( charCount > 72 && !inTag ) {
			  result += "\n";
			  charCount = 0;
		  }
		  result += content.charAt(i);
		  if ( newline.indexOf( content.charAt(i) ) >= 0 ) 
			  charCount = 0;
		  if ( content.charAt(i) == '>' || content.charAt(i) == '}')
			  inTag = false;
	  }
	  
	  return result;
	  **/
  }

  /**
  Writes out the formatted string to a temporary file for printing.
  A temporary file is made because the HTML must be rendered again
  to ensure a newly formatted pane.  A JEditorPane does not render
  the HTML alone if must be handed off to a rendered which in turns
  takes a file.  This is why a temporary file is written.  Once the file
  has been used to render the HTML for the pane it is deleted.
  @param file File to write the formatted text to. 
  @param output Formatted text to write to the file.
  @throws IOException if the file is not found or is unreadable.
   */
  private void writeTempFileForPrinting( File file, String output ) 
  throws IOException
  {
	  BufferedWriter out = new BufferedWriter(
			  new FileWriter( file ));
	  out.write( output );
	  out.close();
  }

  /**
   Formats a JEditorPane for printing.  If the JEditorPane has content
   of text/html then html text is formatted to fit on a line or is
   wrapped to the next line.  The problem with scaling the image for
   printing with html content caused the image to shrink itself so much
   that the image was not readable.  This is caused by scale formatting
   proportionately horizontal and vertical.  This function formats the
   content such that the scaling is only done horizontally so not all text
   is shrunk.  Lines that exceed the horizontal width of the printable page
   are wrapped.
   @param jep JEditorPane to format.
   @return A JEditorPane formatted for printing inside the
   printable boundaries.
   */
  private JEditorPane formatJEditorPane( JEditorPane jep )
  {
	  String content = jep.getText();
	  String result = formatForPrinting(content);
	  // write tmp file to load into jpane to print
	  // if there is an IOException with writing the file
	  // then just use the text from the JEditorPane
	  String tmpFName = "tmpPrintFile.html";
	  JEditorPane tmpJEP = null;
	  try {
		  writeTempFileForPrinting( new File( tmpFName ), result );
		  tmpJEP = createPrintableJEditorPane( 
				  new File( tmpFName ), jep );
		  new File(tmpFName).delete();
	  } catch (IOException e) {
		  tmpJEP = createPrintableJEditorPane( jep );
	  }
	  return tmpJEP;
  }

  
/**  
 Returns the current Document that is set for the JEditorPane.
 @return Document that was used to populate the JEditorPane.
*/
  public Document getDocument() {
    if (jeditorPane != null) return jeditorPane.getDocument();
    else return null;
  }

/**  
 Returns the current choice of the width scaling option.  This
 option is used to control how much scaling is performed on the
 horizontal axis.
 @return True if the width scaling option is used or false if it is not.
*/
  public boolean getScaleWidthToFit() {
    return scaleWidthToFit;
  }

/**  
 Displays a page setup dialogf for printing.
*/
  public void pageDialog() {
    pFormat = pJob.pageDialog(pFormat);
  }

/**
    The print method implements the Printable interface. Although Printables
    may be called to render a page more than once, each page is painted in
    order; therefore, keep track of changes in the page being rendered
    by setting the currentPage variable to equal the pageIndex, and then
    comparing these variables on subsequent calls to this method. When the two
    variables match, it means that the page is being rendered for the second or
    third time. When the currentPage differs from the pageIndex, a new page is
    being requested.

    The highlights of the process used print a page are as follows:

    I.    The Graphics object is cast to a Graphics2D object to allow for
          scaling.
    II.   The JEditorPane is laid out using the width of a printable page.
          This will handle line breaks. If the JEditorPane cannot be sized at
          the width of the graphics clip, scaling will be allowed.
    III.  The root view of the JEditorPane is obtained. By examining this root
          view and all of its children, printView will be able to determine
          the location of each printable element of the document.
    IV.   If the scaleWidthToFit option is chosen, a scaling ratio is
          determined, and the graphics2D object is scaled.
    V.    The Graphics2D object is clipped to the size of the printable page.
    VI.   currentPage is checked to see if this is a new page to render. If so,
          pageStartY and pageEndY are reset.
    VII.  To match the coordinates of the printable clip of graphics2D and the
          allocation rectangle which will be used to lay out the views,
          graphics2D is translated to begin at the printable X and Y
          coordinates of the graphics clip.
    VIII. An allocation Rectangle is created to represent the layout of the
          Views.

          The Printable Interface always prints the area indexed by reference
          to the Graphics object. For instance, with a standard 8.5 x 11 inch
          page with 1 inch margins the rectangle X = 72, Y = 72, Width = 468,
          and Height = 648, the area 72, 72, 468, 648 will be painted regardless
          of which page is actually being printed.

          To align the allocation Rectangle with the graphics2D object two
          things are done. The first step is to translate the X and Y
          coordinates of the graphics2D object to begin at the X and Y
          coordinates of the printable clip, see step VII. Next, when printing
          other than the first page, the allocation rectangle must start laying
          out in coordinates represented by negative numbers. After page one,
          the beginning of the allocation is started at minus the page end of
          the prior page. This moves the part which has already been rendered to
          before the printable clip of the graphics2D object.

    X.    The printView method is called to paint the page. Its return value
          will indicate if a page has been rendered.
*/
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
    double scale = 1.0;
    Graphics2D graphics2D;
    View rootView;
    //  I
    graphics2D = (Graphics2D) graphics;
    //  II
    jeditorPane.setSize((int) pageFormat.getImageableWidth(),Integer.MAX_VALUE);
    jeditorPane.validate();
    //  III
    rootView = jeditorPane.getUI().getRootView(jeditorPane);
    //  IV
    if ((scaleWidthToFit) && (jeditorPane.getMinimumSize().getWidth() >
    pageFormat.getImageableWidth())) {
      scale = pageFormat.getImageableWidth()/
      jeditorPane.getMinimumSize().getWidth();
      graphics2D.scale(scale,scale);
    }
    //  V
    graphics2D.setClip((int) (pageFormat.getImageableX()/scale),
    (int) (pageFormat.getImageableY()/scale),
    (int) (pageFormat.getImageableWidth()/scale),
    (int) (pageFormat.getImageableHeight()/scale));
    //  VI
    if (pageIndex > currentPage) {
      currentPage = pageIndex;
      pageStartY += pageEndY;
      pageEndY = graphics2D.getClipBounds().getHeight();
    }
    //  VII
    graphics2D.translate(graphics2D.getClipBounds().getX(),
    graphics2D.getClipBounds().getY());
    //  VIII
    Rectangle allocation = new Rectangle(0,
    (int) -pageStartY,
    (int) (jeditorPane.getMinimumSize().getWidth()),
    (int) (jeditorPane.getPreferredSize().getHeight()));
    //  X
    if (printView(graphics2D,allocation,rootView)) {
      return Printable.PAGE_EXISTS;
    }
    else {
      pageStartY = 0;
      pageEndY = 0;
      currentPage = -1;
      return Printable.NO_SUCH_PAGE;
    }
  }

/**
 Sets an HTMLDocument for printing.
 @param An HTMLDocument object.
*/
  public void print(HTMLDocument htmlDocument) {
    setDocument(htmlDocument);
    printDialog();
  }

/**
 Prints a Document contained within a JEDitorPane.
 @param JEditorPane that is to be printed.  If this panes content
 is HTML then it will be formatted to fit the printable page.  If
 it is only text then the text will be printed.
*/
  public void print(JEditorPane jedPane)
  {
	  JEditorPane tmpJEP = null;
	if( jedPane.getContentType().equals("text/html")) {
		tmpJEP = formatJEditorPane( jedPane );
	}
	if ( tmpJEP != null ) {
		setDocument( tmpJEP );
	}
	else {
		setDocument( jedPane );
	}
	printDialog();
  }

/**
 Sets a PlainDocument for printing.
 @param PlainDocument object.
*/
  public void print(PlainDocument plainDocument) {
    setDocument(plainDocument);
    printDialog();
  }

/**
 Displays the print dialog and initiates
 printing in response to user input.
*/
  protected void printDialog() {
    if (pJob.printDialog()) {
      pJob.setPrintable(this,pFormat);
      try {
        pJob.print();
      }
      catch (PrinterException printerException) {
        pageStartY = 0;
        pageEndY = 0;
        currentPage = -1;
        System.out.println("Error Printing Document");
      }
    }
  }

/**
    printView iterates through the tree structure of the view sent to it.
    If the view sent to printView is a branch view, that is one with children,
    the method calls itself on each of these children. If the view is a leaf
    view, that is a view without children which represents an actual piece of 
    text to be painted, printView attempts to render the view to the
    Graphics2D object.

    I.    When any view starts after the beginning of the current printable
          page, this means that there are pages to print and the method sets
          pageExists to true.
    II.   When a leaf view is taller than the printable area of a page, it
          cannot, of course, be broken down to fit a single page. Such a View
          will be printed whenever it intersects with the Graphics2D clip.
    III.  If a leaf view intersects the printable area of the graphics clip and
          fits vertically within the printable area, it will be rendered.
    IV.   If a leaf view does not exceed the printable area of a page but does
          not fit vertically within the Graphics2D clip of the current page, the
          method records that this page should end at the start of the view.
          This information is stored in pageEndY.
*/
  protected boolean printView(Graphics2D graphics2D, Shape allocation,
  View view) {
    boolean pageExists = false;
    Rectangle clipRectangle = graphics2D.getClipBounds();
    Shape childAllocation;
    View childView;

    if (view.getViewCount() > 0 && !(view.getElement().getName().equals("td"))
    		&& !(view.getElement().getName().equals("th"))) {
      for (int i = 0; i < view.getViewCount(); i++) {
        childAllocation = view.getChildAllocation(i,allocation);
        if (childAllocation != null) {
          childView = view.getView(i);
          if (printView(graphics2D,childAllocation,childView)) {
            pageExists = true;
          }
        }
      }
    } else {
    	//  I
      if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
        pageExists = true;
        //  II
        if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
        (allocation.intersects(clipRectangle))) {
          view.paint(graphics2D,allocation);
        } else {
        	//  III
          if (allocation.getBounds().getY() >= clipRectangle.getY()) {
            if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
              view.paint(graphics2D,allocation);
            } else {
            	//  IV
              if (allocation.getBounds().getY() < pageEndY) {
                pageEndY = allocation.getBounds().getY();
              }
            }
          }
        }
      }
    }
    return pageExists;
  }

/**
 Sets the content type for the JEditorPane.
 @param type Content type (text or text/html).
*/
  protected void setContentType(String type) {
    jeditorPane.setContentType(type);
  }

/**  
 Sets an HTMLDocument as the Document to print.
 @param HTMLDocument object.
*/
  public void setDocument(HTMLDocument htmlDocument) {
    jeditorPane = new JEditorPane();
    setDocument("text/html",htmlDocument);
  }

/**
 Sets the Document to print as the one contained in a JEditorPane.
 @param The JEditorPane to get the document from.
*/
  public void setDocument(JEditorPane jedPane) {
    jeditorPane = new JEditorPane();
    setDocument(jedPane.getContentType(),jedPane.getDocument());
  }

/**
 Sets a PlainDocument as the Document to print.
 @param PlainDocument object.
*/
  public void setDocument(PlainDocument plainDocument) {
    jeditorPane = new JEditorPane();
    setDocument("text/plain",plainDocument);
  }

/**
 Sets the content type and document of the JEditorPane.
 @param type Type of the content (text or text/html).
 @param Document object to use.
*/
  protected void setDocument(String type, Document document) {
    setContentType(type);
    jeditorPane.setDocument(document);
  }

/**
 Sets the current choice of the width scaling option.
 @param Whether or not to scale the width.  Scales the width
 if true and doesn't if false;
*/
  public void setScaleWidthToFit(boolean scaleWidth) {
    scaleWidthToFit = scaleWidth;
  }
}
