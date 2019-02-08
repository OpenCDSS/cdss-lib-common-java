// EndianRandomAccessFile - RandomAccessFile for big or little endian file

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

//-----------------------------------------------------------------------------
// EndianRandomAccessFile.java - RandomAccessFile for big or little endian file
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
//
// 2001-08-12	Morgan Sheedy,		Initial Implementation for reading
//		Riverside Technology,	little endians.
//		inc.
// 2001-08-15	AMS, RTi		Added methods to write out little
//					endians.
// 2001-08-27	Steven A. Malers, RTi	Review code and incorporate into RTi
//					IO package.
// 2001-08-31	AMS,RTi			Overloaded every read/write method to
//					add seek methods.
// 2001-09-26	SAM, RTi		Add readLittleEndianChar1().  Change so
//					the mode is actually passed to the base
//					class in the constructor.  Fix
//					read methods.  Many were rereading
//					bytes over the first byte in _b.
// 2003-01-23	SAM, RTi		Overload the constructor to add the
//					flag indicating whether the class should
//					by default write in the same endianness
//					as the computer.  Add readEndian*() and
//					writeEndian*() methods.  Clean up the
//					javadoc.
// 2003-03-27	SAM, RTi		Change so that the buffer array _b is
//					resized when dealing with character
//					strings.  Previously a local array was
//					often created, resulting in slower
//					performance.
// 2003-11-23	SAM, RTi		Add writeChar1() since Java does not
//					have this equivalent without dealing
//					with bytes manually.  Similarly add
//					readChar1(), and readString1().
// 2003-11-26	SAM, RTi		Add readLittleEndianString1().
// 2003-12-04	SAM, RTi		* Add isBigEndian() and setBigEndian().
// 2004-03-11	Scott Townsend		Add readLittleEndianByte().
// 2004-04-06	SAT,RTi			Add writeLittleEndianByte(...).
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;

/**
The EndianRandomAccessFile class is used to read and write little and big endian
values to/from a random access file.  The RandomAccessFile base class big endian
Java methods are available and "LittleEndian" versions are available in this
class for reading/writing little endian data.  Consequently, it is possible to
open a binary file and read/write little and big endian values for the same
file.  More often, a file will include data of one endian type.  For example,
temporary binary files written with a C, C++, or Fortran program on a little
endian machine will contain little endian values.  In this case, an
EndianRandomAccessFile can be constructed with match_system=true and then the
readEndian*() and writeEndian*() methods can be used.  If a binary file is known
to always contain only big-endian values, then a RandomAccessFile should be
used.  If a binary file is known to always contain only little endian values,
then a EndianRandomAccessFile should be used and the LittleEndian methods should
be called.
*/
public class EndianRandomAccessFile extends RandomAccessFile {

private byte[] _b;	// Internal buffer used for byte manipulation.

private boolean __match_system = false;	// Default behavior is to act like
private boolean __is_big_endian = true;	// big-endian (Java default).

/**
Create a random access file stream to read from and/or write to.
@param file File to read from and/or write to.
@param mode String indicating access mode.  Specify "r" to only read from the
file (the file should already exist).  Specify "rw" to read from and/or write
to the file (in this case it may be important to delete the file first so that
new data are not confused with the existing contents).
@exception IOException - if the file can not be found or read, if 
	the mode is not equal to "r" or "rw", or if the security permissions
	prevent read and/or write permissions.
*/
public EndianRandomAccessFile ( File file, String mode ) 
throws IOException
{	this ( file, mode, false );
}

/**
Create a random access file stream to read from and/or write to.
@param file File to read from and/or write to.
@param mode String indicating access mode.  Specify "r" to only read from the
file (the file should already exist).  Specify "rw" to read from and/or write
to the file (in this case it may be important to delete the file first so that
new data are not confused with the existing contents).
@param match_system If true, then the endianness of data will match the
operating system when calling the readEndian*() or writeEndian*() methods.  For
example, if true and the system is big-endian, a call to
writeEndianDouble() will result in the base class RandomAccessFile.writeDouble()
method being called.  If true and the system is little-endian, then a call to
writeEndianDouble() will result in a call to littleEndianWriteDouble().  Use
this version if the file endian-ness will always be the same as the operating
system endian-ness.
@exception IOException - if the file can not be found or read, if 
	the mode is not equal to "r" or "rw", or if the security permissions
	prevent read and/or write permissions.
*/
public EndianRandomAccessFile(File file, String mode, boolean match_system ) 
		throws IOException
{	super(file, mode);

	//make byte array
	_b = new byte[8];

	__match_system = match_system;
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Create a random access file stream to read from and/or write to.
@param file The name of a file to read from and/or write to.
@param mode String indicating access mode.  Specify "r" to only read from the
file (the file should already exist).  Specify "rw" to read from and/or write
to the file (in this case it may be important to delete the file first so that
new data is not confused with the existing contents).
@exception IOException - if the file can not be found or read, if 
	the mode is not equal to "r" or "rw", or if the security permissions
	prevent read and/or write permissions.
*/
public EndianRandomAccessFile(String file, String mode) 
throws IOException
{	this ( file, mode, false );
}

/**
Create a random access file stream to read from and/or write to.
@param file The name of a file to read from and/or write to.
@param mode String indicating access mode.  Specify "r" to only read from the
file (the file should already exist).  Specify "rw" to read from and/or write
to the file (in this case it may be important to delete the file first so that
new data are not confused with the existing contents).
@param match_system If true, then the endianness of data will match the
operating system when calling the readEndian*() or writeEndian*() methods.  For
example, if true and the system is big-endian, a call to
writeEndianDouble() will result in the base class RandomAccessFile.writeDouble()
method being called.  If true and the system is little-endian, then a call to
writeEndianDouble() will result in a call to littleEndianWriteDouble().  Use
this version if the file endian-ness will always be the same as the operating
system endian-ness.
@exception IOException - if the file can not be found or read, if 
	the mode is not equal to "r" or "rw", or if the security permissions
	prevent read and/or write permissions.
*/
public EndianRandomAccessFile(String file, String mode, boolean match_system )
throws IOException
{	super(file, mode);
	//make byte array
	_b = new byte[8];
	__match_system = match_system;
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Finalizes and cleans up.
@exception Throwable if there is an error.
*/
public void finalize() throws Throwable {
	_b = null;
	super.finalize();
}

/**
Indicate whether the file is big-endian.  The default is to match the
endian-ness of the machine but setBigEndian() can be used to explicitly indicate
that the file is little endian.  For example, if a binary file is written on
one machine and transferred to another of different endian-ness, then the
endian-ness of the file should be based on the file, not the machine.
@return true if the file should be treated as big-endian, false
if it should be treated as little-endian.
*/
public boolean isBigEndian ()
{	return __is_big_endian;
}

/**
Similar to readChar except read a one-byte big-endian character.
@return a character read from a one-byte big endian character.
@exception IOException if there is a read error.
*/
public final char readChar1() throws IOException
{	if ( read(_b, 0, 1) != 1 ) {
		throw new IOException();
	}
	return (char) ( _b[0] );
}

/**
Read a 64-bit double from the file using an endian-ness that matches the system
(match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the 64-bit double read from the file.
*/
public double readEndianDouble () throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianDouble();
	}
	else {	// Default Java or system that is big-endian...
		return readDouble();
	}
}

/**
Read a 32-bit float from the file using an endian-ness that matches the system
(match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the 32-bit float read from the file.
*/
public float readEndianFloat () throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianFloat();
	}
	else {	// Default Java or system that is big-endian...
		return readFloat();
	}
}

/**
Read a 32-bit int from the file using an endian-ness that matches the system
(match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the 32-bit int read from the file.
*/
public int readEndianInt () throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianInt();
	}
	else {	// Default Java or system that is big-endian...
		return readInt();
	}
}

/**
Read a 64-bit long integer from the file using an endian-ness that matches the
system (match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the 64-bit long integer read from the file.
*/
public long readEndianLong () throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianLong();
	}
	else {	// Default Java or system that is big-endian...
		return readLong();
	}
}

/**
Read a signed 16-bit integer from the file using an endian-ness that matches the
system (match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the signed 16-bit integer read from the file.
*/
public short readEndianShort () throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianShort();
	}
	else {	// Default Java or system that is big-endian...
		return readShort();
	}
}

/**
Similar to readByte() except read a little endian 1 byte (8-bit)
character. 
@return a byte read from a little endian byte.
@exception IOException if there is a read error.
*/
public final byte readLittleEndianByte() throws IOException
{	if ( read(_b, 0, 1) != 1 ) {
		throw new IOException();
	}
	// Treat as a byte...
	return (byte)(_b[0]&0xff);
}

/**
Similar to readChar() except read a little endian 1 byte (8-bit)
character.  The 8-bit character is converted to a 16-bit (Unicode) character,
which is returned.
@return a character read from a little endian character.
@exception IOException if there is a read error.
*/
public final char readLittleEndianChar1() throws IOException
{	if ( read(_b, 0, 1) != 1 ) {
		throw new IOException();
	}
	// Treat as a byte...
	return (char)(_b[0]&0xff);
}

/**
Read a 64-bit little endian double.
@return Value for the 64-bit double.  
@exception IOException if there is an error reading from the file.
*/
public final double readLittleEndianDouble() throws IOException
{	return Double.longBitsToDouble(readLittleEndianLong());
}

/**
Read a 32-bit little endian float.
@return Value for the 32-bit float.  
@exception IOException if there is an error reading from the file.
*/
public final float readLittleEndianFloat() throws IOException
{	return Float.intBitsToFloat(readLittleEndianInt());
}

/**
Read a little endian 64-bit double starting at the specified file location.
@return Value for the 64-bit double.
@param offset  Number of bytes from the beginning of the file to begin 
	reading the double.
@exception IOException if the offset value passed in is negative or there is an
error reading the file.
*/
public final double readLittleEndianDouble(int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);
	return Double.longBitsToDouble(readLittleEndianLong());
}

/**
Read a little endian 32-bit integer.
@return Value for the 32-bit integer.  
@exception IOException if there is an error reading the file.
*/
public final int readLittleEndianInt() throws IOException
{	if ( read(_b, 0, 4) != 4 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return	(_b[3]&0xff) << 24 |
		(_b[2]&0xff) << 16 |
		(_b[1]&0xff) <<  8 |
            	(_b[0]&0xff);
}

/**
Read a little endian 32-bit integer starting at the specified location.
@return Value for the 32-bit integer. 
@param offset  Number of bytes from the beginning of the file to begin 
	reading the integer.
@exception IOException if the offset value passed in is negative or if
there is an error reading the file.
*/
public final int readLittleEndianInt(int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	if ( read(_b, 0, 4) != 4 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return	(_b[3]&0xff) << 24 |
		(_b[2]&0xff) << 16 |
		(_b[1]&0xff) <<  8 |
            	(_b[0]&0xff);
}

/**
Read a 64-bit little endian long integer.
@return a 64-bit long integer that is read.
@exception IOException if there is a read error.
*/
public final long readLittleEndianLong() throws IOException
{	if ( read(_b, 0, 8) != 8 ) {
		throw new IOException( "Premature end of file." );
	}
	return	(long)(_b[7]&0xff) << 56 |
		(long)(_b[6]&0xff) << 48 |
		(long)(_b[5]&0xff) << 40 |
		(long)(_b[4]&0xff) << 32 |
		(long)(_b[3]&0xff) << 24 |
		(long)(_b[2]&0xff) << 16 |
		(long)(_b[1]&0xff) <<  8 |
		(long)(_b[0]&0xff);
}

/**
Read a 16-bit little endian short integer value.
@return Value for the 16-bit short integer. 
@exception IOException if there is a problem reading the file.
*/
public final short readLittleEndianShort() throws IOException
{	if ( read(_b, 0, 2) != 2 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return (short)( (_b[1]&0xff) << 8 | (_b[0]&0xff));
}

/**
Read a 16-bit little endian short integer value starting at the specified file
location.
@return Value for the 16-bit short integer.  
@param offset  Number of bytes from the beginning of the file to 
	begin reading the short integer.
@exception IOException if the offset value passed in is negative or if
there is an error reading from the file.
*/
public final short readLittleEndianShort(int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);
	if ( read(_b, 0, 2) != 2 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return (short)( (_b[1]&0xff) << 8 | (_b[0]&0xff));
}

/**
Read a string of one-byte little endian characters.
@return a String read from a 1-byte character string.
@param size Number of characters to read.
@exception IOException if there is a read error.
*/
public final String readLittleEndianString1( int size) throws IOException
{	char [] c = new char[size];
	for ( int i = 0; i < size; i++ ) {
		c[i] = readLittleEndianChar1();
	}
	String s = new String ( c );
	c = null;
	return s;
}

/**
Read a string of one-byte characters.
@return a String read from a 1-byte character string.
@param size Number of characters to read.
@exception IOException if there is a read error.
*/
public final String readString1( int size) throws IOException
{	char [] c = new char[size];
	for ( int i = 0; i < size; i++ ) {
		if ( read(_b, 0, 1) != 1 ) {
			throw new IOException();
		}
		c[i] = (char) ( _b[0] );
	}
	String s = new String ( c );
	c = null;
	return s;
}

/**
Set whether the file is big-endian.  The default is to match the
endian-ness of the machine but setBigEndian() can be used to explicitly indicate
that the file is little endian.  For example, if a binary file is written on
one machine and transferred to another of different endian-ness, then the
endian-ness of the file should be based on the file, not the machine.
Note that once the endian-ness is set (either by default or with this method),
the writeEndian*() methods can only be called if the match_system parameter
was set to true at construction.
@param is_big_endian true if the file should be treated as big-endian, false
if it should be treated as little-endian.
*/
public void setBigEndian ( boolean is_big_endian )
{	__is_big_endian = is_big_endian;
}

/**
Write a 1-byte (not Unicode) character to the file using a big-endian format.
The character is written as a single byte.
@param c Character to write.
@exception IOException if there is an error writing to the file.
*/
public void writeChar1 ( char c ) throws IOException
{	_b[0] = (byte)c;
	write ( _b, 0, 1 );
}

/**
Write a string as 1-byte (not Unicode) characters to the file using a big-endian
format.
Each character is written as a single byte.
@param s String to write.
@exception IOException if there is an error writing to the file.
*/
public void writeChar1 ( String s ) throws IOException
{	int len = s.length();
	for ( int i = 0; i < len; i++ ) {
		_b[0] = (byte)s.charAt(i);
		write ( _b, 0, 1 );
	}
}

/**
Write a 2-byte Unicode character to the file using an endian-ness that matches
the system (match_system=true should be used in the constructor if this method
is called - otherwise Java big-endian is assumed).
@param c Character to write, as its Unicode value.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianChar ( int c ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianChar ( c );
	}
	else {	// Default Java or system that is big-endian...
		writeChar ( c );
	}
}

/**
Write a 1-byte (not Unicode) character to the file using an endian-ness that
matches the system (match_system=true should be used in the constructor if this
method is called - otherwise Java big-endian is assumed).  The character is
written as a single byte.
@param c Character to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianChar1 ( char c ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianChar1 ( c );
	}
	else {	// Default Java or system that is big-endian...
		_b[0] = (byte)c;
		write ( _b, 0, 1 );
	}
}

/**
Write a string as 1-byte (not Unicode) characters to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
Each character is written as a single byte.
@param s String to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianChar1 ( String s ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianChar1 ( s );
	}
	else {	// Default Java or system that is big-endian...
		int len = s.length();
		for ( int i = 0; i < len; i++ ) {
			_b[0] = (byte)s.charAt(i);
			write ( _b, 0, 1 );
		}
	}
}

/**
Write a string as 2-byte (Unicode) characters to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
Each character is written as two-bytes.
@param s String to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianChars ( String s ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianChars ( s );
	}
	else {	// Default Java or system that is big-endian...
		writeChars ( s );
	}
}

/**
Write a 64-bit double to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
@param d 64-bit double to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianDouble ( double d ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianDouble ( d );
	}
	else {	// Default Java or system that is big-endian...
		writeDouble ( d );
	}
}

/**
Write a 32-bit float to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
@param f 32-bit float to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianFloat ( float f ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianFloat ( f );
	}
	else {	// Default Java or system that is big-endian...
		writeFloat ( f );
	}
}

/**
Write a 32-bit integer to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
@param i 32-bit integer to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianInt ( int i ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianInt ( i );
	}
	else {	// Default Java or system that is big-endian...
		writeInt ( i );
	}
}

/**
Write a 16-bit integer to the file using an
endian-ness that matches the system (match_system=true should be used in the
constructor if this method is called - otherwise Java big-endian is assumed).
@param i 16-bit integer to write.
@exception IOException if there is an error writing to the file.
*/
public void writeEndianShort ( short i ) throws IOException
{	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		writeLittleEndianShort ( i );
	}
	else {	// Default Java or system that is big-endian...
		writeShort ( i );
	}
}

/**
Write a little endian char (2-byte Unicode), specifying the character as its
Unicode integer equivalent.
@param c Int to write.
@exception IOException if there is an error writing the file.
*/
public void writeLittleEndianChar ( int c ) throws IOException
{	//same as for shorts...

	_b[0]= (byte)c;
	_b[1]= (byte)(c>>8);

	write(_b, 0, 2);
}

/**
Write a little endian char (2-byte Unicode) at the specified file location.
@param i Character to write, as Unicode integer equivalent.
@param offset Byte number to begin writing.
@exception IOException - if the offset passed in is negative or if 
there is an error writing to the file.
*/
public void writeLittleEndianChar(int i, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	//same as for shorts...

	_b[0]= (byte)i;
	_b[1]= (byte)(i>>8);

	write(_b, 0, 2);
}

/**
Write a little endian 8-bit char (not Unicode).
@param c Char to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianChar1(char c) throws IOException
{	_b[0] = (byte)c;
	write(_b, 0, 1);
}

/**
Write a little endian 8-bit char (not Unicode) at the byte offset indicated.
@param c Char to write.
@param offset Byte number to start writing.
@exception IOException if the offset passed in is negative or there is an error
writing to the file.
*/
public void writeLittleEndianChar1(char c, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	_b[0] = (byte)c;
	write(_b, 0, 1);
}

/**
Write a little endian 8-bit byte.
@param b byte to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianByte(byte b) throws IOException
{
	_b[0] = (byte)b;
	write(_b, 0, 1);
}

/**
Write a little endian 8-bit byte at the byte offset indicated.
@param b byte to write.
@param offset byte number to start writing.
@exception IOException if the offset passed in is negative or there is an error
writing to the file.
*/
public void writeLittleEndianByte(byte b, int offset) throws IOException
{	
	if (offset <0 ) 
	{
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	_b[0] = (byte)b;
	write(_b, 0, 1);
}

/**
Write a String as little endian 8-bit chars (not Unicode).
@param s String to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianChar1(String s) throws IOException
{	int size = s.length();

	if ( size > _b.length ) {
		// Reallocate the array...
		_b = new byte[size];
		for (int i = 0; i < size; i++) {
			_b[i] = (byte)s.charAt(i);
		}

		write(_b, 0, size);
	} 
	else {	for ( int i = 0; i < size; i++ ) {
			_b[i] = (byte)s.charAt(i);
		}

		write(_b, 0, size);
	}
}

/**
Write a String as little endian 8-bit chars (not Unicode) at the specified file
location.
@param s String to write.
@param offset Byte number to begin writing.
@exception IOException if offset is negative or if there is an error writing to
the file.
*/
public void writeLittleEndianChar1(String s, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	int size = s.length();

	if ( size > _b.length ) {
		// Reallocate the array...
		_b = new byte[size];
		for (int i = 0; i < size; i++) {
			_b[i] = (byte)s.charAt(i);
		}

		write(_b, 0, size);
	} 
	else {	for ( int i = 0; i < size; i++ ) {
			_b[i] = (byte)s.charAt(i);
		}

		write(_b, 0, size);
	}
}

/**
Write a String as little endian (2-byte Unicode) chars.
@param s String to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianChars(String s) throws IOException
{	int len = s.length();
	for (int i=0;i<len;i++) {
		writeChar(s.charAt(i));
	}
}

/**
Writes a String as little endian (2-byte Unicode) chars at the specified file
location.
@param s String to write.
@param offset Byte number to begin writing.
@exception IOException if offset is negative or if there is an error writing to
the file.
*/
public void writeLittleEndianChars(String s, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

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
public void writeLittleEndianDouble(double d) throws IOException
{ 	//convert to floating point long
	long vl =Double.doubleToLongBits(d);

	_b[0]= (byte)vl;
	_b[1]= (byte)(vl>>8);
	_b[2]= (byte)(vl>>16);
	_b[3]= (byte)(vl>>24);
	_b[4]= (byte)(vl>>32);
	_b[5]= (byte)(vl>>40);
	_b[6]= (byte)(vl>>48);
	_b[7]= (byte)(vl>>56);

	write(_b, 0, 8);
}

/**
Write a little endian 64-bit double at the specified file location.
@param d Double to write.
@param offset Byte number to begin writing.
@exception IOException if offset is negative or if there is an error writing to
the file.
*/
public void writeLittleEndianDouble(double d, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	//convert to floating point long
	long vl =Double.doubleToLongBits(d);

	_b[0]= (byte)vl;
	_b[1]= (byte)(vl>>8);
	_b[2]= (byte)(vl>>16);
	_b[3]= (byte)(vl>>24);
	_b[4]= (byte)(vl>>32);
	_b[5]= (byte)(vl>>40);
	_b[6]= (byte)(vl>>48);
	_b[7]= (byte)(vl>>56);

	write(_b, 0, 8);
}

/**
Write a little endian 32-bit float.
@param f 32-bit float to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianFloat(float f) throws IOException
{	writeLittleEndianInt(Float.floatToIntBits(f));
}

/**
Write a 32-bit little endian float at the specified file location.
@param f 32-bit float to write.
@param offset Byte number to begin writing.
@exception IOException if offset is negative or if there is an error writing to
the file.
*/
public void writeLittleEndianFloat(float f, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	writeLittleEndianInt(Float.floatToIntBits(f));
}

/**
Write a 32-bit little endian int.
@param i 32-bit integer to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianInt(int i) throws IOException
{	_b[0]= (byte)i;
	_b[1]= (byte)(i>>8);
	_b[2]= (byte)(i>>16);
	_b[3]= (byte)(i>>24);

	write(_b, 0, 4);
	 
}

/**
Write a 32-bit little endian int at the specified file location.
@param i 32-bit integer to write.
@param offset Byte location to start writing int.
@exception IOException if the offset is negative or if there is an error writing
to the file.
*/
public void writeLittleEndianInt(int i, int offset ) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	_b[0]= (byte)i;
	_b[1]= (byte)(i>>8);
	_b[2]= (byte)(i>>16);
	_b[3]= (byte)(i>>24);

	write(_b, 0, 4);
	
}

/**
Write a 16-bit little endian short integer.
@param s 16-bit short integer value to write.
@exception IOException if there is an error writing to the file.
*/
public void writeLittleEndianShort(short s) throws IOException
{	_b[0]= (byte)s;
	_b[1]= (byte)(s>>8);

	write(_b, 0, 2);
}

/**
Write a 16-bit little endian short at the specified file location.
@param s 16-bit short inteter value to write.
@param offset Byte number to begin writing.
@exception IOException if offset is negative or if there is an error writing to
the file.
*/
public void writeLittleEndianShort(short s, int offset) throws IOException
{	if (offset <0 ) {
		throw new IOException("Offset value must be greater "+
			"than or equal to zero." );
	}

	seek(offset);

	_b[0]= (byte)s;
	_b[1]= (byte)(s>>8);

	write(_b, 0, 2);
}

} //end EndianRandomAccessFile
