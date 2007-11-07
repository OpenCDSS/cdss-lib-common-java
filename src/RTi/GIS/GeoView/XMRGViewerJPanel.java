// ----------------------------------------------------------------------------
// XMRGViewJPanel - a JPanel for displaying the contents of a XMRG file.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-10-14	J. Thomas Sapienza, RTi	Initial version.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import RTi.GIS.GeoView.GeoGrid;
import RTi.GIS.GeoView.XmrgGridLayer;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class is a JPanel that can be plugged into a JFrame to display the 
contents of an XMRG file.
*/
public class XMRGViewerJPanel 
extends JPanel {

/**
The worksheet that displays the XMRG data.
*/
private JScrollWorksheet __worksheet = null;

/**
The name of the file being displayed.
*/
private String __xmrgFilename = null;

/**
The XMRG class created from the file, for pulling out additional data.
*/
private XmrgGridLayer __xmrg = null;

/**
Constructor.  Opens with no file.
*/
public XMRGViewerJPanel() {
	this(null);
}

/**
Constructor.  Opens the specified file.
@param xmrgFilename the file to open.
*/
public XMRGViewerJPanel(String xmrgFilename) {
	setup();
}

/**
Opens an XMRG file.
@param filename the file to open.
*/
public void openFile(String filename) {
	removeAll();
	setLayout(new GridBagLayout());

	try {
		__xmrgFilename = filename;
		__xmrg = new XmrgGridLayer(__xmrgFilename, true, true);
	
		XMRGViewerTableModel model = new XMRGViewerTableModel(__xmrg);
		XMRGViewerCellRenderer renderer = new XMRGViewerCellRenderer(
			model);
		GeoGrid grid = __xmrg.getGrid();

		PropList props = new PropList("Worksheet");
		props.set("JWorksheet.ShowRowHeader=true");
		props.set("JWorksheet.FirstRowNumber=" 
			+ grid.getMaxRow());
		props.set("JWorksheet.AllowCopy=true");
		props.set("JWorksheet.IncrementRowNumbers=false");

		__worksheet = new JScrollWorksheet(renderer, model, props);

		JGUIUtil.addComponent(this, new JLabel("Saved date: " ),
			0, 0, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		JGUIUtil.addComponent(this, new JLabel(""
			+ __xmrg.getSavedDate()),
			1, 0, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.WEST);

		JGUIUtil.addComponent(this, new JLabel("Valid date: " ),
			0, 1, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		JGUIUtil.addComponent(this, new JLabel(""
			+ __xmrg.getValidDate()),
			1, 1, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.WEST);

		JGUIUtil.addComponent(this, new JLabel("Max Header Value : " ),
			0, 2, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.EAST);
		JGUIUtil.addComponent(this, new JLabel(""
			+ __xmrg.getMaxValueHeader()),
			1, 2, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.WEST);

		JGUIUtil.addComponent(this, __worksheet,
			0, 3, 3, 3, 1, 1,
			GridBagConstraints.BOTH, GridBagConstraints.CENTER);
	}
	catch (Exception e) {
		JGUIUtil.addComponent(this, 
			new JLabel("No XMRG file read."),
			0, 0, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
		Message.printWarning(2, "", e);
		e.printStackTrace();
	}	
}

/**
Sets up the layout of the panel.
*/
private void setup() {
	setLayout(new GridBagLayout());
	
	if (__xmrgFilename == null) {
		JGUIUtil.addComponent(this, 
			new JLabel("No XMRG file read."),
			0, 0, 1, 1, 1, 1,
			GridBagConstraints.NONE, GridBagConstraints.NORTHWEST);
	}
	else {
		openFile(__xmrgFilename);
	}
}

}
