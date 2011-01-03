//-----------------------------------------------------------------------------
// DataSet - base class to manage a set of DataSetComponent
//-----------------------------------------------------------------------------
// History:
//
// 2003-07-12	Steven A. Malers, RTi	Created class.  Copy the
//					StateCU_DataSetComponent class and make
//					it generic.  This allows the
//					StateCU_DataSet and StateMod_DataSet
//					classes to be derived from one class
//					with common functionality.
// 2003-10-13	SAM, RTi		Add getComponentForComponentName().
// 2005-04-26	J. Thomas Sapienza, RTi	Added all data members to finalize().
// 2004-05-21	SAM, RTi		Comment some of the finalize() on the
//					command names - apparently this can
//					set static strings to null!
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.IO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.Vector;

import RTi.Util.IO.IOUtil;

import RTi.Util.Message.Message;

import RTi.Util.String.StringUtil;

/**
This DataSet class manages a list of DataSetComponent, typically for use with
a model where each component corresponds to a file or section of data within
a file.  This class should be extended to provide specific functionality for a data set.
*/
public abstract class DataSet
{

/**
Base name for data set, used to provide default file names when creating new files.
*/
private String __basename = "";

/**
List of data components.
*/
private List<DataSetComponent> __components = null;

/**
Array of component names, used in lookups.
*/
protected String [] _component_names = null;

/**
Array of component types (as integers), corresponding to the component names.
*/
protected int [] _component_types = null;

/**
Array of component types (as integers) that are group components.
*/
protected int [] _component_groups = null;

/**
Array of component types (as integers) that indicates the group components for each component.
*/
protected int [] _component_group_assignments = null;

/**
Array of component types (as integers) that indicates the primary components for each group.
These components are used to get the list of object identifiers for displays and processing.
*/
protected int [] _component_group_primaries = null;

/**
Directory for data set.
*/
private String __dataset_dir = "";

/**
Name of the data set file (XML file).
*/
private String __dataset_filename = "";

// TODO - evaluate switching this to a String - it is not checked as often
// as the component types are
/**
Data set type.  The derived class can use this to define specific data set
types.  The value is initialized to -1.
*/
private int __type = -1;

/**
Construct a blank data set.  It is expected that other information will be set
during further processing.  Component groups are not initialized until a data set type is set.
*/
public DataSet ()
{	__components = new Vector ();
}

/**
Construct a blank data set.  It is expected that other information will be set
during further processing.
@param component_types Array of sequential integers (0...N) that are used to
identify components.  Integers are used to optimize processing in classes that
use the data set.  Components can be groups or individual components.
@param component_names Array of String component names, suitable for use in
displays and messages.
@param component_groups A subset of the component_types array, in the same order
as component_types, indicating the components that are group components.
@param component_group_assignments An array of integers containing values for
each value in component_types.  The values should be the component group for
each individual component.
@param component_group_primaries An array of integers, having the same length
as component_groups, indicating the components within the group that are the
primary components.  One primary component should be identified for each group
and the primary component will be used to supply a list of objects/identifiers
to create the list of objects identifiers in the group.
*/
public DataSet ( int [] component_types, String [] component_names, int [] component_groups,
	int [] component_group_assignments, int [] component_group_primaries )
{	__components = new Vector ();
	_component_types = component_types;
	_component_names = component_names;
	_component_groups = component_groups;
	_component_group_assignments = component_group_assignments;
	_component_group_primaries = component_group_primaries;
}

/**
Construct a blank data set.  Specific output files, by default, will use the
output directory and base file name in output file names.  The derived class
method should initialize the specific data components.
@param type Data set type.
@param dataset_dir Data set directory.
@param basename Basename for files (no directory).
*/
public DataSet ( int type, String dataset_dir, String basename )
{	__type = type;
	__dataset_dir = dataset_dir;
	__basename = basename;
	__components = new Vector ();
}

/**
Add a component to the data set.
@param comp Component to add.
*/
public void addComponent ( DataSetComponent comp )
{	__components.add ( comp );	
}

/**
Determine whether a data component has data.  A check is made for a non-null data object.
@param component_type The component type to evaluate.
@return true if a data component has a non-null data object, false if not.
Return false if the component does not exist in the data set.
*/
public boolean componentHasData ( int component_type )
{	DataSetComponent comp = getComponentForComponentType ( component_type );
	if ( comp == null ) {
		return false;
	}
	if ( comp.getData() != null ) {
		return true;
	}
	else {
		return false;
	}
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	__basename = null;
	__components = null;
	// TODO SAM 2005-05-21 The following sets static component names to
	// null!  Subsequent access of the component names do not work.
	//IOUtil.nullArray(_component_names);
	_component_types = null;
	_component_groups = null;
	_component_group_assignments = null;
	_component_group_primaries = null;
	__dataset_dir = null;
	__dataset_filename = null;
	super.finalize();
}

/**
Return the base name for the data set.
@return the base name for the data set.
*/
public String getBaseName ()
{	return __basename;
}

/**
Return the component at an index position within the data set.  This is useful
for iteration through the components.
@return the component at an index position within the data set (can be null).
@param pos Index position in the component vector.
*/
public DataSetComponent getComponentAt ( int pos )
{	return (DataSetComponent)__components.get(pos);
}

/**
Return the full path to the component data file.  If the original file name
was set as absolute, then it is returned.  Otherwise, the data set directory
and component data file name are joined.
@return the full path to the component data file or null if it cannot be determined.
@param comp_type Component type.
*/
public String getComponentDataFilePath ( int comp_type )
{	DataSetComponent comp = getComponentForComponentType ( comp_type );
	if ( comp == null ) {
		return null;
	}
	return getComponentDataFilePath ( comp );
}

/**
Return the full path to the component data file.  If the original file name
was set as absolute, then it is returned.  Otherwise, the data set directory
and component data file name are joined.
@return the full path to the component data file or null if it cannot be determined.
@param comp Component.
*/
public String getComponentDataFilePath ( DataSetComponent comp )
{	File f = new File ( comp.getDataFileName() );
	if ( f.isAbsolute() ) {
		return comp.getDataFileName();
	}
	return __dataset_dir + File.separator + comp.getDataFileName();
}

/**
Return the component for the requested data component name.
@return the component for the requested data component name or null if the
component is not in the data set.
@param name Component name.
*/
public DataSetComponent getComponentForComponentName ( String name )
{	int size = __components.size();
	DataSetComponent comp = null;
	List v;
	int size2;
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)__components.get(i);
		if ( comp.getComponentName().equalsIgnoreCase(name) ) {
			return comp;
		}
		// If the component is a group and did not match the type, check
		// the sub-types in the component...
		if ( comp.isGroup() ) {
			v = (List)comp.getData();
			size2 = 0;
			if ( v != null ) {
				size2 = v.size();
			}
			for ( int j = 0; j < size2; j++ ) {
				comp = (DataSetComponent)v.get(j);
				if ( comp.getComponentName().equalsIgnoreCase( name) ) {
					return comp;
				}
			}
		}
	}
	return null;
}

/**
Return the component for the requested data component type.
@return the component for the requested data component type or null if the
component is not in the data set.
@param type Component type.
*/
public DataSetComponent getComponentForComponentType ( int type )
{	int size = __components.size();
	DataSetComponent comp = null;
	List v;
	int size2;
	//Message.printStatus ( 2, "", "looking up component " + type );
	for ( int i = 0; i < size; i++ ) {
		comp = __components.get(i);
		//Message.printStatus ( 2, "", "Checking " + comp.getComponentType() );
		if ( comp.getComponentType() == type ) {
			return comp;
		}
		// FIXME SAM 2010-12-1 The following does not look right - why is it getting the component
		// data instead of dealing with group/type?
		// If the component is a group and did not match the type, check
		// the sub-types in the component...
		if ( comp.isGroup() ) {
			v = (List)comp.getData();
			size2 = 0;
			if ( v != null ) {
				size2 = v.size();
			}
			for ( int j = 0; j < size2; j++ ) {
				//Message.printStatus ( 2, "", "Checking " + comp.getComponentType() );
				comp = (DataSetComponent)v.get(j);
				if ( comp.getComponentType() == type ) {
					return comp;
				}
			}
		}
	}
	return null;
}

/**
Return the list of data components.
@return the list of data components.
*/
public List<DataSetComponent> getComponents ()
{	return __components;
}

/**
Return the data components Vector for component that are groups.
@return the data components Vector for component that are groups.
*/
public List<DataSetComponent> getComponentGroups ()
{	int size = __components.size();
	List<DataSetComponent> v = new Vector();
	DataSetComponent comp = null;
	for ( int i = 0; i < size; i++ ) {
		comp = __components.get(i);
		if ( comp.isGroup() ) {
			v.add ( comp );
		}
	}
	return v;
}

/**
Return the directory for the data set.
@return the directory for the data set.
*/
public String getDataSetDirectory ()
{	return __dataset_dir;
}

/**
Determine the full path to a component data file.
@param file File name (e.g., from component getDataFileName()).
@return Full path to the data file.
*/
public String getDataFilePath ( String file )
{	File f = new File ( file );
	if ( f.isAbsolute() ) {
		return file;
	}
	else {
		return __dataset_dir + File.separator + file;
	}
}

/**
Return the name of the data set file name (XML file).
@return the name of the data set file name (XML file).
*/
public String getDataSetFileName ()
{	return __dataset_filename;
}

/**
Return the data set type.
@return the data set type.
*/
public int getDataSetType ()
{	return __type;
}

/**
Return the data set type name.  This method calls lookupDataSetName() for the instance.
@return the data set type name.
*/
/* TODO - for now put in extended class because no generic way has been added
here to keep track of different data set types
public String getDataSetName ()
{	return lookupDataSetName ( __type );
}
*/

/**
Initialize the data set.  This method should be defined in the extended class.
*/
//public abstract void initializeDataSet ( );

/**
Determine the component group type for a component type.  For example, use this
to determine the group to add input components to when reading an input file.
@param component_type The component type (should not be a group component).
@return the component group type for the component or -1 if a component group cannot be determined.
*/
public int lookupComponentGroupTypeForComponent ( int component_type )
{	if (	(component_type < 0) ||
		(component_type >= _component_group_assignments.length) ) {
		return -1;
	}
	else {
		return _component_group_assignments[component_type];
	}
}

/**
Return the component name given its number.
@return the component name given its number or null if the component type is not found.
*/
public String lookupComponentName ( int component_type )
{	// The component types are not necessarily numbers that match array indices
	// so match the type values
	for ( int i = 0; i < _component_types.length; i++ ) {
		if ( component_type == _component_types[i] ) {
			return _component_names[i];
		}
	}
	return null;
}

/**
Return the numeric component type given its string name.
@return the numeric component type given its string type, or -1 if not found.
@param component_name the component tag from the response file.
*/
public int lookupComponentType ( String component_name )
{	for ( int i = 0; i < _component_types.length; i++ ) {
		if ( _component_names[i].equalsIgnoreCase(component_name) ) {
			return _component_types[i]; // The _component_names and _component_types arrays must align
		}
	}
	return -1;
}

/**
Determine the primary data set component for a component group.  This is used,
for example, as the component in the group that will supply the list of
objects when no list file is available.
@param component_type The component type (should be a group component).
@return the component type for the primary component in a group or -1 if a
primary component cannot be determined.
*/
public int lookupPrimaryComponentTypeForComponentGroup ( int component_type )
{	// First get the group for the component...
	int compgroup = lookupComponentGroupTypeForComponent ( component_type );
	if ( compgroup < 0 ) {
		return -1;
	}

	// Now find the matching group...

	int size = 0;
	if ( _component_groups != null ) {
		size = _component_groups.length;
	}
	for ( int i = 0; i < size; i++ ) {
		if ( _component_groups[i] == compgroup ) {
			if ( (i >= _component_group_primaries.length) ) {
				return -1;
			}
			else {
				return _component_group_primaries[i];
			}
		}
	}
	return -1;
}

/**
Indicate whether any components in the data set are dirty (data have been modified in memory).
@return true if any files in the data set are dirty.
*/
public boolean isDirty ()
{	int size = __components.size();
	DataSetComponent comp = null;
	List v;
	int size2;
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)__components.get(i);
		if ( comp.isDirty() ) {
			if ( Message.isDebugOn ) {
				Message.printDebug ( 1, "", "Component [" + i + "] " + comp.getComponentName()+ " is dirty." );
			}
			return true;
		}
		// If the component is a group and it was not dirty (above), check the sub-components...
		if ( comp.isGroup() ) {
			v = (List)comp.getData();
			size2 = 0;
			if ( v != null ) {
				size2 = v.size();
			}
			for ( int j = 0; j < size2; j++ ) {
				comp = (DataSetComponent)v.get(j);
				if ( comp.isDirty() ) {
					if ( Message.isDebugOn ) {
						Message.printDebug ( 1, "", "Croup sub-component " +
						comp.getComponentName() + " is dirty." );
					}
					return true;
				}
			}
		}
	}
	return false;
}

// TODO - Need to evaluate whether this NEEDS TO BE IN EACH DERIVED CLASS.
/**
Process an XML Document node during the read process.
@param dataset DataSet that is being read.
@param node an XML document node, which may have children.
@exception Exception if there is an error processing the node.
*/
/* TODO
private static void processDocumentNodeForRead ( DataSet dataset, Node node )
throws Exception
{	String routine = "DataSet.processDocumentNodeForRead";
	switch ( node.getNodeType() ) {
		case Node.DOCUMENT_NODE:
			// The main data set node.  Get the data set type, etc.
			processDocumentNodeForRead( dataset, ((Document)node).getDocumentElement() );
			break;
		case Node.ELEMENT_NODE:
			// Data set components.  Print the basic information...
			String element_name = node.getNodeName();
			Message.printStatus ( 1, routine, "Element name: " + element_name );
			NamedNodeMap attributes;
			Node attribute_Node;
			String attribute_name, attribute_value;
			// Evaluate the nodes attributes...
			if ( element_name.equalsIgnoreCase("DataSet") ){
				attributes = node.getAttributes();
				int nattributes = attributes.getLength();
				for ( int i = 0; i < nattributes; i++ ) {
					attribute_Node = attributes.item(i);
					attribute_name = attribute_Node.getNodeName();
					if ( attribute_name.equalsIgnoreCase("Type" ) ) {
						try {
							dataset.setComponentType ( attribute_Node.getNodeValue(), true );
						}
						catch ( Exception e ) {
							Message.printWarning ( 2, routine, "Data set type \"" + attribute_name +
							"\" is not recognized." );
							throw new Exception ( "Error processing data set" );
						}
					}
					else if (
						attribute_name.equalsIgnoreCase( "BaseName" ) ) {
						dataset.setBaseName ( attribute_Node.getNodeValue() );
					}
				}
			}
			else if ( element_name.equalsIgnoreCase(
				"DataSetComponent") ) {
				attributes = node.getAttributes();
				int nattributes = attributes.getLength();
				String comptype = "", compdatafile = "", complistfile = "", compcommandsfile ="";
				for ( int i = 0; i < nattributes; i++ ) {
					attribute_Node = attributes.item(i);
					attribute_name = attribute_Node.getNodeName();
					attribute_value = attribute_Node.getNodeValue();
					if ( attribute_name.equalsIgnoreCase("Type" ) ) {
						comptype = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase("DataFile" ) ) {
						compdatafile = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase("ListFile" ) ) {
						complistfile = attribute_value;
					}
					else if(attribute_name.equalsIgnoreCase("CommandsFile" ) ) {
						compcommandsfile = attribute_value;
					}
					else {
						Message.printWarning ( 2, routine, "Unrecognized attribute \"" + attribute_name+
						" for \"" + element_name +"\"");
					}
				}
				int component_type = lookupComponentType ( comptype );
				if ( component_type < 0 ) {
					Message.printWarning ( 2, routine,
					"Unrecognized data set component \"" + comptype + "\".  Skipping." );
					return;
				}
				// Add the component...
				DataSetComponent comp = new DataSetComponent ( component_type );
				comp.setDataFileName ( compdatafile );
				comp.setListFileName ( complistfile );
				comp.setCommandsFileName ( compcommandsfile );
				Message.printStatus ( 1, routine, "Adding new component for data \"" + compdatafile + "\" \"" );
				dataset.addComponent ( comp );
			}
			// The main document node will have a list of children
			// (data set components) but components will not.
			// Recursively process each node...
			NodeList children = node.getChildNodes();
			if ( children != null ) {
				int len = children.getLength();
				for ( int i = 0; i < len; i++ ) {
					processDocumentNodeForRead ( dataset, children.item(i) );
				}
			}
			break;
	}
}
*/

// TODO - put in derived class
/**
Read a component.  This method should be defined in the extended class.
@param comp DataSetComponent to read, using the file defined for the data set component.
@exception Exception if an error occurs reading the component.
*/
//public abstract void readComponent ( DataSetComponent comp );

/**
Read a complete data set from an XML data set file.
@param filename XML data set file to read.
@param read_all If true, all the data files mentioned in the response file will
be read into memory, providing a complete data set for viewing and manipulation.
@exception Exception if there is an error reading the file.
*/
/* TODO
public static DataSet readXMLFile ( String filename, boolean read_all )
throws Exception
{	String routine = "DataSet.readXMLFile";
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );

	DOMParser parser = null;
	try {
		parser = new DOMParser();
		parser.parse ( full_filename );
	}
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, "Error reading data set \"" + filename + "\"" );
		Message.printWarning ( 2, routine, e );
		throw new Exception ( "Error reading data set \"" + filename + "\"" );
	}

	// Create a new data set object...

	DataSet dataset = new DataSet();
	File f = new File ( full_filename );
	dataset.setDirectory ( f.getParent() );
	dataset.setDataSetFileName ( f.getName() );

	// Now get information from the document.  For now don't hold the
	// document as a data member...

	Document doc = parser.getDocument();

	// Loop through and process the document nodes, starting with the root node...

	processDocumentNodeForRead ( dataset, doc );

	// Synchronize the response file with the control file (for now just
	// check - need to decide how to make bulletproof)...

	/ * TODO
	StateCU_DataSetComponent comp = dataset.getComponentForComponentType (
		StateCU_DataSetComponent.TYPE_RESPONSE );
	if ( comp != null ) {
		StateCU_DataSet ds2 = readStateCUFile (
		comp.getDataFile(), false );
	}
	* /

	// Compare components and response file.  Need to REVISIT this.

	// Now just read the components - the assumption is that the data set
	// components are correct for the data set but need to tighten this down

	String read_warning = "";
	if ( read_all ) {
		Vector components = dataset.getComponents();
		int size = dataset.__components.size();
		String datafile = "";
		DataSetComponent comp;
		for ( int i = 0; i < size; i++ ) {
			comp = (DataSetComponent)components.elementAt(i);
			try {	datafile = comp.getDataFileName();
				f = new File(datafile);
				if ( !f.isAbsolute() ) {
					datafile = dataset.getDirectory() + File.separator + datafile;
				}
/ * TODO
				if ( comp.getType() == StateCU_DataSetComponent.TYPE_CU_LOCATIONS ) {
					comp.setData (StateCU_Location.readStateCUFile(datafile));
				}
				else if (comp.getType() == StateCU_DataSetComponent.TYPE_CROP_CHARACTERISTICS ) {
					comp.setData ( StateCU_CropCharacteristics.readStateCUFile( datafile));
				}
				else if (comp.getType() == StateCU_DataSetComponent.TYPE_BLANEY_CRIDDLE ) {
					comp.setData ( StateCU_BlaneyCriddle.readStateCUFile(datafile));
				}
				else if (comp.getType() == StateCU_DataSetComponent.TYPE_CLIMATE_STATIONS ) {
					comp.setData ( StateCU_ClimateStation.readStateCUFile(datafile));
				}
* /
			}
			catch ( Exception e ) {
				read_warning += "\n" + datafile;
				Message.printWarning ( 2, routine, e );
			}
		}
	}
	else {
		// Read the control file???
	}
	if ( read_warning.length() > 0 ) {
		Message.printWarning ( 1, routine, "Error reading data files:" + read_warning );
	}

	return dataset;
}
*/

/**
Set the base name for the data set.
@param basename Base name for the data set.
*/
public void setBaseName ( String basename )
{	__basename = basename;
}

/**
Set the file name (no directory) for the data set (XML file).
@param filename File for the data set.
*/
public void setDataSetFileName ( String filename )
{	__dataset_filename = filename;
}

/**
Set the directory for the data set.
@param dir Directory for the data set.
*/
public void setDataSetDirectory ( String dir )
{	__dataset_dir = dir;
}

/**
Set a component dirty (edited).  This method is usually called by the
set methods in the individual data object classes.
@param component_type The component type within the data set.
@param is_dirty Flag indicating whether the component should be marked dirty.
*/
public void setDirty ( int component_type, boolean is_dirty )
{	DataSetComponent comp = getComponentForComponentType ( component_type );
	if ( comp != null ) {
		comp.setDirty ( is_dirty );
	}
}

/**
Set the data set type.
@param type Data set type.
@param initialize_components If true, the components are cleared and the
component groups for the type are initialized by calling the
initializeDataSet() method, which should be defined in the extended class.
@exception Exception if there is an error setting the data type or initializing the component groups.
*/
public void setDataSetType ( int type, boolean initialize_components )
throws Exception
{	__type = type;
	if ( initialize_components ) {
		__components.clear ();
	}
	//initializeDataSet ();
}

/**
Set the data set type
@param type Data set type.
@exception Exception if the data type string is not recognized.
@param initialize_components If true, the components are cleared and the
component groups for the type are initialized.
*/
/* TODO - can this be put here?  For now but in derived class
public void setDataSetType ( String type, boolean initialize_components )
throws Exception
{	int itype = lookupDataSetType ( type );
	if ( itype < 0 ) {
		throw new Exception ( "Data set type \"" + type + "\" is not recognized." );
	}
	setDataSetType ( itype, initialize_components );
}
*/

/**
Return a string representation of the data set (e.g., for debugging).
@return a string representation of the data set.
*/
public String toString ()
{ 	int size = __components.size();
	DataSetComponent comp = null;
	List v;
	int size2;
	StringBuffer buffer = new StringBuffer ();
	for ( int i = 0; i < size; i++ ) {
		comp = (DataSetComponent)__components.get(i);
		buffer.append ( "\nDataSetComponent:  " );
		if ( comp == null ) {
			buffer.append ( "null\n" );
		}
		else {
			buffer.append ( comp.getComponentName() + "\n" );
			buffer.append ( "    Type:       " + comp.getComponentType() + "\n");
			buffer.append ( "    Is group:   "+comp.isGroup()+"\n");
			buffer.append ( "    Is dirty:   "+comp.isDirty()+"\n");
			buffer.append ( "    Is visible: "+comp.isDirty()+"\n");
		}
		if ( comp.isGroup() ) {
			v = (List)comp.getData();
			size2 = 0;
			if ( v != null ) {
				size2 = v.size();
			}
			for ( int j = 0; j < size2; j++ ) {
				comp = (DataSetComponent)v.get(j);
				buffer.append ( "    SubComponent:  " );
				if ( comp == null ) {
					buffer.append ( "null\n" );
				}
				else {
					buffer.append ( comp.getComponentName() + "\n" );
					buffer.append ( "        Type:       " + comp.getComponentType() + "\n");
					buffer.append ( "        Is group:   " + comp.isGroup()+"\n");
					buffer.append ( "        Is dirty:   " + comp.isDirty()+"\n");
					buffer.append ( "        Is visible: " + comp.isVisible()+"\n");
					buffer.append ( "        Data File:  " + comp.getDataFileName()+"\n");
				}
			}
		}
	}
	return buffer.toString();
}

/*
Write the data set to an XML file.  The filename is adjusted to the
working directory if necessary using IOUtil.getPathUsingWorkingDir().
@param filename_prev The name of the previous version of the file (for
processing headers).  Specify as null if no previous file is available.
@param filename The name of the file to write.
@param data_Vector A Vector of StateCU_Location to write.
@param new_comments Comments to add to the top of the file.  Specify as null if no
comments are available.
@exception IOException if there is an error writing the file.
*/
public static void writeXMLFile ( String filename_prev, String filename,
					DataSet dataset, String [] new_comments )
throws IOException
{	String [] comment_str = { "#" };
	String [] ignore_comment_str = { "#>" };
	PrintWriter out = null;
	String full_filename_prev = IOUtil.getPathUsingWorkingDir ( filename_prev );
	if ( !StringUtil.endsWithIgnoreCase(filename,".xml") ) {
		filename = filename + ".xml";
	}
	String full_filename = IOUtil.getPathUsingWorkingDir ( filename );
	out = IOUtil.processFileHeaders ( full_filename_prev, full_filename, 
		new_comments, comment_str, ignore_comment_str, 0 );
	if ( out == null ) {
		throw new IOException ( "Error writing to \"" + full_filename + "\"" );
	}
	writeDataSetToXMLFile ( dataset, out );
	out.flush();
	out.close();
	out = null;
}

/**
Write a data set to an opened XML file.
@param data A DataSet to write.
@param out output PrintWriter.
@exception IOException if an error occurs.
*/
private static void writeDataSetToXMLFile ( DataSet dataset, PrintWriter out )
throws IOException
{	//String cmnt = "#>";
	//DataSetComponent comp = null;

/* TODO - need to evaluate how best to implement
	// Start XML tag...
	out.println ("<!--" );
	out.println ( cmnt );
	out.println ( cmnt + "  StateCU Data Set (XML) File" );
	out.println ( cmnt );
	out.println ( cmnt + "EndHeader" );
	out.println ("-->" );

	out.println ("<StateCU_DataSet " +
		"Type=\"" + lookupTypeName(dataset.getType()) + "\"" +
		"BaseName=\"" + dataset.getBaseName() + "\"" +
		">" );

	int num = 0;
	Vector data_Vector = dataset.getComponents();
	if ( data_Vector != null ) {
		num = data_Vector.size();
	}
	String indent1 = "  ";
	String indent2 = indent1 + indent1;
	for ( int i = 0; i < num; i++ ) {
		comp = (StateCU_DataSetComponent)data_Vector.elementAt(i);
		if ( comp == null ) {
			continue;
		}
		out.println ( indent1 + "<StateCU_DataSetComponent" );

		out.println ( indent2 + "Type=\"" +
			StateCU_DataSetComponent.lookupComponentName(
			comp.getType()) + "\"" );
		out.println ( indent2 + "DataFile=\"" +
			comp.getDataFileName() + "\"" );
		out.println ( indent2 + "ListFile=\"" +
			comp.getListFileName() + "\"" );
		out.println ( indent2 + "CommandsFile=\"" +
			comp.getCommandsFileName() + "\"" );
		out.println ( indent2 + ">" );

		out.println ( indent1 + "</StateCU_DataSetComponent>");
	}
	out.println ("</StateCU_DataSet>" );
*/
}

}