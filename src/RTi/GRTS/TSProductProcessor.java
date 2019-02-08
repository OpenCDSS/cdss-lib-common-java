// TSProductProcessor - class to provide methods to query and process time series into output products (graphs, reports, etc.)

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

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
