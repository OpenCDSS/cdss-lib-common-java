// DataUnits_TableModel - table model for displaying data table data in a JWorksheet.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2022 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;

/**
Table model for displaying data table data in a JWorksheet.
*/
@SuppressWarnings("serial")
public class DataUnits_TableModel extends JWorksheet_AbstractRowTableModel<DataUnits>
{

/**
Number of columns in the table model (with the alias).
*/
private int __COLUMNS = 9;

/**
Absolute column indices, for column lookups.
*/
public final int COL_DIMENSION = 0;
public final int COL_IS_BASE = 1;
public final int COL_ABBREVIATION = 2;
public final int COL_SYSTEM = 3;
public final int COL_LONG_NAME = 4;
public final int COL_PRECISION = 5;
public final int COL_MULT = 6;
public final int COL_ADD = 7;
public final int COL_SOURCE = 8;

/**
Constructor.
@param dataUnitsList the list of data units to show in a worksheet.
@throws NullPointerException if the dataTable is null.
*/
public DataUnits_TableModel(List<DataUnits> dataUnitsList) 
throws Exception {
    if ( dataUnitsList == null ) {
        _rows = 0;
    }
    else {
        _rows = dataUnitsList.size();
    }
    _data = dataUnitsList;
}

/**
From AbstractTableModel.  Returns the class of the data stored in a given column.
All values are treated as strings.
@param columnIndex the column for which to return the data class.
*/
@SuppressWarnings({ "unchecked", "rawtypes" })
public Class getColumnClass (int columnIndex) {
    switch (columnIndex) {
        case COL_DIMENSION: return String.class;
        case COL_IS_BASE: return String.class;
        case COL_ABBREVIATION: return String.class;
        case COL_SYSTEM: return String.class;
        case COL_LONG_NAME: return String.class;
        case COL_PRECISION: return String.class;
        case COL_MULT: return String.class;
        case COL_ADD: return String.class;
        case COL_SOURCE: return String.class;
        default: return String.class;
    }
}

/**
From AbstractTableMode.  Returns the number of columns of data.
@return the number of columns of data.
*/
public int getColumnCount() {
    return __COLUMNS;
}

/**
From AbstractTableMode.  Returns the name of the column at the given position.
@return the name of the column at the given position.
*/
public String getColumnName(int columnIndex) {
    switch (columnIndex) {
        case COL_DIMENSION: return "\n\nDimension";
        case COL_IS_BASE: return "Is Base\nUnit for\nDimension?";
        case COL_ABBREVIATION: return "\n\nAbbreviation";
        case COL_SYSTEM: return "\nUnits\nSystem";
        case COL_LONG_NAME: return "\nLong\nName";
        case COL_PRECISION: return "\n\nPrecision";
        case COL_MULT: return "Base\nMultiply\nFactor";
        case COL_ADD: return "Base\nAdd\nFactor";
        case COL_SOURCE: return "\n\nSource for Units";
        default: return "";
    }
}

/**
Return tool tips for the columns.
*/
public String[] getColumnToolTips() {
    String [] tooltips = new String[__COLUMNS];
    tooltips[COL_DIMENSION] = "Dimension abbreviation (e.g., L=Length)";
    tooltips[COL_IS_BASE] = "Is the base unit? Add and multiply factors are relative to base units.";
    tooltips[COL_ABBREVIATION] = "Units abbreviation, often displayed with data.";
    tooltips[COL_SYSTEM] = "Whether English, SI, or universal units.";
    tooltips[COL_LONG_NAME] = "Long name for units.";
    tooltips[COL_PRECISION] = "Default precision for units in output.";
    tooltips[COL_MULT] = "Multiple of base units.";
    tooltips[COL_ADD] = "Shift from base units, for temperatures.";
    tooltips[COL_SOURCE] = "Source of the data units definition.";
    return tooltips;
}

/**
Returns the format to display the specified column.
@param column column for which to return the format.
@return the format (as used by StringUtil.formatString()).
*/
public String getFormat ( int column ) {
    switch (column) {
        default:    return "%s";
    }
}

/**
From AbstractTableMode.  Returns the number of rows of data in the table.
*/
public int getRowCount() {
    return _rows;
}

/**
From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
@param row the row for which to return data.
@param col the absolute column for which to return data.
@return the data that should be placed in the JTable at the given row and column.
*/
public Object getValueAt(int row, int col)
{   // Make sure the row numbers are never sorted.

    if (_sortOrder != null) {
        row = _sortOrder[row];
    }

    DataUnits dataUnits = _data.get(row);
    if ( dataUnits == null ) {
        return "";
    }
    switch (col) {
        case COL_DIMENSION: return dataUnits.getDimension().getAbbreviation();
        case COL_IS_BASE:
            if ( dataUnits.getBaseFlag() == 1 ) {
                return "Y";
            }
            else {
                return "N";
            }
        case COL_ABBREVIATION: return dataUnits.getAbbreviation();
        case COL_SYSTEM: return dataUnits.getSystemString();
        case COL_LONG_NAME: return dataUnits.getLongName();
        case COL_PRECISION: return "" + dataUnits.getOutputPrecision();
        case COL_MULT: return "" + dataUnits.getMultFactor();
        case COL_ADD: return "" + dataUnits.getAddFactor();
        case COL_SOURCE: return "" + dataUnits.getSource();
        default: return "";
    }
}

/**
Returns an array containing the column widths (in number of characters).
@return an integer array containing the widths for each field.
*/
public int[] getColumnWidths() {
    int[] widths = new int[__COLUMNS];
    widths[COL_DIMENSION] = 7;
    widths[COL_IS_BASE] = 8;
    widths[COL_ABBREVIATION] = 8;
    widths[COL_SYSTEM] = 4;
    widths[COL_LONG_NAME] = 20;
    widths[COL_PRECISION] = 6;
    widths[COL_MULT] = 8;
    widths[COL_ADD] = 8;
    widths[COL_SOURCE] = 40;
    return widths;
}

}