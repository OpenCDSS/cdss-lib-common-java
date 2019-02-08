// BorderJPanel - Panel with a border for AWT

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
// BorderJPanel - Panel with a border for AWT
//-----------------------------------------------------------------------------
// Copyright: See the COPYRIGHT file.
//-----------------------------------------------------------------------------
// History: 
//
// 2002-01-29	Steven A. Malers, RTi	Initial test to help with tabbed pane.
//					Rely on base class for most things.
//-----------------------------------------------------------------------------
// End Header

package RTi.Util.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
The BorderJPanel class draws a black line around the edge of a standard JPanel.
All other functionality is inherited from the JPanel parent class.
Instances of BorderJPanel are useful for use with TabbedPane because the
TabbedPane itself does not draw a line around the body of the tabs (only the top).<p>
TODO (JTS - 2003-11-14) this class can probably be eliminated in favor of using Swing's BorderFactory.
*/
@SuppressWarnings("serial")
public class BorderJPanel extends JPanel 
{

/**
Constructor.
*/
public BorderJPanel ( ) 
{	super () ;
}

/**
Paints the panel on the current graphics context.
@param g the Graphics context on which to draw the panel.
*/
public void paint ( Graphics g ) 
{	g.setColor ( Color.black );
	Dimension d = getSize ();
	// Draw an edge..
	g.drawRect ( 0, 0, d.width - 1, d.height - 1 );
	super.paint ( g );
}

} // End BorderJPanel class
