package RTi.GRTS;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import RTi.GR.GRDeviceUtil;
import RTi.GR.GRJComponentDevice;
import RTi.GR.GRLimits;
import RTi.GR.GRUnits;
import RTi.GR.GRUtil;

/**
 * Glass pane to sit on top of the TSGraphJComponent instance in the TSViewGraphJFrame.
 * This is used to draw a mouse tracker overlay while letting all events pass through to underlying components
 * so that zoom box and other interactions can occur. 
 * @author sam
 *
 */
@SuppressWarnings("serial")
public class TSGraphJComponentGlassPane extends GRJComponentDevice {
	/**
	 * The main graph component.
	 */
	TSGraphJComponent tsgraphJComponent = null;
	
	/**
	 * Mouse tracker type, to control behavior of tracker.
	 * Default is none to mimic legacy behavior.
	 */
	TSGraphMouseTrackerType mouseTrackerType = TSGraphMouseTrackerType.NONE;

	/**
	 * Point for the mouse (will be drawn).
	 */
	private Point point = null;

	/**
	 * Point for the mouse for previous draw, used to optimize drawing.
	 */
	private Point pointPrev = null;

	public TSGraphJComponentGlassPane ( TSGraphJComponent tsgraphJComponent, Container contentPane ) {
		super("TSGraphJComponentGlassPane");
		initialize();
		this.tsgraphJComponent = tsgraphJComponent;
		// Set the preferred size to the same as the original component
		setPreferredSize(new Dimension(tsgraphJComponent.getWidth(),tsgraphJComponent.getHeight()));
		//Message.printStatus(2, "", "Constructed TSGraphJComponentGlassPane");
		//System.out.println("Constructed TSGraphJComponentGlassPane");
		// Set the background to transparent
        this.setBackground(new Color(0,0,0,0));
		TSGraphJComponentGlassPaneMouseListener mouseListener = new TSGraphJComponentGlassPaneMouseListener ( this, tsgraphJComponent, contentPane );
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
	}
	
	/**
	 * Get the mouse tracker type.
	 * @return the mouse tracker type.
	 */
	public TSGraphMouseTrackerType getMouseTrackerType () {
		return this.mouseTrackerType;
	}
	
	/**
	Initializes member variables.  This is simpler than the initialization of the associated TSViewGraphJComponent
	because this class does a lot less work.
	*/
	private void initialize ()
	{	// Drawing always occurs on refresh
		// If the window happens to be under some other window and needs to be redrawn when
		// moved to the front... well, that won't happen because this glass pane only
		// draws when the mouse is interacting with a window that is in the front.
		setDoubleBuffered ( false );

		// Set the general information...

		_mode = GRUtil.MODE_DRAW;
		_name = "";
		_note = "";
		_orientation = GRDeviceUtil.ORIENTATION_PORTRAIT;
		_page = 0;
		_printing = false;
		_reverse_y = true; // Java uses y going down.  This is handled properly in GRJComponentDrawingArea.scaleYData().
		_sizedrawn = -1;
		_sizeout = -1;
		_status = GRUtil.STATUS_OPEN;
		_type = 0;
		_units = GRUnits.PIXEL;	// GRJComponentDevice

		// Fill in later...
		GRLimits limits = null;
		if ( limits == null ) {
			// Use default sizes...
			_dev0x1 = 0.0;
			_dev0x2 = 1.0;
			_dev0y1 = 0.0;
			_dev0y2 = 1.0;

			_devx1 = 0.0;
			_devx2 = 1.0;
			_devy1 = 0.0;
			_devy2 = 1.0;
			_limits = new GRLimits ( _devx1, _devy1, _devx2, _devy2 );
		}

		// The size of the device will have been set in the constructor, but
		// the base class only knows how to store the data.  Force a resize here.
		//resize ( _devx1, _devy1, _devx2, _devy2 );

		// Set to null.  Wait for the derived class to set the graphics for use throughout the drawing event...

		// Set information used by the base class and other code...

		_graphics = null;

		// Set the limits to the JComponent size...
		setLimits ( getLimits ( true ) );
	}

	/**
	 * Paint the component, but not the border or children.
	 */
	protected void paintComponent ( Graphics g ) {
		//String routine = getClass().getSimpleName() + ".paintComponent";
		Graphics2D g2 = (Graphics2D)g; // Used by Swing/AWT
		setPaintGraphics(g2); // Used by the GR package
		setLimits(new GRLimits(0,0,getWidth(),getHeight())); // Ensures consistency with resizing
		//Message.printStatus(2,routine,"In paintComponent()");
		//System.out.println(routine + " In paintComponent()");
		//this.setOpaque(false);
		//g2.setColor(Color.black);
		//g2.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight());
		//System.out.println(routine + " In paintComponent - drawing line from "
		//	+ this.getWidth()/2 + "," + 0 + " " + this.getWidth()/2 + "," + this.getHeight());
		if ( this.point != null ) {
			if ( (pointPrev != null) && ((point.x != pointPrev.x) || (point.y != pointPrev.y)) ) {
				// Try doing the rendering in the main TSGraphJComponent since it has access to all the data.
				// TODO sam 2017-03-04 may move the method into this class.
				this.tsgraphJComponent.drawMouseTracker(this,g2,this.point.x, this.point.y);
			}
			this.pointPrev = point;
		}
	}
	
	/**
	 * Set the mouse tracker type.
	 * @param type the mouse tracker type.
	 */
	public void setMouseTrackerType ( TSGraphMouseTrackerType type ) {
		this.mouseTrackerType = type;
	}
	
	/**
	 * Set the point for the painted line - used during mouse motion event handling.
	 * @param p
	 */
	public void setPoint ( Point p ) {
		this.point = p;
	}
}