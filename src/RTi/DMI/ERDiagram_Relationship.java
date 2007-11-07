// ----------------------------------------------------------------------------
// ERDiagram_Relationship - class to represent the relationship between two
//	tables.
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
//
// 2003-08-27	J. Thomas Sapienza, RTi	* Initial changelog.  
// 					* Added color coding capability.
// ----------------------------------------------------------------------------

package RTi.DMI;

import java.awt.geom.Line2D;

import RTi.GR.GRColor;
import RTi.GR.GRDrawingAreaUtil;
import RTi.GR.GRText;

/**
Class that represents the relationship between two tables, and which can
also draw the relationship.
*/
public class ERDiagram_Relationship extends DMIDataObject {

/**
Whether the relationship should be drawn in bold or not.  This is a public 
field because it will be accessed every time the mouse is pressed, so an 
accessor function could concievably affect performance.
*/
public boolean bold = false;

/**
Whether the relationships are color-coded, so that it is easier to tell which
algorithm was used to draw each one.
*/
private boolean __colorCodedRelationships;

/**
Whether the relationship is between at least one reference tables.
*/
private boolean __isReference = false;

/**
Whether the relationship is visible (should be drawn) or not.
*/
private boolean __visible = false;

/**
The first line of the relationship, from the start table out to the right.
*/
private Line2D.Double __l1;

/**
The second line of the relationship, a diagonal connecting #1 and #3.
*/
private Line2D.Double __l2;

/**
The third line of the relationship, from the left to the end table.
*/
private Line2D.Double __l3;

/**
The name of the field at which the relationship ends.
*/
private String __endField;

/**
The name of the table at which the relationship ends.
*/
private String __endTable;

/**
In a relationship involving a reference table, the name of the table that is
NOT a relationship table.  Null if both are relationship tables.
*/
private String __nonReferenceTable = "";

/**
The name of the field from which the relationship begins.
*/
private String __startField;

/**
The name of the table from which the relationship begins.
*/
private String __startTable;

/**
Constructor.
@param startTable the name of the table that the relationship starts at.
@param startField the name of the field that the relationship starts at.
@param endTable the name of the table that the relationship ends at.
@param endField the name of the field that the relationship ends at.
*/
public ERDiagram_Relationship(String startTable, String startField,
String endTable, String endField) {
	__colorCodedRelationships = false;
	
	__startTable = startTable;
	__startField = startField;
	__endTable = endTable;
	__endField = endField;

	__l1 = new Line2D.Double();
	__l2 = new Line2D.Double();
	__l3 = new Line2D.Double();
}

/**
Returns true if any of the lines in the relationship intersect with the given
point or not.  This actually checks a 5x5 square formed around the specified
point for an intersection, so that users can click near a relationship line
and select it.
@param x the x location to check.
@param y the y location to check.
@return true if the point is contained, false otherwise.
*/
public boolean contains(double x, double y) {
	if (__l1.intersects(x - 2, y - 2, 5, 5)) {
		return true;
	}
	if (__l2.intersects(x - 2, y - 2, 5, 5)) {
		return true;
	}
	if (__l3.intersects(x - 2, y - 2, 5, 5)) {
		return true;
	}
	return false;
}

/**
Draws the relationship on the specified drawing area.
@param da the drawing area on which to draw the relationship.
@param tables the array of all the tables in the er diagram.
*/
public void draw(ERDiagram_DrawingArea da, ERDiagram_Table[] tables) {
	boolean grey = false;
	ERDiagram_Table start = getTableByName(__startTable, tables);
	ERDiagram_Table end = getTableByName(__endTable, tables);
	
	if (start == null || end == null) {
		return;
	}

	if (!start.isVisible() || !end.isVisible()) {
		return;
	}

	// if the "ending" table is left of the "starting" table, 
	// the "starting" and "ending" tables will be swapped.  Originally,
	// the starting table was the parent and the ending table was
	// the child of the parent-child table relationship.  Now that
	// information isn't worried about.
	if (end.getX() < start.getX()) {
		String stemp = new String(__startField);
		__startField = new String(__endField);
		__endField = new String(stemp);

		stemp = new String(__startTable);
		__startTable = new String(__endTable);
		__endTable = new String(stemp);	

		start = getTableByName(__startTable, tables);
		end = getTableByName(__endTable, tables);
	}

	double sxl = start.getX();
	double sxr = start.getX() + start.getWidth() 
		+ start.getDropShadowOffset();

	double exl = end.getX();
	double exr = end.getX() + end.getWidth() 
		+ end.getDropShadowOffset();

	double sy = start.getFieldY(da, __startField, GRText.CENTER_Y);
	double ey = end.getFieldY(da, __endField, GRText.CENTER_Y);

	double extend = 20;

	if (!grey) {
		GRDrawingAreaUtil.setColor(da, GRColor.black);
	}
	else {
		GRDrawingAreaUtil.setColor(da, GRColor.lightGray);
	}

	if (__isReference) {
		if (__nonReferenceTable == null) {
			// both tables are reference tables (probably will never
			// happen)
			return;
		}
		else if (__nonReferenceTable.equals(__startTable)) {
			GRDrawingAreaUtil.drawLine(da, sxr, sy, sxr + extend, 
				sy);
			GRDrawingAreaUtil.drawText(da, "(" + __endTable 
				+ "." + __endField + ")", 
				sxr + extend, sy - 3, 0, 0);
		}
		else {
			GRDrawingAreaUtil.drawLine(da, exr + extend, ey, exr, 
				ey);
			GRDrawingAreaUtil.drawText(da, "(" + __startTable 
				+ "." + __startField + ")",
				exr + extend, ey - 3, 0, 0);
		}
		return;
	}

	// REVISIT (JTS - 2003-08-27)
	// the drawing code can be done better, I think.
	
	// both the right drawing points are the same, so draw
	// a line to the right of the first table up or down to the
	// second, and then to the left
	//
	//   TABLE1 ---+
	//             |
	//             |
	//   TABLE2 ---+
	//
	// Color code: yellow
	if (sxr == exr) {
		if (__colorCodedRelationships) {
			GRDrawingAreaUtil.setColor(da, GRColor.yellow);
		}
		GRDrawingAreaUtil.fillRectangle(da, sxr, sy - 2, 4, 4);
		GRDrawingAreaUtil.fillRectangle(da, exr, ey - 2, 4, 4);
	
		GRDrawingAreaUtil.drawLine(da, sxr, sy, sxr + extend, sy);
		__l1.x1 = sxr;
		__l1.x2 = sxr + extend;
		__l1.y1 = sy;
		__l1.y2 = sy;
		GRDrawingAreaUtil.drawLine(da, sxr + extend,sy,exr + extend,ey);
		__l2.x1 = sxr + extend;
		__l2.x2 = exr + extend;
		__l2.y1 = sy;
		__l2.y2 = ey;
		GRDrawingAreaUtil.drawLine(da, exr + extend, ey, exr, ey);
		__l3.x1 = exr + extend;
		__l3.x2 = exr;
		__l3.y1 = ey;
		__l3.y2 = ey;
	}	
	// both the left drawing points are the same, so draw a line out
	// to the left of the first table, up or down to the second,
	// and then back to the right
	//
	//   +--- TABLE1
	//   | 
	//   | 
	//   +--- TABLE2
	//
	// Color code: red
	else if (sxl == exl) {
		if (__colorCodedRelationships) {
			GRDrawingAreaUtil.setColor(da, GRColor.red);
		}	
		GRDrawingAreaUtil.fillRectangle(da, sxl - 4, sy - 2, 4, 4);
		GRDrawingAreaUtil.fillRectangle(da, exl - 4, ey - 2, 4, 4);
	
		GRDrawingAreaUtil.drawLine(da, sxl, sy, sxl - extend, sy);
		__l1.x1 = sxl;
		__l1.x2 = sxl - extend;
		__l1.y1 = sy;
		__l1.y2 = sy;
		GRDrawingAreaUtil.drawLine(da, sxl - extend,sy,exl - extend,ey);
		__l2.x1 = sxl - extend;
		__l2.x2 = exl - extend;
		__l2.y1 = sy;
		__l2.y2 = ey;
		GRDrawingAreaUtil.drawLine(da, exl - extend, ey, exl, ey);
		__l3.x1 = exl - extend;
		__l3.x2 = exl;
		__l3.y1 = ey;
		__l3.y2 = ey;
	}		
	// draw a line out the right side of the first table, then diagonally
	// towards the second table, then draw a line left into the second
	// table
	//
	//   TABLE1 ---+
	//              \
	//               \
	//                +--- TABLE2
	//
	// Color code: green
	else if (exl > (extend * 1.5) + sxr) {
		if (__colorCodedRelationships) {
			GRDrawingAreaUtil.setColor(da, GRColor.green);
		}		
		GRDrawingAreaUtil.fillRectangle(da, sxr, sy - 2, 4, 4);
		GRDrawingAreaUtil.fillRectangle(da, exl - 4, ey - 2, 4, 4);
	
		GRDrawingAreaUtil.drawLine(da, sxr, sy, sxr + extend, sy);
		__l1.x1 = sxr;
		__l1.x2 = sxr + extend;
		__l1.y1 = sy;
		__l1.y2 = sy;
		GRDrawingAreaUtil.drawLine(da, sxr + extend,sy,exl - extend,ey);
		__l2.x1 = sxr + extend;
		__l2.x2 = exl - extend;
		__l2.y1 = sy;
		__l2.y2 = ey;
		GRDrawingAreaUtil.drawLine(da, exl - extend, ey, exl, ey);
		__l3.x1 = exl - extend;
		__l3.x2 = exl;
		__l3.y1 = ey;
		__l3.y2 = ey;
	}
	// if the tables are not perfectly aligned with each other (i.e., 
	// neither their left sides or right sides are even), but they are
	// still close to each other, avoid drawing diagonal lines (which
	// are messy) and draw a line out the left side of one and into the
	// left side of the other
	//
	//     +---- TABLE1
	//     |
	//     |
	//     +-- TABLE2
	//
	// Color code: blue
	else if (exl <= (extend * 1.5) + sxr) {
		if (__colorCodedRelationships) {
			GRDrawingAreaUtil.setColor(da, GRColor.blue);
		}			
		if (Math.abs(exl - sxl) < Math.abs(exr - sxr)) {
		double minX = 0;
		if (sxl < exl) {
			minX = sxl - extend;
		}
		else {
			minX = exl - extend;
		}
		GRDrawingAreaUtil.fillRectangle(da, sxl - 4, sy - 2, 4, 4);
		GRDrawingAreaUtil.fillRectangle(da, exl - 4, ey - 2, 4, 4);
	
		GRDrawingAreaUtil.drawLine(da, sxl, sy, minX, sy);
		__l1.x1 = sxr;
		__l1.x2 = minX;
		__l1.y1 = sy;
		__l1.y2 = sy;
		GRDrawingAreaUtil.drawLine(da, minX, sy, minX, ey);
		__l2.x1 = minX;
		__l2.x2 = minX;
		__l2.y1 = sy;
		__l2.y2 = ey;
		GRDrawingAreaUtil.drawLine(da, minX, ey, exl, ey);
		__l3.x1 = minX;
		__l3.x2 = exl;
		__l3.y1 = ey;
		__l3.y2 = ey;
		}
		else {
		double farX = 0;
		if (sxr > exr) {
			farX = sxr + extend;
		}
		else {
			farX = exr + extend;
		}
		GRDrawingAreaUtil.fillRectangle(da, sxr, sy - 2, 4, 4);
		GRDrawingAreaUtil.fillRectangle(da, exr, ey - 2, 4, 4);
	
		GRDrawingAreaUtil.drawLine(da, sxr, sy, farX, sy);
		__l1.x1 = sxr;
		__l1.x2 = farX;
		__l1.y1 = sy;
		__l1.y2 = sy;
		GRDrawingAreaUtil.drawLine(da, farX, sy, farX, ey);
		__l2.x1 = farX;
		__l2.x2 = farX;
		__l2.y1 = sy;
		__l2.y2 = ey;
		GRDrawingAreaUtil.drawLine(da, farX, ey, exr, ey);
		__l3.x1 = farX;
		__l3.x2 = exl;
		__l3.y1 = ey;
		__l3.y2 = ey;
		}		
	}
	else {
		// this case shouldn't come up ... 
	}

	// if the line was clicked on, fake a "bold" line by drawing 4 lines:
	// 1) the original line (drawn above)
	// 2) a line drawn exactly 1 pixel to the right of the original
	// 3) a line drawn exactly 1 pixel higher than the original
	// 4) a line drawn exactly 1 pixel to the right and higher than the
	// 	original
	if (bold) {
		GRDrawingAreaUtil.drawLine(da, 
			__l1.x1 + 1, __l1.y1, __l1.x2 + 1, __l1.y2);
		GRDrawingAreaUtil.drawLine(da, 
			__l2.x1 + 1, __l2.y1, __l2.x2 + 1, __l2.y2);
		GRDrawingAreaUtil.drawLine(da, 
			__l3.x1 + 1, __l3.y1, __l3.x2 + 1, __l3.y2);

		GRDrawingAreaUtil.drawLine(da, 
			__l1.x1 + 1, __l1.y1 + 1, __l1.x2 + 1, __l1.y2 + 1);
		GRDrawingAreaUtil.drawLine(da, 
			__l2.x1 + 1, __l2.y1 + 1, __l2.x2 + 1, __l2.y2 + 1);
		GRDrawingAreaUtil.drawLine(da, 
			__l3.x1, __l3.y1 + 1, __l3.x2, __l3.y2 + 1);

		GRDrawingAreaUtil.drawLine(da, 
			__l1.x1, __l1.y1 + 1, __l1.x2, __l1.y2 + 1);
		GRDrawingAreaUtil.drawLine(da, 
			__l2.x1, __l2.y1 + 1, __l2.x2, __l2.y2 + 1);
		GRDrawingAreaUtil.drawLine(da, 
			__l3.x1, __l3.y1 + 1, __l3.x2, __l3.y2 + 1);
	}
}

/**
Checks to see whether the relationship ends with the specified table.
@param table the name of the table.
@return if the relationship ends with the specified table, otherwise false.
*/
public boolean endsWithTable(String table) {
	if (table.equalsIgnoreCase(__endTable)) {
		return true;
	}
	return false;
}

/**
Checks to see whether the relationship ends with the specified table and
field.
@param table the name of the table.
@param field the name of the field.
@return if the relationship ends with the specified table and field, 
otherwise false.
*/
public boolean endsWithTableField(String table, String field) {
	if (table.equalsIgnoreCase(__endTable)) {
		if (field.equalsIgnoreCase(__endField)) {
			return true;
		}
	}
	return false;
}

/**
Returns whether the relationships are color-coded for debugging.
@return whether the relationships are color-coded for debugging.
*/
public boolean getColorCodeRelationships() {
	return __colorCodedRelationships;
}

/**
Returns the end field name.
@return the end field name.
*/
public String getEndField() {
	return __endField;
}

/**
Returns the end table name.
@return the end table name.
*/
public String getEndTable() {
	return __endTable;
}

/**
In the case of a reference relationship, returns the name of the table
that is NOT a reference table.
@return the name of the table that is not a reference table.
*/
public String getNonReferenceTable() {
	return __nonReferenceTable;
}

/**
Returns the start field name.
@return the start field name.
*/
public String getStartField() {
	return __startField;
}

/**
Returns the start table name.
@return the start table name.
*/
public String getStartTable() {
	return __startTable;
}

/**
Finds the table in the array with the specified name and returns it.
@param name the name of the table to find.
@param tables array of tables.
@return the table with the specified name, or null if it can't be found.
*/
private ERDiagram_Table getTableByName(String name, ERDiagram_Table[] tables) {
	for (int i = 0; i < tables.length; i++) {
		if (tables[i].getName().equals(name)) {			
			return tables[i];
		}
	}
	return null;
}	

/**
Checks to see whether the relationship involves the specified table.
@param table the name of the table to check for.
@return true if the table starts with or ends with the specified table.
*/
public boolean involvesTable(String table) {
	if (table.equalsIgnoreCase(__startTable)) {
		return true;
	}
	if (table.equalsIgnoreCase(__endTable)) {
		return true;
	}
	return false;
}

/**
Whether this relationship involves reference tables.
@return whether this relationship involves reference tables.
*/
public boolean isReference() {
	return __isReference;
}

/**
Returns whether the relationship is visible or not.
@return whether the relationship is visible or not.
*/
public boolean isVisible() {
	return __visible;
}

/**
Sets whether the relationships should be color-coded (for use in debugging to
tell which algorithm was used to draw each.
@param code whether to color code the relationships.
*/
public void setColorCodeRelationships(boolean code) {
	__colorCodedRelationships = code;
}

/**
Sets the name of the table in the relationship (if one is a reference table)
that is NOT a reference table.  If both tables in the relationship are reference
tables, this will be null.
@param nonReferenceTable the name of the table in a reference relationship
that is not a reference table.
*/
public void setNonReferenceTable(String nonReferenceTable) {
	__nonReferenceTable = nonReferenceTable;
}

/**
Sets whether this relationship involved any reference tables.
@param isReference whether this relationship involves any reference tables.
*/
public void setReference(boolean isReference) {	
	__isReference = isReference;
}

/**
Sets whether the relationship is visible or not.
@param visible whether the relationship is visible or not.
*/
public void setVisible(boolean visible) {
	__visible = visible;
}

/**
Checks to see whether the relationship starts with the specified table.
@param table the name of the table.
@return if the relationship starts with the specified table, otherwise false.
*/
public boolean startsWithTable(String table) {
	if (table.equalsIgnoreCase(__startTable)) {
		return true;
	}
	return false;
}

/**
Checks to see whether the relationship starts with the specified table and
field.
@param table the name of the table.
@param field the name of the field.
@return if the relationship starts with the specified table and field, 
otherwise false.
*/
public boolean startsWithTableField(String table, String field) {
	if (table.equalsIgnoreCase(__startTable)) {
		if (field.equalsIgnoreCase(__startField)) {
			return true;
		}
	}
	return false;
}

/**
Returns a String representation of this relationship.
@return a String representation of this relationship.
*/
public String toString() {
	return __startTable + "." + __startField + "\t->\t" + __endTable 
		+ "." + __endField;
}

}
