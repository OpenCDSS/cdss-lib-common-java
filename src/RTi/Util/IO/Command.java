//------------------------------------------------------------------------------
// Command - an interface to define a command that can be parsed and run
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-04-29	Steven A. Malers, RTi	Initial version.
// 2005-05-04	SAM, RTi		Change "message_tag" to "command_tag".
// 2005-05-09	SAM, RTi		* Add toString(PropList).
//					* Add setCommandParameter ().
// 2005-05-10	SAM, RTi		* Change "command" to "command_string"
//					  in parameters, where appropriate, to
//					  clarify the difference between the
//					  command name and command string.
//					* Remove setCommandString() - it was not
//					  needed in SkeletonCommand.
// 2005-05-11	SAM, RTi		* Re-add setCommandString() - it is in
//					  fact used by the
//					  GenericCommand_JDialog when saving
//					  the command.
// 2005-05-19	SAM, RTi		* Move from TSTool to this package.
// 2005-07-11	SAM, RTi		* Add to Javadoc to support new
//					  developers.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import javax.swing.JFrame;

/**
This interface is implemented by classes that are commands that can be parsed
and run.
*/
public interface Command extends Cloneable
{

/**
Check the command parameter for valid values, combination, etc.
This is normally called by the command editor dialog.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters (	PropList parameters, String command_tag,
					int warning_level )
throws InvalidCommandParameterException;

/**
Clone the command.  This is typically done for edit tasks where the original data
may need to be restored on a cancel.
*/
public Object clone ();

/**
Edit the command in a dialog.
@param parent the parent JFrame to which the command editor will be a child.
@return true if the command was edited (e.g., "OK" was pressed) or false if the
command was not edited (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent );

/**
Return the command name.
@return the command name.
*/
public String getCommandName ();

/**
Return the processor that is managing the command.
@return the processor that is managing the commandcommand name.
*/
public CommandProcessor getCommandProcessor ();

/**
Return the parameters being used by the command.  The Prop.getHowSet() method
can be used to determine whether a property was defined in the original command
string (Prop.SET_FROM_PERSISTENT) or is defaulted internally
(Prop.SET_AS_RUNTIME_DEFAULT).
REVISIT SAM 2005-04-29 Does this need a boolean parameter to allow dialogs to
see only the parameters in the command, so that defaults are not explicitly
displayed?
@return the parameters being used by the command.  A non-null list is
guaranteed.
*/
public PropList getCommandParameters ();

// REVISIT SAM 2005-05-05 Evaluate whether something like
// getDefaultParameterValue is needed.  The problem is that for some low-level
// code a value of null is the default.  Should null be provided as a default
// parameter?

/**
Initialize the command by parsing the command and indicating warnings.
This is essentially validation.
@param command_string A string command to parse.
@param processor The CommandProcessor that is executing the command, which will
provide necessary data inputs and receive output(s).
@param full_initialization If true, the command string will be parsed and
checked for errors.  If false, a blank command will be initialized (e.g.,
suitable for creating a new command instance before editing in the command
editor).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void initializeCommand (	String command_string,
				CommandProcessor processor,
				boolean full_initialization )
throws InvalidCommandSyntaxException, InvalidCommandParameterException;

/**
Parse the command string into a PropList of parameters, which are then stored
in the command.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException;

/**
Run the command, processing input and producing output.
If non-fatal warnings occur (the command could produce some results), a
CommandWarningException will be thrown after running.  If fatal warnings occur
(the command could not produce output), a CommandException is thrown.
@param command_index The number of the command 0+.  When used with a processor,
this can be used to cross-reference the command to a log, etc.  Pass -1 if a
valid command number cannot be determined.
@exception InvalidCommandParameterException if during parsing of parameters,
one or more parameters are determined to be invalid.
*/
public void runCommand ( int command_index )
throws InvalidCommandParameterException,
CommandWarningException, CommandException;

/**
Set a command parameter.  This is used, for example, by a command editor dialog,
and results in command parameter PropList being updated and the command
string being regenerated.
@param parameter Name of parameter to set.
@param value Value of parameter to set.  Passing a value of null will
effectively unset the parameter (null will be returned when retrieving the
parameter value, requiring handling).
*/
public void setCommandParameter ( String parameter, String value );

/**
Set the command string.  This is currently used only by the generic command
editor (GenericCommand_JDialog) and should only be implemented in the
SkeletonCommand base class.
@param command_string Command string for the command.
*/
public void setCommandString ( String command_string );

/**
Return the standard string representation of the command, which can be parsed
by parseCommand().  The internal list of properties is used to format the
command.  This version is suitable for formatting a representation of a command
using "saved" parameters (e.g., for displays).
@return the standard string representation of the command.
*/
public String toString ();

/**
Return the standard string representation of the command, which can be parsed
by parseCommand().  The given list of properties is used to format the
command.  This version is suitable for formatting a representation of a command
for use in a command editor, where the parameters have not been committed to
the Command's memory.
*/
public String toString ( PropList props );

}
