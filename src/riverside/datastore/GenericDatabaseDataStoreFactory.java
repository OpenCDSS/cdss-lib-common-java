package riverside.datastore;

import RTi.DMI.GenericDMI;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreFactory;

/**
Factory to instantiate ODBCDataStore instances.
@author sam
*/
public class GenericDatabaseDataStoreFactory implements DataStoreFactory
{

/**
Create an ODBCDataStore instance and open the encapsulated DMI using the specified properties.
*/
public DataStore create ( PropList props )
{   String routine = getClass().getName() + ".create";
    String name = props.getValue ( "Name" );
    String description = props.getValue ( "Description" );
    if ( description == null ) {
        description = "";
    }
    String databaseEngine = IOUtil.expandPropertyForEnvironment("DatabaseEngine",props.getValue ( "DatabaseEngine" ));
    String databaseServer = IOUtil.expandPropertyForEnvironment("DatabaseServer",props.getValue ( "DatabaseServer" ));
    String databasePort = IOUtil.expandPropertyForEnvironment("DatabasePort",props.getValue ( "DatabasePort" ));
    int port = -1;
    if ( (databasePort != null) && !databasePort.equals("") ) {
        try {
            port = Integer.parseInt(databasePort);
        }
        catch ( NumberFormatException e ) {
            port = -1;
        }
    }
    String databaseName = IOUtil.expandPropertyForEnvironment("DatabaseName",props.getValue ( "DatabaseName" ));
    String odbcName = IOUtil.expandPropertyForEnvironment("OdbcName",props.getValue ( "OdbcName" ));
    String systemLogin = IOUtil.expandPropertyForEnvironment("SystemLogin",props.getValue ( "SystemLogin" ));
    String systemPassword = IOUtil.expandPropertyForEnvironment("SystemPassword",props.getValue ( "SystemPassword" ));
    try {
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
            // Use the parts to create the connection
            dmi = new GenericDMI( databaseEngine, databaseServer, databaseName, port, systemLogin, systemPassword );
        }
        dmi.open();
        GenericDatabaseDataStore ds = new GenericDatabaseDataStore ( name, description, dmi );
        ds.setProperties(props);
        return ds;
    }
    catch ( Exception e ) {
        // TODO SAM 2010-09-02 Wrap the exception because need to move from default Exception
        Message.printWarning(3,routine,e);
        throw new RuntimeException ( e );
    }
}

}