// TSCursorDecorator -provides a decorator to draw a cross-hair cursor on a JComponent

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

package RTi.GRTS;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

import RTi.GR.GRColor;
import RTi.GR.GRLimits;

/**
 * Provides a decorator to draw a cross-hair cursor on a JComponent.
 * This was used with legacy time series editing.
 * A newer mouse tracker has been implemented.
 * 
 * Usage:
 * <pre>
 * _cursorDecorator = new TSCursorDecorator(this, crosshairColor, _background_color);
 *                                          
 * public void mouseMoved ( MouseEvent event ) {
 *     _cursorDecorator.mouseMoved(event,tsgraph.getGraphDrawingArea().getPlotLimits( GRDrawingArea.COORD_DEVICE) );
 *<pre>
 */
public class TSCursorDecorator {
  /**
   * Canvas component, for example TSGraphJComponent.
   */
  private  JComponent _jComponent;

  /**
   * Flag indicating whether decorator drawn.
   */
  private boolean _eraseNeeded = false;

  /**
   * Decorator color.
   */
  private GRColor _cursorColor;

  /**
   * XOR color.
   */
  private GRColor _xorColor;

  /**
   * Current mouse location.
   */
  private Point _currentMousePoint;
 
  /**
   * Limits of drawing area.
   */
  private GRLimits _daLimits;

  /**
   * Draws a cross-hair cursor on the specified JComponent.
   * <p>
   * The cross-hairs extend to the drawing area limits passed in
   * {@link #mouseMoved(MouseEvent, GRLimits)}
   * 
   * @param jComponent Component on which mouse is tracked
   * @param cursorColor Cursor color
   * @param xorColor   Color for XOR mode
   */
  public TSCursorDecorator(JComponent jComponent, GRColor cursorColor, GRColor xorColor) {
    _jComponent = jComponent;
    _cursorColor = cursorColor;
    _xorColor = xorColor;

    // Set up mouse motion listener for mouse movement
    // Can't use MouseMotionListener because not all regions
    // of JGraphJComponent should have a cross-hair.
    // _jComponent.addMouseMotionListener(new MouseMotionHandler());     
  }

  /**
   * Draws cross-hair cursor.
   * <p>
   * Called both to erase the previous cross-hair & to draw the new cross-hair.
   */
  protected void draw() {
    Graphics g = _jComponent.getGraphics();
    g.setColor ( _cursorColor);

    if ( g != null ) {
        try {
          // Use XORMode so that we don't have to call redraw() to remove the decorator,
        	// rather we just draw again.
          g.setXORMode(_xorColor);
          drawDecorator(g);

          _eraseNeeded = true;
        }
        finally {
          g.dispose();
        }
      }
  }

  /**
   * Draws cross-hair.
   * <p>
   * The cross-hair is drawn to the edges of the drawing area
   * @param g
   */
  protected void drawDecorator(Graphics g) {
    g.drawLine((int)_daLimits.getLeftX(), _currentMousePoint.y, (int) _daLimits.getRightX(), _currentMousePoint.y);
    g.drawLine(_currentMousePoint.x, (int)_daLimits.getTopY(), _currentMousePoint.x,(int) _daLimits.getBottomY());
  }

  /** 
   * Returns whether mouse is inside the drawing area.
   * @return
   */
  private final boolean isInside() {
    return (_currentMousePoint.x > (int)_daLimits.getLeftX()
        && _currentMousePoint.x < (int)_daLimits.getRightX()
        && _currentMousePoint.y > (int)_daLimits.getTopY()
        && _currentMousePoint.y < (int)_daLimits.getBottomY())
      ?true:false;
  }

  /**
   * Erases decorator by redrawing it in XOR mode.
   */
  private final void erase() {
    if (_eraseNeeded) {
        draw();
        _eraseNeeded = false;
      }
  }
  /**
   * Erase previous cross-hair & draw at new position.
   * 
   * @param e
   */
  public final void mouseMoved(MouseEvent e, GRLimits daLimits ) {
    _daLimits = daLimits;
    // Erase old decorator before setting new mouse position.
     erase();            
    _currentMousePoint = e.getPoint();

    //if (_daLimits.contains(_currentMousePoint.x,_currentMousePoint.y))
    if (isInside()) {
        draw();
      }
    //    System.out.println("-->Outside");
    //erase();
  }

  /**
   * Monitors the mouse for movement, initiates drawing/erasing of decorator.
   */
  /* FIXME SAM 2008-04-15 Evaluate whether using.
  private class MouseMotionHandler extends MouseMotionAdapter {
    public void mouseMoved(MouseEvent e) {
      // Erase old decorator before setting new mouse position
      erase();            
      _currentMousePoint = e.getPoint();

      draw();
    }
  }
  */

}