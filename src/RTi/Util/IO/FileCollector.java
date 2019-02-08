// FileCollector - collects a list of files from a folder and it's subfolders, given a fileMask.

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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

// TODO SAM 2017-03-13 need to decide if this should be removed since used in legacy code.
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
 
  private List<String> _files = new ArrayList<String>();
  
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
  public List<String> getFiles()
  {
    return _files;
  }

  /**
   * Returns shorter names for display by stripping the specified root & extension.
   * 
   * @param root
   * @return abbreviated file names
   */
  public static List<String> getAbbrNames(List<String> fileNames, String root, String ext)
  {
    List<String> _filesAbbr = new ArrayList<String>(fileNames.size());

    File f = new File(root);
    int beginIndex = f.getAbsolutePath().length() + 1; // for following sep.
    int extLength = ext.length();
    String s;
   
    int len = fileNames.size();
    for (int i = 0; i < len; i++)
      {
        s = fileNames.get(i).substring(beginIndex, fileNames.get(i).length()- extLength);
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
