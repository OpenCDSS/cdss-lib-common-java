package RTi.GRTS;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** 
 * Listen for MouseEvent and MouseMotionEvent that are of interest to the underlying TSGraphJComponent
 * and redispatch.  Draw the mouse tracker on the graph based on information from the TSGraphJComponent.
 */
public class TSGraphJComponentGlassPaneMouseListener implements MouseListener, MouseMotionListener {
    /**
     * Glass pane that is used to render the mouse tracker.
     */
    TSGraphJComponentGlassPane glassPane;

    /**
     * TSGraphJComponent that renders the normal graph.
     */
    TSGraphJComponent tsgraphJComponent;

    /**
     * The content pane that manages the rendering components, not currently used.
     */
    Container contentPane;

    /**
     * Indicates whether dragging the mouse, not currently used but may move from
     * the main rendering component to the tracker component to improve performance.
     */
    boolean inDrag = false;

    /**
     * Construct the mouse listener for the glass pane.
     * @param glassPane TSGraphJComponentGlassPane instance to render mouse tracker.
     * @param tsgraphJComponent underlying TSGraphJComponent to render the graph product.
     * @param contentPane component that manages the glass pane and main rendering component.
     */
    public TSGraphJComponentGlassPaneMouseListener(TSGraphJComponentGlassPane glassPane,
    	TSGraphJComponent tsgraphJComponent, Container contentPane) {
        this.glassPane = glassPane;
        this.tsgraphJComponent = tsgraphJComponent;
        this.contentPane = contentPane;
    }

    /**
     * Handle mouse moved event in the glass pane.
     */
    public void mouseMoved(MouseEvent e) {
    	//System.out.println("In TSGraphJComponentGlassPaneMouseListener.mouseMoved:  glassPanePoint=" + e.getX() + "," + e.getY());
    	// Cause a redraw of the mouse tracker
        redispatchMouseEvent(e, true);
    }

    /**
     * Handle mouse dragged event in the glass pane.
     */
    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /**
     * Handle mouse clicked event in the glass pane.
     */
    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /**
     * Handle mouse entered event in the glass pane.
     */
    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /**
     * Handle mouse exited event in the glass pane.
     */
    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /**
     * Handle mouse pressed event in the glass pane.
     */
    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    /**
     * Handle mouse released event in the glass pane.
     */
    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e, true);
        this.inDrag = false;
    }

    // TODO sam 2017-02-26 is there a need to separate the event types with different methods?
    /**
     * Redispatch all mouse events to the underlying TSGraphJComponent.
     * This handles MouseEvent and MouseMotionEvent.
     * @param e MouseEvent from glass pane
     * @param repaint if true then redraw the glass pane
     */
    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
        Point glassPanePoint = e.getPoint();
        //System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  originating mouse event component is " + e.getComponent().getName());
        //System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  glassPanePoint=" + glassPanePoint.x + "," + glassPanePoint.y);
        //Container container = contentPane;
        // Convert the point in the glass pane to the JFrame content pane coordinates
        //Point containerPoint = SwingUtilities.convertPoint(
        //                                glassPane,
        //                                glassPanePoint, 
        //                                contentPane);
        //System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  containerPoint=" + containerPoint.x + "," + containerPoint.y);
        
        // The coordinates of the glass pane should be the same as the underlying TSGraphJComponent
        // since both are the same size so just forward the event
        //System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  glassPanePoint=" + glassPanePoint.x + "," + glassPanePoint.y);
        Component component = this.tsgraphJComponent; // Component that should experience event
        component.dispatchEvent(new MouseEvent(component,
        		e.getID(),
                e.getWhen(),
                e.getModifiers(),
                glassPanePoint.x,
                glassPanePoint.y,
                e.getClickCount(),
                e.isPopupTrigger()));

        if (repaint) {
        	//System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  calling repaint on glass pane for x=" + glassPanePoint.x + "," + glassPanePoint.y);
            //toolkit.beep();
            this.glassPane.setPoint(glassPanePoint); // This allows
            // Decide if need intermediate data
            //glassPane.setMouseTrackerData(this.tsgraphJComponent.getMouseTrackerData(glassPane.getMouseTrackerData(),glassPane,glassPanePoint.x, glassPanePoint.y));
            this.glassPane.repaint();
        }
    }
}