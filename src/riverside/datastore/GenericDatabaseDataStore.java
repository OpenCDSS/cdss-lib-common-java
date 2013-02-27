package riverside.datastore;

import java.io.IOException;

import RTi.DMI.AbstractDatabaseDataStore;
import RTi.DMI.DMI;
import RTi.DMI.GenericDMI;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;

/**
Data store for Generic database, to allow table/view queries.
This class maintains the database connection information in a general way.
@author sam
*/
public class GenericDatabaseDataStore extends AbstractDatabaseDataStore
{
    
/**
Construct a data store given a DMI instance, which is assumed to be open.
@param name identifier for the data store
@param description name for the data store
@param dmi DMI instance to use for the data store.
*/
public GenericDatabaseDataStore ( String name, String description, DMI dmi )
{
    setName ( name );
    setDescription ( description );
    setDMI ( dmi );
    // Rely on other authentication to prevent writing.
    // TODO SAM 2013-02-26 Perhaps use a database configuration file property to control
    dmi.setEditable ( true );
}
    
/**
Factory method to construct a data store connection from a properties file.
@param filename name of file containing property strings
*/
public static GenericDatabaseDataStore createFromFile ( String filename )
throws IOException, Exception
{
    // Read the properties from the file
    PropList props = new PropList ("");
    props.setPersistentName ( filename );
    props.readPersistent ( false );
    String name = IOUtil.expandPropertyForEnvironment("Name",props.getValue("Name"));
    String description = IOUtil.expandPropertyForEnvironment("Description",props.getValue("Description"));
    String databaseEngine = IOUtil.expandPropertyForEnvironment("DatabaseEngine",props.getValue("DatabaseEngine"));
    String databaseServer = IOUtil.expandPropertyForEnvironment("DatabaseServer",props.getValue("DatabaseServer"));
    String databaseName = IOUtil.expandPropertyForEnvironment("DatabaseName",props.getValue("DatabaseName"));
    String odbcName = props.getValue ( "OdbcName" );
    String systemLogin = IOUtil.expandPropertyForEnvironment("SystemLogin",props.getValue("SystemLogin"));
    String systemPassword = IOUtil.expandPropertyForEnvironment("SystemPassword",props.getValue("SystemPassword"));
    
    // Get the properties and create an instance
    GenericDMI dmi = null;
    if ( (odbcName != null) && !odbcName.equals("") ) {
        // An ODBC connection is configured so use it
        dmi = new GenericDMI (
            databaseEngine, // Needed for internal SQL handling
            odbcName, // Must be configured on the machine
            systemLogin, // OK if null - use read-only guest
            systemPassword ); // OK if null - use read-only guest
    }
    else {
        // Use the parts and create the connection string on the fly
        dmi = new GenericDMI( databaseEngine, databaseServer, databaseName, -1, systemLogin, systemPassword );
    }
    dmi.open();
    GenericDatabaseDataStore ds = new GenericDatabaseDataStore( name, description, dmi );
    return ds;
}

}