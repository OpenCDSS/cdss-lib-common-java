// Table - class to hold tabular data and perform lookups

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

/**
 *
 * Created on April 18, 2007, 9:52 AM
 *
 */
package riverside.ts.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//import java.util.logging.Logger;

/**
 * A 2-column lookup table implementation taken from the National Weater Service Streamflow Forecast System
 * ResJ operation C/C++ code. Modifications include
 * making it a bit more useful and less hazardous to use.
 * @todo evaluate whether this thing should live...
 * @author iws
 */
public class Table {

    private double[] column1;
    private double[] column2;
    private String id;
    private double missingValue = -999;
    private boolean modified = false;
    public static final int GETCOLUMN_1 = 0;
    public static final int GETCOLUMN_2 = 1;

    public static enum InterpolationMode {

        LINEAR, LOGARITHMIC;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void setMissingValue(double missing) {
        this.missingValue = missing;
    }

    public double getMissingValue() {
        return missingValue;
    }

    public double[] getColumn(int col) {
        double[] ac = col == 0 ? column1 : column2;
        return ac == null ? null : (double[]) ac.clone();
    }

    /**
     * Allocate the memory for the 2-column table and initialize to the missing value, -999.0.
     * The table is marked as modified.
     * @param size number of rows.
     */
    public void allocateDataSpace(int size) {
        column1 = new double[size];
        column2 = new double[size];
        Arrays.fill(column1, missingValue);
        Arrays.fill(column2, missingValue);
        modified = true;
    }

    public void freeDataSpace() {
        column1 = column2 = null;
    }

    public String getID() {
        return id;
    }

    /**
     * Return the value for a table cell.
     * @param row row index (0+).
     * @param col column index (0 or 1).
     * @return value at the given cell address.
     * @exception ArrayIndexOutOfBoundsException if an invalid row or column index is specified.
     */
    public double get(int row, int col) {
        if ((col != 0) && (col != 1)) {
            throw new ArrayIndexOutOfBoundsException("Column " + col + " requested for Table - must be 0 or 1.");
        }
        if ((row < 0) || (row > getNRows() - 1)) {
            throw new ArrayIndexOutOfBoundsException("Row " + row + " requested for Table - must be in range 0 - " +
                    (getNRows() - 1) + ".");
        }
        if (col == 0) {
            return column1[row];
        } else {
            return column2[row];
        }
    }

    public int getNRows() {
        return column1 == null ? 0 : column1.length;
    }

    private double min(double[] d) {
        double min = 0;
        if (d != null) {
            min = Double.MAX_VALUE;
            for (int i = 0; i < d.length; i++) {
                double d2 = d[i];
                if (d2 < min) {
                    min = d2;
                }
            }
        }
        return min;
    }

    private double max(double[] d) {
        double max = 0;
        if (d != null) {
            max = -Double.MAX_VALUE;
            for (int i = 0; i < d.length; i++) {
                double d2 = d[i];
                if (d2 > max) {
                    max = d2;
                }
            }
        }
        return max;
    }

    public void set(int row, double c1, double c2) {
        column1[row] = c1;
        column2[row] = c2;
        modified = true;
    }

    public double getMin(int num) {
        if (num == GETCOLUMN_1) {
            return min(column1);
        } else if (num == GETCOLUMN_2) {
            return min(column2);
        }
        throw new IllegalArgumentException("" + num);
    }

    public double getMax(int num) {
        if (num == GETCOLUMN_1) {
            return max(column1);
        } else if (num == GETCOLUMN_2) {
            return max(column2);
        }
        throw new IllegalArgumentException("" + num);
    }

    public double lookup(int row, int col) {
        if (col == GETCOLUMN_1) {
            return column1[row];
        } else if (col == GETCOLUMN_2) {
            return column2[row];
        }
        throw new IllegalArgumentException("" + col);
    }

    public double lookup(double val, int col, boolean allowBounds) {
        return lookup(val, col, allowBounds, InterpolationMode.LINEAR);
    }

    public double lookup(double val, int col, boolean allowBounds, InterpolationMode mode) {
        double[] lookupColumn, valueColumn;
        if (col == 0) {
            lookupColumn = column2;
            valueColumn = column1;
        } else {
            lookupColumn = column1;
            valueColumn = column2;
        }
        if (val < lookupColumn[0]) {
            if (allowBounds) {
                return valueColumn[0];
            }
            return missingValue;
        }
        int end = lookupColumn.length - 1;
        if (val > lookupColumn[end]) {
            if (allowBounds) {
                return valueColumn[end];
            }
            return missingValue;
        }
        double retVal = missingValue;
        if (mode == null) {
            for (int i = 0; i < lookupColumn.length; i++) {
                if (lookupColumn[i] == val) {
                    retVal = valueColumn[i];
                    break;
                }
            }
        } else {
            retVal = interpolate(val, lookupColumn, valueColumn, mode);
        }
        return retVal;
    }

    public void populate(double[] c1, double[] c2, int size) {
        allocateDataSpace(size);
        System.arraycopy(c1, 0, column1, 0, c1.length);
        System.arraycopy(c2, 0, column2, 0, c2.length);
        modified = true;
    }

    public void populate(int row, int col, double value) {
        if (col == 0) {
            column1[row] = value;
        } else {
            column2[row] = value;
        }
        modified = true;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void sort(final int column) {
        // lets not mess around here, just put all the rows in a list and
        // sort them based on column then copy values back
        List<double[]> rows = new ArrayList<double[]>(column1.length);
        for (int i = 0; i < column1.length; i++) {
            rows.add(new double[]{column1[i], column2[i]});
        }
        Collections.sort(rows, new Comparator<double[]>() {

            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[column], o2[column]);
            }
        });
        for (int i = 0; i < column1.length; i++) {
            double[] row = rows.get(i);
            column1[i] = row[0];
            column2[i] = row[1];
        }
    }

    /* TODO smalers 2019-06-01 evaluate use of function - this code is not actively used.
    private static Logger logger() {
        return Logger.getLogger(Table.class.getName());
    }
    */

    public int getSmallerIndex(double value, int column) {
        double[] d = column == GETCOLUMN_1 ? column1 : column2;
        int idx = 0;
        for (int i = 0; i < d.length; i++) {
            if (value >= d[i]) {
                idx = i;
            }
        }
        return idx;
    }

    public int getLargerIndex(double value, int column) {
        double[] d = column == GETCOLUMN_1 ? column1 : column2;
        int idx = d.length - 1;
        for (int i = d.length - 1; i >= 0; i--) {
            if (value <= d[i]) {
                idx = i;
            }
        }
        return idx;
    }

    /**
     * Create an empty 2-column table (zero rows).
     */
    public Table() {
    }

    /**
     * Create a new 2-column table with given number of rows and initialized to missing values
     * @param size number of rows in the table.
     */
    public Table(int size) {
        allocateDataSpace(size);
    }

    public Table(Table copy) {
        if (copy.column1 != null) {
            column1 = copy.column1.clone();
        }
        if (copy.column2 != null) {
            column2 = copy.column2.clone();
        }
        id = copy.id;
        missingValue = copy.missingValue;
    }

    public static Table create(double... data) {
        Table t = new Table(data.length / 2);
        for (int i = 0; i < data.length; i += 2) {
            t.set(i / 2, data[i], data[i + 1]);
        }
        return t;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, ii = getNRows(); i < ii; i++) {
            sb.append(column1[i]).append(" : ").append(column2[i]);
            if (i + 1 < ii) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private double interpolate(double x, double[] lookupColumn, double[] valueColumn, InterpolationMode mode) {
        for (int i = 0, ii = lookupColumn.length - 1; i < ii; i++) {
            if (x == lookupColumn[i]) {
                return valueColumn[i];
            }
            int j = i + 1;
            if (x > lookupColumn[i] && x < lookupColumn[j]) {
                double xmin = lookupColumn[i];
                double xmax = lookupColumn[j];
                double ymin = valueColumn[i];
                double ymax = valueColumn[j];
                double y;
                if (mode == InterpolationMode.LINEAR) {
                    if (xmax - xmin == 0) {
                        y = ymin;
                    } else {
                        y = ymin + (ymax - ymin) * (x - xmin) / (xmax - xmin);
                    }
                } else {
                    double bar = xmax - xmin == 0 ? 0 : (x - xmin) / (xmax - xmin);
                    y = Math.log10(ymin) + (Math.log10(ymax) - Math.log10(ymin)) * bar;
                    y = Math.pow(10, y);
                }
                return y;
            }
        }
        if (x == lookupColumn[lookupColumn.length - 1]) {
            return valueColumn[lookupColumn.length - 1];
        }
        throw new IllegalArgumentException(x + ":" + Arrays.toString(lookupColumn));
    }
}
