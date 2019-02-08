// InvalidCommandSyntaxException - an Exception to be thrown when command syntax is invalid

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
// InvalidCommandSyntaxException - an Exception to be thrown when command
//					syntax is invalid
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
The InvalidCommandSyntaxException should be thrown when a Command's syntax
is invalid (e.g., mismatched parentheses or quotes).
*/
@SuppressWarnings("serial")
public class InvalidCommandSyntaxException extends Exception
{

public InvalidCommandSyntaxException ( String message )
{	super ( message );
}

}
