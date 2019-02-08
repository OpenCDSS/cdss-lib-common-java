// ProcessRunner - this interface indicates that a class runs one or more processes and allows the process instances to be retrieved.

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
This interface indicates that a class runs one or more processes and allows the process instances to be retrieved.
One use is to allow a program to kill processes if they are misbehaving.
*/
public interface ProcessRunner {

/**
Return the list of processes being managed by an object.
@return the list of processes being managed by an object.
*/
public List<Process> getProcessList ();

}
