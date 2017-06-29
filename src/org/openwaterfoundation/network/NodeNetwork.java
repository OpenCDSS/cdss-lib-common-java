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
