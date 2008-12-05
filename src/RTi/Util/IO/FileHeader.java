// ----------------------------------------------------------------------------
// FileHeader - use with IO.getFileHeader
// ----------------------------------------------------------------------------
// History:
//
// Jun 1997	Steven A. Malers, RTi	Port from C.
// 14 Mar 1998	SAM, RTi		Add javadoc.
// 2005-04-26	J. Thomas Sapienza, RTi	Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

/**
This class is used by the IO.getFileHeader method when processing input/output
file headers.  The file header consists of a vector of comment strings and
integers indicating the first and last revisions in the header.
*/
public class FileHeader {

private List	_header;
private int	_header_first;
private int	_header_last;

/**
Default constructor.
*/
public FileHeader ()
{
	_header = new Vector (10,5);
	_header_first = 0;
	_header_last = 0;
}

/**
Add a string to the header.
@param o String to add to header.
*/
public int addElement ( Object o )
{
	_header.add ( o );
	return 0;
}

/**
Return the string at index "i" (zero-referenced).
@return The string at index "i".
*/
public Object elementAt ( int i )
{
	return _header.get ( i );
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	_header = null;
	super.finalize();
}

/**
@return The first header revision (the smallest number indicating the oldest
revision).
*/
public int getHeaderFirst ( )
{
	return _header_first;
}

/**
@return The last header revision (the largest number indicating the most recent
revision).
*/
public int getHeaderLast ( )
{
	return _header_last;
}

/**
Set the first header revision number.
@param header_first The first header revision number.
*/
public int setHeaderFirst ( int header_first )
{
	_header_first = header_first;
	return 0;
}

/**
Set the last header revision number.
@param header_last The last header revision number.
*/
public int setHeaderLast ( int header_last )
{
	_header_last = header_last;
	return 0;
}

/**
@return The number of strings in the header.
*/
public int size ( )
{
	return _header.size();
}

}
