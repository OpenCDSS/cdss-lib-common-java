package RTi.Util.GUI;

import java.awt.Component;
import java.awt.event.KeyAdapter;

/**
 * KeyListener that absorbs all KeyEvent when the component is wait-locked.
 * This is used by the JGUIUtil.setWaitCursor(true) call to track the
 * instance of listener that is temporarily added so that it can be removed after temporary use.
 * @author sam
 *
 */
public class WaitCursorKeyListener extends KeyAdapter {

	/**
	 * Constructor for listener - all KeyEvent will be absorbed without taking any actions.
	 * @param c component listed to
	 */
	public WaitCursorKeyListener ( Component c ) {
		
	}
	
}