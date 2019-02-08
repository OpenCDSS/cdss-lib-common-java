// ProcessManagerOutputFilter - filter output from the process that is being run by the ProcessManager.

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

/**
Filter output from the process that is being run by the ProcessManager.
This is useful when an external process produces a large amount of output
that would normally slow down the 
*/
public interface ProcessManagerOutputFilter
{
	/**
	 * Filter the program output that is processed by the ProcessManager, for
	 * example to limit the amount of output that is being displayed by the
	 * ProcessManagerJDialog.
	 * @param line Line to evaluate.
	 * @return the String to output, or null if the line should not be output.
	 */
	public String filterOutput ( String line );
}
