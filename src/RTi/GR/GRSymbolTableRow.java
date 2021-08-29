// GRColorTableRecord - class for a single row in a color table

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

package RTi.GR;

import RTi.Util.Message.Message;

/**
 * Single row for a color table, consistent with TSTool raster change and web development. 
 * @author sam
 *
 */
public class GRSymbolTableRow {
	
	/*
	 * Values for operators, to streamline evaluating values for the range.
	 */
	public static final int UNKNOWN = 0;
	public static final int LE = 1;
	public static final int LT = 2;
	public static final int GE = 3;
	public static final int GT = 4;
	
	/*
	 * Minimum value as a string, one of: -Infinity, Infinity, <Value, <=Value, >Value, >=Value.
	 */
	private String valueMinFullString = "";

	/*
	 * Minimum value as a double.
	 */
	private double valueMinDouble;
	
	/*
	 * Minimum value operator.
	 */
	private int valueMinOperator = UNKNOWN;
	
	/*
	 * Indicate whether the minimum value is negative infinity.
	 */
	private boolean valueMinIsNegativeInfinity = false;

	/*
	 * Maximum value as a string, one of: -Infinity, Infinity, <Value, <=Value, >Value, >=Value.
	 */
	private String valueMaxFullString = "";

	/*
	 * Maximum value as a double.
	 */
	private double valueMaxDouble;

	/*
	 * Maximum value operator.
	 */
	private int valueMaxOperator = UNKNOWN;

	/*
	 * Indicate whether the maximum value is infinity.
	 */
	private boolean valueMaxIsInfinity = false;
	
	/*
	 * Border color.
	 */
	private GRColor color = null;
	
	/*
	 * Opacity.  0.0 is no opacity (solid), 1.0 is transparent.
	 */
	private double opacity = 0.0;

	/*
	 * Fill color.
	 */
	private GRColor fillColor = null;
	
	/*
	 * Fill opacity.  0.0 is no opacity (solid), 1.0 is transparent.
	 */
	private double fillOpacity = 0.0;

	/*
	 * Weight for drawing vector features.
	 */
	private double weight = 1.0;
	
	/**
	 * Whether a NoData row.
	 */
	private boolean isNoDataRow = false;
	
	/**
	 * Constructor
	 * @param minValueString minimum value string as in <N, <=N, >N, >=N, -Infinity, Infinity
	 * @param maxValueString minimum value string as in <N, <=N, >N, >=N, -Infinity, Infinity
	 * @param color color string in format #123456
	 * @param opacity opacity 0.0 to 1.0
	 * @param fillColor color string in format #123456
	 * @param fillOpacity opacity 0.0 to 1.0
	 */
	public GRSymbolTableRow (
		String minValueFullString,
		String maxValueFullString,
		String color,
		double opacity,
		String fillColor,
		double fillOpacity ) {
		this.valueMinFullString = minValueFullString;
		parseLimit(minValueFullString, -1);
		this.valueMaxFullString = maxValueFullString;
		parseLimit(maxValueFullString, 1);
		if ( (color != null) && !color.isEmpty() ) {
			this.color = GRColor.parseColor(color);
			Message.printStatus(2,"","Parsed color is: " + this.color.toHex(true));
		}
		if ( (fillColor != null) && !fillColor.isEmpty() ) {
			this.fillColor = GRColor.parseColor(fillColor);
			Message.printStatus(2,"","Parsed fillColor is: " + this.fillColor.toHex(true));
		}
		this.opacity = opacity;
		this.fillOpacity = fillOpacity;
	}
	
	/**
	 * Get the color.
	 */
	public GRColor getColor () {
		return this.color;
	}

	/**
	 * Get the fill color.
	 */
	public GRColor getFillColor () {
		return this.fillColor;
	}

	/**
	 * Get the fill opacity.
	 */
	public double getFillOpacity () {
		return this.fillOpacity;
	}

	/**
	 * Get the fill opacity.
	 */
	public double getOpacity () {
		return this.opacity;
	}

	/**
	 * Get the value max full string, useful for legends.
	 */
	public String getValueMaxFullString () {
		return this.valueMaxFullString;
	}

	/**
	 * Get the value min full string, useful for legends.
	 */
	public String getValueMinFullString () {
		return this.valueMinFullString;
	}

	/**
	 * Get the weight.
	 */
	public double getWeight () {
		return this.weight;
	}
	
	/**
	 * Indicate whether the row is a NoData row.
	 */
	public boolean isNoDataRow () {
		return this.isNoDataRow;
	}

	/**
	 * Parse a limit string and save in internal data so that processing can be fast later.
	 * The 'valueInRange' method can be called to determine if the row matches a value.
	 * @param limitString a limit string for the range.
	 * @param whichEnd, -1 for minimum, 1 for maximum
	 */
	private void parseLimit ( String limitValueString, int whichEnd ) {
		limitValueString = limitValueString.trim();
		if ( limitValueString.equalsIgnoreCase("NoData") ) {
			Message.printStatus(2,"","Parsed NoData row.");
			if ( !this.isNoDataRow ) {
				// Do this to ensure that a single NoData value indicates the row is NoData.
				this.isNoDataRow = true;
			}
		}
		else if ( whichEnd == -1 ) {
			if ( limitValueString.equalsIgnoreCase("-Infinity") ) {
				Message.printStatus(2,"","Parsed -Infinity row.");
				this.valueMinIsNegativeInfinity = true;
			}
			else if ( limitValueString.startsWith(">=") ) {
				this.valueMinOperator = GE;
				this.valueMinDouble = Double.parseDouble(limitValueString.substring(2).trim());
				Message.printStatus(2,"","Parsed min value GE row.");
			}
			else if ( limitValueString.startsWith(">") ) {
				this.valueMinOperator = GT;
				this.valueMinDouble = Double.parseDouble(limitValueString.substring(1).trim());
				Message.printStatus(2,"","Parsed min value GT row.");
			}
			else {
				// TODO smalers 2021-08-27 need to figure out the best exception.
				throw new RuntimeException ( "Invalid operator for minimum value: " + limitValueString );
			}
		}
		else if ( whichEnd == 1 ) {
			if ( limitValueString.equalsIgnoreCase("Infinity") ) {
				Message.printStatus(2,"","Parsed Infinity row.");
				this.valueMaxIsInfinity = true;
			}
			else if ( limitValueString.startsWith("<=") ) {
				this.valueMaxOperator = LE;
				this.valueMaxDouble = Double.parseDouble(limitValueString.substring(2).trim());
				Message.printStatus(2,"","Parsed max value LE row.");
			}
			else if ( limitValueString.startsWith("<") ) {
				this.valueMaxOperator = LT;
				this.valueMaxDouble = Double.parseDouble(limitValueString.substring(1).trim());
				Message.printStatus(2,"","Parsed max value LT row.");
			}
			else {
				// TODO smalers 2021-08-27 need to figure out the best exception.
				throw new RuntimeException ( "Invalid operator for maximum value: " + limitValueString );
			}
		}
	}

	/**
	 * Evaluate whether a double value is in the range for the row.
	 * @return true if the value is in the range for the row
	 */
	public boolean valueInRange ( double value ) {
		//Message.printStatus(2,"","Checking " + value + " against operator " + this.valueMinOperator + " " +
		//	this.valueMinDouble + " " + this.valueMaxOperator + " " + this.valueMaxDouble );
		if ( this.isNoDataRow ) {
			// Currently don't evaluate NoData values.
			return false;
		}
		// Evaluate the minimum value:
		// - if true after this will evaluate the maximum range value
		boolean result = false;
		if ( this.valueMinIsNegativeInfinity ) {
			// Every value is larger than negative infinity.
			result = true;
		}
		else if ( (this.valueMinOperator == GE) && (value >= this.valueMinDouble) ) {
			result = true;
		}
		else if ( (this.valueMinOperator == GT) && (value > this.valueMinDouble) ) {
			result = true;
		}
		if ( ! result ) {
			// Did not satisfy the minimum (left) criteria so return.
			return false;
		}
		// Evaluate the maximum value:
		// - initialize to false until proven true
		result = false;
		if ( this.valueMaxIsInfinity ) {
			// Every value is less than infinity.
			result = true;
		}
		else if ( (this.valueMaxOperator == LE) && (value <= this.valueMaxDouble) ) {
			result = true;
		}
		else if ( (this.valueMaxOperator == LT) && (value < this.valueMaxDouble) ) {
			result = true;
		}
		return result;
	}
	
}