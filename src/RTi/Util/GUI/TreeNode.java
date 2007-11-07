//-----------------------------------------------------------------------------
// TreeNode - primitive data for Tree component
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Original version, from HDF package (see
//					copyright information below).
//-----------------------------------------------------------------------------
/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/
/**
------------------------------------------------------------------

Copyright Notice and Statement for NCSA Hierarchical Data Format (HDF) 
Software Library and Utilities

Copyright 1988-2001 The Board of Trustees of the University of Illinois

All rights reserved.

Contributors:   National Center for Supercomputing Applications 
(NCSA) at the University of Illinois, Fortner Software, Unidata 
Program Center (netCDF), The Independent JPEG Group (JPEG), 
Jean-loup Gailly and Mark Adler (gzip), and Digital Equipment 
Corporation (DEC). Macintosh support contributed by Gregory L. Guerin.


The package 'glguerin':
Copyright 1998, 1999 by Gregory L. Guerin.
Redistribute or reuse only as described below.
These files are from the MacBinary Toolkit for Java:
   <http://www.amug.org/~glguerin/sw/#macbinary>
and are redistributed by NCSA with permission of the 
author.

Redistribution and use in source and binary forms, with or without 
modification, are permitted for any purpose (including commercial 
purposes) provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright 
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright 
notice, this list of conditions, and the following disclaimer in the 
documentation and/or materials provided with the distribution.

3. In addition, redistributions of modified forms of the source or 
binary code must carry prominent notices stating that the original 
code was changed and the date of the change.

4. All publications or advertising materials mentioning features or use 
of this software must acknowledge that it was developed by the National 
Center for Supercomputing Applications at the University of Illinois, 
and credit the Contributors.

5. Neither the name of the University nor the names of the Contributors 
may be used to endorse or promote products derived from this software 
without specific prior written permission from the University or the 
Contributors.

DISCLAIMER

THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND THE CONTRIBUTORS "AS IS" 
WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event 
shall the University or the Contributors be liable for any damages 
suffered by the users arising out of the use of this software, even if 
advised of the possibility of such damage. 
*/

//package ncsa.hdf.util;
package RTi.Util.GUI;

import java.awt.Image;

/**
 * A TreeNode class was written by Sandip Chitale. This class will be used as
 * a base clas to implemnt a node of the tree data structres like heirarchical
 * file systems.
 *
 * @author  HDF Group, NCSA. Modified by Peter Cao, Septemper 10, 1998.
 */
public class TreeNode
{
    /** the node label */
    String label;

    /** the node object defined by users */
    Object userObject;

    /** the default icon */
    Image defaultIcon;

    /** the open folder icon */
    Image expandedIcon;

    /** the icon for leaf node */
    Image leafIcon;

    /** the level of the node in the tree */
    int level;

    /** if the node is expanded */
    boolean isExpanded;

    /**
     * creates a new tree node
     * @param obj            the node object
     * @param defaultImage   the default image
     * @param expandedImage  the expanded image
     */
    public TreeNode(Object obj, Image defaultImage, Image expandedImage)
    {
        userObject = obj;
        defaultIcon = defaultImage;
        expandedIcon = expandedImage;
        leafIcon = defaultImage;
        level = -1;
        isExpanded = false;

        if (userObject != null) label = userObject.toString();
        else label = "";
    }

    /**
     * create the tree node with specified node object and the default image
     * @param obj          the node object
     * @param defaultImage the default image
     */
    public TreeNode(Object obj ,Image defaultImage)
    {
        this(obj, defaultImage, null);
    }

    /**
     * adds the node into the tree, derived class should override it
     */
    public void added() { }

    /**
     * deletes the node from the tree, derived class should override it
     */
    public void deleted() { }

    /**
     * selects the node in the tree, derived class should override it
     */
    public void select() {}

    /**
     * expands or collapses the node, derived class should override it
     */
    public  void  expandCollapse()
    {
        if (isExpandable()) toggleExpanded();
    }

    /** checks if the node is expandable */
    public  boolean  isExpandable() {
        return(!(expandedIcon == null));
    }

    /**
     * set the expand image
     *
     * @param expandedImage the expanded image
     */
    public  void  setExpandable(Image expandedImage) {
        expandedIcon = expandedImage;
    }

    /** check if the node is expanded */
    public  boolean  isExpanded() {
        return isExpanded;
    }

    /**
     * set the expanded status
     *
     * @param b  indicator if the node is expanded
     */
    public  void  setExpanded(boolean b) {
      if (isExpandable()) isExpanded = b;
    }

    public  void toggleExpanded() {
      if (isExpanded()) setExpanded(false);
      else setExpanded(true);
    }

    /** get the node object */
    public Object getObject() {
        return userObject;
    }

    /** get the default image */
    public  Image  getDefaultImage() {
        return defaultIcon;
    }

    /** set the default image */
    public  void  setDefaultImage(Image defaultImage)
    {
        defaultIcon = defaultImage;
    }

    /** set the leaf image */
    public  void  setLeafImage(Image img)
    {
        leafIcon = img;
    }

    /** get the leaf image */
    public  Image  getLeafImage()
    {
        return leafIcon;
    }

    /** get the node level */
    public int getLevel()
    {
        return level;
    }

    /** set the level of the node
     * @param level the level value of the node 
     */
    public void setLevel(int level)
    {
        this.level = level;
    }

    /** sets the label of the node
     *  @param label the label
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /** gets the label of the node
     */
    public String getLabel() { return label; }

    /** get the collapse image(expanded).
     *@return the image
     */
    public  Image getCollapseImage()
    {
        return expandedIcon;
    }

    /**
     * Returns the parameter string representing the state of this 
     * node. This string is useful for debugging.
     * @return     the parameter string of this node.
     */
    protected String paramString() {
	return "isExpandable="+isExpandable()+
            ",isExpanded="+isExpanded() +
            ",level="+level;
    }

    /**
     * Returns a string representation of this node and its values.
     * @return    a string representation of this node.
     */
    public String toString() {
	return getClass().getName() + "[" + paramString() + "]";
    }

	public Object getUserObject() { return userObject; }
}
