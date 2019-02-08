// Timable - Interface that allows a class to be passed into a TimingThread

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
// Timable - Interface that allows a class to be passed into a TimingThread.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// Notes:
//
//------------------------------------------------------------------------------
// History:
// 
// 01 Dec 1997	Matthew J. Rutherford, RTi	Created initial function.
// 18 Mar 1998	MJR	Added documentation.
//------------------------------------------------------------------------------
// Variables:	I/O	Description		
//
//
//------------------------------------------------------------------------------
package RTi.Util.Time;

/**
This interface is used in conjunction with the <B>TimingThread</B> class to
perform a particular action on a regular time step. A class that implements
this interface can be passed into the <B>TimingThread</B> and have the
its performTimedAction() method executed at any regular interval. As with all
interfaces, any class that implements <I>Timable</I> must provide an 
implemention of the performTimedAction() method.
<P>
@see TimingThread
*/

public interface Timable
{
/**
Method that must be implemented by classes that use this interface.
*/
public int performTimedAction();
}
