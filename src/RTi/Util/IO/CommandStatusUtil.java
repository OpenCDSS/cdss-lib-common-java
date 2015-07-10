package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
//import RTi.Util.Message.Message;

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
        }
        // add Command
        assembler.addCommand(command.toString());

        // add Command Summary Table
        addCommandSummary(cs, assembler);

        addCommandDetailTable(assembler, cs);
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
    int nwarn = getSeverityCount ( cs, CommandStatusType.WARNING );
    int nfail = getSeverityCount ( cs, CommandStatusType.FAILURE );
    assembler.startCommandStatusTable( nwarn, nfail );

    int startCount = 1;
    if (cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.WARNING
        ||cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.FAILURE) {
        int nprinted = addPhaseHTML(cs, assembler, CommandPhaseType.INITIALIZATION, startCount);
        if ( nprinted > 0 ) {
            startCount += (nprinted - 1);
        }
    }
    if (cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.WARNING
        ||cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.FAILURE) {
        int nprinted = addPhaseHTML(cs, assembler, CommandPhaseType.DISCOVERY, startCount );
        if ( nprinted > 0 ) {
            startCount += (nprinted - 1);
        }
    }
    if (cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.SUCCESS ||
        cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.WARNING ||
        cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.FAILURE) {
        addPhaseHTML(cs, assembler, CommandPhaseType.RUN, startCount );
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
@param cs CommandStatus instance to print
@param assembler object that processes the output
@param commandPhaseType the command phase type to print in output
@param startCount the starting count to display in output, will be incremented during output
@return the last count printed
*/
private static int addPhaseHTML ( CommandStatus cs,
    HTMLStatusAssembler assembler, CommandPhaseType commandPhaseType, int startCount )
{
    CommandStatusType status = cs.getCommandStatus(commandPhaseType);
      
	List<CommandLogRecord> logRecList = cs.getCommandLog(commandPhaseType);
	int count = startCount;
    for ( CommandLogRecord logRec: logRecList ) {
        assembler.addPhase (
                    count++, commandPhaseType.toString(),
                    status.toString(),
                    getStatusColor(status),
                    logRec.getProblem(),
                    logRec.getRecommendation());
    }
	return logRecList.size();
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
when running a list of commands with a "runner" command like RunCommands to get a full list of logs.
The command associated with the individual logs is set to the original command so that
the "runner" is not associated with the log.
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
          // Append command log records for each run mode...
          List<CommandLogRecord> logs = status2.getCommandLog(CommandPhaseType.INITIALIZATION);
          CommandLogRecord logRecord;
          for ( int il = 0; il < logs.size(); il++ ) {
        	  logRecord = logs.get(il);
        	  logRecord.setCommandStatusProvider(csp);
              status.addToLog(CommandPhaseType.INITIALIZATION, logRecord );
          }
          logs = status2.getCommandLog(CommandPhaseType.DISCOVERY);
          for ( int il = 0; il < logs.size(); il++ ) {
        	  logRecord = logs.get(il);
        	  logRecord.setCommandStatusProvider(csp);
              status.addToLog(CommandPhaseType.DISCOVERY, logRecord );
          }
          logs = status2.getCommandLog(CommandPhaseType.RUN);
          for ( int il = 0; il < logs.size(); il++ ) {
        	  logRecord = logs.get(il);
        	  logRecord.setCommandStatusProvider(csp);
              status.addToLog(CommandPhaseType.RUN, logRecord );
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
full list of problems.  If the command log record has a non-blank type, it will be used.  Otherwise the default
is "CommandRun".  If the CommandLogRecord class name indicates an extended class, then the class name is used.
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
        String type = log.getType();
        if ( (type != null) && !type.equals("") ) {
            return type;
        }
        else {
            // Generic type.
            return "CommandRun";
        }
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
      List<CommandLogRecord> v = cs.getCommandLog(CommandPhaseType.INITIALIZATION);
      if ( v.size() > 0 ){
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Initialization log:");
          int size = v.size();
          for ( int i = 0; i < size; i++ ) {
        	  	if ( i > 0 ) {
        	  		b.append ( nl + dash_line );
        	  	}
                b.append ( nl + v.get(i) );
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
                b.append ( nl + v.get(i) );
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
                b.append ( nl + v.get(i) );
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
  public static String getHTMLStatusReport(List<Command> commands)
  {
    HTMLStatusAssembler assembler = new HTMLStatusAssembler();
    int count = 0;
    
    for ( Command command: commands ) {
        count = count + addCommandHTML(command, assembler);
    }
    /* TODO SAM 2013-12-07 The above will now add output for all successful phases so no need for below
    if (count == 0)
      {
        assembler.addNoProblems();
      }
      */
    
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
Given a list of Command, return a list of all the log records.  This is useful for
providing the full list for reporting.  The getLogRecordList() method is called with the commands that
implement CommandStatusProvider.
@param commandList List of CommandStatusProvider (e.g., list of commands from a command processor).
@param commandPhase command phase to return or null to return all (currently null does not return anything).
@return the list of log records for the given command phase
*/
public static List<CommandLogRecord> getLogRecordListFromCommands ( List<Command> commandList,
    CommandPhaseType commandPhase )
{
    List<CommandStatusProvider> commandStatusProviderList = new Vector();
    for ( Command command: commandList ) {
        if ( command instanceof CommandStatusProvider ) {
            commandStatusProviderList.add((CommandStatusProvider)command);
        }
    }
    CommandPhaseType [] phases = new CommandPhaseType[1];
    phases[0] = commandPhase;
    return getLogRecordList ( commandStatusProviderList, phases, null );
}

/**
Given a list of CommandStatusProvider, return a list of all the log records.  This is useful for
providing the full list for display in a UI or output file.
@param commandStatusProviderList List of CommandStatusProvider (e.g., list of commands from a command processor).
@param commandPhases array of command phases to return or null to return all.
@param commandStatuses array of command statuses to return or null to return all.
@return the list of log records for the given command phase
*/
public static List<CommandLogRecord> getLogRecordList ( List<CommandStatusProvider> commandStatusProviderList,
    CommandPhaseType [] commandPhases, CommandStatusType [] commandStatuses )
{	//String routine = "CommandStatusUtil.getLogRecordList";
	List<CommandLogRecord> logRecordList = new Vector<CommandLogRecord>(); // Returned list of log records
	if ( commandStatusProviderList == null ) {
	    // Return empty list
		return logRecordList;
	}
	CommandStatus status = null; // Status for the command
	List<CommandLogRecord> statusLogRecordList = null; // List of status log records for the command
	for ( CommandStatusProvider csp: commandStatusProviderList ) {
		// Get the information from a single command status provider
		//Message.printStatus(2, routine, "Getting log records for " + csp );
		status = csp.getCommandStatus();
		statusLogRecordList = status.getCommandLog(commandPhases,commandStatuses);
		//Message.printStatus(2, routine, "Command " + csp + " has " + statusLogRecordList.size() + " log records." );
		for ( CommandLogRecord logRecord: statusLogRecordList ) {
			// Append to full list of log records
			// Also set the command instance in the log record here since it was not
			// included in the original design.
			// Special case is that RunCommands() will set the command status provider when it rolls up status messages
			// so don't reset the CommandStatusProvider here in that case.
			if ( logRecord.getCommandStatusProvider() == null ) {
				logRecord.setCommandStatusProvider(csp);
			}
			logRecordList.add(logRecord);
		}
	}
	return logRecordList;
}

/**
Determine the number of log records for the requested severity.
@param status a CommandStatus that has log records to be appended to the first parameter.
@param severity the requested severity for a count.
@return the number of log records for the requested severity.
*/
public static int getSeverityCount ( CommandStatus status, CommandStatusType severity )
{
    int severityCount = 0;
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
        for ( CommandLogRecord log: logs ) {
            if ( log.getSeverity() == severity ) {
                ++severityCount;
            }
        }
    }
    return severityCount;
}

/**
Determine the number of log records (or commands) for the requested severity.
@param command a command that has log records to be appended to the first parameter.  It must implement
CommandStatusProvider.
@param severity the requested severity for a count.
@return the number of log records for the requested severity.
@param countCommands if false, return the total count of log records for a severity, and if true return the total
number of commands that have at least one log record with the indicated severity (in this case 0 or 1).
*/
public static int getSeverityCount ( Command command, CommandStatusType severity, boolean countCommands )
{
    List<Command> list = new Vector(1);
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
          int severityCount0 = getSeverityCount ( status, severity );
          severityCount += severityCount0;
          if ( severityCount0 > 0 ) {
              // Found a command that had status of the requested severity
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