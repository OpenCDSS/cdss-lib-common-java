// CommandFile - command file data

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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
 * This class stores basic data about a command file.
 * It was originally implemented to allow ordering of command files in automated testing.
 * Because automated testing and other processing may involve many command files,
 * each with many commands, this class currently does not retain the command file
 * contents in memory.  Only annotations are read and retained,
 * to facilitate managing command files.
 */
public class CommandFile {
	
	/**
	 * Expected status when run, as string.
	 */
	private String expectedStatusString = "";

	/**
	 * Expected status when run, as enumeration.
	 */
	private CommandStatusType expectedStatus = CommandStatusType.UNKNOWN;

	/**
	 * Name of the command file.
	 * Could use a File but work with strings first.
	 */
	private String filename = "";
	
	/**
	 * Identifier for the command file, used with annotations.
	 */
	private String id = "";
	
	/**
	 * Order operator if set in annotation.
	 */
	private CommandFileOrderType orderOperatorType = null;

	/**
	 * Order 'Id' if set in annotation.
	 */
	private String orderId = "";
	
	/**
	 * Test suite for the command file.
	 */
	private String testSuite = "";
	
	/**
	 * Constructor.
	 * @param command file name, absolute path
	 * @param readAnnotations if true, read the annotations
	 */
	public CommandFile ( String filename, boolean readAnnotations ) {
		this.filename = filename;
		if ( readAnnotations ) {
			readAnnotations();
		}
	}
	
	/**
	 * Return the expected status string, determined from `@expectedStatus status` annotation comment.
	 * @return the expected status string.
	 */
	public String getExpectedStatusString () {
		return this.expectedStatusString;
	}

	/**
	 * Return the expected status, determined from `@expectedStatus status` annotation comment.
	 * @return the expected status.
	 */
	public CommandStatusType getExpectedStatus() {
		return this.expectedStatus;
	}

	/**
	 * Return the command file name as full path.
	 * @return the command file name as full path.
	 */
	public String getFilename () {
		return this.filename;
	}

	/**
	 * Return the command file identifier, determined from `@id` annotation comment.
	 * @return the command file identifier.
	 */
	public String getId () {
		return this.id;
	}

	/**
	 * Return the command file order 'id', determined from `@order before/after id` annotation comment.
	 * @return the command file order.
	 */
	public String getOrderId () {
		return this.orderId;
	}

	/**
	 * Return the test suite, determined from `@testSuite suiteName` annotation comment.
	 * @return the test suite.
	 */
	public String getTestSuite () {
		return this.testSuite;
	}

	/**
	 * Return the command file order operator, determined from `@order before/after id` annotation comment.
	 * @return the command file order operator.
	 */
	public CommandFileOrderType getOrderOperatorType () {
		return this.orderOperatorType;
	}

	/**
	 * Read the command file annotations from the file and populate internal data.
	 */
	public void readAnnotations () {
		String routine = getClass().getSimpleName() + ".readAnnotations";
		BufferedReader in = null;
		try {
			in = new BufferedReader ( new FileReader( this.filename ) );
		}
		catch ( FileNotFoundException e ) {
			// Ignore since no annotations for missing file.
			Message.printWarning(3, routine, "File \"" + this.filename + "\" not found.  Cannot read annotations.");
			return;
		}
		try {
			String line;
			String lineUpper;
			int index;
			while ( true ) {
				line = in.readLine();
				if ( line == null ) {
					break;
				}
				// Trim in case indented and convert to upper case for comparisons.
				line = line.trim();
				lineUpper = line.toUpperCase();
				// Check @expectedStatus status
				index = lineUpper.indexOf("@EXPECTEDSTATUS");
				if ( index >= 0 ) {
					// Get the status as the next token after the tag.
					this.expectedStatusString = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 1);
					// Translate variations to the official name recognized by RunCommands().
    				if ( expectedStatusString.equalsIgnoreCase("Warn") ) {
    					this.expectedStatus = CommandStatusType.WARNING;
    				}
    				else if ( expectedStatusString.equalsIgnoreCase("Fail") ) {
    					this.expectedStatus = CommandStatusType.FAILURE;
    				}
    				else if ( expectedStatusString.equalsIgnoreCase("Success") ) {
    					this.expectedStatus = CommandStatusType.SUCCESS;
    				}
				}
				// Check @id CommandFileId
				index = lineUpper.indexOf("@ID");
				if ( index >= 0 ) {
					// Get the identifier as the next token after the tag.
					this.id = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 1);
					Message.printStatus(2, routine, "Id is: " + this.id);
				}
				// Check @order before/after CommandFileId
				index = lineUpper.indexOf("@ORDER");
				if ( index >= 0 ) {
					// Get the operator and command file id as the next tokens after the tag.
					String operator = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 1);
					this.orderOperatorType = CommandFileOrderType.valueOfIgnoreCase(operator);
					this.orderId = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 2);
					Message.printStatus(2, routine, "order operator: " + this.orderOperatorType);
					Message.printStatus(2, routine, "order id: " + this.orderId);
				}
				// Check @testSuite suiteName
				index = lineUpper.indexOf("@TESTSUITE");
				if ( index >= 0 ) {
					// Get the test suite name as the next token after the tag.
					this.testSuite = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 1);
					Message.printStatus(2, routine, "testsuite: " + this.testSuite);
				}
			}
		}
		catch ( IOException e ) {
			// Ignore - just don't have the tag that is being searched for
		}
		finally {
			try {
				in.close();
			}
			catch ( IOException e ) {
				// Not much to do but absorb - should not happen
			}
		}
	}
	
}