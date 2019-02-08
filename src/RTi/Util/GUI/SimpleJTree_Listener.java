// SimpleJTree_Listener - a listener for classes that need to respond to JTree events

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

//-----------------------------------------------------------------------------
// SimpleJTree_Listener - a listener for classes that need to respond to 
//	JTree events.
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2004-07-06	J. Thomas Sapienza, RTI	Initial version
//-----------------------------------------------------------------------------

package RTi.Util.GUI;

/**
This class is an interface for classes that need to respond to JTree events.
Currently it only has one method in it -- there wasn't enough budget to spend
time adding all the possibilities, so they should be added as needed.
*/
public interface SimpleJTree_Listener {

/**
Called when a node is expanded in the tree.
@param node the node that is being expanded.
*/
public void nodeExpanding(SimpleJTree_Node node);

}
