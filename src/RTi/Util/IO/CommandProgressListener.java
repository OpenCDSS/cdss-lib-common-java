// CommandProgressListener - this interface defines behavior for listening to a command while it runs.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
This interface defines behavior for listening to a command while it runs.
This is implemented, for example, when a GUI is displaying the progress of a command in a progress bar.
In practice the display of messages may be more appropriate for tool-tip popup than a status window, due to flashing.
If command processing allows pause,
then the individual messages will be important to indicate the current state of processing.
*/
public interface CommandProgressListener {

/**
Indicate the progress that is occurring within a command.
This may be a chained call from a CommandProcessor that implements CommandListener to listen to a command.
This level of monitoring is useful if more than one progress indicator is present in an application UI.
@param istep The number of steps being executed in a command (0+), for example loop index of objects being processed.
A value of 0 resets the progress bar limits and subsequent calls increment the progress.
@param nstep The total number of steps to process within a command, for example total number of objects being processed.
@param command The reference to the command that is starting to run, provided to allow future interaction with the command.
@param percentComplete If >= 0, the value can be used to indicate progress running a single command (not the single command).
If less than zero, then no estimate is given for the percent complete and the called code can make its own determination
(e.g., ((istep + 1)/nstep)*100).
@param message A short message describing the status (e.g., "Running command ..." ).
*/
public void commandProgress ( int istep, int nstep, Command command, float percentComplete, String message );

}