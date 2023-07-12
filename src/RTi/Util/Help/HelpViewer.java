// HelpViewer - this class manages displaying help for an application

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.ProcessManager;
import RTi.Util.Message.Message;

/**
 * This class manages displaying help for an application.
 * It does so by handling setup of UI components such as dialogs with a help button and when help is requested,
 * showing the help in the default browser.
 *
 * The HelpManager is a singleton that is requested with getInstance().
 * The application code should call 'setUrlFormatter' at startup and implement the HelpViewerUrlFormatter to
 * format help URLs for the application documentation.
 */
public class HelpViewer {

	/**
	 * Singleton instance of the HelpManager.
	 */
	private static HelpViewer helpViewer = null;

	/**
	 * Interface implementation to format the URL.
	 * This allows application-specific help URLs to be formatted.
	 */
	private HelpViewerUrlFormatter urlFormatter = null;

	/**
	 * Constructor for default instance.
	 */
	public HelpViewer () {
	}

	/**
	 * Return the singleton instance of the HelpManager.
	 */
	public static HelpViewer getInstance() {
		if ( helpViewer == null ) {
			helpViewer = new HelpViewer();
		}
		return helpViewer;
	}

	/**
	 * Set the object that will format URLs for the viewer.
	 * This is typically called application code that has knowledge of the documentation organization.
	 * @param urlFormatting an instance of HelpViewerUtlFormatting that will format URLs for a documentation page
	 */
	public void setUrlFormatter(HelpViewerUrlFormatter urlFormatter) {
		this.urlFormatter = urlFormatter;
	}

	/**
	 * Show the help using a web browser, using the default documentation home.
	 * @param group the group to which the item belongs, will be passed to HelpViewerUrlFormatter().formatUrl().
	 * For example, when used with TSTool command documentation, this is "command".
	 * @param item the item for which to display help, will be passed to HelpViewerUrlFormatter().formatUrl().
	 * For example, when used with TSTool command documentation, this is the command name.
	 */
	public void showHelp ( String group, String item ) {
		showHelp ( group, item, null );
	}

	/**
	 * Show the help using a web browser.
	 * @param group the group to which the item belongs, will be passed to HelpViewerUrlFormatter().formatUrl().
	 * For example, when used with TSTool command documentation, this is "command".
	 * @param item the item for which to display help, will be passed to HelpViewerUrlFormatter().formatUrl().
	 * For example, when used with TSTool command documentation, this is the command name.
	 * @param rootUrl the root URL for documentation, can be used for plugins when standard TSTool documentation is not used,
	 * use null to use the default documentation home set at application startup
	 */
	public void showHelp ( String group, String item, String rootUrl ) {
		String routine = "showHelp";
		// Use the default web browser application to display help.
		if ( this.urlFormatter == null ) {
	    	Message.printWarning(1, "",
		    	"Unable to display documentation for group \"" + group + "\" and item \"" + item + "\" - no URL formatter defined." );
		}
		else {
			// Format the URL for the item.
			String docUri = this.urlFormatter.formatHelpViewerUrl(group, item, rootUrl);
			if ( docUri == null ) {
				Message.printWarning(1, "", "Unable to determine documentation URL for group=\"" + group + "\", item=\""
					+ item + "\", rootUrl=\"" + rootUrl + "\"." );
			}
	        // Now display using the default application for the file extension.
	        Message.printStatus(2, routine, "Opening documentation \"" + docUri + "\"" );
			// Use the desktop to display documentation.
			if ( !Desktop.isDesktopSupported() ) {
				if ( IOUtil.isUNIXMachine() ) {
					// Only try on Linux since Windows Desktop seems to work OK.
					showHelpRunBrowserUnix(docUri);
				}
				else {
					Message.printWarning(1, "", "Opening browser from software not supported.  View the following in a browser: " + docUri );
				}
			}
			else {
		        // The Desktop.browse() method will always open, even if the page does not exist,
		        // and it won't return the HTTP error code in this case.
		        // Therefore, do a check to see if the URI is available before opening in a browser.
		        try {
		            Desktop desktop = Desktop.getDesktop();
		            desktop.browse ( new URI(docUri) );
		        }
		        catch ( Exception e ) {
		        	if ( IOUtil.isUNIXMachine() ) {
					   	// Only try on Linux since Windows Desktop seems to work OK.
					   	showHelpRunBrowserUnix(docUri);
				    }
		        	else {
		        		Message.printWarning(2, "", "Unable to display documentation at \"" + docUri + "\" (" + e + ")." );
		        	}
		        }
			}
	    }
	}

	/**
	 * Show help by running a browser.
	 * This is typically used on Linux because Desktop.browse() does not seem to be supported.
	 */
	private void showHelpRunBrowserUnix ( String docUri ) {
		boolean browserOk = false;
		// First try using xdg-open.
		Message.printWarning(2, "", "Attempting call to xdg-open." );
		try {
			ProcessManager pm = new ProcessManager("xdg-open " + docUri);
			Thread thread = new Thread ( pm );
			thread.start ();	// This executes the run() method in ProcessManager.
			browserOk = true;
			// TODO smalers 2019-10-17 maybe there is a way to check the xdg-open command,
			// but not sure how exit status for a thread would work.
		}
		catch ( Exception e ) {
			Message.printWarning(3, "",  "Error running: xdg-open " + docUri);
			browserOk = false;
		}
		if ( !browserOk ) {
			Message.printWarning(2, "", "Attempting direct call to browsers." );
			// Try running browser directly.
			List<String> browsers = new ArrayList<>();
			browsers.add("chromium");
			browsers.add("chromium.exe");
			browsers.add("firefox");
			browsers.add("firefox.exe");
			for ( String browser : browsers ) {
				// Find the browser in the path.
				File programFile = IOUtil.findProgramInPath(browser);
				if ( programFile != null ) {
					try {
						ProcessManager pm = new ProcessManager(browser + " " + docUri);
						Thread thread = new Thread ( pm );
						thread.start ();	// This executes the run() method in ProcessManager.
						browserOk = true;
						break;
					}
					catch ( Exception e ) {
						Message.printWarning(3, "", "Error running: " + browser + " " + docUri);
						browserOk = false;
					}
				}
			}
		}
		if ( !browserOk ) {
			Message.printWarning(1, "", "Opening browser from software not supported and unable to open in browser." +
			   "  View the following in a browser: " + docUri );
		}
	}

}