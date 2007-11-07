package RTi.Util.IO;

import java.util.Enumeration;
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
   * Adds a command to the HTML accumulating in the HTMLStatusAssembler.
   * 
   * @param command
   * @param assembler
   * @return return 0 if no warnings or failures found, otherwise 1
   */
  private static int addCommandHTML(Command command,
          HTMLStatusAssembler assembler)
  {
    CommandStatus cs;
    int problemsFound = 0;
    
    if ( command instanceof CommandStatusProvider ) 
      {
        cs = ((CommandStatusProvider)command).getCommandStatus();
        if (isProblematic(cs))
          {
            problemsFound = 1;
            // add Command
            assembler.addCommand(command.toString());

            // add Command Summary Table
            addCommandSummary(cs, assembler);

            addCommandDetailTable(assembler, cs);
          }
      }
    else
      {
        addNotACommandStatusProvider(assembler);
      }

    assembler.endCommand();
    
    return problemsFound;
  }
/**
 * Adds Detail table.
 * 
 * @param assembler
 * @param cs
 */
  private static void addCommandDetailTable(
          HTMLStatusAssembler assembler, CommandStatus cs)
  {
    assembler.startCommandStatusTable();

    if (cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.WARNING
            ||cs.getCommandStatus(CommandPhaseType.INITIALIZATION)== CommandStatusType.FAILURE)
      {
        addPhaseHTML(cs, assembler, CommandPhaseType.INITIALIZATION);
      }

    if (cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.WARNING
            ||cs.getCommandStatus(CommandPhaseType.DISCOVERY)== CommandStatusType.FAILURE)
      {
        addPhaseHTML(cs, assembler, CommandPhaseType.DISCOVERY);
      }
    if (cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.WARNING
            ||cs.getCommandStatus(CommandPhaseType.RUN)== CommandStatusType.FAILURE)
      {
        addPhaseHTML(cs, assembler, CommandPhaseType.RUN);
      }
  }
  
  /**
   * Adds command status summary table.
   * @param cs
   * @param assembler
   */
  private static void addCommandSummary(CommandStatus cs, HTMLStatusAssembler assembler)
  {
    assembler.addCommandStatusSummary(
            cs.getCommandStatus(CommandPhaseType.INITIALIZATION),
            cs.getCommandStatus(CommandPhaseType.DISCOVERY),
            cs.getCommandStatus(CommandPhaseType.RUN));
  }
  
  /**
   * Adds text indicating no issues found.
   * @param assembler
   */
  private static void addNotACommandStatusProvider(HTMLStatusAssembler assembler)
  {
    assembler.addNotACommandStatusProvider();
  }
  
  /**
   * Adds html for a command status phase.
   * @param cs
   * @param assembler
   */
  private static void addPhaseHTML(CommandStatus cs,
          HTMLStatusAssembler assembler, CommandPhaseType commandPhaseType)
  {
    CommandStatusType status = cs.getCommandStatus(commandPhaseType);
      
      Vector v = cs.getCommandLog(commandPhaseType);
      if ( v.size() > 0 )
        {
          Enumeration e = v.elements();
          int i = 0;
          while (e.hasMoreElements())
              {
                CommandLogRecord clr = (CommandLogRecord)e.nextElement();

                assembler.addPhase(commandPhaseType.toString(),
                        status.toString(),
                        getStatusColor(status),
                        clr.getProblem(),
                        clr.getRecommendation());
                ++i;
              }
      }
  }
  
  /**
   * Returns the command status log records ready for display as HTML.
   * 
   * @param csp command status provider
   * @return concatenated log records as text
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
   * Returns the command log records ready for display as HTML.
   * 
   * @param csp command status provider
   * @return concatenated log records as HTML
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
	 * TODO SAM 2007-08-15 Need to doc.
	 * @param o
	 * @return
	 */
  public static String getCommandLogHTML(CommandStatusProvider csp)
  {
        String toolTip = getHTMLCommandStatus(csp.getCommandStatus());
        return toolTip;
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
      Vector v = cs.getCommandLog(CommandPhaseType.INITIALIZATION);
      if ( v.size() > 0 ){
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Initialization log:");
          Enumeration e = v.elements();
          int i = 0;
          while (e.hasMoreElements())
              {
        	  	if ( i > 0 ) {
        	  		b.append ( nl + dash_line );
        	  	}
                b.append ( nl + e.nextElement().toString() );
                ++i;
              }
      }
      b.append ( nl + thick_line );
      b.append ( nl + "Discovery status: " + cs.getCommandStatus(CommandPhaseType.DISCOVERY));
      v = cs.getCommandLog(CommandPhaseType.DISCOVERY);
      if ( v.size() > 0 ) {
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Discovery log:");
          Enumeration e = v.elements();
          int i = 0;
          while (e.hasMoreElements())
              {
      	  		if ( i > 0 ) {
      	  			b.append ( nl + dash_line );
      	  		}
                b.append ( nl + e.nextElement().toString() );
                ++i;
              }
      }
      b.append ( nl + thick_line );
      b.append ( nl + "Run status: " + cs.getCommandStatus(CommandPhaseType.RUN));
      v = cs.getCommandLog(CommandPhaseType.RUN);
      if ( v.size() > 0 ) {
    	  b.append ( nl + thin_line );
    	  b.append ( nl + "Run log:");
          Enumeration e = v.elements();
          int i = 0;
          while (e.hasMoreElements())
              {
        	  	if ( i > 0 ) {
        		  b.append ( nl + dash_line );
    	  		}
                b.append ( nl + e.nextElement().toString() );
              }
          ++i;
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
  public static String getHTMLStatusReport(Vector commands)
  {
    HTMLStatusAssembler assembler = new HTMLStatusAssembler();
    Command command;
    int count = 0;
    
    for ( int i = 0; i < commands.size(); i++ ) 
      {
        command = (Command)commands.elementAt(i);
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
   * Returns a color associated with specified status type.
   * 
   * @param type command status type
   * @return color associated with type
   */
  public static String getStatusColor(CommandStatusType type)
  {
    // colors for : success, warning, failure
    String colors[] = {"green","yellow","red"};
    
    if (type == CommandStatusType.UNKNOWN)
      {
        return "white";
      }
    return colors[type.getSeverity()];
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



