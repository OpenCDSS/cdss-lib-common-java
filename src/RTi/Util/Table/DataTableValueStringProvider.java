// DataTableValueStringProvider - interface defines behavior for retrieving object from a DataTable

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

package RTi.Util.Table;

/**
 * The DataTableValueStringProvider interface defines behavior for retrieving object from a DataTable.
 * Implementation of this interface is useful when manipulating the data for a cell
 * prior to setting or after retrieval.
 * @author sam
 *
 */
public interface DataTableValueStringProvider {

	/**
	 * Return the object value in a table cell.
	 * @param valueFormat a format string to be interpreted by called code,
	 * for example ${Property} when used with TSTool.
	 */
	public String getTableCellValueAsString ( String valueFormat );
}
