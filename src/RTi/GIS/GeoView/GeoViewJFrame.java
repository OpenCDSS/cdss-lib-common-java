// ----------------------------------------------------------------------------
// GeoViewJFrame - standard JFrame containing map-based display, based on
//			GeoView package
// ----------------------------------------------------------------------------
// Copyright:  See COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2001-06	Steven A. Malers, RTi	Put together class from existing code
//					to make more modular and flexible.
// 2001-10-01	SAM, RTi		Add menu to write a GeoViewLayer to a
//					shapefile, for use with grid layers.
// 2001-10-17	SAM, RTi		Review javadoc and set unused data to
//					null to help with garbage collection.
// 2001-12-04	SAM, RTi		Copy GeoViewGUI to GeoViewJFrame and
//					modify to support Swing.
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Brought code in line with the non-Swing
//					version of the code.
// 2003-06-30	SAM, RTi		Add getGeoViewJPanel().
// 2004-01-08	SAM, RTi		Use the icon and title information set
//					in JGUIUtil, if available.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package	RTi.GIS.GeoView;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
Class to display a GeoViewJFrame.  This is a JFrame containing a GeoViewJPanel
and a menu bar with the main GeoView interaction tools.  The events are directed
to the GeoViewJPanel.
*/
public class GeoViewJFrame extends JFrame
implements ActionListener, WindowListener
{

// Menu items...

private final String OPEN_GVP = "Open Project...";
private final String ADD_LAYER_TO_GEOVIEW = "Add Layer...";
private final String ADD_SUMMARY_LAYER_TO_GEOVIEW = "Add Summary Layer...";
private final String GEOVIEW_ZOOM = "Zoom Mode";
private final String GEOVIEW_ZOOM_OUT = "Zoom Out";
private final String PRINT_GEOVIEW = "Print...";
private final String SAVE_AS_JPEG = "Save As Image ...";
private final String SAVE_AS_SHAPEFILE = "Save As ...";
private final String SELECT_GEOVIEW_ITEM = "Select Mode";
private final String SET_ATTRIBUTE_KEY = "Set Attribute Key...";

private GeoViewJPanel _the_GeoViewJPanel = null;

public GeoViewJFrame ( JFrame parent, PropList p )
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	if (	(JGUIUtil.getAppNameForWindows() == null) ||
		JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( "GeoView" );
	}
	else {	setTitle( JGUIUtil.getAppNameForWindows() + " - GeoView" );
	}

	JGUIUtil.setSystemLookAndFeel(true);

	addWindowListener ( this );
	JMenuItem	add_JMenuItem,
			key_JMenuItem,
			print_JMenuItem,
			saveAsImage_JMenuItem,
			saveAs_JMenuItem,
			select_JMenuItem,
			zoom_JMenuItem,
			zoomOut_JMenuItem;
	boolean do_menu = true;	// Open in menu is useful to test GeoViewProject

	if ( do_menu ) {	// This may eventually be a constructor option
	JMenuBar menu_bar = new JMenuBar();

	JMenu file_menu = new JMenu( "File" );

	file_menu.add( add_JMenuItem = new JMenuItem( OPEN_GVP ) );
	add_JMenuItem.addActionListener( this );
	file_menu.addSeparator();

	file_menu.add( add_JMenuItem = new JMenuItem( ADD_LAYER_TO_GEOVIEW ) );
	add_JMenuItem.addActionListener( this );

	file_menu.add(add_JMenuItem=
		new JMenuItem(ADD_SUMMARY_LAYER_TO_GEOVIEW));
	add_JMenuItem.addActionListener( this );

	file_menu.add( key_JMenuItem = new JMenuItem( SET_ATTRIBUTE_KEY ) );
	key_JMenuItem.setEnabled(false);
	key_JMenuItem.addActionListener( this );

	file_menu.addSeparator();

	file_menu.add( select_JMenuItem = new JMenuItem(SELECT_GEOVIEW_ITEM ) );
	select_JMenuItem.addActionListener( this );

	file_menu.add( zoom_JMenuItem = new JMenuItem( GEOVIEW_ZOOM ) );
	zoom_JMenuItem.addActionListener( this );

	file_menu.add( zoomOut_JMenuItem = new JMenuItem( GEOVIEW_ZOOM_OUT ) );
	zoomOut_JMenuItem.addActionListener( this );

	file_menu.addSeparator();

	file_menu.add( print_JMenuItem = new JMenuItem( PRINT_GEOVIEW ) );
	print_JMenuItem.addActionListener( this );

	file_menu.add( saveAsImage_JMenuItem = new JMenuItem( SAVE_AS_JPEG ) );
	saveAsImage_JMenuItem.addActionListener( this );
	file_menu.add( saveAs_JMenuItem = new JMenuItem(
		SAVE_AS_SHAPEFILE ) );
	saveAs_JMenuItem.addActionListener( this );

	menu_bar.add( file_menu );

	setJMenuBar( menu_bar );

	menu_bar = null;
	file_menu = null;
	} // End do_menu

	// Add a panel to hold the canvas...

	JToolBar toolbar = new JToolBar();
	_the_GeoViewJPanel = new GeoViewJPanel ( this, null, toolbar );
	getContentPane().add("North", toolbar);
	getContentPane().add ( "Center", _the_GeoViewJPanel );

	setSize( 950, 950 );
	setBackground ( Color.lightGray );
	pack();
	setVisible( true );
	// Clean up...
	add_JMenuItem = null;
	key_JMenuItem = null;
	print_JMenuItem = null;
	saveAsImage_JMenuItem = null;
	saveAs_JMenuItem = null;
	select_JMenuItem = null;
	zoom_JMenuItem = null;
	zoomOut_JMenuItem = null;
}

/**
Handle action events.  Need in case the menu is enabled.  May disable if menus
are never used.  Event-handling actually occurs in the GeoViewPanel class.
*/
public void actionPerformed( ActionEvent evt )
{	if ( _the_GeoViewJPanel != null ) {
		_the_GeoViewJPanel.actionPerformed(evt);
	}
}

/**
Close the GUI and dispose.  This does not call any listeners to notify any
components of the closing.  If a parent app wants to know if this window is
closed it should add a WindowListener.
*/
public void close ()
{	setVisible ( false );
	dispose();
}

/**
Clean up for garbage collection.
*/
protected void finalize ()
throws Throwable
{	_the_GeoViewJPanel = null;
	super.finalize();
}

/**
Return the GeoViewPanel associated with the GeoViewJFrame.
@return the GeoViewPanel associated with the GeoViewJFrame.
*/
public GeoViewJPanel getGeoViewJPanel ()
{	return _the_GeoViewJPanel;
}

public void windowActivated ( WindowEvent e )
{
}

/**
This class is listening for GeoViewGUI closing so it can gracefully handle.
*/
public void windowClosed ( WindowEvent e )
{
}

/**
Cause the Frame to close.
*/
public void windowClosing ( WindowEvent e )
{	close();	
}

public void windowDeactivated ( WindowEvent e )
{
}

public void windowDeiconified ( WindowEvent e )
{
}

public void windowIconified ( WindowEvent e )
{
}

public void windowOpened ( WindowEvent e )
{
}

public static void main(String args[]) {
	PrintWriter ofp;
	IOUtil.testing(true);
	JGUIUtil.setLastFileDialogDirectory(
		"I:\\DEVELOP\\GIS\\libGeoViewJava\\src\\RTi\\GIS\\GeoView");
	String logFile = "c:\\temp\\test.out";
	try {   
		ofp = Message.openLogFile(logFile);
		Message.setOutputFile(Message.LOG_OUTPUT, ofp);
	 	Message.setDebugLevel(Message.LOG_OUTPUT, 1);
		 Message.setWarningLevel(Message.LOG_OUTPUT, 1);
		 Message.setStatusLevel(Message.LOG_OUTPUT, 1);
		
		Message.printStatus(1, "", "Using logfile: '" + logFile + "'");
	}
	catch (Exception e) {
		Message.printWarning(2, "", 
			"Unable to open log file \"" + logFile + "\"");
	}


	JFrame jframe = new JFrame();
	GeoViewJFrame g = new GeoViewJFrame(jframe, new PropList("blah"));
	JGUIUtil.center(g);
	g.show();
}

} // End GeoViewJFrame class
