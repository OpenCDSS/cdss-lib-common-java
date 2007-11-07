//------------------------------------------------------------------------------
// TSProductDMI - An interface for managing data storage of TSProducts.
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
// 
// 2004-05-03	J. Thomas Sapienza, RTi	Initial version.
// 2005-08-24	JTS, RTi		Added getTSProductList().
//------------------------------------------------------------------------------

package RTi.GRTS;

import java.util.Vector;

/**
This interface controls the behavior or classes that need to save TSProducts to
some kind of persistent storage.
*/
public interface TSProductDMI {

/**
Returns a name of the DMI that is suitable for use in dialogs and window titles.
@return the name of the DMI.
*/
public String getDMIName();

/**
Returns a list of all the records from the TSProduct table (defined differently
for each database) that the user has access to.
*/
public Vector readTSProductDMITSProductList(boolean newProduct);

/**
Writes the specified TSProduct.  The classes that implements this interface is
responsible for deciding how the product should be written.
@param tsproduct the TSProduct to write.
*/
public boolean writeTSProduct(TSProduct tsproduct);

}
