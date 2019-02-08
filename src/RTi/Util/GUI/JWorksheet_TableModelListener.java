// JWorksheet_TableModelListener - interface for classes that want to respond to JWorksheet table model events

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
// JWorksheet_TableModelListener - An interface for classes that want to 
//	respond to JWorksheet table model events.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2003-10-277	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This interface is for classes that want to be able to respond to JWorksheet
table model events, such as when data are changed 
*/
public interface JWorksheet_TableModelListener {

/**
Called when a data value is changed.
@param rowNumber the number of the row in which data were changed
@param colNumber the number of the column in which data were changed
@param value the new value of the cell corresponding to the row and column
(must be cast appropriately based on the table model).
*/
public void tableModelValueChanged(int rowNumber, int colNumber, Object value);

}
