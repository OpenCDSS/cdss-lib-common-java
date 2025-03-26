// SimpleSplashScreen - simple splash screen window

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

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
Class to display a simple splash screen window during the initialization of the main application.
*/
@SuppressWarnings("serial")
public class SimpleSplashScreen extends Window
{

/**
The string containing the absolute path to the file containing the splash image.
*/	
private String __splashFilename;

/**
The Image object for the splash image.
*/	
private Image  __splashImage;

/**
The dimension of the splash image.
*/
private int __splashWidth, __splashHeight;

/**
Border of the splash screen.
*/
private static final int BORDERSIZE = 1;

/**
Color of the splash screen border.
*/
private static final Color BORDERCOLOR = Color.black;

/**
Toolkit object.
*/
Toolkit __toolK;

/**
Default constructor
@param frm the main application Frame
@param splashFilename the main application Frame
*/
public SimpleSplashScreen( Frame applicastionFrame, String splashFilename )
{
// REVISIT (JTS - 2005-04-26)
// Frame should probably be changed to JFrame.
	super( applicastionFrame );
	__splashFilename = splashFilename;
	__toolK          = Toolkit.getDefaultToolkit();
	__splashImage    = loadSplashImage();
	showSplashScreen();
	applicastionFrame.addWindowListener( new WindowListener() );
	
	// Users shall be able to close the splash window by clicking on its display area.
	// This mouse listener listens for mouse clicks and disposes the splash window.
        MouseAdapter disposeOnClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                dispose();
            }
        };
        addMouseListener( disposeOnClick );
}

/**
Load the splash image.
*/
public Image loadSplashImage() {
	MediaTracker tracker = new MediaTracker( this );
	Image result;
	result = __toolK.getImage( __splashFilename );
	tracker.addImage( result, 0 );
	try {
		tracker.waitForAll();
	}
	catch (Exception e) {
	  	e.printStackTrace();
	}
	__splashWidth  = result.getWidth ( this );
	__splashHeight = result.getHeight( this );
	return result;
}

/**
Show the splash screen. 
*/
public void showSplashScreen() {
	Dimension screenDimension = __toolK.getScreenSize();
	int w = __splashWidth  + ( 2 * BORDERSIZE );
	int h = __splashHeight + ( 2 * BORDERSIZE );
	int x = ( screenDimension.width  - w ) /2;
	int y = ( screenDimension.height - h ) /2;
	setBounds     (  x, y, w, h );
	setBackground ( BORDERCOLOR );
	setVisible    (    true     );
}

/**
Paint
*/
public void paint( Graphics g ) {
	g.drawImage( __splashImage,
		     BORDERSIZE,
		     BORDERSIZE,
		     __splashWidth,
		     __splashHeight,
		     this );
}

/**
*/
class WindowListener extends WindowAdapter {

	/**
	*/
	public void windowOpened( WindowEvent Event ) {
		setVisible( false );
		dispose();
	}

}
    
}