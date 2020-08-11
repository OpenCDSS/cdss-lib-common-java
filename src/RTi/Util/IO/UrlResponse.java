package RTi.Util.IO;

/**
 * Class to store reponse from URL, including content, error, and response code.
 * @author sam
 *
 */
public class UrlResponse {
	
	/**
	 * Bytes read.
	 */
	private int bytesRead = 0;
	
	/**
	 * Response code.
	 */
	private int responseCode = 0;
	
	/**
	 * Response content if successful, empty if error.
	 */
	private String response = "";
	
	/**
	 * Error response content if an error, empty if successful.
	 */
	private String responseError = "";

	/**
	 * UrlResponse constructor.
	 */
	public UrlResponse ( int responseCode, int bytesRead, String response, String responseError ) {
		this.responseCode = responseCode;
		this.bytesRead = bytesRead;
		this.response = response;
		this.responseError = responseError;
	}

	/**
	 * Return the number of bytes read.
	 */
	public int getBytesRead() {
		return this.bytesRead;
	}

	/**
	 * Return the response content.
	 */
	public String getResponse() {
		return this.response;
	}

	/**
	 * Return the response code.
	 */
	public int getResponseCode() {
		return this.responseCode;
	}

	/**
	 * Return the response content for an error.
	 */
	public String getResponseError() {
		return this.responseError;
	}

	/**
	 * Return whether an error occurred, true if the response code is >= 400.
	 */
	public boolean hadError () {
		if ( this.responseCode >= 400 ) {
			return true;
		}
		else {
			return false;
		}
	}
}