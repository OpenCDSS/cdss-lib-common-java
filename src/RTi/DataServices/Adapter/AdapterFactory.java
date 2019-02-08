// AdapterFactory - interface to build a Data Services adapter factories.

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

//------------------------------------------------------------------------------------
// AdapterFactory - interface to build a Data Services adapter factories.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-16      Scott Townsend, RTi     Create initial version of this
//                                              Adapter Factory interface. This
//						interface will help developers
//						build new dynamic Data Services 
//						adapters as the data become available.
//------------------------------------------------------------------------------------
// Endheader

package RTi.DataServices.Adapter;

import RTi.DataServices.Adapter.Adapter;
import RTi.DataServices.Adapter.ConfigurationFile;

/**
 * Interface of a Factory for instantiating specific adapters determined from a
 * configuration file.
 * 
 */

public interface AdapterFactory {

/**
 * The returned Adapter instance created in this factory for passing through
 * exception handling
*/
Adapter __DSAdapter = null;

/**
 * The returned ConfigurationFile instance created in this factory for passing through
 * exception handling
*/
ConfigurationFile __DSConfigurationFile = null;

} // End of interface AdapterFactory

