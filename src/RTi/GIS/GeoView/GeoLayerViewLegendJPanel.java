// ----------------------------------------------------------------------------
// GeoLayerViewLegendJPanel - panel to hold layer checkbox, select button, and
//				symbol canvas
// ----------------------------------------------------------------------------
// History:
//
// 2001-10-09	Steven A. Malers, RTi	Overload constructor to allow legend to
//					be drawn in passive mode for the
//					GeoViewPropertiesGUI.
// 2001-10-12	SAM, RTi		Update constructor to pass in
//					GeoViewLegendPanel so that the legend
//					panel states can be checked when an
//					individual layer view setting changes.
// 2001-10-15	SAM, RTi		Set unused data to null to help with
//					garbage collection.
// 2001-12-04	SAM, RTi		Update to Swing.
// 2002-01-08	SAM, RTi		Change GeoLayerViewLegendCanvas to
//					GeoLayerViewLegendJComponent.
// ----------------------------------------------------------------------------
// 2003-05-06	J. Thomas Sapienza, RTi	Brought code up to date with the
//					non-Swing code.
// 2003-05-14	JTS, RTi		Removed font-setting code.
// 2004-11-11	JTS, RTi		In legends with class breaks, the
//					class break text background is now
//					set to match the color of the tree.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.GR.GRScaledClassificationSymbol;
import RTi.GR.GRSymbol;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;

import RTi.Util.IO.IOUtil;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;

/**
JPanel to display a legend for a GeoLayerView.  This panel includes a checkbox
to enable/disable the GeoLayerView, the name associated with the GeoLayerView,
and, a canvas (GRDevice) to display the symbology, and a label next to the
canvas to indicate the symbol classification field.  If the multiple classes are
used, then multiple canvases and labels are used.
*/

@SuppressWarnings("serial")
public class GeoLayerViewLegendJPanel extends JPanel
implements ActionListener, ItemListener, MouseListener
{

private GeoViewLegendJTree _parent = null;
private GeoLayerView _layer_view = null;
private JCheckBox _enabled_JCheckBox = null;
private SimpleJButton _layer_JButton = null;
private JLabel _layer_JLabel = null;
private GeoLayerViewLegendJComponent[] _layer_Canvas = null;
private JLabel [] _layer_class_JLabel = null;
private boolean _ctrl_pressed;		// Used to indicate whether a shift or
					// control key is pressed when the
					// button is pressed.  This is only used
					// in mousePressed() and mouseReleased()

/**
Construct a legend panel instance for a given GeoLayerView.  The checkbox and
select button are included by default.
@param parent GeoViewLegendJTree parent or null.
@param layer_view GeoLayerView for legend panel.
*/
public GeoLayerViewLegendJPanel ( GeoViewLegendJTree parent, GeoLayerView layer_view )
{	this ( parent, layer_view, true );
}

/**
Construct a legend panel instance for a given GeoLayerView, optionally
not displaying controls (and including only the symbol and layer view label).
This is typically used in the layer view properties interface where only
the canvas and related labels are needed.
@param layer_view GeoLayerView for legend panel.
@param include_controls If true, then the JCheckBox and select Button are added.
If false, a Label is used in place of the JCheckBox and Button.
*/
public GeoLayerViewLegendJPanel ( GeoLayerView layer_view, boolean include_controls )
{	this (null, layer_view, include_controls );
}

/**
Construct a legend panel instance for a given GeoLayerView.
@param parent GeoViewLegendPanel parent or null.
@param layer_view GeoLayerView for legend panel.
@param include_controls If true, then the Checkbox and select Button are added.
If false, a Label is added is used instead of the Checkbox and Button.
*/
public GeoLayerViewLegendJPanel (GeoViewLegendJTree parent, GeoLayerView layer_view, boolean include_controls ) {
	_layer_view = layer_view;
	_parent = parent;
	setLayout ( new GridBagLayout() );

	Insets insets_none = new Insets ( 1, 1, 1, 1 );
	int y = 0;
	if ( include_controls ) {
		// Do not specify a label because doing so enables toggling by
		// selecting on the label.  The label should be inert.
		_enabled_JCheckBox = new JCheckBox ();
		_enabled_JCheckBox.setSelected ( layer_view.isVisible() );
		_enabled_JCheckBox.addItemListener ( this );
		JGUIUtil.addComponent ( this, _enabled_JCheckBox,
				0, y, 1, 1, 0, 0,
				insets_none, GridBagConstraints.NONE,
				GridBagConstraints.NORTH );
		_layer_JButton = new SimpleJButton (
				layer_view.getLegend().getText(),
				layer_view.getLegend().getText(), this );
		JGUIUtil.addComponent ( this, _layer_JButton,
				1, y, 2, 1, 1, 0,
				insets_none, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH );
	}
	else {
		_layer_JLabel = new JLabel (layer_view.getLegend().getText());
		JGUIUtil.addComponent ( this, _layer_JLabel,
				0, y, 3, 1, 1, 0,
				insets_none, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH );
	}

	// Now draw the symbol(s)...
	// Get the number of symbols for the layer view.  First need to
	// determine the layer view number, which is stored in the "Number"
	// property for the layerview.  Currently this is supported only for
	// CLASSIFICATION_SINGLE and CLASSIFICATION_SCALED_SYMBOL.

	int nsymbol = layer_view.getLegend().size();
	GRSymbol symbol = null;
	for ( int isym = 0; isym < nsymbol; isym++ ) {
		symbol = layer_view.getLegend().getSymbol(isym);
		if ( symbol.getClassificationType() == GRSymbol.CLASSIFICATION_SINGLE ) {
			if ( isym == 0 ) {
				// For now assume that symbol types will not be mixed for a layer...
				_layer_Canvas = new GeoLayerViewLegendJComponent[nsymbol];
				_layer_class_JLabel = new JLabel[nsymbol];
			}
			_layer_Canvas[isym] = new GeoLayerViewLegendJComponent( _layer_view, isym, 0);
			JGUIUtil.addComponent ( this, _layer_Canvas[isym],
				1, ++y, 1, 1, 0, 0,
				insets_none, GridBagConstraints.NONE,
				GridBagConstraints.SOUTH );
			// Add a label to keep spacing consistent...
			_layer_class_JLabel[isym] = new JLabel("");
			JGUIUtil.addComponent ( this,
				_layer_class_JLabel[isym],
				2, y, 1, 1, 1, 0,
				insets_none, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.SOUTH );
		}
		else if ( symbol.getClassificationType() == GRSymbol.CLASSIFICATION_SCALED_SYMBOL ) {
			// This is currently enabled only for vertical signed
			// bars where the bar is centered vertically on the
			// point, positive values are drawn with the main
			// foreground color and negative values are drawn with
			// the secondary foreground color.
			if ( isym == 0 ) {
				// For now assume that symbol types will not
				// be mixed for a layer...
				_layer_Canvas = new GeoLayerViewLegendJComponent[nsymbol];
				_layer_class_JLabel = new JLabel[nsymbol];
			}
			_layer_Canvas[isym] = new GeoLayerViewLegendJComponent( _layer_view, isym, 0);
			JGUIUtil.addComponent ( this, _layer_Canvas[isym],
				1, ++y, 1, 1, 0, 0,
				insets_none, GridBagConstraints.NONE,
				GridBagConstraints.SOUTH );
			if ( !symbol.getClassificationField().equals("") ) {
				// Get the maximum value for the symbol, which
				// is used to scale the symbol...
				// SAMX - need to streamline this - store with
				// symbol at creation?
				DataTable attribute_table = _layer_view.getLayer().getAttributeTable();
				int classification_field = -1;
				String cf = symbol.getClassificationField();
				if ( attribute_table != null ) {
					try {
						classification_field = attribute_table.getFieldIndex( cf );
					}
					catch ( Exception e ) {
						// Just won't label below.
						classification_field = -1;
					}
				}
				//Message.printStatus ( 1, "",
				//"SAMX classification field = " +
				//classification_field + " \"" + cf + "\"" );
				// Message.printStatus ( 1, "",
				// "SAMX geoview panel = " + geoview_Panel );
				if ( (classification_field >= 0)) {
					double symbol_max = ((GRScaledClassificationSymbol)
					symbol).getClassificationDataDisplayMax();
					// Do this to keep legend a reasonable width...
					if ( cf.length() > 20 ) {
						cf = cf.substring(0,20) + "...";
					}
					_layer_class_JLabel[isym] = new JLabel(
						cf + ", Max = " + StringUtil.formatString(symbol_max,"%.3f") );
				}
				else {
					if ( cf.length() > 20 ) {
						cf = cf.substring(0,20) + "...";
					}
					_layer_class_JLabel[isym] = new JLabel( cf );
				}
			}
			else {
				// Add a label with the field and maximum value...
				_layer_class_JLabel[isym] = new JLabel("");
			}
			JGUIUtil.addComponent ( this,
				_layer_class_JLabel[isym],
				2, y, 1, 1, 1, 0,
				insets_none, GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH );
		}
		else {
			// Multiple legend items need to be drawn...
			int numclass = symbol.getNumberOfClassifications();
			_layer_Canvas = new GeoLayerViewLegendJComponent[numclass];
			_layer_class_JLabel = new JLabel[numclass];
			for ( int i = 0; i < numclass; i++ ) {
				_layer_Canvas[i] = new GeoLayerViewLegendJComponent( _layer_view, isym, i );
				JGUIUtil.addComponent ( this, _layer_Canvas[i],
					1, ++y, 1, 1, 0, 0,
					insets_none, GridBagConstraints.NONE,
					GridBagConstraints.SOUTH );
				// Add a label for the classification...
				_layer_class_JLabel[i] = new JLabel(symbol.getClassificationLabel(i));
				if (_parent != null) {
					_layer_class_JLabel[i].setBackground(_parent.getBackground());
				}
				JGUIUtil.addComponent ( this,
					_layer_class_JLabel[i],
					2, y, 1, 1, 1, 0,
					insets_none,
					GridBagConstraints.HORIZONTAL,
					GridBagConstraints.SOUTH );
			}
			if (_parent != null) {
				setBackground(_parent.getBackground());
			}
		}
		// Set the text on the button...
		if ( !symbol.getClassificationField().equals("") ) {
			if ( include_controls ) {
				// Reset the button to display the field
				if ( (layer_view.getLegend().size() == 1) &&
					(symbol.getClassificationType() != GRSymbol.CLASSIFICATION_SCALED_SYMBOL)){
					// Put the field on the button...
					_layer_JButton.setText(
					layer_view.getLegend().getText() + " ("+
					symbol.getClassificationField() + ")" );
				}
				else {
					_layer_JButton.setText( layer_view.getLegend().getText() );
				}
			}
			else {
				// No controls so the label needs to display the classification field...
				if ( layer_view.getLegend().size() == 1 ) {
					_layer_JLabel.setText (
					layer_view.getLegend().getText() + " ("+ symbol.getClassificationField() + ")" );
				}
				else {
					// The label is next to the symbol...
					// This will take some more work.
				}
			}
		}
		else {
			// Set the label to the legend text...
			//_layer_JButton.setLabel (layer_view.getLegend().getText() );
			// Does not seem to work.
		}
	}
}

/**
Process action events.  If the button is selected, this toggles the layer view
from selected to deselected, highlighting when selected.
If a parent GeoViewJPanel is specified during construction, its checkState() method is called.
@param e ActionEvent to process.
*/
public void actionPerformed ( ActionEvent e )
{	if ( _layer_view.isSelected() ) {
		// Already selected so de-select...
		setBackground ( Color.lightGray );
		_enabled_JCheckBox.setBackground ( Color.lightGray );
		_layer_JButton.setBackground ( Color.lightGray );
		_layer_JButton.setForeground ( Color.black );
		for ( int i = 0; i < _layer_class_JLabel.length; i++ ) {
			_layer_Canvas[i].setBackground( Color.lightGray );
			_layer_Canvas[i].repaint();
			_layer_class_JLabel[i].setBackground ( Color.lightGray);
			_layer_class_JLabel[i].setForeground ( Color.black );
		}
		_layer_view.isSelected ( false );
	}
	else {
		// Not already selected so select...
		setBackground ( Color.darkGray );
		_enabled_JCheckBox.setBackground ( Color.darkGray );
		_layer_JButton.setBackground ( Color.darkGray );
		_layer_JButton.setForeground ( Color.white );
		for ( int i = 0; i < _layer_class_JLabel.length; i++ ) {
			_layer_Canvas[i].setBackground( Color.darkGray );
			_layer_Canvas[i].repaint();
			_layer_class_JLabel[i].setBackground ( Color.darkGray );
			_layer_class_JLabel[i].setForeground ( Color.white );
		}
		_layer_view.isSelected ( true );
	}
}

/**
Reset the state of the check box based on the visibility of the layer view.
The state of the checkbox is set consistent with the GeoLayerView "isSelected()" value.
*/
public void checkState ()
{	if ( _enabled_JCheckBox != null ) {
		_enabled_JCheckBox.setSelected ( _layer_view.isVisible() );
	}
}

/**
Deselect the legend item.
*/
public void deselect ()
{	setBackground ( Color.lightGray );
	_enabled_JCheckBox.setBackground ( Color.lightGray );
	_layer_JButton.setBackground ( Color.lightGray );
	_layer_JButton.setForeground ( Color.black );
	for ( int i = 0; i < _layer_class_JLabel.length; i++ ) {
		_layer_Canvas[i].setBackground( Color.lightGray );
		_layer_Canvas[i].repaint();
		_layer_class_JLabel[i].setBackground ( Color.lightGray );
		_layer_class_JLabel[i].setForeground ( Color.black );
	}
	_layer_view.isSelected ( false );
}

/**
Clean up for garbage collection.
*/
protected void finalize()
throws Throwable
{	_enabled_JCheckBox = null;
	_parent = null;
	_layer_view = null;
	_layer_JButton = null;
	_layer_JLabel = null;
	IOUtil.nullArray(_layer_Canvas);
	IOUtil.nullArray(_layer_class_JLabel);
	super.finalize();
}

/**
Return the GeoLayerView associated with the legend panel.
@return the GeoLayerView associated with the legend panel.
*/
public GeoLayerView getLayerView ()
{	return _layer_view;
}

/**
Handle item events.  The isVisible() method for the GeoLayerView is called with
the state of the JCheckBox.
*/
public void itemStateChanged ( ItemEvent e )
{	if ( e.getItemSelectable().equals(_enabled_JCheckBox) ) {
		_layer_view.isVisible(_enabled_JCheckBox.isSelected());
	}
}

/**
Handle mouse clicked event.  Don't do anything.  Rely on mousePressed().
*/
public void mouseClicked ( MouseEvent event )
{	
}

/**
Handle mouse drag event.  Don't do anything.
@param event Mouse drag event.
*/
public void mouseDragged ( MouseEvent event )
{	//event.consume();
}

/**
Handle mouse enter event.  Currently does not do anything.
*/
public void mouseEntered ( MouseEvent event )
{
}

/**
Handle mouse exit event.  Currently does not do anything.
*/
public void mouseExited ( MouseEvent event )
{
}

/**
Handle mouse motion event.  Currently does not do anything.
*/
public void mouseMoved ( MouseEvent event )
{
}

/**
Handle mouse pressed event.  Just save a flag indicating what keys were active.
*/
public void mousePressed ( MouseEvent e )
{	if ( ((e.getModifiers()&MouseEvent.SHIFT_MASK) != 0) ||
		((e.getModifiers()&MouseEvent.CTRL_MASK) != 0) ) {
		_ctrl_pressed = true;
	}
}

/**
Handle mouse released event.
*/
public void mouseReleased ( MouseEvent e )
{	// The _ctrl_pressed data member indicates whether a CTRL or SHIFT key
	// was pressed when the mouse was pressed.  Use that because it is
	// possible to release/press the key between releasing/pressing the mouse.
	if ( !_ctrl_pressed ) {
		// Unselect all other layer views except the current layer view...
		// _parent.deselectExcept ( this );
	}
	if ( _layer_view.isSelected() ) {
		// Already selected so de-select...
		deselect ();
	}
	else {
		// Not already selected so select...
		select ();
	}
}

/**
Check to make sure that the checkbox is accurate.
*/
public void paint ( Graphics g )
{	checkState();
	super.paint(g);
}

/**
Select this legend item.
*/
public void select ()
{	setBackground ( Color.darkGray );
	_enabled_JCheckBox.setBackground ( Color.darkGray );
	_layer_JButton.setBackground ( Color.darkGray );
	_layer_JButton.setForeground ( Color.white );
	for ( int i = 0; i < _layer_class_JLabel.length; i++ ) {
		_layer_Canvas[i].setBackground( Color.darkGray );
		_layer_Canvas[i].repaint();
		_layer_class_JLabel[i].setBackground ( Color.darkGray );
		_layer_class_JLabel[i].setForeground ( Color.white );
	}
	_layer_view.isSelected ( true );
}

/**
Set the panel's canvas components visibility.
This method is called from the properties interface to hide heavyweight canvas components.
@param visible true if the components should be visible.
*/
public void setVisible ( boolean visible )
{	if ( (_layer_Canvas != null) && (_layer_Canvas.length > 0) ) {
		for ( int i = 0; i < _layer_Canvas.length; i++ ) {
			_layer_Canvas[i].setVisible( visible );
		}
	}
	super.setVisible ( visible );
}

}