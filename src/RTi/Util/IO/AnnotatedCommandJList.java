
package RTi.Util.IO;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import RTi.Util.IO.CommandStatusProvider;

/**
 * Provides a JList with line numbers and visual problem markers.
 * 
 * @author dre
 */
public class AnnotatedCommandJList extends JPanel
{
  private static final long serialVersionUID = 1L;
  /** GUI component displaying problem overview of entire list*/
  private OverviewGutter _overviewGutter;
  private ProblemGutter _problemGutter;
  /** Extended JList */
  private JList _jList;
  /** JScrollPane containing JList */
  private JScrollPane _jScrollPane; 
  
  ListModel _dataModel;
  
  /**
   * Creates a component for viewing a list with line numbers &
   * markers.
   */
  public AnnotatedCommandJList()
  {
	  initialize();
  }
  /**
   * Creates a component for viewing a list with line numbers &
   * markers.
   * <p>
   * The compound component consists of a JScrollPane containing
   * a JList, ProblemGutter & OverviewGutter.
   * <p>
   * Markers can only be displayed for data model items implementing the
   * <code>CommandStatusProvider</code> interface.
   * 
   * @param dataModel - data model to be displayed
   */
  public AnnotatedCommandJList(ListModel dataModel)
  {
	  _dataModel = dataModel;
	  initialize();
	  _jList.setModel(dataModel);
	  
      // Listen for data model changes
	  //dataModel.addListDataListener(new MyListDataListener());
  }
  
  /**
   * Initialize and connect components.
   */
  private void initialize()
  {
    setLayout(new BorderLayout());
    // Override JList's getToolTipText method to return tooltip
    _jList = new JList();
 //Uncomment the semicolon after JList() above & uncomment the following
 //block to see JList tooltips    
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
  
    // Enable horizontal scrolling
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
      new ChangeListener() 
      {
        public void stateChanged(ChangeEvent e)
        {
        e.getSource();
        _jList.repaint();
        }
      });
    
    // Provide line numbers and markers
    _problemGutter = new ProblemGutter(_jList, _jScrollPane);
    _jScrollPane.setRowHeaderView(_problemGutter);
    
    // Provide an overview of markers
    _overviewGutter = new OverviewGutter(_dataModel, _jList);
    add(_overviewGutter, BorderLayout.EAST);
  } // eof initialize()
  
  /**
   * Returns the JList component.
   * 
   * @return
   */
 public JList getJList()
 {
	 return _jList;
 }

/**
 * Sets the font for the JList
 * 
 * @param font font to be used
 */
 public void setFont(Font font)
 {
   if (_jList != null)
     {
       _jList.setFont(font);
     }
 }
} // AnnotatedList
/**
 * Provides a ListCellRenderer for JList, enabling a functional 
 * JScrollPane horizontal scroll bar.
 *
 * By default Swing will show JList items with an ellipsis if they are
 * too long to display in the available space. Therefore JScrollpane's
 * horizontal scroll bar is not activated. Or, if it is displayed it does
 * not display the knob.
 *
 * This behaviour can be changed to make the horizontal scroll bar
 * functional by providing the JList a ListCellRenderer which avoids
 * using the ellipsis i.e.  SwingUtilities.layoutCompoundLabel(..)
 * (And the private layoutCompoundLabelImpl method that does the
 * actual implementation).
 * <p><p>
 * Usage:
 * <pre>
 * JList jList = new JList();
 * jList.setCellRenderer(new HorzScrollListCellRenderer());
 * // Following statement needed to cause scrollbar to show 
 * jList.setMinimumSize(300,100);
 * </pre>
 * 
 * <b>setMinimumSize() is crucial to scroll bar showing, though I haven't
 * researched why. I suspect it somehow triggers code in
 * JScrollPane.
 */

class HorzScrollListCellRenderer extends JPanel implements ListCellRenderer
{
  private static final long serialVersionUID = 1L;
  private Object _currentValue;
  private JList _currentList;
  protected static Border _noFocusBorder;
  private boolean _isSelected;
  /**
   * Creates a HorzListCellRenderer for JList that avoids the use of
   * ellipsis when the rendering width is insufficient. This is necessary
   * if a horizontal scroll bar is to be employed for displaying the text.
   */
  public HorzScrollListCellRenderer()
  {
  }
 
  public Component getListCellRendererComponent( JList list,
                         Object value,
                         int index,
                         boolean isSelected,
                         boolean cellHasFocus)
  {
    _currentValue = value;
    _currentList = list;
    _isSelected = isSelected;
    _noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : _noFocusBorder);
    return this;
  }

  public void paintComponent(Graphics g)
  {
    // draw text
    int stringLen = g.getFontMetrics().stringWidth(_currentValue.toString());
    int ht = _currentList.getFixedCellHeight();

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
  
  public Dimension getPreferredSize()
  {
    //TODO: Investigate why only first JList item used in determining width
    //BasicListUI.updateLayoutState call getPreferredSize()
    // look at logic around setFixed cellheight/width
    Graphics2D g = (Graphics2D)getGraphics();
    if (g != null)
      {
        //int stringLen = g.getFontMetrics().stringWidth(_currentValue.toString());
        int stringLen = _currentList.getFontMetrics(_currentList.getFont()).stringWidth(_currentValue.toString());

        int ht = _currentList.getFixedCellHeight();
        return new Dimension( stringLen, ht );
      }
    else
      return new Dimension( 150, 10 );
  }

}