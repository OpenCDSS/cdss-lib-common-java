package RTi.Util.IO;

// TODO SAM 2007-06-25 Can a link to full logging be implemented somehow to allow
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
 * Log type/severity level.
 */
private CommandStatusType __severity = null;
	
/**
 * Problem that has been identified.
 * @uml.property  name="__problem"
 */
private String __problem = null;

/**
 * Recommended solution.
 * @uml.property  name="__recommendation"
 */
private String __recommendation = null;

/**
 * Constructor for a command log record.
 * @param severity Severity for the log record, from CommandStatusType.
 * @param problem A String describing the problem.
 * @param recommendation A String recommending a solution.
 */
public CommandLogRecord(CommandStatusType severity, String problem, String recommendation)
  {
	__severity = severity;
    __problem = problem;
    __recommendation = recommendation;
  }

/**
 * Details that can be used to troubleshoot and link to other information.
 * TODO SAM 2007-06-25 Need to flush out the details.  For example, this could
 * be a list of the parameter/value pairs.  It is more difficult to define the
 * properties when a run-time error with dynamic data.
 
private PropList __details_PropList = null;
	*/
/**
 * Return the status for a phase of processing.
 
public CommandLogRecord ( String problem, String recommendation, PropList details )
{
	setProblem ( problem );
	setRecommendation ( recommendation );
	setDetails ( details );
}
*/

/**
Clone the instance.  All command data are cloned.
*/
public Object clone ()
{	try {	CommandLogRecord record = (CommandLogRecord)super.clone();
		// The problem and recommendation are automatically copied.
		// Copy the severity...
		record.__severity = __severity;
		return record;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
 * Get the log record problem.
 * 
 * @return the problem string
 */
public String getProblem()
{
  return __problem;
}

/**
 * Get the log record recommendation.
 * 
 * @return recommendation string
 */
public String getRecommendation()
{
  return __recommendation;
}

/**
Get the severity associated with a log record.
*/
public CommandStatusType getSeverity()
{
	return __severity;
}

/**
 * Set the details describing the problem.
 
public void setDetails ( PropList details )
{	__details_PropList = details;
}
*/

/**
 * Set the description of the problem that was identified.
 */
public void setProblem ( String problem )
{	__problem = problem;
}

/**
 * Set the recommendation to resolve the problem.
 */
public void setRecommendation ( String recommendation )
{	__recommendation = recommendation;
}

/**
 * Return a string representation of the problem, suitable for display in
 * a popup, etc.
 */
public String toString ()
{	return
	"Severity:  " + __severity + "\n" +
	"Problem:  " + __problem + "\n" +
	"Recommendation:  " + __recommendation + "\n";// +
	//"Details:\n" + "uncomment"; //XXX dre:uncomment
	//__details_PropList;
}

}
