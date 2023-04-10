// TSGraphType - time series graph types to be used for graph properties

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

package RTi.GRTS;

/**
Time series graph types to be used for graph properties.
This can be used for a graph space, or for individual time series in a graph space.
Other properties influence the actual appearance (e.g., log/linear axis in conjunction with line graph).
*/
public enum TSGraphType {
    /**
     * Unknown graph type, used to initialize data.
     */
    UNKNOWN("Unknown"),

    /**
     * Used with right-axis graph type to indicate no right-axis graph.
     */
    NONE("None"),

    /**
     * Area graph where time series can overlap.
     */
    AREA("Area"),

    /**
     * Stacked area graph where time series stack on top of each other (don't overlap).
     */
    AREA_STACKED("AreaStacked"),

    /**
     * Bar graph, requires other properties to control the position and width of bars.
     */
    BAR("Bar"),

    /**
     * Double mass graph, used in quality control when comparing time series.
     */
    DOUBLE_MASS("Double-Mass"),

    /**
     * Duration graph, which shows the percentage occurrence of various values.
     */
    DURATION("Duration"),

    /**
     * Line graph.
     */
    LINE("Line"),

    /**
     * Period of record graph, horizontal bars showing when data values are present.
     */
    PERIOD("PeriodOfRecord"),

    /**
     * Point graph, not connected by lines.
     */
    POINT("Point"),

    /**
     * Used with regression to shown what values would result from regression.
     */
    PREDICTED_VALUE("PredictedValue"),

    /**
     * Used with regression to shown the difference between actual and predicated values.
     */
    PREDICTED_VALUE_RESIDUAL("PredictedValueResidual"),

    /**
     * Raster (heat map) graph.
     */
    RASTER("Raster"),

    /**
     * Plot a time series on each axis using points for matching times.
     */
    XY_SCATTER("XY-Scatter");

    private final String displayName;

    /**
     * Name that should be displayed in choices, etc.
     * @param displayName
     */
    private TSGraphType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Return the display name.
     * @return the display name.
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Return the enumeration value given a string name (case-independent).
     * @return the enumeration value given a string name (case-independent), or null if not matched.
     */
    public static TSGraphType valueOfIgnoreCase(String name) {
        if ( name == null ) {
            return null;
        }
        TSGraphType [] values = values();
        for ( TSGraphType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        }
        return null;
    }
}