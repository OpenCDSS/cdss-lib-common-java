//------------------------------------------------------------------------------
// UnrecognizedRequestException - an Exception to be thrown when a
//		command processor processRequest() is called with an unrecognized
//		request
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2007-02-11	Steven A. Malers, RTi	Initial version.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

/**
The UnrecognizedRequestException should be thrown when running 
CommandProcessor.processRequest() and the request to be performed is not recognized.
*/
public class UnrecognizedRequestException extends Exception
{

public UnrecognizedRequestException ( String message )
{	super ( message );
}

}