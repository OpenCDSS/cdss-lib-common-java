// SimpleBrowser - a simple browser for displaying HTML

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

/*****************************************************************************
 * SimpleBrowser.java
 * Author:kat	Date: 2007-03-13
 * A Simple browser for displaying HTML.
 * <p>
 * Useful for displaying URLs, HTML files or text files.
 * 
 * <ul>
 * <li>Provides an address field for specifying URLs 
 * <li>a JEditorPane to display the HTML page,
 * <li>a status bar to display hyperlink addresses
 * <li>a button to launch the user's browser
 * <li>navigation buttons 
 * <li>print button
 * <ul>
 
 * REVISIONS:
 * 2007-03-13	Kurt Tometich	Initial version.
 * 2007-10-30   Dean Enix       Revise comfor javadoc
 ****************************************************************************/
package RTi.Util.GUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.Util.IO.DocumentRenderer;
import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
/**
* A Simple browser for displaying HTML.
* <p>
* Useful for displaying URLs, HTML files or text files.
* 
* <ul>
* <li>Provides an address field for specifying URLs 
* <li>a JEditorPane to display the HTML page,
* <li>a status bar to display hyperlink addresses
* <li>a button to launch the user's browser
* <li>navigation buttons 
* <li>print button
* <ul>
* */
public class SimpleBrowser extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JEditorPane __jep_JEditorPane;
	private JButton __back_JButton, __top_JButton, __print_JButton,
	__browser_JButton;
									// Buttons used to navigate to other
									// URL's.  __back is used to go to
									// the previous URL.  __top is used
									// to jump to the original URL.  Top
									// should only be used for check files
									// because it allows the user to jump to
									// the top of the check file.  If the
									// browser is being used to navigate HTML
									// pages, then this button should be
									// disabled.  The __print button is used
									// to print the current graphic or text
									// of the JEditorPane.
									// The __browser button is only available
									// when content is HTML.  When clicked,
									// the browser button will open the default
									// browser on the local machine to the same
									// URL as in the editor pane.
	
	private JTextField __urlField_JTextField;	
									// Contains the current URL and can
									// be used as an address toolbar
									// similar to IE or Firefox.
	
	private String __top_url;		// Stores the original URL navigated.
	private String __prev_url;		// Stores the previous URL
									// that was navigated.

	/**
	 * Creates a simple browser displaying the specified URL.
	 * <p>
	 * Usage:
	 * <br>
	 *  A valid URL is of the form "http://..." or a file name.
	 *  <pre>
	 * Ex 1: SimpleBrowser browse = new SimpleBrowser("C:/tmp/file.html")
	 * Ex 2: SimpleBrowser browse = new SimpleBrowser("http://www.google.com")
	 * </pre>
	 * @param startingUrl URL to be initially displayed
	 * @throws IOException if URL is not found
	 */
public SimpleBrowser(String startingUrl ) throws IOException
{	
	// Set the title as the starting URL (reset below)
	super( startingUrl );
	// Check the url given
    startingUrl = checkURL( startingUrl );
	// Set the title bar information
    JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
    if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
        setTitle ( "HTML Viewer - " + startingUrl );
    }
    else {
        setTitle( JGUIUtil.getAppNameForWindows() + " - HTML Viewer - " + startingUrl );
    }

	__top_url = startingUrl;
	__prev_url = startingUrl;
	
	setSize(700,500);
	// set the frame location to the middle of the screen for now
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension window = this.getSize();
    this.setLocation((screen.width - window.width) / 2, 
    	(screen.height - window.height) / 2);
	setDefaultCloseOperation( DISPOSE_ON_CLOSE );
	
	// Now set up our basic screen components, the editor pane, the
	// text field for URLs, and the label for status and link information.
	JPanel urlPanel = new JPanel();
	urlPanel.setLayout(new BorderLayout());
	JPanel buttonPanel = new JPanel();
	//buttonPanel.setLayout(new BorderLayout());
	buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
	
	// text field for url 
	// TODO KAT 2007-02-22
	// Look into changing this to a JComboBox
	// to hold previous URL's visited.
	__urlField_JTextField = new JTextField( __top_url );
	// back button used to navigate to previous location
	__back_JButton = new JButton("Back");
	__back_JButton.setEnabled(false);
	__back_JButton.setActionCommand("back");
	__back_JButton.addActionListener(this);
	__back_JButton.setToolTipText("Go to previous location in file.");
	// top button to navigate to the top of the page
	__top_JButton = new JButton("Top");
	__top_JButton.setActionCommand("top");
	__top_JButton.addActionListener(this);
	__top_JButton.setToolTipText("Go to top of file.");
	// used to print contents of the JEditorPane
	__print_JButton = new JButton("Print");
	__print_JButton.setActionCommand("print");
	__print_JButton.addActionListener(this);
	__print_JButton.setToolTipText("Print file.");
	//	 launches the default browser to current url
	__browser_JButton = new JButton("Browser");
	__browser_JButton.setActionCommand("browser");
	__browser_JButton.addActionListener(this);
	__browser_JButton.setToolTipText("View content in external default web browser.");
	
	// add buttons to button panel
	buttonPanel.add( __browser_JButton );
	buttonPanel.add( __top_JButton );
	buttonPanel.add( __back_JButton );
	buttonPanel.add( __print_JButton );
	
	urlPanel.add(new JLabel("Address: "), BorderLayout.WEST);
	urlPanel.add(__urlField_JTextField, BorderLayout.CENTER);
	urlPanel.add(buttonPanel, BorderLayout.EAST);
	//urlPanel.add(back, BorderLayout.EAST);
	final JLabel statusBar = new JLabel(" ");
	
	// The following is the editor pane configuration. It’s important to 
	//make the "setEditable(false)" call. Otherwise, hyperlinks won’t
	// work. (If the text is editable, then clicking on a hyperlink
	// simply means that the text should be changed, not to follow the
	// link.)
	__jep_JEditorPane = new JEditorPane();
	__jep_JEditorPane.setEditable(false);
	__jep_JEditorPane.setContentType( "text/html" );
	__jep_JEditorPane.setPage( __top_url );
	
	JScrollPane jsp = new JScrollPane(__jep_JEditorPane);
	// and get the GUI components onto our content pane
	getContentPane().add(jsp, BorderLayout.CENTER);
	getContentPane().add(urlPanel, BorderLayout.NORTH);
	getContentPane().add(statusBar, BorderLayout.SOUTH);
	
	// hook up the event handlers
	__urlField_JTextField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			try {
				__jep_JEditorPane.setPage(ae.getActionCommand());
				SimpleLinkListener.addToPreviousUrl(__prev_url);
				if ( !__back_JButton.isEnabled())
					__back_JButton.setEnabled(true);
				__prev_url = ae.getActionCommand();
			}
			catch(Exception e) {
				statusBar.setText("Couldn't navigate to URL:" +
					__prev_url + ".  See log for details.");
				Message.printWarning(3, "SimpleBrowser.SimpleBrowser", e);
			}
		}
	});
	__jep_JEditorPane.addHyperlinkListener(new SimpleLinkListener(
		__jep_JEditorPane, __urlField_JTextField, 
		statusBar, __back_JButton));
	
	// Display the frame
	this.setVisible(true);
}

// TODO smalers 2019-05-28 figure out deprecated method
/**
Checks if the given URL is valid and adds protocol if needed.
<p>
Protocol is either file: or http://.
@param url URL to check.
@return Newly formed URL with protocol.
@throws MalformedURLException if the URL given is invalid.
 */
@SuppressWarnings("deprecation")
private String checkURL(String url) throws MalformedURLException
{
	if (!(url.startsWith("http:") || url.startsWith("file:"))) {
		// If it’s not a fully qualified url, assume it’s a file
		if (url.startsWith("/")) {
			// Absolute path, so just prepend "file:"
			url = "file:" + url;
		}
		else {
			// assume it’s relative to the starting point...
			File f = new File(url);
			url = f.toURL().toString();
		}
	}
	return url;
}

// TODO smalers 2019-05-28 need to udpate to remove deprecation
/**
 * Handles actions events for the back, top, and print buttons.
 * @param event User triggered event.  
*/
@SuppressWarnings("deprecation")
public void actionPerformed( ActionEvent event ) 
{
	// back button was clicked by user
	if( event.getActionCommand().equals("back") ) {
		String prev_url = SimpleLinkListener.getPreviousUrl();
		try {
			if( !prev_url.equals("") || prev_url.length() > 0 ) {
				__jep_JEditorPane.setPage( prev_url );
				__urlField_JTextField.setText( prev_url );
			}
			// if list is empty then disable back button
			if( SimpleLinkListener.getPreviousUrlSize() == 0 ) {
				__back_JButton.setEnabled(false);
			}	
		} catch (IOException e) {
			Message.printWarning(2, "SimpleBrowser.actionPerformed",
					"Couldn't set the page back to url:" +
					prev_url);
		}
	}
	// top button was clicked by user
	else if( event.getActionCommand().equals("top") ) {
		try {
			__jep_JEditorPane.setPage( __top_url );
			__urlField_JTextField.setText( __top_url );
		} catch (IOException e) {
			Message.printWarning(2, "SimpleBrowser.actionPerformed",
				"Couldn't set the page back to url:" +
				__top_url);
		}
	}
	// print button was clicked by user
	else if ( event.getActionCommand().equals("print")) {
		DocumentRenderer dr = new DocumentRenderer();
		dr.print( __jep_JEditorPane );	
	}
	// browser button was clicked by user
	else if ( event.getActionCommand().equals("browser")) {
		IOUtil.openURL( __top_url );
	}
}

//public static void main(String args[]) 
//{
//  try
//  {
//    SimpleBrowser browse = new SimpleBrowser("http://www.google.com");
//  }
//  catch (IOException e)
//  {
//    System.out.println( e.getMessage());
//  }
//}
}


