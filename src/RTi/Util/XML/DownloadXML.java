//------------------------------------------------------------------------------------
// DownloadXML - utility class to download XML or XSD files
//------------------------------------------------------------------------------------
// History:
//
//      2006-06-26      Scott Townsend, RTi     Create initial version of this
//                                              utility class. This class will
//                                              download an XML or XSD file from
//						a URL passed into the static
//						method getFilePath.
//------------------------------------------------------------------------------------
// Endheader

package RTi.Util.XML;

import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringUtil;

/**
 * A utility class to download an XML file.
 */
public class DownloadXML {

/**
 * <p>Method to download an XML or XSD file from a URL. </p>
 * @param URLToFile The URL object of the XML file from which to download.
 * @throws IOException
 */
public static String getFilePath(URL URLToFile) throws IOException {
	//Local variables
	char[] fileBuf = new char[1024];
	FileWriter FW;
	InputStreamReader ISR;
	int bufLen = 0;
	String FilePath = IOUtil.getPathUsingWorkingDir((String)null),
		parseURL = null;

	// Parse the URL.getFile() String to get the filename without
	// the path. Add the file to the path from IOUtil to get
	// the local file path to download the file.
	parseURL = URLToFile.getFile();
	List URLFileString = StringUtil.breakStringList(parseURL,"/",0);
	FilePath += (String)URLFileString.get(URLFileString.size() - 1);

	// Download the file at the URL
	ISR = new InputStreamReader(URLToFile.openStream());
	FW = new FileWriter(FilePath);
	bufLen = ISR.read(fileBuf,0,1024);
	if(bufLen != -1) {
		FW.write(fileBuf);
	}

	// Loop to get all of the characters of the XSD file.
	while(bufLen == 1024) {
		bufLen = ISR.read(fileBuf,0,1024);
		if(bufLen != -1) {
			FW.write(fileBuf);
		}
	}

	// Close the streams
	FW.close();
	ISR.close();
	
	// Return the file path to the downloaded file
	return FilePath;
} // End of static method getFilePath
} // End of Utility class DownloadXML
