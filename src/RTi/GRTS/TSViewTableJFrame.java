//------------------------------------------------------------------------------
// TSViewTableJFrame - tabular View of one or more time series
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 05 Dec 1998	Steven A. Malers,	Initial version.  Copy OpTableDislayGUI
//		Riverside Technology,	and modify as necessary.
//		inc.
// 30 Oct 2000	Catherine E.		Implemented MultiLists, checkboxes,
//		Nutting-Lane		and export button.
// 19 Feb 2001	SAM, RTi		Change GUI to GUIUtil.
// 28 Mar 2001	SAM, RTi		Fix so headers use location, data type,
//					and units if the legend is not set.
// 27 Apr 2001	SAM, RTi		Fix so hourly data does not print
//					minutes in output.  Add finalize().
// 04 May 2001	SAM, RTi		Add TSViewTitleString property to set
//					view title bar.  Add OutputPrecision
//					property to control precision of output.
//					Streamline handling of precision.
//					Use TSIterator()instead of
//					TSDateIterator()- improve performance.
// 18 May 2001	SAM, RTi		Change Save As button to a choice.
// 23 May 2001	SAM, RTi		Add the ability to use the extended
//					legend to label the column headings
//					using the Table.UseExtendedLegend
//					property.
// 17 Jul 2001	SAM, RTi		Add a warning at startup if the number
//					of points is large.  Use 10 years of
//					daily data(3650 points)as an initial
//					value to see how this performs.
// 06 Sep 2001	SAM, RTi		Fix bug where first data value was not
//					being displayed.  The TSIterator next()
//					method was being called once when it
//					did not need to be.  Remove debugs that
//					are no longer needed.  Verify that hour
//					and minute data are fully supported.
//					Clean up the code some to be more
//					readable and help with garbage
//					collection.  Change so 24-hour data use
//					a date that has hours(CDSS daily time
//					series were switched from 24 hour to
//					true daily time series awhile back).
//					Add YearTS support.
// 2001-11-05	SAM, RTi		Update javadoc.  Make sure variables are
//					set to null when no longer used.
// 2001-12-11	SAM, RTi		Change help key to "TSView.Table".
// 2002-01-17	SAM, RTi		Change name from TSViewTableGUI to
//					TSViewTableFrame to allow support for
//					Swing.
// 2002-04-21	SAM, RTi		Reorganize the buttons to be Graph and
//					then Summary.
// ====================================
// 2002-11-11	SAM, RTi		Copy AWT version and convert to Swing.
// 2003-06-03	SAM, RTi		* Update based on current TS package.
//					* Synchronize with AWT by implementing:
//					2003-03-21	SAM, RTi
//					* If the time series has an alias,
//					  display that in the column heading
//					  rather than the location.
// 2003-07-10	J. Thomas Sapienza, RTi	Began implementing JWorksheets.
// 2003-07-16	JTS, RTi		Continued work.  Multiple time series
//					for a single time series type can be
//					displayed, and tables can be turned
//					on and off.
// 2003-07-24	JTS, RTi		* Added support for copying data from
//					  tables.
//					* Added support for writing text
//					  files.
//					* Selected worksheets are now
//					  highlighted on the GUI.
// 2003-07-30	JTS, RTi		* Added code to selectively turn on and
//					  off the title border for the interval
//					  panels based on how many panels are
//					  showing.
//					* Added code to only display the blue
//					  outline for selected worksheets if
//					  more than one worksheet is visible.
// 2003-08-14	JTS, RTi		* Change so that when all the 
//					  JCheckBoxes are deselected, the panel
//					  gets placed in the North position,
//					  instead of being centered in the
//					  Center position.
// 2003-09-30	SAM, RTi		* Add icon/title code to be consistent
//					  with the application look.
// 2003-11-04	JTS, RTi		* When moving between worksheets,
//					  code now attempts to save any data
//					  that was entered (if the cell editor
//					  was not closed).  If that fails,
//					  the cell editing is cancelled.
// 2004-01-04	SAM, RTi		* Comment out the help button for now.
// 2004-01-05	JTS, RTi		When only one worksheet is displayed,
//					it is now automatically selected once
//					the GUI is visible.
// 2004-01-22	JTS, RTi		Change to use the new row header model
//					for the worksheets.
// 2004-05-18	JTS, RTi		Added comment stating that viewing of
//					irregular data in the table is not
//					enabled yet.
// 2005-04-27	JTS, RTi		Added all data members to finalize().
// 2005-06-07	JTS, RTi		* Updated properties that are being 
//					  phased out of JWorksheet.
//					* Corrected an error where time series
//					  with the same interval, but different
//					  multipliers, were using the multiplier
//					  of the first-displayed set of time
//					  series.  They will now use the correct
//					  multiplier.
// 2005-07-07	JTS, RTi		Changed the status message at the bottom
//					when a time series table is selected.
// 2006-01-31	JTS, RTi		Worksheets now are listeners for scroll
//					events to solve a display glitch on 
//					UNIX.  For more information, see
//					JWorksheet.adjustmentValueChanged().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------

package RTi.GRTS;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import javax.swing.border.Border;

import RTi.Collections.MultiValueHash;
import RTi.TS.DateValueTS;
import RTi.TS.IrregularTS;
import RTi.TS.TS;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_Header;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Help.URLHelp;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeInterval;

/**
The TSViewTableJFrame provides a table view for a list of time series and is
managed by the TSViewJFrame parent.  The GUI can display multiple time series 
of different interval bases and multipliers.  All the TS of the same interval
base and multiplier are displayed in the same Worksheet.  Different 
interval multiplier TS with the same interval base are shown in worksheets 
next to other worksheets with the same interval base.  Minute TS are the top 
row, the next row are Hour TS, the next are Day TS, the next are Month TS and
the bottom row are Year TS.<p>
If there are no TS of a particular interval base (e.g., Day), the row for 
that kind of TS is not displayed.  If more than one interval base is to be 
displayed on the same form, a JCheckbox will appear for each interval base
so that it can be turned on or off.  If only one interval base appears on 
the form, no such Checkbox will appear.
*/
public class TSViewTableJFrame 
extends JFrame
implements ActionListener, ItemListener, MouseListener, PropertyChangeListener, WindowListener {

/**
The number of characters in the table double values.
*/
private final int __OUTPUT_WIDTH = 10;

/**
JButton and JPopupMenu Strings.
*/
private final String 
	__BUTTON_CLOSE = 	"Close",	
	__BUTTON_GRAPH = 	"Graph",
	__BUTTON_HELP = 	"Help",
	__BUTTON_SAVE = 	"Save",
	__BUTTON_SUMMARY = 	"Summary",
	__MENU_COPY = 		"Copy",
	__MENU_PASTE = 		"Paste";

/**
Boolean to keep track of whether the main JPanel has been removed from the 
Center position of the content pane and placed in the North position, or not.
The main panel will be moved to the north panel when the user has deselected
all the checkboxes on the form and has no TS interval types visible.
*/
private boolean __mainJPanelNorth = false;

/**
Whether worksheets should be built in displayed in the opposite of the normal
order, from minute to year instead of year to minute.
*/
private boolean __reverseNormalOrder = false;

/**
Boolean to keep track of whether the save button should always be enabled, 
no matter what.  This is only true if there is a single worksheet displayed
on the GUI.
*/
private boolean __saveAlwaysEnabled = false;

/**
The original border set up for a JScrollPane, before the border is changed
because the scrollpane's worksheet was selected.
*/
private Border __originalScrollPaneBorder = null;

/**
A counter that tracks how many panels and checkboxes have been added to the 
main panel, so that the Y placement in the grid bag can be done correctly.
*/
private int __panelCount = 0;

/**
Checkboxes corresponding to each TS data interval for turning on and off the 
table displays.
*/
private JCheckBox 
	__dayJCheckBox,
	__hourJCheckBox,
	__irregularJCheckBox,
	__minuteJCheckBox,
	__monthJCheckBox,
	__yearJCheckBox;

/**
The main panel into which the form components are added.
*/
private JPanel __mainJPanel;

/**
JPanels for holding JCheckBoxes and JWorksheets for each TS data interval.
*/
private JPanel
	__dayJPanel,
	__hourJPanel,
	__irregularJPanel,
	__minuteJPanel,
	__monthJPanel,
	__yearJPanel;

/**
JScrollPanes for each of the associated ts worksheets, in order to highlight
the proper JScrollPane border after a worksheet was clicked on and selected.
*/
private JScrollPane[] 
	__dayScrollPanes,
	__hourScrollPanes,
	__irregularScrollPanes,
	__minuteScrollPanes,
	__monthScrollPanes,
	__yearScrollPanes;

/**
The scroll pane of the last worksheet that was clicked on and selected.
*/
private JScrollPane __lastSelectedScrollPane = null;

/**
The status bar message text field.
*/
private JTextField __messageJTextField;

/**
Arrays containing all the different JWorksheets (one for each interval type)
for each different TS.
*/
private JWorksheet[]
	__dayWorksheets,
	__hourWorksheets,
	__irregularWorksheets,
	__minuteWorksheets,
	__monthWorksheets,
	__yearWorksheets;

/**
The last worksheet that was clicked on and selected.
*/
private JWorksheet __lastSelectedWorksheet = null;

/**
Properties for the table display gui.
*/
private PropList __props;

/**
Buttons that appear at the bottom of the GUI, for doing special operations.
*/
private SimpleJButton 
	__summaryJButton,
	__graphJButton,
	__closeJButton,
	__saveJButton;

/**
TSViewJFrame parent that displays this gui.
*/
private TSViewJFrame __tsviewJFrame;	

/**
Table models for each of the different worksheets (one for each interval type)
for each different TS.
*/
private TSViewTable_TableModel[]
	__dayModels,
	__hourModels,
	__irregularModels,
	__minuteModels,
	__monthModels,
	__yearModels;

/**
Vectors of the mouse listeners that have been set up for all of the different
kinds of worksheets.  These are used in order to find which worksheet was 
clicked on after a mouse press on the JScrollPane associated with a worksheet.
*/
private Vector[]
	__dayMouseListeners,
	__hourMouseListeners,
	__irregularMouseListeners,
	__minuteMouseListeners,
	__monthMouseListeners,
	__yearMouseListeners;

/**
Vectors of Time Series to be displayed in the GUI.  __tslist is set from a 
Vector passed in to this GUI at construction, and then the other Vectors are
formed from the TS split out of __tslist.
*/
private Vector	
	__day,
	__hour,
	__minute,
	__month,	
	__irregular,
	__tslist,
	__year;

/** Controls whether irregular TS scroll synchronously */
private JCheckBox __synchronizeCheckBox;
/** Flag whether irregular TS are to scroll synchronously */
private boolean __scrollIrregularSychnonously = false;

/**
Constructor.
@param tsviewJFrame Parent TSViewJFrame.
@param tslist List of time series to view.
@param props Properties for display (currently same list passed in to
TSViewJFrame).
*/
public TSViewTableJFrame(TSViewJFrame tsviewJFrame, Vector tslist, 
PropList props)
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );

	__tsviewJFrame = tsviewJFrame;
	__tslist = tslist;
	__props = props;

	String propValue = __props.getValue("TSViewTitleString");

	if ( propValue == null ) {
		if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Time Series - Table" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() +
			" - Time Series - Table" );
		}
	}
	else {	if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( propValue + " - Table" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() + " - " +
			propValue + " - Table" );
		}
	}

	propValue = __props.getValue("TSViewTableOrder");
	if (propValue != null) {
		if (propValue.equalsIgnoreCase("FineToCoarse")) {
			__reverseNormalOrder = true;
		}
	}
	setupGUI(true);	
}

/**
Handle action events from ActionListener.
@param event ActionEvent to handle.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(__BUTTON_CLOSE)) {
		__tsviewJFrame.closeGUI(TSViewJFrame.TABLE);
	}
	else if (command.equals(__BUTTON_GRAPH)) {
		__tsviewJFrame.openGUI(TSViewJFrame.GRAPH);
	}
	else if (command.equals(__BUTTON_HELP)) {
		URLHelp.showHelpForKey("TSView.Table");
	}
	else if (command.equals(__BUTTON_SUMMARY)) {
		__tsviewJFrame.openGUI(TSViewJFrame.SUMMARY);
	}
	else if (command.equals(__BUTTON_SAVE)) {
		saveClicked();
	}
	else if (command.equals(__MENU_COPY)) {
		__lastSelectedWorksheet.copyToClipboard();
	}
	else if (command.equals(__MENU_PASTE)) {
		__lastSelectedWorksheet.pasteFromClipboard();
	}
}

/**
Adds worksheets of the same interval base to their panel and also adds 
the appropriate check box, as long as more than one kid of interval base 
will be displayed on the GUI.
@param panel the main panel to which to add the panels (__mainJPanel).
@param subPanel the panel to which to add the checkbox and worksheets.
@param intervalDescription a text string that concisely names the kind of 
interval being dealt with
@param checkBox the check box to (possibly) add to the panel.
@param worksheets the worksheets to add to the panel.
@param scrollpanes the scrollpanes for each worksheet
@param mouseListeners a Vector array used to store mouse listeners for the
worksheets' scrollpanes so that the last selected worksheet can be tracked.
*/
private void addWorksheetsToPanel(JPanel panel, String intervalDescription, 
JPanel subPanel, JCheckBox checkBox, JWorksheet[] worksheets, 
JWorksheet[] headers, JScrollPane[] scrollPanes, Vector[] mouseListeners) {
	if (worksheets == null || worksheets.length == 0) {
		// There are no worksheets for the current interval type.
		checkBox.setSelected(false);
		return;
	}
	
	int numWorksheets = worksheets.length;
  
  AdjustmentListener irregularAdjustmentListener = null;
	boolean isIrregular = (intervalDescription.equals("Irregular"));
	if (isIrregular)
	  {
      irregularAdjustmentListener = new IrregularAdjustmentListener();
	  }

	// Create the panel into which these worksheets will be placed, and
	// give it a GridBagLayout.
	GridBagLayout gbl = new GridBagLayout();
	subPanel.setLayout(gbl);

	// For each worksheet, create its scrollpane and add the scrollpane
	// to the panel.  Also add mouse listeners to the scrollpane and its
	// scrollbars so that it can be determined after a mouse click which
	// worksheet (or worksheet's scrollpane components) was clicked on.
	// Put the registered mouse listeners into an array of vectors for
	// this purpose.
	for (int i = 0; i < numWorksheets; i++) {
		scrollPanes[i] = new JScrollPane(worksheets[i]);

		if (IOUtil.isUNIXMachine()) {	
			// For the reason why this is done, see 
			// JWorksheet.adjustmentValueChanged().
			scrollPanes[i].getVerticalScrollBar()
				.addAdjustmentListener(worksheets[i]);
			scrollPanes[i].getHorizontalScrollBar()
				.addAdjustmentListener(worksheets[i]);
		}

		// Add lots of mouse listeners, so that (hopefully) anywhere
		// a user clicks it will count as a click on the worksheet.
		
		worksheets[i].addMouseListener(this);

		// The header worksheet will be used to control selection 
		// events on the other worksheets for which the header worksheet
		// is the row header.
		headers[i].setRowSelectionModelPartner(
			worksheets[i].getRowSelectionModel());

		worksheets[i].addHeaderMouseListener(this);

		Vector v = new Vector();
		v.add(scrollPanes[i]);

		scrollPanes[i].addMouseListener(worksheets[i]);
		v.add(scrollPanes[i].getVerticalScrollBar());

		scrollPanes[i].getVerticalScrollBar().addMouseListener(
			worksheets[i]);
		v.add(scrollPanes[i].getHorizontalScrollBar());

		if (isIrregular)
		  {
		    scrollPanes[i].getVerticalScrollBar().setName("vScroll_" + i);
		    scrollPanes[i].getVerticalScrollBar().putClientProperty("Worksheet", worksheets[i]);
		    // Irregular worksheets scroll synchronously
//dre		    scrollPanes[i].getVerticalScrollBar().addAdjustmentListener(irregularAdjustmentListener);
		  }
		
		scrollPanes[i].getHorizontalScrollBar().addMouseListener(
			worksheets[i]);
        	JGUIUtil.addComponent(subPanel, scrollPanes[i],
        	        i, 0, 1, 1, 1, 1, 
        	        GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST); 

		// this next line looks weird, but is done because somewhere
		// the pointer to the worksheet that the models have is getting
		// mis-pointed.  This makes sure the models know their 
		// worksheet.
		((TSViewTable_TableModel)worksheets[i].getModel())
			.setWorksheet(worksheets[i]);
		mouseListeners[i] = v;
	}
	
	// If only one panel of worksheet data will appear on the GUI
	// (and each GUI panel contains the data for a specific Interval base)
	// then it's not necessary to include the checkbox for selecting and
	// deselecting specific interval bases.
	

	if (isIrregular)
	  {
	    JPanel jPanel = new JPanel();
	    if (calculateNumberOfPanels() > 1)
	      {
	        jPanel.add(checkBox);
	      }
	    __synchronizeCheckBox = new JCheckBox("Synchronized scrolling");
	    __synchronizeCheckBox.setName("Synchronized scrolling");
	    __synchronizeCheckBox.setSelected(__scrollIrregularSychnonously);
	    __synchronizeCheckBox.addItemListener(
	            new ItemListener()
	            {
	              public void itemStateChanged(ItemEvent e)
	              {
	                __scrollIrregularSychnonously =
	                  (e.getStateChange() == ItemEvent.SELECTED)?true:false;
	              }
	            });

	    jPanel.add(__synchronizeCheckBox);
	    JGUIUtil.addComponent(panel, jPanel,
	            0,__panelCount, 1, 1, 1, 0,
	            GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	  }
	else if (calculateNumberOfPanels() > 1)
	  {
	    JGUIUtil.addComponent(panel, checkBox,
            0,__panelCount, 1, 1, 1, 0,
            GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	  }
	  
	__panelCount++;
	
	subPanel.setBorder(BorderFactory.createTitledBorder(intervalDescription
		+ " Interval"));
      	JGUIUtil.addComponent(panel, subPanel,
		0, __panelCount++, 1, 1, 1, 1, 
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
}

/**
Builds the mouse listener array for the given worksheet array.  The array data
is not populated.
@param worksheets the array for which to build the mouse listener array
@return the mouse listener array.
*/
private Vector[] buildMouseListeners(JWorksheet[] worksheets) {
	if (worksheets == null) {
		return null;
	}
	
	int numWorksheets = worksheets.length;
	Vector[] v = new Vector[numWorksheets];
	return v;
}

/**
Calculates the number of panels with worksheets that will be added in total
to the __mainJPanel depending on the number of base intervals with time series.
@return the number of panels with worksheets added to __mainJPanel.
*/
private int calculateNumberOfPanels() {
	int size = 0;
	if (__minuteWorksheets != null && __minuteWorksheets.length > 0) {
		size++;
	}
	if (__hourWorksheets != null && __hourWorksheets.length > 0) {
		size++;
	}
	if (__dayWorksheets != null && __dayWorksheets.length > 0) {
		size++;
	}
	if (__monthWorksheets != null && __monthWorksheets.length > 0) {
		size++;
	}
	if (__yearWorksheets != null && __yearWorksheets.length > 0) {
		size++;
	}
	if (__irregularWorksheets != null && __irregularWorksheets.length > 0) {
		size++;
	}
	return size;
}

private static int MINUTE_PANEL_WORKSHEET_COUNT_INDEX = 0;
private static int HOUR_PANEL_WORKSHEET_COUNT_INDEX = 1;
private static int DAY_PANEL_WORKSHEET_COUNT_INDEX = 2;
private static int MONTH_PANEL_WORKSHEET_COUNT_INDEX = 3;
private static int YEAR_PANEL_WORKSHEET_COUNT_INDEX = 4;
private static int IRREGULAR_PANEL_WORKSHEET_COUNT_INDEX =7;
private static int TOTAL_VISIBLE_WORKSHEETS_INDEX = 5;
private static int TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX = 6;
/**
Calculates the number of worksheets in each panel and return an integer array
that can tell exactly how many worksheets are in each panel.
@return an integer array where:<br>
[0] - the number of worksheets in the minute panel<br>
[1] - the number of worksheets in the hour panel<br>
[2] - the number of worksheets in the day panel<br>
[3] - the number of worksheets in the month panel<br>
[4] - the number of worksheets in the year panel<br>
[5] - the total number of worksheets visible<br>
[6] - the total number of panels with visible worksheets
*/
private int[] calculateVisibleWorksheetsByPanel() {

	int[] array = new int[8];

	if (__minuteWorksheets == null || !__minuteJCheckBox.isSelected()) {
		array[MINUTE_PANEL_WORKSHEET_COUNT_INDEX] = 0;
	}
	else {
		array[MINUTE_PANEL_WORKSHEET_COUNT_INDEX] = __minuteWorksheets.length;
	}
	if (__hourWorksheets == null || !__hourJCheckBox.isSelected()) {
		array[HOUR_PANEL_WORKSHEET_COUNT_INDEX] = 0;
	}
	else {
		array[HOUR_PANEL_WORKSHEET_COUNT_INDEX] = __hourWorksheets.length;
	}
	if (__dayWorksheets == null || !__dayJCheckBox.isSelected()) {
		array[DAY_PANEL_WORKSHEET_COUNT_INDEX] = 0;
	}
	else {
		array[DAY_PANEL_WORKSHEET_COUNT_INDEX] = __dayWorksheets.length;
	}
	if (__monthWorksheets == null || !__monthJCheckBox.isSelected()) {
		array[MONTH_PANEL_WORKSHEET_COUNT_INDEX] = 0;
	}
	else {
		array[MONTH_PANEL_WORKSHEET_COUNT_INDEX] = __monthWorksheets.length;
	}
	if (__yearWorksheets == null || !__yearJCheckBox.isSelected()) {
		array[YEAR_PANEL_WORKSHEET_COUNT_INDEX] = 0;
	}
	else {
		array[YEAR_PANEL_WORKSHEET_COUNT_INDEX] = __yearWorksheets.length;
	}

	array[TOTAL_VISIBLE_WORKSHEETS_INDEX] = array[YEAR_PANEL_WORKSHEET_COUNT_INDEX] 
	                                      + array[MONTH_PANEL_WORKSHEET_COUNT_INDEX] 
	                                      + array[DAY_PANEL_WORKSHEET_COUNT_INDEX] 
	                                      + array[HOUR_PANEL_WORKSHEET_COUNT_INDEX] 
	                                      + array[MINUTE_PANEL_WORKSHEET_COUNT_INDEX]
	                                      + array[IRREGULAR_PANEL_WORKSHEET_COUNT_INDEX];

	array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX] = 0;
	if (array[MINUTE_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
		array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	}
	if (array[HOUR_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
		array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	}
	if (array[DAY_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
		array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	}
	if (array[MONTH_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
		array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	}
	if (array[YEAR_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
		array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	}
	 if (array[IRREGULAR_PANEL_WORKSHEET_COUNT_INDEX] > 0) {
	    array[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX]++;
	  }

	return array;
}

/**
Checks to see if only a single worksheet is displayed in the GUI.  If so, it
is selected the save button is set to always be enabled.
*/
private void checkForSingleWorksheet() {
	JWorksheet worksheet = null;

	int count = 0;

	if (__dayWorksheets != null) {
		for (int i = 0; i < __dayWorksheets.length; i++) {
			count++;
			worksheet = __dayWorksheets[i];
		}
	}
	if (__minuteWorksheets != null) {
		for (int i = 0; i < __minuteWorksheets.length; i++) {
			count++;
			worksheet = __minuteWorksheets[i];
		}
	}
	if (__hourWorksheets != null) {
		for (int i = 0; i < __hourWorksheets.length; i++) {
			count++;
			worksheet = __hourWorksheets[i];
		}
	}
	if (__irregularWorksheets != null) {
	  for (int i = 0; i < __irregularWorksheets.length; i++) {
	    count++;
	    worksheet = __irregularWorksheets[i];
	  }
	}
	if (__monthWorksheets != null) {
		for (int i = 0; i < __monthWorksheets.length; i++) {
			count++;
			worksheet = __monthWorksheets[i];
		}
	}
	if (__yearWorksheets != null) {
		for (int i = 0; i < __yearWorksheets.length; i++) {
			count++;
			worksheet = __yearWorksheets[i];
		}
	}

	if (count != 1) {
		return;
	}
	
	selectWorksheet(worksheet, null);
	__saveAlwaysEnabled = true;
	__saveJButton.setEnabled(true);
}

/**
Takes the Time Series from the __tslist Vector and puts each one into a Vector
specific to its data interval (e.g., Day time series go into __day, 
minute time series go into __minute, etc).  Different multipliers are all 
lumped together, as long as they have the same data interval.
*/
private void createSeparateTimeSeries() {
	TS ts;
	int size;
	int interval;

	__minute = new Vector();
	__hour = new Vector();
	__day = new Vector();
	__month = new Vector();
	__year = new Vector();
	__irregular = new Vector();

	if (__tslist.isEmpty()) {
		return;
	}

	size = __tslist.size();
	for (int i = 0; i < size; i++) {
		ts = (TS)__tslist.elementAt(i);
		if (ts == null) {
			continue;
		}
		interval = ts.getDataIntervalBase();

		if (interval == TimeInterval.MINUTE) {
			__minute.addElement(ts);
		}
		else if (interval == TimeInterval.HOUR) {
			__hour.addElement(ts);
		}
		else if (interval == TimeInterval.DAY) {
			__day.addElement(ts);
		}
		else if (interval == TimeInterval.IRREGULAR) {
			__irregular.addElement(ts);
		}
		else if (interval == TimeInterval.MONTH) {
			__month.addElement(ts);
		}
		else if (interval == TimeInterval.YEAR) {
			__year.addElement(ts);
		}		
	}	
}

/**
Create the table models wth the same interval base for all of the worksheets 
for the given Vector of time series.
@param tslist Vector of time series for which to create table models.
@return an array of TSViewTable_TableModel object, one for each worksheet
that needs to be created, or null if no worksheets need be created for the
ts type.
*/
private TSViewTable_TableModel[] createTableModels(Vector tslist) {
	String routine = "createTableModels";

	// if there is no data in the ts vector, there is no need to
	// create the table models
	if ((tslist == null)|| tslist.size() == 0) {
		return null;
	}

	int nTS = tslist.size();

	// the following arrays are used to match up time series with 
	// the same interval multipliers.  The arrays are sized to the 
	// maximum size necessary and won't necessarily be filled completely.
	int[] matches = new int[nTS];
	// Display format for time series
	String[] tsFormatString = new String[nTS];
	
	int count = 0;
	boolean hit = false;
	
	// get the first TS in the Vector and get the interval base.  All other
	// TS in the Vector must have the same interval base
	TS ts = (TS)tslist.elementAt(0);	
	int interval = ts.getDataIntervalBase();

	// Get the date format for the TS
	int dateFormat = dateFormat(interval);

	try {
	int tsPrecision = 2;	// default.
	
	String propValue = __props.getValue("OutputPrecision");
	int multi = 0;

	// Go through the time series and see how many of them have different
	// intervals.  All of the TS with the same intervals need to be
	// placed in the same worksheet. (Excludes irregular TS)
	count = findIntervals(tslist, matches, tsFormatString,  propValue);

	// Find the # of irregular TS
	//int nIregTS = findIrregularTS(tslist);
	
   // Calculate the output precision for Time Series
	for (int i = 0; i < nTS; i++)
	  {
    ts = (TS)tslist.elementAt(i);
 
    tsPrecision = calcOutputPrecision(ts, propValue);
    tsFormatString[i] = "%" + __OUTPUT_WIDTH + "."
      + tsPrecision + "f";
	  }
	
	// create an array of table models big enough to hold one table
	// model for every different interval multiplier & each Irregular TS
	//TSViewTable_TableModel[] models = new TSViewTable_TableModel[count];
	
	boolean useExtendedLegend = false;	
	propValue = __props.getValue("Table.UseExtendedLegend");
	if ((propValue != null) && (propValue.equalsIgnoreCase("true"))) {
		useExtendedLegend = true;
	}	

	Vector tableModels = new Vector();
	// Separate time series into groups. Each group having the same base & multiplier
	MultiValueHash mvh = new MultiValueHash();
	  nTS = tslist.size();
	  for (int i = 0; i < nTS; i++)
	    {
	    ts = (TS)tslist.elementAt(i);

	    mvh.put(new Integer(ts.getDataIntervalBase()),ts);	
	    }
	  
	  Set set = mvh.keySet();
	  Object key;
    
    Iterator it = set.iterator();
    while (it.hasNext())
           {
             key =  it.next();
             List listByIntervalBase = (List) mvh.get(key); // list w/ same base

             // now group by save Interval Multipier
             MultiValueHash mvhByIntervalMult = new MultiValueHash();
             int nList = listByIntervalBase.size();
             for (int i = 0; i < nList; i++)
               {
                 ts = (TS)listByIntervalBase.get(i);
                 mvhByIntervalMult.put(new Integer(ts.getDataIntervalMult()), ts);
               }

             Set set2 = mvh.keySet();
             Object key2;
             Iterator it2 = set2.iterator();
             Vector tempVector = new Vector();
             while (it2.hasNext())
               {
                 key2 =  it2.next();
                 List listByIntervalMult = (List) mvh.get(key); // list w/ same base & mult
                 String[] formats = findFormats(listByIntervalMult, propValue);
              // now get the starting date of data ...
                 TSLimits limits = TSUtil.getPeriodFromTS(new Vector(listByIntervalMult), TSUtil.MAX_POR);
                 DateTime start = limits.getDate1();
               
                 if (listByIntervalMult.get(0) instanceof IrregularTS)
                   {
                     Iterator temp = listByIntervalMult.iterator();
                     while (temp.hasNext())
                       {
                         tempVector.clear();
                         tempVector.add(temp.next());
                         // create models for each ts separately
                         tableModels.add ( new TSViewTable_Irregular_TableModel(tempVector, start, 
                                 interval, multi, dateFormat, formats,
                                 useExtendedLegend));
                       }
                   }
                 else
                   {
                     // create model
                     tableModels.add (
                             new TSViewTable_TableModel(new Vector(listByIntervalMult), 
                                     start, interval, multi, dateFormat, formats,
                                     useExtendedLegend));
                   }
               }
           }
 
    TSViewTable_TableModel models[] = 
      (TSViewTable_TableModel[])tableModels.toArray(new TSViewTable_TableModel[tableModels.size()]);
    return models;
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error generating worksheets");
		Message.printWarning(2, routine, e);
		return null;
	}
}

/**
 * Returns the display formats for the specified list of time series.
 * 
 * @param tsList list of time series
 * @param propValue
 * @return list of display formats
 */
private String[] findFormats(List tsList, String propValue)
{
  String[] formats = new String[tsList.size()];  

  int nTS = tsList.size();
  TS ts;
  for (int i = 0; i < nTS; i++)
    {
      ts = (TS)tsList.get(i);
      //Calculate the output precision for Time Series
      int tsPrecision = calcOutputPrecision(ts, propValue);
      formats[i] = "%" + __OUTPUT_WIDTH + "."
      + tsPrecision + "f";
    }
  return formats;
}
/**
 * Returns the Irregular TS contained in the specified TS list.
 * @param tslist
 * @return
 */
private Vector findIrregularTS(Vector tslist)
{
  TS ts;
  Vector v = new Vector();
  for (int i = 0; i< tslist.size(); i++)
    {
      ts = (TS)tslist.elementAt(i);
  
      if (ts.getDataIntervalBase() == TimeInterval.IRREGULAR)
        {
          v.add(ts);
        }
    }
  return v;
}

/**
 * Returns the number of different intervals in the tslist.
 * 
 * @param tslist List of Time Series.
 * @param matching
 * 
 * @return number of different intervals (number of models needed)
 * 
 */
private int findIntervals(Vector tslist, int[] matches,
        String[] tsFormatString, String propValue)
{
  int nTS = tslist.size();
  int count = 0;
  //the following array is used to match up time series with 
  // the same interval multipliers.  The array is sized to the 
  // maximum size necessary and won't necessarily be filled completely.
  
  int[] mults = new int[nTS];
  boolean hit;
  TS ts;
  int multi;
  for (int i = 0; i < nTS; i++) {
		ts = (TS)tslist.elementAt(i);
	
		// Skip irregular TS because each must get it's own
		if (ts.getDataIntervalBase() == TimeInterval.IRREGULAR)
		  {
		    continue;
		  }
		// get the interval multiplier for the current TS
		multi = ts.getDataIntervalMult();
		hit = false;

		// look through the array of previously-found interval
		// multipliers (mults[]) and see if the multiplier has 
		// already been encountered.
		for (int j = 0; j < count; j++) {
			if (mults[j] == multi) {
				// if it has, list this TS element #j as 
				// a match for interval multiplier #i
				hit = true;
				matches[i] = j;
			}
		}

		// if the interval multiplier was not found, add it to the
		// list of found multipliers and increment the count of
		// different interval multipliers that have been found
		if (!hit) {
			mults[count] = multi;
			matches[i] = count;
			count++;
		}
	}
  return count;
}

/**
 * Returns the output precision of the current TS's data.
 * 
 * @param ts
 * @param propValue
 * @return output precision
 */
private int calcOutputPrecision(TS ts, String propValue)
{
  int tsPrecision;
  tsPrecision = 2;
  if (propValue != null) {
  	tsPrecision = StringUtil.atoi(propValue);
  }
  else {	
  	try {	
  	  DataUnits units = DataUnits.lookupUnits(
  		ts.getDataUnits());
  		tsPrecision = units.getOutputPrecision();
  	}
  	catch (Exception e) {
  		// Use the default...
  		tsPrecision = 2;
  	}
  }
  return tsPrecision;
}

/**
 * Returns the date format.
 * 
 * @param interval
 * @return
 */
private int dateFormat(int interval)
{
  int dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm;
	// get the proper date format
	if (interval == TimeInterval.HOUR) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH;
	}
	else if (interval == TimeInterval.DAY) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD;
	}
	else if (interval == TimeInterval.IRREGULAR) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm;
	}
	else if (interval == TimeInterval.MONTH) {
		dateFormat = DateTime.FORMAT_YYYY_MM;
	}
	else if (interval == TimeInterval.YEAR) {
		dateFormat = DateTime.FORMAT_YYYY;
	}
	else if (interval == TimeInterval.MINUTE) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm;
	}
  return dateFormat;
}

/**
Creates worksheets for all of the table models that were previously-generated.
@param models table models for each of the worksheets that need to be made.
@param p PropList defining JWorksheet characteristics.  See the JWorksheet
constructors.
@return an array of JWorksheets, one for each model.
*/
private JWorksheet[] createWorksheets(TSViewTable_TableModel[] models,
PropList p) {
	if (models == null) {
		return null;
	}
	int numWorksheets = models.length;

	JWorksheet[] worksheets = new JWorksheet[numWorksheets];
	JWorksheet worksheet = null;
	TSViewTable_TableModel model = null;
	for (int i = 0; i < numWorksheets; i++) {
		model = models[i];
		TSViewTable_CellRenderer cr = 
			new TSViewTable_CellRenderer(model);
		worksheet = new JWorksheet(cr, model, p);
	//TODO:dredre	worksheet.setPreferredScrollableViewportSize(null);
		setVisibleRowCount(worksheet,20);

		worksheet.setHourglassJFrame(this);
		worksheets[i] = worksheet;
		model.setWorksheet(worksheets[i]);
	}
	return worksheets;
}
/** 
Sets the visible row count for the specified table.
<p>
Assumes fixed row heights
*/
public static void setVisibleRowCount(JTable table, int rows){ 
  table.setPreferredScrollableViewportSize(new Dimension( 
    table.getPreferredScrollableViewportSize().width, 
    rows*table.getRowHeight() 
  )); 
} 
/**
Clean up before garbage collection.
*/
protected void finalize()
throws Throwable {
	__dayJCheckBox = null;
	__hourJCheckBox = null;
	__irregularJCheckBox = null;
	__minuteJCheckBox = null;
	__monthJCheckBox = null;
	__yearJCheckBox = null;
	__mainJPanel = null;
	__dayJPanel = null;
	__hourJPanel = null;
	__irregularJPanel = null;
	__minuteJPanel = null;
	__monthJPanel = null;
	__yearJPanel = null;
	IOUtil.nullArray(__dayScrollPanes);
	IOUtil.nullArray(__hourScrollPanes);
	IOUtil.nullArray(__minuteScrollPanes);
	IOUtil.nullArray(__monthScrollPanes);
	IOUtil.nullArray(__yearScrollPanes);
	__lastSelectedScrollPane = null;
	__messageJTextField = null;
	IOUtil.nullArray(__dayWorksheets);
	IOUtil.nullArray(__hourWorksheets);
	IOUtil.nullArray(__minuteWorksheets);
	IOUtil.nullArray(__monthWorksheets);
	IOUtil.nullArray(__yearWorksheets);
	__lastSelectedWorksheet = null;
	__props = null;
	__summaryJButton = null;
	__graphJButton = null;
	__closeJButton = null;
	__saveJButton = null;
	__tsviewJFrame = null;
	IOUtil.nullArray(__dayModels);
	IOUtil.nullArray(__hourModels);
	IOUtil.nullArray(__minuteModels);
	IOUtil.nullArray(__monthModels);
	IOUtil.nullArray(__yearModels);
	IOUtil.nullArray(__dayMouseListeners);
	IOUtil.nullArray(__hourMouseListeners);
	IOUtil.nullArray(__minuteMouseListeners);
	IOUtil.nullArray(__monthMouseListeners);
	IOUtil.nullArray(__yearMouseListeners);
	__day = null;
	__hour = null;
	__minute = null;
	__month = null;
	__irregular = null;
	__tslist = null;
	__year = null;
	super.finalize();
}

/**
Figures out which worksheet or worksheet header or worksheet's JScrollPane area
was clicked on, and marks that worksheet as the last-selected one.
@param e the MouseEvent that means a worksheet area was clicked on.
*/
private JWorksheet findClickedOnJWorksheet(MouseEvent e) {
	Object source = e.getSource();
	if (source instanceof JWorksheet) {		
		return (JWorksheet)source;
	}
	else if (source instanceof JWorksheet_Header) {
		return (JWorksheet)((JWorksheet_Header)source).getTable();
	}
	else {
		if (__dayWorksheets != null) {
			JWorksheet worksheet = searchListeners(__dayWorksheets,
				__dayMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (__hourWorksheets != null) {
			JWorksheet worksheet = searchListeners(__hourWorksheets,
				__hourMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (__minuteWorksheets != null) {
			JWorksheet worksheet = searchListeners(
				__minuteWorksheets, __minuteMouseListeners, 
				source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (__monthWorksheets != null) {
			JWorksheet worksheet = searchListeners(
				__monthWorksheets, __monthMouseListeners, 
				source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (__yearWorksheets != null) {
			JWorksheet worksheet = searchListeners(__yearWorksheets,
				__yearMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}

	}
	return null;
}

/**
Find the JPanel associated with a specific worksheet.  
@param worksheet the worksheet for which to find the JPanel it is in.
@return the JPanel for a specific JWorksheet.
*/
public JPanel findWorksheetsJPanel(JWorksheet worksheet) {
	if (worksheet == null) {
		return null;
	}
	if (__dayWorksheets != null) {
		for (int i = 0; i < __dayWorksheets.length; i++) {
			if (__dayWorksheets[i] == worksheet) {
				return __dayJPanel;
			}
		}
	}
	if (__minuteWorksheets != null) {
		for (int i = 0; i < __minuteWorksheets.length; i++) {
			if (__minuteWorksheets[i] == worksheet) {
				return __minuteJPanel;
			}
		}
	}
	if (__hourWorksheets != null) {
		for (int i = 0; i < __hourWorksheets.length; i++) {
			if (__hourWorksheets[i] == worksheet) {
				return __hourJPanel;
			}
		}
	}
	if (__monthWorksheets != null) {
		for (int i = 0; i < __monthWorksheets.length; i++) {
			if (__monthWorksheets[i] == worksheet) {
				return __monthJPanel;
			}
		}
	}
	if (__yearWorksheets != null) {
		for (int i = 0; i < __yearWorksheets.length; i++) {
			if (__yearWorksheets[i] == worksheet) {
				return __yearJPanel;
			}
		}
	}
	return null;	
}

/**
Find the scrollpane associated with a specific worksheet.  
@param worksheet the worksheet for which to find the scrollpane.
@return the JScrollPane for a specific JWorksheet.
*/
public JScrollPane findWorksheetsScrollPane(JWorksheet worksheet) {
	if (worksheet == null) {
		return null;
	}
	if (__dayWorksheets != null) {
		for (int i = 0; i < __dayWorksheets.length; i++) {
			if (__dayWorksheets[i] == worksheet) {
				return __dayScrollPanes[i];
			}
		}
	}
	if (__minuteWorksheets != null) {
		for (int i = 0; i < __minuteWorksheets.length; i++) {
			if (__minuteWorksheets[i] == worksheet) {
				return __minuteScrollPanes[i];
			}
		}
	}
	if (__hourWorksheets != null) {
		for (int i = 0; i < __hourWorksheets.length; i++) {
			if (__hourWorksheets[i] == worksheet) {
				return __hourScrollPanes[i];
			}
		}
	}
	 if (__irregularWorksheets != null) {
	    for (int i = 0; i < __irregularWorksheets.length; i++) {
	      if (__irregularWorksheets[i] == worksheet) {
	        return __irregularScrollPanes[i];
	      }
	    }
	  }
	if (__monthWorksheets != null) {
		for (int i = 0; i < __monthWorksheets.length; i++) {
			if (__monthWorksheets[i] == worksheet) {
				return __monthScrollPanes[i];
			}
		}
	}
	if (__yearWorksheets != null) {
		for (int i = 0; i < __yearWorksheets.length; i++) {
			if (__yearWorksheets[i] == worksheet) {
				return __yearScrollPanes[i];
			}
		}
	}
	return null;	
}

/**
Handle events from the checkboxes, indicating which time series intervals should
be shown.
@param evt ItemEvent to handle.
*/
public void itemStateChanged(ItemEvent evt) {
	Object source = evt.getSource();
	int state = evt.getStateChange();
	
	boolean visible = false;
	if (state == ItemEvent.SELECTED) {
		visible = true;
	}
	else {
		visible = false;
	}	
	
	if (source == __minuteJCheckBox) {
		__minuteJPanel.setVisible(visible);
	}
	else if (source == __hourJCheckBox) {
		__hourJPanel.setVisible(visible);
	}
	else if (source == __dayJCheckBox) {
		__dayJPanel.setVisible(visible);
	}
	else if (source == __irregularJCheckBox) {
		__irregularJPanel.setVisible(visible);
	}
	else if (source == __monthJCheckBox) {
		__monthJPanel.setVisible(visible);
	}
	else if (source == __yearJCheckBox) {
		__yearJPanel.setVisible(visible);
	}

	JPanel panel = findWorksheetsJPanel(__lastSelectedWorksheet);
	if (panel != null && !panel.isVisible()) {
		if (__lastSelectedScrollPane != null) {
			// reset the border to its original state
			__lastSelectedScrollPane.setBorder(
				__originalScrollPaneBorder);
		}	
	
		if (!__lastSelectedWorksheet.stopEditing()) {
			__lastSelectedWorksheet.cancelEditing();
		}
	
		__lastSelectedWorksheet = null;
		__lastSelectedScrollPane = null;
		__originalScrollPaneBorder = null;
		__messageJTextField.setText("Currently-selected worksheet: "
			+ "(none)");
		if (!__saveAlwaysEnabled) {
			__saveJButton.setEnabled(false);
		}
	}

	int[] arr = calculateVisibleWorksheetsByPanel();

	if (arr[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX] == 0) {		
		__mainJPanelNorth = true;
		getContentPane().remove(__mainJPanel);
		getContentPane().add(__mainJPanel, "North");
	}
	else {
		if (__mainJPanelNorth) {
			getContentPane().remove(__mainJPanel);
			getContentPane().add(__mainJPanel, "Center");
			__mainJPanelNorth = false;
		}
	}

	if (arr[TOTAL_VISIBLE_WORKSHEETS_INDEX] == 1) {
		if (__lastSelectedScrollPane != null) {
			// reset the border to its original state
			__lastSelectedScrollPane.setBorder(
				(new JScrollPane()).getBorder());
		}	

		setPanelsBorder(false);
	}	
	else if (arr[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX] > 1 
	        && panel != null && panel.isVisible()) {
		__lastSelectedScrollPane.setBorder(
			BorderFactory.createLineBorder(Color.blue, 2));
		setPanelsBorder(true);
	}
	else if (arr[TOTAL_PANELS_WITH_VISIBLE_WORKSHEETS_INDEX] > 1) {
		setPanelsBorder(true);
	}
}


/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {}

/**
Responds to mouse entered events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent e) {}

/**
Responds to mouse exited events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseExited(MouseEvent e) {}

/**
Responds to mouse pressed events; does nothing.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {
	__saveJButton.setEnabled(true);

	if (__lastSelectedScrollPane != null) {
		// reset the border to its original state
		__lastSelectedScrollPane.setBorder(__originalScrollPaneBorder);
	}

	JWorksheet last = __lastSelectedWorksheet;
	// Find the worksheet that was clicked on ...

	selectWorksheet(findClickedOnJWorksheet(e), last);
}

/**
Responds to mouse released events; checks to see which JWorksheet was clicked on
and marks it as the most-recently selected JWorksheet.
@param e the MouseEvent that happened.
*/
public void mouseReleased(MouseEvent e) {
	mousePressed(e);
}

/**
Called when the user presses the "Save" button.  This gets the time series 
stored in the most-recently selected JWorksheet and writes them to the 
file selected by the user.  How to write the file is determined by the 
extension of the file selected by the user.
*/
private void saveClicked() {
	String routine = "saveClicked";
	if (__lastSelectedWorksheet == null) {
		__messageJTextField.setText("No worksheets currently "
			+ "selected.  Select one and press 'Save' again.");
		return;
	}

	JGUIUtil.setWaitCursor(this, true);
	String last_directory = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc =JFileChooserFactory.createJFileChooser(last_directory);
	fc.setAcceptAllFileFilterUsed(false);
	fc.setDialogTitle("Save TS File");
	SimpleFileFilter dff = new SimpleFileFilter("dv",
		"DateValue Time Series File");
	fc.addChoosableFileFilter(dff);
	SimpleFileFilter tff = new SimpleFileFilter("txt",
		"Tab-Delimited Text File");
	fc.addChoosableFileFilter(tff);	
	SimpleFileFilter cff = new SimpleFileFilter("txt",
		"Comma-Delimited Text File");
	fc.addChoosableFileFilter(cff);		
	fc.setFileFilter(tff);

	if (fc.showSaveDialog(this)!= JFileChooser.APPROVE_OPTION) {		
		JGUIUtil.setWaitCursor(this, false);
		return;
	}

	String directory = fc.getSelectedFile().getParent();
	if (directory != null) {		
		JGUIUtil.setLastFileDialogDirectory(directory);
	}
	String filename = directory + File.separator
		+ fc.getName(fc.getSelectedFile());

	Vector tslist = ((TSViewTable_TableModel)
		__lastSelectedWorksheet.getModel()).getTSList();

	if (fc.getFileFilter() == dff) {
		try {	
			DateValueTS.writeTimeSeriesList(tslist, filename);
		}
		catch (Exception e) {		
			Message.printWarning(1, routine, "Error saving "
				+ "DateValue file \"" + filename + "\"");
			Message.printWarning(1, routine, e);			
		}
	}
	else if (fc.getFileFilter() == tff) {
		writeTextFile(__lastSelectedWorksheet, '\t', filename);
	}
	else if (fc.getFileFilter() == cff) {
		writeTextFile(__lastSelectedWorksheet, ',', filename);
	}
	JGUIUtil.setWaitCursor(this, false);
}

/**
Searches through the listener array associated with worksheets to locate
the JWorksheet for which the JScrollPane area was clicked on.
@param worksheets the worksheets associated with a certain data interval
@param listeners an array of Vectors containing JScrollPane and JScrollPane
scrollbars used to scroll around the worksheets in the above array
@param source the object on which a MouseEvent was triggered.
@return the JWorksheet that was clicked on, or null if it could not be found
*/
private JWorksheet searchListeners(JWorksheet[] worksheets, Vector[] listeners,
Object source) {
	if (listeners == null || source == null) {
		return null;
	}

	int size = listeners.length;

	for (int i = 0; i < size; i++) {
		Vector v = listeners[i];

		for (int j = 0; j < v.size(); j++) {
			if (v.elementAt(j) == source) {
				return worksheets[i];
			}
		}
	}
	return null;
}

/**
Sets a worksheet to be selected, outlining it in blue and also setting the
text at the bottom of the GUI to reflect which worksheet is selected.
@param selectWorksheet the worksheet to be selected.
@param lastWorksheet the worksheet that was previously selected.
*/
private void selectWorksheet(JWorksheet selectWorksheet, 
JWorksheet lastWorksheet) {
	int[] arr = calculateVisibleWorksheetsByPanel();
	__lastSelectedWorksheet = selectWorksheet;

	if (lastWorksheet != null && lastWorksheet != selectWorksheet) {
		if (!lastWorksheet.stopEditing()) {
			lastWorksheet.cancelEditing();
		}	
	}
	// ... and find its scroll pane, too.
	__lastSelectedScrollPane = findWorksheetsScrollPane(
		selectWorksheet);

	// Back up the scroll pane's current border ...
	__originalScrollPaneBorder = __lastSelectedScrollPane.getBorder();
	// ... and change the scroll pane's border to represent that 
	// its worksheet is selected.
	if (arr[TOTAL_VISIBLE_WORKSHEETS_INDEX] > 1) {
		__lastSelectedScrollPane.setBorder(
			BorderFactory.createLineBorder(Color.blue, 2));
	}

	TSViewTable_TableModel model = (TSViewTable_TableModel)
		selectWorksheet.getModel();
	String base = null;
	switch (model.getIntervalBase()) {
		case TimeInterval.MINUTE:	base = "Minute";break;
		case TimeInterval.HOUR:		base = "Hour";	break;
		case TimeInterval.DAY:		base = "Day";	break;
		case TimeInterval.MONTH:	base = "Month";	break;
		case TimeInterval.YEAR:		base = "Year";	break;
		case TimeInterval.IRREGULAR: base = "Irregular"; break;
		default:			base = "???";	break;
	}

	String s = "";
	if (model.getIntervalMult() == 1) {
		s = "" + base;
	}
	else {
		s = "" + model.getIntervalMult() + base;
	}
	
	__messageJTextField.setText("Currently-selected worksheet interval: "
		+ s);
}

/**
Sets the column widths for the specified group of worksheets.
@param worksheets the worksheets for which to set the column widths.
*/
private void setColumnWidths(JWorksheet[] worksheets, int precision) {
	if (worksheets == null || worksheets.length == 0) {
		return;
	}

	int[] skipCols = { 0 };
	int[] widths = null;

	int dateWidth = 16;

	// get the proper date format
	if (precision == DateTime.PRECISION_HOUR) {
		dateWidth = 13;
	}
	else if (precision == DateTime.PRECISION_DAY) {
		dateWidth = 10;
	}
	else if (precision == DateTime.PRECISION_MONTH) {
		dateWidth = 7;
	}
	else if (precision == DateTime.PRECISION_YEAR) {
		dateWidth = 4;
	}
	else if (precision == DateTime.PRECISION_MINUTE) {
		dateWidth = 16;
	}

	for (int i = 0; i < worksheets.length; i++) {
		TSViewTable_TableModel model = (TSViewTable_TableModel)
			worksheets[i].getModel();
		widths = model.getColumnWidths();
		widths[0] = dateWidth;
		worksheets[i].setColumnWidths(widths);
		worksheets[i].calculateColumnWidths(100, 1000, skipCols,
			getGraphics());
	}
}

/**
Turns on or off the borders for all the panels at once.
@param on if true, then the borders for all the panels will be displayed.  If 
false, they will not.
*/
public void setPanelsBorder(boolean on) {
	if (on) {
		__minuteJPanel.setBorder(
			BorderFactory.createTitledBorder("Minute Interval"));
		__hourJPanel.setBorder(
			BorderFactory.createTitledBorder("Hour Interval"));
		__dayJPanel.setBorder(
			BorderFactory.createTitledBorder("Day Interval"));
		__irregularJPanel.setBorder(
			BorderFactory.createTitledBorder("Irregular Interval"));
		__monthJPanel.setBorder(
			BorderFactory.createTitledBorder("Month Interval"));
		__yearJPanel.setBorder(
			BorderFactory.createTitledBorder("Year Interval"));
	}
	else {
		__minuteJPanel.setBorder(null);
		__hourJPanel.setBorder(null);
		__dayJPanel.setBorder(null);
		__irregularJPanel.setBorder(null);
		__monthJPanel.setBorder(null);
		__yearJPanel.setBorder(null);
	}
}

/**
Sets up the GUI.
@param mode Indicates whether the GUI should be visible at creation.
*/
private void setupGUI(boolean mode) {
	String	routine = "TSViewTableJFrame.setupGUI";

	// Start a big try block to set up the GUI...
	try {

	// Add a listener to catch window manager events...
	addWindowListener(this);

	// Lay out the main window component by component.  We will start with
	// the menubar default components.  Then add each requested component
	// to the menu bar and the interface...
	GridBagLayout gbl = new GridBagLayout();

	// Add a panel to hold the tables...
	__mainJPanel = new JPanel();
	__mainJPanel.setLayout(gbl);
	getContentPane().add(__mainJPanel, BorderLayout.CENTER);

	// Create all the JCheckboxes
	__minuteJCheckBox = new JCheckBox("Minute Time Series", true);
	__minuteJCheckBox.addItemListener(this);
	__hourJCheckBox = new JCheckBox("Hour Time Series", true);
	__hourJCheckBox.addItemListener(this);
	__dayJCheckBox = new JCheckBox("Day Time Series", true);
	__dayJCheckBox.addItemListener(this);
	__irregularJCheckBox = new JCheckBox("Irregular Time Series", true);
	__irregularJCheckBox.addItemListener(this);
	__monthJCheckBox = new JCheckBox("Month Time Series", true);
	__monthJCheckBox.addItemListener(this);
	__yearJCheckBox = new JCheckBox("Year Time Series", true);
	__yearJCheckBox.addItemListener(this);

	// create the proplist for the JWorksheets
	PropList p = new PropList("TSViewTableJFrame.JWorksheet");
	p.add("JWorksheet.OneClickColumnSelection=true");
	p.add("JWorksheet.RowColumnPresent=true");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=ExcelSelection");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.AllowPaste=true");

	PropList p2 = new PropList("TSViewTableJFrame.JWorksheet");
	p2.add("JWorksheet.RowColumnPresent=true");
	p2.add("JWorksheet.Selectable=false");
	p2.add("JWorksheet.ShowPopupMenu=true");
	p2.add("JWorksheet.SelectionMode=ExcelSelection");

	// separate the __tslist Vector into Vectors of like time series
	createSeparateTimeSeries();

	// create all the table models and worksheets
	__dayModels = createTableModels(__day);
	__dayWorksheets = createWorksheets(__dayModels, p);
	JWorksheet[] dayHeaders = createWorksheets(__dayModels, p2);

	__minuteModels = createTableModels(__minute);
	__minuteWorksheets = createWorksheets(__minuteModels, p);
	JWorksheet[] minuteHeaders = createWorksheets(__minuteModels, p2);

	__hourModels = createTableModels(__hour);
	__hourWorksheets = createWorksheets(__hourModels, p);
	JWorksheet[] hourHeaders = createWorksheets(__hourModels, p2);

	 __irregularModels = createTableModels(__irregular);
	  __irregularWorksheets = createWorksheets(__irregularModels, p);
	  JWorksheet[] irregularHeaders = createWorksheets(__irregularModels, p2);

	__monthModels = createTableModels(__month);
	__monthWorksheets = createWorksheets(__monthModels, p);
	JWorksheet[] monthHeaders = createWorksheets(__monthModels, p2);

	__yearModels = createTableModels(__year);
	__yearWorksheets = createWorksheets(__yearModels, p);
	JWorksheet[] yearHeaders = createWorksheets(__yearModels, p2);
	
	// create the panels for the interval bases
	__minuteJPanel = new JPanel();
  __minuteJPanel.setName("minuteJPanel");
	__hourJPanel = new JPanel();
  __hourJPanel.setName("hourJPanel");
	__dayJPanel = new JPanel();
	__irregularJPanel = new JPanel();
	__irregularJPanel.setName("irregularJPanel");

	__monthJPanel = new JPanel();
	__yearJPanel = new JPanel();

	// create the arrays of scroll panes
	if (__dayWorksheets != null) {
		__dayScrollPanes = new JScrollPane[__dayWorksheets.length];
	}
	if (__minuteWorksheets != null) {
		__minuteScrollPanes =new JScrollPane[__minuteWorksheets.length];
	}
	if (__hourWorksheets != null) {
		__hourScrollPanes = new JScrollPane[__hourWorksheets.length];
	}
	 if (__irregularWorksheets != null) {
	    __irregularScrollPanes = new JScrollPane[__irregularWorksheets.length];
	  }
	if (__monthWorksheets != null) {
		__monthScrollPanes = new JScrollPane[__monthWorksheets.length];
	}
	if (__yearWorksheets != null) {
		__yearScrollPanes = new JScrollPane[__yearWorksheets.length];
	}

	// add the worksheets to the panels and add the panels to the main
	// panel

	if (__reverseNormalOrder) {
		__minuteMouseListeners = buildMouseListeners(
			__minuteWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Minute", __minuteJPanel, 
			__minuteJCheckBox, __minuteWorksheets, 
			minuteHeaders,
			__minuteScrollPanes, __minuteMouseListeners);
		
		__hourMouseListeners = buildMouseListeners(__hourWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Hour", __hourJPanel, 
			__hourJCheckBox, __hourWorksheets, 
			hourHeaders, __hourScrollPanes,
			__hourMouseListeners);
		
		__dayMouseListeners = buildMouseListeners(__dayWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Day", __dayJPanel, 
			__dayJCheckBox, __dayWorksheets, 
			dayHeaders, __dayScrollPanes,
			__dayMouseListeners);
		
	  __irregularMouseListeners = buildMouseListeners(__irregularWorksheets);
    addWorksheetsToPanel(__irregularJPanel, "Irregular", __irregularJPanel, 
      __irregularJCheckBox, __irregularWorksheets, 
      irregularHeaders, __irregularScrollPanes,
      __irregularMouseListeners);
    
		__monthMouseListeners = buildMouseListeners(__monthWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Month", __monthJPanel, 
			__monthJCheckBox, __monthWorksheets, 
			monthHeaders, __monthScrollPanes,
			__monthMouseListeners);
		
		__yearMouseListeners = buildMouseListeners(__yearWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Year", __yearJPanel, 
			__yearJCheckBox, __yearWorksheets, 
			yearHeaders, __yearScrollPanes,
			__yearMouseListeners);

	}
	else {
		__yearMouseListeners = buildMouseListeners(__yearWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Year", __yearJPanel, 
			__yearJCheckBox, __yearWorksheets, 
			yearHeaders, __yearScrollPanes,
			__yearMouseListeners);
	
		__monthMouseListeners = buildMouseListeners(__monthWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Month", __monthJPanel, 
			__monthJCheckBox, __monthWorksheets, 
			monthHeaders, __monthScrollPanes,
			__monthMouseListeners);
		
	  __irregularMouseListeners = buildMouseListeners(__irregularWorksheets);
    addWorksheetsToPanel(__mainJPanel, "Irregular", __irregularJPanel, 
      __irregularJCheckBox, __irregularWorksheets, 
      irregularHeaders, __irregularScrollPanes,
      __irregularMouseListeners);

		__dayMouseListeners = buildMouseListeners(__dayWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Day", __dayJPanel, 
			__dayJCheckBox, __dayWorksheets, 
			dayHeaders, __dayScrollPanes,
			__dayMouseListeners);
				
		__hourMouseListeners = buildMouseListeners(__hourWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Hour", __hourJPanel, 
			__hourJCheckBox, __hourWorksheets, 
			hourHeaders, __hourScrollPanes,
			__hourMouseListeners);
		

		__minuteMouseListeners = buildMouseListeners(
			__minuteWorksheets);
		addWorksheetsToPanel(__mainJPanel, "Minute", __minuteJPanel, 
			__minuteJCheckBox, __minuteWorksheets, 
			minuteHeaders,
			__minuteScrollPanes, __minuteMouseListeners);		
	}

	JPanel bottomJPanel = new JPanel();
  bottomJPanel.setName("bottomJPanel");
	bottomJPanel.setLayout (gbl);
	__messageJTextField = new JTextField();
	__messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, __messageJTextField,
		0, 1, 7, 1, 1.0, 0.0, 
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Put the buttons on the bottom of the window...
	JPanel button_JPanel = new JPanel();
	 bottomJPanel.setName("button_JPanel");
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	__graphJButton = new SimpleJButton(__BUTTON_GRAPH, this);
	String propValue=__props.getValue("EnableGraph");
	if ((propValue != null) && propValue.equalsIgnoreCase("false")) {
		__graphJButton.setEnabled(false);
	}

	__summaryJButton = new SimpleJButton(__BUTTON_SUMMARY, this);
	propValue=__props.getValue("EnableSummary");
	if ((propValue != null) && propValue.equalsIgnoreCase("false")) {
		__summaryJButton.setEnabled(false);
	}

	__closeJButton = new SimpleJButton(__BUTTON_CLOSE,this);
	__closeJButton.setName(__BUTTON_CLOSE);
	__saveJButton = new SimpleJButton(__BUTTON_SAVE, this);
	__saveJButton.setEnabled(false);

	button_JPanel.add(__graphJButton);
	button_JPanel.add(__summaryJButton);
	button_JPanel.add(__saveJButton);
//	button_JPanel.add(__printJButton);
	button_JPanel.add(__closeJButton);
	// REVISIT - add later
	//button_JPanel.add(__helpJButton);
	
	JGUIUtil.addComponent(bottomJPanel, button_JPanel,
		0, 0, 8, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
	getContentPane().setName("ContentPane");
	getContentPane().add(bottomJPanel,BorderLayout.SOUTH);

	__messageJTextField.setText("Currently-selected worksheet: (none)");	

	//TODO: dre set size BEFORE pack! - but causes layout issues...
  pack();
  setSize(555,500);

	
	JGUIUtil.center(this);
	setVisible(mode);

	// avoid sych events during initialization by delay of adding listeners
	if (__irregularScrollPanes != null && __irregularScrollPanes.length > 0)
	  {
	    AdjustmentListener irregularAdjustmentListener = new IrregularAdjustmentListener();
	    int nPanes = __irregularScrollPanes.length;
	    for (int iPane = 0; iPane < nPanes; iPane++)
	      {
	        // Irregular worksheets scroll synchronously
	        __irregularScrollPanes[iPane].getVerticalScrollBar().addAdjustmentListener(irregularAdjustmentListener);
	      }
	  }
	} // end of try
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	// these calls are here because they require a valid graphics 
	// context to work properly (i.e., show() must have already been
	// called)
	setColumnWidths(__minuteWorksheets, DateTime.PRECISION_MINUTE);
	setColumnWidths(__hourWorksheets, DateTime.PRECISION_HOUR);
	setColumnWidths(__dayWorksheets, DateTime.PRECISION_DAY);
	//TODO:dre verif irregular worksheet precision
	setColumnWidths(__irregularWorksheets, DateTime.PRECISION_HOUR);
	setColumnWidths(__monthWorksheets, DateTime.PRECISION_MONTH);
	setColumnWidths(__yearWorksheets, DateTime.PRECISION_YEAR);

	checkForSingleWorksheet();
}

/**
Responds to window activated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowActivated(WindowEvent evt) {}

/**
Responds to window closed events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowClosed(WindowEvent evt) {}

/**
Responds to window closing events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowClosing(WindowEvent evt) {
	__tsviewJFrame.closeGUI(TSViewJFrame.TABLE);
}

/**
Responds to window deactivated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Responds to window deiconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Responds to window iconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowIconified(WindowEvent evt) {}

/**
Responds to window opened events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowOpened(WindowEvent evt) {}

/**
Responds to window opening events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowOpening(WindowEvent evt) {}

/**
Write all of a worksheet's data to a text file, separating field values
by the given delimiter and separating rows with newlines.
@param worksheet the worksheet to dump out the data of.
@param delimited the character to separate fields with.
@param filename the text file to write to.
*/
private void writeTextFile(JWorksheet worksheet, char delimiter, 
String filename) {
	String routine = "writeTextFile";
	try {
		StopWatch sw = new StopWatch();
		sw.start();
		PrintWriter out = new PrintWriter(
			new FileOutputStream(filename));

		__messageJTextField.setText("Saving file \"" 
			+ filename + "\"");
		JGUIUtil.forceRepaint(__messageJTextField);
		int rows = worksheet.getRowCount();
		int columns = worksheet.getColumnCount();

		worksheet.startNewConsecutiveRead();
		
		StringBuffer buff = null;
		int pct = 0;
		int lastPct = 0;
		for (int i = 0; i < rows; i++) {
			// calculate the percentage complete, and if different
			// from the last percentage complete, update the 
			// status bar text field.
			pct = ((int)(((double)i / (double)rows) * 100));
			if (pct != lastPct) {
				lastPct = pct;
				__messageJTextField.setText("Saving file \"" 
					+ filename + "\" (" + pct +  "% done)");
				JGUIUtil.forceRepaint(__messageJTextField);
			}
		
			buff = new StringBuffer();
			for (int j = 0; j < columns; j++) {
				buff.append(worksheet.getConsecutiveValueAt(
					i, j));
				if (j != (columns - 1)) {
					buff.append(delimiter);
				}
			}
			out.println(buff);
		}

		out.flush();
		out.close();
		sw.stop();
		double seconds = sw.getSeconds();
		String plural = "s";

		// unlikely to happen, but it's just good GUI design to
		// not have something like "1 seconds" or "2 second(s)".
		if (seconds == 1.000) {
			plural = "";
		}
		__messageJTextField.setText("File saved (took " 
			+ sw.getSeconds() + " second" + plural + ")");
	}
	catch (Exception e) {
		__messageJTextField.setText("Error saving file \""
			+ filename + "\"");
		Message.printWarning(2, routine, "Error saving file \"" 
			+ filename + "\"");
		Message.printWarning(2, routine, e);
	}
}

/**
 * Listens for property change events (from TSGraphEditor) &
 * Notifies the model listeners (JTables/JWorksheet) that the
 * table model has changed. 
 */
public void propertyChange(PropertyChangeEvent e)
{
  if (e.getPropertyName().equals("TS_DATA_VALUE_CHANGE"))
    {
      // Expecting modified TS object
      TSViewTable_TableModel model =findModel(((TS)e.getNewValue()));
      model.fireTableDataChanged();
    }
  
}
/**
 * Returns the first model encountered that contains the specified TS.
 * 
 * @param ts
 * @return model containing specified TS, or null
 */
private  TSViewTable_TableModel findModel(TS ts)
{
  // TODO: Order search so that most likely models are searched first!

  TSViewTable_TableModel target = null;
  
  if ((target = findModel(__dayModels,ts)) != null)
    {
      return target;
    }
  else  if ((target = findModel(__hourModels,ts)) != null)
    {
      return target;
    }
  else  if ((target = findModel(__minuteModels,ts)) != null)
    {
      return target;
    }
  else  if ((target = findModel(__monthModels,ts)) != null)
    {
      return target;
    }
  else  if ((target = findModel(__yearModels,ts)) != null)
    {
      return target;
    }
  return target;
} // eof findModel(TS ts)

/**
 * Returns the first model encountered that contains the specified TS.
 * @param models
 * @param ts
 * @return model containing specified TS, or null
 */
private TSViewTable_TableModel findModel(TSViewTable_TableModel[] models, TS ts)
{
  if ( models != null)
    {
      int nModels = models.length;
      for( int iModel = 0; iModel < nModels; iModel++)
        {
          TSViewTable_TableModel m = ((TSViewTable_TableModel)models[iModel]);
          Vector tsVector = m.getTSList();
          int nVec = tsVector.size();
          for (int iVec = 0; iVec < nVec; iVec++)
            {
              if (tsVector.elementAt(iVec) == ts)
                {
                  return m;
                }
            }
        }
    }
  return null;
} // eof findModel()


/**
 * Provides synchronization for scrolling of work sheets displaying
 * irregular time series.
 * <p>
 * Notes: 
 * <ul>
 * <li>getAdjustmentType will ALWAYS return track so can't use it to
 *     determine search direction.
 * <li>All of the irregularTS worksheets use this AdjustmentListener,
 *     so be careful to remove listeners for worksheets being manipulated. 
 * </ul>
 * @author dre
 */
class IrregularAdjustmentListener implements AdjustmentListener
{
  private  boolean _debug = false;
  private  boolean _isAdjusting = false;
  private  AdjustmentListener savedAdjustmentListeners[];
 
  /**
   * Provides synchronized scrolling for irregular worksheets by
   * responding to their respective vertical scroll bar adjustment
   * events.
   * <p>
   * Irregular TS may have dates at irregular date/times, therefore
   * the date/time of a row in one table may not line up horizontally
   * with the same row number in another table.
   * <p> 
   * Synchronized scrolling attempts to keep the top row of the worksheets
   * at the same date for all worksheets provided the date/time exists.
   * Otherwise it tries to align them as closely as possible.
   */  
  public void adjustmentValueChanged(AdjustmentEvent e) 
  {
    if (_isAdjusting) return;
    if (!__scrollIrregularSychnonously) return;
    Object source = e.getSource();

    if (source instanceof JComponent)
      {
        JComponent jComp = (JComponent)source;
        if (_debug) System.out.println(">>> " + jComp.getName());
        JWorksheet jWorksheet= (JWorksheet)jComp.getClientProperty("Worksheet");
        Rectangle vis=jWorksheet.getVisibleRect(); // get the current viewport

        Point upperLeft=vis.getLocation();
        int row=jWorksheet.rowAtPoint(upperLeft);
        int col=jWorksheet.columnAtPoint(upperLeft);

        if (_debug) System.out.println("Row/Col :" + row +","+ col);
        Object valueNew =  jWorksheet.getModel().getValueAt(row, 0);
        if (_debug) System.out.println("  type --> " + valueNew.getClass().getName());


        if (valueNew instanceof DateTime)
          {
            if (_debug) System.out.println("Adj true");

            _isAdjusting = true;
            DateTime dateTime = (DateTime)valueNew;
            if (_debug) System.out.println("Row/Col  date:" + row +","+ col + "  "+ dateTime);
            // send msg to all observers except self!
            // to notify listeners just call
            int nWS = __irregularWorksheets.length;

            for (int iWS =0; iWS < nWS; iWS++)
              {
                if (__irregularWorksheets[iWS] ==jWorksheet)
                  continue;                        
                if (_debug) System.out.println(" ... scroll worksheet: " + iWS);
                /*
                 * remove adjustmentListeners, so events won't propagate to
                 * other irregularScrollPanes - add them back afterwards
                 */ 
                JScrollBar sBar = __irregularScrollPanes[iWS].getVerticalScrollBar();
                savedAdjustmentListeners = sBar.getAdjustmentListeners();
                for (int iListener = 0; iListener < savedAdjustmentListeners.length; iListener++)
                  {
                    sBar.removeAdjustmentListener(savedAdjustmentListeners[iListener]);
                  }
                /* Use top row of worksheet to start search 
                 * - if already sychronized scrolling it should be close
                 * - ensures row exists
                 */
                JWorksheet tmpWorksheet =__irregularWorksheets[iWS];
                vis=tmpWorksheet.getVisibleRect(); // get the current viewport
                upperLeft=vis.getLocation();
                row=tmpWorksheet.rowAtPoint(upperLeft);
                
                int r = JTableUtil.find(tmpWorksheet,dateTime, 0, row);
                if (r > -1)
                  {
                    DateTime dt = ((DateTime)((tmpWorksheet.getModel().getValueAt(r, 0))));
                    if (_debug) System.out.println("   --> iWS: "+ iWS + " r: " + r + "  @ " + dt);

                    JTableUtil.scrollRowToTop(tmpWorksheet, r);
                  }
                else
                  {
                    // System.out.println("   --> " + r);
                  }
                // restore listeners
                for (int iListener = 0; iListener < savedAdjustmentListeners.length; iListener++)
                  {
                    sBar.addAdjustmentListener(savedAdjustmentListeners[iListener]);
                  }
              }
            if (_debug) System.out.println("Adj false");
            _isAdjusting= false;
          }
      }
  }
  

  
 
} // eof class IrregularAdjustmentListener
} // eof class TSViewTableJFrame


 /**
  * Provides useful utility methods for JTable.
  * 
  * @author dre
  */
class JTableUtil
{
  /**
   * Returns the row containing the closest match to the specified 
   * value in the specified column.
   * <p>
   * The specified column must be ordered in ascending order.
   * <p>
   * The direction of search is determined by the relationship of the 
   * target value and startingRow.
   * <br>
   * If the startingRow value is greater than the specified value, 
   * the search will be upwards, and the match will be for the first
   * value that is equal to or less than the target value.
   * <br>
   * Conversely, if the startingRow is less than the specified value, 
   * the search will be downwards, and the match will be the first value
   * that is equal to or greater than the target value. 
   * 
   * @param worksheet
   * @param value
   * @param column
   * @param startingRow
   * @return
   */
  public static int find(JWorksheet worksheet, DateTime value, 
          int column, int startingRow)
  {

    // TODO: dre make sure specified column holds DateTime
    // Strangely, class is reported as string... maybe because it is most general super class
//if (!(c == DateTime.class)) {
//return -1;
//}
    // 
    int flags = JWorksheet.FIND_WRAPAROUND 
    | JWorksheet.FIND_EQUAL_TO
    | JWorksheet.FIND_GREATER_THAN;

    DateTime rowVal = ((DateTime)((worksheet.getModel().getValueAt(startingRow,
            column))));

    int result = rowVal.compareTo(value);
    
    if (result == 0)
      {
        return startingRow;
      }
    else if(result < 0)
      {
        flags = JWorksheet.FIND_WRAPAROUND 
        | JWorksheet.FIND_EQUAL_TO
        | JWorksheet.FIND_GREATER_THAN;
      }
    else 
      {
       flags =  JWorksheet.FIND_WRAPAROUND 
       | JWorksheet.FIND_EQUAL_TO
       | JWorksheet.FIND_LESS_THAN
       | JWorksheet.FIND_REVERSE;
      }
    return JTableUtil.find(worksheet, value, 0, startingRow, flags);
  }
  
  /**
  Finds the first row containing the specified value in the specified column
  @param value the value to match
  @param absoluteColumn the <b>absolute</b> column number to search.  Columns are 
  numbered starting at 0, and 0 is usually the row count column.
  @param startingRow the row to start searching from
  @param flags a bit-mask of flags specifying how to search
  @return the index of the row containing the value, or -1 if not found (also
  -1 if the column class is not Date).
  */
  public static int find(JWorksheet worksheet, DateTime value, int absoluteColumn, int startingRow, int flags) { 
    if (flags == 0) {
      flags = JWorksheet.FIND_EQUAL_TO;
    }

    boolean wrap = false;
    if ((flags & JWorksheet.FIND_WRAPAROUND) == JWorksheet.FIND_WRAPAROUND) {
      wrap = true;
    } 
    
    // check if the starting row is out of bounds
    if (startingRow < 0) {
          return -1;
    }
    if (startingRow > (worksheet.getRowCount() - 1)) {
      if (wrap) {
        startingRow = 0;
      }
    }

    // check if the column is out of bounds
    if ((absoluteColumn < 0) ||
       // (absoluteColumn > (__columnNames.length - 1))) {
            (absoluteColumn >  worksheet.getColumnCount()-1)){
          return -1;
    }

    int visibleColumn = worksheet.getVisibleColumn(absoluteColumn);

    int endingRow = worksheet.getRowCount();
    boolean reverse = false;

    if ((flags & JWorksheet.FIND_REVERSE) == JWorksheet.FIND_REVERSE) {
      reverse = true;
      endingRow = 0;
    }
    boolean equalTo = false;
    if ((flags & JWorksheet.FIND_EQUAL_TO) == JWorksheet.FIND_EQUAL_TO) {
      equalTo = true;
    }
    boolean lessThan = false;
    if ((flags & JWorksheet.FIND_LESS_THAN) == JWorksheet.FIND_LESS_THAN) {
      lessThan = true;
    }
    boolean greaterThan = false;
    if ((flags & JWorksheet.FIND_GREATER_THAN) == JWorksheet.FIND_GREATER_THAN) {
      greaterThan = true;
    }
    
    boolean done = false;
    DateTime rowVal;
    int row = startingRow;
    int result;
    while (!done) {
      rowVal = ((DateTime)((worksheet.getModel().getValueAt(row, visibleColumn))));

      result = rowVal.compareTo(value);

      if (equalTo) {
        if (result == 0) {
          return row;
        }
      }
      if (lessThan) {
        if (result == -1) {
          return row;
        }
      }
      if (greaterThan) {
        if (result == 1) {
          return row;
        }
      }

      if (reverse) {
        row--;
        if (row < endingRow) {
          if (wrap) {
            wrap = false;
            row = worksheet.getRowCount() - 1;  
            endingRow = startingRow;
            if (row < endingRow) {
              done = true;
            }
          }
          else {
            done = true;
          }     
        }
      }
      else {
        row++;
        if (row > endingRow) {
          if (wrap) {
            wrap = false;
            row = 0;
            endingRow = startingRow;
            if (row > endingRow) {
              done = true;
            }
          }
          else { 
            done = true;
          }     
        }
      }
    }
    return -1;
  }
  /**
   * Scrolls the specified row of a JTable to the top of the view.
   * <p>
   * The table must be contained in a JViewport/JScrollPane.
   * 
   * @param table JTable containing row
   * @param row row to be displayed at the top.
   */
  public static void scrollRowToTop(JTable table, int row)
  {
    boolean debug = false;
    if (!(table.getParent() instanceof JViewport))
      {
        return;
      }
    if (debug) System.out.println("scroll2Top: scroll to row: " + row);

 // Rectangle visibleRect = viewport.getVisibleRect();
    Rectangle visibleRect = table.getVisibleRect();
    int topY = visibleRect.y;
    if (debug)  System.out.println( "   currently Top at: " + table.rowAtPoint(new Point(1,topY)));
    Rectangle cellRectangle = table.getCellRect(row, 0, true);
    if (debug)  System.out.println("   row @ " + cellRectangle );
    if (debug) System.out.println("   visibleRect : " + visibleRect );

    if (topY > cellRectangle.y)
      {
        //need to scroll UP
        if (debug) System.out.println("   scroll UP");
        //  cellRectangle.y = cellRectangle.y - visibleRect.y + topY;
        cellRectangle.y = cellRectangle.y - visibleRect.y + topY;
      }
    else
      {
        //need to scroll DOWN
        if (debug) System.out.println("   scroll DOWN");
        cellRectangle.y = cellRectangle.y + visibleRect.height- cellRectangle.height;
      }
    // Scroll the area into view
    if (debug) System.out.println("   scrolling 2: " +  cellRectangle);
    table.scrollRectToVisible(cellRectangle);
  }
}
