// NodeNetwork - interface to link network with TSTool command processor

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

package org.openwaterfoundation.network;

/**
 * Interface to represent a node network.
 * This is a very basic interface to allow the TSCommmandProcessor to generically manage node networks.
 * TODO sam 2017-05-31 need to enable a more robust behavior for network but for now rely on casting where necessary.
 * @author sam
 *
 */
public interface NodeNetwork {
	
	/**
	 * Return the identifier for the network, typically a short string.
	 * @return network ID
	 */
	public String getNetworkId ();
	
	/**
	 * Return the name for the network, typically a sentence-long description.
	 * @return network name
	 */
	public String getNetworkName ();

}
