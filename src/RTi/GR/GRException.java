//------------------------------------------------------------------------------
// GRException - Exception class for handling GR-related exceptions
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 11 Sep 1998	Steven A. Malers, RTi	Copy TSException and modify.
//------------------------------------------------------------------------------

package RTi.GR;

import java.lang.Exception;

/**
This class provides an exception class for the graphics package.
*/
@SuppressWarnings("serial")
public class GRException 
extends Exception
{

/**
Construct a GRException with a string message.
@param s String message.
*/
public GRException( String s )
{
	super( s );
}

} // End of GRException class
