// GeoLayerView - class to hold a geographic layer and view information

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

package RTi.GIS.GeoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import RTi.GR.GRClassificationType;
import RTi.GR.GRColor;
import RTi.GR.GRLegend;
import RTi.GR.GRShape;
import RTi.GR.GRSymbol;
import RTi.GR.GRSymbolShapeType;
import RTi.GR.GRSymbolShapeTypeListContents;
import RTi.GR.GRSymbolType;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;

import RTi.Util.Time.StopWatch;

/**
A GeoViewLayer joins information from a GeoLayer
(raw data) and the view data that are controlled and displayed by a GeoView.
The GeoViewLayer object tracks the legend and other data.
This allows multiple GeoLayerViews to be constructed from the same GeoLayer.
The GeoLayerView is also then used to interact with the user because it understands the view and the data.
The legend indicates what symbol should be used to render the view.
*/
public class GeoLayerView
implements Cloneable
{

/**
For each of the attribute table fields, specifies whether the field should be drawn on the display.
*/
private boolean[] __animationFieldVisible = null;

/**
Indicates whether the layer is visible.
*/
private boolean _is_visible = true;

/**
Indicates whether the layer is selected or not.  Normally this is used only for GUI interaction.
*/
private boolean _is_selected = false;

/**
Whether this is an animated layer, where the shape symbols will change.
*/
private boolean __isAnimated = false;

// TODO (JTS - 2004-08-09) put the missing data things into a proplist.

/**
The value that represents missing data.  Defaults to -999.0.
*/
private double __missingDouble = -999.0;

/**
The value that will replace missing data.  Defaults to -999.0.
*/
private double __missingDoubleReplacement = -999.0;

/**
GeoLayer used by the GeoView.
*/
private GeoLayer _layer = null;

/**
GeoViewJComponent that uses the GeoLayer.
*/
private GeoViewJComponent	_view = null;

/**
Color to use for layer, including symbols.
*/
private GRLegend _legend = null;

/**
An array that points to the fields within the attribute table that are animated.
This array does not correspond with all the fields in the attribute
table but will instead contain a series of values, such as:<p>
<ul>
<li>__animationFields[0] = 12</li>
<li>__animationFields[1] = 13</li>
<li>__animationFields[2] = 15</li>
</ul>
This means that fields 12, 13 and 15 (base-0) in the table are animated fields.
*/
private int[] __animationFields = null;

/**
The gui that controls the animation for this layer.  Null if the layer is not animated.
*/
private JFrame __controlJFrame = null;

/**
Properties for the GeoViewLayer, including labeling information.
*/
private PropList _props = null;

// TODO (JTS - 2004-08-09) put into a proplist.

/**
The name of the layer.
*/
private String __layerName = null;

/**
Construct and initialize to null data.
The view will be set when this GeoLayerView is passed to GeoView.addLayerView.
*/
public GeoLayerView () {
	initialize ();
}

/**
Construct from a layer.  The view will be set when this GeoLayerView is passed to GeoView.addLayerView.
@param layer GeoLayer instance.
*/
public GeoLayerView ( GeoLayer layer ) {
	initialize ();
	_layer = layer;
}

/**
Construct from a layer file and properties.
The layer is first read and then default symbol properties are assigned based on the count of the layers.
Currently attributes are NOT read.
@param filename Name of ESRI shapefile.
@param props Properties for the layer view, for example as read from a GeoView project file
(see overloaded version for description).
@param count Count of layers being added.  This affects the default symbols that are assigned.
The first value should be 1.  <b>This is not the GeoLayerView number in a GVP file.</b>
@exception Exception if there is an error reading the file.
*/
public GeoLayerView ( String filename, PropList props, int count )
throws Exception {
	// First read the file.
	setLayer ( GeoLayer.readLayer ( filename, props ) );
	setProperties ( props );
	// Set default symbol, legend.  This will normally be reset (e.g., when reading in GeoViewProject).
	setDefaultLegend ( count );
}

/**
Construct from a layer and set properties.
The view will be set when this GeoLayerView is passed to GeoView.addLayerView.
@param layer GeoLayer instance.
@param props Properties for the GeoLayerView.  The following properties are recognized:
<p>

<table width=100% cellpadding=10 cellspacing=0 border=2>
<tr>
<td><b>Property</b></td>   <td><b>Description</b></td>   <td><b>Default</b></td>
</tr

<tr>
<td><b>Label</b></td>
<td>Indicates how to label the view.  If the property is set to "UsingGeoViewListener",
then the listener method "geoViewGetLabel" will be called for each shape that is drawn.
The application implementing the "geoViewGetLabel" method should determine an appropriate label and return the String for drawing.
If the property is set to "UsingAttributeTable" then the "AttributeTableLabelField" property should also be defined.
<td>No labels (just symbols, if a symbol type is defined).</td>
</tr>

<tr>
<td><b>AttributeTableLabelField</b></td>
<td>Indicates the field from the attribute table to use for labeling.
<td>Not defined.</td>
</tr>

</table>
*/
public GeoLayerView ( GeoLayer layer, PropList props ) {
	initialize ();
	_layer = layer;
	// Default is just main part of the filename.
	File f = new File ( layer.getFileName() );
	//_legend = new GRLegend ( null, layer.getFileName() );
	_legend = new GRLegend ( null, f.getName() );
	f = null;
	if ( props != null ) {
		_props = props;
	}
}

/**
Construct from a layer and specify the legend.
The view will be set when this GeoLayerView is passed to GeoViewJComponent.addLayerView.
@param layer GeoLayer instance.
@param legend GRLayer instance.
*/
public GeoLayerView ( GeoLayer layer, GRLegend legend ) {
	initialize ();
	_layer = layer;
	_legend = legend;
}

/**
Construct from a layer and specify the legend and other properties.
The view will be set when this GeoLayerView is passed to GeoViewJComponent.addLayerView.
@param layer GeoLayer instance.
@param legend GRLayer instance.
@param props Properties for the GeoLayerView.  See other constructors for a description.
*/
public GeoLayerView ( GeoLayer layer, GRLegend legend, PropList props ) {
	initialize ();
	_layer = layer;
	_legend = legend;
	if ( props != null ) {
		_props = props;
	}
}

/**
Clones this object.  Does not clone animation information.
@return a clone of this object.
*/
public Object clone() {
	GeoLayerView l = null;
	try {
		l = (GeoLayerView)super.clone();
	}
	catch (Exception e) {
		return null;
	}

	if (_layer != null) {
		l._layer = (GeoLayer)_layer.clone();
	}

	l._view = _view;

	if (_legend != null) {
		l._legend = (GRLegend)_legend.clone();
	}

	if (_props != null) {
		l._props = new PropList(_props);
	}

	return l;
}

/**
Returns the JFrame used to control animation on this layer.
@return the JFrame used to control animation on this layer.
*/
public JFrame getAnimationControlJFrame() {
	return __controlJFrame;
}

/**
Returns the number of rows in the attribute table.
@return the number of rows in the attribute table.
*/
public int getAttributeTableRowCount() {
	return _layer.getAttributeTableRowCount();
}

/**
@return label field used for labeling.
This returns the "AttributeTableLabelField" property value or null if not defined.
TODO SAM 2009-07-01 NEED TO PHASE THIS OUT.
*/
public String getLabelField () {
	return _props.getValue("AttributeTableLabelField");
}

/**
Return the GeoLayer associated with the GeoLayerView.
@return the GeoLayer associated with the GeoLayerView.
*/
public GeoLayer getLayer() {
	return _layer;
}

/**
Return the legend used for the GeoLayerView.
@return the legend used for the GeoLayerView.
*/
public GRLegend getLegend ()
{	return _legend;
}

/**
Returns the value that represents missing data for this layer.  Default is -999.0.
@return the value that represents missing data for this layer.
*/
public double getMissingDoubleValue() {
	return __missingDouble;
}

/**
Returns the value that will replace missing data for this layer when drawn on the display.
Default is -999.0.
@return the value that will replace missing data when data are drawn.
*/
public double getMissingDoubleReplacementValue() {
	return __missingDoubleReplacement;
}

// TODO SAM 2006-03-02 Need to determine whether the name is a data member or from the PropList.
/**
Returns the layer's name.
@return the layer's name.
*/
public String getName() {
	if ( __layerName != null ) {
		return __layerName;
	}
	String name = _props.getValue ( "Name" );
	if ( name != null ) {
		return name;
	}
	if ( _legend != null ) {
		return _legend.getText();
	}
	return null;
}

/**
Returns the number of fields to animate.
@return the number of fields to animate.
*/
public int getNumAnimationFields() {
	if (__animationFields == null) {
		return 0;
	}
	return __animationFields.length;
}

/**
Return the PropList used for the GeoLayerView.
@return the PropList used for the GeoLayerView.
*/
public PropList getPropList () {
	return _props;
}

/**
Return the symbol used for the GeoLayerView.  This calls the getSymbol() method for the legend.
@return the symbol used for the GeoLayerView or null if a legend is not defined.
*/
public GRSymbol getSymbol () {
	if ( _legend == null ) {
		return null;
	}
	return _legend.getSymbol();
}

/**
Return the GeoViewJComponent associated with the GeoLayerView.
@return the GeoViewJComponent associated with the GeoLayerView.
*/
public GeoViewJComponent getView() {
	return _view;
}

/**
Initialize data.
*/
private void initialize () {
	_is_visible = true;	// Default is that layer view is visible.
	_is_selected = false; // Default is that layer view is not specifically selected.
    _layer = null;
	_legend = null;
	_view = null;
	// Get an empty property list.
	_props = PropList.getValidPropList ( null, "GeoLayerView" );
}

/**
Returns whether this is an animated layer.
@return whether this is an animated layer.
*/
public boolean isAnimated() {
	return __isAnimated;
}

/**
Returns whether or not a field is one being animated.
@param num the number of the field in the attribute table.
@return true if the field is animated, false if not.
*/
public boolean isAnimatedField(int num) {
	if (__animationFields == null) {
		return false;
	}
	else {
		for (int i = 0; i < __animationFields.length; i++) {
			if (__animationFields[i] == num) {
				return true;
			}
		}
	}
	return false;
}

/**
Returns whether an animated field is visible or not.
@param field the number of the field to check for visibility.
@return true if the field is visible, false if not.
*/
public boolean isAnimationFieldVisible(int field) {
	if (__animationFieldVisible == null) {
		setupAnimationFieldVisible();
		return true;
	}
	return __animationFieldVisible[field];
}

/**
Indicate whether a layer view is selected.
@return true if the layer is selected, false if not.
*/
public boolean isSelected() {
	return _is_selected;
}

/**
Set whether a GeoViewLayer is selected or not.
@return true if the layer is selected, false if not, after the reset.
@param is_selected Indicates whether the layer view is selected or not.
*/
public boolean isSelected ( boolean is_selected ) {
	_is_selected = is_selected;
	return _is_selected;
}

/**
Indicate whether a layer view is visible.
@return true if the layer is visible, false if not.
*/
public boolean isVisible() {
	return _is_visible;
}

/**
Set whether a GeoViewLayer is visible or not.
@return true if the layer is visible, false if not, after the reset.
@param is_visible Indicates whether the layer view is visible or not.
*/
public boolean isVisible ( boolean is_visible ) {
	_is_visible = is_visible;
	return _is_visible;
}

/**
Select features in the layer view based on a check of an attribute value (e.g., a string identifier).
This method is called from the GeoViewPanel selectAppFeatures() method but can be called at a lower level
(e.g., to select all shapes that correspond to identifiers in a data set.
@param feature_array The data attributes corresponding to the AppJoinField property saved with a GeoLayerView.
One or more values can be specified.
@param join_field Fields in the layer to search (e.g., the AppLayerField).
This can be multiple fields separated by commas.
@param append Indicates whether the selections should be added to previous selections.
<b>This feature is under development.</b>
@return list of GeoRecord for the selected features, or null if nothing is selected.
*/
public List<GeoRecord> selectFeatures ( String [][] feature_array, String join_field, boolean append ) {
	String routine = getClass().getSimpleName() + ".selectFeatures";
	// First try the old code.
	StopWatch timer = new StopWatch();
	/* To test old code.
	timer.start();
	Vector v = selectFeatures1 ( feature_array, join_field, append );
	timer.stop();
	Message.printStatus ( 1, routine, "Old select time = " +
	timer.getSeconds() );
	timer.clear();
	*/

//System.out.println("\nGeoLayerView");

	timer.start();
	if ( feature_array == null ) {
//System.out.println("   (null feature array)");
		return null;
	}
	int nfeature = feature_array.length;
	if ( nfeature == 0 ) {
//System.out.println("   (nfeature == 0)");
		return null;
	}
	// List of GeoRecord that will be returned.
	List<GeoRecord> georecords = null;
	// Get the fields to search...
	if ( join_field == null ) {
//System.out.println("   (join field is null)");
		return georecords;
	}
	List<String> joinFieldList = StringUtil.breakStringList ( join_field, ",", 0 );
	if ( joinFieldList == null ) {
//System.out.println("   (join fields vector is null)");
		return georecords;
	}
	// Now figure out what integer fields these are in the attribute data.
	int join_fields_size = joinFieldList.size();
	int [] join_fields = new int[join_fields_size];
	String [] format_spec = new String[join_fields_size];
	DataTable table = _layer.getAttributeTable();
	TableRecord table_record = null;
	int ifeature;
	int ijf = 0;
	for ( ijf = 0; ijf < join_fields_size; ijf++ ) {
		try {
			join_fields[ijf] = table.getFieldIndex( (String)joinFieldList.get(ijf) );
			format_spec[ijf] = table.getFieldFormat( join_fields[ijf] );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Join field \"" + joinFieldList.get(ijf) + " not found in data layer" );
			return georecords;
		}
	}
	// Loop through all the shapes in the layer to find the ones that have attributes that match the selected shapes.
	List<GRShape> shapes = _layer.getShapes();
	int nshapes = 0;
	if ( shapes != null ) {
		nshapes = shapes.size();
	}
	String layer_name = _legend.getText();
	if ( layer_name.equals("") ) {
		layer_name = _layer.getFileName();
	}
	Message.printStatus ( 1, routine, "Searching \"" + layer_name + "\" for matching features..." );
	GRShape shape;
	Object o;
	String formatted_attribute;
	// Allow multiple shapes to match the search criteria.
	// Support for true should be added when 1 to many or numeric field searches are allowed.
	boolean allow_multiple_shapes = false;
	// Create an array to hold a count of the number of fields for the
	// current shape that match for each requested feature.
	// Use the smallest data size possible.
	byte [] shape_matches_feature = new byte[nfeature];

	//System.out.println("nshapes: " + nshapes);
	for ( int ishape = 0; ishape < nshapes; ishape++ ) {
		shape = shapes.get(ishape);
		// Skip null shapes, generally due to missing coordinates.
		if ( shape.type == GeoLayer.UNKNOWN ) {
			continue;
		}
		// For now, skip features that have zeros for the max and min values.
		// This occurs when null data come from a database into shapefiles.
		// For now treat as missing data.  This prevents more problems later.
		if ( shape.xmin == 0.0 ) {
			continue;
		}
		// See if the value matches the value in the list that was passed in.
		// Currently this is done with strings so it will be a problem if floating point data fields are joined.
		//
		// First initialize the following array.
		// For the current shape this indicates a count of how many requested feature attributes are matched.
		// This is necessary because if more than one attribute is compared,
		// a count of the previous loop results is needed.
		// If for any requested feature the count of matches is the number of attributes, then the shape matches.
		for ( ifeature = 0; ifeature < nfeature; ifeature++ ) {
			shape_matches_feature[ifeature] = 0;
		}
		// Loop through each join field.
		for ( ijf = 0; ijf < join_fields_size; ijf++ ) {
			// Get the data value from the attribute table.
			// Do this in the outside loop so that if attributes are read from a file,
			// it is only read once, which should improve performance.
			//if ( Message.isDebugOn ) {
			//	Message.printDebug ( 1, "",
			//	"Checking join field ["  + ijf + "]=" +
			//	join_fields_Vector.elementAt(ijf) );
			//}
			try {
				o = table.getFieldValue ( shape.index, join_fields[ijf] );
			}
			catch ( Exception e ) {
				// Just skip.
				continue;
			}
			// Format the attribute value to a common format.
			formatted_attribute = StringUtil.formatString ( o.toString(), format_spec[ijf]).trim();
			//System.out.println("   formatted_attribute: '" + formatted_attribute + "'");
			// Now loop through each feature that is being searched for
			// (each if the join fields will be checked in an inner loop below).
			for ( ifeature = 0; ifeature < nfeature; ifeature++ ){
				// If the previous field matches where not made,
				// there is no need to check other fields (the shape does not match).
				//
				// shape_matches_feature will = ijf if previous attributes have matched.
				// The first time through, shape_matches_feature will be 0 and ijf will be 0.
				/* Need to change this to see if NO first attribute for the shape matched.
				if ( shape_matches_feature[ifeature] < ijf ) {
					// Previous attribute for shape did not
					// match this feature's attributes so no reason to check more attributes.
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, "",
						"Previous attribute for " +
						"ifeature ["
						+ ifeature +"] did not match.");
					}
					break;
				}
				*/
				//if ( Message.isDebugOn ) {
				//	Message.printDebug ( 1, "",
				//	"comparing \"" +
				//	formatted_attribute + "\" to \""
				//	+ feature_array[ifeature][ijf]+"\"");
				//}
				if ( formatted_attribute.equalsIgnoreCase( feature_array[ifeature][ijf]) ) {
					//System.out.println("      f[" + ifeature + "][" + ijf + "]: '" + feature_array[ifeature][ijf] + "'");
					// The features matches.
					++shape_matches_feature[ifeature];
					//if ( Message.isDebugOn ) {
					//	Message.printDebug ( 1, "",
					//	"shape_matches_feature[" +
					//	ifeature + "]=" +
					//	shape_matches_feature[
					//	ifeature] );
					//}
					if ( shape_matches_feature[ifeature] == join_fields_size ) {
						// Matched all fields so add to the match list.
						if ( !append ) {
							// Always select...
							if (!shape.is_selected) {
								_layer.setNumSelected ( _layer.getNumSelected() + 1 );
							}
							shape.is_selected =true;
						}
						else {
							// Reverse selection.
							if(!shape.is_selected ) {
								shape.is_selected =true;
								_layer.setNumSelected ( _layer.getNumSelected() + 1 );
							}
							else {
								shape.is_selected=false;
								_layer.setNumSelected (_layer.getNumSelected() - 1 );
							}
						}
						Message.printStatus ( 2, "", "Matched shape type=" + shape.type +
						" index=" + shape.index + " id=" + o.toString() );
						// Add to the GeoRecord list.
						if ( georecords == null ) {
							georecords = new ArrayList<GeoRecord>();
						}
						try {
							table_record = table.getRecord( (int)shape.index );
						}
						catch ( Exception e ) {
							table_record = null;
						}
						GeoRecord
						georecord =new GeoRecord ( shape, table_record, _layer, this );
						georecords.add ( georecord );
						// Break out of the loop since a match was made.
						if ( !allow_multiple_shapes ) {
							break;
						}
					}
				}
			}
		}
	}
	timer.stop();
	Message.printStatus ( 1, routine, "Select took " +
	timer.getSeconds() + " seconds." );
	return georecords;
}

// TODO SAM 2007-05-09 Evaluate whether this is needed.
/**
This version is optimized if 1 join field is used.
Select features in the layer view based on a check of an attribute value (e.g., a string identifier).
This method is called from the GeoViewPanel selectAppFeatures() method but can be called at a lower level
(e.g., to select all shapes that correspond to identifiers in a data set).
@param feature_array The data attributes corresponding to the AppJoinField
property saved with a GeoLayerView.  One or more values can be specified.
@param join_field Fields in the layer to search (e.g., the AppLayerField).
This can be multiple fields separated by commas.
@param append Indicates whether the selections should be added to previous selections.
<b>This feature is under development.</b>
@return list of GeoRecord for the selected features, or null if nothing is selected.
*/
/*
private Vector selectFeatures1 (String [][] feature_array, String join_field, boolean append ) {
	String routine = "GeoLayerView.selectFeatures";
	if ( feature_array == null ) {
		return null;
	}
	int nfeature = feature_array.length;
	if ( nfeature == 0 ) {
		return null;
	}
	// List of GeoRecord that will be returned.
	Vector georecords = null;
	// Get the fields to search...
	if ( join_field == null ) {
		return georecords;
	}
	Vector join_fields_Vector = StringUtil.breakStringList ( join_field, ",", 0 );
	if ( join_fields_Vector == null ) {
		return georecords;
	}
	// Now figure out what integer fields these are in the attribute data.
	int join_fields_size = join_fields_Vector.size();
	int join_fields_size_m1 = join_fields_size - 1;
	int [] join_fields = new int[join_fields_size];
	String [] format_spec = new String[join_fields_size];
	DataTable table = _layer.getAttributeTable();
	TableRecord table_record = null;
	int ifeature;
	int ijf = 0;
	boolean allow_multiple_shapes = false;	// Allow multiple shapes to
						// match the search criteria.
						// Support for true should be
						// added when 1 to many or
						// numeric field searches are
						// allowed
	for ( ijf = 0; ijf < join_fields_size; ijf++ ) {
		try {	join_fields[ijf] = table.getFieldIndex(
			(String)join_fields_Vector.elementAt(ijf) );
			format_spec[ijf] = table.getFieldFormat( join_fields[ijf] );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine,
			"Join field \"" +
			(String)join_fields_Vector.elementAt(ijf) +
			" not found in data layer" );
			return georecords;
		}
	}
	// Loop through all the shapes in the layer to find the ones that have attributes that match the selected shapes.
	Vector shapes = _layer.getShapes();
	int nshapes = 0;
	if ( shapes != null ) {
		nshapes = shapes.size();
	}
	String layer_name = _legend.getText();
	if ( layer_name.equals("") ) {
		layer_name = _layer.getFileName();
	}
	Message.printStatus ( 1, routine, "Searching \"" + layer_name + "\" for matching features..." );
	GRShape shape;
	Object o;
	for ( int ishape = 0; ishape < nshapes; ishape++ ) {
		shape = (GRShape)shapes.elementAt(ishape);
		// For now, skip features that have zeros for the max and min values.
		// This occurs when null data come from a database into shapefiles.
		// For now treat as missing data.  This prevents more problems later.
		if ( shape.xmin == 0.0 ) {
			continue;
		}
		// See if the value matches the value in the list that was passed in.
		// Currently this is done with strings so it will be a problem if floating point data fields are joined.
		for ( ifeature = 0; ifeature < nfeature; ifeature++ ) {
			// Loop through each join field.
			for ( ijf = 0; ijf < join_fields_size; ijf++ ) {
				// Get the data value from the attribute table.
				try {
					o = table.getFieldValue ( shape.index, join_fields[ijf] );
				}
				catch ( Exception e ) {
					// Just skip.
					continue;
				}
				if ( Message.isDebugOn ) {
					Message.printDebug ( 1, "",
					"comparing \"" +
					StringUtil.formatString( o.toString(),
					format_spec[ijf]).trim() + "\" to \"" +
					feature_array[ifeature][ijf]+"\"");
				}
				if ( StringUtil.formatString( o.toString(),
					format_spec[ijf]).trim(
					).equalsIgnoreCase(
					feature_array[ifeature][ijf]) ) {
					// The features matches.
					if ( ijf == join_fields_size_m1 ) {
						// Matched all fields so add to the match list.
						if ( !append ) {
							// Always select.
							if (!shape.is_selected){
								_layer.setNumSelected ( _layer.getNumSelected() + 1 );
							}
							shape.is_selected =true;
						}
						else {
							// Reverse selection.
							if(!shape.is_selected ) {
							shape.is_selected =true;
							_layer.setNumSelected ( _layer.getNumSelected() + 1 );
							}
							else {
							shape.is_selected=false;
							_layer.setNumSelected (
							_layer.getNumSelected()
							- 1 );
							}
						}
//						Message.printStatus ( 1, "",
//						"Matched shape " +
//						shape.index + " id=" +
//						o.toString() );
						// Add to the GeoRecord list.
						if ( georecords == null ) {
							georecords = new Vector ( 10 );
						}
						try {
							table_record = table.getRecord( (int)shape.index );
						}
						catch ( Exception e ) {
							table_record = null;
						}
						GeoRecord
						georecord = new GeoRecord(
							shape, table_record,
							_layer, this );
						georecords.addElement ( georecord );
						// Have added so OK to break out of the loop.
						break;
					}
				}
				else {
					// Break out of loop since one of the criteria was not met.
					break;
				}
			}
		}
	}
	return georecords;
}
*/

/**
Sets whether this is an animated layer or not.
Setting to 'true' does not automatically set up animation.
Animation fields and the current step must be set, and the attribute table must support it.
@param animated whether this is an animated layer or not.
*/
public void setAnimated(boolean animated) {
	__isAnimated = animated;
}

/**
Sets the JFrame that is used to control the animation of this layer.
@param jframe the JFrame that is used to control the layer animation.
*/
public void setAnimationControlJFrame(JFrame jframe) {
	__controlJFrame = jframe;
}

/**
Sets the array of fields within the attribute table that will be animated.
Setting to null disables animation.
@param fields the fields within the attribute table that will be animated;
do not have to be consecutively ordered.
*/
public void setAnimationFields(int[] fields) {
	__animationFields = fields;
	if (fields == null) {
		__isAnimated = false;
	}
}

/**
Sets whether an animation field should be visible or not.
@param field the number of the field to set visible or not.
@param visible whether to make the field's data visible or not.
*/
public void setAnimationFieldVisible(int field, boolean visible) {
	if (__animationFieldVisible == null) {
		setupAnimationFieldVisible();
	}
	__animationFieldVisible[field] = visible;
}

/**
Set default legend information.  This picks default colors and symbol sizes.
@param count Count of layers that have been added (1 for first one added).
<b>This is not the GeoLayerView number in the GVP file.</b>
*/
public void setDefaultLegend ( int count ) {
	// Now set default colors.
	// Add later.
	//_layer_view.setColor ( GRColor.getRandomColor() );
	// For now cycle through.
	// Size after add.
	double symsize = 6.0;
	int layerType = 0;
	GRSymbolType symType = GRSymbolType.NONE;
	GRSymbol symbol = null;
	GRColor color = null;
	GRColor outline_color = null;
	if ( (count%5) == 0 ) {
		color = GRColor.cyan;
	}
	else if ( (count%4) == 0 ) {
		color = GRColor.orange;
	}
	else if ( (count%3) == 0 ) {
		color = GRColor.red;
	}
	else if ( (count%2) == 0 ) {
		color = GRColor.blue;
	}
	else {
		color = GRColor.green;
	}
	// Set the symbol.
	//layer_view.setSymbol ( GR.SYM_FCIR + count - 1 );
	// Set the symbol size.
	//layer_view.setSymbolSize ( 6.0 );
	layerType = _layer.getShapeType();
	if ( (layerType == GeoLayer.POINT) || (layerType == GeoLayer.POINT_ZM) ) {
		symType = GRSymbolType.POINT;
	}
	else if ( (layerType == GeoLayer.LINE) || (layerType == GeoLayer.POLYLINE_ZM) ) {
		symType = GRSymbolType.LINE;
	}
	else if ( layerType == GeoLayer.POLYGON ) {
		symType = GRSymbolType.POLYGON;
		outline_color = GRColor.white;
	}
	else if ( layerType == GeoLayer.GRID ) {
		// Try for now.
		//outline_color = GRColor.black;
	}

	// Create a symbol:
	// - the subtype currently only affects the point symbols
	// - the list of symbols is retrieved each time this method is called,
	//   which is a resource hit but there are not that many layers
	GRSymbolShapeType [] shapeTypes = GRSymbol.getShapeTypes(GRSymbolShapeTypeListContents.GEOLAYER_DEFAULT);

	// Initial value.
	symbol = new GRSymbol ( symType, shapeTypes[count - 1], color, outline_color, symsize );
	if ( layerType == GeoLayer.GRID ) {
		// Set a default color table assuming 10 colors.
		symbol.setColorTable ( "YellowToRed", 10 );
		symbol.setClassificationType ( GRClassificationType.CLASS_BREAKS);
		// For now hard-code the colors just to see if this works.
		double [] data = new double[10];
		data[0] = 0.0;
		data[1] = 1.0;
		data[2] = 2.0;
		data[3] = 5.0;
		data[4] = 10.0;
		data[5] = 20.0;
		data[6] = 50.0;
		data[7] = 100.0;
		data[8] = 200.0;
		data[9] = 500.0;
		symbol.setClassificationData ( data, false );
	}
	if ( _legend == null ) {
		_legend = new GRLegend ( symbol );
	}
	else {
		_legend.setSymbol ( symbol );
	}
	//_legend.setText ( _layer.getFileName() );
	// The legend for the layer view is either the name
	// (e.g., from the GeoView project GeoLayerView.Name property, or the filename).
	String prop_value = _props.getValue ( "Name" );
	if ( (prop_value != null) && !prop_value.equals("") ) {
		_legend.setText ( prop_value );
	}
	else {
		File f = new File ( _layer.getFileName() );
		_legend.setText ( f.getName() );
		f = null;
	}
}

/**
Sets a value in the attribute table.
@param row the row in which to set the value.
@param column the column in which to set the value.
@param value the value to set.
@throws Exception if there is an error setting the value.
*/
public void setAttributeTableValue(int row, int column, Object value)
throws Exception {
	_layer.setAttributeTableValue(row, column, value);
}

/**
Set the layer for the layer view.
This is protected because currently it is intended for use only within the package during object initialization.
*/
protected void setLayer ( GeoLayer layer ) {
	_layer = layer;
}

/**
Set the label field used for labeling.
TODO SAM 2009-07-02 THIS NEEDS TO BE PHASED OUT.
@param label_field Name of attribute field to use for labeling.
*/
public void setLabelField ( String label_field ) {
	if ( label_field != null ) {
		_props.setValue ( "AttributeTableLabelField", label_field );
	}
}

/**
Set the legend to use with this GeoLayerView.
@param legend GRLegend to use.
*/
public void setLegend (GRLegend legend) {
	_legend = legend;
}

/**
Sets the value for missing data for this layer.  Default is -999.0.
@param value the value for missing data for this layer.
*/
public void setMissingDoubleValue(double value) {
	__missingDouble = value;
}

/**
Sets the value that will replace missing data when the data are drawn on the display.
This is used so that if missing data are found,
the result can be adjusted to not be a large negative bar.
Default is -999.0.
@param value the value to replace missing data values with when drawn.
*/
public void setMissingDoubleReplacementValue(double value) {
	__missingDoubleReplacement = value;
}

/**
Sets the name of the layer.
@param name the name of the layer.
*/
public void setName(String name) {
	__layerName = name;
}

/**
Set the properties for the layer view.
This is protected because currently it is intended for use only within the package during object initialization.
*/
protected void setProperties ( PropList props ) {
	_props = props;
}

/**
Sets up the array for keeping track of whether animation fields are visible or not.
*/
private void setupAnimationFieldVisible() {
	int num = _layer.getAttributeTableFieldCount();
	__animationFieldVisible = new boolean[num];
	for (int i = 0; i < num; i++) {
		__animationFieldVisible[i] = true;
	}
}

/**
Set the GeoViewJComponent used with this GeoLayerView.
@param view GeoViewJComponent to use with this GeoLayerView.
*/
public void setView ( GeoViewJComponent view ) {
	if ( view != null ) {
		_view = view;
	}
}

}