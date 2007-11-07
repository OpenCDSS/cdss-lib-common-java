//------------------------------------------------------------------------------
// InvalidCommandParameterException - an Exception to be thrown when a command
//					parameter is invalid
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
The InvalidCommandParameterException should be thrown when a Command parameter
is invalid (e.g., during checks in an editor dialog or during initialization
for processing).
*/
public class InvalidCommandParameterException extends Exception
{

public InvalidCommandParameterException ( String message )
{	super ( message );
}

}
