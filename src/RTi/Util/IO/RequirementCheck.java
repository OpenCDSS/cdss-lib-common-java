// RequirementCheck - holds input and output from a requirement check

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2021 Colorado Department of Natural Resources

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
 * For example, this is used with TSTool #@require comments.
 * See also the RequirementCheckList class, which manages a list of multiple checks.
 * @author sam
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
	 */
	private boolean isRequirementMet = false;
	
	/**
	 * The reason a requirement failed.
	 */
	private String failReason = "";
	
	/**
	 * Constructor.
	 */
	public RequirementCheck ( String requirementText ) {
		this.requirementText = requirementText;
	}

	/**
	 * Return the full check text, for example:
	 * <pre>
	 *   @require datastore version >= 1.2.3
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
	 */
	public void setIsRequirementMet ( boolean isRequirementMet ) {
		this.setIsRequirementMet ( isRequirementMet, "" );
	}

	/**
	 * Set whether the requirement is met.
	 * @param requirementIsMet whether or not the requirement is met
	 * @param failReason the reason the requirement was not met, not required if requirement was met
	 */
	public void setIsRequirementMet ( boolean isRequirementMet, String failReason ) {
		this.isRequirementMet = isRequirementMet;
		this.failReason = failReason;
	}
	
}