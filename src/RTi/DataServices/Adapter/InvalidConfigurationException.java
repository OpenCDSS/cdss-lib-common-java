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
