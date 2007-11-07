//------------------------------------------------------------------------------
// CommandFactory - an interface to define a Command factory
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-04-29	Steven A. Malers, RTi	Initial version.
// 2005-05-04	SAM, RTi		Change so that the newCommand() method
//					takes a command string, not a commnad
//					name.
// 2005-05-19	SAM, RTi		Move from TSTool package.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

/**
This interface is implemented by classes that will be a factory for Command
instances.
*/
public interface CommandFactory
{

/**
Create a new Command instance based on the command string.
@param command_string The full command string.
*/
public Command newCommand ( String command_string )
throws UnknownCommandException;

}
