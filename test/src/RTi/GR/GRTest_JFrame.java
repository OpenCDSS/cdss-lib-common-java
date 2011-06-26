package RTi.GR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.awt.print.PageFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import RTi.GR.GRLimits;

import RTi.Util.GUI.JGUIUtil;

import RTi.Util.IO.PrintUtil;

/**
This class is a JFrame to run GR tests.
*/
public class GRTest_JFrame extends JFrame implements WindowListener
{

/**
The device on which the drawing will take place.
*/
private GRTest_Device __device;

/**
The drawing area that the device will use for drawing.
*/
private GRTest_DrawingArea __drawingArea;

/**
The message field that appears at the bottom of the JFrame.
*/
private JTextField __messageField;

/**
The status field that appears at the bottom of the JFrame.
*/
private JTextField __statusField;

/**
Constructor.
*/
public GRTest_JFrame() {
	super("GRTest");

	setupGUI();
}

/** 
Closes the window.
*/
public void closeWindow() {
	setVisible(false);
	System.exit(0);
}

/**
Returns the page format for which the drawing is being formatted.
@return the page format for which the drawing is being formatted.
*/
public PageFormat getPageFormat() {
	PageFormat pageFormat = PrintUtil.getPageFormat("letter");
	PrintUtil.setPageFormatOrientation(pageFormat, PageFormat.LANDSCAPE);
	try {
		PrintUtil.setPageFormatMargins(pageFormat, .5, .5, .5, .5);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	return pageFormat;
}

/**
Sets the message in the status bar of the parent JFrame.  This can be a 
short sentence as the message field is the longer one.  This can also be null,
in which case the message text field will be cleared.
@param text the text to put in the message field.
*/
public void setMessage(String text) {
	if (__messageField != null) {
		if (text != null) {
			__messageField.setText(text);
		}
		else {
			__messageField.setText("");
		}
		JGUIUtil.forceRepaint(__messageField);
	}
}

/**
Sets up a message (which can be long) and a status message (which should 
generally be one word) in the status bar of the parent JFrame.
@param message the message to set in the status bar (can be null, in which 
case the message text field will be cleared).
@param status the status to set in the status bar (can be null, in which 
case the status text field will be cleared).
*/
public void setMessageStatus(String message, String status) {
	setMessage(message);
	setStatus(status);
}

/**
Sets the status message in the status bar of the parent JFrame.  This should
generally just be one word.  This can also be null, in which the case the
status text field will be cleared.
@param status the status to set in the status text field.
*/
public void setStatus(String status) {
	if (__statusField != null) {
		if (status != null) {
			__statusField.setText(status);
		} 
		else {
			__statusField.setText("");
		}
		JGUIUtil.forceRepaint(__statusField);		
	}
}

/**
Sets up the GUI.
*/
private void setupGUI() {
	JGUIUtil.setSystemLookAndFeel(true);

	addWindowListener(this);

	double scale = 1;
	PageFormat pageFormat = getPageFormat();
	int hPixels = (int)(pageFormat.getWidth() / scale);
	int vPixels = (int)(pageFormat.getHeight() / scale);	

	__device = new GRTest_Device(this, scale);
	__device.setPreferredSize(new Dimension(hPixels, vPixels));
	GRLimits drawingLimits = new GRLimits(0.0, 0.0, hPixels, vPixels);
	GRLimits dataLimits = new GRLimits(0, 0, 100, 100);
	__drawingArea = new GRTest_DrawingArea(__device, drawingLimits, dataLimits);
	__drawingArea.setDataLimits(dataLimits);
	__device.setDrawingArea(__drawingArea);
	__device.setDrawingLimits(drawingLimits);
	__device.setDataLimits(dataLimits);

	getContentPane().add(new JScrollPane(__device));

	JPanel bottom = new JPanel();
	bottom.setLayout(new GridBagLayout());
	__statusField = new JTextField(10);
	__messageField = new JTextField(10);
	__statusField.setEditable(false);
	__messageField.setEditable(false);

	JGUIUtil.addComponent(bottom, __messageField,
		0, 0, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.WEST);	
	JGUIUtil.addComponent(bottom, __statusField,
		1, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.WEST);		
	getContentPane().add("South", bottom);	

	__messageField.setText("");
	__statusField.setText("READY");

	setSize(800, 700);
	JGUIUtil.center(this);

	pack();

	setVisible(true);
}

/**
Does nothing.
*/
public void windowActivated(WindowEvent event) {}

/**
Responds to window closed events; calls closeWindow().
@param event the WindowEvent that happened.
*/
public void windowClosed(WindowEvent event) {
	closeWindow();
}

/**
Responds to window closing events; calls closeWindow().
@param event the WindowEvent that happened.
*/
public void windowClosing(WindowEvent event) {
	closeWindow();
}

/**
Does nothing.
*/
public void windowDeactivated(WindowEvent event) {}

/**
Does nothing.
*/
public void windowDeiconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowIconified(WindowEvent event) {}

/**
Does nothing.
*/
public void windowOpened(WindowEvent event) {}

public static void main(String[] args) {
	//GRTest_JFrame g =
    new GRTest_JFrame();
}

}