package RTi.Util.IO;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Useful XML processing methods as a toolkit to avoid static methods. See: -
 * http://www.drdobbs.com/jvm/easy-dom-parsing-in-java/231002580
 */
public class XmlToolkit {

	/**
	 * Create an instance of the toolkit.
	 */
	public XmlToolkit() {
	}

	/**
	 * Return the node that matches an element tag name, given a list of nodes
	 * to search.
	 * 
	 * @param tagName
	 *            element tag name
	 * @param nodes
	 *            nodes to search (not recursive)
	 * @return first node that is matched, or null if not matched
	 */
	public Node getNode(String tagName, NodeList nodes) {
		for (int x = 0; x < nodes.getLength(); x++) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				return node;
			}
		}

		return null;
	}
	
	/**
	 * Return the nodes that matches an element tag name, given a list of nodes
	 * to search.
	 * 
	 * @param tagName
	 *            element tag name
	 * @param nodes
	 *            nodes to search (not recursive)
	 * @return first node that is matched, or null if not matched
	 */
	public List<Node> getNodes(String tagName, NodeList nodes) {
		List<Node> nodeList = new ArrayList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				nodeList.add(node);
			}
		}
		return nodeList;
	}

	/**
	 * Return the attribute value for a node or "" if not found.
	 * 
	 * @param attributeName
	 *            element attribute name
	 * @param node
	 *            node to process
	 * @return string value for attribute name, or "" if not matched
	 */
	public String getNodeAttribute(String attributeName, Node node ) {
	    NamedNodeMap attributes = node.getAttributes();
	    for (int y = 0; y < attributes.getLength(); y++ ) {
	        Node attribute = attributes.item(y);
	        if (attribute.getNodeName().equalsIgnoreCase(attributeName)) {
	            return attribute.getNodeValue();
	        }
	    }
	    return "";
	}
	
	/**
	 * Return the string value of an element or "" if not found.
	 * 
	 * @param tagName
	 *            element tag name
	 * @param nodes
	 *            nodes to search (not recursive)
	 * @return string value for first node that is matched, or "" if not matched
	 */
	public String getNodeValue(String tagName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
	}
}