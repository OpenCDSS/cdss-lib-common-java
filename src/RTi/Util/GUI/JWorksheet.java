// ----------------------------------------------------------------------------
// JWorksheet - Class that transforms a JTable into a real worksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2002-12-XX	J. Thomas Sapienza, RTi	Initial version.
// 2003-03-04	JTS, RTi		Javadoc'd, revised.  
// 2003-03-07	JTS, RTi		Added sorting routines.
// 2003-03-10	JTS, RTi		Added font size and table width code.
// 2003-03-11	JTS, RTi		Added support for PropList-provided
//					information on how the SpreadSheet 
//					should look.
// 2003-03-12	JTS, RTi		Added clear() method.
// 2003-03-13	JTS, RTi		* Corrected fatal error in clear method.
//					* Added getData() methods.
// 2003-03-20	JTS, RTI		Many revisions following SAM's review.
// 2003-04-07	JTS, RTi		Overrode the getSelectedRowCount method
//					because JTable's was returning the wrong
//					number of rows.
// 2003-04-14	JTS, RTi		Overrode the getSelectedColumns and 
//					getSelectedColumnCount because the 
//					others were returning the wrong number
//					of columns.
// 2003-05-22	JTS, RTi		Added code so that the table can set
//					itself to have one click row selection
//					if the first row is clicked on.  
//					By default this is on.
// 2003-06-02	JTS, RTi		Added the __hourglassJFrame so that
//					an hourglass can be displayed when 
//					sorting.
// 2003-06-09	JTS, RTi		* Added the code to scroll to a specific
//					  row or column.
//					* Added hideColumn()
// 2003-06-11	JTS, RTi		* Added the createColumnList() code
//					* Added the first draft of the find()
//					  code for ints, doubles, Dates and
//					  Strings.
//					* Added code to do selection models 
//					  like the old JTable.
// 2003-06-13	JTS, RTi		* Added addRow() and deleteRow()
//					* Added setNewData()
//					* Added stopEditing() and 
//					  cancelEditing()
// 2003-06-17	JTS, RTi		* Deprecated setRenderer() to 
//					  setCellRenderer()
//					* Deprecated setWidths() to 
//					  setColumnWidths()
//					* Deprecated setNewData() to
//					  setData()
// 2003-06-18	JTS, RTi		Corrected bug caused by calling
//					setRowSelectionAllowed(false) in 
//					the JWorksheet's selection model.
// 2003-06-19	JTS, RTi		Added FIND_WRAPAROUND capability to 
//					the searches.
// 2003-06-25	JTS, RTi		* Added setColumnComboBoxValues()
//					* Added removeColumnComboBox()
// 2003-06-30	JTS, RTi		* Added code to set specific JComboBox
//					  editors on individual cells.
//					* Added code to support 
//					  JWorksheet_Listeners.
// 2003-07-01	JTS, RTi		* Added code for setting cell 
//					  attributes.
//					* Added code to set tooltips on column
//					  headers.
//					* Added code to allow multiple-line
//					  column headers.
// 2003-07-07	JTS, RTi		Added code to recalculate the 
//					necessary sizes for the col and row
//					selection models after a row is 
//					added or removed.
// 2003-07-10	JTS, RTi		Eliminated use of 
//					JWorksheet_RowCountCellRenderer in 
//					favor of simply using cell attributes.
// 2003-07-21	JTS, RTi		Documented properties in constructors
//					now, and put the property definitions
//					in an html table with more information
//					than was previously provided.
// 2003-07-22	JTS, RTi		Updated Javadocs following SAM's review.
// 2003-08-14	JTS, RTi		Javadoc'd several methods that had
//					been added recently.
// 2003-08-26	JTS, RTi		Added support for removing cell
//					attributes in the setCellAttributes()
//					method.
// 2003-09-03	JTS, RTi		* Corrected bug in getSelectedRows()
//					  and getSelectedColumns() that was 
//					  resulting in the incorrect number
//					  of selected rows being returned in
//					  non-excel selection mode.
//					* Added cellHasAttributes().
// 2003-09-10	JTS, RTi		Added MultipleDiscontinuousRowSelection
//					for selecting multiple discontinuous
//					rows using the old JTable selection 
//					mode.
// 2003-09-12	JTS, RTi		* Added refresh().
//					* Added getTableModel().
//					* Added getCellRenderer().
// 2003-09-16	JTS, RTi		* Added contains().
//					* Added new find() which looks for an
//					  object in a row.
// 2003-09-19	JTS, RTi		* Overrode isCellSelected() in order
//					  to javadoc it.
//					* RowSelectionModel and 
//					  ColSelectionModel are now recreated
//					  when a column is removed (in order
//					  to keep the selection arrays 
//					  properly sized).
//					* Added showHeader().
// 2003-09-23	JTS, RTi		* Added deselectAll().
//					* Overrode and deprecated 
//					  clearSelection().
// 2003-09-30	JTS, RTi		Overrode getValueAt() in order to 
//					javadoc it.
// 2003-10-07	JTS, RTi		Renamed setMultiLineHeader() to
//					setMultipleLineHeaderEnabled().
// 2003-10-13	JTS, RTi		* Added setColumnAlignment().
//					* Added getColumnAlignment().
// 2003-10-15	JTS, RTi		* Added selectAllRows().
//					* Added insertRowAt().
// 2003-10-20	JTS, RTi		* Added setRowData().
//					* Added isEmpty().
// 2003-10-21	JTS, RTi		Revised the code for adding and deleting
//					cell attributes.
// 2003-10-22	JTS, RTi		* Corrected getCellAttributes() so that
//					  it takes an absolute column.
//					* Corrected error in setCellAttributes()
//					  that was causing attributes for the
//					  cell at 0,0 to not be set properly.
//					* Added alternate text support, for use
//					  in places where predetermined String 
//					  text might need placed in a single
//					  cell in a numeric column.
//					* Added check in getRowData() for 
//					  null data returned internally, so that
//					  null is returned from the method 
//					  instead of throwing a null pointer
//					  exception.
//					* Removed an old constructor as it has
//					  been deprecated for a few months.
//					* Removed haveColumnsBeenHidden() as it
//					  has been deprecated for a few months.
//					* Removed hideColumn() as it has been
//					  deprecated for a few months.
//					* Removed overrideCellEdit() as it has 
//					  been deprecated for a few months.
//					* Removed setNewData() as it has been
//					  deprecated for a few months.
//					* Removed setNewModel() as it has been
//					  deprecated for a few months.
//					* Removed setRenderer() as it has been
//					  deprecated for a few months.
//					* Removed setWidths() as it has been
//					  deprecated for a few months.
//					* Added getColumnClass().
// 2003-10-23	JTS, RTI		Added getColumnName() methods.
// 2003-10-24	JTS, RTi		* Made JWorksheet a KeyListener for
//					  itself in order to trap events in
//					  the future.
//					* Overrode and deprecated JTable's
//					  selectAll() method.
//					* Overrode JTable's isEditing() method.
//					* Added 
//					  getCellSpecificJComboBoxValues().
//					* Added support for one-click column
//					  selection.
// 2003-11-04	JTS, RTi		* Added addHeaderMouseListener().
//					* Popup menu commands are now in
//					  private Strings.
// 2003-11-06	JTS, RTi		* Added warning messages to routines
//					  that can fail but which don't return
//					  failure values (are void).
// 2003-11-11	JTS, RTi		* Because of how __hourglassJFrame is 
//					  used in some other classes, it is now 
//					  initialized to a new JFrame() in the
//					  constructor, so that even if it is 
//					  never set, getHourglassJFrame() will 
//					  never return null.
//					* Worksheet can now be set dirty and
//					  checked for dirty.
//					* Added getAbsoluteColumnCount().
// 2003-11-12	JTS, RTi		All columns now use the 
//					JWorksheet_DefaultTableCellEditor class.
// 2003-11-18	JTS, RTi		* Added setEditCell().
//					* Added getEditRow().
//					* Added getEditColumn().
//					* Added finalize().
//					* Added selectColumn().
//					* Added code to shift the rows and 
//					  columns of cell alternate text and
//					  cell attributes as rows and columns 
//					  are added and deleted.
// 2003-11-19	JTS, RTi		Added getCellAtClick().
// 2003-11-26	JTS, RTi		Corrected a bug caused when compacting
//					the arrays storing alt text and 
//					attribute information.
// 2003-12-03	JTS, RTi		Added code to write the table out 
//					as an HTML file, and also to 
//					copy the table to the clipboard in
//					HTML form.
// 2003-12-09	JTS, RTi		Removed all references to a local 
//					private variable __header.  The table
//					header can now only be operated on
//					by calling getTableHeader().  This
//					corrected a bug that would cause 
//					columns to be non-resizable if a new
//					table model was set in the worksheet.
// 2003-12-10	JTS, RTi		Column tool tips are now re-set in
//					the worksheet after a call to 
//					setModel().
// 2004-01-07	JTS, RTi		Overrode isRowSelected() in order to
//					accomodate JWorksheet row selection.
// 2004-01-20	JTS, RTi		* Moved to use the new JWorksheet column
//					  headers as the default mode. 
//					* Began renaming variables to mark 
//					  whether they are associated with
//					  column or row headers.
//					* Deprecated some properties and
//					  removed some old properties that had
//					  been deprecated for months.
// 					* Revised javadocs.
// 2004-01-22	JTS, RTi		Corrected bug in which the popup 
//					column was treated as the absolute 
//					instead of the visible column.
// 2004-01-26	JTS, RTi		Changed getColumn() calls to allow 
//					multiple columns with the same name.
// 2004-01-27	JTS, RTi		Added deleteRows().
// 2004-01-30	JTS, RTi		Added AllowCopy and AllowPaste 
//					properties.
// 2004-02-10	JTS, RTi		Added getValueAtAsString().
// 2004-03-02	JTS, RTi		Added calculateColumnWidths() and 
//					related code.
// 2004-03-03	JTS, RTi		Added deselectRow().
// 2004-05-10	JTS, RTi		Removed long-deprecated methods.
// 2004-06-01	JTS, RTi		Improved the behavior of one-click
//					row selection.  Now responds properly
//					to holding down the control, shift, 
//					or control and shift keys.
// 2004-07-27	JTS, RTi		Added "Copy with Header" to popup 
//					menu.
// 2004-08-06	JTS, RTi		Added the ability to specify column
//					prefixes by the ColumnNumbering 
//					property.
// 2004-09-15	JTS, RTi		The popup trigger checking being done
//					when the worksheet or its header were
//					right-clicked on was not working in 
//					Linux.  Stopped using:
//					   MouseEvent.isPopupTrigger()
//					and changed to using:
//					   JPopupMenu.isPopupTrigger(MouseEvent)
//					and the popup menus are working now on
//					Linux.
// 2004-10-21	JTS, RTi		Added the right-click menu item for 
//					saving table data directly from the 
//					worksheet.
// 2004-11-01	JTS, RTi		Boolean columns can now be sorted.
// 2004-11-15	JTS, RTi		Added new property that allows row 
//					numbers to decrement the further down
//					the worksheet they go 
//					("IncrementRowNumbers=false").
// 2005-01-25	JTS, RTi		Correct bug caused by a null pointer
//					exception when reading properties.
// 2005-03-22	JTS, RTi		* Found some bugs caused by 1-column
//					  worksheets that were resolved.
//					* Trying to turn off the header 
//					  was causing lots of null pointer 
//					  exceptions.  Resolved.
// 2005-03-30	JTS, RTi		Changed how enableRowHeader() populates
//					its list, resulting in a huge 
//					performance increase on lists over 1000
//					elements.
// 2005-04-05	JTS, RTi		Added setHourglassJDialog().
// 2005-04-26	JTS, RTi		Added all data members to finalize().
// 2005-06-07	JTS, RTi		Converted MutableJLists to 
//					SimpleJLists.
// 2005-06-16	JTS, RTi		Corrected bug in find() where wrapped
//					searches were not going to the next
//					to last row in the wrap.
// 2005-10-31	JTS, RTi		Changed saveToFile() so that it 
//					correctly quotes fields containing the
//					delimiter string and also so that any
//					newlines in the strings are removed 
//					(this was an issue with data in the
//					RTiAdminAssistant).
// 2005-11-18	JTS, RTi		Changed saveToFile() so that it quotes
//					header lines, enforces a file extension,
//					and offers more output formats in the
//					file chooser.
// 2006-01-16	JTS, RTi		* Added getVisibleColumnCount().
//					* Added getColumnNumber().
// 2006-01-20	JTS, RTi		* Added "Copy All" to popup menu.
//					* Added "Copy All With Header" to popup
//					  menu.
// 2006-01-31	JTS, RTi		* Deprecated setHourglass() for
//					  setWaitCursor().
//					* Deprecated setNoHeader() for
//					  removeColumnHeader().
//					* Updated Javadocs.
//					* Renamed notifyAllListeners() to
//					  notifyAllWorksheetListeners().
//					* Renamed __listeners to
//					  __worksheetListeners.
//					* Worksheet is now an adjustment 
//					  listener for scrollpane adjustments.
// 2006-03-02	JTS, RTi		Added deselect/select options to the 
//					default popup menu.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import RTi.DMI.DMIUtil;
import RTi.GR.GRColor;
import RTi.Util.IO.HTMLWriter;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.StopWatch;

// TODO (2004-01-20) need to add main javadocs for dealing with the new row headers.

/**
This class extends the Swing JTable to create a Worksheet that mimics Excel's behavior.<p>
<b>Import note:</b><br>
Because of their reliance on underlying JTable code, JWorksheets do <b>NOT</b>
do well with multiple columns that have the exact same name.  Unexpected
results can occur.  This is an issue that may be REVISITed in the future, 
but in the meantime developers should not hope to have worksheets with two columns of the same name.
<p>
<b>Important note:</b><br>
In the documentation below, mention is made to <b>absolute</b> and <b>visible</b>
columns.  <b>Absolute</b> column numbers never change.  The seventh
<b>absolute</b> column is always the seventh <b>absolute</b> column, because
it is listed in the table model as being column seven.<p>
<b>Visible</b> column numbers change depending on how many columns have been
removed.  The seventh <b>absolute</b> column may be the third <b>visible</b>
column -- but if the first <b>visible</b> column is removed, the seventh
<b>absolute</b> column becomes the second <b>visible</b> column.<p>
For example, if a table has 5 columns, some of which are not visible, the 
absolute and visible column numbers are as shown:<p>
<pre>
[0] - visible     - 0
[1] - visible     - 1
[2] - not visible - n/a
[3] - not visible - n/a
[4] - visible     - 2
</pre>
The <b>absolute</b> column numbers are listed on the left-hand side.  The
<b>visible</b> column numbers are listed on the right-hand side.<p>
Methods explicitly say in their documentation whether they take an 
<b>absolute</b> or <b>visible</b> column number.  The methods 
<code>getVisibleColumn()</code> and <code>getAbsoluteColumn()</code> can be
used to convert between the two column types.<p>
In all cases, column and row numbers begin at zero.  A header is always
shown above the table columns -- but it is not a row, nor can it
be referenced as row 0, and it contains no row data.  It is a separate
object.  The worksheet is often used with table models to display the row
number in the first column, in which case column 1 (the second column)
begins the actual worksheet data.  This method is the old way of doing it,
however, as setting the property ShowRowHeader=true will use a separate
object to keep track of row numbers, and this is the preferred way.<p>

<b>Working with JWorksheets</b><p>
<b>Setting up a JWorksheet</b><p>
Here is some example code that sets up a JWorksheet (note that it uses 
a subclassed table renderer and table model data).<p><pre>
	// Create the proplist containing JWorksheet setup information.
	PropList p = new PropList("StateMod_Diversion_JFrame.JWorksheet");
	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.RowColumnPresent=false");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=SingleRowSelection");

	// try to create the worksheet.
	int[] widths = null;
	try {
		// custom table model
		StateMod_Diversion_TableModel tmd = new StateMod_Diversion_TableModel(__diversionsVector, __readOnly);
		// custom cell renderer
		StateMod_Diversion_CellRenderer crd = new StateMod_Diversion_CellRenderer(tmd);
	
		// create the table
		__worksheet = new JWorksheet(crd, tmd, p);

		// get the column widths
		widths = crd.getColumnWidths();
	}
	catch (Exception e) {
		// if there was a problem, simply create an empty JWorksheet.
		Message.printWarning(3, routine, e);
		__worksheet = new JWorksheet(0, 0, p);
		e.printStackTrace();
	}
	// this call prevents some odd resizing problems with JTable/JWorksheet
	__worksheet.setPreferredScrollableViewportSize(null);

	// set up the current JFrame to display the JWorksheet's hourglass when
	// necessary and also to listen to its key and mouse events.
	__worksheet.setHourglassJFrame(this);
	__worksheet.addMouseListener(this);	
	__worksheet.addKeyListener(this);

	...

	// this code must appear *AFTER* the GUI on which the JWorksheet
	// appears is shown.  
	if (widths != null) {
		__worksheet.setColumnWidths(widths);
	}
</pre>
<p>
<b>Cell Attributes</b><p>
The above example sets up the font the JWorksheet will use for rendering 
the cells in the JWorksheet and in the JWorksheet's header.  Different cell
attributes can still be applied to individual cells to override the JWorksheet
defaults.  The following code will change every other cell in the 3rd column
to have different attributes:<p><pre>
	JWorksheet_CellAttributes ca = new JWorksheet_CellAttributes();
	ca.backgroundColor = Color.red;
	ca.foregroundColor = Color.blue;
	ca.Font = new Font("Arial", Font.PLAIN, 11);	

	for (int i = 0; i < __worksheet.getRowCount(); i++) {
		__worksheet.setCellAttributes(i, 2, ca);
	}
</pre>
<p>
<b>JComboBoxes as Data Entry Fields</b><p>
The JWorksheet offers support for using JComboBoxes for data entry.  The 
following code demonstrates setting a JComboBox on all cells in a column:<p>
<pre>
	Vector v = new Vector();
	v.add("Red");
	v.add("Green");
	v.add("Blue");
	__worksheet.setColumnJComboBoxValues(4, v, true);
</pre>
The call to setColumnJComboBoxValues() sets the fifth column (4) to use 
a SimpleJComboBox for its data entry.  Users can select from a list of three
colors as possible data.  Because the third parameter was provided to 
setColumnJComboBoxValues() and the parameter is 'true', users can also type
in another color that doesn't appear in the combo box.<p>

As opposed to putting a single combo box with the same values on a column, 
individual cells within a column can be set to use combo boxes, and each 
combo box can have a different list of values.  If a column is set up to 
allow cell-specific placement of combo boxes, all the cells in which a 
combo box was NOT explicitly set will use the same text field data entry as 
normal data entry cells.  The following code demonstrates
placing different combo boxes on cells within a column:<p><pre>
	__worksheet.setCellSpecificJComboBoxColumn(3, true);

	Vector diversions = new Vector();
	diversions.add("Diversion 1");
	diversions.add("Diversion 2");

	Vector reservoirs = new Vector();
	reservoirs.add("Reservoir 1");
	reservoirs.add("Reservoir 2");

	Vector wells = new Vector();
	wells.add("Well 1");
	wells.add("Well 2");
	
	__worksheet.setCellSpecificJComboBoxValues(4, 3, diversions);
	__worksheet.setCellSpecificJComboBoxValues(5, 3, reservoirs);
	__worksheet.setCellSpecificJComboBoxValues(3, 3, wells);

	__worksheet.setCellSpecificJComboBoxEditorPreviousRowCopy(3, true);
</pre>
The above code first sets up the 3rd column to allow cell-specific placement
of combo boxes.  The 'true' parameter means that users will be able to type in
other values to the JComboBoxes if they value they want does not appear.<p>

The code then sets up a few Vectors of example entry values and applies 
combo boxes to rows 3, 4 and 5 in the worksheet.<p>

The last part of the code sets up what the worksheet column's behavior should be
when new rows are added after the last row.  In this case, the 'true' parameter
specifies that if the next-to-last row (the one just before the new row added)
has a cell-specific combobox set up in column 3, the same combo box should be
set up in column 3 of the new row, too, with all the same values.<p>

<b>Class Descriptions</b><p>
The following is a brief list of all the related JWorksheet_* classes and their purposes:<p>
<ul>
<li><b>JWorksheet_AbstractExcelCellRenderer</b> - This class is the cell
renderer used by most applications, as it provides the capability to properly
left- and right-justify the text in table cells depending on the kind of 
data stored in the cell.</li>
<li><b>JWorksheet_AbstractRowTableModel</b> - This abstract class is the table 
model from which many application table models will be built, as it provides 
support for storing a single data object in each row of a JWorksheet.</li>
<li><b>JWorksheet_AbstractTableCellRenderer</b> - This is the base class for
building all other Cell Renderers for JWorksheets.  It ensures that all 
JWorksheet cell renderers provide at least a getColumnWidths() method.</li>
<li><b>JWorksheet_AbstractTableModel</b> - This is the class from which all
the table models used in a JWorksheet must be built.  It provides some base
functionality common to all JWorksheet table models, such as row sorting.</li>
<li><b>JWorksheet_CellAttributes</b> - This class contains attributes that can
be set and applied to individual cells within the table.</li>
<li><b>JWorksheet_ColSelectionModel & JWorksheet_RowSelectionModel</b> - 
These classes provide the JWorksheet with the ability to do Microsoft Excel-like
cell selection.  Programmers should not need to work with these classes.</li>
<li><b>JWorksheet_CopyPasteAdapter</b> - This class provides support for copying
and pasting to JWorksheets.  Programs should not need to work directly with this class.</li>
<li><b>JWorksheet_DefaultTableCellEditor</b> - This class overrides the normal
editor used for editing cell values.  Programs should not need to work directly
with this class.</li>
<li><b>JWorksheet_DefaultTableCellRenderer</b> - This is the first
implementation of a Cell Renderer that can be use by a JWorksheet, if no
other Cell Renderer is provided.</li>
<li><b>JWorksheet_Header</b> - This is the class used to render the JWorksheet's
header.  It provide capability like column header tool tips.  Programmers should
not need to work with this class.</li>
<li><b>JWorksheet_HeaderCellRenderer</b> - This is the class used to render
the JWorksheet's header.  Programmers probably won't need to subclass this.</li>
<li><b>JWorksheet_JComboBoxCellEditor</b> - This class provides columns with
the ability to have combo boxes as data entry editors.</li>
<li><b>JWorksheet_Listener</b> - This class is an interface for other classes
that want to be informed whenever the table performs some actions such as 
adding or removing rows.</li>
<li><b>JWorksheet_RowCountCellRenderer</b> - This class is a cell renderer
for the optional first column of worksheet cells that display the number of the row.</li>
</ul>
*/
public class JWorksheet 
extends JTable 
implements ActionListener, KeyListener, MouseListener, AdjustmentListener {

// TODO TODO add setColumnEditable()

/**
JWorksheet selection model in which cells can be non-contiguously selected like in an Excel worksheet.
*/
private static final int __EXCEL_SELECTION = 1000;

/**
JTable Selection mode in which only a single cell can be selected at a time. 
*/
private static final int __SINGLE_CELL_SELECTION = ListSelectionModel.SINGLE_SELECTION + 100;

/**
Selection mode in which only a single row can be selected at a time.
*/
private static final int __SINGLE_ROW_SELECTION = ListSelectionModel.SINGLE_SELECTION;

/**
Selection mode in which multiple contiguous rows can be selected.
*/
private static final int __MULTIPLE_ROW_SELECTION = ListSelectionModel.SINGLE_INTERVAL_SELECTION;

/**
Selection mode in which multiple discontinuous rows can be selected.
*/
private static final int __MULTIPLE_DISCONTINUOUS_ROW_SELECTION = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

public static final int PRE_SELECTION_CHANGE = 0;
public static final int POST_SELECTION_CHANGE = 1;

private static final int __DESELECT_ALL = 100;
private static final int __SELECT_ALL = 101;

/**
Bit-mask parameter for find() that can be used with all searches to check 
if a value is equal to another value.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_EQUAL_TO = 1;

/**
Bit-mask parameter for numeric find()s that can check for values less than the 
specified value.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_LESS_THAN = 2;

/**
Bit-mask parameter for numeric find()s that can check for values greater 
than the 
specified value.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_GREATER_THAN = 4;

/**
Bit-mask parameter for String find()s that turns off case sensitivity.  By default, 
String searches are case-sensitive.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_CASE_INSENSITIVE = 16;

/**
Bit-mask parameter for String find()s to check for the value as a substring that
starts another string.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_STARTS_WITH = 32;

/**
Bit-mask parameter for String find()s to check for the value as a substring that
ends another string.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_ENDS_WITH = 64;

/**
Bit-mask parameter for String find()s to check for the value as a substring that
is contained within another string.  There is no default specifying how 
searches must be done, so parameters must be specified for searches.  For 
numeric and date searches, one of FIND_EQUAL_TO, FIND_LESS_THAN or 
FIND_GREATER_THAN must be specified.  For String searches, one of 
FIND_EQUAL_TO, FIND_CONTAINS, FIND_STARTS_WITH, or FIND_ENDS_WITH must be specified.
*/
public static final int FIND_CONTAINS = 128;

/**
Bit-mask parameter for all find()s to start searching from the back of the list
towards the front.  By default, finds work from the first record towards the 
last.  "First record" refers to the first record returned from the table model
when getValueAt() is called.
*/
public static final int FIND_REVERSE = 256;

/**
Bit-mask parameter for all find()s so that a search can start in the middle 
of a worksheet's data and wrap around and start again at the beginning.  
By default, searches start at the beginning of the table's data.
*/
public static final int FIND_WRAPAROUND = 512;

/**
Constant values used in specifying column alignments.
*/
public final static int
	DEFAULT = -1,
	CENTER = SwingConstants.CENTER,
	LEFT = SwingConstants.LEFT,
	RIGHT = SwingConstants.RIGHT;

/**
Types of column prefix numbering support.  __NUMBERING_NONE means no column
prefixes are set.  __NUMBERING_EXCEL means that column numbers in the format
A...Z AA...AZ BA...BZ ... are done.  __NUMBERING_0 means that the column prefix
is the number of the column, base-0.  __NUMBERING_1 means that the column
prefix is the number of the column, base-1.
*/
private final int
	__NUMBERING_NONE = -1,
	__NUMBERING_EXCEL = 0,
	__NUMBERING_0 = 1,
	__NUMBERING_1 = 2;

/**
Used with notifyAllWorksheetListeners to let JWorksheet_Listeners know that a 
row has been added.  Also used with adjustCellAttributesAndText() to inform that a row has been inserted.
*/
private final int __ROW_ADDED = 0;

/**
Used with notifyAllWorksheetListeners to let JWorksheet_Listeners know that a 
row has been deleted.  Also used with adjustCellAttributesAndText() to inform that a row has been deleted.
*/
private final int __ROW_DELETED = 1;

/**
Used with notifyAllWorksheetListeners to let JWorksheet_Listeners know that the 
row data has been changed with setData().  
*/
private final int __DATA_RESET = 2;

/**
Used with adjustCellAttributesAndText() to inform that a column has been removed from the table.
*/
private final int __COL_DELETED = 3;

/**
Popup menu labels.
*/
private final String
	__MENU_ORIGINAL_ORDER = 	"Original Order",
	__MENU_SORT_ASCENDING = 	"Sort Ascending",
	__MENU_SORT_DESCENDING = 	"Sort Descending",
	__MENU_COPY = 			"Copy",
	__MENU_COPY_HEADER = 		"Copy with Header",
	__MENU_COPY_ALL = 		"Copy All",
	__MENU_COPY_ALL_HEADER = 	"Copy All with Header",
	__MENU_DESELECT_ALL = 		"Deselect All",
	__MENU_PASTE = 			"Paste",
	__MENU_SAVE_TO_FILE = 		"Save to file ...",
	__MENU_SELECT_ALL = 		"Select All";

/**
The initial size of and size by which the cell attribute arrays grow.
*/
private final int __ARRAY_SIZE = 50;

/**
The class name.
*/
public final static String CLASS = "JWorksheet";

/**
An array sized the same as the number of columns in the current data model,
used to tell when columns have been hidden from view.
*/
private boolean[] __columnRemoved = null;

/**
Whether copying from the table has been enabled or not.
*/
private boolean __copyEnabled = false;

/**
Whether the worksheet is dirty or not.
*/
private boolean __dirty = false;

/**
Whether the row numbers along the left side go up in value or not.
*/
private boolean __incrementRowNumbers = true;

/**
Whether the control key is depressed.
*/
private boolean __isControlDown = false;

/**
Whether the shift key is depressed.
*/
private boolean __isShiftDown = false;

/**
Whether pasting from the table has been enabled or not.
*/
private boolean __pasteEnabled = false;

/**
Whether to select an entire column (with the Excel selection mode only) when the column header is clicked on.
*/
private boolean __oneClickColumnSelection = false;

/**
Whether to select an entire row when the row header is clicked on.
TODO (JTS - 2004-11-19) with the new row headers, it's very likely this won't work anymore.
*/
private boolean __oneClickRowSelection = false;

/**
Whether this worksheet's cells can be selected or not.
*/
private boolean __selectable = true;

/**
Whether to show the sorting popup menu.  Defaults to true unless another value is provided by the proplist.
*/
private boolean __showPopup = true;

/**
Whether to show the first column with the row count.
*/
private boolean __showRowCountColumn = true;

/**
Whether worksheet code is currently running any test version of the code.
*/
private boolean __testing = false;

/**
Whether to use the row headers that work similarly to the standard JTable column headers.
*/
private boolean __useRowHeaders = false;

/**
Whether the worksheet should handle displaying the regular popup menu or something external will.
*/
private boolean __worksheetHandlePopup = true;

/**
The color in which the header cells will be drawn.  Defaults to Color.LIGHT_GRAY
unless a new value is provided by the proplist.
*/
private Color __columnHeaderColor = null;

/**
The color in which the row count cells will be drawn.  Defaults to 
Color.LIGHT_GRAY unless a new value is provided by the proplist.
*/
// TODO SAM 2007-05-09 Evaluate whether used
//private Color __rowCountColumnColor = Color.LIGHT_GRAY;

/**
The background color for the row header.  Defaults to the system standard color.
*/
private Color __rowHeaderColor = null;

/**
Used to hold the 'compiled' version of the cell font, for quick retrieval by the cell renderers.
*/
private Font __cellFont = null;

/**
Array marking the columns of the cells with alternate text.
*/
private int[] __altTextCols;

/**
Array marking the rows of the cells with alternate text.
*/
private int[] __altTextRows;

/**
Array marking the columns of the cells with attributes.
*/
private int[] __attrCols;

/**
Array marking the rows of the cells with attributes.
*/
private int[] __attrRows;

/**
Used to override the default alignment of columns in the table.
*/
private int[] __columnAlignments = null;

/**
Count of all the cells with alternate text.
*/
private int __altTextCount = 0;

/**
The size of the font in which the table data should be displayed.  By default is 11.
*/
private int __cellFontSize = -1;

/**
The style of the cell font.  By default is Font.PLAIN.
*/
private int __cellFontStyle = -1;

/**
The size of the font in which the header should be displayed.  By default, is 12.
*/
private int __columnHeaderFontSize = -1;

/**
The style of the header font.  By default, is Font.BOLD.
*/
private int __columnHeaderFontStyle = -1;

/**
The size of the font in which the header should be displayed.  By default, is 12.
*/
private int __rowHeaderFontSize = -1;

/**
The style of the header font.  By default, is Font.BOLD.
*/
private int __rowHeaderFontStyle = -1;

/**
Count of all the cells with attributes.
*/
private int __attrCount = 0;

/**
The kind of column prefix numbering done.
*/
private int __columnNumbering = __NUMBERING_NONE;

/**
The <b>visible</b> column of the cell that was last edited.
*/
private int __editCol = -1;

/**
The row of the cell that was last edited.
*/
private int __editRow = -1;

/**
The first row number that appears in the row header.  All other row numbers are determined from this one.
*/
private int __firstRowNum = 1;

/**
The last row selected by clicking on the row header.
*/
private int __lastRowSelected = -1;

/**
The <b>visible</b> column on which the popup menu was last opened, used to keep track of which column to sort. 
*/
private int __popupColumn = -1;

/**
The selection mode in which the table is currently operating.  A -1 means
the JWorksheet mode, as opposed to one of the JTable modes, is in effect.
*/
private int __selectionMode = __EXCEL_SELECTION;

/**
The dialog in which the hourglass will be shown for sorting.  If null, the hourglass won't be shown.
*/
private JDialog __hourglassJDialog = null;

/**
The frame in which the hourglass will be shown for sorting.  If null, the hourglass won't be shown.
*/
private JFrame __hourglassJFrame = null;

/**
The item in the popup menu that allows a user to undo a sort operation.
*/
private JMenuItem __cancelMenuItem = null;

/**
The item in the popup menu that allows a user to copy cell contents.
*/
private JMenuItem __copyMenuItem = null;
private JMenuItem __copyAllMenuItem = null;
/**
The item in the popup menu that allows a user to copy cell contents with the
appropriate header information, too.
*/
private JMenuItem __copyHeaderMenuItem = null;
private JMenuItem __copyAllHeaderMenuItem = null;

private JMenuItem __deselectAllMenuItem = null;
private JMenuItem __selectAllMenuItem = null;

/**
The item in the popup menu that allows a user to paste into cells.
*/
private JMenuItem __pasteMenuItem = null;

/**
The popup menu that can be set to open when the table is right-clicked on.
*/
private JPopupMenu __mainPopup = null;

/**
The JPopupMenu that will appear when the table header is right-clicked on.
*/
private JPopupMenu __popup;

/**
The header of the table -- used in case the user turns off the header and then wants to turn it back on later.
*/
private JViewport __columnHeaderView = null;

/**
If this worksheet uses another worksheet as its row header, this is the reference to it.
*/
private JWorksheet __worksheetRowHeader = null;

/**
Array of the cell attributes for the cells with attributes.
*/
private JWorksheet_CellAttributes[] __cellAttrs;

/**
The attributes for the cells that compromise the "row count column" (in other words, column 0).
*/
private JWorksheet_CellAttributes __rowCountColumnAttributes;

/**
The adapter for doing copy and paste operations.
*/
private JWorksheet_CopyPasteAdapter __copyPasteAdapter = null;

/**
The default table cell renderer used for rendering cells in the table.
*/
private JWorksheet_DefaultTableCellRenderer __defaultCellRenderer = null;

/**
The renderer used to render the JWorksheet's header.
*/
private JWorksheet_HeaderCellRenderer __hcr;

/**
When two tables are tied together, for instance, one is the row header of the
other, this row model is used so that certain selections on the row header one
will make selections on the other one as well.
*/
private JWorksheet_RowSelectionModel __partner = null;

/**
The List that is used to function as a row header.
*/
private SimpleJList __listRowHeader = null;

/**
Array of the cell attributes for the cells with alternate text.
*/
private String[] __altText;
/**
An array, sized the same as the number of columns in the current table model,
that holds the names of the columns.
*/
private String[] __columnNames = null;

/**
The font name for use in table cells.  By default is "Arial".
*/
private String __cellFontName = null;

/**
The font name for use in the table header.  By default, is "Arial".
*/
private String __columnHeaderFontName = null;

/**
The font name for use in the table header.  By default, is "Arial".
*/
private String __rowHeaderFontName = null;

/**
A list of registered sort listeners.
*/
private List __sortListeners = null;

/**
A Vector of registered JWorksheet_Listeners.
*/
private List __worksheetListeners = null;

/**
In testing -- will probably be moved into a property, but maybe not, so it can be turned on and off.
This specifies whether when doing any sort of internal processing of numeric
data (eg, copying to clipboard, writing to a file, etc), missing values (-999)
will be output as -999 or as empty strings ("").
TODO (JTS - 2005-11-15)
*/
public boolean COPY_MISSING_AS_EMPTY_STRING = true;

/**
Constructor.  
@param cellRenderer a JWorksheet_DefaultTableCellRenderer object 
(or a class derived from JWorksheet_DefaultTableCellRenderer) that will be 
used for rendering cells holding Integers, Strings, Dates and Doubles.
@param tableModel the TableModel that will be used to fill the worksheet with data.
@param props the properties to configure the JWorksheet:
<table width=80% cellpadding=2 cellspacing=0 border=2>
<tr>
<td>Property</td>        <td>Description</td>     <td>Default</td>
</tr>

<tr>
<td>JWorksheet.CellFontName</td>
<td>The font face in which data cells will be drawn.  Example values are 
"Courier", "Arial", "Helvetica"</td>
<td>Default JTable font</td>
</tr>

<tr>
<td>JWorksheet.CellFontStyle</td>
<td>The font style in which cells will be drawn.  Example values 
are "Italic", "Bold", or "Plain"</td>
<td>"Plain"</td>
</tr>
	
<tr>
<td>JWorksheet.CellFontSize</td>
<td>The font size (in points) in which cells will be drawn.  Example values 
are 11, 14</td>
<td>Default JTable font size</td>
</tr>
	
<tr>
<td>JWorksheet.HeaderFontName</td>
<td>The font face in which data header cells will drawn.  Example values are
"Courier", "Arial", "Helvetica"</td>
<td>Default Windows font</td>
</tr>

<tr>
<td>JWorksheet.HeaderFontStyle</td>
<td>The font style in which header cells will be drawn.  Example values are 
"Italic", "Bold", or "Plain"</td>
<td>"Plain"</td>
</tr>
	
<tr>
<td>JWorksheet.HeaderFontSize</td>
<td>The font size in which header cells will be drawn.  Example values are 11,
14</td>
<td>Default Windows font size</td>
</tr>
	
<tr>
<td>JWorksheet.HeaderBackground</td>
<td>The background color in which header cells will be drawn. Example values are
"LightGray", "Blue"</td>
<td>"LightGray"</td>
</tr>
	
<tr>
<td>JWorksheet.RowColumnPresent</td>
<td>Whether the column with the row # count is shown or not.  Possible values 
are "true" or "false"</td>
<td>"true"</td>
</tr>
	
<tr>
<td>JWorksheet.RowColumnBackground</td>
<td>The color in which row count cells will be drawn.  Example values are 
"LightGray", "Blue"</td>
<td>"White"</td>
</tr>
	
<tr>
<td>JWorksheet.ShowPopupMenu</td>
<td>Whether to show the popup menu when the user right-clicks on the 
worksheet header.  This popup menu usually is used to sort the data in the 
column beneath the popup menu.
Values are "true" or "false"</td>
<td>"true"</td>
</tr>

<tr>
<td>JWorksheet.SelectionMode</td>
<td>The kind of selection mode to use in the JWorksheet.  Possible values are:
<ul>
<li><b>ExcelSelection</b> - Cell selection is similar to Microsoft Excel.  
Discontiguous cells and ranges of cells can all be selected at the same time.
</li>
<li><b>SingleCellSelection</b> - A single cell can be selected at a time.</li>
<li><b>SingleRowSelection</b> - A single row can be selected at a time.</li>
<li><b>MultipleRowSelection</b> - Multiple continuous rows can be selected
at one time.</b></li>
<li><b>MultipleDiscontinuousRowSelection</b> - Multiple discontinuous rows can
be selected at one time.</b></li>
</ul>
</td>
<td>"ExcelSelection"</td>
</tr>

<tr>
<td>JWorksheet.OneClickRowSelection</td>
<td>Whether to select all the values in a row when the row header is clicked
on.  Possible values are "true" or "false".  This doesn't work with the 
single-cell selection mode.</td>
<td>"false"</td>
</tr>

<tr>
<td>JWorksheet.OneClickColumnSelection</td>
<td>Whether to select all the values in a column when the column header is
clicked on.  Possible values are "true" or "false".  This only works
with the Excel selection mode.</td>
<td>"false"</td>
</tr>

<tr>
<td>JWorksheet.Unselectable</td>
<td>Whether any cell in the worksheet can be selected or not.  Possible values
are "true" or "false."  
REVISIT (JTS - 2004-11-19)
rename this to selectable, for clarity
</td>
<td>"false"</td>
</tr>

<tr>
<td>JWorksheet.Selectable</td>
<td>Whether any cell in the worksheet can be selected or not.  Possible values
are "true" or "false."  
</td>
<td>"true"</td>
</tr>

<tr>
<td>JWorksheet.AllowCopy</td>
<td>Whether to allow the user to copy data from worksheet cells to the 
clipboard.  Possible values are "true" or "false."</td>
<td>"false"</td>
</tr>

<tr>
<td>JWorksheet.AllowPaste</td>
<td>Whether to allow users to paste in values from the clipboard to groups
of cells.  Possible values are "true" or "false."</td>
<td>"false"</td>
</tr>

<tr>
<td>JWorksheet.ColumnNumbering</td>
<td>How to number the values in the header of the worksheet.  Possible 
values are "Base0", "Base1", "Excel", or "None."
</td>
<td>"Base1"</td>
</tr>

<tr>
<td>JWorksheet.FirstRowNumber</td>
<td>The number of the very top-most row in the worksheet.</td>
<td>"1"</td>
</tr>

<tr>
<td>JWorksheet.IncrementRowNumbers</td>
<td>Whether to increment or decrement the row numbers, starting from the
first row number.  If "true", the row numbers will get bigger from the
first row number by increments of 1.  If "false", the row numbers will get
smaller from the first row number by increments of 1.</td>
<td>"true"</td>
</tr>

</table>
</ul>

TODO (JTS - 2004-11-19) alphabetize the above
*/
public JWorksheet(JWorksheet_DefaultTableCellRenderer cellRenderer, 
JWorksheet_AbstractTableModel tableModel, PropList props) {
	setTableHeader(new JWorksheet_Header());
	__rowCountColumnAttributes = new JWorksheet_CellAttributes();

	// TODO (JTS - 2005-01-25)
	// reading the proplist is done twice in this constructor.  which 
	// one can be removed -- make sure it doesn't break existing worksheets!
	readPropList(props);
	tableModel._worksheet = this;
	__worksheetListeners = new Vector();

	// if one is not defined, do the following to avoid null checks
	__hourglassJFrame = new JFrame();

	__columnHeaderColor = null;
	// TODO SAM 2007-05-09 Evaluate whether used
	//__rowCountColumnColor = Color.LIGHT_GRAY;

	// create the arrays to hold cell attribute information
	__attrCols = new int[__ARRAY_SIZE];
	__attrRows = new int[__ARRAY_SIZE];
	__cellAttrs = new JWorksheet_CellAttributes[__ARRAY_SIZE];
	for (int i = 0; i < __ARRAY_SIZE; i++) {
		__attrCols[i] = -1;
		__attrRows[i] = -1;
		__cellAttrs[i] = null;
	}

	// create the arrays to hold cell alternate text
	__altTextCols = new int[__ARRAY_SIZE];
	__altTextRows = new int[__ARRAY_SIZE];
	__altText = new String[__ARRAY_SIZE];
	for (int i = 0; i < __ARRAY_SIZE; i++) {
		__altTextCols[i] = -1;
		__altTextRows[i] = -1;
		__altText[i] = null;
	}
	
	readPropList(props);

	setCellRenderer(cellRenderer);
	setModel(tableModel);
	if (tableModel.getColumnToolTips() != null) {
		setColumnsToolTipText(tableModel.getColumnToolTips());
	}

	__cellFont = new Font(__cellFontName, __cellFontStyle, __cellFontSize);
	// added as its own key listener here so that it is only intentionally done once	
	addKeyListener(this);
	if (getTableHeader() != null) {
		getTableHeader().addKeyListener(this);
	}
}

/**
Constructor.  Builds a JWorksheet with all empty cells with the given number
of rows and columns.  This version is mostly used to create a blank JWorksheet
object (0 rows, 0 cols) with the specified properties, before data are populated.
@param rows the number of rows in the empty worksheet.
@param cols the number of columns in the empty worksheet.
@param props PropList defining table characteristics.  See the first constructor.
*/
public JWorksheet(int rows, int cols, PropList props) {
	super(rows, cols);
	setTableHeader(new JWorksheet_Header());

	// if one is not defined, do the following to avoid null checks	
	__hourglassJFrame = new JFrame();

	// create the arrays to hold cell attribute information
	__attrCols = new int[__ARRAY_SIZE];
	__attrRows = new int[__ARRAY_SIZE];
	__cellAttrs = new JWorksheet_CellAttributes[__ARRAY_SIZE];
	for (int i = 0; i < __ARRAY_SIZE; i++) {
		__attrCols[i] = -1;
		__attrRows[i] = -1;
		__cellAttrs[i] = null;
	}	

	// create the arrays to hold cell alternate text
	__altTextCols = new int[__ARRAY_SIZE];
	__altTextRows = new int[__ARRAY_SIZE];
	__altText = new String[__ARRAY_SIZE];
	for (int i = 0; i < __ARRAY_SIZE; i++) {
		__altTextCols[i] = -1;
		__altTextRows[i] = -1;
		__altText[i] = null;
	}

	__worksheetListeners = new Vector();

	__columnHeaderColor = null;
	// TODO 2007-05-09 Evaluate whether used
	//__rowCountColumnColor = Color.LIGHT_GRAY;	

	__rowCountColumnAttributes = new JWorksheet_CellAttributes();
	readPropList(props);
	initialize(rows, cols);
	__cellFont = new Font(__cellFontName, __cellFontStyle, __cellFontSize);
	// added as its own key listener here so that it is only intentionally done once
	addKeyListener(this);
	if (getTableHeader() != null) {
		getTableHeader().addKeyListener(this);
	}
}

/**
Responds to actions, in this case popup menu actions.
@param event the ActionEvent that occurred.
*/
public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(__MENU_SORT_ASCENDING)) {
		setWaitCursor(true);
		notifySortListenersSortAboutToChange(StringUtil.SORT_ASCENDING);
		sortColumn (StringUtil.SORT_ASCENDING);		
		notifySortListenersSortChanged(StringUtil.SORT_ASCENDING);
		setWaitCursor(false);
	} 
	else if (command.equals(__MENU_SORT_DESCENDING)) {
		setWaitCursor(true);
		notifySortListenersSortAboutToChange(
			StringUtil.SORT_DESCENDING);
		sortColumn (StringUtil.SORT_DESCENDING);
		notifySortListenersSortChanged(StringUtil.SORT_DESCENDING);
		setWaitCursor(false);
	}
	else if (command.equals(__MENU_ORIGINAL_ORDER)) {
		setWaitCursor(true);
		notifySortListenersSortAboutToChange(-1);
		((JWorksheet_AbstractTableModel)getModel()).setSortedOrder(null);
		((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();
		__cancelMenuItem.setEnabled(false);
		notifySortListenersSortChanged(-1);
		setWaitCursor(false);
	}
	else if (command.equals(__MENU_COPY)) {
		copyToClipboard();
	}
	else if (command.equals(__MENU_COPY_HEADER)) {
		copyToClipboard(true);
	}
	else if (command.equals(__MENU_COPY_ALL)) {
		copyAllToClipboard();
	}
	else if (command.equals(__MENU_COPY_ALL_HEADER)) {
		copyAllToClipboard(true);
	}	
	else if (command.equals(__MENU_DESELECT_ALL)) {
		deselectAll();
	}
	else if (command.equals(__MENU_PASTE)) {
		pasteFromClipboard();
	}
	else if (command.equals(__MENU_SAVE_TO_FILE)) {
		saveToFile();
	}
	else if (command.equals(__MENU_SELECT_ALL)) {
		selectAllRows();
	}
}

/**
Adds a JWorksheet_Listener to the list of registered listeners.
@param l the listener to register.
*/
public void addJWorksheetListener(JWorksheet_Listener l) {
	__worksheetListeners.add(l);
}

/**
Adds a listener for mouse events on the worksheet's header.
@param l the MouseListener to add.
*/
public void addHeaderMouseListener(MouseListener l) {
	if (getTableHeader() != null) {
		getTableHeader().addMouseListener(l);
	}
}

/**
Adds a mouse listener for the JWorksheet.  To add a mouse listener for the 
worksheet's header, use addHeaderMouseListener().
@param l the MouseListener to add.
*/
public void addMouseListener(MouseListener l) {
	super.addMouseListener(l);
}

/**
Adds a new row of data after all the existing rows.  The data object passed
in must be valid for the current table model -- but this method will not do any checking to verify that.
@param o the object to add to the table model.
*/
public void addRow(Object o) {
	__lastRowSelected = -1;
	((JWorksheet_AbstractTableModel)getModel()).addRow(o);
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();	

	int rows = getRowCount();
	if (__selectionMode == __EXCEL_SELECTION) {
		int cols = getColumnCount();
		setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);			
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(rows, cols);
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}

		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);
	}

	if (__listRowHeader != null) {
		adjustListRowHeaderSize(__ROW_ADDED);
	}
	notifyAllWorksheetListeners(__ROW_ADDED, (getRowCount() - 1));
}

/**
Adds a sort listener.
@param listener the sort listener to add.
*/
public void addSortListener(JWorksheet_SortListener listener) {
	if (__sortListeners == null) {
		__sortListeners = new Vector();
	}
	__sortListeners.add(listener);
}

/**
Adjusts the cell attributes and alternate texts following a row being deleted,
a row being inserted within the table, or a column being deleted.  This is
so that all rows maintain the same attributes even when a new row is inserted or a row is deleted.  <p>
For example, consider a table had three rows and row 2 had cell attributes so 
that all of its cells were red, and rows 1 and 3 had no attributes and their cells appear normal.<p>
If a new row is inserted after the first row, this method makes sure that the
cell attributes that were previously on the second row now appear on the third 
row.  If the first row is deleted, this makes sure that the second row's 
attributes follow it as it becomes the new first row.<p>
All the above is done for cell alternate text, as well.
@param adjustment one of __ROW_ADDED, __ROW_DELETED or __COL_DELETED.
@param row the row that was inserted or deleted, or the column that was deleted.
*/
private void adjustCellAttributesAndText(int adjustment, int pos) {
	boolean compact = false;
	int attrLength = __attrRows.length;
	int altLength = __altTextRows.length;
	int row = pos;
	int col = pos;
	if (adjustment == __ROW_ADDED) {
		for (int i = 0; i < attrLength; i++) {
			if (__attrRows[i] >= row) {	
				__attrRows[i]++;
			}
		}
		for (int i = 0; i < altLength; i++) {
			if (__altTextRows[i] >= row) {	
				__attrRows[i]++;
				__altTextRows[i]++;
			}
		}		
	}
	else if (adjustment == __ROW_DELETED) {
		for (int i = 0; i < attrLength; i++) {
			if (__attrRows[i] == row) {	
				__attrCols[i] = -1;
				__attrRows[i] = -1;
				__cellAttrs[i] = null;
				__attrCount--;
			}
			else if (__attrRows[i] > row) {
				__attrRows[i] = __attrRows[i] - 1;
			}
		}
		for (int i = 0; i < altLength; i++) {
			if (__altTextRows[i] == row) {	
				__altTextCols[i] = -1;
				__altTextRows[i] = -1;
				__altText[i] = null;
				__altTextCount--;
			}
			else if (__altTextRows[i] > row) {
				__altTextRows[i] = __altTextRows[i] - 1;
			}
		}
	
		compact = true;
	}
	else if (adjustment == __COL_DELETED) {
		for (int i = 0; i < attrLength; i++) {
			if (__attrCols[i] == col) {
				__attrCols[i] = -1;
				__attrRows[i] = -1;
				__cellAttrs[i] = null;
				__attrCount--;
			}
		}
		for (int i = 0; i < altLength; i++) {
			if (__attrCols[i] == col) {
				__altTextCols[i] = -1;
				__altTextRows[i] = -1;
				__altText[i] = null;
				__altTextCount--;
			}
		}
		
		compact = true;
	}

	if (compact) {
		compactAttrArrays();
		compactAltTextArrays();
	}
}

/**
Adjusts the row header in response to a change in the table size.
@param adjustment the kind of change that happened.  Must be one of:<p>
<ul><li>__ROW_ADDED - if a single row was added</li>
<li>__ROW_DELETED - if a single row was deleted</li>
<li>__DATA_RESET - if more than a single row changed</li>
</ul>
*/
private void adjustListRowHeaderSize(int adjustment) {
	int size = __listRowHeader.getListSize();
	if (adjustment == __ROW_ADDED) {
		__listRowHeader.add("" + (size + 1));
	}
	else if (adjustment == __ROW_DELETED) {
		__listRowHeader.remove(size - 1);
	}
	else if (adjustment == __DATA_RESET) {
		__listRowHeader.removeAll();
		int rows = getRowCount();
		// Simple row header is just the number of the row
		List v = new Vector();
		for (int i = 0; i < rows; i++) {
			v.add("" + (i + 1));
		}
		__listRowHeader = new SimpleJList(v);
		__listRowHeader.addKeyListener(this);
		__listRowHeader.setFixedCellWidth(50);
		__listRowHeader.setFixedCellHeight(getRowHeight());
		Font font = new Font(__rowHeaderFontName, __rowHeaderFontStyle, __rowHeaderFontSize);
		__listRowHeader.setCellRenderer( new JWorksheet_RowHeader(this, font, __rowHeaderColor));
		__listRowHeader.addMouseListener(this);
		__worksheetRowHeader = null;		
		Container p = getParent();
		if (p instanceof JViewport) {
			Container gp = p.getParent();
			if (gp instanceof JScrollPane) {			
				JScrollPane jsp = (JScrollPane)gp;
				__listRowHeader.setBackground(jsp.getBackground());
				jsp.setRowHeaderView(__listRowHeader);		
			}
		}
	}
}

/**
Responds to adjustment events caused by JScrollPanes being scrolled.
This method does nothing on Windows machines, but on UNIX machines it forces a
repaint of the JScrollPane for every scroll adjustment.  This is because on 
certain exceed connections, scrolling a large worksheet was resulting in the
worksheet becoming unreadable.  No exceed settings could be found that would 
solve this, so the following is done.  There is a slight performance hit, and
the display scrolls a little less smoothly, but at least the data are legible.
@param event the AdjustmentEvent that occurred.
*/
public void adjustmentValueChanged(AdjustmentEvent event) {
	if (IOUtil.isUNIXMachine()) {
		getParent().repaint();
	}
}

/**
Applies cell attributes to the specified cell.
@param cell the cell to which to apply attributes
@param ca the attributes to apply.  If null, then the default JTable settings will be used.
@param selected whether this cell is selected or not.
*/
private Component applyCellAttributes(Component cell,
JWorksheet_CellAttributes ca, boolean selected) {
	if (ca == null) {
//		Message.printStatus(1, "", "NULL NULL - " + selected);
		Color bg = null;
		Color fg = null;
		if (selected) {
			bg = UIManager.getColor("Table.selectionBackground");
			fg = UIManager.getColor("Table.selectionForeground");
		}
		else {
			bg = UIManager.getColor("Table.background");
			fg = UIManager.getColor("Table.foreground");
		}
		cell.setBackground(bg);
		cell.setForeground(fg);
		cell.setEnabled(true);
//		cell.setFont(UIManager.getFont("Table.font"));
		cell.setFont(__cellFont);
		if (cell instanceof JLabel) {
			((JLabel)cell).setVerticalAlignment( SwingConstants.CENTER);
		}		
		return cell;		
	}
	else {
//		Message.printStatus(1, "", "NOT NULL - " + selected);
//		Message.printStatus(1, "", ca.toString());
		if (ca.font != null) {
			cell.setFont(ca.font);
		}
		else {
			if (ca.fontName != null && ca.fontSize != -1 && ca.fontStyle != -1) {
				cell.setFont(new Font(ca.fontName, ca.fontSize, ca.fontStyle));
			}
			else {
				cell.setFont(UIManager.getFont("Table.font"));
			}
		}
		if (!selected) {
			if (ca.backgroundColor != null) {
				cell.setBackground(ca.backgroundColor);
			}
			else {
				cell.setBackground(UIManager.getColor( "Table.background"));
			}
			
			if (ca.foregroundColor != null) {
				cell.setForeground(ca.foregroundColor);
			}
			else {
				cell.setForeground(UIManager.getColor( "Table.foreground"));
			}
		}
		else {
			if (ca.backgroundSelectedColor != null) {
				cell.setBackground(ca.backgroundSelectedColor);
			}
			else {
				cell.setBackground(UIManager.getColor( "Table.selectionBackground"));
			}
			
			if (ca.foregroundSelectedColor != null) {
				cell.setForeground(ca.foregroundSelectedColor);
			}
			else {
				cell.setForeground(UIManager.getColor( "Table.selectionForeground"));
			}
		}

		if (cell instanceof JComponent) {
			if (ca.borderColor != null) {
				((JComponent)cell).setBorder( new LineBorder(ca.borderColor));	
			}
			else {
				((JComponent)cell).setBorder(null);
			}
		}
		if (cell instanceof JLabel) {
			if (ca.horizontalAlignment != -1) {
				((JLabel)cell).setHorizontalAlignment(ca.horizontalAlignment);
			}
			if (ca.verticalAlignment != -1) {
				((JLabel)cell).setVerticalAlignment(ca.verticalAlignment);
			}
			else {
				((JLabel)cell).setVerticalAlignment(SwingConstants.CENTER);
			}
		}

		cell.setEnabled(ca.enabled);

		return cell;	
	}
}

/**
Checks to see whether any columns have been removed
@return true if any columns have been removed, false if not.
TODO (JTS - 2004-11-19) I really dislike this method name.  But what's better to fit the style:
	
	if (worksheet.XXXXX())

?

if (worksheet.hasRemovedColumns()) {

if (worksheet.isMissingColumns()) {

????
*/
public boolean areColumnsRemoved() {
	int size = __columnNames.length;
	for (int i = 0; i < size; i++) {
		if (__columnRemoved[i] == true) {
			return true;
		}
	}
	return false;
}

/**
Tries to determine column widths that look best.  It does this by looking at
the column name and finding the largest token (as split out by spaces, commas
and newlines), and then forming the rest of the tokens into lines of text no
larger than the longest one.  <p>
This method should be used for tables that are on GUIs that are visible.  
For other tables (tables being created behind the scenes) get a Graphics object
and pass it to the other calculateColumnWidths method.
*/
public void calculateColumnWidths() {
	calculateColumnWidths(0, getGraphics());
}

/**
Tries to determine column widths that look best.  It does this by looking at
the column name and finding the largest token (as split out by spaces, commas
and newlines), and then forming the rest of the tokens into lines of text no
larger than the longest one.  Alternately, a minimum width can be set so that
even if the length of the longest token (in pixels drawn on-screen) is less
than this width, the column will be padded out to that many pixels wide. <p>
This method should be used for tables that are on GUIs that are visible.  
For other tables (tables being created behind the scenes) get a Graphics object
and pass it to the other calculateColumnWidths method.
@param minWidth the mininimum number of pixels wide the column should be.
*/
public void calculateColumnWidths(int minWidth) {
	calculateColumnWidths(minWidth, getGraphics());
}

/**
Tries to determine column widths that look best.  It does this by looking at
the column name and finding the largest token (as split out by spaces, commas
and newlines), and then forming the rest of the tokens into lines of text no
larger than the longest one.  Alternately, a minimum width can be set so that
even if the length of the longest token (in pixels drawn on-screen) is less
than this width, the column will be padded out to that many pixels wide. 
@param minWidth the mininimum number of pixels wide the column should be.
@param g a Graphics context to use for determining the width of certain 
Strings in pixels.  If the worksheet is on a visible gui, 
worksheet.getGraphics() should be passed in.
*/
public void calculateColumnWidths(int minWidth, Graphics g) {
	calculateColumnWidths(minWidth, 0, g);	
}

/**
Tries to determine column widths that look best.  It does this by looking at
the column name and finding the largest token (as split out by spaces, commas
and newlines), and then forming the rest of the tokens into lines of text no
larger than the longest one.  The minimum width, if greater than 0, 
means that even if the length of the longest token (in pixels drawn on-screen) 
is less than this width, the column will be padded out to that many pixels 
wide. In addition, if rows is greater than 0, then the data in the column 
in rows from 0 to rows (or getRowCount(), if less than rows) will be used
for determining how wide the column should be.  If the width of the widest 
data item is greater than the minimum width and the width of the widest token
in the column name, then it will be used.
@param minWidth the mininimum number of pixels wide the column should be.
@param rows the number of rows to look through and check data for widths.  
<b>Caution:</b> while checking the data to determine a column width can be
effective for properly sizing a column, it can also be very inefficient and performance will be slow.
@param g a Graphics context to use for determining the width of certain 
Strings in pixels.  If the worksheet is on a visible gui, 
worksheet.getGraphics() should be passed in.
*/
public void calculateColumnWidths(int minWidth, int rows, Graphics g) {
	calculateColumnWidths(minWidth, rows, null, g);
}

public void calculateColumnWidths(int minWidth, int rows, int[] skipCols, Graphics g) {
	int count = __columnNames.length;
	String name = null;
	TableColumn tc = null;
	int size = 0;
	int maxLines = 1;
	int lineCount = 0;	
	String[] names = new String[count];

	// first loop through all the columns, building a nicely-sized
	// column name and counting the number of lines the biggest column name will occupy.
	for (int i = 0; i < count; i++) {
		if (!__columnRemoved[i]) {
			name = getColumnName(getVisibleColumn(i));
			name = determineName(name, minWidth, g);

			lineCount = countLines(name);
			if (lineCount > maxLines) {
				maxLines = lineCount;
			}

			names[i] = name;
		}
	}

	boolean shouldDo = ((JWorksheet_AbstractTableModel)getModel()).shouldDoGetConsecutiveValueAt();

	((JWorksheet_AbstractTableModel)getModel()).shouldDoGetConsecutiveValueAt(true);

	// then loop through all the columns, making sure that there are an 
	// equal number of lines in every column name (and if not, pad out
	// the column name with newlines at its beginning), and then putting
	// the name and the width into the column.
	boolean skip = false;
	int dataWidth = 0;
	for (int i = 0; i < count; i++) {
		skip = false;
		if (skipCols != null) {
			for (int j = 0; j < skipCols.length; j++) {
				if (i == skipCols[j]) {
					skip = true;
				}
			}
		}
		if (!skip && !__columnRemoved[i]) {
			StopWatch sw = new StopWatch();
			sw.clear();
			sw.start();
			((JWorksheet_AbstractTableModel)getModel()).shouldResetGetConsecutiveValueAt(true);

			name = names[i];
			lineCount = countLines(name);
			for (int j = lineCount; j < maxLines; j++) {
				name = "\n" + name;
			}
			
			setColumnName(i, name);
			size = getColumnNameFitSize(name, g);
			if (size < minWidth) {
				size = minWidth;
			}
			if (rows > 0) {
				dataWidth = getDataMaxWidth(i, rows, g);
				if (dataWidth > size) {
					size = dataWidth;
				}
			}
			tc = getColumnModel().getColumn(getVisibleColumn(i));		
			tc.setPreferredWidth(size);			
			sw.stop();
		}
	}

	((JWorksheet_AbstractTableModel)getModel()).shouldDoGetConsecutiveValueAt(shouldDo);	
	((JWorksheet_AbstractTableModel)getModel()).shouldResetGetConsecutiveValueAt(true);		
}

/**
Programmatically stops any cell editing that is taking place.  Cell editing
happens when a user double-clicks in a cell or begins typing in a cell.  
This method will stop the editing and will NOT accept the data the user has
entered up to this method call.  To accept the data the user has already entered, use stopEditing().
*/
public void cancelEditing() {
	if (getCellEditor() != null) {
		getCellEditor().cancelCellEditing();
	}
}

/**
Returns whether the specified cell has any attributes set.
@param row the row of the cell to check
@param absoluteColumn the <b>absolute</b> column of the cell to check
@return true if the cell has any attributes set, false if not.
*/
public boolean cellHasAttributes(int row, int absoluteColumn) {
	if (getCellAttributes(row, absoluteColumn) == null) {
		return false;
	}
	return true;
}

/**
Clears the existing data from the worksheet and leaves it empty.
*/
public void clear() {
	__lastRowSelected = -1;
	if (getRowCount() > 0) {
		((JWorksheet_AbstractTableModel)getModel()).clear();
		((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();
		if (__listRowHeader != null) {
			adjustListRowHeaderSize(__DATA_RESET);
		}
	}
}

/**
Returns the <b>absolute</b> column at the specified point, or -1 if none are at that point.
@param point the Point at which to return the column.
@return the <b>absolute</b> column at the specified point, or -1 if none are at that point.
*/
public int columnAtPoint(Point point) {
	return super.columnAtPoint(point);
}

/**
Compacts the arrays used for storing alternate cell text so that 
any used parts of the arrays are at the end.
*/
private void compactAltTextArrays() {
	int hit = -1;
	int length = __altTextCols.length;
//	String c = "";
//	for (int i = 0; i < length; i++) {
//		c = c + __altTextCols[i] + "  ";
//	}
//	Message.printStatus(1, "", c);
	for (int i = 0; i < __altTextCount; i++) {
		for (int j = i; j < length; j++) {
			if (__altTextCols[j] > -1 && __altTextRows[j] > -1) {
				hit = j;
				j = length + 1;
			}
		}
		if (i != hit) {
			__altTextCols[i] = __altTextCols[hit];
			__altTextRows[i] = __altTextRows[hit];
			__altText[i] = __altText[hit];
	
			__altTextCols[hit] = -1;
			__altTextRows[hit] = -1;
			__altText[hit] = null;
		}
	}
//	c = "";
//	for (int i = 0; i < length; i++) {
//		c = c + __altTextCols[i] + "  ";
//	}
//	Message.printStatus(1, "", c);	
}

/**
Compacts the arrays used for storing cell attribute information so that 
any used parts of the arrays are at the end.
*/
private void compactAttrArrays() {
	int hit = -1;
	int length = __attrCols.length;
//	String c = "";
//	for (int i = 0; i < length; i++) {
//		c = c + __attrCols[i] + "  ";
//	}
//	Message.printStatus(1, "", c);
	for (int i = 0; i < __attrCount; i++) {
		for (int j = i; j < length; j++) {
			if (__attrCols[j] > -1 && __attrRows[j] > -1) {
				hit = j;
				j = length + 1;
			}
		}
		if (i != hit) {
			__attrCols[i] = __attrCols[hit];
			__attrRows[i] = __attrRows[hit];
			__cellAttrs[i] = __cellAttrs[hit];
	
			__attrCols[hit] = -1;
			__attrRows[hit] = -1;
			__cellAttrs[hit] = null;
		}
	}
//	c = "";
//	for (int i = 0; i < length; i++) {
//		c = c + __attrCols[i] + "  ";
//	}
//	Message.printStatus(1, "", c);	
}

/**
Checks to see whether the worksheet contains the given object.  This method
should only be used with table models that store an object in each row.
It checks through each row and compares the object stored at that row with
the specified object using '<tt>.equals()</tt>'.  If the objects match, true is
returned.  Otherwise, false is returned.
@param o the object that should be compared to the objects stored in the table.
Null is an acceptable value to pass in, in which case the worksheet will search
for whether the table contains any null values.
@return true if the object could be found in the worksheet.  False if it could not.
*/
public boolean contains(Object o) {
	if (!(getModel() instanceof JWorksheet_AbstractRowTableModel)) {
		return false;
	}

	int size = getRowCount();
	if (o != null) {
		for (int i = 0; i < size; i++) {
			if (o.equals(getRowData(i))) {
				return true;
			}
		}			
	}
	else {
		for (int i = 0; i < size; i++) {
			if (getRowData(i) == null) {
				return true;
			}
		}
	}	
	return false;
}

/**
Converts a column name with newlines into one without.
@param s the column name to convert.
@return a column name without newlines or back-to-back spaces.
*/
public static String convertColumnName(String s) {
	s = StringUtil.replaceString(s, "\n", " ");
	while (s.indexOf("  ") > -1) {
		s = StringUtil.replaceString(s, "  ", " ");
	}
	s = s.trim();

	return s;
}

/**
Copies the table to the clipboard in HTML format.
*/
public void copyAsHTML() 
throws Exception {
	copyAsHTML(0, getRowCount() - 1);
}

/**
Copies the specified rows to the clipboard in HTML format.
@param firstRow the firstRow to copy to the clipboard.
@param lastRow the lastRow to be copied.
*/
public void copyAsHTML(int firstRow, int lastRow) 
throws Exception {
	IOUtil.copyToClipboard(createHTML(null, null, firstRow, lastRow));
}

/**
Copies the currently selected cells to the clipboard.
*/
public void copyAllToClipboard() {
	copyAllToClipboard(false);
}

/**
Copies the currently selected cells to the clipboard.
@param includeHeader whether to include the header data for the copied cells
in the first line of copied information. 
*/
public void copyAllToClipboard(boolean includeHeader) {
	if (__copyPasteAdapter == null) {
		__copyPasteAdapter = new JWorksheet_CopyPasteAdapter(this);
	}
	__copyPasteAdapter.copyAll(includeHeader);
}	

/**
Copies the currently selected cells to the clipboard.
*/
public void copyToClipboard() {
	copyToClipboard(false);
}

/**
Copies the currently selected cells to the clipboard.
@param includeHeader whether to include the header data for the copied cells
in the first line of copied information. 
*/
public void copyToClipboard(boolean includeHeader) {
	if (__copyPasteAdapter == null) {
		__copyPasteAdapter = new JWorksheet_CopyPasteAdapter(this);
	}
	__copyPasteAdapter.copy(includeHeader);
}	

/**
Used by calculateColumnWidths as a utility method for counting the number of lines in a String.
@param name a column name.
@return the number of lines high the name is (ie, how many lines it will occupy in the header).
*/
private int countLines(String name) {
	List v = StringUtil.breakStringList(name, "\n", 0);
	return v.size();
}

/**
This is used to make removing columns nicer.  In the original JTable code, if
there are 3 columns ("a", "b" and "c") and the user removes column 2, then 
they have to know that "c" is now column 2, not column 3.  Hassles for the
programmer.  This way, if the programmer says to remove column 18, it ALWAYS
refers to the column that was #18 in the table model when the table was first built.
*/
private void createColumnList() {
	int columnCount = getColumnCount();
	
	__columnRemoved = new boolean[columnCount];
	__columnNames = new String[columnCount];
	__columnAlignments = new int[columnCount];

	TableColumn col = null;
	JWorksheet_DefaultTableCellEditor dtce = null;
	for (int i = 0; i < columnCount; i++) {
		__columnRemoved[i] = false;
		__columnNames[i] = getColumnName(i);
		__columnAlignments[i] = DEFAULT;

		col = getColumnModel().getColumn(i);
		dtce = new JWorksheet_DefaultTableCellEditor();
		col.setCellEditor(dtce);
	}
}

/**
Creates an HTML representation of the table and possibly returns it.
@param htmlWriter the HTMLWriter into which to create the html representation.
This parameter can be null, in which case a filename can be specified.  If
both the htmlWriter and the filename parameters are not null, the htmlWriter
takes precedence and HTML will be written there.
@param filename the name of the file to write the table into.  If null and
the htmlWriter parameter is null, the HTML will be generated in memory and 
returned as a String.  If both the htmlWriter and the filename parameters 
are not null, the htmlWriter takes precedence and HTML will be written there.
@param firstRow the first row of the table from which to begin turning data into HTML.
@param lastRow the last row of the table to be written to HTML.
@return a String representation of the table, if both the htmlWriter and 
filename parameters are null.  If either are not null, null is returned.
@throws Exception if an error occurs, or if the table model is not derived from
JWorksheet_AbstractTableModel, or if firstRow or lastRow are out of bounds.
*/
private String createHTML(HTMLWriter htmlWriter, String filename, 
int firstRow, int lastRow) 
throws Exception {
	// the table model used in the worksheet absolute must be derived
	// from the JWorksheet_AbstractTableModel, in order to have access
	// to methods for formatting and aligning the row data.
	TableModel model = getModel();
	if (!(model instanceof JWorksheet_AbstractTableModel)) {
		throw new Exception("Table model not derived from JWorksheet_AbstractTableModel");
	}

	if (firstRow < 0) {
		throw new Exception("First row less than zero: " + firstRow);
	}
	if (lastRow >= getRowCount()) {
		String plural = "s";
		if (getRowCount() == 1) {
			plural = "";
		}
		throw new Exception("Last row out of bounds: " + lastRow 
			+ "  (" + getRowCount() + " row" + plural + " in table)");
	}
	if (lastRow < firstRow) {
		throw new Exception("Last row (" + lastRow + ") less than first row (" + firstRow + ")");
	}
	
	JWorksheet_AbstractTableModel tableModel = (JWorksheet_AbstractTableModel)model;
	JWorksheet_AbstractExcelCellRenderer renderer = (JWorksheet_AbstractExcelCellRenderer)getCellRenderer();

	HTMLWriter html = null;
	if (htmlWriter == null) {
		// only create an HTML header if a filename was specified.  If
		// no filename was specified, then this is just creating a snippet of HTML code.
		boolean createHeader = false;
		if (filename != null) { 
			createHeader = true;
		}	
		html = new HTMLWriter(filename, "Table Data", createHeader);
	}
	else {
		html = htmlWriter;
	}

	html.tableStartFloatLeft("border=1");

	int columnCount = getColumnCount();
	int curCol = 0;
	String s;
	
	// prints out the first row -- which is the name of all the visible columns in the table.
	html.tableRowStart();
	for (curCol = 0; curCol < getColumnCount(); curCol++) {
		s = getColumnName(curCol, true);
		html.tableHeaderStart("align=center");
		html.addText(s);
		html.tableHeaderEnd();
	}
	html.tableRowEnd();

	// mass of variables used in looping through all the cells 
	boolean right = false;
	Class colClass = null;
	Color bg = null;
	Color fg = null;
	double d = 0;
	int align = 0;
	int i = 0;
	JWorksheet_CellAttributes ca = null;
	String alignCode = null;
	String altText = null;
	String format = null;

	// output all the specified rows of data.  Loop through row by row ...
	for (int curRow = firstRow; curRow <= lastRow; curRow++) {
		html.tableRowStart();
		
		// and then loop through column by column ...
		for (curCol = 0; curCol < columnCount; curCol++) {
			right = false;
			alignCode = "";

			// if the current cell has any attributes set, get them
			// and store the settings for the background color and the foreground color.
			ca=getCellAttributes(curRow, getAbsoluteColumn(curCol));
			if (ca != null) {
				bg = ca.backgroundColor;
				fg = ca.foregroundColor;
			}
			else {
				bg = null;
				fg = null;
			}

			// retrieve all the important data about this cell, such
			// as the data in it, the kind of data in it, the
			// way the data should be formatted, and whether there is any alternate text specified.
			colClass = tableModel.getColumnClass(getAbsoluteColumn(curCol));
			Object o = getValueAt(curRow, curCol);
			format= renderer.getFormat(getAbsoluteColumn(curCol));
			align = getColumnAlignment(getAbsoluteColumn(curCol));
			altText = getCellAlternateText(curRow, getAbsoluteColumn(curCol));
//			Message.printStatus(1, "", "[" + curRow + "][" + curCol + "]: " + "class: " + colClass 
//				+ "  o: " + o + "  format: " + format + "  align: " + align + "  alt: " + altText);

			// o should probably never be null, but just in case set its cell's data to be equivalent to a 
			// blank string
			if (o == null) {
				s = " ";
			}
			// if there is any alternate text in the cell, that overrides whatever data is stored in the cell
			else if (altText != null) {
				s = altText;
			}
			// integer cells will have formatting information and should be aligned differently from
			// other cells.
			else if (colClass == Integer.class) {
				right = true;			
				i = (new Integer(o.toString())).intValue();
				if (DMIUtil.isMissing(i)) {
					s = " ";
				}
				else {
					s = StringUtil.formatString(o.toString(), format);
				}
			}
			// double cells will have formatting information and
			// should be aligned differently from other cells.
			else if (colClass == Double.class) {
				right = true;			
				d = (new Double(o.toString())).doubleValue();
				if (DMIUtil.isMissing(d)) {
					s = " ";
				}
				else {
					s= StringUtil.formatString(o.toString(), format);
				}
			}
			// string cells will have formatting information.
			else if (colClass == String.class) {
				s=StringUtil.formatString(o.toString(), format);
			}
			// all other cell data should just be turned into a string.
			else {
				s = o.toString();
			}

			s = s.trim();

			// if the cell's data was blank, pad it out to at least two spaces, so that the cell actually
			// shows up in the HTML version of the table.  HTML table cells with only " " or "" stored in
			// them are not even rendered in most HTML tables!
			if (s.equals("")) {
				s = "  ";
			}

			// if no special alignment information has been set for this cell, determine the align code for
			// the cell from the cell data type specified above.
			if (align == DEFAULT) {
				if (right == false) {
					alignCode = "align=left";
				}
				else {
					alignCode = "align=right";
				}
			}
			// otherwise, if special alignment information has been set, that overrides all other alignment 
			// information and should be used.
			else {
				if (align == LEFT) {
					alignCode = "align=left";
				}
				else if (align == RIGHT) {
					alignCode = "align=right";
				}
				else if (align == CENTER) {
					alignCode = "align=center";
				}
			}

			// if the cell has a background color, then specify it in the cell opening tag.
			if (bg != null) {
				html.tableCellStart(alignCode + " " + "bgcolor=#"
					+ MathUtil.decimalToHex(bg.getRed()) + ""
					+ MathUtil.decimalToHex(bg.getGreen()) + ""
					+ MathUtil.decimalToHex(bg.getBlue()));
			}
			else {
				html.tableCellStart(alignCode);
			}

			// if the cell has a foreground color, then open a font tag that changes the font color to the 
			// specified color
			if (fg != null) {
				html.fontStart("color=#"
					+ MathUtil.decimalToHex(fg.getRed()) + ""
					+ MathUtil.decimalToHex(fg.getGreen()) + ""
					+ MathUtil.decimalToHex(fg.getBlue()));
			}

			// finally, put the text that should appear in the cell into the cell
			html.addText(s);

			// and if a font tag was set because of a foreground color, turn it off
			if (fg != null) {
				html.fontEnd();
			}
			html.tableCellEnd();
		}
 
		html.tableRowEnd();
	}
	html.tableEnd();

	// if these are true, the HTML is being generated in memory
	if (filename == null && htmlWriter == null) {
		return html.getHTML();
	}

	// if this is true, then a new html file was created and must be closed.
	if (htmlWriter == null) {
		html.closeFile();
	}
	return null;
}

/**
Deletes a row from the table model.  <b>Note:</b> When deleting multiple rows,
deleting from the last row to the first row is the easiest way.  Otherwise, 
if row X is deleted, all the rows X+1, X+2, ... X+N will shift down one and
need to be referenced by X, X+1, ... X+N-1.  <p>
So to delete rows 5, 6, and 7 in a table, either of these pieces of code
will work:<br><pre>
	worksheet.deleteRow(7);
	worksheet.deleteRow(6);
	worksheet.deleteRow(5);

        (or)

	worksheet.deleteRow(5);
	worksheet.deleteRow(5);
	worksheet.deleteRow(5);
</pre>
@param row the row to delete from the table model.  Rows are numbered starting at 0.
*/
public void deleteRow(int row) {
	__lastRowSelected = -1;
	((JWorksheet_AbstractTableModel)getModel()).deleteRow(row);
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();	

	if (__selectionMode == __EXCEL_SELECTION) {
		int rows = getRowCount();
		int cols = getColumnCount();
		setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);			
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(rows, cols);
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}

		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);
	}	
	
	if (__listRowHeader != null) {
		adjustListRowHeaderSize(__ROW_DELETED);
	}	
	notifyAllWorksheetListeners(__ROW_DELETED, row);
	adjustCellAttributesAndText(__ROW_DELETED, row);
}

/**
Deletes all the rows listed in the integer array.
@param rows integer array for which each value in it is the number of a row
to delete.  This array should be sorted in ascending value (i.e., array element
X is a lower number than array element X+1) and cannot be null.
*/
public void deleteRows(int[] rows) {
	for (int i = (rows.length - 1); i >= 0; i--) {
		deleteRow(rows[i]);
	}
}

/**
Deselects all selected cells.
*/
public void deselectAll() {
	notifyAllWorksheetListeners(__DESELECT_ALL, PRE_SELECTION_CHANGE);
	if (__selectionMode == __EXCEL_SELECTION) {
		((JWorksheet_RowSelectionModel)getSelectionModel()).clearSelection();
		refresh();
	}
	else {
		super.clearSelection();
	}
	notifyAllWorksheetListeners(__DESELECT_ALL, POST_SELECTION_CHANGE);
}

/**
Deselects the specified row, leaving the other row selections alone.  If the
row isn't currently-selected, nothing will happen.
@param row the row to deselect.
*/
public void deselectRow(int row) {
	if (__selectionMode == __EXCEL_SELECTION) {	
		int[] selectedRows = getSelectedRows();
		deselectAll();
		for (int i = 0; i < selectedRows.length; i++) {
			if (selectedRows[i] != row) {
				selectRow(selectedRows[i], false);
			}
		}
	}
	else {
		((DefaultListSelectionModel)getSelectionModel()).removeSelectionInterval(row, row);
	}
}

/**
Used by calculateColumnWidths to determine a column's name, as it should fit within certain constraints.
@param the name of the column.
@param minWidth the minimum width (in pixels) that the column name should be
tailored to fit.  Only used if the single largest token in the column name is smaller than the minimum width.
@param g the Graphics used to determine the width of Strings in certain fonts.
*/
private String determineName(String name, int minWidth, Graphics g) {
	// because commas should be retained in the final column name, pad
	// them out to all be "comma-space" ...
	String temp = StringUtil.replaceString(name, ",", ", ");
	// ... and then split the string based on newlines and spaces.
	List<String> v = StringUtil.breakStringList(temp, " \n", StringUtil.DELIM_SKIP_BLANKS);	
		
	int[] sizes = new int[v.size()];
	String[] strings = new String[sizes.length];		
	FontMetrics fh = g.getFontMetrics(
		new Font(__columnHeaderFontName, __columnHeaderFontStyle, __columnHeaderFontSize));	

	// go through all the strings that were broken out and determine 
	// the size each will take up in pixels when drawn on the screen
	for (int i = 0; i < sizes.length; i++) {
		strings[i] = v.get(i).trim();
		sizes[i] = fh.stringWidth(strings[i]);
	}

	// determine what the largest token is
	int biggest = -1;
	int max = 0;
	for (int i = 0; i < sizes.length; i++) {
		if (sizes[i] > max) {
			biggest = i;
			max = sizes[i];
		}
	}

	// if the largest string is still less than what the minimum width
	// of the column is, create the column name and force it to pad out to the minimum width.
	if ( (biggest >= 0) && (sizes[biggest] < minWidth) ) {
		return determineNameHelper(name, minWidth, g);
	}
	// otherwise ...
	else {
		String fullName = "";
	
		// if the largest token does not appear at the beginning of
		// the string, gather all the tokens that appear before it 
		// and create a String that will be no larger than the largest token
		if (biggest > 0) {
			String pre = "";
			for (int i = 0; i < biggest; i++) {
				pre += strings[i] + " ";
			}
			fullName += determineNameHelper(pre, sizes[biggest], g) + "\n";
		}
	
        if ( biggest >= 0 ) {
            fullName += strings[biggest];
        }
	
		// if the largest token does not appear at the end of the string, gather all the rest of the tokens
        // and create a String that will be no larger than the longest token
		if ( (biggest >= 0) && (biggest < (sizes.length - 1)) ) {
			fullName += "\n";
			String post = "";
			for (int i = biggest + 1; i < sizes.length; i++) {
				post += strings[i] + " ";
			}
			fullName += determineNameHelper(post, sizes[biggest], g);
		}
	
		return fullName;
	}	
}

/**
A helper method for determineName which takes a column name and constrains it
to fit within the bounds of the width passed in the method.  It does this by 
separating out words into separate lines, to return a multiple-line
column name.  Words are separated by commas, spaces and newlines.
@param name the column name to constrain to fit certain proportions.
@param maxWidth the point at which text should be wrapped to a new line.
@param g the Graphics context to use for determining how wide certain Strings are.
@return the column name with newlines that will fit in the desired space.
*/
private String determineNameHelper(String name, int maxWidth, Graphics g) {
	// because commas should be retained in the final column name, pad
	// them out to all be "comma-space" ...
	String temp = StringUtil.replaceString(name, ",", ", ");
	// ... and then split the string based on newlines and spaces.
	List v = StringUtil.breakStringList(temp, " \n", StringUtil.DELIM_SKIP_BLANKS);
	FontMetrics fh = g.getFontMetrics(
		new Font(__columnHeaderFontName, __columnHeaderFontStyle, __columnHeaderFontSize));

	// determine the sizes of all the split-out tokens
	int[] sizes = new int[v.size()];
	String[] strings = new String[sizes.length];
	for (int i = 0; i < sizes.length; i++) {
		strings[i] = ((String)v.get(i)).trim();
		sizes[i] = fh.stringWidth(strings[i]);
	}

	List lines = new Vector();
	boolean done = false;
	boolean invalid = false;
	int curr = 0;
	int max = sizes.length - 1;
	String s = null;
	int size = 0;

	while (!done) {
		s = "";
		// if on the very last one (so the previous string was added already), or the size of the current
		// one is too big, just add it straight to the list and set to done
		if (sizes[curr] > maxWidth || curr == max) {
			lines.add(strings[curr]);
			curr++;
			if (curr > max) {
				done = true;
			}			
		}
		else {
			invalid = false;
			// The curr string is at least a valid size.  Try adding the next ones on until the size is 
			// too large for the width.  Guaranteed to have at least one more after curr.
			s = strings[curr];
			size = sizes[curr];
			curr++;
			while (!invalid) {
				// If adding the next one would result in a string too long, set the loop to invalid.
				// wait for another time through the main loop.  
				if ((size + sizes[curr] + 1) > maxWidth) {
					invalid = true;
				}
				// Otherwise, append the string and set up for another loop through this one.
				else {
					size += sizes[curr] + 1;
					s += " " + strings[curr];
					curr++;
				}	

				if (curr > max) {
					invalid = true;
					done = true;
				}
			}
			lines.add(s);
		}
	}

	// Concatenate all the strings back together, putting in newlines as appropriate
	s = "";
	size = lines.size();
	for (int i = 0; i < size - 1; i++) {
		s += ((String)lines.get(i)).trim() + "\n";
	}
	s += ((String)lines.get(size - 1)).trim();

	return s;
}

/**
Turns on the row header.  This method should probably never be called by 
programmers, as they should rely on the Worksheet to do this programmatically
when the property is set to use row headers.
*/
protected void enableRowHeader() {
	Container p = getParent();
	if (p instanceof JViewport) {
		Container gp = p.getParent();
		if (gp instanceof JScrollPane) {			
			List v = new Vector();
			JScrollPane jsp = (JScrollPane)gp;
			int rows = getRowCount();
			if (__incrementRowNumbers) {
				for (int i = 0; i < rows; i++) {
					v.add("" + (i + __firstRowNum));
				}
			}
			else {
				for (int i = 0; i < rows; i++) {
					v.add("" + (__firstRowNum - i));
				}
			}
			__listRowHeader = new SimpleJList(v);
			__listRowHeader.addKeyListener(this);
			__listRowHeader.setBackground(jsp.getBackground());
			__listRowHeader.setFixedCellWidth(50);
			__listRowHeader.setFixedCellHeight(getRowHeight());
			Font font = new Font(__rowHeaderFontName, __rowHeaderFontStyle, __rowHeaderFontSize);
			__listRowHeader.setCellRenderer( new JWorksheet_RowHeader(this, font, __rowHeaderColor));
			jsp.setRowHeaderView(__listRowHeader);
			__listRowHeader.addMouseListener(this);
			__worksheetRowHeader = null;
		}
	}
}

/**
Turns on row headers using a worksheet as the row header.  Use in conjunction
with the row selection model partner, thusly:<p><tt>
	worksheet1.enableRowHeader(headerWorksheet, cols);
	headerWorksheet.setRowSelectionModelPartner(
		worksheet1.getRowSelectionModel());
</tt>
@param worksheet the worksheet to use as the row header.
@param cols the Columns to use from the worksheet.
*/
public void enableRowHeader(JWorksheet worksheet, int[] cols) {
	Container p = getParent();
	if (p instanceof JViewport) {
		Container gp = p.getParent();
		if (gp instanceof JScrollPane) {			
			JScrollPane jsp = (JScrollPane)gp;
			
			__worksheetRowHeader = worksheet;
			jsp.setRowHeaderView(__worksheetRowHeader);
			__worksheetRowHeader.addMouseListener(this);
			__listRowHeader = null;
		}
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__columnRemoved = null;
	__columnHeaderColor = null;
	__rowHeaderColor = null;
	__cellFont = null;
	__altTextCols = null;
	__altTextRows = null;
	__attrCols = null;
	__attrRows = null;
	__columnAlignments = null;
	__hourglassJDialog = null;
	__hourglassJFrame = null;
	__cancelMenuItem = null;
	__copyMenuItem = null;
	__copyHeaderMenuItem = null;
	__pasteMenuItem = null;
	__mainPopup = null;
	__popup = null;
	__columnHeaderView = null;
	__worksheetRowHeader = null;
	IOUtil.nullArray(__cellAttrs);
	__rowCountColumnAttributes = null;
	__copyPasteAdapter = null;
	__defaultCellRenderer = null;
	__hcr = null;
	__partner = null;
	__listRowHeader = null;
	IOUtil.nullArray(__altText);
	IOUtil.nullArray(__columnNames);
	__cellFontName = null;
	__columnHeaderFontName = null;
	__rowHeaderFontName = null;
	__worksheetListeners = null;

	super.finalize();
}

/**
Finds the first row containing the specified value in the specified column
@param value the value to match
@param absoluteColumn the <b>absolute</b> column number to search.  Columns are 
numbered starting at 0, and 0 is usually the row count column.
@param startingRow the row to start searching from
@param flags a bit-mask of flags specifying how to search
@return the index of the row containing the value, or -1 if not found (also
-1 if the column class is not Integer).
*/
public int find(int value, int absoluteColumn, int startingRow, int flags) {	
	if (flags == 0) {
		flags = FIND_EQUAL_TO;
	}
	Class c = getModel().getColumnClass(absoluteColumn);
	// make sure this column holds integers
	if (!(c == Integer.class)) {
		return -1;
	}

	boolean wrap = false;
	if ((flags & FIND_WRAPAROUND) == FIND_WRAPAROUND) {
		wrap = true;
	}	
	
	// check if the starting row is out of bounds
	if (startingRow < 0) {
	    	return -1;
	}
	if (startingRow > (getRowCount() - 1)) {
		if (wrap) {
			startingRow = 0;
		}
	}

	// check if the column is out of bounds
	if ((absoluteColumn < 0) || (absoluteColumn > (__columnNames.length - 1))) {
	    	return -1;
	}

	int visibleColumn = getVisibleColumn(absoluteColumn);

	int endingRow = getRowCount();
	boolean reverse = false;

	if ((flags & FIND_REVERSE) == FIND_REVERSE) {
		reverse = true;
		endingRow = 0;
	}
	boolean equalTo = false;
	if ((flags & FIND_EQUAL_TO) == FIND_EQUAL_TO) {
		equalTo = true;
	}
	boolean lessThan = false;
	if ((flags & FIND_LESS_THAN) == FIND_LESS_THAN) {
		lessThan = true;
	}
	boolean greaterThan = false;
	if ((flags & FIND_GREATER_THAN) == FIND_GREATER_THAN) {
		greaterThan = true;
	}

	boolean done = false;
	int rowVal;
	int row = startingRow;
	while (!done) {
		rowVal = ((Integer)((getModel().getValueAt(row, visibleColumn)))).intValue();
	
		if (equalTo) {
			if (value == rowVal) {
				return row;
			}
		}
		if (lessThan) {
			if (value < rowVal) {
				return row;
			}
		}
		if (greaterThan) {
			if (value > rowVal) {
				return row;
			}
		}

		if (reverse) {
			row--;
			if (row < endingRow) {
				if (wrap) {
					wrap = false;
					row = getRowCount() - 1;	
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
Finds the first row containing the specified value in the specified column
@param value the value to match
@param absoluteColumn the <b>absolute</b> column number to search.  Columns are 
numbered starting at 0, and 0 is usually the row count column.
@param startingRow the row to start searching from
@param flags a bit-mask of flags specifying how to search
@return the index of the row containing the value, or -1 if not found (also
-1 if the column class is not Double).
*/
public int find(double value, int absoluteColumn, int startingRow, int flags) {	
	if (flags == 0) {
		flags = FIND_EQUAL_TO;
	}

	Class c = getModel().getColumnClass(absoluteColumn);
	// make sure this absoluteColumn holds integers
	if (!(c == Double.class)) {
		return -1;
	}

	boolean wrap = false;
	if ((flags & FIND_WRAPAROUND) == FIND_WRAPAROUND) {
		wrap = true;
	}	
	
	// check if the starting row is out of bounds
	if (startingRow < 0) {
	    	return -1;
	}
	if (startingRow > (getRowCount() - 1)) {
		if (wrap) {
			startingRow = 0;
		}
	}

	// check if the column is out of bounds
	if ((absoluteColumn < 0) || (absoluteColumn > (__columnNames.length - 1))) {
	    	return -1;
	}

	int visibleColumn = getVisibleColumn(absoluteColumn);

	int endingRow = getRowCount();
	boolean reverse = false;

	if ((flags & FIND_REVERSE) == FIND_REVERSE) {
		reverse = true;
		endingRow = 0;
	}
	boolean equalTo = false;
	if ((flags & FIND_EQUAL_TO) == FIND_EQUAL_TO) {
		equalTo = true;
	}
	boolean lessThan = false;
	if ((flags & FIND_LESS_THAN) == FIND_LESS_THAN) {
		lessThan = true;
	}
	boolean greaterThan = false;
	if ((flags & FIND_GREATER_THAN) == FIND_GREATER_THAN) {
		greaterThan = true;
	}
	
	boolean done = false;
	double rowVal;
	int row = startingRow;
	while (!done) {
		rowVal = ((Double)((getModel().getValueAt(row, visibleColumn)))).doubleValue();
	
		if (equalTo) {
			if (value == rowVal) {
				return row;
			}
		}
		if (lessThan) {
			if (value < rowVal) {
				return row;
			}
		}
		if (greaterThan) {
			if (value > rowVal) {
				return row;
			}
		}

		if (reverse) {
			row--;
			if (row < endingRow) {
				if (wrap) {
					wrap = false;
					row = getRowCount() - 1;	
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
Finds the first row containing the specified value in the specified column
@param value the value to match
@param absoluteColumn the <b>absolute</b> column number to search.  Columns are 
numbered starting at 0, and 0 is usually the row count column.
@param startingRow the row to start searching from
@param flags a bit-mask of flags specifying how to search
@return the index of the row containing the value, or -1 if not found (also
-1 if the column class is not Date).
*/
public int find(Date value, int absoluteColumn, int startingRow, int flags) {	
	if (flags == 0) {
		flags = FIND_EQUAL_TO;
	}

	Class c = getModel().getColumnClass(absoluteColumn);
	// make sure this column holds integers
	if (!(c == Date.class)) {
		return -1;
	}

	boolean wrap = false;
	if ((flags & FIND_WRAPAROUND) == FIND_WRAPAROUND) {
		wrap = true;
	}	
	
	// check if the starting row is out of bounds
	if (startingRow < 0) {
	    	return -1;
	}
	if (startingRow > (getRowCount() - 1)) {
		if (wrap) {
			startingRow = 0;
		}
	}

	// check if the column is out of bounds
	if ((absoluteColumn < 0) ||
	    (absoluteColumn > (__columnNames.length - 1))) {
	    	return -1;
	}

	int visibleColumn = getVisibleColumn(absoluteColumn);

	int endingRow = getRowCount();
	boolean reverse = false;

	if ((flags & FIND_REVERSE) == FIND_REVERSE) {
		reverse = true;
		endingRow = 0;
	}
	boolean equalTo = false;
	if ((flags & FIND_EQUAL_TO) == FIND_EQUAL_TO) {
		equalTo = true;
	}
	boolean lessThan = false;
	if ((flags & FIND_LESS_THAN) == FIND_LESS_THAN) {
		lessThan = true;
	}
	boolean greaterThan = false;
	if ((flags & FIND_GREATER_THAN) == FIND_GREATER_THAN) {
		greaterThan = true;
	}
	
	boolean done = false;
	Date rowVal;
	int row = startingRow;
	int result;
	while (!done) {
		rowVal = ((Date)((getModel().getValueAt(row, visibleColumn))));

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
					row = getRowCount() - 1;	
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
Finds the first row containing the specified object.  This method can only
be used with worksheets that store a single object in a single row.  The find
uses '<tt>.equals</tt> to compare the objects to see if they match.
@param o the object for which to search.  Null can be passed in.
@param startingRow the row to start searching from.
@param flags a bit-mask of flags specifying how to search
@return the index of the row containing the object, or -1 if not found.
*/
public int find(Object o, int startingRow, int flags) {	
	boolean wrap = false;
	if ((flags & FIND_WRAPAROUND) == FIND_WRAPAROUND) {
		wrap = true;
	}	
	
	// check if the starting row is out of bounds
	if (startingRow < 0) {
	    	return -1;
	}
	if (startingRow > (getRowCount() - 1)) {
		if (wrap) {
			startingRow = 0;
		}
	}

	int endingRow = getRowCount();
	boolean reverse = false;

	if ((flags & FIND_REVERSE) == FIND_REVERSE) {
		reverse = true;
		endingRow = 0;
	}

	boolean done = false;
	Object rowObj;
	int row = startingRow;
	while (!done) {
		rowObj = getRowData(row);
	
		if (o == null) {
			if (rowObj == null) {
				return row;
			}
		}
		else {
			if (o.equals(rowObj)) {
				return row;
			}
		}

		if (reverse) {
			row--;
			if (row < endingRow) {
				if (wrap) {
					wrap = false;
					row = getRowCount() - 1;	
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
Finds the first row containing the specified value in the specified column
@param findValue the value to match
@param absoluteColumn the <b>absolute</b> column number to search.  Columns are 
numbered starting at 0, and 0 is usually the row count column.
@param startingRow the row to start searching from
@param flags a bit-mask of flags specifying how to search
@return the index of the row containing the value, or -1 if not found (also
-1 if the column class is not String).
*/
public int find(String findValue, int absoluteColumn, int startingRow, int flags) {	
	Class c = getModel().getColumnClass(absoluteColumn);
	// make sure this column holds integers
	if (!(c == String.class)) {
		return -1;
	}

	boolean wrap = false;
	if ((flags & FIND_WRAPAROUND) == FIND_WRAPAROUND) {
		wrap = true;
	}	
	
	// check if the starting row is out of bounds
	if (startingRow < 0) {
	    	return -1;
	}
	if (startingRow > (getRowCount() - 1)) {
		if (wrap) {
			startingRow = 0;
		}
	}

	// check if the column is out of bounds
	if ((absoluteColumn < 0) || (absoluteColumn > (__columnNames.length - 1))) {
	    	return -1;
	}

	String value = findValue.trim();

	int endingRow = getRowCount();
	boolean done = false;
	String rowVal;
	int row = startingRow;
	int result;

	boolean reverse = false;
	if ((flags & FIND_REVERSE) == FIND_REVERSE) {
		reverse = true;
		endingRow = 0;
	}
	boolean caseSensitive = true;
	if ((flags & FIND_CASE_INSENSITIVE) == FIND_CASE_INSENSITIVE) {
		caseSensitive = false;
	}
	boolean contains = false;
	if ((flags & FIND_CONTAINS) == FIND_CONTAINS) {
		contains = true;
	}
	boolean equals = false;
	if ((flags & FIND_EQUAL_TO) == FIND_EQUAL_TO) {
		equals = true;
	}
	boolean startsWith = false;
	if ((flags & FIND_STARTS_WITH) == FIND_STARTS_WITH) {
		startsWith = true;
	}
	boolean endsWith = false;
	if ((flags & FIND_ENDS_WITH) == FIND_ENDS_WITH) {
		endsWith = true;
	}

	while (!done) {
		rowVal =((String)((getModel().getValueAt(row, absoluteColumn)))).trim();

		if (equals) {
			if (caseSensitive) {
				if (rowVal.equals(value)) {
					return row;
				}
			}
			else {
				if (rowVal.equalsIgnoreCase(value)) {
					return row;
				}
			}
		}
		
		if (contains) {
			if (caseSensitive) {
				result = rowVal.indexOf(value);
			}
			else {
				result = StringUtil.indexOfIgnoreCase( rowVal, value, 0);
			}

			if (result != -1) {
				return row;
			}
		}

		if (startsWith) {
			if (caseSensitive) {
				if (rowVal.startsWith(value)) {
					return row;
				}			
			}
			else {
				if (StringUtil.startsWithIgnoreCase( rowVal, value)) {
					return row;
				}			
			}
		}

		if (endsWith) {
			if (caseSensitive) {
				if (rowVal.endsWith(value)) {
					return row;
				}			
			}
			else {
				if (StringUtil.endsWithIgnoreCase( rowVal, value)) {
					return row;
				}			
			}
		}

		if (reverse) {
			row--;
			if (row < endingRow) {
				if (wrap) {
					wrap = false;
					row = getRowCount() - 1;	
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
			if (row >= endingRow) {
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
Returns the <b>absolute</b> column number (i.e., it includes the column 
numbers that are hidden) for a <b>visible</b> column number.<p>
For example, if a table has 5 columns, some of which are not visible, the 
absolute and visible column numbers are as shown:<p>
<pre>
[0] - visible     - 0
[1] - visible     - 1
[2] - not visible - n/a
[3] - not visible - n/a
[4] - visible     - 2
</pre>
The <b>absolute</b> column numbers are listed on the left-hand side.  The
<b>visible</b> column numbers are listed on the right-hand side.<p>
@param visibleColumn the <b>visible</b> column number for which to return 
the <b>absolute</b> column number
@return the <b>absolute</b> column number from the <b>visible</b> column number
@see #getVisibleColumn(int)
*/
public int getAbsoluteColumn(int visibleColumn) {
	int visHit = -1;
	if (__columnRemoved == null) {
		return visibleColumn;
	}
	int size = __columnRemoved.length;
	
	for (int i = 0; i < size; i++) {
		if (__columnRemoved[i] == false) {
			visHit++;
		}
		if (visHit == visibleColumn) {
			return i;
		}
	}
	return -1;
}

/**
Returns the total count of all columns in the worksheet, not just those that are visible.
@return the total count of all columns in the worksheet, not just those that are visible.
*/
public int getAbsoluteColumnCount() {
	return __columnRemoved.length;
}

/**
Returns all the data objects in the table as a single Vector.  Use only 
if the table model for the worksheet stores each row as a separate data object.
@return a list of all data objects in the table.
*/
public List getAllData() {
	if (!(getModel() instanceof JWorksheet_AbstractTableModel)) {	
		return getRowData(0, getRowCount() - 1);
	}
	else {
		return ((JWorksheet_AbstractTableModel)getModel()).getData();
	}
}

/**
Finds alternate text for the specified cell in the alternate text arrays and returns the text.
@param row the row of the cell.  Rows are numbered starting at 0.
@param absoluteColumn the <b>absolute</b> column of the cell.  Columns are 
numbered starting at 0, and column 0 is usually the row count column.
@return the alternate text of the cell.
*/
public String getCellAlternateText(int row, 
int absoluteColumn) {
	if (__altTextCount == 0) {
		return null;
	}

	int visCol = getVisibleColumn(absoluteColumn);
	for (int i = 0; i <= (__altTextCount - 1); i++) {
		if (__altTextRows[i] == row) {
			if (__altTextCols[i] == visCol) {
				return __altText[i];
			}
		}
	}
	return null;
}

/**
Returns the cell that is located at a mouse click on the table.  The cell is
represented as a two-element integer array.  Array element 0 contains the 
row number (or -1 if no rows were clicked on).  Array element 1 contains the
column number (or -1 if no columns were clicked on).
@return the cell that is located at a mouse click on the table.
*/
public int[] getCellAtClick(MouseEvent event) {
	int[] cell = new int[2];

	cell[0] = rowAtPoint(event.getPoint());
	cell[1] = columnAtPoint(event.getPoint());
	
	return cell;
}

/**
Finds cell attributes for the specified cell in the cell attribute arrays and returns the attributes.
@param row the row of the cell.  Rows are numbered starting at 0.
@param absoluteColumn the <b>absolute</b> column of the cell.  Columns are 
numbered starting at 0, and column 0 is usually the row count column.
@return the cell attributes if the cell has them, or null.
*/
public JWorksheet_CellAttributes getCellAttributes(int row, 
int absoluteColumn) {
	if (__attrCount == 0) {
		return null;
	}
	int visCol = getVisibleColumn(absoluteColumn);
	for (int i = 0; i <= (__attrCount - 1); i++) {
		if (__attrRows[i] == row) {
			if (__attrCols[i] == visCol) {
				return __cellAttrs[i];
			}
		}
	}
	return null;
}

/**
Returns the Font in which to render worksheet cells.  While individual cell
attributes can be used to change the font in different cells, getCellFont()
and setCellFont() are quicker for changing and returning the font used 
everywhere in the table where a specific cell font attribute has not been set.
@return the Font in which to render a given cell.
*/
public Font getCellFont() {
	return __cellFont;
}

/**
Returns the cell renderer being used by the worksheet.
@return the cell renderer being used by the worksheet.
*/
public JWorksheet_DefaultTableCellRenderer getCellRenderer() {
	return __defaultCellRenderer;
}

/**
Returns the list of values stored in a cell-specific JComboBox.
@param row the row of the cell.
@param absoluteColumn the <b>absolute</b> column of the cell.
@return the list of values stored in a cell-specific JComboBox, or null
if the cell does not use a combo box.
*/
public List getCellSpecificJComboBoxValues(int row, int absoluteColumn) {
	TableColumn col = getColumnModel().getColumn(getVisibleColumn(absoluteColumn));
	TableCellEditor editor = col.getCellEditor();

	if (editor == null) {
		return null;
	}
	
	return ((JWorksheet_JComboBoxCellEditor)editor).getJComboBoxModel(row);
}

/**
Returns the user-specified alignment for a column.
@param absoluteColumn the <b>absolute</b> column for which to return the alignment.
@return the alignment that has been set with setColumnAlignment(), or DEFAULT
if the column has not had an alignment set yet.
*/
public int getColumnAlignment(int absoluteColumn) {
	if (__columnAlignments == null) {
		return LEFT;
	}
	return __columnAlignments[absoluteColumn];
}

/**
Returns the class of data stored in the table at the specified column.
@param absoluteColumn the <b>absolute</b> column for which to return the class.
@return the class of data stored in the table at the specified column.  Compare
to other classes with code like:
<pre>
if (getColumnClass(0) == Double.class) { ... }
if (getColumnClass(col) != String.class) { ... }
</pre>
*/
public Class getColumnClass(int absoluteColumn) {
	return getModel().getColumnClass(absoluteColumn);
}

/**
Returns the format for the data in the specified column.  TOD -- only 
works for AbstractExcelCellRenderers
@param absoluteColumn the <b>absolute</b> column for which to return the column format.
@return the format for the data in the specified column.
*/
public String getColumnFormat(int absoluteColumn) {
	JWorksheet_AbstractExcelCellRenderer renderer = 
		(JWorksheet_AbstractExcelCellRenderer)getCellRenderer();
	return renderer.getFormat(absoluteColumn);
}

/**
Returns the name of the specified column.  Overrides the original JTable
code in order to provide documentation for the column to provide.
@param visibleColumn the <b>visible</b> column for which to return the name.
@return the name of the specified column.
*/
public String getColumnName(int visibleColumn) {
	return getColumnName(visibleColumn, false);
}

/**
Returns the name of the specified column.  
@param visibleColumn the <b>visible</b> column for which to return the name.
@param convertNewlines if true, then any newlines in the column name (which
would be used in tables that have multiple-line headers) will be stripped out
and replaced with spaces.  This is useful for getting single-line versions of
multiple-line column names.  If false, the standard column name (with newlines,
if they are in the name) will be returned.
@return the name of the specified column.
*/
public String getColumnName(int visibleColumn, boolean convertNewlines) {
	String s = super.getColumnName(visibleColumn);

	if (!convertNewlines) {
		return s;
	}
	else {
		return convertColumnName(s);
	}
}

/**
Determines the width of the largest token in the column name in pixels.  This
is the width at which the other tokens in the column name must be wrapped to
other lines in order to fit the space allotted.
@param name the column name.
@param g the Graphics context to use for determining the width of certain 
Strings in pixels.
*/
private int getColumnNameFitSize(String name, Graphics g) {
	List<String> v = StringUtil.breakStringList(name, "\n", 0);
	FontMetrics fh = g.getFontMetrics(
		new Font(__columnHeaderFontName, __columnHeaderFontStyle, __columnHeaderFontSize));

	int size = v.size();
	int maxSize = 0;
	int width = 0;
	for (int i = 0; i < size; i++) {
		width = fh.stringWidth((String)v.get(i));
		if (width > maxSize) {
			maxSize = width;
		}
	}
	return maxSize + 15;	
}

/**
For a column with the given name, returns the <i>visible</i> column number. 
If the table contains more than one column with the same name, the number of the first one will be returned.
@return the <i>visible</i> column number.
*/
public int getColumnNumber(String columnName) {
	String name = null;

	for (int i = 0; i < getVisibleColumnCount(); i++) {
		name = getColumnName(i);
		if (name.equals(columnName)) {
			return i;
		}
		if (convertColumnName(name).equals(columnName)) {
			return i;
		}
	}
	return -1;
}

/**
Returns the prefix being stored before column names, which depends on the kind
of column prefix that was set up.
@param columnNum the <b>relative</b> column for which to return the prefix.
*/
public String getColumnPrefix(int columnNum) {
	if (__columnNumbering == __NUMBERING_NONE) {
		return "";
	}
	else if (__columnNumbering == __NUMBERING_EXCEL) {
		return getExcelNumber(columnNum);		
	}
	else if (__columnNumbering == __NUMBERING_0) {
		return "" + columnNum + " - ";
	}
	else if (__columnNumbering == __NUMBERING_1) {
		return "" + (columnNum + 1) + " - ";
	}	
	return "";
}

/**
Gets a value from the table at the specified row and column, using a consecutive read policy.<p>
Some table models may store data (e.g., time series dates), in which 
data values are calculated based on the previous row's data.  In this case,
this method can be used and they would know that a consecutive read of the table
data will be done, and that everytime a call is made to getValueAt() in the 
table model, the row parameter is guaranteed to be the same row as the last 
time getValueAt() was called (if the column is different), or 1 more than
the previous row.
*/
public Object getConsecutiveValueAt(int row, int visibleColumn) {
	return((JWorksheet_AbstractTableModel)getModel()).getConsecutiveValueAt(row, visibleColumn);
}

/**
Returns the <b>visible</b> column of the cell being edited, or -1 if no cell is being edited.
@return the <b>visible</b> column of the cell being edited, or -1 if no cell is being edited.
*/
public int getEditColumn() {
	return __editCol;
}

/**
Returns the row of the cell being edited, or -1 if no cell is being edited.
@return the row of the cell being edited, or -1 if no cell is being edited.
*/
public int getEditRow() {
	return __editRow;
}

/**
Returns the JDialog being used as the Hourglass display dialog.  Will never return null.
@return the JDialog being used as the Hourglass display dialog.
*/
public JDialog getHourglassJDialog() {
	return __hourglassJDialog;
}

/**
Returns the JFrame being used as the Hourglass display frame.  Will never return null.
@return the JFrame being used as the Hourglass display frame.
*/
public JFrame getHourglassJFrame() {
	return __hourglassJFrame;
}

/**
Returns the data stored in the last row of the worksheet.  Only works for
worksheetss whose table models stored a single data object per row.
@return the data object stored in the last row of the worksheet.
*/
public Object getLastRowData() {
	return getRowData(getRowCount() - 1);
}

/**
Returns the maximum width of the data in the specified column in the given rows, in pixels.
@param absoluteColumn the column in which to check the data.
@param rows the number of rows (from 0) to check the data in.  If greater 
than getRowCount() will be set equal to getRowCount().
@param g the Graphics context to use for determining font widths.
*/
private int getDataMaxWidth(int absoluteColumn, int rows, Graphics g) {
	String s = null;
	if (rows > getRowCount()) {
		rows = getRowCount();
	}
	FontMetrics fc = g.getFontMetrics( new Font(__cellFontName, __cellFontStyle, __cellFontSize));

	int widest = 0;
	int width = 0;
	int col = getVisibleColumn(absoluteColumn);
	
	for (int i = 0; i < rows; i++) {
		s = getValueAtAsString(i, col);
		width = fc.stringWidth(s);
		if (width > widest) {	
			widest = width;
		}
	}
	return widest + 15;
}

/**
Returns the Excel column heading that corresponds to the given column number.
@param columnNumber the number of the column (base 0).
@return the column header that would appear in an Excel worksheet.
*/
public static String getExcelNumber(int columnNumber) {
	int[] val = new int[2];
	if (columnNumber > 25) {
		val[0] = columnNumber / 26;
	}
	else {
		val[0] = 0;
	}
	val[1] = columnNumber % 26;

	String excel = "";

	int num = -1;
	for (int i = 0; i < 2; i++) {
		num = val[i];
		if (i == 0) {
			num--;
		}
		switch (num) {
			case -1:	excel += "";	break;
			case  0:	excel += "A";	break;
			case  1:	excel += "B";	break;
			case  2:	excel += "C";	break;
			case  3:	excel += "D";	break;
			case  4:	excel += "E";	break;
			case  5:	excel += "F";	break;
			case  6:	excel += "G";	break;
			case  7:	excel += "H";	break;
			case  8:	excel += "I";	break;
			case  9:	excel += "J";	break;
			case 10:	excel += "K";	break;
			case 11:	excel += "L";	break;
			case 12:	excel += "M";	break;
			case 13:	excel += "N";	break;
			case 14:	excel += "O";	break;
			case 15:	excel += "P";	break;
			case 16:	excel += "Q";	break;
			case 17:	excel += "R";	break;
			case 18:	excel += "S";	break;
			case 19:	excel += "T";	break;
			case 20:	excel += "U";	break;
			case 21:	excel += "V";	break;
			case 22:	excel += "W";	break;
			case 23:	excel += "X";	break;
			case 24:	excel += "Y";	break;
			case 25:	excel += "Z";	break;
		}
	}
	return excel;
}

/**
Returns whether the user can select an entire row by clicking on the first (0th)
column.  One click row selection is not possible if the worksheet was built
with the JWorksheet.SelectionMode property set to: SingleCellSelection,
SingleRowSelection, MultipleRowSelection, or MultipleDiscontinuousRowSelection.
@return whether the user can select an entire row by clicking on the first (0th) column.
*/
public boolean getOneClickRowSelection() {
	if (__selectionMode == __EXCEL_SELECTION) {
		return ((JWorksheet_RowSelectionModel)getSelectionModel()).getOneClickRowSelection();
	}
	else {
		return false;
	}
}

/**
Gets the original row number a row had, given its current position in the sorted rows.
@param sortedRow the sorted row number of the row.
@return the original, unsorted row number.
*/
public int getOriginalRowNumber(int sortedRow) {
	int[] sortedOrder = ((JWorksheet_AbstractTableModel)getModel())._sortOrder;
	if (sortedOrder == null) {
		return sortedRow;
	}
	if (sortedRow < 0 || sortedRow > sortedOrder.length) {
		return -1;
	}
	return sortedOrder[sortedRow];
}

/**
Returns the data element at the given row.  The data must be cast
to the proper data type; the table has no idea what it is.  Row 0 is the 
first row.   If the row is out of the range of rows in the table, or less than 0, null will be returned.
This only works with JWorksheet_AbstractRowTableModels.
@param row the row for which to return the data.  Rows are numbered starting at 0.
@return the object stored at the given row, or null if the row was invalid. 
Returns null if the table model is not derived from JWorksheet_AbstractRowTableModel.
*/
public Object getRowData(int row) {
	if (!(getModel() instanceof JWorksheet_AbstractRowTableModel)) {
		return null;
	}

	List v = getRowData(row, row);
	if (v != null && v.size() > 0) {
		return (getRowData(row, row)).get(0);
	}
	return null;
}

/**
Returns the data elements from the given rows.  Row 0 is the first row.  If
the range of rows goes out of range of the number of rows in the table, or 
goes less than 0, a null value will be returned for each row for which the row number is out of range.
This only works with JWorksheet_AbstractRowTableModels.
@param row1 the first row for which to return data.  Rows are numbered starting at 0.
@param row2 the last row for which to return data.  Rows are numbered starting at 0.
@return a Vector of the objects stored in the given rows.
Returns null if the table model is not derived from 
JWorksheet_AbstractRowTableModel, or if the range of row numbers was invalid.
*/
public List getRowData(int row1, int row2) {
	if (!(getModel() instanceof JWorksheet_AbstractRowTableModel)) {
		return null;
	}
	if (row1 == 0 && row2 == -1) {
		// special case -- getAllData called on an empty worksheet
		return new Vector();
	}

	if (row1 > row2) {
		row2 = row1;
		row1 = row2;
	}

	if (row1 < 0 || row2 < 0) {
		return null;
	}
	int size = getRowCount();
	if (row1 >= size || row2 >= size) {
		return null;
	}
		
	List v = new Vector();
	for (int i = row1; i <= row2; i++) {
		v.add(((JWorksheet_AbstractRowTableModel)getModel()).getRowData(i));
	}	
	return v;
}

/**
Returns the row data for the rows specified in the parameter array.
@param rows the integer array containing the row numbers for which to return
data.  Cannot be null.  Data will be returned in the order of the row numbers in this array.
*/
public List getRowData(int[] rows) {
	List v = new Vector();
	for (int i = 0; i < rows.length; i++) {
		v.add(getRowData(rows[i]));
	}
	return v;
}

/**
Returns the row selection model.
@return the row selection model.
*/
public JWorksheet_RowSelectionModel getRowSelectionModel() {
	return (JWorksheet_RowSelectionModel)getSelectionModel();
}

/**
Returns a list containing two integer arrays, the first of which contains all 
the rows of the selected cells, and the second of which contains the matching
columns for the rows in order to determine which cells are selected.
For a given cell I, the row of the cell is the first array's element at 
position I and the column of the cell is the second array's element at
position I.  The arrays are guaranteed to be non-null.  The Vector will never be null.
@return a list containing two integer arrays.
*/
public List<int []> getSelectedCells() {
	List<Integer> rows = new Vector<Integer>();
	List<Integer> cols = new Vector<Integer>();

	for (int i = 0; i < getRowCount(); i++) {
		for (int j = 0; j < getColumnCount(); j++) {
			if (isCellSelected(i, j)) {
				rows.add(new Integer(i));
				cols.add(new Integer(j));
			}
		}
	}

	int size = rows.size();

	int[] rowCells = new int[size];
	int[] colCells = new int[size];

	for (int i = 0; i < size; i++) {
		rowCells[i] = ((Integer)rows.get(i)).intValue();
		colCells[i] = ((Integer)cols.get(i)).intValue();
	}
	
	List<int []> v = new Vector<int []>();
	v.add(rowCells);
	v.add(colCells);
	return v;
}

/**
Returns the first <b>absolute</b> selected column number, or -1 if none 
are selected.  A column is considered to be selected if any of its cells have are selected.  
@return the first selected column number, or -1 if none are selected.
*/
public int getSelectedColumn() {
	if (__selectionMode == __EXCEL_SELECTION) {
		return getAbsoluteColumn( ((JWorksheet_RowSelectionModel)getSelectionModel()).getSelectedColumn());
	}
	else {	
		// necessary because of how the normal selection models have been mistreated by JWorksheet.
		for (int i = 0; i < getColumnCount(); i++) {
			for (int j = 0; j < getRowCount(); j++) {
				if (isCellSelected(j, i)) {
					return getAbsoluteColumn(i);
				}
			}
		}
		return -1;
	}
}

/**
Returns a count of the number of columns selected.  Columns are considered to
be selected if any of their cells are selected.
@return a count of the number of columns selected.
*/
public int getSelectedColumnCount() {
	return (getSelectedColumns()).length;
}

/**
Returns an integer array of the selected <b>absolute</b> column numbers.  
Columns are considered to be selected if any of their cells are selected.
@return an integer array of the selected <b>absolute</b> column numbers.  The
columns will be in order from the lowest selected column to the highest.
*/
public int[] getSelectedColumns() {
	if (__selectionMode == __EXCEL_SELECTION) {
		int[] selected = ((JWorksheet_RowSelectionModel)getSelectionModel()).getSelectedColumns();
		for (int i = 0; i < selected.length; i++) {
//			Message.printStatus(1, "", "" + selected[i] + " --> " + getAbsoluteColumn(selected[i]));
			selected[i] = getAbsoluteColumn(selected[i]);
		}
		return selected;
	}
	else {	
		int[] selectedCols = new int[getColumnCount()];
		int count = 0;
		// necessary because of how the normal selection models have been mistreated by JWorksheet.
		int rows = getRowCount();
		int cols = getColumnCount();
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (isCellSelected(j, i)) {
					selectedCols[count] = getAbsoluteColumn(i);
					count++;
					j = rows + 1;
				}
			}
		}

		int[] selected = new int[count];
		for (int i = 0; i < count; i++) {
			selected[i] = selectedCols[i];
		}
		return selected;
	}
}

/**
Returns an integer of the first row in the table that is selected or -1 if
none are selected.  Rows are considered to be selected if any of their cells are selected.
@return an integer of the first row in the table that is selected or -1 if none are selected.
*/
public int getSelectedRow() {
	if (__selectionMode == __EXCEL_SELECTION) {
		return ((JWorksheet_RowSelectionModel)getSelectionModel()).getSelectedRow();
	}
	else {	
		// necessary because of how the normal selection models have been mistreated by JWorksheet.
		for (int i = 0; i < getRowCount(); i++) {
			for (int j = 0; j < getColumnCount(); j++) {
				if (isCellSelected(i, j)) {
					return i;
				}
			}
		}
		return -1;	
	}	
}

/**
Returns a count of the number of rows in the worksheet that are selected.
Overrides the method in JTable so that it works correctly.  Rows are considered
to be selected if any of their cells are selected.
@return the number of rows in the worksheet that are selected.
*/
public int getSelectedRowCount() {
	return (getSelectedRows()).length;
}

/**
Returns an integer array of the rows in the table that have been selected.
Rows are considered to be selected if any of their cells are selected.
@return an integer array of the rows in the table that have been selected, guaranteed to be non-null but may
be zero length.  The array results will be in order from lowest row to highest row.
*/
public int[] getSelectedRows() {
	if (__selectionMode == __EXCEL_SELECTION) {
		return ((JWorksheet_RowSelectionModel)getSelectionModel()).getSelectedRows();
	}
	else {	
		int[] selectedRows = new int[getRowCount()];
		int count = 0;
		// necessary because of how the normal selection models have been mistreated by JWorksheet.
		int rows = getRowCount();
		int cols = getColumnCount();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (isCellSelected(i, j)) {
					selectedRows[count] = i;
					count++;
					j = cols + 1;
				}
			}
		}

		int[] selected = new int[count];
		for (int i = 0; i < count; i++) {
			selected[i] = selectedRows[i];
		}
		return selected;	
	}	
}

/**
Returns the sorted row number of a row in the JWorksheet, given its original unsorted row number.
@param unsortedRow the unsorted row number of a row in the worksheet.
@return the sorted row number of the row.  If the table is not sorted, the passed-in row is returned.
*/
public int getSortedRowNumber(int unsortedRow) {
	int[] sortedOrder = ((JWorksheet_AbstractTableModel)getModel())._sortOrder;
	if (sortedOrder == null) {
		return unsortedRow;
	}
	for (int i = 0; i < sortedOrder.length; i++) {	
		if (sortedOrder[i] == unsortedRow) {
			return i;
		}
	}
	return -1;
}

/**
For a number format (e.g., "%9d"), this returns an equivalent format that can
be used to parse the number as a string.
@param numberFormat the number format to process.
@return a string format code (e.g., "%9s") that can be used to read a value in as a String.
*/
public String getStringFormat(String numberFormat) {
	if (numberFormat == null) {
		return "%s";
	}
	try {
		String format = numberFormat.trim();
		int len = format.length();
		if (StringUtil.endsWithIgnoreCase(format, "d")) {
			format = format.substring(1, (len - 1));
			int n = StringUtil.atoi(format);
			return "%" + n + "." + n + "s";
		}
		else if (StringUtil.endsWithIgnoreCase(format, "f")) {
			format = format.substring(1, (len - 1));
			if (format.startsWith("#")) {
				format = format.substring(1);
			}
			int index = format.indexOf(".");
			if (index > -1) {
				format = format.substring(0, index);
			}
			int n = StringUtil.atoi(format);
			return "%" + n + "." + n + "s";
		}
		else if (StringUtil.endsWithIgnoreCase(format, "s")) {
			format = format.substring(1, (len - 1));
			return numberFormat;
		}
		else {
		 	return "%s";
		}
	}
	catch (Exception e) {
		String routine = "JWorksheet.getStringFormat()";
		Message.printWarning(3, routine, e);
		Message.printWarning(3, routine, "Could not parse format: '" + numberFormat + "'");
	}
	return "%s";
}

/**
Returns the table model being used by the worksheet.
@return the table model being used by the worksheet.
*/
public JWorksheet_AbstractTableModel getTableModel() {
	return (JWorksheet_AbstractTableModel)getModel();
}

/**
Returns the value at the specified position.  
@param row the row of the value to return.
@param column the <b>visible</b> column from which to return the value.
*/
public Object getValueAt(int row, int column) {
	return super.getValueAt(row, column);
}

/**
Returns the value at the specified position formatted as a String with the format stored in the table model.
@param row the row of the value to return.
@param column the <b>visible</b> column from which to return the value.
@return the value formatted as a String.
*/
public String getValueAtAsString(int row, int column) {
	// TODO (JTS - 2005-11-07) this could probably be sped up significantly if we weren't getting
	// the renderer every time for every cell
	JWorksheet_AbstractTableCellRenderer renderer = getCellRenderer();
	if (renderer instanceof JWorksheet_AbstractExcelCellRenderer) {
		return getValueAtAsString(row, column, 
			((JWorksheet_AbstractExcelCellRenderer)renderer).getFormat(getAbsoluteColumn(column)));
	}
	else {
		return "" + getValueAt(row, column);
	}
}

/**
Returns the value at the specified position formatted as a String with the specified format.
Return an empty string if null.
@param row the row of the value to return.
@param column the <b>visible</b> column from which to return the value.
@param format the format to use for formatting the value as a String.
@return the value in the position formatted as a String.
*/
public String getValueAtAsString(int row, int column, String format) {
	Object o = getValueAt(row, column);
	Class c = getColumnClass(getAbsoluteColumn(column));
	try {

	if ( o == null ) {
	    return "";
	}
	else if (c == Integer.class) {
		Integer I = (Integer)o;
		if (COPY_MISSING_AS_EMPTY_STRING && DMIUtil.isMissing(I.intValue())) {
			format = getStringFormat(format);
			return StringUtil.formatString("", format);
		}
		else {
			if (format != null) {
				return StringUtil.formatString(I.intValue(), format);
			}
			else {
				return "" + I.intValue();
			}
		}
	}
	else if (c == Double.class) {
		Double d = (Double)o;
		if (COPY_MISSING_AS_EMPTY_STRING && DMIUtil.isMissing(d.doubleValue())) {
			format = getStringFormat(format);
			return StringUtil.formatString("", format);
		}
		else {
			if (format != null) {
				return StringUtil.formatString(d.doubleValue(), format);
			}
			else {
				return "" + d.doubleValue();
			}
		}
	}
	else if (c == Date.class) {
		Date d = (Date)o;
		if (DMIUtil.isMissing(d)) {
			return "";
		}
		else {
			return "" + d;
		}
	}
	else if (c == String.class) {
		String s = (String)o;
		if (DMIUtil.isMissing(s)) {	
			return "";
		}
		else {
			return StringUtil.formatString(s, format);
		}
	}
	else if (c == Float.class) {
		Float F = (Float)o;
		if (COPY_MISSING_AS_EMPTY_STRING && DMIUtil.isMissing(F.floatValue())) {
			return "";
		}
		else {
			return "" + F;
		}
	}
	else {
		return "" + o;
	}
	}
	catch (Exception e) {
		String routine = "JWorksheet.getValueAtAsString()";
		Message.printWarning(3, routine, e);
		Message.printWarning(3, "", "getValueAsString(" + row + ", " 
			+ column + ", " + format + "): class(" + getAbsoluteColumn(column) + ": " + c + "  data: " 
			+ o + "  data class: " + o.getClass());
	}
	return "" + o;
}

/**
Translates the absolute column number (i.e., as used in the table model) to 
the visible column number (in case columns have been removed).
@param absoluteColumn the absolute column number.  Columns are numbered 
starting at 0, though column 0 is usually the row count column.
@return the number of the column on the screen, or -1 if not visible.
@see #getAbsoluteColumn(int)
*/
public int getVisibleColumn(int absoluteColumn) {
	int hit = -1;
	for (int i = 0; i < absoluteColumn + 1; i++) {
		if (__columnRemoved[i] == false) {
			hit++;
		}
	}
	return hit;
}

/**
Returns a count of the visible columns.
@return a count of the visible columns.
*/
public int getVisibleColumnCount() {
	int visColumnCount = 0;

	for (int i = 0; i < __columnRemoved.length; i++) {
		if (__columnRemoved[i] == false) {
			visColumnCount++;
		}
	}
	return visColumnCount;
}

/**
Returns the worksheet header.
@return the worksheet header.
*/
public JWorksheet_Header getWorksheetHeader() {
	return (JWorksheet_Header)getTableHeader();
}

/**
Initializes data members in the worksheet, but mostly sets up the 
specialized row and column selection models that allow selection to mimic
Excel rather than the limited selection model provided by JTable.
@param rows the number of rows in the worksheet.
@param cols the number of columns in the worksheet.
*/
private void initialize(int rows, int cols) {
	String routine = CLASS + ".initialize";
	setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	if (getTableHeader() != null) {
		getTableHeader().addMouseListener(this);

		// cannot simply call "setTableHeader(__header)" here 
		// because then the table columns are always UNresizable 
		// (no matter what) from this point on.
		getTableHeader().setColumnModel(getColumnModel());
	
		getTableHeader().setReorderingAllowed(false);		
		getTableHeader().setResizingAllowed(true);
	}

	if (__selectionMode == __EXCEL_SELECTION) {
		setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);			
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(rows, cols);
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}

		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);
	}
	else {
		if (__selectionMode == __SINGLE_CELL_SELECTION) {
			setSelectionMode((__selectionMode - 100));
			setCellSelectionEnabled(true);			
		}
		else {
			setSelectionMode(__selectionMode);
		}
	}

	if (__showPopup) {
		__popup = new JPopupMenu();
		JMenuItem mi = null;
		mi = new JMenuItem(__MENU_SORT_ASCENDING);
		mi.addActionListener(this);
		__popup.add(mi);
		mi = new JMenuItem(__MENU_SORT_DESCENDING);
		mi.addActionListener(this);	
		__popup.add(mi);
		__cancelMenuItem = new JMenuItem(__MENU_ORIGINAL_ORDER);
		__cancelMenuItem.addActionListener(this);
		__popup.add(__cancelMenuItem);
		__cancelMenuItem.setEnabled(false);
		if (getTableHeader() != null) {
			getTableHeader().addMouseListener(this);
		}
	}

	__hcr = new JWorksheet_HeaderCellRenderer(
		__columnHeaderFontName, __columnHeaderFontStyle, 
		__columnHeaderFontSize,	SwingConstants.CENTER, __columnHeaderColor);

	TableColumn tc = null;
	for (int i = 0; i < getColumnCount(); i++) {
		if (Message.isDebugOn) {
			Message.printDebug(10, routine, "Setting column header "
				+ "for column #" + i + ", '" + getColumnName(i) + "'");
		}
		tc = getColumnModel().getColumn(i);
//		tc = getColumn(getColumnName(i));
		tc.setHeaderRenderer(__hcr);
	}

	setMultipleLineHeaderEnabled(true);

	setOneClickRowSelection(__oneClickRowSelection);
}

/**
Inserts a new element to the table model, at the specified position.
@param o the object to add to the table model.
@param pos the position at which to insert the record.  If the position is
less than 0, nothing will be done.  If the position is greater than the number
of records in the table, the record will be added at the very end.
*/
public void insertRowAt(Object o, int pos) {
	__lastRowSelected = -1;
	String routine = CLASS + ".insertRowAt(Object, int)";
	
	if (pos < 0) {
		Message.printWarning(3, routine, "Attempting to insert at a negative position, not inserting.");
		return;
	}
	
	if (pos >= getRowCount()) {
		addRow(o);		
		return;
	}
	
	((JWorksheet_AbstractTableModel)getModel()).insertRowAt(o, pos);
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();	

	if (__selectionMode == __EXCEL_SELECTION) {
		int rows = getRowCount();
		int cols = getColumnCount();
		setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);			
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(rows, cols);
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}

		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);
	}

	if (__listRowHeader != null) {
		adjustListRowHeaderSize(__ROW_ADDED);
	}
	
	notifyAllWorksheetListeners(__ROW_ADDED, pos);
	adjustCellAttributesAndText(__ROW_ADDED, pos);
}

/**
Determines whether a cell is editable or not by checking cell attributes and
the normal JTable isCellEditable().  Overrides JTable.isCellEditable().  
This method first checks to see if the ell in question has any attributes assigned to it.  
If the cell has attributes and the value of 'editable' is set to false, the cell is returned as
uneditable (false).  If there are no attributes set on the cell, a call is made to the default
JTable isCelEditable() to return the value.
@param row the row of the cell in question.  Rows are numbered starting at 0.
@param visibleColumn the <b>visible</b> column of the cell in question.  
Columns are numbered starting at 0, though column 0 is usually the row count column.
@return whether the cell is editable or not
*/
public boolean isCellEditable(int row, int visibleColumn) {
	JWorksheet_CellAttributes ca = getCellAttributes(row, getAbsoluteColumn(visibleColumn));
	if (ca == null) {
		return super.isCellEditable(row, visibleColumn);
	}
	else {
		if (ca.editable == false) {
			return false;
		}
		else {
			return super.isCellEditable(row, visibleColumn);
		}
	}
}

/**
Checks to see whether a cell is selected.
@param row the row to check
@param col the <b>visible</b> column to check.
@return true if the cell is selected, false if not.
*/
public boolean isCellSelected(int row, int col) {
//	Message.printStatus(1, "", "Checking cell selected: " + row + ", " + col);
	return super.isCellSelected(row, col);
}

/**
Returns whether the worksheet is dirty.
@return whether the worksheet is dirty.
*/
public boolean isDirty() {
	return __dirty;
}

/**
Returns whether a cell in the worksheet is currently being edited.
@return whether a cell in the worksheet is currently being edited.
*/
public boolean isEditing() {
	if (getCellEditor() != null) {
		return true;
	}
	return false;
}

/**
Returns whether the table is empty or not.
@return true if the table is empty (has no rows), or false if it has rows.
*/
public boolean isEmpty() {
	if (getRowCount() > 0) {
		return false;
	}
	return true;
}

/**
Returns whether this worksheet's cells can be selected or not.
@return whether this worksheet's cells can be selected or not.
*/
public boolean isSelectable() {
	return __selectable;
}

/**
Returns whether the table is using the row headers that work similarly to the JTable column headers.
@return whether the table is using row headers.
*/
protected boolean isUsingRowHeaders() {
	return __useRowHeaders;
}

/**
Responds to key press events.  <br>
TODO (JTS - 2003-11-17) What's this doing? (JTS - 2004-01-20) Still no clue.
@param event the KeyEvent that happened.
*/
public void keyPressed(KeyEvent event) {
	/*
	TODO (JTS - 2004-11-22) commented out, see if anything misbehaves (I don't think we'll see
	any problems.

	// do nothing if a cell is being edited
	if (isEditing()) {
		return;
	}
	__isControlDown = false;
	__isShiftDown = false;

	// look for control-? events
	if (event.isControlDown()) {
		__isControlDown = true;
		int code = event.getKeyCode();
		if (code == event.VK_A) {
			selectAllRows();
		}
	}

	if (event.isShiftDown()) {
		__isShiftDown = true;
	}
	*/
}

/**

*/
public void keyReleased(KeyEvent event) {
	__isControlDown = false;
	__isShiftDown = false;

	if (event.isControlDown()) {
		__isControlDown = true;
	}
	if (event.isShiftDown()) {
		__isShiftDown = true;
	}
}

/**
Does nothing.
*/
public void keyTyped(KeyEvent event) {}

/**
Shows the popup menu if the mouse was pressed on the table header.
@param event the MouseEvent that occurred.
*/
private void maybeShowHeaderPopup(MouseEvent event) {
	if (__showPopup) {
		if (__popup.isPopupTrigger(event)) {
			__popupColumn = columnAtPoint(new Point(event.getX(), event.getY()));
			__popup.show(event.getComponent(), event.getX(), event.getY());
		}
	}
}

/**
Shows the popup menu, if it has been set and if the table was right-clicked on.
@param event the MouseEvent that happened.
*/
private void maybeShowPopup(MouseEvent event) {
	if (__mainPopup != null) {
		if (__mainPopup.isPopupTrigger(event)) {
			if (getSelectedRowCount() > 0) {
				__copyMenuItem.setEnabled(true);
				__copyHeaderMenuItem.setEnabled(true);
				__pasteMenuItem.setEnabled(true);
				__deselectAllMenuItem.setEnabled(true);
				__selectAllMenuItem.setEnabled(true);
			}
			else {
				__copyMenuItem.setEnabled(false);
				__copyHeaderMenuItem.setEnabled(false);
				__pasteMenuItem.setEnabled(false);
				__deselectAllMenuItem.setEnabled(false);
				__selectAllMenuItem.setEnabled(true);
			}
			__mainPopup.show(event.getComponent(), event.getX(), event.getY());
		}
	}
}

/**
Does nothing.
*/
public void mouseClicked(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseEntered(MouseEvent event) {}

/**
Does nothing.
*/
public void mouseExited(MouseEvent event) {}

/**
When a mouse key is pressed, if the header was clicked on and one click column
selection is turned on, selects all the values in the clicked-on column.
@param event the MouseEvent that occurred.
*/
public void mousePressed(MouseEvent event) {
	Component c = event.getComponent();

	// if the header was clicked on ...
	if (getTableHeader() != null && c == getTableHeader()) {
		if (__oneClickColumnSelection) {	
			int column = columnAtPoint(new Point(event.getX(), 0));
			if (column == -1) {
				return;
			}
			selectColumn(column);
		}
	}
	else if (c == __listRowHeader) {
		if (__oneClickRowSelection) {
			int row = rowAtPoint(new Point(0, event.getY()));
			if (row == -1) {
				return;
			}

			if ((!__isControlDown && !__isShiftDown) || (__lastRowSelected == -1)) {
				selectRow(row, true);
			}
			else {
				if (__isControlDown && !__isShiftDown) {
					if (rowIsSelected(row)) {
						deselectRow(row);
					}
					else {
						selectRow(row, false);
					}				
				}
				else {
					int low = (row < __lastRowSelected ? row : __lastRowSelected);
					int high = (row > __lastRowSelected ? row : __lastRowSelected);
					if(__isShiftDown && !__isControlDown) {
						deselectAll();
					}

					for (int i = low; i <= high; i++) {
						selectRow(i, false);
					}
				}
			}

			if (__isShiftDown && !__isControlDown) {
				//__lastRowSelected = __lastRowSelected;
			}
			else {
				__lastRowSelected = row;
			}
		}
	}
}

/**
When the mouse key is released, shows the popup menu if appropriate.
@param event the MouseEvent that occurred.
*/
public void mouseReleased(MouseEvent event) {
	Component c = event.getComponent();

	// if the header was clicked on ...
	if (getTableHeader() != null && c == getTableHeader()) {
		maybeShowHeaderPopup(event);
	}
	else if (__worksheetHandlePopup) {
		maybeShowPopup(event);
	}
}

/**
Notifies all listeners of a specific message.  Listeners will have their 
appropriate worksheetRowAdded(), worksheetRowDeleted(), or worksheetSetRowCount() methods called.
@param message the message being sent.
@param row the row to which the message is referring.
*/
public void notifyAllWorksheetListeners(int message, int row) {
	if (__worksheetListeners == null) {
		return;
	}
	for (int i = 0; i < __worksheetListeners.size(); i++) {
		JWorksheet_Listener l = (JWorksheet_Listener)__worksheetListeners.get(i);
		switch(message) {
			case __ROW_ADDED:
				l.worksheetRowAdded(row);
				break;
			case __ROW_DELETED:
				l.worksheetRowDeleted(row);
				break;
			case __DATA_RESET:
				l.worksheetSetRowCount(row);
				break;
			case __SELECT_ALL:
				l.worksheetSelectAllRows(row);
				break;
			case __DESELECT_ALL:
				l.worksheetDeselectAllRows(row);
				break;
			default:
		}
	}
}

/**
Notifies all the registered sort listeners that a sort is about to occur.
@param sort the type of sort occurring, one of StringUtil.SORT_ASCENDING,
StringUtil.SORT_DESCENDING, or -1 if the original order is being restored.
*/
private void notifySortListenersSortAboutToChange(int sort) {
	if (__sortListeners == null) {
		return;
	}

	JWorksheet_SortListener listener = null;
	int size = __sortListeners.size();
	for (int i = 0; i < size; i++) {
		listener= (JWorksheet_SortListener)__sortListeners.get(i);
		listener.worksheetSortAboutToChange(this, sort);
	}
}

/**
Notifies all the registered sort listeners that a sort has occurred.
@param sort the type of sort that occurred, one of StringUtil.SORT_ASCENDING,
StringUtil.SORT_DESCENDING, or -1 if the original order is being restored.
*/
private void notifySortListenersSortChanged(int sort) {
	if (__sortListeners == null) {
		return;
	}

	JWorksheet_SortListener listener = null;
	int size = __sortListeners.size();
	for (int i = 0; i < size; i++) {
		listener= (JWorksheet_SortListener)__sortListeners.get(i);
		listener.worksheetSortChanged(this, sort);
	}
}

/**
Attempts to paste the values in the clipboard into the worksheet.
*/
public void pasteFromClipboard() {
	if (__copyPasteAdapter == null) {
		__copyPasteAdapter = new JWorksheet_CopyPasteAdapter(this);
	}
	__copyPasteAdapter.paste();
}

/**
Prepares the JWorksheet to render a cell; overrides the normal JTable 
prepareRender call.  This sets attributes on the cell if the cell has 
attributes.  Programmers should not need to call this.  It is public because
it overrides JTable.prepareRenderer().
@param tcr the TableCellRenderer used to render the cell
@param row the row of the cell
@param column the <b>visible</b> column of the cell
@return the rendered cell.
*/
public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {	
	JWorksheet_CellAttributes ca = null;

	// only set the default row count column attributes for the 0th 
	// column if the row count is present.  Otherwise, the 0th column
	// attributes need to be set manually
	if (column == 0 && __showRowCountColumn && !__useRowHeaders) {
		ca = __rowCountColumnAttributes;	
	}
	else {
		ca = getCellAttributes(row, getAbsoluteColumn(column));
	}

	Component cell = super.prepareRenderer(tcr, row, column);
	if (__altTextCount > 0) {
		if (cell instanceof JLabel) {
			String text = getCellAlternateText(row, getAbsoluteColumn(column));
			if (text != null) {
				((JLabel)cell).setText(text);
			}
		}
	}

	boolean selected = isCellSelected(row, column);
	if (ca == null) {
		cell = applyCellAttributes(cell, null, selected);
		return cell;
	}
	else {
		cell = applyCellAttributes(cell, ca, selected);
		return cell;
	}
}

/**
Reads the provided proplist and sets up values within the JWorksheet.
@param p the PropList containing JWorksheet setup values.
*/
private void readPropList(PropList p) {
	String routine = "JWorksheet.readPropList";

	JTableHeader header = new JTableHeader();
	int hfsize = 12;
	int hfstyle = Font.BOLD;
	String hfname = "Arial";

	// SAM 2007-05-09 Evaluate use
	//Color cellBackground = (Color)(getClientProperty("Table.background"));
	//Color cellForeground = (Color)(getClientProperty("Table.foreground"));
	Color headerBackground = (Color)(header.getClientProperty( "TableHeader.background"));
	//Color headerForeground = (Color)(header.getClientProperty(
		//"TableHeader.foreground"));

	int tfsize = 11;
	int tfstyle = Font.PLAIN;
	String tfname = "Arial";

	boolean paste = false;
	boolean copy = false;

	if (p == null) {
		__cellFontName = tfname;
		__cellFontStyle = tfstyle;
		__cellFontSize = tfsize;
		__columnHeaderFontName = hfname;
		__rowHeaderFontName = hfname;
		__columnHeaderFontStyle = hfstyle;
		__rowHeaderFontStyle = hfstyle;
		__columnHeaderFontSize = hfsize;
		__rowHeaderFontSize = hfsize;
		__columnHeaderColor = headerBackground;
		__rowHeaderColor = headerBackground;
		__showRowCountColumn = false;
		__useRowHeaders = false;
		__showPopup = false;
		__selectionMode = __EXCEL_SELECTION;
		__oneClickRowSelection = false;
		__oneClickColumnSelection = false;
		return;
	}

	String s = p.getValue("JWorksheet.CellFontName");
	if (s != null) {
		__cellFontName = s;
	}
	else {
		__cellFontName = tfname;
	}
	
	s = p.getValue("JWorksheet.CellFontStyle");
	if (s != null) {
		if (s.equalsIgnoreCase("Plain")) {
			__cellFontStyle = Font.PLAIN;
		}
		else if (s.equalsIgnoreCase("Bold")) {
			__cellFontStyle = Font.BOLD;
		}
		else if (s.equalsIgnoreCase("Italic")) {
			__cellFontStyle = Font.ITALIC;
		}
	}
	else {
		__cellFontStyle = tfstyle;
	}
	
	s = p.getValue("JWorksheet.CellFontSize");
	if (s != null) {
		__cellFontSize = (new Integer(s)).intValue();
	}
	else {
		__cellFontSize = tfsize;
	}
	
	s = p.getValue("JWorksheet.ColumnHeaderFontName");
	if (s != null) {
		__columnHeaderFontName = s;
	}
	else {
		__columnHeaderFontName = hfname;
	}

	s = p.getValue("JWorksheet.RowHeaderFontName");
	if (s != null) {
		__rowHeaderFontName = s;
	}
	else {
		__rowHeaderFontName = hfname;
	}
	
	s = p.getValue("JWorksheet.ColumnHeaderFontStyle");
	if (s != null) {
		if (s.equalsIgnoreCase("Plain")) {
			__columnHeaderFontStyle = Font.PLAIN;
		}
		else if (s.equalsIgnoreCase("Bold")) {
			__columnHeaderFontStyle = Font.BOLD;
		}
		else if (s.equalsIgnoreCase("Italic")) {
			__columnHeaderFontStyle = Font.ITALIC;
		}
	}
	else {
		__columnHeaderFontStyle = hfstyle;
	}

	s = p.getValue("JWorksheet.RowHeaderFontStyle");
	if (s != null) {
		if (s.equalsIgnoreCase("Plain")) {
			__rowHeaderFontStyle = Font.PLAIN;
		}
		else if (s.equalsIgnoreCase("Bold")) {
			__rowHeaderFontStyle = Font.BOLD;
		}
		else if (s.equalsIgnoreCase("Italic")) {
			__rowHeaderFontStyle = Font.ITALIC;
		}
	}
	else {
		__rowHeaderFontStyle = hfstyle;
	}

	s = p.getValue("JWorksheet.ColumnHeaderFontSize");
	if (s != null) {
		__columnHeaderFontSize = (new Integer(s)).intValue();
	}
	else {
		__columnHeaderFontSize = hfsize;
	}

	s = p.getValue("JWorksheet.RowHeaderFontSize");
	if (s != null) {
		__rowHeaderFontSize = (new Integer(s)).intValue();
	}	
	else {
		__rowHeaderFontSize = hfsize;
	}

	s = p.getValue("JWorksheet.ColumnHeaderBackground");
	if (s != null) {
		__columnHeaderColor = (Color)GRColor.parseColor(s);
	}
	else {
		__columnHeaderColor = headerBackground;
	}	

	s = p.getValue("JWorksheet.RowHeaderBackground");
	if (s != null) {
		__rowHeaderColor = (Color)GRColor.parseColor(s);
	}
	else {
		__rowHeaderColor = headerBackground;
	}	

	s = p.getValue("JWorksheet.RowColumnPresent");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			__showRowCountColumn = true;
		}
		else {
			__showRowCountColumn = false;
		}
	}

	s = p.getValue("JWorksheet.ShowRowHeader");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			__showRowCountColumn = true;
			__useRowHeaders = true;
		}
		else {
			__useRowHeaders = false;
		}
	}
		
	s = p.getValue("JWorksheet.RowColumnBackground");
	if (s != null) {
		__rowCountColumnAttributes.backgroundColor = 
			(Color)GRColor.parseColor(s);
	}
	
	s = p.getValue("JWorksheet.ShowPopupMenu");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			__showPopup = true;
		}
		else {
			__showPopup = false;
		}
	}	

	s = p.getValue("JWorksheet.SelectionMode");
	if (s != null) {
		if (s.equalsIgnoreCase("SingleRowSelection")) {
			__selectionMode = __SINGLE_ROW_SELECTION;
		}
		else if (s.equalsIgnoreCase("MultipleRowSelection")) {
			__selectionMode = __MULTIPLE_ROW_SELECTION;
		}
		else if (s.equalsIgnoreCase("MultipleDiscontinuousRowSelection")) {
			__selectionMode =__MULTIPLE_DISCONTINUOUS_ROW_SELECTION;
		}
		else if (s.equalsIgnoreCase("SingleCellSelection")) {
			__selectionMode = __SINGLE_CELL_SELECTION;
		}
		else if (s.equalsIgnoreCase("ExcelSelection")) {
			__selectionMode = __EXCEL_SELECTION;
		}
		else {
			Message.printWarning(3, routine, "Unrecognized selection mode: " + s);
			__selectionMode = __EXCEL_SELECTION;
		}
	}
	else {
		__selectionMode = __EXCEL_SELECTION;
	}

	s = p.getValue("JWorksheet.OneClickRowSelection");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			__oneClickRowSelection = true;
		}
		else {
			__oneClickRowSelection = false;
		}
	}
	else {
		__oneClickRowSelection = false;
	}

	s = p.getValue("JWorksheet.OneClickColumnSelection");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			__oneClickColumnSelection = true;
		}
		else {
			__oneClickColumnSelection = false;
		}
	}
	else {
		__oneClickColumnSelection = false;
	}

	s = p.getValue("JWorksheet.Unselectable");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			setSelectable(false);
		}
		Message.printWarning(3, routine, "Unselectable is being phased out.  Use property 'Selectable' instead.");
	}

	s = p.getValue("JWorksheet.Selectable");
	if (s != null) {
		if (s.equalsIgnoreCase("false")) {
			setSelectable(false);
		}
	}

	s = p.getValue("JWorksheet.AllowCopy");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			copy = true;
			__copyEnabled = true;
		}
	}

	s = p.getValue("JWorksheet.AllowPaste");
	if (s != null) {
		if (s.equalsIgnoreCase("true")) {
			paste = true;
			__pasteEnabled = true;
		}
	}

	setCopyPasteEnabled(copy, paste);
	if (paste || copy) {
		__mainPopup = new JPopupMenu();
		addMouseListener(this);
		setupPopupMenu(__mainPopup, true);
	}

	// check for old properties no longer supported
	s = p.getValue("JWorksheet.HeaderFont");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderFont) is no longer supported.  Use JWorksheet.ColumnHeaderFontName instead.");
	}

	s = p.getValue("JWorksheet.HeaderFontName");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderFontName) is no longer supported.  Use JWorksheet.ColumnHeaderFontName instead.");
	}

	s = p.getValue("JWorksheet.HeaderFontStyle");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderFontStyle) is no longer supported.  Use JWorksheet.ColumnHeaderFontStyle instead.");
	}

	s = p.getValue("JWorksheet.HeaderSize");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderSize) is no longer supported.  Use JWorksheet.ColumnHeaderFontSize instead.");
	}

	s = p.getValue("JWorksheet.HeaderBackground");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderBackground) is no longer supported.  Use JWorksheet.ColumnHeaderBackground instead.");
	}

	s = p.getValue("JWorksheet.HeaderStyle");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderStyle) is no longer supported.  Use JWorksheet.ColumnHeaderFontStyle instead.");
	}

	s = p.getValue("JWorksheet.HeaderSize");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "HeaderSize) is no longer supported.  Use JWorksheet.ColumnHeaderFontSize instead.");
	}

	s = p.getValue("JWorksheet.CellFont");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "CellFont) is no longer supported.  Use JWorksheet.CellFontName instead.");
	}

	s = p.getValue("JWorksheet.CellStyle");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "CellStyle) is no longer supported.  Use JWorksheet.CellFontStyle instead.");
	}

	s = p.getValue("JWorksheet.CellSize");
	if (s != null) {
		Message.printWarning(3, routine, "This property (JWorksheet."
			+ "CellSize) is no longer supported.  Use JWorksheet.CellFontSize instead.");
	}	

	s = p.getValue("JWorksheet.ColumnNumbering");
	if (s != null) {
		if (s.equalsIgnoreCase("Base0")) {
			__columnNumbering = __NUMBERING_0;
		}
		else if (s.equalsIgnoreCase("Base1")) {
			__columnNumbering = __NUMBERING_1;
		}
		else if (s.equalsIgnoreCase("Excel")) {
			__columnNumbering = __NUMBERING_EXCEL;
		}
		else if (s.equalsIgnoreCase("None")) {
			__columnNumbering = __NUMBERING_NONE;
		}
	}

	s = p.getValue("JWorksheet.FirstRowNumber");
	if (s != null) {
		if (StringUtil.isInteger(s)) {
			__firstRowNum = StringUtil.atoi(s);
		}
	}

	s = p.getValue("JWorksheet.IncrementRowNumbers");
	if (s != null) {
		if (s.trim().equalsIgnoreCase("false")) {
			__incrementRowNumbers = false;
		}
	}
}

/**
Refreshes the table, repainting all the visible cells.<p>
<b>IMPORTANT!</b>  This method currently will NOT redraw the currently
selected cell in certain cases (such as when SingleCellSelection is turned on),
so do not rely on this 100% to repaint the selected cells.
TODO JTS to REVISIT as soon as possible.
*/
public void refresh() {
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();
}

/**
Removes a column from the table so that it doesn't appear any more.
@param absoluteColumn the <b>absolute</b> column number of the column to prevent from displaying.
Columns are numbered starting at 0, though column 0 is usually the row count column.
*/
public void removeColumn(int absoluteColumn) {
	int vis = getVisibleColumn(absoluteColumn);
	if (__columnRemoved[absoluteColumn] == true) {
		return;
	}
	else {
		__columnRemoved[absoluteColumn] = true;
	}

	TableColumn tc = getColumnModel().getColumn(vis);
	removeColumn(tc);
	
	if (__selectionMode == __EXCEL_SELECTION) {	
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(getRowCount(), getColumnCount());
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}
		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);	
		adjustCellAttributesAndText(__COL_DELETED, absoluteColumn);
	}
}

/**
Removes the column header from each column.
*/
public void removeColumnHeader() {
	TableColumn tc = null;
	for (int i = 0; i < getColumnCount(); i++) {
		tc = getColumnModel().getColumn(i);
		tc.setHeaderRenderer(null);
	}
}

/**
Sets a column to use the default cell editor if it has been to set to use a SimpleJComboBox as an editor.
@param absoluteColumn the <b>absolute</b> column number of the column for 
which to remove a SimpleJComboBox as an editor.  Columns are numbered starting
at 0, though column 0 is usually the row count column.
*/
public void removeColumnJComboBox(int absoluteColumn) {
	TableColumn col = getColumnModel().getColumn(getVisibleColumn(absoluteColumn));
	col.setCellEditor(null);
}

/**
Removes a JWorksheet_Listener from the list of registered listeners.
@param l the listener to remove.
*/
public void removeJWorksheetListener(JWorksheet_Listener l) {
	for (int i = 0; i < __worksheetListeners.size(); i++) {
		if (l == (JWorksheet_Listener)__worksheetListeners.get(i)) {
			__worksheetListeners.remove(i);
		}
	}	
}

/**
Removes a mouse listener from both the worksheet and its header.
@param l the MouseListener to remove.
*/
public void removeMouseListener(MouseListener l) {
	super.removeMouseListener(l);
	if (getTableHeader() != null) {
		getTableHeader().removeMouseListener(l);
	}
}

/**
Removes a sort listener from the Vector of registered sort listeners.
@param listener the listener to remove.
*/
public void removeSortListener(JWorksheet_SortListener listener) {
	if (__sortListeners == null) {
		return;
	}
		
	int size = __sortListeners.size();
	for (int i = (size - 1); i <= 0; i--) {
		if (__sortListeners.get(i) == listener) {
			__sortListeners.remove(i);
		}
	}
}

/**
Returns whether a row is selected or not.
@param row the row to check for whethether it is selected.
@return true if the row is selected, false if not.
*/
public boolean rowIsSelected(int row) {
	int[] rows = getSelectedRows();
	for (int i = 0; i < rows.length; i++) {
		if (rows[i] == row) {
			return true;
		}
	}
	return false;
}

/**
Opens a file chooser for selecting a file with a delimiter type and then writes
out all data from the table to that file with that delimiter.
*/
public void saveToFile() {
	JGUIUtil.setWaitCursor(this, true);
	JFileChooser jfc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory());
		
	jfc.setDialogTitle("Save Worksheet to File");
	SimpleFileFilter comma = new SimpleFileFilter("csv", "Comma-delimited text file");
	SimpleFileFilter commatxt = new SimpleFileFilter("txt", "Comma-delimited text file");
	SimpleFileFilter tab = new SimpleFileFilter("txt", "Tab-delimited text file");
	SimpleFileFilter semicolon = new SimpleFileFilter("txt", "Semicolon-delimited text file");
	jfc.addChoosableFileFilter(comma);
	jfc.addChoosableFileFilter(commatxt);
	jfc.addChoosableFileFilter(tab);
	jfc.addChoosableFileFilter(semicolon);
	jfc.setAcceptAllFileFilterUsed(false);
	jfc.setFileFilter(comma);
	jfc.setDialogType(JFileChooser.SAVE_DIALOG);	
	JGUIUtil.setWaitCursor(this, false);

	int retVal = jfc.showSaveDialog(this);
	if (retVal != JFileChooser.APPROVE_OPTION) {
		return;
	}

	String currDir = (jfc.getCurrentDirectory()).toString();
	JGUIUtil.setLastFileDialogDirectory(currDir);
	
	JGUIUtil.setWaitCursor(this, true);

	String filename = jfc.getSelectedFile().getPath();

	String delimiter = ",";
	if (jfc.getFileFilter() == comma) {
		if (!StringUtil.endsWithIgnoreCase(filename, ".csv")) {
			filename += ".csv";
		}
	}
	else if (jfc.getFileFilter() == commatxt) {
		if (!StringUtil.endsWithIgnoreCase(filename, ".txt")) {
			filename += ".txt";
		}
	}
	if (jfc.getFileFilter()	== tab) {
		delimiter = "\t";
		if (!StringUtil.endsWithIgnoreCase(filename, ".txt")) {
			filename += ".txt";
		}
	}
	else if (jfc.getFileFilter() == semicolon) {
		delimiter = ";";
		if (!StringUtil.endsWithIgnoreCase(filename, ".txt")) {
			filename += ".txt";
		}
	}
	saveToFile(filename, delimiter);
}

/**
Saves the contents of the worksheet (in all visible columns) to a file.
@param filename the name of the file to which to write.  
@param delimiter the delimiter to use for separating field values.
*/
public void saveToFile(String filename, String delimiter) {
	String routine = "saveToFile";

	List<String> lines = new Vector<String>();
	
	int numRows = getRowCount();
	int numCols = getColumnCount();

	StringBuffer line = new StringBuffer();

	for (int i = 0; i < numCols; i++) {
		line.append("\"");
		line.append(getColumnName(i, true));
		line.append("\"");
		if (i < numCols - 1) {
			line.append(delimiter);
		}
	}
	lines.add(line.toString());

	String s = null;
	boolean quote = false;

	for (int i = 0; i < numRows; i++) {
		line = new StringBuffer();
		for (int j = 0; j < numCols; j++) {
			quote = false;
			s = getValueAtAsString(i, j);

			// Check to see if the field contains the delimiter.
			// If it does, the field string needs to be quoted.
			if (s.indexOf(delimiter) > -1) {
				quote = true;
			}
			
			// Remove any newlines.
			if (s.indexOf("\n") > -1) {
				s = StringUtil.replaceString(s, "\n", "");
			}

			if (quote) {
				line.append("\"");
			}
			
			line.append(s);
			
			if (quote) {
				line.append("\"");
			}
			
			if (j < numCols - 1) {
				line.append(delimiter);
			}
		}
		lines.add(line.toString());
	}

	// Create a new FileOutputStream wrapped with a DataOutputStream for writing to a file.
	try {
		PrintWriter oStream = new PrintWriter( new FileWriter(filename));

		// Write each element of the lines Vector to a file.
		// For some reason, when just using println in an
		// applet, the cr-nl pair is not output like it should be on Windows95.  Java Bug???
		String linesep = System.getProperty("line.separator");
		for (int i = 0; i < lines.size(); i++) {
			oStream.print(lines.get(i).toString() + linesep);
		}
		oStream.flush(); 
		oStream.close(); 
	}
	catch (Exception e) {
		JGUIUtil.setWaitCursor(this, false);
		Message.printWarning(3, routine, "Error writing to file.");
		Message.printWarning(3, routine, e);
	}

	JGUIUtil.setWaitCursor(this, false);
}

/**
Scrolls the table to the specified cell.
@param row the row of the cell
@param visibleColumn the <b>visible</b> column number of the cell.
*/
public void scrollToCell(int row, int visibleColumn)  {
	scrollRectToVisible(getCellRect(row, visibleColumn, true));
}

/**
Scrolls to the last row of data.
*/
public void scrollToLastRow() {
	scrollToRow(getRowCount() - 1);
}

/**
Scrolls the table to the specified row.
@param row the row to scroll to.  Rows are numbered starting at 0.
*/
public void scrollToRow(int row) {
	String routine = CLASS + ".scrollToRow";
	
	if (row >= getRowCount()) {
		Message.printWarning(3, routine, "Will not scroll to row "
			+ row + ", total row count is " + getRowCount());
		return;
	}
	if (row < 0) {
		Message.printWarning(3, routine, "Will not scroll to negative row");
		return;
	}
	scrollToCell(row, 0);
}

/**
Selects all the rows in the worksheet.
*/
public void selectAllRows() {
	notifyAllWorksheetListeners(__SELECT_ALL, PRE_SELECTION_CHANGE);
	int size = getRowCount();

	if (size == 0) {
		return;
	}

	if (__selectionMode == __EXCEL_SELECTION) {
		((JWorksheet_RowSelectionModel)getSelectionModel()).selectAllRows();
	}
	else {
		((DefaultListSelectionModel)getSelectionModel()).setSelectionInterval(0, size);
	}
	notifyAllWorksheetListeners(__SELECT_ALL, POST_SELECTION_CHANGE);
}

/**
Selects a single cell.
@param row the row of the cell to select.
@param visibleColumn the <b>visible</b> column to select.
*/
public void selectCell(int row, int visibleColumn) {
	if (row < 0 || visibleColumn < 0) {
		return;
	}

	if (__selectionMode == __EXCEL_SELECTION) {	
		((JWorksheet_RowSelectionModel)getSelectionModel()).selectCell(row, visibleColumn);
	}
	else {
		setRowSelectionInterval(row, row);
		setColumnSelectionInterval(visibleColumn, visibleColumn);
	}
}

/**
Selects a column.
@param visibleColumn the <b>visible</b> column to select.
*/
public void selectColumn(int visibleColumn) {
	if (__selectionMode == __EXCEL_SELECTION) {	
		((JWorksheet_RowSelectionModel)getSelectionModel()).selectColumn(visibleColumn);
	}
	else {
		setColumnSelectionInterval(visibleColumn, visibleColumn);
	}
}

/**
Selects the last row of data.  
*/
public void selectLastRow() {
	selectRow(getRowCount() - 1);
}

/**
Programmatically selects the specified row -- but does not scroll to the row.
Call scrollToRow(row) for that.  Deselects all the rows prior to selecting the new row.
@param row the row to select.  Rows are numbered starting at 0.
*/
public void selectRow(int row) {
	selectRow(row, true);
}

/**
Programmatically selects the specified row -- but does not scroll to the row.  Call scrollToRow(row) for that.  
@param row the row to select.  Rows are numbered starting at 0.
@param deselectFirst if true, then all other selected rows will be deselected
prior to the row being selected.  Otherwise, this row and all the currently-
selectedted rows will end up being selected.
*/
public void selectRow(int row, boolean deselectFirst) {
	String routine = CLASS + ".selectRow";
	
	if (row >= getRowCount()) {
		Message.printWarning(3, routine, "Cannot select row " + row 
			+ ", there are only " + getRowCount() + " rows in the worksheet.");
		return;
	}
	if (row < 0) {
		Message.printWarning(3, routine, "Cannot select a negative row.");
		return;
	}

	if (deselectFirst) {
		deselectAll();
	}

	if (deselectFirst) {
		if (__selectionMode == __EXCEL_SELECTION) {
			((JWorksheet_RowSelectionModel)getSelectionModel()).selectRow(row);
		}
		else {
			((DefaultListSelectionModel)getSelectionModel()).setSelectionInterval(row, row);
		}
	}
	else {
		if (__selectionMode == __EXCEL_SELECTION) {
			((JWorksheet_RowSelectionModel)getSelectionModel()).selectRowWithoutDeselecting(row);
		}
		else {
			((DefaultListSelectionModel)getSelectionModel()).addSelectionInterval(row, row);
		}
	}		
}

/**
Sets cell alternate text for a specified cell.  If the cell already has 
alternate text, it is replaced with the new text.  If the specified text is
null, the alternate text is removed from the cell.<p>
Alternate text can be used so that the worksheet shows other values temporarily,
and the actual data stored in the cells do not need to be disturbed.  For 
instance, this is used in the HydroBase Water Information Sheets to set certain
cells to say "DRY" when the user clicks a button.  If the user clicks the button
again, the "DRY" goes away (the alternate text is removed) and the user can 
then see the data that appears in the cell.
@param row the row of the cell.  Rows are numbered starting at 0.
@param absoluteColumn the <b>absolute</b> column of the cell.  Columns are 
numbered starting at 0, though column 0 is usually the row count column.
@param text the alternate text to set
*/
public void setCellAlternateText(int row, int absoluteColumn, String text) {
	String routine = CLASS + ".setCellAlternateText";
	
	if (row < 0 || absoluteColumn < 0) {
		Message.printWarning(3, routine, "Row " + row + " or column " + absoluteColumn + " is out of bounds.");
		return;
	}
	
	// passing in null alt text removes the alt text for the specified cell
	if (text == null) {
		int visCol = getVisibleColumn(absoluteColumn);
		if (__altTextCount > 0 && absoluteColumn > -1) {
			for (int i = 0; i < __altTextCols.length; i++) {
				if (__altTextCols[i] == visCol && __altTextRows[i] == row) {
					__altTextCols[i] = -1;
					__altTextRows[i] = -1;
					__altText[i] = null;
					__altTextCount--;
//					Message.printStatus(1, "", "Cell alt text removed for: " + row + ", " + visCol);
					compactAltTextArrays();
					refresh();
					return;	
				}
			}
		}
		return;
	}

	int visCol = getVisibleColumn(absoluteColumn);
	// search to see if the cell already has alt text, and if so, reset it to the new alt text 
	if (__altTextCount > 0) {
		for (int i = 0; i < __altTextCols.length; i++) {
			if (__altTextCols[i] == visCol && __altTextRows[i] == row) {
				__altText[i] = text;
				refresh();
//				Message.printStatus(1, "", 
//					"Cell alt text replaced old cell alt text at: " + row + ", " + visCol);
				return;
			}
		}
	}

	// otherwise, add a new alt text to the array.  Check the array sizes and resize if necessary
	if (((__altTextCount + 1) % __ARRAY_SIZE) == 0) {
//		Message.printStatus(1, "", "Need to resize data arrays to: " + (__altTextCount + __ARRAY_SIZE));
		int[] temp = new int[(__altTextCount + 1) + __ARRAY_SIZE];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = -1;
		}
		System.arraycopy(__altTextCols, 0, temp, 0, __altTextCount);
		__altTextCols = temp;
		
		temp = new int[(__altTextCount + 1) + __ARRAY_SIZE];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = -1;
		}
		System.arraycopy(__altTextRows, 0, temp, 0, __altTextCount);
		__altTextRows = temp;
		
		String[] tempat = new String[(__altTextCount + 1)+__ARRAY_SIZE];
		System.arraycopy(__altText, 0, tempat, 0, __altTextCount);
		__altText = tempat;
	}

	// the arrays are always compacted when alt text is removed,
	// so the __altTextCount var can be used safely for putting a new 
	// alt text at the very end of the array
//	Message.printStatus(1, "", "Alt text set at the very end for " + row + ", " + visCol);
	__altTextCols[__altTextCount] = getVisibleColumn(absoluteColumn);
	__altTextRows[__altTextCount] = row;
	__altText[__altTextCount] = text;
	__altTextCount++;
	
	refresh();
}

/**
Sets cell attributes for a specified cell.  If the cell already has attributes,
they are replaced with the new attributes.  If the specified cell attributes are
null, the attributes are removed from the cell.
@param row the row of the cell.  Rows are numbered starting at 0.
@param absoluteColumn the <b>absolute</b> column of the cell.  Columns are 
numbered starting at 0, though column 0 is usually the row count column.
@param ca the cell attributes to set
*/
public void setCellAttributes(int row, int absoluteColumn, JWorksheet_CellAttributes ca) {
	String routine = CLASS + ".setCellAttributes";

	if (row < 0 || absoluteColumn < 0) {
		Message.printWarning(3, routine, "Row " + row + " or column "
			+ absoluteColumn + " is out of bounds.");
		return;
	}
	
	// passing in null cell attributes removes the cell attributes for the specified cell
	if (ca == null) {
		int visCol = getVisibleColumn(absoluteColumn);
		if (__attrCount > 0 && absoluteColumn > -1) {
			for (int i = 0; i < __attrCols.length; i++) {
				if (__attrCols[i] == visCol && __attrRows[i] == row) {
					__attrCols[i] = -1;
					__attrRows[i] = -1;
					__cellAttrs[i] = null;
					__attrCount--;
//					Message.printStatus(1, "", "Cell attributes removed for: " + row + ", " + visCol);
					compactAttrArrays();
					refresh();
					return;	
				}
			}
		}
		return;
	}

	int visCol = getVisibleColumn(absoluteColumn);
	// search to see if the cell already has cell attributes, and if
	// so, reset them to the new attributes
	if (__attrCount > 0) {
		for (int i = 0; i < __attrCols.length; i++) {
			if (__attrCols[i] == visCol && __attrRows[i] == row) {
				__cellAttrs[i] = (JWorksheet_CellAttributes)ca.clone();
				refresh();
//				Message.printStatus(1, "", "Cell attributes replaced old cell attributes at: " + row + ", " + visCol);
				return;
			}
		}
	}

	// otherwise, add a new attribute to the array.  Check the array sizes and resize if necessary
	if (((__attrCount + 1) % __ARRAY_SIZE) == 0) {
//		Message.printStatus(1, "", "Need to resize data arrays to: " + (__attrCount + __ARRAY_SIZE));
		int[] temp = new int[(__attrCount + 1) + __ARRAY_SIZE];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = -1;
		}
		System.arraycopy(__attrCols, 0, temp, 0, __attrCount);		
		__attrCols = temp;
		
		temp = new int[(__attrCount + 1) + __ARRAY_SIZE];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = -1;
		}
		System.arraycopy(__attrRows, 0, temp, 0, __attrCount);
		__attrRows = temp;
		
		JWorksheet_CellAttributes[] tempca = new JWorksheet_CellAttributes[(__attrCount + 1) + __ARRAY_SIZE];
		System.arraycopy(__cellAttrs, 0, tempca, 0, __attrCount);
		__cellAttrs = tempca;
	}

	// the arrays are always compacted when cell attributes are removed,
	// so the __attrCount var can be used safely for putting a new attribute
	// at the very end of the array
//	Message.printStatus(1, "", "Cell attribute set at the very end for " + row + ", " + visCol);
	__attrCols[__attrCount] = getVisibleColumn(absoluteColumn);
	__attrRows[__attrCount] = row;
	__cellAttrs[__attrCount] = ca;
	__attrCount++;
	
	refresh();
}

/**
Overrides the specified cell's default editability and sets whether the value
in the cell may be edited or not.
@param row the row of the cell.  Rows are numbered starting at 0.
@param column the column of the cell
TODO (JTS - 2003-07-23)absolute or visible?
@param state whether the cell should be editable or not.
*/
public void setCellEditable(int row, int column, boolean state) {
	((JWorksheet_AbstractTableModel)getModel()).overrideCellEdit( row, column, state);
}

/**
Sets the font name to be used with worksheet cells.  While individual cell
attributes can be used to change the font in different cells, getCellFont()
and setCellFont*() are quicker for changing and returning the font used 
everywhere in the table where a specific cell font attribute has not been set.
@param cellFontName the font name
*/
public void setCellFontName(String cellFontName) {
	__cellFontName = cellFontName;
}

/**
Sets the font size in which items in the table should be displayed.  While individual cell
attributes can be used to change the font in different cells, getCellFont()
and setCellFont*() are quicker for changing and returning the font used 
everywhere in the table where a specific cell font attribute has not been set.
@param size the size of the font (in points) in which table items should be displayed.
*/
public void setCellFontSize(int size) {
	__cellFontSize = size;
}

/**
Sets the font style in which items in the table should be displayed.  While individual cell
attributes can be used to change the font in different cells, getCellFont()
and setCellFont*() are quicker for changing and returning the font used 
everywhere in the table where a specific cell font attribute has not been set.
@param style the style of the font in which table items should be displayed.
*/
public void setCellFontStyle(int style) {
	__cellFontStyle = style;
}

/**
Sets the cell renderer that the table will use for these classes:<br>
<ul>
<li>Date</li>
<li>Double</li>
<li>Float</li>
<li>Integer</li>
<li>String</li>
</ul>
@param tcr the TableCellRenderer object to be used.
*/
public void setCellRenderer(JWorksheet_DefaultTableCellRenderer tcr) {
	__defaultCellRenderer = tcr;
	setDefaultRenderer(Integer.class, tcr);
	setDefaultRenderer(Double.class, tcr);
	setDefaultRenderer(String.class, tcr);
	setDefaultRenderer(Date.class, tcr);
	setDefaultRenderer(Float.class, tcr);
}

/**
Sets up a column to use a cell-specific JComboBox editor.  This is opposed
to setting up a column so that every cell in it is a JComboBox.  Using this
method, the cells in the column can use
either a JComboBox as the editor, or a textfield, or both.  
This must be used in conjunction with setCellSpecificJComboBoxValues(), and
can be used with setJComboBoxEditorPreviousRowCopy().  This method will 
create the combo boxes so that the user cannot enter new values; they must
select one from the list.
@param absoluteColumn the <b>absolute</b> column to set up with a 
cell-specific editor.  Columns are numbered starting at 0, though column 0 
is usually the row count column.
*/
public void setCellSpecificJComboBoxColumn(int absoluteColumn) {
	setCellSpecificJComboBoxColumn(absoluteColumn, false);
}

/**
Sets up a column to use a cell-specific JComboBox editor.  This is opposed
to setting up a column so that every cell in it is a JComboBox.  Using this
method, the cells in the column can use
either a JComboBox as the editor, or a textfield, or both.  
This must be used in conjunction with setCellSpecificJComboBoxValues(), and
can be used with setJComboBoxEditorPreviousRowCopy().  
@param absoluteColumn the <b>absolute</b> column to set up with a 
cell-specific editor.  Columns are numbered starting at 0, though column 0 is
usually the row count column.
@param editable whether the ComboBoxes in the column should allow the user to
enter a new value (true), or if the user can only select what is already in the list (false)
*/
public void setCellSpecificJComboBoxColumn(int absoluteColumn, 
boolean editable) {
	TableColumn col = getColumnModel().getColumn(getVisibleColumn( absoluteColumn));
	int rows = getRowCount();
	JWorksheet_JComboBoxCellEditor editor = new JWorksheet_JComboBoxCellEditor(this, rows, editable);
	addJWorksheetListener(editor);
	col.setCellEditor(editor);
}

/**
Sets the values to be used in a JComboBox cell editor for a specific cell.
@param row the row in which the cell is located.  Rows are numbered starting at 0.
@param absoluteColumn the <b>absolute</b> column in which the cell is located.  
A call must have already been made to 
setCellSpecificJComboBoxColumn(absoluteColumn) for this to work.
Columns are numbered starting at 0, though column 0 is usually the row count column.
@param v a Vector of values to populate the JComboBox editor with.
*/
public void setCellSpecificJComboBoxValues(int row, int absoluteColumn, List v) {
	String routine = CLASS + ".setCellSpecificJComboBoxValues";
	
	TableColumn col = getColumnModel().getColumn(getVisibleColumn( absoluteColumn));
	TableCellEditor editor = col.getCellEditor();

	if (editor == null) {
		Message.printStatus(1, routine, "No combo box editor set "
			+ "up for column " + absoluteColumn + ", not setting values.");
		return;
	}
	
	((JWorksheet_JComboBoxCellEditor)editor).setJComboBoxModel(row, v);
}

/**
Sets whether when adding a row to a column that has been set to use 
JComboBox editors (via setCellSpecificJComboBoxColumn) should use the same
data model for the JComboBox as the cell immediately above it.  
@param absoluteColumn the <b>absolute</b> absoluteColumn to set previous copy.
Columns are numbered starting at 0, though column 0 is usually the row count column.
@param copy whether or not to copy the previous data model
@see RTi.Util.GUI.JWorksheet_JComboBoxCellEditor#setPreviousRowCopy(boolean)
*/
public void setCellSpecificJComboBoxEditorPreviousRowCopy(int absoluteColumn, boolean copy) {
	String routine = CLASS+".setCellSpecificJComboBoxEditorPreviousRowCopy";

	TableColumn col = getColumnModel().getColumn(getVisibleColumn(absoluteColumn));
	TableCellEditor editor = col.getCellEditor();

	if (editor == null) {
		Message.printStatus(1, routine, "No combo box editor set "
			+ "up for column " + absoluteColumn + ", not setting values.");	
		return;
	}
	
	((JWorksheet_JComboBoxCellEditor)editor).setPreviousRowCopy(copy);
}	

/**
Sets the alignment that a column should display its data with.  This overrides
any column alignment code in the cell renderer.  
@param absoluteColumn the <b>absolute</b> column of the column for which to set an alignment.
@param alignment one of DEFAULT (allows the cell renderer to determine the 
alignment), CENTER, LEFT, or RIGHT.
*/
public void setColumnAlignment(int absoluteColumn, int alignment) {
	if (alignment < DEFAULT || alignment > RIGHT) {
		Message.printStatus(1, "", "Invalid alignment: " + alignment);
		return;
	}
	__columnAlignments[absoluteColumn] = alignment;
}

/**
Sets a column to use a SimpleJComboBox as an editor (for all cells in the
column).  The SimpleJComboBox will contain the values in the passed-in Vector.
<p>
<b>Note:</b> If all the cells in a particular column with use a combo box that
has the same possible values (e.g., a boolean field in which the user can 
select either 'True' or 'False'), it is more efficient to use this method 
rather than using the setCellSpecific*ComboBox*() methods.
@param absoluteColumn the <b>absolute</b> column number of the column for 
which to use a SimpleJComboBo as the editor.  Columns are numbered starting at
0, though column 0 is usually the row count column.
@param v a Vector of Strings with which to populate the 
SimpleJComboBox.  If null, then the Combo box will be removed from the column.
*/
public void setColumnJComboBoxValues(int absoluteColumn, List v) {
	setColumnJComboBoxValues(absoluteColumn, v, false);
}

/**
Sets a column to use a SimpleJComboBox as an editor (for all the cells in the
column).  The SimpleJComboBox will contain the values in the passed-in Vector.
<p>
<b>Note:</b> If all the cells in a particular column with use a combo box that
has the same possible values (e.g., a boolean field in which the user can 
select either 'True' or 'False'), it is more efficient to use this method 
rather than using the setCellSpecific*ComboBox*() methods.
@param absoluteColumn the <b>absolute</b> column number of the column for 
which to use a SimpleJComboBo as the editor.  Columns are numbered starting at
0, though column 0 is usually the row count column.
@param v a Vector of Strings with which to populate the SimpleJComboBox.  If 
null, then the combo box will be removed from the column.
@param editable if true, the SimpleJComboBox values can be selected and also edited by the user.
*/
public void setColumnJComboBoxValues(int absoluteColumn, List v, boolean editable) {
	TableColumn col = getColumnModel().getColumn(getVisibleColumn(absoluteColumn));
	if (v == null) {
		col.setCellEditor(null);
	}
	else {
		SimpleJComboBox editor = new SimpleJComboBox(v, editable);
		col.setCellEditor(new DefaultCellEditor(editor));
	}
}

/**
Sets a new value for a Table column name.
@param absoluteColumn the <b>visible</b> column for which to set the name.
@param name the new column name.
*/
public void setColumnName(int absoluteColumn, String name) {
	TableColumn col = getColumnModel().getColumn(getVisibleColumn(absoluteColumn));
	col.setHeaderValue(name);
	__columnNames[absoluteColumn] = name;
	if (getTableHeader() != null) {
		getTableHeader().repaint();
	}
}

/**
Sets a tooltip for a column.
@param absoluteColumn the <b>absolute</b> column to assign the tooltip.
@param tip the tooltip.
*/
public void setColumnToolTipText(int absoluteColumn, String tip) {
	try {
		getWorksheetHeader().setColumnToolTip(absoluteColumn, tip);	
	}
	catch (Exception e) {	
		Message.printWarning(3, CLASS + ".setColumnToolTipText",
			"Error setting column tool tip (column " 
			+ absoluteColumn + ").  Check log for details.");
		Message.printWarning(3, CLASS + ".setColumnToolTipText", e);
	}
}

/**
Sets tooltips for all the columns in the worksheet.
@param tips array of Strings, each one of which is a tooltip for an absolute column.
*/
public void setColumnsToolTipText(String[] tips) {
	for (int i = 0; i < tips.length; i++) {
		setColumnToolTipText(i, tips[i]);
	}
}

/**
Set the widths of the columns in the worksheet. <b>NOTE!</b> This method
will not work until the GUI on which the JWorksheet is located is visible, 
because otherwise the calls to getGraphics() will return null values.  For
that reason, setVisible(true) must have been called -- or the other version of the
method should be used and a valid Graphics object should be passed in.
@param widths an integer array of widths, one for each column in the table.
The widths are measured in terms of how many characters a column 
should be able to accomodate, not in pixels or font sizes.  <p>
For example, A column 
that needs to be able to display "2003-03" would have a width of 7.
The character "X" is used as the sizing character for calculating how large 
(in screen pixel terms) the column will be to accomodate the given number of characters.
*/
public void setColumnWidths(int[] widths) {
	setColumnWidths(widths, getGraphics());
}

/**
Sets the widths of the columns in the worksheet, using the provided 
Graphics object to determine how wide the columns should be.
@param widths an integer array of widths, one for each column in the table.
The widths are actually measured in terms of how many characters a column 
should be able to accomodate, not in pixels or font sizes.  e.g., A column 
that needs to be able to display "2003-03" would have a width of 7.
The character "X" is used as the sizing character for calculating how large 
(in screen pixel terms) the column will be to accomodate the given number of characters.
@param g a Graphics object that can be used to determine how many pixels
a certain font takes up in a given graphics context.
*/
public void setColumnWidths(int[] widths, Graphics g) {
	String routine = CLASS + ".setColumnWidths";

	if (g == null) {
		Message.printWarning(3, routine, "Graphics are null, not setting column widths.");
		return;
	}
	if (widths == null) {
		Message.printWarning(3, routine, "Widths are null, not setting column widths.");
		return;
	}
	if (__columnNames == null) {
		Message.printWarning(3, routine, "Column names are null, not setting column widths.");			
		return;
	}
	if (widths.length != __columnNames.length) {
		Message.printWarning(3, routine, "Length of widths array ("
			+ widths.length + ") does not match internal column "
			+ "names array length (" + __columnNames.length + ").");
		return;
	}

	// test if the graphics have been 

	// Get the font metrics for each of the main fonts used in the
	// worksheet (the font used for the header and the font used for the cells).
	FontMetrics fh = g.getFontMetrics(
		new Font(__columnHeaderFontName, __columnHeaderFontStyle, __columnHeaderFontSize));
	FontMetrics fc = g.getFontMetrics( new Font(__cellFontName, __cellFontStyle, __cellFontSize));
		
	String s = "";
	int i = 0;
	int hwidth = 0;
	int cwidth = 0;

	int count = __columnNames.length;
	
	// loop through each of the columns and get the desired width 
	// (in characters) from the table model's set widths.  
	// Calculate how many pixels would be needed for each of the
	// above font metrics to show that many characters, and then
	// use the larger measurement as the field width.
	for (i = 0; i < count; i++) {
		s = "";
		if (!__columnRemoved[i]) {
			TableColumn tc = getColumnModel().getColumn( getVisibleColumn(i));		
			for (int j = 0; j < widths[i]; j++) {
				s += "X";
			}
			hwidth = fh.stringWidth(s);
			cwidth = fc.stringWidth(s);
			if (hwidth > cwidth) {
				tc.setPreferredWidth(hwidth + 15);
			}
			else {
				tc.setPreferredWidth(cwidth + 15);
			}
		}
	}
}

/**
Sets whether copying and pasting is enabled in the JWorksheet.
@param copySetting whether users can copy from the worksheet.
@param pasteSetting whether users can paste to the worksheet.
*/
protected void setCopyPasteEnabled(boolean copySetting, boolean pasteSetting) {
//	if (!IOUtil.isRunningApplet()) {
		if (__copyPasteAdapter == null) {
			__copyPasteAdapter = new JWorksheet_CopyPasteAdapter(this);
		}
	
		__copyPasteAdapter.setCopyEnabled(copySetting);
		__copyPasteAdapter.setPasteEnabled(pasteSetting);
		__copyEnabled = copySetting;
		__pasteEnabled = pasteSetting;
/*		
	}
	else {
		__copyEnabled = false;
		__pasteEnabled = false;
	}
*/
}

/**
Sets new data in the existing table model.  This should be used if the 
definition of the table model doesn't change, but the data in the table 
model changes extensively.  Otherwise, just call addRow()
@param data that will be set in the existing table model.
*/
public void setData(List data) {
	__lastRowSelected = -1;
	((JWorksheet_AbstractTableModel)getModel()).setNewData(data);
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();

	if (__selectionMode == __EXCEL_SELECTION) {
		int rows = getRowCount();
		int cols = getColumnCount();
		setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellSelectionEnabled(true);			
		JWorksheet_RowSelectionModel r = new JWorksheet_RowSelectionModel(rows, cols);
		r.setPartner(__partner);
		if (!__selectable) {
			r.setSelectable(false);
		}

		JWorksheet_ColSelectionModel c = new JWorksheet_ColSelectionModel();
		r.setColSelectionModel(c);
		c.setRowSelectionModel(r);
		setSelectionModel(r);
		setOneClickRowSelection(__oneClickRowSelection);
		getColumnModel().setSelectionModel(c);
	}	
	
	if (__listRowHeader != null) {
		adjustListRowHeaderSize(__DATA_RESET);
	}	
	notifyAllWorksheetListeners(__DATA_RESET, getRowCount());
}

/**
Sets whether the worksheet is dirty.
@param dirty whether the worksheet is dirty.
*/
public void setDirty(boolean dirty) {
	__dirty = dirty;
}

/**
Called by the JWorksheet_DefaultTableCellEditor to set the cell which is being edited.
@param row the row of the cell being edited
@param visibleColumn the <b>visible</b> column of the cell being editedd
*/
protected void setEditCell(int row, int visibleColumn) {
	__editRow = row;
	__editCol = visibleColumn;
}

/**
Sets the number of the first row of the table.  All other row numbers in the
row header are determined from this.
@param firstRowNumber the number of the first row in the row header.
*/
public void setFirstRowNumber(int firstRowNumber) {
	__firstRowNum = firstRowNumber;

	if (__listRowHeader != null) {
		enableRowHeader();
	}
}

/**
Sets the frame in which the hourglass will be displayed when sorting.
*/
public void setHourglassJDialog(JDialog dialog) {
	__hourglassJDialog = dialog;
}

/**
Sets the frame in which the hourglass will be displayed when sorting.
*/
public void setHourglassJFrame(JFrame frame) {
	__hourglassJFrame = frame;
}

/**
Sets whether the worksheet's header should support displaying multiple lines.
If this is turned on, linebreaks ('\n') in the column names will result in line breaks in the header.
@param multiline whether to support multiple line headers.
*/
private void setMultipleLineHeaderEnabled(boolean multiline) {
	__hcr.setMultiLine(multiline);
}

/**
Sets a new table model into the worksheet and populates the worksheet with the data in the table model.
@param tm the TableModel with which to populate the worksheet.
*/
public void setModel(JWorksheet_AbstractTableModel tm) {
	__lastRowSelected = -1;
	tm._worksheet = this;
	super.setModel(tm);
	__dirty = false;
	initialize(tm.getRowCount(), tm.getColumnCount());	

	createColumnList();
	
	if (!__showRowCountColumn && !__useRowHeaders) {
		removeColumn(0);
	}

	if (__listRowHeader != null) {
		adjustListRowHeaderSize(__DATA_RESET);
	}	

	if (tm.getColumnToolTips() != null) {
		setColumnsToolTipText(tm.getColumnToolTips());
	}	
	notifyAllWorksheetListeners(__DATA_RESET, getRowCount());
}

/**
Sets whether the user should be able to select an entire row just by clicking
on the first (0th) column.  Not usable with default JTable selection models.
@param oneClick if true, the user can select an entire row by clicking on the first (0th) column.
*/
public void setOneClickRowSelection(boolean oneClick) {
	__oneClickRowSelection = oneClick;
	if (__selectionMode == __EXCEL_SELECTION) {
		if (__listRowHeader == null) {
			((JWorksheet_RowSelectionModel)getSelectionModel()).setOneClickRowSelection(oneClick);
		}
	}
	else {
//		return;
	}		
}

/**
Sets the popup menu to display if the table is right-clicked on.
@param popup the popup menu to display.
*/
public void setPopupMenu(JPopupMenu popup, boolean worksheetHandlePopup) {
	setupPopupMenu(popup, worksheetHandlePopup);
	__mainPopup = popup;
}

/**
Sets the partner row selection model used when this table is the row header 
of another in a JScrollWorksheet.
@param partner the JWorksheet_RowSelectionModel of the main table, the one 
for which another this table is its row header.
*/
public void setRowSelectionModelPartner(JWorksheet_RowSelectionModel partner) {
	((JWorksheet_RowSelectionModel)getSelectionModel()).setPartner(partner);
	__partner = partner;
}

/**
Sets a data object in a row.  Only works with table models that store 
each rows as a separate data object (are descended from JWorksheet_AbstractRowTableModel).
@param o the data object to replace the object at the specified row with.
@param pos the row at which to replace the object.
*/
public void setRowData(Object o, int pos) {
	if (!(getModel() instanceof JWorksheet_AbstractRowTableModel)) {
		return;
	}

	if (pos < 0) {
		return;
	}
	if (pos > getRowCount()) {
		return;
	}

	((JWorksheet_AbstractTableModel)getModel()).setRowData(o, pos);
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();
}

/**
Sets whether this worksheet's cells can be selected or not.
@param selectable whether this worksheet's cells can be selected or not.
*/
public void setSelectable(boolean selectable) {
	__selectable = selectable;
}

/**
Sets up the popup menu for copying and pasting cell values.
@param menu the menu in which to set up the menu items.
*/
private void setupPopupMenu(JPopupMenu menu, boolean worksheetHandlePopup) {
	if (__mainPopup == null && worksheetHandlePopup) { 
		addMouseListener(this);
	}
	__copyMenuItem = new JMenuItem(__MENU_COPY);
	__copyHeaderMenuItem = new JMenuItem(__MENU_COPY_HEADER);
	__pasteMenuItem = new JMenuItem(__MENU_PASTE);
	__copyAllMenuItem = new JMenuItem(__MENU_COPY_ALL);
	__copyAllHeaderMenuItem = new JMenuItem(__MENU_COPY_ALL_HEADER);
	
	__deselectAllMenuItem = new JMenuItem(__MENU_DESELECT_ALL);
	__deselectAllMenuItem.addActionListener(this);
	__selectAllMenuItem = new JMenuItem(__MENU_SELECT_ALL);
	__selectAllMenuItem.addActionListener(this);

	menu.add(__selectAllMenuItem);
	menu.add(__deselectAllMenuItem);

	if (__copyEnabled || __pasteEnabled) {
		menu.addSeparator();
	}
	
	if (__copyEnabled) {
		__copyMenuItem.addActionListener(this);
		menu.add(__copyMenuItem);
		__copyHeaderMenuItem.addActionListener(this);
		menu.add(__copyHeaderMenuItem);
		__copyAllMenuItem.addActionListener(this);
		menu.add(__copyAllMenuItem);
		__copyAllHeaderMenuItem.addActionListener(this);
		menu.add(__copyAllHeaderMenuItem);
	}
	if (__pasteEnabled) {
		__pasteMenuItem.addActionListener(this);
		menu.add(__pasteMenuItem);
	}
	if (1 == 1) {	
		// TODO (JTS - 2004-10-21) activate with a property later.
		menu.addSeparator();
		JMenuItem saveMenuItem = new JMenuItem(__MENU_SAVE_TO_FILE);
		saveMenuItem.addActionListener(this);
		menu.add(saveMenuItem);
	}	

	__worksheetHandlePopup = worksheetHandlePopup;
}

/**
Overrides the default JTable implementation of setValue at.  
@param o the value to set in the cell
@param row the row of the cell
@param col the <b>visible</b> column of the cell.
*/
public void setValueAt(Object o, int row, int col) {
	super.setValueAt(o, row, col);
}

/**
Turns a wait cursor on or off on the worksheet.
@param hourglassEnabled if true, the wait cursor will be displayed.  If false, it will be hidden.
*/
public void setWaitCursor(boolean hourglassEnabled) {
	if (__hourglassJDialog == null && __hourglassJFrame == null) {
		return;
	}
	if (__hourglassJDialog == null) {
		JGUIUtil.setWaitCursor(__hourglassJFrame, hourglassEnabled);
	}
	else {
		JGUIUtil.setWaitCursor(__hourglassJDialog, hourglassEnabled);
	}
}

/**
Selects whether to show the header on the JWorksheet or not.  This method
currently only works if the JWorksheet is in a JScrollPane.
@param show whether to show the header or not.  If the header is already showing
and it is set to be shown, nothing changes.  Otherwise, it is hidden and
no longer shows.  If the header is hidden and then showColumnHeader(true) is 
called, the header (which is stored internally when hidden) will be put back in place.
*/
public void showColumnHeader(boolean show) {
	Container p = getParent();
	if (p instanceof JViewport) {
		Container gp = p.getParent();
		if (gp instanceof JScrollPane) {			
			JScrollPane scrollPane = (JScrollPane)gp;
			if (!show) {
				__columnHeaderView = scrollPane.getColumnHeader();
				scrollPane.setColumnHeader(null);
			}
			else {
				if (__columnHeaderView != null) {
					scrollPane.setColumnHeader(	__columnHeaderView);
					__columnHeaderView = null;
				}
			}
		}
	}
}

/**
Sorts a column in a given sort order.  Missing data is handled as follows:
Missing data is initially handled by the cell renderer, which will
paint an empty string ("") instead of -999 or -999.00, etc.  When the empty
string is attempted to be turned into a Double or an Integer, an exception is
thrown and caught, and the appropriate DMIUtil.MISSING value is placed in the
list to be sorted, so missing data will sort as much lower than other data.
@param order the order in which to sort the column, either SORT_ASCENDING or 
SORT_DESCENDING as defined in StringUtil.
*/
private void sortColumn(int order) {
	// clear out the sorted order for the table model if one has 
	// already been generated by a previous sort.	
	((JWorksheet_AbstractTableModel)getModel()).setSortedOrder(null);
	
	int size = getRowCount();
	int[] sortOrder = new int[size];	

	int absColumn = getAbsoluteColumn(__popupColumn);

	// Sort numbers with MathUtil.sort()	
	int exceptionCount = 0;
	if (getColumnClass(absColumn) == Integer.class) {
		int[] unsorted = new int[size];		
		Integer I = null;
		for (int i = 0; i < size; i++) {
			try {
				I = (Integer)getValueAt(i, __popupColumn);
				unsorted[i] = I.intValue();
			}
			catch (Exception e) {
				++exceptionCount;
				if ( exceptionCount < 10 ) {
					Message.printWarning(3,"","Exception getting data for sort:");
					Message.printWarning(3,"",e);
				}
				unsorted[i] = DMIUtil.MISSING_INT;
			}
		}
		MathUtil.sort(unsorted, MathUtil.SORT_QUICK, order, sortOrder, true);
	}
	else if (getColumnClass(absColumn) == Long.class) {
        long[] unsorted = new long[size];     
        Long l = null;
        for (int i = 0; i < size; i++) {
            try {
                l = (Long)getValueAt(i, __popupColumn);
                unsorted[i] = l.longValue();
            }
            catch (Exception e) {
				++exceptionCount;
				if ( exceptionCount < 10 ) {
					Message.printWarning(3,"","Exception getting data for sort:");
					Message.printWarning(3,"",e);
				}
                unsorted[i] = DMIUtil.MISSING_LONG;
            }
        }
        MathUtil.sort(unsorted, MathUtil.SORT_QUICK, order, sortOrder, true);
    }
	else if ( (getColumnClass(absColumn) == Double.class) || (getColumnClass(absColumn) == Float.class) ) {
		// Sort numbers with MathUtil.sort()
		// Treat Float as Double since sort method does not handle float[]
		double[] unsorted = new double[size];
		Object o = null;
		for (int i = 0; i < size; i++) {
			try {
				o = getValueAt(i, __popupColumn);
				if ( o == null ) {
					unsorted[i] = DMIUtil.MISSING_DOUBLE;
				}
				else if ( o instanceof Double ) {
					unsorted[i] = ((Double)o).doubleValue();
				}
				else {
					unsorted[i] = ((Float)o).doubleValue();
				}
			}
			catch (Exception e) {
				++exceptionCount;
				if ( exceptionCount < 10 ) {
					Message.printWarning(3,"","Exception getting data for sort:");
					Message.printWarning(3,"",e);
				}
				unsorted[i] = DMIUtil.MISSING_DOUBLE;
			}
		}
		MathUtil.sort(unsorted, MathUtil.SORT_QUICK, order, sortOrder, true);		
	}
	// Sort Dates by turning them into Strings first and sorting with StringUtil.sort()
	else if (getColumnClass(absColumn) == Date.class) {
	    // Since sorting by dates, handle the dates generically.  This allows Date and DateTime to be used
		List v = new Vector(size);
		Object o = null;
		for (int i = 0; i < size; i++) {
			//d = (Date)
		    o = getValueAt(i, __popupColumn);
			if (o == null) {
				v.add("");
			}
			else {
				v.add("" + o);
			}
		}
		StringUtil.sortStringList(v, order, sortOrder, true, true);
	}
	// sort booleans by converting to numbers and sorting with quick sort.
	// trues are turned into -1s and falses into 0s so that trues sort
	// to the top when doing sort ascending, like in Microsoft Access.
	else if (getColumnClass(absColumn) == Boolean.class) {
		int[] unsorted = new int[size];
		Boolean B = null;
		for (int i = 0; i < size; i++) {
			try {
				B = (Boolean)getValueAt(i, __popupColumn);
				if (B.booleanValue()) {
					unsorted[i] = -1;
				}
				else {
					unsorted[i] = 0;
				}
			}
			catch (Exception e) {
				++exceptionCount;
				if ( exceptionCount < 10 ) {
					Message.printWarning(3,"","Exception getting data for sort:");
					Message.printWarning(3,"",e);
				}
				unsorted[i] = DMIUtil.MISSING_INT;
			}
		}
		MathUtil.sort(unsorted, MathUtil.SORT_QUICK, order, sortOrder, true);		
	}		
	// Sort Strings with StringUtil.sort()
	else {
		List v = new ArrayList(size);
		Object o = null;	
		for (int i = 0; i < size; i++) {
			o = getValueAt(i, __popupColumn);
			if (o == null) {
				v.add("");
			} 
			else {
				if ( o instanceof String ) {
					v.add(o);
				}
				else {
					v.add("" + o);
				}
			}
		}
		StringUtil.sortStringList(v, order, sortOrder, true, true);
	}
	// Set the sorted order into the table model ...
	((JWorksheet_AbstractTableModel)getModel()).setSortedOrder(sortOrder);
	// ... and force a redraw on the table model.
	((JWorksheet_AbstractTableModel)getModel()).fireTableDataChanged();	

	__cancelMenuItem.setEnabled(true);
}

/**
Sets up the table model to be prepared to do a consecutive row read.<p>
Some table models may store data (e.g., time series dates), in which 
data values are calculated based on the previous row's data.  In this case,
this method can be used to let them know that a consecutive read of the table
data will be done, and that everytime a call is made to getValueAt() in the 
table model, the row parameter is guaranteed to be the same row as the last 
time getValueAt() was called (if the column is different), or 1 more than the previous row.
*/
public void startNewConsecutiveRead() {
	((JWorksheet_AbstractTableModel)getModel()).startNewConsecutiveRead();
}

/**
Programmatically stops any cell editing that is taking place.  Cell editing
happens when a user double-clicks in a cell or begins typing in a cell.  
This method will stop the editing and WILL accept the data the user has
entered up to this method call.  To abort saving the data the user has already entered, use cancelEditing().
@return true if the editing was stopped, false if it wasn't (because there were errors in the data).
*/
public boolean stopEditing() {
	if (getCellEditor() != null) {
		getCellEditor().stopCellEditing();
		if (getCellEditor() == null) {
			return true;
		}
		return false;
	}
	return true;
}

/**
Returns whether test code is turned on for the worksheet.  If testing is on, 
then new code (with possibly undesirable side-effects) may be run.  This should
never be true in publicly-released code.
@return whether test code is turned on for the worksheet.
*/
public boolean testing() {
	return __testing;
}

/**
Sets whether test code is turned on for the worksheet.  If testing is on,
then new code (with possibly undesirable side-effects) may be run.  This should
never be true in publicly-released code.
@return whether test code is turned on for the worksheet.
*/
public void testing(boolean testing) {
	__testing = testing;
}

/**
Writes the table as HTML out to a file and closes it.
@param filename the name of the file to write.
@throws Exception if an error occurs.
*/
public void writeAsHTML(String filename) 
throws Exception {
	writeAsHTML(filename, 0, getRowCount() - 1);
}

/**
Writes the specified rows as HTML out to a file and closes it.  
@param filename the name of the file to write.
@param firstRow the first row to start writing.
@param lastRow the last row to be written.
@throws Exception if an error occurs.
*/
public void writeAsHTML(String filename, int firstRow, int lastRow) 
throws Exception {
	createHTML(null, filename, firstRow, lastRow);
}

/**
Writes the table out as HTML to an already-created HTMLWriter.  If the 
HTMLWriter is writing to a file, the file is not closed after the table is written.
@param htmlWriter the HTMLWriter object to which to write the table.
@throws Exception if an error occurs.
*/
public void writeAsHTML(HTMLWriter htmlWriter) 
throws Exception {
	writeAsHTML(htmlWriter, 0, getRowCount() - 1);
}

/**
Writes the specified rows as HTML out to an already-created HTMLWriter.  If the
HTMLWriter is writing to a file, the file is not closed after the table is written.
@param htmlWriter the HTMLWriter object to which to write the table.
@param firstRow the first row to start writing.
@param lastRow the last row to be written.
@throws Exception if an error occurs.
*/
public void writeAsHTML(HTMLWriter htmlWriter, int firstRow, int lastRow) 
throws Exception {
	createHTML(htmlWriter, null, firstRow, lastRow);
}

}

// TODO (JTS - 2004-02-12 something to set cell background colors on the table as a whole?
// TODO document getColumnCount() -- abs or vis?

// TODO (JTS - 2005-10-19) something so that if the row header is clicked on, the entire row is selected
