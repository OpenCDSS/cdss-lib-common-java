// NetworkLayer - class to represent a Network object as a GeoLayer

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

package RTi.GIS.GeoView;

/* TODO SAM 2007-05-09 Evaluate whether this layer will be supported
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Vector;

import RTi.GIS.GeoView.GeoLayer;
import RTi.GR.GRPoint;
import RTi.GR.GRPolygon;
import RTi.GR.GRPolygonList;
import RTi.GR.GRPolyline;
import RTi.GR.GRPolylineList;
import RTi.GR.GRPolypoint;
import RTi.GR.GRShape;
import RTi.Util.IO.EndianDataInputStream;
import RTi.Util.IO.EndianRandomAccessFile;
import RTi.Util.IO.IOUtil;
*/
import RTi.Util.IO.Network;
/*
import RTi.Util.IO.PropList;
import RTi.Util.Math.MathUtil;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DbaseDataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
*/

/**
Class to store Network in a GeoLayer, for displays in the GeoView.
*/
public class NetworkLayer extends GeoLayer
{

/**
Network object for the data layer.
*/
protected Network _network = null;

/**
Construct a layer from a Network object.
@param network Network object to construct layer from.
@exception Exception if there is an error reading the file.
*/
public NetworkLayer ( Network network )
throws Exception
{	super ( "" );
	initialize ();
	setDataFormat ( "Network" );
	_network = network;
}

/**
Return the Network associated with the layer.
*/
public Network getNetwork()
{	return _network;
}

/**
Return the data value for a shape.  The object will be a Double, or String.
Use the DataTable methods to get field formats for output.
@param index Database record for shape.
@param field Attribute table field to use for data.
@exception Exception if there is an error getting the value.
*/
/* REVISIT
public Object getShapeAttributeValue ( long index, int field )
throws Exception
{	// Get the data from the attribute table in the base class...
	return _attribute_table.getFieldValue( index, field );
}
*/

/**
Initialize the object.  For now, just set the file names.  It is assumed that
_props has been set to a non-null PropList that contains the input name.
*/
private void initialize ()
{	
}

/**
Determine whether the file is an NWSRFS layer file.  Checks are made as follows:
<ol>
<li>	If the file cannot be opened, return false.</li>
<li>	If the file can be opened and the name is one of the following
	return true: county.bin, county.dat, fg_basin.bin, fg_basin.dat,
	forecastpt.dat,
	map_basin.bin, map_basin.dat, rfc_boundary.bin, rfc_boundary.dat,
	river.bin, river.dat, state.bin, state.dat.</li>
<li>	Else, return false.</li>
</ol>
@return true if the file is an ESRIShapefile, false if not.
@param filename Name of file to check, with or without the .shp extension.
*/
/* REVISIT
public static boolean isNwsrfsFile ( String filename )
{	File file = new File ( filename );
	String name = file.getName();
	boolean check = false;
	if (	name.equals("county.bin") ||
		name.equals("county.dat") ||
		name.equals("fg_basin.bin") ||
		name.equals("fg_basin.dat") ||
		name.equals("forecastpt.dat") ||
		name.equals("map_basin.bin") ||
		name.equals("map_basin.dat") ||
		name.equals("rfc_boundary.bin") ||
		name.equals("rfc_boundary.dat") ||
		name.equals("river.bin") ||
		name.equals("river.dat") ||
		name.equals("state.bin") ||
		name.equals("state.dat") ||
		name.equals("swe_stations.dat") ) {
		check = true;
	}
	file = null;
	name = null;
	return check;
}
*/

/**
Read the file.  The attributes will be read if originally requested.
@param geodata_file Name of geo_data file to read.
@param read_attributes Indicates if the attribute table should be assigned.
@exception IOException if an error occurs.
*/
/* REVISIT
private void read ( String geodata_file, boolean read_attributes )
throws IOException
{	String message = null;
	String routine = "Nwsrfs.read";
	int dl = 50;
	byte [] buffer = null;
	byte [] buffer20 = new byte[20];

	// Check the file names and convert to an integer flag to increase
	// performance in code below...

	int filetype = 0;
	boolean binary = false;
	File file = new File ( geodata_file );
	String filename = file.getName();
	if ( filename.equals("county.dat") ) {
		filetype = COUNTY;
	}
	else if ( filename.equals("county.bin") ) {
		filetype = COUNTY;
		binary = true;
	}
	else if ( filename.equals("fg_basin.dat") ) {
		filetype = FG_BASIN;
	}
	else if ( filename.equals("fg_basin.bin") ) {
		filetype = FG_BASIN;
		binary = true;
	}
	else if ( filename.equals("forecastpt.dat") ) {
		filetype = FORECASTPT;
	}
	else if ( filename.equals("map_basin.dat") ) {
		filetype = MAP_BASIN;
	}
	else if ( filename.equals("map_basin.bin") ) {
		filetype = MAP_BASIN;
		binary = true;
	}
	else if ( filename.equals("rfc_boundary.dat") ) {
		filetype = RFC_BOUNDARY;
	}
	else if ( filename.equals("rfc_boundary.bin") ) {
		filetype = RFC_BOUNDARY;
		binary = true;
	}
	else if ( filename.equals("river.dat") ) {
		filetype = RIVER;
	}
	else if ( filename.equals("river.bin") ) {
		filetype = RIVER;
		binary = true;
	}
	else if ( filename.equals("state.dat") ) {
		filetype = STATE;
	}
	else if ( filename.equals("state.bin") ) {
		filetype = STATE;
		binary = true;
	}
	else if ( filename.equals("swe_stations.dat") ) {
		filetype = SWE_STATIONS;
	}
	filename = null;
	file = null;

	// For now, do this brute force, sharing formats as much as possible.

	double xmin = 1.0e10;
	double xmax = -1.0e10;
	double ymin = 1.0e10;
	double ymax = -1.0e10;
	double xmin_layer = 1.0e10;
	double xmax_layer = -1.0e10;
	double ymin_layer = 1.0e10;
	double ymax_layer = -1.0e10;
	String string = null;
	double x, y = 0.0;
	Vector tokens = null;
	if (	((filetype == COUNTY) || (filetype == FG_BASIN) ||
		(filetype == MAP_BASIN) ||
		(filetype == RFC_BOUNDARY) ||
		(filetype == RIVER) ||
		(filetype == STATE)) && !binary ) {
		// ASCII file containing lat/long coordinates
		// Format is ID Name Order Npts
		// Lat +Long
		// Lat +Long
		// ...
		//
		// For counties, it is -Long, lat (is this a standard???)
		//
		// For MAP layer, might want to use GRPolygonList and put
		// multiple polygons under one shape.  Or, add an attribute to
		// include the main MAP area or segment?  For now, read all the
		// areas in as separate GRPolygon.
		if ( (filetype == RIVER) || (filetype == STATE) ) {
			_shape_type = LINE;
		}
		else {	_shape_type = POLYGON;
		}
		BufferedReader in = null;
		in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( geodata_file )) );
		if ( read_attributes ) {
			Vector table_fields = new Vector (1);
			if ( filetype == COUNTY ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"ID",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"COUNTY",24) );
			}
			else if ( filetype == FG_BASIN ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"FG",24) );
			}
			else if ( filetype == MAP_BASIN ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"MAP Area",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"MAP Name",24) );
			}
			if ( filetype == RFC_BOUNDARY ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"RFC",24) );
			}
			else if ( filetype == RIVER ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"REACH",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"NAME",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_DOUBLE,
				"ORDER",4,0) );
			}
			else if ( filetype == STATE ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"Abbreviation",3) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"Name",24) );
			}
			_attribute_table = new DataTable(table_fields);
			table_fields = null;
		}
		// Header line...
		int polycount = 0;
		while ( true ) {
		xmin = 1.0e10;
		xmax = -1.0e10;
		ymin = 1.0e10;
		ymax = -1.0e10;
		string = in.readLine();
		if ( string == null ) {
			break;
		}
		tokens = StringUtil.breakStringList ( string.trim(), " ", 0 );
		int size = 0;
		if ( (tokens != null) && (tokens.size() > 3) ) {
			if ( read_attributes ) {
				// Only attribute is the name of the basin
				// boundary...
				TableRecord record = new TableRecord ( 1 );
				record.addFieldValue (
					(String)tokens.elementAt(0) );
				if ( filetype == RIVER ) {
					record.addFieldValue (
					(String)tokens.elementAt(1) );
					record.addFieldValue (
					Double.valueOf((String)
					tokens.elementAt(2)));
				}
				else if ( (filetype == STATE) ||
					(filetype == COUNTY) ||
					(filetype == FG_BASIN) ||
					(filetype == MAP_BASIN) ) {
					record.addFieldValue (
					(String)tokens.elementAt(1) );
				}
				try {	_attribute_table.addRecord ( record );
				}
				catch ( Exception e ) {
					Message.printWarning ( 2, routine, e );
				}
				record = null;
			}
			size = StringUtil.atoi((String)tokens.elementAt(3));
			GRPolygon polygon = null;
			GRPolyline polyline = null;
			if (	(filetype == RIVER) ||
				(filetype == STATE) ||
				(filetype == COUNTY) ) {
				polyline = new GRPolyline(size);
			}
			else {	polygon = new GRPolygon(size);
			}
			int ptcount = 0;
			for ( int i = 0; i < size; i++ ) {
				string = in.readLine();
				if ( string == null ) {
					break;
				}
				tokens = StringUtil.breakStringList (
					string.trim(), " ", 0 );
				if (	(tokens != null) &&
					(tokens.size() == 2) ) {
					if ( filetype == COUNTY ) {
						x = StringUtil.atod(
						(String)tokens.elementAt(0));
						y = StringUtil.atod(
						(String)tokens.elementAt(1));
					}
					else {	y = StringUtil.atod(
						(String)tokens.elementAt(0));
						x = -(StringUtil.atod(
						(String)tokens.elementAt(1)));
					}
					if (	(filetype == RIVER) ||
						(filetype == STATE) ||
						(filetype == COUNTY) ) {
						polyline.setPoint ( ptcount++,
						new GRPoint(x,y) );
					}
					else {	polygon.setPoint ( ptcount++,
						new GRPoint(x,y) );
					}
					xmin = MathUtil.min ( x, xmin );
					xmax = MathUtil.max ( x, xmax );
					ymin = MathUtil.min ( y, ymin );
					ymax = MathUtil.max ( y, ymax );
				}
			}
			if (	(filetype == RIVER) ||
				(filetype == STATE) ||
				(filetype == COUNTY) ) {
				polyline.xmin = xmin;
				polyline.ymin = ymin;
				polyline.xmax = xmax;
				polyline.ymax = ymax;
				polyline.limits_found = true;
				polyline.index = polycount++;
				_shapes.addElement ( polyline );
			}
			else {	polygon.xmin = xmin;
				polygon.ymin = ymin;
				polygon.xmax = xmax;
				polygon.ymax = ymax;
				polygon.limits_found = true;
				polygon.index = polycount++;
				_shapes.addElement ( polygon );
			}
			xmin_layer = MathUtil.min ( xmin, xmin_layer );
			xmax_layer = MathUtil.max ( xmax, xmax_layer );
			ymin_layer = MathUtil.min ( ymin, ymin_layer );
			ymax_layer = MathUtil.max ( ymax, ymax_layer );
		}
		}
		in.close();
		in = null;
		setProjection ( new GeographicProjection() );
	}
	else if (((filetype == COUNTY) || (filetype == FG_BASIN) ||
		(filetype == MAP_BASIN) || (filetype == RFC_BOUNDARY) ||
		(filetype == RIVER) || (filetype == STATE)) && binary ) {
		// Binary file containing HRAP coordinates.
		// Format is ID XXX -1 Npts
		// HRAP-X HRAP-Y
		// HRAP-X HRAP-Y
		// ...
		//
		// For MAP layer, might want to use GRPolygonList and put
		// multiple polygons under one shape.  Or, add an attribute to
		// include the main MAP area or segment?  For now, read all the
		// areas in as separate GRPolygon.
		if (	(filetype == RIVER) || (filetype == STATE) ||
			(filetype == COUNTY) ) {
			_shape_type = LINE;
		}
		else {	_shape_type = POLYGON;
		}
		EndianDataInputStream in = new EndianDataInputStream(
			IOUtil.getInputStream( geodata_file ) );
		boolean is_big_endian = IOUtil.isBigEndianMachine();
		if ( read_attributes ) {
			Vector table_fields = new Vector (1);
			if ( filetype == COUNTY ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"ID",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"COUNTY",24) );
			}
			else if ( filetype == FG_BASIN ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"FG",24) );
			}
			else if ( filetype == MAP_BASIN ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"MAP Area",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"MAP Name",24) );
			}
			else if ( filetype == RFC_BOUNDARY ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,"RFC",24) );
			}
			else if ( filetype == RIVER ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"REACH",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"NAME",24) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_DOUBLE,
				"ORDER",4,0) );
			}
			else if ( filetype == STATE ) {
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"Abbreviation",3) );
				table_fields.addElement (
				new TableField(
				TableField.DATA_TYPE_STRING,
				"Name",24) );
			}
			_attribute_table = new DataTable(table_fields);
			table_fields = null;
		}
		// Header line...
		int polycount = 0;
		String id, name;
		int npts, order, size = 0;
		while ( true ) {
			xmin = 1.0e10;
			xmax = -1.0e10;
			ymin = 1.0e10;
			ymax = -1.0e10;
			try {	// Read the same whether big or little endian
				// because assume to be 1-byte chars...
				id = in.readString1( 9).trim();
				name = in.readString1 ( 21 ).trim();
				if ( is_big_endian ) {
					order = in.readInt();
					npts = in.readInt();
				}
				else {	order = in.readLittleEndianInt();
					npts = in.readLittleEndianInt();
				}
			}
			catch ( Exception e ) {
				// No more data...
				break;
			}
			if ( read_attributes ) {
				// Only attribute is the name of the basin
				// boundary...
				TableRecord record = new TableRecord ( 1 );
				record.addFieldValue ( id );
				if ( filetype == RIVER ) {
					record.addFieldValue ( name );
					record.addFieldValue(Double.valueOf(order));
				}
				else if ( (filetype == STATE) ||
					(filetype == COUNTY) ||
					(filetype == FG_BASIN) ||
					(filetype == MAP_BASIN) ) {
					record.addFieldValue ( name );
				}
				try {	_attribute_table.addRecord ( record );
				}
				catch ( Exception e ) {
					// Ignore for now..
				}
				record = null;
			}
			size = npts;
			GRPolygon polygon = null;
			GRPolyline polyline = null;
			if (	(filetype == RIVER) || (filetype == STATE) ||
				(filetype == COUNTY) ) {
				polyline = new GRPolyline(size);
			}
			else {	polygon = new GRPolygon(size);
			}
			int ptcount = 0;
			for ( int i = 0; i < size; i++ ) {
				try {	if ( is_big_endian ) {
						x = in.readFloat();
						y = in.readFloat();
					}
					else {	x = in.readLittleEndianFloat();
						y = in.readLittleEndianFloat();
					}
	//if ( filetype == RIVER )
//Message.printStatus ( 1, "", "x = " + x + " y = " + y );
				}
				catch ( Exception e ) {
					Message.printWarning ( 1, "",
					"Error reading x, y" );
					break;
				}
				if (	(filetype == RIVER) ||
					(filetype == STATE) ||
					(filetype == COUNTY) ){
					polyline.setPoint ( ptcount++,
					new GRPoint(x,y) );
				}
				else {	polygon.setPoint ( ptcount++,
					new GRPoint(x,y) );
				}
				xmin = MathUtil.min ( x, xmin );
				xmax = MathUtil.max ( x, xmax );
				ymin = MathUtil.min ( y, ymin );
				ymax = MathUtil.max ( y, ymax );
			}
			if (	(filetype == RIVER) || (filetype == STATE) ||
				(filetype == COUNTY) ) {
				polyline.xmin = xmin;
				polyline.ymin = ymin;
				polyline.xmax = xmax;
				polyline.ymax = ymax;
				polyline.limits_found = true;
				polyline.index = polycount++;
				_shapes.addElement ( polyline );
			}
			else {	polygon.xmin = xmin;
				polygon.ymin = ymin;
				polygon.xmax = xmax;
				polygon.ymax = ymax;
				polygon.limits_found = true;
				polygon.index = polycount++;
				_shapes.addElement ( polygon );
			}
			xmin_layer = MathUtil.min ( xmin, xmin_layer );
			xmax_layer = MathUtil.max ( xmax, xmax_layer );
			ymin_layer = MathUtil.min ( ymin, ymin_layer );
			ymax_layer = MathUtil.max ( ymax, ymax_layer );
		}
		in.close();
		in = null;
		setProjection ( new HRAPProjection() );
	}
	else if ( filetype == FORECASTPT ) {
		// Format is Name State ID Lat Long
		// ...
		_shape_type = POINT;
		BufferedReader in = null;
		in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( geodata_file )) );
		if ( read_attributes ) {
			// Only attribute is the name of the basin
			// boundary...
			Vector table_fields = new Vector (3);
			table_fields.addElement ( new TableField(
				TableField.DATA_TYPE_STRING,"Name",30) );
			table_fields.addElement ( new TableField(
				TableField.DATA_TYPE_STRING,"State",3) );
			table_fields.addElement ( new TableField(
				TableField.DATA_TYPE_STRING,"FP",12) );
			_attribute_table = new DataTable(table_fields);
				table_fields = null;
		}
		GRShape point = null;
		int count = 0;
		while ( true ) {
			string = in.readLine();
			if ( string == null ) {
				break;
			}
			// Get the coordinates from the end of the string (after
			// column 47).
			tokens = StringUtil.breakStringList (
				string.substring(47).trim()," ",
				StringUtil.DELIM_SKIP_BLANKS);
			if (	(tokens == null) || (tokens.size() != 2) ) {
				continue;
			}
			y = StringUtil.atod( (String)tokens.elementAt(0));
			x = StringUtil.atod( (String)tokens.elementAt(1));
			point = new GRPoint ( -x, y );
			xmin = MathUtil.min ( -x, xmin );
			xmax = MathUtil.max ( -x, xmax );
			ymin = MathUtil.min ( y, ymin );
			ymax = MathUtil.max ( y, ymax );
			if ( read_attributes ) {
				// Get the attributes from the first part of
				// the line...
				tokens = StringUtil.fixedRead (
					string.trim(),"s20s7s20");
				TableRecord record = new TableRecord(3);
				if (	(tokens == null) ||
					(tokens.size() != 3) ) {
					record.addFieldValue ( "" );
					record.addFieldValue ( "" );
					record.addFieldValue ( "" );
				}
				else {	record.addFieldValue ( ((String)
					tokens.elementAt(0)).trim() );
					record.addFieldValue ( ((String)
					tokens.elementAt(1)).trim() );
					record.addFieldValue ( ((String)
					tokens.elementAt(2)).trim() );
				}
				try {	_attribute_table.addRecord ( record );
				}
				catch ( Exception e ) {
					// Ignore for now..
				}
				record = null;
			}
			point.index = count++;
			point.limits_found = true;
			_shapes.addElement ( point );
		}
		xmin_layer = xmin;
		xmax_layer = xmax;
		ymin_layer = ymin;
		ymax_layer = ymax;
		setProjection ( new GeographicProjection() );
	}
	else if ( filetype == SWE_STATIONS ) {
		// Format is ID,Lat,-Long,ElevU,Base station,Name
		// ...
		_shape_type = POINT;
		BufferedReader in = null;
		in = new BufferedReader ( new InputStreamReader(
				IOUtil.getInputStream ( geodata_file )) );
		GRShape point = null;
		int count = 0;
		String elev = null;
		String units = null;
		int len = 0;
		while ( true ) {
			string = in.readLine();
			if ( string == null ) {
				break;
			}
			string = string.trim();
			if ( string.length() == 0 ) {
				continue;
			}
			if ( string.charAt(0) == '#' ) {
				continue;
			}
			tokens = StringUtil.breakStringList (
				string,",",0);
			if (	(tokens == null) || (tokens.size() != 6) ) {
				continue;
			}
			elev = ((String)tokens.elementAt(3)).trim();
			// Remove the characters from the elevation...
			len = elev.length();
			units = "";
			for ( int i = 0; i < len; i++ ) {
				if ( !Character.isDigit(elev.charAt(i)) ) {
					elev = elev.substring(0,i);
					units = elev.substring(i);
					break;
				}
			}
			if ( (count == 0) && read_attributes ) {
				// Need to know the units to be able to set
				// the attribute correctly...
				Vector table_fields = new Vector (4);
				table_fields.addElement ( new TableField(
					TableField.DATA_TYPE_STRING, "ID",10) );
				if ( units.length() > 0 ) {
					table_fields.addElement (
					new TableField(
					TableField.DATA_TYPE_DOUBLE,"ELEV_" +
					units.toUpperCase(),
					6,1));
				}
				else {	table_fields.addElement (
					new TableField(
					TableField.DATA_TYPE_DOUBLE,"ELEV",
					6,1));
				}
				table_fields.addElement ( new TableField(
					TableField.DATA_TYPE_STRING,"BASESTA",
					18));
				table_fields.addElement ( new TableField(
					TableField.DATA_TYPE_STRING,"NAME",30));
				_attribute_table = new DataTable( table_fields);
				table_fields = null;
			}
			y = StringUtil.atod(
				(String)tokens.elementAt(1));
			x = StringUtil.atod(
				(String)tokens.elementAt(2));
			point = new GRPoint ( x, y );
			xmin = MathUtil.min ( x, xmin );
			xmax = MathUtil.max ( x, xmax );
			ymin = MathUtil.min ( y, ymin );
			ymax = MathUtil.max ( y, ymax );
			if ( read_attributes ) {
				TableRecord record = new TableRecord(3);
				record.addFieldValue (
					((String)
					tokens.elementAt(0)).trim() );
				record.addFieldValue (
					Double.valueOf( StringUtil.atod(elev)) );
				record.addFieldValue (
					((String)
					tokens.elementAt(4)).trim() );
				record.addFieldValue (
					((String)
					tokens.elementAt(5)).trim() );
				try {	_attribute_table.addRecord (
					record );
				}
				catch ( Exception e ) {
					// Ignore for now..
				}
				record = null;
			}
			point.index = count++;
			point.limits_found = true;
			_shapes.addElement ( point );
		}
		xmin_layer = xmin;
		xmax_layer = xmax;
		ymin_layer = ymin;
		ymax_layer = ymax;
		setProjection ( new GeographicProjection() );
	}

	// Set the limits for the layer data...

	setLimits ( xmin_layer, ymin_layer, xmax_layer, ymax_layer );

	Message.printStatus ( 1, routine,
	"Read " + _shapes.size() + " shapes from \"" + geodata_file + "\"." );
	Message.printStatus ( 1, routine,
	"Defined " + _attribute_table.getNumberOfRecords() +
		" attribute table records." );

	// Clean up...

	message = null;
	routine = null;
	buffer = null;
	buffer20 = null;
}
*/

} // End of NetworkLayer
