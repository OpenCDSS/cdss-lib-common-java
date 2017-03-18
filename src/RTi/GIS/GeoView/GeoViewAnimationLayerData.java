//-----------------------------------------------------------------------------
// GeoViewAnimationLayerData - Data for controlling how layers are built 
//	for GeoViewAnimationData objects.
//-----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History:
// 2004-08-12	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-27	JTS, RTi		Added finalize().
//-----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.util.List;

import RTi.GR.GRSymbol;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

import RTi.Util.Table.DataTable;

/**
This is a class that controls how layers are created for animation.  A single
one of these objects is created and then shared among all the different 
GeoViewAnimationData objects that will be used together in the same layer.
*/
public class GeoViewAnimationLayerData {

/**
Whether maximal animation values should be equalized to the same level.
*/
private boolean __equalizeMax = false;

/**
The attribute table for this layer.
*/
private DataTable __table = null;

/**
The value of missing data on this layer.
*/
private double __missing = -999.0;

/**
The value that will replace missing data on this layer.
*/
private double __missingReplacement = -1;

/**
The numbers of fields that tie the identifier to features.
*/
private int[] __idFields = null;

/**
The fields for data to be displayed.  For non-complicated symbols, such as
GRSymbol.SYM_VBARSIGNED, the data fields are the numbers of fields that will
always be shown on the layer and which are not animated.  For complicated 
symbols, such as teacups, the datafields are a key in a particularly order that
provide information on how things should be animated.  See 
GeoViewJPanel.addSummaryLayerView() for more information on these symbols.
*/
private int[] __dataFields = null;

/**
The kind of symbol to use.
*/
private int __symbolType = GRSymbol.SYM_VBARSIGNED;

/**
Properties providing additional information to the layer.  See
GeoViewJPanel.addSummaryLayerView() for more information.
*/
private PropList __props = null;

/**
The names of the data fields.
*/
private String[] __dataFieldsStrings = null;

/**
The names of the id fields.
*/
private String[] __idFieldsStrings = null;

/**
The name of the layer.
*/
private String __layerName = null;

/**
The app layer types to search through for matches.
*/
private List<String> __availAppLayerTypes = null;

/**
Constructor.
@param table the data table to use for the layer
@param layerName the name of the animation layer
@param symbolType the kind of symbol that is being animated.  Currently
supports GRSymbol.SYM_VBARSIGNED and GRSymbol.SYM_TEACUP.
@param idFields the names of the id fields to use for tying data to a feature
@param dataFields the names of the data fields.  
For non-complicated symbols, such as 
GRSymbol.SYM_VBARSIGNED, the data fields are the fields that will
always be shown on the layer and which are not animated.  For complicated 
symbols, such as teacups, the datafields are a key in a particularly order that
provide information on how things should be animated.  See 
GeoViewJPanel.addSummaryLayerView() for more information on these symbols.<p>
In general, for VBARSIGNED symbols, dataFields will be null.  For teacups, it 
will be a 3-element array where the first element is the field with the maximum
content, the second element is the field with the minimum content, and the 
third element is the field with current content.
@param availAppLayerTypes Vector of app layer types to search through for
feature matches.
@param equalizeMax whether to equalize the maximum data values
@param props a PropList that defines additional information to the layer.
@throws Exception if table, layerName, idFields, or availAppLayerTypes are
null.
*/
public GeoViewAnimationLayerData(DataTable table, String layerName, 
int symbolType, String[] idFields, String[] dataFields, 
List<String> availAppLayerTypes, boolean equalizeMax, PropList props) 
throws Exception {
	if (table == null || layerName == null || idFields == null
		|| availAppLayerTypes == null) {
		throw new NullPointerException();
	}
	
	__table = table;
	__layerName = layerName;
	__symbolType = symbolType;
	__idFieldsStrings = idFields;
	__dataFieldsStrings = dataFields;
	__availAppLayerTypes = availAppLayerTypes;
	__equalizeMax = equalizeMax;
	__props = props;

	// Determine the numbers of the fields named in the id fields array
	__idFields = new int[__idFieldsStrings.length];
	for (int i = 0; i < __idFieldsStrings.length; i++) {
		try {
			__idFields[i] = __table.getFieldIndex(
				__idFieldsStrings[i]);
		}
		catch (Exception e) {
			throw new Exception("ID Field #" + i + " ("
				+ __idFieldsStrings[i] + ") not found in "
				+ "table.");
		}
	}

	// determine the numbers of the data fieleds named in the data fields
	// array.
	if (__dataFieldsStrings == null) {
		// __dataFields cannot be null, so instantiate a 0-element
		// array instead
		__dataFields = new int[0];
	}
	else {
		__dataFields = new int[__dataFieldsStrings.length];
		for (int i = 0; i < __dataFieldsStrings.length; i++) {
			try {
				__dataFields[i] = __table.getFieldIndex(
					__dataFieldsStrings[i]);
			}
			catch (Exception e) {
				throw new Exception("Data Field #" + i + " ("
					+ __dataFieldsStrings[i] 
					+ ") not found in table.");
			}
		}	
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__table = null;
	__idFields = null;
	__dataFields = null;
	__props = null;
	IOUtil.nullArray(__dataFieldsStrings);
	IOUtil.nullArray(__idFieldsStrings);
	__layerName = null;
	__availAppLayerTypes = null;
	super.finalize();
}

/**
Returns the available app layer types.
@return the available app layer types.
*/
public List<String> getAvailAppLayerTypes() {
	return __availAppLayerTypes;
}

/**
Returns the numbers of the data fields.
@return the numbers of the data fields.
*/
public int[] getDataFields() {
	return __dataFields;
}

/**
Returns the names of the data fields.
@return the names of the data fields.
*/
public String[] getDataFieldsStrings() {
	return __dataFieldsStrings;
}

/**
Returns whether to equalize max data.
@return whether to equalzie max data.
*/
public boolean getEqualizeMax() {
	return __equalizeMax;
}

/**
Returns the numbers of the id fields.
@return the numbers of the id fields.
*/
public int[] getIDFields() {
	return __idFields;
}

/**
Returns the names of the id fields.
@return the names of the id fields.
*/
public String[] getIDFieldsStrings() {
	return __idFieldsStrings;
}

/**
Returns the layer name.
@return the layer name.
*/
public String getLayerName() {
	return __layerName;
}

/**
Returns the missing double value.
@return the missing double value.
*/
public double getMissingDoubleValue() {
	return __missing;
}

/**
Returns the missing double replacement value.
@return the missing double replacement value.
*/
public double getMissingDoubleReplacementValue() {
	return __missingReplacement;
}

/**
Returns the prop list.
@return the prop list.
*/
public PropList getProps() {
	return __props;
}

/**
Returns the sumbol type.
@return the symbol type.
*/
public int getSymbolType() {
	return __symbolType;
}

/**
Returns the attribute table.
@return the attribute table.
*/
public DataTable getTable() {
	return __table;
}

/**
Sets the value that will be recognized in this layer as missing data.  The
default value is -999.0
@param missing the value that will be recognized as missing data.
*/
public void setMissingDoubleValue(double missing) {
	__missing = missing;
}

/**
Sets the value that will replace missing data in this layer.  The default value
is -1.0
@param replacement the value that will replace missing data.
*/
public void setMissingDoubleReplacementValue(double replacement) {
	__missingReplacement = replacement;
}

}
