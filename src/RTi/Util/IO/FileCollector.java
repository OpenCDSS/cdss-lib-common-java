package RTi.Util.IO;

/**
 * This class is the same as in cleanup other than it compiles at jdk1.4
 */
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Vector;

/**
 * Collects a list of files from a folder and it's subfolders, given a fileMask.
 * 
 * Usage:
 * <pre>
 *   String root = ".";
 *   String fileMask = ".java" // find any file containing .java
 * 
 *   FileCollector fileCollector = new FileCollector(root, fileMask);
 *   fileCollector.getFiles();
 *   // free memory
 *   fileCollector = null;
 *   </pre>
 * 
 * @author dre
 */
public class FileCollector 
{
  private boolean _debug = false;
  private boolean _recursive = false;
 
  // TODO jdk1.6 private Vector<String> _files = new Vector<String>();
  private List _files = new Vector();
  
  /**
   * Constructor
   *
   * @param folderPath    Root-folder where to start the search
   * @param fileMask      Part that must be in file that you are looking for
   *                                (in your case ".ini")
   * @param               Whether to recursively descend into sub-folders
   * @throws Exception 
   */
  public FileCollector(String folderPath, String fileMask, boolean recursive)
  {
    _recursive = recursive;
    
    File rootFile = new File(folderPath);
    
    if (!rootFile.exists())
      {
        throw new RuntimeException("FolderPathDoesNotExist: " + folderPath);
      }
    
    findFiles(rootFile, fileMask);
  }

  /**
   * Find (recursively) all files with a specific
   * mask and do something with each file...
   *
   * @param folder    Root-folder where to start the search
   * @param fileMask  Part that must be in file that you are looking for
   *                  (in your case ".ini")
   */
  private void findFiles(File folder, final String fileMask)
  {
    // since you are running Windows you probably don't care about case sensitivity...
    final boolean ignoreCase = false;

    // read all files in actual folder that meets the specified criteria
    File[] files = folder.listFiles(new FileFilter()
    {
      public boolean accept(File file)
      {
        String fileName = file.getName();
        if (fileMask.length() == 0)
          {
            return file.isFile();
          }
        int index = fileName.length() - fileMask.length();
        
        //TODO: dre Should support either globbing or java RE conventions
        return file.isFile() && fileMask.regionMatches(ignoreCase, 0, fileName, index, fileMask.length());
      }
    });

    // do something with the files found
    for (int i = 0; files != null && i < files.length; i++)
      {
        if (files[i] != null)
          {
            //...
            if (_debug) System.out.println(files[i]);
            _files.add(files[i].getAbsolutePath());
            //...
          }
      }
    
if (_recursive)
  {
    // for all subfolder to the actual folder...
    File[] folders = folder.listFiles(new FileFilter()
    {
      public boolean accept(File dir)
      {
        return dir.isDirectory();
      }
    });

    int currentFolderIndex = 0;

    for (int i = 0; folders != null && i < folders.length; i++)
      {
        // move to next folder (recursively) and do the
        // above processing on the files found there
        findFiles(folders[currentFolderIndex++], fileMask);
      }
  }
  }
  
  /** 
   * Returns the list of files found under the root folder matching the 
   * file mask.
   *  
   * @return
   */
  public List getFiles()
  {
    return _files;
  }

  /**
   * Returns shorter names for display by stripping the specified root & extension.
   * 
   * @param root
   * @return abbreviated file names
   */
  public static List getAbbrNames(List fileNames, String root, String ext)
  {
    List _filesAbbr = new Vector(fileNames.size());

    File f = new File(root);
    int beginIndex = f.getAbsolutePath().length() + 1; // for following sep.
    int extLength = ext.length();
    String s;
   
    int len = fileNames.size();
    for (int i = 0; i < len; i++)
      {
        s = ((String)fileNames.get(i)).substring(beginIndex, ((String)fileNames.get(i)).length()- extLength);
        _filesAbbr.add(s);
      }
    return _filesAbbr;
  }
  
  
  
  /**
   * Test harness
   * 
   * Should print all files .java files
   * @param args
   */
  public static void main(String[] args)
  {
    String folderPath = ".";
    String mask = ".java";

    new FileCollector(folderPath, mask, true);
  }
}