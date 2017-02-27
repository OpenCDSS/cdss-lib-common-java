package RTi.GRTS;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

import RTi.Util.Message.Message;

/**
 * Glass pane to sit on top of the TSGraphJComponent instance in the TSViewGraphJFrame.
 * This is used to draw a mouse tracker overlay while letting all events pass through to underlying components
 * so that zoom box and other interactions can occur. 
 * @author sam
 *
 */
public class TSGraphJComponentGlassPane extends JComponent {
	
	/**
	 * Point for the mouse (will be drawn).
	 */
	private Point point = null;
	/**
	 * Point for the mouse for previous draw, used to optimize drawing.
	 */
	private Point pointPrev = null;

	public TSGraphJComponentGlassPane ( TSGraphJComponent tsgraphJComponent, Container contentPane ) {
		setName("TSGrapJComponentGlassPane");
		// Set the preferred size to the same as the original component
		setPreferredSize(new Dimension(tsgraphJComponent.getWidth(),tsgraphJComponent.getHeight()));
		Message.printStatus(2, "", "Constructed TSGraphJComponentGlassPane");
		System.out.println("Constructed TSGraphJComponentGlassPane");
		// Set the background to transparent
        this.setBackground(new Color(0,0,0,0));
		TSGraphJComponentGlassMouseListener mouseListener = new TSGraphJComponentGlassMouseListener ( this, tsgraphJComponent, contentPane );
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
	}

	// Start events for MouseMotionListener
	/*
    public void mouseDragged(MouseEvent e) {
        repaint();
    }
    
    public void mouseMoved(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mouseMoved";
    	Message.printStatus(2, "", "Mouse move detected in glass pane.");
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    	// Save the point for the mouse motion
    	this.point = e.getPoint();
    	// Trigger repaint on glass pane, which will draw a vertical line and label the time series
        this.repaint();
    }
	// End events for MouseMotionListener

	// Start events for MouseListener
    
    public void mouseClicked(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mouseClicked";
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    }
    
    public void mouseEntered(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mouseEntered";
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    }
    
    public void mouseExited(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mouseExited";
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    }
    
    public void mousePressed(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mousePressed";
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    }
    
    public void mouseReleased(MouseEvent e) {
    	String routine = getClass().getSimpleName() + ".mouseReleased";
    	System.out.println("In " + routine + " x=" + e.getX() + "," + e.getY() );
    }
    */
    
	// End events for MouseListener
	
	/**
	 * This method will cause all events to be disabled on the component.
	 * Use to allow events to drop through to the main drawing component.
	 * @return false always
	 */
	/*
	public boolean contains ( int x, int y ) {
		String routine = getClass().getSimpleName() + ".contains";
		Message.printStatus(2,routine,"In contains()");
		return false;
	}*/
    
	/**
	 * Paint the component, but not the border or children.
	 */
	protected void paintComponent ( Graphics g ) {
		Graphics2D g2 = (Graphics2D)g;
		String routine = getClass().getSimpleName() + ".paintComponent";
		Message.printStatus(2,routine,"In paintComponent()");
		System.out.println(routine + " In paintComponent()");
		//this.setOpaque(false);
		g2.setColor(Color.black);
		g2.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight());
		System.out.println(routine + " In paintComponent - drawing line from "
			+ this.getWidth()/2 + "," + 0 + " " + this.getWidth()/2 + "," + this.getHeight());
		if ( this.point != null ) {
			if ( (pointPrev != null) && ((point.x != pointPrev.x) || (point.y != pointPrev.y)) ) {
				System.out.println(routine + " In paintComponent - drawing line");
				g2.setColor(Color.black);
				g2.drawLine(this.point.x, 0, this.point.x, this.getHeight());
				g2.drawString("("+point.x+","+point.y+")", point.x, point.y);
			}
			this.pointPrev = point;
		}
	}
	
	// TODO sam 207-02-24 remove this since paintComponent is preferred
	/**
	 * Paint the component, including the border or children.
	 *//*
	public void paint ( Graphics g ) {
		String routine = getClass().getSimpleName() + ".paint";
		Message.printStatus(2,routine,"In paint()");
		System.out.println(routine + " In paint()");
		g.setColor(Color.magenta);
		g.fillRect(0, 0, getWidth(), getHeight());
		if ( this.point != null ) {
			g.setColor(Color.cyan);
			g.drawLine(this.point.x, 0, this.point.x, this.getHeight());
		}
	}*/
	
	/**
	 * Set the point for the painted line - used during mouse motion event handling.
	 * @param p
	 */
	public void setPoint ( Point p ) {
		this.point = p;
	}
}