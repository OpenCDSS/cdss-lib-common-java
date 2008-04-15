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
public Hashtable getConfiguration();

/**
 * <p>This method returns a version string from the XML schema file or null if 
 * no such string is available. This is used to determine whether or not to 
 * recompile the schema before use.</p>
 * @param XSDFilePath Path to the configuration file schema.
 * @return This return value is a string representing the version of the XML 
 * schema if valid otherwise it returns null.
 */
public String getXSDVersion(String XSDFilePath);

} // End of interface ConfigurationFile

