// GRSymbolTable - table containing a list of GRSymbolTableRow, used for symbolizing data

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2021-2021 Colorado Department of Natural Resources

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
 * Symbol table using a list of GRSymbolTableRow.
 * The design of this class is consistent with recent OWF applications
 * including GeoProcessor and InfoMapper used to symbolize time series and map layers.
 */
public class GRSymbolTable {
	
	/*
	 * List of color table rows.
	 */
	private List<GRSymbolTableRow> symbolTableRows = new ArrayList<>();
	
	/*
	 * NoData color.
	 * Evaluated as rows are added.
	 */
	private GRSymbolTableRow nodataRow = null;

	/**
	 * Constructor.
	 */
	public GRSymbolTable () {
	}
	
	/**
	 * Add a symbol table row.
	 */
	public void addRow ( GRSymbolTableRow row ) {
		this.symbolTableRows.add(row);
		if ( row.isNoDataRow() ) {
			// The following is a placeholder for row with NoData (missing value).
			this.nodataRow = row;
		}
	}

	/**
	 * Create a symbol table for a color ramp.
	 * This is useful if a custom symbol table is not provided.
	 * Nice labels will be determined.
	 * @minValue the minimum data value, as a hint for determining nice breaks
	 * @maxValue the maximum data value, as a hint for determining nice breaks
	 * @param nRowsMin minimum number of rows in the table
	 * @param nRowsMax maximum number of rows in the table
	 * @param rampType the color ramp type
	 * @param precision of the numbers in the legend - currently ignored
	 * @param noDataColor if not null, include a NoData color at the end
	 */
	public static GRSymbolTable createForColorRamp (
		double minValue, double maxValue,
		int nRowsMin, int nRowsMax,
		GRColorRampType rampType,
		int precision,
		GRColor noDataColor) {
		GRSymbolTable symtable = new GRSymbolTable();
		boolean includeEndPoints = false; // Will bounding nice end points with infinity.
		String [] labelValues = GRAxis.formatLabels(
			GRAxis.findNLabels ( minValue, maxValue, includeEndPoints, nRowsMin, nRowsMax ));
		int nRows = labelValues.length - 1;
		GRColor [] colors = GRColorTable.createColorTable(GRColorRampType.BLUE_TO_RED, nRows, true).toArray(new GRColor[nRows]);
		double opacity = 1.0;
		double fillOpacity = 1.0;
		for ( int iRow = 0; iRow < nRows; iRow++ ) {
			// Format hole values as integers because the legend should be uncluttered.
			symtable.addRow ( new GRSymbolTableRow(
				">=" + labelValues[iRow],
				"<" + labelValues[iRow + 1],
				colors[iRow].toHex(),
				opacity,
				colors[iRow].toHex(),
				fillOpacity
			) );
		}
		if ( noDataColor != null ) {
			symtable.addRow ( new GRSymbolTableRow(
				"NoData",
				"NoData",
				noDataColor.toHex(),
				noDataColor.getOpacityFloat(),
				noDataColor.toHex(),
				noDataColor.getOpacityFloat()
			) );
		}
		return symtable;
	}
	
	/**
	 * Get color for a data value.  Only valid values can be processed.
	 * Infinity and -Infinity are handled.
	 * Calling code must detect missing values.
	 * @param value the data value to look up
	 * @return the matching fill color, or null if not matched
	 */
	public GRColor getFillColorForValue ( double value ) {
		for ( GRSymbolTableRow row : this.symbolTableRows ) {
			if ( row.valueInRange(value) ) {
				return row.getFillColor();
			}
		}
		// No row found.
		return null;
	}

	/**
	 * Get the  color for no data.
	 * @return the color matching "NoData" values or null if not set.
	 */
	public GRSymbolTableRow getNoDataSymbolTableRow () {
		return this.nodataRow;
	}

	/**
	 * Get the symbol table row.
	 * @param row symbol table row (0+)
	 * @return the requested symbol table row
	 */
	public GRSymbolTableRow getSymbolTableRow ( int row ) {
		return this.symbolTableRows.get(row);
	}

	/**
	 * Get the symbol table row for a data value.  Only valid values can be processed.
	 * Infinity and -Infinity are handled.
	 * Calling code must detect missing values and call getNoDataSymbolTableRow.
	 * @param value the data value to look up
	 * @return the matching symbol table row, or null if not matched
	 */
	public GRSymbolTableRow getSymbolTableRowForValue ( double value ) {
		for ( GRSymbolTableRow row : this.symbolTableRows ) {
			if ( row.valueInRange(value) ) {
				return row;
			}
		}
		// No row found.
		return null;
	}
	
	/**
	 * Read a symbol table file.
	 * @param filepath path to the symbol table file.  An example is:
	 * 
	 * # Symbol table for raster graph.
	 * valueMin,valueMax,color,opacity,fillColor,fillOpacity
	 * -Infinity,<2.0,#ffffff,0.0,#ffffff,0.0
	 * >=2.0,<10.0,#eff3ff,0.0,#eff3ff,0.0
	 * >=10.0,<100.0,#bdd7e7,0.0,#bdd7e7,0.0
	 * >=100.0,<1000.0,#6baed6,0.0,#6baed6,0.0
	 * >=1000.0,<5000.0,#3182bd,0.0,#3182bd,0.0
	 * >=5000.0,Infinity,#08519c,0.0,#08519c,0.0
	 * NoData,NoData,#000000,0.0,#000000,0.0
	 */
	public static GRSymbolTable readFile ( String filepath ) throws IOException {
		List<String> lines = IOUtil.fileToStringList(filepath);
		GRSymbolTable symtable = new GRSymbolTable();
		boolean headerRead = false;
		int valueMinCol = -1, valueMaxCol = -1, colorCol = -1, fillColorCol = -1, fillOpacityCol = - 1, opacityCol = -1;
		String valueMin, valueMax, color, fillColor;
		double opacity, fillOpacity;
		for ( String line : lines ) {
			line = line.trim();
			if ( Message.isDebugOn ) {
				Message.printDebug(1,"", "Processing:  " + line);
			}
			if ( (line.length() == 0) || (line.charAt(0) == '#') ) {
				// Comment.
				continue;
			}
			List<String> parts = StringUtil.breakStringList(line,",", 0);
			if ( !headerRead ) {
				// First line in the file is the header:
				// - determine the column numbers
				int icol = -1;
				for ( String part : parts ) {
					++icol;
					part = part.trim();
					if ( part.equalsIgnoreCase("color") ) {
						colorCol = icol;
					}
					else if ( part.equalsIgnoreCase("fillColor") ) {
						fillColorCol = icol;
					}
					else if ( part.equalsIgnoreCase("fillOpacity") ) {
						fillOpacityCol = icol;
					}
					else if ( part.equalsIgnoreCase("opacity") ) {
						opacityCol = icol;
					}
					else if ( part.equalsIgnoreCase("valueMax") ) {
						valueMaxCol = icol;
					}
					else if ( part.equalsIgnoreCase("valueMin") ) {
						valueMinCol = icol;
					}
				}
				headerRead = true;
			}
			else {
				// All other lines are data.
				int icol = -1;
				// Default values.
				color = null;
				fillColor = null;
				valueMin = null;
				valueMax = null;
				opacity = 0.0;
				fillOpacity = 0.0;
				for ( String part : parts ) {
					++icol;
					part = part.trim();
					if ( icol == colorCol ) {
						color = part;
					}
					else if ( icol == fillColorCol ) {
						fillColor = part;
					}
					else if ( icol == valueMinCol ) {
						valueMin = part;
					}
					else if ( icol == valueMaxCol ) {
						valueMax = part;
					}
					else if ( icol == opacityCol ) {
						try {
							opacity = Double.parseDouble(part);
						}
						catch ( NumberFormatException e ) {
							// Use default.
						}
					}
					else if ( icol == fillOpacityCol ) {
						try {
							fillOpacity = Double.parseDouble(part);
						}
						catch ( NumberFormatException e ) {
							// Use default.
						}
					}
				}
				// Create a new row.
				GRSymbolTableRow row = new GRSymbolTableRow ( valueMin, valueMax, color, opacity, fillColor, fillOpacity );
				symtable.addRow(row);
			}
		}
		return symtable;
	}
	
	/**
	 * Return the number of rows in the symbol table.
	 */
	public int size() {
		return this.symbolTableRows.size();
	}
}