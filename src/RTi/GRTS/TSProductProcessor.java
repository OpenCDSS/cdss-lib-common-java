package RTi.GRTS;

import RTi.Util.IO.PropList;

/**
The TSProductProcessor class provides methods to query and process time series
into output products (graphs, reports, etc.).  This interface is being phased in rather
than extending the TSProcessor class.
An example of implementation is as follows:
<pre>
	try {
        TSProductProcessor p = new TSProcessor ();
		p.addTSSupplier ( this );
		p.processProduct ( "C:\\tmp\\Test.tsp", new PropList("x") );
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "TSToolMainGUI.test", "Error processing the product." );
	}
</pre>
*/
public interface TSProductProcessor {

	/**
	 * Process the time series product.
	 * @param filename time series product file (*.tsp)
	 * @param props run-time properties to control processing (in addition to *.tsp properties)
	 */
	public void processTSProduct ( String filename, PropList props );
}