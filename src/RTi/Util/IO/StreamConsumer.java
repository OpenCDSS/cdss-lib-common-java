// ----------------------------------------------------------------------------
// StreamConsumer - read from a stream until nothing is left
// ----------------------------------------------------------------------------
// Copyright:  See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History:
//
// 2003-12-03	Steven A. Malers, RTi	Initial version.  Define for use with
//					ProcessManager, mainly to consume the
//					standard error of a process so that it
//					does not hang when its buffer gets full.
// 2005-04-26	J. Thomas Sapienza, RTi	Added finalize().
// 2007-05-08	SAM, RTi		Cleanup code based on Eclipse feedback.
// ----------------------------------------------------------------------------
// EndHeader

package RTi.Util.IO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import RTi.Util.Message.Message;

/**
The StreamConsumer class is a thread that will read all the output from a
stream and then expire.  This is used, for example, to read the standard error
in ProcessManager so that a process that prints extensive output to standard
error will not hang.  In the future this class may be extended to to buffer
output and allow requests to retrieve the output.
*/
public class StreamConsumer extends Thread
{

/**
Input stream to consume output from.
*/
private InputStream __is;

/**
Whether to log the information for the stream.  Message.printStatus(2,...) will be used.
TODO SAM 2009-04-03 Evaluate passing a logger when Java logging is implemented.
*/
private boolean __logOutput = false;

/**
Whether to save stream output, for retrieval with getOutputList().  This is simpler than adding listeners, etc.,
and works well with standard error.
*/
private boolean __saveOutput = false;

/**
The list of output returned with getOutputList().
*/
private List<String> __outputList = new ArrayList<String>();

/**
Label with which to prefix all logged output.
*/
private String __label = null;

/**
Construct with an open InputStream.
@param is InputStream to consume.
*/
public StreamConsumer ( InputStream is )
{	this ( is, null, false, false );
}

/**
Construct with an open InputStream.
@param is InputStream to consume.
@param logOutput if true, log the stream output using Message.printStatus(2,...).
@param saveOutput if true, save the output in a list in memory, to return with getOutputList().
*/
public StreamConsumer ( InputStream is, String label, boolean logOutput, boolean saveOutput )
{   __is = is;
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
public List<String> getOutputList()
{
    return __outputList;
}

/**
Start consuming the stream.  Data will be read until a null is read, at which time the thread will expire.
*/
public void run ()
{	//String routine = "StreamConsumer.run";
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
			// TODO SAM 2007-05-09 - this is where output could be passed to listening code.
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
		// Should not happen - uncomment if troubleshooting
	    // Exceptions may be thrown if the system is busy and there is a delay in closing
	    // the file and the next pending read
		//String routine = "StreamConsumer.run";
		//Message.printWarning ( 2, routine, "Exception processing stream." );
		//Message.printWarning ( 2, routine, e );
	}
}

}