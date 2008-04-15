//-----------------------------------------------------------------------------
// SimpleFileEditorJDialog 
//-----------------------------------------------------------------------------
// History: 
//
// 2002-03-05	Morgan Sheedy, RTi	Initial Implementation.
// 2002-03-18 	AMS, RTi		Allow window to be re-sized.
//					Prevent line-wrapping.
//					-Must have the .setPreferredSize( _dim )
//					for the JEditPanel.
//					-Make sure to display scroll bars.
//					-Set bounds to prevent wrapping:
//					int x = _editor_JEditPane.getBounds().x;
//					int y = _editor_JEditPane.getBounds().y;
//					_editor_JEditPane.setBounds( 
//					x, y, 10000, 10000 );
//					-To allow window resizing and JScroll
//					to expand, set weightx and y to 
//					100 for JScrollPane and 0 for other
//					components. Set fill to BOTH.
//
// 2002-10-14   AML 			Updated package name
//					(from RTi.App.NWSRFSGUI_APP) to:
//					RTi.App.NWSRFSGUI.
//					
//					Updated name from: SimpleJFileEditor to:
//					SimpleJFileEditor_JDialog
// 2002-10-15	Steven A. Malers, RTi	Changed package to RTi.Util.GUI and
//					name to SimpleFileEditorJDialog.
//
// 2002-11-20	AML, RTi		Cleaning and updated class to
//					use current LanguageTranslator code.
//
// 2003-03011	AML, RTi		NOTE:  When creating an instance of
//					this class, put in a try/catch
//					statement since it can not handle
//					certain Look and Feels, such as
//					Window's Look and Feel.
// 2005-04-26	J. Thomas Sapienza, RTi	Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.LanguageTranslator;
import RTi.Util.Message.Message;

/**
 * Provides a dialog to display and edit a file.
 * 
 */
public class SimpleFileEditorJDialog extends JDialog implements ActionListener,
	WindowListener, ComponentListener {

private String _class = "SimpleFileEditor_JDialog";

//indicates if the file is to be opened for editing- true 
//or viewing only - false.
private boolean _file_editable = false;

//panes to hold text editor
private JEditorPane _editor_JEditPane = null;
private JScrollPane _scrollpane = null;

//panels
private JPanel _edit_JPanel = null;
private JPanel _button_JPanel = null;

//button
private SimpleJButton _cancel_JButton = null;
private SimpleJButton _save_JButton = null;

//Strings for buttons
private String _cancel_string = "Cancel";
private String _save_string = "Save and Exit";

//Insets for components
private Insets _insets = null;

//font for text - for NwsrfsGUI applications, generally
//need a fixed width font.
private Font _font = new Font( "Monospaced", Font.PLAIN, 12 );

//set dimension for scrollbarpane 
private Dimension _dim = new Dimension( 500, 500 );

//name of file to edit
private String _file_name = null;

//set up input stream and output stream
//model file input
File _inputFile = null;
FileInputStream _fis;
InputStreamReader _isr;
BufferedReader _br = null;
	
//model output going to file
File _outputFile = null;
FileOutputStream _fos;
PrintWriter _pw;

/**
 * Creates an editor in a dialog that permits the user to view and/or edit 
 * the contents of a file. 
 * <p>
 * Usage:
 * <pre>
 * SimpleFileEditorJDialog editor = 
 *        new SimpleFileEditorJDialog("C:/MyFile.txt",true,true);
 * </pre
 * @param file_name Name of file to open for editing.
 * @param editable  Boolean indicating if the file can be edited or not.
 * @param modal  Boolean to indicate if the editor window should be modal 
 * (ie, always stay on top) or not.
 */
public SimpleFileEditorJDialog( String file_name, 
			boolean editable, 
			boolean modal ) {

	setModal( modal );

	//see if there is a translation table for this GUI
	if ( LanguageTranslator.getTranslator() != null ) {

		LanguageTranslator translator = null;
		translator = LanguageTranslator.getTranslator();
		if ( translator != null ) {
			//lookup strings for translating

			_cancel_string =
			translator.translate(
			"cancel_string", "Cancel" );

			_save_string = 
			translator.translate(
			"save_string", "Save and Exit" );
		}
	}

	addWindowListener( this );
	addComponentListener( this );

	_file_name = file_name;
	
	_file_editable = editable;

	initialize_layoutGUI();

} //end constructor


/**
 * Creates an editor in a dialog that permits the user to view 
 * the contents of a file. 
 * <p>
 * Usage:
 * <pre>
 * SimpleFileEditorJDialog editor = new SimpleFileEditorJDialog("C:/MyFile.txt");
 * </pre
 * <p>
 * @param file_name Name of file to open for viewing only.
 */
public SimpleFileEditorJDialog( String file_name ) {
	this( file_name, false, false );
} //end constructor


/**
* Creates the button panel. 
* <p>
* If the constructor is called with 
* false as the second parameter, the "save" button is disabled so
* that the file is opened for viewing only.
*/
private void create_button_panel() {
	String routine = _class + ".create_button_panel";

	try {
		//make button panel
		_button_JPanel = new JPanel();
		_button_JPanel.setLayout( new GridBagLayout() );

		//make buttons
		_cancel_JButton = new SimpleJButton( 
			_cancel_string, _cancel_string, this );
		_cancel_JButton.setPreferredSize( new Dimension( 175, 25 ) );
		_save_JButton = new SimpleJButton( 
			_save_string, _save_string, this );
		_save_JButton.setPreferredSize( new Dimension( 175, 25 ) );
		
		//see if file is to be editable or not.
		if ( !_file_editable ) {
			_save_JButton.setEnabled( false );
		}

		//add buttons to panel
		int cnt = 0;

		JGUIUtil.addComponent( 
			_button_JPanel, _cancel_JButton,
			cnt, 0, 1, 1, 1, 0, _insets,
			GridBagConstraints.NONE,	
			GridBagConstraints.CENTER );
		cnt++;

		JGUIUtil.addComponent( 
			_button_JPanel, _save_JButton,
			cnt, 0, 1, 1, 1, 0, _insets,
			GridBagConstraints.NONE,	
			GridBagConstraints.CENTER );
		cnt++;
	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine,
		"Error laying out Button panel." );
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine, e );
		}
	}

} //end create_button_panel

/**
* Creates the panel that displays the contents of the file.
*/
private void create_edit_panel() {
	String routine = _class + ".create_edit_panel";

	if ( IOUtil.fileExists( _file_name ) ) {
		_inputFile = new File( _file_name );

		try {
			//create edit panel
			_edit_JPanel = new JPanel();
			//_edit_JPanel.setLayout( new GridBagLayout() );
			_edit_JPanel.setLayout( new BorderLayout() );
			_edit_JPanel.setFont( _font );
			_edit_JPanel.setPreferredSize( _dim );
			_edit_JPanel.setBackground( Color.blue );

			//create editor
			_editor_JEditPane = new JEditorPane();
			_editor_JEditPane.setEditable( true );
			_editor_JEditPane.setFont( _font );

			//add editor to scroll pane
			_scrollpane = new JScrollPane( _editor_JEditPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
	
			//set bounds for pane
			int x = _editor_JEditPane.getBounds().x;
			int y = _editor_JEditPane.getBounds().y;
			_editor_JEditPane.setBounds( x, y, 10000, 10000 );
			
			//now read in file

			try {
				//set up input Stream
				_fis = new FileInputStream( _inputFile );
				_isr = new InputStreamReader ( _fis );
				_br = new BufferedReader ( _isr );

				//now try reading in file into editor
				_editor_JEditPane.read( _br, _inputFile );
			}
			catch ( Exception e ) { 
				Message.printWarning( 2, routine, 
				"Erorr reading in file: \"" +
				_file_name + "\"." );
				if ( Message.isDebugOn ) {
					Message.printWarning( 2, routine, e );
				}
	 		}

			//now the editor is created and filled, add it
			//to the _editor_JPanel.
			_edit_JPanel.add( _scrollpane, BorderLayout.CENTER );
		}
		catch ( Exception e ) {
			Message.printWarning( 2, routine,
			"Error laying out Editor panel." );
			if ( Message.isDebugOn ) {
				Message.printDebug( 2, routine, e );
			}
		}
	}
	else { 
		Message.printWarning( 2, routine,
		"Unable to read file: \"" + 
		_file_name + "\".  Will not create edit pane." ); 
	}

} //end create_edit_panel

/**
 * Cleans up member variables.
 */
public void finalize()
throws Throwable {
	_class = null;
	_editor_JEditPane = null;
	_scrollpane = null;
	_edit_JPanel = null;
	_button_JPanel = null;
	_cancel_JButton = null;
	_save_JButton = null;
	_cancel_string = null;
	_save_string = null;
	_insets = null;
	_font = null;
	_dim = null;
	_file_name = null;
	_inputFile = null;
	_fis = null;
	_isr = null;
	_br = null;
	_outputFile = null;
	_fos = null;
	_pw = null;
	super.finalize();
}

/**
 * Lays out the GUI panels.
 * <p>  The main panels are the editor panel and the
 * button panel.
*/
private void initialize_layoutGUI() {
	String routine = _class + ".initialize_layoutGUI";

	try {
		JPanel main_JPanel = new JPanel();
		main_JPanel.setLayout( new GridBagLayout() );

		//set up insets
		_insets = new Insets( 5, 3, 5, 3 );

		int cnt = 0;

		//make menu bar
		//create_menu_bar();

		//add title
		JLabel title_JLabel = new JLabel( _file_name );

		//add title
		JGUIUtil.addComponent(
			main_JPanel, title_JLabel,
			0, cnt, 1, 1, 0, 0, _insets,
			GridBagConstraints.BOTH,
			GridBagConstraints.CENTER );
		cnt++;		

		//make editpane and fill with file to be edited.
		create_edit_panel();
		//add edit panel
		JGUIUtil.addComponent(
			main_JPanel, _edit_JPanel,
			0, cnt, 1, 1, 100, 100, _insets,
			GridBagConstraints.BOTH,
			GridBagConstraints.CENTER );
		cnt++;

		//create button panel
		create_button_panel();
		//add button JPanel
		JGUIUtil.addComponent(
			main_JPanel, _button_JPanel,
			0, cnt, 1, 1, 0, 0, _insets,
			GridBagConstraints.BOTH,
			GridBagConstraints.CENTER );
		cnt++;


		//add main panel to content pane
		getContentPane().add( "Center", main_JPanel );

		pack();
		setVisible( true );
		validate();

	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, 
		"Error laying out GUI." );
		if ( Message.isDebugOn ) {
			Message.printDebug( 2, routine, e );
		}
	}
	
} //end initialize_layoutGUI 


/**
 * Method used to save the changes made in the editor panel to the file.  
 * <p>
 * The file is saved to a temporary file and then the temporary file is 
 * moved to replace the original version of the file.
 */
public void save_file() {
	String routine = _class + ".save_file";
	
	//we need to get the changes from the editor pane 
	//and write them to a file.  They are written to 
	//a temporary file and then once it is finished, the
	//temporary file is moved back to replace the original file.

	try {
		_outputFile = new File( _file_name + ".tmp" );
		_fos = new FileOutputStream( _outputFile );
		_pw = new PrintWriter( _fos );

		//now we have streams, get text from editor
		_pw.print( _editor_JEditPane.getText() );
		_pw.flush();

	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine,
		"Unable to save changes to file: \"" +
		_file_name + "\"." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}	
	}

	//now we have saved the file as a temporary file,
	//move it back to original place.
	try {
		_outputFile.renameTo( _inputFile );
	}
	catch( Exception e ) {
		Message.printWarning( 2, routine, 
		"Unable to save changes to file: \"" +
		_file_name + "\"." );
		if ( Message.isDebugOn ) {
			Message.printWarning( 2, routine, e );
		}	
	}

} //end save_file

/**
* Set Font size to use for text in Editor.
* @param font_size An integer representing the desired font size.
*/
public void setFont( int font_size ) {
	if ( font_size >= 8 ) {
		_font = new Font( "Monospaced", Font.PLAIN, font_size );
	}
} 


/////////////////* ACTIONS *///////////////////////////
/**
 *Event handler for action events.
 * @param event  Event to handle.
 */
public void actionPerformed( ActionEvent event ) {
        String routine = _class + ".actionPerformed";
        Object source = null;
        try {
                source = event.getSource();

		if ( source.equals( _cancel_JButton ) ) {
			//set it invisible and destroy it.
			//do not save any changes.
			setVisible( false );
			dispose();
		}
		if ( source.equals( _save_JButton ) ) {
			save_file();
			setVisible( false );
			dispose();
		}

	}
	catch ( Exception e ) {
		Message.printWarning( 2, routine, e );
	}

} //end actionPerformed

/**
 * Methods to listen for window events.
 *
 * @param e  WindowEvent to handle.
 */
public void windowActivated( WindowEvent e ) { ; }
public void windowClosed( WindowEvent e ) { ; }
public void windowDeactivated( WindowEvent e ) { 
/*
	//NOT WORK...
	//setVisible(true);
	//toFront(); 
	//getContentPane().requestFocus(); 
	JDialog source = (JDialog)e.getSource();
	//source.setVisible( true );
	source.toFront( );
	source.requestFocus( );
*/
}
public void windowDeiconified( WindowEvent e ) { ; }
public void windowOpened( WindowEvent e ) { ; }
public void windowIconified( WindowEvent e ) { ; }
public void windowClosing( WindowEvent e ) { ; }


/**
Component Events
*/
public void componentResized(ComponentEvent e) { ; }
public void componentMoved(ComponentEvent e) { ; }
public void componentShown(ComponentEvent e) { ; }
public void componentHidden(ComponentEvent e) { ; }

//public static void main(String args[]) 
//{
//  
//  SimpleFileEditorJDialog editor = new SimpleFileEditorJDialog("C:/WY.txt");
//  editor.setVisible(true);
//}
} // end SimpleFileEditorJDialog

