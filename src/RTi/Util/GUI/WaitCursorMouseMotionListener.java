package RTi.Util.GUI;

import java.awt.Component;
import java.awt.event.MouseMotionAdapter;

/**
 * MouseListener that absorbs all MouseMotion events when the component is wait-locked.
 * This is used by the JGUIUtil.setWaitCursor(true) call to track the
 * instance of listener that is temporarily added, so it can be removed after temporary use.
 * @author sam
 *
 */
public class WaitCursorMouseMotionListener extends MouseMotionAdapter {

	/**
	 * Constructor for listener - all MouseMotionEvent will be absorbed without taking any actions.
	 * @param c component listed to
	 */
	public WaitCursorMouseMotionListener ( Component c ) {
		
	}
	
}