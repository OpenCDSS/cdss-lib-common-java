// GenericWorksheetData - data object to use in a worksheet that uses the generic table model and cell renderer

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

// ----------------------------------------------------------------------------
// GenericWorksheetData - a data object to use in a worksheet that uses the
// generic table model and cell renderer.
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
// 2003-12-10	J. Thomas Sapienza, RTi	Initial version.
// 2005-04-26	JTS, RTi		Added finalize().
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.util.Date;

import RTi.DMI.DMIUtil;

import RTi.Util.IO.IOUtil;

/**
This class is a data object that can be used in conjunction with the
Generic_TableModel and Generic_CellRenderer classes to display data in a
worksheet, without having to build a specialized table model and renderer 
for the data  <b>For information on how to build a worksheet that uses
generic data, see the documentation for Generic_TableModel</b>.<p>
Currently, there might be problems working with internal data other than
Strings, Integers, Doubles and Dates.
*/
public class GenericWorksheetData {

/**
Actually holds the data.
*/
private Object[] __data = null;

/**
Constructor.
@param columns the number of columns of data that will be stored in the 
data object.  This doesn't include the column that holds the row number.<p>
For instance, in a worksheet that will look similar to the following:<p><pre>
Row #       Name            Number
--------------------------------
1           Fort Collins    100000
2           Greeley         50000
3           Denver          1000000
</pre><p>
The number of columns in the Generic Data would be <b>2</b>, although the 
worksheet would actually have 3 columns.  All the matters is the number of
columns of actual data.
*/
public GenericWorksheetData(int columns) {
	__data = new Object[columns];
	for (int i = 0; i < columns; i++) {
		__data[i] = null;
	}
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	IOUtil.nullArray(__data);
	super.finalize();
}

/**
Returns the number of columns of data in this data object.
@return the number of columns of data in this data object.
*/
public int getColumnCount() {
	return __data.length;
}

/**
Returns a new GenericWorksheetData object that has all the same classes of
data as the one instantiating it.  If a field has a String, Integer, Double,
or Date, it will be filled in with the DMIUtil.MISSING_* version of that
value.  Otherwise, the field will be filled with 'null'.
@return a new GenericWorksheetData object that has all the same classes of
data as the one instantiating it.
*/
public GenericWorksheetData getEmptyGenericWorksheetData() {
	int count = getColumnCount();
	GenericWorksheetData d = new GenericWorksheetData(count);
	
	Object o = null;
	for (int i = 0; i < count; i++) {
		o = getValueAt(i);
		if (o instanceof String) {
			d.setValueAt(i, DMIUtil.MISSING_STRING);	
		}
		else if (o instanceof Integer) {
			d.setValueAt(i, new Integer(DMIUtil.MISSING_INT));
		}
		else if (o instanceof Double) {
			d.setValueAt(i, new Double(DMIUtil.MISSING_DOUBLE));
		}
		else if (o instanceof Date) {
			d.setValueAt(i, DMIUtil.MISSING_DATE);
		}
		else {
			d.setValueAt(i, null);
		}
	}

	return d;
}

/**
Returns the value at the specified position..
@param pos the position at which to return data.
@return the value at the specified position.
*/
public Object getValueAt(int pos) {
	if (pos < 0 || pos > __data.length) {
		return null;
	}

	return __data[pos];
}

/**
Sets the value at the specified position.
@param pos the position at which to set the value.
@param value the value to set.
*/
public void setValueAt(int pos, Object value) {
	__data[pos] = value;
}

/**
Returns a nice-looking String representation of all the data in this object.
@return a nice-looking String representation of all the data in this object.
*/
public String toString() {
	String s = "";
	Object o;
	for (int i = 0; i < __data.length; i++) {
		o = getValueAt(i);
		if (o == null) {
			s += "NULL [class NULL]\n";
		}
		else {
			s += "" + (i + 1) + "]: ";
			if (o instanceof String) {
				s += "'" + o + "'";
			}
			else {
				s += o;
			}
			s += "  [" + o.getClass() + "]\n";
		}
	}
	s += "\n";
	
	return s;
}

}
