//------------------------------------------------------------------------------
// RequestParameterInvalidException - an Exception to be thrown when a
//		command processor processRequest() required input parameter has invalid
//		data
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
The RequestParameterNotFoundException should be thrown when running 
CommandProcessor.processRequest() and a needed parameter is not
provided.
*/
public class RequestParameterInvalidException extends Exception
{

public RequestParameterInvalidException ( String message )
{	super ( message );
}

}
