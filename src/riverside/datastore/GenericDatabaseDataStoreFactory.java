// GenericDatabaseDataStoreFactory - datastore factory for GenericDatabaseDataStore

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

import RTi.DMI.DMI;
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
    String connectionProperties = IOUtil.expandPropertyForEnvironment("ConnectionProperties",props.getValue ( "ConnectionProperties" ));
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

    // Additionally, expand the password property since it might be specified in a file:
    // - for example, ${pgpass:password} is used with PostgreSQL
    // DO NOT print the password except when troubleshooting during development.
    //Message.printStatus(2, "", "systemPassword before expansion=" + systemPassword);
    systemPassword = DMI.expandDatastoreConfigurationProperty(props, "SystemPassword", systemPassword);
    //Message.printStatus(2, "", "systemPassword after expansion=" + systemPassword);

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
        // Always create the datastore, which generally involves simple assignment
        GenericDatabaseDataStore ds = new GenericDatabaseDataStore ( name, description, dmi );
        ds.setProperties(props);
        // Now try to open the database connection, which may generate an exception
        ds.setStatus(0);
        // Set additional connection properties if specified, for example the login timeout:
        // - the properties will depend on the database but usually is ?prop1=value1&prop2=value2
        dmi.setAdditionalConnectionProperties(connectionProperties);
        try {
        	dmi.open();
        }
        catch ( Exception e ) {
        	ds.setStatus(1);
        	ds.setStatusMessage("" + e);
        }
        return ds;
    }
    catch ( Exception e ) {
        // TODO SAM 2010-09-02 Wrap the exception because need to move from default Exception
        Message.printWarning(3,routine,e);
        throw new RuntimeException ( e );
    }
}

}