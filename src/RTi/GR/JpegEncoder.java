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

//import java.awt.MediaTracker;
//import java.awt.Frame;
import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import RTi.Util.Message.Message;

/**
 * JpegEncoder - The JPEG main program which performs a jpeg compression of
 * an image.
 */
public class JpegEncoder // extends Frame
{
/**
 * The output stream to be used to write the jpeg.
 */
private BufferedOutputStream __outStream;
/**
 * ?
 */
private JpegEncoder_DCT __dct;
/**
 * ? 
 */
private JpegEncoder_Huffman __huf;
/**
 * An object to store information about the jpeg.
 */
private JpegEncoder_JpegInfo JpegObj;

//private int code;
/**
 * The height of the jpeg.
 */
private int __imageHeight;
/**
 * The width of the jpeg.
 */
private int __imageWidth;
/**
 * ?
 */
public static int[] __jpegNaturalOrder = {
	0,  1,  8, 16,  9,  2,  3, 10,
	17, 24, 32, 25, 18, 11,  4,  5,
	12, 19, 26, 33, 40, 48, 41, 34,
	27, 20, 13,  6,  7, 14, 21, 28,
	35, 42, 49, 56, 57, 50, 43, 36,
	29, 22, 15, 23, 30, 37, 44, 51,
	58, 59, 52, 45, 38, 31, 39, 46,
	53, 60, 61, 54, 47, 55, 62, 63,
};
/**
 * The quality of the jpeg.
 */
private int __quality;

/** 
 * Constructor.  Sets up all the relvant jpeg generation and writing info
 * based on the parameters passed in.
 *
 * @param image the image to be saved as a jpeg
 * @param quality the quality (0 - 100) to save the Jpeg as
 * @param out the outputstream to write the jpeg with
 */
public JpegEncoder(Image image, int quality, OutputStream out)
{
	/* SAM - comment this out.  Why is it really needed?
	MediaTracker tracker = new MediaTracker(this);
	tracker.addImage(image, 0);
	try {
		tracker.waitForID(0);
	}
	catch (InterruptedException e) {
		// Got to do something?
	}
	*/
	
	/*
	* quality of the image.
	* 0 to 100 and from bad image quality, high compression to good
	* image quality low compression
	*/
	__quality=quality;

	/*
	* Getting picture information
	* It takes the Width, Height and RGB scans of the image. 
	*/
	JpegObj = new JpegEncoder_JpegInfo(image);
	
	__imageHeight=JpegObj.imageHeight;
	__imageWidth=JpegObj.imageWidth;
	
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, "JpegEncoder", "Width=" + __imageWidth +
			" Height=" + __imageHeight );
	}

	__outStream = new BufferedOutputStream(out);
	__dct = new JpegEncoder_DCT(__quality);
	__huf=new JpegEncoder_Huffman(__imageWidth,__imageHeight);
}

/**
 * Only here for backward compatability with older code.
 */
public void Compress() {
	compress();
}

/**
 * Compress the Jpeg according to the quality desired.
 */
public void compress() {
	writeHeaders(__outStream);
	writeCompressedData(__outStream);
	writeEOI(__outStream);
	try {
		__outStream.flush();
	} catch (IOException e) {
		Message.printWarning ( 2, "JpegEncoder.Compress",
			"IO Error: " + e.getMessage());
	}
}

/** 
 * Get the quality.
 *
 * @return the quality
 */
public int getQuality() {
	return __quality;
}

/**
 * Set the quality.
 * 
 * @param quality the quality to set the quality to
 */
public void setQuality(int quality) {
	__quality = quality;
	__dct = new JpegEncoder_DCT(quality);
}

/**
 * Only here for backwards compatability with older code.
 */
private void WriteArray(byte[] data, BufferedOutputStream out) { 
	writeArray(data, out);
}

/**
 * Writes an array of data to the jpeg file.
 *
 * @param data the data to be written
 * @param out the outputstream to which to write the data
 */
private void writeArray(byte[] data, BufferedOutputStream out) {
	int length;
	try {
		length = (((int) (data[2] & 0xFF)) << 8) + 
			(int) (data[3] & 0xFF) + 2;
		out.write(data, 0, length);
	} catch (IOException e) {
		Message.printWarning ( 2, "JpegEncoder.writeArray",
			"IO Error: " + e.getMessage());
	}
}

/**
 * Only here for backwards compatability with older code.
 */
public void WriteCompressedData(BufferedOutputStream outStream) {
	writeCompressedData(outStream);
}

/**
 * This method controls the compression of the image.
 * Starting at the upper left of the image, it compresses 8x8 blocks
 * of data until the entire image has been compressed.
 *
 * @param outStream the outstream to which to write the file.
 */
public void writeCompressedData(BufferedOutputStream outStream) {
	double dctArray2[][] = new double[8][8];
	
	float dctArray1[][] = new float[8][8];
	float inputArray[][];	
	
	int a, b, c, i, j, r;
	int comp;
	int dctArray3[] = new int[8*8];
	int lastDCValue[] = new int[JpegObj.numberOfComponents];
	int minBlockHeight;	
	int minBlockWidth;
	int xblockoffset;
	int xpos;
	int yblockoffset;
	int ypos;
	//int zeroArray[] = new int[64];
	// commented out for reasons seen below (grep for 11-05-2002 to find
	// specific section) 

	// This initial setting of minBlockWidth and minBlockHeight is done to
	// ensure they start with values larger than will actually be the case.
	minBlockWidth = ((__imageWidth%8 != 0) ? 
			(int) (Math.floor((double) __imageWidth/8.0) + 1)*8 : 
			__imageWidth);
	minBlockHeight = ((__imageHeight%8 != 0) ? 
			(int) (Math.floor((double) __imageHeight/8.0) + 1)*8 : 
			__imageHeight);
			
	for (comp = 0; comp < JpegObj.numberOfComponents; comp++) {
		minBlockWidth = Math.min(minBlockWidth, 
			JpegObj.blockWidth[comp]);
		minBlockHeight = Math.min(minBlockHeight, 
			JpegObj.blockHeight[comp]);
	}

	xpos = 0;
	for (r = 0; r < minBlockHeight; r++) {
	for (c = 0; c < minBlockWidth; c++) {
		xpos = c*8;
		ypos = r*8;
		for (comp = 0; comp < JpegObj.numberOfComponents; comp++) {
			inputArray = (float[][]) JpegObj.components[comp];
			for(i = 0; i < JpegObj.vSampFactor[comp]; i++) {
			for(j = 0; j < JpegObj.hSampFactor[comp]; j++) {
				xblockoffset = j * 8;
				yblockoffset = i * 8;
				for (a = 0; a < 8; a++) {
				for (b = 0; b < 8; b++) {
	
				// 11-05-2002 Old comments by SAM?  MLT?
				// I believe this is where the dirty line at 
				// the bottom of the image is coming from.  I 
				// need to do a check here to make sure I'm 
				// not reading past image data. This seems to 
				// not be a big issue right now. (04/04/98)
	
					dctArray1[a][b] = inputArray[ypos + 
						yblockoffset + a][xpos + 
						xblockoffset + b];
				} // match b = 0, < 8
				} // match a = 0, < 8
				// 11-05-2002: Old comments by SAM?  MLT?
				// The following code commented out because on 
				// some images this technique results in poor 
				// right and bottom borders.
				//if ((!JpegObj.lastColumnIsDummy[comp] || 
				//c < Width - 1) && (!JpegObj.lastRowIsDummy[
				//comp] || r < Height - 1)) {
				dctArray2 = __dct.forwardDCT(dctArray1);
				dctArray3 = __dct.quantizeBlock(dctArray2, 
					JpegObj.qTableNumber[comp]);
				//}
				//else {
				//	zeroArray[0] = dctArray3[0];
				//	zeroArray[0] = lastDCValue[comp];
				//	dctArray3 = zeroArray;
				//}
				__huf.HuffmanBlockEncoder(outStream, 
					dctArray3, lastDCValue[comp], 
					JpegObj.dcTableNumber[comp], 
					JpegObj.acTableNumber[comp]);
				lastDCValue[comp] = dctArray3[0];
			} // match j = 0; < JpegObj.hSampFactor[comp]
			} // match i = 0; < JpegObj.vSampFactor[comp]
		} // match comp = 0; < JpegObj.numberOfComponents
	} // match c = 0; < minBlockWidth
	} // match r = 0; < minBlockHeight
	__huf.flushBuffer(outStream);	
}

/**
 * Only here for backwards compatability with older code.
 */
public void WriteEOI(BufferedOutputStream out) {
	writeEOI(out);
}

/**
 * Write the end of image byte to the output stream.
 *
 * @param out the stream to which to write the EOI
 */
public void writeEOI(BufferedOutputStream out) {
	byte[] EOI = {(byte) 0xFF, (byte) 0xD9};
	writeMarker(EOI, out);
}

/**
 * Only here for backwards compatability with older code.
 */
public void WriteHeaders(BufferedOutputStream out) {
	writeHeaders(out);
}

/**
 * Write the headers to the output stream.
 *
 * @param out the output stream to which to write the jpeg headers
 */
public void writeHeaders(BufferedOutputStream out) {
	int i, j;
	int index;
	int length;
	int offset;
	int tempArray[];
	
	// The order of the following headers doesn't matter.

	// the SOI marker
	byte[] SOI = {(byte) 0xFF, (byte) 0xD8};
	writeMarker(SOI, out);
	
	// the JFIF header
	byte JFIF[] = new byte[18];
	JFIF[0] = (byte) 0xff;
	JFIF[1] = (byte) 0xe0;
	JFIF[2] = (byte) 0x00;
	JFIF[3] = (byte) 0x10;
	JFIF[4] = (byte) 0x4a;
	JFIF[5] = (byte) 0x46;
	JFIF[6] = (byte) 0x49;
	JFIF[7] = (byte) 0x46;
	JFIF[8] = (byte) 0x00;
	JFIF[9] = (byte) 0x01;
	JFIF[10] = (byte) 0x00;
	JFIF[11] = (byte) 0x00;
	JFIF[12] = (byte) 0x00;
	JFIF[13] = (byte) 0x01;
	JFIF[14] = (byte) 0x00;
	JFIF[15] = (byte) 0x01;
	JFIF[16] = (byte) 0x00;
	JFIF[17] = (byte) 0x00;
	writeArray(JFIF, out);

	// Comment Header
	String comment = new String();
	comment = JpegObj.getComment();
	length = comment.length();
	byte COM[] = new byte[length + 4];
	COM[0] = (byte) 0xFF;
	COM[1] = (byte) 0xFE;
	COM[2] = (byte) ((length >> 8) & 0xFF);
	COM[3] = (byte) (length & 0xFF);
	java.lang.System.arraycopy(JpegObj.getComment().getBytes(), 
		0, COM, 4, JpegObj.getComment().length());
	writeArray(COM, out);

	// The DQT header
	// 0 is the luminance index and 1 is the chrominance index
	byte DQT[] = new byte[134];
	DQT[0] = (byte) 0xFF;
	DQT[1] = (byte) 0xDB;
	DQT[2] = (byte) 0x00;
	DQT[3] = (byte) 0x84;
	offset = 4;
	for (i = 0; i < 2; i++) {
		DQT[offset++] = (byte) ((0 << 4) + i);
		tempArray = (int[]) __dct.quantum[i];
		for (j = 0; j < 64; j++) {
			DQT[offset++] = (byte) tempArray[__jpegNaturalOrder[j]];
		}
	}
	writeArray(DQT, out);

	// Start of Frame Header
	byte SOF[] = new byte[19];
	SOF[0] = (byte) 0xFF;
	SOF[1] = (byte) 0xC0;
	SOF[2] = (byte) 0x00;
	SOF[3] = (byte) 17;
	SOF[4] = (byte) JpegObj.precision;
	SOF[5] = (byte) ((JpegObj.imageHeight >> 8) & 0xFF);
	SOF[6] = (byte) ((JpegObj.imageHeight) & 0xFF);
	SOF[7] = (byte) ((JpegObj.imageWidth >> 8) & 0xFF);
	SOF[8] = (byte) ((JpegObj.imageWidth) & 0xFF);
	SOF[9] = (byte) JpegObj.numberOfComponents;
	index = 10;
	for (i = 0; i < SOF[9]; i++) {
		SOF[index++] = (byte) JpegObj.compID[i];
		SOF[index++] = (byte) ((JpegObj.hSampFactor[i] << 4) 
			+ JpegObj.vSampFactor[i]);
		SOF[index++] = (byte) JpegObj.qTableNumber[i];
	}
	writeArray(SOF, out);

	// The DHT Header
	byte DHT1[];
	byte DHT2[];
	byte DHT3[];
	byte DHT4[];

	int bytes;
	int intermediateIndex;
	int oldIndex;
	int temp;

	length = 2;
	index = 4;
	oldIndex = 4;
	
	DHT1 = new byte[17];
	DHT4 = new byte[4];
	DHT4[0] = (byte) 0xFF;
	DHT4[1] = (byte) 0xC4;

	for (i = 0; i < 4; i++ ) {
		bytes = 0;
		DHT1[index++ - oldIndex] = 
			(byte) ((int[]) __huf.bits.elementAt(i))[0];
		for (j = 1; j < 17; j++) {
			temp = ((int[]) __huf.bits.elementAt(i))[j];
			DHT1[index++ - oldIndex] =(byte) temp;
			bytes += temp;
		}		
		intermediateIndex = index;
		
		DHT2 = new byte[bytes];
		for (j = 0; j < bytes; j++) {
		DHT2[index++ - intermediateIndex] = 
			(byte) ((int[]) __huf.val.elementAt(i))[j];
		}

		DHT3 = new byte[index];
		java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldIndex);
		java.lang.System.arraycopy(DHT1, 0, DHT3, oldIndex, 17);
		java.lang.System.arraycopy(DHT2, 0, DHT3, oldIndex + 17, bytes);
		DHT4 = DHT3;
		oldIndex = index;
	}
	DHT4[2] = (byte) (((index - 2) >> 8)& 0xFF);
	DHT4[3] = (byte) ((index -2) & 0xFF);
	WriteArray(DHT4, out);

// Start of Scan Header
        byte SOS[] = new byte[14];
        SOS[0] = (byte) 0xFF;
        SOS[1] = (byte) 0xDA;
        SOS[2] = (byte) 0x00;
        SOS[3] = (byte) 12;
        SOS[4] = (byte) JpegObj.numberOfComponents;
        index = 5;
        for (i = 0; i < SOS[4]; i++) {
                SOS[index++] = (byte) JpegObj.compID[i];
                SOS[index++] = (byte) ((JpegObj.dcTableNumber[i] << 4) 
			+ JpegObj.acTableNumber[i]);
        }
        SOS[index++] = (byte) JpegObj.ss;
        SOS[index++] = (byte) JpegObj.se;
        SOS[index++] = (byte) ((JpegObj.ah << 4) + JpegObj.al);
        WriteArray(SOS, out);
}
	
/**
 * Writes a marker to the jpeg file.
 *
 * @param data the marker to write
 * @param out the outputstream to which to write the marker
 */
private void writeMarker(byte[] data, BufferedOutputStream out) {
	try {
		out.write(data, 0, 2);
	} catch (IOException e) {
		Message.printWarning ( 2, "JpegEncoder.writeMarker",
		"IO Error: " + e.getMessage());
	}	
}
}
