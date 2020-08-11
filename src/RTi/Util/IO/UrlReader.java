package RTi.Util.IO;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Read a response from a URL.
 * @author sam
 *
 */
public class UrlReader {

	/**
	 * URL to read.
	 */
	private String url;
	
	/**
	 * UrlReader constructor.
	 * @param url URL to read.
	 */
	public UrlReader ( String url ) {
		this.url = url;
	}
	
	/**
	 * Read the URL.
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
            // Some sites need cookie manager
            // (see http://stackoverflow.com/questions/11022934/getting-java-net-protocolexception-server-redirected-too-many-times-error)
            CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));
            // Open the input stream...
            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection)url.openConnection();
            responseCode = urlConnection.getResponseCode();
            if ( responseCode < 400 ) {
            	bis = new BufferedInputStream(urlConnection.getInputStream());
            }
            else {
            	bis = new BufferedInputStream(urlConnection.getErrorStream());
            }
            // Output the characters to the local file...
            int numCharsRead;
            int arraySize = 8192; // 8K optimal
            byte[] byteArray = new byte[arraySize];
            while ((numCharsRead = bis.read(byteArray, 0, arraySize)) != -1) {
                // Also set the content in memory
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
            // Close the streams and connection
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