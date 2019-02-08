// XMRGViewer - viewer for XMRG data

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

// ----------------------------------------------------------------------------
// XMRGViewer - the main controlling class for running the XMRGProcessor
// ----------------------------------------------------------------------------
// Copyright:   See the COPYRIGHT file
// ----------------------------------------------------------------------------
// History:
// 2004-09-08	J. Thomas Sapienza, RTi	Initial version.
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------

package RTi.GIS.GeoView;

import java.io.File;

import RTi.Util.Message.Message;

/**
This class is the main controlling class for running the XMRGProcessor.  It 
sets up logging and processes command-line arguments.<p>
The XMRGProcessor is a program to automatically download XMRG files from a
server, convert them to the local endian type, clip them to a desired region,
and then generate shapefiles from them.
REVISIT (JTS - 2006-05-22)
This class may be able to be removed, since most of the important functionality
now is in a separate panel class.
*/
public class XMRGViewer {

/**
The name of the program.
*/
public static final String PROGRAM_NAME = "XMRGProcessor";

/**
The program version.
*/
public static final String PROGRAM_VERSION = "0.1.0 2004-10-14";

/**
The home directory in which the log file should be generated.
*/
//private static String __home = "/opt/RTi/NWSRFS/logs";
private static String __home = "./";

private static String __xmrgFilename = null;

/**
Initializes the message system.
*/
private static void initializeMessage() {
	String routine = "XMRGViewer.initializeMessage";

	//set up message levels 
	Message.setDebugLevel(Message.TERM_OUTPUT, 50);
	Message.setDebugLevel(Message.LOG_OUTPUT, 50);
	Message.setStatusLevel(Message.TERM_OUTPUT, 1);
	Message.setStatusLevel(Message.LOG_OUTPUT, 2);
	Message.setWarningLevel(Message.TERM_OUTPUT, 1);
	Message.setWarningLevel(Message.LOG_OUTPUT, 2);

	Message.isDebugOn = false;

	String fs = File.separator;

	try {	
		String logFileName = __home + fs + "XMRGProcessor.log";
		Message.openLogFile(logFileName);
	}
	catch (Exception e) {
		Message.printWarning(1, routine, "Couldn't open log file: "
			+ __home + fs + "XMRGProcessor.log");
		Message.printWarning(2, routine, e);
		System.exit(1);
	}
}

/**
Start main application.
@param args Command line arguments.
*/
public static void main(String args[]) {
	String routine = "XMRGViewer.main";

	//set up message class
	initializeMessage();

	Message.printStatus(10, routine, "Parsing command line arguments.");
	try {
		parseArgs(args);
	}
	catch (Exception e) {
		Message.printWarning(2, routine, e);
		Message.printWarning(2, routine, 
			"Error parsing arguments passed into " + PROGRAM_NAME  
			+ "Exiting...");
		quitProgram(5);
	}

	try {
		new XMRGViewerJFrame(__xmrgFilename, false);
	}
	catch (Exception e) {
		Message.printWarning(1, routine, e);
	}
}


/**
Parses command line arguments.
@param args the arguments from the command line.
*/
public static void parseArgs(String[] args)
throws Exception {
	String routine = "XMRGViewer.parseArgs";

	int length = args.length;

	for (int i = 0; i < length; i++) {
		if (args[i].equals("-f")) {
			if (i == (length - 1)) {				
				Message.printWarning(1, routine,
					"No value set for parameter '-f'");
				throw new Exception("No value set for "
					+ "parameter '-f'");
			}
			i++;
			__xmrgFilename = args[i];
		}
		else {
			Message.printWarning(1, routine,
				"Unknown parameter: '" + args[i] + "'");
		}
	}
}	

/**
Clean up and exit application.
@param status Program exit status.
*/
private static void quitProgram(int status) {
	String routine = "XMRGViewer.quitProgram";
	Message.printStatus(1, routine, "Exiting with status: " + status + ".");
	System.exit(status);
}

}
