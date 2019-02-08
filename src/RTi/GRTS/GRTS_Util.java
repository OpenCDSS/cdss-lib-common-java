// GRTS_Util - utility functions for GRTS package

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

//------------------------------------------------------------------------------
// GRTS_Util - utility functions for GRTS package
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:	(1)	These are static methods that may be useful in various
//			applications but may not be suitable for inclusion in
//			other classes.
//------------------------------------------------------------------------------
// History:
// 
// 2006-05-22	Steven A. Malers, RTi	Initial version of classs.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------
//EndHeader

package	RTi.GRTS;

import java.util.List;

import RTi.TS.TS;
import RTi.Util.IO.PropList;

/**
This class contains static utility methods that operate on GRTS package objects.
*/
public abstract class GRTS_Util
{

/**
This method is meant to be applied to a list of time series being used to
generate a single line or point graph.  TSProduct properties are set to turn on
symbols and labels for time series that have data flags.  Point graphs will only
have the labels turned on because symbol defaults will be set when the TSProduct
is created.  This method is meant to be used by high level code that uses a
time series list and a basic PropList for graphing.  For example, an application
might query time series and provide a default graph appearance.  This is not
meant to replace the default properties that are defined when a TSProduct is
created, although if the behavior is determined to be a good default, it may be
adopted as standard defaults.
@param tslist List of time series to be graphed.
@param props Properties to modify for the graph product (must be non-null).
*/
public static void addDefaultPropertiesForDataFlags ( List<TS> tslist, PropList props )
{	int size = 0;
	if ( tslist != null ) {
		size = tslist.size();
	}
	if ( (size == 0) || (props == null) ) {
		// There is nothing to evaluate.
		return;
	}
	// Generic property...
	String propval = props.getValue ( "GraphType" );
	if ( propval == null ) {
		// Layered properties...
		propval = props.getValue ( "Graph 1.GraphType" );
	}
	TS ts = null;
	for ( int i = 0; i < size; i++ ) {
		ts = tslist.get(i);
		if ( ts.hasDataFlags() ) {
			// Turn on the symbols and labels...
			/* TODO SAM 2006-05-22
			This does not seem like a good idea.  Although it works
			well for sparse data, some data with flags (like daily
			precipitation, when drawn with symbols, overwhelms
			the user.  For now copy this out and only show the label symbols.
			if ( !ispointgraph ) {
				// Don't set symbol style and size for point
				// graphs because defaults are usually in
				// place...
				props.set ( "Data 1." + (i + 1) + ".SymbolStyle=Circle-Filled" );
				props.set ( "Data 1." + (i + 1) + ".SymbolSize=6" );
			}
			*/
			// Always set label position...
			props.set ( "Data 1." + (i + 1) + ".DataLabelFormat=%q" );
			props.set (	"Data 1." + (i + 1) + ".DataLabelPosition=UpperRight" );
		}
	}
}

/**
Format a TSView property for the subproduct (graph) and data property.  The returned string has
the format "Subproduct igraph.Data its.propVal".
@return the formatted property value for the 
@param igraph The graph number 1+.
@param its The time series number 1+.
@param propVal the raw property value.
*/
public static String formatDataProperty ( int igraph, int its, String propVal )
{
	return "Data " + igraph + "." + its + "." + propVal;
}

}
