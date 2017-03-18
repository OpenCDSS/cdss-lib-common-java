// ----------------------------------------------------------------------------
// GeoViewApp - simple GeoView application
// ----------------------------------------------------------------------------
// History:
//
// 2001-11-16	Steven A. Malers, RTi	Initial version.  Test concept of
//					general applet tool.
// 2003-10-06	J. Thomas Sapienza, RTi	Converted to Swing.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.applet.Applet;
import javax.swing.JFrame;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

/**
REVISIT (JTS - 2006-05-23)
I'm not sure this is necessary anymore.
*/
@SuppressWarnings("serial")
public class GeoViewApp extends Applet
{

/**
Instantiates an applet instance.
*/
public void init ()
{	IOUtil.setApplet ( this );
	IOUtil.setProgramData ( "GeoView", "01.00.00", null );

	String gvp = getParameter ( "GeoViewProject" );
	JFrame f = new JFrame ();
	GeoViewJPanel gv = new GeoViewJPanel ( f, new PropList ("") );
	try {	gv.openGVP ( gvp );
		add ( gv );
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "GeoViewApp.init",
		"Error opening GeoView Project file" );
	}
	f = null;
	gv = null;
	gvp = null;
}

}
