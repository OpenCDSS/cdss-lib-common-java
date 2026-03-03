// ExportJGUI - export to file utility

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

import RTi.Util.GUI.GUIUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import java.awt.Cursor;
import java.awt.FileDialog;

import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
This class provides a generic way to export a list of strings to a file.
The static export method is called, which creates a file selector for the export.
*/
public class ExportJGUI {

private static JFrame            _parentJFrame;	// Frame class making the call to HBExportJGUI.
private static JDialog		_parentJDialog;
private static List<String> _export;	// List containing the formatted data to export.

/**
Export a list of strings given the parent frame.
@param parent JFrame that calls this routine.
@param export list of strings to export.
*/
public static void export ( JFrame parent, List<String> export ) {
	export ( parent, export, "" );
}

/**
Export a list of strings given the parent frame and a help key for a help button.
@param parent JFrame that calls this routine.
@param export list of strings to export.
@param helpkey Help key string for use with RTi.Util.Help.URLHelp (this is
reserved for future use.
@see RTi.Util.Help.URLHelp
*/
public static void export( JFrame parent, List<String> export, String helpkey ) {
	String	routine="ExportJGUI.export";

	_parentJFrame 	= parent;
	_export 	= export;

	//
	// Determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to export");
		return;
	}

	// Always try to write to the local disk.  If trusted, do it.
	try {
		exportToLocalDrive();
	}
	catch ( IOException e ) {
	}
}

//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accepts a  JTextArea.
//  (2)calls getListContents( MultiList list ) to get a formatted export Vector
//  (3)calls export( JFrame parent, Vector export )
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)parent - parent JFrame object from which this call was made.
//  (I)status_JTextField - status JTextField to display statis to. May be null
//  (I)textArea - JTextArea object
//-----------------------------------------------------------------------------

/**
Export a JTextArea of strings given the parent frame a JTextField to receive
status messages.
@param parent JFrame that calls this routine.
@param status_JTextField JTextField to receive messages as the JTextArea is
exported.
@param textArea JTextArea to export.
*/
public static void exportJTextAreaObject(	JFrame parent,
						JTextField status_JTextField,
						JTextArea textArea )
{	String 	statusString;		// contains status information
	String	routine = "ExportJGUI.exportJTextAreaObject()";
			
	// display status
	statusString = "Exporting query results...";
	parent.setCursor( new Cursor (Cursor.WAIT_CURSOR) );
	if ( status_JTextField != null ) {
		status_JTextField.setText( statusString );
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

	List<String> export_Vector = StringUtil.breakStringList ( textArea.getText(), "\n", 0 );
	
	// export to file/browser page
	export( parent, export_Vector );

	// display status
	statusString = "Finished exporting.";
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_JTextField != null ) {
		status_JTextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );

	return;
}

/**
Export to a file on the local drive.
*/
private static void exportToLocalDrive() throws IOException {
	FileDialog fd;		// FileDialog Object
	PrintWriter oStream;	// DataOutputStream Object
	String	routine="ExportJGUI.exportToLocalDrive";
		
	// Instantiate a file dialog object with export.txt as the default filename.

	fd = new FileDialog(_parentJFrame, "Export", FileDialog.SAVE);
	String last_directory_selected = GUIUtil.getLastFileDialogDirectory();
	if ( last_directory_selected != null ) {
		fd.setDirectory ( last_directory_selected );
	}
	fd.setFile("export.txt");
	fd.setVisible(true);

	// Determine the name of the export file as specified from the FileDialog object.

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

		_parentJFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		//
		// Create a new FileOutputStream wrapped with a DataOutputStream
		// for writing to a file.
		try {
			oStream = new PrintWriter( new FileWriter( fileName ) );
			//
			// Write each element of the _export Vector to a file.
			//
			String linesep = System.getProperty("line.separator");
			for (int i=0; i<_export.size(); i++) {
				oStream.print( _export.get(i).toString() + linesep );
			}
			//
			// Close the PrintStream Object.
			//
			oStream.flush();
			oStream.close();
		}
		catch (IOException IOError) {
			Message.printWarning( 1, routine, "Trouble opening or writing to file \"" + fileName + "\"." );
			_parentJFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			throw new IOException ( "Trouble opening or writing to file \"" + fileName + "\"." );
		}

		_parentJFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	Message.printStatus( 1, routine, "Successfully exported data to \"" + fileName + "\"." );

	return;
}

/**
Export a list of strings given the parent frame.
@param parent JDialog that calls this routine.
@param export Vector of strings to export.
*/
public static void export ( JDialog parent, List<String> export ) {
	export ( parent, export, "" );
}

/**
Export a list of strings given the parent frame and a help key for a help button.
@param parent JDialog that calls this routine.
@param export list of strings to export.
@param helpkey Help key string for use with RTi.Util.Help.URLHelp (this is reserved for future use.
@see RTi.Util.Help.URLHelp
*/
public static void export( JDialog parent, List<String> export, String helpkey ) {
	String	routine="ExportJGUI.export";

	_parentJDialog 	= parent;
	_export 	= export;

	//
	// Determine if data exist in the Vector, if not, issue a warning.
	//
	if (_export == null || _export.isEmpty() ){
		Message.printWarning( 1, routine, "No text to export");
		return;
	}

	// We always try to write to the local disk.  If we are trusted, we do it.
	try {	dialogExportToLocalDrive();
	}
	catch ( IOException e ) {
	}
}

//-----------------------------------------------------------------------------
//  Notes:
//  (1)This function accepts a  JTextArea.
//  (2)calls getListContents( MultiList list ) to get a formatted export Vector
//  (3)calls export( JDialog parent, Vector export )
//-----------------------------------------------------------------------------
//  Variables:	I/O	Description
//  (I)parent - parent JDialog object from which this call was made.
//  (I)status_JTextField - status JTextField to display statis to. May be null
//  (I)textArea - JTextArea object
//-----------------------------------------------------------------------------

/**
Export a JTextArea of strings given the parent frame a JTextField to receive
status messages.
@param parent JDialog that calls this routine.
@param status_JTextField JTextField to receive messages as the JTextArea is
exported.
@param textArea JTextArea to export.
*/
public static void exportJTextAreaObject ( JDialog parent, JTextField status_JTextField, JTextArea textArea ) {
	String statusString; // Contains status information.
	String	routine = "ExportJGUI.exportJTextAreaObject()";
			
	// Display status.
	statusString = "Exporting query results...";
	parent.setCursor( new Cursor (Cursor.WAIT_CURSOR) );
	if ( status_JTextField != null ) {
		status_JTextField.setText( statusString );
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
	
	// Export to file/browser page.
	export( parent, exportList );

	// display status
	statusString = "Finished exporting.";
	parent.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
	if ( status_JTextField != null ) {
		status_JTextField.setText( statusString );
	}
	Message.printStatus( 1, routine, statusString );

	return;
}

/**
Export to a file on the local drive.
*/
private static void dialogExportToLocalDrive() throws IOException {
	FileDialog fd;		// FileDialog Object.
	PrintWriter oStream;	// DataOutputStream Object.
	String	routine="ExportJGUI.dialogExportToLocalDrive";
		
	// Instantiate a file dialog object with export.txt as the default filename.

	JFrame frame = new JFrame();
	java.awt.Dimension d = _parentJDialog.getSize();
	frame.setSize(d.width, d.height);
	frame.setVisible(true);
	RTi.Util.GUI.JGUIUtil.center(frame);
	frame.setVisible(false);
	fd = new FileDialog(frame, "Export", FileDialog.SAVE);
	frame.dispose();
	String last_directory_selected = GUIUtil.getLastFileDialogDirectory();
	if ( last_directory_selected != null ) {
		fd.setDirectory ( last_directory_selected );
	}
	fd.setFile("export.txt");
	fd.setVisible(true);

	// Determine the name of the export file as specified from the FileDialog object.

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

		_parentJDialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		//
		// Create a new FileOutputStream wrapped with a DataOutputStream for writing to a file.
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
			// Close the PrintStream Object.
			//
			oStream.flush();
			oStream.close();
		}
		catch (IOException IOError) {
			Message.printWarning( 1, routine, "Trouble opening or writing to file \"" + fileName + "\"." );
			_parentJDialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			throw new IOException ( "Trouble opening or writing to file \"" + fileName + "\"." );
		}

		_parentJDialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	Message.printStatus( 1, routine, "Successfully exported data to \"" + fileName + "\"." );

	return;
}

}