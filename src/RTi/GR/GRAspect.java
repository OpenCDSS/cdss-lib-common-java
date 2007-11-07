// ---------------------------------------------------------------------------
// GRAspect - holds data fields of information on Aspects.
// ---------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ---------------------------------------------------------------------------
//
// History:
// ????-??-??	Steven A. Malers, RTi	Initial version.
// ---------------------------------------------------------------------------

package RTi.GR;

/**
This class defines aspect types for GRDrawingArea axes.
@see GRDrawingArea
*/
public class GRAspect
{

/**
True aspect on both axes.
*/
public static final int TRUE = 1;
/**
Fill both axes.
*/
public static final int FILL = 2;
/**
True aspect on the Y-axis and filled on the X-axis.
*/
public static final int FILLX = 3;
/**
True aspect on the X-axis and filled on the Y-axis.
*/
public static final int FILLY = 4;

} // End class GRAspect
