//-----------------------------------------------------------------------------
// DragAndDropTransferPrimitive - A class for holding primitive data for easy 
//	transfer between drag and drop components.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
// 2004-02-26	J. Thomas Sapienza, RTi	Initial version.
// 2004-03-04	JTS, RTi		Updated Javadocs in response to 
//					numerous changes.
// 2004-04-27	JTS, RTi		* Revised after SAM's review.
//					* Renamed from DragAndDropPrimitive to
//					  DragAndDropTransferPrimitive.
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.Serializable;

import RTi.Util.Message.Message;

/**
This class provides a wrapper for transferring primitive data types (boolean,
double, float, int and String) between drag and drop components.  It can be 
used for transferring simple data.  More complex objects can be transferred by:
making the class extend Serializable and Transferable and filling in the 
required method bodies.
*/
@SuppressWarnings("serial")
public class DragAndDropTransferPrimitive
implements Serializable, Transferable {

/**
Class name.
*/
private final String __CLASS = "DragAndDropTransferPrimitive";

/**
Used to refer to the types of data that can be stored in this class.
*/
public final static int 
	TYPE_BOOLEAN = 0,
	TYPE_DOUBLE = 1,
	TYPE_FLOAT = 2,
	TYPE_INT = 3,
	TYPE_STRING = 4;

/**
Boolean data that can be stored in this class.
*/
private boolean __b;

/**
Flavor for local transfer of boolean data.
*/
public static final DataFlavor booleanFlavor = 
	new DataFlavor(DragAndDropTransferPrimitive.class, "Boolean");

/**
Flavor for local transfer of double data.
*/
public static final DataFlavor doubleFlavor = 
	new DataFlavor(DragAndDropTransferPrimitive.class, "Double");

/**
Flavor for local transfer of float data.
*/
public static final DataFlavor floatFlavor = 
	new DataFlavor(DragAndDropTransferPrimitive.class, "Float");

/**
Flavor for local transfer of int data.
*/
public static final DataFlavor intFlavor = 
	new DataFlavor(DragAndDropTransferPrimitive.class, "Integer");

/**
Flavor for local transfer of string data.
*/
public static final DataFlavor stringFlavor = 
	new DataFlavor(DragAndDropTransferPrimitive.class, "String");

/**
Flavor for simple plain text transfer, including text transfer into
non-Java applications.
*/
public static final DataFlavor textFlavor = DataFlavor.stringFlavor;

/**
Double data that can be stored in this class.
*/
private double __d;

/**
Float data that can be stored in this class.
*/
private float __f;

/**
Int data that can be stored in this class.
*/
private int __i;

/**
Type of data stored in this class.
*/
private int __type = -1;

/**
String data that can be stored in this class.
*/
private String __s;

/**
Private constructor so this class can never be instantiated with no parameters.
*/
@SuppressWarnings("unused")
private DragAndDropTransferPrimitive() {}

/**
Constructor for object to hold boolean data.
@param B Boolean wrapper around boolean data to transfer.
*/
public DragAndDropTransferPrimitive(Boolean B) {
	__b = B.booleanValue();
	__type = TYPE_BOOLEAN;
}

/**
Constructor for object to hold boolean data.
@param b boolean data to transfer.
*/
public DragAndDropTransferPrimitive(boolean b) {
	__b = b;
	__type = TYPE_BOOLEAN;
}

/**
Constructor for object to hold double data.
@param D Double wrapper around double data to transfer.
*/
public DragAndDropTransferPrimitive(Double D) {
	__d = D.doubleValue();
	__type = TYPE_DOUBLE;
}

/**
Constructor for object to hold double data.
@param d double data to transfer.
*/
public DragAndDropTransferPrimitive(double d) {
	__d = d;
	__type = TYPE_DOUBLE;
}

/**
Constructor for object to hold float data.
@param F Float wrapper around float data to transfer.
*/
public DragAndDropTransferPrimitive(Float F) {
	__f = F.floatValue();
	__type = TYPE_FLOAT;
}

/**
Constructor for object to hold float data.
@param f float data to transfer.
*/
public DragAndDropTransferPrimitive(float f) {
	__f = f;
	__type = TYPE_FLOAT;
}

/**
Constructor for object to hold int data.
@param I Integer wrapper around int data to transfer.
*/
public DragAndDropTransferPrimitive(Integer I) {
	__i = I.intValue();
	__type = TYPE_INT;
}

/**
Constructor for object to hold int data.
@param i int data to transfer.
*/
public DragAndDropTransferPrimitive(int i) {
	__i = i;
	__type = TYPE_INT;
}

/**
Constructor for object to hold String data.  Null strings are turned into
empty strings.
@param s String data to transfer.
*/
public DragAndDropTransferPrimitive(String s) {
	if (s == null) {
		__s = "";
	}
	else {
		__s = s;
	}
	__type = TYPE_STRING;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__s = null;
	super.finalize();
}

/**
Returns the data stored in this object.
@return the data stored in this object.
*/
public Object getData() {
	switch (__type) {
		case TYPE_BOOLEAN:
			return new Boolean(__b);
		case TYPE_DOUBLE:
			return new Double(__d);
		case TYPE_FLOAT:
			return new Float(__f);
		case TYPE_INT:
			return new Integer(__i);
		case TYPE_STRING:
			return __s;
	}
	// will never happen, just required for the compiler:
	return null;	
}	

/**
Returns the type of data stored in this object.
@return the type of data stored in this object.
*/
public int getDataType() {
	return __type;
}

/**
Returns the data to be transferred using the specified flavor.
@param flavor the flavor in which data should be transferred.
@return the data to be transferred using the specified flavor.
@throws UnsupportedFlavorException if the flavor is not supported.
*/
public Object getTransferData(DataFlavor flavor) 
throws UnsupportedFlavorException {
	if (!isDataFlavorSupported(flavor)) {
		throw new UnsupportedFlavorException(flavor);
	}

	DataFlavor[] flavors = getTransferDataFlavors();

	// check to see if it's a local copy of a data object or a serializable
	// object.  Either way, return the current object.  For local data 
	// it will be returned as normal, otherwise the JVM takes care of
	// serializing it.
	if (flavor.equals(flavors[0])) {
		return this;
	}
	// just a plain text return.  Convert the value to a String and
	// return it.
	else {
		String s = null;
		switch (__type) {
			case TYPE_BOOLEAN:
				s = "" + __b;
			case TYPE_DOUBLE:
				s = "" + __d;
			case TYPE_FLOAT:
				s = "" + __f;
			case TYPE_INT:
				s = "" + __i;
			case TYPE_STRING:	
				s = __s;
		}
		try {
			return new String(s);
		}
		catch (Exception e) {
			Message.printWarning(2, __CLASS + ".getTransferData",
				e);
			return null;
		}
	}
}

/**
Returns the array of data flavors in which the current object can be 
transferred.
@return the array of data flavors in which the current object can be
transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	return getTransferDataFlavors(__type);
}

/**
Returns the array of data flavors in which an object of the specified type can
be transferred.
@param type the type of data for which to return the data flavors.
@return the array of data flavors in which an object of the specified type can
be transferred.
*/
public static DataFlavor[] getTransferDataFlavors(int type) {
	DataFlavor[] flavors = new DataFlavor[2];
	switch (type) {
		case TYPE_BOOLEAN:
			flavors[0] = booleanFlavor;
			break;
		case TYPE_DOUBLE:
			flavors[0] = doubleFlavor;
			break;
		case TYPE_FLOAT:
			flavors[0] = floatFlavor;
			break;
		case TYPE_INT:
			flavors[0] = intFlavor;
			break;
		case TYPE_STRING:
			flavors[0] = stringFlavor;
			break;
	}
	flavors[1] = textFlavor;
	return flavors;	
}

/**
Checks to see if a certain data flavor is supported by this class.
@param flavor the flavor to check.
@return true if the flavor is supported, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	DataFlavor[] flavors = getTransferDataFlavors();	
	for (int i = 0; i < flavors.length; i++) {
		if (flavors[i].equals(flavor)) {
			return true;
		}
	}
	return false;
}

}
