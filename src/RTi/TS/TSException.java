// TSException - Exception class for handling TS-related exceptions

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
@SuppressWarnings("serial")
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
