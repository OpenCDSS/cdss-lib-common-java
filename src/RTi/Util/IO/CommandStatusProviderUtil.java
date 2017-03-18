package RTi.Util.IO;

import java.util.List;

import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;

/**
 * Provides convenience methods for working with Command Status.
 * 
 * @author dre
 */
public class CommandStatusProviderUtil
{
  /**
   * Returns the command log records ready for display as html.
   * 
   * @param csp command status provider
   * @return concatenated log records as text

   */
  public static String getCommandLogHTML(Object o)
  {
    if ( o instanceof CommandStatusProvider)
      {
        CommandStatusProvider csp = (CommandStatusProvider)o;
        String toolTip = CommandStatusProviderUtil.getCommandLogHTML(csp);
        return toolTip;
      }
    else
      return null;
  }

  /**
   * Returns the command log records ready for display as html.
   * 
   * @param csp command status provider
   * @return concatenated log records as text
   */
  public static String getCommandLogHTML(CommandStatusProvider csp)
  {
    return HTMLUtil.text2html(getCommandLogText(csp),true);
  }


  /**
   * Returns the command log records ready for display as text
   * @param csp command status provider
   * 
   * @return concatenated log records as text
   */
  public static String getCommandLogText(CommandStatusProvider csp)
  {
    String markerText ="";

    if (csp != null)
      {
        CommandStatus cs = csp.getCommandStatus();

        if (cs != null)
          {
            List<CommandLogRecord> v = cs.getCommandLog(CommandPhaseType.INITIALIZATION);
            int size = v.size();
            for ( int i = 0; i < size; i++ )
              {
                markerText = markerText + v.get(i).toString();
              }
            v = cs.getCommandLog(CommandPhaseType.DISCOVERY);
            size = v.size();
            for ( int i = 0; i < size; i++ )
              {
                markerText = markerText + v.get(i).toString();
              }
            v = cs.getCommandLog(CommandPhaseType.RUN);
            size = v.size();
            for ( int i = 0; i < size; i++ )
              {
                markerText = markerText + v.get(i).toString();
              }
          }

      }
    return markerText;

  }

  /**
   * Returns the highest status severity of all provided command status providers,
   * to indicate the most severe problem with a list of commands.
   * @param csp_list command status provider list.
   * @see CommandStatusType
   * @return The highest severity status from any command status provider in the list.
   */
  public static CommandStatusType getHighestSeverity(List<CommandStatusProvider> csp_list)
    { if ( csp_list == null ) {
    	return CommandStatusType.UNKNOWN;
      }

    	// Loop through the list...
    
    	int size = csp_list.size();
    	CommandStatusType max = CommandStatusType.UNKNOWN;
    	CommandStatusProvider csp;
    	for ( int i = 0; i < size; i++ ) {
    		csp = csp_list.get(i);
    		max = CommandStatusType.maxSeverity ( max, CommandStatusUtil.getHighestSeverity ( csp ) );
    	}
      return max;

    }
  
  /**
   * Returns the highest status severity of all phases.
   * 
   * @param csp command status provider @see CommandStatusType
   * @return highest severity 
   * <ol> 
   *  <li> -1 - UNKNOWN
   *  <li> 0 - SUCCESS</li>
   *  <li> 1 - WARNING</li>
   *  <li> 2 - FAILURE</li>
   *  </ol>
   * @see CommandStatusType
   */
  public static int getHighestSeverity(CommandStatusProvider csp)
  {
    CommandStatus cs = csp.getCommandStatus();
    int status = CommandStatusType.UNKNOWN.getSeverity();

    if (cs != null)
      {
        int phaseStatus = cs.getCommandStatus(CommandPhaseType.INITIALIZATION).getSeverity();
        if ( phaseStatus> status)
          {
            status = phaseStatus;
          }
        phaseStatus = cs.getCommandStatus(CommandPhaseType.DISCOVERY).getSeverity();
        if ( phaseStatus> status)
          {
            status = phaseStatus;
          }
        phaseStatus = cs.getCommandStatus(CommandPhaseType.RUN).getSeverity();
        if ( phaseStatus> status)
          {
            status = phaseStatus;
          }
      }
    return status;
  } // eof getHighestSeverity()
}

