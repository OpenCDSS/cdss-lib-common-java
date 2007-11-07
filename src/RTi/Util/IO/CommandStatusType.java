package RTi.Util.IO;

/**
This class provides an enumeration of possible command status values.
An enum could be used when Java 1.5 is utilized.     
*/
public class CommandStatusType {
	
    /**
     * UNKNOWN indicates that the command could not be executed (no results). 
     */
    public static CommandStatusType UNKNOWN = new CommandStatusType(-1, "UNKNOWN");
	
	/**
	 * When used with Command processing, SUCCESS indicates that results could be generated, with no warnings.
	 */
	public static CommandStatusType SUCCESS = new CommandStatusType(0, "SUCCESS");
	
	/**
	 * WARNING indicates that partial results were generated, but which may be in
	 * error due to initialization or runtime errors.
	 */
	public static CommandStatusType WARNING = new CommandStatusType(1, "WARNING");
	
	/**
	 * FAILURE indicates that the command could not be executed (no results). 
	 */
	public static CommandStatusType FAILURE = new CommandStatusType(2, "FAILURE");
    
    /**
	 * Used to set severity.
	 * @uml.property  name="__type"
	 */
	private int __type;
	/**
	 * Type name, e.g., "SUCCESS", "FAILURE".
	 * @uml.property  name="__typename"
	 */
	private String __typename;
	
	/**
	 * Construct the status type using the type/severity and name.  It is
	 * private because other code should use the predefined instances.
	 * @param type
	 * @param typename
	 */
	private CommandStatusType ( int type, String typename ){
		__type = type;
		__typename = typename;
	}
	
	/**
	 * Determine if two types are equal.
	 */
	public boolean equals ( CommandStatusType type )
	{
		if ( __type == type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Return the severity of the status (larger number means more severe problem).
	 * This is useful for ranking the severity of problems for output.
	 * @return the severity of the problem.
	 */
    public int getSeverity()
      {
        return __type;
      }
    
	/**
	 * Return the severity of the status (larger number means more severe problem).
	 * @return the severity of the problem.
	 * @deprecated Use getSeverity().
	 */
    public int getPriority()
      {
        return getSeverity();
      }
    
	/**
	 * Determine if a status severity is greater than the current status.
	 * For example, use this to check whether a command status type is greater than
	 * CommandStatusType.SUCCESS.
	 * @param type Command status severity.
	 * @return true if the provided severity is greater than that of the instance.
	 */
	public boolean greaterThan ( CommandStatusType type )
	{
		if ( __type > type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Determine if a status severity is greater than or equal to the current status.
	 * For example, use this to check whether a command status type is greater than
	 * or equal to CommandStatusType.WARNING.
	 * @param type Command status severity.
	 * @return true if the provided severity is greater than that of the instance.
	 */
	public boolean greaterThanOrEqualTo ( CommandStatusType type )
	{
		if ( __type >= type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	Determine the maximum severity.
	@return the status that is the most severe from the two status.
	*/
	public static CommandStatusType maxSeverity ( CommandStatusType status1, CommandStatusType status2 )
	{
		int severity1 = status1.getSeverity();
		int severity2 = status2.getSeverity();
		if ( severity1 > severity2 ) {
			return status1;
		}
		else {
			return status2;
		}
	}
    
	/**
	 * Return a String representation of the command status, as follows:
	 * <pre>
	 * UNKNOWN - status is unknown (not implemented or not initialized)
	 * SUCCESS - command was successful (no WARNING or FAILURE)
	 * WARNING - command completed but user should review possible problem
	 * FAILURE - command failed and results are very likely not complete or accurate
	 * </pre>
	 */
	public String toString () {
		return __typename;
	}
}
