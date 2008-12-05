package RTi.Util.IO;

/**
Implementation of this interface indicates that a command can be run in discovery
mode, in which case a partial read of data will occur during the initializeCommand() call.
*/
public interface CommandDiscoverable
{

/**
Run the command in discovery mode.  A partial read of data will occur.  This is useful when
editing commands (after committing the edit).  Because the data for a command may not be fully
available at edit time (e.g., input file may not exist), a discovery run may produce less
(or no) output than a full run.
The ObjectListProvider can then be implemented to retrieve a list of data objects from the read.
The parameters are the same as the Command.runCommand() method.
@param command_index The number of the command 0+.  When used with a processor,
this can be used to cross-reference the command to a log, etc.  Pass -1 if a
valid command number cannot be determined.
*/
public void runCommandDiscovery ( int command_index )
throws InvalidCommandParameterException,CommandWarningException, CommandException;

}