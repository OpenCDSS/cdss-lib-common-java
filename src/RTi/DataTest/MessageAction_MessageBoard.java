// ----------------------------------------------------------------------------
// MessageAction_MessageBoard - class that reports on positive test results
//	by adding them to a message board class.  Currently this class is 
//	only supported for RiverTrakSentry code.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2006-05-16	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DataTest;

import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.SimpleJTree;
import RTi.Util.GUI.SimpleJTree_Node;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

import RTi.Util.Time.DateTime;

/**
This class is an action that will print information about positive tests
to the message board.
*/
public class MessageAction_MessageBoard 
extends MessageAction {

/**
The JTree used by the message board code.
*/
private SimpleJTree __tree = null;

/**
Constructor.
@param dataModel data model containing this object's values.
*/
public MessageAction_MessageBoard(ActionDataModel dataModel) 
throws Exception {
	super(dataModel);
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__tree = null;
	super.finalize();
}

/**
Does nothing.
@return true.
*/
public boolean logAction(DataTest test) {
	return true;
}

/**
Prints the top-level data result information to the console.
@param test the DataTest for which to run actions.
@return false if there was an error adding nodes to the message board tree.
True otherwise.
*/
public boolean runAction(DateTime runDateTime, DataTest test) {
	removeOldResults(runDateTime);
	List results = getPositiveResults(test);
	int size = results.size();
	List v = null;
	DataTestResult r = null;
	
	boolean dateNodeAdded = false;
	
	String boldStart = "<html><b>";
	String boldEnd = "</b></html>";

	String colorStart = "";
	String colorEnd = "";

	// Turning this on will DRASTICALLY improve the performance of
	// the tree when adding nodes.
	__tree.setFastAdd(true, true);

	List nodes = new Vector();

	// If there is any severity information with the action, get that
	// and use it to color code the font.

	if (getSeverity() != null) {
		if (getSeverity().getDisplayColor() != null) {
			colorStart = "<html><font color = " + 
				getSeverity().getDisplayColor() + ">";
			colorEnd = "</font color = " + 
				getSeverity().getDisplayColor() + "></html>";
		}
	}

	// Look for the parent node (the one with the date in it).  If it 
	// already exists, the othe nodes will be added under it.  If it does
	// not exist, it will be created.
	
	SimpleJTree_Node parent 
		= __tree.findTopLevelNode(runDateTime.toString(
			DateTime.FORMAT_YYYY_MM_DD_HH_mm));
	if (parent == null) {
		// If the parent is null, then it wasn't found in the tree,
		// so one needs made.
		parent = new SimpleJTree_Node(
			boldStart + runDateTime.toString(
				DateTime.FORMAT_YYYY_MM_DD_HH_mm) + boldEnd,
			boldStart + runDateTime.toString(
				DateTime.FORMAT_YYYY_MM_DD_HH_mm) + boldEnd);

		// Store the date teh node was created in the node, so that it
		// can be used in the Sentry.
		parent.setData(new DateTime(runDateTime));
		
		try {
			if (size > 0) {
				// Only add a date node if there are going to
				// be child nodes under it.
				__tree.addNode(parent, 0);
				dateNodeAdded = true;
			}
		}
		catch (Exception e) {
			Message.printWarning(2, "runAction",
				"Node could not be added to the tree.");
			Message.printWarning(2, "runAction", e);
			__tree.setFastAdd(false, false);
			return false;
		}
	}

	int count = 0;

	SimpleJTree_Node node = null;
	SimpleJTree_Node zeroNode = null;
	SimpleJTree_Node subParent = null;
	int level = -1;

	int maxLevel = 0;
	
	// Iterate through all the results.  Level 0 results will be added
	// under the parent node (above) and the further levels will be added
	// under the level 0 node as children.
	for (int i = 0; i < size; i++) {
		v = (List)results.get(i);

		// find the 0-level result -- there's no assumption that 
		// the nodes are sorted by result level.
		for (int j = v.size() - 1; j >= 0; j--) {		
			r = (DataTestResult)v.get(j);
			level = r.getLevel();

			if (level == 0) {
				// Found the level 0 result, so make its node.
				zeroNode = new SimpleJTree_Node(
					colorStart + r.getMessage() + colorEnd, 
					colorStart + r.getMessage() + colorEnd);
				nodes.add(zeroNode);
			}
			else {
				// If not the level 0 node, at least record it
				// so that the maximum result level can be
				// determined (for putting the other nodes in).
				if (level > maxLevel) {
					maxLevel = level;
				}
			}
		}

		try {
			__tree.addNode(zeroNode, parent);
			count++;
		}
		catch (Exception e) {
			Message.printWarning(2, "runAction",
				"Node could not be added to the tree.");
			Message.printWarning(2, "runAction", e);
			__tree.setFastAdd(false, false);
			return false;
		}				

		// Add the nodes for the other result levels.

		for (int j = 1; j <= maxLevel; j++) {
			if (j != 1) {	
				subParent = new SimpleJTree_Node("Level " + j, 
					"Level " + j);
			
				try {
					__tree.addNode(subParent, zeroNode);
				}
				catch (Exception e) {
					Message.printWarning(2, "runAction",
						"Node could not be added to "
						+ "the tree.");
					Message.printWarning(2, "runAction", e);
					__tree.setFastAdd(false, false);
					return false;
				}				
			}

			for (int k = v.size() - 1; k >= 0; k--) {
				r = (DataTestResult)v.get(k);
				level = r.getLevel();

				if (level != j) {
					continue;
				}
				
				node = new SimpleJTree_Node(
					r.getMessage(),  
					r.getMessage());
	
				try {
					if (j == 1) {
						__tree.addNode(node, zeroNode);
					}
					else {
						__tree.addNode(node, subParent);
					}
				}
				catch (Exception e) {
					Message.printWarning(2, "runAction",
						"Node could not be added to "
						+ "the tree.");
					Message.printWarning(2, "runAction", e);
					__tree.setFastAdd(false, false);
					return false;
				}				
			}
		}

		try {	
//			__tree.collapseNode(zeroNode);
		}
		catch (Exception e) {}		
	}

	// Set properties in the tree that will be used by the Sentry to 
	// do some basic reporting on what is stored in the tree.
	
	int num = 0;
	PropList p = __tree.getDataPropList();
	String val = p.getValue("DateNodeCount");

	if (dateNodeAdded) {
		num = (new Integer(val)).intValue();
		num++;
		p.set("DateNodeCount", "" + num);
	}

	val = p.getValue("DataNodeCount");
	num = (new Integer(val)).intValue();
	num += count;
	p.set("DataNodeCount", "" + num);

	__tree.setDataPropList(p);

	__tree.setFastAdd(false, true);
	for (int i = 0; i < nodes.size(); i++) {
		__tree.expandNode((SimpleJTree_Node)nodes.get(i));
	}

	return true;
}

/**
Sets the tree to which results will be posted.
*/
public void setMessageBoardTree(SimpleJTree tree) {
	__tree = tree;
}

}
