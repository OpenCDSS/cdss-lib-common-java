// Node - a representation of a node on network

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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

package RTi.Util.IO;

import java.lang.String;

/**
This Node class represents a node on a network, for use with the NodeNetwork class.
REVISIT JAVADOC: see NodeNetwork
*/
public class Node extends NetworkFeature
{

// REVISIT - figure out better how used.
/**
Indiate how upstream nodes are constructed.
The default is to add tributaries first (like makenet) but CWRAT adds the main stem first.
*/
public static final int TRIBS_ADDED_FIRST	= 1;
public static final int TRIBS_ADDED_LAST	= 2;

// REVISIT - why needed?
//private int		_node_in_reach_number;	// The node count in the reach (starting at one)
// REVISIT - just use the Vector size.
//private int		_nupstream;		// number of upstream nodes

// REVISIT - why needed?
//private int		_tributary_number;	// The reach number of tribs to
						// the parent stream (starting
						// one).  In other words, if
						// the downstream node has
						// multiple upstream nodes, this
						// is the counter for those
						// nodes.  That allows a search
						// coming from upstream to know
						// which reach it is coming
						// from.  Mainly important on
						// nodes above a confluence.
// REVISIT - why needed?
//private int		_reach_level;		// The level of the river with
						// 1 being the main stem
// REVISIT - why needed?
//private int		_reach_counter;		// The reach number counting
						// the total number of streams
						// in the sytem.  Therefore the
						// first reach in the system is
						// 1 the next reach added is
						// 2, etc.
// REVISIT - why needed?
//private int		_serial;		// serial integer used to keep
						// a running count of the nodes
						// in the network.
// REVISIT - why needed?
// private int		_computational_order;	// Computational order of nodes,
						// with 1 being most upstream.
						// This generally has to be
						// set after the entire network
						// has been populated.
//private double		__x = 0.0;		// X-coordinate (for
						// visualization).
//private double		__y = 0.0;		// Y-coordinate (for
						// visualization).

// REVISIT - why needed?
//private int		_upstream_order;	// See TRIBS* above.

/**
Construct a node.
@param id Node identifier.
@param name Node name.
@param type Node type.
*/
public Node ( String id, String name, String type ) {
	super ( id, name, type );
}

}