// CommandStatus - this class provides a collecting point for status information for initializing and processing a command

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

import java.util.ArrayList;
import java.util.List;

/**
This class provides a collecting point for status information for initializing and processing a command.
It is returned when a command implements CommandStatusProvider.
*/
public class CommandStatus implements Cloneable
{

/**
 * Command status for initialization phase.
 */
private CommandStatusType __initialization_status = CommandStatusType.UNKNOWN;

/**
 * Command status for discovery phase.
 */
private CommandStatusType __discovery_status = CommandStatusType.UNKNOWN;

/**
 * Command status for run phase.
 */
private CommandStatusType __run_status = CommandStatusType.UNKNOWN;

/**
 * A list of CommandLogRecord instances, indicating problems with initializing a command, guaranteed to be non-null.
 */
private List<CommandLogRecord> __initializationLogList = new ArrayList<>();

/**
 * A list of CommandLogRecord instances, indicating problems with running the discovery phase of a command, guaranteed to be non-null.
 */
private List<CommandLogRecord> __discoveryLogList = new ArrayList<>();

/**
 * A list of CommandLogRecord instances, indicating problems with running the command, guaranteed to be non-null.
 */
private List<CommandLogRecord> __runLogList = new ArrayList<>();

/**
Constructor that initializes the status for each phase to UNKNOWN.
*/
public CommandStatus () {
	// Default status is initialized above.
}

/**
Add a CommandLogRecord for the command and reset the status for the phase
to the most serious based on the log messages for the phase.
@param phase Phase of running a command (see CommandPhaseType).
@param record CommandLogRecord indicating a problem running a command.
*/
public void addToLog ( CommandPhaseType phase, CommandLogRecord record ) {
	if ( phase == CommandPhaseType.INITIALIZATION ) {
		__initialization_status = CommandStatusType.maxSeverity ( __initialization_status, record.getSeverity() );
		__initializationLogList.add ( record );
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		__discovery_status = CommandStatusType.maxSeverity ( __discovery_status, record.getSeverity());
		__discoveryLogList.add ( record );
	}
	else if ( phase == CommandPhaseType.RUN ) {
		__run_status = CommandStatusType.maxSeverity ( __run_status, record.getSeverity() );
		__runLogList.add ( record );
	}
}

/**
 * Clear the CommandLogRecord for the command.
 * @param phase Phase of running a command (see CommandPhaseType) or null to clear logs for all phases.
 */
public void clearLog ( CommandPhaseType phase ) {
	if ( (phase == CommandPhaseType.INITIALIZATION) || (phase == null)) {
		__initializationLogList.clear ();
		__initialization_status = CommandStatusType.UNKNOWN;
	}
	else if ( (phase == CommandPhaseType.DISCOVERY) || (phase == null)) {
		__discoveryLogList.clear ();
		__discovery_status = CommandStatusType.UNKNOWN;
	}
	else if ( (phase == CommandPhaseType.RUN) || (phase == null)) {
		__runLogList.clear ();
		__run_status = CommandStatusType.UNKNOWN;
	}
}

/**
Clone the instance.  All command data are cloned, including the log records.
*/
public Object clone () {
	try {
        CommandStatus status = (CommandStatus)super.clone();
		// Copy the status information.
		status.__initialization_status = __initialization_status;
		status.__discovery_status = __discovery_status;
		status.__run_status = __run_status;
		// Clone the logs.
		status.__initializationLogList = new ArrayList<>();
		int size = __initializationLogList.size();
		for ( int i = 0; i < size; i++ ) {
			status.__initializationLogList.add ( (CommandLogRecord)((CommandLogRecord)__initializationLogList.get(i)).clone() );
		}
		status.__discoveryLogList = new ArrayList<>();
		size = __discoveryLogList.size();
		for ( int i = 0; i < size; i++ ) {
			status.__discoveryLogList.add ( (CommandLogRecord)((CommandLogRecord)__discoveryLogList.get(i)).clone() );
		}
		status.__runLogList = new ArrayList<>();
		size = __runLogList.size();
		for ( int i = 0; i < size; i++ ) {
			status.__runLogList.add ( (CommandLogRecord)((CommandLogRecord)__runLogList.get(i)).clone() );
		}
		return status;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
 * Return the status for a phase of command processing (INITIALIZATION, DISCOVERY, RUN).
 * @return the status for a phase of command processing
 */
public CommandStatusType getCommandStatus ( CommandPhaseType phase ) {
	if ( phase == CommandPhaseType.INITIALIZATION ) {
		return __initialization_status;
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		return __discovery_status;
	}
	else if ( phase == CommandPhaseType.RUN ) {
		return __run_status;
	}
	else {
		// This should never happen.
		return CommandStatusType.UNKNOWN;
	}
}

/**
 * Returns the command log for the specified phase, guaranteed to be non-null.
 * @param phase - see CommandPhaseType.
 * @return command log as a list of CommandLogRecord
 */
public List<CommandLogRecord> getCommandLog(CommandPhaseType phase) {
    if ( phase == CommandPhaseType.INITIALIZATION ) {
        return __initializationLogList;
    }
    else if ( phase == CommandPhaseType.DISCOVERY ) {
        return __discoveryLogList;
    }
    else if ( phase == CommandPhaseType.RUN ) {
        return __runLogList;
    }
    else {
        // Return all records
        List<CommandLogRecord> v = new ArrayList<>();
        v.addAll(__initializationLogList);
        v.addAll(__discoveryLogList);
        v.addAll(__runLogList);
        return v;
    }
}

/**
Returns the command log for the specified phases and status types, guaranteed to be non-null.
@param phases array of CommandPhaseType to filter log records, or null to return all.
@param statuses array of CommandStatusType to filter log records, or null to return all.
@return command log as a list of CommandLogRecord
*/
public List<CommandLogRecord> getCommandLog(CommandPhaseType [] phases, CommandStatusType [] statuses) {
	if ( phases == null ) {
		phases = new CommandPhaseType[3];
		phases[0] = CommandPhaseType.INITIALIZATION;
		phases[1] = CommandPhaseType.DISCOVERY;
		phases[2] = CommandPhaseType.RUN;
	}
	if ( statuses == null ) {
		statuses = new CommandStatusType[5];
		statuses[0] = CommandStatusType.INFO;
		statuses[1] = CommandStatusType.NOTIFICATION;
		statuses[2] = CommandStatusType.SUCCESS;
		statuses[3] = CommandStatusType.FAILURE;
		statuses[4] = CommandStatusType.WARNING;
	}
	List<CommandLogRecord> logList = new ArrayList<>();
	int j;
	List<CommandLogRecord> logList2 = null;
	for ( int i = 0; i < phases.length; i++ ) {
	    if ( phases[i] == CommandPhaseType.INITIALIZATION ) {
	    	logList2 = __initializationLogList;
	    }
	    else if ( phases[i] == CommandPhaseType.DISCOVERY ) {
	    	logList2 = __discoveryLogList;
	    }
	    else if ( phases[i] == CommandPhaseType.RUN ) {
	    	logList2 = __runLogList;
	    }
	    if ( logList2 != null ) {
	    	for ( CommandLogRecord log : logList2 ) {
		    	for ( j = 0; j < statuses.length; j++ ) {
		    		if ( log.getSeverity() == statuses[j] ) {
		    			logList.add(log);
		    			break;
		    		}
		    	}
	    	}
	    }
	}
    return logList;
}

/**
 * Return whether the command status includes any notifications for a phase.
 * @return whether the command status includes any notifications for a phase
 */
public boolean getHasNotification ( CommandPhaseType phase ) {
	boolean hasNotification = false;
	if ( (phase == CommandPhaseType.ANY) || (phase == CommandPhaseType.INITIALIZATION) ) {
		for ( CommandLogRecord record : this.__initializationLogList ) {
			if ( record.getSeverity() == CommandStatusType.NOTIFICATION ) {
				hasNotification = true;
				break;
			}
		}
	}
	else if ( (phase == CommandPhaseType.ANY) || (phase == CommandPhaseType.DISCOVERY) ) {
		for ( CommandLogRecord record : this.__discoveryLogList ) {
			if ( record.getSeverity() == CommandStatusType.NOTIFICATION ) {
				hasNotification = true;
				break;
			}
		}
	}
	else if ( (phase == CommandPhaseType.ANY) || (phase == CommandPhaseType.RUN) ) {
		for ( CommandLogRecord record : this.__runLogList ) {
			if ( record.getSeverity() == CommandStatusType.NOTIFICATION ) {
				hasNotification = true;
				break;
			}
		}
	}
	return hasNotification;
}

/**
Refresh the command status for a phase.
This should normally only be called when initializing a status or setting to success.
Otherwise, addToLog() should be used and the status determined from the CommandLogRecord status values.
@param phase Command phase
@param severityIfUnknown The severity to set for the phase if it is currently unknown.
For example, specify as CommandStatusType.SUCCESS to override the initial CommandStatusType.UNKNOWN value.
*/
public void refreshPhaseSeverity ( CommandPhaseType phase, CommandStatusType severityIfUnknown ) {
	if ( phase == CommandPhaseType.INITIALIZATION ) {
		if ( __initialization_status.equals(CommandStatusType.UNKNOWN)) {
			__initialization_status = severityIfUnknown;
		}
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		if ( __discovery_status.equals(CommandStatusType.UNKNOWN)) {
			__discovery_status = severityIfUnknown;
		}
	}
	else if ( phase == CommandPhaseType.RUN ) {
		if ( __run_status.equals(CommandStatusType.UNKNOWN)) {
			__run_status = severityIfUnknown;
		}
	}
}

/**
 * Indicate whether the severity is greater than or equal to a provided severity.
 * @param phase to check, or null to check against all phases.
 * @param severity A severity (e.g., CommandStatusType.WARNING) to check.
 * @return true if the maximum command status severity is >= the provided severity.
 */
public boolean severityGreaterThanOrEqualTo ( CommandPhaseType phase, CommandStatusType severity ) {
	if ( phase == null ) {
		// Check maximum of all phases.
		return CommandStatusUtil.getHighestSeverity(this).greaterThanOrEqualTo(severity);
	}
	else if ( phase == CommandPhaseType.INITIALIZATION ) {
		return __initialization_status.greaterThanOrEqualTo ( severity );
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		return __discovery_status.greaterThanOrEqualTo ( severity );
	}
	else if ( phase == CommandPhaseType.RUN ) {
		return __run_status.greaterThanOrEqualTo ( severity );
	}
	else {
		// This should never happen.
		return false;
	}
}

/**
Convert the command status to a String, for simple viewing.
@return string representation of the status
*/
public String toString() {
	return CommandStatusUtil.getCommandLogText( this );
}

}