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
