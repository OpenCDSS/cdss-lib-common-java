// CommandLogRecord - this class provides a single record of logging as managed by the CommandStatus class

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

// TODO smalers 2007-06-25 Can a link to full logging be implemented somehow to allow
// drill-down to the log file (with an appropriate filter/navigation)?
/**
This class provides a single record of logging as managed by the CommandStatus class.
It is meant only to track a problem and recommend a solution.  Consequently,
the status severity should normally be WARNING or FAILURE, with no other
log records tracked for status purposes.
*/
public class CommandLogRecord implements Cloneable
{

/**
CommandStatusProvider that generates this log (currently null unless extracted by other code from the log list).
*/
private CommandStatusProvider __commandStatusProvider = null;

/**
 * Log type/severity level.
 */
private CommandStatusType __severity = null;

/**
 * Type of problem that has been identified.
 * This is used, for example, for a log record report to show categories of problems.
 */
private String __type = null;

/**
 * Problem that has been identified.
 */
private String __problem = null;

/**
 * Recommended solution.
 */
private String __recommendation = null;

/**
 * Constructor for a command log record.
 * @param severity Severity for the log record, from CommandStatusType.
 * @param problem A String describing the problem.
 * @param recommendation A String recommending a solution.
 */
public CommandLogRecord(CommandStatusType severity, String problem, String recommendation) {
	this ( severity, "", problem, recommendation );
}

/**
 * Constructor for a command log record.
 * @param severity Severity for the log record, from CommandStatusType.
 * @param type the log record type.
 * @param problem A String describing the problem.
 * @param recommendation A String recommending a solution.
 */
public CommandLogRecord(CommandStatusType severity, String type, String problem, String recommendation) {
    this.__severity = severity;
    this.__type = type;
    this.__problem = problem;
    this.__recommendation = recommendation;
}

/**
 * Copy constructor for a command log record.
 * @param severity Severity for the log record, from CommandStatusType.
 * @param type the log record type.
 * @param problem A String describing the problem.
 * @param recommendation A String recommending a solution.
 */
public CommandLogRecord(CommandLogRecord record) {
    this.__severity = record.__severity;
    this.__type = record.__type;
    this.__problem = record.__problem;
    this.__recommendation = record.__recommendation;
}

/**
Clone the instance.  All command data are cloned.
*/
public Object clone () {
	try {
        CommandLogRecord record = (CommandLogRecord)super.clone();
		// The problem and recommendation are automatically copied.
		// Copy the severity.
		record.__severity = __severity;
		return record;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Get the log record command status provider.
@return the log record command status provider.
*/
public CommandStatusProvider getCommandStatusProvider() {
	return __commandStatusProvider;
}

/**
 * Get the log record problem.
 * @return the problem string
 */
public String getProblem() {
	return __problem;
}

/**
 * Get the log record recommendation.
 * @return recommendation string
 */
public String getRecommendation() {
	return __recommendation;
}

/**
 * Get the log record type.
 * @return type string
 */
public String getType() {
	return __type;
}

/**
Get the severity associated with a log record.
@return the severity
*/
public CommandStatusType getSeverity() {
	return __severity;
}

/**
Set the command status provider for this record (e.g., the Command that generated the record).
Currently this is not in the constructor and is typically set with CommandStatusUtil.getLogRecordList().
Make it protected to handle internally in this package for now.
@param csp CommandStatusProvider to set
*/
protected void setCommandStatusProvider ( CommandStatusProvider csp ) {
	__commandStatusProvider = csp;
}

/**
 * Set the description of the problem that was identified.
 * @param problem project description
 */
public void setProblem ( String problem ) {
	__problem = problem;
}

/**
 * Set the recommendation to resolve the problem.
 * @param recommendation recommendation to fix the problem
 */
public void setRecommendation ( String recommendation ) {
	__recommendation = recommendation;
}

/**
Return a string representation of the log record, suitable for display in a troubleshooting popup, etc.
@return a string representation of the log record, suitable for display in a troubleshooting popup, etc.
*/
public String toString () {
	return
	"Severity:  " + __severity + "\n" +
	"Type:  " + __type + "\n" +
	"Problem:  " + __problem + "\n" +
	"Recommendation:  " + __recommendation + "\n";
}

}