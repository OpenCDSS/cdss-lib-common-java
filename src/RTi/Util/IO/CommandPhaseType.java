// CommandPhaseType - this class provides an enumeration of possible command phase values

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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
This class provides an enumeration of possible command phase values.
*/
public enum CommandPhaseType {

	/**
	Used when the phase is not important, such as counting log messages of a status type.
	*/
	ANY (-1, "ANY"),

	/**
	When used with Command processing,
	INITIALIZATION indicates that the command is being initialized,
	including construction and validation of parameters.
	*/
	INITIALIZATION (0, "INITIALIZATION"),

	/**
	 * DISCOVERY is the phase where a command is partially run, in order to provide data to dependent commands.
	 */
	DISCOVERY (1, "DISCOVERY"),

	/**
	 * RUN indicates the phase where the command is being run.
	 */
	RUN (2, "RUN");

	//private int __type;
	private String __typename;

	private CommandPhaseType ( int type, String typename ) {
		//__type = type;
		__typename = typename;
	}

	/**
	 * Return a String representation of the command processing phase.
	 */
	public String toString () {
		return __typename;
	}
}