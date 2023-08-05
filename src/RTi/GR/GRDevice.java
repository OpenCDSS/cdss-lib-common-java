// GRDevice - GR interface for a drawable device

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.GR;

import java.awt.Graphics2D;

/**
GR interface for a drawable device.
*/
public abstract interface GRDevice
{

/**
Add a drawing area to the device.
The device will then manage the drawing areas as much as possible.
@param grda GRDrawingArea to add.
*/
public void addDrawingArea ( GRDrawingArea grda );

/**
Return the current limits of the device, in device units.
This should be defined in the derived class.
@return the current limits of the device.
*/
public GRLimits getLimits ();

/**
Return the Graphics2D instance to use for drawing.
This is typically a shared resource set when a GRCanvasDevice or GRJComponentDevice paint() method is called.
@return the Graphics2D instance to use for drawing.
*/
public Graphics2D getPaintGraphics();

/**
Indicate whether the device has a "reversed" Y axis (one that starts at the top and increments down).
@return true if the Y axis starts at the top (upper left) and goes down (lower left).
*/
public boolean getReverseY();

/**
Return the device units (GRUnits.*).
@return The device units.
*/
public int getUnits ( );

/**
Returns whether the graphics are currently being drawn antialiased.
@return true if antialiased, false if not.
*/
public boolean isAntiAliased();

/**
Indicate whether the device is in the process of printing.
@return true if the device is in the process of printing, false if not.
*/
public boolean isPrinting ();

/**
Complete the plot.
@param close_flag GRUtil.CLOSE_HARD to close the output completely (e.g., to close an output file).
*/
public void plotEnd ( int close_flag );

/**
Sets whether the graphics should be drawn anti aliased.
@param antiAlias if true, the graphics will be drawn anti aliased.
*/
public void setAntiAlias(boolean antiAlias);

/**
Set the Graphics2D instance used by the device for drawing.
This Graphics2D should be reset at each paint() call in code that implements a GRDevice because Graphics2D
resources are typically created and destroyed dynamically by the application.
The Graphics2D will be used by all GRDrawingArea associated with the device.
@param graphics instance to use for drawing to the device.
*/
public void setPaintGraphics ( Graphics2D graphics );

}