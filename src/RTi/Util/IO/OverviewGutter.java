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
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;

import RTi.Util.Message.Message;

/**
 * Provides an overview of Problems associated with the
 * MessageModel. 
 * <p>
 * Problem markers are drawn relative to the position where they occur
 * in the model.
 * <p>
 * When a gutter marker is clicked, the associated
 * index in the list will be scrolled to and selected.
 */
public class OverviewGutter extends JComponent
{
  private static final long serialVersionUID = 1L;

  /** Height of marker */
  private final static int MARKER_HEIGHT = 4;
  /** Margin between Component edge & marker */
  private final static int LEFT_MARGIN = 2;
  /** Component's preferred width */
  private final static int PREFERRED_WIDTH = 12;
  /** Data model associated with OverviewGutter */
  private final ListModel _dataModel;
  /** Height of gutter used for marker positioning (it is MARKER_HEIGHT shorter*/
  int _gutterHeight;
  /** Maintains the list of markers */
  private Vector _markerInstances = new Vector();
  /** JList associated with OverviewGutter */
  private final JList        _list;

  /**
   * Creates an overview gutter for the specified data model & JList.
   * <p>
   * Problems associated with data model items are indicated visually by
   * colored markers drawn in the gutter.
   * 
   * @param dataModel Associated data model
   * @param list  Associated JList
   */
  public OverviewGutter(ListModel dataModel, JList list)
    {
      _dataModel = dataModel;
      _list = list;
      
      setName("overviewGutter");

      // Detect mouse clicks on markers & ensure associated JList item is visible
      addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent me)
          {
            int index = findMarkerInstance(me.getPoint());
            if (index >= 0)
              {
                _list.ensureIndexIsVisible(index);
                _list.setSelectedIndex(index);
              }
          }
        
        public void mouseExited(MouseEvent e)
        {
          setToolTipText(null);
        }
      });
      
      // Display marker text when pointer is over a marker
      addMouseMotionListener(new MouseMotionAdapter()
      {
        public void mouseMoved(MouseEvent e) 
        {
          String markerText = null;
          
          int index = findMarkerInstance(e.getPoint());
          
          if( index > -1 && index < _dataModel.getSize())
            {
              CommandStatusProvider csp = (CommandStatusProvider) _dataModel
                .getElementAt(index);
              int highestSeverity = CommandStatusProviderUtil.getHighestSeverity(csp);
              if (highestSeverity !=CommandStatusType.UNKNOWN.getSeverity())
                {

                  // Change pointer to indicate on marker
                  ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  markerText = getMarkerText(index);
                }
            }
          else
            {
              ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          ((JComponent)e.getComponent()).setToolTipText(markerText);
        }
      }
      );

       // really only the width matters
      setPreferredSize(new Dimension(PREFERRED_WIDTH, 16));
      setToolTipText(null);
    }
  
  private int findMarkerInstance(Point point)
  {
    int index = -1;
 
    if (_markerInstances.size() > 0)
      {
        MarkerInstance markerInstance;

        // Determine markerInstance containing point
        for (int i= 0; i < _markerInstances.size(); i++)
          {
          markerInstance = (MarkerInstance)_markerInstances.get(i);
          if (markerInstance.getRect().contains(point))
            {
              index = markerInstance.getIndex();
            }
          }
      }
    return index;
  }
  
  /**
   * Returns the gutter pixel position (0+) corresponding to a JList index.
   * This may be an estimate if the list is long compared to the height of the
   * gutter, resulting in round off in integer math.
   * 
   * @param i JList data model index
   * @return Pixel position in gutter corresponding to JList data model position,
   * assuming that JList index is centered on corresponding gutter position.
   */
  private int findGutterPosition(int index)
    {
      if ( (_gutterHeight <= 0) || (_dataModel.getSize() == 0) ) {
    	  return -1;
      }
      // Order is important to prevent integer math roundoff to zero.
      int y = (_gutterHeight*index)/_dataModel.getSize();
      Message.printStatus ( 1,"findGutterPosition","index=" + index + " height="+ getHeight() + " size="+_dataModel.getSize() + " y="+y);
      return y;
    }

  /**
   * Paints the component on the screen
   * <p>
   * Markers are drawn in the overview gutter relative to their position 
   * in the list. A marker for the first item should be at the top, while
   * a marker for the last item of the list appears at the bottom of the
   * gutter.
   * <p>
   * Markers are drawn from y downward to y + MARKER_HEIGHT, therefore
   * for positioning the height 
   */
  protected void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      _markerInstances.clear();
      
      if (_list.getModel().getSize() == 0) return;
      
       _gutterHeight = getHeight() - MARKER_HEIGHT;
      
      
      // paint background to show gutter area
      // g.setColor(Color.gray);
      // g.fillRect(0,0,getWidth(),getHeight());

      // Find out which markers to draw
      findMarkers();
      // Draw markers
      drawMarkers(g);
    }

  private void drawMarkers(Graphics g)
  {
    Color color;
    /*
     * Loop thru markers & draw
     */
    
    if (_markerInstances.size() > 0)
      {
        MarkerInstance markerInstance;

        for (int i= 0; i < _markerInstances.size(); i++)
          {
            markerInstance = (MarkerInstance)_markerInstances.get(i);
            int severity = markerInstance.getSeverity();
            // TODO:dre refactor colors to CommandStatusProviderUtil?
            if (severity == CommandStatusType.WARNING.getSeverity())
              { color = Color.yellow;
              }
            else if (severity == CommandStatusType.FAILURE.getSeverity())
              { color = Color.red;
              }
            else
              { 
                color = null;
              }
            Rectangle rect = markerInstance.getRect();

            // Paint a filled colored rectangle with black outline.  Center
            // vertically on the y coordinate.
            g.setColor(color);
            g.fillRect(rect.x, rect.y,  rect.width, MARKER_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(rect.x, rect.y, rect.width, MARKER_HEIGHT);
          }
      }
  }

  private void findMarkers()
  {
    int markerWidth = getWidth()- 2* LEFT_MARGIN;
    /*
     * Loop thru data model items detecting problems,
     * creating a new MarkerInstance for each problem.
     */
    for (int i = 0; i < _dataModel.getSize(); i++ )
      {
        // Get the severity associated with an item in the list model.
        int severity = getMarker(i);  // CommandStatus highest severity

        if ( severity == CommandStatusType.WARNING.getSeverity() 
                || severity == CommandStatusType.FAILURE.getSeverity())
          {
            
            // Find position in gutter corresponding to item
            int y = findGutterPosition ( i );
            Rectangle rect = new Rectangle(LEFT_MARGIN, y,
                    markerWidth, MARKER_HEIGHT);
            MarkerInstance markerInstance= new MarkerInstance(i, severity, rect);
            _markerInstances.add(markerInstance);
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
  public String getToolTipText(MouseEvent event)
  {
    String str = null;
    if (_dataModel.getSize() != 0)
      {

    //    int index = findListIndex(event.getPoint());
        int index = findMarkerInstance(event.getPoint());
        if (index > -1)
          {
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
   * Returns the severity the marker.
   * 
   * The highest severity for the item is used to determine which
   * marker to use.
   * @param index JList model item index
   * @return severity index
   */
  private int getMarker(int index)
    {
      int markerIndex= 0;
      CommandStatusProvider csp;

      Object o = _dataModel.getElementAt(index);

      if (o instanceof CommandStatusProvider)
        {
          csp = (CommandStatusProvider)o;
          markerIndex = CommandStatusProviderUtil.getHighestSeverity(csp);
        }
      return markerIndex;
    }

  /**
   * Returns the text (as HTML) associated with the specified data model
   *  index.
   *   
   * @param index Index in data model
   * @return
   */
  private String getMarkerText(int index)
  {
    CommandStatusProvider csp;
    String markerText ="";
    Object o = _dataModel.getElementAt(index);

    if (o instanceof CommandStatusProvider)
      {
        csp = (CommandStatusProvider)o;
        markerText = CommandStatusUtil.getCommandLogHTML(csp);
      }
    return markerText;
  }
  /**
   * Maintains the attributes of a marker.

   * @author dre
   */
  private class MarkerInstance
  {
    /** marker severity */
    private int _severity;
    /** location & size of marker in gutter*/
    private Rectangle _rect;
    /** Index of associated item in model */
    private int _modelIndex;

    /**
     * Creates an instance of a marker.
     * 
     * @param modelIndex index of item in model
     * @param severity 
     * @param rect rectangle enclosing marker
     */
    public MarkerInstance (int modelIndex, int severity, Rectangle rect)
    {
     _modelIndex = modelIndex;
     _severity = severity;
     _rect = rect;
    }
    
    /**
     * Returns the model index associated with the marker
     * @return Index of item in data model
     */
    public int getIndex()
    {
      return _modelIndex;
    }

    /**
     * Returns the rectangle associated with the marker
     * @return rectance enclosing marker
     */
    public Rectangle getRect()
    {
      return _rect;
    }

    /**
     * Returns the marker's severity
     * @return Return the severity associated with the marker
     */
    public int getSeverity()
    {
      return _severity;
    }
  }
  
}

