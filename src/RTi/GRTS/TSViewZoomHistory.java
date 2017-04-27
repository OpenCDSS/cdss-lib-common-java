package RTi.GRTS;

import java.util.ArrayList;
import java.util.List;

import RTi.GR.GRLimits;

/**
 * This class retains a history of the zoom levels for a time series graph.
 * It is primarily used to remember the horizontal (time) zoom on GRTS time series graphs.
 * Behavior is similar to a web browser.
 * See:  http://stackoverflow.com/questions/1313788/how-does-the-back-button-in-a-web-browser-work
 * <ol>
 * <li> History of zoom is retained, seeded with the initial zoom.</li>
 * <li> As new zooms occur via drawing box on the graph, new limits are saved.</li>
 * <li> Current zoom is the current position in the list.</li>
 * <li> Previous zoom is the previous.</li>
 * <li> Next zoom is the next, if a linear previous/next user action sequence has occurred.</li>
 * <li> If after going to previous a new zoom is performed, the "next history" is cleared so that using next is unambiguous.</li>
 * </ol>
 * @author sam
 *
 */
public class TSViewZoomHistory {
	
	/**
	 * The zoom limits.
	 */
	private List<GRLimits> zoomLimits = new ArrayList<GRLimits>();
	
	/**
	 * Position of the current zoom, -1 if nothing in the list.
	 */
	private int currentZoomPos = -1;
	
	/**
	 * Constructor.
	 */
	public TSViewZoomHistory () {
	}
	
	/**
	 * Add a zoom after the current zoom and make it the current.
	 */
	public void add ( GRLimits newLimits ) {
		// Because the user is adding a new zoom, clear all nexts to start the new sequence
		if ( this.currentZoomPos >= 0 ) {
			for ( int i = this.zoomLimits.size() - 1; i > currentZoomPos; i-- ) {
				this.zoomLimits.remove(i);
			}
		}
		// Now add the new limit and advance the current zoom
		this.zoomLimits.add(newLimits);
		this.currentZoomPos = this.zoomLimits.size() - 1;
	}

	/**
	 * Get the current zoom limits.
	 * Return the current limits or null if no current limits.
	 */
	public GRLimits getCurrentZoom () {
		if ( this.currentZoomPos >= 0 ) {
			return this.zoomLimits.get(this.currentZoomPos);
		}
		return null;
	}

	/**
	 * Get the next zoom limits.
	 * Return the next limits or null if no next limits.
	 */
	public GRLimits getNextZoom () {
		if ( this.currentZoomPos >= 0 ) {
			int nextZoomPos = currentZoomPos + 1;
			if ( (this.zoomLimits.size() - 1) >= nextZoomPos ) {
				return this.zoomLimits.get(nextZoomPos);
			}
		}
		return null;
	}
	
	/**
	 * Get the previous zoom limits.
	 * Return the previous limits or null if no previous limits.
	 */
	public GRLimits getPreviousZoom () {
		if ( this.currentZoomPos >= 1 ) {
			int previousZoomPos = currentZoomPos - 1;
			return this.zoomLimits.get(previousZoomPos);
		}
		return null;
	}
	
	/**
	 * Go to the next limits and return the limits.
	 * @return the next limits or null if there were no next limits.
	 */
	public GRLimits next () {
		if ( this.currentZoomPos >= 1 ) {
			int nextZoomPos = currentZoomPos + 1;
			if ( (this.zoomLimits.size() - 1) >= nextZoomPos ) {
				++currentZoomPos;
				return this.zoomLimits.get(nextZoomPos);
			}
		}
		return null;
	}
	
	/**
	 * Go to the previous zoom limits.
	 * Return the previous limits or null if no previous limits.
	 */
	public GRLimits previous () {
		if ( this.currentZoomPos >= 1 ) {
			int previousZoomPos = currentZoomPos - 1;
			--currentZoomPos;
			return this.zoomLimits.get(previousZoomPos);
		}
		return null;
	}
}