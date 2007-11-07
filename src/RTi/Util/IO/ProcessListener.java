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
as it is geneated. 
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
