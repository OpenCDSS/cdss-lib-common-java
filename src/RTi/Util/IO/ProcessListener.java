// ProcessListener - listener for process events

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

// ----------------------------------------------------------------------------
// ProcessListener - listener for process events
// ----------------------------------------------------------------------------
// History:
//
// 2002-10-16	Steven A. Malers, RTi	Implemented code.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

/**
This ProcessListener interface can be implemented by classes that need to
listen for output from an external process, as controlled by the ProcessManager.
This interface is used by the ProcessManagerJDialog to retrieve process output
as it is generated. 
*/
public abstract interface ProcessListener
{
/**
ProcessManager will call this method if a line from standard output is read.
@param output A line from the process' standard output.
*/
public abstract void processOutput ( String output );

/**
ProcessManager will call this method if a line from standard error is read.
@param error A line from the process' standard error.
*/
public abstract void processError ( String error );

/**
ProcessManager will call this method when the status of the process changes.
@param code If zero, then a normal exit has occurred.  If not zero, assume that
the process has terminated with an error.  At some point, need to consider how
to handle pause, interrupt, etc.
@param message A string message that can be displayed so that calling code
does not need to interpret the numeric code.
*/
public abstract void processStatus ( int code, String message );

}
