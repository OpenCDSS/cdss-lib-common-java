// RequirementCheckList - holds input and output from a list of requirement checks

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

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds a list of requirement checks and facilitates overall evaluate of whether
 * requirements have passed.
 * For example, this is used with TSTool #@require comments.
 * See also the RequirementCheck class, which holds a single requirement check.
 * @author sam
 * @see RequirementCheck
 *
 */
public class RequirementCheckList {
	
	/**
	 * The list of requirement checks.
	 */
	private List<RequirementCheck> requirementCheckList = new ArrayList<>();

	/**
	 * Construct an empty list.
	 */
	public RequirementCheckList () {
		
	}
	
	/**
	 * Add a requirement check.
	 * @param check the requirement check to add
	 */
	public void add ( RequirementCheck check ) {
		this.requirementCheckList.add(check);
	}

	/**
	 * Return whether the requirements are all met.
	 * @return whether the requirements are all met
	 */
	public boolean areRequirementsMet () {
		int metCount = 0;
		int notMetCount = 0;
		for ( RequirementCheck check : this.requirementCheckList ) {
			if ( check.isRequirementMet() ) {
				++metCount;
			}
			else {
				++notMetCount;
			}
		}
		if ( (metCount > 0) && (notMetCount == 0) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Format the check results, for example for use in command status.
	 * @return a string with each criteria that did not meet the criteria
	 */
	public String formatResults () {
		StringBuilder b = new StringBuilder();
		for ( RequirementCheck check : this.requirementCheckList ) {
			if ( !check.isRequirementMet() ) {
				if ( b.length() > 0 ) {
					b.append("\n");
				}
				b.append(check.getFailReason());
			}
		}
		return b.toString();
	}
	
	/**
	 * Return the RequirementCheck at the requested position.
	 * @param pos position for the check, 0+
	 * @return RequirementCheck instance at requested position
	 */
	public RequirementCheck get ( int pos ) {
		return this.requirementCheckList.get(pos);
	}
}