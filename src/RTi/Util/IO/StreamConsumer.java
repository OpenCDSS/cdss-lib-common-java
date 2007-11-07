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

InputStream __is;	// Input stream to consume output from

/**
Construct with an open InputStream.
@param is InputStream to consume.
*/
public StreamConsumer ( InputStream is )
{	__is = is;
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
Start consuming the stream.  Data will be read until a null is read, at which
time the thread will expire.
*/
public void run ()
{	try {	BufferedReader br = new BufferedReader(
						new InputStreamReader (__is) );
		while ( br.readLine() != null ) {
			// TODO SAM 2007-05-09 - this is where output could be passed
			// to listening code.
		}
	}
	catch ( Exception e ) {
		// Should not happen but print until code tests out...
		String routine = "StreamConsumer.run";
		Message.printWarning ( 2, routine,
		"Exception processing stream." );
		Message.printWarning ( 2, routine, e );
	}
}

} // End StreamConsumer
