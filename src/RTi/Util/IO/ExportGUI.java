// ExportGUI - export to file utility

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
*/
public class ExportGUI {

private static Frame            _parent;	// Frame class making the call
						// to HBExportGUI
private static List<String> _export;	// List containing the formatted data to export

/**
Export a vector of strings given the parent frame.
@param parent Frame that calls this routine.
@param export list of strings to export.
*/
public static void export ( Frame parent, List<String> export )
{
	export ( parent, export, "" );
}

/**
Export a list of strings given the parent frame and a help key for a help button.
@param parent Frame that calls this routine.
@param export list of strings to export.
@param helpkey Help key string for use with RTi.Util.Help.URLHelp (this is reserved for future use).
@see RTi.Util.Help.URLHelp
*/
public static void export( Frame parent, List<String> export, String helpkey ) {
	String	routine="ExportGUI.export";

	_parent 	= parent;
	_export 	= export;
       
	//
	// Determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to export");
		return;
	}

	// Always try to write to the local disk.  If we are trusted, do it.
	try {
		exportToLocalDrive();
	}
	catch ( IOException e ) {
	}
}

/**
Export a TextArea of strings given the parent frame a TextField to receive status messages.
@param parent Frame that calls this routine.
@param status_TextField TextField to receive messages as the TextArea is exported.
@param textArea TextArea to export.
*/
public static void exportTextAreaObject( Frame parent, TextField status_TextField, TextArea textArea ) {
	String 	statusString;		// contains status information
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

	List<String> exportList = StringUtil.breakStringList ( textArea.getText(), "\n", 0 );
	
	// export to file/browser page     
	export( parent, exportList );

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
			Message.printWarning( 1, routine, "Trouble opening or writing to file \"" + fileName + "\"." );
			_parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			throw new IOException ( "Trouble opening or writing to file \"" + fileName + "\"." );
		}

		_parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	Message.printStatus( 1, routine, "Successfully exported data to \"" + fileName + "\"." );

	return;
}

}