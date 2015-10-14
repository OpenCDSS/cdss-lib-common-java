package RTi.Util.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
}