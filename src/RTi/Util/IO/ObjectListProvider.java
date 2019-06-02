// ObjectListProvider - this interface defines behavior to return a list of objects of a certain class type.

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

package RTi.Util.IO;

import java.util.List;

/**
This interface defines behavior to return a list of objects of a certain class type.
This is useful, for example, if some class maintains a list of objects and those
objects need to be listed in some other code, for example in a UI choice.
*/
public interface ObjectListProvider
{
    /**
     * Return a List of objects of the requested class type.
     * @return a List of objects of the requested class type, or null if no objects of
     * the requested type are available.
     * @param c Class to return.
     */
    public <T> List<T> getObjectList ( Class<T> c );
}