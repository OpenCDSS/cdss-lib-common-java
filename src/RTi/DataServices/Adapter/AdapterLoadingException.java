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
