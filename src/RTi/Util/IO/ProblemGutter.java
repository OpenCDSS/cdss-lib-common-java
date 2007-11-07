package RTi.Util.IO;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;

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
 * ProblemGutter is used for displaying line numbers and markers next to a JList 
 * in a JScrollPane.
 * <p>
 * When the mouse hovers over a marker, text associated with the marker
 * is displayed. 
 * <p>
 * The text is obtained from JList model items implementing the <code>
 * CommandStatusProvider </code> interface.
 * <p>
 * The margin showing the line numbers & markers may be hidden/shown by
 * clicking in the gutter. (When collapsed, the gutter is only a few pixels
 * wide.
 * 
 */

public class ProblemGutter extends JComponent
implements AdjustmentListener
{
  /**
   * GutterRowIterator encapsulates layout logic for the
   * <code>ProblemGutter</code>. 
   */
  class GutterRowIterator
  {
    private double barHeight;

    private double y;

    GutterRowIterator()
      {
        int height = getHeight();
        int rowHeight = (int) _jList.getCellBounds(0, 0).getHeight();
        int errorHeight = _jList.getModel().getSize() * rowHeight;
        // FIXME SAM 2007-08-16 Need to handle the following more gracefully.
        // _jList may have a cell height of zero, so initialize to non-zero.
        if ( errorHeight == 0 )
          {
        	errorHeight = 1;
          }
        barHeight = rowHeight
            * Math.min(height / (double) errorHeight, 1.);
      }

    /**
     * Returns next row position.  
     */
    void next()
    {
      y += barHeight;
    }
  } // eof class GutterRowIterator
  
  private static String PKG = new String ("RTi/Util/IO");

  private static ImageIcon errorIcon = null;
 
  private static final long serialVersionUID = 1L;

  private static ImageIcon warningIcon = null;
  private static ImageIcon unknownIcon = null;
  private int              _iconOffset;
  /** JList component */
  JList            _jList;
  /** Scroll pane containing the JList & ProblemGutter */
  JScrollPane      _jScrollPane;
  /** Width of bar */
  private static final int BAR_WIDTH       = 4;
  /** Dimension of ProblemGutter component */
  private Dimension   _dimn         = new Dimension();
  /** Flag for whether ProblemGutter component expanded */
  private boolean _isComponentExpanded   = true;

  /** 
   * Creates a JComponent displaying line numbers and problem markers.
   * 
   * @param jList <code>JList component</code>
   * @param scroller <code> JScrollPane component </code>
   */
  public ProblemGutter(JList jList, JScrollPane scroller)
    {	String routine = getClass().getName() + ".ProblemGutter";
      _jList = jList;
      _jScrollPane = scroller;
      
      if ( errorIcon == null )
        {
    	  try 
    	    {
    	      //TODO: dre refactor
    		  errorIcon = JGUIUtil.loadIconImage (PKG +"/error.gif");
    		  if (errorIcon == null)
    		    {
    		      URL url = this .getClass().getResource("error.gif" );
    		      errorIcon = new ImageIcon(url);
    		      if (errorIcon.getImageLoadStatus() != MediaTracker.COMPLETE)
    		        {
    		          Message.printWarning ( 2, routine, "Unable to load icon using getResource()");
    		        }
    		    }
    	    }
    	  catch ( Exception e ) 
    	  {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" +
    		  PKG +"/error.gif" );
    		  //Message.printWarning ( 2, "", "Unable to load icon " + "error.gif");

    	  }
      }
      if ( warningIcon == null ) 
        {
    	  try 
    	  {
    		 warningIcon = JGUIUtil.loadIconImage (PKG +"/warning.gif");
    		  //URL url = this .getClass().getResource("warning.gif" );
    		  //warningIcon = new ImageIcon(url);
    	  }
    	  catch ( Exception e )
    	  {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" +
    		  PKG +"/warning.gif" );
            //Message.printWarning ( 2, "", "Unable to load icon " + "warning.gif");

    	  }
      }
      if ( unknownIcon == null )
        {
    	  try
    	  {
    		  unknownIcon = JGUIUtil.loadIconImage (PKG +"/unknown.gif");
    		  //URL url = this .getClass().getResource("unknown.gif" );
              //unknownIcon = new ImageIcon(url);
    	  }
    	  catch ( Exception e ) {
    		  Message.printWarning ( 2, routine, "Unable to load icon using \"" +
    				  PKG +"/unknown.gif" );
            //Message.printWarning ( 2, "", "Unable to load icon " + "unknown.gif");
    	    
    	  }
      }

      // Add listener to hide/show problemGutter
      addMouseListener(new java.awt.event.MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (!inBounds(e.getX(), e.getY()))
            {
              return;
            }
          if (_isComponentExpanded)
            {
              hideBar();
            }
          else
            {
              showBar();
            }
        }
      });
      
      addMouseMotionListener(new MouseMotionAdapter()
      {
        /**
         * Detects when the mouse is over a marker and displays the
         * marker text.
         */
        public void mouseMoved(MouseEvent e) 
          {
            int index = findError(e.getPoint());
            ListModel dataModel = _jList.getModel();
            if( index > -1 && index < dataModel.getSize())
              {
                CommandStatusProvider csp = (CommandStatusProvider) dataModel
                  .getElementAt(index);
                int highestSeverity = CommandStatusProviderUtil.getHighestSeverity(csp);
                if (highestSeverity !=CommandStatusType.UNKNOWN.getSeverity())
                  {
                    // Change pointer to indicate on marker
                    ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    showMarkerText(e);
                  }
                else
                  {
                    ((JComponent)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    ((JComponent)e.getComponent()).setToolTipText(null);
                  }
              }
            else
              {
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

  public void adjustmentValueChanged(AdjustmentEvent ae)
  {
    _jScrollPane.validate();
  }

  /**
   * Returns the index 
   */ 
  private int findError(Point p)
  {
    GutterRowIterator it = new GutterRowIterator();
    int idx = 0;

    while (it.y < getHeight())
      {
        if (p.y >= it.y && p.y < it.y + it.barHeight)
          {
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
  private ImageIcon getMarker(int index)
  {
	  if (index > _jList.getModel().getSize()-1)
	  {
	    return null;
	  }
    Object o = _jList.getModel().getElementAt(index);
    CommandStatusProvider csp;
    ImageIcon markerIcon = null;

    //TODO: dre: refactor to CommandStatusProviderUtil
    // but then CommandStatusProviderUtil would need to
    // deal with icons
    if (o instanceof CommandStatusProvider)
      {
        csp = (CommandStatusProvider)o;
        CommandStatus cs = csp.getCommandStatus();
        markerIcon = null;
        if (cs != null)
          {
            // FIXME SAM 2007-08-15 Remove if tests out
        	//int severity = CommandStatusProviderUtil.getHighestSeverity(csp);
        	CommandStatusType severity = CommandStatusUtil.getHighestSeverity(csp);
        	
            // Determine icon for marker
           if (severity.equals(CommandStatusType.WARNING))
             {
               //TODO: dre refactor so icons are "provided"
              markerIcon = warningIcon;
              }
           else if (severity.equals(CommandStatusType.FAILURE))
            {
              markerIcon = errorIcon;
            }
           // TODO SAM 2007-08-16 Remove when all commands have
           // been updatd - don't want to deceive users into thinking
           // that error handling is updated everywhere
           else if (severity.equals(CommandStatusType.UNKNOWN))
           {
             markerIcon = unknownIcon;
           }
           // No marker for SUCCESS - only show problems
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
  private String getMarkerText(int index)
  {
    String markerText = "No Status Available";
	  if (_jList.getModel().getSize()== 0)return null;
	  
    Object o = _jList.getModel().getElementAt(index);
    if (o instanceof CommandStatusProvider)
      {
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
    private int getMyWidth()
    {
      FontMetrics fm = _jList.getFontMetrics(_jList.getFont());
  
      int lineNumberWidth = fm.stringWidth(getVisibleEndLine() + "");
      _iconOffset = BAR_WIDTH + 4 + lineNumberWidth;
      //int wWidth= warningIcon.getIconWidth();
      return _isComponentExpanded ? lineNumberWidth + warningIcon.getIconWidth() + 4 + BAR_WIDTH
          : BAR_WIDTH;
    }
    
    /**
     * Returns preferred size of ProblemGutter.
     * 
     * @return preferred size
     */
    public Dimension getPreferredSize()
    {
      _dimn.width = getMyWidth();
      _dimn.height = _jList.getHeight();
      return _dimn;
    }

  /**
   * Returns index of last visible item.
   * 
   * @return last visible item index
   */
  private int getVisibleEndLine()
  {
    int lastLine = _jList.getLastVisibleIndex();
    return lastLine;
  }
  
  /**
   * Returns index of first visible item.
   * @return first visible item index

  private int getVisibleStartLine()
  {
    int firstLine = _jList.getFirstVisibleIndex();
    return firstLine;
  }
   */
 
  /** 
   * Collapses ProblemGutter to minimum width.
   */
  private void hideBar()
  {
    _isComponentExpanded = false;
    _jScrollPane.setRowHeaderView(this);
  }
  private boolean inBounds(int x, int y)
  {
    //    if (showing)
    //      return x > (d.width - BAR);
    return true;
  }
  
  public void paint(Graphics g)
  {
    // draw the border one pixel bigger in height so bottom left bevel
    // can look like it doesn't turn.
    // we will paint over the top and bottom center portions of the border
    // in paintNumbers
    getBorder().paintBorder(this, g, 0, 0, _dimn.width, _dimn.height + 1);

    //  Insets insets = getBorder().getBorderInsets(this);
    //  g.drawRect(0, 0, getWidth()- insets.right-1, getHeight() - insets.bottom+1);
    if (_isComponentExpanded)
      {
        paintNumbers(g);
        paintMarkers(g);
      }
  }
  
  /**
   * Paints Markers.
   * 
   * @param g
   */
  private void paintMarkers(Graphics g)
  {
	if (_jList.getModel().getSize() < 1)
	  {
		  return;
	  }
    ImageIcon markerIcon;
    Rectangle r = g.getClipBounds();
    Insets insets = getBorder().getBorderInsets(this);
    // adjust the clip
    // trim the width by border insets
    r.width -= insets.right + insets.left;
    // slide the clip over by the left insets
    r.x += insets.left;
    // we never trimmed the top or bottom.
    // this will paint over the border.
    // ((Graphics2D) g).fill(r);

    int cellHeight = 10; // Provide a default
    if (_jList.getModel().getSize() > 0)
      {
    	cellHeight = _jList.getCellBounds(0,0).height;
      }
    
    int y = 0;// cellHeight;

    g.setColor(UIManager.getColor("Label.foreground"));

    // for (int i = (int) Math.floor(y / h) + 1; i <= max + 1; i++)
   // int firstLine = _jList.getFirstVisibleIndex();
   // int lastLine = _jList.getLastVisibleIndex();
    int lastLine = _jList.getModel().getSize();

    for (int i = 0; i < lastLine; i++)
      {

        markerIcon = getMarker(i);
        if (markerIcon != null)
          {
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
    private void paintNumbers(Graphics g)
    {
      g.setColor(UIManager
          .getColor("InternalFrame.activeTitleBackground"));
      //    g.setColor(UIManager.getColor("control"));
      Rectangle r = g.getClipBounds();
      Font f = _jList.getFont();
      Font italicFont = f.deriveFont(Font.ITALIC);
      g.setFont(italicFont);
      FontMetrics fm = g.getFontMetrics(f);
      Insets insets = getBorder().getBorderInsets(this);
  
      // adjust the clip
      // trim the width by border insets
      r.width -= insets.right + insets.left;
      // slide the clip over by the left insets
      r.x += insets.left;
      // we never trimmed the top or bottom.
      // this will paint over the border.
     // ((Graphics2D) g).fill(r);
  
      int cellHeight = 20; // default to something non-zero
      if (_jList.getModel().getSize()> 0)
        {
    	  cellHeight = _jList.getCellBounds(0,0).height;
        }
      int ascent = fm.getAscent();
      
      int h =  cellHeight;
      int y = (int) (r.getY() / h) * h;
      int max = (int) (r.getY() + r.getHeight()) / h;
  
      g.setColor(UIManager.getColor("Label.foreground"));
  
      for (int i = (int) Math.floor(y / h) + 1; i <= max + 1; i++)
        {
          int xx = _iconOffset - fm.stringWidth("" + (i));
          g.drawString(i + "", xx, y + ascent);
          y += cellHeight;
        }
    }
  
    /** 
     * Convenience method for expanding the Problem Gutter.
     */
  private void showBar()
  {
    _isComponentExpanded = true;
    _jScrollPane.setRowHeaderView(this);
  }
  
  /**
   * Displays text associated with marker using a ToolTip.
   * 
   * @param e mouse event 
   */
  private void showMarkerText(MouseEvent e)
  {
    String markerText = null;

    int index = findError(e.getPoint());
    if( index > -1 && index < _jList.getModel().getSize())
      {
        markerText = getMarkerText(index);
        //  showProblem( "Problem",markerText);
      }
   // System.out.println("MarkerClass: "+e.getSource().getClass().getName());
    ((JComponent)e.getComponent()).setToolTipText(markerText);
  }
}