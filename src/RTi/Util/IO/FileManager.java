// FileManager - class to manage an applications files, focusing on temporary and cached files

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

import java.util.ArrayList;
import java.util.List;

/**
 * The FileManager class s a singleton that can be created for an application session with the following uses:
 * <ul>
 * <li>  Manage temporary files so that they can be deleted at the end or beginning of a session.</li>
 * <li>  Manage cached files that need to persist between sessions, with expiration time.</li>
 * </ul>
 * The files that are managed can be persisted to a file so that the state of the FileManager can be initialized in the
 * next session.  The FileManagerUI can be used to display and handle files, such as clearing temporary or cached files.
 */
public class FileManager {

	/**
	 * Singleton instance.
	 */
	private static FileManager instance = null;
	
	/**
	 * Private list of managed files.
	 */
	private List<FileManagerFile> managedFiles = null;
	
	/**
	 * Create an instance of the singleton.
	 */
	private FileManager () {
		this.managedFiles = new ArrayList<>();
	}

	/**
	 * Return the file at the specified index (0+), or null if out of bounds.
	 * @return the file at the specified index (0+), or null if out of bounds
	 */
	public FileManagerFile get ( int index ) {
		if ( (index < 0) || (index >= size()) ) {
			return null;
		}
		else {
			return this.managedFiles.get ( index );
		}
	}

	/**
	 * Return the all the files
	 * @return the list of all the files, guaranteed to be non-null but may be empty
	 */
	public List<FileManagerFile> getAll () {
		if ( this.managedFiles == null ) {
			return new ArrayList<>();
		}
		else {
			return this.managedFiles;
		}
	}

	/**
	 * Return the singleton instance of the FileManager.
	 */
	public static FileManager getInstance () {
		if ( instance == null ) {
			instance = new FileManager ();
		}
		return instance;
	}
	
	/**
	 * Return the size of the list of files.
	 * @return the size of the list of files
	 */
	public int size () {
		if ( this.managedFiles == null ) {
			return 0;
		}
		else {
			return this.managedFiles.size();
		}
	}
}