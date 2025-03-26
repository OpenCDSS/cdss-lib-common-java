// PictureJPanel - JPanel that displays an Image

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

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;

import RTi.Util.Message.Message;

/**
PictureJPanel is a JPanel that displays an Image.
*/
@SuppressWarnings("serial")
public class PictureJPanel extends JPanel {

/**
The image to be displayed.
*/
private Image __image;

/**
PictureJPanel Constructor.
*/
public PictureJPanel() {
	super();
}

/**
PictureJPanel Constructor.
@param image Image object.
*/
public PictureJPanel(Image image) {
	super();
	__image = image;
}

/**
Manages redraw events for an instance of this class.
@param g Graphics object
*/
public void paintComponent(Graphics g) {
	super.paintComponent(g);
	if (__image != null) {
  	      g.drawImage(__image, 0, 0, this);
    }
}

/**
Set a new image for an instance of this class.
@param image Image object
*/
public void setImage(Image image) {
	__image = image;
	this.repaint();
}

/**
Set a new image for an instance of this class, given a path to the image file.
The image file is assumed to be in the class path and/or JAR.
@param image_file Image file to read (GIF or JPG).
*/
public void setImage(String image_file) {
	/*
	Image image = Toolkit.getDefaultToolkit().getImage( getClass().getResource(image_file));
	setImage ( image );
	image = null;
	*/
	Image image = this.getToolkit().getImage( image_file );
	String function = "setImage";
	// Use the MediaTracker object to halt processing until the _map_Image is completely loaded.
	MediaTracker mt = new MediaTracker( this );
	mt.addImage( image, 0 );
	try {
		setImage(image);
		mt.waitForID(0);
		if (mt.isErrorID(0)) {
			Message.printWarning(2, function, "mt.isErrorID(0)");
		}
	}
	catch (Exception e) {
		Message.printWarning(2, function, e);
	}
	mt = null;
}

}