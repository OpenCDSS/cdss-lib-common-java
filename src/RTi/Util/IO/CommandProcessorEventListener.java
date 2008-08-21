package RTi.Util.IO;

/**
 * This listener provides the interface for handling CommandProcessorEvent instances, which
 * are generated during command processing.
 * @author sam
 *
 */
public interface CommandProcessorEventListener {

    /**
     * Handle a CommandProcessorEvent.
     * @param event CommandProcessorEvent to handle.
     */
    public void handleCommandProcessorEvent ( CommandProcessorEvent event );
}
