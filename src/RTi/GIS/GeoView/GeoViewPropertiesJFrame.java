//-----------------------------------------------------------------------------
// GeoViewPropertiesJFrame - properties for GeoView and a selected layer
//-----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
//
// 2001-10-08	Steven A. Malers, RTi	Initial version.
// 2001-10-15	SAM, RTi		Add check box for
//					"Label Selected Features Only" to make
//					it easier to see labels for dense data
//					layers.
// 2001-10-18	SAM, RTi		Update javadoc, remove unneeded code,
//					set unused variables to null to help
//					with garbage collection.
// 2001-11-27	SAM, RTi		Add number of layer views to GeoView
//					properties.
// 2001-12-04	SAM, RTi		Change name from GeoViewPropertiesGUI
//					and convert to Swing.
// 2002-01-08	SAM, RTi		Change GeoViewCanvas to
//					GeoViewJComponent.
//-----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Checked to see that code corresponds
//					with code in non-Swing version.
// 2003-05-08	JTS, RTi		Added a lot of non-Swing code and
//					converted it.
// 2003-05-09	JTS, RTi		Corrected errors in the Label editor 
//					event handling.
// 2003-05-12	JTS, RTi		Added a second constructor; now can
//					display properties for either a layer
//					or a GeoViewJComponent map display.
// 2003-05-14	JTS, RTi		GeoLayerViewLegendJPanel constructor
//					changed and made appropriate changes
//					here
// 2004-01-08	SAM, RTi		Set the icon and title from JGUIUtil
//					data.
// 2004-05-24	SAM, RTi		* Fix bug where the layer was getting
//					  smashed against the title.
//					* Change some upper case word starts to
//					  lowercase to more closely match
//					  standard softare guidelines.
// 2004-10-13	JTS, RTi		* Added the ability to set properties
//					  for the new legend that can appear on
//					  the map.  Layers can be removed from
//					  the legend, the legend can be turned 
//					  off or on, and the legend can be 
//					  positioned.
//					* Removed the KeyListener code as it 
//					  wasn't doing anything.
// 2004-11-01	JTS, RTi		The label combo box now has both an
//					item listener and an action listener.
//					The action listener is used to catch
//					times when the user selects from the 
//					combo box the same thing that is already
//					selected.  
// 2005-05-26	JTS, RTi		Added all data members to finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------
// EndHeader

package RTi.GIS.GeoView;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import RTi.GR.GRColorTable;
import RTi.GR.GRLegend;
import RTi.GR.GRScaledClassificationSymbol;
import RTi.GR.GRSymbol;
import RTi.GR.GRText;

import RTi.Util.GUI.BorderJPanel;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;

import RTi.Util.String.StringUtil;

import RTi.Util.Message.Message;

/**
The GeoViewPropertiesJFrame displays global properties shared for all
GeoViewLayers in a GeoViewJComponent, as well as specific properties for a
GeoLayerView.
*/
public class GeoViewPropertiesJFrame extends JFrame implements 
ActionListener, ChangeListener, WindowListener, ItemListener 
{
/**
Whether the GUI has finished being built or not.  
*/
boolean __doneInitializing = false;
/**
If true, this will only show the properties for the main geoview map.  If false,
the properties for a layer view will be displayed.
*/
boolean __geoViewOnly = false;

/**
GeoViewJComponent that is visible.
*/
private GeoViewJComponent __geoview = null;

/**
GeoLayerView to view.
*/
private GeoLayerView __layerView = null;

/**
The parent panel holding the geo view.
*/
private GeoViewJPanel		__parentGeoViewJPanel = null;

/**
The geoview project used by the geoview displaying things.
*/
private GeoViewProject __gvp = null;

private JCheckBox __labelSelectedJCheckBox = null;

private JComboBox __uniqueValuesFieldJComboBox = null;
private JComboBox __classBreakFieldJComboBox = null;
private JComboBox __labelFieldJComboBox = null;

private JPanel __symbolJPanel = null;		// Symbol Panel
private JPanel __singleSymbolJPanel = null;
private JPanel __singleLegendJPanel = null;
private JPanel __uniqueValuesJPanel = null;
private JPanel __classBreaksJPanel = null;
private JPanel __scaledSymbolJPanel = null;

private JRadioButton __singleSymbolJRadioButton = null;
private JRadioButton __uniqueValuesJRadioButton = null;
private JRadioButton __classBreaksJRadioButton = null;
private JRadioButton __scaledSymbolJRadioButton = null;

private JTabbedPane __theJTabbedPane = null;

private JTextField __backgroundColorJTextField;
private JTextField __dataHomeJTextField;
private JTextField __initialExtentJTextField;
private JTextField __maximumExtentJTextField;
private JTextField __projectionJTextField;
private JTextField __selectColorJTextField;
private JTextField __layerFileJTextField;
private JTextField __layerFormatJTextField;
private JTextField __layerProjectionJTextField;
private JTextField __numShapesJTextField;
private JTextField __shapeTypeJTextField;
private JTextField __labelFieldJTextField = null;
private JTextField __labelFormatJTextField = null;
private JTextField __appJoinFieldJTextField = null;
private JTextField __appLayerTypeJTextField = null;

private SimpleJButton __clearLabelField_JButton = null;

private SimpleJButton __applyJButton;
private SimpleJButton __okJButton;
private SimpleJButton __cancelJButton;

private final String 
	__BUTTON_APPLY = "Apply",
	__BUTTON_CANCEL = "Cancel",
	__BUTTON_OK = "OK";

/////////////////////////////////////////////////////////
// New items for the layout panel:

/**
Button labels.
*/
private String 
	__BUTTON_ADD_LAYOUT = "Add Layout",
	__BUTTON_DEL_LAYOUT = "Delete Layout";

/**
The button for deleting the current layout.  There must always be at least one
layout, so the button is greyed out if there is only one layout.
*/
private JButton __delLayoutButton = null;

/**
The checkbox for setting whether the legend is visible on the map shown in 
the GUI (not on the printed page).
*/
private JCheckBox __legendVisibleJCheckBox = null;

/**
The combo box that holds the names of the different layouts defined for
a GeoView Project.  Currently there is only a single layout and it cannot be
saved.
*/
private SimpleJComboBox __legendComboBox = null;

/**
The combo box that holds the locations in which the legend can be positioned
on the map.
*/
private SimpleJComboBox __legendLocationComboBox = null;

/**
Constructor.  This constructor is used to show the properties for a 
GeoViewJComponent display, not particular layers.
@param parent JFrame from which this object is created.
@param geoview GeoViewJComponent that is displaying the data.
@param gvp GeoViewProject containing overall GeoView information.
@param parent__geoviewJPanel GeoViewJPanel that is used for the display.
*/
public GeoViewPropertiesJFrame (JFrame parent, GeoViewJComponent geoview,
GeoViewProject gvp, GeoViewJPanel parent__geoviewJPanel)
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	__gvp = gvp;
	__geoview = geoview;
	__parentGeoViewJPanel = parent__geoviewJPanel;

	addWindowListener(this);

	__geoViewOnly = true;

	setupGUI();
}

/**
Constructor.  This constructor is used to show the properties for 
particular layers in a GeoView display.
@param parent JFrame from which this object is created.
@param layer_view GeoLayerView whose properties are being edited.
@param geoview GeoViewJComponent that is displaying the data.
@param gvp GeoViewProject containing overall GeoView information.
@param parent__geoviewJPanel GeoViewPanel that is used for the display.
*/
public GeoViewPropertiesJFrame (	JFrame parent, GeoLayerView layer_view,
					GeoViewJComponent geoview,
					GeoViewProject gvp,
					GeoViewJPanel parent__geoviewJPanel )
{	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	__layerView = layer_view;
	__gvp = gvp;
	__geoview = geoview;
	__parentGeoViewJPanel = parent__geoviewJPanel;

	addWindowListener( this );

	__geoViewOnly = false;
	
	setupGUI();
}

/**
This function responds to ActionEvents.
@param evt ActionEvent object.
*/
public void actionPerformed( ActionEvent evt )
{	String command = evt.getActionCommand();
	Object obj = evt.getSource();

/*
	if ( command.equals( "BrowseForDefaultGVP") ) {
		browseForGVP();
	}
*/
	if ( obj == __clearLabelField_JButton ) {
		// Clear the label and format fields..
		__labelFieldJTextField.setText("");
		__labelFormatJTextField.setText("");
	}
	else if (obj == __legendVisibleJCheckBox) {
		// show or hide the legend on the map depending on whether
		// the checkbox is selected or not
		if (__legendVisibleJCheckBox.isSelected()) {
			// the checkbox was just selected
			__geoview.setDrawLegend(true);
		}
		else {
			// the checkbox was deselected
			__geoview.setDrawLegend(false);
		}
	}
	else if (obj == __legendLocationComboBox) {
		// move the legend depending on the value selected from the 
		// combo box.  Positioning is done based on 
		// North/South/East/West locations, since it is being overlain
		// on a map.
		String pos = __legendLocationComboBox.getSelected();
		GeoViewLegendLayout layout = __geoview.getLegendLayout();	
		if (pos.equalsIgnoreCase("NorthWest")) {
			layout.setPosition(GridBagConstraints.NORTHWEST);
		}
		else if (pos.equalsIgnoreCase("NorthEast")) {
			layout.setPosition(GridBagConstraints.NORTHEAST);
		}
		else if (pos.equalsIgnoreCase("SouthWest")) {
			layout.setPosition(GridBagConstraints.SOUTHWEST);
		}
		else if (pos.equalsIgnoreCase("SouthEast")) {
			layout.setPosition(GridBagConstraints.SOUTHEAST);
		}
		__geoview.redraw(true);
	}
	else if (obj instanceof JCheckBox) {
		// the above check catches all the checkboxes that are
		// shown for deciding which layers to list in the legend.  If
		// any other checkboxes are listened for in the future, they 
		// must be listed explicitly above this check.
		legendCheckBoxChecked((JCheckBox)obj);
	}
	else if ( command.equals(__BUTTON_APPLY) ) {
		applyProperties();
	}
	else if ( command.equals(__BUTTON_CANCEL) ) {
		close();
	}
	else if ( command.equals(__BUTTON_OK) ) {
		applyProperties();
		close();
	}
	else if (obj == __labelFieldJComboBox ) {
		// Add to the label field.  If the text field is not empty, add
		// a command and then the new field...
		if ( __labelFieldJTextField.getText().equals("") ) {
			__labelFieldJTextField.setText(
			(String)__labelFieldJComboBox.getSelectedItem());
		}
		else {	
			__labelFieldJTextField.setText(
			__labelFieldJTextField.getText() + "," +
			__labelFieldJComboBox.getSelectedItem());
		}
	}
	
}

/**
Apply the properties in the dialog to the layer view.  This changes the
properties in the layer view's symbol, etc. but does not resave the
GeoViewProject file.
*/
private void applyProperties()
{	boolean dirty = false;		// Indicates if anything has changed.
	String prop_value = null;	// Use as a generic string.

	if (__layerView == null) {
		// the properties were opened for the geo view as a whole.
		// Currently, the only properties that can be changed for
		// the GeoView are for the legend, and 1) they cannot be
		// saved yet, and 2) the changes are applied immediately
		// so simply return for now.
		return;
	}
	
	GRSymbol symbol = __layerView.getSymbol();

	Message.printStatus ( 1, "", "Applying GeoView properties." );

	// Check the GeoView Panel...

	// Check the Layer panel...

	// Check the Symbol panel...

	// Check the Label panel...

	prop_value = __labelFieldJTextField.getText();
	if ( !prop_value.equals(symbol.getLabelField()) ) {
		symbol.setLabelField ( prop_value );
		dirty = true;
	}
	prop_value = __labelFormatJTextField.getText();
	if ( !prop_value.equals(symbol.getLabelFormat()) ) {
		symbol.setLabelFormat ( prop_value );
		dirty = true;
	}
	boolean b = __labelSelectedJCheckBox.isSelected();
	if ( b != symbol.labelSelectedOnly() ) {
		symbol.labelSelectedOnly ( b );
		dirty = true;
	}

	// Check the Application panel...

	// Check the Animation panel...

	// If anything has changed, redraw the GeoView, but only if the layer
	// view is currently visible...

	if ( dirty && __layerView.isVisible() ) {
		// Redraw the geoview...
		JGUIUtil.setWaitCursor ( this, true );
		__geoview.redraw();
		JGUIUtil.setWaitCursor ( this, false );
	}

	// Clean up...

	symbol = null;
	prop_value = null;
}

/**
Browse for a default GVP file.  When the file is selected, set in the
_defaultGVPJTextField.
*/
/*
private void browseForLayerFile ()
{	FileDialog fd = new FileDialog(this, "Select GeoView Project File",
	FileDialog.LOAD);
	fd.setFile("*.gvp");
	String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
	if ( last_directory_selected != null ) {
		fd.setDirectory( last_directory_selected );
	}
	fd.setVisible(true);

	// Return if no file name is selected...

	if ( fd.getFile() == null || fd.getFile().equals("") ) {
		return;
	}
	if ( fd.getDirectory() != null ) {
		JGUIUtil.setLastFileDialogDirectory(fd.getDirectory());
	}

	String fileName = fd.getDirectory() + fd.getFile();        

	if (fileName != null) {
		_defaultGVPJTextField.setText ( fileName );
	}
	// Tell main GUI what the file is.  Do this here because this is when
	// a file is going to change.  Don't do it every time OK is clicked...
	fd = null;
	last_directory_selected = null;
	__parent_gui.openGVP ( fileName );
	fileName = null;
}
*/

/**
Close the GUI.  It is assumed that properties have already been applied if
appropriate.
*/
public void close ()
{	setVisible( false );	
	dispose();
}

/**
Clean up for garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	// Shared...
	
	__layerView = null;
	__gvp = null;
	__geoview = null;

	// Geoview...

	__backgroundColorJTextField = null;
	__dataHomeJTextField = null;
	__initialExtentJTextField = null;
	__maximumExtentJTextField = null;
	__projectionJTextField = null;
	__selectColorJTextField = null;

	// Layer...

	__layerFileJTextField = null;
	__layerFormatJTextField = null;
	__layerProjectionJTextField = null;
	__numShapesJTextField = null;
	__shapeTypeJTextField = null;

	// Symbol...

	__symbolJPanel = null;
	__singleSymbolJRadioButton = null;
	__uniqueValuesJRadioButton = null;
	__classBreaksJRadioButton = null;
	__singleSymbolJPanel = null;
	__singleLegendJPanel = null;
	__uniqueValuesJPanel = null;
	__uniqueValuesFieldJComboBox = null;
	__classBreaksJPanel = null;
	__classBreakFieldJComboBox = null;

	__labelFieldJComboBox = null;
	__labelFieldJTextField = null;
	__labelFormatJTextField = null;
	__clearLabelField_JButton = null;
	__labelSelectedJCheckBox = null;

	// App...

	__appJoinFieldJTextField = null;
	__appLayerTypeJTextField = null;

	// Bottom...

	__okJButton = null;
	__cancelJButton = null;
	__applyJButton = null;

	__theJTabbedPane = null;

	__delLayoutButton = null;
	__legendVisibleJCheckBox = null;
	__legendComboBox = null;
	__legendLocationComboBox = null;
	
	__parentGeoViewJPanel = null;
	__scaledSymbolJPanel = null;
	__scaledSymbolJRadioButton = null;
	__theJTabbedPane = null;

	super.finalize();
}

/**
Respond to ItemEvents.
@param evt ItemEvent object.
*/
public void itemStateChanged( ItemEvent evt )
{	Object o = evt.getItemSelectable();

	if (!__doneInitializing) {
		return;
	}

	if ( o == __classBreaksJRadioButton ) {
		if ( __classBreaksJRadioButton.isSelected() ) {
			Message.printStatus ( 1, "",
			"Setting class breaks panel visible." );
			__singleSymbolJPanel.setVisible(false);
			__uniqueValuesJPanel.setVisible(false);
			__classBreaksJPanel.setVisible(true);
			__scaledSymbolJPanel.setVisible(false);
			invalidate();
			repaint();
		}
	}
	else if ( o == __labelFieldJComboBox ) {
		if (evt.getStateChange() == ItemEvent.DESELECTED) {
			// item events are fired twice when something is
			// selected from a JComboBox.  Once for the item
			// that is DEselected, again for the item that is
			// selected.  Ignore deselection events.
			return;
		}
		// Add to the label field.  If the text field is not empty, add
		// a command and then the new field...
		if ( __labelFieldJTextField.getText().equals("") ) {
			__labelFieldJTextField.setText(
			(String)__labelFieldJComboBox.getSelectedItem());
		}
		else {	
			__labelFieldJTextField.setText(
			__labelFieldJTextField.getText() + "," +
			__labelFieldJComboBox.getSelectedItem());
		}
	}
	else if ( o == __scaledSymbolJRadioButton ) {
		if ( __scaledSymbolJRadioButton.isSelected() ) {
			__singleSymbolJPanel.setVisible(false);
			__uniqueValuesJPanel.setVisible(false);
			__classBreaksJPanel.setVisible(false);
			__scaledSymbolJPanel.setVisible(true);
			invalidate();
			repaint();
		}
	}
	else if ( o == __singleSymbolJRadioButton ) {
		if ( __singleSymbolJRadioButton.isSelected() ) {
			__singleSymbolJPanel.setVisible(true);
			__uniqueValuesJPanel.setVisible(false);
			__classBreaksJPanel.setVisible(false);
			__scaledSymbolJPanel.setVisible(false);
			invalidate();
			repaint();
		}
	}
	else if ( o == __uniqueValuesJRadioButton ) {
		if ( __uniqueValuesJRadioButton.isSelected() ) {
			__singleSymbolJPanel.setVisible(false);
			__uniqueValuesJPanel.setVisible(true);
			__classBreaksJPanel.setVisible(false);
			__scaledSymbolJPanel.setVisible(false);
			invalidate();
			repaint();
		}
	}
	o = null;
}

/**
Called when one of the checkboxes in the legend setup tab is pressed.  Finds
the checkbox that was clicked and turns on or off the associated layer in 
the legend appropriately.
@param cb the checkbox that was clicked.
*/
private void legendCheckBoxChecked(JCheckBox cb) {
	boolean visible = cb.isSelected();
	
	GeoViewLegendLayout layout = __geoview.getLegendLayout();
	
	int num = layout.findCheckBox(cb);

	if (num == -1) {
		return;
	}

	layout.setLayerLegendVisible(num, visible);

	__geoview.redraw(true);
}

/**
Sets up the GUI.
*/
private void setupGUI() {
        // objects to be used in the GUI layout
	int b = 2;
	Insets TNNN = new Insets(b,0,0,0);
	Insets NNNN = new Insets(0,0,0,0);
        Insets TLBR = new Insets(b,b,b,b);        
	Insets NLNN = new Insets(0,b,0,0);
        Insets TLBN = new Insets(b,b,b,0);
        Insets TLNN = new Insets(b,b,0,0);
	Insets TNBR = new Insets(b,0,b,b);
        GridBagLayout gbl = new GridBagLayout();

	String prop_value = null;
	int y = 0;
	int x = 0;
	JLabel label = null;

	JPanel geoviewJPanel = null;
	JPanel layerJPanel = null;
	JPanel symbolJPanel = null;
	BorderJPanel labelJPanel = null;
	BorderJPanel appJPanel = null;
	BorderJPanel animateJPanel = null;

        //---------------------------------------------------------------------
        // geoviewJPanel
        //---------------------------------------------------------------------
	if (__geoViewOnly) {
	geoviewJPanel = new BorderJPanel();
        geoviewJPanel.setLayout ( gbl );
	label = new JLabel(
		"Properties for the GeoView window (currently view only):" );
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, y, 10, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Data Home:" );
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.GeoDataHome");
	if ( prop_value == null ) {
		__dataHomeJTextField = new JTextField ( 30 );
	}
	else {	
		__dataHomeJTextField = new JTextField ( prop_value, 30 );
	}
	__dataHomeJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __dataHomeJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Projection:");
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.Projection");
	if ( prop_value == null ) {
		__projectionJTextField = new JTextField ( 30 );
	}
	else {	
		__projectionJTextField = new JTextField ( prop_value, 30 );
	}
	__projectionJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __projectionJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Background Color:");
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.Color");
	if ( prop_value == null ) {
		__backgroundColorJTextField = new JTextField ( 30 );
	}
	else {	__backgroundColorJTextField = new JTextField( prop_value, 30 );
	}
	__backgroundColorJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __backgroundColorJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Select Color:");
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.SelectColor");
	if ( prop_value == null ) {
		__selectColorJTextField = new JTextField ( 30 );
	}
	else {	__selectColorJTextField = new JTextField ( prop_value, 30 );
	}
	__selectColorJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __selectColorJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Initial Extent:");
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.InitialExtent");
	if ( prop_value == null ) {
		__initialExtentJTextField = new JTextField ( 30 );
	}
	else {	__initialExtentJTextField = new JTextField ( prop_value, 30 );
	}
	__initialExtentJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __initialExtentJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Maximum Extent:");
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __gvp.getPropList().getValue("GeoView.MaximumExtent");
	if ( prop_value == null ) {
		__maximumExtentJTextField = new JTextField ( 30 );
	}
	else {	__maximumExtentJTextField = new JTextField ( prop_value, 30 );
	}
	__maximumExtentJTextField.setEditable(false);
        JGUIUtil.addComponent( geoviewJPanel, __maximumExtentJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Number of Layer Views:" );
        JGUIUtil.addComponent( geoviewJPanel, label,
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	label =	new JLabel ( "" + __geoview.getNumLayerViews());
        JGUIUtil.addComponent( geoviewJPanel, label,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	
	GeoLayer layer = null;
	GRSymbol symbol = null;
	GRLegend legend = null;
	int shape_type = -1;
	
	JPanel layoutJPanel = null;
	
	if (!__geoViewOnly) { 
	
        //---------------------------------------------------------------------
        // layerJPanel
        //---------------------------------------------------------------------
	layerJPanel = new BorderJPanel();
        layerJPanel.setLayout(gbl);
	layer = __layerView.getLayer();
	symbol = __layerView.getSymbol();
	legend = __layerView.getLegend();
	y = 0;
        JGUIUtil.addComponent( layerJPanel, new JLabel (
		"Properties for the data layer (currently view only):" ),
		0, y, 10, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Name:" ),
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField __layerView_nameJTextField = null;
	prop_value = legend.getText();
	if ( prop_value == null ) {
		__layerView_nameJTextField = new JTextField ( 30 );
	}
	else {	
		__layerView_nameJTextField = new JTextField ( prop_value );
	}
	__layerView_nameJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __layerView_nameJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Data file:" ),
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __layerView.getLayer().getFileName();
	if ( prop_value == null ) {
		__layerFileJTextField = new JTextField ( 45 );
	}
	else {	__layerFileJTextField = new JTextField ( prop_value, 30 );
	}
	__layerFileJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __layerFileJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Data format:" ),
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = layer.getDataFormat();
	if ( prop_value == null ) {
		__layerFormatJTextField = new JTextField ( 30 );
	}
	else {	
		__layerFormatJTextField = new JTextField ( prop_value, 30 );
	}
	__layerFormatJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __layerFormatJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Shape type:" ),
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	shape_type = layer.getShapeType();
	if ( shape_type == GeoLayer.UNKNOWN ) {
		prop_value = "Unknown";
	}
	else if ( shape_type == GeoLayer.POINT ) {
		prop_value = "Point";
	}
	else if ( shape_type == GeoLayer.LINE ) {
		prop_value = "Line";
	}
	else if ( shape_type == GeoLayer.POLYGON ) {
		prop_value = "Polygon";
	}
	else if ( shape_type == GeoLayer.GRID ) {
		prop_value = "Grid";
	}
	else {	
		prop_value = "Undefined";
	}
	__shapeTypeJTextField = new JTextField ( prop_value );
	__shapeTypeJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __shapeTypeJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Number of features:"),
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = "" + layer.getShapes().size();
	__numShapesJTextField = new JTextField ( prop_value, 7 );
	__numShapesJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __numShapesJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( layerJPanel, new JLabel ( "Projection:"), 
		0, ++y, 4, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = "";
	GeoProjection projection = layer.getProjection();
	if ( projection != null ) {
		prop_value = projection.getProjectionName();
	}
	if ( prop_value == null ) {
		__layerProjectionJTextField = new JTextField ( 30 );
	}
	else {	
		__layerProjectionJTextField = new JTextField ( prop_value, 30);
	}
	__layerProjectionJTextField.setEditable(false);
        JGUIUtil.addComponent( layerJPanel, __layerProjectionJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        //---------------------------------------------------------------------
        // symbolJPanel
        //---------------------------------------------------------------------
	symbolJPanel = new BorderJPanel();
        symbolJPanel.setLayout( gbl );
	y = 0;

	// Panel for class checkboxes...

	JPanel symbol_classJPanel = new JPanel();
	symbol_classJPanel.setLayout ( gbl );
	ButtonGroup class_Group = new ButtonGroup();

	y = 0;
	int classification_type =
		__layerView.getSymbol().getClassificationType();
	JGUIUtil.addComponent( symbol_classJPanel,
		new JLabel ( "Classification type:"),
		0, y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	__singleSymbolJRadioButton = new JRadioButton( 
		"Single symbol", false);
	class_Group.add(__singleSymbolJRadioButton);
	__singleSymbolJRadioButton.addItemListener ( this );
	if ( classification_type == GRSymbol.CLASSIFICATION_SINGLE ) {
		__singleSymbolJRadioButton.setSelected(true);
	}
	JGUIUtil.addComponent( symbol_classJPanel, __singleSymbolJRadioButton, 
		0, ++y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__singleSymbolJRadioButton.setEnabled ( false );

	__uniqueValuesJRadioButton = new JRadioButton( 
		"Unique values", false);
	class_Group.add(__uniqueValuesJRadioButton);
	__uniqueValuesJRadioButton.addItemListener ( this );
	if ( classification_type == GRSymbol.CLASSIFICATION_UNIQUE_VALUES ) {
		__uniqueValuesJRadioButton.setSelected(true);
	}
	JGUIUtil.addComponent( symbol_classJPanel, __uniqueValuesJRadioButton, 
		0, ++y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__uniqueValuesJRadioButton.setEnabled ( false );

	__classBreaksJRadioButton = new JRadioButton( 
		"Class breaks", false);
	class_Group.add(__classBreaksJRadioButton);
	__classBreaksJRadioButton.addItemListener ( this );
	if ( classification_type == GRSymbol.CLASSIFICATION_CLASS_BREAKS ) {
		__classBreaksJRadioButton.setSelected (true);
	}
	__classBreaksJRadioButton.setEnabled ( false );
	JGUIUtil.addComponent( symbol_classJPanel, __classBreaksJRadioButton, 
		0, ++y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( symbolJPanel, symbol_classJPanel,
		0, 0, 3, 1, 0, 0, TLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	__scaledSymbolJRadioButton = new JRadioButton( 
		"Scaled symbol", false);
	class_Group.add(__scaledSymbolJRadioButton);
	__scaledSymbolJRadioButton.addItemListener ( this );
	if ( classification_type == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
		__scaledSymbolJRadioButton.setSelected(true);
	}
	JGUIUtil.addComponent( symbol_classJPanel, __scaledSymbolJRadioButton, 
		0, ++y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__scaledSymbolJRadioButton.setEnabled ( false );

/* Later - idea is to allow symbol class breaks on any layer type...
	Checkbox _symbol__classBreaksJRadioButton = new Checkbox( 
		"Class breaks (symbols)", false, class_Group );
	_symbol__classBreaksJRadioButton.addItemListener ( this );
	_symbol__classBreaksJRadioButton.setEnabled ( false );
	JGUIUtil.addComponent( symbol_classJPanel,
		_symbol__classBreaksJRadioButton, 
		0, ++y, 1, 1, 0, 0, NNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
*/

        JGUIUtil.addComponent( symbolJPanel, symbol_classJPanel,
		0, 0, 3, 1, 0, 0, TLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Panel for notes about symbols...

	JPanel symbol_textJPanel = new JPanel();
	symbol_textJPanel.setLayout ( gbl );
        JGUIUtil.addComponent( symbol_textJPanel, new JLabel (
		"Symbols used to draw map features depend on the layer shape" ),
		0, y, 10, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( symbol_textJPanel, new JLabel (
		"type and classification.  A single symbol is the default." ),
		0, ++y, 10, 1, 0, 0, NLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( symbol_textJPanel, new JLabel (
		"Classification requires that a data attribute be selected." ),
		0, ++y, 10, 1, 0, 0, NLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( symbolJPanel, symbol_textJPanel,
		3, 0, 7, 1, 0, 0, TLBR, GridBagConstraints.NONE, GridBagConstraints.NORTH );

	// Now add a panel for each classification type (only set visible the
	// panel that applies).

	// ----------------------------------
	// Panel for single symbol...
	// ----------------------------------

	__singleSymbolJPanel = new JPanel();
	__singleSymbolJPanel.setLayout ( gbl );
	y = 0;
        JGUIUtil.addComponent(__singleSymbolJPanel,new JLabel ("Symbol style:"),
		0, y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JComboBox _symbol_styleJComboBox = new JComboBox();
	_symbol_styleJComboBox.setEnabled ( false );

	if (	(shape_type == GeoLayer.POINT) ||
		(shape_type == GeoLayer.MULTIPOINT) ) {
		// Add list of recognized GRSymbol types...
		JGUIUtil.addToJComboBox ( _symbol_styleJComboBox,
			GRSymbol.SYMBOL_NAMES );
		// Select the one that is in use...
		_symbol_styleJComboBox.setSelectedItem (
			GRSymbol.toString(symbol.getStyle()) );
	}
	else if ( shape_type == GeoLayer.LINE ) {
		// Currently only offer "Solid"...
		_symbol_styleJComboBox.addItem ( "Solid" );
	}
	else if ( shape_type == GeoLayer.POLYGON ) {
		// Currently only offer "FillSolid".  If transparent, use the
		// color to indicate.
		_symbol_styleJComboBox.addItem ( "FillSolid" );
	}
	else {	
		_symbol_styleJComboBox = null;
	}
	if ( _symbol_styleJComboBox != null ) {
        	JGUIUtil.addComponent( __singleSymbolJPanel,
		_symbol_styleJComboBox,
		1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

        JGUIUtil.addComponent( __singleSymbolJPanel, new JLabel ( "Color:"),
		0, ++y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField _single_colorJTextField = new JTextField (4);
	_single_colorJTextField.setEditable ( false );
	if ( symbol.getColor().isTransparent() ) {
		_single_colorJTextField.setText( "None");
	}
	else {	
		_single_colorJTextField.setBackground ( symbol.getColor() );
	}
        JGUIUtil.addComponent( __singleSymbolJPanel, _single_colorJTextField,
		1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	if ( shape_type == GeoLayer.POLYGON ) {
        	JGUIUtil.addComponent( __singleSymbolJPanel, new JLabel (
			"Outline color:"),
			0, ++y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		JTextField _single_outline_colorJTextField = new JTextField(4);
		if ( symbol.getOutlineColor().isTransparent() ) {
			_single_outline_colorJTextField.setText( "None");
		}
		else {	_single_outline_colorJTextField.setBackground (
			symbol.getOutlineColor() );
		}
        	JGUIUtil.addComponent( __singleSymbolJPanel,
			_single_outline_colorJTextField,
			1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		_single_outline_colorJTextField = null;
	}
	if (	(shape_type == GeoLayer.POINT) ||
		(shape_type == GeoLayer.LINE) ) {
        	JGUIUtil.addComponent( __singleSymbolJPanel, new JLabel (
		"Size:"),
		0, ++y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
		JTextField _symbol_sizeJTextField = new JTextField ( 5 );
		_symbol_sizeJTextField.setEditable ( false );
		_symbol_sizeJTextField.setBackground ( Color.lightGray );
		_symbol_sizeJTextField.setText (
			StringUtil.formatString(symbol.getSize(),"%.0f") );
        	JGUIUtil.addComponent( __singleSymbolJPanel,
			_symbol_sizeJTextField,
			1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		_symbol_sizeJTextField = null;
	}
	y = 0;
        JGUIUtil.addComponent( __singleSymbolJPanel, new JLabel (
		"Single symbol:" ),
		2, y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JPanel single_legend_panel = new GeoLayerViewLegendJPanel(
		__layerView, false);
	single_legend_panel.setBackground ( Color.white );
        JGUIUtil.addComponent( __singleSymbolJPanel, 
		new JScrollPane(single_legend_panel),
		2, ++y, 1, 9, 1, 1, TLNN, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	single_legend_panel = null;
        JGUIUtil.addComponent( symbolJPanel, __singleSymbolJPanel,
		0, 1, 10, 1, 0, 0, TNNN, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH );
	if ( classification_type == GRSymbol.CLASSIFICATION_SINGLE ) {
		__singleSymbolJPanel.setVisible(true);
	}
	else {	__singleSymbolJPanel.setVisible(false);
	}

	// ----------------------------------
	// Panel for unique values...
	// ----------------------------------

	__uniqueValuesJPanel = new JPanel();
	__uniqueValuesJPanel.setLayout ( gbl );

        JGUIUtil.addComponent( symbolJPanel, __uniqueValuesJPanel,
		3, 1, 7, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	__uniqueValuesJPanel.setBackground(Color.red);
	if ( classification_type == GRSymbol.CLASSIFICATION_UNIQUE_VALUES ) {
		__uniqueValuesJPanel.setVisible(true);
	}
	else {	__uniqueValuesJPanel.setVisible(false);
	}
	y = 0;
        JGUIUtil.addComponent( __uniqueValuesJPanel, new JLabel ("Symbol:"),
		0, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
        JGUIUtil.addComponent( __uniqueValuesJPanel, new JLabel ("Field:"),
		1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
        JGUIUtil.addComponent( __uniqueValuesJPanel,
		new GeoLayerViewLegendJPanel(__layerView, false),
		0, ++y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__uniqueValuesFieldJComboBox = new JComboBox ();
	__uniqueValuesFieldJComboBox.addItem( "Junk" );
        JGUIUtil.addComponent(__uniqueValuesJPanel, 
		__uniqueValuesFieldJComboBox,
		1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( symbolJPanel, __uniqueValuesJPanel,
		0, 1, 10, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	if ( classification_type == GRSymbol.CLASSIFICATION_UNIQUE_VALUES ) {
		__uniqueValuesJPanel.setVisible(true);
	}
	else {	__uniqueValuesJPanel.setVisible(false);
	}

	// ----------------------------------
	// Panel for class breaks...
	// ----------------------------------

	__classBreaksJPanel = new JPanel();
	__classBreaksJPanel.setLayout ( gbl );
	y = 0;
        JGUIUtil.addComponent( __classBreaksJPanel, new JLabel (
		"Classification Field:"),
		0, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__classBreakFieldJComboBox = new JComboBox ();
	__classBreakFieldJComboBox.setEnabled(false);
	JGUIUtil.addToJComboBox ( __classBreakFieldJComboBox,
		layer.getAttributeTable().getFieldNames() );
	__classBreakFieldJComboBox.setSelectedItem ( 
		symbol.getClassificationField() );
        JGUIUtil.addComponent(__classBreaksJPanel, __classBreakFieldJComboBox,
		1, y, 3, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( __classBreaksJPanel, new JLabel (
			"Number of classes:"),
			0, ++y, 3, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField _numclassesJTextField = new JTextField(3);
	_numclassesJTextField.setText ( "" +
		symbol.getNumberOfClassifications() );
	_numclassesJTextField.setEditable(false);
	_numclassesJTextField.setBackground(Color.lightGray);
	JGUIUtil.addComponent( __classBreaksJPanel,
		_numclassesJTextField,
		3, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

	// Insert color table.

	JPanel colorJPanel = new JPanel();
	colorJPanel.setLayout ( gbl );
	int yc = 0;
	JGUIUtil.addComponent( colorJPanel, new JLabel("Symbol color:"),
		0, yc, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	ButtonGroup colors_group = new ButtonGroup();
	JRadioButton _color_tableJRadioButton = new JRadioButton(
		"Color Table:", true);
	colors_group.add(_color_tableJRadioButton);
	JGUIUtil.addComponent( colorJPanel, _color_tableJRadioButton, 
		0, ++yc, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JComboBox _color_tableJComboBox = new JComboBox();
	_color_tableJComboBox.setEnabled ( false );
	GRColorTable color_table = symbol.getColorTable();
	JGUIUtil.addToJComboBox ( _color_tableJComboBox,
		GRColorTable.COLOR_TABLE_NAMES );
	if ( color_table != null ) {
		_color_tableJComboBox.setSelectedItem( color_table.getName());
	}
	JGUIUtil.addComponent( colorJPanel, _color_tableJComboBox, 
		2, yc, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JRadioButton _color_rampJRadioButton = new JRadioButton(
		"Color ramp:", false);
	colors_group.add(_color_rampJRadioButton);
	_color_rampJRadioButton.setEnabled ( false );
	JGUIUtil.addComponent( colorJPanel, _color_rampJRadioButton, 
		0, ++yc, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JGUIUtil.addComponent( colorJPanel, new JLabel("Start:"),
		2, yc, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField _color_ramp1JTextField = new JTextField(5);
	_color_ramp1JTextField.setEnabled(false);
	JGUIUtil.addComponent( colorJPanel, _color_ramp1JTextField, 
		3, yc, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent( colorJPanel, new JLabel("End:"),
		2, ++yc, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField _color_ramp2JTextField = new JTextField(5);
	_color_ramp2JTextField.setEnabled(false);
	JGUIUtil.addComponent( colorJPanel, _color_ramp2JTextField, 
		3, yc, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JRadioButton _custom_colorsJRadioButton = new JRadioButton(
		"Custom Colors", false);
	colors_group.add(_custom_colorsJRadioButton);
	JGUIUtil.addComponent( colorJPanel, _custom_colorsJRadioButton, 
		0, ++yc, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	_custom_colorsJRadioButton.setEnabled ( false );

        JGUIUtil.addComponent( __classBreaksJPanel, colorJPanel,
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	colorJPanel = null;

	// Insert symbol size.

	JPanel sizeJPanel = new JPanel();
	sizeJPanel.setLayout ( gbl );
	if ( shape_type == GeoLayer.POINT ) {
		// Later add line.
		int ys = 0;
        	JGUIUtil.addComponent( sizeJPanel, new JLabel("Symbol size:"),
			0, ys, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		JGUIUtil.addComponent( sizeJPanel, new JLabel("Start:"),
			2, ys, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
		JTextField _size_ramp1JTextField = new JTextField(5);
		_size_ramp1JTextField.setEnabled(false);
		JGUIUtil.addComponent( sizeJPanel, _size_ramp1JTextField, 
			3, ys, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
		_size_ramp1JTextField = null;
		JGUIUtil.addComponent( sizeJPanel, new JLabel("End:"),
			2, ++ys, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
		JTextField _size_ramp2JTextField = new JTextField(5);
		_size_ramp2JTextField.setEnabled(false);
		JGUIUtil.addComponent( sizeJPanel, _size_ramp2JTextField, 
			3, ys, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
        	JGUIUtil.addComponent( __classBreaksJPanel, sizeJPanel,
			0, ++y, 4, 1, 1, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		_size_ramp2JTextField = null;
	}
	
	// Class breaks legend (show effects of symbol, color table, and size)..

	y = 0;
        JGUIUtil.addComponent( __classBreaksJPanel, new JLabel (
		"Class breaks:" ),
		4, y, 6, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	GeoLayerViewLegendJPanel legendJPanel = new GeoLayerViewLegendJPanel(
		__layerView, false);
	legendJPanel.setBackground ( Color.white );
        JGUIUtil.addComponent( __classBreaksJPanel, 
		new JScrollPane(legendJPanel), 
		4, ++y, 6, 9, 1, 1, TLNN, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	legendJPanel = null;

	// Add the whole class breaks panel...
        JGUIUtil.addComponent( symbolJPanel, __classBreaksJPanel,
		0, 1, 10, 1, 1, 1, TNNN, GridBagConstraints.BOTH, GridBagConstraints.SOUTH );
	if ( classification_type == GRSymbol.CLASSIFICATION_CLASS_BREAKS ) {
		__classBreaksJPanel.setVisible(true);
	}
	else {	__classBreaksJPanel.setVisible(false);
	}

	// --------------------------------------------------------------------
	// Panel for scaled symbol...
	//
	// Layout as follows...
	//
	// +Color -Color Wid. Ht. AbsMaxData DisplayMax
	// +Color -Color Wid. Ht. AbsMaxData DisplayMax
	// +Color -Color Wid. Ht. AbsMaxData DisplayMax
	// --------------------------------------------------------------------

	__scaledSymbolJPanel = new JPanel();
	__scaledSymbolJPanel.setLayout ( gbl );
	y = 0;
        JGUIUtil.addComponent(__scaledSymbolJPanel, new JLabel("Symbol style:"),
		0, y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JComboBox __scaledSymbol_styleJComboBox = new JComboBox();
	__scaledSymbol_styleJComboBox.setEnabled ( false );
	if (	(shape_type == GeoLayer.POINT) ||
		(shape_type == GeoLayer.MULTIPOINT) ) {
		// Add list of recognized GRSymbol types...
		__scaledSymbol_styleJComboBox.addItem ( "VerticalBar-Signed" );
		// Select the one that is in use...
		__scaledSymbol_styleJComboBox.setSelectedItem(
			GRSymbol.toString(symbol.getStyle()) );
	}
/* Later
	else if ( shape_type == GeoLayer.LINE ) {
		// Currently only offer "Solid"...
		_symbol_styleJComboBox.add ( "Solid" );
	}
	else if ( shape_type == GeoLayer.POLYGON ) {
		// Currently only offer "FillSolid".  If transparent, use the
		// color to indicate.
		_symbol_styleJComboBox.add ( "FillSolid" );
	}
*/

	else {	__scaledSymbol_styleJComboBox = null;
	}
	if ( __scaledSymbol_styleJComboBox != null ) {
        	JGUIUtil.addComponent( __scaledSymbolJPanel,
		__scaledSymbol_styleJComboBox,
		1, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}

	// No restriction on the shape type since this symbol is drawn external
	// to the main symbol.  Loop through the number of symbols in the
	// legend and add the grid of output.

	JPanel scaled_symbol_detailJPanel = new JPanel();
	scaled_symbol_detailJPanel.setLayout ( gbl );

	// Add the headers...

	y = 0;
	x = 0;
       	JGUIUtil.addComponent(scaled_symbol_detailJPanel,new JLabel ( "+Color"),
		x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
       	JGUIUtil.addComponent(scaled_symbol_detailJPanel,new JLabel ( "-Color"),
		++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JGUIUtil.addComponent( scaled_symbol_detailJPanel, new JLabel("Width"),
		++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
       	JGUIUtil.addComponent( scaled_symbol_detailJPanel,new JLabel("Height"),
		++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
       	JGUIUtil.addComponent( scaled_symbol_detailJPanel,new JLabel("MaxData"),
		++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
       	JGUIUtil.addComponent(scaled_symbol_detailJPanel,
		new JLabel("MaxDisplay"),
		++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );

	// Now loop through each symbol in the legend and display its
	// information...
	int nsym = legend.size();

	for ( int isym = 0; isym < nsym; isym++ ) {
		symbol = legend.getSymbol(isym);
		++y;
		x = 0;

		// Color...

		JTextField __scaledSymbol_color1JTextField = new JTextField(3);
		__scaledSymbol_color1JTextField.setEditable ( false );
		__scaledSymbol_color1JTextField.setBackground(Color.lightGray);
		if ( symbol.getColor().isTransparent() ) {
			__scaledSymbol_color1JTextField.setText( "None");
		}
		else {	__scaledSymbol_color1JTextField.setBackground(
				symbol.getColor());
		}
        	JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_color1JTextField,
			x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
		// Color2...

		JTextField __scaledSymbol_color2JTextField = new JTextField(3);
		__scaledSymbol_color2JTextField.setEditable ( false );
		__scaledSymbol_color2JTextField.setBackground(Color.lightGray);
		if ( symbol.getColor2().isTransparent() ) {
			__scaledSymbol_color2JTextField.setText( "None");
		}
		else {	__scaledSymbol_color2JTextField.setBackground(
				symbol.getColor2());
		}
        	JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_color2JTextField,
			++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

		// Width...

		JTextField __scaledSymbol_sizexJTextField = new JTextField(2);
		__scaledSymbol_sizexJTextField.setEditable ( false );
		__scaledSymbol_sizexJTextField.setBackground( Color.lightGray);
		__scaledSymbol_sizexJTextField.setText (
			StringUtil.formatString(symbol.getSizeX(),"%.0f") );
       		JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_sizexJTextField,
			++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		__scaledSymbol_sizexJTextField = null;
	
		// Height...

		JTextField __scaledSymbol_sizeyJTextField = new JTextField(3);
		__scaledSymbol_sizeyJTextField.setEditable ( false );
		__scaledSymbol_sizeyJTextField.setBackground( Color.lightGray);
		__scaledSymbol_sizeyJTextField.setText (
			StringUtil.formatString(symbol.getSizeY(),"%.0f") );
       		JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_sizeyJTextField,
			++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		__scaledSymbol_sizeyJTextField = null;

		// Max Data...

		JTextField __scaledSymbol_maxdataJTextField =new JTextField(7);
		__scaledSymbol_maxdataJTextField.setEditable ( false );
		__scaledSymbol_maxdataJTextField.setBackground(
			Color.lightGray);

		if ( symbol.getType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL){
			__scaledSymbol_maxdataJTextField.setText (
			StringUtil.formatString(
			((GRScaledClassificationSymbol)
			symbol).getClassificationDataMax(),"%.2f") );
		}
       		JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_maxdataJTextField,
			++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		__scaledSymbol_maxdataJTextField = null;

		// Max display...

		JTextField __scaledSymbol_maxdisplayJTextField =
			new JTextField(7);
		__scaledSymbol_maxdisplayJTextField.setEditable ( false );
		__scaledSymbol_maxdisplayJTextField.setBackground(
			Color.lightGray );
		if ( symbol.getType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL){
			__scaledSymbol_maxdisplayJTextField.setText (
			StringUtil.formatString(
			((GRScaledClassificationSymbol)
			symbol).getClassificationDataDisplayMax(),"%.2f"));
		}
       		JGUIUtil.addComponent( scaled_symbol_detailJPanel,
			__scaledSymbol_maxdisplayJTextField,
			++x, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
		__scaledSymbol_maxdisplayJTextField = null;

	}

        JGUIUtil.addComponent(__scaledSymbolJPanel, scaled_symbol_detailJPanel,
		0, 1, 2, 8, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	
	y = 0;
        JGUIUtil.addComponent( __scaledSymbolJPanel, new JLabel (
		"Scaled Symbol:" ),
		2, y, 1, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JPanel scaled_legend_panel = new GeoLayerViewLegendJPanel(
		__layerView, false);
	scaled_legend_panel.setBackground ( Color.white );
        JGUIUtil.addComponent( __scaledSymbolJPanel, 
		new JScrollPane(scaled_legend_panel),
		2, ++y, 1, 9, 1, 1, TNNN, GridBagConstraints.BOTH, GridBagConstraints.WEST );
	scaled_legend_panel = null;
        JGUIUtil.addComponent( symbolJPanel, __scaledSymbolJPanel,
		0, 1, 10, 1, 0, 0, TNNN, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTH );

	if ( classification_type == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
		__scaledSymbolJPanel.setVisible(true);
	}
	else {	__scaledSymbolJPanel.setVisible(false);
	}

        //---------------------------------------------------------------------
        // labelJPanel
        //---------------------------------------------------------------------

	labelJPanel = new BorderJPanel();
	labelJPanel.setLayout( gbl );
	y = 0;
        JGUIUtil.addComponent( labelJPanel, new JLabel ( "Text labels can " +
		" be formatted from data fields.  Select one or more fields." ),
		0, y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent(labelJPanel, new JLabel( "Optionally, also use " +
		" the label format to specify how label fields should be" +
		" formatted." ),
		0, ++y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( labelJPanel, new JLabel ( "   Floating point " +
		"values:  Use %NN.Nf (e.g., %f, %.2f, %10.2f)." ),
		0, ++y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
//      JGUIUtil.addComponent( labelJPanel, new JLabel ( "   Integer values:  " 
//		+ "Use %NNd (e.g., %d, %2d, %02d)." ),
//		0, ++y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( labelJPanel, new JLabel( "   Integer values:  " +
		"Treat as 0-precision float (e.g., %.0f, %f, %10.0f)." ),
		0, ++y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
        JGUIUtil.addComponent( labelJPanel, new JLabel (
		"   String values:  Use %s (e.g., %s, %20s, %20.20s)." ),
		0, ++y, 7, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( labelJPanel, new JLabel("Label field(s):"),
		0, ++y, 2, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	__labelFieldJComboBox = new JComboBox ();
	JGUIUtil.addToJComboBox ( __labelFieldJComboBox,
		layer.getAttributeTable().getFieldNames() );
//	__labelFieldJComboBox.addItemListener ( this );
	__labelFieldJComboBox.addActionListener(this);
	prop_value = symbol.getLabelField();
	if ( prop_value != null ) {
		try {	__labelFieldJComboBox.setSelectedItem( prop_value );
		}
		catch ( Exception e ) {
		}
	}
        JGUIUtil.addComponent( labelJPanel, __labelFieldJComboBox,
		2, y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	prop_value = symbol.getLabelField ();
	if ( prop_value == null ) {
		__labelFieldJTextField = new JTextField ( 30 );
	}

	else {	
		__labelFieldJTextField = new JTextField ( prop_value, 30 );
	}
	__labelFieldJTextField.setEditable(false);
	__labelFieldJTextField.setBackground(Color.lightGray);
        JGUIUtil.addComponent( labelJPanel, __labelFieldJTextField,
		2, ++y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	__clearLabelField_JButton = new SimpleJButton("Clear","Clear",this);
        JGUIUtil.addComponent( labelJPanel, __clearLabelField_JButton,
		4, y, 1, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( labelJPanel, new JLabel (
		"Label format:" ),
		0, ++y, 2, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = symbol.getLabelFormat();
	if ( prop_value == null ) {
		__labelFormatJTextField = new JTextField ( 30 );
	}
	else {	__labelFormatJTextField = new JTextField ( prop_value, 30 );
	}
        JGUIUtil.addComponent( labelJPanel, __labelFormatJTextField,
		2, y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( labelJPanel, new JLabel (
		"Label font:" ),
		0, ++y, 2, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JComboBox __labelFontJComboBox = new JComboBox();
	__labelFontJComboBox.setEnabled ( false );
	__labelFontJComboBox.addItem ( "Helvetica" );
        JGUIUtil.addComponent( labelJPanel, __labelFontJComboBox,
		2, y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( labelJPanel, new JLabel (
		"Font size (points):" ),
		0, ++y, 2, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = StringUtil.formatString(
		symbol.getLabelFontHeight(),"%.1f");
	JTextField __labelFont_heightJTextField = null;
	if ( prop_value == null ) {
		__labelFont_heightJTextField = new JTextField ( 4 );
	}
	else {	__labelFont_heightJTextField = new JTextField ( prop_value, 4);
	}
	__labelFont_heightJTextField.setEditable(false);
	__labelFont_heightJTextField.setBackground(Color.lightGray);
        JGUIUtil.addComponent( labelJPanel, __labelFont_heightJTextField,
		2, y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        JGUIUtil.addComponent( labelJPanel, new JLabel (
		"Position:" ),
		0, ++y, 2, 1, 0, 0, TLNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JComboBox _label_positionJComboBox = new JComboBox();
	_label_positionJComboBox.setEnabled(false);
	JGUIUtil.addToJComboBox ( _label_positionJComboBox,
		GRText.getTextPositions() );
	_label_positionJComboBox.setSelectedItem(
		GRText.toString(symbol.getLabelPosition()));
        JGUIUtil.addComponent( labelJPanel, _label_positionJComboBox,
		2, y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

	__labelSelectedJCheckBox = new JCheckBox (
		"Label Selected Features Only", symbol.labelSelectedOnly() );
        JGUIUtil.addComponent( labelJPanel, __labelSelectedJCheckBox,
		0, ++y, 2, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        //---------------------------------------------------------------------
        // appJPanel
        //---------------------------------------------------------------------
	appJPanel = new BorderJPanel();
	appJPanel.setLayout( gbl );
	y = 0;
        label = new JLabel ( "Properties to link" +
		" data to the application (currently view only):");
        JGUIUtil.addComponent( appJPanel, label,
		0, y, 10, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel("Application layer type:");
        JGUIUtil.addComponent( appJPanel, label,
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = layer.getAppLayerType();
	if ( prop_value == null ) {
		__appLayerTypeJTextField = new JTextField ( 30 );
	}
	else {	__appLayerTypeJTextField = new JTextField ( prop_value, 30 );
	}
	__appLayerTypeJTextField.setEditable(false);
	__appLayerTypeJTextField.setBackground(Color.lightGray);
        JGUIUtil.addComponent( appJPanel, __appLayerTypeJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

        label = new JLabel ( "Application join field(s):" );
        JGUIUtil.addComponent( appJPanel, label,
		0, ++y, 4, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.EAST );
	prop_value = __layerView.getPropList().getValue("AppJoinField");
	if ( prop_value == null ) {
		__appJoinFieldJTextField = new JTextField ( 30 );
	}
	else {	__appJoinFieldJTextField = new JTextField ( prop_value, 30 );
	}
	__appJoinFieldJTextField.setEditable(false);
	__appJoinFieldJTextField.setBackground(Color.lightGray);
        JGUIUtil.addComponent( appJPanel, __appJoinFieldJTextField,
		4, y, 6, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );

	//---------------------------------------------------------------------
	// animateJPanel
	//---------------------------------------------------------------------
	animateJPanel = new BorderJPanel();
	animateJPanel.setLayout( gbl );
	y = 0;
	
	label = new JLabel("Properties for animation are under development." );
 	JGUIUtil.addComponent( animateJPanel, label,
		0, y, 10, 1, 0, 0, TNNN, GridBagConstraints.NONE, GridBagConstraints.WEST );
	}
	else {
	//---------------------------------------------------------------------
	// Legend Layout Panel (only available on the main Geo View Properties)
	//---------------------------------------------------------------------
	
		// __geoViewOnly == true

		layoutJPanel = new JPanel();
		layoutJPanel.setLayout(new GridBagLayout());

		y = 0;

		__legendVisibleJCheckBox = new JCheckBox((String)null);
		__legendVisibleJCheckBox.setSelected(__geoview.getDrawLegend());
		__legendVisibleJCheckBox.addActionListener(this);

		JGUIUtil.addComponent(layoutJPanel, new JLabel(
			"Legends are visible on-screen: "),
			0, y, 1, 1, 0, 0, TLBN,
			GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
		JGUIUtil.addComponent(layoutJPanel, __legendVisibleJCheckBox,
			1, y++, 1, 1, 0, 0, TLBN,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

		JGUIUtil.addComponent(layoutJPanel, new JLabel(
			"Layout Name: "),
			0, y, 1, 1, 0, 0, TLBN,
			GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
			
		List v = new Vector();
		v.add("Default");
		__legendComboBox = new SimpleJComboBox(v, true);
		JGUIUtil.addComponent(layoutJPanel, __legendComboBox,
			1, y++, 1, 1, 0, 0, TNBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);

		__delLayoutButton = new JButton(__BUTTON_DEL_LAYOUT);
		__delLayoutButton.addActionListener(this);
		JButton addLayoutButton = new JButton(__BUTTON_ADD_LAYOUT);
		addLayoutButton.addActionListener(this);

		JGUIUtil.addComponent(layoutJPanel, addLayoutButton,
			0, y, 1, 1, 0, 0, TLBR,
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		JGUIUtil.addComponent(layoutJPanel, __delLayoutButton,
			1, y++, 1, 1, 0, 0, TLBR,
			GridBagConstraints.NONE, GridBagConstraints.CENTER);
		
		JGUIUtil.addComponent(layoutJPanel, new JLabel(
			"Location: "),
			0, y, 1, 1, 0, 0, TNBR,
			GridBagConstraints.NONE, GridBagConstraints.NORTHEAST);
		v = new Vector();
		v.add("NorthWest");
		v.add("NorthEast");
		v.add("SouthWest");
		v.add("SouthEast");		

		__legendLocationComboBox = new SimpleJComboBox(v, false);
		JGUIUtil.addComponent(layoutJPanel, __legendLocationComboBox,
			1, y++, 1, 1, 0, 0, TLBN,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		__legendLocationComboBox.addActionListener(this);

		// the legend is drawn on the map by iterating through the 
		// tree, so links need made between the tree and the layers
		// listed in the legend so that they can be turned on or off
		// and thus appear (or not) in the legend on screen
		GeoViewLegendJTree legendJTree 
			= __parentGeoViewJPanel.getLegendJTree();

		// the following method returns an array of Vectors.  It's
		// not the cleanest method, but it's not called often and
		// guarantees that this method gets all the information it
		// needs to set up the layout object
		// [0] - the GeoLayerViews that are associated with each node
		//       in the tree
		// [1] - the layer names associated with each node in the tree
		// [2] - the actual nodes in the tree
		List[] vectors = legendJTree.getLayersNamesAndNodes(false);
		List layers = vectors[0];
		List names = vectors[1];
		List nodes = vectors[2];

		// all methods must be the same size (forced by the called 
		// method, so just get the size from one)
		int size = names.size();

		GeoLayerView glv = null;
		int num = -1;
		JCheckBox checkBox = null;
		JLabel nodeLabel = null;
		JPanel panel = null;
		SimpleJTree tree = new SimpleJTree();
		SimpleJTree_Node node = null;
		String name = null;

		// the following object should never be null. 
		GeoViewLegendLayout layout = __geoview.getLegendLayout();

		for (int i = 0; i < size; i++) {
			// pull out the values returned from the Vectors above
			glv = (GeoLayerView)layers.get(i);
			name = (String)names.get(i);
			checkBox = new JCheckBox((String)null);
			
			// the following panel is what will be displayed in 
			// the tree of layers that users can select to appear
			// in the legend or not.  The panel will contain a
			// check box on the left (for the user to show/hide
			// a layer in the legend) and the description of the
			// layer on the right
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			
			// finds the position within the internal layout Vectors
			// of the current GeoLayerView.  Returns -1 if the
			// GeoLayerView is not in the layout -- this means 
			// either 1) a new layer has been added to the display
			// since last the properties window was opened, or 2)
			// the layout has never been set up (is new) and simply
			// displays everything
			num = layout.findLayer(glv);
			
			if (num == -1 || layout.isLayerLegendVisible(glv)) {
				// set the layer visible in the legend if -1
				// because this either means it's a new legend
				// since last the layout was modified or
				// the layout has never been edited before
				checkBox.setSelected(true);
			}
			else {
				checkBox.setSelected(false);
			}
			
			if (legendJTree.isLayerVisible(i)) {
				checkBox.setSelected(true);
			}

			checkBox.addActionListener(this);

			// the nodeLabel is the text that will be shown
			// next to the checkbox -- contains the name of the 
			// layer
			nodeLabel = new JLabel("  " + name);

			// force the colors to match 
			checkBox.setBackground(tree.getBackground());
			nodeLabel.setBackground(tree.getBackground());	
			panel.setBackground(tree.getBackground());

			JGUIUtil.addComponent(panel, checkBox,
				0, 0, 1, 1, 0, 0, 
				GridBagConstraints.NONE,
				GridBagConstraints.EAST);
			JGUIUtil.addComponent(panel, nodeLabel,
				1, 0, 1, 1, 0, 0, 
				GridBagConstraints.NONE,
				GridBagConstraints.EAST);
			
			node = new SimpleJTree_Node(panel, name);

			if (num == -1) {
				// if the layer was not found in the layout,
				// add it and all its other associated control
				// information to the layout
				layout.addNodeLayerCheckBox( (SimpleJTree_Node)nodes.get(i), 
					glv, checkBox, checkBox.isSelected());
			}
			else {
				// if the layer was found in the layout, 
				// associate it with the new checkbox so that
				// actions occur properly
				layout.setCheckBox(num, checkBox);
			}
			
			try {
				tree.addNode(node);
			}
			catch (Exception e) {
				Message.printWarning(2, 
					"GeoViewPropertiesJFrame.setupGUI", e);
			}
		}
	
		tree.setVisibleRowCount(8);
		JGUIUtil.addComponent(layoutJPanel, new JScrollPane(tree),
			0, y++, 10, 1, 1, 0, TLBR,
			GridBagConstraints.HORIZONTAL, 
			GridBagConstraints.CENTER);
	}

        //---------------------------------------------------------------------
        // Add all Panels to the Tab Panel
        //---------------------------------------------------------------------
	layer = null;
	JTabbedPane tab = new JTabbedPane();
	//tab.setLayout( new FlowLayout(FlowLayout.LEFT) );
	if (__geoViewOnly) {
		tab.addTab( "GeoView", geoviewJPanel );
		tab.addTab("Layouts", layoutJPanel);
	}
	else {	tab.addTab( "Layer", layerJPanel );
		tab.addTab( "Symbol", symbolJPanel );
		tab.addTab( "Label" , labelJPanel );
		if ( shape_type != GeoLayer.POINT ) {
			labelJPanel.setEnabled ( false );
		}
		tab.addTab( "Application", appJPanel );
	       	tab.addTab( "Animation", animateJPanel );
		//tab.showTabPanel ( 1 );
	}
        getContentPane().add("Center", tab );

        //---------------------------------------------------------------------
        // Button Panel (shared between all Tabs)
        //---------------------------------------------------------------------
        JPanel buttonJPanel = new JPanel();
        buttonJPanel.setLayout( new BorderLayout() );
        getContentPane().add("South", buttonJPanel);

        JPanel buttonNJPanel = new JPanel();
        buttonNJPanel.setLayout( new FlowLayout(FlowLayout.CENTER) );
        buttonJPanel.add("North", buttonNJPanel);

        __applyJButton = new SimpleJButton(__BUTTON_APPLY,this);
        buttonNJPanel.add( __applyJButton );

        __okJButton = new SimpleJButton(__BUTTON_OK, this);
        buttonNJPanel.add( __okJButton );

        __cancelJButton = new SimpleJButton(__BUTTON_CANCEL, this);
        buttonNJPanel.add( __cancelJButton );

        //_help_Button = new Button("Help");
	//_help_Button.addActionListener( this );
        //buttonNJPanel.add( _help_Button );

        JPanel buttonSJPanel = new JPanel();
        buttonSJPanel.setLayout( gbl );
        buttonJPanel.add("South", buttonSJPanel);

/*
	_statusJTextField = new JTextField();
	_statusJTextField.setEditable( false );
	JGUIUtil.addComponent( buttonSJPanel, _statusJTextField,
			0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST ); 
*/

	// Frame settings

	if ( __geoViewOnly || (legend.getText() == null) ) {
		// Main GeoView properties...
		if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( "GeoView - Properties" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() +
			" - GeoView - Properties" );
		}
	}
	else {	if (	(JGUIUtil.getAppNameForWindows() == null) ||
			JGUIUtil.getAppNameForWindows().equals("") ) {
			setTitle ( legend.getText() + " - Properties" );
		}
		else {	setTitle( JGUIUtil.getAppNameForWindows() + " - " +
			legend.getText() + " - Properties" );
		}
	}
	pack();
	// SAMX try letting it do its own thing.
	//setSize( 600, 525 );
	//setResizable( false );
	JGUIUtil.center( this );
	setVisible( true );

	__doneInitializing = true;
}

/**
Detected when the state of the JTabbedPane has changed.  Mainly, want to check
when the __symbolJPanel is visible so that we can manage the heavy weight
canvas.
REVISIT (JTS - 2004-10-18)
is this old AWT stuff?  Can it be removed?
*/
public void stateChanged ( ChangeEvent e )
{	if ( e.getSource() == __theJTabbedPane ) {
		// If the selected tab is the __symbolJPanel, make set the
		// legend panel to visible, otherwise set it to not visible
		// so the canvas does not show through the other components
		if (__theJTabbedPane.getSelectedComponent() == __symbolJPanel ){
			Message.printStatus ( 1, "", "Symbol panel selected" );
			if ( __singleLegendJPanel != null ) {
				__singleLegendJPanel.setVisible(true);
			}
		}
		else {	
			if ( __singleLegendJPanel != null ) {
				__singleLegendJPanel.setVisible(false);
			}
		}
	}
}

public void windowActivated( WindowEvent evt )
{
}

public void windowClosed( WindowEvent evt )
{
}

public void windowClosing( WindowEvent evt )
{	close();
}

public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

} // End GeoViewPropertiesJFrame
