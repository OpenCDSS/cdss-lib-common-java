// GenericCommand - a generic command, to use for basic data handling when a specific command class has not been implemented

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

//------------------------------------------------------------------------------
// GenericCommand - a generic command, to use for basic data handling when
//			a specific command class has not been implemented
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-05-09	Steven A. Malers, RTi	Initial version.
// 2005-05-19	SAM, RTi		Move from TSTool package.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import RTi.Util.Message.Message;

import javax.swing.JFrame;	// For the editor

/**
This class is a generic command, for example to use when editing a command
that does not have a command editor.
In this case, the GenericCommand_JDialog will be used to edit the command.
All known commands should have an editor.
*/
public class GenericCommand extends AbstractCommand
{

/**
Default constructor for a command.
*/
public GenericCommand ()
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
@return the Command instance that is created and edited, or null if the
edit was cancelled.
@param parent Parent JFrame on which the model command editor dialog will be
shown.
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
suitable for creating a new command instance before editing in the command
editor).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
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
Parse the command string into a PropList of parameters.  Does nothing in this
base class.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	// Does nothing.
}

/**
Run the command.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	// Throw an exception because if something tries to run with this it needs
    // to be made known that nothing is happening.
    Message.printStatus ( 2, "GenericCommand.runCommand", "In runCommand().");
    if ( getCommandName().isEmpty() ) {
    	throw new CommandException ( "Unknown command (command name not set) runCommand() method is not enabled.");
    }
    else {
    	throw new CommandException ( getCommandName() + " runCommand() method is not enabled.");
    }
}

// TODO SAM 2005-05-31 If the editor is ever implemented with a tabular
// display for parameters, will need to deal with the parsing.
// For now, this will at least allow unrecognized (and therefore unable to parse) commands to be edited.
/**
Return the string representation of the command.
This always returns the command string.
*/
public String toString()
{	return getCommandString();
}

}