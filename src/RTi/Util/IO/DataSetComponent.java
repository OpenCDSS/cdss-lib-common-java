//-----------------------------------------------------------------------------
// DataSetComponent - an object to maintain information about a single
//			component from a data set
//-----------------------------------------------------------------------------
// History:
//
// 2003-07-12	Steven A. Malers, RTi	Created class. Copy
//					StateCU_DataSetComponent and make more
//					general.
// 2003-07-15	J. Thomas Sapienza, RTi	Added hasData()
// 2003-10-13	SAM, RTi		* Initialize __is_dirty and __is_group
//					  to false.
//					* Add a copy constructor.
// 2005-04-26	J. Thomas Sapienza, RTi	Added all data members to finalize().
// 2006-04-10	SAM, RTi		* Added isOutput() to indicate whether
//					  the component is being created as
//					  output.  This is used to evaluate
//					  whether data checks need to be done.
//					* Add getDataCheckResults() and
//					  setDataCheckResults() to handle
//					  verbose output for data checks.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
//-----------------------------------------------------------------------------

package RTi.Util.IO;

import java.util.Vector;

import RTi.Util.Message.Message;

/**
This DataSetComponent class stores information for a single data
component in a data set.
Typically, each component corresponds to a file, a table in a database or a
section within a single monolithic data file.  A list of components is
maintained in the DataSet class.
*/
public class DataSetComponent
{

/**
Indicate how the list for a component group is created.
*/
// REVISIT - need to figure out generic this can be.
public final static String LIST_SOURCE_PRIMARY_COMPONENT = "PrimaryComponent";
public final static String LIST_SOURCE_NETWORK = "Network";
public final static String LIST_SOURCE_LISTFILE = "ListFile";

private int __type = -1;
				// Type of component - integer used to increase
				// performace so string lookups don't need to
				// be done.

private String __name = "";	// Name of the component.

private String __data_file_name = "";
				// Name of file that will hold the data when
				// saved to disk.

private String __commands_file_name = "";
				// Name of the commands file used to create the
				// component, if _created_from is
				// DATA_FROM_COMMANDS.

private String __list_file_name = "";
				// Name of the list file corresponding to the
				// data component (e.g., used by StateDMI).

private String __list_source = "";
				// Indicates how the list for a component group
				// is created (see LIST_SOURCE_*).
				// REVISIT - need to handle

private Object __data = null;	// Data for component type (often a Vector of
				// objects).  If the component is a group, the
				// data will be a Vector of the components in
				// the group.

private boolean __is_dirty = false;
				// Indicates whether the component is dirty.

private boolean __is_group = false;
				// Is the data component actually a group of
				// components.  In this case the _data is a
				// Vector of StateCU_DataSetComponent.
				// This is determined from the group type, not
				// whether the component actually has
				// subcomponents

private boolean __is_output = false;
				// Is the data component being saved as output?
				// This is used, for example, to help know when
				// to perform data checks.

private boolean __is_visible = true;
				// Indicates if the component should be visible
				// because of the data set type (control
				// settings).  Extra components may be included
				// in a data set to ease transition from one
				// data set type to another.

private DataSetComponent __parent = null;
				// If the component belongs to a group, this
				// reference points to the group component.

private DataSet __dataset = null;
				// The DataSet that this DataSetComponent
				// belongs to.  It is assumed that a
				// DataSetComponent always belongs to a DataSet,
				// even if only a partial data set (e.g., one
				// group).

private Vector __data_check_results = null;
				// A Vector of String used to store data check
				// results, suitable for printing to an output
				// file header, etc.  See the __is_output flag
				// to help indicate when check results should
				// be created.

/**
Construct the data set component and set values to empty strings and null.
@param dataset the DataSet instance that this component belongs to (note that
the DataSet.addComponent() method must still be called to add the component).
@param type Component type.
@exception Exception if there is an error creating the object.
*/
public DataSetComponent ( DataSet dataset, int type )
throws Exception
{	__dataset = dataset;
	if ( (type < 0) || (type >= __dataset._component_types.length) ) {
		throw new Exception ( "Unrecognized type " + type );
	}
	__type = type;
	int size = 0;
	if ( __dataset._component_groups != null ) {
		size = __dataset._component_groups.length;
	}
	// Set whether the component is a group, based on the data set
	// information.
	__is_group = false;
	for ( int i = 0; i < size; i++ ) {
		if ( __dataset._component_groups[i] == __type ) {
			__is_group = true;
			break;
		}
	}
	// Set the component name, based on the data set information.
	__name = __dataset.lookupComponentName ( __type );
}

/**
Copy constructor.
@param comp Original data component to copy.
@param dataset The dataset for the component.  This is normally a copy, not the
original (e.g., from the DataSet copy constructor).
@param deep_copy If true, all data are copied (currently not recognized).
If false, the component is copied but not the data itself.  This is
typically used to save the names of components before editing in the response
file editor.
*/
public DataSetComponent (	DataSetComponent comp, DataSet dataset,
				boolean deep_copy )
throws Exception
{	this ( dataset, comp.getComponentType() );
	__type = comp.__type;
	__name = comp.__name;
	__data_file_name = comp.__data_file_name;
	__commands_file_name = comp.__commands_file_name;
	__list_file_name = comp.__list_file_name;
	__list_source = comp.__list_source;
	__data = null;	// REVISIT - support deep copy later
	__is_dirty = comp.__is_dirty;
	__is_group = comp.__is_group;
	__is_visible = comp.__is_visible;
	__parent = comp.__parent;
	__dataset = comp.__dataset;
}

/**
Add a component to this component.  This method should only be called to add
sub-components to a group component.
@param component Sub-component to add to the component.
@exception Exception if trying to add a component to a non-group component.
*/
public void addComponent ( DataSetComponent component )
throws Exception
{	String routine = "DataSetComponent.addComponent";
	if ( !isGroup() ) {
		Message.printWarning ( 2, routine,
		"Trying to add component to non-group component." );
		return;
	}
	if ( __data == null ) {
		// Allocate memory for the components.
		__data = new Vector ();
	}
	((Vector)__data).addElement ( component );
	// Set so the component knows who its parent is...
	component.__parent = this;
	if ( Message.isDebugOn ) {
		Message.printDebug ( 1, routine,
		"Added " + component.getComponentName() + " to " +
		getComponentName() );
	}
}

/**
Get the parent (group) component.
@return the parent (group) component (may be null).
*/
public DataSetComponent getParentComponent()
{	return __parent;
}

/**
Finalize before garbage collection.
@exception Throwable if an error occurs.
*/
protected void finalize()
throws Throwable
{	__commands_file_name = null;
	__data_file_name = null;
	__list_file_name = null;
	__name = null;
	__data = null;
	__list_source = null;
	__parent = null;
	__dataset = null;
	super.finalize();
}

/**
Return the file name for the commands used to create the object.
@return the file name for the commands used to create the object.
*/
public String getCommandsFileName ()
{	return __commands_file_name;
}

/**
Return the name of the component.
@return the name of the component.
*/
public String getComponentName ()
{	return __name;
}

/**
Return the data component type.
@return the data component type.
*/
public int getComponentType ()
{	return __type;
}

/**
Return the data for the component.
@return the data for the component.
*/
public Object getData ()
{	return __data;
}

/**
Return the data check results, as a Vector of String.
@return the data check results.
*/
public Vector getDataCheckResults ()
{	return __data_check_results;
}

/**
Return the file name where data are written.
@return the file name where data are written.
*/
public String getDataFileName ()
{	return __data_file_name;
}

/**
Return the file name for the original list.
@return the file name for the original list.
*/
public String getListFileName ()
{	return __list_file_name;
}

/**
Return the source of the list for the component (see LIST_SOURCE_*).
@return the source of the list for the component.
*/
public String getListSource ()
{	return __list_source;
}

/**
Indicates whether there is data contained in the object.  If the data is null,
false is returned.  If the data is a Vector and is has 0 elements, false is 
returned.  Otherwise, true is returned.
@return whether there is data.
*/
public boolean hasData() {
	if (__data == null) {
		return false;
	}
	if (__data instanceof Vector) {
		if (((Vector)__data).size() == 0) {
			return false;
		}
	}
	return true;
}

/**
Indicate whether the component is dirty (has been modified).
@return true if the component is a dirty (has been modified).
*/
public boolean isDirty ()
{	return __is_dirty;
}

/**
Indicate whether the component is a group.
@return true if the component is a group to organize other components.
*/
public boolean isGroup ()
{	return __is_group;
}

/**
Indicate whether the component is being created as output, which usually means
that the component is being written to a file or other persistent location.
@return true if the component is being created as output.
*/
public boolean isOutput ()
{	return __is_output;
}

/**
Indicate whether the component is visible (if not it is because the component
has been included but is not needed for the current data set).
@return true if the component is visible in displays.
*/
public boolean isVisible ()
{	return __is_visible;
}

/**
Set the commands file name for the component's data.
@param commands_file_name Commands file name for the component's data.
*/
public void setCommandsFileName ( String commands_file_name )
{	__commands_file_name = commands_file_name;
}

/**
Set the component type.
@param type Component type.
*/
public void setComponentType ( int type )
{	__type = type;
}

/**
Set the data object containing the component's data.  Often this is a Vector
of objects.
@param data Data object containing the component's data.
*/
public void setData ( Object data )
{	__data = data;
}

/**
Set the data check results, as a Vector of String.
@param data_check_results The data check results.
*/
public void setDataCheckResults ( Vector data_check_results )
{	__data_check_results = data_check_results;
}

/**
Set the file name for the component's data.
@param data_file_name File name for the component's data.
*/
public void setDataFileName ( String data_file_name )
{	__data_file_name = data_file_name;
}

/**
Set whether the component is dirty (has been edited).
@param is_dirty true if the component is dirty (has been edited).
*/
public void setDirty ( boolean is_dirty )
{	__is_dirty = is_dirty;
}

/**
Set whether the component is a group.
@param is_group Indicate whether the component is a group collector.
*/
public void setIsGroup ( boolean is_group )
{	__is_group = is_group;
}

/**
Set whether the component is output.
@param is_output Indicate whether the component is being created as output,
which usually means that it is being writtent to a file or other persistent
location.
*/
public void setIsOutput ( boolean is_output )
{	__is_output = is_output;
}

/**
Set the list file name for the component's data.
@param list_file_name List file name for the component's data.
*/
public void setListFileName ( String list_file_name )
{	__list_file_name = list_file_name;
}

/**
Set the source of the list for the component (see LIST_SOURCE_*).
@param list_source The source of the list for the component.
*/
public void setListSource ( String list_source )
{	__list_source = list_source;
}

/**
Set the parent component.  This is used when the parent is a group.
@param parent Group component that is the parent.
*/
public void setParent ( DataSetComponent parent )
{	__parent = parent;
}

/**
Set whether the component is visible (if not it is because the component
has been included but is not needed for the current data set).
@param is_visible true if the component should be visible in displays.
*/
public void setVisible ( boolean is_visible )
{	__is_visible = is_visible;
}

} // End DataSetComponent
