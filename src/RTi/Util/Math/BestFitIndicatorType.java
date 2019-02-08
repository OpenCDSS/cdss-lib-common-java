// BestFitIndicatorType - best fit indicators that may be determined for an analysis

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

package RTi.Util.Math;

/**
Best fit indicators that may be determined, for example when performing a regression analysis.
*/
public enum BestFitIndicatorType
{
    /**
     * Nash-Sutcliffe model efficiency coefficient.
    */
    NASH_SUTCLIFFE("NashSutcliffeEfficiency"),
    /**
     * Correlation coefficient.
     */
    R("R"),
    /**
     * Standard Error of Prediction, defined as the square root
     * of the sum of squared differences between the known dependent value, and the value
     * determined from the equation used to estimate the data.
     */
    SEP("SEP"),
    /**
     * The following is used with TSTool's Mixed Station Analysis and indicates that monthly
     * equations are used but the error is the total of all months.
     */
    SEP_TOTAL("SEPTotal"),
    /**
     * The following is used for filling non-mixed station, to allow for the same code without requiring sorting
     */
    NONE("None");
    
    /**
     * The name that should be displayed when the best fit type is used in UIs and reports.
     */
    private final String displayName;

    /**
     * Construct an enumeration value.
     * @param displayName name that should be displayed in choices, etc.
     */
    private BestFitIndicatorType(String displayName) {
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
    public static BestFitIndicatorType valueOfIgnoreCase(String name)
    {
        BestFitIndicatorType [] values = values();
        for ( BestFitIndicatorType t : values ) {
            if ( name.equalsIgnoreCase(t.toString()) ) {
                return t;
            }
        } 
        return null;
    }
}
