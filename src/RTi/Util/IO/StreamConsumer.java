// StreamConsumer - read from a stream until nothing is left

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

package RTi.Util.IO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;

/**
The StreamConsumer class is a thread that will read all the output from a stream and then expire.
This is used, for example, to read the standard error in ProcessManager
so that a process that prints extensive output to standard error will not hang.
In the future this class may be extended to to buffer
output and allow requests to retrieve the output.
*/
public class StreamConsumer extends Thread {

/**
Input stream to consume output from.
*/
private InputStream __is;

/**
Whether to log the information for the stream.  Message.printStatus(2,...) will be used.
TODO smalers 2009-04-03 Evaluate passing a logger when Java logging is implemented.
*/
private boolean __logOutput = false;

/**
Whether to save stream output, for retrieval with getOutputList().
This is simpler than adding listeners, etc., and works well with standard error.
*/
private boolean __saveOutput = false;

/**
The list of output returned with getOutputList().
*/
private List<String> __outputList = new ArrayList<>();

/**
Label with which to prefix all logged output.
*/
private String __label = null;

/**
Construct with an open InputStream.
@param is InputStream to consume.
*/
public StreamConsumer ( InputStream is ) {
	this ( is, null, false, false );
}

/**
Construct with an open InputStream.
@param is InputStream to consume.
@param label label for the front of output (embed spaces at the end if necessary)
@param logOutput if true, log the stream output using Message.printStatus(2,...).
@param saveOutput if true, save the output in a list in memory, to return with getOutputList().
*/
public StreamConsumer ( InputStream is, String label, boolean logOutput, boolean saveOutput ) {
    __is = is;
    __logOutput = logOutput;
    __saveOutput = saveOutput;
    __label = label;
}

/**
Cleans up member variables.
*/
public void finalize()
throws Throwable {
	__is = null;
	super.finalize();
}

/**
Return the output list.
@return the output list.
*/
public List<String> getOutputList() {
    return __outputList;
}

/**
Start consuming the stream.  Data will be read until a null is read, at which time the thread will expire.
*/
public void run () {
	//String routine = "StreamConsumer.run";
    try {
        BufferedReader br = new BufferedReader(	new InputStreamReader (__is) );
        String line;
		while ( true ) {
		    //Message.printStatus ( 2, routine, "Reading another line...");
		    line = br.readLine();
		    //Message.printStatus ( 2, routine, "...done reading another line.");
		    if ( line == null ) {
		        break;
		    }
			// TODO smalers 2007-05-09 - this is where output could be passed to listening code.
		    if (__logOutput) {
		        if ( __label == null ) {
		            Message.printStatus(2, "StreamConsumer", "\"" + line + "\"" );
		        }
		        else {
                    Message.printStatus(2, "StreamConsumer", __label + "\"" + line + "\"" );
                }
		    }
		    if ( __saveOutput ) {
		        __outputList.add ( line );
		    }
		}
	}
	catch ( Exception e ) {
		// Should not happen - uncomment if troubleshooting.
	    // Exceptions may be thrown if the system is busy and there is a delay in closing the file and the next pending read.
		//String routine = "StreamConsumer.run";
		//Message.printWarning ( 2, routine, "Exception processing stream." );
		//Message.printWarning ( 2, routine, e );
	}
}

}