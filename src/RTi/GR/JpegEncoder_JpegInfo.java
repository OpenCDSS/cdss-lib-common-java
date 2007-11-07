// ----------------------------------------------------------------------------
// History:
//
// 23 May 2000	Steven A. Malers, RTi	See copyright for source.  Add to GR
//					library and test with some GR devices
//					(e.g., GeoView).  Modified the code
//					as follows:
//					* Convert import * to explicit imports.
//					* Use RTi messages instead of errors to
//					  standard error.
//					* Remove derivation from Frame - this
//					  appears to have been used only to
//					  enable use of the Media tracker.
// 2002-11-05	J. Thomas Sapienza, RTi	Reformatted.  Got rid of 4-space tabs,
//					made variable private and public 
//					depending on use.  Eliminated unused
//					variables, cleaned up code, commented
//					where possible, organized methods 
//					alphabetically, and split out into
//					separate classes.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// COPYRIGHT:
//
// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.
// ----------------------------------------------------------------------------

package RTi.GR;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.image.PixelGrabber;

/**
 * JpegInfo - Given an image, sets default information about it and divides
 * it into its constituant components, downsizing those that need to be.
 */
class JpegEncoder_JpegInfo
{
public int blockWidth[];
public int blockHeight[];
private String __comment;
private int __compWidth[];
private int __compHeight[];
public int imageHeight;
private Image __imageObj;
public int imageWidth;
private int __maxHSampFactor;
private int __maxVSampFactor;

// the following are set as the default
public int[] acTableNumber = {0, 1, 1};
public int ah = 0;
public int al = 0;
public int[] compID = {1, 2, 3};
public Object components[];
public int[] dcTableNumber = {0, 1, 1};
public int[] hSampFactor = {1, 1, 1};
public boolean[] lastColumnIsDummy = {false, false, false};
public boolean[] lastRowIsDummy = {false, false, false};
public int numberOfComponents = 3;
public int precision = 8;
public int[] qTableNumber = {0, 1, 1};
public int se = 63;
public int ss = 0;
public int[] vSampFactor = {1, 1, 1};

/**
 * Constructor. 
 *
 * @param image the Image to turn into a JPeg
 */
public JpegEncoder_JpegInfo(Image image)
{
	components = 	new Object[numberOfComponents];
	__compWidth = 	new int[numberOfComponents];
	__compHeight = 	new int[numberOfComponents];
	blockWidth = 	new int[numberOfComponents];
	blockHeight = 	new int[numberOfComponents];
	__imageObj = 	image;
	imageWidth = 	image.getWidth(null);
	imageHeight = image.getHeight(null);
	__comment = 	"JPEG Encoder Copyright 1998, James R. Weeks and "
			+ "BioElectroMech.  ";
	getYCCArray();
}

/**
 * Only here for backwards compatability.
 */
float[][] DownSample(float[][] c, int comp)
{
	return downSample(c, comp);
}

/**
 * Downsample some data.
 */
float[][] downSample(float[][] C, int comp)
{
	int bias;
	int incol = 0;
	int inrow = 0;
	int outcol;
	int outrow;
	float output[][];
	//= new float[__compHeight[comp]][__compWidth[comp]];
	
	output = new float[__compHeight[comp]][__compWidth[comp]];
	
	for (outrow = 0; outrow < __compHeight[comp]; outrow++) {
		bias = 1;
		for (outcol = 0; outcol < __compWidth[comp]; outcol++) {
			output[outrow][outcol] = (C[inrow][incol++] + 
				C[inrow++][incol--] + C[inrow][incol++] + 
				C[inrow--][incol++] + (float)bias)/(float)4.0;
			bias ^= 3;
		}
		inrow += 2;
		incol = 0;
	}
	return output;
}

/**
 * Returns the comment string
 *
 * @return the comment string
 */
public String getComment() {
	return __comment;
}

/**
 * This method creates and fills three arrays, Y, Cb, and Cr using the
 * input image.
 */
private void getYCCArray()
{
	int b, g, r, x, y;
	int values[] = new int[imageWidth * imageHeight];

	// In order to minimize the chance that grabPixels will throw an 
	// exception it may be necessary to grab some pixels every few 
	// scanlines and process those before going for more.  The time 
	// expense may be prohibitive. However, for a situation where memory 
	// overhead is a concern, this may be the only choice.
	PixelGrabber grabber = 
		new PixelGrabber(__imageObj.getSource(), 0, 0, 
		imageWidth, imageHeight, values, 0, imageWidth);
	__maxHSampFactor = 1;
	__maxVSampFactor = 1;
	
	for (y = 0; y < numberOfComponents; y++) {
		__maxHSampFactor = Math.max(__maxHSampFactor, hSampFactor[y]);
		__maxVSampFactor = Math.max(__maxVSampFactor, vSampFactor[y]);
	}
	
	for (y = 0; y < numberOfComponents; y++) {
		__compWidth[y] = (((imageWidth%8 != 0) ? 
			((int) Math.ceil((double) imageWidth/8.0))*8 : 
			imageWidth)/__maxHSampFactor)*hSampFactor[y];
		if (__compWidth[y] != 
			((imageWidth/__maxHSampFactor)*hSampFactor[y])) {
			lastColumnIsDummy[y] = true;
		}

		// results in a multiple of 8 for __compWidth
		// this will make the rest of the program fail for the unlikely
		// event that someone tries to compress an 16 x 16 pixel image
		// which would of course be worse than pointless
		blockWidth[y] = (int) Math.ceil((double) __compWidth[y]/8.0);
		__compHeight[y] = (((imageHeight%8 != 0) ? 
			((int) Math.ceil((double) imageHeight/8.0))*8 :
			imageHeight)/__maxVSampFactor)*vSampFactor[y];
		if (__compHeight[y] != 
			((imageHeight/__maxVSampFactor)*vSampFactor[y])) {
			lastRowIsDummy[y] = true;
		}
		
		blockHeight[y] = (int) Math.ceil((double) __compHeight[y]/8.0);
	}

	try {
		if(grabber.grabPixels() != true) {
			try {
			        throw new AWTException("Grabber returned "
					+ "false: " + grabber.status());
			}
			catch (Exception e) {};
		}
	}
	catch (InterruptedException e) {}

	float Y[][] = 	new float[__compHeight[0]][__compWidth[0]];
	float Cr1[][] = new float[__compHeight[0]][__compWidth[0]];
	float Cb1[][] = new float[__compHeight[0]][__compWidth[0]];
	int index = 0;
	
	for (y = 0; y < imageHeight; ++y) {
		for (x = 0; x < imageWidth; ++x) {
			r = ((values[index] >> 16) & 0xff);
			g = ((values[index] >> 8) & 0xff);
			b = (values[index] & 0xff);

			// The following three lines are a more correct color 
			// conversion but the current conversion technique is 
			// sufficient and results in a higher compression rate.
			//Y[y][x] = 16 + (float)(0.8588*(0.299 * (float)r 
			//+ 0.587 * (float)g + 0.114 * (float)b ));
			//Cb1[y][x] = 128 + (float)(0.8784*(-0.16874 * (float)r 
			//- 0.33126 * (float)g + 0.5 * (float)b));
			//Cr1[y][x] = 128 + (float)(0.8784*(0.5 * (float)r 
			//- 0.41869 * (float)g - 0.08131 * (float)b));
			Y[y][x] = (float)((0.299 * (float)r 
				+ 0.587 * (float)g + 0.114 * (float)b));
			Cb1[y][x] = 128 + (float)((-0.16874 * (float)r 
				- 0.33126 * (float)g + 0.5 * (float)b));
			Cr1[y][x] = 128 + (float)((0.5 * (float)r 
				- 0.41869 * (float)g - 0.08131 * (float)b));
			index++;
		}
	}

	// Need a way to set the H and V sample factors before allowing 
	// downsampling. For now (04/04/98) downsampling must be hard coded.
	// Until a better downsampler is implemented, this will not be done.
	// Downsampling is currently supported.  The downsampling method here
	// is a simple box filter.

	components[0] = Y;
	//        Cb2 = downSample(Cb1, 1);
	components[1] = Cb1;
	//        Cr2 = downSample(Cr1, 2);
	components[2] = Cr1;
}

/**
 * Set the comment string.
 *
 * @param comment string to concatenate onto the end of the comment string.
 */
public void setComment(String comment) {
	__comment.concat(comment);
}

}
