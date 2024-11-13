// AnnotatedCommandJList - provides a JList with line numbers and visual problem markers

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides a JList with left gutter (line numbers and icon indicating issue) and a right gutter gutter showing relative position of issues:
 * <pre>
 * +--------------------------------------AnnotatedComandJList (extends JPanel) ------------+
 * | +-----------------+-------------------------------------------------+----------------+ |
 * | | ProblemGutter   |         JList (with data model)                 | OverviewGutter | |
 * | |                 |                                                 |                | |
 * | |                 |                                                 |                | |
 * | |                 |                                                 |                | |
 * | |                 |                                                 |                | |
 * | |                 |                                                 |                | |
 * | +-----------------+-------------------------------------------------+----------------+ |
 * +----------------------------------------------------------------------------------------+
 * </pre>
 */
public class AnnotatedCommandJList extends JPanel {
  private static final long serialVersionUID = 1L;
  /**
   * UI component displaying line numbers and icons for problem, for visible list items.
   */
  private ProblemGutter _problemGutter;

  /**
   * UI component displaying overview rectangles in relative Y-position for the entire list.
   */
  private OverviewGutter _overviewGutter;

  /**
   * JList in the middle, which contains the data being displayed.
   */
  private JList _jList;

  /**
   * JScrollPane containing the JList.
   */
  private JScrollPane _jScrollPane;

  /**
   * List model for the commands, for example a TSCommandProcessorListModel when used with TSTool.
   */
  private ListModel _dataModel;

  /**
   * The last command phase type that applies.
   * This is used so that if there are discovery issues they are only shown during discovery, and not after running.
   * Dynamic command files often have discovery issues that are cleared up after running.
   */
  private CommandPhaseType lastCommandPhaseType = null;

  /**
   * Creates a component for viewing a list with line numbers and markers.
   */
  public AnnotatedCommandJList() {
	  initialize();
  }

  /**
   * Creates a component for viewing a list with line numbers and markers.
   * <p>
   * The compound component consists of a JScrollPane containing a JList, ProblemGutter, and OverviewGutter.
   * </p>
   * <p>
   * Markers can only be displayed for data model items implementing the <code>CommandStatusProvider</code> interface.
   * </p>
   *
   * @param dataModel - data model to be displayed
   */
  public AnnotatedCommandJList ( ListModel dataModel ) {
	  _dataModel = dataModel;
	  initialize();
	  _jList.setModel(dataModel);

      // Listen for data model changes.
	  //dataModel.addListDataListener(new MyListDataListener());
  }

  /**
   * Initialize and connect components.
   */
  private void initialize() {
    setLayout(new BorderLayout());
    // Override JList's getToolTipText method to return tooltip.
    _jList = new JList();
    // Uncomment the semicolon after JList() above and uncomment the following block to see JList tooltips.
//    {
//      static final long serialVersionUID = 1L;
//
//      /**
//       * Returns text to be displayed in tool tip.
//       * <p>
//       * Called by ToolTipMgr to get tool tip text
//       */
//       public String getToolTipText(MouseEvent event)
//       {
//   	     if (getModel().getSize() != 0)
//    	   {
//         // Get item index
//         int index = locationToIndex(event.getPoint());
//
//         // Get item
//
//         Object item = getModel().getElementAt(index);
//          return CommandStatusProviderUtil.getCommandLogHTML(item);
//         }
//         else
//         {
//        	 return new String("");
//         }
//       }
//    };

    // Enable horizontal scrolling.
    // FontMetrics metrics = getFontMetrics(_jList.getFont());
    // System.out.println(_jList.getFont().toString());
    // Set a fixed cell height to avoid calling the ListCellRenderer
    // getPreferredSize() method
   // _jList.setFixedCellHeight(metrics.getHeight());
   //dre _jList.setCellRenderer(new HorzScrollListCellRenderer());
    _jList.setPrototypeCellValue("gjqqyAZ");
    _jList.setFixedCellWidth(-1);
    _jScrollPane = new JScrollPane(_jList);
    add(_jScrollPane, BorderLayout.CENTER);

    _jScrollPane.getViewport().addChangeListener(
    	new ChangeListener() {
    		public void stateChanged(ChangeEvent e) {
    			e.getSource();
    			_jList.repaint();
    		}
    	}
    );

    // Provide line numbers and markers.
    _problemGutter = new ProblemGutter(_jList, _jScrollPane);
    _jScrollPane.setRowHeaderView(_problemGutter);

    // Get the size of the vertical scrollbar arrow,
    // and the "lowerright" component in the JScroll pane in order to compute the vertical size of wasted space.
    // This space is passed to the overview gutter constructor and is used to offset computations of the problem indicators.
    //Message.printStatus_jScrollPane.get

    // Provide an overview of markers.
    _overviewGutter = new OverviewGutter(_dataModel, _jList);
    add(_overviewGutter, BorderLayout.EAST);
  }

  /**
   * Returns the JList component.
   * @return the JList component
   */
 public JList getJList() {
	 return _jList;
 }

/**
 * Sets the font for the JList.
 * @param font font to be used
 */
 public void setFont(Font font) {
	 if (_jList != null) {
		 _jList.setFont(font);
     }
 }

 /**
  * Set the last command phase that for the component.
  * @param lastCommandPhase the last command phase run,
  * provided as a hint to the annotated command list so earlier warnings like discovery are not shown after running
  */
 public void setLastCommandPhase ( CommandPhaseType lastCommandPhaseType ) {
	 this.lastCommandPhaseType = lastCommandPhaseType;
	 this._problemGutter.setLastCommandPhase(lastCommandPhaseType);
 }
} // AnnotatedCommandJList

// TODO smalers 2024-11-04 Need to remove since not used.
/**
 * <p>
 * Provides a ListCellRenderer for JList, enabling a functional JScrollPane horizontal scroll bar.
 * </p>
 * <p>
 * By default Swing will show JList items with an ellipsis if they are too long to display in the available space.
 * Therefore, JScrollpane's horizontal scroll bar is not activated.
 * Or, if it is displayed it does not display the knob.
 * </p>
 * <p>
 * This behavior can be changed to make the horizontal scroll bar functional by providing
 * the JList a ListCellRenderer which avoids using the ellipsis (i.e., SwingUtilities.layoutCompoundLabel(...))
 * (and the private layoutCompoundLabelImpl method that does the actual implementation).
 * </p>
 * <p>
 * Usage:
 * </p>
 * <pre>
 * JList jList = new JList();
 * jList.setCellRenderer(new HorzScrollListCellRenderer());
 * // The following statement is needed to cause the scrollbar to show.
 * jList.setMinimumSize(300,100);
 * </pre>
 * <p>
 * <b>setMinimumSize() is crucial to scroll bar showing, although it is not clear why (perhaps it somehow triggers code in JScrollPane?).</b>
 * </p>
 */

class xHorzScrollListCellRenderer extends JPanel implements ListCellRenderer {
  private static final long serialVersionUID = 1L;
  private Object _currentValue;
  private JList _currentList;
  protected static Border _noFocusBorder;
  private boolean _isSelected;

  /**
   * Creates a HorzListCellRenderer for JList that avoids the use of ellipsis when the rendering width is insufficient.
   * This is necessary if a horizontal scroll bar is to be employed for displaying the text.
   */
  public xHorzScrollListCellRenderer() {
  }

  public Component getListCellRendererComponent( JList list,
	  Object value,
	  int index,
	  boolean isSelected,
	  boolean cellHasFocus) {
    _currentValue = value;
    _currentList = list;
    _isSelected = isSelected;
    _noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : _noFocusBorder);
    return this;
  }

  /**
   * Paint the component.
   * @param g Graphics instance for rendering
   */
  public void paintComponent(Graphics g) {
    // Draw text.
    //int stringLen = g.getFontMetrics().stringWidth(_currentValue.toString());
    //int ht = _currentList.getFixedCellHeight();

    // g.setColor(currentList.getBackground());
    g.setColor(_isSelected?_currentList.getSelectionBackground():
    _currentList.getBackground());
    g.fillRect(0,0,getWidth(),getHeight());

    getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());

    g.setColor(_currentList.getForeground());
    g.setColor(_isSelected?_currentList.getSelectionForeground():
    _currentList.getForeground());
    //g.drawString(currentValue.toString(),0,ht/2);
//    g.drawString(currentValue.toString(), 0,
//            g.getFontMetrics().getLeading()
//            +g.getFontMetrics().getAscent());
    FontMetrics fm = _currentList.getFontMetrics(_currentList.getFont());
    g.drawString(_currentValue.toString(), 0,
            fm.getLeading()
            +fm.getAscent());
  }

  /**
   * Get the preferred size for the component.
   */
  public Dimension getPreferredSize() {
    //TODO: Investigate why only first JList item used in determining width
    //BasicListUI.updateLayoutState call getPreferredSize()
    // look at logic around setFixed cellheight/width
    Graphics2D g = (Graphics2D)getGraphics();
    if (g != null) {
        //int stringLen = g.getFontMetrics().stringWidth(_currentValue.toString());
        int stringLen = _currentList.getFontMetrics(_currentList.getFont()).stringWidth(_currentValue.toString());

        int ht = _currentList.getFixedCellHeight();
        return new Dimension( stringLen, ht );
    }
    else {
    	return new Dimension( 150, 10 );
    }
  }

}