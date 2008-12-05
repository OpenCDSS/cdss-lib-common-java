//-----------------------------------------------------------------------------
// ExportGUI - export to file utility
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// Notes:	(1)	This GUI accepts a vector of strings and exports the
//			strings to the local file system.  If the file cannot
//			be written, a warning is printed and the file is not
//			written.
//-----------------------------------------------------------------------------
// History:
//
// 21 Nov 1997	Steven A. Malers, RTi	Implement this as a simple version of
//					the HBExportGUI.
// 14 Mar 1998	SAM, RTi		Add Java documentation.
// 07 Dec 1999	SAM, RTi		Remove import * and add data member so
//					that the save directory is remembered.
//					This minimizes the need for user
//					navigation.
// 18 May 2001	SAM, RTi		Use the GUIUtil.lastFileDialogDirectory
//					information for picking the directory.
//-----------------------------------------------------------------------------

package RTi.Util.IO;

import RTi.Util.GUI.GUIUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.SecurityCheck;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
This class provides a generic way to export a vector of strings to a file.  The
static export method is called, which creates a file selector for the export.
At some point, this code will be able to export to a browser (when running as
an applet), but Java security often prevents this from being done easily.
At this time, an export to the local file system is always attempted, with
security exceptions handled.
*/
public class ExportGUI {

//-----------------------------------------------------------
//  Data Members
//-----------------------------------------------------------            

private static Frame            _parent;	// Frame class making the call
						// to HBExportGUI
private static List           _export;	// Vector containing the formatted data to export

/* support later... 
These are now in IOUtil so take out of here when ready
private static boolean          _isApplet;	// true if running an Applet,
						// false otherwise
private static AppletContext    _appletContext; // current applet context
private static URL              _documentBase;	// complete URL of the HTML
						// file that loaded the applet.
*/

//-----------------------------------------------------------------------------
//  Notes:
//  This function may be called from any class which extends Frame. 
//  The appropriate export functionality is invoked depending upon whether 
//  an application or Applet calls this function.              
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//
//          (I)parent - Frame class from which this function is called.
//          (I)export - Vector containing the information to export.
//          (I)isApplet - true if running an Applet, false otherwise.
//          (I)appletContext - current applet context.     
//          (I)documentBase - complete URL of the HTML file that loaded the 
//		applet.  
//----------------------------------------------------------------------------

/**
Export a vector of strings given the parent frame.
@param parent Frame that calls this routine.
@param export list of strings to export.
*/
public static void export ( Frame parent, List export )
{
	export ( parent, export, "" );
}

/**
Export a vector of strings given the parent frame and a help key for a help
button.
@param parent Frame that calls this routine.
@param export list of strings to export.
@param helpkey Help key string for use with RTi.Util.Help.URLHelp (this is
reserved for future use.
@see RTi.Util.Help.URLHelp
*/
public static void export( Frame parent, List export, String helpkey )
{	String	routine="ExportGUI.export";

	_parent 	= parent;
	_export 	= export;
/* support later.
	_isApplet 	= isApplet;               
	_appletContext 	= appletContext;     
	_documentBase 	= documentBase;  
*/
       
	//
	// determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to export");
		return;
	}

	// We always try to write to the local disk.  If we are trusted, we
	// do it.  Otherwise, we try to write to the server if an applet.
	try {	exportToLocalDrive();
	}
	catch ( IOException e ) {
		//
		// Try export to browser if running from an Applet.
		//
		if ( IOUtil.isApplet() ) {
		Message.printWarning ( 1, routine,
		"This application does not have sufficient permissions\n" +
		"to write to the local machine.  Please configure as a\n" +
		"trusted application using your browser security settings.\n");
		}

/* Later we might allow output to a browser...
		if ( isApplet ) {

			if ( Message.isDebugOn ) {
				Message.printDebug( 10, routine,
				"Going to exportToBrowser()" );
			}
			try {	exportToBrowser();
			}
			catch ( IOException e2 ) {
				Message.printWarning ( 1, routine,
				"Cannot export to web server");
			}
		}
*/
	}
}

//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accepts a  TextArea. 
//  (2)calls getListContents( MultiList list ) to get a formatted export Vector
//  (3)calls export( Frame parent, Vector export )
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)parent - parent Frame object from which this call was made.
//  (I)status_TextField - status TextField to display statis to. May be null
//  (I)textArea - TextArea object
//-----------------------------------------------------------------------------

/**
Export a TextArea of strings given the parent frame a TextField to receive
status messages.
@param parent Frame that calls this routine.
@param status_TextField TextField to receive messages as the TextArea is
exported.
@param textArea TextArea to export.
*/
public static void exportTextAreaObject(	Frame parent,
						TextField status_TextField,
						TextArea textArea )
{	String 	statusString;		// contains status information
	String	routine = "ExportGUI.exportTextAreaObject()";
			
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
		
	export_Vector.addElement( new String( textArea.getText() ) );
	**
	** So use the following instead...
	*/

	List export_Vector = StringUtil.breakStringList ( textArea.getText(), "\n", 0 );
	
	// export to file/browser page     
	export( parent, export_Vector );

	// display status
	statusString = "Finished exporting.";		   
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );        
 
	return;
}

//-----------------------------------------------------------------------------
//  Notes:
//  This function exports the _export Vector to a LOCAL FILE if running an 
//  application.
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//-----------------------------------------------------------------------------

/**
Export to a file on the local drive.
*/
private static void exportToLocalDrive() throws IOException
{	FileDialog fd;		// FileDialog Object
	PrintWriter oStream;	// DataOutputStream Object
	String	routine="ExportGUI.exportToLocalDrive";
		
	// Instantiate a file dialog object with export.txt as 
	// the default filename. 

	fd = new FileDialog(_parent, "Export", FileDialog.SAVE);
	String last_directory_selected = GUIUtil.getLastFileDialogDirectory();
	if ( last_directory_selected != null ) {
		fd.setDirectory ( last_directory_selected );
	}
	fd.setFile("export.txt");
	fd.setVisible(true);

	// Determine the name of the export file as specified from 
	// the FileDialog object        

	String fileName = fd.getDirectory() + fd.getFile();        

	// return if no file name is selected

	if ( fd.getFile() == null || fd.getFile().equals("") ) {
		return;
	}
	if ( fd.getDirectory() != null ) {
		GUIUtil.setLastFileDialogDirectory(fd.getDirectory() );
	}

	if (fileName != null) {

		// First see if we can write the file given the security
		// settings...

		if ( !SecurityCheck.canWriteFile(fileName) ) {
			Message.printWarning ( 1, routine,
			"Cannot save \"" + fileName + "\".");
			throw new IOException (
				"Security check failed - unable to write \"" +
				fileName + "\"" );
		}

		// We are allowed to write the file so try to do it...

		_parent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		//
		// Create a new FileOutputStream wrapped with a DataOutputStream
		// for writing to a file.
		try {
			oStream = new PrintWriter( new FileWriter( fileName ) );
			//
			// Write each element of the _export Vector to a file.
			//
			// For some reason, when just using println in an
			// applet, the cr-nl pair is not output like it should
			// be on Windows95.  Java Bug???
			String linesep = System.getProperty("line.separator");
			/* For debugging...
			Vector v;
			v = StringUtil.showControl(linesep);
			oStream.print ( "Separator:" + linesep );
			for ( int j = 0; j < v.size(); j++ ) {
				oStream.print ((String)v.elementAt(j) +
				linesep );
			}
			*/
			for (int i=0; i<_export.size(); i++) {
				oStream.print( _export.get(i).toString() + linesep );
				/* For debugging...
				v = StringUtil.showControl(	_export.elementAt(i).toString() );
				for ( int j = 0; j < v.size(); j++ ) {
					oStream.print ((String)v.elementAt(j) + linesep );
				}
				*/
			}
			//
			// close the PrintStream Object
			//
			oStream.flush(); 
			oStream.close(); 
		}
		catch (IOException IOError) {
			Message.printWarning( 1, routine,
			"Trouble opening or writing to file \"" + 
			fileName + "\"." );
			_parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			throw new IOException (
				"Trouble opening or writing to file \"" + 
				fileName + "\"." );
		}

		_parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	Message.printStatus( 1, routine,
	"Successfully exported data to \"" + fileName + "\"." );

	return;
}

/* Make available later...

//-----------------------------------------------------------------------------
//  Notes:
//  This function exports the _export Vector to a BROWSER page if running an 
//  Applet.
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//-----------------------------------------------------------------------------                        
private static void exportToBrowser() throws IOException
{
	Socket	s = null;
	String	fileName="", response, routine="HBExport.exportToBrowser", 
		//server="arkansas.riverside.com";
		server=HBSource.getDBHost();

	try {
		s = new Socket( server, 5150 );
	}catch ( IOException e ){
		Message.printWarning( 1, routine,
		"Unable to connect to server: " + server );
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug( 2, routine,
		"Successfully established connection with: " + server );
	}

	try {
		PrintStream out;
		DataInputStream din;
		out = new PrintStream(s.getOutputStream());
		din = new DataInputStream(s.getInputStream());

		//
		// Send the Password across.
		//
		out.print("ex&or!\n");
		//
		// Wait for the response.
		//
		response = din.readLine();

		if( !response.equals( "Password OK" ) ){
			Message.printWarning( 1, routine,
		"Invalid response \"" + response + "\" retured." );
			throw( 
			new IOException( "Password not accepted." ) );
		}
		//
		// Build the file name out of the program name, 
		// user name, and a time stamp.
		//
		String date = "{0,date,yyyy.MM.dd.HH.mm.ss}";
		MessageFormat mf = new MessageFormat( date );
		Object[] o = new Object [ 1 ];
		o[0]	= new Date();

		fileName = 	
			HBGUIApp.getProgramName() + "." +
			HBGUIApp.getLogin() + "." +
			mf.format(o).toString() + ".txt";

		if ( Message.isDebugOn ) {
			Message.printDebug( 10, routine,
			"Sending across file name \"" + fileName + "\"." );
		}
		//
		// Send the file name.
		//
		out.print(  fileName + "\n" );
		//
		// Wait for the response
		//
		response = din.readLine();

		if( !response.equals( "Filename OK" ) ){
			Message.printStatus(1, routine,
		"Invalid response \"" + response + "\" retured." );
			throw( 
			new IOException( "Filename not accepted." ) );
		}
		if ( Message.isDebugOn ) {
			Message.printDebug( 10, routine,
			"Printing contents of Vector to file." );
		}

		for( int i=0; i<_export.size(); i++ ){
			out.print( _export.elementAt(i).toString() );
		}

	}catch( IOException e ){
	}finally {
		try {
			s.close();
		}
		catch ( IOException ie ){
		}
	}

	//
	// Send new page to the parent window.
	//
	try {
		String string = "http://" + server + "/tmp/" + fileName;
		URL url = new URL( string );
		if ( Message.isDebugOn ) {
			Message.printDebug( 10, routine,
			"Attempting to show document: " + string );
		}

		_appletContext.showDocument( url, "_blank" );
	}
	catch (MalformedURLException Excep) {
		Message.printWarning( 1, routine,
		"Problem showing exported page to browser!" );
		Message.printWarning( 1, routine, Excep.toString() );
		return;
	}
	return;
}
*/

/* Enable later.  This is specific to a Symantec multilist...
//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accepts a  MultiList object. 
//  (2)calls getListContents( MultiList list ) to get a formatted export Vector
//  (3)calls export( Frame parent, Vector export )
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)parent - parent Frame object from which this call was made.
//  (I)satus_TextField - status TextField to display statis to. May be null
//  (I)list - MultiList object
//-----------------------------------------------------------------------------                        
public static void exportListObject (	Frame parent,
					TextField status_TextField,
					MultiList list )
{	String 	statusString;		// contains status information
        int 	numRows;		// number of list rows  	
			
	numRows = list.getNumberOfRows();

	// display status
	statusString = "Exporting query results...";		   
	parent.setCursor( new Cursor(Cursor.WAIT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, "HBExportGUI.exportListObject()", statusString );        
      
	// get the formatted export Vector for the list object
	Vector export_Vector = new Vector( numRows, 5);
	getListContents( list, export_Vector );

	// export to file/browser page     
	export( parent, export_Vector );

	// display status
	statusString = "Finished exporting.";		   
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, "HBExportGUI.exportListObject()", statusString );        
 
	return;
}

/* Maybe support this later - this is specific to a Symantec multilst.

//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accepts two MultiList objects. 
//  (2)calls getListContents( MultiList list ) to get a formatted export Vector
//  (3)calls export( Frame parent, Vector export )
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)parent - parent Frame object from which this call was made.
//  (I)satus_TextField - status TextField to display statis to. May be null
//  (I)first_list - MultiList object
//  (I)second_list - MultiList object
//-----------------------------------------------------------------------------                        
public static void exportListObject (	Frame parent,
					TextField status_TextField,
					MultiList first_list,
					MultiList second_list )
{	String 	statusString;		// contains status information
        int 	totRows;		// total number of rows
			
	totRows = first_list.getNumberOfRows() + second_list.getNumberOfRows();

	// display status
	statusString = "Exporting query results...";		   
	parent.setCursor( new Cursor(Cursor.WAIT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, "HBExportGUI.exportListObject()", statusString );        
      
	// get the formatted export Vector for each  list object
	Vector export_Vector = new Vector( totRows, 5);
	getListContents( first_list, export_Vector );
	getListContents( second_list, export_Vector );

	// export to file/browser page     
	export( parent, export_Vector );

	// display status
	statusString = "Finished exporting.";		   
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_TextField != null ) {
		status_TextField.setText( statusString );
	}
	Message.printStatus( 1, "HBExportGUI.exportListObject()", statusString );        
 
	return;
}
*/

/* maybe support this later
//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accept a MultiList object and formats a Vector containing
//  headings and list contents for exporting. 
//  (2)columns are deliminated via the user preference deliminator.
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)list - MultiList object
//  (O)returns the headings and contents of the MultiList object as formatted
//  for exporting. Columns are deliminated via the user preferenece export deliminator		
//-----------------------------------------------------------------------------                        
private static void getListContents( MultiList list, Vector export_Vector ) {
        String[] 	headings,		// list headings
			listItems;              // list items. Columns are 
						// deliminated via ';'
	String 		formatString,		// formatted export String
			delim;			// export deliminator
        int 		numRows,		// number of list rows  	
			curRow,			// row counter
			curCol,			// column counter
			size;			// Vector and String[] sizes
	Vector 		row_Vector;		// contains columns for the cuRow as elements

	// get the export deliminator
	delim = HBGUIApp.getValue("RunTime.ExportDelimiter").trim();
	if ( delim.equals("[TAB]") ) {
		delim = "\t";
	}

	// add headings to Vector
	// NOTE: headings are NOT deliminated when returned from the list object
	// get the list headings, list items, and number of rows
	headings = list.getHeadings();
        listItems = list.getListItems();
	numRows = list.getNumberOfRows();
	size = headings.length;
	formatString = "";
	for ( curCol=0; curCol < size; curCol++ ) {
		formatString = formatString 
				+ headings[curCol].trim() 
				+ delim;
	}
       	export_Vector.addElement(new String( formatString + "\n" ));         	

        // add list items to Vector
	// NOTE: list ARE deliminated when returned from the list object via ';'
        for (curRow=0; curRow<numRows; curRow++) {

		formatString = "";
		// add an extra ';' to the end of the selectedRowContents String so that
		// the breakStringList will be abl eto detect all the columns
		row_Vector = StringUtil.breakStringList( listItems[curRow] + ";", ";", 0);		
		size = row_Vector.size();
		for ( curCol=0 ; curCol < size; curCol++ ) {
			formatString = formatString 
				  	+ row_Vector.elementAt( curCol ).toString().trim()
					+ delim;
		}
        	export_Vector.addElement(new String( formatString + "\n" ));         
        }           
	return;
}
*/

} // end ExportGUI class definition
