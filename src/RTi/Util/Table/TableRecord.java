package RTi.Util.Table;

//import java.util.ArrayList;
//import java.util.List;
import RTi.Util.Message.Message;

/**
This class is used to contain all the information associated with one record in
a DataTable.  The field values are stored as an array of Object (formerly List of Object).
The record field values must be consistent with the definition of the DataTable.
An example of defining a TableRecord is as follows.  Setting the record size initially
improves performance because less memory adjustment is required:
<p>

<pre>
TableRecord contents = new TableRecord (3);
contents.addFieldValue ( "123456" );
contents.addFieldValue ( new Integer (6));
contents.addFieldValue ( "Station ID" );
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
//private List<Object> __recordList = null;
//TODO SAM 2012-05-12 the setSize() method in this class needs to be reviewed - limits using List<Object> here
private Object [] __recordArray = null;
/**
Indicate whether record objects should be managed as an array (true) or list (false).
Arrays take less memory but require more memory manipulation if the array is resized dynamically.
*/
private boolean __useArray = true;
/**
Indicate how many values have been set in the record, needed because array may be sized but not populated.
A value of -1 indicates that no columns have been populated.
*/
private int __colMax = -1;

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
Initialize the record.
@param num Number of fields in the record (for memory purposes).
*/
private void initialize(int num) {
	__dirty = false;
	if ( __useArray ) {
	    __recordArray = new Object[num];
	    __colMax = -1;
	}
	else {
	    //__recordList = new ArrayList<Object>(num);
	}
}

/**
Add a field data value to the record.
@param new_element Data object to add to record.
*/
public void addFieldValue(Object new_element)
{   if ( __useArray ) {
        if ( __colMax == -1 ) {
            // No array has been assigned
            __recordArray = new Object[1];
        }
        else if ( __colMax == (__recordArray.length - 1) ) {
            // Have at least one column and need to increment the array size
            Object [] temp = __recordArray;
            __recordArray = new Object [__recordArray.length + 1];
            System.arraycopy(temp, 0, __recordArray, 0, temp.length);
        }
        ++__colMax;
        __recordArray[__colMax] = new_element;
    }
    else {
        //__recordList.add(new_element);
    }
}

/**
Deletes a field's data value from the record, shifting all other values "left".
@param fieldNum the number of the field to delete (0+).
*/
public void deleteField(int fieldNum)
throws Exception
{
    if ( __useArray ) {
        if (fieldNum < 0 || fieldNum > (__colMax) ) {
            throw new Exception ("Field num " + fieldNum + " out of bounds.");
        }
        // Set the internal object to null just to make sure the value does not mistakenly get used
        __recordArray[fieldNum] = null;
        if ( fieldNum == (__recordArray.length - 1) ) {
            // Removing the last value so don't need to do a shift
        }
        else {
            // Copy the right-most objects one to the left
            System.arraycopy(__recordArray, (fieldNum + 1), __recordArray, fieldNum, (__recordArray.length - 1 - fieldNum) );
        }
        --__colMax;
    }
    else {
        /*
    	if (fieldNum < 0 || fieldNum > (__recordList.size() - 1)) {
    		throw new Exception ("Field num " + fieldNum + " out of bounds.");
    	}
    	__recordList.remove(fieldNum);
    	*/
    }
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
	//if ( __useArray ) {
        if (__colMax < index) {
            throw new Exception ("Column index [" + index + "] invalid (record has " + __colMax + " columns)");
        }
        return __recordArray[index];
	//}
    /*
	else {
    	if (__recordList.size() <= index) {
    		throw new Exception ("Column index [" + index + "] invalid (record has " + __recordList.size() + " columns)");
    	}
    	return __recordList.get(index);
	}      */
}

/**
Return the contents of a string record field.  The field must be of type string.
@return contents at the specified zero-based index.
@exception Exception if an invalid index is requested.
*/
public String getFieldValueString(int index)
throws Exception {
    if (Message.isDebugOn) {
        Message.printDebug(20, "TableRecord.getFieldValue", "Getting index " + index);
    }
    Object o = null;
    if ( __useArray ) {
        if (__colMax < index) {
            throw new Exception ("Column index [" + index + "] invalid (record has " + __colMax + " columns)");
        }
        o = __recordArray[index];
    }
    /*
    else {
        if (__recordList.size() <= index) {
            throw new Exception ("Column index [" + index + "] invalid (record has " + __recordList.size() + " columns)");
        }
        o = __recordList.get(index);
    }*/
    if ( o == null ) {
        return null;
    }
    else {
        // Do this to handle cast from integer to string, where integer ID is used in column
        return "" + o;
    }
}

/**
Return the number of fields in the record.
@return the number of fields in the record.  
*/
public int getNumberOfFields()
{
    //if ( __useArray ) {
        return (__colMax + 1);
    //}
    //else {
        //return __recordList.size();
    //}
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
throws Exception
{
    if ( __useArray ) {
        if ( index <= __colMax ) {
            __recordArray[index] = contents;
        }
        else {  
            throw new Exception("Column index [" + index + "] invalid (record has " + (__colMax + 1) + " columns)");
        }        
    }
    else {
        /*
    	if (index < __recordList.size()) {
    		__recordList.set(index,contents);
    	}
    	else {	
    		throw new Exception("Column index [" + index + "] invalid (record has " + __recordList.size() + " columns)");
    	}
    	*/
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
// FIXME SAM 2013-09-18 Comment out and see what complains.  This method is not useful because can't
// just add row columns without knowing the column metadata (type, etc.)
/**
Sets the number of fields within this record.  If the previous number of
fields is larger than the new number, those fields after the new number of fields will be lost.
@param num Number of fields to include in the record.
*/
//public void setNumberOfFields(int num)
//{	__recordList.setSize(num);
//}

}