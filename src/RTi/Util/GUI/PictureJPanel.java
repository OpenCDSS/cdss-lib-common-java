//------------------------------------------------------------------------------
// PictureJPanel - Component to display images.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//	(1)Supports the following images: gif, jpg
//------------------------------------------------------------------------------
// History: 
//
// 2002-09-12	J. Thomas Sapienza, RTi	Created initial version from 
//					PictureJPanel.java
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.GUI; 

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;

import RTi.Util.Message.Message;

/**
PictureJPanel is a JPanel that displays an Image.
*/
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
Clean up for garbage collection.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable {
	__image = null;
	super.finalize();
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
	Image image = Toolkit.getDefaultToolkit().getImage(
		getClass().getResource(image_file));
	setImage ( image );
	image = null;
	*/
	Image image = this.getToolkit().getImage( image_file );
	String function = "setImage";
	// use the MediaTracker object to hault processing
	// until the _map_Image is completely loaded.
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
