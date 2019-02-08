// InvalidCommandParameterException - an Exception to be thrown when a command parameter is invalid

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
// InvalidCommandParameterException - an Exception to be thrown when a command
//					parameter is invalid
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-04-29	Steven A. Malers, RTi	Initial version.
// 2005-05-19	SAM, RTi		Move from TSTool package.
//------------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

/**
The InvalidCommandParameterException should be thrown when a Command parameter
is invalid (e.g., during checks in an editor dialog or during initialization
for processing).
*/
@SuppressWarnings("serial")
public class InvalidCommandParameterException extends Exception
{

public InvalidCommandParameterException ( String message )
{	super ( message );
}

}
