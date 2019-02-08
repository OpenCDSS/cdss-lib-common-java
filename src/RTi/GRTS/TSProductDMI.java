// TSProductDMI - an interface for managing data storage of TSProducts

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

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

import java.util.List;

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
public List readTSProductDMITSProductList(boolean newProduct);

/**
Writes the specified TSProduct.  The classes that implements this interface is
responsible for deciding how the product should be written.
@param tsproduct the TSProduct to write.
*/
public boolean writeTSProduct(TSProduct tsproduct);

}
