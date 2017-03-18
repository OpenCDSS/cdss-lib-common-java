//------------------------------------------------------------------------------
// RTi.Util.Message.Message - debug and status message class.
//------------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 26 Aug 1997  Matthew J. Rutherford,  Created initial version based
//              RTi                     on HBData C functions.
// 12 Nov 1997  Steven A. Malers, RTi   Add setDebug(boolean) to allow for
//                                      more control of debugging.
// 16 Mar 1998  SAM, RTi                Add javadoc.
// 10 Aug 1998  SAM, RTi                Update versions that take exceptions
//                                      to print to open log file.  There is
//                                      currently no way to get the stack trace
//                                      as strings.
// 06 Oct 1998  SAM, RTi                Clean up code in conjunction with C++
//                                      port.
// 23 Dec 1998  SAM, RTi                Overload setDebugLevel to take a string.
// 04 Feb 1999  SAM, RTi                Change so that instead of accepting
//                                      an Exception, printWarning accepts a
//                                      Throwable.
// 20 Feb 1999  SAM, RTi                Check for null Throwable, routine when
//                                      printing.
// 17 Sep 1999  SAM, RTi                Add closeLogFile().
// 15 Mar 2001  SAM, RTi                Add setProp() and
//                                      getMessageProp() to allow more
//                                      flexibility in message handling,
//                                      especially for Warning dialogs.  Change
//                                      IO to IOUtil.  Clean up javadoc and set
//                                      unused variables to null.
// 07 Aug 2001  SAM, RTi                Update so that the output functions can
//                                      be set to null to stop output
//                                      redirection.
// 2002-05-24   SAM, RTi                closeLogFile() was not static and was
//                                      not able to be called.  Make static so
//                                      it can be used in TSTool.
// 2003-04-09   SAM, RTi                Fix bug where initialize() was going
//                                      into an infinite recursion if the debug
//                                      flag is already turned on by external
//                                      code.
//------------------------------------------------------------------------------
// 2003-08-22   J. Thomas Sapienza, RTi Switched over to use the Swing
//                                      MessageJDialog and DiagnosticsJFrame.
// 2005-03-11   SAM, RTi                * Add a property in setPropValue() to
//                                        turn on levels for the messages.
//                                      * Do similar to above for message tags
//                                        and add __SHOW_MESSAGE_TAG to the
//                                        options for the behavior flag.
//                                      * Overload the printWarning() method
//                                        to take a tag for use in the log file
//                                        viewer.
// 2005-03-22   JTS, RTi                * Added addMessageLogListener().
//                                      * Added getMessageLogListeners().
//                                      * Added removeMessageLogListener().
// 2005-05-12   JTS, RTi                * Added restartLogFile().
//                                      * Added openNewLogFile().
// 2005-10-19   SAM, RTi                * Change so that the warning dialog is
//                                        not displayed if running in batch
//                                        mode.  In this case the GUI would not
//                                        be visible and the application waits
//                                        for a response on an invisible dialog.
// 2005-12-12   JTS, RTi                Changed restartLogFile() because in
//                                      certain conditions the code was not
//                                      opening the same log file after it was
//                                      closed.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.Message;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import javax.swing.JFrame;

/**
This class provides useful static functions for printing debug, status, and
warning messages (see the print* routines).  The class works both for terminal
and GUI based applications.
If a message level is zero, then no messages will be generated.  The higher the
number, the more detailed the messages.  If a warning message is printed at
level 1 and it is from a GUI application, a modal dialog will appear that
forces the user to acknowledge the warning.
To use the class, place code similar to the following in the main application:
<p>
<pre>
Message.setDebugLevel ( Message.TERM_OUTPUT, 0 );      // Stdout
Message.setWarningLevel ( Message.TERM_OUTPUT, 0 );
Message.setDebugLevel ( Message.LOG_OUTPUT, 10 );      // Log file
Message.setWarningLevel ( Message.LOG_OUTPUT, 10 );
Message.setWarningLevel ( Message.STATUS_HISTORY_OUTPUT, 1 );
</pre>
<p>

As a developer, decide on the appropriate message levels in code, with
level 1 messages being visible to the user.  Libraries should generally never
print level 1 warning messages.  Errors should be trapped and migrated back to
the main application through exceptions.  Use the print* methods to embed
message calls in code.  Because debug messages may occur often and because
formatting messages is a performance hit, debug messages should be wrapped in the following code:
<p>
<pre>
if ( Message.isDebugOn ) {
  // Debug message...
  Message.printDebug ( ... );
}
</pre>

If using messages with a GUI, let the Message class know what the top-level
component is (for use with the warning dialog, etc.)...
<p>

<pre>
Message.setTopLevel ( this );  // Where this is usually a frame.
</pre>
<p>

The following causes messages to be passed to a function, which can in turn
process the messages (the DiagnosticsJFrame does this).  In the future,
a listener approach may be taken.
<p>

<pre>
       Message.setOutputFunction ( Message.STATUS_HISTORY_OUTPUT, this,
               "printStatusMessages" );
</pre>
*/
public abstract class Message
{


/**
The following are used when setting message output locations.  Output to the
terminal (UNIX or DOS shell).  This prints to System.out.
*/
public final static int TERM_OUTPUT = 0;

/**
Output to the log file.
*/
public final static int LOG_OUTPUT = 1;

/**
Output messages to the DiagnosticsJFrame as a scrolling history.
@see DiagnosticsJFrame
*/
public final static int STATUS_HISTORY_OUTPUT = 2;

/**
Output to a one-line status bar at the bottom of the application screen.
The routine to print the message needs to be set using setOutputFunction.
REVISIT JAVADOC: see RTi.Util.Message.Message.setOutputFunction
*/
public final static int STATUS_BAR_OUTPUT = 3;

/**
Output to a scrolling status area within ProcessManagerDialog or
ProcessManagerJDialog - this may not be needed after 2002-10-16 revisions to the ProcessManager.
@see RTi.Util.IO.ProcessManagerDialog
@see RTi.Util.IO.ProcessManagerJDialog
*/
public final static int PROCESS_MANAGER_GUI = 4;

/**
A list of output names corresponding to the *_OUTPUT values.
*/
public final static String [] OUTPUT_NAMES	= {
	"Terminal",
	"Log file",
  	"Status history",
  	"Status bar",
	"Process manager" };

/**
Indicates whether debug is on.  This is public so that it can be quickly
accessed.  To increase performance, all debug messages should be wrapped with
<p>
<pre>
if ( Message.isDebugOn ) {
  // Do the debug message...
  Message.printDebug ( 1, "myroutine", "the message" );
}
</pre>
The default to no debugging being done.  Set to true in command-line parsing
code if setDebug is called.
*/
public static boolean isDebugOn = false;

/**
The following are flags for printing messages, set by various methods in this
class.  The following setting will print the message level in debug and warning
messages (the default is to not show the level).  The Diagnostics GUI will at
some point edit all of these settings.

Show the message level in output (normally the level is not displayed in messages).
*/
public final static int SHOW_MESSAGE_LEVEL = 0x1;

/**
Show the dialog for warning messages at level 1 (this is the default).
THIS IS BEING PHASED OUT IN FAVOR OF THE PROPERTIES.
*/
public final static int GUI_FOR_WARNING_1 = 0x2;

/**
Show the routine name (the default is to print the routine name for debug
messages and for warning messages greater than level 1).
*/
public final static int SHOW_ROUTINE = 0x4;

/**
Flush the output buffer after each message is written.  The default is to let
the system flush the output buffer.
*/
public final static int FLUSH_OUTPUT = 0x8;

private static final MessageImpl impl = getImpl();

private static MessageImpl getImpl() {
    String prop = System.getProperty("RTi.Util.MessageImpl");
    MessageImpl impl = null;
    Exception ex = null;
    if (prop != null) {
        try {
            impl = (MessageImpl) Class.forName(prop).newInstance();
        } catch (Exception e) {
            ex = e;
        }
    } 
    if (impl == null) {
        impl = new MessageImpl();
    }
    if (ex != null) {
        impl.printWarning(0,"getImpl",ex);
    }
    return impl;
}

/**
Adds a listener to the list of listeners that can respond to actions from
the MessageLogJFrame.  The listeners are updated by the MessageLogJFrame 
every time its processLogFile() method is called and a log file is read and 
displayed.  Any listeners added after a log file is read will not receive
commands from the MessageLogJFrame until it reads the log file again.
@param listener the listener to add.
*/
public static void addMessageLogListener(MessageLogListener listener) {
    impl.addMessageLogListener(listener);
}

/**
Flush and close the log file associated with Message.LOG_OUTPUT, if it has been opened.
*/
public static void closeLogFile ()
{	
    impl.closeLogFile();
}

/**
Flush the output buffers.  It does not appear that this method has any effect on some systems.
@param flag Currently unused.
*/
public static void flushOutputFiles ( int flag )
{	
    impl.flushOutputFiles(flag);
}

/**
Return the debug level for an output stream.
@return The debug level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
public static int getDebugLevel ( int i )
{	
    return impl.getDebugLevel(i);
}

/**
Return the name of the log file.
@return The name of the log file.
*/
public static String getLogFile ()
{	return impl.getLogFile();
}

/**
Returns the list of listeners that are set to respond to actions from the
MessageLogJFrame.  The returned list will never be null.
@return the list of listeners for the MessageLogJFrame.
*/
public static List<MessageLogListener> getMessageLogListeners() {
	return impl.getMessageLogListeners();
}

/**
Return the value of a Message property.  See setPropValue() for a description of valid properties.
@return the property value or null if not defined.
*/
public static String getPropValue ( String key )
{	
    return impl.getPropValue(key);
}

/**
Return the status level for an output stream.
@return The status level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
public static int getStatusLevel ( int i )
{	
    return impl.getStatusLevel(i);
}

/**
Return the warning level for an output stream.
@return The warning level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
public static int getWarningLevel ( int i )
{	
    return impl.getWarningLevel(i);
}

/**
Open the log file.
Because no log file is specified, the name of the log file will default to
the program name and the extension ".log" (set with IOUtil.setProgramName).
@see RTi.Util.IO.IOUtil#setProgramName
@return The PrintWriter corresponding to the log file.
@exception IOException if there is an error opening the log file.
*/
public static PrintWriter openLogFile ()
throws IOException
{	
    return impl.openLogFile();
}

/**
Open the log file using the specified name.
@return The PrintWriter corresponding to the log file.
@exception IOException if there is an error opening the log file.
@param logfile Name of log file to open.
*/
public static PrintWriter openLogFile ( String logfile )
throws IOException
{	return impl.openLogFile(logfile);
}

/**
Opens a new file at the specified location.
@param path the full path to the new log file to open.
*/
public static void openNewLogFile(String path) 
throws Exception {
	impl.openNewLogFile(path);
}

/**
Print a debug message to the registered output receivers.
@param level Debug level for the message.
@param routine Name of the routine printing the message.
@param message Debug message.
*/
public static void printDebug( int level, String routine, String message )
{	
    impl.printDebug(level,routine,message);
}

/**
Print a stack trace as if debug message.  Currently, this will print to the
log file if it is not null.
@param level Debug level for the message.
@param routine Name of the routine printing the message.
@param e Throwable (e.g., Error, Exception) for which to print a stack trace.
*/
public static void printDebug ( int level, String routine, Throwable e )
{	
    impl.printDebug(level,routine,e);
}

/**
Print information about the registered message levels using status messages,
using the current message settings.
*/
static public void printMessageLevels ( )
{	
    impl.printMessageLevels();
}

/**
Print information about the registered message levels using the current message settings.
@param flag If true, print using status messages.  If false, print to the
system standard output.
*/
static public void printMessageLevels ( boolean flag )
{	
    impl.printMessageLevels(flag);
}

/**
Print a status message to the registered output receivers.
@param level Status level for the message.
@param routine Name of the routine printing the message.
@param message Status message.
*/
public static void printStatus ( int level, String routine, String message )
{	
    impl.printStatus(level,routine,message);
}

/**
This routine calls printWarning but allows the developer to specify a
different _top_level frame from the preset top level frame.  This is useful
when you are printing a warning of level 1 and want to specify which window
that WarningDialog should be associated with, without changing the top
level window that should be typically used.
*/
public static void printWarning ( int level, String routine, String message, JFrame top_level )
{
	if ( top_level == null ) {
		impl.printWarning(level,routine,message);
	}
	else {
		impl.printWarning(level,routine,message,top_level);
	}
}

/**
Print a warning message to the registered output receivers.  The overloaded
version is called without the tag.
@param level Warning level for the message.
@param routine Name of the routine printing the message.
@param message Warning message.
*/
public static void printWarning( int level, String routine, String message )
{	printWarning ( level, null, routine, message );
}

/**
Print a warning message to the registered output receivers.
@param level Warning level for the message.  The level will be printed with
the message if the ShowMessageLevel property is "true".
@param tag A tag to be printed with the message.  The tag will be printed with
the message if the ShowMessageTag property is "true".
@param routine Name of the routine printing the message.  If blank the routine will not be printed.
@param message Warning message.
*/
public static void printWarning ( int level, String tag, String routine, String message )
{	impl.printWarning(level,tag,routine,message);
}

/**
Print a stack trace as if a warning message.  Output will only be to the log file, if open.
@param level Warning level for the message.
@param routine Name of the routine printing the message.
@param e Throwable (e.g. Error, Exception) for which to print a stack trace.
*/
public static void printWarning ( int level, String routine, Throwable e )
{	
    impl.printWarning(level,routine,e);
}

/**
Removes a listener from the Vector of listeners that are set to listen to 
actions from the MessageLogJFrame.
@param listener the listener to remove from the Vector of MessageLogListeners.
*/
public static void removeMessageLogListener(MessageLogListener listener) {
	impl.removeMessageLogListener(listener);
}

/**
Closes and opens the log file, so old data will be overwritten next time something is logged.
*/
public static void restartLogFile() 
throws Exception {
	impl.closeLogFile();
	impl.openLogFile(getLogFile());
}

/**
Set the output behavior flags (e.g., SHOW_MESSAGE_LEVEL) as a bit mask.
Currently this sets the given bit (but does not unset the others).
<b>The handling of these flags needs more work (the defaults are usually OK).</b>
See setProp() for additional properties used to control message behavior.
@param flag Bit mask for flag to set.
*/
public static void setBehaviorFlag( int flag )
{	
    impl.setBehaviorFlag(flag);
}

/**
Set the flag indicating whether debug is on or off.  This is NOT called by the
setDebugLevel method if the debug level is greater than zero for any output
receiver.  If debug is turned off, then debug messages that check this flag will run much faster.
@param flag true if debug should be turned on, false if not.
*/
public static void setDebug ( boolean flag )
{	impl.setDebug(flag);
}

/**
Set the debug level for an output receiver. If the level is > 0, do not set
debugging to on.  The debug levels are independent of whether debug is actually on or off.
@param i Output receiver number (the *_OUTPUT values).
@param level Debug level for the output receiver.
TODO JAVADOC: see RTi.Util.Message.Message.setDebug
*/
public static void setDebugLevel ( int i, int level )
{	impl.setDebugLevel(i,level);
}

/**
Set the debug level by parsing the levels from a string like "#,#", "#", ",#",
etc.  The TERM_OUTPUT and LOG_OUTPUT levels are set.
@param debug_level Debug level as a string.
*/
public static void setDebugLevel ( String debug_level )
{	impl.setDebugLevel(debug_level);
}

/**
Set the log file name for the log file.
@param logfile Name of log file.
*/
public static void setLogFile ( String logfile )
{	impl.setLogFile(logfile);
}

/**
Set the output file for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param output_stream Output PrintStream for the output receiver (usually a log
file).  The default is no log file.
*/
public static void setOutputFile ( int i, PrintStream output_stream )
{	impl.setOutputFile(i,output_stream);
}

/**
Set the output file for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param output_stream Output PrintWriter for the output receiver (usually a log
file).  The default is no log file.
*/
public static void setOutputFile ( int i, PrintWriter output_stream )
{	impl.setOutputFile(i,output_stream);
}

/**
Set the function to call for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param obj Object containing the function to call.
@param function_name Name of function to call to receive message.  The function
must take an integer (message level), a String (the routine name), and a
second String (the message).
*/
public static void setOutputFunction ( int i, Object obj, String function_name )
{	impl.setOutputFunction(i,obj,function_name);
}

/**
Set the prefix to use in front of all messages (the default is nothing).
This can be used, for example, to format messages for HTML.
@param prefix Prefix string to use in front of all messages.
*/
public static void setPrefix ( String prefix )
{	
    impl.setPrefix(prefix);
}

/**
Set a property used to control message behavior.  These properties currently are
used mainly to control the warning dialog behavior.
The properties should be set by high-level code
to control how the application behaves.  For example, if there is potential for
many warnings to be generated, the warning dialog can be configured to be turned
off during stretches of processing and then turned on again during stretches
where user input is required.  Use the getPropValue() method to determine a
property value (e.g., to determine if the use has indicated that further warning
messages should not be shown.  The following properties are recognized (not all
are normally set by application code - some are typically only set internally
and are then queried by application code - see comments):

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>	<td><b>Description</b></td>
<td><b>Default</b></td>
</tr>

<tr>
<td>ShowMessageLevel</td>
<td>Indicate whether message levels should be shown for messages.  The default
is to NOT show message levels.  If true, the normal messages will be modified
to include the level in [brackets], as follows (the optional Tag and Routine are
shown for illustration):
<pre>
Warning[Level]<Tag>(Routine)...
</pre>
</td>
<td>false - do not show message levels in output.</td>
</tr>

<tr>
<td>ShowMessageTag</td>
<td>Indicate whether message tags should be shown for messages.  The default
is to NOT show message tags.  Message tags are used in overloaded methods that
pass the tag to the message.  For example, this can be used to help link the
messages to some scope/content in an application.  If true, the normal messages
will be modified to include the tag in <brackets>, as follows (the optional Tag
and Routine are shown for illustration):
<pre>
Warning[Level]<Tag>(Routine)...
</pre>
</td>
<td>false - do not show message tags in output.</td>
</tr>

<tr>
<td>ShowWarningDialog</td>
<td>Set to false to turn off the Warning dialog.  Use selectively to
turn off warnings in cases where multiple warnings may occur.  This property
is normally set to true internally by the MessageJDialog when the user indicates
via the dialog that no more warnings should be shown (see the
"WarningDialogOKNoMoreButton" property).</td>
<td>true - always show warning dialogs (at level 1).</td>
</tr>

<tr>
<td>WarningDialogOKNoMoreButton</td>
<td>Set to true to turn cause the warning dialog to display a button like
"OK - Do Not Show More Warnings".  Pressing this button will cause the
"ShowWarningDialog" property to be set to false.  See also the
"WarningDialogOKNoMoreButtonLabel" property.
</td>
<td>false</td>
</tr>

<tr>
<td>WarningDialogOKNoMoreButtonLabel</td>
<td>Set the label for the button associated with the
"WarningDialogOKNoMoreButton" property.
</td>
<td>When button is active: "OK - Do Not Show More Warnings".</td>
</tr>

<tr>
<td>WarningDialogViewLogButton</td>
<td>Indicate with true or false whether a button should be enabled to view the log file.
</td>
<td>false</td>
</tr>
</table>
*/
public static void setPropValue ( String prop )
{	impl.setPropValue(prop);
}

/**
Set the status level for the application.  Only messages with levels less than
or equal to the set level will be printed.
@param i Output receiver (the *_OUTPUT values).
@param level Status message level for the application.
*/
public static void setStatusLevel ( int i, int level )
{	
    impl.setStatusLevel(i,level);
}

/**
Set the suffix to use at the end of all messages (the default is nothing).
This can be used, for example, to format messages for HTML.
@param suffix Suffix string to use behind all messages.
*/
public static void setSuffix ( String suffix )
{	
    impl.setSuffix(suffix);
}

/**
Set the top-level frame for the application that uses messages.  This allows
the modal warning dialog to be created.
@param f Top-level JFrame for application.
*/
public static void setTopLevel ( JFrame f )
{	
    impl.setTopLevel(f);
}

/**
Set the warning level for the application.  Only messages with levels less than
or equal to the set level will be printed.
@param i Output receiver (the *_OUTPUT values).
@param level Warning message level for the application.
*/
public static void setWarningLevel( int i, int level )
{	
    impl.setWarningLevel(i,level);
}

}