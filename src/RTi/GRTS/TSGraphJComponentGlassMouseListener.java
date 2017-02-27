package RTi.GRTS;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** 
 * Listen for all events that are of interest to the underlying TSGraphJComponent and redispatch.
 */
public class TSGraphJComponentGlassMouseListener implements MouseListener, MouseMotionListener {
    Toolkit toolkit;
    TSGraphJComponentGlassPane glassPane;
    TSGraphJComponent tsgraphJComponent;
    Container contentPane;
    boolean inDrag = false;

    public TSGraphJComponentGlassMouseListener(TSGraphJComponentGlassPane glassPane,
    	TSGraphJComponent tsgraphJComponent, Container contentPane) {
        toolkit = Toolkit.getDefaultToolkit();
        this.glassPane = glassPane;
        this.tsgraphJComponent = tsgraphJComponent;
        this.contentPane = contentPane;
    }

    public void mouseMoved(MouseEvent e) {
    	System.out.println("In TSGraphJComponentGlassPaneMouseListener.mouseMoved:  glassPanePoint=" + e.getX() + "," + e.getY());
    	// Cause a redraw of the mouse tracker
        redispatchMouseEvent(e, true);
    }

    /*
     * Forward dragging to underlying TSGraphJComponent.
     */
    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e, true);
        inDrag = false;
    }

    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
        Point glassPanePoint = e.getPoint();
        System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  originating mouse event component is " + e.getComponent().getName());
        System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  glassPanePoint=" + glassPanePoint.x + "," + glassPanePoint.y);
        //Container container = contentPane;
        // Convert the point in the glass pane to the JFrame content pane coordinates
        //Point containerPoint = SwingUtilities.convertPoint(
        //                                glassPane,
        //                                glassPanePoint, 
        //                                contentPane);
        //System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  containerPoint=" + containerPoint.x + "," + containerPoint.y);
        
        // The coordinates of the glass pane should be the same as the underlying TSGraphJComponent
        // since both are the same size so just forward the event
        System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  glassPanePoint=" + glassPanePoint.x + "," + glassPanePoint.y);
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
        	System.out.println("In TSGraphJComponentGlassPaneMouseListener.redispatchMouseEvent:  calling repaint on glass pane for x=" + glassPanePoint.x + "," + glassPanePoint.y);
            //toolkit.beep();
            glassPane.setPoint(glassPanePoint);
            glassPane.repaint();
        }
    }
}