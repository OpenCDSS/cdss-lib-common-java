// GeoViewApp - simple GeoView application

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
public void init () {
	IOUtil.setApplet ( this );
	IOUtil.setProgramData ( "GeoView", "01.00.00", null );

	String gvp = getParameter ( "GeoViewProject" );
	JFrame f = new JFrame ();
	GeoViewJPanel gv = new GeoViewJPanel ( f, new PropList ("") );
	try {	gv.openGVP ( gvp );
		add ( gv );
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "GeoViewApp.init", "Error opening GeoView Project file" );
	}
	f = null;
	gv = null;
	gvp = null;
}

}