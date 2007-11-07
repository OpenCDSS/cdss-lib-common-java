//------------------------------------------------------------------------------------
// ParseXML - interface to do parse XML files using an XML schema.
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-12      Scott Townsend, RTi     Create initial version of this
//                                              utility. This utility allows the
//                                              parsing of XML files using the
//						Apache xmlbeans XML schema 
//						utility libraries. These libraries
//						allow for compiling of an XSD
//						(XML schema file) into a java
//						API that can be used to parse
//						the XML file without the need to
//						know all of the XML structure.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//------------------------------------------------------------------------------------
// Endheader

package RTi.Util.XML;

import java.io.IOException;

// TODO SAM 2007-05-09 Is this used for anything?

/**
 * This interface defines the data types and methods on how XML documents 
 * which have an XML schema can be parsed.
 * 
 */
public interface ParseXML {
/**
 * <p>This method takes the URL to the XML schema and compiles it with Apache's 
 * XMLBeans package to produce a jar that has an API to parse the specific XML 
 * files the schema represents. This dynamic compilation of the XML
 * schema will require some new functionality of the java 1.5.0 jdk.</p>
 * 
 * 
 * @param XSDFilePath URL to the XML Schema which is to be compiled
 */
public void compileXSD(String XSDFilePath) throws IOException;
/**
 * <p>This method takes the URL to the XML schema and compiles it with Apache's 
 * XMLBeans package to produce a jar that has an API to parse the specific XML 
 * files the schema represents. This dynamic compilation of the XML
 * schema will require some new functionality of the java 1.5.0 jdk.</p>
 * 
 * 
 * @param URLToXSD URL to the XML Schema which is to be compiled
 */
public void compileXSD(java.net.URL URLToXSD) throws IOException;
/**
 * <p>This method returns a version string from the XML schema file or null 
 * if no such string is available. This is used to determine whether or not to 
 * recompile the schema before use. This dynamic compilation of the XML
 * schema will require some new functionality of the java 1.5.0 jdk.</p>
 * 
 * 
 * @param URLToXSD This is the URL to the XML schema file to get the version.
 * @return This return value is a string representing the version of the XML 
 * schema if valid otherwise it returns null.
 */
public String getXSDVersion(java.net.URL URLToXSD);
}


