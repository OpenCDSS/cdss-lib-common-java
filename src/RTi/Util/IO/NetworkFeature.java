// ----------------------------------------------------------------------------
// NetworkFeature - a representation of a feature (e.g., Node, Link) in a
//			network
// ----------------------------------------------------------------------------
// History:
// 
// 2003-07-28	Steven A. Malers,	Initial version - copy and modify
//		Riverside Technology,	HBNode.
//		inc.
// 2005-04-26	J. Thomas Sapienza, RTi	Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.Util.IO;

import java.util.List;
import java.util.Vector;

import RTi.GR.GRDrawingArea;

/**
This class is the base class for network features, including the Node and Link
objects, for use in NodeNetwork and other collections.
REVISIT JAVADOC: see NodeNetwork
*/
public abstract class NetworkFeature extends Object
{

// TODO SAM 2007-05-09 - put these in the NodeNetwork class so only one list needs to be
// initialized.
//private int [] __feature_types = null;		// An array of allowed feature
						// types (e.g., node type
						// numbers).  To be set
						// in the derived class.
private String [] __feature_names = null;	// An array of allowed feature
						// names (e.g., short names
						// corresponding to node types).


/**
Feature type (e.g., "Diversion").
*/
protected String _type = "";

/**
Identifier for the feature (e.g., a short string).
*/
protected String _id = "";

/**
Name for the feature (e.g., a longer string).
*/
protected String _name = "";

/**
X-coordinate for feature.  REVISIT - should features be derived from GRShape?
*/
public double x = 0.0;

/**
Y-coordinate for feature.  REVISIT - should features be derived from GRShape?
*/
public double y = 0.0;

/**
Properties associated with the feature.
*/
// REVISIT - not needed if use derived classes for each specific node type?
//protected PropList _props = null;

/**
Upstream features.
*/
protected List<NetworkFeature> _upstream_feature_Vector;

/**
Downstream features.
*/
protected List<NetworkFeature> _downstream_feature_Vector;

/**
Object that is associated with the node (e.g., external data object).
*/
protected Object _data;

/**
Temporary data used when processing a network, to indicate that the feature
has been processed.   This may be difficult because of the linkage between
features in the network.  This is meant to be used by higher-level code.
*/
protected boolean _processed = false;

/**
Construct a feature.
@param id Feature identifier.
@param name Feature name.
@param type Feature type.
*/
public NetworkFeature ( String id, String name, String type )
{	initialize();
	// Use these to deal with nulls - don't want null strings.
	setID ( id );
	setName ( name );
	setType ( type );
}

//REVISIT - put in the network?
/**
Add a NetworkFeature downstream from this feature.
@param downstream_feature Downstream feature to add.
*/
/*
public void addDownstreamFeature ( NetworkFeature downstream_feature )
{	String	routine = "NetworkFeature.addDownstreamFeature";
	int	dl =50;

	try {	if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"Adding \"" + downstream_feature.getID() +
			"\" downstream of \"" + getID() + "\"" );
		}
		NetworkFeature old_downstream_feature = _downstream;
		if ( _downstream != null ) {
			// There is a downstream node and we need to reconnect it...
	
			// For the original downstream node, reset its upstream
			// reference to the new node.  Use the common identifier to
			// find the element to reset...
			int pos = _downstream.getUpstreamNodePosition (
					getCommonID() );
			if ( pos >= 0 ) {
				Vector downstream_upstream =
				_downstream.getUpstreamNodes();
				if ( downstream_upstream != null ) {
					downstream_upstream.setElementAt(
					downstream_node, pos);
				}
			}
			// Connect the new downstream node to this node.
			_downstream = downstream_node;
			// Set the upstream node of the new downstream node to point to
			// this node.  For now, assume that the node that is being
			// inserted is a new node...
			if ( downstream_node.getNumUpstreamNodes() > 0 ) {
				Message.printWarning ( 1, routine,
				"Node \"" + downstream_node.getCommonID() +
				"\" has #upstream > 0" );
			}
			// Set the new downstream node data...
			downstream_node.setDownstreamNode ( old_downstream_node );
			downstream_node.addUpstreamNode ( this );
			// Set the new current node data...
			_tributary_number = downstream_node.getNumUpstreamNodes();
		}
		else {	// We always need to do this step...
			downstream_node.addUpstreamNode ( this );
		}
		String downstream_commonid = null;
		if ( downstream_node.getDownstreamNode() != null ) {
			downstream_commonid = old_downstream_node.getCommonID();
		}
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine,
			"\"" + downstream_node.getCommonID() +
			"\" is downstream of \"" +
			getCommonID() + "\" and upstream of \"" +
			downstream_commonid + "\"" );
		}
		return 0;
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error adding downstream node." );
		return 1;
	}
}
*/

/**
Add a node upstream from this node.
@param upstream_node Node to add upstream.
@return 0 if successful, 1 if not.
*/
/* REVISIT - put in the network?
public int addUpstreamNode ( HBNode upstream_node )
{	String	routine = "HBNode.addUpstreamNode";
	int	dl = 50;

	// Add the node to the vector...

	try {
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Adding \"" + upstream_node.getCommonID() +
		"\" upstream of \"" + getCommonID() + "\"" );
	}
	if ( _upstream == null ) {
		// Need to allocate space for it...
		_upstream = new Vector ( 1, 1 );
	}

	_upstream.addElement(upstream_node);

	// Make so the upstream node has this node as its downstream node...

	upstream_node.setDownstreamNode ( this );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "\"" +
		upstream_node.getCommonID() + "\" downstream is \"" +
		getCommonID() + "\"" );
	}
	return 0;
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error adding upstream node." );
		return 1;
	}
}
*/

/**
Break the link with an upstream node.
@param upstream_node Upstream node to disconnect from the network.
@return 0 if successful, 1 if not.
*/
/* REVISIT - put int the network?
public int deleteUpstreamNode ( HBNode upstream_node )
{	String routine = "HBNode.deleteUpstreamNode";

	// Find a matching node.  Just check addesses...

	try {
	for ( int i = 0; i < _upstream.size(); i++ ) {
		if ( upstream_node.equals((HBNode)_upstream.elementAt(i)) ) {
			// We have found a match.  Delete the element...
			_upstream.removeElementAt(i);
			return 0;
		}
	}
	return 1;
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine,
		"Error deleting upstream node." );
		return 1;
	}
}
*/

/**
Finalize before garbage collection.
*/
protected void finalize ()
throws Throwable {
	IOUtil.nullArray(__feature_names);
	_type = null;
	_id = null;
	_name = null;
	_upstream_feature_Vector = null;
	_downstream_feature_Vector = null;
	_data = null;
	super.finalize();
}

/**
Return the identifier for the feature.
@return the identifier for the feature.
*/
public String getID ()
{	return _id;
}

/**
Return the name for the feature.
@return the name for the feature.
*/
public String getDescription ()
{	return _name;
}

/**
Return the downstream feature or null if not available.
@param index of the downstream feature (typically 0 unless there is a
divergence).
*/
public NetworkFeature getDownstreamFeature ( int index )
{	if ( (index < 0) || (index >= _downstream_feature_Vector.size() ) ) {
		return null;
	}
	return (NetworkFeature)_downstream_feature_Vector.get(index);
}

/**
Return the number of downstream features.
@return the number of downstream features.
*/
public int getNumDownstreamFeatures ()
{	return _downstream_feature_Vector.size();
}

/**
Return the number of upstream features.
@return the number of upstream features.
*/
public int getNumUpstreamFeatures ()
{	return _upstream_feature_Vector.size();
}

/**
Return the feature type (e.g., node type).
@return the feature type (e.g., node type).
*/
public String getType ()
{	return _type;
}

/**
Return the upstream feature or null if not available.
@param index of the upstream feature (typically 0 unless the feature is a
convergence).
*/
public NetworkFeature getUpstreamFeature ( int index )
{	if ( (index < 0) || (index >= _upstream_feature_Vector.size() ) ) {
		return null;
	}
	return (NetworkFeature)_upstream_feature_Vector.get(index);
}

/**
Initialize data members.
*/
private void initialize ()
{	_downstream_feature_Vector = new Vector<NetworkFeature>();
	_upstream_feature_Vector = new Vector<NetworkFeature>();
}

/**
Indicate whether the feature has been processed.
@return true if the feature has been processed, false if not.
*/
public boolean isProcessed ()
{	return _processed;
}

/**
Render the feature (draw itself).
@param da GRDrawingArea to draw to.
*/
public void render ( GRDrawingArea da )
{
}

/* REVISIT 
public void setDownstreamNode ( HBNode downstream )
{
	_downstream = downstream;
}
*/

/**
Set the feature identifier.
@param id Feature identifier.
*/
public void setID ( String id )
{	if ( id != null ) {
		_id = id;
	}
}

/**
Set the feature name.
@param name Feature name.
*/
public void setName ( String name )
{	if ( name != null ) {
		_name = name;
	}
}

/**
Set whether the feature has been processed.
@param processed true if the feature has been processed, false if not.
*/
public void setProcessed ( boolean processed )
{	_processed = processed;
}

/**
Set the feature type.
@param type Feature type.
*/
public void setType ( String type )
{	if ( type != null ) {
		_type = type;
	}
}

/**
Return a verbose string representation of the object.
@return a verbose string representation of the object.
*/
public String toString ()
{	/* REVISIT
	String up = "";
	if ( _downstream != null ) {
		down = _downstream.getCommonID();
	}
	else {	down = "null";
	}
	if ( _upstream != null ) {
		for ( int i = 0; i < getNumUpstreamNodes(); i++ ) {
			up = up + " [" + i + "]:\"" +
			getUpstreamNode(i).getCommonID() + "\"";
		}
	}
	else {	up = "null";
	}
	return "\"" + getCommonID() + "\" T=" + getTypeString(_type,1) +
	" T#=" + _tributary_number + " RC=" + _reach_counter + " RL=" +
	_reach_level + " #=" + _serial + " #inR=" + _node_in_reach_number +
	" CO=" + _computational_order +
	" DWN=\"" + down + "\" #up=" + getNumUpstreamNodes() + " UP=" + up;
	*/
	return _id;
}

} // End of NetworkFeature
