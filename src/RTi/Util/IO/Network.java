// ----------------------------------------------------------------------------
// Network - a representation of a branching network
// ----------------------------------------------------------------------------
// History:
//
// 2003-07-29	Steven A. Malers,	Initial version - copy and modify
//		Riverside Technology,	HBNodeNetwork to be more generic.
//		inc.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.lang.Math;
//import java.lang.String;
//import java.lang.Long;
import java.util.List;
import java.util.Vector;

//import RTi.GR.GR;
//import RTi.GR.GRAspect;
//import RTi.GR.GRColor;
//import RTi.GR.GRDevice;
//import RTi.GR.GRException;
//import RTi.GR.GRDrawingArea;
//import RTi.GR.GRLimits;
//import RTi.GR.GRText;
//import RTi.GR.GRUnits;

//import RTi.TS.TS;

//import RTi.Util.IO.IOUtil;
//import RTi.Util.IO.PropList;
//import RTi.Util.Math.MathUtil;
//import RTi.Util.Message.Message;
//import RTi.Util.String.StringUtil;
//import RTi.Util.Time.TimeUtil;
//import RTi.Util.Time.StopWatch;


/**
The Network class maintains a list of NetworkFeature.  The network can be
comprised of Node and Link objects (each of which is derived from
NetworkFeature).
*/
public class Network extends Object
{

/**
Upstream and downstream positions within the network.
*/
public static final int POSITION_ABSOLUTE	= 1;	// go to top or bottom
							// of system
public static final int POSITION_COMPUTATIONAL	= 2;	// find the next
							// upstream or
							// downstream
							// computation-wise
public static final int POSITION_REACH		= 3;	// find the most
							// upstream or
							// downstream in a reach
public static final int POSITION_RELATIVE	= 4;	// find the next
							// upstream or
							// downstream in the
							// same reach - really
							// only applies to going
							// downstream
public static final int POSITION_REACH_NEXT	= 5;	// Get the next node
							// in the reach.

public static final int POSITION_UPSTREAM	= 1;	// Flags indicating how
public static final int POSITION_DOWNSTREAM	= 2;	// to output the
							// network.


// Data members...

/**
The list of features in the network.  This is all features.  The connectivity
of the features is maintained by references in each feature.
*/
protected List _feature_Vector;

// Data for graphics...

/* REVISIT - maybe put in the GeoView NetworkLayer
private String	_font;
private double	_fontsize;
private int	_label_type;
private double	_legend_x;
private double	_legend_y;
private double	_legend_dx;
private double	_legend_dy;
private double	_node_diam;
private double	_title_x;
private double	_title_y;
private String	_title;
static Vector	_labels = new Vector(10,10);
*/

/**
Construct a new Network, with no data.
*/
public Network ()
{	initialize ();
}

/**
Add a feature to the network.  The connectivity of features is expected to be
enforced elsewhere.
@param feature A network feature to add.
*/
public void addFeature ( NetworkFeature feature )
{	if ( feature != null ) {
		_feature_Vector.add ( feature );
	}
}

/**
Store a label to be drawn on the plot.
@param x X-coordinate of label.
@param y Y-coordinate of label.
@param flag Text orientation.
@param label Text for label.
*/
/* REVISIT - need to add GRShape as annotations - do here or as part of
the GeoView?
protected void addLabel (	double x, double y, double size, int flag,
				String label )
{	String	routine = "addLabel";

	if ( Message.isDebugOn ) {
		Message.printDebug ( 2, routine,
		"Storing label \"" + label + "\" at " + x + " " + y );
	}
	_labels.addElement ( new Label ( x, y, size, flag, label ) );
}
*/

/**
Draw label on plot.
@param da GRDrawing area to draw to.
@param node Node of interest.
@param label Label for symbol.
@param diam Diameter for symbol (used for positioning).
*/
/* REVISIT - put in GeoView code?
protected void drawLabel (	GRDrawingArea da, HBNode node, String label,
				double diam )
{	int	dir;

	if ( node.getType() == HBNode.NODE_TYPE_RES ) {
		dir = node.getLabelDirection()%10;
	}
	else {	dir = node.getLabelDirection();
	}

	if ( dir == 1 ) {
		// Label above node...
		if ( _auto_label )
			GR.drawText (	da, label, node.getX(),
				(node.getY() + diam*.6), node.getLabelAngle(),
				GRText.BOTTOM|GRText.LEFT );
		else
			GR.drawText (	da, label, node.getX(),
				(node.getY() + diam*.6), 0.0,
				GRText.BOTTOM|GRText.CENTER_X );
	}
	else if ( dir == 2 ) {
		// Label below node...
		if ( _auto_label )
			GR.drawText (	da, label, node.getX(),
				(node.getY() + .6*diam), node.getLabelAngle(),
				GRText.BOTTOM|GRText.LEFT );
		else
			GR.drawText (	da, label, node.getX(),
				(node.getY() - .6*diam), 0.0,
				GRText.TOP|GRText.CENTER_X );
	}
	else if ( dir == 3 ) {
		// Label to left of node...
		GR.drawText (	da, label, (node.getX() - diam*.6),
				node.getY(), 0.0,
				GRText.RIGHT|GRText.CENTER_Y );
	}
	else if ( dir == 4 ) {
		// Label to right of node...
		GR.drawText (	da, label, (node.getX() + diam*.6),
				node.getY(), 0.0,
				GRText.LEFT|GRText.CENTER_Y );
	}
}

/**
Draw legend on plot.
@param da GRDrawing area to draw to.
*/
/* REVISIT
protected void drawLegend ( GRDrawingArea da )
{	String	label, routine = "HBNodeNetwork.drawLegend", stime, user;
	double	fontht_pts, x = _legend_x, y = _legend_y;
	HBNode	node = new HBNode ();
	int	lt;

	fontht_pts = da.scaleYData ( _fontsize ) - da.scaleYData ( 0.0);
	GR.setFont ( da, _font, fontht_pts );

	stime = TimeUtil.getSystemTimeString ( );
	user = IOUtil.getProgramUser ();
	label =  "(" + user + ", " + stime + ")";
	GR.drawText ( da, label, x, y, 0.0, GRText.LEFT|GRText.BOTTOM );

	// Figure out what the label type is...

	y += _legend_dy;
	if ( _label_type == LABEL_NODES_AREA_PRECIP ) {
		label = "Node labels are area*precip";
	}
	else if ( _label_type == LABEL_NODES_COMMONID ) {
		label = "Node labels are common identifiers";
	}
	else if ( _label_type == LABEL_NODES_NAME ) {
		label = "Node labels are names.";
	}
	else if ( _label_type == LABEL_NODES_PF ) {
		label = "Node labels are proration factors";
	}
	else if ( _label_type == LABEL_NODES_RIVERNODE ) {
		label = "Node labels are river node identifiers";
	}
	else if ( _label_type == LABEL_NODES_WATER ) {
		label = "Node labels are area*precip product";
	}
	else {	label = "Node labels are short identifiers";
	}
	GR.drawText ( da, label, x, y, 0.0, GRText.LEFT|GRText.BOTTOM );

	node.setLabelDirection( 4 );
	node.setX(x);
	node.setLabelAngle ( 0 );

	node.setY( y + _legend_dy );
	node.setType( HBNode.NODE_TYPE_OTHER );
	drawOtherNodeSymbol ( da, node, "Other", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Other/Baseflow",
	_node_diam*1.3, false );
	drawOtherNodeSymbol ( da, node, "Other/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_DIV );
	drawDemandNodeSymbol ( da, node, "Diversion", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Diversion/Baseflow",
	_node_diam*1.3, false );
	drawDemandNodeSymbol ( da, node, "Diversion/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_WELL );
	drawDemandNodeSymbol ( da, node, "Well", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Well/Baseflow",
	_node_diam*1.3, false );
	drawDemandNodeSymbol ( da, node, "Well/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_DIV_AND_WELL );
	drawDemandNodeSymbol ( da, node, "Diversion+Well(s)", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Diversion+Well(s)/Baseflow",
	_node_diam*1.3, false );
	drawDemandNodeSymbol ( da, node, "Diversion+Well(s)/Baseflow",
			_node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY( node.getY() + _legend_dy);
	node.setType( HBNode.NODE_TYPE_IMPORT );
	drawDemandNodeSymbol ( da, node, "Import", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Import/Baseflow",
	_node_diam*1.3, false );
	drawDemandNodeSymbol ( da, node, "Import/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_ISF );
	drawISFNodeSymbol ( da, node, "Instream Flow", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Instream Flow/Baseflow",
	_node_diam*1.3, false );
	drawISFNodeSymbol ( da, node, "Instream Flow/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );

	node.setY ( node.getY() + _legend_dy );
	node.setLabelDirection ( 44 );
	node.setType( HBNode.NODE_TYPE_RES );
	drawReservoirNodeSymbol ( da, node, "Reservoir", _node_diam );
	node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Reservoir/Baseflow",
	_node_diam*1.3, false );
	drawReservoirNodeSymbol ( da, node, "Reservoir/Baseflow", _node_diam );
	node.setX( node.getX() - _legend_dx );
	node.setLabelDirection ( 4 );

	node.setY( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_FLOW );
	drawStreamflowNodeSymbol ( da, node, "Streamflow Gage", _node_diam );
		node.setX( node.getX() + _legend_dx );
	drawBaseflowNodeSymbol ( da, node, "Streamflow Gage/Baseflow",
		_node_diam*1.3, false);
	drawStreamflowNodeSymbol ( da, node, "Streamflow Gage/Baseflow",
		_node_diam );
	node.setX ( node.getX() - _legend_dx );

	node.setY ( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_BASEFLOW );
	drawBaseflowNodeSymbol ( da, node, "Baseflow (no gage/structure)",
	_node_diam, true);

	node.setY ( node.getY() + _legend_dy );
	node.setType ( HBNode.NODE_TYPE_END );
	drawEndNodeSymbol ( da, node, "Most Downstream Node", _node_diam );
}
*/

/**
Draw the network to the drawing area.  Should be able to call this for
PostScript and screen output.
@param da GR drawing area.
@param node_top Node at the top of the network.
@param reach_level_max Maximum reach level in data.
@param plotflags Flags used to control the network format.
*/
/* REVISIT
private void drawNetwork (	GRDrawingArea da, HBNode node_top,
				int reach_level_max, int plotflags )
{	String	routine = "HBNodeNetwork.drawNetwork";
	HBNode	downstream_node = null, node_pt = null;
	int	dl = 10, i;
	double	x[] = new double[10];
	double	y[] = new double[10];

	// Set the font...

	double fontht_pts = da.scaleYData ( _fontsize ) - da.scaleYData ( 0.0);
	GR.setColor ( da, GRColor.black );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Setting font height to " +
		_font + " (" + fontht_pts + " points)" );
	}
	GR.setFont ( da, _font, fontht_pts );

	GR.pageStart ( da );
	Message.printStatus ( 1, routine, "Drawing lines..." );
	HBNode downstream_real_node;

	// First draw the lines.  Do so by drawing from the current node
	// downstream to the next node.  The only complication is that for
	// drying rivers don't draw the line.  The nodes are drawn on top of
	// the lines.

	double [] dashes = { 1.5 };
	boolean dodash = false;
	for (	node_pt = node_top;
		node_pt != null;
		node_pt = getDownstreamNode(node_pt, POSITION_COMPUTATIONAL)){
		// Break if we are at the end of the list (we do not need to
		// draw the line)...
		downstream_node = node_pt.getDownstreamNode();
		if (	(downstream_node == null) ||
			(node_pt.getType() == HBNode.NODE_TYPE_END) ) {
			break;
		}
		downstream_real_node = findNextRealOrXConfluenceDownstreamNode(
			node_pt );
		// If the confluence of the reach (as opposed
		// to a trib coming in) then this is the last
		// real node in disappearing
		// stream.  Use the end node for the downstream
		// node...
		dodash = false;
		if (	downstream_real_node == getDownstreamNode( node_pt,
			POSITION_REACH).getDownstreamNode() ) {
			// Show as a dashed line...
			dodash = true;
			GR.setLineDash ( da, dashes, 0 );
			// This was before they wanted a dashed line...
			//Don't want to show connection...
			//continue;
		}
		x[0] = node_pt.getX();
		y[0] = node_pt.getY();
		x[1] = downstream_node.getX();
		y[1] = downstream_node.getY();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 15, routine,
			"Drawing line from " + x[0] + "," + y[0] + " to " +
			x[1] + "," + y[1] );
		}
		if ( (plotflags & PLOT_RIVERS_SHADED) != 0 ) {
			// We want to show a shaded wide line for the
			// rivers...
			GR.setColor ( da, GRColor.gray );
			GR.setLineWidth ( da, (double)(reach_level_max -
			node_pt.getReachLevel() + 1) );
			GR.drawLine ( da, x, y );
		}
		GR.setColor ( da, GRColor.black );
		GR.setLineWidth ( da, .5 );
		GR.drawLine ( da, x, y );
		if ( dodash ) {
			// Set back to solid...
			GR.setLineDash ( da, null, 0 );
		}
	}

	// Loop through each node and draw the symbols...

	Message.printStatus ( 1, routine, "Drawing nodes..." );
	for (	node_pt = node_top;
		node_pt != null;
		node_pt = getDownstreamNode( node_pt, POSITION_COMPUTATIONAL)){
		// Break if we are at the end of the list...
		if ( node_pt == null ) {
			break;
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( 5, routine,
			"Processing plot node \"" + node_pt.getNetID() +
			"\" (\"" + node_pt.getCommonID() + "\")" );
		}

		drawNode ( da, node_pt );
		if ( node_pt.getDownstreamNode() == null ) {
			break;
		}
	}

	// Draw the labels calculated elsewhere...

	int nlabels = _labels.size();
	Message.printStatus ( 1, routine, "Drawing " + nlabels + " labels..." );
	GR.comment ( da, "Drawing " + nlabels + " labels..." );
	GR.setColor ( da, GRColor.black );
	for ( i = 0; i < nlabels; i++ ) {
		// Change the font height if not the same as the previous
		// label...
		if (	(i == 0) ||
			((i > 0) &&
			(((Label)(_labels.elementAt(i))).size !=
			((Label)(_labels.elementAt(i - 1))).size)) ) {
			fontht_pts =	da.scaleYData (
					((Label)(_labels.elementAt(i))).size) -
					da.scaleYData ( 0.0 );
			if ( Message.isDebugOn ) {
				Message.printDebug ( 2, routine,
				"Setting font height to " + _font +
				" (" + fontht_pts + " points)" );
			}
			GR.setFont ( da, _font, fontht_pts );
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( 2, routine, "Drawing label \"" +
			((Label)(_labels.elementAt(i))).text + "\" at " +
			((Label)(_labels.elementAt(i))).x + " " +
			((Label)(_labels.elementAt(i))).y );
		}
		GR.drawText (	da, ((Label)(_labels.elementAt(i))).text,
				((Label)(_labels.elementAt(i))).x,
				((Label)(_labels.elementAt(i))).y, 0.0,
				((Label)(_labels.elementAt(i))).flag );
	}

	// process plot commands

	int size = _plot_commands.size();
	for ( i=0; i<size; i++ ) {
		String command = (String)_plot_commands.elementAt(i);
		if ( command.startsWith ( "labelcarrier" ))
			drawCarrier(da, command.substring ( 12 ));
	}

	// Plot the legend...

	drawLegend ( da );

	// Close the plot file...

	GR.pageEnd ( da );
}
*/

/**
Draw label on plot.
@param da GRDrawing area to draw to.
@param node Node of interest.
*/
/* REVISIT
protected void drawNode ( GRDrawingArea da, HBNode node )
{	String 	label, routine = "drawNode";
	double	diam_in = _node_diam, diam_out = (_node_diam * 1.3),
		gray_conf = .01, gray_out = 1.0, gray_in = 1.0;
	int	dl = 20;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Drawing node \"" + node.getNetID() + "\" at " + node.getX() +
		"," + node.getY() );
	}

	if ( node.isBaseflow() ) {
		// Node has proration information and is a baseflow node -
		// draw outer circle. 
		drawBaseflowNodeSymbol ( da, node, "", diam_out, false );
	}
	int node_type = node.getType();
	if ( node_type == HBNode.NODE_TYPE_BLANK ) {
		// Just a spacer in the diagram.  Do not draw anything.
	}
	else if ( (node_type == HBNode.NODE_TYPE_DIV) ||
		(node_type == HBNode.NODE_TYPE_DIV_AND_WELL) ||
		(node_type == HBNode.NODE_TYPE_WELL) ||
		(node_type == HBNode.NODE_TYPE_IMPORT) ) {
		// Demand Node, Import Node...
		label = getNodeLabel ( node, _label_type );
		drawDemandNodeSymbol ( da, node, label, diam_in );
	}
	else if ( node_type == HBNode.NODE_TYPE_FLOW ) {
		label = getNodeLabel ( node, _label_type );
		drawStreamflowNodeSymbol ( da, node, label, diam_in );
	}
	else if ( node_type == HBNode.NODE_TYPE_END ) {
		label = getNodeLabel ( node, _label_type );
		drawEndNodeSymbol ( da, node, label, diam_in );
	}
	else if ( node_type == HBNode.NODE_TYPE_BASEFLOW ) {
		label = getNodeLabel ( node, _label_type );
		drawBaseflowNodeSymbol ( da, node, label, diam_in, true );
	}
	else if ( (node_type == HBNode.NODE_TYPE_CONFLUENCE) ||
		(node_type == HBNode.NODE_TYPE_XCONFLUENCE) ) {
		// Confluences - small filled in dot
		drawConfluenceNodeSymbol ( da, node, diam_in*.3 );
	}
	else if ( node_type == HBNode.NODE_TYPE_ISF ) {
		// Instream flow Node - empty circle with ISF in center
		//
		// Do not pass in the ID because we don't know how to
		// label yet.  But this needs to be done at some point.
		label = getNodeLabel ( node, _label_type );
		drawISFNodeSymbol ( da, node, label, diam_in );
	}
	else if ( node_type == HBNode.NODE_TYPE_OTHER ) {
		label = getNodeLabel ( node, _label_type );
		drawOtherNodeSymbol ( da, node, label, diam_in );
	}
	else if ( node_type == HBNode.NODE_TYPE_RES ) {
		// Reservoir Node - filled triangle pointing upstream
		// (based on "dir")
		label = getNodeLabel ( node, _label_type );
		drawReservoirNodeSymbol ( da, node, label, diam_in );
	}
}
*/

/**
Find the downstream node of a certain type.
@param node Node to start search.
@param type_to_find Node type to find.
*/
/* REVISIT - finish the code
public Node findDownstreamNode ( Node node, String type_to_find )
{	Node	node_downstream_of_type = null, node_prev, node_pt;
	String	routine = "Network.findDownstreamNode";
	int	dl = 12;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Trying to find downstream " + type_to_find +
		" node starting with \"" + node.getID() + "\"" );
	}
	
	node_pt = node;
	for (	node_prev = node_pt,
		node_pt = getDownstreamNode(node_pt, POSITION_RELATIVE);
		node_pt != null;
		node_prev = node_pt,
		node_pt = getDownstreamNode(node_pt, POSITION_RELATIVE)){
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Checking node \"" + node_pt.getCommonID() +
			"\" (previous \"" + node_prev.getCommonID() +
			"\" node_in_reach=" + node_prev.getNodeInReachNumber()
			+ ")" );
		}
		if ( node_prev.equals(node_pt) ) {
			// We have gone to the bottom of the system and there
			// is no downstream flow node.  Return NULL...
			Message.printDebug ( 1, routine,
			"Node \"" + node.getCommonID() +
			"\" has no downstream flow node" );
			return null;
		}
		// This originally worked for makenet and should work now for
		// the admin tool since we are using base flow switch.
		else if (	(node_pt.getType() == HBNode.NODE_TYPE_FLOW) &&
				node_pt.isBaseflow() ) {
			// We have a downstream flow node...
			node_downstream_flow = node_pt;
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"For \"" + node.getCommonID() +
				"\", downstream flow node is \"" +
				node_downstream_flow.getCommonID() +
				"\" A*P=" + node_downstream_flow.getWater() );
			}
			return node_pt;
		}
	}
	return null;
}
*/

/**
Find a node matching some criteria.
This is a general-purpose node search routine that will expand over time as
needed.
@return HBNode that is found or null if none is found.
@param data_type_to_find Types of node data to find.  See NODE_DATA_*
definitions.
*/
/* REVISIT - finish later
public HBNode findNode (	int data_type_to_find, int node_type_to_find,
				String data_value_to_find )
{	String routine = "HBNodeNetwork.findNode";
	int dl = 30;

	// Work from the top of the system...

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Finding data type " + data_type_to_find +
		" node type " + HBNode.getTypeString(node_type_to_find,1) +
		" value \"" + data_value_to_find + "\"" );
	}
	for (	HBNode node_pt =getUpstreamNode(_node_head,POSITION_ABSOLUTE);
		node_pt.getDownstreamNode() != null;
		node_pt = getDownstreamNode(node_pt, POSITION_COMPUTATIONAL) ) {
		if ( node_pt == null ) {
			break;
		}
		// Get the WIS data in case we need it...
		HBWISFormat wis_format = node_pt.getWISFormat();
		if ( node_type_to_find > 0 ) {
			// We have specified a node type to find...
			if (	data_type_to_find == NODE_DATA_LINK ) {
				// We are searching for a link...
				long link = Long.parseLong(data_value_to_find);
				if (	(node_pt.getType()==node_type_to_find)&&
					(wis_format.getWDWaterLink() == link) &&
					(wis_format.getWDWaterLink() > 0) ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine,
						"Found CONF node \"" +
						node_pt.getCommonID() +
						"\" with link " + link );
					}
					return node_pt;
				}
				// Else no link matches...
			}
			else {	Message.printWarning ( 1, routine,
				"Don't know how to find node type: " + 
				node_type_to_find + " data type: " +
				data_type_to_find + " data value: " +
				data_value_to_find );
			}
		}
		else {	Message.printWarning ( 1, routine,
			"Don't know how to find node type: " + 
			node_type_to_find + " data type: " +
			data_type_to_find + " data value: " +
			data_value_to_find );
		}
	}
	return null;
}
*/

/**
Find a node given its common indentifier and a starting node.
@return HBNode that is found or null if not found.
@param commonid Common identifier for node.
@param node Starting node in tree.
*/
/* REVISIT - finish later
public HBNode findNode ( String commonid, HBNode node )
{	HBNode	node_pt;

	for (	node_pt = getUpstreamNode(node, POSITION_ABSOLUTE);
		node_pt != null;
		node_pt = getDownstreamNode(node_pt,POSITION_COMPUTATIONAL)){
		// Break if we are at the end of the list...
		if (	(node_pt == null) ||
			(node_pt.getType() == HBNode.NODE_TYPE_END) ) {
			break;
		}
		if ( node_pt.getCommonID().equalsIgnoreCase(commonid) ) {
			return node_pt;
		}
	}

	return null;
}
*/

/**
Find a node given its common indentifier.  The node head is used as a starting
point to position the network at the top and then the network is traversed.
@return HBNode that is found or null if not found.
@param commonid Common identifier for node.
*/
/* REVISIT later
public HBNode findNode ( String commonid )
{	HBNode	node_pt;

	for (	node_pt = getUpstreamNode(_node_head, POSITION_ABSOLUTE);
		node_pt != null;
		node_pt = getDownstreamNode(node_pt,POSITION_COMPUTATIONAL)){
		// Break if we are at the end of the list...
		if (	(node_pt == null) ||
			(node_pt.getType() == HBNode.NODE_TYPE_END) ) {
			break;
		}
		if ( node_pt.getCommonID().equalsIgnoreCase(commonid) ) {
			return node_pt;
		}
	}

	return null;
}
*/

// ----------------------------------------------------------------------------
// getDownstreamNode - given a node pointer, find a downstream node
// ----------------------------------------------------------------------------
// History:
//
// 07 May 1996	Steven A. Malers, RTi	Created code.
// 24 Jun 1996	SAM, RTi		Check on POSITION_COMPUTATIONAL.  May
//					be having trouble dealing with reaches
//					that have a confluence at the top.
// 23 Nov 1997	SAM, RTi		Change C SetNodeToDownstream to this
//					Java routine.  Add the STREAM, FORMULA,
//					and LABEL nodes to the check.
// ----------------------------------------------------------------------------
/* REVISIT - finish
public static HBNode getDownstreamNode ( HBNode node, int flag )
{	String	routine = "HBNodeNetwork.getDownstreamNode";
	HBNode	node_pt = node;
	int	dl = 20;

	if ( node.getDownstreamNode() == null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Found absolute downstream node \"" +
			node_pt.getNetID() +
			"\" (\"" + node_pt.getCommonID() + "\")" );
		}
		return node;
	}

	if ( flag == POSITION_RELATIVE ) {
		// Just return the downstream node...
		if ( node.getDownstreamNode() == null ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Node \"" + node.getCommonID() +
				"\" has downstream node NULL" );
			}
		}
		else {	if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Node \"" + node.getCommonID() +
				"\" has downstream node \"" +
				(node.getDownstreamNode()).getCommonID() +"\"");
			}
		}
		return node.getDownstreamNode();
	}
	else if ( flag == POSITION_ABSOLUTE ) {
		// It is expected that if you call this you are starting from
		// somewhere on a main stem.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find absolute downstream starting with \"" +
			node.getCommonID() + "\"" );
		}
		// Think of the traversal as a "left-hand" traversal.  Always
		// follow the first branch added since it is the most downstream
		// computation-wise.  When we have no more downstream nodes we
		// are at the bottom...
		node_pt = node;
		if ( node_pt.getDownstreamNode() == null ) {
			// Nothing below this node...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found absolute downstream: \"" + 
				node_pt.getCommonID() + "\"" );
			}
			return node_pt;
		}
		else {	// Follow the first reach entered for this node
			// (the most downstream)...
			return	getDownstreamNode (
				node_pt.getDownstreamNode(),
				POSITION_ABSOLUTE );
		}
	}
	else if ( flag == POSITION_COMPUTATIONAL ) {
		// We want to find the next computational downstream node.  Note
		// that this may mean that an additional branch above a
		// convergence point is traversed.  The computational order is
		// basically decided by the user.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find next computational downstream node for \"" +
			node.getCommonID() + "\" reachnum=" +
			node.getTributaryNumber() );
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Downstream node \"" +
			(node.getDownstreamNode()).getCommonID()+
			"\" has nupstream = " +
			(node.getDownstreamNode()).getNumUpstreamNodes() );
		}
		if ( node.getUpstreamOrder() == HBNode.TRIBS_ADDED_FIRST ) {
			// Makenet convention...
			if (	((node.getDownstreamNode()).getNumUpstreamNodes(
				) == 1) || (node.getTributaryNumber() == 1) ) {
				// Then there is only one downstream node or we
				// are the furthest downstream node in a list of
				// upstream reaches...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to only possible downstream node:  \"" +
					(node.getDownstreamNode()).getCommonID() +
					"\"" );
				}
				return node.getDownstreamNode();
			}
			else {	// The downstream node has several upstream
				// nodes and we are one of them.  Go to the next
				// lowest reach number and travel to the top of
				// that reach and use that node for the next
				// downstream node.  This is a computation
				// order.
				HBNode	node_down = node.getDownstreamNode();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to highest node on next downstream branch starting at \"" +
					(node_down.getUpstreamNode(
					node.getTributaryNumber() - 1 - 1)).getCommonID() + "\"" );
				}
				
				return	getUpstreamNode (
					node_down.getUpstreamNode(
					node.getTributaryNumber() - 1 - 1),
					POSITION_ABSOLUTE );
			}
		}
		else {	// Admin tool convention...
			// Check to see if we are the furthest downstream by
			// having a trib number that is the count of upstream
			// nodes...
			if (	((node.getDownstreamNode()).getNumUpstreamNodes(
				) == 1) ||
				(node.getTributaryNumber() ==
				node.getDownstreamNode().getNumUpstreamNodes())) {
				// Then there is only one downstream node or we
				// are the furthest downstream node in a list of
				// upstream reaches...
				if ( node.getDownstreamNode().getNumUpstreamNodes() == 1 ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine,
						"Going to only possible downstream node because no branch:  \"" +
						(node.getDownstreamNode()).getCommonID() +
						"\"" );
					}
				}
				else {	if ( Message.isDebugOn ) {
						Message.printDebug ( dl,
						routine,
						"Going to only possible downstream node:  \"" +
						(node.getDownstreamNode()).getCommonID() +
						"\" because trib " +
						node.getTributaryNumber() +
						" is max for downstream branching node" );
					}
				}
				return node.getDownstreamNode();
			}
			else {	// The downstream node has several upstream
				// nodes and we are one of them.  Go to the next
				// highest reach number and travel to the top of
				// that reach and use that node for the next
				// downstream node.  This is a computation
				// order.
				HBNode	node_down = node.getDownstreamNode();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to highest node on next downstream branch starting at \"" +
					(node_down.getUpstreamNode(
					node.getTributaryNumber() + 1 - 1)).getCommonID() + "\"" );
				}
				
				return	getUpstreamNode (
					node_down.getUpstreamNode(
					node.getTributaryNumber() + 1 - 1),
					POSITION_ABSOLUTE );
			}
		}
	}
	else if ( flag == POSITION_REACH ) {
		// Find the most downstream node in the reach.  This does NOT
		// include the node on the previous stem, but the first node
		// of the stem in the reach.  Find the node in the current
		// reach with node_in_reach = 1.  This is the node off of the
		// parent river node.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find downstream node in reach starting at \"" +
			node.getCommonID() + "\"" );
		}
		node_pt = node;
		while ( true ) {
			if ( node_pt.getNodeInReachNumber() == 1 ) {
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"First node in reach is \"" +
					node_pt.getCommonID() + "\"" );
				}
				return node_pt;
			}
			else {	// Reset to the next downstream node...
				node_pt = node_pt.getDownstreamNode();
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to downstream node \"" +
					node_pt.getCommonID() +
					"\" [node_in_reach = " +
					node_pt.getNodeInReachNumber() + "]" );
				}
			}
		}
	}
	else {	// Trouble...
		Message.printWarning ( 1, routine,
		"Invalid POSITION argument" );	
		return null;
	}
}
*/

/**
Return the list of features in the Network.
@return the list of features in the Network.
*/
public List getFeatures()
{	return _feature_Vector;
}

/**
@return the most upstream node in the network or null if not found.
*/
/* REVISIT
public HBNode getMostUpstreamNode ()
{	int dl = 30;
	String routine = "HBNodeNetwork.getMostUpstreamNode";

	// Go to the bottom of the system so that we can get to the top of
	// the main stem...

	HBNode node = null;
	node = getDownstreamNode ( _node_head, POSITION_ABSOLUTE );

	// Now traverse downstream, creating the strings...

	node = getUpstreamNode ( node, POSITION_ABSOLUTE );
	return node;
}
*/

/**
@return a Vector of all identifiers in the network given the types
of interest.  The common identifiers are returned or, if the identifier contains
a ".", the part before the "." is returned.  It is expected that this method
is only called with real node types (not BLANK, etc.).
The list is determined going from upstream to downstream so any
code that uses this list should also go upstream to downstream for fastest
performance.
@param node_types Array of node types to find (see HBNode.NODE_TYPE_*).
@exception Exception if there is an error during the search.
*/
/* REVISIT - use to extract lists from network
public Vector getNodeIdentifiersByType ( int [] node_types )
throws Exception
{	String routine = "HBNodeNetwork.getNodeIdentifiersByType";
	Vector ids = new Vector ( 50, 50 );
	int dl = 15;

	try {	// Main try for method

	if ( node_types == null ) {
		return ids;
	}

	HBNode node_pt = null;
	int node_type = 0;
	int j, nnode_types = node_types.length;
	boolean node_type_matches = false;
	String common_id = null;
	Vector v = null;
	// Traverse from upstream to downstream...
	for (	node_pt = getUpstreamNode(
		getDownstreamNode(_node_head,POSITION_ABSOLUTE),
		POSITION_ABSOLUTE);
		node_pt != null;
		node_pt = getDownstreamNode(node_pt, POSITION_COMPUTATIONAL)){
		node_type = node_pt.getType();
		// See if the node_type matches one that we are interested in...
		node_type_matches = false;
		for ( j = 0; j < nnode_types; j++ ) {
			if ( node_types[j] == node_type ) {
				node_type_matches = true;
				break;
			}
		}
		if ( !node_type_matches ) {
			// No need to check further...
			if ( node_pt.getDownstreamNode() == null ) {
				// End...
				break;
			}
			continue;
		}
		// Use the node type of the HBNode to make decisions about
		// extra checks, etc...
		if ( node_type == HBNode.NODE_TYPE_FLOW ) {
			// Just use the common identifier...
			ids.addElement ( node_pt.getCommonID() );
		}
		else if ((node_type == HBNode.NODE_TYPE_DIV) ||
			(node_type == HBNode.NODE_TYPE_DIV_AND_WELL) ||
			(node_type == HBNode.NODE_TYPE_ISF) ||
			(node_type == HBNode.NODE_TYPE_RES) ||
			(node_type == HBNode.NODE_TYPE_WELL) ||
			(node_type == HBNode.NODE_TYPE_IMPORT) ) {
			common_id = node_pt.getCommonID();
			if ( common_id.indexOf('.') >= 0 ) {
				// Get the string before the "." in case some
				// ISF or other modified identifier is used...
				v = StringUtil.breakStringList ( common_id,
					".", 0 );
				ids.addElement( (String)v.elementAt(0));
				// Note that if the _Dwn convention is used,
				// then the structure should be found when the
				// upstream terminus is queried and the
				// information can be reused for the downstream.
			}
			else {	// Just add id as is...
				ids.addElement ( node_pt.getCommonID() );
			}
		}
		else {	// Just use the common identifier...
			ids.addElement ( node_pt.getCommonID() );
		}
		if ( node_pt.getDownstreamNode() == null ) {
			// End...
			break;
		}
	}
	} // Main try
	catch ( Exception e ) {
		String message = "Error getting node identifiers for network";
		Message.printWarning ( 2, routine, message );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( message );
	}

	// Return the final list...

	return ids;
}
*/

// ----------------------------------------------------------------------------
// getUpstreamNode - given a node pointer, find an upstream node
// ----------------------------------------------------------------------------
// History:
//
// 07 May 1996	Steven A. Malers, RTi	Created code.
// 23 Jun 1996	SAM, RTi		Many changes to resolve bugs about
//					going to the top of reaches with
//					confluences at the top.
// 24 Jun 1996	SAM, RTi		Change so that if we are going to the
//					absolute top of a reach, we traverse
//					reaches that come in at the top of a
//					reach.
// 23 Nov 1997	SAM, RTi		Port C SetNodeToUpstream to this Java
//					code.
// 30 Nov 1997	SAM, RTi		Add POSITION_REACH)NEXT to get the next
//					upstream node on the same reach (using
//					the reach counter).
// ----------------------------------------------------------------------------
/* REVISIT
public static HBNode getUpstreamNode ( HBNode node, int flag )
{	HBNode	node_pt, node_pt2;
	String	routine = "HBNodeNetwork.getUpstreamNode";
	int	dl = 15, i, reachnum;

	if ( flag == POSITION_ABSOLUTE ) {
		// It is expected that if you call this you are starting from
		// somewhere on a main stem.  If not, it will only go to the
		// top of a reach.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find absolute upstream starting with \"" +
			node.getCommonID() + "\"" );
		}
		// Think of the traversal as a "right-hand" traversal.  Always
		// follow the last branch added since it is the most upstream
		// computation-wise.  When we have no more upstream nodes we
		// are at the top...
		node_pt = node;
		if ( node_pt.getNumUpstreamNodes() == 0 ) {
			// Nothing above this node...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found absolute upstream: \"" +
				node_pt.getCommonID() + "\"" );
			}
			return node_pt;
		}
		else {	// Follow the last reach entered for this node
			// (the most upstream)...
			if (	node.getUpstreamOrder() ==
				HBNode.TRIBS_ADDED_FIRST ) {
				// We want the last one added (makenet order)...
				return	getUpstreamNode (
					node_pt.getUpstreamNode(
					(node_pt.getNumUpstreamNodes() - 1)),
					POSITION_ABSOLUTE );
			}
			else {	// We want the first one added...
				return	getUpstreamNode (
					node_pt.getUpstreamNode( 0 ), 
					POSITION_ABSOLUTE );
			}
		}
	}
	else if ( flag == POSITION_REACH ) {
		// Try to find the upstream node in the reach...
		//
		// This amounts to taking the last reachnum for every
		// convergence (do not take tribs) and stop at the top of
		// the reach.  This is almost the same as POSITION_ABSOLUTE
		// except that POSITION_ABSOLUTE will follow confluences at the
		// top of a reach and POSITION_REACH will not.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find upstream node in reach starting at \"" +
			node.getCommonID() + "\"" );
		}
		//
		// Think of the traversal as a "right-hand" traversal.  Always
		// follow the last branch added since it is the most upstream
		// computation-wise.  When we have no more upstream nodes we
		// are at the top...
		//
		node_pt = node;
		if ( node_pt.getNumUpstreamNodes() == 0 ) {
			// Nothing above this node...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found absolute upstream: \"" +
				node_pt.getCommonID() + "\"" );
			}
			return node_pt;
		}
		else if ((node_pt.getNumUpstreamNodes() == 1) &&
			((node_pt.getType() == node_pt.NODE_TYPE_CONFLUENCE) ||
			(node_pt.getType() == node_pt.NODE_TYPE_XCONFLUENCE)) ){
			// If it is a confluence, then it must be at the top of
			// the reach and we want to stop...
			if ( Message.isDebugOn ) {
				Message.printDebug ( dl, routine,
				"Found reach top is confluence - not following: \"" +
				node_pt.getCommonID() + "\"" );
			}
			return node_pt;
		}
		else {	// Follow the last reach entered for this node
			// (the most upstream).
			if (	node.getUpstreamOrder() ==
				HBNode.TRIBS_ADDED_FIRST ) {
				// We want the last one added (makenet order)...
				return	getUpstreamNode (
					node_pt.getUpstreamNode(
					(node_pt.getNumUpstreamNodes() - 1)),
					POSITION_REACH );
			}
			else {	// Admin tool style...
				return	getUpstreamNode (
					node_pt.getUpstreamNode(0),
					POSITION_REACH );
			}
		}
	}
	else if ( flag == POSITION_REACH_NEXT ) {
		// Get the next upstream node on the same reach.  This will be
		// a node with the same reach counter.
		for ( i = 0; i < node.getNumUpstreamNodes(); i++ ) {
			node_pt = node.getUpstreamNode ( i );
			if ( node_pt == null ) {
				// Should not happen if the number of nodes
				// came back OK...
				Message.printWarning ( 1, routine,
				"Null upstream node for \"" + node.toString() +
				"\" should not be" );
				return null;
			}
			// Return the node that matches the same reach counter
			if (node.getReachCounter() ==node_pt.getReachCounter()){
				return node_pt;
			}
			else {	// No match...
				return null;
			}
		}
	}
	else if ( flag == POSITION_COMPUTATIONAL ) {
		// We want to find the next upstream computational node.  Note
		// that this may mean choosing a branch above a convergence
		// point...
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Trying to find computational upstream node starting at \"" +
			node.getCommonID() + "\"" );
		}
		if ( node.getNumUpstreamNodes() == 0 ) {
			// Then we are at the end of our reach and we need to
			// to to the next reach if available.
			return findReachConfluenceNext ( node );
		}
		else {	// We have at least one node upstream but we are only
			// interested in the first one or there is only one
			// upstream node (i.e., we are interested in the next
			// computational node upstream)...
			if (	node.getUpstreamOrder() ==
				HBNode.TRIBS_ADDED_FIRST ) {
				// We want the first one added (makenet
				// order)...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to first upstream node \"" +
					node.getUpstreamNode(0).getCommonID() +
					"\"" );
				}
				return node.getUpstreamNode(0);
			}
			else {	// We want the last one added (admin tool
				// order)...
				if ( Message.isDebugOn ) {
					Message.printDebug ( dl, routine,
					"Going to last upstream node \"" +
					node.getUpstreamNode(
					node.getNumUpstreamNodes() - 1).getCommonID() +
					"\"" );
				}
				return node.getUpstreamNode(
				node.getNumUpstreamNodes() - 1);
			}
		}
	}
	else {	// Not called correctly...
		Message.printWarning ( 1, routine,
		"POSITION flag " + flag + " is incorrect" );
	}
	return null;
}
*/

/**
Initialize data.
*/
private void initialize ()
{	_feature_Vector = new Vector();
}

// insertDownstreamNode - insert a node downstream from a given node
/* REVISIT
public int insertDownstreamNode (	HBNode upstream_node,
					HBNode downstream_node )
{	String routine = "HBNodeNetwork.insertDownstreamNode";

	if ( downstream_node == null ) {
		Message.printWarning ( 2, routine,
		"Downstream node is null.  Unable to insert" );
		return 1;
	}

	if ( upstream_node == null ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine,
			"Setting head of list to downstream node" );
		}
		_node_head = downstream_node;
		return 0;
	}

	// Else we should be able to insert the node...

	if ( Message.isDebugOn ) {
		Message.printDebug ( 10, routine,
		"Adding \"" + downstream_node.getCommonID() +
		"\" downstream of \"" + upstream_node.getCommonID() + "\"" );
	}
	upstream_node.addDownstreamNode ( downstream_node );
	return 0;
} 
*/

// ----------------------------------------------------------------------------
// isMostUpstreamNodeInReach -	is the specified node the most upstream in the
//				reach?
// ----------------------------------------------------------------------------
// Notes:	(1)	Start with the given node and move upstream
//			within the reach (not computationally) until we get to
//			a new trib.  If the
//			starting structure is the most upstream real node
//			(a physical-based node), then return true.
// ----------------------------------------------------------------------------
/* REVISIT
public boolean isMostUpstreamNodeInReach ( HBNode node )
{	String routine = "HBNodeNetwork.isMostUpstreamNodeInReach";

	// Loop starting upstream of the specified node and moving upstream.
	// Assume that we are the most upstream and try to prove otherwise.

	int node_type;
	for (	HBNode node_pt = getUpstreamNode(node,POSITION_REACH_NEXT);
		((node_pt != null) &&
		(node_pt.getReachCounter() == node.getReachCounter()));
		node_pt = getUpstreamNode ( node_pt, POSITION_REACH_NEXT) ) {
		node_type = node_pt.getType();
		if ( Message.isDebugOn ) {
			Message.printDebug ( 10, routine,
			"Checking node:  " + node_pt.toString() );
		}
		if (	(node_type == HBNode.NODE_TYPE_OTHER) ||
			(node_type == HBNode.NODE_TYPE_DIV) ||
			(node_type == HBNode.NODE_TYPE_DIV_AND_WELL) ||
			(node_type == HBNode.NODE_TYPE_WELL) ||
			(node_type == HBNode.NODE_TYPE_RES) ||
			(node_type == HBNode.NODE_TYPE_ISF) ||
			(node_type == HBNode.NODE_TYPE_FLOW) ) {
			// We found a higher node...
			return false;
		}
		if ( node_pt.getUpstreamNode() == null ) {
			break;
		}
	}
	return true;
}
*/

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable
{	_feature_Vector = null;
	super.finalize();
}

/**
Set the "processed" flag for each NetworkFeature in the Network.  This can be
used, for example, to clear the flag before processing the nodes in the network.
@param processed Processed flag to set for every feature.
*/
public void setProcessed ( boolean processed )
{	int size = _feature_Vector.size();
	NetworkFeature feature = null;
	for ( int i = 0; i < size; i++ ) {
		feature = (NetworkFeature)_feature_Vector.get(i);
		feature.setProcessed ( processed );
	}
}

} // End of Network
