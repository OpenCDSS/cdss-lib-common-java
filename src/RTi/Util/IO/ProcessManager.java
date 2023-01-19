// ProcessManager - class that controls the execution of a process and the retrieving of output from that process.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import RTi.Util.GUI.EventTimer;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

/**
This ProcessManager class controls the execution of a process and the retrieval of output from that process.
The class primarily provides an interface to the Runtime Java interface,
using Process.exec() to run the command.
By default, the ProcessManager is not a separate thread.
Therefore, using ProcessManager will pause the calling program until the external process is complete.
Use ProcessManagerDialog or ProcessManagerJDialog if a threaded process with user feedback is desired
or create a thread using an instance of ProcessManager (see below for example).
<p>
The basic information associated with the process includes:
<ol>
<li>	Input to the process.
	This can be specified as command line arguments as part of the command.
	There is currently no way to provide input to the command line on its standard input
	(e.g., to provide newline terminated lines of input (this feature can be added if needed).
	</li>
<li>	Output from the process.
	Output is composed of standard error and standard output.
	This output, if viewed in a command shell, is typically intermixed and difficult to deal with separately.
	Currently in ProcessManager, only the standard output is available and can be echoed back to the calling code.
	For a non-threaded run, output from the process is typically retrieved by calling saveOutput(true)
	before running the process and then getOutputList() after the run.
	For a threaded process, use the ProcessListener.  See examples below.
	</li>
<li>	Exit status from the process.
	The exit status is retrieved when the process completes.
	For a non-threaded run, use the getExitStatus() method after the run is complete.
	For a threaded run, use a ProcessListener and handle the call to the processStatus() method.
	</li>
</ol>

Typically, a command will be run in one of the ways illustrated below:

<ol>
<li>	Run a command completely (e.g., a fast execution), and retrieve the output all at once,
	without the option to kill the process prematurely.

	<pre>
	// Use any of the available constructors.
	ProcessManager pm = new ProcessManager ( "Some command" );
	// Returns all the output.
	pm.saveOutput ( true );
	pm.run();
	List<String> output = pm.getOutputList();
	int status = pm.getExitStatus();
	// Then display output, status, etc.
	</pre>
	<p>

	In this case the ProcessManager is not run as a new thread.
	All of the output from the command is added to a list.
	The process cannot be terminated until it is complete.
	</li>

<li>	Run a command completely (e.g., a fast execution) as a thread, ignoring output.
	<p>

	<pre>
	// Use any of the available constructors...
	ProcessManager pm = new ProcessManager ( "Some command" );
	Thread thread = new Thread ( pm );
	thread.start ();	// This executes the run() method in ProcessManager.
	// Do not set pm to null.  This apparently causes a problem with the thread.
	</pre>
	<p>

	In this case the ProcessManager is run as a new thread.
	All of the output from the command is added to a list.
	</li>

<li>	Run a command completely (e.g., a fast execution), and retrieve the output all at once,
	WITH the option to kill the process prematurely.
	This can only be done using a thread.
	<p>

	<pre>
	// Use any of the available constructors...
	ProcessManager pm = new ProcessManager ( "Some command" );
	pm.saveOutput ( true );
	Thread thread = new Thread ( pm );
	pm.start ();	// This executes the run() method in ProcessManager.
	// Returns all the output.
	List<String> output = pm.getOutputList();
	// Because a thread is running, the following can be done to cancel the process.
	// If not already finished, this will set the exit status to 999 and gracefully
	// terminate the process by exiting out of the ProcessManager run() method.
	pm.cancel();
	// Can check the exit status.
	int status = pm.getExitStatus();
	// Then display output, status, etc.
	</pre>
	<p>

	In this case the ProcessManager is run as a new thread.
	All of the output from the command is added to a list.
	</li>

<li>	Run a command using a thread and retrieve its output as the command runs.
	This is the approach taken by the ProcessManagerDialog and
	ProcessManagerJDialog and is suitable for longer execution times.
	<p>

	<pre>
	// Create a process manager using an available constructor.
	ProcessManager pm = new ProcessManager ( "some command" );
	// Provide a listener to receive process output.
	pm.addProcessListener ( this );
	// Instantiate and start a thread to run the process.
	Thread thread = new Thread ( pm );
	thread.start ();
	// The code is now asynchronous.
	// The output from the process must be captured by implementing the ProcessListener interface.
	// For example, implement processOutput() and processError() to display lines of
	// output as they are generated by the process.
	// Use processStatus() to detect when the process has completed.
	</pre>
	</li>

<li>	Run a command as a thread and use a graphical user interface to monitor the command progress.
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
Command to run.
If the command array is specified, this string will be created by using the array values separated by spaces.
In this case, the command array is still used for actual calls and the string is used for messages.
The string DOES not contain surrounding quotes so add those if needed.
*/
private String __command = null;

/**
Command array, which can be used to initialize a process instead of a simple command string.
*/
private String [] __commandArray = null;
/**
Operating system interpreter to run the command.
This is by default determined automatically but can be set.
An array is used because the interpreter often consists of a program name and option (e.g., cmd.exe /c).
*/
private String [] __commandInterpreterArray = null;

/**
String version of the __commandInterpreterArray with strings separated by spaces - use for messages.
*/
private String __commandInterpreter = null;

/**
Environment that should be added to the existing environment during runs.
*/
private Map<String,String> __environmentMap = new HashMap<String,String>();

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
Indicates whether process is complete.
Use setProcessFinished() for all manipulation of this variable so that the process time can be determined.
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
private List<String> __outList = null;
/**
List containing process errors, used if __save_output is true.
*/
private List<String> __errorList = null;
/**
Listeners to receive process output.
*/
private ProcessListener [] __listeners = null;
/**
If not null, indicate a token that if found at the beginning of the line will indicate the program exit status.
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
@param command  The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
*/
public ProcessManager ( String command ) {
	this ( command, 0, null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String command ) method - it may be
more appropriate to call the version that takes an array for arguments.
@param command  The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeoutMilliseconds If the process is not complete in this time, then exit with a status of 999.
Specifying 0 will result in no timeout.
*/
public ProcessManager ( String command, int timeoutMilliseconds ) {	
    this ( command, timeoutMilliseconds, null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval and exit status indicator.
Using this version calls the Runtime.exec ( String command ) method - it may be
more appropriate to call the version that takes an array for arguments.
@param command  The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeoutMilliseconds If the process is not complete in this time,
then exit with a status of 999.  Specifying 0 will result in no timeout.
@param exitStatusIndicator a string which if found at the start of output lines,
will be followed by an integer exit status (e.g., "Status:").
If null, the exit status is taken from the process exit status.
@param useCommandShell indicates if the process should be run using a command shell.
Because the internal code is not smart enough to evaluate whether a command to be run
is a self-contained executable or a script that may contain shell commands,
the calling code must define whether the command shell is used.
Use true if unsure and use false if it is known that a specific executable is being called.
Using the command shell may complicate error and stream handling due to the additional layer of processes.
@param workingDir the working directory in which to run the command.
Specifying this allows the command name and parameters to be specified relative to the working directory,
if appropriate, which may actually be required to limit the length of strings for some software.
*/
public ProcessManager ( String command, int timeoutMilliseconds, String exitStatusIndicator,
    boolean useCommandShell, File workingDir ) {
    String routine = getClass().getSimpleName();
    if ( Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "ProcessManager constructor: \"" + command + "\"" );
    }
    // TODO smalers 2019-10-12 don't automatically quote because this causes problems on Linux:
    // - add the double quotes when needed
    // If the command includes spaces, escape with quotes.
    //if ( command.indexOf(" ") > 0 ) {
        //__command = "\"" + command + "\"";
    //}
    //else {
        __command = command;
    //}
    __timeoutMilliseconds = timeoutMilliseconds;
    __exitValue = 0;

    if ( __timeoutMilliseconds > 0 ) {
        // Setup a timer thread which will.
        __eventTimer = new EventTimer ( __timeoutMilliseconds, this, "Timeout" );
    }
    if ( (exitStatusIndicator != null) && exitStatusIndicator.equals("") ) {
        // Set to null for internal use.
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
specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param props PropList containing properties to control the process.
Currently, the only property that is recognized is "ExitStatusTokens",
which can be set to a string to indicate tokens to check for (e.g., "STOP") to detect an exit status.
This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating system command call.
Currently, only one token can be specified and spaces are used as a delimiter to evaluate the output.
*/
public ProcessManager ( String [] command_array, PropList props ) {
	this ( command_array, 0, props );
}

/**
Create a ProcessManager for the given command.
Using this version calls the Runtime.exec ( String command[] ) method.
The process is created only when the "run" function is called.
No timeout will be in effect (the process will run until complete, no matter how long it takes).
@param command_array  The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
*/
public ProcessManager ( String [] command_array ) {
	this ( command_array, 0, (String)null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeout_milliseconds If the process is not complete in this time,
then exit with a status of 999.  Specifying 0 will result in no timeout.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds ) {
	this ( command_array, timeout_milliseconds, (String)null, true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor,
which can either specify a full path command or just the command itself.
If the full path is not specified, the PATH environment variable is used to find the executable,
according to the operating system.
@param timeout_milliseconds If the process is not complete in this time,
then exit with a status of 999.  Specifying 0 will result in no timeout.
@param props PropList containing properties to control the process.
Currently, the only property that is recognized is "ExitStatusTokens", which can be set
to a string to indicate tokens to check for (e.g., "Stop") to detect an exit status.
This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating system command call.
Currently, only one token can be specified and spaces are used as a delimiter to evaluate the output.
@deprecated use the version that specifies the exitStatusIndicator as a string.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds, PropList props ) {
    this ( command_array, timeout_milliseconds, props == null ? (String)null : props.getValue( "ExitStatusTokens" ),
        true, (File)null );
}

/**
Create a ProcessManager for the given command and optionally specify a timeout interval.
Using this version calls the Runtime.exec ( String [] command ) method.
@param command_array The command passed to the constructor,
which can either specify a full path command or just the command itself.  If the full path is not specified,
the PATH environment variable is used to find the executable, according to the operating system.
@param timeout_milliseconds If the process is not complete in this time,
then exit with a status of 999.  Specifying 0 will result in no timeout.
@param exitStatusIndicator A string at the start of output lines to indicate tokens to check for
(e.g., "Stop") to detect an exit status.
This may be necessary, for example, if a FORTRAN program is called and
its exit status does not seem to be getting passed back through the operating system command call.
Currently, only one token can be specified and spaces are used as a delimiter to evaluate the output.
The exit status integer is expected to follow the indicator.
@param useCommandShell indicates if the process should be run using a command shell.
Because the internal code is not smart enough to evaluate whether a command to be run
is a self-contained executable or a script that may contain shell commands,
the calling code must define whether the command shell is used.
Use true if unsure and use false if it is known that a specific executable is being called.
Using the command shell may complicate error and stream handling due to the additional layer of processes.
@param workingDir the working directory in which to run the command.
Specifying this allows the command name and parameters to be specified relative to the working directory,
if appropriate, which may actually be required to limit the length of strings for some software.
*/
public ProcessManager ( String [] command_array, int timeout_milliseconds, String exitStatusIndicator,
    boolean useCommandShell, File workingDir ) {
    String routine = "ProcessManager";
    // Create a single command string from the command parts.
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
        // Set to null for internal use.
        exitStatusIndicator = null;
    }
    __exitStatusIndicator = exitStatusIndicator;
    if ( (__exitStatusIndicator != null) && Message.isDebugOn ) {
        Message.printDebug ( 1, routine, "Will use lines starting with \"" +
        __exitStatusIndicator + "\" to detect end of process." );
    }

    if ( __timeoutMilliseconds > 0 ) {
        // Setup a timer thread that will end the process.
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
public void actionPerformed ( ActionEvent e ) {
	if ( e.getActionCommand().equals("Timeout") && !__processFinished) {
		// Called by the __event_timer to allow a check for a timeout.
		// If this method is called, then the process needs to be stopped and an exit status of 999 returned.
		Message.printWarning ( 2, "ProcessManager.actionPerformed",
		"Process has timed out after " + __timeoutMilliseconds + " milliseconds." );
		setProcessFinished ( 999, "Process timed out." );
	}
}

/**
 * Add environment variables for the process.
 */
private void addEnvironmentToProcess( ProcessBuilder pb) {
	Map<String,String> pbMap = pb.environment();
	for ( String env: __environmentMap.keySet() ) {
		String oldVal = pbMap.get(env);
	    String newVal = __environmentMap.get(env);
	    // Determine if the map value has a +, indicating that it should be appended to existing values,
	    // and remove the + so that the set can occur.
	    boolean append = true;
	    if ( (newVal.length() > 0) && (newVal.charAt(0) == '+') ) {
	        append = true;
	        newVal = newVal.substring(1);
	    }
	    if ( append && (oldVal != null) ) {
	        pbMap.put(env, oldVal + newVal);
	       	//Message.printStatus(2, rtn, "Setting environment variable " + env + "=" + (oldVal + newVal) );
	    }
	    else {
	        // Just set.
	      	//Message.printStatus(2, rtn, "Setting environment variable " + env + "=" + newVal);
	        pbMap.put(env, newVal);
	    }
    }
}

/**
Add a ProcessListener to receive Process output.  Multiple listeners can be registered.
If an attempt is made to register the same listener more than once, the later attempt is ignored.
@param listener ProcessListener to add.
*/
public void addProcessListener ( ProcessListener listener ) {
	// Use arrays to make a little simpler than lists to use later.
	if ( listener == null ) {
		return;
	}
	// See if the listener has already been added.
	// Resize the listener array.
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
	    // Need to resize and transfer the list.
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
Cancel the process.  A flag is set, which is detected in the run() method,
which will then exit and do a graceful shutdown.
*/
public void cancel () {
	__cancel = true;
}

/**
Clean up the process.  This should only be called by setProcessFinished().
*/
private void cleanup() {
	// Catch exceptions for each just in case something is thrown.
	try {
	    // Destroy the process
		// Having problem on NT with cmd.exe processes not going away.
		// Maybe should not destroy - just let close themselves.
		// Part of the problem is that two batch files are used.
		// Need to figure out if Windows 2000 behaves better.
		__process.destroy();
	}
	catch ( Exception e ) {
	}
	try {
	    // Get rid of the timer.
		if ( __eventTimer != null ) {
			__eventTimer.finish();
		}
	}
	catch ( Exception e ) {
	}
	// Close the output streams.
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
throws Throwable {
	__command = null;
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
public String getCommand () {
	return __command;
}

// TODO SAM 2016-02-24 Evaluate whether to define these exit codes as enum or static for external comparison.
/**
Get the exit status of a process that has been run.
This should be called if not threaded and the run() method is done.
If using threads, use the ProcessListener.processStatus() method to detect the end of a process.
@return The exit status of the process.
An exit status of non-0 is normally considered an error.
The following exit status values are internally set:
<pre>
1 - if exit token is found with no integer or no stop token is found.
996 - error reading output line from process (e.g., when used with runUntilFinished()).
997 - security exception running
997 - unable to create process
998 - unable to create temporary files on NT
999 - process timeout
</pre>
*/
public int getExitStatus () {
	return __exitValue;
}

/**
Return the standard error of the process as a list of String.
@return the standard error of the process as a list of String.
If saveOutput(true) is called, this will be non-null.  Otherwise, it will be null.
*/
public List<String> getErrorList () {
	return __errorList;
}

/**
Return the standard output of the process as a list of String.
@return the standard output of the process as a list of String.
If saveOutput(true) is called, this will be non-null.  Otherwise, it will be null.
*/
public List<String> getOutputList () {
	return __outList;
}

/**
Returns the Process instance.  Use this call when processing I/O externally to retrieve the streams for the process.
@return the Process instance.
*/
public Process getProcess() {
	return __process;
}

/**
Indicates whether process is finished running.
This will be the case if the process has terminated normally or an error has occurred.
@return true if the process has finished running, false if not.
*/
public boolean isProcessFinished () {
	return __processFinished;
}

/**
Remove a ProcessListener.
The matching object address is removed, even if it was registered multiple times.
@param listener ProcessListener to remove.
*/
public void removeProcessListener ( ProcessListener listener ) {
	if ( listener == null ) {
		return;
	}
	if ( __listeners != null ) {
		// Loop through and set to null any listeners that match the requested listener.
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
		// Now resize the listener array.
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
Instantiate and run the process.
The runtime environment is retrieved (and is appended to if setEnvironment() has been called),
the input stream established, etc.
If a ProcessManager IS NOT defined as a Thread then the command will be run
all the way through and output can be retrieved using the getOutput() method.
If a ProcessManager IS defined in a Thread
(e.g., Thread t = new Thread ( new ProcessManager("command")) ) then the
process will complete in one of the following ways:
<ol>
<li>	The process completes gracefully (and exits this method).
	The exit status is set to the exit status of the external process.
	</li>
<li>	The process is interrupted (via a call to cancel()).  The exit status is set to 999.
	</li>
<li>	An error occurs.  The exit status is set to 999
	</li>
</ol>
*/
public void run () {
	String rtn = "ProcessManager.run";

	__stopwatch = new StopWatch();
	__stopwatch.start();

	// Start the process.

	String os_name = System.getProperty("os.name");
	String commandString = "";	// Actual command sent to exec().
	try {
	    // Might want to put the following in the constructor,
		// but leave here for now because we can rely on the check for a non-null interpreter to know if the user has defined.
		if ( __commandInterpreter != null ) {
			// Assume that the calling code has set in the constructor or setCommandInterpreter():
			//   __command_interpreter
			//   __command_interpreter_array
		}
		else if ( os_name.equalsIgnoreCase( "Windows 95" )) {
			// Older Windows 95/98.
			__commandInterpreter = "command.com /C";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "command.com";
			__commandInterpreterArray[1] = "/C";
		}
		else if ( IOUtil.isUNIXMachine() ) {
			// Unix, including Linux.
			__commandInterpreter = "/bin/sh -c";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "/bin/sh";
			__commandInterpreterArray[1] = "-c";
		}
		else if ( os_name.startsWith("Windows") ) {
			// Most recent Windows.
			__commandInterpreter = "cmd.exe /C";
			__commandInterpreterArray = new String[2];
			__commandInterpreterArray[0] = "cmd.exe";
			__commandInterpreterArray[1] = "/C";
		}
		else {
		    Message.printWarning ( 2, rtn, "Unknown OS \"" + os_name + "\". Unable to start process for:  " + __command );
			setProcessFinished ( 997, "Unknown operating system \""+ os_name + "\"" );
			return;
		}
		// Set up the command string, which is used for messages and if the command array version is NOT used.
		if ( __isCommandInterpreterUsed ) {
			if ( !__commandInterpreter.isEmpty() ) {
				// Command interpreter used and it is not empty:
				// - when the interpreter is used, the command to run follows an argument,
				//   and need to encapsulate it in double quotes if necessary to escape content
				// - don't use surrounding double quotes if the command string starts with double quotes,
				//   such as if the program is already:  "C:\Program Files...",
				//   which assumes that the command also has double quotes in other necessary places
				if ( ((__command.indexOf(" ") > 0) || (__command.indexOf("\t") > 0)) &&
					!__command.startsWith("\"") ) {
					Message.printStatus(2,rtn,"Adding double quotes around command due to whitespace in command.");
					commandString = __commandInterpreter + " \"" + __command + "\"";
				}
				else {
					commandString = __commandInterpreter + " " + __command;
				}
			}
			else {
			    // Command interpreter used but it is empty.
				commandString = __command;
			}
		}
		else {
		    // No interpreter - just use command.
			commandString = __command;
		}
		// Now actually start the process.
		if ( __commandArray != null ) {
			// Execute using the array of arguments.
			String [] array = null;
			if ( __isCommandInterpreterUsed ) {
				array = new String[__commandInterpreterArray.length + __commandArray.length];
			}
			else {
			    array = new String[__commandArray.length];
			}
			// Transfer command interpreter, if the command interpreter is to be used.
			int n = 0;
			if ( __isCommandInterpreterUsed ) {
				n = __commandInterpreterArray.length;
				for ( int i = 0; i < n; i++ ) {
					array[i]=__commandInterpreterArray[i];
				}
			}
			// Transfer command and arguments.
			for ( int i = 0; i < __commandArray.length; i++ ) {
				array[i + n] = __commandArray[i];
			}
		    Message.printStatus ( __processStatusLevel, rtn,
	             "Running on \"" + os_name + "\".  Executing process using individual program name and arguments (array):" );
		    for ( int i = 0; i < array.length; i++ ) {
		        Message.printStatus ( __processStatusLevel, rtn,
		            "Array [" + i + "] = \""+ array[i] + "\" (" + array[i].length() + " characters)." );
		    }
		    // Use ProcessBuilder to run the process.
		    ProcessBuilder pb = new ProcessBuilder ( array );
		    if ( __workingDir != null ) {
		        Message.printStatus ( 2, rtn, "Setting ProcessBuilder working directory to \"" + __workingDir + "\".");
		        pb.directory ( __workingDir );
		    }
		    // Add the environment to the process.
		    addEnvironmentToProcess(pb);
		    __process = pb.start();
		}
		else {
		    // Execute using the full command line.  The interpreter is properly included (or not) in the command_string.
		    Message.printStatus ( __processStatusLevel, rtn,
	            "Running on \"" + os_name + "\".  Executing process using full command line : " + commandString );
		    Message.printStatus ( __processStatusLevel, rtn,
	            "Command string length = " + commandString.length() + " characters." );
		    if ( !IOUtil.isUNIXMachine() && (commandString.length() > 8191) ) {
                Message.printStatus ( 2, rtn, "Maximum Windows command line length is 8191. Command may not run.");
		    }
		    // Need to separate the command shell and its argument so that the underlying
		    // system "exec" call can find the program in the path.
		    // The remaining command is passed as is but may require care with double quotes around commands that contain spaces.
		    ProcessBuilder pb = new ProcessBuilder ( __commandInterpreterArray[0], __commandInterpreterArray[1], __command );
            if ( __workingDir != null ) {
                Message.printStatus ( 2, rtn, "Setting ProcessBuilder working directory to \"" + __workingDir + "\".");
                pb.directory ( __workingDir );
            }
		    addEnvironmentToProcess(pb);
		    __process = pb.start();
		}
	} catch ( SecurityException se ) {
		Message.printWarning ( 2, rtn, "Security exception encountered." );
		Message.printWarning ( 3, rtn, se );
		setProcessFinished ( 996, "Security exception (" + se + ")." );
		return;
	} catch ( Exception e ) {
		// Do not use quotes here to avoid confusion with whether quotes are in the command string.
		Message.printWarning ( 1, rtn, "Unable to create process using command: " + commandString );
		Message.printWarning ( 2, rtn, e );
		setProcessFinished ( 997, "Error creating process (" + e + ")." );
		return;
	}

	// If here the process started successfully.
	// Now connect streams to the process to read the process output.
	// Note that internally this class uses "out" even though the Process class refers to the output
	// from the exec'ed process as the input stream to Process.
	// It just seems more intuitive to call it "out" and "error".
	
	// Always consume the standard error on a thread.
	__error = __process.getErrorStream ();
    StreamConsumer errorConsumer = new StreamConsumer (__error, "Error",true,true);
    errorConsumer.start();

    // For the standard output, either just consume it (and process the strings later),
    // or use the standard output to know when the process is complete.
    int exitValueFromProcess = -9999; // From waitFor() or exitValue().
    int exitValueFromOutput = -9999; // From __exitStatusIndicator.
    try {
    	//boolean handleOutputWithStreamConsumer = true; // Uses Process.waitFor() to determine stations.
    	boolean handleOutputWithStreamConsumer = false; // Uses output and exitValue() to determine status.
    	__out = __process.getInputStream ();
    	if ( handleOutputWithStreamConsumer ) {
    	    // Consume the stream.
    	    StreamConsumer outputConsumer = new StreamConsumer (__out, "Output:",true,true);
    	    outputConsumer.start();
    	    // This will block if the process is not complete.
    	    exitValueFromProcess = __process.waitFor();
    	    // TODO SAM 2009-04-03 With this approach need to post-process the output if the exit status
    	    // is determined from output rather than waitFor() returned value.
    	    // TODO SAM 2009-04-03 Need to evaluate cancel of process.
    	}
    	else {
    	    // Read the process output until null is returned (do not use Process.waitFor().
    	    // TODO SAM 2009-04-03 How did this ever detect the process end - only from end of output due to null?
    	    // Old code that has worked but hangs with recent work.
        	// Now loop indefinitely to process the command output.
    	    //
    	    // The process output can be read one of two ways:
        	//
        	// 1.	The output is read here and either ignored or saved according to __save_output.
        	// 2.	The output is read from an external class (e.g.,
        	//	ProcessManagerJFrame, in which case that class must process all the output.
    		// In this case the thread will not totally shut down until buffered output is complete.

        	// Always use this loop so that we can handle cancels if the ProcessManager is in a thread.

        	__outDone = false;
        	String exitStatusLine = null, outLine;
        	int i = 0;	// Reusable Loop counter.
            __outReader = new BufferedReader(new InputStreamReader (__out));
        	while ( !__processFinished ) {
        		// Handle the output here.  In each loop we check for more standard output and error lines.
        		// As soon as a stream returns null, it is assumed to be done (is this correct?).
        		// What happens if there is no more input on the buffer - does it hang?
        		if ( !__outDone ) {
        		    //Message.printStatus(2, rtn, "Reading another line from stdout...");
        			outLine = __outReader.readLine();
        			//Message.printStatus ( 2, rtn, "...done reading another line.");
        			if ( outLine == null ) {
        				__outDone = true;
        				if ( Message.isDebugOn ) {
        					Message.printDebug (1, rtn, "End of output detected as null." );
        				}
        				// This will block if the process is not complete but since no more output is expected, it should work.
        	            exitValueFromProcess = __process.waitFor();
        			}
        			else {
        			    // Always log output.
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
        						// Save the exit status line to evaluate below when the process is finished.
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
         		// Check to see if the process has been canceled due to a call to cancel().
        		// This normally occurs only from something like the ProcessManagerJDialog.
        		if ( __cancel ) {
        			setProcessFinished ( 900, "Process canceled." );
        		}
        		// Check to see if the process is done.
        		// Do this by calling exitValue() instead of waitFor() in order to not have to wait for complete output.
        		// This is important for the ProcessManagerJFrame where output is grabbed as the process runs.
        		// Only check after the streams are done.hopefully they
        		// Hopefully will actually be done close to the time that the process is done.
        		if ( __outDone ) {
        			try {
        			    if ( (__exitStatusIndicator != null) && (exitStatusLine != null) ) {
        					// Get the exit status from the exit line.  First remove the exit status indicator,
        			    	// then parse using whitespace, and then convert the first token to an integer as the status.
        			        // The length of the line is guaranteed to be at least that of the indicator because of
        			        // checks performed above.
        			        String exitStatusLineCropped = exitStatusLine.substring(__exitStatusIndicator.length());
        					List<String> v = StringUtil.breakStringList(exitStatusLineCropped, " \t",
        					    StringUtil.DELIM_SKIP_BLANKS);
        					if ( (v == null) || (v.size() < 1) || !StringUtil.isInteger( ((String)v.get(0)).trim()) ) {
        						// Get the exit status from the process.
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
        					// Assume an error.
        					exitValueFromOutput = 1;
    						Message.printStatus (2, rtn,
    						    "Exit status set to 1 - expecting exit status from output line using \"" +
    						    __exitStatusIndicator + "\" but did not find it." );
        				}
        				else {
        				    // Get the exit status from the process.
        					exitValueFromProcess = __process.exitValue();
    						Message.printDebug (2, rtn, "Exit status from process was " + exitValueFromProcess );
        				}
        			    // Tell this ProcessManager what the exit value was so that external code can access the value.
        			    if ( exitValueFromOutput != -9999 ) {
        			        setProcessFinished ( exitValueFromOutput, "" );
        			    }
        			    else {
        			        // Just use exit status from the process.
        			        setProcessFinished ( exitValueFromProcess, "" );
        			    }
        				setErrorList ( errorConsumer.getOutputList() );
        				break; // This will allow the process to die.
        			}
        			catch ( Exception e ) {
        				// Assume it is not done even though there is no more standard output or error.
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
Indicate that the output from the command be saved in memory.
The run() method will save the output, which can be retrieved using the getOutput() method.
The default is not to save the output.
@param save_output Indicates whether the output from the command should be saved.
*/
public void saveOutput ( boolean save_output ) {
	__saveOutput = save_output;
	// Create the list for output.
	__outList = new ArrayList<String>();
}

/**
Set the command interpreter to use to run the command.
This method is typically only used when troubleshooting command shell issues.
Otherwise, the following interpreters are used:
<pre>
Windows 95/98:  command.com /C
Windows NT, 2000, xp:  cmd.exe /C
UNIX/Linux:  /bin/sh -c
</pre>
@param interpreter The command interpreter separated into separate String tokens.
If no interpreter is desired, specify a null array.
*/
public void setCommandInterpreter ( String [] interpreter ) {
	String routine = getClass().getSimpleName() + ".setCommandInterpreter";
    __commandInterpreterArray = interpreter;
	__isCommandInterpreterUsed = true;	// Default.
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
Set the environment variables to be used by the run.
@param the map of environment variables to set for the run.
Specify the value with a + as the first character to append to the existing value,
or no + to set to the specified value.
*/
public void setEnvironment ( Map<String,String> env ) {
    __environmentMap = env;
}

/**
Set the List that contains the error output for the process.  This is usually retrieved from a stream consumer.
*/
private void setErrorList ( List<String> errorList ) {
    __errorList = errorList;
}

/**
Set the exit status for the process.
If this method is called, the process has completed successfully or has failed.
@param exitValue Exit value for the process.
@param exitMessage Exit message associated with the exit value.
*/
public void setProcessFinished ( int exitValue, String exitMessage ) {
	__processFinished = true;
	__stopwatch.stop();
	__exitValue = exitValue;
	Message.printStatus ( __processStatusLevel, "ProcessManager.setProcessFinished",
	"Process took "+ StringUtil.formatString(__stopwatch.getSeconds(),"%.3f")+ " seconds." );
	// Call the listeners.
	if ( __listeners != null ) {
		for ( int i = 0; i <__listeners.length;i++){
			__listeners[i].processStatus (exitValue, exitMessage);
		}
	}
	// Destroy the process to make sure that it does not somehow keep running.
	cleanup();
}

/**
Sets the status level of the messages generated by the ProcessManager during a run.
The default is status level 1.
@param level the level at which ProcessManager's messages should be printed during a run.
*/
public void setRunStatusLevel(int level) {
	__processStatusLevel = level;
}

}