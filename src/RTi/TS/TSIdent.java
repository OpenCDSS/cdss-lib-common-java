// TSIdent - class to store and manipulate a time series identifier, or TSID string

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

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

package RTi.TS;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.io.Serializable;

import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.TimeInterval;

/**
The TSIdent class stores and manipulates a time series identifier, or TSID string.
The TSID string consists of the following parts:
<p>
<pre>
[LocationType:]Location[-SubLoc].Source.Type[-Subtype].Interval.Scenario[SeqID]~InputType~InputName
</pre>
<pre>
[LocationType:]Location[-SubLoc].Source.Type[-Subtype].Interval.Scenario[SeqID]~DataStoreName
</pre>
<p>
TSID's as TSIdent objects or strings can be used to pass unique time series
identifiers and are used throughout the time series package.
Some TS object data, including data type, are stored only in the TSIdent, to avoid redundant data.
*/
@SuppressWarnings("serial")
public class TSIdent
implements Cloneable, Serializable, Transferable
{

/**
Mask indicating that no sub-location should be allowed (treat as part of the main location), used by setLocation().
*/
public static final int NO_SUB_LOCATION	= 0x1;

/**
Mask indicating that no sub-source should be allowed (treat as part of the main source), used by setSource().
*/
public static final int NO_SUB_SOURCE = 0x2;

/**
Mask indicating that no sub-type should be allowed (treat as part of the main type), used by setType().
*/
public static final int NO_SUB_TYPE = 0x4;

/**
Mask indicating that no validation of data should occur.
This is useful for storing identifier parts during manipulation (e.g., use wildcards, or specify parts of identifiers).
*/
public static final int NO_VALIDATION = 0x8;

/**
Separator string for TSIdent string parts.
*/
public static final String SEPARATOR = ".";

/**
Separator string between the TSIdent location type and start of the location ID.
*/
public static final String LOC_TYPE_SEPARATOR = ":";

/**
Separator string for TSIdent location parts.
*/
public static final String LOCATION_SEPARATOR = "-";

/**
Separator string for TSIdent source parts.
*/
public static final String SOURCE_SEPARATOR = "-";

/**
Separator string for TSIdent data type parts.
*/
public static final String TYPE_SEPARATOR = "-";

/**
Start of sequence identifier (old sequence number).
*/
public static final String SEQUENCE_NUMBER_LEFT = "[";

/**
End of sequence identifier (old sequence number).
*/
public static final String SEQUENCE_NUMBER_RIGHT = "]";

/**
Separator string for input type and datastore at end of TSID.
*/
public static final String INPUT_SEPARATOR = "~";

/**
The quote can be used to surround TSID parts that have periods, so as to protect the part.
This is typically used with location and data type, although not common.
*/
public static final String PERIOD_QUOTE = "'";

/**
The quote character can be used to surround TSID parts that have periods, so as to protect the part.
This is typically used with location and data type, although not common.
*/
public static final char PERIOD_QUOTE_CHAR = '\'';

/**
The DataFlavor for transferring this specific class.
*/
public static DataFlavor tsIdentFlavor = new DataFlavor(TSIdent.class, "TSIdent");

// Data members.

/**
The whole identifier, including the input type.
*/
private String __identifier;

/**
A comment that can be used to describe the TSID, for example one-line TSTool software comment.
*/
private String __comment = "";

/**
A short alias for the time series identifier.
*/
private String __alias;

/**
The location (combining the main location and the sub-location), does not include the location type.
*/
private String __full_location;

/**
Location type (optional).
*/
private String __locationType = "";

/**
The main location.
*/
private String __main_location;

/**
The sub-location (2nd+ parts of the location, using the LOCATION_SEPARATOR.
*/
private String __sub_location;

/**
The time series data source (combining the main source and the sub-source).
*/
private String __full_source;

/**
The main source.
*/
private String __main_source;

/**
The sub-source.
*/
private String __sub_source;

/**
The time series data type (combining the main and sub types).
*/
private String __full_type;

/**
The main data type.
*/
private String __main_type;

/**
The sub data type.
*/
private String __sub_type;

/**
The time series interval as a string.
*/
private String __interval_string;

/**
The base data interval.
*/
private int	__interval_base;

/**
The data interval multiplier.
*/
private int	__interval_mult;

/**
 * The irregular interval precision, if an irregular interval time series, initialized to TimeInterval.UNKNOWN.
 */
private int __irregularIntervalPrecision = TimeInterval.UNKNOWN;

/**
The time series scenario.
*/
private String __scenario;

/**
Identifier used for ensemble trace (e.g., if a list of time series is grouped as a set of traces in an ensemble,
the trace ID can be the year that the trace starts).
*/
private String __sequenceID;

/**
Type of input (e.g., "DateValue", "RiversideDB")
*/
private String __input_type;

/**
Name of input (e.g., a file, data store, or database connection name).
*/
private String __input_name;

/**
Mask that controls behavior (e.g., how sub-fields are handled).
*/
private int	__behavior_mask;

/**
Construct and initialize each part to empty strings.  Do handle sub-location and sub-source.
*/
public TSIdent () {
	init();
}

/**
Construct using modifiers, indicating how to handle sub-location, etc.
@param mask Behavior mask (see NO_SUB*).
*/
public TSIdent ( int mask ) {
	init ();
	setBehaviorMask ( mask );
}

/**
Construct using a full string identifier, which will be parsed and the individual parts of the identifier set.
@param identifier Full string identifier (optionally, with right-most fields omitted).
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( String identifier )
throws Exception {
	init();
	setIdentifier ( identifier );
}

/**
Construct using a full string identifier, which will be parsed and the individual parts of the identifier set.
@param identifier Full string identifier (optionally, with right-most fields omitted).
@param mask Modifier to control behavior of TSIdent.
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( String identifier, int mask )
throws Exception {
	init();
	setBehaviorMask ( mask );
	setIdentifier ( identifier );
}

/**
Construct using each identifier part.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( String full_location, String full_source, String full_type, String interval_string, String scenario )
throws Exception {
	init();
	setIdentifier ( full_location, full_source, full_type, interval_string, scenario, "", "" );
}

/**
Construct using each identifier part.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param mask Modifier to control behavior of TSIdent.
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( String full_location, String full_source, String full_type, String interval_string, String scenario, int mask )
throws Exception {
	init();
	setBehaviorMask ( mask );
	setIdentifier ( full_location, full_source, full_type, interval_string, scenario, "", "" );
}

/**
Construct using each identifier part.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param input_type Input type.
@param input_name Input name.
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( String full_location, String full_source, String full_type, String interval_string,
	String scenario, String input_type, String input_name )
throws Exception {
	init();
	setIdentifier ( full_location, full_source, full_type, interval_string,	scenario, input_type, input_name );
}

/**
Copy constructor.
@param tsident TSIdent to copy.
@exception if the identifier is invalid (usually the interval is incorrect).
*/
public TSIdent ( TSIdent tsident )
throws Exception {
	// Identifier will get set from its parts.
	init();
	setAlias ( tsident.getAlias() );
	setBehaviorMask ( tsident.getBehaviorMask() );
	// Do not use the following!  It triggers infinite recursion!
	//setIdentifier ( tsident.__identifier );
	setLocationType ( tsident.getLocationType() );
	setIdentifier ( tsident.getLocation(), tsident.getSource(),
			tsident.getType(), tsident.getInterval(),
			tsident.getScenario(), tsident.getSequenceID(),
			tsident.getInputType(), tsident.getInputName() );
	__interval_base = tsident.getIntervalBase ();
	__interval_mult = tsident.getIntervalMult ();
}

/**
Clone the object.  The Object base class clone() method is called, which clones all the TSIdent primitive data.
The result is a complete deep copy.
*/
public Object clone () {
	try {
    TSIdent tsident = (TSIdent)super.clone();
		return tsident;
	}
	catch ( CloneNotSupportedException e ) {
		// Should not happen because everything is cloneable.
		throw new InternalError();
	}
}

/**
Compare identifiers, using a string comparison of the major parts (so that the number of "." does not cause a problem).
This version <b>does not</b> compare the input type and name (use the overloaded version to do so).
@return true if the identifier string equals the instance (if the string does
not match, the individual five main parts are also compared and if they match true is returned).
@param id String identifier to compare.
*/
public boolean equals ( String id ) {
	return equals ( id, false );
}

/**
Compare identifiers, using a string comparison of the major parts (so that the number of "." does not cause a problem).
@return true if the identifier string equals the instance (if the string does
not match, the individual five main parts are also compared and if they match true is returned).
@param id String identifier to compare.
@param include_input If true, the input type and name are included in the comparison.
If false, only the 5-part TSID are checked.
*/
public boolean equals ( String id, boolean include_input ) {
	boolean is_equal = false;
	if ( include_input ) {
		// Do a full comparison.
		if ( __identifier.equalsIgnoreCase(id) ) {
			// Simple compare.
			is_equal = true;
		}
		else {
		    // Compare parts to be sure.
			try {
			    TSIdent ident = new TSIdent ( id );
				if ( ident.getLocation().equalsIgnoreCase(__full_location) &&
					ident.getSource().equalsIgnoreCase(__full_source) &&
					ident.getType().equalsIgnoreCase(__full_type) &&
					ident.getInterval().equalsIgnoreCase(__interval_string)&&
					ident.getScenario().equalsIgnoreCase(__scenario) &&
					ident.getSequenceID().equalsIgnoreCase(__sequenceID) &&
					ident.getInputType().equalsIgnoreCase(__input_type) &&
					ident.getInputName().equalsIgnoreCase(__input_name)){
					is_equal = true;
				}
			}
			catch ( Exception e ) {
				// Usually because the interval is bad.
				is_equal = false;
			}
		}
	}
	else {
	    // Only compare the 5-part identifier.
		// Get the part of the identifier before the first ~.
		int pos = id.indexOf('~');
		if ( pos >= 0 ) {
			// Has a ~ so get the leading part for the remainder of the comparison.
			id = id.substring(0,pos);
		}
		if ( __identifier.equalsIgnoreCase(id) ) {
			// Simple compare.
			is_equal = true;
		}
		else {
		    // Compare parts to be sure.
			try {
			    TSIdent ident = new TSIdent ( id );
				if ( ident.getLocation().equalsIgnoreCase(__full_location) &&
					ident.getSource().equalsIgnoreCase(__full_source) &&
					ident.getType().equalsIgnoreCase(__full_type) &&
					ident.getInterval().equalsIgnoreCase(__interval_string)&&
					ident.getScenario().equalsIgnoreCase(__scenario) &&
					ident.getSequenceID().equalsIgnoreCase(__sequenceID) ) {
					is_equal = true;
				}
			}
			catch ( Exception e ) {
				// Usually because the interval is bad.
				is_equal = false;
			}
		}
	}
	return is_equal;
}

/**
Compare identifiers, using TSIdent.  The input type and name are not compared.
@return true if the TSIdent equals the instance.
@param id TSIdent to compare.
*/
public boolean equals ( TSIdent id ) {
	return equals ( id.getIdentifier(), false );
}

/**
Compare identifiers, using TSIdent.
@return true if the TSIdent equals the instance.
@param id TSIdent to compare.
@param include_input If true, the input type and name are included in the comparison.
If false, only the 5-part TSID is used in the comparison.
*/
public boolean equals ( TSIdent id, boolean include_input ) {
	return equals ( id.getIdentifier(), include_input );
}

/**
Return the time series alias.
@return The alias for the time series.
*/
public String getAlias () {
	return __alias;
}

/**
Return the behavior mask.
@return The behavior mask.
*/
public int getBehaviorMask( ) {
	return __behavior_mask;
}

/**
Return the TSID comment.
@return The TSID comment.
*/
public String getComment() {
	return __comment;
}

/**
Return the full identifier String.
@return The full identifier string.
*/
public String getIdentifier() {
	return toString ( false );
}

/**
Return the full identifier String.
@param include_input  If true, the input type and name will be included in the identifier.
If false, only the 5-part TSID will be included.
@return The full identifier string.
*/
public String getIdentifier ( boolean include_input ) {
	return toString ( include_input );
}

/**
Return the full identifier given the parts.  This method may be called internally.
Null fields are treated as empty strings.
@return The full identifier string given the parts.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
*/
public String getIdentifierFromParts ( String full_location,String full_source, String full_type,
	String interval_string, String scenario) {
	return getIdentifierFromParts ( full_location, full_source, full_type,
		interval_string, scenario, null, "", "" );
}

/**
Return the full identifier given the parts.  This method may be called internally.
Null fields are treated as empty strings.
@return The full identifier string given the parts.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param sequenceID sequence identifier for the time series (in an ensemble).
*/
public String getIdentifierFromParts ( String full_location,String full_source, String full_type,
	String interval_string, String scenario, String sequenceID ) {
	return getIdentifierFromParts ( full_location, full_source, full_type,
		interval_string, scenario, sequenceID, "", "" );
}

/**
Return the full identifier given the parts.  This method may be called internally.
Null fields are treated as empty strings.
@return The full identifier string given the parts.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param input_type Input type.
@param input_name Input name.
*/
public String getIdentifierFromParts ( String full_location,String full_source, String full_type,
	String interval_string, String scenario, String input_type, String input_name ) {
	return getIdentifierFromParts ( full_location, full_source, full_type,
		interval_string, scenario, null, "", "" );
}

/**
Return the full identifier given the parts.  This method may be called internally.
Null fields are treated as empty strings.
@return The full identifier string given the parts.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param sequenceID sequence identifier for the time series (in an ensemble).
@param input_type Input type.  If blank, the input type will not be added.
@param input_name Input name.  If blank, the input name will not be added.
*/
public String getIdentifierFromParts ( String full_location, String full_source, String full_type,
    String interval_string, String scenario, String sequenceID, String input_type, String input_name ) {
    return getIdentifierFromParts ( "", full_location, full_source, full_type,
        interval_string, scenario, sequenceID, input_type, input_name );
}

/**
Return the full identifier given the parts.  This method may be called internally.
Null fields are treated as empty strings.
@return The full identifier string given the parts.
@param locationType location type
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param sequenceID sequence identifier for the time series (in an ensemble).
@param input_type Input type.  If blank, the input type will not be added.
@param input_name Input name.  If blank, the input name will not be added.
*/
public String getIdentifierFromParts ( String locationType, String full_location,
    String full_source, String full_type, String interval_string, String scenario,
    String sequenceID, String input_type, String input_name ) {
	StringBuffer full_identifier = new StringBuffer();

    if ( (locationType != null) && (locationType.length() > 0) ) {
        full_identifier.append ( locationType + LOC_TYPE_SEPARATOR );
    }
	if ( full_location != null ) {
		full_identifier.append ( full_location );
	}
	full_identifier.append ( SEPARATOR );
	if ( full_source != null ) {
		full_identifier.append ( full_source );
	}
	full_identifier.append ( SEPARATOR );
	if ( full_type != null ) {
		full_identifier.append ( full_type );
	}
	full_identifier.append ( SEPARATOR );
	if ( interval_string != null ) {
		full_identifier.append ( interval_string );
	}
	if ( (scenario != null) && (scenario.length() != 0) ) {
		full_identifier.append ( SEPARATOR );
		full_identifier.append ( scenario );
	}
	if ( (sequenceID != null) && (sequenceID.length() != 0) ) {
		full_identifier.append ( SEQUENCE_NUMBER_LEFT + sequenceID + SEQUENCE_NUMBER_RIGHT );
	}
	if ( (input_type != null) && (input_type.length() != 0) ) {
		full_identifier.append ( "~" + input_type );
	}
	if ( (input_name != null) && (input_name.length() != 0) ) {
		full_identifier.append ( "~" + input_name );
	}
	return full_identifier.toString();
}

/**
Return the input name.
@return The input name.
*/
public String getInputName () {
	return __input_name;
}

/**
Return the input type.
@return The input type.
*/
public String getInputType () {
	return __input_type;
}

/**
Return the full interval string.
@return The full interval string.
*/
public String getInterval () {
	return __interval_string;
}

/**
Return the data interval base as an int.
@return The data interval base (see TimeInterval.*).
*/
public int getIntervalBase () {
	return __interval_base;
}

/**
Return the data interval multiplier.
@return The data interval multiplier.
*/
public int getIntervalMult () {
	return __interval_mult;
}

/**
Return the irregular data interval precision as an int.
@return The irregular data interval precision (see TimeInterval.*).
*/
public int getIrregularIntervalPrecision () {
	return __irregularIntervalPrecision;
}

/**
Return the full location, which does not include the location type, but does include the main and sub locations.
@return The full location string.
*/
public String getLocation( ) {
	return __full_location;
}

/**
Return the location type.
@return The location type string.
*/
public String getLocationType( ) {
    return __locationType;
}

/**
Return the main location, which is the first part of the full location (does not include location type).
@return The main location string.
*/
public String getMainLocation( ) {
	return __main_location;
}

/**
Return the main source string.
@return The main source string.
*/
public String getMainSource() {
	return __main_source;
}

/**
Return the main data type string.
@return The main data type string.
*/
public String getMainType() {
	return __main_type;
}

/**
Return the scenario string.
@return The scenario string.
*/
public String getScenario( ) {
	return __scenario;
}

/**
Return the sequence identifier for the time series.
@return The sequence identifier for the time series.
This is meant to be used when an array of time series traces is maintained, for example in an ensemble.
@return time series sequence identifier.
*/
public String getSequenceID () {
    if ( __sequenceID == null ) {
        return "";
    }
    else {
        return __sequenceID;
    }
}

/**
Return the full source string.
@return The full source string.
*/
public String getSource() {
	return __full_source;
}

/**
Return the sub-location, which will be an empty string if __behavior_mask has NO_SUB_LOCATION set.
@return The sub-location string.
*/
public String getSubLocation() {
	return __sub_location;
}

/**
Return the sub-source, which will be an empty string if __behavior_mask has NO_SUB_SOURCE set.
@return The sub-source string.
*/
public String getSubSource( ) {
	return __sub_source;
}

/**
Return the sub-data-type, which will be an empty string if __behavior_mask has NO_SUB_TYPE set.
@return The data sub-type string.
*/
public String getSubType( ) {
	return __sub_type;
}

// FIXME Smalers 2008-04-28 Need to move data transfer out of this class.
/**
Returns the data in the specified DataFlavor, or null if no matching flavor exists.  From the Transferable interface.
@param flavor the flavor in which to return the data.
@return the data in the specified DataFlavor, or null if no matching flavor exists.
*/
public Object getTransferData(DataFlavor flavor) {
	if (flavor.equals(tsIdentFlavor)) {
		return this;
	}
	else {
		return null;
	}
}

/**
Returns the flavors in which data can be transferred.  From the Transferable interface.
The order of the dataflavors that are returned are:<br>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@return the flavors in which data can be transferred.
*/
public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[1];
	flavors[0] = tsIdentFlavor;
	return flavors;
}

/**
Return the data type.
@return The full data type string.
*/
public String getType( ) {
	return __full_type;
}

/**
Initialize data members.
*/
private void init () {
	__behavior_mask = 0; // Default is to process sub-location and sub-source.

	// Initialize to null strings so that there are not problems with the recursive logic.

	__identifier = null;
	__full_location = null;
	__main_location = null;
	__sub_location = null;
	__full_source = null;
	__main_source = null;
	__sub_source = null;
	__full_type = null;
	__main_type = null;
	__sub_type = null;
	__interval_string = null;
	__scenario = null;
	__sequenceID = null;
	__input_type = null;
	__input_name = null;

	setAlias("");

	// Initialize the overall identifier to an empty string.

	setFullIdentifier ("");

	// Initialize the location components.

	setMainLocation("");

	setSubLocation("");

	// Initialize the source.

	setMainSource("");
	setSubSource("");

	// Initialize the data type.

	setType("");
	setMainType("");
	setSubType("");

	// Initialize the interval.

	__interval_base = TimeInterval.UNKNOWN;
	__interval_mult = 0;

	try {
	    setInterval("");
	}
	catch ( Exception e ) {
		// Can ignore here.
	}

	// Initialize the scenario.

	setScenario("");

	// Initialize the input.

	setInputType("");
	setInputName("");
}

/**
Determines whether the specified flavor is supported as a transfer flavor. From the Transferable interface.
The order of the dataflavors that are returned are:<br>
<li>TSIdent - TSIdent.class / RTi.TS.TSIdent</li></ul>
@param flavor the flavor to check.
@return true if data can be transferred in the specified flavor, false if not.
*/
public boolean isDataFlavorSupported(DataFlavor flavor) {
	if (flavor.equals(tsIdentFlavor)) {
		return true;
	}
	else {
	    return false;
	}
}

/**
Compare the string time series identifier to a regular expression, checking the
alias first if it is specified and then the identifier but not including the input parts of the identifier.
See the overloaded version for more information.
@return true if the time series identifier matches the regular expression.
*/
public boolean matches ( String id_regexp ) {
	return matches ( id_regexp, true, false );
}

/**
Compare the string time series identifier to a limited regular expression, where only * is recognized.
Because time series identifiers use the "." character, the regular expression should only contain the "*" wildcard, as needed.
The full string is first compared using normal Java conventions and treating dots as literals.
If the previous check does not produce a match and if the regular expression does contain ".",
then the regular expression is split into time series identifier parts and
a string comparison of the major parts is done (so that the number of "." does not cause a problem).
This version <b>does not</b> compare the input type and name (use the overloaded version to do so).
See the overloaded version for more information.
@param id_regexp String identifier to compare,
with identifier fields containing regular expressions using glob syntax (* will be replaced with Java-style .*).
@param check_alias If true, check the alias first for a match.  If not matched, the identifier is checked.
If false, the alias is not checked and only the identifier string is checked.
@return true if the identifier string matches the instance (if the string does not match,
the individual five main parts are also compared and if they match true is returned).
Wild-cards are allowed in the identifier. Comparisons are done case-independent by converting strings to upper-case.
*/
public boolean matches ( String idGlobRegexp, boolean check_alias, boolean include_input ) {
	// Do a comparison on the whole string.  The user may or may not have defined an alias with periods.
    // Only allow * wildcards when matching the whole string so replace . with literal.
    String java_regexp=StringUtil.replaceString(idGlobRegexp,".","\\.").toUpperCase();
    // Replace * used in calling code with .* so java string comparison works.
    java_regexp=StringUtil.replaceString(java_regexp,"*",".*").toUpperCase();
    // TODO smalers 2015-06-02 Do all of the following need to be escaped or let some flow through?:  \.[]{}()*+-?^$|
 	//      In particular the period is used in TSID, [] is used for ensembles, and \ could be in file path
 	//      The following has generally worked for years.
    // Also replace ${ from property notation with \$\{ because these characters have meaning in regular expressions.
    java_regexp=java_regexp.replace("${", "\\$\\{").replace("}", "\\}");
    if ( check_alias && (__alias != null) && (__alias.length() > 0) && __alias.toUpperCase().matches(java_regexp) ) {
        return true;
    }
    if ( __identifier.toUpperCase().matches(java_regexp) ) {
        return true;
    }
    // If here, check to see if the original string contains periods that that indicate that identifier parts need checked:
    // - period will only be present if TSID with parts is specified
    if ( idGlobRegexp.indexOf(".") >= 0 ) {
		// Regular expression to match contains parts so compare the parts.
		try {
		    TSIdent tsident = new TSIdent ( idGlobRegexp );
			return matches (
					tsident.getLocation(),
					tsident.getSource(),
					tsident.getType(),
					tsident.getInterval(),
					tsident.getScenario(),
					tsident.getSequenceID(),
					tsident.getInputType(),
					tsident.getInputName(),
					include_input );
		}
		catch ( Exception e ) {
			return false;
		}
	}
    return false; // Fall-through.
}

/**
Compare identifiers, using a string comparison of all the identifier parts, including the input type and name.
See the overloaded version for more information.
@return true if the identifier string matches the instance (if the string does not match,
the individual five main parts are also compared and if they match true is returned).
Wild-cards are allowed in the identifier.
@param id_regexp String identifier to compare, with identifier fields containing regular expressions.
@param include_input If true, the input type and name are included in the
comparison.  If false, only the 5-part TSID are checked.
@deprecated Use the overloaded method that takes an option for the alias.
*/
public boolean matches ( String id_regexp, boolean include_input ) {
	try {
        TSIdent tsident = new TSIdent ( id_regexp );
		return matches ( tsident.getLocation(), tsident.getSource(),
				tsident.getType(), tsident.getInterval(),
				tsident.getScenario(),
				tsident.getSequenceID(), null, null,
				include_input );
	}
	catch ( Exception e ) {
		return false;
	}
}

/**
Compare identifiers, using a string comparison of the major parts (so that the number of "." does not cause a problem).
The instance fields will be compared to the specified regular expressions.
The instance fields can also contain regular expressions with "*" to indicate to match everything.
This allows some fields to be ignored (always matched).
The regular expressions can be either "*" (match all), a literal string (blank is a literal string) or a combination using "*".
Each "*" is converted internally to ".*" and the String.matches() method is called.
Comparisons are done case-independent by converting all strings to uppercase.
@return true if the identifier string equals the instance (if the string does not match,
the individual five main parts are also compared and if they match true is returned).
@param location_regexp Location regular expression to compare.
@param source_regexp Data source regular expression to compare.
@param data_type_regexp Data type regular expression to compare.
@param interval_regexp Data interval regular expression to compare.
@param scenario_regexp Scenario regular expression to compare.
@param input_type_regexp Input type regular expression to compare.
@param input_name_regexp Input name regular expression to compare.
@param include_input If true, the input type and name are included in the comparison.  If false, only the 5-part TSID are checked.
*/
public boolean matches ( String location_regexp, String source_regexp, String data_type_regexp,
    String interval_regexp, String scenario_regexp, String input_type_regexp,
	String input_name_regexp, boolean include_input ) {
	return matches ( location_regexp, source_regexp, data_type_regexp, interval_regexp,
		scenario_regexp, null, input_type_regexp, input_name_regexp, include_input );
}

/**
Compare identifiers, using a string comparison of the major parts (so that the number of "." does not cause a problem).
The instance fields will be compared to the specified regular expressions.
The instance fields can also contain regular expressions with "*" to indicate to match everything.
This allows some fields to be ignored (always matched).
The regular expressions can be either "*" (match all), a literal string (blank is a literal string) or a combination using "*".
Each "*" is converted internally to ".*" and the String.matches() method is called.
Comparisons are done case-independent by converting all strings to uppercase.
If include_input=true, the input type and name will be checked, even if they are blank.
Therefore, it may be important in some cases that calling code check to see whether the
input information is part of the time series identifier and pass true only if such information
is actually available in both the instance and the values being checked.
@param location_regexp Location regular expression to compare.
@param source_regexp Data source regular expression to compare.
@param data_type_regexp Data type regular expression to compare.
@param interval_regexp Data interval regular expression to compare.
@param scenario_regexp Scenario regular expression to compare.
@param sequenceID_regexp sequence identifier regular expression to compare.
@param input_type_regexp Input type regular expression to compare.
@param input_name_regexp Input name regular expression to compare.
@param include_input If true, the input type and name are included in the comparison.
If false, only the 5-part TSID are checked.
@return true if the identifier string equals the instance (if the string does not match,
the individual five main parts are also compared and if they match true is returned).
*/
public boolean matches ( String location_regexp, String source_regexp, String data_type_regexp,
    String interval_regexp, String scenario_regexp, String sequenceID_regexp, String input_type_regexp,
	String input_name_regexp, boolean include_input ) {
	// Do the comparison by comparing each part.  Check for mismatches and if any occur return false.
	// Then if at the end, the TSIdent must match.
	String routine = "TSIdent.matches";
	if ( Message.isDebugOn ) {
		Message.printDebug ( 5, routine, "Checking match of \"" + toString(true) + " with loc=\"" +
		location_regexp + "\" source=\"" + source_regexp +
		"\" type=\"" + data_type_regexp + "\" interval=\"" +
		interval_regexp + "\" scenario=\"" + scenario_regexp +
		"\" sequenceID=" + sequenceID_regexp + " input_type=\"" +
		input_type_regexp + "\" inputname=\"" + input_name_regexp +
		"\" include_input=" +include_input );
	}
	// Replace "*" in the regular expressions with ".*", which is necessary to utilize the Java matches() method.
    // TODO smalers 2015-06-02 Do all of the following need to be escaped or let some flow through?:  \.[]{}()*+-?^$|
 	//      In particular the period is used in TSID, [] is used for ensembles, and \ could be in file path.
 	//      The following has generally worked for years.
    // Also replace ${ from property notation with \$\{ because these characters have meaning in regular expressions.
	String location_regexp_Java = StringUtil.replaceString( location_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	String source_regexp_Java = StringUtil.replaceString( source_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	String data_type_regexp_Java = StringUtil.replaceString( data_type_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	String interval_regexp_Java = StringUtil.replaceString( interval_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	String scenario_regexp_Java = StringUtil.replaceString( scenario_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	String sequenceID_regexp_Java = null;
	if ( sequenceID_regexp != null ) {
	    sequenceID_regexp_Java = StringUtil.replaceString( sequenceID_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
	}
	// Compare the 5-part identifier parts first.
	if ( !__full_location.toUpperCase().matches(location_regexp_Java) ) {
		return false;
	}
	if ( !__full_source.toUpperCase().matches(source_regexp_Java) ) {
		return false;
	}
	if ( !__full_type.toUpperCase().matches(data_type_regexp_Java) ) {
		return false;
	}
	if ( !__interval_string.toUpperCase().matches(interval_regexp_Java) ){
		return false;
	}
	if ( (sequenceID_regexp != null) && !__sequenceID.toUpperCase().matches(sequenceID_regexp_Java) ) {
		return false;
	}
	if ( !__scenario.toUpperCase().matches(scenario_regexp_Java) ) {
		return false;
	}
	if ( include_input ) {
		String input_type_regexp_Java = StringUtil.replaceString( input_type_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
		String input_name_regexp_Java = StringUtil.replaceString( input_name_regexp,"*",".*").replace("${", "\\$\\{").replace("}", "\\}").toUpperCase();
		// Sometimes input_type_regexp is blank.  In this case do not use to compare.
		// TODO smalers 2007-06-22 Not sure why regexp would not be OK here and below?
		if ( (input_type_regexp.length() > 0) && !__input_type.toUpperCase().matches(input_type_regexp_Java) ) {
			return false;
		}
		// Typical that input_name_regexp is blank.  In this case do not use to compare.
		if ( (input_name_regexp.length() > 0) && !__input_name.toUpperCase().matches(input_name_regexp_Java) ) {
			return false;
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( 5, routine, "TSIdent matches" );
	}
	return true;
}

/**
Parse a TSIdent instance given a String representation of the identifier.
The behavior flag is assumed to be zero.
@return A TSIdent instance given a full identifier string.
@param identifier Full identifier as string.
@exception if an error occurs (usually a bad interval).
*/
public static TSIdent parseIdentifier ( String identifier )
throws Exception {
	return parseIdentifier ( identifier, 0 );
}

/**
Parse a TSIdent instance given a String representation of the identifier.
@return A TSIdent instance given a full identifier string.
@param identifier Full identifier as string.
@param behavior_flag Behavior mask to use when creating instance.
@exception if an error occurs (usually a bad interval).
*/
public static TSIdent parseIdentifier (	String identifier, int behavior_flag )
throws Exception {
	String routine="TSIdent.parseIdentifier";
	int	dl = 100;

	// Declare a TSIdent which is filled and returned.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Declare TSIdent within this routine..." );
	}
	TSIdent	tsident = new TSIdent ( behavior_flag );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "...done declaring TSIdent" );
	}

	// First parse the datastore and input type information:
	// - does not matter if TSID parts are quoted at this point

	String identifierNoInputName = identifier;
	List<String> inputTypeList = StringUtil.breakStringList ( identifier, "~", 0 );
	if ( inputTypeList != null ) {
		int nlist = inputTypeList.size();
		// Reset to first part for processing below checks below.
		identifierNoInputName = inputTypeList.get(0);
		if ( nlist == 2 ) {
			tsident.setInputType ( inputTypeList.get(1) );
		}
		else if ( nlist >= 3 ) {
			tsident.setInputType ( inputTypeList.get(1) );
			// File name may have a ~ so find the second instance of ~ and use the remaining string.
			int pos = identifier.indexOf ( "~" );
			if ( (pos >= 0) && identifier.length() > (pos + 1) ) {
				// Have something at the end.
				String sub = identifier.substring ( pos + 1 );
				pos = sub.indexOf ( "~" );
				if ( (pos >= 0) && (sub.length() > (pos + 1))) {
					// The rest is the file.
					tsident.setInputName ( sub.substring(pos + 1) );
				}
			}
		}
	}

	// Now parse the 5-part identifier that does not have trailing ~.

	String full_location = "", full_source = "", interval_string = "", scenario = "", full_type = "";

	List<String> tsidPartList = null;  // TSID parts, as delimited by period (.).
	int posQuote = identifierNoInputName.indexOf(PERIOD_QUOTE);
	// 'list' below are the TSID parts split by periods:
	//   list[0] = location
	//   list[1] = data source
	//   list[2] = data type
	//   list[3] = data interval
	//   list[4] = scenario, with [trace] at end
	if ( posQuote >= 0 ) {
		// Have at least one quote so assume TSID something like the following where data type is escaped:
		//   LocaId.Source.'DataType-some.parts.with.periods'.Interval
		// or location ID is escaped:
		//   'LocaId.xx'.Source.'DataType-some.parts.with.periods'.Interval
		tsidPartList = parseIdentifier_SplitWithQuotes(identifierNoInputName);
	}
	else {
		// No quote in TSID so do simple parse.
		tsidPartList = StringUtil.breakStringList ( identifierNoInputName, ".", 0 );
	}
	int tsidPartListSize = tsidPartList.size();
	for ( int i = 0; i < tsidPartListSize; i++ ) {
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "TS ID list[" + i + "]:  \"" + tsidPartList.get(i) + "\"" );
		}
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Full TS ID:  \"" + identifier +"\"");
	}

	// Parse out location and split the rest of the ID.
	//
	// FIXME smalers 2013-06-16 Actually, may need quotes for more new use cases where periods are in identifier parts.
	// This field is allowed to be surrounded by quotes since some locations cannot be identified by a simple string.
	// Allow either ' or " to be used and bracket it.

	// Location type:
	// - does not need to be surrounded by single quotes
	int locationTypeSepPos = -1;
	String locationPart = tsidPartList.get(0);  // LocationType:FullLocation or FullLocation.
	String locationIdPart = locationPart; // Full string used if location type is not used.
	locationTypeSepPos = locationPart.indexOf(LOC_TYPE_SEPARATOR);
	String locationType = "";
	if ( locationTypeSepPos >= 0 ) {
	    // Have a location type so split out and set, then treat the rest of the location
	    // as the location identifier for further processing.
	    locationType = locationPart.substring(0,locationTypeSepPos);
	    // Remaining location part is the remainder of the LocType:locationPart string,
	    // which will be processed below.
	    locationIdPart = locationPart.substring(locationTypeSepPos + 1);
	}

	// Location identifier (without leading LocationType: from above):
	// - may be surrounded by single quotes (TODO smalers 2020-02-13 double quotes allowed for history)
	if ( tsidPartListSize >= 1 ) {
		// TODO smalers 2020-02-13 treat like datatype and keep surrounding quotes.
		//if ( (locationIdPart.charAt(0) == '\'') || (locationIdPart.charAt(0) == '\"')) {
			// Read the location excluding the delimiter
		//	full_location = StringUtil.readToDelim ( locationPart.substring(1), locationPart.charAt(0) );
		//}
		//else {
				full_location = locationIdPart;
		//	}
	}

	// Data source.
	if ( tsidPartListSize >= 2 ) {
		full_source = tsidPartList.get(1);
	}

	// Data type.
	if ( tsidPartListSize >= 3 ) {
		// Data type will include surrounding quotes - otherwise would need to add when rebuilding the full TSID string.
		full_type = tsidPartList.get(2);
	}

	// Data interval.
	String sequenceId = null;
	if ( tsidPartListSize >= 4 ) {
		interval_string = tsidPartList.get(3);
		// If no scenario is used, the interval string may have the sequence ID on the end,
		// so search for the [ and split the sequence ID out of the interval string.
		int index = interval_string.indexOf ( SEQUENCE_NUMBER_LEFT );
		// Get the sequence ID.
		if ( index >= 0 ) {
			if ( interval_string.endsWith(SEQUENCE_NUMBER_RIGHT)){
				// Should be a properly-formed sequence ID, but need to remove the brackets.
				sequenceId = interval_string.substring( index + 1, interval_string.length() - 1).trim();
			}
			if ( index == 0 ) {
				// There is no interval, just the sequence ID (should not happen).
				interval_string = "";
			}
			else {
                interval_string = interval_string.substring(0,index);
			}
		}
	}

	// Scenario:
	// - it is possible that the scenario has delimiters in it
	// - therefore, need to concatenate all the remaining fields to compose the complete scenario
	if ( tsidPartListSize >= 5 ) {
		StringBuffer scenarioBuffer = new StringBuffer();
		scenarioBuffer.append ( tsidPartList.get(4) );
		for ( int i = 5; i < tsidPartListSize; i++ ) {
			// Append the delimiter to create the original un-parsed string.
			scenarioBuffer.append ( "." );
			scenarioBuffer.append ( tsidPartList.get(i) );
		}
		scenario = scenarioBuffer.toString ();
	}
	// The scenario may now have the sequence ID on the end, search for the [ and split out of the scenario.
	int index = scenario.indexOf ( SEQUENCE_NUMBER_LEFT );
	// Get the sequence ID.
	if ( index >= 0 ) {
		if ( scenario.endsWith(SEQUENCE_NUMBER_RIGHT) ) {
			// Should be a properly-formed sequence ID.
			sequenceId = scenario.substring ( index + 1, scenario.length() - 1 ).trim();
		}
		if ( index == 0 ) {
			// There is no scenario, just the sequence ID.
			scenario = "";
		}
		else {
            scenario = scenario.substring(0,index);
		}
	}
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
			"After split: full_location=\"" + full_location +
			"\" full_source=\"" + full_source +
			"\" type=\"" + full_type +
			"\" interval=\"" + interval_string +
			"\" scenario=\"" + scenario +
			"\" sequenceId=\"" + sequenceId + "\"" );
	}

	// Now set the identifier component parts.

	tsident.setLocationType ( locationType );
	tsident.setLocation ( full_location );
	tsident.setSource ( full_source );
	tsident.setType ( full_type );
	tsident.setInterval ( interval_string );
	tsident.setScenario ( scenario );
	tsident.setSequenceID ( sequenceId );

	// Return the TSIdent object for use elsewhere.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Returning local TSIdent..." );
	}
	return tsident;
}

/**
Parse a TSID that has quoted part with periods in one or more parts, handling each part:
<pre>
   abc
   'abc'
   'abc'xyz'efg'
</pre>
@param identifier TSID main part (no ~).
@return list of parts for TSID
*/
private static List<String> parseIdentifier_SplitWithQuotes(String identifier) {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = StringUtil.class.getSimpleName() + "parseIdentifier_SplitWithQuotes";
	}
	// Process by getting one token at a time:
	// - tokens are between periods
	// - if first character of part is single quote, get to the next single quote
	// - also allow multiple single-quoted strings in a part
	List<String> parts = new ArrayList<>();
	boolean inPart = true; // Should always have a part at the front.
	boolean inQuote = false;
	char c;
	StringBuilder b = new StringBuilder();
	int ilen = identifier.length();
	// Use debug messages to help with troubleshooting.
	for ( int i = 0; i < ilen; i++ ) {
		c = identifier.charAt(i);
		if ( Message.isDebugOn ) {
			Message.printDebug(1, routine, "Character is: " + c);
		}
		if ( c == '.' ) {
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Found period" );
			}
			if ( inQuote ) {
				// In a quote so just keep adding characters.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "In quote" );
				}
				b.append(c);
			}
			else {
				// Not in quote.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "Not in quote" );
				}
				if ( inPart ) {
					// Between periods.  Already in part so end it without adding period.
					if ( Message.isDebugOn ) {
						Message.printDebug(1, routine, "In part, ending part" );
					}
					parts.add(b.toString());
					b.setLength(0);
					// Will be in part at top of loop because current period will be skipped:
					// - but if last period treat the following part as empty string
					if ( i == (ilen - 1) ) {
						// Add an empty string
						parts.add("");
					}
					else {
						// Keep processing.
						// Set to not be in part.
						inPart = false;
						--i; // Re-process period to trigger in a part in next iteration.
					}
				}
				else {
					// Was not in a part so start it.
					if ( Message.isDebugOn ) {
						Message.printDebug(1, routine, "Not in part, starting part" );
					}
					inPart = true;
					// Don't add period to part.
				}
			}
		}
		else if ( c == PERIOD_QUOTE_CHAR ) {
			// Found a quote, which will surround a point, as in:  .'some.part.xx'.
			if ( Message.isDebugOn ) {
				Message.printDebug(1, routine, "Found quote" );
			}
			if ( inQuote ) {
				// At the end of the quoted part.
				// Always include the quote in the part.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "In quote, ending quote" );
				}
				b.append(c);
				// Next period immediately following will cause next part to be started, even if period at end of string.
				inQuote = false;
				if ( (i == (ilen - 1)) || (identifier.charAt(i + 1) == '.') ) {
					// Only end the part if at the end of the identifier or the next character is a period.
					// Otherwise parts with multiple quoted sub-parts like .'abc'-'xyz'. are not handled.
					parts.add(b.toString());
					b.setLength(0);
					// Only indicate done with part if the next character is a period.
					inPart = false;
				}
			}
			else {
				// Need to start a part.
				if ( Message.isDebugOn ) {
					Message.printDebug(1, routine, "Not in quote, starting quote" );
				}
				b.append(c); // Keep the quote.
				inQuote = true;
			}
		}
		else {
			// Normal character to add to part.
			b.append(c);
			if ( i == (ilen - 1) ) {
				// Last part.
				parts.add(b.toString());
			}
		}
	}
	if ( Message.isDebugOn ) {
		for ( String s : parts ) {
			Message.printDebug(1, routine, "TSID part is \"" + s + "\"");
		}
	}
	return parts;
}

/**
Set the time series alias.
@param alias Alias for the time series.
*/
public void setAlias ( String alias ) {
	if ( alias != null ) {
		__alias = alias;
	}
}

/**
Set the behavior mask.  The behavior mask controls how identifier sub-parts are joined into the full identifier.
Currently this routine does a full reset (not bit-wise).
@param behavior_mask Behavior mask that controls how sub-fields are handled.
*/
public void setBehaviorMask( int behavior_mask ) {
	__behavior_mask = behavior_mask;
}

/**
Set the comment.
@param comment Comment for the identifier.
*/
public void setComment ( String comment ) {
	if ( comment != null ) {
		__comment = comment;
	}
}

/**
Set the full identifier (this does not result in a parse).  It is normally only called from within this class.
@param full_identifier Full identifier string.
*/
private void setFullIdentifier ( String full_identifier ) {
	if ( full_identifier == null ) {
		return;
	}
	__identifier = full_identifier;
	// DO NOT call setIdentifier() from here!
}

/**
Set the full location (this does not result in a parse).  It is normally only called from within this class.
@param full_location Full location string.
*/
private void setFullLocation ( String full_location ) {
	if ( full_location == null ) {
		return;
	}
	__full_location = full_location;
	// DO NOT call setIdentifier() from here!
}

/**
Set the full source (this does not result in a parse).  It is normally only called from within this class.
@param full_source Full source string.
*/
private void setFullSource ( String full_source ) {
	if ( full_source == null ) {
		return;
	}
	__full_source = full_source;
	// DO NOT call setIdentifier() from here!
}

/**
Set the full data type (this does not result in a parse).  It is normally only called from within this class.
@param full_type Full data type string.
*/
private void setFullType ( String full_type ) {
	if ( full_type == null ) {
		return;
	}
	__full_type = full_type;
	// DO NOT call setIdentifier() from here!
}

/**
Set the full identifier from its parts.
*/
public void setIdentifier ( ) {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setIdentifier(void)";
	}
	int	dl = 100;

	// Assume that all the individual set routines have handled the
	// __behavior_mask accordingly and therefore can just concatenate strings here.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine,
		"Setting full identifier from parts: \"" + __full_location +
		"." + __full_source + "." + __full_type +"."+__interval_string +
		"." + __scenario + "~" + __input_type + "~" + __input_name );
	}

	String full_identifier;
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Calling getIdentifierFromParts..." );
	}
	full_identifier = getIdentifierFromParts(__locationType, __full_location, __full_source,
		__full_type, __interval_string, __scenario, __sequenceID, __input_type, __input_name );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "...successfully called getIdentifierFromParts..." );
	}

	setFullIdentifier ( full_identifier );

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "ID..." );
		Message.printDebug ( dl, routine, "\"" + __identifier + "\"" );
	}
}

/**
Set the identifier by parsing the given string.
@param identifier Full identifier string.
@exception if the identifier cannot be set (usually the interval is incorrect).
*/
public void setIdentifier ( String identifier )
throws Exception {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setIdentifier";
	}
	int	dl = 100;

	if ( identifier == null ) {
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Trying to set identifier to \"" + identifier + "\"" );
	}

	if ( identifier.length() == 0 ) {
		// Cannot parse the identifier because doing so would result in an infinite loop.
		// If this routine is being called with an empty string, it is a mistake.
		// The initialization code will call setFullIdentifier() directly.
		if ( Message.isDebugOn ) {
			Message.printDebug ( dl, routine, "Identifier string is empty, not processing!" );
		}
		return;
	}

	// Parse the identifier using the public static function to create a temporary identifier object.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Done declaring temp TSIdent." );
		Message.printDebug ( dl, routine, "Parsing identifier..." );
	}
	TSIdent tsident = parseIdentifier ( identifier, __behavior_mask );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "...back from parsing identifier" );
	}

	// Now copy the temporary copy into this instance.

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Setting the individual parts..." );
	}
	setLocationType( tsident.getLocationType() );
	setLocation( tsident.getLocation() );
	setSource( tsident.getSource() );
	setType( tsident.getType() );
	setInterval( tsident.getInterval() );
	setScenario( tsident.getScenario() );
	setSequenceID ( tsident.getSequenceID() );
	setInputType ( tsident.getInputType() );
	setInputName ( tsident.getInputName() );
	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "... done setting the individual parts" );
	}
}

/**
Set the identifier given the main parts, but no sequence ID, input type, or input name.
@param full_location Full location string.
@param full_source Full source string.
@param full_type Full data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@exception if the identifier cannot be set (usually the interval is incorrect).
*/
public void setIdentifier (	String full_location, String full_source,
	String full_type, String interval_string, String scenario )
throws Exception {
	setLocation ( full_location );
	setSource ( full_source );
	setType ( full_type );
	setInterval ( interval_string );
	setScenario ( scenario );
}

/**
Set the identifier given the parts, but not including the sequence ID.
@param full_location Full location string.
@param full_source Full source string.
@param type Data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param input_type Input type.
@param input_name Input name.
@exception if the identifier cannot be set (usually the interval is incorrect).
*/
public void setIdentifier (	String full_location, String full_source,
	String type, String interval_string, String scenario, String input_type, String input_name )
throws Exception {
	setLocation ( full_location );
	setSource ( full_source );
	setType ( type );
	setInterval ( interval_string );
	setScenario ( scenario );
	setInputType ( input_type );
	setInputName ( input_name );
}

/**
Set the identifier given the parts, including sequence ID.
@param full_location Full location string.
@param full_source Full source string.
@param type Data type.
@param interval_string Data interval string.
@param scenario Scenario string.
@param sequenceID sequence identifier (for time series in ensemble).
@param input_type Input type.
@param input_name Input name.
@exception if the identifier cannot be set (usually the interval is incorrect).
*/
public void setIdentifier (	String full_location, String full_source, String type, String interval_string,
	String scenario, String sequenceID, String input_type, String input_name )
throws Exception {
	setLocation ( full_location );
	setSource ( full_source );
	setType ( type );
	setInterval ( interval_string );
	setScenario ( scenario );
	setSequenceID ( sequenceID );
	setInputType ( input_type );
	setInputName ( input_name );
}

/**
Set the input name.
The input name.
*/
public void setInputName ( String input_name ) {
	if ( input_name != null ) {
		__input_name = input_name;
	}
}

/**
Set the input type.
The input type.
*/
public void setInputType ( String input_type ) {
	if ( input_type != null ) {
		__input_type = input_type;
	}
}

/**
Set the interval given the interval string.
@param interval_string Data interval string.
@exception if there is an error parsing the interval string.
*/
public void setInterval ( String interval_string )
throws Exception {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setInterval(String)";
	}
	int	dl = 10;
	TimeInterval tsinterval = null;

	if ( interval_string == null ) {
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Setting interval to \"" + interval_string + "\"" );
	}

	if ( !interval_string.equals("*") && interval_string.length() > 0 ) {
		// First split the string into its base and multiplier for regular interval, or detect irregular.

   		if ( Message.isDebugOn ) {
   			Message.printDebug ( dl, routine, "Parsing interval \"" + interval_string + "\"" );
   		}

        if ( (__behavior_mask & NO_VALIDATION) == 0 ) {
            try {
                tsinterval = TimeInterval.parseInterval ( interval_string );
            }
            catch ( Exception e ) {
                // Not validating so ignore the exception.
            }
        }
        else {
            // Want to validate so throw an exception.
            tsinterval = TimeInterval.parseInterval ( interval_string );
        }

		// Now set the base and multiplier.
        if ( tsinterval != null ) {
    		__interval_base = tsinterval.getBase();
    		__interval_mult = tsinterval.getMultiplier();
    		if ( __interval_base == TimeInterval.IRREGULAR ) {
    			__irregularIntervalPrecision = tsinterval.getIrregularIntervalPrecision();
    		}
    		if ( Message.isDebugOn ) {
    			Message.printDebug ( dl, routine, "Setting interval base to " + __interval_base	+ " (" +
    				TimeInterval.getName(__interval_base, 0) + ") mult: " + __interval_mult );
    			if ( tsinterval.isIrregularInterval() ) {
    				Message.printDebug ( dl, routine, "  Interval is irregular, precision is " +
    					this.__irregularIntervalPrecision + " (" + TimeInterval.getName(this.__irregularIntervalPrecision, 0) + ")." );
    			}
    		}
        }
	}
	// Else, don't do anything (leave as zero initialized values).

	// Now set the interval string.  Use the given interval base string because need to preserve existing file names, etc.

	setIntervalString ( interval_string );
	setIdentifier();
}

/**
Set the interval given the interval integer values.
@param interval_base Base interval (see TimeInterval.*).
@param interval_mult Base interval multiplier.
*/
public void setInterval ( int interval_base, int interval_mult ) {
	if ( interval_mult <= 0 ) {
		Message.printWarning ( 2, "TSIdent.setInterval", "Interval multiplier (" + interval_mult +
                " must be greater than zero" );
	}
	if ( (interval_base != TimeInterval.SECOND) &&
		(interval_base != TimeInterval.MINUTE) &&
		(interval_base != TimeInterval.HOUR) &&
		(interval_base != TimeInterval.DAY) &&
		(interval_base != TimeInterval.WEEK) &&
		(interval_base != TimeInterval.MONTH) &&
		(interval_base != TimeInterval.YEAR) &&
		(interval_base != TimeInterval.IRREGULAR) ) {
		Message.printWarning ( 2, "TSIdent.setInterval", "Base interval (" + interval_base + ") is not recognized" );
		return;
	}
	__interval_base = interval_base;
	__interval_mult = interval_mult;

	// Now need to set the string representation of the interval.

	StringBuffer interval_string = new StringBuffer ();
	if ( (interval_base != TimeInterval.IRREGULAR) && (interval_mult != 1) ) {
		interval_string.append ( interval_mult );
	}

	if ( interval_base == TimeInterval.SECOND ) {
		interval_string.append ( "sec" );
	}
	else if	( interval_base == TimeInterval.MINUTE ) {
		interval_string.append ( "min" );
	}
	else if	( interval_base == TimeInterval.HOUR ) {
		interval_string.append ( "hour" );
	}
	else if	( interval_base == TimeInterval.DAY ) {
		interval_string.append ( "day" );
	}
	else if	( interval_base == TimeInterval.WEEK ) {
		interval_string.append ( "week" );
	}
	else if	( interval_base == TimeInterval.MONTH ) {
		interval_string.append ( "month" );
	}
	else if	( interval_base == TimeInterval.YEAR ) {
		interval_string.append ( "year" );
	}
	else if	( interval_base == TimeInterval.IRREGULAR ) {
		interval_string.append ( "irreg" );
	}

	setIntervalString ( interval_string.toString() );
	setIdentifier ();
}

/**
Set the interval string.  This is normally only called from this class.
@param interval_string Interval string.
*/
private void setIntervalString ( String interval_string ) {
	if ( interval_string != null ) {
		__interval_string = interval_string;
	}
}

/**
Set the full location from its parts.
This method is generally called from setMainLocation() and setSubLocation() methods to reset __full_location.
*/
public void setLocation () {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setLocation";
	}
	int	dl = 100;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Resetting full location from parts..." );
	}
	if ( (__behavior_mask & NO_SUB_LOCATION) != 0 ) {
		// Just use the main location as the full location.
		if ( __main_location != null ) {
			// There should always be a main location after the object is initialized.
			setFullLocation ( __main_location );
		}
	}
	else {
        // Concatenate the main and sub-locations to get the full location.
		StringBuffer full_location = new StringBuffer ();
		// May want to check for __main_location[] also.
		if ( __main_location != null ) {
			// This should always be the case after the object is initialized.
			full_location.append ( __main_location );
			if ( __sub_location != null ) {
				// Only want to add the sub-location if it is not an empty string
				// (it will be an empty string after the object is initialized).
				if ( __sub_location.length() > 0 ) {
					// Have a sub_location so append it to the main location.
					full_location.append ( LOCATION_SEPARATOR );
					full_location.append ( __sub_location );
				}
			}
			setFullLocation ( full_location.toString() );
		}
	}
	// Now reset the full identifier.
	setIdentifier ();
}

/**
Set the full location from its parts.
@param main_location The main location string.
@param sub_location The sub-location string.
*/
public void setLocation ( String main_location, String sub_location ) {
	setMainLocation ( main_location );
	setSubLocation ( sub_location );
	// The full location will be set when the parts are set.
}

/**
Set the full location from its full string.
@param full_location The full location string.
*/
public void setLocation( String full_location ) {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setLocation(String)";
	}
	int	dl = 100;

	if ( full_location == null ) {
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Trying to set location to \"" + full_location + "\"" );
	}

	if ( (__behavior_mask & NO_SUB_LOCATION) != 0 ) {
		// The entire string passed in is used for the main location.
		setMainLocation ( full_location );
	}
	else {
	    // Need to split the location into main and sub-location.
		List<String> list;
		StringBuffer sub_location = new StringBuffer ();
		int nlist;
		list = StringUtil.breakStringList ( full_location, LOCATION_SEPARATOR, 0 );
		nlist = list.size();
		if ( nlist >= 1 ) {
			// Set the main location.
			setMainLocation ( list.get(0) );
		}
		if ( nlist >= 2 ) {
			// Now set the sub-location.
			// This allows for multiple delimited parts (everything after the first delimiter is treated as the sublocation).
			int iend = nlist - 1;
			for ( int i = 1; i <= iend; i++ ) {
				if ( i != 1 ) {
					sub_location.append ( LOCATION_SEPARATOR );
				}
				sub_location.append (list.get(i));
			}
			setSubLocation ( sub_location.toString() );
		}
		else {
		    // Since only setting the main location need to set the sub-location to an empty string.
			setSubLocation ( "" );
		}
	}
}

/**
Set the location type.
@param locationType location type.
*/
public void setLocationType ( String locationType ) {
   if ( locationType == null ) {
        return;
    }
    __locationType = locationType;
    setIdentifier ();
}

/**
Set the main location string (and reset the full location).
@param main_location The main location string.
*/
public void setMainLocation ( String main_location ) {
	if ( main_location == null ) {
		return;
	}
	__main_location = main_location;
	setLocation();
}

/**
Set the main source string (and reset the full source).
@param main_source The main source string.
*/
public void setMainSource ( String main_source ) {
	if ( main_source == null ) {
		return;
	}
	__main_source = main_source;
	setSource();
}

/**
Set the main data type string (and reset the full type).
@param main_type The main data type string.
*/
public void setMainType ( String main_type ) {
	if ( main_type == null ) {
		return;
	}
	__main_type = main_type;
	setType();
}

/**
Set the scenario string.
@param scenario The scenario string.
*/
public void setScenario( String scenario ) {
	if ( scenario == null ) {
		return;
	}
	__scenario = scenario;
	setIdentifier ();
}

/**
Set the sequence identifier, for example when the time series is part of an ensemble.
@param sequenceID sequence identifier for the time series.
*/
public void setSequenceID ( String sequenceID ) {
	__sequenceID = sequenceID;
	setIdentifier ();
}

/**
Set the full source from its parts.
*/
public void setSource ( ) {
	if ( (__behavior_mask & NO_SUB_SOURCE) != 0 ) {
		// Just use the main source as the full source.
		if ( __main_source != null ) {
			// There should always be a main source after the object is initialized.
			setFullSource ( __main_source );
		}
	}
	else {
	    // Concatenate the main and sub-sources to get the full source.
		StringBuffer full_source = new StringBuffer ();
		if ( __main_source != null ) {
			// Only want to add the sub-source if it is not an empty string
			// (it will be an empty string after the object is initialized).
			full_source.append ( __main_source );
			if ( __sub_source != null ) {
				// Have sub_source so append it to the main source.
				// Have a sub_source so append it to the main source.
				if ( __sub_source.length() > 0 ) {
					full_source.append ( SOURCE_SEPARATOR );
					full_source.append ( __sub_source );
				}
			}
			setFullSource ( full_source.toString() );
		}
	}
	// Now reset the full identifier.
	setIdentifier ();
}

/**
Set the full source from its parts.
@param main_source The main source string.
@param sub_source The sub-source string.
*/
public void setSource ( String main_source, String sub_source ) {
	setMainSource ( main_source );
	setSubSource ( sub_source );
	// The full source will be set when the parts are set.
}

/**
Set the full source from a full string.
@param source The full source string.
*/
public void setSource( String source ) {
	if ( source == null ) {
		return;
	}

	if ( source.equals("") ) {
		setMainSource ( "" );
		setSubSource ( "" );
	}
	else if ( (__behavior_mask & NO_SUB_SOURCE) != 0 ) {
		// The entire string passed in is used for the main source.
		setMainSource ( source );
	}
	else {
	    // Need to split the source into main and sub-source.
		List<String> list;
		StringBuffer sub_source = new StringBuffer ();
		int nlist;
		list =	StringUtil.breakStringList ( source, SOURCE_SEPARATOR, 0 );
		nlist = list.size();
		if ( nlist >= 1 )  {
			// Set the main source.
			setMainSource ( list.get(0) );
		}
		if ( nlist >= 2 ) {
			// Now set the sub-source.
			int iend = nlist - 1;
			for ( int i = 1; i <= iend; i++ ) {
				sub_source.append ( list.get(i) );
				if ( i != iend ) {
					sub_source.append ( SOURCE_SEPARATOR );
				}
			}
			setSubSource ( sub_source.toString() );
		}
		else {
		    // Since are only setting the main location need to set the sub-location to an empty string.
			setSubSource ( "" );
		}
	}
}

/**
Set the sub-location string (and reset the full location).
@param sub_location The sub-location string.
*/
public void setSubLocation ( String sub_location ) {
	if ( sub_location == null ) {
		return;
	}
	__sub_location = sub_location;
	setLocation();
}

/**
Set the sub-source string (and reset the full source).
@param sub_source The sub-source string.
*/
public void setSubSource ( String sub_source ) {
	if ( sub_source == null ) {
		return;
	}
	__sub_source = sub_source;
	setSource();
}

/**
Set the sub-type string (and reset the full data type).
@param sub_type The sub-type string.
*/
public void setSubType ( String sub_type ) {
	if ( sub_type == null ) {
		return;
	}
	__sub_type = sub_type;
	setType();
}

/**
Set the full data type from its parts.
This method is generally called from setMainType() and setSubType() methods to reset __full_type.
*/
public void setType () {
	String	routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setType";
	}
	int	dl = 100;

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Resetting full type from parts..." );
	}
	if ( (__behavior_mask & NO_SUB_TYPE) != 0 ) {
		// Just use the main type as the full type.
		if ( __main_type != null ) {
			// There should always be a main type after the object is initialized.
			setFullType ( __main_type );
		}
	}
	else {
	    // Concatenate the main and sub-types to get the full type.
		StringBuffer full_type = new StringBuffer ();
		if ( __main_type != null ) {
			// This should always be the case after the object is initialized.
			full_type.append ( __main_type );
			if ( __sub_type != null ) {
				// Only want to add the sub-type if it is not an empty string
				// (it will be an empty string after the object is initialized).
				if ( __sub_type.length() > 0 ) {
					// Have a sub type so append it to the main type.
					full_type.append ( TYPE_SEPARATOR );
					full_type.append ( __sub_type );
				}
			}
			setFullType ( full_type.toString() );
		}
	}
	// Now reset the full identifier.
	setIdentifier ();
}

/**
Set the full data type from its full string.
@param type The full data type string.
*/
public void setType ( String type ) {
	String routine = "";
	if ( Message.isDebugOn ) {
		routine = getClass().getSimpleName() + ".setType";
	}
	int	dl = 100;

	if ( type == null ) {
		return;
	}

	if ( Message.isDebugOn ) {
		Message.printDebug ( dl, routine, "Trying to set data type to \"" + type + "\"" );
	}

	if ( (__behavior_mask & NO_SUB_TYPE) != 0 ) {
		// The entire string passed in is used for the main data type.
		setMainType ( type );
	}
	else {
		// Need to split the data type into main and sub-location.
		List<String> list;
		StringBuffer sub_type = new StringBuffer ();
		int nlist;
		list =	StringUtil.breakStringList ( type, TYPE_SEPARATOR, 0 );
		nlist = list.size();
		if ( nlist >= 1 ) {
			// Set the main type.
			setMainType ( list.get(0) );
		}
		if ( nlist >= 2 ) {
			// Now set the sub-type.
			int iend = nlist - 1;
			for ( int i = 1; i <= iend; i++ ) {
				sub_type.append (list.get(i));
				if ( i != iend ) {
					sub_type.append ( TYPE_SEPARATOR );
				}
			}
			setSubType ( sub_type.toString() );
		}
		else {
		    // Since are only setting the main type need to set the sub-type to an empty string.
			setSubType ( "" );
		}
	}
}

/**
Return a string representation of the TSIdent.
This <b>does not</b> include the input type and name (use the overloaded version to do so).
@return A string representation of the TSIdent.
*/
public String toString () {
	return toString ( false );
}

/**
Return a string representation of the TSIdent.
@return A string representation of the TSIdent.
@param include_input If true, the input type and name are included in the identifier.  If false, the 5-part TSID is returned.
*/
public String toString ( boolean include_input ) {
	String locationType = "";
    String scenario = "";
	String sequenceID = "";
	String input_type = "";
	String input_name = "";
	if ( (__locationType != null) && (__locationType.length() > 0) ) {
	    locationType = __locationType + LOC_TYPE_SEPARATOR;
	}
	if ( (__scenario != null) && (__scenario.length() > 0) ) {
		// Add the scenario if it is not blank.
	    scenario = "." + __scenario;
	}
	if ( (__sequenceID != null) && (__sequenceID.length() > 0) ) {
		// Add the sequence ID if it is not blank.
		sequenceID = SEQUENCE_NUMBER_LEFT + __sequenceID + SEQUENCE_NUMBER_RIGHT;
	}
	if ( include_input ) {
		if ( (__input_type != null) && (__input_type.length() > 0) ) {
			input_type = "~" + __input_type;
		}
		if ( (__input_name != null) && (__input_name.length() > 0) ) {
			input_name = "~" + __input_name;
		}
	}
	return ( locationType + __full_location + "." + __full_source + "." + __full_type +
	    "." + __interval_string + scenario + sequenceID + input_type + input_name );
}

/**
Return a string representation of the identifier in the form:  Alias="xxxx" TSID="xxxx" where the alias is omitted if not set.
This string is useful for messages.
*/
public String toStringAliasAndTSID() {
    StringBuffer b = new StringBuffer();
    if ( !getAlias().equals("") ) {
        b.append ( "Alias=\"" + getAlias() + "\", " );
    }
    b.append ( "TSID=\"" );
    b.append ( toString() );
    b.append ( "\"" );
    return b.toString();
}

/**
Returns a String representation of the TSIdent,
with each individual piece of the TSIdent explicitly printed on a single line and labeled as to the part of the TSIdent it is.
@return a String representation of the TSIdent, with each individual piece of
the TSIdent explicitly printed on a single line and labeled as to the part of the TSIdent it is.
*/
public String toStringVerbose() {
	return
		"Identifier:      '" + __identifier + "'\n" +
		"Alias:           '" + __alias + "'\n" +
	    "Location Type:   '" + __locationType + "'\n" +
		"Full Location:   '" + __full_location + "'\n" +
		"Main Location:   '" + __main_location + "'\n" +
		"Sub Location:    '" + __sub_location + "'\n" +
		"Full Source:     '" + __full_source + "'\n" +
		"Main Source:     '" + __main_source + "'\n" +
		"Sub Source:      '" + __sub_source + "'\n" +
		"Full Type:       '" + __full_type + "'\n" +
		"Main Type:       '" + __main_type + "'\n" +
		"Sub Type:        '" + __sub_type + "'\n" +
		"Interval String: '" + __interval_string + "'\n" +
		"Interval Base:    " + __interval_base + "\n" +
		"Interval Mult:    " + __interval_mult + "\n" +
		"Scenario:        '" + __scenario + "'\n" +
		"SequenceID:      '" + __sequenceID + "'\n" +
		"Input Type:      '" + __input_type + "'\n" +
		"Input Name:      '" + __input_name + "'\n" +
		"Behavior Mask:    " + __behavior_mask + "\n";
}

}