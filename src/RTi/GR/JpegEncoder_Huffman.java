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

//import java.awt.Frame;
//import java.awt.MediaTracker;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Vector;

import RTi.Util.Message.Message;
/**
 * This class was modified by James R. Weeks on 3/27/98.
 * It now incorporates Huffman table derivation as in the C jpeg library
 * from the IJG, Jpeg-6a.
 */
class JpegEncoder_Huffman
{
public int ACmatrix0[][];
public int ACmatrix1[][];
public int[] bitsACchrominance = { 0x11,0,2,1,2,4,4,3,4,7,5,4,4,0,1,2,0x77 };;
public int[] bitsACluminance = {0x10,0,2,1,3,3,2,4,3,5,5,4,4,0,0,1,0x7d };
public int[] bitsDCchrominance = { 0x01,0,3,1,1,1,1,1,1,1,1,1,0,0,0,0,0 };
public int[] bitsDCluminance = { 0x00, 0, 1, 5, 1, 1,1,1,1,1,0,0,0,0,0,0,0};
private int __bufferPutBits;
private int __bufferPutBuffer;    
public int code;
public int DCmatrix0[][];
public int DCmatrix1[][];
public int imageHeight;
public int imageWidth;
/**
 * __jpegNaturalOrder[i] is the natural-order position of the i'th element
 * of zigzag order.
 */
public static int[] jpegNaturalOrder = {
	0,  1,  8, 16,  9,  2,  3, 10,
	17, 24, 32, 25, 18, 11,  4,  5,
	12, 19, 26, 33, 40, 48, 41, 34,
	27, 20, 13,  6,  7, 14, 21, 28,
	35, 42, 49, 56, 57, 50, 43, 36,
	29, 22, 15, 23, 30, 37, 44, 51,
	58, 59, 52, 45, 38, 31, 39, 46,
	53, 60, 61, 54, 47, 55, 62, 63,
};
public int numACTables;
public int numDCTables;
public int[] valACchrominance = {
	0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21,
	0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71,
	0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91,
	0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0,
	0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34,
	0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26,
	0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 
	0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 
	0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 
	0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 
	0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 
	0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 
	0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 
	0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 
	0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 
	0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 
	0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 
	0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 
	0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 
	0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
	0xf9, 0xfa
};
public int[] valACluminance = {
	0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12,
	0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07,
	0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08,
	0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0,
	0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16,
	0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
	0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
	0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
	0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
	0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
	0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
	0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
	0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98,
	0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
	0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6,
	0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
	0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4,
	0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2,
	0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea,
	0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
	0xf9, 0xfa
};
public int[] valDCchrominance = { 
	0,1,2,3,4,5,
	6,7,8,9,10,11 
};
public int[] valDCluminance = { 
	0,1,2,3,4,5,
	6,7,8,9,10,11 
};

public Object ACmatrix[];
public Object DCmatrix[];

public Vector bits;
public Vector val;

/**
 * The Huffman class constructor.
 *
 * @param width the width of the image
 * @param height the height of the image
 */
public JpegEncoder_Huffman(int Width,int Height)
{
	bits = new Vector();
	bits.addElement(bitsDCluminance);
	bits.addElement(bitsACluminance);
	bits.addElement(bitsDCchrominance);
	bits.addElement(bitsACchrominance);

	val = new Vector();
	val.addElement(valDCluminance);
	val.addElement(valACluminance);
	val.addElement(valDCchrominance);
	val.addElement(valACchrominance);
	
	initHuf();
	//__code=code;
	imageWidth=Width;
	imageHeight=Height;
}

/**
 * Here for backwards compatability with older code.
 */
public void HuffmanBlockEncoder(BufferedOutputStream outStream, int zigzag[],
	int prec, int CDCode, int ACCode) 
{
	huffmanBlockEncoder(outStream, zigzag, prec, CDCode, ACCode);
}

/**
 * huffmanBlockEncoder run length encodes and Huffman encodes the quantized
 * data.
 *
 * @param outStream the outputStream to which to encode the data
 * @param zigzag ?
 * @param prec ??
 * @param DCCode ??
 * @param ACCode ??
 */
public void huffmanBlockEncoder(BufferedOutputStream outStream, 
	int zigzag[], int prec, int CDCode, int ACCode)
{
	int k, i, r;
	int nbits;
	int temp;
	int temp2;

	numDCTables = 2;
	numACTables = 2;

	// The DC portion
	temp = temp2 = zigzag[0] - prec;
	if(temp < 0) {
		temp = -temp;
		temp2--;
	}
	
	nbits = 0;
	while (temp != 0) {
		nbits++;
		temp >>= 1;
	}
	
	//if (nbits > 11) nbits = 11;
	bufferIt(outStream, ((int[][])DCmatrix[CDCode])[nbits][0], 
		((int[][])DCmatrix[CDCode])[nbits][1]);
	// The arguments in bufferIt are code and size.
	if (nbits != 0) {
		bufferIt(outStream, temp2, nbits);
	}

	// The AC portion
	r = 0;
	for (k = 1; k < 64; k++) {
		if ((temp = zigzag[jpegNaturalOrder[k]]) == 0) {
			r++;
		}
		else {
			while (r > 15) {
				bufferIt(outStream, 
					((int[][])ACmatrix[ACCode])[0xF0][0], 
					((int[][])ACmatrix[ACCode])[0xF0][1]);
				r -= 16;
			}
			temp2 = temp;
			if (temp < 0) {
				temp = -temp;
				temp2--;
			}
			nbits = 1;
			while ((temp >>= 1) != 0) {
				nbits++;
			}
			i = (r << 4) + nbits;
			bufferIt(outStream, 
				((int[][])ACmatrix[ACCode])[i][0], 
				((int[][])ACmatrix[ACCode])[i][1]);
			bufferIt(outStream, temp2, nbits);
			r = 0;
		}
	}
	
	if (r > 0) {
		bufferIt(outStream, 
			((int[][])ACmatrix[ACCode])[0][0], 
			((int[][])ACmatrix[ACCode])[0][1]);
	}
}

/**
 * Uses an integer long (32 bits) buffer to store the Huffman encoded bits
 * and sends them to outStream by the byte.
 * 
 * @param outStream the outputstream on which they are written
 */
public void bufferIt(BufferedOutputStream outStream, int code,int size)
{
	int putBits = __bufferPutBits;
	int putBuffer = code;

	putBuffer &= (1 << size) - 1;
	putBits += size;
	putBuffer <<= 24 - putBits;
	putBuffer |= __bufferPutBuffer;

	while(putBits >= 8) {
		int c = ((putBuffer >> 16) & 0xFF);
		try {
			outStream.write(c);
		}
		catch (IOException e) {
			Message.printWarning ( 2,
				"JpegEncoder.HuffmanBlockEncoder",
				"IO Error: " + e.getMessage());
		}
		if (c == 0xFF) {
			try {
				outStream.write(0);
			}
			catch (IOException e) {
				Message.printWarning ( 2,
					"JpegEncoder.HuffmanBlockEncoder",
				"IO Error: " + e.getMessage());
			}
		}
		putBuffer <<= 8;
		putBits -= 8;
	}
	
	__bufferPutBuffer = putBuffer;
	__bufferPutBits = putBits;
}

/**
 * Flushes the buffer to the given outputStream
 *
 * @param outStream the outputStream which to flush
 */
public void flushBuffer(BufferedOutputStream outStream) {
	int putBuffer = __bufferPutBuffer;
	int putBits = __bufferPutBits;

	while (putBits >= 8) {
		int c = ((putBuffer >> 16) & 0xFF);
		try {
			outStream.write(c);
		}
		catch (IOException e) {
			Message.printWarning ( 2,
				"JpegEncoder.flushBuffer",
				"IO Error: " + e.getMessage());
		}
	
		if (c == 0xFF) {
			try {
				outStream.write(0);
			}
			catch (IOException e) {
				Message.printWarning ( 2,
					"JpegEncoder.flushBuffer",
					"IO Error: " + e.getMessage());
			}
		}

		putBuffer <<= 8;
		putBits -= 8;
	}

	if (putBits > 0) {
		int c = ((putBuffer >> 16) & 0xFF);
		try {
			outStream.write(c);
		}
		catch (IOException e) {
			Message.printWarning ( 2,
				"JpegEncoder.flushBuffer",
				"IO Error: " + e.getMessage());
		}
	}
}

/**
 * Initialisation of the Huffman codes for Luminance and Chrominance.
 * This code results in the same tables created in the IJG Jpeg-6a
 * library.
 */

public void initHuf()
{
	int code;
	int i, l, p;
	int lastp;
	int si;
	int[] huffcode= new int[257];
	int[] huffsize = new int[257];
	
	DCmatrix0=new int[12][2];
	DCmatrix1=new int[12][2];
	ACmatrix0=new int[255][2];
	ACmatrix1=new int[255][2];
	DCmatrix = new Object[2];
	ACmatrix = new Object[2];

	/**
	 * init of the DC values for the chrominance
	 * [][0] is the code   [][1] is the number of bit
	 */

	p = 0;
	for (l = 1; l <= 16; l++) {
		for (i = 1; i <= bitsDCchrominance[l]; i++) {
			huffsize[p++] = l;
		}
	}

	huffsize[p] = 0;
	lastp = p;
	
	code = 0;
	si = huffsize[0];
	p = 0;
	while(huffsize[p] != 0) {
		while(huffsize[p] == si) {
			huffcode[p++] = code;
			code++;
		}
		code <<= 1;
		si++;
	}

	for (p = 0; p < lastp; p++) {
		DCmatrix1[valDCchrominance[p]][0] = huffcode[p];
		DCmatrix1[valDCchrominance[p]][1] = huffsize[p];
	}

	/**
	 * Init of the AC hufmann code for the chrominance matrix [][][0] 
	 * is the code & matrix[][][1] is the number of bit needed
	 */

	p = 0;
	for (l = 1; l <= 16; l++) {
		for (i = 1; i <= bitsACchrominance[l]; i++) {
			huffsize[p++] = l;
		}
	}
	
	huffsize[p] = 0;
	lastp = p;

	code = 0;
	si = huffsize[0];
	p = 0;

	while(huffsize[p] != 0) {
		while(huffsize[p] == si) {
			huffcode[p++] = code;
			code++;
		}
		code <<= 1;
		si++;
	}

	for (p = 0; p < lastp; p++) {
		ACmatrix1[valACchrominance[p]][0] = huffcode[p];
		ACmatrix1[valACchrominance[p]][1] = huffsize[p];
	}

	/**
	 * init of the DC values for the luminance
	 * [][0] is the code   [][1] is the number of bit
	 */
	p = 0;
	for (l = 1; l <= 16; l++) {
		for (i = 1; i <= bitsDCluminance[l]; i++) {
			huffsize[p++] = l;
		}
	}
	
	huffsize[p] = 0;
	lastp = p;

	code = 0;
	si = huffsize[0];
	p = 0;
	while(huffsize[p] != 0) {
		while(huffsize[p] == si) {
			huffcode[p++] = code;
			code++;
		}
		code <<= 1;
		si++;
	}

	for (p = 0; p < lastp; p++) {
		DCmatrix0[valDCluminance[p]][0] = huffcode[p];
		DCmatrix0[valDCluminance[p]][1] = huffsize[p];
	}

	/**
	 * Init of the AC hufmann code for luminance
	 * matrix [][][0] is the code & matrix[][][1] is the number of bit
	 */
	p = 0;
	for (l = 1; l <= 16; l++) {
		for (i = 1; i <= bitsACluminance[l]; i++) {
			huffsize[p++] = l;
		}
	}
	
	huffsize[p] = 0;
	lastp = p;

	code = 0;
	si = huffsize[0];
	p = 0;
	while(huffsize[p] != 0) {
		while(huffsize[p] == si) {
			huffcode[p++] = code;
			code++;
		}
		code <<= 1;
		si++;
	}

	for (int q = 0; q < lastp; q++) {
		ACmatrix0[valACluminance[q]][0] = huffcode[q];
		ACmatrix0[valACluminance[q]][1] = huffsize[q];
	} 

	DCmatrix[0] = DCmatrix0;
	DCmatrix[1] = DCmatrix1;
	ACmatrix[0] = ACmatrix0;
	ACmatrix[1] = ACmatrix1;
}
}

