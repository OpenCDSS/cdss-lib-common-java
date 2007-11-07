//------------------------------------------------------------------------------
// RTi.Util.Message.MessageUtil - Utility code for the Message package
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2005-04-29	Steven A. Malers, RTi	Initialize code with the
//					formatMessageTag() method.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.Message;

/**
This class provides useful static methods to support the Message package.
*/
public abstract class MessageUtil
{

/**
Format a standard message tag string, for use with command-oriented programs.
The format of the string will be:
<pre>
root,count
</pre>
@param tag_root A root string to include in the tag.
@param count A count to modify the root (1+), for example, indicating the
count of warnings within a command.
@return a formatted message tag, or an empty string if root is null.
*/
public final static String formatMessageTag ( String root, int count )
{	if ( (root == null) || (root.length() == 0) ) {
		return "";
	}
	else {	return root + "," + count;
	}
}

}
