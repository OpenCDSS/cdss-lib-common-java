// ----------------------------------------------------------------------------
// ProgressJDialog - dialog box that displays an updatable progress bar that
//	runs in a Thread.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:	
// 2003-07-24	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
This class is a dialog box that can be used to track the completion of a 
process.  The dialog runs in a thread so it can be updated while other processes
are going on.
<p>
<b>Using ProgressJDialog</b><p>
Instantiate the class with the name of the parent frame (the dialog is opened
modally), the title for the dialog, and the min and max values.<p>
<blockquote><pre>
	ProgressJDialog progress = new ProgressJDialog(this, 
		"Copy Progress", 0, numberFilesToBeCopied);
</pre></blockquote><p>

Every time some counter has been updated, call setProgressBarValue to set the
current value (which should be between min and max, as defined in the
constructor).<p>
<blockquote><pre>
	for (int i = 0; i &lt; numberFilesToBeCopied; i++) {
		copyFileToNewLocation(files[i], newLocation[i]);
		progress.setProgressBarValue(i + 1);
	}
</pre></blockquote>
*/
public class ProgressJDialog 
extends JDialog 
implements Runnable {

/**
The progress bar displayed in the dialog box.
*/
private JProgressBar __progressBar;

/**
Constructor.
@param parent the JFrame on which this dialog appears.  The dialog is opened
modally.
@param title the title string for the dialog.
@param min the minimum value for computing the % complete in the progress bar.
@param max the maximum value for computing the % complete in the progress bar.
*/
public ProgressJDialog(JFrame parent, String title, int min, int max) {
	super(parent, title, false);

	__progressBar = new JProgressBar(min, max);
	__progressBar.setValue(0);
	__progressBar.setIndeterminate(false);
	__progressBar.setStringPainted(true);
	setupGUI();
	new Thread(this).start();	
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__progressBar = null;
	super.finalize();
}

/**
Runs the thread.
*/
public void run() {}

/**
Sets the value of the progress bar.  The value should be &gr;= to the min value
passed in to the constructor and &lt;= to the max value passed in to the 
constructor.
@param value the value to set the progress bar completion amount to.
*/
public void setProgressBarValue(int value) {
	__progressBar.setValue(value);
	Rectangle rect = getBounds();
	rect.x = 0;
	rect.y = 0;
	__progressBar.paintImmediately(rect);
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	GridBagLayout gbl = new GridBagLayout();

	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(gbl);

	JGUIUtil.addComponent(mainPanel, __progressBar,
		0, 0, 1, 1, 1, 1, 
		0, 0, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.CENTER);

	getContentPane().add(mainPanel);

	pack();
	JGUIUtil.center(this);
}

}
