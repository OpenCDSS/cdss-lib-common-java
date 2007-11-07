package RTi.Util.IO;

/**
This interface defines behavior for listening to a command processor.
This is implemented, for example, when a GUI is displaying the progress of
a command processor.  Ini practice the display of messages may be more
appropriate for tool-tip popup than a status window, due to flashing.  If
command processing allows pause, then the individual messages will be
important to indicate the current state of processing.
*/
public interface CommandProcessorListener {
	
/**
Indicate that a command has started running.
@param icommand The command index (0+).
@param ncommand The total number of commands to process
@param command The reference to the command that is starting to run,
provided to allow future interaction with the command.
@param percent_complete If >= 0, the value can be used to indicate progress
running a list of commands (not the single command).  If less than zero, then
no estimate is given for the percent complete and calling code can make its
own determination (e.g., ((icommand + 1)/ncommand)*100).
@param message A short message describing the status (e.g., "Running command ..." ).
*/
public void commandStarted ( int icommand, int ncommand, Command command,
		float percent_complete, String message );

/**
Indicate that a command has completed.  The success/failure of the command
is not indicated (see CommandStatusProvider).
@param icommand The command index (0+).
@param ncommand The total number of commands to process
@param command The reference to the command that is starting to run,
provided to allow future interaction with the command.
@param percent_complete If >= 0, the value can be used to indicate progress
running a list of commands (not the single command).  If less than zero, then
no estimate is given for the percent complete and calling code can make its
own determination (e.g., ((icommand + 1)/ncommand)*100).
@param message A short message describing the status (e.g., "Running command ..." ).
*/
public void commandCompleted ( int icommand, int ncommand, Command command,
		float percent_complete, String message );

/**
Indicate that a command has been cancelled.
@param icommand The command index (0+).
@param ncommand The total number of commands to process
@param command The reference to the command that has been cancelled.  If the cancel
occurred in a processor, the command may be the previous or next command.
@param percent_complete If >= 0, the value can be used to indicate progress
running a list of commands (not the single command).  If less than zero, then
no estimate is given for the percent complete and calling code can make its
own determination (e.g., ((icommand + 1)/ncommand)*100).
@param message A short message describing the status (e.g., "Running command ..." ).
*/
public void commandCancelled ( int icommand, int ncommand, Command command,
		float percent_complete, String message );

/**
Indicate the progress that is occurring within a command.  This may be a chained call
from a CommandProcessor that implements CommandListener to listen to a command.  This
level of monitoring is useful if more than one progress indicator is present in an
application UI.
@param istep The number of steps being executed in a command (0+).
@param nstep The total number of steps to process within a command.
@param command The reference to the command that is starting to run,
provided to allow future interaction with the command.
@param percent_complete If >= 0, the value can be used to indicate progress
running a single command (not the single command).  If less than zero, then
no estimate is given for the percent complete and calling code can make its
own determination (e.g., ((istep + 1)/nstep)*100).
@param message A short message describing the status (e.g., "Running command ..." ).
*/
public void commandProgress ( int istep, int nstep, Command command,
		float percent_complete, String message );

}
