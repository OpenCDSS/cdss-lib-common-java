// AbstractWebServiceDataStore - abstract base class for web service datastores

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

import java.net.URI;

/**
Abstract implementation of WebServiceDataStore, to handle management of common configuration data.
*/
abstract public class AbstractWebServiceDataStore extends AbstractDataStore implements WebServiceDataStore
{

/**
The root web service URI, to which more specific resource addresses will be appended
(e.g., "https://data.rcc-acis.org" or "https://some.server/api/vi/").
The trailing / is optional and should be handled in any case by datastore code.
*/
private URI serviceRootURI = null;

/**
Return the service root URI for the data store.
Calling code should handle whether the returned value ends with / or not.
@return the service root URI for the data store.
*/
public URI getServiceRootURI() {
    return this.serviceRootURI;
}

/**
Set the service root URI for the data store.
@param serviceRootURI the service root URI for the data store.
*/
public void setServiceRootURI ( URI serviceRootURI ) {
    this.serviceRootURI = serviceRootURI;
}

}