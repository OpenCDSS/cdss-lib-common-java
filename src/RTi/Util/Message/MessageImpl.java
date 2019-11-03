// MessageImpl - implementation of logging using legacy Message

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
// RTi.Util.Message.MessageImpl - implementation of logging using legacy Message
//------------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2007-02-27  Ian Schneider, RTi   Initial version, to allow legacy Message
//                                  class method to be called but also allow the
//                                  new MessageLoggingImpl class to be used,
//                                  resulting in Java logging to be used.
// 2007-05-11  Steven A. Malers,    Minor cleanup based on Eclipse feedback, and
//                                  add Javadoc for class.
//------------------------------------------------------------------------------

package RTi.Util.Message;


import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;

/**
This class is an implementation of the Message design for logging.  This
implementation matches the legacy implementation.  Use the MessageLoggingImpl
implementation to use the Java logging approach.
*/
class MessageImpl
{

/**
Maximum number of output files to receive Message output.
*/
protected int MAX_FILES = 5;

/**
Show the message tags.  This is only used internally since properties are being
phased in to set behavior. 
*/
protected int __SHOW_MESSAGE_TAG = 0x10;

/**
Indicates if the Message class has been initialized.
*/
protected boolean _initialized = false;
/**
JFrame when a GUI application, to allow message dialogs to be on top.
*/
protected JFrame _top_level = null;
/**
Controls the appearance of messages.
*/
protected int _flag = 0;
/**
Debug levels for the different output streams.
*/
protected int [] _debug_level;
/**
Status levels for the different output streams.
*/
protected int [] _status_level;
/**
Warning levels for the different output streams.
*/
protected int [] _warning_level;
/**
Methods to receive output for the different streams.
*/
protected Method [] _method;
/**
Indicates if any methods (functions) have been registered to receive output.
*/
protected boolean _method_registered;
/**
Associated with _method.
*/
protected Object [] _object;
/**
Newline character to use for output.
*/
protected String _newline;
/**
Prefix to use for messages.
*/
protected String _prefix;
/**
Suffix to use for messages.
*/
protected String _suffix;
/**
Output streams for file output.
*/
protected PrintWriter [] _out_stream;
/**
Name of log file (perhaps at some point track names of all output receivers?).
*/
protected String _logfile;
/**
Properties to control display of messages, especially warnings.
*/
protected PropList _props = new PropList ("Message.props");
/**
Use to increase performance rather than hit proplist.
*/
protected boolean _show_warning_dialog;

protected List<MessageLogListener> _messageLogListeners;

/**
Adds a listener to the list of listeners that can respond to actions from
the MessageLogJFrame.  The listeners are updated by the MessageLogJFrame 
every time its processLogFile() method is called and a log file is read and 
displayed.  Any listeners added after a log file is read will not receive
commands from the MessageLogJFrame until it reads the log file again.
@param listener the listener to add.
*/
protected void addMessageLogListener(MessageLogListener listener) {
	if (!_initialized) {
		initialize();
	}
	_messageLogListeners.add(listener);
}

/**
Flush and close the log file associated with Message.LOG_OUTPUT, if it has been opened.
*/
protected void closeLogFile ()
{	if ( _out_stream[Message.LOG_OUTPUT] != null ) {
		_out_stream[Message.LOG_OUTPUT].flush();
		_out_stream[Message.LOG_OUTPUT].close();
		_out_stream[Message.LOG_OUTPUT] = null;
	}
}

/**
Flush the output buffers.  It does not appear that this method has any effect on some systems.
@param flag Currently unused.
*/
protected void flushOutputFiles ( int flag )
{	if( !_initialized ) {
		initialize();
	}
	for ( int i = 0; i < MAX_FILES; i++ ) {
		if ( _out_stream[i] != null ) {
			_out_stream[i].flush ();
		}
	}
}

/**
Return the debug level for an output stream.
@return The debug level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
protected int getDebugLevel ( int i )
{	if( !_initialized ){
		initialize();
	}
	return _debug_level[i];
}

/**
Return the name of the log file.
@return The name of the log file.
*/
protected String getLogFile ()
{	return _logfile;
}

/**
Returns the list of listeners that are set to respond to actions from the
MessageLogJFrame.  The returned Vector will never be null.
@return the list of listeners for the MessageLogJFrame.
*/
protected List<MessageLogListener> getMessageLogListeners() {
	if (!_initialized) {
		initialize();
	}

	return _messageLogListeners;
}

/**
Return the value of a Message property.  See setPropValue() for a description of valid properties.
@return the property value or null if not defined.
*/
protected String getPropValue ( String key )
{	if ( !_initialized ) {
		initialize();
	}
	return _props.getValue ( key );
}

/**
Return the status level for an output stream.
@return The status level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
protected int getStatusLevel ( int i )
{	if( !_initialized ){
		initialize();
	}
	return _status_level[i];
}

/**
Return the warning level for an output stream.
@return The warning level for an output stream number (specified by a *_OUTPUT value).
@param i The output stream number.
*/
protected int getWarningLevel ( int i )
{	if( !_initialized ){
		initialize();
	}
	return _warning_level[i];
}

/**
Initialize global data.
*/
protected void initialize()
{	if( _initialized ) {
		return;
	}
	// For now we want to show the routine and flush the output.  For
	// performance, we will likely turn flushing off at some point...
	_debug_level = new int[MAX_FILES];
	_flag = Message.GUI_FOR_WARNING_1 | Message.SHOW_ROUTINE | Message.FLUSH_OUTPUT;
	_logfile = "";
	_method = new Method[MAX_FILES];
	_method_registered = false;
	_newline = System.getProperty( "line.separator" );
	_object = new Object[MAX_FILES];
	_out_stream = new PrintWriter[MAX_FILES];
	_prefix = "";
	_show_warning_dialog = true;
	_status_level = new int[MAX_FILES];
	_suffix = "";
	_warning_level = new int[MAX_FILES];
	_messageLogListeners = new Vector<MessageLogListener>();

	initStreams();

	_initialized = true;

	// Default values for properties (set after flag is set to avoid
	// recursion - because if debug is turned on the following may call
	// printDebug() which will call this initialize() method if _initialized
	// is false).  Setting these values here assures that null properties
	// will not happen in later code.

	_props.set ( "ShowWarningDialog=true" );
	_props.set ( "WarningDialogOKNoMoreButton=false" );
	_props.set ( "WarningDialogOKNoMoreButtonLabel=OK - Do Not Show More Warnings" );
	_props.set ( "ShowMessageLevel=false" );
	_props.set ( "ShowMessageTag=false" );
}

protected void initStreams() {
    for( int i=0; i<MAX_FILES; i++ ){
		_out_stream[i] = null;
		_method[i] = null;
		_object[i] = null;
		_debug_level[i] = 0;
		_status_level[i] = 1; // Default is some status
		_warning_level[i] = 1; // Default is some warning
	}

	// Set the output stream for terminal so that we can debug initial
	// output.  Don't call setOutputFile or we will recurse...

    // TODO smalers 2019-10-20 switch to standard error for console logging to prevent
    // logging messages from being confused with analysis output printed to standard output.
	//_out_stream[Message.TERM_OUTPUT] = new PrintWriter ( System.out, true );
	_out_stream[Message.TERM_OUTPUT] = new PrintWriter ( System.err, true );
}

/**
Open the log file.
Because no log file is specified, the name of the log file will default to
the program name and the extension ".log" (set with IOUtil.setProgramName).
@see RTi.Util.IO.IOUtil#setProgramName
@return The PrintWriter corresponding to the log file.
@exception IOException if there is an error opening the log file.
*/
protected PrintWriter openLogFile ()
throws IOException
{	String message = null, routine = "Message.openLogFile";

	if( !_initialized ){
		initialize();
	}
	String program_name = IOUtil.getProgramName();
	if ( program_name == null ) {
		// No program name set...
		message = "No program name set.  Cannot use default log file name.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	if ( program_name.length() == 0 ) {
		// No program name set...
		message = "Program name is zero-length.  Cannot use default log file name.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	String logfile = program_name + ".log";
	// The following will throw an IOException if there is an error...
	return openLogFile ( logfile );
}

/**
Open the log file using the specified name.
@return The PrintWriter corresponding to the log file.
@exception IOException if there is an error opening the log file.
@param logfile Name of log file to open.
*/
protected PrintWriter openLogFile ( String logfile )
throws IOException
{	if( !_initialized ){
		initialize();
	}
	String	routine = "Message.openLogFile";
	String	message = null;

	if ( logfile == null ) {
		message = "Null log file.  Not opening log file.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	if ( logfile.length() <= 0 ) {
		message = "Zero-length log file.  Not opening log file.";
		Message.printWarning ( 2, routine, message );
		throw new IOException ( message );
	}
	PrintWriter ofp = null;
	try {
		ofp = new PrintWriter( new FileWriter(logfile));
		Message.setOutputFile( Message.LOG_OUTPUT, ofp);
	}
	catch( IOException e ){
		message = "Unable to open log file \"" + logfile + "\"";
		Message.printWarning ( 2, routine, message );
		if ( Message.isDebugOn ) {
			// Need to get the stack trace because without the log
			// it will be difficult to troubleshoot other issues.
			e.printStackTrace ( _out_stream[Message.TERM_OUTPUT] );
		}
		throw new IOException ( message );
	}

	// If we get here, we opened the log file...

	Message.printStatus ( 1, routine, "Opened log file \"" + logfile +
	"\".  Previous messages not in file." );

	setLogFile ( logfile );
	Message.setOutputFile ( Message.LOG_OUTPUT, ofp );

	// Write the log file information...

	ofp.println ( "#" );
	ofp.println ( "# " + logfile + " - " + IOUtil.getProgramName() + " log file" );
	ofp.println ( "#" );
	IOUtil.printCreatorHeader ( ofp, "#", 80, 0 );
	return ofp;
}

/**
Opens a new file at the specified location.
@param path the full path to the new log file to open.
*/
protected void openNewLogFile(String path) 
throws Exception {
	Message.setLogFile(path);
	Message.openLogFile(path);
}

/**
Print a debug message to the registered output receivers.
@param level Debug level for the message.
@param routine Name of the routine printing the message.
@param message Debug message.
*/
protected void printDebug( int level, String routine, String message )
{	if( !_initialized ) {
		initialize();
	}
	String dlstring = null;
	if( (_flag & Message.SHOW_MESSAGE_LEVEL) != 0 ){
		dlstring = "[" + level + "]";
	}
	else {
		dlstring = "";
	}
	String routine_string = null;
	if ( routine != null ) {
		if( (_flag & Message.SHOW_ROUTINE) != 0 ){
			if ( routine.isEmpty() ) {
				routine_string = "";
			}
			else {
				routine_string = "(" + routine + ")";
			}
		}
		else {
			routine_string = "";
		}
	}
	else {
		routine_string = "";
	}
	
	Object [] arg_list = null;

	// Format the final string...

	String message2 = _prefix + "Debug" + dlstring + routine_string + ": " + message + _suffix;

	// Only do the following if methods are registered...
	if ( _method_registered ) {
		arg_list = new Object[3];
		arg_list[0]	= new Integer( level );
		arg_list[1]	= routine;
		arg_list[2]	= message2;
	}

	for( int i=0; i<MAX_FILES; i++ ){
		if( _out_stream[i] != null && level <= _debug_level[i] ){
			_out_stream[i].print( message2 + _newline );
			if ( (_flag & Message.FLUSH_OUTPUT) != 0 ) {
				_out_stream[i].flush();
			}
		}

		if ( (_method[i] != null) && (_object[i] != null) && (level <= _debug_level[i]) ){
			try {
				_method[i].invoke( _object[i], arg_list );
			}
			catch( Exception e ){
				printDebug ( level, routine, e );
			}
		}
	}
	dlstring = null;
	routine_string = null;
	message2 = null;
	if ( _method_registered ) {
		arg_list[0] = null;
		arg_list[1] = null;
		arg_list[2] = null;
		arg_list = null;
	}
}

/**
Print a stack trace as if debug message.  Currently, this will print to the
log file if it is not null.
@param level Debug level for the message.
@param routine Name of the routine printing the message.
@param e Throwable (e.g., Error, Exception) for which to print a stack trace.
*/
protected void printDebug ( int level, String routine, Throwable e )
{	if( !_initialized ) {
		initialize();
	}
	printDebug ( level, routine, "Exception stack trace follows (see log file)..." );
	if ( e != null ) {
		printDebug ( level, routine, e.getMessage() );
		if( _out_stream[Message.LOG_OUTPUT] != null ) {
			e.printStackTrace ( _out_stream[Message.LOG_OUTPUT] );
		}
	}
	else {
		printDebug ( level, routine, "Null Throwable." );
	}
	printDebug ( level, routine, "... end of exception stack trace." );
}

/**
Print information about the registered message levels using status messages,
using the current message settings.
*/
protected void printMessageLevels ( )
{	if( !_initialized ) {
		initialize();
	}
	printMessageLevels ( true );
}

/**
Print information about the registered message levels using the current message settings.
@param flag If true, print using status messages.  If false, print to the system standard output.
*/
protected void printMessageLevels ( boolean flag )
{	int	i;
	String	string = null;

	if( !_initialized ) {
		initialize();
	}
	if ( flag ) {
		// Print using status messages...
		printStatus ( 1, "", "" );
		printStatus ( 1, "", "Is debug turned on:  " + Message.isDebugOn );
		printStatus ( 1, "", "" );
		printStatus ( 1, "", "-------------------------------------------------");
		printStatus ( 1, "", "File  Debug   Status  Warning  File      Function");
		printStatus ( 1, "", "#     Level   Level   Level    Attached  Attached");
		printStatus ( 1, "", "-------------------------------------------------");
		for ( i = 0; i < MAX_FILES; i++ ) {
			string = 
			StringUtil.formatString(i,"%-3d") + "   " +
			StringUtil.formatString( _debug_level[i], "%-5d") +	"   " +
			StringUtil.formatString ( _status_level[i], "%-5d") + "   " +
			StringUtil.formatString ( _warning_level[i], "%-5d") + "    ";
			// Now indicate whether files are attached for the different message types.
			// Since all the types go to the same file, just output one.
			if ( _out_stream[i] != null ) {
				string = string + "  Y       ";
			}
			else {
				string = string + "          ";
			}
			// Now indicate whether functions are attached for the different message types...
			if ( _method[i] != null ) {
				string = string + "  Y  ";
			}
			else {
				string = string + "     ";
			}
			printStatus ( 1, "", string );
		}
	}
	else {
		// Print to the system...
		PrintWriter fp = new PrintWriter ( System.out, true );
	
		fp.println ();
		fp.println ( "-------------------------------------------------");
		fp.println ( "File  Debug   Status  Warning  File      Function");
		fp.println ( "#     Level   Level   Level    Attached  Attached");
		fp.println ( "-------------------------------------------------");
		for ( i = 0; i < MAX_FILES; i++ ) {
			string =
			StringUtil.formatString(i,"%-3d") + "   " +
			StringUtil.formatString( _debug_level[i], "%-5d") + "   " +
			StringUtil.formatString ( _status_level[i], "%-5d") + "   " +
			StringUtil.formatString ( _warning_level[i], "%-5d") + "    ";
			// Now indicate whether files are attached for the different message types.
			// Since all the types go to the same file, just output one.
			if ( _out_stream[i] != null ) {
				string = string + "  Y  ";
			}
			else {
				string = string + "     ";
			}
			string = string + "     ";
			// Now indicate whether functions are attached for the different message types...
			if ( _method[i] != null ) {
				string = string + "  Y  ";
			}
			else {
				string = string + "     ";
			}
			fp.println ( string );
		}
		fp.close();
		fp.flush();
		fp = null;
	}
	string = null;
}

/**
Print a status message to the registered output receivers.
@param level Status level for the message.
@param routine Name of the routine printing the message.
@param message Status message.
*/
protected void printStatus ( int level, String routine, String message )
{	if( !_initialized ){
		initialize ();
	}

	String slstring = null;
	if( (_flag & Message.SHOW_MESSAGE_LEVEL) != 0 ){
		slstring = "[" + level + "]";
	}
	else {
		slstring = "";
	}
	String routine_string = "";
	if ( routine != null ) {
		// We only show the routine if we have requested it and the
		// status level is greater than 1.
		if( ( _flag & Message.SHOW_ROUTINE) != 0 ){
			if ( level > 1 ) {
				if ( routine.isEmpty() ) {
					routine_string = "";
				}
				else {
					routine_string = "(" + routine + ")";
				}
			}
		}
	}

	Object [] arg_list = null;

	// Format the final string...

	String message2 = _prefix + "Status" + slstring + routine_string + ": " + message + _suffix;

	if ( _method_registered ) {
		arg_list = new Object[3];
		arg_list[0] = new Integer( level );
		arg_list[1] = "Status: ";
		arg_list[2] = message2;
	}

	DateTime now = null;
	if (IOUtil.testing()) {
		now = new DateTime(DateTime.DATE_CURRENT);
		now.setPrecision(DateTime.PRECISION_SECOND);
	}

	for( int i=0; i<MAX_FILES; i++ ){
		if( (_out_stream[i] != null) && (level <= _status_level[i]) ){
			_out_stream[i].print( message2 + _newline );
			_out_stream[i].flush();
		}
		if( (_method[i] != null && _object[i] != null) && (level <= _status_level[i]) ){
			try {
				_method[i].invoke( _object[i], arg_list );
			}
			catch( Exception e ){
				//System.out.println( 
				//"Exception (Message.printStatus): " + e );
			}
		}
	}
	slstring = null;
	routine_string = null;
	message2 = null;
	if ( _method_registered ) {
		arg_list[0] = null;
		arg_list[1] = null;
		arg_list[2] = null;
		arg_list = null;
	}
}

/**
This method calls printWarning but allows the developer to specify a
different _top_level frame from the preset top level frame.  This is useful
when you are printing a warning of level 1 and want to specify which window
that WarningDialog should be associated with, without changing the top
level window that should be typically used.
*/
protected void printWarning ( int level, String routine, String message, JFrame top_level )
{	// Save current global top level JFrame...
	JFrame permanent_top_level = _top_level;

	if ( top_level != null ) {
		_top_level = top_level;
	}
	printWarning ( level, routine, message );

	// Reset back to original...
	_top_level = permanent_top_level;
}

/**
Print a warning message to the registered output receivers.  The overloaded
version is called without the tag.
@param level Warning level for the message.
@param routine Name of the routine printing the message.
@param message Warning message.
*/
protected void printWarning( int level, String routine, String message )
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
protected void printWarning ( int level, String tag, String routine, String message )
{	if ( !_initialized ){
		initialize();
	}

	String wlstring = null;
	if( (_flag & Message.SHOW_MESSAGE_LEVEL) != 0 ){
		wlstring = "[" + level + "]";
	}
	else {
		wlstring = "";
	}

	String tagstring = null;
	if ( ((_flag & __SHOW_MESSAGE_TAG) != 0) && (tag != null) && !tag.equals("")){
		tagstring = "<" + tag + ">";
	}
	else {
		tagstring = "";
	}

	String routine_string = null;
	if ( routine != null ) {
		if( ((_flag & Message.SHOW_ROUTINE) != 0) && (level > 1) ){
			if ( routine.isEmpty() ) {
				routine_string = "";
			}
			else {
				routine_string = "(" + routine + ")";
			}
		}
		else {
			routine_string = "";
		}
	}
	else {
		routine_string = "";
	}

	// Format the final string...

	// TODO SAM 2005-03-11 Not sure of warning level and tag should
	// always be shown or should only be in the log file.  Need to evaluate
	// with StateDMI and TSTool, which are probably the only applications
	// that will initially use the extra information.
	String message2 = _prefix + "Warning" + wlstring + tagstring + routine_string + ": " + message + _suffix;

	Object [] arg_list = null;

	if ( _method_registered ) {
		arg_list = new Object[3];
		arg_list[0] = new Integer( level );
		arg_list[1] = routine;
		arg_list[2] = message2;
	}

	for( int i=0; i<MAX_FILES; i++ ){
		if( _out_stream[i] != null && level <= _warning_level[i] ){
			_out_stream[i].print( message2 + _newline );
			if ( (_flag & Message.FLUSH_OUTPUT) != 0 ) {
				_out_stream[i].flush();
			}
		}
		if( _method[i] != null && _object[i] != null && level <= _warning_level[i] ){
			try {
				_method[i].invoke( _object[i], arg_list );
			}
			catch( Exception e ){
				printWarning(level,routine,e);
			}
		}
	}

	// Now pop up the MessageJDialog if necessary.

	if ( _show_warning_dialog ) {
		if( (level == 1) && ( (_flag & Message.GUI_FOR_WARNING_1) != 0 ) &&
			(_top_level != null ) && !IOUtil.isBatch() ) { // Batch = no GUI
			new MessageJDialog( _top_level, message2 );
		}
	}
	wlstring = null;
	tagstring = null;
	routine_string = null;
	message2 = null;
	if ( _method_registered ) {
		arg_list[0] = null;
		arg_list[1] = null;
		arg_list[2] = null;
		arg_list = null;
	}
}

/**
Print a stack trace as if a warning message.  Output will only be to the log file, if open.
@param level Warning level for the message.
@param routine Name of the routine printing the message.
@param e Throwable (e.g. Error, Exception) for which to print a stack trace.
*/
protected void printWarning ( int level, String routine, Throwable e )
{	if ( !_initialized ){
		initialize();
	}
    e.printStackTrace();

	if (IOUtil.isRunningApplet()) {
		System.err.println("Exception stack trace follows...");
		e.printStackTrace();
		System.err.println("... end of exception stack trace.");
	}
	else {
		printWarning ( level, routine, "Exception stack trace follows (see log file)..." );
		if ( e != null ) {
			printWarning ( level, routine, e.getMessage() );
			if( _out_stream[Message.LOG_OUTPUT] != null ) {
				e.printStackTrace ( _out_stream[Message.LOG_OUTPUT] );
			}
		}
		printWarning ( level, routine, "... end of exception stack trace." );
	}
}

/**
Removes a listener from the Vector of listeners that are set to listen to 
actions from the MessageLogJFrame.
@param listener the listener to remove from the Vector of MessageLogListeners.
*/
protected void removeMessageLogListener(MessageLogListener listener) {
	if ( !_initialized ) {
		initialize();
	}

	int size = _messageLogListeners.size();
	MessageLogListener temp = null;
	for (int i = (size - 1); i >= 0; i--) {
		temp = (MessageLogListener)_messageLogListeners.get(i);

		if (temp == listener) {
			_messageLogListeners.remove(i);
		}
	}
}

/**
Closes and opens the log file, so old data will be overwritten next time something is logged.
*/
protected void restartLogFile() 
throws Exception {
	Message.closeLogFile();
	Message.openLogFile(getLogFile());
}

/**
Set the output behavior flags (e.g., SHOW_MESSAGE_LEVEL) as a bit mask.
Currently this sets the given bit (but does not unset the others).
<b>The handling of these flags needs more work (the defaults are usually OK).</b>
See setProp() for additional properties used to control message behavior.
@param flag Bit mask for flag to set.
*/
protected void setBehaviorFlag( int flag )
{	if ( !_initialized ) {
		initialize();
	}
	int current = _flag;
	_flag = current | flag;
}

/**
Set the flag indicating whether debug is on or off.  This is called by the
setDebugLevel method if the debug level is greater than zero for any output
receiver.  If debug is turned off, then debug messages that check this flag will run much faster.
@param flag true if debug should be turned on, false if not.
*/
protected void setDebug ( boolean flag )
{	if( !_initialized ) {
		initialize();
	}
	Message.isDebugOn = flag;
}

/**
Set the debug level for an output receiver. If the level is > 0, do not set
debugging to on.  The debug levels are independent of whether debug is actually on or off.
@param i Output receiver number (the *_OUTPUT values).
@param level Debug level for the output receiver.
TODO JAVADOC: see RTi.Util.Message.Message.setDebug
*/
protected void setDebugLevel ( int i, int level )
{	if( !_initialized ) {
		initialize();
	}
	String	routine = "setDebugLevel";
	if( i >= MAX_FILES ){
		printWarning( 1, routine,
		"Attempting to set level " + i + ". Only " + MAX_FILES + " are available." );
		return;
	}
	_debug_level[i]	= level;
	printStatus ( 1, routine, "Set debug level for " + Message.OUTPUT_NAMES[i] + " to " + level );
	routine = null;
}

/**
Set the debug level by parsing the levels from a string like "#,#", "#", ",#",
etc.  The TERM_OUTPUT and LOG_OUTPUT levels are set.
@param debug_level Debug level as a string.
*/
protected void setDebugLevel ( String debug_level )
{	if ( debug_level == null ) {
		return;
	}
	if ( debug_level.length() == 0 ) {
		return;
	}

	// Split the argument...

	List<String> list = null;
	int nlist = 0;
	if ( debug_level.length() > 0 ) {
		list = StringUtil.breakStringList (debug_level, ",", 0);
		nlist = list.size();
	}
	else {
		nlist = 0;
	}

	int level0, level1;
	if ( nlist == 1 ) {
		//
		// #
		//
		level0 = StringUtil.atoi(list.get(0).toString() );
		level1 = level0;
		setDebugLevel ( Message.TERM_OUTPUT, level0 );
		setDebugLevel ( Message.LOG_OUTPUT, level1 );
	}
	else {
		//
		// ,# or #,#
		//
		String tmp = list.get(0).toString();

		if( tmp.length() > 0 ){
			level0 = StringUtil.atoi ( tmp );
			setDebugLevel ( Message.TERM_OUTPUT, level0 );
		}
		tmp = list.get(1).toString();

		if ( tmp.length() > 0 ) {
			level1 = StringUtil.atoi ( tmp );
			setDebugLevel ( Message.LOG_OUTPUT, level1 );
		}
		tmp = null;
	}
	list = null;
}

/**
Set the log file name for the log file.
@param logfile Name of log file.
*/
protected void setLogFile ( String logfile )
{	if ( logfile != null ) {
		_logfile = new String ( logfile );
	}
}

/**
Set the output file for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param output_stream Output PrintStream for the output receiver (usually a log
file).  The default is no log file.
*/
protected void setOutputFile ( int i, PrintStream output_stream )
{	setOutputFile ( i, new PrintWriter( output_stream, true ) );
}

/**
Set the output file for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param output_stream Output PrintWriter for the output receiver (usually a log
file).  The default is no log file.
*/
protected void setOutputFile ( int i, PrintWriter output_stream )
{	if( !_initialized ) {
		initialize();
	}
	if( i >= MAX_FILES ){
		printWarning( 1, "Message.setOutputFile",
		"Attempting to set file " + i + ". Only " + MAX_FILES + " are available." );
		return;
	}
	_out_stream[i] = output_stream;
}

/**
Set the function to call for an output receiver.
@param i Output receiver (the *_OUTPUT values).
@param obj Object containing the function to call.
@param function_name Name of function to call to receive message.  The function
must take an integer (message level), a String (the routine name), and a second String (the message).
*/
protected void setOutputFunction ( int i, Object obj, String function_name )
{	if( !_initialized ){
		initialize();
	}
	String	routine = "Message.setOutputFunction";
	if( i >= MAX_FILES ){
		printWarning( 1, routine,
		"Attempting to set function " + i + ". Only " + MAX_FILES + " are available." );
		return;
	}

	if ( obj == null ) {
		// Just set it (to turn off stream) and return...
		_object[i] = null;
		_method[i] = null;
		// If all objects and methods are null, set functions to off...
		boolean nonnull_found = false;
		for ( int j = 0; j < MAX_FILES; j++ ) {
			if ( (_object[j] != null) || (_method[j] != null) ) {
				nonnull_found = true;
				break;
			}
		}
		if ( !nonnull_found ) {
			_method_registered = false;
		}
		return;
	}
		
	Class [] args = new Class[3];

	args[0]	= int.class;
	args[1] = String.class;
	args[2] = String.class;

	Method method;
	try {
		method = obj.getClass().getMethod( function_name, args );
	}
	catch( NoSuchMethodException e ){
		Message.printWarning( 2, routine,
		"Error getting \"Method\" for " + function_name + "( int, String, String )" );
		return;
	}
	_object[i] = obj;
	_method[i] = method;
	_method_registered = true;
	routine = null;
}

/**
Set the prefix to use in front of all messages (the default is nothing).
This can be used, for example, to format messages for HTML.
@param prefix Prefix string to use in front of all messages.
*/
protected void setPrefix ( String prefix )
{	if ( !_initialized ){
		initialize();
	}
	if ( prefix != null ){
		_prefix = prefix;
	}
}

/**
Set a property used to control message behavior.  These properties are currently
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
<td>Indicate with true or false whether a button should be enabled to view the
log file.
</td>
<td>false</td>
</tr>
</table>
*/
protected void setPropValue ( String prop )
{	if ( !_initialized ) {
		initialize();
	}
	_props.set ( prop ); // Set the property no matter what it is.
	// Check some properties and set other variables to optimize performance...
	if ( _props.getValue("ShowWarningDialog").equalsIgnoreCase("true") ) {
		_show_warning_dialog = true;
	}
	else {
		_show_warning_dialog = false;
	}
	if ( _props.getValue("ShowMessageLevel").equalsIgnoreCase("true") ) {
		// Turn on the level...
		_flag |= Message.SHOW_MESSAGE_LEVEL;
	}
	else {
		// Turn off the level...
		_flag ^= Message.SHOW_MESSAGE_LEVEL;
	}
	if ( _props.getValue("ShowMessageTag").equalsIgnoreCase("true") ) {
		// Turn on the level...
		_flag |= __SHOW_MESSAGE_TAG;
	}
	else {
		// Turn off the level...
		_flag ^= __SHOW_MESSAGE_TAG;
	}
}

/**
Set the status level for the application.  Only messages with levels less than
or equal to the set level will be printed.
@param i Output receiver (the *_OUTPUT values).
@param level Status message level for the application.
*/
protected void setStatusLevel ( int i, int level )
{	if( !_initialized ){
		initialize();
	}
	String	routine = "setStatusLevel";
	if ( i >= MAX_FILES ){
		printWarning( 1, routine,
		"Attempting to set level " + i + ". Only " + MAX_FILES + " are available." );
		return;
	}
	_status_level[i] = level;
	printStatus ( 1, routine, "Set status level for " + Message.OUTPUT_NAMES[i] + " to " + level );
	routine = null;
}

/**
Set the suffix to use at the end of all messages (the default is nothing).
This can be used, for example, to format messages for HTML.
@param suffix Suffix string to use behind all messages.
*/
protected void setSuffix ( String suffix )
{	if ( !_initialized ){
		initialize();
	}
	if( suffix != null ){
		_suffix = suffix;
	}
}

/**
Set the top-level frame for the application that uses messages.  This allows
the modal warning dialog to be created.
@param f Top-level JFrame for application.
*/
protected void setTopLevel ( JFrame f )
{	if ( !_initialized ) {
		initialize();
	}
	_top_level = f;
}

/**
Set the warning level for the application.  Only messages with levels less than
or equal to the set level will be printed.
@param i Output receiver (the *_OUTPUT values).
@param level Warning message level for the application.
*/
protected void setWarningLevel( int i, int level )
{	if ( !_initialized ){
		initialize();
	}
	String	routine = "setWarningLevel";
	if( i >= MAX_FILES ){
		printWarning( 1, routine,
		"Attempting to set level " + i + ". Only " + MAX_FILES + " are available." );
		return;
	}
	_warning_level[i] = level;
	printStatus ( 1, routine, "Set warning level for " + Message.OUTPUT_NAMES[i] + " to " + level );
	routine = null;
}

}
