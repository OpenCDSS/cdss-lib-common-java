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
	
	/**
	 * Unzip a zip file to a folder.
	 * See:  http://examples.javacodegeeks.com/core-java/util/zip/zipinputstream/java-unzip-file-example/
	 */
	public void unzipFileToFolder ( String zipFile, String destinationFolder )
	throws IOException, FileNotFoundException
	{
		String routine = getClass().getSimpleName() + ".unzipFileToFolder";
		File directory = new File(destinationFolder);
        
		// if the output directory doesn't exist, create it
		if(!directory.exists()) 
			directory.mkdirs();

		// buffer for read and write data to file
		byte[] buffer = new byte[2048];
        
		FileInputStream fInput = new FileInputStream(zipFile);
		ZipInputStream zipInput = new ZipInputStream(fInput);
        
		ZipEntry entry = zipInput.getNextEntry();
        
		while(entry != null){
			String entryName = entry.getName();
			File file = new File(destinationFolder + File.separator + entryName);
            
			if ( Message.isDebugOn ) {
				Message.printDebug(1,routine,"Unzip file " + entryName + " to " + file.getAbsolutePath());
			}
            
			// create the directories of the zip directory
			if(entry.isDirectory()) {
				File newDir = new File(file.getAbsolutePath());
				if(!newDir.exists()) {
					boolean success = newDir.mkdirs();
					if(success == false) {
						Message.printWarning(3,routine,"Problem creating folder \"" + file.getAbsolutePath() + "\"");
					}
				}
            }
			else {
				FileOutputStream fOutput = new FileOutputStream(file);
				int count = 0;
				while ((count = zipInput.read(buffer)) > 0) {
					// write 'count' bytes to the file output stream
					fOutput.write(buffer, 0, count);
				}
				fOutput.close();
			}
			// close ZipEntry and take the next one
			zipInput.closeEntry();
			entry = zipInput.getNextEntry();
		}
        
		// close the last ZipEntry
		zipInput.closeEntry();
        
		zipInput.close();
		fInput.close();
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