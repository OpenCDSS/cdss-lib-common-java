// ----------------------------------------------------------------------------
// ERDiagram_DrawingArea - the drawing area onto which the ER Diagram is
//	drawn.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-27	J. Thomas Sapienza, RTi	Initial changelog.  
// ----------------------------------------------------------------------------

package RTi.DMI;

import RTi.GR.GRAspect;
import RTi.GR.GRJComponentDrawingArea;
import RTi.GR.GRLimits;
import RTi.GR.GRUnits;

/**
Class that is used as the drawing area for an ER Diagram.
*/
public class ERDiagram_DrawingArea extends GRJComponentDrawingArea {

/**
Constructor.
@param dev the ERDiagram_Device to use this drawing area with.
@param drawingLimits the drawingLimits of the drawing area.
*/
public ERDiagram_DrawingArea(ERDiagram_Device dev, GRLimits drawingLimits) {
	super(dev, "ERDiagram_DrawingArea", GRAspect.TRUE, drawingLimits,
		GRUnits.DEVICE, GRLimits.DEVICE, drawingLimits);
}

}
