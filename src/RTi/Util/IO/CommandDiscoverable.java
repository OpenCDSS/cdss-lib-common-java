// CommandDiscoverable - interface to indicate that a command can be run in discovery mode

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
