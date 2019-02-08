// TimeSeriesIdentifierProvider - interface that requires the implementing class to return a TSIdent,

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

package RTi.TS;

/**
 * Interface that requires the implementing class to return a TSIdent,
 * useful for UI objects that list time series and need to provide time series identifier
 * to TSTool in a generic way.
 * @author sam
 *
 */
public interface TimeSeriesIdentifierProvider {

	/**
	 * Return a TSIdent object.
	 * The implementing objec may throw an exception but currently a specific
	 * exception type is not required.
	 * @param pos position (0+), used to retrieve identifier from a set, sequence, etc.
	 */
	public TSIdent getTimeSeriesIdentifier(int pos);
}
