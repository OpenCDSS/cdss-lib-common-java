// CommandProfile - this class provides profile information related to command execution.

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

package RTi.Util.IO;

/**
This class provides profile information related to command execution.  This information can be used
to evaluate command processor performance and memory use.
*/
public class CommandProfile implements Cloneable
{
	
/**
Command start time in milliseconds (from Date).
*/
private long startTime = 0;

/**
Command end time in milliseconds (from Date).
*/
private long endTime = 0;

/**
Command start heap memory in bytes (from Date).
*/
private long startHeap = 0;

/**
Command end heap memory in bytes (from Date).
*/
private long endHeap = 0;

/**
Construct and initialize all profile values to zero.
*/
public CommandProfile ()
{
}

/**
Constructor.
@param startTime starting milliseconds for command (specify 0 if unknown)
@param startHeap starting heap memory for command (specify 0 if unknown)
@param endTime ending milliseconds for command (specify 0 if unknown)
@param endHeap ending heap memory for command (specify 0 if unknown)
*/
public CommandProfile ( long startTime, long startHeap, long endTime, long endHeap )
{
	this.startTime = startTime;
	this.startHeap = startHeap;
	this.endTime = endTime;
    this.endHeap = endHeap;
}

/**
Clone the instance.  All command data are cloned.
*/
public Object clone ()
{	try {
        CommandProfile profile = (CommandProfile)super.clone();
		return profile;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Return the heap memory at the end of the command execution, in bytes.
@return the heap memory at the end of the command execution, in bytes
*/
public long getEndHeap ()
{
    return this.endHeap;
}

/**
Return the time in milliseconds (from 1970) at the end of the command execution.
@return the time in milliseconds (from 1970 ) at the end of the command execution
*/
public long getEndTime ()
{
    return this.endTime;
}

/**
Return the run time in milliseconds (from 1970), computed as the end time minus the start time, or zero if
the command has no end time.
@return the run time in milliseconds (from 1970 )
*/
public long getRunTime ()
{
    if ( this.endTime == 0 ) {
        return 0;
    }
    else {
        return (this.endTime - this.startTime);
    }
}

/**
Return the heap memory at the start of the command execution, in bytes.
@return the heap memory at the start of the command execution, in bytes
*/
public long getStartHeap ()
{
    return this.startHeap;
}

/**
Return the time in milliseconds (from 1970) at the start of the command execution.
@return the time in milliseconds (from 1970 ) at the start of the command execution
*/
public long getStartTime ()
{
    return this.startTime;
}

/**
Set the heap memory in bytes at the end of the command execution.
@param endheap the heap memory in bytes at the end of the command execution
*/
public void setEndHeap ( long endHeap )
{
    this.endHeap = endHeap;
}

/**
Set the time in milliseconds (from 1970) at the end of the command execution.
@param endTime the time in milliseconds (from 1970 ) at the end of the command execution
*/
public void setEndTime ( long endTime )
{
    this.endTime = endTime;
}

/**
Set the heap memory in bytes at the end of the command execution.
@param endheap the heap memory in bytes at the end of the command execution
*/
public void setStartHeap ( long startHeap )
{
    this.startHeap = startHeap;
}

/**
Set the time in milliseconds (from 1970) at the start of the command execution.
@param startTime the time in milliseconds (from 1970 ) at the start of the command execution
*/
public void setStartTime ( long startTime )
{
    this.startTime = startTime;
}

/**
Return a string representation of the problem, suitable for display in logging, etc.
*/
public String toString ()
{	return
	"Runtime " + this.startTime + "/" + this.endTime + "/" + (this.endTime - this.startTime) +
	" Heap " + this.startHeap + "/" + this.endHeap + "/" + (this.endHeap - this.startHeap);
}

}
