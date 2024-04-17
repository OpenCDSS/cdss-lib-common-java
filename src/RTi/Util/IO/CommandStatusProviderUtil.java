// CommandStatusProviderUtil - provides convenience methods for working with CommandStatus.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

import java.util.List;

import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;

/**
 * Provides convenience methods for working with CommandStatus.
 */
public class CommandStatusProviderUtil {
  /**
   * Returns the command log records ready for display as html.
   *
   * @param csp command status provider
   * @return concatenated log records as text, or null if not a CommandStatusProvider
   */
  public static String getCommandLogHTML(Object o) {
    if ( o instanceof CommandStatusProvider) {
        CommandStatusProvider csp = (CommandStatusProvider)o;
        String toolTip = CommandStatusProviderUtil.getCommandLogHTML(csp);
        return toolTip;
    }
    else {
      return null;
    }
  }

  /**
   * Returns the command log records ready for display as html.
   *
   * @param csp command status provider
   * @return concatenated log records as text
   */
  public static String getCommandLogHTML(CommandStatusProvider csp) {
    return HTMLUtil.text2html(getCommandLogText(csp),true);
  }

  /**
   * Returns the command log records ready for display as text
   * @param csp command status provider
   * @return concatenated log records as text
   */
  public static String getCommandLogText ( CommandStatusProvider csp ) {
    StringBuilder markerText = new StringBuilder();

    if ( csp != null ) {
        CommandStatus cs = csp.getCommandStatus();

        if ( cs != null ) {
            for ( CommandLogRecord logRecord : cs.getCommandLog(CommandPhaseType.INITIALIZATION) ) {
                markerText.append ( logRecord.toString() );
            }
            for ( CommandLogRecord logRecord : cs.getCommandLog(CommandPhaseType.DISCOVERY) ) {
                markerText.append ( logRecord.toString() );
            }
            for ( CommandLogRecord logRecord : cs.getCommandLog(CommandPhaseType.RUN) ) {
                markerText.append ( logRecord.toString() );
            }
        }
    }
    return markerText.toString();
  }

  /**
   * Returns the highest status severity of all provided command status providers,
   * to indicate the most severe problem with a list of commands.
   * @param cspList command status provider list
   * @see CommandStatusType
   * @return The highest severity status from any command status provider in the list, or CommandStatusType.UNKNOWN if a null list
   */
  public static CommandStatusType getHighestSeverity ( List<CommandStatusProvider> cspList ) {
      if ( cspList == null ) {
    	  return CommandStatusType.UNKNOWN;
      }

      // Loop through the list.

      CommandStatusType max = CommandStatusType.UNKNOWN;
      for ( CommandStatusProvider csp : cspList ) {
    	  max = CommandStatusType.maxSeverity ( max, CommandStatusUtil.getHighestSeverity ( csp ) );
      }

      return max;
  }

  /**
   * Returns whether the command status provider has a notification.
   * @param csp command status provider
   * @return true if the CommmandStatusProvider has a notification, false if not
   */
  public static boolean getHasNotification ( CommandStatusProvider csp ) {
    CommandStatus cs = csp.getCommandStatus();
    boolean hasNotification = false;

    if ( cs != null ) {
        boolean phaseHasNotification = cs.getHasNotification ( CommandPhaseType.INITIALIZATION );
        if ( phaseHasNotification ) {
            hasNotification = true;
        }
        phaseHasNotification = cs.getHasNotification ( CommandPhaseType.DISCOVERY );
        if ( phaseHasNotification ) {
            hasNotification = true;
        }
        phaseHasNotification = cs.getHasNotification ( CommandPhaseType.RUN );
        if ( phaseHasNotification ) {
            hasNotification = true;
        }
    }
    return hasNotification;
  }

  /**
   * Returns the highest status severity of all phases.
   *
   * @param csp command status provider
   * @see CommandStatusType
   * @return highest severity
   * <ol>
   *   <li> -1 - UNKNOWN
   *   <li> 0 - SUCCESS</li>
   *   <li> 1 - WARNING</li>
   *   <li> 2 - FAILURE</li>
   * </ol>
   * @see CommandStatusType
   */
  public static int getHighestSeverity ( CommandStatusProvider csp ) {
    CommandStatus cs = csp.getCommandStatus();
    int status = CommandStatusType.UNKNOWN.getSeverity();

    if ( cs != null ) {
        int phaseStatus = cs.getCommandStatus ( CommandPhaseType.INITIALIZATION ).getSeverity();
        if ( phaseStatus> status) {
            status = phaseStatus;
        }
        phaseStatus = cs.getCommandStatus ( CommandPhaseType.DISCOVERY ).getSeverity();
        if ( phaseStatus> status) {
            status = phaseStatus;
        }
        phaseStatus = cs.getCommandStatus ( CommandPhaseType.RUN ).getSeverity();
        if ( phaseStatus> status) {
            status = phaseStatus;
        }
    }
    return status;
  }
}