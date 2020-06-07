// ZipToolkit - toolkit to work with zip files.

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import RTi.Util.Message.Message;

/**
Toolkit to work with zip files.
*/
public class ZipToolkit {

	/**
	 * Constructor.
	 */
	public ZipToolkit () {
		
	}

	// See:  http://examples.javacodegeeks.com/core-java/util/zip/zipinputstream/java-unzip-file-example/
	/**
	 * Unzip a zip file to a folder.
	 * @param zipFile path to file to unzip
	 * @param destinationFolder 
	 * @param returnList if true, return the list of unzipped file paths, if false return an empty list.
	 * @return the list if extracted files, as per 'returnList'
	 */
	public List<String> unzipFileToFolder ( String zipFile, String destinationFolder, boolean returnList )
	throws IOException, FileNotFoundException {
		List<String> outputFileList = new ArrayList<>();
		String routine = getClass().getSimpleName() + ".unzipFileToFolder";
		File directory = new File(destinationFolder);
        
		// if the output directory doesn't exist, create it
		if(!directory.exists()) {
			if ( Message.isDebugOn ) {
				Message.printDebug(1,routine,"Creating folder for unzip output: " + destinationFolder );
			}
			directory.mkdirs();
		}

		// buffer for read and write data to file
		byte[] buffer = new byte[2048];
        
		FileInputStream fInput = new FileInputStream(zipFile);
		ZipInputStream zipInput = new ZipInputStream(fInput);
        
		// Get the first entry (loop below will get the next entry to process at the end of the loop)
		ZipEntry entry = zipInput.getNextEntry();
        
		try {
			boolean isDirectory;
			String entryType = null;
			long size = 0;
			while(entry != null){
				String entryName = entry.getName();
				String unzippedFile = destinationFolder + File.separator + entryName;
				size = entry.getSize();
				File file = new File(unzippedFile);
	            
				isDirectory = entry.isDirectory();
				if ( isDirectory ) {
					entryType = "directory";
				}
				else {
					entryType = "file";
				}
				if ( Message.isDebugOn ) {
					Message.printDebug(1,routine, "Unzip " + entryType + " (" + size + " bytes) \"" + entryName + "\" to: " + file.getAbsolutePath());
				}
	            
				// create the directories of the zip directory
				if ( isDirectory ) {
					File newDir = new File(file.getAbsolutePath());
					if(!newDir.exists()) {
						boolean success = newDir.mkdirs();
						if ( !success ) {
							Message.printWarning(3,routine,"Problem creating folder \"" + file.getAbsolutePath() + "\"");
						}
					}
	            }
				else {
					// For some reason some zip files don't seem to have entries for directories so check the
					// path and create the parent folder if necessary
					File parent = file.getParentFile();
					if ( !parent.exists() ) {
						boolean success = parent.mkdirs();
						if ( !success ) {
							Message.printWarning(3,routine,"Problem creating folder \"" + parent.getAbsolutePath() + "\"");
						}
					}
					FileOutputStream fOutput = new FileOutputStream(file);
					int count = 0;
					while ((count = zipInput.read(buffer)) > 0) {
						// write 'count' bytes to the file output stream
						fOutput.write(buffer, 0, count);
					}
					fOutput.close();
					// Add the unzipped file to the list after the file was successfully unzipped.
					if ( returnList ) {
						outputFileList.add(unzippedFile);
					}
				}
				// close ZipEntry and process the next one
				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}
		}
		finally {
			// close the last ZipEntry
			zipInput.closeEntry();
			zipInput.close();
			if ( fInput != null ) {
				fInput.close();
			}
		}
		return outputFileList;
	}
	
	/**
	 * Open a BufferedReader for a zip file that contains a single file that is zipped.
	 * This is useful when a large data input file has been zipped.
	 * See:  http://www.oracle.com/technetwork/articles/java/compress-1565076.html
	 * @param zipFile zip file to read
	 * @param useTempFile if 1, save the zipped file to a temporary file; if -1, keep in memory,
	 * if 0 default based on size of file (parameter is currently not enabled).
	 */
	public BufferedReader openBufferedReaderForSingleFile ( String zipFile, int useTempFile )
	throws FileNotFoundException, IOException {
	    int BUFFER = 1024;
	    byte data[] = new byte[BUFFER];
	    BufferedReader bufferedReader = null;
	    ZipEntry entry;
	    ByteArrayInputStream bais = null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    FileInputStream fis = new FileInputStream(new File(zipFile));
	    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	    while ((entry = zis.getNextEntry()) != null) {
	    	if ( entry.isDirectory() ) {
	    		zis.close();
	    		throw new IOException ( "Zip file \"" + zipFile + "\" contains directory - expecting single file." );
	    	}
	        int count;
	        while ((count = zis.read(data, 0, BUFFER)) != -1) {
	            baos.write(data, 0, count);
	            bais = new ByteArrayInputStream(baos.toByteArray());
	        }
	        // Currently only process the first entry and then return
	        InputStreamReader isr = new InputStreamReader(bais);
	        bufferedReader = new BufferedReader(isr);
	        zis.close();
	        Message.printStatus(2,"","Opened zip file \"" + zipFile + "\".");
	        return bufferedReader;
	    }
	    // If here something is probably wrong but clean up
	    zis.close();
	    throw new IOException ( "No file found in zip file \"" + zipFile + "\"." );
	}
}
