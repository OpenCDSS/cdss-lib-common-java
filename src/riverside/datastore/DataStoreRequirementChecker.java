// DataStoreRequirementChecker - interface for DataStore requirement check

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2021 Colorado Department of Natural Resources

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

import RTi.Util.IO.RequirementCheck;

/**
Interface to implement features that allow comparing checking a datastore for a requirement,
such as version,
which is used to ensure that command files are run with only appropriate datastore versions.
*/
public interface DataStoreRequirementChecker
{
    
	/**
	 * Check that a datastore's property adheres to a requirement.
	 * The requirement string in RequirementCheck is free format and depends on the datastore,
	 * although standards are being implemented.
	 * @param requirement the full requirement string such as, the following,
	 * which allows full handling of the syntax and logging messages:
	 *   "@require datastore DataStoreName version >= 1.1.0"
	 * @return true if the requirement is met
	 */
	public boolean checkRequirement ( RequirementCheck requirement );

    /**
     * Check that the a datastore's internal version adheres to a requirement.
     * The string that is passed in as requirement should match the syntax used for the datastore so that version strings
     * can be compared consistently.  For example, compare 'YYYYMMDD' strings.
     * @param operator the operator to use to compare the datastore version with 'version',
     * ">", ">=", "<", "<=", "=" or "==".
     * @param version the version to compare to, format depends on the database version format.
     * @return true if 'datastore version' operator 'version' is true, such as YYYYMMDD > YYYYMMDD.
     */
   	//public boolean checkVersion ( String operator, String version );
   	
   	/**
   	 * Return the datastore version string, suitable for version checks.
   	 * It should be a simple string such as YYYYMMDD or semantic version.
   	 * @return the database version string used with 'checkVersion' such as YYYYMMDD.
   	 */
   	//public String getVersionForCheck ();
}