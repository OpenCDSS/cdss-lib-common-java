//------------------------------------------------------------------------------------
// PrintXSDTree - class to use Apache's XMLBeans to print an XML schema.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-12      Scott Townsend, RTi     Create initial version of this
//                                              utility. This utility allows the
//                                              printing of XSD schema file 
//						using the Apache XMLBeans code.
//						The code in this module is based
//						on the Apache XMLBreans utility 
//						program:
//						   TypeHierarchyPrinter
//
//						Any code lifted from that program
//						is under the following Apache
//						license statement.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//	-----
//	Copyright 2004 The Apache Software Foundation
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//		http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	-----
//------------------------------------------------------------------------------------
// Endheader

// Package
package RTi.Util.XML;

// Imports
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;
/**
 * This class takes an XML schema file (XSD file) and prints a tree of the 
 * XML hierarchy for the XML data file.
 * 
 */
public class PrintXSDTree {
// Class variables

/**
 * <p>Static method to download a XML schema from a URL. </p>
 * 
 * 
 * @param URLToXSD The URL object of the XML schema file from which to download.
 * @throws IOException
 */
public  static void DownloadXSDFile(URL URLToXSD) throws IOException {
	//Local variables
	char[] xsdBuf = new char[1024];
	FileWriter FW;
	InputStreamReader ISR;
	int bufLen = 0;
	String XSDFilePath = IOUtil.getPathUsingWorkingDir((String)null), 
		parseURL = null;
	Vector URLFileString = new Vector();
		
	// Parse the URL.getFile() String to get the filename without
	// the path. Add the file to the path from IOUtil to get
	// the local XSD file path to download the file.
	parseURL = URLToXSD.getFile();
	URLFileString = StringUtil.breakStringList(parseURL,"/",0);
	XSDFilePath += (String)URLFileString.lastElement();

	// Download the file at the URL
	ISR = new InputStreamReader(URLToXSD.openStream());
	FW = new FileWriter(XSDFilePath);
	bufLen = ISR.read(xsdBuf,0,1024);
	if(bufLen != -1) {
		FW.write(xsdBuf);
	}
		
	// Loop to get all of the characters of the XSD file.
	while(bufLen == 1024) {
		bufLen = ISR.read(xsdBuf,0,1024);
		if(bufLen != -1) {
			FW.write(xsdBuf);
		}
	}
		
	// Close the streams
	FW.close();
	ISR.close();
} // End of method DownloadXSDFile(URL)

/**
 * <p>Method to do the printing of an XML schema. The XSD file is passed as a
 * File object and thus has already been downloaded.</p>
 * 
 * @param XSDFile a File object holding the XML schema file to print.
 * @return Vector holding a String for each line of the "Printed" schema .
 * @throws IOException
 */
public 	static Vector PrintSchemaTree(File XSDFile) throws IOException {
	// Local variables
	String printLineString = null;
	Vector returnVector = new Vector();
	
	// Set up the dynamic compile for the schema
	SchemaTypeLoader linkTo = null;
	SchemaTypeSystem typeSystem;
	Collection compErrors = new ArrayList();
	XmlOptions schemaOptions = new XmlOptions();
	schemaOptions.setErrorListener(compErrors);
	schemaOptions.setCompileDownloadUrls();

	// Get the XML Schema File object and put into XmlObject
	try
	{
		List sdocs = new ArrayList();
		sdocs.add(SchemaDocument.Factory.parse(XSDFile,
			(new XmlOptions()).setLoadLineNumbers()));

		XmlObject[] schemas = (XmlObject[])sdocs.
			toArray(new XmlObject[0]);

		// Dynamically compile the schema file
		typeSystem = XmlBeans.compileXsd(schemas, linkTo, 
			schemaOptions);
	}
	catch (XmlException e)
	{
		printLineString = "Schema invalid:" + 
			" couldn't recover from errors";
		if (compErrors.isEmpty())
			printLineString += e.getMessage();
		else for (Iterator i = compErrors.iterator(); i.hasNext(); )
			printLineString += i.next();
		throw new IOException(printLineString);
	}

	if (!compErrors.isEmpty())
	{
		printLineString = "Schema invalid: partial " +
			"schema type system recovered";
		for (Iterator i = compErrors.iterator(); i.hasNext(); )
			printLineString += i.next();

		returnVector.add(printLineString);
	}

	// Go through all the types, and note their base types 
	// and namespaces
	Map prefixes = new HashMap();
	prefixes.put("http://www.w3.org/XML/1998/namespace", "xml");
	prefixes.put("http://www.w3.org/2001/XMLSchema", "xs");
	returnVector.add("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");

	// This will be a map of (base SchemaType -> Collection of 
	// directly dervied types)
	Map childTypes = new HashMap();

	// breadthfirst traversal of the type containment tree
	List allSeenTypes = new ArrayList();
	allSeenTypes.addAll(Arrays.asList(typeSystem.documentTypes()));
	allSeenTypes.addAll(Arrays.asList(typeSystem.attributeTypes()));
	allSeenTypes.addAll(Arrays.asList(typeSystem.globalTypes()));

	for (int i = 0; i < allSeenTypes.size(); i++)
	{
		SchemaType sType = (SchemaType)allSeenTypes.get(i);

		// recurse through nested anonymous types as well
		allSeenTypes.addAll(Arrays.
			asList(sType.getAnonymousTypes()));

		// we're not interested in document types, attribute 
		// types, or chasing the base type of anyType
		if (sType.isDocumentType() || sType.isAttributeType() || 
			sType == XmlObject.type)
			continue;

		// assign a prefix to the namespace of this type 
		// if needed
		noteNamespace(prefixes, sType);

		// enter this type in the list of children of its base type
		Collection children = (Collection)childTypes.
			get(sType.getBaseType());

		if (children == null)
		{
			children = new ArrayList();
			childTypes.put(sType.getBaseType(), children);

			// the first time a builtin type is seen, add 
			// it too (to get a complete tree up to anyType)
			if (sType.getBaseType().isBuiltinType())
				allSeenTypes.add(sType.getBaseType());
		}
		children.add(sType);
	}

	// Print the tree, starting from xs:anyType (i.e., XmlObject.type)
	List typesToPrint = new ArrayList();
	typesToPrint.add(XmlObject.type);
	StringBuffer spaces = new StringBuffer();
	while (!typesToPrint.isEmpty())
	{
		SchemaType sType = (SchemaType)typesToPrint.
			remove(typesToPrint.size() - 1);
		if (sType == null)
			spaces.setLength(Math.max(0, spaces.length() - 2));
		else
		{
			printLineString = spaces + "+-" + 
				QNameHelper.readable(sType, prefixes) + 
				notes(sType);
			returnVector.add(printLineString);
			Collection children = (Collection)childTypes.
				get(sType);
			if (children != null && children.size() > 0)
			{
				spaces.append(typesToPrint.size() == 0 || 
					typesToPrint.get(typesToPrint.
					size() - 1) == null ? "  " : "| ");
				typesToPrint.add(null);
				typesToPrint.addAll(children);
			}
		}
	}

	// Return the Vector
	return(returnVector);

} // End of method void PrintSchemaTree()

/**
 * <p>Method do the return a type String from an XML schema.</p>
 * 
 * @param sType The XML schema type to determine the type String.
 * @return String value holding the schema "type" for the note.
 */
private static String notes(SchemaType sType)
{
	if (sType.isBuiltinType())
		return " (builtin)";

	if (sType.isSimpleType())
	{
		switch (sType.getSimpleVariety())
		{
			case SchemaType.LIST:
				return " (list)";
			case SchemaType.UNION:
				return " (union)";
			default:
				if (sType.getEnumerationValues() != null)
					return " (enumeration)";
				return "";
		}
	}

	switch (sType.getContentType())
	{
		case SchemaType.MIXED_CONTENT:
			return " (mixed)";
		case SchemaType.SIMPLE_CONTENT:
			return " (complex)";
		default:
			return "";
	}
} // End of method notes(SchemaType)

/**
 * <p>Method to print the type String note for an XML schema namespace.</p>
 * 
 * @param prefixes The XML schema prefix tags to map.
 * @param sType The XML schema type.
 */
private static void noteNamespace(Map prefixes, SchemaType sType)
{
	String namespace = QNameHelper.namespace(sType);
	if (namespace.equals("") || prefixes.containsKey(namespace))
		return;

	String base = QNameHelper.suggestPrefix(namespace);
	String result = base;
	for (int n = 0; prefixes.containsValue(result); n += 1)
	{
		result = base + n;
	}

	prefixes.put(namespace, result);
	System.out.println("xmlns:" + result + "=\"" + namespace + "\"");
} // End of method noteNameSpace(Map,SchemaType)
} // End of class PrintXSDTree
