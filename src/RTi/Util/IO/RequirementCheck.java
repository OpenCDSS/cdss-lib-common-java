// RequirementCheck - holds input and output from a requirement check

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.IO;

/**
 * This class holds input and output from requirements checks.
 * For example, this is used with TSTool #@require and #@enabledif comment annotations.
 * See also the RequirementCheckList class, which manages a list of multiple checks.
 * @see RequirementCheckList
 *
 */
public class RequirementCheck {

	/**
	 * The original requirement string, for example an annotation in TSTool.
	 */
	private String requirementText = "";

	/**
	 * Whether the requirement is met.
	 * Default is false until check occurs and passes.
	 */
	private boolean isRequirementMet = false;

	/**
	 * The reason a requirement failed.
	 */
	private String failReason = "";

	/**
	 * Whether processing should exit if the requirement is not met.
	 */
	private boolean shouldExitIfRequirementNotMet = false;

	/**
	 * Name of the object that performed the check, useful for troubleshooting when multiple checkers are involved
	 * for application, datastore, etc.
	 */
	private String checkerName = "Unknown";

	/**
	 * Annotation "@require" or "@enabledif" associated with the check, first part of 'requirementText'.
	 */
	private String annotation = "@??????";

	/**
	 * Constructor.
	 * @param requirementText the full requirement starting with "@"
	 * (but omitting leading # and possibly whitespace before @).
	 */
	public RequirementCheck ( String requirementText ) {
		this.requirementText = requirementText.trim();
		// Also set the annotation associated with the requirement.
		if ( requirementText.startsWith("@") ) {
			// Expect a space after the annotation name.
			int pos = requirementText.indexOf(" ");
			if ( pos > 0 ) {
				this.annotation = requirementText.substring(0,pos).trim();
			}
		}
	}

	/**
	 * Return the annotation associated with the requirement ("@require" or "@enabledif").
	 * @return the annotation associated with the requirement
	 */
	public String getAnnotation () {
		return this.annotation;
	}

	/**
	 * Return the name of the checker, useful in troubleshooting and development.
	 * @return the name of the checker
	 */
	public String getCheckerName () {
		return this.checkerName;
	}

	/**
	 * Return the full check text, for example:
	 * <pre>
	 *   @require application AppName version >= 1.2.3
	 *   @require datastore DataStoreName version >= 1.2.3
	 *   @require user != root
	 *
	 *   @enabledif application AppName version >= 1.2.3
	 *   @enabledif datastore DataStoreName version >= 1.2.3
	 *   @enabledif user != root
	 * </pre>
	 * @return
	 */
	public String getRequirementText () {
		return this.requirementText;
	}

	/**
	 * Return the reason that a requirement was not met.
	 * This text is formed by code that does the check and should be human-readable,
	 * for example:  "Datastore does not provide a version."
	 * @return reason that a requirement was not met
	 */
	public String getFailReason () {
		return this.failReason;
	}

	/**
	 * Return whether the requirement is met.
	 * @return whether the requirement is met
	 */
	public boolean isRequirementMet () {
		return this.isRequirementMet;
	}

	/**
	 * Set whether the requirement is met.
	 * @param requirementIsMet whether or not the requirement is met
	 * @deprecated use the version with checkerName
	 */
	@Deprecated
	public void setIsRequirementMet ( boolean isRequirementMet ) {
		this.setIsRequirementMet ( isRequirementMet, "" );
	}

	/**
	 * Set whether the requirement is met.
	 * @param checkerName the name of the checker that performed the check (e.g., datastore type)
	 * @param requirementIsMet whether or not the requirement is met
	 */
	public void setIsRequirementMet ( String checkerName, boolean isRequirementMet ) {
		this.setIsRequirementMet ( checkerName, isRequirementMet, "" );
	}

	/**
	 * Set whether the requirement is met.
	 * @param requirementIsMet whether or not the requirement is met
	 * @param failReason the reason the requirement was not met, not required if requirement was met
	 * @deprecated use the version with checkerName
	 */
	@Deprecated
	public void setIsRequirementMet ( boolean isRequirementMet, String failReason ) {
		this.isRequirementMet = isRequirementMet;
		this.failReason = failReason;
	}

	/**
	 * Set whether the requirement is met.
	 * @param checkerName the name of the checker that performed the check (e.g., datastore type)
	 * @param requirementIsMet whether or not the requirement is met
	 * @param failReason the reason the requirement was not met, not required if requirement was met
	 */
	public void setIsRequirementMet ( String checkerName, boolean isRequirementMet, String failReason ) {
		this.checkerName = checkerName;
		this.isRequirementMet = isRequirementMet;
		this.failReason = failReason;
	}

	/**
	 * Set whether should exit if requirement is not met.
	 * @param exitIfRequirementNotMet whether or not to exit if the requirement is not met
	 */
	public void setShouldExitIfRequirementNotMet ( boolean exitIfRequirementNotMet ) {
		this.shouldExitIfRequirementNotMet = exitIfRequirementNotMet;
	}

	/**
	 * Indicate whether processing should exit if the requirement is not met.
	 */
    public boolean shouldExitIfRequirementNotMet() {
    	return this.shouldExitIfRequirementNotMet;
    }

    /**
     * Return a formatted version of the requirement check, suitable for command status.
     */
    public String toString () {
    	if ( this.isRequirementMet ) {
    		return this.checkerName + ": " + this.annotation + " condition is met";
    	}
    	else {
    		return this.checkerName + ": " + this.annotation + " condition is NOT met\nReason: " + this.failReason;
    	}
    }

}