// CommandProcessor - this interface is implemented by classes that can process string commands

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

package RTi.Util.IO;

import java.util.List;

/**
This interface is implemented by classes that can process string commands.
The command processor should perform the following:
<ol>
<li>	Implement the methods in this interface, in order to to be able to
	process and interact with commands.</li>
<li>	Maintain important data.  For example, the CommandProcessor should
	maintain the list of data objects relevant to a data set, as an
	intermediary between commands.   Depending on the complexity of the
	data set, the CommandProcessor may be very simple, or may need extensive
	knowledge about the data set.</li>
</ol>
*/
public interface CommandProcessor
{
    
/**
Return a list of commands being managed by the processor.  This allows generic utility code to
further process the commands.
@return the list of commands being managed by the processor.
*/
public List<Command> getCommands();

/**
Return a property given the property name (request).  Return null if not found.
@param prop Name of property being requested.
@return the Property contents as a Prop instance.
@exception Exception if the property is not recognized.
*/
public Prop getProp ( String prop ) throws Exception;

/**
Return a property's contents given the property name.  Return null if not found.
@param prop Name of property being requested.
@return the Property contents as the data object.
@exception Exception if the property is not recognized (this is not handled, null is returned instead).
*/
public Object getPropContents ( String prop ) throws Exception;

/**
Process (run) a command.
@param command Command to process.
@exception CommandWarningException If there are warnings running the command.
@exception CommandException If there is an error running the command.
*/
// TODO SAM 2005-05-05 need to implement in a generic way
//public void processCommands () throws CommandException;

// TODO SAM 2007-02-16 How to pass back useful information when an exception
/**
Process a request.  This provides a generalized way for commands
to call specialized methods in the command processor.
This version allows properties to be passed as name/value pairs
in a PropList.  It also allows multiple results to be returned.
The parameter and result names/contents should be well documented by specific processors.
@return a CommandProcessorRequestResultsBean containing the results of a request and other useful information.
@param request A request keyword.
@param parameters Input to the request, as a PropList.
@exception Exception if there is an error processing the request.
*/
public CommandProcessorRequestResultsBean processRequest ( String request, PropList parameters ) throws Exception;

/**
Set a property given the property.
@param prop Property to set.
@exception Exception if the property is not recognized or cannot be set.
*/
public void setProp ( Prop prop ) throws Exception;

/**
Set a property's contents given the property name.
@param prop Property name.
@param contents Property contents.
@exception Exception if the property is not recognized or cannot be set.
*/
public void setPropContents ( String prop, Object contents ) throws Exception;

}
