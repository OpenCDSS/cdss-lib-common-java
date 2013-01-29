//------------------------------------------------------------------------------
// TSProductFrame - view to display a list of TSProduct properties
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 12 Oct 2000	Steven A. Malers,	Initial version.  Copy TSViewSummaryGUI
//		Riverside Technology,	and modify as necessary.
//		inc.
// 17 Jan 2001	SAM, RTi		Enable Microsoft-like handling of lists.
//					Change GUI to GUIUtil.
// 13 Apr 2001	SAM, RTi		Condense the GUI to fit other graph
//					properties on the dialog.  Do so by
//					using PopUp menus instead of buttons
//					and shorten some lists.  Allow the title
//					to be edited and select the plot type.
// 2001-11-05	SAM, RTi		Clean up javadoc and verify that
//					variables are set to null when no longer
//					used.  Change labels for line and symbol
//					from "Type" to "Style" to more closely
//					match GeoView.
// 2001-12-11	SAM, RTi		Change help key to "TSView.Properties".
// 2002-01-17	SAM, RTi		Change name of class from
//					TSViewPropertiesGUI to
//					TSViewPropertiesFrame.  Phase out use
//					of TS.setPlot*() methods.  Plot
//					properties are now stored in a TSProduct
//					object so convert constructor to use
//					TSProduct.
// 2002-01-27	SAM, RTi		Rework this interface into a group of
//					tabbed panels corresponding to TSProduct
//					parts.  Pass in a TSGraphCanvas, which
//					has a TSProduct and display properties,
//					so the properties can be fully viewed
//					and manipulated.
// 2002-03-06	SAM, RTi		Fix subproduct (graph) and data (TS)
//					choices to have a length limit.
// 2002-04-25	SAM, RTi		Fix problem where RegressionLineEnabled
//					property was getting saved even when
//					not an XY Scatter plot.  Add
//					LegendPosition property.  Add analysis
//					tab.
// 2002-05-19	SAM, RTi		Add to the Analysis tab for the
//					XY-Scatter graph.  Add a Label tab for
//					the graph and time series for plotting
//					a label at each data point (see the
//					DataLabel* properties).  Take the label
//					out of the TS Symbol tab.  Keeping the
//					label separate from the symbol is
//					consistent with the GeoView GIS
//					approach (even if not quite the same).
// 2002-05-23	SAM, RTi		Add to the Analysis tab for the time
//					series propertes and move the curve fit
//					line property "RegressionLineEnabled" to
//					the new tab.
// ==================================
// 2002-11-11	SAM, RTi		Copy AWT version and update to use
//					Swing.
// 2003-06-04	SAM, RTi		* Update code for final conversion to
//					  Swing, using new GR and TS packages.
//					* Update for recent change in AWT
//					  version:
//					2003-05-14	SAM, RTi
//					* Add support for the XYScatterIntercept
//					  property.
//					* Add support for the
//					  XYScatterAnalyzeForFilling property.
// 2003-09-03	J. Thomas Sapienza, RTi	* Corrected bug that was caused by 
//					  SimpleJComboBox add() methods chaining
//					  down to itemStateChanged() before the
//					  GUI was fully set up.  Added the 
//					  __is_initialized boolean.
// 2003-09-30	SAM, RTi		* Use the icon/title from the main
//					  application.
// 2003-12-04	SAM, RTi		* Phase out SimpleJComboBox and use
//					  SimpleJComboBox instead.
// 2004-02-24	JTS, RTi		Added code to handle addition of the
//					layout preview window, including:
//					* addGraph()
//					* getSelectedGraph()
//					* clearAllProperties()
//					* clearDataProperties()
//					* clearProductProperties()
//					* clearSubProductProperties()
//					* getGraphList()
//					* getTSViewJFrame()
//					* redisplayProperties()
//					* setSelectedGraph()
//					* setSelectedSubproductAndData()
// 2004-03-02	JTS, RTi		Added code for drag and drop, including:
//					* All the DragAndDropListener methods
//					* addData()
//					* moveSelectedData()
// 2004-03-11	JTS, RTi		clear*Properties() and addData() now
//					default the property values by using
//					TSProduct.getDefaultPropValue().
// 2004-04-20	JTS, RTi		* Added the annotation panel.
//					* Added clearAnnotationProperties().
//					* Added createAnnotationJPanel().
//					* Added displayAnnotationProperties().
//					* Updated itemStateChanged() to respond
//					  to annotation events.
//					* Updates updateTSProduct() to update
//					  annotation properties.
// 2004-04-21	JTS, RTi		* Added addAnnotation().
//					* Added code to delete an annotation.
//					* Added code to change the order of
//					  annotations in the tsp file.
// 2004-04-23	SAM, RTi		* Rename class from
//					  TSViewPropertiesJFrame to
//					  TSProductJFrame.
//					* Rename annotation components to be
//					  more unique names, consistent with
//					  other data members.
// 2004-04-26	JTS, RTi		* Added buttons for adding, deleting 
//					  and moving annotations.
//					* Added checkGUIState() to set button
//					  states properly.
//					* Annotation colors are now specified
//					  inside the specific shape box.
// 2004-05-03	JTS, RTi		* Added Product ID to the General 
//					  Product Properties tab.
//					* Added code to handle the ProductID
//					  property.
//					* Added checkUserInput().
// 2004-05-04	JTS, RTi		* Added setAnnotationFieldsEnabled().
//					* Added setTimeSeriesFieldsEnabled().
// 2004-05-11	JTS, RTi		* Added setGraphFieldsEnabled().
//					* Commented out the Cancel button.
//					* Added support for having 0 graphs.
// 2004-05-13	JTS, RTi		Added code to handle the ProductName
//					property.
// 2004-05-24	SAM, RTi		Add tool tips to the graph analysis
//					components.
// 2004-05-25	JTS, RTi		* Added clearGraphProperties().
//					* Added limitGraphTypes().
// 2004-07-26	JTS, RTi		* Changed the size of the product name
//					  text field.
//					* Added applyClicked().	
// 2005-04-20	JTS, RTi		Changed the LeftYAxisMin/Max textfields
//					to be combo boxes.
// 2005-04-22	JTS, RTi		Added code so that the GUI could be
//					opened in a non-visible state.
// 2005-04-27	JTS, RTi		Added all data members to finalize().
// 2005-04-29	JTS, RTi		* Added support for Point graphs.
// 					* Graph settings are now enabled or
//					  disabled properly when the graph 
//					  type is changed.
// 2005-05-05	JTS, RTi		* Added support for Predicted Value 
//					  graphs.
//					* Added support for Predicted Value
//					  Residual graphs.
//					* Time series can now be disabled so
//					  they are not considered to be on
//					  the graph.
// 2005-05-06	JTS, RTi		Added support for dashed lines.
// 2005-06-09	JTS, RTi		LegendPosition now includes "None",
//					"InsideUpperLeft", "InsideUpperRight",
//					"InsideLowerLeft", "InsideLowerRight"
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------

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

import java.util.List;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
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
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Time.TimeUtil;

/**
The TSProductJFrame displays properties for a TSProduct, specifically as
used by the TSViewJComponent.
Currently this is only available from the TSViewGraphJFrame and lets the user
view and modify graph TSProduct properties.  An attempt is made to track when
a property changes from the previous value to avoid redraws and check
user-modified properties (need to enhance PropList to track status of whether a
property is read from a file/database, defaulted at run-time, or set by the
user).  The layout of the interface is as follows:
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
| | Also have summary and table properties later in an interchangable  | |
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
	createDataJPanel()</ol>
<li>	Display properties for the project in the available controls using:
	displayProductProperties(), displaySubproductProperties(),
	displayDataProperties().</li>
<li>	Interface event handling.</li>
<li>	When applying or saving, transfer the properties in the controls using
	updateTSProduct().</li>
</ol>
*/
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
Whether the GUI is ALMOST fully initialized or not yet.  If the GUI is not yet fully
initialized, then any calls to a SimpleJComboBox add() method will result in 
the ItemListener's itemStateChanged() method being called.  This was throwing
null-pointer exceptions because itemStateChanged() depends on the GUI being
fully initialized.  If __is_initialized is false, itemStateChanged() will 
return without doing anything.
*/
private boolean __is_initialized = false;

/**
Whether some basic setup has been completed and graph types can be limited
in the combo box now or not.  
TODO (JTS - 2004-05-25) see if can use __is_initialized instead
*/
private boolean __limitGraphTypes = false;

// Shared data..

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
The number of dirty properties.  This is incremented as properties are
set with calls to updateTSProduct() and is cleared if the display is updated.
Properties need to be updated if subproducts or time series
are selected - otherwise only the last changes shown will be saved.
*/
private int _ndirty = 0;

// The following panels are listed in the general order of the interface.

// Product panel

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

// Subproduct (Graph) panel

private SimpleJComboBox _graph_JComboBox = null;
private JTabbedPane _graph_JTabbedPane = null;
private JCheckBox _graph_enabled_JCheckBox = null;
private JCheckBox _graph_isref_JCheckBox = null;
private SimpleJComboBox _graph_graphtype_JComboBox = null;
private JLabel _graph_barposition_JLabel = null;
private SimpleJComboBox _graph_barposition_JComboBox = null;
private JLabel _graph_barOverlap_JLabel = null;
private SimpleJComboBox _graph_barOverlap_JComboBox = null;

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

private SimpleJComboBox _graph_righty_label_fontname_JComboBox = null;
private SimpleJComboBox _graph_righty_label_fontstyle_JComboBox = null;
private JTextField _graph_righty_label_fontsize_JTextField = null;

private JTextField _graph_lefty_majorgrid_color_JTextField = null;
private SimpleJComboBox _graph_lefty_majorgrid_color_JComboBox = null;
private SimpleJButton _graph_lefty_majorgrid_color_JButton = null;

private JTextField _graph_righty_title_JTextField = null;
private SimpleJComboBox _graph_righty_title_fontname_JComboBox = null;
private SimpleJComboBox _graph_righty_title_fontstyle_JComboBox = null;
private JTextField _graph_righty_title_fontsize_JTextField = null;

private JTextField _graph_datalabelformat_JTextField = null;
private SimpleJComboBox _graph_datalabelformat_JComboBox = null;
private SimpleJComboBox _graph_datalabelposition_JComboBox = null;
private SimpleJComboBox _graph_datalabelfontname_JComboBox = null;
private SimpleJComboBox _graph_datalabelfontstyle_JComboBox = null;
private JTextField _graph_datalabelfontsize_JTextField = null;

private JTextField _graph_legendformat_JTextField = null;
private SimpleJComboBox _graph_legendformat_JComboBox = null;
private SimpleJComboBox _graph_legendposition_JComboBox = null;
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
private int __selectedAnnotation = -1;	// The currently-selected annotation.
private JTextField __annotation_id_JTextField = null;
private SimpleJComboBox __annotation_ShapeType_JComboBox = null;
private SimpleJComboBox __annotation_Order_JComboBox = null;
private JTextField __annotation_line_color_JTextField = null;
private JTextField __annotation_text_color_JTextField = null;
private SimpleJComboBox __annotation_text_color_JComboBox = null;
private SimpleJComboBox __annotation_line_color_JComboBox = null;
private SimpleJComboBox __annotation_XAxisSystem_JComboBox = null;
private SimpleJComboBox __annotation_YAxisSystem_JComboBox = null;

private JPanel __annotation_line_JPanel = null;
private SimpleJComboBox __lineStyleJComboBox = null;
private JTextField __annotation_line_LineWidth_JTextField = null;
private JTextField __annotation_line_PointX1_JTextField = null;
private JTextField __annotation_line_PointY1_JTextField = null;
private JTextField __annotation_line_PointX2_JTextField = null;
private JTextField __annotation_line_PointY2_JTextField = null;

private JPanel __annotation_text_JPanel = null;
private JTextField __annotation_text_Text_JTextField = null;
private JTextField __annotation_text_PointX_JTextField = null;
private JTextField __annotation_text_PointY_JTextField = null;
private SimpleJComboBox __annotation_text_Position_JComboBox = null;
private SimpleJComboBox __annotation_text_FontName_JComboBox = null;
private SimpleJComboBox __annotation_text_FontStyle_JComboBox = null;
private JTextField __annotation_text_FontSize_JTextField = null;

private JPanel __annotation_symbol_JPanel = null;
private JTextField __annotation_symbol_color_JTextField = null;
private SimpleJComboBox __annotation_symbol_color_JComboBox = null;
private JTextField __annotation_symbol_PointX_JTextField = null;
private JTextField __annotation_symbol_PointY_JTextField = null;
private SimpleJComboBox __annotation_symbol_SymbolPosition_JComboBox = null;
private SimpleJComboBox __annotation_symbol_SymbolStyle_JComboBox = null;
private SimpleJComboBox __annotation_symbol_SymbolSize_JComboBox = null;

// Data (Time Series) JPanel

private DragAndDropSimpleJComboBox __ts_JComboBox = null;
private JTabbedPane _ts_JTabbedPane = null;

private JCheckBox _ts_enabled_JCheckBox = null;

private SimpleJComboBox _ts_graphtype_JComboBox = null;	// Enable later
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
Whether to ignore a state change when certain modifications are made to data
in a combo box.
*/
private boolean __ignoreItemStateChange = false;

////////////////////////////////////////////
// Layout GUI items
/**
The text field that determines the percentage of Y area that each graph should take up.
*/
protected JTextField _yPercentJTextField = null;

/**
The combo box that defines how the graphs are layed out.
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
Combo boxes for choosing the annotation providers for the graphs.
*/
private SimpleJComboBox __graphAnnotationProvider = null;

/**
The annotation providers that are available to be used in the product.
*/
private List __annotationProviders = null;

public final static String NO_GRAPHS_DEFINED = "No Graphs Defined";

/**
Construct a TSProductJFrame.
@param tsview_gui The main TSViewJFrame that manages the view windows.
@param tsgraphcanvas TSGraphJComponent associated with the properties (can get
the list of time series and TSProduct from this object).
*/
public TSProductJFrame (TSViewJFrame tsview_gui, TSGraphJComponent tsgraphcanvas)
throws Exception {	
	this(tsview_gui, tsgraphcanvas, true);
}

/**
Construct a TSProductJFrame.
@param tsview_gui The main TSViewJFrame that manages the view windows.
@param tsgraphcanvas TSGraphJComponent associated with the properties (can get
the list of time series and TSProduct from this object).
@param visible whether this GUI is to be visible once created.
*/
public TSProductJFrame (TSViewJFrame tsview_gui, TSGraphJComponent tsgraphcanvas, boolean visible)
throws Exception {
	super("Time Series - Graph Properties");
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
	initialize(tsview_gui, tsgraphcanvas, visible);
	__graphJFrame = getTSViewJFrame().getViewGraphJFrame();
}

/**
Handle action events (button press, etc.)
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e )
{	checkGUIState();
	// Check the names of the events.  These are tied to menu names.

	String command = e.getActionCommand();
	if ( command.equals("TSProductJFrame.Apply") ) {
		applyClicked();
	}
	else if ( command.equals("TSProductJFrame.Cancel") ) {
		// Close the GUI via the parent...
		_tsview_gui.closeGUI(TSViewType.PROPERTIES);
	}
	else if ( command.equals("TSProductJFrame.Close") ) {
		if (!checkUserInput()) {	
			return;
		}
		if ( updateTSProduct () > 0 ) {
			// Cause a redraw of the displays...
			_tsview_gui.refresh ();
		}
		// Close the GUI via the parent...
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
		if (__selectedAnnotation == -1) {
			return;
		}
		int num = _tsproduct.getNumAnnotations(_selected_subproduct);
		_tsproduct.removeAnnotation(_selected_subproduct, __selectedAnnotation);

		__ignoreItemStateChange = true;
		__annotation_JComboBox.removeAll();
		for (int i = 0; i < num - 1; i++) {
			__annotation_JComboBox.add("" + _tsproduct.getLayeredPropValue("AnnotationID",
				_selected_subproduct, i, false, true));		
		}
		__ignoreItemStateChange = false;
		__selectedAnnotation = 0;
		if (num > 1) {
			displayAnnotationProperties(_selected_subproduct, 
				__selectedAnnotation);		
		} 
		else {
			clearAnnotationProperties();
			__selectedAnnotation = -1;
		}
	}
	else if (command.equals(__BUTTON_ANNOTATION_UP)) {
		updateTSProduct();

		if (__selectedAnnotation == -1 || __selectedAnnotation == 0) {
			return;
		}

		_tsproduct.swapAnnotations(_selected_subproduct, __selectedAnnotation, _selected_subproduct,
			__selectedAnnotation - 1);
		__ignoreItemStateChange = true;

		String s = (String)__annotation_JComboBox.getItemAt(__selectedAnnotation);
		__annotation_JComboBox.removeItemAt(__selectedAnnotation);
		__annotation_JComboBox.insertItemAt(s,__selectedAnnotation - 1);
		__annotation_JComboBox.select(__selectedAnnotation - 1);
		__ignoreItemStateChange = false;
		__selectedAnnotation--;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotation);
	}
	else if (command.equals(__BUTTON_ANNOTATION_DOWN)) {
		updateTSProduct();

		int num = _tsproduct.getNumAnnotations(_selected_subproduct);

		if ( (__selectedAnnotation == -1) || (__selectedAnnotation == (num - 1)) ) {
			return;
		}
		_tsproduct.swapAnnotations(_selected_subproduct, 
			__selectedAnnotation, _selected_subproduct, __selectedAnnotation + 1);
		__ignoreItemStateChange = true;
		String s = (String)__annotation_JComboBox.getItemAt(__selectedAnnotation);		
		__annotation_JComboBox.removeItemAt(__selectedAnnotation);
		__annotation_JComboBox.insertItemAt(s,__selectedAnnotation + 1);
		__annotation_JComboBox.select(__selectedAnnotation + 1);
		__selectedAnnotation++;		
		__ignoreItemStateChange = false;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotation);
	}
/* TODO SAM - need to update to new
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
		// Find time series in the enabled list
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
@param pos the number that the subproduct should have in the product proplist.
The other properties will be renumbered appropriately.
*/
protected void addGraph(int pos) {
	int nsub = _tsproduct.getNumSubProducts();
	for (int i = (nsub - 1); i >= pos; i--) {
		_tsproduct.swapSubProducts(i, i + 1);
	}
	// a few default settings that all graphs need
	_tsproduct.getPropList().setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);

	if (pos == -1) {
		// adding a new graph to a display with no graphs defined
		pos++;
	}
	
	_selected_subproduct = pos;
	_selected_data = -1;
	_tsproduct.setDirty(true);
	__selectedAnnotation = -1;
	_tsproduct.setPropValue("Enabled", "true", pos, -1);
	_tsproduct.setPropValue("OriginalGraphType", "line", pos, -1);
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
	// Update displayed properties to the TSProduct...
	updateTSProduct();
	if ( _ndirty > 0 ) {
		// Cause a redraw of the displays...
		_tsview_gui.refresh ();
		_ndirty = 0;
	}
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
Checks the state of the GUI and enables or disables buttons appropriately.
*/
public void checkGUIState() {
	if (__selectedAnnotation == -1 || _selected_subproduct == -1) {
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

	if (__selectedAnnotation == 0 || __selectedAnnotation == -1) {
		JGUIUtil.setEnabled(__moveAnnotationUpJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__moveAnnotationUpJButton, true);
	}

	if (__selectedAnnotation == -1 || __selectedAnnotation 
		== (_tsproduct.getNumAnnotations(_selected_subproduct) - 1)) {
		JGUIUtil.setEnabled(__moveAnnotationDownJButton, false);
	}
	else {
		JGUIUtil.setEnabled(__moveAnnotationDownJButton, true);
	}

	// check to see if there are any data defined in the data combo box.
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

	// check to see if there are any annotations in the annotation
	// combo box.  If so, enable all the annotation entry fields.  If not,
	// disable everything but the add button.
 
	if (__annotation_JComboBox == null || __annotation_JComboBox.getItemCount() == 0) {
		setAnnotationFieldsEnabled(false);
	}
	else {
		setAnnotationFieldsEnabled(true);
	}	

	String graphTypeString = _graph_graphtype_JComboBox.getSelected();
	if (graphTypeString == null) {
		return;
	}
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase(graphTypeString);
	if ( graphType == TSGraphType.AREA || graphType == TSGraphType.AREA_STACKED ||
	    graphType == TSGraphType.BAR || graphType == TSGraphType.LINE ||
	    graphType == TSGraphType.POINT) {
	    _graph_graphtype_JComboBox.setEnabled(true);
	}
	else {
		_graph_graphtype_JComboBox.setEnabled(false);
	}	

	enableComponentsBasedOnGraphType(_selected_subproduct, _selected_data, false);
}

/**
Checks whether user entered values are valid.  
@return true if all the user-entered values valid, false if not.
*/
public boolean checkUserInput() {
	String routine = "TSProductJFrame.checkUserInput";
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
Clears out the properties set in the GUI objects.  This is necessary for
instances when all graphs are removed from the GUI or when a graph has no TSIDs.
*/
private void clearAllProperties() {
	clearProductProperties();
	clearSubProductProperties();
	clearDataProperties();
	clearAnnotationProperties();
	checkGUIState();
}

/**
Clears out the annotation properties set in the GUI objects to the defaults,
as defined in TSProduct.  Necessary for instances when all graphs are removed
from the GUI or a graph has no annotations.
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

	__annotation_Order_JComboBox.select(_tsproduct.getDefaultPropValue("Order", 1, 1, true));

	__annotation_ShapeType_JComboBox.select(0);

	__annotation_XAxisSystem_JComboBox.select(_tsproduct.getDefaultPropValue("XAxisSystem", 1, 1, true));

	__annotation_YAxisSystem_JComboBox.select(_tsproduct.getDefaultPropValue("YAxisSystem", 1, 1, true));

	__lineStyleJComboBox.select(_tsproduct.getDefaultPropValue("LineStyle", 1, 1, true));

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
Clears out the data properties set in the GUI objects to the defaults, as
defined in TSProduct.  Necessary for cases such as when all graphs are removed from the product.
*/
private void clearDataProperties() {
	// data properties
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
Clears out graph properties that are not appropriate for the 
currently-selected graph.  It does this in order that graphs have only their
own proper properties when written to persistent storage.  This method is only
important if the user changes graph types.
@param subproduct the subproduct (base-zero) for which to clear out the other
graph properties than for the one that is selected.
@param ts unused
@param graphType the kind of graph that the user selected from the graph type combo box.
*/
private void clearGraphProperties(int subproduct, int ts, TSGraphType graphType) {
	String prefix = "SubProduct " + (subproduct + 1) + ".";
	if (graphType != TSGraphType.BAR && graphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		_tsproduct.unSet(prefix + "BarPosition");
		_tsproduct.unSet(prefix + "BarOverlap");
	}
	else {
		_tsproduct.unSet(prefix + "LineStyle");
		_tsproduct.unSet(prefix + "LineWidth");
		_tsproduct.unSet(prefix + "SymbolSize");
		_tsproduct.unSet(prefix + "SymbolStyle");
		_tsproduct.unSet(prefix + "FlaggedDataSymbolStyle");
	}

	if (graphType == TSGraphType.POINT) {
		_tsproduct.unSet(prefix + "LineStyle");
		_tsproduct.unSet(prefix + "LineWidth");
	}

	if (graphType != TSGraphType.XY_SCATTER
	    && graphType != TSGraphType.PREDICTED_VALUE
	    && graphType != TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		_tsproduct.unSet(prefix + "XYScatterAnalyzeForFilling");
		_tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodEnd");
		_tsproduct.unSet(prefix + "XYScatterDependentAnalysisPeriodStart");
		_tsproduct.unSet(prefix + "XYScatterFillPeriodEnd");
		_tsproduct.unSet(prefix + "XYScatterFillPeriodStart");
		_tsproduct.unSet(prefix + "XYScatterIndependentAnalysisPeriodEnd");
		_tsproduct.unSet(prefix	+ "XYScatterIndependentAnalsysisPeriodStart");
		_tsproduct.unSet(prefix + "XYScatterIntercept");
		_tsproduct.unSet(prefix + "XYScatterMethod");
		_tsproduct.unSet(prefix + "XYScatterMonth");
		_tsproduct.unSet(prefix + "XYScatterNumberOfEquations");
		_tsproduct.unSet(prefix + "XYScatterTransformation");
		_tsproduct.unSet(prefix + "XYScatterConfidenceInterval");
		_tsproduct.unSet(prefix + "RegressionLineEnabled");
	}
}

/**
Clears out the product properties set in the GUI objects.
*/
private void clearProductProperties() {
	// product properties
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
	String graphType = _tsproduct.getDefaultPropValue("GraphType", 1, -1);
	_graph_graphtype_JComboBox.select(graphType);

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

	String colorString = _tsproduct.getDefaultPropValue( "BottomXAxisMajorGridColor", 1, -1);
	GRColor color = GRColor.parseColor(colorString);
	_graph_bottomx_majorgrid_color_JTextField.setText(colorString);
	_graph_bottomx_majorgrid_color_JComboBox.select(colorString);
	_graph_bottomx_majorgrid_color_JTextField.setBackground(color);

	_graph_bottomx_title_JTextField.setText(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleString", 1,-1));

	_graph_bottomx_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleFontName", 1, -1));

	_graph_bottomx_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleFontStyle", 1, -1));

	_graph_bottomx_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleFontSize", 1, -1));

	_graph_bottomx_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontName", 1, -1));

	_graph_bottomx_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontStyle", 1, -1));
		
	_graph_bottomx_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontSize", 1, -1));

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

	_graph_lefty_title_JTextField.setText( _tsproduct.getDefaultPropValue("LeftYAxisTitleString", 1, -1));

	_graph_lefty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName", 1, -1));

	_graph_lefty_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontStyle", 1, -1));

	_graph_lefty_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontSize", 1, -1));

	if ( __graphAnnotationProvider != null ) {
	    __graphAnnotationProvider.select( _tsproduct.getDefaultPropValue("AnnotationProvider", 1, -1));
	}

	_graph_lefty_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontName", 1, -1));

	_graph_lefty_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontStyle", 1, -1));

	_graph_lefty_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontSize", 1, -1));

	if (_tsproduct.getDefaultPropValue("LeftYAxisIgnoreUnits", 1, -1).equals("False")) {
		_graph_lefty_ignoreunits_JCheckBox.setSelected(false);
	}
	else {
		_graph_lefty_ignoreunits_JCheckBox.setSelected(true);
	}

	_graph_lefty_max_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisMax", 1, -1));

	_graph_lefty_min_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisMin", 1, -1));

	_graph_lefty_type_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisType", 1, -1));

	_graph_lefty_units_JTextField.setText( _tsproduct.getDefaultPropValue("LeftYAxisUnits", 1, -1));
	
	_graph_lefty_direction_JComboBox.select( _tsproduct.getDefaultPropValue("LeftYAxisDirection", 1, -1));

	_graph_legendfontname_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontName", 1, -1));

	_graph_legendfontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontStyle", 1, -1));

	_graph_legend_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("LegendFontSize", 1, -1));

	_graph_legendformat_JTextField.setText( _tsproduct.getDefaultPropValue("LegendFormat", 1, -1));

	_graph_legendposition_JComboBox.select( _tsproduct.getDefaultPropValue("LegendPosition", 1, -1));

	_graph_maintitle_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleString", 1, -1));

	_graph_maintitle_fontname_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontName", 1, -1));

	_graph_maintitle_fontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("MainTitleFontStyle", 1, -1));

	_graph_maintitle_fontsize_JTextField.setText( _tsproduct.getDefaultPropValue("MainTitleFontSize", 1, -1));

/*
	_graph_righty_title_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisTitleString", 1, -1));
	_graph_righty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisFontName", 1, -1));

	_graph_righty_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisFontStyle", 1, -1));

	_graph_righty_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisFontSize", 1, -1));

	_graph_righty_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisFontName", 1, -1));

	_graph_righty_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontStyle", 1, -1));

	_graph_righty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisTitleFontName", 1, -1));
	_graph_righty_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisTitleFontSize", 1, -1));		
	_graph_righty_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisTitleFontStyle", 1, -1));		
	_graph_righty_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontName", 1, -1));
	_graph_righty_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontSize", 1, -1));		
	_graph_righty_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontStyle", 1, -1));		
*/
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
	
	JGUIUtil.addComponent(annotationJPanel, new JLabel("Annotation properties:"),
		0, 0, 1, 1, 1, 1,
		_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);

	__annotation_JComboBox = new SimpleJComboBox(false);

	// Initialized to first annotation of first product...
	int nann = _tsproduct.getNumAnnotations(0);
	for (int iann = 0; iann < nann; iann++) {
		__annotation_JComboBox.add("" + _tsproduct.getLayeredPropValue("AnnotationID", 0, iann, true));
	}

	__addAnnotationJButton = new JButton(__BUTTON_ADD_ANNOTATION);
	__addAnnotationJButton.addActionListener(this);
	__addAnnotationJButton.setToolTipText("Add new annotation.");
	__delAnnotationJButton = new JButton(__BUTTON_DEL_ANNOTATION);
	__delAnnotationJButton.addActionListener(this);
	__delAnnotationJButton.setToolTipText("Delete current annotation.");
	__moveAnnotationUpJButton = new JButton(__BUTTON_ANNOTATION_UP);
	__moveAnnotationUpJButton.addActionListener(this);
	__moveAnnotationUpJButton.setToolTipText("Move annotation up one position.");
	__moveAnnotationDownJButton = new JButton(__BUTTON_ANNOTATION_DOWN);
	__moveAnnotationDownJButton.addActionListener(this);
	__moveAnnotationDownJButton.setToolTipText("Move annotation down one position.");

	int y = 0;

	JGUIUtil.addComponent(annotationJPanel, __addAnnotationJButton,
		4, y, 1, 1, 1, 1, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __delAnnotationJButton,
		5, y, 1, 1, 1, 1, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __moveAnnotationUpJButton,
		6, y, 1, 1, 1, 1, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __moveAnnotationDownJButton,
		7, y, 1, 1, 1, 1, 
		GridBagConstraints.NONE, GridBagConstraints.CENTER);

	__annotation_JComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
	JGUIUtil.addComponent(annotationJPanel, __annotation_JComboBox,
		1, y, 3, 1, 0, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.NORTH);
	y++;
	
	__annotation_id_JTextField = new JTextField(15);
	JGUIUtil.addComponent(annotationJPanel, new JLabel("Annotation ID: "),
		0, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(annotationJPanel, __annotation_id_JTextField,
		1, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(annotationJPanel, new JLabel("Shape type: "),
		0, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_ShapeType_JComboBox = new SimpleJComboBox(false);
	__annotation_ShapeType_JComboBox.add("Text");
	__annotation_ShapeType_JComboBox.add("Line");
	__annotation_ShapeType_JComboBox.add("Symbol");
	JGUIUtil.addComponent(annotationJPanel,__annotation_ShapeType_JComboBox,
		1, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(annotationJPanel, new JLabel("Order: " ),
		0, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_Order_JComboBox = new SimpleJComboBox(false);
	__annotation_Order_JComboBox.add("OnTopOfData");
	__annotation_Order_JComboBox.add("BehindData");
	__annotation_Order_JComboBox.select(0);
	__annotation_Order_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationJPanel, __annotation_Order_JComboBox,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(annotationJPanel, new JLabel("X axis system: "),
		0, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_XAxisSystem_JComboBox = new SimpleJComboBox(false);
	__annotation_XAxisSystem_JComboBox.add("Data");
	__annotation_XAxisSystem_JComboBox.add("Percent");
	__annotation_XAxisSystem_JComboBox.select(0);
	__annotation_XAxisSystem_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationJPanel, __annotation_XAxisSystem_JComboBox, 
		1, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(annotationJPanel, new JLabel("Y axis system: "),
		0, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	__annotation_YAxisSystem_JComboBox = new SimpleJComboBox(false);
	__annotation_YAxisSystem_JComboBox.add("Data");
	__annotation_YAxisSystem_JComboBox.add("Percent");
	__annotation_YAxisSystem_JComboBox.select(0);
	__annotation_YAxisSystem_JComboBox.addItemListener(this);
	JGUIUtil.addComponent(annotationJPanel, __annotation_YAxisSystem_JComboBox, 
		1, y, 1, 1, 1, 1, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	y++;
	__graphAnnotationProvider = new SimpleJComboBox(__annotationProviders);
	__graphAnnotationProvider.setPrototypeDisplayValue(	"XXXXXXXXXXXXXXXXXXXX");
	JGUIUtil.addComponent(annotationJPanel,	new JLabel("Annotation provider: "),
		0, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,	GridBagConstraints.WEST);	
	JGUIUtil.addComponent(annotationJPanel,	__graphAnnotationProvider,
		1, y, 1, 1, 1, 1, _insetsTLBR, GridBagConstraints.NONE,
		GridBagConstraints.WEST);
	
	__annotation_ShapeType_JComboBox.addItemListener(this);
	__annotation_ShapeType_JComboBox.select(0);

	__annotation_JComboBox.addItemListener(this);

	__annotation_line_JPanel = new JPanel();
	__annotation_line_JPanel.setLayout(new GridBagLayout());
	__annotation_line_PointX1_JTextField = new JTextField(5);
	__annotation_line_PointY1_JTextField = new JTextField(5);
	__annotation_line_PointX2_JTextField = new JTextField(5);
	__annotation_line_PointY2_JTextField = new JTextField(5);
	y = 0;
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("X1: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		__annotation_line_PointX1_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("Y1: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		__annotation_line_PointY1_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("X2: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		__annotation_line_PointX2_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_line_JPanel, new JLabel("Y2: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		__annotation_line_PointY2_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_line_LineWidth_JTextField = new JTextField(5);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		new JLabel("Line Width: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		__annotation_line_LineWidth_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	
	__lineStyleJComboBox = new SimpleJComboBox(false);
	__lineStyleJComboBox.add("Solid");
	__lineStyleJComboBox.add("None");
	__lineStyleJComboBox.select(0);
	y++;
	JGUIUtil.addComponent(__annotation_line_JPanel, 
		new JLabel("Line Style: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_line_JPanel, __lineStyleJComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	
	__annotation_line_color_JComboBox = new SimpleJComboBox(false);
	int size = GRColor.COLOR_NAMES.length;
	for (int i = 0; i < size; i++) {
		__annotation_line_color_JComboBox.addItem(GRColor.COLOR_NAMES[i]);
	}
	__annotation_line_color_JComboBox.setMaximumRowCount(__annotation_line_color_JComboBox.getItemCount());
	__annotation_line_color_JComboBox.select(1);
	__annotation_line_color_JComboBox.addItemListener(this);
	__annotation_line_color_JTextField = new JTextField(10);
	__annotation_line_color_JTextField.setEditable(false);
	__annotation_line_color_JTextField.setBackground(GRColor.parseColor(GRColor.COLOR_NAMES[1]));

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

	__annotation_line_JPanel.setBorder(BorderFactory.createTitledBorder("Line"));
	
	__annotation_text_JPanel = new JPanel();
	__annotation_text_JPanel.setLayout(new GridBagLayout());
	
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
	__annotation_text_PointX_JTextField = new JTextField(5);
	__annotation_text_PointY_JTextField = new JTextField(5);
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("X: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, 
		__annotation_text_PointX_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Y: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
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
	
	__annotation_text_FontName_JComboBox = JGUIUtil.newFontNameJComboBox();
	__annotation_text_FontStyle_JComboBox =JGUIUtil.newFontStyleJComboBox();
	__annotation_text_FontSize_JTextField = new JTextField(3);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font name: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontName_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font style: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontStyle_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	JGUIUtil.addComponent(__annotation_text_JPanel, new JLabel("Font size: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_text_JPanel, __annotation_text_FontSize_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_text_color_JComboBox = new SimpleJComboBox(false);
	size = GRColor.COLOR_NAMES.length;
	for (int i = 0; i < size; i++) {
		__annotation_text_color_JComboBox.addItem(GRColor.COLOR_NAMES[i]);
	}
	__annotation_text_color_JComboBox.setMaximumRowCount(__annotation_text_color_JComboBox.getItemCount());
	__annotation_text_color_JComboBox.select(1);
	__annotation_text_color_JComboBox.addItemListener(this);
	__annotation_text_color_JTextField = new JTextField(10);
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

	__annotation_text_JPanel.setBorder(BorderFactory.createTitledBorder("Text"));
		
	__annotation_symbol_JPanel = new JPanel();
	__annotation_symbol_JPanel.setLayout(new GridBagLayout());
	
	__annotation_symbol_SymbolStyle_JComboBox = new SimpleJComboBox(false);
	size = GRSymbol.SYMBOL_NAMES.length;
	for (int i = 0; i < size; i++) {
		__annotation_symbol_SymbolStyle_JComboBox.addItem(GRSymbol.SYMBOL_NAMES[i]);
	}
	
	y = 0;
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Symbol style: " ),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_SymbolStyle_JComboBox,
		1, y, 7, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	y++;
	__annotation_symbol_PointX_JTextField = new JTextField(5);
	__annotation_symbol_PointY_JTextField = new JTextField(5);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("X: "),
		0, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_PointX_JTextField,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, new JLabel("Y: "),
		2, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_PointY_JTextField,
		3, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_symbol_SymbolPosition_JComboBox = new SimpleJComboBox(false);
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
	for ( int i = 0; i <= 20; i++ ) {
		__annotation_symbol_SymbolSize_JComboBox.addItem("" + i);
	}		
	JGUIUtil.addComponent(__annotation_symbol_JPanel, __annotation_symbol_SymbolSize_JComboBox,
		1, y, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	__annotation_symbol_color_JComboBox = new SimpleJComboBox(false);
	size = GRColor.COLOR_NAMES.length;
	for (int i = 0; i < size; i++) {
		__annotation_symbol_color_JComboBox.addItem(GRColor.COLOR_NAMES[i]);
	}
	__annotation_symbol_color_JComboBox.setMaximumRowCount(__annotation_symbol_color_JComboBox.getItemCount());
	__annotation_symbol_color_JComboBox.select(1);
	__annotation_symbol_color_JComboBox.addItemListener(this);
	__annotation_symbol_color_JTextField = new JTextField(10);
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

	__annotation_symbol_JPanel.setBorder(BorderFactory.createTitledBorder("Symbol"));

	JGUIUtil.addComponent(annotationJPanel, __annotation_symbol_JPanel,
		4, 1, 10, 10, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	JGUIUtil.addComponent(annotationJPanel, __annotation_text_JPanel,
		4, 1, 10, 10, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);		
	JGUIUtil.addComponent(annotationJPanel, __annotation_line_JPanel,
		4, 1, 10, 10, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.CENTER);
	__annotation_line_JPanel.setVisible(false);
	__annotation_symbol_JPanel.setVisible(false);
	
	return annotationJPanel;
}

/**
Create the panel used to display the data properties.  This consists of a
label at the top and a JTabbedPane for properties.
*/
private JPanel createDataJPanel ()
{	JPanel data_JPanel = new JPanel();
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
	// Initialized to first time series of first product...
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
		    // Limit the property to 60 characters...
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

	// General tab...

	JPanel general_JPanel = new JPanel();
	general_JPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	_ts_JTabbedPane.addTab("General", null, general_JPanel, "General view properties");
	_ts_enabled_JCheckBox = new JCheckBox("Time series enabled", true);
	general_JPanel.add(_ts_enabled_JCheckBox);

	// Graph Type tab...

	JPanel graphtype_JPanel = new JPanel();
	graphtype_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Graph Type", null, graphtype_JPanel, "Graph type properties" );

	int y = 0;
	JGUIUtil.addComponent ( graphtype_JPanel,
			new JLabel(
			"The graph type for each time series currently must be the same as the graph itself: " +
			_graph_graphtype_JComboBox.getSelected()),
			0, y, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE,
			GridBagConstraints.EAST );

	// Axes tab...

	JPanel axes_JPanel = new JPanel();
	axes_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Axes", null, axes_JPanel, "Axes properties" );

	y = 0;

	// "XAxis", "YAxis"

	JGUIUtil.addComponent ( axes_JPanel, new JLabel ("X axis:"),
			0, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_ts_xaxis_JComboBox = new SimpleJComboBox ( false );
	_ts_xaxis_JComboBox.addItem ( "Bottom" );
	_ts_xaxis_JComboBox.addItem ( "Top" );
	_ts_xaxis_JComboBox.setEnabled ( false );
	JGUIUtil.addComponent ( axes_JPanel, _ts_xaxis_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( axes_JPanel, new JLabel ("Y axis:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_ts_yaxis_JComboBox = new SimpleJComboBox ( false );
	_ts_yaxis_JComboBox.addItem ( "Left" );
	_ts_yaxis_JComboBox.addItem ( "Right" );
	_ts_yaxis_JComboBox.setEnabled ( false );
	JGUIUtil.addComponent ( axes_JPanel, _ts_yaxis_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Symbol...

	JPanel symbol_JPanel = new JPanel();
	symbol_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Symbol", null, symbol_JPanel, "Symbol properties" );

	y = 0;
	// Some will be disabled if not a line graph...
	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Line style:"),
			0, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_linestyle_JComboBox = new SimpleJComboBox ( false );
	_ts_linestyle_JComboBox.setToolTipText("Line style for line graphs.");
	_ts_linestyle_JComboBox.addItem("Dashed");
	_ts_linestyle_JComboBox.addItem("None");
	_ts_linestyle_JComboBox.addItem("Solid");
	JGUIUtil.addComponent ( symbol_JPanel, _ts_linestyle_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Line width:"),
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_linewidth_JComboBox = new SimpleJComboBox ( true );
	_ts_linewidth_JComboBox.setToolTipText("Line width in pixels on screen.");
	_ts_linewidth_JComboBox.addItem("1");
	_ts_linewidth_JComboBox.addItem("2");
	_ts_linewidth_JComboBox.addItem("3");
	_ts_linewidth_JComboBox.addItem("4");
	_ts_linewidth_JComboBox.addItem("5");
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
	for ( int i = 0; i < size; i++ ) {
		_ts_color_JComboBox.addItem ( GRColor.COLOR_NAMES[i] );
	}
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
	for ( int i = 0; i < size; i++ ) {
		_ts_symbolstyle_JComboBox.addItem ( GRSymbol.SYMBOL_NAMES[i] );
	}
	JGUIUtil.addComponent ( symbol_JPanel, _ts_symbolstyle_JComboBox,
			1, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Symbol size:"),
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_symbolsize_JComboBox = new SimpleJComboBox ( false );
	_ts_symbolsize_JComboBox.setToolTipText ( "Symbol size for line and point graphs - see also symbol style.");
	for ( int i = 0; i <= 20; i++ ) {
		_ts_symbolsize_JComboBox.addItem ( "" + i );
	}
	_ts_symbolsize_JComboBox.setMaximumRowCount(_ts_symbolsize_JComboBox.getItemCount());
	JGUIUtil.addComponent ( symbol_JPanel, _ts_symbolsize_JComboBox,
			3, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	
   JGUIUtil.addComponent ( symbol_JPanel, new JLabel ("Flagged data symbol style:"),
        0, ++y, 1, 1, 0, 0, _insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.EAST );
    _ts_flaggedDataSymbolStyle_JComboBox = new SimpleJComboBox ( false );
    _ts_flaggedDataSymbolStyle_JComboBox.setToolTipText ( "Flagged data symbol for line and point graphs - see also symbol size.");
    _ts_flaggedDataSymbolStyle_JComboBox.addItem ( "" );
    size = GRSymbol.SYMBOL_NAMES.length;
    for ( int i = 0; i < size; i++ ) {
        _ts_flaggedDataSymbolStyle_JComboBox.addItem ( GRSymbol.SYMBOL_NAMES[i] );
    }
    JGUIUtil.addComponent ( symbol_JPanel, _ts_flaggedDataSymbolStyle_JComboBox,
        1, y, 1, 1, 0, 0, _insetsTLBR,
        GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Label...

	JPanel label_JPanel = new JPanel();
	label_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Label", null,label_JPanel,"Label properties");

	y = 0;
	JGUIUtil.addComponent (label_JPanel, new JLabel ("If the Label Format is Auto, defaults or"+
			" the Graph Label Format will be used."),
			0, y, 3, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel, new JLabel ("Position:"),
			0, ++y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_ts_datalabelposition_JComboBox = new SimpleJComboBox( false );
	String [] positions = GRText.getTextPositions();
	_ts_datalabelposition_JComboBox.setMaximumRowCount( positions.length + 1);
	_ts_datalabelposition_JComboBox.addItem ( "Auto" );
	for ( int i = 0; i < positions.length; i++ ) {
		_ts_datalabelposition_JComboBox.addItem ( positions[i] );
	}
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
	String [] formats = TimeUtil.getDateTimeFormatSpecifiers ( true, true );
	for ( int i = 0; i < formats.length; i++ ) {
		_ts_datalabelformat_JComboBox.addItem ( formats[i] );
	}
	_ts_datalabelformat_JComboBox.addItem ( "%v - Value" );
	_ts_datalabelformat_JComboBox.addItem ( "%U - Units" );
	_ts_datalabelformat_JComboBox.addItem ( "%q - Flag" );
	_ts_datalabelformat_JComboBox.setMaximumRowCount(_ts_datalabelformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( label_JPanel, _ts_datalabelformat_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Legend...

	JPanel legend_JPanel = new JPanel();
	legend_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Legend", null, legend_JPanel, "Legend properties" );

	y = 0;
	// Need to add "LegendEnabled" checkbox
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ( "If the " +
			"Format is Auto, defaults or the Graph Legend Format will be used."),
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
	_ts_legendformat_JComboBox.addItem ( "Auto" );
	for ( int i = 0; i < formats.length; i++ ) {
		_ts_legendformat_JComboBox.addItem ( formats[i] );
	}
	_ts_legendformat_JComboBox.setMaximumRowCount( _ts_legendformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( legend_JPanel, _ts_legendformat_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Analysis...

	// This panel is the main panel managed by the TabbedPane...

	_ts_analysis_JPanel = new JPanel();
	_ts_analysis_JPanel.setLayout ( gbl );
	_ts_JTabbedPane.addTab ( "Analysis", null, _ts_analysis_JPanel, "Analysis properties" );

	// Create a blank panel that is set visible when there is no analysis for the time series...

	_ts_blank_analysis_JPanel = new JPanel ();
	_ts_blank_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent ( _ts_analysis_JPanel, _ts_blank_analysis_JPanel,
			0, 0, 1, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_ts_blank_analysis_JPanel.setVisible ( true );
	JGUIUtil.addComponent ( _ts_blank_analysis_JPanel, new JLabel ( "Time series has no analysis." ),
			0, 0, 1, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	// Create a panel that is set visible for the XY-Scatter graph...

	_ts_xyscatter_analysis_JPanel = new JPanel ();
	_ts_xyscatter_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (_ts_analysis_JPanel, _ts_xyscatter_analysis_JPanel,
	        0, 0, 1, 1, 1, 1, _insetsTLBR,
	        GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_ts_xyscatter_analysis_JPanel.setVisible ( false );

	y = 0;
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, new JLabel (
			"Only the properties for time series 2 and higher cause a visual change." ),
			0, y, 4, 1, 0, 0, _insetsTLBR,
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
	_ts_confidenceinterval_JComboBox.addItem ( "" );
	_ts_confidenceinterval_JComboBox.addItem ( "95" );
	_ts_confidenceinterval_JComboBox.addItem ( "99" );
	JGUIUtil.addComponent ( _ts_xyscatter_analysis_JPanel, _ts_confidenceinterval_JComboBox,
			2, y, 1, 1, 0, 0, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.WEST );
	
	return data_JPanel;
}

/**
Create the panel used to display the graph layout.  This is a panel with two
components, one for the main graph, one for the reference graph.  For now, just
do a simple diagram.  Later, might add WYSIWYG display so users can see exactly
what the graph will look like.
@return the Panel that shows the layout of the graphs.
*/
private JPanel createLayoutJPanel ()
{	JPanel layout_JPanel = new JPanel();

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
Create the panel used to display the Product properties.  This consists of a
label at the top and a JTabbedPane for properties.
*/
private JPanel createProductJPanel ()
{	JPanel product_JPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout();
	product_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (product_JPanel,new JLabel("Product Properties:"),
			0, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL,
			GridBagConstraints.NORTH );

	_product_JTabbedPane = new JTabbedPane();
	JGUIUtil.addComponent ( product_JPanel, _product_JTabbedPane,
			0, 1, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.BOTH,
			GridBagConstraints.NORTH );

	// General tab...

	JPanel general_JPanel = new JPanel();
	_product_JTabbedPane.addTab ( "General", null, general_JPanel, "General properties" );
	general_JPanel.setLayout(new GridBagLayout());
	__product_id_JTextField = new JTextField(20);	
	JGUIUtil.addComponent(general_JPanel, new JLabel("Product ID: "),
		0, 0, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, __product_id_JTextField,
		1, 0, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	__product_name_JTextField = new JTextField(40);
	JGUIUtil.addComponent(general_JPanel, new JLabel("Product name: "),
		0, 1, 1, 1, 0, 0, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, __product_name_JTextField,
		1, 1, 1, 1, 1, 1,
		GridBagConstraints.NONE, GridBagConstraints.WEST);
	_product_enabled_JCheckBox = new JCheckBox("Product enabled", true);
	_product_enabled_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent(general_JPanel, _product_enabled_JCheckBox,
		0, 2, 1, 1, 0, 0,
		GridBagConstraints.NONE, GridBagConstraints.EAST);

	// Titles tab...

	JPanel title_JPanel = new JPanel();
	title_JPanel.setLayout ( gbl );
	_product_JTabbedPane.addTab ( "Titles", null, title_JPanel, "Title properties" );

	int y = 0;
	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Main title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent ( title_JPanel, _product_maintitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_maintitle_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (title_JPanel, _product_maintitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Sub title:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( title_JPanel, _product_subtitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent(title_JPanel,_product_subtitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontstyle_JComboBox =JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel, _product_subtitle_fontstyle_JComboBox,
		3, y, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_product_subtitle_fontsize_JTextField = new JTextField ( 3 );
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
	_layoutTypeJComboBox.add("Grid");
	_layoutTypeJComboBox.setEnabled(false);
	JGUIUtil.addComponent(layoutJPanel, _layoutTypeJComboBox,
		1, y, 1, 1, 0, 0, _insetsTLBR, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	JGUIUtil.addComponent(layoutJPanel, new JLabel("Number of grid rows:"),
		0, ++y, 1, 1, 0, 0, _insetsTLBR, 
		GridBagConstraints.NONE, GridBagConstraints.EAST);
	_numberRowsJTextField = new JTextField(10);
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
	_numberColsJTextField.setText("1");
	_numberColsJTextField.setEnabled(false);
	_numberColsJTextField.setEditable(false);
	JGUIUtil.addComponent(layoutJPanel, _numberColsJTextField,
		1, y, 1, 1, 1, 0, _insetsTLBR, 
		GridBagConstraints.NONE, GridBagConstraints.WEST);

/* TODO SAM ...
	// Reference graph...

	JPanel reference_JPanel = new JPanel();
	_product_JTabbedPane.addTab ( "Reference Graph", null, reference_JPanel, "Reference graph properties" );
*/

	return product_JPanel;
}

/**
Create the panel used to display the SubProduct properties.  This consists of a
label at the top and a TabbedPane for properties.
*/
private JPanel createSubproductJPanel ()
{	JPanel graph_JPanel = new JPanel();
	GridBagLayout gbl = new GridBagLayout();
	graph_JPanel.setLayout ( gbl );
	String prop_val;
	JGUIUtil.addComponent ( graph_JPanel, new JLabel ( "Graph Properties:"),
			0, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );

	_graph_JComboBox = new SimpleJComboBox ( false );
	int nsub = _tsproduct.getNumSubProducts();
	if ( nsub == 0 ) {
		_graph_JComboBox.addItem ( NO_GRAPHS_DEFINED );
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
			_graph_JComboBox.addItem ( "" + (isub + 1) + " - " + prop_val );
		}
	}
	_graph_JComboBox.addItemListener ( this );

	JGUIUtil.addComponent ( graph_JPanel, _graph_JComboBox,
			2, 0, 1, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST );

	_graph_JTabbedPane = new JTabbedPane();
	_graph_JTabbedPane.addChangeListener ( this );
	JGUIUtil.addComponent ( graph_JPanel, _graph_JTabbedPane,
			0, 1, 3, 1, 1, 0,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	// General tab (use flow layout until more items are added)...

	JPanel general_JPanel = new JPanel();
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
	_graph_isref_JCheckBox = new JCheckBox("", true);
	_graph_isref_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent(general_JPanel, _graph_enabled_JCheckBox,
		1, 0, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);

	_yPercentJTextField = new JTextField(10);
	JGUIUtil.addComponent(general_JPanel, new JLabel("Y percent size: "),
		0, 1, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	JGUIUtil.addComponent(general_JPanel, _yPercentJTextField,
		1, 1, 1, 1, 0, 0,
		_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	
	// Graph Type tab...

	JPanel graphtype_JPanel = new JPanel();
	graphtype_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Graph Type", null, graphtype_JPanel, "Graph type properties" );

	int y = 0;
	JGUIUtil.addComponent ( graphtype_JPanel, new JLabel("Graph type:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_graphtype_JComboBox = new SimpleJComboBox( false );
//	_graph_graphtype_JComboBox.setEnabled ( false );
	for ( TSGraphType graphType: TSGraphType.values() ) {
	    if ( graphType != TSGraphType.UNKNOWN ) {
	        _graph_graphtype_JComboBox.addItem ( "" + graphType );
	    }
	}
	int size = _graph_graphtype_JComboBox.getItemCount();
	_graph_graphtype_JComboBox.addItemListener(this);
//	_graph_graphtype_JComboBox.setEnabled(false);
	JGUIUtil.addComponent ( graphtype_JPanel, _graph_graphtype_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Add bar position and overlap choice regardless of type.  The displaySubProduct()
	// method sets to visible only if a bar graph...

	_graph_barposition_JLabel = new JLabel("Bar position:");
	JGUIUtil.addComponent ( graphtype_JPanel, _graph_barposition_JLabel,
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_barposition_JComboBox = new SimpleJComboBox( false );
	_graph_barposition_JComboBox.addItem ( "CenteredOnDate" );
	_graph_barposition_JComboBox.addItem ( "LeftOfDate" );
	_graph_barposition_JComboBox.addItem ( "RightOfDate" );
	JGUIUtil.addComponent ( graphtype_JPanel, _graph_barposition_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
    _graph_barOverlap_JLabel = new JLabel("Bar overlap:");
    JGUIUtil.addComponent ( graphtype_JPanel, _graph_barOverlap_JLabel,
        0, ++y, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
    _graph_barOverlap_JComboBox = new SimpleJComboBox( false );
    _graph_barOverlap_JComboBox.addItem ( "False" );
    _graph_barOverlap_JComboBox.addItem ( "True" );
    _graph_barOverlap_JComboBox.setToolTipText ( "False will display bars next to each other, " +
    	"True will display bars with first time series in back and last in front." );
    JGUIUtil.addComponent ( graphtype_JPanel, _graph_barOverlap_JComboBox,
        1, y, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Titles tab...

	JPanel title_JPanel = new JPanel();
	title_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Titles", null, title_JPanel, "Title properties" );

	y = 0;
	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Main title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( title_JPanel, _graph_maintitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent(title_JPanel, _graph_maintitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel,_graph_maintitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_maintitle_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent(title_JPanel,_graph_maintitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( title_JPanel, new JLabel ("Sub title:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( title_JPanel, _graph_subtitle_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent(title_JPanel, _graph_subtitle_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(title_JPanel, _graph_subtitle_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_subtitle_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (title_JPanel,_graph_subtitle_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

/* TODO SAM take out for now
	// Enabled Data tab..

	JPanel enabled_data_JPanel = new JPanel();
	_graph_TabbedPane.addTab ( "Enabled Data", null, enabled_data_JPanel, "Enabled data" );
	enabled_data_Panel.add (
		new JLabel( "Data (time series) for the graph are enabled using the data General tab.") );
*/

	// X Axes tab..

	JPanel xaxes_JPanel = new JPanel();
	xaxes_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "X Axis", null, xaxes_JPanel, "X Axis properties" );

	y = 0;
	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Bottom title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_title_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_title_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontname_JComboBox=JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent(xaxes_JPanel, _graph_bottomx_title_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(xaxes_JPanel, _graph_bottomx_title_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_title_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_title_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Label font:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_label_fontname_JComboBox=JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_label_fontname_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_label_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_label_fontstyle_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_bottomx_label_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (xaxes_JPanel, _graph_bottomx_label_fontsize_JTextField,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( xaxes_JPanel, new JLabel ("Major grid color:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_bottomx_majorgrid_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the grid color.
	_graph_bottomx_majorgrid_color_JTextField.setEditable(false);
	JGUIUtil.addComponent ( xaxes_JPanel, _graph_bottomx_majorgrid_color_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_bottomx_majorgrid_color_JComboBox = new SimpleJComboBox( false );
	_graph_bottomx_majorgrid_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	for ( int i = 0; i < size; i++ ) {
		_graph_bottomx_majorgrid_color_JComboBox.addItem ( GRColor.COLOR_NAMES[i]);
	}
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

	// Y Axes tab..

	JPanel yaxes_JPanel = new JPanel();
	yaxes_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Y Axis", null, yaxes_JPanel, "Y Axis properties" );

	y = 0;
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Left title:"),
			0, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_title_JTextField = new JTextField ( 30 );
	JGUIUtil.addComponent ( yaxes_JPanel, _graph_lefty_title_JTextField,
			1, y, 3, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_title_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_title_fontname_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_title_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yaxes_JPanel, _graph_lefty_title_fontstyle_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_title_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_title_fontsize_JTextField,
			6, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Label:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Precision:"),
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_precision_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_precision_JTextField,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Font:"),
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_label_fontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_label_fontname_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_label_fontstyle_JComboBox=JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(yaxes_JPanel, _graph_lefty_label_fontstyle_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_lefty_label_fontsize_JTextField = new JTextField ( 3 );
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_label_fontsize_JTextField,
			6, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Axis type:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_type_JComboBox = new SimpleJComboBox ( false );
	_graph_lefty_type_JComboBox.setEnabled ( false );
	_graph_lefty_type_JComboBox.addItem ( "Linear" );
	_graph_lefty_type_JComboBox.addItem ( "Log" );
	JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_type_JComboBox,
			1, y, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Min value:"),
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	List values = getPropertyChoices("LeftYAxisMin");
	String value = getPropertyChoiceDefault("LeftYAxisMin");
	_graph_lefty_min_JComboBox = new SimpleJComboBox(values, true);
	_graph_lefty_min_JComboBox.select(value);
	_graph_lefty_min_JComboBox.setEnabled ( false );
	_graph_lefty_min_JComboBox.setToolTipText(
		"<html>Minimum Y-axis value.<br>Auto = use full data set "
		+ "limits.<br>### = use the entered number</html>");	
	JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_min_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	_graph_lefty_ignoreunits_JCheckBox=new JCheckBox("Ignore units", false);
	_graph_lefty_ignoreunits_JCheckBox.setEnabled ( false );
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_ignoreunits_JCheckBox,
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Units:"),
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_units_JTextField = new JTextField ( 6 );
	_graph_lefty_units_JTextField.setEnabled(false);
	JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_units_JTextField,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Max value:"),
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );

	values = getPropertyChoices("LeftYAxisMax");
	value = getPropertyChoiceDefault("LeftYAxisMax");
	_graph_lefty_max_JComboBox = new SimpleJComboBox( values, true);
	_graph_lefty_max_JComboBox.select(value);
	_graph_lefty_max_JComboBox.setEnabled ( false );
	_graph_lefty_max_JComboBox.setToolTipText(
		"<html>Maximum Y-axis value.<br>Auto = use full data set "
		+ "limits.<br>### = use the entered number</html>");
	JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_max_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

/* TODO SAM add these later when the axis is actually used.
	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Right title:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
*/
	_graph_righty_title_JTextField = new JTextField ( 30 );
	_graph_righty_title_JTextField.setEnabled ( false );
/* TODO SAM add later when the axis is actually used.
	JGUIUtil.addComponent ( yaxes_JPanel, _graph_righty_title_JTextField,
			1, y, 3, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/
	_graph_righty_title_fontname_JComboBox =JGUIUtil.newFontNameJComboBox();
	_graph_righty_title_fontname_JComboBox.setEnabled ( false );
/* TODO SAM
	JGUIUtil.addComponent(yaxes_JPanel, _graph_righty_title_fontname_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/
	_graph_righty_title_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	_graph_righty_title_fontstyle_JComboBox.setEnabled ( false );
/* TODO SAM
	JGUIUtil.addComponent ( yaxes_JPanel, _graph_righty_title_fontstyle_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/
	_graph_righty_title_fontsize_JTextField = new JTextField ( 3 );
	_graph_righty_title_fontsize_JTextField.setEnabled ( false );
/* TODO SAM 
	JGUIUtil.addComponent(yaxes_JPanel, _graph_righty_title_fontsize_JTextField,
			6, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Label font:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
*/
	_graph_righty_label_fontname_JComboBox =JGUIUtil.newFontNameJComboBox();
	_graph_righty_label_fontname_JComboBox.setEnabled ( false );
/* TODO SAM
	JGUIUtil.addComponent(yaxes_JPanel, _graph_righty_label_fontname_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/
	_graph_righty_label_fontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	_graph_righty_label_fontstyle_JComboBox.setEnabled ( false );
/* TODO SAM
	JGUIUtil.addComponent(yaxes_JPanel, _graph_righty_label_fontstyle_JComboBox,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/
	_graph_righty_label_fontsize_JTextField = new JTextField ( 3 );
	_graph_righty_label_fontsize_JTextField.setEnabled ( false );
/* TODO SAM
	JGUIUtil.addComponent(yaxes_JPanel, _graph_righty_label_fontsize_JTextField,
			6, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
*/

	JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Major grid color:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_lefty_majorgrid_color_JTextField = new JTextField (10);
	// Do not set the background color because it will be set to that of the grid color.
	_graph_lefty_majorgrid_color_JTextField.setEditable(false);
	JGUIUtil.addComponent ( yaxes_JPanel, _graph_lefty_majorgrid_color_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE,
			GridBagConstraints.WEST );
	_graph_lefty_majorgrid_color_JComboBox = new SimpleJComboBox( false );
	_graph_lefty_majorgrid_color_JComboBox.addItemListener(this);
	size = GRColor.COLOR_NAMES.length;
	for ( int i = 0; i < size; i++ ) {
		_graph_lefty_majorgrid_color_JComboBox.addItem ( GRColor.COLOR_NAMES[i]);
	}
	_graph_lefty_majorgrid_color_JComboBox.setMaximumRowCount(
		_graph_lefty_majorgrid_color_JComboBox.getItemCount());
	JGUIUtil.addComponent ( yaxes_JPanel, _graph_lefty_majorgrid_color_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_lefty_majorgrid_color_JButton = new SimpleJButton ( "Custom", "Custom", this );
	_graph_lefty_majorgrid_color_JButton.setEnabled(false);
	JGUIUtil.addComponent (yaxes_JPanel, _graph_lefty_majorgrid_color_JButton,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
    values = getPropertyChoices("LeftYAxisDirection");
    value = getPropertyChoiceDefault("LeftYAxisDirection");
    JGUIUtil.addComponent ( yaxes_JPanel, new JLabel ("Axis direction:"),
        0, ++y, 1, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );   
    _graph_lefty_direction_JComboBox = new SimpleJComboBox( values, true);
    _graph_lefty_direction_JComboBox.select(value);
    //_graph_lefty_direction_JComboBox.setEnabled ( false );
    _graph_lefty_direction_JComboBox.setToolTipText(
        "Direction of y-axis values (default=Normal)");
    JGUIUtil.addComponent (yaxes_JPanel,_graph_lefty_direction_JComboBox,
        1, y, 2, 1, 0, 0,
        _insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
    
	// Label tab..

	JPanel label_JPanel = new JPanel();
	label_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Label", null, label_JPanel, "Label properties" );

	y = 0;
	JGUIUtil.addComponent ( label_JPanel, new JLabel ( "The Label Format," +
			" if specified, will override the Time Series label properties."),
			0, y, 6, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( label_JPanel, new JLabel ("Position:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_datalabelposition_JComboBox = new SimpleJComboBox( false );
	String [] positions = GRText.getTextPositions();
	_graph_datalabelposition_JComboBox.setMaximumRowCount(positions.length);
	for ( int i = 0; i < positions.length; i++ ) {
		_graph_datalabelposition_JComboBox.addItem ( positions[i] );
	}
	_graph_datalabelposition_JComboBox.select ( "Right" );
	JGUIUtil.addComponent ( label_JPanel,_graph_datalabelposition_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent (label_JPanel,new JLabel("Format (see choices):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_datalabelformat_JTextField = new JTextField ( 15 );
	JGUIUtil.addComponent ( label_JPanel, _graph_datalabelformat_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_datalabelformat_JComboBox = new SimpleJComboBox( false );
	_graph_datalabelformat_JComboBox.addItemListener ( this );
	String [] formats = TimeUtil.getDateTimeFormatSpecifiers ( true, true );
	for ( int i = 0; i < formats.length; i++ ) {
		_graph_datalabelformat_JComboBox.addItem ( formats[i] );
	}
	_graph_datalabelformat_JComboBox.addItem ( "%v - Value" );
	_graph_datalabelformat_JComboBox.addItem ( "%U - Units" );
	_graph_datalabelformat_JComboBox.addItem ( "%q - Flag" );
	_graph_datalabelformat_JComboBox.setMaximumRowCount( _graph_datalabelformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( label_JPanel, _graph_datalabelformat_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent (label_JPanel, _graph_datalabelfontname_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent(label_JPanel, _graph_datalabelfontstyle_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_datalabelfontsize_JTextField = new JTextField ( 2 );
	JGUIUtil.addComponent (label_JPanel,_graph_datalabelfontsize_JTextField,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Legend tab..

	JPanel legend_JPanel = new JPanel();
	legend_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ( "Legend", null, legend_JPanel, "Legend properties" );

	y = 0;
	// Need to add "LegendEnabled" checkbox

	JGUIUtil.addComponent ( legend_JPanel, new JLabel ( "If the Format is Auto, the Time"+
			" Series Legend Format or defaults will be used."),
			0, y, 6, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Position:"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_legendposition_JComboBox = new SimpleJComboBox( false );
	_graph_legendposition_JComboBox.addItem ( "Bottom" );
	_graph_legendposition_JComboBox.addItem ( "InsideLowerLeft");
	_graph_legendposition_JComboBox.addItem ( "InsideLowerRight");
	_graph_legendposition_JComboBox.addItem ( "InsideUpperLeft");
	_graph_legendposition_JComboBox.addItem ( "InsideUpperRight");
	_graph_legendposition_JComboBox.addItem ( "Left" );
	_graph_legendposition_JComboBox.addItem ( "None" );
	_graph_legendposition_JComboBox.addItem ( "Right" );
	// Disable for now - mainly need the option of vertical or horizontal edge for spacing...
	//_graph_legendposition_Choice.addItem ( "Top" );
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendposition_JComboBox,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent ( legend_JPanel, new JLabel ("Format (see choices):"),
			0, ++y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_graph_legendformat_JTextField = new JTextField ( 20 );
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendformat_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_legendformat_JComboBox = new SimpleJComboBox( false );
	_graph_legendformat_JComboBox.addItemListener ( this );
	formats = TSUtil.getTSFormatSpecifiers ( true );
	_graph_legendformat_JComboBox.addItem ( "Auto" );
	for ( int i = 0; i < formats.length; i++ ) {
		_graph_legendformat_JComboBox.addItem ( formats[i] );
	}
	_graph_legendformat_JComboBox.setMaximumRowCount(_graph_legendformat_JComboBox.getItemCount());
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendformat_JComboBox,
			2, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legendfontname_JComboBox = JGUIUtil.newFontNameJComboBox();
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendfontname_JComboBox,
			3, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legendfontstyle_JComboBox = JGUIUtil.newFontStyleJComboBox();
	JGUIUtil.addComponent ( legend_JPanel, _graph_legendfontstyle_JComboBox,
			4, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );
	_graph_legend_fontsize_JTextField = new JTextField ( 2 );
	JGUIUtil.addComponent (legend_JPanel,_graph_legend_fontsize_JTextField,
			5, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Zoom tab...

	JPanel zoom_JPanel = new JPanel();
	zoom_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab ("Zoom", null, zoom_JPanel,"Zoom properties");

	_graph_zoomenabled_JCheckBox = new JCheckBox("Zoom enabled", true);
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
	_graph_zoomgroup_JTextField.setEditable(false);
	JGUIUtil.addComponent (zoom_JPanel,_graph_zoomgroup_JTextField,
			1, y, 1, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_graph_zoomgroup_JTextField.setEnabled ( false );

	// Analysis tab...

	// This panel is the main panel managed by the TabbedPane...

	_graph_analysis_JPanel = new JPanel();
	_graph_analysis_JPanel.setLayout ( gbl );
	_graph_JTabbedPane.addTab( "Analysis",null,_graph_analysis_JPanel, "Analysis properties");

	// Create a blank panel that is set visible when there is no analysis for the graph...

	_blank_analysis_JPanel = new JPanel ();
	_blank_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent ( _graph_analysis_JPanel, _blank_analysis_JPanel,
			0, 0, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_blank_analysis_JPanel.setVisible ( true );
	JGUIUtil.addComponent ( _blank_analysis_JPanel, new JLabel ( "Graph has no analysis." ),
			0, 0, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	// Create a panel that is set visible for the XY-Scatter graph...

	_xyscatter_analysis_JPanel = new JPanel ();
	_xyscatter_analysis_JPanel.setLayout ( gbl );
	JGUIUtil.addComponent (_graph_analysis_JPanel,
			_xyscatter_analysis_JPanel, 0, 0, 1, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	_xyscatter_analysis_JPanel.setVisible ( false );
	y = 0;
	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel,
			new JLabel ( "Select the parameters for the " +
			"XY-Scatter Graph curve fit analysis (applies to all time series)." ),
			0, y, 7, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Curve fit method:" ),
			0,++y, 2, 1, 1, 1,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_method_JComboBox = new SimpleJComboBox ( false );
	_xyscatter_analysis_method_JComboBox.addItemListener ( this );
	_xyscatter_analysis_method_JComboBox.addItem("MOVE2");
	_xyscatter_analysis_method_JComboBox.addItem("OLSRegression");
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
	_xyscatter_analysis_transform_JComboBox.addItemListener ( this );
	_xyscatter_analysis_transform_JComboBox.addItem("Log");
	_xyscatter_analysis_transform_JComboBox.addItem("None");
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
	_xyscatter_analysis_neqn_JComboBox.addItemListener ( this );
	_xyscatter_analysis_neqn_JComboBox.addItem("MonthlyEquations");
	_xyscatter_analysis_neqn_JComboBox.addItem("OneEquation");
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
		"<html>Specify months of interest as numbers 1-12,<br>" +
		"separated by spaces or commas.</html>" );
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
		"<html>Specify blank to analyze full period,<br>" +
		"or specify using standard date/time formats.</html>" );
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
		"<html>Specify blank to analyze full period,<br>" +
		"or specify using standard date/time formats.</html>" );
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
		"<html>Specify blank to analyze full period,<br>" +
		"or specify using standard date/time formats.</html>" );
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
		"<html>Specify blank to analyze full period,<br>" +
		"or specify using standard date/time formats.</html>" );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel,
		_ind_analysis_period_end_JTextField,
		5, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// The following used only when analyzing for filling data...

	_xyscatter_analysis_fill_JCheckBox = new JCheckBox ( "Analyze (RSME) for filling", false );
	_xyscatter_analysis_fill_JCheckBox.addItemListener ( this );
	_xyscatter_analysis_fill_JCheckBox.setToolTipText(
		"<html>Default is to analyze for line of best fit.</html>");
	JGUIUtil.addComponent (_xyscatter_analysis_JPanel, _xyscatter_analysis_fill_JCheckBox,
			0, ++y, 2, 1, 0, 0,
			_insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );

	JGUIUtil.addComponent ( _xyscatter_analysis_JPanel, new JLabel ( "Intercept:" ),
			4, y, 2, 1, 1, 1, _insetsTLBR,
			GridBagConstraints.NONE, GridBagConstraints.EAST );
	_xyscatter_analysis_intercept_JTextField = new JTextField ( "", 15 );
	_xyscatter_analysis_intercept_JTextField.setEnabled ( false );
	_xyscatter_analysis_intercept_JTextField.addKeyListener ( this );
	_xyscatter_analysis_intercept_JTextField.setToolTipText(
		"<html>If not blank, it must be zero.</html>");
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
		"<html>Specify blank to analyze filling the full period,<br>" +
		"or specify using standard date/time formats.</html>" );
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
		"<html>Specify blank to analyze filling the full period,<br>" +
		"or specify using standard date/time formats.</html>" );
	JGUIUtil.addComponent(_xyscatter_analysis_JPanel, _xyscatter_analysis_fill_period_end_JTextField,
		5, y, 2, 1, 1, 0, _insetsTLBR,
		GridBagConstraints.NONE, GridBagConstraints.WEST);

	// Annotations...

	_graph_JTabbedPane.addTab( "Annotations", null, createAnnotationJPanel(), "Annotation properties");

	return graph_JPanel;
}

/**
Update the display with the annotation properties for a tsproduct.
@param isub the subproduct that was/is selected.
@param iann the annotation number that is selected.
*/
private void displayAnnotationProperties(int isub, int iann) {
	String prop_val = null;
	__selectedAnnotation = iann;

	// AnnotationID
	prop_val = _tsproduct.getLayeredPropValue("AnnotationID", isub, iann, false, true);
	if (prop_val == null){ 
		prop_val = _tsproduct.getDefaultPropValue("AnnotationID", isub, iann, true);
	}
	__annotation_id_JTextField.setText(prop_val);

	// ShapeType
	prop_val = _tsproduct.getLayeredPropValue("ShapeType", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("ShapeType", isub, iann, true);
	}
	__annotation_ShapeType_JComboBox.select(prop_val);

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

	// YAxisSystem
	prop_val = _tsproduct.getLayeredPropValue("YAxisSystem", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("YAxisSystem", isub, iann, true);
	}
	__annotation_YAxisSystem_JComboBox.select(prop_val);	

	// Color
	prop_val = _tsproduct.getLayeredPropValue("Color", isub, iann, false, true);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("Color", isub, iann, true);
	}

	__annotation_line_color_JTextField.setText(prop_val);
	try {	
		JGUIUtil.selectIgnoreCase( __annotation_line_color_JComboBox, prop_val);
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

	__annotation_text_color_JTextField.setText(prop_val);
	try {	
		JGUIUtil.selectIgnoreCase(__annotation_text_color_JComboBox, prop_val);
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

	__annotation_symbol_color_JTextField.setText(prop_val);
	try {	
		JGUIUtil.selectIgnoreCase(__annotation_symbol_color_JComboBox, prop_val);
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
	__lineStyleJComboBox.select(prop_val);

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
	v = StringUtil.breakStringList(prop_val, ",", 0);
	if (v.size() != 2) {
		__annotation_text_PointX_JTextField.setText("0");
		__annotation_text_PointY_JTextField.setText("0");
		__annotation_symbol_PointX_JTextField.setText("0");
		__annotation_symbol_PointY_JTextField.setText("0");
	}
	else {
		__annotation_text_PointX_JTextField.setText(("" + v.get(0)).trim());
		__annotation_text_PointY_JTextField.setText(("" + v.get(1)).trim());
		__annotation_symbol_PointX_JTextField.setText(("" + v.get(0)).trim());
		__annotation_symbol_PointY_JTextField.setText(("" + v.get(1)).trim());
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

/**
Update the data components with settings appropriate for the TSProduct.
This should be called when a time series is selected.
*/
private void displayDataProperties ( int isub, int its )
{	String prop_val;

	// Get the graph type, which influences some settings (for now use the
	// subproduct graph type since we don't allow individual time series to be different)...

	TSGraphType graphType = TSGraphType.valueOfIgnoreCase ( _graph_graphtype_JComboBox.getSelected() );

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
Update the product components with settings appropriate for the TSProduct.
This should be called at initialization.
*/
private void displayProductProperties ()
{	String prop_val;
	String routine = "TSProductJFrame.displayProductProperties";

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
}

/**
Update the subproduct components with settings appropriate for the TSProduct.
This should be called when a graph is selected.
*/
private void displaySubproductProperties ( int isub )
{	String routine = "TSProductJFrame.displaySubproductProperties";
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

	try {
	    JGUIUtil.selectIgnoreCase(_graph_graphtype_JComboBox,
	        _tsproduct.getLayeredPropValue ("GraphType", isub, -1, false ));
	}
	catch ( Exception e ) {
		_graph_graphtype_JComboBox.select( "" + TSGraphType.LINE );
	}
	graphType = TSGraphType.valueOfIgnoreCase ( _graph_graphtype_JComboBox.getSelected() );
	
	// "BarOverlap" - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS

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

	// "BarPosition" - maybe move this to TS Symbol when/if GraphType is allowed to be set for each TS

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

	// "BottomXAxisMajorGridColor"

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

	// "BottomXAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue ("BottomXAxisTitleString", isub, -1, false );
	_graph_bottomx_title_JTextField.setText(prop_val);

	// "BottomXAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_bottomx_title_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_bottomx_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXYAxisTitleFontName",
		isub,-1) );
	}

	// "BottomXAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"BottomXAxisTitleFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_bottomx_title_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisTitleFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisTitleFontStyle",
		isub,-1) );
	}

	// "BottomXAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue("BottomXAxisTitleFontSize", 
		isub, -1, false);
	if (StringUtil.isDouble(prop_val)) {
		_graph_bottomx_title_fontsize_JTextField.setText(prop_val);
	}
	else {	
		Message.printWarning(2,routine,"BottomXAxisTitleFontSize \"" 
			+ prop_val + "\" is not recognized");
		_graph_bottomx_title_fontsize_JTextField.setText(
			_tsproduct.getDefaultPropValue(
			"BottomXAxisTitleFontSize", isub,-1));
	}

	// "BottomXAxisLabelFontName"

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

	// "BottomXAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_bottomx_label_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"BottomXAxisLabelFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontStyle",
		isub,-1) );
	}

	// "BottomXAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"BottomXAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_bottomx_label_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine,"BottomXAxisLabelFontSize \""+
		prop_val + "\" is not recognized" );
		_graph_bottomx_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("BottomXAxisLabelFontSize",
		isub,-1) );
	}

	// "DataLabelFormat"

	prop_val = _tsproduct.getLayeredPropValue (
			"DataLabelFormat", isub, -1, false );
	_graph_datalabelformat_JTextField.setText(prop_val);

	// "DataLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"DataLabelFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_datalabelfontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2,routine,"DataLabelFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_datalabelfontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("DataLabelFontName",
		isub,-1) );
	}

	// "DataLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"DataLabelFontStyle", isub, -1, false );
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

	// "DataLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"DataLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_datalabelfontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine,"DataLabelFontSize \""+
		prop_val + "\" is not recognized" );
		_graph_datalabelfontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("DataLabelFontSize",
		isub,-1) );
	}

	// "DataLabelPosition"

	prop_val = _tsproduct.getLayeredPropValue (
			"DataLabelPosition", isub, -1, false );
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

	// "General" - "Enabled"

	prop_val = _tsproduct.getLayeredPropValue (
			"Enabled", isub, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("true") ) {
		_graph_enabled_JCheckBox.setSelected ( true );
	}
	else {	_graph_enabled_JCheckBox.setSelected ( false );
	}

	// "LeftYAxisMajorGridColor"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor",
		isub, -1, false );
	_graph_lefty_majorgrid_color_JTextField.setText(prop_val);
	if ( prop_val.equalsIgnoreCase("None") ) {
		_graph_lefty_majorgrid_color_JComboBox.select("None");
		_graph_lefty_majorgrid_color_JTextField.setBackground (
		Color.white );
	}
	else {
	try {	JGUIUtil.selectIgnoreCase(
		_graph_lefty_majorgrid_color_JComboBox,prop_val);
		try {	_graph_lefty_majorgrid_color_JTextField.setBackground (
			(Color)GRColor.parseColor(
			_graph_lefty_majorgrid_color_JTextField.getText()) );
		}
		catch ( Exception e2 ) {
			_graph_lefty_majorgrid_color_JComboBox.select("None");
			_graph_lefty_majorgrid_color_JTextField.setBackground (
			Color.white );
		}
	}
	catch ( Exception e ) {
		_graph_lefty_majorgrid_color_JComboBox.select("None");
		_graph_lefty_majorgrid_color_JTextField.setBackground (
		Color.white );
	}
	}

	// "LeftYAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleString", isub, -1, false );
	_graph_lefty_title_JTextField.setText(prop_val);

	// "LeftYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_lefty_title_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_lefty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName",
		isub,-1) );
	}

	// "LeftYAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_lefty_title_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_lefty_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontStyle",
		isub,-1) );
	}

	// "LeftYAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_lefty_title_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "LeftYAxisTitleFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_lefty_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontSize",
		isub,-1) );
	}

	// "LeftYAxisLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_lefty_label_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisLabelFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_lefty_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontName",
		isub,-1) );
	}

	// "LeftYAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_lefty_label_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisLabelFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_lefty_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontStyle",
		isub,-1) );
	}

	// "LeftYAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_lefty_label_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "LeftYAxisLabelFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_lefty_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("LeftYAxisLabelFontSize",
		isub,-1) );
	}

	// AnnotationProvider
	prop_val = _tsproduct.getLayeredPropValue("AnnotationProvider",	isub, -1, false);
	if (prop_val == null) {
		prop_val = _tsproduct.getDefaultPropValue("AnnotationProvider",	isub, -1);
	}

	if ( __graphAnnotationProvider != null ) {
	    __graphAnnotationProvider.select(prop_val);
	}
	
	// "LeftYAxisDirection"

    prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisDirection", isub, -1, false );
    _graph_lefty_direction_JComboBox.select(prop_val);

	// "LeftYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (	"LeftYAxisTitleFontName", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_lefty_title_fontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisTitleFontName \"" + prop_val + "\" is not recognized" );
		_graph_lefty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisTitleFontName",
		isub,-1) );
	}
  
	// "LeftYAxisIgnoreUnits"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisIgnoreUnits", isub, -1, false );
	if ( (prop_val == null) || prop_val.equalsIgnoreCase("true") ) {
		_graph_lefty_ignoreunits_JCheckBox.setSelected ( true );
	}
	else {	_graph_lefty_ignoreunits_JCheckBox.setSelected ( false );
	}

	// "LeftYAxisLabelPrecision"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisLabelPrecision", isub, -1, false );
	_graph_lefty_precision_JTextField.setText(prop_val);

	// "LeftYAxisMax"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMax", isub, -1, false );
	_graph_lefty_max_JComboBox.select(prop_val);

	// "LeftYAxisMin"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMin", isub, -1, false );
	_graph_lefty_min_JComboBox.select(prop_val);

	// "LeftYAxisType"

	prop_val = _tsproduct.getLayeredPropValue (
			"LeftYAxisType", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_lefty_type_JComboBox,prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LeftYAxisType \"" +
		prop_val + "\" is not recognized" );
		_graph_lefty_type_JComboBox.select(
		_tsproduct.getDefaultPropValue("LeftYAxisType",isub,-1) );
	}

	// "LeftYAxisUnits"

	prop_val = _tsproduct.getLayeredPropValue (	"LeftYAxisUnits", isub, -1, false );
	_graph_lefty_units_JTextField.setText(prop_val);

	// "LegendEnabled"

	// "LegendFontName"

	prop_val = _tsproduct.getLayeredPropValue (	"LegendFontName", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_legendfontname_JComboBox, prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LegendFontName \"" + prop_val + "\" is not recognized" );
		_graph_legendfontname_JComboBox.select(	_tsproduct.getDefaultPropValue("LegendFontName",isub,-1) );
	}

	// "LegendFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (	"LegendFontStyle", isub, -1, false );
	try {
	    JGUIUtil.selectIgnoreCase(_graph_legendfontstyle_JComboBox,	prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LegendFontStyle \"" + prop_val + "\" is not recognized" );
		_graph_legendfontstyle_JComboBox.select( _tsproduct.getDefaultPropValue("LegendFontStyle",isub,-1) );
	}

	// "LegendFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"LegendFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_legend_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "LegendFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_legend_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("LegendFontSize",isub,-1) );
	}

	// "LegendFormat"

	prop_val = _tsproduct.getLayeredPropValue (
			"LegendFormat", isub, -1, false );
	_graph_legendformat_JTextField.setText(prop_val);

	// "LegendPosition"

	prop_val = _tsproduct.getLayeredPropValue (
			"LegendPosition", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_legendposition_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "LegendPosition \"" +
		prop_val + "\" is not recognized" );
		_graph_legendposition_JComboBox.select(
		_tsproduct.getDefaultPropValue("LegendPosition",isub,-1) );
	}

	// "MainTitleString"

	prop_val = _tsproduct.getLayeredPropValue (
			"MainTitleString", isub, -1, false );
	_graph_maintitle_JTextField.setText(prop_val);

	// "MainTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"MainTitleFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_maintitle_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_maintitle_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("MainTitleFontName",isub,-1) );
	}

	// "MainTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"MainTitleFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_maintitle_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "MainTitleFontStyle \"" +
		prop_val + "\" is not recognized" );
		_graph_maintitle_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("MainTitleFontStyle",isub,-1) );
	}

	// "MainTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"MainTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_maintitle_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "MainTitleFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_maintitle_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("MainTitleFontSize",isub,-1) );
	}

/*
	// "RightYAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisTitleString", isub, -1, false );
	_graph_righty_title_JTextField.setText(prop_val);

	// "RightYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisTitleFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_righty_title_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,"RightYAxisTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_righty_title_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightyYAxisTitleFontName",
		isub,-1) );
	}

	// "RightYAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisTitleFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_righty_title_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,"RightYAxisTitleFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_righty_title_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisTitleFontStyle",
		isub,-1) );
	}

	// "RightYAxisTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_righty_title_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine,"RightYAxisTitleFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_righty_title_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisTitleFontSize",
		isub,-1) );
	}

	// "RightYAxisLabelFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisLabelFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_righty_label_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,"RightYAxisLabelFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_righty_label_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightyYAxisLabelFontName",
		isub,-1) );
	}

	// "RightYAxisLabelFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisLabelFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(
		_graph_righty_label_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,"RightYAxisLabelFontStyle \""+
		prop_val + "\" is not recognized" );
		_graph_righty_label_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontStyle",
		isub,-1) );
	}

	// "RightYAxisLabelFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"RightYAxisLabelFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_righty_label_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "RightYAxisLabelFontSize \""+
		prop_val + "\" is not recognized" );
		_graph_righty_label_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("RightYAxisLabelFontSize",
		isub,-1) );
	}
*/
	// "SubTitleString"

	prop_val = _tsproduct.getLayeredPropValue (
			"SubTitleString", isub, -1, false );
	_graph_subtitle_JTextField.setText(prop_val);

	// "SubTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue (
			"SubTitleFontName", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_subtitle_fontname_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontName \"" +
		prop_val + "\" is not recognized" );
		_graph_subtitle_fontname_JComboBox.select(
		_tsproduct.getDefaultPropValue("SubTitleFontName",isub,-1) );
	}

	// "SubTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue (
			"SubTitleFontStyle", isub, -1, false );
	try {	JGUIUtil.selectIgnoreCase(_graph_subtitle_fontstyle_JComboBox,
			prop_val);
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "SubTitleFontStyle \"" +
		prop_val + "\" is not recognized" );
		_graph_subtitle_fontstyle_JComboBox.select(
		_tsproduct.getDefaultPropValue("SubTitleFontStyle",isub,-1) );
	}

	// "SubTitleFontSize"

	prop_val = _tsproduct.getLayeredPropValue (
			"SubTitleFontSize", isub, -1, false );
	if ( StringUtil.isDouble(prop_val) ) {
		_graph_subtitle_fontsize_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "SubTitleFontSize \"" +
		prop_val + "\" is not recognized" );
		_graph_subtitle_fontsize_JTextField.setText(
		_tsproduct.getDefaultPropValue("SubTitleFontSize",isub,-1) );
	}

	// "ZoomEnabled"

	prop_val = _tsproduct.getLayeredPropValue (
			"ZoomEnabled", isub, -1, false );
	if ( prop_val.equalsIgnoreCase("true") ) {
		_graph_zoomenabled_JCheckBox.setSelected(true);
	}
	else {	_graph_zoomenabled_JCheckBox.setSelected(false);
	}

	// "ZoomGroup"

	prop_val = _tsproduct.getLayeredPropValue (
			"ZoomGroup", isub, -1, false );
	if ( StringUtil.isInteger(prop_val) ) {
		_graph_zoomgroup_JTextField.setText(prop_val);
	}
	else {	Message.printWarning ( 2, routine, "ZoomGroup \"" +
		prop_val + "\" is not recognized" );
		_graph_zoomgroup_JTextField.setText(
		_tsproduct.getDefaultPropValue("ZoomGroup",isub,-1) );
	}

	// Analysis tab.  

	if (graphType == TSGraphType.XY_SCATTER
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterMethod", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_method_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine, "XYScatterMethod \"" + prop_val + "\" is not recognized" );
			_xyscatter_analysis_method_JComboBox.select(_tsproduct.getDefaultPropValue("XYScatterMethod",isub,-1) );
		}

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterTransformation", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_transform_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 3, routine,"XYScatterTransformation \"" +
			prop_val + "\" is not recognized" );
			_xyscatter_analysis_transform_JComboBox.select(
			_tsproduct.getDefaultPropValue("XYScatterTransformation",isub,-1) );
		}

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterNumberOfEquations", isub, -1, false );
		try {
		    JGUIUtil.selectIgnoreCase(_xyscatter_analysis_neqn_JComboBox,prop_val);
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "XYScatterNumberOfEquations \"" +
			prop_val + "\" is not recognized" );
			_xyscatter_analysis_neqn_JComboBox.select(
			_tsproduct.getDefaultPropValue("XYScatterNumberOfEquations",isub,-1) );
		}

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterMonth", isub, -1, false );
		_xyscatter_analysis_month_JTextField.setText ( prop_val );

		prop_val = _tsproduct.getLayeredPropValue ( "XYScatterAnalyzeForFilling", isub, -1, false );
		if ( (prop_val == null) || prop_val.equalsIgnoreCase ( "false" ) ) {
			// Default...
			_xyscatter_analysis_fill_JCheckBox.setEnabled ( false );
		}
		else {	_xyscatter_analysis_fill_JCheckBox.setEnabled ( true );
		}

		prop_val = _tsproduct.getLayeredPropValue (
				"XYScatterIntercept", isub, -1, false );
		_xyscatter_analysis_intercept_JTextField.setText ( prop_val );

		prop_val = _tsproduct.getLayeredPropValue (
				"XYScatterFillPeriodStart", isub, -1, false );
		_xyscatter_analysis_fill_period_start_JTextField.setText (
				prop_val );

		prop_val = _tsproduct.getLayeredPropValue (
				"XYScatterFillPeriodEnd", isub, -1, false );
		_xyscatter_analysis_fill_period_end_JTextField.setText(
				prop_val);

		// Display the regression analysis panel...
		_blank_analysis_JPanel.setVisible ( false );
		_xyscatter_analysis_JPanel.setVisible ( true );
		_xyscatter_analysis_JPanel.repaint();
	}
	else {	// Display the blank analysis panel...
		_xyscatter_analysis_JPanel.setVisible ( false );
		_blank_analysis_JPanel.setVisible ( true );
		_blank_analysis_JPanel.repaint();
	}
}

////////////////////////////////////////////////////////////
// DragAndDropListener methods
/**
Updates the TSProduct to save any changes immediately prior to the drag 
starting, so that the dragged data is up-to-date.  From RTiDragAndDrop 
interface.
@return true
*/
public boolean dragAboutToStart() { 
	updateTSProduct();
	return true; 
}

/**
Called when a drag is started -- does nothing.
*/
public void dragStarted() {}

/**
Responds to a successful drag by updating the time series combo box for
the current graph.  Called when a drag is completed successfully.  Moves the
dragged time series to the graph it was dragged to.
@param action ignored.
*/
public void dragSuccessful(int action) {
	int nts = _tsproduct.getNumData ( _selected_subproduct );
	__ts_JComboBox.removeAllItems();
	String prop_val;
	String sequence_number;
	for ( int its = 0; its < nts; its++ ) {
		prop_val = _tsproduct.getLayeredPropValue( "TSAlias",
			_selected_subproduct, its, false );
		if ( (prop_val == null) || prop_val.trim().equals("") ) {
			prop_val = _tsproduct.getLayeredPropValue( "TSID",
				_selected_subproduct, its, false );
		}
		sequence_number = _tsproduct.getLayeredPropValue(
			"SequenceNumber", _selected_subproduct, its, false );
		if ((sequence_number == null) || sequence_number.equals("-1") ){
			sequence_number = "";
		}
		else {	sequence_number = " [" + sequence_number + "] ";
		}
		if ( prop_val == null ) {
			prop_val = "No data";
		}
		else {	// Limit the TSID to 60 characters...
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60) + "..." +
					sequence_number;
			}
		}
		__ts_JComboBox.add ( "" + (its + 1) + " - " + prop_val +
			sequence_number );
	}
	
	if (nts > 0) {
		// Display the first time series...
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
Called when a drag was not completed successfully -- does nothing.
*/
public void dragUnsuccessful(int action) {
	checkGUIState();
}

/**
Called if the mouse cursor is over an area where dropping is allowed --
does nothing.
*/
public void dropAllowed() {}

/**
Called if the mouse leaves an area when dropping is allowed -- does nothing.
*/
public void dropExited() {}

/**
Called if the mouse cursor is over an area where dropping is not allowed --
does nothing.
*/
public void dropNotAllowed() {}

/**
Called when data are dropped successfully -- does nothing.
*/
public void dropSuccessful() {}

/**
Called when data were not dropped successfully -- does nothing.
*/
public void dropUnsuccessful() {}

/**
Enables and disables components on the GUI based on the currently-selected
graph type.  
@param isub the subproduct currently selected.
@param its the time series currently selected.
*/
void enableComponentsBasedOnGraphType(int isub, int its, boolean setValue) {
	if (!isVisible()) {
		// If the GUI is not visible (ie, in setup), this method
		// will cause major problems if called as is.  
		return;
	}
	
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase ( _graph_graphtype_JComboBox.getSelected() );
	
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
Clean up before destruction.
@exception Throwable if there is an error.
*/
protected void finalize()
throws Throwable {
	_tsview_gui = null;
	_tsproduct = null;
	__layoutCanvas = null;
	_apply_JButton = null;
	_close_JButton = null;
	__graphJFrame = null;
	_insetsTLBR = null;
	_product_JTabbedPane = null;
	_product_enabled_JCheckBox = null;
	__product_id_JTextField = null;
	__product_name_JTextField = null;
	_product_maintitle_JTextField = null;
	_product_maintitle_fontname_JComboBox = null;
	_product_maintitle_fontstyle_JComboBox = null;
	_product_maintitle_fontsize_JTextField = null;
	_product_subtitle_JTextField = null;
	_product_subtitle_fontname_JComboBox = null;
	_product_subtitle_fontstyle_JComboBox = null;
	_product_subtitle_fontsize_JTextField = null;
	_graph_JComboBox = null;
	_graph_JTabbedPane = null;
	_graph_enabled_JCheckBox = null;
	_graph_isref_JCheckBox = null;
	_graph_graphtype_JComboBox = null;
	_graph_barposition_JLabel = null;
	_graph_barposition_JComboBox = null;
	_graph_maintitle_JTextField = null;
	_graph_maintitle_fontname_JComboBox = null;
	_graph_maintitle_fontstyle_JComboBox = null;
	_graph_maintitle_fontsize_JTextField = null;
	_graph_subtitle_JTextField = null;
	_graph_subtitle_fontname_JComboBox = null;
	_graph_subtitle_fontstyle_JComboBox = null;
	_graph_subtitle_fontsize_JTextField = null;
	_graph_bottomx_label_fontname_JComboBox = null;
	_graph_bottomx_label_fontstyle_JComboBox = null;
	_graph_bottomx_label_fontsize_JTextField = null;
	_graph_bottomx_title_JTextField = null;
	_graph_bottomx_title_fontname_JComboBox = null;
	_graph_bottomx_title_fontstyle_JComboBox = null;
	_graph_bottomx_title_fontsize_JTextField = null;
	_graph_bottomx_majorgrid_color_JTextField = null;
	_graph_bottomx_majorgrid_color_JComboBox = null;
	_graph_bottomx_majorgrid_color_JButton = null;
	_graph_lefty_label_fontname_JComboBox = null;
	_graph_lefty_label_fontstyle_JComboBox = null;
	_graph_lefty_label_fontsize_JTextField = null;
	_graph_lefty_precision_JTextField = null;
	_graph_lefty_type_JComboBox = null;
	_graph_lefty_min_JComboBox = null;
	_graph_lefty_max_JComboBox = null;
	_graph_lefty_ignoreunits_JCheckBox = null;
	_graph_lefty_units_JTextField = null;
	_graph_lefty_title_JTextField = null;
	_graph_lefty_title_fontname_JComboBox = null;
	_graph_lefty_title_fontstyle_JComboBox = null;
	_graph_lefty_title_fontsize_JTextField = null;
	_graph_righty_label_fontname_JComboBox = null;
	_graph_righty_label_fontstyle_JComboBox = null;
	_graph_righty_label_fontsize_JTextField = null;
	_graph_lefty_majorgrid_color_JTextField = null;
	_graph_lefty_majorgrid_color_JComboBox = null;
	_graph_lefty_majorgrid_color_JButton = null;
	_graph_righty_title_JTextField = null;
	_graph_righty_title_fontname_JComboBox = null;
	_graph_righty_title_fontstyle_JComboBox = null;
	_graph_righty_title_fontsize_JTextField = null;
	_graph_datalabelformat_JTextField = null;
	_graph_datalabelformat_JComboBox = null;
	_graph_datalabelposition_JComboBox = null;
	_graph_datalabelfontname_JComboBox = null;
	_graph_datalabelfontstyle_JComboBox = null;
	_graph_datalabelfontsize_JTextField = null;
	_graph_legendformat_JTextField = null;
	_graph_legendformat_JComboBox = null;
	_graph_legendposition_JComboBox = null;
	_graph_legendfontname_JComboBox = null;
	_graph_legendfontstyle_JComboBox = null;
	_graph_legend_fontsize_JTextField = null;
	_graph_zoomenabled_JCheckBox = null;
	_graph_zoomgroup_JTextField = null;
	_graph_analysis_JPanel = null;
	_graph_blank_analysis_JPanel = null;
	_blank_analysis_JPanel = null;
	_xyscatter_analysis_JPanel = null;
	_xyscatter_analysis_intercept_JTextField = null;
	_xyscatter_analysis_method_JComboBox = null;
	_xyscatter_analysis_transform_JComboBox = null;
	_xyscatter_analysis_neqn_JComboBox = null;
	_xyscatter_analysis_month_JTextField = null;
	_dep_analysis_period_start_JTextField = null;
	_dep_analysis_period_end_JTextField = null;
	_ind_analysis_period_start_JTextField = null;
	_ind_analysis_period_end_JTextField = null;
	_xyscatter_analysis_fill_JCheckBox = null;
	_xyscatter_analysis_fill_period_start_JTextField = null;
	_xyscatter_analysis_fill_period_end_JTextField = null;
	__annotation_JComboBox = null;
	__selectedAnnotation = -1;	
	__annotation_id_JTextField = null;
	__annotation_ShapeType_JComboBox = null;
	__annotation_Order_JComboBox = null;
	__annotation_line_color_JTextField = null;
	__annotation_text_color_JTextField = null;
	__annotation_text_color_JComboBox = null;
	__annotation_line_color_JComboBox = null;
	__annotation_XAxisSystem_JComboBox = null;
	__annotation_YAxisSystem_JComboBox = null;
	__graphAnnotationProvider = null;
	__annotation_line_JPanel = null;
	__lineStyleJComboBox = null;
	__annotation_line_LineWidth_JTextField = null;
	__annotation_line_PointX1_JTextField = null;
	__annotation_line_PointY1_JTextField = null;
	__annotation_line_PointX2_JTextField = null;
	__annotation_line_PointY2_JTextField = null;
	__annotation_text_JPanel = null;
	__annotation_text_Text_JTextField = null;
	__annotation_text_PointX_JTextField = null;
	__annotation_text_PointY_JTextField = null;
	__annotation_text_Position_JComboBox = null;
	__annotation_text_FontName_JComboBox = null;
	__annotation_text_FontStyle_JComboBox = null;
	__annotation_text_FontSize_JTextField = null;
	__annotation_symbol_JPanel = null;
	__annotation_symbol_color_JTextField = null;
	__annotation_symbol_color_JComboBox = null;
	__annotation_symbol_PointX_JTextField = null;
	__annotation_symbol_PointY_JTextField = null;
	__annotation_symbol_SymbolPosition_JComboBox = null;
	__annotation_symbol_SymbolStyle_JComboBox = null;
	__annotation_symbol_SymbolSize_JComboBox = null;	
	__ts_JComboBox = null;
	_ts_JTabbedPane = null;
	_ts_enabled_JCheckBox = null;
	_ts_graphtype_JComboBox = null;	
	_ts_regressionline_JCheckBox = null;
	_ts_xaxis_JComboBox = null;
	_ts_yaxis_JComboBox = null;
	_ts_linestyle_JComboBox = null;
	_ts_linewidth_JComboBox = null;
	_ts_symbolstyle_JComboBox = null;
	_ts_symbolsize_JComboBox = null;
	_ts_color_JTextField = null;
	_ts_color_JComboBox = null;
	_ts_color_JButton = null;
	_ts_datalabelposition_JComboBox = null;
	_ts_datalabelformat_JComboBox = null;
	_ts_datalabelformat_JTextField = null;
	_ts_legendformat_JTextField = null;
	_ts_legendformat_JComboBox = null;
	_ts_analysis_JPanel = null;
	_ts_blank_analysis_JPanel = null;
	_ts_xyscatter_analysis_JPanel = null;
	_ts_confidenceinterval_JComboBox = null;
	__addAnnotationJButton = null;
	__delAnnotationJButton = null;
	__moveAnnotationUpJButton = null;
	__moveAnnotationDownJButton = null;

	super.finalize();
}

/**
Returns a list of the values in the graph selection combo box.
@return a list of the values in the graph selection combo box.
*/
protected List getGraphList() {
	List v = new Vector();
	for (int i = 0; i < _graph_JComboBox.getItemCount(); i++) {
		v.add(_graph_JComboBox.getItemAt(i));
	}
	return v;
}

/**
For properties that have combo boxes, returns the default selection for the
property.  Currently only supports LeftYAxisMax and LeftYAxisMin.
@param property the property to return the default combo box selection for.
@return the default combo box selection for the given property.  Returns null
if the property is unrecognized.
*/
private String getPropertyChoiceDefault(String property) {
	if (property.equalsIgnoreCase("LeftYAxisMax")) {
		return "Auto";
	}
	else if (property.equalsIgnoreCase("LeftYAxisMin")) {
		return "Auto";
	}

	return null;
}

/**
Returns a list of choices to fill a combo box with for properties that have
combo box selection interfaces.  Currently only supports LeftYAxisMax and LeftYAxisMin.
@param property the property for which to return a Vector of combo box choices.
@return a list of combo box choices, or null if the property is unrecognized.
*/
private List<String> getPropertyChoices(String property) {
    if (property.equalsIgnoreCase("LeftYAxisDirection")) {
        List<String> v = new Vector<String>();
        v.add("" + GRAxisDirectionType.NORMAL);
        v.add("" + GRAxisDirectionType.REVERSE);
        return v;
    }
    else if (property.equalsIgnoreCase("LeftYAxisMax")) {
		List<String> v = new Vector<String>();
		v.add("Auto");
//	Reserved for future use		
//		v.add("AutoCrop");
		return v;
	}
	else if (property.equalsIgnoreCase("LeftYAxisMin")) {
		List<String> v = new Vector<String>();
		v.add("Auto");
//	Reserved for future use		
//		v.add("AutoCrop");
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
	_selected_data = 0;	// First one in list
	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	__annotationProviders = new Vector();
	/*
	String prop_value = tsgraphcanvas.getTSProduct().getPropValue(
		"Product.AnnotationProviders");

	if (prop_value == null) {
		// try the old method
		prop_value = tsview_gui.getPropValue("AnnotationProviders");
	}

	Vector v = new Vector();

	if (prop_value == null) {
		// none could be found
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

	List v = tsview_gui.getTSProductAnnotationProviders();
	if (v != null) {
		int size = v.size();
		int size2 = 0;
		List v2 = null;
		TSProductAnnotationProvider tsap = null;
		for (int i = 0; i < size; i++) {
			tsap = (TSProductAnnotationProvider)v.get(i);
			v2 = tsap.getAnnotationProviderChoices();

			if (v2 != null) {
				size2 = v2.size();
				
				for (int j = 0; j < size2; j++) {
					__annotationProviders.add( ((String)v2.get(j)).trim());
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

	// For all events, only care about when a selection is made...

	Object o = evt.getItemSelectable();

	if (evt.getStateChange() != ItemEvent.SELECTED) {
		return;
	}

	String selected = null;

	boolean graphTypeSetValues = false;
	
	if (o == __annotation_JComboBox) {
		// A new annotation has been selected.
		// First save the settings from the current display...
		updateTSProduct();
		// Now update the time series tabs to agree with the selected
		// time series.  The number at the front of the items allows the
		// time series to be looked up (the rest of the string is ignored.
		__selectedAnnotation=__annotation_JComboBox.getSelectedIndex();
		if (__selectedAnnotation < 0) {
			return;
		}
		displayAnnotationProperties(_selected_subproduct, 
			__selectedAnnotation);
	}
	else if (o == __annotation_ShapeType_JComboBox) {
		selected = __annotation_ShapeType_JComboBox.getSelected();
		if (selected.equalsIgnoreCase("Line")) {
			__annotation_line_JPanel.setVisible(true);
			__annotation_text_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(false);
		}
		else if (selected.equalsIgnoreCase("Text")) {
			__annotation_line_JPanel.setVisible(false);
			__annotation_text_JPanel.setVisible(true);
			__annotation_symbol_JPanel.setVisible(false);
		}
		else if (selected.equalsIgnoreCase("Symbol")) {
			__annotation_line_JPanel.setVisible(false);
			__annotation_text_JPanel.setVisible(false);
			__annotation_symbol_JPanel.setVisible(true);
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
	else if ( o == _graph_JComboBox ) {
		// A new graph has been selected.
		// First save the settings from the current display...
		// Now refresh the graph tabs for the selected graph.  The
		// graphs are labeled with a number (starting at 1), " - ", and
		// then the title from the graph .
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
			// Auto must stand on its own...
			_graph_legendformat_JTextField.setText( "Auto" );
		}
		else {	_graph_legendformat_JTextField.setText(
			_graph_legendformat_JTextField.getText() + token );
		}
	}
	else if ( o == _graph_bottomx_majorgrid_color_JComboBox ) {
		// Set the choice in the color textfield...
		_graph_bottomx_majorgrid_color_JTextField.setText(
		_graph_bottomx_majorgrid_color_JComboBox.getSelected());
		try {	if ( _graph_bottomx_majorgrid_color_JTextField.getText(
			).equalsIgnoreCase("None")) {
			_graph_bottomx_majorgrid_color_JTextField.setBackground(
				Color.white );
			}
			else {
			_graph_bottomx_majorgrid_color_JTextField.setBackground(
			(Color)GRColor.parseColor(
			_graph_bottomx_majorgrid_color_JTextField.getText()) );
			}
		}
		catch ( Exception e ) {
			_graph_bottomx_majorgrid_color_JTextField.setBackground(
			Color.white );
		}
	}
	else if ( o == _graph_lefty_majorgrid_color_JComboBox ) {
		// Set the choice in the color textfield...
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
	else if ( o == __ts_JComboBox ) {
		// A new time series has been selected.
		// First save the settings from the current display...
		updateTSProduct();
		// Now update the time series tabs to agree with the selected
		// time series.  The number at the front of the items allows the
		// time series to be looked up (the rest of the string is ignored.
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
			// Auto must stand on its own...
			_ts_legendformat_JTextField.setText( "Auto" );
		}
		else {
		    _ts_legendformat_JTextField.setText( _ts_legendformat_JTextField.getText() + token );
		}
	}
	else if ( o == _ts_color_JComboBox ) {
		// Set the choice in the color textfield...
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
	else if (o == _graph_graphtype_JComboBox) {
		TSGraphType graphType = TSGraphType.valueOfIgnoreCase (_graph_graphtype_JComboBox.getSelected() );
		if (graphType == TSGraphType.XY_SCATTER 
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
			// Display the regression analysis panel...
			_blank_analysis_JPanel.setVisible ( false );
			_xyscatter_analysis_JPanel.setVisible ( true );
			_xyscatter_analysis_JPanel.repaint();
			// Display the regression analysis panel...
			_ts_blank_analysis_JPanel.setVisible ( false );
			_ts_xyscatter_analysis_JPanel.setVisible ( true );
			_ts_xyscatter_analysis_JPanel.repaint();
			if (graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
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
			// Display the blank analysis panel...
			_xyscatter_analysis_JPanel.setVisible ( false );
			_blank_analysis_JPanel.setVisible ( true );
			_blank_analysis_JPanel.repaint();
			_ts_xyscatter_analysis_JPanel.setVisible ( false );
			_ts_blank_analysis_JPanel.setVisible ( true );
			_ts_blank_analysis_JPanel.repaint();
			if (graphType == TSGraphType.BAR) {
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
		clearGraphProperties(_selected_subproduct, -1, graphType);
		graphTypeSetValues = true;
	}
	checkGUIState();

	enableComponentsBasedOnGraphType(_selected_subproduct, _selected_data, graphTypeSetValues);
}

/**
Respond to key press events.  Most single-key events are handled in keyReleased
to prevent multiple events.  Do track when the shift is pressed here.
@param event Key event to process.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

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
public void keyReleased ( KeyEvent event )
{	int code = event.getKeyCode();

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
public void keyTyped ( KeyEvent event )
{
}

/**
Limits the choices in the graph type combo box to only those that should 
be available given the original graph type of the graph.
@param subproduct the subproduct (base-zero) for which the graphs should be limited.
*/
private void limitGraphTypes(int subproduct) {
	boolean hold = __ignoreItemStateChange;
	__ignoreItemStateChange = true;	
	String originalGraphType = _tsproduct.getLayeredPropValue( "OriginalGraphType", subproduct, -1, false);

	if ( originalGraphType.equals("Area") || originalGraphType.equals("AreaStacked") ||
	    originalGraphType.equals("Bar") || originalGraphType.equals("Line")
	    || originalGraphType.equals("Point") ) {
		_graph_graphtype_JComboBox.removeAll();
		_graph_graphtype_JComboBox.add("Area");
		_graph_graphtype_JComboBox.add("AreaStacked");
		_graph_graphtype_JComboBox.add("Bar");
		_graph_graphtype_JComboBox.add("Line");
		_graph_graphtype_JComboBox.add("Point");
	}
	else {
	    for ( TSGraphType graphType: TSGraphType.values() ) {
	        if ( graphType != TSGraphType.UNKNOWN ) {
	            _graph_graphtype_JComboBox.addItem ( "" + graphType );
	        }
	    }
	}
	__ignoreItemStateChange = hold;
}

/**
Moves a time series from one graph to another and adjusts the numbering 
for the other times series appropriately.  The data that get moved to the new
graph are the currently-selected time series from the time series combo box.
@param fromGraph the graph (0-based) under which the data are currently stored.
@param toGraph the graph (0-based) under which to move the data.
*/
protected void moveSelectedData(int fromGraph, int toGraph, int fromTS) {
	int fromDataCount = _tsproduct.getNumData(fromGraph);
//	int moveTS = __ts_JComboBox.getSelectedIndex(); 
	int moveTS = fromTS;
	int toDataCount = _tsproduct.getNumData(toGraph);
	_tsproduct.setDirty(true);
	_tsproduct.renameDataProps("" + (fromGraph + 1), "" + (moveTS + 1), 
		"" + (toGraph + 1), "" + (toDataCount + 1));	

	for (int i = moveTS + 2; i <= fromDataCount; i++) {
		_tsproduct.renameDataProps("" + (fromGraph + 1), "" + i, "" + (fromGraph + 1), "" + (i - 1));
	}
	
	_tsproduct.sortProps();	

	int nts = _tsproduct.getNumData ( _selected_subproduct );
	if (nts > 0) {
		// Display the first time series...
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
		// A time series was moved into the graph
		String propVal = _tsproduct.getLayeredPropValue("GraphType", toGraph, 0, false);
		_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_BY_USER);
		_tsproduct.setPropValue("GraphType", propVal, toGraph, -1 );
	}

	// Do this any time graphs are changed ...
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
@param mode Indicates if the GUI should be visible at creation.
*/
private void openGUI ( boolean mode )
{	__is_initialized = false;
	if (	(JGUIUtil.getAppNameForWindows() == null) ||
		JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( "Time Series Product Properties" );
	}
	else {	setTitle( JGUIUtil.getAppNameForWindows() +
		" - Time Series Product Properties" );
	}

	// Start a big try block to set up the GUI...
	try {

	// Add a listener to catch window manager events...

	addWindowListener ( this );

	// Lay out the main window component by component.  We will start with
	// the menubar default components.  Then add each requested component
	// to the menu bar and the interface...

	GridBagLayout gbl = new GridBagLayout();

	int edge = 2;
	Insets insetsLR = new Insets ( 0, edge, 0, edge );
							// space around text
							// area
	
	// Add a panel to hold the main components...

	JPanel display_JPanel = new JPanel ();
	display_JPanel.setLayout ( gbl );
	getContentPane().add ( display_JPanel );

	int y = 0;

	// Panel for graph layout (shows sub-product row/column), etc...

	JGUIUtil.addComponent ( display_JPanel, createLayoutJPanel(),
			0, y, 2, 1, 0, 0,
			insetsLR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Panel for product properties...

	JGUIUtil.addComponent ( display_JPanel, createProductJPanel(),
			2, y, 8, 1, 1, 0,
			insetsLR, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH );
	displayProductProperties ();

	// Panel for subproduct properties...

	JGUIUtil.addComponent ( display_JPanel, createSubproductJPanel(),
			0, ++y, 10, 1, 1, 0,
			insetsLR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );
	displaySubproductProperties ( 0 );

	// Panel for time series properties...

	JGUIUtil.addComponent ( display_JPanel, createDataJPanel(),
			0, ++y, 10, 1, 1, 0,
			insetsLR, GridBagConstraints.BOTH, GridBagConstraints.NORTH );

	__is_initialized = true;
	displayDataProperties ( 0, 0 );

	// Put the buttons on the bottom of the window...

	JPanel button_JPanel = new JPanel ();
	button_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

	_apply_JButton = new SimpleJButton("Apply",
		"TSProductJFrame.Apply",this);
	button_JPanel.add ( _apply_JButton );

	_close_JButton = new SimpleJButton("Close",
		"TSProductJFrame.Close",this);
	button_JPanel.add ( _close_JButton );

	getContentPane().add ( "South", button_JPanel );
	button_JPanel = null;

	_graph_graphtype_JComboBox.addItemListener(this);

	// select these other graph types in order that the packing takes into
	// account their components.
	
	_graph_graphtype_JComboBox.select("XY-Scatter");
	_graph_graphtype_JComboBox.select("Bar");
	_graph_graphtype_JComboBox.setEnabled(true);

	// makes the text panel visible in order that pack()ing the JFrame
	// will account for space in the largest annotation panel.  
	// Currently the text panel (the default one) is the larger, so this
	// isn't necessary, but in the future the addition of more panel types may require it.
	__annotation_line_JPanel.setVisible(false);
	__annotation_symbol_JPanel.setVisible(false);
	__annotation_text_JPanel.setVisible(true);
	pack();

	// this is called now in order that it will set the proper annotation
	// panel visible at this point, and room will already have been reserved
	// to display the largest annotation panel
	displayAnnotationProperties(0, 0);

	setSubproduct(0);

	JGUIUtil.center ( this );
	setVisible ( mode );
	// Clean up...
	gbl = null;
	display_JPanel = null;
	} // end of try
	catch ( Exception e ) {
		if (IOUtil.testing()) {
			e.printStackTrace();
		}
	}
	checkGUIState();
	setSize(getWidth(), getHeight() + 15);
	setResizable(false);

	int nsp = _tsproduct.getNumSubProducts();
	String graphType = null;
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet(Prop.SET_AT_RUNTIME_FOR_USER);
	for (int isp = 0; isp < nsp; isp++) {
		graphType = _tsproduct.getLayeredPropValue( "GraphType", isp, -1, false);
		_tsproduct.setPropValue("OriginalGraphType", graphType, isp, -1);
	}
	_tsproduct.getPropList().setHowSet(how_set_prev);
	__limitGraphTypes = true;
}

/**
Handle window closing event.
*/
public void processWindowEvent ( WindowEvent e) 
{	if (e.getID() == WindowEvent.WINDOW_CLOSING ) {
		super.processWindowEvent(e);
		_tsview_gui.closeGUI(TSViewType.PROPERTIES);
	}
	else {
	    super.processWindowEvent(e);
	}
}

/**
Re-reads the properties from the TSProduct and sets the GUI to match what is
read.  Called when a new graph is added, the order of graphs are changed, or 
a graph is removed.  Forces redisplay of the properties in the tsproduct.
*/
protected void redisplayProperties() {
	// if the select subproduct is -1, then it's a new table that was just
	// added, so all the properties should be cleared and set to default values.
	if (_selected_subproduct == -1) {
		clearAllProperties();
	}
	else {
		// otherwise, read the subproduct info from the prop list
		displayProductProperties();
		displaySubproductProperties(_selected_subproduct);
		displayDataProperties(_selected_subproduct, _selected_data);
	}
	_graph_JComboBox.removeItemListener(this);
	_graph_JComboBox.removeAllItems();
	int nsub = _tsproduct.getNumSubProducts();

	if (nsub == 0) {
		_graph_JComboBox.addItem(NO_GRAPHS_DEFINED);
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
			_graph_JComboBox.addItem("" + (isub + 1) + " - " + prop_val);
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
	JGUIUtil.setEnabled(__annotation_ShapeType_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_Order_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_line_color_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_color_JTextField, enabled);
	JGUIUtil.setEnabled(__annotation_text_color_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_line_color_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_XAxisSystem_JComboBox, enabled);
	JGUIUtil.setEnabled(__annotation_YAxisSystem_JComboBox, enabled);
//	JGUIUtil.setEnabled(__graphAnnotationProvider, enabled);
	JGUIUtil.setEnabled(__lineStyleJComboBox, enabled);
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

	// add is not enabled or disabled.  It is always enabled.
	// The other buttons should have been enabled or disabled already, but
	// if everything should be disabled, turn them off.
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
//	JGUIUtil.setEnabled(_graph_enabled_JCheckBox, enabled);
	JGUIUtil.setEnabled(_graph_isref_JCheckBox, enabled);
	JGUIUtil.setEnabled(_yPercentJTextField, enabled);
	JGUIUtil.setEnabled(_graph_graphtype_JComboBox, enabled);
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
//	JGUIUtil.setEnabled(_graph_bottomx_majorgrid_color_JButton, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_label_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_precision_JTextField, enabled);
//	JGUIUtil.setEnabled(_graph_lefty_type_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_min_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_max_JComboBox, enabled);
//	JGUIUtil.setEnabled(_graph_lefty_ignoreunits_JCheckBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_units_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_lefty_title_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_righty_label_fontsize_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_majorgrid_color_JTextField, enabled);
	JGUIUtil.setEnabled(_graph_lefty_majorgrid_color_JComboBox, enabled);
//	JGUIUtil.setEnabled(_graph_lefty_majorgrid_color_JButton, enabled);
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
	JGUIUtil.setEnabled(_graph_legendposition_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legendfontname_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legendfontstyle_JComboBox, enabled);
	JGUIUtil.setEnabled(_graph_legend_fontsize_JTextField, enabled);
//	JGUIUtil.setEnabled(_graph_zoomenabled_JCheckBox, enabled);
//	JGUIUtil.setEnabled(_graph_zoomgroup_JTextField, enabled);
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
	setEditable(_graph_lefty_precision_JTextField, enabled);
	setEditable(_graph_lefty_units_JTextField, enabled);
	setEditable(_graph_lefty_title_JTextField, enabled);
	setEditable(_graph_lefty_title_fontsize_JTextField, enabled);
	setEditable(_graph_righty_label_fontsize_JTextField, enabled);
	setEditable(_graph_lefty_majorgrid_color_JTextField, enabled);
	setEditable(_graph_righty_title_JTextField, enabled);
	setEditable(_graph_righty_title_fontsize_JTextField, enabled);
	setEditable(_graph_datalabelformat_JTextField, enabled);
	setEditable(_graph_datalabelfontsize_JTextField, enabled);
	setEditable(_graph_legendformat_JTextField, enabled);
	setEditable(_graph_legend_fontsize_JTextField, enabled);
//	setEditable(_graph_zoomgroup_JTextField, enabled);
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
@param apply if true, then applyClicked() will be called and the new selection's
value will be applied to the product graph.
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
@param apply if true, then applyClicked() will be called and the new selection's
value will be applied to the product graph.
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
		// Display the first time series...
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
		__selectedAnnotation = 0;
		__annotation_JComboBox.select(__selectedAnnotation);
		displayAnnotationProperties(_selected_subproduct,__selectedAnnotation);
	}
	else {
		__selectedAnnotation = -1;
		clearAnnotationProperties();
	}
	checkGUIState();
}

/**
Directly sets the number of the selected subproduct and data, without triggering
any other methods internally.
@param isub the subproduct to select
@param idata the data to select.
*/
protected void setSelectedSubProductAndData(int isub, int idata) {
	_selected_subproduct = isub;
	_selected_data = idata;
}

/**
Set the selected sub-product.  This method is called when the properties are
displayed from a popup menu from the graph.  This is equivalent to changing the graph choice.
@param subproduct Subproduct number.
*/
protected void setSubproduct(int subproduct) {
	// A new graph has been selected.
	// First save the settings from the current display...
	updateTSProduct(); 
	// Select the subproduct in the list...
	try {
	    _graph_JComboBox.select ( subproduct );
	}
	catch ( Exception e ) {
		_graph_JComboBox.select ( 0 );
	}
	// Now refresh the graph tabs for the selected graph.  The
	// graphs are labeled with a number (starting at 1), " - ", and then the title from the graph .
	setSubproduct2 ( _graph_JComboBox.getSelected() );
}

/**
Second phase of setting the subproduct.  Does all the actual selection of 
values and does not call updateTSProduct first.
@param selected Selected subproduct (item from _graph_Choice).
*/
private void setSubproduct2 ( String selected ) {
	List list = StringUtil.breakStringList ( selected, " ", 0 );
	if ( list == null ) {
		return;
	}
	// Else, get the selected product from the first token...
	selected = (String)list.get(0);
	if ( !StringUtil.isInteger(selected) ) {
		return;
	}
	// Internally, products start at 0...
	_selected_subproduct = StringUtil.atoi(selected) - 1;
	displaySubproductProperties ( _selected_subproduct );
	// Update the list of choices in the time series to the list for the current graph...
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
		    // Limit the TSID to 60 characters...
			if ( prop_val.length() > 60 ) {
				prop_val = prop_val.substring(0,60) + "..." + sequence_number;
			}
		}
		__ignoreItemStateChange = true;
		__ts_JComboBox.add ( "" + (its + 1) + " - " + prop_val + sequence_number );
		__ignoreItemStateChange = false;
	}
	if (nts > 0) {
		// Display the first time series...
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
		__selectedAnnotation = 0;
		displayAnnotationProperties(_selected_subproduct, __selectedAnnotation);
	}
	else {
		__selectedAnnotation = -1;
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
public void stateChanged ( ChangeEvent e )
{	// Both the graph and time series tabbed panels have tabs labeled
	// "Analysis" so need to check the analysis component...
	if ( _graph_graphtype_JComboBox == null ) {
		// Initializing the interface?
		return;
	}
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase ( _graph_graphtype_JComboBox.getSelected() );
	
	Object comp = e.getSource();
	if ( comp == _graph_analysis_JPanel ) {
		if (graphType == TSGraphType.XY_SCATTER 
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
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
	else if ( comp == _ts_analysis_JPanel ) {
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
}

/**
Update the TSProduct properties.  Only properties that are different from the
original are changed.  It is assumed that _selected_subproduct and
_selected_data have been propertly set for the properties that are to be saved.
Properties that are not case senstive are only updated if truly different (e.g.,
titles are compared case-sensitive, but parameter settings are compared case-insensitive).
@return the total number of properties that have been changed.
*/
protected int updateTSProduct () {
	if (_selected_subproduct == -1) {
		return 0;
	}
	return updateTSProduct(Prop.SET_AT_RUNTIME_BY_USER);
}

/**
Update the TSProduct properties.  Only properties that are different from the
original are changed.  It is assumed that _selected_subproduct and
_selected_data have been propertly set for the properties that are to be saved.
Properties that are not case senstive are only updated if truly different (e.g.,
titles are compared case-sensitive, but parameter settings are compared case-insensitive).
@param howSet an integer value from Prop that determines how the props will
be set (such as SET_AT_RUNTIME_BY_USER).
@return the total number of properties that have been changed.
*/
protected int updateTSProduct (int howSet) {
	int ndirty = 0;
	String prop_val;
	String gui_val;
	int how_set_prev = _tsproduct.getPropList().getHowSet();
	_tsproduct.getPropList().setHowSet (howSet);
	TSGraphType graphType = TSGraphType.valueOfIgnoreCase(_graph_graphtype_JComboBox.getSelected());

	// --------------------------------------------------------------------
	// Product properties
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

	// --------------------------------------------------------------------
	// Subproduct (graph) properties
	// --------------------------------------------------------------------

	// "BarPosition" - only set if it is a bar graph, otherwise it will
	// get saved in the TSProduct file...

	if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue( "BarPosition", _selected_subproduct, -1, false);
		gui_val = _graph_barposition_JComboBox.getSelected();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("BarPosition", gui_val, _selected_subproduct, -1);
			++ndirty;
		}
	}
	
	// "BarOverlap" - only set if it is a bar graph, otherwise it will
    // get saved in the TSProduct file...

    if (graphType == TSGraphType.BAR || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
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

	// "GraphType"
	prop_val = _tsproduct.getLayeredPropValue ( "GraphType", _selected_subproduct, -1, false );
	gui_val = _graph_graphtype_JComboBox.getSelected();
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

	// "LeftYAxisMajorGridColor"

	prop_val = _tsproduct.getLayeredPropValue ( "LeftYAxisMajorGridColor", _selected_subproduct, -1, false );
	gui_val = _graph_lefty_majorgrid_color_JTextField.getText().trim();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "LeftYAxisMajorGridColor", gui_val, _selected_subproduct, -1);
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

	// "LegendPosition"

	prop_val = _tsproduct.getLayeredPropValue ( "LegendPosition", _selected_subproduct, -1, false );
	gui_val = _graph_legendposition_JComboBox.getSelected().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "LegendPosition", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

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
		// Also update the _graph_JComboBox item...
		_graph_JComboBox.removeItemListener(this);
		_graph_JComboBox.removeAt (pos);
		_graph_JComboBox.insert (("" + (pos+ 1) + " - "+ gui_val), pos);
		_graph_JComboBox.addItemListener(this);
		++ndirty;
	}

/*
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

	// "RightYAxisTitleFontName"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontName", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_fontname_JComboBox.getSelected();
	if ( !gui_val.equalsIgnoreCase(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleFontName", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}

	// "RightYAxisTitleFontStyle"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleFontStyle", _selected_subproduct, -1, false );
	gui_val= _graph_righty_title_fontstyle_JComboBox.getSelected();
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

	// "RightYAxisTitleString"

	prop_val = _tsproduct.getLayeredPropValue ( "RightYAxisTitleString", _selected_subproduct, -1, false );
	gui_val = _graph_righty_title_JTextField.getText().trim();
	if ( !gui_val.equals(prop_val) ) {
		_tsproduct.setPropValue ( "RightYAxisTitleString", gui_val, _selected_subproduct, -1 );
		++ndirty;
	}
*/

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

	if (graphType == TSGraphType.XY_SCATTER 
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {

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
			_tsproduct.setPropValue ( "XYScatterNumberOfEquations",
			gui_val, _selected_subproduct, -1 );
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
	// Data (time series) properties
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

	if (graphType == TSGraphType.XY_SCATTER || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
		prop_val = _tsproduct.getLayeredPropValue (
			"RegressionLineEnabled", _selected_subproduct, _selected_data, false );
		if ( _ts_regressionline_JCheckBox.isSelected() ) {
			gui_val = "true";
		}
		else {	
			gui_val = "false";
		}
		if ((graphType == TSGraphType.XY_SCATTER
		    || graphType == TSGraphType.PREDICTED_VALUE
		    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) &&
			!gui_val.equalsIgnoreCase(prop_val)) {
			// Only save the property if a scatter plot...
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

	if (graphType == TSGraphType.XY_SCATTER 
	    || graphType == TSGraphType.PREDICTED_VALUE
	    || graphType == TSGraphType.PREDICTED_VALUE_RESIDUAL) {
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
		__annotation_JComboBox.add("" 
			+ _tsproduct.getLayeredPropValue("AnnotationID", _selected_subproduct, i, false, true));		
	}
	__ignoreItemStateChange = false;
*/
	if (__selectedAnnotation > -1) {
	// General annotation data

	prop_val = _tsproduct.getLayeredPropValue("AnnotationID",
		_selected_subproduct, __selectedAnnotation, false, true);
	gui_val = __annotation_id_JTextField.getText().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {	
		_tsproduct.setPropValue("AnnotationID", gui_val, _selected_subproduct, __selectedAnnotation, true);
		__annotation_JComboBox.insertItemAt(gui_val, __selectedAnnotation);
		__annotation_JComboBox.removeItemAt(__selectedAnnotation + 1);
		ndirty++;
	}
	
	String shape = null;
	prop_val = _tsproduct.getLayeredPropValue( "ShapeType", _selected_subproduct, __selectedAnnotation, false, true);
	shape = prop_val;
	gui_val = __annotation_ShapeType_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("ShapeType", gui_val, _selected_subproduct, __selectedAnnotation, true);
		ndirty++;
		shape = gui_val;
	}
	
	if (shape == null || shape.equalsIgnoreCase("Text")) {
		prop_val = _tsproduct.getLayeredPropValue(
			"Color", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_text_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"FontSize", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val =__annotation_text_FontSize_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontSize", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue(
			"FontStyle", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_text_FontStyle_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontStyle", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue( "FontName", _selected_subproduct, 
			__selectedAnnotation, false, true);
		gui_val = __annotation_text_FontName_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("FontName", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue( "Point", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_text_PointX_JTextField.getText().trim() 
			+ "," + __annotation_text_PointY_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Point", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue( "Text", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_text_Text_JTextField.getText().trim();
		if (!gui_val.equals(prop_val)) {
			_tsproduct.setPropValue("Text", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue( "TextPosition", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_text_Position_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("TextPosition", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	}
	else if (shape.equalsIgnoreCase("Line")) {
		prop_val = _tsproduct.getLayeredPropValue( "Color", _selected_subproduct, 
			__selectedAnnotation, false, true);
		gui_val = __annotation_line_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"LineStyle", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __lineStyleJComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("LineStyle", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue(
			"LineWidth", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_line_LineWidth_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("LineWidth", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue(
			"Points", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_line_PointX1_JTextField.getText().trim() 
			+ "," + __annotation_line_PointY1_JTextField.getText().trim() 
			+ "," + __annotation_line_PointX2_JTextField.getText().trim()
			+ "," + __annotation_line_PointY2_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Points", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}	
	}
	else if (shape.equalsIgnoreCase("Symbol")) {
		prop_val = _tsproduct.getLayeredPropValue(
			"Color", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_symbol_color_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Color", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue(
			"Point", _selected_subproduct, __selectedAnnotation, false, true);
		gui_val = __annotation_symbol_PointX_JTextField.getText().trim()
			+ "," +__annotation_symbol_PointY_JTextField.getText().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("Point", gui_val, 
			_selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	
		prop_val = _tsproduct.getLayeredPropValue( "SymbolPosition", _selected_subproduct, 
			__selectedAnnotation, false, true);
		gui_val = __annotation_symbol_SymbolPosition_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolPosition", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "SymbolStyle", _selected_subproduct,
			__selectedAnnotation, false, true);
		gui_val = __annotation_symbol_SymbolStyle_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolStyle", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}

		prop_val = _tsproduct.getLayeredPropValue( "SymbolSize", _selected_subproduct,
			__selectedAnnotation, false, true);
		gui_val = __annotation_symbol_SymbolSize_JComboBox.getSelected().trim();
		if (!gui_val.equalsIgnoreCase(prop_val)) {
			_tsproduct.setPropValue("SymbolSize", gui_val, _selected_subproduct, __selectedAnnotation, true);
			ndirty++;
		}
	}

	prop_val = _tsproduct.getLayeredPropValue( "Order", _selected_subproduct, __selectedAnnotation, false, true);
	gui_val = __annotation_Order_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("Order", gui_val, 
		_selected_subproduct, __selectedAnnotation, true);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue(
		"XAxisSystem", _selected_subproduct, __selectedAnnotation, false, true);
	gui_val = __annotation_XAxisSystem_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("XAxisSystem", gui_val, _selected_subproduct, __selectedAnnotation, true);
		ndirty++;
	}

	prop_val = _tsproduct.getLayeredPropValue(
		"YAxisSystem", _selected_subproduct, __selectedAnnotation, false, true);
	gui_val = __annotation_YAxisSystem_JComboBox.getSelected().trim();
	if (!gui_val.equalsIgnoreCase(prop_val)) {
		_tsproduct.setPropValue("YAxisSystem", gui_val, _selected_subproduct, __selectedAnnotation, true);
		ndirty++;
	}

	_tsproduct.checkAnnotationProperties(_selected_subproduct, __selectedAnnotation);

	}
	
	// --------------------------------------------------------------------
	// Return the number of properties that were updated...
	// --------------------------------------------------------------------

	_tsproduct.getPropList().setHowSet ( how_set_prev );
	_ndirty += ndirty;
	return ndirty;
}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowActivated(WindowEvent evt) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowClosed(WindowEvent evt) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowClosing(WindowEvent event) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowDeactivated(WindowEvent evt) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowDeiconified(WindowEvent evt) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowOpened(WindowEvent evt) {}

/**
Does nothing.
TODO (JTS - 2006-05-24)
Is a window listener really necessary for this class?
*/
public void windowIconified(WindowEvent evt) {}

}