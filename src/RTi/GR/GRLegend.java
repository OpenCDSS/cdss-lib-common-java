// GRLegend - class to store information for a legend for a single data layer

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

import java.lang.StringBuffer;

/**
The GRLegend class stores information for a legend for a single data layer.
Examples of data layers are spatial data and time series.
Each layer can have one or more symbol.
For earlier versions of this class only a single GRSymbol and text label was stored and this behavior is still transparently supported.
However, the class also can save multiple symbols.  Only one text label is currently used (not one label per symbol).
*/
public class GRLegend
implements Cloneable
{

/**
Array of symbols used in the legend.
*/
private GRSymbol [] __symbol = null;

/**
The text of the legend.
*/
private String __text = "";

/**
Construct by indicating the number of symbols.
@param nsymbols The number of symbols that will be used.
After construction, use setText() and setSymbol() to set the symbols.
*/
public GRLegend ( int nsymbols ) {
	__text = "";
	__symbol = new GRSymbol[nsymbols];
}

/**
Construct using the single symbol.
@param symbol the symbol to use in the legend.
*/
public GRLegend ( GRSymbol symbol ) {
	__text = "";
	__symbol = new GRSymbol[1];
	__symbol[0] = symbol;
}

/**
Construct using the single symbol and the text.
@param symbol the symbol to use in the legend.
@param text the text to put in the legend.
*/
public GRLegend ( GRSymbol symbol, String text ) {
	__text = "";
	__symbol = new GRSymbol[1];
	__symbol[0] = symbol;
	setText ( text );
}

/**
Clones the object.
@return a clone of the Object.
*/
public Object clone() {
	GRLegend l = null;
	try {
		l = (GRLegend)super.clone();
	}
	catch (Exception e) {
		return null;
	}

	if (__symbol != null) {
		l.__symbol = new GRSymbol[__symbol.length];
		for (int i = 0; i < __symbol.length; i++) {
			l.__symbol[i] = (GRSymbol)__symbol[i].clone();
		}
	}

	return l;
}

/**
Return the symbol used for the legend.
It is assumed that only one symbol is used and therefore the first symbol is returned.
@return the symbol used for the legend.
*/
public GRSymbol getSymbol () {
	if ( __symbol == null ) {
		return null;
	}
	else {
		return __symbol[0];
	}
}

/**
Return the symbol used for the legend, for a specific position.
@param pos Position in the symbol array (zero index).
@return the symbol used for the legend.
*/
public GRSymbol getSymbol ( int pos ) {
	if ( __symbol == null ) {
		return null;
	}
	else {
		return __symbol[pos];
	}
}

/**
Return legend text.
@return the text used for the legend.
*/
public String getText () {
	return __text;
}

/**
Set the number of symbols.  This reallocates the symbols array.
@param num_symbols The number of symbols to use for the legend.
*/
public void setNumberOfSymbols ( int num_symbols ) {
	__symbol = new GRSymbol[num_symbols];
	for ( int i = 0; i < num_symbols; i++ ) {
		__symbol[i] = null;
	}
}

/**
Set the symbol to use.
It is assumed that only one symbol is being used and therefore the symbol at position zero is set.
@param symbol Symbol to use for legend.
*/
public void setSymbol ( GRSymbol symbol ) {
	if ( (__symbol == null) || (__symbol.length == 0) ) {
		__symbol = new GRSymbol[1];
	}
	__symbol[0] = symbol;
}

/**
Set the symbol to use.  The number of symbols must have been set in the constructor.
@param pos Position for the symbol (zero index).
@param symbol GRSymbol to set at the position.
*/
public void setSymbol ( int pos, GRSymbol symbol ) {
	// Later need to make sure copy constructor will work for symbol and can redefine symbol array.
	__symbol[pos] = symbol;
}

/**
Set the legend text.
@param text Text to use for legend.
*/
public void setText ( String text ) {
	if ( text != null ) {
		__text = text;
	}
}

/**
Return the size of the legend (the number of symbols).
@return the size of the legend (the number of symbols).
*/
public int size () {
	if ( __symbol == null ) {
		return 0;
	}
	else {
		return __symbol.length;
	}
}

/**
Return a string representation of the legend in the form "text,symbol".
@return a string representation of the legend.
*/
public String toString () {
	StringBuffer b = new StringBuffer("\"" + __text + "\"" );
	if ( __symbol != null ) {
		for ( int i = 0; i < __symbol.length; i++ ) {
			b.append ( "," + __symbol[i] );
		}
	}
	return b.toString();
}

}