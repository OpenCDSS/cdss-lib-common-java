package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;

/**
 * Provides convenience methods for working with CommandStatus.
 * 
 * @author dre
 */
public class CommandStatusUtil
{
/**
Adds a command to the HTML accumulating in the HTMLStatusAssembler.
@param command
@param assembler
@return return 0 if no warnings or failures found, otherwise 1
*/
private static int addCommandHTML(Command command, HTMLStatusAssembler assembler)
{
    CommandStatus cs;
    int problemsFound = 0;
    
    if ( command instanceof CommandStatusProvider ) {
        cs = ((CommandStatusProvider)command).getCommandStatus();
        if (isProblematic(cs)) {
            problemsFound = 1;
            // add Command
            assembler.addCommand(command.toString());

            // add Command Summary Table
            addCommandSummary(cs, assembler);

            addCommandDetailTable(assembler, cs);
        }
    }
    else {
        addNotACommandStatusProvider(assembler);
    }

    assembler.endCommand();
    
    return problemsFound;
}
  
/**
Adds Detail table.
@param assembler
@param cs
*/
private static void addCommandDetailTable( HTMLStatusAssembler assembler, CommandStatus cs) 
{
    assembler.startCommandStatusTable();

    if (cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.WARNING
        ||cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.FAILURE) {
        addPhaseHTML(cs, assembler, CommandPhaseType.INITIALIZATION);
    }
    if (cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.WARNING
        ||cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.FAILURE) {
        addPhaseHTML(cs, assembler, CommandPhaseType.DISCOVERY);
    }
    if (cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.WARNING
        ||cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.FAILURE) {
        addPhaseHTML(cs, assembler, CommandPhaseType.RUN);
    }
}
  
/**
Adds command status summary table.
@param cs
@param assembler
*/
private static void addCommandSummary(CommandStatus cs, HTMLStatusAssembler assembler)
{
    assembler.addCommandStatusSummary(
            cs.getCommandStatus(CommandPhaseType.INITIALIZATION),
            cs.getCommandStatus(CommandPhaseType.DISCOVERY),
            cs.getCommandStatus(CommandPhaseType.RUN));
}
  
/**
Adds text indicating no issues found.
@param assembler
*/
private static void addNotACommandStatusProvider(HTMLStatusAssembler assembler)
{
    assembler.addNotACommandStatusProvider();
}
  
/**
Adds html for a command status phase.
@param cs
@param assembler
*/
private static void addPhaseHTML(CommandStatus cs,
    HTMLStatusAssembler assembler, CommandPhaseType commandPhaseType)
{
    CommandStatusType status = cs.getCommandStatus(commandPhaseType);
      
	List v = cs.getCommandLog(commandPhaseType);
	if ( v.size() > 0 ) {
	    int size = v.size();
	    for ( int i = 0; i < size; i++ ) {
	        CommandLogRecord clr = (CommandLogRecord)v.get(i);
	        assembler.addPhase(commandPhaseType.toString(),
	                    status.toString(),
	                    getStatusColor(status),
	                    clr.getProblem(),
	                    clr.getRecommendation());
	    }
	}
}
  
/**
Returns the command status log records ready for display as HTML.
@param csp command status provider
@return concatenated log records as text
*/
//  public static String getCommandLogHTML(CommandStatus status)
//    {
//	  if ( status == null ) {
//		  return "<HTML>Status is not available.</HTML>";
//	  }
//	  else {
//		  //FIXME SAM 2007-08-15 Need to figure out where the following lives
//		  //return HTMLUtil.text2html(status.getCommandLogText());
//		  return "<HTML><pre>" + getCommandLogText(status) + "</pre></HTML>";
//	  }
//    }
//    
/**
Returns the command log records ready for display as HTML.
@param csp command status provider
@return concatenated log records as HTML
*/
//  public static String getCommandLogHTML(CommandStatusProvider csp)
//    {
//      return "<html><font bgcolor=red> Stati </font></html>";
////	  if ( csp == null ) {
////	    return "DeanDoIt";
////	//	  return getCommandLogTextHTML ( (CommandStatus)null );
////	  }
////	  else {
////		  return getCommandLogText(csp.getCommandStatus());
////	  }
//      
//    }
  
/**
Append log records from a list of commands to a status.  For example, this is used
when running a list of commands with a "runner" command to get a full list of logs.
@param status a CommandStatus instance to which log records should be appended.
@param commandList a list of CommandStatusProviders (such as Command instances) that
have log records to be appended to the first parameter.
*/
public static void appendLogRecords ( CommandStatus status, List<CommandStatusProvider> commandList )
{
      if ( status == null ) {
          return;
      }
      if ( commandList == null ) {
          return;
      }
      // Loop through the commands
      int size = commandList.size();
      CommandStatusProvider csp;
      for ( int i = 0; i < size; i++ ) {
          // Transfer the command log records to the status...
          csp = commandList.get(i);
          CommandStatus status2 = csp.getCommandStatus();
          // Get the logs for the initialization...
          List logs = status2.getCommandLog(CommandPhaseType.INITIALIZATION);
          for ( int il = 0; il < logs.size(); il++ ) {
              status.addToLog(CommandPhaseType.INITIALIZATION, (CommandLogRecord)logs.get(il) );
          }
          logs = status2.getCommandLog(CommandPhaseType.DISCOVERY);
          for ( int il = 0; il < logs.size(); il++ ) {
              status.addToLog(CommandPhaseType.DISCOVERY, (CommandLogRecord)logs.get(il) );
          }
          logs = status2.getCommandLog(CommandPhaseType.RUN);
          for ( int il = 0; il < logs.size(); il++ ) {
              status.addToLog(CommandPhaseType.RUN, (CommandLogRecord)logs.get(il) );
          }
      }
}
  
/**
@param csp CommandStatusProvider (i.e., a command for which to get the command log).
@return the command log as HTML.
*/
public static String getCommandLogHTML(CommandStatusProvider csp)
{
    String toolTip = getHTMLCommandStatus(csp.getCommandStatus());
    return toolTip;
}

/**
Get the display name (problem type) for a CommandLogRecord class.  This is used, for example when displaying the
full list of problems.
@param log CommandLogRecord instance.
*/
public static String getCommandLogRecordDisplayName ( CommandLogRecord log )
{
    // Have to check the class name because instanceof will return true for anything derived from
    // CommandLogRecord
    String className = log.getClass().getSimpleName();
    if ( className.equals("CommandLogRecord") ) {
        // Using base class for log record class - general command run-time error but indicate whether
        // the problem was generated in initialization, discover, or run
        // TODO SAM 2009-03-06 Need to figure out whether phases are reflected in the string
        return "CommandRun";
    }
    else {
        // The class name must be specific and is used for output.
        return className;
    }
}

  /**
   * Returns the command log records ready for display as text, suitable
   * for general output.  Rudimentary formatting is done to make the output readable,
   * but see the HTML output.
   * @param csp command status provider
   * 
   * @return concatenated log records as text
   */
  public static String getCommandLogText ( CommandStatus cs )
    {
      if ( cs == null ) {
    	  return "Unable to determine command status.";
      }
      
      String nl = System.getProperty ( "line.separator");

      String thick_line = "=================================================================";
      String thin_line =  "-----------------------------------------------------------------";
      String dash_line =  ".................................................................";
      StringBuffer b = new StringBuffer();
      b.append ( thick_line );
      // TODO SAM 2007-09-06 Need to figure out how to list command string
      //b.append( nl + "Command:  " + "would be nice to have here.")
      //b.append( nl + thick_line );
      b.append ( nl + "Initialization status: " + cs.getCommandStatus(CommandPhaseType.INITIALIZATION));
      List v = cs.getCommandLog(CommandPhaseType.INITIALIZATION);
      if ( v.size() > 0 ){
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Initialization log:");
          int size = v.size();
          for ( int i = 0; i < size; i++ ) {
        	  	if ( i > 0 ) {
        	  		b.append ( nl + dash_line );
        	  	}
                b.append ( nl + v.get(i).toString() );
          }
      }
      b.append ( nl + thick_line );
      b.append ( nl + "Discovery status: " + cs.getCommandStatus(CommandPhaseType.DISCOVERY));
      v = cs.getCommandLog(CommandPhaseType.DISCOVERY);
      if ( v.size() > 0 ) {
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Discovery log:");
          int size = v.size();
          for ( int i = 0; i < size; i++ )
              {
      	  		if ( i > 0 ) {
      	  			b.append ( nl + dash_line );
      	  		}
                b.append ( nl + v.get(i).toString() );
              }
      }
      b.append ( nl + thick_line );
      b.append ( nl + "Run status: " + cs.getCommandStatus(CommandPhaseType.RUN));
      v = cs.getCommandLog(CommandPhaseType.RUN);
      if ( v.size() > 0 ) {
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Run log:");
    	  int size = v.size();
          for ( int i = 0; i < size; i++ )
              {
        	  	if ( i > 0 ) {
        		  b.append ( nl + dash_line );
    	  		}
                b.append ( nl + v.get(i).toString() );
              }
      }

      return b.toString();

    }
  
  /**
   * Returns the command log records ready for display as text
   * @param csp command status provider
   * 
   * @return concatenated log records as text
   */
//  public static String getCommandLogText(CommandStatusProvider csp)
//  {
//	  if ( csp == null ) {
//		  return getCommandLogText ( (CommandStatus)null );
//	  }
//	  else {
//		  return getCommandLogText ( csp.getCommandStatus() );
//	  }
//  }

  /**
   * Returns the highest status severity of all phases, to indicate the most
   * severe problem with a command.
   * @param cs command status
   * @see CommandStatusType
   * @return The highest severity status from a command.
   * @see CommandStatusType
   */
  public static CommandStatusType getHighestSeverity( CommandStatus cs )
  {
	  CommandStatusType status = CommandStatusType.UNKNOWN;
	  if ( cs == null ) {
		  return status;	// Default UNKNOWN
	  }
      
          CommandStatusType phaseStatus = cs.getCommandStatus(CommandPhaseType.INITIALIZATION);
          if ( phaseStatus.getSeverity() > status.getSeverity())
            {
              status = phaseStatus;
            }
          phaseStatus = cs.getCommandStatus(CommandPhaseType.DISCOVERY);
          if ( phaseStatus.getSeverity() > status.getSeverity())
            {
              status = phaseStatus;
            }
          phaseStatus = cs.getCommandStatus(CommandPhaseType.RUN);
          if ( phaseStatus.getSeverity()> status.getSeverity())
            {
              status = phaseStatus;
            }
 
      return status;
  }
 
  /**
   * Returns the highest status severity of all phases, to indicate the most
   * severe problem with a command.
   * 
   * @param csp command status provider
   * @see CommandStatusType
   * @return The highest severity status from a command.
   * @see CommandStatusType
   */
  public static CommandStatusType getHighestSeverity(CommandStatusProvider csp)
    { 
      if ( csp == null ) {
    	return CommandStatusType.UNKNOWN;
      }
    
      return getHighestSeverity ( csp.getCommandStatus() );
    }

  /**
   * Returns the Command Status for the specified commands as HTML.
   * 
   * @param commands
   * @return HTML status report
   */
  public static String getHTMLStatusReport(List commands)
  {
    HTMLStatusAssembler assembler = new HTMLStatusAssembler();
    Command command;
    int count = 0;
    
    for ( int i = 0; i < commands.size(); i++ ) 
      {
        command = (Command)commands.get(i);
        count = count + addCommandHTML(command, assembler);
       }
    if (count == 0)
      {
        assembler.addNoProblems();
      }
    
    return assembler.getHTML();
  }

  /**
   * Returns status for a single command in HTML.
   * <p>
   * Useful for providing tooltip
   * @param css
   * @return
   */
public static String getHTMLCommandStatus(CommandStatus css)
{
  HTMLStatusAssembler assembler = new HTMLStatusAssembler();
  addCommandDetailTable(assembler, css);
  return assembler.getHTML();
}

/**
Given a list of CommandStatusProvider, return a list of all the log records.  This is useful for
providing the full list for reporting.
@param commandStatusProviderList List of CommandStatusProvider (e.g., list of commands from a command
processor).
@param commandPhase command phase to return or null to return all (currently null does not return anything).
@return the list of log records for the given command phase
*/
public static List getLogRecordList ( List commandStatusProviderList, CommandPhaseType commandPhase )
{	//String routine = "CommandStatusUtil.getLogRecordList";
	List logRecordList = new Vector(); // Returned list of log records
	int size = 0;
	if ( commandStatusProviderList != null ) {
		size = commandStatusProviderList.size();
	}
	CommandStatusProvider csp = null; // For example a Command
	CommandStatus status = null; // Status for the command
	List statusLogRecordList = null; // List of status log records for the command
	CommandLogRecord logRecord = null; // Single log record
	for ( int iCommandStatusProvider = 0; iCommandStatusProvider < size; iCommandStatusProvider++ ) {
		// Get the information from a single command status provider
		csp = (CommandStatusProvider)commandStatusProviderList.get(iCommandStatusProvider);
		//Message.printStatus(2, routine, "Getting log records for " + csp );
		status = csp.getCommandStatus();
		statusLogRecordList = status.getCommandLog(commandPhase);
		int statusLogRecordListSize = statusLogRecordList.size();
		//Message.printStatus(2, routine, "Command " + iCommandStatusProvider + " has " + statusLogRecordListSize +
		//    " log records." );
		for ( int iLogRecord = 0; iLogRecord < statusLogRecordListSize; iLogRecord++ ) {
			// Append to full list of log records
			// Also set the command instance in the log record here since it was not
			// included in the original design
			logRecord = (CommandLogRecord)statusLogRecordList.get(iLogRecord);
			logRecord.setCommandStatusProvider(csp);
			logRecordList.add(logRecord);
		}
	}
	return logRecordList;
}

/**
Determine the number of log records (or commands) for the requested severity.
@param command a command that has log records to be appended to the first parameter.  It must implement
CommandStatusProvider.
@param severity the requested severity for a count.
@return the number of log records for the requested severity.
@param countCommands if false, return the total count of log records for a severity, and if true return the total
number of commands that have at least one log record with the indicated severity.
*/
public static int getSeverityCount ( Command command, CommandStatusType severity, boolean countCommands )
{
    List list = new Vector(1);
    list.add(command);
    return getSeverityCount ( list, severity, countCommands );
}

/**
Determine the number of log records (or commands) for the requested severity.
@param commandList a list of CommandStatusProviders (such as Command instances) to be examined.
@param severity the requested severity for a count.
@return the number of log records for the requested severity.
@param countCommands if false, return the total count of log records for a severity, and if true return the total
number of commands that have at least one log record with the indicated severity.
*/
public static int getSeverityCount ( List<Command> commandList, CommandStatusType severity, boolean countCommands )
{
      if ( commandList == null ) {
          return 0;
      }
      // Loop through the commands
      int size = commandList.size();
      CommandStatusProvider csp;
      Command command;
      int severityCount = 0;
      int severityCountCommandCount = 0;
      for ( int i = 0; i < size; i++ ) {
          // Transfer the command log records to the status...
          command = commandList.get(i);
          if ( command instanceof CommandStatusProvider ) {
              csp = (CommandStatusProvider)command;
          }
          else {
              continue;
          }
          CommandStatus status = csp.getCommandStatus();
          // Get the logs for the initialization...
          boolean commandSeverityMatched = false; // Did command have log with matching severity?
          for ( int iphase = 0; iphase <= 2; iphase++ ) {
              List<CommandLogRecord> logs = null;
              if ( iphase == 0 ) {
                  logs = status.getCommandLog(CommandPhaseType.INITIALIZATION);
              }
              else if ( iphase == 1 ) {
                  logs = status.getCommandLog(CommandPhaseType.DISCOVERY);
              }
              else if ( iphase == 2 ) {
                  logs = status.getCommandLog(CommandPhaseType.RUN);
              }
              CommandLogRecord log;
              int logsSize = logs.size();
              for ( int ilog = 0; ilog < logsSize; ilog++ ) {
                  log = logs.get(ilog);
                  if ( log.getSeverity() == severity ) {
                      commandSeverityMatched = true;
                      ++severityCount;
                  }
              }
          }
          if ( commandSeverityMatched ) {
              ++severityCountCommandCount;
          }
      }
      if ( countCommands ) {
          return severityCountCommandCount;
      }
      else {
          return severityCount;
      }
}

/**
* Returns a color associated with specified status type, for background color.
* 
* @param type command status type
* @return color associated with type
*/
public static String getStatusColor(CommandStatusType type)
{
    if (type == CommandStatusType.SUCCESS) {
        return "green";
    }
    else if (type == CommandStatusType.WARNING) {
        return "yellow";
    }
    else if (type == CommandStatusType.FAILURE) {
        return "red";
    }
    else {
        return "white";
    }
}
  
  /**
   * Returns whether command status has warnings/failures.
   * @param cs
   * @return True if command has warnings/failures
   */
  private static boolean isProblematic(CommandStatus cs)
  {
    boolean ret = false;
    if (getHighestSeverity(cs).greaterThan(CommandStatusType.SUCCESS))
      {
        ret = true;
      }
    return ret;
  }
}