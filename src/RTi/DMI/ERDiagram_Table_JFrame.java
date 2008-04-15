// ----------------------------------------------------------------------------
// ERDiagram_Table_JFrame - a class for displaying ERDiagram information in a
//	JWorksheet.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-03-05	JTS, RTi		Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.util.Vector;

import javax.swing.JFrame;

import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.JScrollWorksheet;

import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;

/**
This class is a simple JFrame that will open a JWorksheet and display
ERDiagram information in it.  It was originally written to give Morgan 
information about how to setup a JWorksheet and work with it, so it 
reads like a tutorial.
*/
public class ERDiagram_Table_JFrame extends JFrame {

/**
Constructor.
@param tables the Vector of tables to display in the worksheet.
*/
public ERDiagram_Table_JFrame(Vector tables) {
	setupGUI(tables);
}

/**
Sets up the GUI.
*/
private void setupGUI(Vector tables) {
	String routine = "setupGUI";

	// AML:
	// there are a lot of settings that can be configured with the
	// worksheet's proplist.  These are most of them.
	//
	// All the details are listed on the JWorksheet javadocs:
	// file:///i:/develop/javadoc_140/RTi/Util/GUI/JWorksheet.html

	// the proplist details are here:
	// file:///i:/develop/javadoc_140/RTi/Util/GUI/JWorksheet.html#JWorksheet(RTi.Util.GUI.JWorksheet_DefaultTableCellRenderer,%20RTi.Util.GUI.JWorksheet_AbstractTableModel,%20RTi.Util.IO.PropList)

	// (or, you can click on the Second constructor listed in the javadocs
	PropList p = new PropList("ERDiagram_Table_JFrame.JWorksheet");
	p.add("JWorksheet.CellFont=Courier");
	p.add("JWorksheet.CellStyle=Plain");
	p.add("JWorksheet.CellSize=11");
	p.add("JWorksheet.HeaderFont=Arial");
	p.add("JWorksheet.HeaderStyle=Plain");
	p.add("JWorksheet.HeaderSize=11");
	p.add("JWorksheet.HeaderBackground=LightGray");
	p.add("JWorksheet.ShowRowHeader=true");
	p.add("JWorksheet.ShowPopupMenu=true");
	p.add("JWorksheet.SelectionMode=ExcelSelection");
	p.add("JWorksheet.AllowCopy=true");

	JWorksheet worksheet = null;
	JScrollWorksheet jsw = null;

	int[] widths = null;
	try {
		ERDiagram_Table_TableModel tmr = new
			ERDiagram_Table_TableModel(tables);
		ERDiagram_Table_CellRenderer crr = new
			ERDiagram_Table_CellRenderer(tmr);
	
		jsw = new JScrollWorksheet(crr, tmr, p);

		widths = crr.getColumnWidths();
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		worksheet = new JWorksheet(0, 0, p);
	}

	worksheet = jsw.getJWorksheet();

	// AML: 
	// worksheets do some weird sizing by default (because of some stuff
	// they inherit from JTable), so the next call makes them resize
	// nicer.  If you're making a form with multiple things on it besides
	// the worksheet, you'll want to make sure to do this.  If you're
	// just making a frame that holds a worksheet, you don't need to worry
	// about it
	worksheet.setPreferredScrollableViewportSize(null);

	// AML:
	// the next call sets the JFrame that the worksheet should put
	// an hourglass on for long operations.  This is for copy/paste and
	// sorting operations.  Nothing else will take so long as to need 
	// to show the hourglass.
	worksheet.setHourglassJFrame(this);	

	// AML:
	// if you want to know every time the user clicks on the worksheet
	// (for selecting a new row, mostly), then you'll want to enable the
	// next line
//	worksheet.addMouseListener(this);	

	// AML:
	// the next call is for the same reason as the last one, but it's for
	// when the users use the arrow keys to move around on the table
//	worksheet.addKeyListener(this);

	// AML:
	// the next line enables copying FROM the worksheet with 
	// control-insert or control-c.  Pasting isn't enabled yet,
	// so don't try to paste or just pass false for the second parameter.
	//
	// there are still some bugs in copy, too, but for the most part
	// (80% of the time) it works just fine

	getContentPane().add(jsw);

	setSize(400, 400);
	pack();
	setVisible(true);

	// AML:
	// because setting the column widths requires a valid graphics context
	// (for determining the size of fonts on-screen), you can't set the 
	// column widths until AFTER the JFrame on which the table appears
	// is shown with a show() call.  
	if (widths != null) {
		worksheet.setColumnWidths(widths);
	}
}

}
