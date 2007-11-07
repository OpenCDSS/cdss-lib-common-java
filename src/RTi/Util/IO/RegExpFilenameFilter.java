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
@return true if the name matches the regular expression passed in during
construction.
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
