// UnrecognizedRequestException - an Exception to be thrown when a command processor processRequest()
// is called with an unrecognized request

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
@SuppressWarnings("serial")
public class UnrecognizedRequestException extends Exception
{

public UnrecognizedRequestException ( String message )
{	super ( message );
}

}
