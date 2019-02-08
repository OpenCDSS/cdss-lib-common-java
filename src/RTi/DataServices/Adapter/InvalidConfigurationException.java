// InvalidConfigurationException - exception thrown when an error parsing an XML configuration file

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
// InvaildConfigurationException - class to build an exception handler that gets 
// thrown when parsing an XML configuration file encounters an invalid configuration.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-16      Scott Townsend, RTi     Create initial version of this
//                                              Adapter exception class. This
//						class is necessary since it does
//						handle specific error messages
//						and specific config file clean-up.
//						This clean-up includes closing any
//						open connections to the config
//						file or server which holds the file.
//------------------------------------------------------------------------------------
// Endheader

package RTi.DataServices.Adapter;

import java.lang.Exception;

import RTi.DataServices.Adapter.ConfigurationFile;

/**
 * Exception which occurs when the ConfigurationFile object can not parse the 
 * given XML configuration file.
 * 
 */
@SuppressWarnings("serial")
public class InvalidConfigurationException extends Exception {

/**
 * private Adapter instance
*/
private ConfigurationFile __IOConfiguration = null;

/**
 * <p>Default constructor to build this exception.</p>
 */
public  InvalidConfigurationException() {
	super("An Error occured: Parse error happened when "+
		"parsing the Data Services configuration file");
	if(__IOConfiguration != null) {
		//__IOConfiguration.finalize();
	}
} 

/**
 * <p>Constructor to build this exception with a developer defined error message.</p>
 * @param exceptionMessage a String holding the developer defined error message.
 */
public  InvalidConfigurationException(String exceptionMessage) {
	super(exceptionMessage);
	if(__IOConfiguration != null) {
		//__IOConfiguration.finalize();
	}
}

/**
 * <p>Constructor to build this exception with the ConfigurationFile specified.</p>
 * @param DSConfiguration a ConfigurationFile object that will be finalized when
 * this constructor is called.
 */
public  InvalidConfigurationException(ConfigurationFile DSConfiguration) {
	super("An Error occured: Parse error happened when "+
		"parsing the Data Services configuration file");
	
	// Set the ConfigurationFile
	__IOConfiguration = DSConfiguration;
	
	if(__IOConfiguration != null) {
		//__IOConfiguration.finalize();
	}
} 

/**
 * <p>Constructor to build this exception with a developer defined error message
 * and the ConfigurationFile.</p>
 * @param exceptionMessage a String holding the developer defined error message.
 * @param DSConfiguration a ConfigurationFile object that will be finalized when
 * this constructor is called.
 */
public  InvalidConfigurationException(ConfigurationFile DSConfiguration, 
		String exceptionMessage) {
	super(exceptionMessage);
	
	// Set the ConfigurationFile
	__IOConfiguration = DSConfiguration;
	
	if(__IOConfiguration != null) {
		//__IOConfiguration.finalize();
	}
}

/**
 * A get accessor method to get the ConfigurationFile specified by the Exception.
 @return The ConfigurationFile utilized in theis Exception.
*/
public ConfigurationFile getConfigurationFile() {
	return __IOConfiguration;
}
}
