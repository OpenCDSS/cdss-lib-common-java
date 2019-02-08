// RegExpFilenameFilter - general purpose filename filter that supports regular expressions

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

// ----------------------------------------------------------------------------
// RegExpFilenameFilter - general purpose filename filter that supports
//			regular expressions
// ----------------------------------------------------------------------------
// Copyright RTi:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 23 Jun 1999	Steven A. Malers,	Initial version.  Finally got tired of
//		Riverside Technology,	lake of API.
//		inc.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.File;
import java.io.FilenameFilter;

import RTi.Util.String.StringUtil;

/**
This class implements FilenameFilter for use with FileDialog.
It supports UNIX-style wildcards in filenames, as follows:
<p>
<pre>
.     Match one character.
*     Match zero or more characters.
[...] Match any one of the characters enclosed in the brackets.  This can be
      a list of characters or a character range separated by a -.
</pre>
<p>
To use with FileDialog, contruct this filter with an appropriate filename
regular expression (usually *.ext, where ext is a desired file extension).
Then specify the filter to the FileDialog.
*/
public class RegExpFilenameFilter
implements FilenameFilter
{

private String _regexp = "";

/**
Basic constructor.  The main functionality is in the accept() method.
@param regexp a regular expression.
*/
public RegExpFilenameFilter ( String regexp )
{	_regexp = regexp;
}

/**
@return true if the name matches the regular expression passed in during construction.
@param dir Directory of file being evaluated.
@param name File being evaluated (without leading directory path).
*/
public boolean accept ( File dir, String name )
{	return StringUtil.matchesRegExp ( name, _regexp );
}

/**
Finalize before garbage collection.
*/
protected void finalize()
throws Throwable
{	_regexp = null;
	super.finalize();
}

} // End RegExpFilenameFilter class
