package RTi.Util.IO;

/**
This interface defines the implementation to provide a status from a Command.
The intent is to allow legacy Command implementations to begin to implement
CommandStatusProvider during transition to new user interface capabilities.
*/
public interface CommandStatusProvider {
	
	/**
	 * Return the command status as a CommandStatus object.
	 * @return the command status as a CommandStatus object.
	 */
	public CommandStatus getCommandStatus();
}
