//------------------------------------------------------------------------------
// Function - interface to define a function
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2004-06-02	Steven A. Malers, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------

package RTi.Util.Math;

/**
This interface defines the methods necessary to define a function, for use
in generic code that needs to evaluate the function.
*/
public abstract interface Function
{

public abstract double evaluate ( double [] parameters ) throws Exception;

} // End Function
