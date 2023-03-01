// TSProductFrame - view to display a list of TSProduct properties

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

package RTi.GRTS;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import RTi.GR.GRAxisDirectionType;
import RTi.GR.GRColor;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;
import RTi.GRTS.TSProductAnnotationProvider;
import RTi.TS.TSUtil;
import RTi.Util.GUI.DragAndDropListener;
import RTi.Util.GUI.DragAndDropSimpleJComboBox;
import RTi.Util.GUI.DragAndDropUtil;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.ReportJFrame;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeUtil;

/**
The TSProductJFrame displays properties for a TSProduct, specifically as used by the TSViewJComponent.
Currently this is only available from the TSViewGraphJFrame and lets the user view and modify graph TSProduct properties.
An attempt is made to track when a property changes from the previous value to avoid redraws and check user-modified properties
(need to enhance PropList to track status of whether a property is read from a file/database,
defaulted at run-time, or set by the user).  The layout of the interface is as follows:

<pre>
--------------------------------------------------------------------------
| ------------------ --------------------------------------------------- |
| | Graph layout   | |                                                 | |
| | panel          | |    Product properties (_product_JTabbedPane)    | |
| |                | |                                                 | |
| |(_layout_JPanel)| |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| |                | |                                                 | |
| ------------------ --------------------------------------------------- |
| ---------------------------------------------------------------------- |
| |                                                                    | |
| |                    Subproduct properties (_subproduct_JTabbedPane) | |
| |                                                                    | |
| ---------------------------------------------------------------------- |
| ---------------------------------------------------------------------- |
| |                     Graph properties (_graph_JTabbedPane)          | |
| | Also have summary and table properties later in an interchangeable | |
| | JTabbedPane?                                                       | |
| ---------------------------------------------------------------------- |
| ---------------------------------------------------------------------- |
| |                      Window buttons (_window_JPanel)               | |
| ---------------------------------------------------------------------- |
--------------------------------------------------------------------------
</pre>

The logic is as follows:
<ol>
<li>	Create the interface and populate with available options using:
	createLayoutJPanel(), createProductJPanel(), createSubproductJPanel(),
	createDataJPanel()</li>
<li>	Display properties for the project in the available controls using:
	displayProductProperties(), displaySubproductProperties(),
	displayDataProperties().</li>
<li>	Interface event handling.</li>
<li>	When applying or saving, transfer the properties in the controls using
	updateTSProduct().</li>
</ol>
*/
@SuppressWarnings("serial")
public class TSProductJFrame extends JFrame
implements ActionListener, ChangeListener, ItemListener, KeyListener,
WindowListener, DragAndDropListener
{

// Private data...

/**
TSViewJFrame parent
*/
private TSViewJFrame _tsview_gui;

/**
Time Series Product definition, from TSGraphJComponent.getTSProduct()
*/
private TSProduct _tsproduct;

private TSProductLayoutJComponent __layoutCanvas;

private SimpleJButton _apply_JButton = null;
private SimpleJButton _close_JButton = null;

/**
The JFrame above which warnings will be displayed.
*/
private JFrame __graphJFrame = null;

/**
Whether the GUI is ALMOST fully initialized or not yet. If the GUI is not yet fully initialized,
then any calls to a SimpleJComboBox add() method will result in the ItemListener's itemStateChanged() method being called.
This was throwing null-pointer exceptions because itemStateChanged() depends on the GUI being fully initialized.
If __is_initialized is false, itemStateChanged() will return without doing anything.
*/
private boolean __is_initialized = false;

/**
Whether some basic setup has been completed and graph types can be limited in the combo box now or not.
TODO (JTS - 2004-05-25) see if can use __is_initialized instead
*/
private boolean __limitGraphTypes = false;

// Shared data.

private int _insets_edge = 1;

private Insets _insetsTLBR = new Insets(_insets_edge, _insets_edge, _insets_edge, _insets_edge );
/**
Subproduct that is selected (zero index).
*/
private int _selected_subproduct = -1;

/**
Time series that is selected (zero index).
*/
private int _selected_data = -1;

/**
The number of dirty properties.
This is incremented as properties are set with calls to updateTSProduct() and is cleared if the display is updated.
Properties need to be updated if subproducts or time series are selected.
Otherwise only the last changes shown will be saved.
*/
private int _ndirty = 0;

// The following panels are listed in the general order of the interface.

// Product panel.

private JTabbedPane _product_JTabbedPane = null;
private JCheckBox _product_enabled_JCheckBox = null;

private JTextField __product_id_JTextField = null;
private JTextField __product_name_JTextField = null;

private JTextField _product_maintitle_JTextField = null;
private SimpleJComboBox _product_maintitle_fontname_JComboBox = null;
private SimpleJComboBox _product_maintitle_fontstyle_JComboBox = null;
private JTextField _product_maintitle_fontsize_JTextField = null;

private JTextField _product_subtitle_JTextField = null;
private SimpleJComboBox _product_subtitle_fontname_JComboBox = null;
private SimpleJComboBox _product_subtitle_fontstyle_JComboBox = null;
private JTextField _product_subtitle_fontsize_JTextField = null;

// Subproduct (Graph) panel.

private SimpleJComboBox _graph_JComboBox = null;
private JTabbedPane _graph_JTabbedPane = null;
private JCheckBox _graph_enabled_JCheckBox = null;
private JTextField _graphSelectedTimeSeriesLineWidth_JTextField = null;
private JCheckBox _graph_isref_JCheckBox = null;
// Left y-axis (note this uses general name without "lefty" for historical purposes when right y-axis was not supported).
private SimpleJComboBox _graph_lefty_graphtype_JComboBox = null;
private JLabel _graph_barposition_JLabel = null;
private SimpleJComboBox _graph_barposition_JComboBox = null;
private JLabel _graph_barOverlap_JLabel = null;
private SimpleJComboBox _graph_barOverlap_JComboBox = null;

// Right-axis.
private SimpleJComboBox _graph_righty_graphtype_JComboBox = null;
private JLabel _graph_righty_barposition_JLabel = null;
private SimpleJComboBox _graph_righty_barposition_JComboBox = null;
private JLabel _graph_righty_barOverlap_JLabel = null;
private SimpleJComboBox _graph_righty_barOverlap_JComboBox = null;

private JTextField _graph_maintitle_JTextField = null;
private SimpleJComboBox _graph_maintitle_fontname_JComboBox = null;
private SimpleJComboBox _graph_maintitle_fontstyle_JComboBox = null;
private JTextField _graph_maintitle_fontsize_JTextField = null;

private JTextField _graph_subtitle_JTextField = null;
private SimpleJComboBox _graph_subtitle_fontname_JComboBox = null;
private SimpleJComboBox _graph_subtitle_fontstyle_JComboBox = null;
private JTextField _graph_subtitle_fontsize_JTextField = null;

private SimpleJComboBox _graph_bottomx_label_fontname_JComboBox = null;
private SimpleJComboBox _graph_bottomx_label_fontstyle_JComboBox = null;
private JTextField _graph_bottomx_label_fontsize_JTextField = null;

private JTextField _graph_bottomx_title_JTextField = null;
private SimpleJComboBox _graph_bottomx_title_fontname_JComboBox = null;
private SimpleJComboBox _graph_bottomx_title_fontstyle_JComboBox = null;
private JTextField _graph_bottomx_title_fontsize_JTextField = null;
private JTextField _graph_bottomx_majorgrid_color_JTextField = null;
private SimpleJComboBox _graph_bottomx_majorgrid_color_JComboBox = null;
private SimpleJButton _graph_bottomx_majorgrid_color_JButton = null;

private SimpleJComboBox _graph_lefty_label_fontname_JComboBox = null;
private SimpleJComboBox _graph_lefty_label_fontstyle_JComboBox = null;
private JTextField _graph_lefty_label_fontsize_JTextField = null;
private JTextField _graph_lefty_precision_JTextField = null;
private SimpleJComboBox _graph_lefty_type_JComboBox = null;
private SimpleJComboBox _graph_lefty_min_JComboBox = null;
private SimpleJComboBox _graph_lefty_max_JComboBox = null;
private JCheckBox _graph_lefty_ignoreunits_JCheckBox = null;
private JTextField _graph_lefty_units_JTextField = null;
private SimpleJComboBox _graph_lefty_direction_JComboBox = null;

private JTextField _graph_lefty_title_JTextField = null;
private SimpleJComboBox _graph_lefty_title_fontname_JComboBox = null;
private SimpleJComboBox _graph_lefty_title_fontstyle_JComboBox = null;
private JTextField _graph_lefty_title_fontsize_JTextField = null;
private SimpleJComboBox _graph_lefty_title_position_JComboBox = null;
private JTextField _graph_lefty_title_rotation_JTextField = null;

private SimpleJComboBox _graph_righty_label_fontname_JComboBox = null;
private SimpleJComboBox _graph_righty_label_fontstyle_JComboBox = null;
private JTextField _graph_righty_label_fontsize_JTextField = null;
private JTextField _graph_righty_precision_JTextField = null;
private SimpleJComboBox _graph_righty_type_JComboBox = null;
private SimpleJComboBox _graph_righty_min_JComboBox = null;
private SimpleJComboBox _graph_righty_max_JComboBox = null;
private JCheckBox _graph_righty_ignoreunits_JCheckBox = null;
private JTextField _graph_righty_units_JTextField = null;
private SimpleJComboBox _graph_righty_direction_JComboBox = null;

private JTextField _graph_lefty_majorgrid_color_JTextField = null;
private SimpleJComboBox _graph_lefty_majorgrid_color_JComboBox = null;
private SimpleJButton _graph_lefty_majorgrid_color_JButton = null;

private JTextField _graph_lefty_majortick_color_JTextField = null;
private SimpleJComboBox _graph_lefty_majortick_color_JComboBox = null;
private SimpleJButton _graph_lefty_majortick_color_JButton = null;

private JTextField _graph_righty_majorgrid_color_JTextField = null;
private SimpleJComboBox _graph_righty_majorgrid_color_JComboBox = null;
private SimpleJButton _graph_righty_majorgrid_color_JButton = null;

private JTextField _graph_righty_majortick_color_JTextField = null;
private SimpleJComboBox _graph_righty_majortick_color_JComboBox = null;
private SimpleJButton _graph_righty_majortick_color_JButton = null;

private JTextField _graph_righty_title_JTextField = null;
private SimpleJComboBox _graph_righty_title_fontname_JComboBox = null;
private SimpleJComboBox _graph_righty_title_fontstyle_JComboBox = null;
private JTextField _graph_righty_title_fontsize_JTextField = null;
private SimpleJComboBox _graph_righty_title_position_JComboBox = null;
private JTextField _graph_righty_title_rotation_JTextField = null;

private JTextField _graph_datalabelformat_JTextField = null;
private SimpleJComboBox _graph_datalabelformat_JComboBox = null;
private SimpleJComboBox _graph_datalabelposition_JComboBox = null;
private SimpleJComboBox _graph_datalabelfontname_JComboBox = null;
private SimpleJComboBox _graph_datalabelfontstyle_JComboBox = null;
private JTextField _graph_datalabelfontsize_JTextField = null;

private JTextField _graph_legendformat_JTextField = null;
private SimpleJComboBox _graph_legendformat_JComboBox = null;
private SimpleJComboBox _graph_lefty_legendposition_JComboBox = null;
private SimpleJComboBox _graph_righty_legendposition_JComboBox = null;
private SimpleJComboBox _graph_legendfontname_JComboBox = null;
private SimpleJComboBox _graph_legendfontstyle_JComboBox = null;
private JTextField _graph_legend_fontsize_JTextField = null;

private JCheckBox _graph_zoomenabled_JCheckBox = null;
private JTextField _graph_zoomgroup_JTextField = null;

private JPanel _graph_analysis_JPanel = null;
private JPanel _graph_blank_analysis_JPanel = null;
private JPanel _blank_analysis_JPanel = null;
private JPanel _xyscatter_analysis_JPanel = null;
private JTextField _xyscatter_analysis_intercept_JTextField = null;
private SimpleJComboBox _xyscatter_analysis_method_JComboBox = null;
private SimpleJComboBox _xyscatter_analysis_transform_JComboBox = null;
private SimpleJComboBox _xyscatter_analysis_neqn_JComboBox = null;
private JTextField _xyscatter_analysis_month_JTextField = null;
private JTextField _dep_analysis_period_start_JTextField = null;
private JTextField _dep_analysis_period_end_JTextField = null;
private JTextField _ind_analysis_period_start_JTextField = null;
private JTextField _ind_analysis_period_end_JTextField = null;
private JCheckBox _xyscatter_analysis_fill_JCheckBox = null;
private JTextField _xyscatter_analysis_fill_period_start_JTextField = null;
private JTextField _xyscatter_analysis_fill_period_end_JTextField = null;

// Annotation part of the subproduct.

private SimpleJComboBox __annotation_JComboBox = null;
private int __selectedAnnotationIndex = -1;	// The currently-selected annotation.
private JTextField __annotation_id_JTextField = null;
private JTextField __annotation_tableid_JTextField = null;
private SimpleJComboBox __annotation_ShapeType_JComboBox = null;
private SimpleJComboBox __annotation_Order_JComboBox = null;
private SimpleJComboBox __annotation_XAxisSystem_JComboBox = null;
private SimpleJComboBox __annotation_YAxis_JComboBox = null;
private SimpleJComboBox __annotation_YAxisSystem_JComboBox = null;

private JPanel __annotation_line_JPanel = null;
private SimpleJComboBox __annotationLineStyleJComboBox = null;
private JTextField __annotation_line_LineWidth_JTextField = null;
private JTextField __annotation_line_PointX1_JTextField = null;
private JTextField __annotation_line_PointY1_JTextField = null;
private JTextField __annotation_line_PointX2_JTextField = null;
private JTextField __annotation_line_PointY2_JTextField = null;
private JTextField __annotation_line_color_JTextField = null;
private SimpleJComboBox __annotation_line_color_JComboBox = null;

private JPanel __annotation_rectangle_JPanel = null;
// TODO SAM 2016-10-15 Evaluate whether to add transparency, fill pattern, etc.
private JTextField __annotation_rectangle_PointX1_JTextField = null;
private JTextField __annotation_rectangle_PointY1_JTextField = null;
private JTextField __annotation_rectangle_PointX2_JTextField = null;
private JTextField __annotation_rectangle_PointY2_JTextField = null;
private JTextField __annotation_rectangle_color_JTextField = null;
private SimpleJComboBox __annotation_rectangle_color_JComboBox = null;

private JPanel __annotation_symbol_JPanel = null;
private JTextField __annotation_symbol_color_JTextField = null;
private SimpleJComboBox __annotation_symbol_color_JComboBox = null;
private JTextField __annotation_symbol_PointX_JTextField = null;
private JTextField __annotation_symbol_PointY_JTextField = null;
private SimpleJComboBox __annotation_symbol_SymbolPosition_JComboBox = null;
private SimpleJComboBox __annotation_symbol_SymbolStyle_JComboBox = null;
private SimpleJComboBox __annotation_symbol_SymbolSize_JComboBox = null;

private JPanel __annotation_text_JPanel = null;
private JTextField __annotation_text_Text_JTextField = null;
private JTextField __annotation_text_PointX_JTextField = null;
private JTextField __annotation_text_PointY_JTextField = null;
private SimpleJComboBox __annotation_text_Position_JComboBox = null;
private SimpleJComboBox __annotation_text_FontName_JComboBox = null;
private SimpleJComboBox __annotation_text_FontStyle_JComboBox = null;
private JTextField __annotation_text_FontSize_JTextField = null;
private JTextField __annotation_text_color_JTextField = null;
private SimpleJComboBox __annotation_text_color_JComboBox = null;

// Data (Time Series) JPanel.

private DragAndDropSimpleJComboBox __ts_JComboBox = null;
private JTabbedPane _ts_JTabbedPane = null;

private JCheckBox _ts_enabled_JCheckBox = null;

private SimpleJComboBox _ts_graphtype_JComboBox = null;	// Enable later.
private JCheckBox _ts_regressionline_JCheckBox = null;

private SimpleJComboBox _ts_xaxis_JComboBox = null;
private SimpleJComboBox _ts_yaxis_JComboBox = null;

private SimpleJComboBox _ts_linestyle_JComboBox = null;
private SimpleJComboBox _ts_linewidth_JComboBox = null;
private SimpleJComboBox _ts_symbolstyle_JComboBox = null;
private SimpleJComboBox _ts_symbolsize_JComboBox = null;
private SimpleJComboBox _ts_flaggedDataSymbolStyle_JComboBox = null;
private JTextField _ts_color_JTextField = null;
private SimpleJComboBox _ts_color_JComboBox = null;
private SimpleJButton _ts_color_JButton = null;

private SimpleJComboBox _ts_datalabelposition_JComboBox = null;
private SimpleJComboBox _ts_datalabelformat_JComboBox = null;
private JTextField _ts_datalabelformat_JTextField = null;

private JTextField _ts_legendformat_JTextField = null;
private SimpleJComboBox _ts_legendformat_JComboBox = null;

private JPanel _ts_analysis_JPanel = null;
private JPanel _ts_blank_analysis_JPanel = null;
private JPanel _ts_xyscatter_analysis_JPanel = null;
private SimpleJComboBox _ts_confidenceinterval_JComboBox = null;

/**
Button labels for the annotation panel.
*/
private final String
	__BUTTON_ADD_ANNOTATION = "Add Annotation",
	__BUTTON_DEL_ANNOTATION = "Delete Annotation",
	__BUTTON_ANNOTATION_UP = "^",
	__BUTTON_ANNOTATION_DOWN = "v";

private JButton
	__addAnnotationJButton,
	__delAnnotationJButton,
	__moveAnnotationUpJButton,
	__moveAnnotationDownJButton;

/**
Whether to ignore a state change when certain modifications are made to data in a combo box.
*/
private boolean __ignoreItemStateChange = false;

////////////////////////////////////////////
// Layout GUI items
/**
The text field that determines the percentage of Y area that each graph should take up.
*/
protected JTextField _yPercentJTextField = null;

/**
The combo box that defines how the graphs are laid out.
*/
protected SimpleJComboBox _layoutTypeJComboBox = null;

/**
The text field that determines the number of columns of graphs to show.
Currently, only one column of graphs is supported.
*/
protected JTextField _numberColsJTextField = null;

/**
The text field that determines the number of rows of graphs to show.
*/
protected JTextField _numberRowsJTextField = null;

/**
The combo box that defines whether drawing area outlines should be shown, used by developers.
*/
protected SimpleJComboBox _developerShowDrawingAreaOutlineJComboBox = null;

/**
 * Button that causes full list of product properties to be shown.
 */
protected SimpleJButton _developerShowProductProperties_JButton = null;

/**
 * Button that causes full list of drawing area properties to be shown.
 */
protected SimpleJButton _developerShowDrawingAreaProperties_JButton = null;

/**
The text field for original left y-axis graph type, used by developers.
*/
protected JTextField _graphLeftYAxisOriginalGraphType_JTextField = null;

/**
The text field for original right y-axis graph type when graph window is first opened, used by developers.
*/
protected JTextField _graphRightYAxisOriginalGraphType_JTextField = null;

/**
Combo boxes for choosing the annotation providers for the graphs.
*/
private SimpleJComboBox __graphAnnotationProvider = null;

/**
The annotation providers that are available to be used in the product.
Annotation providers are actually names of providers.
*/
private List<String> __annotationProviders = null;

public final static String NO_GRAPHS_DEFINED = "No Graphs Defined";

/**
Construct a TSProductJFrame.
@param tsview_gui The main TSViewJFrame that manages the view windows.
@param tsgraphcanvas TSGraphJComponent associated with the properties
(can get the list of time series and TSProduct from this object).
*/
public TSProductJFrame (TSViewJFrame tsview_gui, TSGraphJComponent tsgraphcanvas)
throws Exception {
	this(tsview_gui, tsgraphcanvas, true);
}

/**
Construct a TSProductJFrame.
@param tsview_gui The main TSViewJFrame that manages the view windows.
@param tsgraphcanvas TSGraphJComponent associated with the properties
(can get the list of time series and TSProduct from this object).
@param visible whether this GUI is to be visible once created.
*/
public TSProductJFrame (TSViewJFrame tsview_gui, TSGraphJComponent tsgraphcanvas, boolean visible)
throws Exception {
	super("Time Series - Graph Properties");
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	initialize(tsview_gui, tsgraphcanvas, visible);
}

/**
Handle action events (button press, etc.)
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e ) {
	checkGUIState();
	// Check the names of the events (tied to menu names) or object instances.

	String command = e.getActionCommand();
	Object source = e.getSource();
	if ( command.equals("TSProductJFrame.Apply") ) {
		applyClicked();
	}
	else if ( command.equals("TSProductJFrame.Cancel") ) {
		// Close the GUI via the parent.
		_tsview_gui.closeGUI(TSViewType.PROPERTIES);
	}
	else if ( command.equals("TSProductJFrame.Close") ) {
		if (!checkUserInput()) {
			return;
		}
		if ( updateTSProduct () > 0 ) {
			// Cause a redraw of the displays.
			_tsview_gui.refresh ();
		}
		// Close the GUI via the parent.
		_tsview_gui.closeGUI(TSViewType.PROPERTIES);
	}
	else if (command.equals(__BUTTON_ADD_ANNOTATION)) {
		if (_selected_subproduct == -1) {
			addAnnotation(_selected_subproduct, 0);
		}
		int num = _tsproduct.getNumAnnotations(_selected_subproduct);
		addAnnotation(_selected_subproduct, num);
	}
	else if (command.equals(__BUTTON_DEL_ANNOTATION)) {
		updateTSProduct();
		if (__selectedAnnotationIndex == -1) {
			return;
		}
		int num = _tsproduct.getNumAnnotations(_selected_subproduct);
		_tsproduct.removeAnnotation(_selected_subproduct, __selectedAnnotationIndex);

		__ignoreItemStateChange = true;
		__annotation_JComboBox.removeAll();
		for (int i = 0; i < num - 1; i++) {
			__annotation_JComboBox.add("" + _tsproduct.getLayeredPropValue("AnnotationID",
				_selected_subproduct, i, false, true));
		}
		__ignoreItemStateChange = false;
		__selectedAnnotationIndex = 0;
		if (num > 1) {
			displayAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);
		}
		else {
			clearAnnotationProperties();
			__selectedAnnotationIndex = -1;
		}
	}
	else if (command.equals(__BUTTON_ANNOTATION_UP)) {
		updateTSProduct();

		if (__selectedAnnotationIndex == -1 || __selectedAnnotationIndex == 0) {
			return;
		}

		_tsproduct.swapAnnotations(_selected_subproduct, __selectedAnnotationIndex, _selected_subproduct,
			__selectedAnnotationIndex - 1);
		__ignoreItemStateChange = true;

		String s = (String)__annotation_JComboBox.getItemAt(__selectedAnnotationIndex);
		__annotation_JComboBox.removeItemAt(__selectedAnnotationIndex);
		__annotation_JComboBox.insertItemAt(s,__selectedAnnotationIndex - 1);
		__annotation_JComboBox.select(__selectedAnnotationIndex - 1);
		__ignoreItemStateChange = false;
		__selectedAnnotationIndex--;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);
	}
	else if (command.equals(__BUTTON_ANNOTATION_DOWN)) {
		updateTSProduct();

		int num = _tsproduct.getNumAnnotations(_selected_subproduct);

		if ( (__selectedAnnotationIndex == -1) || (__selectedAnnotationIndex == (num - 1)) ) {
			return;
		}
		_tsproduct.swapAnnotations(_selected_subproduct,
			__selectedAnnotationIndex, _selected_subproduct, __selectedAnnotationIndex + 1);
		__ignoreItemStateChange = true;
		String s = (String)__annotation_JComboBox.getItemAt(__selectedAnnotationIndex);
		__annotation_JComboBox.removeItemAt(__selectedAnnotationIndex);
		__annotation_JComboBox.insertItemAt(s,__selectedAnnotationIndex + 1);
		__annotation_JComboBox.select(__selectedAnnotationIndex + 1);
		__selectedAnnotationIndex++;
		__ignoreItemStateChange = false;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);
	}
	else if ( source == _developerShowProductProperties_JButton ) {
		displayProductPropertiesText ();
	}
	else if ( source == _developerShowDrawingAreaProperties_JButton ) {
		displayDrawingAreaProperties ();
	}

	/* TODO SAM - need to update to new.
	else if ( command.equals("TSProductJFrame.DisableAll") ) {
		// Disable all time series in the _enabledts_List.
		int size = _enabledts_List.getItemCount();
		for ( int i = 0; i < size; i++ ) {
			ts = tslist.elementAt(i);
			if ( ts != null ) {
				_enabledts_List.deselect(i);
				ts.setEnabled(false);
			}
		}
	}
	else if ( command.equals("TSProductJFrame.EnableAll") ) {
		// Enable all time series in the _enabledts_List.
		int size = _enabledts_List.getItemCount();
		for ( int i = 0; i < size; i++ ) {
			ts = _tslist.elementAt(i);
			if ( ts != null ) {
				_enabledts_List.select(i);
				ts.setEnabled(true);
			}
		}
	}
	else if ( command.equals("TSProductJFrame.FindTimeSeries") ) {
		// Find time series in the enabled list.
		new FindInJListJDialog ( this, _enabledts_List, "Find Time Series" );
	}
	else if ( command.equals("TSView.Properties") ) {
		// Show help...
		URLHelp.showHelpForKey ("TSProductJFrame");
	}
	*/
	checkGUIState();
	command = null;
}

/**
Adds a new annotation to the product.
@param sp the subproduct under which to add the annotation (base 0).
@param iann the number of the annotation to add (base 0).
*/
protected void addAnnotation(int sp, int iann) {
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
	_tsproduct.setPropValue("ShapeType", "Text", sp, iann, true);

	_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
	_tsproduct.checkAnnotationProperties(sp, iann);
	_tsproduct.getPropList().setHowSet(how_set_prev);

	__annotation_JComboBox.add("Annotation " + (iann + 1));
	__annotation_JComboBox.select("Annotation " + (iann + 1));
	checkGUIState();
}

/**
Adds a new data section under the specified subproduct with the specified tsid.
All the values other than TSID will be set to the default values.
@param sp the subproduct under which to put the data.
@param tsid the tsid of the data.
*/
protected void addData(int sp, String tsid) {
	int num = _tsproduct.getNumData(sp);
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
	_tsproduct.setPropValue("TSID", tsid, sp, num);

	_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
	_tsproduct.checkDataProperties(sp, num);
	_tsproduct.getPropList().setHowSet(how_set_prev);
	_tsproduct.setDirty(true);

	__ts_JComboBox.add("" + (num + 1) + " - " + tsid);
	__ts_JComboBox.select(__ts_JComboBox.getItemCount() - 1);
	checkGUIState();
}

/**
Inserts a blank graph into the TSProduct at the given position.
@param pos the index that the subproduct should have in the product proplist (0+).
The other properties will be renumbered appropriately.
*/
protected void addGraph(int pos) {
	int nsub = _tsproduct.getNumSubProducts();
	for (int i = (nsub - 1); i >= pos; i--) {
		_tsproduct.swapSubProducts(i, i + 1);
	}
	// Set a few default settings that all graphs need.
	_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);

	if (pos == -1) {
		// Adding a new graph to a display with no graphs defined.
		pos++;
	}

	_selected_subproduct = pos;
	_selected_data = -1;
	_tsproduct.setDirty(true);
	__selectedAnnotationIndex = -1;
	_tsproduct.setPropValue("Enabled", "true", pos, -1);
	// The original graph type is used to constrain options for changing the graph type.
	// For example, some complex graph types like XY-Scatter that require analysis do not allow changing later through the UI.
	_tsproduct.setPropValue("LeftYAxisOriginalGraphType", "Line", pos, -1);
	_tsproduct.setPropValue("RightYAxisOriginalGraphType", "None", pos, -1);
	clearAllProperties();
	_tsproduct.checkProperties();
	updateTSProduct(Prop.SET_AS_RUNTIME_DEFAULT);
	redisplayProperties();
	checkGUIState();
}

/**
Called when apply is pressed.
*/
protected void applyClicked() {
	if (!checkUserInput()) {
		return;
	}
	int selected = _graph_JComboBox.getSelectedIndex();
	// Update displayed properties to the TSProduct.
	updateTSProduct();
	if ( _ndirty > 0 ) {
		// Cause a redraw of the displays.
		_tsview_gui.refresh ();
		_ndirty = 0;
	}
	//else {
	//    String routine = getClass().getName() + ".applyClicked";
	//    Message.printStatus(2,routine,"Nothing is updated - no need to redraw.");
	//}
	_graph_JComboBox.select(selected);
	getTSViewJFrame().getViewGraphJFrame().getMainJComponent().reinitializeGraphs(_tsproduct);
	if (getTSViewJFrame().getViewGraphJFrame().getReferenceGraph() != null) {
		getTSViewJFrame().getViewGraphJFrame().getReferenceGraph().reinitializeGraphs(_tsproduct);
	}
}

/**
Determines if any graphs are defined.
@return true if any graphs are defined, false if not.
*/
public boolean areGraphsDefined() {
	if (((String)_graph_JComboBox.getItemAt(0)).equals(NO_GRAPHS_DEFINED)) {
		return false;
	}
	return true;
}

/**
Checks the state of the GUI and enables or disables components appropriately.
*/
public void checkGUIState() {
	if (__selectedAnnotationIndex == -1 || _selected_subproduct == -1) {
		JGUIUtil.setEnabled(__delAnnotationJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__delAnnotationJButton, true);
	}

	if (_selected_subproduct == -1) {
		JGUIUtil.setEnabled(__addAnnotationJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__addAnnotationJButton, true);
	}

	if (__selectedAnnotationIndex == 0 || __selectedAnnotationIndex == -1) {
		JGUIUtil.setEnabled(__moveAnnotationUpJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__moveAnnotationUpJButton, true);
	}

	if (__selectedAnnotationIndex == -1 || __selectedAnnotationIndex
		== (_tsproduct.getNumAnnotations(_selected_subproduct) - 1)) {
		JGUIUtil.setEnabled(__moveAnnotationDownJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__moveAnnotationDownJButton, true);
	}

	// Check to see if there are any data defined in the data combo box.
	// If so, enable all the data entry fields.  If not, disable everything.

	if (__ts_JComboBox == null || __ts_JComboBox.getItemCount() == 0) {
		setTimeSeriesFieldsEnabled(false);
	}
	else {
		setTimeSeriesFieldsEnabled(true);
	}

	if (_graph_JComboBox.getItemCount() == 0
		|| _graph_JComboBox.getSelected() == null
		|| _graph_JComboBox.getSelected().equals(NO_GRAPHS_DEFINED)){
		setGraphFieldsEnabled(false);
	}
	else {
		setGraphFieldsEnabled(true);
	}

	// Check to see if there are any annotations in the annotation combo box.
	// If so, enable all the annotation entry fields.  If not, disable everything but the add button.

	if (__annotation_JComboBox == null || __annotation_JComboBox.getItemCount() == 0) {
		setAnnotationFieldsEnabled(false);
	}
	else {
		setAnnotationFieldsEnabled(true);
	}

	// Left y-axis graph type.
	String graphTypeString = _graph_lefty_graphtype_JComboBox.getSelected();
	if (graphTypeString == null) {
		return;
	}
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase(graphTypeString);
	if ( graphType == TSGraphType.AREA || graphType == TSGraphType.AREA_STACKED ||
	    graphType == TSGraphType.BAR || graphType == TSGraphType.LINE ||
	    graphType == TSGraphType.POINT) {
	    _graph_lefty_graphtype_JComboBox.setEnabled(true);
	}
	else {
		_graph_lefty_graphtype_JComboBox.setEnabled(false);
	}

	// Right y-axis graph type.
	String graphTypeRightYAxisString = _graph_righty_graphtype_JComboBox.getSelected();
	if (graphTypeRightYAxisString == null) {
		return;
	}
	TSGraphType graphTypeRightYAxis = TSGraphType.valueOfIgnoreCase(graphTypeString);
	if ( graphTypeRightYAxis == TSGraphType.AREA || graphTypeRightYAxis == TSGraphType.AREA_STACKED ||
		graphTypeRightYAxis == TSGraphType.BAR || graphTypeRightYAxis == TSGraphType.LINE ||
		graphTypeRightYAxis == TSGraphType.POINT) {
	    _graph_righty_graphtype_JComboBox.setEnabled(true);
	}
	else {
		_graph_righty_graphtype_JComboBox.setEnabled(false);
	}

	enableComponentsBasedOnGraphType(_selected_subproduct, _selected_data, false);
}

/**
Checks whether user entered values are valid.
@return true if all the user-entered values valid, false if not.
*/
public boolean checkUserInput() {
	String routine = getClass().getSimpleName() + ".checkUserInput";
	String warning = "";

	String s = __product_id_JTextField.getText().trim();
	if (s.equals("")
		|| (s.indexOf(" ") > -1)
		|| (s.indexOf("'") > -1)
		|| (s.indexOf("-") > -1)) {
		warning += "\nProduct ID must be entered and cannot contain spaces, apostrophes or dashes.";
	}

	s = __product_name_JTextField.getText().trim();
	if (s.indexOf("'") > -1) {
		warning += "\nProduct name cannot contain apostrophes.";
	}

	if (warning.length() > 0) {
		Message.printWarning(1, routine, "Warning: " + warning, __graphJFrame);
		return false;
	}
	else {
		return true;
	}
}

/**
Clears out the properties set in the GUI objects.
This is necessary for instances when all graphs are removed from the GUI or when a graph has no TSIDs.
*/
private void clearAllProperties() {
	clearProductProperties();
	clearSubProductProperties();
	clearDataProperties();
	clearAnnotationProperties();
	checkGUIState();
}

/**
Clears out the annotation properties set in the GUI objects to the defaults, as defined in TSProduct.
Necessary for instances when all graphs are removed from the GUI or a graph has no annotations.
*/
private void clearAnnotationProperties() {
	__annotation_line_color_JComboBox.select(_tsproduct.getDefaultPropValue("Color", 1, 1, true));

	__annotation_text_color_JComboBox.select(_tsproduct.getDefaultPropValue("Color", 1, 1, true));

	if (_selected_subproduct == -1) {
		__annotation_id_JTextField.setText( _tsproduct.getDefaultPropValue("AnnotationID", 0, 0, true));
	}
	else {
		__annotation_id_JTextField.setText( _tsproduct.getDefaultPropValue("AnnotationID",
			_selected_subproduct, _tsproduct.getNumAnnotations(_selected_subproduct), true));
	}

	__annotation_tableid_JTextField.setText( _tsproduct.getDefaultPropValue("AnnotationTableID",
			_selected_subproduct, _tsproduct.getNumAnnotations(_selected_subproduct), true));

	__annotation_Order_JComboBox.select(_tsproduct.getDefaultPropValue("Order", 1, 1, true));

	__annotation_ShapeType_JComboBox.select(0);

	__annotation_XAxisSystem_JComboBox.select(_tsproduct.getDefaultPropValue("XAxisSystem", 1, 1, true));

	__annotation_YAxis_JComboBox.select(_tsproduct.getDefaultPropValue("YAxis", 1, 1, true));

	__annotation_YAxisSystem_JComboBox.select(_tsproduct.getDefaultPropValue("YAxisSystem", 1, 1, true));

	__annotationLineStyleJComboBox.select(_tsproduct.getDefaultPropValue("LineStyle", 1, 1, true));

	__annotation_line_LineWidth_JTextField.setText(	_tsproduct.getDefaultPropValue("LineWidth", 1, 1, true));

	__annotation_line_PointX1_JTextField.setText("0");
	__annotation_line_PointY1_JTextField.setText("0");
	__annotation_line_PointX2_JTextField.setText("0");
	__annotation_line_PointY2_JTextField.setText("0");

	__annotation_text_FontName_JComboBox.select(_tsproduct.getDefaultPropValue("FontName", 1, 1, true));

	__annotation_text_FontStyle_JComboBox.select(_tsproduct.getDefaultPropValue("FontStyle", 1, 1, true));

	__annotation_text_FontSize_JTextField.setText(_tsproduct.getDefaultPropValue("FontSize", 1, 1, true));

	__annotation_text_Text_JTextField.setText(_tsproduct.getDefaultPropValue("Text", 1, 1, true));

	__annotation_text_PointX_JTextField.setText("0");
	__annotation_text_PointY_JTextField.setText("0");

	__annotation_text_Position_JComboBox.select(_tsproduct.getDefaultPropValue("TextPosition", 1, 1, true));

	__annotation_symbol_SymbolSize_JComboBox.select("0");
	__annotation_symbol_SymbolPosition_JComboBox.select(_tsproduct.getDefaultPropValue("SymbolPosition", 1, 1, true));
	__annotation_symbol_SymbolStyle_JComboBox.select(0);
	__annotation_symbol_PointX_JTextField.setText("0");
	__annotation_symbol_PointY_JTextField.setText("0");
	__annotation_symbol_color_JComboBox.select(_tsproduct.getDefaultPropValue("Color", 1, 1, true));
}

/**
Clears out the data properties set in the GUI objects to the defaults, as defined in TSProduct.
Necessary for cases such as when all graphs are removed from the product.
*/
private void clearDataProperties() {
	// Data properties.
	String temp = _tsproduct.getDefaultPropValue("Color", 1, 1);
	_ts_color_JTextField.setText(temp);
	_ts_color_JComboBox.select(temp);
	GRColor color = GRColor.parseColor(temp);
	String graphType = _tsproduct.getDefaultPropValue("GraphType", 1, 1);

	_ts_color_JTextField.setBackground(color);

	_ts_datalabelformat_JTextField.setText(	_tsproduct.getDefaultPropValue("DataLabelFormat", 1, 1));

	_ts_datalabelposition_JComboBox.select(
		_tsproduct.getDefaultPropValue("DataLabelPosition", 1, 1));

	if (_tsproduct.getDefaultPropValue("Enabled", 1, 1).equals("True")) {
		_ts_enabled_JCheckBox.setSelected(true);
	}
	else {
		_ts_enabled_JCheckBox.setSelected(false);
	}

	_ts_legendformat_JTextField.setText(_tsproduct.getDefaultPropValue("LegendFormat", 1, 1));

	_ts_linestyle_JComboBox.select(_tsproduct.getDefaultPropValue("LineStyle", 1, 1));

	if (graphType.equals("Bar")) {
		_ts_linestyle_JComboBox.setEnabled(false);
	}
	else {
		_ts_linestyle_JComboBox.setEnabled(true);
	}

	_ts_linewidth_JComboBox.select(_tsproduct.getDefaultPropValue("LineWidth", 1, 1));

	if (graphType.equals("Bar")) {
		_ts_linewidth_JComboBox.setEnabled(false);
	}
	else {
		_ts_linewidth_JComboBox.setEnabled(true);
	}

	if (_tsproduct.getDefaultPropValue("RegressionLineEnabled", 1, 1).equals("True")) {
		_ts_regressionline_JCheckBox.setSelected(true);
	}
	else {
		_ts_regressionline_JCheckBox.setSelected(false);
	}

	_ts_symbolsize_JComboBox.select(_tsproduct.getDefaultPropValue("SymbolSize", 1, 1));

	if (graphType.equals("Bar")) {
		_ts_symbolsize_JComboBox.setEnabled(false);
	}
	else {
		_ts_symbolsize_JComboBox.setEnabled(false);
	}

	_ts_symbolstyle_JComboBox.select(_tsproduct.getDefaultPropValue("SymbolStyle", 1, 1));

	if (graphType.equals("Bar")) {
		_ts_symbolstyle_JComboBox.setEnabled(false);
	}
	else {
		_ts_symbolstyle_JComboBox.setEnabled(true);
	}

    _ts_flaggedDataSymbolStyle_JComboBox.select(_tsproduct.getDefaultPropValue("FlaggedDataSymbolStyle", 1, 1));

    if (graphType.equals("Bar")) {
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(false);
    }
    else {
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(true);
    }

	_ts_xaxis_JComboBox.select(_tsproduct.getDefaultPropValue("XAxis", 1, 1));

	_ts_confidenceinterval_JComboBox.select(_tsproduct.getDefaultPropValue("YAxis", 1, 1));

	if (_ts_regressionline_JCheckBox.isSelected()) {
		_ts_confidenceinterval_JComboBox.setEnabled(true);
	}
	else {
		_ts_confidenceinterval_JComboBox.setEnabled(false);
	}

	_ts_yaxis_JComboBox.select(_tsproduct.getDefaultPropValue("YAxis", 1, 1));

	if (!graphType.equals("XY-Scatter")) {
		_ts_xyscatter_analysis_JPanel.setVisible(false);
		_ts_blank_analysis_JPanel.setVisible(true);
	}
	else {
		_ts_xyscatter_analysis_JPanel.setVisible(true);
		_ts_blank_analysis_JPanel.setVisible(false);
	}

	__ts_JComboBox.removeAllItems();
}

/**
Clears out graph properties that are not appropriate for the currently-selected graph.
It does this in order that graphs have only their own proper properties when written to persistent storage.
This method is only important if the user changes graph types.
@param subproduct the subproduct (base-zero) for which to clear out the other graph properties than for the one that is selected.
@param ts unused
@param graphTypeLeft the kind of graph that the user selected from the left y-axis graph type combo box (or null if checking right y-axis).
@param graphTypeRight the kind of graph that the user selected from the right y-axis graph type combo box (or null if checking left y-axis).
*/
private void clearGraphProperties(TSProduct tsproduct, int subproduct, int ts, TSGraphType graphTypeLeft, TSGraphType graphTypeRight) {
	String prefix = "SubProduct " + (subproduct + 1) + ".";
	if ( graphTypeLeft != null ) {
		if (graphTypeLeft != TSGraphType.BAR && graphTypeLeft != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			tsproduct.unSet(prefix + "BarPosition");
			tsproduct.unSet(prefix + "BarOverlap");
		}
		else {
			tsproduct.unSet(prefix + "LineStyle");
			tsproduct.unSet(prefix + "LineWidth");
			tsproduct.unSet(prefix + "SymbolSize");
			tsproduct.unSet(prefix + "SymbolStyle");
			tsproduct.unSet(prefix + "FlaggedDataSymbolStyle");
		}

		if (graphTypeLeft == TSGraphType.POINT) {
			tsproduct.unSet(prefix + "LineStyle");
			tsproduct.unSet(prefix + "LineWidth");
		}

		if (graphTypeLeft != TSGraphType.XY_SCATTER
		    && graphTypeLeft != TSGraphType.PREDICTED_VALUE
		    && graphTypeLeft != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			tsproduct.unSet(prefix + "XYScatterAnalyzeForFilling");
			tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodStart");
			tsproduct.unSet(prefix + "XYScatterFillPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterFillPeriodStart");
			tsproduct.unSet(prefix + "XYScatterIndependentAnalysisPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterIndependentAnalsysisPeriodStart");
			tsproduct.unSet(prefix + "XYScatterIntercept");
			tsproduct.unSet(prefix + "XYScatterMethod");
			tsproduct.unSet(prefix + "XYScatterMonth");
			tsproduct.unSet(prefix + "XYScatterNumberOfEquations");
			tsproduct.unSet(prefix + "XYScatterTransformation");
			tsproduct.unSet(prefix + "XYScatterConfidenceInterval");
			tsproduct.unSet(prefix + "RegressionLineEnabled");
		}
	}
	if ( graphTypeRight != null ) {
		if (graphTypeLeft != TSGraphType.BAR && graphTypeLeft != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			tsproduct.unSet(prefix + "RightYAxisBarPosition");
			tsproduct.unSet(prefix + "RightYAxisBarOverlap");
		}
		/* TODO SAM 2016-10-23 Figure out how defaults are dealt with.
		else {
			tsproduct.unSet(prefix + "LineStyle");
			tsproduct.unSet(prefix + "LineWidth");
			tsproduct.unSet(prefix + "SymbolSize");
			tsproduct.unSet(prefix + "SymbolStyle");
			tsproduct.unSet(prefix + "FlaggedDataSymbolStyle");
		}

		if (graphTypeLeft == TSGraphType.POINT) {
			tsproduct.unSet(prefix + "LineStyle");
			tsproduct.unSet(prefix + "LineWidth");
		}

		if (graphTypeLeft != TSGraphType.XY_SCATTER
		    && graphTypeLeft != TSGraphType.PREDICTED_VALUE
		    && graphTypeLeft != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			tsproduct.unSet(prefix + "XYScatterAnalyzeForFilling");
			tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodStart");
			tsproduct.unSet(prefix + "XYScatterFillPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterFillPeriodStart");
			tsproduct.unSet(prefix + "XYScatterIndependentAnalysisPeriodEnd");
			tsproduct.unSet(prefix + "XYScatterIndependentAnalsysisPeriodStart");
			tsproduct.unSet(prefix + "XYScatterIntercept");
			tsproduct.unSet(prefix + "XYScatterMethod");
			tsproduct.unSet(prefix + "XYScatterMonth");
			tsproduct.unSet(prefix + "XYScatterNumberOfEquations");
			tsproduct.unSet(prefix + "XYScatterTransformation");
			tsproduct.unSet(prefix + "XYScatterConfidenceInterval");
			tsproduct.unSet(prefix + "RegressionLineEnabled");
		}
		*/
	}
}

/**
Clears out the product properties set in the GUI objects.
*/
private void clearProductProperties() {
	// Product properties.
	if (_tsproduct.getDefaultPropValue("Enabled", -1, -1).equals("True")) {
		_product_enabled_JCheckBox.setSelected(true);
	}
	else {
		_product_enabled_JCheckBox.setSelected(false);
	}
	_product_maintitle_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleString", -1, -1));
	_product_maintitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontName", -1, -1));
	_product_maintitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontStyle", -1, -1));
	_product_maintitle_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleFontSize", -1, -1));
	_product_subtitle_JTextField.setText( _tsproduct.getDefaultPropValue("SubTitleString", -1, -1));
	_product_subtitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("SubTitleFontName", -1, -1));
	_product_subtitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("SubTitleFontStyle", -1, -1));
	_product_subtitle_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("SubTitleFontSize", -1, -1));
}

/**
Clears out the sub product properties set in the GUI objects.
*/
private void clearSubProductProperties() {
	_yPercentJTextField.setText("");
	// Left y-axis.
	String graphType = _tsproduct.getDefaultPropValue("GraphType", 1, -1);
	_graph_lefty_graphtype_JComboBox.select(graphType);

	if (graphType.equals("Bar")) {
		_graph_barposition_JComboBox.select( _tsproduct.getDefaultPropValue("BarPosition", 1, -1));
		_graph_barposition_JLabel.setVisible(true);
		_graph_barposition_JComboBox.setVisible(true);
		_graph_barOverlap_JLabel.setVisible(true);
        _graph_barOverlap_JComboBox.setVisible(true);
	}
	else {
		_graph_barposition_JLabel.setVisible(false);
		_graph_barposition_JComboBox.setVisible(false);
		_graph_barOverlap_JLabel.setVisible(false);
        _graph_barOverlap_JComboBox.setVisible(false);
	}

	// Right y-axis.
	String graphTypeRight = _tsproduct.getDefaultPropValue("RightYAxisGraphType", 1, -1);
	_graph_righty_graphtype_JComboBox.select(graphTypeRight);

	if (graphTypeRight.equals("Bar")) {
		_graph_righty_barposition_JComboBox.select( _tsproduct.getDefaultPropValue("BarPosition", 1, -1));
		_graph_righty_barposition_JLabel.setVisible(true);
		_graph_righty_barposition_JComboBox.setVisible(true);
		_graph_righty_barOverlap_JLabel.setVisible(true);
        _graph_righty_barOverlap_JComboBox.setVisible(true);
	}
	else {
		_graph_righty_barposition_JLabel.setVisible(false);
		_graph_righty_barposition_JComboBox.setVisible(false);
		_graph_righty_barOverlap_JLabel.setVisible(false);
        _graph_righty_barOverlap_JComboBox.setVisible(false);
	}

	String colorString = _tsproduct.getDefaultPropValue( "BottomXAxisMajorGridColor", 1, -1);
	GRColor color = GRColor.parseColor(colorString);
	_graph_bottomx_majorgrid_color_JTextField.setText(colorString);
	_graph_bottomx_majorgrid_color_JComboBox.select(colorString);
	_graph_bottomx_majorgrid_color_JTextField.setBackground(color);

	_graph_bottomx_title_JTextField.setText(_tsproduct.getDefaultPropValue("BottomXAxisTitleString", 1,-1));

	_graph_bottomx_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("BottomXAxisTitleFontName", 1, -1));

	_graph_bottomx_title_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("BottomXAxisTitleFontStyle", 1, -1));

	_graph_bottomx_title_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("BottomXAxisTitleFontSize", 1, -1));

	_graph_bottomx_label_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("BottomXAxisLabelFontName", 1, -1));

	_graph_bottomx_label_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("BottomXAxisLabelFontStyle", 1, -1));

	_graph_bottomx_label_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("BottomXAxisLabelFontSize", 1, -1));

	_graph_datalabelformat_JTextField.setText( _tsproduct.getDefaultPropValue("DataLabelFormat", 1, -1));

	_graph_datalabelfontname_JComboBox.select( _tsproduct.getDefaultPropValue("DataLabelFontName", 1, -1));

	_graph_datalabelfontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("DataLabelFontStyle", 1, -1));

	_graph_datalabelfontsize_JTextField.setText( _tsproduct.getDefaultPropValue("DataLabelFontSize", 1, -1));

	_graph_datalabelposition_JComboBox.select( _tsproduct.getDefaultPropValue("DataLabelPosition", 1, -1));

	colorString = _tsproduct.getDefaultPropValue("LeftYAxisMajorGridColor", 1, -1);
	color = GRColor.parseColor(colorString);
	_graph_lefty_majorgrid_color_JTextField.setText(colorString);
	_graph_lefty_majorgrid_color_JComboBox.select(colorString);
	_graph_lefty_majorgrid_color_JTextField.setBackground(color);
	colorString = _tsproduct.getDefaultPropValue("LeftYAxisMajorTickColor", 1, -1);
	color = GRColor.parseColor(colorString);
	_graph_lefty_majortick_color_JTextField.setText(colorString);
	_graph_lefty_majortick_color_JComboBox.select(colorString);
	_graph_lefty_majortick_color_JTextField.setBackground(color);
	_graph_lefty_title_JTextField.setText( _tsproduct.getDefaultPropValue("LeftYAxisTitleString", 1, -1));
	_graph_lefty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName", 1, -1));
	_graph_lefty_title_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontStyle", 1, -1));
	_graph_lefty_title_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontSize", 1, -1));
	_graph_lefty_label_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontName", 1, -1));
	_graph_lefty_label_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontStyle", 1, -1));
	_graph_lefty_label_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontSize", 1, -1));
	if (_tsproduct.getDefaultPropValue("LeftYAxisIgnoreUnits", 1, -1).equalsIgnoreCase("False")) {
		_graph_lefty_ignoreunits_JCheckBox.setSelected(false);
	}
	else {
		_graph_lefty_ignoreunits_JCheckBox.setSelected(true);
	}
	_graph_lefty_title_position_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisTitlePosition", 1, -1));
	_graph_lefty_title_rotation_JTextField.setText( _tsproduct.getDefaultPropValue("LeftYAxisTitleRotation", 1, -1));
	_graph_lefty_max_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisMax", 1, -1));
	_graph_lefty_min_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisMin", 1, -1));
	_graph_lefty_type_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisType", 1, -1));
	_graph_lefty_units_JTextField.setText( _tsproduct.getDefaultPropValue("LeftYAxisUnits", 1, -1));
	_graph_lefty_direction_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisDirection", 1, -1));

	colorString = _tsproduct.getDefaultPropValue("RightYAxisMajorGridColor", 1, -1);
	color = GRColor.parseColor(colorString);
	_graph_righty_majorgrid_color_JTextField.setText(colorString);
	_graph_righty_majorgrid_color_JComboBox.select(colorString);
	_graph_righty_majorgrid_color_JTextField.setBackground(color);
	colorString = _tsproduct.getDefaultPropValue("RightYAxisMajorTickColor", 1, -1);
	color = GRColor.parseColor(colorString);
	_graph_righty_majortick_color_JTextField.setText(colorString);
	_graph_righty_majortick_color_JComboBox.select(colorString);
	_graph_righty_majortick_color_JTextField.setBackground(color);
	_graph_righty_title_JTextField.setText( _tsproduct.getDefaultPropValue("RightYAxisTitleString", 1, -1));
	_graph_righty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisTitleFontName", 1, -1));
	_graph_righty_title_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisTitleFontStyle", 1, -1));
	_graph_righty_title_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("RightYAxisTitleFontSize", 1, -1));
	_graph_righty_label_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisLabelFontName", 1, -1));
	_graph_righty_label_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisLabelFontStyle", 1, -1));
	_graph_righty_label_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("RightYAxisLabelFontSize", 1, -1));
	if (_tsproduct.getDefaultPropValue("RightYAxisIgnoreUnits", 1, -1).equalsIgnoreCase("False")) {
		_graph_righty_ignoreunits_JCheckBox.setSelected(false);
	}
	else {
		_graph_righty_ignoreunits_JCheckBox.setSelected(true);
	}
	// TODO SAM 2016-10-17 Figure out why the above selects when default is false.
	_graph_righty_ignoreunits_JCheckBox.setSelected(false);
	_graph_righty_title_position_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisTitlePosition", 1, -1));
	_graph_righty_title_rotation_JTextField.setText( _tsproduct.getDefaultPropValue("RightYAxisTitleRotation", 1, -1));
	_graph_righty_max_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisMax", 1, -1));
	_graph_righty_min_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisMin", 1, -1));
	_graph_righty_type_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisType", 1, -1));
	_graph_righty_units_JTextField.setText( _tsproduct.getDefaultPropValue("RightYAxisUnits", 1, -1));
	_graph_righty_direction_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisDirection", 1, -1));

	if ( __graphAnnotationProvider != null ) {
	    __graphAnnotationProvider.select( _tsproduct.getDefaultPropValue("AnnotationProvider", 1, -1));
	}

	_graph_legendfontname_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontName", 1, -1));

	_graph_legendfontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontStyle", 1, -1));

	_graph_legend_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("LegendFontSize", 1, -1));

	_graph_legendformat_JTextField.setText( _tsproduct.getDefaultPropValue("LegendFormat", 1, -1));

	_graph_lefty_legendposition_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisLegendPosition", 1, -1));
	// TODO sam 2017-02-07 evaluate whether additional effort is needed to transition from legacy property.
	//_graph_lefty_legendposition_JComboBox.select( _tsproduct.getDefaultPropValue("LegendPosition", 1, -1));

	_graph_maintitle_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleString", 1, -1));

	_graph_maintitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontName", 1, -1));

	_graph_maintitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontStyle", 1, -1));

	_graph_maintitle_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleFontSize", 1, -1));
	_graph_subtitle_JTextField.setText( _tsproduct.getDefaultPropValue("SubTitleString", 1, -1));

	_graph_subtitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("SubTitleFontName", 1, -1));

	_graph_subtitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("SubTitleFontStyle", 1, -1));

	_graph_subtitle_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("SubTitleFontSize", 1, -1));

	if (_tsproduct.getDefaultPropValue("ZoomEnabled", 1, -1).equals("False")) {
		_graph_zoomenabled_JCheckBox.setSelected(false);
	}
	else {
		_graph_zoomenabled_JCheckBox.setSelected(true);
	}

	_graph_zoomgroup_JTextField.setText( _tsproduct.getDefaultPropValue("ZoomGroup", 1, -1));

	_xyscatter_analysis_method_JComboBox.select( _tsproduct.getDefaultPropValue("XYScatterMethod", 1, -1));

	_xyscatter_analysis_transform_JComboBox.select( _tsproduct.getDefaultPropValue("XYScatterTransformation", 1, -1));

	_xyscatter_analysis_neqn_JComboBox.select( _tsproduct.getDefaultPropValue("XYScatterNumberOfEquations",1, -1));

	_xyscatter_analysis_month_JTextField.setText( _tsproduct.getDefaultPropValue("XYScatterMonth", 1, -1));

	_xyscatter_analysis_fill_JCheckBox.setEnabled(false);
	_xyscatter_analysis_intercept_JTextField.setText("");
	_xyscatter_analysis_fill_period_start_JTextField.setText("");
	_xyscatter_analysis_fill_period_end_JTextField.setText("");
	_xyscatter_analysis_JPanel.setVisible(false);
	_blank_analysis_JPanel.setVisible(true);
}

/**
Create the panel for displaying annotation properties.
@return the panel that was created.
*/
private JPanel createAnnotationJPanel() {
	JPanel annotationJPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout();
	annotationJPanel.setLayout(gbl);

	int y = -1;

	// Create a panel for annotation controls.

	JPanel controls_JPanel = new JPanel();
	int yControl = -1;
	controls_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"List / Add / Delete / Move Annotations"));
	controls_JPanel.setLayout(gbl);

	JGUIUtil.addComponent(annotationJPanel, controls_JPanel,
		0, ++y, 2, 1, 1, 1, // 2 units wide and 1 high from perspective of main layout.
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	JGUIUtil.addComponent(controls_JPanel, new JLabel("Annotation list:"),
		0, yControl, 1, 1, 1, 1,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);

	__annotation_JComboBox = new SimpleJComboBox(false);
	__annotation_JComboBox.setToolTipText("List of defined annotations using Annotation ID - use Add Annotation button to add a new annotation.");
	__annotation_JComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
	JGUIUtil.addComponent(controls_JPanel, __annotation_JComboBox,
		1, yControl, 1, 1, 0, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Initialized to first annotation of first product.
	int nann = _tsproduct.getNumAnnotations(0);
	for (int iann = 0; iann < nann; iann++) {
		__annotation_JComboBox.add("" + _tsproduct.getLayeredPropValue("AnnotationID", 0, iann, true));
	}

	__addAnnotationJButton = new JButton(__BUTTON_ADD_ANNOTATION);
	__addAnnotationJButton.addActionListener(this);
	__addAnnotationJButton.setToolTipText("Add new annotation (edit its properties below).");
	JGUIUtil.addComponent(controls_JPanel, __addAnnotationJButton,
		4, yControl, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__delAnnotationJButton = new JButton(__BUTTON_DEL_ANNOTATION);
	__delAnnotationJButton.addActionListener(this);
	__delAnnotationJButton.setToolTipText("Delete currently-selected annotation.");
	JGUIUtil.addComponent(controls_JPanel, __delAnnotationJButton,
		5, yControl, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__moveAnnotationUpJButton = new JButton(__BUTTON_ANNOTATION_UP);
	__moveAnnotationUpJButton.addActionListener(this);
	__moveAnnotationUpJButton.setToolTipText("Move currently-selected annotation up one position, which will impact drawing order.");
	JGUIUtil.addComponent(controls_JPanel, __moveAnnotationUpJButton,
		6, yControl, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__moveAnnotationDownJButton = new JButton(__BUTTON_ANNOTATION_DOWN);
	__moveAnnotationDownJButton.addActionListener(this);
	__moveAnnotationDownJButton.setToolTipText("Move currently-selected annotation down one position, which will impact drawing order.");
	JGUIUtil.addComponent(controls_JPanel, __moveAnnotationDownJButton,
		7, yControl, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	// Create a panel for general annotation properties.

	JPanel annotationProps_JPanel = new JPanel();
	int yAnnotationProps = -1;
	annotationProps_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1), "General Annotation Properties"));
	annotationProps_JPanel.setLayout(gbl);

	JGUIUtil.addComponent(annotationJPanel, annotationProps_JPanel,
		0, ++y, 1, 1, 1, 1, // Treat as single cell in the main layout.
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	__annotation_id_JTextField = new JTextField(15);
	__annotation_id_JTextField.setToolTipText("Annotation ID (must be unique within a graph)" );
	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Annotation ID: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_id_JTextField,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Shape type: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_ShapeType_JComboBox = new SimpleJComboBox(false);
	__annotation_ShapeType_JComboBox.setToolTipText("Shape type for annotation (properties shown on right are specific to shape type).");
	__annotation_ShapeType_JComboBox.add("Line");
	__annotation_ShapeType_JComboBox.add("Rectangle");
	__annotation_ShapeType_JComboBox.add("Symbol");
	__annotation_ShapeType_JComboBox.add("Text");
	JGUIUtil.addComponent(annotationProps_JPanel,__annotation_ShapeType_JComboBox,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Order: " ),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_Order_JComboBox = new SimpleJComboBox(false);
	__annotation_Order_JComboBox.setToolTipText("Indicate when annotation is drawn compared to other graph layers (order is top drilling down)");
	__annotation_Order_JComboBox.add("OnTopOfData");
	__annotation_Order_JComboBox.add("BehindData");
	__annotation_Order_JComboBox.add("BehindAxes");
	__annotation_Order_JComboBox.select(0);
	__annotation_Order_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_Order_JComboBox,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("X axis system: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_XAxisSystem_JComboBox = new SimpleJComboBox(false);
	__annotation_XAxisSystem_JComboBox.setToolTipText("If Data, annotation coordinates are in data units; if Percent, annotation coordinates are in percent of graph width 0-100");
	__annotation_XAxisSystem_JComboBox.add("Data");
	__annotation_XAxisSystem_JComboBox.add("Percent");
	__annotation_XAxisSystem_JComboBox.select(0);
	__annotation_XAxisSystem_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_XAxisSystem_JComboBox,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Y axis: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_YAxis_JComboBox = new SimpleJComboBox(false);
	__annotation_YAxis_JComboBox.setToolTipText("Indicate the y-axis for the annotation");
	//__annotation_YAxis_JComboBox.add("Both"); // TODO SAM 201-02-07 need to enable, to facilitate different units on the y-axes.
	__annotation_YAxis_JComboBox.add("Left");
	__annotation_YAxis_JComboBox.add("Right");
	__annotation_YAxis_JComboBox.select(0);
	__annotation_YAxis_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_YAxis_JComboBox,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Y axis system: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_YAxisSystem_JComboBox = new SimpleJComboBox(false);
	__annotation_YAxisSystem_JComboBox.setToolTipText("If Data, annotation coordinates are in data units; if Percent, annotation coordinates are in percent of graph height 0-100");
	__annotation_YAxisSystem_JComboBox.add("Data");
	__annotation_YAxisSystem_JComboBox.add("Percent");
	__annotation_YAxisSystem_JComboBox.select(0);
	__annotation_YAxisSystem_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_YAxisSystem_JComboBox,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_tableid_JTextField = new JTextField(15);
	__annotation_tableid_JTextField.setToolTipText("Identifer for table, used to provide annotation data" );
	JGUIUtil.addComponent(annotationProps_JPanel, new JLabel("Annotation table ID: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(annotationProps_JPanel, __annotation_tableid_JTextField,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__graphAnnotationProvider = new SimpleJComboBox(__annotationProviders);
	__graphAnnotationProvider.setToolTipText("Advanced feature to get annotations from a database or other system");
	__graphAnnotationProvider.setPrototypeDisplayValue(	"XXXXXXXXXXXXXXXXXXXX");
	JGUIUtil.addComponent(annotationProps_JPanel,	new JLabel("Annotation provider: "),
		0, ++yAnnotationProps, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,	GridBagConstraints.WEST);
	JGUIUtil.addComponent(annotationProps_JPanel,	__graphAnnotationProvider,
		1, yAnnotationProps, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	__annotation_ShapeType_JComboBox.addItemListener(this);
	__annotation_ShapeType_JComboBox.select(0);

	__annotation_JComboBox.addItemListener(this);

	// Panel for line annotation properties.

	__annotation_line_JPanel = new JPanel();
	__annotation_line_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Line Shape Type Annotation Properties"));
	__annotation_line_JPanel.setLayout(gbl);
	__annotation_line_PointX1_JTextField = new JTextField(10);
	__annotation_line_PointX1_JTextField.setToolTipText("X-coordinate for line end-point 1, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	y = 0;
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("X1: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_PointX1_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__annotation_line_PointY1_JTextField = new JTextField(10);
	__annotation_line_PointY1_JTextField.setToolTipText("Y-coordinate for line end-point 1, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("Y1: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_PointY1_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_line_PointX2_JTextField = new JTextField(10);
	__annotation_line_PointX2_JTextField.setToolTipText("X-coordinate for line end-point 2, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("X2: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_PointX2_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__annotation_line_PointY2_JTextField = new JTextField(10);
	__annotation_line_PointY2_JTextField.setToolTipText("Y-coordinate for line end-point 2, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("Y2: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_PointY2_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_line_LineWidth_JTextField = new JTextField(5);
	__annotation_line_LineWidth_JTextField.setToolTipText("Line width in pixels for on-screen, printed width varies");
	JGUIUtil.addComponent(__annotation_line_JPanel,
		new JLabel("Line width: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_LineWidth_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotationLineStyleJComboBox = new SimpleJComboBox(false);
	__annotationLineStyleJComboBox.add("Solid");
	__annotationLineStyleJComboBox.add("None");
	__annotationLineStyleJComboBox.select(0);
	y++;
	JGUIUtil.addComponent(__annotation_line_JPanel,
		new JLabel("Line style: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, __annotationLineStyleJComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	__annotation_line_color_JComboBox = new SimpleJComboBox(false);
	int size = GRColor.COLOR_NAMES.length;
	List<String> annotatuinLineColorChoices = new ArrayList<>();
	for (int i = 0; i < size; i++) {
		annotatuinLineColorChoices.add(GRColor.COLOR_NAMES[i]);
	}
	__annotation_line_color_JComboBox.setData(annotatuinLineColorChoices);
	__annotation_line_color_JComboBox.setMaximumRowCount(__annotation_line_color_JComboBox.getItemCount());
	__annotation_line_color_JComboBox.select(1);
	__annotation_line_color_JComboBox.addItemListener(this);
	__annotation_line_color_JTextField = new JTextField(10);
	__annotation_line_color_JTextField.setEditable(false);
	__annotation_line_color_JTextField.setBackground(GRColor.parseColor(GRColor.COLOR_NAMES[1]));
	__annotation_line_color_JTextField.setToolTipText("Line color as named color or use Red,Green,Blue (each value in range 0-255), or one 0xrrggbb hexadecimal value)");

	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("Color: "),
		0, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_color_JTextField,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_line_JPanel,
		__annotation_line_color_JComboBox,
		2, y, 5, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	// Panel for rectangle annotation properties.

	__annotation_rectangle_JPanel = new JPanel();
	__annotation_rectangle_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Rectangle Shape Type Annotation Properties"));
	__annotation_rectangle_JPanel.setLayout(gbl);
	__annotation_rectangle_PointX1_JTextField = new JTextField(10);
	__annotation_rectangle_PointX1_JTextField.setToolTipText("X-coordinate for rectangle corner 1, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	y = 0;
	JGUIUtil.addComponent(__annotation_rectangle_JPanel, new JLabel("X1: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_PointX1_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__annotation_rectangle_PointY1_JTextField = new JTextField(10);
	__annotation_rectangle_PointY1_JTextField.setToolTipText("Y-coordinate for rectangle corner 1, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_rectangle_JPanel, new JLabel("Y1: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_PointY1_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_rectangle_PointX2_JTextField = new JTextField(10);
	__annotation_rectangle_PointX2_JTextField.setToolTipText("X-coordinate for rectangle corner 2, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_rectangle_JPanel, new JLabel("X2: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_PointX2_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__annotation_rectangle_PointY2_JTextField = new JTextField(10);
	__annotation_rectangle_PointY2_JTextField.setToolTipText("Y-coordinate for rectangle corner 2, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_rectangle_JPanel, new JLabel("Y2: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_PointY2_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;

	__annotation_rectangle_color_JComboBox = new SimpleJComboBox(false);
	int colorSize = GRColor.COLOR_NAMES.length;
	List<String> annotationRectangleColorChoices = new ArrayList<>();
	for (int i = 0; i < colorSize; i++) {
		annotationRectangleColorChoices.add(GRColor.COLOR_NAMES[i]);
	}
	__annotation_rectangle_color_JComboBox.setData(annotationRectangleColorChoices);
	__annotation_rectangle_color_JComboBox.setMaximumRowCount(__annotation_rectangle_color_JComboBox.getItemCount());
	__annotation_rectangle_color_JComboBox.select(1);
	__annotation_rectangle_color_JComboBox.addItemListener(this);
	__annotation_rectangle_color_JTextField = new JTextField(10);
	__annotation_rectangle_color_JTextField.setEditable(false);
	__annotation_rectangle_color_JTextField.setBackground(GRColor.parseColor(GRColor.COLOR_NAMES[1]));
	__annotation_rectangle_color_JTextField.setToolTipText("Rectangle fill color as named color or use Red,Green,Blue (each value in range 0-255), or one 0xrrggbb hexadecimal value)");

	JGUIUtil.addComponent(__annotation_rectangle_JPanel, new JLabel("Color: "),
		0, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_color_JTextField,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_rectangle_JPanel,
		__annotation_rectangle_color_JComboBox,
		2, y, 5, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	// Panel for symbol annotation properties.

	__annotation_symbol_JPanel = new JPanel();
	__annotation_symbol_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Symbol Shape Type Annotation Properties"));
	__annotation_symbol_JPanel.setLayout(gbl);

	__annotation_symbol_SymbolStyle_JComboBox = new SimpleJComboBox(false);
	size = GRSymbol.SYMBOL_NAMES.length;
	List<String> annotationSymbolStyleChoices = new ArrayList<>();
	for (int i = 0; i < size; i++) {
		annotationSymbolStyleChoices.add(GRSymbol.SYMBOL_NAMES[i]);
	}
	__annotation_symbol_SymbolStyle_JComboBox.setData(annotationSymbolStyleChoices);

	y = 0;
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Symbol style: " ),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_SymbolStyle_JComboBox,
		1, y, 7, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_symbol_PointX_JTextField = new JTextField(10);
	__annotation_symbol_PointX_JTextField.setToolTipText("X-coordinate for symbol, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("X: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_PointX_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Y: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_symbol_PointY_JTextField = new JTextField(10);
	__annotation_symbol_PointY_JTextField.setToolTipText("Y-coordinate for symbol, in y-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_PointY_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_symbol_SymbolPosition_JComboBox = new SimpleJComboBox(false);
	__annotation_symbol_SymbolPosition_JComboBox.setToolTipText("How is symbol positioned relative to data value?");
	__annotation_symbol_SymbolPosition_JComboBox.add("UpperRight");
	__annotation_symbol_SymbolPosition_JComboBox.add("Right");
	__annotation_symbol_SymbolPosition_JComboBox.add("LowerRight");
	__annotation_symbol_SymbolPosition_JComboBox.add("Below");
	__annotation_symbol_SymbolPosition_JComboBox.add("LowerLeft");
	__annotation_symbol_SymbolPosition_JComboBox.add("Left");
	__annotation_symbol_SymbolPosition_JComboBox.add("UpperLeft");
	__annotation_symbol_SymbolPosition_JComboBox.add("Above");
	__annotation_symbol_SymbolPosition_JComboBox.add("Center");
	__annotation_symbol_SymbolPosition_JComboBox.select("Right");
	__annotation_symbol_SymbolPosition_JComboBox.setMaximumRowCount(10);
	y++;
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Symbol position: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_SymbolPosition_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Symbol size: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_symbol_SymbolSize_JComboBox = new SimpleJComboBox(true);
	__annotation_symbol_SymbolSize_JComboBox.setToolTipText("Symbol size in pixels (printed size will scale to graph size)");
	List<String> annotationSymbolSizeChoices = new ArrayList<>();
	for ( int i = 0; i <= 20; i++ ) {
		annotationSymbolSizeChoices.add("" + i);
	}
	__annotation_symbol_SymbolSize_JComboBox.setData(annotationSymbolSizeChoices);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_SymbolSize_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_symbol_color_JComboBox = new SimpleJComboBox(false);
	size = GRColor.COLOR_NAMES.length;
	List<String> annotationSymbolColorChoices = new ArrayList<>();
	for (int i = 0; i < size; i++) {
		annotationSymbolColorChoices.add(GRColor.COLOR_NAMES[i]);
	}
	__annotation_symbol_color_JComboBox.setData(annotationSymbolColorChoices);
	__annotation_symbol_color_JComboBox.setMaximumRowCount(__annotation_symbol_color_JComboBox.getItemCount());
	__annotation_symbol_color_JComboBox.select(1);
	__annotation_symbol_color_JComboBox.addItemListener(this);
	__annotation_symbol_color_JTextField = new JTextField(10);
	__annotation_symbol_color_JTextField.setToolTipText("Symbol color as named color or use Red,Green,Blue (values each value in range 0-255), or one 0xrrggbb hexadecimal value)");
	__annotation_symbol_color_JTextField.setEditable(false);
	__annotation_symbol_color_JTextField.setBackground(GRColor.parseColor(GRColor.COLOR_NAMES[1]));

	y++;
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Color: "),
		0, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_color_JTextField,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_color_JComboBox,
		2, y, 3, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	// Panel for text annotation properties.

	__annotation_text_JPanel = new JPanel();
	__annotation_text_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Text Shape Type Annotation Properties"));
	__annotation_text_JPanel.setLayout(gbl);

	__annotation_text_Text_JTextField = new JTextField(20);
	y = 0;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Text: " ),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel,
		__annotation_text_Text_JTextField,
		1, y, 7, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("X: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_text_PointX_JTextField = new JTextField(10);
	__annotation_text_PointX_JTextField.setToolTipText("X-coordinate for text, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_text_JPanel,
		__annotation_text_PointX_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Y: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_text_PointY_JTextField = new JTextField(10);
	__annotation_text_PointY_JTextField.setToolTipText("Y-coordinate for text, in x-axis system (YYYY-MM-DD, etc. if date/time)");
	JGUIUtil.addComponent(__annotation_text_JPanel,
		__annotation_text_PointY_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_text_Position_JComboBox = new SimpleJComboBox(false);
	__annotation_text_Position_JComboBox.add("UpperRight");
	__annotation_text_Position_JComboBox.add("Right");
	__annotation_text_Position_JComboBox.add("LowerRight");
	__annotation_text_Position_JComboBox.add("Below");
	__annotation_text_Position_JComboBox.add("LowerLeft");
	__annotation_text_Position_JComboBox.add("Left");
	__annotation_text_Position_JComboBox.add("UpperLeft");
	__annotation_text_Position_JComboBox.add("Above");
	__annotation_text_Position_JComboBox.add("Center");
	__annotation_text_Position_JComboBox.select("Right");
	__annotation_text_Position_JComboBox.setMaximumRowCount(10);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Text position: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_Position_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_text_FontName_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	__annotation_text_FontStyle_JComboBox =JGUIUtil.newFontStyleJComboBox();
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font name: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontName_JComboBox,
		1, y, 7, 1, 1, 1, // Make width 2 so it does not cause the first column to be wide for other components.
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font style: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontStyle_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_text_FontSize_JTextField = new JTextField(3);
	__annotation_text_FontSize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font size: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontSize_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_text_color_JComboBox = new SimpleJComboBox(false);
	size = GRColor.COLOR_NAMES.length;
	List<String> annotationTextColorChoices = new ArrayList<>();
	for (int i = 0; i < size; i++) {
		annotationTextColorChoices.add(GRColor.COLOR_NAMES[i]);
	}
	__annotation_text_color_JComboBox.setData(annotationTextColorChoices);
	__annotation_text_color_JComboBox.setMaximumRowCount(__annotation_text_color_JComboBox.getItemCount());
	__annotation_text_color_JComboBox.select(1);
	__annotation_text_color_JComboBox.addItemListener(this);
	__annotation_text_color_JTextField = new JTextField(10);
	__annotation_text_color_JTextField.setToolTipText("Text color as named color or use Red,Green,Blue (values each value in range 0-255), or one 0xrrggbb hexadecimal value)");
	__annotation_text_color_JTextField.setEditable(false);
	__annotation_text_color_JTextField.setBackground(GRColor.parseColor(GRColor.COLOR_NAMES[1]));

	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Color: "),
		0, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_color_JTextField,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_color_JComboBox,
		2, y, 3, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);

	// Now add each of the annotation property panels in overlapping fashion (one will be set visible at a time, as needed).

	JGUIUtil.addComponent(annotationJPanel, __annotation_line_JPanel,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __annotation_rectangle_JPanel,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __annotation_symbol_JPanel,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __annotation_text_JPanel,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__annotation_line_JPanel.setVisible(false);
	__annotation_symbol_JPanel.setVisible(false);

	return annotationJPanel;
}

/**
Create the panel used to display the data properties.
This consists of a label at the top and a JTabbedPane for properties.
*/
private JPanel createDataJPanel () {
	JPanel data_JPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout ();
	data_JPanel.setLayout ( gbl );
	String prop_val;
	JGUIUtil.addComponent ( data_JPanel, new JLabel ("Time Series Properties:"),
			0, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL,
			GridBagConstraints.NORTH );

	__ts_JComboBox = new DragAndDropSimpleJComboBox ( false,
		DragAndDropUtil.ACTION_COPY, DragAndDropUtil.ACTION_NONE);
	DragAndDropUtil.addDragAndDropListener(__ts_JComboBox, this);
	__ts_JComboBox.addItemListener ( this );
	// Initialized to first time series of first product.
	int nts = _tsproduct.getNumData ( 0 );
	String sequence_number;
	for ( int its = 0; its < nts; its++ ) {
		prop_val = _tsproduct.getLayeredPropValue( "TSAlias", _selected_subproduct, its, false );
		if ( (prop_val == null) || prop_val.trim().equals("") ) {
			prop_val = _tsproduct.getLayeredPropValue( "TSID", _selected_subproduct, its, false );
		}
		sequence_number = _tsproduct.getLayeredPropValue( "SequenceNumber", _selected_subproduct, its, false );
		if ((sequence_number == null) || sequence_number.equals("-1") ){
			sequence_number = "";
		}
		else {
		    sequence_number = " [" + sequence_number + "] ";
		}
		if ( prop_val == null ) {
			prop_val = "No data";
		}
		else {
		    // Limit the property to 60 characters.
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60) + "..." + sequence_number;
			}
		}
		__ts_JComboBox.add ( "" + (its + 1) + " - " + prop_val + sequence_number );
	}
	JGUIUtil.addComponent ( data_JPanel, __ts_JComboBox,
			1, 0, 1, 1, 1, 0, _insetsTLBR,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );

	_ts_JTabbedPane = new JTabbedPane();
	JGUIUtil.addComponent ( data_JPanel, _ts_JTabbedPane,
			0, 1, 2, 1, 1, 0, _insetsTLBR,
			GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	// General tab.

	JPanel general_JPanel = new JPanel();
	general_JPanel.setLayout(gbl);
	_ts_JTabbedPane.addTab("General", null, general_JPanel, "General time series properties");

	int y = -1;
	JGUIUtil.addComponent ( general_JPanel,
			new JLabel("Defaults for how a time series is displayed come from the Graph Properties but can be overridden with Time Series Properties."),
			0, ++y, 6, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( general_JPanel,
			new JSeparator(SwingConstants.HORIZONTAL),
			0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	_ts_enabled_JCheckBox = new JCheckBox("Time series enabled", true);
	JGUIUtil.addComponent ( general_JPanel,	_ts_enabled_JCheckBox,
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );

	// Graph Type tab.

	JPanel graphtype_JPanel = new JPanel();
	graphtype_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Graph Type", null, graphtype_JPanel, "Graph type properties" );

	y = -1;
	JGUIUtil.addComponent ( graphtype_JPanel,
		new JLabel(
		"The graph type for each time series currently must be the same as the graph itself: " +
		_graph_lefty_graphtype_JComboBox.getSelected()),
		0, ++y, 2, 1, 0, 0, _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( graphtype_JPanel,
		new JLabel(
		"An exception is lines drawn on stacked area graph (must edit time series product file manually)."
		+ " More flexibility is being enabled in this properties interface."),
		0, ++y, 2, 1, 0, 0, _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Axes tab.

	JPanel axes_JPanel = new JPanel();
	axes_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Axes", null, axes_JPanel, "Axes properties" );

	y = -1;

	JGUIUtil.addComponent ( axes_JPanel,
		new JLabel("Indicate the axes to associate the time series (default is left Y-axis and bottom X-axis)."),
		0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( axes_JPanel,
		new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	// "XAxis", "YAxis"

	JGUIUtil.addComponent ( axes_JPanel, new JLabel ("X-axis:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_ts_xaxis_JComboBox = new SimpleJComboBox ( false );
	List<String> tsXAxisChoices = new ArrayList<>();
	tsXAxisChoices.add ( "Bottom" );
	tsXAxisChoices.add ( "Top" );
	_ts_xaxis_JComboBox.setData(tsXAxisChoices);
	_ts_xaxis_JComboBox.setEnabled ( false );
	JGUIUtil.addComponent ( axes_JPanel, _ts_xaxis_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( axes_JPanel, new JLabel ("Y-axis:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_ts_yaxis_JComboBox = new SimpleJComboBox ( false );
	List<String> tsXYxisChoices = new ArrayList<>();
	tsXYxisChoices.add ( "Left" );
	tsXYxisChoices.add ( "Right" );
	_ts_yaxis_JComboBox.setData(tsXYxisChoices);
	//_ts_yaxis_JComboBox.setEnabled ( false );
	JGUIUtil.addComponent ( axes_JPanel, _ts_yaxis_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Symbol.

	JPanel symbol_JPanel = new JPanel();
	symbol_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Symbol", null, symbol_JPanel, "Symbol properties" );

	y = 0;
	// Some will be disabled if not a line graph.
	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Line style:"),
			0, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_linestyle_JComboBox = new SimpleJComboBox ( false );
	_ts_linestyle_JComboBox.setToolTipText("Line style for line graphs.");
	List<String> tsLineStyleChoices = new ArrayList<>();
	tsLineStyleChoices.add("Dashed");
	tsLineStyleChoices.add("None");
	tsLineStyleChoices.add("Solid");
	_ts_linestyle_JComboBox.setData(tsLineStyleChoices);
	JGUIUtil.addComponent ( symbol_JPanel, _ts_linestyle_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Line width:"),
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_linewidth_JComboBox = new SimpleJComboBox ( true );
	_ts_linewidth_JComboBox.setToolTipText("Line width in pixels on screen.");
	List<String> tsLineWidthChoices = new ArrayList<>();
	tsLineWidthChoices.add("1");
	tsLineWidthChoices.add("2");
	tsLineWidthChoices.add("3");
	tsLineWidthChoices.add("4");
	tsLineWidthChoices.add("5");
	_ts_linewidth_JComboBox.setData(tsLineWidthChoices);
	JGUIUtil.addComponent ( symbol_JPanel, _ts_linewidth_JComboBox,
			3, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Color:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_color_JTextField = new JTextField (10);
	_ts_color_JTextField.setEditable(false);
	_ts_color_JTextField.setToolTipText ( "Color for graph visualization of time series - pick from choices on right." );
	JGUIUtil.addComponent ( symbol_JPanel, _ts_color_JTextField,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	_ts_color_JComboBox = new SimpleJComboBox( false );
	_ts_color_JComboBox.setToolTipText ( "Color choices." );
	_ts_color_JComboBox.addItemListener(this);
	int size = GRColor.COLOR_NAMES.length;
	List<String> tsColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		tsColorChoices.add ( GRColor.COLOR_NAMES[i] );
	}
	_ts_color_JComboBox.setData(tsColorChoices);
	_ts_color_JComboBox.setMaximumRowCount( _ts_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( symbol_JPanel, _ts_color_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	_ts_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_ts_color_JButton.setEnabled(false);
	_ts_color_JButton.setToolTipText ( "Custom color choices are not implemented." );
	JGUIUtil.addComponent ( symbol_JPanel, _ts_color_JButton,
			3, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Symbol style:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_symbolstyle_JComboBox = new SimpleJComboBox ( false );
	_ts_symbolstyle_JComboBox.setToolTipText ( "Symbol for line and point graphs - see also symbol size.");
	size = GRSymbol.SYMBOL_NAMES.length;
	List<String> tsSymbolStyleChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		tsSymbolStyleChoices.add ( GRSymbol.SYMBOL_NAMES[i] );
	}
	_ts_symbolstyle_JComboBox.setData(tsSymbolStyleChoices);
	JGUIUtil.addComponent ( symbol_JPanel, _ts_symbolstyle_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Symbol size:"),
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_symbolsize_JComboBox = new SimpleJComboBox ( false );
	_ts_symbolsize_JComboBox.setToolTipText ( "Symbol size for line and point graphs - see also symbol style.");
	List<String> tsSymbolSizeChoices = new ArrayList<>();
	for ( int i = 0; i <= 20; i++ ) {
		tsSymbolSizeChoices.add ( "" + i );
	}
	_ts_symbolsize_JComboBox.setData(tsSymbolSizeChoices);
	_ts_symbolsize_JComboBox.setMaximumRowCount(_ts_symbolsize_JComboBox.getItemCount());
	JGUIUtil.addComponent ( symbol_JPanel, _ts_symbolsize_JComboBox,
			3, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

   JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Flagged data symbol style:"),
        0, ++y, 1, 1, 0, 0, _insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.EAST );
    _ts_flaggedDataSymbolStyle_JComboBox = new SimpleJComboBox ( false );
    _ts_flaggedDataSymbolStyle_JComboBox.setToolTipText ( "Flagged data symbol for line and point graphs - see also symbol size.");
    size = GRSymbol.SYMBOL_NAMES.length;
    List<String> tsFlaggedSymbolStyleChoices = new ArrayList<>();
    tsFlaggedSymbolStyleChoices.add ( "" );
    for ( int i = 0; i < size; i++ ) {
    	tsFlaggedSymbolStyleChoices.add ( GRSymbol.SYMBOL_NAMES[i] );
    }
    _ts_flaggedDataSymbolStyle_JComboBox.setData(tsFlaggedSymbolStyleChoices);
    JGUIUtil.addComponent ( symbol_JPanel, _ts_flaggedDataSymbolStyle_JComboBox,
        1, y, 1, 1, 0, 0, _insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Data point label.

	JPanel label_JPanel = new JPanel();
	label_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Data Point Label", null,label_JPanel,"Data point label properties");

	y = -1;
	JGUIUtil.addComponent (label_JPanel, new JLabel ("Indicate how time series data points should be labeled with text, "
			+ "useful for showing data flags."),
			0, ++y, 3, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent (label_JPanel, new JLabel ("If the Label Format is Auto, defaults or"+
			" the Graph Properties / Data Point Label / Format will be used."),
			0, ++y, 3, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel,
			new JSeparator(SwingConstants.HORIZONTAL),
			0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel, new JLabel ("Position:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_datalabelposition_JComboBox = new SimpleJComboBox( false );
	String [] positions = GRText.getTextPositions();
	_ts_datalabelposition_JComboBox.setMaximumRowCount( positions.length + 1);
	List<String> tsDataLabelPosChoices = new ArrayList<>();
	tsDataLabelPosChoices.add ( "Auto" );
	for ( int i = 0; i < positions.length; i++ ) {
		tsDataLabelPosChoices.add ( positions[i] );
	}
	_ts_datalabelposition_JComboBox.setData(tsDataLabelPosChoices);
	_ts_datalabelposition_JComboBox.select ( "Right" );
	JGUIUtil.addComponent ( label_JPanel, _ts_datalabelposition_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel, new JLabel ("Format (see choices):"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_ts_datalabelformat_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( label_JPanel, _ts_datalabelformat_JTextField,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	_ts_datalabelformat_JComboBox = new SimpleJComboBox( false );
	_ts_datalabelformat_JComboBox.addItemListener ( this );
	String [] formats = TimeUtil.getDateTimeFormatSpecifiers ( true, true, false );
	List<String> tsDataLabelFormatChoices = new ArrayList<>();
	for ( int i = 0; i < formats.length; i++ ) {
		tsDataLabelFormatChoices.add ( formats[i] );
	}
	tsDataLabelFormatChoices.add ( "%v - Value" );
	tsDataLabelFormatChoices.add ( "%U - Units" );
	tsDataLabelFormatChoices.add ( "%q - Flag" );
	_ts_datalabelformat_JComboBox.setData(tsDataLabelFormatChoices);
	_ts_datalabelformat_JComboBox.setMaximumRowCount(_ts_datalabelformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( label_JPanel, _ts_datalabelformat_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Legend.

	JPanel legend_JPanel = new JPanel();
	legend_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Legend", null, legend_JPanel, "Legend properties" );

	y = 0;
	// Need to add "LegendEnabled" checkbox.
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ( "If the " +
			"Format is Auto, defaults or the Graph Properties / Legend / Format will be used."),
			0, y, 3, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Format (see choices):"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_legendformat_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( legend_JPanel, _ts_legendformat_JTextField,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	_ts_legendformat_JComboBox = new SimpleJComboBox( false );
	_ts_legendformat_JComboBox.addItemListener ( this );
	formats = TSUtil.getTSFormatSpecifiers ( true );
	List<String> tsLegendFormatChoices = new ArrayList<>();
	tsLegendFormatChoices.add ( "Auto" );
	for ( int i = 0; i < formats.length; i++ ) {
		tsLegendFormatChoices.add ( formats[i] );
	}
	_ts_legendformat_JComboBox.setData(tsLegendFormatChoices);
	_ts_legendformat_JComboBox.setMaximumRowCount( _ts_legendformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( legend_JPanel, _ts_legendformat_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Analysis.

	// This panel is the main panel managed by the TabbedPane.

	_ts_analysis_JPanel = new JPanel();
	_ts_analysis_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Analysis", null, _ts_analysis_JPanel, "Analysis properties" );

	// Create a blank panel that is set visible when there is no analysis for the time series.

	_ts_blank_analysis_JPanel = new JPanel ();
	_ts_blank_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent ( _ts_analysis_JPanel, _ts_blank_analysis_JPanel,
			0, 0, 1, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_ts_blank_analysis_JPanel.setVisible ( true );
	JGUIUtil.addComponent ( _ts_blank_analysis_JPanel, new JLabel ( "Time series has no analysis." ),
			0, 0, 1, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	// Create a panel that is set visible for the XY-Scatter graph.

	_ts_xyscatter_analysis_JPanel = new JPanel ();
	_ts_xyscatter_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (_ts_analysis_JPanel, _ts_xyscatter_analysis_JPanel,
	        0, 0, 1, 1, 1, 1, _insetsTLBR,
	        GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_ts_xyscatter_analysis_JPanel.setVisible ( false );

	y = -1;
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, new JLabel (
			"Only the properties for time series 2 and higher cause a visual change." ),
			0, ++y, 4, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, new JLabel (
			"Confidence intervals are useful for a low number of points (e.g., 100)." ),
			0, ++y, 4, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	_ts_regressionline_JCheckBox =new JCheckBox("Show curve fit line",true);
	_ts_regressionline_JCheckBox.addItemListener(this);
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, _ts_regressionline_JCheckBox,
			0, ++y, 2, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, new JLabel("Confidence interval (%):"),
			0, ++y, 2, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_confidenceinterval_JComboBox = new SimpleJComboBox( false );
	//_ts_confidenceinterval_JComboBox.addItemListener ( this );
	List<String> tsConfIntChoices = new ArrayList<>();
	tsConfIntChoices.add ( "" );
	tsConfIntChoices.add ( "95" );
	tsConfIntChoices.add ( "99" );
	_ts_confidenceinterval_JComboBox.setData(tsConfIntChoices);
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, _ts_confidenceinterval_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	return data_JPanel;
}

/**
Create the panel used to display the graph layout.
This is a panel with two components, one for the main graph, one for the reference graph.
For now, just do a simple diagram.  Later, might add WYSIWYG display so users can see exactly what the graph will look like.
@return the Panel that shows the layout of the graphs.
*/
private JPanel createLayoutJPanel () {
	JPanel layout_JPanel = new JPanel();

	layout_JPanel.setLayout ( new GridBagLayout() );
	__layoutCanvas = new TSProductLayoutJComponent(this, _tsproduct);
	__layoutCanvas.setPreferredSize(new java.awt.Dimension(100, 111));
	__layoutCanvas.setMinimumSize(new java.awt.Dimension(100, 111));
	__layoutCanvas.addMouseListener(__layoutCanvas);
	JGUIUtil.addComponent(layout_JPanel, __layoutCanvas,
		0, 0, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.SOUTH);
	return layout_JPanel;
}

/**
Create the panel used to display the Product properties.
This consists of a label at the top and a JTabbedPane for properties.
*/
private JPanel createProductJPanel () {
	JPanel product_JPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout();
	product_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (product_JPanel,new JLabel("Product Properties (some properties may be disabled and can only be edited in product files):"),
			0, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL,
			GridBagConstraints.NORTH );

	_product_JTabbedPane = new JTabbedPane();
	JGUIUtil.addComponent ( product_JPanel, _product_JTabbedPane,
			0, 1, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.BOTH,
			GridBagConstraints.NORTH );

	// General tab.

	JPanel general_JPanel = new JPanel();
	_product_JTabbedPane.addTab ( "General", null, general_JPanel, "General properties" );
	general_JPanel.setLayout(gbl);
	__product_id_JTextField = new JTextField(20);
	__product_id_JTextField.setToolTipText("Identifier used to save product to database, for example.");
	JGUIUtil.addComponent(general_JPanel, new JLabel("Product ID: "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, __product_id_JTextField,
		1, 0, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__product_name_JTextField = new JTextField(40);
	__product_name_JTextField.setToolTipText("Descriptive name used with product ID.");
	JGUIUtil.addComponent(general_JPanel, new JLabel("Product name: "),
		0, 1, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, __product_name_JTextField,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	_product_enabled_JCheckBox = new JCheckBox("Product enabled", true);
	_product_enabled_JCheckBox.setToolTipText("Enable or disable the product (for use with saved product file)" );
	_product_enabled_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent(general_JPanel, _product_enabled_JCheckBox,
		0, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	// Titles tab.

	JPanel title_JPanel = new JPanel();
	title_JPanel.setLayout ( gbl );
	_product_JTabbedPane.addTab ( "Titles", null, title_JPanel, "Title properties" );

	int y = 0;
	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Main title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_JTextField = new JTextField ( 30 );
	_product_maintitle_JTextField.setToolTipText("Main title shown at top of product");
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontsize_JTextField = new JTextField ( 3 );
	_product_maintitle_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (title_JPanel, _product_maintitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Sub title:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_JTextField = new JTextField ( 30 );
	_product_subtitle_JTextField.setToolTipText("Sub title shown at top of product below main title");
	JGUIUtil.addComponent ( title_JPanel, _product_subtitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent(title_JPanel,_product_subtitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontstyle_JComboBox =JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel, _product_subtitle_fontstyle_JComboBox,
		3, y, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontsize_JTextField = new JTextField ( 3 );
	_product_subtitle_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent(title_JPanel, _product_subtitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JPanel layoutJPanel = new JPanel();
	layoutJPanel.setLayout(gbl);
	_product_JTabbedPane.addTab("Layout", null, layoutJPanel, "Layout Properties" );

	y = -1;
	JGUIUtil.addComponent(layoutJPanel, new JLabel ("Layout type:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_layoutTypeJComboBox = new SimpleJComboBox();
	_layoutTypeJComboBox.setToolTipText("Layout for multiple graphs in a product");
	_layoutTypeJComboBox.add("Grid");
	_layoutTypeJComboBox.setEnabled(false);
	JGUIUtil.addComponent(layoutJPanel, _layoutTypeJComboBox,
		1, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(layoutJPanel, new JLabel("Number of grid rows:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_numberRowsJTextField = new JTextField(10);
	_numberRowsJTextField.setToolTipText("Number of rows in the grid layout");
	_numberRowsJTextField.setText("" + _tsproduct.getNumSubProducts());
	_numberRowsJTextField.setEnabled(false);
	_numberRowsJTextField.setEditable(false);
	JGUIUtil.addComponent(layoutJPanel, _numberRowsJTextField,
		1, y, 1, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(layoutJPanel, new JLabel("Number of grid columns:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_numberColsJTextField = new JTextField(10);
	_numberColsJTextField.setToolTipText("Number of columns in the grid layout");
	_numberColsJTextField.setText("1");
	_numberColsJTextField.setEnabled(false);
	_numberColsJTextField.setEditable(false);
	JGUIUtil.addComponent(layoutJPanel, _numberColsJTextField,
		1, y, 1, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Developer tab.

	JPanel devJPanel = new JPanel();
	devJPanel.setLayout(gbl);
	_product_JTabbedPane.addTab("Developer", null, devJPanel, "Developer Properties" );

	y = -1;
	JGUIUtil.addComponent ( devJPanel, new JLabel ( "These properties are used by software developers.  Remember to press Apply to change the graph."),
		0, ++y, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( devJPanel,
		new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	JGUIUtil.addComponent(devJPanel, new JLabel ("Show drawing area outline:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_developerShowDrawingAreaOutlineJComboBox = new SimpleJComboBox();
	_developerShowDrawingAreaOutlineJComboBox.setToolTipText("Show drawing area outlines, used by developers");
	_developerShowDrawingAreaOutlineJComboBox.add("False");
	_developerShowDrawingAreaOutlineJComboBox.add("True");
	JGUIUtil.addComponent(devJPanel, _developerShowDrawingAreaOutlineJComboBox,
		1, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	_developerShowProductProperties_JButton = new SimpleJButton ( "Show Product Properties", "Show Product Properties", this );
	_developerShowProductProperties_JButton.setToolTipText ( "Display all the product properties, useful for troubleshooting." );
	JGUIUtil.addComponent ( devJPanel, _developerShowProductProperties_JButton,
		0, ++y, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_developerShowDrawingAreaProperties_JButton = new SimpleJButton ( "Show Drawing Area Properties", "Show Drawing Area Properties", this );
	_developerShowDrawingAreaProperties_JButton.setToolTipText ( "Display all the drawing area properties, useful for troubleshooting." );
	JGUIUtil.addComponent ( devJPanel, _developerShowDrawingAreaProperties_JButton,
		1, y, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	/* TODO SAM
	// Reference graph.

	JPanel reference_JPanel = new JPanel();
	_product_JTabbedPane.addTab ( "Reference Graph", null, reference_JPanel, "Reference graph properties" );
	 */

	return product_JPanel;
}

/**
Create the panel used to display the SubProduct properties.
This consists of a label at the top and a TabbedPane for properties.
*/
private JPanel createSubproductJPanel () {
	JPanel graph_JPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout();
	graph_JPanel.setLayout ( gbl );
	String prop_val;
	Insets insetsTLBRWestSpace = new Insets(_insets_edge, (_insets_edge + 10), _insets_edge, _insets_edge ); // Buffer on west

	JGUIUtil.addComponent ( graph_JPanel, new JLabel ( "Graph Properties:"),
			0, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );

	_graph_JComboBox = new SimpleJComboBox ( false );
	_graph_JComboBox.setToolTipText("Graph number in product (and main title of product, if available)");
	int nsub = _tsproduct.getNumSubProducts();
	List<String> graphChoices = new ArrayList<>();
	if ( nsub == 0 ) {
		graphChoices.add ( NO_GRAPHS_DEFINED );
	}
	else {
	    for ( int isub = 0; isub < nsub; isub++ ) {
			prop_val = _tsproduct.getLayeredPropValue ( "MainTitleString", isub, -1, false );
			if ( prop_val == null ) {
				prop_val = "";
			}
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60);
			}
			graphChoices.add ( "" + (isub + 1) + " - " + prop_val );
		}
	}
	_graph_JComboBox.setData(graphChoices);
	_graph_JComboBox.addItemListener ( this );

	JGUIUtil.addComponent ( graph_JPanel, _graph_JComboBox,
			2, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST );

	_graph_JTabbedPane = new JTabbedPane();
	_graph_JTabbedPane.addChangeListener ( this );
	JGUIUtil.addComponent ( graph_JPanel, _graph_JTabbedPane,
			0, 1, 3, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	// General tab (use flow layout until more items are added).

	JPanel general_JPanel = new JPanel();
	int yGeneral = -1;
	/*
	general_JPanel.setLayout ( new FlowLayout(FlowLayout.LEFT) );
	_graph_JTabbedPane.addTab ( "General", null, general_JPanel, "General properties" );

	_graph_enabled_JCheckBox = new JCheckBox("Graph Enabled", true);
	_graph_enabled_JCheckBox.setEnabled ( false );
	_graph_isref_JCheckBox = new JCheckBox("", true);
	_graph_isref_JCheckBox.setEnabled ( false );
	general_JPanel.add ( _graph_enabled_JCheckBox );
	*/
	general_JPanel.setLayout ( gbl);
	_graph_JTabbedPane.addTab ( "General", null, general_JPanel, "General properties" );

	_graph_enabled_JCheckBox = new JCheckBox("Graph enabled", true);
	_graph_enabled_JCheckBox.setEnabled ( false );
	_graph_enabled_JCheckBox.setToolTipText("Enable/disable graph (generally always enabled)");
	_graph_isref_JCheckBox = new JCheckBox("", true);
	_graph_isref_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent(general_JPanel, _graph_enabled_JCheckBox,
		1, ++yGeneral, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	_yPercentJTextField = new JTextField(10);
	_yPercentJTextField.setToolTipText("Percent of vertical product size for this graph, 0-100");
	JGUIUtil.addComponent(general_JPanel, new JLabel("Y percent size: "),
		0, ++yGeneral, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, _yPercentJTextField,
		1, 1, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	// Create a sub-panel with a border to hold selected time series highlight properties.
	JPanel selectedTSProps_JPanel = new JPanel();
	int ySelectedTSProps = -1;
	selectedTSProps_JPanel.setLayout(gbl);
	selectedTSProps_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Selected Time Series Highlighting"));
	JGUIUtil.addComponent ( general_JPanel, selectedTSProps_JPanel,
		0, ++yGeneral, 2, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( selectedTSProps_JPanel, new JLabel ("Selected time series line width:"),
		0, ++ySelectedTSProps, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graphSelectedTimeSeriesLineWidth_JTextField = new JTextField (10);
	_graphSelectedTimeSeriesLineWidth_JTextField.setToolTipText("Line width for time series selected in legend, can be: #pixels, +#pixels, xFacter");
	JGUIUtil.addComponent ( selectedTSProps_JPanel, _graphSelectedTimeSeriesLineWidth_JTextField,
		1, ySelectedTSProps, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Graph Type tab.

	JPanel graphtype_JPanel = new JPanel();
	graphtype_JPanel.setLayout ( gbl );
	int yGraphType = -1;
	_graph_JTabbedPane.addTab ( "Graph Type", null, graphtype_JPanel, "Graph type properties" );

	JGUIUtil.addComponent ( graphtype_JPanel,
		new JLabel("The graph type will by default be used for all time series on an axis and can be overridden in Time Series Properties / Graph Type."),
		0, ++yGraphType, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( graphtype_JPanel,
		new JLabel("Some graph types when initially specified do not allow changes after the initial graph type is specified."),
		0, ++yGraphType, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( graphtype_JPanel,
		new JLabel("Assign the time series to an axis using the Time Series Properties / Axes properties."),
		0, ++yGraphType, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( graphtype_JPanel,
		new JSeparator(SwingConstants.HORIZONTAL),
		0, ++yGraphType, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold left y-axis properties.
	JPanel yAxisLeftGraphType_JPanel = new JPanel();
	int yYAxisLeftGraphType = -1;
	yAxisLeftGraphType_JPanel.setLayout(gbl);
	yAxisLeftGraphType_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Left Y-Axis Graph Type"));
	JGUIUtil.addComponent ( graphtype_JPanel, yAxisLeftGraphType_JPanel,
		0, ++yGraphType, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, new JLabel("Graph type:"),
			0, ++yYAxisLeftGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_graphtype_JComboBox = new SimpleJComboBox( false );
	_graph_lefty_graphtype_JComboBox.setToolTipText("Graph type used for left y-axis if not overriden with time series property.");
	//_graph_graphtype_JComboBox.setEnabled ( false );
	List<String> leftyGraphTypeChoices = new ArrayList<>();
	for ( TSGraphType graphType: TSGraphType.values() ) {
	    if ( graphType != TSGraphType.UNKNOWN ) {
	    	leftyGraphTypeChoices.add ( "" + graphType );
	    }
	}
	_graph_lefty_graphtype_JComboBox.setData(leftyGraphTypeChoices);
	int size = _graph_lefty_graphtype_JComboBox.getItemCount();
	_graph_lefty_graphtype_JComboBox.addItemListener(this);
	//_graph_graphtype_JComboBox.setEnabled(false);
	JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, _graph_lefty_graphtype_JComboBox,
			1, yYAxisLeftGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Add bar position and overlap choice regardless of type.
	// The displaySubProduct() method sets to visible only if a bar graph.

	_graph_barposition_JLabel = new JLabel("Bar position:");
	JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, _graph_barposition_JLabel,
			0, ++yYAxisLeftGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_barposition_JComboBox = new SimpleJComboBox( false );
	_graph_barposition_JComboBox.setToolTipText("Indicate how to align bar with respect to date/time of time series data value.");
	List<String> graphBarPosChoices = new ArrayList<>();
	graphBarPosChoices.add ( "CenteredOnDate" );
	graphBarPosChoices.add ( "LeftOfDate" );
	graphBarPosChoices.add ( "RightOfDate" );
	_graph_barposition_JComboBox.setData(graphBarPosChoices);
	JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, _graph_barposition_JComboBox,
			1, yYAxisLeftGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    _graph_barOverlap_JLabel = new JLabel("Bar overlap:");
    JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, _graph_barOverlap_JLabel,
        0, ++yYAxisLeftGraphType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
    _graph_barOverlap_JComboBox = new SimpleJComboBox( false );
    _graph_barOverlap_JComboBox.setToolTipText("If True, bars will be drawn on top of each other, if False they will be drawn next to each other");
    List<String> graphBarOverlapChoices = new ArrayList<>();
    graphBarOverlapChoices.add ( "False" );
    graphBarOverlapChoices.add ( "True" );
    _graph_barOverlap_JComboBox.setData(graphBarOverlapChoices);
    _graph_barOverlap_JComboBox.setToolTipText ( "False will display bars next to each other, " +
    	"True will display bars with first time series in back and last in front." );
    JGUIUtil.addComponent ( yAxisLeftGraphType_JPanel, _graph_barOverlap_JComboBox,
        1, yYAxisLeftGraphType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold right y-axis properties.
	JPanel yAxisRightGraphType_JPanel = new JPanel();
	int yYAxisRightGraphType = -1;
	yAxisRightGraphType_JPanel.setLayout(gbl);
	yAxisRightGraphType_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Right Y-Axis Graph Type"));
	JGUIUtil.addComponent ( graphtype_JPanel, yAxisRightGraphType_JPanel,
		1, yGraphType, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, new JLabel("Graph type:"),
			0, ++yYAxisRightGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_graphtype_JComboBox = new SimpleJComboBox( false );
	_graph_righty_graphtype_JComboBox.setToolTipText("Graph type used for right y-axis if not overriden with time series property.");
	//_graph_graphtype_JComboBox.setEnabled ( false );
	List<String> graphRightYGraphTypeChoices = new ArrayList<>();
	for ( TSGraphType graphType: TSGraphType.values() ) {
	    if ( graphType != TSGraphType.UNKNOWN ) {
	    	graphRightYGraphTypeChoices.add ( "" + graphType );
	    }
	}
	_graph_righty_graphtype_JComboBox.setData(graphRightYGraphTypeChoices);
	// Make choice wide so the border label does not get cut off (can happen with short names).
	_graph_righty_graphtype_JComboBox.setPrototypeDisplayValue("AreaStacked");
	size = _graph_righty_graphtype_JComboBox.getItemCount();
	_graph_righty_graphtype_JComboBox.addItemListener(this);
	//_graph_graphtype_JComboBox.setEnabled(false);
	JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, _graph_righty_graphtype_JComboBox,
			1, yYAxisRightGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Add bar position and overlap choice regardless of type.
	// The displaySubProduct() method sets to visible only if a bar graph...

	_graph_righty_barposition_JLabel = new JLabel("Bar position:");
	JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, _graph_righty_barposition_JLabel,
			0, ++yYAxisRightGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_barposition_JComboBox = new SimpleJComboBox( false );
	_graph_righty_barposition_JComboBox.setToolTipText("Indicate how to align bar with respect to date/time of time series data value.");
	List<String> rightYBarPosChoices = new ArrayList<>();
	rightYBarPosChoices.add ( "CenteredOnDate" );
	rightYBarPosChoices.add ( "LeftOfDate" );
	rightYBarPosChoices.add ( "RightOfDate" );
	_graph_righty_barposition_JComboBox.setData(rightYBarPosChoices);
	JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, _graph_righty_barposition_JComboBox,
			1, yYAxisRightGraphType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    _graph_righty_barOverlap_JLabel = new JLabel("Bar overlap:");
    JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, _graph_righty_barOverlap_JLabel,
        0, ++yYAxisRightGraphType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
    _graph_righty_barOverlap_JComboBox = new SimpleJComboBox( false );
    _graph_righty_barOverlap_JComboBox.setToolTipText("If True, bars will be drawn on top of each other, if False they will be drawn next to each other");
    List<String> rightYBarOverlapChoices = new ArrayList<>();
    rightYBarOverlapChoices.add ( "False" );
    rightYBarOverlapChoices.add ( "True" );
    _graph_righty_barOverlap_JComboBox.setData(rightYBarOverlapChoices);
    _graph_righty_barOverlap_JComboBox.setToolTipText ( "False will display bars next to each other, " +
    	"True will display bars with first time series in back and last in front." );
    JGUIUtil.addComponent ( yAxisRightGraphType_JPanel, _graph_righty_barOverlap_JComboBox,
        1, yYAxisRightGraphType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Titles tab.

	JPanel title_JPanel = new JPanel();
	title_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Titles", null, title_JPanel, "Title properties" );

	int y = 0;
	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Main title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_JTextField = new JTextField ( 30 );
	_graph_maintitle_JTextField.setToolTipText("Graph main title text, shown above the graph");
	JGUIUtil.addComponent ( title_JPanel, _graph_maintitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent(title_JPanel, _graph_maintitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel,_graph_maintitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontsize_JTextField = new JTextField ( 3 );
	_graph_maintitle_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent(title_JPanel,_graph_maintitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Sub title:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_JTextField = new JTextField ( 30 );
	_graph_subtitle_JTextField.setToolTipText("Graph subtitle text, shown below the graph main title");
	JGUIUtil.addComponent ( title_JPanel, _graph_subtitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent(title_JPanel, _graph_subtitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel, _graph_subtitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontsize_JTextField = new JTextField ( 3 );
	_graph_subtitle_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (title_JPanel,_graph_subtitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	/* TODO SAM take out for now.
	// Enabled Data tab.

	JPanel enabled_data_JPanel = new JPanel();
	_graph_TabbedPane.addTab ( "Enabled Data", null, enabled_data_JPanel, "Enabled data" );
	enabled_data_Panel.add (
		new JLabel( "Data (time series) for the graph are enabled using the data General tab.") );
	 */

	// X Axes tab.

	JPanel xaxes_JPanel = new JPanel();
	xaxes_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "X Axis (Bottom)", null, xaxes_JPanel, "X Axis properties" );

	y = 0;
	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_title_JTextField = new JTextField ( 30 );
	_graph_bottomx_title_JTextField.setToolTipText("Graph bottom title text, shown below the graph");
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_title_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontname_JComboBox=JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent(xaxes_JPanel, _graph_bottomx_title_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(xaxes_JPanel, _graph_bottomx_title_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontsize_JTextField = new JTextField ( 3 );
	_graph_bottomx_title_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_title_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Label font:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_label_fontname_JComboBox=JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_label_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_label_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_label_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_label_fontsize_JTextField = new JTextField ( 3 );
	_graph_bottomx_label_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_label_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Major grid color:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_majorgrid_color_JTextField = new JTextField (10);
	_graph_bottomx_majorgrid_color_JTextField.setToolTipText("Color for x-axis major tick mark grid, use None to not show grid");
	// Do not set the background color because it will be set to that of the grid color.
	_graph_bottomx_majorgrid_color_JTextField.setEditable(false);
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_majorgrid_color_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_bottomx_majorgrid_color_JComboBox = new SimpleJComboBox( false );
	_graph_bottomx_majorgrid_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	List<String> graphBottomGridColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		graphBottomGridColorChoices.add ( GRColor.COLOR_NAMES[i]);
	}
	_graph_bottomx_majorgrid_color_JComboBox.setData(graphBottomGridColorChoices);
	_graph_bottomx_majorgrid_color_JComboBox.setMaximumRowCount(
		_graph_bottomx_majorgrid_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_majorgrid_color_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_bottomx_majorgrid_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_bottomx_majorgrid_color_JButton.setEnabled(false);
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_majorgrid_color_JButton,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Y Axis (Left) tab.

	JPanel yAxisLeft_JPanel = new JPanel();
	yAxisLeft_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Y Axis (Left)", null, yAxisLeft_JPanel, "Left Y axis properties" );

	y = 0;
	// Create a sub-panel with a border to hold title properties.
	JPanel yAxisLeftTitle_JPanel = new JPanel();
	yAxisLeftTitle_JPanel.setLayout(gbl);
	yAxisLeftTitle_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Left) Title"));
	JGUIUtil.addComponent ( yAxisLeft_JPanel, yAxisLeftTitle_JPanel,
		0, y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	int yAxisLeftTitle = -1;
	JGUIUtil.addComponent ( yAxisLeftTitle_JPanel, new JLabel ("Title:"),
			0, ++yAxisLeftTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_title_JTextField = new JTextField ( 30 );
	_graph_lefty_title_JTextField.setToolTipText("Left y-axis title, time series units by default");
	JGUIUtil.addComponent ( yAxisLeftTitle_JPanel, _graph_lefty_title_JTextField,
			1, yAxisLeftTitle, 3, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );

	_graph_lefty_title_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent (yAxisLeftTitle_JPanel, _graph_lefty_title_fontname_JComboBox,
			4, yAxisLeftTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH ); // HORIZONTAL to resize to Max value choice
	_graph_lefty_title_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yAxisLeftTitle_JPanel, _graph_lefty_title_fontstyle_JComboBox,
			5, yAxisLeftTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_title_fontsize_JTextField = new JTextField ( 3 );
	_graph_lefty_title_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (yAxisLeftTitle_JPanel, _graph_lefty_title_fontsize_JTextField,
			6, yAxisLeftTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yAxisLeftTitle_JPanel, new JLabel ("Position:"),
		0, ++yAxisLeftTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	List<String> yAxisTitlePositionValues = getPropertyChoices("LeftYAxisTitlePosition");
	String yAxisTitlePosition = getPropertyChoiceDefault("LeftYAxisTitlePosition");
	_graph_lefty_title_position_JComboBox = new SimpleJComboBox(yAxisTitlePositionValues, true);
	_graph_lefty_title_position_JComboBox.setToolTipText("Position of y-axis title");
	//_graph_lefty_title_position_JComboBox.setEnabled ( false );
	_graph_lefty_title_position_JComboBox.select(yAxisTitlePosition);
	_graph_lefty_title_position_JComboBox.setToolTipText("Indicate position of left y-axis title");
	JGUIUtil.addComponent ( yAxisLeftTitle_JPanel, _graph_lefty_title_position_JComboBox,
		1, yAxisLeftTitle, 3, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisLeftTitle_JPanel, new JLabel ("Rotation:"),
		0, ++yAxisLeftTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_title_rotation_JTextField = new JTextField(5);
	_graph_lefty_title_rotation_JTextField.setToolTipText(
		"Rotation (clockwise degrees) of left y-axis title, 0=horizontal, 270=vertical with top of text on left");
	JGUIUtil.addComponent (yAxisLeftTitle_JPanel,_graph_lefty_title_rotation_JTextField,
		1, yAxisLeftTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold label properties.
	JPanel yAxisLeftLabel_JPanel = new JPanel();
	yAxisLeftLabel_JPanel.setLayout(gbl);
	yAxisLeftLabel_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Left) Labels / Limits"));
	JGUIUtil.addComponent ( yAxisLeft_JPanel, yAxisLeftLabel_JPanel,
		0, ++y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	int yAxisLeftLabel = 0;

	JGUIUtil.addComponent ( yAxisLeftLabel_JPanel, new JLabel ("Precision:"),
			0, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_precision_JTextField = new JTextField ( 3 );
	_graph_lefty_precision_JTextField.setToolTipText("Number of digits after decimal to display in left y-axis labels");
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel,_graph_lefty_precision_JTextField,
			1, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yAxisLeftLabel_JPanel, new JLabel ("Font:"),
			2, yAxisLeftLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_label_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel, _graph_lefty_label_fontname_JComboBox,
			3, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_label_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yAxisLeftLabel_JPanel, _graph_lefty_label_fontstyle_JComboBox,
			4, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_label_fontsize_JTextField = new JTextField ( 3 );
	_graph_lefty_label_fontsize_JTextField.setToolTipText("Left y-axis label font size in points");
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel, _graph_lefty_label_fontsize_JTextField,
			5, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yAxisLeftLabel_JPanel, new JLabel ("Minimum value:"),
			0, ++yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	List<String> values = getPropertyChoices("LeftYAxisMin");
	String value = getPropertyChoiceDefault("LeftYAxisMin");
	_graph_lefty_min_JComboBox = new SimpleJComboBox(values, true);
	_graph_lefty_min_JComboBox.select(value);
	_graph_lefty_min_JComboBox.setEnabled ( false );
	_graph_lefty_min_JComboBox.setToolTipText("Minimum y-axis value:  Auto = use full data set limits; or specify number value");
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel,_graph_lefty_min_JComboBox,
			1, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisLeftLabel_JPanel, new JLabel ("Maximum value:"),
			2, yAxisLeftLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	values = getPropertyChoices("LeftYAxisMax");
	value = getPropertyChoiceDefault("LeftYAxisMax");
	_graph_lefty_max_JComboBox = new SimpleJComboBox( values, true);
	_graph_lefty_max_JComboBox.select(value);
	_graph_lefty_max_JComboBox.setEnabled ( false );
	_graph_lefty_max_JComboBox.setToolTipText("Maximum y-axis value:  Auto = use full data set limits; or specify number value");
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel,_graph_lefty_max_JComboBox,
			3, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	_graph_lefty_ignoreunits_JCheckBox=new JCheckBox("Ignore units", false);
	_graph_lefty_ignoreunits_JCheckBox.setToolTipText("Allow time series with different units to be on same graph (default is warn if unites are incompatible)");
	_graph_lefty_ignoreunits_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel, _graph_lefty_ignoreunits_JCheckBox,
			1, ++yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yAxisLeftLabel_JPanel, new JLabel ("Units:"),
			2, yAxisLeftLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_units_JTextField = new JTextField ( 6 );
	_graph_lefty_units_JTextField.setToolTipText("Units to use for the y-axis (default for some display values)");
	_graph_lefty_units_JTextField.setEnabled(false);
	JGUIUtil.addComponent (yAxisLeftLabel_JPanel,_graph_lefty_units_JTextField,
			3, yAxisLeftLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold axis type properties.
	JPanel yAxisLeftType_JPanel = new JPanel();
	yAxisLeftType_JPanel.setLayout(gbl);
	yAxisLeftType_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Left) Type"));
	JGUIUtil.addComponent ( yAxisLeft_JPanel, yAxisLeftType_JPanel,
		0, ++y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	int yAxisLeftType = 0;

	JGUIUtil.addComponent ( yAxisLeftType_JPanel, new JLabel ("Axis type:"),
			0, ++yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_type_JComboBox = new SimpleJComboBox ( false );
	_graph_lefty_type_JComboBox.setToolTipText("Left y-axis type, for projecting data");
	_graph_lefty_type_JComboBox.setEnabled ( false );
	List<String> graphLeftYTypeChoices = new ArrayList<>();
	graphLeftYTypeChoices.add ( "Linear" );
	graphLeftYTypeChoices.add ( "Log" );
	_graph_lefty_type_JComboBox.setData(graphLeftYTypeChoices);
	JGUIUtil.addComponent (yAxisLeftType_JPanel,_graph_lefty_type_JComboBox,
			1, yAxisLeftType, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisLeftType_JPanel, new JLabel ("Major grid color:"),
			0, ++yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_majorgrid_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the grid color.
	_graph_lefty_majorgrid_color_JTextField.setEditable(false);
	_graph_lefty_majorgrid_color_JTextField.setToolTipText("Use a named color or specify as Red,Green,Blue (each 0-255)");
	JGUIUtil.addComponent ( yAxisLeftType_JPanel, _graph_lefty_majorgrid_color_JTextField,
			1, yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE,
			GridBagConstraints.WEST );
	_graph_lefty_majorgrid_color_JComboBox = new SimpleJComboBox( false );
	_graph_lefty_majorgrid_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	List<String> graphLeftMajorGridColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		graphLeftMajorGridColorChoices.add ( GRColor.COLOR_NAMES[i]);
	}
	_graph_lefty_majorgrid_color_JComboBox.setData(graphLeftMajorGridColorChoices);
	_graph_lefty_majorgrid_color_JComboBox.setMaximumRowCount(
		_graph_lefty_majorgrid_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( yAxisLeftType_JPanel, _graph_lefty_majorgrid_color_JComboBox,
			2, yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_lefty_majorgrid_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_lefty_majorgrid_color_JButton.setEnabled(false);
	JGUIUtil.addComponent (yAxisLeftType_JPanel, _graph_lefty_majorgrid_color_JButton,
			3, yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisLeftType_JPanel, new JLabel ("Major tick color:"),
		0, ++yAxisLeftType, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_majortick_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the tick color.
	_graph_lefty_majortick_color_JTextField.setEditable(false);
	_graph_lefty_majortick_color_JTextField.setToolTipText("Use a named color or specify as Red,Green,Blue (each 0-255)");
	JGUIUtil.addComponent ( yAxisLeftType_JPanel, _graph_lefty_majortick_color_JTextField,
		1, yAxisLeftType, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST );
	_graph_lefty_majortick_color_JComboBox = new SimpleJComboBox( false );
	_graph_lefty_majortick_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	List<String> graphLeftMajorTickColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		graphLeftMajorTickColorChoices.add ( GRColor.COLOR_NAMES[i]);
	}
	_graph_lefty_majortick_color_JComboBox.setData(graphLeftMajorTickColorChoices);
	_graph_lefty_majortick_color_JComboBox.setMaximumRowCount(
		_graph_lefty_majortick_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( yAxisLeftType_JPanel, _graph_lefty_majortick_color_JComboBox,
			2, yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_lefty_majortick_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_lefty_majortick_color_JButton.setEnabled(false);
	JGUIUtil.addComponent (yAxisLeftType_JPanel, _graph_lefty_majortick_color_JButton,
			3, yAxisLeftType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	List<String> leftYAxisDirectionValues = getPropertyChoices("LeftYAxisDirection");
    String leftYAxisDirectionValue = getPropertyChoiceDefault("LeftYAxisDirection");
    JGUIUtil.addComponent ( yAxisLeftType_JPanel, new JLabel ("Axis direction:"),
        0, ++yAxisLeftType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
    _graph_lefty_direction_JComboBox = new SimpleJComboBox( leftYAxisDirectionValues, true);
    _graph_lefty_direction_JComboBox.setToolTipText("Normal means minimum value at bottom and maximum at top; Reverse means maximum value at bottom and minimum at top");
    _graph_lefty_direction_JComboBox.select(leftYAxisDirectionValue);
    _graph_lefty_direction_JComboBox.setEnabled ( false );
    JGUIUtil.addComponent (yAxisLeftType_JPanel,_graph_lefty_direction_JComboBox,
        1, yAxisLeftType, 2, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Y Axis (Right) tab.

	JPanel yAxisRight_JPanel = new JPanel();
	yAxisRight_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Y Axis (Right)", null, yAxisRight_JPanel, "Right Y axis properties" );

	y = 0;
	// Create a sub-panel with a border to hold title properties.
	JPanel yAxisRightTitle_JPanel = new JPanel();
	yAxisRightTitle_JPanel.setLayout(gbl);
	yAxisRightTitle_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Right) Title"));
	JGUIUtil.addComponent ( yAxisRight_JPanel, yAxisRightTitle_JPanel,
		0, y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	int yAxisRightTitle = 0;
	JGUIUtil.addComponent ( yAxisRightTitle_JPanel, new JLabel ("Title:"),
			0, ++yAxisRightTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_title_JTextField = new JTextField ( 30 );
	_graph_righty_title_JTextField.setToolTipText("Right y-axis title, time series units by default");
	JGUIUtil.addComponent ( yAxisRightTitle_JPanel, _graph_righty_title_JTextField,
			1, yAxisRightTitle, 3, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );

	_graph_righty_title_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent (yAxisRightTitle_JPanel, _graph_righty_title_fontname_JComboBox,
			4, yAxisRightTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH ); // HORIZONTAL to resize to Max value choice.
	_graph_righty_title_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yAxisRightTitle_JPanel, _graph_righty_title_fontstyle_JComboBox,
			5, yAxisRightTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_righty_title_fontsize_JTextField = new JTextField ( 3 );
	_graph_righty_title_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (yAxisRightTitle_JPanel, _graph_righty_title_fontsize_JTextField,
			6, yAxisRightTitle, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yAxisRightTitle_JPanel, new JLabel ("Position:"),
		0, ++yAxisRightTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	List<String> yAxisRightTitlePositionValues = getPropertyChoices("RightYAxisTitlePosition");
	String yAxisRightTitlePosition = getPropertyChoiceDefault("RightYAxisTitlePosition");
	_graph_righty_title_position_JComboBox = new SimpleJComboBox(yAxisRightTitlePositionValues, true);
	_graph_righty_title_position_JComboBox.setToolTipText("Position of y-axis title");
	//_graph_righty_title_position_JComboBox.setEnabled ( false );
	_graph_righty_title_position_JComboBox.select(yAxisRightTitlePosition);
	_graph_righty_title_position_JComboBox.setToolTipText("Indicate position of right y-axis title");
	JGUIUtil.addComponent ( yAxisRightTitle_JPanel, _graph_righty_title_position_JComboBox,
		1, yAxisRightTitle, 3, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisRightTitle_JPanel, new JLabel ("Rotation:"),
		0, ++yAxisRightTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_title_rotation_JTextField = new JTextField(5);
	_graph_righty_title_rotation_JTextField.setToolTipText(
		"Rotation (clockwise degrees) of right y-axis title, 0=horizontal, 90=vertical with top of text on right");
	JGUIUtil.addComponent (yAxisRightTitle_JPanel,_graph_righty_title_rotation_JTextField,
		1, yAxisRightTitle, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold label properties.
	JPanel yAxisRightLabel_JPanel = new JPanel();
	yAxisRightLabel_JPanel.setLayout(gbl);
	yAxisRightLabel_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Right) Labels / Limits"));
	JGUIUtil.addComponent ( yAxisRight_JPanel, yAxisRightLabel_JPanel,
		0, ++y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	int yAxisRightLabel = 0;

	JGUIUtil.addComponent ( yAxisRightLabel_JPanel, new JLabel ("Precision:"),
			0, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_precision_JTextField = new JTextField ( 3 );
	_graph_righty_precision_JTextField.setToolTipText("Number of digits after decimal to display in right y-axis labels");
	JGUIUtil.addComponent (yAxisRightLabel_JPanel,_graph_righty_precision_JTextField,
			1, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yAxisRightLabel_JPanel, new JLabel ("Font:"),
			2, yAxisRightLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_label_fontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent (yAxisRightLabel_JPanel, _graph_righty_label_fontname_JComboBox,
			3, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_righty_label_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yAxisRightLabel_JPanel, _graph_righty_label_fontstyle_JComboBox,
			4, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_righty_label_fontsize_JTextField = new JTextField ( 3 );
	_graph_righty_label_fontsize_JTextField.setToolTipText("Right y-axis label font size in points");
	JGUIUtil.addComponent (yAxisRightLabel_JPanel, _graph_righty_label_fontsize_JTextField,
			5, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yAxisRightLabel_JPanel, new JLabel ("Minimum value:"),
			0, ++yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	List<String> rightYAxisMinChoices = getPropertyChoices("RightYAxisMin");
	String rightYAxisMin = getPropertyChoiceDefault("RightYAxisMin");
	_graph_righty_min_JComboBox = new SimpleJComboBox(rightYAxisMinChoices, true);
	_graph_righty_min_JComboBox.select(rightYAxisMin);
	_graph_righty_min_JComboBox.setEnabled ( false );
	_graph_righty_min_JComboBox.setToolTipText("Minimum y-axis value:  Auto = use full data set limits; or specify number value");
	JGUIUtil.addComponent (yAxisRightLabel_JPanel,_graph_righty_min_JComboBox,
			1, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisRightLabel_JPanel, new JLabel ("Maximum value:"),
			2, yAxisRightLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	values = getPropertyChoices("RightYAxisMax");
	value = getPropertyChoiceDefault("RightYAxisMax");
	_graph_righty_max_JComboBox = new SimpleJComboBox( values, true);
	_graph_righty_max_JComboBox.select(value);
	_graph_righty_max_JComboBox.setEnabled ( false );
	_graph_righty_max_JComboBox.setToolTipText("Maximum y-axis value:  Auto = use full data set limits; or specify number value");
	JGUIUtil.addComponent (yAxisRightLabel_JPanel,_graph_righty_max_JComboBox,
			3, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	_graph_righty_ignoreunits_JCheckBox=new JCheckBox("Ignore units", false);
	_graph_righty_ignoreunits_JCheckBox.setToolTipText("Allow time series with different units to be on same graph (default is warn if unites are incompatible)");
	_graph_righty_ignoreunits_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent (yAxisRightLabel_JPanel, _graph_righty_ignoreunits_JCheckBox,
			1, ++yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yAxisRightLabel_JPanel, new JLabel ("Units:"),
			2, yAxisRightLabel, 1, 1, 0, 0,
			insetsTLBRWestSpace, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_units_JTextField = new JTextField ( 6 );
	_graph_righty_units_JTextField.setToolTipText("Units to use for the y-axis (default for some display values)");
	_graph_righty_units_JTextField.setEnabled(false);
	JGUIUtil.addComponent (yAxisRightLabel_JPanel,_graph_righty_units_JTextField,
			3, yAxisRightLabel, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Create a sub-panel with a border to hold axis type properties.
	JPanel yAxisRightType_JPanel = new JPanel();
	yAxisRightType_JPanel.setLayout(gbl);
	yAxisRightType_JPanel.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.BLACK,1),"Y Axis (Right) Type"));
	JGUIUtil.addComponent ( yAxisRight_JPanel, yAxisRightType_JPanel,
		0, ++y, 5, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	int yAxisRightType = 0;

	JGUIUtil.addComponent ( yAxisRightType_JPanel, new JLabel ("Axis type:"),
			0, ++yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_type_JComboBox = new SimpleJComboBox ( false );
	_graph_righty_type_JComboBox.setToolTipText("Right y-axis type, for projecting data");
	_graph_righty_type_JComboBox.setEnabled ( false );
	List<String> graphRightYTypeChoices = new ArrayList<>();
	graphRightYTypeChoices.add ( "Linear" );
	graphRightYTypeChoices.add ( "Log" );
	_graph_righty_type_JComboBox.setData(graphRightYTypeChoices);
	JGUIUtil.addComponent (yAxisRightType_JPanel,_graph_righty_type_JComboBox,
			1, yAxisRightType, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisRightType_JPanel, new JLabel ("Major grid color:"),
			0, ++yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_majorgrid_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the grid color.
	_graph_righty_majorgrid_color_JTextField.setEditable(false);
	_graph_righty_majorgrid_color_JTextField.setToolTipText("Use a named color or specify as Red,Green,Blue (each 0-255)");
	JGUIUtil.addComponent ( yAxisRightType_JPanel, _graph_righty_majorgrid_color_JTextField,
			1, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE,
			GridBagConstraints.WEST );
	_graph_righty_majorgrid_color_JComboBox = new SimpleJComboBox( false );
	_graph_righty_majorgrid_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	List<String> graphRightMajorGridColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		graphRightMajorGridColorChoices.add ( GRColor.COLOR_NAMES[i]);
	}
	_graph_righty_majorgrid_color_JComboBox.setData(graphRightMajorGridColorChoices);
	_graph_righty_majorgrid_color_JComboBox.setMaximumRowCount(
		_graph_righty_majorgrid_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( yAxisRightType_JPanel, _graph_righty_majorgrid_color_JComboBox,
			2, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_righty_majorgrid_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_righty_majorgrid_color_JButton.setEnabled(false);
	JGUIUtil.addComponent (yAxisRightType_JPanel, _graph_righty_majorgrid_color_JButton,
			3, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yAxisRightType_JPanel, new JLabel ("Major tick color:"),
			0, ++yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_majortick_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the tick color.
	_graph_righty_majortick_color_JTextField.setEditable(false);
	_graph_righty_majortick_color_JTextField.setToolTipText("Use a named color or specify as Red,Green,Blue (each 0-255)");
	JGUIUtil.addComponent ( yAxisRightType_JPanel, _graph_righty_majortick_color_JTextField,
			1, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE,
			GridBagConstraints.WEST );
	_graph_righty_majortick_color_JComboBox = new SimpleJComboBox( false );
	_graph_righty_majortick_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	List<String> graphRightYMajorTickColorChoices = new ArrayList<>();
	for ( int i = 0; i < size; i++ ) {
		graphRightYMajorTickColorChoices.add ( GRColor.COLOR_NAMES[i]);
	}
	_graph_righty_majortick_color_JComboBox.setData(graphRightYMajorTickColorChoices);
	_graph_righty_majortick_color_JComboBox.setMaximumRowCount(
		_graph_righty_majortick_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( yAxisRightType_JPanel, _graph_righty_majortick_color_JComboBox,
			2, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_righty_majortick_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_righty_majortick_color_JButton.setEnabled(false);
	JGUIUtil.addComponent (yAxisRightType_JPanel, _graph_righty_majortick_color_JButton,
			3, yAxisRightType, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

    List<String> rightYAxisDirectionValues = getPropertyChoices("RightYAxisDirection");
    String rightYAxisDirectionValue = getPropertyChoiceDefault("RightYAxisDirection");
    JGUIUtil.addComponent ( yAxisRightType_JPanel, new JLabel ("Axis direction:"),
        0, ++yAxisRightType, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
    _graph_righty_direction_JComboBox = new SimpleJComboBox( rightYAxisDirectionValues, true);
    _graph_righty_direction_JComboBox.setToolTipText("Normal means minimum value at bottom and maximum at top; Reverse means maximum value at bottom and minimum at top");
    _graph_righty_direction_JComboBox.select(rightYAxisDirectionValue);
    _graph_righty_direction_JComboBox.setEnabled ( false );
    JGUIUtil.addComponent (yAxisRightType_JPanel,_graph_righty_direction_JComboBox,
        1, yAxisRightType, 2, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Label tab.

	JPanel label_JPanel = new JPanel();
	label_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Data Point Label", null, label_JPanel, "Data point label properties" );

	y = -1;
	JGUIUtil.addComponent ( label_JPanel, new JLabel (
			"These properties set the default data point labels for all time series, "
			+ "which can be overriden by Time Series Properties / Data Point Label properties."),
			0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel,
			new JSeparator(SwingConstants.HORIZONTAL),
			0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel, new JLabel ("Position:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_datalabelposition_JComboBox = new SimpleJComboBox( false );
	_graph_datalabelposition_JComboBox.setToolTipText("Specify the label format to annotate each data point");
	String [] positions = GRText.getTextPositions();
	_graph_datalabelposition_JComboBox.setMaximumRowCount(positions.length);
	List<String> graphDataLabelPosChoices = new ArrayList<>();
	for ( int i = 0; i < positions.length; i++ ) {
		graphDataLabelPosChoices.add ( positions[i] );
	}
	_graph_datalabelposition_JComboBox.setData(graphDataLabelPosChoices);
	_graph_datalabelposition_JComboBox.select ( "Right" );
	JGUIUtil.addComponent ( label_JPanel,_graph_datalabelposition_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent (label_JPanel,new JLabel("Format (see choices):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_datalabelformat_JTextField = new JTextField ( 15 );
	_graph_datalabelformat_JTextField.setToolTipText("Format for data label, using time series data value specifiers");
	JGUIUtil.addComponent ( label_JPanel, _graph_datalabelformat_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_datalabelformat_JComboBox = new SimpleJComboBox( false );
	_graph_datalabelformat_JComboBox.addItemListener ( this );
	String [] formats = TimeUtil.getDateTimeFormatSpecifiers ( true, true, false );
	List<String> graphDataLabelFormatChoices = new ArrayList<>();
	for ( int i = 0; i < formats.length; i++ ) {
		graphDataLabelFormatChoices.add ( formats[i] );
	}
	graphDataLabelFormatChoices.add ( "%v - Value" );
	graphDataLabelFormatChoices.add ( "%U - Units" );
	graphDataLabelFormatChoices.add ( "%q - Flag" );
	_graph_datalabelformat_JComboBox.setData(graphDataLabelFormatChoices);
	_graph_datalabelformat_JComboBox.setMaximumRowCount( _graph_datalabelformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( label_JPanel, _graph_datalabelformat_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent (label_JPanel, _graph_datalabelfontname_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(label_JPanel, _graph_datalabelfontstyle_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontsize_JTextField = new JTextField ( 2 );
	_graph_datalabelfontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (label_JPanel,_graph_datalabelfontsize_JTextField,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Legend tab.

	JPanel legend_JPanel = new JPanel();
	legend_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Legend", null, legend_JPanel, "Legend properties" );

	y = 0;
	// Need to add "LegendEnabled" checkbox.

	JGUIUtil.addComponent ( legend_JPanel, new JLabel ( "If the Format is Auto, the Time Series Legend Format or defaults will be used."),
			0, y, 6, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( legend_JPanel,
			new JSeparator(SwingConstants.HORIZONTAL),
			0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Legend position (left y-axis):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_legendposition_JComboBox = new SimpleJComboBox( false );
	_graph_lefty_legendposition_JComboBox.setToolTipText("Position of graph legend \"box\" for left y-axis data relative to the data view");
	List<String> graphLeftYLegendPosChoices = new ArrayList<>();
	graphLeftYLegendPosChoices.add ( "Bottom" );
	graphLeftYLegendPosChoices.add ( "BottomLeft" );
	graphLeftYLegendPosChoices.add ( "BottomRight" );
	graphLeftYLegendPosChoices.add ( "InsideLowerLeft");
	graphLeftYLegendPosChoices.add ( "InsideLowerRight");
	graphLeftYLegendPosChoices.add ( "InsideUpperLeft");
	graphLeftYLegendPosChoices.add ( "InsideUpperRight");
	graphLeftYLegendPosChoices.add ( "Left" );
	graphLeftYLegendPosChoices.add ( "None" );
	graphLeftYLegendPosChoices.add ( "Right" );
	_graph_lefty_legendposition_JComboBox.setData(graphLeftYLegendPosChoices);
	_graph_lefty_legendposition_JComboBox.setMaximumRowCount(_graph_lefty_legendposition_JComboBox.getItemCount());
	// Disable for now - mainly need the option of vertical or horizontal edge for spacing.
	//_graph_legendposition_Choice.addItem ( "Top" );
	JGUIUtil.addComponent ( legend_JPanel, _graph_lefty_legendposition_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Legend position (right y-axis):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_righty_legendposition_JComboBox = new SimpleJComboBox( false );
	_graph_righty_legendposition_JComboBox.setToolTipText("Position of graph legend \"box\" for right y-axis data relative to the data view");
	List<String> graphRightYLegendPosChoices = new ArrayList<>();
	graphRightYLegendPosChoices.add ( "Bottom" );
	graphRightYLegendPosChoices.add ( "BottomLeft" );
	graphRightYLegendPosChoices.add ( "BottomRight" );
	graphRightYLegendPosChoices.add ( "InsideLowerLeft");
	graphRightYLegendPosChoices.add ( "InsideLowerRight");
	graphRightYLegendPosChoices.add ( "InsideUpperLeft");
	graphRightYLegendPosChoices.add ( "InsideUpperRight");
	graphRightYLegendPosChoices.add ( "Left" );
	graphRightYLegendPosChoices.add ( "None" );
	graphRightYLegendPosChoices.add ( "Right" );
	_graph_righty_legendposition_JComboBox.setData(graphRightYLegendPosChoices);
	_graph_righty_legendposition_JComboBox.setMaximumRowCount(_graph_righty_legendposition_JComboBox.getItemCount());
	// Disable for now - mainly need the option of vertical or horizontal edge for spacing.
	//_graph_legendposition_Choice.addItem ( "Top" );
	JGUIUtil.addComponent ( legend_JPanel, _graph_righty_legendposition_JComboBox,
		1, y, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Format (see choices):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_legendformat_JTextField = new JTextField ( 20 );
	_graph_legendformat_JTextField.setToolTipText(
			"Format specifiers for legend, using time series properties, can use % specifiers in list and ${ts:property} for time series property name");
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendformat_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_legendformat_JComboBox = new SimpleJComboBox( false );
	_graph_legendformat_JComboBox.addItemListener ( this );
	formats = TSUtil.getTSFormatSpecifiers ( true );
	List<String> graphLegendFormatChoices = new ArrayList<>();
	graphLegendFormatChoices.add ( "Auto" );
	for ( int i = 0; i < formats.length; i++ ) {
		graphLegendFormatChoices.add ( formats[i] );
	}
	_graph_legendformat_JComboBox.setData(graphLegendFormatChoices);
	_graph_legendformat_JComboBox.setMaximumRowCount(_graph_legendformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendformat_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legendfontname_JComboBox = JGUIUtil.newFontNameJComboBox(true,true);
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendfontname_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legendfontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendfontstyle_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legend_fontsize_JTextField = new JTextField ( 2 );
	_graph_legend_fontsize_JTextField.setToolTipText("Font size in points");
	JGUIUtil.addComponent (legend_JPanel,_graph_legend_fontsize_JTextField,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Zoom tab.

	JPanel zoom_JPanel = new JPanel();
	zoom_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ("Zoom", null, zoom_JPanel,"Zoom properties");

	_graph_zoomenabled_JCheckBox = new JCheckBox("Zoom enabled", true);
	_graph_zoomenabled_JCheckBox.setToolTipText("If zoom is enabled use the mouse to draw a box on the graph to zoom - generally only x-axis will zoom");
	_graph_zoomenabled_JCheckBox.setEnabled ( false );
	_graph_zoomenabled_JCheckBox.setSelected ( true );
	_graph_zoomenabled_JCheckBox.setEnabled ( false );
	y = 0;
	JGUIUtil.addComponent (zoom_JPanel,_graph_zoomenabled_JCheckBox,
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent (zoom_JPanel,new JLabel("Zoom group:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_zoomgroup_JTextField = new JTextField ( 4 );
	_graph_zoomgroup_JTextField.setToolTipText("Graphs in the same zoom group will zoom together if one graph is zoomed");
	_graph_zoomgroup_JTextField.setEditable(false);
	JGUIUtil.addComponent (zoom_JPanel,_graph_zoomgroup_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_zoomgroup_JTextField.setEnabled ( false );

	// Analysis tab.

	// This panel is the main panel managed by the TabbedPane.

	_graph_analysis_JPanel = new JPanel();
	_graph_analysis_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab( "Analysis",null,_graph_analysis_JPanel, "Analysis properties");

	// Create a blank panel that is set visible when there is no analysis for the graph.

	_blank_analysis_JPanel = new JPanel ();
	_blank_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent ( _graph_analysis_JPanel, _blank_analysis_JPanel,
			0, 0, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_blank_analysis_JPanel.setVisible ( true );
	JGUIUtil.addComponent ( _graph_analysis_JPanel, new JLabel ( "Analysis properties are displayed when an analysis is displayed, for example regression."),
			0, 0, 6, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( _blank_analysis_JPanel, new JLabel ( "Graph has no analysis." ),
			0, 1, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	// Create a panel that is set visible for the XY-Scatter graph.

	_xyscatter_analysis_JPanel = new JPanel ();
	_xyscatter_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (_graph_analysis_JPanel,
			_xyscatter_analysis_JPanel, 0, 0, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_xyscatter_analysis_JPanel.setVisible ( false );
	y = 0;
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel,
			new JLabel ( "Select the parameters for the XY-Scatter Graph curve fit analysis (applies to all time series)." ),
			0, y, 7, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Curve fit method:" ),
			0,++y, 2, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_method_JComboBox = new SimpleJComboBox ( false );
	_xyscatter_analysis_method_JComboBox.setToolTipText("Regression method to develop relationship equations");
	_xyscatter_analysis_method_JComboBox.addItemListener ( this );
	List<String> xyscatterMethodChoices = new ArrayList<>();
	xyscatterMethodChoices.add("MOVE2");
	xyscatterMethodChoices.add("OLSRegression");
	_xyscatter_analysis_method_JComboBox.setData(xyscatterMethodChoices);
	_xyscatter_analysis_method_JComboBox.select("OLSRegression");
	_xyscatter_analysis_method_JComboBox.setToolTipText(
		"Indicate the method used to determine the line of best fit.");
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, _xyscatter_analysis_method_JComboBox,
			2, y, 2, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Transformation:" ),
			4, y, 2, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_transform_JComboBox = new SimpleJComboBox ( false );
	_xyscatter_analysis_transform_JComboBox.setToolTipText("Transformation on input data before performing regression");
	_xyscatter_analysis_transform_JComboBox.addItemListener ( this );
	List<String> xyscatterTransformChoices = new ArrayList<>();
	xyscatterTransformChoices.add("Log");
	xyscatterTransformChoices.add("None");
	_xyscatter_analysis_transform_JComboBox.setData(xyscatterTransformChoices);
	_xyscatter_analysis_transform_JComboBox.select("None");
	_xyscatter_analysis_transform_JComboBox.setEnabled(false);
	_xyscatter_analysis_transform_JComboBox.setToolTipText("Indicate how to transform data before analysis." );
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, _xyscatter_analysis_transform_JComboBox,
			6, y, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Number of equations:" ),
			0,++y, 2, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_neqn_JComboBox = new SimpleJComboBox ( false );
	_xyscatter_analysis_neqn_JComboBox.setToolTipText("Indicate whether one equation or monthly equations should be used for regression "
		+ " - monthly will show month number on right end of line");
	_xyscatter_analysis_neqn_JComboBox.addItemListener ( this );
	List<String> xyNeqnTransformChoices = new ArrayList<>();
	xyNeqnTransformChoices.add("MonthlyEquations");
	xyNeqnTransformChoices.add("OneEquation");
	_xyscatter_analysis_neqn_JComboBox.setData(xyNeqnTransformChoices);
	_xyscatter_analysis_neqn_JComboBox.select("OneEquation");
	_xyscatter_analysis_neqn_JComboBox.setToolTipText(
		"Data can be analyzed with a single or monthly equations." );
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, _xyscatter_analysis_neqn_JComboBox,
			2, y, 2, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Month(s) of Interest:" ),
			4, y, 2, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_month_JTextField = new JTextField (10);
	_xyscatter_analysis_month_JTextField.setToolTipText(
		"Specify months of interest as numbers 1-12, separated by spaces or commas." );
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, _xyscatter_analysis_month_JTextField,
			6, y, 1, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ("Dependent analysis period:" ),
		0, ++y, 2, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_dep_analysis_period_start_JTextField = new JTextField ( "", 15 );
	_dep_analysis_period_start_JTextField.setEnabled ( false );
	_dep_analysis_period_start_JTextField.addKeyListener ( this );
	_dep_analysis_period_start_JTextField.setToolTipText(
		"Specify blank to analyze full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _dep_analysis_period_start_JTextField,
		2, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ( "to" ),
		4, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	_dep_analysis_period_end_JTextField = new JTextField ( "", 15 );
	_dep_analysis_period_end_JTextField.setEnabled ( false );
	_dep_analysis_period_end_JTextField.addKeyListener ( this );
	_dep_analysis_period_end_JTextField.setToolTipText(
		"Specify blank to analyze full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel,_dep_analysis_period_end_JTextField,
		5, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ( "Independent analysis Period:" ),
		0, ++y, 2, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_ind_analysis_period_start_JTextField = new JTextField ( "", 15 );
	_ind_analysis_period_start_JTextField.setEnabled ( false );
	_ind_analysis_period_start_JTextField.addKeyListener ( this );
	_ind_analysis_period_start_JTextField.setToolTipText(
		"Specify blank to analyze full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _ind_analysis_period_start_JTextField,
		2, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ( "to" ),
		4, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	_ind_analysis_period_end_JTextField = new JTextField ( "", 15 );
	_ind_analysis_period_end_JTextField.setEnabled ( false );
	_ind_analysis_period_end_JTextField.addKeyListener ( this );
	_ind_analysis_period_end_JTextField.setToolTipText(
		"Specify blank to analyze full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel,
		_ind_analysis_period_end_JTextField,
		5, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// The following used only when analyzing for filling data.

	_xyscatter_analysis_fill_JCheckBox = new JCheckBox ( "Analyze (RSME) for filling", false );
	_xyscatter_analysis_fill_JCheckBox.addItemListener ( this );
	_xyscatter_analysis_fill_JCheckBox.setToolTipText("Default is to analyze for line of best fit.");
	JGUIUtil.addComponent (_xyscatter_analysis_JPanel, _xyscatter_analysis_fill_JCheckBox,
			0, ++y, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Intercept:" ),
			4, y, 2, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_intercept_JTextField = new JTextField ( "", 15 );
	_xyscatter_analysis_intercept_JTextField.setEnabled ( false );
	_xyscatter_analysis_intercept_JTextField.addKeyListener ( this );
	_xyscatter_analysis_intercept_JTextField.setToolTipText("If not blank, it must be 0 (zero).");
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _xyscatter_analysis_intercept_JTextField,
		6, y, 1, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ( "Fill period:" ),
		0, ++y, 2, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_xyscatter_analysis_fill_period_start_JTextField = new JTextField ( "", 15 );
	_xyscatter_analysis_fill_period_start_JTextField.setEnabled ( false );
	_xyscatter_analysis_fill_period_start_JTextField.addKeyListener ( this);
	_xyscatter_analysis_fill_period_start_JTextField.setToolTipText(
		"Specify blank to analyze filling the full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _xyscatter_analysis_fill_period_start_JTextField,
		2, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, new JLabel ( "to" ),
		4, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	_xyscatter_analysis_fill_period_end_JTextField =new JTextField ("", 15);
	_xyscatter_analysis_fill_period_end_JTextField.setEnabled ( false );
	_xyscatter_analysis_fill_period_end_JTextField.addKeyListener ( this );
	_xyscatter_analysis_fill_period_end_JTextField.setToolTipText(
		"Specify blank to analyze filling the full period, or specify using standard date/time formats." );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _xyscatter_analysis_fill_period_end_JTextField,
		5, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Annotations.

	_graph_JTabbedPane.addTab( "Annotations", null, createAnnotationJPanel(), "Annotation properties");

	// Developer tab.

	JPanel devJPanel = new JPanel();
	devJPanel.setLayout(gbl);
	_graph_JTabbedPane.addTab("Developer", null, devJPanel, "Developer Properties" );

	y = -1;
	JGUIUtil.addComponent ( devJPanel, new JLabel ( "These properties are used by software developers.  Remember to press Apply to change the graph."),
		0, ++y, 6, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( devJPanel,
		new JSeparator(SwingConstants.HORIZONTAL),
		0, ++y, 6, 1, 0, 0, _insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent(devJPanel, new JLabel ("Left y-axis original graph type:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_graphLeftYAxisOriginalGraphType_JTextField = new JTextField(10);
	_graphLeftYAxisOriginalGraphType_JTextField.setEnabled(false);
	_graphLeftYAxisOriginalGraphType_JTextField.setToolTipText("Left y-axis original graph type when graph window was opened.");
	JGUIUtil.addComponent(devJPanel, _graphLeftYAxisOriginalGraphType_JTextField,
		1, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(devJPanel, new JLabel ("Right y-axis original graph type:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_graphRightYAxisOriginalGraphType_JTextField = new JTextField(10);
	_graphRightYAxisOriginalGraphType_JTextField.setEnabled(false);
	_graphRightYAxisOriginalGraphType_JTextField.setToolTipText("Right y-axis original graph type when graph window was opened.");
	JGUIUtil.addComponent(devJPanel, _graphRightYAxisOriginalGraphType_JTextField,
		1, y, 1, 1, 0, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	return graph_JPanel;
}

/**
Update the display with the annotation properties for a tsproduct.
@param isub the subproduct that was/is selected.
@param iann the annotation number that is selected.
*/
private void displayAnnotationProperties(int isub, int iann) {
	String prop_val = null;
	__selectedAnnotationIndex = iann;

	// First update generic annotation properties.

	// AnnotationID
	prop_val = _tsproduct.getLayeredPropValue("AnnotationID", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("AnnotationID", isub, iann, true);
	}
	__annotation_id_JTextField.setText(prop_val);

	// AnnotationTableID
	prop_val = _tsproduct.getLayeredPropValue("AnnotationTableID", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("AnnotationTableID", isub, iann, true);
	}
	__annotation_tableid_JTextField.setText(prop_val);

	// ShapeType
	String shapeType = _tsproduct.getLayeredPropValue("ShapeType", isub, iann, false, true);
	if (shapeType == null) {
		shapeType = _tsproduct.getDefaultPropValue("ShapeType", isub, iann, true);
	}
	__annotation_ShapeType_JComboBox.select(shapeType);

	// Order
	prop_val = _tsproduct.getLayeredPropValue("Order", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("Order", isub, iann, true);
	}
	__annotation_Order_JComboBox.select(prop_val);

	// XAxisSystem
	prop_val = _tsproduct.getLayeredPropValue("XAxisSystem", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("XAxisSystem", isub, iann, true);
	}
	__annotation_XAxisSystem_JComboBox.select(prop_val);

	// YAxis
	prop_val = _tsproduct.getLayeredPropValue("YAxis", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("YAxis", isub, iann, true);
	}
	__annotation_YAxis_JComboBox.select(prop_val);

	// YAxisSystem
	prop_val = _tsproduct.getLayeredPropValue("YAxisSystem", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("YAxisSystem", isub, iann, true);
	}
	__annotation_YAxisSystem_JComboBox.select(prop_val);

	// Now update properties for each annotation shape type.
	// Although some are shared, it is easier to duplicate some code so as to be sure about which properties are used.

	// Color (use Color2 because of conflict with Swing class).
	String Color2 = _tsproduct.getLayeredPropValue("Color", isub, iann, false, true);
	if (Color2 == null) {
		Color2 = _tsproduct.getDefaultPropValue("Color", isub, iann, true);
	}

	if ( shapeType.equalsIgnoreCase("Line") ) {
		// Color
		__annotation_line_color_JTextField.setText(Color2);
		try {
			JGUIUtil.selectIgnoreCase( __annotation_line_color_JComboBox, Color2);
			try {
				__annotation_line_color_JTextField.setBackground(
					(Color)GRColor.parseColor(__annotation_line_color_JTextField.getText()));
			}
			catch (Exception e2) {
				__annotation_line_color_JTextField.setBackground(Color.white);
			}
		}
		catch (Exception e) {
			__annotation_line_color_JComboBox.select("None");
			__annotation_line_color_JTextField.setBackground(Color.white);
		}
		// LineWidth
		prop_val = _tsproduct.getLayeredPropValue("LineWidth", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("LineWidth", isub, iann, true);
		}
		__annotation_line_LineWidth_JTextField.setText(prop_val);
		// LineStyle
		prop_val = _tsproduct.getLayeredPropValue("LineStyle", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("LineStyle", isub, iann, true);
		}
		__annotationLineStyleJComboBox.select(prop_val);
		// Points
		prop_val = _tsproduct.getLayeredPropValue("Points", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = "";
		}
		List<String> v = StringUtil.breakStringList(prop_val, ",", 0);
		if (v.size() != 4) {
			__annotation_line_PointX1_JTextField.setText("0");
			__annotation_line_PointY1_JTextField.setText("0");
			__annotation_line_PointX2_JTextField.setText("1");
			__annotation_line_PointY2_JTextField.setText("1");
		}
		else {
			__annotation_line_PointX1_JTextField.setText(("" + v.get(0)).trim());
			__annotation_line_PointY1_JTextField.setText(("" + v.get(1)).trim());
			__annotation_line_PointX2_JTextField.setText(("" + v.get(2)).trim());
			__annotation_line_PointY2_JTextField.setText(("" + v.get(3)).trim());
		}
	}
	else if ( shapeType.equalsIgnoreCase("Rectangle") ) {
		// Color
		__annotation_rectangle_color_JTextField.setText(Color2);
		try {
			JGUIUtil.selectIgnoreCase( __annotation_rectangle_color_JComboBox, Color2);
			try {
				__annotation_rectangle_color_JTextField.setBackground(
					(Color)GRColor.parseColor(__annotation_rectangle_color_JTextField.getText()));
			}
			catch (Exception e2) {
				__annotation_rectangle_color_JTextField.setBackground(Color.white);
			}
		}
		catch (Exception e) {
			__annotation_rectangle_color_JComboBox.select("None");
			__annotation_rectangle_color_JTextField.setBackground(Color.white);
		}
		// Points
		prop_val = _tsproduct.getLayeredPropValue("Points", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = "";
		}
		List<String> v = StringUtil.breakStringList(prop_val, ",", 0);
		if (v.size() != 4) {
			__annotation_rectangle_PointX1_JTextField.setText("0");
			__annotation_rectangle_PointY1_JTextField.setText("0");
			__annotation_rectangle_PointX2_JTextField.setText("1");
			__annotation_rectangle_PointY2_JTextField.setText("1");
		}
		else {
			__annotation_rectangle_PointX1_JTextField.setText(("" + v.get(0)).trim());
			__annotation_rectangle_PointY1_JTextField.setText(("" + v.get(1)).trim());
			__annotation_rectangle_PointX2_JTextField.setText(("" + v.get(2)).trim());
			__annotation_rectangle_PointY2_JTextField.setText(("" + v.get(3)).trim());
		}
	}
	else if ( shapeType.equalsIgnoreCase("Symbol") ) {
		// Color
		__annotation_symbol_color_JTextField.setText(Color2);
		try {
			JGUIUtil.selectIgnoreCase(__annotation_symbol_color_JComboBox, Color2);
			try {
				__annotation_symbol_color_JTextField.setBackground(
					(Color)GRColor.parseColor(__annotation_symbol_color_JTextField.getText()));
			}
			catch (Exception e2) {
				__annotation_symbol_color_JTextField.setBackground(Color.white);
			}
		}
		catch (Exception e) {
			__annotation_symbol_color_JComboBox.select("None");
			__annotation_symbol_color_JTextField.setBackground(Color.white);
		}
		// Point
		prop_val = _tsproduct.getLayeredPropValue("Point", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = "";
		}
		List<String> v = StringUtil.breakStringList(prop_val, ",", 0);
		if (v.size() != 2) {
			__annotation_symbol_PointX_JTextField.setText("0");
			__annotation_symbol_PointY_JTextField.setText("0");
		}
		else {
			__annotation_symbol_PointX_JTextField.setText(("" + v.get(0)).trim());
			__annotation_symbol_PointY_JTextField.setText(("" + v.get(1)).trim());
		}
		// SymbolStyle
		prop_val = _tsproduct.getLayeredPropValue("SymbolStyle", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("SymbolStyle", isub, iann, true);
		}
		__annotation_symbol_SymbolStyle_JComboBox.select(prop_val);
		// SymbolSize
		prop_val = _tsproduct.getLayeredPropValue("SymbolSize", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("SymbolSize", isub, iann, true);
		}
		__annotation_symbol_SymbolSize_JComboBox.select(prop_val);
		// SymbolPosition
		prop_val = _tsproduct.getLayeredPropValue("SymbolPosition", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("SymbolPosition", isub, iann, true);
		}
		__annotation_symbol_SymbolPosition_JComboBox.select(prop_val);
	}
	else if ( shapeType.equalsIgnoreCase("Text") ) {
		// Color
		__annotation_text_color_JTextField.setText(Color2);
		try {
			JGUIUtil.selectIgnoreCase(__annotation_text_color_JComboBox, Color2);
			try {
				__annotation_text_color_JTextField.setBackground(
					(Color)GRColor.parseColor(__annotation_text_color_JTextField.getText()));
			}
			catch (Exception e2) {
				__annotation_text_color_JTextField.setBackground(Color.white);
			}
		}
		catch (Exception e) {
			__annotation_text_color_JComboBox.select("None");
			__annotation_text_color_JTextField.setBackground(Color.white);
		}
		// FontSize
		prop_val = _tsproduct.getLayeredPropValue("FontSize", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("FontSize", isub, iann, true);
		}
		__annotation_text_FontSize_JTextField.setText(prop_val);
		// FontStyle
		prop_val = _tsproduct.getLayeredPropValue("FontStyle", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("FontStyle", isub, iann, true);
		}
		__annotation_text_FontStyle_JComboBox.select(prop_val);
		// FontName
		prop_val = _tsproduct.getLayeredPropValue("FontName", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("FontName", isub, iann, true);
		}
		__annotation_text_FontName_JComboBox.select(prop_val);
		// Point
		prop_val = _tsproduct.getLayeredPropValue("Point", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = "";
		}
		List<String> v = StringUtil.breakStringList(prop_val, ",", 0);
		if (v.size() != 2) {
			__annotation_text_PointX_JTextField.setText("0");
			__annotation_text_PointY_JTextField.setText("0");
		}
		else {
			__annotation_text_PointX_JTextField.setText(("" + v.get(0)).trim());
			__annotation_text_PointY_JTextField.setText(("" + v.get(1)).trim());
		}
		// Text
		prop_val = _tsproduct.getLayeredPropValue("Text", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("Text", isub, iann, true);
		}
		__annotation_text_Text_JTextField.setText(prop_val);
		// TextPosition
		prop_val = _tsproduct.getLayeredPropValue("TextPosition", isub, iann, false, true);
		if (prop_val == null) {
			prop_val = _tsproduct.getDefaultPropValue("TextPosition", isub, iann, true);
		}
		__annotation_text_Position_JComboBox.select(prop_val);
	}
}

/**
Update the data components with settings appropriate for the TSProduct.
This should be called when a time series is selected.
*/
private void displayDataProperties ( int isub, int its ) {
	String prop_val;

	// TODO SAM 2016-10-21 need to allow graph type on time series for basic graphs.
	// Get the graph type, which influences some settings
	// (for now use the subproduct graph type since we don't allow individual time series to be different).

	TSGraphType graphType = TSGraphType.valueOfIgnoreCase ( _graph_lefty_graphtype_JComboBox.getSelected() );

	// "Color"

	prop_val = _tsproduct.getLayeredPropValue ( "Color", isub, its, false );

	// Message.printStatus(2, "", "Display data properties for " + isub + " / " + its);
	// Message.printStatus(2, "", "  COLOR: " + prop_val);

	_ts_color_JTextField.setText(prop_val);
	try {
		JGUIUtil.selectIgnoreCase(_ts_color_JComboBox,prop_val);
		try {
			_ts_color_JTextField.setBackground( (Color)GRColor.parseColor(_ts_color_JTextField.getText()));
		}
		catch (Exception e2) {
			_ts_color_JTextField.setBackground(Color.white);
		}
	}
	catch (Exception e) {
		_ts_color_JComboBox.select("None");
		_ts_color_JTextField.setBackground(Color.white);
	}

	// "DataLabelFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFormat", isub, its, false );
	_ts_datalabelformat_JTextField.setText(prop_val);

	// "DataLabelPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelPosition", isub, its, false );
	try {
	    JGUIUtil.selectIgnoreCase(_ts_datalabelposition_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		_ts_datalabelposition_JComboBox.select( _tsproduct.getDefaultPropValue("DataLabelPosition",isub,its) );
	}

	// "Enabled"

	prop_val = _tsproduct.getLayeredPropValue("Enabled", isub, its, false);
	if ((prop_val == null) || prop_val.equalsIgnoreCase("true")) {
		_ts_enabled_JCheckBox.setSelected(true);
	}
	else {
		_ts_enabled_JCheckBox.setSelected(false);
	}

	// "FlaggedDataSymbolStyle"

    prop_val = _tsproduct.getLayeredPropValue ( "FlaggedDataSymbolStyle", isub, its, false );
    if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ||
        (graphType == TSGraphType.BAR) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
        _ts_flaggedDataSymbolStyle_JComboBox.select("");
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(false);
    }
    else {
        try {
            JGUIUtil.selectIgnoreCase(_ts_flaggedDataSymbolStyle_JComboBox, prop_val);
        }
        catch (Exception e) {
            _ts_flaggedDataSymbolStyle_JComboBox.select(
                _tsproduct.getDefaultPropValue("FlaggedDataSymbolStyle",isub,its));
        }
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(true);
    }

	// "LegendFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFormat", isub, its, false );
	_ts_legendformat_JTextField.setText(prop_val);

	// "XAxis"

	prop_val = _tsproduct.getLayeredPropValue ( "XAxis", isub, its, false );
	try {
	    JGUIUtil.selectIgnoreCase(_ts_xaxis_JComboBox, prop_val );
	}
	catch ( Exception e ) {
		_ts_xaxis_JComboBox.select(_tsproduct.getDefaultPropValue("XAxis",isub,its));
	}

	// "YAxis"

	prop_val = _tsproduct.getLayeredPropValue ("YAxis", isub, its, false );
	try {
	    JGUIUtil.selectIgnoreCase(_ts_yaxis_JComboBox, prop_val );
	}
	catch ( Exception e ) {
		_ts_yaxis_JComboBox.select(_tsproduct.getDefaultPropValue("YAxis",isub,its));
	}

	// "LineStyle"

	prop_val = _tsproduct.getLayeredPropValue("LineStyle", isub, its,false);
	if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ||
	     (graphType == TSGraphType.BAR) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		_ts_linestyle_JComboBox.select("Solid");
		_ts_linestyle_JComboBox.setEnabled(false);
	}
	else if (graphType == TSGraphType.POINT) {
		_ts_linestyle_JComboBox.select("None");
		_ts_linestyle_JComboBox.setEnabled(false);
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_ts_linestyle_JComboBox,prop_val);
		}
		catch (Exception e) {
			_ts_linestyle_JComboBox.select(_tsproduct.getDefaultPropValue("LineStyle",isub,its));
		}
		_ts_linestyle_JComboBox.setEnabled(true);
	}

	// "LineWidth"

	prop_val = _tsproduct.getLayeredPropValue("LineWidth", isub, its,false);
	if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ||
	    (graphType == TSGraphType.BAR) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		_ts_linewidth_JComboBox.select("0");
		_ts_linewidth_JComboBox.setEnabled(false);
	}
	else if (graphType == TSGraphType.POINT) {
		_ts_linewidth_JComboBox.select("0");
		_ts_linewidth_JComboBox.setEnabled(false);
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_ts_linewidth_JComboBox, prop_val);
		}
		catch (Exception e) {
			_ts_linewidth_JComboBox.select(_tsproduct.getDefaultPropValue("LineWidth",isub,its));
		}
		_ts_linewidth_JComboBox.setEnabled(true);
	}

	// "RegressionLineEnabled"

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue("RegressionLineEnabled", isub, its, false);
		if ((prop_val == null) || prop_val.equalsIgnoreCase("true")) {
			_ts_regressionline_JCheckBox.setSelected(true);
		}
		else {
			_ts_regressionline_JCheckBox.setSelected(false);
		}
	}

	// "SymbolSize"

	prop_val = _tsproduct.getLayeredPropValue ( "SymbolSize", isub, its, false );
	if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ||
	    (graphType == TSGraphType.BAR) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		_ts_symbolsize_JComboBox.select("0");
		_ts_symbolsize_JComboBox.setEnabled(false);
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_ts_symbolsize_JComboBox, prop_val);
		}
		catch (Exception e) {
			_ts_symbolsize_JComboBox.select(_tsproduct.getDefaultPropValue("SymbolSize",isub,its));
		}
		_ts_symbolsize_JComboBox.setEnabled(true);
	}

	// "SymbolStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "SymbolStyle", isub, its, false );
	if ( (graphType == TSGraphType.AREA) || (graphType == TSGraphType.AREA_STACKED) ||
	    (graphType == TSGraphType.BAR) || (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) ) {
		_ts_symbolstyle_JComboBox.select("None");
		_ts_symbolstyle_JComboBox.setEnabled(false);
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_ts_symbolstyle_JComboBox, prop_val);
		}
		catch (Exception e) {
			_ts_symbolstyle_JComboBox.select(_tsproduct.getDefaultPropValue("SymbolStyle",isub,its));
		}
		_ts_symbolstyle_JComboBox.setEnabled(true);
	}

	// "XYScatterConfidenceLevel"

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue("XYScatterConfidenceInterval", isub, its, false);
		try {
			JGUIUtil.selectIgnoreCase(_ts_confidenceinterval_JComboBox,prop_val);
		}
		catch (Exception e) {
			_ts_confidenceinterval_JComboBox.select(_tsproduct.getDefaultPropValue("XYScatterConfidenceInterval",isub,its));
		}

		if (_ts_regressionline_JCheckBox.isSelected()) {
			_ts_confidenceinterval_JComboBox.setEnabled(true);
		}
		else {
			_ts_confidenceinterval_JComboBox.setEnabled(false);
		}
	}

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		// Display the regression analysis panel...
		_ts_blank_analysis_JPanel.setVisible ( false );
		_ts_xyscatter_analysis_JPanel.setVisible ( true );
		_ts_xyscatter_analysis_JPanel.repaint();
	}
	else {
	    // Display the blank analysis panel...
		_ts_xyscatter_analysis_JPanel.setVisible ( false );
		_ts_blank_analysis_JPanel.setVisible ( true );
		_ts_blank_analysis_JPanel.repaint();
	}
}

/**
 * Display the drawing area properties.
 */
private void displayDrawingAreaProperties () {
	// Display in window.
	PropList props = new PropList("ProductJDialog");
	props.set("Title=Drawing Area Properties");
	props.set("Search=true");
	props.set("TotalWidth=700");
	props.set("TotalHeight=500");
	// Loop through graphs
	String text = _tsview_gui.getViewGraphJFrame().getMainJComponent().toString();
	new ReportJFrame(StringUtil.breakStringList(text, "\n", 0), props);
}

/**
Update the product components with settings appropriate for the TSProduct.
This should be called at initialization.
*/
private void displayProductProperties () {
	String prop_val;
	String routine = getClass().getSimpleName() + ".displayProductProperties";

	// Make the Titles pane the visible since that is what most people care about.
	_product_JTabbedPane.setSelectedIndex(1);

	// "Enabled"

	prop_val = _tsproduct.getLayeredPropValue ( "Enabled", -1, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("true") ) {
		_product_enabled_JCheckBox.setSelected ( true );
	}
	else {
		_product_enabled_JCheckBox.setSelected ( false );
	}

	// "MainTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleString", -1, -1, false );
	_product_maintitle_JTextField.setText(prop_val);

	// "ProductID"
	prop_val = _tsproduct.getLayeredPropValue("ProductID", -1, -1, false);
	__product_id_JTextField.setText(prop_val);

	// "ProductName"
	prop_val = _tsproduct.getLayeredPropValue("ProductName", -1, -1, false);
	__product_name_JTextField.setText(prop_val);

	// "MainTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontName", -1, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_product_maintitle_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_product_maintitle_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("MainTitleFontName",-1,-1) );
	}

	// "MainTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", -1, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_product_maintitle_fontstyle_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontStyle \"" +
		prop_val + "\" is not recognized" );
		_product_maintitle_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("MainTitleFontStyle",-1,-1) );
	}

	// "MainTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", -1, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_product_maintitle_fontsize_JTextField.setText(prop_val);
	}
	else {
	    Message.printWarning ( 2, routine, "MainTitleFontSize \"" + prop_val + "\" is not recognized" );
		_product_maintitle_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("MainTitleFontSize",-1,-1) );
	}

	// "SubTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleString", -1, -1, false );
	_product_subtitle_JTextField.setText(prop_val);

	// "SubTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontName", -1, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_product_subtitle_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontName \"" + prop_val + "\" is not recognized" );
		_product_subtitle_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("SubTitleFontName",-1,-1) );
	}

	// "SubTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", -1, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_product_subtitle_fontstyle_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontStyle \"" + prop_val + "\" is not recognized" );
		_product_subtitle_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("SubTitleFontStyle",-1,-1) );
	}

	// "SubTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", -1, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_product_subtitle_fontsize_JTextField.setText(prop_val);
	}
	else {
	    Message.printWarning ( 2, routine, "SubTitleFontSize \"" + prop_val + "\" is not recognized" );
		_product_subtitle_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("SubTitleFontSize",-1,-1) );
	}

	// "LayoutType"

	prop_val = _tsproduct.getLayeredPropValue( "LayoutType", -1, -1, false);
	if (prop_val == null) {
		_layoutTypeJComboBox.select("Grid");
	}
	else {
		_layoutTypeJComboBox.select(prop_val);
	}

	// "LayoutNumberOfRows"

	prop_val = _tsproduct.getLayeredPropValue("LayoutNumberOfRows", -1, -1, false);
	if (prop_val == null) {
		_numberRowsJTextField.setText("" + (_tsproduct.getNumSubProducts() + 1));
	}
	else {
		int num = StringUtil.atoi(prop_val);
		if (num != _tsproduct.getNumSubProducts()) {
			num = _tsproduct.getNumSubProducts();
			_tsproduct.setPropValue("LayoutNumberOfRows", "" + num, -1, -1);
		}
		_numberRowsJTextField.setText("" + num);
	}

	// "LayoutNumberOfCols"

	prop_val = _tsproduct.getLayeredPropValue("LayoutNumberOfCols", -1, -1, false);
	if (prop_val == null) {
		// do nothing for now
	}
	else {
		// ignore for now.
	}

	// Developer properties

	// "ShowDrawingAreaOutline"

	prop_val = _tsproduct.getLayeredPropValue( "ShowDrawingAreaOutline", -1, -1, false);
	if (prop_val == null) {
		_developerShowDrawingAreaOutlineJComboBox.select("False");
	}
	else {
		_developerShowDrawingAreaOutlineJComboBox.select(prop_val);
	}
}

/**
 * Display the product properties as text in a separate window.
 * This is used by developers to review properties.
 */
private void displayProductPropertiesText () {
	// First get the product properties as text.
	// Output uses native newline so replace \r\n by \n before splitting lines.
	boolean outputAll = true;
	boolean outputHowSet = true;
	boolean sort = true;
	String text = _tsproduct.toString(outputAll,outputHowSet,TSProductFormatType.PROPERTIES,sort).replace("\r\n", "\n");
	// Display in window.
	PropList props = new PropList("ProductJDialog");
	props.set("Title=Time Series Product Properties");
	props.set("Search=true");
	props.set("TotalWidth=700");
	props.set("TotalHeight=500");
	new ReportJFrame(StringUtil.breakStringList(text, "\n", 0), props);
}

/**
Update the subproduct components with settings appropriate for the TSProduct.
This should be called when a graph is selected.
*/
private void displaySubproductProperties ( int isub ) {
	String routine = getClass().getSimpleName() + ".displaySubproductProperties";
	String prop_val;
	TSGraphType graphType;

	// "GraphType" - do this first and then alphabetize other properties.
	// Some properties depend on the graph type.

	prop_val = _tsproduct.getLayeredPropValue("LayoutYPercent", isub, -1, false);
	if (prop_val != null) {
		_yPercentJTextField.setText(prop_val);
	}
	else {
		_yPercentJTextField.setText("");
	}

	if (__limitGraphTypes) {
		limitGraphTypes(isub);
	}

	// Left y-axis graph type and related properties.

	try {
	    JGUIUtil.selectIgnoreCase(_graph_lefty_graphtype_JComboBox,_tsproduct.getLayeredPropValue ("GraphType", isub, -1, false ));
	}
	catch ( Exception e ) {
		_graph_lefty_graphtype_JComboBox.select( "" + TSGraphType.LINE );
	}
	graphType = TSGraphType.valueOfIgnoreCase ( _graph_lefty_graphtype_JComboBox.getSelected() );

	// AnnotationProvider [Annotations]

	prop_val = _tsproduct.getLayeredPropValue("AnnotationProvider",	isub, -1, false);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("AnnotationProvider",	isub, -1);
	}

	if ( __graphAnnotationProvider != null ) {
	    __graphAnnotationProvider.select(prop_val);
	}

	// "BarOverlap" [GraphType] - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS.

    prop_val = _tsproduct.getLayeredPropValue ( "BarOverlap", isub, -1, false );
    if ( graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        try {
            JGUIUtil.selectIgnoreCase(_graph_barOverlap_JComboBox, prop_val);
        }
        catch (Exception e) {
            Message.printWarning (2, routine,"BarOverlap \"" + prop_val + "\" is not recognized");
            _graph_barOverlap_JComboBox.select(_tsproduct.getDefaultPropValue("BarOverlap", isub,-1));
        }
        _graph_barOverlap_JLabel.setVisible(true);
        _graph_barOverlap_JComboBox.setVisible(true);
    }
    else {
        _graph_barOverlap_JLabel.setVisible(false);
        _graph_barOverlap_JComboBox.setVisible(false);
    }

	// "BarPosition" [GraphType] - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS.

	prop_val = _tsproduct.getLayeredPropValue ( "BarPosition", isub, -1, false );
	if ( graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		try {
			JGUIUtil.selectIgnoreCase(_graph_barposition_JComboBox, prop_val);
		}
		catch (Exception e) {
			Message.printWarning (2, routine,"BarPosition \"" + prop_val + "\" is not recognized");
			_graph_barposition_JComboBox.select(_tsproduct.getDefaultPropValue("BarPosition", isub,-1));
		}
		_graph_barposition_JLabel.setVisible(true);
		_graph_barposition_JComboBox.setVisible(true);
	}
	else {
		_graph_barposition_JLabel.setVisible(false);
		_graph_barposition_JComboBox.setVisible(false);
	}

	// "BottomXAxisMajorGridColor" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", isub, -1, false );
	_graph_bottomx_majorgrid_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_bottomx_majorgrid_color_JComboBox.select("None");
		_graph_bottomx_majorgrid_color_JTextField.setBackground ( Color.white );
	}
	else {
    	try {
    	    JGUIUtil.selectIgnoreCase(_graph_bottomx_majorgrid_color_JComboBox,prop_val);
    		try {
    		    _graph_bottomx_majorgrid_color_JTextField.setBackground(
    			(Color)GRColor.parseColor(_graph_bottomx_majorgrid_color_JTextField.getText()) );
    		}
    		catch ( Exception e2 ) {
    			_graph_bottomx_majorgrid_color_JComboBox.select("None");
    			_graph_bottomx_majorgrid_color_JTextField.setBackground(Color.white );
    		}
    	}
    	catch ( Exception e ) {
    		_graph_bottomx_majorgrid_color_JComboBox.select("None");
    		_graph_bottomx_majorgrid_color_JTextField.setBackground (Color.white );
    	}
	}

	// "BottomXAxisTitleString" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ("BottomXAxisTitleString", isub, -1, false );
	_graph_bottomx_title_JTextField.setText(prop_val);

	// "BottomXAxisTitleFontName" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_bottomx_title_fontname_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_bottomx_title_fontname_JComboBox.select(	_tsproduct.getDefaultPropValue("BottomXYAxisTitleFontName", isub,-1) );
	}

	// "BottomXAxisTitleFontStyle" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_bottomx_title_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisTitleFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleFontStyle",
		isub,-1) );
	}

	// "BottomXAxisTitleFontSize" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue("BottomXAxisTitleFontSize",
		isub, -1, false);
	if (StringUtil.isDouble(prop_val)) {
		_graph_bottomx_title_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning(2,routine,"BottomXAxisTitleFontSize \"" + prop_val + "\" is not recognized");
		_graph_bottomx_title_fontsize_JTextField.setText(
			_tsproduct.getDefaultPropValue(
			"BottomXAxisTitleFontSize", isub,-1));
	}

	// "BottomXAxisLabelFontName" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_bottomx_label_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisLabelFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_bottomx_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXYAxisLabelFontName",
		isub,-1) );
	}

	// "BottomXAxisLabelFontStyle" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_bottomx_label_fontstyle_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisLabelFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontStyle",
		isub,-1) );
	}

	// "BottomXAxisLabelFontSize" [X Axis (Bottom)]

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_bottomx_label_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine,"BottomXAxisLabelFontSize \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontSize",
		isub,-1) );
	}

	// "DataLabelFormat" [Data Point Label]

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFormat", isub, -1, false );
	_graph_datalabelformat_JTextField.setText(prop_val);

	// "DataLabelFontName" [Data Point Label]

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_datalabelfontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"DataLabelFontName \"" + prop_val + "\" is not recognized" );
		_graph_datalabelfontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("DataLabelFontName",
		isub,-1) );
	}

	// "DataLabelFontStyle" [Data Point Label]

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_datalabelfontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"DataLabelFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_datalabelfontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("DataLabelFontStyle",
		isub,-1) );
	}

	// "DataLabelFontSize" [Data Point Label]

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_datalabelfontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine,"DataLabelFontSize \""+
		prop_val + "\" is not recognized" );
		_graph_datalabelfontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("DataLabelFontSize",
		isub,-1) );
	}

	// "DataLabelPosition" [Data Point Label]

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelPosition", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_datalabelposition_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"DataLabelPosition \"" +
		prop_val + "\" is not recognized" );
		_graph_datalabelposition_JComboBox.select(
		_tsproduct.getDefaultPropValue("DataLabelPosition",
		isub,-1) );
	}

	// "Enabled" [General]

	prop_val = _tsproduct.getLayeredPropValue ( "Enabled", isub, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("true") ) {
		_graph_enabled_JCheckBox.setSelected ( true );
	}
	else {
		_graph_enabled_JCheckBox.setSelected ( false );
	}

	// "LeftYAxisMajorGridColor" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor", isub, -1, false );
	_graph_lefty_majorgrid_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_lefty_majorgrid_color_JComboBox.select("None");
		_graph_lefty_majorgrid_color_JTextField.setBackground (	Color.white );
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_graph_lefty_majorgrid_color_JComboBox,prop_val);
			try {
				_graph_lefty_majorgrid_color_JTextField.setBackground (	(Color)GRColor.parseColor(
				_graph_lefty_majorgrid_color_JTextField.getText()) );
			}
			catch ( Exception e2 ) {
				_graph_lefty_majorgrid_color_JComboBox.select("None");
				_graph_lefty_majorgrid_color_JTextField.setBackground (	Color.white );
			}
		}
		catch ( Exception e ) {
			_graph_lefty_majorgrid_color_JComboBox.select("None");
			_graph_lefty_majorgrid_color_JTextField.setBackground ( Color.white );
		}
	}

	// "LeftYAxisMajorTickColor" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorTickColor", isub, -1, false );
	_graph_lefty_majortick_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_lefty_majortick_color_JComboBox.select("None");
		_graph_lefty_majortick_color_JTextField.setBackground (	Color.white );
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_graph_lefty_majortick_color_JComboBox,prop_val);
			try {
				_graph_lefty_majortick_color_JTextField.setBackground (	(Color)GRColor.parseColor(
				_graph_lefty_majortick_color_JTextField.getText()) );
			}
			catch ( Exception e2 ) {
				_graph_lefty_majortick_color_JComboBox.select("None");
				_graph_lefty_majortick_color_JTextField.setBackground (	Color.white );
			}
		}
		catch ( Exception e ) {
			_graph_lefty_majortick_color_JComboBox.select("None");
			_graph_lefty_majortick_color_JTextField.setBackground ( Color.white );
		}
	}

	// "LeftYAxisTitlePosition" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitlePosition", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_title_position_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		_graph_lefty_title_position_JComboBox.select("AboveAxis");
	}

	// "LeftYAxisTitleRotation" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", isub, -1, false );
	_graph_lefty_title_rotation_JTextField.setText(prop_val);

	// "LeftYAxisTitleString" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", isub, -1, false );
	_graph_lefty_title_JTextField.setText(prop_val);

	// "LeftYAxisTitleFontName" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_title_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_lefty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName",isub,-1) );
	}

	// "LeftYAxisTitleFontStyle" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_title_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontStyle \""+prop_val + "\" is not recognized" );
		_graph_lefty_title_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontStyle",isub,-1) );
	}

	// "LeftYAxisTitleFontSize" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_lefty_title_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontSize \"" + prop_val + "\" is not recognized" );
		_graph_lefty_title_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontSize",	isub,-1) );
	}

	// "LeftYAxisLabelFontName" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue (	"LeftYAxisLabelFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_label_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisLabelFontName \"" + prop_val + "\" is not recognized" );
		_graph_lefty_label_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontName",isub,-1) );
	}

	// "LeftYAxisLabelFontStyle" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_label_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisLabelFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_lefty_label_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontStyle",isub,-1) );
	}

	// "LeftYAxisLabelFontSize" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_lefty_label_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "LeftYAxisLabelFontSize \"" + prop_val + "\" is not recognized" );
		_graph_lefty_label_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("LeftYAxisLabelFontSize",isub,-1) );
	}

	// "LeftYAxisDirection" [Y Axis (Left)]

    prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisDirection", isub, -1, false );
    _graph_lefty_direction_JComboBox.select(prop_val);

	// "LeftYAxisTitleFontName" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue (	"LeftYAxisTitleFontName", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_lefty_title_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_lefty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName",isub,-1) );
	}

	// "LeftYAxisIgnoreUnits" [Y Axis (Left)]

	// TODO SAM 2016-10-17 Shouldn't the following default to false if null?
	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisIgnoreUnits", isub, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("true") ) {
		_graph_lefty_ignoreunits_JCheckBox.setSelected ( true );
	}
	else {	_graph_lefty_ignoreunits_JCheckBox.setSelected ( false );
	}

	// "LeftYAxisLabelPrecision" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelPrecision", isub, -1, false );
	_graph_lefty_precision_JTextField.setText(prop_val);

	// "LeftYAxisLegendPosition" [Legend]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLegendPosition", isub, -1, false );
	if ( prop_val == null ) {
		// Get legacy value.
		prop_val = _tsproduct.getLayeredPropValue ( "LegendPosition", isub, -1, false );
	}
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_legendposition_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisLegendPosition \"" + prop_val + "\" is not recognized" );
		_graph_lefty_legendposition_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisLegendPosition",isub,-1) );
	}

	// "LeftYAxisMax" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMax", isub, -1, false );
	_graph_lefty_max_JComboBox.select(prop_val);

	// "LeftYAxisMin" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMin", isub, -1, false );
	_graph_lefty_min_JComboBox.select(prop_val);

	// "LeftYAxisOriginalGraphType"

	prop_val = _tsproduct.getLayeredPropValue( "LeftYAxisOriginalGraphType", isub, -1, false);
	if (prop_val == null) {
		_graphLeftYAxisOriginalGraphType_JTextField.setText("");
	}
	else {
		_graphLeftYAxisOriginalGraphType_JTextField.setText(prop_val);
	}

	// "LeftYAxisType" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisType", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_lefty_type_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisType \"" + prop_val + "\" is not recognized" );
		_graph_lefty_type_JComboBox.select(_tsproduct.getDefaultPropValue("LeftYAxisType",isub,-1) );
	}

	// "LeftYAxisUnits" [Y Axis (Left)]

	prop_val = _tsproduct.getLayeredPropValue (	"LeftYAxisUnits", isub, -1, false );
	_graph_lefty_units_JTextField.setText(prop_val);

	// "LegendEnabled" [Legend]
	// TODO SAM 2016-10-23 is this legacy or future?

	// "LegendFontName" [Legend]

	prop_val = _tsproduct.getLayeredPropValue (	"LegendFontName", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_legendfontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LegendFontName \"" + prop_val + "\" is not recognized" );
		_graph_legendfontname_JComboBox.select(	_tsproduct.getDefaultPropValue("LegendFontName",isub,-1) );
	}

	// "LegendFontStyle" [Legend]

	prop_val = _tsproduct.getLayeredPropValue (	"LegendFontStyle", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_legendfontstyle_JComboBox,	prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LegendFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_legendfontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontStyle",isub,-1) );
	}

	// "LegendFontSize" [Legend]

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_legend_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "LegendFontSize \"" + prop_val + "\" is not recognized" );
		_graph_legend_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("LegendFontSize",isub,-1) );
	}

	// "LegendFormat" [Legend]

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFormat", isub, -1, false );
	_graph_legendformat_JTextField.setText(prop_val);

	// "LegendPosition" [Legend]  (legacy property replaced by "LeftYAxisLegendPosition"

	// "MainTitleString" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleString", isub, -1, false );
	_graph_maintitle_JTextField.setText(prop_val);

	// "MainTitleFontName" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_maintitle_fontname_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_maintitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontName",isub,-1) );
	}

	// "MainTitleFontStyle" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_maintitle_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_maintitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontStyle",isub,-1) );
	}

	// "MainTitleFontSize" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_maintitle_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "MainTitleFontSize \"" + prop_val + "\" is not recognized" );
		_graph_maintitle_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("MainTitleFontSize",isub,-1) );
	}

	// "RightYAxisGraphType" [Y Axis (Right)]

	try {
	    JGUIUtil.selectIgnoreCase(_graph_righty_graphtype_JComboBox,_tsproduct.getLayeredPropValue ("RightYAxisGraphType", isub, -1, false ));
	}
	catch ( Exception e ) {
		_graph_righty_graphtype_JComboBox.select( "" + TSGraphType.NONE );
	}
	TSGraphType rightYAxisGraphType = TSGraphType.valueOfIgnoreCase ( _graph_righty_graphtype_JComboBox.getSelected() );

	// "RightYAxisBarOverlap" [Y Axis (Right)] - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS.

    prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisBarOverlap", isub, -1, false );
    if ( rightYAxisGraphType == TSGraphType.BAR || rightYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        try {
            JGUIUtil.selectIgnoreCase(_graph_righty_barOverlap_JComboBox, prop_val);
        }
        catch (Exception e) {
            Message.printWarning (2, routine,"RightYAxisBarOverlap \"" + prop_val + "\" is not recognized");
            _graph_righty_barOverlap_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisBarOverlap", isub,-1));
        }
        _graph_righty_barOverlap_JLabel.setVisible(true);
        _graph_righty_barOverlap_JComboBox.setVisible(true);
    }
    else {
        _graph_righty_barOverlap_JLabel.setVisible(false);
        _graph_righty_barOverlap_JComboBox.setVisible(false);
    }

	// "RightYAxisBarPosition" [Y Axis (Right)] - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS.

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisBarPosition", isub, -1, false );
	if ( rightYAxisGraphType == TSGraphType.BAR || rightYAxisGraphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		try {
			JGUIUtil.selectIgnoreCase(_graph_righty_barposition_JComboBox, prop_val);
		}
		catch (Exception e) {
			Message.printWarning (2, routine,"RightYAxisBarPosition \"" + prop_val + "\" is not recognized");
			_graph_righty_barposition_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisBarPosition", isub,-1));
		}
		_graph_righty_barposition_JLabel.setVisible(true);
		_graph_righty_barposition_JComboBox.setVisible(true);
	}
	else {
		_graph_righty_barposition_JLabel.setVisible(false);
		_graph_righty_barposition_JComboBox.setVisible(false);
	}

	// "RightYAxisDirection" [Y Axis (Right)]

    prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisDirection", isub, -1, false );
    _graph_righty_direction_JComboBox.select(prop_val);

	// "RightYAxisLegendPosition" [Legend]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLegendPosition", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_legendposition_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisLegendPosition \"" + prop_val + "\" is not recognized" );
		_graph_righty_legendposition_JComboBox.select( _tsproduct.getDefaultPropValue("RightYAxisLegendPosition",isub,-1) );
	}

	// "RightYAxisMajorGridColor" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMajorGridColor", isub, -1, false );
	_graph_righty_majorgrid_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_righty_majorgrid_color_JComboBox.select("None");
		_graph_righty_majorgrid_color_JTextField.setBackground (	Color.white );
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_graph_righty_majorgrid_color_JComboBox,prop_val);
			try {
				_graph_righty_majorgrid_color_JTextField.setBackground (	(Color)GRColor.parseColor(
				_graph_righty_majorgrid_color_JTextField.getText()) );
			}
			catch ( Exception e2 ) {
				_graph_righty_majorgrid_color_JComboBox.select("None");
				_graph_righty_majorgrid_color_JTextField.setBackground (	Color.white );
			}
		}
		catch ( Exception e ) {
			_graph_righty_majorgrid_color_JComboBox.select("None");
			_graph_righty_majorgrid_color_JTextField.setBackground ( Color.white );
		}
	}

	// "RightYAxisMajorTickColor" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMajorTickColor", isub, -1, false );
	_graph_righty_majortick_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_righty_majortick_color_JComboBox.select("None");
		_graph_righty_majortick_color_JTextField.setBackground ( Color.white );
	}
	else {
		try {
			JGUIUtil.selectIgnoreCase(_graph_righty_majortick_color_JComboBox,prop_val);
			try {
				_graph_righty_majortick_color_JTextField.setBackground ( (Color)GRColor.parseColor(
				_graph_righty_majortick_color_JTextField.getText()) );
			}
			catch ( Exception e2 ) {
				_graph_righty_majortick_color_JComboBox.select("None");
				_graph_righty_majortick_color_JTextField.setBackground ( Color.white );
			}
		}
		catch ( Exception e ) {
			_graph_righty_majortick_color_JComboBox.select("None");
			_graph_righty_majortick_color_JTextField.setBackground ( Color.white );
		}
	}

	// "RightYAxisOriginalGraphType"

	prop_val = _tsproduct.getLayeredPropValue( "RightYAxisOriginalGraphType", isub, -1, false);
	if (prop_val == null) {
		_graphRightYAxisOriginalGraphType_JTextField.setText("");
	}
	else {
		_graphRightYAxisOriginalGraphType_JTextField.setText(prop_val);
	}

	// "RightYAxisTitlePosition" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_title_position_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		_graph_righty_title_position_JComboBox.select("None"); // Do not show the second y axis
	}

	// "RightYAxisTitleRotation" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", isub, -1, false );
	_graph_righty_title_rotation_JTextField.setText(prop_val);

	// "RightYAxisTitleString" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleString", isub, -1, false );
	_graph_righty_title_JTextField.setText(prop_val);

	// "RightYAxisTitleFontName" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_title_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_righty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisTitleFontName",isub,-1) );
	}

	// "RightYAxisTitleFontStyle" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_title_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisTitleFontStyle \""+prop_val + "\" is not recognized" );
		_graph_righty_title_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisTitleFontStyle",isub,-1) );
	}

	// "RightYAxisTitleFontSize" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_righty_title_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "RightYAxisTitleFontSize \"" + prop_val + "\" is not recognized" );
		_graph_righty_title_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("RightYAxisTitleFontSize",	isub,-1) );
	}

	// "RightYAxisLabelFontName" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue (	"RightYAxisLabelFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_label_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisLabelFontName \"" + prop_val + "\" is not recognized" );
		_graph_righty_label_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisLabelFontName",isub,-1) );
	}

	// "RightYAxisLabelFontStyle" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_label_fontstyle_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisLabelFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_righty_label_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisLabelFontStyle",isub,-1) );
	}

	// "RightYAxisLabelFontSize" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_righty_label_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "RightYAxisLabelFontSize \"" + prop_val + "\" is not recognized" );
		_graph_righty_label_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("RightYAxisLabelFontSize",isub,-1) );
	}

	// "RightYAxisTitleFontName" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue (	"RightYAxisTitleFontName", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_righty_title_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_righty_title_fontname_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisTitleFontName",isub,-1) );
	}

	// "RightYAxisIgnoreUnits" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisIgnoreUnits", isub, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("false") ) {
		_graph_righty_ignoreunits_JCheckBox.setSelected ( false ); // Default
	}
	else {
		_graph_righty_ignoreunits_JCheckBox.setSelected ( true );
	}
	// TODO SAM 2016-10-17 Figure out why property is defaulted to true
	_graph_righty_ignoreunits_JCheckBox.setSelected ( false );

	// "RightYAxisLabelPrecision" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelPrecision", isub, -1, false );
	_graph_righty_precision_JTextField.setText(prop_val);

	// "RightYAxisMax" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMax", isub, -1, false );
	_graph_righty_max_JComboBox.select(prop_val);

	// "RightYAxisMin" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMin", isub, -1, false );
	_graph_righty_min_JComboBox.select(prop_val);

	// "RightYAxisType" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisType", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_righty_type_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "RightYAxisType \"" + prop_val + "\" is not recognized" );
		_graph_righty_type_JComboBox.select(_tsproduct.getDefaultPropValue("RightYAxisType",isub,-1) );
	}

	// "RightYAxisUnits" [Y Axis (Right)]

	prop_val = _tsproduct.getLayeredPropValue (	"RightYAxisUnits", isub, -1, false );
	_graph_righty_units_JTextField.setText(prop_val);

	// "SelectedTimeSeriesLineWidth" [General]

	prop_val = _tsproduct.getLayeredPropValue (	"SelectedTimeSeriesLineWidth", isub, -1, false );
	_graphSelectedTimeSeriesLineWidth_JTextField.setText(prop_val);

	// "SubTitleString" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleString", isub, -1, false );
	_graph_subtitle_JTextField.setText(prop_val);

	// "SubTitleFontName" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontName", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_subtitle_fontname_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_subtitle_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("SubTitleFontName",isub,-1) );
	}

	// "SubTitleFontStyle" [Titles]

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", isub, -1, false );
	try {
		JGUIUtil.selectIgnoreCase(_graph_subtitle_fontstyle_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_subtitle_fontstyle_JComboBox.select(_tsproduct.getDefaultPropValue("SubTitleFontStyle",isub,-1) );
	}

	// "SubTitleFontSize" [Titles]

	prop_val = _tsproduct.getLayeredPropValue (	"SubTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_subtitle_fontsize_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "SubTitleFontSize \"" + prop_val + "\" is not recognized" );
		_graph_subtitle_fontsize_JTextField.setText(_tsproduct.getDefaultPropValue("SubTitleFontSize",isub,-1) );
	}

	// "ZoomEnabled" [Zoom]

	prop_val = _tsproduct.getLayeredPropValue ( "ZoomEnabled", isub, -1, false );
	if ( prop_val.equalsIgnoreCase("true") ) {
		_graph_zoomenabled_JCheckBox.setSelected(true);
	}
	else {
		_graph_zoomenabled_JCheckBox.setSelected(false);
	}

	// "ZoomGroup" [Zoom]

	prop_val = _tsproduct.getLayeredPropValue ( "ZoomGroup", isub, -1, false );
	if ( StringUtil.isInteger(prop_val) ) {
		_graph_zoomgroup_JTextField.setText(prop_val);
	}
	else {
		Message.printWarning ( 2, routine, "ZoomGroup \"" +	prop_val + "\" is not recognized" );
		_graph_zoomgroup_JTextField.setText( _tsproduct.getDefaultPropValue("ZoomGroup",isub,-1) );
	}

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {

		// "XYScatterMethod" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterMethod", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_method_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "XYScatterMethod \"" + prop_val + "\" is not recognized" );
			_xyscatter_analysis_method_JComboBox.select(_tsproduct.getDefaultPropValue("XYScatterMethod",isub,-1) );
		}

		// "XYScatterTransformation" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_transform_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine,"XYScatterTransformation \"" + prop_val + "\" is not recognized" );
			_xyscatter_analysis_transform_JComboBox.select( _tsproduct.getDefaultPropValue("XYScatterTransformation",isub,-1) );
		}

		// "XYScatterNumberOfEquations" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_neqn_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "XYScatterNumberOfEquations \"" + prop_val + "\" is not recognized" );
			_xyscatter_analysis_neqn_JComboBox.select( _tsproduct.getDefaultPropValue("XYScatterNumberOfEquations",isub,-1) );
		}

		// "XYScatterMonth" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterMonth", isub, -1, false );
		_xyscatter_analysis_month_JTextField.setText ( prop_val );

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterAnalyzeForFilling", isub, -1, false );
		if ( (prop_val == null) || prop_val.equalsIgnoreCase ( "false" ) ) {
			// Default...
			_xyscatter_analysis_fill_JCheckBox.setEnabled ( false );
		}
		else {
			_xyscatter_analysis_fill_JCheckBox.setEnabled ( true );
		}

		// "XYScatterIntercept" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterIntercept", isub, -1, false );
		_xyscatter_analysis_intercept_JTextField.setText ( prop_val );

		// "XYScatterFillPeriodStart" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterFillPeriodStart", isub, -1, false );
		_xyscatter_analysis_fill_period_start_JTextField.setText ( prop_val );

		// "XYScatterFillPeriodEnd" [Analysis]

		prop_val = _tsproduct.getLayeredPropValue (	"XYScatterFillPeriodEnd", isub, -1, false );
		_xyscatter_analysis_fill_period_end_JTextField.setText(prop_val);

		// Display the regression analysis panel...
		_blank_analysis_JPanel.setVisible ( false );
		_xyscatter_analysis_JPanel.setVisible ( true );
		_xyscatter_analysis_JPanel.repaint();
	}
	else {
		// Display the blank analysis panel...
		_xyscatter_analysis_JPanel.setVisible ( false );
		_blank_analysis_JPanel.setVisible ( true );
		_blank_analysis_JPanel.repaint();
	}
}

////////////////////////////////////////////////////////////
// DragAndDropListener methods.
/**
Updates the TSProduct to save any changes immediately prior to the drag starting,
so that the dragged data is up-to-date.
From RTiDragAndDrop interface.
@return true
*/
public boolean dragAboutToStart() {
	updateTSProduct();
	return true;
}

/**
Called when a drag is started, does nothing.
*/
public void dragStarted() {
}

/**
Responds to a successful drag by updating the time series combo box for the current graph.
Called when a drag is completed successfully.
Moves the dragged time series to the graph it was dragged to.
@param action ignored.
*/
public void dragSuccessful(int action) {
	int nts = _tsproduct.getNumData ( _selected_subproduct );
	__ts_JComboBox.removeAllItems();
	String prop_val;
	String sequence_number;
	for ( int its = 0; its < nts; its++ ) {
		prop_val = _tsproduct.getLayeredPropValue( "TSAlias", _selected_subproduct, its, false );
		if ( (prop_val == null) || prop_val.trim().equals("") ) {
			prop_val = _tsproduct.getLayeredPropValue( "TSID", _selected_subproduct, its, false );
		}
		sequence_number = _tsproduct.getLayeredPropValue( "SequenceNumber", _selected_subproduct, its, false );
		if ((sequence_number == null) || sequence_number.equals("-1") ){
			sequence_number = "";
		}
		else {
			sequence_number = " [" + sequence_number + "] ";
		}
		if ( prop_val == null ) {
			prop_val = "No data";
		}
		else {
			// Limit the TSID to 60 characters.
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60) + "..." + sequence_number;
			}
		}
		__ts_JComboBox.add ( "" + (its + 1) + " - " + prop_val + sequence_number );
	}

	if (nts > 0) {
		// Display the first time series.
		_selected_data = 0;
		displayDataProperties ( _selected_subproduct, _selected_data );
	}
	else {
		_selected_data = -1;
		clearDataProperties();
	}
	checkGUIState();
}

/**
Called when a drag was not completed successfully, does nothing.
*/
public void dragUnsuccessful(int action) {
	checkGUIState();
}

/**
Called if the mouse cursor is over an area where dropping is allowed, does nothing.
*/
public void dropAllowed() {
}

/**
Called if the mouse leaves an area when dropping is allowed, does nothing.
*/
public void dropExited() {
}

/**
Called if the mouse cursor is over an area where dropping is not allowed, does nothing.
*/
public void dropNotAllowed() {
}

/**
Called when data are dropped successfully, does nothing.
*/
public void dropSuccessful() {
}

/**
Called when data were not dropped successfully, does nothing.
*/
public void dropUnsuccessful() {
}

/**
Enables and disables components on the GUI based on the currently-selected graph type.
@param isub the subproduct currently selected.
@param its the time series currently selected.
*/
void enableComponentsBasedOnGraphType(int isub, int its, boolean setValue) {
	if (!isVisible()) {
		// If the GUI is not visible (i.e., in setup), this method will cause major problems if called as is.
		return;
	}

	String tsYAxis = _ts_yaxis_JComboBox.getSelected();
	if ( tsYAxis == null ) {
		tsYAxis = "Left";
		_ts_yaxis_JComboBox.select(tsYAxis);
	}

	// Default graph type is from the subproduct for the y-axis associated with the time series.
	TSGraphType graphType = null;
	if ( tsYAxis.equalsIgnoreCase("Left") ) {
		// Graph type is from left y-axis.
		graphType = TSGraphType.valueOfIgnoreCase ( _graph_lefty_graphtype_JComboBox.getSelected() );
	}
	else {
		// Graph type is from right y-axis.
		graphType = TSGraphType.valueOfIgnoreCase ( _graph_righty_graphtype_JComboBox.getSelected() );
	}

	// "FlaggedDataSymbolStyle"

    if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        if (setValue) {
            _ts_flaggedDataSymbolStyle_JComboBox.select("");
        }
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(false);
    }
    else if (graphType == TSGraphType.POINT) {
        if (setValue) {
            _ts_symbolstyle_JComboBox.select("");
        }
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(true);
    }
    else {
        if (setValue) {
            _ts_symbolstyle_JComboBox.select("");
        }
        _ts_flaggedDataSymbolStyle_JComboBox.setEnabled(true);
    }

	// "LineStyle"

	if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if (setValue) {
			_ts_linestyle_JComboBox.select("None");
		}
		_ts_linestyle_JComboBox.setEnabled(false);
	}
	else if (graphType == TSGraphType.POINT) {
		if (setValue) {
			_ts_linestyle_JComboBox.select("None");
		}
		_ts_linestyle_JComboBox.setEnabled(false);
	}
	else {
		if (setValue) {
			_ts_linestyle_JComboBox.select("Solid");
		}
		_ts_linestyle_JComboBox.setEnabled(true);
	}

	// "LineWidth"

	if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if (setValue) {
			_ts_linewidth_JComboBox.select("0");
		}
		_ts_linewidth_JComboBox.setEditable(false);
		_ts_linewidth_JComboBox.setEnabled ( false );
	}
	else if (graphType == TSGraphType.POINT) {
		if (setValue) {
			_ts_linewidth_JComboBox.select("0");
		}
		_ts_linewidth_JComboBox.setEditable(false);
		_ts_linewidth_JComboBox.setEnabled(false);
	}
	else {
		if (setValue) {
			_ts_linewidth_JComboBox.select("1");
		}
		_ts_linewidth_JComboBox.setEditable(true);
		_ts_linewidth_JComboBox.setEnabled ( true );
	}

	// "SymbolSize"

	if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if (setValue) {
			_ts_symbolsize_JComboBox.select("0");
		}
		_ts_symbolsize_JComboBox.setEnabled(false);
	}
	else if (graphType == TSGraphType.POINT) {
		if (setValue) {
			_ts_symbolsize_JComboBox.select("5");
		}
		_ts_symbolsize_JComboBox.setEnabled(true);
	}
	else {
		if (setValue) {
			_ts_symbolsize_JComboBox.select("0");
		}
		_ts_symbolsize_JComboBox.setEnabled(true);
	}

	// "SymbolStyle"

	if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if (setValue) {
			_ts_symbolstyle_JComboBox.select("None");
		}
		_ts_symbolstyle_JComboBox.setEnabled(false);
	}
	else if (graphType == TSGraphType.POINT) {
		if (setValue) {
			_ts_symbolstyle_JComboBox.select("Circle-Hollow");
		}
		_ts_symbolstyle_JComboBox.setEnabled(true);
	}
	else {
		if (setValue) {
			_ts_symbolstyle_JComboBox.select("None");
		}
		_ts_symbolstyle_JComboBox.setEnabled(true);
	}

	// "XYScatterConfidenceLevel"

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		if ( _ts_regressionline_JCheckBox.isSelected() ) {
			_ts_confidenceinterval_JComboBox.setEnabled(true);
		}
		else {
		    _ts_confidenceinterval_JComboBox.setEnabled(false);
		}
	}

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		// Display the regression analysis panel.
		_ts_blank_analysis_JPanel.setVisible ( false );
		_ts_xyscatter_analysis_JPanel.setVisible ( true );
		_ts_xyscatter_analysis_JPanel.repaint();
	}
	else {
	    // Display the blank analysis panel.
		_ts_xyscatter_analysis_JPanel.setVisible ( false );
		_ts_blank_analysis_JPanel.setVisible ( true );
		_ts_blank_analysis_JPanel.repaint();
	}
}

/**
Returns a list of the values in the graph selection combo box.
@return a list of the values in the graph selection combo box.
*/
protected List<String> getGraphList() {
	List<String> graphList = new ArrayList<>();
	for (int i = 0; i < _graph_JComboBox.getItemCount(); i++) {
		graphList.add(""+_graph_JComboBox.getItemAt(i));
	}
	return graphList;
}

/**
For properties that have combo boxes, returns the default selection for the property.
Currently only supports LeftYAxisDirection, LeftYAxisMax, LeftYAxisMin,
RightYAxisDirection, RightYAxisMax, and RightYAxisMin.
@param property the property to return the default combo box selection for.
@return the default combo box selection for the given property.  Returns null if the property is unrecognized.
*/
private String getPropertyChoiceDefault(String property) {
	if (property.equalsIgnoreCase("LeftYAxisDirection") || property.equalsIgnoreCase("RightYAxisDirection")) {
		return "" + GRAxisDirectionType.NORMAL;
	}
	else if (property.equalsIgnoreCase("LeftYAxisMax") || property.equalsIgnoreCase("RightYAxisMax")) {
		return "Auto";
	}
	else if (property.equalsIgnoreCase("LeftYAxisMin") || property.equalsIgnoreCase("RightYAxisMin")) {
		return "Auto";
	}
	else if (property.equalsIgnoreCase("LeftYAxisTitlePosition") ) {
		return "AboveAxis";
	}
	else if (property.equalsIgnoreCase("RightYAxisTitlePosition") ) {
		return "None";
	}

	return null;
}

/**
Returns a list of choices to fill a combo box with for properties that have combo box selection interfaces.
Currently only supports LeftYAxisDirection, LeftYAxisMax, LeftYAxisMin,
RighYAxisDirection, RightYAxisMax, RightYAxisMin.
@param property the property for which to return a Vector of combo box choices.
@return a list of combo box choices, or null if the property is unrecognized.
*/
private List<String> getPropertyChoices(String property) {
    if (property.equalsIgnoreCase("LeftYAxisDirection") || property.equalsIgnoreCase("RightYAxisDirection")) {
        List<String> v = new ArrayList<>();
        v.add("" + GRAxisDirectionType.NORMAL);
        v.add("" + GRAxisDirectionType.REVERSE);
        return v;
    }
    else if (property.equalsIgnoreCase("LeftYAxisMax") || property.equalsIgnoreCase("RightYAxisMax")) {
		List<String> v = new ArrayList<>();
		v.add("Auto");
		//	Reserved for future use.
		//		v.add("AutoCrop");
		return v;
	}
	else if (property.equalsIgnoreCase("LeftYAxisMin") || property.equalsIgnoreCase("RightYAxisMin")) {
		List<String> v = new ArrayList<>();
		v.add("Auto");
		//	Reserved for future use.
		//		v.add("AutoCrop");
		return v;
	}
	else if (property.equalsIgnoreCase("LeftYAxisTitlePosition") ) {
        List<String> v = new ArrayList<>();
        v.add("AboveAxis");
        v.add("LeftOfAxis");
        v.add("None");
        return v;
    }
	else if (property.equalsIgnoreCase("RightYAxisTitlePosition")) {
        List<String> v = new ArrayList<>();
        v.add("AboveAxis");
        v.add("None");
        v.add("RightOfAxis");
        return v;
    }

	return null;
}

/**
Returns the index of the graph that is selected from the subproduct (graph) combo box.
@return the index of the graph that is selected from the subproduct (graph) combo box.
*/
protected int getSelectedGraph() {
	return _graph_JComboBox.getSelectedIndex();
}

/**
Returns the TSViewJFrame this JFrame works with.
@return the TSViewJFrame this JFrame works with.
*/
protected TSViewJFrame getTSViewJFrame() {
	return _tsview_gui;
}

/**
Display and initialize the GUI.
@param tsview_gui The main TSViewJFrame that manages the view windows.
@param tsgraphcanvas TSGraphJComponent associated with the product.
@param visible whether the GUI is to be opened visible or not.
*/
private void initialize (TSViewJFrame tsview_gui, TSGraphJComponent tsgraphcanvas, boolean visible) {
	_tsview_gui = tsview_gui;
	_tsproduct = tsgraphcanvas.getTSProduct();
	_selected_data = 0;	// First one in list.
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	__graphJFrame = getTSViewJFrame().getViewGraphJFrame();

	__annotationProviders = new ArrayList<>();
	/*
	String prop_value = tsgraphcanvas.getTSProduct().getPropValue( "Product.AnnotationProviders");

	if (prop_value == null) {
		// Try the old method.
		prop_value = tsview_gui.getPropValue("AnnotationProviders");
	}

	Vector v = new Vector();

	if (prop_value == null) {
		// None could be found.
	}
	else {
		v = StringUtil.breakStringList(prop_value, ",", 0);
		if (v == null) {
			v = new Vector();
		}
	}

	String s = null;
	for (int i = 0; i < v.size(); i++) {
		s = (String)v.elementAt(i);
		__annotationProviders.add(s.trim());
	}
	*/

	__annotationProviders.add("");

	List<TSProductAnnotationProvider> v = tsview_gui.getTSProductAnnotationProviders();
	if (v != null) {
		int size = v.size();
		int size2 = 0;
		List<String> v2 = null;
		TSProductAnnotationProvider tsap = null;
		for (int i = 0; i < size; i++) {
			tsap = v.get(i);
			v2 = tsap.getAnnotationProviderChoices();

			if (v2 != null) {
				size2 = v2.size();

				for (int j = 0; j < size2; j++) {
					__annotationProviders.add( v2.get(j).trim());
				}
			}
		}
	}

	int num = _tsproduct.getNumSubProducts();
	String prop_val = null;
	for (int i = 0; i < num; i++) {
		prop_val = _tsproduct.getLayeredPropValue("AnnotationProvider", i, -1, false);
		if (prop_val != null) {
			if (StringUtil.indexOf(__annotationProviders, prop_val) == -1) {
			    	__annotationProviders.add(prop_val);
			}
		}
	}

	java.util.Collections.sort(__annotationProviders);

	openGUI(visible);
	checkGUIState();
}

/**
Respond to ItemEvents.
@param evt Item event due to list change, etc.
*/
public void itemStateChanged ( ItemEvent evt ) {
	checkGUIState();
	if (!__is_initialized || __ignoreItemStateChange) {
		return;
	}

	// For all events, only care about when a selection is made..

	Object o = evt.getItemSelectable();

	if (evt.getStateChange() != ItemEvent.SELECTED) {
		return;
	}

	String selected = null;

	boolean graphTypeSetValues = false;

	if (o == __annotation_JComboBox) {
		// A new annotation has been selected.
		// First save the settings from the current display.
		updateTSProduct();
		// Now update the annotation editor fields to be updated.
		// The number at the front of the items allows the annotation to be looked up.
		__selectedAnnotationIndex = __annotation_JComboBox.getSelectedIndex();
		if (__selectedAnnotationIndex < 0) {
			return;
		}
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);
	}
	else if (o == __annotation_ShapeType_JComboBox) {
		selected = __annotation_ShapeType_JComboBox.getSelected();
		if (selected.equalsIgnoreCase("Line")) {
			__annotation_line_JPanel.setVisible(true);
			__annotation_rectangle_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(false);
			__annotation_text_JPanel.setVisible(false);
		}
		else if (selected.equalsIgnoreCase("Rectangle")) {
			__annotation_line_JPanel.setVisible(false);
			__annotation_rectangle_JPanel.setVisible(true);
			__annotation_text_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(false);
		}
		else if (selected.equalsIgnoreCase("Symbol")) {
			__annotation_line_JPanel.setVisible(false);
			__annotation_rectangle_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(true);
			__annotation_text_JPanel.setVisible(false);
		}
		else if (selected.equalsIgnoreCase("Text")) {
			__annotation_line_JPanel.setVisible(false);
			__annotation_rectangle_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(false);
			__annotation_text_JPanel.setVisible(true);
		}
	}
	else if (o == __annotation_line_color_JComboBox) {
		try {
			__annotation_line_color_JTextField.setBackground(
				(Color)GRColor.parseColor(__annotation_line_color_JComboBox.getSelected()));
			__annotation_line_color_JTextField.setText(__annotation_line_color_JComboBox.getSelected());
		}
		catch (Exception e2) {
			__annotation_line_color_JTextField.setBackground(Color.white);
			__annotation_line_color_JTextField.setText("White");
		}
	}
	else if (o == __annotation_rectangle_color_JComboBox) {
		try {
			__annotation_rectangle_color_JTextField.setBackground(
				(Color)GRColor.parseColor(__annotation_rectangle_color_JComboBox.getSelected()));
			__annotation_rectangle_color_JTextField.setText(__annotation_rectangle_color_JComboBox.getSelected());
		}
		catch (Exception e2) {
			__annotation_rectangle_color_JTextField.setBackground(Color.white);
			__annotation_rectangle_color_JTextField.setText("White");
		}
	}
	else if (o == __annotation_symbol_color_JComboBox) {
		try {
			__annotation_symbol_color_JTextField.setBackground(
				(Color)GRColor.parseColor(__annotation_symbol_color_JComboBox.getSelected()));
			__annotation_symbol_color_JTextField.setText(
				__annotation_symbol_color_JComboBox.getSelected());
		}
		catch (Exception e2) {
			__annotation_symbol_color_JTextField.setBackground(Color.white);
			__annotation_symbol_color_JTextField.setText("White");
		}
	}
	else if (o == __annotation_text_color_JComboBox) {
		try {
			__annotation_text_color_JTextField.setBackground((Color)GRColor.parseColor(
				__annotation_text_color_JComboBox.getSelected()));
			__annotation_text_color_JTextField.setText(__annotation_text_color_JComboBox.getSelected());
		}
		catch (Exception e2) {
			__annotation_text_color_JTextField.setBackground(Color.white);
			__annotation_text_color_JTextField.setText("White");
		}
	}
	else if ( o == _graph_JComboBox ) {
		// A new graph has been selected.
		// First save the settings from the current display.
		// Now refresh the graph tabs for the selected graph.
		// The graphs are labeled with a number (starting at 1), " - ", and then the title from the graph.
		setSubproduct2 ( _graph_JComboBox.getSelected() );
		__layoutCanvas.repaint();
	}
	else if ( o == _graph_datalabelformat_JComboBox ) {
		_graph_datalabelformat_JTextField.setText(
			_graph_datalabelformat_JTextField.getText() +
			StringUtil.getToken(
			_graph_datalabelformat_JComboBox.getSelected(),
			" ",0,0));
	}
	else if ( o == _graph_legendformat_JComboBox ) {
		String token = StringUtil.getToken(
			_graph_legendformat_JComboBox.getSelected(),
			" ",0,0 );
		if ( token.equalsIgnoreCase("Auto") ) {
			// Auto must stand on its own.
			_graph_legendformat_JTextField.setText( "Auto" );
		}
		else {	_graph_legendformat_JTextField.setText(
			_graph_legendformat_JTextField.getText() + token );
		}
	}
	else if ( o == _graph_bottomx_majorgrid_color_JComboBox ) {
		// Set the choice in the color textfield.
		_graph_bottomx_majorgrid_color_JTextField.setText(
		_graph_bottomx_majorgrid_color_JComboBox.getSelected());
		try {
			if ( _graph_bottomx_majorgrid_color_JTextField.getText().equalsIgnoreCase("None")) {
			_graph_bottomx_majorgrid_color_JTextField.setBackground(Color.white );
			}
			else {
				_graph_bottomx_majorgrid_color_JTextField.setBackground((Color)GRColor.parseColor(
				_graph_bottomx_majorgrid_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_bottomx_majorgrid_color_JTextField.setBackground(Color.white );
		}
	}
	else if ( o == _graph_lefty_majorgrid_color_JComboBox ) {
		// Set the choice in the color textfield.
		_graph_lefty_majorgrid_color_JTextField.setText(
		_graph_lefty_majorgrid_color_JComboBox.getSelected());
		try {
		    if ( _graph_lefty_majorgrid_color_JTextField.getText().equalsIgnoreCase("None") ) {
		        _graph_lefty_majorgrid_color_JTextField.setBackground ( Color.white );
			}
			else {
    			_graph_lefty_majorgrid_color_JTextField.setBackground (
    			    (Color)GRColor.parseColor(_graph_lefty_majorgrid_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_lefty_majorgrid_color_JTextField.setBackground ( Color.white );
		}
	}
	else if ( o == _graph_lefty_majortick_color_JComboBox ) {
		// Set the choice in the color textfield.
		_graph_lefty_majortick_color_JTextField.setText(
		_graph_lefty_majortick_color_JComboBox.getSelected());
		try {
		    if ( _graph_lefty_majortick_color_JTextField.getText().equalsIgnoreCase("None") ) {
		        _graph_lefty_majortick_color_JTextField.setBackground ( Color.white );
			}
			else {
    			_graph_lefty_majortick_color_JTextField.setBackground (
    			    (Color)GRColor.parseColor(_graph_lefty_majortick_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_lefty_majortick_color_JTextField.setBackground ( Color.white );
		}
	}
	else if ( o == _graph_righty_majorgrid_color_JComboBox ) {
		// Set the choice in the color textfield.
		_graph_righty_majorgrid_color_JTextField.setText(
		_graph_righty_majorgrid_color_JComboBox.getSelected());
		try {
		    if ( _graph_righty_majorgrid_color_JTextField.getText().equalsIgnoreCase("None") ) {
		        _graph_righty_majorgrid_color_JTextField.setBackground ( Color.white );
			}
			else {
    			_graph_righty_majorgrid_color_JTextField.setBackground (
    			    (Color)GRColor.parseColor(_graph_righty_majorgrid_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_righty_majorgrid_color_JTextField.setBackground ( Color.white );
		}
	}
	else if ( o == _graph_righty_majortick_color_JComboBox ) {
		// Set the choice in the color textfield.
		_graph_righty_majortick_color_JTextField.setText(
		_graph_righty_majortick_color_JComboBox.getSelected());
		try {
		    if ( _graph_righty_majortick_color_JTextField.getText().equalsIgnoreCase("None") ) {
		        _graph_righty_majortick_color_JTextField.setBackground ( Color.white );
			}
			else {
    			_graph_righty_majortick_color_JTextField.setBackground (
    			    (Color)GRColor.parseColor(_graph_righty_majortick_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_righty_majortick_color_JTextField.setBackground ( Color.white );
		}
	}
	else if ( o == __ts_JComboBox ) {
		// A new time series has been selected.
		// First save the settings from the current display.
		updateTSProduct();
		// Now update the time series tabs to agree with the selected time series.
		// The number at the front of the items allows the time series to be looked up (the rest of the string is ignored.
		selected = __ts_JComboBox.getSelected ();
		List<String> list = StringUtil.breakStringList ( selected, " ", 0 );
		if ( list == null ) {
			return;
		}
		selected = list.get(0);
		if ( !StringUtil.isInteger(selected) ) {
			return;
		}
		_selected_data = StringUtil.atoi(selected) - 1;
		displayDataProperties ( _selected_subproduct, _selected_data );
	}
	else if ( o == _ts_datalabelformat_JComboBox ) {
		_ts_datalabelformat_JTextField.setText(_ts_datalabelformat_JTextField.getText() +
			StringUtil.getToken(_ts_datalabelformat_JComboBox.getSelected()," ",0,0));
	}
	else if ( o == _ts_legendformat_JComboBox ) {
		String token = StringUtil.getToken(_ts_legendformat_JComboBox.getSelected(), " ",0,0 );
		if ( token.equalsIgnoreCase("Auto") ) {
			// Auto must stand on its own.
			_ts_legendformat_JTextField.setText( "Auto" );
		}
		else {
		    _ts_legendformat_JTextField.setText( _ts_legendformat_JTextField.getText() + token );
		}
	}
	else if ( o == _ts_color_JComboBox ) {
		// Set the choice in the color textfield.
		_ts_color_JTextField.setText(_ts_color_JComboBox.getSelected());
		try {
		    _ts_color_JTextField.setBackground ( (Color)GRColor.parseColor(_ts_color_JTextField.getText()) );
		}
		catch ( Exception e ) {
			_ts_color_JTextField.setBackground ( Color.white );
		}
	}
	else if ( o == _ts_regressionline_JCheckBox ) {
		if ( _ts_regressionline_JCheckBox.isSelected() ) {
			_ts_confidenceinterval_JComboBox.setEnabled(true);
		}
		else {
		    _ts_confidenceinterval_JComboBox.setEnabled(false);
		}
	}
	else if ( o == _xyscatter_analysis_fill_JCheckBox ) {
		if ( _xyscatter_analysis_fill_JCheckBox.isSelected() ) {
			_xyscatter_analysis_intercept_JTextField.setEnabled(true);
			_xyscatter_analysis_fill_period_start_JTextField.setEnabled(true);
			_xyscatter_analysis_fill_period_end_JTextField.setEnabled(true);
		}
		else {
		    _xyscatter_analysis_intercept_JTextField.setEnabled(false);
			_xyscatter_analysis_fill_period_start_JTextField.setEnabled(false );
			_xyscatter_analysis_fill_period_end_JTextField.setEnabled(false);
		}
	}
	else if (o == _graph_lefty_graphtype_JComboBox) {
		TSGraphType graphTypeLeft = TSGraphType.valueOfIgnoreCase (_graph_lefty_graphtype_JComboBox.getSelected() );
		if (graphTypeLeft == TSGraphType.XY_SCATTER
		    || graphTypeLeft == TSGraphType.PREDICTED_VALUE
		    || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			// Display the regression analysis panel.
			_blank_analysis_JPanel.setVisible ( false );
			_xyscatter_analysis_JPanel.setVisible ( true );
			_xyscatter_analysis_JPanel.repaint();
			// Display the regression analysis panel.
			_ts_blank_analysis_JPanel.setVisible ( false );
			_ts_xyscatter_analysis_JPanel.setVisible ( true );
			_ts_xyscatter_analysis_JPanel.repaint();
			if (graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
				_graph_barposition_JComboBox.setVisible(true);
				_graph_barposition_JLabel.setVisible(true);
				_graph_barOverlap_JComboBox.setVisible(true);
                _graph_barOverlap_JLabel.setVisible(true);
			}
			else {
				_graph_barposition_JComboBox.setVisible(false);
				_graph_barposition_JLabel.setVisible(false);
			    _graph_barOverlap_JComboBox.setVisible(false);
                _graph_barOverlap_JLabel.setVisible(false);
			}
		}
		else {
			// Display the blank analysis panel.
			_xyscatter_analysis_JPanel.setVisible ( false );
			_blank_analysis_JPanel.setVisible ( true );
			_blank_analysis_JPanel.repaint();
			_ts_xyscatter_analysis_JPanel.setVisible ( false );
			_ts_blank_analysis_JPanel.setVisible ( true );
			_ts_blank_analysis_JPanel.repaint();
			if (graphTypeLeft == TSGraphType.BAR) {
				_graph_barposition_JComboBox.setVisible(true);
				_graph_barposition_JLabel.setVisible(true);
				_graph_barOverlap_JComboBox.setVisible(true);
                _graph_barOverlap_JLabel.setVisible(true);
			}
			else {
				_graph_barposition_JComboBox.setVisible(false);
				_graph_barposition_JLabel.setVisible(false);
				_graph_barOverlap_JComboBox.setVisible(false);
                _graph_barOverlap_JLabel.setVisible(false);
			}
		}
		clearGraphProperties(_tsproduct, _selected_subproduct, -1, graphTypeLeft, null);
		graphTypeSetValues = true;
	}
	else if (o == _graph_righty_graphtype_JComboBox) {
		TSGraphType graphTypeRight = TSGraphType.valueOfIgnoreCase (_graph_righty_graphtype_JComboBox.getSelected() );
		// TODO SAM 2016-10-21 Need to evaluate more how right axis is impacted by the more advanced graph types.
		// For now assume the right y-axis just controls basic drawing.
		if (graphTypeRight == TSGraphType.BAR) {
			_graph_righty_barposition_JComboBox.setVisible(true);
			_graph_righty_barposition_JLabel.setVisible(true);
			_graph_righty_barOverlap_JComboBox.setVisible(true);
            _graph_righty_barOverlap_JLabel.setVisible(true);
		}
		else {
			_graph_righty_barposition_JComboBox.setVisible(false);
			_graph_righty_barposition_JLabel.setVisible(false);
			_graph_righty_barOverlap_JComboBox.setVisible(false);
            _graph_righty_barOverlap_JLabel.setVisible(false);
		}
		clearGraphProperties(_tsproduct,_selected_subproduct, -1, null, graphTypeRight);
		graphTypeSetValues = true;
	}
	checkGUIState();

	enableComponentsBasedOnGraphType(_selected_subproduct, _selected_data, graphTypeSetValues);
}

/**
Respond to key press events.  Most single-key events are handled in keyReleased to prevent multiple events.
Do track when the shift is pressed here.
@param event Key event to process.
*/
public void keyPressed ( KeyEvent event ) {
	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_SHIFT ) {
		JGUIUtil.setShiftDown(true);
	}
	else if ( code == KeyEvent.VK_CONTROL ) {
		JGUIUtil.setControlDown(true);
	}
}

/**
Respond to key release events.
@param event Key event to process.
*/
public void keyReleased ( KeyEvent event ) {
	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_SHIFT ) {
		JGUIUtil.setShiftDown(false);
	}
	else if ( code == KeyEvent.VK_CONTROL ) {
		JGUIUtil.setControlDown(false);
	}
}

/**
Handle key type events.
@param event Key event to process.
*/
public void keyTyped ( KeyEvent event ) {
}

/**
Limits the choices in the graph type combo box to only those that should
be available given the original graph type of the graph.
@param subproduct the subproduct (0+) for which the graphs should be limited.
*/
private void limitGraphTypes(int subproduct) {
	boolean hold = __ignoreItemStateChange;
	// Ignore itemStateChange events while the graph type choices are being manipulated.
	__ignoreItemStateChange = true;

	// Left y-axis.

	String leftYAxisOriginalGraphType = _tsproduct.getLayeredPropValue( "LeftYAxisOriginalGraphType", subproduct, -1, false);

	if ( leftYAxisOriginalGraphType.equals("Area") || leftYAxisOriginalGraphType.equals("AreaStacked") ||
	    leftYAxisOriginalGraphType.equals("Bar") || leftYAxisOriginalGraphType.equals("Line")
	    || leftYAxisOriginalGraphType.equals("Point") ) {
		// Original graph type was basic so limit graph type choices to basic types.
		_graph_lefty_graphtype_JComboBox.removeAll();
		_graph_lefty_graphtype_JComboBox.add("Area");
		_graph_lefty_graphtype_JComboBox.add("AreaStacked");
		_graph_lefty_graphtype_JComboBox.add("Bar");
		_graph_lefty_graphtype_JComboBox.add("Line");
		_graph_lefty_graphtype_JComboBox.add("Point");
	}
	else {
	    for ( TSGraphType graphType: TSGraphType.values() ) {
	        if ( graphType != TSGraphType.UNKNOWN ) {
	            _graph_lefty_graphtype_JComboBox.add ( "" + graphType );
	        }
	    }
	}

	// Right y-axis (can only use basic graph types).

	//String originalRightYAxisGraphType = _tsproduct.getLayeredPropValue( "RightYAxisOriginalGraphType", subproduct, -1, false);

	//if ( originalRightYAxisGraphType.equals("Area") || originalRightYAxisGraphType.equals("AreaStacked") ||
	//	originalRightYAxisGraphType.equals("Bar") || originalRightYAxisGraphType.equals("Line")
	//    || originalRightYAxisGraphType.equals("Point") ) {
		// Original graph type was basic so limit graph type choices to basic types.
		_graph_righty_graphtype_JComboBox.removeAll();
		_graph_righty_graphtype_JComboBox.add("None");
		_graph_righty_graphtype_JComboBox.add("Area");
		//_graph_righty_graphtype_JComboBox.add("AreaStacked"); // TODO SAM 2016-10-24 figure out - need to manage the derived data.
		_graph_righty_graphtype_JComboBox.add("Bar");
		_graph_righty_graphtype_JComboBox.add("Line");
		_graph_righty_graphtype_JComboBox.add("Point");
	//}
	//else {
	//	_graph_righty_graphtype_JComboBox.removeAll();
	//    for ( TSGraphType graphType: TSGraphType.values() ) {
	//        if ( graphType != TSGraphType.UNKNOWN ) {
	//            _graph_righty_graphtype_JComboBox.addItem ( "" + graphType );
	//        }
	//    }
	//}

	__ignoreItemStateChange = hold;
}

/**
Moves a time series from one graph to another and adjusts the numbering for the other times series appropriately.
The data that get moved to the new graph are the currently-selected time series from the time series combo box.
@param fromGraph the graph (0-based) under which the data are currently stored.
@param toGraph the graph (0-based) under which to move the data.
*/
protected void moveSelectedData(int fromGraph, int toGraph, int fromTS) {
	int fromDataCount = _tsproduct.getNumData(fromGraph);
	//	int moveTS = __ts_JComboBox.getSelectedIndex();
	int moveTS = fromTS;
	int toDataCount = _tsproduct.getNumData(toGraph);
	_tsproduct.setDirty(true);
	_tsproduct.renameDataProps("" + (fromGraph + 1), "" + (moveTS + 1), "" + (toGraph + 1), "" + (toDataCount + 1));

	for (int i = moveTS + 2; i <= fromDataCount; i++) {
		_tsproduct.renameDataProps("" + (fromGraph + 1), "" + i, "" + (fromGraph + 1), "" + (i - 1));
	}

	_tsproduct.sortProps();

	int nts = _tsproduct.getNumData ( _selected_subproduct );
	if (nts > 0) {
		// Display the first time series.
		_selected_data = 0;
		displayDataProperties ( _selected_subproduct, _selected_data );
	}
	else {
		_selected_data = -1;
		clearDataProperties();
		_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
		_tsproduct.setPropValue("LeftYAxisUnits", null, _selected_subproduct, -1);
		_graph_lefty_units_JTextField.setText("");
	}

	if (_tsproduct.getNumData(toGraph) == 1) {
		// A time series was moved into the graph.
		String propVal = _tsproduct.getLayeredPropValue("GraphType", toGraph, 0, false);
		_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		_tsproduct.setPropValue("GraphType", propVal, toGraph, -1 );
	}

	// Do this any time graphs are changed.
	getTSViewJFrame().getViewGraphJFrame().getMainJComponent().reinitializeGraphs(_tsproduct);
	redisplayProperties();
	if (getTSViewJFrame().getViewGraphJFrame().getReferenceGraph() != null){
		getTSViewJFrame().getViewGraphJFrame().getReferenceGraph().reinitializeGraphs(_tsproduct);
	}

	_tsview_gui.refresh();
	checkGUIState();
}

/**
Open the GUI.
@param visible Indicates if the GUI should be visible at creation.
*/
private void openGUI ( boolean visible ) {
	__is_initialized = false;
	String routine = getClass().getSimpleName() + ".openGUI";
	if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( "Time Series Product Properties" );
	}
	else {
		setTitle( JGUIUtil.getAppNameForWindows() + " - Time Series Product Properties" );
	}

	// Start a big try block to set up the GUI.
	try {

		// Add a listener to catch window manager events.

		addWindowListener ( this );

		// Lay out the main window component by component.  We will start with the menubar default components.
		// Then add each requested component to the menu bar and the interface.

		GridBagLayout gbl = new GridBagLayout();

		int edge = 2;
		Insets insetsLR = new Insets ( 0, edge, 0, edge ); // Buffer around graphical components.

		// Add a panel to hold the main components.

		JPanel display_JPanel = new JPanel ();
		display_JPanel.setLayout ( gbl );
		getContentPane().add ( display_JPanel );

		int y = 0;

		// Panel for graph layout (shows sub-product row/column), etc.

		JGUIUtil.addComponent ( display_JPanel, createLayoutJPanel(),
				0, y, 2, 1, 0, 0,
				insetsLR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

		// Panel for product properties.

		try {
			JGUIUtil.addComponent ( display_JPanel, createProductJPanel(),
					2, y, 8, 1, 1, 0,
					insetsLR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );
			displayProductProperties ();
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error setting up product panel (" + e + ").");
			Message.printWarning(3,routine,e);
		}

		// Panel for subproduct properties.

		try {
			JGUIUtil.addComponent ( display_JPanel, createSubproductJPanel(),
					0, ++y, 10, 1, 1, 0,
					insetsLR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );
			displaySubproductProperties ( 0 );
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error setting up subproduct (graph) panel (" + e + ").");
			Message.printWarning(3,routine,e);
		}

		// Panel for time series properties.

		try {
			JGUIUtil.addComponent ( display_JPanel, createDataJPanel(),
				0, ++y, 10, 1, 1, 0,
				insetsLR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );
		}
		catch ( Exception e ) {
			Message.printWarning(3,routine,"Error setting up time series (data) panel (" + e + ").");
			Message.printWarning(3,routine,e);
		}

		__is_initialized = true;
		displayDataProperties ( 0, 0 );

		// Put the buttons on the bottom of the window.

		JPanel button_JPanel = new JPanel ();
		button_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

		_apply_JButton = new SimpleJButton("Apply", "TSProductJFrame.Apply",this);
		button_JPanel.add ( _apply_JButton );

		_close_JButton = new SimpleJButton("Close", "TSProductJFrame.Close",this);
		button_JPanel.add ( _close_JButton );

		getContentPane().add ( "South", button_JPanel );
		button_JPanel = null;

		// Left y-axis.
		_graph_lefty_graphtype_JComboBox.addItemListener(this);
		// Right y-axis.
		_graph_righty_graphtype_JComboBox.addItemListener(this);

		// Select these other graph types in order that the packing takes into account their components.
		// TODO SAM 2016-10-23 what does the following do?  Should it use a candidate string for the longest selection?
		// Could do that in the code that creates the component.

		// Left y-axis.
		_graph_lefty_graphtype_JComboBox.select("XY-Scatter");
		_graph_lefty_graphtype_JComboBox.select("Bar");
		_graph_lefty_graphtype_JComboBox.setEnabled(true);
		// Right y-axis.
		_graph_righty_graphtype_JComboBox.select("XY-Scatter");
		_graph_righty_graphtype_JComboBox.select("Bar");
		_graph_righty_graphtype_JComboBox.setEnabled(true);

		// Makes the text panel visible in order that pack()ing the JFrame will account for space in the largest annotation panel.
		// Currently the text panel (the default one) is the larger, so this isn't necessary,
		// but in the future the addition of more panel types may require it.
		__annotation_line_JPanel.setVisible(false);
		__annotation_symbol_JPanel.setVisible(false);
		__annotation_text_JPanel.setVisible(true);
		pack();

		// This is called now in order that it will set the proper annotation panel visible at this point,
		// and room will already have been reserved to display the largest annotation panel.
		displayAnnotationProperties(0, 0);

		setSubproduct(0);

		JGUIUtil.center ( this, __graphJFrame );
		setVisible ( visible );
	} // End of try.
	catch ( Exception e ) {
		Message.printWarning(3,routine,"Error opening graph (" + e + ").");
		Message.printWarning(3,routine,e);
	}
	checkGUIState();
	// TODO SAM 2016-10-23 what does the following "15" do?
	setSize(getWidth(), getHeight() + 15);
	setResizable(false);

	int nsp = _tsproduct.getNumSubProducts();
	String graphType = null;
	// How set indicates how a property was set, to differentiate between user and internal properties.
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
	// The original graph type is what was requested from calling code.
	// For some graph types the graph types can only be changed to a subset.
	// Save the original graph type for some checks in limitGraphTypes().
	for (int isp = 0; isp < nsp; isp++) {
		// Left y-axis.
		graphType = _tsproduct.getLayeredPropValue( "LeftYAxisGraphType", isp, -1, false);
		if ( graphType == null ) {
			// Try legacy property name before support for right y-axis was added.
			graphType = _tsproduct.getLayeredPropValue( "GraphType", isp, -1, false);
		}
		_tsproduct.setPropValue("LeftYAxisOriginalGraphType", graphType, isp, -1);
		// Right y-axis.
		graphType = _tsproduct.getLayeredPropValue( "RightYAxisGraphType", isp, -1, false);
		if ( graphType == null ) {
			// Legacy behavior so set to "None".
			graphType = "None";
		}
		_tsproduct.setPropValue("RightYAxisOriginalGraphType", graphType, isp, -1);
	}
	_tsproduct.getPropList().setHowSet(how_set_prev);
	__limitGraphTypes = true;
}

/**
Handle window closing event.
*/
public void processWindowEvent ( WindowEvent e) {
	if (e.getID() == WindowEvent.WINDOW_CLOSING ) {
		super.processWindowEvent(e);
		_tsview_gui.closeGUI(TSViewType.PROPERTIES);
	}
	else {
	    super.processWindowEvent(e);
	}
}

/**
Re-reads the properties from the TSProduct and sets the GUI to match what is read.
Called when a new graph is added, the order of graphs are changed, or a graph is removed.
Forces redisplay of the properties in the tsproduct.
*/
protected void redisplayProperties() {
	// If the select subproduct is -1, then it's a new table that was just added,
	// so all the properties should be cleared and set to default values.
	if (_selected_subproduct == -1) {
		clearAllProperties();
	}
	else {
		// Otherwise, read the subproduct info from the prop list.
		displayProductProperties();
		displaySubproductProperties(_selected_subproduct);
		displayDataProperties(_selected_subproduct, _selected_data);
	}
	_graph_JComboBox.removeItemListener(this);
	_graph_JComboBox.removeAllItems();
	int nsub = _tsproduct.getNumSubProducts();

	if (nsub == 0) {
		_graph_JComboBox.add(NO_GRAPHS_DEFINED);
		_graph_JComboBox.select(0);
	}
	else {
		String prop_val = null;
		for (int isub = 0; isub < nsub; isub++) {
			prop_val = _tsproduct.getLayeredPropValue("MainTitleString", isub, -1, false);
			if (prop_val == null) {
				prop_val = "";
			}
			if (prop_val.length() > 60) {
				prop_val = prop_val.substring(0, 60);
			}
			_graph_JComboBox.add("" + (isub + 1) + " - " + prop_val);
		}
		_graph_JComboBox.select(_selected_subproduct);
	}
	setSubproduct2 ( _graph_JComboBox.getSelected() );
	__layoutCanvas.repaint();

	_graph_JComboBox.addItemListener(this);
}

/**
Enables or disables all the components related to annotations.
@param enabled whether to enable or disable the components.
*/
private void setAnnotationFieldsEnabled(boolean enabled) {
	JGUIUtil.setEnabled(__annotation_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_id_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_tableid_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_ShapeType_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_Order_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_line_color_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_color_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_color_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_line_color_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_XAxisSystem_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_YAxis_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_YAxisSystem_JComboBox, enabled);
	// JGUIUtil.setEnabled(__graphAnnotationProvider, enabled);
	JGUIUtil.setEnabled(__annotationLineStyleJComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_line_LineWidth_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_line_PointX1_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_line_PointY1_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_line_PointX2_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_line_PointY2_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_Text_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_PointX_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_PointY_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_Position_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_text_FontName_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_text_FontStyle_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_text_FontSize_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_color_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_color_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_PointX_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_PointY_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_SymbolPosition_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_SymbolStyle_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_symbol_SymbolSize_JComboBox, enabled);

	setEditable(__annotation_id_JTextField, enabled);
	setEditable(__annotation_line_color_JTextField, enabled);
	setEditable(__annotation_text_color_JTextField, enabled);
	setEditable(__annotation_line_LineWidth_JTextField, enabled);
	setEditable(__annotation_line_PointX1_JTextField, enabled);
	setEditable(__annotation_line_PointY1_JTextField, enabled);
	setEditable(__annotation_line_PointX2_JTextField, enabled);
	setEditable(__annotation_line_PointY2_JTextField, enabled);
	setEditable(__annotation_text_Text_JTextField, enabled);
	setEditable(__annotation_text_PointX_JTextField, enabled);
	setEditable(__annotation_text_PointY_JTextField, enabled);
	setEditable(__annotation_text_FontSize_JTextField, enabled);

	// Add is not enabled or disabled.  It is always enabled.
	// The other buttons should have been enabled or disabled already,
	// but if everything should be disabled, turn them off.
	if (!enabled) {
		JGUIUtil.setEnabled(__delAnnotationJButton, enabled);
		JGUIUtil.setEnabled(__moveAnnotationDownJButton, enabled);
		JGUIUtil.setEnabled(__moveAnnotationUpJButton, enabled);
	}
}

/**
Sets a text field editable or not.  Handles textfields that may be null.
@param tf the JTextField to set editable or not.
@param editable whether to set the text field editable or uneditable.
*/
private void setEditable(JTextField tf, boolean editable) {
	if (tf == null) {
		return;
	}
	tf.setEditable(editable);
}

/**
Enables or disables all components related to the graphs.
@param enabled whether to enable or disable the components.
*/
private void setGraphFieldsEnabled(boolean enabled) {
	JGUIUtil.setEnabled(_graph_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_JTabbedPane, enabled);
	// JGUIUtil.setEnabled(_graph_enabled_JCheckBox, enabled);
	JGUIUtil.setEnabled(_graph_isref_JCheckBox, enabled);
	JGUIUtil.setEnabled(_yPercentJTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_graphtype_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_barposition_JLabel, enabled);
	JGUIUtil.setEnabled(_graph_barposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_barOverlap_JLabel, enabled);
	JGUIUtil.setEnabled(_graph_barOverlap_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_maintitle_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_maintitle_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_maintitle_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_maintitle_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_subtitle_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_subtitle_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_subtitle_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_subtitle_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_label_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_label_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_label_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_title_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_title_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_title_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_title_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_majorgrid_color_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_bottomx_majorgrid_color_JComboBox, enabled);
	// JGUIUtil.setEnabled(_graph_bottomx_majorgrid_color_JButton, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_precision_JTextField, enabled);
	// JGUIUtil.setEnabled(_graph_lefty_type_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_min_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_max_JComboBox, enabled);
	// JGUIUtil.setEnabled(_graph_lefty_ignoreunits_JCheckBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_units_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_righty_legendposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_precision_JTextField, enabled);
	// JGUIUtil.setEnabled(_graph_righty_type_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_min_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_max_JComboBox, enabled);
	// JGUIUtil.setEnabled(_graph_righty_ignoreunits_JCheckBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_units_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_righty_title_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_righty_title_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_title_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_title_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_datalabelformat_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_datalabelformat_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_datalabelposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_datalabelfontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_datalabelfontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_datalabelfontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_legendformat_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_legendformat_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_legendposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legendfontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legendfontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legend_fontsize_JTextField, enabled);
	// JGUIUtil.setEnabled(_graph_zoomenabled_JCheckBox, enabled);
	// JGUIUtil.setEnabled(_graph_zoomgroup_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_analysis_JPanel, enabled);
	JGUIUtil.setEnabled(_graph_blank_analysis_JPanel, enabled);
	JGUIUtil.setEnabled(_blank_analysis_JPanel, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_JPanel, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_intercept_JTextField, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_method_JComboBox, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_transform_JComboBox, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_neqn_JComboBox, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_month_JTextField, enabled);
	JGUIUtil.setEnabled(_dep_analysis_period_start_JTextField, enabled);
	JGUIUtil.setEnabled(_dep_analysis_period_end_JTextField, enabled);
	JGUIUtil.setEnabled(_ind_analysis_period_start_JTextField, enabled);
	JGUIUtil.setEnabled(_ind_analysis_period_end_JTextField, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_fill_JCheckBox, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_fill_period_start_JTextField, enabled);
	JGUIUtil.setEnabled(_xyscatter_analysis_fill_period_end_JTextField, enabled);

	setEditable(_graph_maintitle_JTextField, enabled);
	setEditable(_graph_maintitle_fontsize_JTextField, enabled);
	setEditable(_graph_subtitle_JTextField, enabled);
	setEditable(_graph_subtitle_fontsize_JTextField, enabled);
	setEditable(_graph_bottomx_label_fontsize_JTextField, enabled);
	setEditable(_graph_bottomx_title_JTextField, enabled);
	setEditable(_graph_bottomx_title_fontsize_JTextField, enabled);
	setEditable(_graph_bottomx_majorgrid_color_JTextField, enabled);
	setEditable(_graph_lefty_label_fontsize_JTextField, enabled);
	setEditable(_graph_lefty_majorgrid_color_JTextField, enabled);
	setEditable(_graph_lefty_majortick_color_JTextField, enabled);
	setEditable(_graph_lefty_precision_JTextField, enabled);
	setEditable(_graph_lefty_title_JTextField, enabled);
	setEditable(_graph_lefty_title_fontsize_JTextField, enabled);
	setEditable(_graph_lefty_units_JTextField, enabled);
	setEditable(_graph_righty_label_fontsize_JTextField, enabled);
	setEditable(_graph_righty_majorgrid_color_JTextField, enabled);
	setEditable(_graph_righty_majortick_color_JTextField, enabled);
	setEditable(_graph_righty_precision_JTextField, enabled);
	setEditable(_graph_righty_title_JTextField, enabled);
	setEditable(_graph_righty_title_fontsize_JTextField, enabled);
	setEditable(_graph_righty_units_JTextField, enabled);
	setEditable(_graph_datalabelformat_JTextField, enabled);
	setEditable(_graph_datalabelfontsize_JTextField, enabled);
	setEditable(_graph_legendformat_JTextField, enabled);
	setEditable(_graph_legend_fontsize_JTextField, enabled);
	// setEditable(_graph_zoomgroup_JTextField, enabled);
	setEditable(_xyscatter_analysis_intercept_JTextField, enabled);
	setEditable(_xyscatter_analysis_month_JTextField, enabled);
	setEditable(_dep_analysis_period_start_JTextField, enabled);
	setEditable(_dep_analysis_period_end_JTextField, enabled);
	setEditable(_ind_analysis_period_start_JTextField, enabled);
	setEditable(_ind_analysis_period_end_JTextField, enabled);
	setEditable(_xyscatter_analysis_fill_period_start_JTextField, enabled);
	setEditable(_xyscatter_analysis_fill_period_end_JTextField, enabled);
}

/**
Sets the maximum Y value in the LeftYAxisMax combo box to the specified value.
The graph will change to show the new maximum Y value.
@param value the value to set the combo box selection to.
*/
public void setMaximumYValue(String value) {
	setMaximumYValue(value, true);
}

/**
Sets the maximum Y value in the LeftYAxisMax combo box to the specified value.
@param value the value to set the combo box selection to.
@param apply if true, then applyClicked() will be called and the new selection's value will be applied to the product graph.
*/
public void setMaximumYValue(String value, boolean apply) {
	if (IOUtil.testing()) {
		Message.printStatus(1, "", "Setting maximum y value to: " + value);
	}
	_graph_lefty_max_JComboBox.select(value);

	if (apply) {
		applyClicked();
	}
}

/**
Sets the minimum Y value in the LeftYAxisMin combo box to the specified value.
The graph will change to show the new minimum Y value.
@param value the value to set the combo box selection to.
*/
public void setMinimumYValue(String value) {
	setMinimumYValue(value, true);
}

/**
Sets the minimum Y value in the LeftYAxisMin combo box to the specified value.
@param value the value to set the combo box selection to.
@param apply if true, then applyClicked() will be called and the new selection's value will be applied to the product graph.
*/
public void setMinimumYValue(String value, boolean apply) {
	if (IOUtil.testing()) {
		Message.printStatus(1, "", "Setting minimum y value to: " + value);
	}
	_graph_lefty_min_JComboBox.select(value);

	if (apply) {
		applyClicked();
	}
}

/**
Sets the selected graph and selects that graph from the graph combo box.
@param index the index of the graph to select.
*/
protected void setSelectedGraph(int index) {
	updateTSProduct();
	__ignoreItemStateChange = false;
	_graph_JComboBox.select(index);
	__ignoreItemStateChange = false;
	int nts = _tsproduct.getNumData ( _selected_subproduct );
	if (nts > 0) {
		// Display the first time series.
		_selected_data = 0;
		__ts_JComboBox.select(_selected_data);
		displayDataProperties ( _selected_subproduct, _selected_data );
	}
	else {
		_selected_data = -1;
		clearDataProperties();
	}

	int nann = _tsproduct.getNumAnnotations(_selected_subproduct);
	if (nann > 0) {
		__selectedAnnotationIndex = 0;
		__annotation_JComboBox.select(__selectedAnnotationIndex);
		displayAnnotationProperties(_selected_subproduct,__selectedAnnotationIndex);
	}
	else {
		__selectedAnnotationIndex = -1;
		clearAnnotationProperties();
	}
	checkGUIState();
}

/**
Directly sets the number of the selected subproduct and data, without triggering any other methods internally.
@param isub the subproduct to select
@param idata the data to select.
*/
protected void setSelectedSubProductAndData(int isub, int idata) {
	_selected_subproduct = isub;
	_selected_data = idata;
}

/**
Set the selected sub-product.
This method is called when the properties are displayed from a popup menu from the graph.
This is equivalent to changing the graph choice. @param subproduct Subproduct number.
*/
protected void setSubproduct(int subproduct) {
	// A new graph has been selected.
	// First save the settings from the current display.
	updateTSProduct();
	// Select the subproduct in the list.
	try {
	    _graph_JComboBox.select ( subproduct );
	}
	catch ( Exception e ) {
		_graph_JComboBox.select ( 0 );
	}
	// Now refresh the graph tabs for the selected graph.
	// The graphs are labeled with a number (starting at 1), " - ", and then the title from the graph.
	setSubproduct2 ( _graph_JComboBox.getSelected() );
}

/**
Second phase of setting the subproduct.
Does all the actual selection of values and does not call updateTSProduct first.
@param selected Selected subproduct (item from _graph_Choice).
*/
private void setSubproduct2 ( String selected ) {
	List<String> list = StringUtil.breakStringList ( selected, " ", 0 );
	if ( list == null ) {
		return;
	}
	// Else, get the selected product from the first token.
	selected = list.get(0);
	if ( !StringUtil.isInteger(selected) ) {
		return;
	}
	// Internally, products start at 0.
	_selected_subproduct = StringUtil.atoi(selected) - 1;
	displaySubproductProperties ( _selected_subproduct );
	// Update the list of choices in the time series to the list for the current graph.
	__ts_JComboBox.removeAll();
	int nts = _tsproduct.getNumData ( _selected_subproduct );
	String prop_val;
	String sequence_number;
	for ( int its = 0; its < nts; its++ ) {
		prop_val = _tsproduct.getLayeredPropValue( "TSAlias", _selected_subproduct, its, false );
		if ( (prop_val == null) || prop_val.trim().equals("") ) {
			prop_val = _tsproduct.getLayeredPropValue( "TSID",_selected_subproduct, its, false );
		}
		sequence_number = _tsproduct.getLayeredPropValue("SequenceNumber", _selected_subproduct, its, false );
		if ((sequence_number == null) || sequence_number.equals("-1") ){
			sequence_number = "";
		}
		else {
		    sequence_number = " [" + sequence_number + "] ";
		}
		if ( prop_val == null ) {
			prop_val = "No data";
		}
		else {
		    // Limit the TSID to 60 characters.
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60) + "..." + sequence_number;
			}
		}
		__ignoreItemStateChange = true;
		__ts_JComboBox.add ( "" + (its + 1) + " - " + prop_val + sequence_number );
		__ignoreItemStateChange = false;
	}
	if (nts > 0) {
		// Display the first time series.
		_selected_data = 0;
		displayDataProperties ( _selected_subproduct, _selected_data );
	}
	else {
		_selected_data = -1;
		clearDataProperties();
	}

	__annotation_JComboBox.removeAll();
	int nann = _tsproduct.getNumAnnotations(_selected_subproduct);
	__ignoreItemStateChange = true;
	for (int iann = 0; iann < nann; iann++) {
		prop_val = _tsproduct.getLayeredPropValue("AnnotationID", _selected_subproduct, iann, false, true);
		if (prop_val == null || prop_val.trim().equals("")) {
			prop_val = _tsproduct.getDefaultPropValue( "AnnotationID", _selected_subproduct, iann, true);
		}
		__annotation_JComboBox.add("" + prop_val);
	}
	__ignoreItemStateChange = false;

	if (nann > 0) {
		__selectedAnnotationIndex = 0;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);
	}
	else {
		__selectedAnnotationIndex = -1;
		clearAnnotationProperties();
	}
}

/**
Enables or disables all components related to time series.
@param enabled whether to enable or disable the components.
*/
private void setTimeSeriesFieldsEnabled(boolean enabled) {
	JGUIUtil.setEnabled(__ts_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_graphtype_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_regressionline_JCheckBox, enabled);
	JGUIUtil.setEnabled(_ts_linestyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_linewidth_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_symbolstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_symbolsize_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_flaggedDataSymbolStyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_color_JTextField, enabled);
	JGUIUtil.setEnabled(_ts_color_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_datalabelposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_datalabelformat_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_datalabelformat_JTextField, enabled);
	JGUIUtil.setEnabled(_ts_legendformat_JTextField, enabled);
	JGUIUtil.setEnabled(_ts_legendformat_JComboBox, enabled);
	JGUIUtil.setEnabled(_ts_confidenceinterval_JComboBox, enabled);

	setEditable(_ts_color_JTextField, enabled);
	setEditable(_ts_datalabelformat_JTextField, enabled);
	setEditable(_ts_legendformat_JTextField, enabled);
}

/**
Handle tab selection events.
@param e ChangeEvent to handle.
*/
public void stateChanged ( ChangeEvent e ) {
	// Both the graph and time series tabbed panels have tabs labeled.
	// "Analysis" so need to check the analysis component.
	if ( _graph_lefty_graphtype_JComboBox == null ) {
		// Initializing the interface?
		return;
	}
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase ( _graph_lefty_graphtype_JComboBox.getSelected() );

	Object comp = e.getSource();
	if ( comp == _graph_analysis_JPanel ) {
		if (graphType == TSGraphType.XY_SCATTER
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			// Display the regression analysis panel.
			_blank_analysis_JPanel.setVisible ( false );
			_xyscatter_analysis_JPanel.setVisible ( true );
			_xyscatter_analysis_JPanel.repaint();
		}
		else {
		    // Display the blank analysis panel.
			_xyscatter_analysis_JPanel.setVisible ( false );
			_blank_analysis_JPanel.setVisible ( true );
			_blank_analysis_JPanel.repaint();
		}
	}
	else if ( comp == _ts_analysis_JPanel ) {
		if (graphType == TSGraphType.XY_SCATTER
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			// Display the regression analysis panel.
			_ts_blank_analysis_JPanel.setVisible ( false );
			_ts_xyscatter_analysis_JPanel.setVisible ( true );
			_ts_xyscatter_analysis_JPanel.repaint();
		}
		else {
		    // Display the blank analysis panel.
			_ts_xyscatter_analysis_JPanel.setVisible ( false );
			_ts_blank_analysis_JPanel.setVisible ( true );
			_ts_blank_analysis_JPanel.repaint();
		}
	}
}

/**
Update the TSProduct properties.
Only properties that are different from the original are changed.
It is assumed that _selected_subproduct and _selected_data have been properly set for the properties that are to be saved.
Properties that are not case-sensitive are only updated if truly different
(e.g., titles are compared case-sensitive, but parameter settings are compared case-insensitive).
@return the total number of properties that have been changed.
*/
protected int updateTSProduct () {
	if (_selected_subproduct == -1) {
		return 0;
	}
	return updateTSProduct(Prop.SET_AT_RUNTIME_BY_USER);
}

/**
Update the TSProduct properties.
Only properties that are different from the original are changed.
It is assumed that _selected_subproduct and
_selected_data have been properly set for the properties that are to be saved.
Properties that are not case-sensitive are only updated if truly different
(e.g., titles are compared case-sensitive, but parameter settings are compared case-insensitive).
@param howSet an integer value from Prop that determines how the props will be set (such as SET_AT_RUNTIME_BY_USER).
@return the total number of properties that have been changed.
*/
protected int updateTSProduct (int howSet) {
	int ndirty = 0;
	String prop_val;
	String gui_val;
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet (howSet);

	// Get left and right y-axis graph types, used for some logic below such properties that apply to a graph type.
	TSGraphType graphTypeLeft = TSGraphType.valueOfIgnoreCase(_graph_lefty_graphtype_JComboBox.getSelected());
	TSGraphType graphTypeRight = TSGraphType.valueOfIgnoreCase(_graph_righty_graphtype_JComboBox.getSelected());

	// Alphabetize properties within each of product, subproduct, data.

	// --------------------------------------------------------------------
	// Product properties.
	// --------------------------------------------------------------------

	// "MainTitleFontName"
	prop_val = _tsproduct.getLayeredPropValue("MainTitleFontName", -1, -1, false);
	gui_val = _product_maintitle_fontname_JComboBox.getSelected();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("MainTitleFontName", gui_val, -1, -1);
		++ndirty;
	}

	// "MainTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue("MainTitleFontStyle", -1, -1, false);
	gui_val = _product_maintitle_fontstyle_JComboBox.getSelected();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("MainTitleFontStyle", gui_val, -1, -1);
		++ndirty;
	}

	// "MainTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue("MainTitleFontSize", -1, -1, false);
	gui_val = _product_maintitle_fontsize_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("MainTitleFontSize", gui_val, -1, -1);
		++ndirty;
	}

	// "MainTitleString"

	prop_val = _tsproduct.getLayeredPropValue("MainTitleString", -1, -1, false);
	gui_val = _product_maintitle_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("MainTitleString", gui_val, -1, -1);
		++ndirty;
	}

	// "ProductID"

	prop_val = _tsproduct.getLayeredPropValue("ProductID", -1, -1, false);
	gui_val = __product_id_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("ProductID", gui_val, -1, -1);
		ndirty++;
	}

	// "ProductName"

	prop_val = _tsproduct.getLayeredPropValue("ProductName", -1, -1, false);
	gui_val = __product_name_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("ProductName", gui_val, -1, -1);
		ndirty++;
	}

	// "SubTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue("SubTitleFontName", -1, -1, false);
	gui_val = _product_subtitle_fontname_JComboBox.getSelected();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("SubTitleFontName", gui_val, -1, -1);
		++ndirty;
	}

	// "SubTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue("SubTitleFontStyle", -1, -1, false);
	gui_val = _product_subtitle_fontstyle_JComboBox.getSelected();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("SubTitleFontStyle", gui_val, -1, -1);
		++ndirty;
	}

	// "SubTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue("SubTitleFontSize", -1, -1, false);
	gui_val = _product_subtitle_fontsize_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("SubTitleFontSize", gui_val, -1, -1);
		++ndirty;
	}

	// "SubTitleString"

	prop_val = _tsproduct.getLayeredPropValue("SubTitleString", -1, -1, false);
	gui_val = _product_subtitle_JTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("SubTitleString", gui_val, -1, -1);
		++ndirty;
	}

	// "LayoutType"

	prop_val = _tsproduct.getLayeredPropValue("LayoutType", -1, -1, false);
	gui_val = _layoutTypeJComboBox.getSelected().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("LayoutType", gui_val, -1, -1);
		++ndirty;
	}

	// "LayoutNumberOfRows"

	prop_val = _tsproduct.getLayeredPropValue("LayoutNumberOfRows", -1, -1, false);
	gui_val = _numberRowsJTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		if (StringUtil.isInteger(gui_val)) {
			_tsproduct.setPropValue("LayoutNumberOfRows", gui_val, -1, -1);
			++ndirty;
		}
	}

	// "LayoutNumberOfCols"

	prop_val = _tsproduct.getLayeredPropValue("LayoutNumberOfCols", -1, -1, false);
	gui_val = _numberColsJTextField.getText().trim();
	if (!gui_val.equals(prop_val)) {
		if (StringUtil.isInteger(gui_val)) {
			_tsproduct.setPropValue("LayoutNumberOfCols", gui_val, -1, -1);
			++ndirty;
		}
	}

	// Developer properties.

	// "ShowDrawingAreaOutline"

	prop_val = _tsproduct.getLayeredPropValue("ShowDrawingAreaOutline", -1, -1, false);
	gui_val = _developerShowDrawingAreaOutlineJComboBox.getSelected().trim();
	if (!gui_val.equals(prop_val)) {
		_tsproduct.setPropValue("ShowDrawingAreaOutline", gui_val, -1, -1);
		++ndirty;
	}

	// --------------------------------------------------------------------
	// Subproduct (graph) properties.
	// --------------------------------------------------------------------

	// "BarPosition" - only set if it is a bar graph, otherwise it will get saved in the TSProduct file.

	if (graphTypeLeft == TSGraphType.BAR || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue( "BarPosition", _selected_subproduct, -1, false);
		gui_val = _graph_barposition_JComboBox.getSelected();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("BarPosition", gui_val, _selected_subproduct, -1);
			++ndirty;
		}
	}

	// "BarOverlap" - only set if it is a bar graph, otherwise it will get saved in the TSProduct file.

    if (graphTypeLeft == TSGraphType.BAR || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        prop_val = _tsproduct.getLayeredPropValue( "BarOverlap", _selected_subproduct, -1, false);
        gui_val = _graph_barOverlap_JComboBox.getSelected();
        if (!gui_val.equalsIgnoreCase(prop_val)) {
            _tsproduct.setPropValue("BarOverlap", gui_val, _selected_subproduct, -1);
            ++ndirty;
        }
    }

	prop_val = _tsproduct.getLayeredPropValue( "AnnotationProvider", _selected_subproduct,-1, false);
	if ( __graphAnnotationProvider != null ) {
    	gui_val = __graphAnnotationProvider.getSelected().trim();
    	if (!gui_val.equals(prop_val)) {
    		_tsproduct.setPropValue("AnnotationProvider", gui_val, _selected_subproduct, -1);
    		ndirty++;
    	}
	}

	// "BottomXAxisLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontName", _selected_subproduct, -1, false );
	gui_val=_graph_bottomx_label_fontname_JComboBox.getSelected();
	if (!gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisLabelFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "BottomXAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_bottomx_label_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisLabelFontStyle", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "BottomXAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisLabelFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_bottomx_label_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisLabelFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "BottomXAxisMajorGridColor"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisMajorGridColor", _selected_subproduct, -1, false );
	gui_val = _graph_bottomx_majorgrid_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisMajorGridColor", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "BottomXAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontName", _selected_subproduct, -1, false );
	gui_val=_graph_bottomx_title_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "BottomXAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontStyle", _selected_subproduct, -1, false );
	gui_val= _graph_bottomx_title_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisTitleFontStyle", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "BottomXAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_bottomx_title_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisTitleFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "BottomXAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "BottomXAxisTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_bottomx_title_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "BottomXAxisTitleString", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "DataLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontName", _selected_subproduct, -1, false );
	gui_val = _graph_datalabelfontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "DataLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_datalabelfontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelFontStyle", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "DataLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_datalabelfontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "DataLabelFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFormat", _selected_subproduct, -1, false );
	gui_val = _graph_datalabelformat_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelFormat", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "DataLabelPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelPosition", _selected_subproduct, -1, false );
	gui_val = _graph_datalabelposition_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelPosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LayoutYPercent"
	prop_val = _tsproduct.getLayeredPropValue( "LayoutYPercent", _selected_subproduct, -1, false);
	gui_val = _yPercentJTextField.getText().trim();
	if (prop_val == null && !gui_val.equals("")) {
	    _tsproduct.setPropValue("LayoutYPercent", gui_val, _selected_subproduct, -1);
		++ndirty;
	}
	else if (prop_val != null && gui_val.equals("")) {
		_tsproduct.unSet("SubProduct " + (_selected_subproduct + 1) + ".LayoutYPercent");
		++ndirty;
	}
	else if (!gui_val.equals(prop_val)) {
	    _tsproduct.setPropValue("LayoutYPercent", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "GraphType" (left y-axis).
	prop_val = _tsproduct.getLayeredPropValue ( "GraphType", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_graphtype_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "GraphType", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisDirection"

    prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisDirection", _selected_subproduct, -1, false );
    gui_val = _graph_lefty_direction_JComboBox.getSelected().trim();
    if ( !gui_val.equalsIgnoreCase(prop_val) ) {
        _tsproduct.setPropValue ( "LeftYAxisDirection", gui_val, _selected_subproduct, -1 );
        ++ndirty;
    }

	// "LeftYAxisIgnoreUnits"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisIgnoreUnits", _selected_subproduct, -1, false );
	if ( _graph_lefty_ignoreunits_JCheckBox.isSelected() ) {
		gui_val = "true";
	}
	else {
	    gui_val = "false";
	}
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisIgnoreUnits", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontName", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_label_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisLabelFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_label_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisLabelFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_label_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisLabelFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisLabelPrecision"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLabelPrecision", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_precision_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisLabelPrecision", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisLegendPosition" - replaces legacy "LegendPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisLegendPosition", _selected_subproduct, -1, false );
	if ( prop_val == null ) {
		// See if legacy property is set, to transition to new property.
		prop_val = _tsproduct.getLayeredPropValue ( "LegendPosition", _selected_subproduct, -1, false );
	}
	gui_val = _graph_lefty_legendposition_JComboBox.getSelected().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisLegendPosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisMajorGridColor"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_majorgrid_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisMajorGridColor", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "LeftYAxisMajorTickColor"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorTickColor", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_majortick_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisMajorTickColor", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "LeftYAxisMax"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMax", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_max_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisMax", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisMin"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMin", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_min_JComboBox.getSelected().trim();

	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisMin", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontName", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitleFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitleFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitlePosition"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitlePosition", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_position_JComboBox.getSelected();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitlePosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitleRotation"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleRotation", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_rotation_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitleRotation", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_title_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisTitleString", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisType"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisType", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_type_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisType", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LeftYAxisUnits"
	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisUnits", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_units_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val)) {
		int how_set_prev2 = _tsproduct.getPropList().getHowSet();
		_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
		_tsproduct.setPropValue ( "LeftYAxisUnits", gui_val, _selected_subproduct, -1 );
		_tsproduct.getPropList().setHowSet(how_set_prev2);
		++ndirty;
	}

	// "LegendFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFontName", _selected_subproduct, -1, false );
	gui_val = _graph_legendfontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LegendFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_legendfontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LegendFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_legend_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LegendFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFormat", _selected_subproduct, -1, false );
	gui_val = _graph_legendformat_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFormat", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "LegendPosition" - replaced by newer "LeftYAxisLegendPosition".

	// "MainTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontName", _selected_subproduct, -1, false );
	gui_val = _graph_maintitle_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "MainTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "MainTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_maintitle_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "MainTitleFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "MainTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_maintitle_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "MainTitleFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "MainTitleString"
	prop_val = _tsproduct.getLayeredPropValue ( "MainTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_maintitle_JTextField.getText().trim();
	int pos = _selected_subproduct;
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "MainTitleString", gui_val, _selected_subproduct, -1 );
		// Also update the _graph_JComboBox item.
		_graph_JComboBox.removeItemListener(this);
		_graph_JComboBox.removeAt (pos);
		_graph_JComboBox.insert (("" + (pos+ 1) + " - "+ gui_val), pos);
		_graph_JComboBox.addItemListener(this);
		++ndirty;
	}

	// "RightYAxisBarPosition" - only set if it is a bar graph, otherwise it will get saved in the TSProduct file.

	if (graphTypeRight == TSGraphType.BAR || graphTypeRight == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue( "RightYAxisBarPosition", _selected_subproduct, -1, false);
		gui_val = _graph_righty_barposition_JComboBox.getSelected();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("RightYAxisBarPosition", gui_val, _selected_subproduct, -1);
			++ndirty;
		}
	}

	// "BarOverlap" - only set if it is a bar graph, otherwise it will get saved in the TSProduct file.

    if (graphTypeRight == TSGraphType.BAR || graphTypeRight == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
        prop_val = _tsproduct.getLayeredPropValue( "RightYAxisBarOverlap", _selected_subproduct, -1, false);
        gui_val = _graph_righty_barOverlap_JComboBox.getSelected();
        if (!gui_val.equalsIgnoreCase(prop_val)) {
            _tsproduct.setPropValue("RightYAxisBarOverlap", gui_val, _selected_subproduct, -1);
            ++ndirty;
        }
    }

	// "RightYAxisDirection"

    prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisDirection", _selected_subproduct, -1, false );
    gui_val = _graph_righty_direction_JComboBox.getSelected().trim();
    if ( !gui_val.equalsIgnoreCase(prop_val) ) {
        _tsproduct.setPropValue ( "RightYAxisDirection", gui_val, _selected_subproduct, -1 );
        ++ndirty;
    }

	// "RightYAxisGraphType"
	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisGraphType", _selected_subproduct, -1, false );
	gui_val = _graph_righty_graphtype_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisGraphType", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisIgnoreUnits"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisIgnoreUnits", _selected_subproduct, -1, false );
	if ( _graph_righty_ignoreunits_JCheckBox.isSelected() ) {
		gui_val = "true";
	}
	else {
	    gui_val = "false";
	}
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisIgnoreUnits", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontName", _selected_subproduct, -1, false );
	gui_val = _graph_righty_label_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisLabelFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_righty_label_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisLabelFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_righty_label_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisLabelFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisLabelPrecision"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLabelPrecision", _selected_subproduct, -1, false );
	gui_val = _graph_righty_precision_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisLabelPrecision", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisLegendPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisLegendPosition", _selected_subproduct, -1, false );
	gui_val = _graph_righty_legendposition_JComboBox.getSelected().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisLegendPosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisMajorGridColor"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMajorGridColor", _selected_subproduct, -1, false );
	gui_val = _graph_righty_majorgrid_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisMajorGridColor", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "RightYAxisMajorTickColor"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMajorTickColor", _selected_subproduct, -1, false );
	gui_val = _graph_righty_majortick_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisMajorTickColor", gui_val, _selected_subproduct, -1);
		++ndirty;
	}

	// "RightYAxisMax"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMax", _selected_subproduct, -1, false );
	gui_val = _graph_righty_max_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisMax", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisMin"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisMin", _selected_subproduct, -1, false );
	gui_val = _graph_righty_min_JComboBox.getSelected().trim();

	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisMin", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitlePosition"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitlePosition", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_position_JComboBox.getSelected();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitlePosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleRotation"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleRotation", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_rotation_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleRotation", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleString", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisType"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisType", _selected_subproduct, -1, false );
	gui_val = _graph_righty_type_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisType", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisUnits"
	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisUnits", _selected_subproduct, -1, false );
	gui_val = _graph_righty_units_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val)) {
		int how_set_prev2 = _tsproduct.getPropList().getHowSet();
		_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
		_tsproduct.setPropValue ( "RightYAxisUnits", gui_val, _selected_subproduct, -1 );
		_tsproduct.getPropList().setHowSet(how_set_prev2);
		++ndirty;
	}

	// "LegendFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFontName", _selected_subproduct, -1, false );
	gui_val = _graph_legendfontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "SelectedTimeSeriesLineWidth"

	prop_val = _tsproduct.getLayeredPropValue ( "SelectedTimeSeriesLineWidth", _selected_subproduct, -1, false );
	gui_val = _graphSelectedTimeSeriesLineWidth_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "SelectedTimeSeriesLineWidth", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "SubTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontName", _selected_subproduct, -1, false );
	gui_val = _graph_subtitle_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "SubTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "SubTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontStyle", _selected_subproduct, -1, false );
	gui_val = _graph_subtitle_fontstyle_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "SubTitleFontStyle", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "SubTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleFontSize", _selected_subproduct, -1, false );
	gui_val = _graph_subtitle_fontsize_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "SubTitleFontSize", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "SubTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "SubTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_subtitle_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "SubTitleString", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	if (graphTypeLeft == TSGraphType.XY_SCATTER
	    || graphTypeLeft == TSGraphType.PREDICTED_VALUE
	    || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {

		// "XYScatterAnalyzeForFilling"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterAnalyzeForFilling", _selected_subproduct, -1, false );
		if ( _xyscatter_analysis_fill_JCheckBox.isSelected() ) {
			gui_val = "true";
		}
		else {
		    gui_val = "false";
		}
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterAnalyzeForFilling", gui_val, _selected_subproduct,-1);
			++ndirty;
		}

		// "XYScatterFillPeriodEnd"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterFillPeriodEnd", _selected_subproduct, -1, false );
		gui_val = _xyscatter_analysis_fill_period_end_JTextField.getText().trim();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterFillPeriodEnd", gui_val, _selected_subproduct,-1);
			++ndirty;
		}

		// "XYScatterFillPeriodStart"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterFillPeriodStart", _selected_subproduct, -1, false );
		gui_val = _xyscatter_analysis_fill_period_start_JTextField.getText().trim();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterFillPeriodStart", gui_val, _selected_subproduct,-1);
			++ndirty;
		}

		// "XYScatterIntercept"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterIntercept", _selected_subproduct, -1, false );
		gui_val = _xyscatter_analysis_intercept_JTextField.getText().trim();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterIntercept", gui_val, _selected_subproduct,-1);
			++ndirty;
		}
		// "XYScatterMethod"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatter.Method", _selected_subproduct, -1, false );
		gui_val =_xyscatter_analysis_method_JComboBox.getSelected();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterMethod", gui_val, _selected_subproduct, -1 );
			++ndirty;
		}

		// "XYScatterMonth"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterMonth", _selected_subproduct, -1, false );
		gui_val = _xyscatter_analysis_month_JTextField.getText().trim();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterMonth", gui_val, _selected_subproduct, -1 );
			++ndirty;
		}

		// "XYScatterNumberOfEquations"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", _selected_subproduct,-1,false);
		gui_val = _xyscatter_analysis_neqn_JComboBox.getSelected();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterNumberOfEquations", gui_val, _selected_subproduct, -1 );
			++ndirty;
		}

		// "XYScatterTransformation"

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterTransformation",_selected_subproduct,-1, false);
		gui_val= _xyscatter_analysis_transform_JComboBox.getSelected();
		if ( !gui_val.equalsIgnoreCase(prop_val) ) {
			_tsproduct.setPropValue ( "XYScatterTransformation",gui_val, _selected_subproduct,
			-1 );
			++ndirty;
		}
	}

	// "ZoomEnabled"

	prop_val = _tsproduct.getLayeredPropValue ( "ZoomEnabled", _selected_subproduct, -1, false );
	if ( _graph_zoomenabled_JCheckBox.isSelected() ) {
		gui_val = "true";
	}
	else {
	    gui_val = "false";
	}
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "ZoomEnabled", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "ZoomGroup"

	prop_val = _tsproduct.getLayeredPropValue ( "ZoomGroup", _selected_subproduct, -1, false );
	gui_val = _graph_zoomgroup_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "ZoomGroup", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	if (_selected_data > -1) {

	// --------------------------------------------------------------------
	// Data (time series) properties.
	// --------------------------------------------------------------------

	// "Enabled"

	prop_val = _tsproduct.getLayeredPropValue("Enabled", _selected_subproduct, _selected_data, false);
	if (_ts_enabled_JCheckBox.isSelected()) {
		gui_val = "true";
	}
	else {
		gui_val = "false";
	}
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("Enabled", gui_val, _selected_subproduct, _selected_data);
		++ndirty;
	}

	// "Color"

	prop_val = _tsproduct.getLayeredPropValue ( "Color", _selected_subproduct, _selected_data, false );
	gui_val = _ts_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
//	Message.printStatus(2, "", "Setting Color for " + _selected_subproduct + " / " + _selected_data);
//	Message.printStatus(2, "", "  COLOR: " + gui_val);
		_tsproduct.setPropValue( "Color", gui_val, _selected_subproduct, _selected_data);
		++ndirty;
	}

	// "DataLabelFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelFormat", _selected_subproduct, _selected_data, false);
	gui_val = _ts_datalabelformat_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelFormat", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "DataLabelPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "DataLabelPosition", _selected_subproduct, _selected_data, false );
	gui_val =_ts_datalabelposition_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "DataLabelPosition", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "FlaggedDataSymbolStyle"

    prop_val = _tsproduct.getLayeredPropValue ( "FlaggedDataSymbolStyle", _selected_subproduct, _selected_data, false );
    gui_val = _ts_flaggedDataSymbolStyle_JComboBox.getSelected().trim();
    if ( !gui_val.equalsIgnoreCase(prop_val) ) {
        _tsproduct.setPropValue ( "FlaggedDataSymbolStyle", gui_val, _selected_subproduct, _selected_data );
        ++ndirty;
    }

	// "LegendFormat"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendFormat", _selected_subproduct, _selected_data, false );
	gui_val = _ts_legendformat_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LegendFormat", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "LineStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "LineStyle", _selected_subproduct, _selected_data, false );
	gui_val = _ts_linestyle_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LineStyle", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "LineWidth"

	prop_val = _tsproduct.getLayeredPropValue ( "LineWidth", _selected_subproduct, _selected_data, false );
	gui_val = _ts_linewidth_JComboBox.getSelected().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LineWidth", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "RegressionLineEnabled"

	if (graphTypeLeft == TSGraphType.XY_SCATTER || graphTypeLeft == TSGraphType.PREDICTED_VALUE
	    || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue (
			"RegressionLineEnabled", _selected_subproduct, _selected_data, false );
		if ( _ts_regressionline_JCheckBox.isSelected() ) {
			gui_val = "true";
		}
		else {
			gui_val = "false";
		}
		if ((graphTypeLeft == TSGraphType.XY_SCATTER
		    || graphTypeLeft == TSGraphType.PREDICTED_VALUE
		    || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) &&
			!gui_val.equalsIgnoreCase(prop_val)) {
			// Only save the property if a scatter plot.
			_tsproduct.setPropValue("RegressionLineEnabled", gui_val, _selected_subproduct, _selected_data);
			++ndirty;
		}
	}

	// "SymbolSize"

	prop_val = _tsproduct.getLayeredPropValue ( "SymbolSize", _selected_subproduct, _selected_data, false );
	gui_val = _ts_symbolsize_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "SymbolSize", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "SymbolStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "SymbolStyle", _selected_subproduct, _selected_data, false );
	gui_val = _ts_symbolstyle_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "SymbolStyle", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "XAxis"

	prop_val = _tsproduct.getLayeredPropValue ( "XAxis", _selected_subproduct, _selected_data, false );
	gui_val = _ts_xaxis_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "XAxis", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}

	// "XYScatterConfidenceInterval"

	if (graphTypeLeft == TSGraphType.XY_SCATTER
	    || graphTypeLeft == TSGraphType.PREDICTED_VALUE
	    || graphTypeLeft == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue (
			"XYScatterConfidenceInterval", _selected_subproduct, _selected_data, false);
		gui_val= _ts_confidenceinterval_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("XYScatterConfidenceInterval", gui_val, _selected_subproduct, _selected_data);
			++ndirty;
		}
	}

	// "YAxis"

	prop_val = _tsproduct.getLayeredPropValue ( "YAxis", _selected_subproduct, _selected_data, false );
	gui_val = _ts_yaxis_JComboBox.getSelected().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "YAxis", gui_val, _selected_subproduct, _selected_data );
		++ndirty;
	}
	} // _selected_data > -1

/*
	int num = _tsproduct.getNumAnnotations(_selected_subproduct);
	__ignoreItemStateChange = true;
	__annotation_JComboBox.removeAll();
	for (int i = 0; i < num; i++) {
		__annotation_JComboBox.add("" + _tsproduct.getLayeredPropValue("AnnotationID", _selected_subproduct, i, false, true));
	}
	__ignoreItemStateChange = false;
*/
	if (__selectedAnnotationIndex > -1) {
	// General annotation data.

	prop_val = _tsproduct.getLayeredPropValue("AnnotationID", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_id_JTextField.getText().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("AnnotationID", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		__annotation_JComboBox.insertItemAt(gui_val, __selectedAnnotationIndex);
		__annotation_JComboBox.removeItemAt(__selectedAnnotationIndex + 1);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue("AnnotationTableID", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_tableid_JTextField.getText().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("AnnotationTableID", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
	}

	String shape = null;
	prop_val = _tsproduct.getLayeredPropValue( "ShapeType", _selected_subproduct, __selectedAnnotationIndex, false, true);
	shape = prop_val;
	gui_val = __annotation_ShapeType_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("ShapeType", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
		shape = gui_val;
	}

	// Apply properties based on the annotation shape type.

	if (shape == null || shape.equalsIgnoreCase("Text")) {
		prop_val = _tsproduct.getLayeredPropValue(
			"Color", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"FontSize", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val =__annotation_text_FontSize_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontSize", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"FontStyle", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_FontStyle_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontStyle", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "FontName", _selected_subproduct,
			__selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_FontName_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontName", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "Point", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_PointX_JTextField.getText().trim()
			+ "," + __annotation_text_PointY_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Point", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "Text", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_Text_JTextField.getText().trim();
		if (!gui_val.equals(prop_val)) {
			_tsproduct.setPropValue("Text", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "TextPosition", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_text_Position_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("TextPosition", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}
	}
	else if (shape.equalsIgnoreCase("Line")) {
		prop_val = _tsproduct.getLayeredPropValue( "Color", _selected_subproduct,
			__selectedAnnotationIndex, false, true);
		gui_val = __annotation_line_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"LineStyle", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotationLineStyleJComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("LineStyle", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"LineWidth", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_line_LineWidth_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("LineWidth", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"Points", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_line_PointX1_JTextField.getText().trim()
			+ "," + __annotation_line_PointY1_JTextField.getText().trim()
			+ "," + __annotation_line_PointX2_JTextField.getText().trim()
			+ "," + __annotation_line_PointY2_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Points", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}
	}
	else if (shape.equalsIgnoreCase("Rectangle")) {
		prop_val = _tsproduct.getLayeredPropValue( "Color", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_rectangle_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue("Points", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_rectangle_PointX1_JTextField.getText().trim()
			+ "," + __annotation_rectangle_PointY1_JTextField.getText().trim()
			+ "," + __annotation_rectangle_PointX2_JTextField.getText().trim()
			+ "," + __annotation_rectangle_PointY2_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Points", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}
	}
	else if (shape.equalsIgnoreCase("Symbol")) {
		prop_val = _tsproduct.getLayeredPropValue(
			"Color", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_symbol_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"Point", _selected_subproduct, __selectedAnnotationIndex, false, true);
		gui_val = __annotation_symbol_PointX_JTextField.getText().trim()
			+ "," +__annotation_symbol_PointY_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Point", gui_val,
			_selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "SymbolPosition", _selected_subproduct,
			__selectedAnnotationIndex, false, true);
		gui_val = __annotation_symbol_SymbolPosition_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolPosition", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "SymbolStyle", _selected_subproduct,
			__selectedAnnotationIndex, false, true);
		gui_val = __annotation_symbol_SymbolStyle_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolStyle", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "SymbolSize", _selected_subproduct,
			__selectedAnnotationIndex, false, true);
		gui_val = __annotation_symbol_SymbolSize_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolSize", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
			ndirty++;
		}
	}

	prop_val = _tsproduct.getLayeredPropValue( "Order", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_Order_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("Order", gui_val,
		_selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue(
		"XAxisSystem", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_XAxisSystem_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("XAxisSystem", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue("YAxis", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_YAxis_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("YAxis", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue("YAxisSystem", _selected_subproduct, __selectedAnnotationIndex, false, true);
	gui_val = __annotation_YAxisSystem_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("YAxisSystem", gui_val, _selected_subproduct, __selectedAnnotationIndex, true);
		ndirty++;
	}

	_tsproduct.checkAnnotationProperties(_selected_subproduct, __selectedAnnotationIndex);

	}

	// --------------------------------------------------------------------
	// Return the number of properties that were updated.
	// --------------------------------------------------------------------

	_tsproduct.getPropList().setHowSet ( how_set_prev );
	_ndirty += ndirty;
	return ndirty;
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowActivated(WindowEvent evt) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowClosed(WindowEvent evt) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowClosing(WindowEvent event) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowDeactivated(WindowEvent evt) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowDeiconified(WindowEvent evt) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowOpened(WindowEvent evt) {
}

/**
Does nothing.
TODO (JTS - 2006-05-24) Is a window listener really necessary for this class?
*/
public void windowIconified(WindowEvent evt) {
}

}