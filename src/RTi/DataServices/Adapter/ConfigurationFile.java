// ConfigurationFile - interface to parse a Data Services adapter configuration file.

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
// ConfigurationFile - interface to parse a Data Services adapter configuration file.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-16      Scott Townsend, RTi     Create initial version of this
//                                              interface. This interface
//						will help developers build new
//						Data Services adapters as the data
//						become available.
//------------------------------------------------------------------------------------
// Endheader

package RTi.DataServices.Adapter;

import java.net.URL;
import java.util.Hashtable;

/**
 * Interface for all configuration files used by adapter code. It implements 
 * the RTi.Util.XML.ParseXML interface.
 * 
 */
public interface ConfigurationFile {

/**
 * <p>Method takes an URL to a XML schema file and compiles it for use in 
 * parsing the XML configuration file.</p>
 * @param URLToXSD URL to a local copy of the configuration file XML schema.
 */
public void compileXSD(URL URLToXSD) throws InvalidConfigurationException;

/**
 * <p>Method takes a local XML schema file and compiles it for use in parsing 
 * the XML configuration file.</p>
 * @param XSDFilePath File path to a local copy of the configuration file XML 
 * schema.
 */
public void compileXSD(String XSDFilePath) throws InvalidConfigurationException;

/**
 * <p>Returns a HashTable of key/value pairs of information in the configuration 
 * file.</p>
 * @return A HashTable of key/value pairs holding the configuration file information.
 */
public Hashtable<String,String> getConfiguration();

/**
 * <p>This method returns a version string from the XML schema file or null if 
 * no such string is available. This is used to determine whether or not to 
 * recompile the schema before use.</p>
 * @param XSDFilePath Path to the configuration file schema.
 * @return This return value is a string representing the version of the XML 
 * schema if valid otherwise it returns null.
 */
public String getXSDVersion(String XSDFilePath);

}
