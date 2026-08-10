// TSPropertiesJFrame - displays properties for a time series

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2026 Colorado Department of Natural Resources

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

package RTi.GRTS;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import RTi.TS.MonthTS;
import RTi.TS.MonthTSLimits;
import RTi.TS.TS;
import RTi.TS.TSDataFlagMetadata;
import RTi.TS.TSUtil;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.JScrollWorksheet;
import RTi.Util.GUI.JWorksheet;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.DataUnits;
import RTi.Util.IO.PrintJGUI;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.DataTable_CellRenderer;
import RTi.Util.Table.DataTable_JPanel;
import RTi.Util.Table.DataTable_TableModel;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.TimeInterval;

/**
The TSPropertiesJFrame displays properties for a time series,
including information from the TSIdent and also basic statistics from TSLimits.
The properties are typically shown from a parent JFrame window.
*/
@SuppressWarnings("serial")
public class TSPropertiesJFrame extends JFrame
implements ActionListener, ChangeListener, WindowListener
{

/**
Time series to display.
*/
private TS ts;

/**
Properties to control output.
*/
private PropList props;

/**
Print button to be enabled only with the History tab.
*/
private SimpleJButton print_JButton;

/**
Tabbed pane to manage panels with properties.
*/
private JTabbedPane props_JTabbedPane;

/**
JTextArea for history tab.
*/
private JTextArea history_JTextArea;

/**
JTextArea for comments tab.
*/
private JTextArea comments_JTextArea;

/**
Panel for time series history.
*/
private JPanel history_JPanel;

/**
Panel for time series comments.
*/
private JPanel comments_JPanel;

/**
Construct a TSPropertiesJFrame.
@param gui Parent JFrame.  Currently this is ignored and can be set to null.
@param ts Time series for which to display properties.
@exception Exception if there is an error displaying properties.
*/
public TSPropertiesJFrame ( JFrame gui, TS ts, PropList props )
throws Exception {
	super ( "Time Series Properties" );
	this.ts = ts;
	if ( props == null ) {
		props = new PropList("");
	}
	this.props = props;
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
	openGUI ( true );
}

/**
Handle action events (button press, etc.)
@param e ActionEvent to handle.
*/
public void actionPerformed ( ActionEvent e ) {
	String command = e.getActionCommand();
	if ( command.equals("Close") ) {
		JGUIUtil.close(this);
	}
	else if ( command.equals("Print") ) {
		try {
		    //PrintJGUI.print ( this, JGUIUtil.toVector(this.history_JTextArea), null, 8 );
			if ( this.props_JTabbedPane.getSelectedComponent() == this.comments_JPanel ) {
				PrintJGUI.printJTextAreaObject(this, null, this.comments_JTextArea);
			}
			else if ( this.props_JTabbedPane.getSelectedComponent() == this.history_JPanel ) {
				PrintJGUI.printJTextAreaObject(this, null, this.history_JTextArea);
			}
		}
		catch ( Exception ex ) {
			Message.printWarning ( 1, "TSPropertiesJFrame.actionPerformed", "Error printing (" + ex + ")." );
			Message.printWarning ( 2, "TSPropertiesJFrame.actionPerformed", ex );
		}
	}
}

/**
Create a data table that contains time series data flags and descriptions.
@param ts time series from which to generate a data flags table.
@return the table containing time series data flags
*/
private DataTable createDataFlagsTable ( TS ts ) {
	List<TSDataFlagMetadata> dataFlagMetadataList = ts.getDataFlagMetadataList();
    // Get the length of the flag and description to set the table column width.
    int nameLength = 25;
    int displayNameLength = 25;
    int descriptionLength = 25;
    for ( TSDataFlagMetadata meta : dataFlagMetadataList ) {
    	// Name.
        nameLength = Math.max(nameLength, meta.getDataFlag().length());
        // Display name.
        String displayName = meta.getDisplayName();
        if ( displayName == null ) {
            displayName = "";
        }
        displayNameLength = Math.max(displayNameLength, displayName.length());
        // Description.
        String description = meta.getDescription();
        if ( description == null ) {
            description = "";
        }
        descriptionLength = Math.max(descriptionLength, description.length());
    }
    List<TableField> tableFields = new ArrayList<>();
    // The above computed lengths may be very long so use auto widths or set in the table model that uses the table.
    // The output will be shown as a string.
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Data Flag",nameLength) );
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Display Name",displayNameLength) );
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Description",descriptionLength) );
    DataTable table = new DataTable ( tableFields );
    table.setTableID("DataFlags");
    TableRecord rec;
    for ( TSDataFlagMetadata meta : dataFlagMetadataList ) {
        rec = new TableRecord();
        rec.addFieldValue(meta.getDataFlag());
        rec.addFieldValue(meta.getDisplayName());
        rec.addFieldValue(meta.getDescription());
        try {
            table.addRecord(rec);
        }
        catch ( Exception e2 ) {
            // Should not happen.
        }
    }
    return table;
}

/**
Create a data table that contains time series properties string values.
@param ts time series from which to generate a property table.
@return the table containing time series properties
*/
private DataTable createPropertyTable ( TS ts ) {
    HashMap<String,Object> properties = ts.getProperties();
    ArrayList<String> keyList = new ArrayList<>(properties.keySet());
    // Don't sort because order of properties often has some meaning.  Users can sort displayed table.
    //Collections.sort(keyList);
    // Get the length of the name and values to set the table width.
    int nameLength = 25;
    int valueLength = 25;
    for ( String key : keyList ) {
        nameLength = Math.max(nameLength, key.length());
        Object value = properties.get(key);
        if ( value == null ) {
            value = "";
        }
        valueLength = Math.max(valueLength, value.toString().length());
    }
    List<TableField> tableFields = new ArrayList<>();
    int typeLength = -1;
    // The output will be shown as a string.
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Property Name",nameLength) );
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Property Type",typeLength) );
    tableFields.add ( new TableField(TableField.DATA_TYPE_STRING,"Property Value",valueLength) );
    DataTable table = new DataTable ( tableFields );
    table.setTableID("Properties");
    TableRecord rec;
    for ( String key : keyList ) {
        rec = new TableRecord();
        rec.addFieldValue(key);
        Object value = properties.get(key);
        if ( value == null ) {
            value = "";
        }
        else if ( value instanceof Double ) {
            Double d = (Double)value;
            if ( d.isNaN() ) {
                value = "";
            }
        }
        else if ( value instanceof Float ) {
            Float f = (Float)value;
            if ( f.isNaN() ) {
                value = "";
            }
        }
        if ( value != null ) {
        	rec.addFieldValue( value.getClass().getSimpleName() ); // To force string, no matter the value.
        }
        // TODO SAM 2010-10-08 Should objects be used?
        rec.addFieldValue( "" + value ); // To force string, no matter the value.
        try {
            table.addRecord(rec);
        }
        catch ( Exception e2 ) {
            // Should not happen.
        }
    }
    return table;
}

/**
Open the properties GUI.
@param mode Indicates whether the GUI is visible at creation.
*/
private void openGUI ( boolean mode ) {
	String	routine = getClass().getSimpleName() + ".openGUI";

	// Start a big try block to set up the GUI.
	try {

	// Add a listener to catch window manager events.

	addWindowListener ( this );
	GridBagLayout gbl = new GridBagLayout();
	Insets insetsTLBR = new Insets ( 2, 2, 2, 2 );	// Space around text area.

	// Font for reports (fixed width).

	Font report_Font = new Font ( "Courier", Font.PLAIN, 11 );

	// Add a panel to hold the main components.

	JPanel display_JPanel = new JPanel ();
	display_JPanel.setLayout ( gbl );
	getContentPane().add ( display_JPanel );

	this.props_JTabbedPane = new JTabbedPane ();
	this.props_JTabbedPane.addChangeListener ( this );
	JGUIUtil.addComponent ( display_JPanel, this.props_JTabbedPane,
			0, 0, 10, 1, 1.0, 1.0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	//
	// Identifier tab.
	//

	JPanel id_JPanel = new JPanel();
	GridBagLayout id_gbl = new GridBagLayout();
	id_JPanel.setLayout ( id_gbl );
	this.props_JTabbedPane.addTab ( "Identifier", null, id_JPanel, "Identifier properties" );

	int yId = -1;
	JGUIUtil.addComponent ( id_JPanel, new JLabel("The time series identifier (TSID) uniquely identifies the time series."),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("A standard TSID adheres to the following naming convention (brackets indicate optional data):"),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("  [LocType:]LocationID.DataSource.DataType.Interval[.Scenario][SequenceID]"),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("Including the datastore allows TSTool to read a time series from a datastore in a TSID command or time series product:"),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("  [LocType:]LocationID.DataSource.DataType.Interval[.Scenario][SequenceID]~DataStore"),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("An alias can also be used to identify the time series, for example when the TSID is complex."),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("The TSID or alias are used in commands to match time series for processing."),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( id_JPanel, new JLabel("Multiple time series may be included in an ensemble, which uses the sequence ID to uniquely identify the trace."),
		0, ++yId, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(id_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yId, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent ( id_JPanel, new JLabel("TSID:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField identifier_JTextField = new JTextField(this.ts.getIdentifierString(), 50);
	identifier_JTextField.setToolTipText ( "Period-delimited time series identifier (TSID), to uniquely identify the time series." );
	identifier_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, identifier_JTextField,
			1, yId, 6, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "TSID (with datastore or input type):"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField input_JTextField = new JTextField( this.ts.getIdentifier().toString(true), 50 );
	input_JTextField.setToolTipText (
		"Period-delimited time series identifier, with ~InputName if the time series was read from a datastore or other input." );
	input_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, input_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Location type:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField locType_JTextField = new JTextField( this.ts.getIdentifier().getLocationType(), 50 );
	locType_JTextField.setToolTipText ( "Location type, use when interpreting the location ID is ambiguous." );
	locType_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, locType_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Location ID:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField locid_JTextField = new JTextField( this.ts.getIdentifier().getLocation(), 50 );
	locid_JTextField.setToolTipText ( "Location identifier (e.g., station or site identifier)." );
	locid_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, locid_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Data source:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField dataSource_JTextField = new JTextField( this.ts.getIdentifier().getSource(), 50 );
	dataSource_JTextField.setToolTipText ( "Data source, typically an organiation abbreviation or software application/system name." );
	dataSource_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, dataSource_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Data type:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField dataType_JTextField = new JTextField( this.ts.getIdentifier().getType(), 50 );
	dataType_JTextField.setToolTipText ( "Data type, typically an abbreviation or short name." );
	dataType_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, dataType_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Data interval:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField interval_JTextField = new JTextField( this.ts.getIdentifier().getInterval(), 50 );
	interval_JTextField.setToolTipText ( "Data interval, can be regular or irregular spacing." );
	interval_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, interval_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	// Limit the length of this field.
	JLabel interval_JLabel = null;
	if ( TimeInterval.isRegularInterval(this.ts.getIdentifier().getIntervalBase()) ) {
		interval_JLabel = new JLabel( "Time series has regular interval spacing." );
	}
	else {
		String label = "Time series has irregular interval spacing.";
		if ( this.ts.getDate1() != null ) {
			label += "  Date/time precision from period start = " + TimeInterval.getName(this.ts.getDate1().getPrecision(), 0);
		}
		interval_JLabel = new JLabel( label );
	}
	JGUIUtil.addComponent ( id_JPanel, interval_JLabel,
			1, ++yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Scenario:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField scenario_JTextField = new JTextField( this.ts.getIdentifier().getScenario(), 50 );
	scenario_JTextField.setToolTipText ( "Scenario identifier, typically used with modeling or analysis." );
	scenario_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, scenario_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel( "Sequence ID:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Limit the length of this field.
	JTextField seqID_JTextField = new JTextField( this.ts.getIdentifier().getSequenceID(), 50 );
	seqID_JTextField.setToolTipText ( "Sequence (trace) identifier, used time series is part of an ensemble." );
	seqID_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, seqID_JTextField,
			1, yId, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel("Sequence (ensemble trace) ID:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField seqnum_JTextField = new JTextField("" + this.ts.getSequenceID(), 5);
	seqnum_JTextField.setToolTipText ( "Identifier for the trace in an ensemble, for example the historical year." );
	seqnum_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, seqnum_JTextField,
			1, yId, 2, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( id_JPanel, new JLabel("Alias:"),
			0, ++yId, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField alias_JTextField = new JTextField( this.ts.getAlias(), 50 );
	alias_JTextField.setToolTipText ( "Alternative to the time series identifier." );
	alias_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( id_JPanel, alias_JTextField,
			1, yId, 2, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	//
	// General tab:
	// - some information is described in tool tips but tool tips are not shown for disabled text fields
	//

	JPanel general_JPanel = new JPanel();
	GridBagLayout general_gbl = new GridBagLayout();
	general_JPanel.setLayout ( general_gbl );
	this.props_JTabbedPane.addTab ( "General", null, general_JPanel, "General properties" );

	int yGen = -1;

	JGUIUtil.addComponent ( general_JPanel, new JLabel("General properties include basic metadata."),
		0, ++yGen, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( general_JPanel, new JLabel("The description typically includes location and data type information."),
		0, ++yGen, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( general_JPanel, new JLabel("The value 'temporal reference' and 'interval closure' indicate how input values are used to compute interval (or duration) data"),
		0, ++yGen, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( general_JPanel, new JLabel("(requires clear documentation from a data source to set accurately if data are read)."),
		0, ++yGen, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( general_JPanel, new JLabel("Hover over fields to display information about a data item."),
		0, ++yGen, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(general_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yGen, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent ( general_JPanel, new JLabel("Description:"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	// Set a maximum size so this does not get outrageously big.
	JTextField description_JTextField=new JTextField(this.ts.getDescription(),50);
	description_JTextField.setToolTipText ( "A short description, typically including the location and data type." );
	description_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, description_JTextField,
			1, yGen, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( general_JPanel, new JLabel("Units (current):"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField units_JTextField = new JTextField( this.ts.getDataUnits(), 10);
	units_JTextField.setToolTipText ( "Data units (reflects original data and processing)." );
	units_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, units_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( general_JPanel, new JLabel("Units (original):"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField unitsorig_JTextField = new JTextField( this.ts.getDataUnitsOriginal(), 10);
	unitsorig_JTextField.setToolTipText ( "Data units, from the time series when created or read." );
	unitsorig_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, unitsorig_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	String tsUnits = this.ts.getDataUnits();
	String precisionFromUnits = "";
	if ( (tsUnits != null) && !tsUnits.isEmpty() ) {
		try {
		    DataUnits u = DataUnits.lookupUnits ( tsUnits );
			precisionFromUnits = "" + u.getOutputPrecision();
		}
		catch ( Exception e ) {
			// No precision from units.
		}
	}
	JGUIUtil.addComponent ( general_JPanel, new JLabel("Output precision (from units):"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField precisionFromUnits_JTextField = new JTextField(precisionFromUnits, 10);
	precisionFromUnits_JTextField.setToolTipText ( "Data precision (digits after decimal point) for output, determined from data units." );
	precisionFromUnits_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, precisionFromUnits_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	String precisionSpecified = "";
	if ( this.ts.getDataPrecision() >= 0 ) {
		precisionSpecified = "" + this.ts.getDataPrecision();
	}
	JGUIUtil.addComponent ( general_JPanel, new JLabel("Output precision (specified):"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField precisionSpecified_JTextField = new JTextField(precisionSpecified, 10);
	precisionSpecified_JTextField.setToolTipText (
		"Data precision (digits after decimal point) for output specified directly, overrides precision from units." );
	precisionSpecified_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, precisionSpecified_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	String valueTemporalReference = "";
	if ( this.ts.getValueTemporalReferenceType() != null ) {
		valueTemporalReference = "" + this.ts.getValueTemporalReferenceType();
	}
	JGUIUtil.addComponent ( general_JPanel, new JLabel("Value temporal reference:"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField valueTemporalReference_JTextField = new JTextField(valueTemporalReference, 10);
	valueTemporalReference_JTextField.setToolTipText (
		"Indicates the value time alignment (not used for date-precision time series)." );
	valueTemporalReference_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, valueTemporalReference_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	String valueIntervalClosure = "";
	if ( this.ts.getValueIntervalClosureType() != null ) {
		valueIntervalClosure = "" + this.ts.getValueIntervalClosureType();
	}
	JGUIUtil.addComponent ( general_JPanel, new JLabel("Value interval closure:"),
			0, ++yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField valueIntervalClosure_JTextField = new JTextField(valueIntervalClosure, 10);
	valueIntervalClosure_JTextField.setToolTipText (
		"Indicates how input sample values are handled when aligned with regular interval time series or duration boundaries." );
	valueIntervalClosure_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( general_JPanel, valueIntervalClosure_JTextField,
			1, yGen, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JCheckBox isselected_JCheckBox = new JCheckBox ( "Is selected?", this.ts.isSelected() );
	isselected_JCheckBox.setEnabled ( false );
	isselected_JCheckBox.setToolTipText ( "Is the time series selected?" );
	JGUIUtil.addComponent ( general_JPanel, isselected_JCheckBox,
			1, ++yGen, 1, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

    JCheckBox iseditable_JCheckBox = new JCheckBox ( "Is editable?", this.ts.isEditable() );
    iseditable_JCheckBox.setEnabled ( false );
	iseditable_JCheckBox.setToolTipText ( "Is the time series editable when viewed?" );
    JGUIUtil.addComponent ( general_JPanel, iseditable_JCheckBox,
            1, ++yGen, 1, 1, 1.0, 0.0,
            insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JCheckBox isdirty_JCheckBox = new JCheckBox ( "Is dirty (data read/edited/modified without recomputing limits)?", this.ts.isDirty() );
	isdirty_JCheckBox.setEnabled ( false );
	isdirty_JCheckBox.setToolTipText ( "Is the time series dirty?  Data have been modified but limits have not been recomputed." );
	JGUIUtil.addComponent ( general_JPanel, isdirty_JCheckBox,
			1, ++yGen, 1, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

    // Properties tab.

    JPanel properties_JPanel = new JPanel();
	GridBagLayout properties_gbl = new GridBagLayout();
    properties_JPanel.setLayout ( properties_gbl );
    this.props_JTabbedPane.addTab ( "Properties", null, properties_JPanel, "Time series properties set during processing." );
    int yProp = -1;

	JGUIUtil.addComponent ( properties_JPanel, new JLabel("The following properties are from the original data source and processing commands."),
		0, ++yProp, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( properties_JPanel, new JLabel("Refer to datastore and API documentation for more information."),
		0, ++yProp, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( properties_JPanel, new JLabel("Time series properties can be used to control processing of a specific time series."),
		0, ++yProp, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(properties_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yProp, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

    boolean doOldProperties = false;
    int [] propertiesWorksheetColumnWidths = null;
   	JWorksheet propertiesWorksheet = null;
    if ( doOldProperties ) {
    	// Old code:
    	// - the column widths do not dynamically size correctly
    	DataTable_JPanel props_JPanel = new DataTable_JPanel(this, createPropertyTable(this.ts));
    	// TODO smalers 2022-04-22 the following does not work - set the widths on the DataTable,
    	// but that does not seem to work either.
    	//int [] columnWidths = {
    	//	30,
    	//	30,
    	//	80
    	//};
    	//panel.setWorksheetColumnWidths(columnWidths);
    	JGUIUtil.addComponent ( properties_JPanel,
            new JScrollPane ( props_JPanel ),
            0, ++yProp, 8, 1, 1.0, 1.0,
            insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    }
    else {
    	// New code:
    	// - the column widths resize dynamically similar to TSTool main UI table view and time series table view.
    	JScrollWorksheet propertiesScrollWorksheet = null;
   		PropList jswProps = new PropList ( "" );
   		jswProps = new PropList("TableModel_JPanel.JWorksheet");
   		jswProps.add("JWorksheet.ShowPopupMenu=true");
   		jswProps.add("JWorksheet.SelectionMode=ExcelSelection");
   		jswProps.add("JWorksheet.AllowCopy=true");
	   	try {
	   		DataTable_TableModel tm = new DataTable_TableModel(createPropertyTable(this.ts));
	   		DataTable_CellRenderer cr = new DataTable_CellRenderer(tm);
		   	propertiesScrollWorksheet = new JScrollWorksheet ( cr, tm, jswProps );
		   	propertiesWorksheet = propertiesScrollWorksheet.getJWorksheet();
		   	propertiesWorksheetColumnWidths = cr.getColumnWidths();
	   	}
	   	catch (Exception e) {
		   	Message.printWarning(2, routine, e);
		   	propertiesScrollWorksheet = new JScrollWorksheet(0, 0, jswProps);
		   	propertiesWorksheet = propertiesScrollWorksheet.getJWorksheet();
	   	}
	   	propertiesWorksheet.setPreferredScrollableViewportSize(null);
	   	//propertiesWorksheet.setHourglassJFrame(this.parent);

    	JGUIUtil.addComponent ( properties_JPanel,
    		propertiesScrollWorksheet,
            0, ++yProp, 8, 1, 1.0, 1.0,
            insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    }

	// Comments tab.

	this.comments_JPanel = new JPanel();
	GridBagLayout comments_gbl = new GridBagLayout();
	this.comments_JPanel.setLayout ( comments_gbl );
	this.props_JTabbedPane.addTab ( "Comments", null, this.comments_JPanel, "Comments" );
	int yComment = -1;

	JGUIUtil.addComponent ( this.comments_JPanel, new JLabel("Comments are used with some output formats."),
		0, ++yComment, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(this.comments_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yComment, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	this.comments_JTextArea = new JTextArea(StringUtil.toString( this.ts.getComments(),
			System.getProperty("line.separator")),5,80);
	this.comments_JTextArea.setFont ( report_Font );
	this.comments_JTextArea.setEditable ( false );
	JGUIUtil.addComponent ( this.comments_JPanel,
			new JScrollPane (this.comments_JTextArea),
			0, ++yComment, 6, 1, 1.0, 1.0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	//
	// Period tab.
	//

	JPanel period_JPanel = new JPanel();
	GridBagLayout period_gbl = new GridBagLayout();
	period_JPanel.setLayout ( period_gbl );
	this.props_JTabbedPane.addTab ( "Period", null, period_JPanel, "Period" );
	int yPeriod = -1;

	JGUIUtil.addComponent ( period_JPanel, new JLabel("The current time series period reflects the original data that were read and subsequent processing."),
		0, ++yPeriod, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( period_JPanel, new JLabel("The original time series period is from reading the data or may reflect the available period from the data source."),
		0, ++yPeriod, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(period_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yPeriod, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent ( period_JPanel, new JLabel("Current (reflects processing):"),
			0, ++yPeriod, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField period_JTextField = new JTextField( " " + this.ts.getDate1() + " to "+ this.ts.getDate2(), 50 );
	period_JTextField.setToolTipText ( "Data period (may contain missing values at ends)." );
	period_JTextField.setEditable(false);
	JGUIUtil.addComponent ( period_JPanel, period_JTextField,
		1, yPeriod, 2, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( period_JPanel, new JLabel(
		"Original (from input):"), 0, ++yPeriod, 1, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField origperiod_JTextField = new JTextField( " " + this.ts.getDate1Original() + " to " + this.ts.getDate2Original(), 50 );
	origperiod_JTextField.setToolTipText ( "Original data period, from input source (may contain missing values at ends)." );
	origperiod_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( period_JPanel, origperiod_JTextField,
		1, yPeriod, 2, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	JGUIUtil.addComponent ( period_JPanel, new JLabel("Total points:"),
			0, ++yPeriod, 1, 1, 0, 0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField points_JTextField = new JTextField( " " + this.ts.getDataSize());
	points_JTextField.setToolTipText ( "Number of data points (points may be missing values)." );
	points_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( period_JPanel, points_JTextField,
			1, yPeriod, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	//
	// Limits tab.
	//

	JPanel limits_JPanel = new JPanel();
	GridBagLayout limits_gbl = new GridBagLayout();
	limits_JPanel.setLayout ( limits_gbl );
	this.props_JTabbedPane.addTab ( "Limits", null, limits_JPanel, "Limits" );
	int yLimits = -1;

	JGUIUtil.addComponent ( limits_JPanel, new JLabel("Data limits are useful for general context and are used by some commands to fill missing data."),
		0, ++yLimits, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( limits_JPanel, new JLabel("Original data limits are computed after reading/creating a time series."),
		0, ++yLimits, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(limits_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yLimits, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent ( limits_JPanel, new JLabel("Current (reflects manipulation):"),
			0, ++yLimits, 6, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JTextArea limits_JTextArea = null;
	if ( this.ts.getDataIntervalBase() == TimeInterval.MONTH ) {
		try {
		    limits_JTextArea = new JTextArea(new MonthTSLimits((MonthTS)this.ts).toString(),12,80);
		}
		catch ( Exception e ) {
			limits_JTextArea = new JTextArea("No Limits Available",5,80);
		}
	}
	else {
	    try {
	        limits_JTextArea = new JTextArea((TSUtil.getDataLimits(this.ts, this.ts.getDate1(),
				this.ts.getDate2())).toString(),15,80 );
		}
		catch ( Exception e ) {
			limits_JTextArea = new JTextArea("No limits available",5,80);
		}
	}
    limits_JTextArea.setToolTipText ( "Data limits (statistics)." );
	limits_JTextArea.setEditable ( false );
	limits_JTextArea.setFont ( report_Font );
	JGUIUtil.addComponent ( limits_JPanel,
			new JScrollPane ( limits_JTextArea ),
			0, ++yLimits, 6, 1, 1.0, 1.0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	++yLimits;
	JGUIUtil.addComponent(limits_JPanel,
			new JLabel("Original (from input):"),
			0, ++yLimits, 6, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JTextArea origlim_JTextArea = null;
	if ( this.ts.getDataLimitsOriginal() == null ) {
		origlim_JTextArea = new JTextArea( "No limits available");
	}
	else {
	    origlim_JTextArea = new JTextArea(this.ts.getDataLimitsOriginal().toString(),10,80);
		origlim_JTextArea.setFont ( report_Font );
		origlim_JTextArea.setEditable ( false );
	}
    origlim_JTextArea.setToolTipText ( "Data limits (statistics), from time series when read or created." );
	origlim_JTextArea.setEditable(false);
	JGUIUtil.addComponent ( limits_JPanel,
			new JScrollPane ( origlim_JTextArea ),
			0, ++yLimits, 6, 1, 1.0, 1.0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	//
	// History tab.
	//

	this.history_JPanel = new JPanel();
	GridBagLayout history_gbl = new GridBagLayout();
	this.history_JPanel.setLayout ( history_gbl );
	this.props_JTabbedPane.addTab("History", null, this.history_JPanel,"History");
	int yHist = -1;

	JGUIUtil.addComponent ( this.history_JPanel, new JLabel("The history is a sequence of notes indicating how the time series was read/created and modified by commands."),
		0, ++yHist, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( this.history_JPanel, new JLabel("The history may include data that are not stored in other time series properties."),
		0, ++yHist, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(this.history_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yHist, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	this.history_JTextArea = new JTextArea( StringUtil.toString(this.ts.getGenesis(),System.getProperty("line.separator")),5,80);
	this.history_JTextArea.setFont ( report_Font );
    this.history_JTextArea.setToolTipText ( "History of how time series has been processed." );
	this.history_JTextArea.setEditable ( false );
	JGUIUtil.addComponent ( this.history_JPanel,
			new JScrollPane (this.history_JTextArea),
			0, ++yHist, 7, 1, 1.0, 1.0,
			insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );

	JGUIUtil.addComponent ( this.history_JPanel, new JLabel("Read from:"),
			0, ++yHist, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );
	JTextField inputname_JTextField = new JTextField( this.ts.getInputName());
    inputname_JTextField.setToolTipText ( "Original data source (file, database, web service, etc.)." );
	inputname_JTextField.setEditable ( false );
	JGUIUtil.addComponent ( this.history_JPanel, inputname_JTextField,
			1, yHist, 6, 1, 1.0, 0.0,
			insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER );

	//
	// Data Flags tab.
	//

	JPanel dataflags_JPanel = new JPanel();
	GridBagLayout dataflags_gbl = new GridBagLayout();
	dataflags_JPanel.setLayout ( dataflags_gbl );
	this.props_JTabbedPane.addTab ( "Data Flags", dataflags_JPanel );
	int yDataflags = 0;

	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("A time series has a numerical value that indicates missing data."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("The default value is -999 for historical reasons but new commands typically use NaN (not a number)."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("Regular interval time series that have gaps will use the missing value."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("Irregular interval time series may also store missing values."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("Each data value can also have a string (text) flag of zero or more characters."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel("Data flags (and flag description) are added by some commands and are used in output."),
		0, ++yDataflags, 8, 1, 0.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST );
    JGUIUtil.addComponent(dataflags_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
        0, ++yDataflags, 8, 1, 0.0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	JGUIUtil.addComponent ( dataflags_JPanel, new JLabel(
			"Missing data value:"), 0, ++yDataflags, 1, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST );
	JTextField missing_JTextField = null;
	if ( Double.isNaN(this.ts.getMissing()) ) {
		missing_JTextField = new JTextField( "NaN", 15);
	}
	else {
		missing_JTextField = new JTextField( StringUtil.formatString( this.ts.getMissing(),"%.4f"), 15);
	}
    missing_JTextField.setToolTipText ( "Value that indicates missing data." );
	missing_JTextField.setEditable(false);
	// Can't get the layout to work without using HORIZONTAL expansion but spent time trying to figure it out and could not.
	JGUIUtil.addComponent ( dataflags_JPanel, missing_JTextField,
		1, yDataflags, 1, 1, 1.0, 0.0,
		insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );

	JCheckBox hasdataflags_JCheckBox = new JCheckBox ( "Has data flags?", this.ts.hasDataFlags() );
	hasdataflags_JCheckBox.setEnabled ( false );
    hasdataflags_JCheckBox.setToolTipText ( "Indicates whether data flags are used for values." );
	JGUIUtil.addComponent ( dataflags_JPanel, hasdataflags_JCheckBox,
			0, ++yDataflags, 2, 1, 0.0, 0.0,
			insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST );

	boolean doOldDataflags = false;
    int [] dataFlagsWorksheetColumnWidths = null;
   	JWorksheet dataFlagsWorksheet = null;
	if ( doOldDataflags ) {
		DataTable_JPanel dataflagsTable_JPanel = new DataTable_JPanel(this, createDataFlagsTable(this.ts));
		// TODO smalers 2022-04-22 the following does not work - set the widths on the DataTable,
		// but that does not seem to work either.
		//int [] columnWidths = {
		//	30,
		//	30,
		//	80
		//};
		//panel.setWorksheetColumnWidths(columnWidths);
		JGUIUtil.addComponent ( dataflags_JPanel,
            new JScrollPane ( dataflagsTable_JPanel ),
            0, ++yDataflags, 8, 10, 1.0, 1.0,
            insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
	}
    else {
    	// New code:
    	// - the column widths resize dynamically similar to TSTool main UI table view and time series table view.
    	JScrollWorksheet dataFlagsScrollWorksheet = null;
   		PropList jswProps = new PropList ( "" );
   		jswProps = new PropList("TableModel_JPanel.JWorksheet");
   		jswProps.add("JWorksheet.ShowPopupMenu=true");
   		jswProps.add("JWorksheet.SelectionMode=ExcelSelection");
   		jswProps.add("JWorksheet.AllowCopy=true");
	   	try {
	   		DataTable_TableModel tm = new DataTable_TableModel(createDataFlagsTable(this.ts));
	   		DataTable_CellRenderer cr = new DataTable_CellRenderer(tm);
		   	dataFlagsScrollWorksheet = new JScrollWorksheet ( cr, tm, jswProps );
		   	dataFlagsWorksheet = dataFlagsScrollWorksheet.getJWorksheet();
		   	dataFlagsWorksheetColumnWidths = cr.getColumnWidths();
	   	}
	   	catch (Exception e) {
		   	Message.printWarning(2, routine, e);
		   	dataFlagsScrollWorksheet = new JScrollWorksheet(0, 0, jswProps);
		   	dataFlagsWorksheet = dataFlagsScrollWorksheet.getJWorksheet();
	   	}
	   	propertiesWorksheet.setPreferredScrollableViewportSize(null);
	   	//propertiesWorksheet.setHourglassJFrame(this.parent);

    	JGUIUtil.addComponent ( dataflags_JPanel,
    		dataFlagsScrollWorksheet,
            0, ++yDataflags, 8, 10, 1.0, 1.0,
            insetsTLBR, GridBagConstraints.BOTH, GridBagConstraints.CENTER );
    }

	// Put the buttons on the bottom of the window.

	JPanel button_JPanel = new JPanel ();
	button_JPanel.setLayout ( new FlowLayout(FlowLayout.CENTER) );

	button_JPanel.add ( new SimpleJButton("Close", "Close",this) );
	this.print_JButton = new SimpleJButton("Print", "Print", this );
	this.print_JButton.setEnabled ( false );
	button_JPanel.add ( this.print_JButton );

	getContentPane().add ( "South", button_JPanel );
	button_JPanel = null;

	if ( (JGUIUtil.getAppNameForWindows() == null) || JGUIUtil.getAppNameForWindows().equals("") ) {
		setTitle ( this.ts.getIdentifier().toString() + " - Properties" );
	}
	else {
	    setTitle( JGUIUtil.getAppNameForWindows() + " - " + this.ts.getIdentifier().toString() + " - Properties" );
	}

	pack ();
	// Get the UI component to determine screen to display on - needed for multiple monitors.
	Object uiComponentO = this.props.getContents( "TSViewParentUIComponent" );
	Component parentUIComponent = null;
	if ( (uiComponentO != null) && (uiComponentO instanceof Component) ) {
		parentUIComponent = (Component)uiComponentO;
	}
	JGUIUtil.center ( this, parentUIComponent );
	setResizable ( false );
	setVisible ( mode );

	// Set worksheet column widths, used with JScrollableWorksheet.

	propertiesWorksheet.setColumnWidths ( propertiesWorksheetColumnWidths );
	dataFlagsWorksheet.setColumnWidths ( dataFlagsWorksheetColumnWidths );

	} // End of try.
	catch ( Exception e ) {
		Message.printWarning ( 2, routine, e );
	}
}

/**
React to tab selections.  Currently all that is done is the Print button is enabled or disabled.
@param e the ChangeEvent that happened.
*/
public void stateChanged ( ChangeEvent e ) {
	// Check for null because events are sometimes generated at startup.
	if ( (this.props_JTabbedPane.getSelectedComponent() == this.history_JPanel) ||
		(this.props_JTabbedPane.getSelectedComponent() == this.comments_JPanel) ) {
		JGUIUtil.setEnabled ( this.print_JButton, true );
	}
	else {
	    JGUIUtil.setEnabled ( this.print_JButton, false );
	}
}

// WindowListener functions.

public void windowActivated( WindowEvent evt ) {
}

public void windowClosed( WindowEvent evt ) {
}

/**
Close the GUI.
*/
public void windowClosing( WindowEvent event ) {
	JGUIUtil.close( this);
}

public void windowDeactivated( WindowEvent evt ) {
}

public void windowDeiconified( WindowEvent evt ) {
}

public void windowOpened( WindowEvent evt ) {
}

public void windowIconified( WindowEvent evt ) {
}

}