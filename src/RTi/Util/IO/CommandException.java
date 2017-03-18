//------------------------------------------------------------------------------
// CommandException - an Exception to be thrown when a command cannot be run due
//			to a nonrecoverable problem
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
The CommandException should be thrown when running a Command results
in non-recoverable warnings that will likely result in inaccurate or
no results.  The CommandProcessor can then report that warnings occurred and continue.
*/
@SuppressWarnings("serial")
public class CommandException extends Exception
{

public CommandException ( String message )
{	super ( message );
}

}
