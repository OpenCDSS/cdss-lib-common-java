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

