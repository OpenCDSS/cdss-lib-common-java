// TSViewTableJFrame - provides a table view for a list of time series and is managed by the TSViewJFrame parent

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.GRTS;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import RTi.TS.DateValueTS;
import RTi.TS.TS;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;
import RTi.TS.UnequalTimeIntervalException;
import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JWorksheet_AbstractTableModel;
import RTi.Util.GUI.JWorksheet_Header;
import RTi.Util.GUI.ResponseJDialog;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.TableModel_JFrame;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Help.URLHelp;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTable_CellRenderer;
import RTi.Util.Table.DataTable_TableModel;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.StopWatch;
import RTi.Util.Time.TimeInterval;

/**
<p>
The TSViewTableJFrame provides a table view for a list of time series and is managed by the TSViewJFrame parent.
The GUI can display multiple time series of different interval bases and multipliers.
All the TS of the same interval base and multiplier are displayed in the same Worksheet.
Different interval multiplier TS with the same interval base are shown in worksheets
next to other worksheets with the same interval base.
Minute TS are the top row, the next row are Hour TS, the next are Day TS,
the next are Month TS and the bottom row are Year TS.</p>
<p>
If there are no TS of a particular interval base (e.g., Day), the row for that kind of TS is not displayed.
If more than one interval base is to be displayed on the same form,
a JCheckbox will appear for each interval base so that it can be turned on or off.
If only one interval base appears on the form, no such Checkbox will appear.
</p>
*/
@SuppressWarnings("serial")
public class TSViewTableJFrame extends JFrame
implements ActionListener, ItemListener, MouseListener, PropertyChangeListener, WindowListener {

/**
The number of characters in the table double values:
- TODO smalers 2022-03-05 does not seem to be necessary and constrains formatting, especially numbers with high precision
*/
//private final int __OUTPUT_WIDTH = 10;

/**
JButton and JPopupMenu Strings.
*/
private final String
	BUTTON_CLOSE = "Close",
	BUTTON_GRAPH = "Graph",
	BUTTON_HELP = "Help",
	BUTTON_PROBLEMS = "Problems...",
	BUTTON_SAVE = "Save",
	BUTTON_SUMMARY = "Summary",

	// Below are popup menu events delegated to here from main JWorksheet.
	MENU_CALCULATE_STATISTICS = "Calculate Statistics",
	MENU_COPY = "Copy",
	MENU_PASTE = "Paste";

/**
Boolean to keep track of whether the main JPanel has been removed from the
Center position of the content pane and placed in the North position, or not.
The main panel will be moved to the north panel when the user has deselected
all the checkboxes on the form and has no TS interval types visible.
*/
private boolean mainJPanelNorth = false;

/**
Whether worksheets should be built in displayed in the opposite of the normal order,
from minute to year instead of year to minute.
*/
private boolean reverseNormalOrder = false;

/**
Boolean to keep track of whether the save button should always be enabled, no matter what.
This is only true if there is a single worksheet displayed on the GUI.
*/
private boolean saveAlwaysEnabled = false;

/**
The original border set up for a JScrollPane,
before the border is changed because the scrollpane's worksheet was selected.
*/
private Border originalScrollPaneBorder = null;

/**
A counter that tracks how many panels and checkboxes have been added to the main panel,
so that the Y placement in the grid bag can be done correctly.
*/
private int panelCount = 0;

/**
Checkboxes corresponding to each TS data interval for turning on and off the table displays.
*/
private JCheckBox
	minuteJCheckBox,
	hourJCheckBox,
	dayJCheckBox,
	monthJCheckBox,
	yearJCheckBox,
	irregularSecondJCheckBox,
	irregularMinuteJCheckBox,
	irregularHourJCheckBox,
	irregularDayJCheckBox,
	irregularMonthJCheckBox,
	irregularYearJCheckBox;

/**
The main panel into which the form components are added.
*/
private JPanel mainJPanel;

/**
JPanels for holding JCheckBoxes and JWorksheets for each TS data interval.
*/
private JPanel
	minuteJPanel,
	hourJPanel,
	dayJPanel,
	monthJPanel,
	yearJPanel,
	irregularSecondJPanel,
	irregularMinuteJPanel,
	irregularHourJPanel,
	irregularDayJPanel,
	irregularMonthJPanel,
	irregularYearJPanel;

/**
JScrollPanes for each of the associated ts worksheets,
in order to highlight the proper JScrollPane border after a worksheet is clicked on and selected.
*/
private List<JScrollPane> minuteScrollPanes = null;
private List<JScrollPane> hourScrollPanes = null;
private List<JScrollPane> dayScrollPanes = null;
private List<JScrollPane> monthScrollPanes = null;
private List<JScrollPane> yearScrollPanes = null;
private List<JScrollPane> irregularSecondScrollPanes = null;
private List<JScrollPane> irregularMinuteScrollPanes = null;
private List<JScrollPane> irregularHourScrollPanes = null;
private List<JScrollPane> irregularDayScrollPanes = null;
private List<JScrollPane> irregularMonthScrollPanes = null;
private List<JScrollPane> irregularYearScrollPanes = null;

/**
The scroll pane of the last worksheet that was clicked on and selected.
*/
private JScrollPane lastSelectedScrollPane = null;

/**
The status bar message text field.
*/
private JTextField messageJTextField = null;

/**
Arrays containing all the different JWorksheets (one for each interval type) for each different TS.
*/
private List<JWorksheet> minuteWorksheets = null;
private List<JWorksheet> hourWorksheets = null;
private List<JWorksheet> dayWorksheets = null;
private List<JWorksheet> monthWorksheets = null;
private List<JWorksheet> yearWorksheets = null;
private List<JWorksheet> irregularSecondWorksheets = null;
private List<JWorksheet> irregularMinuteWorksheets = null;
private List<JWorksheet> irregularHourWorksheets = null;
private List<JWorksheet> irregularDayWorksheets = null;
private List<JWorksheet> irregularMonthWorksheets = null;
private List<JWorksheet> irregularYearWorksheets = null;

/**
The last worksheet that was clicked on and selected.
*/
private JWorksheet lastSelectedWorksheet = null;

/**
Properties for the table display UI.
*/
private PropList props;

/**
Buttons that appear at the bottom of the GUI, for doing special operations.
*/
private SimpleJButton
	closeJButton,
	graphJButton,
	problemsJButton,
	saveJButton,
	summaryJButton;

/**
 * Choice for whether to show the data flag as a subscript showing the flags.
 */
private SimpleJComboBox flagJComboBox = null;

/**
 * Choice for the number of digits shown.
 */
private SimpleJComboBox digitsJComboBox = null;

/**
TSViewJFrame parent that displays this gui.
*/
private TSViewJFrame tsviewJFrame;

/**
 * Metadata for keeping track of worksheets.
 * This is updated by the calculateVisibleWorksheetsByPanel() method.
 */
private TSViewTableJFrameMeta meta = new TSViewTableJFrameMeta();

/**
Table models for each of the different worksheets (one for each interval type) for each different TS.
*/
private List<TSViewTable_TableModel> minuteModels = new ArrayList<>();
private List<TSViewTable_TableModel> hourModels = new ArrayList<>();
private List<TSViewTable_TableModel> dayModels = new ArrayList<>();
private List<TSViewTable_TableModel> monthModels = new ArrayList<>();
private List<TSViewTable_TableModel> yearModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularSecondModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularMinuteModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularHourModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularDayModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularMonthModels = new ArrayList<>();
private List<TSViewTable_TableModel> irregularYearModels = new ArrayList<>();

/**
Lists of the mouse listeners that have been set up for all of the different kinds of worksheets.
These are used in order to find which worksheet was clicked on after a mouse press on the JScrollPane associated with a worksheet.
*/
private List<List<MouseListener>>
	minuteMouseListeners,
	hourMouseListeners,
	dayMouseListeners,
	monthMouseListeners,
	yearMouseListeners,
	irregularSecondMouseListeners,
	irregularMinuteMouseListeners,
	irregularHourMouseListeners,
	irregularDayMouseListeners,
	irregularMonthMouseListeners,
	irregularYearMouseListeners;

/**
List of Time Series to be displayed in the GUI.
'tslist' is set from a list passed in to this GUI at construction,
and then the other lists are formed from the TS split out of __tslist.
*/
private List<TS> tslist; // All time series.
private List<TS> minuteTSList;
private List<TS> hourTSList;
private List<TS> dayTSList;
private List<TS> monthTSList;
private List<TS> yearTSList;
private List<TS> irregularSecondTSList; // All irregular interval time series with date/time precision <= second precision.
private List<TS> irregularMinuteTSList;
private List<TS> irregularHourTSList;
private List<TS> irregularDayTSList;
private List<TS> irregularMonthTSList;
private List<TS> irregularYearTSList;

/**
List of problems from creating table models.
*/
private List<String> tableModelProblems = null;

/**
Constructor.
@param tsviewJFrame Parent TSViewJFrame.
@param tslist List of time series to view.
@param props Properties for display (currently same list passed in to TSViewJFrame).
*/
public TSViewTableJFrame(TSViewJFrame tsviewJFrame, List<TS> tslist, PropList props) {
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );

	this.tsviewJFrame = tsviewJFrame;
	this.tslist = tslist;
	this.props = props;

	String propValue = this.props.getValue("TSViewTitleString");

	if ( propValue == null ) {
		if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "Time Series - Table" );
		}
		else {
		    setTitle( JGUIUtil.getAppNameForWindows() + " - Time Series - Table" );
		}
	}
	else {
	    if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( propValue + " - Table" );
		}
		else {
		    setTitle( JGUIUtil.getAppNameForWindows() + " - " + propValue + " - Table" );
		}
	}

	propValue = this.props.getValue("TSViewTableOrder");
	if (propValue != null) {
		if (propValue.equalsIgnoreCase("FineToCoarse")) {
			this.reverseNormalOrder = true;
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

	if (command.equals(this.BUTTON_CLOSE)) {
		this.tsviewJFrame.closeGUI(TSViewType.TABLE);
	}
	else if (command.equals(this.BUTTON_GRAPH)) {
		this.tsviewJFrame.openGUI(TSViewType.GRAPH);
	}
	else if (command.equals(this.BUTTON_HELP)) {
		URLHelp.showHelpForKey("TSView.Table");
	}
	else if (command.equals(this.BUTTON_PROBLEMS)) {
		showProblems();
	}
	else if (command.equals(this.BUTTON_SUMMARY)) {
		this.tsviewJFrame.openGUI(TSViewType.SUMMARY);
	}
	else if (command.equals(this.BUTTON_SAVE)) {
		saveClicked();
	}
	else if (command.equals(this.MENU_CALCULATE_STATISTICS) ) {
		// An event was generated in a JWorksheet requesting that statistics be calculated:
		// - assume that the event originated from the last selected worksheet
		//   (not sure otherwise how to get the worksheet)
		try {
			calculateAndDisplayStatistics(this.lastSelectedWorksheet);
		}
		catch ( Exception e ) {
			Message.printWarning(1,"","Error calculating statistics (" + e + ").");
			Message.printWarning(2, "", e);
		}
	}
	else if (command.equals(this.MENU_COPY)) {
		// TODO sam 2017-04-01 why is this event handled here?
		// - a Copy popup menu is provided in the JWorksheet by default
		// - does this ever get called?
		// - maybe this was in place for much earlier code when Copy/Paste were buttons like the above
		// - comment out for now since worksheet will handle the event
		Message.printStatus(2, "", "Copy action event disabled in TSViewTableJFrame");
		//this.lastSelectedWorksheet.copyToClipboard();
	}
	else if (command.equals(this.MENU_PASTE)) {
		// TODO sam 2017-04-01 why is this event handled here?
		// - a Paste popup menu is provided in the JWorksheet by default
		// - does this ever get called?
		Message.printStatus(2, "", "Paste action event disabled in TSViewTableJFrame");
		//this.lastSelectedWorksheet.pasteFromClipboard();
	}
}

/**
Adds worksheets of the same interval base to their panel and also adds the appropriate check box,
as long as more than one kid of interval base will be displayed on the GUI.
@param panel the main panel to which to add the panels (this.mainJPanel).
@param subPanel the panel to which to add the checkbox and worksheets.
@param intervalDescription a text string that concisely names the kind of interval being dealt with
@param checkBox the check box to (possibly) add to the panel.
@param worksheets the worksheets to add to the panel.
@param scrollpanes the scrollpanes for each worksheet
@param mouseListeners a list of lists used to store mouse listeners for the
worksheets' scrollpanes so that the last selected worksheet can be tracked.
*/
private void addWorksheetsToPanel (
	JPanel panel,
	String intervalDescription,
	JPanel subPanel,
	JCheckBox checkBox,
	List<JWorksheet> worksheets,
	List<JWorksheet> headers,
	List<JScrollPane> scrollPanes,
	List<List<MouseListener>> mouseListeners) {
    if ( Message.isDebugOn ) {
        Message.printDebug(1,"TSViewTableJFrame.addWorksheetsToPanel","panel="+panel+" intervalDescription="+intervalDescription+
            " subPanel=" + subPanel + " checkBox=" + checkBox + " worksheets=" + worksheets + " headers=" + headers +
            " scrollPanes=" + scrollPanes + " mouseListeners=" + mouseListeners );
    }
	if ( (worksheets == null) || worksheets.isEmpty() ) {
		// There are no worksheets for the current interval type.
		checkBox.setSelected(false);
		return;
	}

	int numWorksheets = worksheets.size();

	// Create the panel into which these worksheets will be placed, and give it a GridBagLayout.
	GridBagLayout gbl = new GridBagLayout();
	subPanel.setLayout(gbl);

	// For each worksheet, create its scrollpane and add the scrollpane to the panel.
	// Also add mouse listeners to the scrollpane and its
	// scrollbars so that it can be determined after a mouse click which
	// worksheet (or worksheet's scrollpane components) was clicked on.
	// Put the registered mouse listeners into a list for this purpose.
	for ( int i = 0; i < numWorksheets; i++ ) {
		JScrollPane scrollPane = new JScrollPane(worksheets.get(i));
		scrollPanes.add(scrollPane);

		if (IOUtil.isUNIXMachine()) {
			// For the reason why this is done, see JWorksheet.adjustmentValueChanged().
			scrollPane.getVerticalScrollBar().addAdjustmentListener(worksheets.get(i));
			scrollPane.getHorizontalScrollBar().addAdjustmentListener(worksheets.get(i));
		}

		// Add lots of mouse listeners, so that (hopefully) anywhere a user clicks it will count as a click on the worksheet.

		JWorksheet worksheet = worksheets.get(i);
		worksheet.addMouseListener(this);

		// The header worksheet will be used to control selection
		// events on the other worksheets for which the header worksheet is the row header.
		headers.get(i).setRowSelectionModelPartner(worksheet.getRowSelectionModel());

		worksheet.addHeaderMouseListener(this);

		List<MouseListener> worksheetMouseListeners = new ArrayList<>();
		if ( scrollPane instanceof MouseListener ) {
			// TODO sam 2017-04-01 The following will throw a ClassCastException if now wrapped in the "if".
			// Similar checks below for the scrollbars behave the same.
			// Maybe this is old code that is ineffective and can be removed.
			// The underlying UI components check for whether the object implements MouseListener
			// and apparently these objects do not... so no listener code would be called anyway.
			worksheetMouseListeners.add((MouseListener)scrollPane);
		}

		scrollPane.addMouseListener(worksheet);
		if (scrollPane.getVerticalScrollBar() instanceof MouseListener ) {
			worksheetMouseListeners.add((MouseListener)scrollPane.getVerticalScrollBar());
		}

		scrollPane.getVerticalScrollBar().addMouseListener(worksheet);
		if (scrollPane.getHorizontalScrollBar() instanceof MouseListener ) {
			worksheetMouseListeners.add((MouseListener)scrollPane.getHorizontalScrollBar());
		}

		scrollPane.getHorizontalScrollBar().addMouseListener(worksheet);
        JGUIUtil.addComponent(subPanel, scrollPane,
			i, 0, 1, 1, 1, 1,
			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);

		// The next line looks weird, but is done because somewhere
		// the pointer to the worksheet that the models have is getting mis-pointed.
        // This makes sure the models know their worksheet.
		((TSViewTable_TableModel)worksheet.getModel()).setWorksheet(worksheet);
		mouseListeners.set(i, worksheetMouseListeners);
	}

	// If only one panel of worksheet data will appear on the GUI
	// (and each GUI panel contains the data for a specific Interval base)
	// then it's not necessary to include the checkbox for selecting and deselecting specific interval bases.
	if (calculateNumberOfPanels() > 1) {
		JGUIUtil.addComponent(panel, checkBox,
			0,this.panelCount++, 1, 1, 1, 0,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	}

	subPanel.setBorder(BorderFactory.createTitledBorder(intervalDescription + " Interval"));
      	JGUIUtil.addComponent(panel, subPanel,
		0, this.panelCount++, 1, 1, 1, 1,
		GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
}

/**
Builds the mouse listener list for the given worksheet array.  The list data is not populated.
@param worksheets the array for which to build the mouse listener array
@return the mouse listener list.
*/
private List<List<MouseListener>> buildMouseListeners(List<JWorksheet> worksheets) {
	if (worksheets == null) {
		return null;
	}

	int numWorksheets = worksheets.size();
	List<List<MouseListener>> mouseListenerList = new ArrayList<>(numWorksheets);
	// Fill in the listeners with null so there is at least a slot:
	// - this was the behavior in legacy code that used an array of List
	for ( int i = 0; i < numWorksheets; i++ ) {
		mouseListenerList.add(null); // Other code will set the list at this position.
	}
	return mouseListenerList;
}

/**
 * Copy the selected cells (or all none are selected) to a new worksheet,
 * add a column for the statistic type, calculate the statistics, and display in a new worksheet.
 * This method handles generic JWorksheets.
 * If more specific behavior is needed
 * (for example time series data with potentially inconsistent missing data values),
 * then create a JWorksheet and set the properties
 * <pre>
 * JWorksheet.AllowCalculateStatistics=true
 * JWorksheet.DelegateCalculateStatistics=true
 * </pre>
 * The latter property will tell this class to not process the "Calculate Statistics"
 * action event and let another registered ActionListener handle.
 * If the Calculate Statistics functionality is enabled and not delegated,
 * then this method is called.
 * This code is substantially copied from JWorksheet, which was substantially guided by JWorksheet_CopyPasteAdapter.
 */
private void calculateAndDisplayStatistics ( JWorksheet worksheet ) throws Exception {
	//String routine = "calculateAndDisplayStatistics";
	try {
		int numSelectedCols = worksheet.getSelectedColumnCount();
		int numSelectedRows = worksheet.getSelectedRowCount();
		int[] selectedRows = worksheet.getSelectedRows();
		int[] selectedCols = worksheet.getSelectedColumns();
		int[] visibleCols = new int[selectedCols.length];
		for (int icol = 0; icol < selectedCols.length; icol++) {
			visibleCols[icol] = worksheet.getVisibleColumn(selectedCols[icol]);
		}

		// No data to process.
		if (numSelectedCols == 0 || numSelectedRows == 0) {
			JGUIUtil.setWaitCursor(worksheet.getHourglassJFrame(), false);
			return;
		}

		/** TODO sam 2017-04-01 the following may or may not be helpful:
		 *  - the statistics code implemented below processes the bounding block rather than specific selections
		if (numSelectedRows == 1 && numSelectedCols == 1) {
			// Trivial case -- this will always be a successful copy.  This case is just a placeholder.
		}
		else if (numSelectedRows == 1) {
			// The rows are valid; the only thing left to check is whether the columns are contiguous.
			if (!areCellsContiguous(numSelectedRows, selectedRows, numSelectedCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block of columns.");
				return;
			}
		}
		else if (numSelectedCols == 1) {
			// The cols are valid; the only thing left to check is whether the rows are contiguous.
			if (!areCellsContiguous(numSelectedRows, selectedRows, numSelectedCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block of rows.");
				return;
			}
		}
		else {
			// There are multiple rows selected and multiple columns selected.  Make sure both are contiguous.
			if (!areCellsContiguous(numSelectedRows, selectedRows, numSelectedCols, visibleCols)) {
				showCopyErrorDialog("You must select a contiguous block\nof rows and columns.");
				return;
			}
		}
		*/

		int numColumns = worksheet.getColumnCount();
		@SuppressWarnings("rawtypes")
		Class[] classes = new Class[numColumns];
		boolean [] canCalcStats = new boolean[numColumns];
		// Arrays for statistics.
		int count [] = new int[numColumns];
		// Allocate arrays for all columns, but only some will be used.
		// Use highest precision types and then cast to lower if needed.
		// For floating point results,
		// time series will generally only have double values but leave other
		// cases in order to handle separate columns for flags, etc. that may be added to table.
		double min [] = new double[numColumns];
		double max [] = new double[numColumns];
		double sum [] = new double[numColumns];
		// For integer results.
		long imin [] = new long[numColumns];
		long imax [] = new long[numColumns];
		long isum [] = new long[numColumns];
		// Time series are needed to determine the missing data value.
		TS[] ts = new TS[numColumns];
		int [] tsPrec = new int[numColumns];
		List<TS> tslist = (List<TS>)((TSViewTable_TableModel)worksheet.getTableModel()).getData();
		String precisionProp = this.props.getValue("OutputPrecision");
		for (int icol = 0; icol < numSelectedCols; icol++) {
			classes[icol] = worksheet.getColumnClass(worksheet.getAbsoluteColumn(selectedCols[icol]));
			canCalcStats[icol] = false;
			count[icol] = 0;
			sum[icol] = Double.NaN;
			min[icol] = Double.NaN;
			max[icol] = Double.NaN;
			isum[icol] = 0;
			imin[icol] = Long.MAX_VALUE;
			imax[icol] = Long.MIN_VALUE;
			// There are no hidden columns but need to align the time series with selected columns:
			// - first column is date/time
			// - therefore time series 0 is actually in column 1
			int absCol = worksheet.getAbsoluteColumn(selectedCols[icol]);
			ts[icol] = null;
			if ( absCol != 0 ) {
				// If 0 need to skip the date/time column.
				ts[icol] = tslist.get(absCol - 1);
				if ( ts[icol] != null ) {
					// Calculate the output precision of the current TS's data.
					tsPrec[icol] = 2;
					if (precisionProp != null) {
						tsPrec[icol] = StringUtil.atoi(precisionProp);
					}
					else {
						try {
							DataUnits units = DataUnits.lookupUnits(ts[icol].getDataUnits());
							tsPrec[icol] = units.getOutputPrecision();
						}
						catch (Exception e) {
							// Use the default.
							tsPrec[icol] = 2;
						}
					}
				}
			}
		}

		// Initialize the list of table fields that contains a leftmost column "Statistic".
		List<TableField> tableFieldList = new ArrayList<>();
		tableFieldList.add(new TableField(TableField.DATA_TYPE_STRING, "Statistic", -1, -1));
		// Add columns for the selected columns.
		boolean copyHeader = true;
		if (copyHeader) {
			int width = -1;
			// Determine the precision from the time series.
			for (int icol = 0; icol < numSelectedCols; icol++) {
				if ( classes[icol] == Double.class) {
					tableFieldList.add(new TableField(TableField.DATA_TYPE_DOUBLE,
						worksheet.getColumnName(selectedCols[icol], true), width, tsPrec[icol]));
					canCalcStats[icol] = true;
				}
				else if ( classes[icol] == Float.class) {
					tableFieldList.add(new TableField(TableField.DATA_TYPE_FLOAT,
						worksheet.getColumnName(selectedCols[icol], true), width, tsPrec[icol]));
					canCalcStats[icol] = true;
				}
				else if ( classes[icol] == Integer.class) {
					tableFieldList.add(new TableField(TableField.DATA_TYPE_INT,
							worksheet.getColumnName(selectedCols[icol], true), width, -1));
					canCalcStats[icol] = true;
				}
				else if ( classes[icol] == Long.class) {
					tableFieldList.add(new TableField(TableField.DATA_TYPE_LONG,
						worksheet.getColumnName(selectedCols[icol], true), width, -1));
					canCalcStats[icol] = true;
				}
				else {
					// Add a string class.
					tableFieldList.add(new TableField(TableField.DATA_TYPE_STRING,
						worksheet.getColumnName(selectedCols[icol], true), width, -1));
					canCalcStats[icol] = false;
				}
			}
		}

		// Create the table.
		DataTable table = new DataTable(tableFieldList);

		JWorksheet_AbstractTableModel tableModel = worksheet.getTableModel();
		// Transfer the data from the worksheet to the subset table.
		Object cellContents;
		for (int irow = 0; irow < numSelectedRows; irow++) {
			TableRecord rec = table.emptyRecord();
			rec.setFieldValue(0, ""); // Blanks for most rows until the statistics added at the end.
			for (int icol = 0; icol < numSelectedCols; icol++) {
				cellContents = tableModel.getValueAt(selectedRows[irow],selectedCols[icol]);
				if ( cellContents instanceof Double ) {
					Double d = (Double)cellContents;
					if ( (ts[icol] != null) && ts[icol].isDataMissing(d) ) {
						// Set to null to let the worksheet handle generically - NaN displays as NaN.
						//rec.setFieldValue((icol + 1), Double.NaN);
						rec.setFieldValue((icol + 1), null);
					}
					else {
						rec.setFieldValue((icol + 1), cellContents);
					}
				}
				else {
					rec.setFieldValue((icol + 1), cellContents);
				}
				// Transfer values to the output cells and calculate the statistics.
				if ( cellContents != null ) {
					// Column type allows calculating statistics so do some basic math.
					if ( canCalcStats[icol] ) {
						if ( (classes[icol] == Double.class) ) {
							Double d = (Double)cellContents;
							if ( !d.isNaN() && (ts[icol] != null) && !ts[icol].isDataMissing(d) ) {
								++count[icol];
								// Sum, used directly and for mean.
								if ( Double.isNaN(sum[icol]) ) {
									sum[icol] = d;
								}
								else {
									sum[icol] += d;
								}
								// Min statistic.
								if ( Double.isNaN(min[icol]) ) {
									min[icol] = d;
								}
								else if ( d < min[icol] ){
									min[icol] = d;
								}
								// Max statistic.
								if ( Double.isNaN(max[icol]) ) {
									max[icol] = d;
								}
								else if ( d > max[icol] ){
									max[icol] = d;
								}
							}
						}
						else if ( (classes[icol] == Float.class) ) {
							Float f = (Float)cellContents;
							if ( !f.isNaN() ) {
								++count[icol];
								// Sum, used directly and for mean.
								if ( Double.isNaN(sum[icol]) ) {
									sum[icol] = f;
								}
								else {
									sum[icol] += f;
								}
								// Min statistic.
								if ( Double.isNaN(min[icol]) ) {
									min[icol] = f;
								}
								else if ( f < min[icol] ){
									min[icol] = f;
								}
								// Max statistic.
								if ( Double.isNaN(max[icol]) ) {
									max[icol] = f;
								}
								else if ( f > max[icol] ){
									max[icol] = f;
								}
							}
						}
						else if ( (classes[icol] == Integer.class) ) {
							Integer i = (Integer)cellContents;
							// No concept of NaN so previous null check is main check for missing.
							++count[icol];
							// Sum, used directly and for mean.
							sum[icol] += i;
							// Min statistic.
							if ( imin[icol] == Long.MAX_VALUE ) {
								imin[icol] = i;
							}
							else if ( i < imin[icol] ){
								imin[icol] = i;
							}
							// Max statistic.
							if ( imax[icol] == Long.MIN_VALUE ) {
								imax[icol] = i;
							}
							else if ( i > imax[icol] ){
								imax[icol] = i;
							}
						}
						else if ( (classes[icol] == Long.class) ) {
							Long i = (Long)cellContents;
							// No concept of NaN so previous null check is main check for missing.
							++count[icol];
							// Sum, used directly and for mean.
							sum[icol] += i;
							// Min statistic.
							if ( imin[icol] == Long.MAX_VALUE ) {
								imin[icol] = i;
							}
							else if ( i < imin[icol] ){
								imin[icol] = i;
							}
							// Max statistic.
							if ( imax[icol] == Long.MIN_VALUE ) {
								imax[icol] = i;
							}
							else if ( i > imax[icol] ){
								imax[icol] = i;
							}
						}
					}
				}
			}
			table.addRecord(rec);
		}
		// Add statistics at the bottom.
		TableRecord rec = table.emptyRecord();
		rec.setFieldValue(0, "Count");
		for ( int icol = 0; icol < numSelectedCols; icol++ ) {
			// TODO sam 2017-04-01 Worksheet should handle case even when object
			// is a different type than the column, but this is generally not done in tables.
			if ( canCalcStats[icol]) {
				rec.setFieldValue((icol+1), Integer.valueOf(count[icol]));
			}
		}
		table.addRecord(rec);
		rec = table.emptyRecord();
		rec.setFieldValue(0, "Mean");
		for ( int icol = 0; icol < numSelectedCols; icol++ ) {
			if ( canCalcStats[icol]) {
				if ( classes[icol] == Double.class) {
					if ( (count[icol] > 0) && !Double.isNaN(sum[icol]) ) {
						rec.setFieldValue((icol+1), Double.valueOf(sum[icol])/count[icol]);
					}
				}
				else if ( classes[icol] == Float.class) {
					if ( (count[icol] > 0) && !Double.isNaN(sum[icol]) ) {
						rec.setFieldValue((icol+1), Float.valueOf((float)sum[icol])/count[icol]);
					}
				}
				else if ( classes[icol] == Long.class) {
					if ( count[icol] > 0 ) {
						rec.setFieldValue((icol+1), Long.valueOf(isum[icol])/count[icol]);
					}
				}
				else if ( classes[icol] == Integer.class) {
					if ( count[icol] > 0 ) {
						rec.setFieldValue((icol+1), Integer.valueOf((int)isum[icol])/count[icol]);
					}
				}
			}
		}
		table.addRecord(rec);
		rec = table.emptyRecord();
		rec.setFieldValue(0, "Min");
		for ( int icol = 0; icol < numSelectedCols; icol++ ) {
			if ( canCalcStats[icol]) {
				if ( classes[icol] == Double.class) {
					if ( !Double.isNaN(min[icol]) ) {
						rec.setFieldValue((icol+1), Double.valueOf(min[icol]));
					}
				}
				else if ( classes[icol] == Float.class) {
					if ( !Double.isNaN(min[icol]) ) {
						rec.setFieldValue((icol+1), Float.valueOf((float)min[icol]));
					}
				}
				else if ( classes[icol] == Long.class) {
					if ( imin[icol] != Long.MAX_VALUE ) {
						rec.setFieldValue((icol+1), Long.valueOf(imin[icol]));
					}
				}
				else if ( classes[icol] == Integer.class) {
					if ( imin[icol] != Long.MAX_VALUE ) {
						rec.setFieldValue((icol+1), Integer.valueOf((int)imin[icol]));
					}
				}
			}
		}
		table.addRecord(rec);
		rec = table.emptyRecord();
		rec.setFieldValue(0, "Max");
		for ( int icol = 0; icol < numSelectedCols; icol++ ) {
			if ( canCalcStats[icol]) {
				if ( classes[icol] == Double.class) {
					if ( !Double.isNaN(max[icol]) ) {
						rec.setFieldValue((icol+1), Double.valueOf(max[icol]));
					}
				}
				else if ( classes[icol] == Float.class) {
					if ( !Double.isNaN(max[icol]) ) {
						rec.setFieldValue((icol+1), Float.valueOf((float)max[icol]));
					}
				}
				else if ( classes[icol] == Long.class) {
					if ( imax[icol] != Long.MIN_VALUE ) {
						rec.setFieldValue((icol+1), Long.valueOf(imax[icol]));
					}
				}
				else if ( classes[icol] == Integer.class) {
					if ( imax[icol] != Long.MIN_VALUE ) {
						rec.setFieldValue((icol+1), Long.valueOf(imax[icol]));
					}
				}
			}
		}
		table.addRecord(rec);
		rec = table.emptyRecord();
		rec.setFieldValue(0, "Sum");
		for ( int icol = 0; icol < numSelectedCols; icol++ ) {
			if ( canCalcStats[icol]) {
				if ( classes[icol] == Double.class) {
					if ( !Double.isNaN(sum[icol]) ) {
						rec.setFieldValue((icol+1), Double.valueOf(sum[icol]));
					}
				}
				else if ( classes[icol] == Float.class) {
					if ( !Double.isNaN(sum[icol]) ) {
						rec.setFieldValue((icol+1), Float.valueOf((float)sum[icol]));
					}
				}
				else if ( classes[icol] == Long.class) {
					rec.setFieldValue((icol+1), Long.valueOf(isum[icol]));
				}
				else if ( classes[icol] == Integer.class) {
					rec.setFieldValue((icol+1), Integer.valueOf((int)isum[icol]));
				}
			}
		}
		table.addRecord(rec);

		DataTable_TableModel dttm = new DataTable_TableModel ( table );
		DataTable_CellRenderer scr = new DataTable_CellRenderer ( dttm );
		PropList frameProps = new PropList("");
		frameProps.set("Title","Time Series Table Statistics");
		PropList worksheetProps = new PropList("");
		worksheetProps.add("JWorksheet.OneClickColumnSelection=true");
		worksheetProps.add("JWorksheet.RowColumnPresent=true");
		worksheetProps.add("JWorksheet.ShowPopupMenu=true");
		worksheetProps.add("JWorksheet.SelectionMode=ExcelSelection");
		worksheetProps.add("JWorksheet.AllowCopy=true");
		// The following will be default center on its parent and be shown in front.
		TableModel_JFrame f = new TableModel_JFrame(dttm, scr, frameProps, worksheetProps);
		JGUIUtil.center(f,this);
		f.toFront(); // This does not seem to always work.
		f.setAlwaysOnTop(true); // TODO sam 2017-04-01 don't like to do this but seems necessary.
	}
	catch ( Exception e ) {
		new ResponseJDialog(worksheet.getHourglassJFrame(),
			"Error", "Error calculating statistics.", ResponseJDialog.OK).response();
		Message.printWarning(2, "", e);
	}
}

/**
Calculates the number of panels with worksheets that will be added in total
to the this.mainJPanel depending on the number of base intervals with time series.
@return the number of panels with worksheets added to this.mainJPanel.
*/
private int calculateNumberOfPanels() {
	int size = 0;
	if ( (this.minuteWorksheets != null) && (this.minuteWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.hourWorksheets != null) && (this.hourWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.dayWorksheets != null) && (this.dayWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.monthWorksheets != null) && (this.monthWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.yearWorksheets != null) && (this.yearWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.irregularSecondTSList != null) && (this.irregularSecondWorksheets.size() > 0) ) {
		size++;
	}
	if ( (this.irregularMinuteTSList != null) && (this.irregularMinuteWorksheets.size() > 0) ) {
		size++;
	}
    if ( (this.irregularHourTSList != null) && (this.irregularHourWorksheets.size() > 0) ) {
        size++;
    }
    if ( (this.irregularDayTSList != null) && (this.irregularDayWorksheets.size() > 0) ) {
        size++;
    }
    if ( (this.irregularMonthTSList != null) && (this.irregularMonthWorksheets.size() > 0) ) {
        size++;
    }
    if ( (this.irregularYearTSList != null) && (this.irregularYearWorksheets.size() > 0) ) {
        size++;
    }
	return size;
}

/**
Calculates the number of worksheets in each panel and return an integer array
that can tell exactly how many worksheets are in each panel.
The results are saved in an instance of TSViewTableJFrameMeta.
*/
private void calculateVisibleWorksheetsByPanel() {
	// Regular minute interval.
	if ( (this.minuteWorksheets == null) || !this.minuteJCheckBox.isSelected() ) {
		this.meta.numWorksheetsInMinutePanel = 0;
	}
	else {
		this.meta.numWorksheetsInMinutePanel = this.minuteWorksheets.size();
	}

	// Regular hour interval.
	if ( (this.hourWorksheets == null) || !this.hourJCheckBox.isSelected() ) {
		this.meta.numWorksheetsInHourPanel = 0;
	}
	else {
		this.meta.numWorksheetsInHourPanel = this.hourWorksheets.size();
	}

	// Regular day interval.
	if ( (this.dayWorksheets == null) || !this.dayJCheckBox.isSelected()) {
		this.meta.numWorksheetsInDayPanel = 0;
	}
	else {
		this.meta.numWorksheetsInDayPanel = this.dayWorksheets.size();
	}

	// Regular month interval.
	if ( (this.monthWorksheets == null) || !this.monthJCheckBox.isSelected()) {
		this.meta.numWorksheetsInMonthPanel = 0;
	}
	else {
		this.meta.numWorksheetsInMonthPanel = this.monthWorksheets.size();
	}

	// Regular year interval.
	if ( (this.yearWorksheets == null) || !this.yearJCheckBox.isSelected()) {
		this.meta.numWorksheetsInYearPanel = 0;
	}
	else {
		this.meta.numWorksheetsInYearPanel = this.yearWorksheets.size();
	}

	// Irregular second interval.
    if ( (this.irregularSecondWorksheets == null) || !this.irregularSecondJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularSecondPanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularSecondPanel = this.irregularSecondWorksheets.size();
    }

	// Irregular minute interval.
    if ( (this.irregularMinuteWorksheets == null) || !this.irregularMinuteJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularMinutePanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularMinutePanel = this.irregularMinuteWorksheets.size();
    }

	// Irregular hour interval.
    if ( (this.irregularHourWorksheets == null) || !this.irregularHourJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularHourPanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularHourPanel = this.irregularHourWorksheets.size();
    }

	// Irregular day interval.
    if ( (this.irregularDayWorksheets == null) || !this.irregularDayJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularDayPanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularDayPanel = this.irregularDayWorksheets.size();
    }

	// Irregular month interval.
    if ( (this.irregularMonthWorksheets == null) || !this.irregularMonthJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularMonthPanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularMonthPanel = this.irregularMonthWorksheets.size();
    }

	// Irregular year interval.
    if ( (this.irregularYearWorksheets == null) || !this.irregularYearJCheckBox.isSelected()) {
		this.meta.numWorksheetsInIrregularYearPanel = 0;
    }
    else {
		this.meta.numWorksheetsInIrregularYearPanel = this.irregularYearWorksheets.size();
    }

    this.meta.numWorksheetsTotalVisible =
    	this.meta.numWorksheetsInMinutePanel +
    	this.meta.numWorksheetsInHourPanel +
    	this.meta.numWorksheetsInDayPanel +
    	this.meta.numWorksheetsInMonthPanel +
    	this.meta.numWorksheetsInYearPanel +
    	this.meta.numWorksheetsInIrregularSecondPanel +
    	this.meta.numWorksheetsInIrregularMinutePanel +
    	this.meta.numWorksheetsInIrregularHourPanel +
    	this.meta.numWorksheetsInIrregularDayPanel +
    	this.meta.numWorksheetsInIrregularMonthPanel +
    	this.meta.numWorksheetsInIrregularYearPanel;

	this.meta.numPanelsWithVisibleWorksheets = 0;
	if ( this.meta.numWorksheetsInMinutePanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
	}
	if ( this.meta.numWorksheetsInHourPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
	}
	if ( this.meta.numWorksheetsInDayPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
	}
	if ( this.meta.numWorksheetsInMonthPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
	}
	if ( this.meta.numWorksheetsInYearPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
	}
    if ( this.meta.numWorksheetsInIrregularSecondPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
    if ( this.meta.numWorksheetsInIrregularMinutePanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
    if ( this.meta.numWorksheetsInIrregularHourPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
    if ( this.meta.numWorksheetsInIrregularDayPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
    if ( this.meta.numWorksheetsInIrregularMonthPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
    if ( this.meta.numWorksheetsInIrregularYearPanel > 0) {
		++this.meta.numPanelsWithVisibleWorksheets;
    }
}

/**
Checks to see if only a single worksheet is displayed in the GUI.
If so, it is selected and the save button is set to always be enabled.
*/
private void checkForSingleWorksheet() {
	JWorksheet worksheet = null;

	int count = 0;

	if (this.dayWorksheets != null) {
		for (int i = 0; i < this.dayWorksheets.size(); i++) {
			count++;
			worksheet = this.dayWorksheets.get(i);
		}
	}
	if (this.minuteWorksheets != null) {
		for (int i = 0; i < this.minuteWorksheets.size(); i++) {
			count++;
			worksheet = this.minuteWorksheets.get(i);
		}
	}
	if (this.hourWorksheets != null) {
		for (int i = 0; i < this.hourWorksheets.size(); i++) {
			count++;
			worksheet = this.hourWorksheets.get(i);
		}
	}
	if (this.monthWorksheets != null) {
		for (int i = 0; i < this.monthWorksheets.size(); i++) {
			count++;
			worksheet = this.monthWorksheets.get(i);
		}
	}
	if (this.yearWorksheets != null) {
		for (int i = 0; i < this.yearWorksheets.size(); i++) {
			count++;
			worksheet = this.yearWorksheets.get(i);
		}
	}
    if (this.irregularSecondWorksheets != null) {
        for (int i = 0; i < this.irregularSecondWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularSecondWorksheets.get(i);
        }
    }
    if (this.irregularMinuteWorksheets != null) {
        for (int i = 0; i < this.irregularMinuteWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularMinuteWorksheets.get(i);
        }
    }
    if (this.irregularHourWorksheets != null) {
        for (int i = 0; i < this.irregularHourWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularHourWorksheets.get(i);
        }
    }
    if (this.irregularDayWorksheets != null) {
        for (int i = 0; i < this.irregularDayWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularDayWorksheets.get(i);
        }
    }
    if (this.irregularMonthWorksheets != null) {
        for (int i = 0; i < this.irregularMonthWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularMonthWorksheets.get(i);
        }
    }
    if (this.irregularYearWorksheets != null) {
        for (int i = 0; i < this.irregularYearWorksheets.size(); i++) {
            count++;
            worksheet = this.irregularYearWorksheets.get(i);
        }
    }

	if (count != 1) {
		return;
	}

	selectWorksheet(worksheet, null);
	this.saveAlwaysEnabled = true;
	this.saveJButton.setEnabled(true);
}

/**
Takes the Time Series from the this.tslist list and puts each one into a list
specific to its data interval (e.g., Day time series go into this.day,
minute time series go into this.minute, etc).
Different multipliers are all lumped together, as long as they have the same data interval.
Irregular interval time series are split out by the interval of the starting date/time.
*/
private void createSeparateTimeSeriesLists() {
    String routine = getClass().getSimpleName() + ".createSeparateTimeSeriesLists";
	int interval;
	this.minuteTSList = new ArrayList<>();
	this.hourTSList = new ArrayList<>();
	this.dayTSList = new ArrayList<>();
	this.monthTSList = new ArrayList<>();
	this.yearTSList = new ArrayList<>();
	this.irregularSecondTSList = new ArrayList<>();
	this.irregularMinuteTSList = new ArrayList<>();
	this.irregularHourTSList = new ArrayList<>();
	this.irregularDayTSList = new ArrayList<>();
	this.irregularMonthTSList = new ArrayList<>();
	this.irregularYearTSList = new ArrayList<>();

	for ( TS ts : this.tslist ) {
		if (ts == null) {
			continue;
		}
		interval = ts.getDataIntervalBase();

		if (interval == TimeInterval.MINUTE) {
			this.minuteTSList.add(ts);
		}
		else if (interval == TimeInterval.HOUR) {
			this.hourTSList.add(ts);
		}
		else if (interval == TimeInterval.DAY) {
			this.dayTSList.add(ts);
		}
		else if (interval == TimeInterval.MONTH) {
			this.monthTSList.add(ts);
		}
		else if (interval == TimeInterval.YEAR) {
			this.yearTSList.add(ts);
		}
        else if (interval == TimeInterval.IRREGULAR) {
            // Put in the appropriate list based on the date/time of the period start.
            DateTime d = ts.getDate1();
            if ( d == null ) {
                d = ts.getDate1Original();
            }
            if ( d != null ) {
                int precision = d.getPrecision();
                if ( (precision >= DateTime.PRECISION_NANOSECOND) && (precision <= DateTime.PRECISION_SECOND) ) {
                    // Include (sub)second precision here because most likely it is minute precision with seconds = 0.
                    this.irregularSecondTSList.add(ts);
                    Message.printStatus(2,routine,"Date/time precision is <= second, using smallest precision for the SECOND table \"" +
                        ts.getIdentifierString() + "\"");
                }
                else if ( precision == DateTime.PRECISION_MINUTE ) {
                    this.irregularMinuteTSList.add(ts);
                }
                else if ( precision == DateTime.PRECISION_HOUR ) {
                    this.irregularHourTSList.add(ts);
                }
                else if ( precision == DateTime.PRECISION_DAY ) {
                    this.irregularDayTSList.add(ts);
                }
                else if ( precision == DateTime.PRECISION_MONTH ) {
                    this.irregularMonthTSList.add(ts);
                }
                else if ( precision == DateTime.PRECISION_YEAR ) {
                    this.irregularYearTSList.add(ts);
                }
                else {
                    // Don't handle the precision.
                    Message.printWarning(3,routine,"Don't know how to handle time series interval in table for \"" +
                        ts.getIdentifierString() + "\"");
                }
            }
        }
	}
}

/**
Create the table models with the same interval base for all of the worksheets for the given list of time series.
The time series will have the same base interval but if regular may have different interval multipliers.
If irregular, the time series will have been grouped by the precision of the period date.
@param tslist list of time series for which to create table models.
@return an array of TSViewTable_TableModel object, one for each worksheet
that needs to be created, or a zero-element array if no worksheets need be created for the ts type.
*/
private List<TSViewTable_TableModel> createTableModels(List<TS> tslist) {
	String routine = getClass().getSimpleName() + ".createTableModels";

	// If there is no data in the tslist list, there is no need to create the table models.
	// Return an empty list.
	if ( (tslist == null) || (tslist.size() == 0) ) {
		return new ArrayList<>();
	}

	int numts = tslist.size();

	// The following arrays are used to match up time series with the same interval multipliers.
	// The arrays are sized to the maximum size necessary and won't necessarily be filled completely.
	int[] mults = new int[numts];
	int[] matches = new int[numts];
	String[] tsFormatString = new String[numts];

	int count = 0;
	boolean hit = false;

	// Get the first TS in the list and get the interval base.
	// All other TS in the list must have the same interval base.
	TS ts = tslist.get(0);
	int interval = ts.getDataIntervalBase();

	// Default format is to minute.
	int dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm;
	// Get the proper date format.
	if (interval == TimeInterval.HOUR) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH;
	}
	else if (interval == TimeInterval.DAY) {
		dateFormat = DateTime.FORMAT_YYYY_MM_DD;
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
    else if (interval == TimeInterval.IRREGULAR) {
    	// Get the precision from the first date/time, assuming that all data are treated consistently.
    	// TODO smalers 2022-03-05 need to get from TimeInterval.getIrregularIntervalPrecision(),
    	// maybe store in IrregularTS.
        DateTime d = ts.getDate1();
        if ( d == null ) {
            d = ts.getDate1Original();
        }
        if ( d != null ) {
            int precision = d.getPrecision();
            if ( precision == DateTime.PRECISION_YEAR ) {
                dateFormat = DateTime.FORMAT_YYYY;
            }
            else if ( precision == DateTime.PRECISION_MONTH ) {
                dateFormat = DateTime.FORMAT_YYYY_MM;
            }
            else if ( precision == DateTime.PRECISION_DAY ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD;
            }
            else if ( precision == DateTime.PRECISION_HOUR ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH;
            }
            else if ( precision == DateTime.PRECISION_MINUTE ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm;
            }
            else if ( precision == DateTime.PRECISION_SECOND ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS;
            }
            else if ( precision == DateTime.PRECISION_HSECOND ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_hh;
            }
            else if ( precision == DateTime.PRECISION_MILLISECOND ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_MILLI;
            }
            else if ( precision == DateTime.PRECISION_MICROSECOND ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_MICRO;
            }
            else if ( precision == DateTime.PRECISION_NANOSECOND ) {
                dateFormat = DateTime.FORMAT_YYYY_MM_DD_HH_mm_SS_NANO;
            }
        }
    }

	try {
		int tsPrecision = 2; // Default.
		String propValue = this.props.getValue("OutputPrecision");
		int multi = 0;

		// Go through the time series and see how many of them have different intervals.
		// All of the TS with the same intervals need to be placed in the same worksheet.
		for (int i = 0; i < numts; i++) {
			ts = tslist.get(i);

	    	if ( interval == TimeInterval.IRREGULAR ) {
	        	count = 1; // Only one table model needed because only interval base (from TS dates for irregular) is of concern.
	    	}
	    	else {
	        	// Regular interval time series.
    			// Get the interval multiplier for the current TS.
    			multi = ts.getDataIntervalMult();
    			hit = false;

    			// Look through the array of previously-found interval
    			// multipliers (mults[]) and see if the multiplier has already been encountered.
    			for (int j = 0; j < count; j++) {
    				if (mults[j] == multi) {
    					// If true;
    					matches[i] = j;
    					hit = true;
    					break;
    				}
    			}

    			// If the interval multiplier was not found,
    			// add it to the list of found multipliers and increment the count of
    			// different interval multipliers that have been found.
    			if (!hit) {
    				mults[count] = multi;
    				matches[i] = count;
    				count++;
    			}
	    	}

			// Calculate the output precision of the current TS's data.
			tsPrecision = 2;
			if (propValue != null) {
				// Get the precision from the property value.
				tsPrecision = StringUtil.atoi(propValue);
			}
			else {
				// Get the precision from the time series.
				tsPrecision = ts.getDataPrecision((short)2);
			}
			//tsFormatString[i] = "%" + this.OUTPUT_WIDTH + "." + tsPrecision + "f";
			tsFormatString[i] = "%." + tsPrecision + "f";

		}

		// Create an array of table models big enough to hold one table
		// model for every different interval multiplier that is needed.
		List<TSViewTable_TableModel> models = new ArrayList<>();

		boolean useExtendedLegend = false;
		propValue = this.props.getValue("Table.UseExtendedLegend");
		if ((propValue != null) && (propValue.equalsIgnoreCase("true"))) {
			useExtendedLegend = true;
		}

		TS tempTS = null;

		// Create the table models for the time series:
		// - loop through all of the different interval multipliers
		// - catch exceptions for each table model
   		this.tableModelProblems = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			List<TS> tslistForIntervalMult = new ArrayList<>();
			if ( interval == TimeInterval.IRREGULAR ) {
		    	for (int j = 0; j < numts; j++) {
		        	tslistForIntervalMult.add(tslist.get(j));
		    	}
			}
			else {
    			// Add all the time series with the same interval multiplier to the list.
    			for (int j = 0; j < numts; j++) {
    				if (matches[j] == i) {
    					tslistForIntervalMult.add(tslist.get(j));
    				}
    			}
			}

			// Get all the format precision strings for the TS that were found in the previous loop.
			String[] formats = new String[tslistForIntervalMult.size()];
			int formatCount = 0;
			for (int j = 0; j < numts; j++) {
		    	if ( interval == TimeInterval.IRREGULAR ) {
		        	formats[formatCount++] = tsFormatString[j];
		    	}
		    	else {
    				if (matches[j] == i) {
    					formats[formatCount++] = tsFormatString[j];
    				}
		    	}
			}

			// Now get the starting date of data.
			TSLimits limits = TSUtil.getPeriodFromTS(tslistForIntervalMult, TSUtil.MAX_POR);
			DateTime start = limits.getDate1();

			// ... and the interval multiplier.
        	if ( interval == TimeInterval.IRREGULAR ) {
            	// In this case multi is the precision.
            	int datePrecision = ts.getDate1().getPrecision();
            	try {
            		models.add(new TSViewTable_TableModel(tslistForIntervalMult, start, interval, datePrecision, dateFormat, formats, useExtendedLegend));
            	}
            	catch ( UnequalTimeIntervalException e ) {
            		this.tableModelProblems.add(e.getMessage());
            		//models[i] = null;
            	}
        	}
        	else {
            	// Regular interval.
    			if ( (tslistForIntervalMult == null) || (tslistForIntervalMult.size() == 0) ) {
    				// In this case, use a representative TS.
    				tempTS = tslist.get(i);
    				multi = tempTS.getDataIntervalMult();
    			}
    			else {
    				tempTS = tslistForIntervalMult.get(0);
    				multi = tempTS.getDataIntervalMult();
    			}
    			// ... and create the table model to display all the time
            	// series with the same interval base and interval multiplier.
            	try {
            		models.add( new TSViewTable_TableModel(tslistForIntervalMult, start, interval, multi, dateFormat, formats, useExtendedLegend));
            	}
            	catch ( UnequalTimeIntervalException e ) {
            		this.tableModelProblems.add(e.getMessage());
            		//models[i] = null;
            	}
        	}
		}

		return models;
	}
	catch (Exception e) {
		Message.printWarning(2, routine, "Error generating table models (" + e + ").");
		Message.printWarning(2, routine, e);
		return null;
	}
}

/**
Creates worksheets for all of the table models that were previously-generated.
@param models table models for each of the worksheets that need to be made.
@param p PropList defining JWorksheet characteristics.  See the JWorksheet constructors.
@return a list of JWorksheets, one for each model, or an empty list if no models.
*/
private List<JWorksheet> createWorksheets(List<TSViewTable_TableModel> models, PropList p) {
	if ( (models == null) || models.isEmpty() ) {
		return new ArrayList<>();
	}
	int numWorksheets = models.size();

	List<JWorksheet> worksheets = new ArrayList<>();
	JWorksheet worksheet = null;
	TSViewTable_TableModel model = null;
	for (int i = 0; i < numWorksheets; i++) {
		model = models.get(i);
		if ( model == null ) {
			// There could have been an exception setting up the table model.
			continue;
		}
		TSViewTable_CellRenderer cr = new TSViewTable_CellRenderer(model);
		worksheet = new JWorksheet(cr, model, p);
		worksheet.setPreferredScrollableViewportSize(null);
		worksheet.setHourglassJFrame(this);
		worksheets.add(worksheet);
		model.setWorksheet(worksheet);
		// Add this class as an action listener on the worksheet so that "Calculate Statistics" can be handled here.
		// Otherwise the generic handling won't be able to handle the time series missing value.
		worksheet.addPopupMenuActionListener(this);
	}
	return worksheets;
}

/**
Figures out which worksheet or worksheet header or worksheet's JScrollPane area was clicked on,
and marks that worksheet as the last-selected one.
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
		if (this.dayWorksheets != null) {
			JWorksheet worksheet = searchListeners(this.dayWorksheets, this.dayMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (this.hourWorksheets != null) {
			JWorksheet worksheet = searchListeners(this.hourWorksheets, this.hourMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (this.minuteWorksheets != null) {
			JWorksheet worksheet = searchListeners( this.minuteWorksheets, this.minuteMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (this.monthWorksheets != null) {
			JWorksheet worksheet = searchListeners( this.monthWorksheets, this.monthMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
		if (this.yearWorksheets != null) {
			JWorksheet worksheet = searchListeners(this.yearWorksheets, this.yearMouseListeners, source);
			if (worksheet != null) {
				return worksheet;
			}
		}
        if (this.irregularSecondWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularSecondWorksheets, this.irregularSecondMouseListeners, source);
            if (worksheet != null) {
                return worksheet;
            }
        }
        if (this.irregularMinuteWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularMinuteWorksheets, this.irregularMinuteMouseListeners, source);
            if (worksheet != null) {
                return worksheet;
            }
        }
        if (this.irregularHourWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularHourWorksheets, this.irregularHourMouseListeners, source);
            if (worksheet != null) {
                return worksheet;
            }
        }
        if (this.irregularDayWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularDayWorksheets, this.irregularDayMouseListeners, source);
            if (worksheet != null) {
                return worksheet;
            }
        }
        if (this.irregularMonthWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularMonthWorksheets, this.irregularMonthMouseListeners, source);
            if (worksheet != null) {
                return worksheet;
            }
        }
        if (this.irregularYearWorksheets != null) {
            JWorksheet worksheet = searchListeners(this.irregularYearWorksheets, this.irregularYearMouseListeners, source);
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
	if (this.dayWorksheets != null) {
		for (int i = 0; i < this.dayWorksheets.size(); i++) {
			if (this.dayWorksheets.get(i) == worksheet) {
				return this.dayJPanel;
			}
		}
	}
	if (this.minuteWorksheets != null) {
		for (int i = 0; i < this.minuteWorksheets.size(); i++) {
			if (this.minuteWorksheets.get(i) == worksheet) {
				return this.minuteJPanel;
			}
		}
	}
	if (this.hourWorksheets != null) {
		for (int i = 0; i < this.hourWorksheets.size(); i++) {
			if (this.hourWorksheets.get(i) == worksheet) {
				return this.hourJPanel;
			}
		}
	}
	if (this.monthWorksheets != null) {
		for (int i = 0; i < this.monthWorksheets.size(); i++) {
			if (this.monthWorksheets.get(i) == worksheet) {
				return this.monthJPanel;
			}
		}
	}
	if (this.yearWorksheets != null) {
		for (int i = 0; i < this.yearWorksheets.size(); i++) {
			if (this.yearWorksheets.get(i) == worksheet) {
				return this.yearJPanel;
			}
		}
	}
    if (this.irregularSecondWorksheets != null) {
        for (int i = 0; i < this.irregularSecondWorksheets.size(); i++) {
            if (this.irregularSecondWorksheets.get(i) == worksheet) {
                return this.irregularSecondJPanel;
            }
        }
    }
    if (this.irregularMinuteWorksheets != null) {
        for (int i = 0; i < this.irregularMinuteWorksheets.size(); i++) {
            if (this.irregularMinuteWorksheets.get(i) == worksheet) {
                return this.irregularMinuteJPanel;
            }
        }
    }
    if (this.irregularHourWorksheets != null) {
        for (int i = 0; i < this.irregularHourWorksheets.size(); i++) {
            if (this.irregularHourWorksheets.get(i) == worksheet) {
                return this.irregularHourJPanel;
            }
        }
    }
    if (this.irregularDayWorksheets != null) {
        for (int i = 0; i < this.irregularDayWorksheets.size(); i++) {
            if (this.irregularDayWorksheets.get(i) == worksheet) {
                return this.irregularDayJPanel;
            }
        }
    }
    if (this.irregularMonthWorksheets != null) {
        for (int i = 0; i < this.irregularMonthWorksheets.size(); i++) {
            if (this.irregularMonthWorksheets.get(i) == worksheet) {
                return this.irregularMonthJPanel;
            }
        }
    }
    if (this.irregularYearWorksheets != null) {
        for (int i = 0; i < this.irregularYearWorksheets.size(); i++) {
            if (this.irregularYearWorksheets.get(i) == worksheet) {
                return this.irregularYearJPanel;
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
	if (this.dayWorksheets != null) {
		for (int i = 0; i < this.dayWorksheets.size(); i++) {
			if (this.dayWorksheets.get(i) == worksheet) {
				return this.dayScrollPanes.get(i);
			}
		}
	}
	if (this.minuteWorksheets != null) {
		for (int i = 0; i < this.minuteWorksheets.size(); i++) {
			if (this.minuteWorksheets.get(i) == worksheet) {
				return this.minuteScrollPanes.get(i);
			}
		}
	}
	if (this.hourWorksheets != null) {
		for (int i = 0; i < this.hourWorksheets.size(); i++) {
			if (this.hourWorksheets.get(i) == worksheet) {
				return this.hourScrollPanes.get(i);
			}
		}
	}
	if (this.monthWorksheets != null) {
		for (int i = 0; i < this.monthWorksheets.size(); i++) {
			if (this.monthWorksheets.get(i) == worksheet) {
				return this.monthScrollPanes.get(i);
			}
		}
	}
	if (this.yearWorksheets != null) {
		for (int i = 0; i < this.yearWorksheets.size(); i++) {
			if (this.yearWorksheets.get(i) == worksheet) {
				return this.yearScrollPanes.get(i);
			}
		}
	}
    if (this.irregularSecondWorksheets != null) {
        for (int i = 0; i < this.irregularSecondWorksheets.size(); i++) {
            if (this.irregularSecondWorksheets.get(i) == worksheet) {
                return this.irregularSecondScrollPanes.get(i);
            }
        }
    }
    if (this.irregularMinuteWorksheets != null) {
        for (int i = 0; i < this.irregularMinuteWorksheets.size(); i++) {
            if (this.irregularMinuteWorksheets.get(i) == worksheet) {
                return this.irregularMinuteScrollPanes.get(i);
            }
        }
    }
    if (this.irregularHourWorksheets != null) {
        for (int i = 0; i < this.irregularHourWorksheets.size(); i++) {
            if (this.irregularHourWorksheets.get(i) == worksheet) {
                return this.irregularHourScrollPanes.get(i);
            }
        }
    }
    if (this.irregularDayWorksheets != null) {
        for (int i = 0; i < this.irregularDayWorksheets.size(); i++) {
            if (this.irregularDayWorksheets.get(i) == worksheet) {
                return this.irregularDayScrollPanes.get(i);
            }
        }
    }
    if (this.irregularMonthWorksheets != null) {
        for (int i = 0; i < this.irregularMonthWorksheets.size(); i++) {
            if (this.irregularMonthWorksheets.get(i) == worksheet) {
                return this.irregularMonthScrollPanes.get(i);
            }
        }
    }
    if (this.irregularYearWorksheets != null) {
        for (int i = 0; i < this.irregularYearWorksheets.size(); i++) {
            if (this.irregularYearWorksheets.get(i) == worksheet) {
                return this.irregularYearScrollPanes.get(i);
            }
        }
    }
	return null;
}

/**
Handle events from the checkboxes and choices, indicating which time series intervals should be shown.
@param evt ItemEvent to handle.
*/
public void itemStateChanged(ItemEvent evt) {
	Object source = evt.getSource();
	int state = evt.getStateChange();

    if ( source == this.flagJComboBox ) {
        // Change the data flag visualization for all the table models.
        TSDataFlagVisualizationType vizType = TSDataFlagVisualizationType.valueOfIgnoreCase(this.flagJComboBox.getSelected());
        if ( vizType != null ) {
            for ( int i = 0; i < this.minuteModels.size(); i++ ) {
                this.minuteModels.get(i).setDataFlagVisualizationType(vizType);
                this.minuteModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.hourModels.size(); i++ ) {
                this.hourModels.get(i).setDataFlagVisualizationType(vizType);
                this.hourModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.dayModels.size(); i++ ) {
                this.dayModels.get(i).setDataFlagVisualizationType(vizType);
                this.dayModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.monthModels.size(); i++ ) {
                this.monthModels.get(i).setDataFlagVisualizationType(vizType);
                this.monthModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.yearModels.size(); i++ ) {
                this.yearModels.get(i).setDataFlagVisualizationType(vizType);
                this.yearModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularSecondModels.size(); i++ ) {
                this.irregularSecondModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularSecondModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularMinuteModels.size(); i++ ) {
                this.irregularMinuteModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularMinuteModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularHourModels.size(); i++ ) {
                this.irregularHourModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularHourModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularDayModels.size(); i++ ) {
                this.irregularDayModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularDayModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularMonthModels.size(); i++ ) {
                this.irregularMonthModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularMonthModels.get(i).fireTableDataChanged();
            }
            for ( int i = 0; i < this.irregularYearModels.size(); i++ ) {
                this.irregularYearModels.get(i).setDataFlagVisualizationType(vizType);
                this.irregularYearModels.get(i).fireTableDataChanged();
            }
        }
        // No need to anything else.
        return;
    }

    if ( source == this.digitsJComboBox ) {
        // Change the data flag visualization for all the table models.
        String digits = this.digitsJComboBox.getSelected();
        if ( (digits == null) || digits.isEmpty() ) {
        	digits = "Auto";
        }
        for ( int i = 0; i < this.minuteModels.size(); i++ ) {
            this.minuteModels.get(i).setValueDigits(digits);
            this.minuteModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.hourModels.size(); i++ ) {
            this.hourModels.get(i).setValueDigits(digits);
            this.hourModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.dayModels.size(); i++ ) {
            this.dayModels.get(i).setValueDigits(digits);
            this.dayModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.monthModels.size(); i++ ) {
            this.monthModels.get(i).setValueDigits(digits);
            this.monthModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.yearModels.size(); i++ ) {
            this.yearModels.get(i).setValueDigits(digits);
            this.yearModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularSecondModels.size(); i++ ) {
            this.irregularSecondModels.get(i).setValueDigits(digits);
            this.irregularSecondModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularMinuteModels.size(); i++ ) {
            this.irregularMinuteModels.get(i).setValueDigits(digits);
            this.irregularMinuteModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularHourModels.size(); i++ ) {
            this.irregularHourModels.get(i).setValueDigits(digits);
            this.irregularHourModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularDayModels.size(); i++ ) {
            this.irregularDayModels.get(i).setValueDigits(digits);
            this.irregularDayModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularMonthModels.size(); i++ ) {
            this.irregularMonthModels.get(i).setValueDigits(digits);
            this.irregularMonthModels.get(i).fireTableDataChanged();
        }
        for ( int i = 0; i < this.irregularYearModels.size(); i++ ) {
            this.irregularYearModels.get(i).setValueDigits(digits);
            this.irregularYearModels.get(i).fireTableDataChanged();
        }
        // No need to anything else.
        return;
    }

    // Check other settings.
	boolean visible = false;
	if ( state == ItemEvent.SELECTED ) {
		visible = true;
	}
	else {
		visible = false;
	}

	if (source == this.minuteJCheckBox) {
		this.minuteJPanel.setVisible(visible);
	}
	else if (source == this.hourJCheckBox) {
		this.hourJPanel.setVisible(visible);
	}
	else if (source == this.dayJCheckBox) {
		this.dayJPanel.setVisible(visible);
	}
	else if (source == this.monthJCheckBox) {
		this.monthJPanel.setVisible(visible);
	}
	else if (source == this.yearJCheckBox) {
		this.yearJPanel.setVisible(visible);
	}
    else if (source == this.irregularSecondJCheckBox) {
        this.irregularSecondJPanel.setVisible(visible);
    }
    else if (source == this.irregularMinuteJCheckBox) {
        this.irregularMinuteJPanel.setVisible(visible);
    }
    else if (source == this.irregularHourJCheckBox) {
        this.irregularHourJPanel.setVisible(visible);
    }
    else if (source == this.irregularDayJCheckBox) {
        this.irregularDayJPanel.setVisible(visible);
    }
    else if (source == this.irregularMonthJCheckBox) {
        this.irregularMonthJPanel.setVisible(visible);
    }
    else if (source == this.irregularYearJCheckBox) {
        this.irregularYearJPanel.setVisible(visible);
    }

	JPanel panel = findWorksheetsJPanel(this.lastSelectedWorksheet);
	if (panel != null && !panel.isVisible()) {
		if (this.lastSelectedScrollPane != null) {
			// Reset the border to its original state.
			this.lastSelectedScrollPane.setBorder(this.originalScrollPaneBorder);
		}

		if (!this.lastSelectedWorksheet.stopEditing()) {
			this.lastSelectedWorksheet.cancelEditing();
		}

		this.lastSelectedWorksheet = null;
		this.lastSelectedScrollPane = null;
		this.originalScrollPaneBorder = null;
		this.messageJTextField.setText("Currently-selected worksheet: (none)");
		if (!this.saveAlwaysEnabled) {
			this.saveJButton.setEnabled(false);
		}
	}

	// Calculate the worksheet/panel tracking information.
	calculateVisibleWorksheetsByPanel();

	if ( this.meta.numPanelsWithVisibleWorksheets == 0 ) {
		this.mainJPanelNorth = true;
		getContentPane().remove(this.mainJPanel);
		getContentPane().add(this.mainJPanel, "North");
	}
	else {
		if (this.mainJPanelNorth) {
			getContentPane().remove(this.mainJPanel);
			getContentPane().add(this.mainJPanel, "Center");
			this.mainJPanelNorth = false;
		}
	}

	if ( this.meta.numWorksheetsTotalVisible == 1 ) {
		if (this.lastSelectedScrollPane != null) {
			// Reset the border to its original state.
			this.lastSelectedScrollPane.setBorder((new JScrollPane()).getBorder());
		}

		setPanelsBorder(false);
	}

	if ( (this.meta.numPanelsWithVisibleWorksheets > 1) && (panel != null) && panel.isVisible() ) {
		this.lastSelectedScrollPane.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
		setPanelsBorder(true);
	}
	else if ( this.meta.numPanelsWithVisibleWorksheets > 1 ) {
		setPanelsBorder(true);
	}
}

/**
Responds to mouse clicked events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseClicked(MouseEvent e) {
}

/**
Responds to mouse entered events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseEntered(MouseEvent e) {
}

/**
Responds to mouse exited events; does nothing.
@param e the MouseEvent that happened.
*/
public void mouseExited(MouseEvent e) {
}

/**
Responds to mouse pressed events, currently only selects the worksheet that was clicked on,
which allows other behavior to be focused on the proper worksheet.
@param e the MouseEvent that happened.
*/
public void mousePressed(MouseEvent e) {
	this.saveJButton.setEnabled(true);

	if (this.lastSelectedScrollPane != null) {
		// Reset the border to its original state.
		this.lastSelectedScrollPane.setBorder(this.originalScrollPaneBorder);
	}

	JWorksheet last = this.lastSelectedWorksheet;
	// Find the worksheet that was clicked on.

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
Called when the user presses the "Save" button.
This gets the time series stored in the most-recently selected JWorksheet and writes them to the file selected by the user.
How to write the file is determined by the extension of the file selected by the user.
*/
private void saveClicked() {
	String routine = "saveClicked";
	if (this.lastSelectedWorksheet == null) {
		this.messageJTextField.setText("No worksheets currently selected.  Select one and press 'Save' again.");
		return;
	}

	JGUIUtil.setWaitCursor(this, true);
	String last_directory = JGUIUtil.getLastFileDialogDirectory();
	JFileChooser fc =JFileChooserFactory.createJFileChooser(last_directory);
	fc.setAcceptAllFileFilterUsed(false);
	fc.setDialogTitle("Save Time Series File");
	SimpleFileFilter dff = new SimpleFileFilter("dv", "DateValue Time Series File");
	fc.addChoosableFileFilter(dff);
	SimpleFileFilter tff = new SimpleFileFilter("txt", "Tab-Delimited Text File");
	fc.addChoosableFileFilter(tff);
	SimpleFileFilter cff = new SimpleFileFilter("txt", "Comma-Delimited Text File");
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
	String filename = directory + File.separator + fc.getName(fc.getSelectedFile());

	List<TS> tslist = ((TSViewTable_TableModel)this.lastSelectedWorksheet.getModel()).getTSList();

	if (fc.getFileFilter() == dff) {
		try {
			DateValueTS.writeTimeSeriesList(tslist, filename);
		}
		catch (Exception e) {
			Message.printWarning(1, routine, "Error saving DateValue file \"" + filename + "\"");
			Message.printWarning(1, routine, e);
		}
	}
	else if (fc.getFileFilter() == tff) {
		writeTextFile(this.lastSelectedWorksheet, '\t', filename);
	}
	else if (fc.getFileFilter() == cff) {
		writeTextFile(this.lastSelectedWorksheet, ',', filename);
	}
	JGUIUtil.setWaitCursor(this, false);
}

/**
Searches through the listener array associated with worksheets to locate
the JWorksheet for which the JScrollPane area was clicked on.
@param worksheets the worksheets associated with a certain data interval
@param mouseListeners a list of lists containing JScrollPane and JScrollPane
scrollbars (as MouseListeners) used to scroll around the worksheets in the above array
@param source the object on which a MouseEvent was triggered.
@return the JWorksheet that was clicked on, or null if it could not be found
*/
private JWorksheet searchListeners(List<JWorksheet> worksheets, List<List<MouseListener>> mouseListeners, Object source) {
	if ( mouseListeners == null || (source == null) ) {
		return null;
	}

	int size = mouseListeners.size();

	for ( int i = 0; i < size; i++ ) {
		List<MouseListener> v = mouseListeners.get(i);

		for ( int j = 0; j < v.size(); j++ ) {
			if (v.get(j) == source) {
				return worksheets.get(i);
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
private void selectWorksheet(JWorksheet selectWorksheet, JWorksheet lastWorksheet) {
	calculateVisibleWorksheetsByPanel();
	this.lastSelectedWorksheet = selectWorksheet;

	if (lastWorksheet != null && lastWorksheet != selectWorksheet) {
		if (!lastWorksheet.stopEditing()) {
			lastWorksheet.cancelEditing();
		}
	}
	// Scroll pane for the worksheet.
	this.lastSelectedScrollPane = findWorksheetsScrollPane(selectWorksheet);

	// Back up the scroll pane's current border.
	this.originalScrollPaneBorder = this.lastSelectedScrollPane.getBorder();
	// ... and change the scroll pane's border to represent that its worksheet is selected.
	if ( this.meta.numWorksheetsTotalVisible > 1 ) {
		this.lastSelectedScrollPane.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
	}

	TSViewTable_TableModel model = (TSViewTable_TableModel)selectWorksheet.getModel();
	String base = null;
	switch ( model.getIntervalBase() ) {
		case TimeInterval.MINUTE:
		    base = "Minute";
		    break;
		case TimeInterval.HOUR:
		    base = "Hour";
		    break;
		case TimeInterval.DAY:
		    base = "Day";
		    break;
		case TimeInterval.MONTH:
		    base = "Month";
		    break;
		case TimeInterval.YEAR:
		    base = "Year";
		    break;
        case TimeInterval.IRREGULAR:
            base = "Irregular";
            break;
		default:
		    base = "???";
		    break;
	}

    String s = "";
	if ( model.getIntervalBase() == TimeInterval.IRREGULAR ) {
	    // Returns all upper case so change to be consistent with displays.
	    String prec = TimeInterval.getName(model.getIrregularDateTimePrecision(),0);
	    s = base + " (" + prec + " precision)";
	}
	else {
    	if (model.getIntervalMult() == 1) {
    		s = base;
    	}
    	else {
    		s = "" + model.getIntervalMult() + base;
    	}
	}

	this.messageJTextField.setText("Currently-selected worksheet interval: " + s + ", " + model.getRowCount() + " data points.");
}

/**
Sets the column widths for the specified group of worksheets.
@param worksheets the worksheets for which to set the column widths.
*/
private void setColumnWidths(List<JWorksheet> worksheets, int precision) {
	if ( (worksheets == null) || worksheets.isEmpty() ) {
		return;
	}

	int[] skipCols = { 0 };
	int[] widths = null;

	int dateWidth = 16;

	// Get the proper date format:
	// - increasing width with precisions
	if (precision == DateTime.PRECISION_YEAR) {
		dateWidth = 4;
	}
	else if (precision == DateTime.PRECISION_MONTH) {
		dateWidth = 7;
	}
	else if (precision == DateTime.PRECISION_DAY) {
		dateWidth = 10;
	}
	else if (precision == DateTime.PRECISION_HOUR) {
		dateWidth = 13;
	}
	else if (precision == DateTime.PRECISION_MINUTE) {
		dateWidth = 16;
	}
	else if (precision == DateTime.PRECISION_SECOND) {
		dateWidth = 20;
	}
	else {
		// Just in case.
		dateWidth = 25;
	}

	for ( int i = 0; i < worksheets.size(); i++ ) {
		TSViewTable_TableModel model = (TSViewTable_TableModel)worksheets.get(i).getModel();
		widths = model.getColumnWidths();
		widths[0] = dateWidth;
		worksheets.get(i).setColumnWidths(widths);
		worksheets.get(i).calculateColumnWidths(100, 1000, skipCols, getGraphics());
	}
}

/**
Turns on or off the borders for all the panels at once.
@param on if true, then the borders for all the panels will be displayed.  If false, they will not.
*/
public void setPanelsBorder(boolean on) {
	if (on) {
		this.minuteJPanel.setBorder(BorderFactory.createTitledBorder("Minute Interval"));
		this.hourJPanel.setBorder(BorderFactory.createTitledBorder("Hour Interval"));
		this.dayJPanel.setBorder(BorderFactory.createTitledBorder("Day Interval"));
		this.monthJPanel.setBorder(BorderFactory.createTitledBorder("Month Interval"));
		this.yearJPanel.setBorder(BorderFactory.createTitledBorder("Year Interval"));
		this.irregularSecondJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (<= Second)"));
		this.irregularMinuteJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (Minute)"));
		this.irregularMinuteJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (Hour)"));
		this.irregularMinuteJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (Day)"));
		this.irregularMinuteJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (Month)"));
		this.irregularMinuteJPanel.setBorder(BorderFactory.createTitledBorder("Irregular Interval (Year)"));
	}
	else {
		this.minuteJPanel.setBorder(null);
		this.hourJPanel.setBorder(null);
		this.dayJPanel.setBorder(null);
		this.monthJPanel.setBorder(null);
		this.yearJPanel.setBorder(null);
	    this.irregularSecondJPanel.setBorder(null);
	    this.irregularMinuteJPanel.setBorder(null);
	    this.irregularHourJPanel.setBorder(null);
	    this.irregularDayJPanel.setBorder(null);
	    this.irregularMonthJPanel.setBorder(null);
	    this.irregularYearJPanel.setBorder(null);
	}
}

/**
Sets up the GUI.
@param mode Indicates whether the GUI should be visible at creation.
*/
private void setupGUI(boolean mode) {
	String routine = getClass().getSimpleName() + ".setupGUI";

	// Start a big try block to set up the GUI.
	try {

	// Add a listener to catch window manager events.
	addWindowListener(this);

	// Lay out the main window component by component.
	// Start with the menubar default components.
	// Then add each requested component to the menu bar and the interface.
	GridBagLayout gbl = new GridBagLayout();

	// Add a panel to hold the tables.
	this.mainJPanel = new JPanel();
	this.mainJPanel.setLayout(gbl);
	getContentPane().add(this.mainJPanel);

	// Create all the JCheckboxes.
	this.minuteJCheckBox = new JCheckBox("Minute Time Series", true);
	this.minuteJCheckBox.addItemListener(this);
	this.hourJCheckBox = new JCheckBox("Hour Time Series", true);
	this.hourJCheckBox.addItemListener(this);
	this.dayJCheckBox = new JCheckBox("Day Time Series", true);
	this.dayJCheckBox.addItemListener(this);
	this.monthJCheckBox = new JCheckBox("Month Time Series", true);
	this.monthJCheckBox.addItemListener(this);
	this.yearJCheckBox = new JCheckBox("Year Time Series", true);
	this.yearJCheckBox.addItemListener(this);
    this.irregularSecondJCheckBox = new JCheckBox("Irregular Interval Time Series (<= Second)", true);
    this.irregularSecondJCheckBox.addItemListener(this);
    this.irregularMinuteJCheckBox = new JCheckBox("Irregular Interval Time Series (Minute)", true);
    this.irregularMinuteJCheckBox.addItemListener(this);
    this.irregularHourJCheckBox = new JCheckBox("Irregular Interval Time Series (Hour)", true);
    this.irregularHourJCheckBox.addItemListener(this);
    this.irregularDayJCheckBox = new JCheckBox("Irregular Interval Time Series (Day)", true);
    this.irregularDayJCheckBox.addItemListener(this);
    this.irregularMonthJCheckBox = new JCheckBox("Irregular Interval Time Series (Month)", true);
    this.irregularMonthJCheckBox.addItemListener(this);
    this.irregularYearJCheckBox = new JCheckBox("Irregular Interval Time Series (Year)", true);
    this.irregularYearJCheckBox.addItemListener(this);

	// Create the PropList for the JWorksheets.
	PropList p = new PropList("TSViewTableJFrame.JWorksheet");
	p.add("JWorksheet.OneClickColumnSelection=true");
	p.add("JWorksheet.RowColumnPresent=true");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=ExcelSelection");
	p.add("JWorksheet.AllowCopy=true");
	p.add("JWorksheet.AllowPaste=true");
	p.add("JWorksheet.AllowCalculateStatistics=true");
	// Handling of "Calculate Statistics" action event will be delegated in JWorksheet to ActionPerformed here.
	p.add("JWorksheet.DelegateCalculateStatistics=true");

	PropList p2 = new PropList("TSViewTableJFrame.JWorksheet");
	p2.add("JWorksheet.RowColumnPresent=true");
	p2.add("JWorksheet.Selectable=false");
	p2.add("JWorksheet.ShowPopupMenu=true");
	p2.add("JWorksheet.SelectionMode=ExcelSelection");

	// Separate the __tslist list into lists of like time series (same base interval and irregular.
	createSeparateTimeSeriesLists();

	// Create all the table models and worksheets.
	this.dayModels = createTableModels(this.dayTSList);
	this.dayWorksheets = createWorksheets(this.dayModels, p);
	List<JWorksheet> dayHeaders = createWorksheets(this.dayModels, p2);

	this.minuteModels = createTableModels(this.minuteTSList);
	this.minuteWorksheets = createWorksheets(this.minuteModels, p);
	List<JWorksheet> minuteHeaders = createWorksheets(this.minuteModels, p2);

	this.hourModels = createTableModels(this.hourTSList);
	this.hourWorksheets = createWorksheets(this.hourModels, p);
	List<JWorksheet> hourHeaders = createWorksheets(this.hourModels, p2);

	this.monthModels = createTableModels(this.monthTSList);
	this.monthWorksheets = createWorksheets(this.monthModels, p);
	List<JWorksheet> monthHeaders = createWorksheets(this.monthModels, p2);

	this.yearModels = createTableModels(this.yearTSList);
	this.yearWorksheets = createWorksheets(this.yearModels, p);
	List<JWorksheet> yearHeaders = createWorksheets(this.yearModels, p2);

    this.irregularSecondModels = createTableModels(this.irregularSecondTSList);
    this.irregularSecondWorksheets = createWorksheets(this.irregularSecondModels, p);
    List<JWorksheet> irregularSecondHeaders = createWorksheets(this.irregularSecondModels, p2);

    this.irregularMinuteModels = createTableModels(this.irregularMinuteTSList);
    this.irregularMinuteWorksheets = createWorksheets(this.irregularMinuteModels, p);
    List<JWorksheet> irregularMinuteHeaders = createWorksheets(this.irregularMinuteModels, p2);

    this.irregularHourModels = createTableModels(this.irregularHourTSList);
    this.irregularHourWorksheets = createWorksheets(this.irregularHourModels, p);
    List<JWorksheet> irregularHourHeaders = createWorksheets(this.irregularHourModels, p2);

    this.irregularDayModels = createTableModels(this.irregularDayTSList);
    this.irregularDayWorksheets = createWorksheets(this.irregularDayModels, p);
    List<JWorksheet> irregularDayHeaders = createWorksheets(this.irregularDayModels, p2);

    this.irregularMonthModels = createTableModels(this.irregularMonthTSList);
    this.irregularMonthWorksheets = createWorksheets(this.irregularMonthModels, p);
    List<JWorksheet> irregularMonthHeaders = createWorksheets(this.irregularMonthModels, p2);

    this.irregularYearModels = createTableModels(this.irregularYearTSList);
    this.irregularYearWorksheets = createWorksheets(this.irregularYearModels, p);
    List<JWorksheet> irregularYearHeaders = createWorksheets(this.irregularYearModels, p2);

	// Create the panels for the interval bases.
	this.minuteJPanel = new JPanel();
	this.hourJPanel = new JPanel();
	this.dayJPanel = new JPanel();
	this.monthJPanel = new JPanel();
	this.yearJPanel = new JPanel();
	this.irregularSecondJPanel = new JPanel();
	this.irregularMinuteJPanel = new JPanel();
    this.irregularHourJPanel = new JPanel();
    this.irregularDayJPanel = new JPanel();
    this.irregularMonthJPanel = new JPanel();
    this.irregularYearJPanel = new JPanel();

	// Create the arrays of scroll panes.
	if (this.minuteWorksheets.size() > 0) {
		this.minuteScrollPanes = new ArrayList<>();
	}
	if (this.hourWorksheets.size() > 0) {
		this.hourScrollPanes = new ArrayList<>();
	}
	if (this.dayWorksheets.size() > 0) {
		this.dayScrollPanes = new ArrayList<>();
	}
	if (this.monthWorksheets.size() > 0) {
		this.monthScrollPanes = new ArrayList<>();
	}
	if (this.yearWorksheets.size() > 0) {
		this.yearScrollPanes = new ArrayList<>();
	}
    if (this.irregularSecondWorksheets.size() > 0) {
        this.irregularSecondScrollPanes = new ArrayList<>();
    }
    if (this.irregularMinuteWorksheets.size() > 0) {
        this.irregularMinuteScrollPanes = new ArrayList<>();
    }
    if (this.irregularHourWorksheets.size() > 0) {
        this.irregularHourScrollPanes = new ArrayList<>();
    }
    if (this.irregularDayWorksheets.size() > 0) {
        this.irregularDayScrollPanes = new ArrayList<>();
    }
    if (this.irregularMonthWorksheets.size() > 0) {
        this.irregularMonthScrollPanes = new ArrayList<>();
    }
    if (this.irregularYearWorksheets.size() > 0) {
        this.irregularYearScrollPanes = new ArrayList<>();
    }

	// Add the worksheets to the panels and add the panels to the main panel.

	if (this.reverseNormalOrder) {
		this.minuteMouseListeners = buildMouseListeners(this.minuteWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Minute", this.minuteJPanel, this.minuteJCheckBox, this.minuteWorksheets,
			minuteHeaders, this.minuteScrollPanes, this.minuteMouseListeners);

		this.hourMouseListeners = buildMouseListeners(this.hourWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Hour", this.hourJPanel, this.hourJCheckBox, this.hourWorksheets,
			hourHeaders, this.hourScrollPanes, this.hourMouseListeners);

		this.dayMouseListeners = buildMouseListeners(this.dayWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Day", this.dayJPanel, this.dayJCheckBox, this.dayWorksheets,
			dayHeaders, this.dayScrollPanes, this.dayMouseListeners);

		this.monthMouseListeners = buildMouseListeners(this.monthWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Month", this.monthJPanel, this.monthJCheckBox, this.monthWorksheets,
			monthHeaders, this.monthScrollPanes, this.monthMouseListeners);

		this.yearMouseListeners = buildMouseListeners(this.yearWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Year", this.yearJPanel, this.yearJCheckBox, this.yearWorksheets,
			yearHeaders, this.yearScrollPanes, this.yearMouseListeners);

		/*
		if (this.irregularMinute != null && this.irregularMinute.size() > 0) {
		    this.irregularMinuteJPanel.setLayout(new GridBagLayout());
		    JGUIUtil.addComponent(this.irregularMinuteJPanel,
    			new JLabel("Table view for irregular data is not currently enabled.  Use the summary view."),
    			0, 0, 1, 1, 1, 1,
    			GridBagConstraints.NONE, GridBagConstraints.WEST);
	      	JGUIUtil.addComponent(this.mainJPanel, this.irregularMinuteJPanel,
    			0, this.panelCount++, 1, 1, 1, 1,
    			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
		}
		*/
	}
	else {
		this.yearMouseListeners = buildMouseListeners(this.yearWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Year", this.yearJPanel, this.yearJCheckBox, this.yearWorksheets,
			yearHeaders, this.yearScrollPanes, this.yearMouseListeners);

		this.monthMouseListeners = buildMouseListeners(this.monthWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Month", this.monthJPanel, this.monthJCheckBox, this.monthWorksheets,
			monthHeaders, this.monthScrollPanes, this.monthMouseListeners);

		this.dayMouseListeners = buildMouseListeners(this.dayWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Day", this.dayJPanel, this.dayJCheckBox, this.dayWorksheets,
			dayHeaders, this.dayScrollPanes, this.dayMouseListeners);

		this.hourMouseListeners = buildMouseListeners(this.hourWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Hour", this.hourJPanel, this.hourJCheckBox, this.hourWorksheets,
			hourHeaders, this.hourScrollPanes, this.hourMouseListeners);

		this.minuteMouseListeners = buildMouseListeners(this.minuteWorksheets);
		addWorksheetsToPanel(this.mainJPanel, "Minute", this.minuteJPanel, this.minuteJCheckBox, this.minuteWorksheets,
			minuteHeaders, this.minuteScrollPanes, this.minuteMouseListeners);

		/*
		if (__irregular != null && __irregular.size() > 0) {
		    __irregularJPanel.setLayout(new GridBagLayout());
		    JGUIUtil.addComponent(__irregularJPanel,
    			new JLabel("Table view for irregular data is not currently enabled.  Use the summary view."),
    			0, 0, 1, 1, 1, 1,
    			GridBagConstraints.NONE, GridBagConstraints.WEST);
	      	JGUIUtil.addComponent(__mainJPanel, __irregularJPanel,
    			0, __panelCount++, 1, 1, 1, 1,
    			GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST);
		}
		*/
	}

	// Irregular is always last.
    this.irregularSecondMouseListeners = buildMouseListeners(this.irregularSecondWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (<= Second)", this.irregularSecondJPanel, this.irregularSecondJCheckBox,
        this.irregularSecondWorksheets, irregularSecondHeaders, this.irregularSecondScrollPanes,
        this.irregularSecondMouseListeners);

    this.irregularMinuteMouseListeners = buildMouseListeners(this.irregularMinuteWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (Minute)", this.irregularMinuteJPanel, this.irregularMinuteJCheckBox,
        this.irregularMinuteWorksheets, irregularMinuteHeaders, this.irregularMinuteScrollPanes,
        this.irregularMinuteMouseListeners);

    this.irregularHourMouseListeners = buildMouseListeners(this.irregularHourWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (Hour)", this.irregularHourJPanel, this.irregularHourJCheckBox,
        this.irregularHourWorksheets, irregularHourHeaders, this.irregularHourScrollPanes,
        this.irregularHourMouseListeners);

    this.irregularDayMouseListeners = buildMouseListeners(this.irregularDayWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (Day)", this.irregularDayJPanel, this.irregularDayJCheckBox,
        this.irregularDayWorksheets, irregularDayHeaders, this.irregularDayScrollPanes,
        this.irregularDayMouseListeners);

    this.irregularMonthMouseListeners = buildMouseListeners(this.irregularMonthWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (Month)", this.irregularMonthJPanel, this.irregularMonthJCheckBox,
        this.irregularMonthWorksheets, irregularMonthHeaders, this.irregularMonthScrollPanes,
        this.irregularMonthMouseListeners);

    this.irregularYearMouseListeners = buildMouseListeners(this.irregularYearWorksheets);
    addWorksheetsToPanel(this.mainJPanel, "Irregular (Year)", this.irregularYearJPanel, this.irregularYearJCheckBox,
        this.irregularYearWorksheets, irregularYearHeaders, this.irregularYearScrollPanes,
        this.irregularYearMouseListeners);

	JPanel bottomJPanel = new JPanel();
	bottomJPanel.setLayout (gbl);
	this.messageJTextField = new JTextField();
	this.messageJTextField.setEditable(false);
	JGUIUtil.addComponent(bottomJPanel, this.messageJTextField,
		0, 1, 7, 1, 1.0, 0.0,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Put the buttons on the bottom of the window.
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	// Add a choice to control whether flags are shown with data values.
	this.flagJComboBox = new SimpleJComboBox(false);
	List<String> flagChoices = new ArrayList<>();
	flagChoices.add("" + TSDataFlagVisualizationType.NOT_SHOWN);
	flagChoices.add("" + TSDataFlagVisualizationType.SUPERSCRIPT);
	//flagChoices.add("" + TSDataFlagVisualizationType.SEPARATE_COLUMN);
	this.flagJComboBox.setData(flagChoices);
	this.flagJComboBox.addItemListener(this);
	this.flagJComboBox.setToolTipText("Indicate how data flags should be visualized.");
	JLabel flagJLabel = new JLabel("Flags:");
	button_JPanel.add(flagJLabel);
	button_JPanel.add(this.flagJComboBox);
	// Only enable flags display if at least one displayed time series has flags.
	if ( timeSeriesHaveFlags() ) {
	    flagJLabel.setEnabled(true);
	    this.flagJComboBox.setEnabled(true);
	    this.flagJComboBox.select(""+TSDataFlagVisualizationType.NOT_SHOWN);
	}
	else {
	    flagJLabel.setEnabled(false);
	    this.flagJComboBox.setEnabled(false);
	}

	// Add a choice to control the number of digits shown for values in the table.
	this.digitsJComboBox = new SimpleJComboBox(false);
	List<String> digitsChoices = new ArrayList<>();
	digitsChoices.add("Auto");
	digitsChoices.add("0");
	digitsChoices.add("1");
	digitsChoices.add("2");
	digitsChoices.add("3");
	digitsChoices.add("4");
	digitsChoices.add("5");
	digitsChoices.add("6");
	this.digitsJComboBox.setData(digitsChoices);
	this.digitsJComboBox.addItemListener(this);
	this.digitsJComboBox.setToolTipText("The number of digits after the decimal point or Auto to set based on units.");
	JLabel digitsJLabel = new JLabel("Digits:");
	button_JPanel.add(digitsJLabel);
	button_JPanel.add(this.digitsJComboBox);
	this.digitsJComboBox.select("Auto");

	// Graph button.
	this.graphJButton = new SimpleJButton(this.BUTTON_GRAPH, this);
	this.graphJButton.setToolTipText("Display the time series graph view.");
	String propValue=this.props.getValue("EnableGraph");
	if ((propValue != null) && propValue.equalsIgnoreCase("false")) {
		this.graphJButton.setEnabled(false);
	}
	button_JPanel.add(this.graphJButton);

	// Summary button.
	this.summaryJButton = new SimpleJButton(this.BUTTON_SUMMARY, this);
	this.summaryJButton.setToolTipText("Display the time series summary view.");
	propValue = this.props.getValue("EnableSummary");
	if ( (propValue != null) && propValue.equalsIgnoreCase("false") ) {
		this.summaryJButton.setEnabled(false);
	}
	button_JPanel.add(this.summaryJButton);

	this.saveJButton = new SimpleJButton(this.BUTTON_SAVE, this);
	this.saveJButton.setEnabled(false);
	this.saveJButton.setToolTipText("Save the time series to a file.");
	button_JPanel.add(this.saveJButton);

	// The problems button will be set visible if any problems exist after adding the tables worksheets.
	this.problemsJButton = new SimpleJButton(this.BUTTON_PROBLEMS,this);
	this.problemsJButton.setBackground(Color.RED);
	this.problemsJButton.setToolTipText("Display the problems encountered displaying the table.");
	if ( this.tableModelProblems.size() == 0 ) {
		this.problemsJButton.setVisible(false);
	}
	button_JPanel.add(this.problemsJButton);

	this.closeJButton = new SimpleJButton(this.BUTTON_CLOSE,this);
	this.closeJButton.setToolTipText("Close the table view.");
	button_JPanel.add(this.closeJButton);

	// TODO - add later
	//button_JPanel.add(__helpJButton);

	JGUIUtil.addComponent(bottomJPanel, button_JPanel,
		0, 0, 8, 1, 1, 1,
		GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	getContentPane().add("South", bottomJPanel);

	this.messageJTextField.setText("Currently-selected worksheet: (none)");

	pack();
	// TODO SAM 2012-04-16 Need to default size based on number of time series.
	setSize(600,500);
	// Get the UI component to determine screen to display on - needed for multiple monitors.
	Object uiComponentO = this.props.getContents( "TSViewParentUIComponent" );
	Component parentUIComponent = null;
	if ( (uiComponentO != null) && (uiComponentO instanceof Component) ) {
		parentUIComponent = (Component)uiComponentO;
	}
	// Center on the UI component rather than the graph, because the graph screen seems to get tied screen 0?
	JGUIUtil.center(this,parentUIComponent);
	setVisible(mode);

	} // End of try.
	catch (Exception e) {
		Message.printWarning(2, routine, e);
	}

	// These calls are here because they require a valid graphics
	// context to work properly (i.e., setVisible(true) must have already been called).
	setColumnWidths(this.minuteWorksheets, DateTime.PRECISION_MINUTE);
	setColumnWidths(this.hourWorksheets, DateTime.PRECISION_HOUR);
	setColumnWidths(this.dayWorksheets, DateTime.PRECISION_DAY);
	setColumnWidths(this.monthWorksheets, DateTime.PRECISION_MONTH);
	setColumnWidths(this.yearWorksheets, DateTime.PRECISION_YEAR);
	setColumnWidths(this.irregularSecondWorksheets, DateTime.PRECISION_SECOND);
	setColumnWidths(this.irregularMinuteWorksheets, DateTime.PRECISION_MINUTE);
	setColumnWidths(this.irregularHourWorksheets, DateTime.PRECISION_HOUR);
	setColumnWidths(this.irregularDayWorksheets, DateTime.PRECISION_DAY);
	setColumnWidths(this.irregularMonthWorksheets, DateTime.PRECISION_MONTH);
	setColumnWidths(this.irregularYearWorksheets, DateTime.PRECISION_YEAR);

	checkForSingleWorksheet();
}

/**
 * Show the problems dialog.
 * This is only enabled if the 'tableModelProblems" list is not empty.
 */
private void showProblems () {
	StringBuilder textBuilder = new StringBuilder();
	textBuilder.append("The table data resulted in the following problems:\n\n");
	for ( String problem : this.tableModelProblems ) {
		textBuilder.append(problem + "\n");
	}
	new ResponseJDialog ( this, IOUtil.getProgramName() + " - Table Problems", textBuilder.toString(), ResponseJDialog.OK ).response();
}

/**
Determine if any of the time series has flags.
@return true if any of the time series has flags
*/
private boolean timeSeriesHaveFlags () {
    for ( TS ts : this.tslist ) {
    	if ( ts == null ) {
    		continue;
    	}
        if ( ts.hasDataFlags() ) {
            return true;
        }
    }
    return false;
}

/**
Responds to window activated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowActivated(WindowEvent evt) {
}

/**
Responds to window closed events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowClosed(WindowEvent evt) {
}

/**
Responds to window closing events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowClosing(WindowEvent evt) {
	this.tsviewJFrame.closeGUI(TSViewType.TABLE);
}

/**
Responds to window deactivated events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeactivated(WindowEvent evt) {
}

/**
Responds to window deiconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowDeiconified(WindowEvent evt) {
}

/**
Responds to window iconified events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowIconified(WindowEvent evt) {
}

/**
Responds to window opened events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowOpened(WindowEvent evt) {
}

/**
Responds to window opening events; does nothing.
@param evt the WindowEvent that happened.
*/
public void windowOpening(WindowEvent evt) {
}

/**
Write all of a worksheet's data to a text file, separating field values
by the given delimiter and separating rows with newlines.
@param worksheet the worksheet to dump out the data of.
@param delimited the character to separate fields with.
@param filename the text file to write to.
*/
private void writeTextFile(JWorksheet worksheet, char delimiter, String filename) {
	String routine = "writeTextFile";
	try {
		StopWatch sw = new StopWatch();
		sw.start();
		PrintWriter out = new PrintWriter(new FileOutputStream(filename));

		this.messageJTextField.setText("Saving file \"" + filename + "\"");
		JGUIUtil.forceRepaint(this.messageJTextField);
		int rows = worksheet.getRowCount();
		int columns = worksheet.getColumnCount();

		worksheet.startNewConsecutiveRead();

		StringBuffer buff = null;
		int pct = 0;
		int lastPct = 0;
		for (int i = 0; i < rows; i++) {
			// Calculate the percentage complete,
			// and if different from the last percentage complete, update the status bar text field.
			pct = ((int)(((double)i / (double)rows) * 100));
			if (pct != lastPct) {
				lastPct = pct;
				this.messageJTextField.setText("Saving file \"" + filename + "\" (" + pct +  "% done)");
				JGUIUtil.forceRepaint(this.messageJTextField);
			}

			buff = new StringBuffer();
			for (int j = 0; j < columns; j++) {
				buff.append(worksheet.getConsecutiveValueAt(i, j));
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

		// Unlikely to happen, but it's just good GUI design to not have something like "1 seconds" or "2 second(s)".
		if (seconds == 1.000) {
			plural = "";
		}
		this.messageJTextField.setText("File saved (took " + sw.getSeconds() + " second" + plural + ")");
	}
	catch (Exception e) {
		this.messageJTextField.setText("Error saving file \"" + filename + "\"");
		Message.printWarning(2, routine, "Error saving file \"" + filename + "\"");
		Message.printWarning(2, routine, e);
	}
}

/**
 * Listens for property change events (from TSGraphEditor) &
 * Notifies the model listeners (JTables/JWorksheet) that the table model has changed.
 */
public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("TS_DATA_VALUE_CHANGE")) {
        // Expecting modified TS object.
        TSViewTable_TableModel model = findModel(((TS)e.getNewValue()));
        model.fireTableDataChanged();
    }
}

/**
 * Returns the first model encountered that contains the specified TS.
 *
 * @param ts
 * @return model containing specified TS, or null
 */
private TSViewTable_TableModel findModel(TS ts) {
    // TODO: Order search so that most likely models are searched first.

    TSViewTable_TableModel target = null;

    if ((target = findModel(this.dayModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.hourModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.minuteModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.monthModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.yearModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularSecondModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularMinuteModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularHourModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularDayModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularMonthModels,ts)) != null) {
        return target;
    }
    else if ((target = findModel(this.irregularYearModels,ts)) != null) {
        return target;
    }
    return target;
}

/**
 * Returns the first model encountered that contains the specified TS.
 * @param models list of models to search
 * @param ts time series to match
 * @return model containing specified TS, or null
 */
private TSViewTable_TableModel findModel ( List<TSViewTable_TableModel> models, TS ts ) {
    if ( models != null) {
        int nModels = models.size();
        for ( int iModel = 0; iModel < nModels; iModel++ ) {
            TSViewTable_TableModel m = models.get(iModel);
            List<TS> tslist = m.getTSList();
            int nVec = tslist.size();
            for (int iVec = 0; iVec < nVec; iVec++) {
                if (tslist.get(iVec) == ts) {
                    return m;
                }
            }
        }
    }
    return null;
}

}