// DataStoreFactory - interface to define DataStore factory behavior

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

package riverside.datastore;

import RTi.Util.IO.PropList;

/**
 * Define implementation minimum for a DataStore factory.  Because properties are often read from
 * INI format property files, use a PropList for properties.
 * @author sam
 */
public interface DataStoreFactory
{
    /**
     * Instantiate a DataStore from a PropList of string properties.
     * @param props properties to describe the DataStore configuration.
     */
    public DataStore create ( PropList props );
}
