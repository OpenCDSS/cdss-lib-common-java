// AdapterLoadingException - exception that gets thrown when an adapter cannot load

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
// AdapterLoadingException - class to build an exception handler that gets thrown when 
// an adapter can not get loaded.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-16      Scott Townsend, RTi     Create initial version of this
//                                              Adapter exception class. This
//						class is necessary since it does
//						handle specific error messages
//						and specific Adapter clean-up.
//						This clean-up includes closing any
//						open connections to the adapter's
//						Server.
//------------------------------------------------------------------------------------
// Endheader

package RTi.DataServices.Adapter;

import java.io.IOException;

import RTi.DataServices.Adapter.Adapter;

/**
 * An exception which occurs when an adapter fails to load for any reason.
 */
@SuppressWarnings("serial")
public class AdapterLoadingException extends IOException {

/**
 * private Adapter instance
*/
private Adapter __IOAdapter = null;

/**
 * <p>Default constructor to build this exception.</p>
 */
public  AdapterLoadingException() {
	super("An Error occured: Data Services Adapter could not load");

	if(__IOAdapter != null) {
		//__IOAdapter.finalize();
	}
} 

/**
 * <p>Constructor to build this exception with a developer defined error message.</p>
 * @param exceptionMessage a String holding the developer defined error message.
 */
public  AdapterLoadingException(String exceptionMessage) {
	super(exceptionMessage);
	if(__IOAdapter != null) {
		//__IOAdapter.finalize();
	}
}

/**
 * <p>Constructor to build this exception with the Adapter specified.</p>
 * @param DSAdapter a Adapter object that will be finalized when
 * this constructor is called.
 */
public  AdapterLoadingException(Adapter DSAdapter) {
	super("An Error occured: Data Services Adapter could not load");
	
	// Set the adapter
	__IOAdapter = DSAdapter;
	
	if(__IOAdapter != null) {
		//__IOAdapter.finalize();
	}
} 

/**
 * <p>Constructor to build this exception with a developer defined error message
 * and an Adapter.</p>
 * @param exceptionMessage a String holding the developer defined error message.
 * @param DSAdapter a Adapter object that will be finalized when
 * this constructor is called.
 */
public  AdapterLoadingException(Adapter DSAdapter, String exceptionMessage) {
	super(exceptionMessage);
	
	// Set the adapter
	__IOAdapter = DSAdapter;
	
	if(__IOAdapter != null) {
		//__IOAdapter.finalize();
	}
}

/**
 * A get accessor method to get the Adapter specified by the Exception.
 @return The Adapter utilized in theis Exception.
*/
public Adapter getAdapter() {
	return __IOAdapter;
}
} // End class AdapterLoadingException
