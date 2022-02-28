// DataStoreSubstitute - store datastore substitute information

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

import java.util.List;

public class DataStoreSubstitute {
	
	/*
	 * Datastore name to use.
	 */
	private String datastoreNameToUse = "";

	/*
	 * Datastore name in commands.
	 */
	private String datastoreNameInCommands = "";

	/**
	 * Constructor.
	 * @param datastoreNameToUse the datastore name to use.
	 * @param datastoreNameInCommands the datastore name in commands.
	 */
	public DataStoreSubstitute ( String datastoreNameToUse, String datastoreNameInCommands ) {
		this.datastoreNameToUse = datastoreNameToUse;
		this.datastoreNameInCommands = datastoreNameInCommands;
	}

	/**
	 * Return the datastore name to use.
	 */
	public String getDatastoreNameToUse() {
		return this.datastoreNameToUse;
	}

	/**
	 * Return the datastore name in commands.
	 */
	public String getDatastoreNameInCommands() {
		return this.datastoreNameInCommands;
	}

	/*
	 * Lookup a DataStoreSubstitute given the datastore name to use.
	 * @param dastastoreSubstituteList list to search
	 * @param dataStoreNameToUse the datastore name to use, to match
	 * @return the matching DataStoreSubstitute, or null if not matched
	 */
    public static DataStoreSubstitute lookupForNameToUse( List<DataStoreSubstitute> datastoreSubstituteList, String dataStoreNameToUse ) {
    	for ( DataStoreSubstitute dssub : datastoreSubstituteList ) {
    		if ( dssub.getDatastoreNameToUse().equals(dataStoreNameToUse)) {
    			return dssub;
    		}
    	}
    	return null;
    }
}