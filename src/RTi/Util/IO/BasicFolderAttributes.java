// BasicFolderAttributes - similar to java.nio.BasicFileAttributes for files in a folder

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2025 Colorado Department of Natural Resources

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
import java.nio.file.attribute.FileTime;

/**
 * This class implements features similar to the BasicFileAttributes interface, as a summation of all files in a folder.
 * This can be used to populate attributes without reading an existing folder's attributes.
 * Although file attributes are slightly different for Windows and Unix, this class is generic and can be used for both.
 */
public class BasicFolderAttributes {
	File folder = null;
    private FileTime folderCreationTime = null;
    private FileTime folderModifiedTime = null;
    //private final FileTime folderLastAccessTime;

    private long size = -1;
    private FileTime filesMinCreationTime = null;
    private FileTime filesMinLastModifiedTime = null;
    private FileTime filesMaxCreationTime = null;
    private FileTime filesMaxLastModifiedTime = null;

    //private final boolean isSymbolicLink;

    /**
     * Create an object containing folder attributes.
     * @param folder the folder being examined
     * @param folderCreationTime folder creation time
     * @param folderModifiedTime folder modified time
     * @param size size of all files in a folder
     * @param fileMinCreationTime minimum (earliest) creation time for all files in a folder
     * @param fileMaxCreationTime maximum (latest) creation time for all files in a folder
     * @param fileMinLastModifiedTime minimum last modified time for all files in a folder
     * @param fileMaxLastModifiedTime maximum last modified time for all files in a folder
     */
    public BasicFolderAttributes (
    	File folder,
    	FileTime folderCreationTime,
    	FileTime folderModifiedTime,
        long size,
    	FileTime filesMinCreationTime, FileTime filesMaxCreationTime,
    	FileTime filesMinLastModifiedTime, FileTime filesMaxLastModifiedTime ) {
    	// Folder.
    	this.folder = folder;
        this.folderCreationTime = folderCreationTime;
        this.folderModifiedTime = folderModifiedTime;
        this.size = size;
        // Files in the folder.
        this.filesMinCreationTime = filesMinCreationTime;
        this.filesMaxCreationTime = filesMaxCreationTime;
        this.filesMinLastModifiedTime = filesMinLastModifiedTime;
        this.filesMaxLastModifiedTime = filesMaxLastModifiedTime;
    }

    /**
     * Return the minimum creation time for all files in a folder.
     * @return the minimum creation time for all files in a folder
     */
    public FileTime getFilesMinCreationTime() {
        return this.filesMinCreationTime;
    }

    /**
     * Return the minimum last modified time for all files in a folder.
     * @return the minimum last modified time for all files in a folder
     */
    public FileTime getFilesMinLastModifiedTime() {
        return this.filesMinLastModifiedTime;
    }

    /**
     * Return the maximum creation time for all files in a folder.
     * @return the maximum creation time for all files in a folder
     */
    public FileTime getFilesMaxCreationTime() {
        return this.filesMaxCreationTime;
    }

    /**
     * Return the maximum last modified time for all files in a folder.
     * @return the maximum last modified time for all files in a folder
     */
    public FileTime getFilesMaxLastModifiedTime() {
        return this.filesMaxLastModifiedTime;
    }

    /**
     * Return the folder as a File object
     * @return the folder as a File object
     */
    public File getFolder() {
        return this.folder;
    }

    /**
     * Return the creation time for the folder.
     * @return the creation time for the folder
     */
    public FileTime getFolderCreationTime() {
        return this.folderCreationTime;
    }

    /**
     * Return the last modified time for all files in a folder.
     * @return the last modified time for all files in a folder
     */
    public FileTime getFolderModifiedTime() {
        return this.folderModifiedTime;
    }

    /**
     * Return the size of all files in a folder.
     * @return the size of all files in a folder
     */
    public long getSize() {
        return size;
    }
}