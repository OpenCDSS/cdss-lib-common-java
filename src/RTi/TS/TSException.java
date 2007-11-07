//------------------------------------------------------------------------------
// TSException - Exception class for handling TS-related exceptions
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 03 Nov 1997	Daniel Weiler, RTi	Created initial version
// 19 Mar 1998	Steven A. Malers, RTi	Add javadoc.
// 13 Apr 1999	SAM, RTi		Add finalize.
//------------------------------------------------------------------------------

package RTi.TS;

import java.lang.Exception;

/**
This class provides an exception class for the time series package.
*/
public class TSException extends Exception
{

/**
Construct a TSException with a string message.
@param s String message.
*/
public TSException( String s )
{	super ( s );
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	super.finalize();
}

} // End of TSException class
