package RTi.Util.Help;

/**
 * Interface for class that will format a URL for the HelpViewer.
 * @author sam
 *
 */
public interface HelpViewerUrlFormatter {

	/**
	 * Format a URL to display help for a topic.
	 * @param group a group (category) to organize items.
	 * For example, the group might be "command".
	 * @param item the specific item for the URL.
	 * For example, the item might be a command name.
	 */
	public String formatHelpViewerUrl ( String group, String item );
	
}