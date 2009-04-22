package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

/**
This class provides a collecting point for status information for initializing and
processing a command.  It is returned when a command implements CommandStatusProvider.
*/
public class CommandStatus implements Cloneable
{
	
/**
 * Command status for initialization phase.
 * @uml.property  name="__initialization_status"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private CommandStatusType __initialization_status = CommandStatusType.UNKNOWN;

/**
 * Command status for discovery phase.
 * @uml.property  name="__discovery_status"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private CommandStatusType __discovery_status = CommandStatusType.UNKNOWN;

/**
 * Command status for run phase.
 * @uml.property  name="__run_status"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private CommandStatusType __run_status = CommandStatusType.UNKNOWN;

/**
 * A list of CommandLogRecord instances, indicating problems with initializing a command, guaranteed to be non-null.
 * @uml.property  name="__initialization_log_Vector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="RTi.Util.IO.CommandLogRecord"
 */
private List __initialization_log_Vector = new Vector();

/**
 * A list of CommandLogRecord instances, indicating problems with running the discovery phase of a command, guaranteed to be non-null.
 * @uml.property  name="__discovery_log_Vector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="RTi.Util.IO.CommandLogRecord"
 */
private List __discovery_log_Vector = new Vector();

/**
 * A list of CommandLogRecord instances, indicating problems with running the command, guaranteed to be non-null.
 * @uml.property  name="__run_log_Vector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="RTi.Util.IO.CommandLogRecord"
 */
private List __run_log_Vector = new Vector();

/**
Constructor that initializes the status for each phase to UNKNOWN.
*/
public CommandStatus ()
{
	// Default status is initialized above.
}

/**
Add a CommandLogRecord for the command and reset the status for the phase
to the most serious based on the log messages for the phase.
@param phase Phase of running a command (see CommandPhaseType).
@param record CommandLogRecord indicating a problem running a command.
*/
public void addToLog ( CommandPhaseType phase, CommandLogRecord record )
{	if ( phase == CommandPhaseType.INITIALIZATION ) {
		__initialization_status = CommandStatusType.maxSeverity ( __initialization_status, record.getSeverity() );
		__initialization_log_Vector.add ( record );
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		__discovery_status = CommandStatusType.maxSeverity ( __discovery_status, record.getSeverity());
		__discovery_log_Vector.add ( record );
	}
	else if ( phase == CommandPhaseType.RUN ) {
		__run_status = CommandStatusType.maxSeverity ( __run_status, record.getSeverity() );
		__run_log_Vector.add ( record );
	}
}

/**
 * Clear the CommandLogRecord for the command.
 * @param phase Phase of running a command (see CommandPhaseType) or null to clear logs for all phases.
 */
public void clearLog ( CommandPhaseType phase )
{
	if ( (phase == CommandPhaseType.INITIALIZATION) || (phase == null)) {
		__initialization_log_Vector.clear ();
		__initialization_status = CommandStatusType.UNKNOWN;
	}
	else if ( (phase == CommandPhaseType.DISCOVERY) || (phase == null)) {
		__discovery_log_Vector.clear ();
		__discovery_status = CommandStatusType.UNKNOWN;
	}
	else if ( (phase == CommandPhaseType.RUN) || (phase == null)) {
		__run_log_Vector.clear ();
		__run_status = CommandStatusType.UNKNOWN;
	}
}

/**
Clone the instance.  All command data are cloned, including the log records.
*/
public Object clone ()
{	try {
        CommandStatus status = (CommandStatus)super.clone();
		// Copy the status information...
		status.__initialization_status = __initialization_status;
		status.__discovery_status = __discovery_status;
		status.__run_status = __run_status;
		// Clone the logs...
		status.__initialization_log_Vector = new Vector();
		int size = __initialization_log_Vector.size();
		for ( int i = 0; i < size; i++ ) {
			status.__initialization_log_Vector.add (
					((CommandLogRecord)__initialization_log_Vector.get(i)).clone() );
		}
		status.__discovery_log_Vector = new Vector();
		size = __discovery_log_Vector.size();
		for ( int i = 0; i < size; i++ ) {
			status.__discovery_log_Vector.add ( ((CommandLogRecord)__discovery_log_Vector.get(i)).clone() );
		}
		status.__run_log_Vector = new Vector();
		size = __run_log_Vector.size();
		for ( int i = 0; i < size; i++ ) {
			status.__run_log_Vector.add ( ((CommandLogRecord)__run_log_Vector.get(i)).clone() );
		}
		return status;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}
	
/**
 * Return the status for a phase of command processing.
 */
public CommandStatusType getCommandStatus ( CommandPhaseType phase )
{
	if ( phase == CommandPhaseType.INITIALIZATION ) {
		return __initialization_status;
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		return __discovery_status;
	}
	else if ( phase == CommandPhaseType.RUN ) {
		return __run_status;
	}
	else { // This should never happen.
		return CommandStatusType.UNKNOWN;
	}
}

/**
 * Returns the command log for the specified phase, guaranteed to be non-null.
 * @param phase - see CommandPhaseType.
 * @return command log as a list of CommandLogRecord
 */
public List<CommandLogRecord> getCommandLog(CommandPhaseType phase)
{
    if ( phase == CommandPhaseType.INITIALIZATION ) {
        return __initialization_log_Vector;
    }
    else if ( phase == CommandPhaseType.DISCOVERY ) {
        return __discovery_log_Vector;
    }
    else if ( phase == CommandPhaseType.RUN ) {
        return __run_log_Vector;
    }
    else {
        // Return all records
        List v = new Vector();
        v.addAll(__initialization_log_Vector);
        v.addAll(__discovery_log_Vector);
        v.addAll(__run_log_Vector);
        return v;
    }
}

/**
Refresh the command status for a phase.  This should normally only be called when
initializing a status or setting to success.  Otherwise, addToLog() should be
used and the status determined from the CommandLogRecord status values.
@param phase Command phase
@param severity_if_unknown The severity to set for the phase if it is currently
unknown.  For example, specify as CommandStatusType.SUCCESS to override the
initial CommandStatusType.UNKNOWN value.
*/
public void refreshPhaseSeverity ( CommandPhaseType phase, CommandStatusType severity_if_unknown )
{
	if ( phase == CommandPhaseType.INITIALIZATION ) {
		if ( __initialization_status.equals(CommandStatusType.UNKNOWN)) {
			__initialization_status = severity_if_unknown;
		}
	}
	else if ( phase == CommandPhaseType.DISCOVERY ) {
		if ( __discovery_status.equals(CommandStatusType.UNKNOWN)) {
			__discovery_status = severity_if_unknown;
		}
	}
	else if ( phase == CommandPhaseType.RUN ) {
		if ( __run_status.equals(CommandStatusType.UNKNOWN)) {
			__run_status = severity_if_unknown;
		}
	}
}

/**
 * Indicate whether the severity is greater than or equal to a provided severity.
 * @param phase to check, or null to check against all phases.
 * @param severity A severity (e.g., CommandStatusType.WARNING) to check.
 * @return true if the maximum command status severity is >= the provided severity.
 */
public boolean severityGreaterThanOrEqualTo ( CommandPhaseType phase, CommandStatusType severity )
{
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
	else { // This should never happen.
		return false;
	}
}

/**
Convert the command status to a String, for simple viewing.
*/
public String toString()
{	return CommandStatusUtil.getCommandLogText( this );
}

}
