//------------------------------------------------------------------------------
// UnknownCommandException - an Exception to be thrown when a command name is
//				not recognized
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-04-29	Steven A. Malers, RTi	Initial version.
// 2005-05-19	SAM, RTi		Move from TSTool package.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

/**
The UnknownCommandException should be thrown processing commands and an
unknown command is encountered.
*/
@SuppressWarnings("serial")
public class UnknownCommandException extends Exception
{

public UnknownCommandException ( String message )
{	super ( message );
}

}