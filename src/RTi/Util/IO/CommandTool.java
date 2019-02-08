// CommandTool - an interface to define an interactive tool that can run a command

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
// CommandTool - an interface to define an interactive tool that can run a
//		command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-11-23	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import javax.swing.JFrame;

/**
This interface should be implemented by Command classes that offer interactive
running of the command (e.g., a Tools menu option).  For example, a command may
be implemented to process a file.  The tool allows the user to interactively run
the command.  The GUI for the command and the tool are essentially the same,
except that the OK button for the command editor results in a text form of the
command, whereas the OK button for the tool will run the command.  To run the
command, the Tool should rely on the normal command methods like
checkCommandParameters().
*/
public interface CommandTool
{

/**
Edit and execute the command in a dialog.
@param parent the parent JFrame to which the command editor will be a child.
@return true if the command was run (e.g., "OK" was pressed) or false if the
command was not edited (e.g., "Cancel" was pressed).
*/
public boolean editRunnableCommand ( JFrame parent );

}
