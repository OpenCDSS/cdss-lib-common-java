// Code to handle GIF files

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

// History:
//
// 2007-05-08  Steven A. Malers, RTi  Clean up based on Eclipse feedback.

package RTi.GR;

import java.io.IOException;
import java.io.OutputStream;

import RTi.Util.Message.Message;

class Packetizer {
    OutputStream os = null;
    byte[] packet = new byte[256];
    int count = 0;
    Packetizer(OutputStream os) {
	this.os = os;
	count = 0;
    }
    void flush() throws IOException {
	if (count > 0) {
	    System.out.println("Packetizer flushing " + count + " bytes");
	    os.write(count);
	    os.write(packet, 0, count);
	    int i;
	    for (i = 0; i < count; i++) {
		System.out.println("Byte " + i + " is " + (packet[i]&0xff));
	    }
	    count = 0;
	}
    }
    void write(int c) throws IOException {
	packet[count++] = (byte)c;
	if (count == 255) {
	    flush();
	}
    }
}

class BitStream {
    Packetizer packetizer;
    BitStream(OutputStream os) {
	packetizer = new Packetizer(os);
    }
    int currentBits = 0;
    int accumulator = 0;
    void write(int code, int nBits) throws IOException {
	System.out.println("Bitstream writing " + code + " as " +
			   nBits + " bits");
	if (currentBits > 0) {
	    accumulator &= (1<<currentBits)-1;
	    accumulator |= code<<currentBits;
	} else {
	    accumulator = code;
	}
	currentBits += nBits;
	flush(8);
    }
    void flush(int bitsLeft) throws IOException {
	while (currentBits > bitsLeft) {
	    packetizer.write(accumulator&0xff);
	    accumulator >>= 8;
	    currentBits -= 8;
	}
    }
    void flush() throws IOException {
	flush(0);
	packetizer.flush();
    }
}

class Compressor {
    OutputStream os;
    BitStream bitStream;
    int initBits;
    int clearCode;
    int eofCode;
    int nBits;
    int maxCode;
    int freeEntry;
    Compressor(int bitsPerCode, OutputStream os) throws IOException {
	int initCodeSize = (bitsPerCode <= 1 ? 2 : bitsPerCode);
	initBits = initCodeSize + 1;
	clearCode = (1 << (initBits - 1));
	eofCode = clearCode + 1;
	this.os = os;
	// Write the initial code size
	os.write(initCodeSize);
	bitStream = new BitStream(os);
	nBits = initBits;
	clearHashTable();
    }
    /**
     * Clear out the hash table
     */
    void clearHashTable() throws IOException {
	// Output the clear code
	bitStream.write(clearCode, nBits);
	freeEntry = eofCode + 1;
	nBits = initBits;
	maxCode = (1<<nBits) - 1;
	int i;
	for (i = 0; i < HASH_TABLE_SIZE; i++) {
	    hashTable[i] = -1;
	}
    }
    /**
     * Hash table to look up prefix/suffix pairs
     * This table contains the search keys
     */
    int[] hashTable = new int[HASH_TABLE_SIZE];
    /**
     * Code mappings for hash table entries
     * i.e. search key -> new code value
     */
    int[] codeTable = new int[HASH_TABLE_SIZE];
    /**
     * Size of the hash table
     * This gives an occupancy of 4096/5003 (over 80%)
     * since we can have at most 4096 (2^MAX_BITS) codes to look up
     */
    final static int HASH_TABLE_SIZE = 5003;
    /**
     * Maximum bits allowed for encoding
     */
    final static int MAX_BITS = 12;
    /**
     * Maximum code for maximum bits; never generated
     */
    final static int MAX_MAX_CODE = 1<<MAX_BITS;
    /**
     * Shift amount for generating primary hash code
     */
    final static int HASH_SHIFT = 4;
    /**
     * Compress a data stream
     */
    void noCompress(byte[] data, int length) throws IOException {
	int byteNo;
	for (byteNo = 1; byteNo < length; byteNo++) {
	    int c = data[byteNo]&0xff;
	    bitStream.write(c, nBits);
	}
	// Write the end of the compressed stream
	bitStream.write(eofCode, nBits);
	bitStream.flush();
	// Write the block terminator
	os.write(0);
    }
    void compress(byte[] data, int length) throws IOException {
	int prefixCode = data[0]&0xff;
	int byteNo;
	for (byteNo = 1; byteNo < length; byteNo++) {
	    int suffixCode = data[byteNo]&0xff;
	    // look up the prefix followed by suffix in the hash table
	    int searchKey = (suffixCode << MAX_BITS) + prefixCode;
	    int hash = (suffixCode << HASH_SHIFT) ^ prefixCode;
	    if (hashTable[hash] != searchKey &&
		hashTable[hash] >= 0) {
		// not found on primary hash; try secondary hash
		int displacement;
		if (hash == 0) {
		    displacement = 1;
		} else {
		    displacement = HASH_TABLE_SIZE - hash;
		}
		do {
		    hash -= displacement;
		    if (hash < 0) {
			hash += HASH_TABLE_SIZE;
		    }
		} while (hashTable[hash] != searchKey &&
			 hashTable[hash] >= 0);
	    }
	    if (hashTable[hash] == searchKey) {
		// we have a code for prefix followed by suffix
		prefixCode = codeTable[hash];
		continue;
	    } else {
		// empty slot
		bitStream.write(prefixCode, nBits);
		// If the next entry is going to be too big for the code size,
		// then increase it, if possible.
		if (freeEntry > maxCode) {
		    nBits++;
		    if (nBits == MAX_BITS) {
			maxCode = MAX_MAX_CODE;
		    } else {
			maxCode = (1<<nBits) - 1;
		    }
		}
		prefixCode = suffixCode;

		if (freeEntry < MAX_MAX_CODE) {
		    // put this entry in the hash table
		    hashTable[hash] = searchKey;
		    codeTable[hash] = freeEntry++;
		} else {
		    // Clear out the hash table
		    clearHashTable();
		}
	    }
	}
	// Write the final code
	bitStream.write(prefixCode, nBits);
	// If the next entry is going to be too big for the code size,
	// then increase it, if possible.
	if (freeEntry > maxCode) {
	    nBits++;
	    if (nBits == MAX_BITS) {
		maxCode = MAX_MAX_CODE;
	    } else {
		maxCode = (1<<nBits) - 1;
	    }
	}
	// Write the end of the compressed stream
	bitStream.write(eofCode, nBits);
	bitStream.flush();
	// Write the block terminator
	os.write(0);
    }
}

public class GIFEncode {
    static void putWord(OutputStream os, int v) throws IOException {
	putByte(os, v);
	putByte(os, v>>8);
    }
    static void putByte(OutputStream os, int v) throws IOException {
	os.write(v&0xff);
    }
    static void writeImageData(int bitsPerPixel, int width, int height,
			byte[] pixels, OutputStream os) throws IOException {
	System.out.println("Writing image data " + bitsPerPixel + " " +
			   width + " " + height);
	// Compress the data
	Compressor compressor = new Compressor(bitsPerPixel, os);
	compressor.compress(pixels, width*height);
    }
    static void encode(int width, int height,
		       int numberColors,
		       byte[] red, byte[] green, byte[] blue,
		       byte[] pixels, OutputStream os) throws IOException {

	// Determine the number of bits per pixel
	int bitsPerPixel;
	for (bitsPerPixel = 1; bitsPerPixel < 8; bitsPerPixel++) {
	    if ((1<<bitsPerPixel) >= numberColors) {
		break;
	    }
	}
	Message.printStatus ( 1, "encode", "bitsPerPixel = " + bitsPerPixel );
	int colorMapSize = 1<<bitsPerPixel;
	Message.printStatus ( 1, "encode", "colorMapSize = " + colorMapSize );

	// Write the Header
	String signature = "GIF";
	String version = "87a";
	putByte(os, signature.charAt(0));
	putByte(os, signature.charAt(1));
	putByte(os, signature.charAt(2));
	putByte(os, version.charAt(0));
	putByte(os, version.charAt(1));
	putByte(os, version.charAt(2));

	// Write the Logical Screen Descriptor
	boolean globalColorTable = true;
	int colorResolution = 8;
	boolean globalSortFlag = false;
	int backgroundColorIndex = 0;
	int pixelAspectRatio = 0;
	putWord(os, width);
	putWord(os, height);
	int flags = 0;
	flags |= (globalColorTable ? 1 : 0) << 7;
	flags |= (colorResolution - 1) << 4;
	flags |= (globalSortFlag ? 1 : 0) << 3;
	flags |= (bitsPerPixel - 1);
	putByte(os, flags);
	putByte(os, backgroundColorIndex);
	putByte(os, pixelAspectRatio);

	// Write the global color table
	int i;
	for (i = 0; i < colorMapSize; i++) {
	    byte r, g, b;
	    if (i < numberColors) {
		r = red[i];
		g = green[i];
		b = blue[i];
	    } else {
		r = 0;
		g = 0;
		b = 0;
	    }
	    putByte(os, r);
	    putByte(os, g);
	    putByte(os, b);
	}

	// Write the image descriptor
	byte imageSeparator = 0x2C;
	int imageLeftPosition = 0;
	int imageTopPosition = 0;
	int imageWidth = width;
	int imageHeight = height;
	boolean localColorTableFlag = false;
	boolean interlaceFlag = false;
	boolean localSortFlag = false;
	int localColorTableSize = 0;
	putByte(os, imageSeparator);
	putWord(os, imageLeftPosition);
	putWord(os, imageTopPosition);
	putWord(os, imageWidth);
	putWord(os, imageHeight);
	int imageFlags = 0;
	imageFlags |= (localColorTableFlag ? 1 : 0) << 7;
	imageFlags |= (interlaceFlag ? 1 : 0) << 6;
	imageFlags |= (localSortFlag ? 1 : 0) << 5;
	imageFlags |= localColorTableSize;
	putByte(os, imageFlags);

	// Write the image data
	writeImageData(bitsPerPixel, width, height, pixels, os);

	// Write Trailer
	byte trailer = 0x3B;
	putByte(os, trailer);
    }
}
