// DataStoreConnectionUIProvider - interface for DataStore UI selector

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

import javax.swing.JFrame;

import RTi.Util.IO.PropList;

/**
Interface for a datastore connection UI which provides an interactive interface to connect to the datastore.
*/
public interface DataStoreConnectionUIProvider
{
    
    /**
     * Open a UI to connect to a datastore, and return the opened datastore or null if canceled.
     * This version is used when a prompt is desired to enter database login credentials at start-up, using properties from a datastore configuration file.
     * @param props properties for the datastore, read from the datastore configuration file
     * @param frame the main UI, used to position the dialog
     * @param return the datastore
     */
    public DataStore openDataStoreConnectionUI ( PropList props, JFrame frame );
    
    /**
     * Open a UI to connect to a datastore, and return the opened datastore or null if canceled.
     * This version is used when (re)connecting to a datastore after initial startup, for example to change users.
     * @param datastoreList a list of ReclamationHDB datastores that were initially configured but may or may not be active/open.
	 * The user will first pick a datastore to access its properties, and will then enter a new login and password for the database connection.
	 * Properties for the datastores are used in addition to the login and password specified interactively to recreate the database connection.
     * @param frame the main UI, used to position the dialog
     * @param return the datastore that was updated
     */
    public DataStore openDataStoreConnectionUI ( List<? extends DataStore> datastoreList, JFrame frame );
    
}
