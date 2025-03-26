// EndianDataOutputStream - write little or big endian binary data streams

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

package RTi.Util.IO;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
This class extends the basic DataOutputStream class by providing LittleEndian
versions of some write methods.  This allows one output stream to be used to
write big and little endian values.  By default, Java assumes big endian
data, and unicode (2-byte) strings.  Use this class if there is a chance that
binary files may be written on multiple platforms and a standard needs to be enforced.
@see java.io.DataOutputStream
*/
public class EndianDataOutputStream 
extends DataOutputStream {

/**
Boolean to specify whether the stream is big endian or not.  The Java default is
that streams ARE big endian.
*/
private boolean __is_big_endian = true;

/**
Specifies whether to match the current system's endianness.
*/
private boolean __match_system = false;

/**
Byte array used for writing to the stream.
*/
private byte __byte8[] = null;

/**
Construct using an OutputStream.
@param ostream the OutputStream from which to construct this Endian 
output stream.
*/
public EndianDataOutputStream(OutputStream ostream)
{	super(ostream);
	__byte8 = new byte[8];
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Construct using an OutputStream.
@param ostream the OutputStream from which to construct this Endian 
output stream.
@param match_system whether to match the system's current endianness when
writing from the stream.
*/
public EndianDataOutputStream(OutputStream ostream, boolean match_system)
{	super(ostream);
	__byte8 = new byte[8];
	__match_system = match_system;
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Indicate whether the stream is big-endian.  The default is to match the
endian-ness of the machine but setBigEndian() can be used to explicitly indicate
that the stream is little endian.  For example, if a binary file is written on
one machine and transferred to another of different endian-ness, then the
endian-ness of the stream should be based on the stream, not the machine.
@return true if the stream should be treated as big-endian, false
if it should be treated as little-endian.
*/
public boolean isBigEndian()
{	return __is_big_endian;
}

/**
Set whether the stream is big-endian.  The default is to match the
endian-ness of the machine but setBigEndian() can be used to explicitly indicate
that the stream is little endian.  For example, if a binary file is written on
one machine and transferred to another of different endian-ness, then the
endian-ness of the stream should be based on the stream, not the machine.
Note that once the endian-ness is set (either by default or with this method),
the writeEndian*() methods can only be called if the match_system parameter
was set to true at construction.
@param is_big_endian true if the stream should be treated as big-endian, false
if it should be treated as little-endian.
*/
public void setBigEndian(boolean is_big_endian)
{	__is_big_endian = is_big_endian;
}

/**
Write a 1-byte (not Unicode) character to the stream using a big-endian format.
The character is written as a single byte.
@param c Character to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeChar1(char c)
throws IOException
{	__byte8[0] = (byte)c;
	write(__byte8, 0, 1);
}

/**
Write a string as 1-byte (not Unicode) characters to the stream using a 
big-endian format.
Each character is written as a single byte.
@param s String to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeChar1(String s)
throws IOException
{	int len = s.length();
	for (int i = 0; i < len; i++) {
		// Get the character from the first byte from every
		// Unicode character...
		__byte8[0] = (byte)s.charAt(i);
		write(__byte8, 0, 1);
	}
}

/**
Write a 2-byte Unicode character to the stream using an endian-ness that matches
the system.  match_system=true should be used in the constructor if this method
is called - otherwise Java big-endian is assumed.
@param c Character to write, as its Unicode value.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianChar(int c)
throws IOException
{	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianChar(c);
	}
	else {	
		// Default Java or system that is big-endian...
		writeChar(c);
	}
}

/**
Write a 1-byte (not Unicode) character to the stream using an endian-ness that
matches the system.  match_system=true should be used in the constructor if this
method is called - otherwise Java big-endian is assumed.  The character is
written as a single byte.
@param c Character to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianChar1(char c)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianChar1(c);
	}
	else {	
		// Default Java or system that is big-endian...
		__byte8[0] = (byte)c;
		write(__byte8, 0, 1);
	}
}

/**
Write a string as 1-byte (not Unicode) characters to the stream using an
endian-ness that matches the system.  match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed.
Each character is written as a single byte.
@param s String to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianChar1(String s)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianChar1(s);
	}
	else {	
		// Default Java or system that is big-endian...
		int len = s.length();
		for (int i = 0; i < len; i++) {
			__byte8[0] = (byte)s.charAt(i);
			write(__byte8, 0, 1);
		}
	}
}

/**
Write a string as 2-byte (Unicode) characters to the stream using an
endian-ness that matches the system.  match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed.
Each character is written as two-bytes.
@param s String to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianChars(String s)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianChars(s);
	}
	else {	
		// Default Java or system that is big-endian...
		writeChars(s);
	}
}

/**
Write a 64-bit double to the stream using an
endian-ness that matches the system.  match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed.
@param d 64-bit double to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianDouble(double d)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianDouble(d);
	}
	else {	
		// Default Java or system that is big-endian...
		writeDouble(d);
	}
}

/**
Write a 32-bit float to the stream using an
endian-ness that matches the system.  match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed.
@param f 32-bit float to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianFloat(float f)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianFloat(f);
	}
	else {	
		// Default Java or system that is big-endian...
		writeFloat(f);
	}
}

/**
Write a 32-bit integer to the stream using an
endian-ness that matches the system.  match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed.
@param i 32-bit integer to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeEndianInt(int i)
throws IOException {
	if (__match_system && !__is_big_endian) {
		// Little endian system...
		writeLittleEndianInt(i);
	}
	else {	
		// Default Java or system that is big-endian...
		writeInt(i);
	}
}

/**
Write a little endian char (2-byte Unicode), specifying the character as its
Unicode integer equivalent.
@param c Int to write.
@exception IOException if there is an error writing the stream.
*/
public void writeLittleEndianChar(int c)
throws IOException {
	//same as for shorts...

	__byte8[0] = (byte)c;
	__byte8[1] = (byte)(c >> 8);

	write(__byte8, 0, 2);
}

/**
Write a little endian 8-bit char (not Unicode).
@param c Char to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianChar1(char c)
throws IOException {
	__byte8[0] = (byte)c;
	write(__byte8, 0, 1);
}

/**
Write a little endian 8-bit byte.
@param b byte to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianByte(byte b)
throws IOException {
	__byte8[0] = (byte)b;
	write(__byte8, 0, 1);
}

/**
Write a String as little endian 8-bit chars (not Unicode).
@param s String to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianChar1(String s)
throws IOException {
	int size = s.length();

	if (size > __byte8.length) {
		// Reallocate the array...
		__byte8 = new byte[size];
		for (int i = 0; i < size; i++) {
			__byte8[i] = (byte)s.charAt(i);
		}

		write(__byte8, 0, size);
	} 
	else {	
		for (int i = 0; i < size; i++) {
			__byte8[i] = (byte)s.charAt(i);
		}

		write(__byte8, 0, size);
	}
}

/**
Write a String as little endian (2-byte Unicode)chars.
@param s String to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianChars(String s)
throws IOException {
	int len = s.length();
	for (int i=0;i<len;i++) {
		writeChar(s.charAt(i));
	}
}

/**
Write a little endian 64-bit double.
@param d 64-bit double to write.
@exception IOException if there is an error writing to file.
*/
public void writeLittleEndianDouble(double d)
throws IOException {
 	//convert to floating point long
	long vl =Double.doubleToLongBits(d);

	__byte8[0] = (byte)vl;
	__byte8[1] = (byte)(vl >> 8);
	__byte8[2] = (byte)(vl >> 16);
	__byte8[3] = (byte)(vl >> 24);
	__byte8[4] = (byte)(vl >> 32);
	__byte8[5] = (byte)(vl >> 40);
	__byte8[6] = (byte)(vl >> 48);
	__byte8[7] = (byte)(vl >> 56);

	write(__byte8, 0, 8);
}

/**
Write a little endian 32-bit float.
@param f 32-bit float to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianFloat(float f)
throws IOException {
	writeLittleEndianInt(Float.floatToIntBits(f));
}

/**
Write a 32-bit little endian int.
@param i 32-bit integer to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianInt(int i)
throws IOException {
	__byte8[0] = (byte)i;
	__byte8[1] = (byte)(i >> 8);
	__byte8[2] = (byte)(i >> 16);
	__byte8[3] = (byte)(i >> 24);

	write(__byte8, 0, 4);
}

/**
Write a 16-bit little endian short integer.
@param s 16-bit short integer value to write.
@exception IOException if there is an error writing to the stream.
*/
public void writeLittleEndianShort(short s)
throws IOException {
	__byte8[0] = (byte)s;
	__byte8[1] = (byte)(s >> 8);

	write(__byte8, 0, 2);
}

}
