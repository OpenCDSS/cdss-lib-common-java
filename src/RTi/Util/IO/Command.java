// Command - this interface is implemented by classes that are commands that can be parsed and run

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.IO;

import javax.swing.JFrame;

/**
This interface is implemented by classes that are commands that can be parsed and run.
@see AbstractCommand
*/
public interface Command extends Cloneable
{

/**
Check the command parameter for valid values, combination, etc.
This is normally called by the command editor dialog.
@param parameters The parameters for the command.
@param commandTag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warningLevel The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String commandTag, int warningLevel )
throws InvalidCommandParameterException;

/**
Clone the command.
This is typically done for edit tasks where the original data may need to be restored on a cancel.
*/
public Object clone ();

/**
Edit the command in a dialog.
@param parent the parent JFrame to which the command editor will be a child.
@return true if the command was edited
(e.g., "OK" was pressed) or false if the command was not edited (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent );

/**
Return the command name.
@return the command name.
*/
public String getCommandName ();

/**
Return the parameters being used by the command.
The Prop.getHowSet() method can be used to determine whether a property was defined in the original command string
(Prop.SET_FROM_PERSISTENT) or is defaulted internally (Prop.SET_AS_RUNTIME_DEFAULT).
TODO SAM 2005-04-29 Does this need a boolean parameter to allow dialogs to see only the parameters in the command,
so that defaults are not explicitly displayed?
@return the parameters being used by the command.  A non-null list is guaranteed.
*/
public PropList getCommandParameters ();

// TODO SAM 2005-05-05 Evaluate whether something like getDefaultParameterValue is needed.
// The problem is that for some low-level code a value of null is the default.
// Should null be provided as a default parameter?

/**
Return the processor that is managing the command.
@return the processor that is managing the command name.
*/
public CommandProcessor getCommandProcessor ();

/**
Return the command profile.
@return the command profile.
*/
public CommandProfile getCommandProfile ( CommandPhaseType phase );

/**
 * Indicate whether the command is a plugin command.
 * @return true if the command is a plugin command, false if not.
 */
public boolean getIsCommandPlugin();

/**
Initialize the command by parsing the command and indicating warnings.  This is essentially validation.
@param command_string A string command to parse.
@param processor The CommandProcessor that is executing the command,
which will provide necessary data inputs and receive output(s).
@param full_initialization If true, the command string will be parsed and checked for errors.
If false, a blank command will be initialized
(e.g., suitable for creating a new command instance before editing in the command editor).
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void initializeCommand (	String command_string, CommandProcessor processor, boolean full_initialization )
throws InvalidCommandSyntaxException, InvalidCommandParameterException;

/**
Parse the command string into a PropList of parameters, which are then stored in the command.
@param commandString A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void parseCommand ( String commandString )
throws InvalidCommandSyntaxException, InvalidCommandParameterException;

/**
Run the command, processing input and producing output.
If non-fatal warnings occur (the command could produce some results),
a CommandWarningException will be thrown after running.
If fatal warnings occur (the command could not produce output), a CommandException is thrown.
@param commandIndex The number of the command 0+.  When used with a processor,
this can be used to cross-reference the command to a log, etc.
Pass -1 if a valid command number cannot be determined.
@exception InvalidCommandParameterException if during parsing of parameters,
one or more parameters are determined to be invalid.
*/
public void runCommand ( int commandIndex )
throws InvalidCommandParameterException, CommandWarningException, CommandException, InterruptedException;

/**
Set a command parameter.  This is used, for example, by a command editor dialog,
and results in command parameter PropList being updated and the command string being regenerated.
@param parameter Name of parameter to set.
@param value Value of parameter to set.  Passing a value of null will effectively unset the parameter
(null will be returned when retrieving the parameter value, requiring handling).
*/
public void setCommandParameter ( String parameter, String value );

/**
Set the command profile information for a phase.
This is used internally to track command resources such as execution time,
heap memory before and after command execution, etc.
*/
public void setCommandProfile ( CommandPhaseType phase, CommandProfile profile );

/**
Set the command string.
This is currently used only by the generic command editor (GenericCommand_JDialog)
and CommandAsText_JDialog for editing a command as text and should only be implemented in the AbstractCommand base class.
@param commandString Command string for the command.
*/
public void setCommandString ( String commandString );

/**
 * Set whether the command is a plugin command.
 * @param isPlugin true if the command is a plugin command, false if not.
 */
public void setIsCommandPlugin( boolean isPlugin );

/**
Return the standard string representation of the command, which can be parsed by parseCommand().
The internal list of properties is used to format the command.
This version is suitable for formatting a representation of a command using "saved" parameters (e.g., for displays).
@return the standard string representation of the command.
*/
public String toString ();

/**
Return the standard string representation of the command, which can be parsed by parseCommand().
The given list of properties is used to format the command.
This version is suitable for formatting a representation of a command for use in a command editor,
where the parameters have not been committed to the Command's memory.
*/
public String toString ( PropList props );

}