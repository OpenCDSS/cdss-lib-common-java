//-----------------------------------------------------------------------------
// SimpleJTree_Listener - a listener for classes that need to respond to 
//	JTree events.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2004-07-06	J. Thomas Sapienza, RTI	Initial version
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This class is an interface for classes that need to respond to JTree events.
Currently it only has one method in it -- there wasn't enough budget to spend
time adding all the possibilities, so they should be added as needed.
*/
public interface SimpleJTree_Listener {

/**
Called when a node is expanded in the tree.
@param node the node that is being expanded.
*/
public void nodeExpanding(SimpleJTree_Node node);

}
