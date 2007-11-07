//------------------------------------------------------------------------------
// InvalidCommandSyntaxException - an Exception to be thrown when command
//					syntax is invalid
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
The InvalidCommandSyntaxException should be thrown when a Command's syntax
is invalid (e.g., mismatched parentheses or quotes).
*/
public class InvalidCommandSyntaxException extends Exception
{

public InvalidCommandSyntaxException ( String message )
{	super ( message );
}

}
