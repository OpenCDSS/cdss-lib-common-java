// ----------------------------------------------------------------------------
// GRTest_DrawingArea - the drawing area for GRTest.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2004-06-03	J. Thomas Sapienza, RTi	Initial changelog.  
// ----------------------------------------------------------------------------

package RTi.GR;

import RTi.GR.GRAspect;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRUnits;

/**
Class that is used as the drawing area for an ER Diagram.
*/
public class GRTest_DrawingArea 
extends GRJComponentDrawingArea {

/**
Constructor.
@param dev the GRTest_Device to use this drawing area with.
@param drawingLimits the drawingLimits of the drawing area.
*/
public GRTest_DrawingArea(GRTest_Device dev, GRLimits drawingLimits,
GRLimits dataLimits) {
	super(dev, "GRTest_DrawingArea", GRAspect.FILL, drawingLimits,
		GRUnits.DEVICE, GRLimits.DEVICE, dataLimits);
}

}
