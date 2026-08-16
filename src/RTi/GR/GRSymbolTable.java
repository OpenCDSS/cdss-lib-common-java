// GRSymbolTable - table containing a list of GRSymbolTableRow, used for symbolizing data

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 2021-2026 Colorado Department of Natural Resources

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
import java.util.Map;

import RTi.Util.IO.IOUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;

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
	
	/**
	 * Whether or not an expression column is used:
	 * - if true then expressions need to be evaluated by calling code that can expand properties
	 */
	private boolean usesExpression = false;

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
	 * Create EvalEx expressions for each row in the symbol table.
	 * It is expected that the expression contains only variable names and not ${property} notation.
	 */
	public void createEvalExExpressions () {
		String routine = getClass().getSimpleName() + "createEvalExExpressions";
		// Create an express configuration for all the expressions:
		// - enable single quotes to indicate strings so that expressions can have double quotes in CSV files
		ExpressionConfiguration configuration = ExpressionConfiguration.builder()
			.singleQuoteStringLiteralsAllowed(true)
			.build();
		Expression expression = null;

		// Loop through the symbol table rows.
		for ( GRSymbolTableRow row : this.symbolTableRows ) {
			try {
				expression = new Expression ( row.getExpressionFullString(), configuration );
				row.setEvalExExpression ( expression );
			}
			catch ( Exception e ) {
       			String message = "Error creating the EvalEx expression from \"" + row.getExpressionFullString() + "\" (" + e + ").";
           		Message.printWarning ( 3, routine, message );
				row.setEvalExExpression ( null );
			}
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
	 * Get the first symbol row that evaluates to true for a map of input objects.
	 * @param objectMap map of named objects to use as input to the expression
	 * @return the first matching row, or null if no match
	 */
	public GRSymbolTableRow getSymbolTableRowForEvalExExpression ( Map<String,Object> objectMap ) {
		String routine = getClass().getSimpleName() + ".getSymboleTablRowForEvalExExpression";
	  	EvaluationValue result = null;
	  	for ( GRSymbolTableRow row : this.symbolTableRows ) {
	  		try {
	  			Expression evalExExpression = row.getEvalExExpression();
	  			if ( evalExExpression == null ) {
	  				if ( Message.isDebugOn ) {
	  					Message.printStatus(2, routine, "Expression is null.");
	  				}
	  			}
	  			else {
	  				result = evalExExpression.withValues(objectMap).evaluate();
	  				// Expect a boolean value.
	  				if ( result.isBooleanValue() ) {
	  					// Expression result is a boolean value so can interpret the result.
	  					if ( Message.isDebugOn ) {
	  						Message.printStatus(2, routine, "Expression result is a boolean: " + result.getBooleanValue());
	  					}
	  					if ( result.getBooleanValue() ) {
	  						// The row evaluated as true so return it:
	  						// - otherwise, keep evaluating expressions
	  						return row;
	  					}
	  				}
	  				else {
	  					if ( Message.isDebugOn ) {
	  						Message.printStatus(2, routine, "Expression result is not a boolean: " + result);
	  					}
	  				}
   				}
			}
			catch ( Exception e ) {
	    		String message = "Error evaluating the expression \"" + row.getExpressionFullString() + "\" (" + e + ").";
	       		Message.printWarning(3, routine, message );
	       		Message.printWarning(3, routine,e);
			}
	  	}
	  	return null;
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
	 * Return the symbol table row list.
	 * This is useful for iteration.
	 * @return the symbol table row list
	 */
	public List<GRSymbolTableRow> getSymbolTableRows () {
		return this.symbolTableRows;
	}
	
	/**
	 * Return whether expression is used.
	 * If true, then calling code needs to evaluate the expressions to determine the matching symbol table row(s).
	 * If false, then the getFillColorForValue() or getSymbolTableRowForValue() methods can be called.
	 * @return whether expression is used
	 */
	public boolean getUsesExpression () {
		return this.usesExpression;
	}
	
	/**
	 * Parse a delimited line.
	 * @param line line to parse
	 * @return List of string
	 */
    public static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // Toggle quote state, skip appending the quote symbol.
            }
            else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim()); // End of field reached.
                currentField.setLength(0); // Reset buffer.
            }
            else {
                currentField.append(c); // Append regular text or commas inside quotes.
            }
        }
        fields.add(currentField.toString().trim()); // Add the last field.
        return fields;
    }

	/**
	 * Read a symbol table file.
	 * @param filepath path to the symbol table file.  An example using minimum and maximum lits is:
	 *
	 *<pre>
# Symbol table for raster graph.
valueMin,valueMax,color,opacity,fillColor,fillOpacity
-Infinity,<2.0,#ffffff,0.0,#ffffff,0.0
>=2.0,<10.0,#eff3ff,0.0,#eff3ff,0.0
>=10.0,<100.0,#bdd7e7,0.0,#bdd7e7,0.0
>=100.0,<1000.0,#6baed6,0.0,#6baed6,0.0
>=1000.0,<5000.0,#3182bd,0.0,#3182bd,0.0
>=5000.0,Infinity,#08519c,0.0,#08519c,0.0
NoData,NoData,#000000,0.0,#000000,0.0
</pre>
	 * 
	 * An example using expressions with property place holders is as follows.
	 * This requires expansion and evaluation of the expression for each value,
	 * which may have a high computational host.
	 * 
	 * <pre>
# Symbol table for 15 minute streamflow example:
# - use an expression rather than value minimum and maximum (but keep the columns with different names for comparison)
# - specify legend to explicitly indicate the legend text because it is difficult to determine from the expression
expression,                                             legend,            color,   opacity, fillColor, fillOpacity, comment
(${tsdata:value} <2.0),                                 <2.0,              #000000, 1.0,     #000000,   1.0,         black
((${tsdata:value} >=2) && (${tsdata:value} <10)),       >=2.0 <10.0,       #ff0000, 1.0,     #ff0000,   1.0,         red
((${tsdata:value} >=10) && (${tsdata:value} <100)),     >=10.0 <100.0,     #ffa500, 1.0,     #ffa500,   1.0,         orange
((${tsdata:value} >=100) && (${tsdata:value} <1000))    >=100 <1000.0,     #ffff00, 1.0,     #ffff00,   1.0,         yellow
((${tsdata:value} >=1000) && (${tsdata:value} <5000)),  >=1000.0 <5000.0,  #00ffff, 1.0,     #00ffff,   1.0,         cyan
(${tsdata:value} >=5000),                               >=5000.0,          #ff00ff, 1.0,     #ff00ff,   1.0,         magenta
(${tsdata:isvaluemissing})                              NoData,            #ffffff, 1.0,     #ffffff,   1.0,         white
</pre>
	 * 
	 * An example using expressions with variables is as follows.
	 * This allows the expression to be created once and evaluated with a list of variable objects,
	 * which evalaute faster.
	 * 
	 * <pre>
# Symbol table for 15 minute streamflow example:
# - use an expression rather than value minimum and maximum (but keep the columns with different names for comparison)
# - specify legend to explicitly indicate the legend text because it is difficult to determine from the expression
expression,                          legend,            color,   opacity, fillColor, fillOpacity, comment
(valueismissing),                    NoData,            #ffffff, 1.0,     #ffffff,   1.0,         white
(value <2.0),                        <2.0,              #000000, 1.0,     #000000,   1.0,         black
((value >=2) && (value} <10)),       >=2.0 <10.0,       #ff0000, 1.0,     #ff0000,   1.0,         red
((value >=10) && (value} <100)),     >=10.0 <100.0,     #ffa500, 1.0,     #ffa500,   1.0,         orange
((value >=100) && (value} <1000)),   >=100 <1000.0,     #ffff00, 1.0,     #ffff00,   1.0,         yellow
((value >=1000) && (value} <5000)),  >=1000.0 <5000.0,  #00ffff, 1.0,     #00ffff,   1.0,         cyan
(value >=5000),                      >=5000.0,          #ff00ff, 1.0,     #ff00ff,   1.0,         magenta
</pre>
	 */
	public static GRSymbolTable readFile ( String filepath ) throws IOException {
		String routine = GRSymbolTable.class.getSimpleName() + ".readFile";
		if ( Message.isDebugOn ) {
			Message.printDebug(1,routine, "Reading symbol table file:  " + filepath);
		}
		List<String> lines = IOUtil.fileToStringList(filepath);
		GRSymbolTable symtable = new GRSymbolTable();
		boolean headerRead = false;
		// Initialize the column numbers to handle missing:
		// - list in the typical order
		// - either 'expression' and 'legend'
		//   or 'valueMin' and 'valueMax' are needed
		int expressionCol = -1;
		int legendCol = -1;
		int valueMinCol = -1;
		int valueMaxCol = -1;
		int colorCol = -1;
		int fillColorCol = -1;
		int fillOpacityCol = - 1;
		int opacityCol = -1;
		// Column text values before parsing.
		String expression = null;
		String legend = null;
		String valueMin = null;
		String valueMax = null;
		String color = null;
		String fillColor = null;
		// Currently 'opacity' and 'fillOpacity' are not used.
		double opacity;
		double fillOpacity;
		int iLine = 0; // Line count (1+) for logging.
		for ( String line : lines ) {
			++iLine;
			line = line.trim();
			if ( Message.isDebugOn ) {
				Message.printDebug(1,routine, "Processing:  " + line);
			}
			if ( (line.length() == 0) || (line.charAt(0) == '#') ) {
				// Comment.
				continue;
			}
			// Allow CSV file parts to include quoted strings:
			// - the following is breaking when single quotes are in the middle of a field, such as for expression literal string
			// - use a ChatGPT-generated function for now
			//List<String> parts = StringUtil.breakStringList(line,",", StringUtil.DELIM_ALLOW_STRINGS);
			List<String> parts = parseCsvLine ( line );
			if ( !headerRead ) {
				// First line in the file is the header:
				// - determine the column numbers for standard properties
				int icol = -1;
				for ( String part : parts ) {
					++icol;
					part = part.trim();
					if ( part.equalsIgnoreCase("color") ) {
						colorCol = icol;
					}
					else if ( part.equalsIgnoreCase("expression") ) {
						expressionCol = icol;
						// Indicate that the table uses expressions.
						symtable.usesExpression = true;
					}
					else if ( part.equalsIgnoreCase("fillColor") ) {
						fillColorCol = icol;
					}
					else if ( part.equalsIgnoreCase("fillOpacity") ) {
						fillOpacityCol = icol;
					}
					else if ( part.equalsIgnoreCase("legend") ) {
						legendCol = icol;
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
				// Check to make sure that the columns were found:
				// - list in the typical order of the file
				if ( (expressionCol < 0) && ((valueMinCol < 0) && (valueMaxCol < 0) )) {
					Message.printWarning(3, routine, "The 'expression' column or 'valueMin' and 'valueMax' columns must be specified.");
				}
				if ( colorCol < 0 ) {
					Message.printWarning(3, routine, "No 'color' column in the symbol table file.");
				}
				/*
				if ( opacityCol < 0 ) {
					Message.printWarning(3, routine, "No 'opacity' column in the symbol table file.");
				}
				*/
				if ( fillColorCol < 0 ) {
					Message.printWarning(3, routine, "No 'fillColor' column in the symbol table file.");
				}
				/*
				if ( fillOpacityCol < 0 ) {
					Message.printWarning(3, routine, "No 'fillOpacity' column in the symbol table file.");
				}
				*/
				headerRead = true;
			}
			else {
				// All other lines are data.
				int icol = -1;
				// Default values.
				color = null;
				expression = null;
				fillColor = null;
				legend = null;
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
					else if ( icol == expressionCol ) {
						expression = part;
					}
					else if ( icol == fillColorCol ) {
						fillColor = part;
					}
					else if ( icol == legendCol ) {
						legend = part;
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
				// Create a new row:
				// - only do so if the necessary parts are specified
				// - list in the typical order of the file
				boolean okToAdd = true;
				if ( (expression == null) && ((valueMin == null) || (valueMax == null)) ) {
					// Problem
					if ( valueMin == null ) {
						Message.printWarning(3, routine, "No 'expression' value specified in symbol table file line " + iLine );
					}
					if ( valueMin == null ) {
						Message.printWarning(3, routine, "No 'valueMin' value specified in symbol table file line " + iLine );
					}
					if ( valueMax == null ) {
						Message.printWarning(3, routine, "No 'valueMax' value specified in symbol table file line " + iLine );
					}
					okToAdd = false;
				}
				if ( color == null ) {
					Message.printWarning(3, routine, "No 'color' value specified in symbol table file line " + iLine );
					okToAdd = false;
				}
				if ( fillColor == null ) {
					Message.printWarning(3, routine, "No 'fillColor' value specified in symbol table file line " + iLine );
					okToAdd = false;
				}
				if ( okToAdd ) {
					// Required columns were found in the symbol table so add the symbol table row.
					GRSymbolTableRow row = new GRSymbolTableRow ( expression, legend, valueMin, valueMax, color, opacity, fillColor, fillOpacity );
					symtable.addRow(row);
				}
			}
		}
		// If expressions are used, create the expression objects.
		if ( symtable.getUsesExpression() ) {
			symtable.createEvalExExpressions();
		}
		return symtable;
	}

	/**
	 * Return the number of rows in the symbol table.
	 */
	public int size() {
		return this.symbolTableRows.size();
	}
	
	/**
	 * Indicate whether the symbol table uses an expression column.
	 */
}