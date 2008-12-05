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
