//------------------------------------------------------------------------------
// CommandWarningException - an Exception to be thrown when a command
//				has generated one or more non-fatal warnings	
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
The CommandWarningException should be thrown when running a Command results
in non-fatal warnings.  The CommandProcessor can then report that warnings
occurred and continue.
*/
@SuppressWarnings("serial")
public class CommandWarningException extends Exception
{

public CommandWarningException ( String message )
{	super ( message );
}

}