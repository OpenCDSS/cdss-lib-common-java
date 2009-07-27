// ----------------------------------------------------------------------------
// TableRecord - Store data from one record of a DataTable
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 23 Jun 1999	Catherine E.
//		Nutting-Lane, RTi	Initial version
// 2001-09-17	Steven A. Malers, RTi	Review code.  Add finalize() and
//					improved Javadoc.
// 2004-03-11	J. Thomas Sapienza, RTi	Added isDirty() and setDirty() to 
//					keep track of when records have been
//					edited.
// 2004-10-26	JTS, RTi		Added deleteField().
// ----------------------------------------------------------------------------

package RTi.Util.Table;

import java.util.Vector;
import RTi.Util.Message.Message;

/**
This class is used to contain all the information associated with one record in
a DataTable.  The field values are stored as a list of Object.  In the future
more specialized (and optimized) handling of primitive data types may be
implemented.  The record field values must be consistent with the definition of
the DataTable.  An example of defining a TableRecord is:
<p>

<pre>
TableRecord contents = new TableRecord (3);
contents.addFieldValue ( "123456" );
contents.addFieldValue ( new Integer (6));
contents.addFieldValue ( "RTi station" );
</pre>

@see RTi.Util.Table.Table
@see RTi.Util.Table.TableField
*/
public class TableRecord {

/**
Whether the data has been changed or not.
*/
private boolean __dirty = false;

/**
List of data values corresponding to the different fields.  Currently no list methods are exposed.
*/
private Vector __record;

/**
Construct a new record (with no contents).
*/
public TableRecord() {
	initialize(1);
}

/**
Construct a new record which will have the specified number of elements.
The number of elements can increase as new items are added; however, specifying
the correct number at initialization increases performance
@param num Number of expected fields.
*/
public TableRecord(int num) {
	initialize(num);
}

/**
Clean up for garbage collection.
*/
protected void finalize()
throws Throwable {
	__record = null;
	super.finalize();
}

/**
Initialize the record.
@param num Number of fields in the record (for memory purposes).
*/
private void initialize(int num) {
	__dirty = false;
	__record = new Vector(num);
}

/**
Add a field data value to the record.
@param new_element Data object to add to record.
*/
public void addFieldValue(Object new_element) {
	__record.add(new_element);
}

/**
Deletes a field's data value from the record.
@param fieldNum the number of the field to delete.
*/
public void deleteField(int fieldNum)
throws Exception {
	if (fieldNum < 0 || fieldNum > (__record.size() - 1)) {
		throw new Exception ("Field num " + fieldNum + " out of bounds.");
	}
	__record.remove(fieldNum);
}

/**
Return the contents of a record field.
@return contents at the specified zero-based index.
The returned object must be properly cast.
@exception Exception if an invalid index is requested.
*/
public Object getFieldValue(int index)
throws Exception {
	if (Message.isDebugOn) {
		Message.printDebug(20, "TableRecord.getFieldValue", "Getting index " + index);
	}
	if (__record.size() <= index) {
		throw new Exception ("Index " + index + " of field not valid (" + __record.size() + ")");
	}
	return __record.get(index);
}

/**
Return the number of fields in the record.
@return the number of fields in the record.  
*/
public int getNumberOfFields() {
	return __record.size();
}

/**
Returns whether the record is dirty or not.
@return whether the record is dirty or not.
*/
public boolean isDirty() {
	return __dirty;
}

/**
Sets the field contents of the record at the specified zero-based index.
The number of available fields should be set in the constructor or use setNumberOfFields().
@param index Field position to set.
@param contents Field contents to set.
@exception if the index exceeds the available number of fields within this record.
@return the instance of this record, to facilitate chaining set calls.
*/
public TableRecord setFieldValue(int index, Object contents)
throws Exception {
	if (index < __record.size()) {
		__record.set(index,contents);
	}
	else {	
		throw new Exception("Specified index " + index + " does not exist.");
	}
	return this;
}

/**
Sets whether the table record is dirty or not.
@param dirty whether the table record is dirty or not.
*/
public void setDirty(boolean dirty) {
	__dirty = dirty;
}

// TODO SAM 2009-07-26 Evaluate what is using this - limits using more generic List for data
/**
Sets the number of fields within this record.  If the previous number of
fields is larger than the new number, those fields after the new number of fields will be lost.
@param num Number of fields to include in the record.
*/
public void setNumberOfFields(int num)
{	__record.setSize(num);
}

}