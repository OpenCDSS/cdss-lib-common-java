// OverviewGutter - provides an overview of Problems associated with the

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2024 Colorado Department of Natural Resources

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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Provides an overview of Problems associated with the MessageModel.
 * <p>
 * Problem markers are drawn relative to the position where they occur in the model.
 * </p>
 * <p>
 * When a gutter marker is clicked, the associated index in the list will be scrolled to and selected.
 * </p>
 */
@SuppressWarnings("serial")
public class OverviewGutter extends JComponent {

  /**
   * Height of marker.
   */
  private final static int MARKER_HEIGHT = 4;

  /**
   * Margin between Component edge and marker.
   */
  private final static int LEFT_MARGIN = 2;

  /**
   * Component's preferred width.
    */
  private final static int PREFERRED_WIDTH = 12;

  /**
   * Data model associated with OverviewGutter.
    */
  private final ListModel _dataModel;

  /**
   * Height of gutter used for marker positioning (it is MARKER_HEIGHT shorter.
   */
  int _gutterHeight;

  /**
   * Maintains the list of markers.
    */
  private List<MarkerInstance> _markerInstances = new ArrayList<>();

  /**
   * JList associated with OverviewGutter.
    */
  private final JList _list;
  
  /**
   * Light blue color using RGB, so don't have to recreate the Color instance every time used:
   * - use Eclipse blue from TODO and FIXME comments
   */
  private final Color lightBlue = new Color(49, 152, 253);

  /**
   * Creates an overview gutter for the specified data model and JList.
   * <p>
   * Problems associated with data model items are indicated visually by colored markers drawn in the gutter.
   * </p>
   * @param dataModel Associated data model, for example a TSCommandProcessorListModel instance,
   * which extends AbstractListModel
   * @param list Associated JList
   */
  public OverviewGutter ( ListModel dataModel, JList list ) {
      this._dataModel = dataModel;
      this._list = list;

      setName("overviewGutter");

      // Detect mouse clicks on markers and ensure associated JList item is visible:
      // - 'setSelectedIndex' will scroll the list
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            int index = findMarkerInstance(mouseEvent.getPoint());
            if ( index >= 0 ) {
                _list.ensureIndexIsVisible(index);
                _list.setSelectedIndex(index);
            }
        }

        public void mouseExited(MouseEvent e) {
          setToolTipText(null);
        }
      });

      // Display marker text when pointer is over a marker.
      addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
          String markerText = null;

          int index = findMarkerInstance(e.getPoint());

          if ( (index > -1) && (index < _dataModel.getSize()) ) {
              CommandStatusProvider csp = (CommandStatusProvider)_dataModel.getElementAt(index);
              int highestSeverity = CommandStatusProviderUtil.getHighestSeverity(csp);
              if (highestSeverity !=CommandStatusType.UNKNOWN.getSeverity()) {

                  // Change pointer to indicate on marker.
                  ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  markerText = getMarkerText(index);
             }
          }
          else {
              ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
          ((JComponent)e.getComponent()).setToolTipText(markerText);
        }
      }
      );

      // Really only the width matters.
      setPreferredSize(new Dimension(PREFERRED_WIDTH, 16));
      setToolTipText(null);
    }

  /**
   * Find the marker for mouse clicked point
   * @param point point that was clicked in the gutter
   * @return
   */
  private int findMarkerInstance ( Point point ) {
    int index = -1;

    if ( this._markerInstances.size() > 0 ) {
        // Determine markerInstance containing point.
        for ( MarkerInstance markerInstance:  this._markerInstances ) {
          if ( markerInstance.getRect().contains(point) ) {
              index = markerInstance.getIndex();
          }
        }
    }
    return index;
  }

  /**
   * Returns the gutter pixel position (0+) corresponding to a JList index.
   * This may be an estimate if the list is long compared to the height of the gutter,
   * resulting in round off in integer math.
   *
   * @param i JList data model index
   * @return Pixel position in gutter corresponding to JList data model position,
   * assuming that JList index is centered on corresponding gutter position.
   */
  private int findGutterPosition(int index) {
      if ( (this._gutterHeight <= 0) || (this._dataModel.getSize() == 0) ) {
    	  return -1;
      }
      // Order is important to prevent integer math roundoff to zero.
      int y = (this._gutterHeight*index)/this._dataModel.getSize();
      //Message.printStatus ( 1,"findGutterPosition","index=" + index + " height="+ getHeight() + " size="+_dataModel.getSize() + " y="+y);
      return y;
    }

  /**
   * Paints the component on the screen.
   * <p>
   * Markers are drawn in the overview gutter relative to their position in the list.
   * A marker for the first item should be at the top,
   * while a marker for the last item of the list appears at the bottom of the gutter.
   * </p>
   * <p>
   * Markers are drawn from y downward to y + MARKER_HEIGHT, therefore for positioning the height.
   * </p>
   */
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      this._markerInstances.clear();

      if ( this._list.getModel().getSize() == 0 ) {
    	  return;
      }

      this._gutterHeight = getHeight() - MARKER_HEIGHT;

      // Paint background to show gutter area.
      // g.setColor(Color.gray);
      // g.fillRect(0,0,getWidth(),getHeight());

      // Find the markers to draw.
      findMarkers();

      // Draw markers.
      drawMarkers(g);
    }

  private void drawMarkers(Graphics g) {
    Color color;
    // Loop through markers and draw.

    if ( this._markerInstances.size() > 0 ) {
        for ( MarkerInstance markerInstance : this._markerInstances ) {
            int severity = markerInstance.getSeverity();
            if ( severity == CommandStatusType.WARNING.getSeverity() ) {
            	color = Color.yellow;
            }
            else if ( severity == CommandStatusType.FAILURE.getSeverity() ) {
            	color = Color.red;
            }
            else if ( markerInstance._hasNotification ) {
            	// Notification marker:
            	// - light blue
            	// - put after more serious status
            	color = this.lightBlue;
            }
            else {
                color = null;
            }
            Rectangle rect = markerInstance.getRect();

            // Paint a filled colored rectangle with black outline.
            // Center vertically on the y coordinate.
            g.setColor(color);
            g.fillRect(rect.x, rect.y, rect.width, MARKER_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(rect.x, rect.y, rect.width, MARKER_HEIGHT);
          }
      }
  }

  /**
   * Create markers for each data model item.
   */
  private void findMarkers() {
    int markerWidth = getWidth()- 2*LEFT_MARGIN;
    // Loop thru data model items detecting problems, creating a new MarkerInstance for each problem.
    for ( int i = 0; i < this._dataModel.getSize(); i++ ) {
        // Get the severity associated with an item in the list model.
        int severity = getMarkerHighestSeverity ( i );  // CommandStatus highest severity.
        boolean hasNotification = getMarkerHasNotification ( i ); // Whether CommandStatus has a notification.

        if ( (severity == CommandStatusType.WARNING.getSeverity()) ||
        	(severity == CommandStatusType.FAILURE.getSeverity()) || hasNotification ) {
        	// Find position in gutter corresponding to item.
            int y = findGutterPosition ( i );
            Rectangle rect = new Rectangle(LEFT_MARGIN, y, markerWidth, MARKER_HEIGHT);
            MarkerInstance markerInstance= new MarkerInstance ( i, severity, hasNotification, rect );
            this._markerInstances.add(markerInstance);
        }
     }
  }

 /**
  * Returns text to be displayed in tool tip.
  * <p>
  * Called by ToolTipMgr to get tool tip text.
  *
  * @param event Mouse event causing invocation
  * @return Text to be displayed in tooltip or null indicating no tooltip
  */
  public String getToolTipText(MouseEvent event) {
    String str = null;
    if ( this._dataModel.getSize() != 0 ) {

    //    int index = findListIndex(event.getPoint());
        int index = findMarkerInstance(event.getPoint());
        if (index > -1) {
            str = getMarkerText(index);
//            try
//              {
//                CommandStatusProvider csp = (CommandStatusProvider) _dataModel
//                        .getElementAt(index);
//                str = CommandStatusProviderUtil.getCommandLogHTML(csp);
//              }
//            catch (ClassCastException e)
//              {
//                Message.printWarning ( 2, "",
//                        "Item #:"
//                                + index
//                                + " does not implement ComandStatusProvider interface"
//                                + "\n  item.toString(): "
//                                + _dataModel.getElementAt(index)
//                                        .toString() + "\n\n" + e);
//                str = new String("NotACommandStatusProvider");
//              }
          }
      }

    return str;
  }

  /**
   * Returns whether the marker has a notification.
   * @param index JList model item index (0+)
   * @return true if the marker has a notification
   */
  private boolean getMarkerHasNotification ( int index ) {
	  boolean hasNotification = false;
      Object o = this._dataModel.getElementAt ( index );

      if ( o instanceof CommandStatusProvider ) {
    	  CommandStatusProvider csp = (CommandStatusProvider)o;
          hasNotification = CommandStatusProviderUtil.getHasNotification ( csp );
      }
      return hasNotification;
  }

  /**
   * Returns the highest severity for the marker.
   * The highest severity for the item (command) is used to determine which marker to use.
   * @param index JList model item index (0+)
   * @return severity index
   */
  private int getMarkerHighestSeverity ( int index ) {
      int markerHighestSeverity = 0;

      Object o = this._dataModel.getElementAt ( index );

      if ( o instanceof CommandStatusProvider ) {
    	  CommandStatusProvider csp = (CommandStatusProvider)o;
          markerHighestSeverity = CommandStatusProviderUtil.getHighestSeverity ( csp );
      }
      return markerHighestSeverity;
  }

  /**
   * Returns the text (as HTML) associated with the specified data model index.
   *
   * @param index Index in data model
   * @return the marker text
   */
  private String getMarkerText(int index) {
    CommandStatusProvider csp;
    String markerText = "";
    Object o = this._dataModel.getElementAt(index);

    if ( o instanceof CommandStatusProvider ) {
        csp = (CommandStatusProvider)o;
        markerText = CommandStatusUtil.getCommandLogHTML(csp);
    }
    return markerText;
  }

  /**
   * Maintains the attributes of a marker.
   */
  private class MarkerInstance {
    /**
     * Marker severity.
     */
    private int _severity;

    /**
     * Whether the marker has a notification.
     */
    private boolean _hasNotification = false;

    /**
     * Location and size of marker in gutter.
     */
    private Rectangle _rect;

    /**
     * Index of associated item in model.
     */
    private int _modelIndex;

    /**
     * Creates an instance of a marker.
     *
     * @param modelIndex index of item in model
     * @param severity the severity for the marker, typically the highest severity for a processor command
     * @param hasNotification whether the marker has a notification
     * @param rect rectangle enclosing marker
     */
    public MarkerInstance ( int modelIndex, int severity, boolean hasNotification, Rectangle rect ) {
    	this._modelIndex = modelIndex;
    	this._severity = severity;
    	this._hasNotification = hasNotification;
    	this._rect = rect;
    }

    /**
     * Returns the model index associated with the marker
     * @return the index of the item in data model (0+)
     */
    public int getIndex() {
    	return this._modelIndex;
    }

    /**
     * Returns the rectangle associated with the marker.
     * @return rectangle enclosing the marker
     */
    public Rectangle getRect() {
    	return this._rect;
    }

    /**
     * Returns the marker's severity.
     * @return Return the severity associated with the marker
     */
    public int getSeverity() {
    	return this._severity;
    }
  }

}