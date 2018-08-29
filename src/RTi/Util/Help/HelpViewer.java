package RTi.Util.Help;

import java.awt.Desktop;
import java.net.URI;

import RTi.Util.Message.Message;

/**
 * This class manages displaying help for an application.
 * It does so by handling setup of UI components such as dialogs with a help button
 * and when help is requested, showing the help in the default browser.
 * The HelpManager is a singleton that is requested with getInstance().
 * @author sam
 *
 */
public class HelpViewer {
	
	/**
	 * Singleton instance of the HelpManager.
	 */
	private static HelpViewer helpViewer = null;
	
	/**
	 * Interface implementation to format the URL.
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
	 */
	public void setUrlFormatter(HelpViewerUrlFormatter urlFormatter) {
		this.urlFormatter = urlFormatter;
	}
	
	/**
	 * Show the help using a web browser.
	 * @param group the group to which the item belongs, will be passed to HelpViewerUrlFormatter().formatUrl().
	 * @param item the item for which to display help, will be passed to HelpViewerUrlFormatter().formatUrl().
	 */
	public void showHelp ( String group, String item ) {
		String routine = "showHelp";
		// Use the default web browser application to display help.
		if ( this.urlFormatter == null ) {
	    	Message.printWarning(1, "",
		    	"Unable to display documentation for group \"" + group + "\" and item \"" + item + "\" - no URL formatter defined." );
		}
		else {
			// Format the URL for the item
			String docUri = this.urlFormatter.formatHelpViewerUrl(group, item);
			if ( docUri == null ) {
				Message.printWarning(1, "", "Unable to determine documentation URL for group=\"" + group + "\", item=\"" + item + "\"." );
			}
	        // Now display using the default application for the file extension
	        Message.printStatus(2, routine, "Opening documentation \"" + docUri + "\"" );
			// If 
			if ( !Desktop.isDesktopSupported() ) {
				Message.printWarning(1, "", "Opening browser from software not supported.  View the following in a browser: " + docUri );
			}
			else {
		        // The Desktop.browse() method will always open, even if the page does not exist,
		        // and it won't return the HTTP error code in this case.
		        // Therefore, do a check to see if the URI is available before opening in a browser
		        try {
		            Desktop desktop = Desktop.getDesktop();
		            desktop.browse ( new URI(docUri) );
		        }
		        catch ( Exception e ) {
		            Message.printWarning(2, "", "Unable to display documentation at \"" + docUri + "\" (" + e + ")." );
		        }
			}
	    }
	}

}