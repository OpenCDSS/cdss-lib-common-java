// UnknownCommand - this class is essentially the same as GenericCommand

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;

import javax.swing.JFrame;	// For the editor

/**
This class is essentially the same as GenericCommand.  However, whereas GenericCommand might
be used for a known command, this UnknownCommand class can be used in cases where a command is
not known but needs to be managed.  The GenericCommand_JDialog is used to edit the command.
*/
public class UnknownCommand extends AbstractCommand
{

/**
Default constructor for a command.
*/
public UnknownCommand ()
{
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{
}

/**
Edit a command instance.  The instance may be a newly created command or one
that has been created previously and is now being re-edited.
@return the Command instance that is created and edited, or null if the edit was cancelled.
@param parent Parent JFrame on which the model command editor dialog will be shown.
*/
public boolean editCommand ( JFrame parent )
{	// Use the generic command editor...	
	return (new GenericCommand_JDialog ( parent, this )).ok();
}

/**
Initialize the command by parsing the command and indicating warnings.
@param command A string command to parse.
@param full_initialization If true, the command string will be parsed and
checked for errors.  If false, a blank command will be initialized (e.g.,
suitable for creating a new command instance before editing in the command editor).
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void initializeCommand ( String command, CommandProcessor processor, boolean full_initialization )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	// Save the processor...
	super.initializeCommand ( command, processor, full_initialization );
	if ( full_initialization ) {
		// Parse the command...
		parseCommand ( command );
	}
}

/**
Parse the command string into a PropList of parameters.  Does nothing in this base class.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	// Does nothing.
}

/**
Run the command.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "UnknownCommand.runCommand";
    int warning_count = 0;
    int warning_level = 2;
    String command_tag = "" + command_number;

    if ( getCommandString().trim().equals("") ) {
        // Empty line so don't do anything
    }
    else {
        Message.printStatus ( 2, "UnknownCommand.runCommand", "In runCommand().");
        CommandStatus status = getCommandStatus();
        status.clearLog(CommandPhaseType.RUN);
        
        String message = "Don't know how to run unknown command: " + toString();
        Message.printWarning(warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify command spelling and if necessary report the problem to software support." ) );
        
        // Throw an exception because if something tries to run with this it needs
        // to be made known that nothing is happening.
        
        throw new CommandException ( getCommandName() + " run() method is not enabled.");
    }
}

// TODO SAM 2005-05-31 If the editor is ever implemented with a tabular
// display for parameters, will need to deal with the parsing.  For now, this
// will at least allow unrecognized commands to be edited using the string representation.
/**
Return the string representation of the command.
This always returns the command string.
*/
public String toString()
{	return getCommandString();
}

}
