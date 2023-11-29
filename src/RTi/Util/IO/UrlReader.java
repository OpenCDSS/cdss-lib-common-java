package RTi.Util.IO;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import RTi.Util.String.MultiKeyStringDictionary;

/**
 * General purpose URL reader to read a response from a URL using HTTP GET.
 * For example, read JSON from a web service and then handle in parsing code.
 * This class only handles GET requests, not PUT, POST, etc.
 */
public class UrlReader {

	/**
	 * URL to read.
	 */
	private String url;

	/**
	 * Optional list of header properties.
	 */
	private MultiKeyStringDictionary requestProperties = null;

	/**
	 * Optional request data (e.g., JSON).
	 */
	private String requestData = null;

	/**
	 * Optional timeout (ms), used for both connect and read.
	 */
	private int timeout = -1;

	/**
	 * UrlReader constructor.
	 * @param url URL to read.
	 */
	public UrlReader ( String url ) {
		// Call the overloaded method.
		this ( url, null, null, -1 );
	}

	/**
	 * UrlReader constructor.
	 * @param url URL to read.
	 * @param requestProperties request properties to send, can be null or empty map
	 * @param requestData request data to send, can be null
	 */
	public UrlReader ( String url, MultiKeyStringDictionary requestProperties, String requestData ) {
		// Call the overloaded method.
		this ( url, requestProperties, requestData, -1 );
	}

	/**
	 * UrlReader constructor.
	 * @param url URL to read.
	 * @param requestProperties request properties to send, can be null or empty map
	 * @param requestData request data to send, can be null
	 * @param timeout connect (time until a connection is established) and
	 * read (time until data available for reading) timeout in milliseconds, or < 0 to not specify the timeout
	 */
	public UrlReader ( String url, MultiKeyStringDictionary requestProperties, String requestData, int timeout ) {
		this.url = url;
		this.requestProperties = requestProperties;
		this.requestData = requestData;
		this.timeout = timeout;
	}

	/**
	 * Read the response for the URL.
	 * @return a UrlResponse object with the response from the read.
	 */
	public UrlResponse read () throws MalformedURLException, IOException {
        BufferedInputStream bis = null;
	    HttpURLConnection urlConnection = null;
    	StringBuilder content = new StringBuilder();
    	UrlResponse urlResponse = null;
    	int responseCode = 0;
        int bytesRead = 0;
        try {
            // Some sites need a cookie manager.
            // (see http://stackoverflow.com/questions/11022934/getting-java-net-protocolexception-server-redirected-too-many-times-error)
            CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
            // Open the input stream.
            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection)url.openConnection();
            if ( this.timeout > 0 ) {
            	urlConnection.setConnectTimeout(timeout);
            	urlConnection.setReadTimeout(timeout);
            }
            if ( this.requestProperties != null ) {
            	// Add the request properties.
				for ( int i = 0; i < this.requestProperties.size(); i++ ) {
					String key = this.requestProperties.getKey(i);
					String value = this.requestProperties.getValue(i);
					urlConnection.setRequestProperty(key, value);
				}
            }
            if ( (this.requestData != null) && !this.requestData.isEmpty() ) {
            	// Have data to pass with the request.
            	urlConnection.setDoOutput(true);
    			OutputStream outputStream = urlConnection.getOutputStream();
            	outputStream.write(requestData.getBytes("UTF-8"));
    			outputStream.flush();
    			outputStream.close();
            }
            responseCode = urlConnection.getResponseCode();
            if ( responseCode < 400 ) {
            	bis = new BufferedInputStream(urlConnection.getInputStream());
            }
            else {
            	bis = new BufferedInputStream(urlConnection.getErrorStream());
            }
            // Output the characters to the local file.
            int numCharsRead;
            int arraySize = 8192; // 8K optimal.
            byte[] byteArray = new byte[arraySize];
            while ((numCharsRead = bis.read(byteArray, 0, arraySize)) != -1) {
                // Also set the content in memory.
                if ( numCharsRead == byteArray.length ) {
                	content.append(new String(byteArray));
                }
                else {
                	byte [] byteArray2 = new byte[numCharsRead];
                	System.arraycopy(byteArray, 0, byteArray2, 0, numCharsRead);
                	content.append(new String(byteArray2));
                }
                bytesRead += numCharsRead;
            }
            if ( responseCode < 400 ) {
            	// Success.
            	urlResponse = new UrlResponse ( responseCode, bytesRead, content.toString(), "" );
            }
            else {
            	// Error.
            	urlResponse = new UrlResponse ( responseCode, 0, "", content.toString() );
            }
            return urlResponse;
        }
        finally {
            // Close the streams and connection.
            if ( bis != null ) {
            	try {
            		bis.close();
            	}
            	catch ( IOException e ) {
            	}
            }
            if ( urlConnection != null ) {
            	urlConnection.disconnect();
            }
        }
	}
}