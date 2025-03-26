// TreePanel - tree component

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
// TreePanel - Tree component
//-----------------------------------------------------------------------------
// History:
//
// 2001-12-13	Steven A. Malers, RTi	Original version, from HDF package (see
//					copyright information below).
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
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
/*
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
package RTi.Util.GUI.unused;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.List;
import java.util.Vector;
import java.lang.reflect.Method;

/**
The TreePanel class displays a tree data structures like hierarchical file systems.
*/
@SuppressWarnings("serial")
public class TreePanel extends Panel
    implements MouseListener, AdjustmentListener
{
    /** the size of tree node */
    public static final int CELLSIZE = 30;

    /** offset of cell node */
    public static final int CELLOFFSET = CELLSIZE/2;

    /** margin of image */
    public static final int IMAGEMARGIN = 2;

    /** size of icon for the node */
    public static final int IMAGESIZE = CELLSIZE-(2 * IMAGEMARGIN);
    public static final int TRIGGERMARGIN = 10;
    public static final int TRIGGERSIZE = CELLSIZE-(2 *TRIGGERMARGIN);

    /** the list to store the whole tree node */
    List<TreeNode> treeVector;

    /** the list to hold the displayed tree node */
    public List<TreeNode> displayedVector;

    /** the current selected tree node */
    public TreeNode selectedNode;

    /** the maximum width of the panel */
    int treeWidth = -1;

    // Scrolling related stuff.
    public int treeVOffset=0; 
	public int treeHOffset=0;
    Scrollbar treeVScrollbar, treeHScrollbar;

    // variables for double-buffer
    Image offScreenImage = null;
    Graphics offGraphics;

    // shift variables
    int tx=0, ty=0;

    /** the index of the node which the mouse points to */
    int nodeIndex = 0;

    /** create new HDF tree */
    public TreePanel() { init(); }

    /** initialize some of variables related to the HDF Tree */
    public void  init()
    {
        treeVector = new Vector<TreeNode>();
        displayedVector = new Vector<TreeNode>();
        selectedNode = null;

        // add vertical & horizontal scroll bar
        addScrollbar();
        addMouseListener(this);
    }

    /** add scrollbar to canvas */
    public void addScrollbar()
    {
        setLayout(new BorderLayout());

        // add Scrollbars
        add("East",  treeVScrollbar = new Scrollbar(Scrollbar.VERTICAL));
        treeVScrollbar.addAdjustmentListener(this);
        add("South",  treeHScrollbar = new Scrollbar(Scrollbar.HORIZONTAL));
        treeHScrollbar.addAdjustmentListener(this);
    }

    /** add the tree node
     * @param node the HDF tree node(HDFObjectNode)
     */
    public  void addTreeNode(TreeNode node)
    {
        treeVector.add(node);
        node.added();
    }

    /** add the tree node who has a child
     * @param parent the parent of the node
     * @param node the HDF tree node
     */
    public  void  addTreeNode(TreeNode parent, TreeNode node)
    {
        int pos = treeVector.indexOf(parent);
        if (pos <0 )  return; // no parent

        if (treeVector.indexOf(node) >= 0)
            return; // node is already in the Vector */

        int lLevel = parent.getLevel();

        TreeNode lNode;
        int lCount = treeVector.size();
        int i;

        // find the position to add the node
        for (i = pos + 1; i < lCount; i++) {
            lNode = (TreeNode) treeVector.get(i);
            if (lLevel == lNode.getLevel())
                break; // the same level
        }

        // the last node
        if (i == lCount)
            treeVector.add(node);
        else
            treeVector.add(i, node);

        node.added();
    }

    /** remove a TreeNode from the tree
     * @param node the HDF tree node
     */
    public void  removeTreeNode(TreeNode node)
    {
        int pos = treeVector.indexOf(node);

        // the node is not found in the tree
        if (pos == -1) return;

        // remove the whole subtree
        removeSubTreeNodes(node);

        treeVector.remove(pos);
        node.deleted();

        if (selectedNode == node)
            selectedNode = null;
    }

    /** remove all TreeNodes from tree */
    public void  removeAllTreeNodes()
    {
        int count=treeVector.size();

        if (count != 0)
           treeVector.clear();

        treeHOffset = 0;
        treeVOffset = 0;
    }

    /** remove the subtree of a TreeNode
     * @param node the tree node
     */
    public void removeSubTreeNodes(TreeNode node)
    {
        int pos = treeVector.indexOf(node);

        // the node is not found in the tree
        if (pos == -1) return;

        int lLevel = node.getLevel();

        TreeNode lNode;
        int lNodeLevel;
        int i;

        // for the rest node
        for (i = pos + 1; i < treeVector.size();)
        {
            lNode = (TreeNode) treeVector.get(i);
            lNodeLevel = lNode.getLevel();

            if (lLevel == lNodeLevel) break;

            if (lLevel < lNodeLevel)
            {
                // remove the subnode of the tree
                treeVector.remove(i);
                lNode.deleted();

                if (selectedNode == lNode) selectedNode = null;
            }
        }
    }

    /** adjust the Scrollbar and repaint the graphics */
    public void  refresh()
    {
        /* get the displayed node */
        computedDisplayTree();

        /* have the node displayed */
    //    invalidate();

        setBounds(getLocation().x,getLocation().y,getSize().width, getSize().height);

        // repaint the graphics
        repaint();
    }

    public void  resetTreeOffset() {
        treeHOffset = 0;
        treeVOffset = 0;
    }

    /** get the displayed tree node */
    protected void computedDisplayTree()
    {

        int lCount;
        TreeNode lNode;
        int lLevel;
        int lDepth = 0;

        /* initialize the displayed vector */
        displayedVector.clear();

        lCount = treeVector.size();
        for (int i = 0; i < lCount; i++) {
            lNode = treeVector.get(i);
            lLevel = lNode.getLevel();

            // get the displayed node
            if (lLevel <= lDepth) {
                // add one node
                displayedVector.add(lNode);
                lDepth = lLevel;

                if (lNode.isExpandable() && lNode.isExpanded())
                    lDepth++;
            }
        }
    }

    public Dimension getMinimumSize() {
	return new Dimension(Math.max(10, treeWidth), getSize().height);
    }

    public Dimension getPreferredSize() {
	return new Dimension(Math.max(10, treeWidth), getSize().height);
    }

    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        //System.out.println("HDFTree.adjustmentValueChanged()");

        int arg = e.getAdjustmentType();
        Scrollbar target = (Scrollbar) e.getAdjustable();

        if (target == null) return;

        // detect the vertival scrollbar
        if (target ==  treeVScrollbar)
        {
            switch(arg)
            {
                case AdjustmentEvent.TRACK:
                case AdjustmentEvent.BLOCK_INCREMENT:
                case AdjustmentEvent.BLOCK_DECREMENT:
                case AdjustmentEvent.UNIT_DECREMENT:
                case AdjustmentEvent.UNIT_INCREMENT:
                    treeVOffset = target.getValue();
            }
            update(getGraphics());
        }

        // detect the horizontal scrollbar
        else if (target ==  treeHScrollbar)
        {
            switch(arg)
            {
                case AdjustmentEvent.TRACK:
                case AdjustmentEvent.BLOCK_INCREMENT:
                case AdjustmentEvent.BLOCK_DECREMENT:
                case AdjustmentEvent.UNIT_DECREMENT:
                case AdjustmentEvent.UNIT_INCREMENT:
                    treeHOffset = target.getValue();
            }

            tx = treeHOffset;
            setHScrollValue();
            update(getGraphics());
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        int lIndex = (y / CELLSIZE) + treeVOffset;

        if (lIndex >= displayedVector.size()) return;

        TreeNode lNode = (TreeNode) displayedVector.get(lIndex);
        selectedNode   = lNode;
        int lLevel = lNode.getLevel();
        Rectangle lRect  = new Rectangle((lLevel * CELLSIZE) + TRIGGERMARGIN,
            ((y/CELLSIZE)*CELLSIZE)+ TRIGGERMARGIN ,TRIGGERSIZE ,TRIGGERSIZE);

        // dynamically invoke the methods of TreeNode subclasses
        Class nodeClass = lNode.getClass();
        Class[] params = {this.getClass()};
        Object[] args = {this};
        Method expandCollapse=null, select=null;
        try {
            expandCollapse = nodeClass.getDeclaredMethod("expandCollapse", params);
            select = nodeClass.getDeclaredMethod("select", params);
        }
        catch (Exception ex) {
		ex.printStackTrace();
	};

        if (lNode.isExpandable())
        {
            try {expandCollapse.invoke(lNode, args);}
            catch (Exception ex) {
		ex.printStackTrace();
		}
            //lNode.expandCollapse(this);
            refresh();
        }
        else if (!lRect.contains(x, y))
        {
            try {select.invoke(lNode, args);}
            catch (Exception ex) {
		ex.printStackTrace();
		}
            //lNode.select(this);
            repaint();
        }
    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

    public synchronized  void setBounds(int x, int y, int w, int h)
    {
        //System.out.println("HDFTree.setBounds()");

        super.setBounds(x, y, w, h);
        Dimension d = getSize();
        int visible = d.height/CELLSIZE;
        int treeSize = displayedVector.size();

        if (d.width > CELLSIZE)
        {
            // fix the bug that the vertical scrollbar does not roll up
            // if the visible tree size is samller than tree panel size
            // while the top part of the tree is hidden
            // Peter Cao, June 8, 1998
            //treeVScrollbar.setValues(treeVOffset,visible, 0, treeSize);
            treeVScrollbar.setValues(treeVOffset,visible, 0,
                treeVOffset+Math.max(treeSize,visible));
            treeVScrollbar.setBlockIncrement(visible);

            // resize horizontal scrollbar
            setHScrollValue();
        }
    }

    /** Adjust the Horizontal Scrollbar value by the specifyed tree width. */
    void setHScrollValue()
    {
        // get current canvas size
        int canvasWidth = getSize().width-5;

        // canvas is valid?
        if (canvasWidth <= 0) return;

        //Shift everything to the right for empty space
        if ((tx + canvasWidth) > treeWidth) {
            int newtx = treeWidth - canvasWidth;
            if (newtx < 0)  newtx = 0;
            tx = newtx;
        }

        int p = (int)(canvasWidth * 0.9);
        int m = (int)(treeWidth - (canvasWidth - p));
        treeHScrollbar.setValues(tx, p, 0, m);

        //"visible" arg to setValues() has no effect after scrollbar is visible.
        treeVScrollbar.setBlockIncrement(p);
        return;
    }

    /**
     * Paints the component.
     * @param gc the specified Graphics window
     */
    public void paint(Graphics gc)
    {
        //System.out.println("Tree.paint()");

        int lCount, i, j, k;
        int lLevel = -1;
        int lWidth;
        TreeNode lNode;
        Image lImage;

        gc.translate(-tx, -ty);

        // the following two line draw the outline box for the tree
        //gc.setColor(Color.gray);
        //gc.drawRect(0,0, getSize().width-1, getSize().height-1);

        FontMetrics lFM = gc.getFontMetrics();
        gc.setColor(Color.gray);

        lCount = displayedVector.size();
	treeWidth = 0;
        for (int ii = 0; ii < lCount; ii++)
        {
            i = ii - treeVOffset;
            lNode = (TreeNode) displayedVector.get(ii);
            lLevel = lNode.getLevel();

            if (lNode == selectedNode)
            {
                // this object node has been selected
                gc.setColor(Color.black);
                gc.fillRect(((lLevel + 2) * CELLSIZE) ,(i * CELLSIZE)
                    ,lFM.stringWidth(lNode.getLabel()) + (2 * TRIGGERMARGIN)
                    ,CELLSIZE);
            }

            gc.setColor(Color.gray);
            // draw line between nodes
            gc.drawLine((lLevel * CELLSIZE) + (CELLSIZE/2)
                  ,(i * CELLSIZE) + CELLSIZE/2
                  ,((lLevel + 1) * CELLSIZE) + CELLSIZE/2
                  ,(i * CELLSIZE) + CELLSIZE/2);

            if (ii + 1 < lCount)
            {
                // not the last node to be displayed        
                if (((TreeNode) displayedVector.get(ii + 1)).getLevel()>=lLevel)
                {
                    // if the level of the next node is not same as the current, 
                    // change to next level
                    if (((TreeNode) displayedVector.get(ii+1)).getLevel()>lLevel)
                    {
                        // draw the vertical line
                        gc.drawLine(((lLevel + 1) * CELLSIZE) + (CELLSIZE/2)
                            ,(i * CELLSIZE) + CELLSIZE/2
                            ,((lLevel + 1) * CELLSIZE) + CELLSIZE/2
                            ,((i+1) * CELLSIZE) + CELLSIZE/2);
                    }

                    for (j = ii + 1, k = -1; j < lCount; j++)
                    {
                        // same level
                        if (((TreeNode) displayedVector.get(j)).getLevel()==lLevel)
                        {
                            k = j;
                            break;
                        }
                        if (((TreeNode) displayedVector.get(j)).getLevel()<lLevel)
                            break;
                    }

                    if (j < lCount && k != -1)
                    {
                        gc.drawLine((lLevel * CELLSIZE) + (CELLSIZE/2)
                            ,(i * CELLSIZE) + CELLSIZE/2
                            ,((lLevel) * CELLSIZE) + CELLSIZE/2
                            ,((k - treeVOffset) * CELLSIZE) + CELLSIZE/2);
                    }
                }  
            }   

            // draw the rectangle
            if (lNode.isExpandable())
            {
                gc.setColor(Color.white);
                gc.fillRect((lLevel * CELLSIZE) + TRIGGERMARGIN
                    ,(i * CELLSIZE) + TRIGGERMARGIN
                    ,TRIGGERSIZE ,TRIGGERSIZE);
                gc.setColor(Color.red);
                gc.drawRect((lLevel * CELLSIZE) + TRIGGERMARGIN
                    ,(i * CELLSIZE) + TRIGGERMARGIN
                    ,TRIGGERSIZE ,TRIGGERSIZE);
                gc.setColor(Color.black);
                gc.drawLine((lLevel * CELLSIZE) + TRIGGERMARGIN 
                    ,(i * CELLSIZE) + TRIGGERMARGIN + (TRIGGERSIZE/2)
                    ,(lLevel * CELLSIZE) + TRIGGERMARGIN + TRIGGERSIZE
                    ,(i * CELLSIZE) + TRIGGERMARGIN + (TRIGGERSIZE/2));
            }

            gc.setColor(Color.black);
            if (lNode.isExpanded()) 
                lImage = lNode.getCollapseImage();
            else
            {
                if (lNode.isExpandable())
                {
                    gc.drawLine((lLevel * CELLSIZE) + TRIGGERMARGIN + (TRIGGERSIZE/2)
                      ,(i * CELLSIZE) + TRIGGERMARGIN
                      ,(lLevel * CELLSIZE) + TRIGGERMARGIN + (TRIGGERSIZE/2)
                      ,(i * CELLSIZE) + TRIGGERMARGIN + TRIGGERSIZE);
                    lImage = lNode.getDefaultImage();  
                }
                else
                    lImage = lNode.getLeafImage();
            }
      
            // draw the icon
	    if ( lImage != null ) {
            	gc.drawImage(lImage, ((lLevel + 1) * CELLSIZE) + IMAGEMARGIN
                ,(i * CELLSIZE) + IMAGEMARGIN,IMAGESIZE, IMAGESIZE, this);
	    }

            // get the selected node to be displayed correctly
            if (lNode == selectedNode) gc.setColor(Color.white);
            else gc.setColor(Color.black);
      
            // draw the string
            gc.drawString(lNode.getLabel()
                ,((lLevel + 2) * CELLSIZE) + TRIGGERMARGIN
                ,((i + 1) * CELLSIZE) - TRIGGERMARGIN);

            // current canvas width
            lWidth = ((lLevel + 2) * CELLSIZE) + lFM.stringWidth(lNode.getLabel())
               + (2 * TRIGGERMARGIN);

            if (treeWidth < lWidth)
                treeWidth = lWidth;
      
        } 

        // set the specified translated parameters 
        // and the subcomponents will be relative to this origin.
        gc.translate(tx, ty);

    }

    /**
     * Updates the component. This method is called in
     * response to a call to repaint. You can assume that
     * the background is not cleared.
     * @param g the specified Graphics window
     * @see java.awt.Component#update

       The offScreenImage size was 25 times larger than the actual size
     *
     */
    public void update(Graphics g)
    {

        Dimension d = getSize();

        // create a new image only if it is null or its size has been changed
        // do not create a new image every time. It may cause memory problems
        if (offScreenImage == null)
            offScreenImage = createImage(d.width, d.height);
        else if (offScreenImage.getWidth(this) != d.width ||
            offScreenImage.getHeight(this) != d.height)
        {
            offScreenImage.flush();
            offScreenImage = createImage(d.width, d.height);
        }

        offGraphics = offScreenImage.getGraphics();
        offGraphics.setFont(getFont());

        // paint the background on the off-screen graphics context
        offGraphics.setColor(getBackground());
        offGraphics.fillRect(0,0,d.width,d.height);
        offGraphics.setColor(getForeground());

        // draw the current frame to the off-screen
        paint(offGraphics);

        //then draw the image to the on-screen
        g.drawImage(offScreenImage, 0, 0, null);
    }
}
