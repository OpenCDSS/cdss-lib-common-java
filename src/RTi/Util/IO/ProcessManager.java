//------------------------------------------------------------------------------
// ProcessManager - class which controls the execution of a process and the
//		retrieving of output from that process.  
//
// The following code is known to use a variant of ProcessManager:
//
// RTi.Util.IO.PropListManager
// RTi.Util.IO.ProcessManagerDialog
// RTi.Util.IO.ProcessManagerJDialog
// RTi.Util.Help.URLHelp
// RTi.Util.Message.DiagnosticsGUI
// RTi.Util.Message.DiagnosticsJFrame
// RTi.Util.Message.MessageDialog
// Applications/components that interact with the system registry via shellcon:
//	DWR.SMGUI.SMgraphWindow	<- also uses ProcessManagerDialog
//	DWR.SMGUI.SMGUIApp	<- also uses ProcessManagerDialog
//	DWR.DMI.tstool.TSEngine
//	DWR.DMI.tstool.TSToolMainGUI
//	DWR.DMI.tstool.TSToolPanel
//	DWR.DMI.HBGUI.HBLoginGUI
//	DWR.DMI.HydroBaseDMI.SelectHydroBaseJDialog
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 14 Oct 1997	Catherine E.	Created initial version of class
//		Nutting-Lane,
//		RTi
// 17 Oct 1997	CEN, RTi	Changed name from SpawnProcess to 
//				ProcessManager.  Added several new member
//				functions.
// 31 Mar 1998	CEN, RTi	Added javadoc comments.
// 10 Nov 1998	CEN, RTi	Added more javadoc and added "try" to each
//				statement within the "run" routine.
// 30 Nov 1998	CEN, RTi	Added routine to retrieve _proc itself.
// 14 Apr 1999 	CEN, RTi	Added setProcessFinished and replace some code
//				(only commented out old code and noted in the 
//				code) to compensate for bugs in Java 1.2
// 08 Sep 1999	CEN, RTi	Added check for "Unsuccessful termination"
// 07 Jan 2001	SAM, RTi	Update to Use GUIUtil instead of GUI.  Swap
//				import java.io.* with specific import
//				statements.
// 04 May 2001	SAM, RTi	Make the code that checks the exit code a little
//				more robust.
// 11 May 2001	SAM, RTi	Synchronize the UNIX code to work on both
//				platforms.  Overload constructor to take
//				timeout and handle timeout.  Add
//				runUntilFinished() method.  Optimize memory by
//				adding finalize() and setting unused data to
//				null.  Change so an error in any step produced
//				a non-zero exit status.  Change so that if there
//				is an error starting the process _done is set
//				to true.  Update batch files to not print
//				messages before the first line of output from
//				the command being run since the output from the
//				command may be used by the calling code.
// 07 Aug 2001	SAM, RTi	Update to include "LF90.EER" check for error
//				code.
// 16 Aug 2001	SAM, RTi	Add ability for the calling code to get the
//				temporary file on NT so that it can be
//				concatenated to the output.
// 23 Aug 2001	SAM, RTi	Clean up the NT batch files some to do a better
//				job of passing the error code back to the
//				calling program.  Also add more comments for
//				someone reading the file and make sure output
//				files exist before deleting.  Overload
//				runUntilFinished() to simplify calling code.
//				Change _proc to _process to make things a little
//				clearer.  Add the ActionListener to allow
//				implementation of a timeout.  Add cleanup() -
//				probably need to go through all the code and
//				figure out the cleanest way to do all the
//				cleanup.
// 2001-11-08	SAM, RTi	Synchronize with UNIX changes.
// 2002-02-20	SAM, RTi	Change so temporary batch files for NT get
//				written to C:\temp.  This avoids problems when
//				the software is run in a server directory.  It
//				also cleans up the system some.
// 2002-05-13	SAM, RTi	Change so temporary batch files for NT get
//				written to C:\WINNT\temp.  Since NT is the only
//				operating system that needs the temporary files,
//				we should be able to assume that the directory
//				exists.
// 2002-07-03	SAM, RTi	Fix so that the temporary file that is used
//				has a name from the program without the leading
//				path.  The leading path was being used in the
//				batch file names.  Don't delete the temporary
//				output file if on NT - there seem to be some
//				timing issues with sending the output to the
//				application.
// 2002-07-15	SAM, RTi	Fix bug where exit status was set after calling
//				setProcessFinished().  In threaded situations
//				sometimes the status value was not set before a
//				call to isOutputFinished() returned true.
// 2002-08-07	SAM, RTi	The NT batch files are really causing problems
//				with hanging processes.  Add a useBatchFiles()
//				method to let calling code disable the batch
//				files and just use cmd /c xxx to see if that can
//				work.
// 2002-09-23	SAM, RTi	Add "exit" at the end of the batch files for NT
//				to see if this closes out the CMD processes that
//				seem to hang around when using process manager
//				on NT.
// 2002-10-15	SAM, RTi	Update to use non-buggy Java classes - the new
//				version seems to work ok for Java 1.1.8 and
//				1.4.0.
// 2002-10-18	SAM, RTi	Overload the constructor to take an array for
//				the command line arguments - this seems to solve
//				some problems with more complicate arguments
//				containing quotes and special characters.
//				Add a getCommand() method to return the String
//				version of the command.
// 2002-10-21	SAM, RTi	Change so that setting a null command
//				interpreter is handled properly (no extra spaces
//				or exceptions).
// 2003-06-27	SAM, RTi	Add the "ExitStatusTokens" property to allow
//				checking for a keyword followed by an exit
//				status.  This seems to be necessary, for
//				example, when running StateMod via cmd.com.
//				Perhaps cmd.com does not pass back the exit
//				status from the called program.
// 2003-12-03	SAM, RTi	Use the StreamConsumer to consume the standard
//				error to prevent processes with a lot of error
//				output from hanging the process - later need to
//				provide a way to pass back the output to this
//				class, perhaps using the ProcessListener
//				interface.
// 2004-09-09	J. Thomas Sapienza, RTi	Added the ability to set the status 
//				level at which information prints out during
//				a process run by calling setRunStatusLevel().
// 2005-04-26	JTS, RTi	Added all data members to finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Exception;
import java.lang.StringBuffer;
import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.EventTimer;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

/**
This ProcessManager class controls the execution of a process and the retrieval
of output from that process.  The class primarily provides an interface to the
Runtime Java interface, using Process.exec() to run the command.  By default,
the ProcessManager is not a separate thread.  Therefore,
using ProcessManager will pause the calling program until the external process
is complete.  Use ProcessManagerDialog or ProcessManagerJDialog if a threaded
process with user feedback is desired or create a thread using an instance of
ProcessManager (see below for example).
<p>
The basic information associated with the process includes:
<ol>
<li>	Input to the process.  This can be specified as command line arguments
	as part of the command.  There is currently no way to provide input to
	the command line on its standard input, e.g., to provide newline
	terminated lines of input (this feature can be added if needed).
	</li>
<li>	Output from the process.  Output is composed of standard error and
	standard output.  This output, if viewed in a command shell, is
	typically intermixed and difficult to deal with separately.  Currently
	in ProcessManager, only the standard output is available and can be
	echoed back to the calling code.  For a non-threaded run, output from
	the process is typically retrieved by calling saveOutput(true) before
	running the process and then getOutputVector() after the run.  For a
	threaded process, use the ProcessListener.  See examples below.
	</li>
<li>	Exit status from the process.  The exit status is retrieved when the
	process completes.  For a non-threaded run, use the getExitStatus()
	method after the run is complete.  For a threaded run, use a
	ProcessListener and handle the call to the processStatus() method.
	</li>
</ol>

Typically, a command will be run in one of the ways illustrated below:

<ol>
<li>	Run a command completely (e.g., a fast execution), and retrieve the
	output all at once, without the option to kill the process prematurely.
	In this case the code that 
	<p>

	<pre>
	// Use any of the available constructors...
	ProcessManager pm = new ProcessManager ( "Some command" );
	// Returns all the output...
	pm.saveOutput ( true );
	pm.run();
	output = pm.getOutputVector ();
	int status = pm.getExitStatus();
	// Then display output, status, etc.
	</pre> 
	<p>

	In this case the ProcessManager is not run as a new thread.  All of the
	output from the command is added to a Vector.  The process cannot be
	terminated until it is complete.
	</li>

<li>	Run a command completely (e.g., a fast execution) as a thread, ignoring output.
	<p>

	<pre>
	// Use any of the available constructors...
	ProcessManager pm = new ProcessManager ( "Some command" );
	Thread thread = new Thread ( pm );
	pm.start ();	// This executes the run() method in ProcessManager.
	// Do not set pm to null.  This apparently causes a problem with the
	// thread.
	</pre> 
	<p>

	In this case the ProcessManager is run as a new thread.  All of the
	output from the command is added to a Vector.
	</li>

<li>	Run a command completely (e.g., a fast execution), and retrieve the
	output all at once, WITH the option to kill the process prematurely.
	This can only be done using a thread.
	<p>

	<pre>
	// Use any of the available constructors...
	ProcessManager pm = new ProcessManager ( "Some command" );
	pm.saveOutput ( true );
	Thread thread = new Thread ( pm );
	pm.start ();	// This executes the run() method in ProcessManager.
	// Returns all the output...
	Vector output = pm.getOutputVector();
	// Because a thread is running, the following can be done to cancel
	// the process.  If not already finished, this will set the exit
	// status to 999 and gracefully terminate the process by exiting out
	// of the ProcessManager run() method...
	pm.cancel();
	// Can check the exit status...
	int status = pm.getExitStatus();
	// Then display output, status, etc.
	</pre> 
	<p>

	In this case the ProcessManager is run as a new thread.  All of the
	output from the command is added to a list.
	</li>

<li>	Run a command using a thread and retrieve its output as the command
	runs.  This is the approach taken by the ProcessManagerDialog and
	ProcessManagerJDialog and is suitable for longer execution times.
	<p>

	<pre>
	// Create a process manager using an available constructor...
	ProcessManager pm = new ProcessManager ( "some command" );
	// Provide a listener to receive process output...
	pm.addProcessListener ( this );
	// Instantiate and start a thread to run the process...
	Thread thread = new Thread ( pm );
	thread.start ();
	// The code is now asynchronous.  The output from the process must be
	// captured by implementing the ProcessListener interface.  For example,
	// implement processOutput() and processError() to display lines of
	// output as they are generated by the process.  Use processStatus() to
	// detect when the process has completed.
	</pre>
	</li>

<li>	Run a command as a thread and use a graphical user interface to monitor
	the command progress.
	For this approach, use a ProcessManagerDialog or ProcessManagerJDialog.
	</li>
</ol>
<p>

@see java.lang.Runtime
@see java.lang.Process
*/
public class ProcessManager 
implements ActionListener, Runnable
{

/**
Command to run.  If the command array is specified, this string will be created by using the array values
separated by spaces.  In this case, the command array is still used for actual calls
and the string is used for messages.
*/
private String __command = null;
/**
Command array, which can be used to initialize a process instead of a simple command string.
*/
private String [] __commandArray = null;
/**
Operating system interpreter to run the command.  This is by default determined automatically but can be
set.  An array is used because the interpreter often consists of a program name and option (e.g., cmd.exe /c).
*/
private String [] __commandInterpreterArray = null;

/**
String version of the __commandInterpreterArray with strings separated by spaces - use for messages.
*/
private String __commandInterpreter = null;

/**
The above is set to false only if setCommandInterpreter is called with a null array,
indicating that no command interpreter should be used.
*/
private boolean __isCommandInterpreterUsed = true;

/**
Process that runs __command
*/
private Process __process = null;
/**
Collects the standard output from __process.
*/
private InputStream __out = null;
/**
Indicates whether output from the process is done.
*/
private boolean __outDone = false;
/**
Collects the standard error from __process
*/
private InputStream __error = null;
/**
Buffers the __process standard output.
*/
private BufferedReader __outReader = null;
/**
Buffers the __process standard error.
*/
private BufferedReader __errorReader = null;
/**
If set to true by calling cancel(), the run() method will gracefully shut down.
*/
private volatile boolean __cancel = false;
/**
Indicates whether process is complete.  Use setProcessFinished() for all manipulation of this
variable so that the process time can be determined.
*/
private boolean __processFinished = false;
/**
Exit status from command (determined from STOP code).
*/
private int __exitValue = 0;
/**
Used to handle timeouts.
*/
private int __timeoutMilliseconds = 0;
/**
Event timer used if a timeout is specified.
*/
private EventTimer __eventTimer = null;
/**
To track the actual time used for the process.
*/
private StopWatch __stopwatch = null;
/**
Indicates whether output from the command should be saved, to be retrieved with getOutputList().
*/
private boolean __saveOutput = false;
/**
List containing process output, used if __save_output is true.
*/
private List __outList = null;
/**
List containing process errors, used if __save_output is true.
*/
private List __errorList = null;
/**
Listeners to receive process output.
*/
private ProcessListener [] __listeners = null;
/**
If not null, indicate a token that if found at the beginning of the line will
indicate the program exit status.
*/
private String __exitStatusIndicator = null;
/**
The level at which status output will be printed when a process is running.
*/
private int __processStatusLevel = 1;

/**
The working directory for the process.
*/
private File __workingDir = null;

/**
Create a ProcessManager for the given command.
Using this version calls the Runtime.exec ( String command ) method - it may be
more appropriate to call the version that takes an array for arguments.
The process is created only when the "run" function is called.
No timeout will be in effect (the process will run until complete, no matter how long it takes).
@param command  The command passed to the constructor, which can either specify
a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable, according to the operating system.
*/
public ProcessManager ( String command ) {
	this ( command, 0, null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String command ) method - it may be
more appropriate to call the version that takes an array for arguments.
@param command  The command passed to the constructor, which can either specify
a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable, according to the operating system.
@param timeoutMilliseconds If the process is not complete in this time, then
exit with a status of 999.  Specifying 0 will result in no timeout.
*/
public ProcessManager ( String command, int timeoutMilliseconds )
{	
    this ( command, timeoutMilliseconds, null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval and exit status indicator.
Using this version calls the Runtime.exec ( String command ) method - it may be
more appropriate to call the version that takes an array for arguments.
@param command  The command passed to the constructor, which can either specify
a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeoutMilliseconds If the process is not complete in this time, then
exit with a status of 999.  Specifying 0 will result in no timeout.
@param exitStatusIndicator a string which if found at the start of output lines, will be followed by an integer
exit status (e.g., "Status:").  If null, the exit status is taken from the process exit status.
@param useCommandShell indicates if the process should be run using a command shell.  Because the internal code
is not smart enough to evaluate whether a command to be run is a self-contained executable or a script that may
contain shell commands, the calling code must define whether the command shell is used.  Use true if unsure and
use false if it is known that a specific executable is being called.  Using the command shell may complicate
error and stream handling due to the additional layer of processes.
@param workingDir the working directory in which to run the command.  Specifying this allows the command name and
parameters to be specified relative to the working directory, if appropriate, which may actually be required to
limit the length of strings for some software.
*/
public ProcessManager ( String command, int timeoutMilliseconds, String exitStatusIndicator,
    boolean useCommandShell, File workingDir )
{   String routine = "ProcessManager";
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "ProcessManager constructor: \"" + command + "\"" );
    }
    __command = command;
    __timeoutMilliseconds = timeoutMilliseconds;
    __exitValue = 0;
    
    if ( __timeoutMilliseconds > 0 ) {
        // Setup a timer thread which will 
        __eventTimer = new EventTimer ( __timeoutMilliseconds, this, "Timeout" );
    }
    if ( (exitStatusIndicator != null) && exitStatusIndicator.equals("") ) {
        // Set to null for internal use
        exitStatusIndicator = null;
    }
    __exitStatusIndicator = exitStatusIndicator;
    __isCommandInterpreterUsed = useCommandShell;
    if ( workingDir == null ) {
        Message.printStatus( 2, routine, "Working directory for process is previous value." );
    }
    else {
        Message.printStatus( 2, routine, "Working directory for process is \"" + workingDir + "\"." );
    }
    __workingDir = workingDir;
}

/**
Create a ProcessManager for the given command.
Using this version calls the Runtime.exec ( String command[] ) method.
The process is created only when the "run" function is called.
No timeout will be in effect (the process will run until complete, no matter how long it takes).
@param command_array  The command passed to the constructor, which can either
specify a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param props PropList containing properties to control the process.  Currently,
the only property that is recognized is "ExitStatusTokens", which can be set
to a string to indicate tokens to check for (e.g., "STOP") to detect an exit
status.  This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating
system command call.  Currently, only one token can be specified and spaces
are used as a delimiter to evaluate the output.
*/
public ProcessManager ( String [] command_array, PropList props ) {
	this ( command_array, 0, props );
}

/**
Create a ProcessManager for the given command.
Using this version calls the Runtime.exec ( String command[] ) method.
The process is created only when the "run" function is called.
No timeout will be in effect (the process will run until complete, no matter how long it takes).
@param command_array  The command passed to the constructor, which can either
specify a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable,
according to the operating system.
*/
public ProcessManager ( String [] command_array ) {
	this ( command_array, 0, (String)null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor, which can either
specify a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeout_milliseconds If the process is not complete in this time, then
exit with a status of 999.  Specifying 0 will result in no timeout.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds )
{	this ( command_array, timeout_milliseconds, (String)null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor, which can either
specify a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeout_milliseconds If the process is not complete in this time, then
exit with a status of 999.  Specifying 0 will result in no timeout.
@param props PropList containing properties to control the process.  Currently,
the only property that is recognized is "ExitStatusTokens", which can be set
to a string to indicate tokens to check for (e.g., "Stop") to detect an exit
status.  This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating
system command call.  Currently, only one token can be specified and spaces
are used as a delimiter to evaluate the output.
@deprecated use the version that specifies the exitStatusIndicator as a string.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds, PropList props )
{
    this ( command_array, timeout_milliseconds, props == null ? (String)null : props.getValue( "ExitStatusTokens" ),
        true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor, which can either
specify a full path command or just the command itself.  If the full path is not
specified, the PATH environment variable is used to find the executable, according to the operating system.
@param timeout_milliseconds If the process is not complete in this time, then
exit with a status of 999.  Specifying 0 will result in no timeout.
@param exitStatusIndicator A string at the start of output lines to indicate tokens to check for
(e.g., "Stop") to detect an exit status.  This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating
system command call.  Currently, only one token can be specified and spaces
are used as a delimiter to evaluate the output.  The exit status integer is expected to follow the indicator.
@param useCommandShell indicates if the process should be run using a command shell.  Because the internal code
is not smart enough to evaluate whether a command to be run is a self-contained executable or a script that may
contain shell commands, the calling code must define whether the command shell is used.  Use true if unsure and
use false if it is known that a specific executable is being called.  Using the command shell may complicate
error and stream handling due to the additional layer of processes.
@param workingDir the working directory in which to run the command.  Specifying this allows the command name and
parameters to be specified relative to the working directory, if appropriate, which may actually be required to
limit the length of strings for some software.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds, String exitStatusIndicator,
    boolean useCommandShell, File workingDir )
{   String routine = "ProcessManager";
    // Create a single command string from the command parts...
    StringBuffer b = new StringBuffer ();
    for ( int i = 0; i < command_array.length; i++ ) {
        if ( i != 0 ) {
            b.append ( " " );
        }
        b.append ( command_array[i] );
    }
    __command = b.toString();
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "ProcessManager constructor: \"" + __command + "\"" );
    }
    __commandArray = command_array;
    __timeoutMilliseconds = timeout_milliseconds;
    __exitValue = 0;
    if ( (exitStatusIndicator != null) && exitStatusIndicator.equals("") ) {
        // Set to null for internal use
        exitStatusIndicator = null;
    }
    __exitStatusIndicator = exitStatusIndicator;
    if ( (__exitStatusIndicator != null) && Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "Will use lines starting with \"" +
        __exitStatusIndicator + "\" to detect end of process." );
    }
    
    if ( __timeoutMilliseconds > 0 ) {
        // Setup a timer thread which will 
        __eventTimer = new EventTimer ( __timeoutMilliseconds, this, "Timeout" );
    }
    __isCommandInterpreterUsed = useCommandShell;
    if ( workingDir == null ) {
        Message.printStatus( 2, routine, "Working directory for process is previous value." );
    }
    else {
        Message.printStatus( 2, routine, "Working directory for process is \"" + workingDir + "\"." );
    }
    __workingDir = workingDir;
}

/**
Handle the action event from the EventTimer.
@param e ActionEvent
*/
public void actionPerformed ( ActionEvent e )
{	if ( e.getActionCommand().equals("Timeout") && !__processFinished) {
		// Called by the __event_timer to allow a check for a timeout.
		// If this method is called, then the process needs to be stopped and an exit status of 999 returned.
		Message.printWarning ( 2, "ProcessManager.actionPerformed",
		"Process has timed out after " + __timeoutMilliseconds + " milliseconds." );
		setProcessFinished ( 999, "Process timed out." );
	}
}

/**
Add a ProcessListener to receive Process output.  Multiple listeners can be
registered.  If an attempt is made to register the same listener more than
once, the later attempt is ignored.
@param listener ProcessListener to add.
*/
public void addProcessListener ( ProcessListener listener )
{	// Use arrays to make a little simpler than Vectors to use later...
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added...
	// Resize the listener array...
	int size = 0;
	if ( __listeners != null ) {
		size = __listeners.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( __listeners[i] == listener ) {
			return;
		}
	}
	if ( __listeners == null ) {
		__listeners = new ProcessListener[1];
		__listeners[0] = listener;
	}
	else {
	    // Need to resize and transfer the list...
		size = __listeners.length;
		ProcessListener [] newlisteners = new ProcessListener[size + 1];
		for ( int i = 0; i < size; i++ ) {
			newlisteners[i] = __listeners[i];
		}
		__listeners = newlisteners;
		__listeners[size] = listener;
		newlisteners = null;
	}
}

/**
Cancel the process.  A flag is set, which is detected in the run() method, which
will then exit and do a graceful shutdown.
*/
public void cancel ()
{	__cancel = true;
}

/**
Clean up the process.  This should only be called by setProcessFinished().
*/
private void cleanup()
{	// Catch exceptions for each just in case something is thrown...
	try {
	    // Destroy the process..
		// Having problem on NT with cmd.exe processes not going away.
		// Maybe should not destroy - just let close themselves.  Part
		// of the problem is that two batch files are used.  Need to
		// figure out if Windows 2000 behaves better.
		__process.destroy();
	}
	catch ( Exception e ) {
	}
	try {
	    // Get rid of the timer...
		if ( __eventTimer != null ) {
			__eventTimer.finish();
		}
	}
	catch ( Exception e ) {
	}
	// Close the output streams...
	try {
	    if ( __out != null ) {
			__out.close();
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "", e );
	}
	try {
	    if ( __outReader != null ) {
			__outReader.close();
		}
	}
	catch ( Exception e ) {
	}
	try {
	    if ( __error != null ) {
			__error.close();
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "", e );
	}
	try {
	    if ( __errorReader != null ) {
			__errorReader.close();
		}
	}
	catch ( Exception e ) {
	}
}

/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable
{	__command = null;
	__process = null;
	__out = null;
	__error = null;
	__outReader = null;
	__errorReader = null;
	__eventTimer = null;
	__stopwatch = null;
	__outList = null;
	IOUtil.nullArray(__commandArray);
	IOUtil.nullArray(__commandInterpreterArray);
	__commandInterpreter = null;
	IOUtil.nullArray(__listeners);
	__exitStatusIndicator = null;
	
	super.finalize();
}

/**
Return the command this is being executed.
@return the command this is being executed.
*/
public String getCommand ()
{	return __command;
}

/**
Get the exit status of a process that has been run.  This should be called if
not threaded and the run() method is done.
If using threads, use the ProcessListener.processStatus() method to detect the end of a process.
@return The exit status of the process.  An exit status of non-0 is normally
considered an error.  The following exit status values are internally set:
<pre>
1 - if exit token is found with no integer or no stop token is found.
996 - error reading output line from process (e.g., when used with
runUntilFinished()).
997 - security exception running
997 - unable to create process
998 - unable to create temporary files on NT
999 - process timeout
</pre>
*/
public int getExitStatus ()
{	return __exitValue;
}

/**
Return the standard error of the process as a list of String.
@return the standard error of the process as a list of String.
If saveOutput(true) is called, this will be non-null.  Otherwise, it will be null.
*/
public List getErrorList ()
{	return __errorList;
}

/**
Return the standard output of the process as a list of String.
@return the standard output of the process as a list of String.
If saveOutput(true) is called, this will be non-null.  Otherwise, it will be null.
*/
public List getOutputList ()
{	return __outList;
}

/**
Returns the Process instance.  Use this call when processing I/O externally to
retrieve the streams for the process.
@return the Process instance.
*/
public Process getProcess()
{	return __process;
}

/**
Indicates whether process is finished running.  This will be the case if the
process has terminated normally or an error has occurred.
@return true if the process has finished running, false if not.
*/
public boolean isProcessFinished ()
{	return __processFinished;
}

/**
Remove a ProcessListener.  The matching object address is removed, even if
it was registered multiple times.
@param listener ProcessListener to remove.
*/
public void removeProcessListener ( ProcessListener listener )
{	if ( listener == null ) {
		return;
	}
	if ( __listeners != null ) {
		// Loop through and set to null any listeners that match the requested listener...
		int size = __listeners.length;
		int count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( (__listeners[i] != null) && (__listeners[i] == listener) ) {
				__listeners[i] = null;
			}
			else {
			    ++count;
			}
		}
		// Now resize the listener array...
		ProcessListener [] newlisteners = new ProcessListener[count];
		count = 0;
		for ( int i = 0; i < size; i++ ) {
			if ( __listeners[i] != null ) {
				newlisteners[count++] = __listeners[i];
			}
		}
		__listeners = newlisteners;
		newlisteners = null;
	}
}

/**
Instantiate and run the process.  The runtime environment is retrieved, the
input stream established, etc.  If a ProcessManager IS NOT defined as a Thread
then the command will be run all the way through and output can be retrieved
using the getOutput() method.  If a ProcessManager IS defined in a Thread
(e.g., Thread t = new Thread ( new ProcessManager("command")) ) then the
process will complete in one of the following ways:
<ol>
<li>	The process completes gracefully (and exits this method).  The exit
	status is set to the exit status of the external process.
	</li>
<li>	The process is interrupted (via a call to cancel()).  The exit status is set to 999.
	</li>
<li>	An error occurs.  The exit status is set to 999
	</li>
</ol>
*/
public void run ()
{	String rtn = "ProcessManager.run";
    boolean useProcessBuilder = true; // If using the ProcessBuilder class to create the process

	__stopwatch = new StopWatch();
	__stopwatch.start();

	// Start the process...

	String os_name = System.getProperty("os.name");
	String commandString = "";	// Actual command sent to exec.
	try {
	    // Might want to put the following in the constructor, but
		// leave here for now because we can rely on the check for a
		// non-null interpreter to know if the user has defined...
		if ( __commandInterpreter != null ) {
			// Assume that the calling code has set
			// __command_interpreter and
			// __command_interpreter_array
			// in the constructor or setCommandInterpreter().
		}
		else if ( os_name.equalsIgnoreCase( "Windows 95" )) {
			// Windows 95/98...
			__commandInterpreter = "command.com /C";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "command.com";
			__commandInterpreterArray[1] = "/C";
		}
		else if ( IOUtil.isUNIXMachine() ) {
			// Unix, including Linux...
			__commandInterpreter = "/bin/sh -c";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "/bin/sh";
			__commandInterpreterArray[1] = "-c";
		}
		else if ( os_name.startsWith("Windows") ) {
			// Assume "Windows NT", "Windows 2000", or later so
			// we don't have to be on the bleeding edge with changes...
			__commandInterpreter = "cmd.exe /C";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "cmd.exe";
			__commandInterpreterArray[1] = "/C";
		}
		else {
		    Message.printWarning ( 2, rtn, "Unable to start process for \"" + __command +
			"\".  Unknown OS \"" + os_name + "\"" );
			setProcessFinished ( 997, "Unknown operating system \""+ os_name + "\"" );
			return;
		}
		// Set up the command string, which is used for messages and
		// if the command array version is NOT used...
		if ( __isCommandInterpreterUsed ) {
			if ( __commandInterpreter.length() != 0 ) {
				// Command interpreter used and it is not empty...
				commandString = __commandInterpreter + " " + __command;
			}
			else {
			    // Command interpreter used but it is empty...
				commandString = __command;
			}
		}
		else {
		    // No interpreter - just use command...
			commandString = __command;
		}
		// Now actually start the process...
		if ( __commandArray != null ) {
			// Execute using the array of arguments...
			String [] array = null;
			if ( __isCommandInterpreterUsed ) {
				array = new String[__commandInterpreterArray.length + __commandArray.length];
			}
			else {
			    array = new String[__commandArray.length];
			}
			// Transfer command interpreter, if the command interpreter is to be used...
			int n = 0;
			if ( __isCommandInterpreterUsed ) {
				n = __commandInterpreterArray.length;
				for ( int i = 0; i < n; i++ ) {
					array[i]=__commandInterpreterArray[i];
				}
			}
			// Transfer command and arguments...
			for ( int i = 0; i < __commandArray.length; i++ ) {
				array[i + n] = __commandArray[i];
			}
		    Message.printStatus ( __processStatusLevel, rtn,
	             "Running on \"" + os_name + "\".  Executing process using individual program name and arguments (array):" );
		    for ( int i = 0; i < array.length; i++ ) {
		        Message.printStatus ( __processStatusLevel, rtn,
		            "Array [" + i + "] = \""+ array[i] + "\" (" + array[i].length() + " characters)." );
		    }
		    if ( useProcessBuilder ) {
		        ProcessBuilder pb = new ProcessBuilder ( array );
		        if ( __workingDir != null ) {
		            Message.printStatus ( 2, rtn, "Setting ProcessBuilder working directory to \"" + __workingDir + "\".");
		            pb.directory ( __workingDir );
		        }
		        __process = pb.start();
		    }
		    else {
	            Runtime rt = Runtime.getRuntime();
	            __process = rt.exec ( array );
		    }
		}
		else {
		    // Execute using the full command line.  The interpreter
			// is properly included (or not) in the command_string...
		    Message.printStatus ( __processStatusLevel, rtn,
	            "Running on \"" + os_name + "\".  Executing process using full command line \""+ commandString +
	            "\" (" + commandString.length() + " characters)." );
		    if ( useProcessBuilder ) {
		        ProcessBuilder pb = new ProcessBuilder ( __commandInterpreterArray[0], __commandInterpreterArray[1], __command );
               if ( __workingDir != null ) {
                    Message.printStatus ( 2, rtn, "Setting ProcessBuilder working directory to \"" + __workingDir + "\".");
                    pb.directory ( __workingDir );
                }
		        __process = pb.start();
	        }
	        else {
	            Runtime rt = Runtime.getRuntime();
	            __process = rt.exec ( commandString );
	        }
		}
	} catch ( SecurityException se ) {
		Message.printWarning ( 2, rtn, "Security exception encountered." );
		Message.printWarning ( 3, rtn, se );
		setProcessFinished ( 996, "Security exception (" + se + ")." );
		return;
	} catch ( Exception e ) {
		Message.printWarning ( 1, rtn, "Unable to create process using command \"" + commandString + "\"" );
		Message.printWarning ( 2, rtn, e );
		setProcessFinished ( 997, "Error creating process (" + e + ")." );
		return;
	}

	// If here the process started successfully.  Now connect streams to
	// the process to read the process output.  Note that internally this
	// class uses "out" even though the Process class refers to the output
	// from the exec'ed process as the input stream to Process.  It just
	// seems more intuitive to call it "out" and "error".
	
	// Always consume the standard error on a thread
	__error = __process.getErrorStream ();
    StreamConsumer errorConsumer = new StreamConsumer (__error, "Error",true,true);
    errorConsumer.start();

    // For the standard output, either just consume it (and process the strings later), or use the standard
    // output to know when the process is complete.
    int exitValueFromProcess = -9999; // from waitFor() or exitValue()
    int exitValueFromOutput = -9999; // from __exitStatusIndicator
    try {
    	//boolean handleOutputWithStreamConsumer = true; // Uses Process.waitFor() to determine stations
    	boolean handleOutputWithStreamConsumer = false; // Uses output and exitValue() to determine status
    	__out = __process.getInputStream ();
    	if ( handleOutputWithStreamConsumer ) {
    	    // Consume the stream
    	    StreamConsumer outputConsumer = new StreamConsumer (__out, "Output:",true,true);
    	    outputConsumer.start();
    	    // This will block if the process is not complete.
    	    exitValueFromProcess = __process.waitFor();
    	    // TODO SAM 2009-04-03 With this approach need to post-process the output if the exit status
    	    // is determined from output rather than waitFor() returned value.
    	    // TODO SAM 2009-04-03 Need to evaluate cancel of process
    	}
    	else {
    	    // Read the process output until null is returned (do not use Process.waitFor().
    	    // TODO SAM 2009-04-03 How did this ever detect the process end - only from end of output due to null?
    	    // Old code that has worked but hangs with recent work
        	// Now loop indefinitely to process the command output.
    	    //
    	    // The process output can be read one of two ways:
        	//
        	// 1.	The output is read here and either ignored or saved according to __save_output.
        	// 2.	The output is read from an external class (e.g.,
        	//	ProcessManagerJFrame, in which case that class must process all
        	//	the output.  In this case the thread will not totally shut down
        	//	until buffered output is complete.
        
        	// Always use this loop so that we can handle cancels if the ProcessManager is in a thread...
        
        	__outDone = false;
        	String exitStatusLine = null, outLine;
        	int i = 0;	// Reusable Loop counter
            __outReader = new BufferedReader(new InputStreamReader (__out));
        	while ( !__processFinished ) {
        		// Handle the output here.  In each loop we check for more
        		// standard output and error lines.  As soon as a stream returns
        		// null, it is assumed to be done (is this correct?)  What
        		// happens if there is no more input on the buffer - does it hang?
        		if ( !__outDone ) {
        		    //Message.printStatus(2, rtn, "Reading another line from stdout...");
        			outLine = __outReader.readLine();
        			//Message.printStatus ( 2, rtn, "...done reading another line.");
        			if ( outLine == null ) {
        				__outDone = true;
        				if ( Message.isDebugOn ) {
        					Message.printDebug (1, rtn, "End of output detected as null." );
        				}
        				// This will block if the process is not complete but since no more output is
        				// expected, it should work.
        	            exitValueFromProcess = __process.waitFor();
        			}
        			else {
        			    // Always log output
        			    //if ( Message.isDebugOn ) {
        					//Message.printDebug ( 1, rtn, "stdout line: \"" + out_line+ "\""  );
        					Message.printStatus ( 2, rtn, "stdout line: \"" + outLine+ "\""  );
        				//}
        				if ( __saveOutput ) {
        					__outList.add ( outLine );
        				}
        				if ( __exitStatusIndicator != null ) {
        					// Check the output for a line that starts with the token.
        					if ( StringUtil.startsWithIgnoreCase(outLine, __exitStatusIndicator)){
        						// Save the exit status line to evaluate below when the process is finished...
        					    if ( __exitStatusIndicator.length() >= outLine.trim().length() ) {
        					        Message.printWarning (3, rtn, "Exit status indicator \"" + __exitStatusIndicator +
        					             "\" is detected at the start of the output line: \"" + outLine +
        					             "\" but output is too short to have status - not using for exit status." );
        					    }
        					    else {
        					        exitStatusLine = outLine;
        					        Message.printStatus (2, rtn, "Exit status indicator \"" + __exitStatusIndicator +
        					            "\" detected in output line:  \"" + exitStatusLine + "\"" );
        					    }
        					}
        				}
        				if ( __listeners != null ) {
        					for ( i = 0; i <__listeners.length;i++){
        						__listeners[i].processOutput ( outLine );
        					}
        				}
        			}
        		}
         		// Check to see if the process has been cancelled due to a call
        		// to cancel() - this normally occurs only from something like
        		// the ProcessManagerJDialog.
        		if ( __cancel ) {
        			setProcessFinished ( 900, "Process cancelled." );
        		}
        		// Check to see if the process is done.  Do this by calling
        		// exitValue() instead of waitFor() in order to not have to
        		// wait for complete output.  This is important for the
        		// ProcessManagerJFrame where output is grabbed as the process
        		// runs.  Only check after the streams are done - hopefully they
        		// will actually be done close to the time that the process is done...
        		if ( __outDone ) {
        			try {
        			    if ( (__exitStatusIndicator != null) && (exitStatusLine != null) ) {
        					// Get the exit status from the exit line.  First remove the exit status indicator, then
        			        // parse using whitespace, and then convert the first token to an integer as the status.
        			        // The length of the line is guaranteed to be at least that of the indicator because of
        			        // checks performed above.
        			        String exitStatusLineCropped = exitStatusLine.substring(__exitStatusIndicator.length());
        					List v = StringUtil.breakStringList(exitStatusLineCropped, " \t",
        					    StringUtil.DELIM_SKIP_BLANKS);
        					if ( (v == null) || (v.size() < 1) || !StringUtil.isInteger( ((String)v.get(0)).trim()) ) {
        						// Get the exit status from the process...
        						exitValueFromProcess = __process.exitValue();
        						Message.printStatus (2, rtn, "Exit status could not be determined from saved output line \"" +
        						    exitStatusLine + "\" using exit status from process: " + exitValueFromProcess );
        					}
        					else {
        						exitValueFromOutput = Integer.parseInt (((String)v.get(0)).trim() );
        						Message.printStatus (2, rtn, "Exit status from output line using indicator \"" +
        						    __exitStatusIndicator + "\" is "+ exitValueFromOutput );
        					}
        				}
        				else if((__exitStatusIndicator != null) && (exitStatusLine == null) ) {
        					// Assume an error...
        					exitValueFromOutput = 1;
    						Message.printStatus (2, rtn,
    						    "Exit status set to 1 - expecting exit status from output line using \"" +
    						    __exitStatusIndicator + "\" but did not find it." );
        				}
        				else {
        				    // Get the exit status from the process...
        					exitValueFromProcess = __process.exitValue();
    						Message.printDebug (2, rtn, "Exit status from process was " + exitValueFromProcess );
        				}
        			    // Tell this ProcessManager what the exit value was so that external code can access
        			    // the value.
        			    if ( exitValueFromOutput != -9999 ) {
        			        setProcessFinished ( exitValueFromOutput, "" );
        			    }
        			    else {
        			        // Just use exit status from the process
        			        setProcessFinished ( exitValueFromProcess, "" );
        			    }
        				setErrorList ( errorConsumer.getOutputList() );
        				break; // This will allow the process to die.
        			}
        			catch ( Exception e ) {
        				// Assume it is not done even though there is no more standard output or error...
        			}
        		}
        	}
    	}
 	}
    catch ( InterruptedException e ) {
        Message.printWarning ( 2, rtn, "Process was interrupted (cancelled)." );
        Message.printWarning ( 3, rtn, e );
        setProcessFinished ( 996, "Process cancelled." );
    }
	catch ( Exception e ) {
		Message.printWarning ( 2, rtn, "Error processing output from command \"" + commandString + "\"" );
		Message.printWarning ( 3, rtn, e );
		setProcessFinished ( 996, "Error reading output." );
	}
	finally {
	    if ( exitValueFromOutput != -9999 ) {
	        __exitValue = exitValueFromOutput;
	    }
	    else {
	        __exitValue = exitValueFromProcess;
	    }
	}
}

/**
Indicate that the output from the command be saved in memory.  The run() method
will save the output, which can be retrieved using the getOutput() method.
The default is not to save the output.
@param save_output Indicates whether the output from the command should be saved.
*/
public void saveOutput ( boolean save_output )
{	__saveOutput = save_output;
	// Create the Vector for output...
	__outList = new Vector ();
}

/**
Set the command interpreter to use to run the command.  This method is typically only used when troubleshooting
command shell issues.  Otherwise, the following interpreters are used:
<pre>
Windows 95/98:  command.com /C
Windows NT, 2000, xp:  cmd.exe /C
UNIX/Linux:  /bin/sh -c
</pre>
@param interpreter The command interpreter separated into separate String
tokens.  If no interpreter is desired, specify a null array.
*/
public void setCommandInterpreter ( String [] interpreter )
{	String routine = "ProcessManager.setCommandInterpreter";
    __commandInterpreterArray = interpreter;
	__isCommandInterpreterUsed = true;	// Default
	if ( interpreter == null ) {
		__commandInterpreter = "";
		__isCommandInterpreterUsed = false;
		Message.printStatus( 2, routine, "Command interpreter will not be used because calling code has " +
			"set the interpreter string to null." );
	}
	else {
	    // Put together the interpreter string...
		StringBuffer b = new StringBuffer();
		for ( int i = 0; i < interpreter.length; i++ ) {
			if ( i != 0 ) {
				b.append ( " " );
			}
			b.append ( interpreter[i] );
		}
		__commandInterpreter = b.toString().trim();
	    Message.printStatus( 2, routine, "Command interpreter string is set to \"" + __commandInterpreter + "\"." );
	}
}

/**
Set the List that contains the error output for the process.  This is usually retrieved from a stream consumer.
*/
private void setErrorList ( List errorList ) {
    __errorList = errorList;
}

/**
Set the exit status for the process.  If this method is called, the process has
completed successfully or has failed.
@param exitValue Exit value for the process.
@param exitMessage Exit message associated with the exit value.
*/
public void setProcessFinished ( int exitValue, String exitMessage )
{	__processFinished = true;
	__stopwatch.stop();
	__exitValue = exitValue;
	Message.printStatus ( __processStatusLevel, "ProcessManager.setProcessFinished",
	"Process took "+ StringUtil.formatString(__stopwatch.getSeconds(),"%.3f")+ " seconds." );
	// Call the listeners...
	if ( __listeners != null ) {
		for ( int i = 0; i <__listeners.length;i++){
			__listeners[i].processStatus (exitValue, exitMessage);
		}
	}
	// Destroy the process to make sure that it does not somehow keep running...
	cleanup();
}

/**
Sets the status level of the messages generated by the ProcessManager during a
run.  The default is status level 1.
@param level the level at which ProcessManager's messages should be printed during a run.
*/
public void setRunStatusLevel(int level) {
	__processStatusLevel = level;
}

}