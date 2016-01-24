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
import java.util.zip.GZIPInputStream;

import RTi.Util.Message.Message;

/**
Toolkit to work with gzip files.
*/
public class GzipToolkit {

	/**
	 * Constructor.
	 */
	public GzipToolkit () {
		
	}

	// See:  http://examples.javacodegeeks.com/core-java/io/fileinputstream/decompress-a-gzip-file-in-java-example/
	/**
	 * Unzip a zip file to a folder.
	 * @return the path to the unzipped file
	 * @param gzipFile path to gzipped file
	 * @param destinationFolder folder where unzipped file should be created
	 * @param unzippedFile name of unzippled file (with no leading path) -
	 * if null the output filename will default to input with .gz extension
	 */
	public String unzipFileToFolder ( String gzipFile, String destinationFolder, String unzippedFile )
	throws IOException, FileNotFoundException
	{
		// Output filename is same as input but without .gzip extension
		File directory = new File(destinationFolder);
		if ( (unzippedFile != null) && !unzippedFile.isEmpty() ) {
			unzippedFile = destinationFolder + File.separator + unzippedFile;
		}
		else {
			String extension = IOUtil.getFileExtension(gzipFile);
			File gf = new File(gzipFile);
			unzippedFile = destinationFolder + File.separator + gf.getName().substring(0,gf.getName().length() - extension.length() - 1);
		}
        
		// if the output directory doesn't exist, create it
		if(!directory.exists()) {
			directory.mkdirs();
		}

		// buffer for read and write data to file
		byte[] buffer = new byte[2048];

		FileInputStream fInput = null;
		GZIPInputStream gzipInput = null;
		FileOutputStream fOutput = null;
		try {
			fInput = new FileInputStream(gzipFile);
			gzipInput = new GZIPInputStream(fInput);
	        
			// TODO SAM 2016-01-23 What to do when gzip file contains more than single file?
			// Does not seem to often be the case because something like tar is used to create one file?
	
			fOutput = new FileOutputStream(unzippedFile);
			int count = 0;
			while ((count = gzipInput.read(buffer)) > 0) {
				// write 'count' bytes to the file output stream
				fOutput.write(buffer, 0, count);
			}
		}
		finally {
			if ( gzipInput != null ) {
				gzipInput.close();
			}
			if ( fInput != null ) {
				fInput.close();
			}
			if ( fOutput != null ) {
				fOutput.close();
			}
		}
		return unzippedFile;
	}
	
	/**
	 * Open a BufferedReader for a gzip file that contains a single file that is gzipped.
	 * This is useful when a large data input file has been gzipped.
	 * See:  ZipToolkit
	 * @param gzipFile zip file to read
	 * @param useTempFile if 1, save the zipped file to a temporary file; if -1, keep in memory,
	 * if 0 default based on size of file (parameter is currently not enabled).
	 */
	public BufferedReader openBufferedReaderForSingleFile ( String gzipFile, int useTempFile )
	throws FileNotFoundException, IOException {
	    int BUFFER = 1024;
	    byte data[] = new byte[BUFFER];
	    BufferedReader bufferedReader = null;
	    ByteArrayInputStream bais = null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    FileInputStream fis = new FileInputStream(new File(gzipFile));
	    GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(fis));
        int count;
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            baos.write(data, 0, count);
            bais = new ByteArrayInputStream(baos.toByteArray());
        }
        // Currently only process the first entry and then return
        InputStreamReader isr = new InputStreamReader(bais);
        bufferedReader = new BufferedReader(isr);
        zis.close();
        return bufferedReader;
	}
}