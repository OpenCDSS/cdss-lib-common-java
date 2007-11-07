// ----------------------------------------------------------------------------
// GRScaledClassificationSymbol - store symbol definition information for
//				GRSymbol.CLASSIFICATION_SCALED_SYMBOL type.
// ----------------------------------------------------------------------------
// History:
//
// 2002-09-24	Steven A. Malers, RTi	Create this class to extend GRSymbol.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GR;

/**
This class stores information necessary to draw symbols for scaled symbol
classifications.  For example, a scaled symbol is used where the symbol
appearance changes only in size based on the data value at the point.
Additional symbols will be recognized later but currently only SYM_VBARSIGNED
is recognized.  For this symbol, the following methods should be called after
construction:  setSizeX() (bar width), setSizeY() (max bar height),
setColor() (positive/up bar color), setColor2() (negative/down bar color).
*/
public class GRScaledClassificationSymbol extends GRSymbol
{

/**
Maximum actual data value.
*/
protected double _double_data_max = 0.0;

/**
Maximum displayed data value.  User-defined or automatically-determined.
*/
protected double _double_data_display_max = 0.0;

/**
Constructor.  The symbol style defaults to GRSymbol.SYM_VBARSIGNED.
*/
public GRScaledClassificationSymbol ()
{	super();
	setStyle ( SYM_VBARSIGNED );
	setClassificationType ( "ScaledSymbol" );
}

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	super.finalize();
}

/**
Return the maximum displayed value used in the classification.
@return the maximum displayed value used in the classification.
*/
public double getClassificationDataDisplayMax()
{	return _double_data_display_max;
}

/**
Return the maximum data value used in the classification.
@return the maximum data value used in the classification.
*/
public double getClassificationDataMax()
{	return _double_data_max;
}

/**
Set the maximum displayed value used in the classification.
This is typically the maximum absolute value rounded to a more presentable
value.
@param max_data Maximum value to display.
*/
public void setClassificationDataDisplayMax ( double max_data )
{	_double_data_display_max = max_data;
}

/**
Set the maximum value used in the classification (for use with scaled
classification).  This is typically the maximum absolute value.
@param max_data Maximum value from the data set.
*/
public void setClassificationDataMax ( double max_data )
{	_double_data_max = max_data;
}

} // End of GRScaledClassificationSymbol
