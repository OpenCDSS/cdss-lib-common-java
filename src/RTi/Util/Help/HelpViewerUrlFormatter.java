// HelpViewerUrlFormatter - interface for class that will format a URL for the HelpViewer

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

package RTi.Util.Help;

/**
 * Interface for class that will format a URL for the HelpViewer.
 * @author sam
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

	/**
	 * Format a URL to display help for a topic.
	 * @param group a group (category) to organize items.
	 * For example, the group might be "command".
	 * @param item the specific item for the URL.
	 * For example, the item might be a command name.
	 * @param rootUrl root URL that is an alternative to the built-in help location,
	 * for example used by plugins that provide their own documentation URL
	 */
	public String formatHelpViewerUrl ( String group, String item, String rootUrl );
	
}