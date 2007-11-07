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
