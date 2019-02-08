// CommandListUI - interface that provides methods to allow code to interact with a UI for a command list

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

//import java.util.List;

/**
This interface provides methods to allow code to interact with a UI for a command list, beyond the
basic actions of the command list model (whatever is implemented).  For example, a method is included
to request the insert position for a command.  This may be indicated by selections in a UI, whereas the
basic list model does not know about UI state.  It is expected that this interface will remain fairly simple
but may grow over time.
*/
public interface CommandListUI {

/**
Insert a command in the command list, depending on the state of the UI (e.g., insert before highlighted
commands).
@param command single command to insert
*/
public void insertCommand ( Command command );

/**
Insert multiple commands in the command list, depending on the state of the UI (e.g., insert before highlighted
commands).
@param commands list of commands to insert
*/
// TODO SAM 2009-06-12 Enable when needed.
// public void insertCommands ( List<Command> commands );
	
}
