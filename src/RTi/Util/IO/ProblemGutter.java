// ProblemGutter - used for displaying line numbers and markers next to a JList in a JScrollPane.

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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.UIManager;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusProvider;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatusUtil;
import RTi.Util.Message.Message;

/**
 * ProblemGutter is used for displaying line numbers and markers next to a JList in a JScrollPane.
 * <p>
 * When the mouse hovers over a marker, text associated with the marker is displayed.
 * <p>
 * The text is obtained from JList model items implementing the <code>CommandStatusProvider</code> interface.
 * <p>
 * The margin showing the line numbers & markers may be hidden/shown by
 * clicking in the gutter. (When collapsed, the gutter is only a few pixels wide.
 */

@SuppressWarnings("serial")
public class ProblemGutter extends JComponent
implements AdjustmentListener {
  /**
   * GutterRowIterator encapsulates layout logic for the <code>ProblemGutter</code>.
   */
  class GutterRowIterator {
    private double barHeight;

    private double y;

    GutterRowIterator() {
        int height = getHeight();
        int rowHeight = (int) _jList.getCellBounds(0, 0).getHeight();
        int errorHeight = _jList.getModel().getSize() * rowHeight;
        // FIXME SAM 2007-08-16 Need to handle the following more gracefully.
        // _jList may have a cell height of zero, so initialize to non-zero.
        if ( errorHeight == 0 ) {
        	errorHeight = 1;
        }
        barHeight = rowHeight * Math.min(height / (double) errorHeight, 1.);
      }

    /**
     * Returns next row position.
     */
    void next() {
      y += barHeight;
    }
  } // End class GutterRowIterator

  private static String PKG = new String ("RTi/Util/IO");

  private static ImageIcon errorIcon = null;
  private static ImageIcon noticeIcon = null;
  private static ImageIcon warningIcon = null;
  private static ImageIcon unknownIcon = null;
  private int _iconOffset;

  /**
   * JList component.
   */
  JList<?> _jList = null;

  /**
   * Scroll pane containing the JList & ProblemGutter.
   */
  JScrollPane _jScrollPane;

  /**
   * Width of bar.
   */
  private static final int BAR_WIDTH = 4;

  /**
   * Dimension of ProblemGutter component.
   */
  private Dimension _dimn = new Dimension();

  /**
   * Flag for whether ProblemGutter component expanded.
   */
  private boolean _isComponentExpanded = true;

  /**
   * Use to make decisions about what marker to show.
   * For example if the last phase is RUN, then don't need to choose the marker because of a discovery issue.
   */
  private CommandPhaseType lastCommandPhaseType = null;

  /**
   * Creates a JComponent displaying line numbers and problem markers.
   *
   * @param jList <code>JList component</code>
   * @param scroller <code> JScrollPane component </code>
   */
  public ProblemGutter(JList<?> jList, JScrollPane scroller) {
	  String routine = getClass().getSimpleName() + ".ProblemGutter";
      _jList = jList;
      _jScrollPane = scroller;

      if ( errorIcon == null ) {
    	  try {
    		  errorIcon = JGUIUtil.loadIconImage (PKG +"/error.gif");
    	  }
    	  catch ( Exception e ) {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" + PKG +"/error.gif" );
    	  }
      }
      if ( warningIcon == null ) {
    	  try {
    		 warningIcon = JGUIUtil.loadIconImage (PKG +"/warning.gif");
    	  }
    	  catch ( Exception e ) {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" + PKG +"/warning.gif" );
    	  }
      }
      if ( noticeIcon == null ) {
    	  try {
    		 noticeIcon = JGUIUtil.loadIconImage (PKG +"/notice.gif");
    	  }
    	  catch ( Exception e ) {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" + PKG +"/notice.gif" );
    	  }
      }
      if ( unknownIcon == null ) {
    	  try {
    		  unknownIcon = JGUIUtil.loadIconImage (PKG +"/unknown.gif");
    	  }
    	  catch ( Exception e ) {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" + PKG +"/unknown.gif" );
    	  }
      }

      // Add listener to hide/show problemGutter.
      addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (!inBounds(e.getX(), e.getY())) {
              return;
          }
          if (_isComponentExpanded) {
              hideBar();
          }
          else {
              showBar();
          }
        }
      });

      addMouseMotionListener(new MouseMotionAdapter() {
        /**
         * Detects when the mouse is over a marker and displays the marker text.
         */
        public void mouseMoved(MouseEvent e) {
            // Protect against empty list.
            if (_jList.getModel().getSize() < 1) {
                return;
            }
            int index = findError(e.getPoint());
            ListModel<?> dataModel = _jList.getModel();
            if( index > -1 && index < dataModel.getSize()) {
                CommandStatusProvider csp = (CommandStatusProvider) dataModel.getElementAt(index);
                int highestSeverity = CommandStatusProviderUtil.getHighestSeverity(csp);
                if (highestSeverity !=CommandStatusType.UNKNOWN.getSeverity()) {
                    // Change pointer to indicate on marker.
                    ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    showMarkerText(e);
                }
                else {
                    ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    ((JComponent)e.getComponent()).setToolTipText(null);
                }
              }
            else {
                ((JComponent)e.getComponent()).setToolTipText(null);
                ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
              }
          }
      }
      );
      scroller.getVerticalScrollBar().addAdjustmentListener(this);

      Font f = jList.getFont();
      Font italicFont = f.deriveFont(Font.ITALIC);
      setFont(italicFont);
      setBorder(BorderFactory.createRaisedBevelBorder());
    }

  public void adjustmentValueChanged(AdjustmentEvent ae) {
    _jScrollPane.validate();
  }

  /**
   * Returns the index.
   */
  private int findError(Point p) {
    GutterRowIterator it = new GutterRowIterator();
    int idx = 0;

    while (it.y < getHeight()) {
        if (p.y >= it.y && p.y < it.y + it.barHeight) {
            return idx;
        }
        it.next();
        idx++;
      }
    return -1;
  }

  /**
   * Returns the ImageIcon for the marker.
   *
   * The highest severity for the item is used to determine which
   * marker to use.
   * @param index JList model item index
   * @return
   */
  private ImageIcon getMarker(int index) {
	  if (index > _jList.getModel().getSize()-1) {
	    return null;
	  }
    Object o = _jList.getModel().getElementAt(index);
    CommandStatusProvider csp;
    ImageIcon markerIcon = null;

    //TODO: dre: refactor to CommandStatusProviderUtil
    // but then CommandStatusProviderUtil would need to deal with icons.
    if (o instanceof CommandStatusProvider) {
        csp = (CommandStatusProvider)o;
        CommandStatus cs = csp.getCommandStatus();
        markerIcon = null;
        if (cs != null) {
        	CommandStatusType severity = CommandStatusType.UNKNOWN;
            // Get the highest severity considering the command phase.
        	// TODO sam 2017-04-13 need to get this worked out.
        	boolean legacyLogic = true;
        	if ( legacyLogic ) {
        		// Severity is the max on the command:
        		// - can be an issue if discovery severity is higher than the run severity,
        		//   for example dynamic data not found at discovery but created later
        		// - in this case the run severity (SUCCESS) should be used.
        		severity = CommandStatusUtil.getHighestSeverity(csp);
        	}
        	else {
	        	if ( this.lastCommandPhaseType == CommandPhaseType.INITIALIZATION ) {
	        		// Show all issues.
	        		severity = CommandStatusUtil.getHighestSeverity(csp);
	        	}
	        	else if ( this.lastCommandPhaseType == CommandPhaseType.DISCOVERY ) {
	        		// Show all issues.
	        		severity = CommandStatusUtil.getHighestSeverity(csp);
	        	}
	        	else if ( this.lastCommandPhaseType == CommandPhaseType.RUN ) {
	        		// Just ran and have not done more editing so show only run issues.
	        		CommandPhaseType [] commandPhaseTypes = { CommandPhaseType.RUN };
	        		severity = CommandStatusUtil.getHighestSeverity(csp, commandPhaseTypes );
	        	}
        	}

            // Determine icon for marker.
            if (severity.equals(CommandStatusType.WARNING)) {
               markerIcon = warningIcon;
            }
            else if (severity.equals(CommandStatusType.FAILURE)) {
                markerIcon = errorIcon;
            }
            //else if (severity.equals(CommandStatusType.NOTIFICATION)) {
            else if ( csp.getCommandStatus().getHasNotification(this.lastCommandPhaseType) ) {
               markerIcon = noticeIcon;
            }
            // TODO SAM 2007-08-16 Remove when all commands have been updated.
            // Don't want to deceive users into thinking that error handling is updated everywhere.
            else if (severity.equals(CommandStatusType.UNKNOWN)) {
                markerIcon = unknownIcon;
            }
            // No marker for SUCCESS - only show problems.
       }
    }
    else {
    	markerIcon = unknownIcon;
    }
    return markerIcon;
  }

  /**
   * Returns text associated with marker formatted as HTML.
   *
   * @param index index of item in JList
   * @return text associated with mark formatted as HTML
   */
  private String getMarkerText(int index) {
    String markerText = "No Status Available";
	if (_jList.getModel().getSize()== 0) {
		return null;
	}

    Object o = _jList.getModel().getElementAt(index);
    if (o instanceof CommandStatusProvider) {
        CommandStatusProvider csp = (CommandStatusProvider)o;
        markerText = CommandStatusUtil.getCommandLogHTML(csp);
    }

    return markerText;
  }

  /**
   * Returns component width.
   * <p>
   * As the number of digits in the line number
   * increases so will the required width.
   * @return width in pixels
   */
    private int getMyWidth() {
      FontMetrics fm = _jList.getFontMetrics(_jList.getFont());

      int lineNumberWidth = fm.stringWidth(getVisibleEndLine() + "");
      _iconOffset = BAR_WIDTH + 4 + lineNumberWidth;
      //int wWidth= warningIcon.getIconWidth();
      return _isComponentExpanded ? lineNumberWidth + warningIcon.getIconWidth() + 4 + BAR_WIDTH : BAR_WIDTH;
    }

    /**
     * Returns preferred size of ProblemGutter.
     *
     * @return preferred size
     */
    public Dimension getPreferredSize() {
      _dimn.width = getMyWidth();
      _dimn.height = _jList.getHeight();
      return _dimn;
    }

  /**
   * Returns index of last visible item.
   *
   * @return last visible item index
   */
  private int getVisibleEndLine() {
    int lastLine = _jList.getLastVisibleIndex();
    return lastLine;
  }

  /**
   * Returns index of first visible item.
   * @return first visible item index

  private int getVisibleStartLine() {
    int firstLine = _jList.getFirstVisibleIndex();
    return firstLine;
  }
  */

  /**
   * Collapses ProblemGutter to minimum width.
   */
  private void hideBar() {
      // TODO SAM 2008-10-01 For now disable the hiding because it causes usability problems.
      boolean barIsHideable = false;
      if ( barIsHideable ) {
          _isComponentExpanded = false;
          _jScrollPane.setRowHeaderView(this);
      }
  }

  private boolean inBounds(int x, int y) {
    //    if (showing) {
    //      return x > (d.width - BAR);
	//    }
    return true;
  }

  public void paint(Graphics g) {
    // Draw the border one pixel bigger in height so bottom left bevel can look like it doesn't turn.
    // we will paint over the top and bottom center portions of the border in paintNumbers>
    getBorder().paintBorder(this, g, 0, 0, _dimn.width, _dimn.height + 1);

    //  Insets insets = getBorder().getBorderInsets(this);
    //  g.drawRect(0, 0, getWidth()- insets.right-1, getHeight() - insets.bottom+1);
    if (_isComponentExpanded) {
        paintNumbers(g);
        paintMarkers(g);
    }
  }

  /**
   * Paints Markers.
   *
   * @param g
   */
  private void paintMarkers(Graphics g) {
	if (_jList.getModel().getSize() < 1) {
		return;
	}
    ImageIcon markerIcon;
    Rectangle r = g.getClipBounds();
    Insets insets = getBorder().getBorderInsets(this);
    // Adjust the clip.
    // Trim the width by border insets.
    r.width -= insets.right + insets.left;
    // Slide the clip over by the left insets.
    r.x += insets.left;
    // Have never trimmed the top or bottom.
    // This will paint over the border.
    // ((Graphics2D) g).fill(r);

    int cellHeight = 10; // Provide a default.
    if (_jList.getModel().getSize() > 0) {
    	cellHeight = _jList.getCellBounds(0,0).height;
    }

    int y = 0;// cellHeight;

    g.setColor(UIManager.getColor("Label.foreground"));

    // for (int i = (int) Math.floor(y / h) + 1; i <= max + 1; i++)
    // int firstLine = _jList.getFirstVisibleIndex();
    // int lastLine = _jList.getLastVisibleIndex();
    int lastLine = _jList.getModel().getSize();

    for (int i = 0; i < lastLine; i++) {

        markerIcon = getMarker(i);
        if (markerIcon != null) {
            //g.drawString(i + "", insets.left, y + ascent);
            markerIcon.paintIcon(this, g, _iconOffset,y );
        }
        // y += h;
        y += cellHeight;
      }
  } // eof paintMarkers

  /**
   * Paints line numbers.
   * @param g
   */
    private void paintNumbers(Graphics g) {
      g.setColor(UIManager.getColor("InternalFrame.activeTitleBackground"));
      //    g.setColor(UIManager.getColor("control"));
      Rectangle r = g.getClipBounds();
      Font f = _jList.getFont();
      Font italicFont = f.deriveFont(Font.ITALIC);
      g.setFont(italicFont);
      FontMetrics fm = g.getFontMetrics(f);
      Insets insets = getBorder().getBorderInsets(this);

      // Adjust the clip.
      // Trim the width by border insets.
      r.width -= insets.right + insets.left;
      // Slide the clip over by the left insets.
      r.x += insets.left;
      // Have never trimmed the top or bottom.
      // This will paint over the border.
      // ((Graphics2D) g).fill(r);

      int cellHeight = 20; // Default to something non-zero.
      if (_jList.getModel().getSize()> 0) {
    	  cellHeight = _jList.getCellBounds(0,0).height;
      }
      int ascent = fm.getAscent();

      int h =  cellHeight;
      int y = (int) (r.getY() / h) * h;
      int max = (int) (r.getY() + r.getHeight()) / h;

      g.setColor(UIManager.getColor("Label.foreground"));

      for (int i = (int) Math.floor(y / h) + 1; i <= max + 1; i++) {
          int xx = _iconOffset - fm.stringWidth("" + (i));
          g.drawString(i + "", xx, y + ascent);
          y += cellHeight;
      }
    }

    /**
     * Set the last command phase that for the component.
     */
    public void setLastCommandPhase ( CommandPhaseType lastCommandPhaseType ) {
   	 this.lastCommandPhaseType = lastCommandPhaseType;
    }

    /**
     * Convenience method for expanding the Problem Gutter.
     */
  private void showBar() {
      if ( !_isComponentExpanded ) {
          _isComponentExpanded = true;
          _jScrollPane.setRowHeaderView(this);
      }
  }

  /**
   * Displays text associated with marker using a ToolTip.
   *
   * @param e mouse event
   */
  private void showMarkerText(MouseEvent e) {
    String markerText = null;

    int index = findError(e.getPoint());
    if( index > -1 && index < _jList.getModel().getSize()) {
        markerText = getMarkerText(index);
        //  showProblem( "Problem",markerText);
      }
    // System.out.println("MarkerClass: "+e.getSource().getClass().getName());
    ((JComponent)e.getComponent()).setToolTipText(markerText);
  }
}