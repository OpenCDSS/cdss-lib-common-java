/*****************************************************************************
 * SimpleLinkListener.java
 * Author:kat	Date: 2007-03-13
 * A hyperlink listener for use with JEditorPane. This
 * listener changes the cursor over hotspots based on enter/exit
 * events and also load a new page when a valid hyperlink is clicked.
 * Keeps track of previous pages or links visited.
 * 
 * REVISIONS:
 * 2007-03-13	Kurt Tometich	Initial version.
 ****************************************************************************/
package RTi.Util.GUI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import RTi.Util.Message.Message;

/**
 Class provides a LinkListener to update the pages for a JEditorPane
 whose content is rendered HTMl.  This clas also keeps track of the
 pages and links visited so that back and forward buttons can be used
 in the JEditorPane.
 */
public class SimpleLinkListener implements HyperlinkListener {
	private JEditorPane jep_JEditorPane; // The pane we’re using to display HTML
	private JTextField url_TextField; // An optional text field for showing
	// the current URL being displayed
	private JLabel statusBar_JTextField; // An optional label for showing where
	// a link would take you
	private JButton back_JButton;	// button that needs to be updated
	// when user navigates to new page or section
	private static ArrayList __urls; 
	
/**
Constructor to initialize SimpleLinkListener class. 
@param jep JEditorPane to use.
@param jtf Text field to update current link.  
@param jl Label used for JEditorPane.
@param back Button to mimic browser back button for current JEditorPane.
*/
public SimpleLinkListener(JEditorPane jep, JTextField jtf, 
JLabel jl, JButton back) {
		jep_JEditorPane = jep;
		url_TextField = jtf;
		statusBar_JTextField = jl;
		back_JButton = back;
		__urls = new ArrayList();
}

/**
Another constructor to initialize SimpleLinkListener class. 
@param jep JEditorPane to use.
@param back Button to mimic browser back button for current JEditorPane.
 */
public SimpleLinkListener(JEditorPane jep, JButton back) {
		this(jep, null, null, back);
}

/**
Handles any hyperlink event caused by the user clicking a hyperlink. 
@param he Event that occurs when user clicks a hyperlink.
 */
public void hyperlinkUpdate(HyperlinkEvent he) {
	HyperlinkEvent.EventType type = he.getEventType();
	if (type == HyperlinkEvent.EventType.ENTERED) {
		// Enter event. Fill in the status bar
		if (statusBar_JTextField != null) {
			statusBar_JTextField.setText(he.getURL().toString());
		}
	}
	else if (type == HyperlinkEvent.EventType.EXITED) {
		// Exit event. Clear the status bar
		if (statusBar_JTextField != null) {
			statusBar_JTextField.setText(" "); 
			// must be a space or JTextField disappears
		}
	}
	else {
		// Jump event. Get the url, and if it’s not null, switch to that
		// page in the main editor pane and update the "site url" label.
		if (he instanceof HTMLFrameHyperlinkEvent) {
		// frame event... handle this separately
		HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)he;
			HTMLDocument doc = (HTMLDocument)jep_JEditorPane.getDocument();
			doc.processHTMLFrameHyperlinkEvent(evt);
		} else {
			try {
				// set the previous url for back button 
				// and update page with new URL
				__urls.add(url_TextField.getText());
				jep_JEditorPane.setPage(he.getURL());
				back_JButton.setEnabled(true);
				if (url_TextField != null) {
					url_TextField.setText(he.getURL().toString());
				}
			}
			catch (FileNotFoundException fnfe) {
				Message.printWarning(2, "SimpleLinkListener.HyperLinkUpdate",
					"File: " + he.getURL().toString() + " was not found.");
			} catch (IOException e) {
				Message.printWarning(2, "SimpleLinkListener.HyperLinkUpdate",
					"Couldn't set text from file: " + he.getURL().toString());
				Message.printWarning(3, 
					"SimpleLinkListener.HyperLinkUpdate", e);
			}
		}
	}
}

/**
Returns the previous URL that was navigated by the user.
@return previous_url Previous URL navigated.
*/
public static String getPreviousUrl() {
	
	String previous_url = "";
	// check internal list of urls
	if(__urls.size() > 0 ) {
		int last_index = __urls.size() - 1;
		previous_url = (String)__urls.get( last_index );
		__urls.remove(last_index);
	}
	return previous_url;
}

/**
Returns the current size of the previously navigated URL list.
Used to enable the back button used in a JEditorPane. 
@return Size of previously navigated URL list.
 */
public static int getPreviousUrlSize()
{
	return __urls.size();
}

/**
Adds a URL to the previous URL list used by the back button.
@param url Url to add.
 */
public static void addToPreviousUrl( String url )
{
	if ( url != null ) {
		__urls.add(url);
	}
}

}

