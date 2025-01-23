// CommandStatusType - this class provides an enumeration of possible command status values.

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

import java.security.InvalidParameterException;

/**
This class provides an enumeration of possible command status values.
This is used for the status for command status log messages and the overall command status.
*/
public enum CommandStatusType {

    /**
     * UNKNOWN indicates that the command could not be executed (no results).
     */
    UNKNOWN (-1, "UNKNOWN", "Unknown"),

    /**
     * When used with Command processing, INFO indicates information relevant to a command,
     * perhaps to explain a warning that might come up later.
     */
    INFO (-2, "INFO", "Info"),

    /**
     * When used with Command processing, NOTIFICATION indicates a notification for a command,
     * which includes newer @version available, @todo, or @fixme annotations.
     * This type is typically only used with comments.
     * This is additive to SUCCESS, WARNING, and FAILURE and is used to decorate commands
     */
    NOTIFICATION (-3, "NOTIFICATION", "Notification"),

	/**
	 * When used with Command processing, SUCCESS indicates that results could be generated, with no warnings.
	 */
	SUCCESS (0, "SUCCESS", "Success"),

	/**
	 * WARNING indicates that partial results were generated,
	 * but which may be in error due to initialization or runtime errors.
	 */
	WARNING (1, "WARNING", "Warning"),

	/**
	 * FAILURE indicates that the command could not be executed (no results).
	 */
	FAILURE (2, "FAILURE", "Failure");

    /**
	 * Status as an integer, used to set severity, with 0 being success similar to Linux error code and larger number indicating more severe issue.
	 */
	private int type;

	/**
	 * Status type name, upper case (e.g., "SUCCESS", "FAILURE")
	 */
	private String typename;

	/**
	 * Status type name, mixed case (e.g., "Success", "Failure")
	 */
	private String typenameMixed;

	/**
	 * Construct the status type using the type/severity and name.
	 * It is private because other code should use the predefined instances.
	 * @param type command status type as an integer
	 * @param typename command status type name (upper case)
	 * @param typenameMixed command status type name (mixed case)
	 */
	private CommandStatusType ( int type, String typename, String typenameMixed ) {
		this.type = type;
		this.typename = typename;
		this.typenameMixed = typenameMixed;
	}

	/**
	 * Determine if two types are equal based on the type integer.
	 * @param type another CommandStatusType to compare to
	 * @return true if equal and false if not
	 */
	public boolean equals ( CommandStatusType type ) {
		if ( this.type == type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Return the severity of the status (larger number means more severe problem).
	 * This is useful for ranking the severity of problems for output.
	 * @return the severity of the problem.
	 */
    public int getSeverity() {
        return this.type;
    }

	/**
	 * Determine if a status severity is greater than the current status.
	 * For example, use this to check whether a command status type is greater than CommandStatusType.SUCCESS.
	 * @param type Command status severity.
	 * @return true if the provided severity is greater than that of the instance.
	 */
	public boolean greaterThan ( CommandStatusType type ) {
		if ( this.type > type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Determine if a status severity is greater than or equal to the current status.
	 * For example, use this to check whether a command status type is greater than or equal to CommandStatusType.WARNING.
	 * @param type Command status severity.
	 * @return true if the provided severity is greater than that of the instance.
	 */
	public boolean greaterThanOrEqualTo ( CommandStatusType type ) {
		if ( this.type >= type.getSeverity() ) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	Determine the maximum severity.
	@param status1 the first status to compare
	@param status2 the second status to compare
	@return the status that is the most severe from the two status.
	*/
	public static CommandStatusType maxSeverity ( CommandStatusType status1, CommandStatusType status2 ) {
		int severity1 = status1.getSeverity();
		int severity2 = status2.getSeverity();
		if ( severity1 > severity2 ) {
			return status1;
		}
		else {
			return status2;
		}
	}

	/**
	 * Parse the command status type and return an instance of the enumeration.
	 * @param cst CommandStatusType string to parse.
	 * @return an instance of the enumeration that matches the string.
	 * @exception InvalidParameterException if the requested string does not match a command status type.
	 */
	public static CommandStatusType parse ( String cst ) {
		if ( cst.equalsIgnoreCase(UNKNOWN.toString())) {
			return UNKNOWN;
		}
		else if ( cst.equalsIgnoreCase(INFO.toString())) {
			return INFO;
		}
		else if ( cst.equalsIgnoreCase(NOTIFICATION.toString())) {
			return NOTIFICATION;
		}
		else if ( cst.equalsIgnoreCase(SUCCESS.toString())) {
			return SUCCESS;
		}
		else if ( cst.equalsIgnoreCase(WARNING.toString()) || cst.equalsIgnoreCase("warn")) {
			return WARNING;
		}
		else if ( cst.equalsIgnoreCase(FAILURE.toString()) || cst.equalsIgnoreCase("fail")) {
			return FAILURE;
		}
		else {
			throw new InvalidParameterException ( "The command status type \"" + cst + "\" is not a recognized type.");
		}
	}

	/**
	 * Return a String representation of the command status, upper case, as follows:
	 * <pre>
	 * UNKNOWN - status is unknown (not implemented or not initialized)
	 * INFO - informational message
	 * NOTIFICATION - status is notification
	 * SUCCESS - command was successful (no WARNING or FAILURE)
	 * WARNING - command completed but user should review possible problem
	 * FAILURE - command failed and results are very likely not complete or accurate
	 * </pre>
	 */
	public String toString () {
		return this.typename;
	}

	/**
	 * Return a String representation of the command status, mixed case.
	 */
	public String toMixedCaseString () {
		return this.typenameMixed;
	}
}