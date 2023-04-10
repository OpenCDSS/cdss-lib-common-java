// GRScaledClassificationSymbol - store symbol definition information for GRSymbol.CLASSIFICATION_SCALED_SYMBOL type

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

package RTi.GR;

/**
This class stores information necessary to draw symbols for scaled symbol classifications.
For example, a scaled symbol is used where the symbol appearance changes only in size based on the data value at the point.
Additional symbols will be recognized later but currently only SYM_VBARSIGNED is recognized.
For this symbol, the following methods should be called after construction:
setSizeX() (bar width), setSizeY() (max bar height),
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
public GRScaledClassificationSymbol () {
	super();
	setStyle ( SYM_VBARSIGNED );
	setClassificationType ( "ScaledSymbol" );
}

/**
Return the maximum displayed value used in the classification.
@return the maximum displayed value used in the classification.
*/
public double getClassificationDataDisplayMax() {
	return _double_data_display_max;
}

/**
Return the maximum data value used in the classification.
@return the maximum data value used in the classification.
*/
public double getClassificationDataMax() {
	return _double_data_max;
}

/**
Set the maximum displayed value used in the classification.
This is typically the maximum absolute value rounded to a more presentable value.
@param max_data Maximum value to display.
*/
public void setClassificationDataDisplayMax ( double max_data ) {
	_double_data_display_max = max_data;
}

/**
Set the maximum value used in the classification (for use with scaled classification).
This is typically the maximum absolute value.
@param max_data Maximum value from the data set.
*/
public void setClassificationDataMax ( double max_data ) {
	_double_data_max = max_data;
}

}