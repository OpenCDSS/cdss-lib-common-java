// ----------------------------------------------------------------------------
// EndianDataInputStream - read little or big endian binary data streams
// ----------------------------------------------------------------------------
// History:
//
// 14 Jun 1999	Steven A. Malers,	Initial version.
//		Riverside Technology,
//		inc.
// 2001-11-26	SAM, RTi		Add readChar1() and readString1() to
//					read big-endian 8-bit characters and
//					strings.  These methods are not found
//					in the standard big-endian
//					DataInputStream.
// 2004-05-05	J. Thomas Sapienza, RTi	Added support for reading both
//					big endian and little endian values 
//					from the stream.
// 2004-5-07	Scott Townsend, RTi	Added a readEndianChar1 method.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import RTi.Util.IO.IOUtil;

/**
This class extends the basic DataInputStream class by providing LittleEndian
versions of some read methods.  This allows one input stream to be used to
read big and little endian values.  By default, Java assumes big endian
data.  Use the normal DataInputStream methods for reading unless you
specifically know that values are little endian, in which case the methods of
this derived class should be used.
@see java.io.DataInputStream
*/
public class EndianDataInputStream 
extends DataInputStream {

/**
Boolean to specify whether the stream is big endian or not.  Java default is
that streams ARE big endian.
*/
private boolean __is_big_endian = true;

/**
Specifies whether to match the current system's endianness.
*/
private boolean __match_system = false;

/**
Byte array used for reading from the stream.
*/
private byte __byte8[] = null;

/**
Construct using an InputStream.
@param istream the InputStream from which to construct this Endian input stream.
*/
public EndianDataInputStream(InputStream istream) {
	super (istream);
	__byte8 = new byte[8];
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Construct using an InputStream.
@param istream the InputStream from which to construct this Endian input stream.
@param matchSystem whether to match the system's current endianness when readin
from the stream.
*/
public EndianDataInputStream(InputStream istream, boolean matchSystem) {
	super (istream);
	__byte8 = new byte[8];
	__match_system = matchSystem;
	__is_big_endian = IOUtil.isBigEndianMachine();
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable {
	__byte8 = null;
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
Similar to DataInputStream.readChar except read a one-byte big-endian character.
@return a character read from a one-byte big endian character.
@exception IOException if there is a read error.
*/
public final char readChar1() throws IOException
{	if ( read(__byte8, 0, 1) != 1 ) {
		throw new IOException();
	}
	return (char) ( __byte8[0] );
}

/**
Read a 64-bit double from the file using an endian-ness that matches the system
(match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the 64-bit double read from the file.
*/
public double readEndianDouble () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
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
public float readEndianFloat () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
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
public int readEndianInt () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
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
public long readEndianLong () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
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
public short readEndianShort () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianShort();
	}
	else {	// Default Java or system that is big-endian...
		return readShort();
	}
}

/**
Similar to DataInputStream.readChar except read a endian 1 byte (8-bit)
from the file using an endian-ness that matches the
system (match_system=true should be used in the constructor if this method is
called - otherwise Java big-endian is assumed).
@return the Character read from the file.
*/
public char readEndianChar1 () 
throws IOException {	
	if ( __match_system && !__is_big_endian ) {
		// Little endian system...
		return readLittleEndianChar1();
	}
	else {	// Default Java or system that is big-endian...
		return readChar1();
	}
}

/**
Similar to DataInputStream.readShort except read a little endian number.
@return a short read from a little endian number.
@exception IOException if there is a read error.
*/
public final short readLittleEndianShort()
throws IOException
{
	if ( read(__byte8, 0, 2) != 2 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return (short)( (__byte8[1]&0xff) << 8 | (__byte8[0]&0xff));
}

/**
Similar to DataInputStream.readUnsignedShort except read a little endian number.
@return Integer value of the short.
@exception IOException if there is a read error.
*/
public final int readLittleEndianUnsignedShort() throws IOException
{	if ( read(__byte8, 0, 2) != 2 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return ( (__byte8[1]&0xff) << 8 | (__byte8[0]&0xff));
}

/**
Similar to DataInputStream.readChar except read a little endian character.
This is a 16-bit Unicode character.  Use readLittleEndianChar1 to read a one
byte character.
@return a character read from a little endian character.
@exception IOException if there is a read error.
*/
public final char readLittleEndianChar() throws IOException
{	if ( read(__byte8, 0, 2) != 2 ) {
		throw new IOException();
	}
	return (char) ( (__byte8[1]&0xff) << 8 | (__byte8[0]&0xff));
}

/**
Similar to DataInputStream.readChar except read a little endian 1 byte (8-bit)
character.  The 8-bit character is converted to a 16-bit character, which is
returned.
@return a character read from a little endian character.
@exception IOException if there is a read error.
*/
public final char readLittleEndianChar1() throws IOException
{	if ( read(__byte8, 0, 1) != 1 ) {
		throw new IOException();
	}
	// Treat as a byte...
	return (char)(__byte8[0]&0xff);
}

/**
Similar to DataInputStream.readByte except read a little endian 1 byte (8-bit).
@return a byte read from a little endian byte.
@exception IOException if there is a read error.
*/
public final byte readLittleEndianByte() throws IOException
{	
	if ( read(__byte8, 0, 1) != 1 ) {
		throw new IOException();
	}
	// Treat as a byte...
	return (byte)(__byte8[0]&0xff);
}

/**
Simiar to DataInputStream.readInt except read a little endian number.
@return an integer read from a little endian number.
@exception IOException if there is a read error.
*/
public final int readLittleEndianInt() throws IOException
{	if ( read(__byte8, 0, 4) != 4 ) {
		throw new IOException ( "Unexpected end of file." );
	}
	return	(__byte8[3]&0xff) << 24 |
		(__byte8[2]&0xff) << 16 |
		(__byte8[1]&0xff) <<  8 |
            	(__byte8[0]&0xff);
}
    
/**
Similar to DataInputStream.readInt except read a little endian number.
@return an integer read from a little endian number.
@param bytes number of bytes to read for the integer.
@exception IOException if there is a read error.
*/
public final int readLittleEndianInt(int bytes) throws IOException
{	if ( read(__byte8, 0, bytes) != bytes ) {
		throw new IOException( "Premature end of file." );
	}
	int ret = 0;
	for ( int i = 0; i < bytes; i++ ) {
		ret = ret | (__byte8[i]<<(i*8));
	}
	return ret;
}
    
/**
Similar to DataInputStream.readLong except read a little endian number.
@return a long integer read from a little endian number.
@exception IOException if there is a read error.
*/
public final long readLittleEndianLong() throws IOException
{	if ( read(__byte8, 0, 8) != 8 ) {
		throw new IOException( "Premature end of file." );
	}
	return	(long)(__byte8[7]&0xff) << 56 |
		(long)(__byte8[6]&0xff) << 48 |
		(long)(__byte8[5]&0xff) << 40 |
		(long)(__byte8[4]&0xff) << 32 |
		(long)(__byte8[3]&0xff) << 24 |
		(long)(__byte8[2]&0xff) << 16 |
		(long)(__byte8[1]&0xff) <<  8 |
		(long)(__byte8[0]&0xff);
}

/**
Similar to DataInputStream.readFloat except read a little endian number.
@return a float read from a little endian number.
@exception IOException if there is a read error.
*/
public final float readLittleEndianFloat() throws IOException
{	return Float.intBitsToFloat(readLittleEndianInt());
}

/**
Similar to DataInputStream.readDouble except read a little endian number.
@return a double read from a little endian number.
@exception IOException if there is a read error.
*/
public final double readLittleEndianDouble() throws IOException
{	return Double.longBitsToDouble(readLittleEndianLong());
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
		if ( read(__byte8, 0, 1) != 1 ) {
			throw new IOException();
		}
		c[i] = (char) ( __byte8[0] );
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

} // end EndianDataInputStream class
