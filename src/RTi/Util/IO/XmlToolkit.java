// XmlToolkit - useful XML processing methods as a toolkit to avoid static methods

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import RTi.Util.Message.Message;

// TODO smalers 2017-07-12 the methods in this class need to be renamed/refactored.
// Code was consolidated from several places and the methods are redundant with different naming conventions.

/**
 * Useful XML processing methods as a toolkit to avoid static methods. See: -
 * http://www.drdobbs.com/jvm/easy-dom-parsing-in-java/231002580
 *
 * Element and Node are generally interchangeable for basic functionality and different nomenclature is used
 * only because of historical reasons.
 */
public class XmlToolkit {

	/**
	 * Create an instance of the toolkit.
	 */
	public XmlToolkit() {
	}

	/**
	Find an element (given a parent element) that matches the given element name.
	@param parentElement parent element to process
	@param elementName the element name to match
	@param attributeName the attribute name to match (if null, don't try to match the attribute name)
	@return the first matched element, or null if none are matched
	@throws IOException
	*/
	public Element findSingleElement(Element parentElement, String elementName ) throws IOException {
	    return findSingleElement ( parentElement, elementName, null );
	}

	/**
	Find an element (given a starting element) that matches the given element name.
	@param startElement Starting element to process.
	@param elementName The element name to match.  Namespace is ignored.
	@param attributeName Only return the element if the attribute name is matched.
	@return the first matched element, or null if none are matched
	@throws IOException
	*/
	public Element findSingleElement(Element startElement, String elementName, String attributeName ) throws IOException {
	    NodeList nodes = startElement.getElementsByTagNameNS("*",elementName);
	    if ( nodes.getLength() == 0 ) {
	        return null;
	    }
	    else {
	        if ( (attributeName != null) && !attributeName.equals("") ) {
	            // Want to search to see if the node has a matching attribute name.
	            for ( int i = 0; i < nodes.getLength(); i++ ) {
	                Node node = nodes.item(i);
	                NamedNodeMap nodeMap = node.getAttributes();
	                if ( nodeMap.getNamedItem(attributeName) != null ) {
	                    // Found the node of interest.
	                    return (Element)node;
	                }
	            }
	            // No node had the requested attribute.
	            return null;
	        }
	        else {
	            return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
	        }
	    }
	}

	/**
	Find an element (given a starting element) that matches the given element name and return its string content.
	@param startElement parent element to process
	@param elementName the element name to match, returning the value from the elements getTextContent().
	@return the first string text of the first matched element, or null if none are matched
	@throws IOException
	*/
	public String findSingleElementValue(Element startElement, String elementName) throws IOException {
	    Element el = findSingleElement(startElement, elementName);
	    return el == null ? null : el.getTextContent().trim();
	}

	/**
	Get all elements matching the given name from a starting element
	@param startElement Starting element to process
	@param elementName the element name to match, namespace is not used to match.
	@return the elements matching elementName, or null if not matched
	@throws IOException if the number of matching nodes is not 1
	*/
	public List<Element> getElements(Element startElement, String name) throws IOException {
	    NodeList nodes = startElement.getElementsByTagNameNS("*",name);
	    List<Element> elements = new ArrayList<Element>();
	    for ( int i = 0; i < nodes.getLength(); i++ ) {
	        elements.add((Element)nodes.item(i));
	    }
	    return elements;
	}

	/**
	Get a list of element text values.
	Return the list of element text values, using values returned from node getTextContent().
	@param startElement Start element to start the search.
	@param name Name of element to match.  Namespace is ignored to match elements.
	*/
	public List<String> getElementValues(Element startElement, String name) throws IOException {
	    NodeList nodes = startElement.getElementsByTagNameNS("*",name);
	    ArrayList<String> vals = new ArrayList<String>(nodes.getLength());
	    for (int i = 0; i < nodes.getLength(); i++) {
	        vals.add(((Element) nodes.item(i)).getTextContent().trim());
	    }
	    return vals;
	}

	/**
	 * Return the node that matches an element tag name, given a list of nodes to search.
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
	 * Return the nodes that matches an element tag name, given a list of nodes to search.
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
	 * @param attributeName element attribute name, with namespace if present in file
	 * @param node node to process
	 * @return string value for attribute name, or "" if not matched
	 */
	public String getNodeAttribute(String attributeName, Node node ) {
	    NamedNodeMap attributes = node.getAttributes();
	    //Message.printStatus(2, "", "Node \"" + node.getNodeName() + "\" has " + attributes.getLength() + " attributes" );
	    for (int i = 0; i < attributes.getLength(); i++ ) {
	        Node attribute = attributes.item(i);
	        //Message.printStatus(2, "", "Comparing requested attribute \"" + attributeName + "\" with element attribute \"" + attribute.getNodeName() +"\"");
	        if (attribute.getNodeName().equalsIgnoreCase(attributeName)) {
	        	//Message.printStatus(2, "", "Matched, returning \"" + attribute.getNodeValue() + "\"");
	            return attribute.getNodeValue();
	        }
	        else {
	        	//Message.printStatus(2, "", "Not Matched" );
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

	/**
	Return the text value of the single matching element.
	@param parentElement Starting element to process
	@param elementName the element name to match, namespace is not used to match.
	@return the first string text of the first matched element (from getTextContent), or null if none are matched
	@throws IOException
	*/
	public String getSingleElementValue(Element startElement, String elementName)
	throws IOException {
	    return getSingleElement(startElement, elementName).getTextContent().trim();
	}

	/**
	Get the single element matching the given name from a starting element
	@param startElement Starting element to process
	@param elementName the element name to match, namespace is not used to match.
	@return the element matching elementName, or null if not matched
	@throws IOException if the number of matching nodes is not 1
	*/
	public Element getSingleElement(Element startElement, String name) throws IOException {
	    NodeList nodes = startElement.getElementsByTagNameNS("*",name);
	    if (nodes.getLength() != 1) {
	        throw new IOException("Expected to find 1 child \"" + name + "\" in \"" + startElement.getTagName() + "\" - found " + nodes.getLength() );
	    }
	    return (Element) nodes.item(0);
	}

	/**
	Get the single element matching the given name moving up in the DOM relative to the starting element.
	@param startElement Starting element to process
	@param elementName the element name to match, namespace is not used to match.
	@return the element matching elementName, or null if not matched
	*/
	public Element getSingleElementPrevious(Element startElement, String elementName) throws IOException {
		Node node = startElement;
		while ( true ) {
			// Get the parent node.
			node = node.getParentNode();
			if ( node == null ) {
				// Did not find the requested node.
				break;
			}
			if ( node.getNodeName().equalsIgnoreCase(elementName) ) {
				return (Element)node;
			}
		}
	    return null;
	}

}