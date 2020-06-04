// TSProcessor - provides methods to query and process time series into output products (graphs, reports, etc.)

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

import java.awt.event.WindowListener;
import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSSupplier;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.TS.DateValueTS;

import java.awt.image.BufferedImage;

/**
The TSProcessor class provides methods to query and process time series
into output products (graphs, reports, etc.).
An example of implementation is as follows:
<pre>
	try {
        TSProcessor p = new TSProcessor ();
		p.addTSSupplier ( this );
		p.processProduct ( "C:\\tmp\\Test.tsp", new PropList("x") );
	}
	catch ( Exception e ) {
		Message.printWarning ( 1, "TSToolMainGUI.test", "Error processing the product." );
	}
</pre>
*/
public class TSProcessor
{

/**
TSSuppliers to use when reading time series.
*/
private TSSupplier[] _suppliers = null;

/**
The last TSViewJFrame created and opened when displaying a graph.
*/
private TSViewJFrame __lastTSViewJFrame = null;

/**
A single WindowListener that can be associated with the TSViewFrame.  This is
being tested to determine whether an application like TSTool can detect a
TSViewJFrame closing and close the application.
*/
private WindowListener _tsview_window_listener = null;

public TSProcessor ()
{
}

/**
Add a time series supplier.  Suppliers are used to query time series based on a
time series identifier.
@param supplier TSSupplier to use with the TSProcessor.
*/
public void addTSSupplier ( TSSupplier supplier )
{	// Use arrays to make a little simpler than lists to use later...
	if ( supplier != null ) {
		// Resize the supplier array...
		if ( _suppliers == null ) {
			_suppliers = new TSSupplier[1];
			_suppliers[0] = supplier;
		}
		else {
			// Need to resize and transfer the list...
			int size = _suppliers.length;
			TSSupplier [] newsuppliers = new TSSupplier[size + 1];
			for ( int i = 0; i < size; i++ ) {
				newsuppliers[i] = _suppliers[i];
			}
			_suppliers = newsuppliers;
			_suppliers[size] = supplier;
			newsuppliers = null;
		}
	}
}

/**
Add a WindowListener for TSViewFrame instances that are created.  Currently
only one listener can be set.
@param listener WindowListener to listen to TSViewFrame WindowEvents.
*/
public void addTSViewWindowListener ( WindowListener listener )
{	_tsview_window_listener = listener;
}

/**
Returns the last TSViewJFrame created when displaying a graph.
@return the last TSViewJFrame created when displaying a graph.
*/
public TSViewJFrame getLastTSViewJFrame() {
	return __lastTSViewJFrame;
}

/**
Process a graph product.  Time series that are indicated in the time series
product are collected by matching in memory or reading time series.  This is
done here so that low-level graph code can get a list of time series and not
need to do any collecting itself.
@param tsproduct Time series product definition.
@exception Exception if the product cannot be processed (e.g., the graph cannot
be created due to a lack of data).
*/
private void processGraphProduct ( TSProduct tsproduct )
throws Exception
{	
	String routine = "TSProcessor.processGraphProduct";
	List<TS> tslist = new Vector<TS>(10);
	TS ts = null;
	// Loop through the sub-products (graphs on page) and get the time series to
	// support the graph.
	String tsid;
	String tsalias;
	String prop_value = null;
	DateTime date1 = null;
	DateTime date2 = null;
	Message.printStatus ( 2, routine, "Processing product" );
	int nsubs = tsproduct.getNumSubProducts();
	prop_value = tsproduct.getLayeredPropValue("IsTemplate", -1, -1 );
	boolean is_template = false;
	if ( (prop_value != null) && prop_value.equalsIgnoreCase("true") ) {
		is_template = true;
		Message.printStatus ( 2, routine, "Processing template." );
	}
	for ( int isub = 0; isub < nsubs ;isub++ ) {
		Message.printStatus ( 2, routine, "Reading time series for subproduct [" + isub + "]" );
		// New...
		prop_value = tsproduct.getLayeredPropValue("IsEnabled", isub, -1 );
		// Old...
		if ( prop_value == null ) {
			prop_value = tsproduct.getLayeredPropValue("Enabled", isub, -1 );
		}
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
			continue;
		}
		// Loop through the time series in the subproduct
		for ( int i = 0; ; i++ ) {
			// New version...
			prop_value = tsproduct.getLayeredPropValue ( "IsEnabled", isub, i );
			// Old version...
			if ( prop_value == null ) {
				prop_value = tsproduct.getLayeredPropValue ( "Enabled", isub, i );
			}
			if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
				// Add a null time series...
				tslist.add ( null );
				continue;
			}
			prop_value = tsproduct.getLayeredPropValue ( "PeriodStart", isub, i );
			if ( prop_value != null ) {
				try {
                    date1 = DateTime.parse ( prop_value );
				}
				catch ( Exception e ) {
					date1 = null;
				}
			}
			prop_value = tsproduct.getLayeredPropValue ( "PeriodEnd", isub, i );
			if ( prop_value != null ) {
				try {
                    date2 = DateTime.parse ( prop_value );
				}
				catch ( Exception e ) {
					date2 = null;
				}
			}
			// Make sure this is last since the TSID is used in the following readTimeSeries() call...
			if ( is_template ) {
				tsid = tsproduct.getLayeredPropValue ( "TemplateTSID", isub, i, false );
			}
			else {
                // Just get the normal property...
				tsid = tsproduct.getLayeredPropValue ( "TSID", isub, i, false );
			}
			// Make sure we have both or none...
			if ( (date1 == null) || (date2 == null) ) {
				date1 = null;
				date2 = null;
			}
			// First try to read the time series using the "TSAlias".  This normally will only return non-null
			// for something like TSTool where the time series may be in memory.
			tsalias = tsproduct.getLayeredPropValue ( "TSAlias", isub, i, false );
	        if ( (tsid == null) && (tsalias == null) ) {
	            // No more time series in the product file...
	            break;
	        }
			if ( !is_template && (tsalias != null) && !tsalias.trim().equals("") ) {
				// Have the "TSAlias" property so use it instead of the TSID...
				Message.printStatus ( 3, routine, "Requesting TS read from TS suppliers using alias \"" + tsalias + "\"." );
				try {
                    ts = readTimeSeries ( tsalias.trim(), date1, date2,	null, true );
				}
				catch ( Exception e ) {
					// Always add a time series because visual properties are going to be
					// tied to the position of the time series.
					Message.printWarning ( 3, routine, "Error getting time series \"" +	tsalias.trim() + "\" - setting to null." );
					ts = null;
				}
			}
			else {
                // Don't have a "TSAlias" so try to read the time series using the full "TSID"...
				Message.printStatus ( 2, routine, "Requesting TS read from TS suppliers using TSID \"" + tsid + "\".");
				try {
                    ts = readTimeSeries ( tsid.trim(), date1, date2, null, true );
				}
				catch ( Exception e ) {
					// Always add a time series because visual properties are going to be
					// tied to the position of the time series.
				    Message.printWarning ( 2, routine,
				        "Error getting time series \"" + tsid.trim() + "\".  Setting to null." );
					ts = null;
				}
				if ( ts == null  ) {
					// Logic place-holder
				}
				else if ( is_template ) {
					// Non-null TS.  The TemplateTSID was requested but now the actual TSID needs to be set...
					tsproduct.setPropValue(	"TSID", ts.getIdentifier().toString(), isub, i );
				}
			}
			// In any case add the time series, even if null.
			if ( ts == null ) {
			    Message.printStatus(2, routine, "Adding null time series for graph." );
			}
			else {
			    Message.printStatus(2, routine, "Adding time series for graph:  " + ts.getIdentifier() + " period " +
			            ts.getDate1() + " to " + ts.getDate2() );
			}
			tslist.add ( ts );
		}
	}

	// Now add the time series to the TSProduct.  This simply provides the time series. They will be looked
    // up as needed when the TSGraph is created.

	tsproduct.setTSList ( tslist );

	// Now create the graph.  For now use the PropList associated with the
	// TSProduct.  The use of a frame seems to be necessary to get this to
	// work (tried lots of other things including just declaring a TSGraph),
	// but could not get the combination of Graphics, Image, etc. to work.
	// The following is essentially a serialized TSP file using dot-notation
	PropList tsviewprops = tsproduct.getPropList();
	//Message.printStatus(2, routine, "Graph properties=" + tsviewprops);

	String graph_file =	tsproduct.getLayeredPropValue ( "OutputFile", -1, -1 );
	/* TODO SAM 2008-11-19 Don't think this is needed
	if ( graph_file == null ) {
		if ( IOUtil.isUNIXMachine() ) {
			graph_file = "/tmp/tmp.png";
		}
		else {
            graph_file = "C:\\TEMP\\tmp.png";
		}
	}
	*/
	String outputProductFile = tsproduct.getLayeredPropValue ( "OutputProductFile", -1, -1 );
	String outputProductFormat = tsproduct.getLayeredPropValue ( "OutputProductFormat", -1, -1 );
	if ( (outputProductFormat == null) || outputProductFormat.isEmpty() ) {
		outputProductFormat = "JSON"; // Default
	}
	String preview_output = tsproduct.getLayeredPropValue ( "PreviewOutput", -1, -1 );
	try {
	    // Draw to the image file first in case because the on-screen display throws
	    // an exception for missing data and for troubleshooting it would be good to
	    // see the image.
		// TODO SAM 2007-06-22 Need to figure out how to combine on-screen
		// drawing with file to do one draw, if possible.
		if ( (graph_file != null) && (graph_file.length() > 0) ){
			// Create an in memory image and let the TSGraphJComponent draw to it.  Use properties since
			// that was what was done before...
			// Image image = f.createImage(width,height);
			// Image ii = f.createImage(width, height);
			// Make this the same size as the TSGraph defaults and then reset with the properties...
			int width = 400;
			int height = 400;
			prop_value = tsproduct.getLayeredPropValue ( "TotalWidth", -1, -1 );
			if ( prop_value != null ) {
				width = StringUtil.atoi(prop_value);
			}
			prop_value = tsproduct.getLayeredPropValue ( "TotalHeight", -1, -1 );
			if ( prop_value != null ) {
				height = StringUtil.atoi(prop_value);
			}
			BufferedImage image = new BufferedImage (width, height,	BufferedImage.TYPE_3BYTE_BGR);

			tsviewprops.set(new Prop("Image", image, "") );
			TSGraphJComponent graph = new TSGraphJComponent ( null, tsproduct, tsviewprops );
			if ( graph.needToClose() ) {
				throw new Exception ( "Graph was automatically closed due to data problem." );
			}
			graph.paint ( image.getGraphics() );
			// Figure out the output file name for the product...
			Message.printStatus ( 2, routine, "Saving graph to image file \"" + graph_file + "\"" );
			graph.saveAsFile ( graph_file );
			Message.printStatus ( 2, "", "Done" );
			graph_file = null;
			graph = null;
			image = null;
		}
		// Put the on-screen graph second so that above image can be
		// created first for troubleshooting
        if ( (preview_output != null) && preview_output.equalsIgnoreCase("true") ) {
            // Create a TSViewJFrame (an output file can still be created below)...
            TSViewJFrame tsview = new TSViewJFrame ( tsproduct );
            if ( tsview.needToCloseGraph() ) {
                throw new Exception ( "Graph was automatically closed due to data problem." );
            }
            __lastTSViewJFrame = tsview;
            // Put this in to test letting TSTool shut down when
            // a single TSView closes (and no main GUI is visible)..
            if ( _tsview_window_listener != null ) {
                tsview.addWindowListener ( _tsview_window_listener );
            }
        }
		// Create the output product file if requested.
		// - this is the same as "Save" in the TSView_Graph window
        // - do this after the product is processed because many properties will be set internally
		if ( (outputProductFile != null) && !outputProductFile.isEmpty() ) {
			if ( outputProductFormat.equalsIgnoreCase("json") ) {
				tsproduct.writeJsonFile(outputProductFile, true);
			}
			else {
				tsproduct.writeFile(outputProductFile, true);
			}
		}
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, "TSProcessor.processGraphProduct", "Unable to create graph." );
		Message.printWarning ( 3, "TSProcessor.processGraphProduct", e);
		// Throw a new error...
		throw new Exception ( "Unable to create graph (" + e + ")", e );
	}
}

/**
Process a time series product file.
@param filename Name of time series product file.
@param override_props Properties to override the properties in the product file
(e.g., to set the period for the plot dynamically).
@exception Exception if the product cannot be processed (e.g., the graph cannot
be created due to a lack of data).
*/
public void processProduct ( String filename, PropList override_props ) throws Exception
{	Message.printStatus ( 2, "", "Processing time series product \"" + filename + "\"" );
	TSProduct tsproduct = new TSProduct (filename, override_props);	
	processProduct ( tsproduct );
}

/**
Process a time series product.
@param tsproduct Time series product definition.
@exception Exception if the product cannot be processed (e.g., the graph cannot
be created due to a lack of data).
*/
public void processProduct ( TSProduct tsproduct )
throws Exception
{	String prop_value = null;
	// Determine whether the product should be processed...
	// New version...
	prop_value = tsproduct.getLayeredPropValue ( "IsEnabled", -1, -1 );
	if ( prop_value == null ) {
		// Old version...
		prop_value = tsproduct.getLayeredPropValue ("Enabled", -1, -1 );
	}
	if ( (prop_value == null) || prop_value.equalsIgnoreCase("true") ) {
		// Determine if a graph or report product is being generated...
		prop_value = tsproduct.getLayeredPropValue ( "ProductType", -1, -1 );

		if ( (prop_value != null) && prop_value.equalsIgnoreCase ( "Report" ) ) {
			processReportProduct ( tsproduct );
		}
		else if ( (prop_value == null) || prop_value.equalsIgnoreCase("Graph") ) { // Default if no product type
			processGraphProduct ( tsproduct );
		}
	}
}

/**
Processes a time series product of type "Report" using its given properties.
Each subproduct in the product is processed, and will have an outfile
associated with it, in order to put time series of different interval in
separate files. The only supported ReportType is DateValue.
@param tsproduct Time series product.
*/
public void processReportProduct( TSProduct tsproduct ) throws Exception
{
	String routine = "TSProcessor.processReportProduct";
	String tsid;
	String tsalias;
	DateTime date1 = null;
	DateTime date2 = null;
	boolean is_template = false;

	// loop through each subproduct and print out the corresponding files 
	int nsubs = tsproduct.getNumSubProducts();	 
	for ( int isub = 0; isub < nsubs; isub++ ) {

		String fname = null;
		String prop_value = null;
		String report_type = null;
		List<TS> tslist = new Vector<TS>();

		// get file name for subproduct
		// if there isn't one set then set a temp file name
		fname = tsproduct.getLayeredPropValue ( "OutputFile", isub, -1);
		if ( fname == null ) {
		  if ( IOUtil.isUNIXMachine() ) {
			fname = "/tmp/tmp_report_" + isub;
		  }
		  else {
              fname = "C:\\TEMP\\tmp_report_" + isub;
		  }	
		}
				
		// Set report type for subproduct
		report_type = tsproduct.getLayeredPropValue( "ReportType", isub, -1);

		Message.printStatus ( 2, routine, "Reading time series for subproduct [" + isub + "]" );
		// New...
		prop_value = tsproduct.getLayeredPropValue("IsEnabled", isub, -1 );
		// Old...
		if ( prop_value == null ) {
			prop_value = tsproduct.getLayeredPropValue("Enabled", isub, -1 );
		}
		if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
			continue;
		}

		// Loop through the time series in the subproduct
		for ( int i = 0; ; i++ ) {

			TS ts = null;
			// New version...
			prop_value = tsproduct.getLayeredPropValue ( "IsEnabled", isub, i );
			// Old version...
			if ( prop_value == null ) {
				prop_value = tsproduct.getLayeredPropValue ( "Enabled", isub, i );
			}
			if ( (prop_value != null) && prop_value.equalsIgnoreCase("false") ) {
				// Add a null time series...
				tslist.add ( (TS)null );
				continue;
			}
			prop_value = tsproduct.getLayeredPropValue ( "PeriodStart", isub, i );
			if ( prop_value != null ) {
				try {
                    date1 = DateTime.parse ( prop_value );
				}
				catch ( Exception e ) {
					date1 = null;
				}
			}
			prop_value = tsproduct.getLayeredPropValue ( "PeriodEnd", isub, i );
			if ( prop_value != null ) {
				try {
                    date2 = DateTime.parse ( prop_value );
				}
				catch ( Exception e ) {
					date2 = null;
				}
			}
			// Make sure this is last since the TSID is used in the following readTimeSeries() call...
			if ( is_template ) {
				tsid = tsproduct.getLayeredPropValue ( "TemplateTSID", isub, i, false );
			}
			else {
                // Just get the normal property...
				tsid = tsproduct.getLayeredPropValue ( "TSID", isub, i, false );
			}
			if ( tsid == null ) {
				// No more time series...
				break;
			}
			// Make sure we have both or none...
			if ( (date1 == null) || (date2 == null) ) {
				date1 = null;
				date2 = null;
			}
			// First try to read the time series using the
			// "TSAlias".  This normally will only return non-null
			// for something like TSTool where the time series may
			// be in memory.
			tsalias = tsproduct.getLayeredPropValue ( "TSAlias", isub, i, false );
			if ( !is_template && (tsalias != null) && !tsalias.trim().equals("") ) {
				// Have the property so use the TSAlias instead of the TSID...
				Message.printStatus ( 2, routine, "Reading TSAlias \"" + tsalias + "\" from TS suppliers." );
				try {
                    ts = readTimeSeries ( tsalias.trim(), date1, date2,	null, true );
				}
				catch ( Exception e ) {
					// Always add a time series because visual properties are going to be
					// tied to the position of the time series.
					Message.printWarning ( 2, routine, "Error getting time series \"" +	tsalias.trim() + "\"" );
					ts = null;
				}
			}
			else {
                // Don't have a "TSAlias" so try to read the time series using the full "TSID"...
				Message.printStatus ( 2, routine, "Reading TSID \"" + tsid + "\" from TS suppliers.");
				try {
                    ts = readTimeSeries ( tsid.trim(), date1, date2, null, true );
				}
				catch ( Exception e ) {
					// Always add a time series because visual properties are going to be
					// tied to the position of the time series.
					ts = null;
				}
				if ( ts == null  ) {
					Message.printWarning ( 2, routine, "Error getting time series \"" +	tsid.trim() + "\".  Setting to null." );
				}
				else if ( is_template ) {
					// Non-null TS.  The TemplateTSID was requested but now the actual TSID needs to be set...
					tsproduct.setPropValue(	"TSID", ts.getIdentifier().toString(),isub, i );
				}
			}
			
			tslist.add ( ts );
		}

		// Done adding all time series for that subproduct write output for this subproduct to a file

		if(report_type.equalsIgnoreCase("DateValue")) {

			DateValueTS.writeTimeSeriesList(tslist, fname);
 		}
	}
}

/**
Read a time series using the time series suppliers.  The first supplier to
return a time series is assumed to be the correct supplier.
@param tsident TSIdent string indicating the time series to read.
@param date1 Starting date of read, or null.
@param date2 Ending date of read, or null.
@param req_units Requested units.
@param read_data Indicates whether to read data (false is header only).
@return a time series corresponding to the tsident.
@exception Exception if no time series can be found.
*/
public TS readTimeSeries ( String tsident, DateTime date1, DateTime date2,
				String req_units, boolean read_data )
throws Exception
{	String routine = "TSProcessor.readTimeSeries";
	int size = 0;
	if ( _suppliers != null ) {
		size = _suppliers.length;
	}
	TS ts = null;
	for ( int i = 0; i < size; i++ ) {
        String supplier_name = _suppliers[i].getTSSupplierName();
		Message.printStatus ( 2, routine, "Trying to get \"" + tsident +
			"\" from TSSupplier \"" + supplier_name + "\"" );
		try {
            ts = _suppliers[i].readTimeSeries (	tsident, date1, date2, (String)null, true );
		}
		catch ( Exception e ) {
			Message.printWarning ( 2, routine, "Error reading/finding time series for supplier \"" +
                    supplier_name + "\".  Ignoring and trying other suppliers (if available)." );
			Message.printWarning ( 2, routine, e );
			continue;
		}
		if ( ts == null ) {
			Message.printStatus ( 2, routine, "Did not find TS \"" + tsident +
			"\" using TSSupplier \"" + supplier_name + "\".  Ignoring and trying other suppliers (if available)." );
		}
		else {
            // Found a time series so assume it is the one that is needed...
			Message.printStatus ( 2, routine, "Supplier \"" + supplier_name + "\": found TS \"" + tsident +
                    "\" (alias \"" + ts.getAlias() + "\" period " + ts.getDate1() + " to " + ts.getDate2() + ")" );
			return ts;
		}
	}
	throw new Exception ( "Unable to get time series \"" + tsident + "\" from " + size + " TSSupplier(s).");
}

}
